package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.LUInfoExtract;

public class Station {
	public static int recentThreshMilli = 30*60*1000;
	
	private int id;		
	private ArrayList<Integer> linkedOpStations = null;
	public FastList<GpsDataLike> points = new FastList<GpsDataLike>();
	public ArrayList<RecentVehicleInfo> recentVehicleList = new ArrayList<RecentVehicleInfo>();
	private long ptsChangeTSMinIncl = -1;
	private long ptsChangeTSMaxIncl = -1;
	
	public int getLatestEntryForVehicle(int vehicleId) {
		return RecentVehicleInfo.getLatestEntryForVehicle(vehicleId, recentVehicleList);
	}
	public boolean removeLatestEntryForVehicle(int vehicleId, ManagementUnit mu) {
		boolean retval = RecentVehicleInfo.removeLatestEntryForVehicle(vehicleId, recentVehicleList);
		makeDirty(mu);
		return retval;
	}
	public RecentVehicleInfo addRecentVehicle(RecentVehicleInfo toAdd, ManagementUnit mu) {
		RecentVehicleInfo retval = RecentVehicleInfo.addRecentVehicle(toAdd, recentVehicleList);
		makeDirty(mu);
		return retval;
	}
	public void makeDirty(ManagementUnit mu) {
		CurrentStation currStn = mu.getCurrentStationForStation(this);
		if (currStn != null)
			currStn.setDirty(true);
	}
	
	public boolean isCurrMergeable(Station other) {
		//check if other is later than last pt of this and if so are the distance within mergeable threshold
		GpsDataLike otherLast = other.points.get(other.points.size()-1);
		if (otherLast == null)
			return false;
		GpsDataLike justBef = points.get(otherLast);
		if (justBef == null)
			return false;
		return GpsDataLike.helperIsWithinThresh(justBef.getAvgLonInt()-otherLast.getAvgLonInt(), justBef.getAvgLatInt() - otherLast.getAvgLatInt(), CurrentStation.currMergeLonDeltaInt, CurrentStation.currMergeLatDeltaInt);
	}
	
	public synchronized void removeFromList(Connection conn, GpsDataLike data, BestMergeDistResult mergeResult) {
		FastList<GpsDataLike> theList = points;
		int index = mergeResult.ptIndexBefore;
		int toDoSmoothingFrom = index;
		GpsDataLike old = theList.get(index);
		if (old == null || old.getEndGpsRecordTimeIncl() < data.getGpsRecordTime())
			return;
		
		helperSetMinMaxOnChange(data);
		
		
		//will create a moving average of pts until X min back with no gap being more than Y min ... also update support based
		//on many pts it saw load of ..
		old.setSupport(old.getSupport()-1);
		if (old.getSupport() == 0) {
			System.out.println("[DBG rmpt fin]"+data);
			theList.remove(index);
		}
		else {
			System.out.println("[DBG rmpt partial]"+data+ " rem supp:"+old.getSupport());
		}
		GpsDataLike.helperDoSmoothingFrom(theList, toDoSmoothingFrom, this);
	}
	public synchronized void dbgCheckSupport() {
		for (int i=0,is=this.points.size(); i<is; i++) {
			GpsDataLike pt = this.points.get(i);
			if (pt.getAvgSupport() == 0 || pt.getSupport() == 0) {
				int dbg = 1;
				dbg++;
			}
		}
	}
	public synchronized void addToList(Connection conn, GpsDataLike data, BestMergeDistResult mergeResult, int movingOpStationId) {
		MiscInner.Pair changeFrom = null;
		FastList<GpsDataLike> theList = points;
		Pair<Integer, Boolean> idx = theList.indexOf(data);
		//will create a moving average of pts until X min back with no gap being more than Y min ... also update support based
		//on many pts it saw load of ..
		int index = mergeResult.ptIndexBefore;
		if (index < 0)
			index = -1;
		int toDoSmoothingFrom = index;
		
		//first check if mergeable by lowDistThresh
		GpsDataLike neraerPt = mergeResult.refPtIsAfter ? theList.get(index+1) : theList.get(index);
		boolean done = false;
		
		if (neraerPt != null && GpsDataLike.isWithinLowDistThresh(neraerPt.getAvgLonInt()-data.getAvgLonInt(), neraerPt.getAvgLatInt()-data.getAvgLatInt())) {
			done = true;
			neraerPt.helperAddNearPt(data, this);
			if (!Misc.isUndef(movingOpStationId))
				neraerPt.addMovingStation(movingOpStationId);
		}
		else {
			GpsDataLike prior = theList.get(index);
			GpsDataLike next = theList.get(index+1);
			data.setSupport(1);
			data.setAvgSupport(1);
			data.addMovingStation(movingOpStationId);
			theList.add(data);
			helperSetMinMaxOnChange(data);
			GpsDataLike splitPtOfPrior = null;
			toDoSmoothingFrom = index+1;
			if (prior != null && prior.getSupport() == 1) {//should not happen
				prior.setEndGpsRecordTimeIncl(prior.getGpsRecordTime());
			}
			else if (prior != null && prior.getEndGpsRecordTimeIncl() == data.getGpsRecordTime()) {//assume the last pt to be added data
				prior.setSupport(prior.getSupport()-1);
				prior.setEndGpsRecordTimeIncl(data.getGpsRecordTime()-1000);
				if (prior.getSupport() <= 1)
					prior.setEndGpsRecordTimeIncl(prior.getGpsRecordTime());
				data.setSupport(data.getSupport()+1);
				toDoSmoothingFrom = index;
			}
			else if (prior != null && prior.getEndGpsRecordTimeIncl() > data.getGpsRecordTime()) {//see if to split
				//will proportionately ... reduce stuff
				
				int justBefSupp = (int) Math.round( 
						((double)(data.getGpsRecordTime() - prior.getGpsRecordTime()))/
						((double)(prior.getEndGpsRecordTimeIncl() - prior.getGpsRecordTime()))
						*prior.getSupport()
						);
				if (justBefSupp == prior.getSupport())
					justBefSupp--;
				if (justBefSupp <= 0)
					justBefSupp = 1;
				int balSupport = prior.getSupport() - justBefSupp;
				long oldEndGps = prior.getEndGpsRecordTimeIncl();
				
				if (justBefSupp == 1) {
					prior.setEndGpsRecordTimeIncl(prior.getGpsRecordTime());
				}
				else {
					prior.setEndGpsRecordTimeIncl(data.getGpsRecordTime()-1000);
				}
				prior.setSupport(justBefSupp);

				splitPtOfPrior = new GpsDataLike(prior);
				splitPtOfPrior.setGpsRecordTime(data.getGpsRecordTime()+1000);
				splitPtOfPrior.setEndGpsRecordTimeIncl(oldEndGps);
				splitPtOfPrior.setSupport(balSupport);
				if (balSupport == 1)
					splitPtOfPrior.setGpsRecordTime(splitPtOfPrior.getEndGpsRecordTimeIncl());
				theList.add(splitPtOfPrior);
				toDoSmoothingFrom = index;
			}
			//now need to do smoothening
			GpsDataLike.helperDoSmoothingFrom(theList, toDoSmoothingFrom, this);
		}//else if not too close to prior or next
	}
	protected boolean savePoints(Connection conn) throws Exception {//returns true if something saved
		boolean retval = false;
		if (this.ptsChangeTSMinIncl >= 0) {
			//1st delete and then add pts
			PreparedStatement ps = conn.prepareStatement("delete from art_load_opstation_points where art_load_opstation_id=? and gps_record_time >= ? and gps_record_time <= ?");
			ps.setInt(1, this.getId());
			ps.setTimestamp(2, Misc.longToSqlDate(this.ptsChangeTSMinIncl));
			ps.setTimestamp(3, Misc.longToSqlDate(this.ptsChangeTSMaxIncl));
			ps.execute();
			ps = Misc.closePS(ps);
			if (points.size() > 0) {
				ps = conn.prepareStatement("insert ignore into art_load_opstation_points(art_load_opstation_id, gps_record_time, longitude, latitude, dist) values (?,?,?,?,?)");
				int idx = points.indexOf(new GpsDataLike(this.ptsChangeTSMinIncl)).first;
				GpsDataLike ptAtIdx = points.get(idx);
				if (ptAtIdx == null || ptAtIdx.getGpsRecordTime() < ptsChangeTSMinIncl)
					idx++;
				GpsDataLike prev = points.get(idx-1);
				boolean toExec = false;
				for (int i=idx,is = points.size(); i<is;i++) {
					GpsDataLike pt = points.get(i);
					if (pt.getGpsRecordTime() > ptsChangeTSMaxIncl)
						break;
					toExec = true;
					ps.setInt(1, this.getId());
					ps.setTimestamp(2, Misc.longToSqlDate(pt.getGpsRecordTime()));
					double ptLon = (double)pt.getAvgLonInt()/(double)GpsDataLike.doubleToIntMult;
					double ptLat = (double)pt.getAvgLatInt()/(double)GpsDataLike.doubleToIntMult;
					ps.setDouble(3, ptLon);
					ps.setDouble(4, ptLat);
					double distRelPrev = Misc.getUndefDouble();
					if (prev != null) {
						distRelPrev = Point.fastGeoDistance((double)prev.getAvgLonInt()/(double)GpsDataLike.doubleToIntMult, (double)prev.getAvgLatInt()/(double)GpsDataLike.doubleToIntMult, ptLon, ptLat);
					}
					Misc.setParamDouble(ps, distRelPrev,5);
					ps.addBatch();
					if ((i-idx+1) % 10000 > 0) {
						toExec = false;
						ps.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
					}
					prev = pt;
				}
				if (toExec) {
					ps.executeBatch();
					if (!conn.getAutoCommit())
						conn.commit();
				}
				ps = Misc.closePS(ps);
				
			}
			else {
				ps = conn.prepareStatement("delete from art_load_opstations where id=? and not exists(select 1 from art_load_opstation_points where art_load_opstation_id = art_load_opstations.id) ");
				ps.setInt(1, this.getId());
				ps.execute();
				ps = Misc.closePS(ps);
			}
			retval = true;
		}
		resetMinMax();
		return retval;
	}
	
	protected void resetMinMax() {
		this.ptsChangeTSMinIncl = this.ptsChangeTSMaxIncl = -1;
	}
	public void helperSetMinMaxOnChange(GpsDataLike pt) {
		if (pt != null) {
			long ptSt = pt.getGpsRecordTime();
			long ptEn = pt.getEndGpsRecordTimeIncl();
			if (this.ptsChangeTSMinIncl < 0 || this.ptsChangeTSMinIncl > ptSt)
				this.ptsChangeTSMinIncl = ptSt;
			if (this.ptsChangeTSMaxIncl < 0 || this.ptsChangeTSMaxIncl < ptEn)
				this.ptsChangeTSMaxIncl = ptEn;
		}
	}
	public Station(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	

}

package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.cache.NewVehicleData.AddnlPointsOnAdd;
import com.ipssi.common.ds.trip.LUSequence;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.PerfStat;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.geometry.Point;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.GpsDataResultSetReader;
import com.ipssi.processor.utils.Vehicle;
import com.ipssi.reporting.common.util.Common;

public class Dala01Mgmt {
	public static final boolean g_hackConsider1asEnding = false;//DEBUG15r must be false in prod
	FastList<Dala01> dataList = new FastList<Dala01>();
    long minTimeInList = -1;
    long maxTimeInList = -1;
	
    public void resetForNewDalaPt(Connection conn, long tsPrev, NewVehicleData dalaVDT) {
    	
    	if (tsPrev >= this.maxTimeInList){
    		return;
    	}
    	else {
    		Dala01 dummy = new Dala01(tsPrev);
    		Pair<Integer, Boolean> idx = this.dataList.indexOf(dummy);
    		int index = idx.first;	
    		if (index < 0) {
    			this.dataList.clear();
    			this.maxTimeInList = -1;
    			this.minTimeInList = -1;
    		}
    		else {
    			Dala01 entry = this.dataList.get(idx.first);
    			long endOfDala01 = entry.tstart+entry.secEnd*1000;
    			if (endOfDala01 <= tsPrev) {
    				index++;
    				this.maxTimeInList = tsPrev;
    			}
    			else {
    				GpsData ref = new GpsData(entry.tstart);
    				GpsData dalapt = dalaVDT.get(conn,ref);
    				if (dalapt != null && dalapt.getGps_Record_Time() == entry.tstart)
    					dalapt = dalaVDT.get(conn, ref, -1);
    				if (dalapt == null) {
    					index = 0;
    					this.maxTimeInList = -1;
    					this.minTimeInList = -1;
    				}
    				else {
    					this.maxTimeInList = dalapt.getGps_Record_Time();
    				}
    			}
    			for (int i=dataList.size()-1;i>=index;i--)
    				dataList.remove(i);
    		}
    	}
    }
	private void remove(Dala01 data) {
		Pair<Integer, Boolean> idx = dataList.indexOf(data);
		if (idx.second)
			dataList.remove(idx.first);
	}

	public MiscInner.TripleLongLongBoolean getValidBetween(Connection conn, StopDirControl stopDirControl, long tstart, long tsEnd, NewVehicleData dalaVDT, NewVehicleData distVDT, GpsData lastDala) {
	    double distInvalidThreshKM = stopDirControl == null ? 0.05 : stopDirControl.getDalaUpMaxDistKM(); 
	//get(Connection conn, double distInvalidThreshKM, long ts, NewVehicleData dalaVDT, NewVehicleData distVDT)
		long mx = -1;
		long mi = -1;
		
		double threshKM = (stopDirControl == null ? 0.05 : stopDirControl.getDalaUpMaxDistKM());
		boolean isAtEndUp = lastDala == null || lastDala.getValue() < 0.5;//this.dataList.get(this.dataList.size()-1).tstart == dalaEntry.tstart;
		if (g_hackConsider1asEnding) {
			
			if (!isAtEndUp && lastDala != null) {
				GpsData dalaEntryPrev = dalaVDT.get(conn, lastDala, -1);
		
				if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !isAtEndUp)
					isAtEndUp = true;
			}
		}
		
		for (long ts = tsEnd, newts = tsEnd; ts >= tstart; ts=newts-1000) {
			Dala01 dalaEntry = get(conn, distInvalidThreshKM, ts, dalaVDT, distVDT);
			if (dalaEntry == null)
				break;
			boolean isValid = dalaEntry.isValid();
			newts = dalaEntry.tstart;
			long st = newts;
			long en = st + 1000*dalaEntry.secEnd;
			if (lastDala != null && isAtEndUp) {
				GpsData nextAfterEn = dalaVDT.get(conn, new GpsData(en), 1);
				if (nextAfterEn != null && lastDala.getGps_Record_Time() != nextAfterEn.getGps_Record_Time()) {
					isAtEndUp = false;
				}
			}
			if (isAtEndUp && !isValid && en < tsEnd) {
				//in case the ending dalaPt hasnt arrived yet ..
				en = tsEnd;
				GpsData ptAtEnd = distVDT.get(conn, new GpsData(en));
				
				if (ptAtEnd != null) {
					double d = ptAtEnd == null ? dalaEntry.distKM : ptAtEnd.getValue()-dalaEntry.distMarkerAtStart;
					isValid = Dala01.isValid(st, en, d, distInvalidThreshKM);
				}
			}
			if (!isValid)
				continue;
			
			if (en < tstart || st > tsEnd)
				continue;
			if (mi > 0 && mx > 0 && LUSequence.isDalaUpLikeRest((int)((en-st)/1000))) {
				continue; //if both valid and rest like exist then choose the one that is valid
			}
			if (mi <= 0 || mi > st)
				mi = st;
			if (mx <= 0 || mx < en)
				mx = en;
		}
		return new MiscInner.TripleLongLongBoolean(mi, mx, lastDala != null && isAtEndUp && mx >= lastDala.getGps_Record_Time());
	}
	
	public ArrayList<Dala01> getValidBetweenFullList(Connection conn, StopDirControl stopDirControl, long tstart, long tsEnd, NewVehicleData dalaVDT, NewVehicleData distVDT) {
	    double distInvalidThreshKM = stopDirControl == null ? 0.05 : stopDirControl.getDalaUpMaxDistKM(); 
	//get(Connection conn, double distInvalidThreshKM, long ts, NewVehicleData dalaVDT, NewVehicleData distVDT)
		ArrayList<Dala01> retval = null;
		
		double threshKM = (stopDirControl == null ? 0.05 : stopDirControl.getDalaUpMaxDistKM());
		
		for (long ts = tsEnd, newts = tsEnd; ts >= tstart; ts=newts-1000) {
			Dala01 dalaEntry = get(conn, distInvalidThreshKM, ts, dalaVDT, distVDT);
			if (dalaEntry == null)
				break;
			newts = dalaEntry.tstart;
			boolean isValid = dalaEntry.isValid();
			if (!isValid)
				continue;
			if (retval == null)
				retval = new ArrayList<Dala01>();
			retval.add(dalaEntry);
		}
		return retval;
	}
	
	public Dala01 get(Connection conn, double distInvalidThreshKM, long ts, NewVehicleData dalaVDT, NewVehicleData distVDT) {
		long minTime = dalaVDT.getMinTime();
		long maxTime = dalaVDT.getMaxTime();
		if (ts < minTime)
			return null;
		if (ts > maxTime)
			ts = maxTime;
		Dala01 dummy = new Dala01(ts);
		
	int sz = dataList.size();
		long tsAtStart = this.minTimeInList;
		long tsAtEnd = this.maxTimeInList;
		MiscInner.PairLong changeInfo = new MiscInner.PairLong(-1,-1);
		if (tsAtStart <= 0 || ts < tsAtStart || ts > tsAtEnd) {
			//populate
			
			
			if (tsAtStart <= 0) {
				GpsData refGps = new GpsData(ts);
				GpsData dalaEntry = dalaVDT.get(conn, refGps);
				GpsData dalaEntryNext = dalaVDT.get(conn, refGps,1);
				
				boolean dalaEntryUp = dalaEntry != null && dalaEntry.getValue() < 0.5;
				boolean dalaEntryNextUp = dalaEntryNext != null && dalaEntryNext.getValue() < 0.5;
				if (g_hackConsider1asEnding) {
					if (dalaEntryUp && !dalaEntryNextUp)
						dalaEntryNextUp = true;
					GpsData dalaEntryPrev = dalaVDT.get(conn, refGps, -1);
					if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dalaEntryUp)
						dalaEntryUp = true;
				}
				
				if (dalaEntry != null) {
					updateDalaPt(conn, distInvalidThreshKM, dalaEntry.getGps_Record_Time(), dalaEntryUp, dalaVDT, distVDT, changeInfo);
				}
				if (dalaEntryNext != null) {
					updateDalaPt(conn, distInvalidThreshKM, dalaEntryNext.getGps_Record_Time(), dalaEntryNextUp, dalaVDT, distVDT, changeInfo);
				}
			}
			else if (ts < tsAtStart) {
				GpsData refGps = new GpsData(tsAtStart -1);
				for (int i=0;;i--) {
					GpsData dalaEntry = dalaVDT.get(conn, refGps,i);
					if (dalaEntry == null || dalaEntry.getGps_Record_Time() < ts)
						break;
					boolean dalaEntryUp = dalaEntry != null && dalaEntry.getValue() < 0.5;
					if (g_hackConsider1asEnding) {
					
						GpsData dalaEntryPrev = dalaVDT.get(conn, refGps, i-1);
						if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dalaEntryUp)
							dalaEntryUp = true;
					}	
					updateDalaPt(conn, distInvalidThreshKM, dalaEntry.getGps_Record_Time(), dalaEntryUp, dalaVDT, distVDT, changeInfo);
				}
			}
			else {
				GpsData refGps = new GpsData(tsAtEnd);
				for (int i=1;;i++) {
					GpsData dalaEntry = dalaVDT.get(conn, refGps,i);
					if (dalaEntry == null || dalaEntry.getGps_Record_Time() > ts)
						break;
					boolean dalaEntryUp = dalaEntry != null && dalaEntry.getValue() < 0.5;
					if (g_hackConsider1asEnding) {
						
						GpsData dalaEntryPrev = dalaVDT.get(conn, refGps, i-1);
						if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dalaEntryUp)
							dalaEntryUp = true;
					}	

					updateDalaPt(conn, distInvalidThreshKM, dalaEntry.getGps_Record_Time(), dalaEntryUp, dalaVDT, distVDT, changeInfo);
				}
			}
		}
		if (changeInfo.first > 0) {
			int idx1 = (int) dataList.indexOf(new Dala01(changeInfo.first)).first;
			int idx2 = (int) dataList.indexOf(new Dala01(changeInfo.second)).first;
			//adjust idx1 till 20 min before idx1;
			long gapTS = LUSequence.g_distInValidImpactSec*1000;
			for (;idx1 >= 0; idx1--) {
				Dala01 entry = dataList.get(idx1);
				if ((changeInfo.first-entry.tstart) > gapTS)
					break;
			}
			for (int sz2 = dataList.size();idx2 < sz2; idx2++) {
				Dala01 entry = dataList.get(idx2);
				if ((entry.tstart-changeInfo.second) > gapTS)
					break;
			}
			Dala01 base = null;
			for (int i=idx1;i<=idx2;i++) {
				Dala01 entry = dataList.get(idx1);
				if (entry == null)
					continue;
				if (base != null && base.isMeDistInvalid() && (entry.tstart-base.tstart) <= gapTS) {
					entry.setMeDistInvalidBecausePastInvalid(true);
				}
				if (entry.isMeDistInvalid())
					base = entry;
			}
		}
		int idx = (int) dataList.indexOf(dummy).first;
		Dala01 currEntry = dataList.get(dummy);
		removeStale(idx);
		return currEntry;
	}
	public void removeStale(int idx) {
		int sz = this.dataList.size();
		int mxPt = NewVehicleData.g_reg_maxPoints;
		if (sz > mxPt) {
			if (idx < sz/2) {
				//remove from end
				int desiredSize = idx+NewVehicleData.g_reg_maxPoints/3;
				if (desiredSize < sz) {
					for (int i=sz-1;i>=desiredSize;i--)
						this.dataList.remove(i);
					Dala01 end = this.dataList.get(this.dataList.size()-1);
					this.maxTimeInList = end.tstart+end.secEnd*1000;
				}
			}
			else {
				//remove from beg
				int desiredIdx = idx-NewVehicleData.g_reg_maxPoints/3;;
				if (desiredIdx > 0) {
					this.dataList.removeFromStart(desiredIdx);
					this.minTimeInList = this.dataList.get(0).tstart;
				}
			}
		}
	}
	public boolean helperUpdateDalaInfo(Connection conn, double threshKMForInvalid, Dala01 dalaPt, long tstart, long tend, NewVehicleData distVDT) {
		double distMarkerAtStart = dalaPt.distMarkerAtStart;
		boolean oldDistInValid = dalaPt.isMeDistInvalid();//dalaPt.distKM > threshKMForInvalid && !Misc.isEqual(dalaPt.distKM, threshKMForInvalid);
		
		if (tstart <= dalaPt.tstart) {
			GpsData data = distVDT.get(conn, new GpsData(tstart));
			distMarkerAtStart = data == null ? 0 : data.getValue();
			dalaPt.distMarkerAtStart = distMarkerAtStart;
		}
		GpsData endData = distVDT.get(conn, new GpsData(tend));
		double distMarkerAtEnd = endData == null ? dalaPt.distMarkerAtStart : endData.getValue();
		dalaPt.distKM = (distMarkerAtEnd-distMarkerAtStart);
		//boolean newDistInValid = dalaPt.distKM > threshKMForInvalid && !Misc.isEqual(dalaPt.distKM, threshKMForInvalid);
		dalaPt.setMeDistInvalid(threshKMForInvalid);
		return dalaPt.isMeDistInvalid() != oldDistInValid;
	}
	public void updateDalaPt(Connection conn, double distInvalidThreshKM, long ts, boolean amDalaUp, NewVehicleData dalaVDT, NewVehicleData distVDT, MiscInner.PairLong changeInfo) {
		GpsData dummyGps = new GpsData(ts);
		GpsData refDala = dalaVDT.get(conn, dummyGps);
		
		Dala01 dummyDala01 = new Dala01(ts);
		int prevDala01Index = (int) dataList.indexOf(dummyDala01).first;
		Dala01 prevDala01 = dataList.get(prevDala01Index);
		if (this.minTimeInList <= 0 || this.minTimeInList > ts)
			this.minTimeInList = ts;
		if (this.maxTimeInList <= 0 || this.maxTimeInList < ts)
			this.maxTimeInList = ts;
		long prevDalaEndTS = prevDala01 == null ? -1 : prevDala01.tstart+prevDala01.secEnd*1000;
		boolean amInPrevDala =  prevDala01 != null && prevDala01.tstart <= ts && ts <=prevDalaEndTS;
		if ((!amDalaUp && !amInPrevDala) || (amDalaUp && amInPrevDala))
			return;
		long minSeen = -1;
		long maxSeen = -1;
		if (!amDalaUp) {//means I am in prevDala
			//get prev imm DalaUp and next imm DalaUp
			GpsData prevDalaUp = null;
			GpsData nextDalaUp = null;
			for (int i=0;;i--) {
				GpsData data = dalaVDT.get(conn, dummyGps,i);
				if (data == null)
					break;
				minSeen = data.getGps_Record_Time();
				boolean dataDalaUp = data.getValue() < 0.5;
				if (g_hackConsider1asEnding) {
					
					GpsData dalaEntryPrev = dalaVDT.get(conn, dummyGps, i-1);
					if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dataDalaUp)
						dataDalaUp = true;
				}	

				if (dataDalaUp) {
					prevDalaUp = data;
					break;
				}
			}
			for (int i=0;;i++) {
				GpsData data = dalaVDT.get(conn, dummyGps,i);
				if (data == null)
					break;
				
				maxSeen = data.getGps_Record_Time();
				boolean dataDalaUp = data.getValue() < 0.5;//this.dataList.get(this.dataList.size()-1).tstart == dalaEntry.tstart;
				if (g_hackConsider1asEnding) {
					GpsData dalaEntryPrev = dalaVDT.get(conn, dummyGps, i-1);
					if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dataDalaUp)
						dataDalaUp = true;
				}

				if (dataDalaUp) {
					nextDalaUp = data;
					break;
				}
			}
			long prevNewEnding = prevDalaUp == null ? prevDala01.tstart : prevDalaUp.getGps_Record_Time();
			prevDala01.secEnd =(int)( (prevNewEnding-prevDala01.tstart)/1000);
			boolean changePrev = helperUpdateDalaInfo(conn, distInvalidThreshKM, prevDala01, prevDala01.tstart, prevNewEnding, distVDT);
			long nextDalaStart = nextDalaUp == null ? prevDalaEndTS : nextDalaUp.getGps_Record_Time();
			
			Dala01 nextDala01 = new Dala01(nextDalaStart);
			nextDala01.secEnd = (int)((prevDalaEndTS-nextDalaStart)/1000);
			this.dataList.add(nextDala01);
			boolean changeNext = helperUpdateDalaInfo(conn, distInvalidThreshKM, nextDala01, nextDalaStart, prevDalaEndTS, distVDT);
			if (changePrev) {
				helperUpdateTSChange(prevDala01, changeInfo);
			}
			if (changeNext) {
				helperUpdateTSChange(nextDala01, changeInfo);
			}
		}
		else {//i am not in prevDala - the prev may or may not exist
			Dala01 nextDala01 = dataList.get(dummyDala01, 1);
			long nextDalaStartTS = nextDala01 == null ? -1 : nextDala01.tstart;
			long nextDalaEndTS = nextDalaStartTS > 0 ? nextDalaStartTS + nextDala01.secEnd*1000 : -1;
			//	prevDalaEndTS//
			long prevDala = -1;
			long nextDala = -1;
			
			for (int i=0;;i--) {
				GpsData data = dalaVDT.get(conn, dummyGps,i);
				if (data == null)
					break;
				minSeen = data.getGps_Record_Time();
				if (data.getGps_Record_Time() <= prevDalaEndTS) {
					prevDala = prevDalaEndTS;
					break;
				}
				boolean dataDalaUp = data.getValue() < 0.5;
				if (g_hackConsider1asEnding) {
					GpsData dalaEntryPrev = dalaVDT.get(conn, dummyGps, i-1);
					if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dataDalaUp)
						dataDalaUp = true;
				}

				if (dataDalaUp) {
					prevDala = data.getGps_Record_Time();
				}
				else {
					break;
				}
			}
			for (int i=0;;i++) {
				GpsData data = dalaVDT.get(conn, dummyGps,i);
				if (data == null)
					break;
				maxSeen = data.getGps_Record_Time();
				if (nextDalaStartTS > 0 && data.getGps_Record_Time() >= nextDalaStartTS) {
					nextDala = nextDalaStartTS;
					break;
				}
				boolean dataDalaUp = data.getValue() < 0.5;
				if (g_hackConsider1asEnding) {
					GpsData dalaEntryPrev = dalaVDT.get(conn, dummyGps, i-1);
					if (dalaEntryPrev != null && dalaEntryPrev.getValue() < 0.5 && !dataDalaUp)
						dataDalaUp = true;
				}

				if (dataDalaUp) {
					nextDala = data.getGps_Record_Time();
				}
				else {
					break;
				}
			}
			
			if (prevDala <= prevDalaEndTS && prevDala > 0 && prevDalaEndTS > 0) {
				//merge with left
				prevDala01.secEnd = (int)((ts-prevDala01.tstart)/1000);
				boolean change = helperUpdateDalaInfo(conn, distInvalidThreshKM, prevDala01, prevDalaEndTS+1, ts, distVDT);
				if (change) {
					helperUpdateTSChange(prevDala01, changeInfo);
				}
			}
			else if (nextDala >= nextDalaStartTS && nextDalaStartTS > 0) {
				long nextDalEndTS = nextDalaStartTS+1000*nextDala01.secEnd;
				nextDala01.tstart = ts;
				nextDala01.secEnd = ((int)((nextDalEndTS-ts)/1000));
				boolean change = helperUpdateDalaInfo(conn, distInvalidThreshKM, nextDala01, ts, nextDalEndTS-1, distVDT);
				if (change) {
					helperUpdateTSChange(nextDala01, changeInfo);
				}
			}
			else {
				//create new
				Dala01 toAdd = new Dala01(prevDala <= 0 ? ts : prevDala);
				long tsEnd = (nextDala <= 0 ? ts : nextDala);
				toAdd.secEnd = (int)((tsEnd-toAdd.tstart)/1000);
				boolean change = helperUpdateDalaInfo(conn, distInvalidThreshKM, toAdd, toAdd.tstart, tsEnd, distVDT);
				if (change) {
					helperUpdateTSChange(toAdd, changeInfo);
				}
				this.dataList.add(toAdd);
			}
		}
		if (this.minTimeInList > minSeen)
			this.minTimeInList = minSeen;
		if (this.maxTimeInList < minSeen)
			this.maxTimeInList = maxSeen;
	}
	private void helperUpdateTSChange(Dala01 dala01, MiscInner.PairLong changeInfo) {
		if (changeInfo.first <= 0 || dala01.tstart < changeInfo.first)
			changeInfo.first = dala01.tstart;
		if (changeInfo.second <= 0 || dala01.tstart > changeInfo.second)
			changeInfo.second = dala01.tstart;
	}

}

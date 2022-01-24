package com.ipssi.simulHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Triple;
import com.ipssi.simulHelper.SimParams.*;

public class ShovelInfo {
	public static HashMap<Integer, Integer> g_shovelToRefShoveMapping = new HashMap<Integer, Integer>();
	public static class GenInfo {
		public long tsStartLoadCycle;
		public ArrayList<MiscInner.Pair> cycleMarkers;
		public int numShovelCycles;
		public GenInfo(long tsStartLoadCycle, 
				ArrayList<MiscInner.Pair> cycleMarkers, int numShovelCycles) {
			super();
			this.tsStartLoadCycle = tsStartLoadCycle;
			this.cycleMarkers = cycleMarkers;
			this.numShovelCycles = numShovelCycles;
		}
	}
	public synchronized boolean canGenerateCycle(long currTS) {
		return cleanEnd <= currTS; 
	}
	private CacheTrack.VehicleSetup vehsetup = null;
	private double prevNameAtLon = Misc.getUndefDouble();
	private double prevNameAtLat = Misc.getUndefDouble();
	
	public String getGpsName(Connection conn, boolean force) throws Exception {
		if (force || Math.abs(lon-prevNameAtLon) > 0.0005 || Math.abs(lat-prevNameAtLat) > 0.0005) {
			if (vehsetup == null) {
				vehsetup = CacheTrack.VehicleSetup.getSetup(shovelId, conn);
			}
			gpsName = SimGenerateData.calcGpsName(conn, this.shovelId, vehsetup, lon, lat, this.lastLgdAt);
			prevNameAtLon = lon;
			prevNameAtLat = lat;
		}
		return gpsName;
	}
	public  synchronized GenInfo generateCycles(Connection conn, ShovelQItem dumperInfo, long currTS, PreparedStatement psInsertLoad, PreparedStatement psInsertLgd, boolean generateSingleCleanCycle) throws Exception {
		//first = firstCycle, second = cycles (all), third = numLoadCycles
		long mints = 0;
		//first generate a bit of delay to position .. if not positioned
		//next get number of cycles needed ...generate load cycles
		//next check if cleanup needed, then generate cleanup cycles
		//set loadComplete and cleanEnd time etc appropriately
		//finally save data to exc_load_event
		
		long tsFirstCycle = currTS;
		long deltaSec = 0;
		
		if (!generateSingleCleanCycle && dumperInfo.positionCompleted <= 0) {
			int posSec = SimParams.getVal(this.shovelParam.getPositioningTime(),this.shovelParam.getLoPositioningTime(), this.shovelParam.getHiPositioningTime());
			dumperInfo.positionCompleted = dumperInfo.arrivedAt+posSec*1000;
		}
 		if (!generateSingleCleanCycle && dumperInfo.positionCompleted > currTS) {
 			dumperInfo.setPositioningBeforeLoad(true);
			return null;
		}
		
 		deltaSec = !generateSingleCleanCycle && dumperInfo.isPositioningBeforeLoad() ? SimParams.getVal(SimParams.NO_POS_AVG,SimParams.NO_POS_LO,SimParams.NO_POS_HI)
 				: 0;
		tsFirstCycle += deltaSec*1000;
		if (this.lastCycleAt > 0) {
			long gapRelPrevCycle = SimParams.getVal(shovelParam.getCycleTime(), shovelParam.getLoCycleTime(), shovelParam.getHiCycleTime());
			if ((lastCycleAt + 1000*gapRelPrevCycle) >= tsFirstCycle)
				tsFirstCycle = (lastCycleAt + 1000*gapRelPrevCycle);
		}
		int numShovelCycles = !generateSingleCleanCycle ? SimParams.getVal(shovelParam.getNumCycles(), shovelParam.getLoNumCycles(), shovelParam.getHiNumCycles())
		 : 0;
		ArrayList<MiscInner.Pair> cycleMarkers = new ArrayList<MiscInner.Pair> ();
		int prev = 0;
		boolean intermediateCleaningApproach = true;
		if (intermediateCleaningApproach) {
			if (generateSingleCleanCycle) {
				if ( this.numCyclesRemainingForCleaning > 0) {
					cycleMarkers.add(new MiscInner.Pair(0,0));
					this.numCyclesRemainingForCleaning--;
				}
			}
			else {
				if ((this.numTripsCompletedSinceClean) >= this.numTripsAfterCleanNeeded) {
					//generate any balance cleaning ..set up parameter for next rounw
					for (;this.numCyclesRemainingForCleaning > 0;this.numCyclesRemainingForCleaning--) {
						int gap = cycleMarkers.size() == 0 ? 0 : SimParams.getVal(shovelParam.getCycleTime(), shovelParam.getLoCycleTime(), shovelParam.getHiCycleTime());
						prev += gap;
						cycleMarkers.add(new MiscInner.Pair(prev,0));
					}
				
					int cleanUpNeededAfter = SimParams.getVal(shovelParam.getCleanupForTrip(), shovelParam.getCleanupForTrip()-1, shovelParam.getCleanupForTrip()+1);
					int cleanupTime = SimParams.getVal(shovelParam.getCleanupTime(), shovelParam.getLoCleanupTime(), shovelParam.getHiCleanupTime());
					int numCleanCyclesNeeded =(int) Math.round((double)cleanupTime/(double)shovelParam.getCycleTime());
					this.numTripsAfterCleanNeeded = cleanUpNeededAfter;
					this.numCyclesRemainingForCleaning = numCleanCyclesNeeded;
					this.numTripsCompletedSinceClean = 0;
				}
				else {
					this.numTripsCompletedSinceClean++;
				}
			} 
		}
		else {
			int cleanUpNeededAfter = SimParams.getVal(shovelParam.getCleanupForTrip(), shovelParam.getCleanupForTrip()-1, shovelParam.getCleanupForTrip()+1);
			if ((this.numTripsCompletedSinceClean) >= cleanUpNeededAfter) {
				int numSecElapsed = 0;
				int cleanupTime = SimParams.getVal(shovelParam.getCleanupTime(), shovelParam.getLoCleanupTime(), shovelParam.getHiCleanupTime());
				do {
					int gap = cycleMarkers.size() == 0 ? 0 : SimParams.getVal(shovelParam.getCycleTime(), shovelParam.getLoCycleTime(), shovelParam.getHiCycleTime());
					prev += gap;
					cycleMarkers.add(new MiscInner.Pair(prev, 0));
					numSecElapsed += gap;
				} while (numSecElapsed < cleanupTime);
				this.numTripsCompletedSinceClean = 0;
			}
			else {
				this.numTripsCompletedSinceClean++;
			}
		}
		
		for (int i=0;i<numShovelCycles;i++) {
			prev += (cycleMarkers.size() == 0 ? 0 :SimParams.getVal(shovelParam.getCycleTime(), shovelParam.getLoCycleTime(), shovelParam.getHiCycleTime()));
			cycleMarkers.add(new MiscInner.Pair(prev,1));
		}
		
		
		long ts = -1;
//		PreparedStatement psInsertLoad = conn.prepareStatement("insert into exc_load_event(vehicle_id, gps_record_time, quality, dig_prior_sec, stick_in_sec, swing_sec, boom_up, close_dur, updated_on) "+
//				" values (?,?,?,?,?,?,?,?,?)"
//			)
//			;
		boolean isFirstLoadCycle = true;
		for (int i=0,is=cycleMarkers.size();i<is;i++) {
			ts = tsFirstCycle + 1000*cycleMarkers.get(i).first;			
			psInsertLoad.setInt(1, shovelId);
			psInsertLoad.setTimestamp(2, Misc.longToSqlDate(ts));
			psInsertLoad.setInt(3, cycleMarkers.get(i).second == 0 ? 1 : 7);
			psInsertLoad.setInt(4, 10);
			psInsertLoad.setInt(5, 300);
			psInsertLoad.setInt(6, 300);
			psInsertLoad.setInt(7, 300);
			psInsertLoad.setInt(8, 300);
			psInsertLoad.setTimestamp(9, Misc.longToSqlDate(ts));
			psInsertLoad.addBatch();
			if (cycleMarkers.get(i).second != 0 && isFirstLoadCycle && dumperInfo != null) {
				dumperInfo.loadStart = ts;
				isFirstLoadCycle = false;
			}
			if (cycleMarkers.get(i).second != 0  && dumperInfo != null) {
				dumperInfo.loadComplete = ts;
			}
			this.lastCycleAt = ts;
			
		}
		if (!generateSingleCleanCycle && numShovelCycles > 0) {
			int extraIdlingDelay = (int)SimParams.getExtraShovelDelay();
			if (extraIdlingDelay > 0) {
				this.cleanEnd = this.lastCycleAt + extraIdlingDelay*1000;
			}
		}
		return new GenInfo(tsFirstCycle, cycleMarkers, numShovelCycles);
	}
	int shovelId;
	PriorityQueue<ShovelQItem> pendingDumpers = new PriorityQueue<ShovelQItem>();
	SimParams.ShovelParam shovelParam = null;
	volatile int numTripsCompletedSinceClean = 0;
	volatile int numTripsAfterCleanNeeded = 0;
	volatile int numCyclesRemainingForCleaning = 0;
	volatile int extraordinaryDelayAftCleaning = 0;
    volatile long cleanEnd = -1;
	volatile double lon;
	volatile double lat;
	volatile long lastLgdAt = -1;
	volatile double gpsAttribVal = 0;
	String gpsName = null;
	volatile long lastCycleAt = -1;
	
	public int getShovelId() {
		return shovelId;
	}
	public void setShovelId(int shovelId) {
		this.shovelId = shovelId;
	}
	public  synchronized Queue<ShovelQItem> getPendingDumpers() {
		return pendingDumpers;
	}
	
	public  synchronized SimParams.ShovelParam getShovelParam() {
		return shovelParam;
	}
	public  synchronized void setShovelParam(SimParams.ShovelParam shovelParam) {
		this.shovelParam = shovelParam;
	}
	public ShovelInfo(int shovelId, ShovelParam shovelParam) {
		super();
		this.shovelId = shovelId;
		this.shovelParam = shovelParam;
	}
	public  synchronized int getNumTripsCompletedSinceClean() {
		return numTripsCompletedSinceClean;
	}
	public  synchronized void setNumTripsCompletedSinceClean(int numTripsCompletedSinceClean) {
		this.numTripsCompletedSinceClean = numTripsCompletedSinceClean;
	}
	public  synchronized long getCleanEnd() {
		return cleanEnd;
	}
	public  synchronized void setCleanEnd(long cleanEnd) {
		this.cleanEnd = cleanEnd;
	}
	public  synchronized double getLon() {
		return lon;
	}
	public  synchronized void setLon(double lon) {
		this.lon = lon;
	}
	public  synchronized double getLat() {
		return lat;
	}
	public  synchronized void setLat(double lat) {
		this.lat = lat;
	}
	
	public static class ShovelQItem implements Comparable<ShovelQItem> {
	     private int dumperId;
	     private long arrivedAt;
	     private long loadStart = -1;
	     private long loadComplete = -1;
	     private long moveStarted = -1;
	     private long positionCompleted =-1;
	     private boolean positioningBeforeLoad = false;
	     public String toString() {
	    	 StringBuilder sb =  new StringBuilder();
	    	 SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
	    	 sb.append("Dumper:").append(dumperId)
	    	 .append(",A:").append(sdf.format(Misc.longToUtilDate(arrivedAt)))
	    	 .append(",S:").append(loadStart <= 0 ? "null":sdf.format(Misc.longToUtilDate(loadStart)))
	    	 .append(",C:").append(loadComplete <= 0 ? "null":sdf.format(Misc.longToUtilDate(loadComplete)))
	    	 .append(",M:").append(moveStarted <= 0 ? "null":sdf.format(Misc.longToUtilDate(moveStarted)))
	    	 ;
	    	 return sb.toString();
	     }
		public int compareTo(ShovelQItem arg0) {
			// TODO Auto-generated method stub
			long gap = this.arrivedAt - arg0.arrivedAt;
			if (gap == 0)
				gap = this.dumperId -  arg0.dumperId;
			return gap < 0 ? -1 : gap == 0 ? 0 : 1;
		}
		public int getDumperId() {
			return dumperId;
		}
		public void setDumperId(int dumperId) {
			this.dumperId = dumperId;
		}
		public long getArrivedAt() {
			return arrivedAt;
		}
		public void setArrivedAt(long arrivedAt) {
			this.arrivedAt = arrivedAt;
		}
		public long getLoadStart() {
			return loadStart;
		}
		public void setLoadStart(long loadStart) {
			this.loadStart = loadStart;
		}
		public long getLoadComplete() {
			return loadComplete;
		}
		public void setLoadComplete(long loadComplete) {
			this.loadComplete = loadComplete;
		}
		public long getMoveStarted() {
			return moveStarted;
		}
		public void setMoveStarted(long moveStarted) {
			this.moveStarted = moveStarted;
		}
		public ShovelQItem(int dumperId, long arrivedAt) {
			super();
			this.dumperId = dumperId;
			this.arrivedAt = arrivedAt;
		}
		public long getPositionCompleted() {
			return positionCompleted;
		}
		public void setPositionCompleted(long positionCompleted) {
			this.positionCompleted = positionCompleted;
		}
		public boolean isPositioningBeforeLoad() {
			return positioningBeforeLoad;
		}
		public void setPositioningBeforeLoad(boolean positioningBeforeLoad) {
			this.positioningBeforeLoad = positioningBeforeLoad;
		}
		
	     
	}

	public  synchronized long getLastLgdAt() {
		return lastLgdAt;
	}
	public  synchronized void setLastLgdAt(long lastLgdAt) {
		this.lastLgdAt = lastLgdAt;
	}
	public  synchronized long getLastCycleAt() {
		return lastCycleAt;
	}
	public  synchronized void setLastCycleAt(long lastCycleAt) {
		this.lastCycleAt = lastCycleAt;
	}
	public  synchronized void setPendingDumpers(PriorityQueue<ShovelQItem> pendingDumpers) {
		this.pendingDumpers = pendingDumpers;
	}
	public  synchronized double getGpsAttribVal() {
		return gpsAttribVal;
	}
	public  synchronized void setGpsAttribVal(double gpsAttribVal) {
		this.gpsAttribVal = gpsAttribVal;
	}
	public String getGpsName() {
		return gpsName;
	}
	public void setGpsName(String gpsName) {
		this.gpsName = gpsName;
	}
	public static void main(String a[]) throws Exception {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		
		long ts = System.currentTimeMillis()/1000*1000;
		SimGenerateData simgen = new SimGenerateData();
		simgen.generateAndGetTS(conn, ts);
		simgen.generateAndGetTS(conn, ts+1000);
	}
}

package com.ipssi.mining;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.LUInfoExtract;

public class BaseStat {
	private int processed = 0;
	private int qLen = 0;
	private int totWaitSec = 0;
	private int totLoadSec = 0;
	private int waitIncidentCount = 0;
	private int loadIncidentCount = 0;
	private int windowSec = 0;
	
	private int latestLoadSec = 0;
	private int waitOfLastProccessedSec = 0;
	private long lastProcessedTime = Misc.getUndefInt();

	public int getRateOfLoading() {
		return windowSec <= 0 ? Misc.getUndefInt() : Math.round((processed*3600)/windowSec); 
	}
	public int getQLength() {
		return qLen;
	}
	public int getAvgWaitSec() {
		return waitIncidentCount == 0 ? Misc.getUndefInt() : Math.round((totWaitSec)/waitIncidentCount);
	}
	public int getAvgLoadSec() {
		return loadIncidentCount == 0 ? Misc.getUndefInt() : Math.round((totLoadSec)/loadIncidentCount);
	}
	public int getLatestLoadSec() {
		return lastProcessedTime > 0 ? this.latestLoadSec : Misc.getUndefInt();
	}
	public int getLatestWaitSec() {
		return lastProcessedTime > 0 ? this.waitOfLastProccessedSec : Misc.getUndefInt();
	}
	public long getLastProcessedTime() {
		return lastProcessedTime;
	}
	public void reset() {
		processed = 0;
		qLen = 0;
		totWaitSec = 0;
		totLoadSec = 0;
		waitIncidentCount = 0;
		loadIncidentCount = 0;
		windowSec = 0;
		
		latestLoadSec = 0;
		waitOfLastProccessedSec = 0;
		lastProcessedTime = Misc.getUndefInt();
	}
	
	public void updateStats(ArrayList<RecentVehicleInfo> vehList, long desiredMi, boolean useLoad, ManagementUnit mu) {
		RecentVehicleInfo prevGout = null;
		for (int j=vehList == null ? -1 : vehList.size()-1; j >= 0; j--) {
			RecentVehicleInfo entry = vehList.get(j);
			LUInfoExtract ext = useLoad ? entry.getLoad() : entry.getUnload();
			if (ext == null)
				continue;
			if (ext.getGateIn() < desiredMi) {
				break;
			}
			prevGout = updateStats(entry, prevGout, desiredMi, useLoad, mu);
		}//for each veh
	}

	public RecentVehicleInfo updateStats(RecentVehicleInfo entry, RecentVehicleInfo prevGout, long desiredMi, boolean useLoad, ManagementUnit mu) {
		//return the entry if it has Gout or is presumed to be Gout
		LUInfoExtract prevExt = prevGout == null ? null : (useLoad ? prevGout.getLoad() : prevGout.getUnload());
		boolean seenGout = prevExt != null;
		LUInfoExtract ext = useLoad ? entry.getLoad() : entry.getUnload();
		if (ext == null)
			return prevGout;
		if (ext.getGateIn() < desiredMi)
			return prevGout;
		
		long endWO = ext.getWaitOut();
		long endGO = ext.getGateOut();
		if (endWO <= 0 && endGO > 0)
			endWO = endGO;
		long startWI = ext.getWaitIn();
		long startGI = ext.getGateIn();
		boolean presumedProcessed = endGO > 0;
		if (!presumedProcessed) {
			//check if current time of the device is more than prev gate in ... in which case dont treat as processed
			GpsData currLastPoint = mu.getLastPoint(entry.getVehicleId());
			long currTS = currLastPoint == null ? Misc.getUndefInt() : currLastPoint.getGps_Record_Time();
			if (prevExt != null) {
				if (currTS < prevExt.getGateIn()) { //give benefit of doubt and assume to be processed
					presumedProcessed = true;
					endWO = endGO = prevExt.getGateIn();
				}
				else {
					endWO = endGO = currTS;
					presumedProcessed = false;
				}
			}
		}
		if (presumedProcessed) {
			processed++;
		}
		else {
			qLen++;
		}
		if (ext.getGateOut() > 0) {
			prevGout = entry;
		}
		int loadSec = (int)((endGO - ext.getGateIn())/1000);
		if (loadSec < CurrentStation.defaultLoadSec)
			loadSec = CurrentStation.defaultLoadSec;
		if (presumedProcessed) {
			totLoadSec += loadSec;
			loadIncidentCount++;
		}
		int waitSec = (int)((endWO - ext.getWaitIn())/1000);
		if (waitSec < loadSec)
			waitSec = loadSec;
		totWaitSec += waitSec;
		waitIncidentCount++;
		if (presumedProcessed && (lastProcessedTime < 0 || lastProcessedTime < endGO)) {
			latestLoadSec = loadSec;
			lastProcessedTime = endGO;
			waitOfLastProccessedSec = waitSec;
		}
		return prevGout;
	}

	public void setWindowSec(int windowSec) {
		this.windowSec = windowSec;
	}
	
	public static Pair<Long, Long> getMinMaxTS(Connection conn,ArrayList<RecentVehicleInfo> vehList, Pair<Long, Long> prev, boolean useLoad, ManagementUnit mu) throws Exception {
		long mx = prev == null ? 0 : prev.second;
		long mi = prev == null ? (long) Misc.LARGE_NUMBER : prev.first;
		for (RecentVehicleInfo entry:vehList) {
			int vehicleId = entry.getVehicleId();
			GpsData lastPoint = mu.getLastPoint(vehicleId);
			if (lastPoint != null && lastPoint.getGps_Record_Time() > mx)
				mx = lastPoint.getGps_Record_Time();
			LUInfoExtract ext = useLoad ? entry.getLoad() : entry.getUnload();
			if (ext != null && ext.getGateIn() < mi)
				mi = ext.getGateIn();
				
		}
		if (prev == null)
			prev = new Pair<Long, Long> (mi,mx);
		else {
			prev.first =mi;
			prev.second = mx;
		}
		return prev;
	}
}

package com.ipssi.mining;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.LUInfoExtract;

public class RecentVehicleInfo {
	private int vehicleId;
	private LUInfoExtract load = null;
	private LUInfoExtract unload = null;
	private double leadDist = Misc.getUndefDouble();//gin to gin
	private double returnLeadDist = Misc.getUndefDouble();//gin to gin
	private int leadSec = Misc.getUndefInt();//wout to win
	private int returnLeadSec = Misc.getUndefInt();//wout to win
	private long confirmedTime = Misc.getUndefInt();
	public LUInfoExtract getFirstLU() {
		return load != null ? load : unload;
	}
	
	public static int getLatestEntryForVehicle(int vehicleId, ArrayList<RecentVehicleInfo> recentVehicleList) {
		for (int i=recentVehicleList.size()-1; i>=0 ;i--) {
			RecentVehicleInfo curr = recentVehicleList.get(i);
			if (curr.vehicleId == vehicleId)
				return i;
		}
		return -1;
	}
	public static boolean removeLatestEntryForVehicle(int vehicleId, ArrayList<RecentVehicleInfo> recentVehicleList) {
		int idx = getLatestEntryForVehicle(vehicleId, recentVehicleList);
		if (idx >= 0)
			recentVehicleList.remove(idx);
		return idx >= 0;
	}
	private static void removeOlderEntry(ArrayList<RecentVehicleInfo> recentVehicleList, int before, int vehicleId) {
		for (int i=before-1;i>=0;i--) {
			if (recentVehicleList.get(i).vehicleId == vehicleId)
				recentVehicleList.remove(i);
		}
	}
	private static void removeNewerEntry(ArrayList<RecentVehicleInfo> recentVehicleList, int after, int vehicleId) {
		for (int i=recentVehicleList.size()-1;i>after;i--) {
			if (recentVehicleList.get(i).vehicleId == vehicleId)
				recentVehicleList.remove(i);
		}
	}
	public static RecentVehicleInfo addRecentVehicle(RecentVehicleInfo toAdd, ArrayList<RecentVehicleInfo> recentVehicleList) {
		LUInfoExtract relevantToAddExt = toAdd.getFirstLU();
		long ts = relevantToAddExt == null ? Misc.getUndefInt() : relevantToAddExt.getGateIn();
		for (int i=0,is=recentVehicleList.size(); i<is;i++) {
			RecentVehicleInfo curr = recentVehicleList.get(i);
			LUInfoExtract relevantExt = curr.getFirstLU();
			long currTS = relevantExt == null ? Misc.getUndefInt() : relevantExt.getGateIn();
			if (curr.vehicleId == toAdd.vehicleId) {
				if (currTS == ts) {
					return curr;
				}
			}
			if (currTS > ts) {
				recentVehicleList.add(i, toAdd);
				removeOlderEntry(recentVehicleList, i, toAdd.vehicleId);
				removeNewerEntry(recentVehicleList, i, toAdd.vehicleId);
				return toAdd;
			}
		}
		recentVehicleList.add(toAdd);
		removeOlderEntry(recentVehicleList, recentVehicleList.size()-1, toAdd.vehicleId);
		return toAdd;
	}

	public void updateLeadEtc(Connection conn, LUInfoExtract newExt, boolean isLoad, GpsData ginGpsData, NewVehicleData vdp) {
		if (isLoad) {//TODO
			confirmedTime = newExt.getGateIn();
		}
		if (newExt == null) {
			this.leadDist = Misc.getUndefDouble();
			this.returnLeadDist = Misc.getUndefDouble();
			this.leadSec = Misc.getUndefInt();
			this.returnLeadSec = Misc.getUndefInt();
			this.unload = null;
			this.confirmedTime = Misc.getUndefInt();
			return;
		}
		long meGin = newExt.getGateIn();
		long meWin = newExt.getWaitIn();
		long prevWout = unload == null ? load.getWaitOut() : unload.getWaitOut();
		long prevGin = unload == null ? load.getGateIn() : unload.getGateIn();
		GpsData meGinData = ginGpsData == null ? vdp.getSinglePoint(conn, new GpsData(meGin)) : ginGpsData;
		GpsData prevGinData = vdp.getSinglePoint(conn, new GpsData(prevGin));
		double dist = meGinData.getValue() - prevGinData.getValue();
		int sec = (int)((meWin-prevWout)/1000);
		if (unload != null) {
			this.returnLeadDist = dist;
			this.returnLeadSec = sec;
		}
		else {
			this.leadDist = dist/2;
			this.returnLeadDist = dist/2;
			this.leadSec = sec/2;
			this.returnLeadSec = sec/2;
		}
	}
	
	public RecentVehicleInfo(int vehicleId, LUInfoExtract load, LUInfoExtract unload) {
		this.vehicleId = vehicleId;
		this.load = load;
		this.unload = unload;
	}

	
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public LUInfoExtract getLoad() {
		return load;
	}
	public void setLoad(LUInfoExtract load) {
		this.load = load;
	}
	public LUInfoExtract getUnload() {
		return unload;
	}
	public void setUnload(LUInfoExtract unload) {
		this.unload = unload;
	}
	public double getLeadDist() {
		return leadDist;
	}
	public void setLeadDist(double leadDist) {
		this.leadDist = leadDist;
	}
	public double getReturnLeadDist() {
		return returnLeadDist;
	}
	public void setReturnLeadDist(double returnLeadDist) {
		this.returnLeadDist = returnLeadDist;
	}
	public int getLeadSec() {
		return leadSec;
	}
	public void setLeadSec(int leadSec) {
		this.leadSec = leadSec;
	}
	public int getReturnLeadSec() {
		return returnLeadSec;
	}
	public void setReturnLeadSec(int returnLeadSec) {
		this.returnLeadSec = returnLeadSec;
	}
	public long getConfirmedTime() {
		return confirmedTime;
	}
	public void setConfirmed(long confirmedTime) {
		this.confirmedTime = confirmedTime;
	}
}

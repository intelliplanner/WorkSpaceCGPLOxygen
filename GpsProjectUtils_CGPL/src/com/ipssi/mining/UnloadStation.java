package com.ipssi.mining;

import java.util.ArrayList;

public class UnloadStation extends BaseStat {
	
	private int opStationId;
	private boolean dirty = true;
	public ArrayList<RecentVehicleInfo> recentVehicleList = new ArrayList<RecentVehicleInfo>();
	
	public void removeRecentVehicleInfo(int vehicleId) {
		RecentVehicleInfo.removeLatestEntryForVehicle(vehicleId, recentVehicleList);
		this.dirty = true;
	}
	public void addRecentVehicleInfo(RecentVehicleInfo recentInfo) {
		RecentVehicleInfo.addRecentVehicle(recentInfo, recentVehicleList);
		this.dirty = true;
	}
	public UnloadStation(int opStationId) {
		super();
		this.opStationId = opStationId;
	}
	public int getOpStationId() {
		return opStationId;
	}
	public void setOpStationId(int opStationId) {
		this.opStationId = opStationId;
	}
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	} 
	
}

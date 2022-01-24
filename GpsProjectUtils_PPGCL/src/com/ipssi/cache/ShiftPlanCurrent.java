package com.ipssi.cache;

import java.util.ArrayList;

public class ShiftPlanCurrent {
	private int shiftTargetId;
	private int routeId;
	private int loadSiteId;
	private int unloadSiteId;
	private int numShovels;
	private long timestamp;
	private ArrayList<Integer> shovelIdList;
	
	public ShiftPlanCurrent () {
		routeId = 0;
		loadSiteId = 0;
		unloadSiteId = 0;
		numShovels = 0;
		timestamp = 0;
		shovelIdList = new ArrayList<Integer> ();
	}
	
	public void setShiftTargetId(int shiftTargetId) {
		this.shiftTargetId = shiftTargetId;
	}

	public int getShiftTargetId() {
		return shiftTargetId;
	}
	
	public int getRouteId() {
		return routeId;
	}

	public int getLoadSiteId() {
		return loadSiteId;
	}

	public int getUnloadSiteId() {
		return unloadSiteId;
	}

	public int getNumShovels() {
		return numShovels;
	}

	public ArrayList<Integer> getShovelIdList() {
		return shovelIdList;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}

	public void setLoadSiteId(int loadSiteId) {
		this.loadSiteId = loadSiteId;
	}

	public void setUnloadSiteId(int unloadSiteId) {
		this.unloadSiteId = unloadSiteId;
	}

	public void setNumShovels(int numShovels) {
		this.numShovels = numShovels;
	}

	public void setShovelIdList(ArrayList<Integer> shovelIdList) {
		this.shovelIdList = shovelIdList;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public void addToShovelIdList(int shovelId) {
		if (shovelIdList.contains(shovelId) == false){
			shovelIdList.add(shovelId);
			numShovels++;			
		}
	}
}
package com.ipssi.cache;

import java.util.ArrayList;

public class ShiftPlanHistory {
	private int shiftTargetId;
	private ArrayList<Integer> loadSiteIdList;
	private long timestamp;	
	
	public ShiftPlanHistory () {
		shiftTargetId = 0;
		timestamp = 0;
		loadSiteIdList = new ArrayList<Integer> ();
	}
	
	public int getShiftTargetId() {
		return shiftTargetId;
	}

	public ArrayList<Integer> getLoadSiteIdList() {
		return loadSiteIdList;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setShiftTargetId(int shiftTargetId) {
		this.shiftTargetId = shiftTargetId;
	}

	/*public void setLoadSiteIdList(ArrayList<Integer> loadSiteIdList) {
		this.loadSiteIdList = loadSiteIdList;
	}
	*/
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void addToLoadSiteIdList(int loadSiteId) {		
		if (loadSiteIdList.contains(loadSiteId) == false){
			loadSiteIdList.add(loadSiteId);
		}
	}
}

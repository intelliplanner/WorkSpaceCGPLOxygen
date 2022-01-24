package com.ipssi.dispatchoptimization.vo;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class ShovelInfoVo extends CoreVehicleInfoVo {
	private double cycleTimeSec = Misc.getUndefDouble(); // Dim 76100 = Dim 82535
	
	//76102   may not need, I think shovel name will be used. please confirm with Tanuj Sir
	private int assignedLoadSite = Misc.getUndefInt();	
	//76103  may not need, I think shovel name will be used. please confirm with Tanuj Sir
	private int estimatedLoadSite = Misc.getUndefInt();
	//76104		couldn't understand
	private int isOnDigSite = Misc.getUndefInt();	
	//76105		setup based loaded initially by TripOptimizer
	private int allowRedeploy = Misc.getUndefInt();
	//76106		loaded by cachedValue
	private String currPos="";	
	private ArrayList<Integer> notAllowedOtherCatTypes = new ArrayList<Integer>();

	private ArrayList<Integer> assignedDumperIds = new ArrayList<Integer>();
	
	/*
	private int currWait = 0;//76107
	private int qlenWhenMeLeavesAndComesBack = 0;//76108
	//CURRENT PARAMS
	 id="82531" name="tonnagePerHour" 
	id="82532" name="Avg cleaning percentage" 
	id="82533" name="Avg Idle Per" 
	id="82534" name="avgDumperWaitTime" 
	id="82535" name="avgCycleTime" 
	id="82536" name="AavgNumOfCyclePerTrip" 
	id="83128" name="avgLoadingTime" 
	id="83129" name="avgUnloadingTime" 
	id="83130" name="avgLoadTripTime"
//	 SHIFT PARAMS
	id="83131" name="avgUnloadTripTime" 
	id="83111" name="tonnageDispatched"
	id="83112" name="Avg cleaning percentage"
	id="83113" name="Avg Idle Per" 
	id="83114" name="avgDumperWaitTime"
	id="83115" name="avgCycleTime"
	id="83116" name="avgCyclesPerTrip"
	id="83132" name="avgUnloadTripTime" 
	id="83137" name="numTrips"

	private double totTonnesInShift;//76109

	private long latestDispAt;//76111
	private boolean normEvent = false;//76112
	private boolean critEvent = false;//76113
	private boolean notWorking = false;//76114
	Stopped Since //20255
	ign on //20266
	private long latestDataAt = -1;//20173
	private String latestPosAt = null;//20167
*/	
/*
	this.cycleTimeSec = OPDashHelper.getDimValue(conn, shovel.getId(), 82535).getIntVal(); // Dim 76100 = Dim 82535
	this.assignedLoadSite = OPDashHelper.getDimValue(conn, shovel.getId(), 76102).getIntVal();
	this.estimatedLoadSite = OPDashHelper.getDimValue(conn, shovel.getId(), 76103).getIntVal();
	this.isOnDigSite = OPDashHelper.getDimValue(conn, shovel.getId(), 76104).getIntVal();
	this.allowRedeploy =OPDashHelper.getDimValue(conn, shovel.getId(), 76105).getIntVal();
	this.currPos=OPDashHelper.getDimValue(conn, shovel.getId(), 76106).getIntVal();
	*/
	public ArrayList<Integer> getAssignedDumperIds() {
		return assignedDumperIds;
	}
	public void addAssignedDumperId(DumperInfoVo assignedDumper) {
		this.assignedDumperIds.add(assignedDumper.getId());
	}
	public void delAssignedDumperId(DumperInfoVo assignedDumper) {
		int index = this.assignedDumperIds.indexOf(assignedDumper.getId());
		if (index >= 0) {
			this.assignedDumperIds.remove(index);
		}
	}
	public void setAssignedDumperIds(ArrayList<Integer> assignedDumperIds) {
		this.assignedDumperIds = assignedDumperIds;
	}
	public int getIsOnDigSite() {
		return isOnDigSite;
	}
	public void setIsOnDigSite(int isOnDigSite) {
		this.isOnDigSite = isOnDigSite;
	}
	public int getAllowRedeploy() {
		return allowRedeploy;
	}
	public void setAllowRedeploy(int allowRedeploy) {
		this.allowRedeploy = allowRedeploy;
	}
	public String getCurrPos() {
		return currPos;
	}
	public void setCurrPos(String currPos) {
		this.currPos = currPos;
	}
	
	public double getCycleTimeSec() {
		return cycleTimeSec;
	}
	public void setCycleTimeSec(double cycleTimeSec) {
		this.cycleTimeSec = cycleTimeSec;
	}
	
	public int getAssignedLoadSite() {
		return assignedLoadSite;
	}
	public void setAssignedLoadSite(int assignedLoadSite) {
		this.assignedLoadSite = assignedLoadSite;
	}
	public int getEstimatedLoadSite() {
		return estimatedLoadSite;
	}
	public void setEstimatedLoadSite(int estimatedLoadSite) {
		this.estimatedLoadSite = estimatedLoadSite;
	}
	public ArrayList<Integer> getNotAllowedOtherCatTypes() {
		return notAllowedOtherCatTypes;
	}
	public void setNotAllowedOtherCatTypes(
			ArrayList<Integer> notAllowedOtherCatTypes) {
		this.notAllowedOtherCatTypes = notAllowedOtherCatTypes;
	}
	
	
}

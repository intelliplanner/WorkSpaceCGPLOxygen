package com.ipssi.dispatchoptimization.vo;

import java.sql.Connection;

import com.ipssi.gen.utils.Misc;

public class DumperInfoVo extends CoreVehicleInfoVo {
	public final static int L_WAIT = 0;
	public final static int L_BEING_OP = 1;
	public final static int L_WAIT_AFT = 2;
	public final static int L_ENROUTE = 3;
	public final static int U_WAIT = 4;
	public final static int U_BEING_OP = 5;
	public final static int U_WAIT_AFT = 6;
	public final static int U_ENROUTE = 7;
//	1 Load, 0- Unload, 2 wait unload,3 wait at load
	
/*	public final static int L_WAIT = 3;
	public final static int LOAD = 1;
	public final static int UNLOAD = 0;
	public final static int U_WAIT = 2;*/
	
//	public final static int NORMAL_OP = 0;
//	public final static int DIVERTED_TO_LU = 1;
//	public final static int DIVERTED_TO_REST = 2;
//	public final static int IN_BD = 3;
//	public final static int IN_REST = 4;
//	public final static int SHOULD_BE_AT_TARGET = 5;
//	public final static int OVERSHOT_TARGET = 6;

	private double assignedPlusUsageRatePerKM = Misc.getUndefDouble(); //Dim 76130
	private double avgOpSpeedPerKM = Misc.getUndefDouble(); //mapped to cycle_time_sec param
	private double avgUnloadTimeSec = 90; //double_field1;
	
	private int assignedRoute = Misc.getUndefInt();
	private int estimatedRoute = Misc.getUndefInt();
	private int currentLoadStatus = Misc.getUndefInt();	
	private int currentAddnlOpStatus = Misc.getUndefInt();
	double percentageLegCompleted = Misc.getUndefDouble();//76120
	private double distTravelledSinceLastOp;
	
	private double distMarkerAtLastOp;
	private double distMarkerAtPrevToLastOp;
	private double totDistToTargetOp;
	private double estDistToTargetOp;
	
	private long lastLUEventTime;
	
	private int numberTripsSinceReset;
	private double loadKMSinceReset;
	private double distMarkerAtReset;
	
	public long latestGRT;
	
	private int optimizeSrcSiteId = -1;
	private int optimizeDestSiteId = -1;
	private int optimizeForDumperId = -1;
	private boolean optimizeRecommended = false;
	private long dynOptimizerRunAt = -1;
	private long lastPredictionRunAt = -1;
	private double prevTripLeadAvg; //currently as previous
	private double prevLoadLon = Misc.getUndefDouble(); //not yet populated
	private double prevLoadLat = Misc.getUndefDouble(); //not yet populated
	
	
	public double getPercentLegCompleted(){
		return percentageLegCompleted;
	}
	public String getTripStatusString() {
		String retval = "N/A";
		if (this.isInRest()) {
			return "Going to/In Rest";
		}
		
//		1 Load, 0- Unload, 2 wait unload,3 wait at load
/*		switch (currentLoadStatus) {
			case L_WAIT : return "Wait For Load";
			case LOAD : return "Loaded";
			case UNLOAD : return "UnLoaded";
			case U_WAIT : return "Wait For Unload";
		}*/
		
		switch (currentLoadStatus) {
		case L_WAIT : return "Wait For Load";
		case L_BEING_OP : return "Being Loaded";
		case L_WAIT_AFT : return "Waitng After Load";
		case L_ENROUTE : return "Going to Unload";
		case U_WAIT : return "Wait For Unload";
		case U_BEING_OP : return "Being Unloaded";
		case U_WAIT_AFT : return "Waitng After Unload";
		case U_ENROUTE : return "Going to Load";
	
	}
		return "N/A";
	}
	
	
	public double getAssignedPlusUsageRatePerKM() {
		return assignedPlusUsageRatePerKM;
	}
	public void setAssignedPlusUsageRatePerKM(double assignedPlusUsageRatePerKM) {
		this.assignedPlusUsageRatePerKM = assignedPlusUsageRatePerKM;
	}
	
	public double getAvgOpSpeedPerKM() {
		return avgOpSpeedPerKM;
	}
	public void setAvgOpSpeedPerKM(double avgOpSpeedPerKM) {
		this.avgOpSpeedPerKM = avgOpSpeedPerKM;
	}
	public double getAvgUnloadTimeSec() {
		return avgUnloadTimeSec;
	}
	public void setAvgUnloadTimeSec(double avgUnloadTimeSec) {
		this.avgUnloadTimeSec = avgUnloadTimeSec;
	}
	public int getAssignedRoute() {
		return assignedRoute;
	}
	public void setAssignedRoute(int assignedRoute) {
		this.assignedRoute = assignedRoute;
	}
	public int getEstimatedRoute() {
		return estimatedRoute;
	}
	public void setEstimatedRoute(int estimatedRoute) {
		this.estimatedRoute = estimatedRoute;
	}
	public int getCurrentLoadStatus() {
		return currentLoadStatus;
	}
	public void setCurrentLoadStatus(int currentLoadStatus) {
		this.currentLoadStatus = currentLoadStatus;
	}
	public int getCurrentAddnlOpStatus() {
		return currentAddnlOpStatus;
	}
	public void setCurrentAddnlOpStatus(int currentAddnlOpStatus) {
		this.currentAddnlOpStatus = currentAddnlOpStatus;
	}
	public double getDistTravelledSinceLastOp() {
		return distTravelledSinceLastOp;
	}
	public void setDistTravelledSinceLastOp(double distTravelledSinceLastOp) {
		this.distTravelledSinceLastOp = distTravelledSinceLastOp;
	}
	public double getDistMarkerAtLastOp() {
		return distMarkerAtLastOp;
	}
	public void setDistMarkerAtLastOp(double distMarkerAtLastOp) {
		this.distMarkerAtLastOp = distMarkerAtLastOp;
	}
	public double getDistMarkerAtPrevToLastOp() {
		return distMarkerAtPrevToLastOp;
	}
	public void setDistMarkerAtPrevToLastOp(double distMarkerAtPrevToLastOp) {
		this.distMarkerAtPrevToLastOp = distMarkerAtPrevToLastOp;
	}
	public double getTotDistToTargetOp() {
		return totDistToTargetOp;
	}
	public void setTotDistToTargetOp(double totDistToTargetOp) {
		this.totDistToTargetOp = totDistToTargetOp;
	}
	public double getEstDistToTargetOp() {
		return estDistToTargetOp;
	}
	public void setEstDistToTargetOp(double estDistToTargetOp) {
		this.estDistToTargetOp = estDistToTargetOp;
	}
	public long getLastLUEventTime() {
		return lastLUEventTime;
	}
	public void setLastLUEventTime(long lastLUEventTime) {
		this.lastLUEventTime = lastLUEventTime;
	}
	public int getNumberTripsSinceReset() {
		return numberTripsSinceReset;
	}
	public void setNumberTripsSinceReset(int numberTripsSinceReset) {
		this.numberTripsSinceReset = numberTripsSinceReset;
	}
	public double getLoadKMSinceReset() {
		return loadKMSinceReset;
	}
	public void setLoadKMSinceReset(double loadKMSinceReset) {
		this.loadKMSinceReset = loadKMSinceReset;
	}
	public double getDistMarkerAtReset() {
		return distMarkerAtReset;
	}
	public void setDistMarkerAtReset(double distMarkerAtReset) {
		this.distMarkerAtReset = distMarkerAtReset;
	}
	public long getLatestGRT() {
		return latestGRT;
	}
	public void setLatestGRT(long latestGRT) {
		this.latestGRT = latestGRT;
	}
	public int getOptimizeSrcSiteId() {
		return optimizeSrcSiteId;
	}
	public void setOptimizeSrcSiteId(int optimizeSrcSiteId) {
		this.optimizeSrcSiteId = optimizeSrcSiteId;
	}
	public int getOptimizeDestSiteId() {
		return optimizeDestSiteId;
	}
	public void setOptimizeDestSiteId(int optimizeDestSiteId) {
		this.optimizeDestSiteId = optimizeDestSiteId;
	}
	public int getOptimizeForDumperId() {
		return optimizeForDumperId;
	}
	public void setOptimizeForDumperId(int optimizeForDumperId) {
		this.optimizeForDumperId = optimizeForDumperId;
	}
	public boolean isOptimizeRecommended() {
		return optimizeRecommended;
	}
	public void setOptimizeRecommended(boolean optimizeRecommended) {
		this.optimizeRecommended = optimizeRecommended;
	}
	public long getDynOptimizerRunAt() {
		return dynOptimizerRunAt;
	}
	public void setDynOptimizerRunAt(long dynOptimizerRunAt) {
		this.dynOptimizerRunAt = dynOptimizerRunAt;
	}
	public long getLastPredictionRunAt() {
		return lastPredictionRunAt;
	}
	public void setLastPredictionRunAt(long lastPredictionRunAt) {
		this.lastPredictionRunAt = lastPredictionRunAt;
	}
	public double getPrevTripLeadAvg() {
		return prevTripLeadAvg;
	}
	public void setPrevTripLeadAvg(double prevTripLeadAvg) {
		this.prevTripLeadAvg = prevTripLeadAvg;
	}
	public double getPrevLoadLon() {
		return prevLoadLon;
	}
	public void setPrevLoadLon(double prevLoadLon) {
		this.prevLoadLon = prevLoadLon;
	}
	public double getPrevLoadLat() {
		return prevLoadLat;
	}
	public void setPrevLoadLat(double prevLoadLat) {
		this.prevLoadLat = prevLoadLat;
	}
	
	private ShovelInfoVo shovel=null;
	
	
	public ShovelInfoVo getShovel() {
		return shovel;
	}
	public void setShovel(ShovelInfoVo shovel) {
		this.shovel = shovel;
	}
	public String getHoverText(Connection conn, int fromPerspectiveOf) {//0 - when getting name of route ignore src, 1= ignore dest, 2 = full Src/Site
		StringBuilder sb = new StringBuilder();
		String tripStatStr = this.getTripStatusString();
		sb.append(getName()).append(" Trip Status:").append(tripStatStr);
		sb.append("<br/>");
//		NewMU newmu = this.getOwnerMU();
		RouteVo route = OperatorDashboardMU.getRoute(this.getAssignedRoute());
		//RouteVo estRoute = OperatorDashboardMU.getRoute(this.getEstimatedRoute());
		if (route != null) {
			sb.append("Assigned:").append(this.shovel.getName());
		}
		if (this.getAssignedRoute() != this.getEstimatedRoute()) {
			sb.append("Est Route:").append(this.shovel.getName());
		}
		return sb.toString();
	}
	
}

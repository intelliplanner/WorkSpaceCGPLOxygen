package com.ipssi.miningOpt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


import com.ipssi.RegionTest.RegionTest;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;

public class LoadSite extends Site {
	private ArrayList<Integer> assignedShovels = new ArrayList<Integer>();
	private int blendedCycleTimeSec = Misc.getUndefInt();
	private double blendedCapVol = Misc.getUndefDouble();
	private double blendedCapWt = Misc.getUndefDouble();
	private int predictedQLenWhenLatestDumperOutComesBack = 0;
	private int optimizeSrcSiteId = -1;
	private int optimizeDestSiteId = -1;
	private int optimizeForDumperId = -1;
	private boolean optimizeRecommended = false;
	private long dynOptimizerRunAt = -1;
	private long lastPredictionRunAt = -1;
	public void toString(StringBuilder sb, boolean doAllProp) {
		super.toString(sb, doAllProp);
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			this.getReadLock();
			toString(sb, true);
			Helper.putDBGProp(sb, "assigned_shovels", assignedShovels);
			
		}
		catch (Exception e) {
			
		}
		finally {
			this.releaseReadLock();
		}
		return sb.toString();
	}

	public ArrayList<Integer> getAssignedShovels() {
		return assignedShovels;
	}
	
	public boolean noDumpersAssigned() {
		try {
			this.getReadLock();
			return this.getAssignedDumpers().size() == 0;
		}
		catch (Exception e) {
			
		}
		finally {
			this.releaseReadLock();
		}
		return false;
	}
	public void setAssignedShovels(ArrayList<Integer> assignedShovels) {
		this.assignedShovels = assignedShovels;
	}
	
	
	public LoadSite(int id, NewMU ownerMU) {
		super(id, ownerMU);
	}
	
	public void addIfNotExist(int val) {
		try {
			getWriteLock();
			for (int i=0,is=this.assignedShovels == null ? 0 : this.assignedShovels.size(); i<is; i++)
				if (val == assignedShovels.get(i).intValue())
					return;
			assignedShovels.add(val);
		}
		catch (Exception e2) {
			
		}
		finally {
			releaseWriteLock();
		}
	}
	
	public boolean removeVal(int val) {
		try {
			getWriteLock();
		
			for (int i=0,is=this.assignedShovels == null ? 0 : assignedShovels.size(); i<is; i++)
				if (val == assignedShovels.get(i).intValue()) {
					assignedShovels.remove(i);
					return true;
				}
			return false;
		}
		catch (Exception e) {
			
		}
		finally {
			releaseWriteLock();
		}
		return false;
	}
	public double getBlendedCapVol() {
		calcBlendedAvg();
		return blendedCapVol;
	}
	public void setBlendedCapVol(double blendedCapVol) {
		this.blendedCapVol = blendedCapVol;
	}
	public double getBlendedCapWt() {
		calcBlendedAvg();
		return blendedCapWt;
	}
	public void setBlendedCapWt(double blendedCapWt) {
		
		this.blendedCapWt = blendedCapWt;
	}
	public void setBlendedCycleTimeSec(int blendedCycleTimeSec) {
		this.blendedCycleTimeSec = blendedCycleTimeSec;
	}
	public void calcBlendedAvg() {
		if (!Misc.isUndef(this.blendedCapVol) && Misc.isUndef(this.blendedCapWt) && Misc.isUndef(this.blendedCycleTimeSec))
			return;
		try {
			getReadLock();
			double max = Double.MIN_VALUE;
			int validShovel = 0;
			double totCapVol = 0;
			double totCapWt = 0;
			for (int i=0,is=this.assignedShovels == null ? 0 : this.assignedShovels.size(); i<is; i++) {
				ShovelInfo shovelInfo = (ShovelInfo) this.getOwnerMU().getVehicleInfo(assignedShovels.get(i));
				if (shovelInfo == null)
					continue;
				if (max < shovelInfo.getCycleTimeSec())
					max = shovelInfo.getCycleTimeSec();
				totCapVol += shovelInfo.getCapacityVol();
				totCapWt += shovelInfo.getCapacityWt();
				validShovel++;
			}
			double totCycle = max*validShovel;
			double totCount = 0;
			for (int i=0,is=this.assignedShovels == null ? 0 : this.assignedShovels.size(); i<is; i++) {
				ShovelInfo shovelInfo = (ShovelInfo) this.getOwnerMU().getVehicleInfo(assignedShovels.get(i));
				if (shovelInfo == null)
					continue;
				
				double factor = max/shovelInfo.getCycleTimeSec();
				totCount += factor;
			}
			this.blendedCycleTimeSec = (int)(totCycle/totCount);
			this.blendedCapWt = totCapWt;
			this.blendedCapVol = totCapVol;
		}
		catch (Exception e2) {
			
		}
		finally {
			releaseReadLock();
		}
		
	}
	public int getBlendedCycleTimeSec() {
		calcBlendedAvg();
		return blendedCycleTimeSec;
	}
	public int getPredictedQLenWhenLatestDumperOutComesBack() {
		return predictedQLenWhenLatestDumperOutComesBack;
	}
	public void setPredictedQLenWhenLatestDumperOutComesBack(
			int predictedQLenWhenLatestDumperOutComesBack) {
		this.predictedQLenWhenLatestDumperOutComesBack = predictedQLenWhenLatestDumperOutComesBack;
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
	public void runPredictionIfNeeded() {
		runPredictionIfNeeded(-1, false);
	}
	public void runPredictionIfNeeded(long ts, boolean must) {
		ts = ts < 0 ? System.currentTimeMillis() : ts;
		if (must || this.lastPredictionRunAt <= 0 || (ts-this.lastPredictionRunAt) > 3*60*1000) {
			Predictor predictor = new Predictor(this);
			predictor.bringSiteToTargetTS(ts, true);
			predictor.writeBackPrediction();
		}
		
	}
}

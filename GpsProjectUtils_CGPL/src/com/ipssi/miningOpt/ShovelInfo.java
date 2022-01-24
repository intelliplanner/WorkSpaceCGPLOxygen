package com.ipssi.miningOpt;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class ShovelInfo extends CoreVehicleInfo {
	private double cycleTimeSec = Misc.getUndefDouble();
	private double avgFuelConsumptionRate = Misc.getUndefDouble();
	
	private int assignedLoadSite = Misc.getUndefInt();	
	private int estimatedLoadSite = Misc.getUndefInt();
	private ArrayList<Integer> notAllowedOtherCatTypes = new ArrayList<Integer>();
	
	public void toString(StringBuilder sb, boolean doAll) {
		if (doAll) {
			Helper.putDBGProp(sb, "cycle_time", cycleTimeSec);
			Helper.putDBGProp(sb, "assigned_site", assignedLoadSite);
			Helper.putDBGProp(sb, "est_site", estimatedLoadSite);
		}
		super.toString(sb, doAll);
		if (doAll) {
			Helper.putDBGProp(sb, "avg_fuel_hr", avgFuelConsumptionRate);
			Helper.putDBGProp(sb, "not_allowed_other_cat", notAllowedOtherCatTypes);
		}
	}
	public boolean isNotAllowedDumperType(int v) {
		return Helper.isInList(notAllowedOtherCatTypes, v);
	}
	public ShovelInfo(int id, NewMU ownerMU) {
		super(id, ownerMU);
	}
	public void populateInfo(ResultSet rs) throws Exception {
		super.populateInfo(rs);
		this.avgFuelConsumptionRate = Misc.getRsetDouble(rs, "fuel_consumption_rate_hourly");
		this.cycleTimeSec = Misc.getRsetDouble(rs, "cycle_time_second");
	}
	public double getCycleTimeSec() {
		return cycleTimeSec;
	}
	public void setCycleTimeSec(double cycleTimeSec) {
		this.cycleTimeSec = cycleTimeSec;
	}
	public int getEstimatedLoadSite() {
		return estimatedLoadSite;
	}
	public void setEstimatedLoadSite(int estimatedLoadSite) {
		this.estimatedLoadSite = estimatedLoadSite;
	}
	public double getAvgFuelConsumptionRate() {
		return avgFuelConsumptionRate;
	}
	public void setAvgFuelConsumptionRate(double avgFuelConsumptionRate) {
		this.avgFuelConsumptionRate = avgFuelConsumptionRate;
	}
	public int getAssignedLoadSite() {
		return assignedLoadSite;
	}
	public void setAssignedLoadSite(int assignedLoadSite) {
		this.assignedLoadSite = assignedLoadSite;
	}
	public ArrayList<Integer> getNotAllowedOtherCatTypes() {
		return notAllowedOtherCatTypes;
	}
	public void setNotAllowedOtherCatTypes(
			ArrayList<Integer> notAllowedOtherCatTypes) {
		this.notAllowedOtherCatTypes = notAllowedOtherCatTypes;
	}
	
}

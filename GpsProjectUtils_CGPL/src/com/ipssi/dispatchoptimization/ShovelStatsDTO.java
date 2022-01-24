package com.ipssi.dispatchoptimization;

public class ShovelStatsDTO {
	private double tonnagePerHour;//last 10 min trips
	private double avgCycleTime;//
	private double avgNumOfCyclePerTrip;
	private double avgDumperWaitTime;
	private double avgShovelIdlePercentage;
	private double avgCleaningPercentage;
	private double extraField1;
	private double extraField2;
	private double extraField3;
	public double getTonnagePerHour() {
		return tonnagePerHour;
	}
	public void setTonnagePerHour(double tonnagePerHour) {
		this.tonnagePerHour = tonnagePerHour;
	}
	public double getAvgCycleTime() {
		return avgCycleTime;
	}
	public void setAvgCycleTime(double avgCycleTime) {
		this.avgCycleTime = avgCycleTime;
	}
	public double getAvgNumOfCyclePerTrip() {
		return avgNumOfCyclePerTrip;
	}
	public void setAvgNumOfCyclePerTrip(double avgNumOfCyclePerTrip) {
		this.avgNumOfCyclePerTrip = avgNumOfCyclePerTrip;
	}
	public double getAvgDumperWaitTime() {
		return avgDumperWaitTime;
	}
	public void setAvgDumperWaitTime(double avgDumperWaitTime) {
		this.avgDumperWaitTime = avgDumperWaitTime;
	}
	public double getAvgShovelIdlePercentage() {
		return avgShovelIdlePercentage;
	}
	public void setAvgShovelIdlePercentage(double avgShovelIdlePercentage) {
		this.avgShovelIdlePercentage = avgShovelIdlePercentage;
	}
	public double getAvgCleaningPercentage() {
		return avgCleaningPercentage;
	}
	public void setAvgCleaningPercentage(double avgCleaningPercentage) {
		this.avgCleaningPercentage = avgCleaningPercentage;
	}
	public double getExtraField1() {
		return extraField1;
	}
	public void setExtraField1(double extraField1) {
		this.extraField1 = extraField1;
	}
	public double getExtraField2() {
		return extraField2;
	}
	public void setExtraField2(double extraField2) {
		this.extraField2 = extraField2;
	}
	public double getExtraField3() {
		return extraField3;
	}
	public void setExtraField3(double extraField3) {
		this.extraField3 = extraField3;
	}

	

}

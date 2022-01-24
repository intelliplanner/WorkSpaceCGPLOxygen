package com.ipssi.dispatchoptimization;

import java.util.ArrayList;

public class PitStatsDTO {
	private int numOfShovels;
	private int numOfDumpers;
	private double avgTonnageDispatched;
	private double avgWaitTimeForDumpers;
	private double avgIdleTimeOfShovel;
	private double avgCycleTime;
	private double avgCyclePerTrip;
	private double AvgLead;
	private double extraField1;
	private double extraField2;
	private double extraField3;
	private ArrayList<Integer> listOfShovels;
	private ArrayList<Integer> listOfDumpers;
	public double getNumOfShovels() {
		return numOfShovels;
	}
	public void setNumOfShovels(int numOfShovels) {
		this.numOfShovels = numOfShovels;
	}
	public double getNumOfDumpers() {
		return numOfDumpers;
	}
	public void setNumOfDumpers(int numOfDumpers) {
		this.numOfDumpers = numOfDumpers;
	}
	public double getAvgTonnageDispatched() {
		return avgTonnageDispatched;
	}
	public void setAvgTonnageDispatched(double avgTonnageDispatched) {
		this.avgTonnageDispatched = avgTonnageDispatched;
	}
	public double getAvgWaitTimeForDumpers() {
		return avgWaitTimeForDumpers;
	}
	public void setAvgWaitTimeForDumpers(double avgWaitTimeForDumpers) {
		this.avgWaitTimeForDumpers = avgWaitTimeForDumpers;
	}
	public double getAvgIdleTimeOfShovel() {
		return avgIdleTimeOfShovel;
	}
	public void setAvgIdleTimeOfShovel(double avgIdleTimeOfShovel) {
		this.avgIdleTimeOfShovel = avgIdleTimeOfShovel;
	}
	public double getAvgCycleTime() {
		return avgCycleTime;
	}
	public void setAvgCycleTime(double avgCycleTime) {
		this.avgCycleTime = avgCycleTime;
	}
	public double getAvgCyclePerTrip() {
		return avgCyclePerTrip;
	}
	public void setAvgCyclePerTrip(double avgCyclePerTrip) {
		this.avgCyclePerTrip = avgCyclePerTrip;
	}
	public double getAvgLead() {
		return AvgLead;
	}
	public void setAvgLead(double avgLead) {
		AvgLead = avgLead;
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
	public ArrayList<Integer> getListOfShovels() {
		return listOfShovels;
	}
	public void setListOfShovels(ArrayList<Integer> listOfShovels) {
		this.listOfShovels = listOfShovels;
	}
	public ArrayList<Integer> getListOfDumpers() {
		return listOfDumpers;
	}
	public void setListOfDumpers(ArrayList<Integer> listOfDumpers) {
		this.listOfDumpers = listOfDumpers;
	}
	
	
	
}

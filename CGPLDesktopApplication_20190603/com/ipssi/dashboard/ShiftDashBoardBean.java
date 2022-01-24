package com.ipssi.dashboard;

import java.sql.Date;

public class ShiftDashBoardBean {
	private int shiftId;
	private int shiftInfoId;
	private Date shiftDate;
	private int noOfVehiclesAssignedPlanned;
	private int noOfVehiclesAssignedNow;
	private int noOfTripsTargeted;
	private int noOfTripsNow;
	private double tripsPerHourCumm;
	private double tripsPerHourCurr;
	private double avgRoundTripCumm;
	private double avgRoundTripCurr;
	private double avgRatioTargeted;
	private double avgRatioCumm;
	private double avgRatioCurr;
	private int regionId;
	private String regionName;
	private int opStationId;
	private String opStationName;
	public int getOpStationId() {
		return opStationId;
	}
	public void setOpStationId(int opStationId) {
		this.opStationId = opStationId;
	}
	public String getOpStationName() {
		return opStationName;
	}
	public void setOpStationName(String opStationName) {
		this.opStationName = opStationName;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public int getShiftId() {
		return shiftId;
	}
	public void setShiftId(int shiftId) {
		this.shiftId = shiftId;
	}
	public int getShiftInfoId() {
		return shiftInfoId;
	}
	public void setShiftInfoId(int shiftInfoId) {
		this.shiftInfoId = shiftInfoId;
	}
	public Date getShiftDate() {
		return shiftDate;
	}
	public void setShiftDate(Date shiftDate) {
		this.shiftDate = shiftDate;
	}
	public int getNoOfVehiclesAssignedPlanned() {
		return noOfVehiclesAssignedPlanned;
	}
	public void setNoOfVehiclesAssignedPlanned(int noOfVehiclesAssignedPlanned) {
		this.noOfVehiclesAssignedPlanned = noOfVehiclesAssignedPlanned;
	}
	public int getNoOfVehiclesAssignedNow() {
		return noOfVehiclesAssignedNow;
	}
	public void setNoOfVehiclesAssignedNow(int noOfVehiclesAssignedNow) {
		this.noOfVehiclesAssignedNow = noOfVehiclesAssignedNow;
	}
	public int getNoOfTripsTargeted() {
		return noOfTripsTargeted;
	}
	public void setNoOfTripsTargeted(int noOfTripsTargeted) {
		this.noOfTripsTargeted = noOfTripsTargeted;
	}
	public int getNoOfTripsNow() {
		return noOfTripsNow;
	}
	public void setNoOfTripsNow(int noOfTripsNow) {
		this.noOfTripsNow = noOfTripsNow;
	}
	public double getTripsPerHourCumm() {
		return tripsPerHourCumm;
	}
	public void setTripsPerHourCumm(double tripsPerHourCumm) {
		this.tripsPerHourCumm = tripsPerHourCumm;
	}
	public double getTripsPerHourCurr() {
		return tripsPerHourCurr;
	}
	public void setTripsPerHourCurr(double tripsPerHourCurr) {
		this.tripsPerHourCurr = tripsPerHourCurr;
	}
	public double getAvgRoundTripCumm() {
		return avgRoundTripCumm;
	}
	public void setAvgRoundTripCumm(double avgRoundTripCumm) {
		this.avgRoundTripCumm = avgRoundTripCumm;
	}
	public double getAvgRoundTripCurr() {
		return avgRoundTripCurr;
	}
	public void setAvgRoundTripCurr(double avgRoundTripCurr) {
		this.avgRoundTripCurr = avgRoundTripCurr;
	}
	public double getAvgRatioTargeted() {
		return avgRatioTargeted;
	}
	public void setAvgRatioTargeted(double avgRatioTargeted) {
		this.avgRatioTargeted = avgRatioTargeted;
	}
	public double getAvgRatioCumm() {
		return avgRatioCumm;
	}
	public void setAvgRatioCumm(double avgRatioCumm) {
		this.avgRatioCumm = avgRatioCumm;
	}
	public double getAvgRatioCurr() {
		return avgRatioCurr;
	}
	public void setAvgRatioCurr(double avgRatioCurr) {
		this.avgRatioCurr = avgRatioCurr;
	}
	
}

package com.ipssi.dashboard;

import java.sql.Date;

public class EngineEventBean {
	private int engineEventId;
	private int vehicleId;
	private int vehicleType;
	private int contractor;
	private Date tripDate;
	private int rule;
	private Date startTime;
	private String startLocation;
	private Date stopTime;
	private String stopLocation;
	private double duration;
	public int getEngineEventId() {
		return engineEventId;
	}
	public void setEngineEventId(int engineEventId) {
		this.engineEventId = engineEventId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}
	public int getContractor() {
		return contractor;
	}
	public void setContractor(int contractor) {
		this.contractor = contractor;
	}
	public Date getTripDate() {
		return tripDate;
	}
	public void setTripDate(Date tripDate) {
		this.tripDate = tripDate;
	}
	public int getRule() {
		return rule;
	}
	public void setRule(int rule) {
		this.rule = rule;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public String getStartLocation() {
		return startLocation;
	}
	public void setStartLocation(String startLocation) {
		this.startLocation = startLocation;
	}
	public Date getStopTime() {
		return stopTime;
	}
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	public String getStopLocation() {
		return stopLocation;
	}
	public void setStopLocation(String stopLocation) {
		this.stopLocation = stopLocation;
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
}

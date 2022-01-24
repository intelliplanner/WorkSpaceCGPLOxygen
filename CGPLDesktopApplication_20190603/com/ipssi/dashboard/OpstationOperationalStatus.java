package com.ipssi.dashboard;

import java.sql.Timestamp;

public class OpstationOperationalStatus {
	private int opStationId ;
	private int status = -1;
	private Timestamp startTime;
	private Timestamp endTime;
	private Timestamp updatedOn;
	private int reason;
	private String opName;
	
	
	
	
	public int getOpStationId() {
		return opStationId;
	}
	public void setOpStationId(int opStationId) {
		this.opStationId = opStationId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	public Timestamp getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}
	public void setReason(int reason) {
		this.reason = reason;
	}
	public int getReason() {
		return reason;
	}
	public void setOpName(String opName) {
		this.opName = opName;
	}
	public String getOpName() {
		return opName;
	}
	
	
}

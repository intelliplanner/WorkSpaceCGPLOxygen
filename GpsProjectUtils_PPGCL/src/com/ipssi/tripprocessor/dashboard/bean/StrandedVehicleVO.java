package com.ipssi.tripprocessor.dashboard.bean;

import java.util.Date;

public class StrandedVehicleVO extends VOInterface{
	private static final long serialVersionUID = 1L;
	private int opStationId;
	private String opStationName;
	private int vehicleId;
	private String vehicleName;
	private Date atTime;
	private String phone;
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
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public Date getAtTime() {
		return atTime;
	}
	public void setAtTime(Date atTime) {
		this.atTime = atTime;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}

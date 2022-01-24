package com.ipssi.tripprocessor.dashboard.bean;


public class VehicleOutsideGateVO extends VOInterface{
	private static final long serialVersionUID = 1L;
	private int vehicleId;
	private String vehicleName;
	private String type;
	private String phone;
	private int vehicleTypeId;
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
	public int getVehicleTypeId() {
		return vehicleTypeId;
	}
	public void setVehicleTypeId(int vehicleTypeId) {
		this.vehicleTypeId = vehicleTypeId;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}

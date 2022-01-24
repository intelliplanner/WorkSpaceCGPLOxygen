package com.ipssi.tripprocessor.dashboard.bean;

import java.util.Date;

import com.ipssi.tripcommon.LUInfoExtract;

public class VehicleVO  extends VOInterface{
	private static final long serialVersionUID = 1L;
	private int opStationId;
	private String opStationName;
	private int vehicleId;
	private String vehicleName;
	private Date lastProcessedTime;
	private LUInfoExtract luInfoExtract;
	private String vehicleStatus;
	private Date currentGpsRecordTime;
	private String currentLocation;
	private int ownerOrgId;
	private int vehicleType;
	
	
	public String getVehicleStatus() {
		return vehicleStatus;
	}
	public void setVehicleStatus(String vehicleStatus) {
		this.vehicleStatus = vehicleStatus;
	}
	public Date getCurrentGpsRecordTime() {
		return currentGpsRecordTime;
	}
	public void setCurrentGpsRecordTime(Date currentGpsRecordTime) {
		this.currentGpsRecordTime = currentGpsRecordTime;
	}
	public String getCurrentLocation() {
		return currentLocation;
	}
	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}
	public LUInfoExtract getLuInfoExtract() {
		return luInfoExtract;
	}
	public void setLuInfoExtract(LUInfoExtract luInfoExtract) {
		this.luInfoExtract = luInfoExtract;
	}
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
	public Date getLastProcessedTime() {
		return lastProcessedTime;
	}
	public void setLastProcessedTime(Date lastProcessedTime) {
		this.lastProcessedTime = lastProcessedTime;
	}
	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}
	public int getVehicleType() {
		return vehicleType;
	}
	
}

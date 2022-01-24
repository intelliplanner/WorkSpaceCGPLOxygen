package com.ipssi.dashboard;

import java.util.Date;

import com.ipssi.tripprocessor.dashboard.bean.VOInterface;

public class DetentionVO extends VOInterface {
	private int opStationId;
	private int vehicleId;
	private String opStationName;
	private String vehicleName;
	private String contractor;
	private String detentionTime;
	private Date GateIn;
	private Date GateOut;
	private Date gpsRecordTime;
	private String location;
	private String vehicleTypeString;
	private int vehicleType;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public int getOpStationId() {
		return opStationId;
	}
	public void setOpStationId(int opStationId) {
		this.opStationId = opStationId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getOpStationName() {
		return opStationName;
	}
	public void setOpStationName(String opStationName) {
		this.opStationName = opStationName;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getContractor() {
		return contractor;
	}
	public void setContractor(String contractor) {
		this.contractor = contractor;
	}
	public String getDetentionTime() {
		return detentionTime;
	}
	public void setDetentionTime(String detentionTime) {
		this.detentionTime = detentionTime;
	}
	public Date getGateIn() {
		return GateIn;
	}
	public void setGateIn(Date gateIn) {
		GateIn = gateIn;
	}
	public Date getGateOut() {
		return GateOut;
	}
	public void setGateOut(Date gateOut) {
		GateOut = gateOut;
	}
	public Date getGpsRecordTime() {
		return gpsRecordTime;
	}
	public void setGpsRecordTime(Date gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	public void setVehicleTypeString(String vehicleType) {
		this.vehicleTypeString = vehicleType;
	}
	public String getVehicleTypeString() {
		return vehicleTypeString;
	}
	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}
	public int getVehicleType() {
		return vehicleType;
	}
	
}

/**
 * 
 */
package com.ipssi.tracker.map.vehicle;

import java.sql.Timestamp;

/**
 * @author jai
 *
 */
public class VehicleBean {
	private String vehicleName;
	private int vehicleId;
	private double speed;
	private double latitude;
	private double longitude;
	private Timestamp gpsRecordTime ;
	private int timeFactor;
	private int speedFactor;
	private double orientation;
	private String location;
	private String contractor;
	private int vehicleType;
	private int ignition;
	private long stoppedSince;
	private int loadStatus;
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public void setGpsRecordTime(Timestamp gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	public Timestamp getGpsRecordTime() {
		return gpsRecordTime;
	}
	public void setTimeFactor(int timeFactor) {
		this.timeFactor = timeFactor;
	}
	public int getTimeFactor() {
		return timeFactor;
	}
	public void setSpeedFactor(int speedFactor) {
		this.speedFactor = speedFactor;
	}
	public int getSpeedFactor() {
		return speedFactor;
	}
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	public double getOrientation() {
		return orientation;
	}
	public void setContractor(String contractor) {
		this.contractor = contractor;
	}
	public String getContractor() {
		return contractor;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLocation() {
		return location;
	}
	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}
	public int getVehicleType() {
		return vehicleType;
	}
	public void setIgnition(int ignition) {
		this.ignition = ignition;
	}
	public int getIgnition() {
		return ignition;
	}
	public void setStoppedSince(long stoppedSince){
		this.stoppedSince = stoppedSince;
	}
	public long getStoppedSince(){
		return stoppedSince;
	}
	public void setLoadStatus(int loadStatus){
		this.loadStatus = loadStatus;
	}
	public int getLoadStatus(){
		return loadStatus;
	}
}

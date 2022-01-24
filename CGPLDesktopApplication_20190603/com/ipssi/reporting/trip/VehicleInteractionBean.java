package com.ipssi.reporting.trip;

import java.util.Date;

public class VehicleInteractionBean {
	private int vehicleId;
	private int causeId;
	private int userId;
	private Date updatedOn;
	private String operator;
	private String vehicleName;
	private String notes;
	private Date nextFollowTime;
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
	private double latitude;
	private double longitude;
	public int getVehicleId(){
		return vehicleId;
	}
	public void setVehicleId(int vehicleId){
		this.vehicleId = vehicleId;
	}
	public int getCauseId(){
		return causeId;
	}
	public void setCauseId(int causeId){
		this.causeId = causeId;
	}
	public int getUserId(){
		return userId;
	}
	public void setUserId(int userId){
		this.userId = userId;
	}
	public void setUpdatedOn(Date updatedOn){
		this.updatedOn = updatedOn;
	}
	public Date getUpdatedOn(){
		return updatedOn;
	}
	public String getVehicleName(){
		return vehicleName;
	}
	public void setVehicleName(String vehicleName){
		this.vehicleName = vehicleName;
	}
	public String getOperator(){
		return operator;
	}
	public void setOperator(String operator){
		this.operator = operator;
	}
	public String getNotes(){
		return notes;
	}
	public void setNotes(String notes){
		this.notes = notes;
	}
	public Date getNextFollowTime() {
		return nextFollowTime;
	}
	public void setNextFollowTime(Date nextFollowTime) {
		this.nextFollowTime = nextFollowTime;
	}
}

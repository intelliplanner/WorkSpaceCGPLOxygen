package com.ipssi.tripprocessor.dashboard.bean;

import java.util.Date;

public class RegionAlertVO {
	private static final long serialVersionUID = 1L;
	private int opStationId;
	private String opStationName;
	private int ruleId;
	private int notificationSetId;
	private int type;
	private Date validFromDate;
	private Date validToDate;
	private int customerContactId;
	private Date contactTimeFromTime;
	private Date contactTimeToTime;
	private String address;
	private String contactName;
	private String mobile;
	private String email;
	private int threshold;
	private int regValue;
	private Date lastAlertSendAt;
	private Date lastQLAlertSendAt;
	private Date lastSVAlertSendAt;
	private Date lastPTAlertSendAt;
	private Date lastNOAlertSendAt;
	private Date lastQLEmailAlertSendAt;
	private Date lastSVEmailAlertSendAt;
	private Date lastPTEmailAlertSendAt;
	private Date lastNOEmailAlertSendAt;
	private int noOfVehiclesQueued;
	private int noOfVehiclesStranded;
	private int processingTime;
	private int vehicleId;
	private String vehicleName;
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public String toString() {
		return "opStationId:"+opStationId+"ruleId:"+ruleId+"threshold:"+threshold+"lastAlertSendAt:"+lastAlertSendAt+"mobile:"+mobile+"email:"+email+"type:"+type+"regValue:"+regValue;
}
	public int getRegValue() {
		return regValue;
	}
	public void setRegValue(int regValue) {
		this.regValue = regValue;
	}
	public Date getLastAlertSendAt() {
		return lastAlertSendAt;
	}
	public void setLastAlertSendAt(Date lastAlertSendAt) {
		this.lastAlertSendAt = lastAlertSendAt;
	}
	public Date getLastQLEmailAlertSendAt() {
		return lastQLEmailAlertSendAt;
	}
	public void setLastQLEmailAlertSendAt(Date lastQLEmailAlertSendAt) {
		this.lastQLEmailAlertSendAt = lastQLEmailAlertSendAt;
	}
	public Date getLastSVEmailAlertSendAt() {
		return lastSVEmailAlertSendAt;
	}
	public void setLastSVEmailAlertSendAt(Date lastSVEmailAlertSendAt) {
		this.lastSVEmailAlertSendAt = lastSVEmailAlertSendAt;
	}
	public Date getLastPTEmailAlertSendAt() {
		return lastPTEmailAlertSendAt;
	}
	public void setLastPTEmailAlertSendAt(Date lastPTEmailAlertSendAt) {
		this.lastPTEmailAlertSendAt = lastPTEmailAlertSendAt;
	}
	public Date getLastNOEmailAlertSendAt() {
		return lastNOEmailAlertSendAt;
	}
	public void setLastNOEmailAlertSendAt(Date lastNOEmailAlertSendAt) {
		this.lastNOEmailAlertSendAt = lastNOEmailAlertSendAt;
	}
	public int getProcessingTime() {
		return processingTime;
	}
	public void setProcessingTime(int processingTime) {
		this.processingTime = processingTime;
	}
	public int getNoOfVehiclesQueued() {
		return noOfVehiclesQueued;
	}
	public void setNoOfVehiclesQueued(int noOfVehiclesQueued) {
		this.noOfVehiclesQueued = noOfVehiclesQueued;
	}
	public int getNoOfVehiclesStranded() {
		return noOfVehiclesStranded;
	}
	public void setNoOfVehiclesStranded(int noOfVehiclesStranded) {
		this.noOfVehiclesStranded = noOfVehiclesStranded;
	}
	public Date getLastQLAlertSendAt() {
		return lastQLAlertSendAt;
	}
	public void setLastQLAlertSendAt(Date lastQLAlertSendAt) {
		this.lastQLAlertSendAt = lastQLAlertSendAt;
	}
	public Date getLastSVAlertSendAt() {
		return lastSVAlertSendAt;
	}
	public void setLastSVAlertSendAt(Date lastSVAlertSendAt) {
		this.lastSVAlertSendAt = lastSVAlertSendAt;
	}
	public Date getLastPTAlertSendAt() {
		return lastPTAlertSendAt;
	}
	public void setLastPTAlertSendAt(Date lastPTAlertSendAt) {
		this.lastPTAlertSendAt = lastPTAlertSendAt;
	}
	public Date getLastNOAlertSendAt() {
		return lastNOAlertSendAt;
	}
	public void setLastNOAlertSendAt(Date lastNOAlertSendAt) {
		this.lastNOAlertSendAt = lastNOAlertSendAt;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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
	public int getRuleId() {
		return ruleId;
	}
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}
	public int getNotificationSetId() {
		return notificationSetId;
	}
	public void setNotificationSetId(int notificationSetId) {
		this.notificationSetId = notificationSetId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Date getValidFromDate() {
		return validFromDate;
	}
	public void setValidFromDate(Date validFromDate) {
		this.validFromDate = validFromDate;
	}
	public Date getValidToDate() {
		return validToDate;
	}
	public void setValidToDate(Date validToDate) {
		this.validToDate = validToDate;
	}
	public int getCustomerContactId() {
		return customerContactId;
	}
	public void setCustomerContactId(int customerContactId) {
		this.customerContactId = customerContactId;
	}
	public Date getContactTimeFromTime() {
		return contactTimeFromTime;
	}
	public void setContactTimeFromTime(Date contactTimeFromTime) {
		this.contactTimeFromTime = contactTimeFromTime;
	}
	public Date getContactTimeToTime() {
		return contactTimeToTime;
	}
	public void setContactTimeToTime(Date contactTimeToTime) {
		this.contactTimeToTime = contactTimeToTime;
	}
	
	
}

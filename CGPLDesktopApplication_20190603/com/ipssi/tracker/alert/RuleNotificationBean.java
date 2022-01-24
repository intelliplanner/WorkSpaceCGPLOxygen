package com.ipssi.tracker.alert;

import java.util.Date;

import com.ipssi.gen.utils.Misc;

public class RuleNotificationBean {
	
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	private int status;
	private int regionId;
	private int ruleId;
	private int notificationSetId;
	private int forThresholdLevel;
	private int type;
	private Date validFromDate;
	private Date validToDate;
	private String validFrom;
	private String validTo;
	private int customerContactId;
	private String contactTimeFrom;
	private String contactTimeTo;
	private Date contactTimeFromTime;
	private Date contactTimeToTime;
	private int opThreshold = Misc.getUndefInt();
	private String opStationName;
	public String getOpStationName() {
		return opStationName;
	}
	public void setOpStationName(String opStationName) {
		this.opStationName = opStationName;
	}
	public int getOpThreshold() {
		return opThreshold;
	}
	public void setOpThreshold(int opThreshold) {
		this.opThreshold = opThreshold;
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
	public int getForThresholdLevel() {
		return forThresholdLevel;
	}
	public void setForThresholdLevel(int forThresholdLevel) {
		this.forThresholdLevel = forThresholdLevel;
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
	public String getValidFrom() {
		return validFrom;
	}
	public void setValidFrom(String validFrom) {
		this.validFrom = validFrom;
	}
	public String getValidTo() {
		return validTo;
	}
	public void setValidTo(String validTo) {
		this.validTo = validTo;
	}
	public int getCustomerContactId() {
		return customerContactId;
	}
	public void setCustomerContactId(int customerContactId) {
		this.customerContactId = customerContactId;
	}
	public String getContactTimeFrom() {
		return contactTimeFrom;
	}
	public void setContactTimeFrom(String contactTimeFrom) {
		this.contactTimeFrom = contactTimeFrom;
	}
	public String getContactTimeTo() {
		return contactTimeTo;
	}
	public void setContactTimeTo(String contactTimeTo) {
		this.contactTimeTo = contactTimeTo;
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
	public void setStatus(int status) {
		this.status = status;
	}
	public int getStatus() {
		return status;
	}
	
	
}

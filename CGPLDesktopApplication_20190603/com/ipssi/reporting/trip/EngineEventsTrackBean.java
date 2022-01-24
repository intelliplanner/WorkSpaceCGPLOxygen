package com.ipssi.reporting.trip;

import java.util.Date;

public class EngineEventsTrackBean {
	private int engineEventId;
	private String vehicleName;
	private int vehicleId;
	private int ruleId;
	private String ruleName;
	private double eventBeginLongitude;
	private double eventBeginLatitude;
	private double eventEndLongitude;
	private double eventEndLatitude;
	private Date eventStartTime;
	private Date eventStopTime;
	private String eventStartTimeStr;
	private int attributeId;
	private double attributeValue;
	private Date updatedOn;
	private Date eventCreateRecTime;
	private String eventStartName;
	private String eventEndName;
	private int reason1;
	private Date reason1UpdatedOn;
	private String comment1;
	private int reason2;
	private Date reason2UpdatedOn;
	private String comment2;
	private int reason3;
	private Date reason3UpdatedOn;
	private String comment3;
	private String reason1UpdatedOnStr;
	private String reason2UpdatedOnStr;
	private String reason3UpdatedOnStr;
	private boolean isEnggEventTrack = false;
	private String question;
	private Date questCreatedDate;
	private String questCreatedDateStr;
	private int status;
	private int assignedTo;
	private int priority;
	private long reason1UpdatedById;
	private long reason2UpdatedById;
	private String reason1UpdatedBy;
	private String reason2UpdatedBy;
	private long alarmCreatedById;
	private String alarmCreatedBy;
	private int eventType;
	private int level;
	private int unsafeZoneAction;
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getEventType() {
		return eventType;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	public long getAlarmCreatedById() {
		return alarmCreatedById;
	}
	public void setAlarmCreatedById(long alarmCreatedById) {
		this.alarmCreatedById = alarmCreatedById;
	}
	public String getAlarmCreatedBy() {
		return alarmCreatedBy;
	}
	public void setAlarmCreatedBy(String alarmCreatedBy) {
		this.alarmCreatedBy = alarmCreatedBy;
	}
	public long getReason1UpdatedById() {
		return reason1UpdatedById;
	}
	public void setReason1UpdatedById(long reason1UpdatedById) {
		this.reason1UpdatedById = reason1UpdatedById;
	}
	public long getReason2UpdatedById() {
		return reason2UpdatedById;
	}
	public void setReason2UpdatedById(long reason2UpdatedById) {
		this.reason2UpdatedById = reason2UpdatedById;
	}
	public String getReason1UpdatedBy() {
		return reason1UpdatedBy;
	}
	public void setReason1UpdatedBy(String reason1UpdatedBy) {
		this.reason1UpdatedBy = reason1UpdatedBy;
	}
	public String getReason2UpdatedBy() {
		return reason2UpdatedBy;
	}
	public void setReason2UpdatedBy(String reason2UpdatedBy) {
		this.reason2UpdatedBy = reason2UpdatedBy;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getAssignedTo() {
		return assignedTo;
	}
	public void setAssignedTo(int assignedTo) {
		this.assignedTo = assignedTo;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getEventStartTimeStr() {
		return eventStartTimeStr;
	}
	public void setEventStartTimeStr(String eventStartTimeStr) {
		this.eventStartTimeStr = eventStartTimeStr;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getQuestCreatedDateStr() {
		return questCreatedDateStr;
	}
	public void setQuestCreatedDateStr(String questCreatedDateStr) {
		this.questCreatedDateStr = questCreatedDateStr;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public Date getQuestCreatedDate() {
		return questCreatedDate;
	}
	public void setQuestCreatedDate(Date questCreatedDate) {
		this.questCreatedDate = questCreatedDate;
	}
	public boolean isEnggEventTrack() {
		return isEnggEventTrack;
	}
	public void setEnggEventTrack(boolean isEnggEventTrack) {
		this.isEnggEventTrack = isEnggEventTrack;
	}
	public String getReason1UpdatedOnStr() {
		return reason1UpdatedOnStr;
	}
	public void setReason1UpdatedOnStr(String reason1UpdatedOnStr) {
		this.reason1UpdatedOnStr = reason1UpdatedOnStr;
	}
	public String getReason2UpdatedOnStr() {
		return reason2UpdatedOnStr;
	}
	public void setReason2UpdatedOnStr(String reason2UpdatedOnStr) {
		this.reason2UpdatedOnStr = reason2UpdatedOnStr;
	}
	public String getReason3UpdatedOnStr() {
		return reason3UpdatedOnStr;
	}
	public void setReason3UpdatedOnStr(String reason3UpdatedOnStr) {
		this.reason3UpdatedOnStr = reason3UpdatedOnStr;
	}
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
	public int getRuleId() {
		return ruleId;
	}
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}
	public double getEventBeginLongitude() {
		return eventBeginLongitude;
	}
	public void setEventBeginLongitude(double eventBeginLongitude) {
		this.eventBeginLongitude = eventBeginLongitude;
	}
	public double getEventBeginLatitude() {
		return eventBeginLatitude;
	}
	public void setEventBeginLatitude(double eventBeginLatitude) {
		this.eventBeginLatitude = eventBeginLatitude;
	}
	public double getEventEndLongitude() {
		return eventEndLongitude;
	}
	public void setEventEndLongitude(double eventEndLongitude) {
		this.eventEndLongitude = eventEndLongitude;
	}
	public double getEventEndLatitude() {
		return eventEndLatitude;
	}
	public void setEventEndLatitude(double eventEndLatitude) {
		this.eventEndLatitude = eventEndLatitude;
	}
	public Date getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(Date eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public Date getEventStopTime() {
		return eventStopTime;
	}
	public void setEventStopTime(Date eventStopTime) {
		this.eventStopTime = eventStopTime;
	}
	public int getAttributeId() {
		return attributeId;
	}
	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}
	public double getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(double attributeValue) {
		this.attributeValue = attributeValue;
	}
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public Date getEventCreateRecTime() {
		return eventCreateRecTime;
	}
	public void setEventCreateRecTime(Date eventCreateRecTime) {
		this.eventCreateRecTime = eventCreateRecTime;
	}
	public String getEventStartName() {
		return eventStartName;
	}
	public void setEventStartName(String eventStartName) {
		this.eventStartName = eventStartName;
	}
	public String getEventEndName() {
		return eventEndName;
	}
	public void setEventEndName(String eventEndName) {
		this.eventEndName = eventEndName;
	}
	public int getReason1() {
		return reason1;
	}
	public void setReason1(int reason1) {
		this.reason1 = reason1;
	}
	public Date getReason1UpdatedOn() {
		return reason1UpdatedOn;
	}
	public void setReason1UpdatedOn(Date reason1UpdatedOn) {
		this.reason1UpdatedOn = reason1UpdatedOn;
	}
	public String getComment1() {
		return comment1;
	}
	public void setComment1(String comment1) {
		this.comment1 = comment1;
	}
	public int getReason2() {
		return reason2;
	}
	public void setReason2(int reason2) {
		this.reason2 = reason2;
	}
	public Date getReason2UpdatedOn() {
		return reason2UpdatedOn;
	}
	public void setReason2UpdatedOn(Date reason2UpdatedOn) {
		this.reason2UpdatedOn = reason2UpdatedOn;
	}
	public String getComment2() {
		return comment2;
	}
	public void setComment2(String comment2) {
		this.comment2 = comment2;
	}
	public int getReason3() {
		return reason3;
	}
	public void setReason3(int reason3) {
		this.reason3 = reason3;
	}
	public Date getReason3UpdatedOn() {
		return reason3UpdatedOn;
	}
	public void setReason3UpdatedOn(Date reason3UpdatedOn) {
		this.reason3UpdatedOn = reason3UpdatedOn;
	}
	public String getComment3() {
		return comment3;
	}
	public void setComment3(String comment3) {
		this.comment3 = comment3;
	}
	public int getUnsafeZoneAction() {
		return unsafeZoneAction;
	}
	public void setUnsafeZoneAction(int unsafeZoneAction) {
		this.unsafeZoneAction = unsafeZoneAction;
	}
	
}

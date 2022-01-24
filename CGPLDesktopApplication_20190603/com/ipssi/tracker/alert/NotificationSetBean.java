package com.ipssi.tracker.alert;

import java.util.ArrayList;
import java.util.Date;

public class NotificationSetBean {
	private int id;
	private int opId;
public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}
private String name;
private int status;
private Date statusFrom_date;
private Date statusTo_date;
private String statusFrom;
private String statusTo;
private int portNodeId;
private String notes;
private Date updatedOn;
private String applicableTo;
private int loadStatus;
private int createType;
private int opstationSubtype;
private int relativeDurOperator;
private int relativeDurOperand1;
private int relativeDurOperand2;
private String loadingAt;
private String unloadingAt;
private int eventDurOperator;
private int eventDurOperand1;
private int eventDurOperand2;
private int eventDistOperator;
private int eventDistOperand1;
private int eventDistOperand2; 
public int getLoadStatus() {
	return loadStatus;
}
public void setLoadStatus(int loadStatus) {
	this.loadStatus = loadStatus;
}
public int getCreateType() {
	return createType;
}
public void setCreateType(int createType) {
	this.createType = createType;
}
public int getOpstationSubtype() {
	return opstationSubtype;
}
public void setOpstationSubtype(int opstationSubtype) {
	this.opstationSubtype = opstationSubtype;
}
public int getRelativeDurOperator() {
	return relativeDurOperator;
}
public void setRelativeDurOperator(int relativeDurOperator) {
	this.relativeDurOperator = relativeDurOperator;
}
public int getRelativeDurOperand1() {
	return relativeDurOperand1;
}
public void setRelativeDurOperand1(int relativeDurOperand1) {
	this.relativeDurOperand1 = relativeDurOperand1;
}
public int getRelativeDurOperand2() {
	return relativeDurOperand2;
}
public void setRelativeDurOperand2(int relativeDurOperand2) {
	this.relativeDurOperand2 = relativeDurOperand2;
}
public String getLoadingAt() {
	return loadingAt;
}
public void setLoadingAt(String loadingAt) {
	this.loadingAt = loadingAt;
}
public String getUnloadingAt() {
	return unloadingAt;
}
public void setUnloadingAt(String unloadingAt) {
	this.unloadingAt = unloadingAt;
}
public int getEventDurOperator() {
	return eventDurOperator;
}
public void setEventDurOperator(int eventDurOperator) {
	this.eventDurOperator = eventDurOperator;
}
public int getEventDurOperand1() {
	return eventDurOperand1;
}
public void setEventDurOperand1(int eventDurOperand1) {
	this.eventDurOperand1 = eventDurOperand1;
}
public int getEventDurOperand2() {
	return eventDurOperand2;
}
public void setEventDurOperand2(int eventDurOperand2) {
	this.eventDurOperand2 = eventDurOperand2;
}
public int getEventDistOperator() {
	return eventDistOperator;
}
public void setEventDistOperator(int eventDistOperator) {
	this.eventDistOperator = eventDistOperator;
}
public int getEventDistOperand1() {
	return eventDistOperand1;
}
public void setEventDistOperand1(int eventDistOperand1) {
	this.eventDistOperand1 = eventDistOperand1;
}
public int getEventDistOperand2() {
	return eventDistOperand2;
}
public void setEventDistOperand2(int eventDistOperand2) {
	this.eventDistOperand2 = eventDistOperand2;
}
private ArrayList<RuleNotificationBean> ruleNotificationBeanList = new ArrayList<RuleNotificationBean>();

public ArrayList<RuleNotificationBean> getRuleNotificationBeanList() {
	return ruleNotificationBeanList;
}
public void setRuleNotificationBeanList(ArrayList<RuleNotificationBean> ruleNotificationBeanList) {
	this.ruleNotificationBeanList = ruleNotificationBeanList;
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public int getStatus() {
	return status;
}
public void setStatus(int status) {
	this.status = status;
}
public Date getStatusFrom_date() {
	return statusFrom_date;
}
public void setStatusFrom_date(Date statusFrom_date) {
	this.statusFrom_date = statusFrom_date;
}
public Date getStatusTo_date() {
	return statusTo_date;
}
public void setStatusTo_date(Date statusTo_date) {
	this.statusTo_date = statusTo_date;
}
public String getStatusFrom() {
	return statusFrom;
}
public void setStatusFrom(String statusFrom) {
	this.statusFrom = statusFrom;
}
public String getStatusTo() {
	return statusTo;
}
public void setStatusTo(String statusTo) {
	this.statusTo = statusTo;
}
public int getPortNodeId() {
	return portNodeId;
}
public void setPortNodeId(int portNodeId) {
	this.portNodeId = portNodeId;
}
public String getNotes() {
	return notes;
}
public void setNotes(String notes) {
	this.notes = notes;
}
public Date getUpdatedOn() {
	return updatedOn;
}
public void setUpdatedOn(Date updatedOn) {
	this.updatedOn = updatedOn;
}
public String getApplicableTo() {
	return applicableTo;
}
public void setApplicableTo(String applicableTo) {
	this.applicableTo = applicableTo;
}

}

package com.ipssi.communicator.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Triple;

public class CommunicatorDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Integer> unDeliveredIdList = new ArrayList<Integer>();
	
	private long id;
	
	private String to;

	private String cc;

	private String bcc;

	private String from;

	private String subject;

	private String body;

	private int vehicleId;

	private Timestamp createdDate;

	private Timestamp lastAttemptedDate;

	private String status;

	private int notificationType;
	
	private HashMap<String, Triple<String, String, Timestamp>> list = new HashMap<String, Triple<String, String, Timestamp>>();
	
	public int retryCount = 0;
	
	public int alertIndex ;
	
	private int ruleId;
	
	private int engineEventId;
	
	private boolean forceSend; 
	
	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getTo() {
		return to;
	}


	public void setTo(String to) {
		this.to = to;
	}


	public String getCc() {
		return cc;
	}


	public void setCc(String cc) {
		this.cc = cc;
	}


	public String getBcc() {
		return bcc;
	}


	public void setBcc(String bcc) {
		this.bcc = bcc;
	}


	public String getFrom() {
		return from;
	}


	public void setFrom(String from) {
		this.from = from;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}


	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}


	public int getVehicleId() {
		return vehicleId;
	}


	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}


	public Timestamp getCreatedDate() {
		return createdDate;
	}


	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}


	public Timestamp getLastAttemptedDate() {
		return lastAttemptedDate;
	}


	public void setLastAttemptedDate(Timestamp lastAttemptedDate) {
		this.lastAttemptedDate = lastAttemptedDate;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public int getNotificationType() {
		return notificationType;
	}


	public void setNotificationType(int notificationType) {
		this.notificationType = notificationType;
	}


	/**
	 * @param list the list to set
	 */
	public void setDeliveryReportList(HashMap<String, Triple<String, String, Timestamp>> list) {
		this.list = list;
	}


	/**
	 * @return the list
	 */
	public HashMap<String, Triple<String, String, Timestamp>> getDeliveryReportList() {
		return list;
	}


	/**
	 * @return the alertIndex
	 */
	public int getAlertIndex() {
		return alertIndex;
	}


	/**
	 * @param alertIndex the alertIndex to set
	 */
	public void setAlertIndex(int alertIndex) {
		this.alertIndex = alertIndex;
	}


	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}


	public int getRuleId() {
		return ruleId;
	}


	/**
	 * @return the engineEventId
	 */
	public int getEngineEventId() {
		return engineEventId;
	}


	/**
	 * @param engineEventId the engineEventId to set
	 */
	public void setEngineEventId(int engineEventId) {
		this.engineEventId = engineEventId;
	}


	public void setForceSend(boolean forceSend) {
		this.forceSend = forceSend;
	}


	public boolean isForceSend() {
		return forceSend;
	}
		
	  
}

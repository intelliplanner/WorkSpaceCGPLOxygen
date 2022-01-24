package com.ipssi.rfid.beans;

import java.util.Date;

public class TPRFitness {

	private int rfidHandheldId;                    
	private int blacklisted;                          
	private int rfidStatus;                          
	private int gpsStatus;                           
	private int documentStatus;                      
	private int actionTaken;                         
	private Date createdOn;                         
	private Date updatedOn;  
	private int updatedBy;
	public int getRfidHandheldId() {
		return rfidHandheldId;
	}
	public int getBlacklisted() {
		return blacklisted;
	}
	public int getRfidStatus() {
		return rfidStatus;
	}
	public int getGpsStatus() {
		return gpsStatus;
	}
	public int getDocumentStatus() {
		return documentStatus;
	}
	public int getActionTaken() {
		return actionTaken;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setRfidHandheldId(int rfidHandheldId) {
		this.rfidHandheldId = rfidHandheldId;
	}
	public void setBlacklisted(int blacklisted) {
		this.blacklisted = blacklisted;
	}
	public void setRfidStatus(int rfidStatus) {
		this.rfidStatus = rfidStatus;
	}
	public void setGpsStatus(int gpsStatus) {
		this.gpsStatus = gpsStatus;
	}
	public void setDocumentStatus(int documentStatus) {
		this.documentStatus = documentStatus;
	}
	public void setActionTaken(int actionTaken) {
		this.actionTaken = actionTaken;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	} 
    
}

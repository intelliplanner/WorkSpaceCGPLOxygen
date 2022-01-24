package com.ipssi.gen.deviceMessaging;

import java.util.Date;

import com.ipssi.gen.utils.Misc;

public class Message {
	//will expire unsent messages in X seconds
	
	private String message = null;
	private Date inDate = null;
	private int id = Misc.getUndefInt(); //message Id in DB
	private Date latestTryDate = null;
	private Date acknowledgeDate = null;
	private MessageStatus status = MessageStatus.CREATED;
	private int deliveryMode = 0; //0 = GPRS, 1 = SMS
	private String acknowledgeMessage = null;
	private int retentionStrategy = Misc.getUndefInt();
	public Message(String message) {
		this.message = message;
		inDate = new Date();
	}
	public int getRetentionStrategy() {
		return this.retentionStrategy;
	}
	public void setRetentionStrategy(int retentionStrategy) {
		this.retentionStrategy = retentionStrategy;
	}
	private void fixDates() {
		if (inDate == null) {
			if (latestTryDate != null)
				inDate = new Date(latestTryDate.getTime());
			else if (acknowledgeDate == null)
				inDate = new Date(acknowledgeDate.getTime());
			else
				inDate = new Date();
		}
		if (status == MessageStatus.ACKNOWLEDGED || status == MessageStatus.UNACKNOWLEDGED) {
			if (acknowledgeDate == null) {
				acknowledgeDate = new Date (latestTryDate != null ? latestTryDate.getTime() : this.inDate.getTime());
			}
			if (latestTryDate == null) {
				latestTryDate = new Date(acknowledgeDate.getTime());
			}
		}
		else if (status == MessageStatus.SENT) {
			if (latestTryDate == null)
				latestTryDate = new Date(inDate.getTime());
		}
	}
	public Message(String message, int id, Date inDate, Date latestTryDate, Date acknowledgeDate, int status, int deliveryMode, String acknowledgeMessage, int retentionStrategy) {
		this.message = message;
		this.id = id;
		this.inDate = inDate;
		this.latestTryDate = latestTryDate;
		this.acknowledgeDate = acknowledgeDate;
		this.status = MessageStatus.toMessageStatus(status);
		this.deliveryMode = deliveryMode;
		this.setAcknowledgeMessage(acknowledgeMessage);
		this.retentionStrategy = retentionStrategy;
		this.fixDates();
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getInDate() {
		return inDate;
	}
	public void setInDate(Date inDate) {
		this.inDate = inDate;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Date getLatestTryDate() {
		return latestTryDate;
	}
	public void setLatestTryDate(Date latestTryDate) {
		this.latestTryDate = latestTryDate;
	}
	public Date getAcknowledgeDate() {
		return acknowledgeDate;
	}
	public void setAcknowledgeDate(Date acknowledgeDate) {
		this.acknowledgeDate = acknowledgeDate;
	}
	public MessageStatus getStatus() {
		return status;
	}
	public void setStatus(MessageStatus status) {
		this.status = status;
	}
	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}
	public int getDeliveryMode() {
		return deliveryMode;
	}
	public void setAcknowledgeMessage(String acknowledgeMessage) {
		this.acknowledgeMessage = acknowledgeMessage;
	}
	public String getAcknowledgeMessage() {
		return acknowledgeMessage;
	}
	
}

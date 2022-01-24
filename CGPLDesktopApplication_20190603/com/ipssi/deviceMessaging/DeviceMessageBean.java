package com.ipssi.deviceMessaging;

import java.util.Date;

import com.ipssi.gen.deviceMessaging.MessageStatus;

public class DeviceMessageBean {
	private int vehicleId;
   	private String vehicleName;
   	private String phoneNumber;
   	private String lastMessage; //overloaded when reading vals - will use this to get vals
   	private MessageStatus lastMessageStatus;
   	private Date lastMessageTime;
   	private int ownerOrg;
   	private String lastMessageFromDevice;
   	private Date lastMessageFromDeviceTime;
   	private String deviceId;
   	public DeviceMessageBean(int vehicleId, String message) {
   		this.vehicleId = vehicleId;
   		this.lastMessage = message;
   	}
   	public DeviceMessageBean(int vehicleId, String vehicleName, String phoneNumber, String lastMessage, int lastMessageStatus, Date lastMessageTime, int ownerOrg, String lastMessageFromDevice, Date lastMessageFromDeviceTime, String deviceId) {
   		this.vehicleId = vehicleId;
   		this.vehicleName = vehicleName;
   		this.phoneNumber = phoneNumber;
   		this.lastMessage = lastMessage;
   		this.lastMessageStatus = MessageStatus.toMessageStatus(lastMessageStatus);
   		this.lastMessageTime = lastMessageTime;
   		this.setOwnerOrg(ownerOrg);
   		this.lastMessageFromDevice = lastMessageFromDevice;
   		this.lastMessageFromDeviceTime = lastMessageFromDeviceTime;
   		this.deviceId = deviceId;
   	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getLastMessage() {
		return lastMessage;
	}
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	public MessageStatus getLastMessageStatus() {
		return lastMessageStatus;
	}
	public void setLastMessageStatus(MessageStatus lastMessageStatus) {
		this.lastMessageStatus = lastMessageStatus;
	}
	public Date getLastMessageTime() {
		return lastMessageTime;
	}
	public void setLastMessageTime(Date lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
	public void setOwnerOrg(int ownerOrg) {
		this.ownerOrg = ownerOrg;
	}
	public int getOwnerOrg() {
		return ownerOrg;
	}
	public void setLastMessageFromDevice(String lastMessageFromDevice) {
		this.lastMessageFromDevice = lastMessageFromDevice;
	}
	public String getLastMessageFromDevice() {
		return lastMessageFromDevice;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setLastMessageFromDeviceTime(Date lastMessageFromDeviceTime) {
		this.lastMessageFromDeviceTime = lastMessageFromDeviceTime;
	}
	public Date getLastMessageFromDeviceTime() {
		return lastMessageFromDeviceTime;
	}
}

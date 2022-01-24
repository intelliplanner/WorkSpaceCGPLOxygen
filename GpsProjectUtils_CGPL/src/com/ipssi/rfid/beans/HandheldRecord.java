package com.ipssi.rfid.beans;

import java.util.Date;

public class HandheldRecord {

	private int id;                                  
	private int deviceId;                           
	private int vehicleId;                          
	private String vehicleName;                    
	private String epcId;                          
	private int minesId;                               
	private int transporterId;                         
	private Date recordTime;                        
	private int doId;                               
	private String challanNo;                      
	private String lrNo;                           
	private int grade;                               
	private int material;                            
	private double loadTare;                            
	private double loadGross;                           
	private int preMinesId;                           
	private int preDeviceId;                       
	private int preRecordId;                       
	private String preChallanNo;                      
	private int isData;                             
	private int writeStatus;                        
	private byte[] tagData;                                            
	private int recordUser;                         
	private Date createdOn;                         
	private Date updatedOn;  
	private int updatedBy;
	public int getId() {
		return id;
	}
	public int getDeviceId() {
		return deviceId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public String getEpcId() {
		return epcId;
	}
	public int getMinesId() {
		return minesId;
	}
	public int getTransporterId() {
		return transporterId;
	}
	public Date getRecordTime() {
		return recordTime;
	}
	public int getDoId() {
		return doId;
	}
	public String getChallanNo() {
		return challanNo;
	}
	public String getLrNo() {
		return lrNo;
	}
	public int getGrade() {
		return grade;
	}
	public int getMaterial() {
		return material;
	}
	public double getLoadTare() {
		return loadTare;
	}
	public double getLoadGross() {
		return loadGross;
	}
	public int getPreMinesId() {
		return preMinesId;
	}
	public int getPreDeviceId() {
		return preDeviceId;
	}
	public int getPreRecordId() {
		return preRecordId;
	}
	public String getPreChallanNo() {
		return preChallanNo;
	}
	public int getIsData() {
		return isData;
	}
	public int getWriteStatus() {
		return writeStatus;
	}
	public byte[] getTagData() {
		return tagData;
	}
	public int getRecordUser() {
		return recordUser;
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
	public void setId(int id) {
		this.id = id;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public void setEpcId(String epcId) {
		this.epcId = epcId;
	}
	public void setMinesId(int minesId) {
		this.minesId = minesId;
	}
	public void setTransporterId(int transporterId) {
		this.transporterId = transporterId;
	}
	public void setRecordTime(Date recordTime) {
		this.recordTime = recordTime;
	}
	public void setDoId(int doId) {
		this.doId = doId;
	}
	public void setChallanNo(String challanNo) {
		this.challanNo = challanNo;
	}
	public void setLrNo(String lrNo) {
		this.lrNo = lrNo;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public void setMaterial(int material) {
		this.material = material;
	}
	public void setLoadTare(double loadTare) {
		this.loadTare = loadTare;
	}
	public void setLoadGross(double loadGross) {
		this.loadGross = loadGross;
	}
	public void setPreMinesId(int preMinesId) {
		this.preMinesId = preMinesId;
	}
	public void setPreDeviceId(int preDeviceId) {
		this.preDeviceId = preDeviceId;
	}
	public void setPreRecordId(int preRecordId) {
		this.preRecordId = preRecordId;
	}
	public void setPreChallanNo(String preChallanNo) {
		this.preChallanNo = preChallanNo;
	}
	public void setIsData(int isData) {
		this.isData = isData;
	}
	public void setWriteStatus(int writeStatus) {
		this.writeStatus = writeStatus;
	}
	public void setTagData(byte[] tagData) {
		this.tagData = tagData;
	}
	public void setRecordUser(int recordUser) {
		this.recordUser = recordUser;
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

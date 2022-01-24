package com.ipssi.rfid.beans;

import java.util.Date;

public class DocumentOrderDetail {
    
	private int id;
	private String mplId;
	private int type;
	private String doNumber;
	private String otherRefNo;
	private int seller;
	private int minesId;
	private Date startDate;
	private Date targetLiftDate;
	private Date lapseDate;
	private String remarks;
	private int portNodeId;
	private int status;
	private Date createdOn;                         
	private Date updatedOn;  
	private int updatedBy;
	public int getId() {
		return id;
	}
	public String getMplId() {
		return mplId;
	}
	public int getType() {
		return type;
	}
	public String getDoNumber() {
		return doNumber;
	}
	public String getOtherRefNo() {
		return otherRefNo;
	}
	public int getSeller() {
		return seller;
	}
	public int getMinesId() {
		return minesId;
	}
	public Date getStartDate() {
		return startDate;
	}
	public Date getTargetLiftDate() {
		return targetLiftDate;
	}
	public Date getLapseDate() {
		return lapseDate;
	}
	public String getRemarks() {
		return remarks;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public int getStatus() {
		return status;
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
	public void setMplId(String mplId) {
		this.mplId = mplId;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}
	public void setOtherRefNo(String otherRefNo) {
		this.otherRefNo = otherRefNo;
	}
	public void setSeller(int seller) {
		this.seller = seller;
	}
	public void setMinesId(int minesId) {
		this.minesId = minesId;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public void setTargetLiftDate(Date targetLiftDate) {
		this.targetLiftDate = targetLiftDate;
	}
	public void setLapseDate(Date lapseDate) {
		this.lapseDate = lapseDate;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public void setStatus(int status) {
		this.status = status;
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

package com.ipssi.tracker.customer;

import java.util.ArrayList;
import java.util.Date;


public class CustomerBean {
	private int id;
	private String custName;
	private String shortCode;
	private int activeFlag;
	private String custNote;
	private int custType;
	private String partner;
	private String location;
	private int numDevices;
	private Date updatedOn;
	private Date createdOn;
	private String createdBy;
	private ArrayList<CustomerContactBean> custContactList = new ArrayList<CustomerContactBean>();

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getCustNote() {
		return custNote;
	}

	public void setCustNote(String custNote) {
		this.custNote = custNote;
	}

	public int getCustType() {
		return custType;
	}

	public void setCustType(int custType) {
		this.custType = custType;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getNumDevices() {
		return numDevices;
	}

	public void setNumDevices(int numDevices) {
		this.numDevices = numDevices;
	}

	public ArrayList<CustomerContactBean> getCustContactList() {
		return custContactList;
	}

	public void setCustContactList(ArrayList<CustomerContactBean> custContactList) {
		this.custContactList = custContactList;
	}

	public void addCustContactList(CustomerContactBean custContact) {
		this.custContactList.add(custContact);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
}

package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.Unique;
@Table("transporter_details")
public class Transporter {
	@KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
	private int id = Misc.getUndefInt();
	
	@Column("name")
	private String name;
	
	@Column("sap_code")
	private String sapCode;
	
	@Unique
	@Column("sn")
	private String code;
	
	@Column("port_node_id")
	private int portNodeId = Misc.getUndefInt();
	
	@Column("tare_freq")
	private int tareFreq = Misc.getUndefInt();
	private String refPONumber;
	private String refPOLineItem;
	private double loadAllocation;
	
	@Column("created_on")
	private Date createdOn;
	
	@Column("updated_on")
	private Date updatedOn;
	
	@Column("updated_by")
	private int updatedBy = Misc.getUndefInt();
	@Column("status")
	private int status = Misc.getUndefInt();
	
	@Column("material_cat")
	private int materialCat = Misc.getUndefInt();
	
	
	
	public int getMaterialCat() {
		return materialCat;
	}
	public void setMaterialCat(int materialCat) {
		this.materialCat = materialCat;
	}
	public int getTareFreq() {
		return tareFreq;
	}
	public void setTareFreq(int tareFreq) {
		this.tareFreq = tareFreq;
	}
	public String getSapCode() {
		return sapCode;
	}
	public void setSapCode(String sapCode) {
		this.sapCode = sapCode;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getRefPONumber() {
		return refPONumber;
	}
	public String getRefPOLineItem() {
		return refPOLineItem;
	}
	public double getLoadAllocation() {
		return loadAllocation;
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
	public void setName(String name) {
		this.name = name;
	}
	public void setRefPONumber(String refPONumber) {
		this.refPONumber = refPONumber;
	}
	public void setRefPOLineItem(String refPOLineItem) {
		this.refPOLineItem = refPOLineItem;
	}
	public void setLoadAllocation(double loadAllocation) {
		this.loadAllocation = loadAllocation;
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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}  
    
}

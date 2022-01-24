package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
@Table("block_instruction")
public class BlockingInstruction {

	@KEY
	@PRIMARY_KEY
	@GENRATED
	@Column("id")
	int id = Misc.getUndefInt();
	
	@Column("vehicle_id")
	int vehicleId = Misc.getUndefInt();
	
	@Column("port_node_id")
	int portNodeId = Misc.getUndefInt();
	
	@Column("type")
	int type = Misc.getUndefInt();
	
	@Column("status")
	int status = Misc.getUndefInt();
	
	@Column("block_from")
	Date blockFrom;
	
	@Column("block_to")
	Date blockTo;
	
	@Column("created_on")
	Date createdOn;
	
	@Column("created_by")
	int createdBy = Misc.getUndefInt();
	
	@Column("updated_on")
	Date updatedOn;
	
	@Column("updated_by")
	int updatedBy = Misc.getUndefInt();
	
	@Column("notes")
	String notes;
	
	@Column("material_cat")
	int materialCat = Misc.getUndefInt();

	public int getId() {
		return id;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	
	public int getStatus() {
		return status;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public int getCreatedBy() {
		return createdBy;
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
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Date getBlockFrom() {
		return blockFrom;
	}
	public Date getBlockTo() {
		return blockTo;
	}
	public void setBlockFrom(Date blockFrom) {
		this.blockFrom = blockFrom;
	}
	public void setBlockTo(Date blockTo) {
		this.blockTo = blockTo;
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
	public int getMaterialCat() {
		return materialCat;
	}
	public void setMaterialCat(int materialCat) {
		this.materialCat = materialCat;
	}

}

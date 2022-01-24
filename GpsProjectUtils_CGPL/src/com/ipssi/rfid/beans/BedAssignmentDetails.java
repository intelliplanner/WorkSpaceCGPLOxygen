package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("bed_assignment_details")
public class BedAssignmentDetails {
	
	@KEY
	@PRIMARY_KEY
	@GENRATED
	@Column("id") 
	private int id = Misc.getUndefInt();
	@Column("transporter_id")
	private int transportId = Misc.getUndefInt();
	@Column("curr_bed_module") 
	private int curr_bed_module = Misc.getUndefInt();
	@Column("curr_start_hopper_no")
	private int currStartHopperNo = Misc.getUndefInt();
	@Column("curr_end_hopper_no")         
	private int currEndHopperNo = Misc.getUndefInt();
	@Column("curr_start_date")
	private Date currStartDate = null;
	@Column("curr_end_date")
	private Date currEndDate = null;
	@Column("port_node_id") 
	private int portNodeId = Misc.getUndefInt();
	@Column("status") 
	private int status = Misc.getUndefInt();
	@Column("created_by")
	private int createdBy = Misc.getUndefInt();
	@Column("updated_by") 
	private int updatedBy = Misc.getUndefInt();
	@Column("created_on")
	private Date createdOn; 
	@Column("updated_on")
	private Date updatedOn;
	@Column("name")
	private String name;
	@Column("comments")
	private String comment;
	@Column("hopper_2_start")
	private int hopperTwoStart = Misc.getUndefInt();
	@Column("hopper_2_end")
	private int hopperTwoEnd = Misc.getUndefInt();
	@Column("mines_id")
	private int minesId = Misc.getUndefInt();
	@Column("grade_id")
	private int gradeId = Misc.getUndefInt();
	public int getId() {
		return id;
	}
	public int getTransportId() {
		return transportId;
	}
	
	public int getCurr_bed_module() {
		return curr_bed_module;
	}
	public int getCurrStartHopperNo() {
		return currStartHopperNo;
	}
	public int getCurrEndHopperNo() {
		return currEndHopperNo;
	}
	public Date getCurrStartDate() {
		return currStartDate;
	}
	public Date getCurrEndDate() {
		return currEndDate;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public int getStatus() {
		return status;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public String getName() {
		return name;
	}
	public String getComment() {
		return comment;
	}
	public int getHopperTwoStart() {
		return hopperTwoStart;
	}
	public int getHopperTwoEnd() {
		return hopperTwoEnd;
	}
	public int getMinesId() {
		return minesId;
	}
	public int getGradeId() {
		return gradeId;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setTransportId(int transportId) {
		this.transportId = transportId;
	}
	
	public void setCurr_bed_module(int curr_bed_module) {
		this.curr_bed_module = curr_bed_module;
	}
	public void setCurrStartHopperNo(int currStartHopperNo) {
		this.currStartHopperNo = currStartHopperNo;
	}
	public void setCurrEndHopperNo(int currEndHopperNo) {
		this.currEndHopperNo = currEndHopperNo;
	}
	public void setCurrStartDate(Date currStartDate) {
		this.currStartDate = currStartDate;
	}
	public void setCurrEndDate(Date currEndDate) {
		this.currEndDate = currEndDate;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setHopperTwoStart(int hopperTwoStart) {
		this.hopperTwoStart = hopperTwoStart;
	}
	public void setHopperTwoEnd(int hopperTwoEnd) {
		this.hopperTwoEnd = hopperTwoEnd;
	}
	public void setMinesId(int minesId) {
		this.minesId = minesId;
	}
	public void setGradeId(int gradeId) {
		this.gradeId = gradeId;
	}
	
}


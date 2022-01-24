package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("tpr_block_detail")
public class TPRBlockDetail {
	
	@KEY
	@GENRATED
	@PRIMARY_KEY
	@Column("tpr_block_id")
	private int tprBlockId = Misc.getUndefInt();
	
	@Column("vehicle_id")
	private int vehicleId = Misc.getUndefInt(); 
	
	@Column("tps_id")
	private int tpsId = Misc.getUndefInt();
	
	@Column("tpr_id")
	private int tprId = Misc.getUndefInt(); 
	
	@Column("user_block_step_id")
	private int userBlockStepId = Misc.getUndefInt();
	
	@Column("block_user_by")
	private int blockUserBy = Misc.getUndefInt();  
	
	@Column("system_block_step_id")
	private int systemBlockStepId = Misc.getUndefInt();  
		
	@Column("block_after_curr_tpr_id")
	private int blockAfterCurrTPRId = Misc.getUndefInt(); 
	
	@Column("block_till_date")                            
	private Date blockTillDate;  
	
	@Column("block_for_paper_after_curr_tpr_id")
	private int blockforPaperAfterCurrTPRId = Misc.getUndefInt(); 
	
	@Column("user_block_step_status")
	private int userBlockStepStatus = Misc.getUndefInt();  
	
	@Column("system_block_step_status")
	private int systemBlockStepStatus = Misc.getUndefInt();   
	
	@Column("block_after_curr_tpr_status")
	private int blockAfterCurrTPRStatus = Misc.getUndefInt(); 
	
	@Column("block_for_paper_after_curr_tpr_status")
	private int blockForPaperAfterCurrTPRStatus = Misc.getUndefInt(); 
	
	@Column("reason")
	private int reason = Misc.getUndefInt(); 
	
	@Column("action_taken")
	private int actionTaken = Misc.getUndefInt(); 
	
	@Column("resultant_status")
	private int resultantStatus = Misc.getUndefInt(); 
	
	@Column("supervisor_notes")                       
	private String supervisorNotes; 
	
	@Column("user_notes")                   
	private String userNotes; 
	
	@Column("created_on")                            
	private Date createdOn;
	
	@Column("created_by")                                 
	private int createdBy = Misc.getUndefInt();
	
	public int getTprBlockId() {
		return tprBlockId;
	}
	public void setTprBlockId(int tprBlockId) {
		this.tprBlockId = tprBlockId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getTpsId() {
		return tpsId;
	}
	public void setTpsId(int tpsId) {
		this.tpsId = tpsId;
	}
	public int getTprId() {
		return tprId;
	}
	public void setTprId(int tprId) {
		this.tprId = tprId;
	}
	public int getUserBlockStepId() {
		return userBlockStepId;
	}
	public void setUserBlockStepId(int userBlockStepId) {
		this.userBlockStepId = userBlockStepId;
	}
	public int getBlockUserBy() {
		return blockUserBy;
	}
	public void setBlockUserBy(int blockUserBy) {
		this.blockUserBy = blockUserBy;
	}
	public int getSystemBlockStepId() {
		return systemBlockStepId;
	}
	public void setSystemBlockStepId(int systemBlockStepId) {
		this.systemBlockStepId = systemBlockStepId;
	}
	public int getBlockAfterCurrTPRId() {
		return blockAfterCurrTPRId;
	}
	public void setBlockAfterCurrTPRId(int blockAfterCurrTPRId) {
		this.blockAfterCurrTPRId = blockAfterCurrTPRId;
	}
	public int getBlockforPaperAfterCurrTPRId() {
		return blockforPaperAfterCurrTPRId;
	}
	public void setBlockforPaperAfterCurrTPRId(int blockforPaperAfterCurrTPRId) {
		this.blockforPaperAfterCurrTPRId = blockforPaperAfterCurrTPRId;
	}
	public int getUserBlockStepStatus() {
		return userBlockStepStatus;
	}
	public void setUserBlockStepStatus(int userBlockStepStatus) {
		this.userBlockStepStatus = userBlockStepStatus;
	}
	public int getSystemBlockStepStatus() {
		return systemBlockStepStatus;
	}
	public void setSystemBlockStepStatus(int systemBlockStepStatus) {
		this.systemBlockStepStatus = systemBlockStepStatus;
	}
	public int getBlockAfterCurrTPRStatus() {
		return blockAfterCurrTPRStatus;
	}
	public void setBlockAfterCurrTPRStatus(int blockAfterCurrTPRStatus) {
		this.blockAfterCurrTPRStatus = blockAfterCurrTPRStatus;
	}
	public int getBlockForPaperAfterCurrTPRStatus() {
		return blockForPaperAfterCurrTPRStatus;
	}
	public void setBlockForPaperAfterCurrTPRStatus(
			int blockForPaperAfterCurrTPRStatus) {
		this.blockForPaperAfterCurrTPRStatus = blockForPaperAfterCurrTPRStatus;
	}
	public int getReason() {
		return reason;
	}
	public void setReason(int reason) {
		this.reason = reason;
	}
	public int getActionTaken() {
		return actionTaken;
	}
	public void setActionTaken(int actionTaken) {
		this.actionTaken = actionTaken;
	}
	public int getResultantStatus() {
		return resultantStatus;
	}
	public void setResultantStatus(int resultantStatus) {
		this.resultantStatus = resultantStatus;
	}
	public String getSupervisorNotes() {
		return supervisorNotes;
	}
	public void setSupervisorNotes(String supervisorNotes) {
		this.supervisorNotes = supervisorNotes;
	}
	public String getUserNotes() {
		return userNotes;
	}
	public void setUserNotes(String userNotes) {
		this.userNotes = userNotes;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public Date getBlockTillDate() {
		return blockTillDate;
	}
	public void setBlockTillDate(Date blockTillDate) {
		this.blockTillDate = blockTillDate;
	}
	
}

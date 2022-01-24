package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("vehicle_tpr_block_status")
public class VehicleTPRBlockStatus {
	

	@KEY
	@PRIMARY_KEY
	@Column("vehicle_id")
	private int vehicleId = Misc.getUndefInt();
	
	@Column("tpr_id")
	private int tprId = Misc.getUndefInt(); 
	
	@Column("tpr_block_id")
	private int tprBlockId = Misc.getUndefInt();
	
	@Column("is_block_by_prev_tpr")
	private int isBlockByPrevTPR = Misc.getUndefInt();
	
	@Column("block_after_tpr_id")
	private int blockAfterTPRId = Misc.getUndefInt(); 
	
	@Column("block_till_date")                            
	private Date blockTillDate;
	
	@Column("block_for_paper_after_tpr_id")
	private int blockForPaperAfterTPRId = Misc.getUndefInt();  
		
	@Column("next_step_allowed")
	private int nextStepAllowed = Misc.getUndefInt();  
	
	@Column("block_every_trip_status")
	private int blockEveryTripStatus = Misc.getUndefInt(); 
	
	@Column("open_step_id")
	private int openStepId = Misc.getUndefInt();
	
	@Column("updated_on")                            
	private Date updatedOn;
	
	@Column("updated_by")                                 
	private int updatedBy = Misc.getUndefInt();
	
	@Column("is_blacklisted")                            
	private int isBlackListed = Misc.getUndefInt();
	
	@Column("is_paper_ok")                            
	private int isPaperOk = Misc.getUndefInt();
	
	@Column("is_gps_ok")                            
	private int isGpsOk = Misc.getUndefInt();

	@Column("block_instruction_str")                            
	private String blockInstructionStr;
	
	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public int getTprId() {
		return tprId;
	}

	public void setTprId(int tprId) {
		this.tprId = tprId;
	}

	public int getTprBlockId() {
		return tprBlockId;
	}

	public void setTprBlockId(int tprBlockId) {
		this.tprBlockId = tprBlockId;
	}

	public int getIsBlockByPrevTPR() {
		return isBlockByPrevTPR;
	}

	public void setIsBlockByPrevTPR(int isBlockByPrevTPR) {
		this.isBlockByPrevTPR = isBlockByPrevTPR;
	}

	public int getBlockAfterTPRId() {
		return blockAfterTPRId;
	}

	public void setBlockAfterTPRId(int blockAfterTPRId) {
		this.blockAfterTPRId = blockAfterTPRId;
	}

	public int getBlockForPaperAfterTPRId() {
		return blockForPaperAfterTPRId;
	}

	public void setBlockForPaperAfterTPRId(int blockForPaperAfterTPRId) {
		this.blockForPaperAfterTPRId = blockForPaperAfterTPRId;
	}

	public int getNextStepAllowed() {
		return nextStepAllowed;
	}

	public void setNextStepAllowed(int nextStepAllowed) {
		this.nextStepAllowed = nextStepAllowed;
	}

	public int getBlockEveryTripStatus() {
		return blockEveryTripStatus;
	}

	public void setBlockEveryTripStatus(int blockEveryTripStatus) {
		this.blockEveryTripStatus = blockEveryTripStatus;
	}

	public int getOpenStepId() {
		return openStepId;
	}

	public void setOpenStepId(int openStepId) {
		this.openStepId = openStepId;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getBlockTillDate() {
		return blockTillDate;
	}

	public void setBlockTillDate(Date blockTillDate) {
		this.blockTillDate = blockTillDate;
	}

	public int getIsBlackListed() {
		return isBlackListed;
	}

	public void setIsBlackListed(int isBlackListed) {
		this.isBlackListed = isBlackListed;
	}

	public int getIsPaperOk() {
		return isPaperOk;
	}

	public void setIsPaperOk(int isPaperOk) {
		this.isPaperOk = isPaperOk;
	}

	public int getIsGpsOk() {
		return isGpsOk;
	}

	public void setIsGpsOk(int isGpsOk) {
		this.isGpsOk = isGpsOk;
	}

	public String getBlockInstructionStr() {
		return blockInstructionStr;
	}

	public void setBlockInstructionStr(String blockInstructionStr) {
		this.blockInstructionStr = blockInstructionStr;
	}
	
	
}

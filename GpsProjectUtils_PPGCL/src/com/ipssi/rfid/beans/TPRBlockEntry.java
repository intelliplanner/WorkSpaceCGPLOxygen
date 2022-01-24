package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("tpr_block_status")
public class TPRBlockEntry {
	public static final int CREATE_TYPE_AUTO = 0;
	public static final int CREATE_TYPE_MANUAL = 1;
	
	@KEY
	@PRIMARY_KEY
	@GENRATED
	@Column("id")
	int id = Misc.getUndefInt();
	
	
	@Column("tpr_id")
	int tprId = Misc.getUndefInt();
	
	@Column("workstation_type_id")
	int workstationTypeId = Misc.getUndefInt();
	
	@Column("skipped_step_id")
	int skippedStepId = Misc.getUndefInt();
	
	@Column("system_cause_id")
	int systemCauseId = Misc.getUndefInt();
	
	@Column("instruction_id")
	int instructionId = Misc.getUndefInt();
	
	@Column("override_workstation_type_id")
	int overrideWorkstationTypeId = Misc.getUndefInt();
	
	@Column("override_step_only")
	int overrideStepOnly = Misc.getUndefInt();
	
	@Column("override_tpr_only")
	int overrideTPROnly = Misc.getUndefInt();
	
	@Column("override_status")
	int overrideStatus = Misc.getUndefInt();
	
	@Column("override_notes")
	String overridNotes;
	
	@Column("override_date")
	Date overrideDate;
	
	@Column("created_on")
	Date createdOn;
	
	@Column("created_by")
	int createdBy = Misc.getUndefInt();
	
	@Column("updated_on")
	Date updatedOn;
	
	@Column("updated_by")
	int updatedBy = Misc.getUndefInt();
	
	@Column("type")
	int type = Misc.getUndefInt();

	@Column("status")
	int status = Misc.getUndefInt();
	
	@Column("create_type")
	int createType = Misc.getUndefInt();//auto = 0; manual = 1
	
	
	public int getInstructionId() {
		return instructionId;
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
	
	
	public void setInstructionId(int instructionId) {
		this.instructionId = instructionId;
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
	public int getTprId() {
		return tprId;
	}
	
	public int getSystemCauseId() {
		return systemCauseId;
	}
	public int getOverrideStepOnly() {
		return overrideStepOnly;
	}
	public int getOverrideTPROnly() {
		return overrideTPROnly;
	}
	public int getOverrideStatus() {
		return overrideStatus;
	}
	public String getOverridNotes() {
		return overridNotes;
	}
	public Date getOverrideDate() {
		return overrideDate;
	}
	public void setTprId(int tprId) {
		this.tprId = tprId;
	}
	
	public void setSystemCauseId(int systemCauseId) {
		this.systemCauseId = systemCauseId;
	}
	public void setOverrideStepOnly(int overridStepOnly) {
		this.overrideStepOnly = overridStepOnly;
	}
	public void setOverrideTPROnly(int overrideTPROnly) {
		this.overrideTPROnly = overrideTPROnly;
	}
	public void setOverrideStatus(int overrideStatus) {
		this.overrideStatus = overrideStatus;
	}
	public void setOverridNotes(String overridNotes) {
		this.overridNotes = overridNotes;
	}
	public void setOverrideDate(Date overrideDate) {
		this.overrideDate = overrideDate;
	}
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getWorkstationTypeId() {
		return workstationTypeId;
	}

	public int getOverrideWorkstationTypeId() {
		return overrideWorkstationTypeId;
	}

	public void setWorkstationTypeId(int workstationTypeId) {
		this.workstationTypeId = workstationTypeId;
	}

	public void setOverrideWorkstationTypeId(int overrideWorkstationTypeId) {
		this.overrideWorkstationTypeId = overrideWorkstationTypeId;
	}

	public int getId() {
		return id;
	}

	public int getStatus() {
		return status;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCreateType() {
		return createType;
	}

	public void setCreateType(int createType) {
		this.createType = createType;
	}

	public int getSkippedStepId() {
		return skippedStepId;
	}

	public void setSkippedStepId(int skippedStepId) {
		this.skippedStepId = skippedStepId;
	}

}

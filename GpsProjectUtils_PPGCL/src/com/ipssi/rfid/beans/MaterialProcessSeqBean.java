package com.ipssi.rfid.beans;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
@Table("material_process_seq")
public class MaterialProcessSeqBean {

	@Column("material_type")
	int materialType = Misc.getUndefInt();
	@Column("workstation_type")
	int workstationType = Misc.getUndefInt();
	@Column("seq")
	int seq = Misc.getUndefInt();
	@Column("status")
	int status = Misc.getUndefInt();
	public int getMaterialType() {
		return materialType;
	}
	public int getWorkstationType() {
		return workstationType;
	}
	public int getSeq() {
		return seq;
	}
	public int getStatus() {
		return status;
	}
	public void setMaterialType(int materialType) {
		this.materialType = materialType;
	}
	public void setWorkstationType(int workstationType) {
		this.workstationType = workstationType;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public void setStatus(int status) {
		this.status = status;
	}

}

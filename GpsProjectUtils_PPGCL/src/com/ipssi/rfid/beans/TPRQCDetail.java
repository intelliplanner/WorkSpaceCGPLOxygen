package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("tpr_qc_detail")
public class TPRQCDetail {

    @Table.KEY
    @PRIMARY_KEY
    @Table.GENRATED
    @Table.Column("qc_id")
    private int id;
    @Table.Column("tps_id")
    private int tpsId;
    @Table.Column("tpr_id")
    private int tprId;
    @Table.Column("informed_iia")
    private int informedIIA;
    @Table.Column("iia_receipt_no")
    private String iiaReceipt_no;
    @Table.Column("qc_status")
    private int status;
    @Table.Column("qc_instruction")
    private String instruction;
    @Table.Column("qc_remark")
    private String remark;
    @Table.Column("created_on")
    private Date createdOn;
    @Table.Column("updated_on")
    private Date updatedOn;
    @Table.Column("user_by")
    private int updatedBy;

    public int getId() {
        return id;
    }

    public int getTpsId() {
        return tpsId;
    }

    public int getTprId() {
        return tprId;
    }

    public int getInformedIIA() {
        return informedIIA;
    }

    public String getIiaReceipt_no() {
        return iiaReceipt_no;
    }

    public int getStatus() {
        return status;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getRemark() {
        return remark;
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

    public void setTpsId(int tpsId) {
        this.tpsId = tpsId;
    }

    public void setTprId(int tprId) {
        this.tprId = tprId;
    }

    public void setInformedIIA(int informedIIA) {
        this.informedIIA = informedIIA;
    }

    public void setIiaReceipt_no(String iiaReceipt_no) {
        this.iiaReceipt_no = iiaReceipt_no;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

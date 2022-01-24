package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("tps_question_detail")
public class TPSQuestionDetail {
	
	public static final int YES = 1;
	public static final int NO = 2;
	public static final int NC = 3;
	public static final int NOSELECTED = 4;
    
	@KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("tps_question_id")
    private int tpsQuestionId = Misc.getUndefInt();

    public TPSQuestionDetail() {
	}
    public TPSQuestionDetail(int questionId,int answerId){
    	this.questionId = questionId;
    	this.answerId = answerId;
    }
    public TPSQuestionDetail(int tprId,int questionId,int answerId){
    	this.tprId = tprId;
    	this.questionId = questionId;
    	this.answerId = answerId;
    }
    public int getTpsQuestionId() {
        return tpsQuestionId;
    }

    public void setTpsQuestionId(int tpsQuestionId) {
        this.tpsQuestionId = tpsQuestionId;
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

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(int actionRequired) {
        this.actionRequired = actionRequired;
    }

    public int getBlockNextStep() {
        return blockNextStep;
    }

    public void setBlockNextStep(int blockNextStep) {
        this.blockNextStep = blockNextStep;
    }

    public int getBlockTrip() {
        return blockTrip;
    }

    public void setBlockTrip(int blockTrip) {
        this.blockTrip = blockTrip;
    }

    public int getBlockVehicle() {
        return blockVehicle;
    }

    public void setBlockVehicle(int blockVehicle) {
        this.blockVehicle = blockVehicle;
    }

    public int getTprBlockId() {
        return tprBlockId;
    }

    public void setTprBlockId(int tprBlockId) {
        this.tprBlockId = tprBlockId;
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
    @Column("tps_id")
    private int tpsId = Misc.getUndefInt();
    @Column("tpr_id")
    private int tprId = Misc.getUndefInt();
    @Column("question_id")
    private int questionId = Misc.getUndefInt();
    @Column("answer_id")
    private int answerId = Misc.getUndefInt();
    @Column("action_required")
    private int actionRequired = Misc.getUndefInt();
    @Column("block_next_step")
    private int blockNextStep = Misc.getUndefInt();
    @Column("block_trip")
    private int blockTrip = Misc.getUndefInt();
    @Column("block_vehicle")
    private int blockVehicle = Misc.getUndefInt();
    @Column("tpr_block_id")
    private int tprBlockId = Misc.getUndefInt();
    @Column("updated_on")
    private Date updatedOn;
    @Column("user_by")
    private int updatedBy = Misc.getUndefInt();
}

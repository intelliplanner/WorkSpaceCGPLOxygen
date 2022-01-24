package com.ipssi.rfid.beans;

import java.util.Date;

public class TPRVehicleBlockStatus {

	private int id;                       
	private int vehicleId;                          
	private int tprId;                              
	private int tprBlockId;                        
	private int blockForUnfulfilledQuestion;      
	private int blockByPrevStep;                  
	private int blockByWebUser;                   
	private int blockForStepJump;                 
	private Date createdOn;                         
	private Date updatedOn;  
	private int updatedBy;
	public int getId() {
		return id;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public int getTprId() {
		return tprId;
	}
	public int getTprBlockId() {
		return tprBlockId;
	}
	public int getBlockForUnfulfilledQuestion() {
		return blockForUnfulfilledQuestion;
	}
	public int getBlockByPrevStep() {
		return blockByPrevStep;
	}
	public int getBlockByWebUser() {
		return blockByWebUser;
	}
	public int getBlockForStepJump() {
		return blockForStepJump;
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
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public void setTprId(int tprId) {
		this.tprId = tprId;
	}
	public void setTprBlockId(int tprBlockId) {
		this.tprBlockId = tprBlockId;
	}
	public void setBlockForUnfulfilledQuestion(int blockForUnfulfilledQuestion) {
		this.blockForUnfulfilledQuestion = blockForUnfulfilledQuestion;
	}
	public void setBlockByPrevStep(int blockByPrevStep) {
		this.blockByPrevStep = blockByPrevStep;
	}
	public void setBlockByWebUser(int blockByWebUser) {
		this.blockByWebUser = blockByWebUser;
	}
	public void setBlockForStepJump(int blockForStepJump) {
		this.blockForStepJump = blockForStepJump;
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

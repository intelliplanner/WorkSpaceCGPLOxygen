package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Type;

public class TPRWeighmentRecord {
	public static enum StepType{
		loadGateIn,
		loadWBIn,
		loadRfWBIn,
		loadYardIn,
		loadYardOut,
		loadWBOut,
		loadRfWBOut,
		loadGateOut,
		unloadGateIn,
		unloadWBIn,
		unloadYardIn,
		unloadYardOut,
		unloadWBOut,
		unloadGateOut
	}
	private double weight = Misc.getUndefDouble();
	private String station;
	private Date inTime;
	private Date outTime;
	private StepType stepType;
	public TPRWeighmentRecord() {
		super();
	}
	
	

	public TPRWeighmentRecord(double weight, String station, Date inTime, Date outTime, StepType stepType) {
		super();
		this.weight = weight;
		this.station = station;
		this.inTime = inTime;
		this.outTime = outTime;
		this.stepType = stepType;
	}



	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getStation() {
		return station;
	}
	public void setStation(String station) {
		this.station = station;
	}
	public Date getInTime() {
		return inTime;
	}
	public void setInTime(Date inTime) {
		this.inTime = inTime;
	}
	public Date getOutTime() {
		return outTime;
	}
	public void setOutTime(Date outTime) {
		this.outTime = outTime;
	}
	public StepType getStepType() {
		return stepType;
	}
	public void setStepType(StepType stepType) {
		this.stepType = stepType;
	}
	public Date getTime() {
		Date date = outTime;
		if(this.stepType == StepType.unloadWBOut){
			date = outTime != null ? outTime : inTime;
		}
		return date;
	}
	public boolean isNull(){
		switch(this.stepType){
			case loadWBIn :
			case loadRfWBIn :
			case loadWBOut :
			case loadRfWBOut :
			case unloadWBIn : 
				return Misc.isUndef(this.weight) || outTime == null;
			case unloadWBOut : 
				return Misc.isUndef(this.weight) || (inTime == null && outTime == null);

			case loadGateIn : 
			case loadYardIn : 
			case loadYardOut : 
			case loadGateOut : 
			case unloadGateIn : 
			case unloadYardIn : 
			case unloadYardOut : 
			case unloadGateOut : return outTime == null;
			default : return true;
		}
	}
	
	public static StepType getStepType(int workStationType){

		if(Misc.isUndef(workStationType) )
			return null;
		switch (workStationType) {
		case Type.WorkStationType.GATE_IN_TYPE:
		case Type.WorkStationType.SECL_UNLOAD_GATE_IN:
			return StepType.unloadGateIn;
			
		case Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE:
		case Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS:
			return StepType.unloadWBIn;
			
		case Type.WorkStationType.SECL_UNLOAD_INT_YARD_IN:
		case Type.WorkStationType.YARD_IN_TYPE:
			return StepType.unloadYardIn;
			
		case Type.WorkStationType.SECL_UNLOAD_INT_YARD_OUT:
		case Type.WorkStationType.YARD_OUT_TYPE:
			return StepType.unloadYardOut;
			
		case Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE:
		case Type.WorkStationType.SECL_UNLOAD_INT_WB_TARE:
			return StepType.unloadWBOut;
			
		case Type.WorkStationType.GATE_OUT_TYPE:
		case Type.WorkStationType.SECL_UNLOAD_GATE_OUT:
			return StepType.unloadGateOut;
			
		case Type.WorkStationType.FLY_ASH_IN_TYPE:
		case Type.WorkStationType.SECL_LOAD_GATE_IN:
			return StepType.loadGateIn;
			
		case Type.WorkStationType.FLY_ASH_TARE_WT_TYPE:
		case Type.WorkStationType.STONE_TARE_WT_TYPE:
		case Type.WorkStationType.FIRST_WEIGHTMENT_TYPE:
		case Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE:
		case Type.WorkStationType.SECL_LOAD_INT_WB_TARE:
		case Type.WorkStationType.SECL_LOAD_WASHERY_TARE:
		case Type.WorkStationType.SECL_OTHER_FIRST:
			return StepType.loadWBIn;
			
		case Type.WorkStationType.SECL_LOAD_INT_YARD_IN:
			return StepType.loadYardIn;
		
		case Type.WorkStationType.SECL_LOAD_INT_YARD_OUT:
			return StepType.loadYardOut;
		
		case Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE:
		case Type.WorkStationType.STONE_GROSS_WT_TYPE:
		case Type.WorkStationType.SECOND_WEIGHTMENT_TYPE:
		case Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS:
		case Type.WorkStationType.SECL_LOAD_INT_WB_GROSS:
		case Type.WorkStationType.SECL_LOAD_WASHERY_GROSS:
		case Type.WorkStationType.SECL_OTHER_SECOND:
			return StepType.loadWBOut;
		
		case Type.WorkStationType.SECL_LOAD_GATE_OUT:
			return StepType.loadGateOut;
		
		default:
			return null;
		}
	
	}
}

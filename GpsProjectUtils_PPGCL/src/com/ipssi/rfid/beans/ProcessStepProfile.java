package com.ipssi.rfid.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Type;

public class ProcessStepProfile {
	private static final ConcurrentHashMap<Integer, ArrayList<Integer>> processList = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	static{
		processList.put(Type.TPRMATERIAL.COAL, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.GATE_IN_TYPE,
						Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE
						)));
		processList.put(Type.TPRMATERIAL.STONE, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.STONE_TARE_WT_TYPE,
						Type.WorkStationType.STONE_GROSS_WT_TYPE
						)));	
		processList.put(Type.TPRMATERIAL.FLYASH, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.FLY_ASH_IN_TYPE,
						Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE
						)));	
		processList.put(Type.TPRMATERIAL.OTHERS, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.FIRST_WEIGHTMENT_TYPE,
						Type.WorkStationType.SECOND_WEIGHTMENT_TYPE
						)));	
		processList.put(Type.TPRMATERIAL.COAL_INTERNAL, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.SECL_LOAD_GATE_IN,
						Type.WorkStationType.SECL_LOAD_INT_WB_TARE,
						Type.WorkStationType.SECL_LOAD_INT_YARD_IN,
						Type.WorkStationType.SECL_LOAD_INT_YARD_OUT,
						Type.WorkStationType.SECL_LOAD_INT_WB_GROSS,
						Type.WorkStationType.SECL_LOAD_GATE_OUT,
						Type.WorkStationType.SECL_UNLOAD_GATE_IN,
						Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS,
						Type.WorkStationType.SECL_UNLOAD_INT_YARD_IN,
						Type.WorkStationType.SECL_UNLOAD_INT_YARD_OUT,
						Type.WorkStationType.SECL_UNLOAD_INT_WB_TARE,
						Type.WorkStationType.SECL_UNLOAD_GATE_OUT
						)));	
		processList.put(Type.TPRMATERIAL.COAL_ROAD, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.SECL_LOAD_GATE_IN,
						Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE,
						Type.WorkStationType.SECL_LOAD_INT_YARD_IN,
						Type.WorkStationType.SECL_LOAD_INT_YARD_OUT,
						Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS,
						Type.WorkStationType.SECL_LOAD_GATE_OUT
						)));
		processList.put(Type.TPRMATERIAL.COAL_WASHERY, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.SECL_LOAD_GATE_IN,
						Type.WorkStationType.SECL_LOAD_WASHERY_TARE,
						Type.WorkStationType.SECL_LOAD_INT_YARD_IN,
						Type.WorkStationType.SECL_LOAD_INT_YARD_OUT,
						Type.WorkStationType.SECL_LOAD_WASHERY_GROSS,
						Type.WorkStationType.SECL_LOAD_GATE_OUT
						)));	
		processList.put(Type.TPRMATERIAL.COAL_WASHERY, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.SECL_LOAD_GATE_IN,
						Type.WorkStationType.SECL_LOAD_WASHERY_TARE,
						Type.WorkStationType.SECL_LOAD_INT_YARD_IN,
						Type.WorkStationType.SECL_LOAD_INT_YARD_OUT,
						Type.WorkStationType.SECL_LOAD_WASHERY_GROSS,
						Type.WorkStationType.SECL_LOAD_GATE_OUT
						)));
		processList.put(Type.TPRMATERIAL.COAL_OTHER, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.SECL_LOAD_GATE_IN,
						Type.WorkStationType.SECL_OTHER_FIRST,
						Type.WorkStationType.SECL_LOAD_INT_YARD_IN,
						Type.WorkStationType.SECL_LOAD_INT_YARD_OUT,
						Type.WorkStationType.SECL_OTHER_SECOND,
						Type.WorkStationType.SECL_UNLOAD_GATE_IN
						)));
		processList.put(Type.TPRMATERIAL.FLYASH_CGPL, 
				new ArrayList<Integer>(Arrays.asList(
						Type.WorkStationType.CGPL_LOAD_GATE_IN,
						Type.WorkStationType.CGPL_LOAD_WB_OUT,
						Type.WorkStationType.CGPL_LOAD_WB_IN,
						Type.WorkStationType.CGPL_LOAD_GATE_OUT
						)));
	}
	public static ProcessStepProfile getStandardProcessStepByMaterialCat(int materialCat){
		return new ProcessStepProfile(processList.get(materialCat));
	}
	ArrayList<Integer> processStep = new ArrayList<Integer>();
	public ArrayList<Integer> getProcessStep() {
		return processStep;
	}
	public void setProcessStep(ArrayList<Integer> processStep) {
		this.processStep = processStep;
	}
	public ProcessStepProfile(ArrayList<Integer> processStep) {
		super();
		this.processStep = processStep;
	}
	public int getNextStep(int currStep){
		if(Misc.isUndef(currStep))
			return Misc.getUndefInt();
		int currStepIndex = Misc.getUndefInt();
		for (int i = 0,is=processStep == null ? 0 : processStep.size(); i < is; i++) {
			if(currStep == processStep.get(i)){
				currStepIndex = i+1;
				break;
			}
		}
		return Misc.isUndef(currStepIndex) || currStepIndex >= processStep.size() ? Misc.getUndefInt() : processStep.get(currStepIndex);
	}
	
}

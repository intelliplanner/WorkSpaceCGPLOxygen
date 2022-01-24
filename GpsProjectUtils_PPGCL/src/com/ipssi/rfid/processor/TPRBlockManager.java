package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.constant.Status.TPRQuestion;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;

public class TPRBlockManager {
	public static final int OVERRIDE = 2;
    public static final int BLOCKED = 3;
    public static final int NOT_BLOCKED = 1;
	private static int TRUE = 1;
	private static int FALSE = 2;
	public static final int BLOCK_STEP = 0;
	public static final int BLOCK_TRIP = 1;
	public static final int BLOCK_QUESTION = 2;
	private int vehicleId = Misc.getUndefInt();
	private int tprId = Misc.getUndefInt();
	private int portNodeId = Misc.getUndefInt(); 
	private ArrayList<BlockingInstruction> instructionList = null;
	private ArrayList<TPSQuestionDetail> questionList = null;
	private ArrayList<TPRBlockEntry> tprBlockEntries = null;
	private TPRecord tpr = null;
	private int workstationTypeId = Misc.getUndefInt();
	private boolean isWeb = false;
	private int materialCat = Type.TPRMATERIAL.COAL;
	
	
	private static ArrayList<Pair<Integer,Integer>> blockQuestionMap = new ArrayList<Pair<Integer,Integer>>();
	
	static{
		
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_QC, TPRQuestion.qcDone));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DRUNCK, TPRQuestion.breathLyzerOk));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_GPS, TPRQuestion.gpsOk));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_TAG_NOT_READ, TPRQuestion.isTagRead));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_VERIFIED, TPRQuestion.isFingerVerified));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_CAPTURED, TPRQuestion.isFingerCaptured));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_VEHICLE_NOT_EXIST, TPRQuestion.isVehicleExist));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_CHALLAN_NOT_EXIST, TPRQuestion.isChallanExist));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_FITNESS_EXPIRED, TPRQuestion.isFitnessOk));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_ROAD_PERMIT_EXPIRED, TPRQuestion.isRoadPermitOk));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_INSURANCE_EXPIRED, TPRQuestion.isInsuranceOk));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_POLUTION_EXPIRED, TPRQuestion.isPolutionOk));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR, TPRQuestion.haveYouInformedGpsVendor));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DRIVER_NOT_EXIST, TPRQuestion.isDriverExist));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_EXIST, TPRQuestion.isFingerExist));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DRIVER_BLACKLISTED, TPRQuestion.isDriverBlacklisted));
		
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_ACCESS_DENIED, TPRQuestion.isAccessAllowed));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_NO_TARE_ALLOWED, TPRQuestion.isNoTareAllowed));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DO_EXPIARED, TPRQuestion.isDoValid));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_CARD_NOT_RETURNED, TPRQuestion.isTempCardReturned));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DO_QUOTA, TPRQuestion.isQuotaRemaing));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_EXIT_WITHOUT_WEIGHMENT, TPRQuestion.isWeighmentCompleted));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_TRIP_BEFORE_GAP, TPRQuestion.isTripGapProper));
		
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_UNSAFE_DRIVING, TPRQuestion.isUnSafeDriving));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY, TPRQuestion.isPlantPropertyDamaged));
		blockQuestionMap.add(new Pair<Integer, Integer>(Type.BlockingInstruction.BLOCK_DUETO_DRIVER_BEHAVIOUR, TPRQuestion.isMisBehaviorByDriver));
	}
	
	public int getBlockInstructionType(int questionId){
		if(Misc.isUndef(questionId))
			return Misc.getUndefInt();
		for(int i=0,is=blockQuestionMap == null ? 0 : blockQuestionMap.size();i<is;i++){
			if(blockQuestionMap.get(i).second == questionId)
				return blockQuestionMap.get(i).first;
		}
		return Misc.getUndefInt();
	}
	public int getQuestionId(int blockInstructionType){
		if(Misc.isUndef(blockInstructionType))
			return Misc.getUndefInt();
		for(int i=0,is=blockQuestionMap == null ? 0 : blockQuestionMap.size();i<is;i++){
			if(blockQuestionMap.get(i).first == blockInstructionType)
				return blockQuestionMap.get(i).second;
		}
		return Misc.getUndefInt();
	}
	
    private TPRBlockManager(int vehicleId,int tprId,int portNodeId, int workstationTypeId,int materialCat, boolean isWeb){
    	this.vehicleId = vehicleId;
    	this.tprId = tprId;
    	this.portNodeId = portNodeId;
    	this.workstationTypeId = workstationTypeId;
    	this.isWeb = isWeb;
    	this.materialCat  = materialCat;
    }
    public static TPRBlockManager getTprBlockStatus(Connection conn, int portNodeId, TPRecord tpr, int workstationTypeId, int materialCat){
    	return getTprBlockStatus(conn, portNodeId, tpr, workstationTypeId,materialCat, false);
    }
    public static TPRBlockManager getTprBlockStatus(Connection conn, int portNodeId, TPRecord tpr, int workstationTypeId, int materialCat, boolean isWeb){
    	TPRBlockManager retval = null;
    	try{
    		retval = new TPRBlockManager(tpr == null ? Misc.getUndefInt() : tpr.getVehicleId(), tpr == null ? Misc.getUndefInt() : tpr.getTprId(), portNodeId,workstationTypeId, materialCat,isWeb);
    		retval.tpr = tpr;
    		retval.setInstructionList(conn);
    		retval.setTprQuestionList(conn);
    		retval.calculateBlocking(conn);
    		
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
		return retval;
    }
    public void setTprBlockStatus(Connection conn, int tprId,int userId) throws SQLException{
    	try{
    		//update tprBlockEntries
    		for(int i=0,is=tprBlockEntries==null? 0 : tprBlockEntries.size();i<is;i++){
    			TPRBlockEntry tprBlockEntry = tprBlockEntries.get(i);
    			Date updatedOn = new Date();
    			tprBlockEntry.setUpdatedOn(updatedOn);
    			tprBlockEntry.setUpdatedBy(userId);
    			tprBlockEntry.setTprId(tprId);
    			if(Misc.isUndef(tprBlockEntry.getId())){
    				tprBlockEntry.setCreatedBy(userId);
    				tprBlockEntry.setCreatedOn(updatedOn);
    				RFIDMasterDao.insert(conn, tprBlockEntry,false);
    			}else{
    				RFIDMasterDao.update(conn, tprBlockEntry,false);
    			}
    		}
    		//update manualInstructions
    		for(int i=0,is=instructionList==null? 0 : instructionList.size();i<is;i++){
    			BlockingInstruction instruction = instructionList.get(i);
    			if(Misc.isUndef(instruction.getId()) || !Misc.isUndef(instruction.getPortNodeId()))
    				continue;
    			RFIDMasterDao.update(conn, instruction,false);
    		}
    		
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	
    }
    public void setTprBlockStatus(Connection conn, int tprId,int userId,int blockStatus) throws SQLException{
    	try{
    		//update tprBlockEntries
    		for(int i=0,is=tprBlockEntries==null? 0 : tprBlockEntries.size();i<is;i++){
    			TPRBlockEntry tprBlockEntry = tprBlockEntries.get(i);
    			Date updatedOn = new Date();
    			tprBlockEntry.setUpdatedOn(updatedOn);
    			tprBlockEntry.setUpdatedBy(userId);
    			tprBlockEntry.setTprId(tprId);
    			tprBlockEntry.setStatus(blockStatus);
    			if(Misc.isUndef(tprBlockEntry.getId())){
    				tprBlockEntry.setCreatedBy(userId);
    				tprBlockEntry.setCreatedOn(updatedOn);
    				RFIDMasterDao.insert(conn, tprBlockEntry,false);
    			}else{
    				RFIDMasterDao.update(conn, tprBlockEntry,false);
    			}
    		}
    		//update manualInstructions
    		/* for(int i=0,is=instructionList==null? 0 : instructionList.size();i<is;i++){
    			BlockingInstruction instruction = instructionList.get(i);
    			if(Misc.isUndef(instruction.getId()) || !Misc.isUndef(instruction.getPortNodeId()))
    				continue;
    			RFIDMasterDao.update(conn, instruction,false);
    		}
    		*/
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }

    
	private void setInstructionList(Connection conn){
		if(Misc.isUndef(portNodeId))
			return;
		try{
			BlockingInstruction instruction = new BlockingInstruction();
			instruction.setPortNodeId(portNodeId);
			instruction.setVehicleId(Misc.getUndefInt());
			instruction.setStatus(1);
			instruction.setMaterialCat(materialCat);
			this.instructionList = (ArrayList<BlockingInstruction>) RFIDMasterDao.getList(conn, instruction,null);
			instruction.setVehicleId(vehicleId);
			instruction.setPortNodeId(Misc.getUndefInt());
			instruction.setMaterialCat(Misc.getUndefInt());
			ArrayList<BlockingInstruction> webInstructionList = (ArrayList<BlockingInstruction>) RFIDMasterDao.getList(conn, instruction, null);
			for(int i=0,is=webInstructionList == null ? 0 : webInstructionList.size();i<is;i++){
				if(this.instructionList == null){
					this.instructionList = webInstructionList;
				}else{
					this.instructionList.add(webInstructionList.get(i));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public BlockingInstruction getInstructionByType(int instructionType){
		if(Misc.isUndef(instructionType))
			return null;
		for(int i=0,is=this.instructionList==null? 0 : this.instructionList.size();i<is;i++){
			if(this.instructionList.get(i).getType() == instructionType)
				return this.instructionList.get(i);
		}
		return null;
	}
	public BlockingInstruction getInstructionById(int instructionId){
		if(Misc.isUndef(instructionId))
			return null;
		for(int i=0,is=this.instructionList==null? 0 : this.instructionList.size();i<is;i++){
			if(this.instructionList.get(i).getId() == instructionId)
				return this.instructionList.get(i);
		}
		return null;
	}
	private TPRBlockEntry requireBlock(BlockingInstruction bInstruction){
		TPRBlockEntry retval = null;
		int blockType = BLOCK_STEP;
		int blockCreateType = TPRBlockEntry.CREATE_TYPE_AUTO;
		boolean addNew = false;
		int skippedWorkstationId = Misc.getUndefInt();
		try{
			if(Misc.isUndef(vehicleId) || bInstruction == null )//|| Misc.isUndef(tprId))
				return retval;
			if(Type.BlockingInstruction.BLOCK_DUETO_NEXT_STEP == bInstruction.getType()){
				blockType = BLOCK_STEP;
				blockCreateType = TPRBlockEntry.CREATE_TYPE_MANUAL;
				addNew = true;
			}else if(Type.BlockingInstruction.BLOCK_DUETO_BLACKLIST == bInstruction.getType() ){
				long blockFrom = bInstruction.getBlockFrom() != null ? bInstruction.getBlockFrom().getTime() : Misc.getUndefInt();
				long blockTo = bInstruction.getBlockTo() != null ? bInstruction.getBlockTo().getTime() : Misc.getUndefInt();
				blockType = BLOCK_TRIP;
				blockCreateType = TPRBlockEntry.CREATE_TYPE_MANUAL;
				addNew = !Misc.isUndef(blockFrom) && (tpr.getTprCreateDate().getTime() > blockFrom  && (Misc.isUndef(blockTo) || tpr.getTprCreateDate().getTime() < blockTo));
			}else if(
					(Type.BlockingInstruction.BLOCK_DUETO_STEP_JUMP == bInstruction.getType() 
					&& 
					(tpr.getNextStepType() == Type.WorkStationType.REGISTRATION && workstationTypeId == Type.WorkStationType.FLY_ASH_IN_TYPE ? false :  (tpr.getNextStepType() < workstationTypeId))
					)
					){
				blockType = BLOCK_STEP;
				skippedWorkstationId = tpr.getNextStepType();
				addNew = true;	
			}else if(Type.BlockingInstruction.BLOCK_DUETO_MULTIPLE_TPR == bInstruction.getType() ){
				blockType = BLOCK_STEP;
				boolean isMultipleTPR = tpr.getIsMultipleOpenTPR() == 1;
				if(workstationTypeId == Type.WorkStationType.GATE_IN_TYPE){
					addNew = isMultipleTPR && tpr.getLatestUnloadGateInExit() != null;
				}else if(workstationTypeId == Type.WorkStationType.FLY_ASH_IN_TYPE){
					addNew = isMultipleTPR && tpr.getLatestLoadGateInExit() != null;
				}else{
					addNew = isMultipleTPR ;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_QC == bInstruction.getType() ){
				int ans = tpr.getMarkForQC() == 1 ? getTPRQuestionResult(tpr.getTprId(), bInstruction.getType()) : Misc.getUndefInt() ;//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.qcDone);
				//if(!Misc.isUndef(ans) && ans != 1 ){
				if(ans == TPSQuestionDetail.NO){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_DRUNCK == bInstruction.getType() ){
				int ans = getTPRQuestionResult(tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.breathLyzerOk);
				//if(!Misc.isUndef(ans) && ans != 1){
				if(ans == TPSQuestionDetail.NO){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_GPS == bInstruction.getType() ){
				int ans = tpr.getMarkForGPS() == 1 ? getTPRQuestionResult(tpr.getTprId(), bInstruction.getType()) : Misc.getUndefInt();//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.gpsOk);
				//if(!Misc.isUndef(ans) && ans != 1){
				if(ans == TPSQuestionDetail.NO){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_DOC_INCOMPLETE == bInstruction.getType()){
				/*int ans = TPRUtils.isVehicleDocumentComplete(conn, tpr.getVehicleId(), (15*24*60*60*1000));
				if(!Misc.isUndef(ans) && ans != 1){
					blockType = BLOCK_TRIP;
					addNew = true;
				}*/
				
				
			}
			else if(Type.BlockingInstruction.BLOCK_DUETO_UNSAFE_DRIVING == bInstruction.getType() ){
				int ans = getTPRQuestionResult(tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.breathLyzerOk);
				//if(!Misc.isUndef(ans) && ans != 1){
				if(ans == TPSQuestionDetail.YES){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}
			else if(Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY == bInstruction.getType() ){
				int ans = getTPRQuestionResult(tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.breathLyzerOk);
				if(ans == TPSQuestionDetail.YES){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}
			else if(Type.BlockingInstruction.BLOCK_DUETO_DRIVER_BEHAVIOUR == bInstruction.getType() ){
				int ans = getTPRQuestionResult(tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.breathLyzerOk);
				//if(!Misc.isUndef(ans) && ans != 1){
				if(ans == TPSQuestionDetail.YES){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}
//			else if(bInstruction.getType() > 4000){
//				int ans = getTPRQuestionResult(tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.gpsOk);
//				//if(!Misc.isUndef(ans) && ans != 1){
//				if(ans == TPSQuestionDetail.NO){
//					blockType = BLOCK_TRIP;
//					addNew = true;
//				}
//			}
			
			
			if(addNew)
				retval = createTPRBlockingEntry(vehicleId, tpr.getTprId(), workstationTypeId, bInstruction.getId(), blockType, blockCreateType, skippedWorkstationId);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	public boolean isBlockForInstruction(int instructionType){
		BlockingInstruction bIns = getInstructionByType(instructionType);
		if(bIns == null || Misc.isUndef(bIns.getId()))
			return false;
		TPRBlockEntry tprBlockEntry = requireBlock(bIns);
		return (tprBlockEntry != null && tprBlockEntry.getWorkstationTypeId() > tprBlockEntry.getOverrideWorkstationTypeId()); 
	}
	
	public ArrayList<TPRBlockEntry> getTPRBlockEntryList(Connection conn, int tprId){
		if(Misc.isUndef(tprId))
			return null;
		ArrayList<TPRBlockEntry> retval = null;
		try{
			TPRBlockEntry blockEntry = new TPRBlockEntry();
			blockEntry.setTprId(tprId);
			blockEntry.setStatus(1);
			retval = (ArrayList<TPRBlockEntry>) RFIDMasterDao.getList(conn, blockEntry,null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	public void calculateBlocking(Connection conn){
		ArrayList<Integer> ignoreInstruction = new ArrayList<Integer>();
		int tprId = tpr == null ?  Misc.getUndefInt() : tpr.getTprId();
		if(Misc.isUndef(vehicleId) || Misc.isUndef(tprId))
			return ;
		try{
			System.out.println("getVehicleBlockingEntries:"+vehicleId+","+Type.WorkStationType.getString(workstationTypeId));
			if(tpr != null && !Misc.isUndef(tpr.getTprId())	){
				if(Misc.isUndef(workstationTypeId)){
					TPStep currStep = TPRUtils.getCurrentStep(conn, tprId);
					if(currStep != null)
						workstationTypeId = currStep.getWorkStationType();
				}
				this.tprBlockEntries = getTPRBlockEntryList(conn, tprId);
				TPRBlockEntry tprBlockEntry = null;
				for(int i=0, is=this.tprBlockEntries==null? 0 : this.tprBlockEntries.size(); i<is; i++){
					tprBlockEntry = this.tprBlockEntries.get(i);
					boolean ignore = false;
					tprBlockEntry = cleanBlockEntry(conn, tprBlockEntry, workstationTypeId);
					if(tprBlockEntry != null){
						if(tprBlockEntry.getType() == BLOCK_STEP ){
							if(tprBlockEntry.getWorkstationTypeId() == workstationTypeId 
									//|| tprBlockEntry.getSystemCauseId() > 3000
									){
								ignore = true;
							}
						}else{
							ignore = true;
						} 
						if(ignore){
							BlockingInstruction ignIns = getInstructionById(tprBlockEntry.getInstructionId());
							if(ignIns != null && !Misc.isUndef(ignIns.getType()))
								ignoreInstruction.add(ignIns.getType());
							
						}
					}
				}
			}
			if(tprBlockEntries == null)
				tprBlockEntries = new ArrayList<TPRBlockEntry>();
			if(!isWeb){
				//create for autoInstruction
				BlockingInstruction bAutoIns = null;
				TPRBlockEntry autoBlockEntry = null;
				for(int j=0,js=instructionList == null ? 0 : instructionList.size() ;j<js;j++){
					boolean ignoreAuto = false;
					bAutoIns = (BlockingInstruction)instructionList.get(j);
					for(int k=0,ks=ignoreInstruction == null ? 0 : ignoreInstruction.size() ;k<ks;k++){
						if(ignoreInstruction.get(k) == bAutoIns.getType()){//in case of system based blocking instruction_id field is null
							ignoreAuto = true;
							break;
						}
					}
					if(ignoreAuto)
						continue;
					autoBlockEntry = requireBlock(bAutoIns);
					if(autoBlockEntry != null)
						tprBlockEntries.add(autoBlockEntry);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

    private boolean deleteInstruction(int instructionId){
    	boolean retval = false;
    	BlockingInstruction bInstruction = null;
    	try{
    		System.out.println("deleteInstruction:"+instructionId);
    		for(int i=0,is=this.instructionList == null? 0 : this.instructionList.size();i<is;){
    			bInstruction = this.instructionList.get(i);
    			bInstruction.setStatus(0);
    			break;
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	System.out.println("deleteInstruction:"+retval);
    	return retval;
    }
	private TPRBlockEntry cleanBlockEntry(Connection conn, TPRBlockEntry tprBlockEntry,int workStationTypeId) {
		try{
			if(tprBlockEntry == null)
				return tprBlockEntry;
			System.out.println("Clean blockEntry:"+tprBlockEntry.getInstructionId()+","+tprBlockEntry.getSystemCauseId() );
			if(tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_AUTO &&  tprBlockEntry.getType() == BLOCK_TRIP){
				BlockingInstruction bIns = (BlockingInstruction) RFIDMasterDao.get(conn, BlockingInstruction.class, tprBlockEntry.getInstructionId());
				int ans = getTPRQuestionResult(tprBlockEntry.getTprId(), bIns == null ? Misc.getUndefInt() : bIns.getType());
				if(ans == 1){
					tprBlockEntry.setOverrideWorkstationTypeId(Integer.MAX_VALUE);
				}
			}else if(tprBlockEntry.getWorkstationTypeId() > workStationTypeId){
				if(tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_MANUAL && tprBlockEntry.getType() == BLOCK_STEP){
					deleteInstruction(tprBlockEntry.getInstructionId());
				}
				tprBlockEntry.setStatus(0);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("Clean blockEntry:"+tprBlockEntry);
		return tprBlockEntry;
	}
	public TPRBlockEntry createTPRBlockingEntry(int vehicleId ,int tprId, int workstationTypeId, int bInstructionId, int type, int createType, int skippedStepId){
		TPRBlockEntry retval = null;
		try{
			System.out.println("createTPRBlockingEntry:"+vehicleId+","+tprId+","+workstationTypeId+","+bInstructionId+","+(type == BLOCK_STEP ? "STEP" : "TRIP" )+",");
			retval = new TPRBlockEntry();
			retval.setTprId(tprId);
			retval.setType(type);
			retval.setStatus(1);
			retval.setWorkstationTypeId(workstationTypeId);
			retval.setInstructionId(bInstructionId);
			retval.setSkippedStepId(skippedStepId);
			retval.setCreatedOn(new Date());
			retval.setCreateType(createType);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("createTPRBlockingEntry:"+retval);
		return retval;
	}
	
	public boolean overrideTPRBlockingEntry(Connection conn, int vehicleId, TPRecord tpr, int overrideStatus, String notes, boolean overrideTPR, int userId){
		boolean retval = false;
		TPRBlockEntry tprBlockEntry = null;
		TPStep currStep = null;
		try{//override all entries by using override tpr or override step status
			if(tpr == null)
				return false;
			System.out.println("overrideTPRBlockingEntry:"+vehicleId+","+tpr.getTprId()+","+overrideStatus+","+notes+","+userId);
			currStep = TPRUtils.getCurrentStep(conn, tpr.getTprId());
			for(int i=0,is=tprBlockEntries == null ? 0 : tprBlockEntries.size();i<is;i++){
				tprBlockEntry = tprBlockEntries.get(i);
				int overrideWorkstationTypeId = Misc.getUndefInt();
				if(tprBlockEntry.getWorkstationTypeId() <= tprBlockEntry.getOverrideWorkstationTypeId())
					continue;
				if(
					tprBlockEntry.getType() == BLOCK_STEP
					){//system based block
					overrideWorkstationTypeId = currStep != null ? currStep.getWorkStationType() : tprBlockEntry.getWorkstationTypeId();
				}else{
					overrideWorkstationTypeId = Integer.MAX_VALUE;
				}
				tprBlockEntry.setTprId(tpr.getTprId());
				tprBlockEntry.setOverrideWorkstationTypeId(overrideWorkstationTypeId);
				tprBlockEntry.setOverrideStatus(overrideStatus);
				tprBlockEntry.setOverrideDate(new Date());
				tprBlockEntry.setOverridNotes(notes);
				tprBlockEntry.setOverrideTPROnly(overrideTPR ? 1 :0);
				tprBlockEntry.setOverrideStepOnly(overrideTPR ? 0 : 1);
				tprBlockEntry.setUpdatedBy(userId);
				if(RFIDMasterDao.update(conn, tprBlockEntry,false)){
					retval = true;
				}
				if(tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_MANUAL && tprBlockEntry.getType() == BLOCK_STEP){
					deleteInstruction(tprBlockEntry.getInstructionId());
					RFIDMasterDao.update(conn, tprBlockEntry,false);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	public int getBlockStatus(){
		int status = NOT_BLOCKED;
		try{
			TPRBlockEntry tprBlockEntry = null;
			for(int i=0,is = tprBlockEntries == null ? 0 : tprBlockEntries.size(); i< is; i++){
				tprBlockEntry = tprBlockEntries.get(i);
				if(tprBlockEntry.getStatus() == 0 || tprBlockEntry.getWorkstationTypeId() <= tprBlockEntry.getOverrideWorkstationTypeId()){
					continue;
				}else{//blocked
					status = BLOCKED;
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return status;
	}
	
	public Pair<Integer, ArrayList<TPRBlockEntry>> allowCurrentStep(Connection conn, int vehicleId ,TPRecord tpr, int tpsId, int workstationTypeId, int userId, boolean createNew, boolean save){
		int status = NOT_BLOCKED;
		ArrayList<TPRBlockEntry> finalBlockingEntries = null; 
		try{
			TPRBlockEntry tprBlockEntry = null;
			int is = tprBlockEntries == null ? 0 : tprBlockEntries.size();
			if(is > 0){
				status = OVERRIDE;
				for(int i=0; i< is; i++){
					tprBlockEntry = tprBlockEntries.get(i);
					if(tprBlockEntry.getWorkstationTypeId() <= tprBlockEntry.getOverrideWorkstationTypeId()){
						continue;
					}else{//blocked
						status = BLOCKED;
						if(finalBlockingEntries == null){
							finalBlockingEntries = new ArrayList<TPRBlockEntry>();
						}
						finalBlockingEntries.add(tprBlockEntry);
						//break;
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return new Pair<Integer, ArrayList<TPRBlockEntry>>(status, finalBlockingEntries);
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		Triple<TPRecord, Integer, Boolean> tpr = null;
		
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			//int id = TPRUtils.isVehicleDocumentComplete(conn, 25427, Status.TPRQuestion.isInsuranceOk, 24*60*60*1000); 
			Pair<Integer, String> data = TPRUtils.getSupplierFromDo(conn, 2);
			System.out.println();
			//insertAutoInstruction(conn, 463);
			/*tpr = TPRInformation.getLatestTPR(conn, 23450, null, null, false, true, Misc.getUndefInt());
			overrideTPRBlockingEntry(conn, 23450, tpr != null ?tpr.first : null, 1, "123", false, 1);*/
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public int getTPRQuestionResult(int tprId,int instructionType){
		int ans = Misc.getUndefInt();
		int questionId = getQuestionId(instructionType);
		if(Misc.isUndef(questionId))
			return ans;
		for(int i=0,is=questionList == null ? 0 : questionList.size();i<is;i++){
			if(questionList.get(i).getQuestionId() == questionId)
				return questionList.get(i).getAnswerId();
		}
		return Misc.getUndefInt();
	}
	public TPSQuestionDetail getTPRQuestion(int instructionType){
		int questionId = getQuestionId(instructionType);
		if(Misc.isUndef(questionId))
			return null;
		for(int i=0,is=questionList == null ? 0 : questionList.size();i<is;i++){
			if(questionList.get(i).getQuestionId() == questionId)
				return questionList.get(i);
		}
		return null;
	}
	private void setTprQuestionList(Connection conn) {
		if(Misc.isUndef(tprId))
			return;
		try{
			TPSQuestionDetail tpsQuestionBean = new TPSQuestionDetail();
            tpsQuestionBean.setTprId(tprId);
            this.questionList = (ArrayList<TPSQuestionDetail>) RFIDMasterDao.getList(conn, tpsQuestionBean, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void addQuestions(TPSQuestionDetail tpsQuestionBean){
		if(tpsQuestionBean == null || Misc.isUndef(tpsQuestionBean.getQuestionId()))
			return;
		for(int i=0,is=questionList == null ? 0 : questionList.size();i<is;i++){
			if(questionList.get(i).getQuestionId() == tpsQuestionBean.getQuestionId()){
				questionList.remove(i);
				break;
			}
		}
		if(this.questionList == null)
			this.questionList = new ArrayList<TPSQuestionDetail>();
		questionList.add(tpsQuestionBean);
		if(!useForBlocking(tpsQuestionBean.getQuestionId()))
			return;
		int blockInstructionType = getBlockInstructionType(tpsQuestionBean.getQuestionId());
		if(Misc.isUndef(blockInstructionType))
			return;
		BlockingInstruction bIns = getInstructionByType(blockInstructionType);
		TPRBlockEntry tempTprBlockEntry = null;
		int findIndex = Misc.getUndefInt();
		for(int i=0,is=tprBlockEntries == null ? 0 : tprBlockEntries.size();i<is;i++){
			if(tprBlockEntries.get(i).getInstructionId() == bIns.getId()){
				tempTprBlockEntry = tprBlockEntries.get(i);
				findIndex = i;
				//tprBlockEntries.remove(i);
				break;
			}
		}
		
		if(tempTprBlockEntry != null){
			if(tempTprBlockEntry.getWorkstationTypeId() <= tempTprBlockEntry.getOverrideWorkstationTypeId())
				return;
			else{
				if(!Misc.isUndef(findIndex)){
					if(Misc.isUndef(tprBlockEntries.get(findIndex).getId())){
						tprBlockEntries.remove(findIndex);
					}else{
						tprBlockEntries.get(findIndex).setStatus(0);
					}
					
				}
			}
		}
		TPRBlockEntry tprBlockEntry = requireBlock(bIns);
		if(tprBlockEntry != null){
			if(tprBlockEntries == null)
				tprBlockEntries = new ArrayList<TPRBlockEntry>();
			tprBlockEntries.add(tprBlockEntry);
		}
	}
	public void removeQuestions(Connection conn , int questionId){
		if(Misc.isUndef(questionId) || questionList == null || questionList.size() == 0)
			return;
		for(int i=0,is=questionList == null ? 0 : questionList.size();i<is;i++){
			if(questionList.get(i).getQuestionId() == questionId){
				questionList.remove(i);
				break;
			}
		}
		int blockInstructionType = getBlockInstructionType(questionId);
		if(Misc.isUndef(blockInstructionType))
			return;
		BlockingInstruction bIns = getInstructionByType(blockInstructionType);
		for(int i=0,is=tprBlockEntries == null ? 0 : tprBlockEntries.size();i<is;i++){
			if(tprBlockEntries.get(i).getInstructionId() == bIns.getId()){
				tprBlockEntries.remove(i);
				break;
			}
		}
	}
	public boolean useForBlocking(int questionId){//used if question id is passed
		boolean retval = false;
		int instructionType = getBlockInstructionType(questionId);
		if(Misc.isUndef(questionId))
			return retval;
		try{
			for(int i=0,is=instructionList == null ? 0 : instructionList.size();i<is;i++){
				if(instructionList.get(i).getType() == instructionType)
					return true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public boolean useForBlockingByInstructionId(int instructionType){//used if instruction id is passed
		boolean retval = false;
		try{
			for(int i=0,is=instructionList == null ? 0 : instructionList.size();i<is;i++){
				if(instructionList.get(i).getType() == instructionType)
					return true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public String getBlockingReason(){
		return getBlockingReason(true);
	}
	public String getBlockingReason(boolean doHTML){
    	String retval = "";
    	try{
    		for(int i=0,is=tprBlockEntries==null? 0 : tprBlockEntries.size();i<is;i++){
    			TPRBlockEntry tprBlockEntry = tprBlockEntries.get(i);
    			if(tprBlockEntry == null || tprBlockEntry.getStatus() == 0 ||tprBlockEntry.getWorkstationTypeId() <= tprBlockEntry.getOverrideWorkstationTypeId())
    				continue;
    			BlockingInstruction bInstruction = getInstructionById(tprBlockEntry.getInstructionId());
    			if(bInstruction == null)
    				continue;
    			String skipWorkStationTypeStr = tpr == null ? "" : Type.WorkStationType.getString(tpr.getNextStepType());
    			String blockStr = (bInstruction.getType() == Type.BlockingInstruction.BLOCK_DUETO_STEP_JUMP ? "Skip "+ skipWorkStationTypeStr : Type.BlockingInstruction.getBlockingStr(bInstruction.getType()));
    			if(retval.length() > 0){
    				if(retval.contains(blockStr))
    					continue;
    				if(doHTML)
    					retval += "&nbsp;&nbsp;";
    				else if(retval != null && retval.length() > 0){
    					retval += ";";
    				}
    			}
    			if(doHTML){
    				retval += "<span style='background-color: red;color:white; margin-left:5px; padding:5px; font-size:16pt;'>&nbsp;"+blockStr+"&nbsp;";
    				retval += "</span>";
    			}else{
    				retval += blockStr;
    			}
    		}
    		if(retval != null && retval.length() > 0 && doHTML){
    			retval = "<html><body>"+retval+"</body></html>";
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return retval;
    }
	public boolean isOverrideAllowed(Connection conn){
		BlockingInstruction bIns = getInstructionByType(Type.BlockingInstruction.BLOCK_DUETO_MULTIPLE_TPR);
		TPRBlockEntry tprBlockEntry = null;
		for (int i = 0,is = tprBlockEntries == null ? 0 : tprBlockEntries.size(); i < is; i++) {
			tprBlockEntry = tprBlockEntries.get(i);
			if(tprBlockEntry.getInstructionId() == bIns.getId()){
				try{
					if(RFIDMasterDao.getRowCount(conn, "select 1 from tp_record where status=1 and tpr_status=0 and vehicle_id"+vehicleId) > 1)
						return false;
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		return true;
	}
	public ArrayList<TPRBlockEntry> getBlockEntries(){
		return tprBlockEntries;
	}
}

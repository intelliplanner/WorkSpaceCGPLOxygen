package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.VehicleTPRBlockStatus;
import com.ipssi.rfid.constant.Status.TPRQuestion;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;

public class TPRBlockStatusHelper {
	public static final int OVERRIDE = 2;
    public static final int BLOCKED = 3;
    public static final int NOT_BLOCKED = 1;
	
	private static int TRUE = 1;
	private static int FALSE = 2;
	
	public static final int BLOCK_STEP = 0;
	public static final int BLOCK_TRIP = 1;
	public static final int BLOCK_QUESTION = 2;
//	private static ArrayList<BlockingInstruction> autoInstructionList = null;



	static{
		BlockingInstruction bInstruction = null;    	
		try{
			/*if(autoInstructionList == null){
				autoInstructionList = new ArrayList<BlockingInstruction>();
				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_STEP_JUMP);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);

				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_QC);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);

				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_GPS);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);

				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_DRUNCK);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);
				
				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_TAG_NOT_READ);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);
				
				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_CAPTURED);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);
				
				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_VERIFIED);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);
				
				bInstruction = new BlockingInstruction();
				bInstruction.setType(Type.BlockingInstruction.BLOCK_DUETO_VEHICLE_NOT_EXSIT);
				//bInstruction.setVehicleId(vehicleId);
				bInstruction.setStatus(1);
				autoInstructionList.add(bInstruction);
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	private static ArrayList<Object> getAutoInstructionList(Connection conn, int portNodeId){
		if(Misc.isUndef(portNodeId))
			return null;
		ArrayList<Object> retval = null;
		try{
			BlockingInstruction instruction = new BlockingInstruction();
			instruction.setPortNodeId(portNodeId);
			instruction.setStatus(1);
			retval = RFIDMasterDao.select(conn, instruction);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	private static ArrayList<Object> getManualInstructionList(Connection conn, int vehicleId){
		if(Misc.isUndef(vehicleId))
			return null;
		ArrayList<Object> retval = null;
		try{
			BlockingInstruction instruction = new BlockingInstruction();
			instruction.setVehicleId(vehicleId);
			instruction.setStatus(1);
			retval = RFIDMasterDao.select(conn, instruction);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static TPRBlockEntry requireAutoBlock(Connection conn,int vehicleId,TPRecord tpr,int workStationTypeId,BlockingInstruction bInstruction,int userId,boolean save){
		TPRBlockEntry retval = null;
		int blockType = BLOCK_STEP;
		boolean addNew = false;
		try{
			if(Misc.isUndef(vehicleId) || bInstruction == null || tpr == null)
				return retval;
			if((Type.BlockingInstruction.BLOCK_DUETO_STEP_JUMP == bInstruction.getType() 
					&& (
					tpr.getNextStepType() < workStationTypeId))){
				blockType = BLOCK_STEP;
				addNew = true;	
			}else if(Type.BlockingInstruction.BLOCK_DUETO_QC == bInstruction.getType() ){
				int ans = tpr.getMarkForQC() == 1 ? getTPRQuestionResult(conn, tpr.getTprId(), bInstruction.getType()) : Misc.getUndefInt() ;//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.qcDone);
				if(!Misc.isUndef(ans) && ans == 2 ){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_DRUNCK == bInstruction.getType() ){
				int ans = getTPRQuestionResult(conn, tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.breathLyzerOk);
				if(!Misc.isUndef(ans) && ans != 1){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_GPS == bInstruction.getType() ){
				int ans = tpr.getMarkForGPS() == 1 ? getTPRQuestionResult(conn, tpr.getTprId(), bInstruction.getType()) : Misc.getUndefInt();//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.gpsOk);
				if(!Misc.isUndef(ans) && ans != 1){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}else if(Type.BlockingInstruction.BLOCK_DUETO_DOC_INCOMPLETE == bInstruction.getType()){
				/*int ans = TPRUtils.isVehicleDocumentComplete(conn, tpr.getVehicleId(), (15*24*60*60*1000));
				if(!Misc.isUndef(ans) && ans != 1){
					blockType = BLOCK_TRIP;
					addNew = true;
				}*/
				
			}else if(bInstruction.getType() > 4000){
				int ans = getTPRQuestionResult(conn, tpr.getTprId(), bInstruction.getType());//TPRUtils.getQuestionResult(conn, tpr.getTprId(), TPRQuestion.gpsOk);
				if(!Misc.isUndef(ans) && ans != 1){
					blockType = BLOCK_TRIP;
					addNew = true;
				}
			}
			if(addNew)
				retval = createTPRBlockingEntry(conn, vehicleId, tpr.getTprId(), workStationTypeId, bInstruction.getId(), userId,blockType, TPRBlockEntry.CREATE_TYPE_AUTO,save);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	private static TPRBlockEntry requireManualBlock(Connection conn,int vehicleId,TPRecord tpr,int workStationTypeId,BlockingInstruction bInstruction,int userId,boolean save){
		TPRBlockEntry retval = null;
		int blockType = BLOCK_STEP;
		boolean addNew = false;
		try{
			if(Misc.isUndef(vehicleId) || bInstruction == null || tpr == null)
				return retval;
			if(Type.BlockingInstruction.BLOCK_DUETO_NEXT_STEP == bInstruction.getType()){
				blockType = BLOCK_STEP;
				addNew = true;
			}else if(Type.BlockingInstruction.BLOCK_DUETO_BLACKLIST == bInstruction.getType() ){
				long blockFrom = bInstruction.getBlockFrom() != null ? bInstruction.getBlockFrom().getTime() : Misc.getUndefInt();
				long blockTo = bInstruction.getBlockTo() != null ? bInstruction.getBlockTo().getTime() : Misc.getUndefInt();
				blockType = BLOCK_TRIP;
				addNew = !Misc.isUndef(blockFrom) && (tpr.getTprCreateDate().getTime() > blockFrom  && (Misc.isUndef(blockTo) || tpr.getTprCreateDate().getTime() < blockTo));
			}
			if(addNew)
				retval = createTPRBlockingEntry(conn, vehicleId, tpr.getTprId(), workStationTypeId, bInstruction.getId(), userId,blockType, TPRBlockEntry.CREATE_TYPE_MANUAL,save);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	private static ArrayList<Object> getTPRBlockEntryList(Connection conn, int tprId){
		if(Misc.isUndef(tprId))
			return null;
		ArrayList<Object> retval = null;
		try{
			TPRBlockEntry blockEntry = new TPRBlockEntry();
			blockEntry.setTprId(tprId);
			blockEntry.setStatus(1);
			retval = RFIDMasterDao.select(conn, blockEntry);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static ArrayList<TPRBlockEntry> getVehicleBlockingEntries(Connection conn, int vehicleId ,TPRecord tpr, int workstationTypeId, int userId){
		return getVehicleBlockingEntries(conn, vehicleId, tpr, workstationTypeId, userId, true);
	}
	public static ArrayList<TPRBlockEntry> getVehicleBlockingEntries(Connection conn, int vehicleId ,TPRecord tpr, int workstationTypeId, int userId, boolean createNew){
		return getVehicleBlockingEntries(conn, vehicleId, tpr, workstationTypeId, userId, createNew,true);
	}
	public static ArrayList<TPRBlockEntry> getVehicleBlockingEntries(Connection conn, int vehicleId ,TPRecord tpr, int workstationTypeId, int userId, boolean createNew, boolean save){
		ArrayList<TPRBlockEntry> retval = new ArrayList<TPRBlockEntry>();
		ArrayList<Object> manualInstructionList = null;
		ArrayList<Object> autoInstructionList = null;
		ArrayList<Object> tprBlockingEntries = null;
		ArrayList<Integer> ignoreAutoInstruction = new ArrayList<Integer>();
		ArrayList<Integer> ignoreManualInstruction = new ArrayList<Integer>();
		int tprId = tpr == null ?  Misc.getUndefInt() : tpr.getTprId();
		if(Misc.isUndef(vehicleId))
			return null;
		try{
			System.out.println("getVehicleBlockingEntries:"+vehicleId+","+Type.WorkStationType.getString(workstationTypeId));
			if(tpr != null 
//			&& !Misc.isUndef(tpr.getTprId())
			){
				if(Misc.isUndef(workstationTypeId)){
					TPStep currStep = TPRUtils.getCurrentStep(conn, tprId);
					if(currStep != null)
						workstationTypeId = currStep.getWorkStationType();
				}
				tprBlockingEntries = getTPRBlockEntryList(conn, tprId);
				TPRBlockEntry tprBlockEntry = null;
				for(int i=0, is=tprBlockingEntries==null? 0 : tprBlockingEntries.size(); i<is; i++){
					tprBlockEntry = (TPRBlockEntry)tprBlockingEntries.get(i);
					boolean ignore = false;
					tprBlockEntry = cleanBlockEntry(conn, tprBlockEntry, workstationTypeId,save);
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
							if(
								//!Misc.isUndef(tprBlockEntry.getSystemCauseId())
								tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_AUTO
									){
								ignoreAutoInstruction.add(tprBlockEntry.getInstructionId());
							}else{
								ignoreManualInstruction.add(tprBlockEntry.getInstructionId());
							}
						}
						retval.add(tprBlockEntry);
					}
				}
			}
			if(createNew){
				//create for autoInstruction
				BlockingInstruction bAutoIns = null;
				TPRBlockEntry autoBlockEntry = null;
				autoInstructionList = getAutoInstructionList(conn, 463);
				for(int j=0,js=autoInstructionList == null ? 0 : autoInstructionList.size() ;j<js;j++){
					boolean ignoreAuto = false;
					bAutoIns = (BlockingInstruction)autoInstructionList.get(j);
					for(int k=0,ks=ignoreAutoInstruction == null ? 0 : ignoreAutoInstruction.size() ;k<ks;k++){
						if(ignoreAutoInstruction.get(k) == bAutoIns.getId()){//in case of system based blocking instruction_id field is null
							ignoreAuto = true;
							break;
						}
					}
					if(ignoreAuto)
						continue;
					autoBlockEntry = requireAutoBlock(conn, vehicleId, tpr, workstationTypeId, bAutoIns,userId,save);
					if(autoBlockEntry != null)
						retval.add(autoBlockEntry);
				}
				//create for manualInstruction
				manualInstructionList = getManualInstructionList(conn, vehicleId);
				BlockingInstruction bManualIns = null;
				TPRBlockEntry manualBlockEntry = null;
				for(int l=0,ls=manualInstructionList == null ? 0 : manualInstructionList.size() ;l<ls;l++){
					boolean ignoreManual = false;
					bManualIns = (BlockingInstruction)manualInstructionList.get(l);
					for(int m=0,ms=ignoreManualInstruction == null ? 0 : ignoreManualInstruction.size() ;m<ms;m++){
						if(ignoreManualInstruction.get(m) == bManualIns.getId()){
							ignoreManual = true;
							break;
						}
					}
					if(ignoreManual)
						continue;
					manualBlockEntry = requireManualBlock(conn, vehicleId, tpr, workstationTypeId, bManualIns,userId,save);
					if(manualBlockEntry != null)
						retval.add(manualBlockEntry);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("getVehicleBlockingEntries result:"+retval);
		return retval;
	}

    private static boolean deleteInstruction(Connection conn,int instructionId){
    	boolean retval = false;
    	BlockingInstruction bInstruction = null;
    	ArrayList<Object> list = null;
    	try{
    		System.out.println("deleteInstruction:"+instructionId);
    		bInstruction = new BlockingInstruction();
    		bInstruction.setId(instructionId);
    		list = RFIDMasterDao.select(conn, bInstruction);
    		for(int i=0,is=list == null? 0 : list.size();i<is;){
    			bInstruction = (BlockingInstruction) list.get(i);
    			bInstruction.setStatus(0);
    			retval = RFIDMasterDao.update(conn, bInstruction,false);
    			break;
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	System.out.println("deleteInstruction:"+retval);
    	return retval;
    }
	private static TPRBlockEntry cleanBlockEntry(Connection conn, TPRBlockEntry tprBlockEntry,int workStationTypeId,boolean save) {
		try{
			if(tprBlockEntry == null)
				return tprBlockEntry;
			System.out.println("Clean blockEntry:"+tprBlockEntry.getInstructionId()+","+tprBlockEntry.getSystemCauseId() );
			if(tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_AUTO &&  tprBlockEntry.getType() == BLOCK_TRIP){
				BlockingInstruction bIns = (BlockingInstruction) RFIDMasterDao.get(conn, BlockingInstruction.class, tprBlockEntry.getInstructionId());
				int ans = getTPRQuestionResult(conn, tprBlockEntry.getTprId(), bIns == null ? Misc.getUndefInt() : bIns.getType());
				if(ans == 1){
					tprBlockEntry.setOverrideWorkstationTypeId(Integer.MAX_VALUE);
					if(save){
						if(RFIDMasterDao.update(conn, tprBlockEntry,false))
							tprBlockEntry = null;
					}else
						tprBlockEntry = null;
				}
			}else if(tprBlockEntry.getWorkstationTypeId() > workStationTypeId){
				if(tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_MANUAL && tprBlockEntry.getType() == BLOCK_STEP && save){
					deleteInstruction(conn, tprBlockEntry.getInstructionId());
				}
				tprBlockEntry.setStatus(0);
				if(save){
					RFIDMasterDao.update(conn, tprBlockEntry,false);
				}
				tprBlockEntry = null;
			}

			/*
			if(tprBlockEntry.getWorkstationTypeId() > workStationTypeId && ((tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_AUTO &&  tprBlockEntry.getType() == BLOCK_STEP) || (tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_MANUAL) )){//delete
				if(tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_MANUAL && tprBlockEntry.getType() == BLOCK_STEP){
					deleteInstruction(conn, tprBlockEntry.getInstructionId());
				}
				tprBlockEntry.setStatus(0);
				if(save){
					RFIDMasterDao.update(conn, tprBlockEntry);
				}
				tprBlockEntry = null;
			}else{
				int ans = getTPRQuestionResult(conn, tprBlockEntry.getTprId(), tprBlockEntry.getSystemCauseId());
				if(ans == 1){
					tprBlockEntry.setOverrideWorkstationTypeId(Integer.MAX_VALUE);
					if(save){
						if(RFIDMasterDao.update(conn, tprBlockEntry))
							tprBlockEntry = null;
					}else
						tprBlockEntry = null;
				}
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("Clean blockEntry:"+tprBlockEntry);
		return tprBlockEntry;
	}
	public static TPRBlockEntry createTPRBlockingEntry(Connection conn, int vehicleId ,int tprId, int workstationTypeId, int bInstructionId,int userId, int type, int createType, boolean save){
		TPRBlockEntry retval = null;
		try{
			System.out.println("createTPRBlockingEntry:"+vehicleId+","+tprId+","+workstationTypeId+","+bInstructionId+","+(type == BLOCK_STEP ? "STEP" : "TRIP" )+",");
			retval = new TPRBlockEntry();
			retval.setTprId(tprId);
			retval.setType(type);
			retval.setStatus(1);
			retval.setWorkstationTypeId(workstationTypeId);
			retval.setInstructionId(bInstructionId);
			retval.setCreatedBy(userId);
			retval.setCreatedOn(new Date());
			retval.setCreateType(createType);
			if(!Misc.isUndef(tprId) && save){
				if(!RFIDMasterDao.insert(conn, retval,false)){
					retval = null;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("createTPRBlockingEntry:"+retval);
		return retval;
	}
	public static void updateVehicleTPRBlockStatus(Connection conn, int vehicleId ,int tprId,String causeString,int blockStatus){
		if(Misc.isUndef(vehicleId))
			return;
		ArrayList<Object> list = null;
		boolean create = true;
		try{
			VehicleTPRBlockStatus vehicleBlockStatus = new VehicleTPRBlockStatus();
			vehicleBlockStatus.setVehicleId(vehicleId);
			list = RFIDMasterDao.select(conn, vehicleBlockStatus);
			if(list != null && list.size() > 0){
				vehicleBlockStatus = (VehicleTPRBlockStatus) list.get(0);
				create = false;
			}
			vehicleBlockStatus.setIsBlackListed(blockStatus);
			vehicleBlockStatus.setBlockInstructionStr(causeString);
			if(!create)
				RFIDMasterDao.update(conn, vehicleBlockStatus,false);
			else
				RFIDMasterDao.insert(conn, vehicleBlockStatus,false);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static boolean overrideTPRBlockingEntry(Connection conn, int vehicleId, TPRecord tpr, int overrideStatus, String notes, boolean overrideTPR, int userId){
		boolean retval = false;
		TPRBlockEntry tprBlockEntry = null;
		TPStep currStep = null;
		try{//override all entries by using override tpr or override step status
			if(tpr == null)
				return false;
			System.out.println("overrideTPRBlockingEntry:"+vehicleId+","+tpr.getTprId()+","+overrideStatus+","+notes+","+userId);
			ArrayList<TPRBlockEntry> list = getVehicleBlockingEntries(conn, vehicleId, tpr, Misc.getUndefInt(), userId, false);
			currStep = TPRUtils.getCurrentStep(conn, tpr.getTprId());
			for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
				tprBlockEntry = list.get(i);
				int overrideWorkstationTypeId = Misc.getUndefInt();
				if(tprBlockEntry.getWorkstationTypeId() <= tprBlockEntry.getOverrideWorkstationTypeId())
					continue;
				if(
//					!Misc.isUndef(tprBlockEntry.getSystemCauseId()) 
//					|| 
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
					deleteInstruction(conn, tprBlockEntry.getInstructionId());
				}
			}
			tpr.setBlockedStepType(Misc.getUndefInt());
			tpr.setBlockedStepDate(null);
			TPRInformation.insertUpdateTpr(conn, tpr);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static Pair<Integer, ArrayList<TPRBlockEntry>> allowCurrentStep(Connection conn, int vehicleId ,TPRecord tpr, int tpsId, int workstationTypeId, int userId, boolean createNew, boolean save){
		int status = NOT_BLOCKED;
		ArrayList<TPRBlockEntry> finalBlockingEntries = null; 
		try{
			ArrayList<TPRBlockEntry> blockEntries = getVehicleBlockingEntries(conn, vehicleId, tpr, workstationTypeId, userId, createNew, save);
			TPRBlockEntry tprBlockEntry = null;
			int is = blockEntries == null ? 0 : blockEntries.size();
			if(is > 0){
				status = OVERRIDE;
				for(int i=0; i< is; i++){
					tprBlockEntry = blockEntries.get(i);
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
	/*public static boolean allowCurrentStep(Connection conn, int vehicleId ,TPRecord tpr, int tpsId, int workstationTypeId, int userId, boolean createNew, boolean save){
		boolean retval = true;
		
		try{
			ArrayList<TPRBlockEntry> blockEntries = getVehicleBlockingEntries(conn, vehicleId, tpr, workstationTypeId, userId, createNew, save);
			TPRBlockEntry tprBlockEntry = null;
			for(int i=0,is=blockEntries == null ? 0 : blockEntries.size(); i< is; i++){
				tprBlockEntry = blockEntries.get(i);
				if(tprBlockEntry.getWorkstationTypeId() <= tprBlockEntry.getOverrideWorkstationTypeId()){
					continue;
				}else{
					retval = false;
					break;
				}
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}*/
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
	
	private static int getTPRQuestionResult(Connection conn,int tprId,int instructionType){
		int ans = Misc.getUndefInt();
		if(Type.BlockingInstruction.BLOCK_DUETO_QC == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.qcDone);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_DRUNCK == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.breathLyzerOk);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_GPS == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.gpsOk);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_TAG_NOT_READ == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isTagRead);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_VERIFIED == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isFingerVerified);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_CAPTURED == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isFingerCaptured);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_VEHICLE_NOT_EXIST == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isVehicleExist);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_CHALLAN_NOT_EXIST == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isChallanExist);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_FITNESS_EXPIRED == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isFitnessOk);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_ROAD_PERMIT_EXPIRED == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isRoadPermitOk);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_INSURANCE_EXPIRED == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isInsuranceOk);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_POLUTION_EXPIRED == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.isPolutionOk);
		}else if(Type.BlockingInstruction.BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR == instructionType ){
			ans = TPRUtils.getQuestionResult(conn, tprId, TPRQuestion.haveYouInformedGpsVendor);
		}
		return ans;
	}
	public static boolean useForBlocking(int questionId){
		boolean retval = false;
		int instructionType = Misc.getUndefInt();
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if(TPRQuestion.qcDone == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_QC;
			}else if(TPRQuestion.gpsOk == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_GPS;
			}else if(TPRQuestion.breathLyzerOk == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_DRUNCK;
			}else if(TPRQuestion.isTagRead == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_TAG_NOT_READ;
			}else if(TPRQuestion.isFingerVerified == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_VERIFIED;
			}else if(TPRQuestion.isFingerCaptured == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_FINGER_NOT_CAPTURED;
			}else if(TPRQuestion.isVehicleExist == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_VEHICLE_NOT_EXIST;
			}else if(TPRQuestion.isChallanExist == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_CHALLAN_NOT_EXIST;
			}else if(TPRQuestion.isFitnessOk == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_FITNESS_EXPIRED;
			}else if(TPRQuestion.isRoadPermitOk == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_ROAD_PERMIT_EXPIRED;
			}else if(TPRQuestion.isInsuranceOk == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_INSURANCE_EXPIRED;
			}else if(TPRQuestion.isPolutionOk == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_POLUTION_EXPIRED;
			}else if(TPRQuestion.haveYouInformedGpsVendor == questionId ){
				instructionType = Type.BlockingInstruction.BLOCK_DUETO_NOT_INFORMED_GPS_VENDOR;
			}
			BlockingInstruction bInstruction = new BlockingInstruction();
			bInstruction.setType(instructionType);
			bInstruction.setStatus(1);
			ArrayList<Object> list = RFIDMasterDao.select(conn, bInstruction);
			for(int i=0,is=list== null ? 0 : list.size();i<is;i++){
				BlockingInstruction bIns = (BlockingInstruction) list.get(i);
				if(instructionType == bIns.getType()){
					retval = true;
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			try{
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retval;
	}
	public static Pair<Integer, Boolean> isBlockedForInstruction(Connection conn, int tprId, int instructionType){
		boolean blockStatus = false;
		int tprBlockId = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select tbs.id, tbs.workstation_type_id,tbs.override_workstation_type_id from tpr_block_status tbs left join block_instruction bin on (bin.id=tbs.instruction_id) where tbs.status=1 and tpr_id=? and bin.type=? ");
			Misc.setParamInt(ps, tprId, 1);
			Misc.setParamInt(ps, instructionType, 2);
			rs = ps.executeQuery();
			while(rs.next()){
				tprBlockId = Misc.getRsetInt(rs, 1);
				if(Misc.getRsetInt(rs, 2) > Misc.getRsetInt(rs, 3)){
					blockStatus = true;
					break;
				}
			}
			rs.close();
			ps.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return new Pair<Integer, Boolean>(tprBlockId, blockStatus);
	}
}

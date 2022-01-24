package com.ipssi.rfid.readers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.ProcessStepProfile;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;

public class RFIDDataProcessor {

    TAGListener tagListener = null;
    Object obj = new Object();
    int readerId;
    //Connection conn;
    int workStationType = Type.WorkStationType.GATE_IN_TYPE;
    int workStationId = Type.WorkStationType.GATE_IN_TYPE;
    int userId = Misc.getUndefInt();
    private Thread tagDataRead = null;
    public RFIDDataProcessor(int readerId ,int workStationType, int workStationTypeId, int userId) {
        this.readerId = readerId;
        this.workStationType = workStationType;
        this.workStationId = workStationTypeId;
        this.userId = userId;
    }

    public RFIDDataProcessor() {
    }

    public void setTagListener(TAGListener tagListener) {
        this.tagListener = tagListener;
    }

    synchronized public void pause() {
        //isRunning = true;
    }

    synchronized public void Resume() {
        //isRunning = false;
    }
    volatile boolean isTagReadThreadRunning = false;
    public void stopReadTagData(){
    	if(tagDataRead == null)
    		return;
    	try{
    		isTagReadThreadRunning = false;
    		//tagDataRead.join();
    		tagDataRead = null;
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    synchronized public void readTagData(final long sessionId, final String epc,final TAGListener handler){
    	stopReadTagData();
    	try{
    		tagDataRead = new Thread(new Runnable() {

    			@Override
    			public void run() {
    				while(isTagReadThreadRunning){
    					try{
    						RFIDTagInfo tag = RFIDMaster.getReader(readerId).getData(Utils.HexStringToByteArray(epc));
    						if(tag != null && tag.epcId !=null && (Utils.ByteArrayToHexString(tag.epcId).equalsIgnoreCase(Thread.currentThread().getName())) && handler != null && isTagReadThreadRunning){
    							RFIDHolder holder = new RFIDHolder(null, tag);
    							System.out.println("Read By Smart Read");
    							holder.printData();
    							handler.mergeData(readerId, sessionId, epc, holder);
    							isTagReadThreadRunning = false;
    							break;
    						}
    					}catch(Exception ex){
    						ex.printStackTrace();
    					}finally{
    						try {
								Thread.sleep(TokenManager.refreshInterval);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
    					}
    				}
    			}
    		});
    		isTagReadThreadRunning = true;
    		tagDataRead.setName(epc);
    		tagDataRead.start();
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
   
    private ArrayList<Pair<String,Long>> ignoreList = null;  
    synchronized public void processTag() {
        ArrayList<String> tags = null;
        Token token = null;
//        String vehicleName = null;
//        int vehicleId = Misc.getUndefInt();
        Connection conn = null;
        boolean destroyIt = false;
        StringBuilder sb = new StringBuilder();
        try {
            if (RFIDMaster.getReader(readerId) != null) {
                try {
                	sb.setLength(0);
                	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
                    tags = RFIDMaster.getReader(readerId).getRFIDTagList();
                    TPRWorkstationConfig workstationConfig = null;//!TokenManager.useSECLRFIDReaderProcess ? new TPRWorkstationConfig(workStationType, TokenManager.materialCat) : tagListener.getWorkstationConfig(conn, readerId, null);
                    String s = null;
                    if (tags != null && tags.size() > 0 && tagListener != null) {
                    	sb.append("[RFID TAG List Size]:" + tags.size()).append("\n");
                    	Vehicle veh = null;
                    	for (String epc : tags) {
                    		s = epc;
                    		if(!TokenManager.isTokenAvailable(conn, s))
                				continue;
                    		if(TokenManager.useSECLRFIDReaderProcess){
                    			Vehicle currVeh = Vehicle.getVehicleByEpc(conn, s);
                    			if(currVeh == null)
                    				continue;
                    			if(currVeh.getVehicleOnGate() != 1 && currVeh.getRfid_issue_date() != null && (System.currentTimeMillis()-currVeh.getRfid_issue_date().getTime()) <= 300*1000)
                    				continue;
                    			/*Token lastToken = TokenManager.getLastToken(s);
                    			if(lastToken != null && lastToken.isIgnore() && (System.currentTimeMillis() - lastToken.getLastProcessed()) < TokenManager.ingnoreThreshold ){
                    				
                    				continue;
                    			}*/
                    			if(veh == null)
                    				veh = currVeh;
                    			if(currVeh.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL || currVeh.getCardType() == Type.RFID_CARD_TYPE.PERMANENT){
                    				veh = currVeh;
                    				break;
                    			}
                    			//check initialization of tag;
                    		}else{
                    			break;
                    		}
                    	}
                    	if(veh == null)
                    		return;
                    	sb.append("[RFID Handler]:" + s).append("\n");
                    	token = TokenManager.createToken(conn, s);
                    	if(token == null)
                    		return;
                    	//if (!(TokenManager.useSECLRFIDReaderProcess && workstationConfig == null) && TokenManager.isTokenAvailable(conn, s)) {
                    	if(token != null){
                    		sb.append("######### Token Available  ########").append("\n");
                    		workstationConfig = !TokenManager.useSECLRFIDReaderProcess ? new TPRWorkstationConfig(workStationType, TokenManager.materialCat) : tagListener.getWorkstationConfig(conn, readerId, veh);
                    		System.out.println("Vehicle:"+veh.getVehicleName()+","+veh.getCardPurpose()+","+veh.getCardType()+","+veh.getVehicleOnGate());
                    		if(	//veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD || 
                    			//veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY || 
                    			veh.getCardType() == Type.RFID_CARD_TYPE.TEMPORARY ){
                    			if(veh.getCardInit() != 1){
                    				if(workstationConfig.getWorkstationType() != Type.WorkStationType.SECL_LOAD_GATE_OUT){
                    					tagListener.varfiyVehicle(readerId, veh);
                    				}
                    				return;
                    			}
                    		}

                    		if(workstationConfig != null) {
                    			int materialCat = workstationConfig.getMaterialCat();
                    			if(Misc.isUndef(materialCat) && veh != null){
                    				materialCat = veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ? Type.TPRMATERIAL.COAL_ROAD : 
                    					veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY ? Type.TPRMATERIAL.COAL_WASHERY :
                    						Type.TPRMATERIAL.COAL_INTERNAL; 
                    			}
                    			processVehicleAndNotify(readerId,conn, token, s, veh.getId(), null, veh.getStdName(), workstationConfig.getWorkstationType(), materialCat , sb, false);    
                    		}
                    		return;
                    	}else{
                    		//poll if gate
                    	}

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    destroyIt = true;
                } finally {
                    if(sb != null)
                    	System.out.println(sb.toString());
                    DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void processVehicleAndNotify(int readerId, Connection conn, Token token, String epcId, int vehicleId, RFIDHolder data, String vehicleName, int workStationType, int materialCat, StringBuilder sb) throws Exception {
    	processVehicleAndNotify(readerId, conn, token, epcId, vehicleId, data, vehicleName, workStationType, materialCat, sb, true);
    }
    public void processVehicleAndNotify(int readerId, Connection conn, Token token, String epcId, int vehicleId, RFIDHolder data, String vehicleName, int workStationType, int materialCat, StringBuilder sb,boolean isUIThread) throws Exception {
        if(token == null || token.getStatus() == Status.Token.PROCESSED)
            tagListener.clear(readerId, false, conn);
        if(sb == null)
            sb = new StringBuilder();
        Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, vehicleId, data, vehicleName, workStationType, materialCat,tagListener.getProcessStepProfile(materialCat));//ProcessStepProfile.getStandardProcessStepByMaterialCat(materialCat));//(conn, vehicleId, data, vehicleName, TokenManager.createNewTPR, workStationType);
        TPStep tpStep = null;
        TPRBlockManager tprBlockManager = null;
        if (tprTriplet == null || tprTriplet.first == null) {
            sb.append("Not creating tpr at this step").append("\n");
            tagListener.setVehicleName(readerId, vehicleName);
            tagListener.showMessage(readerId, "Sikpped Gate In.Go to gate In first.");//message will be changed according to scenario
            TokenManager.returnToken(conn, token);
            tagListener.clear(readerId, false, conn);
        } else if(tprTriplet.third){
        	//tprBlockManager = TPRBlockManager.getTprBlockStatus(conn, TokenManager.portNodeId, tprTriplet.first, workStationType,materialCat);
        	tpStep = TPRInformation.getTpStep(conn, tprTriplet.first, workStationType, workStationId, userId);
        	tagListener.manageTag(readerId, conn, token, tprTriplet.first, tpStep, tprBlockManager, isUIThread, false);
            if(tagListener.showMessage(readerId, "Record Already -Captured."))
            	tagListener.clear(readerId, false, conn);
        }else {
            tpStep = TPRInformation.getTpStep(conn, tprTriplet.first, workStationType, workStationId, userId);
            tprBlockManager = TPRBlockManager.getTprBlockStatus(conn, TokenManager.portNodeId, tprTriplet.first, workStationType,materialCat);
            tagListener.manageTag(readerId, conn, token, tprTriplet.first, tpStep, tprBlockManager, isUIThread, true);
            if(workStationType == Type.WorkStationType.GATE_IN_TYPE && TokenManager.useSmartRFRead && data == null){
                readTagData(token.getLastSeen(), epcId, tagListener);
            }
        }
    }

    public Triple<Token, TPRecord, TPRBlockManager> getTprecord(String vehicleName) {
        return getTprecord(vehicleName, Misc.getUndefInt(), true,true);
    }

    public Triple<Token, TPRecord, TPRBlockManager> getTprecord(String vehicleName, int vehicleId, boolean generateToken, boolean initTPR) {
        Triple<Token, TPRecord, TPRBlockManager> retval = null;
        Token token = null;
        Connection conn = null;
        boolean destroyIt = false;
        StringBuilder sb = new StringBuilder();
        //int vehicleId = Misc.getUndefInt();
        try {
        	sb.setLength(0);
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            if (TokenManager.isTokenAvailable(conn, null, vehicleName) || !generateToken) {
                //pause();
                if (generateToken) {
                    token = TokenManager.createToken(conn, null, vehicleName);
                }
                if (token != null) {
                    vehicleName = token.getVehicleName();
                    vehicleId = token.getVehicleId();
                }
                sb.append("[Vehicle Found]:" + vehicleId+","+vehicleName).append("\n");
                Triple<TPRecord, Integer, Boolean> tprTriplet = TPRInformation.getLatestNonWeb(conn, vehicleId, null, vehicleName, workStationType, TokenManager.materialCat);
                TPStep tpStep = null;
                TPRBlockManager tprBlockManager = null;
                if (tprTriplet == null || tprTriplet.first == null) {
                	sb.append("Not creating tpr at this step").append("\n");
                    if(tagListener != null){
                    	tagListener.showMessage(readerId, "Sikpped Gate In.Go to gate In first.");//message will be changed according to scenario
                    	tagListener.setVehicleName(readerId, vehicleName);
                    }
                    if(generateToken)
                    TokenManager.returnToken(conn, token);
                    if(tagListener != null){
                    	tagListener.clear(readerId, false, conn);
                    }
                } else if(tprTriplet.third){
                	 if(tagListener != null){
                		 //tagListener.setVehicleName(vehicleName);
                		 if(initTPR)
                			 tagListener.manageTag(readerId, conn, token, tprTriplet.first, tpStep, tprBlockManager, true, true);
                		 tagListener.showMessage(readerId, "Record Already Captured.");//message will be changed according to scenario
                     }
                     if(tagListener != null){
                     	tagListener.clear(readerId, false, conn);
                     }
                }else {
                	tprBlockManager = TPRBlockManager.getTprBlockStatus(conn, TokenManager.portNodeId, tprTriplet.first, TokenManager.currWorkStationType,!Misc.isUndef(tprTriplet.first.getMaterialCat()) ? tprTriplet.first.getMaterialCat() : TokenManager.materialCat);
                    //allowedForThisStep = TPRBlockStatusHelper.allowCurrentStep(conn, vehicleId, tprTriplet != null ? tprTriplet.first : null, Misc.getUndefInt(), workStationType, vehicleId,true,false);
                    tpStep = TPRInformation.getTpStep(conn, tprTriplet.first, workStationType, workStationId, userId);
                    retval = new Triple<Token, TPRecord, TPRBlockManager>(token, tprTriplet.first, tprBlockManager);
                    if(tagListener != null){
                    	if(initTPR)
                    		tagListener.manageTag(readerId, conn, token, tprTriplet.first, tpStep, tprBlockManager, true, true);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            destroyIt = true;
        } finally {
        	try{
        		if(sb != null)
        			System.out.println(sb.toString());
        	DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
            //resume();
        }
        return retval;
    }
    private static String getBlockingReason(ArrayList<TPRBlockEntry> blockingEntries, Connection conn,TPRecord tpr){
    	String retval = "";
    	try{
    		for(int i=0,is=blockingEntries==null? 0 : blockingEntries.size();i<is;i++){
    			TPRBlockEntry tprBlockEntry = blockingEntries.get(i);
    			if(tprBlockEntry == null)
    				continue;
    			BlockingInstruction bInstruction = (BlockingInstruction) RFIDMasterDao.get(conn, BlockingInstruction.class, tprBlockEntry.getInstructionId());
    			if(bInstruction == null)
    				continue;
    			String blockStr = (bInstruction.getType() == Type.BlockingInstruction.BLOCK_DUETO_STEP_JUMP ? "Skip "+ Type.WorkStationType.getString(tpr.getNextStepType()) : Type.BlockingInstruction.getBlockingStr(bInstruction.getType()));
    			if(retval.length() > 0){
    				if(retval.contains(blockStr))
    					continue;
    				retval += "&nbsp;&nbsp;";
    			}
    			//retval += blockStr;
    			retval += "<span style='background-color: red;color:white; margin-left:5px; padding:5px; font-size:16pt;'>&nbsp;"+blockStr+"&nbsp;";
    			/*if(tprBlockEntry.getType() == TPRBlockStatusHelper.BLOCK_STEP && tprBlockEntry.getCreateType() == TPRBlockEntry.CREATE_TYPE_AUTO)
    				retval += "<br>&nbsp;Skip "+ Type.WorkStationType.getString(tpr.getNextStepType())+"&nbsp;";*/
    			retval += "</span>";
    		}
    		if(retval != null && retval.length() > 0){
    			retval = "<html><body>"+retval+"</body></html>";
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return retval;
    }
    public void clearData(byte[] epc, int attempt) {
        try {
            if (RFIDMaster.getReader(0) != null) {
                //pause();
                RFIDMaster.getReader(0).clearData(epc, 5);
                //resume();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static String getTprBlockStatus(TPRecord tprRecord, BlockingInstruction bIns, boolean add){
    	String retval = "";
    	Connection conn = null;
		boolean destroyIt = false;
		if(tprRecord == null || bIns == null)
    		return "";
		try{
    	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    	TPRBlockEntry tprBlockEntry = new TPRBlockEntry();
    	tprBlockEntry.setTprId(tprRecord.getTprId());
    	tprBlockEntry.setType(0);
    	tprBlockEntry.setStatus(1);
    	tprBlockEntry.setWorkstationTypeId(TokenManager.currWorkStationType);
    	tprBlockEntry.setInstructionId(bIns.getId());
    	tprBlockEntry.setCreatedBy(TokenManager.userId);
    	tprBlockEntry.setCreatedOn(new Date());
    	tprBlockEntry.setCreateType(0);
    	int id = Misc.getUndefInt();
    	for(int i=0,is=tprRecord.getBlockingEntries() == null ? 0 : tprRecord.getBlockingEntries().size();i<is;i++){
    		if(tprBlockEntry.getInstructionId() == tprRecord.getBlockingEntries().get(i).getInstructionId()){
    			id =i;
    			break;
    		}
    	}
    	if(add){
			if(Misc.isUndef(id)){
				if(tprRecord.getBlockingEntries() == null){
					tprRecord.setBlockingEntries(new ArrayList<TPRBlockEntry>());
				}
				tprRecord.getBlockingEntries().add(tprBlockEntry);
			}
		}else{
			if(!Misc.isUndef(id) && tprRecord.getBlockingEntries() != null)
				tprRecord.getBlockingEntries().remove(id);
		}
    	
    	retval = getBlockingReason(tprRecord.getBlockingEntries(), conn, tprRecord);
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
}

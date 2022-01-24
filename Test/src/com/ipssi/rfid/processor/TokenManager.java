package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.Workstate;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.ui.secl.controller.PropertyManager;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;

public class TokenManager {
	public static final int NORMAL = 0;
	public static final int STRICT_WITH_STEP_BLOCKING = 1;
	public static final int STRICT_WITH_PAPER_BLOCKING = 2;
	public static final int STRICT_WITH_WEB_BLOCKING = 3;
	private static ConcurrentHashMap<String, Token> rfidTokenMap = new ConcurrentHashMap<String, Token>();
	private static Object locker = new Object();
	private static boolean isBusy = false;
	public static long refreshInterval = 10 * 1000;
	private static long minTokenGap = 30 * 60 * 1000;
	private static String lastEpc;
	private static Token lastToken = null;
	private static long updatedOn = Misc.getUndefInt();
	private static int readerId = Misc.getUndefInt();
	private static int status = Workstate.IDLE;
	public static int nextWorkStationType = Misc.getUndefInt();
	public static int prevWorkStationType = Misc.getUndefInt();
	public static int currWorkStationType = Misc.getUndefInt();
	public static int currWorkStationId = Misc.getUndefInt();
	public static int morphoDeviceExist = Misc.getUndefInt();
	public static int morphoAPIType = Misc.getUndefInt();
    public static int maxTareDays = 15;
	public static int Printer_Connected = Misc.getUndefInt();
	public static int Weightment_Printer_Connected = 1;
	public static boolean createNewTPR = false;
	public static boolean closeTPR = false;
	public static int portNodeId = 816;
	public static int applicationMode = NORMAL;
	public static long fitnessExpiaryThreshold;
	public static long roadPermitExpiaryThreshold;
	public static long insauranceExpiaryThreshold;
	public static long polutionExpiaryThreshold;
	public static int userId = Misc.getUndefInt();
	public static String userName = "";
	public static int nextStationNumber = 5;
	private static long threshold = 15*24*60*60*1000;
	public static int noOfStation = 2;
	public static int lastStationCount = 0;
	public static int materialCat = Type.TPRMATERIAL.COAL;
	public static String weight_val = "";
	public static long morphoSyncFreq = 30*60*1000;
	public static long clockSyncFreq = 30*60*1000;
	public static int maxTagDataRetry = 1;
	public static boolean useSmartRFRead = false;
	public static boolean syncClock = false;
	public static String systemDateFormat = null;
	public static boolean isDebug = false;
	public static boolean isDebugReadings = false;
	public static boolean isSimulateWB = false;
	public static long weighBridgeTimeout = 10*1000;
	public static boolean forceManual = false;
    public static HashMap<Integer,Integer> isManualEntry  =  new HashMap<Integer,Integer>();
    public static boolean checkSyncReg = false;
	public static boolean useSECLRFIDReaderProcess=false;
	
	
	//station config params
	public static String stationCode="001";
	public static String stationChallanSeries="0";
	public static boolean workingModeServer=true;
	public static int minesId=Misc.getUndefInt();
                
//	 HashMap<Integer,String> hm=new HashMap<Integer,String>();  
	public static long ingnoreThreshold = 5*60*1000;
	
	public static boolean isTokenAvailable(Connection conn, String epcId) {
		return isTokenAvailable(conn, epcId,null);
	}
	public static boolean isTokenAvailable(Connection conn, String epcId, String vehicleName) {
		boolean retval = false;
		if (Utils.isNull(epcId) && !Utils.isNull(vehicleName)) {
			epcId = vehicleName;
		}
		if(Utils.isNull(epcId))
			return false;
		if (status != Workstate.BUSY) {
			if (!Utils.isNull(epcId)) {
				//new 
				Token lastToken = rfidTokenMap.get(epcId);
				long lastUpdatedOn = lastToken != null ? lastToken.getLastProcessed() : Misc.getUndefInt();
				long t = (System.currentTimeMillis() - lastUpdatedOn);
				if (Misc.isUndef(lastUpdatedOn) || t >= minTokenGap || status == Workstate.CLEAR) {
					retval = true;
				}
				//end new
				//start prev
				/*long lastUpdatedOn = rfidTokenMap.containsKey(epcId) ? rfidTokenMap.get(epcId).getLastProcessed() : updatedOn;
				if (!epcId.equalsIgnoreCase(lastEpc)) {
					long t = (System.currentTimeMillis() - lastUpdatedOn);
					if (Misc.isUndef(lastUpdatedOn) || t >= refreshInterval || status == Workstate.CLEAR) {
						retval = true;
					}
				} else {
					if (!Misc.isUndef(lastUpdatedOn)) {
						long t = (System.currentTimeMillis() - lastUpdatedOn);
						if (status == Workstate.CLEAR || t >= minTokenGap) {
							retval = true;
						}
					} else {
						retval = true;
					}
				}*/
				//end prev
			}
		} else {
			//LoggerNew.Write("[RFID TOKEN]:" + epcId + ",workstation busy");
		}
		return retval;
	}
	public static Token createToken(Connection conn, String epcId) {
		return createToken(conn, epcId, null);
	}
	public static Token createToken(Connection conn, String epcId,String vehicleName){
		return createToken(conn,epcId,vehicleName,false);
	}
	public static Token createToken(Connection conn, String epcId,String vehicleName ,boolean isManualEntry) {
		Token token = null;
		long issueTimeStamp = System.currentTimeMillis();
		int vehicleId = Misc.getUndefInt();
		Pair<Integer, String> vehiclePair = null;
		boolean readVehicleFromTag = true;
		synchronized (locker) {
			try {
				while(isBusy)
					locker.wait();
				isBusy = true;
				//if manual entry then return last token
				if(isManualEntry){
					returnToken(conn,lastToken);
				}
				if (isTokenAvailable(conn,vehicleName, epcId) || isManualEntry ) {
					vehiclePair = TPRInformation.getVehicle(conn, epcId, vehicleName);
					if (Utils.isNull(epcId) && !Utils.isNull(vehicleName)) {
						epcId = vehicleName;
						readVehicleFromTag = false;
					}
					if(Utils.isNull(epcId))
						return null;
					if(vehiclePair != null){
						vehicleId = vehiclePair.first;
						vehicleName = vehiclePair.second;
					}
					System.out.println("TOKEN : " +vehicleId);
					token = rfidTokenMap.get(epcId);
					if (token == null) {//first token for that vehicle
						token = new Token(vehicleId, epcId);
						token.setVehicleName(vehicleName);
						rfidTokenMap.put(epcId, token);
					}
					token.setVehicleId(vehicleId);
					token.setVehicleName(vehicleName);
					token.setEpcId(epcId);
					token.setStatus(Status.Token.ASSIGNED);
					token.setLastSeen(issueTimeStamp);
					token.setReadFromTag(readVehicleFromTag);
					status = Status.Workstate.BUSY;
					lastEpc = token.getEpcId();
					updatedOn = issueTimeStamp;
					lastToken = token;
					System.out.println("[RFID TOKEN ISSUE]:" + epcId);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}finally{
				isBusy = false;
				locker.notifyAll();
			}
		}		
		return token;
	}

	public static void returnToken(Connection conn, Token token) {
		//synchronized (locker) {
			try{
			//	while(isBusy)
				//	locker.wait();
				status = Workstate.IDLE;
				if (token == null || Utils.isNull(token.getEpcId())) {
					return;
				}
				if (rfidTokenMap.get(token.getEpcId()) == null) {
					rfidTokenMap.put(token.getEpcId(), token);
					rfidTokenMap.get(token.getEpcId()).setLastSeen(System.currentTimeMillis());
				}
				rfidTokenMap.get(token.getEpcId()).setLastProcessed(System.currentTimeMillis());
				rfidTokenMap.get(token.getEpcId()).setStatus(Status.Token.PROCESSED);
				System.out.println("[RFID TOKEN Return]:" + token.getEpcId());
				token.setStatus(Status.Token.PROCESSED);
			}catch(Exception ex){
				ex.printStackTrace();
			}finally{
				isBusy = false;
//				locker.notifyAll();
			}
		//}
	}

	synchronized public static void clearWorkstation() {
		status = Workstate.CLEAR;
		if (!Utils.isNull(lastEpc) && rfidTokenMap.get(lastEpc) != null) {
			//rfidTokenMap.get(lastEpc).setLastProcessed(System.currentTimeMillis());
			rfidTokenMap.get(lastEpc).setStatus(Status.Token.PROCESSED);
		}
		System.out.println("[RFID TOKEN Clear]:" + lastEpc);
	}
	public static void initSystemConfig(){
		try{
			Properties config = PropertyManager.getProperty(PropertyType.System);
			if(config == null)
				return;
			minesId= Misc.getParamAsInt(config.getProperty("MINES"));
			stationCode= Misc.getParamAsString(config.getProperty("SYNC_CLOCK"),"000");
			stationChallanSeries=Misc.getParamAsString(config.getProperty("SYNC_CLOCK"),"00");
			workingModeServer="1".equalsIgnoreCase(config.getProperty("SERVER_CONNECTED"));
			
			currWorkStationId  = Misc.getParamAsInt(config.getProperty("WORKSTATION_ID"));
			syncClock = Misc.getParamAsInt(config.getProperty("SYNC_CLOCK")) == 1;
			systemDateFormat = Misc.getParamAsString(config.getProperty("SYSTEM_DATE_FORMAT"),"dd-MM-yyyy HH:mm:ss");
			clockSyncFreq  = Misc.getParamAsInt(config.getProperty("CLOCK_SYNC_FREQ"));
			
			if(clockSyncFreq > 0)
				clockSyncFreq *=  1000;
			else
				clockSyncFreq	= 30 * 60 * 1000;
			isDebug   = Misc.getParamAsInt(config.getProperty("DEBUG")) == 1;
			isSimulateWB   = Misc.getParamAsInt(config.getProperty("SIMULATE_WB")) == 1;
			forceManual = Misc.getParamAsInt(config.getProperty("FORCE_MANUAL")) == 1;
			checkSyncReg = Misc.getParamAsInt(config.getProperty("CHECK_SYNC_REG")) == 1;
			portNodeId = Misc.getParamAsInt(config.getProperty("PORT_NODE_ID"),portNodeId);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static boolean isLastProcessedByUser(String epcId, int vehicleId){
		Token token =  rfidTokenMap.get(epcId);
		return ( token != null 
				&& (
						(
								token.getEpcId() != null && token.getEpcId().equalsIgnoreCase(epcId)) 
								|| (token.getVehicleId() == vehicleId)
						) 
						&& (token.getLastProcessed() > 0  && (System.currentTimeMillis() - token.getLastProcessed()) < minTokenGap )
				);
	}
	public static String getNextStationSuffix(){
		TokenManager.nextStationNumber = TokenManager.nextStationNumber++ % TokenManager.noOfStation;
		return TokenManager.nextStationNumber+"";
	}
	public static boolean useSDK(){
		return morphoAPIType == 1;
	}
	public static void initSystemConfigNew() {
		try{
			Properties config = PropertyManager.getProperty(PropertyType.System);
			if(config == null)
				return;
			minesId= Misc.getParamAsInt(config.getProperty("MINES"));
			stationCode= Misc.getParamAsString(config.getProperty("SYNC_CLOCK"),"000");
			stationChallanSeries=Misc.getParamAsString(config.getProperty("SYNC_CLOCK"),"00");
			workingModeServer="1".equalsIgnoreCase(config.getProperty("SERVER_CONNECTED"));
			
			currWorkStationId  = Misc.getParamAsInt(config.getProperty("WORKSTATION_ID"));
			syncClock = Misc.getParamAsInt(config.getProperty("SYNC_CLOCK")) == 1;
			systemDateFormat = Misc.getParamAsString(config.getProperty("SYSTEM_DATE_FORMAT"),"dd-MM-yyyy HH:mm:ss");
			clockSyncFreq  = Misc.getParamAsInt(config.getProperty("CLOCK_SYNC_FREQ"));
			isSimulateWB   = "1".equalsIgnoreCase(PropertyManager.getPropertyVal(PropertyType.WeighBridge, "SIMULATE_WB"));
			if(clockSyncFreq > 0)
				clockSyncFreq *=  1000;
			else
				clockSyncFreq	= 30 * 60 * 1000;
			isDebug   = Misc.getParamAsInt(config.getProperty("DEBUG")) == 1;
			forceManual = Misc.getParamAsInt(config.getProperty("FORCE_MANUAL")) == 1;
			checkSyncReg = Misc.getParamAsInt(config.getProperty("CHECK_SYNC_REG")) == 1;
			portNodeId = Misc.getParamAsInt(config.getProperty("PORT_NODE_ID"),portNodeId);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static Token getLastToken(String epc){
		Token lastToken = rfidTokenMap.get(epc);
		try {
			return lastToken == null ? null : (Token)lastToken.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void enbaleToken(boolean isbusy){
		status = isbusy ?  Workstate.BUSY : Workstate.IDLE;
	}
}

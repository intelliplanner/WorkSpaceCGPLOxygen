package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.connection.ConfigUtility;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.Workstate;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant.COLUR;

public class TokenManager {

	// public static long screenClearInterval = 120;
	// public static int count = 1;
	public static final int NORMAL = 0;
	public static final int STRICT_WITH_STEP_BLOCKING = 1;
	public static final int STRICT_WITH_PAPER_BLOCKING = 2;
	public static final int STRICT_WITH_WEB_BLOCKING = 3;
	// public static String HTTP_PORT = "50000"; //50200
	// public static String HTTPS_PORT = "50001"; //50200
	public static String HTTPS_PORT = "50001";
	public static String SAP_INVOICE_CREATION_URL = "http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_ppgcl_vts/400/zws_inv_ppgcl_vts/zvts_serv_binding";
	public static String SAP_INVOICE_CANCELLATION_URL = "http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_cancel_ppgcl_vts/400/zws_inv_cancel_ppgcl_vts/zvts_serv_binding";
	public static String SELECTED_COLOR = COLUR.BLUE.toString();
	public static String SAP_USERNAME = null;
	public static String SAP_PASSWORD = null;
	public static boolean IS_AUTO_COMPLETE_ON = false;
	public static boolean gatePassPrinterConnected = false;
	private static ConcurrentHashMap<String, Token> rfidTokenMap = new ConcurrentHashMap<String, Token>();
	private static Object locker = new Object();
	public static long refreshInterval = 10 * 1000;
	private static long minTokenGap = 30 * 60 * 1000;
	private static String lastEpc;
	private static Token lastToken = null;
	private static long updatedOn = Misc.getUndefInt();
	private static int readerId = Misc.getUndefInt();
	private static int status = Workstate.IDLE;
	public static int srcType = 1;
	public static int nextWorkStationType = Misc.getUndefInt();
	public static int prevWorkStationType = Misc.getUndefInt();
	public static int currWorkStationType = Misc.getUndefInt();
	public static int currWorkStationId = Misc.getUndefInt();
	// public static long maxTareDays = Misc.getUndefInt();
	// public static int Printer_Connected = Misc.getUndefInt();
	// public static int Weightment_Printer_Connected = 1;
	public static boolean createNewTPR = false;
	public static boolean closeTPR = false;
	public static int portNodeId = 3;
	public static int applicationMode = NORMAL;
	public static int userId = Misc.getUndefInt();
	public static String userName = "";
	public static int nextStationNumber = 5;
	private static long threshold = 15 * 24 * 60 * 60 * 1000;
	public static int noOfStation = 2;
	public static int lastStationCount = 0;
	public static int materialCat = Type.TPRMATERIAL.FLYASH_CGPL;
	public static String weight_val = "0";
	public static long clockSyncFreq = 30 * 60 * 1000;
	public static int maxTagDataRetry = 1;
	public static boolean useSmartRFRead = false;
	public static boolean syncClock = false;
	public static String systemDateFormat = null;
	public static boolean isDebug = false;
	public static boolean tagIdentifyManually = false;
	public static boolean isDebugReadings = false;
	public static long weighBridgeTimeout = 10 * 1000;
	public static boolean forceManual = false;
	public static boolean checkSyncReg = false;
	public static long min_weight = 15000;
	public static long max_weight = 30000;
	public static long save_timing = 2;
	public static long remaining_quatitity_limit = 0;
	public static int sameStationThreshold = 150;
	public static HashMap<String, Pair<Integer, Integer>> randomQC = new HashMap<String, Pair<Integer, Integer>>();
	public static int randomCheckLotSize = 4;
	public static boolean isRandomQcAlertPlay = false;
	public static int systemId = 1;
	// HashMap<Integer,String> hm=new HashMap<Integer,String>();
	// public static boolean isAutoSave = false;
	// public static boolean isAutoClear = false;
	public static String HSN_NO = "26219000";
	public static long tprSyncFreq = 60 * 1000;

	public static String DB_HOST = "";
	public static String DB_PORT = "";
	public static String DB_USER_NAME = "";
	public static String DB_NAME = "";
	public static String DB_PASSWORD = "";
	public static String DB_MAX_CONNECTION = "";

	public static int WEIGHBRIDGE_ONE_PORT;
	public static int WEIGHBRIDGE_TWO_PORT;
	public static int WEIGHBRIDGE_THREE_PORT;
	public static int WEIGHBRIDGE_FOUR_PORT;
	public static String WEIGHBRIDGE_ONE_HOST = "";
	public static String WEIGHBRIDGE_TWO_HOST = "";
	public static String WEIGHBRIDGE_THREE_HOST = "";
	public static String WEIGHBRIDGE_FOUR_HOST = "";

	public static boolean isTokenAvailable(Connection conn, String epcId) {
		return isTokenAvailable(conn, epcId, null);
	}

	public static boolean isTokenAvailable(Connection conn, String epcId, String vehicleName) {
		boolean retval = false;
		if (Utils.isNull(epcId) && !Utils.isNull(vehicleName)) {
			epcId = vehicleName;
		}
		if (Utils.isNull(epcId)) {
			return false;
		}
		if (status != Workstate.BUSY) {
			if (!Utils.isNull(epcId)) {
				// new
				long lastUpdatedOn = rfidTokenMap.containsKey(epcId) ? rfidTokenMap.get(epcId).getLastProcessed()
						: Misc.getUndefInt();
				long t = (System.currentTimeMillis() - lastUpdatedOn);
				if (Misc.isUndef(lastUpdatedOn) || t >= minTokenGap || status == Workstate.CLEAR) {
					retval = true;
				}
				// end new
				// start prev
				/*
				 * long lastUpdatedOn = rfidTokenMap.containsKey(epcId) ?
				 * rfidTokenMap.get(epcId).getLastProcessed() : updatedOn; if
				 * (!epcId.equalsIgnoreCase(lastEpc)) { long t = (System.currentTimeMillis() -
				 * lastUpdatedOn); if (Misc.isUndef(lastUpdatedOn) || t >= refreshInterval ||
				 * status == Workstate.CLEAR) { retval = true; } } else { if
				 * (!Misc.isUndef(lastUpdatedOn)) { long t = (System.currentTimeMillis() -
				 * lastUpdatedOn); if (status == Workstate.CLEAR || t >= minTokenGap) { retval =
				 * true; } } else { retval = true; } }
				 */
				// end prev
			}
		} else {
			// LoggerNew.Write("[RFID TOKEN]:" + epcId + ",workstation busy");
		}
		return retval;
	}

	public static Token createToken(Connection conn, String epcId) {
		return createToken(conn, epcId, null);
	}

	public static Token createToken(Connection conn, String epcId, String vehicleName) {
		Token token = null;
		long issueTimeStamp = System.currentTimeMillis();
		int vehicleId = Misc.getUndefInt();
		Pair<Integer, String> vehiclePair = null;
		boolean readVehicleFromTag = true;
		try {
			synchronized (locker) {
				if (isTokenAvailable(conn, vehicleName, epcId)) {
					vehiclePair = TPRInformation.getVehicle(conn, epcId, vehicleName);
					if (Utils.isNull(epcId) && !Utils.isNull(vehicleName)) {
						epcId = vehicleName;
						readVehicleFromTag = false;
					}
					if (Utils.isNull(epcId)) {
						return null;
					}
					if (vehiclePair != null) {
						vehicleId = vehiclePair.first;
						vehicleName = vehiclePair.second;
					}
					System.out.println("TOKEN : " + vehicleId);
					token = rfidTokenMap.get(epcId);
					if (token == null) {// first token for that vehicle
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
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return token;
	}

	public static void returnToken(Connection conn, Token token) {
		try {
			synchronized (locker) {
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
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void clearWorkstation() {
		status = Workstate.CLEAR;
		synchronized (locker) {
			if (!Utils.isNull(lastEpc) && rfidTokenMap.get(lastEpc) != null) {
				rfidTokenMap.get(lastEpc).setStatus(Status.Token.PROCESSED);
			}
			System.out.println("[RFID TOKEN Clear]:" + lastEpc);
		}
	}

	public static void initSystemConfig() {
		try {
			Properties config = ConfigUtility.getSystemConfiguration();
			if (config == null) {
				return;
			}
			syncClock = Misc.getParamAsInt(config.getProperty("SYNC_CLOCK")) == 1;
			systemDateFormat = Misc.getParamAsString(config.getProperty("SYSTEM_DATE_FORMAT"), "dd-MM-yyyy HH:mm:ss");
			clockSyncFreq = Misc.getParamAsInt(config.getProperty("CLOCK_SYNC_FREQ"));
			if (clockSyncFreq > 0) {
				clockSyncFreq *= 1000;
			} else {
				clockSyncFreq = 30 * 60 * 1000;
			}
			isDebug = Misc.getParamAsInt(config.getProperty("DEBUG")) == 1;
			checkSyncReg = Misc.getParamAsInt(config.getProperty("CHECK_SYNC_REG")) == 1;
			systemId = Misc.getParamAsInt(config.getProperty("SYSTEM_ID"));
			portNodeId = Misc.getParamAsInt(config.getProperty("PORT_NODE_ID"), 3);
			SAP_USERNAME = Misc.getParamAsString(config.getProperty("SAP_USERNAME"), "RFIDUSER");
			SAP_PASSWORD = Misc.getParamAsString(config.getProperty("SAP_PASSWORD"), "Tata@123");
			IS_AUTO_COMPLETE_ON = Misc.getParamAsInt(config.getProperty("AUTO_COMPLETE_ON_OFF")) == 0;
			// HTTP_PORT =
			// Misc.getParamAsString(config.getProperty("SAP_HTTP_PORT"),"50000");
			// HTTPS_PORT =
			// Misc.getParamAsString(config.getProperty("SAP_HTTPS_PORT"),"50001");
			SAP_INVOICE_CREATION_URL = Misc.getParamAsString(config.getProperty("SAP_INVOICE_CREATION_URL"),
					"http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_ppgcl_vts/400/zws_inv_ppgcl_vts/zvts_serv_binding");
			SAP_INVOICE_CANCELLATION_URL = Misc.getParamAsString(config.getProperty("SAP_INVOICE_CANCELLATION_URL"),
					"http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_cancel_ppgcl_vts/400/zws_inv_cancel_ppgcl_vts/zvts_serv_binding");
			tagIdentifyManually = Misc.getParamAsInt(config.getProperty("TAG_READ_TYPE")) == 1;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void initDatabaseConfig() {
		try {
			Properties config = ConfigUtility.getDBConfiguration();
			if (config == null) {
				return;
			}
			DB_HOST = Misc.getParamAsString(config.getProperty("desktop.DBConn.host"), "localhost");
			DB_PORT = Misc.getParamAsString(config.getProperty("desktop.DBConn.port"), "1433");
			DB_NAME = Misc.getParamAsString(config.getProperty("desktop.DBConn.Database"), "ipssi_cgpl");
			DB_USER_NAME = Misc.getParamAsString(config.getProperty("desktop.DBConn.userName"), "root");
			DB_PASSWORD = Misc.getParamAsString(config.getProperty("desktop.DBConn.password"), "root");
			DB_MAX_CONNECTION = Misc.getParamAsString(config.getProperty("desktop.DBConn.maxConnection"), "5");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void initConfig(String sufix, int workStationType) {
		try {
			Properties config = ConfigUtility.getWorkStationConfiguration(sufix, workStationType);
			if (config != null) {
				nextWorkStationType = Misc.getParamAsInt(config.getProperty("NEXT_WORK_STATION_TYPE"));
				prevWorkStationType = Misc.getParamAsInt(config.getProperty("PREV_WORK_STATION_TYPE"));
				currWorkStationType = Misc.getParamAsInt(config.getProperty("WORK_STATION_TYPE"));
				currWorkStationId = Misc.getParamAsInt(config.getProperty("WORK_STATION_ID"));
				refreshInterval = Misc.getParamAsLong(config.getProperty("REFRESH_INTERVAL"));
				weighBridgeTimeout = Misc.getParamAsLong(config.getProperty("WB_TIMEOUT"));
				weight_val = config.getProperty("WEIGHT");
				max_weight = Misc.getParamAsLong(config.getProperty("MAX_WEIGHT"));
				min_weight = Misc.getParamAsLong(config.getProperty("MIN_WEIGHT"));
				save_timing = Misc.getParamAsLong(config.getProperty("SAVE_TIME"));
				sameStationThreshold = Misc.getParamAsInt(config.getProperty("SAME_STATION_TPR_THRESHOLD"));
				remaining_quatitity_limit = Misc.getParamAsLong(config.getProperty("REMAINING_QUANTITY_LIMIT"));
				// isAutoSave = Misc.getParamAsInt(config.getProperty("AUTO_SAVE")) == 1;
				// isAutoClear = Misc.getParamAsInt(config.getProperty("AUTO_CLEAR")) == 1;
				// screenClearInterval =
				// Misc.getParamAsLong(config.getProperty("SCREEN_CLEAR_INTERVAL"), 180);
				TPRInformation.setSameStationTprThresholdMinutes(sameStationThreshold);

				if (save_timing > 0) {
					save_timing *= 1000;
				} else {
					save_timing = 2 * 1000;
				}

				if (refreshInterval > 0) {
					refreshInterval *= 1000;
				} else {
					refreshInterval = 10 * 1000;
				}
				if (weighBridgeTimeout > 0) {
					weighBridgeTimeout *= 1000;
				} else {
					weighBridgeTimeout = 10 * 1000;
				}

				minTokenGap = Misc.getParamAsInt(config.getProperty("MIN_TOKEN_GAP"));
				if (minTokenGap > 0) {
					minTokenGap *= 1000;
				} else {
					minTokenGap = 30 * 60 * 1000;
				}
				maxTagDataRetry = Misc.getParamAsInt(config.getProperty("MAX_TAG_DATA_RETRY"));
				createNewTPR = Misc.getParamAsInt(config.getProperty("CREATE_NEW_TRIP")) == 1;
				closeTPR = Misc.getParamAsInt(config.getProperty("CLOSE_TRIP")) == 1;
				applicationMode = Misc.getParamAsInt(config.getProperty("APPLICATION_MODE"), 0);
				useSmartRFRead = Misc.getParamAsInt(config.getProperty("USE_SMART_RF_READ")) == 1;
				isRandomQcAlertPlay = Misc.getParamAsInt(config.getProperty("RANDOM_QC_ALERT_PLAY")) == 1;
				forceManual = Misc.getParamAsInt(config.getProperty("FORCE_MANUAL")) == 1;
				gatePassPrinterConnected = Misc.getParamAsInt(config.getProperty("GATE_PASS_PRINTER_CONNECTED")) == 1;
				nextStationNumber = 5;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isLastProcessedByUser(String epcId, int vehicleId) {
		Token token = rfidTokenMap.get(epcId);
		return (token != null
				&& ((token.getEpcId() != null && token.getEpcId().equalsIgnoreCase(epcId))
						|| (token.getVehicleId() == vehicleId))
				&& (token.getLastProcessed() > 0
						&& (System.currentTimeMillis() - token.getLastProcessed()) < minTokenGap));
	}

	public static String getNextStationSuffix() {
		TokenManager.nextStationNumber = TokenManager.nextStationNumber++ % TokenManager.noOfStation;
		return TokenManager.nextStationNumber + "";
	}

	public static void initWeighBridgeConfig() {
		try {
			Properties config = ConfigUtility.getWeighBridgeConfigurationNew();
			if (config != null) {
				WEIGHBRIDGE_ONE_HOST = config.getProperty("WEIGHBRIDGE_ONE_HOST", "192.168.1.1");
				WEIGHBRIDGE_TWO_HOST = config.getProperty("WEIGHBRIDGE_TWO_HOST", "192.168.1.2");
				WEIGHBRIDGE_THREE_HOST = config.getProperty("WEIGHBRIDGE_THREE_HOST", "192.168.1.3");
				WEIGHBRIDGE_FOUR_HOST = config.getProperty("WEIGHBRIDGE_FOUR_HOST", "192.168.1.4");
				WEIGHBRIDGE_ONE_PORT = Misc.getParamAsInt(config.getProperty("WEIGHBRIDGE_ONE_PORT"), 1);
				WEIGHBRIDGE_TWO_PORT = Misc.getParamAsInt(config.getProperty("WEIGHBRIDGE_TWO_PORT"), 1);
				WEIGHBRIDGE_THREE_PORT = Misc.getParamAsInt(config.getProperty("WEIGHBRIDGE_THREE_PORT"), 1);
				WEIGHBRIDGE_FOUR_PORT = Misc.getParamAsInt(config.getProperty("WEIGHBRIDGE_FOUR_PORT"), 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

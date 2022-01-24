package com.ipssi.secldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class TxExecutor {
	public static String g_clientDateFormat = "yyyyMMddHHmmss";
	/**
	 * 
	 * @param conn
	 * @param data CSV of various format, first being cmd
	 * @return null if not a validTxCmd else result of TxCmd - see documents for different TxCmd
	 * @throws Exception 
	 */
	public static String processTxCmd(Connection conn, String data) throws Exception {
		String retval = null;
		
		if (data != null && data.startsWith("@@secl-cmd:")) {
			String cmd = data.substring(0, data.indexOf(','));
			cmd = cmd.substring("@@secl-cmd:".length());
			if ("GET_PO".equals(cmd)) {
				retval = TxExecutor.getPOInfo(conn, data);
			}
			else if ("SET_RFID".equals(cmd)) {
				retval = TxExecutor.setRFIDFromHH(conn, data);
			}
			else if ("PREP_GPS".equals(cmd)) {
				retval = TxExecutor.prepGPSExceptionScreen(conn, data);
			}
			else if ("PREP_RF".equals(cmd)) {
				retval = TxExecutor.prepReadAtGate(conn, data);
			}
			else if ("PREP_TARE".equals(cmd)) {
				retval = TxExecutor.prepTareScreen(conn, data); 
			}
			else if ("PREP_GOUT".equals(cmd)) {
				retval = TxExecutor.prepGateOutScreen(conn, data);
			}
			else if ("CREATE_RF".equals(cmd)) {
				retval = TxExecutor.createOrUpdateLoadRecord(conn, data);
			}
			else if ("UPD_GIN".equals(cmd)) {
				retval = TxExecutor.updateGateIn(conn, data);
			}
			else if ("UPD_GOUT".equals(cmd)) {
				retval = TxExecutor.updateGateOut(conn, data);
			}
			else if ("UPD_AIN".equals(cmd)) {
				retval = TxExecutor.updateAreaIn(conn, data);
			}
			else if ("UPD_AOUT".equals(cmd)) {
				retval = TxExecutor.updateAreaOut(conn, data);
			}
			else if ("UPD_GROSS".equals(cmd)) {
				retval = TxExecutor.updateGrossWt(conn, data);
			}
			else if ("UPD_TARE".equals(cmd)) {
				retval = TxExecutor.updateTareWt(conn, data);
			}
			else if ("UPD_COMMENT".equals(cmd)) {
				retval = TxExecutor.updateComments(conn, data);
			}
			else if ("UPD_WEIGHMENT".equals(cmd)) {
				retval = TxExecutor.createWeighmentRecord(conn, data);
			}
			else if ("UPD_TOKEN".equals(cmd)) {
				retval = TxExecutor.createTokenRecord(conn, data);
			}
		}
		return retval;
	}
//	vehicleName,lastSyncDate,criticality
	private static String prepGPSExceptionScreen(Connection conn, String data) {
		String retval = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date lastSyncDate = null;
		int criticality = Misc.getUndefInt();
		String dataParts[] = data == null ? null : data.split(",");
		if (dataParts == null || dataParts.length < 2 )
			return "";
		try{
		String stdName = CacheTrack.standardizeName(dataParts[1]);
		int vehId = CacheTrack.VehicleSetup.getSetupByStdName(stdName, conn);
		lastSyncDate = dataParts.length > 2 && dataParts[2] != null && dataParts[2].length() > 0? sdf.parse(dataParts[2]) : null;
		criticality = dataParts.length > 3 && dataParts[3] != null && dataParts[2].length() > 0 ? Misc.getParamAsInt(dataParts[3]) : Misc.getUndefInt();
		if(!Misc.isUndef(vehId))
			retval = DataRequester.populateTripDataForVehicle(conn, vehId, lastSyncDate, criticality);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	/**
	 * 
	 * @param conn
	 * @param data cmd, minesId
	 * @return CSV list of valid PO for the mines
	 * @throws SQLException
	 */
	public static String getPOInfo(Connection conn, String data) throws SQLException {
		String dataParts[] = data.split(",");
		int colIndex = 1;
		int id = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		StringBuilder sb = new StringBuilder();
		sb.append(" select ponumber from mines_ponumber where id = ? ");
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
		StringBuilder retval = new StringBuilder();
		while (rs.next()) {
			if (retval.length() != 0)
				retval.append(",");
			retval.append(rs.getString("ponumber"));
			
		}
		rs.close();
		ps.close();
		return retval.toString();
	}
	/**
	 * 
	 * @param conn
	 * @param data cmd, vehicleName, transporter, mines, loadGrossTS, loadGrossWt, loadTareWt, refPOId, challanNumber
	 * @return
	 * @throws Exception
	 */
	public static String setRFIDFromHH(Connection conn, String data) throws Exception {
		ExtChallanEntry params = ExtChallanEntry.readFromString(data, 1, false);
		Triple<Integer, ExtChallanEntry, Boolean> currEntry = TxExecutor.prepReadAtGate(conn, false, params.getVehicleNumber(), params.getTransporter(), params.getMines(), params.getLoadGrossTS(), params.getLoadGrossWt(), params.getLoadTareWt(), params.getRefPOId(), params.getChallanId(), params.getEpc());
		boolean toCreate = false;
		boolean toUpdate = false;
		//	     if id is not undef then remember and use it when saving
	    //     if id is undef, then look at Trip record - if it is not null then that trip has not been    
	    //           closed - prompt user to get comments why, save the comment with previous trip record 
	    //           then save using read Info from tag but as brand new
	    //     if third field is true ... then info on tag can be considered proper else improper


		if (currEntry == null) {
			toCreate = true;
		}
		else {
			if (currEntry.third) {
				if (!Misc.isUndef(currEntry.first)) {
					toUpdate = true;
				}
				else {
					toCreate = true;
				}
			}			
		}
		int vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(ExtChallanEntry.standardizeName(params.getVehicleNumber()), conn);
		int retval = Misc.getUndefInt();
		int userId = Misc.getUndefInt();
		if (toCreate) {
			retval = ExtChallanEntry.createLoadRecord(conn, true, params.getEpc(), null, params.getVehicleNumber(), vehicleId, params.getMines(), params.getTransporter(), params.getRefPOId(), params.getChallanId(), Misc.getUndefInt(), params.getLoadGrossTS(), params.getLoadTareWt(), params.getLoadGrossWt(), false, false, userId, params.getVehicleNumber(), params.getMines(), params.getTransporter(), params.getLoadTareWt(), params.getLoadGrossWt(), params.getRefPOId(), params.getChallanId(), params.getLoadGrossTS());			
		}
		else if (toUpdate) {
			retval = currEntry.first;
			ExtChallanEntry.updateLoadRecordAuto(conn, retval, params.getVehicleNumber(), vehicleId, params.getMines(), params.getTransporter(), params.getRefPOId(), params.getChallanId(), Misc.getUndefInt(), params.getLoadGrossTS(), params.getLoadTareWt(), params.getLoadGrossWt(), userId);	
		}
		return Integer.toString(retval);
	}
	/**
	 * 
	 * @param conn
	 * @param data  cmd, 1 or 0 for doingGate, CSV for ExtChallanEntry without Id having atleast vehicleName, transporter, mines, loadGrossTS, loadGrossWt, loadTareWt, refPOId, challanNumber
	 * @return csv for matchId, 1 or 0 for third param of prep, ExtChallanEntry
	 * @throws Exception 
	 */
	public static String prepReadAtGate(Connection conn, String data) throws Exception {
		String dataParts[] = data == null ? null : data.split(",");
		if (dataParts == null || dataParts.length < 3)
			return ",,";
		boolean doingGate = "1".equals(dataParts[1]);
		ExtChallanEntry params = ExtChallanEntry.readFromString(dataParts, 2, false);
		Triple<Integer, ExtChallanEntry, Boolean> retval = 
			prepReadAtGate(conn, doingGate, params.getVehicleNumber(), params.getTransporter(), params.getMines(), params.getLoadGrossTS(), params.getLoadGrossWt(), params.getLoadTareWt(), params.getRefPOId(), params.getChallanId(), params.getEpc())
			;
		StringBuilder sb = new StringBuilder();
		if (retval == null || Misc.isUndef(retval.first))
			sb.append(",");
		else
			sb.append(retval.first).append(",");
		sb.append(retval == null || !retval.third ? "0" : "1");
		sb.append(",");
		sb.append(retval == null || retval.second == null || Misc.isUndef(retval.second.getId()) ? "" : retval.second.toString());
		return sb.toString();
	}
	/**
	 * 
	 * @param conn
	 * @param data  cmd,vehicleName
	 * @return
	 * @throws Exception 
	 */
	public static String prepTareScreen(Connection conn, String data) throws Exception {
		String dataParts[] = data == null ? null : data.split(",");
		if (dataParts == null || dataParts.length < 2) {
			return ",";
		}
		Pair<Integer, ExtChallanEntry> retval = prepTareScreenInternal(conn, dataParts[1]);
		StringBuilder sb = new StringBuilder();
		if (retval == null || Misc.isUndef(retval.first))
			sb.append(",");
		else
			sb.append(retval.first).append(",");
		sb.append(retval == null || retval.second == null || Misc.isUndef(retval.second.getId()) ? "" : retval.second.toString());
		return sb.toString();
	}
	/**
	 * 
	 * @param conn
	 * @param data  cmd,vehicleName
	 * @return
	 * @throws Exception 
	 */
	public static String prepGateOutScreen(Connection conn, String data) throws Exception {
		String dataParts[] = data == null ? null : data.split(",");
		if (dataParts == null || dataParts.length < 2) {
			return ",";
		}
		Pair<Integer, ExtChallanEntry> retval = prepGateOutScreenInternal(conn, dataParts[1]);
		StringBuilder sb = new StringBuilder();
		if (retval == null || Misc.isUndef(retval.first))
			sb.append(",");
		else
			sb.append(retval.first).append(",");
		sb.append(retval == null || retval.second == null || Misc.isUndef(retval.second.getId()) ? "" : retval.second.toString());
		return sb.toString();
	}
	/**
	 * 
	 * @param conn
	 * @param data  cmd,systemCode, userId, refId, comments
	 * @return 1 or 0 depending upon succ
	 * @throws Exception 
	 */
	public static String updateComments(Connection conn, String data) {
		//String systemCode, int userId, int refId, String comments) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		int refId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		String comments = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		if (systemCode != null && systemCode.length() == 0)
		   systemCode = null;
		if (comments != null) 
			comments = ExtChallanEntry.readAdjustedString(comments);
		boolean retval = updateComments(conn, systemCode, userId, refId, comments);
		return retval ? "1" : "0";
	}
	/**
	 * 
	 * @param conn
	 * @param data CSV of cmd,systemCode,userId,1 or 0 for doingGate, 1 or 0 for auto,CSV for ExtChallan
	 *                  witht Id having atleast id, vehicleName, transporter, mines, loadGrossTS, loadGrossWt, loadTareWt, refPOId, challanNumber
	 * @return id of newly created record or newly updated record
	 */
	public static String createOrUpdateLoadRecord(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		boolean doingGate = 1 == Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		boolean doingAuto = 1 == Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		ExtChallanEntry params = ExtChallanEntry.readFromString(dataParts, colIndex, true);
		if (systemCode != null && systemCode.length() == 0)
		   systemCode = null;
		int retval = createOrUpdateLoadRecord(conn, systemCode, userId, doingGate, doingAuto, params.getId(), params.getVehicleNumber()
				, params.getTransporter(), params.getMines(), params.getLoadGrossTS(), params.getLoadGrossWt(), params.getLoadTareWt(), params.getRefPOId(), params.getChallanId(), params.getEpc()
				, params.getAutoVehicleName(), params.getAutoTransporter(), params.getAutoMines(), params.getAutoGrossWtDate(), params.getAutoLoadGrossWt(), params.getAutoLoadTareWt(), params.getAutoRefPOId(), params.getAutoChallanNumber(), params.getComments());
		return Integer.toString(retval);
	}
	/**
	 * 
	 * @param conn
	 * @param data CSV of cmd,systemCode,userId, refId, ts in yyyyMMddHHmmss
	 * @return 1 or 0 depending upon success
	 * @throws Exception 
	 */
	public static String updateGateIn(Connection conn, String data) throws Exception {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		int refId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		long ts = dataParts != null && dataParts.length > colIndex ?
				Misc.utilToLong(Misc.getParamAsDate(dataParts[colIndex], null, new SimpleDateFormat(g_clientDateFormat))) : Misc.getUndefInt();
		boolean retval = updateGateIn(conn, systemCode, userId, refId, ts);
		return retval ? "1" : "0";
	}
	/**
	 * 
	 * @param conn
	 * @param data CSV for cmd, systemCode, userId, refId, dt in yyyyMMddHHmmss format, grossWt, autoGrossWt, comments
	 * @return "1" or "0" dependig upon success
	 */
	public static String updateGrossWt(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		int refId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		long ts = dataParts != null && dataParts.length > colIndex ?
				Misc.utilToLong(Misc.getParamAsDate(dataParts[colIndex++], null, new SimpleDateFormat(g_clientDateFormat))) : Misc.getUndefInt();
	    double wt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    double autoWt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    if (Misc.isUndef(autoWt))
	    	autoWt = wt;
	    String comments = ExtChallanEntry.readAdjustedString(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    boolean retval = updateGrossWt(conn, systemCode, userId, refId, ts, wt, autoWt, comments);
	    return retval ? "1" : "0";
	}
	/**
	 * 
	 * @param conn
	 * @param data CSV of cmd,systemCode,userId, vehicleName, ts in yyyyMMddHHmmss
	 * @return 1 or 0 depending upon success
	 * @throws Exception 
	 */
	public static String updateAreaIn(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		String vehicleName = ExtChallanEntry.readAdjustedString(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		long ts = dataParts != null && dataParts.length > colIndex ?
				Misc.utilToLong(Misc.getParamAsDate(dataParts[colIndex], null, new SimpleDateFormat(g_clientDateFormat))) : Misc.getUndefInt();
		boolean retval = updateAreaIn(conn, systemCode, userId, vehicleName, ts);
		return retval ? "1" : "0";
	}
	/**
	 * 
	 * @param conn
	 * @param data CSV of cmd,systemCode,userId, vehicleName, ts in yyyyMMddHHmmss
	 * @return 1 or 0 depending upon success
	 * @throws Exception 
	 */
	public static String updateAreaOut(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		String vehicleName = ExtChallanEntry.readAdjustedString(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		long ts = dataParts != null && dataParts.length > colIndex ?
				Misc.utilToLong(Misc.getParamAsDate(dataParts[colIndex], null, new SimpleDateFormat(g_clientDateFormat))) : Misc.getUndefInt();
		boolean retval = updateAreaOut(conn, systemCode, userId, vehicleName, ts);
		return retval ? "1" : "0";

	}
	/**
	 * 
	 * @param conn
	 * @param data CSV for cmd, systemCode, userId, refId, dt in yyyyMMddHHmmss format, grossWt, autoGrossWt, toClose, comments
	 * @return "1" or "0" dependig upon success
	 */
	public static String updateTareWt(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		int refId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		long ts = dataParts != null && dataParts.length > colIndex ?
				Misc.utilToLong(Misc.getParamAsDate(dataParts[colIndex++], null, new SimpleDateFormat(g_clientDateFormat))) : Misc.getUndefInt();
	    double wt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    double autoWt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    if (Misc.isUndef(autoWt))
	    	autoWt = wt;
	    boolean toClose = "1".equals(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    String comments = ExtChallanEntry.readAdjustedString(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
	    boolean retval = updateTareWt(conn, systemCode, userId, refId, ts, wt, autoWt, toClose, comments);
	    return retval ? "1" : "0";
	}
	/**
	 * 
	 * @param conn
	 * @param data CSV of cmd,systemCode,userId, refId, ts in yyyyMMddHHmmss, toClose or toKeepOpen
	 * @return 1 or 0 depending upon success
	 * @throws Exception 
	 */	
	public static String updateGateOut(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
		int userId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		int refId = Misc.getParamAsInt(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		long ts = dataParts != null && dataParts.length > colIndex ?
				Misc.utilToLong(Misc.getParamAsDate(dataParts[colIndex], null, new SimpleDateFormat(g_clientDateFormat))) : Misc.getUndefInt();
		boolean toClose = !"0".equals(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null);
		boolean retval = updateGateOut(conn, systemCode, userId, refId, ts, toClose);
		return retval ? "1" : "0";
	}
	        		
	
	/**
	 * @param conn
	 * @param data  ExtChallanEntry - parsed from read of RFLabel in format cmd_name,vehicle_name,transporter,mine,gross_dt (yyyyMMddHHmmss), gross wt, tare wt,po_number,challan_number
    * @return Triple<Integer, ExtChallanEntry, Boolean> 
    *  1. id if record exists in DB for matching RF else undefInt, 
    *  2. Earlier trip if different from record passed 
    *  3. Tells usability of Prev Trip Record - see how to use   
    * How to use: Read tag or user enters vehicleNumber. Call prepReadAtGate 
    *  if the tag read had loadWtInfo (i.e you passed vehiclenumber and loading info) {
    *     if id is not undef then remember and use it when saving
    *     if id is undef, then look at Trip record - if it is not null then that trip has not been    
    *            closed - prompt user to get comments why, save the comment with previous trip record 
    *            then save using read Info from tag but as brand new
    *    if third field is true ... then info on tag has not already been close, else it is closed and must be discarded        
    *  }
    *  else {
    *     if Trip Record is non empty, and third field is false then prev trip was not closed
    *                                      prompt user to get comment save
    *               if third field is true - there exists a reasonable prev record 
    *               .. prompt user to confirm if to continue or clear. 
    *               .. if clear then clear the id, have user enter other details else pass id of record
    *     if trip record is empty then brand new entry needed                      
    *  }		
	 * @throws Exception 
	 */
	private static Triple<Integer, ExtChallanEntry, Boolean> prepReadAtGate(Connection conn, boolean doingGate, String vehicleName, String transporter, String mines, long grossWtDate, double grossWt, double tareWt, String poNumber, String challanNumber, String epc) throws Exception {
		ExtChallanEntry prev = vehicleName == null ? null : ExtChallanEntry.getLatestChallanEntry(conn, vehicleName, Misc.getUndefInt(), true, true, true, true, true);
		
		if (prev != null && (Misc.isUndef(prev.getId()))) {
			prev = null;
		}
		
		int matchId = Misc.getUndefInt();
		boolean suitableToUse = true;
		if (prev != null) {
			if (grossWtDate > 0) {
				//found a record for the same GrossWtDate
				if (ExtChallanEntry.guessIfLatestChallanEntryValid(conn, prev, ExtChallanEntry.OPS_NEW_RFID_RECORD, grossWtDate)) {					
					if (prev.getStatus() == ExtChallanEntry.IS_OPEN_REC) {
						matchId = prev.getId();
						suitableToUse = true;
						prev = null;
					}
					else {
						suitableToUse = false;
						prev = null;
					}
				}
				else {//else found a different record
					if (prev.getStatus() == ExtChallanEntry.IS_OPEN_REC && (prev.getUnloadGrossTS() <= 0 || prev.getUnloadTareTS() <= 0)) {
						//ask for why it was not closed
					}
					else {
						prev = null;
					}
					if (prev.getLoadGrossTS() < grossWtDate) {
						suitableToUse = true;
					}
					else {
						suitableToUse = false;
					}
				}				
			}
			else {//tag did not have any wt info ... check if something came automatically
				if (prev.getStatus() == ExtChallanEntry.IS_OPEN_REC && (prev.getUnloadGrossTS() < 0 )) {
					if (ExtChallanEntry.guessIfLatestChallanEntryValid(conn, prev, ExtChallanEntry.OPS_NEW_GATE_IN, System.currentTimeMillis())) {
						suitableToUse = true;
					}
					else {						
						suitableToUse = false; //prev trip needs to be closed
					}
				}
				else {
					prev = null;
					suitableToUse = false;
				}
			}
		}
		
		return new Triple<Integer, ExtChallanEntry, Boolean>(matchId, prev, suitableToUse);
	}
	/**
		 * 
		 * @param conn
		 * @param data
		 * @return Triple<Integer, ExtChallanEntry, Boolean> 
		 * 				 1. id if record exists in DB for matching RF else undefInt, 
		 *                  2. Earlier trip if not closed and different from record passed (if only vehicle was passed then it will be different) 
		 *                  3. if client passed only vehicle name then we also tell if the earlier trip being passed is suitable to be used in continuation
		 * @throws Exception 
 
		 */
	private static Pair<Integer, ExtChallanEntry> prepTareScreenInternal(Connection conn, String vehicleName) throws Exception {
		ExtChallanEntry prev = vehicleName == null ? null : ExtChallanEntry.getLatestChallanEntry(conn, vehicleName, Misc.getUndefInt(), true, true, true, true, false);
		if (prev != null && (Misc.isUndef(prev.getId()))) {
			prev = null;
		}
		int matchId = Misc.getUndefInt();
		boolean suitableToUse = false;
		if (prev != null) {
				if (ExtChallanEntry.guessIfLatestChallanEntryValid(conn, prev, ExtChallanEntry.OPS_TARE_WT, System.currentTimeMillis())) {
					matchId = prev.getId();
				}
		}
		return new Pair<Integer, ExtChallanEntry>(matchId, prev);
	}
	/**
		 * 
		 * @param conn
		 * @param data
		 * @return Triple<Integer, ExtChallanEntry, Boolean> 
		 * 				 1. id if record exists in DB for matching RF else undefInt, 
		 *                  2. Earlier trip if not closed and different from record passed (if only vehicle was passed then it will be different) 
		 *                  3. if client passed only vehicle name then we also tell if the earlier trip being passed is suitable to be used in continuation
		 * @throws Exception 
 
		 */
	private static Pair<Integer, ExtChallanEntry> prepGateOutScreenInternal(Connection conn, String vehicleName) throws Exception {
		ExtChallanEntry prev = vehicleName == null ? null : ExtChallanEntry.getLatestChallanEntry(conn, vehicleName, Misc.getUndefInt(), true, true, true, true, false);
		if (prev != null && (Misc.isUndef(prev.getId()))) {
			prev = null;
		}
		int matchId = Misc.getUndefInt();
		boolean suitableToUse = false;
		if (prev != null) {
				if (ExtChallanEntry.guessIfLatestChallanEntryValid(conn, prev, ExtChallanEntry.OPS_TARE_WT, System.currentTimeMillis())) {
					matchId = prev.getId();
				}
		}
		return new Pair<Integer, ExtChallanEntry>(matchId, prev);
	}
	
    private static boolean updateComments(Connection conn, String systemCode, int userId, int refId, String comments) {
    	boolean retval = true;
    	try {
    		ExtChallanEntry.updateComments(conn, userId, refId, comments);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    	}
        return retval;
    }
    /**
     * set auto=false if nothing was read from RF or usable id was not obtained in prepForRead
     * set refId to Misc.getUndefInt() if there is no previous record
     * set autoValues if auto=true and if there has been changes to respective values
     * the values to use to update
     * Call createOrUpdateLoadRecord at Gate or at Gross WB if there are changes or if refId was undef
     * Returns the id of the newly created record
     */
    private static int createOrUpdateLoadRecord(Connection conn, String systemCode, int userId, boolean doingGate, boolean auto, int refId, String vehicleName, String transporter, String mines, long grossWtDate, double grossWt, double tareWt, String poNumber, String challanNumber, String epc, String autoVehicleName, String autoTransporter, String autoMines, long autoGrossWtDate, double autoGrossWt, double autoTareWt, String autoPoNumber, String autoChallanNumber, String comments) {
    	int retval = Misc.getUndefInt();
    	try {
        	int vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(ExtChallanEntry.standardizeName(vehicleName), conn);
        	if (Misc.isUndef(refId)) {
        		retval = ExtChallanEntry.createLoadRecord(conn, auto, epc, systemCode, vehicleName, vehicleId, mines, transporter, poNumber, challanNumber, Misc.getUndefInt(), grossWtDate, tareWt, grossWt, false, false, userId, autoVehicleName, autoMines, autoTransporter, autoTareWt, autoGrossWt, autoPoNumber, autoChallanNumber, autoGrossWtDate);
        	}
        	else {
        		retval = refId;
        		ExtChallanEntry.updateLoadRecord(conn, refId, vehicleName, vehicleId, mines, transporter, poNumber, challanNumber, Misc.getUndefInt(), grossWtDate, tareWt, grossWt, userId, comments);
        	}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    	}
    	return retval;
		
	}

    private static boolean updateGateIn(Connection conn, String systemCode, int userId, int refId, long ts) throws Exception {
    	boolean retval = true;
    	try {
    		ExtChallanEntry.updateUnloadTimings(conn, refId,  ExtChallanEntry.OPS_NEW_GATE_IN,  ts, userId, systemCode, false);	
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    	}
        return retval;
    }
    private static boolean updateGrossWt(Connection conn, String systemCode, int userId, int refId, long dt, double grossWt, double autoGrossWt, String comments) {
    	boolean retval = true;
    	try {
    		ExtChallanEntry.updateUnload(conn, refId, false, dt, grossWt, autoGrossWt, false, userId, comments, systemCode, false);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    		
    	}
    	return retval;
	}
    private static boolean updateAreaIn(Connection conn, String systemCode, int userId, String vehicleName, long dt) {
    	boolean retval = true;
    	try {
    		int vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
    		ExtChallanEntry entry = ExtChallanEntry.getLatestChallanEntry(conn, vehicleName, vehicleId, true, false, false, false, false);
    		if (entry != null && !Misc.isUndef(entry.getId())) {
    			if (ExtChallanEntry.guessIfLatestChallanEntryValid(conn, entry, ExtChallanEntry.OPS_AREA_IN, System.currentTimeMillis()));
    			ExtChallanEntry.updateUnloadTimings(conn, entry.getId(),  ExtChallanEntry.OPS_AREA_IN,  dt, userId, systemCode, false);
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    	}
        return retval;
	}
    
    private static boolean updateAreaOut(Connection conn, String systemCode, int userId, String vehicleName, long dt) {
    	boolean retval = true;
    	try {
    		int vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
    		ExtChallanEntry entry = ExtChallanEntry.getLatestChallanEntry(conn, vehicleName, vehicleId, true, false, false, false, false);
    		if (entry != null && !Misc.isUndef(entry.getId())) {
    			if (ExtChallanEntry.guessIfLatestChallanEntryValid(conn, entry, ExtChallanEntry.OPS_AREA_OUT, System.currentTimeMillis()));
    			ExtChallanEntry.updateUnloadTimings(conn, entry.getId(),  ExtChallanEntry.OPS_AREA_OUT,  dt, userId, systemCode, false);
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    	}
        return retval;	
	}
    private static boolean updateTareWt(Connection conn, String systemCode, int userId, int refId, long dt, double grossWt, double autoGrossWt, boolean toClose, String comments) {
    	boolean retval = true;
    	try {
    		ExtChallanEntry.updateUnload(conn, refId, true, dt, grossWt, autoGrossWt, false, userId, comments, systemCode, toClose);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    		
    	}
    	return retval;
	}
    private static boolean updateGateOut(Connection conn, String systemCode, int userId, int refId, long dt, boolean toClose) {
    	boolean retval = true;
    	try {
    		ExtChallanEntry.updateUnloadTimings(conn, refId,  ExtChallanEntry.OPS_GATE_OUT,  dt, userId, systemCode, toClose);	
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		retval = false;
    	}
        return retval;
	}
    public static String createTokenRecord(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		int retval = 0;
		try{
			if(dataParts != null && dataParts.length >= 10){
				ExtChallanEntry params = ExtChallanEntry.readFromString(dataParts, colIndex, true);
				retval = ExtChallanEntry.createTokenData(conn, params);
			}
		}catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    	}
		return Integer.toString(retval);
	}
    public static String createWeighmentRecord(Connection conn, String data) {
		String dataParts[] = data == null ? null : data.split(",");
		int colIndex = 1;
		int retval = 0;
		try{
			if(dataParts != null && dataParts.length >= 8){
				String systemCode = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
				String vehicleName = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
				String dateStr = dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null;
				double loadGwt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null,0);
				double loadTwt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null,0);
				double unloadGwt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null,0);
				double unloadTwt = Misc.getParamAsDouble(dataParts != null && dataParts.length > colIndex ? dataParts[colIndex++] : null,0);
				retval = ExtChallanEntry.updateWeighBridgeData(conn, systemCode, vehicleName, dateStr, loadTwt, loadGwt, unloadTwt, unloadGwt);
			}
		}catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    	}
		return Integer.toString(retval);
	}
    public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (!conn.getAutoCommit())
				conn.setAutoCommit(true);
			//TxExecutor.processTxCmd(conn, "@@secl-GET_PO,2");
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_WEIGHMENT,TEST,TEST,20150320101010,10000,40000,10001,40001"));
			/*String rf = "OR15N1397, 11, 12, 20141212040000, 23.5, 11.5, PO1, ch1";
			String rf2 = "OR15N1397, 11, 12, 20141212040000, 21.5, 8.5, PO1, ch1";
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:SET_RFID,"+rf));
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:PREP_RF,0,"+rf));
            // cmd,systemCode,userId,1 or 0 for doingGate, 1 or 0 for auto			
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:CREATE_RF,xyz,1,1,0,"+Misc.getUndefInt()+","+rf2));
			Triple<Integer, ExtChallanEntry, Boolean> ret = TxExecutor.prepReadAtGate(conn, true, "OR15N1397", null,null, 0l, 0, 0, null, null, null);
			int id = ret.first >= 0 ? ret.first : ret.second.getId();
			//cmd,systemCode,userId, refId, ts in yyyyMMddHHmmss
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_GIN,abc,1,"+id+",20141212043000"));
			//cmd, systemCode, userId, refId, dt in yyyyMMddHHmmss format, grossWt, autoGrossWt, comments
			
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_GROSS,abc,1,"+id+",20141212044000,35,35,rwdw"));
			//System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_AIN,2"));
			//System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_AOUT,2"));
			String s = null;
			s = TxExecutor.processTxCmd(conn, "@@secl-cmd:PREP_TARE,OR15N1397");
			System.out.println(s);
			id = Misc.getParamAsInt(s.substring(0, s.indexOf(",")));
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_TARE,abc,1,"+id+",20141212044000,35,35,1,rwdw"));
			s=(TxExecutor.processTxCmd(conn, "@@secl-cmd:PREP_GOUT,OR15N1397"));
			System.out.println(s);
			id = Misc.getParamAsInt(s.substring(0, s.indexOf(",")));
			System.out.println(TxExecutor.processTxCmd(conn, "@@secl-cmd:UPD_GOUT,abc,1,"+id+",20141212045000"));
			*/
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		}
		finally {
			if (conn != null) {
			   try {
				 DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);  
			   }
			   catch (Exception e) {
				   
			   }
			}
		}
		System.out.println("end main");
	}
}

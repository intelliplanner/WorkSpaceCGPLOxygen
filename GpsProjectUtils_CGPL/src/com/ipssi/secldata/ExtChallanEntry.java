package com.ipssi.secldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;

public class ExtChallanEntry {
	public static String standardizeName(String name) {//duplicate code ... move it to CacheTrack or Misc
	    	return (name == null ? null : name.replaceAll("[^A-Za-z0-9_]", "").toUpperCase());
	 }
	public static final int IS_OPEN_REC = 1;
	public static final int IS_CANCELLED_REC = 2;
	public static final int IS_CLOSED_REC = 3;

	public static final int OPS_NEW_RFID_RECORD = 0;
	public static final int OPS_NEW_GATE_IN = 1;
	public static final int OPS_GROSS_WT = 2;
	public static final int OPS_AREA_IN = 3;
	public static final int OPS_AREA_OUT = 4;
	public static final int OPS_TARE_WT = 5;
	public static final int OPS_GATE_OUT = 6;
	
	private int status=1;
	private int id=Misc.getUndefInt();
	private String challanId;
	private String refPOId;
	private String transporter;
	private String mines;
	private String vehicleNumber;
	private int vehicleId;
	private double loadGrossWt;
	private double loadTareWt;
	private double unloadGrossWt;
	private double unloadTareWt;
	private long loadGrossTS;
	private long loadTareTS;
	private long unloadGrossTS;
	private long unloadTareTS;
	private String epc;
	private long loadGateIn;
	private long loadGateOut;
	private long unloadGateIn;
	private long unloadGateOut;
	private long loadAreaIn;
	private long loadAreaOut;
	private long unloadAreaIn;
	private long unloadAreaOut;
	private boolean loadGrossWCS;
	private boolean loadTareWCS;
	private boolean unloadGrossWCS;
	private boolean unloadTareWCS;
    private String autoMines; //-1 means there was none
    private String autoTransporter; //-1 means there was none
    private String autoVehicleName; //"NONE" means there was none ... auto
    private double autoLoadGrossWt;//-1 means there was none
    private double autoLoadTareWt;//-1 means there was none
    private double autoUnloadGrossWt;//-1 means there was none
    private double autoUnloadTareWt;//-1 mens there was none
    private String autoRefPOId;
    private String autoChallanNumber;
    private long autoGrossWtDate;
    private String comments;
    private long createdOn;
    private long updatedOn;
    private int createdBy;
    private int updatedBy;
    private int writeOnCard;
    
   
	public static String GET_RFID_LOG_ROW_HEADER = " rl.id, rl.status, rl.vehicle_id, rl.vehicle_name, rl.transporter, rl.mines, rl.auto_mines, rl.auto_transporter, rl.auto_vehicle_name, rl.po_number, rl.challan_no,  rl.comments, rl.created_on, rl.updated_on, rl.created_by, rl.updated_by  ";
    public static String GET_RFID_LOG_ROW_GATE_TIMING = " rl.load_gate_in, rl.load_gate_out, rl.unload_gate_in, rl.unload_gate_out, rl.load_area_in, rl.load_area_out, rl.unload_area_in, rl.unload_area_out ";
    public static String GET_RFID_LOG_ROW_LOAD = " rl.load_wb_date, rl.load_wcs_val, rl.load_tare, rl.load_wb_gross_date, rl.load_wb_gross_wcs, rl.load_gross, auto_load_tare_wt, rl.auto_load_gross_wt ";
    public static String GET_RFID_LOG_ROW_UNLOAD = " rl.unload_wb_date, rl.unload_wcs_val, rl.unload_tare, rl.unload_wb_gross_date, rl.unload_wb_gross_wcs, rl.unload_gross, rl.auto_unload_gross_wt, rl.auto_unload_tare_wt ";
    
    public static String CREATE_LOAD_RECORD = " insert into rfid_log(status, load_system_code, vehicle_name, vehicle_id, transporter, mines, load_wb_date, load_wcs_val, load_tare, load_wb_gross_date, load_wb_gross_wcs, load_gross, auto_transporter, auto_mines, auto_vehicle_name, auto_load_tare_wt, auto_load_gross_wt, created_by, created_on, po_number, challan_no, card_epc,  auto_po_number, auto_challan_number, auto_load_gross_date) "+
        " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?) ";
    public static String UPDATE_LOAD_RECORD = "update rfid_log set vehicle_name=?, vehicle_id=?, transporter=?, mines=?,  load_wb_date=?, load_tare=?, load_wb_gross_date=?, load_gross=?, updated_by=?, updated_on=?, po_number=?, challan_no=?, comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end)) where id=? ";
    public static String UPDATE_LOAD_RECORD_AUTO = "update rfid_log set vehicle_name=?, vehicle_id=?, transporter=?, mines=?,  load_wb_date=?, load_tare=?, load_wb_gross_date=?, load_gross=?, updated_by=?, updated_on=?, po_number=?, challan_no=?, auto_transporter=?, auto_mines=?, auto_vehicle_name=?, auto_load_tare_wt=?, auto_load_gross_wt=?,auto_po_number=?, auto_challan_number=?, auto_load_gross_date=? where id=? and unload_gross is null and unload_tare is null ";
    public static String UPDATE_UNLOAD_TARE_RECORD = "update rfid_log set  unload_wb_date=?, unload_wcs_val=?, unload_tare=?, auto_unload_tare_wt=(case when auto_unload_tare_wt is not null then auto_unload_tare_wt else ? end), updated_by=?, updated_on=?, comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end)), status=?  where id=? ";
    
    public static String UPDATE_UNLOAD_GROSS_RECORD = "update rfid_log set  unload_wb_gross_date=?, unload_wb_gross_wcs=?, unload_gross=?, auto_unload_gross_wt=(case when auto_unload_gross_wt is not null then auto_unload_gross_wt else ? end), updated_by=?, updated_on=?, comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end)), unload_system_code = ? where id=? ";
    
    public static String UPDATE_CLOSE_RECORD_BY_ID = "update rfid_log set status=? ,  comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end)) where id = ? ";
    public static String UPDATE_CLOSE_RECORD_BY_VEHICLE = "update rfid_log join (select vehicle_name, std_name, vehicle_id, max(load_wb_gross_date) cst from rfid_log where status=1 and (std_name=? or vehicle_id=?) group by vehicle_name, vehicle_id) ltp on ((ltp.std_name = rfid_log.std_name or ltp.vehicle_id = rfid_log.vehicle_id) and (ltp.cst = rfid_log.load_wb_gross_date)) set status=? , comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end))";
    public static String UPDATE_COMMENTS = "update rfid_log set comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end)), updated_by=?, updated_on=? where id=? "
    	;
    
    //for secl purpose
    public static String GET_WEIGH_BRIDGE_RECORD = "select system_code, vehicle_name, datestr, load_tare, load_gross, unload_tare, unload_gross from wb_log where vehicle_name like '%@vehicleName%' and datestr like '%@dateStr%'";
    public static String INSERT_WEIGH_BRIDGE_RECORD = "insert into wb_log(system_code,vehicle_name,datestr,load_tare,load_gross,unload_tare,unload_gross,wb_date) values (?, ?, ?, ?, ?, ?, ?, ?)";
    public static String UPDATE_WEIGH_BRIDGE_RECORD = "update wb_log set load_tare=?,load_gross=?,unload_tare=?,unload_gross=? where vehicle_name like '%@vehicleName%' and datestr like '%@dateStr%'";
    
    public static String INSERT_TOKEN_RECORD = "insert into rfid_log(token_id,vehicle_name,auto_transporter,auto_mines,load_wb_date,load_gross,load_tare, auto_po_number, auto_challan_number, card_epc,unload_wb_date,unload_gross,unload_tare,load_wb_gross_wcs,load_wb_tare_wcs,unload_wb_gross_wcs,unload_wb_tare_wcs,load_gate_in,load_gate_out,unload_gate_in,unload_gate_out,comments,write_on_card) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void reset() {
    	this.id = Misc.getUndefInt();
    }
    
    public static void updateUnloadTimings(Connection conn, int id, int timingType, long dt, int userId, String systemCode, boolean toClose) throws Exception {
    	try {
	    	String colName = timingType == OPS_NEW_GATE_IN ? "unload_gate_in" : timingType == OPS_AREA_IN ? "unload_area_in" : timingType == OPS_AREA_OUT ? "unload_area_out" : timingType == OPS_GATE_OUT ? "unload_gate_out" : null;
	    	if (colName == null)
	    		return;
	    	String q = "update rfid_log set "+colName+ " = ? "+ (timingType == OPS_GATE_OUT ? " ,status=?" : "") + ",updated_on = now(), updated_by=? ";
	    	boolean doSysCode = false;
	    	boolean doStatus = false;
	    	if (timingType == OPS_NEW_GATE_IN) {
	    		q += " , gate_system_code = ? ";
	    		doSysCode = true;
	    	}
	    	if (timingType == OPS_GATE_OUT) {
	    		doStatus = true;
	    	}
	    	
	    	q += " where id = ? ";
	    	PreparedStatement ps = conn.prepareStatement(q);
	    	int colIndex = 1;
	    	ps.setTimestamp(colIndex++, Misc.utilToSqlDate(dt));
	    	if (doStatus)
	    		ps.setInt(colIndex++, toClose ? IS_CLOSED_REC : IS_OPEN_REC);

	    	Misc.setParamInt(ps, userId, colIndex++);
	    	
	    	if (doSysCode)
	    		ps.setString(colIndex++, systemCode);
	    	ps.setInt(colIndex++, id);
	    	
	    	ps.execute();
	    	ps.close();
	    	clearDownStreamInfo(conn, id, timingType);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }

    
    public static int createLoadRecord(Connection conn, boolean isAuto, String epc, String loadSystemCode, String vehicleName, int vehicleId, String mines, String transporter, String poNumber, String challanNumber, long tareDate, long grossDate, double tareWt, double grossWt, boolean tareWCS, boolean grossWCS, int userId, String autoVehicleNumber, String autoMines, String autoTransporter, double autoTareWt, double autoGrossWt, String autoPONumber, String autoChallanNumber, long autoGrossWtDate) throws Exception {
    	try {
    		int retval = Misc.getUndefInt();
    		if (Misc.isUndef(vehicleId)) {
    			CacheTrack.VehicleSetup.getSetupByStdName(standardizeName(vehicleName), conn);
    		}
    		ExtChallanEntry.closeRecordByVehicle(conn, vehicleName, vehicleId, null, true);
    		if (!isAuto) {
    			autoTransporter = null;
    			autoMines = null;
    			autoVehicleNumber = null;
    			autoGrossWt = Misc.getUndefDouble();
    			autoTareWt = Misc.getUndefDouble();
    			autoPONumber = null;
    			autoChallanNumber = null;
    			autoGrossWtDate = 0;
    		}
    		else if (false) {
    			autoTransporter = autoTransporter == null ? transporter : autoTransporter;
    			autoMines = autoMines == null ? mines : autoMines;
    			autoVehicleNumber = autoVehicleNumber == null ? vehicleName : autoVehicleNumber;
    			autoGrossWt = Misc.isUndef(autoGrossWt) ? grossWt : autoGrossWt;
    			autoTareWt = Misc.isUndef(autoGrossWt) ? tareWt : autoTareWt;
    			autoPONumber = autoPONumber == null ? poNumber : autoPONumber;
    			autoChallanNumber = autoChallanNumber == null ? challanNumber : autoChallanNumber;
    			autoGrossWtDate = autoGrossWtDate <= 0 ? grossDate : autoGrossWtDate;
    		}
    		
    		PreparedStatement ps = conn.prepareStatement(CREATE_LOAD_RECORD);
    		int colIndex = 1;
    		ps.setInt(colIndex++, 1);
    		
    		ps.setString(colIndex++, loadSystemCode);
    		ps.setString(colIndex++, vehicleName);
    		Misc.setParamInt(ps, vehicleId, colIndex++);
    		ps.setString(colIndex++, transporter);
    		ps.setString(colIndex++, mines);
    		if (tareDate <= 0 && !Misc.isUndef(tareWt))
    			tareDate = grossDate;
    		if (grossDate <= 0 && !Misc.isUndef(grossWt))
    			grossDate = tareDate;
    		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(tareDate));
    		Misc.setParamInt(ps, tareWCS ? 1 : 0, colIndex++);
    		Misc.setParamDouble(ps, tareWt, colIndex++);
    		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(grossDate));
    		Misc.setParamInt(ps, grossWCS ? 1 : 0, colIndex++);
    		Misc.setParamDouble(ps, grossWt, colIndex++);
    		Misc.setParamInt(ps, Misc.getParamAsInt(autoTransporter), colIndex++);
    		Misc.setParamInt(ps, Misc.getParamAsInt(autoMines), colIndex++);
    		ps.setString(colIndex++, isAuto ? vehicleName : null);
    		Misc.setParamDouble(ps, isAuto ? tareWt : Misc.getUndefDouble(), colIndex++);
    		Misc.setParamDouble(ps, isAuto ? grossWt : Misc.getUndefDouble(), colIndex++);
    		Misc.setParamInt(ps, userId, colIndex++);
    		ps.setTimestamp(colIndex++, new java.sql.Timestamp(System.currentTimeMillis()));
    		ps.setString(colIndex++, poNumber);
    		ps.setString(colIndex++, challanNumber);
    		ps.setString(colIndex++, epc);
    		ps.setString(colIndex++, autoPONumber);
    		ps.setString(colIndex++, autoChallanNumber);
    		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(autoGrossWtDate));
    		ps.executeUpdate();
    		ResultSet rs = ps.getGeneratedKeys();
    		if (rs.next()) {
    			retval = rs.getInt(1);
    		}
    		rs.close();
    		ps.close();
    		clearDownStreamInfo(conn, retval, ExtChallanEntry.OPS_NEW_RFID_RECORD);
    		return retval;    		
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
   
   public static void updateLoadRecordAuto(Connection conn, int id, String vehicleName, int vehicleId, String mines, String transporter, String poNumber, String challanNumber, long tareDate, long grossDate, double tareWt, double grossWt, int userId) throws Exception {
   	try {
   		PreparedStatement ps = conn.prepareStatement(UPDATE_LOAD_RECORD_AUTO);
   		int colIndex = 1;
   		ps.setString(colIndex++, vehicleName);
   		Misc.setParamInt(ps, vehicleId, colIndex++);
   		ps.setString(colIndex++, transporter);
   		ps.setString(colIndex++, mines);
   		if (tareDate <= 0 && !Misc.isUndef(tareWt))
   			tareDate = grossDate;
   		if (grossDate <= 0 && !Misc.isUndef(grossWt))
   			grossDate = tareDate;
   		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(tareDate));
   		Misc.setParamDouble(ps, tareWt, colIndex++);
   		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(grossDate));
   		Misc.setParamDouble(ps, grossWt, colIndex++);
   		Misc.setParamInt(ps, userId, colIndex++);
   		ps.setTimestamp(colIndex++, new java.sql.Timestamp(System.currentTimeMillis()));
   		ps.setString(colIndex++, poNumber);
   		ps.setString(colIndex++, challanNumber);
   		ps.setString(colIndex++, transporter);
   		ps.setString(colIndex++, mines);
   		ps.setString(colIndex++, vehicleName);
   		Misc.setParamDouble(ps, tareWt, colIndex++);
   		Misc.setParamDouble(ps, grossWt, colIndex++);
   	    ps.setString(colIndex++, poNumber);
   	    ps.setString(colIndex++, challanNumber);
   	    ps.setTimestamp(colIndex++, Misc.utilToSqlDate(grossDate));
   	    
   		ps.setInt(colIndex++, id);
   		ps.executeUpdate();
   		ps.close();
   		ps = null;
   	}
   	catch (Exception e) {
   		e.printStackTrace();
   		throw e;
   	}
   }

    public static void updateLoadRecord(Connection conn, int id, String vehicleName, int vehicleId, String mines, String transporter, String poNumber, String challanNumber, long tareDate, long grossDate, double tareWt, double grossWt, int userId, String comments) throws Exception {
    	try {
    		PreparedStatement ps = conn.prepareStatement(UPDATE_LOAD_RECORD);
    		int colIndex = 1;
    		ps.setString(colIndex++, vehicleName);
    		Misc.setParamInt(ps, vehicleId, colIndex++);
    		ps.setString(colIndex++, transporter);
    		ps.setString(colIndex++, mines);
    		if (tareDate <= 0 && !Misc.isUndef(tareWt))
    			tareDate = grossDate;
    		if (grossDate <= 0 && !Misc.isUndef(grossWt))
    			grossDate = tareDate;
    		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(tareDate));
    		Misc.setParamDouble(ps, tareWt, colIndex++);
    		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(grossDate));
    		Misc.setParamDouble(ps, grossWt, colIndex++);
    		Misc.setParamInt(ps, userId, colIndex++);
    		ps.setTimestamp(colIndex++, new java.sql.Timestamp(System.currentTimeMillis()));
    		ps.setString(colIndex++, poNumber);
    		ps.setString(colIndex++, challanNumber);
    		ps.setString(colIndex++, comments);
    		ps.setString(colIndex++, comments);
    		ps.setInt(colIndex++, id);
    		ps.executeUpdate();
    		ps.close();
    		ps = null;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }

    public static void updateUnload(Connection conn, int id, boolean doTare, long dt, double wt, double autoWt, boolean wcs, int userId, String comments, String systemCode, boolean toClose) throws Exception {
    	try {
    	    //public static String UPDATE_UNLOAD_TARE_RECORD = "update rfid_log set  unload_wb_date=?, unload_wcs_val=?, unload_tare=?, auto_unload_tare_wt=(case when auto_unload_tare_wt is not null then auto_unload_tare_wt else  end), updated_by=?, updated_on=?, comments=concat((case when comments is null then '' else comments end), (case when comments is null then '' else '<br>' end), (case when ? is null then '' else ? end)) where id=? ";
    		PreparedStatement ps = conn.prepareStatement(doTare ? UPDATE_UNLOAD_TARE_RECORD : UPDATE_UNLOAD_GROSS_RECORD);
    		int colIndex = 1;
    		long now = System.currentTimeMillis();
    		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(dt <= 0 ? now : dt));
    		ps.setInt(colIndex++,  wcs ? 1 : 0);
    		Misc.setParamDouble(ps, wt, colIndex++);
    		Misc.setParamDouble(ps, autoWt, colIndex++);
    		Misc.setParamInt(ps, userId, colIndex++);
    		ps.setTimestamp(colIndex++, new java.sql.Timestamp(System.currentTimeMillis()));
    		ps.setString(colIndex++, comments);
    		ps.setString(colIndex++, comments);
    		if (!doTare)
    			ps.setString(colIndex++, systemCode);
    		if (doTare)
    			ps.setInt(colIndex++, toClose ? ExtChallanEntry.IS_CLOSED_REC : ExtChallanEntry.IS_OPEN_REC);
    		ps.setInt(colIndex++, id);
    		ps.executeUpdate();
    		ps.close();
    		clearDownStreamInfo(conn, id, doTare ? ExtChallanEntry.OPS_TARE_WT: ExtChallanEntry.OPS_GROSS_WT);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    public static int createTokenData(Connection conn,ExtChallanEntry params) throws Exception{
    	int retval = 0;
    	PreparedStatement ps = null;
    	try {//tokenId,vehicleName,epcId,minesName,transporter,challanNumber,poNumber,loadGateSystemCode,loadgateIn,loadWBSystemCode,loadWBDate,loadGross,loadTare,loadWCS,loadGateOut,unloadGateSystemCode,unloadgateIn,unloadWBSystemCode,unloadWBDate,unloadGross,unloadTare,unloadWCS,unloadGateOut,writeCard
    		if(params == null || params.getVehicleNumber() == null || params.getVehicleNumber().length() <= 0)
    			return 0;
    			ps = conn.prepareStatement(INSERT_TOKEN_RECORD);
    			Misc.setParamInt(ps, params.getId(), 1);
    			ps.setString(2, params.getVehicleNumber());
        		ps.setString(3, params.getTransporter());
        		ps.setString(4, params.getMines());
        		ps.setTimestamp(5, Misc.utilToSqlDate(params.getLoadGrossTS()));
    			Misc.setParamDouble(ps, params.getLoadGrossWt(), 6);
    			Misc.setParamDouble(ps, params.getLoadTareWt(), 7);
    			ps.setString(8, params.getAutoRefPOId());
        		ps.setString(9, params.getChallanId());
        		ps.setString(10, params.getEpc());
        		ps.setTimestamp(11, Misc.utilToSqlDate(params.getUnloadGrossTS()));
    			Misc.setParamDouble(ps, params.getUnloadGrossWt(), 12);
    			Misc.setParamDouble(ps, params.getUnloadTareWt(), 13);
    			Misc.setParamInt(ps, params.loadGrossWCS ? 1 : 0, 14);
    			Misc.setParamInt(ps, params.loadTareWCS ? 1 : 0, 15);
    			Misc.setParamInt(ps, params.unloadGrossWCS ? 1 : 0, 16);
    			Misc.setParamInt(ps, params.unloadTareWCS ? 1 : 0, 17);
    			ps.setTimestamp(18, Misc.utilToSqlDate(params.loadAreaIn));
    			ps.setTimestamp(19, Misc.utilToSqlDate(params.loadAreaOut));
    			ps.setTimestamp(20, Misc.utilToSqlDate(params.unloadAreaIn));
    			ps.setTimestamp(21, Misc.utilToSqlDate(params.unloadAreaOut));
    			ps.setString(22, params.getComments());
    			Misc.setParamInt(ps, params.getWriteOnCard(), 23);
        		ps.executeUpdate();
    		if(ps != null)
    			ps.close();
    		retval = 1;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    	return retval;
    }
    public static int updateWeighBridgeData(Connection conn, String  systemCode,String vehicleName,String dateStr,double load_twt, double load_gwt,double unload_twt, double unload_gwt) throws Exception {
    	PreparedStatement preStat = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
    	Date wbDate = null;
    	int retval = 0;
    	try {
    		String preSystemCode = null;
    		String preVehicleName = null;
    		String preDateStr = null;
    		double preLoadTare = Misc.getUndefDouble();
    		double preLoadGross = Misc.getUndefDouble();
    		double preUnloadTare = Misc.getUndefDouble();
    		double preUnloadGross = Misc.getUndefDouble();
    		if(vehicleName == null || vehicleName.length() <= 0 || dateStr == null || dateStr.length() <= 0)
    			return 0;
    		wbDate = sdf.parse(dateStr);
    		preStat = conn.prepareStatement(GET_WEIGH_BRIDGE_RECORD.replaceAll("@vehicleName", vehicleName).replaceAll("@dateStr", dateStr.substring(0, 12)));
    		rs = preStat.executeQuery();
    		while(rs.next()){
    			preSystemCode = rs.getString("system_code");
    			preVehicleName = rs.getString("vehicle_name");
    			preDateStr = rs.getString("datestr");
    			preLoadTare = Misc.getRsetDouble(rs, "load_tare");
    			preLoadGross = Misc.getRsetDouble(rs, "load_gross");
    			preUnloadTare = Misc.getRsetDouble(rs, "unload_tare");
    			preUnloadGross = Misc.getRsetDouble(rs, "unload_gross");
    		}
    		if(preSystemCode != null  && preSystemCode.length() > 0 && preVehicleName != null  && preVehicleName.length() > 0 && preDateStr != null && preDateStr.length() > 0){
    			ps = conn.prepareStatement(UPDATE_WEIGH_BRIDGE_RECORD.replaceAll("@vehicleName", vehicleName).replaceAll("@dateStr", dateStr));
    			if(load_gwt > 0)
    				preLoadGross = load_gwt;
    			if(load_twt > 0)
    				preLoadTare = load_twt;
    			if(unload_gwt > 0)
    				preUnloadGross = unload_gwt;
    			if(unload_twt > 0)
    				preUnloadGross = unload_twt;
    			Misc.setParamDouble(ps, preLoadTare, 1);
    			Misc.setParamDouble(ps, preLoadGross, 2);
    			Misc.setParamDouble(ps, preUnloadTare, 3);
    			Misc.setParamDouble(ps, preUnloadGross, 4);
        		ps.executeUpdate();
    		}else{
    			ps = conn.prepareStatement(INSERT_WEIGH_BRIDGE_RECORD);
    			ps.setString(1, systemCode);
    			ps.setString(2, vehicleName);
        		ps.setString(3, dateStr);
    			Misc.setParamDouble(ps, load_twt, 4);
    			Misc.setParamDouble(ps, load_gwt, 5);
    			Misc.setParamDouble(ps, unload_twt, 6);
    			Misc.setParamDouble(ps, unload_gwt, 7);
    			ps.setTimestamp(8, new Timestamp(wbDate.getTime()));
        		ps.executeUpdate();
    		}
    		if(preStat != null)
    			preStat.close();
    		if(rs != null)
    			rs.close();
    		if(ps != null)
    			ps.close();
    		retval = 1;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    	return retval;
    }
    public static int getLatestOpenChallanId(Connection conn, String vehicleName, int vehicleId) throws Exception {
    	try {
    		String s =  "select id from rfid_log rl where (rl.std_name = ?  or rl.vehicle_id = ?) and rl.status in (1) order by rl.status asc, rl.updated_on asc ";
    		PreparedStatement ps = conn.prepareStatement(s);
    		ps.setString(1, standardizeName(vehicleName));
    		ps.setInt(2, vehicleId);
    		ResultSet rs = ps.executeQuery();
    		int retval = Misc.getUndefInt();
    		if (rs.next()) {
    			retval = rs.getInt(1);
    		}
    		rs.close();
    		ps.close();
    		return retval;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    public static ExtChallanEntry getLatestChallanEntry(Connection conn, String vehicleName, int vehicleId, boolean doHeader, boolean doGateTiming, boolean doLoadInfo, boolean doUnloadInfo, boolean getClosedAlso) throws Exception {
    	try {
	    	ExtChallanEntry retval = new ExtChallanEntry();
			StringBuilder sb = getSelPartForChallan(doHeader, doGateTiming, doLoadInfo, doUnloadInfo);
			
			if (sb.length() > 0) {
				sb.append(" from rfid_log rl where (rl.std_name = ?  or rl.vehicle_id = ?) and rl.status in (1");
				if (getClosedAlso)
					sb.append(",").append(IS_CLOSED_REC);
				//sb.append(") ");
				sb.append(") order by rl.status asc, rl.updated_on asc ");
				PreparedStatement ps = conn.prepareStatement(sb.toString());
				ps.setString(1, standardizeName(vehicleName));
				ps.setInt(2, vehicleId);
				ResultSet rs = ps.executeQuery();
				readRow(rs, retval, doHeader, doGateTiming, doLoadInfo, doUnloadInfo);
				rs.close();
				ps.close();
			}
			return retval;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    public static ExtChallanEntry getChallanEntryById(Connection conn, int id, boolean doHeader, boolean doGateTiming, boolean doLoadInfo, boolean doUnloadInfo) throws Exception {
    	try {
    		ExtChallanEntry retval = new ExtChallanEntry();
    		StringBuilder sb = getSelPartForChallan(doHeader, doGateTiming, doLoadInfo, doUnloadInfo);
    		
    		if (sb.length() > 0) {
    			sb.append(" from rfid_log rl where id=? ");
    			PreparedStatement ps = conn.prepareStatement(sb.toString());
    			ps.setInt(1, id);
    			ResultSet rs = ps.executeQuery();
    			readRow(rs, retval, doHeader, doGateTiming, doLoadInfo, doUnloadInfo);
    			rs.close();
    			ps.close();
    		}
    		return retval;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }

    public static boolean clearDownStreamInfo(Connection conn, int id, int opType) throws SQLException {
    	StringBuilder sb = new StringBuilder();
    	if (opType >= OPS_GATE_OUT)
    		return false;
    	sb.append("update rfid_log set unload_gate_out=null ");
    	if (opType <= ExtChallanEntry.OPS_AREA_OUT) {
    		sb.append(", unload_wcs_val=null, unload_wb_date = null, unload_tare=null ");
    	}
    	if (opType <= ExtChallanEntry.OPS_AREA_IN) {
    		sb.append(", unload_area_out = null ");
    	}
    	if (opType <= ExtChallanEntry.OPS_GROSS_WT) {
    		sb.append(", unload_area_in = null ");
    	}
    	if (opType <= ExtChallanEntry.OPS_NEW_GATE_IN) {
    		sb.append(", unload_gross = null, unload_system_code=null, unload_gross = null, unload_wb_gross_date=null, unload_wb_gross_wcs=null  ");
    	}
    	if (opType <= ExtChallanEntry.OPS_NEW_RFID_RECORD) {
    		sb.append(", unload_gate_in = null, gate_system_code=null ");
    	}
    	sb.append(" where id =?");
    	PreparedStatement ps = conn.prepareStatement(sb.toString());
    	ps.setInt(1, id);
    	ps.execute();
    	ps.close();
    	return true;
    }
    
    public static boolean guessIfLatestChallanEntryValid(Connection conn, ExtChallanEntry challanEntry, int operationsContext, long refTime) throws Exception {
    	boolean retval = true;
    	if (Misc.isUndef(challanEntry.getId()))
    		return false;
    	//public static final int OPS_NEW_RFID_RECORD = 0; 
    	//public static final int OPS_NEW_GATE_IN = 1;
    	//public static final int OPS_GROSS_WT = 2;
    	//public static final int OPS_AREA_IN = 3;
    	//public static final int OPS_AREA_OUT = 4;
    	//public static final int OPS_TARE_WT = 5;
    	//public static final int OPS_GATE_OUT = 6;
    	long thresholdMilli = 4*3600*1000;
    	long stTime = 0;
    	long enTime = refTime;
    	switch (operationsContext) {
	    	case OPS_NEW_RFID_RECORD: { //if grossWt Date in X min range or create date in X min range then ok 
	    		thresholdMilli = 10*60*1000;
	    		stTime = challanEntry.getLoadGrossTS();
	    		break;
	    	}
	    	case OPS_NEW_GATE_IN: { //if grossLoadWt - refTime then ok 
	    		thresholdMilli = getTransitThresholdMilli(conn, challanEntry);
	    		stTime = challanEntry.getLoadGrossTS();
	    		break;
	    	}
	    	case OPS_GROSS_WT: {//if 
	    		thresholdMilli = 12*3600*1000;
	    		stTime = challanEntry.getUnloadGateIn();
	    		if (stTime <= 0) {
	    			thresholdMilli += getTransitThresholdMilli(conn, challanEntry);
	    			stTime = challanEntry.getLoadGrossTS();
	    		}
	    		break;
	    	}
	    	case OPS_AREA_IN: {
	    		thresholdMilli = Long.MAX_VALUE;
	    		stTime = challanEntry.getLoadGrossTS();
	    		break;
	    	}
	    	case OPS_AREA_OUT: {
	    		thresholdMilli = Long.MAX_VALUE;
	    		stTime = challanEntry.getLoadGrossTS();
	    		break;
	    	}
	    	case OPS_TARE_WT: {
	    		thresholdMilli = Long.MAX_VALUE;
	    		stTime = challanEntry.getLoadGrossTS();
	    		break;
	    	}
	    	case OPS_GATE_OUT: {
	    		thresholdMilli = 12*3600*1000;
	    		stTime = challanEntry.getLoadGrossTS();
	    		break;
	    	}
    	}
    	return Math.abs(enTime - stTime) < thresholdMilli;    	
    }
    
    public static long getTransitThresholdMilli(Connection conn, ExtChallanEntry entry) {
    	DimInfo minesDim  = DimInfo.getDimInfo("mines_list");
    	DimInfo.ValInfo valInfo = minesDim == null ? null : minesDim.getValInfo(Misc.getParamAsInt(entry.getMines()));
    	int maxMin = valInfo == null ? Misc.getUndefInt() : Misc.getParamAsInt(valInfo.getOtherProperty("transit_min"));
    	return maxMin < 0 ? 24*3600*1000 : maxMin * 60* 1000;
    }
    
    public static void closeRecordById(Connection conn, int id, String comments, boolean toClose) throws Exception {
    	try {
    		PreparedStatement ps = conn.prepareStatement(UPDATE_CLOSE_RECORD_BY_ID);
    		ps.setInt(1, toClose ? IS_CLOSED_REC : IS_CANCELLED_REC);
    		ps.setString(2, comments);
    		ps.setString(3, comments);
    		ps.setInt(4, id);
    		ps.executeUpdate();
    		ps.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    public static void closeRecordByVehicle(Connection conn, String vehicleName, int vehicleId,  String comments, boolean toClose) throws Exception {
    	try {
    		int id = ExtChallanEntry.getLatestOpenChallanId(conn, vehicleName, vehicleId);
    		ExtChallanEntry.closeRecordById(conn, id, comments, toClose);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    private static StringBuilder getSelPartForChallan(boolean doHeader, boolean doGateTiming, boolean doLoadInfo, boolean doUnloadInfo) {
    	StringBuilder sb = new StringBuilder();
		sb.append("select ");
		boolean added = false;
		if (doHeader) {
			if (added)
				sb.append(", ");
			sb.append(GET_RFID_LOG_ROW_HEADER);
			added = true;
		}
		if (doGateTiming) {
			if (added)
				sb.append(", ");
			sb.append(GET_RFID_LOG_ROW_GATE_TIMING);
			added = true;
		}
		if (doLoadInfo) {
			if (added)
				sb.append(", ");
			sb.append(GET_RFID_LOG_ROW_LOAD);
			added = true;
		}
		if (doUnloadInfo) {
			if (added)
				sb.append(", ");
			sb.append(GET_RFID_LOG_ROW_UNLOAD);
			added = true;
		}
		if (!added)
			sb.setLength(0);
		return sb;
    }
    public static ExtChallanEntry readFromString(String str, int startAtIndex, boolean readWithId) {
        if (str == null)
            return null;
        str = str.trim();
        String[] dataParts = str.split(",");
        return readFromString(dataParts, startAtIndex, readWithId);
    }
    public static ExtChallanEntry readFromString(String[] dataParts, int startAtIndex, boolean readWithId) {
        ExtChallanEntry retval = new ExtChallanEntry();
        //id, vehicle_name,transporter,mine,gross_dt (yyyyMMddHHmmss), gross wt, tare wt,po_number,challan_no
        //,epc,vehicle_id,unloadGrossTS,unloadGrossWt,unloadTareTS,unloadTareWt,loadGrossWCS,loadTareWCS,unloadGrossWCS,unloadTareWCS
        //,loadAreaIn,loadAreaOut,unloadAreaIn,unloadAreaOut
        int adj = readWithId ? 0 : 1;
        SimpleDateFormat sdf = new SimpleDateFormat(TxExecutor.g_clientDateFormat);
        for (int i = startAtIndex, sz = dataParts == null ? 0 : dataParts.length; i < sz; i++) {
            String dat = dataParts[i];
            if (dat == null)
                continue;
            dat = readAdjustedString(dat);
            switch (i - startAtIndex+adj) {
                case 0:
                    retval.setId(Misc.getParamAsInt(dat));
                    break;
                case 1:
                    retval.setVehicleNumber(dat);
                    break;
                case 2:
                    retval.setTransporter(dat);
                    break;
                case 3:
                    retval.setMines(dat);
                    break;
                case 4:
                    retval.setLoadGrossTS(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 5:
                    retval.setLoadGrossWt(Misc.getParamAsDouble(dat));
                    break;
                case 6:
                    retval.setLoadTareWt(Misc.getParamAsDouble(dat));
                    break;
                case 7:
                    retval.setRefPOId(dat);
                    break;
                case 8:
                    retval.setChallanId(dat);
                    break;
                case 9:
                    retval.setEpc(dat);
                    break;
                case 10:
                    retval.setVehicleId(Misc.getParamAsInt(dat));
                    break;
                case 11:
                    retval.setUnloadGrossTS(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 12:
                    retval.setUnloadGrossWt(Misc.getParamAsDouble(dat));
                    break;
                case 13:
                    retval.setUnloadTareTS(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 14:
                    retval.setUnloadTareWt(Misc.getParamAsDouble(dat));
                    break;
                case 15:
                    retval.setLoadGrossWCS("1".equals(dat));
                    break;
                case 16:
                    retval.setLoadTareWCS("1".equals(dat));
                    break;
                case 17:
                    retval.setUnloadGrossWCS("1".equals(dat));
                    break;
                case 18:
                    retval.setUnloadTareWCS("1".equals(dat));
                    break;
                case 19:
                    retval.setLoadAreaIn(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 20:
                    retval.setLoadAreaOut(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 21:
                    retval.setUnloadAreaIn(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 22:
                    retval.setUnloadAreaOut(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                    break;
                case 23:
                	retval.setComments(dat);
                	break;
                case 24:
                    retval.setAutoVehicleName(dat);
                    break;
                case 25:
                	retval.setAutoMines(dat);
                	break;
                case 26:
                	retval.setAutoTransporter(dat);
                	break;
                case 27:
                	retval.setAutoRefPOId(dat);
                	break;
                case 28:
                	retval.setAutoChallanNumber(dat);
                	break;
                case 29:
                	retval.setAutoLoadGrossWt(Misc.getParamAsDouble(dat));
                	break;
                case 30:
                	retval.setAutoLoadTareWt(Misc.getParamAsDouble(dat));
                	break;
                case 31:
                	retval.setAutoGrossWtDate(Misc.utilToLong(Misc.getParamAsDate(dat,null,sdf)));
                	break;
                case 32:
                	retval.setAutoUnloadGrossWt(Misc.getParamAsDouble(dat));
                	break;
                case 33:
                	retval.setAutoUnloadTareWt(Misc.getParamAsDouble(dat));
                	break; 
                case 34:
                	retval.setWriteOnCard(Misc.getParamAsInt(dat));
                	break;  
                default: 
                    break;                    
            }
        }
        return retval;
    }
    public String toString() {
    	return toString(true);
    }
    public String toString(boolean withId) {
        StringBuilder sb = new StringBuilder();
        //vehicle_name,transporter,mine,gross_dt (yyyyMMddHHmmss), gross wt, tare wt,po_number,challan_no
        //,epc,vehicle_id,unloadGrossTS,unloadGrossWt,unloadTareTS,unloadTareWt,loadGrossWCS,loadTareWCS,unloadGrossWCS,unloadTareWCS
        //,loadAreaIn,loadAreaOut,unloadAreaIn,unloadAreaOut
        SimpleDateFormat sdf = new SimpleDateFormat(TxExecutor.g_clientDateFormat);
        if (withId)
        	sb.append(getAdjustedString(id)).append(",");
        sb.append(getAdjustedString(vehicleNumber))
            .append(",").append(getAdjustedString(transporter))
            .append(",").append(getAdjustedString(mines))                
            .append(",").append(getAdjustedString(sdf, loadGrossTS))
            .append(",").append(getAdjustedString(loadGrossWt))
            .append(",").append(getAdjustedString(loadTareWt))
            .append(",").append(getAdjustedString(refPOId))
            .append(",").append(getAdjustedString(challanId))
            .append(",").append(getAdjustedString(epc))
            .append(",").append(getAdjustedString(vehicleId))
            .append(",").append(getAdjustedString(sdf, unloadGrossTS))
            .append(",").append(getAdjustedString(unloadGrossWt))
            .append(",").append(getAdjustedString(sdf, unloadTareTS))
            .append(",").append(getAdjustedString(unloadTareWt))
            .append(",").append(getAdjustedString(loadGrossWCS))
            .append(",").append(getAdjustedString(loadTareWCS))
            .append(",").append(getAdjustedString(unloadGrossWCS))
            .append(",").append(getAdjustedString(unloadTareWCS))
            .append(",").append(getAdjustedString(sdf, loadAreaIn))
            .append(",").append(getAdjustedString(sdf, loadAreaOut))
            .append(",").append(getAdjustedString(sdf, unloadAreaIn))
            .append(",").append(getAdjustedString(sdf, unloadAreaOut))
            .append(",").append(getAdjustedString(comments))
            .append(",").append(getAdjustedString(autoVehicleName))
            .append(",").append(getAdjustedString(autoMines))
            .append(",").append(getAdjustedString(autoTransporter))
            .append(",").append(getAdjustedString(autoRefPOId))
            .append(",").append(getAdjustedString(autoChallanNumber))
            .append(",").append(getAdjustedString(autoLoadGrossWt))
            .append(",").append(getAdjustedString(this.autoLoadTareWt))
            .append(",").append(getAdjustedString(sdf, this.autoGrossWtDate))
            .append(",").append(getAdjustedString(this.autoUnloadGrossWt))
            .append(",").append(getAdjustedString(this.autoUnloadTareWt))
            ;
        return sb.toString();
    }
    public static String readAdjustedString(String s) {
        if (s != null) {
            s = s.trim();
            s = s.replaceAll("@@", "@");
            s = s.replaceAll("@commma", ",");
            s = s.replaceAll("@hash", "#");
        }
        return s;
    }
    
    private static String getAdjustedString(String s) {
        if (s != null) {
            s = s.trim();
            s = s.replaceAll("@", "@@");
            s = s.replaceAll(",", "@comma");
            s = s.replaceAll("#", "@hash");
        }
        else {
            s = "";
        }
        return s;
    }
    private static String getAdjustedString(int v) {
        return Misc.isUndef(v) ? "" : Integer.toString(v);
    }
    private static String getAdjustedString(SimpleDateFormat sdf, long v) {//assumed to be date
        String s = "";
        if (v > 0) {
            Date dt = new Date(v);
            s = sdf.format(dt);
        }
        return s;
    }
    private static String getAdjustedString(long v) {//assumed to be date
        String s = "";
        if (v > 0) {
            Date dt = new Date(v);
            s = (new SimpleDateFormat(TxExecutor.g_clientDateFormat)).format(dt);
        }
        return s;
    }
    private static String getAdjustedString(double v) {
        return Misc.isUndef(v) ? "" : Double.toString(v);
    }
    private static String getAdjustedString(boolean v) {
        return v ? "1" : "0";
    }
    private static String getAdjustedString(SimpleDateFormat sdf, Date dt) {
        return dt == null ? "" : sdf.format(dt);
    }
    private static boolean readRow(ResultSet rs, ExtChallanEntry challanEntry, boolean doHeader, boolean doGateTiming, boolean doLoadInfo, boolean doUnloadInfo) throws Exception {
    	try {
	    	boolean retval = rs.next();
	    	if (retval) {
	    		if (doHeader) {
	    			challanEntry.setId(Misc.getRsetInt(rs, "id"));
	    			challanEntry.setStatus(Misc.getRsetInt(rs, "status",1));
	    		    challanEntry.setVehicleId(Misc.getRsetInt(rs, "vehicle_id"));
	    		    challanEntry.setVehicleNumber(rs.getString("vehicle_name"));
	    		    String trn = rs.getString("transporter");
	    		    if (trn == null || trn.length() == 0)
	    		    	trn = Integer.toString(Misc.getUndefInt());
	    		    challanEntry.setTransporter(trn);
	    		    String min = rs.getString("mines");
	    		    if (min == null || min.length() == 0)
	    		    	min = Integer.toString(Misc.getUndefInt());
	    		    challanEntry.setMines(min);
	    		  //auto_mines, auto_transporter, auto_vehicle_name, po_number, challan_no,  comments, created_on, updated_on, created_by, updated_by  ";
	    		    challanEntry.setAutoMines(Misc.getRsetString(rs, "auto_mines"));
	    		    challanEntry.setAutoTransporter(Misc.getRsetString(rs, "auto_transporter"));
	    		    challanEntry.setAutoVehicleName(rs.getString("auto_vehicle_name"));
	    		    challanEntry.setRefPOId(rs.getString("po_number"));
	    		    challanEntry.setChallanId(rs.getString("challan_no"));
	    		    challanEntry.setComments(rs.getString("comments"));
	    		    challanEntry.setCreatedOn(Misc.sqlToLong(rs.getTimestamp("created_on")));
	    		    challanEntry.setUpdatedOn(Misc.sqlToLong(rs.getTimestamp("updated_on")));
	    		    challanEntry.setCreatedBy(Misc.getRsetInt(rs, "created_by"));
	    		    challanEntry.setUpdatedBy(Misc.getRsetInt(rs, "updated_by"));
	    		}
	    		if (doGateTiming) {
	    			//" load_gate_in, load_gate_out, unload_gate_in, unload_gate_out, load_area_in, load_area_out, unload_area_in, unload_area_out ";
	    			challanEntry.setLoadGateIn(Misc.sqlToLong(rs.getTimestamp("load_gate_in")));
	    			challanEntry.setLoadGateOut(Misc.sqlToLong(rs.getTimestamp("load_gate_out")));
	    			challanEntry.setUnloadGateIn(Misc.sqlToLong(rs.getTimestamp("unload_gate_in")));
	    			challanEntry.setUnloadGateOut(Misc.sqlToLong(rs.getTimestamp("unload_gate_out")));
	    			challanEntry.setLoadAreaIn(Misc.sqlToLong(rs.getTimestamp("load_area_in")));
	    			challanEntry.setLoadAreaOut(Misc.sqlToLong(rs.getTimestamp("load_area_out")));
	    			challanEntry.setUnloadAreaIn(Misc.sqlToLong(rs.getTimestamp("unload_area_in")));
	    			challanEntry.setUnloadAreaOut(Misc.sqlToLong(rs.getTimestamp("unload_area_out")));    			
	    		}
	    		if (doLoadInfo) {
	    //" load_wb_date, load_wcs_val, load_tare, load_wb_gross_date, load_wb_gross_wcs, load_gross, auto_load_tare_wt, auto_load_gross_wt ";
	    			challanEntry.setLoadTareTS(Misc.sqlToLong(rs.getTimestamp("load_wb_date")));
	    			challanEntry.setLoadTareWCS(Misc.getRsetInt(rs, "load_wcs_val") == 1);
	    			challanEntry.setLoadTareWt(Misc.getRsetDouble(rs, "load_tare"));
	    			challanEntry.setAutoLoadTareWt(Misc.getRsetDouble(rs, "auto_load_tare_wt"));
	    			challanEntry.setLoadGrossTS(Misc.sqlToLong(rs.getTimestamp("load_wb_gross_date")));
	    			challanEntry.setLoadGrossWCS(Misc.getRsetInt(rs, "load_wb_gross_wcs") == 1);
	    			challanEntry.setLoadGrossWt(Misc.getRsetDouble(rs, "load_gross"));
	    			challanEntry.setAutoLoadGrossWt(Misc.getRsetDouble(rs, "auto_load_gross_wt"));
	    		}
	    		if (doUnloadInfo) {
	    			challanEntry.setUnloadTareTS(Misc.sqlToLong(rs.getTimestamp("unload_wb_date")));
	    			challanEntry.setUnloadTareWCS(Misc.getRsetInt(rs, "unload_wcs_val") == 1);
	    			challanEntry.setUnloadTareWt(Misc.getRsetDouble(rs, "unload_tare"));
	    			challanEntry.setAutoUnloadTareWt(Misc.getRsetDouble(rs, "auto_unload_tare_wt"));
	    			challanEntry.setUnloadGrossTS(Misc.sqlToLong(rs.getTimestamp("unload_wb_gross_date")));
	    			challanEntry.setUnloadGrossWCS(Misc.getRsetInt(rs, "unload_wb_gross_wcs") == 1);
	    			challanEntry.setUnloadGrossWt(Misc.getRsetDouble(rs, "unload_gross"));
	    			challanEntry.setAutoUnloadGrossWt(Misc.getRsetDouble(rs, "auto_unload_gross_wt"));
	    		}
	    	}
	    	return retval;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    public static void updateComments(Connection conn, int userId, int refId, String comments) throws Exception {
    	try {
    		PreparedStatement ps = conn.prepareStatement(UPDATE_COMMENTS);
    		ps.setString(1, comments);
    		ps.setString(2, comments);
    		Misc.setParamInt(ps, userId, 3);
    		ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
    		ps.setInt(5, refId);
    		ps.execute();
    		ps.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getChallanId() {
		return challanId;
	}
	public void setChallanId(String challanId) {
		this.challanId = challanId;
	}
	public String getRefPOId() {
		return refPOId;
	}
	public void setRefPOId(String refPOId) {
		this.refPOId = refPOId;
	}
	public String getTransporter() {
		return transporter;
	}
	public void setTransporter(String transporter) {
		this.transporter = transporter;
	}
	public String getMines() {
		return mines;
	}
	public void setMines(String mines) {
		this.mines = mines;
	}
	public String getVehicleNumber() {
		return vehicleNumber;
	}
	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public double getLoadGrossWt() {
		return loadGrossWt;
	}
	public void setLoadGrossWt(double loadGrossWt) {
		this.loadGrossWt = loadGrossWt;
	}
	public double getLoadTareWt() {
		return loadTareWt;
	}
	public void setLoadTareWt(double loadTareWt) {
		this.loadTareWt = loadTareWt;
	}
	public double getUnloadGrossWt() {
		return unloadGrossWt;
	}
	public void setUnloadGrossWt(double unloadGrossWt) {
		this.unloadGrossWt = unloadGrossWt;
	}
	public double getUnloadTareWt() {
		return unloadTareWt;
	}
	public void setUnloadTareWt(double unloadTareWt) {
		this.unloadTareWt = unloadTareWt;
	}
	public long getLoadGrossTS() {
		return loadGrossTS;
	}
	public void setLoadGrossTS(long loadGrossTS) {
		this.loadGrossTS = loadGrossTS;
	}
	public long getLoadTareTS() {
		return loadTareTS;
	}
	public void setLoadTareTS(long loadTareTS) {
		this.loadTareTS = loadTareTS;
	}
	public long getUnloadGrossTS() {
		return unloadGrossTS;
	}
	public void setUnloadGrossTS(long unloadGrossTS) {
		this.unloadGrossTS = unloadGrossTS;
	}
	public long getUnloadTareTS() {
		return unloadTareTS;
	}
	public void setUnloadTareTS(long unloadTareTS) {
		this.unloadTareTS = unloadTareTS;
	}
	public String getEpc() {
		return epc;
	}
	public void setEpc(String epc) {
		this.epc = epc;
	}
	public long getLoadGateIn() {
		return loadGateIn;
	}
	public void setLoadGateIn(long loadGateIn) {
		this.loadGateIn = loadGateIn;
	}
	public long getLoadGateOut() {
		return loadGateOut;
	}
	public void setLoadGateOut(long loadGateOut) {
		this.loadGateOut = loadGateOut;
	}
	public long getUnloadGateIn() {
		return unloadGateIn;
	}
	public void setUnloadGateIn(long unloadGateIn) {
		this.unloadGateIn = unloadGateIn;
	}
	public long getUnloadGateOut() {
		return unloadGateOut;
	}
	public void setUnloadGateOut(long unloadGateOut) {
		this.unloadGateOut = unloadGateOut;
	}
	public long getLoadAreaIn() {
		return loadAreaIn;
	}
	public void setLoadAreaIn(long loadAreaIn) {
		this.loadAreaIn = loadAreaIn;
	}
	public long getLoadAreaOut() {
		return loadAreaOut;
	}
	public void setLoadAreaOut(long loadAreaOut) {
		this.loadAreaOut = loadAreaOut;
	}
	public long getUnloadAreaIn() {
		return unloadAreaIn;
	}
	public void setUnloadAreaIn(long unloadAreaIn) {
		this.unloadAreaIn = unloadAreaIn;
	}
	public long getUnloadAreaOut() {
		return unloadAreaOut;
	}
	public void setUnloadAreaOut(long unloadAreaOut) {
		this.unloadAreaOut = unloadAreaOut;
	}
	public boolean isLoadGrossWCS() {
		return loadGrossWCS;
	}
	public void setLoadGrossWCS(boolean loadGrossWCS) {
		this.loadGrossWCS = loadGrossWCS;
	}
	public boolean isLoadTareWCS() {
		return loadTareWCS;
	}
	public void setLoadTareWCS(boolean loadTareWCS) {
		this.loadTareWCS = loadTareWCS;
	}
	public boolean isUnloadGrossWCS() {
		return unloadGrossWCS;
	}
	public void setUnloadGrossWCS(boolean unloadGrossWCS) {
		this.unloadGrossWCS = unloadGrossWCS;
	}
	public boolean isUnloadTareWCS() {
		return unloadTareWCS;
	}
	public void setUnloadTareWCS(boolean unloadTareWCS) {
		this.unloadTareWCS = unloadTareWCS;
	}
	public String getAutoVehicleName() {
		return autoVehicleName;
	}
	public void setAutoVehicleName(String autoVehicleName) {
		this.autoVehicleName = autoVehicleName;
	}
	public double getAutoLoadGrossWt() {
		return autoLoadGrossWt;
	}
	public void setAutoLoadGrossWt(double autoLoadGrossWt) {
		this.autoLoadGrossWt = autoLoadGrossWt;
	}
	public double getAutoLoadTareWt() {
		return autoLoadTareWt;
	}
	public void setAutoLoadTareWt(double autoLoadTareWt) {
		this.autoLoadTareWt = autoLoadTareWt;
	}
	public double getAutoUnloadGrossWt() {
		return autoUnloadGrossWt;
	}
	public void setAutoUnloadGrossWt(double autoUnloadGrossWt) {
		this.autoUnloadGrossWt = autoUnloadGrossWt;
	}
	public double getAutoUnloadTareWt() {
		return autoUnloadTareWt;
	}
	public void setAutoUnloadTareWt(double autoUnloadTareWt) {
		this.autoUnloadTareWt = autoUnloadTareWt;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public long getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
	public long getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(long updatedOn) {
		this.updatedOn = updatedOn;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getAutoRefPOId() {
		return autoRefPOId;
	}

	public void setAutoRefPOId(String autoRefPOId) {
		this.autoRefPOId = autoRefPOId;
	}

	public String getAutoChallanNumber() {
		return autoChallanNumber;
	}

	public void setAutoChallanNumber(String autoChallanNumber) {
		this.autoChallanNumber = autoChallanNumber;
	}

	public long getAutoGrossWtDate() {
		return autoGrossWtDate;
	}

	public void setAutoGrossWtDate(long autoGrossWtDate) {
		this.autoGrossWtDate = autoGrossWtDate;
	}

	public String getAutoMines() {
		return autoMines;
	}

	public void setAutoMines(String autoMines) {
		this.autoMines = autoMines;
	}

	public String getAutoTransporter() {
		return autoTransporter;
	}

	public void setAutoTransporter(String autoTransporter) {
		this.autoTransporter = autoTransporter;
	}
	public int getWriteOnCard() {
		return writeOnCard;
	}
	public void setWriteOnCard(int writeOnCard) {
		this.writeOnCard = writeOnCard;
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (!conn.getAutoCommit())
				conn.setAutoCommit(true);
			ExtChallanEntry e1 = null;
			ExtChallanEntry.updateWeighBridgeData(conn, "TEST", "TEST123", "20150315133210", 10230, 0, 0, 0);
			ExtChallanEntry.updateWeighBridgeData(conn, "TEST", "TEST123", "20150315133210", 10230, 40420, 0, 0);
			ExtChallanEntry.updateWeighBridgeData(conn, "TEST", "TEST131", "20150315133210", 10230, 0, 0, 0);
			ExtChallanEntry.updateWeighBridgeData(conn, "TEST", "TEST131", "20150315133210", 10230, 43520, 0, 0);
			/*int id = ExtChallanEntry.createLoadRecord(conn, true, "asas", "sff", "OR15N1397", Misc.getUndefInt(), "1", "2", "po1", "ch1", System.currentTimeMillis(), Misc.getUndefInt(), 11.2, 32.1, false, false, 1, "Test1v", "2", "3", 11, 23, "po3", "ch3", System.currentTimeMillis());
			e1 = ExtChallanEntry.getLatestChallanEntry(conn, "OR15N1397", Misc.getUndefInt(), true, true, true, true, true);
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateLoadRecord(conn, id, "test32", Misc.getUndefInt(),"34", "5", "po1", "ch1", System.currentTimeMillis(), System.currentTimeMillis(), 11, 65, 1, "Hello");
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateLoadRecordAuto(conn, id, "test32", Misc.getUndefInt(),"34", "5", "po1", "ch1", System.currentTimeMillis(), System.currentTimeMillis(), 11, 65, 1);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateComments(conn, 1, id, "asdsad");
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateUnloadTimings(conn, id, ExtChallanEntry.OPS_NEW_GATE_IN, System.currentTimeMillis(), 1, "abc", false);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateUnload(conn, id, false, System.currentTimeMillis(), 12.3, 12.4, false, 1, null, "xyz", false);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateUnloadTimings(conn, id, ExtChallanEntry.OPS_AREA_IN, System.currentTimeMillis(), 1, "abc", false);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateUnloadTimings(conn, id, ExtChallanEntry.OPS_AREA_OUT, System.currentTimeMillis(), 1, "abc", false);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateUnload(conn, id, true, System.currentTimeMillis(), 34.3, 21.4, false, 1, null, "xyz", false);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
			
			ExtChallanEntry.updateUnloadTimings(conn, id, ExtChallanEntry.OPS_GATE_OUT, System.currentTimeMillis(), 1, "abc", true);
			e1 = ExtChallanEntry.getChallanEntryById(conn, id, true, false, false, true);;
			System.out.println(e1.toString());
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

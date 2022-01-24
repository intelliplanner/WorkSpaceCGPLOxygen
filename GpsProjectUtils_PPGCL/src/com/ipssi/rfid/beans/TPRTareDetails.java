package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.Utils;

public class TPRTareDetails {
	private int tprId;
	private int vehicleId;
	private double tare;
	private long lastTareOn = Misc.getUndefInt();
	private long tareExpiareOn = Misc.getUndefInt();
	private boolean noTareAllowed = false;
	private boolean isload = true;
	private double tareFreq = Misc.getUndefDouble();//no of days  
	private static double minIntTareFreq = 30;
	private static double minOtherTareFreq = Misc.getUndefDouble();
	private static double minRoadTareFreq = Misc.getUndefDouble();
	private static double minWasheryTareFreq = 1.0;
	
	private String wbCode;
	public TPRTareDetails(){
		super();
	}
	
	public static void setInternalTareFreq(double freq){
		minIntTareFreq = freq;
	}
	public static void setOtherTareFreq(double freq){
		minOtherTareFreq = freq;
	}
	private TPRTareDetails(int vehicleId, double tare, long lastTareOn,double tareFreq, boolean isload, String wbCode) {
		super();
		this.vehicleId = vehicleId;
		this.tare = tare;
		this.lastTareOn = lastTareOn;
		this.tareFreq = tareFreq;
		this.isload = isload;
		this.wbCode = wbCode;
		if(!Misc.isUndef(this.tareFreq) && !Misc.isUndef(this.lastTareOn)){
			this.tareExpiareOn = Double.MAX_VALUE == this.tareFreq ?  Misc.getUndefInt() : lastTareOn + (long)(this.tareFreq*24*60*60*1000);
			this.noTareAllowed = Double.MAX_VALUE == this.tareFreq || this.tareExpiareOn > System.currentTimeMillis(); 
		}
	}
    /*public static TPRTareDetails getTareInfo(Connection conn,TPRecord tpRecord, Vehicle veh,boolean isLoad, int materialCat) throws Exception{
    	if(veh == null || Misc.isUndef(veh.getId()))
    		return null;
    	if(tpRecord == null && !Misc.isUndef(veh.getLastTareTPR())){
    		tpRecord = (TPRecord) RFIDMasterDao.get(conn, TPRecord.class, veh.getLastTareTPR());
    	}
    	if(tpRecord != null &&  materialCat != tpRecord.getMaterialCat())
			tpRecord = null;
    	double tareWt = tpRecord == null ? Misc.getUndefDouble() : isLoad ? tpRecord.getLoadTare() : tpRecord.getUnloadTare();
    	Date tareOn = tpRecord == null ? null : isLoad ? tpRecord.getLatestLoadWbInExit() : tpRecord.getLatestUnloadWbOutExit();
    	double tareFreq = Misc.getUndefDouble();
    	boolean isCurrent = tareOn != null && tareWt > 0.0;
		//if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL || veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY){
    	if(materialCat == Type.TPRMATERIAL.COAL_INTERNAL || materialCat == Type.TPRMATERIAL.COAL_WASHERY){
			tareFreq = isLoad ? veh.getLoadTareFreq() : veh.getUnloadTareFreq();
		//}else if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD){
    	}else if(materialCat == Type.TPRMATERIAL.COAL_ROAD){
			DoDetails doDetails = DoDetails.getDODetails(conn, veh.getDoAssigned(), Misc.getUndefInt());
			//Transporter transporter = (Transporter) RFIDMasterDao.get(conn, Transporter.class, tpRecord.getTransporterId()); 
			tareFreq = doDetails == null || doDetails.getAllowNoTare() != 1 ? Misc.getUndefDouble(): doDetails.getMaxTareGap()/24;
		}
		if(Misc.isUndef(tareFreq)){
			if(isCurrent){
				tareFreq = 1;
			}
			//else if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL){
			else if(materialCat == Type.TPRMATERIAL.COAL_INTERNAL ){
				tareFreq = minIntTareFreq;
//			}else if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ){
			}else if(materialCat == Type.TPRMATERIAL.COAL_ROAD ){
				tareFreq = minRoadTareFreq;
//			}else if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY ){
			}else if(materialCat == Type.TPRMATERIAL.COAL_WASHERY ){
				tareFreq = minWasheryTareFreq;
			}else{
				tareFreq = Integer.MAX_VALUE;
			}
			tareFreq = veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL ? minIntTareFreq
					   : veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ? minRoadTareFreq
					   : Integer.MAX_VALUE;
		}
    	if(Misc.isUndef(tareWt) || tareOn == null){
    		tareWt = isLoad ? veh.getLoadTare() : veh.getUnloadTare();
    		tareOn = isLoad ? veh.getLoadTareTime() : veh.getUnloadTareTime();
    	}
    	return new TPRTareDetails(veh.getId(), tareWt, tareOn == null ? Misc.getUndefInt() : tareOn.getTime(), tareFreq, isLoad);
    }*/
	public double getTareFreq() {
		return tareFreq;
	}
	public void setTareFreq(double tareFreq) {
		this.tareFreq = tareFreq;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public double getTare() {
		return tare;
	}
	public void setTare(double tare) {
		this.tare = tare;
	}
	public long getLastTareOn() {
		return lastTareOn;
	}
	public void setLastTareOn(long lastTareOn) {
		this.lastTareOn = lastTareOn;
	}
	public long getTareExpiareOn() {
		return tareExpiareOn;
	}
	public void setTareExpiareOn(long tareExpiareOn) {
		this.tareExpiareOn = tareExpiareOn;
	}
	public boolean isNoTareAllowed() {
		return noTareAllowed;
	}
	public void setNoTareAllowed(boolean noTareAllowed) {
		this.noTareAllowed = noTareAllowed;
	}
	public boolean isIsload() {
		return isload;
	}
	public void setIsload(boolean isload) {
		this.isload = isload;
	}
	
	public static double getAvgTare(Connection conn,boolean isLoad,int vehicleId){
		double retval = Misc.getUndefDouble();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(isLoad)
				ps = conn.prepareStatement("select avg(load_tare) from tp_record where vehicle_id=? and status=1 and load_tare is not null order by latest_load_wb_in_out desc limit 5");
			else
				ps = conn.prepareStatement("select avg(unload_tare) from tp_record where vehicle_id=? and status=1 and unload_tare is not null order by latest_unload_wb_out_out desc limit 5");
			Misc.setParamInt(ps, vehicleId, 1);
			rs = ps.executeQuery();
			if(rs.next())
				retval = Misc.getRsetDouble(rs, 1);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	
	
	public static TPRTareDetails getTareInfo(Connection conn,TPRecord tpRecord, Vehicle veh,boolean isLoad, int materialCat,boolean apprvd, String doNumber, String fromCode, String toCode) throws Exception{
		TPRTareDetails retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int tprId = Misc.getUndefInt();
		double tare = Misc.getUndefDouble();
		java.sql.Timestamp lastTareOn = null;
		boolean isEmpty = false;
		String wbCode = null;
		java.sql.Timestamp lastGrossOn = null;
		boolean isLatest = false;
		boolean isOpen = false;
		if(veh == null || Misc.isUndef(veh.getId()))
			return null;
		try{
			if(isLoad)
				ps = conn.prepareStatement("select "
						+ "tpr_id,load_tare, latest_load_wb_in_out ,"
						+ "(case when tpr_status=0 and is_latest=1 and "
						+ "coalesce("
						//+ "latest_load_wb_out_out,"
						+ "latest_load_gate_out_out,"
						+ "latest_unload_gate_in_out,"
						+ "latest_unload_wb_in_out,"
						+ "latest_unload_wb_out_out,"
						+ "latest_unload_gate_out_out"
						+ ") is null then 1 else 0 end),"
						+ "load_wb_in_name,latest_load_wb_out_out,do_number,is_latest,tpr_status,mines_code,destination_code from tp_record where vehicle_id=? and status=1 and load_tare is not null and material_cat=? order by latest_load_wb_in_out desc limit 1");
			else
				ps = conn.prepareStatement("select tpr_id,unload_tare, latest_unload_wb_out_out,(case when tpr_status=0 and is_latest=1  and coalesce(latest_unload_wb_out_out,latest_unload_gate_out_out) is null then 1 else 0 end),unload_wb_in_name, latest_unload_wb_out_out,do_number,is_latest,tpr_status,mines_code,destination_code from tp_record where vehicle_id=? and status=1 and unload_tare is not null and material_cat=? order by latest_unload_wb_out_out desc limit 1");
			Misc.setParamInt(ps, veh.getId(), 1);
			Misc.setParamInt(ps, materialCat, 2);
			rs = ps.executeQuery();
			if(rs.next()){
				tprId = Misc.getRsetInt(rs, 1);
				tare = Misc.getRsetDouble(rs, 2);
				lastTareOn = rs.getTimestamp(3);
				isEmpty = 1 == Misc.getRsetInt(rs, 4);
				wbCode = rs.getString(5);
				lastGrossOn = rs.getTimestamp(6);
				if(Utils.isNull(doNumber) && isEmpty)
					doNumber = rs.getString(7);
				isLatest = 1 == Misc.getRsetInt(rs, 8);
				isOpen = 0 == Misc.getRsetInt(rs, 9);
				if(Utils.isNull(fromCode))
					fromCode = rs.getString(10);
				if(Utils.isNull(toCode))
					toCode = rs.getString(11);
				double freq = Misc.getUndefDouble();
				if((tpRecord != null && tpRecord.getTprId() == tprId) || (tpRecord==null && (isEmpty 
						&& (lastGrossOn == null || (lastGrossOn != null && (System.currentTimeMillis()-lastGrossOn.getTime()) < TPRInformation.getDependentStationTprThresholdMinutes()))
						)))
					freq = Double.MAX_VALUE;
				else{
					if(materialCat == Type.TPRMATERIAL.COAL_INTERNAL || materialCat == Type.TPRMATERIAL.COAL_OTHER){
						if(materialCat == Type.TPRMATERIAL.COAL_INTERNAL){
							Mines mines = isLoad ? Mines.getMines(conn, toCode != null && toCode.length() > 0 ? toCode : tpRecord == null ? null : tpRecord.getDestinationCode(), Misc.getUndefInt())
												 : Mines.getMines(conn, fromCode != null && fromCode.length() > 0 ? fromCode : tpRecord == null ? null : tpRecord.getMinesCode(), Misc.getUndefInt());
							freq = mines == null ? Misc.getUndefDouble() : mines.getTareFreqInt(); 
						}
						if(Misc.isUndef(freq))
							freq = isLoad ? veh.getLoadTareFreq() : veh.getUnloadTareFreq();
						if(Misc.isUndef(freq) || freq < 0.0)
							freq = materialCat == Type.TPRMATERIAL.COAL_INTERNAL ? minIntTareFreq : minOtherTareFreq;
					}else if(materialCat == Type.TPRMATERIAL.COAL_ROAD || materialCat == Type.TPRMATERIAL.COAL_WASHERY){
						DoDetails doDetails = DoDetails.getDODetails(conn, Utils.isNull(doNumber) ?  veh.getDoAssigned() : doNumber, Misc.getUndefInt(), apprvd, Misc.getUndefInt());
						freq = doDetails == null || Misc.isUndef(doDetails.getMaxTareGap()) ? Misc.getUndefDouble(): doDetails.getMaxTareGap();
						if(Misc.isUndef(freq) || freq < 0.0 )
							freq = minRoadTareFreq;
					}
				}
				retval = new TPRTareDetails(veh.getId(), tare, lastTareOn == null ? Misc.getUndefInt() : lastTareOn.getTime(), freq, isLoad, wbCode);
				retval.setTareFreq(freq);
				retval.setTprId(tprId);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}

	public int getTprId() {
		return tprId;
	}

	public void setTprId(int tprId) {
		this.tprId = tprId;
	}

	public String getWbCode() {
		return wbCode;
	}

	public void setWbCode(String wbCode) {
		this.wbCode = wbCode;
	}
	
}

package com.ipssi.rfid.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.Utils;
//import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class RFIDSyncMaster1Old {

	/**
	 * @param args
	 */
	public static final int MINES = 40001;
	public static final int DONUMBER = 40002;
	public static final int TRANSPORTER = 40003;
	public static final int GRADES = 40004;
	public static final int USERS = 40005;
	public static final int RELATION=40006;
	public static final int FITNESS=40007;
	public static final int MINESDEVICES=40008;
	public static final int TABLESTATUS=40010;
	public static final int DATETIME=10001;
	public static int tableStatus[]= new int[10];
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			StringBuilder sb = getDONumbers(conn, 27, 1, System.currentTimeMillis(), 2);
			System.out.println(sb.toString());
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
	public static StringBuilder getProperty(Connection conn, int propertyId, int deviceId, int userId,long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
	    try{
	    	switch(propertyId){
	    	case MINES : result = getMines(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case DONUMBER : result = getDONumbers(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case TRANSPORTER : result = getTransporters(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case GRADES : result = getGrades(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case USERS : result = getUsers(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case RELATION :result = getRelations(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case MINESDEVICES :result = getMinesDevices(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case FITNESS : result=getFitness(conn, deviceId, userId,lastSync,syncStatus); break;
	    	case TABLESTATUS : result=getTableStatus(conn, deviceId,userId,lastSync,syncStatus); break;
	    	case DATETIME : result=getDateTime(); break;
	    	}
	    }catch(Exception ex){
			ex.printStackTrace();
		}
	    retval.append("<result>");
	    if(result == null){
	    	retval.append("0");
	    }else{
	    	retval.append(result.toString());
	    }
	    retval.append("</result>");
	    
		return retval;
	}
	private static StringBuilder getDateTime() {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	Connection conn = null;
    	StringBuilder retval = null;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		ps = conn.prepareStatement("select DATE_FORMAT(NOW(),'%d-%m-%Y %H:%i:%s')");
    		rs = ps.executeQuery();
    		System.out.println("DateTime: "+ps.toString());
    		if(rs.next()){
    			if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(DATETIME).append("\" >");
				}
				retval.append("<field D=\"").append(rs.getString(1)).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}finally{
    		try{
    			Misc.closeRS(rs);
    			Misc.closePS(ps);
    			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
		return retval;
	}
	public static int getVehicle(Connection conn, String epcId, String vehicleName) {
        int vehicleId = Misc.getUndefInt();
        ArrayList<Object> list = null;
        try {
            Vehicle veh = new Vehicle();
            veh.setStatus(1);
            if(epcId != null && epcId.length() > 0){
                veh.setEpcId(epcId);
                list = (ArrayList<Object>) RFIDMasterDao.select(conn, veh);
                if (list != null && list.size() > 0) {
                    return ((Vehicle) list.get(0)).getId();
                }
                veh.setEpcId(null);
            }else if(vehicleName != null && vehicleName.length() > 0){
                veh.setStdName(CacheTrack.standardizeName(vehicleName));
                //veh.setVehicleName(vehicleName);
                list = (ArrayList<Object>) RFIDMasterDao.select(conn, veh);
                if (list != null && list.size() > 0) {
                    return ((Vehicle) list.get(0)).getId();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return vehicleId;
    }
	 public static Triple<TPRecord, Integer, Boolean> getLatestTPR(Connection conn, int vehicleId, RFIDHolder holder) {
	        TPRecord tpr = null;
	        int status = 0;
	        boolean isHHSync = false;
	        ArrayList<Object> list = null;
	        try {
	            if (!Misc.isUndef(vehicleId)) {
	                tpr = new TPRecord();
	                tpr.setVehicleId(vehicleId);
	                tpr.setTprStatus(0);//TPR.OPEN=0
	                list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr);
	                if (list != null && list.size() > 0) {
	                    tpr = (TPRecord) list.get(0);//first item of list
	                } else {
	                    tpr = null;
	                }
	                if (tpr == null) {
	                    tpr = new TPRecord();
	                    tpr.setVehicleId(vehicleId);
	                    tpr.setTprCreateDate(new Date());
	                    tpr.setTprStatus(0);//TPR.OPEN=0
	                    if (holder != null && holder.isDataOnCard() && holder.getValidityFlag()) {
	                    	tpr.setTransporterId(holder.getTransporterId());
	    					tpr.setMinesId(holder.getMinesId());
	    					tpr.setVehicleName(holder.getVehicleName());
	    					tpr.setChallanDate(holder.getDatetime());
	    					tpr.setChallanNo(holder.getChallanId());
	    					tpr.setLrNo(holder.getLRID());
	    					tpr.setLoadTare(holder.getLoadTare());
	    					tpr.setLoadGross(holder.getLoadGross());
	    					tpr.setIsMergedWithHHTpr(1);
	    					tpr.setHhTprMergedTime(new Date());
	    					tpr.setDoId(holder.getDoId());
	    					tpr.setHhDeviceId(holder.getDeviceId());
	    					tpr.setMaterialGradeId(holder.getGrade());
	                        //tpr.set
	                    }
	                    RFIDMasterDao.insert(conn, tpr);
	                    conn.commit();
	                }
	                if (list != null) {
	                    list = null;
	                }
	                TPRecord refTpr = new TPRecord();
	                refTpr.setVehicleId(vehicleId);
	                refTpr.setChallanDate(tpr.getChallanDate());
	                refTpr.setChallanNo(tpr.getChallanNo());
	                list = (ArrayList<Object>) RFIDMasterDao.select(conn, refTpr);
	                if (list != null && list.size() > 0) {
	                    for (Object obj : list) {
	                        if (((TPRecord) obj).getTprId() != tpr.getTprId()) {
	                            status = 1;
	                        }
	                    }
	                }
	                isHHSync = tpr.getIsMergedWithHHTpr() == 1;
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return new Triple<TPRecord, Integer, Boolean>(tpr, status, isHHSync);
	    }

	public static StringBuilder setRFIDData(Connection conn, String dataStr, String epcId, String vehicleName, int deviceId, int userId, int updatedBy) throws SQLException{
		StringBuilder retval = new StringBuilder();
		//PreparedStatement ps = null;
	    //ResultSet rs = null;
		int logStatus=0;
	    RFIDTagInfo tag = null;
	    RFIDHolder holder = null;
	    byte[] data = null;
		try{
			//retval.append("<result>");
			data = Utils.GetBytesFromBinaryString(dataStr);//"001
			Pair<Integer, String> vehPair = null;
			if(epcId != null && epcId.length() > 0 && data != null && data.length > 0){
				tag = new RFIDTagInfo();
				tag.epcId = Utils.HexStringToByteArray(epcId);
				tag.userData = data;
				holder = new RFIDHolder(vehicleName, tag);
				vehicleName = holder.getVehicleName();
				epcId = holder.getEpcId();
			}
			System.out.println("[Web RFID Tag Data]:"+tag);
			System.out.println("[Web RFID Vehicle]:"+vehicleName);
			System.out.println("[Web RFID EPC]:"+epcId);
			if(epcId != null || vehicleName != null)
				vehPair = TPRInformation.getVehicle(conn, epcId, vehicleName);
			if(vehPair != null && holder != null){
				holder.setVehicleId(vehPair.first);
			}
			logStatus=insertHHLogData(conn, tag, holder, userId, updatedBy,epcId);
			System.out.println("logStatus:"+logStatus);
			if(logStatus!=com.ipssi.gen.utils.Misc.getUndefInt())
			{
				retval.append("1");
			}
			if(holder != null ){
				System.out.println("Web rfid data recieved");
				holder.printData();
				Mines mines_details = new Mines();
				mines_details.setId(holder.getMinesId());
				ArrayList<Object> mines_list = RFIDMasterDao.select(conn, mines_details);
				String unitCode = "";
				int finYear = 0;
				if(mines_list != null){
					Mines _mines = (Mines) mines_list.get(0);
					unitCode =  _mines.getUnitCode();
					finYear = _mines.getFinYear();
				}
				TPRecord tpr =TPRInformation.getTPRForHHWeb(conn, holder.getVehicleId(), holder, holder.getVehicleName());
				if(tpr!=null)
				{
					//if(Misc.isUndef(tpr.getTprId()))
					tpr.setWbChallanNo(unitCode + finYear + holder.getPreChallanId());
					TPRInformation.insertUpdateTpr(conn, tpr);
					
					updateHHLogTprStatus(conn, holder, 1,tpr.getTprId());
				}
				//RFIDMasterDao.insert(conn, holder);
			}
			
			conn.commit();
			//retval.append("1");
		}catch(Exception ex){
			ex.printStackTrace();
			retval.append("0");
		}
		return retval;
	}
	
	
	
	
	public static StringBuilder setRFIDDataMulti(Connection conn, byte[] dataByte) throws SQLException{
		StringBuilder retval = new StringBuilder();
		//PreparedStatement ps = null;
	    //ResultSet rs = null;
		int recordCount=dataByte[0];
		int recordindex=0;
		int byteindex=1;
		
		while(++recordindex<=recordCount)
		{
			
		
		int logStatus=0;
	    RFIDTagInfo tag = null;
	    RFIDHolder holder = null;
	    byte[] data = null;
	 
		try{
			
			//retval.append("<result>");
			//data = Utils.GetBytesFromBinaryString(dataStr);//+"0010000000100000");
			String vehicleName = null;
			String epcId = null;
			Pair<Integer, String> vehPair = null;
			if(dataByte != null && dataByte.length > 0){
				tag = new RFIDTagInfo();
				tag.epcId= Arrays.copyOfRange(dataByte, byteindex, byteindex+12);
				byteindex=byteindex+12;
				tag.userData = Arrays.copyOfRange(dataByte, byteindex, byteindex+64);
				byteindex=byteindex+64;
				holder = new RFIDHolder(null, tag);
				vehicleName = holder.getVehicleName();
				epcId = holder.getEpcId();
			}
			System.out.println("[Web RFID Tag Data]:"+tag);
			System.out.println("[Web RFID Vehicle]:"+vehicleName);
			System.out.println("[Web RFID EPC]:"+epcId);
			if(epcId != null || vehicleName != null)
				vehPair = TPRInformation.getVehicle(conn, epcId, vehicleName);
			if(vehPair != null && holder != null){
				holder.setVehicleId(vehPair.first);
			}
			logStatus=insertHHLogData(conn, tag, holder,1, holder.getUpdatedBy(),holder.getEpcId());
			System.out.println("logStatus:"+logStatus);
			if(logStatus!=com.ipssi.gen.utils.Misc.getUndefInt())
			{
				retval.append(holder.getId()+",");
			}
			if(holder != null ){
				System.out.println("Web rfid data recieved");
				holder.printData();
				TPRecord tpr =TPRInformation.getTPRForHHWeb(conn, holder.getVehicleId(), holder, holder.getVehicleName());
				if(tpr!=null)
				{
					//if(Misc.isUndef(tpr.getTprId()))
					TPRInformation.insertUpdateTpr(conn, tpr);
					updateHHLogTprStatus(conn, holder, 1,tpr.getTprId());
				}
				//RFIDMasterDao.insert(conn, holder);
			}
			//retval.append("1");
		}catch(Exception ex){
			ex.printStackTrace();
			retval.append("0");
		}
		}
		System.out.println("retvalSync="+retval);
		return retval;
	}
	
	public static StringBuilder getMines(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query1 = "select mines_details.id,mines_details.name,mines_details.challan_prefix " +
		" from " +
		//" mines_devices  " +
		//" join " +
		" mines_details join mines_devices on mines_devices.mines_id=mines_details.id where  mines_details.status=1 and mines_devices.device_id="+deviceId +" and mines_details.updated_on > ?" ; 

	    String query = "select mines_details.id,mines_details.name,mines_details.challan_prefix " +
	    		" from " +
	    		//" mines_devices  " +
	    		//" join " +
	    		" mines_details join mines_devices on mines_devices.mines_id=mines_details.id where  mines_details.status=1 and mines_devices.device_id="+deviceId; 
	    		//+" on (mines_details.id=mines_devices.mines_id) ";
		try{//mines_details
			if(syncStatus==2)
			{
			ps = conn.prepareStatement(query);
			System.out.println("mines_details: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("mines_details: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(MINES).append("\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id")).append("\" N=\"").append(rs.getString("name")).append("\" P=\"").append(rs.getString("challan_prefix")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	
	
	public static StringBuilder getTransporters(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		 Date last_sync=Utils.getDateTime(lastSync);
		 String query1 = "select transporter_details.id,transporter_details.name,transporter_details.lr_prefix " +
			" from " +
			//" mines_devices join  do_rr_details on (do_rr_details.mines_id=mines_devices.mines_id) join do_transporter on (do_rr_details.id=do_transporter.do_id) join " +
			" transporter_details where transporter_details.status=1"+" and transporter_details.updated_on > ?" ; 
	String query = "select transporter_details.id,transporter_details.name,transporter_details.lr_prefix " +
				" from " +
				//" mines_devices join  do_rr_details on (do_rr_details.mines_id=mines_devices.mines_id) join do_transporter on (do_rr_details.id=do_transporter.do_id) join " +
				" transporter_details where transporter_details.status=1"; 
				//+"on (transporter_details.id=do_transporter.transporter_id)";
		try{//mines_details
			if(syncStatus==2)
			{
			ps = conn.prepareStatement(query);
			System.out.println("Transporters: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("Transporters: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(TRANSPORTER).append("\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id")).append("\" N=\"").append(rs.getString("name")).append("\" P=\"").append(rs.getString("lr_prefix")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static StringBuilder getDONumbers(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query1 = "select id,do_rr_number name ,mines_id from " +
		//" mines_devices join  " +
		" do_rr_details_apprvd where status=1 and (lapse_date >= now() or lapse_date is null)"+" and updated_on > ?"  ;
String query = "select id,do_rr_number name ,mines_id from " +
	    		//" mines_devices join  " +
	    		" do_rr_details_apprvd where status=1 and (lapse_date >= now() or lapse_date is null)" ;
	    		//+" on (do_rr_details.mines_id=mines_devices.mines_id) ";
		try{//mines_details
			if(syncStatus==2)
			{
			ps = conn.prepareStatement(query);
			System.out.println("DONumbers: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("DONumbers: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(DONUMBER).append("\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id")).append("\" N=\"").append(rs.getString("name")).append("\" M=\"").append(rs.getInt("mines_id")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static StringBuilder getGrades(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query1 = "select grade_details.id,grade_details.name " +
		" from " +
		//" mines_devices join  do_rr_details on (do_rr_details.mines_id=mines_devices.mines_id) join do_grade_qty on (do_rr_details.id=do_grade_qty.do_id) join " +
		" grade_details where status=1"
	    +" and grade_details.updated_on > ?" ;
	    String query = "select grade_details.id,grade_details.name " +
	    		" from " +
	    		//" mines_devices join  do_rr_details on (do_rr_details.mines_id=mines_devices.mines_id) join do_grade_qty on (do_rr_details.id=do_grade_qty.do_id) join " +
	    		" grade_details where status=1" ;
	    		//+" on (grade_details.id=do_grade_qty.grade_id)";
		try{//mines_details
			if(syncStatus==2)
			{
			ps = conn.prepareStatement(query);
			System.out.println("Grades: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("Grades: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(GRADES).append("\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id")).append("\" N=\"").append(rs.getString("name")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static StringBuilder getUsers(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query1 = "select distinct users.id,USERNAME,PASSWORD,NAME from users join mines_users on users.id=mines_users.user_id join mines_devices on mines_devices.mines_id=mines_users.mines_id where isactive=1  " ;//and users.updated_on > ?
		
	    String query = "select distinct users.id,USERNAME,PASSWORD,NAME from users join mines_users on users.id=mines_users.user_id join mines_devices on mines_devices.mines_id=mines_users.mines_id where isactive=1 ";
		try{//mines_details
			if(syncStatus==2)
			{
				ps = conn.prepareStatement(query);
				System.out.println("Users: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				//ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("Users: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(USERS).append("\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id")).append("\" U=\"").append(rs.getString("USERNAME")).append("\" P=\"").append(rs.getString("PASSWORD")).append("\" N=\"").append(rs.getString("NAME")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	
	public static StringBuilder getRelations(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query1 = "select do_id,grade_id,transporter_id from do_grade_transporter join do_rr_details_apprvd on (do_rr_details_apprvd.id=do_grade_transporter.do_id) where do_grade_transporter.status=1 and (lapse_date >= now() or lapse_date is null)" +" and do_grade_transporter.updated_on > ?" ;
		
	    String query = "select do_id,grade_id,transporter_id from do_grade_transporter  join do_rr_details_apprvd on (do_rr_details_apprvd.id=do_grade_transporter.do_id)  where do_grade_transporter.status=1 and (lapse_date >= now() or lapse_date is null)";
		try{//mines_details
			if(syncStatus==2)
			{
			ps = conn.prepareStatement(query);
			System.out.println("Relations: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("Relations: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(RELATION).append("\" >");
				}
				retval.append("<field D=\"").append(Misc.getRsetInt(rs, "do_id")).append("\" G=\"").append(rs.getInt("grade_id")).append("\" T=\"").append(rs.getInt("transporter_id")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	public static StringBuilder getMinesDevices(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query1 = "select mines_id,device_id,rfFlag, toAllowFlag from mines_devices where status=1  and mines_devices.updated_on > ?" ;
		
	    String query = "select mines_id,device_id,rfFlag, toAllowFlag from mines_devices where status=1 ";
		try{//mines_details
			if(syncStatus==2)
			{
			ps = conn.prepareStatement(query);
			System.out.println("mines_devices: "+ps.toString());
			}
			else
			{
				ps = conn.prepareStatement(query1);	
				ps.setTimestamp(1,new Timestamp(last_sync.getTime()));
				System.out.println("mines_devices: "+ps.toString());
			}
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(MINESDEVICES).append("\" >");
				}
				retval.append("<field D=\"").append(Misc.getRsetInt(rs, "device_id")).append("\" M=\"").append(rs.getInt("mines_id")).append("\" R=\"").append(rs.getInt("rfFlag")).append("\" T=\"").append(rs.getInt("toAllowFlag")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}
	
	public static StringBuilder getFitness(Connection conn, int deviceId, int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    Date last_sync=Utils.getDateTime(lastSync);
	    String query = "select vehicle.id,"+
"vehicle.name,(case when (current_data.gps_record_time is null or timestampdiff(minute,current_data.gps_record_time,now()) > 240) then 0 else 1 end) gps_ok,"
+"(case when (block_instruction.id is not null and block_instruction.block_from < now() and  (block_instruction.block_to is null or block_instruction.block_to > now()) ) then 1 else 0 end) blacklisted,"
+"(case when ((vehicle_extended.insurance_number_expiry is not null and vehicle_extended.insurance_number_expiry < now()) or (vehicle_extended.registeration_number_expiry is not null and vehicle_extended.registeration_number_expiry < now())) then 0 else 1 end) paper_ok "
+"from " 
+"vehicle "
+"join " 
+"(select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (463) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
 
+"left outer join vehicle_extended on (vehicle.id=vehicle_extended.vehicle_id) left outer join block_instruction on (vehicle.id=block_instruction.vehicle_id and block_instruction.type=1) left outer join current_data on (vehicle.id =current_data.vehicle_id and current_data.attribute_id=0) where vehicle.status=1 ";
		try{//mines_details
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			System.out.println("Fitness: "+ps.toString());
			while(rs.next()){
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(FITNESS).append("\" >");
				}
				retval.append("<field N=\"").append(Misc.getRsetString(rs, "name")).append("\" B=\"").append(rs.getInt("blacklisted")).append("\" G=\"").append(rs.getInt("gps_ok")).append("\" P=\"").append(rs.getInt("paper_ok")).append("\" />");
			}
			if(retval != null)
				retval.append("</object>");
		}catch(Exception ex){
			retval = null;
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return retval;
	}

	
	public static int insertHHLogData(Connection conn, RFIDTagInfo tag, RFIDHolder holder, int userId, int updatedBy,String epcId) throws Exception{
        String query = " insert into rfid_handheld_log (record_id, device_id, vehicle_id, vehicle_name, epc_id, mines, " +
               " transporter,record_time,do_id,challan_id,lr_id,grade,material,load_tare,load_gross,pre_mines, " +
               " pre_device_id,pre_record_id, pre_challan_id,write_status,tag_data,record_user,updated_by,created_on,tpr_status,port_node_id,status,wb_challan_no) values " +
               " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) ";
        PreparedStatement ps = null;
        int retval = com.ipssi.gen.utils.Misc.getUndefInt();
        
        try{
              ps = conn.prepareStatement(query);
                    Misc.setParamInt(ps, holder.getId(), 1);
                    Misc.setParamInt(ps, holder.getDeviceId(), 2);
                    Misc.setParamInt(ps, holder.getVehicleId(), 3);
                    ps.setString(4, holder.getVehicleName());
                    ps.setString(5, holder.getEpcId());
                    Misc.setParamInt(ps, holder.getMinesId(), 6);
                    Misc.setParamInt(ps, holder.getTransporterId(), 7);
                    System.out.println("holder.getDateTime=="+holder.getDatetime().getTime());
                    ps.setTimestamp(8,holder.getDatetime() == null ? null : new Timestamp(holder.getDatetime().getTime()));
                    Misc.setParamInt(ps, holder.getDoId(), 9);
                    ps.setString(10, holder.getChallanId());
                    ps.setString(11, holder.getLRID());
                    Misc.setParamInt(ps, holder.getGrade(), 12);
                    Misc.setParamInt(ps, holder.getMaterial(), 13);
                    Misc.setParamDouble(ps, holder.getLoadTare(), 14);
                    Misc.setParamDouble(ps, holder.getLoadGross(), 15);
                    Misc.setParamInt(ps, holder.getPreMinesId(), 16);
                    Misc.setParamInt(ps, holder.getPreDeviceId(), 17);
                    Misc.setParamInt(ps, holder.getPreRecordId(), 18);
                    ps.setString(19,  holder.getPreChallanId());
                    Misc.setParamInt(ps, (holder.getValidityFlag() ? 1 : 0), 20);
                    ps.setBytes(21, tag.userData);
//                  ps.setObject(21, tag.userData);
                    Misc.setParamInt(ps, userId, 22);
                    Misc.setParamInt(ps, updatedBy, 23);
                    ps.setTimestamp(24, new Timestamp(System.currentTimeMillis()));
                    Misc.setParamInt(ps, 0, 25);
                    Misc.setParamInt(ps, 463, 26);
                    Misc.setParamInt(ps, 1, 27);
                    ps.setString(28, holder.getWbChallanNo());
                    ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            retval=1;
           if (rs.next()) {
                 retval = rs.getInt(1);
                 holder.setGeneratedId(retval);
            }
            rs.close();
            System.out.println("RFIDMasterDao.insertHHLogData() ID : "+retval);
            if(ps != null)
                     ps.close();
        }
        catch(Exception e){
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")){
            	retval=1;
            	
            }
            
            else{
           String query1 = " insert into rfid_handheld_log (epc_id, mines,tag_data,record_user,updated_by,created_on,tpr_status,port_node_id,status) values " +
            " (?, ?, ?, ?, ?,? ,?,?) ";
     PreparedStatement ps1 = null;
            
            try{
            	
            	   
                       ps1 = conn.prepareStatement(query1);
                            
                            
                             ps1.setString(1,Misc.getParamAsString(epcId));
                            
                          
                             
                             ps1.setBytes(2, tag.userData);
                          // ps.setObject(21, tag.userData);
                             Misc.setParamInt(ps1, userId, 3);
                             Misc.setParamInt(ps1, updatedBy, 4);
                             ps1.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                             Misc.setParamInt(ps1, 2, 6);
                             Misc.setParamInt(ps1, 463, 7);
                             Misc.setParamInt(ps1, 1, 8);
                             ps1.executeUpdate();
                             retval=1;
                     ResultSet rs1 = ps1.getGeneratedKeys();
                    if (rs1.next()) {
                          retval = rs1.getInt(1);
                          holder.setGeneratedId(retval);
                     }
                     rs1.close();
                     System.out.println("RFIDMasterDao.insertHHLogData.Catch() ID : "+retval);
                     if(ps1 != null)
                              ps1.close();
                 
            }
            
            catch(Exception f){
            	f.printStackTrace();
            	throw f;
            	
            }
            }
            //throw new GenericException(e);
        }
        return retval;
}

	
	public static void updateHHLogTprStatus(Connection conn, RFIDHolder holder ,int tpr_status, int tprId) throws Exception{
        String query = " update  rfid_handheld_log set tpr_status=?,ref_tpr_id=?,updated_on=now() where id=?";
        PreparedStatement ps = null;
        //int retval = com.ipssi.gen.utils.Misc.getUndefInt();
        try{
              ps = conn.prepareStatement(query);
                    Misc.setParamInt(ps, tpr_status, 1);
                    Misc.setParamInt(ps, tprId, 2);
                    Misc.setParamInt(ps, holder.getGeneratedId(), 3);
                    System.out.println(ps);
                    ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.close();
            System.out.println("RFIDMasterDao.updateHHLogTprStatus() RecodID : "+holder.getGeneratedId()+" Tpr Status :" +tpr_status);
            if(ps != null)
                     ps.close();
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
               //throw new GenericException(e);
        }
       
}

	public static StringBuilder getTableStatus(Connection conn, int deviceId,int userId, long lastSync,int syncStatus) throws SQLException{
		StringBuilder retval = null;

		String tables[]={"mines_details","do_rr_details","transporter_details","grade_details","users","do_grade_transporter","","mines_devices"};
	    Date last_sync=Utils.getDateTime(lastSync);
	    System.out.println("getTableStatus()  lastSync: "+lastSync);
	    
	    int status=0;
	    int index=40001;
	    
			for(String table:tables){
				System.out.println("Table Name: " +table);
				String query = "select  count(*) as diff from "+
				table+" where created_on >?";
			    String query1 = "select  count(*) as diff from "+table+" where updated_on >?";
			    
				if(index==40007)
				{
					int diff=0;
					diff=last_sync.compareTo((new Date()));
					if(diff==-1)
					{
						retval.append("<field I=\"").append(index).append("\" S=\"").append(2).append("\" />");
					}
					else
					{
						retval.append("<field I=\"").append(index).append("\" S=\"").append(0).append("\" />");
						
					}
					index++;
					continue;
				}
				if(getCount(conn,query,table,last_sync)>0)
				{	status=2;// insert
				}
				else if(getCount(conn,query1,table,last_sync)>0)
				{
					status=1; // update
				}
				else 
				{
					status=0; // no change
				}
				if(retval == null){
					retval = new StringBuilder();
					retval.append("<object id=\"").append(TABLESTATUS).append("\" >");
				}
				retval.append("<field I=\"").append(index).append("\" S=\"").append(status).append("\" />");
			index++;
			}
			if(retval != null)
				retval.append("<field I=\"").append("40010").append("\" S=\"").append(Utils.getDateTimeLong(new Date())).append("\" />");
				retval.append("</object>");
		
				System.out.println("retval: "+ retval);		
		return retval;
	}
	
	public static int getCount(Connection conn,String query,String table,Date lastSync) throws SQLException
	{	
		PreparedStatement ps = null;
		ResultSet rs=null;
		 int count=0;
		
		try{//mines_details
			ps = conn.prepareStatement(query);
			ps.setTimestamp(1,new Timestamp(lastSync.getTime()) == null ? null : new Timestamp(lastSync.getTime()));
			rs = ps.executeQuery();
			System.out.println("Table Status " +ps.toString());
			while(rs.next()){
				count=rs.getInt("diff");
			}
			
		}catch(Exception ex){
		
			ex.printStackTrace();
		}finally{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}
		return count;
	
		
	}

}

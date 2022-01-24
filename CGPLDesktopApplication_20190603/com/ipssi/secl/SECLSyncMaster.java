package com.ipssi.secl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.SECLDataHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.Utils;
//import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class SECLSyncMaster {

	/**
	 * @param args
	 */
	public static final int DATETIME=10001;
	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement("select epc_id,vehicle_name,device_id,tag_data from rfid_handheld_log_copy");
			rs =ps.executeQuery();
			while(rs.next()){
				byte[] data = (byte[]) rs.getObject("tag_data");
				String dataStr = Utils.getBinaryStrFromByteArray(data);
				//setRFIDData(conn, dataStr, rs.getString("epc_id"), rs.getString("vehicle_name"), rs.getShort("device_id"), Misc.getUndefInt(), Misc.getUndefInt());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		// TODO Auto-generated method stub
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
	
	
	public static StringBuilder setSECLDataMulti(Connection conn, byte[] dataByte) throws Exception{
		StringBuilder retval = new StringBuilder();
		//PreparedStatement ps = null;
	    //ResultSet rs = null;
		int recordCount=dataByte[0];
		int recordindex=0;
		int byteindex=1;
		
		while(++recordindex<=recordCount)
		{
			
		
		boolean logStatus=false;
	    RFIDTagInfo tag = null;
	    SECLDataHolder holder = null;
	    byte[] data = null;
	 
		try{
			
			//retval.append("<result>");
			//data = Utils.GetBytesFromBinaryString(dataStr);//+"0010000000100000");
			String vehicleName = null;
			String epcId = null;
			int id=0;
			Pair<Integer, String> vehPair = null;
			if(dataByte != null && dataByte.length > 0){
				tag = new RFIDTagInfo();
				tag.epcId= Arrays.copyOfRange(dataByte, byteindex, byteindex+12);
				byteindex=byteindex+12;
				tag.userData = Arrays.copyOfRange(dataByte, byteindex, byteindex+64);
				byteindex=byteindex+64;
				id=Utils.DecodingBinaryToInt(Utils.getBinaryStrFromByteArray(Arrays.copyOfRange(dataByte, byteindex, byteindex+1)),1);
				byteindex=byteindex+1;
				holder = new SECLDataHolder("", tag,0);
				holder.setId(id);
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
			logStatus=RFIDMasterDao.insert(conn, holder);
			System.out.println("logStatus:"+logStatus);
			if(logStatus!=false)
			{
				retval.append(holder.getId()+",");
			}
			if(holder != null ){
				System.out.println("Web rfid data recieved");
				holder.printData();
				TPRecord tpr =null;//TPRInformation.getTPRForHHWeb(conn, holder.getVehicleId(), holder, holder.getVehicleName());
				if(tpr!=null)
				{
//					if(Misc.isUndef(tpr.getTprId()))
					TPRInformation.insertUpdateTpr(conn, tpr);
					//updateHHLogTprStatus(conn, holder, 1,tpr.getTprId());
				}
				
				
				//RFIDMasterDao.insert(conn, holder);
			}
			conn.commit();
			//retval.append("1");
		}catch(Exception ex){
			ex.printStackTrace();
			retval.append("0");
			throw ex;
		}
		}
		System.out.println("retval="+retval);
		return retval;
	}
	
		/*public static int insertHHLogData(Connection conn, RFIDTagInfo tag, SECLDataHolder holder, int userId, int updatedBy,String epcId) throws Exception{
        String query = " insert into secl_rfid_log (token_id, device_id, vehicle_id, vehicle_name, epc_id, mines, " +
               " transporter,record_time,do_id,challan_id,lr_id,grade,material,load_tare,load_gross,pre_mines, " +
               " pre_device_id,pre_record_id, pre_challan_id,write_status,tag_data,record_user,updated_by,created_on,tpr_status,port_node_id,status) values " +
               " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
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
                    ps.setTimestamp(8,  new Timestamp(System.currentTimeMillis()));//new Timestamp(holder.getDatetime().getTime()));
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
                    Misc.setParamInt(ps, holder.getPreChallanId(), 19);
                    Misc.setParamInt(ps, (holder.getValidityFlag() ? 1 : 0), 20);
                    ps.setBytes(21, tag.userData);
//                  ps.setObject(21, tag.userData);
                    Misc.setParamInt(ps, userId, 22);
                    Misc.setParamInt(ps, updatedBy, 23);
                    ps.setTimestamp(24, new Timestamp(System.currentTimeMillis()));
                    Misc.setParamInt(ps, 0, 25);
                    Misc.setParamInt(ps, 463, 26);
                    Misc.setParamInt(ps, 1, 27);
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
*/
	
	
}

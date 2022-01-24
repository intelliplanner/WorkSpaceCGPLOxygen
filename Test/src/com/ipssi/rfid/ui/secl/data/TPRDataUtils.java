package com.ipssi.rfid.ui.secl.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.beans.DriverDetailBean;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.SECLWorkstationDetails;
import com.ipssi.rfid.beans.TPRTareDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.User;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDException;
import com.ipssi.rfid.readers.RFIDMaster;

/**
 * Created by ipssi11 on 19-Oct-16.
 */
public class TPRDataUtils {
	public static enum WeighmentState{
		gross,
		tare,
		Nothing
	}
	/*public static DO getDoSuggestion(String text) {
        if(text == null || text.length() == 0)
            return null;
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> suggestionList = new ArrayList<>();
        String query = " select id,do_rr_number from do_rr_details where port_node_id=?";
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
            Misc.setParamInt(ps, TokenManager.portNodeId,1);
            rs = ps.executeQuery();
            while (rs.next()) {
                suggestionList.add(rs.getString(2));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Misc.closeRS(rs);
            Misc.closePS(ps);
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return suggestionList;
    }*/
	public static int getDoId(Connection conn, String doNumber) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		int retval = Misc.getUndefInt();
		try {
			ps = conn.prepareStatement("select id from mines_do_details where do_number like ? order by updated_on desc limit 1");
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = Misc.getRsetInt(rs, 1);
			}
			Misc.closeRS(rs);
			Misc.closePS(ps);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return  retval;
	}
	public static int getDriverId(Connection conn, DriverDetailBean driverBean) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String driverName = driverBean.getDriver_name();
		String driverDLNo = driverBean.getDriver_dl_number();
		int driverId = driverBean.getId();
		try {
			if (Misc.isUndef(driverId)) {
				ps = conn.prepareStatement("select id from driver_details where driver_name like ? or driver_dl_number like ? order by updated_on desc limit 1");
				ps.setString(1, driverName);
				ps.setString(2, driverDLNo);
				rs = ps.executeQuery();
				if (rs.next()) {
					driverId = Misc.getRsetInt(rs, 1);
				}
			}
			Misc.closeRS(rs);
			Misc.closePS(ps);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return  driverId;
	}

	public static void saveDriverDetails(Connection conn,int portNodeId,DriverDetailBean driverBean) throws Exception {
		int driverId = getDriverId(conn,driverBean);
		if(!Misc.isUndef(driverId)) {
			DriverDetailBean driverBeanUpd = (DriverDetailBean) RFIDMasterDao.get(conn, DriverDetailBean.class, driverId);
			RFIDMasterDao.mergeNonNull(driverBeanUpd,driverBean);
			driverBeanUpd.setUpdated_on(new Date());
			RFIDMasterDao.update(conn,driverBean);
		}else {
			driverBean.setUpdated_on(new Date());
			driverBean.setOrg_id(portNodeId);
			driverBean.setStatus(Status.ACTIVE);
			driverBean.setCreated_on(new Date());
			RFIDMasterDao.insert(conn,driverBean);
		}
	}
	public static void updateVehicleLoadTare(Connection conn, int vehicleId,double tare,long tareDatetime){
		updateVehicleTare(conn, vehicleId, tare, tareDatetime, true);
	}
	public static void updateVehicleUnloadTare(Connection conn, int vehicleId,double tare,long tareDatetime){
		updateVehicleTare(conn, vehicleId, tare, tareDatetime, true);
	}
	private static void updateVehicleTare(Connection conn, int vehicleId,double tare,long tareDatetime,boolean isLoad) {
		if(Misc.isUndef(vehicleId))
			return;
		PreparedStatement ps = null;
		int index = 1;
		String query = null;
		if(isLoad){
			query = "update vehicle set flyash_tare=?,flyash_tare_time=? where id=?";
		}else{
			query = "update vehicle set unload_tare=?,unload_tare_time=? where id=?";
		}
		try {
			ps = conn.prepareStatement(query);
			ps.setDouble(index++, tare);
			ps.setTimestamp(index++, new Timestamp(tareDatetime));
			ps.setInt(index++, vehicleId);
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closePS(ps);
		}
	}
	public static void main(String[] arg) {
		System.out.println("TPRDataUtils");
	}
	public static DoDetails getDo(Connection conn, int doId,String doNumber) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		DoDetails retval = null;
		try {

			ps = conn.prepareStatement("select id from do_rr_details where do_rr_number like ? order by updated_on desc limit 1");
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			if (rs.next()) {
				//retval = Misc.getRsetInt(rs, 1);
			}
			Misc.closeRS(rs);
			Misc.closePS(ps);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return  retval;
	}
	public static TPSQuestionDetail getQuestionResponse(Connection conn,SECLWorkstationDetails workStation,String workstationMinesCode,TPRecord tpr,Vehicle veh, int workstationType,int questionId, int workstationMaterialCat){
		TPSQuestionDetail retval = null;
		if(veh == null || tpr == null || Misc.isUndef(veh.getId()) || Misc.isUndef(tpr.getVehicleId()))
			return null;
		try{
			switch (questionId) {
			case Status.TPRQuestion.isNoTareAllowed:
				if(
					workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS 
					|| workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS
					|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_GROSS
						){
					TPRTareDetails tareInfo = TPRTareDetails.getTareInfo(conn, tpr, veh, true, workstationMaterialCat);
					return new TPSQuestionDetail(questionId, tareInfo != null && tareInfo.isNoTareAllowed() ? UIConstant.YES : UIConstant.NO);
				}/*else if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_TARE || workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE){
					return new TPSQuestionDetail(questionId, UIConstant.YES);
				}
				//to do for unloadgross*/
				break;
			case Status.TPRQuestion.isAccessAllowed:
				if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.OTHER){
					if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS 
							|| workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_TARE
							|| workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS
							|| workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE
							|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_GROSS
							|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_TARE
							|| workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS
							|| workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_TARE
							){
						return new TPSQuestionDetail(questionId,UIConstant.NO);
					}else{
						return new TPSQuestionDetail(questionId,UIConstant.YES);
					}
				}
				String allowedVehicleMines = null;
				if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.INTERNAL){
					allowedVehicleMines = veh.getPreferedMinesCode();
				}else if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD || veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY){
					DoDetails doDetails = DoDetails.getDODetails(conn, veh.getDoAssigned(),Misc.getUndefInt());
					if(doDetails != null)
						allowedVehicleMines = doDetails.getSourceCode();
				}
				int response = isAccessAllowed(conn, workstationMinesCode, allowedVehicleMines);
				if(response != UIConstant.NO){
					int allowedWorkstationMatCat = Misc.getUndefInt();
					if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS 
							|| workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_TARE
							|| workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS
							|| workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE
							|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_GROSS
							|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_TARE
							|| workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS
							|| workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_TARE
							){
						allowedWorkstationMatCat = workstationMaterialCat;
					}
					if(!Misc.isUndef(allowedWorkstationMatCat)){
						int vehicleMaterialCat =veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD ? Type.TPRMATERIAL.COAL_ROAD : 
            				veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY ? Type.TPRMATERIAL.COAL_WASHERY :
            				Type.TPRMATERIAL.COAL_INTERNAL;   
						response = allowedWorkstationMatCat == vehicleMaterialCat ? UIConstant.YES : UIConstant.NO;
					}
				}
				if(!Misc.isUndef(response))
					return new TPSQuestionDetail(questionId,response);
				break;
			case Status.TPRQuestion.isDoValid:
				if(veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.ROAD || veh.getCardPurpose() == Type.RFID_CARD_PURPOSE.WASHERY){
					DoDetails doDetails = DoDetails.getDODetails(conn, veh.getDoAssigned(), Misc.getUndefInt());
					LatestDOInfo latestDOInfo = DoDetails.getLatestDOInfo(conn, veh.getDoAssigned(), workStation.getCode());
					Pair<Integer,String> isDoValid = doDetails.isDOValid(conn,workStation.getCode(),latestDOInfo);
					int isDOValid = isDoValid == null ? UIConstant.NO : isDoValid.first;
					return new TPSQuestionDetail(questionId,isDOValid); 
				}
				break;
			case Status.TPRQuestion.isTempCardReturned:
				if(veh.getCardPurpose() == Type.RFID_CARD_TYPE.TEMPORARY){
					int issuedTPRID = veh != null && veh.getVehicleRFIDInfo() != null ? veh.getVehicleRFIDInfo().getIssuedTprId() : Misc.getUndefInt();
					if(!Misc.isUndef(issuedTPRID)){
						if(tpr.getTprId() != issuedTPRID){
							return new TPSQuestionDetail(questionId,UIConstant.YES);
						}
					}else{
						long cardIssuedDate = veh.getRfid_issue_date() != null ? veh.getRfid_issue_date().getTime() : Misc.getUndefInt();
						if(!Misc.isUndef(cardIssuedDate) && (System.currentTimeMillis() - cardIssuedDate) > (24*60*60*1000)){
							return new TPSQuestionDetail(questionId,UIConstant.YES);
						}
					}
				}
				break;
			default:
				break;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static int isAccessAllowed(Connection conn,String workstationMinesCode,String vehicleAllowedMinesCode) throws Exception{
		if(Utils.isNull(workstationMinesCode))
			return Misc.getUndefInt();
		int retval = UIConstant.NO;
		if(Utils.isNull(vehicleAllowedMinesCode))
			return retval;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select 1 from mines_details m1 "; 
		try{
			Mines allowedArea = Mines.getMines(conn,vehicleAllowedMinesCode,Misc.getUndefInt());
			if(allowedArea.getType() == Mines.TYPE.MINES.ordinal()){
				return vehicleAllowedMinesCode.equalsIgnoreCase(workstationMinesCode) ? UIConstant.YES : UIConstant.NO;
			}
			if(allowedArea.getType() == Mines.TYPE.AREA.ordinal()){
				query += " left outer join mines_details m2 on (m1.parent_area_code = m2.sn) ";
			}else if(allowedArea.getType() == Mines.TYPE.SUB_AREA.ordinal()){
				query += " left outer join mines_details m2 on (m1.parent_sub_area_code = m2.sn) ";
			}else{
				query += " left outer join mines_details m2 on (m1.parent_mines_code = m2.sn) ";
			}
			query += " where m1.sn=? and m2.sn=?";
			ps = conn.prepareStatement(query);
			ps.setString(1, workstationMinesCode);
			ps.setString(2, vehicleAllowedMinesCode);
			rs = ps.executeQuery();
			if(rs.next())
				retval = UIConstant.YES;
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		return retval;  	
	}
	public static void sendTprDataToServer(String stationCode){
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psDelete = null;
		ResultSet rs = null;
		Connection localConnection = null;
		boolean destroyIt = false;
		Connection remoteConnection = null;
		try{
				remoteConnection = DBConnectionPool.getConnectionFromPoolNonWeb();
				localConnection = NodeDBConnectionChecker.getDBConnection("desktop"); 
				psDelete = localConnection.prepareStatement("delete from tp_record where tpr_id=?");
				psSelect = localConnection.prepareStatement(
						   " select tpr_id, vehicle_id, vehicle_name, transporter_code,challan_no,"+
						   " challan_date,do_number,mines_code,grade_code,destination_code, " +
						   " washery_code,load_tare,load_gross,unload_tare,unload_gross," +
						   " earliest_load_gate_in_in,latest_load_gate_in_out," +
						   " earliest_load_wb_in_in,latest_load_wb_in_out," +
						   " earliest_load_yard_in_in,latest_load_yard_in_out," +
						   " earliest_load_yard_out_in,latest_load_yard_out_out," +
						   " earliest_load_wb_out_in,latest_load_wb_out_out," +
						   " earliest_load_gate_out_in,latest_load_gate_out_out," +
						   " earliest_unload_gate_in_in,latest_unload_gate_in_out," +
						   " earliest_unload_wb_in_in,latest_unload_wb_in_out," +
						   " earliest_unload_yard_in_in,latest_unload_yard_in_out," +
						   " earliest_unload_yard_out_in,latest_unload_yard_out_out," +
						   " earliest_unload_wb_out_in,latest_unload_wb_out_out," +
						   " earliest_unload_gate_out_in,latest_unload_gate_out_out," +
						" tpr_type from tp_record order by tpr_id desc");
				psInsert = remoteConnection.prepareStatement("insert ignore into tp_record_client(station_code, "
						+ " vehicle_id, vehicle_name, transporter_code,challan_no,challan_date,do_number,"
						+ " mines_code,grade_code,destination_code,washery_code,load_tare,"
						+ " load_gross,unload_tare,unload_gross,load_gate_in_entry,load_gate_in_exit,load_tare_entry,"
						+ " load_tare_exit,load_gross_entry,load_gross_exit,load_yard_in_entry,load_yard_in_exit,"
						+ " load_yard_out_entry,load_yard_out_exit,load_gate_out_entry,load_gate_out_exit,unload_gate_in_entry,"
						+ " unload_gate_in_exit,unload_tare_entry,unload_tare_exit,unload_gross_entry,unload_gross_exit,"
						+ " unload_yard_in_entry,unload_yard_in_exit,unload_yard_out_entry,unload_yard_out_exit,"
						+ " unload_gate_out_entry,unload_gate_out_exit,tpr_type) values "
						+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
						+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				rs = psSelect.executeQuery();
				int count = 0;
				while(rs.next()){
					if(count > 100){
						localConnection.commit();
						psDelete.executeBatch();
						psDelete.clearBatch();
						psInsert.executeBatch();
						psInsert.clearBatch();
						count = 0;
					}
					int index = 1;
					int tpr_id = Misc.getRsetInt(rs, index);
					//add for delete
					Misc.setParamInt(psDelete, tpr_id, 1);
					psDelete.addBatch();
					psInsert.setString(index++, stationCode);//stationCode
					Misc.setParamInt(psInsert, Misc.getRsetInt(rs, index), index++);//vehicleId
					psInsert.setString(index, rs.getString(index++));//vehicleName
					psInsert.setString(index, rs.getString(index++));//transporterCode
					psInsert.setString(index, rs.getString(index++));//challan_no
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//challan_date
					psInsert.setString(index, rs.getString(index++));//doNo
					psInsert.setString(index, rs.getString(index++));//minesCode
					psInsert.setString(index, rs.getString(index++));//gradeCode
					psInsert.setString(index, rs.getString(index++));//destinationCode
					psInsert.setString(index, rs.getString(index++));//washeryCode
					psInsert.setDouble(index, rs.getDouble(index++));//load_tare
					psInsert.setDouble(index, rs.getDouble(index++));//load_gross
					psInsert.setDouble(index, rs.getDouble(index++));//unload_tare
					psInsert.setDouble(index, rs.getDouble(index++));//unload_gross
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadGateInEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadGateInExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadWBInEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadWBInExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadYardInEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadYardInExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadYardOutEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadYardOutExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadWbOutEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadWbOutExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadGateOutEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//loadGateOutExit
					
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadGateInEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadGateInExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadWBInEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadWBInExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadYardInEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadYardInExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadYardOutEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadYardOutExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadWbOutEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadWbOutExit
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadGateOutEntry
					psInsert.setTimestamp(index, rs.getTimestamp(index++));//unloadGateOutExit
					Misc.setParamInt(psInsert, Misc.getRsetInt(rs, index), index++);
					psInsert.addBatch();
					count++;
				}
				if(count > 0){
					localConnection.commit();
					psDelete.executeBatch();
					psDelete.clearBatch();
					psInsert.executeBatch();
					psInsert.clearBatch();
					count = 0;
				}
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(psSelect);
			Misc.closePS(psInsert);
			Misc.closePS(psDelete);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(remoteConnection, destroyIt);
				NodeDBConnectionChecker.returnConnection(localConnection, destroyIt);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public static boolean updateTPRQuestion(Connection conn, int tprId, int workstationTypeId, int questionId, int answerId, int userBy) throws Exception{
		TPSQuestionDetail tpsQuestionBean = null;
		boolean isInsert = false;
		try{
			tpsQuestionBean = new TPSQuestionDetail();
            tpsQuestionBean.setTprId(tprId);
            tpsQuestionBean.setTpsId(workstationTypeId);
            tpsQuestionBean.setQuestionId(questionId);
            tpsQuestionBean.setAnswerId(answerId);
            tpsQuestionBean.setUpdatedBy(userBy);
            System.out.println("End quesId :" + questionId + "ansId :" + answerId);
            RFIDMasterDao.executeQuery(conn, "delete from tps_question_detail where tpr_id="+tprId+" and question_id="+questionId);
            isInsert = RFIDMasterDao.insert(conn, tpsQuestionBean,false);
            RFIDMasterDao.executeQuery(conn, "delete from tps_question_detail_apprvd where tpr_id="+tprId+" and question_id="+questionId);
            isInsert = RFIDMasterDao.insert(conn, tpsQuestionBean,true);
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		return isInsert;
	}

	public static void insertReadings(Connection conn, int tprId,ArrayList<Pair<Long, Integer>> readings) {
		if(readings == null || readings.size() <= 0 || Misc.isUndef(tprId) || conn == null)
			return;
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("insert into tpr_wb_readings (tpr_id,capture_time,reading) values (?,?,?) ");
			for(Pair<Long, Integer> reading : readings ){
				Misc.setParamInt(ps, tprId, 1);
				ps.setTimestamp(2, new Timestamp(reading.first));
				Misc.setParamInt(ps, reading.second, 3);
				ps.addBatch();
			}
			ps.executeBatch();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static int insertVehicleSCEL(Connection conn, String vehiclename, int userBy) throws Exception {
		return insertVehicleSCEL(conn, vehiclename, userBy, TokenManager.portNodeId);
	}
    public static int insertVehicleSCEL(Connection conn, String vehiclename, int userBy,int portNodeId) throws Exception {
        System.out.println(" ######## Insert New Vehicle New ######");
        if(vehiclename == null || vehiclename.length() == 0)
            return Misc.getUndefInt();
        Vehicle vehicleBean = new Vehicle();
        VehicleExtended vehicleExtend = null;
        java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
        try {
            vehicleBean.setVehicleName(vehiclename);
            vehicleBean.setStdName(CacheTrack.standardizeName(vehiclename));
            vehicleBean.setUpdatedBy(userBy);
            vehicleBean.setUpdatedOn(now);
            vehicleBean.setRfidTempStatus(100);
            vehicleBean.setStatus(1);
            vehicleBean.setCreatedOn(now);
            vehicleBean.setCustomerId(portNodeId);
            boolean isInserted = RFIDMasterDao.insert(conn, vehicleBean,false);
            conn.commit();
            if (isInserted) {
                vehicleExtend = new VehicleExtended();
                vehicleExtend.setVehicleId(vehicleBean.getId());
                RFIDMasterDao.insert(conn, vehicleExtend,false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return vehicleBean.getId();
    }
    public static enum TAG_ISSUE_RESPONSE{
        ISSUED,
        NOT_ISSUED,
        READER_NOT_CONNECTED,
        NO_TAG,
        MULTIPLE_TAG
    }
    public int tagIssued(Vehicle vehicleBean) throws RFIDException{
        TAG_ISSUE_RESPONSE response = issueRFIDTag(vehicleBean);
        return response == null ? Misc.getUndefInt() :response.ordinal();
    }

    public static TAG_ISSUE_RESPONSE issueRFIDTag(Vehicle vehicleBean) throws RFIDException{
        TAG_ISSUE_RESPONSE response = null;
        RFIDHolder Holder = new RFIDHolder();
        Holder.setVehicleName(vehicleBean.getVehicleName());
        Holder.setAvgGross(Misc.isUndef(vehicleBean.getAvgGross()) ? 0 : (int)Math.round(vehicleBean.getAvgGross()));
        Holder.setAvgTare(Misc.isUndef(vehicleBean.getAvgTare()) ? 0 : (int)Math.round(vehicleBean.getAvgTare()));
        Holder.setTransporterId(vehicleBean.getTransporterId());
        if(RFIDMaster.getDesktopReader() != null){
            ArrayList<String> tags = RFIDMaster.getDesktopReader().getRFIDTagList();
            int size = tags == null ? 0 : tags.size();
            if(size == 1){
            	String s = tags.get(0);
                Holder.setEpcId(s);
                RFIDTagInfo rfidTagInfo = Holder.createTag(0);
                boolean isWrite = RFIDMaster.getDesktopReader().writeCardG2(rfidTagInfo, 5);
              if(isWrite){
            	  vehicleBean.setLastEPC(vehicleBean.getEpcId());
            	  vehicleBean.setEpcId(s);
            	  response = TAG_ISSUE_RESPONSE.ISSUED;
              }
              else{
                  response = TAG_ISSUE_RESPONSE.NOT_ISSUED;
              }
            }else if(size == 0){
                response = TAG_ISSUE_RESPONSE.NO_TAG;
            }else{
                response = TAG_ISSUE_RESPONSE.MULTIPLE_TAG;
            }
        }else{
            response = TAG_ISSUE_RESPONSE.READER_NOT_CONNECTED;
        }
      return response;
    }
    public static User Login(Connection conn, String username, String password) throws GenericException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        User retval = null;
        try {
            ps = conn.prepareStatement("select id,name from users where username=? and password=? and isactive=1");
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if(rs.next()) {
                retval = new User(Misc.getRsetInt(rs,1), username, rs.getString(2), false, true) ;
                retval.setSupperUser(isSuperUser(conn,retval.getId()));
                retval.setPrivList(getPrivList(conn,retval.getId()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Misc.closeRS(rs);
            Misc.closePS(ps);
        }
        return retval;
    }
    public static Pair<Integer, String>  Login(Connection conn, String username1, char[] password1) throws GenericException {
        ResultSet rs = null;
        PreparedStatement ps = null;
        int userId = Misc.getUndefInt();
        String name = null;
        try {
            ps = conn.prepareStatement("select id,name from users where username=? and password=? and isactive=1");
            ps.setString(1, username1);
            ps.setString(2, new String(password1));
            rs = ps.executeQuery();
            if(rs.next()) {
                userId = rs.getInt(1);
                name = rs.getString(2);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        	try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return new Pair<Integer, String>(userId, name);
    }
    public static ArrayList<Integer> getPrivList(Connection conn,int userId){
		ArrayList<Integer> retval = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	//check if it is super user
        	ps = conn.prepareStatement("select role_privs.priv_id from users join user_roles on (user_roles.user_1_id=users.id)  join role_privs on (user_roles.role_id = role_privs.role_id) where users.id=?");
        	Misc.setParamInt(ps, userId, 1);
        	rs = ps.executeQuery();
        	while(rs.next()){
        		if(retval == null)
        			retval = new ArrayList<Integer>();
        		retval.add(Misc.getRsetInt(rs, 1));
        	}
        } catch (Exception e) {
              e.printStackTrace();
        }
		return retval;
	}
	public static boolean isSuperUser(Connection conn, int userId) {
		if(userId == 1)
			return true;
		boolean retval = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	//check if it is super user
        	ps = conn.prepareStatement("select user_roles.role_id from users join user_roles on (user_roles.user_1_id=users.id)  where user_roles.role_id=1 and users.id=?");
        	Misc.setParamInt(ps, userId, 1);
        	rs = ps.executeQuery();
        	if(rs.next()){
        		return true;
        	}
        } catch (Exception e) {
              e.printStackTrace();
        }
		return retval;
	}
	
	public static boolean updateDORemaining(Connection conn, DoDetails doDetails, String wbCode, double net) {
		if(Utils.isNull(wbCode) || doDetails == null)
			return false;
		boolean retval = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	//check if it is super user
    		if(wbCode.equalsIgnoreCase(doDetails.getPreferedWb1()))
    			ps = conn.prepareStatement("update mines_do_details set prefered_wb_1_qty=prefered_wb_1_qty-? where do_number=?");
    		else if(wbCode.equalsIgnoreCase(doDetails.getPreferedWb2()))
    			ps = conn.prepareStatement("update mines_do_details set prefered_wb_2_qty=prefered_wb_2_qty-? where do_number=?");
    		else if(wbCode.equalsIgnoreCase(doDetails.getPreferedWb3()))
    			ps = conn.prepareStatement("update mines_do_details set prefered_wb_3_qty=prefered_wb_3_qty-? where do_number=?");
    		else if(wbCode.equalsIgnoreCase(doDetails.getPreferedWb4()))
    			ps = conn.prepareStatement("update mines_do_details set prefered_wb_4_qty=prefered_wb_4_qty-? where do_number=?");
    		Misc.setParamDouble(ps, net, 1);
    		ps.setString(2, doDetails.getDoNumber());
    		ps.executeUpdate();
    		Misc.closePS(ps);
        } catch (Exception e) {
              e.printStackTrace();
        }
		return retval;
	}
}

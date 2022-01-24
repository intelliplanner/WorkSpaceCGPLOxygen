package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SeclRemoteConnManager;
import com.ipssi.rfid.db.DBSchemaManager;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.Utils;
import java.sql.DatabaseMetaData;

public class DOUpdInfo {
	
	private String doNumber;
	private int doId;// for ease of use of lookup 
	private String wbCode; 
	private int wbId;//for ease of use of lookup 
	private double currentAllocation = Misc.getUndefDouble(); 
	private double lastQtyLifted = Misc.getUndefDouble(); 
	private int lockStatus; //till above if field is undef then don't update that field on the WB 
	private int resultStatus; 
	private String resultMessage; //initially undef and put result of status - if succ then 0 else some message 
	//private double allocationQty;
	//private double alreadyLiftedQty;
	//private double clientAllocationQty;
	//private int syncStatus;
	public static final int UNLOCK = 0;
	public static final int LOCK = 1;
	public static final int SUCC = 1;
	public static final int FAIL = 0;
	
	private boolean doIncrementCurrentAlloc = false;
	private boolean doIncrementLastQtyLifted = false;
	/*
	  1.	Scenario 1 – by mistake a trip was booked – need to “cancel” trip – but also adjust current_do_status
	  2.	Scenario 2 – by mistake a trip was booked in incorrect DO and vehicle already left – need to change DO
	  3.	Scenario 3 – tare weighment of washery/internal vehicles
	  4.	Scenario 4 – change of vehicle in trip
	 */
	
	private final byte TPR_CANCEL = 0x1;
	private final byte TPR_DO_CHANGE = 0x2;
	private final byte TPR_VEHICLE_CHANGE = 0x4;
	private final byte TPR_WB_CHANGE = 0x8;
	
	public static void mergeTPRWithRemote(Connection localConn ,String challanNo, String wbCode){
		// load TPR using conn
		// get remoteConn for wbCode
		// call updTPR()
	}
	public void synTPRDataChanges(Connection server, Connection desktop, int tprId) throws Exception{
		System.out.println("[DB][SYNC][TPR][CHANGE]-start");
		if(Misc.isUndef(tprId))
	    	return;
		TPRecord tpr = (TPRecord) RFIDMasterDao.get(server, TPRecord.class, tprId);
		int vehicleId = TPRInformation.getVehicleByName(desktop, tpr.getVehicleName());
		int mergedTprId = TPRInformation.getTprIdByChallan(desktop,tpr.getChallanNo(),vehicleId);
		if(tpr == null || Misc.isUndef(mergedTprId) || Misc.isUndef(vehicleId))
			return;
		tpr.setTprId(mergedTprId);
		tpr.setVehicleId(vehicleId);
		RFIDMasterDao.update(desktop, tpr, false);
		DOUpdInfo.handleChangesTPR(desktop, tpr.getChallanNo());
		RFIDMasterDao.update(desktop, tpr, true);
		System.out.println("[DB][SYNC][TPR][CHANGE]-end");
	}
	public static void handleChangesTPR(Connection conn, String challanNo){
		if(conn == null || Utils.isNull(challanNo))
			return;
		int tprId = TPRInformation.getTprIdByChallan(conn, challanNo, Misc.getUndefInt());
		if(!Misc.isUndef(tprId)){
			PreparedStatement ps = null;
			ResultSet rs = null;
			try{
				ps = conn.prepareStatement("select "
						+ "   (case when (t0.load_tare != t1.load_tare or t0.load_gross != t1.load_gross) then 1 else 0 end) wt_change"
						+ " , (case when t0.status!=1 and t1.status=1 then 2 when t0.status=1 and t1.status!=1 then 1 else 0 end) status_change"
						+ " , (case when t0.do_number is not null and t1.do_number is not null and t0.do_number not like t1.do_number then 1 else 0 end) do_change"
						+ " , (case when t0.load_wb_out_name is not null and t1.load_wb_out_name is not null and t0.load_wb_out_name not like t1.load_wb_out_name then 1 else 0 end) "
						+ " , (case when t0.vehicle_name is not null and t1.vehicle_name is not null and t0.vehicle_name not like t1.vehicle_name then 1 else 0 end) "
						+ " , t0.do_number, t0.load_wb_out_name, t0.vehicle_name "
						+ "   from tp_record t0 left outer join tp_record_apprvd t1 on (t0.tpr_id=t1.tpr_id) where t0.tpr_id=?");
				Misc.setParamInt(ps, tprId, 1);
				rs = ps.executeQuery();
				int wtChange = Misc.getUndefInt();
				int statusChange = Misc.getUndefInt();
				int doChange = Misc.getUndefInt();
				int wbChange = Misc.getUndefInt();
				int vehicleChange = Misc.getUndefInt();
				String targetDoNumber = null;
				String targetWB = null;
				String targetVehicle = null;
				if(rs.next()){
					wtChange = Misc.getRsetInt(rs, 1);
					statusChange = Misc.getRsetInt(rs, 2);
					doChange = Misc.getRsetInt(rs, 3);
					wbChange = Misc.getRsetInt(rs, 4);
					vehicleChange = Misc.getRsetInt(rs, 5);
					targetDoNumber = rs.getString(6);
					targetWB = rs.getString(7);
					targetVehicle = rs.getString(8);
				}
				if(wtChange > 0 || statusChange > 0 || doChange > 0 || wbChange > 0 || vehicleChange > 0){
					//cancel tpr
					cancelTPR(conn, tprId);
					/*if(wtChange == 1)
						changeTPRWeight(conn, tprId);*/
					//tpr status change
					if(wtChange == 1 || statusChange == 1 || doChange == 1 || wbChange == 1)
						handleTPRQty(conn, tprId);
					//tpr change vehicle
				}
				if(vehicleChange == 1)
					changeTPRVehicle(conn, tprId, targetVehicle);
			}catch(Exception ex){
				ex.printStackTrace();
			}finally{
				Misc.closeRS(rs);
				Misc.closePS(ps);
			}
		}
	}
	public static boolean activateTPR(Connection conn, int tprId) throws Exception{
		if(Utils.isNull(tprId))
			return false;
		PreparedStatement ps = null;
		try{
			if(true){//cancel tpr and handle wt changes
				ps = conn.prepareStatement("update current_do_status cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);

				/*ps = conn.prepareStatement("update tp_record_apprvd set status=? where tpr_id=?");
					Misc.setParamInt(ps, 1, 1);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();
					Misc.closePS(ps);

					ps = conn.prepareStatement("update tp_record set status=? where tpr_id=?");
					Misc.setParamInt(ps, 1, 1);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();*/
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
		}
		return true;
		//check vehicle
	}
	public static boolean cancelTPR(Connection conn, int tprId) throws Exception{
		if(Utils.isNull(tprId))
			return false;
		PreparedStatement ps = null;
		try{
			if(true){//cancel tpr and handle wt changes
				ps = conn.prepareStatement("update current_do_status cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);

				/*ps = conn.prepareStatement("update tp_record_apprvd set status=? where tpr_id=?");
					Misc.setParamInt(ps, 0, 1);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();
					Misc.closePS(ps);

					ps = conn.prepareStatement("update tp_record set status=? where tpr_id=?");
					Misc.setParamInt(ps, 0, 1);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();*/
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
		}
		return true;
		//check vehicle
	}
	public static boolean handleTPRQty(Connection conn, int tprId) throws Exception{
		if(Utils.isNull(tprId))
			return false;
		PreparedStatement ps = null;
		try{
			if(true){
				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
		}
		return true;
	}
	public static boolean changeTPRDONumber(Connection conn, int tprId, String doNumber) throws Exception{
		if(Utils.isNull(tprId))
			return false;
		PreparedStatement ps = null;
		try{
			if(true){//
/*				ps = conn.prepareStatement("update current_do_status cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.do_number != ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, doNumber);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd_apprvd cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.do_number != ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, doNumber);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);*/

				/*ps = conn.prepareStatement("update tp_record_apprvd set do_number=? where tpr_id=?");
					ps.setString(1, doNumber);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();
					Misc.closePS(ps);

					ps = conn.prepareStatement("update tp_record set do_number=? where tpr_id=?");
					ps.setString(1, doNumber);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();*/

				ps = conn.prepareStatement("update current_do_status cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.do_number = ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, doNumber);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.do_number = ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, doNumber);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
		}
		return true;
	}
	public static boolean changeTPRWB(Connection conn, int tprId, String wbCode) throws Exception{
		if(Utils.isNull(tprId))
			return false;
		PreparedStatement ps = null;
		try{
			if(true){
/*				ps = conn.prepareStatement("update current_do_status cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.load_wb_out_name != ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, wbCode);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.load_wb_out_name != ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, wbCode);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);*/

				/*ps = conn.prepareStatement("update tp_record_apprvd set load_wb_out_name=? where tpr_id=?");
					ps.setString(1, wbCode);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();
					Misc.closePS(ps);

					ps = conn.prepareStatement("update tp_record set load_wb_out_name=? where tpr_id=?");
					ps.setString(1, wbCode);
					Misc.setParamInt(ps, tprId, 2);
					ps.executeUpdate();*/

				ps = conn.prepareStatement("update current_do_status cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.load_wb_out_name = ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, wbCode);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.load_wb_out_name = ? and tp.tpr_id=? and tp.status=1");
				ps.setString(1, wbCode);
				Misc.setParamInt(ps, tprId, 2);
				ps.executeUpdate();
				Misc.closePS(ps);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
		}
		return true;
	}
	public static boolean changeTPRWeight(Connection conn, int tprId) throws Exception{
		if(Utils.isNull(tprId))
			return false;
		PreparedStatement ps = null;
		try{
			if(true){
/*				ps = conn.prepareStatement("update current_do_status cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where  tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record_apprvd tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty - (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);*/

				ps = conn.prepareStatement("update current_do_status cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);

				ps = conn.prepareStatement("update current_do_status_apprvd cds join tp_record tp on (cds.do_number=tp.do_number and cds.wb_code=tp.load_wb_out_name) set "
						+ " cds.lifted_qty = cds.lifted_qty + (case when tp.load_tare is not null and tp.load_gross is not null and tp.load_gross > tp.load_tare then (tp.load_gross-tp.load_tare) else 0.0 end )"
						+ " where tp.tpr_id=? and tp.status=1");
				Misc.setParamInt(ps, tprId, 1);
				ps.executeUpdate();
				Misc.closePS(ps);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
		}
		return true;
	}
	
	public static void changeTPRVehicle(Connection conn, int tprId, String vehicleName){
		
	}
	
public static void sendToWB(Connection conn, ArrayList <Pair<Integer, ArrayList<DOUpdInfo>>> updateThis, boolean doLockStatus){
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList <Pair<Integer,Integer>> retVal = new ArrayList <Pair<Integer,Integer>>();
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Start");

		int wbId = Misc.getUndefInt();
		ArrayList<DOUpdInfo> doUpdInfoList = null;
		String query = "select code from secl_workstation_details where id= ?";

		for (Iterator iterator = updateThis.iterator(); iterator.hasNext();) {
			Pair<Integer, ArrayList<DOUpdInfo>> doDetails = (Pair<Integer, ArrayList<DOUpdInfo>>) iterator.next();
			wbId = doDetails.first;
			doUpdInfoList = doDetails.second;
			if(wbId != Misc.getUndefInt()){
				
				//RemoteCredential uCred = null;
				try{
					ps = conn.prepareStatement(query);
					ps.setInt(1, wbId);
					rs = ps.executeQuery();
					String wbCode = null;
					if (rs.next()) {
						wbCode = rs.getString(1);
					}
					rs = Misc.closeRS(rs);
					ps = Misc.closePS(ps);
					if (wbCode != null) {
						ArrayList<SeclRemoteConnManager.Station> stationList = SeclRemoteConnManager.getStationAndParentInfo(conn, wbCode);
					
						for (int j1=0,j1s=stationList == null ? 0 : stationList.size(); j1<j1s;j1++) {
							SeclRemoteConnManager.Station station = stationList.get(j1);
							if (station.isSameAsMeMachine(conn))
								continue;
							if(doUpdInfoList != null && doUpdInfoList.size() > 0){
								for (int i1=0,i1s=doUpdInfoList.size(); i1<i1s; i1++) {
									DOUpdInfo doUpdInfo = doUpdInfoList.get(i1);
									doUpdInfo.resultStatus = FAIL;
								}
							
								Connection remoteConn = null;
								boolean destroyRemote = false;
								try{
									remoteConn = station.getConnection();//getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
									
									for (int i1=0,i1s=remoteConn == null || doUpdInfoList == null ? 0 : doUpdInfoList.size(); i1<i1s; i1++) {
										DOUpdInfo doUpdInfo = doUpdInfoList.get(i1);
										System.out.println("[DOManager].[updateCurrDOStatus] Start ... first inserting/update doInfo:"+doUpdInfo.doNumber);
										DOUpdInfo.updateDOInfoInRemote(conn, remoteConn, doUpdInfo.doNumber);
										if (!doLockStatus) {
											System.out.println("[DOManager].[updateCurrDOStatus] Start ...  inserting/update curr_do_status::"+doUpdInfo.doNumber);
											upsertCurrDOStatus(conn, remoteConn, wbCode, doUpdInfo);
										}
										remoteConn.commit();
										doUpdInfo.resultStatus = SUCC;
									}
								}
								catch (Exception e2) {
									e2.printStackTrace();
									destroyRemote = true;
									throw e2;
								}
								finally {
									remoteConn.rollback();
									remoteConn.close();
									remoteConn = null;
								}
								DOUpdInfo.saveCommunicationResult(doDetails,conn, station.getCode());
							}//if doupdInfoList is not null
						}
					}//if wbCode != null
				}
				catch(Exception e){
//					retVal.add(new Pair(wbId, FAIL));
					System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
					e.printStackTrace();
				}
				
			}else{
				System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Wb id is null: ");
			}
		}
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] retVal: "+ retVal);
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] End");
		//return retVal;
	}

public static void saveCommunicationResult(Pair<Integer, ArrayList<DOUpdInfo>> updTo, Connection conn, String sendToWBCode)  {
	PreparedStatement psGetWbCode = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	try {
		ps = conn.prepareStatement("insert into secl_remote_comm_result(do_id, do_number, wb_id, wb_code, lifted_qty, alloc_qty, lock_status, result_code, sent_to_wb_id, sent_to_wb_code, created_on) "+
				"values (?,?,?,?,?,?,?,?,?,?,now())"
				);
		psGetWbCode = conn.prepareStatement("select code from secl_workstation_details where id = ? ");
		psGetWbCode.setInt(1, updTo.first);
		String toWbCode = null;
		rs = psGetWbCode.executeQuery();
		if (rs.next()) {
			toWbCode = rs.getString(1);
		}
		rs = Misc.closeRS(rs);
		for (int j=0,js=updTo.second == null ? 0 : updTo.second.size(); j<js; j++) {
			DOUpdInfo info = updTo.second.get(j);
			Misc.setParamInt(ps, info.getDoId(), 1);
			ps.setString(2, info.getDoNumber());
			Misc.setParamInt(ps, info.getWbId(), 3);
			ps.setString(4, info.getWbCode());
			Misc.setParamDouble(ps, info.getLastQtyLifted(), 5);
			Misc.setParamDouble(ps, info.getCurrentAllocation(), 6);
			Misc.setParamInt(ps, info.getLockStatus(), 7);
			Misc.setParamInt(ps, info.getResultStatus(), 8);
			Misc.setParamInt(ps, updTo.first, 9);
			ps.setString(10, sendToWBCode);
			ps.addBatch();
		}
		ps.executeBatch();
		ps = Misc.closePS(ps);
		if (!conn.getAutoCommit())
			conn.commit();
	}
	catch (Exception e) {
		e.printStackTrace();
		//eat it
	}
	finally {
		ps = Misc.closePS(ps);
	}
}
	public static void sendToWBPreHierarchy(Connection conn, ArrayList <Pair<Integer, ArrayList<DOUpdInfo>>> updateThis, boolean doLockStatus){
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList <Pair<Integer,Integer>> retVal = new ArrayList <Pair<Integer,Integer>>();
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Start");

		int wbId = Misc.getUndefInt();
		ArrayList<DOUpdInfo> doUpdInfoList = null;
		String query = "select sisd.id,sd.ip, sd.port, sd.db, sd.user_id, sd.password, sisd.code from secl_workstation_details sisd join secl_ip_details sd on (sisd.uid = sd.mac_id) where sisd.status = 1 and sd.status = 1 and sisd.id = ?" ;

		for (Iterator iterator = updateThis.iterator(); iterator.hasNext();) {
			Pair<Integer, ArrayList<DOUpdInfo>> doDetails = (Pair<Integer, ArrayList<DOUpdInfo>>) iterator.next();
			wbId = doDetails.first;
			doUpdInfoList = doDetails.second;
			if(wbId != Misc.getUndefInt()){
				RemoteCredential uCred = null;
				try{
					ps = conn.prepareStatement(query);
					ps.setInt(1, wbId);
					rs = ps.executeQuery();
					while(rs.next()){
						String ip = rs.getString("ip");
						if(Utils.isMyIp(ip))
							continue;
						int id = Misc.getRsetInt(rs, "id");
						String port = rs.getString("port");
						String db = rs.getString("db");
						String userId = rs.getString("user_id");
						String password = rs.getString("password");
						String wbCode = rs.getString("code");
						uCred = new RemoteCredential(id,ip,port,db,userId,password,wbCode);
					}
					Misc.closeRS(rs);
					Misc.closePS(ps);

					
					if(doUpdInfoList != null && doUpdInfoList.size() > 0 && uCred != null){
						for (int i1=0,i1s=doUpdInfoList.size(); i1<i1s; i1++) {
							DOUpdInfo doUpdInfo = doUpdInfoList.get(i1);
							doUpdInfo.resultStatus = FAIL;
						}
					
						Connection remoteConn = null;
						boolean destroyRemote = false;
						try{
							remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
							for (int i1=0,i1s=doUpdInfoList.size(); i1<i1s; i1++) {
								DOUpdInfo doUpdInfo = doUpdInfoList.get(i1);
								System.out.println("[DOManager].[updateCurrDOStatus] Start ... first inserting/update doInfo:"+doUpdInfo.doNumber);
								DOUpdInfo.updateDOInfoInRemote(conn, remoteConn, doUpdInfo.doNumber);
								if (!doLockStatus) {
									System.out.println("[DOManager].[updateCurrDOStatus] Start ...  inserting/update curr_do_status::"+doUpdInfo.doNumber);
									upsertCurrDOStatus(conn, remoteConn, uCred.wbCode, doUpdInfo);
								}
								remoteConn.commit();
								doUpdInfo.resultStatus = SUCC;
							}
						}
						catch (Exception e2) {
							e2.printStackTrace();
							destroyRemote = true;
							throw e2;
						}
						finally {
							remoteConn.rollback();
							remoteConn.close();
							remoteConn = null;
						}
					}//if doupdInfoList is not null
//					retVal.add(new Pair(wbId, SUCC));
				}catch(Exception e){
//					retVal.add(new Pair(wbId, FAIL));
					System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
					e.printStackTrace();
				}
				
			}else{
				System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Wb id is null: ");
			}
		}
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] retVal: "+ retVal);
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] End");
		//return retVal;
	}

	public static void sendToWBOld(Connection conn, ArrayList <Pair<Integer, ArrayList<DOUpdInfo>>> updateThis, boolean doLockStatus){

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList <Pair<Integer,Integer>> retVal = new ArrayList <Pair<Integer,Integer>>();
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Start");

		int wbId = Misc.getUndefInt();
		ArrayList<DOUpdInfo> doUpdInfoList = null;
		String query = "select sisd.id,sd.ip, sd.port, sd.db, sd.user_id, sd.password, sisd.code from secl_workstation_details sisd join secl_ip_details sd on (sisd.uid = sd.mac_id) where sisd.status = 1 and sd.status = 1 and sisd.id = ?" ;

		for (Iterator iterator = updateThis.iterator(); iterator.hasNext();) {
			Pair<Integer, ArrayList<DOUpdInfo>> doDetails = (Pair<Integer, ArrayList<DOUpdInfo>>) iterator.next();
			wbId = doDetails.first;
			doUpdInfoList = doDetails.second;
			if(wbId != Misc.getUndefInt()){
				RemoteCredential uCred = null;
				try{
					ps = conn.prepareStatement(query);
					ps.setInt(1, wbId);
					rs = ps.executeQuery();
					while(rs.next()){
						String ip = rs.getString("ip");
						if(Utils.isMyIp(ip))
							continue;
						int id = Misc.getRsetInt(rs, "id");
						String port = rs.getString("port");
						String db = rs.getString("db");
						String userId = rs.getString("user_id");
						String password = rs.getString("password");
						String wbCode = rs.getString("code");
						uCred = new RemoteCredential(id,ip,port,db,userId,password,wbCode);
					}
					Misc.closeRS(rs);
					Misc.closePS(ps);
					updateCurrDOStatus(conn, uCred, doUpdInfoList,false, doLockStatus);
					updateCurrDOStatus(conn, uCred, doUpdInfoList,true, doLockStatus);
					
//					retVal.add(new Pair(wbId, SUCC));
				}catch(Exception e){
//					retVal.add(new Pair(wbId, FAIL));
					System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
					e.printStackTrace();
				}
				
			}else{
				System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Wb id is null: ");
			}
		}
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] retVal: "+ retVal);
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] End");
		//return retVal;
	}
	
	public static ArrayList<Pair<Boolean, Integer>>  migrateOpenTPR(Connection conn, String fromWbCode, String toWbCode, ArrayList<Integer> optionalOnlyMigrateTheseDo) throws Exception{
		
		ArrayList<Pair<Boolean, Integer>> retVal = new ArrayList<Pair<Boolean, Integer>>();
		
		if(optionalOnlyMigrateTheseDo ==  null || optionalOnlyMigrateTheseDo.size() == 0){
			
		}
		String schema = "ipssi_secl";
		StringBuilder query = new StringBuilder("select tp_record.* from tp_record where ( ");
				query.append("load_gate_in_name = '").append(fromWbCode);
		query.append("' or load_wb_in_name = '").append(fromWbCode).append("' or load_yard_in_name = '").append(fromWbCode);
		query.append("' or load_wb_out_name = '").append(fromWbCode).append("' or load_gate_out_name = '").append(fromWbCode);
		query.append("' or unload_gate_in_name = '").append(fromWbCode).append("' or unload_wb_in_name = '").append(fromWbCode);
		query.append("' or unload_yard_in_name = '").append(fromWbCode).append("' or unload_wb_out_name = '").append(fromWbCode);
		query.append("' or unload_gate_out_name = '").append(fromWbCode).append("' ) and is_latest = 1 and tpr_status = 0 ");
		
		Pair<java.sql.Timestamp,ArrayList<TPRecord>> tprListPair = DBSchemaManager.getList(conn, TPRecord.class, schema, query);
		if(tprListPair == null)
			return retVal;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ArrayList<SeclRemoteConnManager.Station> stationsList = SeclRemoteConnManager.getStationAndParentInfo(conn, toWbCode);
			
			for (int i1=0,i1s=stationsList == null ? 0 : stationsList.size(); i1<i1s;i1++) {
				SeclRemoteConnManager.Station station = stationsList.get(i1);
				if (station == null || station.isSameAsMeMachine(conn))
					continue;
			
				Connection remoteConn = null;
				boolean destroyRemote = false;
				try{
					destroyRemote = false;
					remoteConn = station.getConnection();//getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				PreparedStatement setAllowDiffWBPS = remoteConn.prepareStatement("update tp_record set allow_gross_tare_diff_wb=1 where tpr_id=?");
				PreparedStatement setAllowDiffWBPSApprvd = remoteConn.prepareStatement("update tp_record_apprvd set allow_gross_tare_diff_wb=1 where tpr_id=?");
				for (int i = 0,is=tprListPair == null || tprListPair.second == null ? 0 : tprListPair.second.size(); i < is; i++) {
					TPRecord tpr = tprListPair.second.get(i);
					TPRInformation.handleMergeSECL(remoteConn, tpr, 0L,conn,Integer.MAX_VALUE);
					setAllowDiffWBPS.setInt(1, tpr.getTprId());
					setAllowDiffWBPSApprvd.setInt(1, tpr.getTprId());
					setAllowDiffWBPS.addBatch();
					setAllowDiffWBPSApprvd.addBatch();					
				}
				setAllowDiffWBPS.executeBatch();
				setAllowDiffWBPSApprvd.executeBatch();
				setAllowDiffWBPS = Misc.closePS(setAllowDiffWBPS);
				setAllowDiffWBPSApprvd = Misc.closePS(setAllowDiffWBPSApprvd);
				remoteConn.commit();	
				}catch (Exception e){
					// updateStatus(conn,uCred.id,3);
					destroyRemote = true;
					System.out.println("[DOManager][copyTprToWorkStation] Error while updating remote machine: "+"[Thread:"+Thread.currentThread().getId()+"] uCred: "+station+" remoteConn: "+remoteConn);
					e.printStackTrace();
					//retVal = false;
				}finally {
					try {
						returnConnection(remoteConn, destroyRemote);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}catch(Exception e){
				System.out.println("[DOManager].[copyTprToWorkStation] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
				e.printStackTrace();
				//retVal = false;
			}
		return retVal;
	}

public static ArrayList<Pair<Boolean, Integer>>  migrateOpenTPRPreHierarchy(Connection conn, String fromWbCode, String toWbCode, ArrayList<Integer> optionalOnlyMigrateTheseDo) throws Exception{
	
	ArrayList<Pair<Boolean, Integer>> retVal = new ArrayList<Pair<Boolean, Integer>>();
	
	if(optionalOnlyMigrateTheseDo ==  null || optionalOnlyMigrateTheseDo.size() == 0){
		
	}
	String schema = "ipssi_secl";
	StringBuilder query = new StringBuilder("select tp_record.* from tp_record where ( ");
			query.append("load_gate_in_name = '").append(fromWbCode);
	query.append("' or load_wb_in_name = '").append(fromWbCode).append("' or load_yard_in_name = '").append(fromWbCode);
	query.append("' or load_wb_out_name = '").append(fromWbCode).append("' or load_gate_out_name = '").append(fromWbCode);
	query.append("' or unload_gate_in_name = '").append(fromWbCode).append("' or unload_wb_in_name = '").append(fromWbCode);
	query.append("' or unload_yard_in_name = '").append(fromWbCode).append("' or unload_wb_out_name = '").append(fromWbCode);
	query.append("' or unload_gate_out_name = '").append(fromWbCode).append("' ) and is_latest = 1 and tpr_status = 0 ");
	
	Pair<java.sql.Timestamp,ArrayList<TPRecord>> tprListPair = DBSchemaManager.getList(conn, TPRecord.class, schema, query);
	if(tprListPair == null)
		return retVal;
	
	String qu = "select sisd.id,sd.ip, sd.port, sd.db, sd.user_id, sd.password, sisd.code from secl_workstation_details sisd join secl_ip_details sd on (sisd.uid = sd.mac_id) where sisd.status = 1 and sd.status = 1 and sisd.code = ? order by sd.id asc" ;
	RemoteCredential uCred = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	try{
		ps = conn.prepareStatement(qu);
		ps.setString(1, toWbCode);
		rs = ps.executeQuery();
		while(rs.next()){
			String ip = rs.getString("ip");
			if(Utils.isMyIp(ip))
				continue;
			int id = Misc.getRsetInt(rs, "id");
			String port = rs.getString("port");
			String db = rs.getString("db");
			String userId = rs.getString("user_id");
			String password = rs.getString("password");
			String wbCode = rs.getString("code");
			uCred = new RemoteCredential(id,ip,port,db,userId,password,wbCode);
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
		
		if(uCred != null){
			Connection remoteConn = null;
			boolean destroyRemote = false;
			try{
				destroyRemote = false;
				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
	
			for (int i = 0,is=tprListPair == null || tprListPair.second == null ? 0 : tprListPair.second.size(); i < is; i++) {
				TPRecord tpr = tprListPair.second.get(i);
				TPRInformation.handleMergeSECL(remoteConn, tpr, 0L,conn,Integer.MAX_VALUE);
			}
			remoteConn.commit();	
			}catch (Exception e){
				// updateStatus(conn,uCred.id,3);
				destroyRemote = true;
				System.out.println("[DOManager][copyTprToWorkStation] Error while updating remote machine: "+"[Thread:"+Thread.currentThread().getId()+"] uCred: "+uCred+" remoteConn: "+remoteConn);
				e.printStackTrace();
				//retVal = false;
			}finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}catch(Exception e){
			System.out.println("[DOManager].[copyTprToWorkStation] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
			e.printStackTrace();
			//retVal = false;
		}
	return retVal;
}

public static void upsertCurrDOStatus(Connection conn, Connection remoteConn, String wbCode, DOUpdInfo doUpdInfo) throws Exception {
	try {
		PreparedStatement remotePS = null;
		ResultSet remoteRS = null;
		System.out.println("[DOManager].[updateCurrDOStatus] Start");
		double currAllocation = 0.0;
		double liftedQty = 0.0;

		for (int art=0;art < 3; art++) {//0 - read from remote apprvd to get what is there, 1 - reular upsert, 2 apprvd upsert 
			boolean hasRecord = false;
			String query1Select = "select current_allocation, lifted_qty from current_do_status"+(art == 1 ? "" : "_apprvd")+" where do_number = ? and wb_code = ? limit 1";
			remotePS = remoteConn.prepareStatement(query1Select);
			remotePS.setString(1, doUpdInfo.doNumber);
			remotePS.setString(2, wbCode);
			remoteRS = remotePS.executeQuery();
			if (remoteRS.next()) {
				hasRecord = true;
				if (art == 0) {
					currAllocation = Misc.getRsetDouble(remoteRS, "current_allocation");
					liftedQty = Misc.getRsetDouble(remoteRS, "lifted_qty");
				}
			}
			remoteRS = Misc.closeRS(remoteRS);
			remotePS = Misc.closePS(remotePS);
			if (art == 0)
				continue;
			StringBuilder query1 = new StringBuilder();
			if (hasRecord) {
				query1.append("update current_do_status").append(art == 1 ? " set " : "_apprvd set ");
				if(!Misc.isUndef(doUpdInfo.currentAllocation))
					query1.append(" current_allocation = ").append((doUpdInfo.doIncrementCurrentAlloc) ? (doUpdInfo.currentAllocation + currAllocation) : doUpdInfo.currentAllocation);
				if(!Misc.isUndef(doUpdInfo.lastQtyLifted)){
					if(!Misc.isUndef(doUpdInfo.currentAllocation))
						query1.append(" , ");
					query1.append(" lifted_qty = ").append((doUpdInfo.doIncrementLastQtyLifted) ? (doUpdInfo.lastQtyLifted + liftedQty) : doUpdInfo.lastQtyLifted);
				}
				query1.append(" where do_number = ? and wb_code = ? ");
				remotePS = remoteConn.prepareStatement(query1.toString());
				remotePS.setString(1, doUpdInfo.doNumber);
				remotePS.setString(2, wbCode);
				remotePS.executeUpdate();
				System.out.println("[DOManager].[updateCurrDOStatus] do_no:"+doUpdInfo.doNumber+" hasRecord:"+hasRecord+"remoteConn :"+remoteConn);				
				remotePS = Misc.closePS(remotePS);
			}
			else {
				query1.append("insert into current_do_status").append(art == 1 ? " " : "_apprvd  ").append(" (do_number, wb_code, current_allocation, lifted_qty) values (?,?,?,?)");
				remotePS = remoteConn.prepareStatement(query1.toString());
				remotePS.setString(1, doUpdInfo.doNumber);
				remotePS.setString(2, wbCode);
				remotePS.setDouble(3, Misc.isUndef(doUpdInfo.currentAllocation) ? 0 : doUpdInfo.currentAllocation);
				remotePS.setDouble(4, Misc.isUndef(doUpdInfo.lastQtyLifted) ? 0 : doUpdInfo.lastQtyLifted);
				remotePS.executeUpdate();
				System.out.println("[DOManager].[insertCurrDOStatus] do_no:"+doUpdInfo.doNumber+" hasRecord:"+hasRecord+"remoteConn :"+remoteConn);				
				remotePS = Misc.closePS(remotePS);
			}
		}//for reg and apprvd
	}//try
	catch (Exception e) {
		e.printStackTrace();
		throw e;
	}
}

	public static void updateCurrDOStatus(Connection conn, RemoteCredential uCred, ArrayList<DOUpdInfo> doUpdInfoList, boolean apprvd, boolean doLockStatus){ 
		//NOT USED
		Connection remoteConn = null;
		PreparedStatement remotePS = null;
		ResultSet remoteRS = null;
		boolean destroyRemote = false;
		System.out.println("[DOManager].[updateCurrDOStatus] Start");

		if(doUpdInfoList != null && doUpdInfoList.size() > 0 && uCred != null){
			try{
				destroyRemote = false;

				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				if (!doLockStatus) {
					String query = "update current_do_status"+(apprvd ? "_apprvd" : "")+" set ";//current_allocation = ?, lifted_qty = ? where do_number = ? and wb_code = ?";
					
					int count = Misc.getUndefInt();
					for (Iterator iterator = doUpdInfoList.iterator(); iterator.hasNext();) {
						DOUpdInfo doUpdInfo = (DOUpdInfo) iterator.next();
						if(uCred.wbCode != null && !"".equals(uCred.wbCode) && doUpdInfo != null && doUpdInfo.doNumber != null && !"".equals(doUpdInfo.doNumber) && (!Misc.isUndef(doUpdInfo.currentAllocation) || !Misc.isUndef(doUpdInfo.lastQtyLifted))){
													
							boolean hasRecord = false;
							double currAllocation = 0.0;
							double liftedQty = 0.0;
							String query1Select = "select current_allocation, lifted_qty from current_do_status"+(apprvd ? "_apprvd" : "")+" where do_number = ? and wb_code = ? limit 1";
							remotePS = remoteConn.prepareStatement(query1Select);
							remotePS.setString(1, doUpdInfo.doNumber);
							remotePS.setString(2, uCred.wbCode);
							remoteRS = remotePS.executeQuery();
							if (remoteRS.next()) {
								hasRecord = true;
								currAllocation = Misc.getRsetDouble(remoteRS, "current_allocation");
								liftedQty = Misc.getRsetDouble(remoteRS, "lifted_qty");
							}
							Misc.closeRS(remoteRS);
							Misc.closePS(remotePS);
							
							StringBuilder query1 = new StringBuilder();
							if(!Misc.isUndef(doUpdInfo.currentAllocation))
								query1.append(" current_allocation = ").append((doUpdInfo.doIncrementCurrentAlloc) ? (doUpdInfo.currentAllocation + currAllocation) : doUpdInfo.currentAllocation);
							if(!Misc.isUndef(doUpdInfo.lastQtyLifted)){
								if(!Misc.isUndef(doUpdInfo.currentAllocation))
									query1.append(" , ");
								query1.append(" lifted_qty = ").append((doUpdInfo.doIncrementLastQtyLifted) ? (doUpdInfo.lastQtyLifted + liftedQty) : doUpdInfo.lastQtyLifted);
							}
							query1.append(" where do_number = ? and wb_code = ? ");
							
							if(hasRecord){
								try{
									remotePS = remoteConn.prepareStatement(query+query1.toString());
									remotePS.setString(1, doUpdInfo.doNumber);
									remotePS.setString(2, uCred.wbCode);
									count = remotePS.executeUpdate();
									System.out.println("[DOManager].[updateCurrDOStatus] do_no:"+doUpdInfo.doNumber+" hasRecord:"+hasRecord+"remoteConn :"+remoteConn);
									remoteConn.commit();
									Misc.closePS(remotePS);
	
									doUpdInfo.resultStatus = SUCC;
									System.out.println("[DOManager].[updateCurrDOStatus] SUCC do_no:"+doUpdInfo.doNumber);
								}catch(Exception e){
									e.printStackTrace();
									doUpdInfo.resultStatus = FAIL;
									doUpdInfo.resultMessage = "Unable to Update DO details in remote machine";
									System.out.println("[DOManager].[updateCurrDOStatus] FAIL do_no:"+ doUpdInfo != null ? doUpdInfo.doNumber : "Null"+ " uCred:"+ uCred);
								}
							}else{
								try{
									insertRemoteCurrDoStatus(conn,remoteConn, uCred, doUpdInfo);
									
									remoteConn.commit(); 
									
									doUpdInfo.resultStatus = SUCC;
									System.out.println("[DOManager].[updateCurrDOStatus] SUCC do_no:"+doUpdInfo.doNumber);
								}catch(Exception e){
									e.printStackTrace();
									doUpdInfo.resultStatus = FAIL;
									doUpdInfo.resultMessage = "Unable to insert DO details in remote machine";
									System.out.println("[DOManager].[updateCurrDOStatus] FAIL do_no:"+ doUpdInfo != null ? doUpdInfo.doNumber : "Null"+ " uCred:"+ uCred);
								}
								
							}
						}else{
							doUpdInfo.resultStatus = FAIL;
							doUpdInfo.resultMessage = "DO Number is null";
							System.out.println("[DOManager].[updateCurrDOStatus] FAIL do_no:"+ doUpdInfo != null ? doUpdInfo.doNumber : "Null");
						}
					}					
					//updateRemoteInBatch(conn, ps, rs, remoteConn, remotePS, remoteRS, pair);
					//updateStatus(conn,uCred.id,2);
				}
				else {
					String query = "update mines_do_details"+(apprvd ? "_apprvd" : "")+" set lock_status=? where do_number=? ";//current_allocation = ?, lifted_qty = ? where do_number = ? and wb_code = ?";
					remotePS = remoteConn.prepareStatement(query);
					
					int count = Misc.getUndefInt();
					for (Iterator iterator = doUpdInfoList.iterator(); iterator.hasNext();) {
						DOUpdInfo doUpdInfo = (DOUpdInfo) iterator.next();
						remotePS.setInt(1, doUpdInfo.getLockStatus());
						remotePS.setString(2, doUpdInfo.getDoNumber());
						remotePS.addBatch();
					}
					remotePS.executeBatch();
				}
				Misc.closePS(remotePS);
				remoteConn.commit();
			}catch (Exception e){
				// updateStatus(conn,uCred.id,3);
				if(doUpdInfoList != null && doUpdInfoList.size() > 0){
					for (Iterator iterator = doUpdInfoList.iterator(); iterator.hasNext();) {
						DOUpdInfo doUpdInfo = (DOUpdInfo) iterator.next();
						doUpdInfo.resultStatus = FAIL;
						doUpdInfo.resultMessage = "Unable to Update DO details in remote machine MSG: " + e.getMessage();
					}
				}
				
				destroyRemote = true;
				System.out.println("[DOManager].[updateCurrDOStatus] Error while updating remote machine: "+"[Thread:"+Thread.currentThread().getId()+"] uCred: "+uCred+" remoteConn: "+remoteConn);
				e.printStackTrace();
			}finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else{
			if(doUpdInfoList != null && doUpdInfoList.size() > 0){
				for (Iterator iterator = doUpdInfoList.iterator(); iterator.hasNext();) {
					DOUpdInfo doUpdInfo = (DOUpdInfo) iterator.next();
					doUpdInfo.resultStatus = FAIL;
					doUpdInfo.resultMessage = "Unable to Update DO details in remote machine with NULL Credential";
				}
			}
			System.out.println("[DOManager].[updateCurrDOStatus] doUpdInfoList size is 0 : "+"[Thread:"+Thread.currentThread().getId()+"] uCred: "+ uCred);
		}
		System.out.println("[DOManager].[updateCurrDOStatus] End");

	}
	
	public static void updateStatus(Connection conn,int id,int value){
		String queryUpdateStatus = "update secl_ip_sync_details set status=? where id=?";
		try {
			PreparedStatement ps = conn.prepareStatement(queryUpdateStatus);
			ps.setInt(1, value);
			ps.setInt(2, id);
			ps.executeQuery();
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static HashMap<String, String> getColsInDB(Connection forThisConn, String tableName) throws Exception {
		HashMap<String, String> retval =new HashMap<String, String>();
		if (forThisConn == null || tableName == null)
			return retval;
		DatabaseMetaData dbmt =  forThisConn.getMetaData();
		ResultSet rs = dbmt.getColumns(null, null, tableName, null);
		boolean isFirst = true;
		ArrayList<Object> parameters = new ArrayList<Object>();
		int paramCount = 0;
		while (rs.next()) {
			String col = rs.getString("COLUMN_NAME");
			retval.put(col,col);
		}
		rs = Misc.closeRS(rs);
		return retval;
	}
	
	public static void updateDOInfoInRemote(Connection conn, Connection remoteConn, String doNumber) throws Exception {
		StringBuilder getValLocal = new StringBuilder();
		StringBuilder insertRemoteReg = new StringBuilder();
		StringBuilder insertRemoteApprvd = new StringBuilder();
		StringBuilder updateRemoteReg = new StringBuilder();
		StringBuilder updateRemoteApprvd = new StringBuilder();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int remoteDOId = Misc.getUndefInt();
		getValLocal.append("select ");
		insertRemoteReg.append("insert into mines_do_details (");
		insertRemoteApprvd.append("insert into mines_do_details_apprvd (");
		updateRemoteReg.append("update mines_do_details set ");
		updateRemoteApprvd.append("update mines_do_details_apprvd set ");
		try {
			ps = remoteConn.prepareStatement("select id from mines_do_details where do_number = ?");
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			if (rs.next())
				remoteDOId = rs.getInt(1);
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			HashMap<String, String> regRemoteCols = getColsInDB(remoteConn, "mines_do_details");
			HashMap<String, String> apprvdRemoteCols = getColsInDB(remoteConn, "mines_do_details_apprvd");
			
			DatabaseMetaData dbmt =  conn.getMetaData();
			rs = dbmt.getColumns(null, null, "mines_do_details", null);
			boolean isFirst = true;
			ArrayList<Object> parameters = new ArrayList<Object>();
			int paramCount = 0;
			while (rs.next()) {
				String col = rs.getString("COLUMN_NAME");
				if (col.equalsIgnoreCase("id") || !regRemoteCols.containsKey(col) || !apprvdRemoteCols.containsKey(col)) 
					continue;
				if (!Misc.isUndef(remoteDOId) && col.equalsIgnoreCase("do_number"))
					continue;
				if (!isFirst) {
					insertRemoteReg.append(",");
					insertRemoteApprvd.append(",");
					updateRemoteReg.append(",");
					updateRemoteApprvd.append(",");
					getValLocal.append(",");
				}
				else {
					isFirst=false;
				}
				insertRemoteReg.append(col);
				insertRemoteApprvd.append(col);
				updateRemoteReg.append(col).append(" = ?");
				updateRemoteApprvd.append(col).append(" = ?");
				getValLocal.append(col);
				paramCount++;
			}
			rs = Misc.closeRS(rs);
			getValLocal.append(" from mines_do_details where do_number = ? ");
			ps = conn.prepareStatement(getValLocal.toString());
			ps.setString(1, doNumber);
			rs = ps.executeQuery();
			if (rs.next()) {
				for (int i1=0;i1<paramCount;i1++)
					parameters.add(rs.getObject(i1+1));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (Misc.isUndef(remoteDOId)) {
				//insert reg, then insert apprvd using the id
				insertRemoteReg.append(") values (");
				insertRemoteApprvd.append(",id) values (");
				
				for (int i1=0;i1<paramCount;i1++) {
					if (i1 != 0) {
						insertRemoteReg.append(",");
						insertRemoteApprvd.append(",");
					}
					insertRemoteReg.append("?");
					insertRemoteApprvd.append("?");
				}
				insertRemoteReg.append(")");
				insertRemoteApprvd.append(",?)");
				ps = remoteConn.prepareStatement(insertRemoteReg.toString());
				for (int i1=0;i1<parameters.size();i1++) {
					ps.setObject(i1+1, parameters.get(i1));
				}
				ps.execute();
				rs = ps.getGeneratedKeys();
				if (rs.next())
					remoteDOId = rs.getInt(1);
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				ps = remoteConn.prepareStatement(insertRemoteApprvd.toString());
				for (int i1=0;i1<parameters.size();i1++) {
					ps.setObject(i1+1, parameters.get(i1));
				}
				ps.setInt(parameters.size()+1, remoteDOId);
				ps.execute();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);				
			}
			else {//do update
				updateRemoteReg.append(" where id = ? ");
				updateRemoteApprvd.append(" where id = ? ");
				ps = remoteConn.prepareStatement(updateRemoteReg.toString());
				for (int i1=0;i1<parameters.size();i1++) {
					ps.setObject(i1+1, parameters.get(i1));
				}
				ps.setInt(parameters.size()+1, remoteDOId);
				ps.execute();
				ps = Misc.closePS(ps);
				ps = remoteConn.prepareStatement(updateRemoteApprvd.toString());
				for (int i1=0;i1<parameters.size();i1++) {
					ps.setObject(i1+1, parameters.get(i1));
				}
				ps.setInt(parameters.size()+1, remoteDOId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	
	public static void updateRemoteInBatchOld(Connection conn, Connection remoteConn, RemoteCredential uCred, DOUpdInfo doUpdInfo) throws Exception{
		if(doUpdInfo == null || uCred == null)
			return;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement remotePS = null;
		PreparedStatement remoteApprvdPS = null;
		String query = "select do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person" +
		",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge" +
		",dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on" +
		",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1" +
		",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code " +
		",delivery_point_code, delivery_point, material, royalty_charge, transport_mode,validity_date, prefered_wb_1_qty " +
		", prefered_wb_2_qty, prefered_wb_3_qty, prefered_wb_4_qty, min_retry_hours, product_code, type_of_release, release_priority " +
		", temp_remote_id, record_src, lock_status, allocation_approval_status, lock_changed_at, do_type " +
		"from mines_do_details where do_number = ? " ;
		String remoteQ = "insert ignore into mines_do_details( do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person" +
		",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge" +
		",dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on" +
		",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1" +
		",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code " +
		",delivery_point_code, delivery_point, material, royalty_charge, transport_mode,validity_date, prefered_wb_1_qty " +
		", prefered_wb_2_qty, prefered_wb_3_qty, prefered_wb_4_qty, min_retry_hours, product_code, type_of_release, release_priority " +
		", temp_remote_id, record_src, lock_status, allocation_approval_status, lock_changed_at, do_type " +
		") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		
		/*String queryApp = "select id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person" +
		",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge" +
		",dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on" +
		",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1" +
		",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code from mines_do_details_apprvd where do_number = ? " ;*/
		String remoteQApp = "insert ignore into mines_do_details_apprvd (id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person" +
		",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge" +
		",dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on" +
		",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1" +
		",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code " +
		",delivery_point_code, delivery_point, material, royalty_charge, transport_mode,validity_date, prefered_wb_1_qty " +
		", prefered_wb_2_qty, prefered_wb_3_qty, prefered_wb_4_qty, min_retry_hours, product_code, type_of_release, release_priority " +
		", temp_remote_id, record_src, lock_status, allocation_approval_status, lock_changed_at, do_type " +
		") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
		
		int count = 0;
		ps = conn.prepareStatement(query);
		ps.setString(1, doUpdInfo.doNumber);
		rs = ps.executeQuery();
		remotePS = remoteConn.prepareStatement(remoteQ);
		
		String name = null;
		long doDate = Misc.getUndefInt() ;
		long doReleaseDate = Misc.getUndefInt();
		String doReleaseNo = null;
		int typeOfConsumer = Misc.getUndefInt();
		int customer = Misc.getUndefInt();
		String customerRef = null;
		String customerContactPerson = null;
		int grade = Misc.getUndefInt();
		String coalSize = null;
		int sourceMines = Misc.getUndefInt();
		int washery = Misc.getUndefInt();
		double qtyAlloc =Misc.getUndefDouble();
		double qtyAlreadyLifted =Misc.getUndefDouble();
		double quota =Misc.getUndefDouble();
		double rate =Misc.getUndefDouble();
		double transportCharge =Misc.getUndefDouble();
		double sizingCharge =Misc.getUndefDouble();
		double siloCharge =Misc.getUndefDouble();
		double dumpCharge =Misc.getUndefDouble();
		double stowCharge =Misc.getUndefDouble();
		double terminalCharge =Misc.getUndefDouble();
		double forestCess =Misc.getUndefDouble();
		double stowEd =Misc.getUndefDouble();
		double avap =Misc.getUndefDouble();
		int allowNoTare = Misc.getUndefInt(); 
		double maTareGap = Misc.getUndefDouble(); 
		int destination = Misc.getUndefInt(); 
		int status = Misc.getUndefInt(); 
		long createdOn = Misc.getUndefInt();
		long updatedOn = Misc.getUndefInt();
		int updatedBy = Misc.getUndefInt();
		int portNodeId = Misc.getUndefInt();
		int createdBy = Misc.getUndefInt(); 
		String gradeCode = null; 
		String sourceCode = null; 
		String washeryCode = null; 
		String destinationCode = null; 
		String preferedWb_1 = null; 
		String preferedWb_2 = null; 
		String preferedWb_3 = null; 
		String preferedWb_4 = null; 
		String customerCode = null;
		String deliveryPointCode = null;
		String deliveryPoint = null;
		String material = null;
		double royaltyCharge =Misc.getUndefDouble();
		int transportMode = Misc.getUndefInt(); 
		long validityDate = Misc.getUndefInt();
		double preferedWB_1_Qty =Misc.getUndefDouble();
		double preferedWB_2_Qty =Misc.getUndefDouble();
		double preferedWB_3_Qty =Misc.getUndefDouble();
		double preferedWB_4_Qty =Misc.getUndefDouble();
		double minRetryHours =Misc.getUndefDouble();
		String productCode = null;
		int typeOfRelease = Misc.getUndefInt(); 
		int releasePriority = Misc.getUndefInt(); 
		int tempRemoteId = Misc.getUndefInt(); 
		int recordSrc = Misc.getUndefInt(); 
		int lockStatus  = Misc.getUndefInt(); 
		int allocationApprovalStatus = Misc.getUndefInt(); 
		long lockChangedAt = Misc.getUndefInt();
		int doType = Misc.getUndefInt();
		while(rs.next()){

//			int id = Misc.getRsetInt(rs,"id");
		
			name = rs.getString("do_number");
			doDate = Misc.sqlToLong(rs.getTimestamp("do_date"));
			doReleaseDate = Misc.sqlToLong(rs.getTimestamp("do_release_date"));
			doReleaseNo = rs.getString("do_release_no");
			typeOfConsumer = Misc.getRsetInt(rs,"type_of_consumer");
			customer = Misc.getRsetInt(rs,"customer");
			customerRef = rs.getString("customer_ref");
			customerContactPerson = rs.getString("customer_contact_person");
			grade = Misc.getRsetInt(rs,"grade");
			coalSize = rs.getString("coal_size");
			sourceMines = Misc.getRsetInt(rs,"source_mines");
			washery = Misc.getRsetInt(rs,"washery");
			qtyAlloc =Misc.getRsetDouble(rs, "qty_alloc");
			qtyAlreadyLifted =Misc.getRsetDouble(rs, "qty_already_lifted");
			quota =Misc.getRsetDouble(rs, "quota");
			rate =Misc.getRsetDouble(rs, "rate");
			transportCharge =Misc.getRsetDouble(rs, "transport_charge");
			sizingCharge =Misc.getRsetDouble(rs, "sizing_charge");
			siloCharge =Misc.getRsetDouble(rs, "silo_charge");
			dumpCharge =Misc.getRsetDouble(rs, "dump_charge");
			stowCharge =Misc.getRsetDouble(rs, "stc_charge");
			terminalCharge =Misc.getRsetDouble(rs, "terminal_charge");
			forestCess =Misc.getRsetDouble(rs, "forest_cess");
			stowEd =Misc.getRsetDouble(rs, "stow_ed");
			avap =Misc.getRsetDouble(rs, "avap");
			allowNoTare = Misc.getRsetInt(rs,"allow_no_tare"); 
			maTareGap = Misc.getRsetInt(rs,"max_tare_gap"); 
			destination = Misc.getRsetInt(rs,"destination"); 
			status = Misc.getRsetInt(rs,"status"); 
			createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
			updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
			updatedBy = Misc.getRsetInt(rs,"updated_by"); 
			portNodeId = Misc.getRsetInt(rs,"port_node_id");  
			createdBy = Misc.getRsetInt(rs,"created_by");
			gradeCode = rs.getString("grade_code"); 
			sourceCode = rs.getString("source_code"); 
			washeryCode = rs.getString("washery_code"); 
			destinationCode = rs.getString("destination_code"); 
			preferedWb_1 = rs.getString("prefered_wb_1"); 
			preferedWb_2 = rs.getString("prefered_wb_2"); 
			preferedWb_3 = rs.getString("prefered_wb_3"); 
			preferedWb_4 = rs.getString("prefered_wb_4"); 
			customerCode = rs.getString("customer_code");
			
			deliveryPointCode = rs.getString("delivery_point_code");
			deliveryPoint = rs.getString("delivery_point");
			material = rs.getString("material");
			royaltyCharge =Misc.getRsetDouble(rs, "royalty_charge");
			transportMode = Misc.getRsetInt(rs,"transport_mode");
			validityDate = Misc.sqlToLong(rs.getTimestamp("validity_date"));
			preferedWB_1_Qty =Misc.getRsetDouble(rs, "prefered_wb_1_qty");
			preferedWB_2_Qty =Misc.getRsetDouble(rs, "prefered_wb_2_qty");
			preferedWB_3_Qty =Misc.getRsetDouble(rs, "prefered_wb_3_qty");
			preferedWB_4_Qty =Misc.getRsetDouble(rs, "prefered_wb_4_qty");
			minRetryHours =Misc.getRsetDouble(rs, "min_retry_hours");
			productCode = rs.getString("product_code");
			typeOfRelease = Misc.getRsetInt(rs,"type_of_release");
			releasePriority = Misc.getRsetInt(rs,"release_priority");
			tempRemoteId = Misc.getRsetInt(rs,"temp_remote_id");
			recordSrc = Misc.getRsetInt(rs,"record_src");
			lockStatus = Misc.getRsetInt(rs,"lock_status");
			allocationApprovalStatus = Misc.getRsetInt(rs,"allocation_approval_status");
			lockChangedAt = Misc.sqlToLong(rs.getTimestamp("lock_changed_at"));
			doType = Misc.getRsetInt(rs, "do_type");
			count = 0;
			
//			Misc.setParamInt(remotePS, id,++count);
			// remotePS.setInt(++count, id);
			remotePS.setString(++count, name);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(doDate));
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(doReleaseDate));
			remotePS.setString(++count, doReleaseNo);
			Misc.setParamInt(remotePS, typeOfConsumer,++count);
			//remotePS.setInt(++count, typeOfConsumer);
			Misc.setParamInt(remotePS, customer,++count);
//			remotePS.setInt(++count, customer);
			remotePS.setString(++count, customerRef);
			remotePS.setString(++count, customerContactPerson);
			Misc.setParamInt(remotePS, grade,++count);
//			remotePS.setInt(++count, grade);
			remotePS.setString(++count, coalSize);
			Misc.setParamInt(remotePS, sourceMines,++count);
//			remotePS.setInt(++count, sourceMines);
			Misc.setParamInt(remotePS, washery,++count);
//			remotePS.setInt(++count, washery);
			Misc.setParamDouble(remotePS, qtyAlloc,++count);
//			remotePS.setDouble(++count, qtyAlloc);
			Misc.setParamDouble(remotePS, qtyAlreadyLifted,++count);
//			remotePS.setDouble(++count, qtyAlreadyLifted);
			Misc.setParamDouble(remotePS, quota,++count);
//			remotePS.setDouble(++count, quota);
			Misc.setParamDouble(remotePS, rate,++count);
//			remotePS.setDouble(++count, rate);
			Misc.setParamDouble(remotePS, transportCharge,++count);
//			remotePS.setDouble(++count, transportCharge);
			Misc.setParamDouble(remotePS, sizingCharge,++count);
//			remotePS.setDouble(++count, sizingCharge);
			Misc.setParamDouble(remotePS, siloCharge,++count);
//			remotePS.setDouble(++count, siloCharge);
			Misc.setParamDouble(remotePS, dumpCharge,++count);
//			remotePS.setDouble(++count, dumpCharge);
			Misc.setParamDouble(remotePS, stowCharge,++count);
//			remotePS.setDouble(++count, stowCharge);
			Misc.setParamDouble(remotePS, terminalCharge,++count);
//			remotePS.setDouble(++count, terminalCharge);
			Misc.setParamDouble(remotePS, forestCess,++count);
//			remotePS.setDouble(++count, forestCess);
			Misc.setParamDouble(remotePS, stowEd,++count);
//			remotePS.setDouble(++count, stowEd);
			Misc.setParamDouble(remotePS, avap,++count);
//			remotePS.setDouble(++count, avap);
			Misc.setParamInt(remotePS, allowNoTare,++count);
//			remotePS.setInt(++count, allowNoTare);
			Misc.setParamDouble(remotePS, maTareGap,++count);
//			remotePS.setInt(++count, maTareGap);
			Misc.setParamInt(remotePS, destination,++count);
//			remotePS.setInt(++count, destination);
			Misc.setParamInt(remotePS, status,++count);
//			remotePS.setInt(++count, status);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
			Misc.setParamInt(remotePS, updatedBy,++count);
			Misc.setParamInt(remotePS, portNodeId,++count);
//			remotePS.setInt(++count, portNodeId);
			Misc.setParamInt(remotePS, createdBy,++count);
			remotePS.setString(++count, gradeCode);
			remotePS.setString(++count, sourceCode);
			remotePS.setString(++count, washeryCode);
			remotePS.setString(++count, destinationCode);
			remotePS.setString(++count, preferedWb_1);
			remotePS.setString(++count, preferedWb_2);
			remotePS.setString(++count, preferedWb_3);
			remotePS.setString(++count, preferedWb_4);
			remotePS.setString(++count, customerCode);	
			
			remotePS.setString(++count, deliveryPointCode);	
			remotePS.setString(++count, deliveryPoint);	
			remotePS.setString(++count, material);
			Misc.setParamDouble(remotePS, royaltyCharge,++count);
			Misc.setParamInt(remotePS, transportMode,++count);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(validityDate));
			Misc.setParamDouble(remotePS, preferedWB_1_Qty,++count);
			Misc.setParamDouble(remotePS, preferedWB_2_Qty,++count);
			Misc.setParamDouble(remotePS, preferedWB_3_Qty,++count);
			Misc.setParamDouble(remotePS, preferedWB_4_Qty,++count);
			Misc.setParamDouble(remotePS, minRetryHours,++count);
			remotePS.setString(++count, productCode);
			Misc.setParamInt(remotePS, typeOfRelease,++count);
			Misc.setParamInt(remotePS, releasePriority,++count);
			Misc.setParamInt(remotePS, tempRemoteId,++count);
			Misc.setParamInt(remotePS, recordSrc,++count);
			Misc.setParamInt(remotePS, lockStatus,++count);
			Misc.setParamInt(remotePS, allocationApprovalStatus,++count);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(lockChangedAt));
			Misc.setParamInt(remotePS, doType, ++count);	
			//for apprvd db
			int remoteDoId = Misc.getUndefInt();
			if(count > 0){
				remotePS.execute();
				ResultSet remoteRS = remotePS.getGeneratedKeys();
				if(remoteRS.next())
					remoteDoId = Misc.getRsetInt(remoteRS, 1);
			}
			
			remoteApprvdPS = remoteConn.prepareStatement(remoteQApp);
			count = 0;
			Misc.setParamInt(remoteApprvdPS, remoteDoId,++count);
			// remoteApprvdPS.setInt(++count, id);
			remoteApprvdPS.setString(++count, name);
			remoteApprvdPS.setTimestamp(++count, Misc.utilToSqlDate(doDate));
			remoteApprvdPS.setTimestamp(++count, Misc.utilToSqlDate(doReleaseDate));
			remoteApprvdPS.setString(++count, doReleaseNo);
			Misc.setParamInt(remoteApprvdPS, typeOfConsumer,++count);
			//remoteApprvdPS.setInt(++count, typeOfConsumer);
			Misc.setParamInt(remoteApprvdPS, customer,++count);
//			remoteApprvdPS.setInt(++count, customer);
			remoteApprvdPS.setString(++count, customerRef);
			remoteApprvdPS.setString(++count, customerContactPerson);
			Misc.setParamInt(remoteApprvdPS, grade,++count);
//			remoteApprvdPS.setInt(++count, grade);
			remoteApprvdPS.setString(++count, coalSize);
			Misc.setParamInt(remoteApprvdPS, sourceMines,++count);
//			remoteApprvdPS.setInt(++count, sourceMines);
			Misc.setParamInt(remoteApprvdPS, washery,++count);
//			remoteApprvdPS.setInt(++count, washery);
			Misc.setParamDouble(remoteApprvdPS, qtyAlloc,++count);
//			remoteApprvdPS.setDouble(++count, qtyAlloc);
			Misc.setParamDouble(remoteApprvdPS, qtyAlreadyLifted,++count);
//			remoteApprvdPS.setDouble(++count, qtyAlreadyLifted);
			Misc.setParamDouble(remoteApprvdPS, quota,++count);
//			remoteApprvdPS.setDouble(++count, quota);
			Misc.setParamDouble(remoteApprvdPS, rate,++count);
//			remoteApprvdPS.setDouble(++count, rate);
			Misc.setParamDouble(remoteApprvdPS, transportCharge,++count);
//			remoteApprvdPS.setDouble(++count, transportCharge);
			Misc.setParamDouble(remoteApprvdPS, sizingCharge,++count);
//			remoteApprvdPS.setDouble(++count, sizingCharge);
			Misc.setParamDouble(remoteApprvdPS, siloCharge,++count);
//			remoteApprvdPS.setDouble(++count, siloCharge);
			Misc.setParamDouble(remoteApprvdPS, dumpCharge,++count);
//			remoteApprvdPS.setDouble(++count, dumpCharge);
			Misc.setParamDouble(remoteApprvdPS, stowCharge,++count);
//			remoteApprvdPS.setDouble(++count, stowCharge);
			Misc.setParamDouble(remoteApprvdPS, terminalCharge,++count);
//			remoteApprvdPS.setDouble(++count, terminalCharge);
			Misc.setParamDouble(remoteApprvdPS, forestCess,++count);
//			remoteApprvdPS.setDouble(++count, forestCess);
			Misc.setParamDouble(remoteApprvdPS, stowEd,++count);
//			remoteApprvdPS.setDouble(++count, stowEd);
			Misc.setParamDouble(remoteApprvdPS, avap,++count);
//			remoteApprvdPS.setDouble(++count, avap);
			Misc.setParamInt(remoteApprvdPS, allowNoTare,++count);
//			remoteApprvdPS.setInt(++count, allowNoTare);
			Misc.setParamDouble(remoteApprvdPS, maTareGap,++count);
//			remoteApprvdPS.setInt(++count, maTareGap);
			Misc.setParamInt(remoteApprvdPS, destination,++count);
//			remoteApprvdPS.setInt(++count, destination);
			Misc.setParamInt(remoteApprvdPS, status,++count);
//			remoteApprvdPS.setInt(++count, status);
			remoteApprvdPS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
			remoteApprvdPS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
			Misc.setParamInt(remoteApprvdPS,updatedBy,++count);
			Misc.setParamInt(remoteApprvdPS, portNodeId,++count);
//			remoteApprvdPS.setInt(++count, portNodeId);
			Misc.setParamInt(remoteApprvdPS,createdBy,++count);
			remoteApprvdPS.setString(++count, gradeCode);
			remoteApprvdPS.setString(++count, sourceCode);
			remoteApprvdPS.setString(++count, washeryCode);
			remoteApprvdPS.setString(++count, destinationCode);
			remoteApprvdPS.setString(++count, preferedWb_1);
			remoteApprvdPS.setString(++count, preferedWb_2);
			remoteApprvdPS.setString(++count, preferedWb_3);
			remoteApprvdPS.setString(++count, preferedWb_4);
			remoteApprvdPS.setString(++count, customerCode);
			
			remoteApprvdPS.setString(++count, deliveryPointCode);	
			remoteApprvdPS.setString(++count, deliveryPoint);	
			remoteApprvdPS.setString(++count, material);
			Misc.setParamDouble(remoteApprvdPS, royaltyCharge,++count);
			Misc.setParamInt(remoteApprvdPS, transportMode,++count);
			remoteApprvdPS.setTimestamp(++count, Misc.utilToSqlDate(validityDate));
			Misc.setParamDouble(remoteApprvdPS, preferedWB_1_Qty,++count);
			Misc.setParamDouble(remoteApprvdPS, preferedWB_2_Qty,++count);
			Misc.setParamDouble(remoteApprvdPS, preferedWB_3_Qty,++count);
			Misc.setParamDouble(remoteApprvdPS, preferedWB_4_Qty,++count);
			Misc.setParamDouble(remoteApprvdPS, minRetryHours,++count);
			remoteApprvdPS.setString(++count, productCode);
			Misc.setParamInt(remoteApprvdPS, typeOfRelease,++count);
			Misc.setParamInt(remoteApprvdPS, releasePriority,++count);
			Misc.setParamInt(remoteApprvdPS, tempRemoteId,++count);
			Misc.setParamInt(remoteApprvdPS, recordSrc,++count);
			Misc.setParamInt(remoteApprvdPS, lockStatus,++count);
			Misc.setParamInt(remoteApprvdPS, allocationApprovalStatus,++count);
			remoteApprvdPS.setTimestamp(++count, Misc.utilToSqlDate(lockChangedAt));
			Misc.setParamInt(remotePS, doType, ++count);
			System.out.println(remoteApprvdPS);
			remoteApprvdPS.execute();
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
		Misc.closePS(remotePS);
		Misc.closePS(remoteApprvdPS);
		remoteConn.commit();
		// insertCurrDoStatus();  TODO
		updateRemoteCurrDoStatusOld(conn, remoteConn, uCred, doUpdInfo, ps, rs, remotePS);
		/*ps = conn.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();*/
	}
	public static void insertRemoteCurrDoStatus(Connection conn, Connection remoteConn, RemoteCredential uCred, DOUpdInfo doUpdInfo) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null; 
		PreparedStatement remotePS = null;
		
		String query = "select  do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
				", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id " +
				"from current_do_status where do_number = ? " ;
		String remoteQ = "insert ignore into current_do_status( do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
				", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id) " +
				"values (?,?,?,?,?,?,?,?,?,?,?)" ;
		String queryApp = "select do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
		", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id " +
		"from current_do_status_apprvd where do_number = ? " ;
		String remoteQApp = "insert ignore into current_do_status_apprvd( do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
		", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id) " +
		"values (?,?,?,?,?,?,?,?,?,?,?)" ;
		try {
			int count = 0;
			ps = conn.prepareStatement(query);
			ps.setString(1, doUpdInfo.doNumber);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			
			while(rs.next()){
	
	//			int doId = Misc.getRsetInt(rs,"do_id");
				String doNumber = rs.getString("do_number");
				double liftedQty =Misc.getRsetDouble(rs, "lifted_qty");
				long lastLiftedOn = Misc.sqlToLong(rs.getTimestamp("last_lifted_on"));
				double lastLiftedQty =Misc.getRsetDouble(rs, "last_lifted_qty");
				int lastLiftedVehicleId = Misc.getRsetInt(rs,"last_lifted_vehicle_id");
				int lastLiftedTprId = Misc.getRsetInt(rs,"last_lifted_tpr_id");
				int tripsCountDaily = Misc.getRsetInt(rs,"trips_count_daily");
				int tripsCount = Misc.getRsetInt(rs,"trips_count");
				String wbCode = rs.getString("wb_code");
				double currentAllocationQty =Misc.getRsetDouble(rs, "current_allocation");
				int wbId = Misc.getRsetInt(rs,"wb_id");
				
				count = 0;
				
	//			Misc.setParamInt(remotePS, doId,++count);
	//			remotePS.setInt(++count, doId);
				remotePS.setString(++count, doNumber);
				Misc.setParamDouble(remotePS, liftedQty,++count);
	//			remotePS.setDouble(++count, liftedQty);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(lastLiftedOn));
				Misc.setParamDouble(remotePS, lastLiftedQty,++count);
	//			remotePS.setDouble(++count, lastLiftedQty);
				Misc.setParamInt(remotePS, lastLiftedVehicleId,++count);
	//			remotePS.setInt(++count, lastLiftedVehicleId);
				Misc.setParamInt(remotePS, lastLiftedTprId,++count);
	//			remotePS.setInt(++count, lastLiftedTprId);
				Misc.setParamInt(remotePS, tripsCountDaily,++count);
	//			remotePS.setInt(++count, tripsCountDaily);
				Misc.setParamInt(remotePS, tripsCount,++count);
	//			remotePS.setInt(++count, tripsCount);
				remotePS.setString(++count, wbCode);
				Misc.setParamDouble(remotePS, currentAllocationQty,++count);
	//			remotePS.setDouble(++count, currentAllocationQty);
				Misc.setParamInt(remotePS, wbId,++count);
	//			remotePS.setInt(++count, wbId);
				
				remotePS.addBatch();
			}
	
			Misc.closeRS(rs);
			Misc.closePS(ps);
			
			if(count > 0){
				int[] rowCount = remotePS.executeBatch();
				System.out.println("DOUpdInfo.updateRemoteCurrDoStatus() rows inserted : "+rowCount.length);
			}
			Misc.closePS(remotePS);
			remoteConn.commit();
			
			count = 0;
			ps = conn.prepareStatement(queryApp);
			ps.setString(1, doUpdInfo.doNumber);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQApp);
			
			while(rs.next()){
	
	//			int doId = Misc.getRsetInt(rs,"do_id");
				String doNumber = rs.getString("do_number");
				double liftedQty =Misc.getRsetDouble(rs, "lifted_qty");
				long lastLiftedOn = Misc.sqlToLong(rs.getTimestamp("last_lifted_on"));
				double lastLiftedQty =Misc.getRsetDouble(rs, "last_lifted_qty");
				int lastLiftedVehicleId = Misc.getRsetInt(rs,"last_lifted_vehicle_id");
				int lastLiftedTprId = Misc.getRsetInt(rs,"last_lifted_tpr_id");
				int tripsCountDaily = Misc.getRsetInt(rs,"trips_count_daily");
				int tripsCount = Misc.getRsetInt(rs,"trips_count");
				String wbCode = rs.getString("wb_code");
				double currentAllocationQty =Misc.getRsetDouble(rs, "current_allocation");
				int wbId = Misc.getRsetInt(rs,"wb_id");
				
				count = 0;
				
	//			Misc.setParamInt(remotePS, doId,++count);
	//			remotePS.setInt(++count, doId);
				remotePS.setString(++count, doNumber);
				Misc.setParamDouble(remotePS, liftedQty,++count);
	//			remotePS.setDouble(++count, liftedQty);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(lastLiftedOn));
				Misc.setParamDouble(remotePS, lastLiftedQty,++count);
	//			remotePS.setDouble(++count, lastLiftedQty);
				Misc.setParamInt(remotePS, lastLiftedVehicleId,++count);
	//			remotePS.setInt(++count, lastLiftedVehicleId);
				Misc.setParamInt(remotePS, lastLiftedTprId,++count);
	//			remotePS.setInt(++count, lastLiftedTprId);
				Misc.setParamInt(remotePS, tripsCountDaily,++count);
	//			remotePS.setInt(++count, tripsCountDaily);
				Misc.setParamInt(remotePS, tripsCount,++count);
	//			remotePS.setInt(++count, tripsCount);
				remotePS.setString(++count, wbCode);
				Misc.setParamDouble(remotePS, currentAllocationQty,++count);
	//			remotePS.setDouble(++count, currentAllocationQty);
				Misc.setParamInt(remotePS, wbId,++count);
	//			remotePS.setInt(++count, wbId);
				
				remotePS.addBatch();
			}
	
			Misc.closeRS(rs);
			Misc.closePS(ps);
	
			if(count > 0){
				int[] rowCount = remotePS.executeBatch();
				System.out.println("DOUpdInfo.updateRemoteCurrDoStatus() rows inserted apprvd : "+rowCount.length);
			}
			Misc.closePS(remotePS);
			remoteConn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs= Misc.closeRS(rs);;
			ps = Misc.closePS(ps);
			remotePS = Misc.closePS(remotePS);
		}

	}
	
	public static void updateRemoteCurrDoStatusOld(Connection conn, Connection remoteConn, RemoteCredential uCred, DOUpdInfo doUpdInfo
			, PreparedStatement ps, ResultSet rs, PreparedStatement remotePS) throws Exception{

		String query = "select  do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
				", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id " +
				"from current_do_status where do_number = ? " ;
		String remoteQ = "insert ignore into current_do_status( do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
				", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id) " +
				"values (?,?,?,?,?,?,?,?,?,?,?)" ;
		String queryApp = "select do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
		", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id " +
		"from current_do_status_apprvd where do_number = ? " ;
		String remoteQApp = "insert ignore into current_do_status_apprvd( do_number, lifted_qty, last_lifted_on, last_lifted_qty, last_lifted_vehicle_id" +
		", last_lifted_tpr_id, trips_count_daily, trips_count, wb_code, current_allocation, wb_id) " +
		"values (?,?,?,?,?,?,?,?,?,?,?)" ;
		int count = 0;
		ps = conn.prepareStatement(query);
		ps.setString(1, doUpdInfo.doNumber);
		rs = ps.executeQuery();
		remotePS = remoteConn.prepareStatement(remoteQ);
		
		while(rs.next()){

//			int doId = Misc.getRsetInt(rs,"do_id");
			String doNumber = rs.getString("do_number");
			double liftedQty =Misc.getRsetDouble(rs, "lifted_qty");
			long lastLiftedOn = Misc.sqlToLong(rs.getTimestamp("last_lifted_on"));
			double lastLiftedQty =Misc.getRsetDouble(rs, "last_lifted_qty");
			int lastLiftedVehicleId = Misc.getRsetInt(rs,"last_lifted_vehicle_id");
			int lastLiftedTprId = Misc.getRsetInt(rs,"last_lifted_tpr_id");
			int tripsCountDaily = Misc.getRsetInt(rs,"trips_count_daily");
			int tripsCount = Misc.getRsetInt(rs,"trips_count");
			String wbCode = rs.getString("wb_code");
			double currentAllocationQty =Misc.getRsetDouble(rs, "current_allocation");
			int wbId = Misc.getRsetInt(rs,"wb_id");
			
			count = 0;
			
//			Misc.setParamInt(remotePS, doId,++count);
//			remotePS.setInt(++count, doId);
			remotePS.setString(++count, doNumber);
			Misc.setParamDouble(remotePS, liftedQty,++count);
//			remotePS.setDouble(++count, liftedQty);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(lastLiftedOn));
			Misc.setParamDouble(remotePS, lastLiftedQty,++count);
//			remotePS.setDouble(++count, lastLiftedQty);
			Misc.setParamInt(remotePS, lastLiftedVehicleId,++count);
//			remotePS.setInt(++count, lastLiftedVehicleId);
			Misc.setParamInt(remotePS, lastLiftedTprId,++count);
//			remotePS.setInt(++count, lastLiftedTprId);
			Misc.setParamInt(remotePS, tripsCountDaily,++count);
//			remotePS.setInt(++count, tripsCountDaily);
			Misc.setParamInt(remotePS, tripsCount,++count);
//			remotePS.setInt(++count, tripsCount);
			remotePS.setString(++count, wbCode);
			Misc.setParamDouble(remotePS, currentAllocationQty,++count);
//			remotePS.setDouble(++count, currentAllocationQty);
			Misc.setParamInt(remotePS, wbId,++count);
//			remotePS.setInt(++count, wbId);
			
			remotePS.addBatch();
		}

		Misc.closeRS(rs);
		Misc.closePS(ps);
		
		if(count > 0){
			int[] rowCount = remotePS.executeBatch();
			System.out.println("DOUpdInfo.updateRemoteCurrDoStatus() rows inserted : "+rowCount.length);
		}
		Misc.closePS(remotePS);
		remoteConn.commit();
		
		count = 0;
		ps = conn.prepareStatement(queryApp);
		ps.setString(1, doUpdInfo.doNumber);
		rs = ps.executeQuery();
		remotePS = remoteConn.prepareStatement(remoteQApp);
		
		while(rs.next()){

//			int doId = Misc.getRsetInt(rs,"do_id");
			String doNumber = rs.getString("do_number");
			double liftedQty =Misc.getRsetDouble(rs, "lifted_qty");
			long lastLiftedOn = Misc.sqlToLong(rs.getTimestamp("last_lifted_on"));
			double lastLiftedQty =Misc.getRsetDouble(rs, "last_lifted_qty");
			int lastLiftedVehicleId = Misc.getRsetInt(rs,"last_lifted_vehicle_id");
			int lastLiftedTprId = Misc.getRsetInt(rs,"last_lifted_tpr_id");
			int tripsCountDaily = Misc.getRsetInt(rs,"trips_count_daily");
			int tripsCount = Misc.getRsetInt(rs,"trips_count");
			String wbCode = rs.getString("wb_code");
			double currentAllocationQty =Misc.getRsetDouble(rs, "current_allocation");
			int wbId = Misc.getRsetInt(rs,"wb_id");
			
			count = 0;
			
//			Misc.setParamInt(remotePS, doId,++count);
//			remotePS.setInt(++count, doId);
			remotePS.setString(++count, doNumber);
			Misc.setParamDouble(remotePS, liftedQty,++count);
//			remotePS.setDouble(++count, liftedQty);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(lastLiftedOn));
			Misc.setParamDouble(remotePS, lastLiftedQty,++count);
//			remotePS.setDouble(++count, lastLiftedQty);
			Misc.setParamInt(remotePS, lastLiftedVehicleId,++count);
//			remotePS.setInt(++count, lastLiftedVehicleId);
			Misc.setParamInt(remotePS, lastLiftedTprId,++count);
//			remotePS.setInt(++count, lastLiftedTprId);
			Misc.setParamInt(remotePS, tripsCountDaily,++count);
//			remotePS.setInt(++count, tripsCountDaily);
			Misc.setParamInt(remotePS, tripsCount,++count);
//			remotePS.setInt(++count, tripsCount);
			remotePS.setString(++count, wbCode);
			Misc.setParamDouble(remotePS, currentAllocationQty,++count);
//			remotePS.setDouble(++count, currentAllocationQty);
			Misc.setParamInt(remotePS, wbId,++count);
//			remotePS.setInt(++count, wbId);
			
			remotePS.addBatch();
		}

		Misc.closeRS(rs);
		Misc.closePS(ps);

		if(count > 0){
			int[] rowCount = remotePS.executeBatch();
			System.out.println("DOUpdInfo.updateRemoteCurrDoStatus() rows inserted apprvd : "+rowCount.length);
		}
		Misc.closePS(remotePS);
		remoteConn.commit();

	}
	public static class RemoteCredential{
		private int id = Misc.getUndefInt(); 
		private String ip = null;
		private String port = null;
		private String db = null;
		private String userId = null;
		private String password = null;
		private String wbCode = null;
		
		public RemoteCredential(int id,String ip, String port, String db, String userId, String password, String wbCode){
			this.id=id;
			this.ip = ip;
			this.port = port;
			this.db = db;
			this.userId = userId;
			this.password = password;
			this.wbCode = wbCode;
		}
		@Override
		public String toString() {
			return super.toString()+" id:"+id+" ip:"+ip+" port:"+port+" db:"+db+" userId:"+userId+" password:"+password+" wbCode:"+wbCode;
		}
	}
	
	private static Connection getConnection(String ip, String port, String db, String userName, String password) throws SQLException {
		// MySQL
		if(ip == null || port == null || db == null || "".equalsIgnoreCase(ip) || "".equalsIgnoreCase(port) ||"".equalsIgnoreCase(db))
			return null;
		password = DBConnectionPool.unmangleString(password, Misc.g_doPasswordMangling);
		String connectString = "jdbc:mysql://"+ip+":"+port+"/"+db+"?zeroDateTimeBehavior=convertToNull&traceProtocol=false";
		
		Connection retval = ((DriverManager.getConnection(connectString, userName, password)));
		retval.setAutoCommit(false);
		return retval;
		

	}
	
	private static void returnConnection(Connection retConn, boolean destroyIt){
		if (retConn == null)
			return;
	
		boolean isClosed = false;
		boolean toCreateNew = false;
		try {
			isClosed = retConn.isClosed();
			com.mysql.jdbc.Connection mysqlConn = (com.mysql.jdbc.Connection) retConn;
			if (mysqlConn.getActiveStatementCount() > 0) {
				System.out.println("[MasterSync] connection being returned has open statements:"+retConn.hashCode()+" cnt:"+mysqlConn.getActiveStatementCount());
				destroyIt = true;
			}
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		//if not already closed - rollback or commit as needed
		if (!isClosed) {
			try {
				if (!retConn.getAutoCommit()) {
					if (destroyIt) {
						retConn.rollback();
						
					}//if to destroy
					else {
						retConn.commit();
					}//if to commit
				}//was not autco
			}
			catch (Exception e) {
				destroyIt = true;
				e.printStackTrace();
			}
		}
	}
	
	// using Bean DoDetails
	// to take lock on mines_do_details [field: lock_status(int) {0-unlock, 1-lock} ]
	// will change lock_status and update lock_changed_at in mines_do_details and update DoDetails object
	// if succ return dbStatus = 1, else dbStatus = 0
	public static void setDoLock(Connection conn, ArrayList <DOUpdInfo> doList, int lock) throws SQLException{
		PreparedStatement ps = null;
		long currTime = System.currentTimeMillis();
		System.out.println("[DOManager].[setDoLock] Start");
		String query = "update mines_do_details set lock_status = ?, lock_changed_at = ? where id = ? ";
		ps = conn.prepareStatement(query);
		
		for (Iterator iterator = doList.iterator(); iterator.hasNext();) {
			DOUpdInfo doDetails = (DOUpdInfo) iterator.next();
			if(doDetails.doId != Misc.getUndefInt()){
				ps.setInt(1, lock);
				ps.setTimestamp(2, Misc.longToSqlDate(currTime));
				ps.setInt(3, doDetails.doId);
				ps.executeUpdate();
				doDetails.resultStatus = SUCC;
				System.out.println("[DOManager].[setDoLock] SUCC do_id:"+doDetails.doId+" do_no:"+doDetails.doNumber);
			}else{
				doDetails.resultStatus = FAIL;
				doDetails.resultMessage = "DO id is undef";
				System.out.println("[DOManager].[setDoLock] FAIL do_no:"+doDetails.doNumber);
			}
		}
		Misc.closePS(ps);
		//conn.commit();
		System.out.println("[DOManager].[setDoLock] End");
	}
	
	public String getDoNumber() {
		return doNumber;
	}

	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}

	public int getDoId() {
		return doId;
	}

	public void setDoId(int doId) {
		this.doId = doId;
	}

	public String getWbCode() {
		return wbCode;
	}

	public void setWbCode(String wbCode) {
		this.wbCode = wbCode;
	}

	public int getWbId() {
		return wbId;
	}

	public void setWbId(int wbId) {
		this.wbId = wbId;
	}

	public double getCurrentAllocation() {
		return currentAllocation;
	}

	public void setCurrentAllocation(double currentAllocation) {
		this.currentAllocation = currentAllocation;
	}

	public double getLastQtyLifted() {
		return lastQtyLifted;
	}

	public void setLastQtyLifted(double lastQtyLifted) {
		this.lastQtyLifted = lastQtyLifted;
	}

	public int getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(int lockStatus) {
		this.lockStatus = lockStatus;
	}

	public int getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(int resultStatus) {
		this.resultStatus = resultStatus;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	
	public boolean isDoIncrementCurrentAlloc() {
		return doIncrementCurrentAlloc;
	}

	public void setDoIncrementCurrentAlloc(boolean doIncrementCurrentAlloc) {
		this.doIncrementCurrentAlloc = doIncrementCurrentAlloc;
	}

	public boolean isDoIncrementLastQtyLifted() {
		return doIncrementLastQtyLifted;
	}

	public void setDoIncrementLastQtyLifted(boolean doIncrementLastQtyLifted) {
		this.doIncrementLastQtyLifted = doIncrementLastQtyLifted;
	}

	public static void main(String[] args) {

		DOManager ms = new DOManager();
		System.out.println("[DOUpdInfo] : beginSync start ");
		Connection conn = null;
		
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			/*System.out.println("[DOUpdInfo] : beginSync updateIPSyncDetails start ");
			ArrayList<Integer> optionalOnlyMigrateTheseDo = null;
			ArrayList<Pair<Boolean, Integer>> retVal = migrateOpenTPR(conn, "WB07", "INOUTW", optionalOnlyMigrateTheseDo);
			if(retVal != null)
				return ;
			ArrayList <DOUpdInfo> doList = new ArrayList<DOUpdInfo>();
			DOUpdInfo doUpdInfo = new DOUpdInfo();
			doUpdInfo.setDoNumber("10000000001");doUpdInfo.setDoId(1);
			doList.add(doUpdInfo);
			DOUpdInfo doUpdInfo1 = new DOUpdInfo();
			doUpdInfo1.setDoNumber("10000000002");doUpdInfo1.setDoId(2);
			doList.add(doUpdInfo1);
			DOUpdInfo doUpdInfo2 = new DOUpdInfo();
			doUpdInfo2.setDoNumber("10000000001");doUpdInfo2.setDoId(1);doUpdInfo2.setWbCode("INOUTW");
			doList.add(doUpdInfo2);
			// DOUpdInfo.setDoLock(conn,doList,1);
			ArrayList <Pair<Integer, ArrayList<DOUpdInfo>>> updateThis = new ArrayList <Pair<Integer, ArrayList<DOUpdInfo>>>();
			updateThis.add(new Pair(5,doList));
			DOUpdInfo.sendToWB(conn, updateThis);
			System.out.println("[DOUpdInfo] : beginSync updateIPSyncDetails end ");*/
			
			
			/*activateTPR(conn, "1000GEVWB070100072340");
			cancelTPR(conn, "1000GEVWB070100072340");
			activateTPR(conn, "1000GEVWB070100072340");*/
			
			//changeTPRDONumber(conn, "1000GEVWB070100072338","241");
			
		} catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		} finally {
			try {
				destroyIt = false;
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("[DOUpdInfo] : beginSync end ");
	
	}
}

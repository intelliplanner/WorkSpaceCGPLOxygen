package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.db.DBSchemaManager;
import com.ipssi.rfid.processor.TPRInformation;

public class DOManager {
	public static final int UNLOCK = 0;
	public static final int LOCK = 1;
	public static final int SUCC = 1;
	public static final int FAIL = 0;
	
	// using Bean DoDetails
	// to take lock on mines_do_details [field: lock_status(int) {0-unlock, 1-lock} ]
	// will change lock_status and update lock_changed_at in mines_do_details and update DoDetails object
	// if succ return dbStatus = 1, else dbStatus = 0
	public static void setDoLock(Connection conn, ArrayList <DoDetails> doList, int lock) throws SQLException{
		PreparedStatement ps = null;
		long currTime = System.currentTimeMillis();
		System.out.println("[DOManager].[setDoLock] Start");
		String query = "update mines_do_details set lock_status = ?, lock_changed_at = ? where id = ? ";
		ps = conn.prepareStatement(query);
		
		for (Iterator iterator = doList.iterator(); iterator.hasNext();) {
			DoDetails doDetails = (DoDetails) iterator.next();
			if(doDetails.getId() != Misc.getUndefInt()){
				ps.setInt(1, lock);
				ps.setTimestamp(2, Misc.longToSqlDate(currTime));
				ps.setInt(3, doDetails.getId());
				ps.executeUpdate();
				doDetails.setDoDbStatus(SUCC);
				System.out.println("[DOManager].[setDoLock] SUCC do_id:"+doDetails.getId()+" do_no:"+doDetails.getDoNumber());
			}else{
				doDetails.setDoDbStatus(FAIL);
				System.out.println("[DOManager].[setDoLock] FAIL do_no:"+doDetails.getDoNumber());
			}
		}
		ps.close();
		//conn.commit();
		System.out.println("[DOManager].[setDoLock] End");
	}
	
	// update current_do_status @ remote/client and update remoteDbStatus in LatestDOInfo
	public static ArrayList <Pair<Integer,Integer>> updateRemoteCurrentDoStatus(Connection conn, ArrayList <Pair<Integer, ArrayList<DOUpdInfo>>> doStatusList) throws SQLException{

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList <Pair<Integer,Integer>> retVal = new ArrayList <Pair<Integer,Integer>>();
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Start");

		int wbId = Misc.getUndefInt();
		ArrayList<DOUpdInfo> doUpdInfoList = null;
		ArrayList <Pair<RemoteCredential,String>> tableArr = null;
		String query = "select sisd.id,sd.ip, sd.port, sd.db, sd.user_id, sd.password, sisd.code from secl_workstation_details sisd join secl_ip_details sd on (sisd.uid = sd.mac_id) where sisd.status = 1 and sd.status = 1 and sisd.id = ?" ;

		for (Iterator iterator = doStatusList.iterator(); iterator.hasNext();) {
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
						int id = Misc.getRsetInt(rs, "id");
						String ip = rs.getString("ip");
						String port = rs.getString("port");
						String db = rs.getString("db");
						String userId = rs.getString("user_id");
						String password = rs.getString("password");
						String wbCode = rs.getString("code");
						uCred = new RemoteCredential(id,ip,port,db,userId,password,wbCode);
					}
					rs.close();
					ps.close();
					
					updateCurrDOStatus(conn, uCred, doUpdInfoList);
					
					retVal.add(new Pair(wbId, SUCC));
				}catch(Exception e){
					retVal.add(new Pair(wbId, FAIL));
					System.out.println("[DOManager].[updateRemoteCurrentDoStatus] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
					e.printStackTrace();
				}
				
			}
		}
		System.out.println("[DOManager].[updateRemoteCurrentDoStatus] End");
		return retVal;
	}
	
	public static boolean copyTprToWorkStation(Connection conn, String fromWbCode, String toWbCode) throws Exception{
		
		boolean retVal = true;
		String schema = "ipssi_secl";
		StringBuilder query = new StringBuilder("select * from tp_record where (load_gate_in_name = ");
		query.append(fromWbCode).append(" or load_wb_in_name = ").append(fromWbCode).append(" or load_yard_in_name = ").append(fromWbCode);
		query.append(" or load_wb_out_name = ").append(fromWbCode).append(" or load_gate_out_name = ").append(fromWbCode);
		query.append(" or unload_gate_in_name = ").append(fromWbCode).append(" or unload_wb_in_name = ").append(fromWbCode);
		query.append(" or unload_yard_in_name = ").append(fromWbCode).append(" or unload_wb_out_name = ").append(fromWbCode);
		query.append(" or unload_gate_out_name = ").append(fromWbCode).append(" ) and is_latest = 1 and tpr_status = 0 ");
		
		Pair<java.sql.Timestamp,ArrayList<TPRecord>> tprListPair = DBSchemaManager.getList(conn, TPRecord.class, schema, query);
		
		String qu = "select ssid.id,sd.ip, sd.port, sd.db, sd.user_id, sd.password, sisd.code from secl_workstation_details sisd join secl_ip_details sd on (sisd.uid = sd.mac_id) where sisd.status = 1 and sd.status = 1 and sisd.code = ?" ;
		RemoteCredential uCred = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(qu);
			ps.setString(1, toWbCode);
			rs = ps.executeQuery();
			while(rs.next()){
				int id = Misc.getRsetInt(rs, "id");
				String ip = rs.getString("ip");
				String port = rs.getString("port");
				String db = rs.getString("db");
				String userId = rs.getString("user_id");
				String password = rs.getString("password");
				String wbCode = rs.getString("code");
				uCred = new RemoteCredential(id,ip,port,db,userId,password,wbCode);
			}
			rs.close();
			ps.close();
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
					retVal = false;
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
				retVal = false;
			}
		return retVal;
	}
	
	public static void updateCurrDOStatus(Connection conn, RemoteCredential uCred, ArrayList<DOUpdInfo> doUpdInfoList){
		Connection remoteConn = null;
		PreparedStatement remotePS = null;
		ResultSet remoteRS = null;
		boolean destroyRemote = false;
		System.out.println("[DOManager].[updateCurrDOStatus] Start");

		if(doUpdInfoList != null && doUpdInfoList.size() > 0 && uCred != null){
			try{
				destroyRemote = false;

				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				String query = "update current_do_status set current_allocation = ?, lifted_qty = ? where do_number = ? and wb_code = ?";
				remotePS = remoteConn.prepareStatement(query);
				int count = Misc.getUndefInt();
				/*for (Iterator iterator = doUpdInfoList.iterator(); iterator.hasNext();) {
					DOUpdInfo doUpdInfo = (DOUpdInfo) iterator.next();
					if(doUpdInfo != null && doUpdInfo.doNumber != null && !"".equals(doUpdInfo.doNumber)){
						remotePS.setDouble(1, doUpdInfo.clientAllocationQty);
						remotePS.setDouble(2, doUpdInfo.alreadyLiftedQty);
						remotePS.setString(3, doUpdInfo.doNumber);
						remotePS.setString(4, uCred.wbCode);
						count = remotePS.executeUpdate();
						if(count == 0){
							updateRemoteInBatch(conn,  remoteConn, uCred, doUpdInfo);
						}

						doUpdInfo.syncStatus = SUCC;
						System.out.println("[DOManager].[updateCurrDOStatus] SUCC do_no:"+doUpdInfo.doNumber);
					}else{
						doUpdInfo.syncStatus = FAIL;
						System.out.println("[DOManager].[updateCurrDOStatus] FAIL do_no:"+ doUpdInfo != null ? doUpdInfo.doNumber : "Null");
					}
				}*/					
				//updateRemoteInBatch(conn, ps, rs, remoteConn, remotePS, remoteRS, pair);
				//updateStatus(conn,uCred.id,2);
				remotePS.close();
				remoteConn.commit();
			}catch (Exception e){
				// updateStatus(conn,uCred.id,3);
				destroyRemote = true;
				System.out.println("[DOManager] Error while updating remote machine: "+"[Thread:"+Thread.currentThread().getId()+"] uCred: "+uCred+" remoteConn: "+remoteConn);
				e.printStackTrace();
			}finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else{
			System.out.println("[DOManager] doUpdInfoList size is 0 : "+"[Thread:"+Thread.currentThread().getId()+"] uCred: "+ uCred);
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
	
	public static void updateRemoteInBatch(Connection conn, Connection remoteConn, RemoteCredential uCred, DOUpdInfo doUpdInfo) throws Exception{

		if(doUpdInfo == null || uCred == null)
			return;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement remotePS = null;

		String query = "select id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person" +
		",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge" +
		",dump_charge,stow_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on" +
		",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1" +
		",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code from mines_do_details where do_number = ? " ;
		String remoteQ = "insert into mines_do_details(id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person" +
		",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge" +
		",dump_charge,stow_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on" +
		",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1" +
		",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;

		ps = conn.prepareStatement(query);
//		ps.setString(1, doUpdInfo.doNumber);
		rs = ps.executeQuery();
		remotePS = remoteConn.prepareStatement(remoteQ);
		
		while(rs.next()){

			int id = Misc.getRsetInt(rs,"id");
			String name = rs.getString("do_number");
			long doDate = Misc.sqlToLong(rs.getTimestamp("do_date"));
			long doReleaseDate = Misc.sqlToLong(rs.getTimestamp("do_release_date"));
			String doReleaseNo = rs.getString("do_release_no");
			int typeOfConsumer = Misc.getRsetInt(rs,"type_of_consumer");
			int customer = Misc.getRsetInt(rs,"customer");
			String customerRef = rs.getString("customer_ref");
			String customerContactPerson = rs.getString("customer_contact_person");
			int grade = Misc.getRsetInt(rs,"grade");
			int coalSize = Misc.getRsetInt(rs,"coal_size");
			int sourceMines = Misc.getRsetInt(rs,"source_mines");
			int washery = Misc.getRsetInt(rs,"washery");
			double qtyAlloc =Misc.getRsetDouble(rs, "qty_alloc");
			double qtyAlreadyLifted =Misc.getRsetDouble(rs, "qty_already_lifted");
			double quota =Misc.getRsetDouble(rs, "quota");
			double rate =Misc.getRsetDouble(rs, "rate");
			double transportCharge =Misc.getRsetDouble(rs, "transport_charge");
			double sizingCharge =Misc.getRsetDouble(rs, "sizing_charge");
			double siloCharge =Misc.getRsetDouble(rs, "silo_charge");
			double dumpCharge =Misc.getRsetDouble(rs, "dump_charge");
			double stowCharge =Misc.getRsetDouble(rs, "stow_charge");
			double terminalCharge =Misc.getRsetDouble(rs, "terminal_charge");
			double forestCess =Misc.getRsetDouble(rs, "forest_cess");
			double stowEd =Misc.getRsetDouble(rs, "stow_ed");
			double avap =Misc.getRsetDouble(rs, "avap");
			int allowNoTare = Misc.getRsetInt(rs,"allow_no_tare"); 
			int maTareGap = Misc.getRsetInt(rs,"max_tare_gap"); 
			int destination = Misc.getRsetInt(rs,"destination"); 
			int status = Misc.getRsetInt(rs,"status"); 
			long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
			long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
			String updatedBy = rs.getString("updated_by");
			int portNodeId = Misc.getRsetInt(rs,"port_node_id");  
			String createdBy = rs.getString("created_by");  
			String gradeCode = rs.getString("grade_code"); 
			String sourceCode = rs.getString("source_code"); 
			String washeryCode = rs.getString("washery_code"); 
			String destinationCode = rs.getString("destination_code"); 
			String preferedWb_1 = rs.getString("prefered_wb_1"); 
			String preferedWb_2 = rs.getString("prefered_wb_2"); 
			String preferedWb_3 = rs.getString("prefered_wb_3"); 
			String preferedWb_4 = rs.getString("prefered_wb_4"); 
			String customerCode = rs.getString("customer_code"); 

			int count = 0;
			remotePS.setInt(++count, id);
			remotePS.setString(++count, name);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(doDate));
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(doReleaseDate));
			remotePS.setString(++count, doReleaseNo);
			remotePS.setInt(++count, typeOfConsumer);
			remotePS.setInt(++count, customer);
			remotePS.setString(++count, customerRef);
			remotePS.setString(++count, customerContactPerson);
			remotePS.setInt(++count, grade);
			remotePS.setInt(++count, coalSize);
			remotePS.setInt(++count, sourceMines);
			remotePS.setInt(++count, washery);
			remotePS.setDouble(++count, qtyAlloc);
			remotePS.setDouble(++count, qtyAlreadyLifted);
			remotePS.setDouble(++count, quota);
			remotePS.setDouble(++count, rate);
			remotePS.setDouble(++count, transportCharge);
			remotePS.setDouble(++count, sizingCharge);
			remotePS.setDouble(++count, siloCharge);
			remotePS.setDouble(++count, dumpCharge);
			remotePS.setDouble(++count, stowCharge);
			remotePS.setDouble(++count, terminalCharge);
			remotePS.setDouble(++count, forestCess);
			remotePS.setDouble(++count, stowEd);
			remotePS.setDouble(++count, avap);
			remotePS.setInt(++count, allowNoTare);
			remotePS.setInt(++count, maTareGap);
			remotePS.setInt(++count, destination);
			remotePS.setInt(++count, status);
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
			remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
			remotePS.setString(++count, updatedBy);
			remotePS.setInt(++count, portNodeId);
			remotePS.setString(++count, createdBy);
			remotePS.setString(++count, gradeCode);
			remotePS.setString(++count, sourceCode);
			remotePS.setString(++count, washeryCode);
			remotePS.setString(++count, destinationCode);
			remotePS.setString(++count, preferedWb_1);
			remotePS.setString(++count, preferedWb_2);
			remotePS.setString(++count, preferedWb_3);
			remotePS.setString(++count, preferedWb_4);
			remotePS.setString(++count, customerCode);

		}

		rs.close();
		ps.close();

		remotePS.execute();
		remotePS.close();

		/*ps = conn.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();*/

	}
	public static class RemoteCredential{
		private String ip = null;
		private String port = null;
		private String db = null;
		private String userId = null;
		private String password = null;
		private String wbCode = null;
		private int id = Misc.getUndefInt();
		
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
	public static void main(String[] args) {
		DOManager ms = new DOManager();
		System.out.println("[DOManager] : beginSync start ");
		Connection conn = null;
		
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			System.out.println("[DOManager] : beginSync updateIPSyncDetails start ");
//			boolean hasAnyChange =  DOManager.updateIPSyncDetails(conn);
			System.out.println("[DOManager] : beginSync updateIPSyncDetails end ");
			//if(hasAnyChange){
				System.out.println("[DOManager] : beginSync updateRemoteServers start ");
				//DOManager.updateRemoteServers(conn);
				System.out.println("[DOManager] : beginSync updateRemoteServers end ");
			//}

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
		System.out.println("[DOManager] : beginSync end ");
	}
	
}

package com.ipssi.secl.sync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.SECLDo;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.SECLDataHolder;

public class MasterSync {

	public static class UserCredential {
		private String ip = null;
		private String port = null;
		private String db = null;
		private String userId = null;
		private String password = null;
		int id = Misc.getUndefInt();

		public UserCredential(String ip, String port, String db, String userId,
				String password, int id) {
			this.ip = ip;
			this.port = port;
			this.db = db;
			this.userId = userId;
			this.password = password;
			this.id = id;
		}

		@Override
		public String toString() {
			return super.toString() + " ip:" + ip + " port:" + port + " db:"
					+ db + " userId:" + userId + " password:" + password;
		}
	}

	public static boolean updateIPSyncDetails(Connection conn)
			throws SQLException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psSync = null;
		String tableStr = "";
		// Date last_sync=Utils.getDateTime(lastSync);
		String query = "select table_name from secl_master_change where last_updated_on is not null";
		ArrayList<String> tableArr = new ArrayList<String>();

		ps = conn.prepareStatement(query);
		rs = ps.executeQuery();
		while (rs.next()) {
			String tableName = rs.getString("table_name");
			tableArr.add(tableName);
			tableStr = !"".equalsIgnoreCase(tableStr) ? tableStr + "," + "'"
					+ tableName + "'" : "'" + tableName + "'";
		}

		rs.close();
		ps.close();

		if (tableArr == null || tableArr.size() == 0)
			return false;
		long currTime = System.currentTimeMillis();
		psSync = conn
				.prepareStatement("insert into secl_ip_sync_details (ip,table_name,status,created_on) values (?,?,?,?)");
		query = "select ip from secl_ip_details where status = 1";
		ps = conn.prepareStatement(query);
		rs = ps.executeQuery();
		while (rs.next()) {
			String ip = rs.getString("ip");
			if (ip != null && !"".equalsIgnoreCase(ip)) {
				for (String table : tableArr) {
					psSync.setString(1, ip);
					psSync.setString(2, table);
					psSync.setInt(3, 1);
					psSync.setTimestamp(4, Misc.longToSqlDate(currTime));
					psSync.addBatch();
				}
			}

		}
		rs.close();
		ps.close();

		psSync.executeBatch();

		psSync.close();
		if (tableStr != null && tableStr.length() > 0) {
			query = "update secl_master_change set last_updated_on = null where table_name in ("
					+ tableStr + ") ";
			ps = conn.prepareStatement(query);
			ps.execute();

			ps.close();
		}
		return true;
	}

	public static void updateRemoteServers(Connection conn) throws SQLException {

		PreparedStatement ps = null;
		ResultSet rs = null;
		String tableStr = "";
		// Date last_sync=Utils.getDateTime(lastSync);
		String query = "select sd.ip, sd.port, sd.db, sd.user_id, sd.password, sisd.table_name, sisd.id from secl_ip_sync_details sisd join secl_ip_details sd on (sisd.ip = sd.ip) where sisd.status = 1 and sd.status = 1";
		ArrayList<Pair<UserCredential, String>> tableArr = new ArrayList<Pair<UserCredential, String>>();
		String prevIp = null;
		UserCredential prevUCred = null;

		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				String ip = rs.getString("ip");
				String port = rs.getString("port");
				String db = rs.getString("db");
				String userId = rs.getString("user_id");
				String password = rs.getString("password");
				String tableName = rs.getString("table_name");
				int id = Misc.getRsetInt(rs, "id");
				UserCredential uCred = new UserCredential(ip, port, db, userId,
						password, id);
				if (prevIp != ip) {
					tableArr.add(new Pair<UserCredential, String>(prevUCred,
							tableStr));
					tableStr = "";
					prevUCred = uCred;
				}
				tableStr = !"".equalsIgnoreCase(tableStr) ? tableStr + ","
						+ tableName : tableName;

			}
			tableArr.add(new Pair<UserCredential, String>(prevUCred, tableStr));

			rs.close();
			ps.close();
		} catch (Exception e) {
			System.out
					.println("[MasterSync] Error while getting list of remote system list: "
							+ "[Thread:"
							+ Thread.currentThread().getId()
							+ "] dbconn: " + conn);
			e.printStackTrace();
		}
		if (tableArr != null && tableArr.size() > 0) {
			Connection remoteConn = null;
			PreparedStatement remotePS = null;
			ResultSet remoteRS = null;
			boolean destroyRemote = false;

			for (Pair<UserCredential, String> pair : tableArr) {
				if (pair.first != null && pair.second != null
						&& pair.second.length() > 0) {

					try {
						destroyRemote = false;
						remoteConn = getConnection(pair.first.ip,
								pair.first.port, pair.first.db,
								pair.first.userId, pair.first.password);

						updateRemoteInBatch(conn, ps, rs, remoteConn, remotePS,
								remoteRS, pair);

					} catch (Exception e) {
						destroyRemote = true;
						System.out
								.println("[MasterSync] Error while updating remote machine: "
										+ "[Thread:"
										+ Thread.currentThread().getId()
										+ "] pair: "
										+ pair
										+ " remoteConn: "
										+ remoteConn);
						e.printStackTrace();
					} finally {
						try {
							returnConnection(remoteConn, destroyRemote);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} else {
					System.out.println("[MasterSync] No IP details: "
							+ "[Thread:" + Thread.currentThread().getId()
							+ "] pair: " + pair);
				}
			}

		} else {
			System.out.println("[MasterSync] No System need updation: "
					+ "[Thread:" + Thread.currentThread().getId() + "]");
		}
	}

	public UserCredential getCredentials(Connection conn, String wbCode){
		String QUERY = "select ip,port,db,user_id,password from secl_ip_details join secl_workstation_details on (uid=mac_id) where code=?";
		UserCredential uCred=null;
		try{
			PreparedStatement ps = conn.prepareStatement(QUERY);
			ps.setString(1, wbCode);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String ip = rs.getString("ip");
				String port = rs.getString("port");
				String db = rs.getString("db");
				String userId = rs.getString("user_id");
				String password = rs.getString("password");
			//	int id = Misc.getRsetInt(rs,"id");
				uCred = new UserCredential(ip,port,db,userId,password,Misc.UNDEF_VALUE);
			}
			rs.close();
			ps.close();
			}catch(Exception e){
				System.out.println("[MasterSync] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
				e.printStackTrace();
			}
			return uCred;
	}
	

	public  SECLDo updateRemoteDODetails(Connection conn, SECLDo doDetails)
	{
//		PreparedStatement ps;
//		ResultSet rs;
		Connection remoteConn=null;
//		PreparedStatement remotePS;
//		ResultSet remoteRS;
//		Pair<UserCredential, String> pair; 
		boolean destroyRemote =false;
		
		if(!doDetails.getPreferredWB1().equalsIgnoreCase(doDetails.getPreferredWB1old())){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB1());
			try{remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
			
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				doDetails.setFlagWB1(false);
				destroyRemote =true;
			}finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			UserCredential uCredOld = getCredentials(conn,doDetails.getPreferredWB1old());
			try{remoteConn = getConnection(uCredOld.ip, uCredOld.port, uCredOld.db, uCredOld.userId, uCredOld.password);
			
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB1Old(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else if(doDetails.getPreferredQty1()!=doDetails.getPreferredQty1old()){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB1());
			try{remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
			
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB1(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
		}
		if(!doDetails.getPreferredWB2().equalsIgnoreCase(doDetails.getPreferredWB2old())){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB2());
			try{remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
			
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB2(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
			UserCredential uCredOld = getCredentials(conn,doDetails.getPreferredWB2old());
			try{
			remoteConn = getConnection(uCredOld.ip, uCredOld.port, uCredOld.db, uCredOld.userId, uCredOld.password);
			
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB2Old(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else if(doDetails.getPreferredQty2()!=doDetails.getPreferredQty2old()){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB2());
			try{
			remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
			
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB2(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		
		if(!doDetails.getPreferredWB3().equalsIgnoreCase(doDetails.getPreferredWB3old())){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB3());
			try{	
				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB3(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
			UserCredential uCredOld = getCredentials(conn,doDetails.getPreferredWB3old());
			try{	
				remoteConn = getConnection(uCredOld.ip, uCredOld.port, uCredOld.db, uCredOld.userId, uCredOld.password);
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB3Old(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else if(doDetails.getPreferredQty3()!=doDetails.getPreferredQty3old()){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB3());
			try{
				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB3(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		
		if(!doDetails.getPreferredWB4().equalsIgnoreCase(doDetails.getPreferredWB4old())){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB4());
			try{
				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB4(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
			UserCredential uCredOld = getCredentials(conn,doDetails.getPreferredWB4old());
			try{
				remoteConn = getConnection(uCredOld.ip, uCredOld.port, uCredOld.db, uCredOld.userId, uCredOld.password);
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			}catch(Exception e){
				destroyRemote =true;
				doDetails.setFlagWB4Old(false);
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}else if(doDetails.getPreferredQty4()!=doDetails.getPreferredQty4old()){
			UserCredential uCred = getCredentials(conn,doDetails.getPreferredWB4());
			try {
				remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);
				fetchAndUpdateRemote(conn,remoteConn,doDetails.getId());
			} catch (Exception e1) {
				destroyRemote =true;
				doDetails.setFlagWB4(false);
				e1.printStackTrace();
			}
			finally {
				try {
					returnConnection(remoteConn, destroyRemote);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		return doDetails;
		
	}

	public void fetchAndUpdateRemote(Connection conn,Connection remoteConn,int doID) throws SQLException {
		PreparedStatement ps;
		ResultSet rs;
		PreparedStatement remotePS;
		ResultSet remoteRS;
		remoteConn.setAutoCommit(false);
		String query = "select id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person"
				+ ",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge"
				+ ",dump_charge,stow_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on"
				+ ",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1"
				+ ",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code from mines_do_details where id=?";
		String remoteQ = "insert into mines_do_details(id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person"
				+ ",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge"
				+ ",dump_charge,stow_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on"
				+ ",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1"
				+ ",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key "
				+ "update do_number=?,do_date=?,do_release_date=?,do_release_no=?,type_of_consumer=?,customer=?,customer_ref=?,customer_contact_person=?"
				+ ",grade=?,coal_size=?,source_mines=?,washery=?,qty_alloc=?,qty_already_lifted=?,quota=?,rate=?,transport_charge=?,sizing_charge=?,silo_charge=?"
				+ ",dump_charge=?,stow_charge=?,terminal_charge=?,forest_cess=?,stow_ed=?,avap=?,allow_no_tare=?,max_tare_gap=?,destination=?,status=?,created_on=?"
				+ ",updated_on=?,updated_by=?,port_node_id=?,created_by=?,grade_code=?,source_code=?,washery_code=?,destination_code=?,prefered_wb_1=?,prefered_wb_2=?,prefered_wb_3=?,prefered_wb_4=?,customer_code=?";

		ps = conn.prepareStatement(query);
		ps.setInt(1, doID);
		rs = ps.executeQuery();

		remotePS = remoteConn.prepareStatement(remoteQ);
		while (rs.next()) {

			int id = Misc.getRsetInt(rs, "id");
			String name = rs.getString("do_number");
			long doDate = Misc.sqlToLong(rs.getTimestamp("do_date"));
			long doReleaseDate = Misc.sqlToLong(rs
					.getTimestamp("do_release_date"));
			String doReleaseNo = rs.getString("do_release_no");
			int typeOfConsumer = Misc.getRsetInt(rs, "type_of_consumer");
			int customer = Misc.getRsetInt(rs, "customer");
			String customerRef = rs.getString("customer_ref");
			String customerContactPerson = rs
					.getString("customer_contact_person");
			int grade = Misc.getRsetInt(rs, "grade");
			int coalSize = Misc.getRsetInt(rs, "coal_size");
			int sourceMines = Misc.getRsetInt(rs, "source_mines");
			int washery = Misc.getRsetInt(rs, "washery");
			double qtyAlloc = Misc.getRsetDouble(rs, "qty_alloc",0.0);
			double qtyAlreadyLifted = Misc.getRsetDouble(rs,
					"qty_already_lifted",0.0);
			double quota = Misc.getRsetDouble(rs, "quota",0.0);
			double rate = Misc.getRsetDouble(rs, "rate",0.0);
			double transportCharge = Misc.getRsetDouble(rs, "transport_charge",0.0);
			double sizingCharge = Misc.getRsetDouble(rs, "sizing_charge",0.0);
			double siloCharge = Misc.getRsetDouble(rs, "silo_charge",0.0);
			double dumpCharge = Misc.getRsetDouble(rs, "dump_charge",0.0);
			double stowCharge = Misc.getRsetDouble(rs, "stow_charge",0.0);
			double terminalCharge = Misc.getRsetDouble(rs, "terminal_charge",0.0);
			double forestCess = Misc.getRsetDouble(rs, "forest_cess",0.0);
			double stowEd = Misc.getRsetDouble(rs, "stow_ed",0.0);
			double avap = Misc.getRsetDouble(rs, "avap",0.0);
			int allowNoTare = Misc.getRsetInt(rs, "allow_no_tare");
			int maTareGap = Misc.getRsetInt(rs, "max_tare_gap");
			int destination = Misc.getRsetInt(rs, "destination");
			int status = Misc.getRsetInt(rs, "status");
			long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
			long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
			String updatedBy = rs.getString("updated_by");
			int portNodeId = Misc.getRsetInt(rs, "port_node_id");
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
			// UPDATE query data
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

			

			remotePS.execute();
			remotePS.close();
			remoteConn.commit();
			//remoteConn.commit();
		}
		rs.close();
		ps.close();
	}

	public static ArrayList<Pair<String,Boolean>> updateRemoteDO(Connection conn, DoDetails doDetails) throws SQLException{
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String tableStr = "";
		ArrayList<Pair<String,Boolean>> retVal = new ArrayList<Pair<String,Boolean>>();
		if(doDetails == null || doDetails.getId() == Misc.getUndefInt()){
			System.out.println("MasterSync.updateRemoteDO : " + "Either doDetail is null or do id is undef" );
			return null;
		}
		//Date last_sync=Utils.getDateTime(lastSync);
		String query = "select swsd1.name name1, swsd1.id id1, sd1.ip ip1, sd1.port port1, sd1.db db1, sd1.user_id user1, sd1.password pass1 "
	+",swsd2.name name2, swsd2.id id2, sd2.ip ip2, sd2.port port2, sd2.db db2, sd2.user_id user2, sd2.password pass2 "
	+",swsd3.name name3, swsd3.id id3, sd3.ip ip3, sd3.port port3, sd3.db db3, sd3.user_id user3, sd3.password pass3 "
	+",swsd4.name name4, swsd4.id id4, sd4.ip ip4, sd4.port port4, sd4.db db4, sd4.user_id user4, sd4.password pass4 "
	+"from mines_do_details mdd   "
	+"left outer join secl_workstation_details swsd1  "
	+"on (mdd.prefered_wb_1 = swsd1.code) left outer join secl_ip_details sd1 on (swsd1.uid = sd1.mac_id)  "
	+"left outer join secl_workstation_details swsd2  "
	+"on (mdd.prefered_wb_2 = swsd2.code) left outer join secl_ip_details sd2 on (swsd2.uid = sd2.mac_id)  "
	+"left outer join secl_workstation_details swsd3  "
	+"on (mdd.prefered_wb_3 = swsd3.code) left outer join secl_ip_details sd3 on (swsd3.uid = sd3.mac_id)  "
	+"left outer join secl_workstation_details swsd4  "
	+"on (mdd.prefered_wb_4 = swsd4.code) left outer join secl_ip_details sd4 on (swsd4.uid = sd4.mac_id)  "
	+"where mdd.id = ? and swsd1.status = 1 and sd1.status = 1 and  "
	+" swsd2.status = 1 and sd2.status = 1 and  "
	+" swsd3.status = 1 and sd3.status = 1 and  "
	+" swsd4.status = 1 and sd4.status = 1 " ;
		
//		select ip from secl_ip_details join secl_workstaion_details on (uid=mac_id) where code=?
		ArrayList <Pair<UserCredential,String>> tableArr = new ArrayList<Pair<UserCredential,String>>();
		String prevIp = null;
		UserCredential prevUCred = null;
		
		try{
		ps = conn.prepareStatement(query);
		ps.setInt(1, doDetails.getId());
		rs = ps.executeQuery();
		int rowIndex = 1;
		while(rs.next()){
			String ip = rs.getString("ip"+rowIndex);
			String port = rs.getString("port"+rowIndex);
			String db = rs.getString("db"+rowIndex);
			String userId = rs.getString("user"+rowIndex);
			String password = rs.getString("pass"+rowIndex);
			String tableName = rs.getString("name"+rowIndex);
			int id = Misc.getRsetInt(rs,"id"+rowIndex);
			UserCredential uCred = new UserCredential(ip,port,db,userId,password,id);
			tableArr.add(new Pair<UserCredential,String>(uCred,tableName));
			rowIndex += 1;
			
//			if(prevIp != ip){
//				tableArr.add(new Pair<UserCredential,String>(prevUCred,tableStr));
//				tableStr = "";
//				prevUCred = uCred;
//			}
//			tableStr = !"".equalsIgnoreCase(tableStr) ? tableStr+","+tableName : tableName;
//			
//		}
//		tableArr.add(new Pair<UserCredential,String>(prevUCred,tableStr));
		}
		
		
		rs.close();
		ps.close();
		}catch(Exception e){
			System.out.println("[MasterSync] Error while getting list of remote system list: "+"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
			e.printStackTrace();
		}
		if(tableArr != null && tableArr.size() > 0){
			Connection remoteConn = null;
			PreparedStatement remotePS = null;
			ResultSet remoteRS = null;
			boolean destroyRemote = false;
			
			for (Pair<UserCredential, String> pair : tableArr) {
				if(pair.first != null && pair.second != null && pair.second.length() > 0){
					
					try{
						destroyRemote = false;
						remoteConn = getConnection(pair.first.ip, pair.first.port, pair.first.db, pair.first.userId, pair.first.password);

						updateRemoteInBatch(conn, ps, rs, remoteConn, remotePS, remoteRS, pair);

					}catch(Exception e){
						destroyRemote = true;
						System.out.println("[MasterSync] Error while updating remote machine: "+"[Thread:"+Thread.currentThread().getId()+"] pair: "+pair+" remoteConn: "+remoteConn);
						e.printStackTrace();
					}finally {
						try {
							returnConnection(remoteConn, destroyRemote);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}else{
					System.out.println("[MasterSync] No IP details: "+"[Thread:"+Thread.currentThread().getId()+"] pair: "+ pair);
				}
			}
						
		}else{
			System.out.println("[MasterSync] No System need updation: "+"[Thread:"+Thread.currentThread().getId()+"]");
		}
		return retVal;
	}

	public static void updateRemoteInBatch(Connection conn,
			PreparedStatement ps, ResultSet rs, Connection remoteConn,
			PreparedStatement remotePS, ResultSet remoteRS,
			Pair<UserCredential, String> pair) throws Exception {

		if (pair == null || pair.first.ip == null
				|| Misc.isUndef(pair.first.id) || pair.second == null
				|| pair.second.length() < 1)
			return;
		String query = "";
		String remoteQ = "";
		int doTruncate = -1;

		if ("customers".equalsIgnoreCase(pair.second)) {
			query = "select id, name, short_code, status, notes, type, partner, device_count, location, created_on, created_by, updated_on from customers";
			remoteQ = "insert into customers(id, name, short_code, status, notes, type, partner, device_count, location, created_on, created_by, updated_on) values (?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("name");
				String shortCode = rs.getString("short_code");
				String status = rs.getString("status");
				String notes = rs.getString("notes");
				String type = rs.getString("type");
				String partner = rs.getString("partner");
				int deviceCount = Misc.getRsetInt(rs, "device_count");
				String location = rs.getString("location");
				long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
				String createdBy = rs.getString("created_by");
				long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, shortCode);
				remotePS.setString(++count, status);
				remotePS.setString(++count, notes);
				remotePS.setString(++count, type);
				remotePS.setString(++count, partner);
				remotePS.setInt(++count, deviceCount);
				remotePS.setString(++count, location);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
				remotePS.setString(++count, createdBy);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));

				if (doTruncate < 1)
					remotePS.addBatch("truncate customers");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("destination_details".equalsIgnoreCase(pair.second)) {
			query = "select id, name, sap_code, address, status, created_on, created_by, updated_on, updated_by, port_node_id from destination_details";
			remoteQ = "insert into destination_details(id, name, sap_code, address, status, created_on, created_by, updated_on, updated_by, port_node_id) values (?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("name");
				String sapCode = rs.getString("sap_code");
				String address = rs.getString("address");
				String status = rs.getString("status");
				long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
				String createdBy = rs.getString("created_by");
				long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
				String updatedBy = rs.getString("updated_by");
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, sapCode);
				remotePS.setString(++count, address);
				remotePS.setString(++count, status);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
				remotePS.setString(++count, createdBy);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
				remotePS.setString(++count, updatedBy);
				remotePS.setInt(++count, portNodeId);

				if (doTruncate < 1)
					remotePS.addBatch("truncate destination_details");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("grade_details".equalsIgnoreCase(pair.second)) {
			query = "select id, name, sap_code, comments, status, created_on, created_by, updated_on, updated_by, port_node_id, gcv_low, gcv_high, impurities, sn from grade_details";
			remoteQ = "insert into grade_details(id, name, sap_code, comments, status, created_on, created_by, updated_on, updated_by, port_node_id, gcv_low, gcv_high, impurities, sn) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("name");
				String sapCode = rs.getString("sap_code");
				String comments = rs.getString("comments");
				String status = rs.getString("status");
				long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
				String createdBy = rs.getString("created_by");
				long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
				String updatedBy = rs.getString("updated_by");
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");
				int gcvLow = Misc.getRsetInt(rs, "gcv_low");
				int gcvHigh = Misc.getRsetInt(rs, "gcv_high");
				String impurities = rs.getString("impurities");
				String sn = rs.getString("sn");

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, sapCode);
				remotePS.setString(++count, comments);
				remotePS.setString(++count, status);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
				remotePS.setString(++count, createdBy);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
				remotePS.setString(++count, updatedBy);
				remotePS.setInt(++count, portNodeId);
				remotePS.setInt(++count, gcvLow);
				remotePS.setInt(++count, gcvHigh);
				remotePS.setString(++count, impurities);
				remotePS.setString(++count, sn);

				if (doTruncate < 1)
					remotePS.addBatch("truncate grade_details");
				remotePS.addBatch();
				doTruncate++;
			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("mines_details".equalsIgnoreCase(pair.second)) {
			query = "select id, name, sap_code, comments, status, created_on, created_by, updated_on, updated_by, port_node_id, supplier_id"
					+ ", challan_prefix, opstation_id, type, parent_mines, parent_sub_area, parent_area, parent_area_code, parent_sub_area_code"
					+ ", parent_mines_code, sn from grade_details";
			remoteQ = "insert into grade_details(id, name, sap_code, comments, status, created_on, created_by, updated_on, updated_by, port_node_id, supplier_id, challan_prefix, opstation_id, type, parent_mines, parent_sub_area, parent_area, parent_area_code, parent_sub_area_code, parent_mines_code, sn) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("name");
				String sapCode = rs.getString("sap_code");
				String comments = rs.getString("comments");
				String status = rs.getString("status");
				long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
				String createdBy = rs.getString("created_by");
				long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
				String updatedBy = rs.getString("updated_by");
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");
				int supplierId = Misc.getRsetInt(rs, "supplier_id");
				String challanPrefix = rs.getString("challan_prefix");
				int opstationId = Misc.getRsetInt(rs, "opstation_id");
				int type = Misc.getRsetInt(rs, "type");
				int parentMines = Misc.getRsetInt(rs, "parent_mines");
				int parentSubArea = Misc.getRsetInt(rs, "parent_sub_area");
				int parentArea = Misc.getRsetInt(rs, "parent_area");
				String parentAreaCode = rs.getString("parent_area_code");
				String parentSubAreaCode = rs.getString("parent_sub_area_code");
				String parentMinesCode = rs.getString("parent_mines_code");
				String sn = rs.getString("sn");

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, sapCode);
				remotePS.setString(++count, comments);
				remotePS.setString(++count, status);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
				remotePS.setString(++count, createdBy);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
				remotePS.setString(++count, updatedBy);
				remotePS.setInt(++count, portNodeId);
				remotePS.setInt(++count, supplierId);
				remotePS.setString(++count, challanPrefix);
				remotePS.setInt(++count, opstationId);
				remotePS.setInt(++count, type);
				remotePS.setInt(++count, parentMines);
				remotePS.setInt(++count, parentSubArea);
				remotePS.setInt(++count, parentArea);
				remotePS.setString(++count, parentAreaCode);
				remotePS.setString(++count, parentSubAreaCode);
				remotePS.setString(++count, parentMinesCode);
				remotePS.setString(++count, sn);

				if (doTruncate < 1)
					remotePS.addBatch("truncate mines_details");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("transporter_details".equalsIgnoreCase(pair.second)) {
			query = "select id, name, sap_code, comments, status, created_on, created_by, updated_on, updated_by, port_node_id, type"
					+ ", material_code, lr_prefix, supervisor, supervisor_mobile, supervisor_address, full_name, active_upto, active_from"
					+ ", material_cat, tare_freq from transporter_details";
			remoteQ = "insert into transporter_details(id, name, sap_code, comments, status, created_on, created_by, updated_on, updated_by, port_node_id, type"
					+ ", material_code, lr_prefix, supervisor, supervisor_mobile, supervisor_address, full_name, active_upto, active_from"
					+ ", material_cat, tare_freq) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("name");
				String sapCode = rs.getString("sap_code");
				String comments = rs.getString("comments");
				String status = rs.getString("status");
				long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
				String createdBy = rs.getString("created_by");
				long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
				String updatedBy = rs.getString("updated_by");
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");
				int type = Misc.getRsetInt(rs, "type");
				int materialCode = Misc.getRsetInt(rs, "material_code");
				String lrPrefix = rs.getString("lr_prefix");
				String supervisor = rs.getString("supervisor");
				String supervisorMobile = rs.getString("supervisor_mobile");
				String supervisorAddress = rs.getString("supervisor_address");
				String fullName = rs.getString("full_name");
				long activeUpto = Misc
						.sqlToLong(rs.getTimestamp("active_upto"));
				long activeFrom = Misc
						.sqlToLong(rs.getTimestamp("active_from"));
				int materialCat = Misc.getRsetInt(rs, "material_cat");
				int tareFreq = Misc.getRsetInt(rs, "tare_freq");

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, sapCode);
				remotePS.setString(++count, comments);
				remotePS.setString(++count, status);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(createdOn));
				remotePS.setString(++count, createdBy);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(updatedOn));
				remotePS.setString(++count, updatedBy);
				remotePS.setInt(++count, portNodeId);
				remotePS.setInt(++count, type);
				remotePS.setInt(++count, materialCode);
				remotePS.setString(++count, lrPrefix);
				remotePS.setString(++count, supervisor);
				remotePS.setString(++count, supervisorMobile);
				remotePS.setString(++count, supervisorAddress);
				remotePS.setString(++count, fullName);
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(activeUpto));
				remotePS.setTimestamp(++count, Misc.utilToSqlDate(activeFrom));
				remotePS.setInt(++count, materialCat);
				remotePS.setInt(++count, tareFreq);

				if (doTruncate < 1)
					remotePS.addBatch("truncate transporter_details");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("mines_do_details".equalsIgnoreCase(pair.second)) {
			query = "select id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person"
					+ ",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge"
					+ ",dump_charge,stow_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on"
					+ ",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1"
					+ ",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code from mines_do_details";
			remoteQ = "insert into mines_do_details(id, do_number,do_date,do_release_date,do_release_no,type_of_consumer,customer,customer_ref,customer_contact_person"
					+ ",grade,coal_size,source_mines,washery,qty_alloc,qty_already_lifted,quota,rate,transport_charge,sizing_charge,silo_charge"
					+ ",dump_charge,stow_charge,terminal_charge,forest_cess,stow_ed,avap,allow_no_tare,max_tare_gap,destination,status,created_on"
					+ ",updated_on,updated_by,port_node_id,created_by,grade_code,source_code,washery_code,destination_code,prefered_wb_1"
					+ ",prefered_wb_2,prefered_wb_3,prefered_wb_4,customer_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("do_number");
				long doDate = Misc.sqlToLong(rs.getTimestamp("do_date"));
				long doReleaseDate = Misc.sqlToLong(rs
						.getTimestamp("do_release_date"));
				String doReleaseNo = rs.getString("do_release_no");
				int typeOfConsumer = Misc.getRsetInt(rs, "type_of_consumer");
				int customer = Misc.getRsetInt(rs, "customer");
				String customerRef = rs.getString("customer_ref");
				String customerContactPerson = rs
						.getString("customer_contact_person");
				int grade = Misc.getRsetInt(rs, "grade");
				int coalSize = Misc.getRsetInt(rs, "coal_size");
				int sourceMines = Misc.getRsetInt(rs, "source_mines");
				int washery = Misc.getRsetInt(rs, "washery");
				double qtyAlloc = Misc.getRsetDouble(rs, "qty_alloc");
				double qtyAlreadyLifted = Misc.getRsetDouble(rs,
						"qty_already_lifted");
				double quota = Misc.getRsetDouble(rs, "quota");
				double rate = Misc.getRsetDouble(rs, "rate");
				double transportCharge = Misc.getRsetDouble(rs,
						"transport_charge");
				double sizingCharge = Misc.getRsetDouble(rs, "sizing_charge");
				double siloCharge = Misc.getRsetDouble(rs, "silo_charge");
				double dumpCharge = Misc.getRsetDouble(rs, "dump_charge");
				double stowCharge = Misc.getRsetDouble(rs, "stow_charge");
				double terminalCharge = Misc.getRsetDouble(rs,
						"terminal_charge");
				double forestCess = Misc.getRsetDouble(rs, "forest_cess");
				double stowEd = Misc.getRsetDouble(rs, "stow_ed");
				double avap = Misc.getRsetDouble(rs, "avap");
				int allowNoTare = Misc.getRsetInt(rs, "allow_no_tare");
				int maTareGap = Misc.getRsetInt(rs, "max_tare_gap");
				int destination = Misc.getRsetInt(rs, "destination");
				int status = Misc.getRsetInt(rs, "status");
				long createdOn = Misc.sqlToLong(rs.getTimestamp("created_on"));
				long updatedOn = Misc.sqlToLong(rs.getTimestamp("updated_on"));
				String updatedBy = rs.getString("updated_by");
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");
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
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(doReleaseDate));
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

				if (doTruncate < 1)
					remotePS.addBatch("truncate mines_do_details");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("vehicle".equalsIgnoreCase(pair.second)) {
			query = "select id,name,std_name,type,customer_id,tare,gross,rfid_epc,rfid_issue_date,last_epc,rfid_temp_status,flyash_tare,flyash_tare_time"
					+ ",unload_tare_time,unload_tare,load_tare_freq,unload_tare_freq,card_type,card_purpose,do_assigned,card_validity_type,card_init"
					+ ",card_init_date,card_expiary_date,prefered_mines,prefered_mines_code,prefered_driver,is_vehicle_on_gate from vehicle";
			remoteQ = "insert into vehicle(id,name,std_name,type,customer_id,tare,gross,rfid_epc,rfid_issue_date,last_epc,rfid_temp_status,flyash_tare,flyash_tare_time"
					+ ",unload_tare_time,unload_tare,load_tare_freq,unload_tare_freq,card_type,card_purpose,do_assigned,card_validity_type,card_init"
					+ ",card_init_date,card_expiary_date,prefered_mines,prefered_mines_code,prefered_driver,is_vehicle_on_gate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("name");
				String stdName = rs.getString("std_name");
				int customerId = Misc.getRsetInt(rs, "customer_id");
				double tare = Misc.getRsetDouble(rs, "tare");
				double gross = Misc.getRsetDouble(rs, "gross");
				String rfidEpc = rs.getString("rfid_epc");
				long rfidIssueDate = Misc.sqlToLong(rs
						.getTimestamp("rfid_issue_date"));
				String lastEpc = rs.getString("last_epc");
				int rfidTempStatus = Misc.getRsetInt(rs, "rfid_temp_status");
				double flyashTare = Misc.getRsetDouble(rs, "flyash_tare");
				long flyashTareTime = Misc.sqlToLong(rs
						.getTimestamp("flyash_tare_time"));
				long unloaTareTime = Misc.sqlToLong(rs
						.getTimestamp("unload_tare_time"));
				double unloadTare = Misc.getRsetDouble(rs, "unload_tare");
				int loadTareFreq = Misc.getRsetInt(rs, "load_tare_freq");
				int unloadTareFreq = Misc.getRsetInt(rs, "unload_tare_freq");
				int cardType = Misc.getRsetInt(rs, "card_type");
				int cardPurpose = Misc.getRsetInt(rs, "card_purpose");
				String doAssigned = rs.getString("do_assigned");
				int cardValidityType = Misc
						.getRsetInt(rs, "card_validity_type");
				int cardInit = Misc.getRsetInt(rs, "card_init");
				long cardInitDate = Misc.sqlToLong(rs
						.getTimestamp("card_init_date"));
				long cardExpiaryDate = Misc.sqlToLong(rs
						.getTimestamp("card_expiary_date"));
				int preferedMines = Misc.getRsetInt(rs, "prefered_mines");
				String preferedMinesCode = rs.getString("prefered_mines_code");
				int preferedDriver = Misc.getRsetInt(rs, "prefered_driver");
				int isVehicleOnGate = Misc.getRsetInt(rs, "is_vehicle_on_gate");

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, stdName);
				remotePS.setInt(++count, customerId);
				remotePS.setDouble(++count, tare);
				remotePS.setDouble(++count, gross);
				remotePS.setString(++count, rfidEpc);
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(rfidIssueDate));
				remotePS.setString(++count, lastEpc);
				remotePS.setInt(++count, rfidTempStatus);
				remotePS.setDouble(++count, flyashTare);
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(flyashTareTime));
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(unloaTareTime));
				remotePS.setDouble(++count, unloadTare);
				remotePS.setInt(++count, loadTareFreq);
				remotePS.setInt(++count, unloadTareFreq);
				remotePS.setInt(++count, cardType);
				remotePS.setInt(++count, cardPurpose);
				remotePS.setString(++count, doAssigned);
				remotePS.setInt(++count, cardValidityType);
				remotePS.setInt(++count, cardInit);
				remotePS
						.setTimestamp(++count, Misc.utilToSqlDate(cardInitDate));
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(cardExpiaryDate));
				remotePS.setInt(++count, preferedMines);
				remotePS.setString(++count, preferedMinesCode);
				remotePS.setInt(++count, preferedDriver);
				remotePS.setInt(++count, isVehicleOnGate);

				if (doTruncate < 1)
					remotePS.addBatch("truncate vehicle");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("vehicle_extended".equalsIgnoreCase(pair.second)) {
			query = "select vehicle_id,transporter_code,insurance_number,insurance_number_expiry,permit1_number,permit1_number_expiry from vehicle_extended";
			remoteQ = "insert into vehicle_extended(vehicle_id,transporter_code,insurance_number,insurance_number_expiry,permit1_number,permit1_number_expiry)"
					+ " values (?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "vehicle_id");
				String transporterCode = rs.getString("transporter_code");
				String insuranceNumber = rs.getString("insurance_number");
				long insuranceNumberExpiry = Misc.sqlToLong(rs
						.getTimestamp("insurance_number_expiry"));
				String permit1Number = rs.getString("permit1_number");
				long permit1NumberExpiry = Misc.sqlToLong(rs
						.getTimestamp("permit1_number_expiry"));

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, transporterCode);
				remotePS.setString(++count, insuranceNumber);
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(insuranceNumberExpiry));
				remotePS.setString(++count, permit1Number);
				remotePS.setTimestamp(++count, Misc
						.utilToSqlDate(permit1NumberExpiry));

				if (doTruncate < 1)
					remotePS.addBatch("truncate vehicle_extended");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		} else if ("driver_details".equalsIgnoreCase(pair.second)) {
			query = "select id,driver_name,driver_dl_number,driver_mobile_one,dl_expiry_date from driver_details";
			remoteQ = "insert into driver_details(id,driver_name,driver_dl_number,driver_mobile_one,dl_expiry_date) values (?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			remotePS = remoteConn.prepareStatement(remoteQ);
			doTruncate = 0;
			while (rs.next()) {

				int id = Misc.getRsetInt(rs, "id");
				String name = rs.getString("driver_name");
				String driverDlNumber = rs.getString("driver_dl_number");
				String driverMobileOne = rs.getString("driver_mobile_one");
				long dlExpiryDate = Misc.sqlToLong(rs
						.getTimestamp("dl_expiry_date"));

				int count = 0;
				remotePS.setInt(++count, id);
				remotePS.setString(++count, name);
				remotePS.setString(++count, driverDlNumber);
				remotePS.setString(++count, driverMobileOne);
				remotePS
						.setTimestamp(++count, Misc.utilToSqlDate(dlExpiryDate));

				if (doTruncate < 1)
					remotePS.addBatch("truncate driver_details");
				remotePS.addBatch();
				doTruncate++;

			}

			rs.close();
			ps.close();

			remotePS.executeBatch();
			remotePS.close();

			ps = conn
					.prepareStatement("update secl_ip_sync_details set status = 2 where id = ?");
			ps.setInt(1, pair.first.id);
			ps.executeUpdate();

			ps.close();
		}
	}

	private static Connection getConnection(String ip, String port, String db,
			String userName, String password) throws SQLException {
		// MySQL
		if (ip == null || port == null || db == null || "".equalsIgnoreCase(ip)
				|| "".equalsIgnoreCase(port) || "".equalsIgnoreCase(db))
			return null;
		String connectString = "jdbc:mysql://" + ip + ":" + port + "/" + db
				+ "?zeroDateTimeBehavior=convertToNull&traceProtocol=false";

		Connection retval = ((DriverManager.getConnection(connectString,
				userName, password)));

		return retval;

	}

	private static void returnConnection(Connection retConn, boolean destroyIt) {
		if (retConn == null)
			return;

		boolean isClosed = false;
		boolean toCreateNew = false;
		try {
			isClosed = retConn.isClosed();
			com.mysql.jdbc.Connection mysqlConn = (com.mysql.jdbc.Connection) retConn;
			if (mysqlConn.getActiveStatementCount() > 0) {
				System.out
						.println("[MasterSync] connection being returned has open statements:"
								+ retConn.hashCode()
								+ " cnt:"
								+ mysqlConn.getActiveStatementCount());
				destroyIt = true;
			}
		} catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		// if not already closed - rollback or commit as needed
		if (!isClosed) {
			try {
				if (!retConn.getAutoCommit()) {
					if (destroyIt) {
						retConn.rollback();

					}// if to destroy
					else {
						retConn.commit();
					}// if to commit
				}// was not autco
			} catch (Exception e) {
				destroyIt = true;
				e.printStackTrace();
			}
		}
		if(retConn!=null)
			try {
				retConn.close();
				retConn=null;
			} catch (SQLException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public static void main(String[] args) {
		MasterSync ms = new MasterSync();
		System.out.println("[MasterSyncTimerTask] : beginSync start ");
		Connection conn = null;

		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			System.out
					.println("[MasterSyncTimerTask] : beginSync updateIPSyncDetails start ");
			boolean hasAnyChange = MasterSync.updateIPSyncDetails(conn);
			System.out
					.println("[MasterSyncTimerTask] : beginSync updateIPSyncDetails end ");
			// if(hasAnyChange){
			System.out
					.println("[MasterSyncTimerTask] : beginSync updateRemoteServers start ");
			MasterSync.updateRemoteServers(conn);
			System.out
					.println("[MasterSyncTimerTask] : beginSync updateRemoteServers end ");
			// }

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
		System.out.println("[MasterSyncTimerTask] : beginSync end ");
	}
}

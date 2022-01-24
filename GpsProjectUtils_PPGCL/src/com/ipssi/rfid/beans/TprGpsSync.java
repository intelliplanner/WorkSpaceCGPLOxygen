package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.db.DBSchemaManager;
import com.ipssi.rfid.db.DBSchemaManager.Table;

public class TprGpsSync {
	
	public static String minesQ = "insert into mines_details(id, name, sap_code, status, created_on, created_by, updated_on, updated_by" +
									", port_node_id, type, parent_mines_code, parent_area_code" +
									", parent_sub_area_code, sn" +
									", address,tin_number,central_excise_reg_no,central_excise_goods,assessee,cst_no,vat_no,project_name" +
									",address_range,address_division" +
									", commissionerate,dmf_rate,nmet_rate,excise_duty_rate,education_cess_rate,higher_education_cess_rate,area_code " +
									") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key " +
									" update id=?, name=?, sap_code=?, status=?, created_on=?, created_by=?, updated_on=?, updated_by=?" +
									", port_node_id=?, type=?, parent_mines_code=?, parent_area_code=?" +
									", parent_sub_area_code=?, sn=?" +
									", address=?,tin_number=?,central_excise_reg_no=?,central_excise_goods=?,assessee=?,cst_no=?,vat_no=?" +
									",project_name=?,address_range=?,address_division=?" +
									", commissionerate=?,dmf_rate=?,nmet_rate=?,excise_duty_rate=?,education_cess_rate=?,higher_education_cess_rate=?" +
									",area_code=?" ;
	public static String vehicleUpdQ = "update secl_area_sync_details set vehicle_updated_on = ? where id = ?";
	public static String minesUpdQ = "update secl_area_sync_details set mines_updated_on = ? where id = ?";
	public static String gradeUpdQ = "update secl_area_sync_details set grade_updated_on = ? where id = ?";
	public static String productUpdQ = "update secl_area_sync_details set product_updated_on = ? where id = ?";
	public static String tprUpdQ = "update secl_area_sync_details set tpr_updated_on = ? where id = ?";
	public static String doUpdQ = "update secl_area_sync_details set do_updated_on = ? where id = ?";
	public static String currDOUpdQ = "update secl_area_sync_details set curr_do_updated_on = ? where id = ?";
	public static String currDOAppUpdQ = "update secl_area_sync_details set curr_do_app_updated_on = ? where id = ?";
	public static String tprInvoiceUpdQ = "update secl_area_sync_details set tpr_invoice_updated_on = ? where id = ? ";
	
	
	public static ArrayList <RemoteCredential> loadAreaCredentials(Connection conn){

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList <RemoteCredential> retVal = new ArrayList <RemoteCredential>();
		System.out.println("[TprGpsSync].[loadAreaCredentials] Start");

		String query = "select id,ip,port,db,user_id,password,prefix,tpr_updated_on,mines_updated_on,grade_updated_on,product_updated_on" +
				",do_updated_on,curr_do_updated_on,curr_do_app_updated_on,vehicle_updated_on" +
		" from secl_area_sync_details where status = 1" ;

		try{
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()){
				RemoteCredential uCred = null;
				String ip = rs.getString("ip");
				//						if(Utils.isMyIp(ip))
				//							continue;
				int id = Misc.getRsetInt(rs, "id");
				String port = rs.getString("port");
				String db = rs.getString("db");
				String userId = rs.getString("user_id");
				String password = rs.getString("password");
				String prefix = rs.getString("prefix");
				long tprUpdatedOn = Misc.sqlToLong(rs.getTimestamp("tpr_updated_on"));
				long minesUpdatedOn = Misc.sqlToLong(rs.getTimestamp("mines_updated_on"));
				long gradeUpdatedOn = Misc.sqlToLong(rs.getTimestamp("grade_updated_on"));
				long productUpdatedOn = Misc.sqlToLong(rs.getTimestamp("product_updated_on"));
				long doUpdatedOn = Misc.sqlToLong(rs.getTimestamp("do_updated_on"));
				long currDoUpdatedOn = Misc.sqlToLong(rs.getTimestamp("curr_do_updated_on"));
				long currDoAppUpdatedOn = Misc.sqlToLong(rs.getTimestamp("curr_do_app_updated_on"));
				long vehicleUpdatedOn = Misc.sqlToLong(rs.getTimestamp("vehicle_updated_on"));
				long tprInvoiceUpdatedOn = Misc.sqlToLong(rs.getTimestamp("tpr_invoice_updated_on"));
				
				uCred = new RemoteCredential(id,ip,port,db,userId,password,prefix,tprUpdatedOn,minesUpdatedOn,gradeUpdatedOn,productUpdatedOn
						,doUpdatedOn,currDoUpdatedOn,currDoAppUpdatedOn,vehicleUpdatedOn,tprInvoiceUpdatedOn);
				retVal.add(uCred);
			}
			Misc.closeRS(rs);
			Misc.closePS(ps);

		}catch(Exception e){
			//					retVal.add(new Pair(wbId, FAIL));
			System.out.println("[TprGpsSync].[loadAreaCredentials] Error while getting list of remote system list: "+
					"[Thread:"+Thread.currentThread().getId()+"] dbconn: "+conn);
			e.printStackTrace();
		}
		System.out.println("[TprGpsSync].[loadAreaCredentials] retVal: "+ retVal);
		System.out.println("[TprGpsSync].[loadAreaCredentials] End");

		return retVal;
	}
	 public static java.text.SimpleDateFormat sqlFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 public static String getSQLDateStr(java.util.Date date) {
	     if (date == null) return "";
	     return sqlFormatter.format(date);
	     //return (Integer.toString(date.getMonth()+1) + "/" + Integer.toString(date.getDate()) + "/" + Integer.toString(date.getYear()+1900));
	  }

	 public static ArrayList<Pair<Boolean, Integer>>  migrateTPRsToGPS(Connection conn, RemoteCredential uCred) throws Exception{

		 ArrayList<Pair<Boolean, Integer>> retVal = new ArrayList<Pair<Boolean, Integer>>();

		 if(uCred != null){
			 Connection remoteConn = null;
			 PreparedStatement remotePS = null;
			 ResultSet remoteRS = null;
			 PreparedStatement ps = null;
			 ResultSet rs = null;
			 boolean destroyRemote = false;
			 PreparedStatement psAreaCode = null;
			 ResultSet rsAreaCode = null;
			 HashMap<String, Integer> areaPortMap = new HashMap<String, Integer>();
			 try{
				 // arae_code vs port_node_id mapping
				 psAreaCode = conn.prepareStatement("select area_code, port_node_id from secl_area_port_mapping");
				 rsAreaCode = psAreaCode.executeQuery();
				 while (rsAreaCode.next()) {
					 areaPortMap.put(rsAreaCode.getString("area_code"), Misc.getRsetInt(rsAreaCode, "port_node_id"));
				 }
				 Misc.closeRS(rsAreaCode);
				 Misc.closePS(psAreaCode); 
				 int portNodeId = Misc.getUndefInt();
				 if(areaPortMap.size() > 0)
					 portNodeId = areaPortMap.get(uCred.prefix);
				 remoteConn = getConnection(uCred.ip, uCred.port, uCred.db, uCred.userId, uCred.password);

				 String schema = "ipssi_secl";
				 String updOn = "";
				 String tableName = "";
				 StringBuilder query = null;
				 Table fromTable  = null;
				 Table toTable  = null;
				 long updOnArea = Misc.getUndefInt();
				 // vehicle : check against std_name, if vehicle name exists then check cust_id and if not matched then update vehicle_access_group
				 try{
					 tableName = "vehicle";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(vehicleUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.vehicleUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.updateVehicles(remoteConn, conn, fromTable, toTable, query, uCred.prefix, portNodeId);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 // mines_details
				 try{
					 tableName = "mines_details";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(minesUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.minesUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 // current_do_status
				 try{
					 tableName = "current_do_status";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(currDOUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.currDoUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 // current_do_status_apprvd
				 try{
					 tableName = "current_do_status_apprvd";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(currDOAppUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.currDoAppUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 // Mines_do_details
				 try{
					 tableName = "mines_do_details";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(doUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.doUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 // tp_record
				 try{
					 tableName = "tp_record";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(tprUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.tprUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 
				 // secl_tpr_invoice
				 try{
					 tableName = "secl_tpr_invoice";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(tprInvoiceUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.tprInvoiceUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);
					 if(!Misc.isUndef(updOnArea)){
						 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
						 Misc.setParamInt(ps, uCred.id,2);
						 ps.executeUpdate();
					 }
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
				 
				 //grade_details
				 /*try{
					 tableName = "grade_details";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(gradeUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.gradeUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);

					 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
					 Misc.setParamInt(ps, uCred.id,2);
					 ps.executeUpdate();
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }*/
				 //coal_product
				 /*try{
					 tableName = "coal_product";
					 System.out.println("TprGpsSync.migrateTPRsToGPS() tableName: "+tableName + "uCred: " + uCred);
					 ps = conn.prepareStatement(productUpdQ);
					 updOn = getSQLDateStr(new Date(uCred.productUpdatedOn));
					 //query = new StringBuilder("select tp_record.* from tp_record where updated_on >= '").append(updOn).append("'");
					 query = new StringBuilder(" t0.updated_on >= '").append(updOn).append("'");
					 fromTable  = DBSchemaManager.getTable(remoteConn,tableName,uCred.db);
					 toTable  = DBSchemaManager.getTable(conn,tableName,schema);
					 updOnArea = DBSchemaManager.copyDataInsertIgnoreUpdate(remoteConn, conn, fromTable, toTable, query, uCred.prefix);

					 ps.setTimestamp(1, Misc.utilToSqlDate(updOnArea));
					 Misc.setParamInt(ps, uCred.id,2);
					 ps.executeUpdate();
					 Misc.closePS(ps);
					 conn.commit();
				 }catch(Exception e){
					 e.printStackTrace();
				 }*/

				 //				String updOn = getSQLDateStr(new Date(uCred.minesUpdatedOn));
				 /*StringBuilder query = new StringBuilder("select mines_details.* from mines_details where updated_on >= '").append(updOn).append("'");

				Pair<java.sql.Timestamp,ArrayList<Mines>> minesListPair = DBSchemaManager.getList(conn, Mines.class, schema, query);
				Date updatedOn = new Date(1L);
				if(minesListPair != null && minesListPair.second != null && minesListPair.second.size() > 0){
					System.out.println("[TprGpsSync][migrateTPRsToGPS] tprListPair size: "+minesListPair.second.size()+"[Thread:"
					+Thread.currentThread().getId()+"] uCred: "+uCred+" remoteConn: "+remoteConn);
					remotePS = remoteConn.prepareStatement(minesQ);
//					remotePS = remoteConn.prepareStatement("insert into mines_details(id, name, sap_code,area_code,updated_on) values (?,?,?,?,?)
				  *  on duplicate key update name=?, sap_code=?, area_code=?, updated_on=?");
					ps = conn.prepareStatement(minesUpdQ);
					for (int i = 0,is=minesListPair == null || minesListPair.second == null ? 0 : minesListPair.second.size(); i < is; i++) {
						Mines mines = minesListPair.second.get(i);

						int count = 0;
						Misc.setParamInt(remotePS, mines.getId(),++count);
						remotePS.setString(++count, mines.getName());
						remotePS.setString(++count, mines.getSapCode());
						Misc.setParamInt(remotePS, mines.getStatus(),++count);
						remotePS.setTimestamp(++count, Misc.utilToSqlDate(mines.getCreatedOn()));
						Misc.setParamInt(remotePS, mines.getCreatedBy(),++count);
						remotePS.setTimestamp(++count, Misc.utilToSqlDate(mines.getUpdatedOn()));
						Misc.setParamInt(remotePS, mines.getUpdatedBy(),++count);
						Misc.setParamInt(remotePS, mines.getPortNodeId(),++count);
						Misc.setParamInt(remotePS, mines.getType(),++count);
						remotePS.setString(++count, mines.getParentMinesCode());
						remotePS.setString(++count, mines.getParentAreaCode());
						remotePS.setString(++count, mines.getParentSubAreaCode());
						remotePS.setString(++count, mines.getCode());
						remotePS.setString(++count, mines.getAddress());
						remotePS.setString(++count, mines.getTinNumber());
						remotePS.setString(++count, mines.getCentralExciseRegNo());
						remotePS.setString(++count, mines.getCentralExciseGoods());
						remotePS.setString(++count, mines.getAssessee());
						remotePS.setString(++count, mines.getCstNo());
						remotePS.setString(++count, mines.getVatNo());
						remotePS.setString(++count, mines.getProjectName());
						remotePS.setString(++count, mines.getAddressRange());
						remotePS.setString(++count, mines.getAddressDivision());
						remotePS.setString(++count, mines.getCommissionerate());
						Misc.setParamDouble(remotePS, mines.getDmfRate(),++count);
						Misc.setParamDouble(remotePS, mines.getNmetRate(),++count);
						Misc.setParamDouble(remotePS, mines.getExciseDutyRate(),++count);
						Misc.setParamDouble(remotePS, mines.getEducationCessRate(),++count);
						Misc.setParamDouble(remotePS, mines.getHigherEducationCessRate(),++count);
						remotePS.setString(++count, uCred.prefix);// TBD

						Misc.setParamInt(remotePS, mines.getId(),++count);
						remotePS.setString(++count, mines.getName());
						remotePS.setString(++count, mines.getSapCode());
						Misc.setParamInt(remotePS, mines.getStatus(),++count);
						remotePS.setTimestamp(++count, Misc.utilToSqlDate(mines.getCreatedOn()));
						Misc.setParamInt(remotePS, mines.getCreatedBy(),++count);
						remotePS.setTimestamp(++count, Misc.utilToSqlDate(mines.getUpdatedOn()));
						Misc.setParamInt(remotePS, mines.getUpdatedBy(),++count);
						Misc.setParamInt(remotePS, mines.getPortNodeId(),++count);
						Misc.setParamInt(remotePS, mines.getType(),++count);
						remotePS.setString(++count, mines.getParentMinesCode());
						remotePS.setString(++count, mines.getParentAreaCode());
						remotePS.setString(++count, mines.getParentSubAreaCode());
						remotePS.setString(++count, mines.getCode());
						remotePS.setString(++count, mines.getAddress());
						remotePS.setString(++count, mines.getTinNumber());
						remotePS.setString(++count, mines.getCentralExciseRegNo());
						remotePS.setString(++count, mines.getCentralExciseGoods());
						remotePS.setString(++count, mines.getAssessee());
						remotePS.setString(++count, mines.getCstNo());
						remotePS.setString(++count, mines.getVatNo());
						remotePS.setString(++count, mines.getProjectName());
						remotePS.setString(++count, mines.getAddressRange());
						remotePS.setString(++count, mines.getAddressDivision());
						remotePS.setString(++count, mines.getCommissionerate());
						Misc.setParamDouble(remotePS, mines.getDmfRate(),++count);
						Misc.setParamDouble(remotePS, mines.getNmetRate(),++count);
						Misc.setParamDouble(remotePS, mines.getExciseDutyRate(),++count);
						Misc.setParamDouble(remotePS, mines.getEducationCessRate(),++count);
						Misc.setParamDouble(remotePS, mines.getHigherEducationCessRate(),++count);
						remotePS.setString(++count, uCred.prefix);// TBD

						updatedOn = mines.getUpdatedOn() != null ? (updatedOn.after(mines.getUpdatedOn()) ? updatedOn : mines.getUpdatedOn()) :
						 updatedOn;

						remotePS.addBatch();
					}
				}else{
					System.out.println("[TprGpsSync][migrateTPRsToGPS] tprListPair is null: "+"[Thread:"+Thread.currentThread().getId()+"] uCred: 
					"+uCred+" remoteConn: "+remoteConn);
				}
				remotePS.executeBatch();
				Misc.closePS(remotePS);
				remoteConn.commit();

				ps.setTimestamp(1, Misc.utilToSqlDate(updatedOn));
				Misc.setParamInt(ps, uCred.id,2);
				ps.executeUpdate();
				Misc.closePS(ps);
				conn.commit();*/
				 //Misc.setParamDate(ps, 1, Misc.utilToSqlDate(updatedOn.getTime()));

			 }catch (Exception e){
				 // updateStatus(conn,uCred.id,3);
				 destroyRemote = true;
				 System.out.println("[TprGpsSync][copyTprToWorkStation] Error while updating remote machine: "
						 +"[Thread:"+Thread.currentThread().getId()+"] uCred: "+uCred+" remoteConn: "+remoteConn);
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


		 return retVal;
	 }

	public static class RemoteCredential{
		private int id = Misc.getUndefInt(); 
		private String ip = null;
		private String port = null;
		private String db = null;
		private String userId = null;
		private String password = null;
		private String prefix = null;
		private long tprUpdatedOn = Misc.getUndefInt(); 
		private long minesUpdatedOn = Misc.getUndefInt();
		private long gradeUpdatedOn = Misc.getUndefInt();
		private long productUpdatedOn = Misc.getUndefInt();
		private long doUpdatedOn = Misc.getUndefInt();
		private long currDoUpdatedOn = Misc.getUndefInt();
		private long currDoAppUpdatedOn = Misc.getUndefInt();
		private long vehicleUpdatedOn = Misc.getUndefInt();
		private long tprInvoiceUpdatedOn = Misc.getUndefInt();
		

		public RemoteCredential(int id,String ip, String port, String db, String userId, String password, String prefix,long tprUpdatedOn
				,long minesUpdatedOn,long gradeUpdatedOn,long productUpdatedOn,long doUpdatedOn,long currDoUpdatedOn,long currDoAppUpdatedOn
				,long vehicleUpdatedOn,long tprInvoiceUpdatedOn){
			this.id=id;
			this.ip = ip;
			this.port = port;
			this.db = db;
			this.userId = userId;
			this.password = password;
			this.prefix = prefix;
			this.tprUpdatedOn = tprUpdatedOn;
			this.minesUpdatedOn = minesUpdatedOn;
			this.gradeUpdatedOn = gradeUpdatedOn;
			this.productUpdatedOn = productUpdatedOn;
			this.doUpdatedOn = doUpdatedOn;
			this.currDoUpdatedOn = currDoUpdatedOn;
			this.currDoAppUpdatedOn = currDoAppUpdatedOn;
			this.vehicleUpdatedOn = vehicleUpdatedOn;
			this.tprInvoiceUpdatedOn = tprInvoiceUpdatedOn;
		}
		@Override
		public String toString() {
			return super.toString()+" id:"+id+" ip:"+ip+" port:"+port+" db:"+db+" userId:"+userId+" password:"+password+" prefix:"+prefix
			+" tprUpdatedOn:"+new Date(tprUpdatedOn)+" minesUpdatedOn:"+new Date(minesUpdatedOn)+" gradeUpdatedOn:"+new Date(gradeUpdatedOn)
			+" productUpdatedOn:"+new Date(productUpdatedOn)+" doUpdatedOn:"+new Date(doUpdatedOn)+" currDoUpdatedOn:"+new Date(currDoUpdatedOn)
			+" currDoAppUpdatedOn:"+new Date(currDoAppUpdatedOn)+" vehicleUpdatedOn:"+new Date(vehicleUpdatedOn);
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
				System.out.println("[TprGpsSync] connection being returned has open statements:"+retConn.hashCode()+" cnt:"
						+mysqlConn.getActiveStatementCount());
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
		TprGpsSync ms = new TprGpsSync();
		System.out.println("[TprGpsSync] : beginSync start ");
		Connection conn = null;
		
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			System.out.println("[TprGpsSync] : beginSync updateIPSyncDetails start ");
			ArrayList <RemoteCredential> rCredList =  TprGpsSync.loadAreaCredentials(conn);
			
			for (Iterator iterator = rCredList.iterator(); iterator.hasNext();) {
				RemoteCredential remoteCredential = (RemoteCredential) iterator.next();
				try{
				System.out.println("[TprGpsSync] : beginSync migrateTPRsToGPS start remoteCredential: "+remoteCredential);
				TprGpsSync.migrateTPRsToGPS(conn,remoteCredential);
				System.out.println("[TprGpsSync] : beginSync migrateTPRsToGPS end ");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
							

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
		System.out.println("[TprGpsSync] : beginSync end ");
	}
}

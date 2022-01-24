package com.ipssi.rfid.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.db.DBSchemaManager.Trigger.ActionOrientation;
import com.ipssi.rfid.db.DBSchemaManager.Trigger.ActionTiming;
import com.ipssi.rfid.db.DBSchemaManager.Trigger.EventManipulation;
import com.ipssi.rfid.db.Table.Column;

public class DBSchemaManager {
	public static final String TEMP_REMOTE_ID_COLUMN = "temp_remote_id";
	public static final String RECORD_SRC = "record_src";
	public static final ArrayList<TableInfo> tableToBeSync = new ArrayList<TableInfo>();
	private static final String[] vehicleRfFields = new String[]{"tare", "gross", "rfid_epc", "rfid_issue_date", "flyash_tare", "flyash_tare_time", "last_epc", "rfid_temp_status", "unload_tare_time", "unload_tare", "load_tare_freq", "unload_tare_freq", "card_type", "card_purpose", "do_assigned", "card_validity_type", "card_expiary_date", "card_init", "prefered_driver", "rfid_info_id", "card_init_date", "is_vehicle_on_gate", "prefered_mines_code", "last_tare_tpr", "min_tare", "min_gross"};
	static {
		tableToBeSync.add(new TableInfo("mines_do_details", "do_number", "do_id", null));
		tableToBeSync.add(new TableInfo("mines_details", "sn", "mines_code", null));
		tableToBeSync.add(new TableInfo("transporter_details", "sn", "mines_code", null));
		tableToBeSync.add(new TableInfo("grade_details", "sn", "grade_code", null));
		tableToBeSync.add(new TableInfo("customer_details", "sn", "customer_code", null));
		tableToBeSync.add(new TableInfo("destination_details", "sn", "destination_code", null));
		tableToBeSync.add(new TableInfo("coal_product", "sn", "product_code", null));
		//		tableToBeSync.add(new TableInfo("driver_details", "id", "driver_id", null));
		/*tableToBeSync.add(new TableInfo("secl_workstation_profile", "id", "workstation_profile_id", null));
		tableToBeSync.add(new TableInfo("vehicle", "id", "vehicle_id", 
				new ArrayList<>(Arrays.asList(
						new TableInfo("vehicle_rfid_info", "vehicle_id", "id", null)
						,new TableInfo("vehicle_extended", "vehicle_id", "id", null)
						))));
		tableToBeSync.add(new TableInfo("secl_workstation_details", "id", "workstation_id", 
				new ArrayList<>(Arrays.asList(
						new TableInfo("secl_workstation_destination_group", "workstation_id", "id", null),
						new TableInfo("secl_workstation_mines_group", "workstation_id", "id", null)
						))));*/
	}
	public static void main(String args[]) throws Exception {
		Connection conn = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			Table fromTable = getTable(conn, "tp_record", "ipssi_secl");
			Table toTable = getTable(conn, "tp_record_export", "ipssi_secl");
			copyDataInsertIgnoreUpdate(conn, conn, fromTable, toTable, null ,"gevra");
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}
	}
	public static class TableInfo{
		private String tableName;
		private String primaryColumnName;
		private String refColumnName;
		ArrayList<TableInfo> nestedTables;
		public static enum SyncScheme {
			upStream,
			downStream,
			upStreamThenDownStream,
			downStreamThenUpStrem
		}
		private SyncScheme syncScheme;

		public SyncScheme getSyncScheme() {
			return syncScheme;
		}
		public void setSyncScheme(SyncScheme syncScheme) {
			this.syncScheme = syncScheme;
		}
		public String getTableName() {
			return tableName;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		public String getPrimaryColumnName() {
			return primaryColumnName;
		}
		public void setPrimaryColumnName(String primaryColumnName) {
			this.primaryColumnName = primaryColumnName;
		}
		public String getRefColumnName() {
			return refColumnName;
		}
		public void setRefColumnName(String refColumnName) {
			this.refColumnName = refColumnName;
		}
		public ArrayList<TableInfo> getNestedTables() {
			return nestedTables;
		}
		public void setNestedTables(ArrayList<TableInfo> nestedTables) {
			this.nestedTables = nestedTables;
		}
		public TableInfo() {
			super();
		}
		public TableInfo(String tableName, String primaryColumnName, String refColumnName,
				ArrayList<TableInfo> nestedTables) {
			super();
			this.tableName = tableName;
			this.primaryColumnName = primaryColumnName;
			this.refColumnName = refColumnName;
			this.nestedTables = nestedTables;
		}
	}

	public static void syncDB(Connection fromConn,Connection toConn, String fromSchema, String toSchema,int workstationIdOnServer) throws Exception{
		Table dataSyncTable = getTable(fromConn, Table.dataTableSyncStatus, fromSchema);
		if(dataSyncTable == null){
			createTable(fromConn, Table.getSyncStatusSchema());
		}
		dataSyncTable = getTable(toConn, Table.dataTableSyncStatus, toSchema);
		if(dataSyncTable == null){
			createTable(toConn, Table.getSyncStatusSchema());
		}
		for(TableInfo tableInfo : tableToBeSync){
			syncTable(fromConn, toConn, tableInfo, fromSchema, toSchema,workstationIdOnServer,true,null);
			toConn.commit();
		}
	}
	public static void syncTable(Connection fromConn,Connection toConn, TableInfo tableInfo, String fromSchema,String toSchema, int workstionIdOnServer, boolean useReadWriteTs ,Pair<ArrayList<Pair<Integer, Integer>>, Timestamp> keysList) throws Exception{
		System.out.println("[DB][SYNC]:"+tableInfo.getTableName()+",["+tableInfo.getNestedTables()+"]");
		Table fromTable = null;
		Table toTable = null;
		Pair<Table,Table> syncPair = handleSchemaChanges(fromConn, toConn, fromSchema, toSchema, tableInfo.getTableName(),false);
		if(syncPair == null || syncPair.first == null || syncPair.second == null)
			return;
		fromTable = syncPair.first;
		toTable = syncPair.second;
		Pair<Timestamp, Timestamp> readWriteTs = useReadWriteTs ? getLastReadWriteTimeStamp(toConn, tableInfo.getTableName()) : null;
		if(tableInfo.getTableName().equalsIgnoreCase("vehicle1")){
			//get last writeTimestamp
			ArrayList<DBColumn> rfFields = null;
			ArrayList<DBColumn> allFields = toTable.getColums();
			for (int i = 0; i < vehicleRfFields.length; i++) {
				for (int j = 0, js = toTable.getColums() == null ? 0 : toTable.getColums().size(); j < js; j++) {
					if(vehicleRfFields[i].equalsIgnoreCase(toTable.getColums().get(j).getName())){
						if(rfFields == null)
							rfFields = new ArrayList<DBSchemaManager.DBColumn>();
						rfFields.add(toTable.getColums().get(j));
					}
				}
			}
			fromTable.setColums(rfFields);
			toTable.setColums(rfFields);
			keysList =  copyData(fromConn, toConn, fromTable, toTable, null,toTable.getColumnByName(tableInfo.getPrimaryColumnName()),null,null,readWriteTs != null ? readWriteTs.first : null,workstionIdOnServer);
		}else if(tableInfo.getTableName().equalsIgnoreCase("mines_do_details1")){

		}else if(tableInfo.getTableName().equalsIgnoreCase("secl_workstion_details1")){
			//send data to server
		}else{
			//copy data from server
			keysList =  copyData(fromConn, toConn, fromTable, toTable, keysList == null ? null :  keysList.first,toTable.getColumnByName(tableInfo.getPrimaryColumnName()),null,null,readWriteTs != null ? readWriteTs.first : null,workstionIdOnServer);
			for (int i = 0,is=tableInfo.getNestedTables() == null ? 0 : tableInfo.getNestedTables().size(); i < is; i++) {
				syncPair = handleSchemaChanges(fromConn, toConn, fromSchema, toSchema, tableInfo.getNestedTables().get(i).getTableName(),true);
				if(syncPair == null || syncPair.first == null || syncPair.second == null)
					return;
				copyData(fromConn, toConn, syncPair.first, syncPair.second, keysList.first, syncPair.second.getColumnByName(tableInfo.getNestedTables().get(i).getPrimaryColumnName()) ,toTable.getColumnByName(tableInfo.getNestedTables().get(i).getRefColumnName()),toTable,readWriteTs != null ? readWriteTs.first : null,workstionIdOnServer);
			}
			if(useReadWriteTs)
				updateLastReadWriteTimestamp(toConn, tableInfo.getTableName(), keysList != null ? keysList.second : null, null);
		}


	}
	public static Pair<Timestamp,Timestamp> getLastReadWriteTimeStamp(Connection conn,String tableName) throws SQLException{
		Pair<Timestamp,Timestamp> retval = null;
		PreparedStatement ps = conn.prepareStatement(getSelectStatment(Table.getSyncStatusSchema().getColums(), Table.dataTableSyncStatus).first.toString()+" where table_name like ?");
		ps.setString(1, tableName);
		ResultSet rs = ps.executeQuery();
		if(rs.next()){
			retval = new Pair<Timestamp, Timestamp>(rs.getTimestamp(2),rs.getTimestamp(3)); 
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
		return retval;
	}
	public static void updateLastReadWriteTimestamp(Connection conn,String tableName,Timestamp readTs, Timestamp writeTs) throws SQLException{
		PreparedStatement ps = null;
		Pair<Timestamp,Timestamp> lastReadWrite = getLastReadWriteTimeStamp(conn, tableName); 
		if(lastReadWrite != null){
			if(readTs == null)
				readTs = lastReadWrite.first;
			if(writeTs == null)
				writeTs = lastReadWrite.second;
		}
		ps = lastReadWrite != null ? 
				conn.prepareStatement("update "+Table.dataTableSyncStatus+" set last_read_at=?, last_write_on=? where table_name=?") :
					conn.prepareStatement("insert into "+Table.dataTableSyncStatus+" (last_read_at,last_write_on,table_name) values (?,?,?)");
				setParam(Table.getSyncStatusSchema().getColumnByName("last_read_at"), ps, 1, readTs);
				setParam(Table.getSyncStatusSchema().getColumnByName("last_read_at"), ps, 2, writeTs);
				setParam(Table.getSyncStatusSchema().getColumnByName("table_name"), ps, 3, tableName);
				ps.executeUpdate();
				Misc.closePS(ps);
	}
	public static ArrayList<DBColumn> getColumnInfo(Connection conn, String tableName) throws Exception {
		if(tableName == null || tableName.length() <= 0)
			return null;
		ArrayList<DBColumn> colList = null;
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet resultSet = meta.getIndexInfo(null, null, tableName, true, false);//.getColumns(schema, null, tableName, "%"); 
		Set<String> uniqueColumn = null;
		while (resultSet.next()) {
			if(resultSet.getBoolean("NON_UNIQUE") == true)
				continue;
			if(uniqueColumn == null){
				uniqueColumn = new HashSet<String>();
			}
			uniqueColumn.add(resultSet.getString("COLUMN_NAME").toLowerCase());
		}
		resultSet = meta.getColumns(null, null, tableName, "%");
		while (resultSet.next()) {
			if(colList == null)
				colList = new ArrayList<DBColumn>();
			String columnName = resultSet.getString("COLUMN_NAME");
			ColumnType columnType = ColumnType.valueOf(resultSet.getString("TYPE_NAME"));
			int columnSize = Misc.getRsetInt(resultSet, "COLUMN_SIZE");
			boolean isGenrated = resultSet.getBoolean("IS_AUTOINCREMENT");
			boolean isUnique = uniqueColumn != null && uniqueColumn.contains(columnName);
			int dataType = Misc.getRsetInt(resultSet, "DATA_TYPE");
			colList.add(new DBColumn(columnName, columnType, columnSize,isGenrated,isUnique,dataType,tableName));
		}
		if(colList != null)
			System.out.println(colList);
		return colList;
	}
	public static void createTable(Connection conn,ArrayList<DBColumn> colList,String tableName) throws SQLException{
		if(conn == null || colList == null || colList.size() <= 0 || tableName == null ||tableName.length() <= 0)
			return;
		StringBuilder fields = null;
		ArrayList<DBColumn> uniqueColumns = null;
		for (int i = 0; i < colList.size(); i++) {
			if(fields == null){
				fields = new StringBuilder();
			}else{
				fields.append(", ");
			}
			if(!colList.get(i).isGenerated() && colList.get(i).isUnique()){
				if(uniqueColumns == null)
					uniqueColumns = new ArrayList<DBColumn>();
				uniqueColumns.add(colList.get(i));
			}
			fields.append(colList.get(i).getName()+" "+colList.get(i).getType()+(Misc.isUndef(colList.get(i).getLength()) ? "" : "("+colList.get(i).getLength()+") ")+(colList.get(i).isGenerated() ? " primary key auto_increment " : ""));// +(colList.get(i).isGenerated() || !colList.get(i).isUnique() ? "" : ""  ));
		}
		if(fields == null)
			return;
		String query = "create table "+ tableName + "(" + fields.toString() + ")";
		System.out.println(query);
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
		/*for (int i = 0,is = uniqueColumns == null ? 0 : uniqueColumns.size(); i < is; i++) {
			query = "alter table "+tableName+" add constraint uc_"+tableName+uniqueColumns.get(i).getName()+" UNIQUE ("+uniqueColumns.get(i).getName()+")";
			System.out.println(query);
			st = conn.createStatement();
			st.execute(query);
			st.close();
		}*/
	}
	public static void createTable(Connection conn,Table table) throws SQLException{
		if(table == null)
			return;
		createTable(conn, table.getColums(),table.getName());
		//create unique columns
		for (int i = 0,is=table.getUniqueKeys() == null ? 0 : table.getUniqueKeys().size(); i < is; i++) {
			createUniqueKey(conn, table.getUniqueKeys().get(i));
		}
		//create trigger
		for (int i = 0,is=table.getTriggers() == null ? 0 : table.getTriggers().size(); i < is; i++) {
			createTrigger(conn, table.getTriggers().get(i));
		}
	}
	public static void addColumn(Connection conn,DBColumn column) throws SQLException{
		String query = "alter table "+ column.getTable() + " add column "+column.getName()+" "+column.getType().toString()+(Misc.isUndef(column.getLength()) ? "":"("+column.getLength()+")");
		System.out.println(query);
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
	}
	public static void changeColumn(Connection conn,DBColumn column) throws SQLException{
		String query = "alter table "+ column.getTable() + " change column "+column.getName()+" "+column.getName()+" "+column.getType().toString()+(Misc.isUndef(column.getLength()) ? "":"("+column.getLength()+")");
		System.out.println(query);
		Statement st = conn.createStatement();
		st.execute(query);
		st.close();
	}
	public static void createUniqueKey(Connection conn,DBColumn column) throws SQLException{
		try{
			String query = "alter table "+column.getTable()+" add constraint uc_"+column.getTable()+"_"+column.getName()+" UNIQUE ("+column.getName()+")";
			System.out.println(query);
			Statement st = conn.createStatement();
			st = conn.createStatement();
			st.execute(query);
			st.close();
		}catch(com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException ex){
			System.err.println(ex.getMessage());
			String query = "truncate "+column.getTable();
			System.out.println(query);
			Statement st = conn.createStatement();
			st = conn.createStatement();
			st.execute(query);
			query = "update "+Table.dataTableSyncStatus+" set last_read_at=null where table_name='"+column.getTable()+"'";
			st.execute(query);
			st.close();
			createUniqueKey(conn, column);
		}
	}
	public static void createTrigger(Connection conn,Trigger trigger) throws SQLException{
		if(trigger == null)
			return;
		String query = getCreateTriggerStatement(trigger).toString();
		System.out.println(query);
		Statement st = conn.createStatement();
		st.execute("DROP TRIGGER IF EXISTS "+trigger.getName());
		st.execute(query);
		st.close();
	}
	public static Pair<Table,Table> handleSchemaChanges(Connection fromConn, Connection toConn,String fromSchema, String toSchema,String tableName,boolean isNested) throws Exception{
		Table fromTable = getTable(fromConn, tableName, fromSchema);
		Table toTable = getTable(toConn, tableName, toSchema);
		if(fromTable == null)
			return null;
		ArrayList<DBColumn> fromColList = fromTable.getColums();
		if(fromColList == null )
			return null;
		ArrayList<DBColumn> toColList = null;
		try{
			if(!isNested){
				if(fromTable.getColumnByName(TEMP_REMOTE_ID_COLUMN) == null){
					DBColumn col = new DBColumn(TEMP_REMOTE_ID_COLUMN,ColumnType.INT, 11, false, false, 4, tableName);
					addColumn(fromConn, col);
					fromTable.getColums().add(col);
				}
				if(fromTable.getColumnByName(RECORD_SRC) == null){
					DBColumn col = new DBColumn(RECORD_SRC,ColumnType.INT, 11, false, false, 4, tableName);
					addColumn(fromConn, col);
					fromTable.getColums().add(col);
				}
				if(!fromTable.isHasUpdatedOn()){
					//add updatedOn
					DBColumn col = new DBColumn("updated_on",ColumnType.INT, 11, false, false, 4, tableName);
					addColumn(fromConn, col);
					fromTable.getColums().add(col);
				}
				if(!fromTable.isHasUpdTrigger()){
					createTrigger(fromConn, Trigger.getOnInsertTrigger(tableName, fromSchema));
				}
				if(!fromTable.isHasInsTrigger()){
					createTrigger(fromConn, Trigger.getOnUpdateTrigger(tableName, fromSchema));
				}
			}
			if(toTable == null){
				toTable = new Table(fromTable);
				createTable(toConn, toTable);
			}
			toColList = toTable.getColums();

			for(int i = 0; i < fromColList.size(); i++) {
				DBColumn fromCol = fromColList.get(i);
				boolean isExist = false;
				for (int j = 0; j < toColList.size(); j++) {
					isExist = toColList.get(j).equals(fromCol);
					if(isExist){
						if(fromCol.getType() != toColList.get(j).getType() || fromCol.getLength() != toColList.get(j).getLength())
							changeColumn(toConn, fromCol);
						break;
					}
				}
				if(!isExist)
					addColumn(toConn, fromCol);
			}
			for (int i = 0,is=fromTable.getTriggers() == null ? 0 : fromTable.getTriggers().size(); i < is; i++) {
				boolean isExist = false;
				for (int j = 0,js = toTable.getTriggers() == null ? 0 : toTable.getTriggers().size() ; j < js; j++) {
					isExist = fromTable.getTriggers().get(i).getName().equalsIgnoreCase(toTable.getTriggers().get(j).getName());
					if(isExist)
						break;
				}
				if(!isExist){
					createTrigger(toConn, fromTable.getTriggers().get(i));
				}
			}
			ArrayList<DBColumn> toUniueKeys = toTable.getUniqueKeys();
			for (int i = 0,is=fromTable.getUniqueKeys() == null ? 0 : fromTable.getUniqueKeys().size(); i < is; i++) {
				boolean isExist = false;
				for (int j = 0,js = toTable.getUniqueKeys() == null ? 0 : toTable.getUniqueKeys().size() ; j < js; j++) {
					isExist = fromTable.getUniqueKeys().get(i).equals(toTable.getUniqueKeys().get(j));
					if(isExist)
						break;
				}
				if(!isExist){
					if(toUniueKeys == null)
						toUniueKeys = new ArrayList<DBSchemaManager.DBColumn>();
					if(toTable.getColumnByName(fromTable.getUniqueKeys().get(i).getName()) != null)
						toTable.getColumnByName(fromTable.getUniqueKeys().get(i).getName()).setUnique(true);
					toUniueKeys.add(toTable.getColumnByName(fromTable.getUniqueKeys().get(i).getName()));
					createUniqueKey(toConn, toTable.getColumnByName(fromTable.getUniqueKeys().get(i).getName()));
				}
			}
			toTable.setUniqueKeys(toUniueKeys);
		}catch(Exception ex){
			ex.printStackTrace();
			//throw ex;
		}
		DBColumnComparator dbColumnComparator = new DBColumnComparator();
		if(fromColList != null)
			Collections.sort(fromColList, dbColumnComparator);
		if(toColList != null)
			Collections.sort(toColList, dbColumnComparator);
		return new Pair<Table,Table>(fromTable, toTable);
	}
	public static Pair<ArrayList<DBColumn>,ArrayList<DBColumn>> handleSchemaChanges(Connection fromConn, Connection toConn, String tableName) throws Exception{
		ArrayList<DBColumn> fromColList = null;
		ArrayList<DBColumn> toColList = null;
		try{
			fromColList = getColumnInfo(fromConn, tableName);
			toColList = getColumnInfo(toConn, tableName);
			if(fromColList == null )
				return null;
			if(toColList == null){
				createTable(toConn, fromColList, tableName);
				toColList = getColumnInfo(toConn, tableName);
			}
			if(toColList == null)
				return null;
			boolean isRemoteIdPresent = false;
			boolean isRemoteHasGeneratedColumn = false;
			for (int i = 0; i < toColList.size(); i++) {
				if(toColList.get(i).getName().equalsIgnoreCase(TEMP_REMOTE_ID_COLUMN)){
					isRemoteIdPresent = true;
					break;
				}
			}
			for(int i = 0; i < fromColList.size(); i++) {
				DBColumn fromCol = fromColList.get(i);
				if(!isRemoteHasGeneratedColumn){
					isRemoteHasGeneratedColumn = fromCol.isGenerated();
				}
				boolean isExist = false;
				for (int j = 0; j < toColList.size(); j++) {
					isExist = toColList.get(j).equals(fromCol);
					if(isExist){
						if(fromCol.getType() != toColList.get(j).getType()){
							//run change command
							String query = "alter table "+ tableName + " change column "+fromCol.getName()+" "+fromCol.getName()+" "+fromCol.getType().toString()+(Misc.isUndef(fromCol.getLength()) ? "":"("+fromCol.getLength()+")");
							System.out.println(query);
							Statement st = toConn.createStatement();
							st.execute(query);
							st.close();
						}
						break;
					}
				}
				if(!isExist){
					//run alter command
					String query = "alter table "+ tableName + " add column "+fromCol.getName()+" "+fromCol.getType().toString()+(Misc.isUndef(fromCol.getLength()) ? "":"("+fromCol.getLength()+")");
					System.out.println(query);
					Statement st = toConn.createStatement();
					st.execute(query);
					st.close();
				}
			}
			if(isRemoteHasGeneratedColumn && !isRemoteIdPresent){
				String query = "alter table "+ tableName + " add column "+TEMP_REMOTE_ID_COLUMN+" int(11)";
				System.out.println(query);
				Statement st = toConn.createStatement();
				st.execute(query);
				st.close();
				//				toColList.add(new DBColumn(TEMP_REMOTE_ID_COLUMN, ColumnType.INT, 10, false, false, 4));
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		DBColumnComparator dbColumnComparator = new DBColumnComparator();
		if(fromColList != null)
			Collections.sort(fromColList, dbColumnComparator);
		if(toColList != null)
			Collections.sort(toColList, dbColumnComparator);
		return new Pair<ArrayList<DBColumn>, ArrayList<DBColumn>>(fromColList, toColList);
	}

	public static class DBColumnComparator implements Comparator<DBColumn>{
		public int compare(DBColumn o1, DBColumn o2) {
			// TODO Auto-generated method stub
			return o1.getName().compareToIgnoreCase(o2.getName());
		}

	}
	public static enum ColumnType{
		INT,
		TINYINT,
		SMALLINT,
		MEDIUMINT,
		BIGINT,
		FLOAT,
		DOUBLE,
		DATE,
		DATETIME,
		TIMESTAMP,
		VARCHAR,
		BLOB,
		LONGBLOB,
		MEDIUMBLOB
	}
	public static class DBColumn{
		private String name;
		private ColumnType type;
		private int length;
		private boolean isUnique = false;
		private boolean isGenerated = false;
		private int index = Misc.getUndefInt();
		private int dataType = Misc.getUndefInt();
		private String table;
		public DBColumn() {
			super();
		}
		public DBColumn(DBColumn ref) {
			super();
			this.name = ref.getName();
			this.type = ref.getType();
			this.length = ref.getLength();
			this.isUnique = ref.isUnique();
			this.isGenerated = ref.isGenerated();
			this.index = ref.getIndex();
			this.dataType = ref.getDataType();
			this.table = ref.getTable();
		}
		public DBColumn(String name, ColumnType type, int length, boolean isGenerated, boolean isUnique, int dataType, String table) {
			super();
			this.name = name;
			this.type = type;
			this.length = length;
			this.isUnique = isUnique;
			this.isGenerated = isGenerated;
			this.dataType = dataType;
			this.table = table;
		}
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ColumnType getType() {
			return type;
		}

		public void setType(ColumnType type) {
			this.type = type;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public boolean isUnique() {
			return isUnique;
		}

		public void setUnique(boolean isUnique) {
			this.isUnique = isUnique;
		}

		public boolean isGenerated() {
			return isGenerated;
		}

		public void setGenerated(boolean isGenerated) {
			this.isGenerated = isGenerated;
		}


		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getDataType() {
			return dataType;
		}

		public void setDataType(int dataType) {
			this.dataType = dataType;
		}



		@Override
		public String toString() {
			return "DBColumn [name=" + name + ", type=" + type + ", length=" + length + ", isUnique=" + isUnique
					+ ", isGenerated=" + isGenerated + ", index=" + index + ", dataType=" + dataType + ", table="
					+ table + "]";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + length;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DBColumn other = (DBColumn) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		public String getTable() {
			return table;
		}
		public void setTable(String table) {
			this.table = table;
		}

	}

	public static Pair<StringBuilder, ArrayList<DBColumn>> getUpdateStatment(Table table, DBColumn primaryKey){
		if(table == null)
			return null;
		StringBuilder params = null;
		String tableName = table.getName();
		ArrayList<DBColumn> colList = table.getColums();
		ArrayList<DBColumn> queryCols = new ArrayList<DBSchemaManager.DBColumn>();
		if(primaryKey == null){
			for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
				if(colList.get(i).isGenerated() || colList.get(i).isUnique())
					continue;
				if(params == null){
					params = new StringBuilder();
				}else {
					params.append(" , ");
				}
				params.append(colList.get(i).getName()+"=?");
				queryCols.add(colList.get(i));
			}
		}
		StringBuilder where = null;
		if((table.getUniqueKeys() != null && table.getUniqueKeys().size() > 0) || primaryKey != null || table.getGeneratedKey() != null){
			if(primaryKey != null){
				if(where == null)
					where = new StringBuilder();
				where.append(primaryKey.getName()+"=?");
				queryCols.add(primaryKey);
			}else if(table.getUniqueKeys() != null && table.getUniqueKeys().size() > 0){
				for (int i = 0,is=table.getUniqueKeys() == null ? 0 : table.getUniqueKeys().size(); i < is; i++) {
					if(where == null)
						where = new StringBuilder();
					else
						where.append(" and ");
					where.append(table.getUniqueKeys().get(i).getName()+"=?");
					queryCols.add(table.getUniqueKeys().get(i));
				}
			}else{
				if(where == null)
					where  = new StringBuilder();
				where.append(table.getGeneratedKey().getName()+"=?");
				queryCols.add(table.getGeneratedKey());
			}
		}
		return params == null || where == null ? null :new Pair<StringBuilder, ArrayList<DBColumn>>(new StringBuilder("update "+tableName+" set "+params.toString()+" where "+where.toString()),queryCols);
	}
	public static Pair<StringBuilder, ArrayList<DBColumn>> getUniqueSelect(Table table, DBColumn primaryKey){
		if(table == null)
			return null;
		String tableName = table.getName();
		ArrayList<DBColumn> queryCols = new ArrayList<DBSchemaManager.DBColumn>();
		StringBuilder where = null;
		if((table.getUniqueKeys() != null && table.getUniqueKeys().size() > 0) || primaryKey != null || table.getGeneratedKey() != null){
			if(primaryKey != null){
				if(where == null)
					where = new StringBuilder();
				where.append(primaryKey.getName()+"=?");
				queryCols.add(primaryKey);
			}else if(table.getUniqueKeys() != null && table.getUniqueKeys().size() > 0){
				for (int i = 0,is=table.getUniqueKeys() == null ? 0 : table.getUniqueKeys().size(); i < is; i++) {
					if(where == null)
						where = new StringBuilder();
					else
						where.append(" and ");
					where.append(table.getUniqueKeys().get(i).getName()+"=?");
					queryCols.add(table.getUniqueKeys().get(i));
				}
			}else{
				if(where == null)
					where  = new StringBuilder();
				where.append(table.getGeneratedKey().getName()+"=?");
				queryCols.add(table.getGeneratedKey());
			}
		}
		return where == null ? null :new Pair<StringBuilder, ArrayList<DBColumn>>(new StringBuilder("select "+tableName+"."+table.getGeneratedKey().getName()+" from "+tableName+" where "+where.toString()),queryCols);
	}
	public static Pair<StringBuilder, ArrayList<DBColumn>> getInsertStatment(ArrayList<DBColumn> colList,String tableName){
		return getInsertStatment(colList, tableName,false,false);
	}
	public static Pair<StringBuilder, ArrayList<DBColumn>> getInsertStatment(ArrayList<DBColumn> colList,String tableName,boolean isNested){
		return getInsertStatment(colList, tableName,isNested,false);
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getInsertStatment(ArrayList<DBColumn> colList,String tableName, boolean isNested, boolean addIgnore){
		StringBuilder params = null;
		StringBuilder values = null;
		ArrayList<DBColumn> cols = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			if(isNested && colList.get(i).isGenerated())
				continue;
			if(params == null){
				params = new StringBuilder();
				values = new StringBuilder();
			}else {
				params.append(", ");
				values.append(", ");
			}
			if(cols == null)
				cols = new ArrayList<DBSchemaManager.DBColumn>();
			cols.add(colList.get(i));
			params.append(colList.get(i).isGenerated() ? TEMP_REMOTE_ID_COLUMN : colList.get(i).getName());
			values.append("?");
		}
		StringBuilder query = null;
		if(params != null){
			query = new StringBuilder();
			query.append(" insert into "+tableName+" ("+params.toString()+") values ("+values.toString()+" ) ");
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, cols); 
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getInsertWithColSet(ArrayList<DBColumn> colList,String tableName,HashSet<String> remoteSelColSet){
		StringBuilder params = null;
		StringBuilder values = null;
		ArrayList<DBColumn> cols = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			if(colList.get(i).isGenerated())
				continue;
			if(!remoteSelColSet.contains(colList.get(i).getName()))
				continue;
			if(params == null){
				params = new StringBuilder();
				values = new StringBuilder();
			}else {
				params.append(", ");
				values.append(", ");
			}
			
			if(cols == null)
				cols = new ArrayList<DBSchemaManager.DBColumn>();
			cols.add(colList.get(i));
			params.append(colList.get(i).getName());
			values.append("?");
		}
		/*StringBuilder onUpadate = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			if( colList.get(i).isUnique())
				continue;
			if(!remoteSelColSet.contains(colList.get(i).getName()))
				continue;
			if(onUpadate == null){
				onUpadate = new StringBuilder();
			}else {
				onUpadate.append(", ");
			}
			
			if(cols == null)
				cols = new ArrayList<DBSchemaManager.DBColumn>();
			cols.add(colList.get(i));
			onUpadate.append(colList.get(i).getName()).append("=").append("?");
		}*/
		StringBuilder query = null;
		if(params != null){
			query = new StringBuilder();
			query.append(" insert into "+tableName+" ("+params.toString()+") values ("+values.toString()+" )");
			//query.append(" insert into "+tableName+" ("+params.toString()+") values ("+values.toString()+" ) on duplicate key update "+ onUpadate.toString());
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, cols); 
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getInsertOnDuplicateUpd(ArrayList<DBColumn> colList,String tableName,HashSet<String> remoteSelColSet){
		StringBuilder params = null;
		StringBuilder values = null;
		ArrayList<DBColumn> cols = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			/*if(colList.get(i).isGenerated())
				continue;*/
			if(!remoteSelColSet.contains(colList.get(i).getName()))
				continue;
			if(params == null){
				params = new StringBuilder();
				values = new StringBuilder();
			}else {
				params.append(", ");
				values.append(", ");
			}
			if(cols == null)
				cols = new ArrayList<DBSchemaManager.DBColumn>();
			cols.add(colList.get(i));
			params.append(colList.get(i).getName());
			values.append("?");
		}
		StringBuilder onUpadate = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			if( colList.get(i).isUnique())
				continue;
			if(!remoteSelColSet.contains(colList.get(i).getName()))
				continue;
			if(onUpadate == null){
				onUpadate = new StringBuilder();
			}else {
				onUpadate.append(", ");
			}
			if(cols == null)
				cols = new ArrayList<DBSchemaManager.DBColumn>();
			cols.add(colList.get(i));
			onUpadate.append(colList.get(i).getName()).append("=").append("?");
		}
		StringBuilder query = null;
		if(params != null){
			query = new StringBuilder();
			query.append(" insert into "+tableName+" ("+params.toString()+") values ("+values.toString()+" ) on duplicate key update "+ onUpadate.toString());
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, cols); 
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getSelectStatment(ArrayList<DBColumn> colList,String tableName){
		return getSelectStatment(colList, tableName, false);
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getSelectStatmentWithColSet(ArrayList<DBColumn> colList,String tableName, boolean isNested, HashSet<String> remoteSelColSet){
		StringBuilder params = null;
		ArrayList<DBColumn> queryCols = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			if(isNested && colList.get(i).isGenerated())
				continue;
			if(params == null){
				params = new StringBuilder();
			}else {
				params.append(", ");
			}
			if(queryCols == null)
				queryCols = new ArrayList<DBSchemaManager.DBColumn>();
			queryCols.add(colList.get(i));
			params.append("t0."+colList.get(i).getName());
			remoteSelColSet.add(colList.get(i).getName());
		}
		StringBuilder query = null;
		if(params != null){
			query = new StringBuilder();
			query.append(" select "+params.toString()+" from "+tableName+" t0");
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, queryCols);
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getSelectStatment(ArrayList<DBColumn> colList,String tableName, boolean isNested){
		StringBuilder params = null;
		ArrayList<DBColumn> queryCols = null;
		for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
			if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(colList.get(i).getName()))
				continue;
			if(isNested && colList.get(i).isGenerated())
				continue;
			if(params == null){
				params = new StringBuilder();
			}else {
				params.append(", ");
			}
			if(queryCols == null)
				queryCols = new ArrayList<DBSchemaManager.DBColumn>();
			queryCols.add(colList.get(i));
			params.append("t0."+colList.get(i).getName());
		}
		StringBuilder query = null;
		if(params != null){
			query = new StringBuilder();
			query.append(" select "+params.toString()+" from "+tableName+" t0");
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, queryCols);
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getDeleteStatmentByUniqueKey(Table table, DBColumn primaryKey, DBColumn foreignKey){
		if(table == null)
			return null;
		StringBuilder params = null;
		String tableName = table.getName();
		ArrayList<DBColumn> colList = table.getUniqueKeys();
		ArrayList<DBColumn> queryCols = new ArrayList<DBSchemaManager.DBColumn>();
		if(primaryKey == null){
			for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
				if(params == null){
					params = new StringBuilder();
				}else {
					params.append(" and ");
				}
				queryCols.add(colList.get(i));
				params.append(colList.get(i).getName()+"=?");
			}
		}
		StringBuilder query = null;
		if(params != null || primaryKey != null || foreignKey != null){
			query = new StringBuilder();
			query.append(" delete from "+tableName+" where ");
			if(foreignKey != null){
				queryCols.add(foreignKey);
				query.append(foreignKey.getName()+"=?");
			}else if(params != null){
				query.append(params.toString());
			}else{
				queryCols.add(primaryKey);
				query.append(primaryKey.getName()+"=?");
			}
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, queryCols);
	}
	public static Pair<StringBuilder,ArrayList<DBColumn>> getDeleteStatmentByUniqueKey(ArrayList<DBColumn> colList,String tableName, DBColumn keyColumn){
		StringBuilder params = null;
		DBColumn generatedCol = null;
		ArrayList<DBColumn> queryCols = new ArrayList<DBSchemaManager.DBColumn>();
		if(keyColumn == null){
			for (int i = 0,is=colList == null ? 0 : colList.size(); i < is; i++) {
				if(colList.get(i).isGenerated() || !colList.get(i).isUnique()){
					if(colList.get(i).isGenerated()){
						generatedCol = colList.get(i);
					}
					continue;
				}
				if(params == null){
					params = new StringBuilder();
				}else {
					params.append(" and ");
				}
				queryCols.add(colList.get(i));
				params.append(colList.get(i).getName()+"=?");
			}
		}
		StringBuilder query = null;
		if(params != null || generatedCol != null || keyColumn != null){
			query = new StringBuilder();
			query.append(" delete from "+tableName+" where ");
			if(keyColumn != null){
				queryCols.add(keyColumn);
				query.append(keyColumn.getName()+"=?");
			}else if(params != null){
				query.append(params.toString());
			}else{
				queryCols.add(generatedCol);
				query.append(generatedCol.getName()+"=?");
			}
		}
		return query == null ? null : new Pair<StringBuilder, ArrayList<DBColumn>>(query, queryCols);
	}
	private static void syncTables(Connection fromConn, Connection toConn, String tableName,ArrayList<Pair<String,String>> dependentTables) throws Exception{
		if(fromConn == null || toConn == null || tableName == null || tableName.length() <= 0)
			return;
		Pair<ArrayList<DBColumn>, ArrayList<DBColumn>> colListPair =  handleSchemaChanges(fromConn,toConn, tableName);
		if(colListPair == null)
			return;
		ArrayList<Pair<Integer,Integer>> keysPairList = copyData(fromConn, toConn, tableName, colListPair.first, colListPair.second, null, null);
		for (int i = 0,is=dependentTables == null ? 0 : dependentTables.size(); i < is; i++) {
			tableName = dependentTables.get(i).first;
			colListPair = handleSchemaChanges(fromConn, toConn, tableName);
			if(colListPair == null)
				continue;
			DBColumn keyColumn = null;
			for (int j = 0,js=colListPair.first == null ? 0 : colListPair.first.size(); j < js; j++) {
				if(colListPair.first.get(j).getName().equalsIgnoreCase(dependentTables.get(i).second)){
					keyColumn = colListPair.first.get(j);
					break;
				}
			}
			copyData(fromConn, toConn, dependentTables.get(i).first, colListPair.first, colListPair.second, keysPairList, keyColumn);
		}
	}
	public static Pair<ArrayList<Pair<Integer,Integer>>,Timestamp> copyData(Connection fromConn, Connection toConn,Table fromTable,Table toTable, ArrayList<Pair<Integer,Integer>> askedKeysPairList,DBColumn primaryKey, DBColumn foreignKey, Table parentTable,Timestamp readTimestamp,int workstationIdOnserver) throws SQLException{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psDelete = null;
		PreparedStatement psUpdate = null;
		PreparedStatement psSelectUnique = null;
		ResultSet rs = null;
		boolean isNested = foreignKey != null;
		Pair<StringBuilder, ArrayList<DBColumn>> select = getSelectStatment(fromTable.getColums(), fromTable.getName(),isNested);
		Pair<StringBuilder, ArrayList<DBColumn>> insert = getInsertStatment(isNested ? toTable.getColums() : toTable.getUniqueKeys(), toTable.getName(),isNested);
		Pair<StringBuilder, ArrayList<DBColumn>> update = isNested ? null : getUpdateStatment(toTable, isNested ? primaryKey : null);
		Pair<StringBuilder, ArrayList<DBColumn>> selectUnique = isNested ? null : getUniqueSelect(toTable, isNested ? primaryKey : null);
		Pair<StringBuilder, ArrayList<DBColumn>> delete = !isNested ? null : getDeleteStatmentByUniqueKey(toTable.getColums(), toTable.getName(), isNested ? primaryKey : null);
		StringBuilder selectQuery = select.first;
		StringBuilder keysList = null;
		ArrayList<Pair<Integer,Integer>> keysPairList = null;
		for (int i = 0,is=askedKeysPairList == null ? 0 : askedKeysPairList.size(); i < is; i++) {
			if(keysList == null){
				keysList = new StringBuilder();
			}else{
				keysList.append(", ");
			}
			keysList.append(askedKeysPairList.get(i).first);
		}
		if(isNested && keysList == null)
			return null;
		if(isNested){
			selectQuery.append(" join "+parentTable.getName()+" t1 on (t0."+primaryKey.getName()+"=t1."+foreignKey.getName()+") where t1."+foreignKey.getName()+" in ("+(keysList == null ? "" :keysList.toString())+")");
		}else if(keysList != null){
			selectQuery.append(" where t0."+primaryKey.getName()+" in ("+(keysList == null ? "" :keysList.toString())+")");
		}else{
			selectQuery.append(" where (? is null or t0.updated_on > ? ) and (t0."+RECORD_SRC+" is null or t0."+RECORD_SRC+"!="+workstationIdOnserver+") ");
		}
		//psSelect = fromConn.prepareStatement(select.first.toString()+(!isNested  ? " where (? is null or t0.updated_on > ? ) " : " where t0."+primaryKey.getName()+" in (" +keysList.toString()+ ")"));
		psSelect = fromConn.prepareStatement(selectQuery.toString());
		psInsert = toConn.prepareStatement(insert.first.toString(),PreparedStatement.RETURN_GENERATED_KEYS);
		if(delete != null)
			psDelete = toConn.prepareStatement(delete.first.toString());
		if(!isNested && keysList == null){
			setParam(Table.getSyncStatusSchema().getColums().get(1), psSelect, 1, readTimestamp);
			setParam(Table.getSyncStatusSchema().getColums().get(1), psSelect, 2, readTimestamp);
		}
		if(selectUnique != null)
			psSelectUnique = toConn.prepareStatement(selectUnique.first.toString());
		if(update != null)
			psUpdate = toConn.prepareStatement(update.first.toString());
		System.out.println(psSelect);
		rs = psSelect.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		HashSet<String> rsColSet = null;
		while(rs.next()){
			if(rsColSet == null){
				rsColSet = new HashSet<String>();
				for (int i = 0; i < colCount; i++) {
					rsColSet.add(rsmd.getColumnName(i+1));
				}
			}
			if(!isNested){
				int toGeneratedId = Misc.getUndefInt();
				int remoteGeneratedId = Misc.getUndefInt(); 
				int psIndex = 1;
				for (int i = 0,is=selectUnique == null || selectUnique.second == null ? 0 : selectUnique.second.size() ; i < is; i++) {
					if(rsColSet.contains(selectUnique.second.get(i).getName())){
						setParam(selectUnique.second.get(i), psSelectUnique, psIndex++, getParam(selectUnique.second.get(i),rs));
					}
				}
				psIndex = 1;
				System.out.println(psSelectUnique.toString());
				ResultSet tempRs = psSelectUnique.executeQuery();
				if(tempRs.next())
					toGeneratedId = Misc.getRsetInt(tempRs, 1);
				tempRs.close();
				if(Misc.isUndef(toGeneratedId)){//insert new 
					for (int i = 0,is=insert == null || insert.second == null ? 0 : insert.second.size() ; i < is; i++) {
						if(rsColSet.contains(insert.second.get(i).getName())){
							setParam(insert.second.get(i), psInsert, psIndex++, getParam(insert.second.get(i),rs));
						}
					}
					System.out.println(psInsert.toString());
					psInsert.execute();
					tempRs = psInsert.getGeneratedKeys();
					if(tempRs.next())
						toGeneratedId = Misc.getRsetInt(tempRs, 1);
					tempRs.close();
				}
				if(fromTable.getGeneratedKey() != null && rsColSet.contains(fromTable.getGeneratedKey().getName())){
					remoteGeneratedId = Misc.getRsetInt(rs, fromTable.getGeneratedKey().getName());
				}
				psIndex = 1;
				for (int i = 0,is=update == null || update.second == null ? 0 : update.second.size() ; i < is; i++) {
					if(TEMP_REMOTE_ID_COLUMN.equalsIgnoreCase(update.second.get(i).getName())){
						Misc.setParamInt(psUpdate, remoteGeneratedId, psIndex++);
						continue;
					}
					if(rsColSet.contains(update.second.get(i).getName())){
						if(update.second.get(i).isGenerated()){
							Misc.setParamInt(psUpdate, toGeneratedId, psIndex++);
						}else{
							setParam(update.second.get(i), psUpdate, psIndex++, getParam(update.second.get(i),rs));
						}
					}
				}
				System.out.println(psUpdate.toString());
				psUpdate.executeUpdate();
				if(!Misc.isUndef(remoteGeneratedId)){
					if(keysPairList == null)
						keysPairList = new ArrayList<Pair<Integer,Integer>>();
					keysPairList.add(new Pair<Integer, Integer>(remoteGeneratedId, toGeneratedId));
				}
				if(rsColSet.contains("updated_on")){
					Timestamp cTs = rs.getTimestamp("updated_on");
					if(readTimestamp == null)
						readTimestamp = cTs;
					else{
						if(cTs != null && cTs.getTime() > readTimestamp.getTime())
							readTimestamp = cTs;
					}
				}




			}else{
				int insertIndex = 1;
				DBColumn dbc = null;
				for (int i = 0; i < colCount; i++) {
					for (int j = 0; j < fromTable.getColums().size(); j++) {
						if(fromTable.getColums().get(j).getName().equalsIgnoreCase(rsmd.getColumnLabel(i+1))){
							dbc = fromTable.getColums().get(j);
							break;
						}
					}
					if(dbc == null)
						continue;
					Object val = getParam(dbc, rs);
					if(primaryKey != null  && dbc.equals(primaryKey) && isNested){
						for (int j = 0,js=askedKeysPairList == null ? 0 : askedKeysPairList.size(); j < js; j++) {
							if((Integer)val == askedKeysPairList.get(j).first){
								val = askedKeysPairList.get(j).second;
								break;
							}
						}
					}
					if(delete != null && delete.second != null && delete.second.size() > 0){
						for (int j = 0, js=delete.second == null ? 0 : delete.second.size(); j < js; j++) {
							if(delete.second.get(j).equals(dbc)){
								setParam(dbc, psDelete, j+1, val);
								break;
							}
						}
					}
					setParam(dbc, psInsert, insertIndex++, val);
				}
				if(psDelete != null ){
					System.out.println(psDelete.toString()+";");
					psDelete.addBatch();
				}
				if(psInsert != null ){
					System.out.println(psInsert.toString()+";");
					psInsert.addBatch();
				}
			}
		}
		if(isNested){
			if(psDelete != null && isNested)
				psDelete.executeBatch();
			if(psInsert != null && isNested){
				psInsert.executeBatch();
			}
		}
		Misc.closeRS(rs);
		Misc.closePS(psSelect);
		Misc.closePS(psDelete);
		Misc.closePS(psInsert);
		Misc.closePS(psUpdate);
		Misc.closePS(psSelectUnique);
		return new Pair<ArrayList<Pair<Integer,Integer>>, Timestamp>(keysPairList, readTimestamp);
	}
	public static Pair<ArrayList<Pair<Integer,Integer>>,Timestamp> copyDataWithDel(Connection fromConn, Connection toConn,Table fromTable,Table toTable, ArrayList<Pair<Integer,Integer>> keysPairList,DBColumn primaryKey, DBColumn foreignKey, Table parentTable,Timestamp readTimestamp,int workstationIdOnserver) throws SQLException{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psDelete = null;
		ResultSet rs = null;
		boolean isNested = foreignKey != null;
		Pair<StringBuilder, ArrayList<DBColumn>> select = getSelectStatment(fromTable.getColums(), fromTable.getName(),isNested);
		Pair<StringBuilder, ArrayList<DBColumn>> insert = getInsertStatment(toTable.getColums(), toTable.getName(),isNested);
		Pair<StringBuilder, ArrayList<DBColumn>> update = getUpdateStatment(toTable, isNested ? primaryKey : null);
		Pair<StringBuilder, ArrayList<DBColumn>> delete = getDeleteStatmentByUniqueKey(toTable.getColums(), toTable.getName(), isNested ? primaryKey : null);
		ArrayList<Pair<Integer,Integer>> retval = null;
		StringBuilder selectQuery = select.first;
		StringBuilder keysList = null;
		for (int i = 0,is=keysPairList == null ? 0 : keysPairList.size(); i < is; i++) {
			if(keysList == null){
				keysList = new StringBuilder();
			}else{
				keysList.append(", ");
			}
			keysList.append(keysPairList.get(i).first);
		}
		if(isNested && keysList == null)
			return null;
		if(isNested){
			selectQuery.append(" join "+parentTable.getName()+" t1 on (t0."+primaryKey.getName()+"=t1."+foreignKey.getName()+") where t1."+foreignKey.getName()+" in ("+(keysList == null ? "" :keysList.toString())+")");
		}else{
			selectQuery.append(" where (? is null or t0.updated_on > ? ) and (t0."+RECORD_SRC+" is null or t0."+RECORD_SRC+"!="+workstationIdOnserver+") ");
		}
		//psSelect = fromConn.prepareStatement(select.first.toString()+(!isNested  ? " where (? is null or t0.updated_on > ? ) " : " where t0."+primaryKey.getName()+" in (" +keysList.toString()+ ")"));
		psSelect = fromConn.prepareStatement(selectQuery.toString());
		psInsert = toConn.prepareStatement(insert.first.toString(),PreparedStatement.RETURN_GENERATED_KEYS);
		if(delete != null)
			psDelete = toConn.prepareStatement(delete.first.toString());
		if(!isNested){
			setParam(Table.getSyncStatusSchema().getColums().get(1), psSelect, 1, readTimestamp);
			setParam(Table.getSyncStatusSchema().getColums().get(1), psSelect, 2, readTimestamp);
		}
		System.out.println(psSelect);
		rs = psSelect.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		while(rs.next()){
			int insertIndex = 1;
			for (int i = 0; i < colCount; i++) {
				DBColumn dbc = null;
				for (int j = 0; j < fromTable.getColums().size(); j++) {
					if(fromTable.getColums().get(j).getName().equalsIgnoreCase(rsmd.getColumnLabel(i+1))){
						dbc = fromTable.getColums().get(j);
						//						insertIndex = j+1;
						break;
					}
				}
				if(dbc.getName().equalsIgnoreCase("updated_on")){
					Timestamp cTs = (Timestamp)getParam(dbc, rs);
					if(readTimestamp == null)
						readTimestamp = cTs;
					else{
						if(cTs != null && cTs.getTime() > readTimestamp.getTime())
							readTimestamp = cTs;
					}
				}
				if(dbc.isGenerated()){
					if(retval == null)
						retval = new ArrayList<Pair<Integer,Integer>>();
					retval.add(new Pair<Integer, Integer>((Integer)getParam(dbc, rs), Misc.getUndefInt()));
				}
				if(dbc != null){
					if(delete != null && delete.second != null && delete.second.size() > 0){
						for (int j = 0, js=delete.second == null ? 0 : delete.second.size(); j < js; j++) {
							if(delete.second.get(j).equals(dbc)){
								setParam(dbc, psDelete, j+1, getParam(dbc, rs));
								break;
							}
						}
					}
					Object val = getParam(dbc, rs);
					//System.out.println(dbc.getName()+":"+val+":"+insertIndex);
					if(primaryKey != null  && dbc.equals(primaryKey) && isNested){
						for (int j = 0,js=keysPairList == null ? 0 : keysPairList.size(); j < js; j++) {
							if((Integer)val == keysPairList.get(j).first){
								val = keysPairList.get(j).first;
								break;
							}
						}
					}
					setParam(dbc, psInsert, insertIndex++, val);
				}
			}
			if(psDelete != null){
				System.out.println(psDelete.toString()+";");
				psDelete.addBatch();
			}
			if(psInsert != null){
				System.out.println(psInsert.toString()+";");
				psInsert.addBatch();
			}
		}
		if(psDelete != null)
			psDelete.executeBatch();
		if(psInsert != null){
			psInsert.executeBatch();
			if(retval != null && !isNested){
				ResultSet genRs = psInsert.getGeneratedKeys();
				int count = 0;
				while (genRs.next()) {
					Pair<Integer,Integer> keys = retval.get(count);
					keys.second = Misc.getRsetInt(genRs, 1);
					retval.set(count++, keys);
				}
			}
		}
		Misc.closeRS(rs);
		Misc.closePS(psSelect);
		Misc.closePS(psDelete);
		Misc.closePS(psInsert);
		return new Pair<ArrayList<Pair<Integer,Integer>>, Timestamp>(keysPairList, readTimestamp);
	}
	public static Pair<ArrayList<Pair<Integer,Integer>>,Timestamp> insertIgnoreData(Connection fromConn, Connection toConn,Table fromTable,Table toTable, ArrayList<Pair<Integer,Integer>> keysPairList,DBColumn primaryKey, DBColumn foreignKey, Table parentTable,Timestamp readTimestamp) throws SQLException{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;
		Pair<StringBuilder, ArrayList<DBColumn>> select = getSelectStatment(fromTable.getColums(), fromTable.getName(),false);
		Pair<StringBuilder, ArrayList<DBColumn>> insert = getInsertStatment(toTable.getColums(), toTable.getName(),false);
		ArrayList<Pair<Integer,Integer>> retval = null;
		StringBuilder selectQuery = select.first;
		StringBuilder keysList = null;
		for (int i = 0,is=keysPairList == null ? 0 : keysPairList.size(); i < is; i++) {
			if(keysList == null){
				keysList = new StringBuilder();
			}else{
				keysList.append(", ");
			}
			keysList.append(keysPairList.get(i).first);
		}
		selectQuery.append(" where (? is null or t0.updated_on > ? ) ");
		//psSelect = fromConn.prepareStatement(select.first.toString()+(!isNested  ? " where (? is null or t0.updated_on > ? ) " : " where t0."+primaryKey.getName()+" in (" +keysList.toString()+ ")"));
		psSelect = fromConn.prepareStatement(selectQuery.toString());
		System.out.println(psSelect);
		psInsert = toConn.prepareStatement(insert.toString(),PreparedStatement.RETURN_GENERATED_KEYS);
		setParam(Table.getSyncStatusSchema().getColums().get(1), psSelect, 1, readTimestamp);
		setParam(Table.getSyncStatusSchema().getColums().get(1), psSelect, 2, readTimestamp);
		rs = psSelect.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		while(rs.next()){
			int insertIndex = 1;
			for (int i = 0; i < colCount; i++) {
				DBColumn dbc = null;
				for (int j = 0; j < fromTable.getColums().size(); j++) {
					if(fromTable.getColums().get(j).getName().equalsIgnoreCase(rsmd.getColumnLabel(i+1))){
						dbc = fromTable.getColums().get(j);
						//						insertIndex = j+1;
						break;
					}
				}
				if(dbc.getName().equalsIgnoreCase("updated_on")){
					Timestamp cTs = (Timestamp)getParam(dbc, rs);
					if(readTimestamp == null)
						readTimestamp = cTs;
					else{
						if(cTs != null && cTs.getTime() > readTimestamp.getTime())
							readTimestamp = cTs;
					}
				}
				if(dbc.isGenerated()){
					if(retval == null)
						retval = new ArrayList<Pair<Integer,Integer>>();
					retval.add(new Pair<Integer, Integer>((Integer)getParam(dbc, rs), Misc.getUndefInt()));
				}
				if(dbc != null){
					Object val = getParam(dbc, rs);
					//System.out.println(dbc.getName()+":"+val+":"+insertIndex);
					setParam(dbc, psInsert, insertIndex++, val);
				}
			}
			if(psInsert != null){
				System.out.println(psInsert.toString()+";");
				psInsert.addBatch();
			}
		}
		if(psInsert != null){
			psInsert.executeBatch();
		}
		Misc.closeRS(rs);
		Misc.closePS(psSelect);
		Misc.closePS(psInsert);
		return new Pair<ArrayList<Pair<Integer,Integer>>, Timestamp>(keysPairList, readTimestamp);
	}
	public static ArrayList<Pair<Integer,Integer>> copyData(Connection fromConn, Connection toConn,String tableName, ArrayList<DBColumn> fromColumn,ArrayList<DBColumn> toColumn,ArrayList<Pair<Integer,Integer>> keysPairList, DBColumn keyColumn) throws SQLException{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psDelete = null;
		ResultSet rs = null;
		Pair<StringBuilder, ArrayList<DBColumn>> select = getSelectStatment(fromColumn, tableName);
		Pair<StringBuilder, ArrayList<DBColumn>> insert = getInsertStatment(toColumn, tableName);
		Pair<StringBuilder, ArrayList<DBColumn>> delete = getDeleteStatmentByUniqueKey(toColumn, tableName, keyColumn);
		if(select == null || insert == null)
			return null;
		ArrayList<Pair<Integer,Integer>> retval = null;
		System.out.println("["+tableName+"] "+select.first);
		System.out.println("["+tableName+"] "+insert);
		System.out.println("["+tableName+"] "+(delete == null ? null : delete.first));
		StringBuilder keysList = null;
		for (int i = 0,is=keysPairList == null ? 0 : keysPairList.size(); i < is; i++) {
			if(keysList == null){
				keysList = new StringBuilder();
			}else{
				keysList.append(", ");
			}
			keysList.append(keysPairList.get(i).first);
		}
		psSelect = fromConn.prepareStatement(select.first.toString()+(keyColumn == null || keysList == null ? "" : " where "+keyColumn.getName()+" in (" +keysList.toString()+ ")"));
		psInsert = toConn.prepareStatement(insert.first.toString(),PreparedStatement.RETURN_GENERATED_KEYS);
		if(delete != null)
			psDelete = toConn.prepareStatement(delete.first.toString());
		rs = psSelect.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		while(rs.next()){
			int insertIndex = 1;
			int deleteIndex = 1;
			for (int i = 0; i < colCount; i++) {
				DBColumn dbc = null;
				for (int j = 0; j < fromColumn.size(); j++) {
					if(fromColumn.get(j).getName().equalsIgnoreCase(rsmd.getColumnLabel(i+1))){
						dbc = fromColumn.get(j);
						break;
					}
				}
				if(dbc.isGenerated()){
					if(retval == null)
						retval = new ArrayList<Pair<Integer,Integer>>();
					retval.add(new Pair<Integer, Integer>((Integer)getParam(dbc, rs), Misc.getUndefInt()));
				}
				if(dbc != null){
					if(delete != null && delete.second != null && delete.second.size() > 0){
						for (int j = 0, js=delete.second == null ? 0 : delete.second.size(); j < js; j++) {
							if(delete.second.get(j).equals(dbc)){
								setParam(dbc, psDelete, deleteIndex++, getParam(dbc, rs));
								break;
							}
						}
					}
					Object val = getParam(dbc, rs);
					//System.out.println(dbc.getName()+":"+val+":"+insertIndex);
					if(keyColumn != null  && dbc.equals(keyColumn)){
						for (int j = 0,js=keysPairList == null ? 0 : keysPairList.size(); j < js; j++) {
							if((Integer)val == keysPairList.get(j).first){
								val = keysPairList.get(j).first;
								break;
							}
						}
					}
					setParam(dbc, psInsert, insertIndex++, val);
				}
			}
			if(psDelete != null){
				psDelete.addBatch();
			}
			if(psInsert != null){
				System.out.println(psInsert.toString()+";");
				psInsert.addBatch();
			}
		}
		Misc.closeRS(rs);
		Misc.closePS(psSelect);
		if(psDelete != null)
			psDelete.executeBatch();
		if(psInsert != null){
			psInsert.executeBatch();
			if(retval != null){
				ResultSet genRs = psInsert.getGeneratedKeys();
				int count = 0;
				while (genRs.next()) {
					Pair<Integer,Integer> keys = retval.get(count);
					keys.second = Misc.getRsetInt(genRs, 1);
					retval.set(count++, keys);
				}
			}
		}
		Misc.closePS(psDelete);
		Misc.closePS(psInsert);
		return retval;
	}
	public static Object getParam(DBColumn dbColumn,ResultSet rs) throws SQLException{
		if(dbColumn == null || rs == null )
			return null;
		if(ColumnType.INT == dbColumn.getType() || ColumnType.TINYINT == dbColumn.getType()  || ColumnType.SMALLINT == dbColumn.getType()){
			int i = rs.getInt(dbColumn.getName());
			if (rs.wasNull())
				return null;
			return new Integer(i);
		}else if(ColumnType.MEDIUMINT == dbColumn.getType() || ColumnType.BIGINT == dbColumn.getType()){
			Long i = rs.getLong(dbColumn.getName());
			if (rs.wasNull())
				return null;
			return new Long(i);
		}else if(ColumnType.DOUBLE == dbColumn.getType()){
			Double i = rs.getDouble(dbColumn.getName());
			if (rs.wasNull())
				return null;
			return new Double(i);
		}else if(ColumnType.FLOAT == dbColumn.getType()){
			Float i = rs.getFloat(dbColumn.getName());
			if (rs.wasNull())
				return null;
			return new Float(i);
		}else if(ColumnType.TIMESTAMP == dbColumn.getType() || ColumnType.DATETIME == dbColumn.getType()){
			return rs.getTimestamp(dbColumn.getName());
		}else if(ColumnType.DATE == dbColumn.getType()){
			return rs.getDate(dbColumn.getName());
		}else if(ColumnType.BLOB == dbColumn.getType() || ColumnType.LONGBLOB == dbColumn.getType()){
			return rs.getBinaryStream(dbColumn.getName());
		}else{//set as string
			return rs.getString(dbColumn.getName());
		}
	}
	public static Object getParamAsJavaObj(DBColumn dbColumn,ResultSet rs) throws SQLException{
		if(dbColumn == null || rs == null )
			return null;
		if(ColumnType.INT == dbColumn.getType() || ColumnType.TINYINT == dbColumn.getType()  || ColumnType.SMALLINT == dbColumn.getType()){
			int i = rs.getInt(dbColumn.getName());
			if (rs.wasNull())
				i=Misc.getUndefInt();
			return new Integer(i);
		}else if(ColumnType.MEDIUMINT == dbColumn.getType() || ColumnType.BIGINT == dbColumn.getType()){
			Long i = rs.getLong(dbColumn.getName());
			if (rs.wasNull())
				i= (long) Misc.getUndefInt();
			return new Long(i);
		}else if(ColumnType.DOUBLE == dbColumn.getType()){
			Double i = rs.getDouble(dbColumn.getName());
			if (rs.wasNull())
				i=Misc.getUndefDouble();
			return new Double(i);
		}else if(ColumnType.FLOAT == dbColumn.getType()){
			Float i = rs.getFloat(dbColumn.getName());
			if (rs.wasNull())
				return Misc.getUndefDouble();
			return new Float(i);
		}else if(ColumnType.TIMESTAMP == dbColumn.getType() || ColumnType.DATETIME == dbColumn.getType()){
			Timestamp ts = rs.getTimestamp(dbColumn.getName());
			return ts == null ? null : new Date(ts.getTime());
		}else if(ColumnType.DATE == dbColumn.getType()){
			java.sql.Date dt = rs.getDate(dbColumn.getName());
			return dt == null ? null : new Date(dt.getTime());
		}else if(ColumnType.BLOB == dbColumn.getType() || ColumnType.LONGBLOB == dbColumn.getType()){
			return rs.getBinaryStream(dbColumn.getName());
		}else{//set as string
			return rs.getString(dbColumn.getName());
		}
	}
	public static void setParam(DBColumn dbColumn,PreparedStatement ps, int index,Object val) throws SQLException{
		if(dbColumn == null || ps == null || index < 1)
			return;
		if(val == null){
			ps.setNull(index, dbColumn.getDataType());
		}else{
			ps.setObject(index, val, dbColumn.getDataType());	
		}
	}

	public static class Table{
		private ArrayList<DBColumn> colums;
		private DBColumn generatedKey;
		private ArrayList<DBColumn> uniqueKeys;
		private ArrayList<Trigger> triggers;
		private boolean hasUpdatedOn;
		private boolean hasUpdTrigger;
		private boolean hasInsTrigger;
		private boolean hasRemoteId;
		private String name;


		public DBColumn getColumnByName(String name){
			for (int i = 0,is=getColums() == null ? 0 : getColums().size(); i < is; i++) {
				if(getColums().get(i).getName().equalsIgnoreCase(name))
					return getColums().get(i);
			}
			return null;
		}

		public Table() {
			super();
		}
		public Table(Table ref) {
			super();
			if(ref == null)
				return;
			ArrayList<DBColumn> colums = null;
			ArrayList<DBColumn> uniqueKeys = null;
			ArrayList<Trigger> triggers = null;
			for (int i = 0,is= ref.getColums() == null ? 0 : ref.getColums().size() ; i < is; i++) {
				if(colums == null)
					colums = new ArrayList<DBSchemaManager.DBColumn>();
				colums.add(new DBColumn(ref.getColums().get(i)));
			}
			this.colums = colums;
			for (int i = 0,is= ref.getUniqueKeys() == null ? 0 : ref.getUniqueKeys().size() ; i < is; i++) {
				if(uniqueKeys == null)
					uniqueKeys = new ArrayList<DBSchemaManager.DBColumn>();
				uniqueKeys.add(new DBColumn(ref.getUniqueKeys().get(i)));
			}
			this.uniqueKeys = uniqueKeys;
			for (int i = 0,is= ref.getTriggers() == null ? 0 : ref.getTriggers().size() ; i < is; i++) {
				if(triggers == null)
					triggers = new ArrayList<DBSchemaManager.Trigger>();
				triggers.add(new Trigger(ref.getTriggers().get(i)));
			}
			this.triggers = triggers;
			this.generatedKey = new DBColumn(ref.getGeneratedKey());
			this.hasUpdatedOn = ref.isHasUpdatedOn();
			this.hasUpdTrigger= ref.isHasUpdTrigger();
			this.hasUpdTrigger = ref.isHasUpdTrigger();
			this.hasRemoteId = ref.isHasRemoteId();
		}
		public Table(ArrayList<DBColumn> colums, DBColumn generatedKey, ArrayList<DBColumn> uniqueKeys) {
			super();
			this.colums = colums;
			this.generatedKey = generatedKey;
			this.uniqueKeys = uniqueKeys;
		}
		public Table(ArrayList<DBColumn> colums, DBColumn generatedKey, ArrayList<DBColumn> uniqueKeys,
				ArrayList<Trigger> triggers) {
			super();
			this.colums = colums;
			this.generatedKey = generatedKey;
			this.uniqueKeys = uniqueKeys;
			this.triggers = triggers;
		}

		public Table(ArrayList<DBColumn> colums, DBColumn generatedKey, ArrayList<DBColumn> uniqueKeys,
				ArrayList<Trigger> triggers, boolean hasUpdatedOn, boolean hasUpdTrigger, boolean hasInsTrigger) {
			super();
			this.colums = colums;
			this.generatedKey = generatedKey;
			this.uniqueKeys = uniqueKeys;
			this.triggers = triggers;
			this.hasUpdatedOn = hasUpdatedOn;
			this.hasUpdTrigger = hasUpdTrigger;
			this.hasInsTrigger = hasInsTrigger;
		}

		public Table(ArrayList<DBColumn> colums, DBColumn generatedKey, ArrayList<DBColumn> uniqueKeys,
				ArrayList<Trigger> triggers, boolean hasUpdatedOn, boolean hasUpdTrigger, boolean hasInsTrigger,
				boolean hasRemoteId) {
			super();
			this.colums = colums;
			this.generatedKey = generatedKey;
			this.uniqueKeys = uniqueKeys;
			this.triggers = triggers;
			this.hasUpdatedOn = hasUpdatedOn;
			this.hasUpdTrigger = hasUpdTrigger;
			this.hasInsTrigger = hasInsTrigger;
			this.hasRemoteId = hasRemoteId;
		}
		public Table(ArrayList<DBColumn> colums, DBColumn generatedKey, ArrayList<DBColumn> uniqueKeys,
				ArrayList<Trigger> triggers, boolean hasUpdatedOn, boolean hasUpdTrigger, boolean hasInsTrigger,
				boolean hasRemoteId, String name) {
			super();
			this.colums = colums;
			this.generatedKey = generatedKey;
			this.uniqueKeys = uniqueKeys;
			this.triggers = triggers;
			this.hasUpdatedOn = hasUpdatedOn;
			this.hasUpdTrigger = hasUpdTrigger;
			this.hasInsTrigger = hasInsTrigger;
			this.hasRemoteId = hasRemoteId;
			this.name = name;
		}
		public ArrayList<DBColumn> getColums() {
			return colums;
		}
		public void setColums(ArrayList<DBColumn> colums) {
			this.colums = colums;
		}
		public DBColumn getGeneratedKey() {
			return generatedKey;
		}
		public void setGeneratedKey(DBColumn generatedKey) {
			this.generatedKey = generatedKey;
		}
		public ArrayList<DBColumn> getUniqueKeys() {
			return uniqueKeys;
		}
		public void setUniqueKeys(ArrayList<DBColumn> uniqueKeys) {
			this.uniqueKeys = uniqueKeys;
		}
		public ArrayList<Trigger> getTriggers() {
			return triggers;
		}
		public void setTriggers(ArrayList<Trigger> triggers) {
			this.triggers = triggers;
		}
		public boolean isHasUpdatedOn() {
			return hasUpdatedOn;
		}
		public void setHasUpdatedOn(boolean hasUpdatedOn) {
			this.hasUpdatedOn = hasUpdatedOn;
		}
		public boolean isHasUpdTrigger() {
			return hasUpdTrigger;
		}
		public void setHasUpdTrigger(boolean hasUpdTrigger) {
			this.hasUpdTrigger = hasUpdTrigger;
		}
		public boolean isHasInsTrigger() {
			return hasInsTrigger;
		}
		public void setHasInsTrigger(boolean hasInsTrigger) {
			this.hasInsTrigger = hasInsTrigger;
		}
		public boolean isHasRemoteId() {
			return hasRemoteId;
		}
		public void setHasRemoteId(boolean hasRemoteId) {
			this.hasRemoteId = hasRemoteId;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		private static Table dataSyncStatus = null;
		private static final String  dataTableSyncStatus = "data_table_sync_status";
		public static Table getSyncStatusSchema(){
			if(dataSyncStatus == null){
				dataSyncStatus = new Table();
				dataSyncStatus.setName(dataTableSyncStatus);
				dataSyncStatus.setColums(new ArrayList<DBSchemaManager.DBColumn>(Arrays.asList(
						new DBColumn("table_name", ColumnType.VARCHAR, 128, false, false, 12,dataTableSyncStatus),
						new DBColumn("last_read_at", ColumnType.DATETIME, Misc.getUndefInt(), false, false, 93,dataTableSyncStatus),
						new DBColumn("last_write_on", ColumnType.DATETIME, Misc.getUndefInt(), false, false, 93,dataTableSyncStatus)
						)));
				dataSyncStatus.setUniqueKeys(new ArrayList<DBSchemaManager.DBColumn>(Arrays.asList(new DBColumn("table_name", ColumnType.INT, 11, false, false, 4,dataTableSyncStatus))));
			}
			return dataSyncStatus;
		}
	}
	public static class Trigger{
		private static final Trigger beforeUpdate = new Trigger(null, null, null, 
				new StringBuilder()
				.append("BEGIN\n")
				.append("set new.updated_on = now();\n")
				/*.append("IF new."+RECORD_SRC+" is null then\n")
				.append("set new."+RECORD_SRC+"="+Integer.MAX_VALUE+";\n")
				.append("END IF")*/
				.append("END\n")
				, EventManipulation.UPDATE, ActionTiming.BEFORE, ActionOrientation.ROW);
		private static final Trigger beforeInsert = new Trigger(null, null, null, 
				new StringBuilder()
				.append("BEGIN\n")
				.append("set new.updated_on = now();\n")
				/*.append("IF new."+RECORD_SRC+" is null then\n")
				.append("set new."+RECORD_SRC+"="+Integer.MAX_VALUE+";\n")
				.append("END IF")*/
				.append("END\n")
				, EventManipulation.INSERT, ActionTiming.BEFORE, ActionOrientation.ROW);

		public static Trigger getOnUpdateTrigger(String tableName,String schemaName){
			return new Trigger(beforeUpdate).setTable(tableName).setSchema(schemaName).setName("upd_upo_upd_"+tableName);
		}
		public static Trigger getOnInsertTrigger(String tableName,String schemaName){
			return new Trigger(beforeInsert).setTable(tableName).setSchema(schemaName).setName("upd_upo_ins_"+tableName);
		}
		public static enum EventManipulation{
			INSERT,
			UPDATE
		}
		public static enum ActionTiming{
			BEFORE,
			AFTER
		}
		public static enum ActionOrientation{
			ROW,
			COLUMN
		}
		private String name;
		private String schema;
		private String table;
		private StringBuilder actionStatement;
		private EventManipulation eventManipulation;
		private ActionTiming actionTiming;
		private ActionOrientation actionOrientation;
		public Trigger() {
			super();
		}

		public Trigger(String name, String schema, String table, StringBuilder actionStatement,
				EventManipulation eventManipulation, ActionTiming actionTiming, ActionOrientation actionOrientation) {
			super();
			this.name = name;
			this.schema = schema;
			this.table = table;
			this.actionStatement = actionStatement;
			this.eventManipulation = eventManipulation;
			this.actionTiming = actionTiming;
			this.actionOrientation = actionOrientation;
		}
		public Trigger(Trigger ref) {
			super();
			this.name = ref.name;
			this.schema = ref.schema;
			this.table = ref.table;
			this.actionStatement = ref.actionStatement;
			this.eventManipulation = ref.eventManipulation;
			this.actionTiming = ref.actionTiming;
			this.actionOrientation = ref.actionOrientation;
		}
		public String getSchema() {
			return schema;
		}
		public Trigger setSchema(String schema) {
			this.schema = schema;
			return this;
		}
		public String getTable() {
			return table;
		}
		public Trigger setTable(String table) {
			this.table = table;
			return this;
		}
		public StringBuilder getActionStatement() {
			return actionStatement;
		}
		public Trigger setActionStatement(StringBuilder actionStatement) {
			this.actionStatement = actionStatement;
			return this;
		}
		public EventManipulation getEventManipulation() {
			return eventManipulation;
		}
		public Trigger setEventManipulation(EventManipulation eventManipulation) {
			this.eventManipulation = eventManipulation;
			return this;
		}
		public ActionTiming getActionTiming() {
			return actionTiming;
		}
		public Trigger setActionTiming(ActionTiming actionTiming) {
			this.actionTiming = actionTiming;
			return this;
		}
		public ActionOrientation getActionOrientation() {
			return actionOrientation;
		}
		public Trigger setActionOrientation(ActionOrientation actionOrientation) {
			this.actionOrientation = actionOrientation;
			return this;
		}
		public String getName() {
			return name;
		}
		public Trigger setName(String name) {
			this.name = name;
			return this;
		}
	}
	public static Table getTable(Connection conn, String tableName,String schema) throws Exception {
		if(tableName == null || tableName.length() <= 0)
			return null;
		boolean hasUpdatedOn = false;
		boolean hasUpdTrigger = false;
		boolean hasInsTrigger = false;
		boolean hasRemoteId = false;
		ArrayList<DBColumn> colList = null;
		DBColumn generatedKey = null;
		ArrayList<DBColumn> uniqueKeys = null;
		ArrayList<Trigger> triggers = null;
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet resultSet = null;
		//ResultSet resultSet = meta.getIndexInfo(null, schema, tableName, true, false);//.getColumns(schema, null, tableName, "%"); 
		/*Set<String> uniqueColumn = null;
		while (resultSet.next()) {
			if(resultSet.getBoolean("NON_UNIQUE") == true)
				continue;
			if(uniqueColumn == null){
				uniqueColumn = new HashSet<String>();
			}
			uniqueColumn.add(resultSet.getString("COLUMN_NAME").toLowerCase());
		}*/
		resultSet = meta.getColumns(null, null, tableName, "%");
		while (resultSet.next()) {
			if(colList == null)
				colList = new ArrayList<DBColumn>();
			String columnName = resultSet.getString("COLUMN_NAME");
			ColumnType columnType = ColumnType.valueOf(resultSet.getString("TYPE_NAME"));
			int columnSize = Misc.getRsetInt(resultSet, "COLUMN_SIZE");
			boolean isGenrated = false;//resultSet.getBoolean("IS_AUTOINCREMENT");
			boolean isUnique = false;//uniqueColumn != null && uniqueColumn.contains(columnName);
			int dataType = Misc.getRsetInt(resultSet, "DATA_TYPE");
			DBColumn dbc = new DBColumn(columnName, columnType, columnSize,isGenrated,isUnique,dataType,tableName);
			if(isGenrated){
				generatedKey = dbc;
			}else if(isUnique){
				if(uniqueKeys == null)
					uniqueKeys = new ArrayList<DBSchemaManager.DBColumn>();
				uniqueKeys.add(dbc);
			}
			colList.add(dbc);
			if(dbc.getName().equalsIgnoreCase("updated_on"))
				hasUpdatedOn = true;
			if(dbc.getName().equalsIgnoreCase(TEMP_REMOTE_ID_COLUMN))
				hasRemoteId = true;
		}
		triggers = getTriggers(conn, schema, tableName);
		for (int i = 0,is =triggers == null ? 0 : triggers.size(); i < is; i++) {
			if(triggers.get(i).getName().equalsIgnoreCase("upd_upo_upd_"+tableName))
				hasUpdTrigger = true;
			if(triggers.get(i).getName().equalsIgnoreCase("upd_upo_ins_"+tableName))
				hasInsTrigger = true;
		}
		/*if(colList != null)
			System.out.println(colList);*/
		return colList == null ? null : new Table(colList, generatedKey, uniqueKeys,triggers,hasUpdatedOn,hasUpdTrigger,hasInsTrigger, hasRemoteId, tableName);
	}

	public static void getTriggerInfo(Connection conn,String tableName) throws SQLException{
		DatabaseMetaData dbmd = conn.getMetaData(); 
		ResultSet result = dbmd.getTables("%", tableName, "%", new String[]{ "TRIGGER" });
		ResultSetMetaData rm = result.getMetaData();
		for (int i = 0,is=rm == null ? 0 : rm.getColumnCount(); i < is; i++) {
			if(i>0){
				System.out.print("  ");
			}
			System.out.print(rm.getColumnName(i+1));
		}
		System.out.println();
		while (result.next()) {
			for (int i = 0,is=rm == null ? 0 : rm.getColumnCount(); i < is; i++) {
				if(i>0){
					System.out.println("  ");
				}
				System.out.print(result.getObject(i+1));
			}
			System.out.println();
		}
	}
	public static StringBuilder getCreateTableStatment(Connection conn, String table) throws SQLException {
		if(table == null || table.length() <= 0 )
			return null;
		StringBuilder retval = null;
		Statement s = conn.createStatement (ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		s.executeQuery ("SHOW CREATE TABLE " + table);
		ResultSet rs = s.getResultSet ();
		if (rs.next ()){
			if(retval == null)
				retval = new StringBuilder();
			retval.append(rs.getString("Create Table"));
		}
		rs.close();
		s.close();
		return retval;
	}
	public static void createTable(Connection conn,StringBuilder query) throws SQLException{
		if(query == null || query.toString().length() <= 0 )
			return;
		Statement s = conn.createStatement();
		s.execute(query.toString());
		s.close();
	}

	public static ArrayList<Trigger> getTriggers(Connection conn,String schemaName,String tableName) throws SQLException{
		if(schemaName == null || schemaName.length() <= 0 || tableName == null || tableName.length() <= 0)
			return null;
		ArrayList<Trigger> retval = null;
		PreparedStatement ps = conn.prepareStatement("select trigger_schema,trigger_name,event_manipulation,event_object_table,action_statement,action_orientation,action_timing from information_schema.TRIGGERS where trigger_schema like ? and event_object_table like ?");
		ps.setString(1, schemaName);
		ps.setString(2, tableName);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			if(retval == null)
				retval = new ArrayList<DBSchemaManager.Trigger>();
			retval.add(new Trigger(rs.getString("trigger_name"), rs.getString("trigger_schema"), rs.getString("event_object_table"), new StringBuilder(rs.getString("action_statement")), EventManipulation.valueOf(rs.getString("event_manipulation")), ActionTiming.valueOf(rs.getString("action_timing")), ActionOrientation.valueOf(rs.getString("action_orientation"))));
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
		return retval;
	}


	public static StringBuilder getCreateTriggerStatement(Trigger trigger){
		if(trigger == null )
			return null;
		StringBuilder query = new StringBuilder();
		query
		.append("CREATE TRIGGER "+trigger.getName()+" "+trigger.getActionTiming().name()+" "+trigger.getEventManipulation().name()+" "+" on "+trigger.getTable()+" \n")
		.append("FOR EACH ROW\n")
		.append(trigger.getActionStatement())
		;
		return query;
	}
	public static <T> Pair<java.sql.Timestamp,T> getList(Connection conn, Class<?> base, String schema) throws Exception{
		return getList(conn, base, schema, null);
	}
	public static <T> Pair<java.sql.Timestamp,T > getList(Connection conn, Class<?> base, String schema,StringBuilder query) throws Exception{
		return getList(conn, base, schema,query, null);
	}
	public static <T> Pair<java.sql.Timestamp,T> getList(Connection conn, Class<?> base, String schema, StringBuilder query, ArrayList<Pair<Object,Integer>> params) throws Exception{
		if(conn == null || base == null)
			return null;
		java.sql.Timestamp ts = null;
		String tableName = base.isAnnotationPresent(com.ipssi.rfid.db.Table.class)  ? ((com.ipssi.rfid.db.Table) base.getAnnotation(com.ipssi.rfid.db.Table.class)).value() : base.getSimpleName().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
		Table table = getTable(conn, tableName, schema);
		if(table == null || table.getColums() == null)
			return null;
		System.out.println("[Table]:"+tableName);
		if(query == null || query.length() <= 0 )
			query = getSelectStatment(table.getColums(), tableName).first;
		if(query == null || query.length() <= 0 )
			return null;

		PreparedStatement ps = conn.prepareStatement(query.toString().toLowerCase().replaceAll("select", "select now() read_timestamp, "));
		for (int i = 0,is= params == null ? 0 : params.size(); i < is; i++) {
			if(i >= ps.getParameterMetaData().getParameterCount())
				break;
			if(params.get(i) == null){
				ps.setNull(i+1, params.get(i).second);
			}else{
				ps.setObject(i+1, params.get(i).first,params.get(i).second);	
			}
		}
		System.out.println(ps.toString());
		ResultSet rs = ps.executeQuery();
		ArrayList<Pair<DBColumn,Field>> fieldColPair = getFieldColPair(base, table);
		ArrayList<T> list = null; 
		while(rs.next()){
			T item = readRowInObject(base, rs, fieldColPair);
			if(item != null){
				if(list == null)
					list = new ArrayList<T>();
				list.add(item);
			}
			if(ts == null)
				ts = rs.getTimestamp("read_timestamp");
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
		return list == null ? null : new Pair<Timestamp, T>(ts, (T)list);
	}
	public static <T> T readRowInObject(Class<?> base, ResultSet rs, ArrayList<Pair<DBColumn,Field>> fieldColPair) throws SQLException, InstantiationException, IllegalAccessException{
		if(rs == null)
			return null;
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		if(fieldColPair == null || fieldColPair.size() == 0 || colCount == 0)
			return null;
		Object retval = base.newInstance();
		for (int i = 0; i < colCount; i++) {
			String colName = rsmd.getColumnLabel(i+1);
			Field field = null;
			DBColumn dbColumn = null;
			for (int j = 0; j < fieldColPair.size(); j++) {
				if(fieldColPair.get(j).first.getName().equalsIgnoreCase(colName)){
					dbColumn = fieldColPair.get(j).first;
					field = fieldColPair.get(j).second;
					break;
				}
			}
			if(field != null){
				field.setAccessible(true);
				Object val = getParamAsJavaObj(dbColumn, rs);
				if(val instanceof Date && field.getType().isAssignableFrom(Long.TYPE)){
					field.set(retval, val == null ? new Long(Misc.getUndefInt()) : new Long(((Date)val).getTime()));
				}else{
					field.set(retval, val);
				}
			}
		}
		return (T)retval;
	}

	public static ArrayList<Pair<DBColumn,Field>> getFieldColPair(Class<?> base,Table table){
		if(base == null||table == null || table.getColums() == null || table.getColums().size() == 0)
			return null;
		ArrayList<Pair<DBColumn,Field>> retval = null;
		for(Field field : base.getDeclaredFields()){
			DBColumn dbColumn = table.getColumnByName(field.isAnnotationPresent(Column.class)  ? ((Column) field.getAnnotation(Column.class)).value() : field.getName().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase());
			if(dbColumn != null){
				if(retval == null)
					retval = new ArrayList<Pair<DBColumn,Field>>();
				retval.add(new Pair<DBSchemaManager.DBColumn, Field>(dbColumn, field));
			}
		}
		return retval;
	}
	/*public static class Vehicle{
		int id;
		String name;
	}*/
	public static void addRemoteDBUser(Connection conn) throws SQLException{
		PreparedStatement ps = conn.prepareStatement("select 1, case when Password = Password('redhat') then 1 else 0 end from mysql.user where user='root' and host='%'");
		ResultSet rs = ps.executeQuery();
		System.out.println("[DB][USER]['root'@'%']");
		boolean isExist = false;
		boolean isPassMatched = false;
		if(rs.next()){
			System.out.println("[DB][USER][EXIST]:true");
			isExist = true ;
			isPassMatched = 1 == Misc.getRsetInt(rs, 2);
		}
		Misc.closeRS(rs);
		StringBuilder create = new StringBuilder("CREATE USER 'root'@'%'");
		StringBuilder setPass = new StringBuilder("SET PASSWORD FOR 'root'@'%' = PASSWORD('redhat')");
		StringBuilder grant = new StringBuilder("GRANT ALL ON *.* TO 'root'@'%'");
		StringBuilder flush = new StringBuilder("FLUSH PRIVILEGES");
		if(!isExist){
			System.out.println("[DB][USER]['root'@'%']:create");
			ps = conn.prepareStatement(create.toString());
			ps.execute();
		}
		if(!isPassMatched){
			System.out.println("[DB][USER]['root'@'%']:setPass");
			ps = conn.prepareStatement(setPass.toString());
			ps.execute();
		}
		if(!isExist){
			System.out.println("[DB][USER]['root'@'%']:grant");
			ps = conn.prepareStatement(grant.toString());
			ps.execute();
			System.out.println("[DB][USER]['root'@'%']:flush");
			ps = conn.prepareStatement(flush.toString());
			ps.execute();
		}
		Misc.closePS(ps);
	}
	public static long updateVehicles(Connection fromConn, Connection toConn,
			Table fromTable,Table toTable,StringBuilder whereClause,String areaCode,int portNodeId) throws SQLException{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psSelectLocal = null;
//		PreparedStatement psInsertLocal = null;
		ResultSet rs = null;
		ResultSet rsLocal = null;
		HashSet<String> remoteSelColSet = new HashSet<String>();
		Pair<StringBuilder, ArrayList<DBColumn>> select = getSelectStatmentWithColSet(fromTable.getColums(), fromTable.getName(),false, remoteSelColSet);
		Pair<StringBuilder, ArrayList<DBColumn>> insert = getInsertWithColSet(toTable.getColums(), toTable.getName(),remoteSelColSet);
		StringBuilder selectQuery = select.first;
		if(whereClause != null && whereClause.toString() != null && whereClause.toString().length() > 0){
			selectQuery.append(" where "+whereClause.toString());
		}
		psSelect = fromConn.prepareStatement(selectQuery.toString());
		psInsert = toConn.prepareStatement(insert.first.toString(),PreparedStatement.RETURN_GENERATED_KEYS);
		psSelectLocal = toConn.prepareStatement("select id, customer_id, name from vehicle where (std_name like ? or name like ?) and status = 1 order by id desc limit 1");
//		psInsertLocal = toConn.prepareStatement("insert ignore into vehicle_access_groups values (?,?)");
		System.out.println(psSelect);
		rs = psSelect.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		HashSet<String> rsColSet = null;
		Timestamp readTimestamp = null;
		int count = 0;
		String stdName = null;
		int vehId = Misc.getUndefInt();
		int custId = Misc.getUndefInt();
		int custIdLocal = Misc.getUndefInt();
		boolean isVehExists = false;
		while(rs.next()){
			try {
				isVehExists = false;
				if(rsColSet == null){
					rsColSet = new HashSet<String>();
					for (int i = 0; i < colCount; i++) {
						rsColSet.add(rsmd.getColumnName(i+1));
					}
				}
				if(rsColSet.contains("updated_on")){
					Timestamp cTs = rs.getTimestamp("updated_on");
					if(readTimestamp == null)
						readTimestamp = cTs;
					else{
						if(cTs != null && cTs.getTime() > readTimestamp.getTime())
							readTimestamp = cTs;
					}
				}
				stdName = rs.getString("std_name");
				custId = Misc.getRsetInt(rs, "customer_id");
				psSelectLocal.setString(1, stdName);
				psSelectLocal.setString(2, stdName);
				rsLocal = psSelectLocal.executeQuery();
				if(rsLocal.next()){
					vehId = Misc.getRsetInt(rsLocal, "id");
					// stdName = rsLocal.getString("std_name");
					custIdLocal = Misc.getRsetInt(rsLocal, "customer_id");
					isVehExists = true;
				}
				Misc.closeRS(rsLocal);
				/*if(!Misc.isUndef(custIdLocal) && !Misc.isUndef(portNodeId) && portNodeId != custIdLocal){
					psInsertLocal.setInt(1, vehId);
					psInsertLocal.setInt(2, portNodeId);
					System.out.println(psInsertLocal.toString());
					psInsertLocal.addBatch();
				}*/
				
				if(!isVehExists){
					int psIndex = 1;
					for (int i = 0,is=insert == null || insert.second == null ? 0 : insert.second.size() ; i < is; i++) {
						if(DBSchemaManager.RECORD_SRC.equalsIgnoreCase(insert.second.get(i).getName())){
							Misc.setParamInt(psInsert, Integer.MAX_VALUE, psIndex++);
							continue;
						}
						if("area_code".equalsIgnoreCase(insert.second.get(i).getName())){
							psInsert.setString(psIndex++, areaCode);
							continue;
						}
						if(rsColSet.contains(insert.second.get(i).getName())){
							DBSchemaManager.setParam(insert.second.get(i), psInsert, psIndex++, DBSchemaManager.getParam(insert.second.get(i),rs));
						}
					}
					System.out.println(psInsert.toString());
					psInsert.addBatch();
					count++;
					if(count%100 ==0){
						/*if(psInsertLocal != null){
							psInsertLocal.executeBatch();
							psInsertLocal.clearBatch();
							toConn.commit();
						}*/
						
						psInsert.executeBatch();
						psInsert.clearBatch();
						toConn.commit();	
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*if(psInsertLocal != null)
			psInsertLocal.executeBatch();*/
			
		if(psInsert != null)
			psInsert.executeBatch();
		toConn.commit();
		System.out.println("DBSchemaManager.updateVehicles() Tot rows: "+count);
		Misc.closeRS(rs);
		Misc.closePS(psSelect);
		Misc.closePS(psInsert);
		return readTimestamp == null ? Misc.getUndefInt() : readTimestamp.getTime();
	}
	public static long copyDataInsertIgnoreUpdate(Connection fromConn, Connection toConn,
			Table fromTable,Table toTable,StringBuilder whereClause,String areaCode) throws SQLException{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		PreparedStatement psSelectCount = null;
		ResultSet rs = null;
		Timestamp readTimestamp = null;
		HashSet<String> remoteSelColSet = new HashSet<String>();
		Pair<StringBuilder, ArrayList<DBColumn>> select = getSelectStatmentWithColSet(fromTable.getColums(), fromTable.getName(),false,remoteSelColSet);
		remoteSelColSet.add("area_code");
		Pair<StringBuilder, ArrayList<DBColumn>> insert = getInsertOnDuplicateUpd(toTable.getColums(), toTable.getName(),remoteSelColSet);
		StringBuilder selectQuery = select.first;
		String selCount = " select count(*) ct from "+fromTable.getName()+" t0 ";
		if(whereClause != null && whereClause.toString() != null && whereClause.toString().length() > 0){
			selCount = selCount +" where "+whereClause.toString();
		}
		psSelectCount = fromConn.prepareStatement(selCount.toString());
		rs = psSelectCount.executeQuery();
		int selCot = 1000;
		while(rs.next()){
			selCot = Misc.getRsetInt(rs,"ct");
		}
		System.out.println("selCot : " + selCot);
		int forCnt = selCot/1000;
		forCnt++;
		
		
		if(whereClause != null && whereClause.toString() != null && whereClause.toString().length() > 0){
			selectQuery.append(" where "+whereClause.toString());
		}
		int count = 0;
		int countDone = 0;
		for (int j = 1; j <= forCnt; j++) {
			StringBuilder selQuery = new StringBuilder(selectQuery);
			selQuery.append("  order by t0.updated_on asc limit "+countDone+" , " + 1000);
			/*if(j == forCnt){
				selQuery.append(" order by t0.updated_on ");
			}else {
				selQuery.append("  order by t0.updated_on limit "+countDone+" , " + 1000);
			}*/
			// .append("'  order by t0.updated_on limit 1000 ")
			psSelect = fromConn.prepareStatement(selQuery.toString());
			psInsert = toConn.prepareStatement(insert.first.toString(),PreparedStatement.RETURN_GENERATED_KEYS);
			System.out.println(psSelect);
			rs = psSelect.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			HashSet<String> rsColSet = null;
			// readTimestamp = null;
			count = 0;
			while(rs.next()){
				try {
					if(rsColSet == null){
						rsColSet = new HashSet<String>();
						for (int i = 0; i < colCount; i++) {
							rsColSet.add(rsmd.getColumnName(i+1));
						}
					}
					if(rsColSet.contains("updated_on")){
						Timestamp cTs = rs.getTimestamp("updated_on");
						if(readTimestamp == null)
							readTimestamp = cTs;
						else{
							if(cTs != null && cTs.getTime() > readTimestamp.getTime())
								readTimestamp = cTs;
						}
					}
					if(true){
						int psIndex = 1;
						for (int i = 0,is=insert == null || insert.second == null ? 0 : insert.second.size() ; i < is; i++) {
							if(DBSchemaManager.RECORD_SRC.equalsIgnoreCase(insert.second.get(i).getName())){
								Misc.setParamInt(psInsert, Integer.MAX_VALUE, psIndex++);
								continue;
							}
							if("area_code".equalsIgnoreCase(insert.second.get(i).getName())){
								psInsert.setString(psIndex++, areaCode);
								continue;
							}
							if(rsColSet.contains(insert.second.get(i).getName())){
								DBSchemaManager.setParam(insert.second.get(i), psInsert, psIndex++, DBSchemaManager.getParam(insert.second.get(i),rs));
							}
						}
						System.out.println(psInsert.toString());
						psInsert.addBatch();
						count++;
						/*if(count%4 ==0){
							psInsert.executeBatch();
							psInsert.clearBatch();
							toConn.commit();	
						}*/
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(psInsert != null){
				psInsert.executeBatch();
				psInsert.clearBatch();
				toConn.commit();	
				fromConn.commit();
				countDone = countDone + count;
			}
			System.out.println("DBSchemaManager.copyDataInsertIgnoreUpdate() Tot rows: "+countDone);
			Misc.closeRS(rs);
			Misc.closePS(psSelect);
			Misc.closePS(psInsert);
		}
		return readTimestamp == null ? Misc.getUndefInt() : readTimestamp.getTime();
	}
	public static DBObjectCursor fetch(Connection conn, Class<?> base, String schema, StringBuilder query, ArrayList<Pair<Object,Integer>> params) throws Exception{
		if(conn == null || base == null)
			return new DBObjectCursor(base, null, null, null);
		String tableName = base.isAnnotationPresent(com.ipssi.rfid.db.Table.class)  ? ((com.ipssi.rfid.db.Table) base.getAnnotation(com.ipssi.rfid.db.Table.class)).value() : base.getSimpleName().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
		Table table = getTable(conn, tableName, schema);
		if(table == null || table.getColums() == null)
			return new DBObjectCursor(base, null, null, null);
		System.out.println("[Table]:"+tableName);
		if(query == null || query.length() <= 0 )
			query = getSelectStatment(table.getColums(), tableName).first;
		if(query == null || query.length() <= 0 )
			return new DBObjectCursor(base, null, null, null);
		PreparedStatement ps = conn.prepareStatement(query.toString().toLowerCase().replaceAll("select", "select now() read_timestamp, "));
		for (int i = 0,is= params == null ? 0 : params.size(); i < is; i++) {
			if(i >= ps.getParameterMetaData().getParameterCount())
				break;
			if(params.get(i) == null){
				ps.setNull(i+1, params.get(i).second);
			}else{
				ps.setObject(i+1, params.get(i).first,params.get(i).second);	
			}
		}
		System.out.println(ps.toString());
		ResultSet rs = ps.executeQuery();
		return new DBObjectCursor(base, ps, rs, getFieldColPair(base, table));
	}
	public static class DBObjectCursor{
		private final Class<?> base;
		private final PreparedStatement ps;
		private final ResultSet rs;
		private final ArrayList<Pair<DBColumn,Field>> fieldColPair;
		private boolean isNext = false;
		private Timestamp readTimestamp = null;

		public DBObjectCursor(Class<?> base, PreparedStatement ps, ResultSet rs, ArrayList<Pair<DBColumn, Field>> fieldColPair) {
			super();
			this.base = base;
			this.ps = ps;
			this.rs = rs;
			this.fieldColPair = fieldColPair;
		}
		public boolean next() throws SQLException{
			isNext = rs != null && rs.next();
			return isNext;
		}

		public <T> T read() throws Exception{
			if(!isNext)
				throw new Exception("Cusor out of bounds");
			return readRowInObject(base, rs, fieldColPair);
		}
		public Timestamp getReadTS() throws Exception{
			if(!isNext)
				throw new Exception("Cusor out of bounds");
			if(readTimestamp == null)
				readTimestamp = rs.getTimestamp("read_timestamp");
			return readTimestamp;
		}
		public void close(){
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
	}
}

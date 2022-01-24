package com.ipssi.rfid.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRWeighmentRecord;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.GenratedTime;
import com.ipssi.rfid.db.Table.JOIN;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.ReadOnly;
import com.ipssi.rfid.db.Table.Tag;
import com.ipssi.rfid.db.Table.Unique;


public class RFIDMasterDao {
	public static final int INSERT = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
	public static final int SELECT = 3;
	
	private static HashMap<Class,ArrayList<DatabaseColumn>> classFieldMap = new HashMap<Class, ArrayList<DatabaseColumn>>();
	private static ConcurrentHashMap<String, Field[]> fieldMap = new ConcurrentHashMap<String, Field[]>();
	public static boolean executeQuery(Connection conn, String query) throws Exception{
		boolean retval = false;
		PreparedStatement ps = null;
		try{//generic_params
			if(query != null && query.length() > 0){
				ps = conn.prepareStatement(query);
				ps.executeUpdate();
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
		}
		return retval;
	}
	public static int getRowCount(Connection conn, String query) throws Exception{
		int retval = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{//generic_params
			if(query != null && query.length() > 0){
				ps = conn.prepareStatement(query);
				rs = ps.executeQuery();
				while(rs.next()){
					retval++;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			if(ps != null)
				ps.close();
		}
		return retval;
	}
	public static Object get(Connection conn, Class<?> objType , int id) throws Exception{
		return get(conn, objType, id, false);
	}
	public static Object get(Connection conn, Class<?> objType , int id, boolean apprvd) throws Exception{
		return get(conn, objType, id, apprvd,null);
	}
	public static Object get(Connection conn, Class<?> objType , int id, boolean apprvd, Object obj) throws Exception{
		if(Misc.isUndef(id))
			return null;
		long st = System.currentTimeMillis();
		Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		try{
			if(obj == null)
				obj = objType.newInstance();
			setPrimaryValue(obj, id);
			queryPair = getGeneralQuery(conn,SELECT, obj, apprvd, null);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				if (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return dataBean;
	}
	public static Object getSingle(Connection conn,Class<?> base,Criteria criteria) throws Exception{
		if(base == null)
			return null;
		long st = System.currentTimeMillis();
		Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		try{
			if(criteria == null)
				criteria = new Criteria(base);
			criteria.setLimit(1);
			queryPair = getGeneralQuery(conn,SELECT, base.newInstance(),false, criteria);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				if (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = base.newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return dataBean;
	}
	public static void main(String[] args) {
		TPRecord tpr = new TPRecord();
		tpr.setVehicleId(12331);
		tpr.setVehicleName("1321");
		tpr.setLatestLoadGateInExit(new Date());
		TPRecord clone = null;
		clone = (TPRecord) getClone(tpr);
		System.out.println();
	}
	private static StringBuilder getGeneralQuery(int type, String tableName,ArrayList<DatabaseColumn> cols,ArrayList<DatabaseColumn> clauses){
		StringBuilder retval = null;
		StringBuilder colStr = null;
		StringBuilder valueStr = null;
		StringBuilder whereStr = null;
		try{
			if(tableName != null && tableName.length() > 0 && cols != null && cols.size() > 0){
				if(type == INSERT){
					for(DatabaseColumn dc : cols){
						if(dc.isAuto() && dc.isNull())
							continue;
						if(retval == null){
							retval = new StringBuilder("");
							colStr = new StringBuilder("");
							valueStr = new StringBuilder("");
							retval.append("insert into ").append(tableName).append(" ");
						}
						else {
							colStr.append(", ");
							valueStr.append(", ");
						}
						colStr.append(dc.getColName());
						valueStr.append(dc.getColVal());
					}
					if(retval != null && colStr != null && valueStr != null){
						retval.append("(").append(colStr.toString()).append(") values (").append(valueStr).append(")");
					}
				}
				else if(type == UPDATE){
					for(DatabaseColumn dc : cols){
						if(retval == null){
							retval = new StringBuilder("");
							colStr = new StringBuilder("");
							retval.append("update ").append(tableName).append(" set ");
						}
						else {
							colStr.append(", ");
						}

						colStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
					}
					if(clauses != null  && clauses.size() > 0){
						for(DatabaseColumn dc : clauses){
							if(whereStr == null){
								whereStr = new StringBuilder("");
								whereStr.append(" where ");
							}
							else {
								whereStr.append(" and ");
							}
							whereStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
						}
					}
					if(retval != null && colStr != null){
						retval.append(colStr.toString());
						if(whereStr != null)
							retval.append(whereStr.toString());
					}
				}else if(type == SELECT){
					for(DatabaseColumn dc : cols){
						if(retval == null){
							retval = new StringBuilder("");
							colStr = new StringBuilder("");
							retval.append("select ");
						}
						else {
							colStr.append(", ");
						}

						colStr.append(tableName).append(".").append(dc.getColName());;
					}
					if(clauses != null  && clauses.size() > 0){
						for(DatabaseColumn dc : clauses){
							if(whereStr == null){
								whereStr = new StringBuilder("");
								whereStr.append(" where ");
							}
							else {
								whereStr.append(" and ");
							}
							whereStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
						}
					}
					if(retval != null && colStr != null){
						retval.append(colStr.toString()).append(" from ").append(tableName).append(" ");
						if(whereStr != null)
							retval.append(whereStr.toString());
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;

	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(Connection conn,int type, Object obj) throws IllegalArgumentException, IllegalAccessException{
		return getGeneralQuery(conn, type, obj, false,null);
	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(Connection conn, int type, Object obj, boolean isApprvd) throws IllegalArgumentException, IllegalAccessException{
		return getGeneralQuery(conn, type, obj, isApprvd,null);
	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(Connection conn, int type, Object obj, boolean isApprvd,Criteria criteria) throws IllegalArgumentException, IllegalAccessException{
		return getGeneralQuery(conn,type, obj, isApprvd,criteria,null);
	}
	private static Pair<StringBuilder, ArrayList<DatabaseColumn>> getGeneralQuery(Connection conn, int type, Object obj,boolean isApprvd,Criteria criteria,String tag) throws IllegalArgumentException, IllegalAccessException{
		StringBuilder query = new StringBuilder();
		StringBuilder colStr = new StringBuilder();
		StringBuilder valueStr = new StringBuilder();
		StringBuilder fromStr = new StringBuilder();
		StringBuilder whereStr = new StringBuilder();
		String tableName = null;
		Class<?> base = null;
		ArrayList<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();
//		try{
		if(obj != null){
			base = obj.getClass();
			if (base.isAnnotationPresent(Table.class)) {
				Annotation annotation = base.getAnnotation(Table.class);
				Table table  = (Table) annotation;
				if(table != null)
					tableName = isApprvd ? table.value()+"_apprvd" :  table.value();
				
			}
			getGeneralQuery(conn,type, obj, query, colStr, valueStr, fromStr, whereStr, columns, isApprvd, tag);
			if( colStr != null && colStr.length() > 0 ){
				if(type == INSERT)
					query.append("insert into ").append(tableName).append(" (").append(colStr.toString()).append(") values (").append(valueStr).append(")");
				else if(type == UPDATE){
					query.append("update ").append(tableName).append(" set ").append(colStr.toString());
					if(whereStr != null)
						query.append(" where ").append(whereStr.toString());
				}else if(type == SELECT){
					query.append("select ").append(colStr.toString()).append(",now() curr from ").append(tableName).append(" ").append(fromStr.toString());
					if(whereStr != null && whereStr.toString().length() > 0){
						query.append(" where ").append(whereStr.toString());
					}
					if(criteria != null ){
						if(criteria.getWhrClause() != null ){
							if(whereStr != null && whereStr.toString().length() > 0)
								query.append(" and ");
							else
								query.append(" where ");
							query.append(criteria.getWhrClause().toString());
						}
						if(criteria.getOrderByClause() != null && criteria.getOrderByClause().toString() != null && criteria.getOrderByClause().length() > 0){
							query.append(" order by ").append(criteria.getOrderByClause().toString());
							if(criteria.isDesc())
								query.append(" desc ");
							else
								query.append(" asc ");
							if(!Misc.isUndef(criteria.getLimit()))
								query.append(" limit ").append(criteria.getLimit());
						}
					}
				}
				System.out.println(Thread.currentThread().toString()+"[RFIDMaster Query]["+DBConnectionPool.getPrintableConnectionStr(conn)+"]:"+query.toString());
			}
		}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		return new Pair<StringBuilder, ArrayList<DatabaseColumn>>(query, columns);
	}
	private static void getGeneralQuery(Connection conn, int type, Object obj,StringBuilder query,StringBuilder colStr,StringBuilder valueStr,StringBuilder fromStr,StringBuilder whereStr,ArrayList<DatabaseColumn> columns ,boolean isApprvd,String tag) throws IllegalArgumentException, IllegalAccessException{
		String tableName = null;
		Class<?> base = null;
		int fieldType = 0;
		DatabaseColumn dc = null;
//		try{
			if(obj != null){
				base = obj.getClass();
				if (base.isAnnotationPresent(Table.class)) {
					Annotation annotation = base.getAnnotation(Table.class);
					Table table  = (Table) annotation;
					if(table != null)
						tableName = isApprvd ? table.value()+"_apprvd" :  table.value();
				}
				Field[] fields = getFields(conn,base,tableName);//base.getDeclaredFields();//fieldMap.get(base.getCanonicalName());
				for(int i=0,is=fields==null?0:fields.length;i<is;i++){
					Field field = fields[i];
					fieldType = DatabaseColumn.STRING;
					if(tag != null && tag.length() > 0 && field.isAnnotationPresent(Tag.class)){
						String tags  = ((Tag)field.getAnnotation(Tag.class)).value();
						if(tags != null && tags.length() > 0 && !(tags.contains(tag)))
							continue;
					}
					if (field.getType().isAssignableFrom(Integer.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(String.class)) {
						fieldType = DatabaseColumn.STRING;
					}else if (field.getType().isAssignableFrom(Date.class)) {
						fieldType = DatabaseColumn.DATE;
					}else if (field.getType().isAssignableFrom(Long.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(Float.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Double.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
						fieldType = DatabaseColumn.BOOLEAN;
					}
					else if(type == SELECT){
						if(field.isAnnotationPresent(JOIN.class) ) {
							Annotation annotation = field.getAnnotation(JOIN.class);
							JOIN join = (JOIN) annotation;
							field.setAccessible(true);
							Object child = field.get(obj);
							if(fromStr != null)
								fromStr.append(" join ").append(join.entity())
								.append(" on ").append(" (")
								.append(tableName).append(".").append(join.parentCol())
								.append("=")
								.append(join.entity()).append(".").append(join.childCol())
								.append(") ");
							getGeneralQuery(conn,fieldType, child, query, colStr, valueStr, fromStr, whereStr, columns,false,null);
							continue;
						}
					}
					
					if (field.isAnnotationPresent(Column.class) ) {
						Annotation annotation = field.getAnnotation(Column.class);
						Column col = (Column) annotation;
						field.setAccessible(true);
						dc = new DatabaseColumn(fieldType, col.value(), field.get(obj));
						dc.setField(field);
						if (field.isAnnotationPresent(KEY.class)) {
							dc.setKey(true);
						}
						if (field.isAnnotationPresent(GENRATED.class)) {
							dc.setAuto(true);
						}
						
						if(type == INSERT){
							if(field.isAnnotationPresent(ReadOnly.class)){
								continue;
							}
							if (field.isAnnotationPresent(GenratedTime.class)) {
								dc.setGeneratedTime(true);
							}
							if(dc.isAuto() && !isApprvd){
								if(columns == null)
									columns = new ArrayList<DatabaseColumn>();
								columns.add(dc);
								continue;
							}
							if(query == null){
								query = new StringBuilder("");
								colStr = new StringBuilder("");
								valueStr = new StringBuilder("");
								query.append("insert into ").append(tableName).append(" ");
							}
							else if(colStr.length() > 0) {
								colStr.append(", ");
								valueStr.append(", ");
							}
							colStr.append(dc.getColName());
							valueStr.append(dc.getColVal());
						}
						else if(type == UPDATE){
							if(field.isAnnotationPresent(ReadOnly.class)){
								continue;
							}
							if(!dc.isNull() && dc.isKey()){
								if(whereStr == null ){
									whereStr = new StringBuilder("");
									whereStr.append(" where ");
								}
								else if(whereStr.length() > 0) {
									whereStr.append(" and ");
								}
								whereStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
								continue;
							}
							if(query == null){
								query = new StringBuilder("");
								colStr = new StringBuilder("");
								query.append("update ").append(tableName).append(" set ");
							}
							else if(colStr.length() > 0) {
								colStr.append(", ");
							}
							colStr.append(dc.getColName()).append(" = ").append(dc.getColVal());
							
			
						}else if(type == SELECT){
							if(columns == null)
								columns = new ArrayList<DatabaseColumn>();
							columns.add(dc);
							if(query == null){
								query = new StringBuilder("");
								colStr = new StringBuilder("");
								fromStr = new StringBuilder(" from ").append(tableName).append(" ");
								query.append("select ");
							}
							else if(colStr.length() > 0) {
								colStr.append(", ");
							}
							colStr.append(tableName).append(".").append(dc.getColName());;
							if(!dc.isNull()){
								if(whereStr == null){
									whereStr = new StringBuilder("");
									whereStr.append(" where ");
								}
								else if(whereStr.length() > 0){
									whereStr.append(" and ");
								}
								whereStr.append(tableName).append(".").append(dc.getColName()).append("=").append(dc.getColVal());
							}
						}
					}
				}
			}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		//return new Pair<StringBuilder, ArrayList<DatabaseColumn>>(query, columns);
	}
    public static ArrayList<Object> select(Connection conn, Object obj) throws Exception{
    	return select(conn, obj,null); 
    }
    public static ArrayList<?> getList(Connection conn, Object obj, Criteria criteria) throws Exception{
    	return getList(conn, obj,criteria,false); 
    }
    public static ArrayList<?> getList(Connection conn, Object obj,Criteria criteria,boolean apprvd) throws Exception{
		long st = System.currentTimeMillis();
    	Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		try{
			queryPair = getGeneralQuery(conn,SELECT, obj,apprvd, criteria);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				while (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
					retval.add(dataBean);
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return retval;
	}
    public static Pair<ArrayList<?>,Long> getListWithReadTimeStamp(Connection conn, Object obj,Criteria criteria,List<Class<? extends Annotation>> skipAnnotedKeys) throws Exception{
    	return getListWithReadTimeStamp(conn, obj, criteria, skipAnnotedKeys,false);
    }
    public static Pair<ArrayList<?>,Long> getListWithReadTimeStamp(Connection conn, Object obj,Criteria criteria,List<Class<? extends Annotation>> skipAnnotedKeys, boolean apprvd) throws Exception{
		long st = System.currentTimeMillis();
    	Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		Long readTimeStamp = null;
		try{
			queryPair = getGeneralQuery(conn, SELECT, obj,apprvd, criteria);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				while (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    if(readTimeStamp == null)
				    	readTimeStamp = Misc.getDateInLong(rs, "curr");
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 if (skipAnnotedKeys != null && skipAnnotedKeys.size() >0 && dc.getField() != null) {
								 boolean skip = false;
								 for(Class<? extends Annotation> key : skipAnnotedKeys){
									 if(dc.getField().isAnnotationPresent(key)){
										 skip = true;
										 break;
									 }
								 }
								 if(skip)
									 continue;
							 }
							 setRsetValue(rs,dc,dataBean);
						 }
					}
					retval.add(dataBean);
				}
				//retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return new Pair<ArrayList<?>, Long>(retval,readTimeStamp);
	}
    public static ArrayList<Object> select(Connection conn, Object obj,Criteria criteria) throws Exception{
		return select(conn, obj, criteria, false);
	}
	public static ArrayList<Object> select(Connection conn, Object obj,Criteria criteria, boolean apprvd) throws Exception{
		long st = System.currentTimeMillis();
		Object dataBean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		ArrayList<Object> retval = null;
		try{
			queryPair = getGeneralQuery(conn, SELECT, obj, apprvd, criteria);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				rs = ps.executeQuery();
				while (rs.next()){
				    if(retval == null)
				    	retval = new ArrayList<Object>();
				    dataBean = obj.getClass().newInstance();
					if(queryPair.second != null && queryPair.second.size() > 0){
						 for(DatabaseColumn dc : queryPair.second){
							 setRsetValue(rs,dc,dataBean);
						 }
					}
					retval.add(dataBean);
				}
				//retval = true;
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return retval;
	}
	private static void setRsetValue(ResultSet rs,DatabaseColumn dc,Object parent){
//		try{
			Field field = dc.getField();
			if (field.getType().isAssignableFrom(Integer.TYPE) || 
				field.getType().isAssignableFrom(String.class) || 
				field.getType().isAssignableFrom(Date.class) ||
				field.getType().isAssignableFrom(Long.TYPE) ||
				field.getType().isAssignableFrom(Float.TYPE) ||
				field.getType().isAssignableFrom(Double.TYPE) ||
				field.getType().isAssignableFrom(Boolean.TYPE)){
				dc.setValue(parent,rs);
			}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
	public static boolean insertList(Connection conn, ArrayList<?> list) throws Exception{
		return insertList(conn, list, false);
	}
	public static boolean insertList(Connection conn, ArrayList<?> list, boolean isApprvd) throws Exception{
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		try{
			for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
				queryPair = getGeneralQuery(conn, INSERT, list.get(i),isApprvd);
				if(queryPair == null || queryPair.first == null)
					continue;
				if(ps == null)
					ps = conn.prepareStatement(queryPair.first.toString());
				ps.addBatch(queryPair.first.toString());
			}
			if(ps != null){
				ps.executeBatch();
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public static boolean insertAndDelete(Connection conn, ArrayList<?> list) throws Exception{
		return insertAndDelete(conn, list, null);
	}
	public static boolean insertAndDelete(Connection conn, ArrayList<?> list,Class<? extends Annotation> key) throws Exception{
		long st = System.currentTimeMillis();
		boolean retval = false;
		PreparedStatement insertPs = null;
		PreparedStatement deletePs = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		Field primaryField = null;
		DatabaseColumn dc = null;
		String tableName = null;
		try{
			if(list != null && list.size() > 0){
				primaryField = getFieldWithKey(list.get(0).getClass(),key);
				if(primaryField != null){
					primaryField.setAccessible(true);
					dc = getDBColumnFromField(primaryField);
					dc.setField(primaryField);
				}
			}
			for(int i=0,is=list == null ? 0 : list.size();i<is;i++){
				Object row = list.get(i); 
				if(dc != null)
					dc.setColVal(primaryField.get(row));
				queryPair = getGeneralQuery(conn, INSERT, row,false);
				if(queryPair == null || queryPair.first == null)
					continue;
				if(deletePs == null)
					deletePs = conn.prepareStatement("delete from "+getTableName(list.get(i).getClass()) +(dc != null ? " where "+dc.getColName()+"=?":""));
				if(dc != null){
					dc.setStatement(row, deletePs, 1);
					deletePs.addBatch();
				}
				if(insertPs == null)
					insertPs = conn.prepareStatement(queryPair.first.toString());
				insertPs.addBatch(queryPair.first.toString());
			}
			if(deletePs != null){
				retval = false;
				if(dc != null)
					deletePs.executeBatch();
				else 
					deletePs.executeUpdate();
				retval = true;
			}
			if(insertPs != null){
				insertPs.executeBatch();
				retval = true;
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(insertPs);
			Misc.closePS(deletePs);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return retval;
	}
	public static boolean insert(Connection conn, Object obj) throws Exception{
		return insert(conn, obj, false);
	}
	public static boolean insert(Connection conn, Object obj,boolean isApprvd) throws Exception{
		return insert(conn, obj, isApprvd,null);
	}
	public static boolean insert(Connection conn, Object obj, boolean isApprvd,String tag) throws Exception{
		long st = System.currentTimeMillis();
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		try{
			queryPair = getGeneralQuery(conn, INSERT, obj, isApprvd,null,tag);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if (rs.next()){
					if(queryPair.second != null && queryPair.second.size() > 0){
						queryPair.second.get(0).setColVal(Misc.getRsetInt(rs, 1));
						queryPair.second.get(0).setValue(obj);
					}
				}
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return retval;
	}
	public static boolean update(Connection conn, Object obj) throws Exception{
		return update(conn, obj, false);
	}
	public static boolean update(Connection conn, Object obj, boolean isApprvd) throws Exception{
		return update(conn, obj, isApprvd, null);
	}
	public static boolean update(Connection conn, Object obj, boolean isApprvd,String tag) throws Exception{
		long st = System.currentTimeMillis();
		boolean retval = false;
		PreparedStatement ps = null;
		Pair<StringBuilder,ArrayList<DatabaseColumn>> queryPair = null;
		try{//generic_params
			queryPair = getGeneralQuery(conn, UPDATE, obj, isApprvd, null, tag);
			if(queryPair != null && queryPair.first != null){
				ps = conn.prepareStatement(queryPair.first.toString());
				ps.executeUpdate();
				retval = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closePS(ps);
			System.out.println("Time : "+(System.currentTimeMillis()-st)+ " ms");
		}
		return retval;
	}
	public static ArrayList<DatabaseColumn> getDatabaseColumns(Object obj) throws IllegalArgumentException, IllegalAccessException{
		ArrayList<DatabaseColumn> retval = null;
		DatabaseColumn dc = null;
		String tableName = null;
		Class<?> base = null;
//		try{
			if(obj != null){
				base = obj.getClass();
				if (base.isAnnotationPresent(Table.class)) {
					Annotation annotation = base.getAnnotation(Table.class);
					Table table  = (Table) annotation;
					if(table != null)
						tableName = table.value();
				}
				for(Field field : base.getDeclaredFields()){
					int type = DatabaseColumn.STRING;
					if (field.getType().isAssignableFrom(Integer.TYPE)) {
						type = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(String.class)) {
						type = DatabaseColumn.STRING;
					}else if (field.getType().isAssignableFrom(Date.class)) {
						type = DatabaseColumn.DATE;
					}else if (field.getType().isAssignableFrom(Long.TYPE)) {
						type = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(Float.TYPE)) {
						type = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Double.TYPE)) {
						type = DatabaseColumn.DOUBLE;
					}
					if (field.isAnnotationPresent(Column.class)) {
						Annotation annotation = field.getAnnotation(Column.class);
						Column col = (Column) annotation;
						field.setAccessible(true);
						dc = new DatabaseColumn(type, col.value(), field.get(obj));
						if (field.isAnnotationPresent(KEY.class)) {
							dc.setKey(true);
						}
						if (field.isAnnotationPresent(GENRATED.class)) {
							dc.setAuto(true);
						}
					}
					if(retval == null)
						retval = new ArrayList<DatabaseColumn>();
					if(dc != null)
						retval.add(dc);
				}
			}
	/*	}catch(Exception ex){
			ex.printStackTrace();
		}*/
		return retval;
	}
	private static void setPrimaryValue(Object obj,int key) throws IllegalArgumentException, IllegalAccessException{
//		try{
			if(obj == null || Misc.isUndef(key))
				return;
			Class<?> base = obj.getClass();
			Field primary = null;
			for(Field field : base.getDeclaredFields()){
				/*if(field.isAnnotationPresent(KEY.class)){
					primary = field;
				}*/
				if (field.isAnnotationPresent(PRIMARY_KEY.class)) {
					primary = field;
					break;
				}
			}
			if(primary != null){
				primary.setAccessible(true);
				primary.set(obj, key);
			}
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
	public static Field getPrimaryField(Class<?> base) throws IllegalArgumentException, IllegalAccessException{
		if(base == null)
			return null;
		for(Field field : base.getDeclaredFields()){
			if (field.isAnnotationPresent(PRIMARY_KEY.class)) {
				return field;
			}
		}
		return null;
	}
	public static Field getGeneratedField(Class<?> base) throws IllegalArgumentException, IllegalAccessException{
		if(base == null)
			return null;
		for(Field field : base.getDeclaredFields()){
			if (field.isAnnotationPresent(GENRATED.class)) {
				return field;
			}
		}
		return null;
	}
	public static Field getUniqueField(Class<?> base) throws IllegalArgumentException, IllegalAccessException{
		if(base == null)
			return null;
		for(Field field : base.getDeclaredFields()){
			if (field.isAnnotationPresent(Unique.class)) {
				return field;
			}
		}
		return null;
	}
	public static Field getFieldWithKey(Class<?> base,Class<? extends Annotation> key) throws IllegalArgumentException, IllegalAccessException{
		if(base == null)
			return null;
		for(Field field : base.getDeclaredFields()){
			if (field.isAnnotationPresent(key)) {
				return field;
			}
		}
		return null;
	}
	
	public static ArrayList<DatabaseColumn> getClassDatabaseColumn(Class base){
		if(base == null)
			return null;
//		try{
			if(!classFieldMap.containsKey(base)){
				String tableName = null;
				int fieldType = DatabaseColumn.STRING;
				ArrayList<DatabaseColumn> colList = null;
				if (base.isAnnotationPresent(Table.class)) {
					Annotation annotation = base.getAnnotation(Table.class);
					Table table  = (Table) annotation;
					if(table != null)
						tableName = table.value();
				}
				for(Field field : base.getDeclaredFields()){
					fieldType = DatabaseColumn.STRING;
					if (field.getType().isAssignableFrom(Integer.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(String.class)) {
						fieldType = DatabaseColumn.STRING;
					}else if (field.getType().isAssignableFrom(Date.class)) {
						fieldType = DatabaseColumn.DATE;
					}else if (field.getType().isAssignableFrom(Long.TYPE)) {
						fieldType = DatabaseColumn.INTEGER;
					}else if (field.getType().isAssignableFrom(Float.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Double.TYPE)) {
						fieldType = DatabaseColumn.DOUBLE;
					}else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
						fieldType = DatabaseColumn.BOOLEAN;
					}
					DatabaseColumn dc = null;
					if (field.isAnnotationPresent(Column.class) ) {
						if(colList == null)
							colList = new ArrayList<DatabaseColumn>();
						Annotation annotation = field.getAnnotation(Column.class);
						Column col = (Column) annotation;
						field.setAccessible(true);
						dc = new DatabaseColumn(fieldType, col.value(), null);
						dc.setField(field);
						if (field.isAnnotationPresent(KEY.class)) {
							dc.setKey(true);
						}
						if (field.isAnnotationPresent(GENRATED.class)) {
							dc.setAuto(true);
						}
						colList.add(dc);
					}
					
				}
				classFieldMap.put(base, colList);
			}
			return classFieldMap.get(base);
		/*}catch(Exception ex){
			ex.printStackTrace();
		}*/
		//return null;
	}
	public static <T> boolean mergeNonNull(T from,T to){
		if(from == null || to == null)
			return false;
		boolean retval = true;
		try{
		Class base = from.getClass();
		for(Field field : base.getDeclaredFields()){
			int type = DatabaseColumn.STRING;
			if (field.getType().isAssignableFrom(Integer.TYPE)) {
				type = DatabaseColumn.INTEGER;
			}else if (field.getType().isAssignableFrom(String.class)) {
				type = DatabaseColumn.STRING;
			}else if (field.getType().isAssignableFrom(Date.class)) {
				type = DatabaseColumn.DATE;
			}else if (field.getType().isAssignableFrom(Long.TYPE)) {
				type = DatabaseColumn.INTEGER;
			}else if (field.getType().isAssignableFrom(Float.TYPE)) {
				type = DatabaseColumn.DOUBLE;
			}else if (field.getType().isAssignableFrom(Double.TYPE)) {
				type = DatabaseColumn.DOUBLE;
			}
			field.setAccessible(true);
			DatabaseColumn dc = new DatabaseColumn(type, null, field.get(from));
			if(!dc.isNull()){
				field.set(to, field.get(from));
			}
		}
		}catch(Exception ex){
			ex.printStackTrace();
			retval = false;
		}
		return retval;
	}
	public static Object getRowObject(Class<?> base,ResultSet rs, ArrayList<DatabaseColumn> dbColumns) throws Exception{
		Object dataBean = base.newInstance();
		for(DatabaseColumn dc : dbColumns){
			 setRsetValue(rs,dc,dataBean);
		 }
		return dataBean;
	}
	public static void setParamObject(Object obj,PreparedStatement ps, ArrayList<DatabaseColumn> dbColumns) throws Exception{
		int pos = 1;
		for(DatabaseColumn dc : dbColumns){
			dc.setStatement(obj,ps, pos);
		 }
	}
	public static void copyData(Connection conn, Class<?> base,Connection from, Connection to, int batchSize) throws Exception{
		PreparedStatement psSelect = null;
		PreparedStatement psInsert = null;
		ResultSet rs = null;
		Pair<StringBuilder, ArrayList<DatabaseColumn>> selectQueryPair = getGeneralQuery(conn, SELECT, base);
		Pair<StringBuilder, ArrayList<DatabaseColumn>> insertQueryPair = getGeneralQuery(conn, INSERT, base);
		psSelect = from.prepareStatement(selectQueryPair.first.toString());
		psInsert = to.prepareStatement(insertQueryPair.first.toString());
		rs = psSelect.executeQuery();
		int count = 0;
		while(rs.next()){
			if(count > batchSize){
				psInsert.executeBatch();
				psInsert.clearBatch();
				count = 0;
			}
			setParamObject(getRowObject(base, rs, selectQueryPair.second),psInsert,selectQueryPair.second);
			psInsert.addBatch();
			count++;
		}
		if(count > 0){
			psInsert.executeBatch();
			psInsert.clearBatch();
			count = 0;
		}
		Misc.closeRS(rs);
		Misc.closePS(psSelect);
		Misc.closePS(psInsert);
	}
	public static DatabaseColumn getDBColumnFromField(Field field){
		int fieldType = DatabaseColumn.STRING;
		if (field.getType().isAssignableFrom(Integer.TYPE)) {
			fieldType = DatabaseColumn.INTEGER;
		}else if (field.getType().isAssignableFrom(String.class)) {
			fieldType = DatabaseColumn.STRING;
		}else if (field.getType().isAssignableFrom(Date.class)) {
			fieldType = DatabaseColumn.DATE;
		}else if (field.getType().isAssignableFrom(Long.TYPE)) {
			fieldType = DatabaseColumn.INTEGER;
		}else if (field.getType().isAssignableFrom(Float.TYPE)) {
			fieldType = DatabaseColumn.DOUBLE;
		}else if (field.getType().isAssignableFrom(Double.TYPE)) {
			fieldType = DatabaseColumn.DOUBLE;
		}else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
			fieldType = DatabaseColumn.BOOLEAN;
		}
		Column col = null;
		if (field.isAnnotationPresent(Column.class) ) {
			Annotation annotation = field.getAnnotation(Column.class);
			col = (Column) annotation;
		}
		return new DatabaseColumn(fieldType, col != null ? col.value() : null, null);
	}
	public static String getTableName(Class<?> base){
		String tableName = null;
		if (base.isAnnotationPresent(Table.class)) {
			Annotation annotation = base.getAnnotation(Table.class);
			Table table  = (Table) annotation;
			if(table != null )
				tableName = table.value();
		}
		return tableName;
	}
	private static Field[] getFields(Connection conn,Class<?> base, String tableName){
		if(base == null)
			return null;
		Field[] fields = null;
		if(!fieldMap.contains(base.getCanonicalName().toString())){
			ArrayList<Field> filteredFields = null;
			try{
				Field[]  declaredFields = base.getDeclaredFields();
				if(conn != null){
					Pair<String, String> connParams = DBConnectionPool.getConnectionParams(conn);
					com.ipssi.rfid.db.DBSchemaManager.Table table = DBSchemaManager.getTable(conn, tableName, connParams.second);
					if(table != null && declaredFields.length > 0 ){
						for (int i = 0; i < declaredFields.length; i++) {
							if (declaredFields[i].isAnnotationPresent(Column.class) ) {
								Annotation annotation = declaredFields[i].getAnnotation(Column.class);
								Column col = (Column) annotation;
								if(table.getColumnByName(col.value()) != null){
									if(filteredFields == null)
										filteredFields = new ArrayList<Field>();
									filteredFields.add(declaredFields[i]);
								}else{
									System.out.println("[RFIDMasterDao][Warning Field Missing] : "+tableName+"."+col.value());
								}
							}
						}
					}
				}
				if(filteredFields != null){
					for (int i = 0; i < filteredFields.size(); i++) {
						if(fields == null)
							fields = new Field[filteredFields.size()];
						fields[i] = filteredFields.get(i);
					}
					
				}
				else
					fields = declaredFields;
				fieldMap.put(base.getCanonicalName().toString(), fields);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return fields;
		}else{
			return fieldMap.get(base.getCanonicalName());
		}
	}
	
	public static Object getClone(Object obj) {
		if(obj == null)
			return null;
		Object retval = null;
		try{
			Class<?> base = obj.getClass();	
			retval = obj.getClass().newInstance();
			Field[] fields = getFields(null,base,null);
			for (int i = 0, is= fields==null?0:fields.length; i < is; i++) {
				System.out.println(", "+fields[i].getName());
				fields[i].setAccessible(true);
				fields[i].set(retval, fields[i].get(obj));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
	
}

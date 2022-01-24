package com.ipssi.dodetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.trip.challan.ChallanParamBean;

public class DoInformation {
	private static HashMap<Integer, DoDefinitionBean> doDefinition;
	public synchronized static DoDefinitionBean getDODefinitionByPortNodeId(Connection conn,int portNodeId, boolean toreload) {
		if (toreload)
			doDefinition = null;
		DoDefinitionBean retval = null; 
		if(doDefinition == null)
			loadDODefinition(conn);
		try{
		if(doDefinition != null){
			Cache cache = Cache.getCacheInstance(conn);
			MiscInner.PortInfo currPort = cache.getPortInfo(portNodeId, conn);
			for (;currPort != null; currPort = currPort.m_parent) {
				retval = doDefinition.get(currPort.m_id);
				if (retval != null)
					break;
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return retval;
	}
	public static void loadDODefinition(Connection conn){
		DoDefinitionDao doDefinitionDao = new DoDefinitionDao();
		try{
			doDefinition = doDefinitionDao.getDoDefinition(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
public static String getDynamicUpdateQuery(ArrayList<DimInfo.ValInfo> valList, HashMap<Integer, DoParamBean> doParamList){
		StringBuilder q = new StringBuilder();
		q.append("update mines_do_details set ");
		boolean paramAdded = false;
		if(valList != null){
			for(DimInfo.ValInfo val : valList){
				DoParamBean bean = doParamList.get(val.m_id);
				if (bean == null)
					continue;
				
				if (paramAdded)
					q.append(",");
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
		q.append(", updated_on=now() ");
		q.append(" where id = ? ");
		return q.toString();
	}


public static String getDynamicInsertQuery(ArrayList<DimInfo.ValInfo> valList){
	String retval = null;
	String paramList = "updated_on",insertParam = "now()";
	
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
			paramList += "," + val.m_name;
			insertParam += ",?";
			if(val.m_id == 15)
				break;
		}
		retval = " insert into mines_do_details(";
		retval += paramList + ") values (" + insertParam + ")";
	}
	return retval;
}

public static String getDynamicInsertQuery(ArrayList<DimInfo.ValInfo> valList,String tableName,int port_node_id){
	String retval = null;
	String paramList = "updated_on",insertParam = "now()";
	
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
		
		if(val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") && val.m_name != ""){		
			paramList += "," + val.m_name;
			insertParam += ",?";
			}
		}
		retval = " insert into "+ tableName +"(";
		retval += paramList + ",created_on,status,port_node_id,do_type,taxation_type,material,transport_mode,max_tare_gap,date_field1) values (" + insertParam + ",now(),1,"+port_node_id+",1,1,'COAL',1,0,now())";
	}
	return retval;
}
public static String getDynamicInsertQueryForCustomerDetails(ArrayList<DimInfo.ValInfo> valList,String tableName,int port_node_id){
	String retval = null;
	String paramList = "updated_on",insertParam = "now()";
	
	if(valList != null){
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("customer_details") && val.m_name != ""){		
				paramList += "," + val.m_name;
				insertParam += ",?";
			}
		}
		retval = " insert into "+ tableName +"(";
		//retval += paramList + ", sap_code,sn,status,port_node_id,created_on) values (" + insertParam + ",?,?,1,"+port_node_id+",now())";
		retval += paramList + ",status,port_node_id,created_on) values (" + insertParam + ",1,"+port_node_id+",now())";
}
	return retval;
}

public static String getDynamicUpdateQueryForCustomer(ArrayList<DimInfo.ValInfo> valList,String tableName,String whereClauseColumn){
	StringBuilder q = new StringBuilder();
	q.append("update customer_details set ");
	boolean paramAdded = false;
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("customer_details") && val.m_name != ""){
				if (paramAdded)
					q.append(",");
			
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
	q.append(" where ");
	q.append(whereClauseColumn).append("=?");
	return q.toString();
}

public static String getDynamicUpdateQuery(ArrayList<DimInfo.ValInfo> valList,String tableName,String whereClauseColumn){
	StringBuilder q = new StringBuilder();
	q.append("update mines_do_details set ");
	boolean paramAdded = false;
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") && val.m_name != "" ){
				if (paramAdded)
					q.append(",");
			
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
	q.append(" where ");
	q.append(whereClauseColumn).append("=?");
	return q.toString();
}

public static String getDynamicUpdateQueryReleaseType(ArrayList<DimInfo.ValInfo> valList,String tableName,String whereClauseColumn){
	StringBuilder q = new StringBuilder();
	q.append("update mines_do_details set ");
	boolean paramAdded = false;
		for(DimInfo.ValInfo val : valList){
			if(val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") && val.m_name != ""){
				if(val.m_name.equalsIgnoreCase("do_number"))
					continue;
				
				if (paramAdded)
					q.append(",");
			
				q.append(val.m_name).append("=?");
				paramAdded = true;
			}
		}
	q.append(" where ");
	q.append(whereClauseColumn).append("=?");
	return q.toString();
}

public static void loadMinesDoDetails(Connection conn, boolean toreload) {
	if (toreload)
		DoMaster.seclMinesDetailsMap = null;

	String fetchMinesDo = "select id,name from mines_details";
	ResultSet rs = null;
	PreparedStatement ps = null;
	
	try {	
		ps = conn.prepareStatement(fetchMinesDo);
		rs = ps.executeQuery();
		while(rs.next()){
			if(DoMaster.seclMinesDetailsMap  == null)
				DoMaster.seclMinesDetailsMap = new HashMap<Integer, String>();

			DoMaster.seclMinesDetailsMap.put(rs.getInt("id"), rs.getString("name"));
		}

	} catch (SQLException sqlEx) {
		sqlEx.printStackTrace();
	} catch (Exception ex) {
		ex.printStackTrace();
	}finally{
		try{
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
}

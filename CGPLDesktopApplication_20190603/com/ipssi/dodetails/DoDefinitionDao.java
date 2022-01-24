package com.ipssi.dodetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.geometry.Point;
import com.ipssi.mapping.common.db.DBQueries;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.tracker.landmarkdefinition.LandmarkBean;
import com.ipssi.trip.challan.ChallanDefinitionBean;
import com.ipssi.trip.challan.ChallanDefinitionDao;
import com.ipssi.trip.challan.ChallanParamBean;
import com.sun.org.apache.bcel.internal.generic.RETURN;

public class DoDefinitionDao {
	public SessionManager m_session = null;

	public DoDefinitionDao(SessionManager m_session) {
		this.m_session = m_session;
	}
	public DoDefinitionDao() {
		// TODO Auto-generated constructor stub
	}
	public int isDoExist(String doNumber){
		    int count = 0;
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			
			if(Utils.isNull(doNumber))
				return count;
				
			try {
				conn = m_session.getConnection();
				String sql = "Select count(*) from tp_record where do_number = ? and status in (1,2)";
				System.out.println(sql);
				ps = conn.prepareStatement(sql);
				ps.setString(1,doNumber) ;
				System.out.println("DODefinition :"+ps.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					count++;
				}
			} catch (SQLException e) {
				System.out.println("Error");
				 e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Error");
				e.printStackTrace();
			}finally{
				if (ps != null) {
					Misc.closePS(ps);
				}
				if (rs != null) {
					Misc.closeRS(rs);
				}
			}
		return count;	
}
	Logger logger = Logger.getLogger(DoDefinitionDao.class);

	public HashMap<Integer,DoDefinitionBean> getDoDefinition(Connection conn) throws GenericException {
		String fetchDoDefinition = "select id, port_node_id, name, subject, file_name, total_column, start_row, server_mail_id, from_mail_group_id,listener, csv_delemeter,charset from mines_definition";
		ResultSet rs = null;
		PreparedStatement ps = null;
		HashMap<Integer, DoDefinitionBean> retval = null;
		DoDefinitionBean doDefinitionBean = null;
		try {	
			ps = conn.prepareStatement(fetchDoDefinition);
			rs = ps.executeQuery();
			while(rs.next()){
				doDefinitionBean = new DoDefinitionBean();
				if(retval == null)
					retval = new HashMap<Integer, DoDefinitionBean>();
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");
				int challanDefinitionId = Misc.getRsetInt(rs, "id");
				doDefinitionBean.setId(challanDefinitionId);
				doDefinitionBean.setPortNodeId(portNodeId);
				doDefinitionBean.setName(rs.getString("name"));
				doDefinitionBean.setSubject(rs.getString("subject"));
				doDefinitionBean.setFileName(rs.getString("file_name"));
				doDefinitionBean.setTotalColumn(Misc.getRsetInt(rs, "total_column"));
				doDefinitionBean.setStartRow(Misc.getRsetInt(rs, "start_row"));
				doDefinitionBean.setServerMailId(rs.getString("server_mail_id"));
				doDefinitionBean.setFromMail(rs.getString("from_mail_group_id"));
				doDefinitionBean.setCsvDelemeter(rs.getString("csv_delemeter"));
				doDefinitionBean.setCharset(rs.getString("charset"));
				doDefinitionBean.setListener(rs.getInt("listener"));
				doDefinitionBean.setDoParamList(getDoParams(conn, challanDefinitionId));
				retval.put(portNodeId, doDefinitionBean);
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
		return retval;
	}
	public HashMap<Integer,DoParamBean> getDoParams(Connection conn,int challanDefinitionId) throws GenericException {
		String fetch = "select 	id, param_id, param_pos, do_definition_id, notes, data_type,sec_col_pos,sec_col_datatype from do_mapping where do_definition_id=? order by param_pos asc";
		ResultSet rs = null;
		PreparedStatement ps = null;
		HashMap<Integer,DoParamBean> retval = null;
		DoParamBean doParamBean = null;
		try {	
			ps = conn.prepareStatement(fetch);
			Misc.setParamInt(ps, challanDefinitionId, 1);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new HashMap<Integer, DoParamBean>();
				int challanParamId = Misc.getRsetInt(rs, "param_id");
				doParamBean = new DoParamBean();
				doParamBean.setId(Misc.getRsetInt(rs, "id"));
				doParamBean.setParamId(challanParamId);
				doParamBean.setParamPos(Misc.getRsetInt(rs, "param_pos"));
				doParamBean.setDataType(Misc.getRsetInt(rs, "data_type"));
				doParamBean.setNotes(rs.getString("notes"));
				doParamBean.setSecColPos(Misc.getRsetInt(rs, "sec_col_pos"));
				doParamBean.setSecColDataType(Misc.getRsetInt(rs, "sec_col_datatype"));
				retval.put(challanParamId,doParamBean);
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
		return retval;
	}


	public static int  isExist(Connection conn,String value ,String query , boolean doAddParameter){
		boolean isExist = false;
		int id = Misc.getUndefInt();
		PreparedStatement ps = null;
		int parameterCount = 1;
		if(value == null  || value.length() == 0 )
			return id;
	   
	     try {
	          ps = conn.prepareStatement(query);
	          if(doAddParameter)
	        	ps.setString(1, value);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()){
	        	 isExist = true;
	        	 id = rs.getInt(1);
	         }
	         rs.close();
	         ps.close();
	     }
	     
	     catch (Exception e) {
	        e.printStackTrace();
	     }
		
		return id;
	}
	public static String getSapCode(Connection conn,String value ,String query){
		String strVal = "";
		PreparedStatement ps = null;
		if(value == null  || value.length() == 0 )
			return "";
	   
	     try {
	          ps = conn.prepareStatement(query);
	         ps.setString(1, value);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next())
	        	 strVal = rs.getString(1);
	         rs.close();
	         ps.close();
	     }
	     
	     catch (Exception e) {
	        e.printStackTrace();
	     }
		
		return strVal;
	}
	
	public static String getGradeSapCode(Connection conn,String value ,String query){
        String strVal = "";
        PreparedStatement ps = null;
        if(value == null  || value.length() == 0 )
              return "";
        query = query+" name like '"+value+ "' or name like '"+ value.replaceAll("-", "")+"'";
       try {
            ps = conn.prepareStatement(query);
//         ps.setString(1, value);
           ResultSet rs = ps.executeQuery();
           if (rs.next())
               strVal = rs.getString(1);
           rs.close();
           ps.close();
       }
       
       catch (Exception e) {
          e.printStackTrace();
       }
        
        return strVal;
  }

	
	public static int getSapId(Connection conn,String value ,String query){
		int intVal = Misc.getUndefInt() ;
		PreparedStatement ps = null;
		if(value == null  || value.length() == 0 )
			return intVal;
	   
	     try {
	          ps = conn.prepareStatement(query);
	         ps.setString(1, value);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next())
	        	 intVal = rs.getInt(1);
	         rs.close();
	         ps.close();
	     }
	     
	     catch (Exception e) {
	        e.printStackTrace();
	     }
		
		return intVal;
	}
	public static int insertUpdateSapCode(Connection conn, ArrayList<String> data,int port_node_id,String query, boolean doUpdate) {
	  	PreparedStatement ps = null;
		//PreparedStatement ps1 = null;
		    int colPos = 1;
		   // int colPos1 = 1;
		    int generateId = Misc.getUndefInt();
		    String stateGstCode = (data.get(8) != null && data.get(8).length() > 2)  ? data.get(8).substring(0, 2) : "";
		    //insert into customer_details(updated_on,name,gst_no,address,str_field1,
		    //record_src,state_gst_code,status,port_node_id,created_on) 
		    //values (now(),?,?,?,?,?,?,1,3,now())
		    String name = (data.get(7) != null && data.get(7).length() > 63) ? data.get(7).substring(0, 60) : data.get(7); 
		    try{
    	      ps = conn.prepareStatement(query);
    	      ps.setString(colPos++,name);
			  ps.setString(colPos++,data.get(8));
			  ps.setString(colPos++,data.get(11));
			  ps.setString(colPos++,data.get(12));
			  ps.setString(colPos++,data.get(41));
			  ps.setInt(colPos++,Misc.getRecordSrcId(conn));
			  ps.setString(colPos++,stateGstCode);
			  ps.setString(colPos++,data.get(6));
			  ps.setString(colPos++,data.get(6));
			 
			  if(doUpdate)
				  ps.setString(colPos++,data.get(6));
			  
			  ps.executeUpdate();
			  ResultSet rs = ps.getGeneratedKeys();
			  if (rs.next())
				  generateId =  rs.getInt(1);
			  Misc.closeRS(rs);
		} catch (Exception ex) {
	        ex.printStackTrace();
	    }finally{
	    	
	    	Misc.closePS(ps);
		}
	    return generateId;
	}
	
}
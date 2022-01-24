package com.ipssi.dodetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.SessionManager;

public class DoDetailsDefinitionDao {
	public SessionManager m_session = null;
	private static final String insertMinesDoDetails = "insert ignore into mines_do_details(do_number,do_date,validity_date,do_release_date,do_release_no,type_of_consumer,customer_code,customer_contact_person,coal_size,qty_alloc,rate,transport_charge,sizing_charge,silo_charge,dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,royalty_charge,grade_code,source_code,washery_code,type_of_release,release_priority,status,do_type,port_node_id)";
	private static final String insertCustomerDetails = "insert ignore into mines_do_details(do_number,do_date,validity_date,do_release_date,do_release_no,type_of_consumer,customer_code,customer_contact_person,coal_size,qty_alloc,rate,transport_charge,sizing_charge,silo_charge,dump_charge,stc_charge,terminal_charge,forest_cess,stow_ed,avap,royalty_charge,grade_code,source_code,washery_code,type_of_release,release_priority,status,do_type,port_node_id)";
	
	public DoDetailsDefinitionDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	
	public boolean isExist(String value ,String str) throws Exception{
		boolean isExist = false;
		String query = "";
		Connection dbConn = null;
		if(value == null  || value.length() == 0 )
			return isExist;
	   
		if(str.equalsIgnoreCase("CustomerDetails")){
			query = "select 1 from customer_details where sap_code = ?";
		}else if(str.equalsIgnoreCase("MinesDetails")){
			query = "" +value; 
		}else if(str.equalsIgnoreCase("CoalProduct")){
			query = "" +value; 
		}else if(str.equalsIgnoreCase("GradeDetails")){
			query = "select 1 from customer_details where role_id =" +value; 
		}
	     try {
	    	 dbConn = m_session.getConnection();
	         PreparedStatement ps = dbConn.prepareStatement(query);
	         ps.setString(1, value);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next())
	        	 isExist = true;
	         rs.close();
	         ps.close();
	     }
	     
	     catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	     }
		
		return isExist;
	}
	
	public void insertMinesDoDetails(ArrayList<MinesDoBean> list){
		
	}
	public void insertCustomerDetails(ArrayList list){
		
	}
	public void insertGradeDetails(ArrayList list){
		
	}
	public void insertCoalProduct(ArrayList list){
		
	}
	public void insertMinesDetails(ArrayList list){
		
	}

}

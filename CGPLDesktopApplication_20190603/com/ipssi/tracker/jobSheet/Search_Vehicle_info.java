package com.ipssi.tracker.jobSheet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.tracker.web.ActionI;

public class Search_Vehicle_info implements ActionI{
	private SessionManager m_session;

	PreparedStatement ps=null;
	Connection conn=null;
	
    String forward="/VehicleJobSheet.jsp";
	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		// TODO Auto-generated method stub
		m_session = InitHelper.helpGetSession(request);
		 conn = m_session.getConnection();
		String dateStr = request.getParameter("statusFroms");
		Date startDate = null;
		Date endDate = null;
		String vehicle = request.getParameter("vehiclenames");
		String shifts = request.getParameter("shifts");
		SimpleDateFormat psdf = new SimpleDateFormat("dd/MM/yy HH:mm");
		SimpleDateFormat msdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(vehicle.equals("Select One")){
			vehicle="";			
		}
		if(shifts.equals("Select One")){
			shifts="";			
		}
		if(dateStr != null && dateStr.length() > 0){
			startDate = psdf.parse(dateStr);
			TimePeriodHelper.setTimeBegOfDate(startDate);
			endDate = new Date(startDate.getTime());
			Misc.addDays(endDate, 1);
			
		}
		ArrayList<VehicleDataBean> dataList = new ArrayList<VehicleDataBean>();
	    try{ 
	    	String query;
	    	query="select Container_No,Export_Import,Planned_Loc,Actual_Loc,Planned_Time,Actual_Time,DateStamp,id from vehiclejobsheet  where 1=1";		
	    						if(vehicle !=null && vehicle.length() > 0)
		    		    		{
		    		    			query+=" and Vehicle='"+vehicle+"'"; 
		    		    		}
		    		    		if(dateStr!=null && dateStr.length()>0 ){
		    		    				query+=" and DateStamp between '"+msdf.format(startDate)+"' and '"+msdf.format(endDate)+"'";
		    					}
		    		    		if(shifts !=null && shifts.length() > 0)
		    		    		{
		    		    			query+=" and Shift='"+shifts+"'"; 
		    		    		}
		    		    		
		    		    					ps = conn.prepareStatement(query);
		    		    				ResultSet	rs = ps.executeQuery();
		    		    			
		    		    					VehicleDataBean data = null;
		    		    					while(rs.next()){
		    		    						data = new VehicleDataBean();
		    				    		    	data.setContainerNo(rs.getString("Container_No"));
		    				    		    	data.setExport_Import(rs.getString("Export_Import"));
		    				    		    	data.setPlanned_Loc(rs.getString("Planned_Loc"));
		    				    		    	data.setActual_Loc(rs.getString("Actual_Loc"));
		    				    		    	data.setPlanned_Time(rs.getString("Planned_Time"));
		    				    		    	data.setActual_Time(rs.getString("Actual_Time"));
		    				    		    	data.setDateStmp(rs.getString("DateStamp"));
		    				    		    	data.setId(rs.getInt("id"));
		    		    						dataList.add(data);
		    							} 
		    		    					request.setAttribute("dataList", dataList);
		    		    					
		    		    					request.setAttribute("vehicle_name", vehicle);
		    		    					request.setAttribute("from_date",dateStr);
		    		    					request.setAttribute("shifts",shifts);
		    		    					   					
	    			}
	    			catch(Exception ex){
	    				ex.printStackTrace();
	    				
	    				}
	    			finally{
	    				try {
	    					if (ps != null) {
	    						ps.close();
	    						
	    					}
	    				} catch (Exception e2) {
	    					e2.printStackTrace();
	    				}
	    			
	    			}
	    			
	    			
		return forward;
	}

	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}

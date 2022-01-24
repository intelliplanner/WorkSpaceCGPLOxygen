package com.ipssi.tracker.jobSheet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.web.ActionI;



public class VehicleJobSheet extends HttpServlet implements ActionI {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SessionManager m_session;

	PreparedStatement ps=null;
	Connection conn=null;
	
    String forward="/VehicleJobSheet.jsp";
	
	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		java.sql.Date cur_sys_Date = new java.sql.Date(new java.util.Date().getTime());
		m_session = InitHelper.helpGetSession(request);
		 conn = m_session.getConnection();
			String dataString=request.getParameter("dataString");
			String dataString1=request.getParameter("dataString1");
			String vehicle=request.getParameter("vehicles");
			String statusFrom=request.getParameter("statusFroms");
			String shift=request.getParameter("shifts");			
			ArrayList<VehicleDataBean> dataList = new ArrayList<VehicleDataBean>();
			ArrayList<VehicleDataBean> UpdatedataList = new ArrayList<VehicleDataBean>();
			java.util.Date dateStamp = null;
			SimpleDateFormat psdf = new SimpleDateFormat("dd/MM/yy hh:mm");
			if(statusFrom != null && statusFrom.length() > 0)
				dateStamp = psdf.parse(statusFrom);
	        if (dataString != null && dataString.length() > 0) {
	        	String[] dataArrayRowWise = dataString.split("#");
	        	int rowSize = dataArrayRowWise.length;
	        	for (int i = 0; i < rowSize; i++) {
	        		String columnData = dataArrayRowWise[i];
	        		String cellData[]=columnData.split(",",-1);
	           		VehicleDataBean dataHolder=new VehicleDataBean();
	           		dataHolder.setContainerNo(cellData[0]);
	           		dataHolder.setExport_Import(cellData[1]);
	           		dataHolder.setPlanned_Loc(cellData[2]);
	           		dataHolder.setActual_Loc(cellData[3]);
	           		dataHolder.setPlanned_Time(cellData[4]);
	           		dataHolder.setActual_Time(cellData[5]);
	           		if(dataHolder.getContainerNo() != null && dataHolder.getContainerNo().length() > 0)
	           			dataList.add(dataHolder);
	            	}
	        		InsertData(dataList,vehicle,dateStamp,shift,cur_sys_Date);
	        }
	        if (dataString1 != null && dataString1.length() > 0) {
	        	String[] dataArrayRowWise1 = dataString1.split("#");
	        	int rowSize = dataArrayRowWise1.length;
	        	for (int i = 0; i < rowSize; i++) {
	        		String columnData1 = dataArrayRowWise1[i];
	        		String cellData1[]=columnData1.split(",",-1);
	           		VehicleDataBean dataHolder=new VehicleDataBean();
	           		dataHolder.setContainerNo(cellData1[0]);
	           		dataHolder.setExport_Import(cellData1[1]);
	           		dataHolder.setPlanned_Loc(cellData1[2]);
	           		dataHolder.setActual_Loc(cellData1[3]);
	           		dataHolder.setPlanned_Time(cellData1[4]);
	           		dataHolder.setActual_Time(cellData1[5]);
	           		dataHolder.setId(Integer.parseInt(cellData1[6]));
	        		 		UpdatedataList.add(dataHolder);
	            	}
	        	
	        		UpDateData(UpdatedataList,vehicle,dateStamp,shift,cur_sys_Date);
	        }
	        
		return forward;
	}


	private void InsertData(ArrayList<VehicleDataBean> dataList,
			String vehicle, java.util.Date date, String shift,Date cur_sys_Date) {

		
	
		try {
			
	String sql = "INSERT INTO vehiclejobsheet (Container_No,Export_Import,Planned_Loc,Actual_Loc,Planned_Time,Actual_Time,Vehicle,Shift,DateStamp)" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
     
			 if (dataList.size() > 0) {
					
					ps = conn.prepareStatement(sql);
					int dataListSize = dataList.size();
					for (int i = 0; i < dataListSize; i++) {
						VehicleDataBean dataHolder = dataList.get(i);
						ps.setString(1, dataHolder.getContainerNo());
						ps.setString(2,dataHolder.getExport_Import());
						ps.setString(3,dataHolder.getPlanned_Loc());
						ps.setString(4, dataHolder.getActual_Loc());
						ps.setString(5, dataHolder.getPlanned_Time());
						ps.setString(6, dataHolder.getActual_Time());
						ps.setString(7, vehicle);
						ps.setString(8,shift);
						ps.setTimestamp(9,Misc.utilToSqlDate(date));
						ps.addBatch();
					}
						ps.executeBatch();
						
				}
			 
		
	} catch (Exception e) {
		e.printStackTrace();
		


	}finally{
		try {
			if (ps != null) {
				ps.close();
				
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			
		}
		
	}

	}
		

	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	

	private void UpDateData(ArrayList<VehicleDataBean> UpdatedataList,
			String vehicle, java.util.Date date, String shift, Date cur_sys_Date) {

		try {
			
	String sql = "update  vehiclejobsheet set Container_No=?,Export_Import=?,Planned_Loc=?,Actual_Loc=?,Planned_Time=?,Actual_Time=?,Vehicle=?,Shift=?,DateStamp=? where id=? ";
     
			 if (UpdatedataList.size() > 0) {
					
					ps = conn.prepareStatement(sql);
					int dataListSize = UpdatedataList.size();
					for (int i = 0; i < dataListSize; i++) {
						VehicleDataBean dataHolder = UpdatedataList.get(i);
						ps.setString(1, dataHolder.getContainerNo());
						ps.setString(2,dataHolder.getExport_Import());
						ps.setString(3,dataHolder.getPlanned_Loc());
						ps.setString(4, dataHolder.getActual_Loc());
						ps.setString(5, dataHolder.getPlanned_Time());
						ps.setString(6, dataHolder.getActual_Time());
						ps.setString(7, vehicle);
						ps.setString(8,shift);
						ps.setTimestamp(9,Misc.utilToSqlDate(date));	
						//ps.setDate(10,cur_sys_Date);
						ps.setInt(10,dataHolder.getId());
						ps.addBatch();
					}
						ps.executeBatch();
						
				}
			 
		
	} catch (Exception e) {
		e.printStackTrace();
		

	}finally{
		try {
			if (ps != null) {
				ps.close();
				
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			
		}
		
	}	
		
		
		
	}
	
	

}

package com.ipssi.tracker.driverDetails;

import static com.ipssi.gen.utils.Common.getParamAsInt;
import static com.ipssi.tracker.common.util.ApplicationConstants.CREATE;
import static com.ipssi.tracker.common.util.ApplicationConstants.DELETE;
import static com.ipssi.tracker.common.util.ApplicationConstants.EDIT;
import static com.ipssi.tracker.common.util.ApplicationConstants.SAVE;
import static com.ipssi.tracker.common.util.ApplicationConstants.VIEW;
import static com.ipssi.tracker.common.util.Common.getParamAsInt;
import static com.ipssi.tracker.common.util.Common.isNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;

 
import com.ipssi.tracker.driverDetails.DriverDetailsBean;
import com.ipssi.tracker.driverDetails.DriverSkillsBean;
import com.ipssi.tracker.driverDetails.DriverDetailsDao;
import com.ipssi.tracker.vehicleMaintenance.VehicleMaintenanceDao;
import com.ipssi.tracker.web.ActionI;
import java.sql.*;

import com.ipssi.gen.utils.*;

public class DriverDetailsAction implements ActionI{
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		System.out.println("DriverDetailsAction.processRequest() ##############  ");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if(CREATE.equals(action))
				actionForward = createDriverDetails(request, response);
			else if(SAVE.equals(action))
				actionForward = saveDriverDetails(request, response);
			else if(EDIT.equals(action))
				actionForward = editDriverDetails(request, response);
			else if (DELETE.equals(action)) 
				actionForward = deleteDriverDetails(request,response);
			else //if(VIEW.equals(action))
				actionForward = viewDriverDetails(request, response);
		}
		catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if(action.equals(CREATE)){
			if(success)
				actionForward = "/driverDetails.jsp";
			else
				actionForward = "/driverDetails.jsp";
		}else if(action.equals(EDIT)){
			if(success)
				actionForward = "/driverDetails.jsp";
			else
				actionForward = "/driverDetails.jsp";
		}else if(action.equals(DELETE)){
			actionForward = "/driverDetails.jsp";
		}else if(action.equals(SAVE)){
			if(success)
				actionForward = "/driverDetailsList.jsp";
			else
				actionForward = "/driverDetails.jsp";
		}else {//if(action.equals(VIEW)){
			if(success)
				actionForward = "/driverDetailsList.jsp";
			else
				actionForward = "/driverDetailsList.jsp";
		}
		return actionForward;
	}
	public String saveDriverDetails(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		String xml = Common.getParamAsString(request.getParameter("XML_DATA_DRIVER_DETAILS"));
		DriverDetailsBean driverDetailsBean = new DriverDetailsBean();
		SessionManager session = InitHelper.helpGetSession(request);
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		driverDetailsBean.setOrgId(v123);
		driverDetailsBean.setName(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("name")));
		driverDetailsBean.setDriverUID(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_uid")));
		driverDetailsBean.setDriverMobileOne(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_mobile_one")));
		driverDetailsBean.setDriverMobileTwo(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_mobile_two")));
		driverDetailsBean.setDLNumber(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_dl_number")));
		driverDetailsBean.setDriverInsuranceOne(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_insurance_one")));
		driverDetailsBean.setDriverInsuranceTwo(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_insurance_two")));
		driverDetailsBean.setDriverAddressOne(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_address_one")));
		driverDetailsBean.setDriverAddressTwo(com.ipssi.gen.utils.Misc.getParamAsString(request.getParameter("driver_address_two")));
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		
		driverDetailsBean.setInsuranceOneDate(Misc.getParamAsDate(request.getParameter("insurance_one_date"), null, sdf));
		driverDetailsBean.setInsuranceTwoDate(Misc.getParamAsDate(request.getParameter("insurance_two_date"), null, sdf));
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
	    org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("SKILL");
	    List<DriverSkillsBean> driverSkillsList = driverDetailsBean.getDriverSkillsList();
	    int size = nList.getLength();
	    DriverSkillsBean driverSkillsBean = null;
		for (int i = 0; i < size; i++) {
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;
			int skillType = Common.getParamAsInt(e.getAttribute("skill_type"));
			int skillLevel = Common.getParamAsInt(e.getAttribute("skill_level"));
	        if (!Misc.isUndef(skillType) && !Misc.isUndef(skillLevel)){
	        	driverSkillsBean = new DriverSkillsBean();
	        	driverSkillsBean.setKey(Common.getParamAsInt(e.getAttribute("skill_type")));
	        	driverSkillsBean.setValue(Common.getParamAsInt(e.getAttribute("skill_level")));
	        	driverSkillsList.add(driverSkillsBean);
	        }
			 
		}
		driverDetailsBean.setDriverSkillsList((ArrayList<DriverSkillsBean>) driverSkillsList);
		DriverDetailsDao driverDetailDao = new DriverDetailsDao();
		try {
			if(request.getParameter("id") != null && !"".equals(request.getParameter("id"))){
	    		int id = com.ipssi.gen.utils.Misc.getParamAsInt(request.getParameter("id"));
	    		driverDetailsBean.setId(com.ipssi.gen.utils.Misc.getParamAsInt(request.getParameter("id")));
				driverDetailDao.updateDriverDetails(conn, driverDetailsBean);
	    	}else
			driverDetailDao.insertDriverDetails(conn, driverDetailsBean);
		} catch (GenericException e) {
			System.out.println("DriverDetailsAction.saveDriverDetails()"+ e.getMessage());				
			e.printStackTrace();
			throw e;
		}
		try {
				conn.commit();
		}
		catch (Exception e2) {
			e2.printStackTrace();
			throw e2;
		}
		return viewDriverDetails(request, response);
	}
	public String createDriverDetails(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		return CREATE;
	}
	public String editDriverDetails(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		DriverDetailsDao driverDetailsDao = new DriverDetailsDao();
		String driverId = (String) request.getParameter("id");
		DriverDetailsBean driverDetailsBean = new DriverDetailsBean();
		Connection conn = InitHelper.helpGetDBConn(request);
		if(driverId != null && !"".equals(driverId))
			driverDetailsBean = driverDetailsDao.getDriverDetailsById(conn, Misc.getParamAsInt(driverId));
		System.out.println("DriverDetailsAction.editRule()   ##############   driverDetailsBean :  : " +driverDetailsBean.getName());
		
		request.setAttribute("driverDetailsBean",driverDetailsBean);
		return CREATE;
	}
	public String viewDriverDetails(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		DriverDetailsDao driverDetailsDao = new DriverDetailsDao();
		request.setAttribute("driverList", driverDetailsDao.getDriverDataByOrg(InitHelper.helpGetSession(request)));
		return VIEW;
	}
	private String deleteDriverDetails(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {	
		Connection conn = InitHelper.helpGetDBConn(request);
		System.out.println("##DriverDetailAction.deleteRule() #####");
		String[] checkBoxDelete = request.getParameterValues("checkbox");
		int[] checkDelete = new int[checkBoxDelete.length]; 
		for(int i=0;i<checkBoxDelete.length;i++){
		checkDelete[i] = com.ipssi.gen.utils.Misc.getParamAsInt(checkBoxDelete[i]);
		}
		DriverDetailsDao driverDetailsDao = new DriverDetailsDao();
		driverDetailsDao.deleteDriverDetails(conn, checkDelete);
		
		return viewDriverDetails(request, response);
	}
}

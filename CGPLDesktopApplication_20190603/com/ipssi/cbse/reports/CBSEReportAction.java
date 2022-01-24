package com.ipssi.cbse.reports;

import static com.ipssi.tracker.common.util.ApplicationConstants.DELETE;
import static com.ipssi.tracker.common.util.ApplicationConstants.EDIT;
import static com.ipssi.tracker.common.util.ApplicationConstants.SAVE;
import static com.ipssi.tracker.common.util.ApplicationConstants.VIEW;
import static com.ipssi.tracker.common.util.ApplicationConstants.SEARCH;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class CBSEReportAction implements ActionI{
   static	SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		System.out.println("VehicleMaintenanceAction.processRequest()   $##################  ");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if(VIEW.equals(action))
				actionForward = viewCBSEReport(request, response);
			else if ("detail".equalsIgnoreCase(action)) {
				actionForward = getCBSEDetailForCentre(request, response);
			}
			else //if(VIEW.equals(action))
				actionForward = setupCBSEReportPage(request, response);
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
		if(action.equals(VIEW)){
			if(success)
				actionForward = "/viewCBSEReport.jsp";
			
			else
				actionForward = "/viewCBSEReport.jsp";
		}
		else if(action.equals("detail")){
			if(success)
				actionForward = "/viewCBSEDetailPage.jsp";
			
			else
				actionForward = "/viewCBSEDetailPage.jsp";
		}
		else if(action.equals(SEARCH)){
			if(success)
				actionForward = "/viewCBSEReport.jsp";
			else
				actionForward = "/viewCBSEReport.jsp";
		}else if(action.equals(DELETE)){
			actionForward = "/viewCBSEReport.jsp";
		}else if(action.equals(SAVE)){
			if(success)
				actionForward = "/viewCBSEReport.jsp";
			else
				actionForward = "/viewCBSEReport.jsp";
		}
		else {//if(action.equals(VIEW)){
			if(success)
				actionForward = "/viewCBSEReport.jsp";
			else
				actionForward = "/viewCBSEReport.jsp";
		}
		return actionForward;
	}
	public String getCBSEDetailForCentre(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String retVal = "detail";
		SessionManager session = InitHelper.helpGetSession(request);
		CBSEReportDao dao = new CBSEReportDao();
		ArrayList<CBSEStudentBean> cbseData = new ArrayList<CBSEStudentBean>();
		CBSEReportBean searchParms = new CBSEReportBean();
		Date startDt = null;
		String centreName = session.getParameter("cen_no");
		String classCode = session.getParameter("class");
		String examCode = session.getParameter("exam_code");
		searchParms.setCenterCode(centreName);
		String startDate = session.getParameter("e_date") ;
		if( startDate!= null && startDate.length() > 0)
			startDt = sdf.parse(startDate);
		else {
			startDt = new Date();
		}
		searchParms.setExamCode(examCode);
		searchParms.setStartDate(startDt);
		
		String classNo = session.getParameter("class");
		searchParms.setClassCode(classNo);
		Triple<String, String, String> counts = null;
		if(centreName != null && centreName.length() > 0){
		 counts = dao.getReportDetail(cbseData,  searchParms, session);
		}
		request.setAttribute("dataList", cbseData);
		request.setAttribute("counts", counts);
		return retVal;
		
	}
	public String setupCBSEReportPage(HttpServletRequest request,HttpServletResponse response) throws Exception{
		SessionManager session = InitHelper.helpGetSession(request);
		CBSEReportDao cbseDao = new CBSEReportDao();
		request.setAttribute("centreList", cbseDao.getCentreList(session));
		request.setAttribute("stateList",cbseDao.getStateList(session));
		request.setAttribute("exam_sch",cbseDao.getExamScheduleList(session));
		return SEARCH;
	}
	public String viewCBSEReport(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception  {
		SessionManager session = InitHelper.helpGetSession(request);
		
		CBSEReportDao cbseDao = new CBSEReportDao();
		CBSEReportBean searchParms = new CBSEReportBean();
		Date startDt = null;
		Date endDt = null;
		String className = session.getParameter("class");
		searchParms.setClassCode(className);
		String startDate = session.getParameter("start_date") ;
		String endDate = session.getParameter("end_date"); 
		if( startDate!= null && startDate.length() > 0)
			startDt = sdf.parse(startDate);
		if( endDate!= null && endDate.length() > 0)
			endDt = sdf.parse(endDate);
		searchParms.setStartDate(startDt);
		searchParms.setEndDate(endDt);
		String examName = session.getParameter("exam_name");
		searchParms.setExamCode(examName);
		String centreName = session.getParameter("centre_name");
		searchParms.setCenterCode(centreName);
		String state_name = session.getParameter("state");
		searchParms.setState(state_name);
		request.setAttribute("cbse_report", cbseDao.getCBSEReport(session, searchParms));
		request.setAttribute("centreList", cbseDao.getCentreList(session));
		request.setAttribute("stateList",cbseDao.getStateList(session));
		request.setAttribute("exam_name", examName);
		request.setAttribute("centre_name", centreName);
		request.setAttribute("state_name", state_name);
		request.setAttribute("start_date",startDate);
		request.setAttribute("end_date", endDate);
		request.setAttribute("class_name", className);
		request.setAttribute("exam_sch",cbseDao.getExamScheduleList(session));
		return VIEW;
	}
}

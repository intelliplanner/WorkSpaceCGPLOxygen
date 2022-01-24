package com.ipssi.reporting.reportForMenu;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.web.ActionI;


public class ReportForMenu  implements ActionI{
	
	private SessionManager m_session;
    private final static String EDIT_PAGE = "/reportsOnMenu.jsp";
	private final static String LIST_PAGE = "/manage_menu.jsp";
    private final static String AUTOEMAIL_LIST_PAGE = "/manageAutoEmail.jsp";
    private final static String ONLINE_REPORT_EDIT_PAGE = "/reportsForOnline.jsp";
    private final static String ONLINE_REPORT_LIST_PAGE = "/manage_onlinereport.jsp";
    private final static String LIBRARY_REPORT_LIST_PAGE = "/manage_libraryreport.jsp";
    private final static String LIBRARY_REPORT_EDIT_PAGE = "/reportsForLibrary.jsp";
	@Override
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		
		String actionForward = "";
		boolean success = false;
		Cache cache = null;
		m_session = InitHelper.helpGetSession(request);
		String action = Misc.getParamAsString(request.getParameter("action"), "LIST");
		int reportId = Misc.getParamAsInt(request.getParameter("reportId"));
		int emailGroupId = Misc.getParamAsInt(request.getParameter("emailGroupId"));
		int menuMasterId = Misc.getParamAsInt(request.getParameter("menuMaseteId"));
		
	System.out.println("This Method Has Been Called");
	String xmlExistGroup = request.getParameter("XML_DATA_EXISTGROUP");
	String xmlNewGroupEmailList = request.getParameter("XML_DATA_NEWGROUP_EMAIL");
	String xmlMenuField = request.getParameter("XML_DATA_MENU_FIELD");
	String xmlNewGroupData = request.getParameter("XML_DATA_NEWGROUP");
	String newGroupFrequencyList = request.getParameter("XML_DATA_NEWGROUP_FREQUENCY");
	String oldMenuTag = request.getParameter("oldmenuTag");
	String columnXML = request.getParameter("columnXML");
	String paramXML = request.getParameter("paramXML");

	if (action.equalsIgnoreCase(ApplicationConstants.SAVE)) {
		    cache = m_session.getCache();
			MenuReportDao dao = new MenuReportDao(m_session);
			MenuReportBean bean = new MenuReportBean();
			bean.setReportId(reportId);
			bean.setMenuMasterId(menuMasterId);
			MenuReportBean.ReprtGroupEmailAndFrequency emailData = new MenuReportBean.ReprtGroupEmailAndFrequency();
			emailData.setEmailReportId(emailGroupId);
			bean.setEmailReportGroupInformation(emailData);
			bean = dao.saveMenuReport(xmlMenuField, xmlExistGroup, xmlNewGroupData, xmlNewGroupEmailList,newGroupFrequencyList,bean,oldMenuTag,columnXML,paramXML,0);

			success = true;
			//com.ipssi.gen.utils.UserGen.Menu mainMenu = m_session.getMenu();
		    
			cache.insertMenuTreeFromPage(m_session,bean.getMenuPlaceHolderId(),bean.getOptionMenuTagName(), bean.getMenuTagName(),bean.getMenuTitle(), bean.getOrgId(), bean.getUserId(), bean.getReportId(),bean.getStatus(),bean.getUrl(),bean.getConfigFile());
			
		}
	// need to correct .............

	if ("onlinereport".equalsIgnoreCase(action)) {
	    cache = m_session.getCache();
		MenuReportDao dao = new MenuReportDao(m_session);
		MenuReportBean bean = new MenuReportBean();
		bean.setReportId(reportId);
		bean.setMenuMasterId(menuMasterId);
		MenuReportBean.ReprtGroupEmailAndFrequency emailData = new MenuReportBean.ReprtGroupEmailAndFrequency();
		emailData.setEmailReportId(emailGroupId);
		bean.setEmailReportGroupInformation(emailData);
		bean = dao.saveMenuReport(xmlMenuField, xmlExistGroup, xmlNewGroupData, xmlNewGroupEmailList,newGroupFrequencyList,bean,oldMenuTag,columnXML,paramXML,1);

		success = true;
	}
	if ("libraryreport".equalsIgnoreCase(action)) {
	    cache = m_session.getCache();
		MenuReportDao dao = new MenuReportDao(m_session);
		MenuReportBean bean = new MenuReportBean();
		bean.setReportId(reportId);
		bean.setMenuMasterId(menuMasterId);
		MenuReportBean.ReprtGroupEmailAndFrequency emailData = new MenuReportBean.ReprtGroupEmailAndFrequency();
		emailData.setEmailReportId(emailGroupId);
		bean.setEmailReportGroupInformation(emailData);
		bean = dao.saveMenuReport(xmlMenuField, xmlExistGroup, xmlNewGroupData, xmlNewGroupEmailList,newGroupFrequencyList,bean,oldMenuTag,columnXML,paramXML,2);

		success = true;
	}
		
	
	
	if (action.equalsIgnoreCase(ApplicationConstants.EDIT)) {
		if (reportId != Misc.getUndefInt()) {
			MenuReportBean bean = null;
			if(!Misc.isUndef(reportId)){
				MenuReportDao dao = new MenuReportDao(m_session);
				bean = dao.getMenuReportData(reportId,0);
				dao.getEmailReportGroupData(reportId, bean,0);
				dao.getEmailReportData(reportId, bean);
				dao.getEmailReportEmailData(reportId, bean);
				dao.getEmailReportFrequencyData(reportId,bean,0);
			}
			success = true;
			System.out.println("EmailReportId = "+ bean.getEmailReportGroupInformation().getEmailReportId());
          request.setAttribute("menuReportData", bean);
		}
	}
	if (action.equalsIgnoreCase("editOnlineReport")) {
		if (reportId != Misc.getUndefInt()) {
			MenuReportBean bean = null;
			if(!Misc.isUndef(reportId)){
				MenuReportDao dao = new MenuReportDao(m_session);
				bean = dao.getMenuReportData(reportId,1);
				dao.getEmailReportGroupData(reportId, bean,1);
				dao.getEmailReportData(reportId, bean);
				dao.getEmailReportEmailData(reportId, bean);
				dao.getEmailReportFrequencyData(reportId,bean,1);
			}
			success = true;
			System.out.println("EmailReportId = "+ bean.getEmailReportGroupInformation().getEmailReportId());
          request.setAttribute("menuReportData", bean);
		}
	}
	if (action.equalsIgnoreCase("editLibraryReport")) {
		if (reportId != Misc.getUndefInt()) {
			MenuReportBean bean = null;
			if(!Misc.isUndef(reportId)){
				MenuReportDao dao = new MenuReportDao(m_session);
				bean = dao.getMenuReportData(reportId,2);
				dao.getEmailReportGroupData(reportId, bean,2);
				dao.getEmailReportData(reportId, bean);
				dao.getEmailReportEmailData(reportId, bean);
				dao.getEmailReportFrequencyData(reportId,bean,2);
			}
			success = true;
			System.out.println("EmailReportId = "+ bean.getEmailReportGroupInformation().getEmailReportId());
          request.setAttribute("menuReportData", bean);
		}
	}
	
	
	if ("saveAutoEmail".equalsIgnoreCase(action)) {
		String emailGroupName = request.getParameter("groupname");
		int emailFormat = Misc.getParamAsInt(request.getParameter("reportformat"));
		int status = Misc.getParamAsInt(request.getParameter("reportStatus"));
		int emailGroupEditId = Misc.getParamAsInt(request.getParameter("editAutoEmail"));
		MenuReportDao dao = new MenuReportDao(m_session);
		dao.saveNewAutoEmailGroup(emailGroupName, emailFormat, status, newGroupFrequencyList, xmlNewGroupEmailList,emailGroupEditId);
		success = true;
	}
	if ("search".equalsIgnoreCase(action)) {
		SessionManager session = InitHelper.helpGetSession(request);
		int typeOfReport = Misc.getParamAsInt(session.getParameter("reportType"));
		String optionalReportName = Misc.getParamAsString(session.getParameter("optionalReportName"));
		String reportName = Misc.getParamAsString(session.getParameter("reportName"));
		int status = Misc.getParamAsInt(session.getParameter("status"));
		int menuPlaceHolderId = Misc.getParamAsInt(session.getParameter("menuplaceholder"));
		int pv123 = Misc.getParamAsInt(session.getParameter("pv123"));
		session.setAttribute("typeOfReport",typeOfReport+"", false);
		session.setAttribute("optionalReportName",optionalReportName, false);
		session.setAttribute("reportName",reportName, false);
		session.setAttribute("status", status+"", false);
		session.setAttribute("menuPlaceHolderId", menuPlaceHolderId+"", false);
		session.setAttribute("pv123", pv123+"", false);
		MenuReportDao dao = new MenuReportDao(m_session);
		ArrayList<MenuReportBean> list = dao.getSearchData(typeOfReport,optionalReportName,reportName,status,menuPlaceHolderId,pv123);
		success = true;
		request.setAttribute("searchDataList", list);
		if (typeOfReport == 0) {
			action = action+"_menu";
		}else if (typeOfReport == 1) {
			action = action+"_online";
		}else if (typeOfReport == 2) {
			action = action+"_library";
		}
	}
	actionForward = sendResponse(action, success, request);
	return actionForward;
	}
	
	
	@Override
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		if (ApplicationConstants.EDIT.equalsIgnoreCase(action)) {
			if (success) {
				return EDIT_PAGE;
			} else {
				return LIST_PAGE;
			}
		}
		if ("LIST".equalsIgnoreCase(action)) {
			return LIST_PAGE;
		} else if("saveAutoEmail".equalsIgnoreCase(action)){
			return AUTOEMAIL_LIST_PAGE;
		}else if ("editOnlineReport".equalsIgnoreCase(action)) {
			return ONLINE_REPORT_EDIT_PAGE;
		}else if ("onlinereport".equalsIgnoreCase(action)){
			return ONLINE_REPORT_LIST_PAGE;
		}else if ("libraryreport".equalsIgnoreCase(action)) {
			return LIBRARY_REPORT_LIST_PAGE;
		}else if ("editLibraryReport".equalsIgnoreCase(action)) {
			return LIBRARY_REPORT_EDIT_PAGE;
		}else if ("search_menu".equalsIgnoreCase(action)) {
			return LIST_PAGE;
		}else if ("search_online".equalsIgnoreCase(action)) {
			return ONLINE_REPORT_LIST_PAGE;
		}else if ("search_library".equalsIgnoreCase(action)) {
			return LIBRARY_REPORT_LIST_PAGE;
		}
			return LIST_PAGE;
	}
}
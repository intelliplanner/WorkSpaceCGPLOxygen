package com.ipssi.reporting.trip;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class XmlServlet implements ActionI {

	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = null;
		if ("generalizeData".equals(action))
			actionForward = processXMLData(request, response);
		return actionForward;
	}
	private String processXMLData(HttpServletRequest request, HttpServletResponse response) {
		ServletOutputStream stream = null;
		SessionManager _session = InitHelper.helpGetSession(request);
		FrontPageInfo fPageInfo = null;
		com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = null;
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			User _user = _session.getUser();
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null);
			String pgContext = Misc.getParamAsString(_session.getParameter("page_context"), "trip_summary_report");
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "trip_summary.xml");
			String reportName = Misc.getParamAsString(_session.getParameter("page_name"), "Report");
			_session.setAttribute("page_context", pgContext, false);
			_session.setAttribute("_main_page_context", null, false);
			fPageInfo = CacheManager.getFrontPageConfig(_dbConnection,(int) _user.getUserId(), Misc.getUserTrackControlOrg(_session), pgContext, frontPageName,Misc.getParamAsInt(request.getParameter("row"),0), 0);
			searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, pgContext, fPageInfo.m_frontSearchCriteria, null);
			stream = response.getOutputStream();
			response.setContentType("text/xml");
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
			qb.printPage(_dbConnection , fPageInfo, _session , searchBoxHelper,null,null, Misc.XML, reportName,Misc.getUndefInt(),stream,null,null, null, null);		
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
}

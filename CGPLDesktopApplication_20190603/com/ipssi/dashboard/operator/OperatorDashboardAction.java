
package com.ipssi.dashboard.operator;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTION;
import static com.ipssi.tracker.common.util.Common.getParamAsString;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.web.ActionI;

public class OperatorDashboardAction implements ActionI {
	private SessionManager m_session = null;
	private static Logger logger = Logger.getLogger(OperatorDashboardAction.class);
	private final String SERVLET = "/controlRoom.jsp";
	// private final String IO_SERVLET = "/IoServlet.do";
	private final String AJAX = "/operatorDashboardAjax.jsp";

	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		m_session = InitHelper.helpGetSession(request);
		String actionForward = "";
		String action = "";
		boolean success = false;
		try {
			Connection conn = InitHelper.helpGetDBConn(request);
			action = getParamAsString(request.getParameter(ACTION));
			if ("ACKNOWLEDGE".equalsIgnoreCase(action)) {
				int eventId = Misc.getParamAsInt(request.getParameter("event_id"));
				int status = Misc.getParamAsInt(request.getParameter("status"));
				int tableIndex = Misc.getParamAsInt(request.getParameter("tableIndex"));
				OperatorDashboardDao dao = new OperatorDashboardDao(m_session);
				dao.acknowlegde(eventId,status,tableIndex);
				request.setAttribute("xmlEventData", "");
				
			} else if ("UPDATE".equalsIgnoreCase(action)) {
				OperatorDashboardDao dao = new OperatorDashboardDao(m_session);
				String selectedRulesCSV = Misc.getParamAsString(request.getParameter("ruleList"));
				String vehicleName = Misc.getParamAsString(request.getParameter("vehicleName"));
				String xmlString = dao.fetchData(selectedRulesCSV,vehicleName);
				
				request.setAttribute("xmlEventData", xmlString);
				success = true;

			} else if ("ALARM".equalsIgnoreCase(action)) {
				OperatorDashboardDao dao = new OperatorDashboardDao(m_session);
				int eventId = Misc.getParamAsInt(request.getParameter("event_id"));
				int tableIndex = Misc.getParamAsInt(request.getParameter("tableIndex"));
				dao.setAlarm(eventId,tableIndex);
				request.setAttribute("xmlEventData", "");
			} else if ("VEHICLE_STATUS".equalsIgnoreCase(action)) {
				OperatorDashboardDao dao = new OperatorDashboardDao(m_session);
				int vehicleId = Misc.getParamAsInt(request.getParameter("vehicle_id"));
				int reasonId = Misc.getParamAsInt(request.getParameter("reasonId"));
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
				String startTime = request.getParameter("startFrom");
				String expectedEnd = request.getParameter("expectedEnd");
				String actualEnd = request.getParameter("actualEnd");
				String notes = request.getParameter("notes");
				int id = Misc.getParamAsInt(request.getParameter("id"));
				if ( id > 0 ){
					dao.updateUnavailableVehicles(id,vehicleId,reasonId,startTime,expectedEnd,actualEnd,notes);
				} else {
					dao.saveUnavailableVehicles(vehicleId,reasonId,startTime,expectedEnd,actualEnd,notes);
				}
				String selectedRulesCSV = Misc.getParamAsString(request.getParameter("ruleList"));
				String xmlString = dao.fetchData(selectedRulesCSV,"");
				request.setAttribute("xmlEventData",xmlString);
				success = true;
			} else {
				com.ipssi.dashboard.operator.OperatorDashboardDao dao = new com.ipssi.dashboard.operator.OperatorDashboardDao(m_session);
				String xmlString = dao.fetchData("","");

				request.setAttribute("xmlEventData", xmlString);
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		actionForward = sendResponse(action, success, request);
		return actionForward;
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		if ( "ACKNOWLEDGE".equalsIgnoreCase(action)){
			return AJAX;
		} if ("UPDATE".equalsIgnoreCase(action)) {
			return AJAX;
		} if ("ALARM".equalsIgnoreCase(action)) {
			return AJAX;
		} if ("VEHICLE_STATUS".equalsIgnoreCase(action)) {
			return AJAX;
		} else {//
			return SERVLET;
		}
	}

}

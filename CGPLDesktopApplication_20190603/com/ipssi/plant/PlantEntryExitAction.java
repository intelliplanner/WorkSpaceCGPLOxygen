package com.ipssi.plant;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.web.ActionI;

public class PlantEntryExitAction implements ActionI{
private SessionManager m_session;
private static final String SAVE_ENTRY = "entrysave";
private static final String SAVE_EXIT = "exitsave";
private static final String SAVE_RESPONAE_ENTRY = "/plantEntryCheck.jsp";
private static final String SAVE_RESPONAE_EXIT = "/plantExitCheck.jsp";
@Override
public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
	
    String actionForward = null;
    boolean success = false;
	String action  = request.getParameter("action");
	m_session = InitHelper.helpGetSession(request);
	if (SAVE_ENTRY.equalsIgnoreCase(action) || SAVE_EXIT.equalsIgnoreCase(action)) {	
		int vehicleId = Misc.getParamAsInt(request.getParameter("vehicle_id"));
		int cause = Misc.getParamAsInt(request.getParameter("_cause"));
		String comment = Misc.getParamAsString(request.getParameter("_comment"));
		//plant entry - 0 .. plant exit - 1
		int temp = Misc.getParamAsInt(request.getParameter("temp"));
        int userId = m_session.getUser().getUserId();		
		PlantEntryExitDao dao = new PlantEntryExitDao(m_session);
		dao.saveCaluseComment(vehicleId, userId, cause, comment, temp);
		success = true;
	}
	actionForward = sendResponse(action, success, request);
	return actionForward;
}
@Override
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
	String response = null;
        if (success && SAVE_ENTRY.equalsIgnoreCase(action))  {
			response = SAVE_RESPONAE_ENTRY;
		}else  if (success && SAVE_EXIT.equalsIgnoreCase(action))  {
			response = SAVE_RESPONAE_EXIT;
		}
		return response;
	}
}

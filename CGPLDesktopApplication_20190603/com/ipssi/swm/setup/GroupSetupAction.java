package com.ipssi.swm.setup;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTION;
import static com.ipssi.tracker.common.util.ApplicationConstants.CREATE;
import static com.ipssi.tracker.common.util.ApplicationConstants.EDIT;
import static com.ipssi.tracker.common.util.ApplicationConstants.SAVE;
import static com.ipssi.tracker.common.util.ApplicationConstants.SEARCH;
import static com.ipssi.tracker.common.util.Common.getParamAsString;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.db.ApplicationDao;
import com.ipssi.tracker.web.ActionI;

public class GroupSetupAction implements ActionI {
	private static Logger logger = Logger.getLogger(GroupSetupAction.class);

    public static final String GET_OPLIST = "oplist";
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		String action = "";
		boolean success = false;
		SessionManager session = InitHelper.helpGetSession(request);
		//ACTION=save => save and then goto list page, edit => get infoBean and then goto detail page, create => goto detail page, search = goto list page 
		try {
			action = getParamAsString(request.getParameter(ACTION));
			String actionForward = action; //incase there is an error is SAVE and we need to go back to detail page change set actionForward
			Connection conn = InitHelper.helpGetDBConn(request);
			if (action == null || action.length() == 0) {
				action = SEARCH;
			}
			if (GET_OPLIST.equals(action)) {
				int portNodeId = Misc.getParamAsInt(session.getParameter("portNode"));
				ArrayList<MiscInner.PairIntStr> loadList = ApplicationDao.getOpStations(conn, portNodeId, 1);
				ArrayList<MiscInner.PairIntStr> unloadList = ApplicationDao.getOpStations(conn, portNodeId, 2);
				request.setAttribute("csv_list0", loadList);
				request.setAttribute("csv_list1", unloadList);
				
			}
			else if (EDIT.equals(action)) {
				int groupId = Misc.getParamAsInt(session.getParameter("group_id"));
                GroupBean infoBean = GroupSetupDao.getGroup(conn, groupId);
                request.setAttribute("infoBean", infoBean);
			}
			else if (CREATE.equals(action)) {
				//do nothing - just forward
			}
			else if (SAVE.equals(action)) {
				GroupBean group = GroupSetupDao.read(session);
				GroupSetupDao.save(conn, group);
			}

			if (!EDIT.equals(actionForward) && !CREATE.equals(actionForward) && !GET_OPLIST.equals(actionForward)) {
				ArrayList<GroupBean> infoBean = GroupSetupDao.getGroups(session);
				request.setAttribute("infoBeanList",infoBean);
			}
			return sendResponse(actionForward, success, request);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		//ACTION=save => save and then goto list page, edit => get infoBean and then goto detail page, create => goto detail page, search = goto list page
		if (GET_OPLIST.equals(action)) {
			return "/genAjaxCSVgetter.jsp";
		}
		else if (CREATE.equals(action) || EDIT.equals(action)) {
			return "/swm_group_detail.jsp";
		}
		return "/swm_group_list.jsp";
	}
}

/**
 * 
 */
package com.ipssi.tracker.feedback;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.exception.ExceptionMessages;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.iomap.IoAction;
import com.ipssi.tracker.web.ActionI;
import org.apache.log4j.Logger;

import static com.ipssi.gen.utils.Common.*;
import static com.ipssi.tracker.common.util.ApplicationConstants.*;

/**
 * @author jai
 * 
 */
public class FeedbackAction implements ActionI {
	private SessionManager m_session = null;
	private static Logger logger = Logger.getLogger(FeedbackAction.class);
	private static String REDIRECTION = "/redirectionFeedback.jsp";
	private static String HOME = "/home.jsp";
	private static String FEEDBACK = "/feedback.jsp";
	private static String _LIST = "/viewUserFeedback.jsp";
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ipssi.tracker.web.ActionI#processRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		String action;
		String actionForward;
		boolean success = false;
		m_session = InitHelper.helpGetSession(request);
		action = getParamAsString(request.getParameter(ACTION));

		try {

			if (SAVE.equalsIgnoreCase(action)) {
				FeedbackBean bean = populateBean(request);
				if (!isNull(bean)) {
					FeedbackDao dao = new FeedbackDao(m_session);
					success = dao.save(bean);
				} else {
					success = false;
				}
			}if( VIEW.equalsIgnoreCase(action) ){
				FeedbackDao dao = new FeedbackDao(m_session);
				ArrayList<FeedbackBean> list =   dao.getFeedbackList();
				request.setAttribute("feedbackList", list);
			} else {
				success = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		actionForward = sendResponse(action, success, request);
		return actionForward;
	}

	/**
	 * @param request
	 * @return
	 */
	
	private FeedbackBean populateBean(HttpServletRequest request) {
		FeedbackBean bean = null;
		try {
			bean = new FeedbackBean();
			bean.setUserId(getParamAsInt(request.getParameter("userId")));
			bean.setFeedback(getParamAsString(request.getParameter("feedback")));

		} catch (Exception e) {
			logger.error(ExceptionMessages.INVALID_REQUEST);
		}
		return bean;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ipssi.tracker.web.ActionI#sendResponse(java.lang.String, boolean, javax.servlet.http.HttpServletRequest)
	 */
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward;
		if(SAVE.equalsIgnoreCase(action)){
			request.setAttribute("success", success);
			actionForward = REDIRECTION;
		} else if( VIEW.equalsIgnoreCase(action) ){ 
			actionForward = _LIST;
		}
		else if (success) {
			actionForward = FEEDBACK;
		}else {
			actionForward = HOME;
		} 
		return actionForward;
	}

}

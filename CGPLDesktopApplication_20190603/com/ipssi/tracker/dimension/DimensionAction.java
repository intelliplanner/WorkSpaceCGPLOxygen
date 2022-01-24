/**
 * 
 */
package com.ipssi.tracker.dimension;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.*;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.web.ActionI;

import static com.ipssi.tracker.common.util.ApplicationConstants.*;
import static com.ipssi.gen.utils.Common.*;

/**
 * @author jai
 * 
 */
public class DimensionAction implements ActionI {

	private static Logger logger = Logger.getLogger(DimensionAction.class);
	private final String EDIT_JSP = "/dimensionMapDetails.jsp";
	private final String LIST = "/dimensionMapList.jsp";
	private final String DIMENSION_SERVLET = "/DimensionMapServlet.do";
	private SessionManager m_session;

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ipssi.tracker.web.ActionI#processRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		m_session = InitHelper.helpGetSession(request);		
		String actionForward = "";
		String action = "";
		boolean success = false;
		try {
			//Connection conn = InitHelper.helpGetDBConn(request);

			action = request.getParameter("action");
			if (CREATE.equalsIgnoreCase(action)) {
				success = true;
				request.setAttribute("dimensionMapDetials", null);

			} else if (EDIT.equalsIgnoreCase(action)) {
				int id = getParamAsInt(request.getParameter("id"));
				DimensionDetailBean dBean = fetchDimensionDetail(id, request);
				success = isNull(dBean) ? false : true;
				request.setAttribute("dimensionMapDetials", dBean);
			} else if (SAVE.equalsIgnoreCase(action)) {

				DimensionDetailBean dBean = populateBean(request);
				DimensionDao dDao = new DimensionDao(m_session);
				if (dBean.getId() == -1) {
					success = dDao.insert(dBean);
				} else {
					success = dDao.update(dBean);
				}
				dDao.handleUpdate(m_session.getConnection(), dBean.getId());
				if (success) {
					String pgContext = Misc.getParamAsString(m_session.getParameter("page_context"), "tr_iomapping_value");

					m_session.getUser().loadParamsFromMenuSpec(m_session, pgContext);
					com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(m_session.request, pgContext);
					String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext;
					int status = Misc.getParamAsInt(m_session.getParameter(topPageContext+"9008"),ApplicationConstants.ACTIVE); //will be csv
					int pv123 = Misc.getParamAsInt(m_session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
					ArrayList<DimensionDetailBean> ddList = dDao.fetchDimensionList(pv123,status);
					request.setAttribute("dBeanList", ddList);
				}

			} else if ( DELETE.equalsIgnoreCase(action)){
				String checkDelete[] = request.getParameterValues("check");
				DimensionDao dDao = new DimensionDao( m_session);
				if (checkDelete != null && checkDelete.length>0){  
				
					dDao.deleteDimensionMap(checkDelete);
				
				ArrayList<Integer> readingId = new ArrayList<Integer>();
				
				for (int i=0,is=checkDelete.length;i<is;i++) {
					readingId.add(Misc.getParamAsInt(checkDelete[i]));
				}
				dDao.handleUpdate(m_session.getConnection(), readingId);
				}
				success = true;
				String pgContext = Misc.getParamAsString(m_session.getParameter("page_context"), "tr_iomapping_value");
				m_session.getUser().loadParamsFromMenuSpec(m_session, pgContext);
				com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(m_session.request, pgContext);
				String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext;			 
				int status = Misc.getParamAsInt(m_session.getParameter(topPageContext+"9008"),ApplicationConstants.ACTIVE); //will be csv				
				int pv123 = Misc.getParamAsInt(m_session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);

				ArrayList<DimensionDetailBean> ddList = dDao.fetchDimensionList(pv123,status);
				request.setAttribute("dBeanList", ddList);

			}
				else {
			
				success = true;
				
				DimensionDao dDao = new DimensionDao(m_session);
				
				String pgContext = Misc.getParamAsString(m_session.getParameter("page_context"), "tr_iomapping_value");

				m_session.getUser().loadParamsFromMenuSpec(m_session, pgContext);
				com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(m_session.request, pgContext);

 
				String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext;
			 
				int status = Misc.getParamAsInt(m_session.getParameter(topPageContext+"9008"),ApplicationConstants.ACTIVE); //will be csv

				
				
				int pv123 = Misc.getParamAsInt(m_session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
				
				ArrayList<DimensionDetailBean> ddList = dDao.fetchDimensionList(pv123,status);
				request.setAttribute("dBeanList", ddList);

			}

		} catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
			throw ex;
		}
		actionForward = sendResponse(action, success, request);
	//	if ( SAVE.equalsIgnoreCase(action)){
	//		request.setAttribute("action", "view");
	//	}
		return actionForward;
	}

	/**
	 * @param request
	 * @return
	 */
	private DimensionDetailBean populateBean(HttpServletRequest request) throws GenericException {
		DimensionDetailBean dBean = null;
		try {
			dBean = new DimensionDetailBean();
			dBean.setName(getParamAsString(request.getParameter("name")));
			dBean.setDescription(getParamAsString(request.getParameter("description")));
			dBean.setPortNodeId(getParamAsInt(request.getParameter("Organization")));
			dBean.setId(getParamAsInt(request.getParameter("id"), -1));

			String xml = getParamAsString(request.getParameter("XML_DATA"));

			Document xmlDoc = MyXMLHelper.loadFromString(xml);

			NodeList nList = xmlDoc.getElementsByTagName("d");

			for (int i = 0; i < nList.getLength(); i++) {
				Node n = nList.item(i);
				Element e = (Element) n;
				if ( e.getAttribute("reading").trim() != "" || e.getAttribute("reading").trim().length() != 0 )
					dBean.addToTypeMap(getParamAsDouble(e.getAttribute("reading")), getParamAsDouble(e.getAttribute("reading_value")));
			}

		} catch (Exception e) {
			throw new GenericException(e);
		}
		return dBean;
	}

	/**
	 * @param id
	 * @param request
	 * @return
	 * @throws GenericException
	 */
	private DimensionDetailBean fetchDimensionDetail(int id, HttpServletRequest request) throws GenericException {
		DimensionDetailBean ddBean = null;
		try {
			DimensionDao dDao = new DimensionDao(m_session);

			ddBean = new DimensionDetailBean();
			ddBean.setName(getParamAsString(request.getParameter("name")));
			ddBean.setDescription(getParamAsString(request.getParameter("description")));
			ddBean.setId(getParamAsInt(request.getParameter("id")));
			ddBean.setPortNodeId(getParamAsInt(request.getParameter("Organization")));// portNodeId

			ddBean = dDao.fetchDimension(ddBean);
		} catch (GenericException e) {
			throw e;
		}
		return ddBean;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ipssi.tracker.web.ActionI#sendResponse(java.lang.String, boolean, javax.servlet.http.HttpServletRequest)
	 */
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";

		if (CREATE.equals(action)){
			if (success)
				actionForward = EDIT_JSP;
			else
				actionForward = EDIT_JSP;
		} else if (EDIT.equals(action)) {
			if (success)
				actionForward = EDIT_JSP;
			else
				actionForward = EDIT_JSP;
		} else if (DELETE.equals(action)) {
			actionForward = LIST;
		} else if (SAVE.equals(action)) {
			if (success)
				actionForward = LIST;
			else
				actionForward = LIST;
		} else {
			actionForward = LIST;
		}
		return actionForward;

		
	}

}

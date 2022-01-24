package com.ipssi.tracker.iomap;

import static com.ipssi.tracker.common.util.ApplicationConstants.*;
import static com.ipssi.tracker.common.util.Common.getParamAsInt;
import static com.ipssi.tracker.common.util.Common.getParamAsString;
import static com.ipssi.tracker.common.util.Common.isNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.DataProcessorGateway;
import com.ipssi.tracker.web.ActionI;
import com.ipssi.gen.utils.*;

import java.sql.*;

/**
 * IoAction provides directs to on a specified action by the user
 * 
 * @author jai
 * 
 */
public class IoAction implements ActionI {
	private static Logger logger = Logger.getLogger(IoAction.class);
	private final String EDIT_IO = "/deviceCompatibility.jsp";
	private final String IO_SERVLET = "/IoServlet.do";
	private final String DEVICE_MAPPING = "/defineDeviceMapping.jsp";

	/**
	 * The request is processed in this function whenever the servelet is called
	 * 
	 */
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		String actionForward = "";
		String action = "";
		boolean success = false;
		HashMap<Integer, String> dimensionMap = new HashMap<Integer, String>();
		HashMap<Integer, String> deviceNameMap = new HashMap<Integer, String>();
		HashMap<Integer, Integer> devicePinCountMap = new HashMap<Integer, Integer>();
		HashMap<Integer, String> dimensionReadingValueList = new HashMap<Integer, String>();
		/*
		 * dimensionMap is the mapping of Attributed ID to its Description in Dimensions table deviceNameMap is the mapping of Name and Model ID in Device Model Info Table
		 */
		try {
			Connection conn = InitHelper.helpGetDBConn(request);
			if (isNull(request.getAttribute("dimensionMap"))) {
				IoDao ioDao = new IoDao();
				dimensionMap = ioDao.getDimensionMap(conn);
				request.setAttribute("dimensionMap", dimensionMap);
				ioDao = null;

			}

			if (isNull(request.getAttribute("deviceNameMap"))) {
				IoDao ioDao = new IoDao();
				deviceNameMap = ioDao.getDeviceNameMap(conn);
				request.setAttribute("deviceNameMap", deviceNameMap);
				ioDao = null;
			}

			if (isNull(request.getAttribute("devicePinCountMap"))) {
				IoDao ioDao = new IoDao();
				devicePinCountMap = ioDao.getDevicePinCountMap(conn);
				request.setAttribute("devicePinCountMap", devicePinCountMap);
				ioDao = null;
			}

			if (isNull(request.getAttribute("dimensionReadingVaueList"))) {
				IoDao ioDao = new IoDao();
				SessionManager m_session = InitHelper.helpGetSession(request);
				int pv123 = com.ipssi.gen.utils.Misc.getParamAsInt(m_session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
				dimensionReadingValueList = ioDao.fetchDimensionReadingValueList(conn, pv123);
				request.setAttribute("dimensionReadingVaueList", dimensionReadingValueList);
				ioDao = null;
			}

			action = getParamAsString(request.getParameter(ACTION));

			/*
			 * Process Data for Specified Action
			 */
			if (CREATE.equals(action)) {
				success = true;
				request.setAttribute("io", null);

			} else if (DELETE.equals(action)) {

				String checkDelete[] = request.getParameterValues("checkbox");
				IoDao ioDao = new IoDao();
				if ( checkDelete != null && checkDelete.length > 0 )
					success = ioDao.deleteIo(conn, checkDelete);
				com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_iomapping_list");
				ArrayList<IoBean> ioList = new ArrayList<IoBean>();
				ioList = ioDao.fetchIoData(conn, InitHelper.helpGetSession(request));
				request.setAttribute("ioList", ioList);

			} else if (EDIT.equals(action)) {
				IoBean ioBean = processIo(request);
				success = isNull(ioBean) ? false : true;
				if (success)
					request.setAttribute("io", ioBean);
			} else if (SAVE.equals(action)) {
				success = process(request);
				if (success) {
					com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_iomapping_list");
					ArrayList<IoBean> ioList = new ArrayList<IoBean>();
					IoDao ioDao = new IoDao();
					ioList = ioDao.fetchIoData(conn, InitHelper.helpGetSession(request));
					request.setAttribute("ioList", ioList);
				}
			} else {

				success = true;
				com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_iomapping_list");
				ArrayList<IoBean> ioList = new ArrayList<IoBean>();
				IoDao ioDao = new IoDao();
				ioList = ioDao.fetchIoData(conn, InitHelper.helpGetSession(request));
				request.setAttribute("ioList", ioList);
			}

		} catch (GenericException ex) {
			ex.printStackTrace();
			logger.error(ex);
			throw ex;
		}

		actionForward = sendResponse(action, success, request);
		return actionForward;
	}

	/*
	 * Redirect According to Action in Request (non-Javadoc)
	 * 
	 * @see com.ipssi.tracker.web.ActionI#sendResponse(java.lang.String, boolean, javax.servlet.http.HttpServletRequest)
	 */
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";

		if (CREATE.equals(action)) {
			if (success)
				actionForward = EDIT_IO;
			else
				actionForward = DEVICE_MAPPING;
		} else if (EDIT.equals(action)) {
			if (success)
				actionForward = EDIT_IO;
			else
				actionForward = DEVICE_MAPPING;
		} else if (DELETE.equals(action)) {
			actionForward = DEVICE_MAPPING;
		} else if (SAVE.equals(action)) {
			if (success)
				actionForward = DEVICE_MAPPING;
			else
				actionForward = DEVICE_MAPPING;
		} else {
			actionForward = DEVICE_MAPPING;
		}
		return actionForward;
	}

	/*
	 * If the action is Save then insert/update to the Database using IoDao
	 */

	public boolean process(HttpServletRequest request) throws GenericException {
		IoBean ioBean = new IoBean();
		ioBean = populateFields(request);

		boolean insertStatus = false;

		if (isNull(ioBean)) {
			return insertStatus;
		}
		Connection conn = InitHelper.helpGetDBConn(request);
		IoDao ioDao = new IoDao();
		try {
			if (ioBean.getId() <= 0) {

				insertStatus = ioDao.insertData(conn, ioBean);
			} else {
				insertStatus = ioDao.updateData(conn, ioBean);
			}
			try {
				conn.commit();//sync happens on different connection
				ArrayList<Integer> ioMapSetIds = new ArrayList<Integer>();
				ioMapSetIds.add(ioBean.getId());
				DataProcessorGateway.refreshMapSets(ioMapSetIds);
			}
			catch (Exception e2) {
				e2.printStackTrace();
				//eat it - though we need to give a warning and send an email or maybe just throw??
			}
		} catch (GenericException e) {
			throw new GenericException(e);
		} catch (Exception e) {
			throw new GenericException(e);
		}
		return insertStatus;
	}

	/*
	 * Get Data from the HTML Form to IoBean
	 */
	private IoBean populateFields(HttpServletRequest request) throws GenericException {
		IoBean ioBean = new IoBean();
		String xml = request.getParameter("XML_DATA_FIELD");

		ioBean.setName(getParamAsString(request.getParameter("name")));
		ioBean.setDescription(getParamAsString(request.getParameter("description")));
		ioBean.setModelName(getParamAsString(request.getParameter("model_2")));
		ioBean.setDeviceModelInfoId(getParamAsInt(request.getParameter("deviceModelInfoId")));
		ioBean.setId(getParamAsInt(request.getParameter("id")));
		ioBean.setStatus(getParamAsInt(request.getParameter("status")));
		ioBean.setOrganization(getParamAsInt(request.getParameter("Organization")));
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("d");

		int size = nList.getLength();

		for (int i = 0; i < size; i++) {
			org.w3c.dom.Node n = nList.item(i);

			org.w3c.dom.Element e = (org.w3c.dom.Element) n;
			String ioId = e.getAttribute("pin");
			String attributeId = e.getAttribute("attribute");
			if (attributeId == "") {
				continue;
			}
			ioBean.setIoAttributeValue(getParamAsInt(ioId), getParamAsInt(attributeId));
			ioBean.addToAttributeDimensionMap(getParamAsInt(ioId), new Triple<Integer, Double, Double>(getParamAsInt(e.getAttribute("readingValue")), Misc.getParamAsDouble(e.getAttribute("min_val")), Misc.getParamAsDouble(e.getAttribute("max_val"))));
			ioBean.addToTransientandvalidonpowerDimensionMap(getParamAsInt(ioId), new Pair<Integer, Integer>(getParamAsInt(e.getAttribute("transient_time")),"on".equalsIgnoreCase(e.getAttribute("validOnPower")) == true ? 1 :0));
		}

		return ioBean;
	}

	/*
	 * When the Action is Edit. Get all the Data from Database for the particular to be edited.
	 */

	public IoBean processIo(HttpServletRequest request) throws GenericException {
		IoBean ioBean = null;
		IoDao ioDao = new IoDao();
		int id = 0;
		Connection conn = InitHelper.helpGetDBConn(request);
		try {

			id = getParamAsInt(request.getParameter("id"));

		} catch (NumberFormatException nfe) {
			logger.error(ExceptionMessages.INVALID_PARAM, nfe);
			throw new GenericException(nfe);
		}
		try {
			ioBean = new IoBean();
			ioBean = ioDao.fetchIo(conn, id);
		} catch (GenericException e) {
			throw new GenericException(e);
		}
		return ioBean;
	}

}

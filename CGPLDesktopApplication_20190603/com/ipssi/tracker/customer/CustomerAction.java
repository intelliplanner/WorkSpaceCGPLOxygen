package com.ipssi.tracker.customer;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.PortHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;

import com.ipssi.tracker.web.ActionI;
import static com.ipssi.tracker.common.util.Common.*;
import static com.ipssi.tracker.common.util.ApplicationConstants.*;
import java.sql.*;

public class CustomerAction implements ActionI {
	Logger logger = Logger.getLogger(CustomerAction.class);
	private final String EDIT_JSP = "/edit.jsp";
	private final String CUSTOMER_JSP = "/Customers.jsp";

	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		String actionForward = "";
		String action = "";
		boolean success = false;
		try {
			Connection conn = InitHelper.helpGetDBConn(request);
			action = getParamAsString(request.getParameter(ACTION));
			if (action.equals(CREATE)) {
				success = true;
			} else if (action.equals(DELETE)) {
				String checkDelete[] = request.getParameterValues("checkbox");
				CustomerDao custDao = new CustomerDao();
				if (custDao.deleteCustomer(conn,checkDelete))
					success = true;
				ArrayList<CustomerBean> customerList = new ArrayList<CustomerBean>();
				customerList = custDao.fetchCustomerData(conn);
				request.setAttribute("customerList", customerList);
			} else if (action.equals(EDIT)) {
				CustomerBean customer = processContact(request);
				success = isNull(customer) ? false : true;
				if (success)
					request.setAttribute("customer", customer);
			} else if (action.equals(SAVE)) {
				success = process(request);
				if (success) {
					ArrayList<CustomerBean> customerList = new ArrayList<CustomerBean>();
					CustomerDao custDao = new CustomerDao();
					customerList = custDao.fetchCustomerData(conn);
					request.setAttribute("customerList", customerList);
				}
			} else {
				success = true;
				ArrayList<CustomerBean> customerList = new ArrayList<CustomerBean>();
				CustomerDao custDao = new CustomerDao();
				customerList = custDao.fetchCustomerData(conn);
				request.setAttribute("customerList", customerList);
			}
		} catch (GenericException ex) {
			logger.error(ex);
			ex.printStackTrace();
			throw ex;
		}
		actionForward = sendResponse(action, success, request);
		return actionForward;
	}

	public CustomerBean processContact(HttpServletRequest request) throws GenericException {
		CustomerBean custBean = null;
		CustomerDao custDao = new CustomerDao();
		int custId = 0;
		Connection conn = InitHelper.helpGetDBConn(request);
		try {
			custId = getParamAsInt(request.getParameter("custId"));
		} catch (NumberFormatException nfe) {
			logger.error(ExceptionMessages.INVALID_PARAM, nfe);
			throw new GenericException(nfe);
		}
		try {
			custBean = new CustomerBean();
			custBean = custDao.fetchContactData(conn, custId);
		} catch (GenericException e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return custBean;
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if (action.equals(CREATE)) {
			if (success)
				actionForward = EDIT_JSP;
		} else if (action.equals(EDIT)) {
			if (success)
				actionForward = EDIT_JSP;
		} else if (action.equals(DELETE)) {
			actionForward = CUSTOMER_JSP;
		} else if (action.equals(SAVE)) {
			if (success)
				actionForward = CUSTOMER_JSP;
		} else {
			actionForward = CUSTOMER_JSP;
		}
		return actionForward;
	}

	public boolean process(HttpServletRequest request) throws GenericException {
		CustomerBean custBean = new CustomerBean();
		custBean = populateFields(request);
		boolean insertStatus = false;
		if (isNull(custBean))
			return insertStatus;
		Connection conn = InitHelper.helpGetDBConn(request);
		CustomerDao custDao = new CustomerDao();
		try {
			if (custBean.getId() <= 0) {
				custBean.setId(0);				
				SessionManager session = InitHelper.helpGetSession(request);
				insertStatus = custDao.insertData(conn, custBean, request, session.context);
			} else
				insertStatus = custDao.updateData(conn, custBean);
		} catch (GenericException e) {
			throw new GenericException(e);
		} catch (Exception e) {
			throw new GenericException(e);
		}
		return insertStatus;
	}

	private CustomerBean populateFields(HttpServletRequest request) throws GenericException {

		CustomerBean custBean = null;
		try {
			custBean = new CustomerBean();
			custBean.setCreatedBy(getParamAsString(request.getParameter("loginId")));
			custBean.setId(getParamAsInt(request.getParameter("custId")));
			custBean.setCustName(getParamAsString(request.getParameter("name")));
			custBean.setShortCode(getParamAsString(request.getParameter("shortcode")));
			if (custBean.getCustName().equals("") || custBean.getShortCode().equals(""))
				return null;
			custBean.setActiveFlag(getParamAsInt(request.getParameter("status")));
			custBean.setCustNote(getParamAsString(request.getParameter("note")));
			custBean.setCustType(getParamAsInt(request.getParameter("type")));
			custBean.setPartner(getParamAsString(request.getParameter("partner")));
			custBean.setNumDevices(getParamAsInt(request.getParameter("number")) <= 0 ? 0 : getParamAsInt(request.getParameter("number")));
			custBean.setLocation(getParamAsString(request.getParameter("location")));

			String idTable[] = request.getParameterValues("id_table");
			String nameTable[] = request.getParameterValues("name_table");
			String phoneTable[] = request.getParameterValues("phone_table");
			String mobileTable[] = request.getParameterValues("mobile_table");
			String emailTable[] = request.getParameterValues("email_table");
			String addressTable[] = request.getParameterValues("address_table");
			if (!isNull(nameTable)) {
				for (int i = 0; i < nameTable.length; i++) {
					CustomerContactBean custContact = new CustomerContactBean();
					if (!isNull(idTable) || idTable.length != 0) {
						custContact.setId(com.ipssi.gen.utils.Misc.getParamAsInt(idTable[i]));
					} else
						custContact.setId(0);
					custContact.setName(nameTable[i]);
					custContact.setPhone(phoneTable[i]);
					custContact.setMobile(mobileTable[i]);
					custContact.setEmail(emailTable[i]);
					custContact.setAddress(addressTable[i]);
					custBean.addCustContactList(custContact);
					custContact = null;
				}
			}
		} catch (NumberFormatException e) {
			logger.error(ExceptionMessages.INVALID_PARAM, e);
			throw new GenericException(e);
		} catch (Exception e) {
			throw new GenericException(e);
		}
		return custBean;
	}
}

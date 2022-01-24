package com.ipssi.tracker.customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.PortHelper;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;

import static com.ipssi.tracker.common.util.ApplicationConstants.*;


import static com.ipssi.tracker.common.util.Common.*;
import static com.ipssi.gen.utils.DBConnectionPool.*;

class CustomerDao {
	Logger logger = Logger.getLogger(CustomerDao.class);

	/**
	 * 
	 * @param custBean
	 * @return
	 * @throws GenericException
	 */
	boolean insertData(Connection conn, CustomerBean custBean, HttpServletRequest request, ServletContext context) throws GenericException {
		boolean insertStatus = false;
		String custName = custBean.getCustName();
		String shortCode = custBean.getShortCode();
		int activeFlag = custBean.getActiveFlag();
		String custNote = custBean.getCustNote();
		int custType = custBean.getCustType();
		String partner = custBean.getPartner();
		String location = custBean.getLocation();
		String createdBy = custBean.getCreatedBy();
		int numDevices = custBean.getNumDevices();
		int id = 0;
		int updateCustomer = 0;

		ArrayList<CustomerContactBean> custContactList = new ArrayList<CustomerContactBean>();

		custContactList = custBean.getCustContactList();
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		try {
			String insertCustomer = DBQueries.CUSTOMERS.INSERT_CUSTOMER;

			PreparedStatement stmt = conn.prepareStatement(insertCustomer);
			stmt.setString(1, custName);
			stmt.setString(2, shortCode);
			Misc.setParamInt(stmt, activeFlag, 3);
			stmt.setString(4, custNote);
			Misc.setParamInt(stmt, custType, 5);
			stmt.setString(6, partner);
			Misc.setParamInt(stmt,  numDevices, 7);
			stmt.setString(8, location);
			stmt.setTimestamp(9, sysDate);
			stmt.setString(10, createdBy);
			stmt.setTimestamp(11, sysDate);
			updateCustomer = stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
				id = rs.getInt(1);
			rs.close();
			stmt.close();
			boolean contStatus = true;
			if (!isNull(custContactList) || custContactList.size() > 0)
				contStatus = insertContact(custContactList, id, conn, sysDate);
			PortHelper portHelper = new PortHelper(request, context);			
			//classify1 of PortHelper goes to consolidation_status (1 in our case), classify2 will have customer id
			portHelper.addOrg(Misc.G_TOP_LEVEL_PORT, shortCode, custName, custNote, Misc.getUndefInt(), null, null, Misc.getUndefInt(), Misc.getUndefInt(), 2, null, Misc.getUndefDouble(), 2, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble(), 1, id, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), null, null, null, null, null);
		} catch (SQLException sqlEx) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		} 
		if (updateCustomer > 0)
			insertStatus = true;
		return insertStatus;
	}

	/**
	 * 
	 * @param custBean
	 * @return
	 * @throws GenericException
	 */
	boolean updateData(Connection conn, CustomerBean custBean) throws GenericException {

		boolean insertStatus = false;

		int id = custBean.getId();
		String custName = custBean.getCustName();
		String shortCode = custBean.getShortCode();
		int activeFlag = custBean.getActiveFlag();
		String custNote = custBean.getCustNote();
		int custType = custBean.getCustType();
		String partner = custBean.getPartner();
		String location = custBean.getLocation();
		int numDevices = custBean.getNumDevices();

		ArrayList<CustomerContactBean> custContactList = new ArrayList<CustomerContactBean>();

		custContactList = custBean.getCustContactList();
		int updateCustomer = 0;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		try {
			String insertCustomer = DBQueries.CUSTOMERS.UPDATE_CUSTOMER;

			PreparedStatement stmt = conn.prepareStatement(insertCustomer);
			stmt.setString(1, custName);
			stmt.setString(2, shortCode);
			Misc.setParamInt(stmt, activeFlag,3);
			stmt.setString(4, custNote);
			Misc.setParamInt(stmt,  custType, 5);
			stmt.setString(6, partner);
			Misc.setParamInt(stmt, numDevices, 7);
			stmt.setString(8, location);
			stmt.setTimestamp(9, sysDate);
			stmt.setInt(10, id);
			updateCustomer = stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
				id = rs.getInt(1);
			rs.close();
           stmt.close();
			insertContact(custContactList, id, conn, sysDate);
		} catch (SQLException sqlEx) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		} 
		if (updateCustomer > 0)
			insertStatus = true;
		return insertStatus;
	}

	/**
	 * 
	 * @param custContactList
	 * @param id
	 * @param conn
	 * @param sysDate
	 * @return
	 * @throws SQLException
	 */
	boolean insertContact(ArrayList<CustomerContactBean> custContactList, int id, Connection conn, Timestamp sysDate) throws SQLException {
		CustomerContactBean custContact = null;
		String fetchId = DBQueries.CUSTOMERS.FETCH_CONTACT_ID;
		String deleteContact = DBQueries.CUSTOMERS.DELETE_CONTACT_SELECTIVE;
		String insertContact = DBQueries.CUSTOMERS.INSERT_CONTACT;
		String updContact = DBQueries.CUSTOMERS.UPDATE_CONTACT;
		boolean insertStatus = false;
		int updateContact = 0;

		PreparedStatement getIdStmt = conn.prepareStatement(fetchId);
		PreparedStatement delStmt = conn.prepareStatement(deleteContact);
		PreparedStatement insStmt = conn.prepareStatement(insertContact);
		PreparedStatement updStmt = conn.prepareStatement(updContact);

		getIdStmt.setInt(1, id);
		ResultSet rs = getIdStmt.executeQuery();
		if (!isNull(rs)) {
			addContact: while (rs.next()) {
				for (int i = 0; i < custContactList.size(); i++) {
					if (custContactList.get(i).getId() == rs.getInt("id"))
						continue addContact;
				}
				delStmt.setInt(1, DELETED);
				delStmt.setInt(2, rs.getInt("id"));
				delStmt.addBatch();
			}
		}
		rs.close();
		delStmt.executeBatch();
		

		for (int i = 0; i < custContactList.size(); i++) {
			custContact = custContactList.get(i);
			int contId = custContact.getId();
			String name = custContact.getName();
			if (name.trim().equals(""))
				continue;
			String phone = custContact.getPhone();
			String mobile = custContact.getMobile();
			String email = custContact.getEmail();
			String address = custContact.getAddress();
			if (contId == 0) {
				insStmt.setInt(1, id);
				insStmt.setString(2, name);
				insStmt.setString(3, phone);
				insStmt.setString(4, mobile);
				insStmt.setString(5, email);
				insStmt.setString(6, address);
				insStmt.setTimestamp(7, sysDate);
				updateContact += insStmt.executeUpdate();
				
			} else {
				updStmt.setInt(1, id);
				updStmt.setString(2, name);
				updStmt.setString(3, phone);
				updStmt.setString(4, mobile);
				updStmt.setString(5, email);
				updStmt.setString(6, address);
				updStmt.setTimestamp(7, sysDate);
				updStmt.setInt(8, contId);
				updateContact += updStmt.executeUpdate();
			}
			custContact = null;
		}
		delStmt.close();
		updStmt.close();
		insStmt.close();
		getIdStmt.close();
		insertStatus = true;
		return insertStatus;
	}

	ArrayList<CustomerBean> fetchCustomerData(Connection conn) throws GenericException {
		ArrayList<CustomerBean> customerList = null;
		String fetchCustomer = DBQueries.CUSTOMERS.FETCH_CUSTOMER;
		ResultSet rs = null;
		try {
			PreparedStatement custSt = conn.prepareStatement(fetchCustomer);
			custSt.setInt(1, ApplicationConstants.DELETED);
			custSt.setInt(2, ApplicationConstants.DELETED);
			rs = custSt.executeQuery();
			if (!isNull(rs)) {
				customerList = new ArrayList<CustomerBean>();
				while (rs.next()) {
					CustomerBean custBean = new CustomerBean();
					ArrayList<CustomerContactBean> contactList = new ArrayList<CustomerContactBean>();
					custBean.setId(rs.getInt("id"));
					custBean.setCustName(getParamAsString(rs.getString("name")));
					custBean.setShortCode(getParamAsString(rs.getString("short_code")));
					custBean.setActiveFlag(Misc.getRsetInt(rs, "status", ApplicationConstants.ACTIVE));
					custBean.setCustNote(getParamAsString(rs.getString("notes")));
					custBean.setCustType(Misc.getRsetInt(rs, "type"));
					custBean.setPartner(getParamAsString(rs.getString("partner")));
					custBean.setCreatedOn(getParamAsDate(rs.getString("created_on"), new Date()));
					custBean.setCreatedBy(getParamAsString(rs.getString("created_by")));
					custBean.setNumDevices(getParamAsInt(rs.getString("device_count")));
					custBean.setLocation(getParamAsString(rs.getString("location")));
					CustomerContactBean contBean = new CustomerContactBean();
					contBean.setEmail(getParamAsString(rs.getString("email")));
					contactList.add(contBean);
					contBean = null;
					custBean.setCustContactList(contactList);
					customerList.add(custBean);
					custBean = null;
				}
			}
			rs.close();
			custSt.close();
		} catch (SQLException sqlEx) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		} 
		return customerList;
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws GenericException
	 */
	CustomerBean fetchContactData(Connection conn, int id) throws GenericException {
		CustomerBean custBean = null;
		String fetchContact = DBQueries.CUSTOMERS.FETCH_CONTACT;
		ResultSet rs = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchContact);
			contSt.setInt(1, ApplicationConstants.DELETED);
			contSt.setInt(2, id);
			rs = contSt.executeQuery();
			if (!isNull(rs)) {
				custBean = new CustomerBean();
				ArrayList<CustomerContactBean> contactList = new ArrayList<CustomerContactBean>();
				while (rs.next()) {
					if (isNull(custBean) || custBean.getId() == 0) {
						custBean.setId(id);
						custBean.setCustName(getParamAsString(rs.getString("name")));
						custBean.setShortCode(getParamAsString(rs.getString("short_code")));
						custBean.setActiveFlag(Misc.getRsetInt(rs, "status", ACTIVE));
						custBean.setCustNote(getParamAsString(rs.getString("notes")));
						custBean.setCustType(Misc.getRsetInt(rs, "type"));
						custBean.setPartner(getParamAsString(rs.getString("partner")));
						custBean.setCreatedOn(rs.getDate("created_on"));
						custBean.setCreatedBy(getParamAsString(rs.getString("created_by")));
						custBean.setNumDevices(Misc.getRsetInt(rs, "device_count",0));
						custBean.setLocation(getParamAsString(rs.getString("location")));
					}
					CustomerContactBean contBean = new CustomerContactBean();
					contBean.setId(rs.getInt("id1"));
					contBean.setName(getParamAsString(rs.getString("contact_name")));
					contBean.setPhone(getParamAsString(rs.getString("phone")));
					contBean.setMobile(getParamAsString(rs.getString("mobile")));
					contBean.setEmail(getParamAsString(rs.getString("email")));
					contBean.setAddress(getParamAsString(rs.getString("address")));
					contactList.add(contBean);
					contBean = null;
				}
				custBean.setCustContactList(contactList);
			}
			rs.close();
			contSt.close();
		} catch (SQLException sqlEx) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return custBean;
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws GenericException
	 */
	boolean deleteCustomer(Connection conn, String[] id) throws GenericException {
		boolean success = false;
		String deleteContact = DBQueries.CUSTOMERS.DELETE_CONTACT;
		String deleteCustomer = DBQueries.CUSTOMERS.DELETE_CUSTOMER;
		try {
			PreparedStatement delConStmt = conn.prepareStatement(deleteContact);
			PreparedStatement delCustStmt = conn.prepareStatement(deleteCustomer);
			for (int i = 0; i < id.length; i++) {
				delConStmt.setInt(1, ApplicationConstants.DELETED);
				delConStmt.setInt(2, Misc.getParamAsInt(id[i]));
				delCustStmt.setInt(1, ApplicationConstants.DELETED);
				delCustStmt.setInt(2, Misc.getParamAsInt(id[i]));
				delConStmt.addBatch();
				delCustStmt.addBatch();
			}
			delConStmt.executeBatch();
			delCustStmt.executeBatch();
			delConStmt.close();
			delCustStmt.close();
		} catch (SQLException sqlEx) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return success;
	}
}

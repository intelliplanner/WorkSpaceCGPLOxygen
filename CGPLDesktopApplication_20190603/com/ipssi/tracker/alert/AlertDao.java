package com.ipssi.tracker.alert;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.customer.CustomerBean;
import com.ipssi.tracker.customer.CustomerContactBean;

public class AlertDao {

	Logger logger = Logger.getLogger(AlertDao.class);

	boolean insertNotification(Connection conn, NotificationSetBean notificationSetBean, String reg) throws GenericException, SQLException {		
		int iHit = 0;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
		try {
			String insertNotification = DBQueries.ALERTS.INSERT_NOTIFICATIONSET;
			if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
				insertNotification = DBQueries.ALERTS.INSERT_REGION_NOTIFICATIONSET;
			}
			stmt = conn.prepareStatement(insertNotification);
			stmt.setString(1, notificationSetBean.getName());
			Misc.setParamInt(stmt, notificationSetBean.getStatus(),2);
			stmt.setDate(3, new java.sql.Date(notificationSetBean.getStatusFrom_date().getTime()));
			stmt.setDate(4, new java.sql.Date(notificationSetBean.getStatusTo_date().getTime()));
			Misc.setParamInt(stmt, notificationSetBean.getPortNodeId(), 5);
			stmt.setString(6, notificationSetBean.getNotes());
			stmt.setTimestamp(7, sysDate);
			if("region".equalsIgnoreCase(reg)){
				stmt.setInt(8, 1);
			}else if("role".equalsIgnoreCase(reg)){
				stmt.setInt(8, 2);
			}
			else
			{
				Misc.setParamInt(stmt, notificationSetBean.getLoadStatus(), 8);
				Misc.setParamInt(stmt, notificationSetBean.getCreateType(), 9);
				Misc.setParamInt(stmt, notificationSetBean.getOpstationSubtype(), 10);
				Misc.setParamInt(stmt, notificationSetBean.getRelativeDurOperator(), 11);
				Misc.setParamInt(stmt, notificationSetBean.getRelativeDurOperand1(), 12);
				Misc.setParamInt(stmt, notificationSetBean.getRelativeDurOperand2(), 13);
				stmt.setString(14, notificationSetBean.getLoadingAt());
				stmt.setString(15, notificationSetBean.getUnloadingAt());
				Misc.setParamInt(stmt, notificationSetBean.getEventDurOperator(), 16);
				Misc.setParamInt(stmt, notificationSetBean.getEventDurOperand1(), 17);
				Misc.setParamInt(stmt, notificationSetBean.getEventDurOperand2(), 18);
				Misc.setParamInt(stmt, notificationSetBean.getEventDistOperator(), 19);
				Misc.setParamInt(stmt, notificationSetBean.getEventDistOperand1(), 20);
				Misc.setParamInt(stmt, notificationSetBean.getEventDistOperand2(), 21);
			}
			iHit = stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()){
				notificationSetBean.setId(rs.getInt(1));

			}
			if(rs!=null)
			rs.close();
			if(stmt!=null)
			stmt.close();
			insertNotificationSetsRules(notificationSetBean, conn, sysDate, reg);
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (GenericException ex) {
			logger.error(ExceptionMessages.DB_CONN_PROBLEM, ex);
			throw new GenericException(ex);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally
		{
			if(rs!=null)
				rs.close();
				if(stmt!=null)
				stmt.close();	
		}
		
		if (iHit > 0)
			insertStatus = true;
		return insertStatus;
	}

	boolean updateNotification(Connection conn, NotificationSetBean notificationSetBean, String reg) throws GenericException, SQLException {
		int iHit = 0;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
		try {
			String insertNotification = DBQueries.ALERTS.UPDATE_NOTIFICATIONSET;
			if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
				insertNotification = DBQueries.ALERTS.UPDATE_REGION_NOTIFICATIONSET;
			}
			stmt = conn.prepareStatement(insertNotification);
			stmt.setString(1, notificationSetBean.getName());
			Misc.setParamInt(stmt, notificationSetBean.getStatus(),2);
			stmt.setDate(3, new java.sql.Date(notificationSetBean.getStatusFrom_date().getTime()));
			stmt.setDate(4, new java.sql.Date(notificationSetBean.getStatusTo_date().getTime()));
			Misc.setParamInt(stmt, notificationSetBean.getPortNodeId(), 5);
			stmt.setString(6, notificationSetBean.getNotes());
			stmt.setTimestamp(7, sysDate);
			if(!"region".equalsIgnoreCase(reg) && !"role".equalsIgnoreCase(reg)){
			Misc.setParamInt(stmt, notificationSetBean.getLoadStatus(), 8);
			Misc.setParamInt(stmt, notificationSetBean.getCreateType(), 9);
			Misc.setParamInt(stmt, notificationSetBean.getOpstationSubtype(), 10);
			Misc.setParamInt(stmt, notificationSetBean.getRelativeDurOperator(), 11);
			Misc.setParamInt(stmt, notificationSetBean.getRelativeDurOperand1(), 12);
			Misc.setParamInt(stmt, notificationSetBean.getRelativeDurOperand2(), 13);
			stmt.setString(14, notificationSetBean.getLoadingAt());
			stmt.setString(15, notificationSetBean.getUnloadingAt());
			Misc.setParamInt(stmt, notificationSetBean.getEventDurOperator(), 16);
			Misc.setParamInt(stmt, notificationSetBean.getEventDurOperand1(), 17);
			Misc.setParamInt(stmt, notificationSetBean.getEventDurOperand2(), 18);
			Misc.setParamInt(stmt, notificationSetBean.getEventDistOperator(), 19);
			Misc.setParamInt(stmt, notificationSetBean.getEventDistOperand1(), 20);
			Misc.setParamInt(stmt, notificationSetBean.getEventDistOperand2(), 21);
			Misc.setParamInt(stmt, notificationSetBean.getId(), 22);
			}
			else
				Misc.setParamInt(stmt, notificationSetBean.getId(), 8);
			iHit = stmt.executeUpdate();
			if(stmt!=null)
			stmt.close();

			insertNotificationSetsRules(notificationSetBean, conn, sysDate, reg);
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (GenericException ex) {
			logger.error(ExceptionMessages.DB_CONN_PROBLEM, ex);
			throw new GenericException(ex);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (iHit > 0)
			insertStatus = true;
		return insertStatus;
	}

	public boolean insertNotificationSetsRules(NotificationSetBean notificationSetBean, Connection conn, Timestamp sysDate, String reg) throws SQLException, GenericException {

		PreparedStatement pStat = conn.prepareStatement(DBQueries.ALERTS.DELETE_NOTIFICATIONSET_RULES);
		if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
			pStat = conn.prepareStatement(DBQueries.ALERTS.DELETE_NOTIFICATIONSET_REGION);
		}
		pStat.setInt(1, notificationSetBean.getId());
		pStat.execute();
		if(pStat!=null)
		pStat.close();
		ArrayList<RuleNotificationBean> ruleSetsNotificationList = notificationSetBean.getRuleNotificationBeanList();
		String insertNotification = DBQueries.ALERTS.INSERT_NOTIFICATIONSET_RULES;
		if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
			insertNotification = DBQueries.ALERTS.INSERT_NOTIFICATIONSET_REGION;
			
		}
		pStat = conn.prepareStatement(insertNotification);
		System.out.println("NotificationDao.insertNotificationSetsRules()  ::  ruleSetsNotificationList.size()  ::  "+ruleSetsNotificationList.size());
		for (Iterator<RuleNotificationBean> iterator = ruleSetsNotificationList.iterator(); iterator.hasNext();) {
			RuleNotificationBean ruleSetsNotificationBean = (RuleNotificationBean) iterator.next();

			Misc.setParamInt(pStat, notificationSetBean.getId(),2);
			Misc.setParamInt(pStat, ruleSetsNotificationBean.getRuleId(),1);
			Misc.setParamInt(pStat, ruleSetsNotificationBean.getType(),4);
			Misc.setParamInt(pStat, ruleSetsNotificationBean.getForThresholdLevel(),3);
			pStat.setDate(5, new java.sql.Date(ruleSetsNotificationBean.getValidFromDate().getTime()));
			pStat.setDate(6, new java.sql.Date(ruleSetsNotificationBean.getValidToDate().getTime()));
			Misc.setParamInt(pStat,  ruleSetsNotificationBean.getCustomerContactId(),7);
			pStat.setTime(8, java.sql.Time.valueOf(ruleSetsNotificationBean.getContactTimeFrom()+":"+"00"+":"+"00"));
			pStat.setTime(9, java.sql.Time.valueOf(ruleSetsNotificationBean.getContactTimeTo()+":"+"00"+":"+"00"));
			//			pStat.setTime(7, new java.sql.Time(ruleSetsNotificationBean.getContactTimeFromTime().getTime()));
			//			pStat.setTime(8, new java.sql.Time(ruleSetsNotificationBean.getContactTimeToTime().getTime()));
			//			java.sql.Time t = java.sql.Time.valueOf("");
			if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
				Misc.setParamInt(pStat, ruleSetsNotificationBean.getRegionId(),10);
				Misc.setParamInt(pStat, ruleSetsNotificationBean.getOpThreshold(),11);

			}

			pStat.addBatch();
		}
		if(pStat.executeBatch().length != ruleSetsNotificationList.size()) {
			
			pStat.close();
			throw new GenericException("No of records inserted is not equals to ruleSetsNotificationList size.");
		}
		else {
			pStat.close();
		}

		return true;

	}

	public NotificationSetBean getNotificationSetsById(Connection conn, int ruleId, String reg) throws GenericException, SQLException {
		logger.info("Getting all NotificationSets");
		String fetchNotificationSets = DBQueries.ALERTS.FETCH_NOTIFICATIONSET;
		PreparedStatement contSt=null;
		if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
			fetchNotificationSets = DBQueries.ALERTS.FETCH_REGION_NOTIFICATIONSET;
		}
		ResultSet rs = null;
		NotificationSetBean notificationSetBean = null;
		try {
		     contSt = conn.prepareStatement(fetchNotificationSets);
			contSt.setInt(1, ruleId);
			if("region".equalsIgnoreCase(reg)){
				contSt.setInt(2, 1);
			}else if("role".equalsIgnoreCase(reg)){
				contSt.setInt(2, 2);
			}
			rs = contSt.executeQuery();
			while (rs.next()) {
				notificationSetBean = new NotificationSetBean();
				notificationSetBean.setId(rs.getInt("id"));
				notificationSetBean.setName(rs.getString("name"));
				notificationSetBean.setStatus(Misc.getRsetInt(rs, "status", ApplicationConstants.ACTIVE));
				SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
				notificationSetBean.setStatusFrom(Misc.printDate(sdf,rs.getDate("status_from")));
				notificationSetBean.setStatusTo(Misc.printDate(sdf, rs.getDate("status_to")));
				notificationSetBean.setPortNodeId(Misc.getRsetInt(rs, "port_node_id",1));
				notificationSetBean.setNotes(rs.getString("notes"));
				notificationSetBean.setUpdatedOn(rs.getDate("updated_on"));
				if(!"region".equalsIgnoreCase(reg) && !"role".equalsIgnoreCase(reg)){
					notificationSetBean.setLoadStatus(Misc.getRsetInt(rs, "load_status"));
					notificationSetBean.setCreateType(Misc.getRsetInt(rs, "notification_create_type"));
					notificationSetBean.setOpstationSubtype(Misc.getRsetInt(rs, "opstation_subtype"));
					notificationSetBean.setRelativeDurOperator(Misc.getRsetInt(rs, "relative_dur_operator"));
					notificationSetBean.setRelativeDurOperand1(Misc.getRsetInt(rs, "relative_dur_operand1"));
					notificationSetBean.setRelativeDurOperand2(Misc.getRsetInt(rs, "relative_dur_operand2"));
					notificationSetBean.setLoadingAt(rs.getString("loading_at"));
					notificationSetBean.setUnloadingAt(rs.getString("unloading_at"));
					notificationSetBean.setEventDurOperator(Misc.getRsetInt(rs, "event_dur_operator"));
					notificationSetBean.setEventDurOperand1(Misc.getRsetInt(rs, "event_dur_operand1"));
					notificationSetBean.setEventDurOperand2(Misc.getRsetInt(rs, "event_dur_operand2"));
					notificationSetBean.setEventDistOperator(Misc.getRsetInt(rs, "event_dist_operator"));
					notificationSetBean.setEventDistOperand1(Misc.getRsetInt(rs, "event_dist_operand1"));
					notificationSetBean.setEventDistOperand2(Misc.getRsetInt(rs, "event_dist_operand2"));
				}
				notificationSetBean.setRuleNotificationBeanList(getNotificationSetRulesByNotificationSet(conn, notificationSetBean, reg));
			}
			if(rs!=null)
			rs.close();
			if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally
		{
			if(rs!=null)
				rs.close();
				if(contSt!=null)
				contSt.close();
		}
		return notificationSetBean;

	}

	public ArrayList<RuleNotificationBean> getNotificationSetRulesByNotificationSet(Connection conn, NotificationSetBean notificationSetBean, String reg) throws GenericException, SQLException {
		logger.info("Getting all NotificationSetsRules");
		String fetchNotificationSetsRules = DBQueries.ALERTS.FETCH_NOTIFICATIONSET_RULES;
		PreparedStatement contSt=null;
		if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
			fetchNotificationSetsRules = DBQueries.ALERTS.FETCH_NOTIFICATIONSET_REGIONS;
		}
		ResultSet rs = null;
		ArrayList<RuleNotificationBean> NotificationSetRulesList = null;
		try {
		    contSt = conn.prepareStatement(fetchNotificationSetsRules);
			contSt.setInt(1, notificationSetBean.getId());
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				NotificationSetRulesList = new ArrayList<RuleNotificationBean>();
				RuleNotificationBean ruleNotificationBean = null;
				while (rs.next()) {
					ruleNotificationBean = new RuleNotificationBean();
					if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
						ruleNotificationBean.setNotificationSetId(rs.getInt("region_notification_set_id"));
						ruleNotificationBean.setRuleId(rs.getInt("region_rule_id"));
					}else{
						ruleNotificationBean.setNotificationSetId(rs.getInt("notification_set_id"));
						ruleNotificationBean.setRuleId(rs.getInt("rule_id"));
					}
					ruleNotificationBean.setType(Misc.getRsetInt(rs, "type"));
					ruleNotificationBean.setForThresholdLevel(Misc.getRsetInt(rs,"for_threshold_level"));
					SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
					ruleNotificationBean.setValidFrom(Misc.printDate(sdf, rs.getDate("valid_from")));
					ruleNotificationBean.setValidTo(Misc.printDate(sdf,rs.getDate("valid_to")));
					ruleNotificationBean.setCustomerContactId(Misc.getRsetInt(rs, "customer_contact_id"));
					if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
						ruleNotificationBean.setRegionId(Misc.getRsetInt(rs, "op_station_id"));
						ruleNotificationBean.setOpThreshold(Misc.getRsetInt(rs, "threshold"));
					}
					java.sql.Time tempTime  = null;
					String tempStr = null;

					tempTime = rs.getTime("contact_time_from");
					tempStr = tempTime == null ? "" : tempTime.toString().substring(0, 2);					
					ruleNotificationBean.setContactTimeFrom(tempStr);

					tempTime = rs.getTime("contact_time_to");
					tempStr = tempTime == null ? "" : tempTime.toString().substring(0, 2);
					ruleNotificationBean.setContactTimeTo(tempStr);
					NotificationSetRulesList.add(ruleNotificationBean);
				}
			}
			if(rs!=null)
			rs.close();
			if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_RET_CONN_PROBLEM, ex);
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally
		{
			if(rs!=null)
				rs.close();
				if(contSt!=null)
				contSt.close();
		}
		return NotificationSetRulesList;

	}

	public List<NotificationSetBean> getAllNotificationSets(SessionManager session, String reg ,int status) throws GenericException, SQLException {
		logger.info("Getting all rulesets");
		Connection conn = session.getConnection();
		String fetchNotifications = DBQueries.ALERTS.FETCH_ALL_NOTIFICATIONSET;
		PreparedStatement contSt=null;
		if("region".equalsIgnoreCase(reg) || "role".equalsIgnoreCase(reg)){
			fetchNotifications = DBQueries.ALERTS.FETCH_ALL_REGION_NOTIFICATIONSET;
		}
		ResultSet rs = null;
		ArrayList<NotificationSetBean> ruleList = null;
		try {
			contSt = conn.prepareStatement(fetchNotifications);
			int v123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
			contSt.setInt(1, v123);
			contSt.setInt(2, status);

			if("region".equalsIgnoreCase(reg)){
				contSt.setInt(3, 1);
			}else if("role".equalsIgnoreCase(reg)){
				contSt.setInt(3, 2);
			}

			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				ruleList = new ArrayList<NotificationSetBean>();
				NotificationSetBean notificationSetBean = null;
				while (rs.next()) {
					notificationSetBean = new NotificationSetBean();
					notificationSetBean.setId(rs.getInt("id"));
					notificationSetBean.setName(rs.getString("name"));
					notificationSetBean.setStatus(Misc.getRsetInt(rs, "status", ACTIVE));
					SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
					notificationSetBean.setStatusFrom(Misc.printDate(sdf,rs.getDate("status_from")));
					notificationSetBean.setStatusTo(Misc.printDate(sdf, rs.getDate("status_to")));
					notificationSetBean.setPortNodeId(Misc.getRsetInt(rs, "port_node_id",1));
					notificationSetBean.setNotes(rs.getString("notes"));
					notificationSetBean.setUpdatedOn(rs.getDate("updated_on"));
					ruleList.add(notificationSetBean);
				}
			}
			if(rs!=null)
			rs.close();
			if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_RET_CONN_PROBLEM, ex);
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally{
			if(rs!=null)
				rs.close();
				if(contSt!=null)
				contSt.close();
		}
		return ruleList;

	}

	public static List<CustomerContactBean> getCustomerContacts(Connection conn,SessionManager session) throws GenericException, SQLException {
		String fetchCustContacts = DBQueries.ALERTS.FETCH_CUSTOMER_CONTACTS;
		ResultSet rs = null;
		PreparedStatement contSt=null;
		ArrayList<CustomerContactBean> customerList = null;
		try {
			contSt = conn.prepareStatement(fetchCustContacts);
			contSt.setInt(1,Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT));
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				customerList = new ArrayList<CustomerContactBean>();
				CustomerContactBean cosContactBean = null;
				while (rs.next()) {
					cosContactBean = new CustomerContactBean();
					cosContactBean.setId(rs.getInt("id"));
					cosContactBean.setName(rs.getString("contact_name"));
					cosContactBean.setMobile(rs.getString("mobile"));
					cosContactBean.setEmail(rs.getString("email"));
					customerList.add(cosContactBean);
				}
			}
			if(rs!=null)
			rs.close();
			if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			throw new GenericException(ex);
		}
		finally{
			if(rs!=null)
				rs.close();
				if(contSt!=null)
				contSt.close();
		}
		return customerList;
	}
	public List<CustomerBean> getCustomers(Connection conn) throws GenericException, SQLException {
		logger.info("Getting all customes");
		String fetchCustContacts = DBQueries.ALERTS.FETCH_CUSTOMER;
		PreparedStatement contSt=null;
		ResultSet rs = null;
		ArrayList<CustomerBean> customerList = null;
		try {
			contSt = conn.prepareStatement(fetchCustContacts);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				customerList = new ArrayList<CustomerBean>();
				CustomerBean cosContactBean = null;
				while (rs.next()) {
					cosContactBean = new CustomerBean();
					cosContactBean.setId(rs.getInt("id"));
					cosContactBean.setCustName(rs.getString("name"));

					customerList.add(cosContactBean);
				}
			}
			if(rs!=null)
			rs.close();
			if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_RET_CONN_PROBLEM, ex);
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally{
			if(rs!=null)
				rs.close();
				if(contSt!=null)
				contSt.close();
		}
		return customerList;

	}

	public CustomerContactBean insertContacts(Connection conn, List contactList, int customerId) throws SQLException, GenericException {

		Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
		int updateContact = 0;
		PreparedStatement pStat = null;
		ResultSet rs=null;
		CustomerContactBean contactBean = null;
		try {
			pStat = conn.prepareStatement(DBQueries.ALERTS.INSERT_CONTACT);
			System.out.println("AlertDao.insertContacts()  ::  contactList.size()  ::  "+contactList.size());
			for (Iterator<CustomerContactBean> iterator = contactList.iterator(); iterator.hasNext();) {
				contactBean = (CustomerContactBean) iterator.next();

				pStat.setInt(1, customerId);
				pStat.setString(2, contactBean.getName());
				pStat.setString(3, contactBean.getPhone());
				pStat.setString(4, contactBean.getMobile());
				pStat.setString(5, contactBean.getEmail());
				pStat.setString(6, contactBean.getAddress());
				pStat.setTimestamp(7, sysDate);
				updateContact += pStat.executeUpdate();
				rs = pStat.getGeneratedKeys();
				if (rs.next()){
					contactBean.setId(rs.getInt(1));
				}
				if(pStat!=null)
				pStat.close();
				if(rs!=null)
					rs.close();
			}
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		finally{
			if(pStat!=null)
				pStat.close();
				if(rs!=null)
					rs.close();
		}
		return contactBean;
	}

	public ArrayList<RuleNotificationBean> getOpRegion(Connection conn, SessionManager session) throws SQLException {
		// TODO Auto-generated method stub
		ArrayList<RuleNotificationBean> regionList = new ArrayList<RuleNotificationBean>();
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			ps = conn.prepareStatement(DBQueries.ALERTS.FETCH_OP_STATIONS);
			int v123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
			ps.setInt(1,v123 );
			ps.setInt(2,v123 );
			rs = ps.executeQuery();

			while(rs.next())
			{
				RuleNotificationBean opRegionBean = new RuleNotificationBean();
				opRegionBean.setRegionId(rs.getInt("id"));
				opRegionBean.setOpStationName(rs.getString("name"));
				opRegionBean.setStatus(rs.getInt("status"));

				regionList.add(	opRegionBean);

			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return regionList;
	}
	public ArrayList<RuleNotificationBean> populateRegion(Connection conn, int portNode) throws SQLException {
		// TODO Auto-generated method stub
		ArrayList<RuleNotificationBean> regionList = new ArrayList<RuleNotificationBean>();
		PreparedStatement ps=null;
		ResultSet rs=null;
		try {
			ps  = conn.prepareStatement(DBQueries.ALERTS.FETCH_OP_STATIONS);
			//	int v123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
			ps.setInt(1,portNode );
			ps.setInt(2,portNode );
			rs = ps.executeQuery();

			while(rs.next())
			{
				RuleNotificationBean regionBean = new RuleNotificationBean();
				regionBean.setRegionId(rs.getInt("id"));
				regionBean.setOpStationName(rs.getString("name"));
				regionBean.setStatus(rs.getInt("status"));

				regionList.add(regionBean);

			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return regionList;
	}

	//rahul 
	//update customerContact detail if they change
	public boolean updateCustCont(Connection conn,ArrayList<CustomerContactBean> customerContactBean) throws SQLException {

		if(customerContactBean==null)
			return false;
		
		else
		{   PreparedStatement ps=null;
		try{
				Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
				
				for(CustomerContactBean  custBean : customerContactBean){
					{
						try {
							ps = conn.prepareStatement(DBQueries.ALERTS.UPDATE_CONTACT);
							// ps.setString(1,custBean.getPhone() );
							ps.setString(1,custBean.getMobile());
							ps.setString(2,custBean.getEmail());
							ps.setTimestamp(3, sysDate);
							ps.setInt(4,custBean.getId());
							int rs = ps.executeUpdate();
						}
						catch (SQLException e) {e.printStackTrace();}
					}
				}
				if(ps!=null)
				ps.close();
			}
			catch(Exception e){e.printStackTrace();}
			finally{
				if(ps!=null)
					ps.close();
			}
		}
		return true;
	}
}


package com.ipssi.dashboard.customization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.common.util.Common;

public class CustDashBoardDao {

	Logger logger = Logger.getLogger(CustDashBoardDao.class);

	public boolean insertDashInfo(SessionManager session, DashBean dashBean) throws GenericException, SQLException {
		int iHit = 0;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		Connection conn = session.getConnection();
		try {
			String insertDash = DBQueries.CUSTDASHBOARD.INSERT_DASH_INFO;
			stmt = conn.prepareStatement(insertDash);
			Misc.setParamInt(stmt, dashBean.getPortNodeId(), 1);
			Misc.setParamInt(stmt, dashBean.getUserId(), 2);
			stmt.setString(3, dashBean.getPgContext());
			stmt.setString(4, dashBean.getPgTitle());
			stmt.setString(5, dashBean.getPgAction());
			Misc.setParamInt(stmt, dashBean.getStatus(),6);
			stmt.setString(7, dashBean.getHelp());
			iHit = stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()){
				dashBean.setId(rs.getInt(1));
			}
			if(stmt!=null)
				stmt.close();
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
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (iHit > 0)
			insertStatus = true;
		return insertStatus;
	}

	public boolean insertComponentInfo(SessionManager session, ComponentBean componentBean) throws GenericException, SQLException {
		int iHit = 0;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		Connection conn = session.getConnection();
		try {
			String insertComponent = DBQueries.CUSTDASHBOARD.INSERT_COMPONENT_INFO;
			stmt = conn.prepareStatement(insertComponent);
			Misc.setParamInt(stmt, componentBean.getId(),1);
			Misc.setParamInt(stmt, componentBean.getDashInfoId(),2);
			stmt.setString(3, componentBean.getTitle());
			Misc.setParamInt(stmt, componentBean.getDivLeft(), 4);
			Misc.setParamInt(stmt, componentBean.getDivTop(), 5);
			Misc.setParamInt(stmt, componentBean.getDivHeight(), 6);
			Misc.setParamInt(stmt, componentBean.getDivWidth(), 7);
			Misc.setParamInt(stmt, componentBean.getRefreshInt(), 8);
			stmt.setString(9, componentBean.getMiscellaneous());
			stmt.setString(10, componentBean.getXML());
			iHit = stmt.executeUpdate();
			if(stmt!=null)
				stmt.close();
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
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (iHit > 0)
			insertStatus = true;
		return insertStatus;
	}

	public DashBean getDashInfo(SessionManager session,int status,String pg_context,int type) throws GenericException, SQLException {
		String fetchDashList;
		ResultSet rs = null;
		Connection conn = session.getConnection();
		PreparedStatement contSt = null;
		DashBean dashBean = null;
		int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		User user = session.getUser();
		int userId = user.getUserId();
		try {
			if(type == 1){    //dashboard ,customized for user
				fetchDashList = DBQueries.CUSTDASHBOARD.FETCH_DASH_INFO_USER;
				contSt = conn.prepareStatement(fetchDashList);
				Misc.setParamInt(contSt, userId, 1);
				Misc.setParamInt(contSt, status, 2);
				contSt.setString(3, pg_context);
				rs = contSt.executeQuery();
			}
			if(type == 2){   // dashboard ,customized for org
				fetchDashList = DBQueries.CUSTDASHBOARD.FETCH_DASH_INFO_PORT; 
				contSt = conn.prepareStatement(fetchDashList);
				Misc.setParamInt(contSt, pv123, 1);
				Misc.setParamInt(contSt, status, 2);
				contSt.setString(3, pg_context);
				rs = contSt.executeQuery();
			}
			if(type == 3){   // dashboard(default)
				fetchDashList = DBQueries.CUSTDASHBOARD.FETCH_DASH_INFO; 
				contSt = conn.prepareStatement(fetchDashList);
				Misc.setParamInt(contSt, status, 1);
				contSt.setString(2, pg_context);
				rs = contSt.executeQuery();
			}
			while (rs.next()){
				dashBean = new DashBean();
				dashBean.setId(Misc.getRsetInt(rs, "id"));
				dashBean.setPgContext(rs.getString("pg_context"));
				dashBean.setPgTitle(rs.getString("pg_title"));
				dashBean.setStatus(Misc.getRsetInt(rs, "status"));
				dashBean.setPortNodeId(Misc.getRsetInt(rs, "port_node_id"));
				dashBean.setUserId(Misc.getRsetInt(rs, "user_id"));
				dashBean.setHelp(rs.getString("help"));
				dashBean.setPgAction(rs.getString("pg_action"));

			}
			rs.close();
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
		return dashBean;
	}
	
	public DashBean getDashInfo(SessionManager session,int status,String pg_context) {
		DashBean dashBean = null;
		try {
		for(int i = 1;i< 4; i++){
		dashBean = getDashInfo(session, status ,pg_context, i);
		if(dashBean != null)
		{
		break;
		}
		}
		} catch (GenericException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dashBean;
	}

	public ArrayList<ComponentBean> getComponentInfo(SessionManager session,DashBean dashBean) throws GenericException, SQLException {
		String fetchDashList = DBQueries.CUSTDASHBOARD.FETCH_COMPONENT_INFO;
		ResultSet rs = null;
		Connection conn = session.getConnection();
		PreparedStatement contSt=null;
		ArrayList<ComponentBean> componentList = null;
		int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		try {
			contSt = conn.prepareStatement(fetchDashList);
			Misc.setParamInt(contSt, dashBean.getId(), 1);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				componentList=new ArrayList<ComponentBean>();
				ComponentBean componentBean = null;
				while (rs.next()) {
					String url;
					componentBean = new ComponentBean();
					componentBean.setId(Misc.getRsetInt(rs, "component_id"));
					componentBean.setDashInfoId(Misc.getRsetInt(rs, "dash_info_id"));
					componentBean.setUidTag(rs.getString("uid_tag"));
					componentBean.setTitle(rs.getString("title"));
					componentBean.setXML(rs.getString("xml"));
					componentBean.setMiscellaneous(rs.getString("miscellaneous"));
					componentBean.setDivLeft(Misc.getRsetInt(rs, "div_left"));
					componentBean.setDivTop(Misc.getRsetInt(rs, "div_top"));
					componentBean.setDivHeight(Misc.getRsetInt(rs, "div_height"));
					componentBean.setDivWidth(Misc.getRsetInt(rs, "div_width"));
					componentBean.setRefreshInt(Misc.getRsetInt(rs, "refresh_int"));
					componentBean.setType(Misc.getRsetInt(rs, "type"));
					url = rs.getString("url");
					if(Misc.getRsetInt(rs, "type") == 1){
						url = url + "&page_context=" + dashBean.getPgContext();
						url = url + "&row=" + componentBean.getUidTag() + "&column=0";
						url = url + "&pv123=" + pv123;
						if(rs.getString("miscellaneous") != null)
							url = url + rs.getString("miscellaneous").replace("amp;","&");
					}
					else {
						if(componentBean.getXML() != null && !componentBean.getXML().equalsIgnoreCase(""))
						url = url + "&page_context=" + dashBean.getPgContext() + "&front_page=" + rs.getString("xml");
						url = url + "&row=" + componentBean.getUidTag() + "&column=0";
						if(componentBean.getType() == 3)
							url = url + rs.getString("miscellaneous").replace("amp;","&");
						if(componentBean.getType() == 4)
							componentBean.setTag(rs.getString("miscellaneous"));
						}
					url = url + "&refreshInt=" + Misc.getRsetInt(rs, "refresh_int");
					componentBean.setURL(url);
					if(componentBean!=null)
						componentList.add(componentBean);
				}

			}

			rs.close();
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
		return componentList;
	}
	public ArrayList<ComponentBean> getStandardComponentList(SessionManager session) throws GenericException, SQLException {
		String fetchDashList = DBQueries.CUSTDASHBOARD.FETCH_STANDARD_COMPONENT_LIST;
		ResultSet rs = null;
		Connection conn = session.getConnection();
		PreparedStatement contSt=null;
		ArrayList<ComponentBean> componentList = null;
		try {
			contSt = conn.prepareStatement(fetchDashList);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				componentList=new ArrayList<ComponentBean>();
				ComponentBean componentBean = null;
				while (rs.next()) {
					componentBean = new ComponentBean();
					componentBean.setId(Misc.getRsetInt(rs, "id"));
					componentBean.setTitle(rs.getString("name"));
					componentBean.setXML(rs.getString("xml"));
					componentBean.setType(Misc.getRsetInt(rs, "type"));
					if(componentBean!=null)
						componentList.add(componentBean);
				}

			}

			rs.close();
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
		return componentList;
	}

	public boolean updateComponentInfo(SessionManager session, ComponentBean componentBean) throws GenericException, SQLException {
		int iHit = 0;
		boolean updateStatus = false;
		PreparedStatement stmt=null;
		Connection conn = session.getConnection();
		try {
			String updateComponent = DBQueries.CUSTDASHBOARD.UPDATE_COMPONENT_INFO;
			stmt = conn.prepareStatement(updateComponent);

			stmt.setString(1, componentBean.getTitle());
			Misc.setParamInt(stmt, componentBean.getDivLeft(), 2);
			Misc.setParamInt(stmt, componentBean.getDivTop(), 3);
			Misc.setParamInt(stmt, componentBean.getDivHeight(), 4);
			Misc.setParamInt(stmt, componentBean.getDivWidth(), 5);
			Misc.setParamInt(stmt, componentBean.getRefreshInt(), 6);
			stmt.setString(7, componentBean.getUidTag());
			Misc.setParamInt(stmt, componentBean.getDashInfoId(),8);
			iHit = stmt.executeUpdate();
			if(stmt!=null)
				stmt.close();
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
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (iHit > 0)
			updateStatus = true;
		return updateStatus;
	}

	public boolean updateDashInfo(SessionManager session, DashBean dashBean) throws GenericException, SQLException {
		int iHit = 0;
		boolean updateStatus = false;
		PreparedStatement stmt=null;
		Connection conn = session.getConnection();
		try {
			String updateDash = DBQueries.CUSTDASHBOARD.UPDATE_DASH_INFO;
			stmt = conn.prepareStatement(updateDash);
			stmt.setString(1, dashBean.getPgContext());
			stmt.setString(2, dashBean.getPgTitle());
			Misc.setParamInt(stmt, dashBean.getStatus(),3);
			stmt.setString(4, dashBean.getHelp());
			Misc.setParamInt(stmt, dashBean.getId(), 5);
			iHit = stmt.executeUpdate();
			if(stmt!=null)
				stmt.close();
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
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (iHit > 0)
			updateStatus = true;
		return updateStatus;
	}

	public boolean deleteComponent(SessionManager session,int id,String uid_tag) throws GenericException, SQLException {
		int iHit = 0;
		boolean updateStatus = false;
		PreparedStatement stmt=null;
		Connection conn = session.getConnection();
		try {
			String delete = DBQueries.CUSTDASHBOARD.DELETE_COMPONENT_INFO;
			stmt = conn.prepareStatement(delete);
			Misc.setParamInt(stmt, id, 1);
			Misc.setParamInt(stmt, uid_tag, 2);
			iHit = stmt.executeUpdate();
			if(stmt!=null)
				stmt.close();
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
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (iHit > 0)
			updateStatus = true;
		return updateStatus;
	}
	public boolean dashLookup(SessionManager session,int id,String pg_context,boolean user) throws GenericException, SQLException {
		boolean lookupStatus = false;
		PreparedStatement stmt=null;
		String lookup=null;
		ResultSet rs=null;
		Connection conn = session.getConnection();
		if(user)
		{
			lookup = DBQueries.CUSTDASHBOARD.FETCH_DASH_INFO_USER;
		}
		else
		{
			lookup = DBQueries.CUSTDASHBOARD.FETCH_DASH_INFO_PORT;
		}
		try {
			stmt = conn.prepareStatement(lookup);
			Misc.setParamInt(stmt, id, 1);
			Misc.setParamInt(stmt, ApplicationConstants.ACTIVE, 2);
			stmt.setString(3,pg_context);
			rs = stmt.executeQuery();
			if(stmt!=null)
				stmt.close();
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
		finally
		{
			if(stmt!=null)
				stmt.close();
		}
		if (rs.next())
			lookupStatus = true;
		return lookupStatus;
	}
}

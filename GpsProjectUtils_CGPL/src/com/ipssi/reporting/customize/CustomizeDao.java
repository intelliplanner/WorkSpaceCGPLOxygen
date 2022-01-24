package com.ipssi.reporting.customize;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.apache.log4j.Logger;
import com.ipssi.gen.exception.ExceptionMessages;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.common.db.DBQueries;
import com.ipssi.reporting.common.util.Common;

public class CustomizeDao {
	Logger logger = Logger.getLogger(CustomizeDao.class);

	public int getMenuId(Connection conn, int userId, int portNodeId, String menuTag, String configFile, int row, int column) throws GenericException {
		logger.info("Getting menu");
		String fetchMenu = DBQueries.CUSTOMIZE.FETCH_MENUMASTER_FIND;
		ResultSet rs = null;
		try {	
			if (!Misc.isUndef(portNodeId))
				userId = Misc.getUndefInt();
			PreparedStatement contSt = conn.prepareStatement(fetchMenu);
			Misc.setParamInt(contSt, userId, 1);
			Misc.setParamInt(contSt, userId, 2);
			Misc.setParamInt(contSt, portNodeId, 3);
			Misc.setParamInt(contSt, portNodeId, 4);
			contSt.setString(5, menuTag);
			contSt.setString(6, configFile);
			contSt.setInt(7, row);
			contSt.setInt(8, column);
			rs = contSt.executeQuery();
			int retval = com.ipssi.gen.utils.Misc.getUndefInt();
				if (rs.next()) {
					retval = rs.getInt("id");
				}
				rs.close();
				contSt.close();
				return retval;
		} catch (SQLException sqlEx) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}

	}

	public int getMenuIdForDynamicMenu(Connection conn, int userId, int portNodeId, String menuTag, String configFile, int row, int column) throws GenericException {
		logger.info("Getting menu");
		String fetchMenu = DBQueries.CUSTOMIZE.GET_MENUID_FOR_DYNAMIC_MENU;
		ResultSet rs = null;
		try {	
//select menu_master.id from menu_master join (select leaf.id from port_nodes leaf join port_nodes anc on 
	//	(anc.id = ? and leaf.lhs_number <= anc.lhs_number and leaf.rhs_number >= anc.rhs_number) where leaf.id !=2 ) port_node on 
//		(port_node_id = port_node.id) where menu_tag = ? and component_file = ?		
			if (!Misc.isUndef(portNodeId))
				userId = Misc.getUndefInt();
			PreparedStatement contSt = conn.prepareStatement(fetchMenu);
			Misc.setParamInt(contSt, portNodeId, 1);
			contSt.setString(2, menuTag);
			contSt.setString(3, configFile);
			rs = contSt.executeQuery();
			int retval = com.ipssi.gen.utils.Misc.getUndefInt();
				if (rs.next()) {
					retval = rs.getInt("id");
				}
				rs.close();
				contSt.close();
				return retval;
		} catch (SQLException sqlEx) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}

	}

	
	public MenuBean getMenuById(Connection conn, int menuId) throws GenericException {
		logger.info("Getting menu");
		String fetchMenu = DBQueries.CUSTOMIZE.FETCH_MENU;
		ResultSet rs = null;
		MenuBean menuBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchMenu);
			contSt.setInt(1, menuId);
			rs = contSt.executeQuery();
				while (rs.next()) {
					menuBean = new MenuBean();
					menuBean.setId(rs.getInt("id"));
					menuBean.setPortNodeId(Misc.getRsetInt(rs,"port_node_id"));
					menuBean.setUserId(Misc.getRsetInt(rs,"user_id"));
					menuBean.setMenuTag(rs.getString("menu_tag"));
					menuBean.setComponentFile(rs.getString("component_file"));
					menuBean.setUiColumnBean(getUIColumnByMenu(conn, menuBean));
					menuBean.setUiParameterBean(getUIParamByMenu(conn, menuBean));
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
		return menuBean;

	}
	
	public MenuBean getMenuByUserId(Connection conn, int userId, String menuTag, String configFile, int row, int column) throws GenericException {
		logger.info("Getting menu");
		String fetchMenu = DBQueries.CUSTOMIZE.FETCH_MENUMASTER_USER;
		ResultSet rs = null;
		MenuBean menuBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchMenu);
			contSt.setInt(1, userId);
			contSt.setString(2, menuTag);
			contSt.setString(3, configFile);
			contSt.setInt(4, row);
			contSt.setInt(5, column);
			rs = contSt.executeQuery();
				while (rs.next()) {
					menuBean = new MenuBean();
					menuBean.setId(rs.getInt("id"));
					menuBean.setPortNodeId(rs.getInt("port_node_id"));
					menuBean.setUserId(rs.getInt("user_id"));
					menuBean.setMenuTag(rs.getString("menu_tag"));
					menuBean.setComponentFile(rs.getString("component_file"));
					menuBean.setUiColumnBean(getUIColumnByMenu(conn, menuBean));
					menuBean.setUiParameterBean(getUIParamByMenu(conn, menuBean));
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
		return menuBean;

	}
	
	public MenuBean getMenuByPortId(Connection conn, int portNodeId, String menuTag, String configFile, int row, int column) throws GenericException {
		logger.info("Getting menu");
		String fetchMenu = DBQueries.CUSTOMIZE.FETCH_MENUMASTER_PORT;
		ResultSet rs = null;
		MenuBean menuBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchMenu);
			contSt.setInt(1, portNodeId);
			contSt.setString(2, menuTag);
			contSt.setString(3, configFile);
			contSt.setInt(4, row);
			contSt.setInt(5, column);
			rs = contSt.executeQuery();
				while (rs.next()) {
					menuBean = new MenuBean();
					menuBean.setId(rs.getInt("id"));
					menuBean.setPortNodeId(rs.getInt("port_node_id"));
					menuBean.setUserId(rs.getInt("user_id"));
					menuBean.setMenuTag(rs.getString("menu_tag"));
					menuBean.setComponentFile(rs.getString("component_file"));
					menuBean.setUiColumnBean(getUIColumnByMenu(conn, menuBean));
					menuBean.setUiParameterBean(getUIParamByMenu(conn, menuBean));
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
		return menuBean;

	}
	
	public ArrayList<UIColumnBean> getUIColumnByMenu(Connection conn, MenuBean rbean) throws GenericException {
		logger.info("Getting UIColumnBean");
		String fetchUIColumnBean = DBQueries.CUSTOMIZE.FETCH_UI_COLUMN_BY_MENU;
		ResultSet rs = null;
		ArrayList<UIColumnBean> uIColumnBeanList = null;
		UIColumnBean uIColumnBean= null;
		PreparedStatement contSt = null;
		try {
			contSt = conn.prepareStatement(fetchUIColumnBean);
			contSt.setInt(1, rbean.getId());
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				uIColumnBeanList = new ArrayList<UIColumnBean>();
				while (rs.next()) {
					uIColumnBean = new UIColumnBean();
					uIColumnBean.setMenuId(rs.getInt("menu_id"));
					uIColumnBean.setColumnName(rs.getString("column_name"));
					uIColumnBean.setAttrName(rs.getString("attribute_name"));
					uIColumnBean.setAttrValue(rs.getString("attribute_value"));
					uIColumnBean.setRollup(rs.getInt("rollup"));
					uIColumnBeanList.add(uIColumnBean);
				}
			}
			rs.close();
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
//				returnConnectionToPool(conn);
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
		return uIColumnBeanList;
	}

	public ArrayList<UIParameterBean> getUIParamByMenu(Connection conn, MenuBean rbean) throws GenericException {
		logger.info("Getting UIParameterBean");
		String fetchRuleRegionThresholds = DBQueries.CUSTOMIZE.FETCH_UI_PARAM_BY_MENU;
		ResultSet rs = null;
		ArrayList<UIParameterBean> uIColumnBeanList = null;
		UIParameterBean uiParameterBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchRuleRegionThresholds);
			contSt.setInt(1, rbean.getId());
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				uIColumnBeanList = new ArrayList<UIParameterBean>();
				while (rs.next()) {
					uiParameterBean = new UIParameterBean(rs.getString("param_name"), rs.getString("param_value"), rs.getString("operator"), rs.getString("right_operand"));
					uiParameterBean.setMenuId(rs.getInt("menu_id"));
					uIColumnBeanList.add(uiParameterBean);
				}
				rs.close();
				contSt.close();
			}
//			returnConnectionToPool(conn);
		} catch (SQLException sqlEx) {
			try {
//				returnConnectionToPool(conn);
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
		return uIColumnBeanList;

	}

	public boolean delete(Connection conn, int menuId) throws GenericException {
		if (Misc.isUndef(menuId))
			return false;;
		boolean insertStatus = true;
		try {
			MenuBean menuBean = getMenuById(conn, menuId); //needed to handle make dirty
			PreparedStatement stmt = conn.prepareStatement(DBQueries.CUSTOMIZE.DELETE_UI_COLUMN_BY_MENU);
			stmt.setInt(1, menuId);
			stmt.execute();
			stmt.close();
			
			stmt = conn.prepareStatement(DBQueries.CUSTOMIZE.DELETE_UI_PARAM_BY_MENU);
			stmt.setInt(1, menuId);
			stmt.execute();
			stmt.close();
			
			stmt = conn.prepareStatement(DBQueries.CUSTOMIZE.DELETE_MENU);
			stmt.setInt(1, menuId);
			stmt.execute();
			stmt.close();
			if (menuBean != null) {
				if (!Misc.isUndef(menuBean.getPortNodeId())) {
					CacheManager.makePortConfigDirty(menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
				}
				else {
					CacheManager.makeUserConfigDirty	((int)menuBean.getUserId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
				}
			}
		}
		catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return insertStatus;
	}
	
	public boolean insertMenu(Connection conn, MenuBean menuBean) throws GenericException {
		int iHit = 0;
		boolean insertStatus = false;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		try {
			String insertMenu = DBQueries.CUSTOMIZE.INSERT_MENU;
			conn.setAutoCommit(false);
			PreparedStatement stmt = conn.prepareStatement(insertMenu);
			Misc.setParamInt(stmt, menuBean.getPortNodeId(), 1);
			Misc.setParamInt(stmt, Misc.isUndef(menuBean.getPortNodeId()) ? (int)menuBean.getUserId() : Misc.getUndefInt(), 2);
			
			stmt.setString(3, menuBean.getMenuTag());
			stmt.setString(4, menuBean.getComponentFile());
			stmt.setTimestamp(5, sysDate);
			stmt.setInt(6, menuBean.getRowId());
			stmt.setInt(7, menuBean.getColId());
			
			iHit = stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()){
				menuBean.setId(rs.getInt(1));
			}
			insertUIColumn(menuBean, conn, sysDate);
			insertUIParam(menuBean, conn, sysDate);
			rs.close();
			stmt.close();
			if (!Misc.isUndef(menuBean.getPortNodeId())) {
				CacheManager.makePortConfigDirty(menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
			}
			else {
				CacheManager.makeUserConfigDirty	((int)menuBean.getUserId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
			}
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
//				returnConnectionToPool(conn);
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
		if (iHit > 0)
			insertStatus = true;
		return insertStatus;
	}
	
	public boolean updateMenu(Connection conn, MenuBean menuBean) throws GenericException {
		int iHit = 0;
		boolean insertStatus = false;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		try {
			String insertMenu = DBQueries.CUSTOMIZE.UPDATE_MENU;
			PreparedStatement stmt = conn.prepareStatement(insertMenu);
			Misc.setParamInt(stmt, menuBean.getPortNodeId(), 1);
			Misc.setParamInt(stmt, Misc.isUndef(menuBean.getPortNodeId()) ? (int)menuBean.getUserId() : Misc.getUndefInt(), 2);
			
			stmt.setString(3, menuBean.getMenuTag());
			stmt.setString(4, menuBean.getComponentFile());
			stmt.setTimestamp(5, sysDate);
			stmt.setInt(6, menuBean.getRowId());
			stmt.setInt(7, menuBean.getColId());
			stmt.setInt(8, menuBean.getId());
			
			iHit = stmt.executeUpdate();
			insertUIColumn(menuBean, conn, sysDate);
			insertUIParam(menuBean, conn, sysDate);
			stmt.close();
			if (!Misc.isUndef(menuBean.getPortNodeId())) {
				CacheManager.makePortConfigDirty(menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
			}
			else {
				CacheManager.makeUserConfigDirty	((int)menuBean.getUserId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
			}
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
//				returnConnectionToPool(conn);
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
		if (iHit > 0)
			insertStatus = true;
		return insertStatus;
	}

	public boolean insertUIColumn(MenuBean menuBean, Connection conn, Timestamp sysDate) throws SQLException, GenericException {
		
		PreparedStatement pStat = conn.prepareStatement(DBQueries.CUSTOMIZE.DELETE_UI_COLUMN_BY_MENU);
		pStat.setInt(1, menuBean.getId());
		pStat.execute();
		pStat.close();
		ArrayList<UIColumnBean> uIColumnBeanList = menuBean.getUiColumnBean();
		pStat = conn.prepareStatement(DBQueries.CUSTOMIZE.INSERT_UI_COLUMN);
		//System.out.println("CustomizeDao.insertUIColumn()  ::  ");
		for (Iterator<UIColumnBean> iterator = uIColumnBeanList.iterator(); iterator.hasNext();) {
			UIColumnBean uIColumnBean = (UIColumnBean) iterator.next();
		pStat.setInt(1, menuBean.getId());
		pStat.setString(2, uIColumnBean.getColumnName());
		pStat.setString(3, uIColumnBean.getAttrName());
		pStat.setString(4, uIColumnBean.getAttrValue());
		pStat.setTimestamp(5, sysDate);
		pStat.setInt(6, uIColumnBean.getRollup());
		pStat.addBatch();
		}
		if(pStat.executeBatch().length != uIColumnBeanList.size()) {
			pStat.close();
			throw new GenericException("No of records inserted is not equals to uIColumnBeanList size.");
		}
		else {
		   pStat.close();	
		}
		
		return true;

	}
	
	public boolean insertUIParam(MenuBean menuBean, Connection conn, Timestamp sysDate) throws SQLException, GenericException {
		PreparedStatement pStat = conn.prepareStatement(DBQueries.CUSTOMIZE.DELETE_UI_PARAM_BY_MENU);
		pStat.setInt(1, menuBean.getId());
		pStat.execute();
		pStat.close();
		ArrayList<UIParameterBean> uIParameterBeanList = menuBean.getUiParameterBean();
		pStat = conn.prepareStatement(DBQueries.CUSTOMIZE.INSERT_UI_PARAM);
		for (Iterator<UIParameterBean> iterator = uIParameterBeanList.iterator(); iterator.hasNext();) {
			UIParameterBean uIParameterBean = (UIParameterBean) iterator.next();
		pStat.setInt(1, menuBean.getId());
		pStat.setString(2, uIParameterBean.getParamName());
		pStat.setString(3, uIParameterBean.getParamValue());
		pStat.setString(4, uIParameterBean.getOperator());
		pStat.setString(5, uIParameterBean.getRightOperand());
		pStat.setTimestamp(6, sysDate);
		pStat.addBatch();
		}
		if(pStat.executeBatch().length != uIParameterBeanList.size()) {
			pStat.close();
			throw new GenericException("No of records inserted is not equals to uIParameterBeanList size.");
		}
		else {
			pStat.close();
		}

		return true;

	}
		
	public void saveUserReportDetail(Connection conn, String name, String description, int portId, int userId, String xmlFileName, StringBuilder fileData) throws GenericException {
		try {

//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(fileData);
//			oos.flush();
//			oos.close();
//			byte[] data = baos.toByteArray();
			String query = "INSERT INTO report_detail (name, description, port_node_id, user_id, xml_file_name, xml_file ) values (? ,?, ?, ? ,?, ?)";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, name);
			ps.setString(2, description);
			ps.setInt(3, portId);
			ps.setInt(4, userId);
			ps.setString(5, xmlFileName);
			ps.setString(6, fileData.toString());
			ps.execute();
			ps.close();
			ps = null;
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			throw new GenericException(ex);
		}	
	}
	public ReportDetailVO getUserReportDetail(Connection conn, int id) throws GenericException {
		ResultSet rs = null;
		ReportDetailVO retVal = new ReportDetailVO();
		try {

			String query = "Select id, name, description, port_node_id, user_id, xml_file_name, xml_file from report_detail where id = ? ";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				retVal = new ReportDetailVO();
				retVal.setId(Misc.getRsetInt(rs, "id"));
				retVal.setPortId(Misc.getRsetInt(rs, "port_node_id"));
				retVal.setUserId(Misc.getRsetInt(rs, "user_id"));
				retVal.setName(Misc.getParamAsString(rs.getString("name")));
				retVal.setDescription(Misc.getParamAsString(rs.getString("description")));
				retVal.setFileName(Misc.getParamAsString(rs.getString("xml_file_name")));
				retVal.setXmlData(Misc.getParamAsString(rs.getString("xml_file")));
			}
			rs.close();
			ps.close();
		} 
		
		catch (Exception ex) {
			ex.printStackTrace();
			throw new GenericException(ex);
		}
		return retVal;
	}
	
	public ArrayList <ReportDetailVO> getUserReportDetail(Connection conn) throws GenericException {
		ResultSet rs = null;
		ArrayList <ReportDetailVO> retVal = new ArrayList<ReportDetailVO>();
		try {

			String query = "Select id, name, description, port_node_id, user_id, xml_file_name, xml_file from report_detail ";
			PreparedStatement ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			ReportDetailVO reportDetailVO = null;
			while (rs.next()) {
				reportDetailVO = new ReportDetailVO();
				reportDetailVO.setId(Misc.getRsetInt(rs, "id"));
				reportDetailVO.setPortId(Misc.getRsetInt(rs, "port_node_id"));
				reportDetailVO.setUserId(Misc.getRsetInt(rs, "user_id"));
				reportDetailVO.setName(Misc.getParamAsString(rs.getString("name")));
				reportDetailVO.setDescription(Misc.getParamAsString(rs.getString("description")));
				reportDetailVO.setFileName(Misc.getParamAsString(rs.getString("xml_file_name")));
				reportDetailVO.setXmlData(Misc.getParamAsString(rs.getString("xml_file")));
				retVal.add(reportDetailVO);
			}
			rs.close();
			ps.close();
		} 
		
		catch (Exception ex) {
			ex.printStackTrace();
			throw new GenericException(ex);
		}
		return retVal;
	}
	public boolean copyReportCustomization(Connection conn, int profileId, int portNodeId, int userId, String description, boolean isOrg ,int profileType ,boolean doColorCode) throws Exception {
		//profileType = 0 means User Based Customization profile
		//profileType = 1 means Org Based Customization profile
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Cache cache = Cache.getCacheInstance(conn);
		String profile_name = "";
		MiscInner.PortInfo orgDetail = cache.getPortInfo(portNodeId, conn);
		UserBean userBean = getUser(conn, userId);
		UserBean profileBean = getUser(conn, profileId);
		try {
			String matchedMenuId = "select distinct(m1.id) menu_id from menu_master m1 join menu_master m2 on "+
									" ((m1.menu_tag=m2.menu_tag) and (m1.component_file=m2.component_file)"+ 
									" and "+ 
									" (m1.user_id = ? or (? is null and m1.user_id is null)) and (m1.port_node_id = ? or (? is null and m1.port_node_id is null)) and "+
									" (m2.user_id = ? or (? is null and m2.user_id is null)) and (m2.port_node_id = ? or (? is null and m2.port_node_id is null)))";
			String deleteMatchedColorCodeId = "delete from colorcode where port_node_id =?"; 
			String insertColorCode = " insert into colorcode (report_id,name,notes,status,port_node_id,granularity,aggregation,temp) " +
									"(select report_id,name,notes,status,?,granularity,aggregation,id from colorcode where port_node_id =?)";
			String insertColorCodeDetail = "insert into colorcode_detail (colorcode_id,column_id,oder,thresholdone, thresholdtwo, check_for_all)" +
									" (select colorcode.id,column_id,oder,thresholdone, thresholdtwo, check_for_all from colorcode_detail join colorcode on (colorcode_detail.colorcode_id=colorcode.temp))";
			String clearTemp2 = "update colorcode set temp=null where temp is not null"; 
			String insertProfileDetail = "insert into profiling_details(id, name, type, profile_id,profile_name,profile_type,description) values (?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(matchedMenuId);
			Misc.setParamInt(ps, userId, 1);
			Misc.setParamInt(ps, userId, 2);
			Misc.setParamInt(ps, portNodeId, 3);
			Misc.setParamInt(ps, portNodeId, 4);
			if (profileType == 0)
			{
				Misc.setParamInt(ps, profileId, 5);
				Misc.setParamInt(ps, profileId, 6);
				Misc.setParamInt(ps, Misc.getUndefInt(), 7);
				Misc.setParamInt(ps, Misc.getUndefInt(), 8);
				profile_name = profileBean.getName();
			}
			else{
				Misc.setParamInt(ps, Misc.getUndefInt(), 5);
				Misc.setParamInt(ps, Misc.getUndefInt(), 6);
				Misc.setParamInt(ps, profileId, 7);
				Misc.setParamInt(ps, profileId, 8);
				profile_name = cache.getPortInfo(profileId, conn).m_name;
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				delete(conn,Misc.getRsetInt(rs, "menu_id"));
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			//copy menu_master,ui_column,ui_parameter data from one entity to another entity = {user,org}
			if (profileType == 0 && !isOrg)
			{
				copyMenuMaster(conn, profileId, portNodeId, userId, profileType);	
				//copyMenuMaster(conn, profileBean.getOrgId(),userBean.getOrgId() , Misc.getUndefInt(), 1);//to be check
			}
			else
				copyMenuMaster(conn, profileId, portNodeId, userId, profileType);
			ps = conn.prepareStatement(insertProfileDetail);
			if (!Misc.isUndef(portNodeId))
			{
				Misc.setParamInt(ps, portNodeId, 1);
				ps.setString(2, orgDetail.m_name);
			}
			else{
				Misc.setParamInt(ps, userId, 1);
				ps.setString(2, userBean.getName());
			}
			int profilingType = isOrg ? 1 : 0;
			Misc.setParamInt(ps, profilingType, 3);
			Misc.setParamInt(ps, profileId, 4);
			ps.setString(5, profile_name);
			Misc.setParamInt(ps, profileType, 6);
			ps.setString(7, description);
			ps.executeUpdate();
			ps.close();
			ps = null;
			if (Misc.isUndef(portNodeId))
				portNodeId = userBean.getOrgId();
			if (doColorCode && !Misc.isUndef(portNodeId) && profileBean != null){
				ps = conn.prepareStatement(deleteMatchedColorCodeId);
				Misc.setParamInt(ps, portNodeId, 1);
				ps.executeUpdate();
				ps.close();
				ps = null;
				ps = conn.prepareStatement(insertColorCode);
				Misc.setParamInt(ps, portNodeId, 1);
				Misc.setParamInt(ps, isOrg ? profileId : profileBean.getOrgId(), 2);
				ps.executeUpdate();
				ps.close();
				ps = null;
				ps = conn.prepareStatement(insertColorCodeDetail);
				ps.executeUpdate();
				ps.close();
				ps = null;
				ps = conn.prepareStatement(clearTemp2);
				ps.executeUpdate();
				ps.close();
				ps = null;
			}
		} 
		catch (Exception ex) {
			if (ps!=null)
				ps.close();
			if (rs!=null)
				rs.close();
			ex.printStackTrace();
			throw new GenericException(ex);
		}
		finally
		{
			if (ps!=null)
				ps.close();
			if (rs!=null)
				rs.close();    
		}
		return retval;
	}
	public boolean copyMenuMaster(Connection conn, int profileId, int portNodeId, int userId, int profileType) throws Exception {
		//profileType = 0 means User Based Customization profile
		//profileType = 1 means Org Based Customization profile
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String insertMenuMaster = " insert into menu_master (port_node_id,user_id,menu_tag,component_file,row_table,column_table,temp,updated_on)  " +
									" (select ?,?,menu_tag,component_file,row_table,column_table,id ,? from menu_master " +
									" where (user_id = ? or (? is null and user_id is null)) and (port_node_id = ? " +
									" or (? is null and port_node_id is null)) and menu_tag is not null)";
			String insertUIColumn = " insert into ui_column (menu_id, column_name, attribute_name, attribute_value,updated_on)" +
									" (select menu_master.id, column_name, attribute_name, attribute_value,? from ui_column join menu_master" +
									" on(ui_column.menu_id=menu_master.temp))";
			String insertUIParameter = " insert into ui_parameter(menu_id,param_name,param_value,updated_on)" +
									" (select menu_master.id,param_name,param_value,? from ui_parameter join menu_master on " +
									" (ui_parameter.menu_id=menu_master.temp))";
			String fetchInsertedMenu =" select  menu_tag,component_file,row_table,column_table from menu_master " +
									  " where (user_id = ? or (? is null and user_id is null)) and (port_node_id = ?" +
									  " or (? is null and port_node_id is null)) and menu_tag is not null";
			ps = conn.prepareStatement(insertMenuMaster);
			Misc.setParamInt(ps, portNodeId, 1);
			Misc.setParamInt(ps, userId, 2);
			ps.setTimestamp(3, sysDate);
			if (profileType == 0)
			{
				Misc.setParamInt(ps, profileId, 4);
				Misc.setParamInt(ps, profileId, 5);
				Misc.setParamInt(ps, Misc.getUndefInt(), 6);
				Misc.setParamInt(ps, Misc.getUndefInt(), 7);
			}
			else{
				Misc.setParamInt(ps, Misc.getUndefInt(), 4);
				Misc.setParamInt(ps, Misc.getUndefInt(), 5);
				Misc.setParamInt(ps, profileId, 6);
				Misc.setParamInt(ps, profileId, 7);
			}
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(insertUIColumn);
			ps.setTimestamp(1, sysDate);
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(insertUIParameter);
			ps.setTimestamp(1, sysDate);
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(fetchInsertedMenu);
			if (profileType == 0)
			{
				Misc.setParamInt(ps, profileId, 1);
				Misc.setParamInt(ps, profileId, 2);
				Misc.setParamInt(ps, Misc.getUndefInt(), 3);
				Misc.setParamInt(ps, Misc.getUndefInt(), 4);
			}
			else{
				Misc.setParamInt(ps, Misc.getUndefInt(), 1);
				Misc.setParamInt(ps, Misc.getUndefInt(), 2);
				Misc.setParamInt(ps, profileId, 3);
				Misc.setParamInt(ps, profileId, 4);
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				if (!Misc.isUndef(portNodeId)) {
					CacheManager.makePortConfigDirty(portNodeId, rs.getString("menu_tag"), rs.getString("component_file"), Misc.getRsetInt(rs, "row_table"), Misc.getRsetInt(rs, "column_table"));
				}
				else {
					CacheManager.makePortConfigDirty(userId, rs.getString("menu_tag"), rs.getString("component_file"), Misc.getRsetInt(rs, "row_table"), Misc.getRsetInt(rs, "column_table"));
				}
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			copyDashReport(conn, profileId, portNodeId, userId, profileType);
			retval = true;	
		}
		
		catch (Exception ex) {
			if (ps!=null)
				ps.close();
			if (rs!=null)
				rs.close();
			ex.printStackTrace();
			throw new GenericException(ex);
		} 
		finally
		{
			if (ps!=null)
				ps.close();
		}
		return retval;
	}
	public boolean copyDashReport(Connection conn, int profileId, int portNodeId, int userId, int profileType) throws Exception {
		//profileType = 0 means User Based Customization profile
		//profileType = 1 means Org Based Customization profile
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		boolean retval = false;
		PreparedStatement ps = null;
		try {
			String insertDash = " insert into dash_info (pg_context, pg_title, status, help, pg_action, user_id, port_node_id, temp) "+
								" (select pg_context, pg_title, status, help, pg_action, ?, ?, id from dash_info where ("+
								" user_id = ? or (? is null and user_id is null)) and (port_node_id = ? or (? is null and port_node_id is null)))";
			String insertDashComponent = " insert into dash_component (component_id,dash_info_id,title,div_left,div_top, div_height, div_width, refresh_int,  dash_component.status, xml, miscellaneous, temp)"+
										 " (select component_id,dash_info.id,title,div_left,div_top, div_height, div_width, refresh_int,  dash_component.status, xml, miscellaneous, uid_tag"+
										 " from dash_component join dash_info on (dash_component.dash_info_id=dash_info.temp))";
			String updateDashReports = " update menu_master  join menu_master m1 on (m1.id = menu_master.temp)" +
									   "join dash_component d1 on (d1.uid_tag = m1.row_table) join dash_component d2 on (d2.temp = d1.uid_tag)" +
									   "set menu_master.row_table=d2.uid_tag";
			String clearTemp1 = " update menu_master set temp=null where temp is not null";
			String clearTemp2 = "update dash_info set temp=null where temp is not null";
            String clearTemp3 = "update dash_component set temp=null where temp is not null";
			String deleteDash = "delete from dash_info using dash_info join (select d1.id dash_info_id from dash_info d1 join dash_info d2 on  ((d1.pg_context=d2.pg_context) and (d1.pg_action=d2.pg_action) and " +
								"(d1.user_id = ? or (? is null and d1.user_id is null)) and (d1.port_node_id = ? or (? is null and d1.port_node_id is null)) and " +
								"(d2.user_id = ? or (? is null and d2.user_id is null)) and (d2.port_node_id = ? or (? is null and d2.port_node_id is null)))) temp on" +
								" (temp.dash_info_id=dash_info.id)";
            String deleteDashComponent = "delete from dash_component where dash_info_id in (select d1.id dash_info_id from dash_info d1 join dash_info d2 on  ((d1.pg_context=d2.pg_context) and (d1.pg_action=d2.pg_action) and " +
            							 "(d1.user_id = ? or (? is null and d1.user_id is null)) and (d1.port_node_id = ? or (? is null and d1.port_node_id is null)) and " +
            							 "(d2.user_id = ? or (? is null and d2.user_id is null)) and (d2.port_node_id = ? or (? is null and d2.port_node_id is null))))";
			ps = conn.prepareStatement(deleteDashComponent);
			Misc.setParamInt(ps, userId, 1);
			Misc.setParamInt(ps, userId, 2);
			Misc.setParamInt(ps, portNodeId, 3);
			Misc.setParamInt(ps, portNodeId, 4);
			if (profileType == 0)
			{
				Misc.setParamInt(ps, profileId, 5);
				Misc.setParamInt(ps, profileId, 6);
				Misc.setParamInt(ps, Misc.getUndefInt(), 7);
				Misc.setParamInt(ps, Misc.getUndefInt(), 8);
			}
			else{
				Misc.setParamInt(ps, Misc.getUndefInt(), 5);
				Misc.setParamInt(ps, Misc.getUndefInt(), 6);
				Misc.setParamInt(ps, profileId, 7);
				Misc.setParamInt(ps, profileId, 8);
			}
			ps.executeUpdate();
            ps.close();
            ps = null;
        	ps = conn.prepareStatement(deleteDash);
			Misc.setParamInt(ps, userId, 1);
			Misc.setParamInt(ps, userId, 2);
			Misc.setParamInt(ps, portNodeId, 3);
			Misc.setParamInt(ps, portNodeId, 4);
			if (profileType == 0)
			{
				Misc.setParamInt(ps, profileId, 5);
				Misc.setParamInt(ps, profileId, 6);
				Misc.setParamInt(ps, Misc.getUndefInt(), 7);
				Misc.setParamInt(ps, Misc.getUndefInt(), 8);
			}
			else{
				Misc.setParamInt(ps, Misc.getUndefInt(), 5);
				Misc.setParamInt(ps, Misc.getUndefInt(), 6);
				Misc.setParamInt(ps, profileId, 7);
				Misc.setParamInt(ps, profileId, 8);
			}
			ps.executeUpdate();
            ps.close();
            ps = null;
            ps = conn.prepareStatement(insertDash);
			Misc.setParamInt(ps, userId, 1);
			Misc.setParamInt(ps, portNodeId, 2);
			if (profileType == 0)
			{
				Misc.setParamInt(ps, profileId, 3);
				Misc.setParamInt(ps, profileId, 4);
				Misc.setParamInt(ps, Misc.getUndefInt(), 5);
				Misc.setParamInt(ps, Misc.getUndefInt(), 6);
			}
			else{
				Misc.setParamInt(ps, Misc.getUndefInt(), 3);
				Misc.setParamInt(ps, Misc.getUndefInt(), 4);
				Misc.setParamInt(ps, profileId, 5);
				Misc.setParamInt(ps, profileId, 6);
			}
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(insertDashComponent);
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(updateDashReports);
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(clearTemp1);
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(clearTemp2);
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = conn.prepareStatement(clearTemp3);
			ps.executeUpdate();
			ps.close();
			ps = null;
			
		retval = true;	
		}
		
		catch (Exception ex) {
			if (ps!=null)
				ps.close();
			ex.printStackTrace();
			throw new GenericException(ex);
		}
		finally
		{
			if (ps!=null)
				ps.close();
		}
		return retval;
	}
	
	public UserBean getUser(Connection conn ,int userId) throws GenericException, SQLException
	{
		ResultSet rs = null;
		PreparedStatement ps = null;
		UserBean userBean = null;
		try {

			String query = "select user_data.user_id,user_data.user_name ,user_data.org_id,leaf.name org_name from (select users.id user_id,users.name user_name,user_preferences.value org_id from users left outer join user_preferences on (user_preferences.user_1_id=users.id) where ( user_preferences.user_1_id is not null and user_preferences.name='pv123') or user_preferences.user_1_id is null) user_data " +
					"left outer join port_nodes leaf on (leaf.id = user_data.org_id) where user_data.user_id=?";
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, userId, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				userBean = new UserBean();
				userBean.setId(Misc.getRsetInt(rs, "user_id"));
				userBean.setName(Misc.getParamAsString(rs.getString("user_name")));
				userBean.setOrgId(Misc.getRsetInt(rs, "org_id"));
				userBean.setOrgName(Misc.getParamAsString(rs.getString("org_name")));
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				throw new GenericException(ex);
			}
		finally
		{
			if (ps!=null)
				ps.close();
			if (rs!=null)
				rs.close();
		}
		return userBean;
	}
}
	
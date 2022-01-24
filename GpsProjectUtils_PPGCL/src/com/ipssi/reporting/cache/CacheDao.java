package com.ipssi.reporting.cache;

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
import com.ipssi.gen.utils.Common;
import com.ipssi.gen.utils.Misc;
import com.ipssi.reporting.common.db.DBQueries;
import com.ipssi.reporting.customize.CustomizeDao;
import com.ipssi.reporting.customize.MenuBean;
import com.ipssi.reporting.customize.UIColumnBean;
import com.ipssi.reporting.customize.UIParameterBean;

public class CacheDao {
	Logger logger = Logger.getLogger(CacheDao.class);
	
	public ArrayList<UIColumnBean> getUIColumnByMenu(Connection conn, MenuBean rbean) throws GenericException {
		logger.info("Getting UIColumnBean");
		
		String fetchUIColumnBean = DBQueries.CUSTOMIZE.FETCH_UI_COLUMN_BY_MENU;
		ResultSet rs = null;
		ArrayList<UIColumnBean> uIColumnBeanList = null;
		UIColumnBean uIColumnBean= null;
		try {		
			PreparedStatement contSt = conn.prepareStatement(fetchUIColumnBean);
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
	public void saveCustomInfo(Connection conn, long userId, int portNodeId, String menuTag, String configFile, int row, int column, double upperX, double upperY, double lowerX, double lowerY ) throws GenericException {
		CustomizeDao customizeDao = new CustomizeDao();
		MenuBean menuBean = new MenuBean();
		menuBean.setMenuTag(menuTag);
		menuBean.setPortNodeId(portNodeId);
		menuBean.setUserId(userId);
		menuBean.setRowId(row);
		menuBean.setColId(column);
		menuBean.setComponentFile(configFile);

		
		ArrayList<UIParameterBean> uiParameterBeanList = new ArrayList<UIParameterBean>();
		UIParameterBean up = new UIParameterBean("d9019", "" + upperX, null, null);
		uiParameterBeanList.add(up);
		up = new UIParameterBean("d9020", "" + upperY, null, null);
		uiParameterBeanList.add(up);
		up = new UIParameterBean("d9017", "" + lowerX, null, null);
		uiParameterBeanList.add(up);
		up = new UIParameterBean("d9018", "" + lowerY, null, null);
		uiParameterBeanList.add(up);
		menuBean.setUiParameterBean(uiParameterBeanList);
		
	    try {
	    	int menuIdFound = customizeDao.getMenuId(conn, (int)userId, menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
	    	menuBean.setId(menuIdFound);
	    	if(!Misc.isUndef(menuBean.getId()))
	    	{
				customizeDao.updateMenu(conn, menuBean);
	    	}else
	    		customizeDao.insertMenu(conn, menuBean);
		} catch (GenericException e) {
			System.out.println("CustomizeServlet.saveCustomize()"+ e.getMessage());
			e.printStackTrace();
		}


//		CustomizeDao cuDao = new CustomizeDao();
//		cuDao.insertMenu(conn, menuBean);
	}
}

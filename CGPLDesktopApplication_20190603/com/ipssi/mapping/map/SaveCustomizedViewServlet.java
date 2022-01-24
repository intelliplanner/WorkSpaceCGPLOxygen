package com.ipssi.mapping.map;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.geometry.Point;
import com.ipssi.reporting.cache.CacheDao;
import com.ipssi.reporting.customize.CustomizeDao;
import com.ipssi.reporting.customize.MenuBean;
import com.ipssi.reporting.customize.UIParameterBean;

import static com.ipssi.gen.utils.Common.*;

public class SaveCustomizedViewServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SaveCustomizedViewServlet.class);
	private static int _dbg=1;
	/**
	 * @throws IOException 
	 * @throws ServletException 
	 * 
	 */
	public void doGetFromJSP(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		 doGet(request,response);
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {

			double lowerX = getParamAsDouble(request.getParameter("lx"));
			double lowerY = getParamAsDouble(request.getParameter("ly"));

			double upperX = getParamAsDouble(request.getParameter("ux"));
			double upperY = getParamAsDouble(request.getParameter("uy"));

			int row = getParamAsInt(request.getParameter("row"));
			int column = getParamAsInt(request.getParameter("column"));

			String menuTag = getParamAsString(request.getParameter("menu_tag"));
			String configFile = getParamAsString(request.getParameter("component_file"));

			int portNodeId = getParamAsInt(request.getParameter("portNodeId"), Misc.G_TOP_LEVEL_PORT);// Integer.parseInt(m_session.getParameter("pv123"));
			long userId = getParamAsLong(request.getParameter("userId"));

			InitHelper.init(request, getServletContext());

			Connection conn = InitHelper.helpGetSession(request).getConnection();
			CacheDao cDao = new CacheDao();
			System.out.println("SaveCustomizedViewServlet.doGet() @@@@ : " + menuTag);
			boolean isSuperUser = Boolean.parseBoolean(getParamAsString(request.getParameter("superUser")));
			if (isSuperUser) {
				cDao.saveCustomInfo(conn, Misc.UNDEF_VALUE, portNodeId, menuTag, configFile, row, column, upperX, upperY, lowerX, lowerY);
			} else {

				 cDao.saveCustomInfo(conn, userId, Misc.UNDEF_VALUE, menuTag, configFile, row, column, upperX, upperY, lowerX, lowerY);
//				MenuBean menuBean = new MenuBean();
//				menuBean.setMenuTag(menuTag);
//				menuBean.setPortNodeId(portNodeId);
//				menuBean.setUserId(userId);
//				menuBean.setRowId(row);
//				menuBean.setColId(column);
//				menuBean.setComponentFile(configFile);
//				
//				ArrayList<UIParameterBean> uiParameterBeanList = new ArrayList<UIParameterBean>();
//				UIParameterBean up = new UIParameterBean("upperX", "" + upperX);
//				uiParameterBeanList.add(up);
//				up = new UIParameterBean("upperY", "" + upperY);
//				uiParameterBeanList.add(up);
//				up = new UIParameterBean("lowerX", "" + lowerX);
//				uiParameterBeanList.add(up);
//				up = new UIParameterBean("lowerY", "" + lowerY);
//				uiParameterBeanList.add(up);
//				menuBean.setUiParameterBean(uiParameterBeanList);
//				CustomizeDao customizeDao = new CustomizeDao();
//				customizeDao.insertMenu(conn, menuBean);
				
			}

			InitHelper.close(request, getServletContext());
		} catch (Exception e) {
			logger.error("RequestProcessorServlet.doGet()  Error while sending data to Queue", e);
		}
	}

	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

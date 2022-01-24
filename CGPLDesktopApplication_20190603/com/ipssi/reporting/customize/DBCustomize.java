package com.ipssi.reporting.customize;

import static com.ipssi.reporting.common.util.ApplicationConstants.CREATE;
import static com.ipssi.reporting.common.util.ApplicationConstants.DELETE;
import static com.ipssi.reporting.common.util.ApplicationConstants.EDIT;
import static com.ipssi.reporting.common.util.ApplicationConstants.SAVE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimCalc;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.PrivInfo;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class DBCustomize implements ActionI {

	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException,Exception {
		System.out.println("DBCustomizeServlet.processRequest()   $##################  ");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if(CREATE.equals(action) || EDIT.equals(action))
				actionForward = createCustomize(request, response);
			else if(SAVE.equals(action))
				actionForward = saveCustomize(request, response);
			else if (DELETE.equals(action)) {
				actionForward = deleteCustomize(request, response);
			}else if ("saveNewReport".equals(action)) {
				actionForward = saveNewReport(request, response);
			}else
				actionForward = createCustomize(request, response);
		} catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if (action.equals("CREATE_NEW_REPORT")){
			actionForward = "/dashboard_customize_close.jsp";
		}
		else if (action.equals(CREATE)){
			if (success)
				actionForward = "/dashboard_customize.jsp";
			else
				actionForward = "/dashboard_customize.jsp";
		}
		else if (action.equals(EDIT)){
			if (success)
				actionForward = "/dashboard_customize.jsp";
			else
				actionForward = "/dashboard_customize.jsp";
		}
		else if (action.equals(SAVE) || (action.equals(DELETE))) {
			if (success)
				actionForward = "/dashboard_customize_close.jsp";
			else
				actionForward = "/dashboard_customize_close.jsp";
		}
		else {
			actionForward = "/dashboard_customize.jsp";
		}
		return actionForward;
	}
	

	public String saveNewReport(HttpServletRequest request, HttpServletResponse response) throws Exception { //TODO
		   	return "CREATE_NEW_REPORT"; //editCustomize(request, response);
	}
	
	public String createCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		String menuTag = request.getParameter("menuTag");
		String configFile = request.getParameter("configFile");
		SessionManager session = InitHelper.helpGetSession(request);
		if (configFile == null || configFile.length() == 0) {
			session.getUser().loadParamsFromMenuSpec(session, menuTag);
			configFile = session.getParameter("configFile");
		}
		Cache cache = session.getCache();
		User user = session.getUser();
		int userId = user.getUserId();//Misc.getParamAsInt(request.getParameter("userId"));
		//if calling customize from link, we need to check if doing cust for port - a valid val for forPort indicates that
	   
		
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		
        DBConfig config = null;		
		int forPort = Misc.getParamAsInt(request.getParameter("forPort"));
		boolean doingForPort = !Misc.isUndef(portNodeId);
		
		if (!Misc.isUndef(forPort)) {
			doingForPort = true;
			 PrivInfo.TagInfo portCustrwTagInfo = cache.getPrivId("tr_docustomize_port");
			 int portCustPrivId = portCustrwTagInfo == null ? Misc.getUndefInt() : portCustrwTagInfo.m_write;
			forPort = user.getUserSpecificDefaultPort(session, forPort, portCustPrivId, DimInfo.getDimInfo(123));
	        for (MiscInner.PortInfo forPortInfo = cache.getPortNode(conn, forPort); forPortInfo != null && user.isPrivAvailable(session, portCustPrivId, forPortInfo.m_id); forPortInfo = forPortInfo.m_parent) {
	        	config = DBCache.getExplicitConfig(conn, portNodeId, menuTag);
	        	if (config != null && !config.isEmpty()) {
	        		portNodeId = forPortInfo.m_id;
	        	    session.setAttribute("applicableTo", Integer.toString(forPortInfo.m_id), false);
	        	    break;
	        	}
	        }	            
		}
		else {
		    config = DBCache.getConfig(conn, portNodeId, userId, menuTag, cache, configFile);
		}
		request.setAttribute("menuTag", menuTag);
		request.setAttribute("configFile", configFile);
		request.setAttribute("config", config);
		return CREATE;
	}
	
	
	public String deleteCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
		
		CustomizeDao customizeDao = new CustomizeDao();
		String menuTag = request.getParameter("menuTag");
		String configFile = request.getParameter("configFile");
		if (configFile == null || configFile.length() == 0) {
			session.getUser().loadParamsFromMenuSpec(session, menuTag);
			configFile = session.getParameter("configFile");
		}
		request.setAttribute("menuTag", menuTag);
		request.setAttribute("configFile", configFile);
		User user = session.getUser();
		int userId = user.getUserId();//Misc.getParamAsInt(request.getParameter("userId"));
		//if calling customize from link, we need to check if doing cust for port - a valid val for forPort indicates that
	   
		
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		DBCache.delete(conn, portNodeId, userId, menuTag);
		
		return DELETE;
	}
	
	public String saveCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		   SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
			int userId = session.getUser().getUserId();
			int portNodeId = Misc.getParamAsInt(request.getParameter("applicableTo"));
//			if(request.getParameter("portNodeId") != null && !"".equals(request.getParameter("portNodeId")))
//				menuBean.setPortNodeId(Integer.parseInt(request.getParameter("portNodeId")));
			String menuTag = request.getParameter("menuTag");
			String configFile = request.getParameter("configFile");
			if (configFile == null || configFile.length() == 0) {
				session.getUser().loadParamsFromMenuSpec(session, menuTag);
				configFile = session.getParameter("configFile");
			}		
			String xml = Common.getParamAsString(request.getParameter("XML_DATA_COLUMN"));
			
			org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
			DBConfig config = new DBConfig();
			ArrayList<DBItem> addTo = new ArrayList<DBItem>();
			config.setDbItems(addTo);
			for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
				if (n.getNodeType() != 1)
					continue;
				Element ce = (Element) n;
      		  DBItem item = new DBItem(Misc.getParamAsInt(ce.getAttribute("row_number")), Misc.getParamAsInt(ce.getAttribute("col_number")), Misc.getParamAsInt(ce.getAttribute( "row_span")), Misc.getParamAsInt(ce.getAttribute( "col_span")), ce.getAttribute("title"), ce.getAttribute("item_tag"), ce.getAttribute("front_page"), Misc.getParamAsInt(ce.getAttribute("component_id")), ce.getAttribute("addnl_param"));
      		  if (item.getComponentId() < 0 || Misc.isUndef(item.getCol()) || item.getTitle() == null || item.getTitle().length() == 0 || item.getItemId() == null || item.getItemId().length() == 0 || Misc.isUndef(item.getRow()) || Misc.isUndef(item.getCol()))
      			  continue;
      		  addTo.add(item);
			}
			DBCache.save(conn, config, portNodeId, userId, menuTag);
		    return SAVE; //editCustomize(request, response);
	}
}

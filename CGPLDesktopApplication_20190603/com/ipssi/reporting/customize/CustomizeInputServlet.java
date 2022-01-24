package com.ipssi.reporting.customize;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.PrivInfo;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.User;
import com.ipssi.input.InputTemplate;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class CustomizeInputServlet implements ActionI {
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException,Exception {
		System.out.println("CustomizeInputServlet.processRequest()   $##################  ");
		SessionManager session = InitHelper.helpGetSession(request);
		
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if(SAVE.equals(action))
				actionForward = saveCustomize(session);
			else if (DELETE.equals(action)) {
				actionForward = deleteCustomize(session);
			}
			else {
				actionForward = editCustomize(session);
			}
		} catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	private Pair<InputTemplate, ArrayList<Triple<Integer, String, String>>> getCustomizedAndFullTemplate(SessionManager session) throws Exception {
		String menuTag = session.request.getParameter("menuTag");
		String configFile = session.request.getParameter("configFile");
		
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		if (portNodeId < 0)
			portNodeId = Misc.getParamAsInt(session.getParameter("forport"));
		if (portNodeId < 0)
			portNodeId = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		InputTemplate customized = InputTemplate.getTemplate(session.getCache(), session.getConnection(), menuTag, portNodeId, configFile, session);
		
		InputTemplate core= InputTemplate.getTemplate(session.getCache(), session.getConnection(), menuTag, Misc.getUndefInt(), configFile, session);
		ArrayList<Triple<Integer, String, String>> selOptions = new ArrayList<Triple<Integer, String, String>>();
		HashMap<String, DimConfigInfo> customLookup = customized.populateLookupHelper();
		ArrayList<ArrayList<DimConfigInfo>> rows = core.getRows();
		selOptions.add(new Triple<Integer, String, String>(0, "Row Break", "Row Break"));
	
		for (int i=0, is = rows.size(); i<is;i++) {
			ArrayList<DimConfigInfo> row = rows.get(i);
			for (int j=0,js = row.size(); j<js; j++) {
				DimConfigInfo dc = row.get(j);
				if (dc.m_dimCalc == null || dc.m_dimCalc.m_dimInfo == null)
					continue;
				String colName = "d"+dc.m_dimCalc.m_dimInfo.m_id;
				DimConfigInfo inCustom = customLookup.get(colName);
				String label = inCustom == null ? dc.m_name : inCustom.m_name;
				selOptions.add(new Triple<Integer, String, String>(dc.m_dimCalc.m_dimInfo.m_id, dc.m_name, label));
			}
		}

		return new Pair<InputTemplate, ArrayList<Triple<Integer, String, String>>> (customized, selOptions);
	}
	
	public static StringBuilder printOptionsList(ArrayList<Triple<Integer, String, String>> optionsList, int selectedId, boolean printAny, String printInstructions) {
		StringBuilder sb = new StringBuilder();
		boolean foundMatch = false;
		if (!Misc.isUndef(selectedId)) {
			for (int i=0,is = optionsList == null ? 0 : optionsList.size();i<is;i++) {
				if (optionsList.get(i).first == selectedId) {
					foundMatch = true;
					break;
				}
			}
		}
		if (!foundMatch)
			selectedId = Misc.getUndefInt();
		if (printAny) {
			if (printInstructions == null) {
				printInstructions = " -- select -- ";
			}
			sb.append("<option attr ='' value='").append(Misc.getUndefInt()).append("' ").append(Misc.isUndef(selectedId) ? "selected" : "").append(">").append(printInstructions).append("</option>");
		}
		for (int i=0,is = optionsList == null ? 0 : optionsList.size();i<is;i++) {
			sb.append("<option attr ='").append(optionsList.get(i).third).append(" '  value='").append(optionsList.get(i).first).append("' ").append(optionsList.get(i).first == selectedId ? "selected" : "").append(">").append(optionsList.get(i).second).append("</option>");
		}
		return sb;
	}
	

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		
		
		if (action.equals(SAVE) || (action.equals(DELETE))) {
			if (success)
				actionForward = "/customizeDetailClose.jsp";
			else
				actionForward = "/customizeDetailClose.jsp";
		}
		else { //if (action.equals(EDIT)) {
			if (success)
				actionForward = "/customizeDetailInput.jsp";
			else
				actionForward = "/customizeDetailInput.jsp";
		}
		
		return actionForward;
	}
	
	
	public String deleteCustomize(SessionManager session) throws Exception {
		Connection conn = session.getConnection();
		String menuTag = session.request.getParameter("menuTag");
		String configFile = session.request.getParameter("configFile");
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		InputTemplate.deleteTemplate(conn, menuTag, null, portNodeId);
		return DELETE;
	}
	
	public String editCustomize(SessionManager session) throws Exception {
		Connection conn = session.getConnection();
		session.request.setAttribute("menuTag", session.request.getParameter("menuTag"));
		session.request.setAttribute("configFile", session.request.getParameter("configFile"));
		Pair<InputTemplate, ArrayList<Triple<Integer, String, String>>> retval = getCustomizedAndFullTemplate(session);
		session.request.setAttribute("calc", retval);
		return EDIT;
	}
	
	
	public String saveCustomize(SessionManager session) throws Exception {
		Connection conn = session.getConnection();
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		if (portNodeId < 0)
			portNodeId = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		String menuTag = session.getParameter("menuTag");
		String xml = session.getParameter("XML_DATA_COLUMN");
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("COLUMN");
		int size = nList.getLength();
	    if (size != 0) {//ensure last is proper
	    	org.w3c.dom.Node node =  nList.item(size-1);		        
	        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
	        if (element.getAttribute("colName") == null || "".equals(element.getAttribute("colName")) 
	        		|| element.getAttribute("colTitle") == null && "".equals(element.getAttribute("colTitle"))) {
	        	size--;
	        }
	    }
	    ArrayList<ArrayList<Triple<Integer, String, String>>> rows = new ArrayList<ArrayList<Triple<Integer, String, String>>>();
	    ArrayList<Triple<Integer, String, String>> row = null;
		for (int i=0; i<size ; i++){
		    org.w3c.dom.Node node =  nList.item(i);
		    org.w3c.dom.Element element = (org.w3c.dom.Element) node;
		    if (element.getAttribute("colName") != null && !"".equals(element.getAttribute("colName")) 
		        && element.getAttribute("colTitle") != null && !"".equals(element.getAttribute("colTitle"))) {
		    	int id = Misc.getParamAsInt(element.getAttribute("colName"));
		    	if (id == 0)
		    		row = null;
		    	else if (id < 0)
		    		continue;
		    	else {
		    		if (row == null) {
		    			row = new ArrayList<Triple<Integer, String, String>>();
		    			rows.add(row);
		    		}
		    		row.add(new Triple<Integer, String, String>(id, Misc.getParamAsString(element.getAttribute("colTitle")), Misc.getParamAsString(element.getAttribute("default_val"))));
		    	}
		    }
		}
		int templateId = InputTemplate.updateOrCreateTemplate(conn, menuTag, null, portNodeId, rows);
		session.request.setAttribute("menuId", Integer.toString(templateId));
		return SAVE; //editCustomize(request, response);
	}
	
		
}

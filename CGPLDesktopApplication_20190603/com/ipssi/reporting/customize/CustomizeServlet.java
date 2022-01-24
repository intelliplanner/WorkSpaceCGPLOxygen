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
import com.ipssi.gen.utils.PrivInfo;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class CustomizeServlet implements ActionI {

	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException,Exception {
		System.out.println("CustomizeServlet.processRequest()   $##################  ");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if(CREATE.equals(action))
				actionForward = createCustomize(request, response);
			else if(SAVE.equals(action))
				actionForward = saveCustomize(request, response);
			else if(EDIT.equals(action))
				actionForward = editCustomize(request, response);
			else if (DELETE.equals(action)) {
				actionForward = deleteCustomize(request, response);
			}else if ("saveNewReport".equals(action)) {
				actionForward = saveNewReport(request, response);
			}else if ("view_profile".equals(action)) {
				actionForward = viewProfile(request, response);
			}else if ("save_profile".equals(action)) {
				actionForward = saveProfile(request, response);
			}else
				actionForward = createCustomize(request, response);
		} catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	private String viewProfile(HttpServletRequest request, HttpServletResponse response) throws GenericException, SQLException {
		return "view_profile";
	}

	private String saveProfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int copyFromType = Misc.getParamAsInt(request.getParameter("copying_type"));
		int copyToType = Misc.getParamAsInt(request.getParameter("profiling_type"));
		int userId = Misc.getUndefInt();
		int orgId = Misc.getUndefInt();
		int profileId = Misc.getUndefInt();
		String description = Misc.getParamAsString(request.getParameter("description"));
		boolean isOrg = copyToType == 1;
		boolean doColorCode = Boolean.parseBoolean((request.getParameter("doColorCode")));
		if (copyToType == 0)
			userId = Misc.getParamAsInt(request.getParameter("user_id"));
		else
			orgId = Misc.getParamAsInt(request.getParameter("org_id"));
		if (copyFromType == 0)
			profileId = Misc.getParamAsInt(request.getParameter("copy_user_id"));
		else
			profileId = Misc.getParamAsInt(request.getParameter("copy_org_id"));
		CustomizeDao customizeDao = new CustomizeDao();
		Connection conn = InitHelper.helpGetSession(request).getConnection();
		customizeDao.copyReportCustomization(conn, profileId, orgId, userId, description, isOrg, copyFromType, doColorCode);
		return "view_profile";
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if (action.equals("CREATE_NEW_REPORT")){
			actionForward = "/customizeDetailClose.jsp";
		}
		else if (action.equals(CREATE)){
			if (success)
				actionForward = "/customizeDetail.jsp";
			else
				actionForward = "/customizeDetail.jsp";
		}
		else if (action.equals(EDIT)){
			if (success)
				actionForward = "/customizeDetail.jsp";
			else
				actionForward = "/customizeDetail.jsp";
		}
		else if (action.equals(SAVE) || (action.equals(DELETE))) {
			if (success)
				actionForward = "/customizeDetailClose.jsp";
			else
				actionForward = "/customizeDetailClose.jsp";
		}
		else if (action.equals("view_profile")){
			if (success)
				actionForward = "/report_profile.jsp";
			else
				actionForward = "/report_profile.jsp";
		}
		else {
			actionForward = "/customizeDetail.jsp";
		}
		return actionForward;
	}
	

	public String saveNewReport(HttpServletRequest request, HttpServletResponse response)
	throws Exception {
		   SessionManager session = InitHelper.helpGetSession(request);
			Connection conn = InitHelper.helpGetDBConn(request);
			String configFile = request.getParameter("configFile");
			FrontPageInfo fpInfo = CacheManager.getBaseFrontPageInfo(conn,configFile);
			HashMap<Integer, String> idColumnLookup = new HashMap<Integer, String>();
			HashMap<Integer, String> idSearchLookup = new HashMap<Integer, String>();
			for (int i=0,is=fpInfo.m_frontInfoList.size();i<is;i++){
				DimConfigInfo dimConfigInfo = (DimConfigInfo) fpInfo.m_frontInfoList.get(i);
				if(dimConfigInfo != null && dimConfigInfo.m_dimCalc.m_dimInfo != null)
					idColumnLookup.put(dimConfigInfo.m_dimCalc.m_dimInfo.m_id, dimConfigInfo.m_name);
			}
			
/*			for (int i=0,is=fpInfo.m_frontSearchCriteria.size();i<is;i++){
				List dimConfigInfoList= (List)fpInfo.m_frontSearchCriteria.get(i);
				for (int j = 0; j < dimConfigInfoList.size(); j++) { 
					DimConfigInfo dimConfigInfo = (DimConfigInfo) dimConfigInfoList.get(j);
					if(dimConfigInfo != null && dimConfigInfo.m_dimCalc.m_dimInfo != null)
						idSearchLookup.put(dimConfigInfo.m_dimCalc.m_dimInfo.m_id, dimConfigInfo.m_name);
				}
			}*/
			//
			// Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName
			String filePath = Misc.getServerConfigPath()+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+configFile;
		    byte[] buffer = new byte[(int) new File(filePath).length()];
		    FileInputStream f = new FileInputStream(filePath);
		    f.read(buffer);
		    StringBuilder fileData = new StringBuilder(new String(buffer));
			
//			fpInfo.
//				MenuBean menuBean = new MenuBean();
//				int userId = session.getUser().getUserId();
//				
//				menuBean.setUserId(userId);
//				menuBean.setRowId(Misc.getParamAsInt(request.getParameter("row"),0));
//				menuBean.setColId(Misc.getParamAsInt(request.getParameter("col"),0));
//				menuBean.setPortNodeId(Common.getParamAsInt(request.getParameter("applicableTo")));
//				if(request.getParameter("portNodeId") != null && !"".equals(request.getParameter("portNodeId")))
//					menuBean.setPortNodeId(Integer.parseInt(request.getParameter("portNodeId")));
//				menuBean.setMenuTag(request.getParameter("menuTag"));
//				menuBean.setComponentFile(configFile);
				String reportName = Common.getParamAsString(request.getParameter("reportName"));
				String reportDescription = Common.getParamAsString(request.getParameter("reportDescription"));
				int portId = Common.getParamAsInt(request.getParameter("applicableTo"));
				int userId = session.getUser().getUserId();
				String xml = Common.getParamAsString(request.getParameter("XML_DATA_COLUMN"));
				xml = Common.getParamAsString(request.getParameter("COLUM"));
				System.out.println("CustomizeServlet.saveCustomize()  :  XML_DATA_COLUMN : "  +  xml);
				org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
				
			    org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("COLUMN");
//			    List<UIColumnBean> uiColumnBeanList = menuBean.getUiColumnBean();
			    int size = nList.getLength();
//			    UIColumnBean uiColumnBean = null;
			    for ( int i=0; i<size ; i++){
			        org.w3c.dom.Node node =  nList.item(i);
			        
			        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			        if(element.getAttribute("colName") != null && !"".equals(element.getAttribute("colName")) 
			        		&& element.getAttribute("colTitle") != null && !"".equals(element.getAttribute("colTitle"))){
//			        uiColumnBean = new UIColumnBean();
//			        uiColumnBean.setColumnName(element.getAttribute("colName"));
//			        uiColumnBean.setAttrName(element.getAttribute("colTitle"));
//			        uiColumnBean.setAttrValue(element.getAttribute("colTitle"));
//			        uiColumnBean.setAttrValue(element.getAttribute("colTitle"));
//			        
//					uiColumnBeanList.add(uiColumnBean);
					String id = element.getAttribute("colName");
					
					int intId = Misc.getParamAsInt(id.substring(1));
					idColumnLookup.remove(intId);
					
			        }
			    }
			    Set<Entry<Integer, String>> idColEntry = idColumnLookup.entrySet();
				for (Entry<Integer, String> entry1: idColEntry) {
					int colId = entry1.getKey();
//					String colName = entry1.getValue();
					int idIndex = fileData.indexOf("id=\""+colId);
					if(idIndex > 0){
						int begIndex = fileData.substring(0,idIndex).lastIndexOf("<");
						int endIndex = fileData.substring(idIndex).indexOf("/>");
						fileData.delete(begIndex, (idIndex + endIndex) + 2);
					}
				}
//			    menuBean.setUiColumnBean((ArrayList<UIColumnBean>)uiColumnBeanList);
			   
			    /*xml = Common.getParamAsString(request.getParameter("XML_DATA_PARAM"));
			    System.out.println("CustomizeServlet.saveCustomize() ==  XML_DATA_PARAM  ===> "+xml);
				xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
				
			    nList= xmlDoc.getElementsByTagName("PARAM");
//			    List<UIParameterBean> uiParameterBeanList = menuBean.getUiParameterBean();
			    size = nList.getLength();
			    UIParameterBean uiParameterBean = null;
			    for ( int i=0; i<size ; i++){
			        org.w3c.dom.Node node =  nList.item(i);
			        
			        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			        if(element.getAttribute("propertyName") != null && !"".equals(element.getAttribute("propertyName")) 
			        		&& element.getAttribute("propertyValue") != null && !"".equals(element.getAttribute("propertyValue"))){
//			        	uiParameterBean = new UIParameterBean();
//			        	uiParameterBean.setParamName(element.getAttribute("propertyName"));
//			        	uiParameterBean.setParamValue(element.getAttribute("propertyValue"));
//			        
//			        	uiParameterBeanList.add(uiParameterBean);
			        	String id = element.getAttribute("propertyName");
						
						int intId = Misc.getParamAsInt(id.substring(1));
						idSearchLookup.remove(intId);
			        }
			    }
			    idColEntry = idSearchLookup.entrySet();
				for (Entry<Integer, String> entry1: idColEntry) {
					int colId = entry1.getKey();
//					String colName = entry1.getValue();
					int idIndex = fileData.indexOf(""+colId, fileData.indexOf("<search>"));
					if(idIndex > 0){
						int begIndex = fileData.substring(0,idIndex).lastIndexOf("<");
						int endIndex = fileData.substring(idIndex).indexOf("/>");
						fileData.delete(begIndex, (idIndex + endIndex) + 2);
					}
				}*/
				String xmlFileName = userId + "_" +configFile;
				CustomizeDao customizeDao = new CustomizeDao();
				customizeDao.saveUserReportDetail(conn, reportName, reportDescription, portId, userId, xmlFileName, fileData);
//			    menuBean.setUiParameterBean((ArrayList<UIParameterBean>)uiParameterBeanList);
//			    session.setAttributeObj("menuBean", menuBean);
			    
//			    fileData.

//			    FileInputStream inp = new FileInputStream(Misc.getServerConfigPath()+System.getProperty("file.separator")+configFile);
			    
//			request.setAttribute("menuId", String.valueOf(menuBean.getId()));
			return "CREATE_NEW_REPORT"; //editCustomize(request, response);
		}
	

	
	private void getRemovingDimConfig(FrontPageInfo fPageInfo, MenuBean menuBean) throws IOException, ClassNotFoundException{
		FrontPageInfo fpInfo = null;
		HashMap<String, Integer> idColumnLookup = new HashMap<String, Integer>();
		if(fPageInfo != null){
			fpInfo = getDeepClone(fPageInfo);
			List uiColumnList = menuBean.getUiColumnBean();
			ArrayList dimConfigInfoList_ = new ArrayList();
			int[] posnOfDimConfigAdded = new int[fPageInfo.m_frontInfoList.size()];
			for (int i=0,is=posnOfDimConfigAdded.length;i<is;i++)
				posnOfDimConfigAdded[i] = -1;
			for (int l = 0; l < uiColumnList.size(); l++) {
				UIColumnBean uIColumnBean = (UIColumnBean) uiColumnList.get(l);

				String uiColumnName = uIColumnBean.getColumnName();
				Integer origDimConfigIndex = fPageInfo.m_colIndexLookup.get(uiColumnName);
				if (origDimConfigIndex == null) {
					//due to a naming bug somewhere uiColumnName may be dimId
					int tempId = Misc.getParamAsInt(uiColumnName);
					if (!Misc.isUndef(tempId)) {
						origDimConfigIndex = fPageInfo.m_colIndexLookup.get("d"+tempId);
					}
				}
				if (origDimConfigIndex == null)
					continue;
				int origDimConfigIndexInt = origDimConfigIndex.intValue();
				//add things that are hidden that are before it 
				posnOfDimConfigAdded[origDimConfigIndexInt] = l;
				fPageInfo.m_frontInfoList.remove(origDimConfigIndexInt);
//				DimConfigInfo dimConfigInfo = (DimConfigInfo) fPageInfo.m_frontInfoList.get(origDimConfigIndexInt);
//				dimConfigInfo.m_name = uIColumnBean.getAttrValue();
//				dimConfigInfoList_.add(dimConfigInfo);
			}
			//now add hidden - add it just before the next non-hidden item is added
			int lastAddedPos = dimConfigInfoList_.size();

			for (int i=fPageInfo.m_frontInfoList.size()-1;i>=0;i--) {
				DimConfigInfo dc = (DimConfigInfo)fPageInfo.m_frontInfoList.get(i);
				if (dc.m_hidden || (posnOfDimConfigAdded[i] == -1 && (dc.m_isSelect || (dc.m_innerMenuList != null && dc.m_innerMenuList.size() != 0)))) {
					if (lastAddedPos == dimConfigInfoList_.size())
						dimConfigInfoList_.add(dc);
					else {
						dimConfigInfoList_.add(lastAddedPos, dc);
					}
				}
				else {
					int posAddedAt = posnOfDimConfigAdded[i];
					if (posAddedAt != -1)
						lastAddedPos = posAddedAt;
				}
			}

			for (int i=0,is=posnOfDimConfigAdded.length;i<is;i++)
				posnOfDimConfigAdded[i] = -1;
			fPageInfo.m_frontInfoList = dimConfigInfoList_;
			fPageInfo.postProcess(null, true);
		}
	}
	
	private FrontPageInfo getDeepClone(FrontPageInfo fPageInfo) throws IOException, ClassNotFoundException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(fPageInfo);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object deepCopy = ois.readObject();
		fPageInfo = (FrontPageInfo)deepCopy;
		return fPageInfo;
	}
	
	public String createCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		String menuTag = request.getParameter("menuTag");
		String configFile = request.getParameter("configFile");
		String dynamicReports  = null;
		dynamicReports = request.getParameter("dyn");
		request.setAttribute("menuTag", menuTag);
		request.setAttribute("configFile", configFile);
		CustomizeDao cdao = new CustomizeDao();
		SessionManager session = InitHelper.helpGetSession(request);
		Cache cache = session.getCache();
		User user = session.getUser();
		int userId = user.getUserId();//Misc.getParamAsInt(request.getParameter("userId"));
		//if calling customize from link, we need to check if doing cust for port - a valid val for forPort indicates that
	   
		
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		int row = Misc.getParamAsInt(request.getParameter("row"),0);
		int col = Misc.getParamAsInt(request.getParameter("col"),0);
		int menuIdFound = Misc.getParamAsInt(request.getParameter("menu_id"));
		int forPort = Misc.getParamAsInt(request.getParameter("forPort"));
		boolean doingForPort = !Misc.isUndef(portNodeId);
		if(Misc.isUndef(menuIdFound)){
			if (!Misc.isUndef(forPort)) {
				doingForPort = true;
				PrivInfo.TagInfo portCustrwTagInfo = cache.getPrivId("tr_docustomize_port");
				int portCustPrivId = portCustrwTagInfo == null ? Misc.getUndefInt() : portCustrwTagInfo.m_write;
				forPort = user.getUserSpecificDefaultPort(session, forPort, portCustPrivId, DimInfo.getDimInfo(123));
				for (MiscInner.PortInfo forPortInfo = cache.getPortNode(conn, forPort); forPortInfo != null && user.isPrivAvailable(session, portCustPrivId, forPortInfo.m_id); forPortInfo = forPortInfo.m_parent) {
					menuIdFound = cdao.getMenuId(conn, userId, forPortInfo.m_id, menuTag, configFile, row, col);
					if (!Misc.isUndef(menuIdFound)) {
						portNodeId = forPortInfo.m_id;
						session.setAttribute("applicableTo", Integer.toString(forPortInfo.m_id), false);
						break;
					}
				}	            
			}
			else {

				menuIdFound = cdao.getMenuId(conn, userId, portNodeId, menuTag, configFile, row, col);
				if (Misc.isUndef(menuIdFound) && "dyn".equalsIgnoreCase(dynamicReports)) {
					portNodeId = Misc.getParamAsInt((String)user.getUserPreference(conn, false).get("pv123"));
					menuIdFound = cdao.getMenuIdForDynamicMenu(conn, userId, portNodeId, menuTag, configFile, row, col);
				}
			}
		}
		if (!Misc.isUndef(menuIdFound)) {
			session.setAttribute("menuId", Integer.toString(menuIdFound),false);
		}
		int menuId = menuIdFound;
		//int menuId = Misc.getParamAsInt(session.getParameter("menuId"));
		
		if (!Misc.isUndef(menuId))
		{
			return editCustomize(request, response);
		}
		String fileName = request.getParameter("configFile");
		if(fileName == null){
			fileName = "test_front_page_vehicle.xml";
			request.setAttribute("configFile", fileName);
		}
		FrontPageInfo fPageInfoFull = null;
		FrontPageInfo currDefault = null;
		try {
			currDefault = CacheManager.getFrontPageConfig(conn ,doingForPort ? Misc.getUndefInt() : userId, !Misc.isUndef(portNodeId) ? portNodeId : Misc.getUserTrackControlOrg(session), menuTag, configFile, row, col);
			fPageInfoFull = FrontPageInfo.getFrontPage(fileName, false, conn, cache);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		request.setAttribute("menuBean", MenuBean.getMenuBeanForFrontPage(currDefault, menuTag, configFile, row, col, userId, doingForPort ? (!Misc.isUndef(portNodeId) ? portNodeId : Misc.getUserTrackControlOrg(session)) : Misc.getUndefInt()));
		request.setAttribute("rbList",getFrontpageInfo(fPageInfoFull));
		request.setAttribute("srList",getSearchFrontpageInfo(fPageInfoFull));
			if(request.getAttribute("rbList") != null)
				System.out.println("rbList   :    "+((List)request.getAttribute("rbList")).size());
			if(request.getAttribute("srList") != null)
				System.out.println("srList   :    "+((List)request.getAttribute("srList")).size());
		return CREATE;
	}
	
	
	public String deleteCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException {
		SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
		
		CustomizeDao customizeDao = new CustomizeDao();
		String menuTag = request.getParameter("menuTag");
		String configFile = request.getParameter("configFile");
		request.setAttribute("menuTag", menuTag);
		request.setAttribute("configFile", configFile);
		User user = session.getUser();
		int userId = user.getUserId();//Misc.getParamAsInt(request.getParameter("userId"));
		//if calling customize from link, we need to check if doing cust for port - a valid val for forPort indicates that
	   
		
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		int row = Misc.getParamAsInt(request.getParameter("row"),0);
		int col = Misc.getParamAsInt(request.getParameter("col"),0);
		
		int menuIdFound = customizeDao.getMenuId(conn, userId, portNodeId, menuTag, configFile, row, col);
		
		customizeDao.delete(conn, menuIdFound);
		
		return DELETE;
	}
	
	public String editCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException {
		SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
		request.setAttribute("menuTag", request.getParameter("menuTag"));
		request.setAttribute("configFile", request.getParameter("configFile"));
		CustomizeDao customizeDao = new CustomizeDao();
		int menuId = Misc.getParamAsInt(session.getParameter("menuId"));
		MenuBean menuBean = null;
		System.out.println("CustomizeAction.editCustomize()   ########################   request.getAttribute(menuId)  :  "+request.getAttribute("menuId"));
		if (!Misc.isUndef(menuId))
			menuBean = customizeDao.getMenuById(conn, menuId);
		
		request.setAttribute("menuBean",menuBean);
		String fileName = request.getParameter("configFile");
		FrontPageInfo fPageInfo = null;
		try {
			fPageInfo = FrontPageInfo.getFrontPage(fileName, true, conn,  Cache.getCacheInstance(conn));
		} catch (Exception e) {
			e.printStackTrace();
		}
		List serchFrontPageList = getSearchFrontpageInfo(fPageInfo);
		
		request.setAttribute("rbList",getFrontpageInfo(fPageInfo));
		request.setAttribute("srList",serchFrontPageList);
		//		String actionForward = sendResponse("create", true, request);
		return CREATE;
	}
	
//	public String viewCustomize(HttpServletRequest request, HttpServletResponse response)
//	throws ServletException, IOException, GenericException {
//		RuleSetDao ruleDao = new RuleSetDao();
//
//		request.setAttribute("rbList",ruleDao.getAllRuleSets());
//
//		//		String actionForward = sendResponse("create", true, request);
//		return VIEW;
//	}
	
	public String saveCustomize(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		   SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
			MenuBean menuBean = new MenuBean();
			int userId = session.getUser().getUserId();
			
			menuBean.setUserId(userId);
			menuBean.setRowId(Misc.getParamAsInt(request.getParameter("row"),0));
			menuBean.setColId(Misc.getParamAsInt(request.getParameter("col"),0));
			menuBean.setPortNodeId(Common.getParamAsInt(request.getParameter("applicableTo")));
//			if(request.getParameter("portNodeId") != null && !"".equals(request.getParameter("portNodeId")))
//				menuBean.setPortNodeId(Integer.parseInt(request.getParameter("portNodeId")));
			menuBean.setMenuTag(request.getParameter("menuTag"));
			menuBean.setComponentFile(request.getParameter("configFile"));
				
			String xml = Common.getParamAsString(request.getParameter("XML_DATA_COLUMN"));
			xml = Common.getParamAsString(request.getParameter("COLUM"));
			System.out.println("CustomizeServlet.saveCustomize()  :  XML_DATA_COLUMN : "  +  xml);
			org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
			
		    org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("COLUMN");
		    List<UIColumnBean> uiColumnBeanList = menuBean.getUiColumnBean();
		    int size = nList.getLength();
		    UIColumnBean uiColumnBean = null;
		    boolean doingDynLastCol = "1".equals(request.getParameter("dyn_last_column"));
		    DimConfigInfo lastBaseCol = null; //only pop for doingDynLastCol
		    if (doingDynLastCol) {
		    	FrontPageInfo base = CacheManager.getBaseFrontPageInfo(conn,request.getParameter("configFile"));
		    	lastBaseCol = base != null && base.m_frontInfoList != null ? (DimConfigInfo) base.m_frontInfoList.get(base.m_frontInfoList.size()-1) : null;
		    }
		    
		    if (size != 0) {//ensure last is proper
		    	org.w3c.dom.Node node =  nList.item(size-1);		        
		        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
		        if (element.getAttribute("colName") == null || "".equals(element.getAttribute("colName")) 
		        		|| element.getAttribute("colTitle") == null && "".equals(element.getAttribute("colTitle"))) {
		        	size--;
		        }
		        

		    }
		    for ( int i=0; i<size ; i++){
		    	
		        org.w3c.dom.Node node =  nList.item(i);
		        
		        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
		        if(element.getAttribute("colName") != null && !"".equals(element.getAttribute("colName")) 
		        		&& element.getAttribute("colTitle") != null && !"".equals(element.getAttribute("colTitle"))){
			        uiColumnBean = new UIColumnBean();
			        if (lastBaseCol == null || i != size-1) {
				        uiColumnBean.setColumnName(element.getAttribute("colName"));
				        uiColumnBean.setAttrName(element.getAttribute("colTitle"));
				        uiColumnBean.setAttrValue(element.getAttribute("colTitle"));
				        uiColumnBean.setAttrValue(element.getAttribute("colTitle"));
				        uiColumnBean.setRollup(Misc.getParamAsInt(element.getAttribute("status"),0));
			        }
			        else {
			        	uiColumnBean.setColumnName(lastBaseCol.m_columnName);
				        uiColumnBean.setAttrName(lastBaseCol.m_name);
				        uiColumnBean.setAttrValue(lastBaseCol.m_name);
				        uiColumnBean.setAttrValue(lastBaseCol.m_name);			        	
				        uiColumnBean.setRollup(lastBaseCol.m_doRollupTotal ? 1 : 0);
			        }
			        
					uiColumnBeanList.add(uiColumnBean);
		        }
		    }
		    menuBean.setUiColumnBean((ArrayList<UIColumnBean>)uiColumnBeanList);
		   
		    xml = Common.getParamAsString(request.getParameter("XML_DATA_PARAM"));
		    System.out.println("CustomizeServlet.saveCustomize() ==  XML_DATA_PARAM  ===> "+xml);
			xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
			
		    nList= xmlDoc.getElementsByTagName("PARAM");
		    List<UIParameterBean> uiParameterBeanList = menuBean.getUiParameterBean();
		    size = nList.getLength();
		    UIParameterBean uiParameterBean = null;
		    for ( int i=0; i<size ; i++){
		        org.w3c.dom.Node node =  nList.item(i);
		        
		        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
		        if(element.getAttribute("propertyName") != null && !"".equals(element.getAttribute("propertyName")) 
		        		&& element.getAttribute("propertyValue") != null && !"".equals(element.getAttribute("propertyValue"))){
		        	uiParameterBean = new UIParameterBean(element.getAttribute("propertyName"), element.getAttribute("propertyValue"), element.getAttribute("operator"), element.getAttribute("right_operand"));
		        	//uiParameterBean.setParamName();
		        	//uiParameterBean.setParamValue(element.getAttribute("propertyValue"));
		        
		        	uiParameterBeanList.add(uiParameterBean);
		        }
		    }
		    menuBean.setUiParameterBean((ArrayList<UIParameterBean>)uiParameterBeanList);
		    
		    
		    CustomizeDao customizeDao = new CustomizeDao();
		    
		    try {
		    	int menuIdFound = customizeDao.getMenuId(conn, userId, menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(), menuBean.getRowId(), menuBean.getColId());
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
		request.setAttribute("menuId", String.valueOf(menuBean.getId()));
		return SAVE; //editCustomize(request, response);
	}
	
	public static List getFrontpageInfo(FrontPageInfo fPageInfo)
	{
		List rbList = new ArrayList<ConfigColumnMaster>();
		if(fPageInfo != null){
			List dimConfigInfoList = fPageInfo.m_frontInfoList;
			ConfigColumnMaster configColumnMaster = null;
			DimConfigInfo dimConfigInfo = null;
			DimCalc dimCalc = null;
			DimInfo dimInfo = null;
			for (int i = 0; i < dimConfigInfoList.size(); i++) {
				dimConfigInfo= (DimConfigInfo)dimConfigInfoList.get(i);
				configColumnMaster = new ConfigColumnMaster();
				
				if(dimConfigInfo != null && !dimConfigInfo.m_hidden){
					configColumnMaster.setColumnId(dimConfigInfo.m_columnName);
					configColumnMaster.setLabel(dimConfigInfo.m_name);
					configColumnMaster.setInternalName(dimConfigInfo.m_internalName);
					dimCalc = dimConfigInfo.m_dimCalc;
					dimInfo = dimCalc.m_dimInfo;
					if((configColumnMaster.getColumnId() == null || "".equals(configColumnMaster.getColumnId())) && dimInfo != null){
						configColumnMaster.setColumnId(String.valueOf(dimInfo.m_id));
					}
					if(configColumnMaster.getColumnId() != null && !"".equals(configColumnMaster.getColumnId()))
							rbList.add(configColumnMaster);
				}
				
			}
		}
		
		return rbList;
	}
	
	public static List getSearchFrontpageInfo(FrontPageInfo fPageInfo)
	{
		List rbList = new ArrayList<ConfigColumnMaster>();
		if(fPageInfo != null){
			List searchConfigInfoList = fPageInfo.m_frontSearchCriteria;
			ConfigColumnMaster configColumnMaster = null;
			DimConfigInfo dimConfigInfo = null;
			DimCalc dimCalc = null;
			DimInfo dimInfo = null;
			for (int k = 0; k < searchConfigInfoList.size(); k++) {
				List dimConfigInfoList= (List)searchConfigInfoList.get(k);
				for (int i = 0; i < dimConfigInfoList.size(); i++) {
					dimConfigInfo= (DimConfigInfo)dimConfigInfoList.get(i);
					configColumnMaster = new ConfigColumnMaster();

					//				if(dimConfigInfo != null && !dimConfigInfo.m_askUser){
//					if(dimConfigInfo != null && dimConfigInfo.m_hidden ){/*&& !dimConfigInfo.m_askUser*/
						//for hidden value 
					if(dimConfigInfo != null){// For all search parameters
						configColumnMaster.setColumnId(dimConfigInfo.m_columnName);
						// FRO lebel
						configColumnMaster.setLabel(dimConfigInfo.m_name);
						configColumnMaster.setInternalName(dimConfigInfo.m_internalName);
						configColumnMaster.setOperator(dimConfigInfo.m_defaultOperator);
						configColumnMaster.setRightOperand(dimConfigInfo.m_rightOperand);
						dimCalc = dimConfigInfo.m_dimCalc;
						dimInfo = dimCalc.m_dimInfo;
						configColumnMaster.setDimConfigInfo(dimConfigInfo);
						if((configColumnMaster.getColumnId() == null || "".equals(configColumnMaster.getColumnId())) && dimInfo != null){
							//for hidden value 
							configColumnMaster.setColumnId(String.valueOf(dimInfo.m_id));
						}
						if(configColumnMaster.getColumnId() != null && !"".equals(configColumnMaster.getColumnId()))
							rbList.add(configColumnMaster);
					}

				}
			}
		}

		return rbList;
	}
	
	public static void main(String[] args) {
		try {
			String fileName = "test_front_page_vehicle.xml";
			FrontPageInfo fPageInfo = null;
			try {
				fPageInfo = FrontPageInfo.getFrontPage(fileName, true, DBConnectionPool.getConnectionFromPoolNonWeb(), Cache.getCacheInstance(DBConnectionPool.getConnectionFromPoolNonWeb()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			CustomizeServlet.getSearchFrontpageInfo(fPageInfo);
//			String str = "";str.contains(",");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

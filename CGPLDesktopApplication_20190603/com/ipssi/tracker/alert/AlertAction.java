package com.ipssi.tracker.alert;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE;
import static com.ipssi.tracker.common.util.ApplicationConstants.CREATE;
import static com.ipssi.tracker.common.util.ApplicationConstants.DELETE;
import static com.ipssi.tracker.common.util.ApplicationConstants.EDIT;
import static com.ipssi.tracker.common.util.ApplicationConstants.SAVE;
import static com.ipssi.tracker.common.util.ApplicationConstants.VIEW;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.db.ApplicationDao;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.common.util.RuleProcessorGateway;
import com.ipssi.tracker.common.util.TripProcessorGateway;
import com.ipssi.tracker.customer.CustomerContactBean;
import com.ipssi.tracker.rule.RuleBean;
import com.ipssi.tracker.rule.RuleDao;
import com.ipssi.tracker.web.ActionI;
public class AlertAction implements ActionI {

	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
//		System.out.println("AlertAction.processRequest()   $##################  ");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		Connection conn = InitHelper.helpGetDBConn(request);
		try{
			if(CREATE.equals(action))
				actionForward = createAlerts(request, response);
			else if("saveContact".equals(action))
				actionForward = saveContact(request, response);
			else if("addContact".equals(action))
				actionForward = addContact(request, response);
			else if(SAVE.equals(action))
				actionForward = saveAlerts(request, response);
			else if(EDIT.equals(action))
				actionForward = editAlerts(request, response);
			else {//if(VIEW.equals(action))
				actionForward = viewAlerts(request, response);
			}
		} catch (GenericException e) {
			System.out.println("AlertAction "+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if(action.equals(CREATE)){
			if(success)
				actionForward = "/alertDetail.jsp";
			else
				actionForward = "/alertDetail.jsp";
		}else if(action.equals("addContact")){
			if(success)
				actionForward = "/addCustomerContacts.jsp";
			else
				actionForward = "/blank.jsp";
			
		}else if(action.equals("saveContact")){
			if(success)
				actionForward = "/blank.jsp";
			else
				actionForward = "/blank.jsp";
		}else if(action.equals(EDIT)){
			if(success)
				actionForward = "/alertDetail.jsp";
			else
				actionForward = "/alertDetail.jsp";
		}else if(action.equals(DELETE)){
			actionForward = "/alertDetail.jsp";
		}else if(action.equals(SAVE)){
			if(success)
				actionForward = "/alertDetail.jsp";
			else
				actionForward = "/alertDetail.jsp";
		}else if(action.equals(VIEW)){
			if(success)
				actionForward = "/alertView.jsp";
			else
				actionForward = "/alertView.jsp";
		}else{
			actionForward = "/alertView.jsp";
		}
		return actionForward;
	}
	public String createAlerts(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException,Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		RuleDao ruleDao = new RuleDao();
		AlertDao alertDao = new AlertDao();
		if("tr_region_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
			ArrayList<RuleBean> ruleList = new ArrayList<RuleBean>();
			ArrayList list = (ArrayList)InitHelper.helpGetSession(request).getCache().getValList("region_rules");
			if (list != null) {
				for (int j = 0; j < list.size(); j++) {
					DimInfo.ValInfo d = (DimInfo.ValInfo) list.get(j);
					RuleBean ruleBean = new RuleBean();
					ruleBean.setId(d.m_id);
					ruleBean.setRuleName(d.m_name);
					ruleList.add(ruleBean);
				}
			}
			request.setAttribute("rbList",ruleList);	
		}
		else if("tr_role_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
			ArrayList<RuleBean> ruleList = new ArrayList<RuleBean>();
			ArrayList list = (ArrayList)InitHelper.helpGetSession(request).getCache().getValList("roles");
			if (list != null) {
				for (int j = 0; j < list.size(); j++) {
					DimInfo.ValInfo d = (DimInfo.ValInfo) list.get(j);
					RuleBean ruleBean = new RuleBean();
					ruleBean.setId(d.m_id);
					ruleBean.setRuleName(d.m_name);
					ruleList.add(ruleBean);
				}
			}
			request.setAttribute("rbList",ruleList);	
		}
		else{
			request.setAttribute("rbList",ruleDao.getAllRules(InitHelper.helpGetSession(request)));
		}
		
		ApplicationDao appDao = new ApplicationDao();
		request.setAttribute("ruleActionList",appDao.getAllActionMasters(conn));
		request.setAttribute("customerContactsList",alertDao.getCustomerContacts(conn,InitHelper.helpGetSession(request)));
		request.setAttribute("customerList",alertDao.getCustomers(conn));
		request.setAttribute("opRegionList",alertDao.getOpRegion(conn ,InitHelper.helpGetSession(request)));
			
//			if(request.getAttribute("rbList") != null)
//				System.out.println("rbList   :    "+((List)request.getAttribute("rbList")).size());
		
//		String actionForward = sendResponse("create", true, request);
		return CREATE;
	}
	
	
	public String editAlerts(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		AlertDao alertDao = new AlertDao();
		RuleDao ruleDao = new RuleDao();
		Connection conn = InitHelper.helpGetDBConn(request);
		String alertId = (String) request.getParameter("alertId");
		NotificationSetBean ruleBean = null;
//		System.out.println("AlertAction.editRule()   ########################     :  "+alertId);
		
		if("tr_region_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
			ArrayList<RuleBean> ruleList = new ArrayList<RuleBean>();
			ArrayList list = (ArrayList)InitHelper.helpGetSession(request).getCache().getValList("region_rules");
			for( int j = 0; j < list.size(); j++){
				DimInfo.ValInfo d =  (DimInfo.ValInfo)list.get(j);
				RuleBean ruleBean_ = new RuleBean();
				ruleBean_.setId(d.m_id);
				ruleBean_.setRuleName(d.m_name);
				ruleList.add(ruleBean_);
			}
			request.setAttribute("rbList",ruleList);
			if(alertId != null && !"".equals(alertId))
				ruleBean = alertDao.getNotificationSetsById(conn, Misc.getParamAsInt(alertId), "region");
//			System.out.println("AlertAction.editRule()   ########################   ruleBean : region : " +ruleBean.getNotes());
			request.setAttribute("ruleBean",ruleBean);
		}
		else if("tr_role_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
			ArrayList<RuleBean> ruleList = new ArrayList<RuleBean>();
			ArrayList list = (ArrayList)InitHelper.helpGetSession(request).getCache().getValList("roles");
			for( int j = 0; j < list.size(); j++){
				DimInfo.ValInfo d =  (DimInfo.ValInfo)list.get(j);
				RuleBean ruleBean_ = new RuleBean();
				ruleBean_.setId(d.m_id);
				ruleBean_.setRuleName(d.m_name);
				ruleList.add(ruleBean_);
			}
			request.setAttribute("rbList",ruleList);
			if(alertId != null && !"".equals(alertId))
				ruleBean = alertDao.getNotificationSetsById(conn, Misc.getParamAsInt(alertId), "role");
//			System.out.println("AlertAction.editRule()   ########################   ruleBean : region : " +ruleBean.getNotes());
			request.setAttribute("ruleBean",ruleBean);
		}
		else{
			request.setAttribute("rbList",ruleDao.getAllRules(InitHelper.helpGetSession(request)));
			if(alertId != null && !"".equals(alertId))
				ruleBean = alertDao.getNotificationSetsById(conn, Misc.getParamAsInt(alertId), null);
//			System.out.println("AlertAction.editRule()   ########################   ruleBean :  : " +ruleBean.getNotes());
			request.setAttribute("ruleBean",ruleBean);
		}
		ApplicationDao appDao = new ApplicationDao();
		request.setAttribute("ruleActionList",appDao.getAllActionMasters(conn));
		request.setAttribute("customerContactsList",alertDao.getCustomerContacts(conn,InitHelper.helpGetSession(request)));
		request.setAttribute("customerList",alertDao.getCustomers(conn));
		request.setAttribute("opRegionList",alertDao.getOpRegion(conn ,InitHelper.helpGetSession(request)));
		//		String actionForward = sendResponse("create", true, request);
		return CREATE;
	}
	
	public String viewAlerts(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		//preprocess search parameter - specifically Org dim Id
		com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request,request.getParameter("page_context"));
		String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext + "9008";
		int status = Misc.getParamAsInt(request.getParameter(topPageContext),com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE);

		if("region".equalsIgnoreCase(request.getParameter("ty")) || "tr_region_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
			com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_region_alert_set");
			AlertDao alertDao = new AlertDao();
			request.setAttribute("rbList",alertDao.getAllNotificationSets(InitHelper.helpGetSession(request), "region",status));
		}
		else if("role".equalsIgnoreCase(request.getParameter("ty")) || "tr_role_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
			com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_role_alert_set");
			AlertDao alertDao = new AlertDao();
			request.setAttribute("rbList",alertDao.getAllNotificationSets(InitHelper.helpGetSession(request), "role",status));
		}
		else{
			com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_rule_alert_set");
			AlertDao alertDao = new AlertDao();
			request.setAttribute("rbList",alertDao.getAllNotificationSets(InitHelper.helpGetSession(request), null,status));
		}
		
		
		//		String actionForward = sendResponse("create", true, request);
		return VIEW;
	}
	
	public static String addContact(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException {
		AlertDao alertDao = new AlertDao();
		Connection conn = InitHelper.helpGetDBConn(request);
//		request.setAttribute("customerList",alertDao.getCustomers(conn));
		return "addContact";
	}
	
	
	public static String saveContact(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException {
		
			String xml = Common.getParamAsString(request.getParameter("XML_DATA_FIELD"));
			int custId = Common.getParamAsInt(request.getParameter("applicableTo_"));
			AlertDao alertDao = new AlertDao();
			Connection conn = InitHelper.helpGetDBConn(request);
			List contactList = new ArrayList();
			
		System.out.println("xml  : "+xml);
			org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		    org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("CONTACT");
		    int size = nList.getLength();
		    CustomerContactBean contactBean = null;
		    for ( int i=0; i<size ; i++){
		        org.w3c.dom.Node node =  nList.item(i);
		        
		        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
//		        int rType = Common.getParamAsInt(element.getAttribute("customerId_"));
		        if(custId != -1111111){
		        	contactBean = new CustomerContactBean();
		        	contactBean.setName(element.getAttribute("name_table"));
		        	contactBean.setPhone(element.getAttribute("phone_table"));
		        	contactBean.setMobile(element.getAttribute("mobile_table"));
		        	contactBean.setEmail(element.getAttribute("email_table"));
		        	contactBean.setAddress(element.getAttribute("address_table"));
		        	contactList.add(contactBean);		        }
//		        ioBean.setIoAttributeValue(getParamAsInt(ioId), getParamAsInt(attributeId) );
		    }
		    try {
		    	contactBean = alertDao.insertContacts(conn, contactList, custId);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("contactBean",contactBean.getName()+","+contactBean.getId()+","+contactBean.getMobile()+","+contactBean.getEmail());
		return "saveContact";
	}
	
	public String saveAlerts(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		ArrayList<CustomerContactBean> contactList1 =new ArrayList<CustomerContactBean>();
		CustomerContactBean contactBean1 = null;
			String xml = Common.getParamAsString(request.getParameter("XML_DATA_ALERT"));
		System.out.print(xml);
			NotificationSetBean ruleBean = new NotificationSetBean();
			ruleBean.setName(request.getParameter("name"));
			ruleBean.setStatus(Misc.getParamAsInt(request.getParameter("status"),ACTIVE));
			ruleBean.setLoadStatus(Misc.getParamAsInt(request.getParameter("load_status")));
			ruleBean.setCreateType(Misc.getParamAsInt(request.getParameter("create_type")));
			ruleBean.setOpstationSubtype(Misc.getParamAsInt(request.getParameter("opstation_subtype")));
			ruleBean.setRelativeDurOperator(Misc.getParamAsInt(request.getParameter("relative_dur_operator")));
			ruleBean.setRelativeDurOperand1(Misc.getParamAsInt(request.getParameter("relative_dur_operand1")));
			ruleBean.setRelativeDurOperand2(Misc.getParamAsInt(request.getParameter("relative_dur_operand2")));
			ruleBean.setLoadingAt(request.getParameter("loading_at"));
			ruleBean.setUnloadingAt(request.getParameter("unloading_at"));
			ruleBean.setEventDurOperator(Misc.getParamAsInt(request.getParameter("event_dur_operator")));
			ruleBean.setEventDurOperand1(Misc.getParamAsInt(request.getParameter("event_dur_operand1")));
			ruleBean.setEventDurOperand2(Misc.getParamAsInt(request.getParameter("event_dur_operand2")));
			ruleBean.setEventDistOperator(Misc.getParamAsInt(request.getParameter("event_dist_operator")));
			ruleBean.setEventDistOperand1(Misc.getParamAsInt(request.getParameter("event_dist_operand1")));
			ruleBean.setEventDistOperand2(Misc.getParamAsInt(request.getParameter("event_dist_operand2")));
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
			try {
				ruleBean.setStatusFrom_date(sdf.parse(request.getParameter("statusFrom")));
				ruleBean.setStatusTo_date(sdf.parse(request.getParameter("statusTo")));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			ruleBean.setApplicableTo(request.getParameter("applicableTo"));
			ruleBean.setPortNodeId(Common.getParamAsInt(request.getParameter("applicableTo")));
			ruleBean.setNotes(request.getParameter("notes"));
			org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		    org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("ALERT");
		    List<RuleNotificationBean> norificationSetsRuleList = (List) ruleBean.getRuleNotificationBeanList();
		    int size = nList.getLength();
		    RuleNotificationBean ruleSetsRuleBean = null;
		    for ( int i=0; i<size ; i++){
		        org.w3c.dom.Node node =  nList.item(i);
		        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
		        int rType = Common.getParamAsInt(element.getAttribute("ruleName"));
		        if(rType != -1111111){
		        //rahul 
		        //getting customerContact detail if they change
		        if(Common.getParamAsInt(element.getAttribute("test"))!=-1)
		        {
		        contactBean1=new CustomerContactBean();
		        contactBean1.setId(Common.getParamAsInt(element.getAttribute("customerId")));
		        contactBean1.setMobile(Common.getParamAsString(element.getAttribute("phone")));
		        //contactBean1.setPhone(Common.getParamAsString(element.getAttribute("phone")));
		        contactBean1.setEmail(Common.getParamAsString(element.getAttribute("Email")));
		        System.out.println(contactBean1.getEmail());
		        contactList1.add(contactBean1);
		        
		        }
		              
		        ruleSetsRuleBean = new RuleNotificationBean();
		        ruleSetsRuleBean.setRegionId(Common.getParamAsInt(element.getAttribute("regionName")));
		        ruleSetsRuleBean.setOpThreshold(Common.getParamAsInt(element.getAttribute("opThreshold")));
		        ruleSetsRuleBean.setRuleId(Common.getParamAsInt(element.getAttribute("ruleName")));
		        ruleSetsRuleBean.setType(Common.getParamAsInt(element.getAttribute("alert")));
		        ruleSetsRuleBean.setForThresholdLevel(Common.getParamAsInt(element.getAttribute("for_threshold_level")));
		        try {
					ruleSetsRuleBean.setValidFromDate(sdf.parse(element.getAttribute("validFrom")));
					ruleSetsRuleBean.setValidToDate(sdf.parse(element.getAttribute("validTo")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				ruleSetsRuleBean.setContactTimeFrom(element.getAttribute("fromTime"));
				ruleSetsRuleBean.setContactTimeTo(element.getAttribute("toTime"));
				ruleSetsRuleBean.setCustomerContactId(Common.getParamAsInt(element.getAttribute("customerId")));
				
				norificationSetsRuleList.add(ruleSetsRuleBean);
		        }
		    }
        	

		    ruleBean.setRuleNotificationBeanList((ArrayList<RuleNotificationBean>)norificationSetsRuleList);
		    AlertDao ruleSetDao = new AlertDao();
		    try {
		    	if("tr_region_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
		    		if(request.getParameter("alertId") != null && !"".equals(request.getParameter("alertId")))
			    	{
						ruleBean.setId(Integer.parseInt(request.getParameter("alertId")));
						ruleSetDao.updateNotification(conn, ruleBean, "region");
			    	}else
			    		ruleSetDao.insertNotification(conn, ruleBean, "region");
		    	
				}
				else if("tr_role_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
		    		if(request.getParameter("alertId") != null && !"".equals(request.getParameter("alertId")))
			    	{
						ruleBean.setId(Integer.parseInt(request.getParameter("alertId")));
						ruleSetDao.updateNotification(conn, ruleBean, "role");
			    	}else
			    		ruleSetDao.insertNotification(conn, ruleBean, "role");
				}
				else{
					if(request.getParameter("alertId") != null && !"".equals(request.getParameter("alertId")))
			    	{
						ruleBean.setId(Integer.parseInt(request.getParameter("alertId")));
						ruleSetDao.updateNotification(conn, ruleBean, null);
			    	}else
			    		ruleSetDao.insertNotification(conn, ruleBean, null);
					    ruleSetDao.updateCustCont(conn, contactList1);
				}
		    	
			} catch (GenericException e) {
				System.out.println("RuleSetAction.saveAlerts()"+ e.getMessage());
				e.printStackTrace();
				throw e;
			}
			try {
			conn.commit(); //sync happens on different thread
			// TODO ejb call to refresh role alerts  
			if(!"tr_region_alert_set".equalsIgnoreCase(request.getParameter("page_context"))){
				RuleProcessorGateway.refreshNotificationSet(ruleBean.getId(),Misc.getServerName());
				}
			else{
					TripProcessorGateway.refreshRegionAlerts(new ArrayList<Integer>(ruleBean.getId()),Misc.getServerName());
				}
			}
			catch (Exception e2) {
				e2.printStackTrace();
				//eat it - though we need to give a warning and send an email or maybe just throw??
			}
		return viewAlerts(request, response);
	}

	
}

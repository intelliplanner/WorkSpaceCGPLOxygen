package com.ipssi.dashboard.customization;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class CustomDashAction implements ActionI {
	private String forward_url = "/DashBoardServlet.do?action=";
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		String customActionForwardRoute = Common.getParamAsString(request.getParameter("customActionForwardRoute"));
		boolean success = true;
		try {
			if ("edit".equals(action))
				actionForward =customization(request, response);
			else if ("create".equals(action))
				actionForward =create(request, response);
			else if ("delete".equals(action))
				actionForward =delete(request, response);
			else if ("update".equals(action))
				actionForward = updateComponent(request, response);
			else
				actionForward = viewDashboard(request, response);
		} catch (Exception e) {
			System.out.println("actionList " + e.getMessage());
			e.printStackTrace();
		}
		actionForward = sendResponse(actionForward, success, request);
		if ( customActionForwardRoute != null && customActionForwardRoute != ""){
			actionForward = customActionForwardRoute;
		}

		return actionForward;
	}
	private String updateComponent(HttpServletRequest request, HttpServletResponse response) throws GenericException, SQLException {
		ComponentBean component = null;
		boolean isNew = false;
		String pg_context = request.getParameter("page_context");
		int customize_id = Misc.getParamAsInt(request.getParameter("customize_id"));
		CustDashBoardDao dashDao=new CustDashBoardDao();
		DashBean dashBean = null; 
		String xml = Common.getParamAsString(request.getParameter("XML_DATA"));
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("COMPONENT");
		int user_id = InitHelper.helpGetSession(request).getUser().getUserId();
		int pv123 = Misc.getParamAsInt(InitHelper.helpGetSession(request).getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		int size = nList.getLength();

		if(customize_id==1){
			dashBean = dashDao.getDashInfo(InitHelper.helpGetSession(request),ApplicationConstants.ACTIVE, pg_context,1);
			if(dashBean == null)
			{
			dashBean = dashDao.getDashInfo(InitHelper.helpGetSession(request),ApplicationConstants.ACTIVE, pg_context,3);
			dashBean.setUserId(user_id);
			dashDao.insertDashInfo(InitHelper.helpGetSession(request), dashBean);
			isNew = true;
			}
		}
		if(customize_id==2){
			dashBean = dashDao.getDashInfo(InitHelper.helpGetSession(request),ApplicationConstants.ACTIVE, pg_context,2);
			if(dashBean == null)
			{
			dashBean = dashDao.getDashInfo(InitHelper.helpGetSession(request),ApplicationConstants.ACTIVE, pg_context,3);
			dashBean.setPortNodeId(pv123);
			dashDao.insertDashInfo(InitHelper.helpGetSession(request), dashBean);
			isNew = true;
			}
		}
		int dash_info_id = dashBean.getId();
		for ( int i=0; i<size ; i++){
			org.w3c.dom.Node node =  nList.item(i);
			org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			String misc;
			int rType = Common.getParamAsInt(element.getAttribute("rType"));
			if(rType != -1111111){
				component=new ComponentBean();
				component.setId(Common.getParamAsInt(element.getAttribute("Id")));
				component.setTitle(Common.getParamAsString(element.getAttribute("title")));
				component.setUidTag(Common.getParamAsString(element.getAttribute("uid_tag")));
				component.setRefreshInt(Common.getParamAsInt(element.getAttribute("refresh_int")));
				component.setDivLeft(Common.getParamAsInt(element.getAttribute("left")));
				component.setDivTop(Common.getParamAsInt(element.getAttribute("top")));
				component.setDivHeight(Common.getParamAsInt(element.getAttribute("height")));
				component.setDivWidth(Common.getParamAsInt(element.getAttribute("width")));
				component.setXML(Common.getParamAsString(element.getAttribute("xml")));
				misc = element.getAttribute("misc");
				if(misc != null && misc.equalsIgnoreCase(""))
				component.setMiscellaneous(Common.getParamAsString(element.getAttribute("misc").replace("amp;", "&")));
				else
				component.setMiscellaneous(misc);
				component.setDashInfoId(dash_info_id);
				if(!isNew){
					if(rType==0)
						dashDao.deleteComponent(InitHelper.helpGetSession(request), component.getDashInfoId(),component.getUidTag());	
					if(rType==1)
						dashDao.insertComponentInfo(InitHelper.helpGetSession(request), component);
					if(rType==2)
						dashDao.updateComponentInfo(InitHelper.helpGetSession(request), component);	
				}
				else
				{   if(rType != 0)
					dashDao.insertComponentInfo(InitHelper.helpGetSession(request), component);
				}
			}
		}
		forward_url = forward_url+dashBean.getPgAction()+"&page_context="+dashBean.getPgContext()+"&isDashboard=1";
		//boolean insertStatus=dashDao.insertComponentInfo(InitHelper.helpGetSession(request), componentBean);
		return "update";
	}
	private String delete(HttpServletRequest request, HttpServletResponse response) {

		return "delete";
	}
	
	private String viewDashboard(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int status = com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE;
		CustDashBoardDao dashDao=new CustDashBoardDao();
		request.setAttribute("dashList",dashDao.getDashInfo(InitHelper.helpGetSession(request),status,(String) request.getAttribute("page_context")));
		return "view";
	}
	private String create(HttpServletRequest request, HttpServletResponse response) throws Exception {

		/*CustDashBoardDao dashDao=new CustDashBoardDao();
		DashBean dashBean=new DashBean();
		dashBean.setPortNodeId(Integer.parseInt(request.getParameter("portId")));
		dashBean.setName(request.getParameter("name"));
		dashBean.setStatus(Integer.parseInt(request.getParameter("status")));
		dashBean.setNotes(request.getParameter("notes"));
		dashDao.insertDash(InitHelper.helpGetSession(request), dashBean);*/
		return "create";
	}
	private String customization(HttpServletRequest request, HttpServletResponse response) throws GenericException, SQLException {
		int status = com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE;
		CustDashBoardDao dashDao=new CustDashBoardDao();
		DashBean dashBean=dashDao.getDashInfo(InitHelper.helpGetSession(request),status,(String) request.getParameter("page_context"));
		request.setAttribute("dashInfo",dashBean);
		request.setAttribute("componentList",dashDao.getComponentInfo(InitHelper.helpGetSession(request),dashBean));
		return "edit";
	}
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if (action.equals("edit")) {
			if (success)
				actionForward = "/custDash.jsp";
			else
				actionForward = "/custDash.jsp";
		}
		else if (action.equals("create")) {
			if (success)
				actionForward = "/DashCreate.jsp";
			else
				actionForward = "/DashCreate.jsp";
		}
		else if (action.equals("view")) {
			if (success)
				actionForward = "/viewDashboard.jsp";
			else
				actionForward = "/viewDashboard.jsp";
		}
		else if (action.equals("update")) {
			if (success)
				actionForward = forward_url;
			else
				actionForward = forward_url;
		}
		return actionForward;
	}
}

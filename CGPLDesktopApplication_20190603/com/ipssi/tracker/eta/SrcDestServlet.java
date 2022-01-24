package com.ipssi.tracker.eta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.eta.SrcDestInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.tracker.alert.AlertAction;
import com.ipssi.tracker.common.util.RuleProcessorGateway;
import com.ipssi.tracker.customer.CustomerContactBean;
import com.ipssi.tracker.web.ActionI;


public class SrcDestServlet implements ActionI {
	private static Logger logger = Logger.getLogger(WayPointServlet.class);
	private SessionManager m_session;
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		boolean success = true;
		
		m_session = InitHelper.helpGetSession(request);
		String action = request.getParameter("action");
		if (action == null || action.length() == 0)
			action = "search";
		Connection conn = m_session.getConnection();
		boolean doSearch = true;
		if("saveContact".equals(action)) {
			AlertAction.saveContact(request, response);
			doSearch = false;
		}
		else if("addContact".equals(action)) {
			AlertAction.addContact(request, response);
			doSearch = false;
		}
		else if ("save".equals(action)) {
			SrcDestInfo srcDest = this.getInfoForSave(m_session.getParameter("XML_DATA"));
			if (srcDest != null) {
				int oldId = srcDest.getId();
				SrcDestInfo.save(srcDest, conn);
				int newId = srcDest.getId();
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(oldId);
				if (oldId != newId)
					list.add(newId);
				boolean doReverse = "1".equals(m_session.getParameter("do_reverse"));
				if (doReverse) {
					SrcDestInfo inv = srcDest.reverse();
					int revOldId = inv.getId();
					SrcDestInfo.save(inv, conn);
					int revNewId = inv.getId();
					list.add(revOldId);
					if (revOldId != revNewId)
						list.add(revNewId);
					PreparedStatement ps = conn.prepareStatement("update src_dest_items set reverse_id = ? where id = ?");
					ps.setInt(1, revNewId);
					ps.setInt(2, newId);
					ps.execute();
					ps.close();
				}
				if (!conn.getAutoCommit())
					conn.commit();
				//RuleProcessorGateway.refreshSrcDestItem(list,Misc.getServerName());
			}
		}
		else if ("edit".equals(action)) {
			int srcDestItemId = Misc.getParamAsInt(m_session.getParameter("src_dest_item_id"));
			ArrayList<SrcDestInfo> sdList = SrcDestInfo.getSrcDestList(srcDestItemId, Misc.getUndefInt(), 0, Misc.getUndefInt(), conn);
			request.setAttribute("infoBean", sdList == null || sdList.size() == 0 ? null : sdList.get(0));
			doSearch = false;
		}
		if (doSearch) {
			
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_eta_setup");
			int orgId =  Misc.getParamAsInt(m_session.getParameter("pv123"));
			int status = Misc.getParamAsInt(m_session.getParameter((searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext)+9079));
			if (Misc.isUndef(status)) {//hack ... in case we are continuing with old status
				status = Misc.getParamAsInt(m_session.getParameter((searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext)+9008),1);
			}
			ArrayList<SrcDestInfo> sdList = SrcDestInfo.getSrcDestList(Misc.getUndefInt(), orgId, 1, status, conn);
			request.setAttribute("infoBeanList", sdList);
		}
		String actionForward =  sendResponse(action, success, request);
		return actionForward;
	}

	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		if(action.equals("addContact")){
			if(success)
				return "/addCustomerContacts.jsp";
			else
				return  "/blank.jsp";
		}
		else if(action.equals("saveContact")){
			if(success)
				return "/blank.jsp";
			else
				return  "/blank.jsp";
		}
		else if ("edit".equals(action) && success)
			   return "/src_dest_detail.jsp";
		else
			return "/src_dest_list.jsp";
	}
	
	public static void printAlertSetting(JspWriter out, Connection conn, Cache cache, SessionManager session, SrcDestInfo srcDestInfo) throws Exception {
		DimInfo etaAlertDim = DimInfo.getDimInfo("eta_alert_types");
		DimInfo alertTypeDim = DimInfo.getDimInfo(9077);
		DimInfo alertRoleDim = DimInfo.getDimInfo(9108);
		List<CustomerContactBean> customerContactsList = com.ipssi.tracker.alert.AlertDao.getCustomerContacts(conn, session);
		ArrayList<ValInfo> valList = etaAlertDim.getValList(conn, session);
		ArrayList<MiscInner.PairIntStr> notificationTypes = Misc.getNotificationTypes(conn);
		for (int i=0,is=valList == null ? 0 : valList.size(); i<is; i++) {
			ValInfo valInfo = valList.get(i);
			printAlertSettingItem(conn, out, cache, etaAlertDim, alertTypeDim, alertRoleDim, notificationTypes, customerContactsList
					 , valInfo.m_id
					 , srcDestInfo == null ? null : srcDestInfo.getAlertFormat(valInfo.m_id, 0,false, true,true)
					 , srcDestInfo == null ? null : srcDestInfo.getAlertFormat(valInfo.m_id, 1,false, true,true)
					 , srcDestInfo == null ? null : srcDestInfo.getAlertFormat(valInfo.m_id, 2,false, true,true)
					, srcDestInfo == null ?null : srcDestInfo.getAlertSetting(valInfo.m_id)
					, SrcDestInfo.getDefaultAlertFormat(valInfo.m_id,0)
					, SrcDestInfo.getDefaultAlertFormat(valInfo.m_id,1)
					, SrcDestInfo.getDefaultAlertFormat(valInfo.m_id,2)
					);
			
		}
	}
	private static void printAlertSettingItem(Connection conn, JspWriter out, Cache _cache, DimInfo etaAlertDim, DimInfo alertTypeDim, DimInfo alertRoleDim, List<MiscInner.PairIntStr> notificationTypes,List<CustomerContactBean> customerContactsList, int ty
			, String fmtString, String fmtStringEmail, String fmtStringNotification
			, ArrayList<SrcDestInfo.AlertSetting> alertList
			, String defaultAlertFormat, String defaultAlertFormatEmail, String defaultAlertFormatNotification
			) throws IOException {
		DimInfo.ValInfo valInfo = etaAlertDim.getValInfo(ty);
		if (valInfo == null)
			return;
		String distLabel =  valInfo.getOtherProperty("dist_column");
		if (distLabel != null)
			distLabel = distLabel.trim();
		if (distLabel != null && distLabel.length() == 0)
			distLabel = null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<tr><td colspan='4' valign='top' class='sh2' align='left'>").append(valInfo.m_name).append("</td></tr>");
		sb.append("<tr><td class='tn' colspan='4'><table border='0' cellpadd='2' cellspacing='0'>");
		sb.append("<tr><td class='tn'>SMS(Also default)</td><td class='tn'>Email</td><td class='tn'>Notification</td></tr>");
		sb.append("<tr>");
		sb.append("<td class='tn' ><textarea class='tn' rows='3' cols='40' name='zeroformat_").append(valInfo.m_id).append("'>").append(Misc.printString(fmtString, false)).append("</textarea>").append("<br/>").append(Misc.printString(defaultAlertFormat));
		sb.append("<td class='tn' ><textarea class='tn' rows='3' cols='40' name='oneformat_").append(valInfo.m_id).append("'>").append(Misc.printString(fmtStringEmail, false)).append("</textarea>").append("<br/>").append(Misc.printString(defaultAlertFormatEmail));
		sb.append("<td class='tn' ><textarea class='tn' rows='3' cols='40' name='twoformat_").append(valInfo.m_id).append("'>").append(Misc.printString(fmtStringNotification, false)).append("</textarea>").append("<br/>").append(Misc.printString(defaultAlertFormatNotification));
		sb.append("</tr></table></td></tr>");
		
		sb.append("<tr>");
		sb.append("<td class='tn' valign='top' colspan='4'>");
		sb.append("<table  _js='d' sd_alert_type='").append(valInfo.m_id).append("' style='margin-left:1px' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
		sb.append("<thead><tr>");
		
		if (distLabel != null) {
			sb.append("<td class='tshc'>").append(distLabel).append("</td>");
		}
		sb.append("<td class='tshc'>").append("Alert Type").append("</td>");
		sb.append("<td class='tshc'>").append("Alert To Role OR").append("</td>");
		sb.append("<td class='tshc'>").append("Whom (Leave blank if from challan)").append("</td>");
		sb.append("<td class='tshc'>").append("Phone").append("</td>");
		sb.append("<td class='tshc'>").append("Email").append("</td>");
		sb.append("<td class='tshc'>").append("Start Min").append("</td>");
		sb.append("<td class='tshc'>").append("End Min").append("</td>");
		sb.append("<td class='tshc'>").append("&nbsp;").append("</td>");
		sb.append("</tr></thead>");
		sb.append("<tbody>");
		for (int i=0,is = alertList == null ? 1 : alertList.size()+1; i<is; i++) {
			SrcDestInfo.AlertSetting alert = alertList == null || i >= alertList.size() ? null : alertList.get(i);
			sb.append("<tr>");
			if (distLabel != null) {
				sb.append("<td class='cn'>").append("<input name='dist' type='text' size='3' class='tn' value='").append(Misc.printDouble(alert == null ? Misc.getUndefDouble() : alert.getDist(),false)).append("'/>").append("</td>");
			}
			sb.append("<td class='cn'>");
			sb.append("<select name = 'type' class='tn' >");
            
			int alertType = alert == null ? Misc.getUndefInt() : alert.getAlertType();
			sb.append("<option value=").append(Misc.getUndefInt()).append(alertType == Misc.getUndefInt() ?" selected " :"").append(">").append("&lt; Select &gt;").append("</option>\n");
			ArrayList<DimInfo.ValInfo> valList = alertTypeDim.getValList();
			if (valList != null) {
				for (DimInfo.ValInfo vif : valList) {
					sb.append("<option value=").append(vif.m_id).append(alertType == vif.m_id ?" selected " :"").append(">").append(vif.m_name).append("</option>\n");
				}
			}
			
            if (notificationTypes != null) {
            	for (MiscInner.PairIntStr ntf : notificationTypes) {
					sb.append("<option value=").append(-1*ntf.first).append(-1*alertType == ntf.first ?" selected " :"").append(">").append(ntf.second).append("</option>\n");
				}
            }
           sb.append("</select>");
           sb.append("</td>");
           out.println(sb);
           sb.setLength(0);
           sb.append("<td class='cn'>");
		   sb.append("<select name = 'alert_to_role' class='tn' >");
		   out.println(sb);
	       sb.setLength(0);
		   _cache.printDimVals(alertRoleDim, alert == null ? Misc.getUndefInt() : alert.getAlertRole(), out, false, true, "&lt;Select Alertee's role or give name&gt;");
		   
		   sb.append("</select>");
	       sb.append("</td>");
	       out.println(sb);
	       sb.setLength(0);
           
			sb.append("<td class='cn' nowrap='nowrap' id='customerIdTD'>")
			.append("<select class='tn'")
			.append(" name='customerId' id='customerId' size='1' onChange='modify(this);'>")
			.append(" <option phone='' email='' value='-1'>Select</option>");
			out.println(sb);
			sb.setLength(0);
			String phone = null;
			String email = null;
			int custId = Misc.getUndefInt();
			if (customerContactsList != null) {
				for (Iterator<CustomerContactBean> iterator = customerContactsList.iterator(); iterator.hasNext();) {
					com.ipssi.tracker.customer.CustomerContactBean customerContactBean = (com.ipssi.tracker.customer.CustomerContactBean) iterator.next();
					boolean matching = alert != null && alert.getContactId() == customerContactBean.getId();
					if (matching) {
						phone = customerContactBean.getMobile();
						email = customerContactBean.getEmail();
						custId= customerContactBean.getId();
					}
					sb.append("<option phone='").append(customerContactBean.getMobile()).append("' email='").append(customerContactBean.getEmail())
						.append("' value='").append(customerContactBean.getId()).append("' ").append(matching ? " selected ":"").append(">")
						.append(customerContactBean.getName()).append("</option>");
					out.println(sb);
					sb.setLength(0);
				}//for
			}
			sb.append("</select><img src='").append(Misc.G_IMAGES_BASE).append("add_items.gif'  onClick=\"CallAjaxRequest(G_APP_1_BASE+'SrcDestServlet.do?action=addContact', 'addCont');\" />");
			sb.append("</td>");
			sb.append("<td class='tn'><input size='10' class='tn' name='phone' type='text' value='").append(Misc.printString(phone,false)).append("' onChange=\"custCont(").append(custId).append(",'").append(phone).append("',this)\"/>");
			sb.append("</td>");
			sb.append("<td class='tn'><input size='15' class='tn' name='Email' type='text' value='").append(Misc.printString(email,false)).append("' onChange=\"custCont(").append(custId).append(",'").append(email).append("',this)\"/>");
			sb.append("</td>");
		    int startMin = alert == null ? Misc.getUndefInt() : alert.getStartMin();
		    int endMin = alert == null ? Misc.getUndefInt() : alert.getEndMin();
			sb.append("<td class='tn'><input size='4' class='tn' name='start_min' type='text' value='").append(Misc.printInt(startMin,false)).append("'/>");
			sb.append("</td>");
			sb.append("<td class='tn'><input size='4' class='tn' name='end_min' type='text' value='").append(Misc.printInt(endMin,false)).append("'/>");
			sb.append("</td>");
			sb.append("<td class='cn' >");
			if (alert != null) { 
				sb.append("<img	src='").append(Misc.G_IMAGES_BASE).append("cancel.gif' onclick='removeRowHelper(event.srcElement);' />");
			}
			else {
				sb.append("<img src='").append(Misc.G_IMAGES_BASE).append("green_check.gif' onClick='addRow(this);' />");
			}
			sb.append("<input class='tn' id='test").append(custId).append("' name='test' type='hidden' value='-1' >");
			sb.append("</td>");
			
			sb.append("</tr>");
			out.println(sb);
			sb.setLength(0);
		}
		sb.append("</tbody>");
		sb.append("</table></td></tr>");
		sb.append("<tr><td colspan='1000'><hr noshade size=\"1\"></td></tr>");
		out.println(sb);
		sb.setLength(0);
	}
	
	public  SrcDestInfo getInfoForSave(String dataStr) throws Exception {
		try {
			SrcDestInfo retval = null;;
			//String dataStr = request.getParameter("XML_DATA");
			if (dataStr != null && dataStr.length() > 0) {
				Document dataDoc = MyXMLHelper.loadFromString(dataStr);
				SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
				int srcId =  Misc.getParamAsInt(m_session.getParameter("from_region"));
				int srcOpId = Misc.getParamAsInt(m_session.getParameter("from_station"));
				int srcType =SrcDestInfo.G_REGION_TYPE;
				if (!Misc.isUndef(srcOpId)) {
					srcType = SrcDestInfo.G_OP_STATION_TYPE;
					srcId = srcOpId;
				}
				double srcBuffer = Misc.getUndefDouble();
				double srcLong = Misc.getUndefDouble();
				double srcLat = Misc.getUndefDouble();
				String srcName = null;
				if (Misc.isUndef(srcId)) {
					srcLong = Misc.getParamAsDouble(m_session.getParameter("from_lon"));
					srcLat = Misc.getParamAsDouble(m_session.getParameter("from_lat"));
					srcName = Misc.getParamAsString(m_session.getParameter("from_map_item"),null);
					srcType = SrcDestInfo.G_MAP_TYPE;
					srcBuffer = Misc.getParamAsDouble(m_session.getParameter("from_buffer_map"));
				}
				//if (Misc.isUndef(srcId) && (Misc.isUndef(srcLat) || Misc.isUndef(srcLong) || srcName == null))
				//	return retval;
				int destId = Misc.getParamAsInt(m_session.getParameter("to_region"));
				int destType = SrcDestInfo.G_REGION_TYPE;
				int destOpId = Misc.getParamAsInt(m_session.getParameter("to_station"));
				if (!Misc.isUndef(destOpId)) {
					destType = SrcDestInfo.G_OP_STATION_TYPE;
					destId = destOpId;
				}
				double destBuffer = Misc.getUndefDouble();
				double destLong = Misc.getUndefDouble();
				double destLat = Misc.getUndefDouble();
				String destName = null;
				if (Misc.isUndef(destId)) {
					destLong = Misc.getParamAsDouble(m_session.getParameter("to_lon"));
					destLat = Misc.getParamAsDouble(m_session.getParameter("to_lat"));
					destName = Misc.getParamAsString(m_session.getParameter("to_map_item"),null);
					destType = SrcDestInfo.G_MAP_TYPE;
					destBuffer = Misc.getParamAsDouble(m_session.getParameter("to_buffer_map"));
				}
				double checkCont = Misc.getParamAsDouble(m_session.getParameter("cont_check_freq"));
				double delayThresh = Misc.getParamAsDouble(m_session.getParameter("delay_threshold"));
				int alertSrcDestId = Misc.getParamAsInt(m_session.getParameter("alert_src_dest_item_id"));
				int priority = Misc.getParamAsInt(m_session.getParameter("priority"),0);
				int stoppageRuleId = Misc.getParamAsInt(m_session.getParameter("stoppage_rule_id"),1);
				//if (Misc.isUndef(destId) && (Misc.isUndef(destLat) || Misc.isUndef(destLong) || destName == null))
				//	return retval;
				
				retval = new SrcDestInfo(Misc.getParamAsInt(m_session.getParameter("src_dest_item_id")),Misc.getParamAsInt(m_session.getParameter("src_dest_item_reverse_id")), m_session.getParameter("name"), Misc.getParamAsInt(m_session.getParameter("applicableTo")), Misc.getParamAsInt(m_session.getParameter("status")), srcId, srcType, srcLong, srcLat, srcBuffer, srcName
						, destId, destType, destLong, destLat, destBuffer, destName
						,Misc.getParamAsDouble(m_session.getParameter("lead_duration")), Misc.getParamAsDouble(m_session.getParameter("lead_distance")), Misc.getParamAsString(m_session.getParameter("notes"),null)
						,checkCont, alertSrcDestId, delayThresh,stoppageRuleId
						);
				retval.setPriority(priority);
				//
				for (Node n=dataDoc == null || dataDoc.getDocumentElement() == null ? null : dataDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
					if (n.getNodeType() != 1)
						continue;
					Element e = (Element) n;
					if (e.getTagName().equals("INTERMEDIATE") || (e.getTagName().equals("i"))) {
						readIntermediates(e, retval);
					}
					if (e.getTagName().equals("AREA_OPS") || (e.getTagName().equals("ao"))) {
						readAreaOfOps(e, retval);
					}
					else  {
						readAlerts(e, retval);
					}
				}
			}
		     return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void readIntermediates(Element elem, SrcDestInfo retval) {
		for (Node n=elem == null ? null : elem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			String name = Misc.getParamAsString(e.getAttribute("map_item"),null);
			double lon = Misc.getParamAsDouble(e.getAttribute("lon"));
			double lat = Misc.getParamAsDouble(e.getAttribute("lat"));
			double dur = Misc.getParamAsDouble(e.getAttribute("dur"));
			double buffer = Misc.getParamAsDouble(e.getAttribute("buffer_map"));
			int region = Misc.getParamAsInt(e.getAttribute("region"));
			if (Misc.isUndef(region) && Misc.isUndef(lon))
				continue;
			retval.addIntermediate(lon, lat, name, buffer, dur, region);
		}
	}
	
	private void readAreaOfOps(Element elem, SrcDestInfo retval) {
		for (Node n=elem == null ? null : elem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int region = Misc.getParamAsInt(e.getAttribute("region"));
			if (Misc.isUndef(region))
				continue;
			retval.addAreaOfOp(region);
		}
	}
	
	private void readRoadSegments(Element elem, SrcDestInfo retval) {
		for (Node n=elem == null ? null : elem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int region = Misc.getParamAsInt(e.getAttribute("road_id"));
			if (Misc.isUndef(region))
				continue;
			retval.addRoadSegment(region);
		}
	}
	private void readAlerts(Element elem, SrcDestInfo retval) {
		int srcDestDelayAlertType = Misc.getParamAsInt(elem.getAttribute("sd_alert_type"));
		String format = Misc.getParamAsString(elem.getAttribute("fmt0"));
		if (format != null) 
			retval.addAlertFormat(srcDestDelayAlertType, 0, format);
		format = Misc.getParamAsString(elem.getAttribute("fmt1"));
		if (format != null) 
			retval.addAlertFormat(srcDestDelayAlertType, 1, format);
		format = Misc.getParamAsString(elem.getAttribute("fmt2"));
		if (format != null) 
			retval.addAlertFormat(srcDestDelayAlertType, 2, format);
		
		for (Node n=elem == null ? null : elem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int alertType = Misc.getParamAsInt(e.getAttribute("type"));
			int contactId = Misc.getParamAsInt(e.getAttribute("customerId"));
			double dist = Misc.getParamAsDouble(e.getAttribute("dist"));
			int alertRole = Misc.getParamAsInt(e.getAttribute("alert_to_role"));
			retval.addAlert(srcDestDelayAlertType, alertType, dist, contactId, null,null,null, alertRole, Misc.getParamAsInt(e.getAttribute("start_min")), Misc.getParamAsInt(e.getAttribute("end_min")));
		}
	}
}

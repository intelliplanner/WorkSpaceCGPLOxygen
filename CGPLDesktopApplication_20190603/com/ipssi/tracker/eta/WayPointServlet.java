package com.ipssi.tracker.eta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.util.RuleProcessorGateway;
import com.ipssi.tracker.web.ActionI;

public class WayPointServlet  implements ActionI {
	private static Logger logger = Logger.getLogger(WayPointServlet.class);
	private SessionManager m_session;
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		boolean success = true;
		
		m_session = InitHelper.helpGetSession(request);
		String action = request.getParameter("action");
		Connection conn = m_session.getConnection();
	/*	
		if ("save".equals(action)) {
			ArrayList<Integer> deletedIds = this.getDeletedIds(m_session);
			for (int i=0,is=deletedIds.size();i<is;i++) {
				WayPointInfo.deleteWayPointInfo(conn, deletedIds.get(i));
			}
			ArrayList<WayPointInfo> saveList = getInfoForSave(request.getParameter("XML_DATA"), conn);
			WayPointInfo.saveWayPointInfoList(conn, saveList);
			//do refresh ... we need to get the vehicleId associated .. it will be the same as in saveList!!
			ArrayList<Integer> vehicleList = new ArrayList<Integer>();
			for (int i=0,is = saveList.size();i<is;i++) {
				vehicleList.add(saveList.get(i).getVehicleId());
			}
			if (!conn.getAutoCommit())
				conn.commit();
			RuleProcessorGateway.refreshWaypointETA(vehicleList,Misc.getServerName());
		}
		else if ("estimate".equals(action)) {
			int srcDestItemId = Misc.getParamAsInt(m_session.getParameter("src_dest_item_id"));
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			Date startDate = Misc.getParamAsDate(m_session.getParameter("src_exit"), null, sdf);
			int vehicleId = Misc.getParamAsInt(m_session.getParameter("vehicle_id"));
			Date lastEndDate = Misc.getParamAsDate(m_session.getParameter("last_date"), null, sdf);
			WayPointInfo wp = WayPointInfo.guessSuitableWayPointInfo(conn, vehicleId, srcDestItemId,startDate == null ? Misc.getUndefInt() : startDate.getTime(), lastEndDate == null ? Misc.getUndefInt() : lastEndDate.getTime(), CacheTrack.VehicleSetup.getSetup(vehicleId, conn));
			if (wp == null)
				request.setAttribute("out_xml_str", "<data/>");
			else
				request.setAttribute("out_xml_str", wp.toXML());
		}
		else {
			String vehList[] = request.getParameterValues("vehicle_id");
			
			ArrayList<Integer> vehicleIds = new ArrayList<Integer>();
			int vehiclePortNodeId = Misc.getUndefInt();
			for (int i=0,is=vehList == null ? 0 :vehList.length;  i<is;i++) {
				int v = Misc.getParamAsInt(vehList[i]);
				if (!Misc.isUndef(v)) {
					vehicleIds.add(v);
					CacheTrack.VehicleSetup vsetup = CacheTrack.VehicleSetup.getSetup(v, conn);
					if (vsetup != null)
						vehiclePortNodeId = vsetup.m_ownerOrgId;
				}
			}
			ArrayList<WayPointInfo> wpList = WayPointInfo.getWayPointInfo(conn, vehicleIds, true);
			int orgId =  vehiclePortNodeId;//Misc.getParamAsInt(m_session.getParameter("pv123"));
			ArrayList<MiscInner.PairIntStr> optionsList = getSrcDestList(conn, orgId);
			request.setAttribute("waypoint_list", wpList);
			request.setAttribute("src_dest_list", optionsList);
		}
		*/
		String actionForward = "";
		actionForward = sendResponse(action, success, request);	
		return actionForward;
	}

	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		if ("save".equals(action) && success)
		   return "/waypoint_close.jsp";
		else if ("estimate".equals(action)) {
			return "/genAjaxXMLGetter.jsp";
		}
		else
			return "/waypoint.jsp";
	}
	
	public static ArrayList<MiscInner.PairIntStr> getSrcDestList(Connection conn, int pv123) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select sd.id, sd.name from src_dest_items sd join port_nodes anc on (anc.id = sd.port_node_id and sd.status in (1)) join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by sd.name");
		ps.setInt(1, pv123);
		ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(ps);
		ps.close();
		return retval;
	}
	public static ArrayList<Integer> getDeletedIds(SessionManager session) {
		String delId = session.getParameter("deleted_ids");
		ArrayList<Integer> retval = new ArrayList<Integer>();
		if (delId != null && delId.length() != 0) {
			Misc.convertValToVector(delId, retval);
		}
		return retval;
	}
	/*
	public static ArrayList<WayPointInfo> getInfoForSave(String dataStr, Connection conn) throws Exception {
		try {
			ArrayList<WayPointInfo> retval = new ArrayList<WayPointInfo> ();
			//String dataStr = request.getParameter("XML_DATA");
			if (dataStr != null && dataStr.length() > 0) {
				Document dataDoc = MyXMLHelper.loadFromString(dataStr);
				SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
				for (Node n=dataDoc == null || dataDoc.getDocumentElement() == null ? null : dataDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
					if (n.getNodeType() != 1)
						continue;
					Element e = (Element) n;
					//int id, int vehicleId, int srcDestItemId, int status, String srcDestName, Date srcExit, Date destBase, Date destETA, Date destAct
					Date srcExitDt = Misc.getParamAsDate(e.getAttribute("src_exit"),null, sdf);
					Date destBaseDt = Misc.getParamAsDate(e.getAttribute("dest_base"),null, sdf);
					Date destEta = Misc.getParamAsDate(e.getAttribute("dest_eta"),null, sdf);
					Date destAct = Misc.getParamAsDate(e.getAttribute("dest_act"),null, sdf);
					WayPointInfo wp = new WayPointInfo(Misc.getParamAsInt(e.getAttribute("id")), Misc.getParamAsInt(e.getAttribute("vehicle_id")), Misc.getParamAsInt(e.getAttribute("src_dest_item_id")) , Misc.getParamAsInt(e.getAttribute("status")), null
							,srcExitDt == null ? Misc.getUndefInt() : srcExitDt.getTime(), destBaseDt == null ? Misc.getUndefInt() : destBaseDt.getTime() , destEta == null ? null : destEta.getTime() , destAct == null ? Misc.getUndefInt() : destAct.getTime() 
							);
					
					for (int i=0;i<WayPointInfo.G_MAX_INTERMEDIATES;i++) {
						String prefix = "wp"+(i+1)+"_";
						String name = Misc.getParamAsString(e.getAttribute(prefix+"name"),null);
						if (name == null)
							break;
						Date eta = Misc.getParamAsDate(e.getAttribute(prefix+"eta"),null, sdf);
						Date base = Misc.getParamAsDate(e.getAttribute(prefix+"base"),null, sdf);
						Date act = Misc.getParamAsDate(e.getAttribute(prefix+"act"),null, sdf);
						wp.addWPInfo(name,  base == null ? Misc.getUndefInt() : base.getTime(), eta == null ? Misc.getUndefInt() : eta.getTime(), act == null ? Misc.getUndefInt() : act.getTime());
					}
					retval.add(wp);
				}
			}
		     return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	*/
}

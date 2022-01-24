package com.ipssi.deviceMessaging;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.deviceMessaging.Message;
import com.ipssi.gen.deviceMessaging.MessageCache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.tracker.web.ActionI;

public class DeviceMessageServlet implements ActionI {
	private static Logger logger = Logger.getLogger(DeviceMessageServlet.class);
	private SessionManager m_session;
	
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		boolean success = true;
		
		m_session = InitHelper.helpGetSession(request);
		String action = request.getParameter("action");
		Connection conn = m_session.getConnection();
		if ("save".equals(action)) {
			User _user = InitHelper.helpGetUser(request);
			ArrayList<DeviceMessageBean> saveList = this.getInfoForSave(request, conn);
			ArrayList<DeviceMessageBean> errorList = this.saveInfoForSave(conn, saveList,_user.getUserId());
			if (errorList != null && errorList.size() > 0) {
				success = false;
				request.setAttribute("info", errorList);
				request.setAttribute("_errMsg", "Errors happened while sending message - please resubmit for the vehicles below");
			}
		}
		else {
			ArrayList<DeviceMessageBean> viewList = this.getInfoForShow(request, conn);
			request.setAttribute("info", viewList);
		}
		String actionForward = "";
		actionForward = sendResponse(action, success, request);	
		return actionForward;
	}

	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		if ("save".equals(action) && success)
		   return "/sendMsgClose.jsp";
		else
			return "/sendMsg.jsp";
	}
	
	public ArrayList<DeviceMessageBean> getInfoForShow(HttpServletRequest request, Connection conn) throws Exception {
		try {
			StringBuilder sb = new StringBuilder();
			String[] vehicleId = request.getParameterValues("vehicle_id");
			sb.append("select distinct vehicle.id, vehicle.customer_id, vehicle.name, vehicle.sim_number, msg.message, (case when msg.acknowledge_date is not null then msg.acknowledge_date when msg.latest_try_date is not null then msg.latest_try_date else msg.in_date end) dt, msg.status ")
			.append(", recv.message rcv_message, recv.record_time rcv_time, vehicle.device_internal_id did ")
			.append(" from vehicle left outer join (select vehicle_id, max(in_date) dt from vehicle_messages group by vehicle_id) mx on (vehicle.id = mx.vehicle_id) left outer join vehicle_messages msg on (msg.vehicle_id = mx.vehicle_id and msg.in_date = mx.dt) ")
			.append(" left outer join (select vehicle_id vid, max(record_time) rt from vehicle_recvd_messages group by vehicle_id) rcvmx on (vehicle.id = rcvmx.vid) ")
			.append(" left outer join vehicle_recvd_messages recv on (recv.vehicle_id = rcvmx.vid and rcvmx.rt = recv.record_time) ")
			.append(" where vehicle.status in (1) ");
			if (vehicleId != null && vehicleId.length > 0) {
				sb.append(" and vehicle.id in (");
				Misc.convertInListToStr(vehicleId, sb);
				sb.append(" ) ");
			}
			//sb.append(" order by rcv_time desc limit 1");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			ArrayList<DeviceMessageBean> retval = new ArrayList<DeviceMessageBean>();
			while (rs.next()) {
				DeviceMessageBean bean = new DeviceMessageBean(rs.getInt(1), rs.getString("name"), rs.getString("sim_number"), rs.getString("message"), rs.getInt("status"), Misc.sqlToUtilDate(rs.getTimestamp("dt")), rs.getInt("customer_id"), rs.getString("rcv_message"), Misc.sqlToUtilDate(rs.getTimestamp("rcv_time")), rs.getString("did"));
				retval.add(bean);
			}
			rs.close();
			ps.close();
		    return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private Pair<ArrayList<Integer>, ArrayList<String>> getWildCharRepl(String commonMessage) {
		
		ArrayList<Integer> replIndex = null; //when doing go in rev order
		ArrayList<String> replString = null; //what string needs to be replaced
		if (commonMessage != null) {
			//check if replacement needs to happen
			int startPerPos = -1;
			int pos = -1;
			boolean done = false;
			while (!done) {
				pos = commonMessage.indexOf('%', pos+1);
				if (pos == -1) {
					done = true;
					break;
				}
				if (pos > 0 && commonMessage.charAt(pos-1) == '\\') {
					//do nothing
				}
				else {
					if (startPerPos >= 0) {
						if (replIndex == null)
							replIndex = new ArrayList<Integer>();
						if (replString == null) 
							replString = new ArrayList<String>();
						replIndex.add(startPerPos);
						replString.add(commonMessage.substring(startPerPos+1, pos));
						startPerPos = -1;
					}
					else {
						startPerPos = pos;
					}
				}
			}
		}
		return new Pair<ArrayList<Integer>, ArrayList<String>>(replIndex, replString);
	}
	
	public ArrayList<DeviceMessageBean> getInfoForSave(HttpServletRequest request, Connection conn) throws Exception {
		try {
			ArrayList<DeviceMessageBean> retval = new ArrayList<DeviceMessageBean> ();
			String commonMessage = request.getParameter("common_message");
			if (commonMessage != null)
				commonMessage.trim();
			if (Misc.emptyString.equals(commonMessage))
				commonMessage = null;
			Pair<ArrayList<Integer>, ArrayList<String>> commonReplacement = getWildCharRepl(commonMessage);
			String dataStr = request.getParameter("XML_DATA");
			PreparedStatement ps1 = null;
			if (dataStr != null && dataStr.length() > 0) {
				Document dataDoc = MyXMLHelper.loadFromString(dataStr);
				
				for (Node n=dataDoc == null || dataDoc.getDocumentElement() == null ? null : dataDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
					if (n.getNodeType() != 1)
						continue;
					Element e = (Element) n;
					String msg = e.getAttribute("message");
					if (msg != null)
						msg = msg.trim();
					Pair<ArrayList<Integer>, ArrayList<String>> replacement = null;
					if (msg == null || msg.trim().length() == 0) { 
						msg = commonMessage;
						replacement = commonReplacement;
					}
					else {
						replacement = getWildCharRepl(msg);
					}
					int vehicleId = Misc.getParamAsInt(e.getAttribute("vehicle_id"));
					if (Misc.isUndef(vehicleId) || msg == null || msg.length() == 0)
						continue;
					if (replacement != null && replacement.first != null && replacement.first.size() > 0) {
						for (int i=replacement.second.size()-1;i>=0;i--) {
							String pattern = replacement.second.get(i);
							int pos = replacement.first.get(i);
							if (pattern.equals("did")) {
								if (ps1 == null)
									ps1 = conn.prepareStatement("select device_internal_id from vehicle where id = ?");
								ps1.setInt(1, vehicleId);
								ResultSet rs = ps1.executeQuery();
								String deviceId = null;
								if (rs.next()) {
									deviceId = rs.getString(1);
								}
								rs.close();
								if (deviceId == null) {
									msg = null; //dont send message ... something screwed up
									break;
								}
								msg = msg.substring(0, pos)+deviceId+msg.substring(pos+pattern.length()+2);
							}
							else {
								//dont know .... just send as is
							}
						}
					}
					
					DeviceMessageBean bean = new DeviceMessageBean(vehicleId, msg);
					retval.add(bean);
				}
			}
			if (ps1 != null)
				ps1.close();
			 return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public ArrayList<DeviceMessageBean> saveInfoForSave(Connection conn, ArrayList<DeviceMessageBean> saveList, int userId) throws Exception { //returns the list with error
		ArrayList<DeviceMessageBean> retval = new ArrayList<DeviceMessageBean>();
		PreparedStatement ps = null;
		for (DeviceMessageBean bean: saveList) {
			try {
				Message message = MessageCache.addMessage(conn, bean.getVehicleId(), bean.getLastMessage(), 0, true, CacheTrack.VehicleSetup.getSetup(bean.getVehicleId(), conn));
				String query = " update vehicle_messages join (select ? message_id ,name from current_data " +
							   " where current_data.vehicle_id=? and current_data.attribute_id=0) " +
							   " curr on (vehicle_messages.id= curr.message_id) set create_location=curr.name,updated_by=?";
				ps = conn.prepareStatement(query);
				Misc.setParamInt(ps,message.getId(), 1);
				Misc.setParamInt(ps,bean.getVehicleId(), 2);
				Misc.setParamInt(ps,userId, 3);
				ps.executeUpdate();
				ps.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				retval.add(bean);
				if(ps != null)
					ps.close();
				//throw e;
			}
		}
		return retval;
	}
}

package com.ipssi.deviceMessaging;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.deviceMessaging.Message;
import com.ipssi.gen.deviceMessaging.MessageCache;
import com.ipssi.gen.deviceMessaging.MessageStatus;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tracker.vehicle.VehicleBean;
import com.ipssi.tracker.web.ActionI;
import com.ipssi.deviceMessaging.*;

public class SendVehicleMessage implements ActionI {
	public static class ExtMessage {
		public String message = null;
		public Date inDate = null;
		public int id = Misc.getUndefInt(); //message Id in DB
		public Date latestTryDate = null;
		public Date acknowledgeDate = null;
		public MessageStatus status = MessageStatus.CREATED;
		public int deliveryMode = 0; //0 = GPRS, 1 = SMS
		public String acknowledgeMessage = null;
		public double lon = 0;
		public double lat = 0;
		public String posnName = null;
		public ExtMessage(String message, Date inDate, int id, Date latestTryDate, Date acknowledgeDate, MessageStatus status, int deliveryMode, String acknowledgeMessage, double lon, double lat, String posnName) {
			this.message = message;
			this.inDate = inDate;
			this.id = id;
			this.latestTryDate = latestTryDate;
			this.acknowledgeDate = acknowledgeDate;
			this.status = status;
			this.deliveryMode = deliveryMode;
			this.acknowledgeMessage = acknowledgeMessage;
			this.lon = lon;
			this.lat = lat;
			this.posnName = posnName;
		}
	}
private SessionManager m_session;
private static int g_sleepDurMilli = 2000;
private static int g_totSleepTryCount = 64*1000/g_sleepDurMilli;

	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		boolean success = true;
		
		m_session = InitHelper.helpGetSession(request);
		String action = request.getParameter("action");
		if (action == null || action.length() == 0)
			action = "send";
		Connection conn = m_session.getConnection();
		Triple<Integer, Integer, String> result = null; //1st = message id, 2nd = response, 3rd = error message
		Pair<ExtMessage, String> getDetailResult = null;
		if ("send".equals(action)) {
			result = send(m_session);
			if (result != null && !Misc.isUndef(result.first) && result.second != 0) {
				getDetailResult = this.getMessage(m_session.getConnection(), result.first, result.second);
			}
		}
		else if ("get_details".equals(action)) {
			int messageId = Misc.getParamAsInt(request.getParameter("message_id"));
			int responseMode = Misc.getParamAsInt(request.getParameter("response_mode"),0);
			getDetailResult = this.getMessage(m_session.getConnection(), messageId, responseMode);
		}
	   //now populate the result in sb_list0
		StringBuilder sb_list0 = new StringBuilder();
		if (getDetailResult == null && result == null) {
			sb_list0.append(Misc.getUndefInt()).append(",").append("Unknown Error");
		}
		else if (getDetailResult == null && result != null) {
			sb_list0.append(result.first);
			if (result.third != null && result.third.length() != 0) {
				sb_list0.append(",").append(result.third);
			}
		}
		else {
			ExtMessage msg = getDetailResult.first;
			if (msg == null) {
				sb_list0.append(result != null ? -1*result.first : Misc.getUndefInt());
				sb_list0.append(",").append(getDetailResult.second);
			}
			else {
				sb_list0.append(msg.id);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				//Message_id, status, time_of_receive, time_of_sent, time_of_ack, latitude_of_ack, longitude_of_ack, position_name_of_ack
				DimInfo dimInfo = DimInfo.getDimInfo(22012);
				sb_list0.append(",").append(dimInfo.getValInfo(msg.status.value()).m_name);
				sb_list0.append(",").append(Misc.printDate(sdf,msg.inDate));
				sb_list0.append(",").append(Misc.printDate(sdf,msg.latestTryDate));
				sb_list0.append(",").append(Misc.printDate(sdf,msg.acknowledgeDate));
				sb_list0.append(",").append(msg.lat);
				sb_list0.append(",").append(msg.lon);
				sb_list0.append(",").append(Misc.printString(msg.posnName, false));
				
			}
		}
		request.setAttribute("sb_list0", sb_list0);
		String actionForward = "";
		actionForward = sendResponse(action, success, request);	
		return actionForward;
	}

	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		return "/genAjaxStringGetter.jsp";
	}
	
	Triple<Integer, Integer, String> send(SessionManager session) {
		Triple<Integer, Integer, String> retval = new Triple<Integer, Integer, String>(Misc.getUndefInt(), 0, "Unknown Error");
		Connection conn = null;
		try {
			HttpServletRequest request = session.request;
			conn = session.getConnection();
			String vehicleName = request.getParameter("vehicle");
			String message = request.getParameter("message");
			int responseMode = Misc.getParamAsInt(request.getParameter("response_mode"), 0);
			vehicleName = VehicleBean.standardizeName(vehicleName);
			PreparedStatement ps = conn.prepareStatement("select id from vehicle where status=1 and std_name=?");
			ps.setString(1, vehicleName);
			ResultSet rs = ps.executeQuery();
			int vehicleId = Misc.getUndefInt();
			if (rs.next()) {
				vehicleId = rs.getInt(1);
			}
			rs.close();
			ps.close();
			if (Misc.isUndef(vehicleId)) {
				retval.third = "Unknown vehicle "+vehicleName;
				return retval;
			}
			Message msg = MessageCache.addMessage(conn, vehicleId, message, 0, true, CacheTrack.VehicleSetup.getSetup(vehicleId, conn));
			if (msg != null) {
				retval.first = msg.getId();
				retval.second = responseMode;
				retval.third = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			try {
				if (conn != null && !conn.getAutoCommit()) {
					conn.rollback();
				}
			}
			catch (Exception e2) {
				//eat it
			}
			retval.third = e.getMessage();
			//eat it
		}
		return retval;
	}

	private Pair<ExtMessage, String> getMessage(Connection conn, int messageId, int responseMode) {
		Pair<ExtMessage, String>  retval = new Pair<ExtMessage, String>(null, "Timeout - response not received Yet");
		try {
			int tryAttempt = 0;
			PreparedStatement ps = conn.prepareStatement("select id,message, in_date, latest_try_date, acknowledge_date, status, delivery_mode, acknowledge_message, longitude, latitude, posn_name from vehicle_messages where id = ?");
			ps.setInt(1, messageId);
			
			for (;tryAttempt < this.g_totSleepTryCount;tryAttempt++) {
				ResultSet rs = ps.executeQuery();
				ExtMessage temp = null;
				if (rs.next()) {
					String message = rs.getString("message");
					Date inDate = Misc.sqlToUtilDate(rs.getTimestamp("in_date"));
					Date tryDate = Misc.sqlToUtilDate(rs.getTimestamp("latest_try_date"));
					Date ackDate = Misc.sqlToUtilDate(rs.getTimestamp("acknowledge_date"));
					int status = rs.getInt("status");
					int deliveryMode = rs.getInt("delivery_mode");
					String ackMessage = rs.getString("acknowledge_message");
					double lon = Misc.getRsetDouble(rs, "longitude", 0);
					double lat = Misc.getRsetDouble(rs, "latitude", 0);
					String posnName = rs.getString("posn_name");
					temp  = new ExtMessage(message, inDate, messageId, tryDate, ackDate, MessageStatus.toMessageStatus(status), deliveryMode, ackMessage, lon, lat, posnName);
				}
				else {
					retval.second = "Unknown Message";
					break;
				}
				rs.close();
				if (responseMode == 0) {
					retval.first = temp;
					retval.second = null;
					break;
				}
				else if (responseMode == 1) {
					if (temp.latestTryDate != null) {
						retval.first = temp;
						retval.second = null;
						break;
					}
				}
				else {
					if (temp.acknowledgeDate != null) {
						retval.first = temp;
						retval.second = null;
						break;
					}
				}
				try {
					Thread.sleep(g_sleepDurMilli);
				}
				catch (Exception e) {
					
				}
			}//loop of try
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			retval.second = e.getMessage(); 
			//eat it
		}
		return retval;
	}
}

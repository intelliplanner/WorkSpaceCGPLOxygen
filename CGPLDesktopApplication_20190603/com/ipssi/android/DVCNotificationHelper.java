package com.ipssi.android;

import java.sql.Connection;
import java.sql.SQLException;

import com.ipssi.communicator.dto.CommunicatorDTO;
import com.ipssi.communicator.dto.CommunicatorQueueSender;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class DVCNotificationHelper {
	private static final int SMS = 1;
	private static final int EMAIL = 2;

	public static void sendSMSNotification(String contactList, String msz,
			String vehicleName) {
		CommunicatorDTO commDTO = new CommunicatorDTO();
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn.setAutoCommit(true);
			int vehicleId = getVehicleId(conn, vehicleName);
			String[] mobileNumberList = contactList.split(",");
			for (int i = 0; i < mobileNumberList.length; i++) {
				commDTO.setBody(msz);
				commDTO.setNotificationType(SMS);
				commDTO.setTo(mobileNumberList[i]);
				commDTO.setForceSend(true);
				commDTO.setVehicleId(vehicleId);// "ENTER VEHICLE ID"
				commDTO.setAlertIndex(0);
				commDTO.setRuleId(10001);
				commDTO.setEngineEventId(Misc.UNDEF_VALUE);
				try {
					CommunicatorQueueSender.send(commDTO);
				} catch (Exception e) {
					System.out
							.println("Error in EmailNotificationHandler while send message to queue "
									+ e);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null && conn.getAutoCommit())
					conn.setAutoCommit(false);
				if (conn != null) {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void sendEMailNotification(String emailList, String msz,
			String vehicleName) {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn.setAutoCommit(true);
			int vehicleId = getVehicleId(conn, vehicleName);
			String[] emailAddressList = emailList.split(",");
			for (int i = 0; i < emailAddressList.length; i++) {
				CommunicatorDTO commDTO = new CommunicatorDTO();
				commDTO.setNotificationType(EMAIL);
				commDTO.setTo(emailAddressList[i]);
				commDTO.setBody(msz);
				commDTO.setForceSend(true);
				commDTO.setVehicleId(vehicleId);// vehicle ID
				commDTO.setAlertIndex(0);
				commDTO.setRuleId(10000);
				commDTO.setSubject(vehicleName + " installed");
				commDTO.setEngineEventId(Misc.UNDEF_VALUE);
				System.out
						.println("EmailNotificationHandler.sendNotification() :: Sending out Notification :: "
								+ commDTO.getBody());
				// RuleProcessorUtil.logToDebugFile("<hr><b>EmailNotificationHandler.sendNotification()</b> :: Sending out Notification :: "
				// + commDTO.getBody() + "\n\n\r");
				try {
					CommunicatorQueueSender.send(commDTO);
				} catch (Exception e) {
					System.out
							.println("Error in EmailNotificationHandler while send message to queue "
									+ e);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null && conn.getAutoCommit())
					conn.setAutoCommit(false);
				if (conn != null) {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static int getVehicleId(Connection conn, String vehicleName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int vehicleId = 0;
		try {
			ps = conn
					.prepareStatement("select id from vehicle where name=? limit 1");
			ps.setString(1, vehicleName);
			rs = ps.executeQuery();
			if (rs.next()) {
				vehicleId = Misc.getRsetInt(rs, 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vehicleId;
	}

	public static String getContacts(Connection conn, String destination) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		JSONArray array = new JSONArray();
		String mobile, email;
		try {
			ps = conn
					.prepareStatement("select mobile,email from dvc_contacts where dest=?");
			ps.setString(1, destination);
			rs = ps.executeQuery();
			while (rs.next()) {
				JSONObject object = new JSONObject();
				mobile = rs.getString(1);
				email = rs.getString(2);
				try {
					object.put("mobile", mobile);
					object.put("email", email);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				array.put(object);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return array.toString();

	}
}

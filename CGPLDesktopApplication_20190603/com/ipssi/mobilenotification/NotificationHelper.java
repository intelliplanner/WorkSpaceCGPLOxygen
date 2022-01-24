package com.ipssi.mobilenotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ipssi.eta.NewETAAlertHelper;
import com.ipssi.eta.SrcDestInfo;
import com.ipssi.eta.NewETAAlertHelper.WhoToSend;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Value;
import com.ipssi.mobilenotification.Notification;
import com.ipssi.mobilenotification.NotificationHelper;
import com.ipssi.mobilenotification.NotificationInfo;

public class NotificationHelper {

	public static int createNotificationInfo(Connection conn, int vehicleId,
			int parentNotificationId, int notificationTypeId, int forTarget,
			NewETAAlertHelper.WhoToSend whoToSend) {
		ArrayList<String> userList = null;
		switch (forTarget) {
		case SrcDestInfo.G_TARGET_SENDER:
			userList = whoToSend.forSender;
			break;
		case SrcDestInfo.G_TARGET_RECEIVER:
			userList = whoToSend.forCustomer;
			break;
		case SrcDestInfo.G_TARGET_TRANSPORTER:
			userList = whoToSend.forTransporter;
			break;
		case SrcDestInfo.G_TARGET_UNCLASSIFIED:
			userList = whoToSend.unclassified;
			break;
		}

		Notification notification = Notification.getNotification(conn,
				notificationTypeId);
		NotificationInfo nInfo = new NotificationInfo();
		nInfo.setNotificationId(notification.getId());
		nInfo.setVehicleId(vehicleId);
		StringBuilder nText = getText(notification);
		nInfo.setText(nText != null ? nText.toString() : null);
		nInfo.setQuestionsId_1(notification.getQuestionsId_1());
		nInfo.setQuestionsId_2(notification.getQuestionsId_2());
		nInfo.setQuestionsId_3(notification.getQuestionsId_3());
		nInfo.setGReminderDuration(notification.getGReminderDuration());
		nInfo.setConsigneeId(whoToSend.forSender);
		nInfo.setConsignorId(whoToSend.forCustomer);
		nInfo.setTransporterId(whoToSend.forTransporter);
		nInfo.setUnclassifiedId(whoToSend.unclassified);
		nInfo.setForTarget(forTarget);
		int retval = NotificationInfo.saveNotificationMetaData(conn,
				parentNotificationId, nInfo);
		nInfo.setGroupNotificationId(retval);
		for (String id : userList) {
			NotificationInfo.saveNotificationInfo(conn, nInfo, id);
		}
		// prepareAndSendNotification(conn, nInfo, userList);
		// NotificationInfo.updateNotificationInfo(conn,
		// nInfo.getNotificationId(), NotificationStatus.SEND);
		return retval;
	}

	public static ArrayList<NotificationBean> getActiveNotifications(
			Connection conn) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<NotificationBean> notificationList = new ArrayList<NotificationBean>();

		String query = "select m_user_notification_info.id, m_user_notification_info.m_notification_id,user_id,vehicle.name,m_user_notification_info.text,mq1.text ques_text1,"
				+ "mq1.answer_type ans_type_1,m_questions_id_1,mq2.text ques_text2,mq2.answer_type ans_type_2,"
				+ "m_questions_id_2,mq3.text ques_text3,mq3.answer_type ans_type_3,m_questions_id_3 "
				+ "from m_user_notification_info join m_questions mq1 on mq1.id=m_questions_id_1 "
				+ "join m_questions mq2 on mq2.id = m_questions_id_2 join m_questions mq3 on "
				+ "mq3.id=m_questions_id_3 join vehicle on vehicle.id=m_user_notification_info.vehicle_id"
				+ " where m_user_notification_info.status = ? OR (select date_add(m_user_notification_info.updated_on,interval g_reminder_duration minute) <= now() AND m_user_notification_info.status = ?)";
		try {
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, NotificationStatus.ACTIVE);
			stmt.setInt(2, NotificationStatus.SEND);
			rs = stmt.executeQuery();

			while (rs.next()) {
				NotificationBean bean = new NotificationBean();
				bean.setId(Misc.getRsetInt(rs, "id"));
				bean
						.setNotificationId(Misc.getRsetInt(rs,
								"m_notification_id"));
				bean.setUserId(Misc.getRsetInt(rs, "user_id"));
				bean.setVehicleName(Misc.getRsetString(rs, "vehicle.name"));
				bean.setNotificationText(Misc.getRsetString(rs,
						"m_user_notification_info.text"));
				bean.setQuestionText_1(Misc.getRsetString(rs, "ques_text1"));
				bean.setQuestionsId_1(Misc.getRsetInt(rs, "m_questions_id_1"));
				bean.setQuestionText_2(Misc.getRsetString(rs, "ques_text2"));
				bean.setQuestionsId_2(Misc.getRsetInt(rs, "m_questions_id_2"));

				bean.setQuestionText_3(Misc.getRsetString(rs, "ques_text3"));
				bean.setQuestionsId_3(Misc.getRsetInt(rs, "m_questions_id_3"));

				bean.setAnswerType_1(Misc.getRsetInt(rs, "ans_type_1"));
				bean.setAnswerType_2(Misc.getRsetInt(rs, "ans_type_2"));
				bean.setAnswerType_3(Misc.getRsetInt(rs, "ans_type_3"));

				notificationList.add(bean);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return notificationList;
	}

	public static String getFCMTokens(Connection conn, int userId) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String fcmToken = null;
		// StringBuilder usersId = new StringBuilder();
		// for (String id : users) {
		// usersId.append(usersId.length() == 0 ? id : "," + id);
		// }
		String query = "select fcm_id from users where id like " + userId;
		try {
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			while (rs.next()) {
				fcmToken = Misc.getRsetString(rs, "fcm_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Misc.closePS(stmt);
			try {
				Misc.closeResultSet(rs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fcmToken;
	}

	public static void prepareAndSendNotification(Connection conn) {
		System.out.println("prepare and send notification");
		ArrayList<NotificationBean> beanList = getActiveNotifications(conn);
		FcmClient client = new FcmClient();
		client.setAPIKey("AAAA4ouh_6Y:APA91bEznEuu6dLtOv_amJeMWpl0OsYOiHJWs7YGDWrij1vKl60s7ftlxHjWdyHyH9wrn8zNRXG2WXhTe69YQfoTO1-ICkFSB8vorrrOv2Uza51zsYq-qlkWTQE0_1H_YTntGvZTmu0D");//("AIzaSyBZfBZ1aDvCQelVZCWIqIPDk3oGeT1Yj9U");
		for (int i = 0; i < beanList.size(); i++) {
			NotificationBean bean = beanList.get(i);
			String fcmToken = getFCMTokens(conn, bean.getUserId());
			//StringBuilder tokens = new StringBuilder();

			JSONObject head = new JSONObject();
			JSONObject data = new JSONObject();
			try {
				head.put("to", fcmToken);
				data.put("id", bean.getId());
				data.put("notification_id", bean.getNotificationId());
				data.put("notification_text", bean.getNotificationText());
				data.put("vehicle", bean.getVehicleName());
				data.put("ques_id_1", bean.getQuestionsId_1());
				data.put("ques_id_2", bean.getQuestionsId_2());
				data.put("ques_id_3", bean.getQuestionsId_3());
				data.put("ques_text_1", bean.getQuestionText_1());
				data.put("ques_text_2", bean.getQuestionText_2());
				data.put("ques_text_3", bean.getQuestionText_3());
				data.put("ans_type_1", bean.getAnswerType_1());
				data.put("ans_type_2", bean.getAnswerType_2());
				data.put("ans_type_3", bean.getAnswerType_3());
				head.put("data", data);
				FcmResponse response = client.pushNotify(head);
				if (response.getSuccess() > 0)
					NotificationInfo.updateNotificationInfo(conn, bean.getId(),
							NotificationStatus.SEND);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	// public static void updateNotificationInfo(Connection conn,
	// int notificationId, int consigneeId, int consignorId,
	// int transporterId, int vehicleId) throws Exception {
	//
	// Notification notification = Notification.getNotification(conn,
	// notificationId);
	// NotificationInfo nInfo = new NotificationInfo();
	// // nInfo.setVehicleId(vehicleId);
	// // nInfo.setConsigneeId(consigneeId);
	// // nInfo.setConsignorId(consignorId);
	// // nInfo.setTransporterId(transporterId);
	//
	// StringBuilder nText = getText(notification);
	// nInfo.setText(nText != null ? nText.toString() : null);
	// nInfo.setQuestionsId_1(notification.getQuestionsId_1());
	// nInfo.setQuestionsId_2(notification.getQuestionsId_2());
	//
	// NotificationInfo.saveNotificationInfo(conn, nInfo);
	// }

	public static boolean updateNotificationInfoWithResponse(Connection conn,
			int nNotificationId, int id, int answer1, int answer2, int answer3,
			String answerText1, String answerText2, String answerText3)
			throws Exception {

		NotificationInfo nInfo = new NotificationInfo();
		nInfo.setNotificationId(nNotificationId);
		nInfo.setId(id);
		nInfo.setAnswersId_1(answer1);
		nInfo.setAnswersId_2(answer2);
		nInfo.setAnswersId_3(answer3);
		nInfo.setAnswerText_1(answerText1);
		nInfo.setAnswerText_2(answerText2);
		nInfo.setAnswerText_3(answerText3);
		nInfo.setNotes(answerText1 != null ? answerText1
				: answerText2 != null ? answerText2 : null);

		boolean retval = NotificationInfo.updateNotificationInfoText(conn,
				nInfo);
		NotificationInfo.updateNotificationInfo(conn, id,
				NotificationStatus.CLOSE);

		nInfo = NotificationInfo.getNotificationInfo(conn, nNotificationId);
		Notification notification = Notification.getNotification(conn, nInfo
				.getNotificationId());
		WhoToSend whoToSend = NotificationInfo.getUserList(conn,
				nNotificationId);
		// WhoToSend whoToSend = new WhoToSend();
		// whoToSend.forCustomer = nInfo.getConsignorId();
		// whoToSend.forSender = nInfo.getConsigneeId();
		// whoToSend.forTransporter = nInfo.getTransporterId();
		// whoToSend.unclassified = nInfo.getUnclassifiedId();
		if (notification != null) {
			if (notification.getAnswerId_1_1() == answer1) {
				if (!Misc.isUndef(notification.getNotificationId_1_1())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_1_1());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			} else if (notification.getAnswerId_1_2() == answer1) {
				if (!Misc.isUndef(notification.getNotificationId_1_2())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_1_2());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			} else if (notification.getAnswerId_1_3() == answer1) {
				if (!Misc.isUndef(notification.getNotificationId_1_3())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_1_3());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			}

			if (notification.getAnswerId_2_1() == answer2) {
				if (!Misc.isUndef(notification.getNotificationId_2_1())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_2_1());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			} else if (notification.getAnswerId_2_2() == answer2) {
				if (!Misc.isUndef(notification.getNotificationId_2_2())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_2_2());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			} else if (notification.getAnswerId_2_3() == answer2) {
				if (!Misc.isUndef(notification.getNotificationId_2_3())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_2_3());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			}

			if (notification.getAnswerId_3_1() == answer3) {
				if (!Misc.isUndef(notification.getNotificationId_3_1())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_3_1());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			} else if (notification.getAnswerId_3_2() == answer3) {
				if (!Misc.isUndef(notification.getNotificationId_3_2())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_3_2());
					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			} else if (notification.getAnswerId_3_3() == answer3) {
				if (!Misc.isUndef(notification.getNotificationId_3_3())) {
					Notification newNotification = Notification
							.getNotification(conn, notification
									.getNotificationId_3_3());

					createNotificationInfo(conn, nInfo.getVehicleId(),
							nNotificationId, newNotification.getId(), nInfo
									.getForTarget(), whoToSend);
				}
			}
		}
		return retval;
	}

	public static StringBuilder getText(Notification notification) {
		StringBuilder retval = null;
		String pattern = null;
		SimpleDateFormat sdf = new SimpleDateFormat(
				Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		String format = notification.getText();
		if (notification != null)
			retval = new StringBuilder();
		pattern = "%vehicleName";
		if (format.indexOf(pattern) >= 0) {
			String replacement = null;
			replacement = NewETAAlertHelper.helperGetCleanString(replacement);
			format = format.replaceAll(pattern, replacement);
		}

		pattern = "%location";
		if (format.indexOf(pattern) >= 0) {
			Value val = null;// CacheValue.getValueInternal(conn, vehicleId,
			// 20167, vehSetup, vdf);
			String replacement = val == null ? "N/A" : val.toString();
			replacement = NewETAAlertHelper.helperGetCleanString(replacement);
			format = format.replaceAll(pattern, replacement);
		}

		pattern = "%sentAt";
		if (format.indexOf(pattern) >= 0) {
			String replacement = "";// helperGetCleanDate(sdf,vehicleETA.getCurrFromOpStationInTime());
			format = format.replaceAll(pattern, replacement);
		}

		return retval;
	}

	/**
	 * @param args
	 * @throws GenericException
	 */
	public static void main(String[] args) throws GenericException {
		Connection conn = null;

		conn = DBConnectionPool.getConnectionFromPoolNonWeb();

		WhoToSend whoToSend = new WhoToSend();
		ArrayList<String> list = new ArrayList<String>();
		list.add("1");
		whoToSend.forCustomer = list;
		whoToSend.forSender = new ArrayList<String>();
		whoToSend.forTransporter = new ArrayList<String>();
		whoToSend.unclassified = new ArrayList<String>();
		NotificationHelper.createNotificationInfo(conn, 90, Misc.getUndefInt(),
				3, SrcDestInfo.G_TARGET_RECEIVER, whoToSend);

		DBConnectionPool.returnConnectionToPoolNonWeb(conn);

	}

	public interface NotificationStatus {
		int ACTIVE = 1;
		int INACTIVE = 2;
		int SEND = 3;
		int CLOSE = 4;
		int CANCEL = 5;
	}
}

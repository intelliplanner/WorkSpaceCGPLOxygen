package com.ipssi.mobilenotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ipssi.eta.NewETAAlertHelper.WhoToSend;
import com.ipssi.gen.utils.Misc;
import com.ipssi.mobilenotification.NotificationInfo;

/*
 saveNotificationInfo
 updateNotificationInfo
 getNotificationInfo(int notificationInfoId)
 getLatestNotificationInfo(int vehicleId)

 */

public class NotificationInfo {
	private int id;
	private int notificationId;
	private int vehicleId;
	private ArrayList<String> consigneeId;
	private ArrayList<String> consignorId;
	private ArrayList<String> transporterId;
	private ArrayList<String> unclassifiedId;
	private String text;
	private int questionsId_1;
	private int answersId_1;
	private String answerText_1;
	private int questionsId_2;
	private int answersId_2;
	private String answerText_2;
	private int questionsId_3;
	private int answersId_3;
	private String answerText_3;
	private String notes;
	private int status;
	private int forTarget;
	private int portNodeId;
	private int updatedBy;
	private int createdBy;
	private int groupNotificationId;
	private int reminderDuration_1;
	private int reminderDuration_2;
	private int reminderDuration_3;
	private long gReminderDuration;

	public static int saveNotificationInfo(Connection conn,
			NotificationInfo nInfo, String userId) {

		int retval = Misc.getUndefInt();
		PreparedStatement stmt = null;
		int index = 1;
		ResultSet rs = null;
		String insertNotification = "insert into m_user_notification_info (m_notification_id, vehicle_id,"
				+ " user_id, text ,m_questions_id_1,"
				+ "  m_questions_id_2, m_questions_id_3, notes, status,group_notification_id"
				+ ", port_node_id,g_reminder_duration) values (?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			stmt = conn.prepareStatement(insertNotification);
			Misc.setParamInt(stmt, nInfo.getNotificationId(), index++);
			Misc.setParamInt(stmt, nInfo.getVehicleId(), index++);
			stmt.setString(index++, userId);
			stmt.setString(index++, nInfo.getText());
			Misc.setParamInt(stmt, nInfo.getQuestionsId_1(), index++);
			Misc.setParamInt(stmt, nInfo.getQuestionsId_2(), index++);
			Misc.setParamInt(stmt, nInfo.getQuestionsId_3(), index++);
			stmt.setString(index++, nInfo.getNotes());
			Misc.setParamInt(stmt, nInfo.getStatus(), index++);
			Misc.setParamInt(stmt, nInfo.getGroupNotificationId(), index++);
			Misc.setParamInt(stmt, nInfo.getPortNodeId(), index++);
			Misc.setParamLong(stmt,nInfo.getGReminderDuration(),index);

			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				retval = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				Misc.closePS(stmt);
				Misc.closeResultSet(rs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retval;
	}

	public static String getStringFromList(ArrayList<String> data) {
		StringBuilder strData = new StringBuilder();
		for (String str : data) {
			strData.append(strData.length() == 0 ? str : ("," + str));
		}
		return strData.toString();
	}

	public static ArrayList<String> getListFromString(String data) {
		if (data == null || data.length() == 0) {
			return null;
		}
		ArrayList<String> dataList = new ArrayList<String>();
		String[] idArray = data.split(",");
		for (String id : idArray) {
			dataList.add(id);
		}
		return dataList;
	}

	public static int saveNotificationMetaData(Connection conn,
			int parentNotificationId, NotificationInfo nInfo) {
		int retval = Misc.getUndefInt();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String insertQuery = "insert into m_notification_info(parent_notification_id,notification_type_id,"
				+ "consignee,consignor ,transporter,unspecified,vehicle_id,for_target,m_ques_id_1,m_ques_id_2"
				+ ",m_ques_id_3) values(?,?,?,?,?,?,?,?,?,?,?)";
		try {
			stmt = conn.prepareStatement(insertQuery);
			int index = 1;
			stmt.setInt(index++, parentNotificationId);
			stmt.setInt(index++, nInfo.getNotificationId());
			stmt.setString(index++, getStringFromList(nInfo.consigneeId));
			stmt.setString(index++, getStringFromList(nInfo.consignorId));
			stmt.setString(index++, getStringFromList(nInfo.transporterId));
			stmt.setString(index++, getStringFromList(nInfo.unclassifiedId));
			stmt.setInt(index++, nInfo.getVehicleId());
			stmt.setInt(index++, nInfo.getForTarget());
			stmt.setInt(index++, nInfo.getQuestionsId_1());
			stmt.setInt(index++, nInfo.getQuestionsId_2());
			stmt.setInt(index++, nInfo.getQuestionsId_3());

			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				retval = rs.getInt(1);

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
		return retval;
	}

	public static boolean updateNotificationInfoText(Connection conn,
			NotificationInfo nInfo) throws Exception {

		boolean retval = false;
		PreparedStatement stmt = null;
		// Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
		String updNotification = "update m_user_notification_info set m_answers_id_1 = ?, m_answers_id_2 = ?, m_answers_id_3 = ?,m_answer_text_1 = ? ,m_answer_text_2 = ?,m_answer_text_3 = ?, notes = ? where id = ? ";

		stmt = conn.prepareStatement(updNotification);
		Misc.setParamInt(stmt, nInfo.getAnswersId_1(), 1);
		Misc.setParamInt(stmt, nInfo.getAnswersId_2(), 2);
		Misc.setParamInt(stmt, nInfo.getAnswersId_3(), 3);
		stmt.setString(5, nInfo.getAnswerText_1());
		stmt.setString(4, nInfo.getAnswerText_2());
		stmt.setString(6, nInfo.getAnswerText_3());
		stmt.setString(7, nInfo.getNotes());
		Misc.setParamInt(stmt, nInfo.getId(), 8);

		int count = stmt.executeUpdate();
		if (count > 0)
			retval = true;
		return retval;
	}

	public static boolean updateNotificationInfo(Connection conn, int nInfoId,
			int status) {
		int count = Misc.getUndefInt();
		boolean retval = false;
		PreparedStatement stmt = null;
		String updNotification = "update m_user_notification_info set status = ?,updated_on = ? where group_notification_id = ?";
		try {
			int id2 = getGroupNotificationId(conn, nInfoId);
			stmt = conn.prepareStatement(updNotification);
			Misc.setParamInt(stmt, status, 1);
			Misc.setParamLong(stmt,System.currentTimeMillis(), 2);
			Misc.setParamInt(stmt, id2, 3);

			count = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (count > 0)
			retval = true;
		return retval;
	}

	public static int getGroupNotificationId(Connection conn, int nInfoId) {
		int retval = Misc.getUndefInt();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String query = "select group_notification_id from m_user_notification_info where id = ?";
		try {
			stmt = conn.prepareStatement(query);
			Misc.setParamInt(stmt, nInfoId, 1);
			rs = stmt.executeQuery();
			if(rs.next()){
				retval = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return retval;
	}

	public static WhoToSend getUserList(Connection conn, int notificationId) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		WhoToSend whoToSend = new WhoToSend();
		String query = "select consignee,consignor,transporter,unspecified from m_notification_info where id = ?";
		try {
			int groupNotificationId2 = getGroupNotificationId(conn,
					notificationId);

			stmt = conn.prepareStatement(query);
			Misc.setParamInt(stmt, groupNotificationId2, 1);
			rs = stmt.executeQuery();
			rs.first();
			whoToSend.forCustomer = getListFromString(rs.getString(1));
			whoToSend.forSender = getListFromString(rs.getString(2));
			whoToSend.forTransporter = getListFromString(rs.getString(3));
			whoToSend.unclassified = getListFromString(rs.getString(4));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return whoToSend;
	}

	public static NotificationInfo getNotificationInfo(Connection conn,
			int nInfoId) throws Exception {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		String selectNotification = "select m_notification_id, vehicle_id,"
				+ "text ,m_questions_id_1, m_answers_id_1, m_questions_id_2, m_answers_id_2, m_questions_id_3, m_answers_id_3, notes, status"
				+ ", port_node_id from m_user_notification_info where id = ? ";

		stmt = conn.prepareStatement(selectNotification);
		NotificationInfo nInfo = new NotificationInfo();

		stmt.setInt(1, nInfoId);
		rs = stmt.executeQuery();

		if (rs.next()) {
			nInfo.setNotificationId(Misc.getRsetInt(rs, "m_notifications_id"));
			nInfo.setVehicleId(Misc.getRsetInt(rs, "vehicle_id"));
			nInfo.setText(rs.getString("text"));
			nInfo.setQuestionsId_1(Misc.getRsetInt(rs, "m_questions_id_1"));
			nInfo.setAnswersId_1(Misc.getRsetInt(rs, "m_answers_id_1"));
			nInfo.setQuestionsId_2(Misc.getRsetInt(rs, "m_questions_id_2"));
			nInfo.setAnswersId_2(Misc.getRsetInt(rs, "m_answers_id_2"));
			nInfo.setQuestionsId_3(Misc.getRsetInt(rs, "m_questions_id_3"));
			nInfo.setAnswersId_3(Misc.getRsetInt(rs, "m_answers_id_3"));
			nInfo.setNotes(rs.getString("notes"));
			nInfo.setStatus(Misc.getRsetInt(rs, "status"));
			nInfo.setPortNodeId(Misc.getRsetInt(rs, "port_node_id"));
		}

		Misc.closePS(stmt);
		Misc.closeResultSet(rs);
		return nInfo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getForTarget() {
		return forTarget;
	}

	public void setForTarget(int forTarget) {
		this.forTarget = forTarget;
	}

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public ArrayList<String> getConsigneeId() {
		return consigneeId;
	}

	public void setConsigneeId(ArrayList<String> consigneeId) {
		this.consigneeId = consigneeId;
	}

	public ArrayList<String> getConsignorId() {
		return consignorId;
	}

	public void setConsignorId(ArrayList<String> consignorId) {
		this.consignorId = consignorId;
	}

	public ArrayList<String> getTransporterId() {
		return transporterId;
	}

	public void setTransporterId(ArrayList<String> transporterId) {
		this.transporterId = transporterId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getQuestionsId_1() {
		return questionsId_1;
	}

	public void setQuestionsId_1(int questionsId_1) {
		this.questionsId_1 = questionsId_1;
	}

	public int getAnswersId_1() {
		return answersId_1;
	}

	public void setAnswersId_1(int answersId_1) {
		this.answersId_1 = answersId_1;
	}

	public int getQuestionsId_2() {
		return questionsId_2;
	}

	public void setQuestionsId_2(int questionsId_2) {
		this.questionsId_2 = questionsId_2;
	}

	public int getAnswersId_2() {
		return answersId_2;
	}

	public void setAnswersId_2(int answersId_2) {
		this.answersId_2 = answersId_2;
	}

	public int getQuestionsId_3() {
		return questionsId_3;
	}

	public void setQuestionsId_3(int questionsId_3) {
		this.questionsId_3 = questionsId_3;
	}

	public int getAnswersId_3() {
		return answersId_3;
	}

	public void setAnswersId_3(int answersId_3) {
		this.answersId_3 = answersId_3;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getPortNodeId() {
		return portNodeId;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public void setAnswerText_1(String answerText_1) {
		this.answerText_1 = answerText_1;
	}

	public String getAnswerText_1() {
		return answerText_1;
	}

	public void setAnswerText_2(String answerText_2) {
		this.answerText_2 = answerText_2;
	}

	public String getAnswerText_2() {
		return answerText_2;
	}

	public void setAnswerText_3(String answerText_3) {
		this.answerText_3 = answerText_3;
	}

	public String getAnswerText_3() {
		return answerText_3;
	}

	public void setUnclassifiedId(ArrayList<String> unclassifiedId) {
		this.unclassifiedId = unclassifiedId;
	}

	public ArrayList<String> getUnclassifiedId() {
		return unclassifiedId;
	}

	public void setGroupNotificationId(int notificationId) {
		this.groupNotificationId = notificationId;
	}

	public int getGroupNotificationId() {
		return groupNotificationId;
	}

	public void setReminderDuration_1(int reminderDuration_1) {
		this.reminderDuration_1 = reminderDuration_1;
	}

	public int getReminderDuration_1() {
		return reminderDuration_1;
	}

	public void setReminderDuration_2(int reminderDuration_2) {
		this.reminderDuration_2 = reminderDuration_2;
	}

	public int getReminderDuration_2() {
		return reminderDuration_2;
	}

	public void setReminderDuration_3(int reminderDuration_3) {
		this.reminderDuration_3 = reminderDuration_3;
	}

	public int getReminderDuration_3() {
		return reminderDuration_3;
	}

	public void setGReminderDuration(long gReminderDuration) {
		this.gReminderDuration = gReminderDuration;
	}

	public long getGReminderDuration() {
		return gReminderDuration;
	}

}

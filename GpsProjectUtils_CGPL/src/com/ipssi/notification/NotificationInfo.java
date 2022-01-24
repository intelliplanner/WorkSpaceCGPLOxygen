package com.ipssi.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ipssi.gen.utils.Misc;

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
	private int consigneeId;
	private int consignorId;
	private int transporterId;
	private String text;
	private int questionsId_1;
	private int answersId_1;
	private int questionsId_2;
	private int answersId_2;
	private int questionsId_3;
	private int answersId_3;
	private String notes;
	private int status;
	private int portNodeId;
	private int updatedBy;
	private int createdBy;
	
	public static int saveNotificationInfo(Connection conn, NotificationInfo nInfo) throws Exception {
		
		int retval = Misc.getUndefInt();
		PreparedStatement stmt=null;
		ResultSet rs=null;
			String insertNotification = "insert into m_notification_info (m_notifications_id, vehicle_id, consignee_id, consignor_id, transporter_id" +
					", text ,m_questions_id_1, m_answers_id_1, m_questions_id_2, m_answers_id_2, m_questions_id_3, m_answers_id_3, notes, status" +
					", port_node_id) " +
					"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
			stmt = conn.prepareStatement(insertNotification);
			Misc.setParamInt(stmt, nInfo.getNotificationId(),1);
			Misc.setParamInt(stmt, nInfo.getVehicleId(),2);
			Misc.setParamInt(stmt, nInfo.getConsigneeId(),3);
			Misc.setParamInt(stmt, nInfo.getConsignorId(),4);
			Misc.setParamInt(stmt, nInfo.getTransporterId(),5);
			stmt.setString(6, nInfo.getText());
			Misc.setParamInt(stmt, nInfo.getQuestionsId_1(),7);
			Misc.setParamInt(stmt, nInfo.getAnswersId_1(),8);
			Misc.setParamInt(stmt, nInfo.getQuestionsId_2(),9);
			Misc.setParamInt(stmt, nInfo.getAnswersId_2(),10);
			Misc.setParamInt(stmt, nInfo.getQuestionsId_3(),11);
			Misc.setParamInt(stmt, nInfo.getAnswersId_3(),12);
			stmt.setString(13, nInfo.getNotes());
			Misc.setParamInt(stmt, nInfo.getStatus(),14);
			Misc.setParamInt(stmt, nInfo.getPortNodeId(),15);
			
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()){
				retval = rs.getInt(1);
			}
			Misc.closePS(stmt);
			Misc.closeResultSet(rs);
		return retval;
	}
	
	public static boolean updateNotificationInfoText(Connection conn, NotificationInfo nInfo) throws Exception {

		boolean retval = false;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		//Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
		String updNotification = "update m_notification_info set m_answers_id_1 = ?, m_answers_id_2 = ?, notes = ? where id = ? ";

		stmt = conn.prepareStatement(updNotification);
		Misc.setParamInt(stmt, nInfo.getAnswersId_1(),1);
		Misc.setParamInt(stmt, nInfo.getAnswersId_2(),2);
		stmt.setString(3, nInfo.getNotes());
		Misc.setParamInt(stmt, nInfo.getId() ,4);
		
		int count = stmt.executeUpdate();
		if(count > 0)
			retval = true;
		return retval;
	}
	
	public static boolean updateNotificationInfo(Connection conn, int nInfoId, int status) throws Exception {

		boolean retval = false;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		//Timestamp sysDate = new Timestamp((new java.util.Date()).getTime());
		String updNotification = "update m_notification_info set status = ? where id = ? ";

		stmt = conn.prepareStatement(updNotification);
		Misc.setParamInt(stmt, status,1);
		Misc.setParamInt(stmt, nInfoId,2);

		int count = stmt.executeUpdate();
		if(count > 0)
			retval = true;
		return retval;
	}
	
public static NotificationInfo getNotificationInfo(Connection conn, int nInfoId) throws Exception {
		
	NotificationInfo retval = null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
			String selectNotification = "select m_notifications_id, vehicle_id, consignee_id, consignor_id, transporter_id" +
					", text ,m_questions_id_1, m_answers_id_1, m_questions_id_2, m_answers_id_2, m_questions_id_3, m_answers_id_3, notes, status" +
					", port_node_id from m_notification_info where id = ? ";
			
			stmt = conn.prepareStatement(selectNotification);
			NotificationInfo nInfo = new NotificationInfo();
			
			
			stmt.setInt(1, nInfoId);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				nInfo.setNotificationId(Misc.getRsetInt(rs, "m_notifications_id"));
				nInfo.setVehicleId(Misc.getRsetInt(rs, "vehicle_id"));
				nInfo.setConsigneeId(Misc.getRsetInt(rs, "consignee_id"));
				nInfo.setConsignorId(Misc.getRsetInt(rs, "consignor_id"));
				nInfo.setTransporterId(Misc.getRsetInt(rs, "transporter_id"));
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

	public int getConsigneeId() {
		return consigneeId;
	}

	public void setConsigneeId(int consigneeId) {
		this.consigneeId = consigneeId;
	}

	public int getConsignorId() {
		return consignorId;
	}

	public void setConsignorId(int consignorId) {
		this.consignorId = consignorId;
	}

	public int getTransporterId() {
		return transporterId;
	}

	public void setTransporterId(int transporterId) {
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
	
	
	
}

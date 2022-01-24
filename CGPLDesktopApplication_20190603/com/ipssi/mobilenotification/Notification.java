package com.ipssi.mobilenotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;

public class Notification {
	private int id;
	private String name;
	private String text;
	private int forConsignee;
	private int forConsignor;
	private int forTransporter;

	private int questionsId_1;
	private int answerId_1_1;
	private int notificationId_1_1;
	private int reminderDuration_1_1;
	private int answerId_1_2;
	private int notificationId_1_2;
	private int reminderDuration_1_2;
	private int answerId_1_3;
	private int notificationId_1_3;
	private int reminderDuration_1_3;

	private int questionsId_2;
	private int answerId_2_1;
	private int notificationId_2_1;
	private int reminderDuration_2_1;
	private int answerId_2_2;
	private int notificationId_2_2;
	private int reminderDuration_2_2;
	private int answerId_2_3;
	private int notificationId_2_3;
	private int reminderDuration_2_3;

	private int questionsId_3;
	private int answerId_3_1;
	private int notificationId_3_1;
	private int reminderDuration_3_1;
	private int answerId_3_2;
	private int notificationId_3_2;
	private int reminderDuration_3_2;
	private int answerId_3_3;
	private int notificationId_3_3;
	private int reminderDuration_3_3;

	private String notes;
	private int status;
	private int portNodeId;
	private long activeFrom;
	private long activeTo;
	private long gReminderDuration;
	ArrayList<Question> questList = new ArrayList<Question>();

	// **use read write lock to improve
	private static ConcurrentHashMap<Integer, Notification> notificationCache = new ConcurrentHashMap<Integer, Notification>();
	private static volatile boolean notificationInitDone = false;

	public static Notification getNotification(Connection conn,
			int notificationId) {
		try {
			if (!notificationInitDone) {
				load(conn);
			}
			return notificationCache.get(notificationId);
		} catch (Exception e) {
			e.printStackTrace();
			// eat it
		}
		return null;
	}

	private static synchronized void load(Connection conn) throws Exception {
		try {
			String q = "select mn.id,mn.name,mn.text,mn.for_consignee,mn.for_consignor,"
					+ "mn.for_transporter,mn.questions_id_1,mn.answer_id_1_1,mn.notification_id_1_1,"
					+ "mn.reminder_duration_1_1,mn.answer_id_1_2,mn.notification_id_1_2,"
					+ "mn.reminder_duration_1_2,mn.answer_id_1_3,mn.notification_id_1_3,"
					+ "mn.reminder_duration_1_3,mn.questions_id_2,mn.answer_id_2_1,mn.g_reminder_duration"
					+ "mn.notification_id_2_1,mn.reminder_duration_2_1,mn.answer_id_2_2,"
					+ "mn.notification_id_2_2,mn.reminder_duration_2_2,mn.answer_id_2_3,"
					+ "mn.notification_id_2_3,mn.reminder_duration_2_3,mn.questions_id_3,"
					+ "mn.answer_id_3_1,mn.notification_id_3_1,mn.reminder_duration_3_1,"
					+ "mn.answer_id_3_2,mn.notification_id_3_2,mn.reminder_duration_3_2, "
					+ "mn.answer_id_3_3,mn.notification_id_3_3,mn.reminder_duration_3_3,"
					+ "mn.notes,mn.status, mn.active_from,mn.active_to,"
					+"mq1.id q_id_1,mq1.text q_text_1,mq1.answer_type q_type_1,"
					+ "mq2.id q_id_2,mq2.text q_text_2,mq2.answer_type q_type_2,"
					+ "mq3.id q_id_3,mq3.text q_text_3,mq3.answer_type q_type_3 from m_notifications mn"
					+ " left outer join m_questions mq1 on(mn.questions_id_1 = mq1.id)"
					+ "	left outer join m_questions mq2 on(mn.questions_id_2 = mq2.id) "
					+ "	left outer join m_questions mq3 on(mn.questions_id_3 = mq3.id) where"
					+ "	mn.status = 1 and mq1.status =1 and mq2.status =1 and mq3.status =1";

			System.out.println("[MNotification] "
					+ Thread.currentThread().getId()
					+ " Building MNotification Cache : " + q);
			PreparedStatement ps = conn.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			// select ctp.vehicle_id, tp_record.tpr_id, tp_record.tpr_status,
			// tp_record.mines_id, mines_details.name mines_name,
			// tp_record.combo_start, tp_record.challan_date,
			// tp_record.combo_end, tp_record.material_cat,
			// tp_record.earliest_load_gate_in,
			// tp_record.earliest_load_gate_out,
			// tp_record.earliest_unload_gate_in,
			// tp_record.earliest_unload_gate_out, tp_record.challan_date,
			// tp_record.rf_challan_date
			int count = 0;

			while (rs.next()) {
				Notification notification = new Notification();
				notification.setId(Misc.getRsetInt(rs, "id"));
				notification.setName(rs.getString("name"));
				notification.setText(rs.getString("text"));
				notification.setForConsignee(Misc.getRsetInt(rs,
						"for_consignee"));
				notification.setForConsignor(Misc.getRsetInt(rs,
						"for_consignor"));
				notification.setForTransporter(Misc.getRsetInt(rs,
						"for_transporter"));
				notification.setGReminderDuration(Misc.getRsetLong(rs, "g_reminder_duration"));
				notification.setQuestionsId_1(Misc.getRsetInt(rs,
						"questions_id_1"));
				notification.setAnswerId_1_1(Misc.getRsetInt(rs,
						"answer_id_1_1"));
				notification.setNotificationId_1_1(Misc.getRsetInt(rs,
						"notification_id_1_1"));
				notification.setReminderDuration_1_1(Misc.getRsetInt(rs,
						"reminder_duration_1_1"));
				notification.setAnswerId_1_2(Misc.getRsetInt(rs,
						"answer_id_1_2"));
				notification.setNotificationId_1_2(Misc.getRsetInt(rs,
						"notification_id_1_2"));
				notification.setReminderDuration_1_2(Misc.getRsetInt(rs,
						"reminder_duration_1_2"));
				notification.setAnswerId_1_3(Misc.getRsetInt(rs,
						"answer_id_1_3"));
				notification.setNotificationId_1_3(Misc.getRsetInt(rs,
						"notification_id_1_3"));
				notification.setReminderDuration_1_3(Misc.getRsetInt(rs,
						"reminder_duration_1_3"));
				notification.setQuestionsId_2(Misc.getRsetInt(rs,
						"questions_id_2"));
				notification.setAnswerId_2_1(Misc.getRsetInt(rs,
						"answer_id_2_1"));
				notification.setNotificationId_2_1(Misc.getRsetInt(rs,
						"notification_id_2_1"));
				notification.setReminderDuration_2_1(Misc.getRsetInt(rs,
						"reminder_duration_2_1"));
				notification.setAnswerId_2_2(Misc.getRsetInt(rs,
						"answer_id_2_2"));
				notification.setNotificationId_2_2(Misc.getRsetInt(rs,
						"notification_id_2_2"));
				notification.setReminderDuration_2_2(Misc.getRsetInt(rs,
						"reminder_duration_2_2"));
				notification.setAnswerId_2_3(Misc.getRsetInt(rs,
						"answer_id_2_3"));
				notification.setNotificationId_2_3(Misc.getRsetInt(rs,
						"notification_id_2_3"));
				notification.setReminderDuration_2_3(Misc.getRsetInt(rs,
						"reminder_duration_2_3"));
				notification.setNotes(rs.getString("notes"));
				notification.setStatus(Misc.getRsetInt(rs, "status"));

				Question qust1 = new Question();
				qust1.id = Misc.getRsetInt(rs, "q_id_1");
				qust1.text = rs.getString("q_text_1");
				qust1.answerType = AnswerEnum.getAnswerType(Misc.getRsetInt(rs,
						"q_type_1"));
				notification.getQuestList().add(qust1);

				Question qust2 = new Question();
				qust2.id = Misc.getRsetInt(rs, "q_id_2");
				qust2.text = rs.getString("q_text_2");
				qust2.answerType = AnswerEnum.getAnswerType(Misc.getRsetInt(rs,
						"q_type_2"));
				notification.getQuestList().add(qust2);

				Question qust3 = new Question();
				qust3.id = Misc.getRsetInt(rs, "q_id_3");
				qust3.text = rs.getString("q_text_3");
				qust3.answerType = AnswerEnum.getAnswerType(Misc.getRsetInt(rs,
						"q_type_3"));
				notification.getQuestList().add(qust3);

				notificationCache.put(notification.getId(), notification);
				System.out.println(notification.toString());
				count++;
			}
			System.out.println("[MNotification] "
					+ Thread.currentThread().getId()
					+ " Done Building MNotification Cache : count= " + count);
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			notificationInitDone = true;

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static class Question {
		private int id;
		private String text;
		private String notes;
		private AnswerEnum answerType;
		private int status;
		private int portNodeId;
	}

	public static enum AnswerEnum {
		YESNO, YESNOTEXT, TEXT;

		public static AnswerEnum getAnswerType(String type) {

			if (AnswerEnum.YESNO.toString().equalsIgnoreCase(type))
				return YESNO;
			else if (AnswerEnum.YESNOTEXT.toString().equalsIgnoreCase(type))
				return YESNOTEXT;
			else if (AnswerEnum.TEXT.toString().equalsIgnoreCase(type))
				return TEXT;
			else
				return TEXT;
		}

		public static AnswerEnum getAnswerType(int type) {
			if (type == 0)
				return YESNO;
			else if (type == 1)
				return YESNOTEXT;
			else if (type == 2)
				return TEXT;
			else
				return TEXT;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getForConsignee() {
		return forConsignee;
	}

	public void setForConsignee(int forConsignee) {
		this.forConsignee = forConsignee;
	}

	public int getForConsignor() {
		return forConsignor;
	}

	public void setForConsignor(int forConsignor) {
		this.forConsignor = forConsignor;
	}

	public int getForTransporter() {
		return forTransporter;
	}

	public void setForTransporter(int forTransporter) {
		this.forTransporter = forTransporter;
	}

	public int getQuestionsId_1() {
		return questionsId_1;
	}

	public void setQuestionsId_1(int questionsId_1) {
		this.questionsId_1 = questionsId_1;
	}

	public int getAnswerId_1_1() {
		return answerId_1_1;
	}

	public void setAnswerId_1_1(int answerId_1_1) {
		this.answerId_1_1 = answerId_1_1;
	}

	public int getNotificationId_1_1() {
		return notificationId_1_1;
	}

	public void setNotificationId_1_1(int notificationId_1_1) {
		this.notificationId_1_1 = notificationId_1_1;
	}

	public int getReminderDuration_1_1() {
		return reminderDuration_1_1;
	}

	public void setReminderDuration_1_1(int reminderDuration_1_1) {
		this.reminderDuration_1_1 = reminderDuration_1_1;
	}

	public int getAnswerId_1_2() {
		return answerId_1_2;
	}

	public void setAnswerId_1_2(int answerId_1_2) {
		this.answerId_1_2 = answerId_1_2;
	}

	public int getNotificationId_1_2() {
		return notificationId_1_2;
	}

	public void setNotificationId_1_2(int notificationId_1_2) {
		this.notificationId_1_2 = notificationId_1_2;
	}

	public int getReminderDuration_1_2() {
		return reminderDuration_1_2;
	}

	public void setReminderDuration_1_2(int reminderDuration_1_2) {
		this.reminderDuration_1_2 = reminderDuration_1_2;
	}

	public int getAnswerId_1_3() {
		return answerId_1_3;
	}

	public void setAnswerId_1_3(int answerId_1_3) {
		this.answerId_1_3 = answerId_1_3;
	}

	public int getNotificationId_1_3() {
		return notificationId_1_3;
	}

	public void setNotificationId_1_3(int notificationId_1_3) {
		this.notificationId_1_3 = notificationId_1_3;
	}

	public int getReminderDuration_1_3() {
		return reminderDuration_1_3;
	}

	public void setReminderDuration_1_3(int reminderDuration_1_3) {
		this.reminderDuration_1_3 = reminderDuration_1_3;
	}

	public int getQuestionsId_2() {
		return questionsId_2;
	}

	public void setQuestionsId_2(int questionsId_2) {
		this.questionsId_2 = questionsId_2;
	}

	public int getAnswerId_2_1() {
		return answerId_2_1;
	}

	public void setAnswerId_2_1(int answerId_2_1) {
		this.answerId_2_1 = answerId_2_1;
	}

	public int getNotificationId_2_1() {
		return notificationId_2_1;
	}

	public void setNotificationId_2_1(int notificationId_2_1) {
		this.notificationId_2_1 = notificationId_2_1;
	}

	public int getReminderDuration_2_1() {
		return reminderDuration_2_1;
	}

	public void setReminderDuration_2_1(int reminderDuration_2_1) {
		this.reminderDuration_2_1 = reminderDuration_2_1;
	}

	public int getAnswerId_2_2() {
		return answerId_2_2;
	}

	public void setAnswerId_2_2(int answerId_2_2) {
		this.answerId_2_2 = answerId_2_2;
	}

	public int getNotificationId_2_2() {
		return notificationId_2_2;
	}

	public void setNotificationId_2_2(int notificationId_2_2) {
		this.notificationId_2_2 = notificationId_2_2;
	}

	public int getReminderDuration_2_2() {
		return reminderDuration_2_2;
	}

	public void setReminderDuration_2_2(int reminderDuration_2_2) {
		this.reminderDuration_2_2 = reminderDuration_2_2;
	}

	public int getAnswerId_2_3() {
		return answerId_2_3;
	}

	public void setAnswerId_2_3(int answerId_2_3) {
		this.answerId_2_3 = answerId_2_3;
	}

	public int getNotificationId_2_3() {
		return notificationId_2_3;
	}

	public void setNotificationId_2_3(int notificationId_2_3) {
		this.notificationId_2_3 = notificationId_2_3;
	}

	public int getReminderDuration_2_3() {
		return reminderDuration_2_3;
	}

	public void setReminderDuration_2_3(int reminderDuration_2_3) {
		this.reminderDuration_2_3 = reminderDuration_2_3;
	}

	public int getQuestionsId_3() {
		return questionsId_3;
	}

	public void setQuestionsId_3(int questionsId_3) {
		this.questionsId_3 = questionsId_3;
	}

	public int getAnswerId_3_1() {
		return answerId_3_1;
	}

	public void setAnswerId_3_1(int answerId_3_1) {
		this.answerId_3_1 = answerId_3_1;
	}

	public int getNotificationId_3_1() {
		return notificationId_3_1;
	}

	public void setNotificationId_3_1(int notificationId_3_1) {
		this.notificationId_3_1 = notificationId_3_1;
	}

	public int getReminderDuration_3_1() {
		return reminderDuration_3_1;
	}

	public void setReminderDuration_3_1(int reminderDuration_3_1) {
		this.reminderDuration_3_1 = reminderDuration_3_1;
	}

	public int getAnswerId_3_2() {
		return answerId_3_2;
	}

	public void setAnswerId_3_2(int answerId_3_2) {
		this.answerId_3_2 = answerId_3_2;
	}

	public int getNotificationId_3_2() {
		return notificationId_3_2;
	}

	public void setNotificationId_3_2(int notificationId_3_2) {
		this.notificationId_3_2 = notificationId_3_2;
	}

	public int getReminderDuration_3_2() {
		return reminderDuration_3_2;
	}

	public void setReminderDuration_3_2(int reminderDuration_3_2) {
		this.reminderDuration_3_2 = reminderDuration_3_2;
	}

	public int getAnswerId_3_3() {
		return answerId_3_3;
	}

	public void setAnswerId_3_3(int answerId_3_3) {
		this.answerId_3_3 = answerId_3_3;
	}

	public int getNotificationId_3_3() {
		return notificationId_3_3;
	}

	public void setNotificationId_3_3(int notificationId_3_3) {
		this.notificationId_3_3 = notificationId_3_3;
	}

	public int getReminderDuration_3_3() {
		return reminderDuration_3_3;
	}

	public void setReminderDuration_3_3(int reminderDuration_3_3) {
		this.reminderDuration_3_3 = reminderDuration_3_3;
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

	public long getActiveFrom() {
		return activeFrom;
	}

	public void setActiveFrom(long activeFrom) {
		this.activeFrom = activeFrom;
	}

	public long getActiveTo() {
		return activeTo;
	}

	public void setActiveTo(long activeTo) {
		this.activeTo = activeTo;
	}

	public ArrayList<Question> getQuestList() {
		return questList;
	}

	public void setQuestList(ArrayList<Question> questList) {
		this.questList = questList;
	}

	public static ConcurrentHashMap<Integer, Notification> getNotificationCache() {
		return notificationCache;
	}

	public static void setNotificationCache(
			ConcurrentHashMap<Integer, Notification> notificationCache) {
		Notification.notificationCache = notificationCache;
	}

	public static boolean isNotificationInitDone() {
		return notificationInitDone;
	}

	public static void setNotificationInitDone(boolean notificationInitDone) {
		Notification.notificationInitDone = notificationInitDone;
	}

	public void setGReminderDuration(long gReminderDuration) {
		this.gReminderDuration = gReminderDuration;
	}

	public long getGReminderDuration() {
		return gReminderDuration;
	}

}

package com.ipssi.mobilenotification;

public class NotificationBean {
	private int id;
	private int userId;
	private int notificationId;
	private String vehicleName;
	private String notificationText;
	private int questionsId_1;
	private String questionText_1;
	private int answerType_1;
	private int questionsId_2;
	private String questionText_2;
	private int answerType_2;
	private int questionsId_3;
	private String questionText_3;
	private int answerType_3;
	private String notes;
	private long gReminderDuration;
	
	public int getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getNotificationText() {
		return notificationText;
	}
	public void setNotificationText(String notificationText) {
		this.notificationText = notificationText;
	}
	public int getQuestionsId_1() {
		return questionsId_1;
	}
	public void setQuestionsId_1(int questionsId_1) {
		this.questionsId_1 = questionsId_1;
	}
	public String getQuestionText_1() {
		return questionText_1;
	}
	public void setQuestionText_1(String questionText_1) {
		this.questionText_1 = questionText_1;
	}
	public int getAnswerType_1() {
		return answerType_1;
	}
	public void setAnswerType_1(int answerType_1) {
		this.answerType_1 = answerType_1;
	}
	public int getQuestionsId_2() {
		return questionsId_2;
	}
	public void setQuestionsId_2(int questionsId_2) {
		this.questionsId_2 = questionsId_2;
	}
	public String getQuestionText_2() {
		return questionText_2;
	}
	public void setQuestionText_2(String questionText_2) {
		this.questionText_2 = questionText_2;
	}
	public int getAnswerType_2() {
		return answerType_2;
	}
	public void setAnswerType_2(int answerType_2) {
		this.answerType_2 = answerType_2;
	}
	public int getQuestionsId_3() {
		return questionsId_3;
	}
	public void setQuestionsId_3(int questionsId_3) {
		this.questionsId_3 = questionsId_3;
	}
	public String getQuestionText_3() {
		return questionText_3;
	}
	public void setQuestionText_3(String questionText_3) {
		this.questionText_3 = questionText_3;
	}
	public int getAnswerType_3() {
		return answerType_3;
	}
	public void setAnswerType_3(int answerType_3) {
		this.answerType_3 = answerType_3;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getUserId() {
		return userId;
	}
	public void setGReminderDuration(long gReminderDuration) {
		this.gReminderDuration = gReminderDuration;
	}
	public long getGReminderDuration() {
		return gReminderDuration;
	}
}

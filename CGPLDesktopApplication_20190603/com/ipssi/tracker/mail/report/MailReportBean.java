package com.ipssi.tracker.mail.report;

public class MailReportBean {
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public int getMailSize() {
		return mailSize;
	}
	public void setMailSize(int mailSize) {
		this.mailSize = mailSize;
	}
	public int getSchedule() {
		return schedule;
	}
	public void setSchedule(int schedule) {
		this.schedule = schedule;
	}
	public int getScheduleHour() {
		return scheduleHour;
	}
	public void setScheduleHour(int scheduleHour) {
		this.scheduleHour = scheduleHour;
	}
	public int getScheduleDay() {
		return scheduleDay;
	}
	public void setScheduleDay(int scheduleDay) {
		this.scheduleDay = scheduleDay;
	}
	String name ;
	int type;
	String user;
	int mailSize;
	int schedule;
	int scheduleHour;
	int scheduleDay;
	int custId;
	int contactId;
	String [] userMail;
	int report ;
	int serchOption;
	String subject;
	String searchXml;
	int margeMail;
	int mailReportId;
	
	public int getMailReportId() {
		return mailReportId;
	}
	public void setMailReportId(int mailReportId) {
		this.mailReportId = mailReportId;
	}
	public int getMargeMail() {
		return margeMail;
	}
	public void setMargeMail(int margeMail) {
		this.margeMail = margeMail;
	}
	public String getSearchXml() {
		return searchXml;
	}
	public void setSearchXml(String searchXml) {
		this.searchXml = searchXml;
	}
	public int getReport() {
		return report;
	}
	public void setReport(int report) {
		this.report = report;
	}
	public int getSerchOption() {
		return serchOption;
	}
	public void setSerchOption(int serchOption) {
		this.serchOption = serchOption;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String[] getUserMail() {
		return userMail;
	}
	public void setUserMail(String[] userMail) {
		this.userMail = userMail;
	}
	public int getContactId() {
		return contactId;
	}
	public void setContactId(int contactId) {
		this.contactId = contactId;
	}
	public int getCustId() {
		return custId;
	}
	public void setCustId(int custID) {
		this.custId = custID;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	String custName;

	
}

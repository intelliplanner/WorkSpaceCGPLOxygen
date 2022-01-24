package com.ipssi.cbse.reports;

import java.util.ArrayList;
import java.util.Date;

public class CBSEReportBean {
	private String centerCode;
	private String classCode;
	private String examCode;
	private String examName;
    private String centre;

	private ArrayList<CBSEReportDetailBean> detailData;
	private int registeredStudent;
	private int scanStudent;
	private int scanAnswersheet;
	private String state;
	private Date startDate;
	private Date endDate;
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getRegisteredStudent() {
		return registeredStudent;
	}
	public void setRegisteredStudent(int registeredStudent) {
		this.registeredStudent = registeredStudent;
	}
	public int getScanStudent() {
		return scanStudent;
	}
	public void setScanStudent(int scanStudent) {
		this.scanStudent = scanStudent;
	}
	public int getScanAnswersheet() {
		return scanAnswersheet;
	}
	public void setScanAnswersheet(int scanAnswersheet) {
		this.scanAnswersheet = scanAnswersheet;
	}
	public String getCenterCode() {
		return centerCode;
	}
	public void setCenterCode(String centerCode) {
		this.centerCode = centerCode;
	}
	public String getClassCode() {
		return classCode;
	}
	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}
	public String getExamCode() {
		return examCode;
	}
	public void setExamCode(String examCode) {
		this.examCode = examCode;
	}
	public ArrayList<CBSEReportDetailBean> getDetailData() {
		return detailData;
	}
	public void setDetailData(ArrayList<CBSEReportDetailBean> detailData) {
		this.detailData = detailData;
	}
	public String getCentre() {
		return centre;
	}
	public void setCentre(String centre) {
		this.centre = centre;
	}
	public String getExamName() {
		return examName;
	}
	public void setExamName(String examName) {
		this.examName = examName;
	}
	
}
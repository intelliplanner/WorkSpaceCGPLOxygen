package com.ipssi.reporting.mail;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class AutoEmailBean {
	private int mailId = Misc.getUndefInt();
	private int reportId;
	private int groupId;
	private String reportName;
	private String subject;
	private int reportFormat;
	private ArrayList<String> recipient = new ArrayList<String>();
	private int status;
	private long createDate;
	private long scheduleDate;
	private long sendingDate;
	private int granularity;
	private int frequencyId;
	private int noData;
	private OrgMailingBean orgMailingBean;
	
	public void setMailId(int mailId){
		this.mailId = mailId;	
	}
	public int getMailId(){
		return this.mailId;
	}
	public void setGroupId(int groupId){
		this.groupId = groupId;	
	}
	public int getGroupId(){
		return this.groupId;
	}
	public void setReportId(int reportId){
		this.reportId = reportId;	
	}
	public int getReportId(){
		return this.reportId;
	}
	public void setReportName(String reportName){
		this.reportName = reportName;	
	}
	public String getReportName(){
		return this.reportName;
	}
	public void setSubject(String subject){
		this.subject = subject;	
	}
	public String getSubject(){
		return this.subject;
	}
	public void setReportFormat(int reportFormat){
		this.reportFormat = reportFormat;	
	}
	public int getReportFormat(){
		return this.reportFormat;
	}
	public void setRecipient(ArrayList<String> recipient){
		this.recipient = recipient;	
	}
	public ArrayList<String> getRecipient(){
		return this.recipient;
	}
	public void setStatus(int status){
		this.status = status;	
	}
	public int getStatus(){
		return this.status;
	}
	public void setSendingDate(long sendingDate){
		this.sendingDate = sendingDate;	
	}
	public long getSendingDate(){
		return this.sendingDate;
	}
	public void setScheduleDate(long scheduleDate){
		this.scheduleDate = scheduleDate;	
	}
	public long getScheduleDate(){
		return this.scheduleDate;
	}
	public void setCreateDate(long createDate){
		this.createDate = createDate;	
	}
	public long getCreateDate(){
		return this.createDate;
	}
	public void setGranularity(int granularity){
		this.granularity = granularity;	
	}
	public int getGranularity(){
		return this.granularity;
	}
	public void setFrequencyId(int frequencyId){
		this.frequencyId = frequencyId;	
	}
	public int getFrequencyId(){
		return this.frequencyId;
	}
	public void setNoData(int noData){
		this.noData = noData;	
	}
	public int getNoData(){
		return this.noData;
	}
	public void setOrgMailingBean(OrgMailingBean orgMailingBean){
		this.orgMailingBean = orgMailingBean;	
	}
	public OrgMailingBean getOrgMailingBean(){
		return this.orgMailingBean;
	}
	public String getRecipientStr(){
		StringBuilder sb = null;
		if(recipient == null || recipient.size() <= 0)
			return null;
		sb = new StringBuilder();
		for(String user : recipient){
			sb.append(user);
		}
		return sb == null ? null : sb.toString();
	}
}

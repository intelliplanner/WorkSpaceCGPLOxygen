package com.ipssi.reporting.email;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.shift.ShiftBean;
import com.ipssi.shift.ShiftInformation;

public class EmailSchedulerInformation {

	public static ArrayList<EmailInfo> getEmailForSending(Connection conn){
		ArrayList<EmailStatusDetail> latestEmailStatus = null;
		ArrayList<EmailInfo> scheduledEmailList = null;
		try{
			EmailSchedulerDao dao = new EmailSchedulerDao();
			latestEmailStatus = dao.fetchLatestEmailStatus(conn);
			scheduledEmailList = dao.fetchEmailScheduleInfo(conn);
			long currentTimeStamp = System.currentTimeMillis();
			for(int k=0,ks=scheduledEmailList == null ? 0 : scheduledEmailList.size();k<ks;k++){//k,ks changes inside loop
				boolean removeIt = false;
				EmailInfo emailInfo = scheduledEmailList.get(k);
				System.out.println("[AMSTT] EMAIL("+emailInfo.getId()+"):"+emailInfo.isValidEmailInfo(conn,emailInfo.getPortNodeId())+","+emailInfo.getFrequency().getScheduledDate(conn,emailInfo.getPortNodeId()));
				if(!emailInfo.isValidEmailInfo(conn,emailInfo.getPortNodeId()) || currentTimeStamp < emailInfo.getFrequency().getScheduledDate(conn,emailInfo.getPortNodeId())){
					removeIt = true;
				}else{
					for(int i=0,is=latestEmailStatus == null? 0:latestEmailStatus.size();i<is;i++){
						EmailStatusDetail statusBean = latestEmailStatus.get(i);
						long lastScheduledDate = statusBean.getScheduledDate();
						long lastSendingStatus = statusBean.getStatus();	
						if( emailInfo.getId() != statusBean.getEmailInfoId())
							continue;
						System.out.println("[AMSTT]Last EMAIL Status("+statusBean.getId()+"):"+","+lastScheduledDate+","+lastSendingStatus+","+emailInfo.getFrequency().getGranularityTotalTime(conn, emailInfo.getPortNodeId()));
						if(!Misc.isUndef(lastScheduledDate) && lastSendingStatus >= EmailStatusDetail.STATUS_SEND_FAILED && !emailInfo.getFrequency().isValidFreq(conn,emailInfo.getPortNodeId(),lastScheduledDate)){
							removeIt = true;
						}
						break;
					}
				}
				if(removeIt){
					System.out.println("[AMSTT] Remove Email("+emailInfo.getId()+"):");
					scheduledEmailList.remove(k--);
					ks--;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return scheduledEmailList;
	}
	public static class EmailInfo{
		private int id;
		private Report report;
		private ReportSpan reportSpan;
		private EmailGroup group;
		private Frequency frequency;
		private int orgMailingId;
		private boolean sendNoData;
		private int reportFormat;
		private ExcelTemplate template;
		private String subject;
		private String body;
		private long scheduledAt;
		private long sendAt;
		private int deliveryStatus;
		private int status;
		private int portNodeId;
		
		public int getPortNodeId() {
			return portNodeId;
		}

		public void setPortNodeId(int portNodeId) {
			this.portNodeId = portNodeId;
		}

		public EmailGroup getGroup() {
			return group;
		}

		public void setGroup(EmailGroup group) {
			this.group = group;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public boolean isValidEmailInfo(Connection conn,int portNodeId){
			return !( report == null || Misc.isUndef(report.getId()) || Misc.isUndef(report.getMenuId()) 
					|| frequency == null || Misc.isUndef(frequency.getScheduledDate(conn,portNodeId)) 
					|| group == null || group.getEmailUserList() == null || group.getEmailUserList().size() == 0 );
		}

		public void setTemplate(ExcelTemplate template) {
			this.template = template;
		}
		public Report getReport() {
			return report;
		}
		public Frequency getFrequency() {
			return frequency;
		}
		public int getOrgMailingId() {
			return orgMailingId;
		}
		public boolean isSendNoData() {
			return sendNoData;
		}
		public int getReportFormat() {
			return reportFormat;
		}
		public ExcelTemplate getTemplate() {
			return template;
		}
		public String getSubject() {
			return subject;
		}
		public String getBody() {
			return body;
		}
		public void setReport(Report report) {
			this.report = report;
		}
		public void setFrequency(Frequency frequency) {
			this.frequency = frequency;
		}
		public void setOrgMailingId(int orgMailingId) {
			this.orgMailingId = orgMailingId;
		}
		public void setSendNoData(boolean sendNoData) {
			this.sendNoData = sendNoData;
		}
		public void setReportFormat(int reportFormat) {
			this.reportFormat = reportFormat;
		}
		public void setTemplateId(ExcelTemplate template) {
			this.template = template;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public void setBody(String body) {
			this.body = body;
		}
		public long getScheduledAt() {
			return scheduledAt;
		}
		public long getSendAt() {
			return sendAt;
		}
		public int getDeliveryStatus() {
			return deliveryStatus;
		}
		public void setScheduledAt(long scheduledAt) {
			this.scheduledAt = scheduledAt;
		}
		public void setSendAt(long sendAt) {
			this.sendAt = sendAt;
		}
		public void setDeliveryStatus(int deliveryStatus) {
			this.deliveryStatus = deliveryStatus;
		}
		public ReportSpan getReportSpan() {
			return reportSpan;
		}
		public void setReportSpan(ReportSpan reportSpan) {
			this.reportSpan = reportSpan;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}

	}
	public static class Report{
		private int id;
		private String name;
		private String pageContext;
		private String generator;
		private int userId;
		private int portNodeId;
		private int status;
		private int type;
		private int menuId;
		private String help;
		public int getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public String getPageContext() {
			return pageContext;
		}
		public String getGenerator() {
			return generator;
		}
		public int getUserId() {
			return userId;
		}
		public int getPortNodeId() {
			return portNodeId;
		}
		public int getStatus() {
			return status;
		}
		public int getType() {
			return type;
		}
		public int getMenuId() {
			return menuId;
		}
		public String getHelp() {
			return help;
		}
		public void setId(int id) {
			this.id = id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setPageContext(String pageContext) {
			this.pageContext = pageContext;
		}
		public void setGenerator(String generator) {
			this.generator = generator;
		}
		public void setUserId(int userId) {
			this.userId = userId;
		}
		public void setPortNodeId(int portNodeId) {
			this.portNodeId = portNodeId;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public void setType(int type) {
			this.type = type;
		}
		public void setMenuId(int menuId) {
			this.menuId = menuId;
		}
		public void setHelp(String help) {
			this.help = help;
		}

		public Report(int id, String pageContext,String name, int status, int portNodeId, int userId,
				int type, int menuId, String help) {
			super();
			this.id = id;
			this.pageContext = pageContext;
			this.name = name;
			this.userId = userId;
			this.portNodeId = portNodeId;
			this.status = status;
			this.type = type;
			this.menuId = menuId;
			this.help = help;
		}

	}
	public static class EmailGroup{
		private int id;
		private int status;
		private String name;
		private String notes;
		private ArrayList<EmailUser> emailUserList;
		public ArrayList<EmailUser> getEmailUserList() {
			return emailUserList;
		}
		public void setEmailUserList(ArrayList<EmailUser> emailUserList) {
			this.emailUserList = emailUserList;
		}
		public EmailGroup(int id, int status, String name, String notes) {
			super();
			this.id = id;
			this.status = status;
			this.name = name;
			this.notes = notes;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getNotes() {
			return notes;
		}
		public void setNotes(String notes) {
			this.notes = notes;
		}

	}

	public static class EmailUser{
		public static final int USER_TYPE_TO = 0;
		public static final int USER_TYPE_CC = 1;
		public static final int USER_TYPE_BCC = 2;
		private int groupId;
		private String name;
		private String email;
		private int type;
		private int status;
		private String mobile;

		public String getMobile() {
			return mobile;
		}
		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		public EmailUser(int groupId, String name, String email, int type, int status,String mobile) {
			super();
			this.groupId = groupId;
			this.name = name;
			this.email = email;
			this.type = type;
			this.status = status;
			this.mobile = mobile;
		}
		public int getGroupId() {
			return groupId;
		}
		public void setGroupId(int groupId) {
			this.groupId = groupId;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getName() {
			return name;
		}
		public int getStatus() {
			return status;
		}
		public int getType() {
			return type;
		}

		public void setName(String name) {
			this.name = name;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public void setType(int type) {
			this.type = type;
		}
	}
	public static class Frequency{
		private int id;
		private long startDate;//when start scheduling.
		private int dailyStartHr = Misc.getUndefInt();//start sending hr in current day.
		private int dailyStartMin = Misc.getUndefInt();//start sending min in current day
		private int granularity;//daily,weekly,monthly,yearly,hourly
		private int sendFreq = Misc.getUndefInt(); //suppose if granularity is daily then this field value define every x days
		private int dailyMailLimit;//define 
		private long expireAfter;//expires after 
		private int status;//active,inactive,deleted
		private long scheduledDate=Misc.getUndefInt();
		private String name;
		private String notes;
		private ShiftBean shift=null;
		
		private boolean isShift(Connection conn,int portNodeId){
			if(this.shift == null){
				this.shift = ShiftInformation.getShift(conn, portNodeId, new Date());
			}
			return this.shift != null;
		}
		public ShiftBean getShift() {
			return shift;
		}
		public String getNotes() {
			return notes;
		}
		public void setNotes(String notes) {
			this.notes = notes;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public long getScheduledDate(Connection conn,int portNodeId) {
			calculateScheduledDate(conn,portNodeId);
			return scheduledDate;
		}
		public void setScheduledDate(long scheduledDate) {
			this.scheduledDate = scheduledDate;
		}
		@SuppressWarnings("deprecation")
		private void calculateScheduledDate(Connection conn,int portNodeId){
			if(!Misc.isUndef(scheduledDate))
				return;
			long st = (granularity == Misc.SCOPE_SHIFT && isShift(conn,portNodeId)) ? Misc.getUndefInt() : TimePeriodHelper.getBegOfDate(System.currentTimeMillis(), granularity);
			if(!Misc.isUndef(st)){
				Date dt = new Date(st);
				if(granularity == Misc.SCOPE_SHIFT){
					dt.setHours(shift.getStartHour());
					dt.setMinutes(shift.getStartMin());
				}else if(granularity ==  Misc.SCOPE_HOUR || granularity == Misc.SCOPE_HOUR_RELATIVE ){
					dt.setMinutes(0);
				}else{
					if(!Misc.isUndef(dailyStartHr))
						dt.setHours(dailyStartHr);
					if(!Misc.isUndef(dailyStartMin))
						dt.setMinutes(dailyStartMin);
				}
				this.scheduledDate = dt.getTime();
			}
		}
		private long getGranularityTotalTime(Connection conn, int portNodeId){
			switch(granularity){
			case Misc.SCOPE_SHIFT :
				return (isShift(conn, portNodeId) && shift.getDur() > 0.0 ?  (long)(shift.getDur()*60) : -1)*60*1000*(Misc.isUndef(sendFreq) ? 1 : sendFreq);
			case Misc.SCOPE_HOUR:
				return 60*60*1000*(Misc.isUndef(sendFreq) ? 1 : sendFreq);
			case Misc.SCOPE_DAY:				
				return 24*60*60*1000*(Misc.isUndef(sendFreq) ? 1 : sendFreq);
			case Misc.SCOPE_WEEK:
				return 7*24*60*60*1000*(Misc.isUndef(sendFreq) ? 1 : sendFreq);
			case Misc.SCOPE_ANNUAL:
				return 365*24*60*60*1000*(Misc.isUndef(sendFreq) ? 1 : sendFreq);
			default : 
				return Long.MAX_VALUE;
			}
			
		}
		public boolean isValidFreq(Connection conn, int portNodeId, long lastSendingDate){
			return Misc.isUndef(lastSendingDate) || ( !Misc.isUndef(getScheduledDate(conn,portNodeId)) && (((double)(scheduledDate-((lastSendingDate/1000)*1000))/(double)getGranularityTotalTime(conn,portNodeId)) >= 0.9));
		}

		public Frequency(int id, long startDate, int dailyStartHr, int dailyStartMin, int granularity, int sendFreq,
				int dailyMailLimit, long expireAfter, int status,String name, String notes) {
			super();
			this.id = id;
			this.startDate = startDate;
			this.dailyStartHr = dailyStartHr;
			this.dailyStartMin = dailyStartMin;
			this.granularity = granularity;
			this.sendFreq = sendFreq;
			this.dailyMailLimit = dailyMailLimit;
			this.expireAfter = expireAfter;
			this.status = status;
			this.name = name;
			this.notes = notes;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public long getStartDate() {
			return startDate;
		}
		public void setStartDate(long startDate) {
			this.startDate = startDate;
		}
		public int getDailyStartHr() {
			return dailyStartHr;
		}
		public void setDailyStartHr(int dailyStartHr) {
			this.dailyStartHr = dailyStartHr;
		}
		public int getDailyStartMin() {
			return dailyStartMin;
		}
		public void setDailyStartMin(int dailyStartMin) {
			this.dailyStartMin = dailyStartMin;
		}
		public int getGranularity() {
			return granularity;
		}
		public void setGranularity(int granularity) {
			this.granularity = granularity;
		}
		public int getSendFreq() {
			return sendFreq;
		}
		public void setSendFreq(int sendFreq) {
			this.sendFreq = sendFreq;
		}
		public int getDailyMailLimit() {
			return dailyMailLimit;
		}
		public void setDailyMailLimit(int dailyMailLimit) {
			this.dailyMailLimit = dailyMailLimit;
		}
		public long getExpireAfter() {
			return expireAfter;
		}
		public void setExpireAfter(long expireAfter) {
			this.expireAfter = expireAfter;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}

	}
	public static class ReportSpan{
		private int id;
		private int status;
		private int granularity;
		private int startHr;
		private int startMin;
		private double relativeSt;
		private double relativeEn;
		private String name;
		private String notes;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getNotes() {
			return notes;
		}
		public void setNotes(String notes) {
			this.notes = notes;
		}
		public int getId() {
			return id;
		}
		public int getStatus() {
			return status;
		}
		public int getGranularity() {
			return granularity;
		}
		public double getRelativeSt() {
			return relativeSt;
		}
		public double getRelativeEn() {
			return relativeEn;
		}
		public void setId(int id) {
			this.id = id;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public void setGranularity(int granularity) {
			this.granularity = granularity;
		}
		public void setRelativeSt(double relativeSt) {
			this.relativeSt = relativeSt;
		}
		public void setRelativeEn(double relativeEn) {
			this.relativeEn = relativeEn;
		}
		public int getStartHr() {
			return startHr;
		}
		public int getStartMin() {
			return startMin;
		}
		public void setStartHr(int startHr) {
			this.startHr = startHr;
		}
		public void setStartMin(int startMin) {
			this.startMin = startMin;
		}
		public ReportSpan(int id, int status, int granularity, int startHr, int startMin, double relativeSt,
				double relativeEn, String name, String notes ) {
			super();
			this.id = id;
			this.status = status;
			this.granularity = granularity;
			this.startHr = startHr;
			this.startMin = startMin;
			this.relativeSt = relativeSt;
			this.relativeEn = relativeEn;
			this.name = name;
			this.notes = notes;
		}

	}
	public static class ExcelTemplate{
		private int id;
		private String name;
		private String url;
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
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public ExcelTemplate(int id, String name, String url) {
			super();
			this.id = id;
			this.name = name;
			this.url = url;
		}

	}
	public static class OrgMailingProfile{
		private int id=1;
		private String shortCode="IPSSI";
		private String name="IntelliPlanner Software Systems India Pvt. Ltd.";
		private String mailFrom="reports@ipssi.com";
		private String mailLogo="/home/jboss/static/images/report_logo.png";
		private String website="http://www.ipssi.com/amber.htm;http://203.197.197.17:8008/LocTracker/home.jsp";
		private String address="D-83, Second Floor, Sector-6, Noida-201301, India";
		private String body;
		private String contactNo="1204323898";
		private String supportMail="operation@ipssi.com";
		private String smtpHost="smtp.ipssi.com";
		private int smtpPort=25;
		private String smtpUser="reports@ipssi.com";
		private String smtpPassword="reports123";
		public OrgMailingProfile(){

		}
		public OrgMailingProfile(int id, String shortCode, String name, String mailFrom, String mailLogo, String website,
				String address, String body, String contactNo, String smtpHost, int smtpPort,
				String smtpUser, String smtpPassword) {
			super();
			this.id = id;
			this.shortCode = shortCode;
			this.name = name;
			this.mailFrom = mailFrom;
			this.mailLogo = mailLogo;
			this.website = website;
			this.address = address;
			this.body = body;
			this.contactNo = contactNo;
			this.smtpHost = smtpHost;
			this.smtpPort = smtpPort;
			this.smtpUser = smtpUser;
			this.smtpPassword = smtpPassword;
		}
		public void setId(int id){
			this.id =id;
		}
		public int getId(){
			return this.id;
		}
		public void setShortCode(String shortCode){
			this.shortCode = shortCode;
		}
		public String getShortCode(){
			return this.shortCode;
		}
		public void setName(String name){
			this.name = name;
		}
		public String getName(){
			return this.name;
		}
		public void setMailFrom(String mailFrom){
			this.mailFrom = mailFrom;
		}
		public String getMailFrom(){
			return this.mailFrom;
		}
		public void setMailLogo(String mailLogo){
			this.mailLogo = mailLogo;
		}
		public String getMailLogo(){
			return this.mailLogo;
		}
		public void setWebsite(String website){
			this.website = website;
		}
		public String getWebsite(){
			return this.website;
		}
		public void setAddress(String address){
			this.address = address;
		}
		public String getAddress(){
			return this.address;
		}
		public void setBody(String body){
			this.body = body;
		}
		public String getBody(){
			return this.body;
		}
		public void setContactNo(String contactNo){
			this.contactNo = contactNo;
		}
		public String getContactNo(){
			return this.contactNo;
		}
		public void setSupportMail(String supportMail){
			this.supportMail = supportMail;
		}
		public String getSupportMail(){
			return this.supportMail;
		}
		public void setSmtpHost(String smtpHost){
			this.smtpHost = smtpHost;
		}
		public String getSmtpHost(){
			return this.smtpHost;
		}
		public void setSmtpPort(int smtpPort){
			this.smtpPort = smtpPort;
		}
		public int getSmtpPort(){
			return this.smtpPort;
		}
		public void setSmtpUser(String smtpUser){
			this.smtpUser = smtpUser;
		}
		public String getSmtpUser(){
			return this.smtpUser;
		}
		public void setSmtpPassword(String smtpPassword){
			this.smtpPassword = smtpPassword;
		}
		public String getSmtpPassword(){
			return this.smtpPassword;
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		StringBuilder error = new StringBuilder();
		try {
			//conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			System.out.println(new Date(1475209800000l));
			System.out.println(new Date(1475148113000l));
			/*ArrayList<EmailInfo> emailInfoList = EmailSchedulerInformation.getEmailForSending(conn);
			for(int k=0,ks=emailInfoList == null ? 0 : emailInfoList.size(); k<ks; k++){
				EmailInfo mailBean = emailInfoList.get(k);
				if(mailBean == null|| !mailBean.isValidEmailInfo(conn,mailBean.getPortNodeId()))
					continue;
				System.out.println("LocTracker Invoke Result:"+mailBean.id);
			}*/
		} catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	public static class EmailStatusDetail{
		public static final int STATUS_SEND_RETRY_0 = -3;
		public static final int STATUS_SEND_RETRY_1 = -2;
		public static final int STATUS_SEND_RETRY_2 = -1;
		public static final int STATUS_SEND_FAILED = 0;
		public static final int STATUS_SEND_SUCCESS = 1;
		public static final int STATUS_SEND_NO_DATA = 2;

		private int id;
		private int emailInfoId;
		private int status;
		private long createDate;
		private long scheduledDate;
		private long sendingDate;

		public EmailStatusDetail(int id, int emailInfoId, int status, long createDate, long scheduledDate,
				long sendingDate) {
			super();
			this.id = id;
			this.emailInfoId = emailInfoId;
			this.status = status;
			this.createDate = createDate;
			this.scheduledDate = scheduledDate;
			this.sendingDate = sendingDate;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getEmailInfoId() {
			return emailInfoId;
		}
		public void setEmailInfoId(int emailInfoId) {
			this.emailInfoId = emailInfoId;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public long getCreateDate() {
			return createDate;
		}
		public void setCreateDate(long createDate) {
			this.createDate = createDate;
		}
		public long getScheduledDate() {
			return scheduledDate;
		}
		public void setScheduledDate(long scheduledDate) {
			this.scheduledDate = scheduledDate;
		}
		public long getSendingDate() {
			return sendingDate;
		}
		public void setSendingDate(long sendingDate) {
			this.sendingDate = sendingDate;
		}

	}

}

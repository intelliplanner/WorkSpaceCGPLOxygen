package com.ipssi.reporting.reportForMenu;

import java.util.ArrayList;

public class MenuReportBean {
    private long reportId;
    private int menuMasterId;
	private int menuPlaceHolderId;
	private int userId;
	private int orgId;
	private int status;
	private int type;
	private String optionMenuTagName;
	private String menuTagName;
	private String menuTitle;
	private String url;
	private String help;
	private String configFile;
	private ReprtGroupEmailAndFrequency emailReportGroupInformation;
	private ArrayList reportUserEmailList;
	private ArrayList reportFrequencyList;
	private ArrayList existingEmailGroupList;
	private int noData;
	private int senderProfile;
	
	public static class ReprtGroupEmailAndFrequency{
		private int emailReportId;
		private int status;
		private int granularity;
		private String emailReportName;
		private int shiftId;
		private String shiftName;
		private int shiftHours;
		private int shiftMinutes;
		private int email_report_format;
		public int getEmailReportId() {
			return emailReportId;
		}
		public void setEmailReportId(int emailReportId) {
			this.emailReportId = emailReportId;
		}
		public String getEmailReportName() {
			return emailReportName;
		}
		public void setEmailReportName(String emailReportName) {
			this.emailReportName = emailReportName;
		}
		public int getShiftId() {
			return shiftId;
		}
		public void setShiftId(int shiftId) {
			this.shiftId = shiftId;
		}
		public String getShiftName() {
			return shiftName;
		}
		public void setShiftName(String shiftName) {
			this.shiftName = shiftName;
		}
		public double getShiftHours() {
			return shiftHours;
		}
		public void setShiftHours(int shiftHours) {
			this.shiftHours = shiftHours;
		}
		public int getEmail_report_format() {
			return email_report_format;
		}
		public void setEmail_report_format(int email_report_format) {
			this.email_report_format = email_report_format;
		}
		public double getShiftMinutes() {
			return shiftMinutes;
		}
		public void setShiftMinutes(int shiftMinutes) {
			this.shiftMinutes = shiftMinutes;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public int getGranularity() {
			return granularity;
		}
		public void setGranularity(int granularity) {
			this.granularity = granularity;
		}
		
	}
	public long getReportId() {
		return reportId;
	}
	public void setReportId(long reportId) {
		this.reportId = reportId;
	}
	public int getMenuPlaceHolderId() {
		return menuPlaceHolderId;
	}
	public void setMenuPlaceHolderId(int menuPlaceHolderId) {
		this.menuPlaceHolderId = menuPlaceHolderId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getOrgId() {
		return orgId;
	}
	public void setOrgId(int orgId) {
		this.orgId = orgId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getOptionMenuTagName() {
		return optionMenuTagName;
	}
	public void setOptionMenuTagName(String optionMenuTagName) {
		this.optionMenuTagName = optionMenuTagName;
	}
	public String getMenuTagName() {
		return menuTagName;
	}
	public void setMenuTagName(String menuTagName) {
		this.menuTagName = menuTagName;
	}
	public String getMenuTitle() {
		return menuTitle;
	}
	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHelp() {
		return help;
	}
	public void setHelp(String help) {
		this.help = help;
	}
	
	public ArrayList getReportUserEmailList() {
		return reportUserEmailList;
	}
	public void setReportUserEmailList(ArrayList reportUserEmailList) {
		this.reportUserEmailList = reportUserEmailList;
	}
	public ArrayList getReportFrequencyList() {
		return reportFrequencyList;
	}
	public void setReportFrequencyList(ArrayList reportFrequencyList) {
		this.reportFrequencyList = reportFrequencyList;
	}
	public ReprtGroupEmailAndFrequency getEmailReportGroupInformation() {
		return emailReportGroupInformation;
	}
	public void setEmailReportGroupInformation(ReprtGroupEmailAndFrequency emailReportGroupInformation) {
		this.emailReportGroupInformation = emailReportGroupInformation;
	}
	public ArrayList getExistingEmailGroupList() {
		return existingEmailGroupList;
	}
	public void setExistingEmailGroupList(ArrayList existingEmailGroupList) {
		this.existingEmailGroupList = existingEmailGroupList;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getConfigFile() {
		return configFile;
	}
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	public int getMenuMasterId() {
		return menuMasterId;
	}
	public void setMenuMasterId(int menuMasterId) {
		this.menuMasterId = menuMasterId;
	}
	public void setNodata(int noData){
		this.noData = noData;
	}
	public int getNodata(){
		return noData;
	}
	public void setSenderProfile(int senderProfile){
		this.senderProfile = senderProfile;
	}
	public int getSenderProfile(){
		return senderProfile;
	}
}

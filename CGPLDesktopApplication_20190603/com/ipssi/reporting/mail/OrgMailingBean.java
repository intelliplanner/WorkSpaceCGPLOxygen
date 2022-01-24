package com.ipssi.reporting.mail;

public class OrgMailingBean {
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

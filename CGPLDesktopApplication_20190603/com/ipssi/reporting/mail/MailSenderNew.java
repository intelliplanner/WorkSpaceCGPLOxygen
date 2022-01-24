package com.ipssi.reporting.mail;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import sun.net.smtp.SmtpClient;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailInfo;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailUser;
import com.ipssi.reporting.email.EmailSchedulerInformation.OrgMailingProfile;

public class MailSenderNew {
	private static Logger logger = Logger.getLogger(MailSender.class);
	private static final String SMTP_HOST_NAME = "SMTP_HOST_NAME";
	private static final String SMTP_PORT = "SMTP_PORT";
	private static final String SMTP_AUTH_USER = "SMTP_AUTH_USER";
	private static final String SMTP_AUTH_PWD = "SMTP_AUTH_PWD";
	private static final String FILE_TYPE = "FILE_TYPE_";
	private static final String FILE_NAME = "FILE_NAME_";
	private static String NO_DATA_RECIPIENT = "NO_DATA_RECIPIENT";
	private static String BUG_RECIPIENT = "BUG_RECIPIENT";
	private static int SEND_NO_DATA_REPORT = 1;
	private static final String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

	private static Session _getSession() throws NamingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", PropertyManager.getString(SMTP_HOST_NAME));
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "false");//true default cases , false only for maithon
		props.put("mail.smtp.port", PropertyManager.getString(SMTP_PORT));

		props.put ("mail.debug.auth", "true");
		props.put ("mail.smtp.auth", "false");

		//comment for mpl

		/*

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl", "true");
		Authenticator auth = new MailSender().new SMTPAuthenticator();
		Session session = Session.getInstance(props, auth);*/

		//uncomment for maithon
		props.put("mail.smtp.localhost","mail.tatapower.com");
		Session session = Session.getDefaultInstance(props);
		return session;
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			String username = PropertyManager.getString(SMTP_AUTH_USER);
			String password = PropertyManager.getString(SMTP_AUTH_PWD);
			return new PasswordAuthentication(username, password);
		}
	}

	private static void _sendError(InternetAddress to, SendFailedException sfe,String reportName) {
		try {
			Session session = _getSession();
			String mailHost = session.getProperty("mail.smtp.host");
			SmtpClient smtp = new SmtpClient(mailHost);
			InternetAddress from = new InternetAddress("MAILER-DAEMON@" + mailHost, "Mail Delivery Subsystem");
			smtp.from(from.getAddress());
			smtp.to(to.getAddress());
			StringBuffer sb = new StringBuffer();
			sb.append("From: ").append(from.toString()).append("\n");
			sb.append("To: ").append(to.toString()).append("\n");
			sb.append("Subject: ").append("Returned mail: see transcript for details").append("\n\n");
			sb.append(sfe.toString()).append("\n");
			sb.append("Report Name: ").append(reportName).append("\n");
			PrintStream msg = smtp.startMessage();
			msg.print(sb.toString());
			msg.close();
			smtp.closeServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void send(AutoEmailBean mailBean, String subject, String body,byte[] file, int fileType, boolean noData,String instanceName) throws Exception {
		ArrayList<String> toList = mailBean.getRecipient(); 
		if(toList != null){
			String toAddrStr = null;
			for(String toAddr : toList){
				if(toAddrStr == null)
					toAddrStr = toAddr;
				else
					toAddrStr += ", "+toAddr;
			}
			System.out.print("[AMSTT] "+ subject + " send to -" + toAddrStr);
		}
		String reportName = mailBean.getReportName();
		InternetAddress[] to = null;
		int index = 0;
		body += noData ? "No Data Generated For Report " + reportName + " at " + new Date() : "Please Find Attached Report." ;
		InternetAddress from = new InternetAddress(mailBean.getOrgMailingBean().getSmtpUser());
		String website[] = mailBean != null && mailBean.getOrgMailingBean() != null && mailBean.getOrgMailingBean().getWebsite() != null ? mailBean.getOrgMailingBean().getWebsite().split(";") : null;
		body  = body + 
				"<table>	" +
				((mailBean == null || mailBean.getOrgMailingBean() == null ||  mailBean.getOrgMailingBean().getShortCode() == null || !"MAITHON".equalsIgnoreCase(mailBean.getOrgMailingBean().getShortCode())) ?
						("<tr><td>--</td></tr>" +
								"<tr><td><font color='BlueViolet'>"+mailBean.getOrgMailingBean().getName()+"</font></td></tr>"+
								"<tr><td>"+mailBean.getOrgMailingBean().getAddress()+"</td></tr>" +
								"<tr><td>"+mailBean.getOrgMailingBean().getSupportMail()+"</td></tr> "+
								"<tr><td>Phone- +91"+mailBean.getOrgMailingBean().getContactNo()+"</td></tr> "+
								"<tr><td>"+(website != null && website.length > 0 ? website[0] : "" )+"</td></tr>" +
								"<tr><td>OR</td></tr> "+
								"<tr><td>"+(website != null && website.length > 1 ? website[1] : "" )+"</td></tr>" +
								"<tr><td><font color='green'>----------------------------------------------------------------------	</font></td></tr>"+
								"<tr><td><font color='green'>GPS Fleet Management Solutions, Vehicle Tracking</font></td></tr><tr><td><font color='green'>"+
								"----------------------------------------------------------------------</font></td></tr>"+
								"<tr><td>This mail contains proprietary and confidential information.</td></tr>	<tr><td>"+
								"If you received this in error, please delete it and inform the sender.	</td></tr>" +
								"<tr><td>Report Generated From "+instanceName.toUpperCase()+"</td></tr>" )
						: 
							(
									"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Regards,</b></td></tr>" +
											"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Administrator- RFID WB System </b></td></tr>" +
											"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Powered by IntelliPlanner </b></td></tr>" +
											"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Note: This is an auto generated email from RFID System, please do not respond to the same.</b></td></tr>" 
									)
						)+		
				"</table>";
		if(false){
			if(to == null)
				to = new InternetAddress[1];
			to[0] = new InternetAddress("rahul@ipssi.com");
			send(from, to, subject, body, file, reportName, fileType,instanceName);
		}else{
			if(noData){
				String supportMail = PropertyManager.getString(NO_DATA_RECIPIENT);
				String[] supportMailList = supportMail.split(",");
				body += "No data generated for report " + reportName + " at " + new Date();
				if(mailBean.getNoData() == SEND_NO_DATA_REPORT){
					for(String toAdd : toList){
						if(toAdd != null && toAdd.length() > 0){
							if(to == null)
								to = new InternetAddress[toList.size()];
							to[index++] = new InternetAddress(toAdd);	
						}
					}
					if(to != null)
						send(from, to, subject, body, null, reportName, fileType,instanceName);
				}else{
					for(String toAdd : supportMailList){
						if(toAdd != null && toAdd.length() > 0){
							if(to == null)
								to = new InternetAddress[supportMailList.length];
							to[index++] = new InternetAddress(toAdd);
						}
					}
					if(to != null)
						send(from, to, subject, body, null, reportName, fileType,instanceName);
				}
			}else{
				for(String toAdd : toList){
					if(toAdd != null && toAdd.length() > 0 ){
						if(to == null)
							to = new InternetAddress[toList.size()];
						to[index++] = new InternetAddress(toAdd);	
					}
				}
				if(to != null)
					send(from, to, subject, body, file, reportName, fileType,instanceName);
			}
		}
	}
	public static void sendNew(EmailInfo mailBean,byte[] file, boolean noData,String instanceName,OrgMailingProfile orgProfile) throws Exception {
		ArrayList<EmailUser> toList = mailBean.getGroup().getEmailUserList(); 
		ArrayList<InternetAddress> to = null;
		ArrayList<InternetAddress> cc = null;
		ArrayList<InternetAddress> bcc = null;
		String toAddrStr = null;
		if(toList != null){
			for(EmailUser user : toList){
				if(user != null && user.getEmail() != null && user.getEmail().length() > 0){
					if(user.getType() == EmailUser.USER_TYPE_BCC){//bcc
						if(bcc == null)
							bcc = new ArrayList<InternetAddress>();
						bcc.add(new InternetAddress(user.getEmail()));
					}else if(user.getType() == EmailUser.USER_TYPE_CC){//cc
						if(cc == null)
							cc = new ArrayList<InternetAddress>();
						cc.add(new InternetAddress(user.getEmail()));
					}else{
						if(to == null)
							to = new ArrayList<InternetAddress>();
						to.add(new InternetAddress(user.getEmail()));
					}
				}
				if(toAddrStr == null)
					toAddrStr = user.getEmail();
				else
					toAddrStr += ", "+user.getEmail();
			}
		}
		String body = "";
		String subject = mailBean.getSubject();
		String reportName = mailBean.getReport().getName();
		System.out.print("[AMSTT] "+ subject + " send to -" + toAddrStr);
		body += noData ? "No Data Generated For Report " + reportName + " at " + new Date() : mailBean.getBody() ;
		InternetAddress from = new InternetAddress(orgProfile.getSmtpUser());
		String website[] = orgProfile != null && orgProfile.getWebsite() != null ? orgProfile.getWebsite().split(";") : null;
		body  = body + 
				"   <table>	"
				+ " <tr><td>&nbsp;</td></tr>"
				+ " <tr><td>&nbsp;</td></tr>"
				+ " <tr><td>&nbsp;</td></tr>"
				+ " <tr><td>&nbsp;</td></tr>" +
				//mail signature
				(orgProfile.getBody() != null && orgProfile.getBody().length() > 0 ? orgProfile.getBody() :
					("<tr><td>--</td></tr>" +
							"<tr><td><font color='BlueViolet'>"+orgProfile.getName()+"</font></td></tr>"+
							"<tr><td>"+orgProfile.getAddress()+"</td></tr>" +
							"<tr><td>"+orgProfile.getSupportMail()+"</td></tr> "+
							"<tr><td>Phone- +91"+orgProfile.getContactNo()+"</td></tr> "+
							"<tr><td>"+(website != null && website.length > 0 ? website[0] : "" )+"</td></tr>" +
							"<tr><td>OR</td></tr> "+
							"<tr><td>"+(website != null && website.length > 1 ? website[1] : "" )+"</td></tr>" +
							"<tr><td><font color='green'>----------------------------------------------------------------------	</font></td></tr>"+
							"<tr><td><font color='green'>GPS Fleet Management Solutions, Vehicle Tracking</font></td></tr><tr><td><font color='green'>"+
							"----------------------------------------------------------------------</font></td></tr>"+
							"<tr><td>This mail contains proprietary and confidential information.</td></tr>	<tr><td>"+
							"If you received this in error, please delete it and inform the sender.	</td></tr>" +
							"<tr><td>Report Generated From "+instanceName.toUpperCase()+"</td></tr>" )
						
				)+
				/*((orgProfile == null ||  orgProfile.getShortCode() == null || !"MAITHON".equalsIgnoreCase(orgProfile.getShortCode())) ?
						("<tr><td>--</td></tr>" +
								"<tr><td><font color='BlueViolet'>"+orgProfile.getName()+"</font></td></tr>"+
								"<tr><td>"+orgProfile.getAddress()+"</td></tr>" +
								"<tr><td>"+orgProfile.getSupportMail()+"</td></tr> "+
								"<tr><td>Phone- +91"+orgProfile.getContactNo()+"</td></tr> "+
								"<tr><td>"+(website != null && website.length > 0 ? website[0] : "" )+"</td></tr>" +
								"<tr><td>OR</td></tr> "+
								"<tr><td>"+(website != null && website.length > 1 ? website[1] : "" )+"</td></tr>" +
								"<tr><td><font color='green'>----------------------------------------------------------------------	</font></td></tr>"+
								"<tr><td><font color='green'>GPS Fleet Management Solutions, Vehicle Tracking</font></td></tr><tr><td><font color='green'>"+
								"----------------------------------------------------------------------</font></td></tr>"+
								"<tr><td>This mail contains proprietary and confidential information.</td></tr>	<tr><td>"+
								"If you received this in error, please delete it and inform the sender.	</td></tr>" +
								"<tr><td>Report Generated From "+instanceName.toUpperCase()+"</td></tr>" )
						: 
							(
									"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Regards,</b></td></tr>" +
									"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Administrator- RFID WB System </b></td></tr>" +
									"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Powered by IntelliPlanner </b></td></tr>" +
									"<tr><td style='font-size:11.0pt;line-height:105%;font-family:\"Arial\",\"sansserif\";color:black'><b>Note: This is an auto generated email from RFID System, please do not respond to the same.</b></td></tr>" 
							)
						)+*/		
				"</table>";

		if((!noData || mailBean.isSendNoData()) && ( to != null && to.size() > 0))
			send(from,to.toArray(new InternetAddress[to.size()]), subject, body, file, reportName, mailBean.getReportFormat(), instanceName, cc == null ? null : cc.toArray(new InternetAddress[cc.size()]), bcc == null ? null : bcc.toArray(new InternetAddress[bcc.size()]));
	}
	public static void send(InternetAddress from, InternetAddress[] to, String subject, String body,byte[] file, String reportName,int fileType,String instanceName) throws Exception {
		send(from, to, subject, body, file, reportName, fileType, instanceName, null, null);
	}
	public static void send(InternetAddress from, InternetAddress[] to, String subject, String body,byte[] file, String reportName,int fileType,String instanceName,InternetAddress[] cc,InternetAddress[] bcc ) throws Exception {
		String bugRecipient = "rahul@ipssi.com";
		InternetAddress[] bugMailRecipients = null;
		try {
			bugRecipient = Misc.getParamAsString(PropertyManager.getString(BUG_RECIPIENT),"rahul@ipssi.com");

			Message msg = new MimeMessage(_getSession());
			msg.setFrom(from);
			if(PropertyManager.getInteger("DEBUG") == 1){
				System.out.println("[AMSTT DEBUG MODE :]");
				int index = 0;
				String[] supportMailList = PropertyManager.getString("SUPPORT_"+instanceName.toUpperCase()).split(",");
				InternetAddress[] supportRecipients = new InternetAddress[supportMailList.length];
				for(String toAdd : supportMailList){
					supportRecipients[index++] = new InternetAddress(toAdd);
				}
				msg.setRecipients(Message.RecipientType.TO,supportRecipients);
			}
			else
				msg.setRecipients(Message.RecipientType.TO,to);
			if(cc != null && cc.length > 0)
				msg.setRecipients(Message.RecipientType.CC,cc);
			if(bcc != null && bcc.length > 0)
				msg.setRecipients(Message.RecipientType.BCC,bcc);
			msg.setSubject(subject.split(",")[0]);
			msg.setSentDate(Calendar.getInstance().getTime());
			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText(body);
			bodyPart.setContent(body, "text/html");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);
			if (file != null && file.length > 0){
				String[] reportNameAndMail = reportName.split(",");
				addAtachments(multipart, file,reportNameAndMail[0], fileType);
			}
			msg.setContent(multipart);
			Transport.send(msg);
		} catch (SendFailedException sfe) {
			int index = 0;
			sfe.printStackTrace();
			String[] bugMailList = bugRecipient.split(",");
			bugMailRecipients = new InternetAddress[bugMailList.length];
			for(String toAdd : bugMailList){
				bugMailRecipients[index++] = new InternetAddress(toAdd);	
			}
			_sendError(new InternetAddress(bugRecipient), sfe,reportName);
			throw new Exception(sfe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		finally{
			try{
				/*if(file != null){
					file.flush();	
					file.close();
				}*/
			}catch(Exception e){
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
		}
	}

	private static void addAtachments(Multipart multipart,byte[] file,String reportName, int fileType)
			throws MessagingException, AddressException, IOException {
		MimeBodyPart attachmentBodyPart = new MimeBodyPart();
		attachmentBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(file, PropertyManager.getString(FILE_TYPE+fileType))));
		attachmentBodyPart.setFileName(reportName+PropertyManager.getString(FILE_NAME+fileType));
		multipart.addBodyPart(attachmentBodyPart);
	}
	public static boolean isValidEmail(String emailId) {
		return emailId.matches(EMAIL_REGEX);
	}
	public static void sendAllReminders(Connection conn){
		ArrayList<DataBean> reminderAlertList = null;
		try{
			reminderAlertList = VehicleServiceReminderHelper.getServiceAlertList(conn);
			if(reminderAlertList != null){
				for(DataBean reminderAlert : reminderAlertList){
					try{
						InternetAddress[] to = null;
						InternetAddress from = new InternetAddress("notifications@ipssi.com");
						String body  = reminderAlert.getAlertFormatedString() + "<table>	<tr><td>--</td></tr><tr><td><font color='BlueViolet'>IntelliPlanner Software Systems India Pvt. Ltd.</font></td></tr>"+
								"<tr><td>D-83, Second Floor, Sector-6, Noida-201301, India</td></tr>" +
								"<tr><td>operation@ipssi.com</td></tr> "+
								"<tr><td>Phone- +911204323898</td></tr> "+
								"<tr><td>http://203.197.197.18:9380/LocTracker/home.jsp</td></tr>" +
								"<tr><td><font color='green'>----------------------------------------------------------------------	</font></td></tr>"+
								"<tr><td><font color='green'>GPS Fleet Management Solutions, Vehicle Tracking</font></td></tr><tr><td><font color='green'>"+
								"----------------------------------------------------------------------</font></td></tr>"+
								"<tr><td>This mail contains proprietary and confidential information.</td></tr>	<tr><td>"+
								"If you received this in error, please delete it and inform the sender.	</td></tr>" +
								"<tr><td>Report Generated From Server Gati</td></tr></table>";
						if(reminderAlert.getCustomerContactList() == null || reminderAlert.getCustomerContactList().size() <= 0){
							to = new InternetAddress[3];
							to[0] = new InternetAddress("dev@ipssi.com");
							to[1] = new InternetAddress("aditya@ipssi.com");
							to[2] = new InternetAddress("rahul@ipssi.com");
						}
						else{
							to = new InternetAddress[reminderAlert.getCustomerContactList().size()];
							int index = 0;
							for(CustomerContactInfo custInfo : reminderAlert.getCustomerContactList()){
								to[index++] = new InternetAddress(custInfo.getEmail());
							}
						}
						send(from, to, "Ipssi Service Reminder", body, null, null, Misc.getUndefInt(), Misc.getServerName());
						VehicleServiceReminderHelper.setAlertLogSendStatus(conn,2,reminderAlert.getId());
					}catch(Exception ex){
						if(reminderAlert.getStatus() == 3)
							VehicleServiceReminderHelper.setAlertLogSendStatus(conn,6,reminderAlert.getId());
						else
							VehicleServiceReminderHelper.setAlertLogSendStatus(conn,3,reminderAlert.getId());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws AddressException, Exception
	{
		InternetAddress from = new InternetAddress("rahul@ipssi.com");
		InternetAddress[] to = new InternetAddress[]{new InternetAddress("rahul@gmail.com")};

		Connection conn  = null;
		boolean destroyIt = false;
		try{
			send(from, to, "test", "test", null, "text", Misc.getUndefInt(), "local");
			//conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			//sendAllReminders(conn);
		}catch(Exception ex){
			destroyIt = true;
		}finally{
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}
}

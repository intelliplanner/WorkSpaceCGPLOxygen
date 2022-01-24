package com.ipssi.reporting.mail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.TimePeriodHelper;

public class EmailSenderDao {

	private static final String INSERT = "insert into auto_email_information" +
			"(report_id,group_id,report_name,report_type,subject,status,create_date,scheduled_date,sending_date,granularity,frequency_id,org_mailing_id,no_data,error_str,db_name) " +
			"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String FETCH_EMAIL_REPORT_DEFINITION = " select email_report_definition.email_report_group_id group_id,email_report_definition.report_definition_id report_id,report_definitions.name, "+
			" email_report_frequencies.frequency_id,email_report_frequencies.hours,email_report_frequencies.minutes,email_report_frequencies.granularity,report_definitions.org_mailing_id,report_definitions.no_data from email_report_definition left outer join report_definitions on (email_report_definition.report_definition_id=report_definitions.id) "+
			" left outer join email_report_frequencies on (email_report_definition.email_report_group_id=email_report_frequencies.email_report_group_id) where report_definitions.status=1 ";
	private static final String FETCH_EMAIL_REPORT_DEFINITION_BY_ID = " select email_report_definition.email_report_group_id group_id,email_report_definition.report_definition_id report_id,report_definitions.name, "+
			" email_report_frequencies.frequency_id,email_report_frequencies.hours,email_report_frequencies.minutes,email_report_frequencies.granularity,report_definitions.org_mailing_id,report_definitions.no_data from email_report_definition left outer join report_definitions on (email_report_definition.report_definition_id=report_definitions.id) "+
			" left outer join email_report_frequencies on (email_report_definition.email_report_group_id=email_report_frequencies.email_report_group_id) where report_definitions.status=1 and report_definitions.id=?";
	private static final String FETCH_EMAIL_INFORMATION = " select * from auto_email_information where status in (-1,2) order by group_id asc" ;  
	private static final String FETCH_GROUP_USER = " select * from email_report_users where email_report_group_id=?";
	private static final String UPDATE_STATUS = " update auto_email_information set status=?,sending_date=?, error_str=?,db_name=? where id=?";
	private static final String DELETE_EMAIL_INFORMATION = " delete from auto_email_information where group_id=? and report_id=? and frequency_id=? and status in (-1,2)";
	//private static final String FETCH_DAILY_EMAIL_INFORMATION = " select * from auto_email_information where scheduled_date >= ? and status != 0" ;
	private static final String FETCH_DAILY_EMAIL_INFORMATION = "select ei.id, ei.report_id, ei.report_name, ei.report_type, ei.granularity, ei.group_id, ei.status, ei.subject, ei.create_date, ei.scheduled_date, ei.sending_date, ei.frequency_id,ei.org_mailing_id, ei.no_data " +
			" from auto_email_information ei join  " +
			" (select email_report_definition.email_report_group_id group_id,email_report_definition.report_definition_id report_id,report_definitions.name,  email_report_frequencies.frequency_id,email_report_frequencies.hours,email_report_frequencies.minutes,email_report_frequencies.granularity from email_report_definition " +
			" left outer join report_definitions on (email_report_definition.report_definition_id=report_definitions.id)  " +
			" left outer join email_report_frequencies on (email_report_definition.email_report_group_id=email_report_frequencies.email_report_group_id) " +
			" where report_definitions.status=1) ed  on (ei.group_id=ed.group_id and ei.report_id=ed.report_id and ei.frequency_id=ed.frequency_id) where ei.scheduled_date >= ?  " +
			" and ei.status != 0 and ei.granularity=? ";
	private static final String FETCH_ORG_MAILING_DETAIL = " select id, name, short_code, mail_from, mail_logo, mail_smtp_host,mail_smtp_port,mail_smtp_user,mail_smtp_password,contact_no,website,body,updated_on,email,address from org_mailing_params where id=? ";
	public ArrayList<AutoEmailBean> fetchEmailReportDefinition(Connection conn) throws SQLException{
		ArrayList<AutoEmailBean> retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		AutoEmailBean mailBean = null;
		try {
			Date sysDate = new Date(System.currentTimeMillis());
			Date temp_date = null;
			retval = new ArrayList<AutoEmailBean>();
			ps = conn.prepareStatement(FETCH_EMAIL_REPORT_DEFINITION);
			rs = ps.executeQuery();
			while(rs.next())
			{   
				temp_date = new Date(System.currentTimeMillis());
				mailBean = new AutoEmailBean();
				mailBean.setReportId(Misc.getRsetInt(rs, "report_id"));
				mailBean.setGroupId(Misc.getRsetInt(rs, "group_id"));
				mailBean.setReportName(rs.getString("name"));
				mailBean.setSubject("Reporting");
				mailBean.setRecipient(fetchGroupUser(conn, mailBean.getGroupId()));
				mailBean.setCreateDate(sysDate.getTime());
				mailBean.setGranularity(Misc.getRsetInt(rs, "granularity"));
				mailBean.setStatus(-2);
				temp_date = TimePeriodHelper.getBegOfDate(temp_date, mailBean.getGranularity());	
				temp_date.setHours(Misc.getRsetInt(rs, "hours"));
				temp_date.setMinutes(Misc.getRsetInt(rs, "minutes"));
				temp_date.setSeconds(0);
				mailBean.setScheduleDate(temp_date.getTime());
				mailBean.setFrequencyId(Misc.getRsetInt(rs,"frequency_id"));
				mailBean.setOrgMailingBean(fetchOrgMailingDetail(conn,Misc.getRsetInt(rs,"org_mailing_id")));
				mailBean.setNoData(Misc.getRsetInt(rs,"no_data"));
				retval.add(mailBean);
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}
	public AutoEmailBean fetchEmailReportDefinitionById(Connection conn,int reportDefinitionId) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		AutoEmailBean retval = null;
		try {
			Date sysDate = new Date(System.currentTimeMillis());
			Date temp_date = null;
			ps = conn.prepareStatement(FETCH_EMAIL_REPORT_DEFINITION_BY_ID);
			Misc.setParamInt(ps, reportDefinitionId, 1);
			rs = ps.executeQuery();
			if(rs.next())
			{   
				temp_date = new Date(System.currentTimeMillis());
				retval = new AutoEmailBean();
				retval.setReportId(Misc.getRsetInt(rs, "report_id"));
				retval.setGroupId(Misc.getRsetInt(rs, "group_id"));
				retval.setReportName(rs.getString("name"));
				retval.setSubject("Reporting");
				retval.setRecipient(fetchGroupUser(conn, retval.getGroupId()));
				retval.setCreateDate(sysDate.getTime());
				retval.setGranularity(Misc.getRsetInt(rs, "granularity"));
				retval.setStatus(-2);
				temp_date = TimePeriodHelper.getBegOfDate(temp_date, retval.getGranularity());	
				temp_date.setHours(Misc.getRsetInt(rs, "hours"));
				temp_date.setMinutes(Misc.getRsetInt(rs, "minutes"));
				temp_date.setSeconds(0);
				retval.setScheduleDate(temp_date.getTime());
				retval.setFrequencyId(Misc.getRsetInt(rs,"frequency_id"));
				retval.setOrgMailingBean(fetchOrgMailingDetail(conn,Misc.getRsetInt(rs,"org_mailing_id")));
				retval.setNoData(Misc.getRsetInt(rs,"no_data"));
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}

	public boolean insertEmailInformation(AutoEmailBean mailDetail, Connection conn,StringBuilder errorStr, String instanceName) throws SQLException{		
		int[] iHit = null;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		try {
			if (mailDetail != null){
				stmt = conn.prepareStatement(INSERT);
				Misc.setParamInt(stmt, mailDetail.getReportId(),1);
				Misc.setParamInt(stmt, mailDetail.getGroupId(),2);
				stmt.setString(3, mailDetail.getReportName());
				Misc.setParamInt(stmt, mailDetail.getReportFormat(),4);
				stmt.setString(5, mailDetail.getSubject());
				Misc.setParamInt(stmt, mailDetail.getStatus(),6);
				stmt.setTimestamp(7, Misc.longToSqlDate(mailDetail.getCreateDate()));
				stmt.setTimestamp(8, Misc.longToSqlDate(mailDetail.getScheduleDate()));
				stmt.setTimestamp(9, Misc.longToSqlDate(mailDetail.getSendingDate()));
				Misc.setParamInt(stmt, mailDetail.getGranularity(),10);
				Misc.setParamInt(stmt, mailDetail.getFrequencyId(),11);
				Misc.setParamInt(stmt, mailDetail.getOrgMailingBean().getId(),12);
				Misc.setParamInt(stmt, mailDetail.getNoData(),13);
				stmt.setString(14, errorStr.toString());
				stmt.setString(15, instanceName);
				stmt.addBatch(); 
			}
			iHit = stmt.executeBatch();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(stmt!=null)
				stmt.close();	
		}

		if (iHit[0] > 0)
			insertStatus = true;
		return insertStatus;
	}
	public ArrayList<AutoEmailBean> fetchEmailInformation(Connection conn) throws SQLException{
		ArrayList<AutoEmailBean> retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		AutoEmailBean mailBean = null;
		try {
			retval = new ArrayList<AutoEmailBean>();
			ps = conn.prepareStatement(FETCH_EMAIL_INFORMATION+" and report_definitio");
			rs = ps.executeQuery();
			while(rs.next())
			{
				mailBean = new AutoEmailBean();
				mailBean.setMailId(Misc.getRsetInt(rs, "id"));
				mailBean.setReportId(Misc.getRsetInt(rs, "report_id"));
				mailBean.setReportName(rs.getString("report_name"));
				mailBean.setReportFormat(Misc.getRsetInt(rs, "report_type"));
				mailBean.setGranularity(Misc.getRsetInt(rs, "granularity"));
				mailBean.setGroupId(Misc.getRsetInt(rs, "group_id"));
				mailBean.setRecipient(fetchGroupUser(conn, mailBean.getGroupId()));
				mailBean.setStatus(Misc.getRsetInt(rs, "status"));
				mailBean.setSubject(rs.getString("subject"));
				mailBean.setCreateDate(rs.getTimestamp("create_date").getTime());
				mailBean.setScheduleDate(rs.getTimestamp("scheduled_date").getTime());
				mailBean.setSendingDate(rs.getTimestamp("sending_date").getTime());
				mailBean.setFrequencyId(Misc.getRsetInt(rs,"frequency_id"));
				mailBean.setOrgMailingBean(fetchOrgMailingDetail(conn,Misc.getRsetInt(rs,"org_mailing_id")));
				mailBean.setNoData(Misc.getRsetInt(rs,"no_data"));
				retval.add(mailBean);
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}
	
	public boolean updateEmailInformation(AutoEmailBean mailDetail, Connection conn,StringBuilder errorStr, String dbName) throws SQLException{		
		int iHit = 0;
		boolean updateStatus = false;
		PreparedStatement stmt=null;
		try {
			if (mailDetail != null){
				stmt = conn.prepareStatement(UPDATE_STATUS);
				Misc.setParamInt(stmt, mailDetail.getStatus(),1);
				stmt.setTimestamp(2, Misc.longToSqlDate(System.currentTimeMillis()));
				stmt.setString(3, errorStr.toString());
				stmt.setString(4, dbName);
				Misc.setParamInt(stmt, mailDetail.getMailId(),5);
			}
			iHit = stmt.executeUpdate();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(stmt!=null)
				stmt.close();	
		}

		if (iHit > 0)
			updateStatus = true;
		return updateStatus;
	}
	public boolean deleteEmailInformation(AutoEmailBean mailDetail, Connection conn) throws SQLException{		
		int iHit = 0;
		boolean updateStatus = false;
		PreparedStatement stmt=null;
		try {
			if (mailDetail != null){
				stmt = conn.prepareStatement(DELETE_EMAIL_INFORMATION);
				Misc.setParamInt(stmt, mailDetail.getGroupId(),1);
				Misc.setParamInt(stmt, mailDetail.getReportId(),2);
				Misc.setParamInt(stmt, mailDetail.getFrequencyId(),3);
			}
			iHit = stmt.executeUpdate();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(stmt!=null)
				stmt.close();	
		}

		if (iHit > 0)
			updateStatus = true;
		return updateStatus;
	}
	public ArrayList<String> fetchGroupUser(Connection conn,int groupId) throws SQLException{
		ArrayList<String> retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			retval = new ArrayList<String>();
			ps = conn.prepareStatement(FETCH_GROUP_USER);
			Misc.setParamInt(ps, groupId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				retval.add(rs.getString("email"));
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}
	public OrgMailingBean fetchOrgMailingDetail(Connection conn,int orgMailingId) throws SQLException{
		OrgMailingBean retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			retval = new OrgMailingBean();
			ps = conn.prepareStatement(FETCH_ORG_MAILING_DETAIL);
			Misc.setParamInt(ps, orgMailingId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				retval.setId(Misc.getRsetInt(rs, "id"));
				retval.setShortCode(rs.getString("short_code"));
				retval.setName(rs.getString("name"));
				retval.setMailFrom(rs.getString("mail_from"));
				retval.setMailLogo(rs.getString("mail_logo"));
				retval.setSmtpHost(rs.getString("mail_smtp_host"));
				retval.setSmtpPort(Misc.getRsetInt(rs, "mail_smtp_port"));
				retval.setSmtpUser(rs.getString("mail_smtp_user"));
				retval.setSmtpPassword(rs.getString("mail_smtp_password"));
				retval.setWebsite(rs.getString("website"));
				retval.setContactNo(rs.getString("contact_no"));
				retval.setBody(rs.getString("body"));
				retval.setSupportMail(rs.getString("email"));
				retval.setAddress(rs.getString("address"));
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}
	public HashMap<String,AutoEmailBean> getLookup(Connection conn) throws SQLException{
		HashMap<String,AutoEmailBean> retval = new HashMap<String, AutoEmailBean>();
		try {
			Date current = new Date(System.currentTimeMillis());
			//for daily reports
			current.setHours(0);
			current.setMinutes(0);
			current.setSeconds(0);
			setLookup(retval, conn, Misc.SCOPE_DAY, current.getTime());

			//for weakly reports
			current = TimePeriodHelper.getBegOfDate(current, Misc.SCOPE_WEEK);
			setLookup(retval, conn, Misc.SCOPE_WEEK, current.getTime());

			//for monthly reports
			current.setTime(System.currentTimeMillis());
			current.setHours(0);
			current.setMinutes(0);
			current.setSeconds(0);
			current = TimePeriodHelper.getBegOfDate(current, Misc.SCOPE_MONTH);
			setLookup(retval, conn, Misc.SCOPE_MONTH, current.getTime());

			//for yearly reports
			current.setTime(System.currentTimeMillis());
			current.setHours(0);
			current.setMinutes(0);
			current.setSeconds(0);
			current = TimePeriodHelper.getBegOfDate(current, Misc.SCOPE_ANNUAL);
			setLookup(retval, conn, Misc.SCOPE_ANNUAL, current.getTime());
		}catch(Exception e){
			e.printStackTrace();
		}
		return retval;

	}
	public void setLookup(HashMap<String,AutoEmailBean> lookup,Connection conn,int granularity,long scheduledTime) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		AutoEmailBean mailBean = null;
		try{
			ps = conn.prepareStatement(FETCH_DAILY_EMAIL_INFORMATION);
			ps.setTimestamp(1, Misc.longToSqlDate(scheduledTime));
			Misc.setParamInt(ps, granularity, 2);
			rs = ps.executeQuery();
			while(rs.next())
			{
				mailBean = new AutoEmailBean();
				mailBean.setMailId(Misc.getRsetInt(rs, "id"));
				mailBean.setReportId(Misc.getRsetInt(rs, "report_id"));
				mailBean.setReportName(rs.getString("report_name"));
				mailBean.setReportFormat(Misc.getRsetInt(rs, "report_type"));
				mailBean.setGranularity(Misc.getRsetInt(rs, "granularity"));
				mailBean.setGroupId(Misc.getRsetInt(rs, "group_id"));
				mailBean.setRecipient(fetchGroupUser(conn, mailBean.getGroupId()));
				mailBean.setStatus(Misc.getRsetInt(rs, "status"));
				mailBean.setSubject(rs.getString("subject"));
				mailBean.setCreateDate(rs.getTimestamp("create_date").getTime());
				mailBean.setScheduleDate(rs.getTimestamp("scheduled_date").getTime());
				mailBean.setSendingDate(rs.getTimestamp("sending_date").getTime());
				mailBean.setFrequencyId(Misc.getRsetInt(rs,"frequency_id"));
				mailBean.setOrgMailingBean(fetchOrgMailingDetail(conn,Misc.getRsetInt(rs,"org_mailing_id")));
				mailBean.setNoData(Misc.getRsetInt(rs,"no_data"));
				String key = (mailBean.getGroupId())+("#"+mailBean.getReportId())+("#"+mailBean.getFrequencyId());;
				lookup.put(key, mailBean);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
		}
	}
}


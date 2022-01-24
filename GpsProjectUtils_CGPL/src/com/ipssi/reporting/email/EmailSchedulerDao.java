package com.ipssi.reporting.email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.reporting.common.db.DBQueries;
import com.ipssi.reporting.customize.MenuBean;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailGroup;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailInfo;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailStatusDetail;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailUser;
import com.ipssi.reporting.email.EmailSchedulerInformation.ExcelTemplate;
import com.ipssi.reporting.email.EmailSchedulerInformation.Frequency;
import com.ipssi.reporting.email.EmailSchedulerInformation.OrgMailingProfile;
import com.ipssi.reporting.email.EmailSchedulerInformation.Report;
import com.ipssi.reporting.email.EmailSchedulerInformation.ReportSpan;

public class EmailSchedulerDao {
	private static final String FETCH_EMAIL_SCHEDULE = 
			" select "
					+ " email_report_info.id,email_report_info.report_format,email_report_info.org_mailing_id,email_report_info.no_data, "
					+ " email_report_info.subject,email_report_info.body,email_report_info.status,"
					+ " email_report_groups.id,email_report_groups.status,email_report_groups.name,email_report_groups.notes,"
					+ " report_definitions.id,report_definitions.page_context,report_definitions.name,"
					+ " report_definitions.type,report_definitions.status,report_definitions.for_port_node_id,"
					+ " report_definitions.for_user_id,report_definitions.master_menu_id,report_definitions.help,"
					+ " email_report_span.id, email_report_span.status,email_report_span.granularity,email_report_span.start_hr,"
					+ " email_report_span.start_min,email_report_span.rel_start,email_report_span.rel_end,email_report_span.name,email_report_span.notes,"
					+ " email_report_frequencies.id,email_report_frequencies.start_date, email_report_frequencies.hours,email_report_frequencies.minutes, "
					+ " email_report_frequencies.granularity,email_report_frequencies.send_freq,email_report_frequencies.daily_mail_limit,email_report_frequencies.expire_after,email_report_frequencies.status,email_report_frequencies.name,email_report_frequencies.status,"
					+ " excel_template.id,excel_template.name,excel_template.url,email_report_info.port_node_id "
					+ " from email_report_info "
					+ " left outer join report_definitions on (email_report_info.report_id=report_definitions.id) "
					+ " left outer join email_report_span on (email_report_info.report_span_id=email_report_span.id) "
					+ " left outer join email_report_frequencies on (email_report_info.frequency_id=email_report_frequencies.id) "
					+ " left outer join excel_template on (email_report_info.template_id=excel_template.id)"
					+ " left outer join email_report_groups on (email_report_info.group_id=email_report_groups.id) where email_report_info.status=1";
	private static final String INSERT_EMAIL_STATUS  = "insert into email_status_detail(email_info_id,status,create_date,scheduled_date,sending_date,error) values (?,?,?,?,?,?)";
	private static final String UPDATE_EMAIL_STATUS = "update email_status_detail set status=? , sending_date=?, error=? where id=?";
	private static final String FETCH_EMAIL_STATUS = "select id,email_info_id,status,create_date,scheduled_date,sending_date,error from email_status_detail";
	private static final String FETCH_LATEST_EMAIL_STATUS = "select email_status_detail.id,email_info_id,status,create_date,scheduled_date,sending_date,error from email_status_detail join (select max(id) id from email_status_detail group by email_info_id ) le on (email_status_detail.id=le.id)";
	private static final String FETCH_GROUP_USER = " select email_report_group_id,name,email,type,status,mobile from email_report_users where email_report_group_id=?";
	private static final String FETCH_ORG_MAILING_DETAIL = " select id, name, short_code, mail_from, mail_logo, mail_smtp_host,mail_smtp_port,mail_smtp_user,mail_smtp_password,contact_no,website,body,updated_on,email,address from org_mailing_params where id=? ";
	
	private static final String INSERT_EMAIL_INFO = "insert into email_report_info (report_id, group_id, report_span_id, frequency_id, report_format, org_mailing_id, no_data, template_id, subject, body, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_EMAIL_INFO = "update email_report_info set report_id=?, group_id=?, report_span_id=?, frequency_id=?, report_format=?, org_mailing_id=?, no_data=?, template_id=?, subject=?, body=?, status=? where id=?";
	private static final String DELETE_EMAIL_INFO = "update email_report_info set status status=? where id=?";
	private static final String INSERT_EMAIL_GROUPS = "insert into email_report_groups (name, status, notes ) values (?, ?, ?)";
	private static final String UPDATE_EMAIL_GROUPS = "update email_report_groups set name=?, status=?, notes=? where id=?";
	private static final String DELETE_EMAIL_GROUPS = "update email_report_groups set status=? where id=?";
	private static final String INSERT_EMAIL_REPORT_SPAN = "insert into email_report_span (name,status, granularity, start_hr, start_min, rel_start, rel_end, updated_on, notes) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_EMAIL_REPORT_SPAN = "update email_report_span set name=?,status=?, granularity=?, start_hr=?, start_min=?, rel_start=?, rel_end=?, updated_on=?, notes=? where id=?";
	private static final String DELETE_EMAIL_REPORT_SPAN = "update email_report_span set status=? where id=?";
	private static final String INSERT_EMAIL_REPORT_USERS = "insert into email_report_users (email_report_group_id, email, status, type, name, mobile) values (?, ?, ?, ?, ?, ?)";
	private static final String DELETE_EMAIL_REPORT_USERS = "delete from email_report_users where email_report_group_id=?";
	private static final String INSERT_EMAIL_REPORT_FREQ = "insert into email_report_frequencies (name, granularity, hours, minutes, start_date, send_freq, daily_mail_limit, expire_after, status, notes) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_EMAIL_REPORT_FREQ = "update email_report_frequencies set name=?, email_report_group_id=?, granularity=?, hours=?, minutes=?, start_date=?, send_freq=?, daily_mail_limit=?, expire_after=?, status=?, notes=? where id=?";
	private static final String DELETE_EMAIL_REPORT_FREQ = "update email_report_frequencies set status=? where id=?";
	
	public EmailInfo fetchEmailScheduleInfoById(Connection conn,int emailInfoId) throws Exception{
		if(Misc.isUndef(emailInfoId))
			return null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		EmailInfo mailBean = null;
		String query = FETCH_EMAIL_SCHEDULE + " and email_report_info.id=?";
		try{
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, emailInfoId,1);
			rs = ps.executeQuery();
			if(rs.next())
			{
				int count=0;
				mailBean = new EmailInfo();
				mailBean.setId(Misc.getRsetInt(rs, ++count));
				mailBean.setReportFormat(Misc.getRsetInt(rs, ++count));
				mailBean.setOrgMailingId(Misc.getRsetInt(rs, ++count));
				mailBean.setSendNoData(Misc.getRsetInt(rs, ++count) == 1);
				mailBean.setSubject(rs.getString(++count));
				mailBean.setBody(rs.getString(++count));
				mailBean.setStatus(Misc.getRsetInt(rs, ++count));
				mailBean.setGroup(new EmailGroup(Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), rs.getString(++count), rs.getString(++count)));
				mailBean.setReport(new Report(Misc.getRsetInt(rs, ++count), rs.getString(++count), rs.getString(++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), rs.getString(++count)));//
				mailBean.setReportSpan(new ReportSpan(Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetDouble(rs, ++count), Misc.getRsetDouble(rs, ++count), rs.getString(++count), rs.getString(++count)));//
				mailBean.setFrequency(new Frequency(Misc.getRsetInt(rs, ++count), Misc.getDateInLong(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count),Misc.getRsetInt(rs, ++count),Misc.getRsetInt(rs, ++count),Misc.getRsetInt(rs, ++count), Misc.getDateInLong(rs, ++count),Misc.getRsetInt(rs, ++count), rs.getString(++count), rs.getString(++count)));//
				mailBean.setTemplateId(new ExcelTemplate(Misc.getRsetInt(rs, ++count),rs.getString(++count),rs.getString(++count)));
				mailBean.setPortNodeId(Misc.getRsetInt(rs, ++count));
				mailBean.getGroup().setEmailUserList(fetchGroupUser(conn,mailBean.getGroup().getId()));
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return mailBean;
	
	}
	public ArrayList<EmailInfo> fetchEmailScheduleInfo(Connection conn) throws Exception{
		ArrayList<EmailInfo> retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		EmailInfo mailBean = null;
		String query = FETCH_EMAIL_SCHEDULE;
		try{
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next())
			{
				int count=0;
				mailBean = new EmailInfo();
				mailBean.setId(Misc.getRsetInt(rs, ++count));
				mailBean.setReportFormat(Misc.getRsetInt(rs, ++count));
				mailBean.setOrgMailingId(Misc.getRsetInt(rs, ++count));
				mailBean.setSendNoData(Misc.getRsetInt(rs, ++count) == 1);
				mailBean.setSubject(rs.getString(++count));
				mailBean.setBody(rs.getString(++count));
				mailBean.setStatus(Misc.getRsetInt(rs, ++count));
				mailBean.setGroup(new EmailGroup(Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), rs.getString(++count), rs.getString(++count)));
				mailBean.setReport(new Report(Misc.getRsetInt(rs, ++count), rs.getString(++count), rs.getString(++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), rs.getString(++count)));//
				mailBean.setReportSpan(new ReportSpan(Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetDouble(rs, ++count), Misc.getRsetDouble(rs, ++count), rs.getString(++count), rs.getString(++count)));//
				mailBean.setFrequency(new Frequency(Misc.getRsetInt(rs, ++count), Misc.getDateInLong(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count),Misc.getRsetInt(rs, ++count),Misc.getRsetInt(rs, ++count),Misc.getRsetInt(rs, ++count), Misc.getDateInLong(rs, ++count),Misc.getRsetInt(rs, ++count), rs.getString(++count), rs.getString(++count)));//
				mailBean.setTemplateId(new ExcelTemplate(Misc.getRsetInt(rs, ++count),rs.getString(++count),rs.getString(++count)));
				mailBean.setPortNodeId(Misc.getRsetInt(rs, ++count));
				mailBean.getGroup().setEmailUserList(fetchGroupUser(conn,mailBean.getGroup().getId()));
				if(retval == null)
					retval = new ArrayList<EmailSchedulerInformation.EmailInfo>();
				retval.add(mailBean);
					
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	
	public boolean insertEmailInformation(Connection conn, EmailStatusDetail mailStatusDetail, StringBuilder errorStr) throws Exception{		
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		if(mailStatusDetail == null)
			return false;
		try {
			if (mailStatusDetail != null){
				stmt = conn.prepareStatement(INSERT_EMAIL_STATUS);
				Misc.setParamInt(stmt, mailStatusDetail.getEmailInfoId(),1);
				Misc.setParamInt(stmt,mailStatusDetail.getStatus(),2);
				stmt.setTimestamp(3, Misc.longToSqlDate(mailStatusDetail.getCreateDate()));
				stmt.setTimestamp(4, Misc.longToSqlDate(mailStatusDetail.getScheduledDate()));
				stmt.setTimestamp(5, Misc.longToSqlDate(mailStatusDetail.getSendingDate()));
				stmt.setString(6, errorStr != null ? errorStr.toString() : null);
				stmt.addBatch(); 
			}
			insertStatus = stmt.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(stmt);
		}
		return insertStatus;
	}		
	public EmailStatusDetail fetchEmailStatusByEmailInfoId(Connection conn,int emailInfoId,long scheduledDate) throws Exception{
		EmailStatusDetail retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		if(Misc.isUndef(scheduledDate) || Misc.isUndef(emailInfoId))
			return null;
		try {
			ps = conn.prepareStatement(FETCH_LATEST_EMAIL_STATUS+" where scheduled_date >= ? and email_info_id=?");
			ps.setTimestamp(1, new Timestamp(scheduledDate - 60*1000));
			Misc.setParamInt(ps, emailInfoId, 2);
			rs = ps.executeQuery();
			if(rs.next()){
				int count =0;
				retval = new EmailStatusDetail(Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getDateInLong(rs, ++count), Misc.getDateInLong(rs, ++count), Misc.getDateInLong(rs, ++count));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public ArrayList<EmailStatusDetail> fetchLatestEmailStatus(Connection conn) throws Exception{
		ArrayList<EmailStatusDetail> retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		EmailStatusDetail mailStatusBean = null;
		try {
			ps = conn.prepareStatement(FETCH_LATEST_EMAIL_STATUS);
			rs = ps.executeQuery();
			while(rs.next()){
				int count =0;
				mailStatusBean = new EmailStatusDetail(Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getRsetInt(rs, ++count), Misc.getDateInLong(rs, ++count), Misc.getDateInLong(rs, ++count), Misc.getDateInLong(rs, ++count));
				if(retval == null)
					retval = new ArrayList<EmailSchedulerInformation.EmailStatusDetail>();
				retval.add(mailStatusBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean updateEmailStatusDetails(Connection conn,EmailStatusDetail mailStatusDetail,StringBuilder error) throws Exception{		
		boolean updateStatus = false;
		PreparedStatement stmt=null;
		if(mailStatusDetail == null)
			return false;
		try {
			if (mailStatusDetail != null){
				stmt = conn.prepareStatement(UPDATE_EMAIL_STATUS);
				Misc.setParamInt(stmt, mailStatusDetail.getStatus(),1);
				stmt.setTimestamp(2, Misc.longToSqlDate(System.currentTimeMillis()));
				stmt.setString(3, error == null ? null : error.toString());
				Misc.setParamInt(stmt, mailStatusDetail.getEmailInfoId(),4);
			}
			updateStatus = stmt.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(stmt);
		}
		return updateStatus;
	}

	public ArrayList<EmailUser> fetchGroupUser(Connection conn,int groupId) throws SQLException{
		ArrayList<EmailUser> retval = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			ps = conn.prepareStatement(FETCH_GROUP_USER);
			Misc.setParamInt(ps, groupId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				if(retval == null)
					retval = new ArrayList<EmailSchedulerInformation.EmailUser>();
				retval.add(new EmailUser(Misc.getRsetInt(rs, 1), rs.getString(2), rs.getString(3), Misc.getRsetInt(rs, 4), Misc.getRsetInt(rs, 5),rs.getString(6)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public OrgMailingProfile fetchOrgMailingDetail(Connection conn,int orgMailingId) throws SQLException{
		OrgMailingProfile retval = new OrgMailingProfile();
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			ps = conn.prepareStatement(FETCH_ORG_MAILING_DETAIL);
			Misc.setParamInt(ps, orgMailingId,1);
			rs = ps.executeQuery();
			if(rs.next()){
				retval  = new OrgMailingProfile(Misc.getRsetInt(rs, "id"), rs.getString("short_code"), rs.getString("name"), rs.getString("mail_from"), rs.getString("mail_logo"), rs.getString("website"), rs.getString("address"), rs.getString("body"), rs.getString("contact_no"), rs.getString("mail_smtp_host"), Misc.getRsetInt(rs, "mail_smtp_port"), rs.getString("mail_smtp_user"), rs.getString("mail_smtp_password"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public MenuBean getMenuById(Connection conn, int menuId) throws Exception {
		String fetchMenu = DBQueries.CUSTOMIZE.FETCH_MENU;
		ResultSet rs = null;
		MenuBean menuBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchMenu);
			contSt.setInt(1, menuId);
			rs = contSt.executeQuery();
				while (rs.next()) {
					menuBean = new MenuBean();
					menuBean.setId(rs.getInt("id"));
					menuBean.setPortNodeId(Misc.getRsetInt(rs,"port_node_id"));
					menuBean.setUserId(Misc.getRsetInt(rs,"user_id"));
					menuBean.setMenuTag(rs.getString("menu_tag"));
					menuBean.setComponentFile(rs.getString("component_file"));
				}
				rs.close();
				contSt.close();
		} catch (Exception ex) {
			throw ex;
		}
		return menuBean;

	}
	
	public int insertEmailReportInfo(Connection conn,EmailInfo emailInfo) throws Exception {
		//report_id, group_id, report_span_id, frequency_id, report_format, org_mailing_id, no_data, template_id, subject, body, status
		int retval = Misc.getUndefInt();
		PreparedStatement ps=null;
		ResultSet rs = null;
		if(emailInfo == null)
			return Misc.getUndefInt();
		try {
				ps = conn.prepareStatement(INSERT_EMAIL_INFO);
				Misc.setParamInt(ps, emailInfo.getReport() != null ? emailInfo.getReport().getId() : Misc.getUndefInt(),1);
				Misc.setParamInt(ps, emailInfo.getGroup() != null ? emailInfo.getGroup().getId() : Misc.getUndefInt(),2);
				Misc.setParamInt(ps, emailInfo.getReportSpan() != null ? emailInfo.getReportSpan().getId() : Misc.getUndefInt(),3);
				Misc.setParamInt(ps, emailInfo.getFrequency() != null ? emailInfo.getFrequency().getId() : Misc.getUndefInt(),4);
				Misc.setParamInt(ps,emailInfo.getReportFormat(),5);
				Misc.setParamInt(ps,emailInfo.getOrgMailingId(),6);
				Misc.setParamInt(ps,emailInfo.isSendNoData() ? 1 : 0,7);
				Misc.setParamInt(ps,emailInfo.getTemplate() != null ? emailInfo.getTemplate().getId() : Misc.getUndefInt(),8);
				ps.setString(9, emailInfo.getSubject());
				ps.setString(10, emailInfo.getBody());
				Misc.setParamInt(ps,emailInfo.getStatus(),11);
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if(rs.next())
					retval = Misc.getRsetInt(rs, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean updateEmailReportInfo(Connection conn,EmailInfo emailInfo) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(emailInfo == null || Misc.isUndef(emailInfo.getId()))
			return false;
		try {
				ps = conn.prepareStatement(UPDATE_EMAIL_INFO);
				Misc.setParamInt(ps, emailInfo.getReport() != null ? emailInfo.getReport().getId() : Misc.getUndefInt(),1);
				Misc.setParamInt(ps, emailInfo.getGroup() != null ? emailInfo.getGroup().getId() : Misc.getUndefInt(),2);
				Misc.setParamInt(ps, emailInfo.getReportSpan() != null ? emailInfo.getReportSpan().getId() : Misc.getUndefInt(),3);
				Misc.setParamInt(ps, emailInfo.getFrequency() != null ? emailInfo.getFrequency().getId() : Misc.getUndefInt(),4);
				Misc.setParamInt(ps,emailInfo.getReportFormat(),5);
				Misc.setParamInt(ps,emailInfo.getOrgMailingId(),6);
				Misc.setParamInt(ps,emailInfo.isSendNoData() ? 1 : 0,7);
				Misc.setParamInt(ps,emailInfo.getTemplate() != null ? emailInfo.getTemplate().getId() : Misc.getUndefInt(),8);
				ps.setString(9, emailInfo.getSubject());
				ps.setString(10, emailInfo.getBody());
				Misc.setParamInt(ps,emailInfo.getStatus(),11);
				Misc.setParamInt(ps,emailInfo.getId(),12);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean deleteEmailReportInfo(Connection conn,int emailInfoId) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(Misc.isUndef(emailInfoId))
			return false;
		try {
				ps = conn.prepareStatement(DELETE_EMAIL_INFO);
				Misc.setParamInt(ps,0,1);
				Misc.setParamInt(ps,emailInfoId,2);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public int insertEmailGroup(Connection conn,EmailGroup emailGroup) throws Exception {
		//name, status, notes
		int retval = Misc.getUndefInt();
		PreparedStatement ps=null;
		ResultSet rs = null;
		if(emailGroup == null)
			return Misc.getUndefInt();
		try {
				ps = conn.prepareStatement(INSERT_EMAIL_GROUPS);
				ps.setString(1, emailGroup.getName());
				Misc.setParamInt(ps,emailGroup.getStatus(),2);
				ps.setString(3, emailGroup.getNotes());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if(rs.next())
					retval = Misc.getRsetInt(rs, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean updateEmailReportGroup(Connection conn,EmailGroup emailGroup) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(emailGroup == null || Misc.isUndef(emailGroup.getId()))
			return retval;
		try {
				ps = conn.prepareStatement(UPDATE_EMAIL_GROUPS);
				ps.setString(1, emailGroup.getName());
				Misc.setParamInt(ps,emailGroup.getStatus(),2);
				ps.setString(3, emailGroup.getNotes());
				Misc.setParamInt(ps,emailGroup.getId(),4);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean deleteEmailReportGroup(Connection conn,int emailGroupId) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(Misc.isUndef(emailGroupId))
			return retval;
		try {
				ps = conn.prepareStatement(DELETE_EMAIL_GROUPS);
				Misc.setParamInt(ps,0,1);
				Misc.setParamInt(ps,emailGroupId,2);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public int insertEmailReportSpan(Connection conn,ReportSpan emailReportSpan) throws Exception {
		//name,status, granularity, start_hr, start_min, rel_start, rel_end, updated_on, notes
		int retval = Misc.getUndefInt();
		PreparedStatement ps=null;
		ResultSet rs = null;
		if(emailReportSpan == null)
			return Misc.getUndefInt();
		try {
				ps = conn.prepareStatement(INSERT_EMAIL_REPORT_SPAN);
				ps.setString(1, emailReportSpan.getName());
				Misc.setParamInt(ps,emailReportSpan.getStatus(),2);
				Misc.setParamInt(ps,emailReportSpan.getGranularity(),3);
				Misc.setParamInt(ps,emailReportSpan.getStartHr(),4);
				Misc.setParamInt(ps,emailReportSpan.getStartMin(),5);
				Misc.setParamDouble(ps,emailReportSpan.getRelativeSt(),6);
				Misc.setParamDouble(ps,emailReportSpan.getRelativeEn(),7);
				ps.setTimestamp(8, Misc.longToSqlDate(System.currentTimeMillis()));
				ps.setString(9, emailReportSpan.getNotes());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if(rs.next())
					retval = Misc.getRsetInt(rs, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean updateEmailReportSpan(Connection conn,ReportSpan emailReportSpan) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(emailReportSpan == null || Misc.isUndef(emailReportSpan.getId()))
			return retval;
		try {
				ps = conn.prepareStatement(UPDATE_EMAIL_REPORT_SPAN);
				ps.setString(1, emailReportSpan.getName());
				Misc.setParamInt(ps,emailReportSpan.getStatus(),2);
				Misc.setParamInt(ps,emailReportSpan.getGranularity(),3);
				Misc.setParamInt(ps,emailReportSpan.getStartHr(),4);
				Misc.setParamInt(ps,emailReportSpan.getStartMin(),5);
				Misc.setParamDouble(ps,emailReportSpan.getRelativeSt(),6);
				Misc.setParamDouble(ps,emailReportSpan.getRelativeEn(),7);
				ps.setTimestamp(8, Misc.longToSqlDate(System.currentTimeMillis()));
				ps.setString(9, emailReportSpan.getNotes());
				Misc.setParamInt(ps,emailReportSpan.getId(),10);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean deleteEmailReportSpan(Connection conn,int emailReportSpanId) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(Misc.isUndef(emailReportSpanId))
			return retval;
		try {
				ps = conn.prepareStatement(DELETE_EMAIL_REPORT_SPAN);
				Misc.setParamInt(ps, 0, 1);
				Misc.setParamInt(ps,emailReportSpanId,2);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean insertEmailReportUsers(Connection conn,EmailUser emailUser, int emailGroupId) throws Exception {
		//email_report_group_id, email, status, type, name, mobile
		boolean retval = false;
		PreparedStatement ps=null;
		if(emailUser == null || Misc.isUndef(emailGroupId))
			return false;
		try {
				ps = conn.prepareStatement(INSERT_EMAIL_REPORT_USERS);
				Misc.setParamInt(ps,emailGroupId,1);
				ps.setString(2, emailUser.getEmail());
				Misc.setParamInt(ps,emailUser.getStatus(),3);
				Misc.setParamInt(ps,emailUser.getType(),4);
				ps.setString(5, emailUser.getName());
				ps.setString(6, emailUser.getMobile());
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}		
		return retval;
	}
	public boolean deleteEmailReportUsers(Connection conn,int emailReportGroupId) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(Misc.isUndef(emailReportGroupId))
			return retval;
		try {
				ps = conn.prepareStatement(DELETE_EMAIL_REPORT_USERS);
				Misc.setParamInt(ps,emailReportGroupId,1);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public int insertEmailReportFreq(Connection conn, Frequency emailReportFreq) throws Exception {
		//name, email_report_group_id, granularity, hours, minutes, start_date, send_freq, daily_mail_limit, expire_after, status, notes
		int retval = Misc.getUndefInt();
		PreparedStatement ps=null;
		ResultSet rs = null;
		if(emailReportFreq == null)
			return Misc.getUndefInt();
		try {
				ps = conn.prepareStatement(INSERT_EMAIL_REPORT_FREQ);
				ps.setString(1, emailReportFreq.getName());
				Misc.setParamInt(ps,emailReportFreq.getGranularity(),2);
				Misc.setParamInt(ps,emailReportFreq.getDailyStartHr(),3);
				Misc.setParamInt(ps,emailReportFreq.getDailyStartMin(),4);
				ps.setTimestamp(5, Misc.longToSqlDate(emailReportFreq.getStartDate()));
				Misc.setParamInt(ps,emailReportFreq.getSendFreq(),6);
				Misc.setParamInt(ps,emailReportFreq.getDailyMailLimit(),7);
				ps.setTimestamp(8, Misc.longToSqlDate(emailReportFreq.getExpireAfter()));
				Misc.setParamInt(ps,emailReportFreq.getStatus(),9);
				ps.setString(10, emailReportFreq.getNotes());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if(rs.next())
					retval = Misc.getRsetInt(rs, 1);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean updateEmailReportFreq(Connection conn,Frequency emailReportFreq) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(emailReportFreq == null || Misc.isUndef(emailReportFreq.getId()))
			return retval;
		try {
				ps = conn.prepareStatement(UPDATE_EMAIL_REPORT_FREQ);
				ps.setString(1, emailReportFreq.getName());
				Misc.setParamInt(ps,emailReportFreq.getGranularity(),2);
				Misc.setParamInt(ps,emailReportFreq.getDailyStartHr(),3);
				Misc.setParamInt(ps,emailReportFreq.getDailyStartMin(),4);
				ps.setTimestamp(5, Misc.longToSqlDate(emailReportFreq.getStartDate()));
				Misc.setParamInt(ps,emailReportFreq.getSendFreq(),6);
				Misc.setParamInt(ps,emailReportFreq.getDailyMailLimit(),7);
				ps.setTimestamp(8, Misc.longToSqlDate(emailReportFreq.getExpireAfter()));
				Misc.setParamInt(ps,emailReportFreq.getStatus(),9);
				ps.setString(10, emailReportFreq.getNotes());
				Misc.setParamInt(ps,emailReportFreq.getId(),11);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
	public boolean deleteEmailReportFreq(Connection conn,int emailReportFreqId) throws Exception {
		boolean retval = false;
		PreparedStatement ps=null;
		if(Misc.isUndef(emailReportFreqId))
			return retval;
		try {
				ps = conn.prepareStatement(DELETE_EMAIL_REPORT_FREQ);
				Misc.setParamInt(ps,0,1);
				Misc.setParamInt(ps,emailReportFreqId,2);
				retval = ps.executeUpdate() > 0;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			Misc.closePS(ps);
		}
		return retval;
	}
}


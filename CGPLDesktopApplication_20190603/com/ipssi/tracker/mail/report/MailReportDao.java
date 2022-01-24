package com.ipssi.tracker.mail.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.db.DBQueries;

public class MailReportDao {
	
	public static void saveReportMailInfo(Connection conn , MailReportBean mailBean ,ArrayList<MailReportBean> reportBeanList)
	{
		Timestamp updatedOn = new Timestamp((new Date()).getTime());
		try {
			PreparedStatement ps  = conn.prepareStatement(DBQueries.MailReport.INSERT_REPORT_MAILING) ;
			ps.setString(1, mailBean.getName());
			ps.setInt(2,mailBean.getType());
			ps.setInt(3,mailBean.getMailSize());
			ps.setInt(4,mailBean.getMargeMail());
			ps.setInt(5, mailBean.getSchedule());
			ps.setInt(6, mailBean.getScheduleHour());
			ps.setInt(7, mailBean.getScheduleDay());
			ps.setInt(8, 1);
			ps.setTimestamp(9, updatedOn);	
			ps.executeUpdate();
			ResultSet rs  = ps.getGeneratedKeys();
			if(rs.next())
			{
				int id = rs.getInt(1);
				PreparedStatement pst = conn.prepareStatement(DBQueries.MailReport.INSERT_REPORT_MAILING_USERS);
				String [] arr =	mailBean.getUserMail();
				for(int i = 0 ; i < arr.length ; i++)
				{
					pst.setInt(1, id);
					pst.setInt(2,Misc.getParamAsInt(arr[i]));
					pst.executeUpdate();
				}
				PreparedStatement psr = conn.prepareStatement(DBQueries.MailReport.INSERT_REPORT_MAILING_LIST);
				if(reportBeanList != null)
				{
					for (Iterator<MailReportBean> iterator = reportBeanList.iterator(); iterator.hasNext();) {
						MailReportBean reportBean = (MailReportBean) iterator.next();
						psr.setInt(1, id);
						psr.setInt(2,reportBean.getReport());
						psr.setString(3, reportBean.getSearchXml());
						psr.setString(4, reportBean.getSubject());
						psr.executeUpdate();
					}
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static ArrayList<MailReportBean> getCustomer(Connection conn)
	{
		ArrayList<MailReportBean> customerList = new ArrayList<MailReportBean>();
		try {
			
			PreparedStatement ps  = conn.prepareStatement(DBQueries.MailReport.FETCH_CUST_LIST);
			ResultSet rs  = ps.executeQuery();
			while(rs.next())
			{
				MailReportBean custBean = new MailReportBean();
				custBean.setContactId(rs.getInt("id"));
				custBean.setCustId(rs.getInt("customer_id"));
				custBean.setCustName(rs.getString("contact_name"));
				customerList.add(custBean);
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return customerList;
		
	}
	public static ArrayList<MailReportBean> getMailingReportList(Connection conn)
	{
		ArrayList<MailReportBean> mailReportList = new ArrayList<MailReportBean>();
		try {
			
			PreparedStatement ps  = conn.prepareStatement(DBQueries.MailReport.FETCH_MAILING_REPORT_LIST);
			ResultSet rs  = ps.executeQuery();
			while(rs.next())
			{
				MailReportBean mailReportBean = new MailReportBean();
				mailReportBean.setMailReportId(rs.getInt("id"));
				mailReportBean.setName(rs.getString("name_"));
				mailReportBean.setReport(rs.getInt("report_type"));
				mailReportList.add(mailReportBean);
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mailReportList;
		
	}


public static ArrayList<MailReportBean> getMailingReport(Connection conn , int id)
{
	ArrayList<MailReportBean> reportList = new ArrayList<MailReportBean>();
	try {
		
		PreparedStatement ps  = conn.prepareStatement(DBQueries.MailReport.FETCH_MAILING_REPORT);
		ps.setInt(1, id);
		ResultSet rs  = ps.executeQuery();
		while(rs.next())
		{
			MailReportBean mailReportBean = new MailReportBean();
			//mailReportBean.setMailReportId(rs.getInt("id"));
			mailReportBean.setName(rs.getString("name_"));
			mailReportBean.setReport(rs.getInt("report_type"));
			mailReportBean.setMailSize(rs.getInt("max_mail_size"));
			mailReportBean.setMargeMail(rs.getInt("send_all_report_in_single_mail"));
			mailReportBean.setSchedule(rs.getInt("schedule_type"));
			mailReportBean.setScheduleHour(rs.getInt("schedule_hour"));
			mailReportBean.setReport(rs.getInt("report_id"));
			mailReportBean.setSubject(rs.getString("report_subject"));
			reportList.add(mailReportBean);
			
			
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return reportList;
	
}

}

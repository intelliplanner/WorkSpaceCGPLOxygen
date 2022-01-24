package com.ipssi.tracker.mail.report;

import static com.ipssi.tracker.common.util.ApplicationConstants.*;
import static com.ipssi.tracker.common.util.Common.getParamAsString;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.tracker.vehicle.org.VehicleOrgBean;
import com.ipssi.tracker.vehicle.org.VehicleOrgDao;
import com.ipssi.tracker.web.ActionI;

public class MailReportAction implements ActionI {

	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		String action = "action";
		// TODO Auto-generated method stub
		Connection conn = InitHelper.helpGetDBConn(request);
		 action = request.getParameter("action") != null ? request.getParameter("action"):"action" ;
		String name = Misc.getParamAsString(request.getParameter("name"));
		String [] userMail = request.getParameterValues("userMail");
		//String userMail = Misc.getParamAsString(request.getParameter("userMail"));
		int reportType = Misc.getParamAsInt(request.getParameter("reportType"));
		int mailSize = Misc.getParamAsInt(request.getParameter("mailSize"));
		int schedule = Misc.getParamAsInt(request.getParameter("schedule"));
		int scheduleHour = Misc.getParamAsInt(request.getParameter("scheduleHour"));
		int scheduleDay = Misc.getParamAsInt(request.getParameter("scheduleDay"));
		int margeMail = Misc.getParamAsInt(request.getParameter("margeMail"));
		int id  = Misc.getParamAsInt(request.getParameter("mailReportId"));
		ArrayList<MailReportBean> reportBeanList = new  ArrayList<MailReportBean>();
		ArrayList<MailReportBean> mailReportList = new  ArrayList<MailReportBean>();
		ArrayList<MailReportBean> custList = MailReportDao.getCustomer(conn);
		request.setAttribute("custList",custList);
		if(action.equals(SAVE))
		{
			MailReportBean mailBean = new MailReportBean();
			mailBean.setName(name);
			mailBean.setMailSize(mailSize);
			mailBean.setSchedule(schedule);
			mailBean.setScheduleHour(scheduleHour);
			mailBean.setScheduleDay(scheduleDay);
			mailBean.setType(reportType);
			mailBean.setUserMail(userMail);
			mailBean.setMargeMail(margeMail);
			

			
			
			String xmlDoc = getParamAsString(request.getParameter("xmldata"));
			
			
			Document xmldoc = MyXMLHelper.loadFromString(xmlDoc);
			NodeList nlist = xmldoc.getElementsByTagName("d");
			int size = nlist.getLength();
			for (int i = 0; i < size; i++) {
				org.w3c.dom.Node n = nlist.item(i);
				
				Element e = (Element) n;
				//MailReportBean mailReportBean = new MailReportBean();
				if(e.hasAttributes())
				{
					MailReportBean mailReportBean = new MailReportBean();
				mailReportBean.setReport(Misc.getParamAsInt(e.getAttribute("report")));
				mailReportBean.setSearchXml(e.getAttribute("searchXml"));
				mailReportBean.setSerchOption(Misc.getParamAsInt(e.getAttribute("searchOption")));
				mailReportBean.setSubject(e.getAttribute("subject"));
				reportBeanList.add(mailReportBean);
				
				}
				
			
					
		
				
			}
			
			MailReportDao.saveReportMailInfo(conn, mailBean , reportBeanList);
			mailReportList = MailReportDao.getMailingReportList(conn);
			request.setAttribute("mailReportList", mailReportList);
			return "/mailReportView.jsp";
		}
		if(action.equals(EDIT))
		{
			ArrayList<MailReportBean> mailReport = MailReportDao.getMailingReport(conn, id);
			request.setAttribute("mailReport",mailReport );
		}
	
	
		return "/mailReport.jsp";	
		
	}

	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return "/mailReportView.jsp";
	}

}

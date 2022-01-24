package com.ipssi.reporting.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Common;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.User;
import com.ipssi.input.InputTemplate;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.customize.CustomizeDao;
import com.ipssi.reporting.customize.MenuBean;
import com.ipssi.reporting.email.EmailSchedulerDao;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailInfo;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailStatusDetail;
import com.ipssi.reporting.email.EmailSchedulerInformation.EmailUser;
import com.ipssi.reporting.email.EmailSchedulerInformation.OrgMailingProfile;
import com.ipssi.reporting.mail.AutoEmailBean;
import com.ipssi.reporting.mail.EmailSenderDao;
import com.ipssi.reporting.mail.MailSender;
import com.ipssi.reporting.mail.MailSenderNew;
import com.ipssi.reporting.trip.ExcelGenerator;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder;
import com.ipssi.reporting.trip.PrintDateTable;
import com.ipssi.reporting.trip.Table;
import com.ipssi.workflow.WorkflowHelper;

public class GenerateDataNew extends HttpServlet {
	private static final int TRIP_SUMMARY = 0;
	private static final int TRIP_PERFORMANCE = 1;
	private Logger logger = Logger.getLogger(GenerateData.class);

	private static final int NEW = -1;
	private static final int FAILED = 0;
	private static final int SENT = 1;
	private static final int WAIT = 2;
	private static final int NO_DATA = 3;

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		try {
			String action = Common.getParamAsString(request.getParameter("action"));
			boolean isMail = "1".equalsIgnoreCase(Common.getParamAsString(request.getParameter("mail")));
			com.ipssi.gen.utils.InitHelper.init(request, getServletContext()); //authenticate, session etc.
			if ("download".equalsIgnoreCase(action))
				processDownload(request, response);	
			else if ("xmlData".equalsIgnoreCase(action))
				processXMLData(request, response);
			else if ("config_file".equalsIgnoreCase(action))
				processConfigData(request, response);
			else if ("auto_email".equalsIgnoreCase(action)){
				if(isMail)
					processMailRequest(request, response);
				else
					processDownload(request, response);
			}else if("new_auto_email".equalsIgnoreCase(action)){
				processMailRequestLatest(request,response);
			}
			else
				processDownloadForPage(request, response);	
		} catch (Exception e) {
			e.printStackTrace();
			try {
				SessionManager session  = InitHelper.helpGetSession(request);
				Connection   _dbConnection = (Connection) request.getAttribute("_dbConnection");
				DBConnectionPool.returnConnectionToPoolNonWeb(_dbConnection, true);
				_dbConnection = null;
				request.removeAttribute("_dbConnection");
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}

		}
		finally{
			try {
				com.ipssi.gen.utils.InitHelper.close(request,getServletContext());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void processConfigData(HttpServletRequest request, HttpServletResponse response) {
		try {

			SessionManager session = InitHelper.helpGetSession(request);
			String pgContext = Misc.getParamAsString(session.getParameter("page_context"),"curr_op_status.xml");
			String frontPageName = Misc.getParamAsString(session.getParameter("front_page"),"curr_op_status.xml");
			FrontPageInfo fpi = CacheManager.getFrontPageConfig(session.getConnection() , Misc.getUndefInt(), Misc.getUndefInt(), pgContext, frontPageName, 0, 0);
			if(fpi == null)
				return;
			ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
			if(fpList == null || fpList.size() <= 0)
				return;
			for(DimConfigInfo dimConfig : fpList){
				if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) {
					continue;
				}
				if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.length() == 0 || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("Dummy")) {
					continue;
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processXMLData(HttpServletRequest request, HttpServletResponse response) {
		ServletOutputStream stream = null;
		GenerateDataDao genDao = null;
		boolean error = false;
		SessionManager _session = InitHelper.helpGetSession(request);
		int pv123 = Misc.getParamAsInt(_session.getParameter("orgId"), Misc.getUndefInt());
		String startTime = Misc.getParamAsString(_session.getParameter("startTime"),null);
		String endTime = Misc.getParamAsString(_session.getParameter("endTime"),null);
		String wbName = Misc.getParamAsString(_session.getParameter("wbName"),null);
		String opStation = Misc.getParamAsString(_session.getParameter("opStation"),null);
		String vehicleName = Misc.getParamAsString(_session.getParameter("vehicle"),null);
		int direction = Misc.getParamAsInt(_session.getParameter("direction"), 0);
		Date start = null, end = null;
		try {
			if(Misc.isUndef(pv123))
				pv123 = Misc.getUserTrackControlOrg(_session);
			if(Misc.isUndef(pv123) || pv123 == 1)
				error = true;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(startTime != null)
				start = (Date)sdf.parse(startTime);
			if(endTime != null)
				end = sdf.parse(endTime);
			stream = response.getOutputStream();
			response.setContentType("text/xml");
			genDao = new GenerateDataDao();
			genDao.getXMLData(_session.getConnection(), stream, start, end, opStation, wbName, vehicleName, pv123,direction,error);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processDownloadForPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		PrintWriter out = null;
		ServletOutputStream stream = null;
		ByteArrayOutputStream buffer = null;
		FrontPageInfo fPageInfo = null;
		com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = null;
		Connection _dbConnection = InitHelper.helpGetDBConn(request);
		SessionManager _session = InitHelper.helpGetSession(request);
		User _user = _session.getUser();
		int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); 
		String pgContext = Misc.getParamAsString(_session.getParameter("page_context"), "trip_summary_report");
		String templateName = Misc.getParamAsString(_session.getParameter("template_name"), null);
		String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "trip_summary.xml");
		String reportName = Misc.getParamAsString(_session.getParameter("page_name"), "Trip Summary Report");
		int reportType = Misc.getParamAsInt(_session.getParameter("reportType"), 1);
		int pageType = Misc.getParamAsInt(_session.getParameter("page_type"), 1);
		String contentType = reportType == Misc.PDF ? "application/pdf" : "application/xls";
		String fileName = Misc.getParamAsString(_session.getParameter("fileName"),reportName);
		_session.setAttribute("page_context", pgContext, false);
		_session.setAttribute("_main_page_context", null, false);
		if(fileName.indexOf(".pdf") > 0 || fileName.indexOf(".xls")>0){
			//do nothing
		}
		else{
			fileName = reportType == Misc.PDF ?reportName + ".pdf" : reportName + ".xls";
		}
		try {

			buffer = new ByteArrayOutputStream();
			if (templateName != null) {
				Cache _cache = _session.getCache();
				int portNodeId = //Misc.getParamAsInt(_session.getParameter("pv123"));
						Misc.getUserTrackControlOrg(_session);

				InputTemplate inputTemplate = InputTemplate.getTemplate(_cache, _dbConnection, pgContext, portNodeId, templateName, _session);
				WorkflowHelper.TableObjectInfo driverTableInfo = WorkflowHelper.getTableInfo(inputTemplate.getObjectType());
				String objectIdParam = inputTemplate == null || driverTableInfo == null ? "vehicle_id" : driverTableInfo.getParamName();

				int objectId = Misc.getParamAsInt(_session.getParameter(objectIdParam));
				String objectIds[] =request.getParameterValues(objectIdParam);
				ArrayList<Integer> objectIdsInt = new ArrayList<Integer>();
				if (objectIds != null && objectIds.length != 0) {
					HashMap<String, String> temp1 = new HashMap<String, String>();
					for (int i1=0,i1s=objectIds.length; i1<i1s; i1++) {
						if (!temp1.containsKey(objectIds[i1])) {
							temp1.put(objectIds[i1], objectIds[i1]);
							objectIdsInt.add(Misc.getParamAsInt(objectIds[i1]));
						}
					}
				}
				if (objectIdsInt.size() == 0)
					objectIdsInt.add(objectId);
				inputTemplate.callPrintInputBlockGeneric(_dbConnection, _session, objectIdsInt, null, buffer, reportType,reportName,Misc.getUndefInt(), null, true);
			}
			else {
				fPageInfo = CacheManager.getFrontPageConfig(_dbConnection,(int) _user.getUserId(), Misc.getUserTrackControlOrg(_session), pgContext, frontPageName,Misc.getParamAsInt(request.getParameter("row"),0), 0);
				searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, pgContext, fPageInfo.m_frontSearchCriteria, null);
				if (pageType == TRIP_PERFORMANCE) {
					PrintDateTable qb1 = new PrintDateTable();
					qb1.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, buffer, reportType, reportName,Misc.getUndefInt());
				}
				else{
					GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
					qb.printPage(_dbConnection , fPageInfo, _session , searchBoxHelper,null,buffer, reportType, reportName,Misc.getUndefInt(),null, null,null, null, null);
				}
			}

			//			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
			//			qb.printPage(_dbConnection , fPageInfo, _session , searchBoxHelper,null,buffer, reportType, reportName);

			if(buffer != null && buffer.size() > 0){
				stream = response.getOutputStream();
				response.setContentType(contentType);
				response.addHeader("Content-Disposition", "attachment; filename="+fileName);
				response.setContentLength(buffer.size());		     
				stream.write(buffer.toByteArray());
			}
			else{
				response.setContentType("text/html");
				out = response.getWriter();
				out.print("no Data");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (stream != null){
				stream.flush();
			}
			if (buffer != null){
				buffer.flush();
				buffer.close();
			}
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);

	}
	private boolean processDownload(HttpServletRequest request, HttpServletResponse response){
		PrintWriter out = null;
		ServletOutputStream stream = null;
		ByteArrayOutputStream buffer = null;
		FrontPageInfo fPageInfo = null;
		com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = null;
		Connection _dbConnection = InitHelper.helpGetDBConn(request);
		SessionManager _session = InitHelper.helpGetSession(request);
		User _user = _session.getUser();
		int emailInfoId = Misc.getParamAsInt(request.getParameter("emailInfoId"));
		int pv123 = Misc.getUndefInt();
		long scheduleDate = Misc.getParamAsLong(request.getParameter("scheduled_date"));
		String contentType = "text/html";
		String fileName = "report.xls";
		try {
			if(!Misc.isUndef(emailInfoId)){
				int privIdForOrg = _user.getPrivToCheckForOrg(_session, null);	
				buffer = new ByteArrayOutputStream();
				EmailSchedulerDao emailSchedulerDao = new EmailSchedulerDao();
				EmailInfo emailInfo = emailSchedulerDao.fetchEmailScheduleInfoById(_dbConnection, emailInfoId);
				if(emailInfo != null && emailInfo.isValidEmailInfo(_dbConnection,pv123) ){
					MenuBean menuBean = (new CustomizeDao()).getMenuById(_dbConnection, emailInfo.getReport().getMenuId());
					String reportName = emailInfo.getReport().getName();
					int reportType = emailInfo.getReportFormat();
					fileName = reportType == Misc.PDF ?reportName + ".pdf" : reportName + ".xls";	
					contentType = reportType == Misc.PDF ? "application/pdf" : "application/xls"; 
					fPageInfo = CacheManager.getFrontPageInfoByMenuId(_dbConnection, emailInfo.getReport().getMenuId(), menuBean.getComponentFile());
					//fPageInfo = CacheManager.getFrontPageConfig(_dbConnection,(int) menuBean.getUserId(), menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(),menuBean.getRowId(), menuBean.getColId());
					//pv123 = !Misc.isUndef(emailInfo.getPortNodeId()) ? emailInfo.getPortNodeId() : Misc.getParamAsInt((String) User.loadUserPreference(_dbConnection, menuBean.getUserId()).get("pv123"));
					pv123 = emailInfo.getPortNodeId();
					_session.setAttribute("pv123",pv123+"", false);
					_session.setAttribute("front_page",menuBean.getComponentFile()+"", false);
					_session.setAttribute("email_report",1+"", false);
					_session.setAttribute("email_report_relative_start",emailInfo.getReportSpan().getRelativeSt()+"", false);
					_session.setAttribute("email_report_relative_end",emailInfo.getReportSpan().getRelativeEn()+"", false);
					_session.setAttribute("email_report_granularity",emailInfo.getReportSpan().getGranularity()+"", false);
					_session.setAttribute("email_report_startHr",emailInfo.getReportSpan().getStartHr()+"", false);
					_session.setAttribute("email_report_startMin",emailInfo.getReportSpan().getStartMin()+"", false);
					System.out.println("AMSTT_LocTracker:("+emailInfoId+","+scheduleDate+","+emailInfo.getReportSpan().getRelativeSt()+","
							+emailInfo.getReportSpan().getRelativeEn()+","+emailInfo.getReportSpan().getGranularity()+","
							+emailInfo.getReportSpan().getStartHr()+","+emailInfo.getReportSpan().getStartMin());
					searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, menuBean.getMenuTag(), fPageInfo.m_frontSearchCriteria, null,true,false);
					if("TRIP_PERFORMANCE".equalsIgnoreCase(emailInfo.getReport().getPageContext())){
						PrintDateTable qb1 = new PrintDateTable();
						qb1.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, buffer, reportType, reportName,emailInfo.getReport().getId());//DEBUG13 - wasnt compiling ... ,null removed 20160321
					}
					else{
						GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
							qb.printPageGen(_dbConnection , fPageInfo, _session , searchBoxHelper,null,buffer, reportType, reportName,emailInfo.getReport().getId(),null,null,null, null, null);
					}
				}
				if(buffer.size() > 0){
					stream = response.getOutputStream();
					response.setContentType(contentType);
					response.addHeader("Content-Disposition", "attachment; filename="+fileName);
					response.setContentLength(buffer.size());		     
					stream.write(buffer.toByteArray());
				}
				else{
					response.setContentType("text/html");
					out = response.getWriter();
					out.print("no Data");
				}
			} 
		}catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{
				if (stream != null){
					stream.flush();
				}
				if (buffer != null){
					buffer.flush();
					buffer.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return true;
	}


	private boolean processMailRequest(HttpServletRequest request, HttpServletResponse response){

		PrintWriter out = null;
		ServletOutputStream stream = null;
		ByteArrayOutputStream buffer = null;
		FrontPageInfo fPageInfo = null;
		com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = null;
		Connection _dbConnection = InitHelper.helpGetDBConn(request);
		SessionManager _session = InitHelper.helpGetSession(request);
		User _user = _session.getUser();
		int reportId = Misc.getParamAsInt(request.getParameter("reportId"));
		int pv123 = Misc.getUndefInt();
		MenuBean menuBean = null;
		String reportName = "";
		String contentType = "";
		Date scheduleDate = new Date(Misc.getParamAsLong(request.getParameter("scheduled_date"), System.currentTimeMillis()));
		Date currentDate = new Date(System.currentTimeMillis());
		int relativeStart = -1;
		Triple<String, Integer, Integer> reportInfo = null;
		int reportType = Misc.EXCEL;
		String fileName = "";
		int granularity = Misc.SCOPE_DAY;
		StringBuilder error = new StringBuilder();
		try {
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null);	
			/*	Map<String, String[]> parameters = request.getParameterMap();
			for(String parameter : parameters.keySet()) {
				if(parameter.toLowerCase().startsWith("tr_analysis")) {
					request.setAttribute(parameter.replace("tr_analysis", "home"),parameters.get(parameter)[0]);
				}
			}
			 */	
			buffer = new ByteArrayOutputStream();
			GenerateDataDao genDao = new GenerateDataDao();
			menuBean = genDao.getMenuParams(reportId, _dbConnection);
			EmailSenderDao  esDao = new EmailSenderDao();
			AutoEmailBean mailBean = esDao.fetchEmailReportDefinitionById(_dbConnection, reportId);
			Pair<String, Table> reportPair = null;
			if(menuBean != null){
				reportInfo = genDao.getReportInfo(reportId, _dbConnection);
				reportName = reportInfo.first;
				reportType = reportInfo.second;
				fileName = reportType == Misc.PDF ?reportName + ".pdf" : reportName + ".xls";	
				contentType = reportType == Misc.PDF ? "application/pdf" : "application/xls"; 
				fPageInfo = CacheManager.getFrontPageInfoByMenuId(_dbConnection, menuBean.getId(), menuBean.getComponentFile());
				//fPageInfo = CacheManager.getFrontPageConfig(_dbConnection,(int) menuBean.getUserId(), menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(),menuBean.getRowId(), menuBean.getColId());
				pv123 = !Misc.isUndef(menuBean.getPortNodeId()) ? menuBean.getPortNodeId() : Misc.getParamAsInt((String) User.loadUserPreference(_dbConnection, menuBean.getUserId()).get("pv123"));
				granularity = genDao.getReportGranularity(reportId,_dbConnection);
				relativeStart = (int) ((TimePeriodHelper.getBegOfDate(scheduleDate, granularity).getTime() - TimePeriodHelper.getBegOfDate(currentDate, granularity).getTime())/(24*3600*1000));
				_session.setAttribute("pv123",pv123+"", false);
				_session.setAttribute("front_page",menuBean.getComponentFile()+"", false);
				_session.setAttribute("email_report",1+"", false);
				_session.setAttribute("email_report_relative_start",relativeStart+"", false);
				searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, menuBean.getMenuTag(), fPageInfo.m_frontSearchCriteria, null,true,false);
				if(reportInfo.third == TRIP_PERFORMANCE){
					PrintDateTable qb1 = new PrintDateTable();
					qb1.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, buffer, reportType, reportName,reportId);//DEBUG13 - wasnt compiling ... ,null removed 20160321
				}
				else{
					GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
					reportPair =  qb.printPageGen(_dbConnection , fPageInfo, _session , searchBoxHelper,null,buffer, reportType, reportName,reportId,null,null,null, null, null);
				}
			}
			Table table = null;
			if(reportPair != null)
				table = reportPair.second;
			String subject = mailBean.getGranularity() == 0 ? "[QUATARLY]:" :mailBean.getGranularity() == 1?"[ANUALY]:":mailBean.getGranularity() == 2?"[MONTHLY]:":
				mailBean.getGranularity() == 3?"[WEEKLY]:":mailBean.getGranularity() == 4?"[DAILY]:":mailBean.getGranularity() == 5?"[CUSTOM]:":mailBean.getGranularity() == 6?"[SHIFT]:":
					mailBean.getGranularity() == 7?"[HOURLY]:":"[RELATIVE HOURLY]:";
			subject += mailBean.getReportName();

			boolean noData = buffer == null || buffer.toByteArray() == null || buffer.toByteArray().length <= 0;
			System.out.println("LocTracker Mail(d,s,r):("+noData+","+subject+",\""+mailBean.getRecipientStr()+"\")");
			try{
				mailBean.setSendingDate(System.currentTimeMillis());
				if(mailBean.getRecipient() != null && mailBean.getRecipient().size() > 0)
					MailSender.send(mailBean, subject, "", buffer.toByteArray(), reportType,noData,_session.getParameter("db_server_name"));
				if(noData)
					mailBean.setStatus(NO_DATA);
				else
					mailBean.setStatus(SENT);
				if(!Misc.isUndef(mailBean.getMailId()))
					esDao.updateEmailInformation(mailBean, _dbConnection, error, Misc.getServerName());
				else
					esDao.insertEmailInformation(mailBean, _dbConnection, error, Misc.getServerName());	
			}catch(Exception e){
				error.append("[Sending Email Failure :] ").append(e.getMessage()).append("\n");
				if(mailBean.getStatus() == WAIT){
					mailBean.setStatus(FAILED);
					esDao.updateEmailInformation(mailBean, _dbConnection, error, Misc.getServerName());
				}
				else{
					mailBean.setStatus(WAIT);
					esDao.insertEmailInformation(mailBean, _dbConnection, error, Misc.getServerName());
				}	
				e.printStackTrace();
			}
			doPostProcessForTableData(_dbConnection,reportId,reportName,subject,mailBean,table,fPageInfo,_session);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{
				response.setContentType("text/html");
				out = response.getWriter();
				if(buffer != null && buffer.size() > 0){
					out.print(error.toString());
				}
				else{

					out.print("no Data");
				}
				if (stream != null){
					stream.flush();
				}
				if (buffer != null){
					buffer.flush();
					buffer.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return true;
	}
	private boolean processMailRequestLatest(HttpServletRequest request, HttpServletResponse response){
		PrintWriter out = null;
		ServletOutputStream stream = null;
		ByteArrayOutputStream buffer = null;
		FrontPageInfo fPageInfo = null;
		com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = null;
		Connection _dbConnection = InitHelper.helpGetDBConn(request);
		SessionManager _session = InitHelper.helpGetSession(request);
		User _user = _session.getUser();
		int emailInfoId = Misc.getParamAsInt(request.getParameter("emailInfoId"));
		int pv123 = Misc.getUndefInt();
		long scheduleDate = Misc.getParamAsLong(request.getParameter("scheduled_date"));
		long currentDate = System.currentTimeMillis();
		StringBuilder error = new StringBuilder();

		try {
			if(!Misc.isUndef(emailInfoId) && !Misc.isUndef(scheduleDate)){
				int privIdForOrg = _user.getPrivToCheckForOrg(_session, null);	
				buffer = new ByteArrayOutputStream();
				EmailSchedulerDao emailSchedulerDao = new EmailSchedulerDao();
				EmailInfo emailInfo = emailSchedulerDao.fetchEmailScheduleInfoById(_dbConnection, emailInfoId);
				Pair<String, Table> reportPair = null;
				if(emailInfo != null && emailInfo.isValidEmailInfo(_dbConnection,pv123) ){
					MenuBean menuBean = (new CustomizeDao()).getMenuById(_dbConnection, emailInfo.getReport().getMenuId());
					String reportName = emailInfo.getReport().getName();
					int reportType = emailInfo.getReportFormat();
					String fileName = reportType == Misc.PDF ?reportName + ".pdf" : reportName + ".xls";	
					String contentType = reportType == Misc.PDF ? "application/pdf" : "application/xls"; 
					fPageInfo = CacheManager.getFrontPageInfoByMenuId(_dbConnection, emailInfo.getReport().getMenuId(), menuBean.getComponentFile());
					//fPageInfo = CacheManager.getFrontPageConfig(_dbConnection,(int) menuBean.getUserId(), menuBean.getPortNodeId(), menuBean.getMenuTag(), menuBean.getComponentFile(),menuBean.getRowId(), menuBean.getColId());
					//pv123 = !Misc.isUndef(emailInfo.getPortNodeId()) ? emailInfo.getPortNodeId() : Misc.getParamAsInt((String) User.loadUserPreference(_dbConnection, menuBean.getUserId()).get("pv123"));
					pv123 = emailInfo.getPortNodeId();
					_session.setAttribute("pv123",pv123+"", false);
					_session.setAttribute("front_page",menuBean.getComponentFile()+"", false);
					_session.setAttribute("email_report",1+"", false);
					_session.setAttribute("email_report_relative_start",emailInfo.getReportSpan().getRelativeSt()+"", false);
					_session.setAttribute("email_report_relative_end",emailInfo.getReportSpan().getRelativeEn()+"", false);
					_session.setAttribute("email_report_granularity",emailInfo.getReportSpan().getGranularity()+"", false);
					_session.setAttribute("email_report_startHr",emailInfo.getReportSpan().getStartHr()+"", false);
					_session.setAttribute("email_report_startMin",emailInfo.getReportSpan().getStartMin()+"", false);
					System.out.println("AMSTT_LocTracker:("+emailInfoId+","+scheduleDate+","+emailInfo.getReportSpan().getRelativeSt()+","
																	  +emailInfo.getReportSpan().getRelativeEn()+","+emailInfo.getReportSpan().getGranularity()+","
																	  +emailInfo.getReportSpan().getStartHr()+","+emailInfo.getReportSpan().getStartMin());
					searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, menuBean.getMenuTag(), fPageInfo.m_frontSearchCriteria, null,true,false);
					if("TRIP_PERFORMANCE".equalsIgnoreCase(emailInfo.getReport().getPageContext())){
						PrintDateTable qb1 = new PrintDateTable();
						qb1.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, buffer, reportType, reportName,emailInfo.getReport().getId());//DEBUG13 - wasnt compiling ... ,null removed 20160321
					}
					else{
						GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
						reportPair =  qb.printPageGen(_dbConnection , fPageInfo, _session , searchBoxHelper,null,buffer, reportType, reportName,emailInfo.getReport().getId(),null,null,null, null, null);
					}
					Table table = reportPair == null ? null :  reportPair.second;
					boolean noData = buffer == null || buffer.toByteArray() == null || buffer.toByteArray().length <= 0;
					EmailStatusDetail emailStatus = null;
					OrgMailingProfile orgProfile = null;
					try{
						emailStatus = emailSchedulerDao.fetchEmailStatusByEmailInfoId(_dbConnection, emailInfoId,scheduleDate);
						if(emailStatus == null)
							emailStatus = new EmailStatusDetail(Misc.getUndefInt(), emailInfoId, EmailStatusDetail.STATUS_SEND_RETRY_0, System.currentTimeMillis(),scheduleDate , currentDate);
						emailStatus.setSendingDate(currentDate);
						orgProfile = emailSchedulerDao.fetchOrgMailingDetail(_dbConnection, emailInfo.getOrgMailingId());
						MailSenderNew.sendNew(emailInfo, buffer.toByteArray(),noData,_session.getParameter("db_server_name"),orgProfile);
						if(noData)
							emailStatus.setStatus(EmailStatusDetail.STATUS_SEND_NO_DATA);
						else
							emailStatus.setStatus(EmailStatusDetail.STATUS_SEND_SUCCESS);
						if(!Misc.isUndef(emailStatus.getId()))
							emailSchedulerDao.updateEmailStatusDetails(_dbConnection,emailStatus, error);
						else
							emailSchedulerDao.insertEmailInformation(_dbConnection, emailStatus, error);	
					}catch(Exception e){
						error.append("[Sending Email Failure :] ").append(e.getMessage()).append("\n");
						if(emailStatus.getStatus() >= EmailStatusDetail.STATUS_SEND_RETRY_2){
							emailStatus.setStatus(EmailStatusDetail.STATUS_SEND_FAILED);
							if(!Misc.isUndef(emailStatus.getId()))
								emailSchedulerDao.updateEmailStatusDetails(_dbConnection,emailStatus, error);
							else
								emailSchedulerDao.insertEmailInformation(_dbConnection, emailStatus, error);
						}
						else{
							emailStatus.setStatus(emailStatus.getStatus()+1);
							if(!Misc.isUndef(emailStatus.getId()))
								emailSchedulerDao.updateEmailStatusDetails(_dbConnection,emailStatus, error);
							else
								emailSchedulerDao.insertEmailInformation(_dbConnection, emailStatus, error);
						}	
						e.printStackTrace();
					}
					doPostProcessForTableDataNew(_dbConnection,emailInfo,table,fPageInfo,_session,orgProfile);
				}else{
					System.out.println("AMSTT_LocTracker:Invalid Request");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try{
				response.setContentType("text/html");
				out = response.getWriter();
				if(error != null && error.length() > 0){
					out.print(error.toString());
				}
				else{

					out.print("no Data");
				}
				if (stream != null){
					stream.flush();
				}
				if (buffer != null){
					buffer.flush();
					buffer.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private void doPostProcessForTableData(Connection _dbConnection,int reportId,String reportName, String subject,AutoEmailBean mailBean, Table table, FrontPageInfo fPageInfo, SessionManager _session) {
		if(fPageInfo == null || fPageInfo.m_frontInfoList == null || table == null )
			return;
		int colIndex =  fPageInfo.getColIndexByName("cluster_mail_id");
		if(colIndex < 0)
			return;
		System.out.println("LocTracker Mail:Doing Post Prcoess");
		try{
			DimConfigInfo dimConf = (DimConfigInfo) fPageInfo.m_frontInfoList.get(colIndex);
			if(dimConf == null || dimConf.m_dimCalc == null || dimConf.m_dimCalc.m_dimInfo == null)
				return;
			int paramId = dimConf.m_dimCalc.m_dimInfo.m_id;
			if(paramId < 0)
				return;
			int mailClusteringIndex = table.getColumnIndexById(paramId);
			if(mailClusteringIndex < 0)
				return;
			table.setMailClusteringIndex(mailClusteringIndex);
			ArrayList<String> clusterGroupNames = table.getClusterGroupNames();

			for(String groupParamVal : clusterGroupNames){

				ByteArrayOutputStream buffer = null;
				try{
					buffer = new ByteArrayOutputStream();
					ExcelGenerator exl = new ExcelGenerator();
					exl.printExcel(buffer, "data", table,_session,reportId,groupParamVal);
					if(exl.getDataRowsCount() <= 0 || buffer == null || buffer.size() <= 0)
						continue;
					ArrayList<String> to = new ArrayList<String>();
					to.add(groupParamVal);
					mailBean.setRecipient(to);
					System.out.println("Loctracker Mail(s,r):("+subject+",\""+groupParamVal+"\")");
					MailSender.send(mailBean, subject, "", buffer.toByteArray(), Misc.EXCEL,false,Misc.getServerName());
				}catch(Exception ex){
					ex.printStackTrace();
				}finally{
					if (buffer != null){
						buffer.flush();
						buffer.close();
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private void doPostProcessForTableDataNew(Connection _dbConnection,EmailInfo emailInfo, Table table, FrontPageInfo fPageInfo, SessionManager _session,OrgMailingProfile orgProfile) {
		if(fPageInfo == null || fPageInfo.m_frontInfoList == null || table == null )
			return;
		int colIndex =  fPageInfo.getColIndexByName("cluster_mail_id");
		if(colIndex < 0)
			return;
		System.out.println("LocTracker Mail:Doing Post Prcoess");
		try{
			DimConfigInfo dimConf = (DimConfigInfo) fPageInfo.m_frontInfoList.get(colIndex);
			if(dimConf == null || dimConf.m_dimCalc == null || dimConf.m_dimCalc.m_dimInfo == null)
				return;
			int paramId = dimConf.m_dimCalc.m_dimInfo.m_id;
			if(paramId < 0)
				return;
			int mailClusteringIndex = table.getColumnIndexById(paramId);
			if(mailClusteringIndex < 0)
				return;
			table.setMailClusteringIndex(mailClusteringIndex);
			ArrayList<String> clusterGroupNames = table.getClusterGroupNames();

			for(String groupParamVal : clusterGroupNames){

				ByteArrayOutputStream buffer = null;
				try{
					buffer = new ByteArrayOutputStream();
					ExcelGenerator exl = new ExcelGenerator();
					exl.printExcel(buffer, "data", table,_session,emailInfo.getReport().getId(),groupParamVal);
					if(exl.getDataRowsCount() <= 0 || buffer == null || buffer.size() <= 0)
						continue;
					ArrayList<EmailUser> to = new ArrayList<EmailUser>();
					to.add(new EmailUser(emailInfo.getGroup().getId(), "", groupParamVal, EmailUser.USER_TYPE_TO, 1,null));
					emailInfo.getGroup().setEmailUserList(to);
					System.out.println("Loctracker Mail(s,r):("+emailInfo.getSubject()+",\""+groupParamVal+"\")");
					MailSenderNew.sendNew(emailInfo, buffer.toByteArray(), false, Misc.getServerName(), orgProfile);
				}catch(Exception ex){
					ex.printStackTrace();
				}finally{
					if (buffer != null){
						buffer.flush();
						buffer.close();
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
package com.ipssi.dashboard.customization;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.dashboard.DashBoardServlet;
import com.ipssi.dashboard.DashboardDao;
import com.ipssi.dashboard.DashboardWriter;
import com.ipssi.dashboard.OpstationOperationalStatus;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class DashboardAction implements ActionI {

	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		System.out.println("  ####    DashBoardAction.processRequest()");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = ""; 
		String customActionForwardRoute = Common.getParamAsString(request.getParameter("customActionForwardRoute"));
		boolean success = true;
		try {
			if ("trackRegionPerformance".equals(action))
				actionForward = processTrackRegionPerformance(request, response);
			else if ("strandedVehicle".equals(action))
				actionForward = processStrandedVehicle(request, response);
			else if ("vehicleOutsideGate".equals(action))
				actionForward = processVehicleOutsideGate(request, response);
			else if ("tripCountPerformance".equals(action))
				actionForward = tripCountPerformance(request, response);
			else if ("dashboard1".equals(action))
				actionForward = processDashboard(request, response, "db_dashboard.xml");
			else if ("dashboard2".equals(action))
				actionForward = processDashboard(request, response, "db_dashboard_acc.xml");
			else if ("dashboard3".equals(action))
				actionForward = processDashboard(request, response, "db_dashboard_text.xml");
			else if ("dashboard5".equals(action))
				actionForward = processDashboard(request, response, "db_dashboard_opstation.xml");
			else if ("operationalStatus".equals(action))
				actionForward = operationalStatus(request,response);
			else if ("tripEventTextDashboard".equals(action))
				actionForward = tripEventTextDashboard(request, response);
			else if ("summaryReport".equals(action))
				actionForward = processSummaryReport(request, response);
			else if ("listQueuedVehicles".equals(action))
				actionForward = processListQueuedVehicles(request, response);
			else if ("TripInfoDetail".equals(action))
				actionForward = processTripInfoDetail(request, response);
			else if ("detentionReport".equals(action))
				actionForward = processDetentionReport(request, response);
			else if ("detentionDetails".equals(action))
				actionForward = processDetentionDetails(request, response);
			else if ("dashboard10".equals(action))
				actionForward = processDashboard(request, response, "db_dashboard_somya.xml");
			else if ("tripPerformanceRatio".equals(action))
				actionForward = processTripPerformanceRatio(request, response);
			else if ("productionTarget".equals(action))
					actionForward = processProductionTarget(request, response);
			else  if ("tripSummary".equals(action))
					actionForward = processTripSummary(request, response);
			else if ("tripDetail".equals(action))
				actionForward = processTripDetail(request, response);
			else if ("haulageSummary".equals(action))
				actionForward = processHaulageSummary(request, response);
			else
				actionForward = processDashboard(request, response, null);
		} catch (GenericException e) {
			System.out.println("actionList " + e.getMessage());
			e.printStackTrace();
		}
		actionForward = sendResponse(actionForward, success, request);
		if ( customActionForwardRoute != null && customActionForwardRoute != ""){
            actionForward = customActionForwardRoute;
      }

		return actionForward;
	}
	private String processProductionTarget(HttpServletRequest request,HttpServletResponse response) throws GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); 
			String frontPageName =Misc.getParamAsString(_session.getParameter("front_page"), "db_dashboard_report_production.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "productionTarget";
	}
	private String processTripDetail(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); 
			String frontPageName =Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_detail.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripPerformanceRatio";
	}
	private String processTripPerformanceRatio(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); 
			String frontPageName =Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_perfomance_ratio.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			if (Misc.getParamAsInt(_session.getAttribute("home20051")) == Misc.SCOPE_SHIFT || true) {
				String startDateParamName = searchBoxHelper.m_topPageContext + 20035;
				String endDateParamName = searchBoxHelper.m_topPageContext + 20036;
				 
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
				java.util.Date dt = new Date();
				java.util.Date start = new Date(dt.getTime()-60*60*1000);
				java.util.Date en = new Date(dt.getTime());
				
				_session.setAttribute(startDateParamName, sdf.format(start), false);
				_session.setAttribute(endDateParamName, sdf.format(en), false);
			}
			String wildCharUnloadingccb = searchBoxHelper.m_topPageContext + 20016;
			if ("db_trip_perfomance_ratio.xml".equals(frontPageName) ){
				_session.setAttribute(wildCharUnloadingccb, "_", false);
			} 
			else if ("db_trip_perfomance_ratio_2.xml".equals(frontPageName) ){
				_session.setAttribute(wildCharUnloadingccb, "east", false);
			} 
			else if ("db_trip_perfomance_ratio_3.xml".equals(frontPageName) ){
				_session.setAttribute(wildCharUnloadingccb,"west", false);
			}
	
			System.out.println("DashBoardServlet.processTripPerformanceRatio() Unloading 20016 :: " + _session.getAttribute(wildCharUnloadingccb));
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);

			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripPerformanceRatio";

	}


	private String processTripPerformanceRatioOld(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); 
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_perfomance_ratio.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			if (Misc.getParamAsInt(_session.getAttribute("home20051")) == Misc.SCOPE_SHIFT || true) {
				String startDateParamName = searchBoxHelper.m_topPageContext + 20035;
				String endDateParamName = searchBoxHelper.m_topPageContext + 20036;
				String wildCharUnloadingccb = searchBoxHelper.m_topPageContext + 20016;
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
				java.util.Date dt = new Date();
				java.util.Date start = new Date(dt.getTime()-60*60*1000);
				java.util.Date en = new Date(dt.getTime());
				
				_session.setAttribute(startDateParamName, sdf.format(start), false);
				_session.setAttribute(endDateParamName, sdf.format(en), false);
			}
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);

			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripPerformanceRatio";

	}

	private String processTripCount(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_track_region_performance.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "trackRegionPerformance");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripCount";
	}

	private String operationalStatus(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		try {
			
			
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			DashboardDao dao = new DashboardDao();
			int save = Misc.getParamAsInt(request.getParameter("insert"));
			if ( save == 1 ){
				String xml = Misc.getParamAsString(request.getParameter("xml_data"));
				org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
				org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("non_operating");

				int size = nList.getLength();
				ArrayList<OpstationOperationalStatus> opList = new ArrayList<OpstationOperationalStatus>();
				Date date = new Date();
				for (int i = 0; i < size; i++) {
					org.w3c.dom.Node n = nList.item(i);

					org.w3c.dom.Element e = (org.w3c.dom.Element) n;
					int id = Misc.getParamAsInt(e.getAttribute("opId"));
					int reason = Misc.getParamAsInt(e.getAttribute("reason"));
					SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
					Timestamp endTs = new Timestamp(sdf.parse(Misc.getParamAsString(e.getAttribute("time"))).getTime());
					OpstationOperationalStatus bean = new OpstationOperationalStatus();
					
					bean.setStatus(Misc.getParamAsInt(e.getAttribute("status")));
					bean.setOpStationId(id);
					bean.setReason(reason);
					bean.setStartTime(new Timestamp(date.getTime()));
					if (bean.getStatus() == 1)
					bean.setEndTime(endTs);
					opList.add(bean);
				}
				
				dao.insertOperationalStatus(_dbConnection,opList);
			}
			
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_operational_status.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);
			
			DashboardWriter qb = new DashboardWriter();
			HashMap<String, String> filterCriteriaMap = new HashMap<String, String>();
			ArrayList<ArrayList<DimConfigInfo>> searchBox = fPageInfo.m_frontSearchCriteria;
			qb.setFilterCriteria(filterCriteriaMap, _session, searchBox, searchBoxHelper);
			
			String valList = filterCriteriaMap.get("20210");
			
			
			ArrayList<OpstationOperationalStatus> list =  dao.getOpstationStatus(valList,_dbConnection);
			
			request.setAttribute("searchBoxHelper", searchBoxHelper);
			request.setAttribute("opStationOperationalStatus", list);
			request.setAttribute("opStationlist", valList);
//			DashboardWriter qb = new DashboardWriter();
//			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "summaryReport",null);
//			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "operationalStatus";
	}

	private String processSummaryReport(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_opstation_summary.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "summaryReport",null);
			
			
			
			request.setAttribute("outStr", outStr);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "summaryReport";
	}

	public String processTripInfoDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_info_detail.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);
			System.out.println("DashBoardServlet.processTripInfoDetail()  : request.getParameter(home20022) :  tripId     :  " + request.getParameter("home20022"));
			System.out.println("DashBoardServlet.processTripInfoDetail()  : request.getParameter(home9029) :  opStationId     :  " + request.getParameter("home9029"));
			System.out.println("DashBoardServlet.processTripInfoDetail()  : request.getParameter(home9064) :  Latest Event Time     :  " + request.getParameter("home9064"));
			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "TripInfoDetail");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "TripInfoDetail";

	}

	public String processListQueuedVehicles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_op_station_vehicles_detail.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "listQueuedVehicles", request.getParameter("home9029"));
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "listQueuedVehicles";

	}

	public String tripCountPerformance(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_count_performance.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);
			
			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "tripCountPerformance");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripCountPerformance";
	}

	public String tripEventTextDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {

		return "tripEventTextDashboard";
	}

	public String processTrackRegionPerformance(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_track_region_performance.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "trackRegionPerformance");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "trackRegionPerformance";
	}
	
	public String processDetentionReport(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_detention_report_plants.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "processDetentionReport");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "detentionReport";
	}
	
	public String processDetentionDetails(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_detention_detail.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "processDetentionDetails");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "detentionDetails";
	}
	
	public String processHaulageSummary(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_general_haulage_summary.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);

//			DashboardWriter qb = new DashboardWriter();
//			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "processTripSummary");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripSummary";
	}
	public String processTripSummary(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_summary.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);
			//HACK until we change FP 
			if ("db_trip_summary.xml".equals(frontPageName)) {
				_session.setAttribute("do_transpose", "1", false);
			}

			if (Misc.getParamAsInt(_session.getAttribute("home20051")) == Misc.SCOPE_SHIFT) {
				String startDateParamName = searchBoxHelper.m_topPageContext + 20023;
				String endDateParamName = searchBoxHelper.m_topPageContext + 20034;
				
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
				java.util.Date dt = new Date();
				if (dt.getHours() <= 6) {
					Misc.addDays(dt, -1);
				}
				dt.setHours(5);
				dt.setMinutes(30);
				java.util.Date en = new Date(dt.getTime());
				Misc.addDays(en, 1);
				_session.setAttribute(startDateParamName, sdf.format(dt), false);
				_session.setAttribute(endDateParamName, sdf.format(en), false);
				//System.out.println();
				// _session.setAttribute(wildCharUnloading,"_", false);
				 
				
			}
			String wildCharUnloadingccb = searchBoxHelper.m_topPageContext + 20016;
			if ( "db_trip_summary.xml".equals(frontPageName) ){
				_session.setAttribute(wildCharUnloadingccb,"_", false);
			} else if ("db_trip_summary_2.xml".equals(frontPageName) ){
				_session.setAttribute(wildCharUnloadingccb,"east", false);
			} else if ("db_trip_summary_3.xml".equals(frontPageName) ){
				_session.setAttribute(wildCharUnloadingccb,"west", false);
			}
	
            System.out.println("DashBoardServlet.processTripSummary() Unloading 20016 ::" + _session.getAttribute(wildCharUnloadingccb));
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);

//			DashboardWriter qb = new DashboardWriter();
//			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "processTripSummary");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripSummary";
	}

	public String processTripSummaryOld(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_trip_summary.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			if (Misc.getParamAsInt(_session.getAttribute("home20051")) == Misc.SCOPE_SHIFT) {
				String startDateParamName = searchBoxHelper.m_topPageContext + 20023;
				String endDateParamName = searchBoxHelper.m_topPageContext + 20034;
				String wildCharUnloading = searchBoxHelper.m_topPageContext + 20016;
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
				java.util.Date dt = new Date();
				if (dt.getHours() <= 6) {
					Misc.addDays(dt, -1);
				}
				dt.setHours(5);
				dt.setMinutes(30);
				java.util.Date en = new Date(dt.getTime());
				Misc.addDays(en, 1);
				_session.setAttribute(startDateParamName, sdf.format(dt), false);
				_session.setAttribute(endDateParamName, sdf.format(en), false);
				// _session.setAttribute(wildCharUnloading, "_", false);
			}
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
            String outStr = qb.printDashBoard(_dbConnection , fPageInfo, _session, searchBoxHelper, null, null);

//			DashboardWriter qb = new DashboardWriter();
//			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "processTripSummary");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "tripSummary";
	}

	public String processStrandedVehicle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_stranded_vehicle.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "strandedVehicle");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "strandedVehicle";

	}

	public String processVehicleOutsideGate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException {
		try {
			Connection _dbConnection = InitHelper.helpGetDBConn(request);
			SessionManager _session = InitHelper.helpGetSession(request);
			User _user = InitHelper.helpGetUser(request);
			int privIdForOrg = _user.getPrivToCheckForOrg(_session, null); // this tells the privilege to use for showing the Org tree
			String frontPageName = Misc.getParamAsString(_session.getParameter("front_page"), "db_vehicle_outside_gate.xml");
			String pageContext = Misc.getParamAsString(_session.getParameter("page_context"), "Dashboard");
			FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection, _user.getUserId(), Misc.getUserTrackControlOrg(_session), pageContext, frontPageName, 0, 0);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, null, fPageInfo.m_frontSearchCriteria, null, true);

			DashboardWriter qb = new DashboardWriter();
			String outStr = qb.printPage(_dbConnection, fPageInfo, _session, searchBoxHelper, "vehicleOutsideGate");
			request.setAttribute("outStr", outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		return "vehicleOutsideGate";
	}

	public String processDashboard(HttpServletRequest request, HttpServletResponse response, String fileName,String junk) throws ServletException, IOException, GenericException {

		Connection conn = InitHelper.helpGetDBConn(request);
		if (fileName == null)
			fileName = "db_dashboard_acc.xml";

		FrontPageInfo fPageInfo = null;
		ArrayList<DimConfigInfo> tableDimConfig = new ArrayList<DimConfigInfo>();
		ArrayList<DimConfigInfo> leftDimConfig = new ArrayList<DimConfigInfo>();
		ArrayList<DimConfigInfo> rightDimConfig = new ArrayList<DimConfigInfo>();
		int tableWidth = 65;
		// String[][] rowColInfo = new String[5][5];
		try {
			fPageInfo = FrontPageInfo.getFrontPage(fileName, true, conn, Cache.getCacheInstance(conn));
			ArrayList<ArrayList<DimConfigInfo>> dashboardList = fPageInfo.m_frontDashboardList;
			for (int i = 0; i < dashboardList.size(); i++) {
				ArrayList<DimConfigInfo> dimConfiglist = dashboardList.get(i);
				for (int j = 0; j < dimConfiglist.size(); j++) {
					DimConfigInfo dimConf = dimConfiglist.get(j);
					if (i == 0)
						tableDimConfig.add(dimConf);
					else if (i == 1)
						leftDimConfig.add(dimConf);
					else
						rightDimConfig.add(dimConf);
					System.out.println(dimConf.m_url);
					System.out.println(dimConf.m_xml);
					// rowColInfo[j][i] = dimConf.m_url;
				}
			}
			if (tableDimConfig != null && tableDimConfig.size() > 0) {
				tableWidth = tableDimConfig.get(0).m_width;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		request.setAttribute("tableWidth", tableWidth);
		request.setAttribute("tableDimConfig", tableDimConfig);
		request.setAttribute("leftDimConfig", leftDimConfig);
		request.setAttribute("rightDimConfig", rightDimConfig);
		return "dashboard";
	}
	
	
	public String processDashboard(HttpServletRequest request, HttpServletResponse response, String fileName) throws ServletException, IOException, GenericException {

		Connection conn = InitHelper.helpGetDBConn(request);
		if (fileName == null)
			fileName = "db_dashboard_acc.xml";

		FrontPageInfo fPageInfo = null;
		ArrayList<DimConfigInfo> tableDimConfig = new ArrayList<DimConfigInfo>();
		ArrayList<DimConfigInfo> leftDimConfig = new ArrayList<DimConfigInfo>();
		ArrayList<DimConfigInfo> rightDimConfig = new ArrayList<DimConfigInfo>();
		int tableWidth = 65;
		// String[][] rowColInfo = new String[5][5];
		try {
			fPageInfo = FrontPageInfo.getFrontPage(fileName, true, conn, Cache.getCacheInstance(conn));
			ArrayList<ArrayList<DimConfigInfo>> dashboardList = fPageInfo.m_frontDashboardList;
			for (int i = 0; i < dashboardList.size(); i++) {
				ArrayList<DimConfigInfo> dimConfiglist = dashboardList.get(i);
				for (int j = 0; j < dimConfiglist.size(); j++) {
					DimConfigInfo dimConf = dimConfiglist.get(j);
					if (i == 0)
						tableDimConfig.add(dimConf);
					else if (i == 1)
						leftDimConfig.add(dimConf);
					else
						rightDimConfig.add(dimConf);
					System.out.println(dimConf.m_url);
					System.out.println(dimConf.m_xml);
					// rowColInfo[j][i] = dimConf.m_url;
				}
			}
			if (tableDimConfig != null && tableDimConfig.size() > 0) {
				tableWidth = tableDimConfig.get(0).m_width;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(e);
		}
		request.setAttribute("tableWidth", tableWidth);
		request.setAttribute("tableDimConfig", tableDimConfig);
		request.setAttribute("leftDimConfig", leftDimConfig);
		request.setAttribute("rightDimConfig", rightDimConfig);
		return "dashboard";
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if (action.equals("dashboard")) {
			if (success)
				actionForward = "/baseDashboard.jsp";
			else
				actionForward = "/baseDashboard.jsp";
		} else if (action.equals("trackRegionPerformance")) {
			if (success)
				actionForward = "/dashboard.jsp";
			else
				actionForward = "/dashboard.jsp";
		} else if (action.equals("strandedVehicle")) {
			if (success)
				actionForward = "/dashboard.jsp";
			else
				actionForward = "/dashboard.jsp";
		} else if (action.equals("vehicleOutsideGate")) {
			if (success)
				actionForward = "/dashboard.jsp";
			else
				actionForward = "/dashboard.jsp";
		} else if (action.equals("tripCountPerformance")) {
			if (success)
				actionForward = "/dashboard.jsp";
			else
				actionForward = "/dashboard.jsp";
		} else if (action.equals("listQueuedVehicles")) {
			if (success)
				actionForward = "/dashboard.jsp";
			else
				actionForward = "/dashboard.jsp";
		} else if (action.equals("tripEventTextDashboard")) {
			if (success)
				actionForward = "/dashboardText.jsp";
			else
				actionForward = "/dashboardText.jsp";
		} else if (action.equals("TripInfoDetail")) {
			if (success)
				actionForward = "/dashboard.jsp";
			else
				actionForward = "/dashboard.jsp";
		} else if ( action.equalsIgnoreCase("summaryReport")){
			actionForward = "/dashboard.jsp";
		} else if ( action.equalsIgnoreCase("detentionReport")){
			actionForward = "/dashboard.jsp";
		} else if ( action.equalsIgnoreCase("tripPerformanceRatio")){
			actionForward = "/dashboard.jsp";
		} else if ( action.equalsIgnoreCase("tripSummary")){
			actionForward = "/dashboard.jsp";
		} else if ( action.equalsIgnoreCase("detentionDetails")){
			actionForward = "/dashboard.jsp";
		} else if ( action.equals("operationalStatus")){
			actionForward = "/operationalStatusSetup.jsp";
		}
		return actionForward;
	}

}


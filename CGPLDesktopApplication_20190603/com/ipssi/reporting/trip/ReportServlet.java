package com.ipssi.reporting.trip;

import static com.ipssi.reporting.common.util.ApplicationConstants.CREATE;
import static com.ipssi.reporting.common.util.ApplicationConstants.SAVE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.reporting.customize.CustomizeDao;
import com.ipssi.reporting.customize.ReportDetailVO;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class ReportServlet implements ActionI {

	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		System.out.println("ReportServlet.processRequest()   $##################  ");
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		Connection conn = InitHelper.helpGetDBConn(request);
		try{
			if(CREATE.equals(action))
				actionForward = createEngineEventsTrack(request, response);
			else if(SAVE.equals(action))
				actionForward = saveEngineEventsTrack(request, response);
			else if("DisplayReport".equals(action))
				actionForward = diplayReport(request, response);
			else if("viewReport".equals(action))
				actionForward = viewReport(request, response);
			else if("save_vehicle_notes".equals(action))
				actionForward = saveVehicleInteractionNotes(request, response);
			else if("create_vehicle_notes".equals(action))
				actionForward = createVehicleNotes(request, response);
		} catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	private String createVehicleNotes(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		SessionManager session = InitHelper.helpGetSession(request);
		ReportDao reportDao = new ReportDao();
		int vehicleId = Misc.getParamAsInt(session.getParameter("vehicle_id"));
		try {
	    	request.setAttribute("lastCommentOnVehicle", reportDao.getLastCommentOnvehicle(session.getConnection(),vehicleId ));
		} catch (GenericException e) {
			System.out.println("Vehicle Interaction Notes :"+ e.getMessage());				
			e.printStackTrace();
			throw e;
		}		
		return "create_vehicle_notes";
	}

	private String saveVehicleInteractionNotes(HttpServletRequest request, HttpServletResponse response) throws GenericException {
		SessionManager session = InitHelper.helpGetSession(request);
		ReportDao reportDao = new ReportDao();
	    try {
	    	VehicleInteractionBean vehInBean = new VehicleInteractionBean();
	    	int vehicleId = Misc.getParamAsInt(session.getParameter("vehicle_id"));
	    	int breakdownCause = Misc.getParamAsInt(session.getParameter("breakdownCause"));
	    	String comment = Misc.getParamAsString(session.getParameter("comment"));
	    	long currentDateTime = System.currentTimeMillis();
	    	int userId =session.getUser().getUserId();
	    	vehInBean.setVehicleId(vehicleId);
	    	vehInBean.setCauseId(breakdownCause);
	    	vehInBean.setUserId(userId);
	    	vehInBean.setNotes(comment);
	    	vehInBean.setUpdatedOn(new Date(currentDateTime));
	    	int nxt = Misc.getParamAsInt(session.getParameter("next_followup"));
	    	java.util.Date nxtDate = nxt <= 0 ? null : new java.util.Date((nxt*60*1000+currentDateTime));
	    	//if (nxtDate != null)
	    		//Misc.addDays(nxtDate, (double)nxt/(24.0*60.0));
	    	vehInBean.setNextFollowTime(nxtDate);
	    	reportDao.insertVehicleNotes(session,vehInBean);
	    	VehicleExtendedInfo.markDirty(session.getConnection(), vehicleId);
	    	/*CacheValue.add(vehicleId, 20461, Misc.getUndefInt(), Misc.getUndefDouble(), comment, Misc.getUndefInt(), Cache.STRING_TYPE);
	    	CacheValue.add(vehicleId, 20462, Misc.getUndefInt(), Misc.getUndefDouble(), null, currentDateTime, Cache.DATE_TYPE);
	    	CacheValue.add(vehicleId, 20463, Misc.getUndefInt(), Misc.getUndefDouble(), session.getUser().getUserName(), Misc.getUndefInt(), Cache.STRING_TYPE);
	    	CacheValue.add(vehicleId, 21422, Misc.getUndefInt(), Misc.getUndefDouble(), null, nxtDate != null ? nxtDate.getTime() : Misc.getUndefInt(), Cache.DATE_TYPE);*/
	    } catch (GenericException e) {
			System.out.println("Vehicle Interaction Notes :"+ e.getMessage());				
			e.printStackTrace();
			throw e;
		}
		return "save_vehicle_notes";
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		String actionForward = "";
		if(action.equals(CREATE)){
			actionForward = "/engineEventTrack.jsp";
		}else if(action.equals(SAVE)){
			actionForward = "/engineEventClose.jsp";
		}else if(action.equals("DisplayReport")){
			actionForward = "/displayReport.jsp";
		}else if(action.equals("viewReport")){
			actionForward = "/trip_summary.jsp";
		}
		else if(action.equals("save_vehicle_notes")){
			actionForward = "/customizeDetailClose.jsp";
		}
		else if(action.equals("create_vehicle_notes")){
			actionForward = "/vehicle_interaction_notes.jsp";
		}
		return actionForward;
	}
	public String viewReport(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		CustomizeDao customizeDao = new CustomizeDao();
		SessionManager session = InitHelper.helpGetSession(request);
        Connection conn = session.getConnection();
        ReportDetailVO retVal = customizeDao.getUserReportDetail(conn, Misc.getParamAsInt(request.getParameter("reportId")));
        session.setAttribute("page_context", "user_summary_report", false);
        session.setAttribute("page_name", retVal.getName(), false);
        session.setAttribute("front_page", retVal.getFileName(), false);
        session.setAttribute("user_defined_report", "1", false);
        session.setAttributeObj("ReportDetailVO", retVal);
		return "viewReport";
	}
	
	public String diplayReport(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		CustomizeDao customizeDao = new CustomizeDao();
		SessionManager session = InitHelper.helpGetSession(request);
        Connection conn = session.getConnection();
	
		request.setAttribute("reportList", customizeDao.getUserReportDetail(conn));
		return "DisplayReport";
	}
	
	
	public String createEngineEventsTrack(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		ReportDao reportDao = new ReportDao();
		SessionManager session = InitHelper.helpGetSession(request);
        Connection conn = session.getConnection();
		int engineEventId = Misc.getParamAsInt(request.getParameter("home20139"));
		String pgContext = Misc.getParamAsString(request.getParameter("page_context"));
		int eventType = Misc.getParamAsInt(request.getParameter("eventType"),getEventType(pgContext));
		EngineEventsTrackBean engineEventsTrackBean = reportDao.getEngineEventsTrackById(conn , engineEventId, eventType);
		request.setAttribute("engineEventsTrack", engineEventsTrackBean);
		Cache _cache = session.getCache();
		int userOrgControlId = Misc.getUndefInt();
		if(eventType == 1)
			userOrgControlId = CacheTrack.VehicleSetup.getSetup(engineEventsTrackBean.getVehicleId(), conn).m_ownerOrgId;
		else if(eventType == 2)
			userOrgControlId = reportDao.getOrg(conn, engineEventsTrackBean.getEngineEventId());//reportDao.getOrg(conn, engineEventsTrackBean.getRuleId());
		MiscInner.PortInfo userOrgControlOrg = _cache.getPortInfo(userOrgControlId,conn);
		ArrayList orgSupervisionList = (ArrayList) userOrgControlOrg.getIntParams(OrgConst.ID_ORG_SUPERVISION_LEVEL);
		if(orgSupervisionList != null && orgSupervisionList.size() > 0)
			engineEventsTrackBean.setLevel((Integer)orgSupervisionList.get(0));
		request.setAttribute("userList", reportDao.getUserList(conn, userOrgControlId));
		return CREATE;
	}
	
	public String saveEngineEventsTrack(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		    Connection conn = InitHelper.helpGetDBConn(request);
		    SessionManager session = InitHelper.helpGetSession(request);
			EngineEventsTrackBean engEvtTrackBean = new EngineEventsTrackBean();
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			engEvtTrackBean.setEngineEventId(Misc.getParamAsInt(request.getParameter("eventId")));
			engEvtTrackBean.setEventType(Misc.getParamAsInt(request.getParameter("eventType")));
			
			engEvtTrackBean.setStatus(Misc.getParamAsInt(request.getParameter("status")));
			engEvtTrackBean.setPriority(Misc.getParamAsInt(request.getParameter("priority")));
			engEvtTrackBean.setAssignedTo(Misc.getParamAsInt(request.getParameter("assignedTo")));
			
			engEvtTrackBean.setReason1(Misc.getParamAsInt(request.getParameter("reason2")));
			
//			engEvtTrackBean.setComment1(request.getParameter("comment1"));
			engEvtTrackBean.setReason2(Misc.getParamAsInt(request.getParameter("reason2")));
			engEvtTrackBean.setUnsafeZoneAction(Misc.getParamAsInt(request.getParameter("unsafezone_action")));
			DimInfo reasonDim = DimInfo.getDimInfo(20202);
			int res = Misc.getParamAsInt(request.getParameter("reason1"));
			String reason = "";
			if(res > 0){
				DimInfo.ValInfo valInfo = reasonDim.getValInfo(res);
				reason = valInfo.m_name;
			}
			
			String comment1 = request.getParameter("comment1");
			String comment2 = request.getParameter("reason3") + " : " + request.getParameter("comment2");
			StringBuilder commentTemp = new StringBuilder();
//			Timestamp sysDate = new Timestamp((new Date()).getTime());
			commentTemp.append("<b>").append(session.getUserName()).append(" <b>");
			commentTemp.append("<b>[").append(Misc.printDate(sdf, new Date())).append("] : <b>");
			if(!"".equals(reason))
				commentTemp.append("<b>[").append(reason).append("] : <b>");
			comment2 = commentTemp.toString() + comment2 +"<br>";
			comment2 = comment1 != null ? "null".equalsIgnoreCase(comment1) ? comment2 : comment1+comment2 : comment2;
//			engEvtTrackBean.setComment2(request.getParameter("comment2"));
			engEvtTrackBean.setReason3(Misc.getParamAsInt(request.getParameter("reason3")));
			
			engEvtTrackBean.setComment3(request.getParameter("comment3"));
			engEvtTrackBean.setQuestion(request.getParameter("question"));
			String label = request.getParameter("label");
			boolean isEnggEventTrack = request.getParameter("isEnggEventTrack") == null ? false : "".equals(request.getParameter("isEnggEventTrack")) ? false : "true".equalsIgnoreCase(request.getParameter("isEnggEventTrack")) ? true : false;
		    ReportDao reportDao = new ReportDao();
		    try {
		    	if(isEnggEventTrack){
		    		if("Manager".equalsIgnoreCase(label)){
		    			engEvtTrackBean.setComment1(comment2);
		    			engEvtTrackBean.setReason1(Misc.getParamAsInt(request.getParameter("reason2")));
		    			engEvtTrackBean.setReason2UpdatedById(session.getUserId());
		    			reportDao.updateEngineEventsTrackManager(conn, engEvtTrackBean);
		    		}else{
		    			engEvtTrackBean.setComment1(comment2);
		    			engEvtTrackBean.setReason1UpdatedById(session.getUserId());
		    			engEvtTrackBean.setReason1(Misc.getParamAsInt(request.getParameter("reason2")));
		    			reportDao.updateEngineEventsTrackSupervisor(conn, engEvtTrackBean);
		    		}
		    	}else{
		    		engEvtTrackBean.setAlarmCreatedById(session.getUserId());
		    		engEvtTrackBean.setStatus(1);
		    		engEvtTrackBean.setComment1(comment2);
		    		reportDao.insertEngineEventsTrack(conn, engEvtTrackBean);
		    	}
			} catch (GenericException e) {
				System.out.println("RuleSetAction.saveEngineEventsTrack()"+ e.getMessage());				
				e.printStackTrace();
				throw e;
			}
					
		return SAVE;
	}
	
	private int getEventType(String pgContext){
		if("tr_event_report.xml".equalsIgnoreCase(pgContext))
			return 1;
		else if("re_event_report.xml".equalsIgnoreCase(pgContext))
			return 2;
		else
			return 0;
	}

//	try {
//		if(request.getParameter("reason1UpdatedOn") != null)
//			engEvtTrackBean.setReason1UpdatedOn(sdf.parse(request.getParameter("reason1UpdatedOn")));
//	} catch (Exception e1) {
//		e1.printStackTrace();
//	}
//	try {
//		if(request.getParameter("reason2UpdatedOn") != null)
//			engEvtTrackBean.setReason2UpdatedOn(sdf.parse(request.getParameter("reason2UpdatedOn")));
//	} catch (Exception e1) {
//		e1.printStackTrace();
//	}
//	try {
//		if(request.getParameter("reason3UpdatedOn") != null)
//			engEvtTrackBean.setReason3UpdatedOn(sdf.parse(request.getParameter("reason3UpdatedOn")));
//	} catch (Exception e1) {
//		e1.printStackTrace();
//	}
//	try {
//		if(request.getParameter("questCreatedOn") != null)
//			engEvtTrackBean.setQuestCreatedDate(sdf.parse(request.getParameter("questCreatedOn")));
//	} catch (Exception e1) {
//		e1.printStackTrace();
//	}
//	public static void printSelectBox(ArrayList<Pair<Integer, String>> opstationList, ArrayList<Integer> secondList, JspWriter out) throws IOException {
//        if (opstationList != null && opstationList.size() > 0) {
//              for (int i = 0; i < opstationList.size(); i++) {
//                    Pair pair = (Pair) opstationList.get(i);
//                    int first = ((Integer) pair.first).intValue();
//                    out.write("<option value='" + first + "'");
//                    if (secondList != null && secondList.size() >= 0) {
//                          for (int j = 0; j < secondList.size(); j++) {
//                                if (((Integer) secondList.get(j)).intValue() == first) {
//                                      out.write("selected=selected");
//                                }
//                          }
//                    }
//                    out.write(">" + pair.second + "</option>");
//              }
//        }
//    }

}

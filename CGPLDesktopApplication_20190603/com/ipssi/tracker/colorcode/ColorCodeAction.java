package com.ipssi.tracker.colorcode;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTION;
import static com.ipssi.tracker.common.util.ApplicationConstants.CREATE;
import static com.ipssi.tracker.common.util.ApplicationConstants.EDIT;
import static com.ipssi.tracker.common.util.ApplicationConstants.SAVE;
import static com.ipssi.tracker.common.util.Common.getParamAsString;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.reporting.customize.ConfigColumnMaster;
import com.ipssi.tracker.web.ActionI;

public class ColorCodeAction implements ActionI {

	
	String action = "" ;
	
	String actionFarword =  "/colorCodeDetail.jsp";
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		action =Misc.getParamAsString(request.getParameter(ACTION));
		Connection conn = InitHelper.helpGetDBConn(request);
		SessionManager _session = InitHelper.helpGetSession(request);
		int portNodeId = Misc.getParamAsInt(request.getParameter("applicableTo"),Misc.getParamAsInt(_session.getParameter("pv123")));
	
		
		
		if(SAVE.equals(action))
		{
			ColorCodeBean colorCodeBean = new ColorCodeBean();
			List colorCodeBeanList = new ArrayList<ColorCodeBean>();
			String xmlDoc = getParamAsString(request.getParameter("xmlDoc"));
			String name = getParamAsString(request.getParameter("name"));
			String notes = getParamAsString(request.getParameter("notes"));
			int applicableTo = Misc.getParamAsInt(getParamAsString(request.getParameter("applicableTo")));
			int reportType = Misc.getParamAsInt(request.getParameter("report_type"));
			int granularity = Misc.getParamAsInt(request.getParameter("granuality"));
			int aggregation = Misc.getParamAsInt(request.getParameter("aggregation"));
			colorCodeBean.setNotes(notes);
			colorCodeBean.setName(name);
			colorCodeBean.setReportType(reportType);
			colorCodeBean.setPortNode(applicableTo);
			colorCodeBean.setGranuality(granularity);
			colorCodeBean.setAggrigation(aggregation);
			colorCodeBean.setStatus(Misc.getParamAsInt(request.getParameter("status")));
			Document xmldoc = MyXMLHelper.loadFromString(xmlDoc);
			NodeList nlist = xmldoc.getElementsByTagName("d");
			int size = nlist.getLength();
			for (int i = 0; i < size; i++) {
				org.w3c.dom.Node n = nlist.item(i);
				Element e = (Element) n;
				if(e.getAttribute("report_column")!=null && e.getAttribute("report_column")!="" )
				{
					ColorCodeBean colorCodeDetailBean = new ColorCodeBean();
					colorCodeDetailBean.setColumnName(e.getAttribute("report_column"));
					//colorCodeDetailBean.setColumnId(Misc.getParamAsInt(e.getAttribute("report_column")));
					//colorCodeDetailBean.setAggrigation(Misc.getParamAsInt(Misc.getParamAsString(e.getAttribute("aggregation"))));
					//colorCodeDetailBean.setGranuality(Misc.getParamAsInt(Misc.getParamAsString(e.getAttribute("granuality"))));
					colorCodeDetailBean.setOrder(Misc.getParamAsInt(Misc.getParamAsString(e.getAttribute("order"))));
					colorCodeDetailBean.setThresholdOne(Misc.getParamAsInt(Misc.getParamAsString(e.getAttribute("thresholdOne"))));
					colorCodeDetailBean.setThresholdTwo(Misc.getParamAsInt(Misc.getParamAsString(e.getAttribute("thresholdTwo"))));
					colorCodeDetailBean.setChkAll(Misc.getParamAsInt(Misc.getParamAsString(e.getAttribute("chkAll"))));
					colorCodeBeanList.add(colorCodeDetailBean);
				}
			}
			ColorCodeDao.saveColorCodeDetail(conn , colorCodeBean , colorCodeBeanList, Misc.getParamAsInt(request.getParameter("id")));
			actionFarword =  "/colorCodeDetail.jsp?action=search";
		}
		if(CREATE.equals(action))
		{
			actionFarword =  "/colorPatternEdit.jsp?tr=PageNew";
		}
		if(EDIT.equals(action))
		{
			int colorCodeId = Misc.getParamAsInt(request.getParameter("id"));
			ColorCodeBean detailColorBean = ColorCodeDao.getDetail(conn, colorCodeId);
			request.setAttribute("detailColorBean", detailColorBean);
			
			actionFarword =  "/colorPatternEdit.jsp?tr=editPage";
		} else {
			SessionManager session = InitHelper.helpGetSession(request);
			
			String pgContext = Misc.getParamAsString(session.getParameter("page_context"), "tr_trip_setup_color_pattern");
			session.getUser().loadParamsFromMenuSpec(session, pgContext);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(session.request, pgContext);
			int v123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
			String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext + "9008";
			int status = Misc.getParamAsInt(session.getParameter(topPageContext),com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE);
			ArrayList<ColorCodeBean> colorInfoList = ColorCodeDao.getColorCodeDetail(conn,v123,status);
			request.setAttribute("colorInfoList",colorInfoList);
		}
		
		return actionFarword;
	}

	
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	public static List getFrontpageInfo(FrontPageInfo fPageInfo)
	{
		List rbList = new ArrayList<ConfigColumnMaster>();
		List columnList = new ArrayList<DimConfigInfo>();
		if(fPageInfo != null){
			List dimConfigInfoList = fPageInfo.m_frontInfoList;
		
			DimConfigInfo dimConfigInfo = null;
			
			for (int i = 0; i < dimConfigInfoList.size(); i++) {
				dimConfigInfo= (DimConfigInfo)dimConfigInfoList.get(i);
				
				
				if(dimConfigInfo != null && !dimConfigInfo.m_hidden){
					columnList.add(dimConfigInfo);
					
			}
		}
		
		
			
	}
		return columnList;
}
}

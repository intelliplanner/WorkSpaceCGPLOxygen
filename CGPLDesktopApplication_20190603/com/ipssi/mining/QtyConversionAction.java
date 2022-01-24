package com.ipssi.mining;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

public class QtyConversionAction   implements ActionI{
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if("save".equals(action))
				saveAnnProd(request, response);
			ArrayList<QtyConversionBean> infoList = getAnnProd(request);
			request.setAttribute("infoList", infoList);
		}
		catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}
		actionForward = "/quantity_conversion.jsp";
		return actionForward;
	}

	

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		return "/quantity_conversion.jsp";
	}
	
	
	public void saveAnnProd(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String xml = Common.getParamAsString(request.getParameter("XML_DATA"));
		SessionManager session = InitHelper.helpGetSession(request);
		int v123 = Misc.getParamAsInt(session.getParameter("applicableTo"));
		if (Misc.isUndef(v123))
			v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		ArrayList<QtyConversionBean> beanList = new ArrayList<QtyConversionBean>();
	    for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
	    	if (n.getNodeType() != 1)
	    		continue;
	    	Element e = (Element) n;
	    	int materialId = Misc.getParamAsInt(e.getAttribute("material_id"));
	    	int vehType = Misc.getParamAsInt(e.getAttribute("vehicle_type"));
	    	double qty = Misc.getParamAsDouble(e.getAttribute("qty"));
	    	if (Misc.isUndef(materialId) || Misc.isUndef(vehType) || Misc.isUndef(qty))
	    		continue;
	    	
	    	beanList.add(new QtyConversionBean(materialId, vehType, qty));
	    }
	   	QtyConversionBean.saveConversionFactor(session.getConnection(), v123, beanList);
	}
	
	public ArrayList<QtyConversionBean> getAnnProd(HttpServletRequest request) throws Exception {
		ArrayList<QtyConversionBean> retval = new ArrayList<QtyConversionBean>();
		SessionManager session = InitHelper.helpGetSession(request);
		Cache _cache = session.getCache();
		Connection conn = session.getConnection();
		
		MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_prod_plan");
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		int year = Misc.getParamAsInt(request.getParameter(searchBoxHelper.m_topPageContext+22048));
		retval = QtyConversionBean.getConversionFactor(conn,v123);
		return retval;
	}
	
}

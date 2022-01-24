package com.ipssi.mining;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

public class AnnProdAction  implements ActionI{
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if("save".equals(action))
				saveAnnProd(request, response);
			ArrayList<AnnProdBean> infoList = getAnnProd(request);
			request.setAttribute("infoList", infoList);
		}
		catch (GenericException e) {
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		actionForward = "/production_plan.jsp";
		return actionForward;
	}

	

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		return "/production_plan.jsp";
	}
	
	public static int getCurYear() {
		Date cd = new Date();
		if (cd.getMonth() < 3)
			return cd.getYear()-1;
		return cd.getYear();
	}
	
	public void saveAnnProd(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String xml = Common.getParamAsString(request.getParameter("XML_DATA"));
		SessionManager session = InitHelper.helpGetSession(request);
		int v123 = Misc.getParamAsInt(session.getParameter("applicableTo"));
		if (Misc.isUndef(v123))
			v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		int currYear = getCurYear();
		int year = Misc.getParamAsInt(session.getParameter("year"), currYear);
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		ArrayList<AnnProdBean> beanList = new ArrayList<AnnProdBean>();
	    for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
	    	if (n.getNodeType() != 1)
	    		continue;
	    	Element e = (Element) n;
	    	int materialId = Misc.getParamAsInt(e.getAttribute("material_id"));
	    	if (Misc.isUndef(materialId))
	    		continue;
	    	ArrayList<Double> target = new ArrayList<Double>();
	    	
	    	for (int i=0;i<12;i++) {
	    		double qty = Misc.getParamAsDouble(e.getAttribute("v"+i));
	    		target.add(Misc.isUndef(qty) ? null : new Double(qty));
	    	}
	    	beanList.add(new AnnProdBean(materialId, target));
	    }
	   	AnnProdBean.saveProdGoal(session.getConnection(), v123, year, beanList);
	}
	
	public ArrayList<AnnProdBean> getAnnProd(HttpServletRequest request) throws Exception {
		ArrayList<AnnProdBean> retval = new ArrayList<AnnProdBean>();
		SessionManager session = InitHelper.helpGetSession(request);
		Cache _cache = session.getCache();
		Connection conn = session.getConnection();
		
		MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, "tr_prod_plan");
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		int year = Misc.getParamAsInt(request.getParameter(searchBoxHelper.m_topPageContext+22048));
		if (Misc.isUndef(year))
			year = Misc.getParamAsInt(request.getParameter("v"+22048));
		if (Misc.isUndef(year)) 
			year = getCurYear();
		request.setAttribute("year", new Integer(year));
		retval = AnnProdBean.getProdGoal(conn, v123, year, true);
		return retval;
	}
	
}

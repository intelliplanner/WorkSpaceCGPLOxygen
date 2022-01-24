package com.ipssi.inventory;
/**
 * @author balwant
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.web.ActionI;

public class InventoryAction implements ActionI{
	private SessionManager m_session;
	private final static String LIST_PAGE = "/Inventory_ListPage.jsp";
	private final static String DETAIL_PAGE = "/Inventory_DetailPage.jsp";
	private final static String EDIT_PAGE = "/Inventory_EditStockData.jsp";

@Override
public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {

	
	String actionForward = "";
	boolean success = false;
	m_session = InitHelper.helpGetSession(request);
	String action = Misc.getParamAsString(request.getParameter("action"), "LIST");
    if (ApplicationConstants.SAVE.equalsIgnoreCase(action) || "editproduct".equalsIgnoreCase(action)) {
    	String xmlData = request.getParameter("XML_DATA_FIELD");
    	int productId = Misc.getParamAsInt(request.getParameter("p_id"));
    	InventoryMngDao dao = new InventoryMngDao(m_session);
    	dao.saveNewCategoryData(xmlData,productId);
    	success = true;
    }else if("detail".equalsIgnoreCase(action)){
    	System.out.println("In The Detail Page Link");
    	String itemCode = Misc.getParamAsString(request.getParameter("itemcode"));
    	int productId = Misc.getParamAsInt(request.getParameter("p_id"));
    	String[] stockIds = null;
    	InventoryMngDao dao = new InventoryMngDao(m_session);
    	ArrayList<InventoryMngBean> dataList = dao.getInventoryDetailDataList(itemCode,productId,stockIds);
    	request.setAttribute("productId",productId);
    	request.setAttribute("inventoryDataList", dataList);
    	
    	success = true;
    	System.out.println("Method Is Successfully Executed");
    }else if(ApplicationConstants.EDIT.equalsIgnoreCase(action)){
        int pId = Misc.getParamAsInt(request.getParameter("p_id"));
    	String[] stockIds = request.getParameterValues("sotck_id");
    	InventoryMngDao dao = new InventoryMngDao(m_session);
    	
    	String itemCode = Misc.getParamAsString((String)request.getParameter("itemcode_id"));
    	int productId = Misc.getUndefInt();
    	ArrayList<InventoryMngBean> dataList = dao.getInventoryDetailDataList(itemCode,productId,stockIds);
    	request.setAttribute("inventoryDataList", dataList);
    	request.setAttribute("productId", pId);
    	success = true;
    	System.out.println("In The Edit Method");
    }else if ("detailSearch".equalsIgnoreCase(action)) {
		int productId = Misc.getParamAsInt(request.getParameter("product_id"));
		//firstDate,secondDate,lotNumber,mfgName,supplierName
		String lotNumber = Misc.getParamAsString(request.getParameter("lotNumber"));
		String mfgName = Misc.getParamAsString(request.getParameter("mfgName"));
		String supplierName = Misc.getParamAsString(request.getParameter("supplierName"));
		String firstDate = Misc.getParamAsString(request.getParameter("firstDate"));
		String secondDate = Misc.getParamAsString(request.getParameter("secondDate"));
		InventoryMngDao dao = new InventoryMngDao(m_session);
		
		request.setAttribute("inventoryDataList", dao.searchDetailData(productId,lotNumber,mfgName,supplierName,firstDate,secondDate));
		request.setAttribute("productId",productId);
		success = true;
    }else if ("inventorySearch".equalsIgnoreCase(action)) {
	    InventoryMngDao dao = new InventoryMngDao(m_session);
	    request.setAttribute("inventoryDataList", dao.getInventoryList());
	    success = true;
    }else if("release".equalsIgnoreCase(action)){
    	String xmlReleaseStockData = request.getParameter("XML_DATA_FIELD");
    	InventoryMngDao dao = new InventoryMngDao(m_session);
    	dao.saveReleaseStockData(xmlReleaseStockData);
    	success = true;
	}else if("dialog_save_product".equalsIgnoreCase(action)){
    	if(!Misc.isUndef(Misc.getParamAsInt(m_session.getParameter("categoryCode")))){
    		InventoryMngDao dao = new InventoryMngDao(m_session);
        	InventoryMngBean mbean = new InventoryMngBean();
        	mbean.setPortNodeId(Misc.getParamAsInt(m_session.getParameter("port_node_id")));
        	mbean.setCategoryId(Misc.getParamAsInt(m_session.getParameter("categoryCode")));
        	mbean.setItemName(Misc.getParamAsString(m_session.getParameter("itemName")));
        	mbean.setItemCode(Misc.getParamAsString(m_session.getParameter("itemCode")));
        	mbean.setManufacturer(Misc.getParamAsString(m_session.getParameter("manufacturer")));
        	mbean.setManufacturerCode(Misc.getParamAsString(m_session.getParameter("manufacturerCode")));
        	mbean.setNotes(Misc.getParamAsString(m_session.getParameter("itemNotes")));
        	mbean.setProductLife(Misc.getParamAsInt(m_session.getParameter("itemLife")));
        	mbean.setProductLifeUnit(Misc.getParamAsInt(m_session.getParameter("lifeUnit")));
    		dao.saveNewCategoryData(mbean);
    	}
    	success = true;
	}else if("dialog_save_stock".equalsIgnoreCase(action)){
		
	}else{	
		String xmlData = null;
		String xmlStockData = request.getParameter("stockData");
		String xmlEditedStockData = request.getParameter("XML_DATA_FIELD");
		String pId = request.getParameter("product_id");
		InventoryMngDao dao = new InventoryMngDao(m_session);
		if (xmlStockData != null && xmlEditedStockData == null) {
			xmlData = xmlStockData;
		}else if (xmlEditedStockData != null && xmlStockData == null) {
			xmlData = xmlEditedStockData;
			action = "editinventory";
		}
		dao.saveNewStocksData(xmlData);
		if ("editinventory".equalsIgnoreCase(action) && pId!= null && pId.length() > 0) {
	    	String[] stockIds = null;
	    	ArrayList<InventoryMngBean> dataList = dao.getInventoryDetailDataList(null,Misc.getParamAsInt(pId),stockIds);
	    	request.setAttribute("productId",Misc.getParamAsInt(pId));
	    	request.setAttribute("inventoryDataList", dataList);

		}
		System.out.println("do Get !!!!!!");
		success = true;
	}
	actionForward = sendResponse(action, success, request);
	return actionForward;
}
@Override
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		 if ("editproduct".equalsIgnoreCase(action)) {
			return LIST_PAGE;
		}else if ("editinventory".equalsIgnoreCase(action) ||"detail".equalsIgnoreCase(action) || "detailSearch".equalsIgnoreCase(action)) {
			return DETAIL_PAGE;
		}else if (ApplicationConstants.EDIT.equalsIgnoreCase(action)) {
			return EDIT_PAGE;
		}else if ( "inventorySearch".equalsIgnoreCase(action) || "release".equalsIgnoreCase(action)) {
				return LIST_PAGE;
		}else if ( "dialog_save_product".equalsIgnoreCase(action) || "dialog_save_stock".equalsIgnoreCase(action)) {
			return "/genericClose.jsp";
		}
		return "/customizeDetailClose.jsp";
		
	}
}

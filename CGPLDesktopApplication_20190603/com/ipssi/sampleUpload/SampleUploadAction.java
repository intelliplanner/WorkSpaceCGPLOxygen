package com.ipssi.sampleUpload;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.web.ActionI;

public class SampleUploadAction implements ActionI{
	private SessionManager m_session;
	private final static String LIST_PAGE = "/SampleUploadData.jsp";
	
	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		String actionForward = "";
		boolean success = false;
		m_session = InitHelper.helpGetSession(request);
		String action = Misc.getParamAsString(request.getParameter("action"), "LIST");
		
		if ("SAVE_SAMPLING_DATA".equalsIgnoreCase(action)) {
	    	String xmlData = request.getParameter("XML_DATA_FIELD");
	    	int lotId = Misc.getParamAsInt(request.getParameter("lotId"));
	    	int labId = Misc.getParamAsInt(request.getParameter("labId"));
	    	SampleUploadDao dao = new SampleUploadDao(m_session);
	    	dao.saveSampleData(xmlData,lotId,labId);
	    	success = true;
	    }
	    else if ("POST_LOT_SEARCH".equalsIgnoreCase(action)) {
			int lotId = Misc.getParamAsInt(request.getParameter("lotId"));
			String lotNumber = Misc.getParamAsString(request.getParameter("lotNumber"));
			int labId = Misc.getParamAsInt(request.getParameter("labList"));
			SampleUploadDao dao = new SampleUploadDao(m_session);

			request.setAttribute("searchLotId", Integer.toString(lotId));
			request.setAttribute("searchLotNumber",lotNumber);
			request.setAttribute("searchLabId",Integer.toString(labId));
			
			if(Misc.isUndef(lotId) && (lotNumber==null || lotNumber.trim().length()==0 )){
				success = true;
			}else{
				Triple<Integer,String,Integer> val = dao.searchPostLotData(lotId,lotNumber,labId);
				int isAlreadySampleExist = Misc.getUndefInt();
				int lotNotExist = Misc.getUndefInt();
				if(val != null){
					//request.setAttribute("postLotId",!Misc.isUndef(val.first) ? Integer.toString(val.first) : "");
					//request.setAttribute("lotNumber",val.second);
					//request.setAttribute("labId", !Misc.isUndef(val.third) ? Integer.toString(val.third) : "");
					request.setAttribute("postlotExist",Integer.toString(1));
				}else{
					isAlreadySampleExist = dao.isExistSampleUploadForSelectedLab(lotId,labId);
					request.setAttribute("labDataExist",Integer.toString(isAlreadySampleExist));
				}
				success = true;
			}
		}
		
		actionForward = sendResponse(action, success, request);
		return actionForward;
	}
	

	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		if ("inventorySearch".equalsIgnoreCase(action) || "release".equalsIgnoreCase(action)) {
			return LIST_PAGE;
		} else if ("dialog_save_product".equalsIgnoreCase(action) || "dialog_save_stock".equalsIgnoreCase(action)) {
			return "/genericClose.jsp";
		}
		return LIST_PAGE;

	}

}

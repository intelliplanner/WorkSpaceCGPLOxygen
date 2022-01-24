package com.ipssi.grn;

import static com.ipssi.tracker.common.util.ApplicationConstants.VIEW;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.User;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.customer.CustomerContactBean;
import com.ipssi.tracker.web.ActionI;

public class EditGrnAction implements ActionI {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		try{
			if("ready".equals(action)){
				actionForward = viewGRN(request, response);
			}else if("fail".equals(action)){
				actionForward = viewGRN(request, response);
			}else if("reverse".equals(action)){
				actionForward = viewGRN(request, response);
			}else if("save".equals(action)){
				actionForward = saveGRN(request, response);
			}
			
			
		} catch (GenericException e) {
			System.out.println("OverrideAction "+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	
	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		String actionForward = action;
		if(action.equals(VIEW)){
			actionForward = "/grnEdit.jsp";
		}
		return actionForward;
	}
	
	public String viewGRN(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		int postStatus;
		int errorMessage=0;
		Connection conn = InitHelper.helpGetDBConn(request);
		String action = Common.getParamAsString(request.getParameter("action"));
		
		GRNDao grnDao= new GRNDao();
		int grnId = Misc.getParamAsInt(request.getParameter("grn_id"));
		postStatus=grnDao.getGRNPostStatus(conn, grnId);
		
		if(("ready".equals(action)||"fail".equals(action))&&postStatus!=5)
		{
			errorMessage=1;
		}
		else if("reverse".equals(action)&&postStatus!=3)
				{
			errorMessage=1;
				}

		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("grn_id", grnId);
		request.setAttribute("action", action);
		
			return VIEW;
	}
	
	
	public String saveGRN(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		int updateStatus=Misc.getUndefInt();
		Connection conn = InitHelper.helpGetDBConn(request);
		String actionReq = Common.getParamAsString(request.getParameter("actionReq"));
		String notes=Common.getParamAsString(request.getParameter("notes"));
		int errorMessage=1;
		GRNDao grnDao= new GRNDao();
		int grnId = Misc.getParamAsInt(request.getParameter("grn_id"));
		
		
		if("ready".equals(actionReq))
		{
			updateStatus=1;
		}
		else if("fail".equals(actionReq))
				{
			updateStatus=4;
				}
		else if("reverse".equals(actionReq))
		{
			updateStatus=6;
		}

		errorMessage=grnDao.updateGRNPostStatus(conn, grnId,updateStatus,notes)?2:1;
		request.setAttribute("errorMessage", errorMessage);
		request.setAttribute("grn_id", grnId);
		request.setAttribute("action", actionReq);
		
			return VIEW;
	}

	
		
}
 
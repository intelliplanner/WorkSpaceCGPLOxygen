package com.ipssi.rfid.override;

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

public class OverrideAction implements ActionI {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {
		String action = Common.getParamAsString(request.getParameter("action"));
		String actionForward = "";
		boolean success = true;
		String block = Common.getParamAsString(request.getParameter("block"));
		try{
			if("saveBlock".equals(action) && "block".equals(block)){
				actionForward = saveBlock(request, response);
			}else if("cancel_block".equals(action)){
				actionForward = cancelBlock(request, response);
			}else if("block".equals(block)){
				actionForward = viewBlock(request, response);
			}else if("save".equals(action)){
				actionForward = saveBlockStatus(request, response);
			}else if("override_old".equals(action)){
				viewBlockStatus(request, response);
				actionForward = action;
			}else if("override_strict".equals(action)){
				actionForward = viewBlockStatus(request, response);
			}else{
				actionForward = viewBlockStatus(request, response);
			}
		} catch (GenericException e) {
			System.out.println("OverrideAction "+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	private String cancelBlock(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GenericException, Exception {
		int blockInstructionId = Misc.getParamAsInt(request.getParameter("instructionId"));
		if(!Misc.isUndef(blockInstructionId)){
			User user = (com.ipssi.gen.utils.User) request.getAttribute("_user");
			RFIDMasterDao.executeQuery(InitHelper.helpGetDBConn(request), "update block_instruction set status=0,updated_by="+user.getUserId()+" where id="+blockInstructionId);
		}
		return viewBlock(request, response);
	}

	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		String actionForward = action;
		if("viewBlock".equals(action)){
			actionForward = "/blockVehicles.jsp";
		}else if(action.equals(VIEW)){
			actionForward = "/overrideVehicles.jsp";
		}else if(action.equals("override_old")){
			actionForward = "/overrideVehicleOld.jsp";
		}else if(action.equals("override_strict")){
			actionForward = "/overrideVehicleStrict.jsp";
		}/*else{
			actionForward = "/overrideVehicles.jsp";
		}*/
		return actionForward;
	}
	public String saveBlock(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		
		String action = Common.getParamAsString(request.getParameter("action"));
		int vehicleId = Misc.getParamAsInt(request.getParameter("vehicle_id"));
		String vehicleName = Misc.getParamAsString(request.getParameter("vehicleName"));
		
		if(Misc.isUndef(vehicleId)){
			if(vehicleName != null && !"".equals(vehicleName.trim())){
				vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
			}
		}
		String comments = request.getParameter("comments");
		int blockType = Misc.getParamAsInt(request.getParameter("blockType"));
		Date fromDate = null;
		Date toDate = null;
		String fromDateStr = Misc.getParamAsString(request.getParameter("from_date"));
		String toDateStr = Misc.getParamAsString(request.getParameter("to_date"));
		if(fromDateStr != null && fromDateStr.trim().length() > 0){
			fromDate = Misc.getParamAsDate(fromDateStr, null, sdf); 
			if(fromDate == null)
				fromDate = new Date();
		}
        if(toDateStr != null && toDateStr.trim().length() > 0){
        	toDate = Misc.getParamAsDate(toDateStr, null, sdf);
		}
		User user = (com.ipssi.gen.utils.User) request.getAttribute("_user");
		com.ipssi.rfid.beans.BlockingInstruction blockingIns = new com.ipssi.rfid.beans.BlockingInstruction();
		blockingIns.setVehicleId(vehicleId);
		blockingIns.setType(blockType);
		blockingIns.setStatus(1);
		blockingIns.setBlockFrom(fromDate);
		blockingIns.setBlockTo(toDate);
		blockingIns.setCreatedBy(user.getUserId());
		blockingIns.setNotes(comments);
		blockingIns.setCreatedOn(new Date());
		RFIDMasterDao.insert(conn, blockingIns, false);
										
		return viewBlock(request, response);
	}
	public String viewBlock(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		
		Connection conn = InitHelper.helpGetDBConn(request);
		int vehicleId = Misc.getParamAsInt(request.getParameter("vehicle_id"));
		String vehicleName = Misc.getParamAsString(request.getParameter("vehicleName"));
		TPRecord tpr = null;
		
		ArrayList<Object> blockInstructionList = null;
		if(Misc.isUndef(vehicleId)){
			if(vehicleName != null && !"".equals(vehicleName.trim())){
				vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
			}
		}
		if(!Misc.isUndef(vehicleId)){
			VehicleSetup veh = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			if(veh != null)
				vehicleName = veh.m_name;
			tpr = TPRInformation.getLatestTPRForView(conn, vehicleId);
			BlockingInstruction binInstruction = new BlockingInstruction();
			binInstruction.setVehicleId(vehicleId);
			binInstruction.setStatus(1);
			blockInstructionList = RFIDMasterDao.select(conn, binInstruction);
		}
		
		request.setAttribute("blockInstructionList", blockInstructionList);
		request.setAttribute("tprInfo",tpr);
		request.setAttribute("vehicleName",vehicleName);
		request.setAttribute("vehicleId",vehicleId);
		return "viewBlock";
	}
	
	public String saveBlockStatus(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		Connection conn = InitHelper.helpGetDBConn(request);
		
		String action = Common.getParamAsString(request.getParameter("action"));
		int vehicleId = Misc.getParamAsInt(request.getParameter("vehicle_id"));
		String vehicleName = Misc.getParamAsString(request.getParameter("vehicleName"));
		
		if(Misc.isUndef(vehicleId)){
			if(vehicleName != null && !"".equals(vehicleName.trim())){
				vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
			}
		}

		ArrayList<CustomerContactBean> contactList1 =new ArrayList<CustomerContactBean>();
		String xml = Common.getParamAsString(request.getParameter("XML_DATA"));
		System.out.print(xml);
		String comments = request.getParameter("comments");
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("d");
		int size = nList.getLength();
		ArrayList<TPRBlockEntry> tprBlockEntryList = new ArrayList<TPRBlockEntry> ();
		for ( int i=0; i<size ; i++){
			org.w3c.dom.Node node =  nList.item(i);
			org.w3c.dom.Element element = (org.w3c.dom.Element) node;
			TPRBlockEntry tprBlockEntry = new TPRBlockEntry();
			int instructionId = Common.getParamAsInt(element.getAttribute("instructionId"));
			if(instructionId != Misc.getUndefInt()){
				tprBlockEntry.setInstructionId(instructionId);
				
				tprBlockEntryList.add(tprBlockEntry);
			}
		}
		boolean overrideTPR = false;
		if("saveTrip".equals(action))
			overrideTPR = true;
		int overrideStatus = 1;
		User user = (com.ipssi.gen.utils.User) request.getAttribute("_user");
		TPRecord tpr = TPRInformation.getLatestTPRForView(conn, vehicleId);
		TPRBlockManager tprBlockManager = TPRBlockManager.getTprBlockStatus(conn, 463, tpr, Misc.getUndefInt(), tpr != null ? tpr.getMaterialCat() : Misc.getUndefInt(), true);
		if(tprBlockManager != null && tprBlockManager.getBlockStatus() == TPRBlockManager.BLOCKED){
			tprBlockManager.overrideTPRBlockingEntry(conn, vehicleId, tpr, overrideStatus, comments, overrideTPR, user.getUserId());
		}
//		TPRBlockStatusHelper.overrideTPRBlockingEntry(conn, tprInfo.first.getVehicleId(), tprInfo.first, overrideStatus, comments, overrideTPR, user.getUserId());
		return viewBlockStatus(request, response);
	}
	public String viewBlockStatus(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, GenericException, Exception {
		String retval = VIEW;
		Connection conn = InitHelper.helpGetDBConn(request);
		int tprId = Misc.getParamAsInt(request.getParameter("tpr_id"));
		int vehicleId = Misc.getParamAsInt(request.getParameter("vehicle_id"));
		String vehicleName = Misc.getParamAsString(request.getParameter("vehicleName"));
		TPRecord tpr = null;
		ArrayList<TPRBlockEntry> tprBlockEntryList = null;
		if(Misc.isUndef(vehicleId)){
			if(vehicleName != null && !"".equals(vehicleName.trim())){
				vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
			}else if(!Misc.isUndef(tprId)){
				PreparedStatement ps = conn.prepareStatement("select vehicle_id,vehicle_name from tp_record where tpr_id=?");
				Misc.setParamInt(ps, tprId, 1);
				ResultSet rs = ps.executeQuery();
				if(rs.next()){
					vehicleId = Misc.getRsetInt(rs, 1);
					vehicleName = rs.getString(2);
				}
				Misc.closeRS(rs);
				Misc.closePS(ps);
			}
		}
		if(!Misc.isUndef(vehicleId)){
			VehicleSetup veh = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			if(veh != null)
				vehicleName = veh.m_name;
			tpr = TPRInformation.getLatestTPRForView(conn, vehicleId);
			TPRBlockManager tprBlockManager = TPRBlockManager.getTprBlockStatus(conn, 463, tpr, Misc.getUndefInt(), tpr != null ? tpr.getMaterialCat() : Misc.getUndefInt(), true);
			tprBlockEntryList = tprBlockManager != null ? tprBlockManager.getBlockEntries() : null;
		}
		request.setAttribute("tprInfo",tpr);
		request.setAttribute("vehicleName",vehicleName);
		request.setAttribute("vehicleId",vehicleId);
		request.setAttribute("tprBlockEntryList",tprBlockEntryList);
		String action = Common.getParamAsString(request.getParameter("action"));
		if(action.endsWith("override_strict")){
			retval = "/overrideVehicleStrict.jsp?";
			StringBuilder params = new StringBuilder();
			params.append("no_search").append("=").append("0").append("&");
			params.append("pv60242").append("=").append("0").append("&");
			/* params.append("pv20035").append("=").append("").append("&");
			params.append("pv20036").append("=").append("").append("&");
			params.append("pv9002").append("=").append("").append("&"); */
			params.append("_from_link").append("=").append("1").append("&");
			params.append("object_type").append("=").append("2").append("&");
			params.append("tpr_id").append("=").append(vehicleId).append("&");
			params.append("pv60264").append("=").append("1").append("&");
			params.append("manage_multi_tpr_button").append("=").append("1").append("&");
			params.append("pv9002").append("=").append("").append("&");
			params.append("suggested").append("=").append("");
			retval += params.toString();
		}
		return retval;
	}
}
 
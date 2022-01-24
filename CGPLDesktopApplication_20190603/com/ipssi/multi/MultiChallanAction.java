package com.ipssi.multi;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.common.ds.trip.ChallanUpdHelper;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.common.ds.trip.ChallanInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.User;
import com.ipssi.tracker.web.ActionI;
import com.ipssi.userNameUtils.IdInfo;
import com.itextpdf.awt.geom.Point;

public class MultiChallanAction implements ActionI {
	private static Logger logger = Logger.getLogger(MultiChallanAction.class);
	private SessionManager m_session;
//  boolean doMultiRowMaterial = (flagQtyMatDistEmail & 0x10) != 0;
	 
	public static String hackPrintReadPartForChallanUpd(Connection conn, SessionManager session, ChallanBean bean
			,boolean showQty, boolean showMat, boolean showDist, boolean showEmail
			,boolean  showFromOp,boolean  showFromAddr,boolean  showToOp,boolean  showToAddr
			,boolean  doMultiRowMaterial,boolean  singleVehicleMode, boolean showLoadStatus
			,ArrayList<MiscInner.PairIntStr> loadOpList
			,ArrayList<MiscInner.PairIntStr>unloadOpList
			,DimInfo matDim, DimInfo loadStatusDim
			) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		StringBuilder sb = new StringBuilder();
		if (!singleVehicleMode) {
			sb.append("<b>").append(bean.getVehicleName()).append("</b>,");
		}
		
		sb.append("<b>Challan #:").append("</b>").append(bean.getChallanNumber());
		sb.append(", <b>Challan Date:").append("</b>").append(Misc.printDate(df, bean.getChallanDate()));
		if (showLoadStatus)
			sb.append(", <b>Load Status:</b>").append(session.getCache().getAttribDisplayNameFull(session, conn, loadStatusDim, bean.getLoadStatus()));
		if (showFromOp || showFromAddr) {
			sb.append("</br><b>From:</b>");
			if (!Misc.isUndef(bean.getFromStationId())) {
				sb.append(Misc.getNameFromArrayPairIntStr(loadOpList, bean.getFromStationId()));
			}
			else {
				if (bean.getConsignor() != null) {
					sb.append(bean.getConsignor());
					if (bean.getFromAddress() != null) {
						sb.append(", ");
					}
				}
				if (bean.getFromAddress() != null)
					sb.append(bean.getFromAddress());
			}
		}
		if (showToOp || showToAddr) {
			sb.append("</br><b>To:</b>");
			if (!Misc.isUndef(bean.getToStationId())) {
				sb.append(Misc.getNameFromArrayPairIntStr(unloadOpList, bean.getToStationId()));
			}
			else {
				if (bean.getConsignee() != null) {
					sb.append(bean.getConsignee());
					if (bean.getToAddress() != null) {
						sb.append(", ");
					}
				}
				if (bean.getToAddress() != null)
					sb.append(bean.getToAddress());
			}
		}
		if (showMat || showQty) {
			sb.append("<br/>");
			if (showMat) {
				sb.append("<b>Material:</b>");
				if (Misc.isUndef(bean.getMaterialId())) {
					sb.append("N/A");
				}
				else {
					sb.append(session.getCache().getAttribDisplayNameFull(session, conn, matDim, bean.getMaterialId()));
				}
				if (showQty)
					sb.append(", ");
			}
			if (showQty) {
				sb.append("<b>Gross:</b>");
				if (Misc.isUndef(bean.getGrossLoad())) {
					sb.append("N/A");
				}
				else
					sb.append(Misc.printDouble(bean.getGrossLoad(), false));
				sb.append(", <b>Tare:</b>");
				if (Misc.isUndef(bean.getTareLoad())) {
					sb.append("N/A");
				}
				else
					sb.append(Misc.printDouble(bean.getTareLoad(), false));
			}
		}
		if (showDist || showEmail) {
			sb.append("<br/>");
			if (showDist) {
				if (showDist) {
					sb.append("<b>Invoice Dist:</b>");
				}
				if (Misc.isUndef(bean.getInvoiceDistKM())) 
					sb.append("N/A");
				else
					sb.append(Misc.printDouble(bean.getInvoiceDistKM()));
				sb.append(", <b>Orig ETA:</b>");
				if (bean.getOrigETA() <= 0)
					sb.append("N/A");
				else
					sb.append(Misc.printDate(df, bean.getOrigETA()));
				if (showEmail)
					sb.append(", ");
			}
			if (showEmail) {
				sb.append("<b>Email:<b>");
				if (bean.getBillEmail() == null)
					sb.append("N/A");
				else
					sb.append(bean.getBillEmail());
			}
		}
		return sb.toString();
	}
	
	public static String specialPrintString(String varName, boolean hidden, boolean readOnly, String v, int hsz) {
		StringBuilder sb = new StringBuilder();
		if (!readOnly) {
			sb.append("<input type='"+(hidden ? "hidden" : "text")+"' class='tn' name='"+varName+"' value='"+(Misc.printString(v, false))+"' size="+hsz+"/>" );
		}
		if (readOnly)
			sb.append(Misc.printString(v, true));
		return sb.toString();
	}

	public static String specialPrintInt(String varName, boolean hidden, boolean readOnly, int v, int hsz) {
		StringBuilder sb = new StringBuilder();
		if (!readOnly) {
			sb.append("<input type='"+(hidden ? "hidden" : "text")+"' class='tn' name='"+varName+"' value='"+(Misc.printInt(v, false))+"' size="+hsz+"/>" );
		}
		if (readOnly)
			sb.append(Misc.printInt(v, true));
		return sb.toString();
	}

	public static String specialPrintDouble(String varName, boolean hidden, boolean readOnly, double v, int hsz) {
		StringBuilder sb = new StringBuilder();
		if (!readOnly) {
			sb.append("<input type='"+(hidden ? "hidden" : "text")+"' class='tn' name='"+varName+"' value='"+(Misc.printDouble(v, false))+"' size="+hsz+"/>" );
		}
		if (readOnly)
			sb.append(Misc.printDouble(v, true));
		return sb.toString();
	}
	public static String specialPrintSel(Cache cache, Connection conn, SessionManager session, String varName, boolean hidden, boolean readOnly, DimInfo dimInfo, ArrayList<MiscInner.PairIntStr> valList, int selectedId, int hsz, boolean doAny, String doAnyInstruct) throws Exception {
		StringBuilder sb = new StringBuilder();
		String text = null;
		if (readOnly) {
			if (valList != null) {
				for (int i1=0,i1s = valList.size(); i1<i1s;i1++) {
					if (valList.get(i1).first == selectedId) {
						text = valList.get(i1).second;
						break;
					}
				}
			}
			else {
				text = cache.getAttribDisplayNameFull(session, conn, dimInfo, selectedId);
			}
			if (text == null || text.trim().length() == 0)
				text = "&nbsp;";
			else
				text = text.trim();
		}
		if (readOnly)
			sb.append(text);
		if (hidden) {
			sb.append("<input type='hidden' name='"+varName+"' value='"+selectedId+"'/>");
		}
		if (!readOnly && !hidden) {
			if (valList != null) {
				sb.append("<select class='tn' name='"+varName+"'>");
				sb.append(Misc.printOptionsArrayPairIntStr(valList, selectedId, doAny, doAnyInstruct));
				sb.append("</select>");
			}
			else {
			cache.printDimVals(session, conn, session.getUser(), dimInfo, selectedId, null, sb, varName, doAny,  doAnyInstruct, false, Misc.getUndefInt(), 1, hsz
			          , false, null, false, false, false, null, null, Misc.getUndefInt(), Misc.getUndefInt()
			          , null
			          );
			}
		}
		return sb.toString();
	}
	
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		boolean success = true;
		
		m_session = InitHelper.helpGetSession(request);
		String action = request.getParameter("action");
		Connection conn = m_session.getConnection();
		if ("save_challan_create".equals(action)) {//save creation or updating of desp part of challan
			User _user = InitHelper.helpGetUser(request);
			ArrayList<ChallanBean> saveList = this.getInfoForChallanCreate(request, conn);
			int pv123 = Misc.getParamAsInt(m_session.getParameter("pv123"));
			String delChallan = m_session.getParameter("DEL_CHALLAN");
			if (delChallan != null)
				delChallan = delChallan.trim();
			if (delChallan !=  "") {
				ArrayList<Integer> delChallanItems = new ArrayList<Integer>();
				Misc.convertValToVector(delChallan, delChallanItems);
				if (delChallanItems != null && delChallanItems.size() > 0) {
					StringBuilder q = new StringBuilder();
					q.append("update challan_details set trip_status=0, updated_on = now(), challan_rec_date=now() where id in (");
					Misc.convertInListToStr(delChallanItems, q);
					q.append(")");
					PreparedStatement ps = conn.prepareStatement(q.toString());
					ps.executeUpdate();
					ps.close();
				}
			}
			this.saveInfoForChallanCreate(conn, pv123, saveList);
		}
		else if ("save_challan_update".equals(action)) { //save delivery related challan info
			User _user = InitHelper.helpGetUser(request);
			ArrayList<ChallanBean> saveList = this.getInfoForChallanCreate(request, conn);
			int pv123 = Misc.getParamAsInt(m_session.getParameter("pv123"));
			this.saveInfoForChallanUpdate(conn, pv123, saveList);
		}
		else if ("list_challan_update".equals(action)) {//show challans for update status of ongoing challan
			User _user = InitHelper.helpGetUser(request);
			ArrayList<ChallanBean> saveList = getInfoForShow(request, conn, false);
			request.setAttribute("info", saveList);
		}
		else if ("list_challan_create".equals(action)) {//create new challan
			User _user = InitHelper.helpGetUser(request);
			ArrayList<ChallanBean> saveList = new ArrayList<ChallanBean>();//this.getInfoForShow(request, conn, false);
			request.setAttribute("info", saveList);
		}
		else if ("list_challan_edit".equals(action)) {//update challan that has not been yet delivered
			User _user = InitHelper.helpGetUser(request);
			ArrayList<ChallanBean> saveList = this.getInfoForShow(request, conn, true);
			request.setAttribute("info", saveList);
		}
		String actionForward = "";
		actionForward = sendResponse(action, success, request);	
		return actionForward;
	}

	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		if ("save_challan_create".equals(action) && success)
		   return "/customizeDetailClose.jsp";
		if ("save_challan_update".equals(action) && success)
			   return "/customizeDetailClose.jsp";
		if ("list_challan_create".equals(action) && success)
			   return "/multi_challan.jsp";
		if ("list_challan_update".equals(action) && success)
			   return "/multi_challan.jsp";
		else
			return "/multi_challan.jsp";
	}
	
	
	public static ArrayList<MiscInner.PairIntStr> getLoadUnloadLov(Connection conn, int pv123, boolean getLoad) throws Exception {
		String loadopq = "select op_station.id, op_station.name n from op_station join opstation_mapping on (op_station.status in (1) and op_station_id = op_station.id and opstation_mapping.type in (?, 11,15,16,17,24)) join port_nodes anc on (anc.id=opstation_mapping.port_node_id) join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) union select op_station.id, op_station.name from op_station join opstation_mapping_addnl on (op_station.status in (1) and op_station_id = op_station.id and opstation_mapping_addnl.type in (?, 11,15,16,17,24))  join port_nodes anc on (anc.id=opstation_mapping_addnl.port_node_id) join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by n ";
		PreparedStatement psLoad=  conn.prepareStatement(loadopq);
		psLoad.setInt(1, getLoad ? 1:2);
		psLoad.setInt(2, pv123);
		psLoad.setInt(3, getLoad ?1 : 2);
		psLoad.setInt(4, pv123);
		
		ArrayList<MiscInner.PairIntStr> retval = Misc.getLovList(psLoad);
		psLoad.close();
		return retval;
	}
	
	public ArrayList<ChallanBean> getInfoForShow(HttpServletRequest request, Connection conn, boolean getStat1Only) throws Exception {
		try {
			ArrayList<ChallanBean> retval = new ArrayList<ChallanBean>();
			StringBuilder sb = new StringBuilder();
			String[] vehicleId = request.getParameterValues("vehicle_id");
			
			
		sb.append(" select challan_details.id ch_id, vehicle.id vehicle_id, vehicle.name vehicle_name, challan_details.gr_no_, challan_date, from_station_id, fromop.name from_op_name, to_station_id, toop.name to_op_name ")
			.append(" ,mat.row_id mat_id,mat.id mat_code, mat.dispatch_qty, mat.dispatch_notes ")
			.append(" ,mat.received_qty, mat.recieved_notes ")
			.append(" , challan_details.consignor, challan_details.consignee,challan_details.load_gross,  challan_details.load_tare, challan_details.unload_gross, challan_details.unload_tare ")
			.append(" ,challan_details.from_location, challan_details.to_location, challan_details.delivery_date")
			.append(" ,challan_details.orig_eta, challan_details.curr_eta, challan_details.at_cust_arrival ")
			.append(" ,challan_details.invoice_distkm, challan_details.bill_email, challan_details.material_id ")
			.append(" ,challan_details.delivery_notes, challan_details.notes, challan_details.load_status ")
			.append(" from vehicle left outer join challan_details on (vehicle.id = challan_details.vehicle_id and challan_details.trip_status in (1"+(getStat1Only ? "" : ",3")+")) ")
			.append(" left outer join challan_dispatch_item mat on (mat.challan_id = challan_details.id) ")
			.append(" left outer join op_station fromop on (fromop.id = from_station_id) ")
			.append(" left outer join op_station toop on (toop.id = to_station_id) ")
			.append(" where vehicle.status in (1) ")
			;
//				order by vehicle.name, challan_details.challan_date, challan_details.id, mat.id

			if (vehicleId != null && vehicleId.length > 0) {
				sb.append(" and vehicle.id in (");
				Misc.convertInListToStr(vehicleId, sb);
				sb.append(" ) ");
			}
			sb.append(" order by vehicle.name, challan_details.challan_date, challan_details.id, mat.id ");
			Cache cache = Cache.getCacheInstance(conn);
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();

			ChallanBean prevBean = null;
			int pv123 = Misc.getParamAsInt(m_session.getParameter("pv123"));
			
			while (rs.next()) {
//"select distinct vehicle.id, vehicle.customer_id, vehicle.name,  trip_info.id trip_id, trip_info.load_gate_op, trip_info.load_gate_in, trip_info.load_gate_out, challan_details.id challan_id, challan_details.gr_no_, challan_details.challan_date, challan_details.consignor, challan_details.consignee "
				//sb.append("select challan_details.id ch_id, vehicle.id vehicle_id, vehicle.name vehicle_name, challan_details.gr_no_, challan_date, from_station_id, fromop.name from_op_name, to_station_id, toop.name to_op_name ")
				//.append(",mat.id,mat.code mat_code, mat.dispatch_qty, mat.dispatch_notes ")
				//.append(" ,mat.received_qty, mat.recieved_notes ")
			
				int challanId = Misc.getRsetInt(rs, 1);
				if (Misc.isUndef(challanId))
					continue;
				if (prevBean == null || (prevBean != null && prevBean.getChallanId() != challanId)) {
					prevBean = null;
					prevBean = new ChallanBean();
					prevBean.setChallanId(challanId);
					prevBean.setVehicleId(Misc.getRsetInt(rs, "vehicle_id"));
					prevBean.setVehicleName(Misc.getRsetString(rs, "vehicle_name"));
					prevBean.setChallanNumber(Misc.getRsetString(rs, "gr_no_"));
					prevBean.setChallanDate(Misc.sqlToUtilDate(rs.getTimestamp("challan_date")));
					prevBean.setFromStationId(Misc.getRsetInt(rs, "from_station_id"));
					prevBean.setFromStationName(Misc.getRsetString(rs,"from_op_name"));
					prevBean.setToStationId(Misc.getRsetInt(rs, "to_station_id"));
					prevBean.setToStationName(Misc.getRsetString(rs,"to_op_name"));
					prevBean.setConsignor(Misc.getRsetString(rs, "consignor"));
					prevBean.setConsignee(Misc.getRsetString(rs, "consignee"));
					prevBean.setFromAddress(Misc.getRsetString(rs, "from_location"));
					prevBean.setToAddress(Misc.getRsetString(rs, "to_location"));
					prevBean.setGrossLoad(Misc.getRsetDouble(rs, "load_gross"));
					prevBean.setTareLoad(Misc.getRsetDouble(rs, "load_tare"));
					prevBean.setGrossUnload(Misc.getRsetDouble(rs, "unload_gross"));
					prevBean.setTareUnload(Misc.getRsetDouble(rs, "unload_tare"));
					prevBean.setInvoiceDistKM(Misc.getRsetDouble(rs, "invoice_distkm"));
					prevBean.setBillEmail(Misc.getRsetString(rs, "bill_email"));
					prevBean.setMaterialId(Misc.getRsetInt(rs, "material_id"));
					prevBean.setLoadStatus(Misc.getRsetInt(rs, "load_status"));
					prevBean.setCurrETA(Misc.sqlToLong(rs.getTimestamp("curr_eta")));
					prevBean.setOrigETA(Misc.sqlToLong(rs.getTimestamp("orig_eta")));
					prevBean.setAtCustArrivalTS(Misc.sqlToLong(rs.getTimestamp("at_cust_arrival")));
					prevBean.setDeliveryDate(Misc.sqlToLong(rs.getTimestamp("delivery_date")));
					prevBean.setDeliveryNotes(rs.getString("delivery_notes"));
					prevBean.setNotes(rs.getString("notes"));
					retval.add(prevBean);
				}
					
				int matId = Misc.getRsetInt(rs, "mat_id");
				if (Misc.isUndef(matId))
					continue;
				int matCodeId = Misc.getRsetInt(rs, "mat_code");
				if (Misc.isUndef(matCodeId))
					continue;
				double despQty = Misc.getRsetDouble(rs, "dispatch_qty");
				String despNote = rs.getString("dispatch_notes");
				double recQty = Misc.getRsetDouble(rs, "received_qty");
				String recNotes = Misc.getRsetString(rs, "recieved_notes");
				ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> multi_material = prevBean.getMulti_material();
				if (multi_material == null) {
					multi_material = new ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> ();
					prevBean.setMulti_material(multi_material);
				}
				Triple<Integer, Double,String> desp = new Triple<Integer, Double,String>(matCodeId, despQty, despNote);
				 Pair<Double, String> rec = new Pair<Double, String>(recQty, recNotes);					 
				multi_material.add(new Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>(matId, desp, rec));
			}
			rs.close();
			ps.close();
		    return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public ArrayList<ChallanBean> getInfoForChallanCreate(HttpServletRequest request, Connection conn) throws Exception {
		int vehicleId = Misc.getParamAsInt(m_session.getParameter("vehicle_id"));
		String dataStr = request.getParameter("XML_DATA");
		return readChallanFromXML(vehicleId, dataStr);
	}
	public static ArrayList<ChallanBean> readChallanFromXML(int vehicleId, String dataStr)  {
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfHHMM = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		
		ArrayList<ChallanBean> retval = new ArrayList<ChallanBean> ();
		
		
		if (dataStr != null && dataStr.length() > 0) {
			Document dataDoc = MyXMLHelper.loadFromString(dataStr);
			java.util.Date prevDate = null;
			int secIncr = 0;
			for (Node n=dataDoc == null || dataDoc.getDocumentElement() == null ? null : dataDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
				if (n.getNodeType() != 1)
					continue;
				Element e = (Element) n;
				vehicleId = Misc.getParamAsInt(e.getAttribute("vehicle_id"), vehicleId);
				String challanNumber = Misc.getParamAsString(e.getAttribute("challan_number"), null);
				java.util.Date challanDate = Misc.getParamAsDate(e.getAttribute("challan_date"), null, sdfHHMM, sdf);
				int challanId = Misc.getParamAsInt(e.getAttribute("challan_id"));
				int fromStationId = Misc.getParamAsInt(e.getAttribute("from_station"));
				int toStationId = Misc.getParamAsInt(e.getAttribute("to_station"));
				
				if (challanDate == null)
					challanDate = new java.util.Date();
				if (prevDate != null && prevDate.equals(challanDate)) {
					challanDate.setSeconds(challanDate.getSeconds()+(++secIncr));
				}
				else {
					prevDate = challanDate;
				}
				ChallanBean bean = new ChallanBean();
				retval.add(bean);
				bean.setChallanId(challanId);
				bean.setVehicleId(vehicleId);
				bean.setChallanNumber(challanNumber);
				bean.setChallanDate(challanDate);
				bean.setFromStationId(fromStationId);
				bean.setToStationId(toStationId);
				bean.setConsignor(Misc.getParamAsString(e.getAttribute("consignor")));
				bean.setConsignee(Misc.getParamAsString(e.getAttribute("consignee")));
				java.util.Date dttemp = null;
				dttemp = Misc.getParamAsDate(e.getAttribute("delivery_date"), null, sdfHHMM, sdf);
				bean.setDeliveryDate(dttemp == null ? Misc.getUndefInt() : dttemp.getTime());
				dttemp = Misc.getParamAsDate(e.getAttribute("curr_eta"), null, sdfHHMM, sdf);
				bean.setCurrETA(dttemp == null ? Misc.getUndefInt() : dttemp.getTime());
				dttemp = Misc.getParamAsDate(e.getAttribute("orig_eta"), null, sdfHHMM, sdf);
				bean.setOrigETA(dttemp == null ? Misc.getUndefInt() : dttemp.getTime());
				dttemp = Misc.getParamAsDate(e.getAttribute("at_cust_arrival"), null, sdfHHMM, sdf);
				bean.setAtCustArrivalTS(dttemp == null ? Misc.getUndefInt() : dttemp.getTime());
				
				bean.setFromAddress(Misc.getParamAsString(e.getAttribute("from_address")));
				bean.setToAddress(Misc.getParamAsString(e.getAttribute("to_address")));
				bean.setGrossLoad(Misc.getParamAsDouble(e.getAttribute("gross_load")));
				bean.setTareLoad(Misc.getParamAsDouble(e.getAttribute("tare_load")));
				bean.setGrossUnload(Misc.getParamAsDouble(e.getAttribute("gross_unload")));
				bean.setTareUnload(Misc.getParamAsDouble(e.getAttribute("tare_unload")));
				bean.setMaterialId(Misc.getParamAsInt(e.getAttribute("material_id")));
				bean.setDeliveryNotes(Misc.getParamAsString(e.getAttribute("delivery_notes")));
				bean.setNotes(Misc.getParamAsString(e.getAttribute("notes")));
				bean.setInvoiceDistKM(Misc.getParamAsDouble(e.getAttribute("invoice_distkm")));
				bean.setBillEmail(Misc.getParamAsString(e.getAttribute("bill_email")));
				bean.setLoadStatus(Misc.getParamAsInt(e.getAttribute("load_status")));
				for (Node n1 = e.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
					if (n1.getNodeType() != 1)
						continue;
					Element e1 = (Element) n1;
					int matId = Misc.getParamAsInt(e1.getAttribute("mat_id"));
					int matCodeId = Misc.getParamAsInt(e1.getAttribute("mat_code"));
					if (Misc.isUndef(matCodeId))
						continue;
					double despQty = Misc.getParamAsDouble(e1.getAttribute("desp_qty"));
					String despNote = e1.getAttribute("desp_note");
					double recQty = Misc.getParamAsDouble(e1.getAttribute("rec_qty"));
					String recNote = e1.getAttribute("rec_note");
					ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> multi_material = bean.getMulti_material();
					if (multi_material == null) {
						multi_material = new ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> ();
						bean.setMulti_material(multi_material);
					}
					Triple<Integer, Double,String> desp = new Triple<Integer, Double,String>(matCodeId, despQty, despNote);
					 Pair<Double, String> rec = new Pair<Double, String>(recQty, recNote);					 
					multi_material.add(new Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>(matId, desp, rec));
				}
			}
		}
		return retval;
	}
	
	
	public static void saveInfoForChallanCreate(Connection conn, int pv123, ArrayList<ChallanBean> saveList) throws Exception { //returns the list with error
		
		PreparedStatement psIns = conn.prepareStatement("insert into challan_details (vehicle_id, trip_status, challan_rec_date, updated_on) values (?,1,now(),now())");
		PreparedStatement psUpd = conn.prepareStatement("update challan_details set from_station_id=?, to_station_id=?, gr_no_=?, challan_date=?, consignor=?, consignee=?, from_location = ?, to_location=?, load_gross=?, load_tare=?, invoice_distkm=?, bill_email=?, notes=?, material_id=?, load_status=?, orig_eta=?, updated_on = now(), challan_rec_date=now() where id=?" );
		PreparedStatement psMatIns = conn.prepareStatement("insert into challan_dispatch_item (challan_id, id) values (?,?)");
		PreparedStatement psMatUpd = conn.prepareStatement("update challan_dispatch_item set dispatch_qty = ?, dispatch_notes = ? where row_id =? ");
		for (ChallanBean bean: saveList) {
			
			if (Misc.isUndef(bean.getChallanId())) {
				psIns.setInt(1, bean.getVehicleId());
				psIns.executeUpdate();
				ResultSet rs = psIns.getGeneratedKeys();
				if (rs.next())
					bean.setChallanId(rs.getInt(1));
				rs.close();
			}
			//"from_station_id=?, to_station_id=?, gr_no_=?, challan_date=?, consignor=?, consigness=?, from_location = ?, to_location=?

			int colIndex = 1;
			Misc.setParamInt(psUpd, bean.getFromStationId(), colIndex++);
			Misc.setParamInt(psUpd, bean.getToStationId(), colIndex++);
			psUpd.setString(colIndex++, bean.getChallanNumber());
			psUpd.setTimestamp(colIndex++, Misc.utilToSqlDate(bean.getChallanDate()));
			psUpd.setString(colIndex++, bean.getConsignor());
			psUpd.setString(colIndex++, bean.getConsignee());
			psUpd.setString(colIndex++, bean.getFromAddress());
			psUpd.setString(colIndex++, bean.getToAddress());
			//load_gross=?, load_tare=?, invoice_distkm=?, bill_email=?, notes=?, materal=?, load_status=?, updated_on = now(), challan_rec_date=now() where id=?" );

			Misc.setParamDouble(psUpd, bean.getGrossLoad(), colIndex++);
			Misc.setParamDouble(psUpd, bean.getTareLoad(), colIndex++);
			Misc.setParamDouble(psUpd, bean.getInvoiceDistKM(), colIndex++);
			psUpd.setString(colIndex++, bean.getBillEmail());
			psUpd.setString(colIndex++, bean.getNotes());
			
			Misc.setParamInt(psUpd, bean.getMaterialId(), colIndex++);
			Misc.setParamInt(psUpd, bean.getLoadStatus(), colIndex++);
		    psUpd.setTimestamp(colIndex++, Misc.utilToSqlDate(bean.getOrigETA()));
		    
			psUpd.setInt(colIndex++, bean.getChallanId());
			psUpd.addBatch();
			ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> multiMaterial = bean.getMulti_material();
			for (int j=0,js = multiMaterial == null ? 0 : multiMaterial.size(); j<js;j++) {
				Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>> item = multiMaterial.get(j);
				int matId = item.first;
				int matCode = item.second.first;
				double despQty = item.second.second;
				String despNote = item.second.third;
				if (Misc.isUndef(matCode))
					continue;
				if (Misc.isUndef(matId)) {
					psMatIns.setInt(1, bean.getChallanId());
					psMatIns.setInt(2, matCode);
					psMatIns.executeUpdate();
					ResultSet rs = psMatIns.getGeneratedKeys();
					if (rs.next()) {
						matId = rs.getInt(1);
						item.first = matId;
					}
					rs.close();
				}
				Misc.setParamDouble(psMatUpd, despQty, 1);
				psMatUpd.setString(2, despNote);
				psMatUpd.setInt(3, item.first);
				psMatUpd.addBatch();
			}
			psUpd.executeBatch();
			psMatUpd.executeBatch();
			
	//		public static String getMatCodeName(PreparedStatement lookupMatCode, int matCode) throws Exception {
		}
		psUpd.close();
		psMatUpd.close();
		psIns.close();
		psMatIns.close();
		postProcessChallanCreateByBean(conn, saveList);
	}

	public static void saveInfoForChallanUpdate(Connection conn, int pv123, ArrayList<ChallanBean> saveList) throws Exception { //returns the list with error
		PreparedStatement psUpd = conn.prepareStatement("update challan_details set unload_gross=?, unload_tare=?, delivery_date=?, at_cust_arrival=?, delivery_notes=?, curr_eta=?, updated_on = now(), challan_rec_date=now() where id=?");
		PreparedStatement psMatUpd = conn.prepareStatement("update challan_dispatch_item set received_qty = ?, recieved_notes = ? where row_id =? ");
		for (ChallanBean bean: saveList) {
			
			if (Misc.isUndef(bean.getChallanId())) {
				continue;
			}
//           unload_gross=?, unload_tare=?, delivery_date=?, arrivat_at_cust=?, delivery_notes=?, curr_eta=?, updated_on = now(), challan_rec_date=now() where id=?"
			int colIndex = 1;
			Misc.setParamDouble(psUpd, bean.getGrossUnload(), colIndex++);
			Misc.setParamDouble(psUpd, bean.getTareUnload(), colIndex++);
			psUpd.setTimestamp(colIndex++, Misc.utilToSqlDate(bean.getDeliveryDate()));
			psUpd.setTimestamp(colIndex++, Misc.utilToSqlDate(bean.getAtCustArrivalTS()));
			psUpd.setString(colIndex++, bean.getDeliveryNotes());
			psUpd.setTimestamp(colIndex++, Misc.utilToSqlDate(bean.getCurrETA()));
			psUpd.setInt(colIndex++, bean.getChallanId());
			psUpd.addBatch();
			ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> multiMaterial = bean.getMulti_material();
			for (int j=0,js = multiMaterial == null ? 0 : multiMaterial.size(); j<js;j++) {
				Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>> item = multiMaterial.get(j);
				int matId = item.first;
				int matCode = item.second.first;
				double recQty = item.third.first;
				String recNote = item.third.second;
				if (Misc.isUndef(matCode))
					continue;
				if (Misc.isUndef(matId)) {
					continue;
				}
				Misc.setParamDouble(psMatUpd, recQty, 1);
				psMatUpd.setString(2, recNote);
				psMatUpd.setInt(3, item.first);
				psMatUpd.addBatch();
			}
		}
		psUpd.executeBatch();
		psMatUpd.executeBatch();
		psUpd.close();
		psMatUpd.close();
		postProcessChallanUpdateByBean(conn, saveList);
	}
	public static void postProcessChallanCreateByBean(Connection conn, ArrayList<ChallanBean> challanList) throws Exception {
		ArrayList<Integer> challanIdList = new ArrayList<Integer>();
		for (int i=0,is = challanList == null ? 0 : challanList.size(); i <is; i++) {
			challanIdList.add(challanList.get(i).getChallanId());
		}
		ChallanUpdHelper.postProcessChallanCreate(conn, challanIdList);
	}
	public static void postProcessChallanUpdateByBean(Connection conn, ArrayList<ChallanBean> challanList) throws Exception {
		ArrayList<Integer> challanIdList = new ArrayList<Integer>();
		for (int i=0,is = challanList == null ? 0 : challanList.size(); i <is; i++) {
			challanIdList.add(challanList.get(i).getChallanId());
		}
		ChallanUpdHelper.postProcessChallanUpdate(conn, challanIdList);
	}
	
}

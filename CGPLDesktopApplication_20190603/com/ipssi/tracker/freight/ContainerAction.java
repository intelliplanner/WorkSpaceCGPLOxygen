package com.ipssi.tracker.freight;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTION;
import static com.ipssi.tracker.common.util.ApplicationConstants.CONTAINER;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.web.ActionI;

public class ContainerAction implements ActionI {
	
	public static String noShowStatusTag = "Current Status Not Available";
	private static String cleanAndGetPref(HashMap<String, String> userPref, String prop) {
		String billPartyPref = userPref == null ? null : userPref.get(prop);
		if (billPartyPref != null)
			billPartyPref = billPartyPref.trim();
		if (billPartyPref != null && billPartyPref.length() == 0)
			billPartyPref = null;
		return billPartyPref;
	}
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		String actionForward = "";
		String action = "";
		boolean success = false;
		SessionManager m_session = null;
		StringBuilder retval = new StringBuilder();
		retval.append("<data>");
		try {
			Connection conn = InitHelper.helpGetDBConn(request);
			PreparedStatement ps = null;
			m_session = InitHelper.helpGetSession(request);
			action = Misc.getParamAsString(request.getParameter(ACTION));

			if (true) {
				//
				int pv123 = com.ipssi.gen.utils.Misc.getParamAsInt(m_session.getParameter("pv123"));
				String containerNo = Misc.getParamAsString(request.getParameter("container_no"));
				if (containerNo != null)
					containerNo = containerNo.toUpperCase();
				ResultSet rs = null;
				//select user_1_id, name, value from user_preferences where user_1_id = ?
				HashMap<String, String> userPref = m_session.getUser().getUserPreference(conn, true);
				String billPref = cleanAndGetPref(userPref, "bill_party");
				String consigneePref = cleanAndGetPref(userPref, "bill_party");;
				String addnlClause = null;
				if (billPref != null){
					addnlClause = " cd.bill_party like '%"+billPref+"%' ";
				}
				if(billPref != null && !"".equalsIgnoreCase(billPref))
					request.setAttribute("skipSearch", "1");
				System.out.println("ContainerAction.processRequest() : skipSearch ==> " + request.getAttribute("skipSearch"));
//				if (consigneePref != null) {
//					if (addnlClause == null)
//						addnlClause = "";
//					addnlClause += " and cd.consignee like '%"+consigneePref+"%' ";
//				}
						
				
				if (containerNo != null && containerNo.length() > 0) {
					String q = "SELECT trip_info.movement_type,trip_info.load_gate_out,trip_info.unload_gate_in,trip_info.confirm_time" +
					",trip_info.combo_end,gr_no_,vehicle.id,vehicle.name,current_data.name,current_data.gps_record_time,cd.challan_date," +
					" cd.container_1_no,cd.container_2_no,cd.challan_type,cd.tripsheet_no_, "
					+ "cd.container_1_size,cd.container_2_size,cd.challan_type,cd.consignor,cd.bill_party,cd.to_location FROM challan_details cd left outer JOIN "
					+ " vehicle ON ( vehicle.id = cd.vehicle_id) left outer JOIN current_data ON ( vehicle.id = current_data.vehicle_id AND "
					+ " current_data.attribute_id = 0) left outer JOIN trip_info ON ( trip_info.id = cd.trip_info_id" +
					")" + " WHERE (cd.container_1_no LIKE ? OR cd.container_2_no LIKE ?) ";
					if (addnlClause != null)
						q += " and " + addnlClause;
					q += "order by cd.challan_date desc  limit 1";
					System.out.println("ContainerAction.processRequest() containerNo ==> " + containerNo);
					System.out.println("ContainerAction.processRequest() query ==> " + q);
					ps = conn.prepareStatement(q);
					ps.setString(1, "%"+containerNo+"%");
					ps.setString(2, "%"+containerNo+"%");

					rs = ps.executeQuery();

					SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
					while (rs.next()) {
						Date comboEnd = Misc.sqlToUtilDate(rs.getTimestamp("combo_end"));
						String challanType = rs.getString("challan_type");
						String movementType = rs.getString("movement_type");
						if (challanType == null)
							challanType = "UNKNOWN";
						if (movementType == null){
							movementType = "COULD NOT CALC TRIP STATUS";
						}
						String deliveryTag = "On Route";
						boolean isDelivered = false;
						if ("IMPORT".equalsIgnoreCase(challanType)) {
							if (movementType.equalsIgnoreCase("In Factory") || movementType.equalsIgnoreCase("Way Back") || movementType.equalsIgnoreCase("Trip Complete")) {
								deliveryTag = "DESTINATION REACHED";
								isDelivered = true;
							}
						} else if ("EXPORTS".equalsIgnoreCase(challanType)) {
							if (movementType.equalsIgnoreCase("Trip Complete")) {
								deliveryTag = "DESTINATION REACHED";
								isDelivered = true;
							}
						}
						if ( movementType.equalsIgnoreCase("In ICD") ){
							deliveryTag = "In ICD Periphery"; 
						} else if (movementType.equalsIgnoreCase("On Way")) {
							deliveryTag = "On Route";
						} else if (movementType.equalsIgnoreCase("In Factory")) {
							deliveryTag = "In Factory Periphery";
						} else if (movementType.equalsIgnoreCase("Way Back")) {
							deliveryTag = "Way Back"; // should not do this...but there might be a differnt tag that could be utlized
						} else if (movementType.equalsIgnoreCase("Trip Complete")) {
							deliveryTag = "Back In ICD";
						} 
						else if(movementType.equalsIgnoreCase("Trip Complete")) {
							deliveryTag = "DESTINATION REACHED";
							isDelivered = true;
						}
						
						boolean dontShowStatus = false;
						Date currTrackTime = Misc.utilToSqlDate(rs.getTimestamp("gps_record_time"));
						if ( currTrackTime == null ||  ((new Date().getTime()) - currTrackTime.getTime()) > 24*3600*1000 ){
							dontShowStatus = true;
						}
						String currLoc =rs.getString("current_data.name");
						
						if(currLoc == null || currLoc.startsWith("#%#%") ){
							dontShowStatus = true;
						}
						String contNo = rs.getString("container_1_no");
						String contNo2 = rs.getString("container_2_no");
						if ((contNo != null && contNo.toUpperCase().contains(containerNo.toUpperCase())) ||
								(contNo2 != null && contNo2.toUpperCase().contains(containerNo.toUpperCase())) ) {
							retval.append("<conatiner ");
							retval.append(" vehicle_name='").append(isDelivered ? "N/A" : Misc.printString(rs.getString("vehicle.name"),false)).append("' ");
							retval.append(" last_track_time='").append(isDelivered ? Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("combo_end"))) : dontShowStatus ? noShowStatusTag :  Misc.printDate(sdf, currTrackTime)).append("' ");
							retval.append(" last_track_location='").append(isDelivered ? Misc.printString(rs.getString("to_location"), false) :dontShowStatus ? noShowStatusTag : Misc.printString(currLoc, false)).append("' ");
							retval.append(" challan_date='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("challan_date")))).append("' ");
							retval.append(" load_gate_out='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("load_gate_out")))).append("' ");
							retval.append(" unload_gate_in='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("unload_gate_in")))).append("' ");
							retval.append(" back_in_load='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("confirm_time")))).append("' ");
							retval.append(" conatiner_name='").append(Misc.printString(contNo, false)).append("' ");
							String contSize = rs.getString("container_1_size");
							if (contSize != null)
								contSize = contSize.replaceAll("\'", "");
							retval.append(" conatiner_size='").append(Misc.printString(contSize, false)).append("' ");
							retval.append(" challan_type='").append(Misc.printString(challanType, false)).append("' ");
							retval.append(" bill_party='").append(rs.getString("bill_party")== null ? "N/A" : rs.getString("bill_party").replaceAll("&", "&amp;")).append("' ");
							String challanNo = rs.getString("gr_no_");
							if (challanNo != null) {
								challanNo = challanNo.replaceAll("\'", "\\");
							}
							retval.append(" challan_no='").append(Misc.printString(challanNo, false)).append("' ");
							String tripSheetNo = rs.getString("tripsheet_no_");
							if (tripSheetNo != null)
								tripSheetNo = tripSheetNo.replaceAll("\'", "\\");
							retval.append(" tripsheet_no='").append(Misc.printString(tripSheetNo, false)).append("' ");
							retval.append(" vehicle_id='").append(isDelivered ? "N/A" : rs.getInt("vehicle.id")).append("' ");
							retval.append(" delivery_tag='").append(deliveryTag).append("' ");
							// retval.append(" consignor='").append(rs.getString("consignor")).append("' ");
							retval.append(" />");
						}

					}

				} else if (!Misc.isUndef(pv123)) {
					String q = "SELECT trip_info.movement_type,trip_info.load_gate_out,trip_info.unload_gate_in,trip_info.confirm_time,trip_info.combo_end,gr_no_,vehicle.id,vehicle.name,current_data.name,current_data.gps_record_time," +
					" cd.challan_date,cd.container_1_no,cd.container_2_no,cd.challan_type,"
					+ " cd.container_1_size,cd.container_2_size,cd.challan_type,cd.tripsheet_no_,cd.consignor,cd.bill_party FROM challan_details cd left outer JOIN "
					+ " vehicle ON ( vehicle.id = cd.vehicle_id and cd.src=1) left outer JOIN current_data ON ( vehicle.id = current_data.vehicle_id AND "
					+ " current_data.attribute_id = 0) left outer JOIN trip_info ON ( trip_info.id = cd.trip_info_id"
					+ ") left outer JOIN  (SELECT DISTINCT(vehicle.id) vehicle_id FROM vehicle LEFT OUTER JOIN port_nodes custleaf ON "
					+ "(custleaf.id = vehicle.customer_id) LEFT OUTER JOIN  vehicle_access_groups ON (vehicle_access_groups.vehicle_id = vehicle.id) "
					+ "LEFT OUTER JOIN port_nodes leaf ON (leaf.id = vehicle_access_groups.port_node_id)  JOIN port_nodes anc  ON "
					+ "(anc.id IN (?) AND ((anc.lhs_number <= leaf.lhs_number AND anc.rhs_number >= leaf.rhs_number) OR   "
					+ "(anc.lhs_number <= custleaf.lhs_number AND anc.rhs_number >= custleaf.rhs_number))) ) vi ON (vi.vehicle_id = cd.vehicle_id) "
					;
					if (addnlClause != null)
						q += " where " + addnlClause;
					
					q += " group by cd.container_1_no  order by cd.challan_date ";
					System.out.println("ContainerAction.processRequest() query ==> " + q);
					ps = conn.prepareStatement(q);
					ps.setInt(1, pv123);
					rs = ps.executeQuery();
					SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
					
					while (rs.next()) {
						boolean dontShowStatus = false;
						Date currTrackTime = Misc.utilToSqlDate(rs.getTimestamp("gps_record_time"));
						if ( currTrackTime == null ||  ((new Date().getTime()) - currTrackTime.getTime()) > 24*3600*1000 ){
							dontShowStatus = true;
						}
						String currDataName = rs.getString("current_data.name");
						if(currDataName == null || currDataName.startsWith("#%#%") ){
							dontShowStatus = true;
						}
						Date comboEnd = Misc.sqlToUtilDate(rs.getTimestamp("combo_end"));
						String challanType = rs.getString("challan_type");
						if (challanType == null)
							challanType = "UNKNOWN";
						String movementType = rs.getString("movement_type");
						if (movementType == null){
							movementType = "CANT CALC TRIP STATUS";
							continue;
						}
						String deliveryTag = "On Route";
						boolean isDelivered = false;
						if (movementType.equalsIgnoreCase("Trip Complete") && comboEnd != null && (new Date().getTime() - comboEnd.getTime()) > 12 * 60 * 60 * 1000) {
							continue;
						}
						if ( ((new Date().getTime()) - comboEnd.getTime()) > 96 * 60 * 60 * 1000){
							continue;
						}
						if ( movementType.equalsIgnoreCase("In ICD") ){
							deliveryTag = "In ICD Periphery"; 
						} else if (movementType.equalsIgnoreCase("On Way")) {
							deliveryTag = "On Route";
						} else if (movementType.equalsIgnoreCase("In Factory")) {
							deliveryTag = "In Factory Periphery";
						} else if (movementType.equalsIgnoreCase("Way Back")) {
							deliveryTag = "Way Back"; // should not do this...but there might be a differnt tag that could be utlized
						} else if (movementType.equalsIgnoreCase("Trip Complete")) {
							deliveryTag = "Back In ICD";
						}
						String container1 = rs.getString("container_1_no");
						if (container1 != null && !container1.equalsIgnoreCase("") && (container1.indexOf("EMPTY") == -1) ) {
							retval.append("<conatiner ");
							retval.append(" vehicle_name='").append(Misc.printString(rs.getString("vehicle.name"), false)).append("' ");
							retval.append(" last_track_time='").append(isDelivered ? "N/A" : dontShowStatus ? noShowStatusTag : Misc.printDate(sdf,currTrackTime)).append("' ");
							retval.append(" last_track_location='").append(isDelivered ? "N/A" :dontShowStatus ? noShowStatusTag : Misc.printString(currDataName == null ? null : currDataName.replaceAll("&", "&amp;"), false)).append("' ");
							retval.append(" challan_date='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("challan_date")))).append("' ");
							retval.append(" load_gate_out='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("load_gate_out")))).append("' ");
							retval.append(" unload_gate_in='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("unload_gate_in")))).append("' ");
							retval.append(" back_in_load='").append(Misc.printDate(sdf, Misc.sqlToUtilDate(rs.getTimestamp("confirm_time")))).append("' ");
							retval.append(" conatiner_name='").append(Misc.printString(rs.getString("container_1_no"), false)).append("' ");
							String contSize = rs.getString("container_1_size");
							if (contSize != null)
								contSize = contSize.replaceAll("\'", "");

							retval.append(" conatiner_size='").append(contSize).append("' ");
							retval.append(" challan_type='").append(challanType).append("' ");
							retval.append(" bill_party='").append(rs.getString("bill_party")== null ? "N/A" : rs.getString("bill_party").replaceAll("&", "&amp;")).append("' ");
							String challanNo = rs.getString("gr_no_");
							if (challanNo != null) {
								challanNo = challanNo.replaceAll("\'", "\\");
							}
							retval.append(" challan_no='").append(Misc.printString(challanNo, false)).append("' ");
							String tripSheetNo = rs.getString("tripsheet_no_");
							if (tripSheetNo != null)
								tripSheetNo = tripSheetNo.replaceAll("\'", "\\");
							retval.append(" tripsheet_no='").append(Misc.printString(tripSheetNo, false)).append("' ");
							retval.append(" vehicle_id='").append(isDelivered ? "N/A" : rs.getInt("vehicle.id")).append("' ");
							retval.append(" delivery_tag='").append(deliveryTag).append("' ");
							// retval.append(" consignor='").append(rs.getString("consignor")).append("' ");
							retval.append(" />");
						}						
					}
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				}
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		retval.append("</data>");
		request.setAttribute("conatinerXML", retval.toString());
		actionForward = sendResponse(action, success, request);
		return actionForward;
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		if (CONTAINER.equalsIgnoreCase(action)) {
			return "/freightTracking.jsp";
		} else if ("demo".equalsIgnoreCase(action)){
			return "/freightTrackingDemo.jsp";
		}
		else{
			return "/freightTracking.jsp";
		}
	}

}

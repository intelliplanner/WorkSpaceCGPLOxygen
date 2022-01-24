package com.ipssi.reporting.remote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletOutputStream;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.reporting.customize.MenuBean;


public class GenerateDataDao {
	private static final int TRIP_SUMMARY = 0;
	private static final int TRIP_PERFORMANCE = 1;
	private static final String GET_MENU_BY_ID = "select * from menu_master where id = ?";
	private static final String GET_MENU_ID = "select menu_master_id from menu_master_report_definition where report_definition_id=?";
	private static final String GET_REPORT_INFO = "select name, type, page_context from report_definitions where id=?";
	private static final String GET_REPORT_GRANULARITY = " select email_report_frequencies.granularity from email_report_definition  " +
			" left outer join report_definitions on (email_report_definition.report_definition_id=report_definitions.id)   left outer join email_report_frequencies on (email_report_definition.email_report_group_id=email_report_frequencies.email_report_group_id) where report_definitions.id=?";
	private static final String GET_WIEGH_BRIDGE_DETAIL = " select wbdt.load_gate_in,wbdt.vehicle_name,wbdt.vehicle_id,wbdt.opstation_name," +
			" wbdt.opstation_id,wbdt.wb_rule_name,wbdt.wb_rule_id,wbdt.intime,wbdt.outtime,wbdt.direction,wbdt.trip_id from " +
			" (select vi.name vehicle_name, vi.id vehicle_id ," +
			" op_station.name opstation_name, op_station.id opstation_id,rules.name wb_rule_name," +
			" rules.id wb_rule_id,ee.event_start_time intime,ee.event_stop_time outtime," +
			" (case when (TIMESTAMPDIFF(MINUTE, trp1.load_gate_in, ee.event_start_time) > @time) then 1 else 0 end) direction," +
			" trp1.id trip_id, trp1.load_gate_in load_gate_in " +
			" from (select vehicle.id,vehicle.name,vehicle.customer_id from port_nodes anc join port_nodes leaf " +
			" on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join " +
			" vehicle  on vehicle.customer_id = leaf.id where vehicle.status >= 1 order by vehicle.name ) vi " +
			" join opstation_mapping on (opstation_mapping.port_node_id=vi.customer_id) " +
			" join op_station on (op_station.id=opstation_mapping.op_station_id) join opstation_wb_rules on " +
			" (opstation_wb_rules.opstation_id=op_station.id) join rules on (opstation_wb_rules.wb_rule_id = rules.id) " +
			" join engine_events ee on ((ee.rule_id=rules.id) and (vi.id=ee.vehicle_id) " +
			"  and ((ee.event_start_time >= '@start' and ee.event_stop_time <= '@end') or" +
			" (ee.event_start_time <= '@start' and ((ee.event_stop_time >= '@end')" +
			" or (ee.event_stop_time is null)))))" +
			" left outer join trip_info trp1 on ((ee.event_start_time >= trp1.load_gate_in)" +
			" and (ee.event_stop_time <= trp1.load_gate_out or load_gate_out is null) " +
			" and (trp1.load_gate_op = op_station.id) and ((trp1.combo_end >= '@start' and trp1.combo_end <= '@end') " +
			" or (trp1.combo_start <= '@end' and  trp1.combo_start >= '@start') " +
			" or (trp1.combo_start <= '@start' and  (trp1.combo_end >= '@end' or trp1.combo_end is null)))) group by ee.id" +
			" union  select vi.name vehicle_name, vi.id vehicle_id " +
			" ,op_station.name opstation_name, op_station.id opstation_id, rules.name wb_rule_name, " +
			" rules.id wb_rule_id,ee1.event_start_time intime,ee1.event_stop_time outtime, " +
			" (case when (TIMESTAMPDIFF(MINUTE, trp2.unload_gate_in, ee1.event_start_time) > @time) then 1 else 0 end) direction," +
			"trp2.id trip_id , trp2.unload_gate_in load_gate_in " +
			" from (select vehicle.id,vehicle.name,vehicle.customer_id from port_nodes anc join port_nodes leaf " +
			" on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join " +
			" vehicle  on vehicle.customer_id = leaf.id where vehicle.status >= 1 order by vehicle.name ) vi " +
			" join opstation_mapping on " +
			" (opstation_mapping.port_node_id=vi.customer_id) join op_station on " +
			" (op_station.id=opstation_mapping.op_station_id) join opstation_wb_rules on " +
			" (opstation_wb_rules.opstation_id=op_station.id) join rules on " +
			" (opstation_wb_rules.wb_rule_id = rules.id) " +
			" join engine_events ee1 on ((ee1.rule_id=rules.id) and (vi.id=ee1.vehicle_id)" +
			"  and ((ee1.event_start_time >= '@start' and ee1.event_stop_time <= '@end') or" +
			" (ee1.event_start_time <= '@start' and ((ee1.event_stop_time >= '@end') " +
			"or (ee1.event_stop_time is null)))))" +
			" left outer join trip_info trp2 on ((ee1.event_start_time >= trp2.unload_gate_in)" +
			" and (ee1.event_stop_time <= trp2.unload_gate_out or trp2.unload_gate_out is null) " +
			" and (trp2.unload_gate_op = op_station.id) and ((trp2.combo_end >= '@start' and trp2.combo_end <= '@end') " +
			" or (trp2.combo_start <= '@end' and  trp2.combo_start >= '@start') " +
			" or (trp2.combo_start <= '@start' and  (trp2.combo_end >= '@end' or trp2.combo_end is null)))) group by ee1.id) wbdt " ;
	
	public void getXMLData(Connection conn,ServletOutputStream stream, Date startDate, Date endDate, String opStationName, String wbName, String vehicleName, int portNodeId, int direction, boolean error) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		org.w3c.dom.Document doc = null;
		org.w3c.dom.Element root = null;
		org.w3c.dom.Element ele = null,opStation = null,weighBridge = null,vehicle = null;
		StringBuilder query = new StringBuilder();
		StringBuilder where = new StringBuilder();
		Pair<org.w3c.dom.Document,org.w3c.dom.Element> xml = null;
		java.sql.Timestamp start = null;
		java.sql.Timestamp end = null;
		int opStationId = Misc.getUndefInt();
		int wbRuleId = Misc.getUndefInt();
		int vehicleId = Misc.getUndefInt();
		boolean newOP = false, newWb = false;
		String queryStr = null;
		String time = "30";
		try {
			xml = MyXMLHelper.getDocument("WEIGH_BRIDGE_DATA");
			if(xml != null){
				doc = xml.first;
				root = xml.second;
			}
			if(!error){
			query.append(GET_WIEGH_BRIDGE_DETAIL);
			where.append(" where 1=1 ");
			if(startDate != null){
				start = new java.sql.Timestamp(startDate.getTime());
				//where.append(" wbdt.intime ").append(" >= '").append(new java.sql.Date(startDate.getTime())).append("' ");
				if(endDate != null){
					end = new java.sql.Timestamp(endDate.getTime());
					//where.append(" and wbdt.outtime ").append(" < '").append(new java.sql.Date(endDate.getTime())).append("' ");
				}
				else{
					end = start;
					//where.append(" and (wbdt.outtime is null or wbdt.outtime ").append(" < '").append(new java.sql.Date(System.currentTimeMillis())).append("' ").append(" ) ");
				}
			}
			else {
				start = new java.sql.Timestamp(System.currentTimeMillis());
				end = new java.sql.Timestamp(System.currentTimeMillis());
				//where.append(" wbdt.intime ").append(" <= '").append(new java.sql.Date(System.currentTimeMillis())).append("' ");
				//where.append(" and (wbdt.outtime is null or wbdt.outtime ").append(" > '").append(new java.sql.Date(System.currentTimeMillis())).append("' ").append(" ) ");
			}
			if(opStationName != null)
				where.append(" and wbdt.opstation_name like '%").append(opStationName).append("%' ");
			if(wbName != null)
				where.append(" and wbdt.wb_rule_name like '%").append(wbName).append("%' ");
			if(vehicleName != null)
				where.append(" and wbdt.vehicle_name like '%").append(vehicleName).append("%' ");
			if(!Misc.isUndef(direction))
				where.append(" and wbdt.direction =").append(direction);
			query.append(where.toString());
			query.append(" order by wbdt.opstation_id,wbdt.wb_rule_id,wbdt.vehicle_id,wbdt.intime");
			queryStr = query.toString();
			queryStr = queryStr.replaceAll("@start", start+"");
			queryStr = queryStr.replaceAll("@end", end+"");
			queryStr = queryStr.replaceAll("@time", time);
			ps = conn.prepareStatement(queryStr);
			Misc.setParamInt(ps,portNodeId,1);
			Misc.setParamInt(ps,portNodeId,2);
			System.out.println(ps.toString());
			rs = ps.executeQuery();
			while(rs.next())
			{
				if(doc != null){
					if(opStationId != Misc.getRsetInt(rs, "opstation_id")){
						opStationId = Misc.getRsetInt(rs, "opstation_id");
						opStation = MyXMLHelper.addElement(doc, root, "OP_STATION");
						MyXMLHelper.addAttribute(opStation, "NAME", Misc.getRsetString(rs, "opstation_name"));
						newOP = true;
					}
					if(wbRuleId != Misc.getRsetInt(rs, "wb_rule_id") || newOP){
						if(newOP)
							newOP = false;
						wbRuleId = Misc.getRsetInt(rs, "wb_rule_id");
						weighBridge = MyXMLHelper.addElement(doc, opStation, "WEIGH_BRIDGE");
						MyXMLHelper.addAttribute(weighBridge, "NAME", Misc.getRsetString(rs, "wb_rule_name"));
						newWb = true;
					}
					if(vehicleId != Misc.getRsetInt(rs, "vehicle_id") || newWb){
						if(newWb)
							newWb = false;
						vehicleId = Misc.getRsetInt(rs, "vehicle_id");
						vehicle = MyXMLHelper.addElement(doc, weighBridge, "VEHICLE");
						MyXMLHelper.addAttribute(vehicle, "NAME", Misc.getRsetString(rs, "vehicle_name"));
					}
					ele = MyXMLHelper.addElement(doc, vehicle, "DATA");
					//MyXMLHelper.addAttribute(vehicle, "NAME", Misc.getRsetString(rs, "vehicle_name"));
					MyXMLHelper.addAttribute(ele, "INTIME", Misc.getRsetString(rs, "intime"));
					MyXMLHelper.addAttribute(ele, "OUTTIME", Misc.getRsetString(rs, "outtime"));
					MyXMLHelper.addAttribute(ele, "DIRECTION", Misc.getRsetString(rs, "direction"));
					//MyXMLHelper.addAttribute(ele, "TRIPID", Misc.getRsetString(rs, "trip_id"));
					MyXMLHelper.addAttribute(ele, "GATEIN", Misc.getRsetString(rs, "load_gate_in"));
				}
			}
			}
			else
			{
				ele = MyXMLHelper.addElement(doc, root, "ERROR_MESSAGE");
				MyXMLHelper.addDataNode(doc, ele, "AUTHENTICATION FAILED");
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
			if(doc != null)
				MyXMLHelper.getStreamXMLData(doc, stream);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
	}
	public MenuBean getMenuParams(int reportId,Connection conn) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		MenuBean menuBean = null;
		int menu_id = Misc.getUndefInt();
		try {
			ps = conn.prepareStatement(GET_MENU_ID);
			Misc.setParamInt(ps,reportId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				menu_id = Misc.getRsetInt(rs, "menu_master_id");
			}
			ps.clearParameters();
			Misc.closeRS(rs);
			Misc.closePS(ps);
			
			ps = null;
			rs = null;
			if(!Misc.isUndef(menu_id)){
				ps = conn.prepareStatement(GET_MENU_BY_ID);
				Misc.setParamInt(ps,menu_id,1);
				rs = ps.executeQuery();
				while(rs.next())
				{   
					menuBean = new MenuBean();
					menuBean.setId(Misc.getRsetInt(rs, "id"));
					menuBean.setMenuTag(rs.getString("menu_tag"));
					menuBean.setComponentFile(rs.getString("component_file"));
					menuBean.setUserId(Misc.getRsetInt(rs, "user_id"));
					menuBean.setPortNodeId(Misc.getRsetInt(rs, "port_node_id"));
					menuBean.setRowId(Misc.getRsetInt(rs, "row_table"));
					menuBean.setColId(Misc.getRsetInt(rs, "column_table"));
				}
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return menuBean;
	}
	public Triple<String, Integer, Integer> getReportInfo(int reportId,Connection conn) throws SQLException{
		Triple<String, Integer, Integer> retval = new Triple<String, Integer, Integer>("REPORT", 1, 0); 
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			ps = conn.prepareStatement(GET_REPORT_INFO);
			Misc.setParamInt(ps,reportId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				retval.first = Misc.getRsetString(rs, "name", "REPORT");
				retval.second = Misc.isUndef(Misc.getRsetInt(rs, "type")) ? 1 : Misc.getRsetInt(rs, "type");
				String pageContext = rs.getString("page_context");
				retval.third = pageContext != null && pageContext.contains("trip_performance") ? TRIP_PERFORMANCE : TRIP_SUMMARY;
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}
	public int getReportGranularity(int reportId,Connection conn) throws SQLException{
		int retval = Misc.SCOPE_DAY; 
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			ps = conn.prepareStatement(GET_REPORT_GRANULARITY);
			Misc.setParamInt(ps,reportId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				retval = Misc.getRsetInt(rs, "granularity");
			}
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(ps!=null)
				ps.close();
			if(rs!=null)
				rs.close();
		}
		return retval;
	}
}

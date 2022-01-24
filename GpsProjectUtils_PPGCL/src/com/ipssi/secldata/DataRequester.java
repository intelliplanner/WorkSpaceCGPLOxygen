package com.ipssi.secldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class DataRequester {

	//private HashMap<Integer, Pair<Long,Long>> vehicleWithComboStartEnd = new HashMap<Integer, Pair<Long,Long>>();

	public static void main(String[] args) {
		try{
			Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			
			//DataRequester dataRequester = new DataRequester();
			ArrayList<String> dataQueue = new ArrayList<String>();
			DataRequester.populateTripDataForVehicle(conn, 23514, null, 0);
			DataRequester.populateTripDataForPort(conn, 652, null, dataQueue, Misc.getUndefInt());
			System.out.println();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String populateTripDataForVehicle(Connection conn,int vehicleId,Date lastSyncOn,int criticality){
		PreparedStatement ps = null;
		ResultSet rs = null;
		int vehId = Misc.getUndefInt();
		String retval = "";
		Date currSyncDate = null;
		Date eventStartTime = null;
		StringBuilder tripbuilder = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try{
			if (conn != null) {
				String query = "select vehicle.id vehicle_id,vehicle.std_name,pp.combo_start,pp.combo_end,pp.load_gate_in,pp.load_gate_out, "+
				   " pp.load_area_in,pp.load_area_out,pp.unload_gate_in,pp.unload_gate_out,pp.unload_area_in,pp.unload_area_out,pp.confirm_time, "+
				   " pp.load_area_wait_in,pp.unload_area_wait_in, pp.movement_type, ee.event_start_time,ee.event_stop_time,ee.criticality,ee.event_end_name,ee.event_begin_name,rules.name, "+
				   " current_data.gps_record_time from vehicle "+
				   " join current_data on (current_data.vehicle_id = vehicle.id and current_data.attribute_id=0 and vehicle.id=? ) "+
				   " join (select vehicle_id, max(combo_start) cst from trip_info group by vehicle_id) ltp on (ltp.vehicle_id = vehicle.id) "+
				   " join trip_info pp on (pp.vehicle_id = ltp.vehicle_id and pp.combo_start = ltp.cst) "+
				   " left outer join engine_events ee on (ee.vehicle_id = pp.vehicle_id and ee.event_start_time >= pp.combo_start) "+
				   " left outer join rules on (rules.id = ee.rule_id) "+
				   " where  (? is null and pp.unload_area_wait_out is null) "+//-- if no start date "+
				   " or (? is not null and  "+
				   "   ((pp.unload_area_wait_out is null and pp.updated_on > ?) or (pp.unload_area_wait_out > ?)) "+
				   "   and (ee.event_start_time is null or ee.event_start_time > ?) "+
				   "  ) " +
				   //" and ee.criticality in (4,5) " +				    
				   " order by vehicle_id, ee.event_start_time "
				;
				ps = conn.prepareStatement(query);
				java.sql.Timestamp lastSyncTS = Misc.utilToSqlDate(lastSyncOn);
				Misc.setParamInt(ps, vehicleId, 1);
				ps.setTimestamp(2, lastSyncTS);
				ps.setTimestamp(3, lastSyncTS);
				ps.setTimestamp(4, lastSyncTS);
				ps.setTimestamp(5, lastSyncTS);
				ps.setTimestamp(6, lastSyncTS);
				//Misc.setParamInt(ps, criticality, 7);
				//Misc.setParamInt(ps, criticality, 8);
				rs = ps.executeQuery();
				currSyncDate = new Date(System.currentTimeMillis());
				while (rs.next()) {
					int vehIdDB = rs.getInt(1);
					String vehStdName = rs.getString(2);
					if (vehIdDB != vehId) {
						//if(!Misc.isUndef(vehIdDB) && tripbuilder != null)
						tripbuilder = new StringBuilder("<TD");
						vehId = vehIdDB;
						tripbuilder.append("[");
						tripbuilder.append(vehStdName+",");
						tripbuilder.append(rs.getTimestamp(3)+",");
						tripbuilder.append(rs.getTimestamp(4)+",");
						tripbuilder.append(rs.getTimestamp(5)+",");
						tripbuilder.append(rs.getTimestamp(6)+",");
						tripbuilder.append(rs.getTimestamp(7)+",");
						tripbuilder.append(rs.getTimestamp(8)+",");
						tripbuilder.append(rs.getTimestamp(9)+",");
						tripbuilder.append(rs.getTimestamp(10)+",");
						tripbuilder.append(rs.getTimestamp(11)+",");
						tripbuilder.append(rs.getTimestamp(12)+",");
						tripbuilder.append(rs.getTimestamp(13)+",");
						tripbuilder.append(rs.getTimestamp(14)+",");
						tripbuilder.append(rs.getTimestamp(15)+",");
						tripbuilder.append(rs.getString(16)+",");
						tripbuilder.append(rs.getTimestamp(23));
						tripbuilder.append("|EVT");
					}
					String startLoc  = rs.getString(20);
					String endLoc  = rs.getString(21);
					String ruleName  = rs.getString(22);
					eventStartTime = rs.getTimestamp(17);
					Date eventStopTime = rs.getTimestamp(18);
					if(retval == null || (eventStartTime != null  && currSyncDate != null && currSyncDate.getTime() <= eventStartTime.getTime()))
						currSyncDate = eventStartTime;
					tripbuilder.append("[");
					tripbuilder.append(vehStdName+",");
					tripbuilder.append(sdf.format(eventStartTime)+",");
					tripbuilder.append((eventStopTime != null ? sdf.format(eventStopTime) : eventStopTime)+",");
					tripbuilder.append(rs.getInt(19)+",");
					tripbuilder.append((startLoc != null && startLoc.length() > 0 ? startLoc.replaceAll(",","@coma" ): startLoc) +",");
					tripbuilder.append((endLoc != null && endLoc.length() > 0 ? endLoc.replaceAll(",","@coma" ): endLoc)+",");
					tripbuilder.append((ruleName != null && ruleName.length() > 0 ? ruleName.replaceAll(",","@coma" ): ruleName));
					tripbuilder.append("]");
				
				}
				rs.close();
				ps.close();
			}
			retval = (tripbuilder != null ? tripbuilder.toString()+">" : "");
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static Date populateTripDataForPort(Connection conn,int portNodeId,Date lastSyncOn,ArrayList<String> dataQueue,int criticality){
		PreparedStatement ps = null;
		ResultSet rs = null;
		int vehId = Misc.getUndefInt();
		Date retval = null;
		Date eventStartTime = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try{
			if (conn != null) {
				String query = "select vehicle.id vehicle_id,vehicle.std_name,pp.combo_start,pp.combo_end,pp.load_gate_in,pp.load_gate_out, "+
				  "pp.load_area_in,pp.load_area_out,pp.unload_gate_in,pp.unload_gate_out,pp.unload_area_in,pp.unload_area_out,pp.confirm_time, "+
				  "pp.load_area_wait_in,pp.unload_area_wait_in, pp.movement_type, ee.event_start_time,ee.event_stop_time,ee.criticality,ee.event_end_name,ee.event_begin_name,rules.name, "+
				  "current_data.gps_record_time from vehicle join ( select distinct(vehicle.id) vehicle_id,vehicle.std_name  "+
				  "from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join  "+
				  "vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join  "+
				  "port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
				   "or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))  where vehicle.status in (1)) vi on (vehicle.id=vi.vehicle_id) "+
				   "join current_data on (current_data.vehicle_id = vehicle.id and current_data.attribute_id=0) "+
				   "join (select vehicle_id, max(combo_start) cst from trip_info group by vehicle_id) ltp on (ltp.vehicle_id = vehicle.id) "+
				   "join trip_info pp on (pp.vehicle_id = ltp.vehicle_id and pp.combo_start = ltp.cst) "+
				   "left outer join engine_events ee on (ee.vehicle_id = pp.vehicle_id and ee.event_start_time >= pp.combo_start) "+
				   "left outer join rules on (rules.id = ee.rule_id) "+
				   "where  (? is null and pp.unload_area_wait_out is null) "+//-- if no start date "+
				   "or (? is not null and  "+
				   "   ((pp.unload_area_wait_out is null and pp.updated_on > ?) or (pp.unload_area_wait_out > ?)) "+
				    "   and (ee.event_start_time is null or ee.event_start_time > ?) "+
				    "  ) " +				    
				   " order by vehicle_id, ee.event_start_time "
				;
				//OLD_RAHUL String query = "select vehicle.id vehicle_id,vehicle.std_name,pp.combo_start,pp.combo_end,pp.load_gate_in,pp.load_gate_out," +
				//		" pp.load_area_in,pp.load_area_out,pp.unload_gate_in,pp.unload_gate_out,pp.unload_area_in,pp.unload_area_out,pp.confirm_time," +
				//		" pp.load_area_wait_in,pp.unload_area_wait_in, pp.movement_type, ee.event_start_time,ee.event_stop_time,ee.criticality,ee.event_end_name,ee.event_begin_name,rules.name," +
				//		" current_data.gps_record_time from vehicle join ( select distinct(vehicle.id) vehicle_id,vehicle.std_name " +
				//		" from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join " +
				//		" vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join " +
				//		" port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
				//		" or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))  where vehicle.status in (1)) vi on vehicle.id=vi.vehicle_id " +
				//		" join (select ltp.vehicle_id, ltp.combo_start,ltp.combo_end,ltp.load_gate_in,ltp.load_gate_out,ltp.load_area_in,ltp.load_area_out,ltp.unload_gate_in,ltp.unload_gate_out" +
				//		" ,ltp.unload_area_in,ltp.unload_area_out,ltp.confirm_time,ltp.load_area_wait_in,ltp.unload_area_wait_in,ltp.movement_type  " +
				//		" from (select vehicle_id, max(combo_start) dt from trip_info group by vehicle_id) mx join trip_info ltp on (ltp.vehicle_id = mx.vehicle_id and ltp.combo_start = mx.dt) ) " +
				//		" pp on (pp.vehicle_id=vehicle.id)left outer join current_data on (vehicle.id=current_data.vehicle_id and attribute_id=0)" +
				//		" left outer join engine_events ee on (pp.vehicle_id=ee.vehicle_id and ee.event_start_time >= pp.combo_start and ee.event_start_time <= pp.combo_end) " +
				//		" left outer join rules on (ee.rule_id=rules.id)" +
				//		" where  (? is null or ee.event_start_time > ?) and (? is null or ee.criticality = ?) order by vehicle.id";
				ps = conn.prepareStatement(query);
				//OLD_RAHUL Misc.setParamInt(ps, portNodeId, 1);
				// if(lastSyncOn != null){
				// 	ps.setTimestamp(2, new java.sql.Timestamp(lastSyncOn.getTime()));
				// 	ps.setTimestamp(3, new java.sql.Timestamp(lastSyncOn.getTime()));
				// }else{
				// 	ps.setNull(2, Types.TIMESTAMP);
				// 	ps.setNull(3, Types.TIMESTAMP);
				// }
				// Misc.setParamInt(ps, criticality, 4);
				// Misc.setParamInt(ps, criticality, 5);
				Misc.setParamInt(ps, portNodeId, 1);
				java.sql.Timestamp lastSyncTS = Misc.utilToSqlDate(lastSyncOn);
				ps.setTimestamp(2, lastSyncTS);
				ps.setTimestamp(3, lastSyncTS);
				ps.setTimestamp(4, lastSyncTS);
				ps.setTimestamp(5, lastSyncTS);
				ps.setTimestamp(6, lastSyncTS);
				rs = ps.executeQuery();
				StringBuilder tripbuilder = null;
				boolean isClear = false;
				retval = lastSyncOn;
				while (rs.next()) {
					int vehIdDB = rs.getInt(1);
					String vehStdName = rs.getString(2);
					if(!isClear){
						dataQueue.clear();
						isClear = true;
					}
					if (vehIdDB != vehId) {
						if(!Misc.isUndef(vehId) && tripbuilder != null)
							dataQueue.add(tripbuilder.toString()+">");
						tripbuilder = new StringBuilder("<TD");
						vehId = vehIdDB;
						tripbuilder.append("[");
						tripbuilder.append(vehStdName+",");
						tripbuilder.append(rs.getTimestamp(3)+",");
						tripbuilder.append(rs.getTimestamp(4)+",");
						tripbuilder.append(rs.getTimestamp(5)+",");
						tripbuilder.append(rs.getTimestamp(6)+",");
						tripbuilder.append(rs.getTimestamp(7)+",");
						tripbuilder.append(rs.getTimestamp(8)+",");
						tripbuilder.append(rs.getTimestamp(9)+",");
						tripbuilder.append(rs.getTimestamp(10)+",");
						tripbuilder.append(rs.getTimestamp(11)+",");
						tripbuilder.append(rs.getTimestamp(12)+",");
						tripbuilder.append(rs.getTimestamp(13)+",");
						tripbuilder.append(rs.getTimestamp(14)+",");
						tripbuilder.append(rs.getTimestamp(15)+",");
						tripbuilder.append(rs.getString(16)+",");
						tripbuilder.append(rs.getTimestamp(23));
						tripbuilder.append("|EVT");
					}
					String startLoc  = rs.getString(20);
					String endLoc  = rs.getString(21);
					String ruleName  = rs.getString(22);
					eventStartTime = rs.getTimestamp(17);
					Date eventStopTime = rs.getTimestamp(18);
					if(retval == null || (eventStartTime != null  && retval != null && retval.getTime() <= eventStartTime.getTime()))
						retval = eventStartTime;
					tripbuilder.append("[");
					tripbuilder.append(vehStdName+",");
					tripbuilder.append(sdf.format(eventStartTime)+",");
					tripbuilder.append((eventStopTime != null ? sdf.format(eventStopTime) : eventStopTime)+",");
					tripbuilder.append(rs.getInt(19)+",");
					tripbuilder.append((startLoc != null && startLoc.length() > 0 ? startLoc.replaceAll(",","@coma" ): startLoc) +",");
					tripbuilder.append((endLoc != null && endLoc.length() > 0 ? endLoc.replaceAll(",","@coma" ): endLoc)+",");
					tripbuilder.append((ruleName != null && ruleName.length() > 0 ? ruleName.replaceAll(",","@coma" ): ruleName));
					tripbuilder.append("]");
				}
				rs.close();
				ps.close();
				if(tripbuilder != null && tripbuilder.length() > 0) {
					tripbuilder.append(">");
					dataQueue.add(tripbuilder.toString());
				}
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}
	public  String dataRequester(String portNodeId,Connection conn){
		String dataString = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int portNode = Misc.getParamAsInt(portNodeId);
		try{
			System.out.println("DataRequester.dataRequester() Mehtod called");
			if (conn != null) {
				String query = " select  pp.vehicle_id,pp.std_name,pp.combo_start,pp.combo_end,pp.load_gate_in,pp.load_gate_out,pp.load_area_in,pp.load_area_out,pp.unload_gate_in,pp.unload_gate_out,pp.unload_area_in,pp.unload_area_out,pp.confirm_time,pp.load_area_wait_in,pp.unload_area_wait_in,pp.movement_type,event_start_time,event_stop_time,criticality,event_end_name,event_begin_name,rules.name,current_data.gps_record_time from engine_events right outer join (  select vi.vehicle_id,vi.std_name,m1.combo_start,m1.combo_end,m1.load_gate_in,m1.load_gate_out,m1.load_area_in,m1.load_area_out,m1.unload_gate_in,m1.unload_gate_out,m1.unload_area_in,m1.unload_area_out,"+
						" m1.confirm_time,m1.load_area_wait_in,m1.unload_area_wait_in,m1.movement_type from trip_info m1 join ("+
						" select distinct(vehicle.id) vehicle_id,vehicle.std_name from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))  where vehicle.status in (1)) vi on vi.vehicle_id=m1.vehicle_id  LEFT JOIN trip_info m2"+
						"  ON (m1.vehicle_id = m2.vehicle_id AND m1.combo_start < m2.combo_start) WHERE m2.combo_start IS NULL) pp"+
						" on (engine_events.vehicle_id=pp.vehicle_id and ( (event_start_time <= pp.combo_start and (case when event_stop_time is null then now() else event_stop_time end) >= pp.combo_end) or (event_start_time >= pp.combo_start and (case when event_stop_time is null then now() else event_stop_time end) <= pp.combo_end) or (event_start_time <= pp.combo_start and ((case when event_stop_time is null then now() else event_stop_time end) between  pp.combo_start and  pp.combo_end)) or ((event_start_time between pp.combo_start and  pp.combo_end) and (case when event_stop_time is null then now() else event_stop_time end) >= pp.combo_end) ))  join rules on (rules.id=rule_id) left outer join current_data on (current_data.vehicle_id = pp.vehicle_id) ";
				ps = conn.prepareStatement(query);
				ps.setInt(1, portNode);
				rs = ps.executeQuery();
				StringBuilder tripbuilder = new StringBuilder("TD(");
				StringBuilder eventBuilder = new StringBuilder("EVT(");
				storedResultForTripInfo(tripbuilder,eventBuilder,rs);
				eventBuilder.append(")EVT");
				tripbuilder.append(")TD");
				String tripData = tripbuilder.toString();
				String eventData = eventBuilder.toString();
				dataString = tripData.trim()+"|"+eventData.trim();
				rs.close();
				ps.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return dataString;
	}
	private void storedResultForTripInfo(StringBuilder tripbuilder,StringBuilder eventbuilder,ResultSet rs ){
		int vehId = Misc.UNDEF_VALUE;
		Long comboStart;
		Long comboEnd;
		try{
			while (rs.next()) {
				int vehIdDB = rs.getInt(1);
				String vehStdName = rs.getString(2);
				if (vehIdDB != vehId) {
					vehId = vehIdDB;
					tripbuilder.append("[");
					tripbuilder.append(vehStdName+",");
					tripbuilder.append(rs.getTimestamp(3)+",");
					tripbuilder.append(rs.getTimestamp(4)+",");
					tripbuilder.append(rs.getTimestamp(5)+",");
					tripbuilder.append(rs.getTimestamp(6)+",");
					tripbuilder.append(rs.getTimestamp(7)+",");
					tripbuilder.append(rs.getTimestamp(8)+",");
					tripbuilder.append(rs.getTimestamp(9)+",");
					tripbuilder.append(rs.getTimestamp(10)+",");
					tripbuilder.append(rs.getTimestamp(11)+",");
					tripbuilder.append(rs.getTimestamp(12)+",");
					tripbuilder.append(rs.getTimestamp(13)+",");
					tripbuilder.append(rs.getTimestamp(14)+",");
					tripbuilder.append(rs.getTimestamp(15)+",");
					tripbuilder.append(rs.getString(16)+",");
					tripbuilder.append(rs.getTimestamp(17));
					tripbuilder.append("]");
				}
				String startLoc  = rs.getString(20);
				String endLoc  = rs.getString(21);
				String ruleName  = rs.getString(22);
				eventbuilder.append("[");
				eventbuilder.append(vehStdName+",");
				eventbuilder.append(rs.getTimestamp(17)+",");
				eventbuilder.append(rs.getTimestamp(18)+",");
				eventbuilder.append(rs.getInt(19)+",");
				eventbuilder.append((startLoc != null && startLoc.length() > 0 ? startLoc.replaceAll(",","@coma" ): startLoc) +",");
				eventbuilder.append((endLoc != null && endLoc.length() > 0 ? endLoc.replaceAll(",","@coma" ): endLoc)+",");
				eventbuilder.append((ruleName != null && ruleName.length() > 0 ? ruleName.replaceAll(",","@coma" ): ruleName));
				eventbuilder.append("]");
			}

		}catch (Exception e) {
			e.printStackTrace();
		}

	}
}

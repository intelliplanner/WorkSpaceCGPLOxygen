package com.ipssi.dashboard.operator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Value;
import com.ipssi.reporting.cache.CacheManager;

/**
 * @author Jai
 * 
 */
public class OperatorDashboardDao {
	private SessionManager m_session = null;

	/**
	 * 
	 */
	public OperatorDashboardDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	public String fetchData(String selectedRulesCSV, String vehicleName) throws GenericException {
		int pv123 = com.ipssi.gen.utils.Misc.getParamAsInt(m_session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder retval = new StringBuilder();
		boolean hadSqlException = false;
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			conn = m_session.getConnection();
			int userOrgControlId = Misc.getUserTrackControlOrg(m_session);
			MiscInner.PortInfo userOrgControlOrg = m_session.getCache().getPortInfo(userOrgControlId, conn);
			String pgContext = Misc.getParamAsString(m_session.getParameter("page_context"), "tr_control_dashboard");
			String configFile = Misc.getParamAsString(m_session.getParameter("front_page"), "tr_rule_list.xml");
			
			FrontPageInfo frontPageInfo = CacheManager.getFrontPageConfig(conn, (int) m_session.getUserId(), userOrgControlId, pgContext, configFile, 
					Misc.getParamAsInt(m_session.getParameter("row"),0), 0);
			String ruleFilterDefault = Misc.getParamAsString(frontPageInfo.getDefaultSearchCriteria(20141));
			String query = "(select 1 as table_index," +
					"case when temp_alaram_events.alarm_time is null then event_start_time else temp_alaram_events.alarm_time  end ordered_time ," +
					" engine_events.id event_id,vehicle.id vehicle_id, " +
					"vehicle.name as vehicle_name, rule_id,rules.name as rule_name,engine_events.id,engine_events.event_start_time, " +
					"case when engine_events.event_begin_name is null then '' else engine_events.event_begin_name end " +
					"as event_begin_name, engine_events.event_stop_time,  engine_events.event_end_name,    " +
					"(case when event_stop_time is null then greatest(0,timestampdiff(minute,event_start_time,now())) else timestampdiff(minute,event_start_time,event_stop_time) end) diff,  " +
					"engine_events.acknowledgement from singleton, vehicle    join (select  distinct(vehicle.id) " +
					"vehicle_id from vehicle  left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
					"left outer   join vehicle_access_groups on  (vehicle_access_groups.vehicle_id = vehicle.id) left outer join " +
					"port_nodes leaf on    (leaf.id = vehicle_access_groups.port_node_id)  join port_nodes anc  on (anc.id in (?) " +
					"and ((anc.lhs_number <= leaf.lhs_number    and anc.rhs_number >= leaf.rhs_number) or  " +
					"(anc.lhs_number <= custleaf.lhs_number  and anc.rhs_number >= custleaf.rhs_number))) )  vi " +
					"on vi.vehicle_id = vehicle.id  left outer join engine_events on (engine_events.vehicle_id = vehicle.id )   " +
					"left join rules on ( rules.id = engine_events.rule_id) " +
					"left join temp_alaram_events on ( engine_events.id = temp_alaram_events.event_id and temp_alaram_events.table_index = 1 and temp_alaram_events.alarm_time > now() )    " +
					"where vehicle.status = 1 	%ruleFilter %vehicleFilter and  	engine_events.acknowledgement < 2 order by ordered_time desc limit 100) "
					+ "union "
					+ "(select 2 as table_index," +
					"case when temp_alaram_events.alarm_time is null then event_start_time else temp_alaram_events.alarm_time  end ordered_time," +
					//"event_start_time as ordered_time," +
					"region_track_events.id " +
					"event_id, -1 as vehicle_id, op_station.name as vehicle_name,event_type as rule_id," +
					"event_type as rule_name,region_track_events.id,event_start_time,event_type_value as " +
					"event_begin_name, event_stop_time,event_start_time,(case when event_stop_time is null " +
					"then timestampdiff(minute,event_start_time,now()) else " +
					"timestampdiff(minute,event_start_time,event_stop_time) end) diff," +
					"acknowledgement from region_track_events left join op_station on " +
					"( op_station.id = op_station_id) left join opstation_mapping on " +
					"( op_station.id = opstation_mapping.op_station_id) left outer join port_nodes leaf on " +
					"(leaf.id = opstation_mapping.port_node_id) join port_nodes anc  on (anc.id in (?) and " +
					"((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ))" +
					" left join temp_alaram_events on ( region_track_events.id = temp_alaram_events.event_id " +
					"and temp_alaram_events.table_index = 2 " +
					" and temp_alaram_events.alarm_time > now() ) where %tripEvents acknowledgement < 2 order by ordered_time desc limit 100" +
					") "
					+ "order by ordered_time desc limit 100";
			
			if ( selectedRulesCSV.length() > 0 ){
				query = query.replaceAll("%ruleFilter", " and rules.id in ("+selectedRulesCSV+")");
				if(selectedRulesCSV.indexOf("-2") >=0){
					query = query.replaceAll("%tripEvents", " ");
				} else {
					query = query.replaceAll("%tripEvents", " false and ");
					
				}
			} else if (ruleFilterDefault != null && ruleFilterDefault.trim().length() > 0) {
				query = query.replaceAll("%ruleFilter", " and rules.id in ("+ruleFilterDefault+")");
				query = query.replaceAll("%tripEvents", " false and ");
			} else {
				query = query.replaceAll("%ruleFilter", "");
				query = query.replaceAll("%tripEvents", " ");
			}//%vehicleFilter
			if ( vehicleName.length() > 0 ){
				query = query.replaceAll("%vehicleFilter", " and vehicle.name like '%"+vehicleName+"%'");
			} else {
				query = query.replaceAll("%vehicleFilter", "");
			}
			Cache cache = Cache.getCacheInstance();
			ps = conn.prepareStatement(query);
			ps.setInt(1,pv123);
			ps.setInt(2,pv123);
			rs = ps.executeQuery();
			retval.append("<data>");
			HashMap<Integer, String> list = new HashMap<Integer, String>();
			
			list.put(9001, "Queue Length");
			list.put(9002, "Processing Time");
			list.put(9003, "Not Operating");
			list.put(9004, "Stranded Region");
			
			Value valueFormatter = new Value();
			while (rs.next()) {
				int tableIndex = rs.getInt("table_index");
				retval.append("<d vehicle_id='").append(rs.getInt("vehicle_id")).append("' ");
				retval.append(" table_index='").append(tableIndex).append("' ");
				retval.append(" vehicle_name='").append(rs.getString("vehicle_name")).append("' ");
				//session.getCache().getAttribDisplayNameFull(session, session.getConnection(), DimInfo.getDimInfo(9003),trackRegionInfoVO.getVehicleType())
				retval.append(" rule_name='").append(tableIndex == 1 ? rs.getString("rule_name") : cache.getAttribDisplayNameFull(m_session, conn, DimInfo.getDimInfo(20207),rs.getInt("rule_name")) ).append("' ");
				//retval.append(" rule_name='").append(tableIndex == 1 ? rs.getString("rule_name") : cache.getAttribDisplayName(20207,rs.getInt("rule_name")) ).append("' ");
				retval.append(" rule_id='").append(rs.getInt("rule_id")).append("' ");
				retval.append(" event_start_time='").append(sdfTime.format(rs.getTimestamp("ordered_time"))).append("' ");
				retval.append(" event_stop_time='").append(rs.getTimestamp("event_stop_time") == null ? "" : sdf.format(rs.getTimestamp("event_stop_time"))).append("' ");
				retval.append(" event_begin_name='").append(tableIndex == 1 ? rs.getString("event_begin_name").replaceAll("&", "&amp;") : rs.getInt("event_begin_name")).append("' ");
				retval.append(" time_diff='").append(valueFormatter.formatTimeInterval( rs.getInt("diff"))).append("' ");
				retval.append(" event_id='").append(rs.getInt("event_id")).append("' ");
				retval.append(" resoultion_status='").append(rs.getInt("acknowledgement")).append("' ");
				retval.append(" /> ");
			}
			
			
			
			
			rs.close();
			ps.close();
			
			ps = conn.prepareStatement("select v.vehicle_name,vm.id,v.vehicle_id,vm.actual_start,vm.planned_end,vm.actual_end,vm.veh_maint_work_type,vm.veh_maint_detail  from vehicle_maint " +
					"vm join ( select distinct v.id AS vehicle_id,v.name as vehicle_name from vehicle v left outer join port_nodes custleaf on (custleaf.id = v.customer_id) left outer join " +
					"vehicle_access_groups on (vehicle_access_groups.vehicle_id = v.id)    left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id)   left outer join port_nodes anc  " +
					"on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)   or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number) )   where anc.id = ? ) v on ( v.vehicle_id = vm.vehicle_id) where vm.actual_end is null ");
			ps.setInt(1, pv123);
			rs = ps.executeQuery();
			 sdf = new SimpleDateFormat("d/M/yy HH:mm");
			int counter = 0; 
			while (rs.next()) {
				Timestamp planEnd = rs.getTimestamp("planned_end");
				if (planEnd == null) {
					planEnd = rs.getTimestamp("actual_start");
					if (planEnd != null)
						planEnd = new Timestamp(planEnd.getTime()+24*3600);
				}
				counter++;
				retval.append("<vehicleUnavailable vehicle_id='").append(rs.getInt("vehicle_id")).append("' ");
				retval.append(" reason_code='").append(rs.getInt("veh_maint_work_type")).append("' ");
				retval.append(" start_time='").append( rs.getTimestamp("actual_start") == null ?  ""  :   sdf.format(rs.getTimestamp("actual_start"))).append("' ");
				retval.append(" expected_end='").append(planEnd== null ? "" :  sdf.format(planEnd)).append("' ");
				retval.append(" actual_end='").append(rs.getTimestamp("actual_end") == null ? "" : sdf.format(rs.getTimestamp("actual_end"))).append("' ");
				retval.append(" notes='").append(rs.getString("veh_maint_detail") == null ? "" : rs.getString("veh_maint_detail") ).append("' ");
				retval.append(" vehicle_name='").append(rs.getString("vehicle_name")).append("' ");
				retval.append(" reason_name='").append(cache.getAttribDisplayNameFull(m_session, conn, DimInfo.getDimInfo(389),rs.getInt("veh_maint_work_type"))).append("' ");
				//retval.append(" reason_name='").append(cache.getAttribDisplayName(389,rs.getInt("veh_maint_work_type"))).append("' ");
				retval.append(" id='").append(rs.getInt("id")).append("' ");
				retval.append(" /> ");
			}
			
			
			

			rs.close();
			ps.close();
			
			ps = conn.prepareStatement("select count(distinct current_data.vehicle_id) c  from current_data join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))where vehicle.status in (1,3,4,5,6,7,8,9) )vi on ( vi.vehicle_id = current_data.vehicle_id) ");
			ps.setInt(1,pv123);
			rs = ps.executeQuery();
			if ( rs.next()){
				retval.append("<vehicleCount ");
				retval.append("total='").append(rs.getInt("c")).append("' ").append(" breakdown='").append(counter).append("' />");
			} 
			
			retval.append("</data>");
			String tempRetval = retval.toString();
			tempRetval.replaceAll("&", "&amp;");
			tempRetval.replaceAll("<", "&lt;");
			tempRetval.replaceAll(">", "&gt;");
			tempRetval.replaceAll("'", "&apos;");
			tempRetval.replaceAll("\"", "&quot;");
			retval = new StringBuilder(tempRetval);
			
		} catch (SQLException e) {
			e.printStackTrace();
			hadSqlException = true;
			throw new GenericException(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				hadSqlException = true;
				e.printStackTrace();
				throw new GenericException(e);
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retval.toString();
	}

	public boolean acknowlegde(int eventId, int status, int tableIndex) throws GenericException {
		boolean success = false;
		Connection conn = null;
		PreparedStatement ps = null;
		boolean hadSqlException = false;
		try {
			//conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn = m_session.getConnection();
			switch (tableIndex) {
			case 1:
				ps = conn.prepareStatement("update engine_events set acknowledgement = ?,acknowledgement_time =? where id = ?  ");
				break;
			case 2:
				ps = conn.prepareStatement("update region_track_events set acknowledgement = ? where id = ?  ");
				break;

			}
			if (tableIndex == 1) {

				ps.setInt(1, status);
				ps.setTimestamp(2, Misc.getCurrentTimestamp());
				ps.setInt(3, eventId);
			}else{
			ps.setInt(1, status);
			ps.setInt(2, eventId);
			}
			success = true;
			ps.executeUpdate();
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			hadSqlException = true;
			throw new GenericException(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	public void setAlarm(int eventId, int tableIndex) throws GenericException {
		boolean success = false;
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		boolean hadSqlException = false;
		try {
			Timestamp ts = new Timestamp((new Date().getTime()));
			//conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn = m_session.getConnection();

			switch (tableIndex) {
			case 1:
				ps1 = conn.prepareStatement("delete from temp_alaram_events where event_id = ? and table_index = 1");
				ps = conn.prepareStatement("insert into temp_alaram_events(event_id,alarm_time,updated_on,table_index) values(?,?,?,1)  ");
				break;
			case 2:
				ps1 = conn.prepareStatement("delete from temp_alaram_events where event_id = ? and table_index = 2");
				ps = conn.prepareStatement("insert into temp_alaram_events(event_id,alarm_time,updated_on,table_index) values(?,?,?,2)  ");
				break;
			}

			ps1.setInt(1, eventId);
			ps1.executeUpdate();

			ps.setInt(1, eventId);
			ps.setTimestamp(2, new Timestamp(ts.getTime() + 5 * 60 * 1000));
			ps.setTimestamp(3, ts);

			ps.executeUpdate();
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			hadSqlException = true;
			throw new GenericException(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (ps1 != null) {
					ps1.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}

	
	
	public void saveUnavailableVehicles(int vehicleId, int reasonId, String startTime, String expectedEnd,
			String actualEnd, String notes) throws GenericException {
		boolean success = false;
		Connection conn = null;
		PreparedStatement ps = null;
		boolean hadSqlException = false;
		try {
			//"update vehicle_maint set vehicle_id = ? , veh_maint_work_type = ?, actual_end = ?, " +
			//"planned_end = ?, veh_maint_detail = ?, updated_on = ? where id = ?"
			SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
			conn = m_session.getConnection();
			ps = conn.prepareStatement("insert into vehicle_maint(vehicle_id,veh_maint_work_type,actual_start," +
					"planned_end,actual_end,veh_maint_detail," +
					"updated_on) values(?,?,?, ?,?,?,?)");
			ps.setInt(1,vehicleId);
			ps.setInt(2,reasonId);
			
			ps.setTimestamp(3, new Timestamp(sdf.parse(startTime).getTime()));
			if ( !"".equalsIgnoreCase(expectedEnd) && expectedEnd.length() > 0 ) {
				ps.setTimestamp(4, new Timestamp(sdf.parse(expectedEnd).getTime()));	
			} else {
				ps.setNull(4, java.sql.Types.TIMESTAMP);
			}
			if ( !"".equalsIgnoreCase(actualEnd) && actualEnd.length() > 0 ) {
				ps.setTimestamp(5, new Timestamp(sdf.parse(actualEnd).getTime()));	
			} else {
				ps.setNull(5, java.sql.Types.TIMESTAMP);
			}
					
			ps.setString(6, notes);
			
			ps.setTimestamp(7, new Timestamp(new Date().getTime()));
			success = true;
			ps.executeUpdate();
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			hadSqlException = true;
			throw new GenericException(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	 
	
	
	public void updateUnavailableVehicles(int id, int vehicleId, int reasonId, String startTime, String expectedEnd, String actualEnd, String notes) throws GenericException {
		Connection conn = null;
		PreparedStatement ps = null;
		boolean hadSqlException = false;
		try {
			//conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
			conn = m_session.getConnection();
			ps = conn.prepareStatement("update vehicle_maint set vehicle_id = ? , veh_maint_work_type = ?, planned_end = ?, " +
					" actual_end = ?, veh_maint_detail = ?, updated_on = ? where id = ?");
			ps.setInt(1,vehicleId);
			ps.setInt(2,reasonId);
			
			
			if ( !"".equalsIgnoreCase(expectedEnd) && expectedEnd.length() > 0 ) {
				ps.setTimestamp(3, new Timestamp(sdf.parse(expectedEnd).getTime()));	
			} else {
				ps.setNull(3, java.sql.Types.TIMESTAMP);
			}
			if ( !"".equalsIgnoreCase(actualEnd) && actualEnd.length() > 0 ) {
				ps.setTimestamp(4, new Timestamp(sdf.parse(actualEnd).getTime()));	
			} else {
				ps.setNull(4, java.sql.Types.TIMESTAMP);
			}
					
			ps.setString(5, notes);
			
			ps.setTimestamp(6, new Timestamp(new Date().getTime()));
			ps.setInt(7, id);
			ps.executeUpdate();
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			hadSqlException = true;
			throw new GenericException(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	

}

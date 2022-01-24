/**
 * 
 */
package com.ipssi.mapping.common.db;

/**
 * @author jai
 * 
 */
public class DBQueries {
	private static final boolean isMySql = true;

	public static class RouteDefinition {
		public static final String FETCH_OTHER_TYPE_MAP;
		public static final String INSERT_TO_ROUTE_INFO;
		public static final String INSERT_TO_ROUTE;
		public static final String DELETE_FROM_ROUTE;
		public static final String FETCH_ROUTE_LIST;
		public static final String FETCH_ROUTE;
		public static final String UPDATE_ROUTE_INFO;
		public static final String DELETE_ROUTE;
		public static final String DELETE_ROUTE_AFTER_TIME;
		static {
			if (isMySql) {
				FETCH_OTHER_TYPE_MAP = "select id, type from route_type where type!=\"road\"";
				INSERT_TO_ROUTE_INFO = "insert into route_info(route_name,port_node,updated_on, status) values (?,?,?,?)";
				INSERT_TO_ROUTE = "insert into route(route_info_id,name,start_latitude,start_longitude,end_latitude,end_longitude,g,type,updated_on) "
						+ "values ( ?,?,?,?,?,?,GeomFromText(?),?,?)";
				DELETE_FROM_ROUTE = "delete from route where route_info_id = ?";
				FETCH_ROUTE_LIST = "select route_info.id, route_info.route_name, route_info.port_node, route_info.updated_on from port_nodes asked join port_nodes ofroute on (asked.id = ? and ((asked.lhs_number <= ofroute.lhs_number and asked.rhs_number >= ofroute.rhs_number) or (asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= ofroute.lhs_number))) join route_info on (route_info.port_node = ofroute.id)  where route_info.status != ?";
				FETCH_ROUTE = " select name, astext(g) geom,type  from route where route_info_id = ?";
				UPDATE_ROUTE_INFO = "update route_info set route_name=?,port_node=?,updated_on = ?,status=? where id=?";
				DELETE_ROUTE = "update route_info set status = ? where id = ?";
				DELETE_ROUTE_AFTER_TIME = "delete from route where route_info_id = ? and (updated_on) > (?)";
			}
		}
	}

	public static class Playback {
		public static final String FETCH_VEHICLE;
		public static final String FETCH_RULES;
		public static final String DELETE_FROM_VEHICLE_MOVEMENT_LAYER_FOR_SESSION_ID;
		public static final String DELETE_FROM_VEHICLE_MOVEMENT_LAYER_SETUP_FOR_SESSION_ID;
		public static final String DELETE_FROM_VEHICLE_EVENT_LAYER_MAP_FOR_SESSION;
		public static final String DELETE_FROM_VEHICLE_EVENT_LAYER_FOR_SESSION;
		public static final String DELETE_FROM_VEHICLE_EVENT_LAYER_SETUP_FOR_SESSION;
		public static final String FETCH_DATA_FOR_VEHICLE;
		public static final String FETCH_ENGINE_EVENTS;
		public static final String INSERT_INTO_VEHICLE_EVENT_LAYER_SETUP;
		public static final String INSERT_INTO_VEHICLE_EVENT_LAYER;
		public static final String EVENT_QUERY;
		public static final String INSERT_INTO_VEHICLE_MOVEMENT_LAYER;
		public static final String INSERT_INTO_VEHICLE_MOVEMENT_LAYER_SETUP;
		public static final String FETCH_TRIP_DETAIL;
		public static final String FETCH_TRIP_PREF_EVENT_DETAIL;
		public static final String FETCH_TRIP_TP_RECORD_DETAIL;
		static {
			if (isMySql) {
				FETCH_VEHICLE = " select distinct vehicle.id,vehicle.name from  "
								+ " vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " 
								+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " 
								+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " 
								+ " left outer join port_nodes anc  on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
								+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number) ) where anc.id = ? and vehicle.status = 1 order by vehicle.name"; 

				///FETCH_RULES = "select id, name from rules where status=?";
				FETCH_RULES = "select rule.id, rule.port_node_id, rule.name from port_nodes leaf join port_nodes ansc on (ansc.lhs_number <= leaf.lhs_number and "
					+ "ansc.rhs_number >= leaf.rhs_number and leaf.id = ?) join rules rule on (rule.port_node_id = ansc.id) and rule.status = ?";


				DELETE_FROM_VEHICLE_MOVEMENT_LAYER_FOR_SESSION_ID = "delete from vehicle_movement_layer where session_id = ?";
				DELETE_FROM_VEHICLE_MOVEMENT_LAYER_SETUP_FOR_SESSION_ID = "delete from vehicle_movement_layer_setup where session_id = ? ";
				DELETE_FROM_VEHICLE_EVENT_LAYER_MAP_FOR_SESSION = "delete from vehicle_event_layer_map where session_id = ?";
				DELETE_FROM_VEHICLE_EVENT_LAYER_FOR_SESSION = "delete from vehicle_event_layer where session_id = ?";
				DELETE_FROM_VEHICLE_EVENT_LAYER_SETUP_FOR_SESSION = "delete from vehicle_event_layer_setup where session_id = ?";

				FETCH_DATA_FOR_VEHICLE = " select vehicle_id,longitude,latitude,gps_record_time,attribute_id,attribute_value,A.updated_on,gps_id,vehicle.name " + " from ( "
						+ " select vehicle_id,longitude,latitude,gps_record_time,attribute_id,attribute_value,updated_on,gps_id,name  from logged_data where vehicle_id = ? and (gps_record_time) between (?) and (?) " + " union "
						+ " select vehicle_id,longitude,latitude,gps_record_time,attribute_id,attribute_value,updated_on,gps_id,name  from current_data where vehicle_id = ? and (gps_record_time) between (?) and (?) "
						+ " order by gps_record_time ) A " + " left join vehicle on A.vehicle_id = id ";

				FETCH_ENGINE_EVENTS =   " select cc.rule_type_id,ee.* from engine_events ee left join conditions_clauses cc on ee.rule_id = cc.rule_id " +
						" where vehicle_id = ? and ee.rule_id = ?"
						+ " and (event_start_time) between (?) and (?) ";

				INSERT_INTO_VEHICLE_EVENT_LAYER_SETUP = "insert into vehicle_event_layer_setup values(?,?,?)";
				INSERT_INTO_VEHICLE_EVENT_LAYER = "insert into vehicle_event_layer values(?,?,?,?,?,?,?,?,GeomFromText(?))";
				EVENT_QUERY = " insert into vehicle_event_layer_map "
						+ " select session_id, vehicle_index, event_index, rule_name,vehicle_name, event_start_time, event_stop_time,Z.description,attribute_value,ogc_geom,timestampdiff(MINUTE,event_start_time,event_stop_time) duration, ? as point_type"
						+ " from( "
						+ " select A.session_id, B.vehicle_index, C.event_index, D.name rule_name, F.name vehicle_name, A.event_start_time, A.event_stop_time,attribute_id,A.attribute_value,A.ogc_geom "
						+ " from vehicle_event_layer A, vehicle_movement_layer_setup B, vehicle_event_layer_setup C, rules D, vehicle F " + " where A.session_id = B.session_id "
						+ " AND A.session_id = C.session_id " + " AND A.vehicle_id = B.vehicle_id " + " AND A.rule_id = C.event_id " + " AND A.rule_id = D.id "
						+ " AND A.vehicle_id = F.id ) M " + " left join dimensions Z on M.attribute_id = Z.id ";

				INSERT_INTO_VEHICLE_MOVEMENT_LAYER = "insert into vehicle_movement_layer values(?,?,?,?,GeomFromText(?),?)";

				INSERT_INTO_VEHICLE_MOVEMENT_LAYER_SETUP = "insert into vehicle_movement_layer_setup values(?,?,?)";
				
				FETCH_TRIP_DETAIL = "select trip_info.id,trip_info.vehicle_id,load_gate_in,load_gate_out,unload_gate_in," +
						" unload_gate_out,confirm_time,loagreg.id load_region_id,@startTime, @endTime," +
						" unloagreg.id unload_region_id from trip_info left outer join op_station loadop on " +
						" (trip_info.load_gate_op = loadop.id)  left outer join regions loagreg on " +
						" (loadop.gate_reg_id = loagreg.id) left outer join op_station unloadop on " +
						" (trip_info.unload_gate_op = unloadop.id) left outer join regions unloagreg on " +
						" (unloadop.gate_reg_id = unloagreg.id)  where trip_info.id = ? ";
				FETCH_TRIP_TP_RECORD_DETAIL = "select tp_record.tpr_id,tp_record.vehicle_id,earliest_load_gate_in_in,latest_load_gate_in_out,earliest_load_gate_out_in," +
				" latest_load_gate_out_out,null,null load_region_id,@startTime, @endTime," +
				" null unload_region_id from tp_record  where tp_record.tpr_id = ? ";
			    FETCH_TRIP_PREF_EVENT_DETAIL = "select rule_id,color_id,event_type from playback_pref_items join "+
                                               " (select leaf.id  port_id ,playback_pref.id from   port_nodes leaf  join port_nodes anc on (anc.id = ? and leaf.id !=2 and leaf.lhs_number <= anc.lhs_number and leaf.rhs_number >= anc.rhs_number)  join playback_pref on (leaf.id =  playback_pref.port_node_id) order by port_id desc limit 1) vi "+
                                               " on (vi.id = playback_pref_id)";
			}
		}
	}

	public static class JRMDefiniton {
		public static final String INSERT_JRM ;
		public static final String UPDATE_JRM ;
		static {
			if (isMySql) {
		INSERT_JRM =  "insert into jrm_info(risk_level, category, landmark_type, start_hour, start_min, end_hour, end_min, landmark_region_seg_id, status, created_on,category_type) " +
		"values(?,?,?,?,?,?,?,?,?,?,?)";
		UPDATE_JRM = "UPDATE jrm_info SET  risk_level=?, category=?, landmark_type=?, start_hour=?, start_min=?, end_hour=?, end_min=?, updated_on=?, category_type=?  WHERE landmark_region_seg_id = ?";
	}	
		}
		}
	public static class RegionDefiniton {
		public static final String INSERT_REGION;
		public static final String FETCH_REGIONS;
		public static final String FETCH_REGIONS1;
		public static final String DELETE_REGION;
		public static final String UPDATE_REGION;
	
		static {
			if (isMySql) {
				INSERT_REGION = " insert into regions(shape,short_code,port_node_id,description,lowerX,lowerY,upperX,upperY,equal_to_MBR,region_type) "
						+ "values(GeomFromText(?),?,?,?,?,?,?,?,?,?)";
				FETCH_REGIONS = " select astext(regions.shape) shape,regions.description,regions.short_code," +
						" regions.id,regions.lowerX,regions.lowerY,regions.upperX,regions.upperY, regions.port_node_id,regions.region_type" +
						" from port_nodes asked join port_nodes ofroute on (asked.id = ? and ((asked.lhs_number <= ofroute.lhs_number " +
						"and asked.rhs_number >= ofroute.rhs_number) or (asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= " +
						"ofroute.lhs_number))) join regions on (regions.port_node_id = ofroute.id) where  (regions.region_type <> 3 or regions.region_type is null) and regions.priority >= ? and regions.is_artificial != 1 order by regions.short_code ";
				
				FETCH_REGIONS1 = " select astext(regions.shape) shape,regions.description,regions.short_code," +
				" regions.id,regions.lowerX,regions.lowerY,regions.upperX,regions.upperY, regions.port_node_id,regions.region_type" +
				" from port_nodes asked join port_nodes ofroute on (asked.id = ? and ((asked.lhs_number <= ofroute.lhs_number " +
				"and asked.rhs_number >= ofroute.rhs_number) or (asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= " +
				"ofroute.lhs_number))) join regions on (regions.port_node_id = ofroute.id) where  (regions.region_type <> 3 or regions.region_type is null)" +
				" and regions.priority >= ? and regions.is_artificial != 1 ";
				
				DELETE_REGION = " delete from regions where id = ?";
				UPDATE_REGION = "update regions set shape = GeomFromText(?),short_code=?, port_node_id = ?, description=?, lowerX = ?,"
						+ " lowerY = ?, upperX = ?, upperY = ?, equal_to_MBR = ?, region_type = ? where id = ?";
				}
		}
	}
	public static class BinDefinition {
		public static final String SWM_BIN_STATUS_INFO;
		static{
			SWM_BIN_STATUS_INFO = 
	               " select swm_bins.id, swm_bins.name, landmarks.lowerX, landmarks.lowerY, ind_data.mxgin, vehicle.name "+
	               " , swm_bin_get_clean_status(pickup_freq_by_hh_1*15,pickup_freq_by_hh_2*15, pickup_freq_by_hh_3*15, pickup_freq_by_hh_4*15, totPicks, mxgin, ind_data.start_time, (case when ind_data.user_end < ind_data.end_time then ind_data.user_end else ind_data.end_time end)) "+
	               " , ind_data.period_id, ind_data.period_label "+
	               " from "+
	               " swm_bins "+ 
	               " join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
	               " left outer join swm_bin_details on (swm_bins.id = swm_bin_details.swm_bin_id) "+ 
	               " left outer join landmarks on (landmarks.id = swm_bins.landmark_id) "+
	               " left outer join  "+
	               " (select day_table.label period_label, swm_bins.id swm_bin_id, op_station.id opid, day_table.id period_id, day_table.start_time, day_table.end_time, '@user_end' user_end "+
	               " ,max(trip_info_otherLU.gate_in) mxgin "+
	               " ,count(*) totPicks "+
	               " from day_table cross join "+
	               " swm_bins  "+
	               " join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (@pv123) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
	               " left outer join swm_bin_details on (swm_bins.id = swm_bin_details.swm_bin_id) "+ 
	               " left outer join op_station on (op_station.ref_mines_id = swm_bins.id) "+
	               " left outer join trip_info_otherLU on (trip_info_otherLU.opstation_id = op_station.id and trip_info_otherLU.gate_in between day_table.start_time and day_table.end_time and trip_info_otherLU.gate_in < '@user_end') "+
	               " where "+
	               " day_table.start_time between '@start_period' and '@end_period' "+ 
	               " and swm_bins.status=1 "+
	               " group by "+
	               " day_table.label, swm_bins.id, op_station.id, day_table.id, day_table.start_time, day_table.end_time "+
	               " ) ind_data on (ind_data.swm_bin_id = swm_bins.id) "+
	               " left outer join trip_info_otherLU on (trip_info_otherLU.opstation_id = ind_data.opid and trip_info_otherLU.gate_in = ind_data.mxgin) "+
	               " left outer join vehicle on (vehicle.id = trip_info_otherLU.vehicle_id) "+
	               " order by ind_data.start_time, swm_bins.id ";


		}
	}
	
	public static class LandmarkDefinition {
		public static final String FETCH_LANDMARKS;
		public static final String FETCH_LANDMARKS1;
		public static final String FETCH_LANDMARKS2;
		public static final String UPDATE_LANDMARK;
		public static final String DELETE_LANDMARK;
		public static final String INSERT_LANDMARK;
		public static final String SPATIAL_DATA_DELETE;
		public static final String FETCH_LANDMARKS3;
		static {
			if (isMySql) {
				//from port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)
				
				/*"select ofroute.id from port_nodes asked join port_nodes ofroute on (asked.id = ? and  ((asked.lhs_number <= ofroute.lhs_number and asked.rhs_number >= ofroute.rhs_number) or (asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= ofroute.lhs_number))) " +
				" union select anc.id from port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id!=2)";
				*/
				FETCH_LANDMARKS =" select landmarks.name,landmarks.id,landmarks.lowerX,landmarks.lowerY,landmarks.upperX,landmarks.upperY, " +
				" landmarks.port_node_id from " +
				" port_nodes asked join port_nodes ofroute on (asked.id = ? and " +
				"((asked.lhs_number <= ofroute.lhs_number and asked.rhs_number >= ofroute.rhs_number) or" +
				"(asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= ofroute.rhs_number)) and ofroute.id != 2) " +
				" join landmarks on (landmarks.port_node_id = ofroute.id) where priority_show_landmark >= ? order by  landmarks.name";

				FETCH_LANDMARKS2 = "select landmarks.name,landmarks.id,landmarks.lowerX,landmarks.lowerY,landmarks.upperX,landmarks.upperY,  landmarks.port_node_id from port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)"+
                "  join landmarks on (landmarks.port_node_id = anc.id and anc.id != 2)"+
                "  where landmarks.priority_show_landmark >= ? order by  landmarks.name";
				FETCH_LANDMARKS1 = " select landmarks.name,landmarks.id,landmarks.lowerX,landmarks.lowerY,landmarks.upperX,landmarks.upperY, " +
				" landmarks.port_node_id,landmarks.landmark_type from " +
				" port_nodes asked join port_nodes ofroute on (asked.id = ? and " +
				"((asked.lhs_number <= ofroute.lhs_number and asked.rhs_number >= ofroute.rhs_number) or" +
				"(asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= ofroute.lhs_number))) " +
				" join landmarks on (landmarks.port_node_id = ofroute.id) where priority_show_landmark >= ? order by  landmarks.name";
				
				FETCH_LANDMARKS3 = " select landmarks.name,landmarks.id,landmarks.lowerX,landmarks.lowerY,landmarks.upperX,landmarks.upperY, " +
				" landmarks.port_node_id,landmarks.landmark_type from " +
				" port_nodes asked join port_nodes ofroute on (asked.id = ? and " +
				"((asked.lhs_number <= ofroute.lhs_number and asked.rhs_number >= ofroute.rhs_number) or" +
				"(asked.lhs_number >= ofroute.lhs_number and asked.rhs_number <= ofroute.lhs_number))) " +
				" join landmarks on (landmarks.port_node_id = ofroute.id) where priority_show_landmark >= ? ";

				
				UPDATE_LANDMARK = " update landmarks set shape = GeomFromText(?), name = ?, port_node_id = ?, description=?, user_description = ?, lowerX = ?, lowerY = ?, upperX = ?,"
						+ " upperY = ?,state_name =? ,district_name = ?,updated_on = ?,dest_code = ? where id = ?";
				DELETE_LANDMARK = "delete from landmarks  where id = ?";
				INSERT_LANDMARK = " insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY,updated_on,landmark_type,state_name,district_name,dest_code)"
						+ " values(GeomFromText(?),?,? ,?,?,? ,?,?,? ,?,?,?,?,?)";
				SPATIAL_DATA_DELETE = "delete  from " +
						" spatail_location_name where MBRContains(g,GeomFromText(?)) = 1 and port_node_id = ?";

			}
		}
	}

	public static class PortNode {
		public static final String SELECT_LHS_RHS;

		static {
			if (isMySql) {
				SELECT_LHS_RHS = "select lhs_number ,rhs_number from port_nodes where id = ?";
			}
		}
	}

	public static class RealTimeTracking {
		public static final String SELECT_ACCESSGROUPE;
		public static final String FETCH_VEHICLE;
		public static final String FETCH_VEHICLE_FROM_SESSION;
		public static final String INSERT_VEHICLE_IN_SESSION;
		public static final String DELETE_VEHICLE_FROM_SESSION;
		public static final String SELECT_VEHICLE_FROM_SESSION;
		public static final String DELETE_SESSION;	
		
		static {
			if (isMySql) {
				SELECT_ACCESSGROUPE = "select vehicle_id from vehicle_access_groups where port_node_id in" + "(select id from port_nodes where lhs_number > ? and rhs_number < ? )";
				
				FETCH_VEHICLE = "insert into user_vehicle_session(sessionId,vehicle_id) " +
						"select distinct ? as sessionId, vehicle.id as vehicle_id from " 
						+ " vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
						+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
						+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
						+ " left outer join port_nodes anc  on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)"
						+ "  or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number) ) where anc.id = ?";
				
				FETCH_VEHICLE_FROM_SESSION = " select vehicle_id,vehicle_name,speed,longitude,latitude from vehicle_detail where session_id = ?";
				INSERT_VEHICLE_IN_SESSION = "insert into user_vehicle_session(sessionID , vehicle_id) values(?,?)";
				DELETE_SESSION = "delete from user_vehicle_session where sessionid like ?";
				SELECT_VEHICLE_FROM_SESSION = "select sessionid from user_vehicle_session where sessionid like ?";
				DELETE_VEHICLE_FROM_SESSION = "delete from user_vehicle_session where sessionid like ? and vehicle_id = ?";
			}
		}
	}
	
	public static class RoadSegmentDefiniton {
		public static final String INSERT_ROAD_SEGMENT;
		public static final String DELETE_ROAD_SEGMENT;
		static {
			INSERT_ROAD_SEGMENT = " insert into line_segments_info(shape,short_code,description,lowerX,lowerY,upperX,upperY) "
				+ "values(GeomFromText(?),?,?,?,?,?,?)";
			DELETE_ROAD_SEGMENT = " delete from line_segments_info where id = ?";
		}
	}
	
	public static class LineSegmentDefiniton {
		public static final String INSERT_LINE_SEGMENT;
		public static final String FETCH_LINE_SEGMENTS;
		public static final String DELETE_LINESEGMENT;
		public static final String UPDATE_REGION;
		public static final String SAVE_ROAD_FOR_ORG;
		public static final String SAVE_ROAD_INFO;
		public static final String FETCH_ROAD_INFO;
		public static final String FETCH_ROAD_LIST;
		public static final String DELETE_ROAD_SEGMENT;
		public static final String DELETE_ROAD;

		static {
			if (isMySql) {
				INSERT_LINE_SEGMENT = " insert into line_segments_info(shape,short_code,description,lowerX,lowerY,upperX,upperY,state_id,district_name,align_with,associated_with) "
						+ "values(GeomFromText(?),?,?,?,?,?,?,?,?,?,?)";
				FETCH_LINE_SEGMENTS = " select astext(line_segments_info.shape) shape,line_segments_info.description,line_segments_info.short_code,  line_segments_info.id,line_segments_info.lowerX,line_segments_info.lowerY,line_segments_info.upperX,line_segments_info.upperY  from  line_segments_info  where line_segments_info.priority >= ? order by line_segments_info.short_code ";
				DELETE_LINESEGMENT = " delete from line_segments_info where id = ?";
				UPDATE_REGION = "update regions set shape = GeomFromText(?),short_code=?, port_node_id = ?, description=?, lowerX = ?,"
						+ " lowerY = ?, upperX = ?, upperY = ?, equal_to_MBR = ? where id = ?";
				SAVE_ROAD_FOR_ORG = "insert into roads(road_name,port_node_id) values(?,?)";
				SAVE_ROAD_INFO = "insert into road_line_segments (road_id,line_segment_id,line_segment_order) values (?,?,?);";
				FETCH_ROAD_INFO = "select roads.id,roads.road_name,line_segment_id,line_segment_order,short_code,astext(line_segments_info.shape) shape,lowerX,lowerY,upperX,upperY from roads left join road_line_segments on (roads.id=road_id) left join line_segments_info on (line_segment_id=line_segments_info.id and status=1) where port_node_id = ? ";
				FETCH_ROAD_LIST = "select roads.id,roads.road_name from roads";
				DELETE_ROAD_SEGMENT = "delete from road_line_segments where road_line_segments.road_id = ?";
				DELETE_ROAD = "delete from roads where roads.id= ?";
			}
		}
	}
	/*public static class UserVehicleSession 	{
		public static final String INSERT_VEHICLE_IN_SESSION;
		public static final String DELETE_VEHICLE_FROM_SESSION;
		public static final String SELECT_VEHICLE_FROM_SESSION;
		public static final String DELETE_SESSION;	
		static {
			if (isMySql) {
				INSERT_VEHICLE_IN_SESSION = "insert into user_vehicle_session(sessionID , vehicle_id) values(?,?)";
				DELETE_SESSION = "delete from user_vehicle_session where sessionid like ?";
				SELECT_VEHICLE_FROM_SESSION = "select sessionid from user_vehicle_session where sessionid like ?";
				DELETE_VEHICLE_FROM_SESSION = "delete from user_vehicle_session where sessionid like ? and vehicle_id = ?";

			}
		}
	}
*/
}

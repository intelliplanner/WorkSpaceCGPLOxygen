package com.ipssi.reporting.common.db;

public class DBQueries {

	/**
	 * This variable will be set appropriate value later
	 */
	public static final boolean isMySql = true;
	// Customize Module
	public static class CUSTOMIZE {
		public final static String INSERT_MENU;
		public final static String INSERT_UI_COLUMN;
		public final static String INSERT_UI_PARAM;
		public final static String UPDATE_MENU;
		public final static String DELETE_UI_COLUMN_BY_MENU;
		public final static String DELETE_UI_PARAM_BY_MENU;
		public final static String FETCH_MENU;
		public final static String FETCH_UI_COLUMN_BY_MENU;
		public final static String FETCH_UI_PARAM_BY_MENU;
		public final static String FETCH_MENUMASTER_USER;
		public final static String FETCH_MENUMASTER_PORT;
		public final static String FETCH_MENUMASTER_FIND;
		public final static String DELETE_MENU;
		public final static String GET_MENUID_FOR_DYNAMIC_MENU;
		
		static {
			if (isMySql) {
				DELETE_MENU = "delete from menu_master where menu_master.id = ?";
				
				INSERT_MENU = " insert into menu_master " 
					+ " (port_node_id, user_id, menu_tag, component_file, updated_on, row_table, column_table) "
					+ " values(?, ?, ?, ?, ?,?,?) ";
				INSERT_UI_COLUMN = " insert into ui_column " 
					+ " (menu_id, column_name, attribute_name, attribute_value, updated_on, rollup) "
					+ " values(?, ?, ?, ?, ?, ?) ";
				INSERT_UI_PARAM = " insert into ui_parameter " 
					+ " (menu_id, param_name, param_value,operator,right_operand, updated_on) "
					+ " values(?, ?, ?, ?, ?, ?) ";
				UPDATE_MENU = " update menu_master set " 
					+ " port_node_id = ?, user_id = ?, menu_tag = ?, component_file = ?, updated_on = ?, row_table=?, column_table=?"
					+ " where id = ? ";
				DELETE_UI_COLUMN_BY_MENU = " delete from ui_column " 
					+ "where menu_id = ? ";
				DELETE_UI_PARAM_BY_MENU = " delete from ui_parameter " 
					+ "where menu_id = ? ";
				FETCH_MENU = " select * from menu_master " +
				"where id = ? ";
				FETCH_UI_COLUMN_BY_MENU = " select * from ui_column " +
					"where menu_id = ? ";
				FETCH_UI_PARAM_BY_MENU = " select menu_id, param_name, param_value, updated_on, operator, right_operand from ui_parameter " +
					"where menu_id = ? ";
				FETCH_MENUMASTER_USER = " select * from menu_master " +
				"where user_id = ? and  port_node_id is null and menu_tag = ? and  component_file = ? and  row_table = ? and  column_table = ? ";
				FETCH_MENUMASTER_FIND = " select * from menu_master " +
				"where (user_id = ? or (? is null and user_id is null)) and (port_node_id = ? or (? is null and port_node_id is null)) and  menu_tag = ? and  component_file = ? and  row_table = ? and  column_table = ? ";
				FETCH_MENUMASTER_PORT = " select * from menu_master " +
				"where port_node_id = ? and  menu_tag = ? and  component_file = ? and  row_table = ? and  column_table = ? ";
			    GET_MENUID_FOR_DYNAMIC_MENU = "select menu_master.id from menu_master join (select leaf.id from port_nodes leaf join port_nodes anc on (anc.id = ? and leaf.lhs_number <= anc.lhs_number and leaf.rhs_number >= anc.rhs_number) where leaf.id !=2 ) port_node on (port_node_id = port_node.id) where menu_tag = ? and component_file = ?";
			}
		}
	}
public static class SHIFT {
		
		public final static String GET_REG_SHIFT;
		public final static String GET_DEFINED_SHIFT;
		public static final String FETCH_SHIFT_SCHEDULE ;
		
		static {
			if (isMySql) {
				FETCH_SHIFT_SCHEDULE = "select shift_type,name,port_node_id,start_hour,start_min,stop_hour,stop_min, id, valid_start, valid_end from shift join shift_timings on (shift.id = shift_timings.shift_id) order by port_node_id,start_hour asc ";
				GET_REG_SHIFT = " select shift.id, start_hour, start_min from shift join "+ 
				" (select anc.id pnid from port_nodes leaf join port_nodes anc on  (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
				" where exists(select 1 from shift where shift.port_node_id = anc.id) order by anc.lhs_number desc limit 1) portWithShift  "+
				" on (portWithShift.pnid = shift.port_node_id)  join shift_timings on (shift_timings.shift_id = shift.id) "+
				" where "+
				" (valid_start is null or valid_end is null or (? >= valid_start and ? < valid_end) or (? >= valid_start and ? < valid_end)) and "+
				" ( "+
				" (start_hour < stop_hour and (? between start_hour+1 and stop_hour-1) or (? = start_hour and ? >= start_min) or (?=stop_hour and ? < stop_min)) or "+
				" (start_hour > stop_hour and (?+24 between start_hour+1 and stop_hour+24-1) or (?+24 = start_hour and ? >= start_min) or (?=stop_hour+24 and ? < stop_min) ) "+
				" ) ";
				GET_DEFINED_SHIFT = " select shift_schedule_info.id , shift_schedule_info.shift_id, shift_schedule_info.start_hour, shift_schedule_info.start_min, shift_schedule_info.day "+
				" from  "+
				" port_nodes anc join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
				" join shift_schedule_info on (shift_schedule_info.port_node_id = anc.id) "+
				" where ( "+
				" (start_hour < stop_hour and ? = day and ((? between start_hour+1 and stop_hour-1) or (? = start_hour and ? >= start_min) or (?=stop_hour and ? < stop_min)) ) or "+
				" (start_hour > stop_hour and ? = adddate(day ,1) and ((?+24 between start_hour+1 and stop_hour+24-1) or (?+24 = start_hour and ? >= start_min) or (?=stop_hour+24 and ? < stop_min))) "+
				" ) ";
			}
		}
	}
}
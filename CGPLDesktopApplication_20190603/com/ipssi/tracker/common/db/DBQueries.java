package com.ipssi.tracker.common.db;

import java.util.Date;

public class DBQueries {

	/**
	 * This variable will be set appropriate value later
	 */
	private static final boolean isMySql = true;

	public static class CUSTOMERS {
		public final static String CUSTOMER_MAX_ID;
		public final static String DELETE_CONTACT;
		public final static String DELETE_CONTACT_SELECTIVE;
		public final static String INSERT_CONTACT;
		public final static String UPDATE_CONTACT;
		public final static String INSERT_CUSTOMER;
		public final static String UPDATE_CUSTOMER;
		public final static String FETCH_CUSTOMER;
		public final static String FETCH_CONTACT;
		public final static String DELETE_CUSTOMER;
		public final static String FETCH_CONTACT_ID;

		static {
			if (isMySql) {
				CUSTOMER_MAX_ID = " select (ifnull(max(id), 0) + 1)id from customers ";
				DELETE_CONTACT = " update customer_contacts set status = ? where customer_id = ? ";
				DELETE_CONTACT_SELECTIVE = " update customer_contacts set status = ? where id = ? ";
				INSERT_CONTACT = " insert into customer_contacts (customer_id , contact_name , phone , mobile , email , address , updated_on, status) "
						+ " values(?, ?, ?, ?, ?, ?, ?," + com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE + ") ";
				UPDATE_CONTACT = " update customer_contacts set customer_id = ?, contact_name = ?, phone = ?, mobile = ?, email = ?, address = ?, updated_on = ? "
						+ " where id = ? ";
				INSERT_CUSTOMER = " insert into customers (name, short_code, status, notes, type, partner, device_count, location, created_on, created_by, updated_on) "
						+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				UPDATE_CUSTOMER = " update customers set "
						+ " name = ?, short_code = ?, status = ?, notes = ?, type = ?, partner = ?, device_count = ?, location = ?, updated_on = ? where id = ? ";
				FETCH_CUSTOMER = " select id, name, short_code, status, notes, type, partner, created_on, created_by, device_count, location, email   "
						+ " from customers left join (select email, customer_id from customer_contacts c1 where c1.id = "
						+ " (select max(id) from customer_contacts c2 where c1.customer_id = c2.customer_id and status != ?))cont"
						+ " on (id = customer_id) where status != ? order by id ";
				FETCH_CONTACT = " select customers.id, name, short_code, customers.status, notes, type, partner, device_count, location, created_on, created_by, customers.updated_on, "
						+ " customer_contacts.id id1, customer_id , contact_name , phone , mobile , email , address "
						+ " from customers left join "
						+ " customer_contacts on (customers.id = customer_id and customer_contacts.status  != ? )" + " where customers.id = ? order by customers.id ";
				DELETE_CUSTOMER = " update customers set status = ? where id = ? ";
				FETCH_CONTACT_ID = " select id from customer_contacts where customer_id = ? ";
			}
		}
	}

	// Application level Queries
	public static class APPLICATION {
		public final static String FETCH_SUBJECTS;
		public final static String FETCH_RULE_TYPES;
		public final static String FETCH_DIMENSIONS;
		public final static String FETCH_REGIONS_UPPER;
		public final static String FETCH_LOGICAL_OPERATORS;
		public final static String FETCH_ACTION_MASTER;

		static {
			if (isMySql) {
				FETCH_SUBJECTS = " select id, description from subjects ";
				FETCH_RULE_TYPES = " select id, short_code, description from rule_type ";
				FETCH_DIMENSIONS = " select id, description from dimensions ";
				FETCH_REGIONS_UPPER = " select regions.id, regions.short_code from port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join regions on (regions.port_node_id = anc.id) where regions.status = 1 and (regions.is_artificial <> 1 or regions.is_artificial is null) ";
				FETCH_LOGICAL_OPERATORS = " select id, description from logical_operators ";
				FETCH_ACTION_MASTER = " select id, action from actions_master ";

			}
		}
	}
	public static class ACQUIRE{
		
		public final static String INSERT_ITEM_MASTER;
		public final static String FETCH_MODEL;
		public final static String INSERT_ITEMS;
		public final static String INSERT_STOCK_TRANSACATION_RECORD;
		public final static String INSERT_STOCK_OWNER;
		public final static String FETCH_ACCESSORIES;
		public final static String FETCH_ACQUIREVIEWLIST;
		public final static String FETCH_ITEMDETAIL;
		public final static String UPDATE_ACQUIRE_ITEMS;
		public final static String FETCH_VENDORS;
		public final static String UPDATE_ITEM_STATUS;
		public final static String FETCH_ACCESSORY_TYPE_ID;
		public final static String VERIFY_LUID;
		//public final static String FETCH_ITEM_TYPE;
		public final static String FETCH_OFFICES;
		public final static String FETCH_EMPLOYEES;
		public final static String FETCH_CUSTOMERS;
		public final static String FETCH_VENDORSLIST;
		
		static{
			if(isMySql)
			{
				INSERT_ITEM_MASTER = " insert into item_type_master (item_type_master.item_type_id, item_type_master.name) values ( ?, ?)";
				FETCH_MODEL = "select item_type_master.name ,item_type_master.id from item_type_master where item_type_master.item_type_id = ? AND item_type_master.vendor_id = ?";
				INSERT_ITEMS = " insert into items (items.GUID , items.LUID , items.date_of_purchase , items.date_of_warentee , items.quantity , items.item_type_master_id , items.description) values( ?, ?, ?, ?, ?, ?, ?) ";
				INSERT_STOCK_TRANSACATION_RECORD = "  insert into stock_transaction_record (item_id , owner_type_id , owner_id , transaction_type_id , quantity ,description,item_status, created_on, updated_on) values( ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				INSERT_STOCK_OWNER="insert into stock_owners (stock_owners.owner_type_id , stock_owners.owner_id ,stock_owners.item_id ,stock_owners.item_status ,stock_owners.despatch_detail_id , stock_owners.latest_stock_transaction_record_id, created_on, updated_on) values( ?, ?, ?, ?, ?, ?, ?, ?) ";
				FETCH_ACCESSORIES = " select id from item_type_master where item_type_master.item_type_id = ? AND item_type_master.vendor_id = ? AND item_type_master.model is null ";
				FETCH_ACQUIREVIEWLIST = " SELECT item_type_master.item_type_id,stock_owners.item_id,stock_owners.item_status,items.GUID,items.LUID,items.id,items.item_type_master_id,items.description FROM stock_owners INNER JOIN items ON stock_owners.item_id=items.id INNER JOIN item_type_master ON item_type_master.id=items.item_type_master_id where ";
				FETCH_ITEMDETAIL = " select * from items inner join stock_owners on items.id=stock_owners.item_id join stock_transaction_record on items.id=stock_transaction_record.item_id join item_type_master on items.item_type_master_id=item_type_master.id where  (stock_owners.latest_stock_transaction_record_id = stock_transaction_record.id) AND items.id = ? ";
				UPDATE_ACQUIRE_ITEMS="update items inner join stock_owners on items.id=stock_owners.item_id join stock_transaction_record on items.id=stock_transaction_record.item_id set stock_transaction_record.owner_id=?,stock_owners.owner_id=?,stock_transaction_record.description=?,items.GUID=?,items.LUID=?,items.date_of_purchase=?,items.date_of_warentee=?, items.item_type_master_id=? where items.id=? ";
				FETCH_VENDORS=" SELECT DISTINCT vendor.id,vendor.vendor_name FROM item_type_master INNER JOIN vendor ON item_type_master.vendor_id=vendor.id WHERE item_type_master.item_type_id = ? ";
				UPDATE_ITEM_STATUS=" UPDATE stock_owners SET stock_owners.item_status=? WHERE stock_owners.item_id = ? ";
				FETCH_ACCESSORY_TYPE_ID=" Select DISTINCT item_type_master.item_type_id from item_type_master WHERE item_type_master.name = ? ";
				VERIFY_LUID=" select * from items WHERE LUID = ?";
				//FETCH_ITEM_TYPE="SELECT * from item_type";
				FETCH_OFFICES="SELECT * from office";
				FETCH_EMPLOYEES="SELECT * from employees";
				FETCH_CUSTOMERS="SELECT * from customers";
				FETCH_VENDORSLIST="SELECT * from vendor";
			}
		}
		
	}
	public static class REMOVE_DEVICE {
		public final static String FETCH_VEHICLE_ITEMS_REL_RECORD_FOR_CHANGE_DEVICE;
		public final static String INSERT_INVENTORY_STOCK_OWNER;
		public final static String INSERT_INVENTORY_EMPLOYEES;
		public final static String UPDATE_STOCK_OWNER_FOR_REMOVE_DEVICE;
     	public final static String INSERT_INVENTORY_DISPATCH_DETAIL;
    	public final static String UPDATE_VEHICLE_FOR_REMOVE_DEVICE;
		public final static String UPDATE_INVENTORY_STOCK_OWNER_FOR_DESPATCH_DETAIL;
		//public final static String UPDATE_STOCK_TRANSACTION_RECORD;
		public final static String UPDATE_VEHICLE_SIM_NUMBER;
		public final static String UPDATE_STOCK_OWNER_ITEM_STATUS;
		//public final static String UPDATE_STOCK_TRANSACTION_ITEM_STATUS;
		//public final static String UPDATE_VEHICLE_FOR_ADD_CHANGE_DEVICE;
		public final static String UPDATE_STOCK_OWNER_FOR_ADD_CHANGE_DEVICE;
		public final static String PREPARE_DATALIST_FOR_ADD_CHANGE_DEVICE;
		public final static String UPDATE_VEHICLE_REL_TABLE_FOR_ITEM_STATUS;
		public final static String SELECT_VEHICLE_LIST;
		public final static String INSERT_STOCK_TRANSACTION_RECORD_FOR_DESPATCH_DETAIL;
		public final static String UPDATE_DESPATCH_DETAIL_FOR_RECEIVE_DESPATCH;
		public final static String UPDATE_STOCK_OWNER_FOR_RECEIVE_DESPATCH;
		public final static String UPDATE_STOCK_TRANSACTION_RECORD_FOR_RECEIVE_DESPATCH;
		public final static String FETCH_RECORD_FOR_VEHICLE_ITEMS_STOCK_OWNER;
		public final static String UPDATE_VEHICLE_ITEMS_REL_FOR_CHANGE_DEVICE;
		public final static String INSERT_VEHICLE_ITEM_REL_FOR_CHANGE_DEVICE;
		public final static String INSERT_STOCK_TRANSACTION_RECORD_FOR_ADD_CHANGE;
		public final static String DELETE_VEHICLE_FROM_VEHICLE_ITEMS_REL_FOR_REMOVE_DEVICE;
	static {
			if (isMySql) {
				DELETE_VEHICLE_FROM_VEHICLE_ITEMS_REL_FOR_REMOVE_DEVICE = " DELETE FROM vehicle_items_rel  WHERE vehicle_items_rel.vehicle_id = ? ";
				INSERT_STOCK_TRANSACTION_RECORD_FOR_ADD_CHANGE = "INSERT into stock_transaction_record( item_id, owner_type_id, owner_id, quantity, transaction_type_id, description, item_status) values ( ?, ?, ?, ?, ?, ?, ?)";
				INSERT_INVENTORY_STOCK_OWNER = "INSERT into stock_owners(item_id,owner_id,owner_type_id,item_status,latest_stock_transaction_record_id) values ( ?, ?, ?, ?, ?);";
				UPDATE_STOCK_OWNER_FOR_REMOVE_DEVICE = " UPDATE stock_owners SET stock_owners.owner_id = ?, stock_owners.owner_type_id = ?, stock_owners.item_status = ? , updated_on = ? WHERE stock_owners.item_id = ? ";
				INSERT_INVENTORY_DISPATCH_DETAIL = "INSERT into despatch_details (despatch_type, unit, source_owner_id, source_owner_type_id, device_status, despatch_date, mode_of_dispatch, name_of_person, courier_company, train_name, flight_detail, despatch_receipt_no, despatch_status, item_id, destination_owner_id, destination_owner_type_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				UPDATE_VEHICLE_FOR_REMOVE_DEVICE = "UPDATE vehicle SET vehicle.working_status = ? , vehicle.working_detailed_status = ?,  vehicle.device_internal_id = ?, vehicle.sim_number = ? WHERE vehicle.id = ?";
			    INSERT_INVENTORY_EMPLOYEES = "Insert into employees (name) values (?)";
			    UPDATE_INVENTORY_STOCK_OWNER_FOR_DESPATCH_DETAIL = " UPDATE stock_owners SET stock_owners.latest_stock_transaction_record_id = ?, stock_owners.despatch_details_id_ref = ?, stock_owners.updated_on = ? WHERE stock_owners.item_id = ? ";
			   // UPDATE_STOCK_TRANSACTION_RECORD = "UPDATE stock_transaction_record SET stock_transaction_record.owner_id = ? , stock_transaction_record.owner_type_id = ? WHERE stock_transaction_record.item_id = ?";
			    UPDATE_VEHICLE_SIM_NUMBER = "UPDATE  vehicle SET vehicle.sim_number = ? WHERE vehicle.id = ?" ;
			    UPDATE_STOCK_OWNER_ITEM_STATUS = "UPDATE stock_owners SET stock_owners.item_status = ? WHERE stock_owners.item_id IN (SELECT items.id FROM items WHERE items.LUID = ? )";
			   // UPDATE_STOCK_TRANSACTION_ITEM_STATUS = "UPDATE stock_transaction_record SET stock_transaction_record.item_status = ? WHERE stock_transaction_record.item_id IN (SELECT items.id FROM items WHERE items.LUID = ? )";
			    UPDATE_VEHICLE_REL_TABLE_FOR_ITEM_STATUS = " UPDATE vehicle_items_rel SET item_id = ? WHERE  item_type_id = 1 AND vehicle_id = ?  ";
			    //UPDATE_VEHICLE_FOR_ADD_CHANGE_DEVICE = " UPDATE vehicle SET  vehicle.working_status = ?, vehicle.working_detailed_status = ?,vehicle.sim_number = ?, vehicle.device_internal_id = ? WHERE vehicle.id = ?";
			    UPDATE_STOCK_OWNER_FOR_ADD_CHANGE_DEVICE = " UPDATE stock_owners SET stock_owners.owner_id = ?,stock_owners.owner_type_id = ?, stock_owners.item_status = ?, updated_on = ?  where  stock_owners.item_id = ? ";
			    PREPARE_DATALIST_FOR_ADD_CHANGE_DEVICE = "SELECT items.id FROM items INNER JOIN item_type_master ON (item_type_master.id =items.item_type_master_id AND item_type_master.item_type_id = ?) left outer join stock_owners on (stock_owners.item_id=items.item_type_master_id ) where stock_owners.item_id is null limit 1";
			    SELECT_VEHICLE_LIST =" SELECT vehicle.id,vehicle.name,vehicle.device_internal_id,vehicle.sim_number FROM vehicle  WHERE vehicle.id = ?  ";
			    INSERT_STOCK_TRANSACTION_RECORD_FOR_DESPATCH_DETAIL = "INSERT INTO stock_transaction_record( item_id, owner_id, owner_type_id, quantity, transaction_type_id, item_status) VALUES ( ?, ?, ?, ?, ?, ?)"; 
			    UPDATE_DESPATCH_DETAIL_FOR_RECEIVE_DESPATCH = " UPDATE despatch_details SET despatch_details.despatch_status = ? WHERE despatch_details.id = ? ";
			    UPDATE_STOCK_OWNER_FOR_RECEIVE_DESPATCH = " UPDATE stock_owners SET stock_owners.owner_id = ?, stock_owners.owner_type_id = ? , updated_on = ? WHERE stock_owners.despatch_details_id_ref = ? ";
			    UPDATE_STOCK_TRANSACTION_RECORD_FOR_RECEIVE_DESPATCH = " UPDATE stock_transaction_record SET stock_transaction_record.owner_type_id = ?, stock_transaction_record.owner_id = ? where stock_transaction_record.id = (Select stock_owners.latest_stock_transaction_record_id from stock_owners where stock_owners.despatch_details_id_ref = ? ) ";
			    FETCH_RECORD_FOR_VEHICLE_ITEMS_STOCK_OWNER = " SELECT vehicle_items_rel.item_id, vehicle_items_rel.item_type_id ,stock_owners.owner_id, stock_owners.owner_type_id FROM vehicle_items_rel inner join stock_owners on (stock_owners.item_id = vehicle_items_rel.item_id) WHERE vehicle_items_rel.vehicle_id = ? AND  vehicle_items_rel.item_type_id IN (?)";
			    UPDATE_VEHICLE_ITEMS_REL_FOR_CHANGE_DEVICE = " UPDATE vehicle_items_rel SET vehicle_items_rel.item_id = ? WHERE vehicle_items_rel.item_type_id =  ? AND vehicle_items_rel.vehicle_id = ? ";
			    INSERT_VEHICLE_ITEM_REL_FOR_CHANGE_DEVICE = " INSERT INTO vehicle_items_rel (vehicle_items_rel.vehicle_id, vehicle_items_rel.item_id,vehicle_items_rel.item_type_id) VALUES (?, ?, ?) " ;
			    FETCH_VEHICLE_ITEMS_REL_RECORD_FOR_CHANGE_DEVICE = " SELECT vehicle_items_rel.id FROM vehicle_items_rel WHERE vehicle_items_rel.vehicle_id = ? AND vehicle_items_rel.item_type_id = ? " ;
			} 
		}
	}
	public static class RULES {
		public final static String INSERT_RULE;
		public final static String INSERT_CONDITION_CLAUSE;
		public final static String INSERT_RULES_REGION_THRESHOLD;
		public final static String INSERT_ACTIONS;
		public final static String UPDATE_RULE;
		public final static String DELETE_CONDITION_CLAUSE_BY_RULE;
		public final static String DELETE_RULES_REGION_THRESHOLD_BY_RULE;
		public final static String DELETE_ACTIONS_BY_RULE;
		public final static String DELETE_CONDITION_CLAUSE;
		public final static String DELETE_RULES_REGION_THRESHOLD;
		public final static String DELETE_ACTIONS;
		public final static String FETCH_RULE;
		public final static String FETCH_CONDITION_CLAUSE;
		public final static String FETCH_RULES_REGION_THRESHOLD;
		public final static String FETCH_ACTIONS;
		public final static String FETCH_ALL_RULE;
		public final static String FETCH_ALL_RULE_UPPER;
		public final static String FETCH_CONDITION_CLAUSE_BY_RULE;
		public final static String FETCH_RULES_REGION_THRESHOLD_BY_RULE;
		public final static String FETCH_ACTIONS_BY_RULE;

		static {
			if (isMySql) {
				INSERT_RULE = " insert into rules " + 
				" (port_node_id, name, status, notes, updated_on,alert_text,delayed_alert,on_termination,on_termination_alert_text," +
				"reference_rule_id, merge_min, replaced_rule_id,improper_param,heuristic_param,function_id,dim_id, is_dist_threshold, int_val1, int_val2, int_val3, int_val4, int_val5, check_criticality) " + 
				" values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?) ";
				
				INSERT_CONDITION_CLAUSE = " insert into conditions_clauses " + " (rule_id, rule_type_id, param_0_type, param_0, logical_operator, value_1, value_2) "
						+ " values(?, ?, ?, ?, ?, ?, ?) ";
				INSERT_RULES_REGION_THRESHOLD = " insert into rules_region_threshold " + " (rule_id, region_id, value_1, value_2, value_3, value_4," +
						" value_5, value_6, updated_on) "
						+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				INSERT_ACTIONS = " insert into actions " + " (rule_id, actions_master_id, param1, param2, param3, param4) " + " values(?, ?, ?, ?, ?, ?) ";
				UPDATE_RULE = " update rules set " + " port_node_id = ?, name = ?, status = ?, notes = ?, updated_on = ?, alert_text = ? , " +
						"delayed_alert = ?, on_termination = ?, on_termination_alert_text = ? ,reference_rule_id = ?, merge_min = ?, " +
						"replaced_rule_id=?,improper_param = ?,heuristic_param = ?,function_id = ?,dim_id = ?, is_dist_threshold=?, int_val1=?, int_val2=?, int_val3=?, int_val4=?, int_val5=?, check_criticality=? " + " where id = ? ";
				DELETE_CONDITION_CLAUSE_BY_RULE = " delete from conditions_clauses " + "where rule_id = ? ";
				DELETE_RULES_REGION_THRESHOLD_BY_RULE = " delete from rules_region_threshold " + "where rule_id = ? ";
				DELETE_ACTIONS_BY_RULE = " delete from actions " + "where rule_id = ? ";
				DELETE_CONDITION_CLAUSE = " delete from conditions_clauses " + "where rule_id = ? and rule_type_id = ? ";
				DELETE_RULES_REGION_THRESHOLD = " delete from rules_region_threshold " + "where rule_id = ? and region_id = ? and conditions_clause_id = ? ";
				DELETE_ACTIONS = " delete from actions " + "where rule_id = ? and actions_master_id = ? ";
				FETCH_RULE = " select id, port_node_id, name, status, salience, alert_text ,notes, updated_on,delayed_alert,on_termination," +
						"on_termination_alert_text,reference_rule_id,merge_min, replaced_rule_id,improper_param,heuristic_param,function_id,dim_id,is_dist_threshold, int_val1, int_val2, int_val3, int_val4, int_val5, check_criticality " +
						" from rules " + "where id = ? ";
				FETCH_CONDITION_CLAUSE = " select id, rule_id, rule_type_id, param_0_type, param_0, logical_operator, value_1, value_2 from " +
						"conditions_clauses "
						+ "where rule_id = ? and rule_type_id = ? ";
				FETCH_RULES_REGION_THRESHOLD = " select rule_id, region_id, value_1, value_2, value_3, value_4, value_5, value_6, updated_on from rules_region_threshold "
						+ "where rule_id = ? and region_id = ? ";
				FETCH_ACTIONS = " select * from actions " + "where rule_id = ? and actions_master_id = ? ";
				// FETCH_ALL_RULE = " select * from rules ";
				FETCH_ALL_RULE = "select rules.id, rules.port_node_id, rules.name, rules.status, rules.salience, rules.notes, rules.updated_on, rules.merge_min, leaf.name port_name"
						+ " from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join rules " +
								" on rules.port_node_id = leaf.id where rules.status in (1,2) "
						+ " order by rules.status desc, rules.id desc";
				FETCH_ALL_RULE_UPPER = "select rules.id, rules.port_node_id, rules.name, rules.status, rules.salience, rules.notes, rules.updated_on, rules.merge_min, leaf.name port_name"
					+ " from port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join rules " +
							" on rules.port_node_id = anc.id where rules.status in (1,2) "
					+ " order by rules.status desc, rules.id desc";
				FETCH_CONDITION_CLAUSE_BY_RULE = " select id, rule_id, rule_type_id, param_0_type, param_0, logical_operator, value_1, value_2 from conditions_clauses "
						+ " where rule_id = ? ";
				FETCH_RULES_REGION_THRESHOLD_BY_RULE = " select rule_id, region_id, value_1, value_2, value_3,  value_4, value_5, value_6, updated_on from rules_region_threshold " + 
				"where rule_id = ? ";
				FETCH_ACTIONS_BY_RULE = " select rule_id, actions_master_id, param1, param2, param3, param4 from actions " + "where rule_id = ? ";
			}
		}
	}

	// Query for RuleSet Modules
	public static class RULESET {
		public final static String INSERT_RULESET;
		public final static String UPDATE_RULESET;
		public final static String FETCH_RULESET;
		public final static String FETCH_ALL_RULESET;
		public final static String INSERT_RULESET_RULES;
		public final static String FETCH_RULESET_RULES;
		public final static String DELETE_RULESET_RULES;

		static {
			if (isMySql) {
				INSERT_RULESET = " insert into rulesets " + " (name, status, status_from, status_to, port_node_id, notes, updated_on) " + " values(?, ?, ?, ?, ?, ?, ?) ";
				UPDATE_RULESET = " update rulesets set " + " name = ?, status = ?, status_from = ?, status_to = ?, port_node_id = ?, notes = ?, updated_on = ?" + " where id = ? ";
				FETCH_RULESET = " select * from rulesets " + "where id = ? ";
				// FETCH_ALL_RULESET = " select * from rulesets ";
				FETCH_ALL_RULESET = "select rules.id, rules.port_node_id, rules.name, rules.status_from, rules.status_to, rules.updated_on, rules.status, rules.notes, leaf.name port_name from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join rulesets rules  on rules.port_node_id = leaf.id   order by rules.status desc, rules.id desc";
				INSERT_RULESET_RULES = " insert into ruleset_rules " + " (ruleset_id, rule_id, active_from, active_to) " + " values(?, ?, ?, ?) ";
				FETCH_RULESET_RULES = " select * from ruleset_rules " + "where ruleset_id = ? ";
				DELETE_RULESET_RULES = " delete from ruleset_rules " + "where ruleset_id = ? ";
			}
		}
	}

	// Query for Alerts Modules
	public static class ALERTS {
		public final static String INSERT_NOTIFICATIONSET;
		public final static String INSERT_REGION_NOTIFICATIONSET;
		public final static String UPDATE_NOTIFICATIONSET;
		public final static String UPDATE_REGION_NOTIFICATIONSET;
		public final static String FETCH_NOTIFICATIONSET;
		public final static String FETCH_REGION_NOTIFICATIONSET;
		public final static String FETCH_ALL_NOTIFICATIONSET;
		public final static String FETCH_ALL_REGION_NOTIFICATIONSET;
		public final static String INSERT_NOTIFICATIONSET_RULES;
		public final static String INSERT_NOTIFICATIONSET_REGION;
		public final static String FETCH_NOTIFICATIONSET_RULES;
		public final static String FETCH_NOTIFICATIONSET_REGIONS;
		public final static String DELETE_NOTIFICATIONSET_RULES;
		public final static String DELETE_NOTIFICATIONSET_REGION;
		public final static String FETCH_CUSTOMER_CONTACTS;
		public final static String FETCH_CUSTOMER;
		public final static String INSERT_CONTACT;
		public final static String FETCH_RULES_ORG;
		public final static String FETCH_OP_STATIONS;
		public final static String UPDATE_CONTACT;
		static {
			if (isMySql) {
				UPDATE_CONTACT = " update customer_contacts set mobile = ?, email = ?, updated_on = ? "+" where id = ? ";
				INSERT_NOTIFICATIONSET = " insert into notification_sets " + " (name, status, status_from, status_to, port_node_id, notes, updated_on, load_status, notification_create_type, opstation_subtype, relative_dur_operator, relative_dur_operand1, relative_dur_operand2, loading_at, unloading_at, event_dur_operator, event_dur_operand1, event_dur_operand2, event_dist_operator, event_dist_operand1, event_dist_operand2) "
						+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				INSERT_REGION_NOTIFICATIONSET = " insert into region_notification_sets " + " (name, status, status_from, status_to, port_node_id, notes, updated_on, alert_type) "
						+ " values(?, ?, ?, ?, ?, ?, ?, ?) ";
				UPDATE_NOTIFICATIONSET = " update notification_sets set " + " name = ?, status = ?, status_from = ?, status_to = ?, port_node_id = ?, notes = ?, updated_on = ?, load_status=?, notification_create_type=?, opstation_subtype=?, relative_dur_operator=?, relative_dur_operand1=?, relative_dur_operand2=?, loading_at=?, unloading_at=?, event_dur_operator=?, event_dur_operand1=?, event_dur_operand2=?, event_dist_operator=?, event_dist_operand1=?, event_dist_operand2=?"
						+ " where id = ? ";
				UPDATE_REGION_NOTIFICATIONSET = " update region_notification_sets set "
						+ " name = ?, status = ?, status_from = ?, status_to = ?, port_node_id = ?, notes = ?, updated_on = ?" + " where id = ? ";
				FETCH_NOTIFICATIONSET = " select notification_sets.id, notification_sets.name, notification_sets.status, notification_sets.status_from, notification_sets.status_to, notification_sets.port_node_id, notification_sets.notes, notification_sets.updated_on, notification_sets.load_status, notification_sets.notification_create_type, notification_sets.opstation_subtype, notification_sets.relative_dur_operator, notification_sets.relative_dur_operand1, " +
						" notification_sets.relative_dur_operand2, notification_sets.loading_at, notification_sets.unloading_at, notification_sets.event_dur_operator, notification_sets.event_dur_operand1, notification_sets.event_dur_operand2, notification_sets.event_dist_operator, notification_sets.event_dist_operand1, notification_sets.event_dist_operand2  "
						+ "  from notification_sets where id = ? ";
				FETCH_REGION_NOTIFICATIONSET = " select notification_sets.id, notification_sets.name, notification_sets.status, notification_sets.status_from, notification_sets.status_to, notification_sets.port_node_id, notification_sets.notes, notification_sets.updated_on from region_notification_sets notification_sets "
						+ "where notification_sets.id = ? and alert_type = ? ";
				// FETCH_ALL_NOTIFICATIONSET =
				// " select notification_sets.id, notification_sets.name, notification_sets.status, notification_sets.status_from, notification_sets.status_to, notification_sets.port_node_id, notification_sets.notes, notification_sets.updated_on  from notification_sets ";
				FETCH_ALL_NOTIFICATIONSET = "select notification_sets.id, notification_sets.name, notification_sets.status, notification_sets.status_from, notification_sets.status_to, notification_sets.port_node_id, notification_sets.notes, notification_sets.updated_on from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join notification_sets  on notification_sets.port_node_id = leaf.id where notification_sets.status=?";
				FETCH_ALL_REGION_NOTIFICATIONSET = "select notification_sets.id, notification_sets.name, notification_sets.status, notification_sets.status_from, notification_sets.status_to, notification_sets.port_node_id, notification_sets.notes, notification_sets.updated_on from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join region_notification_sets notification_sets  on notification_sets.port_node_id = leaf.id where notification_sets.status=? and alert_type = ?";
				INSERT_NOTIFICATIONSET_RULES = " insert into rule_notifications_detail "
						+ " (rule_id, notification_set_id,for_threshold_level, type, valid_from, valid_to, customer_contact_id, contact_time_from, contact_time_to) "
						+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				INSERT_NOTIFICATIONSET_REGION = " insert into region_notifications_detail "
						+ " (region_rule_id, region_notification_set_id, for_threshold_level, type, valid_from, valid_to, customer_contact_id, contact_time_from, contact_time_to , op_station_id , threshold) "
						+ " values(?, ?, ?, ?, ?, ?, ?, ? , ? , ?, ?) ";
				FETCH_NOTIFICATIONSET_RULES = " select * from rule_notifications_detail " + "where notification_set_id = ? ";
				FETCH_NOTIFICATIONSET_REGIONS = " select * from region_notifications_detail " + "where region_notification_set_id = ? ";
				DELETE_NOTIFICATIONSET_RULES = " delete from rule_notifications_detail " + "where notification_set_id = ? ";
				DELETE_NOTIFICATIONSET_REGION = " delete from region_notifications_detail " + "where region_notification_set_id = ? ";
				FETCH_CUSTOMER_CONTACTS = " select c.id, c.contact_name, c.mobile, c.email from customer_contacts c join port_nodes leaf on (leaf.id = c.customer_id) join port_nodes anc on (anc.id = ? and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (leaf.lhs_number <= anc.lhs_number and leaf.rhs_number >= anc.rhs_number))) order by c.contact_name, c.id ";
				FETCH_CUSTOMER = "select id, name from customers";
				INSERT_CONTACT = " insert into customer_contacts (customer_id , contact_name , phone , mobile , email , address , updated_on, status) "
						+ " values(?, ?, ?, ?, ?, ?, ?," + com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE + ") ";
				// FETCH_CUSTOMER_CONTACTS = " select CC.id, C.name, CC.mobile, CC.email from customers C, customer_contacts CC where C.id = CC.customer_id; ";
				FETCH_RULES_ORG = "select rule.id, rule.port_node_id, rule.name from port_nodes leaf join port_nodes ansc on (ansc.lhs_number <= leaf.lhs_number and "
						+ "ansc.rhs_number >= leaf.rhs_number and leaf.id = ?) join rules rule on (rule.port_node_id = ansc.id)";
				
				FETCH_OP_STATIONS = "select id , name , status from op_station o , opstation_mapping om where o.id = om.op_station_id and o.name not like '%-stop%' and om.port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =?)and rhs_number <= (select rhs_number from port_nodes where id =? ))";
			}
		}
	}

	public static class IOMAP {

		public final static String INSERT_DATA_TO_IO_MAP_INFO;
		public final static String INSERT_DATA_TO_IO_MAP;
		public final static String UPDATE_IO_MAP_INFO;
		public final static String UPDATE_IO_MAP;
		public final static String FETCH_IO_MAP_INFO;
		public final static String FETCH_IO_MAP;
		public final static String FETCH_IO_MAP_DATA;
		public final static String FETCH_IO_MAP_INFO_DATA;
		public final static String DELETE_IO_MAP_INFO;
		public final static String DELETE_IO_MAP;
		public final static String FETCH_DEVICE_NAME;
		public final static String UPDATE_DEVICE_NAME;
		public final static String FETCH_DEVICE_MODEL_DATA;
		public final static String FETCH_DEVICE_MODEL_PIN_DATA;
		public static final String FETCH_DIMENSION_MAP_LIST;
		static {
			if (isMySql) {
				INSERT_DATA_TO_IO_MAP_INFO = " insert into io_map_info(name,description,device_model_info_id,updated_on,port_node_id, status) values(?,?,?,?,?,"
						+ com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE + ") ";
				INSERT_DATA_TO_IO_MAP = " insert into io_map(io_map_info_id, io_id, attribute_id, updated_on,dimension_reading_id, min_val, max_val,transient,validOnPower) values(?,?,?,?,?,?,?,?,?) ";
				UPDATE_IO_MAP_INFO = "update io_map_info set name = ?, description = ?, device_model_info_id = ?, updated_on = ?, port_node_id = ? where id = ?";
				UPDATE_IO_MAP = "update io_map set  io_id= ?, attribute_id = ?, updated_on = ? where io_map_info_id = ? ";
				FETCH_IO_MAP_INFO = " select * from io_map_info where id = ? ";
				FETCH_IO_MAP = " select io_id, attribute_id,dimension_reading_id, min_val, max_val,transient,validOnPower from io_map where io_map_info_id = ?";
				FETCH_IO_MAP_DATA = "select * from io_map";
				FETCH_IO_MAP_INFO_DATA = "select io_map_info.id, io_map_info.name, io_map_info.description, io_map_info.device_model_info_id,io_map_info.status, io_map_info.updated_on, io_map_info.port_node_id from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join io_map_info on (io_map_info.port_node_id = leaf.id) where io_map_info.status = ?  order by io_map_info.status desc, id desc";
				DELETE_IO_MAP_INFO = " update io_map_info set status = ?  where id = ?";
				DELETE_IO_MAP = "delete from io_map where io_map_info_id = ?";
				FETCH_DEVICE_NAME = " select name from device_model_info where id = ?";
				UPDATE_DEVICE_NAME = " update device_model_info set name = ? where id = ?  ";
				FETCH_DEVICE_MODEL_DATA = " select name, id from device_model_info where status != ? order by name  ";
				FETCH_DEVICE_MODEL_PIN_DATA = " select io_count, id from device_model_info where status != ?  ";
				FETCH_DIMENSION_MAP_LIST = "select ";
			}
		}
	}

	// Rule DB Queries
	public static class RULEPROCESSOR {
		public final static String FETCH_RULE;
		public final static String FETCH_NOTIFICATION_DETAILS;

		static {
			if (isMySql) {
				FETCH_RULE =

				"SELECT v.id vehicle_id," + "       rules.status," + "       v.customer_id," + "       v.name," + "       rr.rule_id," + "       rr.ruleset_id,"
						+ "       rr.active_from," + "       rr.active_to," + "       cc.id condition_clause_id," + "       cc.rule_type_id," + "       cc.param_0_type,"
						+ "       cc.param_0," + "       cc.logical_operator," + "       cc.value_1," + "       cc.value_2," + "       rrt.region_id,"
						+ "       rrt.value_1 first_Threshold," + "       rrt.value_2 second_Threshold," + "       rrt.value_3 third_Threshold" + "  FROM vehicle v,"
						+ "       vehicle_rulesets vr," + "       ruleset_rules rr," + "          rules" + "       LEFT JOIN" + "          (rules_region_threshold rrt)"
						+ "       ON (rules.id = rrt.rule_id)," + "          rules r2" + "       LEFT JOIN" + "          (conditions_clauses cc)"
						+ "       ON (cc.rule_id = r2.id)" + " WHERE     V.id like ?" + "       AND v.id = vr.vehicle_id" + "       AND vr.ruleset_id = rr.ruleset_id"
						+ "       AND rr.rule_id = rules.id" + "       AND rules.id = r2.id" + "		 AND rules.id like ?" + " ORDER BY rr.ruleset_id," + "         rr.rule_id,"
						+ "         cc.id";

				FETCH_NOTIFICATION_DETAILS =

				"SELECT DISTINCT vns.notification_set_id," + "                rr.ruleset_id," + "                rr.rule_id," + "                type,"
						+ "                rnd.contact_time_from," + "                rnd.contact_time_to," + "                rnd.customer_contact_id,"
						+ "                rnd.valid_from," + "                rnd.valid_to" + "  FROM vehicle_notification_sets vns," + "       notification_sets ns,"
						+ "       rule_notifications_detail rnd," + "       vehicle_rulesets vr," + "       ruleset_rules rr" + " WHERE     vns.notification_set_id = ns.id"
						+ "       AND vr.ruleset_id = rr.ruleset_id" + "       AND rr.rule_id = rnd.rule_id" + "       AND vns.notification_set_id = rnd.notification_set_id"
						+ "       AND rr.rule_id = ?" + "       AND now() BETWEEN rnd.valid_from AND rnd.valid_to";

			}
		}
	}

	public static class DEVICEMODELINFO {
		public final static String INSERT_DATA_TO_DEVICE_MODEL_INFO;
		public final static String INSERT_DATA_TO_DEVICE_MODEL_COMMANDS;
		public final static String UPDATE_DATA_TO_DEVICE_MODEL_INFO;
		public final static String FETCH_DATA_FROM_DEVICE_MODEL_INFO;
		public final static String DELETE_DATA_FROM_DEVICE_MODEL_INFO;
		public final static String DELETE_DATA_FROM_DEVICE_MODEL_COMMANDS;
		public final static String FETCH_DATA_FOR_DEVICE_MODEL;
		static {
			if (isMySql) {
				INSERT_DATA_TO_DEVICE_MODEL_INFO = " insert into device_model_info(name,internal_battery,ignition_on_off,voice,buzzer,io_count,display,updated_on, status,device_model,device_version,command_word)"
						+ " values(?,?,?,?,?,?,?,?," + com.ipssi.tracker.common.util.ApplicationConstants.ACTIVE + ",?,?,?)";
				INSERT_DATA_TO_DEVICE_MODEL_COMMANDS = "insert into device_model_commands(device_model_info_id, command1, command2, command3, command4) values(?,?,?,?,?)";
				UPDATE_DATA_TO_DEVICE_MODEL_INFO = "update device_model_info set name = ?, internal_battery = ?, ignition_on_off = ?, "
						+ "voice = ?, buzzer = ?, io_count = ?, display = ?, updated_on = ? , device_model = ?,device_version = ?,command_word = ? where id = ?";
				FETCH_DATA_FROM_DEVICE_MODEL_INFO = " select * from device_model_info where status != ?";
				DELETE_DATA_FROM_DEVICE_MODEL_INFO = " update device_model_info set status = ? where id = ?";
				DELETE_DATA_FROM_DEVICE_MODEL_COMMANDS = "delete from device_model_commands where device_model_info_id = ?";
				FETCH_DATA_FOR_DEVICE_MODEL = "select * from device_model_info left join device_model_commands on id  = device_model_info_id where id = ?";
			}
		}
	}

	public static class VEHICLE {
		public final static String FETCH_VEHICLE_TYPE;
		public final static String FETCH_DEVICE_TYPE;
		public final static String FETCH_CUSTOMER;
		public final static String FETCH_IO_POOL;
		public final static String FETCH_RULE_SET;
		public final static String FETCH_ALERT_SET;
		public final static String FETCH_RULE_SET_SEARCH;
		public final static String FETCH_ALERT_SET_SEARCH;
		public final static String FETCH_SERVICE_PROVIDERS;
		public final static String SEARCH_ID_LIST1;
		public final static String SEARCH_ID_LIST2;
		public final static String SEARCH_ID_LIST3;
		public final static String SEARCH_ID_LIST4;
		public final static String SEARCH_ID_LIST5;
		public final static String SEARCH_ID_LIST6;
		public final static String SEARCH_ID_LIST7;
		public final static String SEARCH_ID_LIST8;
		public final static String SEARCH_ID_LIST9;
		public final static String SEARCH_ID_LIST10;
		public final static String SEARCH_ID_LIST11;
		public final static String SEARCH_VEH_1;
		public final static String SEARCH_VEH_2;
		public final static String SEARCH_VEHICLE;
		public final static String FETCH_VEHICLE;
		public final static String FETCH_PORT_NODE;
		public final static String INSERT_VEHICLE;

		public final static String UPDATE_VEHICLE;

		public final static String DELETE_VEHICLE;
		public final static String DELETE_PORT_NODE;
		public final static String DELETE_RULE;
		public final static String DELETE_ALERT;
		public final static String INSERT_PORT_NODE_NOTIME;
		public final static String INSERT_PORT_NODE_WITHTIME;
		public final static String INSERT_RULE;
		public final static String INSERT_ALERT;
		public final static String INSERT_OPSTATION_PROFILE;
		public final static String DELETE_OPSTATION_PROFILE;
		public final static String INSERT_SRCDEST_PROFILE;
		public final static String DELETE_SRCDEST_PROFILE;
		
		public final static String WHERE;
		public final static String AND;
		public final static String ORDER_BY;

		public final static String GET_ALERTSETS;
		public final static String GET_RULESETS;
		public final static String GET_IOMAPSETS;
		public final static String GET_OPPROFILES;
		public final static String GET_TRIPPARAM_PROFILES;
		public final static String GET_SRCDEST_PROFILES;
		public final static String GET_ALERTSETS_MULTI_1;
		public final static String GET_RULESETS_MULTI_1;
		public final static String GET_IOMAPSETS_MULTI_1;
		public final static String GET_OPPROFILE_MULTI_1;
		public final static String GET_TRIPPARAM_PROFILE_MULTI_1;
		public final static String GET_SRCDEST_PROFILE_MULTI_1;
		public final static String GET_ALERTSETS_MULTI_2;
		public final static String GET_RULESETS_MULTI_2;
		public final static String GET_IOMAPSETS_MULTI_2;
		public final static String GET_OPPROFILE_MULTI_2;
		public final static String GET_TRIPPARAM_PROFILE_MULTI_2;
		public final static String GET_SRCDEST_PROFILE_MULTI_2;
		public final static String FETCH_CUSTOMER_VEHICLES;
		public final static String FETCH_CUSTOMER_VEHICLES_SIMPLE;
		public final static String INSERT_VEHICLE_NEW;
		public final static String UPDATE_VEHICLE_NEW;
		public final static String UPDATE_VEHICLE_TOP_SETUP;
		public final static String UPDATE_VEHICLE_TRIPPARAM_PROFILE;
		public final static String INSERT_VEHICLE_HISTORY;
		public final static String INSERT_DISREGARD_DATA;
		public final static String UPDATE_DISREGARD_DATA;
		public final static String LOOKUP_BY_NAME;
		public final static String LOOKUP_BY_ID;
		public final static String UPDATE_STATUS;
		public final static String GET_VEHICLE_TEMPLATE;
		

		static {
			if (true) {
				GET_VEHICLE_TEMPLATE = "select vehicle.id from port_nodes leaf join port_nodes anc on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join vehicle on (vehicle.customer_id = anc.id) where vehicle.is_template = 1 order by anc.lhs_number desc, vehicle.status desc, vehicle.id desc";
				INSERT_VEHICLE_NEW = "insert into vehicle (customer_id, name, std_name, type, device_internal_id, device_model_info_id, device_serial_number, sim, device_change_reason_code, device_change_reason, status, sim_number, service_provider_id, redirect_url, miscellaneous, driver,fieldone, fieldtwo, fieldthree, fieldfour,fieldfive,fieldsix,fieldseven,fieldeight, updated_on, detailed_status, info_complete, updated_by, do_trip, do_rule, work_area, last_update_comment, install_date, flag, server_ip_1, server_port_1, server_ip_2, server_port_2, sub_type,rfid_epc, ble_tag1, ble_tag2) values "+
										"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				UPDATE_VEHICLE_NEW = "update vehicle set customer_id = ?, name=?, std_name = ?, type=?, device_internal_id=?, device_model_info_id=?, device_serial_number=?, sim=?, device_change_reason_code=?, device_change_reason=?, status=?, sim_number=?, service_provider_id=?, redirect_url=?, miscellaneous=?, driver=?, fieldone=?, fieldtwo=?, fieldthree=?, fieldfour=?,fieldfive=?,fieldsix=?,fieldseven=?,fieldeight=?, updated_on=?, detailed_status=?, info_complete=?, updated_by = ?, do_trip = ?, do_rule = ?, work_area=?, install_date = ?, last_update_comment = (case when ? is null then last_update_comment else ? end), flag=?, server_ip_1=?, server_port_1=?, server_ip_2=?,server_port_2=?, sub_type=?, rfid_epc=?, ble_tag1=?,ble_tag2=?  where vehicle.id = ? ";
				UPDATE_VEHICLE_TOP_SETUP = "update vehicle set io_set_id=? where vehicle.id = ?";
				UPDATE_VEHICLE_TRIPPARAM_PROFILE = "update vehicle set tripparam_profile_id = ? where vehicle.id=?";
				INSERT_VEHICLE_HISTORY = "insert into vehicle_history (vehicle_id, device_id, on_date, description, change_code, change_from, change_to, addnl_note, addnl_code, updated_by) values (?,?,?,?,?,?,?,?,?, ?)";
				INSERT_DISREGARD_DATA = "insert into vehicle_disregard_data (vehicle_id, device_id, why, from_date, to_date, completion) values (?,?,?,?,?,?)";
				LOOKUP_BY_NAME = "select id,name, customer_id from vehicle where name = ? order by updated_on desc";
				LOOKUP_BY_ID = "select id,name, customer_id from vehicle where id = ? ";
				UPDATE_STATUS = "update vehicle set status=?, updated_on=? where id=?";
				UPDATE_DISREGARD_DATA = "";
				FETCH_VEHICLE_TYPE = " select id, type name from vehicle_type ";
				FETCH_DEVICE_TYPE = " select id, name from device_model_info where status != ? ";
				FETCH_SERVICE_PROVIDERS = " select id, name from service_providers ";
				FETCH_CUSTOMER = " select id, full_name name from port_nodes ";
				FETCH_IO_POOL = " select pnf.id port_node_id, imi.id, imi.name from io_map_info imi left join " + " (select pnp.id parent_id, pnc.id "
						+ " from port_nodes pnp, port_nodes pnc " + " where pnp.lhs_number <= pnc.lhs_number and pnp.rhs_number >= pnc.rhs_number) pnf "
						+ " on imi.port_node_id = pnf.parent_id " + " where imi.status != ? ";
				FETCH_RULE_SET = " select pnf.id port_node_id, rs.id, rs.name from rulesets rs left join " + " (select pnp.id parent_id, pnc.id "
						+ " from port_nodes pnp, port_nodes pnc " + " where pnp.lhs_number <= pnc.lhs_number and pnp.rhs_number >= pnc.rhs_number) pnf "
						+ " on rs.port_node_id = pnf.parent_id " + " where rs.status = ? ";
				FETCH_ALERT_SET = " select pnf.id port_node_id, ns.id, ns.name from notification_sets ns left join " + " (select pnp.id parent_id, pnc.id "
						+ " from port_nodes pnp, port_nodes pnc " + " where pnp.lhs_number <= pnc.lhs_number and pnp.rhs_number >= pnc.rhs_number) pnf "
						+ " on ns.port_node_id = pnf.parent_id " + " where ns.status = ? ";
				FETCH_RULE_SET_SEARCH = " select distinct id, name from rulesets where status  = ? ";
				FETCH_ALERT_SET_SEARCH = " select distinct id, name from notification_sets where status  = ? ";
				SEARCH_ID_LIST1 = " select distinct v.id from vehicle v ";
				SEARCH_ID_LIST2 = " , vehicle_access_groups vag, port_nodes pn ";
				SEARCH_ID_LIST3 = " v.id = vag.vehicle_id and vag.port_node_id = pn.id and pn.id in "
						+ " (select id from port_nodes pn where lhs_number >= (select lhs_number from port_nodes where id = ";
				SEARCH_VEH_1 = " ) and rhs_number <= (select rhs_number from port_nodes where id = ";
				SEARCH_VEH_2 = " )) ";
				SEARCH_ID_LIST4 = " lower(v.name) like ";
				SEARCH_ID_LIST5 = " type in ";
				SEARCH_ID_LIST6 = " lower(sim) like ";
				SEARCH_ID_LIST7 = " lower(device_serial_number) like ";
				SEARCH_ID_LIST8 = " device_model_info_id in ";
				SEARCH_ID_LIST9 = " v.id in (select vehicle_id from vehicle_rulesets where ruleset_id in ";
				SEARCH_ID_LIST10 = " v.id in (select vehicle_id from vehicle_notification_sets where notification_set_id in ";
				SEARCH_ID_LIST11 = " order by v.id ";
				WHERE = " where ";
				AND = " and ";
				SEARCH_VEHICLE = " select distinct v.id, v.name, v.type type_id, vt.type, v.device_serial_number, v.device_model_info_id, dmi.name device_model_info_name, "
						+ " v.device_internal_id, v.sim, v.sim_number, v.service_provider_id, sp.name service_provider_name, v.install_date, pn.id pn_id, "
						+ " pn.name pn_name, io.id io_id, io.name io_name, vr.ruleset_id rs_id, rs. name rs_name, " + " vn.notification_set_id as_id, ns.name as_name "
						+ " from vehicle v left join vehicle_type vt on v.type = vt.id, "
						+ " vehicle v1 left join vehicle_access_groups vag on v1.id = vag.vehicle_id left join port_nodes pn on vag.port_node_id = pn.id, "
						+ " vehicle v2 left join device_model_info dmi on v2.device_model_info_id = dmi.id, " + " vehicle v3 left join io_map_info io on io.id = v3.io_set_id, "
						+ " vehicle v4 left join vehicle_rulesets vr on v4.id = vr.vehicle_id left join rulesets rs on vr.ruleset_id = rs.id, "
						+ " vehicle v5 left join vehicle_notification_sets vn on v5.id = vn.vehicle_id left join notification_sets ns on vn.notification_set_id = ns.id, "
						+ "  vehicle v6 left join service_providers sp on v6.service_provider_id = sp.id " + " where v.status != ? "
						+ " and v.id = v1.id and v.id = v2.id and v.id = v3.id and v.id = v4.id and v.id = v5.id and v.id in ";
				ORDER_BY = " order by name, id, type ";
				FETCH_VEHICLE = " select distinct v.id, v.name, v.type type_id, vt.type, v.device_serial_number, v.device_model_info_id, "
						+ " dmi.name device_model_info_name, v.device_internal_id, v.sim, v.sim_number, v.service_provider_id, sp.name service_provider_name, "
						+ " v.install_date, pn.id pn_id, pn.name pn_name, io.id io_id, io.name io_name, vr.ruleset_id rs_id, rs. name rs_name, "
						+ " vn.notification_set_id as_id, ns.name as_name " + " from vehicle v left join vehicle_type vt on v.type = vt.id, "
						+ " vehicle v1 left join vehicle_access_groups vag on v1.id = vag.vehicle_id left join port_nodes pn on vag.port_node_id = pn.id, "
						+ " vehicle v2 left join device_model_info dmi on v2.device_model_info_id = dmi.id, " + " vehicle v3 left join io_map_info io on io.id = v3.io_set_id, "
						+ " vehicle v4 left join vehicle_rulesets vr on v4.id = vr.vehicle_id left join rulesets rs on vr.ruleset_id = rs.id, "
						+ " vehicle v5 left join vehicle_notification_sets vn on v5.id = vn.vehicle_id left join notification_sets ns on vn.notification_set_id = ns.id, "
						+ "  vehicle v6 left join service_providers sp on v6.service_provider_id = sp.id " + " where v.status != ? "
						+ " and v.id = v1.id and v.id = v2.id and v.id = v3.id and v.id = v4.id and v.id = v5.id " + " order by name, id, type ";
				FETCH_PORT_NODE = " select id, full_name from port_nodes ";
				
				FETCH_CUSTOMER_VEHICLES_SIMPLE = "select distinct l1.gps_record_time, l1.name as position_name, l1.vehicle_id, r1.vehicle_name" 
						+ " from logged_data as l1 inner join (select logged_data.name as position_name , max(logged_data.gps_record_time)" 
						+ " as gps_record_time, v1.name as vehicle_name, v1.id as vehicle_id from (select vehicle.name, vehicle.id from" 
						+ " vehicle where vehicle.name like ?) as v1 left join logged_data on v1.id = logged_data.vehicle_id group by v1.id)"
						+ " as r1 on r1.gps_record_time = l1.gps_record_time and l1.vehicle_id = r1.vehicle_id";
							
				FETCH_CUSTOMER_VEHICLES = " select distinct l1.gps_record_time, l1.name as position_name, l1.vehicle_id as vehicle_id, r1.vehicle_name from logged_data as l1"
						+ " inner join (select logged_data.name as position_name , max(logged_data.gps_record_time) as gps_record_time, v1.name as vehicle_name, v1.id as vehicle_id from" 
						+ " (select vehicle.name, vehicle.id from (select customer_contacts.customer_id, customer_contacts.phone from customer_contacts  where phone = ?) c1" 
						+ " left join vehicle_access_groups on c1.customer_id = vehicle_access_groups.port_node_id left join vehicle on vehicle_access_groups.vehicle_id = vehicle.id ) v1" 
						+ " left join logged_data on v1.id = logged_data.vehicle_id where v1.name like ? group by v1.id) as r1  on r1.gps_record_time = l1.gps_record_time and l1.vehicle_id = r1.vehicle_id"; 
				
				INSERT_VEHICLE = " insert into vehicle (name, type, device_serial_number, device_internal_id, device_model_info_id, sim, io_set_id, "
						+ " install_date, updated_on, sim_number, service_provider_id, customer_id, status, redirect_url,miscellaneous,fieldone,fieldtwo,fieldthree,fieldfour,fieldfive,fieldsix,fieldseven,fieldeight, detailed_status, info_complete, is_template, sub_type) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,0)";

				UPDATE_VEHICLE = " update vehicle set name = ?, type = ?, device_serial_number = ?, device_internal_id = ?, "
						+ " device_model_info_id = ?, sim = ?, io_set_id = ?, install_date = ?, updated_on = ?, sim_number = ?, service_provider_id = ?, customer_id=?, status=?, redirect_url=? ,miscellaneous = ?" +
						" ,fieldone = ?,fieldtwo = ?,fieldthree = ?,fieldfour = ?,fieldfive = ?,fieldsix = ?,fieldseven = ?,fieldeight = ?, detailed_status=?, info_complete=?, sub_type=?  where id = ? ";

				DELETE_VEHICLE = " update vehicle set status = ? where id = ? ";
				DELETE_PORT_NODE = " delete from vehicle_access_groups where vehicle_id = ? ";
				DELETE_RULE = " delete from vehicle_rulesets where vehicle_id = ? ";
				DELETE_ALERT = " delete from vehicle_notification_sets where vehicle_id = ? ";
				INSERT_PORT_NODE_NOTIME = " insert into vehicle_access_groups(vehicle_id, port_node_id) values(?, ?) ";
				INSERT_PORT_NODE_WITHTIME = " insert into vehicle_access_groups(vehicle_id, port_node_id, auto_removal_type,org_assignment_type,effective_from) values(?, ?,?,?,?) ";
				INSERT_RULE = " insert into vehicle_rulesets values(?, ?) ";
				INSERT_ALERT = " insert into vehicle_notification_sets values(?, ?) ";
				DELETE_OPSTATION_PROFILE = "delete from vehicle_opstation_profiles where vehicle_id=?";
				INSERT_OPSTATION_PROFILE = "insert into vehicle_opstation_profiles(vehicle_id, opstation_profile_id) values (?,?)";
				
				DELETE_SRCDEST_PROFILE = "delete from vehicle_srcdest_profiles where vehicle_id=?";
				INSERT_SRCDEST_PROFILE = "insert into vehicle_srcdest_profiles(vehicle_id, srcdest_profile_id) values (?,?)";
				
				GET_ALERTSETS = "select notification_sets.id, notification_sets.name from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id = ?) join notification_sets on (notification_sets.port_node_id = anc.id) where notification_sets.status != ? order by notification_sets.name ";
				GET_IOMAPSETS = "select io_map_info.id, io_map_info.name from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id = ?) join io_map_info on (io_map_info.port_node_id = anc.id)  where io_map_info.status != ? order by io_map_info.name ";
				GET_RULESETS = "select rulesets.id, rulesets.name from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id = ?) join rulesets on (rulesets.port_node_id = anc.id)  where rulesets.status != ? order by rulesets.name ";
				GET_OPPROFILES  = "select opstation_profiles.id, opstation_profiles.name from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id = ?) join opstation_profiles on (opstation_profiles.port_node_id = anc.id)  where opstation_profiles.status != ? order by opstation_profiles.name ";
				GET_SRCDEST_PROFILES = "select srcdest_profiles.id, srcdest_profiles.name from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id = ?) join srcdest_profiles on (srcdest_profiles.port_node_id = anc.id)  where srcdest_profiles.status != ? order by srcdest_profiles.name ";
				GET_TRIPPARAM_PROFILES = "select tripparam_profiles.id, tripparam_profiles.name from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id = ?) join tripparam_profiles on (tripparam_profiles.port_node_id = anc.id)  where tripparam_profiles.status != ? order by tripparam_profiles.name ";

				GET_ALERTSETS_MULTI_1 = "select distinct notification_sets.id, notification_sets.name, leaf.id from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id in (";
				GET_ALERTSETS_MULTI_2 = ")) join notification_sets on (notification_sets.port_node_id = anc.id) where notification_sets.status != ? order by leaf.id, notification_sets.name ";
				GET_IOMAPSETS_MULTI_1 = "select distinct io_map_info.id, io_map_info.name, leaf.id from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id in (";
				GET_IOMAPSETS_MULTI_2 = ")) join io_map_info on (io_map_info.port_node_id = anc.id)  where io_map_info.status != ? order by leaf.id, io_map_info.name ";
				GET_RULESETS_MULTI_1 = "select distinct rulesets.id, rulesets.name, leaf.id from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id in(";
				GET_RULESETS_MULTI_2 = ")) join rulesets on (rulesets.port_node_id = anc.id)  where rulesets.status != ? order by leaf.id, rulesets.name ";
				GET_OPPROFILE_MULTI_1 = "select distinct opstation_profiles.id, opstation_profiles.name, leaf.id from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id in (";
				GET_OPPROFILE_MULTI_2 = ")) join opstation_profiles on (opstation_profiles.port_node_id = anc.id) where opstation_profiles.status != ? order by leaf.id, opstation_profiles.name ";
				GET_TRIPPARAM_PROFILE_MULTI_1 = "select distinct tripparam_profiles.id, tripparam_profiles.name, leaf.id from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id in (";
				GET_TRIPPARAM_PROFILE_MULTI_2 = ")) join tripparam_profiles on (tripparam_profiles.port_node_id = anc.id) where tripparam_profiles.status != ? order by leaf.id, tripparam_profiles.name ";
				GET_SRCDEST_PROFILE_MULTI_1 = "select distinct srcdest_profiles.id, srcdest_profiles.name, leaf.id from port_nodes leaf join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and leaf.id in (";
				GET_SRCDEST_PROFILE_MULTI_2 = ")) join srcdest_profiles on (srcdest_profiles.port_node_id = anc.id) where srcdest_profiles.status != ? order by leaf.id, srcdest_profiles.name ";
				
			}
		}
	}

	public static class TICKET {
		public final static String GET_TICKETS;
		public final static String SEARCH_TICKET_ID_1;
		public final static String SEARCH_TICKET_ID_2;
		public final static String SEARCH_TICKET_ID_3;
		public final static String SEARCH_TICKET_ID_4;
		public final static String SEARCH_TICKET_ID_5;
		public final static String SEARCH_TICKET_ID_6;
		public final static String SEARCH_TICKET_ID_7;
		public final static String SEARCH_TICKET_ID_8;
		public final static String SEARCH_TICKET_ID_9;
		public final static String SEARCH_TICKET_ID_10;
		public final static String WHERE;
		public final static String AND;
		public final static String SEARCH_TICKET_1;
		public final static String SEARCH_TICKET_2;
		public final static String INSERT_TICKET;
		public final static String INSERT_ACTION;
		public final static String UPDATE_TICKET;
		public final static String GET_TICKET_ACTION;
		public final static String GET_HISTORY;

		static {
			if (true) {
				GET_TICKETS = " select veh.id, veh.name, veh.sim, veh.device_internal_id, veh.type, veh.port_node_id, veh.port_name, "
						+ " veh.ticket_id, veh.ticket_name, veh.status, veh.description, veh.created_on, tact.id action_id, "
						+ " tact.status action_status, tact.action, tact.updated_on "
						+ " from (select v.id, v.name, v.sim, v.device_internal_id, vt.type, vag.port_node_id, pn.name port_name, "
						+ " t.id ticket_id, t.name ticket_name, t.status, t.description, t.created_on "
						+ " from vehicle v left join ticket t on v.id = t.vehicle_id, vehicle_access_groups vag, vehicle_type vt, port_nodes pn "
						+ " where v.id = vag.vehicle_id and v.type = vt.id and vag.port_node_id = pn.id and v.status != ? "
						+ " and ifnull(t.id, 0) = ifnull((select max(id) from ticket t1 where t1.vehicle_id = t.vehicle_id), 0)) veh " + " left  join "
						+ " (select ticket_id, id, status, action, updated_on from ticket_action ta " + " where ta.id = (select max(id) from ticket_action ta2 "
						+ " where ta.ticket_id = ta2.ticket_id)) tact on veh.ticket_id = tact.ticket_id order by veh.name ";
				SEARCH_TICKET_ID_1 = " select t.id from ticket t ";
				SEARCH_TICKET_ID_2 = " , vehicle v ";
				SEARCH_TICKET_ID_3 = " v.id = t.vehicle_id ";
				SEARCH_TICKET_ID_4 = " and lower(v.name) like ";
				SEARCH_TICKET_ID_5 = " , vehicle_access_groups vag ";
				SEARCH_TICKET_ID_6 = " and v.id = vag.vehicle_id and vag.port_node_id = ";
				SEARCH_TICKET_ID_7 = " t.status in ";
				SEARCH_TICKET_ID_8 = " , ticket_action ta ";
				SEARCH_TICKET_ID_9 = " t.id = ta.ticket_id and ta.status in ";
				SEARCH_TICKET_ID_10 = " and ta.id =  (select max(id) from ticket_action ta2 where ta.ticket_id = ta2.ticket_id) ";
				WHERE = " where ";
				AND = " and ";
				SEARCH_TICKET_1 = " select veh.id, veh.name, veh.sim, veh.device_internal_id, veh.type, veh.port_node_id, veh.port_name, "
						+ " veh.ticket_id, veh.ticket_name, veh.status, veh.description, veh.created_on, tact.id action_id, "
						+ " tact.status action_status, tact.action, tact.updated_on "
						+ " from (select v.id, v.name, v.sim, v.device_internal_id, vt.type, vag.port_node_id, pn.name port_name, "
						+ " t.id ticket_id, t.name ticket_name, t.status, t.description, t.created_on "
						+ " from vehicle v left join ticket t on v.id = t.vehicle_id, vehicle_access_groups vag, vehicle_type vt, port_nodes pn "
						+ " where v.id = vag.vehicle_id and v.type = vt.id and vag.port_node_id = pn.id and ifnull(v.status, '') != ? and t.id in ";
				SEARCH_TICKET_2 = " and ifnull(t.id, 0) = ifnull((select max(id) from ticket t1 where t1.vehicle_id = t.vehicle_id), 0)) veh " + " left  join "
						+ " (select ticket_id, id, status, action, updated_on from ticket_action ta " + " where ta.updated_on = (select max(id) from ticket_action ta2 "
						+ " where ta.ticket_id = ta2.ticket_id)) tact on veh.ticket_id = tact.ticket_id order by veh.name ";
				INSERT_TICKET = " insert into ticket(vehicle_id,  name, description, status, created_on, updated_on) values(?, ?, ?, ?, ?, ?) ";
				INSERT_ACTION = " insert into ticket_action(ticket_id,  action,  status, updated_on) values(?, ?, ?, ?) ";
				UPDATE_TICKET = " update ticket set status = ?, updated_on = ? where id = ? ";
				GET_TICKET_ACTION = " select v.name vehicle_name, t.id, t.name, t.description, t.status, t.created_on, t.updated_on, action, "
						+ " ta.status action_status, ta.updated_on action_updated_on "
						+ " from vehicle v, ticket t, ticket_action ta where t.id = ta.ticket_id and t.id = ? and v.id = t.vehicle_id " + " order by action_updated_on desc ";
				GET_HISTORY = " select veh.id, veh.name, veh.ticket_id, veh.ticket_name, veh.status, veh.description, veh.created_on, veh.updated_on, "
						+ " tact.id action_id, tact.status action_status, tact.action, tact.updated_on action_updated_on "
						+ " from (select v.id, v.name, v.sim, v.device_internal_id, vt.type, vag.port_node_id, pn.name port_name, "
						+ " t.id ticket_id, t.name ticket_name, t.status, t.description, t.created_on, t.updated_on "
						+ " from vehicle v left join ticket t on v.id = t.vehicle_id, vehicle_access_groups vag, vehicle_type vt, port_nodes pn "
						+ " where v.id = vag.vehicle_id and v.type = vt.id and vag.port_node_id = pn.id and ifnull(v.status, '') != ? and v.id = ? ) veh " + " left  join "
						+ " (select ticket_id, id, status, action, updated_on from ticket_action ta) tact "
						+ " on veh.ticket_id = tact.ticket_id order by veh.created_on desc, veh.updated_on ";
			}
		}
	}

	public static class ORG_SETUP_INFO {
		public final static String INSERT_INT_PARAMS;
		public static final String DELETE_INT_PARAMS;

		public static final String DELETE_DOUBLE_PARAMS;
		public static final String INSERT_DOUBLE_PARAMS;

		public static final String DELETE_STRING_PARAMS;
		public static final String INSERT_STRING_PARAMS;

		public static final String FETCH_DATA;
		public static final String FETCH_DETAILS_FOR_INT_PARAMS;
		public static final String FETCH_DETAILS_FOR_DOUBLE_PARAMS;
		public static final String FETCH_DETAILS_FOR_STRING_PARAMS;
		static {
			if (isMySql) {
				INSERT_INT_PARAMS = "insert into org_lov_params(port_node_id,param_id,param_val,seq) values(?,?,?,?)";
				DELETE_INT_PARAMS = "delete from org_lov_params where port_node_id = ?";

				INSERT_DOUBLE_PARAMS = "insert into org_double_params(port_node_id,param_id,param_val,seq) values(?,?,?,?)";
				DELETE_DOUBLE_PARAMS = "delete from org_double_params where port_node_id = ?";

				DELETE_STRING_PARAMS = "delete from org_string_params where port_node_id = ?";
				INSERT_STRING_PARAMS = "insert into org_string_params(port_node_id,param_id,param_val,seq) values(?,?,?,?)";

				FETCH_DATA = "select name,id from port_nodes";

				FETCH_DETAILS_FOR_INT_PARAMS = "select param_id,param_val from org_lov_params where port_node_id = ? order by seq";
				FETCH_DETAILS_FOR_DOUBLE_PARAMS = "select param_id,param_val from org_double_params where port_node_id = ? order by seq";
				FETCH_DETAILS_FOR_STRING_PARAMS = "select param_id,param_val from org_string_params where port_node_id = ? order by seq";
			}
		}
	}

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

		static {
			if (isMySql) {
				INSERT_MENU = " insert into menu_master " + " (port_node_id, user_id, menu_tag, component_file, updated_on) " + " values(?, ?, ?, ?, ?) ";
				INSERT_UI_COLUMN = " insert into ui_column " + " (menu_id, column_name, attribute_name, attribute_value, updated_on) " + " values(?, ?, ?, ?, ?) ";
				INSERT_UI_PARAM = " insert into ui_parameter " + " (menu_id, param_name, param_value, updated_on) " + " values(?, ?, ?, ?) ";
				UPDATE_MENU = " update menu_master set " + " port_node_id = ?, user_id = ?, menu_tag = ?, component_file = ?, updated_on = ?" + " where id = ? ";
				DELETE_UI_COLUMN_BY_MENU = " delete from ui_column " + "where menu_id = ? ";
				DELETE_UI_PARAM_BY_MENU = " delete from ui_parameter " + "where menu_id = ? ";
				FETCH_MENU = " select * from menu_master " + "where id = ? ";
				FETCH_UI_COLUMN_BY_MENU = " select * from ui_column " + "where menu_id = ? ";
				FETCH_UI_PARAM_BY_MENU = " select * from ui_parameter " + "where menu_id = ? ";
				FETCH_MENUMASTER_USER = " select * from menu_master " + "where user_id = ? and  menu_tag = ? and  component_file = ? and  row_table = ? and  column_table = ? ";
				FETCH_MENUMASTER_PORT = " select * from menu_master "
						+ "where port_node_id = ? and  menu_tag = ? and  component_file = ? and  row_table = ? and  column_table = ? ";
			}
		}
	}

	public static class Dimension {
		// public static final String FETCH_ID;
		public static final String FETCH_ID_DETAILS;
		public static final String INSERT_INFO;
		public static final String INSERT_DETAILS;
		public static final String DELETE_DETAILS;
		public static final String UPDATE_INFO;
		public static final String FETCH_LIST;
		public static final String DELETE_INFO;
		public static final String GET_MAPSETS_USING;
		static {
			if (isMySql) {
				GET_MAPSETS_USING = "select distinct io_map.io_map_info_id from io_map join io_map_info on (io_map.io_map_info_id = io_map_info.id) where io_map_info.status=1  and io_map.dimension_reading_id in ("; //rest added in query
				// FETCH_ID = "select name,port_node_id,description from dimension_values_map_info where id = ? ";
				FETCH_ID_DETAILS = "select reading,value from dimension_value_map where dimension_value_map_info_id = ?";

				INSERT_INFO = "insert into dimension_values_map_info(name,description,port_node_id,status,updated_on) values(?,?,?,?,?)";
				INSERT_DETAILS = "insert into dimension_value_map(dimension_value_map_info_id,reading,value,updated_on) values(?,?,?,?)";

				DELETE_DETAILS = "delete from dimension_value_map where dimension_value_map_info_id = ?";

				UPDATE_INFO = " update dimension_values_map_info set name= ?,port_node_id=?,description=?,status=?,updated_on=? where id=?";

				//FETCH_LIST = "select dimension_values_map_info.id, dimension_values_map_info.name, dimension_values_map_info.description, dimension_values_map_info.port_node_id,dimension_values_map_info.status, dimension_values_map_info.updated_on "
				//		+ "from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join dimension_values_map_info on (dimension_values_map_info.port_node_id = leaf.id) where dimension_values_map_info.status != ? order by dimension_values_map_info.id desc ";
				FETCH_LIST = "select dimension_values_map_info.id, dimension_values_map_info.name, dimension_values_map_info.description, dimension_values_map_info.port_node_id,dimension_values_map_info.status, dimension_values_map_info.updated_on "+
				" from dimension_values_map_info join port_nodes pn on (pn.id =  dimension_values_map_info.port_node_id) "+
				" join port_nodes parOrDesc on ( parOrDesc.id = ? and "+
				"	( "+
				"	 (parOrDesc.lhs_number <= pn.lhs_number and parOrDesc.rhs_number >= pn.rhs_number) or "+
				"	 (parOrDesc.lhs_number >= pn.lhs_number and parOrDesc.rhs_number <= pn.rhs_number) "+
				"	) "+
				" ) where dimension_values_map_info.status = ? order by dimension_values_map_info.id desc ";



				DELETE_INFO = "update dimension_values_map_info set status = ? where id = ?";
			}
		}
	}

	public static class VendorInfo {

		public static final String INSERT_IN_TO_VENDORINFO;
		public static final String SELECT_FROM_VENDORINFO;
		public static final String SELECT_CURRENT_INFO;
		static {
			INSERT_IN_TO_VENDORINFO = "insert into vendorinfo (ticket_id,vehiclename,vdate,vnotes,vissue) values(?,?,?,?,?) ";
			SELECT_FROM_VENDORINFO = "select vdate,vnotes,vissue from vendorinfo where ticket_id = ? order by vdate DESC";
			SELECT_CURRENT_INFO = "select v.ticket_id,v.vnotes,v.vdate from (select a.ticket_id,a.vnotes,a.vdate from vendorinfo a  order by a.vdate DESC) v  group by ticket_id";
		}
	}

	public static class FieldInfo {

		public static final String INSERT_IN_TO_FIELDINFO;
		public static final String SELECT_FROM_FIELDINFO;
		public static final String SELECT_CURRENT_INFO;
		static {
			INSERT_IN_TO_FIELDINFO = "insert into fieldinfo (ticket_id,vehiclename,fdate,fnotes,fissue) values(?,?,?,?,?) ";
			SELECT_FROM_FIELDINFO = "select fdate,fnotes,fissue from fieldinfo where ticket_id = ? order by fdate DESC";
			SELECT_CURRENT_INFO = "select f.ticket_id,f.fnotes,f.fdate from (select a.ticket_id,a.fnotes,a.fdate from fieldinfo a  order by a.fdate DESC) f  group by f.ticket_id";

		}
	}

	public static class ActionInfo {

		public static final String INSERT_IN_TO_ACTIONINFO;
		public static final String SELECT_FROM_ACTIONINFO;
		public static final String SELECT_CURRENT_INFO;
		static {
			INSERT_IN_TO_ACTIONINFO = "insert into actioninfo (ticket_id,vehiclename,adate,anotes,aissue) values(?,?,?,?,?) ";
			SELECT_FROM_ACTIONINFO = "select adate,anotes,aissue from actioninfo where ticket_id = ? order by adate DESC";
			SELECT_CURRENT_INFO = "select f.ticket_id,f.anotes,f.adate from (select a.ticket_id,a.anotes,a.adate from actioninfo a  order by a.adate DESC) f  group by f.ticket_id";
		}
	}

	public static class Currentdata {

		public static final String SELECT_GPS_RECORD_TIME;
		static {
			SELECT_GPS_RECORD_TIME = "select gps_record_time from current_data where vehicle_id = ?";

		}
	}

	public static class Ticketsummary {

		public static final String UPDATE_STATUS;
		public static final String SELECT_V_ID;
		public static final String SELECT_VEHICLE_FOR_TICKET;
		public static final String INSERT_TICKET;
		public static final String SELECT_TICKETS;
		public static final String SEARCH_TICKETS;
		public static final String SEARCH_V_TICKETS;
		public static final String SEARCH_S_TICKETS;
		public static final String SEARCH_D_TICKETS;
		public static final String SEARCH_VENDOR_TICKETS;
		public static final String SEARCH_SV_TICKETS;
		public static final String SEARCH_SC_TICKETS;
		public static final String SEARCH_VC_TICKETS;
		public static final String SELECT_V_FOR_TICKETS;
		public static final String INSERT_MAN_TICKET;
		public static final String INSERT_MAN_TICKET_CLOSED;
		public static final String GET_PORTNAME;
		public static final String GET_VEHICLE_NAME;
		public static final String GET_VEHICLE_FOR_ORG;
		public static final String GET_TICKET_STATUS;
		public static final String SELECT_INFO_TICKETS;
		public static final String UPDATE_MAIN_CAUSE;
		public static final String CHECK_TANK_STATUS;
		public static final String GET_TANKERS_FOR_CHECK;
		public static final String GET_TANKER_REGIONS;
		static {
			SELECT_V_FOR_TICKETS = "select v.name , c.gps_record_time ,v.id,cust.name,cust.id,c.name from vehicle v , current_data c ,port_nodes cust  where c.vehicle_id = v.id and cust.id = v.customer_id and gps_record_time < CURDATE()and v.id not in ( select vehicle_id id from ticket_summary where status like ?)";
			UPDATE_STATUS = "update ticket_summary set status = 0,closed_on = ? ,rootcause = ? where vehicle_id = ? and id = ? ";
			SELECT_V_ID = "select vehicle_id , status from ticket_summary where status like ?";
			SELECT_VEHICLE_FOR_TICKET = "select v.name , c.gps_record_time ,v.id,cust.name from vehicle v , current_data c ,customers cust  where c.vehicle_id = v.id and cust.id = v.customer_id and gps_record_time < CURDATE()";
			INSERT_TICKET = "insert into ticket_summary (vehiclename,status,type,created_on,last_tracked,location,vehicle_id,customer_name,port_node_id) values(?,?,?,?,?,?,?,?,?) ";
			SELECT_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id,customer_name from ticket_summary";
			SEARCH_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id , customer_name from ticket_summary where status=? and created_on between ? and ? and port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? )) ";
			SEARCH_V_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id ,customer_name from ticket_summary where vehiclename LIKE ? and created_on between ? and ?and port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))  ";
			SEARCH_S_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id ,customer_name from ticket_summary where status=? and created_on between ? and ?and port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))  ";
			SEARCH_D_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id, customer_name from ticket_summary where created_on between ? and ?and port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))";
			SEARCH_VENDOR_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id, customer_name from ticket_summary where customer_name = ? and created_on between ? and ? ";
			SEARCH_SV_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id , customer_name from ticket_summary where status=? and vehiclename like ? and created_on between ? and ?and port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))  ";
			SEARCH_SC_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id , customer_name from ticket_summary where status=? and customer_name=? and created_on between ? and ? ";
			SEARCH_VC_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id , customer_name from ticket_summary where vehiclename=? and customer_name=? and created_on between ? and ? ";
			INSERT_MAN_TICKET = "insert into ticket_summary (created_on , last_tracked , vehicle_id , type ,status,port_node_id,customer_name,vehiclename) values (?,?,?,?,?,?,?,?)";
			INSERT_MAN_TICKET_CLOSED = "insert into ticket_summary (created_on , last_tracked , vehicle_id , type ,status,port_node_id,customer_name,vehiclename,closed_On) values (?,?,?,?,?,?,?,?,?)";
			GET_PORTNAME ="select name from port_nodes where id = ?" ;
			GET_VEHICLE_NAME = "select name from vehicle where id = ?" ;
			GET_VEHICLE_FOR_ORG = "select id , name from vehicle where customer_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))  " ;
			GET_TICKET_STATUS = "select status from ticket_summary where id = ?";
		 SELECT_INFO_TICKETS = "select id,vehiclename,status,type,created_on,last_tracked,closed_on,location,vehicle_id,customer_name,rootcause,nextfollowup from ticket_summary where id = ?";
		 UPDATE_MAIN_CAUSE = "update ticket_summary set rootcause = ? ,nextfollowup = ? , lastupdate=? where id = ?";
		 CHECK_TANK_STATUS =  "select  vehicle.name,vehicle.type,current_data.longitude,current_data.latitude ,vehicle.id,current_data.vehicle_id "
		 +",current_data.name as location, current_data.name,port_nodes.name ,(select attribute_value from current_data where vehicle_id = 15364 and attribute_id = 0 )"+
		  "as distence,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 1 )"+
		  "as ignition,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 2 )"+
		  "as batteryLavel,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 3 )"+ 
		 "as fuelLavel,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 4 )"+ 
		 "as EOP,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 5)"+
		  "as HP,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 6) as EOT ,"+
		 "(select attribute_value from current_data where vehicle_id = ? and attribute_id = 7) as FS ,"+
		 "(select attribute_value from current_data where vehicle_id = ? and attribute_id = 8) as PS ,"+
		 "(select attribute_value from current_data where vehicle_id = ? and attribute_id = 9) as tank1,"+
		 "(select attribute_value from current_data where vehicle_id = ? and attribute_id = 10) as tank2,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 11) as tank3"+
		 ",(select attribute_value from current_data where vehicle_id = ? and attribute_id = 12) as tank4 ,(select attribute_value from current_data where vehicle_id = ? and attribute_id = 13) as valve,"+
		 "current_data.attribute_id, current_data.speed ,current_data.gps_record_time ,port_nodes.name as custname from current_data  join vehicle on current_data.vehicle_id = vehicle.id and vehicle.id = ? join port_nodes on port_nodes.id = vehicle.customer_id and vehicle.customer_id in (select id from port_nodes where lhs_number >="+
		  "(select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))group by current_data.name";
		 GET_TANKERS_FOR_CHECK = "select vehicle.id from vehicle where vehicle.customer_id in (select id from port_nodes where lhs_number >="+
		  "(select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? ))";
		 GET_TANKER_REGIONS = "select id from regions where port_node_id = ? ";
		}
	}


	public static class VEHICLEMAINTENANCEINFO{
		public static final String INSERT_TICKET_DETAILS;
		public static final String FETCH_TICKET_DETAILS;
		public static final String UPDATE_TICKET_DETAILS;
		public static final String DELETE_TICKET_DETAILS;
		public static final String INSERT_MAINTENANCE_VEHICLES;
		public static final String UPDATE_MAINTENANCE_VEHICLES;
		public static final String DELETE_MAINTENANCE_VEHICLES;
		public static final String FETCH_MAINTENANCE_VEHICLES;
		public static final String FETCH_ALL_MAINTENANCE_VEHICLES;
		public static final String FETCH_MAINTENANCE_VEHICLES_BYID;

		static{
			if(isMySql){
				INSERT_MAINTENANCE_VEHICLES="insert into vehicle_maint"
						+ " (vehicle_id,planned_start,planned_end,actual_start,actual_end,veh_maint_cause,veh_maint_work_type,veh_maint_total_cost,veh_maint_detail,created_on )"
						+ " values(?,?,?,?,?,?,?,?,?,?)";
				FETCH_ALL_MAINTENANCE_VEHICLES = "select vm1.id, vm1.vehicle_id,v1.name as vehicle_name, v1.customer_id as cutomer_id, vm1.planned_start, vm1.planned_end, vm1.actual_start, vm1.actual_end, vm1.veh_maint_cause, vm1.veh_maint_work_type, vm1.veh_maint_total_cost, vm1.veh_maint_detail "
						+ "from (select * from vehicle_maint where vehicle_maint.status = ?) as vm1 left join vehicle as v1 on vm1.vehicle_id = v1.id ";
				
				FETCH_MAINTENANCE_VEHICLES= "select vm1.id, vm1.vehicle_id,v1.name as vehicle_name, v1.customer_id as cutomer_id, vm1.planned_start, vm1.planned_end, vm1.actual_start, vm1.actual_end, vm1.veh_maint_cause, vm1.veh_maint_work_type, vm1.veh_maint_total_cost, vm1.veh_maint_detail "
					    + "from (select * from vehicle_maint where vehicle_maint.status = ?) as vm1 left join vehicle as v1 on vm1.vehicle_id = v1.id where customer_id = ? ";
				
				FETCH_MAINTENANCE_VEHICLES_BYID ="select vm1.id, vm1.vehicle_id,v1.name as vehicle_name, v1.customer_id as cutomer_id, vm1.planned_start, vm1.planned_end, vm1.actual_start, vm1.actual_end, vm1.veh_maint_cause, vm1.veh_maint_work_type, vm1.veh_maint_total_cost, vm1.veh_maint_detail "
						+ "from (select * from vehicle_maint where vehicle_maint.status = ?) as vm1 left join vehicle as v1 on vm1.vehicle_id = v1.id where vm1.id = ? ";
				
				UPDATE_MAINTENANCE_VEHICLES="update vehicle_maint set planned_start = ?,planned_end = ?,actual_start = ?,actual_end = ?, veh_maint_cause = ?,veh_maint_work_type = ?,veh_maint_detail = ?, veh_maint_total_cost = ?, updated_on=? where id=?";
				DELETE_MAINTENANCE_VEHICLES="update vehicle_maint set status = ? where id = ?";
				INSERT_TICKET_DETAILS="insert into maint_details(ticket_id, item, item_work_type,item_life,item_unit,item_life_n,item_unit_n,item_cost,item_notes, item_name) values(?,?,?,?,?,?,?,?,?,?)";
				FETCH_TICKET_DETAILS="select item,item_name, item_work_type, item_life, item_unit,item_life_n,item_unit_n, item_cost, item_notes from maint_details where ticket_id=?";
				UPDATE_TICKET_DETAILS="";
				DELETE_TICKET_DETAILS="update maint_details set ticket_id = -1 where ticket_id =?";
			}
		}
	}
	
	public static class VEHICLEMAINTENANCEINFONEW{
		public static final String FETCH_MAINT_SERVICE_DETAILS;
		public static final String FETCH_MAINT_PARTS_DETAILS;
		public static final String INSERT_MAINTENANCE_VEHICLES;
		public static final String UPDATE_MAINTENANCE_VEHICLES;
		public static final String DELETE_MAINTENANCE_VEHICLES;
		public static final String FETCH_MAINTENANCE_VEHICLES;
		public static final String FETCH_ALL_MAINTENANCE_VEHICLES;
		public static final String FETCH_MAINTENANCE_VEHICLES_BYID;
		public static final String INSERT_MAINT_SERVICE_DETAILS;
		public static final String INSERT_MAINT_PARTS_DETAILS;
		public static final String DELETE_MAINT_PARTS_DETAILS;
		public static final String DELETE_MAINT_SERVICE_DETAILS;
		public static final String INSERT_SERVICE_TYPE;
		public static final String UPDATE_SERVICE_TYPE;
		public static final String DELETE_SERVICE_TYPE;
		public static final String INSERT_SERVICE_TYPE_DETAILS;
		public static final String DELETE_SERVICE_TYPE_DETAILS;
		public static final String FETCH_SERVICE_TYPE_BYID;
		public static final String FETCH_SERVICE_ITEM_DETAIL;
		public static final String FETCH_SERVICE_TYPE_BY_PORT;
		public static final String FETCH_SERVICE_RULES_FOR_PORT;
		public static final String INSERT_RECURRING_SERVICES;
		public static final String UPDATE_RECURRING_SERVICES;
		public static final String INSERT_RECURRING_CONTACTS;
		public static final String DELETE_RECURRING_CONTACTS;
		public static final String DELETE_RECURRING_SERVICES;
		public static final String FETCH_CURRENT_ODOMETER_BY_VEHICLE_ID;
		public static final String FETCH_CURRENT_ENGINE_HR_BY_VEHICLE_ID;
		public static final String FETCH_RECURRING_REMINDER_LIST;
		public static final String UPDATE_REMINDER_STATUS;
		public static final String FETCH_RECURRING_SERVICES;
		public static final String DELETE_OLD_PART_LOG;
		public static final String INSERT_OLD_PART_LOG;
		public static final String CLOSE_ALL_PENDING_SERVICES;
		public static final String FETCH_ALL_PENDING_SERVICES_FOR_VEHICLE;
		public static final String FETCH_ALL_RECURRING_SERVICES_FOR_VEHICLE;
		static{
			if(isMySql){
				FETCH_ALL_MAINTENANCE_VEHICLES = " select vm1.id ticket_id, vm1.name ticket_name, vm1.vehicle_id,v1.name vehicle_name, v1.customer_id as cutomer_id," +
						" vm1.vm1.actual_start, vm1.planned_end, vm1.actual_end, vm1.veh_maint_cause,vm1.other_cost," +
						" vm1.other_mat_cost,vm1.veh_maint_detail,vm1.veh_maint_total_cost,vm1.metric_one,vm1.metric_two,vm1.engine_hr,vm1.odometer   " +
						" from (select * from vehicle_maint where vehicle_maint.status = ?) as vm1 left join vehicle as v1 on vm1.vehicle_id = v1.id ";
				FETCH_MAINTENANCE_VEHICLES= " select vm1.id ticket_id,vm1.status as status, vm1.name ticket_name, vm1.vehicle_id,v1.name vehicle_name," +
						" v1.customer_id as cutomer_id, vm1.planned_start,vm1.actual_start, vm1.planned_end, vm1.actual_end, vm1.veh_maint_cause," +
						" vm1.other_cost,vm1.veh_maint_detail,vm1.other_mat_cost,vm1.veh_maint_total_cost,vm1.metric_one,vm1.metric_two,vm1.engine_hr,vm1.odometer" +
						" from (select * from vehicle_maint where vehicle_maint.status = ?) as vm1 left join vehicle as v1 on vm1.vehicle_id = v1.id where customer_id = ?";
				FETCH_MAINTENANCE_VEHICLES_BYID =" select vm1.id ticket_id, vm1.name ticket_name, vm1.status as status, vm1.vehicle_id,v1.name vehicle_name, v1.customer_id as cutomer_id, " +
						" vm1.planned_start,vm1.actual_start, vm1.planned_end, vm1.actual_end,vm1.veh_maint_detail, vm1.veh_maint_cause,vm1.other_cost,vm1.other_mat_cost, vm1.veh_maint_total_cost,vm1.metric_one,vm1.metric_two," +
						" vm1.engine_hr,vm1.odometer from (select * from vehicle_maint ) as vm1 left join vehicle as v1 on vm1.vehicle_id = v1.id where vm1.id = ? ";
				DELETE_MAINTENANCE_VEHICLES=" update vehicle_maint set status = ? where id = ?";
				INSERT_MAINTENANCE_VEHICLES=" insert into vehicle_maint"
						+ " (vehicle_id,actual_start,planned_start,planned_end,actual_end,veh_maint_cause,name,odometer,engine_hr,metric_one,metric_two,other_cost,other_mat_cost,veh_maint_total_cost,veh_maint_detail,status,created_on )"
						+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				INSERT_MAINT_SERVICE_DETAILS = " insert into vehicle_maint_services(ticket_id, service_item_id,service_type, service_cost) " +
						" values (?, ?, ?, ?) ";
				INSERT_MAINT_PARTS_DETAILS = " insert into vehicle_maint_part_replacement (ticket_id, work_type, part_sr_no, part_cat, life_remaining, life_type, notes, part_code, part_life, cost) " +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				UPDATE_MAINTENANCE_VEHICLES = " update vehicle_maint set planned_start=?, actual_start = ?,planned_end = ?,actual_end = ?, " +
						" veh_maint_cause = ?, name = ?,odometer = ?,engine_hr = ?, metric_one = ?, metric_two = ?, other_cost = ?, other_mat_cost = ? ,veh_maint_total_cost=? ,veh_maint_detail=?, status = ?, updated_on = ?   where id=?";
				DELETE_MAINT_PARTS_DETAILS = " delete from vehicle_maint_part_replacement where ticket_id = ? ";
				DELETE_MAINT_SERVICE_DETAILS = " delete from vehicle_maint_services where ticket_id = ? ";
				FETCH_MAINT_SERVICE_DETAILS = " select service_item_id , service_item.name service_name, service_cost,vehicle_maint_services.service_type service_type from vehicle_maint_services left outer join service_item on (service_item.id=vehicle_maint_services.service_item_id) where ticket_id = ?";
				FETCH_MAINT_PARTS_DETAILS = " select vehicle_maint_part_replacement.work_type,vehicle_maint_part_replacement.part_code , vehicle_maint_part_replacement.part_sr_no, vehicle_maint_part_replacement.part_cat, vehicle_maint_part_replacement.life_remaining, " +
						" vehicle_maint_part_replacement.part_life, vehicle_maint_part_replacement.life_type,old_part_log.part_status old_part_status, vehicle_maint_part_replacement.notes notes, vehicle_maint_part_replacement.cost part_cost, vehicle_maint_part_replacement.part_name,old_part_log.part_sr_no old_part_sr_no," +
						" old_part_log.life_remaining old_part_life_remaining,old_part_log.life_type old_part_life_type,old_part_log.part_life old_part_life from vehicle_maint_part_replacement join old_part_log on (old_part_log.part_replacement_id = vehicle_maint_part_replacement.id) where vehicle_maint_part_replacement.ticket_id = ? ";				
				INSERT_SERVICE_TYPE = " insert into service_item (type, status, port_node_id , std_cost, name, notes, is_mandatory) values (?, ?, ?, ?, ?, ?,?) ";
				UPDATE_SERVICE_TYPE = " update service_item set type = ?, status = ?, port_node_id = ?, std_cost = ?, name = ?, notes = ?, is_mandatory=? where id = ? ";
				DELETE_SERVICE_TYPE = " update service_item set status = ? where id = ? ";
				INSERT_SERVICE_TYPE_DETAILS = " insert into service_item_rules(service_item_id,rule_id) values (?, ?) ";
				DELETE_SERVICE_TYPE_DETAILS = " delete from service_item_rules where service_item_id = ? ";
				FETCH_SERVICE_TYPE_BYID = " select 	id, name, std_cost, notes, port_node_id, type, status, is_mandatory from service_item where id=? and status=?";
				FETCH_SERVICE_ITEM_DETAIL = " select service_item_rules.rule_id,rules.name from service_item_rules join rules on (service_item_rules.rule_id=rules.id) where service_item_rules.service_item_id = ?";
				FETCH_SERVICE_TYPE_BY_PORT = " select 	service_item.id, service_item.name, service_item.std_cost, service_item.notes, service_item.port_node_id, service_item.type, service_item.status, service_item.is_mandatory from service_item join port_nodes leaf on (leaf.id = service_item.port_node_id ) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id in (?))  where  service_item.status=?";
				FETCH_SERVICE_RULES_FOR_PORT = " select rules.id , rules.name from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join rules on (rules.port_node_id=anc.id) where  rules.status=?";
				//INSERT_RECURRING_SERVICES = " insert into veh_recurring_service(vehicle_id, service_item_id, odometer, odometer_threshold, engine_hr, engine_hr_threshold, months_date, months_threshold, metric_one,  metric_one_threshold, metric_two, metric_two_threshold, prev_service_date, next_service_date, completion_threshold, frequency_threshold, status, created_date, updated_on) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				INSERT_RECURRING_SERVICES = "insert into veh_recurring_service (vehicle_id, service_item_id, odometer, odometer_threshold,odometer_recurring_threshold,engine_hr,engine_hr_threshold,engine_hr_recurring_threshold,months_date,months_threshold,months_recurring_threshold,metric_one,metric_one_threshold,metric_one_recurring_threshold,metric_two,metric_two_threshold,metric_two_recurring_threshold,completion_threshold,frequency_threshold,updated_on) values (?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ? , ?, ?, ?) ";
				//	UPDATE_RECURRING_SERVICES = " update veh_recurring_service set service_item_id=? ,service_item_name=?,stop_odometer=?,stop_engine_hr=?,month_metric=?,metric_one=?,metric_two=?,service_date=?,pre_reminder_metric=?,post_reminder_metric=?,updated_on=? where id = ? ";
				//UPDATE_RECURRING_SERVICES = " update veh_recurring_service set service_item_id=?, odometer=?, odometer_threshold=?, engine_hr=?, engine_hr_threshold=?, months_date=?, months_threshold=?, metric_one=?,  metric_one_threshold=?, metric_two=?, metric_two_threshold=?, prev_service_date=?, next_service_date=?, completion_threshold=?, frequency_threshold=? , next_calc_date=?, updated_on=? where id = ?";
				UPDATE_RECURRING_SERVICES = " update veh_recurring_service set service_item_id = ?, odometer = ?, odometer_threshold = ?,odometer_recurring_threshold = ?,engine_hr = ?,engine_hr_threshold = ?,engine_hr_recurring_threshold = ?,months_date = ?,months_threshold = ?,months_recurring_threshold = ?,metric_one = ?,metric_one_threshold = ?,metric_one_recurring_threshold = ?,metric_two = ?,metric_two_threshold = ?,metric_two_recurring_threshold = ?,completion_threshold = ?,frequency_threshold = ?, updated_on=? where id = ?";
				INSERT_RECURRING_CONTACTS = " insert into veh_recurring_users (veh_recurring_service_id,pre_reminder_user_id) values (?,?)";
				DELETE_RECURRING_CONTACTS = " delete from veh_recurring_users where veh_recurring_service_id = ?";
				DELETE_RECURRING_SERVICES = " update veh_recurring_service set status=? where id=?";
				FETCH_CURRENT_ODOMETER_BY_VEHICLE_ID = " select attribute_value from logged_data  where attribute_id=0 and gps_record_time <= ? and vehicle_id = ? order by gps_record_time desc limit 1";
				FETCH_CURRENT_ENGINE_HR_BY_VEHICLE_ID = " select attribute_value from logged_data  where attribute_id=2 and gps_record_time <= ? and vehicle_id = ? order by gps_record_time desc limit 1";
				FETCH_RECURRING_REMINDER_LIST = "select veh_recurring_service.id recurring_id,vehicle.id vehicle_id,vehicle.name vehicle_name,vehicle.type vehicle_type,service_item.name service_item_name, service_item.id service_item_id,veh_recurring_service.status reminder_status,vrs.next_service_date , veh_recurring_service.odometer, veh_recurring_service.engine_hr, (case when vm.actual_end is not null then vm.actual_end when vm.planned_end is not null then planned_end when vm.actual_start is not null then actual_start else planned_start end) completed_date,vm.id ticket_id from veh_recurring_service join (vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))))  on vehicle.id = veh_recurring_service.vehicle_id  left outer join service_item on (service_item.id=veh_recurring_service.service_item_id) left outer join veh_recurring_service_alert_log vrs on (vrs.veh_recurring_service_id =veh_recurring_service.id and vrs.vehicle_id=veh_recurring_service.vehicle_id and vrs.status not in (0,4)) left outer join vehicle_maint vm on (vrs.ticket_id=vm.id)";
				UPDATE_REMINDER_STATUS = " update veh_recurring_service set status=?,updated_on=? where id = ? ";
				FETCH_RECURRING_SERVICES = "";
				DELETE_OLD_PART_LOG = " delete from old_part_log where ticket_id=? ";
				INSERT_OLD_PART_LOG = " insert into old_part_log(ticket_id,part_replacement_id,part_sr_no,life_remaining,life_type,part_life,part_status) values (?, ?, ?, ?, ?, ?, ?)";
				CLOSE_ALL_PENDING_SERVICES = "update veh_recurring_service_alert_log set status=? ,set ticket_id=? where vehicle_id=? ";
				FETCH_ALL_PENDING_SERVICES_FOR_VEHICLE = "select pending_service.id , pending_service.service_name, pending_service.std_cost,pending_service.service_type " +
						" from ( select service_item.id , service_item.name service_name, service_item.std_cost,1 service_type  from veh_recurring_service_alert_log join " +
						" veh_recurring_service on (veh_recurring_service.id = veh_recurring_service_alert_log.veh_recurring_service_id) join service_item on (service_item.id = veh_recurring_service.service_item_id) " +
						" where veh_recurring_service.vehicle_id=? and veh_recurring_service_alert_log.status not in (0,4) union select service_item.id , service_item.name service_name, service_item.std_cost,1 service_type " +
						" from  veh_rec_frequency join veh_recurring_service on (veh_recurring_service.id = veh_rec_frequency.veh_recurring_service_id) join service_item on (service_item.id = veh_recurring_service.service_item_id) where veh_recurring_service.vehicle_id=?  " +
						" and (? is null or veh_rec_frequency.odometer_reading <= ?) and  (? is null or veh_rec_frequency.engine_hr_reading <=?) and (? is null or veh_rec_frequency.metric_one_reading <= ?) and (? is null or veh_rec_frequency.metric_two_reading <= ?) " +
						" union select service_item.id , service_item.name service_name, service_item.std_cost,3 service_type from service_item  where is_mandatory=1 group by service_item.id)pending_service group by pending_service.id";
				FETCH_ALL_RECURRING_SERVICES_FOR_VEHICLE = "select 	si.id,si.name service_name,odometer_recurring_threshold odometerRecurring,engine_hr_recurring_threshold engineHrRecurring,months_recurring_threshold monthsRecurring,metric_one_recurring_threshold metricOneRecurring,metric_two_recurring_threshold metricTwoRecurring from veh_recurring_service vrs join service_item si on (si.id=vrs.service_item_id) where vehicle_id=?";
			}
		}
	}
	public static class JOURNEYBRIEFING{
		public static final String INSERT_JOURNEY_BRIEFING;
		public static final String INSERT_VEHICLE_CHECKLIST;
		public static final String INSERT_DRIVER_CHECKLIST;
		
		static{
			if(isMySql){
				INSERT_JOURNEY_BRIEFING=" insert into journey_briefing"
						+ " (vehicle_id, driver_id, plant_id, vehicle_check_by, driver_check_by, vehicle_deny, driver_deny, notes, blocking_dur, briefing_date, updated_by)"
						+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				INSERT_VEHICLE_CHECKLIST = " insert into vehicle_briefing_params(journey_briefing_id, param_id, param_val, notes) " +
						" values (?, ?, ?, ?) ";
				INSERT_DRIVER_CHECKLIST = " insert into driver_briefing_params(journey_briefing_id, param_id, param_val, notes) " +
						" values (?, ?, ?, ?) ";
			}
		}
	}
	public static class TripSetup {
		public static final String FETCH_GATE_AND_WAIT_REGION;
		public static final String FETCH_REGIONS;
		public static final String FETCH_REGIONS_BY_NAME;
		public static final String INSERT_OP_STATION;
		public static final String UPDATE_OP_STATION;
		public static final String DELETE_OP_STATION_OPERS;
		public static final String DELETE_OP_STATION_MAPPING;
		public static final String INSERT_OP_STATION_OPERS;
		public static final String INSERT_OP_STATION_MAPPING;
		public static final String SELECT_TRIP_SETUP_INFO;
		public static final String SELECT_OPRATED_AREA;
		public static final String SELECT_TRIP_SETUP_INFO_OPAREAS;
		public static final String SELECT_TRIP_SETUP_INFO_ITEM;
		public static final String SELECT_TRIP_SETUP_INFO_OPAREAS_ITEM;
		public static final String FETCH_LINKABLE_VEHICLES;
		public static final String IS_ARTIFICIAL_REGION;
		public static final String CLONE_REGION;
		public static final String INSERT_ART_REGION;
		public static final String FETCH_MATERIAL;
		public static final String GET_OPSTATION_FOR_ORG;
		public static final String GET_BINLIST_FOR_ORG;
		public static final String GET_REGIONS_FOR_ORG;
		public static final String GET_ROADSEGMENT_FOR_ORG;
		static {
			if (isMySql) {
				GET_OPSTATION_FOR_ORG = "select op_station.id, op_station.name from op_station join opstation_mapping on (op_station.id = opstation_mapping.op_station_id and type = ?) join port_nodes leaf on (leaf.id = opstation_mapping.port_node_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by op_station.name ";
				GET_BINLIST_FOR_ORG = "select swm_bins.id, swm_bins.name from swm_bins join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where swm_bins.status in (1) order by swm_bins.name ";
				GET_ROADSEGMENT_FOR_ORG  = "select new_road_segments.id, new_road_segments.name from new_road_segments join port_nodes leaf on (leaf.id = new_road_segments.port_node_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by new_road_segments.name ";
				GET_REGIONS_FOR_ORG  = "select regions.id, regions.short_code from regions join port_nodes leaf on (leaf.id = regions.port_node_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where regions.status in (1) and regions.is_artificial=0 and (regions.flag is null or regions.flag=1) order by regions.short_code ";
				IS_ARTIFICIAL_REGION = "select 1 from regions where regions.id = ? and is_artificial = 1";
				CLONE_REGION = "insert into regions (short_code, shape, port_node_id, description, lowerX, lowerY, upperX, upperY, equal_to_MBR, status, is_artificial) "+
				                               " (select short_code, shape, port_node_id, description, lowerX, lowerY, upperX, upperY, equal_to_MBR, status, 1 from regions where id = ?) ";
				INSERT_ART_REGION = "insert into regions (short_code, shape, port_node_id, lowerX, lowerY, upperX, upperY, equal_to_MBR, status, is_artificial) "+
	            " values (?,geomfromtext(?),?,?,?,?,?,?,?,1)";
				
				FETCH_GATE_AND_WAIT_REGION = "select os.wait_reg_id , os.gate_reg_id from op_station os , opstation_mapping om where os.id = "
						+ "om.op_station_id and port_node_id in (select id from port_nodes where lhs_number >="
						+ "(select lhs_number from port_nodes where id = ?)and rhs_number <= (select rhs_number from port_nodes where id = ?))";
				//FETCH_REGIONS = "select r.id, r.short_code from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) join regions r on (r.port_node_id = leaf.id) "+
				//                               " where r.status = 1 ";
				FETCH_REGIONS = "select r.id, r.short_code from port_nodes anc join port_nodes leaf on (((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number)) and anc.id = ?) join regions r on (r.port_node_id = leaf.id) "+
                " where r.status = 1 and (r.is_artificial is null or r.is_artificial = 0)";
				FETCH_REGIONS_BY_NAME = "select r.id, r.short_code from regions r  "+
                " where r.status = 1 and r.short_code like ?"; 
				FETCH_LINKABLE_VEHICLES = "select vehicle.id, vehicle.name from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) join vehicle on (vehicle.customer_id = leaf.id) where vehicle.status != 0 and vehicle.type in (";//remaining comes in called
				INSERT_OP_STATION = "insert into op_station (wait_reg_id,gate_reg_id,name, description, status, vehicle_id, int_val1, int_val2, int_val3, int_val4, int_val5,int_val6,int_val7,int_val8, int_val9, int_val10, int_val11, pick_last,hybrid_flip_only,nick_name, sub_type "+
				" ,alert_email_l1_customer,alert_email_l2_customer,alert_phone_l1_customer,alert_phone_l2_customer,alert_user_l1_customer,alert_user_l2_customer "+
				" ,alert_email_l1_transporter,alert_email_l2_transporter,alert_phone_l1_transporter,alert_phone_l2_transporter,alert_user_l1_transporter,alert_user_l2_transporter "+
				" ,alert_email_l1_sender,alert_email_l2_sender,alert_phone_l1_sender,alert_phone_l2_sender,alert_user_l1_sender,alert_user_l2_sender "+
				
					" ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?"+
					",?,?,?,?,?,?"+
					",?,?,?,?,?,?"+
					",?,?,?,?,?,?"+
					")";
				UPDATE_OP_STATION = "update op_station set wait_reg_id = ?, gate_reg_id = ?, name = ?, description = ?, status = ?, vehicle_id=?, int_val1=?, int_val2=?, int_val3=?, int_val4=?, int_val5=?, int_val6 = ?,int_val7=?, int_val8=?, int_val9=?, int_val10=?, int_val11=?, pick_last=?,hybrid_flip_only=?, nick_name=?, sub_type=? "+
				" ,alert_email_l1_customer=?,alert_email_l2_customer=?,alert_phone_l1_customer=?,alert_phone_l2_customer=?,alert_user_l1_customer=?,alert_user_l2_customer =?"+
				" ,alert_email_l1_transporter=?,alert_email_l2_transporter=?,alert_phone_l1_transporter=?,alert_phone_l2_transporter=?,alert_user_l1_transporter=?,alert_user_l2_transporter=? "+
				" ,alert_email_l1_sender=?,alert_email_l2_sender=?,alert_phone_l1_sender=?,alert_phone_l2_sender=?,alert_user_l1_sender=?,alert_user_l2_sender=? "+
				
				" where id = ?";
				INSERT_OP_STATION_OPERS = "insert into opstations_opareas (op_station_id,region_id, threshold,material,priority, oparea_type,start_date, end_date) values (?,?,?,?,?,?,?,?)";
				DELETE_OP_STATION_OPERS = "delete from opstations_opareas where op_station_id = ?";
				
				INSERT_OP_STATION_MAPPING = "insert into opstation_mapping (op_station_id,port_node_id,type)values(?,?,?)";
				SELECT_TRIP_SETUP_INFO = "SELECT op_station.id,op_station.name, op_station.wait_reg_id, op_station.gate_reg_id, regions.short_code'waitname',op_station.status , op_station.description," +
						" reg.short_code'gatename' , om.type FROM op_station JOIN regions regions ON regions.id = op_station.gate_reg_id  JOIN regions reg ON reg.id = op_station.wait_reg_id JOIN opstation_mapping om ON op_station.id = om.op_station_id WHERE op_station.id in " +
						" (select op_station_id from opstation_mapping where port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id = ?)" +
						" and rhs_number <=(select rhs_number from port_nodes where id = ?)))";
				SELECT_OPRATED_AREA = "select r.short_code from opstations_opareas o , regions r where o.region_id = r.id and   o.op_station_id =?";
				DELETE_OP_STATION_MAPPING = "delete from opstation_mapping where op_station_id = ?";
				
						//" where (status = ?) order by op_station.id desc" //added in later
						;
				SELECT_TRIP_SETUP_INFO_OPAREAS = "SELECT distinct opstations_opareas.op_station_id, regions.id, regions.short_code, opstations_opareas.threshold , opstations_opareas.material, opstations_opareas.priority, generic_params.name materialName, opstations_opareas.oparea_type, opstations_opareas.start_date, opstations_opareas.end_date FROM port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) "+
				" join opstation_mapping on (opstation_mapping.port_node_id = leaf.id) join op_station on (op_station.id = opstation_mapping.op_station_id) join opstations_opareas on (opstations_opareas.op_station_id = opstation_mapping.op_station_id) JOIN regions  ON regions.id = opstations_opareas.region_id "
				+" JOIN generic_params ON opstations_opareas.material = generic_params.id and generic_params.param_id = 20451 and generic_params.status = 1 "
				//				+" join material on opstations_opareas.material = material.id "
				//" where (status = ?) order by op_station.id desc" //added in later
				;
				SELECT_TRIP_SETUP_INFO_ITEM = "SELECT distinct op_station.id,op_station.name, op_station.wait_reg_id, op_station.gate_reg_id, regions.short_code waitname ," +
				" reg.short_code gatename , om.type, op_station.description, om.port_node_id, op_station.status, op_station.vehicle_id, vehicle.name vehicle_name, op_station.int_val1, op_station.int_val2, op_station.int_val3, op_station.int_val4, op_station.int_val5,op_station.int_val6, op_station.int_val7, op_station.int_val8, op_station.int_val9, op_station.int_val10, op_station.int_val11, op_station.pick_last,op_station.hybrid_flip_only,op_station.nick_name, op_station.sub_type "+
				" ,alert_email_l1_customer,alert_email_l2_customer,alert_phone_l1_customer,alert_phone_l2_customer,alert_user_l1_customer,alert_user_l2_customer "+
				" ,alert_email_l1_transporter,alert_email_l2_transporter,alert_phone_l1_transporter,alert_phone_l2_transporter,alert_user_l1_transporter,alert_user_l2_transporter "+
				" ,alert_email_l1_sender,alert_email_l2_sender,alert_phone_l1_sender,alert_phone_l2_sender,alert_user_l1_sender,alert_user_l2_sender "+
				" FROM  "+
				"  op_station join opstation_mapping om on (op_station.id = om.op_station_id) left outer JOIN regions regions ON regions.id = op_station.gate_reg_id  left outer JOIN regions reg ON reg.id = op_station.wait_reg_id left outer join vehicle on (vehicle.id = op_station.vehicle_id) "+
				" where op_station.id = ? "
				;
				SELECT_TRIP_SETUP_INFO_OPAREAS_ITEM = "SELECT distinct opstations_opareas.op_station_id, regions.id, regions.short_code, opstations_opareas.threshold ,opstations_opareas.material, generic_params.name materialName,opstations_opareas.priority, opstations_opareas.oparea_type, opstations_opareas.start_date, opstations_opareas.end_date FROM  "+
				" op_station join opstation_mapping on (op_station.id = opstation_mapping.op_station_id) join opstations_opareas on (opstations_opareas.op_station_id = opstation_mapping.op_station_id) left JOIN regions  ON regions.id = opstations_opareas.region_id "+
				" left JOIN generic_params ON opstations_opareas.material = generic_params.id and generic_params.param_id = 20451 and generic_params.status = 1 "+
				//				" join material on opstations_opareas.material = material.id  "+
				" where op_station.id = ? "
	 	        ;
				FETCH_MATERIAL = "select generic_params.id,generic_params.name from generic_params join port_nodes leaf on (leaf.id = generic_params.port_node_id) join port_nodes anc on (anc.id=? and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number))) where generic_params.status in (1) and generic_params.param_id=20451 order by generic_params.name";//"SELECT id, NAME FROM material ";
			}
		}
	}

	public static class DRIVERDETAILS{
		
		public static final String INSERT_DRIVER_DETAILS;
		public static final String UPDATE_DRIVER_DETAILS;
		public static final String DELETE_DRIVER_DETAILS;
		public static final String FETCH_DRIVER_DETAILS_BYID;
		public static final String FETCH_DRIVER_DETAILS_BYORG;
		public static final String FETCH_DRIVERS;
		public static final String FETCH_ALL_DRIVERS;
		public static final String INSERT_DRIVER_SKILLS;
		public static final String UPDATE_DRIVER_SKILLS;
		public static final String DELETE_DRIVER_SKILLS;
		public static final String FETCH_DRIVER_SKILLS;
		public static final String FETCH_VEHICLE_DETAILS;
		
		static{
			FETCH_VEHICLE_DETAILS = "select vehicle.id, vehicle.name, vehicle.status, vehicle.detailed_status, vehicle.type, vehicle.customer_id from vehicle join port_nodes leaf on (vehicle.customer_id = leaf.id) join "+
			                                              " port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where vehicle.status <> 0 order by vehicle.detailed_status, vehicle.status, vehicle.name "
															;
			INSERT_DRIVER_DETAILS = "insert into driver_details"
			+ " (org_id,driver_uid,driver_name,driver_dl_number,driver_mobile_one,driver_mobile_two,driver_address_one,driver_address_two,driver_insurance_one,driver_insurance_two,insurance_one_date,insurance_two_date,updated_on, dl_date, info1, info2, info3, info4, vehicle_id_1, vehicle_id_2) "
			+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
			UPDATE_DRIVER_DETAILS = "update driver_details"
			+ " set org_id=?,driver_uid=?,driver_name=?,driver_dl_number=?,driver_mobile_one=?,driver_mobile_two=?,driver_address_one=?,driver_address_two=?,driver_insurance_one=?,driver_insurance_two=?,insurance_one_date=?,insurance_two_date=?,"
			+ " updated_on=?, dl_date=?, info1=?, info2=?, info3=?, info4=?, vehicle_id_1=?, vehicle_id_2=? where id=?";
			DELETE_DRIVER_DETAILS = "update driver_details set status = ? where id =?";
			INSERT_DRIVER_SKILLS  = "insert into driver_skills (driver_id,skill_type,skill_level,skill_name, factor1, factor2) values ( ?,?,?,?,?,?)";
			UPDATE_DRIVER_SKILLS  = "update driver_skills set skill_type=?,skill_level=?,skill_name=?, factor1=?, factor2=? where driver_id = ?";
			DELETE_DRIVER_SKILLS  = "delete from driver_skills where driver_id =?";
			FETCH_DRIVER_DETAILS_BYID = "select driver_name,org_id,driver_uid,driver_dl_number,driver_mobile_one,driver_mobile_two,insurance_one_date,insurance_two_date,driver_address_one,driver_address_two,driver_insurance_one,driver_insurance_two, dl_date, info1, info2, info3, info4, vehicle_id_1, veh1.name veh1_name, vehicle_id_2, veh2.name veh2_name  from driver_details "+
			                                                       " left outer join vehicle veh1 on (veh1.id = vehicle_id_1) left outer join vehicle veh2 on (veh2.id = vehicle_id_2) "+
			                                                       " where driver_details.status = ? and driver_details.id = ?";
			FETCH_DRIVER_SKILLS = "select skill_type,skill_level,skill_name, factor1, factor2 from driver_skills where driver_id =?";
			FETCH_DRIVERS = "select driver_name,id,driver_uid,driver_dl_number,driver_mobile_one,driver_mobile_two,insurance_one_date,insurance_two_date,driver_address_one,driver_address_two,driver_insurance_one,driver_insurance_two, dl_date, info1, info2, info3, info4 from driver_details where status = ? and org_id = ?";
			FETCH_ALL_DRIVERS = "select driver_name,id,driver_uid,driver_dl_number,driver_mobile_one,driver_mobile_two,insurance_one_date,insurance_two_date,driver_address_one,driver_address_two,driver_insurance_one,driver_insurance_two, dl_date, info1, info2, info3, info4, org_id from driver_details where status = ? ";
			FETCH_DRIVER_DETAILS_BYORG = "select dd.id as id, driver_name, driver_uid,driver_dl_number,driver_mobile_one,driver_mobile_two,insurance_one_date,insurance_two_date,driver_address_one,driver_address_two,driver_insurance_one,driver_insurance_two, dl_date, info1, info2, info3, info4, org_id, vehicle_id_1, veh1.name veh1_name, vehicle_id_2, veh2.name veh2_name  from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
				                                          " join driver_details dd on (dd.org_id = leaf.id) "+
				                                          " left outer join vehicle veh1 on (veh1.id = vehicle_id_1) left outer join vehicle veh2 on (veh2.id = vehicle_id_2) "+
				                                          " where dd.status = ? order by dd.driver_name ";
		}
	}
	
	public static class SHIFT{
		public static final String INSERT_SHIFT_HEADER;
		public static final String UPDATE_SHIFT_HEADER;
		public static final String DELETE_SHIFT_HEADER ;
		public static final String MARK_SHIFT_HEADER;
		public static final String INSERT_SHIFT_TIMINGS;		
		public static final String DELETE_SHIFT_TIMINGS ;
		
		public static final String FETCH_SHIFT_SCHEDULE ;
		public static final String INSERT_SHIFT_DAYS;
		public static final String DELETE_SHIFT_DAYS;
		public static final String FETCH_SHIFT_DAYS ;
		public static final String INSERT_SHIFT_BEGIN_DAY;
		public static final String DELETE_SHIFT_BEGIN_DAY;
		public static final String GET_SHIFT_BEGIN_DAY;
		static {
			if ( isMySql){
				INSERT_SHIFT_BEGIN_DAY = "insert into org_week_begin_day (port_node_id, day) values (?,?)";
				GET_SHIFT_BEGIN_DAY = "select day from org_week_begin_day where port_node_id=?  ";
				DELETE_SHIFT_BEGIN_DAY  = "delete  from org_week_begin_day where port_node_id = ? ";
				INSERT_SHIFT_HEADER = "insert into shift(name,port_node_id,updated_on, temp) " +
						" values(?,?,?,1)";
				UPDATE_SHIFT_HEADER = "update shift set name=?, port_node_id=?,updated_on=?,temp=1 where id=?";
				MARK_SHIFT_HEADER = "update shift set temp=0 where port_node_id = ?";
				DELETE_SHIFT_HEADER = "delete from shift where port_node_id = ? and temp = 0";
				
				INSERT_SHIFT_TIMINGS = "insert into shift_timings (shift_id, valid_start, valid_end, start_hour, start_min,stop_hour, stop_min) " +
				" values(?,?,?,?,?,?,?)";
		        
		        DELETE_SHIFT_TIMINGS = "delete from shift_timings using shift_timings join shift on (shift.id = shift_timings.shift_id) where shift.port_node_id=?";
		
				FETCH_SHIFT_SCHEDULE = "select shift_type,name,port_node_id,start_hour,start_min,stop_hour,stop_min, id, valid_start, valid_end from shift join shift_timings on (shift.id = shift_timings.shift_id) where port_node_id = ? order by valid_start, start_hour ";
				
				INSERT_SHIFT_DAYS = "insert into shift_days(port_node_id,type_of_day,day) values(?,?,?)";
				DELETE_SHIFT_DAYS = "delete from shift_days where port_node_id = ?";
				FETCH_SHIFT_DAYS = "select type_of_day,day from shift_days where port_node_id = ?";
			}
		} 
	}
	
	public static class GENERIC_PARAMS {
		public static final String FETCH_PARAMS_ATTACHED_TO_FIRST_ANC;
		public static final String DELETE_PARAMS;
		public static final String INSERT_PARAMS;
		public static final String UPDATE_PARAMS;
		public static final String FETCH_DIRECT_ATTACHED_PARAMS;
		public static final String FETCH_PARAMS_ANC;
		public static final String FETCH_PARAMS_DESC;
		
		static{
			if( isMySql ){
		        FETCH_DIRECT_ATTACHED_PARAMS = "select generic_params.id, generic_params.name from generic_params where generic_params.port_node_id = ? and generic_params.param_id = ? and generic_params.status != 0 order by generic_params.name ";
				FETCH_PARAMS_ATTACHED_TO_FIRST_ANC = //will get the shift at the nearest anc that has shift - this has dependency on mysql (see the limit 1) 					
		        " select generic_params.id,generic_params.name from generic_params join "+
				" (select anc.id pnid from port_nodes leaf join port_nodes anc on  (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
				" where exists (select 1 from generic_params where generic_params.port_node_id = anc.id and generic_params.param_id=?) order by anc.lhs_number desc limit 1) portWithShift "+
				" on (portWithShift.pnid = generic_params.port_node_id) where generic_params.param_id = ? order by generic_params.name ";
				
				FETCH_PARAMS_ANC = //will get the shift at the nearest anc that has shift - this has dependency on mysql (see the limit 1) 					
			        " select generic_params.id,generic_params.name from port_nodes leaf join port_nodes anc on  (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
			        " join generic_params on (generic_params.port_node_id = anc.id) where generic_params.status != 0 and generic_params.param_id = ? order by generic_params.name";

				FETCH_PARAMS_DESC = //will get the shift at the nearest anc that has shift - this has dependency on mysql (see the limit 1) 					
			        " select generic_params.id,generic_params.name from port_nodes anc join port_nodes leaf on  (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
			        " join generic_params on (generic_params.port_node_id = leaf.id) where generic_params.status != 0 and generic_params.param_id = ? order by generic_params.name";

				
				DELETE_PARAMS = "update generic_params set status = 0 where id = ?";
				INSERT_PARAMS = "insert into generic_params (param_id, name, port_node_id, status) values (?,?,?,1)";
				UPDATE_PARAMS = "update generic_params set name = ? where id = ?";
			}
		}	
	}
	
	public static class SHIFT_SCHEDULE{
		public static final String FETCH_SHIFT ;
		public static final String FETCH_SHIFT_WITH_VALID ;
		public static final String FETCH_SCHDEULE_INFO ;
		public static final String FETCH_SCHDEULE ;
		public static final String INSERT_SCHEDULE_INFO ;
		public static final String FETCH_SHIFT_TIMINGS ;
		public static final String INSERT_SCHEDULE ;
		public static final String UPDATE_SCHEDULE_INFO ;
		public static final String DELETE_SCHEDULE;
		public static final String FETCH_LIST ;
		public static final String DELETE_SCHEDULE_INFO ;
		public static final String COPY_SCHCEDULE_INFO ;
		public static final String COPY_SCHCEDULE ;
		public static final String FETCH_NEWLY_ADDED = null;
		public static final String FETCH_OPSTATIONS;
		public static final String FETCH_AVAIL_CONTROL_VEHICLE;
		public static final String FETCH_DRIVER_ASSIGNMENT;
		public static final String DELETE_DRIVER_ASSIGNMENT;
		public static final String ADD_DRIVER_ASSIGNMENT;
		public static final String COPY_DRIVER_ASSIGNMENT;
		public static final String FETCH_INV_PILE;
		static{
			if( isMySql ){
				
				FETCH_INV_PILE = "select inventory_piles.id, inventory_piles.short_code from inventory_piles join port_nodes anc on (anc.id = inventory_piles.port_node_id) join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where inventory_piles.pile_type=? and inventory_piles.status=1";
				COPY_DRIVER_ASSIGNMENT = "insert into driver_assignments (shift_schedule_info_id,vehicle_id, driver_id, factor1, factor2) " +
				" select ? as shift_schedule_info_id,vehicle_id, driver_id, factor1, factor2 " +
				" from driver_assignments where  shift_schedule_info_id = ?";
				FETCH_DRIVER_ASSIGNMENT = " select vehicle_id, driver_id, driver_shift_start_hour, driver_shift_start_min, driver_shift_stop_hour, driver_shift_stop_min, driver_notes, factor1, factor2, vehicle.name vehicle_name, driver_details.driver_name driver_name, src_id, src_type, dest_id, dest_type from driver_assignments "+
				" left outer join vehicle on (vehicle.id = vehicle_id) left outer join driver_details on (driver_details.id = driver_id) "+
				" where shift_schedule_info_id = ? order by factor1, factor2, driver_id, vehicle_id";
				DELETE_DRIVER_ASSIGNMENT = " delete from driver_assignments where shift_schedule_info_id = ?";
				ADD_DRIVER_ASSIGNMENT = "insert into driver_assignments(shift_schedule_info_id, vehicle_id, driver_id, driver_shift_start_hour, driver_shift_start_min, driver_shift_stop_hour,driver_shift_stop_min,driver_notes, factor1, factor2,src_id,src_type,dest_id,dest_type ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				
				FETCH_OPSTATIONS = " select op_station.id, op_station.name from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) join opstation_mapping on (opstation_mapping.port_node_id = leaf.id and opstation_mapping.type = ?) join op_station on (opstation_mapping.op_station_id = op_station.id) where op_station.status != 0 order by op_station.name";
				FETCH_SHIFT = //will get the shift at the nearest anc that has shift - this has dependency on mysql (see the limit 1) 					
			        " select shift.id,shift.name from shift join "+
					" (select anc.id pnid from port_nodes leaf join port_nodes anc on  (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
					" where exists(select 1 from shift where shift.port_node_id = anc.id) order by anc.lhs_number desc limit 1) portWithShift "+
					" on (portWithShift.pnid = shift.port_node_id) order by shift.name ";
				FETCH_SHIFT_WITH_VALID = //will get the shift at the nearest anc that has shift - this has dependency on mysql (see the limit 1) 					
					" select shift.id, concat(shift.name, "+ 
	                "           (case when (min(valid_start) is null or max(valid_end) is null or datediff(valid_end, valid_start) = 365) "+ 
	                "           then '' else "+ 
	                "           concat(' (Valid from ', date_format(min(valid_start),'%d %M'), ' to ', date_format(max(valid_end),'%d %M'), ')') "+ 
	                "           end) "+
	                "         ) from shift join "+ 
					"	 (select anc.id pnid from port_nodes leaf join port_nodes anc on  (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
					"	 where exists(select 1 from shift where shift.port_node_id = anc.id) order by anc.lhs_number desc limit 1) portWithShift  "+
					"	 on (portWithShift.pnid = shift.port_node_id) join shift_timings on (shift.id = shift_timings.shift_id) group by shift.name, shift.id order by shift.name, shift.id "; 
				
				FETCH_AVAIL_CONTROL_VEHICLE = " select vehicle.id, vehicle.name from port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
				                                          " join vehicle on (vehicle.customer_id = leaf.id) where vehicle.status != 0 order by vehicle.name ";
				
				
				FETCH_SCHDEULE_INFO = "select day,shift_id,port_node_id,start_hour,start_min,duration from shift_schedule_info where id = ?";
				FETCH_SCHDEULE = " select source,destination,vehicle_type,number_of_trips," +
						" number_of_vehicles,start_Hour,start_min," +
						" stop_hour,stop_min, source_loc_id, dest_loc_id,lead from shift_schedule where shift_schedule_info_id = ?";
				
				INSERT_SCHEDULE_INFO = " insert into shift_schedule_info(duration,day,shift_id," +
						" port_node_id,start_hour,start_min, " +
						" stop_hour,stop_min,status,updated_on,lead) " +
						" values (?, ?,?,? ,?,?,?, ?,?,?,? ) ";
				FETCH_SHIFT_TIMINGS = " select start_hour,start_min,stop_hour,stop_min " +
						" from shift join shift_timings on (shift.id = shift_timings.shift_id) where id = ? order by (case when (valid_start is null or valid_end is null or (? >= valid_start and ? < valid_end) or (? >= valid_start and ? < valid_end)) then 1 else 0 end) desc limit 1";
				
				INSERT_SCHEDULE = " insert into shift_schedule(shift_schedule_info_id,source,destination,vehicle_type,number_of_trips,number_of_vehicles,start_hour,start_min,stop_hour,stop_min,updated_on,source_loc_id, dest_loc_id, lead) " +
						" values(?,?,?, ?,?,? ,?,?,? ,?,?,?,?,?)  ";
				
				UPDATE_SCHEDULE_INFO = " update shift_schedule_info set duration = ?, day = ?, shift_id = ?, port_node_id = ?, " +
						" start_hour = ?, start_min = ?, stop_hour = ?, stop_min = ?, status = ?, updated_on = ? where id = ?  ";
				
				DELETE_SCHEDULE = " delete from  shift_schedule where shift_schedule_info_id = ?";
				FETCH_LIST  = "select shift_schedule_info.id,shift_schedule_info.day,shift_schedule_info.shift_id, shift_schedule_info.port_node_id, shift.name shift_name, shift_schedule_info.start_hour schedh, shift_schedule_info.start_min schedm, shift_timings.start_hour shifth, shift_timings.start_min shiftm from port_nodes anc join port_nodes leaf on (anc. id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join shift_schedule_info on (shift_schedule_info.port_node_id = leaf.id) join shift on (shift.id = shift_schedule_info.shift_id) join shift_timings on (shift.id = shift_timings.shift_id) where shift_schedule_info.day >= ? and shift_schedule_info.day <= ? and shift_schedule_info.status  != ?";
				DELETE_SCHEDULE_INFO = " update shift_schedule_info set status = ? where id = ? ";
				COPY_SCHCEDULE_INFO = "insert into shift_schedule_info( day,shift_id,port_node_id,start_hour,start_min,stop_hour,stop_min,duration,status,updated_on) " +
						" select ? as day,shift_id,port_node_id,start_hour,start_min,stop_hour,stop_min,duration,status,? as updated_on from shift_schedule_info where  id = ?";
				COPY_SCHCEDULE = "insert into shift_schedule (shift_schedule_info_id,source,destination,vehicle_type,number_of_trips,number_of_vehicles,start_hour,start_min,stop_hour,stop_min,updated_on, source_loc_id, dest_loc_id) " +
								" select ? as shift_schedule_info_id,source,destination,vehicle_type,number_of_trips,number_of_vehicles,start_hour,start_min,stop_hour,stop_min,? as updated_on, source_loc_id, dest_loc_id " +
								" from shift_schedule where  shift_schedule_info_id = ?";
			}
		}
	}
	
	public static class AlertContect {
		public static final String FETCH_VEHICLE;
		public static final String FETCH_ALERT_CONTECT_DETAIL;
		static {
			if (isMySql) {
				FETCH_VEHICLE = "select vehicle.id , vehicle.name from vehicle vehicle , vehicle_access_groups vag where vehicle.id = vag.vehicle_id and vag.port_node_id in (select id from port_nodes where lhs_number >="
						+ "(select lhs_number from port_nodes where id = ?)and rhs_number <= (select rhs_number from port_nodes where id = ?))";
				FETCH_ALERT_CONTECT_DETAIL = "select vehicle_id, name , mobile , mobile1,language from aleart_contectinfo where vehicle_id = ?";
			}
		}
	}

	public static class Feedback {
		public static final String INSERT_FEEDBACK;
		public static final String FETCH_FEEDBACK ;
		static{
			if(isMySql){
				INSERT_FEEDBACK = "insert into feedback(customer_id,feedback,updated_on) values(?,?,?)";
				FETCH_FEEDBACK = "select users.ID,NAME,feedback from feedback left join users on users.id = feedback.customer_id";
			}
		}
	}
	
	public static class DASHBOARD {
		public static final String FETCH_SHIFT_TRIP_INFO;
		public static final String FETCH_SHIFT_SCHEDULE;
		public static final String FETCH_SHIFT_ENGINE_EVENTS;
		public static final String FETCH_CURRENT_DATA;
		public static final String FETCH_TRIP_INFO;
		public static final String FETCH_TRIP_INFO_VEH;
		
		static{
			if(isMySql){
				
				
				FETCH_SHIFT_TRIP_INFO = "SELECT op_station20006.id,op_station20006.wait_reg_id, regions20007.short_code,  COUNT(*), COUNT(DISTINCT(trip_info.vehicle_id)), avg(timestampdiff(minute, trip_info.combo_start, trip_info.combo_end)) av" +
						" FROM "+
				" vehicle join (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "+			
				" ) vi on (vi.vehicle_id = vehicle.id) "+
				"join trip_info on (trip_info.vehicle_id = vehicle.id) "+ 
					" LEFT OUTER JOIN regions regions20006 ON (regions20006.id = (CASE WHEN load_area_manual IS NULL THEN load_area_guess ELSE load_area_manual END)) "+
					" LEFT OUTER JOIN op_station op_station20006 ON (op_station20006.id = load_gate_op) "+ 
					" LEFT OUTER JOIN regions regions20007 ON op_station20006.wait_reg_id = regions20007.id "+
					" WHERE shift_date >= ? GROUP BY op_station20006.id";
				FETCH_SHIFT_SCHEDULE = "Select source, number_of_trips, number_of_vehicles, source_loc_id from shift_schedule where shift_schedule_info_id = ?  group by source";
				FETCH_SHIFT_ENGINE_EVENTS = "SELECT vehicle.id, vehicle.name , event_start_time, event_stop_time FROM engine_events  " + 
					" JOIN vehicle ON engine_events.vehicle_id = vehicle.id " + 
					" JOIN (SELECT DISTINCT(vehicle.id) vehicle_id FROM vehicle LEFT OUTER JOIN port_nodes custleaf ON (custleaf.id = vehicle.customer_id) LEFT OUTER JOIN vehicle_access_groups ON (vehicle_access_groups.vehicle_id = vehicle.id) LEFT OUTER JOIN port_nodes leaf ON (leaf.id = vehicle_access_groups.port_node_id) JOIN port_nodes anc  ON (anc.id IN (?) AND ((anc.lhs_number <= leaf.lhs_number AND anc.rhs_number >= leaf.rhs_number) OR  (anc.lhs_number <= custleaf.lhs_number AND anc.rhs_number >= custleaf.rhs_number)))) vi ON (vi.vehicle_id = vehicle.id) " + 
					" WHERE event_start_time > ? ORDER BY vehicle.id";
				FETCH_CURRENT_DATA = "SELECT * FROM current_data WHERE vehicle_id IN ( # ) and attribute_id in (0) ORDER BY vehicle_id";
				FETCH_TRIP_INFO = "SELECT trip_info.id,load_gate_in,load_gate_out,load_area_in,load_area_out,unload_gate_in,unload_gate_out,unload_area_in,unload_area_out,"+
					"load_area_wait_in,unload_area_wait_in,NAME,vehicle_id,shift_date FROM trip_info JOIN vehicle ON trip_info.vehicle_id = vehicle.id WHERE trip_info.id = ?";
				FETCH_TRIP_INFO_VEH = "SELECT trip_info.id,load_gate_in,load_gate_out,load_area_in,load_area_out,unload_gate_in,unload_gate_out,unload_area_in,unload_area_out,"+
				"load_area_wait_in,unload_area_wait_in,NAME,vehicle_id,shift_date FROM trip_info JOIN vehicle ON trip_info.vehicle_id = vehicle.id WHERE trip_info.vehicle_id = ? and trip_info.combo_start <= ? and trip_info.combo_end >= ?";
//				FETCH_SHIFT_ENGINE_EVENTS = "SELECT vehicle.type, vehicle.customer_id, vehicle.name, engine_events.rule_id, " + 
//				" DATE_FORMAT(CAST(engine_events.event_start_time AS DATE), '%Y-%m-%d'),  " +
//				" engine_events.event_start_time, engine_events.event_begin_name, engine_events.event_stop_time, " + 
//				" engine_events.event_end_name, TIMESTAMPDIFF(MINUTE, engine_events.event_start_time, engine_events.event_stop_time), " + 
//				" MAX((CASE WHEN logged_data.attribute_id=0 THEN logged_data.speed ELSE NULL END) )  " +
//				" FROM singleton, vehicle     " +
//				" JOIN (SELECT DISTINCT(vehicle.id) vehicle_id FROM vehicle LEFT OUTER JOIN port_nodes custleaf ON (custleaf.id = vehicle.customer_id) LEFT OUTER JOIN vehicle_access_groups ON (vehicle_access_groups.vehicle_id = vehicle.id) LEFT OUTER JOIN port_nodes leaf ON (leaf.id = vehicle_access_groups.port_node_id) JOIN port_nodes anc  ON (anc.id IN (?) AND ((anc.lhs_number <= leaf.lhs_number AND anc.rhs_number >= leaf.rhs_number) OR  (anc.lhs_number <= custleaf.lhs_number AND anc.rhs_number >= custleaf.rhs_number))) ) vi ON vi.vehicle_id = vehicle.id " +  
//				" LEFT OUTER JOIN engine_events ON (engine_events.vehicle_id = vehicle.id )  " +
//				" LEFT OUTER JOIN logged_data ON (logged_data.vehicle_id = vehicle.id  AND  (engine_events.event_start_time<= logged_data.gps_record_time AND (engine_events.event_stop_time IS NULL OR (engine_events.event_stop_time>= logged_data.gps_record_time))) ) " + 
//				" WHERE engine_events.event_start_time >= ? " +
//				" GROUP BY vehicle.type,vehicle.customer_id,vehicle.name,engine_events.rule_id,DATE_FORMAT(CAST(engine_events.event_start_time AS DATE), '%Y-%m-%d'),engine_events.event_start_time,engine_events.event_begin_name,engine_events.event_stop_time,engine_events.event_end_name,TIMESTAMPDIFF(MINUTE, engine_events.event_start_time, engine_events.event_stop_time) ";
			}
		}
	}
	public static class MailReport {
		public static final String INSERT_REPORT_MAILING;
		public static final String INSERT_REPORT_MAILING_USERS;
		public static final String INSERT_REPORT_MAILING_LIST;
		public static final String FETCH_CUST_LIST;
		public static final String FETCH_MAILING_REPORT_LIST;
		public static final String FETCH_MAILING_REPORT;
		static{
			if(isMySql){
				INSERT_REPORT_MAILING ="insert into report_mailing (name_,report_type ,max_mail_size,send_all_report_in_single_mail,schedule_type,schedule_hour,schedule_day,status,updated_on )values(?,?,?,?,?,?,?,?,?)";
				INSERT_REPORT_MAILING_USERS = "insert into report_mailing_users (report_mailing_id , user_id) values (?,?) ";
				INSERT_REPORT_MAILING_LIST = "insert into report_list_mailing (report_mailing_id ,report_id,search_parameter,report_subject) values(?,?,?,?)";
				FETCH_CUST_LIST = "select * from customer_contacts ";
				FETCH_MAILING_REPORT_LIST = "select * from  report_mailing";
				FETCH_MAILING_REPORT = "select r.name_,r.report_type,r.max_mail_size,r.send_all_report_in_single_mail , r.schedule_type,r.schedule_hour ,rl.report_id , rl.report_subject from report_list_mailing rl ,   report_mailing r where  r.id = rl.report_mailing_id and r.id = ?";
			}
		}
	}
	
	//ColorCode
	public static class ColorCode {
		public static final String INSERT_COLORCODE;
		public static final String UPDATE_COLORCODE;
		public static final String INSERT_COLORCODE_DETAIL;
		public static final String DELETE_COLORCODE;
		public static final String DELETE_COLORCODE_DETAIL;
		public static final String FETCH_COLORCODE_ID;
		public static final String FETCH_COLORCODE_DETAIL;
		public static final String FETCH_COLORCODE;
		public static final String FETCH_DETAIL_COLORCODE;
		public static final String SELECT_COLORCODE;
		public static final String FETCH_COLORCODE_ID_REPORT;
		public static final String FETCH_COLORCODE_ID_PERFORMANC_REPORT;
		public static final String FETCH_COLORCODE_ID_DETAIL_REPORT;
		static{
			if(isMySql){
				 FETCH_COLORCODE_ID="select max(id) from colorcode where report_id = ? and port_node_id = ? and granularity = ? and aggregation = ? and status=1";
				 DELETE_COLORCODE="delete  from colorcode where id = ?";
				 DELETE_COLORCODE_DETAIL="delete from colorcode_detail where colorcode_id = ?";
				 INSERT_COLORCODE = "insert into colorcode(report_id ,name,notes,status,port_node_id,granularity , aggregation) values(?,?,?,?,?,?,?)";
				 UPDATE_COLORCODE = "update colorcode set report_id=?, name=?, notes=?, status=?, port_node_id=?, granularity=?, aggregation=? where id=?";
				 INSERT_COLORCODE_DETAIL="insert into colorcode_detail(colorcode_id , column_id , oder ,thresholdone ,thresholdtwo,check_for_all) values(?,?,?,?,?,?)";
				 FETCH_COLORCODE_DETAIL = "select colorcode_id ,column_id ,oder ,thresholdone ,thresholdtwo ,check_for_all from colorcode_detail where colorcode_id = ?";
				 FETCH_COLORCODE = "select id , name , notes ,report_id , granularity , aggregation,status from colorcode where port_node_id in (select id from port_nodes where lhs_number >= (select lhs_number from port_nodes where id =? )and rhs_number <= (select rhs_number from port_nodes where id =? )) and status=? ";
				 FETCH_DETAIL_COLORCODE = "select colorcode_id ,column_id ,oder,thresholdone,thresholdtwo,check_for_all from colorcode_detail where colorcode_id = ?";
				 SELECT_COLORCODE ="select name , report_id , notes , status , port_node_id , granularity , aggregation from colorcode where id = ? ";
				 //FETCH_COLORCODE_ID_REPORT = "select id from colorcode where report_id = ? and port_node_id = ? and (? is null or granularity = ?) and (? is null or aggregation = ?)";
				 //FETCH_COLORCODE_ID_PERFORMANC_REPORT = "select id from colorcode where report_id = ? and port_node_id = ? and granularity = ?";
				 //FETCH_COLORCODE_ID_DETAIL_REPORT = "select id from colorcode where report_id = ? and port_node_id = ?";
				 FETCH_COLORCODE_ID_REPORT = "select colorcode.id from colorcode join port_nodes anc on (anc.id = colorcode.port_node_id) join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where report_id = ? and leaf.id = ? and (? is null or granularity = ?) and (? is null or aggregation = ?) and colorcode.status=1 order by anc.lhs_number desc, colorcode.id desc ";
				 FETCH_COLORCODE_ID_PERFORMANC_REPORT = "select colorcode.id from colorcode  join port_nodes anc on (anc.id = colorcode.port_node_id) join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where report_id = ? and leaf.id = ? and granularity = ?  and colorcode.status=1 order by anc.lhs_number desc, colorcode.id desc";
				 FETCH_COLORCODE_ID_DETAIL_REPORT = "select colorcode.id from colorcode  join port_nodes anc on (anc.id = colorcode.port_node_id) join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where report_id = ? and leaf.id = ?  and colorcode.status=1 order by anc.lhs_number desc, colorcode.id desc ";
			}
		}
	}
	public static class MESSAGEBOARD{
		public static final String INSERT_MESSAGE;
		public static final String DELETE_MESSAGE ;
		public static final String FETCH_MESSAGE ;
		
		static {
			if ( isMySql){
				INSERT_MESSAGE = "insert into message_board(user_id,message,port_node_id,updated_on,status) values(?,?,?,?,?)";
				DELETE_MESSAGE = "update message_board set status = ? where id = ?";
				FETCH_MESSAGE = " select message_board.id,user_id,users.name,message,updated_on from message_board left join users on ( users.id = message_board.user_id) where status = ? order by updated_on desc limit 3";
			}
		}
	}
	
	// Queries of Engine events track Report Modules
	public static class ENGINEEVENTSTRACK {
		public final static String INSERT_ENGINE_EVENTS_TRACK;
		public final static String INSERT_REGION_EVENTS_TRACK;
		public final static String UPDATE_ENGINE_EVENTS_TRACK;
		public final static String UPDATE_SUPERVISOR;
		public final static String UPDATE_MANAGER;
		public final static String FETCH_ENGINE_EVENTS_TRACK;
		public final static String FETCH_REGION_EVENTS_TRACK;
		public final static String FETCH_ENGINE_EVENTS;
		public final static String FETCH_REGION_EVENTS;
		public final static String FETCH_ALL_ENGINE_EVENTS_TRACK;
		public final static String GET_ORG;
		public final static String GET_USERS;

		static {
			if (isMySql) {
				INSERT_ENGINE_EVENTS_TRACK = "INSERT INTO engine_events_track (engine_events_id, vehicle_id, rule_id, event_begin_longitude, "
					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
					+" attribute_id, attribute_value, updated_on, event_create_recvtime, event_begin_name, event_end_name, "
					+" alarm_created_by, alarm_created_on, status, priority, user_id, comment_1, event_type, reason_1 ,unsafezone_action)" 
					+" select id, vehicle_id, rule_id, event_begin_longitude, "
					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
					+" attribute_id, attribute_value, updated_on, event_create_recvtime, event_begin_name, event_end_name, ?, ?, ?, ?, ?, ?, ?, ?, ?" 
					+" from engine_events where id = ?";
				INSERT_REGION_EVENTS_TRACK = "INSERT INTO engine_events_track (engine_events_id, vehicle_id, rule_id, "
					+" event_start_time, event_stop_time, "
					+" attribute_id, attribute_value, "
					+" alarm_created_by, alarm_created_on, status, priority, user_id, comment_1, event_type, reason_1,unsafezone_action )" 
					+" select id, vehicle_id, op_station_id, "
					+" event_start_time, event_stop_time, "
					+" event_type, event_type_value, ?, ?, ?, ?, ?, ?, ?, ?, ?" 
					+" from region_track_events where id = ?";
//				INSERT_ENGINE_EVENTS_TRACK = "INSERT INTO engine_events_track (engine_events_id, vehicle_id, rule_id, event_begin_longitude, "
//					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
//					+" attribute_id, attribute_value, updated_on, event_create_recvtime, event_begin_name, event_end_name, "
//					+" reason_1, reason_1_updated_on, comment_1, reason_2, reason_2_updated_on, comment_2, reason_3, reason_3_updated_on, comment_3 )" 
//					+" select id, vehicle_id, rule_id, event_begin_longitude, "
//					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
//					+" attribute_id, attribute_value, updated_on, event_create_recvtime, event_begin_name, event_end_name, ?, ?, ?, ?, ?, ?, ?, ?, ?" 
//					+" from engine_events where id = ?";
				UPDATE_ENGINE_EVENTS_TRACK = " update engine_events_track set " 
					+ " reason_1 = ?, reason_1_updated_on = ?, comment_1 = ?, reason_2 = ?, reason_2_updated_on = ?, comment_2 = ?, reason_3 = ?," 
					+ " reason_3_updated_on = ?, comment_3 = ?" + " where engine_events_id = ? ";
				UPDATE_SUPERVISOR = " update engine_events_track set " 
					+ " reason_1 = ?, reason_1_updated_on = ?, comment_1 = ?, " 
					+ " reason_1_updated_by = ?, status = ?, unsafezone_action = ? " + " where engine_events_id = ? and event_type = ? ";
				UPDATE_MANAGER = " update engine_events_track set " 
					+ " reason_1 = ?, reason_2_updated_on = ?, comment_1 = ?, " 
					+ " reason_2_updated_by = ?, status = ? " + " where engine_events_id = ? and event_type = ? ";
				FETCH_ENGINE_EVENTS_TRACK = " select engine_events_id, vehicle.name vehicleName, vehicle_id, rule_id, rules.name ruleName, event_begin_longitude, "
					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
					+" attribute_id, attribute_value, engine_events_track.updated_on, event_create_recvtime, event_begin_name, event_end_name, "
					+" reason_1, comment_1, event_type, " 
//					+		" reason_1_updated_on, reason_2, reason_2_updated_on, comment_2, reason_3, reason_3_updated_on, comment_3, alarm_question, alarm_created_on,"
					+" alarm_created_on, engine_events_track.status, priority, engine_events_track.user_id  from engine_events_track " 
					+" LEFT JOIN vehicle ON engine_events_track.vehicle_id = vehicle.id "
					+" LEFT JOIN rules ON engine_events_track.rule_id = rules.id "
					+" where engine_events_id = ? and event_type = ? ";
				FETCH_REGION_EVENTS_TRACK = " select engine_events_id, vehicle.name vehicleName, engine_events_track.vehicle_id, rule_id, op_station.name ruleName, event_begin_longitude, "
					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
					+" attribute_id, attribute_value, engine_events_track.updated_on, event_create_recvtime, event_begin_name, event_end_name, "
					+" reason_1, comment_1, event_type, " 
//					+		" reason_1_updated_on, reason_2, reason_2_updated_on, comment_2, reason_3, reason_3_updated_on, comment_3, alarm_question, alarm_created_on,"
					+" alarm_created_on, engine_events_track.status, priority, engine_events_track.user_id  from engine_events_track " 
					+" LEFT JOIN vehicle ON engine_events_track.vehicle_id = vehicle.id "
					+" LEFT JOIN op_station ON engine_events_track.rule_id = op_station.id "
					+" where engine_events_id = ? and event_type = ? ";
				FETCH_ENGINE_EVENTS = " select engine_events.id, vehicle.name vehicleName, vehicle_id, rule_id, rules.name ruleName, event_begin_longitude, "
					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
					+" attribute_id, attribute_value, engine_events.updated_on, event_create_recvtime, event_begin_name, event_end_name "
					+" from engine_events " 
					+" LEFT JOIN vehicle ON engine_events.vehicle_id = vehicle.id "
					+" LEFT JOIN rules ON engine_events.rule_id = rules.id "
					+" where engine_events.id = ? ";
				FETCH_REGION_EVENTS = " select region_track_events.id, vehicle.name vehicleName, region_track_events.vehicle_id, "
					+"  region_track_events.op_station_id, op_station.name opStationName, region_track_events.event_start_time, "
					+"  region_track_events.event_stop_time, region_track_events.event_type, region_track_events.event_type_value "
					+" from region_track_events " 
					+" LEFT JOIN vehicle ON region_track_events.vehicle_id = vehicle.id "
					+" LEFT JOIN op_station ON region_track_events.op_station_id = op_station.id "
					+" where region_track_events.id = ? ";
				GET_ORG = "SELECT port_node_id FROM opstation_mapping "
					+" WHERE op_station_id = (select region_track_events.op_station_id from region_track_events "
					+" where region_track_events.id = ?) ";
				GET_USERS = "SELECT users.id, users.name FROM users WHERE users.port_node_id IN (SELECT pnleft.id"
					+" FROM port_nodes base JOIN  port_nodes pnleft  ON ((base.lhs_number >= pnleft.lhs_number AND base.rhs_number <= pnleft.rhs_number)" 
					+" OR (base.lhs_number <= pnleft.lhs_number AND base.rhs_number >= pnleft.rhs_number)) AND base.id = ?) ";
				FETCH_ALL_ENGINE_EVENTS_TRACK = "select * from engine_events_track";
//				INSERT_ENGINE_EVENTS_TRACK = "INSERT INTO engine_events_track (engine_events_id, vehicle_id, rule_id, event_begin_longitude, "
//					+" event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, "
//					+" attribute_id, attribute_value, updated_on, event_create_recvtime, event_begin_name, event_end_name, "
//					+" reason_1, reason_1_updated_on, comment_1, reason_2, reason_2_updated_on, comment_2, reason_3, reason_3_updated_on, comment_3 )" 
//					+"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			}
		}
	}
	
	public static class OperationStatus{
		
		public static final String INSERT_OP_STATUS ;

		static {
			if (isMySql) {
				INSERT_OP_STATUS = "insert into op_station_operational_status(op_station_id,status,begin_time,end_time,updated_on,reason)" +
																		" value(?,?,?,?,?,?)";
			}
		}
	}
	
	public static class SHIFT_ROSTER {
		public static final String GET_SHIFT_ROSTER = "select driver_details.id, driver_details.driver_name, from_date, to_date, shift_id from driver_details join port_nodes leaf on (leaf.id = driver_details.org_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) "+
		                                                                              " left outer join driver_weekly_shift on (driver_id = driver_details.id and from_date >= ? and from_date < ?) and driver_details.status in (1) order by driver_details.driver_name, from_date";
		public static final String DELETE_SHIFT_ROSTER = "delete from driver_weekly_shift where driver_id=? and from_date >= ? and from_date < ?";
		public static final String INSERT_SHIFT_ROSTER = "insert into driver_weekly_shift (driver_id, from_date, shift_id) values (?,?,?)";
		public static final String GET_ORG_WEEK_BEGIN = "select day from org_week_begin_day join port_nodes leaf on (leaf.id = org_week_begin_day.port_node_id and leaf.id = ?) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by anc.lhs_number desc  ";
	}
	
	public static class ATTENDANCE {
		public static final String GET_ATTENDANCE_ROSTER = "select driver_details.id, driver_details.driver_name, in_ts, out_ts  from driver_details join port_nodes leaf on (leaf.id = driver_details.org_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) "+
		                                                                              " left outer join driver_attendance on (driver_id = driver_details.id and in_ts >= ? and in_ts < ?) and driver_details.status in (1) order by driver_details.driver_name, in_ts";
		public static final String DELETE_ATTENDANCE_ROSTER = "delete from driver_attendance where driver_id=? and date(in_ts)=?";
		public static final String INSERT_ATTENDANCE_ROSTER = "insert into driver_attendance (driver_id, in_ts, out_ts) values (?,?,?)";
	}
	
	public static class WAYPOINT {
		public static final String GET_WAYPOINT_COMMON = "select vwp.id, vwp.vehicle_id, vehicle.name vehicle_name, vwp.status, vwp.lon_at_creation, vwp.lat_at_creation, vwp.time_at_creation, vwp.source_opstation_id, src.name src_name, vwp.source_leave, "+
		" dest_lm_id, dlm.name dest_lm_name, dest_opstation_id, dest.name dest_name, dest_eta_orig, dest_eta, dest_eta_actual, "+
		" wp1_lm_id, wp1lm.name wp1_lm_name, wp1_eta_orig, wp1_eta, wp1_eta_actual, "+
		" wp2_lm_id, wp2lm.name wp2_lm_name, wp2_eta_orig, wp2_eta, wp2_eta_actual, "+
		" wp3_lm_id, wp3lm.name wp3_lm_name, wp3_eta_orig, wp3_eta, wp3_eta_actual, "+
		" wp4_lm_id, wp4lm.name wp4_lm_name, wp4_eta_orig, wp4_eta, wp4_eta_actual, "+
		" wp5_lm_id, wp5lm.name wp5_lm_name, wp5_eta_orig, wp5_eta, wp5_eta_actual "
		//" from vehicle left outer join vehicle_waypoint_eta vwp on (vehicle.id = vwp.vehicle_id " //notice the missing )
		//;
		;
		
		public static final String GET_WAYPOINT_COMMON_2 = 
		" left outer join op_station src on (src.id = vwp.source_opstation_id) "+
		" left outer join op_station dest on (dest.id = vwp.dest_opstation_id) "+
		" left outer join landmarks dlm on (dlm.id = vwp.dest_lm_id) "+
		" left outer join landmarks wp1lm on (wp1lm.id = vwp.wp1_lm_id) "+
		" left outer join landmarks wp2lm on (wp2lm.id = vwp.wp2_lm_id) "+
		" left outer join landmarks wp3lm on (wp3lm.id = vwp.wp3_lm_id) "+
		" left outer join landmarks wp4lm on (wp4lm.id = vwp.wp4_lm_id) "+
		" left outer join landmarks wp5lm on (wp5lm.id = vwp.wp5_lm_id) "
		;
		
		public static final String UPDATE_STATUS = "update vehicle_waypoint_eta set status=? where id=?";
		public static final String INSERT_WP_INFO = "insert into vehicle_waypoint_eta (vehicle_id, stauts, source_opstation_id, source_leave,  "+
		                                                                           ", dest_lm_id, dest_opstation_id, dest_eta_orig, dest_eta, dest_eta_actual "+
		                                                                           ", wp1_lm_id, wp1_eta_orig, wp1_eta, wp1_eta_actual "+
		                                                                           ", wp1_lm_id, wp1_eta_orig, wp1_eta, wp1_eta_actual "+
		                                                                           ", wp1_lm_id, wp1_eta_orig, wp1_eta, wp1_eta_actual "+
		                                                                           ", wp1_lm_id, wp1_eta_orig, wp1_eta, wp1_eta_actual "+
		                                                                           ", wp1_lm_id, wp1_eta_orig, wp1_eta, wp1_eta_actual "+
		                                                                           ",lon_at_creation, lat_at_creation, time_at_creation "+
		                                                                           ")"+
		                                                                           "(select ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current_data.longitude, current_data.latitude, sysdate() " +
		                                                                           " from current_data where vehicle_id = ? and attribute_id=0)"
		                                                                           ;
		public static final String UPDATE_WP_INFO = "update vehicle_waypoint_eta set vehicle_id=?, stauts=?, source_opstation_id=?, source_leave=?,  "+
        ", dest_lm_id=?, dest_opstation_id=?, dest_eta_orig=?, dest_eta=?, dest_eta_actual=? "+
        ", wp1_lm_id=?, wp1_eta_orig=?, wp1_eta=?, wp1_eta_actual=? "+
        ", wp1_lm_id=?, wp1_eta_orig=?, wp1_eta=?, wp1_eta_actual=? "+
        ", wp1_lm_id=?, wp1_eta_orig=?, wp1_eta=?, wp1_eta_actual=? "+
        ", wp1_lm_id=?, wp1_eta_orig=?, wp1_eta=?, wp1_eta_actual=? "+
        ", wp1_lm_id=?, wp1_eta_orig=?, wp1_eta=?, wp1_eta_actual=? "+
        " where id=?"
        ;
		
		public static final String CHECK_IF_DEST_CHANGED ="select dest_opstation_id, dest_lm_id from vehicle_waypoint_eta where id = ?";
		

	}
	//rahul
	public static class CUSTDASHBOARD {
		public static final String FETCH_DASH_INFO;
		public static final String FETCH_DASH_INFO_PORT;
		public static final String FETCH_DASH_INFO_USER;
		public static final String INSERT_DASH_INFO;
		public static final String INSERT_COMPONENT_INFO;
		public static final String UPDATE_COMPONENT_INFO;
		public static final String UPDATE_DASH_INFO;
		public static final String FETCH_COMPONENT_INFO;
		public static final String FETCH_STANDARD_COMPONENT_LIST;
		public static final String DELETE_COMPONENT_INFO;
					static{
			if(isMySql){
				FETCH_DASH_INFO =" select dash_info.id,dash_info.port_node_id,dash_info.user_id,dash_info.pg_title,dash_info.pg_context," +
				"dash_info.status,dash_info.help,dash_info.pg_action from dash_info where dash_info.port_node_id is null and dash_info.user_id is null and dash_info.status=? and dash_info.pg_context = ?";
				FETCH_DASH_INFO_PORT =" select dash_info.id,dash_info.port_node_id,dash_info.user_id,dash_info.pg_title,dash_info.pg_context," +
						"dash_info.status,dash_info.help,dash_info.pg_action from dash_info where dash_info.port_node_id=? and dash_info.status=? and dash_info.pg_context = ?";
				FETCH_DASH_INFO_USER =" select dash_info.id,dash_info.port_node_id,dash_info.user_id,dash_info.pg_title,dash_info.pg_context," +
				"dash_info.status,dash_info.help,dash_info.pg_action from dash_info where dash_info.user_id=? and dash_info.status=? and dash_info.pg_context = ?";
				FETCH_COMPONENT_INFO =" select dash_component.component_id,dash_component.dash_info_id,dash_component.title," +
						"dash_component.uid_tag,dash_component.div_left,dash_component.div_top,dash_component.div_width," +
						"dash_component.div_height,dash_component.refresh_int,dash_component.xml,components.url,dash_component.miscellaneous,components.type from dash_component join components on(components.id=dash_component.component_id) where dash_info_id=? and status=1"; 
				FETCH_STANDARD_COMPONENT_LIST = " select components.id,components.name,components.xml,components.url,components.type from components";  
				UPDATE_COMPONENT_INFO=" update dash_component set title = ?, div_left = ?," +
						" div_top = ?, div_height = ?, div_width = ?, refresh_int = ? where uid_tag = ? and dash_info_id=?";
				UPDATE_DASH_INFO=" update dash_info set pg_context = ?,pg_title = ?, status = ?, help = ? where id=?";
				INSERT_COMPONENT_INFO=" insert into dash_component(component_id,dash_info_id,title,div_left,div_top,div_height,div_width,refresh_int,miscellaneous,xml) " +
						"values(?,?,?,?,?,?,?,?,?,?)";
				INSERT_DASH_INFO = " insert into dash_info(port_node_id,user_id,pg_context,pg_title,pg_action,status,help) values(?,?,?,?,?,?,?)";
				DELETE_COMPONENT_INFO=" update dash_component set status=0 where dash_info_id= ? and uid_tag = ?";
			}
		}
	}
	
	//added by balwant for dynamic menureport...............
	
	public static class MENUREPORT {
		public static final String GET_MENU_REPORT_INFORMATION;
		public static final String INSERT_REPORT_DEFINITION;
		public static final String INSERT_REPORT_PARAMETER;
		public static final String INSERT_REPORT_MENUPLACEHOLDER;
		public static final String UPDATE_REPORT_MENUPLACEHOLDER;
		public static final String INSERT_EMAIL_REPORT_DEFINITION;
		public static final String DELETE_EMAIL_REPORT_DEFINITION;
		public static final String INSERT_EMAIL_REPORT_GROUPS;
		public static final String UPDATE_EMAIL_REPORT_GROUPS;
		public static final String INSERT_EMAIL_REPORT_USERS;
		public static final String DELETE_EMAIL_REPORT_USERS;
		public static final String INSERT_EMAIL_REPORT_FREQUENCIES;
		public static final String DELETE_EMAIL_REPORT_FREQUENCIES;
		public static final String GET_MENU_REPORT_LISTS;
		public static final String GET_MENU_REPORT_DATA;
		public static final String GET_EMAIL_REPORT_GROUP;
		public static final String GET_EMAIL_REPORT_EMAIL;
		public static final String GET_EMAIL_REPORT_FREQUENCY;
		//public static final String DELETE_EMAIL_REPORT_FREQUENCY;
		public static final String UPDATE_REPORT_DEFINITION;
		public static final String GET_EMAIL_REPORT_DATA;
		public static final String UPDATE_OPTIONAL_MENU_NAME;
		public static final String GET_ALL_EMAIL_REPORT_GROUP;
		public static final String INSERT_MENU_MASTER_REPORT_DEFINITION;
		public static final String UPDATE_MENU_MASTER_MENU_TAG;
		public static final String GET_ALL_ACTIVE_EMAIL_REPORT_GROUP;
		public static final String 	GET_ONLINE_REPORT_LIST;
		public static final String GET_ONLINE_REPORT_DATA;
		public static final String GET_REPORT_SENDER_PROFILES;
		public static final String UPDATE_REPORT_DEFINITION_MENU_ID;
		
					static{
			if(isMySql){
			GET_MENU_REPORT_INFORMATION = "select optional_Menu_Name,name,title,for_port_node_id,for_user_id,menu_placeholder_id,component_file,page_context from report_definitions join menu_report_definition on (menu_report_definition.report_definition_id = report_definitions.id)"+ 
                                          " join menu_master_report_definition on (report_definitions.id =  menu_master_report_definition.report_definition_id)" +
                                          " join menu_master on (menu_master_report_definition.menu_master_id = menu_master.id) where report_definitions.id = ?";
	        INSERT_REPORT_DEFINITION = "insert into report_definitions(page_context,optional_Menu_Name,name,title,help,type,status,for_port_node_id,for_user_id,no_data,org_mailing_id) values (?,?,?,?,?,?,?,?,?,?,?)";
	        UPDATE_REPORT_DEFINITION = "update report_definitions set optional_Menu_Name = ? , name = ? ,title = ? ,help = ? ,type = ?, status = ? , for_port_node_id = ? , for_user_id = ?, no_data=?,org_mailing_id=? where id = ?";
	        INSERT_REPORT_PARAMETER = "insert into report_definition_params(report_definition_id,param_name,param_value) values (?,?,?)";
	        INSERT_REPORT_MENUPLACEHOLDER = "insert into menu_report_definition(menu_placeholder_id,report_definition_id) values (?,?)";
	        UPDATE_REPORT_MENUPLACEHOLDER = "update menu_report_definition set menu_placeholder_id = ? where report_definition_id = ?";
	        INSERT_EMAIL_REPORT_DEFINITION = "insert into email_report_definition(report_definition_id,email_report_group_id) values (?,?)";
	        DELETE_EMAIL_REPORT_DEFINITION = "delete from email_report_definition where report_definition_id = ?";
	        INSERT_EMAIL_REPORT_GROUPS = "insert into email_report_groups(name,report_format,status,report_definition_id) values (?,?,?,?)";
	        UPDATE_EMAIL_REPORT_GROUPS = "update email_report_groups set name = ? ,report_format = ? ,status = ?,report_definition_id = ? where id = ?";
	        INSERT_EMAIL_REPORT_USERS = "insert into email_report_users(email_report_group_id,email) values (?,?)";
	        DELETE_EMAIL_REPORT_USERS = "delete from email_report_users where email_report_group_id = ?";
	        INSERT_EMAIL_REPORT_FREQUENCIES = "insert into email_report_frequencies(email_report_group_id,granularity,hours,minutes) values (?,?,?,?)";
	        DELETE_EMAIL_REPORT_FREQUENCIES = "delete from email_report_frequencies where email_report_group_id = ?";
	        GET_MENU_REPORT_LISTS = " select report_definitions.id, menu_report_definition.menu_placeholder_id,report_definitions.name,report_definitions.status,report_definitions.help,report_definitions.optional_Menu_Name ,menu_master.menu_tag, menu_master.component_file, menu_master.port_node_id, menu_master.id menu_id " +
	        		" from report_definitions  join port_nodes custleaf on (custleaf.id = report_definitions.for_port_node_id)  join port_nodes anc  " +
	        		" on (anc.id in (?) and (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))" +
	        		" join menu_report_definition on (menu_report_definition.report_definition_id = report_definitions.id) join menu_master_report_definition on (menu_master_report_definition.report_definition_id=report_definitions.id) join menu_master on (menu_master.id=menu_master_report_definition.menu_master_id)  where type = ? ";
	        GET_MENU_REPORT_DATA = "select menu_placeholder_id,optional_Menu_Name,name,title,status,help,for_user_id,for_port_node_id, menu_master.id as menumasterId,no_data,org_mailing_id from report_definitions join menu_report_definition on (report_definition_id = id)" + 
                                   " join menu_master_report_definition on (menu_master_report_definition.report_definition_id = report_definitions.id)"+ 
                                   " join menu_master on (menu_master.id = menu_master_report_definition.menu_master_id)"+
                                    " where type = ? and report_definitions.id = ?";
	        GET_EMAIL_REPORT_GROUP = "select email_report_groups.id,email_report_groups.name,email_report_groups.report_format from report_definitions join email_report_definition on (email_report_definition.report_definition_id = report_definitions.id) join email_report_groups on (email_report_groups.id=email_report_definition.email_report_group_id) where report_definitions.id = ? and type = ?";
	      //  GET_EMAIL_REPORT_EMAIL = "select email_report_users.email_report_group_id,email_report_users.email from report_definitions join email_report_definition on (email_report_definition.report_definition_id = report_definitions.id)   join email_report_users on (email_report_users.email_report_group_id =email_report_definition.email_report_group_id) where report_definitions.id = ? and type=0";
	        GET_EMAIL_REPORT_EMAIL = "select email_report_users.email_report_group_id,email_report_users.email from email_report_users join email_report_groups on (id = email_report_users.email_report_group_id ) where report_definition_id= ?";
	        GET_EMAIL_REPORT_FREQUENCY = "select email_report_frequencies.email_report_group_id,email_report_frequencies.granularity,email_report_frequencies.hours,email_report_frequencies.minutes from email_report_frequencies join email_report_definition on (email_report_definition.email_report_group_id = email_report_frequencies.email_report_group_id) join report_definitions on (report_definition_id = id) where type = ? and id = ? ";
	        GET_EMAIL_REPORT_DATA = "select id,name,report_format,status from email_report_groups where report_definition_id = ?";
			UPDATE_OPTIONAL_MENU_NAME = "update report_definitions set optional_Menu_Name = ? where optional_Menu_Name = ? ";
			GET_ALL_ACTIVE_EMAIL_REPORT_GROUP = "select id,name from email_report_groups where status = 1";
			GET_ALL_EMAIL_REPORT_GROUP	= "select id,name,status from email_report_groups";
			INSERT_MENU_MASTER_REPORT_DEFINITION = "insert into menu_master_report_definition(report_definition_id,menu_master_id) values (?,?)";
			UPDATE_MENU_MASTER_MENU_TAG = "update menu_master set menu_tag = ?,port_node_id = ?,user_id = ? where id = ?";
		
			GET_ONLINE_REPORT_LIST = " select report_definitions.id, report_definitions.name,report_definitions.status,report_definitions.help,report_definitions.optional_Menu_Name, menu_master.menu_tag, menu_master.component_file, menu_master.port_node_id,menu_master.id menu_id from " +
					" report_definitions  join port_nodes custleaf on (custleaf.id = report_definitions.for_port_node_id)  join port_nodes anc  " +
	        		" on (anc.id in (?) and (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))" +
					" join menu_master on (menu_master.id=report_definitions.master_menu_id)  where type = ? ";
			GET_ONLINE_REPORT_DATA = "select optional_Menu_Name,name,title,status,help,for_user_id,for_port_node_id, menu_master.id as menumasterId,no_data,org_mailing_id from report_definitions  join menu_master_report_definition on (menu_master_report_definition.report_definition_id = report_definitions.id) join menu_master on (menu_master.id = menu_master_report_definition.menu_master_id) where type = ? and report_definitions.id = ?";
			GET_REPORT_SENDER_PROFILES = " select id,short_code from org_mailing_params ";
			UPDATE_REPORT_DEFINITION_MENU_ID = "update report_definitions set master_menu_id=? where id=?" ;
			}
		}
	}
	public static class MANUALFUELLOG{
		public static final String INSERT_FULE_LOG;
		public static final String UPDATE_FUEL_LOG;
		
		static{
			if(isMySql){
				INSERT_FULE_LOG="insert into manual_fuel_log (vehicle_id, odometer, engine_hr," +
                        " metric_one, metric_two, filling_date, fuel_left, fuel_reading, updated_by," +
                     " invoice_no, notes) values(?,?,?,?,?,?,?,?,?,?,?)";
				UPDATE_FUEL_LOG = " update manual_fuel_log set vehicle_id = ?, odometer = ?," 
						+ " filling_date = ?, fuel_left = ?, fuel_reading = ?, updated_by = ?," 
						+ " invoice_no = ?, notes = ? where manual_fuel_log.id = ?";
			}
		}
	}
	public static class VEHICLEUTILIZATION{
		public static final String INSERT_VEHICLE_UTIL_LOG;
		public static final String UPDATE_VEHICLE_UTIL_LOG;
		public static final String DELETE_VEHICLE_UTIL_LOG;
		public static final String FETCH_VEHICLE_UTIL_LOG_BY_ID;
		public static final String INSERT_VEHICLE_MOBIL_LOG;
		public static final String UPDATE_VEHICLE_MOBIL_LOG;
		public static final String DELETE_VEHICLE_MOBIL_LOG;
		public static final String FETCH_VEHICLE_MOBIL_LOG_BY_ID;
		public static final String FETCH_ENGINE_HR_BY_VEHICLE_ID;
		static{
			if(isMySql){
				INSERT_VEHICLE_UTIL_LOG ="insert into vehicle_utilization(vehicle_id, status, from_date, to_date, odometer, start_engine_hr, end_engine_hr, metric_one, metric_two,holding_hr, marching_hr," +
						" overtime_hr, notes, create_date, updated_by, updated_on) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
				UPDATE_VEHICLE_UTIL_LOG = "update vehicle_utilization set vehicle_id = ?, from_date = ?, to_date = ?, odometer=?, start_engine_hr = ?, end_engine_hr = ?, metric_one=?, metric_two=?, holding_hr = ?," +
						" marching_hr = ?, overtime_hr = ?, notes = ?, updated_by = ?, updated_on = ? where id = ?";
				DELETE_VEHICLE_UTIL_LOG = " update vehicle_utilization set status = ? where id = ?";
				FETCH_VEHICLE_UTIL_LOG_BY_ID = "select vehicle_utilization.id, vehicle.name vehicle_name, " +
						" vehicle.customer_id port_node_id,vehicle_utilization.vehicle_id,  vehicle_utilization.status, " +
						" vehicle_utilization.from_date, vehicle_utilization.to_date, start_engine_hr, vehicle_utilization.end_engine_hr, " +
						" vehicle_utilization.holding_hr, vehicle_utilization.marching_hr, vehicle_utilization.overtime_hr, " +
						" vehicle_utilization.notes, vehicle_utilization.create_date, users.name updated_by, " +
						" vehicle_utilization.updated_on from vehicle_utilization  join (port_nodes anc join port_nodes leaf on " +
						" ( anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join vehicle  on " +
						" vehicle.customer_id = leaf.id) on vehicle.id = vehicle_utilization.vehicle_id left outer join users on " +
						" (users.id=vehicle_utilization.updated_by) where vehicle_utilization.id=? group by vehicle_utilization.id ";
				INSERT_VEHICLE_MOBIL_LOG = "insert into vehicle_mobilization(vehicle_id, status, from_date," +
						" to_date, type, notes, create_date, updated_by, updated_on) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
				UPDATE_VEHICLE_MOBIL_LOG = "update vehicle_mobilization set vehicle_id = ?, from_date = ?, to_date = ?," +
						" type = ?, notes = ?, updated_by = ?, updated_on = ? where id = ? ";
				DELETE_VEHICLE_MOBIL_LOG = " update vehicle_mobilization set status = ? where id = ?";
				FETCH_VEHICLE_MOBIL_LOG_BY_ID = "select vehicle_mobilization.id, vehicle.name vehicle_name, " +
						" vehicle.customer_id port_node_id,vehicle_mobilization.vehicle_id, vehicle_mobilization.status, " +
						" vehicle_mobilization.from_date, vehicle_mobilization.to_date, vehicle_mobilization.type, " +
						" vehicle_mobilization.notes, vehicle_mobilization.create_date, users.name updated_by," +
						" vehicle_mobilization.updated_on from vehicle_mobilization  join (port_nodes anc join port_nodes leaf " +
						" on ( anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join vehicle " +
						" on vehicle.customer_id = leaf.id) on vehicle.id = vehicle_mobilization.vehicle_id " +
						" left outer join users on (users.id=vehicle_mobilization.updated_by) where vehicle_mobilization.id=? group by vehicle_mobilization.id";
				FETCH_ENGINE_HR_BY_VEHICLE_ID = "select speed from logged_data where vehicle_id = ? and attribute_id = 2 and gps_record_time > ? order by gps_record_time asc limit 1";
			}
		}
	}
public static class InventoryMng{
	public static final String INSERT_INVENTORY_PRODUCT;
	public static final String GET_INVENTORY_PRODUCT_DATA;
	public static final String INSERT_NEW_STOCK_DATA;
	//public static final String UPDATE_STOCK_DATA;
	public static final String UPDATE_STOCK_RELEASED_DATA;
	public static final String GET_DISTINCT_ITEM_CODE;
	public static final String GET_INVENTORY_DETAIL_DATA;
	public static final String GET_PRODUCT_DETAIL_DATA;
	public static final String UPDATE_INVENTORY_PRODUCT;
	public static final String GET_INVENTORY_DATA_To_RELEASE;
	public static final String INSERT_RELEASE_DATA;
	public static final String STOCK_ACQ_REPORT_DATA;
	public static final String STOCK_RELEASE_REPORT_DATA;
	public static final String ADD_STOCK_HISTORY_DATA;
	public static final String GET_MAX_CUMMULATIVE_ADD_STOCK;
	public static final String GET_MAX_CUMMULATIVE_RELEASE_STOCK;
	static{
	INSERT_INVENTORY_PRODUCT = "insert into inventory_product(categoryId,item_name,item_code,manufacturer,manufacturer_code,notes,created_on,life,unit,port_node_id) values(?,?,?,?,?,?,?,?,?,?)";
	//GET_INVENTORY_PRODUCT_DATA = "select categoryId,item_name,inventory_product.item_code as code,(case when getReleaseStocks(inventory_product_detail.id) is null then sum(qty) else sum(qty)-getReleaseStocks(inventory_product_detail.id) end),inventory_product.id from inventory_product left join inventory_product_detail on (inventory_product.id = inventory_product_detail.inventory_product_id) left join inventory_product_release on (inventory_product_detail.id=prodcut_detail_id)";
	GET_INVENTORY_PRODUCT_DATA = "select inventory_product.categoryId,inventory_product.item_name,inventory_product.item_code,sum((case when qty is not null then qty else 0 end)-(case when inventory_product_release.qtytorelease is not null then inventory_product_release.qtytorelease else 0 end)) " +
			",inventory_product.id,inventory_product.notes,inventory_product.life,inventory_product.unit  " +
			"from port_nodes anc join port_nodes leaf on (anc.id=? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
			"join inventory_product on (inventory_product.port_node_id=leaf.id) left outer join " +
			"inventory_product_detail on (inventory_product.id =inventory_product_detail.inventory_product_id) " +
			"left outer join inventory_product_release on (inventory_product.id=inventory_product_release.prodcut_detail_id) ";
	INSERT_NEW_STOCK_DATA = "insert into inventory_product_detail(inventory_product_id,lot_number,qty,price,age,manufacturer,supplier,purchase_receive,delivery_report,notes,warrenty_date,mfg_date,acquisition_date,created_on,qty_1) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	GET_DISTINCT_ITEM_CODE = "select id,item_code from inventory_product";
	GET_INVENTORY_DETAIL_DATA = "select lot_number,qty,price,age,inventory_product.manufacturer,supplier,purchase_receive,delivery_report,inventory_product_detail.notes,warrenty_date,mfg_date,acquisition_date,inventory_product_detail.created_on,item_name,inventory_product_detail.id,inventory_product.item_code,qty_1,yetReleased  from inventory_product_detail join inventory_product on (inventory_product.id = inventory_product_detail.inventory_product_id )";
	GET_PRODUCT_DETAIL_DATA = "select id, categoryId,item_code,item_name,manufacturer,manufacturer_code,notes,life,unit from inventory_product where id=?";
	UPDATE_INVENTORY_PRODUCT = "update inventory_product set  categoryId =? ,item_name=?,item_code=?,manufacturer=?,manufacturer_code=?,notes=?,updated_on=?,life=?,unit=? where id=?";
//	UPDATE_STOCK_DATA = "update inventory_product_detail  set inventory_product_id = ?,lot_number = ?,qty = ?,price = ?,age = ?,manufacturer = ?,supplier = ?,purchase_receive = ?,delivery_report = ?,notes = ?,warrenty_date = ?,mfg_date = ?,acquisition_date = ?,updated_on = ?,qty_1 = ? where id = ?";
	UPDATE_STOCK_RELEASED_DATA = "update inventory_product_detail  set lot_number = ?,price = ?,age = ?,manufacturer = ?,supplier = ?,purchase_receive = ?,delivery_report = ?,notes = ?,warrenty_date = ?,mfg_date = ?,acquisition_date = ?,updated_on = ? where id = ?";
	GET_INVENTORY_DATA_To_RELEASE = "select inventory_product_detail.id,inventory_product_id,lot_number,sum(qty) qty, age,price,item_name,inventory_product_detail.created_on from inventory_product_detail join inventory_product on (inventory_product.id=inventory_product_id) where qty > 0 and inventory_product_id in (";
	INSERT_RELEASE_DATA = "insert into inventory_product_release (prodcut_detail_id,qtytorelease,ticket_id,itemPurchase,deliveryReport,notes,released_on) values (?,?,?,?,?,?,?)";
	STOCK_ACQ_REPORT_DATA = "select inventory_product_detail.id,inventory_product.item_code,inventory_product.item_name,qty_1,(case when inventory_product_detail.updated_on is null then inventory_product_detail.created_on else inventory_product_detail.updated_on end) date,categoryId,acquisition_date,lot_number from inventory_product_detail join inventory_product on (inventory_product_id= inventory_product.id )";
	STOCK_RELEASE_REPORT_DATA = "select categoryId,inventory_product_release.id,qtytorelease,released_on,inventory_product.item_code,inventory_product.item_name,lot_number from inventory_product_release join inventory_product_detail on (prodcut_detail_id = inventory_product_detail.id) join inventory_product on (inventory_product_id = inventory_product.id)";
	ADD_STOCK_HISTORY_DATA = "insert into inventory_product_history (prodcut_detail_id,qty_to_added,qty_to_release,created_on) values (?,?,?,?)";
	GET_MAX_CUMMULATIVE_ADD_STOCK = "select prodcut_detail_id,max(cummulative_qty_added) from inventory_product_history group by prodcut_detail_id";
	GET_MAX_CUMMULATIVE_RELEASE_STOCK = "select prodcut_detail_id,max(cummulative_qty_released) from inventory_product_history group by prodcut_detail_id";
	
	}
}

public static class DespatchEtc {
	public static final String GET_OFFICE_STATIONLIST;
	public static final String GET_LOAD_STATIONLIST;
	public static final String GET_UNLOAD_STATIONLIST;
	public static final String INSERT_DESP_INSTR;
	public static final String INSERT_DESP_CHALLAN;
	public static final String UPDATE_DESP_CHALLAN;
	
	static {
		GET_OFFICE_STATIONLIST = "select op_station.id, op_station.name "+
" from vehicle join port_nodes leaf on (vehicle.id = ? and leaf.id = vehicle.customer_id) join port_nodes anc on (anc.lhs_number <=leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
" join opstation_mapping on (opstation_mapping.port_node_id = anc.id and opstation_mapping.type in (30)) join op_station on (opstation_mapping.op_station_id=op_station.id) "+
" order by op_station.name ";
		GET_LOAD_STATIONLIST = "select op_station.id, op_station.name "+
		" from vehicle join port_nodes leaf on (vehicle.id = ? and leaf.id = vehicle.customer_id) join port_nodes anc on (anc.lhs_number <=leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
		" join opstation_mapping on (opstation_mapping.port_node_id = anc.id and opstation_mapping.type in (1,11,15,16,17)) join op_station on (opstation_mapping.op_station_id=op_station.id) "+
		" order by op_station.name ";
		GET_UNLOAD_STATIONLIST = "select op_station.id, op_station.name "+
		" from vehicle join port_nodes leaf on (vehicle.id = ? and leaf.id = vehicle.customer_id) join port_nodes anc on (anc.lhs_number <=leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
		" join opstation_mapping on (opstation_mapping.port_node_id = anc.id and opstation_mapping.type in (2,11,15,16,17)) join op_station on (opstation_mapping.op_station_id=op_station.id) "+
		" order by op_station.name ";
		
		INSERT_DESP_INSTR= "insert into ld_despatch(vehicle_id, wait_at_curr, wait_at_station_id, goto_load_station_id, create_time) values (?,?,?,?,?)";
		
		INSERT_DESP_CHALLAN = "insert into challan_details(vehicle_id, challan_date, gr_no_, from_station_id, to_station_id, consignor, consignee " +
				", container_1_no, container_2_no, material_id, wt_by_qty, bill_party, port_node_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
		UPDATE_DESP_CHALLAN = "update challan_details set vehicle_id=?, challan_date=?, gr_no_=?, from_station_id=?, to_station_id=?, consignor=?, consignee=? " +
		", container_1_no=?, container_2_no=?, material_id=?, wt_by_qty=?, bill_party=?, port_node_id=? where id=?";
	
		



	}
}
	
}	
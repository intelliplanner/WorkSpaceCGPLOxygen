package com.ipssi.swm;

public class DBQueries {
	private static final boolean isMySql = true;

	public static class SETUP {
		public final static String GET_GROUP_ALL; //must match GET_GROUP_INFO select
		public final static String GET_GROUP_INFO;
		public final static String INSERT_GROUP_HEADER;
		public final static String UPDATE_GROUP_HEADER;
		public final static String DELETE_OPSLIST;
		public final static String INSERT_OPSLIST;
		static {
			if (isMySql) {
				GET_GROUP_ALL = "select group_opstations.id, group_opstations.name, group_opstations.status, group_opstations.description, group_opstations.port_node_id, group_opstations.recommended_vehicle, group_opstation_items.list_type, group_opstation_items.seq, group_opstation_items.opstation_id, (case when list_type=2 then op_station.name else swm_bins.name end) ops_name"
					+" from "
					+" group_opstations join port_nodes leaf on (leaf.id = group_opstations.port_node_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ?) "
					+" left outer join group_opstation_items on (group_opstation_items.group_opstation_id = group_opstations.id) left outer join op_station on (op_station.id = group_opstation_items.opstation_id and list_type=2) left outer join swm_bins on (swm_bins.id = group_opstation_items.opstation_id and list_type=1)  "
					+" where (? is null or group_opstations.status = ?) order by group_opstations.name, group_opstations.id,  group_opstation_items.list_type, group_opstation_items.seq "
					;
				GET_GROUP_INFO = "select group_opstations.id, group_opstations.name, group_opstations.status, group_opstations.description, group_opstations.port_node_id, group_opstations.recommended_vehicle, group_opstation_items.list_type, group_opstation_items.seq, group_opstation_items.opstation_id, (case when list_type=2 then op_station.name else swm_bins.name end) ops_name "
					+" from "
					+" group_opstations "
					+" left outer join group_opstation_items on (group_opstation_items.group_opstation_id = group_opstations.id) left outer join op_station on (op_station.id = group_opstation_items.opstation_id and list_type=2) left outer join swm_bins on (swm_bins.id = group_opstation_items.opstation_id and list_type=1) where group_opstations.id=? "
					;
				INSERT_GROUP_HEADER = "insert into group_opstations (name, status, description, port_node_id, recommended_vehicle) values (?,?,?,?,?)";
				UPDATE_GROUP_HEADER = "update group_opstations set name = ?, status=?, description = ?, port_node_id = ?, recommended_vehicle = ? where id = ?";
				DELETE_OPSLIST = "delete from group_opstation_items where group_opstation_id = ? and list_type = ?";
				INSERT_OPSLIST = "insert into group_opstation_items (group_opstation_id, list_type, seq, opstation_id) values (?,?,?,?)";
				
			}
		}
	}
}

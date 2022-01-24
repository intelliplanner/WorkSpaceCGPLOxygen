package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;

public class FreezeReport {
	public static java.util.Date getDefaultFreezeDate() {
		java.util.Date dt = new java.util.Date();
		dt.setHours(0);
		dt.setMinutes(0);
		dt.setSeconds(0);
		return dt;
	}
	public static void freezeTable(Connection conn, int pv123, String baseTable, String startTimeCol,long freezeTS, String freezeTable, String primKey) throws Exception {
		if (startTimeCol == null || baseTable == null || baseTable.equals(freezeTable))
			return;
		if (pv123 < 0)
			pv123 = Misc.G_TOP_LEVEL_PORT;
		if (freezeTable == null)
			freezeTable = baseTable+"_freeze";
		if (primKey == null)
			primKey = "id";
		if (freezeTS <= 0) {
			java.util.Date dt = getDefaultFreezeDate();
			
			freezeTS = dt.getTime();
		}
		//1.Get cols of base, Get cols of freeze. Using cols of freeze but excluding vehicle_port_node_id and freeze_ts
		// create query to insert into freeze all rows from baseTable whose startCol < freezeTS and >= vehicle's prev freeze
		HashMap<String, String> colsInBase = new HashMap<String, String>();
		DatabaseMetaData dbmt = conn.getMetaData();
		ResultSet rs = dbmt.getColumns(null, null, baseTable, null);
		while (rs.next()) {
			String col = rs.getString("COLUMN_NAME");
			colsInBase.put(col, col);
		}
		rs = Misc.closeRS(rs);
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		rs = dbmt.getColumns(null, null, baseTable, null);
		boolean isFirst = true;
		sb1.append("insert into ").append(freezeTable).append(" (");
		sb2.append("(select ");
		while (rs.next()) {
			String col = rs.getString("COLUMN_NAME");
			if (!colsInBase.containsKey(col) || primKey.equals(col) || "vehicle_port_node_id".equals(col) || "freeze_ts".equals(col))
				continue;
			if (!isFirst) {
				sb1.append(",");
				sb2.append(",");
			}
			isFirst = false;
			sb1.append(col);
			sb2.append("t.").append(col);
		}
		rs = Misc.closeRS(rs);
		boolean baseTableJoinsWithVehicle = true;//TODO later
		sb1.append(",vehicle_port_node_id, freeze_ts) ");
		sb2.append(",vl.customer_id, ? from ")
		;
		
    sb2.append("(select vehicle.id vehicle_id, vehicle.customer_id, sl.mxts from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ") 
		 .append(" left outer join (select vehicle_id, max(freeze_ts)mxts from ").append(freezeTable).append(" group by vehicle_id) sl on (sl.vehicle_id = vehicle.id) ")
         .append(" ) vl ")
         .append(" join ").append(baseTable).append(" t on (t.vehicle_id=vl.vehicle_id) ")
         .append(" where t.").append(startTimeCol).append(" < ? and t.").append(startTimeCol).append(" >= (case when vl.mxts is null then '1990-01-01' else vl.mxts end) ")
		.append(") ");
		PreparedStatement ps = conn.prepareStatement((sb1.append(sb2)).toString());
		ps.setTimestamp(1, Misc.longToSqlDate(freezeTS));
		ps.setInt(2, pv123);
		ps.setTimestamp(3, Misc.longToSqlDate(freezeTS));
		System.out.println("Freeze Q:"+baseTable+" Thread:"+Thread.currentThread().getId()+":"+ps.toString());
		ps.execute();
		ps = Misc.closePS(ps);
	}
	
	
}

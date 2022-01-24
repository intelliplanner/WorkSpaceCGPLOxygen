package com.ipssi.manualTrip.sand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class Preprocess {
	public static ArrayList<Requirements> preprocess(Connection conn, int portNodeId, String nameStartWith, int materialId, String excludeName) {
		ArrayList<Requirements> dataList = readData(conn, portNodeId, materialId);
		Pair<Map<String, Integer>, Map<Integer, Integer>> pr =getOpsNameToId(conn, portNodeId, nameStartWith, materialId, excludeName); 
		Map<String, Integer> opsNameToId = pr.first;
		preprocess(conn, dataList, opsNameToId);
		return dataList;
	}
	public static void preprocess(Connection conn, ArrayList<Requirements> dataList, Map<String, Integer> opsNameToId) {
		try {
			boolean changed = false;
			for (Requirements req:dataList) {
				changed = req.fixVehicleId(conn) || changed;
				changed = req.fixDateStr(conn) || changed;
				changed = req.fixFromToId(conn, opsNameToId) || changed;
			}
			if (changed)
				saveData(conn, dataList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			
		}
	}

	public static StringBuilder getMissingResult(Connection conn, ArrayList<Requirements> dataList) {
		StringBuilder sb = new StringBuilder();
		for (Requirements req:dataList) {
			if (Misc.isUndef(req.getVehicleId()) || req.getTs() <= 0 || Misc.isUndef(req.getFromId()) || Misc.isUndef(req.getToId())) {
				if (sb.length() == 0) {
					sb.append("<table   border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'><thead><tr><td class='tshb'>Id</td><td class='tshb'>User Data</td><td class='tshb'>Issue</td><td class='tshb'>Processed Data</td></tr></thead><tbody>\n");
				}
				sb.append("<tr>");
				sb.append("<td class='cn'>").append(req.getId()).append("</td><td class='cn'>").append(req.getUserDataString()).append("</td>");
				sb.append("<td class='cn'>").append(req.helperGetDataIssues()).append("</td><td class='cn'>").append(req.getProcessedDataString(conn)).append("</td>");
				sb.append("</tr>\n");
			}
		}
		if (sb.length() != 0)
			sb.append("</tbody></table>");
		return sb;
	}

	public static ArrayList<Requirements> readData(Connection conn, int portNodeId, int materialId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Requirements> retval = new ArrayList<Requirements>();
		try {
			ps = conn.prepareStatement("select mtd.id, mtd.port_node_id, mtd.vehicle_name, mtd.datestr, mtd.from_str, mtd.to_str,mtd.std_name,mtd.count, mtd.vehicle_id,mtd.desired_date, mtd.from_id,mtd.to_id,mta.trip_info_id, mtd.from_id2, mtd.to_id2, mtd.load_change_allowed, mtd.unload_change_allowed, mtd.material_change_allowed, mtd.material_id "+
					" from manual_trip_desired mtd join port_nodes leaf on (leaf.id = mtd.port_node_id) join port_nodes anc on (anc.id=? and anc.lhs_number<=leaf.lhs_number and anc.rhs_number >=leaf.rhs_number) "+
					" left outer join manual_trip_assigned mta on (manual_trip_desired_id = mtd.id)  left outer join trip_info t on (t.id = mta.trip_info_id) where mtd.status=1 and (t.id is null or t.load_gate_op is null or t.unload_gate_op is null) and (? is null or material_id is null or material_id=?) "
					);
			ps.setInt(1, portNodeId);
			Misc.setParamInt(ps, materialId, 2);
			Misc.setParamInt(ps, materialId, 3);
			rs = ps.executeQuery();
			Requirements req = null;
			while (rs.next()) {
				int id = Misc.getRsetInt(rs, 1);
				if (req == null || req.getId() != id) {
					req = new Requirements(Misc.getRsetInt(rs, 1), Misc.getRsetInt(rs, 2), rs.getString(3), rs.getString(4)
							, rs.getString(5), rs.getString(6), Misc.getRsetInt(rs, 8,1), rs.getString(7)
							,Misc.getRsetInt(rs, 9), Misc.sqlToLong(rs.getTimestamp(10)), Misc.getRsetInt(rs, 11), Misc.getRsetInt(rs, 12), Misc.getRsetInt(rs,14), Misc.getRsetInt(rs, 15)
							,1 == Misc.getRsetInt(rs, 16),1 == Misc.getRsetInt(rs, 17),1 == Misc.getRsetInt(rs, 18), Misc.getRsetInt(rs, 19)
							);
					retval.add(req);
				}
				req.addTripId(Misc.getRsetInt(rs, 13));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	public static void saveData(Connection conn, ArrayList<Requirements> datas) {
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			ps = conn.prepareStatement("update manual_trip_desired set std_name=?, count=?, vehicle_id=?, from_id=?, to_id=? where id=?");
			ps2 = conn.prepareStatement("insert into manual_trip_assigned(manual_trip_desired_id, trip_info_id) (select ?,? from dual where not exists(select 1 from manual_trip_assigned where manual_trip_desired_id=? and trip_info_id=?))");
			for (Requirements req : datas) {
				int colIndex = 1;
				ps.setString(colIndex++, req.getStdName());
				Misc.setParamInt(ps, req.getCount(), colIndex++);
				Misc.setParamInt(ps, req.getVehicleId(), colIndex++);
				Misc.setParamInt(ps, req.getFromId(), colIndex++);
				Misc.setParamInt(ps, req.getToId(), colIndex++);
				Misc.setParamInt(ps, req.getId(), colIndex++);
				for (Integer iv: req.getTripId()) {
					Misc.setParamInt(ps2, req.getId(), 1);
					Misc.setParamInt(ps2, iv, 2);
					Misc.setParamInt(ps2, req.getId(), 3);
					Misc.setParamInt(ps2, iv, 4);
					ps2.addBatch();
				}
				ps.addBatch();
			}
			ps.executeBatch();
			ps2.executeBatch();
			ps = Misc.closePS(ps);
			ps2 = Misc.closePS(ps2);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
			ps2 = Misc.closePS(ps2);
		}
	}
	
	public static Pair<Map<String, Integer>, Map<Integer, Integer>> getOpsNameToId(Connection conn, int portNodeId, String nameStartWith, int materialId, String excludeName) {
		Map<String, Integer> retval  = new HashMap<String, Integer>();
		Map<Integer, Integer> opsOfInterest = new HashMap<Integer, Integer>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			for (int art=0;art<2;art++) {
				sb.setLength(0);
				String opmTable = art == 0 ? "opstation_mapping" : "opstation_mapping_addnl";
				sb.append("select distinct op.id, op.name from op_station op join ").append(opmTable).append(" opm on (op.id = opm.op_station_id) join port_nodes leaf on (leaf.id = opm.port_node_id) join port_nodes anc on (anc.id=? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where op.status in (1) and opm.type in (1,2,11,15,16,17,24)");
				if (nameStartWith != null  || materialId > 0)
					sb.append("and (");
				if (nameStartWith != null) {
					sb.append(" op.name like '").append(nameStartWith).append("%'");
				}
				
				
				if (materialId > 0) {
					if (nameStartWith != null) {
						sb.append(" or ");
					}	
					sb.append(" op.material_id=").append(materialId);
				}
				if (nameStartWith != null || materialId > 0)
					sb.append(")");
				if (excludeName != null) {
					sb.append(" and op.name not like '").append(excludeName).append("%'");
				}
				ps = conn.prepareStatement(sb.toString());
				ps.setInt(1, portNodeId);
				rs = ps.executeQuery();
				while (rs.next()) {
					String n = rs.getString(2);
					if (n == null)
						continue;
					n = n.trim();
					if (n.length() == 0)
						continue;
					n = n.toUpperCase();
					int id = rs.getInt(1);
					retval.put(n, id);
					opsOfInterest.put(id, id);
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			//now from opstation_profiles
			sb.setLength(0);
			sb.append("select distinct op.id, op.name from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ")
			.append(" join vehicle_opstation_profiles vop on (vehicle.id = vop.vehicle_id and vehicle.status=1) join opstation_profile_details opd on (opd.opstation_profile_id = vop.opstation_profile_id) join op_station op on (op.id = opd.opstation_id) ")
			;
			if (nameStartWith != null  || materialId > 0)
				sb.append("and (");
			if (nameStartWith != null) {
				sb.append(" op.name like '").append(nameStartWith).append("%'");
			}
			
			
			if (materialId > 0) {
				if (nameStartWith != null) {
					sb.append(" or ");
				}	
				sb.append(" op.material_id=").append(materialId);
			}
			if (nameStartWith != null || materialId > 0)
				sb.append(")");
			if (excludeName != null) {
				sb.append(" and op.name not like '").append(excludeName).append("%'");
			}
			ps = conn.prepareStatement(sb.toString());
			ps.setInt(1, portNodeId);
			rs = ps.executeQuery();
			while (rs.next()) {
				String n = rs.getString(2);
				if (n == null)
					continue;
				n = n.trim();
				if (n.length() == 0)
					continue;
				n = n.toUpperCase();
				int id = rs.getInt(1);
				retval.put(n, id);
				opsOfInterest.put(id, id);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			//now from manual naming
			sb.setLength(0);
			sb.append("select user_name, op_name, opstation_id from manual_opstation_name");
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				String uname = rs.getString(1);
				String oname = rs.getString(2);
				int opid = Misc.getRsetInt(rs,3);
				if (uname == null)
					continue;
				uname = uname.trim();
				if (uname.length() == 0)
					continue;
				uname = uname.toUpperCase();
				if (!Misc.isUndef(opid)) {
					retval.put(uname, opid);
					continue;
				}
				if (oname == null)
					continue;
				oname = oname.trim();
				if (oname.length() == 0)
					continue;
				oname = oname.toUpperCase();
				Integer iv = retval.get(oname);
				if (iv != null) {
					retval.put(uname, iv);
					opsOfInterest.put(iv, iv);
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Map<String, Integer>, Map<Integer, Integer>>(retval, opsOfInterest);
	}
}

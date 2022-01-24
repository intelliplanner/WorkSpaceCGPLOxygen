package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;

public class Helper {
	public static ArrayList<Integer> getNotAllowedShovelTypes(Connection conn, String dumperList, int siteId, int pv123) throws Exception {
		String q1 = "select distinct shovel_type_id from vehicle_type_not_allowed join ( "+
				" select distinct vehicle_types.id vehicle_type_id "+
		" from vehicle join vehicle_types on (vehicle.type = vehicle_types.vehicle_type_lov) join port_nodes anc on (anc.id = vehicle_types.port_node_id) "+
		" join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
		" where vehicle.id in ("+dumperList+") "+
		" 		) nl on (nl.vehicle_type_id = dumper_type_id) ";
		String q2 = "select inv_not_allowed_veh_types.vehicle_type_id from inv_not_allowed_veh_types where inventory_pile_id=?"
				;
		String q = dumperList != null ? q1 +" union "+ q2 : q2;
		PreparedStatement ps = conn.prepareStatement(q);
		int colIndex=  1;
		if (dumperList != null)
			ps.setInt(colIndex++, pv123);
		ps.setInt(colIndex++, siteId);
		ResultSet rs = ps.executeQuery();
		ArrayList<Integer> retval = new ArrayList<Integer>();
		while (rs.next()) {
			retval.add(rs.getInt(1));
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return retval;
	}
	
	public static ArrayList<Integer> getNotAllowedDumperTypes(Connection conn, String shovelList, int siteId, int destId, int pv123) throws Exception {
		String q1 = "select distinct dumper_type_id from vehicle_type_not_allowed join ( "+
		" select distinct vehicle_types.id vehicle_type_id "+
		" from vehicle join vehicle_types on (vehicle.type = vehicle_types.vehicle_type_lov) join port_nodes anc on (anc.id = vehicle_types.port_node_id) "+
		" join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
		" where vehicle.id in ("+shovelList+") "+
		" 		) nl on (nl.vehicle_type_id = shovel_type_id) ";
		
		String q2 = "select inv_not_allowed_veh_types.vehicle_type_id from inv_not_allowed_veh_types where inventory_pile_id=? or inventory_pile_id=?"
		;
		String q = shovelList != null ? q1 +" union "+ q2 : q2;
		PreparedStatement ps = conn.prepareStatement(q);
		int colIndex=  1;
		if (shovelList != null)
			ps.setInt(colIndex++, pv123);
		ps.setInt(colIndex++, siteId);
		ps.setInt(colIndex++, destId);
		ResultSet rs = ps.executeQuery();
		ArrayList<Integer> retval = new ArrayList<Integer>();
		while (rs.next()) {
			retval.add(rs.getInt(1));
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return retval;
	}
	
	public static int findIndex(ArrayList<Integer> theList, int val) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (val == theList.get(i).intValue())
				return i;
		return -1;
	}
	public static boolean removeVal1(ArrayList<Integer> theList, int val) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (val == theList.get(i).intValue()) {
				theList.remove(i);
				return true;
			}
			return false;
	}
	public static boolean isInList(ArrayList<Integer> theList, int val) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (val == theList.get(i).intValue())
				return true;
		return false;
	}
	
	public static void addIfNotExist(ArrayList<Integer> theList, int val) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (val == theList.get(i).intValue())
				return;
		theList.add(val);
	}
	public static void putDBGProp(StringBuilder sb, String propName, int val) {
		if (!Misc.isUndef(val))
			sb.append(" ").append(propName).append("=\"").append(val).append("\"");
	}
	public static void putDBGProp(StringBuilder sb, String propName, String val) {
		if (val != null && val.trim().length() > 0)
			sb.append(" ").append(propName).append("=\"").append(MyXMLHelper.escapedStr(val)).append("\"");
	}
	public static void putDBGProp(StringBuilder sb, String propName, double val) {
		if (!Misc.isUndef(val))
			sb.append(" ").append(propName).append("=\"").append(val).append("\"");
	}
	public static void putDBGProp(StringBuilder sb, String propName, long val) {
		if (val > 0) {
		java.util.Date dt = Misc.longToUtilDate(val);
		String valStr = dt == null ? "null" : (dt.getYear()+1900)+"-"+(dt.getMonth()+1)+"-"+dt.getDate()+" "+dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds();
		sb.append(" ").append(propName).append("=\"").append(valStr).append("\"");
		}
	}
	public static void putDBGProp(StringBuilder sb, String propName, boolean  val) {
		sb.append(" ").append(propName).append("=\"").append(val).append("\"");
	}
	public static void putDBGProp(StringBuilder sb, String propName, ArrayList<Integer> val) {
		if (val != null && val.size() > 0) {
			sb.append(" ").append(propName).append("=\"");
			Misc.convertInListToStr(val, sb);
			sb.append("\" ");
		}
	}
	public static void putDBGPropDtArr(StringBuilder sb, String propName, ArrayList<Long> val) {
		if (val != null && val.size() > 0) {
			sb.append(" ").append(propName).append("=\"");
			for (int i=0,is=val.size();i<is;i++) {
				java.util.Date dt = Misc.longToUtilDate(val.get(i));
				sb.append(dt.getYear()+1900).append("-").append(dt.getMonth()+1).append("-").append(dt.getDate()).append(" ").append(dt.getHours()).append(":").append(dt.getMinutes()).append(":").append(dt.getSeconds());
			}
			sb.append("\" ");
		}
	}
}

package com.ipssi.gen.utils;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class FlexPriv implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private static class PrivItem implements Serializable, Cloneable {
		private static final long serialVersionUID = 1L;

		private int dimId = Misc.getUndefInt();
		//private ColumnMappingHelper colMap;
		private ArrayList<String> vals = new ArrayList<String>();
	}
	private ArrayList<PrivItem> flexPrivList = new ArrayList<PrivItem>();
	public FlexPriv clone() throws CloneNotSupportedException {
		return (FlexPriv) super.clone();
	}
	public String getMenuTag() {
		return menuTag;
	}

	private int orgId = Misc.getUndefInt();
	private int vehicleId = Misc.getUndefInt();
	private String tsStart = null;
	private String tsEnd = null;
	private String menuTag = null;
	public void add(String name, String valList, boolean addToExisting) {
		if (name == null || valList == null)
			return;
		name = name.trim();
		valList = valList.trim();
		if (name.length() == 0 || valList.length() == 0)
			return;
		if (name.equals("pv123") || name.equals("org_id")) {
			this.orgId = Misc.getParamAsInt(valList);
			return;
		}
		if (name.equals("vehicle_id") || name.equals("d20274")) {
			this.vehicleId = Misc.getParamAsInt(valList);
			return;
		}
		if (name.equals("menu_tag")) {
			this.menuTag = valList;
			return;
		}
		
		int dimId = Misc.getParamAsInt(name.startsWith("d") ? name.substring(1) : name.startsWith("pv") ? name.substring(2) : name);
		if (Misc.isUndef(dimId))
			return;
		boolean isStart = Misc.isInList(PageHeader.g_startDateId, dimId);
		boolean isEnd = !isStart ? Misc.isInList(PageHeader.g_endDateId, dimId) : false;
		if (isStart) {
			this.tsStart = valList;
			return;
		}
		if (isEnd) {
			this.tsEnd = valList;
			return;
		}	
		String csv[] = valList.split(",");
		PrivItem priv = null;
		for (int i=0,is=flexPrivList == null ? 0 : flexPrivList.size();i<is;i++) {
			if (flexPrivList.get(i).dimId == dimId) {
				if (addToExisting) {
					priv = flexPrivList.get(i);
				}
				else {
					flexPrivList.remove(i);
				}
				break;
			}
		}
		if (priv == null) {
			priv = new PrivItem();
			priv.dimId = dimId;
			flexPrivList.add(priv);
		}
		for (int i=0,is=csv.length; i<is;i++) {
			priv.vals.add(csv[i]);
		}
	}
	
	public void add(String nameValInURLForm) {
		if (nameValInURLForm == null)
			return;
		String namValPair[] = nameValInURLForm.split("&");
		for (int i=0,is=namValPair.length; i<is; i++) {
			String str = namValPair[i];
			String eqSplit[] = str.split("=");
			String name = eqSplit.length > 1 ? eqSplit[0] : null;
			String valList = eqSplit.length > 1 ? eqSplit[1] : null;
			if (name != null)
				name = name.trim();
			if (valList != null)
				valList = valList.trim();
			if (name == null || name.length() == 0 || valList == null || valList.length() == 0)
				continue;
			this.add(name,valList, false);
		}
	}
	
	public int getOrgId() {
		return this.orgId;
	}
	
	public int getVehicleId() {
		return this.vehicleId;
	}
	
	public String getTsStart() {
		return this.tsStart;
	}
	
	public String getTsEnd() {
		return this.tsEnd;
	}
	
	public String getFilterClauseFromPriv(HashMap<String, String> tablesSeen) {
		StringBuilder sb = new StringBuilder();
		for (int i=0,is=this.flexPrivList.size(); i<is; i++) {
			PrivItem priv = this.flexPrivList.get(i);
			DimInfo dimInfo = DimInfo.getDimInfo(priv.dimId);
			if (dimInfo != null) {
				ColumnMappingHelper colMap = dimInfo.m_colMap;
				if (colMap != null) {
					if (tablesSeen.containsKey(colMap.table)) {
						if (sb.length() != 0)
							sb.append(" and ");
						sb.append("(").append(colMap.table).append(".").append(colMap.column).append(" in (");
						Misc.convertInListOfStrToStr(priv.vals, sb);
						sb.append(")) ");
					}
				}
			}
		}
		return sb.toString();
	}
		
	public static FlexPriv loadFromDB(Connection conn, int userId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		FlexPriv retval = null;
		try {
			ps = conn.prepareStatement("select name, val from users_flex_priv where user_id=? order by name, val");
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			String prevName = null;
			String prevVal = null;
			
			while (rs.next()) {
				String n = rs.getString(1);
				String v = rs.getString(2);
				if (n == null || v == null)
					continue;
				n = n.trim();
				v = v.trim();
				if (n.length() == 0 || v.length() == 0)
					continue;
				if (n.equals(prevName)) {
					if (v.equals(prevVal))
						continue;
				}
				if (retval == null)
					retval = new FlexPriv();
				retval.add(n, v, true);
				prevName = n;
				prevVal = v;
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
}

package com.ipssi.processor.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Region;

public class BranchOffice {
	private static ConcurrentHashMap<Integer, BranchOffice> g_branchOffices = new ConcurrentHashMap<Integer, BranchOffice>();
	private static ConcurrentHashMap<Integer, ArrayList<Integer>> g_branchOfficesByPort = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	private static ConcurrentHashMap<Integer, Integer> g_branchesByOpId = new ConcurrentHashMap<Integer, Integer>();
	
	private int id;
	private String name;
	private int bigRegionId;
	private int garageRegionId;
	private int restRegionId;
	private int portNodeId;
	private int supervisorContactId;
	private int status;
	public BranchOffice(int id, String name, int bigRegionId,
			int garageRegionId, int restRegionId, int portNodeId, int supervisorContactId, int status) {
		super();
		this.id = id;
		this.name = name;
		this.bigRegionId = bigRegionId;
		this.garageRegionId = garageRegionId;
		this.restRegionId = restRegionId;
		this.portNodeId = portNodeId;
		this.supervisorContactId = supervisorContactId;
		this.status = status;
	}
	
	public static void loadBranches(Connection conn, boolean must) throws Exception {
		try {
			if (must) {
				g_branchOffices.clear();
				g_branchOfficesByPort.clear();
			}
			if (g_branchOffices.isEmpty()) {
				PreparedStatement ps = conn.prepareStatement("select id, name, branch_supervisor, big_region_id, wait_region_id, garage_region_id, port_node_id,status from branch_offices where status in (1) order by port_node_id");
				ResultSet rs = ps.executeQuery();
				int prevPortNodeId = Misc.getUndefInt();
				ArrayList<Integer> prevList = null;
				while (rs.next()) {
					BranchOffice branch = new BranchOffice(rs.getInt(1), rs.getString(2), Misc.getRsetInt(rs, 4),
							Misc.getRsetInt(rs, 6), Misc.getRsetInt(rs, 5),Misc.getRsetInt(rs, 7), Misc.getRsetInt(rs, 3), Misc.getRsetInt(rs, 8));
					if (branch.getPortNodeId() != prevPortNodeId)
						prevList = null;
					if (prevList == null) {
						prevList = new ArrayList<Integer>();
						g_branchOfficesByPort.put(branch.getPortNodeId(),prevList);
					}
					synchronized (prevList) {
						prevList.add(branch.getId());
					}
					g_branchOffices.put(branch.getId(), branch);
					prevPortNodeId = branch.getPortNodeId();
				}
				rs.close();
				ps.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static BranchOffice getBranchOffice(Connection conn, int branchId) throws Exception {
		loadBranches(conn, false);
		return g_branchOffices.get(branchId);
	}
	
	public static ArrayList<Integer> getBranchesForPortNodeId(Connection conn, int portNodeId) throws Exception {
		loadBranches(conn, false);
		Cache cache = Cache.getCacheInstance(conn);
		for (MiscInner.PortInfo curr = cache.getPortInfo(portNodeId, conn); curr != null; curr = curr.m_parent) {
			ArrayList<Integer> atThisLevel = g_branchOfficesByPort.get(curr.m_id);
			if (atThisLevel != null && atThisLevel.size() != 0)
				return atThisLevel;
		}
		return null;
	}
	
	public static BranchOffice getBranchForPoint(Connection conn, Point pt, int portNodeId) throws Exception {
		loadBranches(conn, false);
		int retval = Misc.getUndefInt();
		double currMin = Misc.LARGE_NUMBER;
		boolean minInside = false;
		ArrayList<RegionTestHelper> rtl = RegionTest.getRegionsContaining(conn, pt, portNodeId, Misc.getUndefInt());
		ArrayList<Integer> branchList = BranchOffice.getBranchesForPortNodeId(conn, portNodeId);
		if (branchList != null) {
			for (Integer bid : branchList) {
				BranchOffice branch = getBranchOffice(conn, bid);
				boolean isInside = false;
				RegionTestHelper bigregion = RegionTest.getRegionInfo(branch.getBigRegionId(), conn);
				if (bigregion == null)
					continue;
				if (rtl != null && rtl.size() !=0 ) {
					for (RegionTestHelper rt:rtl) {
						Region r = rt.region;
						if (r.id == branch.getBigRegionId()) {
							isInside = true;
							break;
						}
					}
				}
				if (minInside && !isInside) 
					continue;
				double d= pt.distance(bigregion.region.getCenter());
				if ((!minInside && isInside) || (d < currMin)) {
					currMin = d;
					retval = branch.getId();
					minInside = isInside;
				}
			}
		}
		return g_branchOffices.get(retval);
	}
	
	public static BranchOffice getBranchForRegion(Connection conn, Region r, int portNodeId) throws Exception {
		Point pt = r.getCenter();
		if (Misc.isUndef(portNodeId))
			portNodeId = r.getPortNodeId();
		
		return getBranchForPoint(conn, pt, portNodeId);
	}
	
	public static BranchOffice getBranchForRegion(Connection conn, int regionId, int portNodeId) throws Exception {
		RegionTestHelper rth =   RegionTest.getRegionInfo(regionId, conn);
		Point pt = rth.region.getCenter();
		if (Misc.isUndef(portNodeId))
			portNodeId = rth.region.getPortNodeId();
		return getBranchForPoint(conn, pt, portNodeId);
	}
	public static BranchOffice getBranchForOpstation(Connection conn, int opStationId) throws Exception {
		Integer retvalInt = g_branchesByOpId.get(opStationId);
		if (retvalInt == null) {
			retvalInt = new Integer(populateBranchIdForOpStation(conn, opStationId, false));
		}
		else {
			return getBranchOffice(conn, retvalInt.intValue());
		}
		if (retvalInt != null && !Misc.isUndef(retvalInt)) {
			g_branchesByOpId.put(opStationId, retvalInt);
			return getBranchOffice(conn, retvalInt.intValue());
		}
		return null;
	}
	public static void populateBranchIdForAllOpStation(Connection conn, boolean update)  {//use this in home.jsp to do a full pop upfront
		try {
			loadBranches(conn, false);
			PreparedStatement ps = conn.prepareStatement("select  op_station.id, opstation_mapping.port_node_id, (regions.lowerY+regions.upperY)/2 y, (regions.lowerX+regions.upperX)/2 x "+
" from op_station join opstation_mapping on (op_station_id = op_station.id)) join regions on (regions.id = op_station.gate_reg_id) left outer join branch_offices on (op_station.branch_id = branch_offices.id) "+
" where (branch_offices.id is null or branch_offices.status <> 1) and op_station.status=1 ");
			ResultSet rs = ps.executeQuery();
			PreparedStatement ps2 = conn.prepareStatement("update op_station set branch_id = ? where id =?");
			boolean added = false;
			while (rs.next()) {
				int opId = rs.getInt(1);
				double y = rs.getDouble(3);
				double x = rs.getDouble(4);
				int portNodeId = rs.getInt(2);
				int branchId = populateBranchIdForOpStation(conn, opId, new Point(x,y), false, portNodeId);
				if (update && !Misc.isUndef(branchId)) {
					ps2.setInt(1, branchId);
					ps2.setInt(2, opId);
					added = true;
					ps2.addBatch();
				}
			}
			
			rs.close();
			ps.close();
			if (added)
				ps2.executeBatch();
			ps2.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	private static int populateBranchIdForOpStation(Connection conn, int opStationId, boolean update) throws Exception  {//
		PreparedStatement ps = conn.prepareStatement("select  op_station.id, opstation_mapping.port_node_id, (regions.lowerY+regions.upperY)/2 y, (regions.lowerX+regions.upperX)/2 x, op_station.branch_id "+
				" from op_station join opstation_mapping on (op_station.id = ? and op_station_id = op_station.id)) join regions on (regions.id = op_station.gate_reg_id) left outer join branch_offices on (op_station.branch_id = branch_offices.id) "+
				" ");
		ps.setInt(1, opStationId);
		ResultSet rs = ps.executeQuery();
		double x = 0;
		double y = 0;
		int portNodeId = Misc.G_TOP_LEVEL_PORT;
		int retval = Misc.getUndefInt();
		if (rs.next()) {
			int opId = rs.getInt(1);
			y = rs.getDouble(3);
			x = rs.getDouble(4);
			portNodeId = rs.getInt(2);
			retval = Misc.getRsetInt(rs, 5);
		}
		rs.close();
		ps.close();
		BranchOffice bo = g_branchOffices.get(retval);
		if (!update && bo != null)
			return retval;
		return populateBranchIdForOpStation(conn, opStationId, new Point(x,y), true, portNodeId);
	}
	
	private static int populateBranchIdForOpStation(Connection conn, int opStationId, Point pt, boolean update, int portNodeId) throws Exception  {
		loadBranches(conn, false);
		BranchOffice bo = getBranchForPoint(conn, pt, portNodeId);
		int retval = bo == null ? Misc.getUndefInt() : bo.getId();
				
		if (update) {
			PreparedStatement ps = conn.prepareStatement("update op_station set branch_id = ? where id =?");
			ps.setInt(1, retval);
			ps.setInt(2, opStationId);
			ps.execute();
			ps.close();
		}
		return retval;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getBigRegionId() {
		return bigRegionId;
	}
	public void setBigRegionId(int bigRegionId) {
		this.bigRegionId = bigRegionId;
	}
	public int getGarageRegionId() {
		return garageRegionId;
	}
	public void setGarageRegionId(int garageRegionId) {
		this.garageRegionId = garageRegionId;
	}
	public int getRestRegionId() {
		return restRegionId;
	}
	public void setRestRegionId(int restRegionId) {
		this.restRegionId = restRegionId;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}

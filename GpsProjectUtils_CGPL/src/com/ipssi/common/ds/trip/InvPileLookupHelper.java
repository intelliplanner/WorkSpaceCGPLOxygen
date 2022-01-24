package com.ipssi.common.ds.trip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.ShiftPlanCurrent;
import com.ipssi.cache.ShiftPlanHistory;
import com.ipssi.cache.ShiftPlanMgmt;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.miningOpt.SiteStats;
import com.ipssi.miningOpt.SiteStats.InvPile;
import com.ipssi.geometry.Region;
import java.sql.*;

public class InvPileLookupHelper {
	public static void setCurrPlanId(int planId, long planLiveTS) {//TODO - Tanuj to call this whenever plan made live
		
	}
	public static void setInitShovelRouteAssignment(int shovelId, int routeId) {//TODO - Tanuj to provide implementation of this
		//will be called at first LGIN at a shovel post assignment ... thereafter will not be called for that particular shovel
		
	}
	public static ArrayList<MiscInner.Pair> initShovelRouteAssignment(Connection conn) {//first = shovelId, second = routedId
		ArrayList<MiscInner.Pair> retval = new ArrayList<MiscInner.Pair>();
		ArrayList<ShiftPlanCurrent> currPlan = ShiftPlanMgmt.getCurrentPlan();
		if (currPlan == null || currPlan.size() == 0)
			return retval;
		int currPlanId = currPlan.get(0).getShiftTargetId();
		long currPlanTS = currPlan.get(0).getTimestamp();
		//resort the current plan based loadSite and then numShovels .. so that we can assign in round robin manner
		ArrayList<HelpRouteToShovelAssignment> theList = HelpRouteToShovelAssignment.init(currPlan);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_CURR_SHOVELPOS);
			rs = ps.executeQuery();
			while (rs.next()) {
				int shovelId = rs.getInt(1);
				double lon = rs.getDouble(2);
				double lat = rs.getDouble(3);
				long ts = Misc.sqlToLong(rs.getTimestamp(4));
				ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(shovelId);
				if (shovelInfo != null) {
					int pileId = InvPileLookupHelper.getInvPileId(conn, currPlanTS, lon, lat, shovelInfo, null, true);
					HelpRouteToShovelAssignment.addShovel(theList, shovelId, pileId);
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			for (int i=0,is=theList.size();i<is;i++) {
				int routeId = theList.get(i).routeId;
				for (int j=0,js=theList.get(i).shovelList.size();j<js;j++) {
					retval.add(new MiscInner.Pair(theList.get(i).shovelList.get(j), routeId));
				}
			}
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
	
	
	public static int getInvPileId(Connection conn, long ts, double lon, double lat, ShovelSequenceHolder shovelInfo, ThreadContextCache threadContextCache, boolean setInCurr) {
		
		int retval = Misc.getUndefInt();
		try {
			SiteStats.loadInvPileOnly(conn, false);
			ShiftPlanHistory plan = ShiftPlanMgmt.getShiftPlan(ts);
			if (plan == null)
				return retval;
			InvLookupInfo lookupInfo = null;
			if (Misc.isUndef(retval) && shovelInfo != null) {
				lookupInfo = setInCurr ? shovelInfo.getCurrPlanInvLookupShovelPos() : shovelInfo.getLastInvLookup();
				if (lookupInfo != null) {
					if (lookupInfo.isStillValid(plan.getShiftTargetId(), lon, lat)) {
						retval = lookupInfo.getInventoryId();
					}
				}
			}
			lookupInfo = null;
			if (Misc.isUndef(retval) && threadContextCache != null) {
				lookupInfo = threadContextCache.getLastInvLookup();
				if (lookupInfo != null) {
					if (lookupInfo.isStillValid(plan.getShiftTargetId(), lon, lat)) {
						retval = lookupInfo.getInventoryId();
					}
				}
			}
			lookupInfo = null;
			if (Misc.isUndef(retval)) {
				PlanInventoryInfo planInfo = PlanInventoryInfo.getPlanInfo(conn, plan.getShiftTargetId(), plan.getTimestamp(), plan.getLoadSiteIdList());
				Pair<SiteStats.InvPile, Region> invInfo = planInfo.getAppropRegion(lon, lat);
				SiteStats.InvPile inv = invInfo == null ? null : invInfo.first;
				Region vornoi = invInfo == null ? null : invInfo.second;
				if (inv != null) {
					lookupInfo = new InvLookupInfo(plan.getShiftTargetId(), plan.getTimestamp(), lon, lat, inv.getId(), inv, vornoi);
					retval = inv.getId();
				}
			}
			if (shovelInfo != null) {
				if (setInCurr)
					shovelInfo.setCurrPlanInvLookupShovelPos(lookupInfo);
				else
					shovelInfo.setLastInvLookup(lookupInfo);
			}
			if (threadContextCache != null)
				threadContextCache.setLastInvLookup(lookupInfo);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	private static String GET_CURR_SHOVELPOS = "select vehicle.id, lgd.longitude, lgd.latitude, lgd.gps_record_time from vehicle join "+
	   " (select vehicle_id, attribute_id,max(gps_record_time) grt from logged_data where attribute_id=0 group by vehicle_id,attribute_id) mx "+
	   " on (vehicle.id = mx.vehicle_id) join logged_data lgd on (lgd.vehicle_id = vehicle_id and lgd.attribute_id=0 and lgd.gps_record_time = mx.grt) "+
	   " where vehicle.status in (1) and vehicle.type in () ";
	public static class InvLookupInfo { //one will be kept at ShovelSequenceHolder and one at ThreadContextCache
		private int planId;
		private long planTS;
		private double lon;
		private double lat;
		private int inventoryId;
		private SiteStats.InvPile pileInfo;
		private Region vornoiRegion;
		public final static double  NO_RELOOKUP = 0.07;
		public boolean isStillValid(int planId, double lon, double lat) throws Exception {
			boolean retval = false;
			if (planId == this.planId) {
				double dist = Point.fastGeoDistance(lon, lat, this.lon, this.lat);
				if (dist < NO_RELOOKUP)
					retval = true;
				else {
					retval = PlanInventoryInfo.isInInvPile(lon, lat, this.pileInfo, this.vornoiRegion);
				}
			}
			return retval;
		}
		public int getPlanId() {
			return planId;
		}
		public void setPlanId(int planId) {
			this.planId = planId;
		}
		public long getPlanTS() {
			return planTS;
		}
		public void setPlanTS(long planTS) {
			this.planTS = planTS;
		}
		public double getLon() {
			return lon;
		}
		public void setLon(double lon) {
			this.lon = lon;
		}
		public double getLat() {
			return lat;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
		public int getInventoryId() {
			return inventoryId;
		}
		public void setInventoryId(int inventoryId) {
			this.inventoryId = inventoryId;
		}
		public SiteStats.InvPile getPileInfo() {
			return pileInfo;
		}
		public void setPileInfo(SiteStats.InvPile pileInfo) {
			this.pileInfo = pileInfo;
		}
		public Region getVornoiRegion() {
			return vornoiRegion;
		}
		public void setVornoiRegion(Region vornoiRegion) {
			this.vornoiRegion = vornoiRegion;
		}
		public InvLookupInfo(int planId, long planTS, double lon, double lat,
				int inventoryId, InvPile pileInfo, Region vornoiRegion) {
			super();
			this.planId = planId;
			this.planTS = planTS;
			this.lon = lon;
			this.lat = lat;
			this.inventoryId = inventoryId;
			this.pileInfo = pileInfo;
			this.vornoiRegion = vornoiRegion;
		}
		
	}
	
	private static class HelpRouteToShovelAssignment implements Comparable<HelpRouteToShovelAssignment> {
		public int loadSiteId;
		public int numShovels;
		public int routeId;
		public ArrayList<Integer> shovelList = new ArrayList<Integer>();
		public HelpRouteToShovelAssignment(int loadSiteId, int numShovels, int routeId) {
			this.loadSiteId = loadSiteId;
			this.numShovels = numShovels;
			this.routeId = routeId;
		}
		public int compareTo(HelpRouteToShovelAssignment arg0) {
			// TODO Auto-generated method stub
			int diff = this.loadSiteId-arg0.loadSiteId;
			if (diff == 0)
				diff = -1*(this.numShovels-arg0.numShovels);
			if (diff == 0)
				diff = this.routeId-arg0.routeId;
			return diff;
		}
		public static ArrayList<HelpRouteToShovelAssignment> init(ArrayList<ShiftPlanCurrent> currPlan) {
			ArrayList<HelpRouteToShovelAssignment> retval = new ArrayList<HelpRouteToShovelAssignment>();
			for (int i=0,is=currPlan.size();i<is;i++) {
				retval.add(new HelpRouteToShovelAssignment(currPlan.get(i).getLoadSiteId(), currPlan.get(i).getNumShovels(), currPlan.get(i).getRouteId()));
			}
			Collections.sort(retval);
			return retval;
		}
		public static void addShovel(ArrayList<HelpRouteToShovelAssignment> theList, int shovelId, int loadSiteId) {
			int addToIndex = -1;
			int currWorstGap = Misc.getUndefInt();
			for (int i=0,is=theList.size();i<is;i++) {
				HelpRouteToShovelAssignment item = theList.get(i);
				if (item.loadSiteId == loadSiteId) {
					int gap = item.numShovels-item.shovelList.size();
					if (addToIndex < 0 || gap > currWorstGap) {
						addToIndex = i;
						currWorstGap = gap;
					}
				}
			}
			if (addToIndex >= 0) {
				theList.get(addToIndex).shovelList.add(shovelId);
			}
		}
	}
}

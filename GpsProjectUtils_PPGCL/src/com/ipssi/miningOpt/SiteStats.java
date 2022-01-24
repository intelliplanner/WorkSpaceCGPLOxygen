package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.Region;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.common.ds.trip.TripInfoConstants;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;


public class SiteStats {
	
	private static double DEFAULT_CYCLE_TIME = 20;
	private static double DEFAULT_FILL_FACTOR = (6.0*3.0)/15; //Actual # of cycles = ideal cycles * fill factor
	
	private static double DEFAULT_FWD_DIFFICULTY = 16.0/20.0; //Actual speed = ideal speed * difficulty
	private static double DEFAULT_BACK_DIFFICULTY = 18.0/20.0;
	
	private static double DEFAULT_DIST_MULT = 1.4;
	private static double DEFAULT_DIST = 1;
	private static int DEFAULT_UNLOAD_SEC = 42;
	
	private boolean getSiteIdInsteadOfUop = true;
	private boolean getVehicleTypeIdInsteadOf9097 = true;
	private long refTS = -1;
	private boolean getStatsFromRef = false;
	private static String g_tripInfoTableReg = "trip_info";
	private static String g_loggedDataTableReg = "logged_data";
	private static String g_tripInfoTableRef = "trip_info_sim_ref";
	private static String g_loggedDataTableRef = "logged_data_sim_ref";
	public long getRefTS() {
		return refTS;
	}
	public boolean doSiteStatLeadByAdj = false;//should be false by default
	public double loTravelSecFrac = 0.6;
	public double hiTravelSecFrac = 1.6;
	public double stdDevRange = 1.5;
	public SiteStats(Connection conn, long refTS, boolean getStatsFromRef, boolean getSiteIdInsteadOfUop, boolean getVehicleTypeIdInsteadOf9097) throws Exception {
		this.getStatsFromRef = getStatsFromRef;
		if (refTS <= 0) {
			PreparedStatement ps = conn.prepareStatement("select max(combo_start) from "+(this.getStatsFromRef ? g_tripInfoTableRef : g_tripInfoTableReg));
			ResultSet rs=  ps.executeQuery();
			if (rs.next()) {
				refTS = Misc.sqlToLong(rs.getTimestamp(1));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		this.refTS = refTS;
		this.getSiteIdInsteadOfUop = getSiteIdInsteadOfUop;
		this.getVehicleTypeIdInsteadOf9097 = getVehicleTypeIdInsteadOf9097;
	}
	//result is returned in XML form:
	
	//<data>
	//   <inv id="1" fill_factor="0.9">
	//      <avg_cycle_time shovel_type="1" val="20"/>
	//      <avg_cycle_time shovel_type="1" val="20"/>
	//      <route dest_id="1" dist="2.4" forw_diff="1.1" back_diff="1.2"/>
	// 
  //     </inv>
  //     <shovel id="1" fill factor="0.2" nv_id='2"
  //        rest same as inv
 //       </shovel>
 //</data>
	public StringBuilder getXMLForStats(Connection conn, int portNodeId) throws Exception {//load capacity is called separately
		StringBuilder sb = new StringBuilder();
		analyzeAndCalcStats(conn, portNodeId);
		sb.append("<data>\n");
		ArrayList<CapacityDef> shovelTypes = this.getTypesOfCat(1);
		Collection<InvPile> pileList = g_invPiles.values();
		for (Iterator<InvPile> iter = pileList.iterator();iter.hasNext();) {
			InvPile pile = iter.next();
			if (!pile.isAvailableLoad(refTS))
				continue;
			Stats stats = this.m_statsForInv.get(pile.id);
			if (stats == null)
				continue;
			sb.append("<inv id=\"").append(pile.id).append("\" fill_factor=\"").append(stats.getAvgFillFactor())
			.append("\" nc=\"").append(19.0/3.0*stats.getAvgFillFactor())
			.append("\">\n");
			helperAddStatsDetails(sb, stats, shovelTypes) ;
			sb.append("</inv>\n");
		}
		ArrayList<CapacityDef> forParticularShovelTypeList = new ArrayList<CapacityDef>();
		forParticularShovelTypeList.add(null);
		Collection<SiteStats.CachedInvPileForShovel> shovelList =  SiteStats.g_cachedInvForShovel.values();
		for (CachedInvPileForShovel shovel:shovelList) {
			int shovelId = shovel.shovelId;
			//<shovel id="1" fill factor="0.2" nv_id='2"
			Stats stats = this.m_statsForShovelPos.get(shovelId);
			if (stats == null)
				continue;
			sb.append("<shovel id=\"").append(shovelId).append("\" fill_factor=\"").append(stats.getAvgFillFactor()).append("\" inv_id=\"").append(stats.invPileId).append("\">\n");
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(shovelId, conn);
			int ty = vehSetup == null ? Misc.getUndefInt() : vehSetup.m_type;
			forParticularShovelTypeList.set(0, this.getCapacityDefBy9097(ty));
			helperAddStatsDetails(sb, stats, forParticularShovelTypeList) ;
			sb.append("</shovel>\n");
		}
		sb.append("</data>");
		return sb;
	}
	//<data><dumper ty="1" val="20"/></data>
	public StringBuilder getXMLForUnloadStats(Connection conn, int portNodeId) throws Exception {//load capacity is called separately
		StringBuilder sb = new StringBuilder();
		this.loadInvPilesCapDefShovelInfo(conn, portNodeId, true, false, this.refTS); //load Shovel from regular position
		ArrayList<CapacityDef> dumperTypes = this.getTypesOfCat(0);
		sb.append("<data>");
		for(int i=0,is=dumperTypes.size();i<is;i++) {
			sb.append("<dumper ty=\"").append(dumperTypes.get(i).type9097).append("\" val=\"").append(dumperTypes.get(i).keyMetric2).append("\"/>");
		}
		sb.append("</data>");
		return sb;
	}
	
	
	public static CapacityDef getCapacityDefBy9097(int type9097) {
		for (CapacityDef item:g_capacityDefs)
			if (item.type9097 == type9097)
				return item;
		return null;
	}
	
	public static CapacityDef getCapacityDefById(int id) {
		for (CapacityDef item:g_capacityDefs)
			if (item.id == id)
				return item;
		return null;
	}
	
	public static double getAvgCapacityVol(int vehicleCat) {
		int n = 0;
		double v = 0;
		for (CapacityDef item: SiteStats.g_capacityDefs) {
			if (item.cat == vehicleCat && !Misc.isUndef(item.capVol)) {
				v += item.capVol;
				n++;
			}
		}
		return n == 0 ? Misc.getUndefDouble() : v/n;
	}
	public static ArrayList<InvPile> getSiteList(int pileType, long refTS) {
		ArrayList<InvPile> retval = new ArrayList<InvPile>();
		Collection<InvPile> pileList = g_invPiles.values();
		for (InvPile pile:pileList) {
			if (pile.pileType == pileType && pile.isAvailable(refTS))
				retval.add(pile);
		}
		return retval;
		
	}
	public static double getAvgKeyMetric2(int vehicleCat) {
		int n = 0;
		double v = 0;
		for (CapacityDef item: SiteStats.g_capacityDefs) {
			if (item.cat == vehicleCat && !Misc.isUndef(item.keyMetric2)) {
				v += item.keyMetric2;
				n++;
			}
		}
		return n == 0 ? Misc.getUndefDouble() : v/n;
	}
	public static double getAvgKeyMetric(int vehicleCat) {
		int n = 0;
		double v = 0;
		for (CapacityDef item: SiteStats.g_capacityDefs) {
			if (item.cat == vehicleCat && !Misc.isUndef(item.keyPerformanceMetric)) {
				v += item.keyPerformanceMetric;
				n++;
			}
		}
		return n == 0 ? Misc.getUndefDouble() : v/n;
	}
	public Stats getStatsForInvPile(int forId, Connection conn, int portNodeId) throws Exception {
		analyzeAndCalcStats(conn, portNodeId);
		Stats retval = this.m_statsForInv.get(forId);
		return retval;
	}
	public Stats getStatsForShovel(int forId, Connection conn, int portNodeId) throws Exception {
		analyzeAndCalcStats(conn, portNodeId);
		Stats retval = this.m_statsForShovelPos.get(forId);
		return retval;
	}

	
	
	private static class CachedInvPileForShovel {
		int shovelId;
		double prevCalcLon = Misc.getUndefDouble();
		double prevCalcLat = Misc.getUndefDouble();
		long prevCalcTS = -1;
		int invPile = Misc.getUndefInt();
		public CachedInvPileForShovel(int shovelId) {
			this.shovelId = shovelId;
		}
		public CachedInvPileForShovel(int shovelId, double lon, double lat, long ts, int invPile) {
			this.shovelId = shovelId;
			this.prevCalcLon = lon;
			this.prevCalcLat = lat;
			this.prevCalcTS = invPile;
			this.invPile = invPile;
		}
	}
	private static ConcurrentHashMap<Integer, CachedInvPileForShovel> g_cachedInvForShovel = new ConcurrentHashMap<Integer,CachedInvPileForShovel>();
	public static ArrayList<Integer> getShovelsByInvPileId(Connection conn, int pileId) {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		Collection<CachedInvPileForShovel> posList = g_cachedInvForShovel.values();
		for (CachedInvPileForShovel shovelInfo : posList) {
			try {
				getSetCachedPosForShovel(conn, shovelInfo.shovelId, -1);
				if (shovelInfo.invPile == pileId)
					retval.add(shovelInfo.shovelId);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
		}
		return retval;
	}
	public static int getInvPileIdByShovelId(Connection conn, int shovelId) {
		CachedInvPileForShovel pos = null;
		try {
			pos = getSetCachedPosForShovel(conn, shovelId, -1);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return pos == null ? Misc.getUndefInt() : pos.invPile;
	}

	private static CachedInvPileForShovel getSetCachedPosForShovel(Connection conn, int shovelId, long refTS) throws Exception {
		CachedInvPileForShovel pos = g_cachedInvForShovel.get(shovelId);
		if (pos == null) {
			pos = new CachedInvPileForShovel(shovelId);
			g_cachedInvForShovel.put(shovelId, pos);
		}
		if (refTS <= 0 && pos.prevCalcTS > 0) {
			//check if last calculated is near curr time
			if (Math.abs(System.currentTimeMillis()-pos.prevCalcTS) < 5*60*1000)
				return pos;
		}
		VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, shovelId, false,false);
		double lon = Misc.getUndefDouble();
		double lat = Misc.getUndefDouble();
		if (vdf != null) {
			synchronized (vdf) {
				NewVehicleData vdt = vdf.getDataList(conn, shovelId, 0, false);
				if (vdt != null) {
					GpsData pt = refTS <= 0 ? vdt.getLast(conn) : vdt.get(conn, new GpsData(refTS));
					if (pt != null) {
						lon = pt.getLongitude();
						lat = pt.getLatitude();
						if (refTS <= 0)
							refTS = pt.getGps_Record_Time();
					}
				}
			}
		}
		if (Misc.isUndef(lon) || Misc.isUndef(lat) || Misc.isEqual(lon, 0) || Misc.isEqual(lat, 0))
			return pos;
		boolean similar = pos.prevCalcTS <= 0 || refTS <= 0 || Math.abs(pos.prevCalcTS-refTS) < 300000;
		similar = similar && !Misc.isUndef(pos.prevCalcLon) && !Misc.isUndef(pos.prevCalcLat) && Math.abs(lon-pos.prevCalcLon) < 0.0005 && Math.abs(lat-pos.prevCalcLat) < 0.0005;
		pos.prevCalcTS = refTS;
		if (similar) {
			return pos;
		}
		pos.prevCalcLon = lon;
		pos.prevCalcLat = lat;
		pos.invPile = SiteStats.getInventoryPile(conn, lon, lat, refTS);
		return pos;	
	}
	public static Triple<Double, Double, Integer> getSimpleInventoryPileForShovel(int shovelId) {
		CachedInvPileForShovel retval = SiteStats.g_cachedInvForShovel.get(shovelId);
		if (retval != null) {
			return new Triple<Double,Double,Integer>(retval.prevCalcLon, retval.prevCalcLat, retval.invPile);
		}
		return null;
	}
	public static int getInventoryPileForShovel(Connection conn, int shovelId, long refTS) throws Exception {
		CachedInvPileForShovel retval = SiteStats.getSetCachedPosForShovel(conn, shovelId, refTS);
		return retval == null ? Misc.getUndefInt() : retval.invPile;
	}
	public static int getInventoryPile(Connection conn, double lon, double lat, long refTS) throws Exception {//@#@#@#@#
		int nearestIdInside = Misc.getUndefInt();
		int nearestIdOutside = Misc.getUndefInt();
		if (Misc.isUndef(lon) || Misc.isUndef(lat) || refTS <= 0)
			return Misc.getUndefInt();
		if (!Misc.isUndef(lon)) {
			double distFromCenter = Double.MAX_VALUE;
			double distFromEdge = Double.MAX_VALUE;
			Collection<InvPile> pileList = g_invPiles.values();
			for (InvPile pile:pileList) {
				if (!pile.isAvailableLoad(refTS))
					continue;
				boolean isIn = false;
				double dist = Double.MAX_VALUE;
				if (pile.reg != null) {
					isIn = RegionTest.PointIn(conn, new Point(lon, lat), pile.reg.region);
					if (!isIn) 
						dist = pile.reg.region.getShortestDistFromEdge(lon, lat);
					else {
						Point ctr = pile.reg.region.getCenter();
						dist = Point.fastGeoDistance(lon, lat, ctr.getLongitude(), ctr.getLatitude());
					}
				}
				else {
					dist = Point.fastGeoDistance(lon, lat, pile.lon, pile.lat);
					isIn = dist <= pile.radius;
				}
				if (isIn) {
					if (distFromCenter > dist) {
						distFromCenter = dist;
						nearestIdInside = pile.id;
					}
				}
				else {
					if (distFromEdge > dist) {
						distFromEdge = dist;
						nearestIdOutside = pile.id;
					}
				}
			}
		}
		return (!Misc.isUndef(nearestIdInside)) ? nearestIdInside : nearestIdOutside; 
	}
	
	public static void loadInvPilesCapDefShovelInfo(Connection conn, int portNodeId, boolean force, boolean doFromRef, long refTS) throws Exception {
		if (force || !g_invPileLoaded) {
			TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());
			loadInvPileFromDB(conn, portNodeId);
			loadCapacityDefs(conn, portNodeId);
			
			
		}
		if (force || !g_invPileLoaded || g_shovelPosLoadedFromRef != doFromRef) {
			ArrayList<CapacityDef> shovelTypes = getTypesOfCat(1);
			loadShovelPos(conn, portNodeId, shovelTypes, doFromRef);
			loadAvgUnloadTime(conn, doFromRef, portNodeId, refTS);
		}
		g_shovelPosLoadedFromRef = doFromRef;
		g_invPileLoaded = true;
	}
	public void loadInvPileCapDefEtc(Connection conn, int portNodeId) throws Exception {
		loadInvPilesCapDefShovelInfo(conn, portNodeId, true, this.getStatsFromRef, this.refTS);
		loadUopEtc(conn, portNodeId);
	}
	public void loadUopEtc(Connection conn, int portNodeId) throws Exception {
		//1. Load Capacity of different types of dumpers and shovels - capacity and vehicle are linked through vehicle type 9097
		//2. Load current inventory pile definition
		//3. Load current shovel pos
		if (otherMetaLoaded)
			return;
		this.uopList = this.getListOfAllUops(conn, portNodeId);
		
		otherMetaLoaded = true;
	}
	
	private void helperAddStatsDetails(StringBuilder sb, Stats stats, ArrayList<CapacityDef> shovelTypes) {
		for (CapacityDef shovelType:shovelTypes) {
			sb.append("<avg_cycle_time shovel_type=\"").append(shovelType == null ? Misc.getUndefInt() : this.getVehicleTypeIdInsteadOf9097 ? shovelType.id : shovelType.type9097).append("\" val=\"").append(stats.getCycleTime(shovelType == null ? Misc.getUndefInt() : shovelType.type9097)).append("\"/>\n");
		}
		
		for (OpStationBean bean:uopList) {
			//<route dest_id="1" dist="2.4" forw_diff="1.1" back_diff="1.2"/>
			int destId = bean.getOpStationId();
			if (this.getSiteIdInsteadOfUop)
				destId = TripInfoCacheHelper.getSiteForOpstationid(destId);
			sb.append("<route dest_id=\"").append(destId).append("\"")
			.append(" dist=\"").append(stats.getDestLeadDist(destId)).append("\"")
			.append(" forw_diff=\"").append(stats.getForwRouteDifficulty(destId)).append("\"")
			.append(" back_diff=\"").append(stats.getBackRouteDifficulty(destId)).append("\"")
			.append(" forw_time=\"").append(stats.getDestLeadDist(destId)*3600/(18*stats.getForwRouteDifficulty(destId))).append("\"")
			.append(" back_time=\"").append(stats.getDestLeadDist(destId)*3600/(18*stats.getBackRouteDifficulty(destId))).append("\"")
			
			.append("/>\n");
		}
	}
	
	private boolean tripsLoadedAndAnalyzed = false;
	private boolean dbgPrintStatCalc = false;
	private int dbgInvPileId = Misc.getUndefInt();
	private String dbgStatOf = null;
	
	private void analyzeAndCalcStats(Connection conn, int portNodeId) throws Exception {
		if (tripsLoadedAndAnalyzed)
			return;
		loadInvPileCapDefEtc(conn, portNodeId);
		//4. get trips and add it to possible trips for inventory and shovel. Trips of last X (4 days) are loaded similtaneously
		//    then added to 0 or more inventory/shovel but first by classiffying in two dimensions of how recent relative to refTS
		//    and how far. For inventory piles if it is in region then 0 dist, else dist from edge.
		//   We have an upper limit on distance and upper limiit on time
		//   Finally we get wt average of different time by creating an index of time+dist - different wts for different i+j 
		//  We will stop looking at progressively worse i+j once we have seen enough trips
		
		//5, Then for inventory piles and shovels for which we dont have any meaningful stats, we populate average (from 'raw type nos' or from inv piles of shovel)
		
		
		this.loadRelevantTrips(conn, portNodeId); //load trips and assign to inv piles/shovel pos
		//summarize and get stats
		Collection<InvPile> pileList = g_invPiles.values();
		for (Iterator<InvPile> iter = pileList.iterator();iter.hasNext();) {
			InvPile pile = iter.next();
			if (!pile.isAvailableLoad(refTS))
				continue;
			
			double lon = pile.lon;
			double lat = pile.lat;
			if (Misc.isUndef(lon)) {
				RegionTestHelper reg = pile.reg;
				if (reg != null) {
					Point pt = reg.region.getCentroid();
					if (pt != null) {
						lon = pt.getLongitude();
						lat = pt.getLatitude();
					}
				}
			}
			ArrayList<TripLink> tripLink = this.m_tripsForInvPiles.get(pile.id);
			if (tripLink == null) {
				tripLink = new ArrayList<TripLink>();
				this.m_tripsForInvPiles.put(pile.id, tripLink);
			}
			dbgPrintStatCalc = true;
			dbgInvPileId = pile.id;
			
			
			Stats stats = getStatsForTripsLinked(conn, tripLink, lon, lat);
			this.m_statsForInv.put(pile.id, stats);
		}
		dbgPrintStatCalc = false;
		
		Collection<SiteStats.CachedInvPileForShovel> shovelList =  SiteStats.g_cachedInvForShovel.values();
		for (CachedInvPileForShovel shovel:shovelList) {
			
			int shovelId = shovel.shovelId;
			if (Misc.isUndef(shovelId) || Misc.isUndef(shovel.prevCalcLon) || Misc.isUndef(shovel.prevCalcLat)) 
				continue;
			ArrayList<TripLink> tripLink = this.m_tripsForShovelPos.get(shovelId);
			if (tripLink == null) {
				tripLink = new ArrayList<TripLink>();
				this.m_tripsForShovelPos.put(shovelId, tripLink);
			}
			Stats stats = getStatsForTripsLinked(conn, tripLink, shovel.prevCalcLon, shovel.prevCalcLat);
			stats.invPileId = shovel.invPile;
			this.m_statsForShovelPos.put(shovelId, stats);
		}
		this.populateMissingStatsForInventory(conn);
		this.populateMissingStatsForShovels(conn);
		this.fixFor9097UopForInventory(conn);
		this.fixFor9097UopForShovels(conn);
		tripsLoadedAndAnalyzed = true;
	}
	public Stats getSimpleStatsForInvPile(int forId)  {
		return this.m_statsForInv.get(forId);
	}
	private void populateMissingStatsForInventory(Connection conn) {
		Stats avgAcrossAllInv = new Stats();
		this.doAvgAcross(avgAcrossAllInv, this.m_statsForInv);
		Collection<InvPile> pileList = g_invPiles.values();
		for (Iterator<InvPile> iter = pileList.iterator();iter.hasNext();) {
			InvPile pile = iter.next();
			if (!pile.isAvailableLoad(refTS))
				continue;
			Stats stats = this.m_statsForInv.get(pile.id);
			if (stats == null) {
				stats = new Stats();
				this.m_statsForInv.put(pile.id, stats);
			}
			if (stats.avgCycleTime.size() == 0 && avgAcrossAllInv.avgCycleTime.size() > 0) {
				stats.avgCycleTime.add(new Pair<Integer, Double>(Misc.getUndefInt(), avgAcrossAllInv.avgCycleTime.get(0).second));
			}
			double fromLon = pile.lon;
			double fromLat = pile.lat;
			if (pile.reg != null) {
				Point ctr = pile.reg.region.getCentroid();
				fromLon = ctr.getLongitude();
				fromLat = ctr.getLatitude();
			}
			fillDistForMissingUopDist(stats, conn, fromLon, fromLat);
			if (stats.forwAvgRouteDifficulty.size() == 0 && avgAcrossAllInv.forwAvgRouteDifficulty.size() > 0)
				stats.forwAvgRouteDifficulty.add(new Pair<Integer, Double>(Misc.getUndefInt(), avgAcrossAllInv.forwAvgRouteDifficulty.get(0).second));
			if (stats.backAvgRouteDifficulty.size() == 0 && avgAcrossAllInv.backAvgRouteDifficulty.size() > 0)
				stats.backAvgRouteDifficulty.add(new Pair<Integer, Double>(Misc.getUndefInt(), avgAcrossAllInv.backAvgRouteDifficulty.get(0).second));
		}
	}
	
	private void fixFor9097UopForInventory(Connection conn) {
		Collection<Stats> statList = m_statsForInv.values();
		for (Stats stats:statList) {
			this.helperFixStatsFor9097UopId(stats);
		}
	}
	
	private void populateMissingStatsForShovels(Connection conn) throws Exception {
		Collection<SiteStats.CachedInvPileForShovel> shovelList =  SiteStats.g_cachedInvForShovel.values();
		for (CachedInvPileForShovel shovel:shovelList) {
			int shovelId = shovel.shovelId;
			int invPileId = shovel.invPile;
			
			Stats stats = this.m_statsForShovelPos.get(shovelId);
			if (stats == null) {
				stats = new Stats();
				this.m_statsForShovelPos.put(shovelId, stats);
			}
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(shovelId, conn);
			
			CapacityDef capacityDef = this.getCapacityDefBy9097(vehSetup == null ? Misc.getUndefInt() : vehSetup.m_type);
			int shovelType = capacityDef == null ? Misc.getUndefInt() : capacityDef.type9097;
			double defaultCycleTime = capacityDef == null ? SiteStats.DEFAULT_CYCLE_TIME : capacityDef.keyPerformanceMetric;
			Stats invStats = this.getSimpleStatsForInvPile(invPileId);
			if (stats.avgCycleTime.size() == 0) {
				if (invStats != null)
					defaultCycleTime = invStats.getCycleTime(shovelType);
				stats.avgCycleTime.add(new Pair<Integer, Double>(Misc.getUndefInt(), defaultCycleTime));
			}
			if (Misc.isUndef(stats.avgFillFactor) && invStats != null)
				stats.avgFillFactor = invStats.getAvgFillFactor();//get will appropriately default it
			if (invStats != null) {
				populateUopStatsIfMissingFromOther(stats,invStats);
			}
			double fromLon = shovel.prevCalcLon;
			double fromLat = shovel.prevCalcLat;
			fillDistForMissingUopDist(stats, conn, fromLon, fromLat);
			if (stats.forwAvgRouteDifficulty.size() == 0)
				stats.forwAvgRouteDifficulty.add(new Pair<Integer, Double>(Misc.getUndefInt(), SiteStats.DEFAULT_FWD_DIFFICULTY));
			if (stats.backAvgRouteDifficulty.size() == 0)
				stats.backAvgRouteDifficulty.add(new Pair<Integer, Double>(Misc.getUndefInt(), SiteStats.DEFAULT_BACK_DIFFICULTY));
		}
	}
	private void fixFor9097UopForShovels(Connection conn) throws Exception {
		Collection<SiteStats.CachedInvPileForShovel> shovelList =  SiteStats.g_cachedInvForShovel.values();
		for (CachedInvPileForShovel shovel:shovelList) {
			int shovelId = shovel.shovelId;
			Stats stats = this.m_statsForShovelPos.get(shovelId);
			this.helperFixStatsFor9097UopId(stats);
		}
	}
	private void helperFixStatsFor9097UopId(Stats stats) {
		if (this.getVehicleTypeIdInsteadOf9097) {
			for (int i=stats.avgLoadTime.size()-1;i>=0;i--) {
				Triple<Integer,Integer,Double> entry = stats.avgLoadTime.get(i);
				int shovelType = entry.first;
				int dumperType = entry.second;
				boolean toRemove = false;
				if (!Misc.isUndef(shovelType)) {
					CapacityDef def = this.getCapacityDefBy9097(shovelType);
					if (def == null) 
						toRemove = true;
					else
						shovelType = def.id;
				}
				if (!Misc.isUndef(dumperType)) {
					CapacityDef def = this.getCapacityDefBy9097(dumperType);
					if (def == null)
						toRemove = true;
					else
						dumperType = def.id;
				}
				if (toRemove) {
					stats.avgLoadTime.remove(i);
				}
				else {
					entry.first = shovelType;
					entry.second = dumperType;
				}
			}
			for (int i=stats.avgCycleTime.size()-1;i>=0;i--) {
				Pair<Integer,Double> entry = stats.avgCycleTime.get(i);
				int shovelType = entry.first;
				boolean toRemove = false;
				if (!Misc.isUndef(shovelType)) {
					CapacityDef def = this.getCapacityDefBy9097(shovelType);
					if (def == null) 
						toRemove = true;
					else
						shovelType = def.id;
				}
				if (toRemove) {
					stats.avgLoadTime.remove(i);
				}
				else {
					entry.first = shovelType;
				}
			}
		}
		if (this.getSiteIdInsteadOfUop) {
			for (int art=0;art<3;art++) {
				ArrayList<Pair<Integer, Double>> uopbasedList = art == 0 ? stats.uopLeadDist : art == 1 ? stats.forwAvgRouteDifficulty : stats.backAvgRouteDifficulty;
				for (int i=uopbasedList.size()-1;i>=0;i--) {
					Pair<Integer,Double> entry = uopbasedList.get(i);
					int uopId = entry.first;
					boolean toRemove = false;
					if (!Misc.isUndef(uopId)) {
						uopId = TripInfoCacheHelper.getSiteForOpstationid(uopId);
						if (Misc.isUndef(uopId)) {
							toRemove = true;
						}
					}
					if (toRemove)
						uopbasedList.remove(i);
					else
						entry.first = uopId;
				}
			}//for each uopbased list
		}//if to convert site
	}
	private List<OpStationBean> getListOfAllUops(Connection conn, int portNodeId) throws Exception {
		List<OpStationBean> opsList = new ArrayList<OpStationBean>();
		if (true) {
			ArrayList<InvPile> sites = SiteStats.getSiteList(2, this.refTS);
			for (InvPile pile:sites) {
				int beanId = TripInfoCacheHelper.getOpstationIdForSite(pile.id);
				OpStationBean bean = TripInfoCacheHelper.getOpStation(beanId);
				if (bean != null)
					opsList.add(bean);
			}
		}
		else {
			ArrayList<Integer> uopTypes = new ArrayList<Integer>();
			uopTypes.add(TripInfoConstants.UNLOAD);
			uopTypes.add(TripInfoConstants.PREFERRED_UNLOAD_HIPRIORITY);
			uopTypes.add(TripInfoConstants.PREFERRED_UNLOAD_LOWPRIORITY);
			opsList =  TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, portNodeId, uopTypes, 0, null);
		}
		return opsList;
	}
	
	
	private void fillDistForMissingUopDist(Stats stats, Connection conn, double fromLon, double fromLat) {
		for (OpStationBean bean:uopList) {
			Pair<Integer, Double> entryDist = null;
			for (int i=0,is=stats.uopLeadDist.size(); i<is; i++) {
				if (stats.uopLeadDist.get(i).first == bean.getOpStationId()) {
					entryDist = stats.uopLeadDist.get(i);
					break;
				}
			}
			if (entryDist == null) {
				double d = DEFAULT_DIST;
				if (!Misc.isUndef(fromLon)) {
					int regId = bean.getGateAreaId();
					RegionTestHelper rth = RegionTest.getRegionInfo(regId, conn);
					if (rth != null) {
						Point ctr = rth.region.getCentroid();
						d = ctr.fastGeoDistance(fromLon, fromLat)*SiteStats.DEFAULT_DIST_MULT;
					}
				}
				stats.uopLeadDist.add(new Pair<Integer, Double>(bean.getOpStationId(), d));
			}
		}
	}
	
	private void populateUopStatsIfMissingFromOther(Stats stats, Stats otherStats) {
		for (OpStationBean bean:uopList) {
			for (int art=0;art<3;art++) {
				Pair<Integer, Double> entryDist = null;
				ArrayList<Pair<Integer, Double>> statsDataList = art == 0 ? stats.uopLeadDist : art == 1 ? stats.forwAvgRouteDifficulty : stats.backAvgRouteDifficulty;
				ArrayList<Pair<Integer, Double>> otherDataList = art == 0 ? otherStats.uopLeadDist : art == 1 ? otherStats.forwAvgRouteDifficulty : otherStats.backAvgRouteDifficulty;
				for (int i=0,is=statsDataList.size(); i<is; i++) {
					if (statsDataList.get(i).first == bean.getOpStationId()) {
						entryDist = statsDataList.get(i);
						break;
					}
				}
				if (entryDist == null) {
					for (int i=0,is=otherDataList.size(); i<is; i++) {
						if (otherDataList.get(i).first == bean.getOpStationId()) {
							entryDist = otherDataList.get(i);
							break;
						}
					}
					if (entryDist != null) {
						statsDataList.add(new Pair<Integer, Double>(entryDist.first, entryDist.second));
					}
				}//if not in stats
			}//for art 
		}//all op
	}
	private void doAvgAcross(Stats avgResult, HashMap<Integer, Stats> statsMap) {
		
		double avgCycleTime = 0;
		double avgFillFactor = 0;
		double avgLoadTime = 0;
		double forwAvgRouteDifficulty = 0;
		double backAvgRouteDifficulty = 0;
		double uopLeadDist = 0;
		
		double navgCycleTime = 0;
		double navgFillFactor = 0;
		double navgLoadTime = 0;
		double nforwAvgRouteDifficulty = 0;
		double nbackAvgRouteDifficulty = 0;
		double nuopLeadDist = 0;
		
		Collection<Stats> statsList = statsMap.values();
		for (Iterator<Stats> iter= statsList.iterator(); iter.hasNext(); ) {
			Stats stat = iter.next();
			for (int i=0,is=stat.avgCycleTime.size();i<is;i++) {
				if (!Misc.isUndef(stat.avgCycleTime.get(i).second)) {
					avgCycleTime += stat.avgCycleTime.get(i).second;
					navgCycleTime++;
				}
			}
			if (!Misc.isUndef(stat.avgFillFactor)) {
				avgCycleTime += stat.avgFillFactor;
				navgFillFactor++;
			}
			for (int i=0,is=stat.forwAvgRouteDifficulty.size();i<is;i++) {
				if (!Misc.isUndef(stat.forwAvgRouteDifficulty.get(i).second)) {
					forwAvgRouteDifficulty += stat.forwAvgRouteDifficulty.get(i).second;
					nforwAvgRouteDifficulty++;
				}
			}
			for (int i=0,is=stat.backAvgRouteDifficulty.size();i<is;i++) {
				if (!Misc.isUndef(stat.backAvgRouteDifficulty.get(i).second)) {
					backAvgRouteDifficulty += stat.backAvgRouteDifficulty.get(i).second;
					nbackAvgRouteDifficulty++;
				}
			}
			for (int i=0,is=stat.uopLeadDist.size();i<is;i++) {
				if (!Misc.isUndef(stat.uopLeadDist.get(i).second)) {
					uopLeadDist += stat.uopLeadDist.get(i).second;
					nuopLeadDist++;
				}
			}
		}
		Stats stat = avgResult;
		for (int i=0,is=stat.avgCycleTime.size();i<is;i++) {
			if (!Misc.isUndef(stat.avgCycleTime.get(i).second)) {
				avgCycleTime += stat.avgCycleTime.get(i).second;
				navgCycleTime++;
			}
		}
		if (!Misc.isUndef(stat.avgFillFactor)) {
			avgCycleTime += stat.avgFillFactor;
			navgFillFactor++;
		}
		for (int i=0,is=stat.forwAvgRouteDifficulty.size();i<is;i++) {
			if (!Misc.isUndef(stat.forwAvgRouteDifficulty.get(i).second)) {
				forwAvgRouteDifficulty += stat.forwAvgRouteDifficulty.get(i).second;
				nforwAvgRouteDifficulty++;
			}
		}
		for (int i=0,is=stat.backAvgRouteDifficulty.size();i<is;i++) {
			if (!Misc.isUndef(stat.backAvgRouteDifficulty.get(i).second)) {
				backAvgRouteDifficulty += stat.backAvgRouteDifficulty.get(i).second;
				nbackAvgRouteDifficulty++;
			}
		}
		for (int i=0,is=stat.uopLeadDist.size();i<is;i++) {
			if (!Misc.isUndef(stat.uopLeadDist.get(i).second)) {
				uopLeadDist += stat.uopLeadDist.get(i).second;
				nuopLeadDist++;
			}
		}
		
		//now update avgResult
		if (navgCycleTime > 0.1) {
			avgResult.avgCycleTime.clear();
			avgResult.avgCycleTime.add(new Pair<Integer, Double>(Misc.getUndefInt(), avgCycleTime/navgCycleTime));
		}
		if (navgFillFactor > 0.1) {
			avgResult.avgFillFactor = avgFillFactor/navgFillFactor;
		}
		if (nforwAvgRouteDifficulty > 0.1) {
			avgResult.forwAvgRouteDifficulty.clear();
			avgResult.forwAvgRouteDifficulty.add(new Pair<Integer, Double>(Misc.getUndefInt(), forwAvgRouteDifficulty/nforwAvgRouteDifficulty));
		}

		if (nbackAvgRouteDifficulty > 0.1) {
			avgResult.backAvgRouteDifficulty.clear();
			avgResult.backAvgRouteDifficulty.add(new Pair<Integer, Double>(Misc.getUndefInt(), backAvgRouteDifficulty/nbackAvgRouteDifficulty));
		}
		if (nuopLeadDist > 0.1) {
			avgResult.uopLeadDist.clear();
			avgResult.uopLeadDist.add(new Pair<Integer, Double>(Misc.getUndefInt(), uopLeadDist/nuopLeadDist));
		}
	}
	
	
	public static class TripInfo {
		int id;
		long lgin;
		double lgapSec;
		double loadLon;
		double loadLat;
		int shovelId;
		int uopId;
		double loadSec;
		double cycleTime;
		double numCycles;
		double unloadLon;
		double unloadLat;
		double forwLeadKM;
		double backLeadKM;
		double forwSec;
		double backSec;
		double postLoadSec;
		double unloadSec;
		int dumperId;
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Shovel:").append(this.shovelId).append(" ,Uop:").append(this.uopId).append(" ,Lead:").append(this.forwLeadKM).append(" #cycles:").append(this.numCycles).append(" ,CycleDur:").append(this.cycleTime);
			return sb.toString();
		}
	}
	
	private static String g_tripQ = "select t.id, lop.vehicle_id shovel_id,  loadlog.longitude load_lon, loadlog.latitude load_lat, t.vehicle_id dumper_id, t.unload_gate_op "+
	",timestampdiff(second, t.load_gate_in, t.load_gate_out) lgap, timestampdiff(second, t.load_gate_out, t.adj_lgout) postlgap"+
	",timestampdiff(second, t.load_gate_out, t.unload_gate_in) forwleadtime, (case when timestampdiff(second, t.adj_lgout, t.unload_gate_in)*1.3 < timestampdiff(second, t.unload_gate_out, nt.adj_lgin) then timestampdiff(second, t.load_gate_out, t.unload_gate_in) else  timestampdiff(second, t.unload_gate_out, nt.load_gate_in) end) backleadtime "+
	",timestampdiff(second, t.adj_lgout, t.unload_gate_in) adjforwleadtime, (case when timestampdiff(second, t.adj_lgout, t.unload_gate_in)*1.3 < timestampdiff(second, t.unload_gate_out, nt.adj_lgin) then timestampdiff(second, t.adj_lgout, t.unload_gate_in) else  timestampdiff(second, t.unload_gate_out, nt.adj_lgin) end) adjbackleadtime "+
	",timestampdiff(second, t.unload_gate_in, t.unload_gate_out) unloadtime "+
	",t.unload_gate_op, unloadlog.longitude unload_lon, unloadlog.latitude unload_lat "+
	",(unloadlog.attribute_value-loadlog.attribute_value) loadleaddist, (ntloadlog.attribute_value-unloadlog.attribute_value) unloadleaddist "+
	",t.load_gate_in, t.shovel_cycles, (case when t.avg_shovel_cycle_dur_excl_peaks > 0 then t.avg_shovel_cycle_dur_excl_peaks else t.avg_shovel_cycle_dur end) shovel_cycle_dur"+
	" from vehicle join (select distinct vehicle.id vehicle_id from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id=? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)) vi on (vehicle.id = vi.vehicle_id) join @trip_info t on (t.vehicle_id = vehicle.id) left outer join @trip_info nt on (t.next_trip_id=nt.id) "+
	" join op_station lop on (lop.id = t.load_gate_op) "+
	" join op_station uop on (uop.id = t.unload_gate_op) "+
	" join @logged_data loadlog on (loadlog.vehicle_id = t.vehicle_id and loadlog.attribute_id=0 and loadlog.gps_record_time = t.adj_lgin) "+
	" join @logged_data unloadlog on (unloadlog.vehicle_id = t.vehicle_id and unloadlog.attribute_id=0 and unloadlog.gps_record_time = t.unload_gate_in) "+
	" join @logged_data ntloadlog on (ntloadlog.vehicle_id = t.vehicle_id and ntloadlog.attribute_id=0 and ntloadlog.gps_record_time = nt.adj_lgin) "+
	" where t.load_gate_op is not null and t.unload_gate_op is not null and t.combo_start >= ? and t.combo_start < ? @uopClause order by t.combo_start desc "
	;
	public static class CapacityDef {
		int id;
		int cat; //shovel or dumper;
		int type9097;
		double capWt;
		double capVol;
		double keyPerformanceMetric;//if shovel cycle time, if dumper speed
		double keyMetric2;//if dumper unloading time (specifically for dala)
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getCat() {
			return cat;
		}
		public void setCat(int cat) {
			this.cat = cat;
		}
		public int getType9097() {
			return type9097;
		}
		public void setType9097(int type9097) {
			this.type9097 = type9097;
		}
		public double getCapWt() {
			return capWt;
		}
		public void setCapWt(double capWt) {
			this.capWt = capWt;
		}
		public double getCapVol() {
			return capVol;
		}
		public void setCapVol(double capVol) {
			this.capVol = capVol;
		}
		public double getKeyPerformanceMetric() {
			return keyPerformanceMetric;
		}
		public void setKeyPerformanceMetric(double keyPerformanceMetric) {
			this.keyPerformanceMetric = keyPerformanceMetric;
		}
		public double getKeyMetric2() {
			return keyMetric2;
		}
		public void setKeyMetric2(double keyMetric2) {
			this.keyMetric2 = keyMetric2;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Id:").append(this.id).append(" ,Cat:").append(this.cat).append(" ,ty9097:").append(this.type9097).append(" CapVol:").append(this.capVol).append(" ,Metric:").append(this.keyPerformanceMetric).append(" ,Metric2:").append(this.keyMetric2);
			return sb.toString();
		}
		public CapacityDef(int id, int cat, int type9097, double capWt, double capVol, double keyPerformanceMetric) {
			this.id = id;
			this.cat = cat;
			this.type9097 = type9097;
			this.capWt = capWt;
			this.capVol = capVol;
			this.keyPerformanceMetric = keyPerformanceMetric;
			this.keyMetric2 = DEFAULT_UNLOAD_SEC;
		}
	}
	private static String g_qForCapacityDef = "select distinct vehicle_types.id, vehicle_cat, vehicle_type_lov, capacity_wt, capacity_vol, cycle_time_second from vehicle_types "+
	" join port_nodes leaf on (leaf.id = vehicle_types.port_node_id) join port_nodes anc on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number)) "+
	" where anc.id = ? and vehicle_types.status in (1,2) order by vehicle_cat, vehicle_type_lov, vehicle_types.id"
	;
	private List<OpStationBean> uopList = null;
	public List<OpStationBean> getUopList() {
		return this.uopList;
	}
	public ArrayList<Integer> getDestIdList() {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		for (OpStationBean bean:uopList) {
			int destid = this.getSiteIdInsteadOfUop ? TripInfoCacheHelper.getSiteForOpstationid(bean.getOpStationId()) : bean.getOpStationId();
			if (!Misc.isUndef(destid)) {
				retval.add(destid);
			}
		}
		return retval;
	}
	private static ArrayList<CapacityDef> g_capacityDefs = new ArrayList<CapacityDef>();
	private static void loadCapacityDefs(Connection conn, int portNodeId) throws Exception {
		g_capacityDefs.clear();
		PreparedStatement ps = conn.prepareStatement(g_qForCapacityDef);
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			CapacityDef item = new CapacityDef(rs.getInt("id"), rs.getInt("vehicle_cat"), rs.getInt("vehicle_type_lov"), Misc.getRsetDouble(rs, "capacity_wt"), Misc.getRsetDouble(rs, "capacity_vol"), Misc.getRsetDouble(rs, "cycle_time_second"));
			g_capacityDefs.add(item);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	
	public static ArrayList<CapacityDef> getTypesOfCat(int cat) {
		ArrayList<CapacityDef> retval = new ArrayList<CapacityDef>();
		for (CapacityDef item:g_capacityDefs)
			if (item.cat == cat)
				retval.add(item);
		return retval;
	}
	
	public static class InvPile {
		int id;
		double lon;
		double lat;
		double radius;
		RegionTestHelper reg;
	    double difficultyFactor;
	    double clearingTime;
	    double clearingCycles;
	    double positioningTime;
	    long startTime;
	    long endTime;
	    int status;
	    int pileType;
	    int benchId;
	    int directionId;
	   String name;
	   public String getName() {
		   return name;
	   }
	    public int getBenchId() {
			return benchId;
		}
		public void setBenchId(int benchId) {
			this.benchId = benchId;
		}
		public int getDirectionId() {
			return directionId;
		}
		public void setDirectionId(int directionId) {
			this.directionId = directionId;
		}
		public double getPositioningTime() {
			return positioningTime;
		}
		public int getId() {
			return id;
		}
		public double getLon() {
			if (this.getReg() != null) {
				Point pt = this.getReg().region.getCenter();
				return pt.getLongitude();
			}
			return lon;
		}
		public double getLat() {
			if (this.getReg() != null) {
				Point pt = this.getReg().region.getCenter();
				return pt.getLatitude();
			}
			return lat;
		}
		public double getRadius() {
			return radius;
		}
		public RegionTestHelper getReg() {
			return reg;
		}
		public double getDifficultyFactor() {
			return difficultyFactor;
		}
		public double getClearingTime() {
			return clearingTime;
		}
		public double getClearingCycles() {
			return clearingCycles;
		}
		public long getStartTime() {
			return startTime;
		}
		public long getEndTime() {
			return endTime;
		}
		public int getStatus() {
			return status;
		}
		public int getPileType() {
			return pileType;
		}
		public InvPile(int id, double lon, double lat, double radius, RegionTestHelper reg, double difficultyFactor, double clearingTime, double clearingCycles, long startTime, long endTime, int status, int pileType, double positioningTime, int benchId, int directionId, String name) {
			this.name = name;
	    	this.id = id;
	    	this.lon = lon;
	    	this.lat = lat;
	    	if (radius > 10) //hack confusion
	    		radius = radius/1000.0;
	    	this.radius = radius;
	    	this.difficultyFactor = difficultyFactor;
	    	this.reg = reg;
	    	this.clearingTime = clearingTime;
	    	this.clearingCycles = clearingCycles;
	    	this.startTime = startTime;
	    	this.endTime = endTime;
	    	this.status = status;
	    	this.pileType = pileType;
	    	this.positioningTime = positioningTime;
	    	this.benchId = benchId;
	    	this.directionId = directionId;
	    }
	    public boolean isAvailable(long ts) {
	    	return true;//return (this.startTime <= ts || this.startTime <= 0) && (this.endTime > ts || this.endTime <= 0) && this.status == 1;
	    }
	    public boolean isAvailableLoad(long ts) {
	    	return true;//return this.pileType == 1 && (this.startTime <= ts || this.startTime <= 0) && (this.endTime > ts || this.endTime <= 0) && this.status == 1;
	    }
	    public boolean isAvailableUnload(long ts) {
	    	return true;//return this.pileType == 2 && (this.startTime <= ts || this.startTime <= 0) && (this.endTime > ts || this.endTime <= 0) && this.status == 1;
	    }
	}
	private static ConcurrentHashMap<Integer, InvPile> g_invPiles = new ConcurrentHashMap<Integer, InvPile>();
	private static String g_qForInvPiles = "select distinct dos_inventory_piles.id, (case when lm.lowerX is not null then lm.lowerX else longitude end) " +
			"lon, (case when lm.lowerY is not null then lm.lowerY else latitude end) lat, (case when length is not null and width is not null then (length+width)/2 when length is not null then length else width end) rad, " +
			"region_id, op_difficulty, clearing_time, clearing_cycle_time,  dos_inventory_piles.create_date, dos_inventory_piles.close_date, dos_inventory_piles.status, dos_inventory_piles.pile_type, dos_inventory_piles.positioning_time, dos_inventory_piles.bench_id, dos_inventory_piles.direction_id, dos_inventory_piles.short_code from dos_inventory_piles "+
	" join port_nodes leaf on (leaf.id = dos_inventory_piles.port_node_id) join port_nodes anc on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number)) "+
	" left outer join landmarks lm on (lm.id = dos_inventory_piles.landmark_id) " +
	" where anc.id = ?  and dos_inventory_piles.status in (1,2) order by dos_inventory_piles.id"
	;
	public static void loadInvPileOnly(Connection conn, boolean force) throws Exception {
		if (force || !g_newInvPileOnlyLoaded) {
			g_newInvPileOnlyLoaded = true;
			loadInvPileFromDB(conn, 2);
		}
	}
	public static void loadInvPileCapacityDefOnly(Connection conn, boolean force) throws Exception {
		if (force || !SiteStats.g_invPileLoaded) {
			loadInvPileFromDB(conn, 2);
			SiteStats.loadCapacityDefs(conn, 2);
			g_invPileLoaded = true;
		}
	}
	private static void loadInvPileFromDB(Connection conn, int portNodeId) throws Exception {
		g_invPiles.clear();
		PreparedStatement ps = conn.prepareStatement(g_qForInvPiles);
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int regionId = Misc.getRsetInt(rs, "region_id");
			RegionTestHelper reg = null;
			if (!Misc.isUndef(regionId)) {
				reg = RegionTest.getRegionInfo(regionId, conn);
			}
			InvPile item = new InvPile(Misc.getRsetInt(rs, "id"), Misc.getRsetDouble(rs, "lon"), Misc.getRsetDouble(rs, "lat"), Misc.getRsetDouble(rs, "rad"), reg, Misc.getRsetDouble(rs, "op_difficulty"), Misc.getRsetDouble(rs, "clearing_time"), Misc.getRsetDouble(rs, "clearing_cycle_time"), Misc.sqlToLong(rs.getTimestamp("create_date")), Misc.sqlToLong(rs.getTimestamp("close_date")), Misc.getRsetInt(rs, "status"), Misc.getRsetInt(rs, "pile_type"), Misc.getRsetDouble(rs, "positioning_time"), Misc.getRsetInt(rs, "bench_id"), Misc.getRsetInt(rs, "direction_id"), Misc.getRsetString(rs, "short_code",null)) ;
			g_invPiles.put(item.id,item);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		
	}
	
	public static InvPile getInvPile(int id) {
		return g_invPiles.get(id);
	}
	private static volatile boolean g_newInvPileOnlyLoaded = false;
	private static volatile boolean g_invPileLoaded = false;
	private static volatile boolean g_shovelPosLoadedFromRef = false;
	private boolean otherMetaLoaded = false;
	private static void loadShovelPos(Connection conn, int portNodeId, ArrayList<CapacityDef> shovelTypes, boolean doFromRef) throws Exception {
		SiteStats.g_cachedInvForShovel.clear();
		StringBuilder sb = new StringBuilder();
		sb.append("select vehicle.id, lgd.longitude, lgd.latitude, lgd.grt from vehicle join (select distinct vehicle.id vehicle_id from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) ")
		.append(" join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)) vi on (vi.vehicle_id = vehicle.id) ")
		.append(" left outer join (select ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(".vehicle_id, ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(".longitude, ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(".latitude, mx.grt from ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(" join (select vehicle_id, attribute_id, max(gps_record_time) grt from ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append("  where attribute_id=0 group by vehicle_id,attribute_id) mx on (").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(".vehicle_id = mx.vehicle_id and ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(".attribute_id=0 and ").append(doFromRef ? g_loggedDataTableRef : g_loggedDataTableReg).append(".gps_record_time=mx.grt)) lgd ")
		.append(" on (lgd.vehicle_id = vehicle.id) ")
		.append(" where vehicle.type in (");
		for (int i1=0,i1s=shovelTypes.size();i1<i1s;i1++) {
			if (i1 != 0)
				sb.append(",");
			sb.append(shovelTypes.get(i1).type9097);
		}
		sb.append(") ")
		;
		
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			double lon = Misc.getRsetDouble(rs, 2);
			double lat = Misc.getRsetDouble(rs, 3);
			long ts = Misc.sqlToLong(rs.getTimestamp(4));
			int invPile = SiteStats.getInventoryPile(conn, lon, lat, ts);
			SiteStats.CachedInvPileForShovel cachedPos = new SiteStats.CachedInvPileForShovel(id, lon,lat,ts,invPile);
			SiteStats.g_cachedInvForShovel.put(id, cachedPos);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	
	private static class TripLink implements Comparable {
		int timeCat;
		int distCat;
		ArrayList<TripInfo> tripList = new ArrayList<TripInfo>();
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("timeCat:").append(this.timeCat).append(" ,distCat:").append(this.distCat).append(" ,#trips:").append(this.tripList.size());
			return sb.toString();
		}

		public TripLink(int timeCat, int distCat) {
			this.timeCat = timeCat;
			this.distCat = distCat;
		}
		public static MiscInner.PairIntBool getPosOrAddAt(ArrayList<TripLink> theList, TripLink toAdd) {
			boolean found = false;
			int addAfter = 0;
			for(int is=theList.size();addAfter<is && theList.get(addAfter).compareTo(toAdd) < 0;addAfter++) {
				//do nothing
			}
			if (addAfter != theList.size() && theList.get(addAfter).compareTo(toAdd) == 0)
				found = true;
			return new MiscInner.PairIntBool(addAfter, found);
		}
		public static int compareTo(int lhsTimeCat, int lhsDistCat, int rhsTimeCat, int rhsDistCat) {
			int l = lhsTimeCat+lhsDistCat;
			int r = rhsTimeCat+rhsDistCat;
			int gap = l-r;
			if (gap == 0)
				gap = lhsTimeCat-rhsTimeCat;
			return gap;
		}
		public int compareTo(Object arg0) {
			TripLink rhs = (TripLink) arg0;
			return compareTo(this.timeCat, this.distCat, rhs.timeCat, rhs.distCat);
		}
	}
	
	private HashMap<Integer, ArrayList<TripLink>> m_tripsForInvPiles = new HashMap<Integer, ArrayList<TripLink>>();
	private HashMap<Integer, ArrayList<TripLink>> m_tripsForShovelPos = new HashMap<Integer, ArrayList<TripLink>>();

	 int dbgcnt = 0;
	private void classifyAndAddTrip(Connection conn, TripInfo trip) throws Exception {
		int timeCat = 0;
		int minGap = (int)((refTS-trip.lgin)/(1000*60));
		StringBuilder dbgSB = null;//new StringBuilder();
		
		for (;timeCat<g_minuteThresh.length && g_minuteThresh[timeCat] <= minGap;timeCat++) {
			//DO nothing ..
		}
		if (dbgSB != null)
			dbgSB.append("[CLASSIFY] ").append(++dbgcnt).append(" Trip Id:").append(trip.id).append(" TG:").append(minGap).append(" TC:").append(timeCat).append(" ");
		
		Collection<InvPile> pileList = g_invPiles.values();
		for (InvPile pile:pileList) {
			if (!pile.isAvailableLoad(refTS))
				continue;
			double dist = 0;
			int distCat = 0;
			boolean isIn = false;
			if (pile.reg != null) {
				isIn = RegionTest.PointIn(conn, new Point(trip.loadLon, trip.loadLat), pile.reg.region);
				if (!isIn)
					dist = pile.reg.region.getShortestDistFromEdge(trip.loadLon, trip.loadLat);
			}
			else if (!Misc.isUndef(pile.lon)) {
				dist = Point.fastGeoDistance(pile.lon, pile.lat, trip.loadLon, trip.loadLat);
				if (dist < pile.radius) {
					isIn = true;
					dist = 0;
				}
			}
			else {
				continue;
			}
			for (;distCat<g_distKMThresh.length && g_distKMThresh[distCat] <= dist;distCat++) {
				//DO nothing ..
			}	
			ArrayList<TripLink> theList = m_tripsForInvPiles.get(pile.id);
			if (theList == null) {
				theList = new ArrayList<TripLink>();
				m_tripsForInvPiles.put(pile.id, theList);
			}
			TripLink link = new TripLink(timeCat, distCat);
			MiscInner.PairIntBool addAfter = TripLink.getPosOrAddAt(theList, link);
			if (addAfter.second) {
				theList.get(addAfter.first).tripList.add(trip);
			}
			else {
				
				link.tripList.add(trip);
				if (addAfter.first == theList.size())
					theList.add(link);
				else
					theList.add(addAfter.first, link);
			}
			if (dbgSB != null)
				dbgSB.append("[P").append(pile.id).append(",").append((int)(dist*1000)).append(",").append(distCat).append(",").append(addAfter.second ? addAfter.first : theList.size()-1).append(",").append(theList.size()).append("]");
		}
		if (dbgSB != null) {
			System.out.println(dbgSB);
			dbgSB.setLength(0);
		}
		Collection<SiteStats.CachedInvPileForShovel> shovelList =  SiteStats.g_cachedInvForShovel.values();
		for (CachedInvPileForShovel shovel:shovelList) {
			int shovelId = shovel.shovelId;
			if (Misc.isUndef(shovelId) || Misc.isUndef(shovel.prevCalcLon) || Misc.isUndef(shovel.prevCalcLat))
				continue;
			double dist = 0;
			int distCat = 0;
			boolean isIn = false;
			
			dist = Point.fastGeoDistance(shovel.prevCalcLon, shovel.prevCalcLat, trip.loadLon, trip.loadLat);
				
			for (;distCat<g_distKMThresh.length && g_distKMThresh[distCat] <= dist;distCat++) {
				//DO nothing ..
			}	
			ArrayList<TripLink> theList = this.m_tripsForShovelPos.get(shovelId);
			if (theList == null) {
				theList = new ArrayList<TripLink>();
				this.m_tripsForShovelPos.put(shovelId, theList);
			}
			TripLink link = new TripLink(timeCat, distCat);
			MiscInner.PairIntBool addAfter = TripLink.getPosOrAddAt(theList, link);
			if (addAfter.second) {
				theList.get(addAfter.first).tripList.add(trip);
			}
			else {
				
				link.tripList.add(trip);
				if (addAfter.first == theList.size())
					theList.add(link);
				else
					theList.add(addAfter.first, link);
			}
		}
	}
	
	private static long MAX_LOOKBACK_TIME = 4*24*60*60*1000;
	private void loadRelevantTrips(Connection conn, int portNodeId) throws Exception {
		//public static int INT_SITESTAT_LEAD_BY_ADJ = 10130; //default = true
		//public static int DOUBLE_SITESTAT_IGN_LO_TRAVELSECFRAC = 10131; //default = true
		//public static int DOUBLE_SITESTAT_IGN_HI_TRAVELSECFRAC = 10132;
		//public static int DOUBLE_SITESTAT_IGN_STDDEV = 10133;
		Cache cache = Cache.getCacheInstance(conn);
		MiscInner.PortInfo portInfo = cache.getPortInfo(portNodeId, conn);
		ArrayList<Integer> valList = null;
		valList = portInfo == null ? null : portInfo.getIntParams(OrgConst.INT_SITESTAT_LEAD_BY_ADJ, true);
		if (valList != null && valList.size() > 0)
			this.doSiteStatLeadByAdj = 0 != valList.get(0);

		ArrayList<Integer> dvalList = null;
		dvalList = portInfo == null ? null : portInfo.getDoubleParams(OrgConst.DOUBLE_SITESTAT_IGN_LO_TRAVELSECFRAC, true);
		if (dvalList != null && dvalList.size() > 0)
			this.loTravelSecFrac =  dvalList.get(0);

		dvalList = portInfo == null ? null : portInfo.getDoubleParams(OrgConst.DOUBLE_SITESTAT_IGN_HI_TRAVELSECFRAC, true);
		if (dvalList != null && dvalList.size() > 0)
			this.hiTravelSecFrac =  dvalList.get(0);

		dvalList = portInfo == null ? null : portInfo.getDoubleParams(OrgConst.DOUBLE_SITESTAT_IGN_STDDEV, true);
		if (dvalList != null && dvalList.size() > 0)
			this.stdDevRange =  dvalList.get(0);

		String tripQ = g_tripQ;
		tripQ = tripQ.replaceAll("@trip_info", this.getStatsFromRef ? SiteStats.g_tripInfoTableRef : SiteStats.g_tripInfoTableReg);
		tripQ = tripQ.replaceAll("@logged_data", this.getStatsFromRef ? SiteStats.g_loggedDataTableRef : SiteStats.g_loggedDataTableReg);
		StringBuilder uopHolder = new StringBuilder();
		boolean isFirst = true;
		uopHolder.append("and t.unload_gate_op in (");
		for (OpStationBean op : uopList) {
			if (!isFirst)
				uopHolder.append(",");
			isFirst = false;
			uopHolder.append(op.getOpStationId());
		}
		uopHolder.append(")");
		if (isFirst)
			uopHolder.setLength(0);
		
		tripQ = tripQ.replaceAll("@uopClause", uopHolder.toString());
		PreparedStatement ps = conn.prepareStatement(tripQ);
		//we look for enough trips by progressively relaxing the time bounds and distance bounds
		java.sql.Timestamp en = Misc.longToSqlDate(refTS);
		java.sql.Timestamp start = Misc.longToSqlDate(refTS-MAX_LOOKBACK_TIME);
		ps.setInt(1, portNodeId);
		ps.setTimestamp(2, start);
		ps.setTimestamp(3, en);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int tripid = Misc.getRsetInt(rs, "id");
			int shovelId = Misc.getRsetInt(rs, "shovel_id");
			double loadLon = Misc.getRsetDouble(rs, "load_lon");
			double loadLat = Misc.getRsetDouble(rs, "load_lat");
			double unloadLon = Misc.getRsetDouble(rs, "unload_lon");
			double unloadLat = Misc.getRsetDouble(rs, "unload_lat");
			int uopId = Misc.getRsetInt(rs, "unload_gate_op");
	
			int dumperId = Misc.getRsetInt(rs, "dumper_id");
			double lgapSec = Misc.getRsetDouble(rs, "lgap");
			double postlgapSec = Misc.getRsetDouble(rs, "postlgap");
			String leadAdjPrefix = this.doSiteStatLeadByAdj ? "adj" : "";
			double forwLeadSec = Misc.getRsetDouble(rs,leadAdjPrefix+"forwleadtime");
			double backLeadSec = Misc.getRsetDouble(rs, leadAdjPrefix+"backleadtime");
			double unloadTimeSec = Misc.getRsetDouble(rs, "unloadtime");
			double forwLeadKM = Misc.getRsetDouble(rs,"loadleaddist");
			double backLeadKM = Misc.getRsetDouble(rs, "unloadleaddist");
			long lgin = Misc.sqlToLong(rs.getTimestamp("load_gate_in"));
			double shovelCycles = Misc.getRsetDouble(rs, "shovel_cycles");
			double cycleDur = Misc.getRsetDouble(rs, "shovel_cycle_dur");
			if (Misc.isUndef(shovelId)) {
				cycleDur = Misc.getUndefDouble();
				shovelCycles = Misc.getUndefDouble();
			}
			TripInfo tripInfo = new TripInfo();
			tripInfo.backLeadKM = backLeadKM;
			tripInfo.backSec = backLeadSec;
			tripInfo.dumperId = dumperId;
			tripInfo.forwLeadKM = forwLeadKM;
			tripInfo.forwSec = forwLeadSec;
			tripInfo.loadSec = backLeadSec;
			tripInfo.loadLat = loadLat;
			tripInfo.loadLon = loadLon;
			tripInfo.lgapSec = lgapSec;
			tripInfo.numCycles = shovelCycles;
			tripInfo.postLoadSec = postlgapSec;
			tripInfo.shovelId = shovelId;
			tripInfo.unloadLat = unloadLat;
			tripInfo.unloadLon = unloadLon;
			tripInfo.unloadSec = unloadTimeSec;
			tripInfo.uopId = uopId;
			tripInfo.cycleTime = cycleDur;
			tripInfo.lgin = lgin;
			tripInfo.id = tripid;
			classifyAndAddTrip(conn, tripInfo);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	
	
	
	
	private static class TempStatHolder {
		int cat1;
		int cat2;
		int cat3;
		double minRange = Misc.getUndefDouble();
		double maxRange = Misc.getUndefDouble();
		SiteStats parent = null;
		public void setMinMax(double minRange, double maxRange) {
			this.minRange = minRange;
			this.maxRange = maxRange;
		}
		public TempStatHolder(SiteStats parent) {
			this.parent = parent;
		}
		ArrayList<TempStat> valsForTimeDistCat = new ArrayList<TempStat>();
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Cat1:").append(this.cat1).append(" ,cat2:").append(this.cat2).append(" ,vals:").append(this.valsForTimeDistCat);
			return sb.toString();
		}
		public TempStatHolder(SiteStats parent, int cat1, int cat2, int cat3) {
			this.parent = parent;
			this.cat1 = cat1;
			this.cat2 = cat2;
			this.cat3 = cat3;
		}
		public void addVal(int timeCat, int distCat, double v) {
			int addAtIndex = 0;
			boolean added = false;
			
			for (int is=valsForTimeDistCat.size();addAtIndex<is;addAtIndex++) {
				int cmp = TripLink.compareTo(valsForTimeDistCat.get(addAtIndex).timeCat, valsForTimeDistCat.get(addAtIndex).distCat, timeCat, distCat);
				if (cmp == 0) {
					valsForTimeDistCat.get(addAtIndex).val1.add(v);
					added = true;
				}
				else if (cmp > 0) {
					break;
				}
			}
			if (!added) {
				TempStat toAdd = new TempStat(timeCat, distCat);
				toAdd.val1.add(v);
				if (addAtIndex != valsForTimeDistCat.size())
					valsForTimeDistCat.add(addAtIndex, toAdd);
				else
					valsForTimeDistCat.add(toAdd);
			}
		}
		public static TempStatHolder getHolder(SiteStats parent, ArrayList<TempStatHolder> holders, int cat1, int cat2, int cat3) {
			for (int i=0,is=holders.size(); i<is;i++)
				if (holders.get(i).cat1 == cat1 && holders.get(i).cat2 == cat2 && holders.get(i).cat3 == cat3)
					return holders.get(i);
			TempStatHolder holder = new TempStatHolder(parent, cat1, cat2, cat3);
			holders.add(holder);
			return holder;
		}
		public static void addVal(SiteStats parent, ArrayList<TempStatHolder> holders, int cat1, int cat2, int cat3, int timeCat, int distCat, double v) {
			if (!Misc.isUndef(v)) {
				TempStatHolder holder = getHolder(parent, holders, cat1, cat2, cat3);
				if (Misc.isUndef(holder.minRange) || Misc.isUndef(holder.maxRange) || (v >= holder.minRange && v <= holder.maxRange))
					holder.addVal(timeCat, distCat, v);
			}
		}
		public double getMean() {
			MiscInner.PairDouble allMeanDev = getAllMeanAndDeviation(this.minRange, this.maxRange);
			double v = 0;
			boolean seenValidMean = false;
			double fullMean = allMeanDev.first;
			double fullDev = allMeanDev.second;
			double acceptableRange = parent != null ? parent.stdDevRange : 1.5;
			double low = fullMean - acceptableRange*fullDev;
			double hi = fullMean + acceptableRange*fullDev;
			if (!Misc.isUndef(this.minRange) && minRange > low)
				low = this.minRange;
			if (!Misc.isUndef(this.maxRange) && maxRange < hi)
				low = this.maxRange;
			
			double wtMean = 0;
			int count = 0;
			int startTimeDistCatTot = -1;
			for (int i=0,is=this.valsForTimeDistCat.size();i<is;i++) {
				Pair<Integer, Double> nAndMean = this.valsForTimeDistCat.get(i).getAdjMean(low, hi);
				int n = nAndMean.first;
				double timeDistCatMean = nAndMean.second;
				if (n == 0)
					continue;
				//System.out.println("[AVG for:]timeCat:"+this.valsForTimeDistCat.get(i).timeCat+" DistCat:"+this.valsForTimeDistCat.get(i).distCat);
				int timePlusDistCat = this.valsForTimeDistCat.get(i).timeCat+this.valsForTimeDistCat.get(i).distCat;
				if (startTimeDistCatTot < 0)
					startTimeDistCatTot = 0;
				double wtForCombining =timePlusDistCat-startTimeDistCatTot >= g_wtsForCombining.length ? g_wtsForCombining[g_wtsForCombining.length-1] : g_wtsForCombining[timePlusDistCat-startTimeDistCatTot];
				int enoughSamples = timePlusDistCat >= g_minTripsNeeded.length ? g_minTripsNeeded[g_minTripsNeeded.length-1] : g_minTripsNeeded[timePlusDistCat];
				if (count == 0)
					wtMean = timeDistCatMean;
				else {
					wtMean = (count*wtMean*(1-wtForCombining)+n*timeDistCatMean*wtForCombining)/(n*wtForCombining+count*(1-wtForCombining));
					//wtMean = ((1-wtForCombining)*wtMean+wtForCombining*timeDistCatMean);
				}
				count += n;
				//System.out.println("[AVG for:]timeCat:"+this.valsForTimeDistCat.get(i).timeCat+" DistCat:"+this.valsForTimeDistCat.get(i).distCat+" catMean"+timeDistCatMean+" n:"+n+" count:"+count+" wtMean:"+wtMean+" enoughSamples:"+enoughSamples+" Done:"+(count >= enoughSamples));
				if (count >= enoughSamples)
					break;
			}
			return count == 0 ? Misc.getUndefDouble() : wtMean;
		}
		public MiscInner.PairDouble getAllMeanAndDeviation(double lo, double hi) {
			//from knuth .. numerically stable one pass
			
			
			int n=0;
			double mean = 0;
			double m2 = 0;
			for (int i=0,is=this.valsForTimeDistCat.size();i<is;i++) {
				ArrayList<Double> vals = this.valsForTimeDistCat.get(i).val1;
				int timePlusDistCat = this.valsForTimeDistCat.get(i).timeCat+this.valsForTimeDistCat.get(i).distCat;
				int enoughSamples = (int)(1.2*(timePlusDistCat >= g_minTripsNeeded.length ? g_minTripsNeeded[g_minTripsNeeded.length-1] : g_minTripsNeeded[timePlusDistCat]));
				
				for (int j=0,js=vals.size();j<js;j++) {
					double x = vals.get(j);
					if (Misc.isUndef(x))
						continue;
					if (!Misc.isUndef(lo) && !Misc.isUndef(hi) && (lo > x || hi < x))
						continue;
					 n++;
					 
					 double delta = x - mean;
					 mean = mean + delta/n;
					 m2 = m2 + delta*(x-mean);
				}
				if (n >= enoughSamples)
					break;
			}
			double variance = n < 2 ? 0 : m2/(n-1);
			double stdev = Math.sqrt(variance);
			return new MiscInner.PairDouble(mean, stdev);
		}
	}
	
	
	private static class TempStat {
		int timeCat;
		int distCat;
		ArrayList<Double> val1 = new ArrayList<Double>();
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("timeCat:").append(this.timeCat).append(" ,distCat:").append(this.distCat).append(" ,#trips:").append(this.val1.size()).append(", vals:").append(this.val1);
			return sb.toString();
		}
		public TempStat(int timeCat, int distCat) {
			this.timeCat = timeCat;
			this.distCat = distCat;
		}
		public Pair<Integer, Double> getAdjMean(double low, double hi) {
			int n=0;
			double mean = 0;
			for (int i=0,is=val1.size(); i<is; i++) {
				double x = val1.get(i);
				if (Misc.isUndef(x))
					continue;
					
				if ((x >= low && x <= hi)) {
					 n++;
					 double delta = x - mean;
					 mean = mean + delta/n;
				}
			}
			return new Pair<Integer, Double>(n, mean);
		}
	}
	public Stats getStatsForTripsLinked(Connection conn, ArrayList<TripLink> tripLinks, double lon, double lat) {
		
		//get shovel oriented stuff .. first
		ArrayList<TempStatHolder> cycleTime = new ArrayList<TempStatHolder>();
		ArrayList<TempStatHolder> numCycles = new ArrayList<TempStatHolder>();
		ArrayList<TempStatHolder> loadTime = new ArrayList<TempStatHolder>();
		ArrayList<TempStatHolder> forwLead = new ArrayList<TempStatHolder>();
		ArrayList<TempStatHolder> forwTimeSec = new ArrayList<TempStatHolder>();
		ArrayList<TempStatHolder> backTimeSec = new ArrayList<TempStatHolder>();
		for (int i=0,is=tripLinks == null ? 0 : tripLinks.size();i<is;i++) {
			TripLink tripLink = tripLinks.get(i);
			int timeCat = tripLink.timeCat;
			int distCat = tripLink.distCat;
			int cattot = timeCat+distCat;
			ArrayList<TripInfo> trips = tripLink.tripList;
			for (int j=0,js=trips.size(); j<js; j++) {
				TripInfo trip = trips.get(j);
				int shovelId = trip.shovelId;
				int dumperId = trip.dumperId;
				int uopId = trip.uopId;
				CacheTrack.VehicleSetup shovelsetup = CacheTrack.VehicleSetup.getSetup(shovelId, conn);
				CacheTrack.VehicleSetup dumpersetup = CacheTrack.VehicleSetup.getSetup(dumperId, conn);
				CapacityDef shovelDef = null;
				CapacityDef dumperDef = null;
				if (shovelsetup != null)
					shovelDef = this.getCapacityDefBy9097(shovelsetup.m_type);
				if (dumpersetup != null)
					dumperDef = this.getCapacityDefBy9097(dumpersetup.m_type);
				int shovelCat = shovelDef == null ? Misc.getUndefInt() : shovelDef.type9097;
				int dumperCat = dumperDef == null ? Misc.getUndefInt() : dumperDef.type9097;
				TempStatHolder.addVal(this,cycleTime, shovelCat, Misc.getUndefInt(), Misc.getUndefInt(), timeCat, distCat, trip.cycleTime);
				TempStatHolder.addVal(this,numCycles, shovelCat, dumperCat, Misc.getUndefInt(), timeCat, distCat, trip.numCycles);
				TempStatHolder.addVal(this,loadTime, shovelCat, dumperCat, Misc.getUndefInt(), timeCat, distCat, trip.loadSec);
				TempStatHolder.addVal(this,forwLead, uopId, Misc.getUndefInt(), Misc.getUndefInt(), timeCat, distCat, trip.forwLeadKM);
				TempStatHolder.addVal(this,forwTimeSec, dumperCat, uopId, Misc.getUndefInt(), timeCat, distCat, trip.forwSec);
				TempStatHolder.addVal(this,backTimeSec, dumperCat, uopId, Misc.getUndefInt(), timeCat, distCat, trip.backSec);
			}
		}
		Stats retval = new Stats();
		//put avgCycleTime
		for (int i=0,is=cycleTime.size();i<is;i++) {
			//dbgPrintStatCalc = true;
			//dbgInvPileId = pile.id;
			//this.dbgStatOf 
			//System.out.println("[STATS - CYCLE] ShovelCat:"+cycleTime.get(i).cat1);
//			cycleTime.get(i).setMinMax(8, 60);
			cycleTime.get(i).setMinMax(12, 50);
			double avg = cycleTime.get(i).getMean();
			if (!Misc.isUndef(avg)) {
				retval.avgCycleTime.add(new Pair<Integer, Double>(cycleTime.get(i).cat1, avg));
			}
		}
		//get avgFillFactor
		double fillFactor = 0;
		int n = 0;
		for (int i=0,is=numCycles.size();i<is;i++) {
			TempStatHolder holder = numCycles.get(i);
			int shovelCat = holder.cat1;
			int dumperCat = holder.cat2;
			CapacityDef shovelDef = this.getCapacityDefBy9097(shovelCat);
			CapacityDef dumperDef = this.getCapacityDefBy9097(dumperCat);
			if (shovelDef != null && dumperDef != null) {
				int idealCycle = (int)(dumperDef.capVol > 0.001 && shovelDef.capVol > 0.001 ? Math.round(dumperDef.capVol/shovelDef.capVol)
						: Math.round(dumperDef.capWt/shovelDef.capWt))
						;
				//System.out.println("[STATS - NUMCYCLE] shovelCat:"+holder.cat1+" dumperCat:"+ holder.cat2);
				
//				holder.setMinMax(0.6*idealCycle, 1.8*idealCycle);
				holder.setMinMax(5, 10);
				double cycleSeen = holder.getMean();
				if (Misc.isUndef(cycleSeen))
					continue;
				fillFactor += cycleSeen/idealCycle;
				n++;
				//System.out.println("[STATS - NUMCYCLE] Result cat fill:"+cycleSeen/idealCycle+" runn avg:"+fillFactor/n);
			}
			retval.avgFillFactor = n > 0 ? fillFactor/n : Misc.getUndefDouble();
		}
		//get avgLoadTime where we dont have cycle info
		for (int i=0,is=loadTime.size();i<is;i++) {
			TempStatHolder holder = loadTime.get(i);
			int shovelCat = holder.cat1;
			int dumperCat = holder.cat2;
		//	System.out.println("[STATS -LoadTime] shovelCat:"+holder.cat1+" dumperCat:"+ holder.cat2);
			double avg = holder.getMean();
			if (Misc.isUndef(avg))
				continue;
			retval.avgLoadTime.add(new Triple<Integer, Integer, Double>(shovelCat, dumperCat, avg));
		}    
		//get uopLeadDist
		for (int i=0,is=forwLead.size();i<is;i++) {
			TempStatHolder holder = forwLead.get(i);
			int uopId = holder.cat1;
		//	System.out.println("[STATS -UopDist] uopCat:"+holder.cat1);
			double avg = holder.getMean();
			if (Misc.isUndef(avg))
				continue;
			

			retval.uopLeadDist.add(new Pair<Integer, Double>(uopId, avg));
		}
		
		//get forwRouteDifficultyFactor
		//  get dist for uop, get avg speed for dumper cat by looking at time and then for each uop, average it over
		ArrayList<TempStatHolder> avgSpeedByUopDumperCat = new ArrayList<TempStatHolder>();
		for (int art=0;art<2;art++) {
			avgSpeedByUopDumperCat.clear();
			ArrayList<TempStatHolder> timeHolder = art == 0 ? forwTimeSec : backTimeSec;
			 
			for (int i=0,is=timeHolder.size();i<is;i++) {
				TempStatHolder holder = timeHolder.get(i);
				int dumperCat = holder.cat1;
				int uopId = holder.cat2;
				double dist = Misc.getUndefDouble();
				for (int j=0,js=retval.uopLeadDist.size();j<js;j++) {
					if (retval.uopLeadDist.get(j).first == uopId) {
						dist = retval.uopLeadDist.get(j).second;
						break;
					}
				}
				if (Misc.isUndef(dist))
					continue;
				CapacityDef dumperCatDef = this.getCapacityDefBy9097(dumperCat);
				if (dumperCatDef == null)
					continue;
				double idealSpeed = dumperCatDef.keyPerformanceMetric;
				double idealSec = dist/idealSpeed*3600;
				double loFrac = this.loTravelSecFrac;
				double hiFrac = this.hiTravelSecFrac;
				holder.setMinMax(loFrac*idealSec, hiFrac*idealSec);
				//System.out.println("[STATS -"+(art == 0 ? "ForwTime":"BackTime")+"] dumperCat:"+holder.cat1+" uopCat:"+ holder.cat2);
				double avg = holder.getMean();
				if (Misc.isUndef(avg))
					continue;
				
				double speed = (dist*3600)/avg;
				double diffFactor = speed/dumperCatDef.keyPerformanceMetric;
			//	System.out.println("[STATS -"+(art == 0 ? "ForwTimeUopAndDumperCat":"BackTimeUopAndDumperCat")+"] dumperCat:"+holder.cat1+" uopCat:"+ holder.cat2+" Lead:"+dist+" speed:"+speed+" Factor:"+diffFactor);
				TempStatHolder.addVal(this,avgSpeedByUopDumperCat, uopId, Misc.getUndefInt(), Misc.getUndefInt(), 0, 0, diffFactor);
			}
			
			for (int i=0,is=avgSpeedByUopDumperCat.size();i<is;i++) {
				TempStatHolder holder = avgSpeedByUopDumperCat.get(i);
				int uopId = holder.cat1;
			//	System.out.println("[STATS -"+(art == 0 ? "ForwTimeUopCat":"BackTimeUopCat")+" uopCat:"+ holder.cat1);
				double avg = holder.getMean();
				if (Misc.isUndef(avg))
					continue;
				if (art == 0)
					retval.forwAvgRouteDifficulty.add(new Pair<Integer, Double>(uopId, avg));
				else
					retval.backAvgRouteDifficulty.add(new Pair<Integer, Double>(uopId, avg));
			}
		}
			
		return retval;
	}
	private static int g_minuteThresh[] = {4*60,12*60,24*60,48*60,96*60};
	private static double g_distKMThresh[] = {0.04,0.1,0.2,0.4};
	private static int g_minTripsNeeded[] = {20,40,60,60,60};
	private static double g_wtsForCombining[] = {1,0.9,0.8};
	private HashMap<Integer, Stats> m_statsForInv = new HashMap<Integer, Stats>();
	private HashMap<Integer, Stats> m_statsForShovelPos = new HashMap<Integer, Stats>();
	
	public static class Stats {
		private ArrayList<Triple<Integer,Integer,Double>> avgLoadTime = new ArrayList<Triple<Integer, Integer, Double>>();//first = shovelType, 2nd = dumperType, 3rd = value
		private ArrayList<Pair<Integer, Double>> avgCycleTime = new ArrayList<Pair<Integer, Double>>();//first = shovelType
		private double avgFillFactor = Misc.getUndefDouble();//cycles seen = avgFillFactor * ideal # of cycles
		private ArrayList<Pair<Integer, Double>> uopLeadDist = new ArrayList<Pair<Integer, Double>>();//first = uopId
		private ArrayList<Pair<Integer, Double>> forwAvgRouteDifficulty = new ArrayList<Pair<Integer, Double>>();//first = uopId, second = value. AvgSpeed for route=val*performanceMetric for dumper 
		private ArrayList<Pair<Integer, Double>> backAvgRouteDifficulty = new ArrayList<Pair<Integer, Double>>();//first = uopId, second = value. AvgSpeed for route=val*performanceMetric for dumper
		private int invPileId = Misc.getUndefInt();
		
		public double getCycleTime(int shovelType) {//if undef then average across
			double avg = 0;
			int cnt = 0;
			for (int i=0,is=avgCycleTime == null ? 0 : avgCycleTime.size();i<is;i++) {
				if (avgCycleTime.get(i).first == shovelType)
					return avgCycleTime.get(i).second;
				avg += avgCycleTime.get(i).second;
				cnt++;
			}
			return cnt == 0 ? DEFAULT_CYCLE_TIME : avg/(double)cnt;
		}
		
		public double getAvgFillFactor() {
			return Misc.isUndef(this.avgFillFactor) ? DEFAULT_FILL_FACTOR : this.avgFillFactor;
		}
		public double getDestLeadDist(int destId) {
			for (int i=0,is=this.uopLeadDist == null ? 0 : uopLeadDist.size();i<is;i++) {
				if (uopLeadDist.get(i).first == destId)
					return uopLeadDist.get(i).second;
			}
			return Misc.getUndefDouble();
		}
		public double getForwRouteDifficulty(int destId) {
			double v = getGenericAvgSpeed(destId, this.forwAvgRouteDifficulty);
			if (Misc.isUndef(v))
				v = SiteStats.DEFAULT_FWD_DIFFICULTY;
			return v;
		}
		public double getBackRouteDifficulty(int destId) {
			double v = getGenericAvgSpeed(destId, this.backAvgRouteDifficulty);
			if (Misc.isUndef(v))
				v = SiteStats.DEFAULT_BACK_DIFFICULTY;
			return v;
		}
		private double getGenericAvgSpeed(int destId, ArrayList<Pair<Integer, Double>> avgSpeedData) {
			double avg = 0;
			int cnt = 0;
			for (int i=0,is=avgSpeedData == null ? 0 : avgSpeedData.size();i<is;i++) {
				boolean matchUopId = avgSpeedData.get(i).first == destId;
				if (matchUopId)
					return avgSpeedData.get(i).second;
				avg += avgSpeedData.get(i).second;
				cnt++;
			}
			if (cnt > 0)
				return avg/cnt;
			return Misc.getUndefDouble();
		}
	}
	
	private static String g_getAvgUnloadTime = "select v.type, avg(timestampdiff(second, t.unload_gate_in, t.unload_gate_out)) from vehicle v join port_nodes leaf on (leaf.id = v.customer_id) "+ 
	" join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id=?) "+ 
	" join @trip_info t on (t.vehicle_id = v.id) where t.load_gate_op is not null and t.unload_gate_op is not null and t.combo_start between ? and ? and t.is_dala=1 "+ 
	" group by v.type "
	;
	
	private static void loadAvgUnloadTime(Connection conn, boolean getStatsFromRef, int portNodeId, long refTS) throws Exception {
		
		String tripQ = g_getAvgUnloadTime;
		tripQ = tripQ.replaceAll("@trip_info", getStatsFromRef ? SiteStats.g_tripInfoTableRef : SiteStats.g_tripInfoTableReg);
		tripQ = tripQ.replaceAll("@logged_data", getStatsFromRef ? SiteStats.g_loggedDataTableRef : SiteStats.g_loggedDataTableReg);
		
		PreparedStatement ps = conn.prepareStatement(tripQ);
		ps.setInt(1, portNodeId);
		ps.setTimestamp(2, Misc.longToSqlDate(refTS-24*60*60*1000));
		ps.setTimestamp(3, Misc.longToSqlDate(refTS));
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int ty = rs.getInt(1);
			int sec = rs.getInt(2);
			CapacityDef tyDef = getCapacityDefBy9097(ty);
			if (tyDef != null)
				tyDef.keyMetric2 = sec;
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	public static void initStatic(Connection conn, int portNodeId, boolean doFromRef) throws Exception {
		long refTS = -1;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select max(combo_start) from "+(doFromRef ? SiteStats.g_tripInfoTableRef : SiteStats.g_tripInfoTableReg));
			rs = ps.executeQuery();
			if (rs.next())
				refTS = Misc.sqlToLong(rs.getTimestamp(1));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (refTS <= 0)
			refTS = System.currentTimeMillis();
		TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());
		SiteStats.loadInvPilesCapDefShovelInfo(conn, portNodeId, true, doFromRef, refTS);
		
	}
	 public static void main(String[] args) {
		  int tripId = 811518;
		  
		  Connection conn =  null;
		  try {
			  conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			  TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());
			  long refTS = -1;
			  java.util.Date dt = new java.util.Date(118,9,17,13,0,0);
			  refTS = dt.getTime();
			  SiteStats working = new SiteStats(conn, refTS, true, true, true);
			  StringBuilder sb = working.getXMLForStats(conn, 2);
			  System.out.println(sb);
			  
			  sb = working.getXMLForUnloadStats(conn,1467);
			  System.out.println(sb);
			  if (false) {
			  working = new SiteStats(conn, refTS, true, true, true);
			  sb = working.getXMLForStats(conn, 1467);
			  System.out.println(sb);
			  
			  working = new SiteStats(conn, refTS, false, true, true);
			  sb = working.getXMLForStats(conn, 1467);
			  System.out.println(sb);
			  
			  working = new SiteStats(conn, refTS, false, true, true);
			  sb = working.getXMLForStats(conn, 1467);
			  System.out.println(sb);
			  }
		  }
		  catch (Exception e) {
			  e.printStackTrace();
		  }
		  finally {
			  try {
				  DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			  }
			  catch (Exception e) {
				  
			  }
		  }
	 }
}

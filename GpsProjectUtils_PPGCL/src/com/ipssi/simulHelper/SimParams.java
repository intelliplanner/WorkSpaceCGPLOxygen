package com.ipssi.simulHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.miningOpt.SiteStats;
import com.ipssi.processor.utils.GpsData;

public class SimParams {
	//TODO route from shovel to shovel; shovel to rest
	
	public static class ShovelParam {
		volatile private int cycleTime;
		volatile private int numCycles;
		volatile private int cleanupTime;
		volatile private int cleanupForTrip;
		volatile private int positioningTime;
		volatile private int loCycleTime;
		volatile private int hiCycleTime;
		volatile private int loNumCycles;
		volatile private int hiNumCycles;
		volatile private int loCleanupTime;
		volatile private int hiCleanupTime;
		volatile private int loPositioningTime;
		volatile private int hiPositioningTime;
		volatile private int invPileId;
		volatile private double lon;
		volatile private double lat;
		volatile private String posName;
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(invPileId).append(",").append(lon).append(",").append(lat);
			return sb.toString();
		}
		public int getCleanupTime() {
			return cleanupTime;
		}

		public void setCleanupTime(int cleanupTime) {
			this.cleanupTime = cleanupTime;
		}

		public int getCleanupForTrip() {
			return cleanupForTrip;
		}

		public void setCleanupForTrip(int cleanupForTrip) {
			this.cleanupForTrip = cleanupForTrip;
		}

		public int getPositioningTime() {
			return 0;
			//return positioningTime;
		}

		public void setPositioningTime(int positioningTime) {
			this.positioningTime = positioningTime;
		}

		public int getLoCleanupTime() {
			return loCleanupTime;
		}

		public void setLoCleanupTime(int loCleanupTime) {
			this.loCleanupTime = loCleanupTime;
		}

		public int getHiCleanupTime() {
			return hiCleanupTime;
		}

		public void setHiCleanupTime(int hiCleanupTime) {
			this.hiCleanupTime = hiCleanupTime;
		}

		public int getLoPositioningTime() {
			return loPositioningTime;
		}

		public void setLoPositioningTime(int loPositioningTime) {
			this.loPositioningTime = loPositioningTime;
		}

		public int getHiPositioningTime() {
			return hiPositioningTime;
		}

		public void setHiPositioningTime(int hiPositioningTime) {
			this.hiPositioningTime = hiPositioningTime;
		}

		public int getCycleTime() {
			return cycleTime;
		}

		public void setCycleTime(int cycleTime) {
			this.cycleTime = cycleTime;
		}

		public int getNumCycles() {
			return numCycles;
		}

		public void setNumCycles(int numCycles) {
			this.numCycles = numCycles;
		}

		public int getLoCycleTime() {
			return loCycleTime;
		}

		public void setLoCycleTime(int loCycleTime) {
			this.loCycleTime = loCycleTime;
		}

		public int getHiCycleTime() {
			return hiCycleTime;
		}

		public void setHiCycleTime(int hiCycleTime) {
			this.hiCycleTime = hiCycleTime;
		}

		public int getLoNumCycles() {
			return loNumCycles;
		}

		public void setLoNumCycles(int loNumCycles) {
			this.loNumCycles = loNumCycles;
		}

		public int getHiNumCycles() {
			return hiNumCycles;
		}

		public void setHiNumCycles(int hiNumCycles) {
			this.hiNumCycles = hiNumCycles;
		}

		public ShovelParam(int cycleTime, int numCycles, int cleanupTime,
				int cleanupForTrip, int positioningTime, int loCycleTime,
				int hiCycleTime, int loNumCycles, int hiNumCycles,
				int loCleanupTime, int hiCleanupForTrip, int loPositioningTime,
				int hiPositioningTime, double lon, double lat, String posName, int invPileId) {
			super();
			this.cycleTime = cycleTime;
			this.numCycles = numCycles;
			this.cleanupTime = cleanupTime;
			this.cleanupForTrip = cleanupForTrip;
			this.positioningTime = positioningTime;
			this.loCycleTime = loCycleTime;
			this.hiCycleTime = hiCycleTime;
			this.loNumCycles = loNumCycles;
			this.hiNumCycles = hiNumCycles;
			this.loCleanupTime = loCleanupTime;
			this.hiCleanupTime = hiCleanupForTrip;
			this.loPositioningTime = loPositioningTime;
			this.hiPositioningTime = hiPositioningTime;
			this.invPileId = invPileId;
			this.lon = lon;
			this.lat = lat;
			this.posName = posName;
		}

		public int getInvPileId() {
			return invPileId;
		}

		public void setInvPileId(int invPileId) {
			this.invPileId = invPileId;
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

		public String getPosName() {
			return posName;
		}

		public void setPosName(String posName) {
			this.posName = posName;
		}
	}
	
	public static class UOpParam {
		private int unloadTime;
		private int loUnloadTime;
		private int hiUnloadTime;
		private double lon;
		private double lat;
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(unloadTime).append(",").append(lon).append(",").append(lat);
			return sb.toString();
		}
		public int getUnloadTime() {
			return unloadTime;
		}

		public void setUnloadTime(int unloadTime) {
			this.unloadTime = unloadTime;
		}

		

		public int getLoUnloadTime() {
			return loUnloadTime;
		}

		public void setLoUnloadTime(int loUnloadTime) {
			this.loUnloadTime = loUnloadTime;
		}

		public int getHiUnloadTime() {
			return hiUnloadTime;
		}

		public void setHiUnloadTime(int hiUnloadTime) {
			this.hiUnloadTime = hiUnloadTime;
		}

		public UOpParam(int unloadTime, int loUnloadTime, int hiUnloadTime, double lon, double lat) {
			super();
			this.unloadTime = unloadTime;
			this.loUnloadTime = loUnloadTime;
			this.hiUnloadTime = hiUnloadTime;
			this.lon = lon;
			this.lat = lat;
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
	}
	public static class GpsPlus {
		long gpsRecTime;
		double lon;
		double lat;
		double av;
		double speed;
		String name;
		public GpsPlus clone() {
			GpsPlus retval = new GpsPlus(this.gpsRecTime, this.lon, this.lat, this.av, this.speed, this.name);
			return retval;
		}
		public long getGpsRecTime() {
			return gpsRecTime;
		}
		public void setGpsRecTime(long gpsRecTime) {
			this.gpsRecTime = gpsRecTime;
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
		public double getAv() {
			return av;
		}
		public void setAv(double av) {
			this.av = av;
		}
		public double getSpeed() {
			return speed;
		}
		public void setSpeed(double speed) {
			this.speed = speed;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public GpsPlus(long gpsRecTime, double lon, double lat, double av,
				double speed, String name) {
			super();
			this.gpsRecTime = gpsRecTime;
			this.lon = lon;
			this.lat = lat;
			this.av = av;
			this.speed = speed;
			this.name = name;
		}
		public GpsPlus(GpsPlus like){
			this.gpsRecTime = like.gpsRecTime;
			this.lon = like.lon;
			this.lat = like.lat;
			this.av = like.av;
			this.speed = like.speed;
			this.name = like.name;
		}
	}
	public static class Route {
		volatile private int shovelId;
		volatile private int UOpId;
		volatile private int forwSec;
		volatile private int backSec;
		volatile private int loForwSec;
		volatile private int hiForwSec;
		volatile private int loBackSec;
		volatile private int hiBackSec;
		volatile private double forwDist;
		public double getDistBetweenEndPoint() {
			ShovelInfo shovelInfo = SimParams.getShovelInfo(this.shovelId);
			UOpParam uopInfo = SimParams.getUopParam(this.UOpId);
			double dist = 1;
			if (shovelInfo != null && uopInfo != null) {
				double rtStLon = shovelInfo.getLon();
				double rtStLat  = shovelInfo.getLat();;
				double rtEnLon  = uopInfo.getLon();
				double rtEnLat = uopInfo.getLat();
				dist = Point.fastGeoDistance(rtStLon, rtStLat, rtEnLon,rtEnLat);
			}
			return dist;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(shovelId).append(",").append(UOpId).append(",").append(forwSec).append(",").append(backSec).append(",").append(forwDist);
			return sb.toString();
		}
		private ArrayList<GpsPlus> forwRoute = new ArrayList<GpsPlus>();
		private ArrayList<GpsPlus> backRoute = new ArrayList<GpsPlus>();
		public Route clone() {
			Route retval = new 	Route(this.shovelId, this.UOpId, this.forwSec, this.backSec,
					this.loForwSec, this.hiForwSec, this.loBackSec, this.hiBackSec,
					this.forwDist);
			for (int i=0,is=this.forwRoute.size();i<is;i++) {
				retval.forwRoute.add(this.forwRoute.get(i).clone());
			}
			for (int i=0,is=this.backRoute.size();i<is;i++) {
				retval.backRoute.add(this.backRoute.get(i).clone());
			}
			return retval;
		}
		public Route copyAndExtend(ShovelInfo shovelInfo, int uopid, UOpParam uop) {
			ArrayList<GpsPlus>extension = new ArrayList<GpsPlus>();
			double dist = 0;
			GpsPlus toAddPtBase = null;
			
			boolean fromShovel = true;
			if (this.UOpId == uopid) {
				//get pts from me to this.shovel
				ShovelInfo thisShovel = SimGenerateData.getShovelInfo(this.shovelId);
				dist =  Point.fastGeoDistance(shovelInfo.getLon(), shovelInfo.getLat(), thisShovel.getLon(), thisShovel.getLat());
				toAddPtBase = new GpsPlus((long)(dist/this.forwDist*this.forwSec*1000), shovelInfo.getLon(), shovelInfo.getLat(), dist,
					20, this.forwRoute.get(0).name);
			}
			else {
				UOpParam thisUop = SimParams.getUopParam(this.UOpId);
				dist =  Point.fastGeoDistance(uop.getLon(), uop.getLat(), thisUop.getLon(), thisUop.getLat());
				toAddPtBase = new GpsPlus((long)(dist/this.forwDist*this.forwSec*1000), thisUop.getLon(), thisUop.getLat(), dist,
					20, this.forwRoute.get(this.forwRoute.size()-1).name);
				fromShovel = false;
			}
			int deltaSec = (int)(toAddPtBase.gpsRecTime/1000);
			Route retval = new Route(shovelInfo.shovelId, uopid, this.forwSec+deltaSec, this.backSec+deltaSec,
					this.loForwSec+deltaSec, this.hiForwSec+deltaSec
					,this.loBackSec+deltaSec, this.hiBackSec+deltaSec
				    ,this.forwDist+toAddPtBase.av
				    )
				    ;
			for (int art=0;art<2;art++) {
				ArrayList<GpsPlus> copyFrom = art == 0 ?this.forwRoute : this.backRoute;
				ArrayList<GpsPlus> copyTo = art == 0 ?retval.forwRoute : retval.backRoute;
				boolean addToBeg = (fromShovel && art == 0) || (!fromShovel && art == 1);
				double deltaDist = 0;
				for (int i=0,is=copyFrom.size();i<is;i++) {
					if (i == 0 && addToBeg) {
						deltaDist = toAddPtBase.av;
						GpsPlus npt = new GpsPlus(toAddPtBase);
						npt.gpsRecTime = copyFrom.get(0).gpsRecTime-npt.gpsRecTime;
						copyTo.add(npt);
					}
					copyTo.add(new GpsPlus(copyFrom.get(i)));
					copyTo.get(copyTo.size()-1).av += deltaDist;
				}
				if (!addToBeg) {
					GpsPlus npt = new GpsPlus(toAddPtBase);
					npt.gpsRecTime = copyFrom.get(copyFrom.size()-1).gpsRecTime+npt.gpsRecTime;
					npt.av += copyFrom.get(copyFrom.size()-1).av;
					copyTo.add(npt);
				}
			}
			return retval;
		}
		public Pair<Integer, Double> getNearestInfo(double lon, double lat) {
			return getNearestInfo(lon,lat,false);
		}
		public Pair<Integer, Double> getNearestInfo(double lon, double lat, boolean forw) {
			ArrayList<GpsPlus> list = forw ? forwRoute : backRoute;
			int bestIndex = -1;
			double bestDist = Double.MAX_VALUE;
			for (int i=0,is=list.size();i<is;i++) {
				double d = Point.fastGeoDistance(lon, lat, list.get(i).getLon(), list.get(i).getLat());
				if (d < bestDist) {
					bestIndex = i;
					bestDist = d;
				}
			}
			return new Pair<Integer, Double>(bestIndex, bestDist);
		}
		
		public int getShovelId() {
			return shovelId;
		}
		public void setShovelId(int shovelId) {
			this.shovelId = shovelId;
		}
		public int getUOpId() {
			return UOpId;
		}
		public void setUOpId(int opId) {
			UOpId = opId;
		}
		public ArrayList<GpsPlus> getForwRoute() {
			return forwRoute;
		}
		public void setForwRoute(ArrayList<GpsPlus> forwRoute) {
			this.forwRoute = forwRoute;
		}
		public ArrayList<GpsPlus> getBackRoute() {
			return backRoute;
		}
		public void setBackRoute(ArrayList<GpsPlus> backRoute) {
			this.backRoute = backRoute;
		}
		public int getForwSec() {
			return forwSec;
		}
		public void setForwSec(int forwSec) {
			this.forwSec = forwSec;
		}
		public int getBackSec() {
			return backSec;
		}
		public void setBackSec(int backSec) {
			this.backSec = backSec;
		}
		public int getLoForwSec() {
			return loForwSec;
		}
		public void setLoForwSec(int loForwSec) {
			this.loForwSec = loForwSec;
		}
		public int getHiForwSec() {
			return hiForwSec;
		}
		public void setHiForwSec(int hiForwSec) {
			this.hiForwSec = hiForwSec;
		}
		public int getLoBackSec() {
			return loBackSec;
		}
		public void setLoBackSec(int loBackSec) {
			this.loBackSec = loBackSec;
		}
		public int getHiBackSec() {
			return hiBackSec;
		}
		public void setHiBackSec(int hiBackSec) {
			this.hiBackSec = hiBackSec;
		}
		public double getForwDist() {
			return forwDist;
		}
		public void setForwDist(double forwDist) {
			this.forwDist = forwDist;
		}
		public Route(int shovelId, int opId, int forwSec, int backSec,
				int loForwSec, int hiForwSec, int loBackSec, int hiBackSec,
				double forwDist) {
			super();
			this.shovelId = shovelId;
			UOpId = opId;
			this.forwSec = forwSec;
			this.backSec = backSec;
			this.loForwSec = loForwSec;
			this.hiForwSec = hiForwSec;
			this.loBackSec = loBackSec;
			this.hiBackSec = hiBackSec;
			this.forwDist = forwDist;
		}
	}
	
	private static HashMap<Integer, ArrayList<Route>> g_routesByShovel = new HashMap<Integer, ArrayList<Route>>();
	private static HashMap<Integer, ArrayList<Route>> g_routesByUop = new HashMap<Integer, ArrayList<Route>>();
	private static HashMap<Integer, UOpParam> g_uopParams = new HashMap<Integer, UOpParam>();
	public static int getVal(int avg, int lo, int hi) {
		if (avg < lo)
			return 0;
		double v = Math.random();
		double retval = avg;
		if (v < 0.5) {
			retval -= (double)(avg-lo)*(0.5-v)/0.5;
		}
		else {
			retval += (double)(hi-avg)*(v-0.5)/0.5;
		}
		return (int)Math.round(retval);
	}
	public static boolean g_inited = false;
	public static boolean inited() {
		return g_inited;
	}
	public static void init(Connection conn) throws Exception {
		loadParams(conn);
		g_inited = true;
	}
	private static int getTargetPlanId(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select max(id) from dos_shift_target where is_live=1");
		ResultSet rs = ps.executeQuery();
		int retval = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return retval;
	}
	public static void loadParams(Connection conn) throws Exception{
		if (SimGenerateData.g_doLoadFromAssignment) {
			SiteStats siteStats = new SiteStats(conn, -1, true, true, true);
			siteStats.getStatsForInvPile(1, conn, 1467);
			int targetPlanId = getTargetPlanId(conn);
			
			loadShovelParamsFromAssignment(conn, siteStats, targetPlanId);
			loadUopParamsFromAssignment(conn, siteStats, targetPlanId);
			loadRouteParamsFromAssignment(conn, siteStats, targetPlanId);
			loadDumpersFromAssignment(conn, siteStats, targetPlanId);
		}
		else {
			SimParams.loadShovelParams(conn);
			SimParams.loadUopParams(conn);
			SimParams.loadRouteParams(conn);
			SimParams.loadDumpers(conn);
		}
	}
	
	public static ShovelInfo getShovelInfo(int shovelId) {
		return SimGenerateData.getShovelInfo(shovelId);
	}
	public static UOpParam getUopParam(int uopId) {
		return g_uopParams.get(uopId);
	}
	public static ArrayList<Route> getRouteListForShovel(int shoveIId) {
		return g_routesByShovel.get(shoveIId);
	}
	public static Collection<ArrayList<Route>> getAllRoutes() {
		return g_routesByUop.values();
	}
	public static ArrayList<Route> getRouteListForUOp(int uopId) {
		return g_routesByUop.get(uopId);
	}
	public static int getAssignedUopForShovel(int shovelId) {
		ArrayList<Route> routeList = getRouteListForShovel(shovelId);
		return (routeList != null && routeList.size() != 0) ? routeList.get(0).getUOpId() : Misc.getUndefInt();
	}
	
	public static synchronized Route getRouteForShovelUop(int shoveIId, int uopId) {
		ArrayList<Route> routeList = getRouteListForShovel(shoveIId);
		for (int i=0,is=routeList == null ? 0 : routeList.size(); i<is; i++) {
			if (routeList.get(i).UOpId == uopId)
				return routeList.get(i);
		}
		//not found ... let us create one  .. first check for same U, nearest shovel .. then same shovel .. nearest U
		Route bestRoute = null;
		if (true) {
			//Route route = new Route(shoveIId, uopId);
			ShovelInfo shovelInfo = SimParams.getShovelInfo(shoveIId);
			UOpParam uopInfo = SimParams.getUopParam(uopId);
			
			double rtStLon = shovelInfo.getLon();
			double rtStLat  = shovelInfo.getLat();;
			double rtEnLon  = uopInfo.getLon();
			double rtEnLat = uopInfo.getLat();
			double meDist = Point.fastGeoDistance(rtStLon, rtStLat, rtEnLon,rtEnLat);
			Route nearestRoute = null;
			double bestDi = Double.MAX_VALUE;
			Collection<ArrayList<Route>> allroutes = g_routesByShovel.values();
			for (Iterator<ArrayList<Route>> iter = allroutes.iterator(); iter.hasNext();) {
				routeList = iter.next();
				for (int i=0,is=routeList == null ? 0 : routeList.size(); i<is; i++) {
					if (nearestRoute == null || Math.abs(routeList.get(i).getDistBetweenEndPoint()-meDist) < bestDi) {
						nearestRoute = routeList.get(i);
						bestDi = Math.abs(routeList.get(i).getDistBetweenEndPoint()-meDist);
					}
				}
			}
			Route route = nearestRoute.clone();
			route.shovelId = shoveIId;
			route.UOpId = uopId;
			double ratio = nearestRoute.getDistBetweenEndPoint()/meDist;
			route.forwSec /= ratio;
			route.backSec /= ratio;
			route.hiForwSec /= ratio;
			route.hiBackSec /= ratio;
			route.loForwSec /= ratio;
			route.loBackSec /= ratio;
			route.forwDist = meDist*nearestRoute.getForwDist()/nearestRoute.getDistBetweenEndPoint();
			bestRoute = route;
		}	
		else {
			double bestDist = Double.MAX_VALUE;
			ShovelInfo meShovel = SimGenerateData.getShovelInfo(shoveIId);
			UOpParam meUop = SimParams.getUopParam(uopId);
			if (meShovel == null || meUop == null)
				return null;
			ArrayList<Route> routeForU = SimParams.g_routesByUop.get(uopId);
			for (int i=0,is=routeForU.size();i<is;i++) {
				Route r =  routeForU.get(i);
				ShovelInfo rs = SimGenerateData.getShovelInfo(r.getShovelId());
				if (rs == null)
					continue;
				double d= Point.fastGeoDistance(meShovel.getLon(), meShovel.getLat(), rs.getLon(), rs.getLat());
				if (d < bestDist) {
					bestDist = d;
					bestRoute = r;
				}
			}
			if (bestRoute == null) {
				ArrayList<Route> routeForL = SimParams.getRouteListForShovel(shoveIId);
				for (int i=0,is=routeForL.size();i<is;i++) {
					Route r =  routeForL.get(i);
					UOpParam uop = SimParams.getUopParam(uopId); 
					if (uop == null)
						continue;
					double d= Point.fastGeoDistance(meUop.getLon(), meUop.getLat(), uop.getLon(), uop.getLat());
					if (d < bestDist) {
						bestDist = d;
						bestRoute = r;
					}
				}	
			}
			if (bestRoute == null)
				return null;;
			bestRoute = bestRoute.copyAndExtend(meShovel, uopId, meUop);
		}
		if (bestRoute == null)
			return null;
		
		ArrayList<Route> temp = g_routesByShovel.get(bestRoute.shovelId);
		if (temp == null) {
			temp = new ArrayList<Route>();
			g_routesByShovel.put(bestRoute.shovelId, temp);
		}
		temp.add(bestRoute);
		temp = g_routesByUop.get(bestRoute.UOpId);
		if (temp == null) {
			temp = new ArrayList<Route>();
			g_routesByUop.put(bestRoute.UOpId, temp);
		}
		temp.add(bestRoute);
		return bestRoute;
	}
	
	private static void loadDumpers(Connection conn) throws Exception {
		//create table sim_dumper_params(dumper_id, lon double, lat double, pos_name varchar(200), start_shovel_id int, start_uop_id int, start_instruction_move_to_shovel int, primary key(dumper_id));
		PreparedStatement ps = conn.prepareStatement("select dumper_id, lon, lat, pos_name, start_shovel_id, start_uop_id from sim_dumper_params where status=1");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int dumperId =  rs.getInt(1);
			DumperInfo dumperInfo = new DumperInfo(dumperId);
			dumperInfo.setLon(rs.getDouble(2));
			dumperInfo.setLat(rs.getDouble(3));
			dumperInfo.setGpsName(rs.getString(4));
			dumperInfo.setCummDist(0);
			dumperInfo.setShovelId(rs.getInt(5));
			dumperInfo.setUopId(rs.getInt(6));
			
			//public SimInstruction(long instrTime, int applyMode, int toId, int toType)
			dumperInfo.instruction = new SimInstruction(dumperInfo.getDumperId(),-1,SimInstruction.APPLY_NOW, dumperInfo.shovelId, SimInstruction.TYPE_SHOVEL, Misc.getUndefInt());
			SimGenerateData.putDumperInfo(dumperId, dumperInfo);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	//public static HashMap<Integer, Integer> g_shovelToRefShoveMapping = new HashMap<Integer, Integer>()
//	static String g_getDumperAssignments = "select distinct assign.shovel_id, assign.dumper_id, assign.unload_site from dos_shift_plan_assignments assign join "+
//" (select shift_target_id, shovel_id, dumper_id, unload_site, max(id) from dos_shift_plan_assignments where shift_target_id=? group by shift_target_id, shovel_id, dumper_id) mx "+
//" on (mx.shift_target_id = assign.shift_target_id and mx.shovel_id = assign.shovel_id and mx.dumper_id = assign.dumper_id) "
//;
	static String g_getDumperAssignments = "select distinct assign.shovel_id, assign.dumper_id, assign.unload_site from dos_shift_plan_assignments assign  where shift_target_id=?  and assign.plan_status=1 order by dumper_id,dumper_usage_percentage desc "
	;

	private static void loadDumpersFromAssignment(Connection conn, SiteStats stats, int targetPlanId) throws Exception {
		//create table sim_dumper_params(dumper_id, lon double, lat double, pos_name varchar(200), start_shovel_id int, start_uop_id int, start_instruction_move_to_shovel int, primary key(dumper_id));
		PreparedStatement ps = conn.prepareStatement(g_getDumperAssignments);
		ps.setInt(1, targetPlanId);
		ResultSet rs = ps.executeQuery();
		ArrayList<DumperInfo> dumperList = new ArrayList<DumperInfo>();//for recording action
		HashMap<Integer, Integer> shovelCntList = new HashMap<Integer, Integer>();
		
		while (rs.next()) {
			int shovelId = rs.getInt(1);
			int dumperId =  rs.getInt(2);
			int unloadSite = rs.getInt(3);
			
			double lon  = 82.5903680920601;//rs.getDouble(4);
			double lat = 22.3283121683963;//rs.getDouble(5);
			DumperInfo dumperInfo = SimGenerateData.getDumperInfo(dumperId);
			if (dumperInfo == null) {
				dumperInfo = new DumperInfo(dumperId);
				SimGenerateData.putDumperInfo(dumperId, dumperInfo);
				dumperList.add(dumperInfo);
			}
			dumperInfo.setLon(lon);
			dumperInfo.setLat(lat);
			dumperInfo.setGpsName("Dummy");
			dumperInfo.setCummDist(0);
			if (Misc.isUndef(dumperInfo.shovelId)) {
				dumperInfo.shovelId = shovelId;
				dumperInfo.firstShovelId = shovelId;
			}
			else
				dumperInfo.secondShovelId = shovelId;
			dumperInfo.uopId = unloadSite;
			//if (toputInstr) {
			//	dumperInfo.instruction = new SimInstruction(dumperInfo.getDumperId(),-1,SimInstruction.APPLY_NOW, dumperInfo.shovelId, SimInstruction.TYPE_SHOVEL);
			//}
			//public SimInstruction(long instrTime, int applyMode, int toId, int toType)
			
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		
		for (DumperInfo dumperInfo : dumperList) {
			int s1 = dumperInfo.firstShovelId;
			int s2 = dumperInfo.secondShovelId;
			int c1 = Integer.MAX_VALUE;
			int c2 = Integer.MAX_VALUE;
			if (!Misc.isUndef(s1)) {
				c1 = shovelCntList.containsKey(s1) ? shovelCntList.get(s1) : 0;
			}
			if (!Misc.isUndef(s2)) {
				c2 = shovelCntList.containsKey(s2) ? shovelCntList.get(s2) : 0;
			}
			boolean useC1 = c1 <= c2;
			int shovelId = s1;
			int cnt = c1;
			if (!useC1) {
				shovelId = s2;
				cnt = c2;
			}
			shovelCntList.put(shovelId, cnt+1);
			dumperInfo.instruction = new SimInstruction(dumperInfo.getDumperId(),-1,SimInstruction.APPLY_NOW, shovelId, SimInstruction.TYPE_SHOVEL, Misc.getUndefInt());
			dumperInfo.lastPicked = shovelId;
			dumperInfo.shovelId = shovelId;
		///	System.out.println("[ASSIGNING] "+dumperInfo.getDumperId()+" to "+shovelId);
		}
	}
	private static  ArrayList<Pair<Integer, ArrayList<Integer>>> loadAndSetShovelToShovelMapping(Connection conn, ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>> availability) throws Exception {
		//avail 1st = inv pile id, pair.first = shovelId, pair.second = count used
		PreparedStatement ps = conn.prepareStatement("select shovel_id, inv_pile_id, target_shovel_id from sim_shovel_refshovel_map where status=1 order by inv_pile_id, target_shovel_id desc");
		ResultSet rs = ps.executeQuery();
		ArrayList<Pair<Integer, ArrayList<Integer>>> requirement = new ArrayList<Pair<Integer, ArrayList<Integer>>>();
		int prevInv = -1;
		ArrayList<Integer> shovelsForPile = null;
		
		while (rs.next()) {
			int shovelId = rs.getInt(1);
			int invPileId = rs.getInt(2);
			int target = Misc.getRsetInt(rs,3);
			if (prevInv != invPileId)
				shovelsForPile = null;
			if (shovelsForPile == null) {
				for (int i=0,is=requirement.size();i<is;i++) {
					if (requirement.get(i).first == invPileId) {
						shovelsForPile = requirement.get(i).second;
						break;
					}
				}
				if (shovelsForPile == null) {
					shovelsForPile = new ArrayList<Integer>();
					requirement.add(new Pair<Integer, ArrayList<Integer>>(invPileId, shovelsForPile));
				}
				prevInv = invPileId;
			}
			shovelsForPile.add(shovelId);
			if (!Misc.isUndef(target)) {
				ShovelInfo.g_shovelToRefShoveMapping.put(shovelId, target);
				 ArrayList<MiscInner.Pair> availListForInv = helpGetEntryInAvail(invPileId, availability);
				 MiscInner.Pair availEntry = helpGetEntryInAvailForShovel(target, availListForInv);
				 if (availEntry != null) {
					 availEntry.second = availEntry.second++;
				 }
			}
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return requirement;
	}
	private static ArrayList<MiscInner.Pair> helpGetEntryInAvail(int invPileId, ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>> availability) {
		for (int i=0,is=availability == null ? 0 : availability.size();i<is;i++)
			if (availability.get(i).first == invPileId)
				return availability.get(i).second;
		return null;
	}
	private static MiscInner.Pair helpGetEntryInAvailForShovel(int shovelId,  ArrayList<MiscInner.Pair> availability) {
		for (int i=0,is=availability == null ? 0 : availability.size();i<is;i++)
			if (availability.get(i).first == shovelId)
				return availability.get(i);
		return null;
	}
	private static MiscInner.Pair getBestFreeEntry(ArrayList<MiscInner.Pair> availability) {
		MiscInner.Pair retval = null;
		for (int i=0,is=availability == null ? 0 : availability.size();i<is;i++)
			if (retval == null || availability.get(i).second < retval.second) {
				retval = availability.get(i);
			}
		return retval;
	}
	private static void loadShovelParams(Connection conn) throws Exception {
		//create table sim_shovel_params(shovel_id int, cycle_time int, num_cycles int, cleanup_time int, cleanup_for_trip int, positioning_time int, lo_cycle_time time, hi_cycle_time int, lo_num_cycles int, int hi_num_cycles, lo_cleanup_time int, hi_cleanup_time int, lo_positioning_time int, hi_positioning_time int, primary key(shovel_id));
		PreparedStatement ps = conn.prepareStatement("select shovel_id, cycle_time, num_cycles, cleanup_time, cleanup_for_trip, positioning_time, lo_cycle_time, hi_cycle_time, lo_num_cycles, hi_num_cycles, lo_cleanup_time, hi_cleanup_time, lo_positioning_time, hi_positioning_time, lon, lat,pos_name,inv_pile_id from sim_shovel_params where status=1 order by inv_pile_id");
		ResultSet rs = ps.executeQuery();
		ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>> availability = new ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>>();
		HashMap<Integer, ShovelParam> shovelParams = new HashMap<Integer, ShovelParam>();
		
		int prevInvPileId = -1;
		ArrayList<MiscInner.Pair> avail = null;
		while (rs.next()) {
			int shovelId =  rs.getInt(1);
			ShovelParam shovelParam = new ShovelParam(rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getInt(7),rs.getInt(8),rs.getInt(9),rs.getInt(10),rs.getInt(11), rs.getInt(12),rs.getInt(13),rs.getInt(14), rs.getDouble(15), rs.getDouble(16), rs.getString(17), Misc.getRsetInt(rs,18));
			int invPileId = shovelParam.getInvPileId();
			shovelParams.put(shovelId, shovelParam);
			
			if (prevInvPileId != invPileId)
				avail = null;
			if (avail == null) {
				for (int i=0,is=availability.size();i<is;i++) {
					if (availability.get(i).first == invPileId) {
						avail = availability.get(i).second;
						break;
					}
				}
				if (avail == null) {
					avail = new ArrayList<MiscInner.Pair>();
					availability.add(new Pair<Integer, ArrayList<MiscInner.Pair>>(invPileId, avail));
				}
				prevInvPileId = invPileId;
			}
			avail.add(new MiscInner.Pair(shovelId,0));
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ArrayList<Pair<Integer, ArrayList<Integer>>> requirement = loadAndSetShovelToShovelMapping(conn, availability);
		ArrayList<MiscInner.Pair> availListForInv = null;
		prevInvPileId = -1;
		for (int i=0,is=requirement.size();i<is;i++) {
			int invPileId = requirement.get(i).first;
			if (prevInvPileId != invPileId)
				availListForInv = null;
			if (availListForInv == null) {
				availListForInv = helpGetEntryInAvail(invPileId, availability);
				if (availListForInv == null) {
					 requirement.remove(i);
					 i--;
					 is--;
					 continue;
				}
				prevInvPileId = invPileId;
			 }
			for (int j=0,js=requirement.get(i).second.size();j<js;j++) {
				int aksShovelId = requirement.get(i).second.get(j);
				if (ShovelInfo.g_shovelToRefShoveMapping.get(aksShovelId) == null) {
					MiscInner.Pair availEntry = getBestFreeEntry(availListForInv);
					ShovelInfo.g_shovelToRefShoveMapping.put(aksShovelId, availEntry.first);
					availEntry.second += 1;
				}
				int mappedShovelId = ShovelInfo.g_shovelToRefShoveMapping.get(aksShovelId);
				ShovelParam mappedShoveParam = shovelParams.get(mappedShovelId);
				ShovelInfo askedShovelInfo = SimGenerateData.putShovelInfo(aksShovelId, mappedShoveParam);
				askedShovelInfo.setLon(mappedShoveParam.getLon());
				askedShovelInfo.setLat(mappedShoveParam.getLat());
				askedShovelInfo.setGpsName(mappedShoveParam.getPosName());
			}
		}
	}
	private static String g_shovelParamFromPlan = " select distinct assign.inv_pile, assign.shovel_id "+
	 ", (case when  assign.inv_pile = shovelInitParams.curr_pos and shovelInitParams.avg_cycle_time is not null then shovelInitParams.avg_cycle_time else siteShovelParams.cycle_time end) cycle_time "+
	 ", (case when  assign.inv_pile = shovelInitParams.curr_pos and shovelInitParams.avg_fill_factor is not null then shovelInitParams.avg_fill_factor else siteparams.fill_factor end) fill_factor "+
	 ", vehicle_types.id shovel_type "+
	 "from dos_shift_plan_assignments assign join vehicle on (vehicle.id = assign.shovel_id)  "+
	 "left outer join vehicle_types  on (vehicle_types.vehicle_type_lov = vehicle.type)  "+
	 "left outer join dos_shift_target_vehicle shovelInitParams on (assign.shift_target_id = shovelInitParams.shift_target_id and shovelInitParams.vehicle_id = vehicle.id) "+
	 "left outer join dos_r_target_site_params siteParams on (siteParams.shift_target_id = assign.shift_target_id) "+
	 "left outer join dos_load_site_shoveltypes_params siteShovelParams on (siteShovelParams.target_site_param_id = siteParams.id and siteShovelParams.shovel_type=vehicle_types.id) "+
	 "where assign.shift_target_id=? and assign.plan_status=1 ";
	private static void loadShovelParamsFromAssignment(Connection conn, SiteStats siteStats, int targetPlanId) throws Exception {
		double percVariationCycleTim = 0.1;
		double percVariationNumCycle = 0.1;
		
		PreparedStatement ps = conn.prepareStatement(g_shovelParamFromPlan);
		ps.setInt(1, targetPlanId);
		ResultSet rs = ps.executeQuery();
		//ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>> availability = new ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>>();
		//HashMap<Integer, ShovelParam> shovelParams = new HashMap<Integer, ShovelParam>();
		
		int prevInvPileId = -1;
		ArrayList<MiscInner.Pair> avail = null;
		while (rs.next()) {
			int assignedInv = Misc.getRsetInt(rs,1);
			int shovelId =  rs.getInt(2);
			
			Triple<Double, Double, Integer> shovelposInv = SiteStats.getSimpleInventoryPileForShovel(shovelId);
		
			if (shovelposInv != null && Misc.isUndef(assignedInv))
				assignedInv = shovelposInv.third;
			SiteStats.InvPile invPile = SiteStats.getInvPile(assignedInv);
			if (invPile == null)
				continue;
			double lon = shovelposInv == null ? Misc.getUndefDouble() : shovelposInv.first;
			double lat = shovelposInv == null ? Misc.getUndefDouble() : shovelposInv.second;
			if (Misc.isUndef(lon)) {
				lon = invPile.getLon();
				lat = invPile.getLat();
			}
			
			double cycleTime = Misc.getRsetDouble(rs, 3);
			double fillFactor = Misc.getRsetDouble(rs, 4);
			int shovelType = Misc.getRsetInt(rs, 5);
			SiteStats.CapacityDef capacityDef = siteStats.getCapacityDefById(shovelType);
			if (Misc.isUndef(cycleTime) || Misc.isUndef(fillFactor)) {
				SiteStats.Stats invPileStat = siteStats.getSimpleStatsForInvPile(invPile.getId());
				if (Misc.isUndef(cycleTime))
					cycleTime = invPileStat.getCycleTime(shovelType);
				if (Misc.isUndef(fillFactor))
					fillFactor = invPileStat.getAvgFillFactor();
			}
			double dumperCapVol = SiteStats.getAvgCapacityVol(0);
			double shovelCapVol = capacityDef == null ? 1 : capacityDef.getCapVol();
			double numCycles = 6;
			if (!Misc.isUndef(shovelCapVol) && !Misc.isUndef(dumperCapVol) && !Misc.isUndef(fillFactor)) {
				numCycles = dumperCapVol/shovelCapVol*fillFactor;
			}
			double clearingTime = invPile == null ? Misc.getUndefDouble() : invPile.getClearingTime();
			double clearingCycle = invPile == null ? Misc.getUndefDouble() : invPile.getClearingCycles();
			double positioningTime = invPile == null ? Misc.getUndefDouble() : invPile.getPositioningTime();
			if (Misc.isUndef(cycleTime))
				cycleTime = 18;
			if (Misc.isUndef(clearingTime))
				clearingTime = 120;
			if (Misc.isUndef(clearingCycle))
				clearingCycle = 4;
			if (Misc.isUndef(positioningTime))
				positioningTime = 28;
			double percDev = 0.15;
			ShovelParam shovelParam = new ShovelParam(
					(int) Math.round(cycleTime)
					, (int) Math.round(numCycles)
					, (int) Math.round(clearingTime)
					, (int) Math.round(clearingCycle)
					, (int) Math.round(positioningTime)
					,(int) Math.round(cycleTime*(1-percDev))
					,(int) Math.round(cycleTime*(1+percDev))
					, (int) Math.round(numCycles*(1-percDev))
					, (int) Math.round(numCycles*(1+percDev))
					, (int) Math.round(clearingTime*(1-percDev))
					, (int) Math.round(clearingTime*(1+percDev))
					, (int) Math.round(positioningTime*(1-percDev))
					, (int) Math.round(positioningTime*(1+percDev))
					,lon,lat,"Dummy",invPile.getId()
					);
			
			int invPileId = shovelParam.getInvPileId();
			ShovelInfo askedShovelInfo = SimGenerateData.putShovelInfo(shovelId, shovelParam);
			askedShovelInfo.setLon(shovelParam.getLon());
			askedShovelInfo.setLat(shovelParam.getLat());
			askedShovelInfo.setGpsName(shovelParam.getPosName());
			//shovelParams.put(shovelId, shovelParam);
			ShovelInfo.g_shovelToRefShoveMapping.put(shovelId, shovelId);
		//	System.out.println("[Shovel]"+askedShovelInfo.getShovelId()+" CT "+shovelParam.getCycleTime()+" NC"+shovelParam.getNumCycles());
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	private static void loadUopParams(Connection conn) throws Exception {
		//create table sim_uop_params(uop_id int, unload_time int, lo_unload_time time, hi_unload_time int, primary key(uop_id));
		//create table sim_route_params(shovel_id int, uop_id int, forw_sec int, back_sec int, lo_frow_sec int, hi_forw_sec int, lo_back_sec int, hi_back_sec int, forw_dist double, forw_trip_id int, back_trip_id int, primary key(shovel_id, uop_id));
		PreparedStatement ps = conn.prepareStatement("select uop_id, unload_time, lo_unload_time, hi_unload_time,lon,lat from sim_uop_params where status=1");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int uopId =  rs.getInt(1);
			UOpParam param = new UOpParam(rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getDouble(5), rs.getDouble(6));
			g_uopParams.put(uopId, param);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	public static ArrayList<Integer> getUopList() {
		Set<Integer> uopIds = g_routesByUop.keySet();
		ArrayList<Integer> retval = new ArrayList<Integer>();
		for (Iterator<Integer> iter = uopIds.iterator();iter.hasNext();) {
			retval.add(iter.next());
		}
		return retval;
	}
	private static void loadUopParamsFromAssignment(Connection conn, SiteStats siteStats, int targetPlanId) throws Exception {
		//create table sim_uop_params(uop_id int, unload_time int, lo_unload_time time, hi_unload_time int, primary key(uop_id));
		//create table sim_route_params(shovel_id int, uop_id int, forw_sec int, back_sec int, lo_frow_sec int, hi_forw_sec int, lo_back_sec int, hi_back_sec int, forw_dist double, forw_trip_id int, back_trip_id int, primary key(shovel_id, uop_id));
		double avgUnloadTime = SiteStats.getAvgKeyMetric2(0);
		if (Misc.isUndef(avgUnloadTime))
			avgUnloadTime = 54;
		double percDev = 0.2;
		ArrayList<SiteStats.InvPile> unloadSiteList = SiteStats.getSiteList(2, siteStats.getRefTS());
		for (SiteStats.InvPile pile:unloadSiteList) {
			UOpParam param = new UOpParam((int)(Math.round(avgUnloadTime)), (int)(Math.round(avgUnloadTime*(1-percDev))), (int)(Math.round(avgUnloadTime*(1+percDev))),pile.getLon(), pile.getLat());
			g_uopParams.put(pile.getId(), param);
		}
	}
	private static void loadRouteParams(Connection conn) throws Exception {
		//create table sim_route_params(shovel_id int, uop_id int, forw_sec int, back_sec int, lo_frow_sec int, hi_forw_sec int, lo_back_sec int, hi_back_sec int, forw_dist double, forw_trip_id int, back_trip_id int, primary key(shovel_id, uop_id));
		PreparedStatement ps = conn.prepareStatement("select shovel_id, uop_id, forw_sec, back_sec, lo_frow_sec, hi_forw_sec, lo_back_sec, hi_back_sec, forw_dist, forw_trip_id, back_trip_id from sim_route_params where status=1");
		ResultSet rs = ps.executeQuery();
		HashMap<Integer, ArrayList<Route>> routesByShovel = new HashMap<Integer, ArrayList<Route>>();
		while (rs.next()) {
			Route param = new Route(rs.getInt(1),rs.getInt(2),rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getDouble(9));
			int forwTripId =  rs.getInt(10);
			int backTripId = rs.getInt(11);
			ArrayList<GpsPlus> forwtrip = loadTrip(conn, forwTripId, true);
			ArrayList<GpsPlus> backtrip = loadTrip(conn, backTripId, false);
			param.setForwRoute(forwtrip);
			param.setBackRoute(backtrip);
			if (true) {
				int forwActSec = (int)(forwtrip.get(forwtrip.size()-1).gpsRecTime-forwtrip.get(0).gpsRecTime)/1000;
				int backActSec = (int)(backtrip.get(backtrip.size()-1).gpsRecTime-backtrip.get(0).gpsRecTime)/1000;
				if (Math.abs(backActSec-param.backSec) > 30) {
					helperAdjust(backtrip,backActSec,param.backSec);
				}
				else {
					param.backSec = backActSec;
				}
				if (Math.abs(forwActSec-param.forwSec) > 30) {
					helperAdjust(forwtrip,forwActSec,param.forwSec);
				}
				else {
					param.forwSec = forwActSec;
				}
			}
			ArrayList<Route> temp = routesByShovel.get(param.shovelId);
			if (temp == null) {
				temp = new ArrayList<Route>();
				routesByShovel.put(param.shovelId, temp);
			}
			temp.add(param);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		//now map as per shovel to mapped ..
		Set<Entry<Integer, Integer>> entries = ShovelInfo.g_shovelToRefShoveMapping.entrySet();
		for (Iterator<Entry<Integer, Integer>> entry = entries.iterator();entry.hasNext();) {
			Entry<Integer, Integer> item = entry.next();
			int askShovel = item.getKey();
			int mapShovel = item.getValue();
			ArrayList<Route> temp = routesByShovel.get(mapShovel);
			ArrayList<Route> temp2 = g_routesByShovel.get(askShovel);
			if (temp2 == null) {
				temp2 = new ArrayList<Route>();
				g_routesByShovel.put(askShovel, temp2);
			}
			for (int j=0,js=temp == null ? 0 : temp.size(); j<js;j++) {
				Route rt = temp.get(j).clone();
				rt.setShovelId(askShovel);
				temp2.add(rt);
				
				ArrayList<Route>temp3 = g_routesByUop.get(rt.UOpId);
				if (temp3 == null) {
					temp3 = new ArrayList<Route>();
					g_routesByUop.put(rt.UOpId, temp3);
				}
				temp3.add(rt);
			}
		}
		
	}
	static String g_getRouteWithDist = "select  assign.shovel_id, assign.unload_site "+
	 " , max(case when  assign.inv_pile = shovelInitParams.curr_pos and shovelRouteParams.lead_load is not null then shovelRouteParams.lead_load else routes.lead_load end) lead_load "+
	 " , max(case when  assign.inv_pile = shovelInitParams.curr_pos and shovelRouteParams.fwd_difficulty is not null then shovelRouteParams.fwd_difficulty else routes.fwd_difficulty end) fwd "+
	 " , max(case when  assign.inv_pile = shovelInitParams.curr_pos and shovelRouteParams.bkwrd_difficulty is not null then shovelRouteParams.bkwrd_difficulty else routes.bkwrd_difficulty end) back "+
	 " , assign.inv_pile "+
	 " from "+ 
	 " dos_route_target routes join dos_route_def routeDef on (routes.shift_target_id=? and routes.route_id = routeDef.id) "+
	" join dos_shift_plan_assignments assign on (assign.shift_target_id = routes.shift_target_id  and assign.plan_status=1 and assign.inv_pile=routeDef.site_id and assign.unload_site = routeDef.dest_id)  "+
	" left outer join dos_shift_target_vehicle shovelInitParams on (assign.shift_target_id = shovelInitParams.shift_target_id and shovelInitParams.vehicle_id = assign.shovel_id) "+
	" left outer join dos_target_shovel_params shovelRouteParams on (assign.shift_target_id = assign.shift_target_id and assign.shovel_id = shovelRouteParams.shovel_id and shovelRouteParams.dest_id = routeDef.dest_id) "+
	" group by assign.shovel_id, assign.unload_site, assign.inv_pile "
	;

	private static void loadRouteParamsFromAssignment(Connection conn, SiteStats siteStats, int targetPlanId) throws Exception {
		//create table sim_route_params(shovel_id int, uop_id int, forw_sec int, back_sec int, lo_frow_sec int, hi_forw_sec int, lo_back_sec int, hi_back_sec int, forw_dist double, forw_trip_id int, back_trip_id int, primary key(shovel_id, uop_id));
		double avgSpeed = SiteStats.getAvgKeyMetric(0);
		PreparedStatement ps = conn.prepareStatement(g_getRouteWithDist);
		ps.setInt(1, targetPlanId);
		ResultSet rs = ps.executeQuery();
		HashMap<Integer, ArrayList<Route>> routesByShovel = new HashMap<Integer, ArrayList<Route>>();
		while (rs.next()) {
			int shovelId = Misc.getRsetInt(rs, 1);
			int destId = Misc.getRsetInt(rs, 2);
			double dist = Misc.getRsetDouble(rs, 3);
			double forwDiff = Misc.getRsetDouble(rs, 4);
			double backDiff = Misc.getRsetDouble(rs, 5);
			if (Misc.isUndef(dist) || Misc.isUndef(forwDiff) || Misc.isUndef(backDiff)) {
				int pileId = Misc.getRsetInt(rs, 6);
				Triple<Double, Double, Integer> posInfo = siteStats.getSimpleInventoryPileForShovel(shovelId);
				int shovelPile = posInfo == null ? Misc.getUndefInt() : posInfo.third;
				SiteStats.Stats stats = pileId != shovelPile ? siteStats.getStatsForInvPile(pileId, conn, 1467)
						: siteStats.getStatsForShovel(shovelId, conn, 1467);
				if (Misc.isUndef(dist))
					dist = stats.getDestLeadDist(destId);
				if (Misc.isUndef(forwDiff))
					forwDiff = stats.getForwRouteDifficulty(destId);
				if (Misc.isUndef(backDiff))
					backDiff = stats.getBackRouteDifficulty(destId);
			}
			if (Misc.isUndef(forwDiff))
				forwDiff = 1;
			if (Misc.isUndef(backDiff))
				backDiff = 1;
			if (Misc.isUndef(dist))
				dist = 2;
			double forwSec = dist/(avgSpeed*forwDiff)*3600;
			double backSec = dist/(avgSpeed*backDiff)*3600;
			forwSec = getBaseAverageTransitAdjustement(forwSec);
			backSec = getBaseAverageTransitAdjustement(backSec);
			double percDev = 0.2;
			Route route = new Route(shovelId, destId, (int)Math.round(forwSec), (int)Math.round(backSec),
					(int)Math.round(forwSec*(1-percDev)), (int)Math.round(forwSec*(1+percDev)), (int)Math.round(backSec*(1-percDev)), (int)Math.round(backSec*(1+percDev)),
					dist);
			ArrayList<GpsPlus> forwtrip = new ArrayList<GpsPlus>();
			ArrayList<GpsPlus> backtrip = new ArrayList<GpsPlus>();
			route.setForwRoute(forwtrip);
			route.setBackRoute(backtrip);
			
			ArrayList<Route> temp = routesByShovel.get(route.shovelId);
			if (temp == null) {
				temp = new ArrayList<Route>();
				routesByShovel.put(route.shovelId, temp);
			}
			temp.add(route);
		//	System.out.println("[Route]"+route.shovelId+" TO "+destId+ " dist:"+route.forwDist+" ForwS:"+route.forwSec+" BackS:"+route.backSec);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		//now map as per shovel to mapped ..
		Set<Entry<Integer, Integer>> entries = ShovelInfo.g_shovelToRefShoveMapping.entrySet();
		for (Iterator<Entry<Integer, Integer>> entry = entries.iterator();entry.hasNext();) {
			Entry<Integer, Integer> item = entry.next();
			int askShovel = item.getKey();
			int mapShovel = item.getValue();
			ArrayList<Route> temp = routesByShovel.get(mapShovel);
			ArrayList<Route> temp2 = g_routesByShovel.get(askShovel);
			if (temp2 == null) {
				temp2 = new ArrayList<Route>();
				g_routesByShovel.put(askShovel, temp2);
			}
			for (int j=0,js=temp == null ? 0 : temp.size(); j<js;j++) {
				Route rt = temp.get(j).clone();
				rt.setShovelId(askShovel);
				temp2.add(rt);
				
				ArrayList<Route>temp3 = g_routesByUop.get(rt.UOpId);
				if (temp3 == null) {
					temp3 = new ArrayList<Route>();
					g_routesByUop.put(rt.UOpId, temp3);
				}
				temp3.add(rt);
			}
		}
		
	}
	private static void 	helperAdjust(ArrayList<GpsPlus>backtrip, int backActSec, int backSec) {
		double frac = backSec/backActSec;
		long tsInit = backtrip.get(0).gpsRecTime;
		for (int i=0,is=backtrip.size()-1;i<is;i++) {
			long ts = backtrip.get(i).gpsRecTime;
			ts = tsInit+(long)((ts-tsInit)*frac);
			ts = ts/1000*1000;
			backtrip.get(i).gpsRecTime = ts;
		}
		
	}

	private static String forwRouteQ = "select l.gps_record_time, l.longitude, l.latitude, l.attribute_value, l.speed, l.name,b.attribute_value from trip_info_pb_ref t join logged_data_pb_ref l on (l.vehicle_id = t.vehicle_id and l.attribute_id=0 and l.gps_record_time between t.adj_lgout and t.unload_gate_in)  left outer join logged_data_pb_ref b on (b.vehicle_id=t.vehicle_id and b.attribute_id=0 and b.gps_record_time=t.adj_lgin) where t.id = ? order by l.gps_record_time";
	private static String backRouteQ = "select l.gps_record_time, l.longitude, l.latitude, l.attribute_value, l.speed, l.name, b.attribute_value from trip_info_pb_ref t join trip_info_pb_ref nt on (nt.id=t.next_trip_id) join logged_data_pb_ref l on (l.vehicle_id = t.vehicle_id and l.attribute_id=0 and l.gps_record_time between t.unload_gate_out and nt.adj_lgin) left outer join logged_data_pb_ref b on (b.vehicle_id=t.vehicle_id and b.attribute_id=0 and b.gps_record_time=t.unload_gate_in) where t.id = ? order by l.gps_record_time";
	private static ArrayList<GpsPlus> loadTrip(Connection conn, int tripId, boolean forwDir) throws Exception {
		ArrayList<GpsPlus> retval = new ArrayList<GpsPlus>();
		
		PreparedStatement ps = conn.prepareStatement(forwDir ? forwRouteQ : backRouteQ);
		ps.setInt(1, tripId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			GpsPlus dat = new GpsPlus(Misc.sqlToLong(rs.getTimestamp(1)),rs.getDouble(2),rs.getDouble(3),rs.getDouble(4)-rs.getDouble(7),rs.getDouble(5),rs.getString(6));
			retval.add(dat);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return retval;
	}

	
	/////////
	public static int NO_POS_AVG = 0;//8;
	public static int NO_POS_LO = 5;
	public static int NO_POS_HI = 12;
	public static int MOVE_DELAY = 0;
	public static int MOVE_DELAY_LO = 10;
	public static int MOVE_DELAY_HI = 40;
	public static int UNLOAD_AFTER_DELAY = 0;
	public static int UNLOAD_AFTER_DELAY_LO = 10;
	public static int UNLOAD_AFTER_DELAY_HI = 40;
	
	public static final double g_extraOrdinaryShovelDelayInBetweenProb = 1.0/15.0;
	public static final double g_extraOrdinaryDumperDelayAfterLoad = 1.0/15.0;
	public static final double g_extraOrdinaryDumperDelayAfterUnload = 1.0/10.0;
	public static final double g_extraOrdinaryDumperDelayInTransit = 1.0/10.0;
	public static final double g_extraOrdinaryDelayLoSec = 3*60;
	public static final double g_extraOrdinaryDelayHiSec = 5*60;
	public static double getBaseAverageTransitAdjustement(double forwSec) {
		//return ((1.0-SimGenerateData.g_extraordinaryDelayRegime)*forwSec + (SimGenerateData.g_extraordinaryDelayRegime)*(forwSec+(g_extraOrdinaryDelayLoSec+g_extraOrdinaryDelayHiSec)/2))
		//	/ forwSec;
		return forwSec - (SimGenerateData.g_extraordinaryDelayRegime*g_extraOrdinaryDumperDelayInTransit)*((g_extraOrdinaryDelayLoSec+g_extraOrdinaryDelayHiSec)/2); 
	}
	public static int getExtraShovelDelay() {
		return (int)Math.round(getExtraOrdinaryDelay(g_extraOrdinaryShovelDelayInBetweenProb));
	}
	public static int getExtraAfterLoadDelay() {
		return (int)Math.round(getExtraOrdinaryDelay(g_extraOrdinaryDumperDelayAfterLoad));
	}
	public static int getExtraAfterUnloadDelay() {
		return (int)Math.round(getExtraOrdinaryDelay(g_extraOrdinaryDumperDelayAfterUnload));
	}
	public static int getExtraAfterTransit() {
		return (int)Math.round(getExtraOrdinaryDelay(g_extraOrdinaryDumperDelayInTransit));
	}
	private static double getExtraOrdinaryDelay(double prob) {
		double delay = 0;
		boolean toDo = SimGenerateData.g_extraordinaryDelayRegime == 0 ? false : Math.random() < SimGenerateData.g_extraordinaryDelayRegime*prob;
		if (toDo) {
			delay = g_extraOrdinaryDelayLoSec+Math.random()*(g_extraOrdinaryDelayHiSec-g_extraOrdinaryDelayLoSec);
		}
		return delay;
	}
	public static void main(String a[]) throws Exception {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		SimGenerateData simgen = new SimGenerateData();
		simgen.generateAndGetTS(conn, 0);
	}
}

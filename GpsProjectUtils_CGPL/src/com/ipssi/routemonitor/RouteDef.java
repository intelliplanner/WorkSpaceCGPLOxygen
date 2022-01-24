package com.ipssi.routemonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.mapguideutils.ShapeFileBean;
public class RouteDef {
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static HashMap<Integer, RouteDef> g_routeDefs = null;//route id to def
	private static ArrayList<RouteDef> g_routeDefList = null;
	private static ArrayList<PointInfo> g_pointInfoLookup = null;////ONLY start point in g_pointInfoLookup ... end will have id of odd
	private static HashMap<Integer, Pair<Double,RTree>> g_routePointInfo = null;//port node id to corresponding maxDistBetweenPts, RTree
	
	private int id = Misc.getUndefInt();
	private int portNodeId = Misc.getUndefInt();
	private boolean doBuzz = true;
	private float marginKm = 0.1f;
	private ArrayList<MiscInner.PairFloat> points = new ArrayList<MiscInner.PairFloat>();
	private ArrayList<PointPairAddnlInfo> addnlInfo = new ArrayList<PointPairAddnlInfo>();
	private double maxDeltaLonLatBetweenPoints = 0.0005;
	private boolean lafargeLike = true;
	public static class NearestInfo {
		private short routeIndex;
		private short ptIndex;
		private int routeId;
		private double origLon;
		private double origLat;
		private PointPairAddnlInfo addnlInfo;
		private double alpha;
		private double vertDist;
		private boolean onRoute;
		public int getRouteIndex() {
			return routeIndex;
		}
		public int getPointIndex() {
			return ptIndex;
		}
		public void setRouteIndex(short routeIndex) {
			this.routeIndex = routeIndex;
		}
		public void setPointIndex(short ptIndex) {
			this.ptIndex = ptIndex;
		}
		public int getRouteId() {
			return routeId;
		}
		public void setRouteId(int routeId) {
			this.routeId = routeId;
		}
		public double getOrigLon() {
			return origLon;
		}
		public void setOrigLon(double origLon) {
			this.origLon = origLon;
		}
		public double getOrigLat() {
			return origLat;
		}
		public void setOrigLat(double origLat) {
			this.origLat = origLat;
		}
		public PointPairAddnlInfo getAddnlInfo() {
			return addnlInfo;
		}
		public void setAddnlInfo(PointPairAddnlInfo addnlInfo) {
			this.addnlInfo = addnlInfo;
		}
		public double getAlpha() {
			return alpha;
		}
		public void setAlpha(double alpha) {
			this.alpha = alpha;
		}
		public double getVertDist() {
			return vertDist;
		}
		public void setVertDist(double vertDist) {
			this.vertDist = vertDist;
		}
		public NearestInfo(short routeIndex, short ptIndex, int routeId, double origLon, double origLat, PointPairAddnlInfo addnlInfo, double alpha, double vertDist, boolean onRoute) {
			super();
			this.routeIndex = routeIndex;
			this.ptIndex = ptIndex;
			this.routeId = routeId;
			this.origLon = origLon;
			this.origLat = origLat;
			this.addnlInfo = addnlInfo;
			this.alpha = alpha;
			this.vertDist = vertDist;
			this.onRoute = onRoute;
		}
		public boolean isOnRoute() {
			return onRoute;
		}
		public void setOnRoute(boolean onRoute) {
			this.onRoute = onRoute;
		}
	}
	public static void resetCache(Connection conn) {
		load(conn,true);
	}
	public static NearestInfo getNearestPossibleRoutes(Connection conn, double lon, double lat, int portNodeId, Cache cache, boolean justCheckIfSomeRoad, double threshDist) {
		NearestInfo retval= null;
		RouteDef.load(conn, false);		
		try {
			getReadLock();
			com.infomatiq.jsi.Point pt = (new com.infomatiq.jsi.Point((float)lon,(float)lat));
			double mindist= Misc.LARGE_NUMBER;
			for (MiscInner.PortInfo curr = cache.getPortInfo(portNodeId, conn); curr != null; curr = curr.m_parent) {
				Pair<Double, RTree> maxDegDistRTree = RouteDef.g_routePointInfo.get(curr.m_id);
				if (maxDegDistRTree == null)
					continue;
				double dist = maxDegDistRTree.first;
				List arrayOfIds=new ArrayList();
				PrintId pid=new PrintId(); 
				RTree rtree=maxDegDistRTree.second;
				synchronized (rtree) {
					rtree.nearestN(pt, pid,20, (float)dist);
					arrayOfIds=pid.getArrayOfIds();
				}
				
				for(int j=0, js = arrayOfIds.size();j<js;j++){
					PointInfo info = getPointInfo((Integer) arrayOfIds.get(j));
					if (info == null)
						continue;
					NearestInfo nearestInfo = RouteDef.computeNearest(info, lon, lat, threshDist);
					retval = getBestOf(retval, nearestInfo);
					if (justCheckIfSomeRoad && retval != null && retval.isOnRoute())
						break;
				}//for each nearest pt
				if (justCheckIfSomeRoad && retval != null && retval.isOnRoute())
					break;
			}//for each parent port node			
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			releaseReadLock();
		}
		return retval;
	}

	public static RouteDef getRouteDef(Connection conn, int id) {
		load(conn,false);
		try {
			getReadLock();
			return g_routeDefs.get(id);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
			//eat it
		}
		finally {
			releaseReadLock();
		}
	}
	
	private  PointPairAddnlInfo getPointPairAddnlInfo(int idx) {
		int actIdx = idx/2;
		if (actIdx < addnlInfo.size())
			return addnlInfo.get(actIdx);
		return null;
	}
	public static PointInfo getPointInfo(int idx) {
		return g_pointInfoLookup.get(idx);
	}
	
	public static class PointInfo {
		private short routeIndex;
		private short pointIndex;
		public short getRouteIndex() {
			return routeIndex;
		}
		public void setRouteIndex(short routeIndex) {
			this.routeIndex = routeIndex;
		}
		public short getPointIndex() {
			return pointIndex;
		}
		public void setPointIndex(short pointIndex) {
			this.pointIndex = pointIndex;
		}

		public PointInfo(short routeIndex, short pointIndex) {
			this.routeIndex = routeIndex;
			this.pointIndex = pointIndex;
		}
	}
	private static int classifyAlpha(double alpha) {//returns -1 if < 0, 0 if between 0 and 1 and 1 if > 1
		return alpha < 0 && !Misc.isEqual(alpha, 0) ? -1 : alpha > 1 && !Misc.isEqual(alpha, 1) ? 1 : 0;
	}
	private static boolean toReplaceMeAlphaDist(double meAlpha, double meDist, boolean meOnRoute, double otherAlpha, double otherDist, boolean otherOnRoute) {
		if (meOnRoute != otherOnRoute)
			return otherOnRoute;
		if (!meOnRoute)
			return Misc.isUndef(meDist) || (otherDist < meDist && !Misc.isEqual(otherDist, meDist));
		int meCl = classifyAlpha(meAlpha);
		int otherCl = classifyAlpha(otherAlpha);
		return meCl == 0 && otherCl != 0 ? false : meCl != 0 && otherCl == 0 ? true : otherDist < meDist && !Misc.isEqual(otherDist, meDist);
	}
	private static NearestInfo getBestOf(NearestInfo me, NearestInfo other) {
		if (me != null && other != null) {
			return toReplaceMeAlphaDist(me.getAlpha(), me.getVertDist(), me.isOnRoute(), other.getAlpha(), other.getVertDist(), other.isOnRoute()) ? other : me;
		}
		else 
			return me != null ? me : other;
	}
	private static NearestInfo computeNearest(PointInfo ptInfo, double lon, double lat, double threshDist) {//assumes in read Lock ... returns null if vertDist > threshDist
		short routeIndex = ptInfo.routeIndex;
		short ptIndex =  ptInfo.pointIndex;
		if (routeIndex < 0 || routeIndex >= g_routeDefList.size())
			return null;
		RouteDef rd = RouteDef.g_routeDefList.get(routeIndex);
		if (ptIndex < 0 || ptIndex >= rd.points.size())
			return null;
		MiscInner.PairFloat prior = ptIndex == 0 ? null : rd.points.get(ptIndex-1);
		MiscInner.PairFloat curr = rd.points.get(ptIndex);
		MiscInner.PairFloat next = ptIndex >= rd.points.size()-1 ? null : rd.points.get(ptIndex+1);
		if (curr == null)
			return null;
		double bestAlpha = Misc.getUndefDouble();
		double bestDist = Misc.getUndefDouble();
		boolean bestOnRoute = false;
		int bestIndex = Misc.getUndefInt();
		if (prior != null) {
			Pair<Double, Double> intersectPt = checkWhereInSegment(lon, lat, prior.first, prior.second, curr.first, curr.second);
			double alpha = intersectPt.first;
			double dist = intersectPt.second;
			int cl = RouteDef.classifyAlpha(alpha);
			if (cl < 0)
				dist = Point.fastGeoDistance(prior.first, prior.second, lon, lat);
			else if (cl > 0)
				dist = Point.fastGeoDistance(curr.first, curr.second, lon, lat);
			boolean onRoute = (dist <= threshDist || Misc.isEqual(dist, threshDist));
			bestAlpha = alpha;
			bestDist = dist;
			bestIndex = alpha > 1 && !Misc.isEqual(alpha, 1) ? ptIndex : ptIndex - 1;
			bestOnRoute = onRoute;
		}
		if (next != null ) {
			Pair<Double, Double> intersectPt = checkWhereInSegment(lon, lat, curr.first, curr.second, next.first, next.second);
			double alpha = intersectPt.first;
			double dist = intersectPt.second;
			int cl = RouteDef.classifyAlpha(alpha);
			if (cl < 0)
				dist = Point.fastGeoDistance(curr.first, curr.second, lon, lat);
			else if (cl > 0)
				dist = Point.fastGeoDistance(next.first, next.second, lon, lat);

			boolean onRoute = (dist <= threshDist || Misc.isEqual(dist, threshDist));
			if (bestIndex < 0 || toReplaceMeAlphaDist(bestAlpha, bestDist, bestOnRoute, alpha, dist, onRoute)) {
				bestAlpha = alpha;
				bestDist = dist;
				bestIndex = alpha > 1 && !Misc.isEqual(alpha, 1) ? ptIndex+1 : ptIndex;
				bestOnRoute = onRoute;
			}
		}
		if (prior == null && next == null) {
			double dist = Point.fastGeoDistance(curr.first, curr.second, lon, lat);
			boolean onRoute =  (dist <= threshDist || Misc.isEqual(dist, threshDist));
			bestAlpha = 0;
			bestDist = dist;
			bestIndex = ptIndex;
			bestOnRoute = onRoute;
		}
			
		MiscInner.PairFloat pt = rd.points.get(bestIndex);
		return new NearestInfo(routeIndex, (short) bestIndex, rd.getId(), pt.first, pt.second, rd.getPointPairAddnlInfo(bestIndex), bestAlpha, bestDist, bestOnRoute);
	}

	
	private  static void preProcessRoutes() {
		try {
			Properties properties = new Properties();
			properties.put("MinNodeEntries", 5);
			properties.put("MaxNodeEntries", 10);
			g_routePointInfo = new HashMap<Integer, Pair<Double, RTree>>();
			g_pointInfoLookup = new ArrayList<PointInfo>();
			int currId = 0;
			for (int i=0,is = g_routeDefList == null ? 0 : g_routeDefList.size(); i<is; i++) {
				RouteDef rd = g_routeDefList.get(i);
				int portNodeId = rd.getPortNodeId();
				Pair<Double, RTree> maxDegDistRtree = g_routePointInfo.get(portNodeId);
				RTree rtree = null;
				if (maxDegDistRtree == null) {
					rtree = new RTree();
					rtree.init(properties);
					maxDegDistRtree = new Pair<Double, RTree>(-1.0, rtree);
					g_routePointInfo.put(portNodeId, maxDegDistRtree);
				}
				rtree = maxDegDistRtree.second;
				double degGap = rd.maxDeltaLonLatBetweenPoints+RTreeUtils.helpConvertKMInDegree(rd.getMarginKm());
				if (degGap > maxDegDistRtree.first)
					maxDegDistRtree.first =degGap;
				ArrayList<MiscInner.PairFloat> ptList = rd.points;
				for (int j=0,js=ptList == null ? 0 : ptList.size();j<js;j++) {//ONLY start point in g_pointInfoLookup ... end will have id of odd
					MiscInner.PairFloat pt = ptList.get(j);
					float x = pt.first;
					float y = pt.second;
					Rectangle rectangle = new Rectangle();
					rectangle.set(x,y,x,y);
					PointInfo info = new PointInfo((short)i, (short) j);
					g_pointInfoLookup.add(info);
					synchronized (rtree) {
						rtree.add(rectangle, currId);
					}
					currId++;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static RouteDef getRouteDefByIndex(int index) {
		return RouteDef.g_routeDefList.get(index);
	}

	

	private static void load(Connection conn, boolean force)  {
		try {
			getReadLock();
			
			if (!force && g_routeDefs != null) {
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseReadLock();
		}
		try {
			getWriteLock();
			g_routeDefs = new HashMap<Integer, RouteDef>();
			g_routeDefList = new ArrayList<RouteDef>();

			RouteDef.loadLafargeSegment(conn);
			RouteDef.loadRegularRoad(conn);
			preProcessRoutes();
			VehicleInfo.clearLastPointInfo();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseWriteLock();
		}
	}
	
	private static void loadLafargeSegment(Connection conn)  {
		try {
			final String query = "select rm.id, rm.do_buzz, rm.margin, rms.start_lon, rms.start_lat, rms.stop_lon, rms.stop_lat, rms.speed_limit, rm.port_node_id from route_monitors rm join route_monitor_segments rms on (rm.id = rms.route_monitor_id) "+
			" order by rm.id, seq_no";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			RouteDef prev = null;
			float currMax = -1;
			while (rs.next()) {
				int id = rs.getInt(1);
				if (prev == null || prev.getId() != id)
					prev = null;
				if (prev == null) {
					prev = new RouteDef(rs.getInt(1), rs.getInt(9), 1 == rs.getInt(2), rs.getFloat(3));
					g_routeDefs.put(prev.getId(), prev);
					g_routeDefList.add(prev);
					prev.setLafargeLike(true);
					currMax = -1;
				}
				float x = rs.getFloat(4);
				float y = rs.getFloat(5);
				prev.points.add(new MiscInner.PairFloat(x, y));
				float x2 = rs.getFloat(6);
				float y2 = rs.getFloat(7);
				if (Math.abs(x2-x) > currMax)
					currMax = Math.abs(x2-x);
				if (Math.abs(y2-y) > currMax)
					currMax = Math.abs(y2-y);
				prev.maxDeltaLonLatBetweenPoints = currMax;
				prev.points.add(new MiscInner.PairFloat(x2, y2));
				prev.addnlInfo.add(new PointPairAddnlInfo(rs.getFloat(8)));
			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
					//eat it
		}
	}
	public static void loadRegularRoad(Connection conn) {
		try {
			final String q = "select id, new_road_segments.name, port_node_id, new_road_segment_id, seq, lon, lat, margin from new_road_segments " +
					"join new_road_segment_points on (id = new_road_segment_id) order by new_road_segment_id, seq";
			PreparedStatement ps = conn.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			RouteDef prev = null;
			float currMax = -1;
			float prevLon = -1;
			float prevLat = -1;
			boolean ignore = false;
			while (rs.next()) {
				int id = rs.getInt("new_road_segment_id");
				if (prev == null || prev.getId() != id)
					prev = null;
				if (prev == null) {
//					/RouteDef(int id, int portNodeId, boolean doBuzz, float marginKm)
					prev = new RouteDef(id, rs.getInt("port_node_id"), false, 0.08f);
					g_routeDefs.put(prev.getId(), prev);
					g_routeDefList.add(prev);
					prev.setLafargeLike(false);
					currMax = -1;
					prevLon = -1;
					prevLat = -1;
					ignore = true;
					prev.setMarginKm((float)Misc.getRsetFloat(rs, "margin", 0.08f));
				}
				float x = rs.getFloat("lon");
				float y = rs.getFloat("lat");
				if (!ignore) {
					if (Math.abs(x-prevLon) > currMax)
						currMax = Math.abs(x-prevLon);
					if (Math.abs(y-prevLat) > currMax)
						currMax = Math.abs(y-prevLat);
					prev.maxDeltaLonLatBetweenPoints = currMax;
				}
				prevLon = x;
				prevLat = y;
				ignore = false;
				prev.points.add(new MiscInner.PairFloat(x, y));
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public RouteDef(int id, int portNodeId, boolean doBuzz, float marginKm) {
		this.id = id;
		this.portNodeId = portNodeId;
		this.doBuzz = doBuzz;
		this.marginKm = marginKm;
	}
	public static class PointPairAddnlInfo {
		private float speedLimit;
		public float getSpeedLimit() {
			return speedLimit;
		}
		public void setSpeedLimit(float speedLimit) {
			this.speedLimit = speedLimit;
		}
		public PointPairAddnlInfo(float speedLimit) {
			this.speedLimit = speedLimit;
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isDoBuzz() {
		return doBuzz;
	}
	public void setDoBuzz(boolean doBuzz) {
		this.doBuzz = doBuzz;
	}
	public float getMarginKm() {
		return marginKm;
	}
	public void setMarginKm(float marginKm) {
		this.marginKm = marginKm;
	}
	public ArrayList<MiscInner.PairFloat> getPoints() {
		return this.points;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public  static Pair<Double, Double> checkWhereInSegment(double lon, double lat, double startLon, double startLat, double endLon, double endLat) {
		//vector maths - supose u is vector from start to end (e-u)
		//intersection pt: s+Alpha*u
		//(p-s-Alpha*u).(u) = 0 => p.u - s.u -alpha (u.u) => alpha = (p-s).u/u.u =(p-s).(e-s)/mod
		//p.e + s.s - p.s-s.e/uu
		//uu = (e-s).(e-s) = ee+ss-2(e.s)
		//intersect pt = s+alpha*(e-s) = alpha * e + (1-alpha)*s
		double pe = lon*endLon +lat*endLat;
		double ss = startLon*startLon+startLat*startLat;
		double ps = lon*startLon + lat*startLat;
		double se = startLon*endLon + startLat*endLat;
		double ee = endLon*endLon+endLat*endLat;
		double uu = ee+ss-2*se;
		double alpha = (pe+ss-ps-se)/uu;
		boolean iszero = Misc.isEqual(alpha, 0);
		double ptx =  alpha*endLon + (1-alpha)*startLon;
		double pty = alpha*endLat + (1-alpha)*startLat;
		double delx = lon - ptx;
		double dely = lat - pty;
		double sqr = delx*delx+dely*dely;
		double dist = Math.sqrt(sqr);
		dist = RTreeUtils.helpConvertDegreeInKM(dist);
		return new Pair<Double, Double>(alpha, dist);
	}

	public boolean isLafargeLike() {
		return lafargeLike;
	}
	public void setLafargeLike(boolean lafargeLike) {
		this.lafargeLike = lafargeLike;
	}
	public static void getReadLock() {
		lock.readLock().lock();
	}
	public static void releaseReadLock() {
		lock.readLock().unlock();
	}
	public static void getWriteLock() {
		lock.writeLock().lock();
	}
	public static void releaseWriteLock() {
		lock.writeLock().unlock();
	}
	
	public static void main(String[] args) {
		try {
			//double seg1Lon = 77.3177647590637;
			//double seg1Lat = 28.5947760840989;
			//double seg2Lon = 77.3190039396286;
			//double seg2Lat = 28.5955108574132;
			//77.32009,28.592455 //dhaba
			//77.31884,28.59074 //paytm
			double dhabaLon = 77.32078;//dhaba
			double dhabaLat = 28.592869;
			double paytmLon = 77.31884;//paytm
			double paytmLat = 28.59074;
			double tpointLon = 77.32009;//tpoint
			double tpointLat = 28.592455;
			
			double ptList[][] = {
{77.319115,28.593129999999995,0}
,{77.3191964,28.5932274,0}
,{77.31911166666667,28.593116666666667,0}
,{77.31910666666666,28.59310833333333,0.0}
,{77.31911166666667,28.593101666666666,0.0}
,{77.319115,28.593093333333332,0.0}
,{77.319115,28.593095,0.0}
,{77.3191967,28.593228,0.0}
,{77.319115,28.593095,0.0}
,{77.3192081,28.5932235,0.0}
,{77.319115,28.593095,0.0}
,{77.3192064,28.5932148,0.0}
,{77.319115,28.593095,0.0}
,{77.3191887,28.5932295,0.0}
,{77.319115,28.593095,0.0}
,{77.3191957,28.5932181,0.0}
,{77.319115,28.593095,0.0}
,{77.3192428,28.5932244,0.0}
,{77.319115,28.593095,0.0}
,{77.3192011,28.5932124,0.0}
,{77.319115,28.593095,0.0}
,{77.3192072,28.5932469,0.0}
,{77.31914,28.593185000000002,0.0}
,{77.31914,28.593186666666664,0.0}
,{77.3191966,28.5932607,0.0}
,{77.31914,28.593186666666664,0.0}
,{77.3191961,28.5932617,0.0}
,{77.31914,28.593186666666664,0.0}
,{77.3191962,28.5932513,0.0}
,{77.31914,28.593186666666664,0.0}
,{77.3192016,28.5932401,0.0}
,{77.31914,28.593186666666664,0.0}
,{77.3192014,28.5932472,0.0}
,{77.31914,28.593186666666664,0.0}
,{77.31911166666667,28.593206666666667,0.0}
,{77.31912666666668,28.59321333333333,0.0}
,{77.319175,28.593249999999998,0.0}
,{77.31918166666667,28.59325833333333,0.0}
,{77.31920333333333,28.593278333333334,0.0}
,{77.31922333333334,28.593275000000002,0.0}
,{77.31923,28.593281666666666,0}
,{77.3192044,28.593226,0.0}
,{77.31923,28.593281666666666,0.0}
,{77.3192264,28.5932299,0.0}
,{77.31926,28.593279999999996,0.0}
,{77.31926333333334,28.593320000000002,0.51}
,{77.31929166666667,28.593326666666666,0.89}
,{77.31930666666668,28.59331666666667,0.0}
,{77.3192864,28.5932522,0.0}
,{77.31932666666667,28.593328333333336,0.88}
,{77.31932,28.593338333333335,0.557}
,{77.31933000000001,28.593339999999998,0.0}
,{77.31933000000001,28.593339999999998,0.0}
,{77.31936166666667,28.59336,1.14}
,{77.31937166666667,28.59340666666667,2.005}
,{77.3192541,28.593263,0.0}
,{77.31934,28.593461666666666,0.0}
,{77.31934500000001,28.593455000000002,1.13}
,{77.31934,28.593461666666666,0.0}
,{77.31934,28.593461666666666,0.0}
,{77.31930000000001,28.593471666666666,1.36}
,{77.31927,28.593489999999996,1.4}
,{77.3192443,28.5932689,0}
,{77.31927666666667,28.593545000000002,1.7}
,{77.31927333333333,28.593543333333333,0}
,{77.31927333333333,28.593543333333333,0}
,{77.31932666666667,28.593495,4.04}
,{77.31942666666667,28.593369999999997,6.427}
,{77.31954999999999,28.593196666666667,7.47}
,{77.3192502,28.5932923,0}
,{77.31967,28.593014999999998,7.0}			
,{77.31977499999999,28.592865,5.91}
,{77.31991500000001,28.592693333333333,6.699}
,{77.32000500000001,28.592593333333337,3.74}
,{77.32003333333334,28.592515000000002,3.0770}
,{77.31997333333334,28.59241833333333,4.850}
,{77.31984833333333,28.592325,5.562}
,{77.3197206,28.5924769,0}
,{77.31970166666666,28.592213333333333,6.76499}
,{77.31955666666666,28.59210666666667,5.6856}
,{77.31937500000001,28.59198666666667,8.24739}
,{77.31916166666666,28.591878333333337,7.45575}
,{77.31937500000001,28.59198666666667,8.247}
,{77.31896333333334,28.591741666666667,8.0888}
,{77.318765,28.591616666666663,7.213317}
,{77.3197577,28.5922981,0} //network
,{77.31861333333333,28.591493333333332,5.07876}
,{77.31855833333334,28.591396666666665,3.98}
,{77.318635,28.591278333333335,6.51}
,{77.31875666666666,28.591113333333336,7.06}
,{77.31889,28.59099833333333,5.475}
,{77.31898666666666,28.590895,3.855}
,{77.31905333333334,28.590856666666664,2.41}
,{77.31914,28.590891666666668,4.09}
,{77.3191528,28.5912804,0}
,{77.319295,28.59099833333333,7.38}
,{77.31948000000001,28.59112,7.72}
,{77.31968499999999,28.591245000000004,8.08}
,{77.31989833333334,28.591361666666664,8.242}
,{77.320105,28.591496666666664,8.46}
			};

			double ptList2[][] = {
					{77.3192,28.5933},
					{77.3195,28.5933},
					{77.3211,28.593},
					{77.3203,28.5952},
					{77.3178,28.5939},
					{77.3176,28.5927},
					{77.3186,28.5924},
					{77.3188,28.5938},
					{77.3193,28.5934},
					{77.3192,28.5933},
					{77.3192,28.5933}
			}
			;
			double[] prevPt = null;
			for (int i=0,is=ptList.length;i<is;i++) {
				double[] currPt = ptList[i];
				if (prevPt != null) {
					if (Point.fastGeoDistance(prevPt[0], prevPt[1], currPt[0], currPt[1]) > 0.050) {
						Pair<Double, Double> wrtDhaba = checkWhereInSegment(dhabaLon, dhabaLat, prevPt[0], prevPt[1], currPt[0], currPt[1]);
						Pair<Double, Double> wrtPaytm = checkWhereInSegment(paytmLon, paytmLat, prevPt[0], prevPt[1], currPt[0], currPt[1]);
						Pair<Double, Double> wrtTpoint = checkWhereInSegment(tpointLon, tpointLat, prevPt[0], prevPt[1], currPt[0], currPt[1]);
						double dpDhaba = Point.fastGeoDistance(currPt[0], currPt[1], dhabaLon, dhabaLat);
						boolean toPlayDhaba = currPt[2]*3.6 < 10 ? dpDhaba <= 0.05 : dpDhaba/(currPt[2]*3.6)*3600 <= 30;
						double dpPaytm = Point.fastGeoDistance(currPt[0], currPt[1], paytmLon, paytmLat);
						boolean toPlayPaytm = currPt[2]*3.6 < 10 ? dpDhaba <= 0.05 : dpDhaba/(currPt[2]*3.6)*3600 <= 30;
						double dpTpoint = Point.fastGeoDistance(currPt[0], currPt[1], tpointLon, tpointLat);
						boolean toPlayTpoint = currPt[2]*3.6 < 10 ? dpDhaba <= 0.05 : dpDhaba/(currPt[2]*3.6)*3600 <= 30;
						
						System.out.println("wrtDhaba:i:"+i+" pt:"+currPt[0]+","+currPt[1]+" prev:"+prevPt[0]+","+prevPt[1]+" res:"+wrtDhaba+" dist:"+dpDhaba+" speed:"+(currPt[2]*3.6)+" toPlay:"+toPlayDhaba+" PerpTrue:"+(wrtDhaba.second < 0.035));
						System.out.println("wrtTpoint:i:"+i+" pt:"+currPt[0]+","+currPt[1]+" prev:"+prevPt[0]+","+prevPt[1]+" res:"+wrtTpoint+" dist:"+dpTpoint+" speed:"+(currPt[2]*3.6)+" toPlay:"+toPlayTpoint+" PerpTrue:"+(wrtTpoint.second < 0.035));
						System.out.println("wrtPaytm:i:"+i+" pt:"+currPt[0]+","+currPt[1]+" prev:"+prevPt[0]+","+prevPt[1]+" res:"+wrtPaytm+" dist:"+dpPaytm+" speed:"+(currPt[2]*3.6)+" toPlay:"+toPlayPaytm+" PerpTrue:"+(wrtPaytm.second < 0.035));
						prevPt = currPt;
					}
					else {
						System.out.println("Dist i lesser:"+Point.fastGeoDistance(prevPt[0], prevPt[1], currPt[0], currPt[1]));
					}
				}
				else {
					prevPt = currPt;
				}
			}
		}			
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		
		}
	}
}

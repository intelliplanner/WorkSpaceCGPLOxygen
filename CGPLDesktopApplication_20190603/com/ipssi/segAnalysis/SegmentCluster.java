package com.ipssi.segAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.routemonitor.*;

public class SegmentCluster {
	//approach
	//until no changes pick one from currentList, find nearest, if nearest within thresh then, remove nearest from list and merge with current
	public static String escapeJson(String str) {
		  str = str.replaceAll("\"","\\\\\"");
		  str = str.replaceAll("\r\n", ",");
		  str = str.replaceAll("\r", ",");
		  str = str.replaceAll("\n", ",");
		  return str;
	  }	
	static RTree rtreeForCluster = null;
	static Map<Integer, Integer> dbgRTreeIds = new HashMap<Integer, Integer>();
	static {
		Properties properties = new Properties();
		properties.put("MinNodeEntries", 5);
		properties.put("MaxNodeEntries", 10);
		RTree rtree = new RTree();
		rtree.init(properties);
		rtreeForCluster = rtree;
	}
	static Map<Integer, SegmentCluster> g_clusterIdToCluster = new HashMap<Integer, SegmentCluster>();
	ArrayList<Segment> segList = new ArrayList<Segment>();
	
	MiscInner.PairDouble minBound = new MiscInner.PairDouble(Double.MAX_VALUE, Double.MAX_VALUE);
	MiscInner.PairDouble maxBound = new MiscInner.PairDouble(Double.MIN_VALUE, Double.MIN_VALUE);
	public int repId = Integer.MAX_VALUE;
	double centerX = 0;
	double centerY = 0;
	boolean wasDirty = true;
	public ArrayList<Triple<Double, Double, String>> helperPointInCluster = null;	
	public Map<String, String> helperVehicleDay = null; //key = vehicle_day, to check if already found for a particular cluster
	public String toString() {
		return "("+centerX+","+centerY+"),"+minBound+","+maxBound+" Segs:"+segList;
	}
	public static EstimateVehiclePopInSegment hackCreatePointLookupHelper(Connection conn) {
		loadClusterResult(conn);
		loadClusterPointResults(conn, Misc.getUndefInt());

		System.out.println("[PTLOOKUP] ... estimating population of vehicleDay");
		Collection<SegmentCluster> clusterList = g_clusterIdToCluster.values();
		int cnt = 0;
		EstimateVehiclePopInSegment popEstimator = new EstimateVehiclePopInSegment();
		for (SegmentCluster sc:clusterList) {
			System.out.println("[PTLOOKUP] estimating population prepping .."+sc.repId +" sz:"+sc.segList.size());
			popEstimator.prepAddSegment(sc);
		}
		return popEstimator;
	}
	public static void controller(Connection conn, boolean doClustering, boolean doSegPrep, boolean doVehPopEstimator, boolean doOtherDBRec, int ruleId, boolean partialMode, double segmenterThresh) {
		//ruleId -> usually undef basicaly 574 on stag, 634 on ipssi
		//partialMode -> while updatingCounts we add instead of set ... generally shld be true
		if (doClustering) {
			handleClusterCall(conn);
		}
		if (doSegPrep) {
			if (!doClustering) {
				loadClusterResult(conn);
			}
			doRegressionAll(conn, segmenterThresh);
		}
		if (doVehPopEstimator) {
			if (!doClustering && !doSegPrep) {
				loadClusterResult(conn);
			}
			if (!doSegPrep)
				loadClusterPointResults(conn, Misc.getUndefInt());
			System.out.println("[CLUSTER] ... estimating population of vehicleDay");
			Collection<SegmentCluster> clusterList = g_clusterIdToCluster.values();
			int cnt = 0;
			EstimateVehiclePopInSegment popEstimator = new EstimateVehiclePopInSegment();
			for (SegmentCluster sc:clusterList) {
				System.out.println("[CLUSTER] estimating population prepping .."+sc.repId +" sz:"+sc.segList.size());
				popEstimator.prepAddSegment(sc);
			}
			System.out.println("[CLUSTER] estimating population doing");
			String srvrName = Misc.getServerName();
			 if (Misc.isUndef(ruleId)) {
				 ruleId = 634;
				 if ("node_stag".equals(srvrName))
					 ruleId = 574;
			 }
			popEstimator.doEstimationAfterSegmentPrep(conn, ruleId);
			
			System.out.println("[CLUSTER] estimating population saving");
			for (SegmentCluster sc:clusterList) {
				System.out.println("[CLUSTER] saving .."+sc.repId +" sz:"+sc.segList.size()+" vehDays:"+sc.helperVehicleDay.size());
				popEstimator.save(conn, sc, partialMode);
			}

		}
		if (doOtherDBRec) {
			//populate numEvents and num_event_vehicle_days
			PreparedStatement ps = null;
			try {
				if (!partialMode)
					ps = conn.prepareStatement("update ee_slow_cluster_segments join (select count(distinct vehicle_id, date(event_start_time)) nvdc, count(*) nec, crit_eval_id from engine_events_slow_ee where crit_eval_id > 0 and rule_id=? group by crit_eval_id) cnt on (ee_slow_cluster_segments.repid = cnt.crit_eval_id) set num_event_vehicle_days = cnt.nvdc, num_events = cnt.nec");
				else
					ps = conn.prepareStatement("update ee_slow_cluster_segments join (select count(distinct vehicle_id, date(event_start_time)) nvdc, count(*) nec, crit_eval_id from engine_events_slow_ee where crit_eval_id > 0 and rule_id=? group by crit_eval_id) cnt on (ee_slow_cluster_segments.repid = cnt.crit_eval_id) set num_event_vehicle_days = (case when num_event_vehicle_days is null then 0 else num_event_vehicle_days end) + cnt.nvdc, num_events = (case when num_events is null then 0 else num_events end)  + cnt.nec");
				ps.setInt(1, ruleId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				ps = Misc.closePS(ps);
			}
		}
	}
	public static void handleClusterCall() {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			handleClusterCall(conn);
		}
		catch (Exception e) {
			e.printStackTrace();			
		}
		finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
					conn = null;
				}
				catch (Exception e) {
					
				}
			}
		}
	}
	public static void handleClusterCall(Connection conn) {
		try {
			double boxThreshKM = 32;
			double mergeThreshKM = 1.5;
			double boxThreshDegree = RTreeUtils.helpConvertKMInDegree(boxThreshKM);
			double mergeThreshDegree = RTreeUtils.helpConvertKMInDegree(mergeThreshKM);
			System.out.println("[CLUSTER] Loading ..");
			SegmentCluster.load(conn, boxThreshKM*0.75);
			System.out.println("[CLUSTER] Clustering ..");
			//doClustering(boxThreshDegree, mergeThreshDegree, 5000);//SegmentCluster.g_clusterIdToCluster.size()*100);
			doClustering(boxThreshDegree, mergeThreshDegree, SegmentCluster.g_clusterIdToCluster.size()*100);
			System.out.println("[CLUSTER] Saving ..");
			SegmentCluster.save(conn);
			System.out.println("[CLUSTER] # clusers .."+SegmentCluster.g_clusterIdToCluster.size());
			System.out.println("[CLUSTER] Now making segment ..");
			doRegressionAll(conn, 6);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		}
	}
	public static void doClustering(double boxThresh, double mergeThresh, int maxRound) {
		boolean seenChange = false;
		if (g_clusterIdToCluster.isEmpty())
			return;
		int round = 0;
		System.out.println("[CLUSTER] Box:"+boxThresh+ " merge:"+mergeThresh+" MaxRnd:"+maxRound);
		
		do {
			round++;
			boolean printRound = round % 1000 == 0;
			if (printRound)
				System.out.println("[CLUSTER] Round:"+round+ " sz:"+SegmentCluster.g_clusterIdToCluster.size());
			//sanityCheck();
			seenChange = false;
			Set<Map.Entry<Integer, SegmentCluster>> entries =  g_clusterIdToCluster.entrySet();
			Iterator<Map.Entry<Integer, SegmentCluster>> iter = entries.iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, SegmentCluster> entry = iter.next();
				SegmentCluster cluster = entry.getValue();
				if (!cluster.wasDirty)
					continue;
				cluster.wasDirty = false;
				for (Segment sdbg:cluster.segList) {
					if (sdbg.id == 65756233) {
						int dbg = 1;
						dbg++;
					}
				}
				//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId);
				//look in RTree within boxThresh ... and get the cluster that is nearest ... if that is within mergeThresh, merge
				//set seenChange and continue;
				List<Integer> arrayOfIds=new ArrayList<Integer>();
				PrintId pid=new PrintId(); 
				RTree rtree=rtreeForCluster;
				//TODO DEBUG13 compile error rtree.nearestN((new com.infomatiq.jsi.Point((float)cluster.centerX, (float)cluster.centerY)), pid, 300, (float)boxThresh);
				arrayOfIds=pid.getArrayOfIds();
				ArrayList<SegmentCluster> mergeable = new ArrayList<SegmentCluster>();
				for(int i=0;i<arrayOfIds.size();i++){
					int otherId = arrayOfIds.get(i);
					SegmentCluster otherCluster = SegmentCluster.getClusterFor(otherId);
					if (otherCluster.repId == cluster.repId)
						continue;
					for (Segment sdbg:otherCluster.segList) {
						if (sdbg.id == 65756233) {
							int dbg = 1;
							dbg++;
							double ddbg = cluster.getDistance(otherCluster);
							if (ddbg < mergeThresh) {
								ddbg = cluster.getDistance(otherCluster);
							}
						}
					}
					//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId+ " Checking:"+otherCluster.repId);
					double d = cluster.getDistance(otherCluster);
					//System.out.println("[CLUSTER] Round:"+round +" Merge For:"+cluster.repId+ " Checking:"+otherCluster.repId+ " dist,mergeThresh:"+d+","+mergeThresh);
					if (d < mergeThresh) {
						mergeable.add(otherCluster);
					}
				}
				pid.cleanArrayOfIds();
				for (SegmentCluster mc:mergeable) {
					seenChange = true;
					System.out.println("[CLUSTER] Round:"+round + " Merging For:"+cluster.repId+ " merged:"+mc.repId+ " TotSz:"+SegmentCluster.g_clusterIdToCluster.size()+", MeSz:"+cluster.segList.size()+" BestSz:"+mc.segList.size());
					cluster.mergeCluster(mc);
				}
				
				if (seenChange)
					break;
			}
		}
		while (seenChange && round < maxRound);
	}
	public static void doClusteringStrict(double boxThresh, double mergeThresh, int maxRound) {
		boolean seenChange = false;
		if (g_clusterIdToCluster.isEmpty())
			return;
		int round = 0;
		System.out.println("[CLUSTER] Box:"+boxThresh+ " merge:"+mergeThresh+" MaxRnd:"+maxRound);
		
		do {
			round++;
			boolean printRound = round % 1000 == 0;
			if (printRound)
				System.out.println("[CLUSTER] Round:"+round+ " sz:"+SegmentCluster.g_clusterIdToCluster.size());
			//sanityCheck();
			seenChange = false;
			Set<Map.Entry<Integer, SegmentCluster>> entries =  g_clusterIdToCluster.entrySet();
			Iterator<Map.Entry<Integer, SegmentCluster>> iter = entries.iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, SegmentCluster> entry = iter.next();
				SegmentCluster cluster = entry.getValue();
				for (Segment sdbg:cluster.segList) {
					if (sdbg.id == 65756233) {
						int dbg = 1;
						dbg++;
					}
				}
				//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId);
				//look in RTree within boxThresh ... and get the cluster that is nearest ... if that is within mergeThresh, merge
				//set seenChange and continue;
				List<Integer> arrayOfIds=new ArrayList<Integer>();
				PrintId pid=new PrintId(); 
				RTree rtree=rtreeForCluster;
				//TODO DEBUG13 compile error rtree.nearestN((new com.infomatiq.jsi.Point((float)cluster.centerX, (float)cluster.centerY)), pid, 300, (float)boxThresh);
				arrayOfIds=pid.getArrayOfIds();
				double miDist = Double.MAX_VALUE;
				SegmentCluster bestCluster = null;
				for(int i=0;i<arrayOfIds.size();i++){
					int otherId = arrayOfIds.get(i);
					SegmentCluster otherCluster = SegmentCluster.getClusterFor(otherId);
					if (otherCluster.repId == cluster.repId)
						continue;
					for (Segment sdbg:otherCluster.segList) {
						if (sdbg.id == 65756233) {
							int dbg = 1;
							dbg++;
							double ddbg = cluster.getDistance(otherCluster);
							if (ddbg < mergeThresh) {
								ddbg = cluster.getDistance(otherCluster);
							}
						}
					}
					//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId+ " Checking:"+otherCluster.repId);
					double d = cluster.getDistance(otherCluster);
					//System.out.println("[CLUSTER] Round:"+round +" Merge For:"+cluster.repId+ " Checking:"+otherCluster.repId+ " dist,mergeThresh:"+d+","+mergeThresh);
					if (d < miDist) {
						miDist = d;
						bestCluster = otherCluster;
					}
				}
				pid.cleanArrayOfIds();
				if (bestCluster != null && miDist < mergeThresh) {
					seenChange = true;
					System.out.println("[CLUSTER] Round:"+round + " Merging For:"+cluster.repId+ " merged:"+bestCluster.repId+ " TotSz:"+SegmentCluster.g_clusterIdToCluster.size()+", MeSz:"+cluster.segList.size()+" BestSz:"+bestCluster.segList.size());
					if (cluster.segList.size() > 2 && bestCluster.segList.size() > 2) {
						int dbg = 1;
						
						double d1 = cluster.getDistance(bestCluster);
						dbg++;
					}
					cluster.mergeCluster(bestCluster);
					
				}
				if (seenChange)
					break;
			}
		}
		while (seenChange && round < maxRound);
	}
	public static void doClusteringV1(double boxThresh, double mergeThresh, int maxRound) {
		boolean seenChange = false;
		if (g_clusterIdToCluster.isEmpty())
			return;
		int round = 0;
		System.out.println("[CLUSTER] Box:"+boxThresh+ " merge:"+mergeThresh+" MaxRnd:"+maxRound);
		int currSz = 1;
		
		do {
			round++;
			boolean printRound = round % 1000 == 0;
			if (printRound)
				System.out.println("[CLUSTER] Round:"+round+ " sz:"+SegmentCluster.g_clusterIdToCluster.size());
			//sanityCheck();
			seenChange = false;
			Set<Map.Entry<Integer, SegmentCluster>> entries =  g_clusterIdToCluster.entrySet();
			Iterator<Map.Entry<Integer, SegmentCluster>> iter = entries.iterator();
			int maxSz = -1;
			while (iter.hasNext()) {
				Map.Entry<Integer, SegmentCluster> entry = iter.next();
				SegmentCluster cluster = entry.getValue();
				int sz = cluster.segList.size();
				if (sz > maxSz)
					maxSz = sz;
				if (sz > currSz)
					continue;
				//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId);
				//look in RTree within boxThresh ... and get the cluster that is nearest ... if that is within mergeThresh, merge
				//set seenChange and continue;
				List<Integer> arrayOfIds=new ArrayList<Integer>();
				PrintId pid=new PrintId(); 
				RTree rtree=rtreeForCluster;
				//TODO DEBUG13 compile error rtree.nearestN((new com.infomatiq.jsi.Point((float)cluster.centerX, (float)cluster.centerY)), pid, 300, (float)boxThresh);
				arrayOfIds=pid.getArrayOfIds();
				ArrayList<SegmentCluster> bl = new ArrayList<SegmentCluster>();
				for(int i=0;i<arrayOfIds.size();i++){
					int otherId = arrayOfIds.get(i);
					SegmentCluster otherCluster = SegmentCluster.getClusterFor(otherId);
					if (otherCluster.repId == cluster.repId || otherCluster.segList.size() > currSz)
						continue;
					//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId+ " Checking:"+otherCluster.repId);
					double d = cluster.getDistance(otherCluster);
					if (d < mergeThresh) {
						if (cluster.segList.size() > 1 && otherCluster.segList.size() > 1) {
							double d1 = cluster.getDistance(otherCluster);
							int dbg = 1;
						}
						bl.add(otherCluster);
						System.out.println("[CLUSTER] Round:"+round + " CurrSz:"+currSz+" Merge For:"+cluster.repId+ " Checking:"+otherCluster.repId+ " dist,mergeThresh:"+d+","+mergeThresh+" mesz:"+cluster.segList.size()+" osz:"+otherCluster.segList.size());
					}
					
				}
				pid.cleanArrayOfIds();
				for (SegmentCluster ob:bl) {
					seenChange = true;
					cluster.mergeCluster(ob);
					//System.out.println("[CLUSTER] Round:"+round + " For:"+cluster.repId+ " merged:"+best.repId+ " sz:"+SegmentCluster.g_clusterIdToCluster.size());
				}
				if (seenChange)
					break;
			}
			if (!seenChange) {
				currSz++;
				if (currSz <= maxSz) 
					seenChange = true;
			}
		}
		while (seenChange && round < maxRound);
	}
	public static void doRegressionAll(Connection conn, double segmenterThresh) {
		Collection<SegmentCluster> clusterList = g_clusterIdToCluster.values();
		int cnt = 0;
		for (SegmentCluster sc:clusterList) {
			System.out.println("[CLUSTER] Segmenting .."+sc.repId +" sz:"+sc.segList.size());
			Pair<Boolean, ArrayList<Triple<Double, Double, String>>> results = ClusterLineSegmenter.doRegression(sc, segmenterThresh);
			sc.helperPointInCluster = results.second;
			ClusterLineSegmenter.save(conn, sc.repId, results.first, results.second);
		}	
	}
	public static void loadClusterResult(Connection conn) {
		loadClusterResult(conn, Misc.getUndefInt());
	}
	public static void loadClusterResult(Connection conn, int forClusterId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String q = "select crit_eval_id,id, event_begin_longitude, event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, event_begin_name, event_end_name from engine_events_slow_ee where crit_eval_id > 0 "+(forClusterId > 0 ? " and crit_eval_id="+forClusterId : "")+" order by crit_eval_id, end_cumm_dist-start_cumm_dist";
			
			ps = conn.prepareStatement(q);
			SegmentCluster prevCluster = null;
			rs = ps.executeQuery();
			while (rs.next()) {
				int clusterId = rs.getInt(1);
				Segment seg = new Segment(rs.getInt(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), Misc.sqlToLong(rs.getTimestamp(7)), Misc.sqlToLong(rs.getTimestamp(8)), rs.getString(9), rs.getString(10));
				if (prevCluster != null && prevCluster.repId != clusterId)
					prevCluster = null;
				if (prevCluster == null) {
					prevCluster = new SegmentCluster();
					prevCluster.repId = clusterId;
					SegmentCluster.g_clusterIdToCluster.put(clusterId, prevCluster);
				}
				prevCluster.addSegment(seg);
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
		
	}
	public static void loadClusterPointResults(Connection conn, int forClusterId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String q = "select repid, lon, lat, seq,name from ee_slow_cluster_points "+(!Misc.isUndef(forClusterId) ? " where repid=? " : "")+" order by repid, seq";
			
			ps = conn.prepareStatement(q);
			SegmentCluster prevCluster = null;
			rs = ps.executeQuery();
			while (rs.next()) {
				int clusterId = rs.getInt(1);
				double lon = rs.getDouble(2);
				double lat = rs.getDouble(3);
				if (prevCluster == null || prevCluster.repId != clusterId) {
					prevCluster = SegmentCluster.getClusterFor(clusterId);
					if (prevCluster == null)
						continue;
					if (prevCluster.helperPointInCluster == null)
						prevCluster.helperPointInCluster = new ArrayList<Triple<Double, Double, String>>();
				}
				prevCluster.helperPointInCluster.add(new Triple<Double, Double, String>(lon,lat, rs.getString(4)));
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
		
	}
	public static void load(Connection conn, double thresh) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select id, event_begin_longitude, event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, event_begin_name, event_end_name from engine_events_slow_ee where end_cumm_dist-start_cumm_dist < ? and event_stop_time is not null and event_start_time is not null and event_begin_longitude is not null and event_begin_latitude is not null and event_end_longitude is not null and event_end_latitude is not null ");
			ps.setDouble(1, thresh);
			rs = ps.executeQuery();
			while (rs.next()) {
				Segment seg = new Segment(rs.getInt(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), Misc.sqlToLong(rs.getTimestamp(6)), Misc.sqlToLong(rs.getTimestamp(7)), rs.getString(8), rs.getString(9));
				SegmentCluster cluster = SegmentCluster.initCluster(seg);
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
	}
	public static void save(Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update engine_events_slow_ee set criticality=0, crit_eval_id=0");
			ps.execute();
			if (!conn.getAutoCommit())
				conn.commit();
			ps = Misc.closePS(ps);
			
			ps = conn.prepareStatement("update engine_events_slow_ee set criticality=?, crit_eval_id=? where id= ?");
			Collection<SegmentCluster> clusterList = g_clusterIdToCluster.values();
			int cnt = 0;
			for (SegmentCluster sc:clusterList) {
				for (Segment seg:sc.segList) {
					ps.setInt(1, sc.segList.size());
					ps.setInt(2, sc.repId);
					ps.setInt(3, seg.id);
					ps.addBatch();
					cnt++;
					if (cnt % 1000 == 0) {
						ps.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
					}
				}
			}
			ps.executeBatch();
			if (!conn.getAutoCommit())
				conn.commit();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	private double getDistance(SegmentCluster rhs) {
		double min = Double.MAX_VALUE;
		if (this.repId == rhs.repId) {
			int dbg = 1;
			dbg++;
		}
		for (Segment meSeg: this.segList) {
			for (Segment rhsSeg : rhs.segList) {
				if (meSeg.id == rhsSeg.id) {
					int dbg = 1;
					dbg++;
				}
				if (!meSeg.sameDir(rhsSeg))
					continue;
				double d = meSeg.getMinDistanceBetweenSeg(rhsSeg);
				if (d < 0.001) {
					double d2 = meSeg.getMinDistanceBetweenSeg(rhsSeg);
					int dbg2 = 1;
					dbg2++;
				}
				
				if (d < min) {
					min = d;
				}
			}
		}
		return min;
	}
	private static SegmentCluster getClusterFor(int segId) {
		return g_clusterIdToCluster.get(segId);
	}
	public static SegmentCluster initCluster(Segment seg) {
		SegmentCluster retval = new SegmentCluster();
		retval.addSegment(seg);
		retval.addToRTree();
		g_clusterIdToCluster.put(retval.repId, retval);
		return retval;
	}
	private void mergeCluster(SegmentCluster rhs) {
		//remove me, remove rhs from Rtree
		this.removeFromRTree();
		rhs.removeFromRTree();
		g_clusterIdToCluster.remove(rhs.repId);
		g_clusterIdToCluster.remove(this.repId);
		for (Segment s:rhs.segList)
			this.addSegment(s);
		this.addToRTree();
		g_clusterIdToCluster.put(this.repId, this);
		
		//merge individual
		//then reput in cluster
	}
	private void removeFromRTree() {
		if (this.segList.size() > 0) {
			Rectangle rectangle = new Rectangle();
			 rectangle.set((float)this.minBound.first, (float)this.minBound.second, (float)this.maxBound.first, (float)this.maxBound.second); 
			 rtreeForCluster.delete(rectangle, this.repId);
			 dbgRTreeIds.remove(this.repId);
			 
		}	
	}
	private void addToRTree() {
		if (this.segList.size() > 0) {
			Rectangle rectangle = new Rectangle();
			rectangle.set((float)this.minBound.first, (float)this.minBound.second, (float)this.maxBound.first, (float)this.maxBound.second); 
			rtreeForCluster.add(rectangle, this.repId);
			dbgRTreeIds.put(this.repId, this.repId);
		}	
	}
	private void addSegment(Segment seg) {
		this.wasDirty = true;
		segList.add(seg);
		seg.updateMinMaxBound(minBound, maxBound);
		if (seg.id < repId)
			repId = seg.id;
		double t1 = centerX*(segList.size()-1)*2;
		double t2 = centerY*(segList.size()-1)*2;
		t1 += seg.start.first;
		t2 += seg.start.second;
		t1 += seg.end.first;
		t2 += seg.end.second;
		centerX = t1/(segList.size()*2);
		centerY = t2/(segList.size()*2);
	}
	public static void sanityCheck() {
		Collection<SegmentCluster> values = SegmentCluster.g_clusterIdToCluster.values();
		for (SegmentCluster sc:values) {
			int meId = sc.repId;
			for (Segment s: sc.segList) {
				if (g_clusterIdToCluster.containsKey(s.id) && g_clusterIdToCluster.get(s.id).repId != meId) {
					int dbg = 1;
					dbg++;
				}
				if (SegmentCluster.dbgRTreeIds.containsKey(s.id) && SegmentCluster.dbgRTreeIds.get(s.id) != meId) {
					int dbg = 1;
					dbg++;
				}
			}
		}
	}
	public void removeSegment(Segment seg) {
		boolean found = false;
		for (int i=0,is=segList.size(); i<is;i++) {
			if (segList.get(i).id == seg.id) {
				segList.remove(i);
				found = true;
				break;
			}
		}
		if (found) {
			recalcBoundRepId();
		}
	}
	
	private void recalcBoundRepId() {
		minBound.first = minBound.second = Double.MAX_VALUE;
		maxBound.first=  maxBound.second = Double.MAX_VALUE;
		repId = Integer.MAX_VALUE;
		double t1 = 0;
		double t2 = 0;
		for (Segment seg:segList) {
			seg.updateMinMaxBound(minBound, maxBound);
			if (seg.id < repId)
				repId = seg.id;
			t1 += seg.start.first;
			t2 += seg.start.second;
			t1 += seg.end.first;
			t2 += seg.end.second;
		}
		centerX = t1/(segList.size()*2);
		centerY = t2/(segList.size()*2);
	}
	public static void main(String[] args) {
		//handleClusterCall();
		try {
			Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			boolean doClustering = false;
			boolean doSegPrep = true;
			boolean doVehPopEstimator = false;
			boolean doOtherDBRec = false;
			int ruleId=574;//usually undef
			boolean partialMode = true;
			//65664522
			controller(conn, doClustering, doSegPrep, doVehPopEstimator, doOtherDBRec, ruleId, partialMode, 2);
			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

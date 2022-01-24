package com.ipssi.pointAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import com.infomatiq.jsi.Point;
import com.ipssi.common.ds.trip.ThreadContextCache;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.segAnalysis.ConvexHull;

public class PointCluster {
//1. Load Points
//2. Remove points that indicate stop and go movement
//3.
	//ee_addnl_info
	//ee_pt_clusters
//	create table ee_addnl_info(id int not null, in_op int, is_lu int, cluster_id int, primary key(id));
//	create table ee_pt_clusters(id int not null, name varchar(200), port_node_id int, num_trips int, num_overall_vehicles int, num_points int, num_load_points int, num_vehicles int, shape geometry); 
	public static void doAnalysis(
			boolean calcIsInOp, boolean calcIsLU
			, boolean doIsInOp, boolean ignoreDurCat0, boolean doCluster, boolean doConvexHullOfCluster, boolean doAnalysisOfCluster
			,double distCluster, int minPtsInCluster
			,String eeTableName, ArrayList<Integer> ruleIds, java.sql.Timestamp startTS) throws Exception {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());//to initialize
		if (eeTableName == null)
			eeTableName = "engine_events";
		if (startTS == null)
			startTS = new java.sql.Timestamp(new java.util.Date(90,0,1).getTime());
		if (ruleIds == null || ruleIds.size() == 0) {
			ruleIds = new ArrayList<Integer>();
			ruleIds.add(1);
		}
		if (Misc.isUndef(distCluster))
			distCluster = 0.03;
		if (Misc.isUndef(minPtsInCluster))
			minPtsInCluster = 10;
		
		ArrayList<MiscInner.Pair> vehicleIds = null;
		if (calcIsInOp) {
			System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing InOp");
			if (vehicleIds == null)
				vehicleIds = getVehicleList(conn, eeTableName);
			PointCluster.identifyInsideOpStation(conn, ruleIds, vehicleIds, eeTableName, startTS); 
		}
		if (calcIsLU) {
			System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing IsLU");
			if (vehicleIds == null)
				vehicleIds = getVehicleList(conn, eeTableName);
			PointCluster.identifyLU(conn, ruleIds, vehicleIds, eeTableName, startTS);
		}
		if (doCluster) {
			System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster .. prep for save");
			prepForSave(conn);
			
			double eps = RTreeUtils.helpConvertKMInDegree(distCluster);
			DBSCANClusterer clusterer = new DBSCANClusterer<ConvexHull.PointWithId>(eps, minPtsInCluster);
			System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster ..Loading Data");
			ArrayList<ConvexHull.PointWithId> pts = getPtsToCluster(conn, eeTableName,doIsInOp, ignoreDurCat0);
			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			conn = null;
			System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster ..clustering");
			List<Cluster<ConvexHull.PointWithId>> clusterResult = clusterer.cluster(pts);
			
			int idx = 0;
			for (Cluster<ConvexHull.PointWithId> cluster : clusterResult) {
				List<ConvexHull.PointWithId> clusterPoints = cluster.getPoints();
				List<ConvexHull.PointWithId> convexHull = null;
				try {
					ConvexHull qh = new ConvexHull();
					System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster .. convex hul :"+idx);
					convexHull = qh.quickHull(clusterPoints);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster .. saving :"+idx);
				
				save(eeTableName, idx, clusterPoints, convexHull);//because clustering may take lot of time ..
				if (doAnalysisOfCluster) {
					System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster .. analyzing :"+idx);
					analyzeCluster(idx, eeTableName, startTS);
				}
				idx++;
			}
		}
		else if (doAnalysisOfCluster) {
			ArrayList<Integer> clusterId = new ArrayList<Integer>();
			PreparedStatement ps = conn.prepareStatement("select id from ee_pt_clusters");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				clusterId.add(rs.getInt(1));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			for (Integer iv:clusterId) {
				System.out.println("[CLUSTER]"+Thread.currentThread().getId()+" Doing Cluster .. analyzing :"+iv);
				analyzeCluster(iv,  eeTableName, startTS);
			}
		}
	}
    
	public static void prepForSave(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("update ee_addnl_info set cluster_id=null");
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement("delete from ee_pt_clusters");
		ps.execute();
		ps = Misc.closePS(ps);
		
	}
	
	public static String getNameForCluster(Connection conn, String eeTableName, int repId, List<ConvexHull.PointWithId> clusterPoints) throws SQLException {
		double lon = 0;
		double lat = 0;
		int cnt = 0;
		for (ConvexHull.PointWithId pt:clusterPoints) {
			lon += pt.first();
			lat += pt.second();
			cnt++;
		}
		lon = lon/cnt;
		lat = lat/cnt;
		GpsData data = null;
		int eventId = clusterPoints.get(0).getId();
		PreparedStatement ps = conn.prepareStatement("select event_begin_name from "+eeTableName+" where id=?");
		ps.setInt(1, eventId);
		ResultSet rs = ps.executeQuery();
	    String name = rs.next() ? rs.getString(1) : null;	
	    rs = Misc.closeRS(rs);
	    ps = Misc.closePS(ps);
		return name;
	}
	
	public static void analyzeCluster(int repId, String eeTableName, java.sql.Timestamp startTS) throws Exception {
		// get port_node_id
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String s1 = " select customer_id, count(*) "+ 
			  " from ee_addnl_info eea join "+eeTableName+" ee on (ee.id = eea.id) "+ 
			  "     join vehicle on (vehicle.id = ee.vehicle_id)  "+
			  " where eea.cluster_id=? "+
			  " group by customer_id order by count(*) desc "
			  ;
	        ps = conn.prepareStatement(s1);
	        ps.setInt(1, repId);
	        rs = ps.executeQuery();
	        int portNodeId = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
	        rs = Misc.closeRS(rs);
	        ps = Misc.closePS(ps);
	        Cache cache = Cache.getCacheInstance(conn);
	        MiscInner.PortInfo portInfo = cache.getParentPortNode(conn, portNodeId, DimInfo.getDimInfo(2));
	        if (portInfo != null)
	        	portNodeId = portInfo.m_id;
			// get pt counts
			String s2 = " select sum(case when is_lu=0 then 1 else 0 end), sum(1), count(distinct vehicle_id) "+ 
			  " from ee_addnl_info eea join "+eeTableName+" ee on (ee.id = eea.id) "+ 
			  " where eea.cluster_id=?"
			;
			ps = conn.prepareStatement(s2);
	        ps.setInt(1, repId);
	        rs = ps.executeQuery();
	        int lupt = 0;
	        int totpt = 0;
	        int distveh = 0;
	        if (rs.next()) {
	        	lupt = rs.getInt(1);
	        	totpt = rs.getInt(2);
	        	distveh = rs.getInt(3);
	        }
	        rs = Misc.closeRS(rs);
	        ps = Misc.closePS(ps);
	        
	
			// get overall vehicle and overall trips
			String s3 = " select count(*), count(distinct vehicle_id) from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
			" join trip_info on (trip_info.vehicle_id = vehicle.id) where trip_info.combo_start > ? "
			;
			ps = conn.prepareStatement(s3);
	        ps.setInt(1, portNodeId);
	        ps.setTimestamp(2, startTS);
	        rs = ps.executeQuery();
	        int tripCount = 0;
	        int vehCount = 0;
	        if (rs.next()) {
	        	tripCount = rs.getInt(1);
	        	vehCount = rs.getInt(2);
	        }
	        rs = Misc.closeRS(rs);
	        ps = Misc.closePS(ps);
	//    	create table ee_pt_clusters(id int not null, name varchar(200), port_node_id int, num_trips int, num_overall_vehicles int, num_points int, num_load_points int, num_vehicles int, shape geometry); 
	        
	        ps = conn.prepareStatement("update ee_pt_clusters set port_node_id=?, num_trips=?, num_overall_vehicles=?, num_points=?, num_load_points=?, num_vehicles=? where id=?");
	        ps.setInt(1, portNodeId);
	        ps.setInt(2, tripCount);
	        ps.setInt(3, vehCount);
	        ps.setInt(4, totpt);
	        ps.setInt(5, lupt);
	        ps.setInt(6, distveh);
	        ps.setInt(7, repId);
	        ps.execute();
	        ps = Misc.closePS(ps);
	        
	        if (!conn.getAutoCommit())
	        	conn.commit();
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		finally {
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}
	
	public static void save(String eeTableName, int repId, List<ConvexHull.PointWithId> clusterPoints, List<ConvexHull.PointWithId> hull) throws Exception {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		
			if (clusterPoints != null) {
				StringBuilder sb = new StringBuilder();
				if (hull != null) {
					//POLYGON((86.3264465332031 23.5639871284512,86.6670227050781 23.5098485289984,86.5447998046875 23.3170355766987,86.1740112304688 23.346038460872,86.3264465332031 23.5639871284512))
		    	////	create table ee_pt_clusters(id int not null, name varchar(200), port_node_id int, num_trips int, num_overall_vehicles int, num_points int, num_load_points int, num_vehicles int, shape geometry);
					
		    		boolean isFirst = true;
		    		for (ConvexHull.PointWithId pt : hull) {
		    			if (isFirst) {
		    				sb.append("POLYGON((");
		    				isFirst = false;
		    			}
		    			else {
		    				sb.append(",");
		    			}
		    			sb.append(pt.first()).append(" ").append(pt.second());
		    		}
		    		sb.append(",").append(hull.get(0).first()).append(" ").append(hull.get(0).second()).append("))");
				}
				String name = PointCluster.getNameForCluster(conn, eeTableName, repId, clusterPoints);
				PreparedStatement ps = conn.prepareStatement("insert into ee_pt_clusters(id, shape, name) values(?,geomfromtext(?),?)");
				ps.setInt(1, repId);
				ps.setString(2, sb == null || sb.length() == 0 ? null : sb.toString());
				ps.setString(3, name);
				ps.execute();
				ps = Misc.closePS(ps);
				ps = conn.prepareStatement("update ee_addnl_info set cluster_id=? where id=?");
				int cnt = 0;
				for (ConvexHull.PointWithId point:clusterPoints) {
					ps.setInt(1, repId);
					ps.setInt(2, point.getId());
					ps.addBatch();
					if (cnt > 10000) {
						ps.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
						cnt = 0;
					}
					cnt++;
				}
				ps.executeBatch();
				ps = Misc.closePS(ps);
			}
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		finally {
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}
	
	public static ArrayList<ConvexHull.PointWithId> getPtsToCluster(Connection conn, String eeTableName, boolean doIsInOp, boolean ignoreDurCat0) throws Exception {
		ArrayList<ConvexHull.PointWithId> retval = new ArrayList<ConvexHull.PointWithId>();
		
		String q = "select ee.event_begin_longitude, ee.event_begin_latitude, ee.id from ee_addnl_info join "+eeTableName+" ee on (ee.id = ee_addnl_info.id) where to_consider=1 " +(doIsInOp ? " and in_op=0 " : "") + (ignoreDurCat0 ? " and dur_cat<>0 " : "");
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(Integer.MIN_VALUE);
		//				System.out.println("GeneralizedQueryBuilder.printPage()   : 111  stmt.getFetchSize()   :   : "+stmt.getFetchSize());
		ResultSet rs = stmt.executeQuery(q);
		while (rs.next()) {
			retval.add(new ConvexHull.PointWithId(rs.getDouble(1), rs.getDouble(2), rs.getInt(3)));
		}
		rs = Misc.closeRS(rs);
		stmt.close();
		return retval;
	}
	
	public static ArrayList<MiscInner.Pair> getVehicleList(Connection conn, String eeTableName) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<MiscInner.Pair> retval = new ArrayList<MiscInner.Pair>();
		try {
			StringBuilder sb = new StringBuilder("select distinct vehicle.id, vehicle.customer_id from vehicle join ");
			sb.append(eeTableName);
			sb.append(" on (vehicle.id = ").append(eeTableName).append(".vehicle_id) ");
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				retval.add(new MiscInner.Pair(rs.getInt(1), rs.getInt(2)));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return retval;
	}
	
	public static void identifyInsideOpStation(Connection conn, ArrayList<Integer> ruleIds, ArrayList<MiscInner.Pair> vehicleIds, String eeTableName, java.sql.Timestamp startTS)  throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		try {
			StringBuilder sb = new StringBuilder("select id, event_begin_longitude, event_begin_latitude, event_start_time from ");
			sb.append(eeTableName);
			sb.append(" where vehicle_id=? and rule_id in (");
			if (ruleIds == null || ruleIds.size() == 0)
				sb.append("1");
			else 
				Misc.convertInListToStr(ruleIds, sb);
			sb.append(")");
			sb.append(" and event_start_time >= ?");
				
			ps = conn.prepareStatement(sb.toString());
			ps2 = conn.prepareStatement("insert  into ee_addnl_info (id, in_op) values (?,?) on duplicate key update in_op=in_op");
			int cnt = 0;
			for (MiscInner.Pair veh: vehicleIds) {
				ps.setInt(1, veh.first);
				ps.setTimestamp(2, startTS);
				rs = ps.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					double lon = rs.getDouble(2);
					double lat = rs.getDouble(3);
					boolean isinOp = ThreadContextCache.isInSomeOpStation(conn,lon, lat, veh.second, null, veh.first, Misc.sqlToLong(rs.getTimestamp(4)));
					ps2.setInt(1, id);
					ps2.setInt(2, isinOp ? 1 : 0);
					ps2.addBatch();
					cnt++;
					if (cnt > 10000) {
						ps2.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
						cnt = 0;
					}
				}
				rs = Misc.closeRS(rs);
			}
			ps2.executeBatch();
			if (!conn.getAutoCommit())
				conn.commit();

			ps2 = Misc.closePS(ps2);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void identifyLU(Connection conn, ArrayList<Integer> ruleIds, ArrayList<MiscInner.Pair> vehicleIds, String eeTableName, java.sql.Timestamp startTS)  throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		try {
			StringBuilder sb = new StringBuilder();			
			sb.append(
					"update ee_addnl_info join ").append(eeTableName).append(" ee on (ee_addnl_info.id = ee.id) join trip_info on (trip_info.vehicle_id = ee.vehicle_id and ee.event_start_time between trip_info.combo_start and trip_info.combo_end) ")
					.append(" set is_lu=(case when ee.event_start_time > trip_info.load_gate_out and ee.event_start_time < trip_info.unload_gate_in  then 0 ")
					.append(" when ee.event_start_time > trip_info.load_gate_out and trip_info.unload_gate_out is null and ee.event_start_time < trip_info.confirm_time then 0 ")
					.append(" when ee.event_start_time > trip_info.load_gate_out and trip_info.unload_gate_out is null and trip_info.confirm_time is null then 0 ")
					.append(" when  ee.event_start_time > trip_info.unload_gate_out and ee.event_start_time < trip_info.confirm_time then 1 ")
					.append(" when ee.event_start_time > trip_info.unload_gate_out and trip_info.confirm_time is null then 1  else 2 end) ")
					.append(" where ee.vehicle_id=? and ee.rule_id in (");
			if (ruleIds == null || ruleIds.size() == 0)
				sb.append("1");
			else 
				Misc.convertInListToStr(ruleIds, sb);
			sb.append(") ");
			sb.append(" and ee.event_start_time >= ?");
			ps = conn.prepareStatement(sb.toString());	
			for (MiscInner.Pair veh: vehicleIds) {
				ps.setInt(1, veh.first);
				ps.setTimestamp(2, startTS);
				ps.executeUpdate();
				if (!conn.getAutoCommit())
					conn.commit();
			}
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			//String eeTableName = "engine_events";
			//double distForCluster = 0.03;
			//int minPts = 10;
			String eeTableName = "eemimic_for_load_pt";
			double distForCluster = 0.03;
			int minPts = 4;
			java.sql.Timestamp startTS = new java.sql.Timestamp((new java.util.Date(118,0,1)).getTime());
		//	PointCluster.doAnalysis(conn, true, true, true, false, true, true, true, distForCluster, minPts, "engine_events", null, startTS);
//			public static void doAnalysis(Connection conn
//					, boolean calcIsInOp, boolean calcIsLU
//					, boolean doIsInOp, boolean ignoreDurCat0, boolean doCluster, boolean doConvexHullOfCluster, boolean doAnalysisOfCluster
//					,double distCluster, int minPtsInCluster
//					,String eeTableName, ArrayList<Integer> ruleIds, java.sql.Timestamp startTS) throws Exception {

			//public static void doAnalysis(
			//		boolean calcIsInOp, boolean calcIsLU
			//		, boolean doIsInOp, boolean ignoreDurCat0, boolean doCluster, boolean doConvexHullOfCluster, boolean doAnalysisOfCluster
			//		,double distCluster, int minPtsInCluster
			//		,String eeTableName, ArrayList<Integer> ruleIds, java.sql.Timestamp startTS) throws Exception {
			
			doAnalysis(
					 false, false
					, false, false, true, true,  false
					, 0.03, 50 //0.02,8
					,"eemimic_for_load_pt", null,null) ;

			if (!conn.getAutoCommit())
				conn.commit();
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	/*
	public static void identifyInOpAndCreeping(Connection conn, ArrayList<Integer> ruleIds, ArrayList<MiscInner.Pair> vehicleIds, String eeTableName, java.sql.Timestamp startTS)  throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		try {
			StringBuilder sb = new StringBuilder("select id, event_begin_longitude, event_begin_latitude, event_start_time, event_end_longitude, event_end_latitude, event_stop_time from ");
			sb.append(eeTableName);
			sb.append(" where vehicle_id=? and rule_id in (");
			if (ruleIds == null || ruleIds.size() == 0)
				sb.append("1");
			else 
				Misc.convertInListToStr(ruleIds, sb);
			sb.append(")");
			sb.append(" and event_start_time >= ?");
				
			ps = conn.prepareStatement(sb.toString());
			ps2 = conn.prepareStatement("update ee_addnl_info set is_creeping_self=? where id=? ");
			int cnt = 0;
			
			for (MiscInner.Pair veh: vehicleIds) {
				ps.setInt(1, veh.first);
				ps.setTimestamp(2, startTS);
				int prevId = Misc.getUndefInt();
				long prevStart = -1;
				long prevEnd = -1;
				double prevStartLon = Misc.getUndefDouble();
				double prevStartLat = Misc.getUndefDouble();
				double currStartLong = Misc.getUndefDouble();
				double currStartLat = Misc.getUndefDouble();
				
				rs = ps.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					double lon = rs.getDouble(2);
					double lat = rs.getDouble(3);
					boolean isinOp = ThreadContextCache.isInSomeOpStation(conn,lon, lat, veh.second, null, veh.first, Misc.sqlToLong(rs.getTimestamp(4)));
					ps2.setInt(1, id);
					ps2.setInt(2, isinOp ? 1 : 0);
					ps2.addBatch();
					cnt++;
					if (cnt > 10000) {
						ps2.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
						cnt = 0;
					}
				}
				rs = Misc.closeRS(rs);
			}
			ps2.executeBatch();
			ps2 = Misc.closePS(ps2);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	*/
	
	
}

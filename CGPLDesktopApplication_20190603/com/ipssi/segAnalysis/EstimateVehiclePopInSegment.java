package com.ipssi.segAnalysis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Pair;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.routemonitor.RouteDef;
import java.sql.Connection;

public class EstimateVehiclePopInSegment {
	 RTree rtreeOfSeg = null;
	 Map<Integer, SegmentCluster> segPointToCluster = new HashMap<Integer, SegmentCluster>();
	 private String getHelperKeyVehDay(int vehicleId, SimpleDateFormat sdf, java.util.Date dt) {
		 return sdf.format(dt)+vehicleId;
	 }
	 public void doEstimationAfterSegmentPrep(java.sql.Connection conn, int ruleId)  {
		 try {
			 String srvrName = Misc.getServerName();
			 if (Misc.isUndef(ruleId)) {
				 ruleId = 634;
				 if ("node_stag".equals(srvrName))
					 ruleId = 574;
			 }
			 Triple<ArrayList<Integer>, java.sql.Timestamp, java.sql.Timestamp> vehicleListTimeBound = getVehicleListTimeBound(conn, ruleId);
			 java.sql.Timestamp st = vehicleListTimeBound.second;
			 st.setHours(0);
			 st.setMinutes(0);
			 st.setSeconds(0);
			 java.sql.Timestamp en = vehicleListTimeBound.third;
			 en.setHours(23);
			 en.setMinutes(59);
			 en.setSeconds(59);
			 ArrayList<Integer> vehList = vehicleListTimeBound.first;
			 PreparedStatement ps = conn.prepareStatement("select longitude, latitude, gps_record_time from logged_data where vehicle_id = ? and attribute_id=0 and gps_record_time between ? and ? order by gps_record_time");
			 ps.setTimestamp(2, st);
			 ps.setTimestamp(3, en);
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			 double pointRecheckThresh = RTreeUtils.helpConvertKMInDegree(0.1);
			 double distThresh = RTreeUtils.helpConvertKMInDegree(10);
			 for (Integer vehId : vehList) {
				 if (vehId == null)
					 continue;
				 try {
					 System.out.println("[CLUSTER] calc vehicle days:"+vehId);
					 ps.setInt(1, vehId);
					 ResultSet rs = ps.executeQuery();
					 int vehPtCnt = 0;
					 int vehicleDaysCounted = 0;
					 double prevLon = Misc.getUndefDouble();
					 double prevLat = Misc.getUndefDouble();
					 while (rs.next()) {
						 vehPtCnt++;
						 double lon = rs.getDouble(1);
						 double lat = rs.getDouble(2);
						 java.util.Date dt = Misc.sqlToUtilDate(rs.getTimestamp(3));
						 java.util.Date dt1 = rs.getDate(3);
						 if (Math.abs(lon-prevLon) < pointRecheckThresh && Math.abs(lat-prevLat) < pointRecheckThresh) {
							 continue;
						 }
						 //first get nearest pts witin 10 km, then get cluster, then check if still relevant  (ie not counted) and then for each check if on road
						 List<Integer> arrayOfIds=new ArrayList<Integer>();
						 PrintId pid=new PrintId(); 
						 RTree rtree=rtreeOfSeg;
						//TODO DEBUG13 compile error  rtree.nearestN((new com.infomatiq.jsi.Point((float)lon, (float)lat)), pid, 300, (float)distThresh);
						 arrayOfIds=pid.getArrayOfIds();
						 for (Integer iv : arrayOfIds) {
							 SegmentCluster sc = segPointToCluster.get(iv);
							 String key = getHelperKeyVehDay(vehId, sdf, dt);
							 if (sc.helperVehicleDay.containsKey(key))
								 continue;
							 //check if is on segment or within 10 KM of end pts
							 int indexInSegList = iv % 1000;
							 boolean isOnSeg=false;
							 if (indexInSegList == 0 || indexInSegList == sc.helperPointInCluster.size()-1) {
								 isOnSeg = true;
							 }
							 else {
								 Triple<Double, Double, String> prev = sc.helperPointInCluster.get(indexInSegList-1);
								 Triple<Double, Double, String> curr = sc.helperPointInCluster.get(indexInSegList);
								 Triple<Double, Double, String> next = sc.helperPointInCluster.get(indexInSegList+1);
								 Pair<Double, Double> entry = RouteDef.checkWhereInSegment(lon, lat, prev.first, prev.second, curr.first, curr.second);
								 if (entry.first >= 0 && entry.first <= 1 && entry.second < 0.3)
									 isOnSeg = true;
								 else {
									 entry = RouteDef.checkWhereInSegment(lon, lat, curr.first, curr.second, next.first, next.second);
									 if (entry.first >= 0 && entry.first <= 1 && entry.second < 0.3)
										 isOnSeg = true;
								 }
							 }
							 if (isOnSeg) {
								 sc.helperVehicleDay.put(key, key);
								 vehicleDaysCounted++;
							 }
						 }
												 
						 prevLon = lon;
						 prevLat = lat;
					 }
					 rs = Misc.closeRS(rs);
					 
					 System.out.println("[CLUSTER] done calc vehicle days:"+vehId+ " Pts:"+vehPtCnt+", vehDays:"+vehicleDaysCounted);
				 }
				 catch (Exception e) {
					 e.printStackTrace();
					 //eat it
				 }
			 }
			 ps = Misc.closePS(ps);
		 }
		 catch (Exception e) {
			 e.printStackTrace();
		 }
	 }
	 public void save(Connection conn, SegmentCluster sc, boolean partialMode) {
		 PreparedStatement ps = null;
		 try {
			 if (partialMode)
				 ps = conn.prepareStatement("update ee_slow_cluster_segments set num_vehicle_days=(case when num_vehicle_days is null then 0 else num_vehicle_days end)+? where repid=?");
			 else
				 ps = conn.prepareStatement("update ee_slow_cluster_segments set num_vehicle_days=? where repid=?");
			 ps.setInt(1, sc.helperVehicleDay.size());
			 ps.setInt(2, sc.repId);
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
	 public EstimateVehiclePopInSegment() {
			Properties properties = new Properties();
			properties.put("MinNodeEntries", 5);
			properties.put("MaxNodeEntries", 10);
			RTree rtree = new RTree();
			rtree.init(properties);
			rtreeOfSeg = rtree;
	 }
	 private int currSegPtIndex = 0; //0..999 for each pt list and then bump
	 public void prepAddSegment(SegmentCluster sc) {
		 int origCurrSegPtIndex = currSegPtIndex;
		 for (int i=0,is=sc.helperPointInCluster == null ? 0 : sc.helperPointInCluster.size(); i<is; i++) {
			 Rectangle rectangle = new Rectangle();
			 rectangle.set(sc.helperPointInCluster.get(i).first.floatValue(), sc.helperPointInCluster.get(i).second.floatValue(), sc.helperPointInCluster.get(i).first.floatValue(), sc.helperPointInCluster.get(i).second.floatValue()); 
			 rtreeOfSeg.add(rectangle, currSegPtIndex);
			 segPointToCluster.put(currSegPtIndex, sc);
			 sc.helperVehicleDay = new HashMap<String, String>();
			 currSegPtIndex++;
		 }
		 currSegPtIndex = origCurrSegPtIndex+1000;
	 }
	 
	 public Triple<ArrayList<Integer>, java.sql.Timestamp, java.sql.Timestamp> getVehicleListTimeBound(Connection conn, int ruleId) {
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 java.sql.Timestamp mi = null;
		 java.sql.Timestamp mx = null;
		 ArrayList<Integer> vehicleList = new ArrayList<Integer>();
		 try {
			 ps = conn.prepareStatement("select vehicle_id, min(event_start_time), max(event_stop_time) from engine_events_slow_ee where rule_id = ? group by vehicle_id");
			 ps.setInt(1, ruleId);
			 rs = ps.executeQuery();
			 	 
			 while (rs.next()) {
				 int vehicleId = rs.getInt(1);
				 vehicleList.add(vehicleId);
				 java.sql.Timestamp t1 = rs.getTimestamp(2);
				 java.sql.Timestamp t2 = rs.getTimestamp(3);
				 if (t1 != null && (mi == null || mi.after(t1)))
				 	mi = t1;
				 if (t1 != null && (mx == null || mx.before(t1)))
					 	mx = t1;
				 if (t2 != null && (mx == null || mx.before(t2)))
					 	mx = t2;
			 }
			 rs = Misc.closeRS(rs);
			 ps = Misc.closePS(ps);
		 }
		 catch (Exception e) {
			 
		 }
		 finally {
			 rs = Misc.closeRS(rs);
			 ps = Misc.closePS(ps);
		 }
		 return new Triple<ArrayList<Integer>, java.sql.Timestamp, java.sql.Timestamp>(vehicleList, mi, mx);
	 }

}

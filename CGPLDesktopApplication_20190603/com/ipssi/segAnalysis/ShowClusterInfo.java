package com.ipssi.segAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.routemonitor.RouteDef;

public class ShowClusterInfo {
	private static EstimateVehiclePopInSegment userPtLookupHelper = null;
	public synchronized static void resetEstimator() {
		userPtLookupHelper = null;
	}
	public synchronized static void initPtLookupHelper(Connection conn) {
		if (userPtLookupHelper == null) {
			userPtLookupHelper = SegmentCluster.hackCreatePointLookupHelper(conn);
		}
	}
	public synchronized static Pair<SegmentCluster, Boolean> getNearestCluster(Connection conn, double lon, double lat) {
		//second is true if actually on segment else false
		initPtLookupHelper(conn);
		double pointRecheckThresh = RTreeUtils.helpConvertKMInDegree(0.1);
		double distThresh = RTreeUtils.helpConvertKMInDegree(100);
		double nearnessThresh = 0.3;
		 //first get nearest pts witin 10 km, then get cluster, then check if still relevant  (ie not counted) and then for each check if on road
		 List<Integer> arrayOfIds=new ArrayList<Integer>();
		 PrintId pid=new PrintId(); 
		 RTree rtree=userPtLookupHelper.rtreeOfSeg;
	//TODO DEBUG13 compile error	 rtree.nearestN((new com.infomatiq.jsi.Point((float)lon, (float)lat)), pid, 300, (float)distThresh);
		 
		 arrayOfIds=pid.getArrayOfIds();
		 SegmentCluster minSeg = null;
		 Point pt = new Point(lon, lat);
		 double minDistToSeg = Double.MAX_VALUE;
		 for (Integer iv : arrayOfIds) {
			 SegmentCluster sc = userPtLookupHelper.segPointToCluster.get(iv);
			 int indexInSegList = iv % 1000;
			 boolean innerOnSeg = false;
			 if (indexInSegList == 0 || indexInSegList == sc.helperPointInCluster.size()-1) {
				 sc.helperPointInCluster.get(indexInSegList);
				 Triple<Double, Double, String> onSeg = sc.helperPointInCluster.get(indexInSegList);
				 double d = pt.fastGeoDistance(onSeg.first, onSeg.second);
				 if(minDistToSeg > d) {
					 minDistToSeg = d;
					 minSeg = sc;
				 }
			 }
			 else {
				 Triple<Double, Double, String> prev = sc.helperPointInCluster.get(indexInSegList-1);
				 Triple<Double, Double, String> curr = sc.helperPointInCluster.get(indexInSegList);
				 Triple<Double, Double, String> next = sc.helperPointInCluster.get(indexInSegList+1);
				 Pair<Double, Double> entry = RouteDef.checkWhereInSegment(lon, lat, prev.first, prev.second, curr.first, curr.second);
				 Pair<Double, Double>entry2 = RouteDef.checkWhereInSegment(lon, lat, curr.first, curr.second, next.first, next.second);
				 
				 if (entry.first >= 0 && entry.first <= 1) {
					 if (minDistToSeg > entry.second) {
						 minDistToSeg = entry.second;
						 minSeg = sc;
					 }
				 }
				 if (entry2.first >= 0 && entry2.first <= 1) {
					 if (minDistToSeg > entry2.second) {
						 minDistToSeg = entry2.second;
						 minSeg = sc;
					 }
				 }
			 }
		 }
		 return new Pair<SegmentCluster, Boolean> (minSeg, minDistToSeg < nearnessThresh);
	}
	
	static public class CountByHrForCluster {
		int lowHr;
		int hiHr;
		double ratio;
		int count;
		public static Triple<ArrayList<CountByHrForCluster>, Integer, Integer> getResults(Connection conn, int repId) {
			ArrayList<CountByHrForCluster> result = new ArrayList<CountByHrForCluster>();
			int numEvents = 0;
			int numVehicles = 0;
		 	PreparedStatement ps = null;
		 	ResultSet rs = null;
			try {
				ps = conn.prepareStatement("select floor(hour(ee.event_start_time)/2), count(distinct vehicle_id, date(event_start_time)) from engine_events_slow_ee ee where crit_eval_id = ? group by floor(hour(ee.event_start_time)/2) order by floor(hour(ee.event_start_time)/2)");
				ps.setInt(1, repId);
				rs = ps.executeQuery();
				int count = 0;
				while (rs.next()) {
					int hr = rs.getInt(1);
					int cnt = rs.getInt(2);
					count += cnt;
					CountByHrForCluster res = new CountByHrForCluster(hr*2, hr*2+1, cnt, count);
					result.add(res);
				}
				rs= Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				for (CountByHrForCluster res : result) {
					res.ratio = res.ratio/count;
					res.count = count;
				}
				ps = conn.prepareStatement("select num_event_vehicle_days, num_vehicle_days from ee_slow_cluster_segments where repid=?");
				ps.setInt(1, repId);
				rs = ps.executeQuery();
				if (rs.next()) {
					numEvents = rs.getInt(1);
					numVehicles = rs.getInt(2);
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			finally {
				rs= Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			
			return new Triple<ArrayList<CountByHrForCluster>, Integer, Integer>(result, numEvents, numVehicles);
		}
		public int getLowHr() {
			return lowHr;
		}
		public void setLowHr(int lowHr) {
			this.lowHr = lowHr;
		}
		public int getHiHr() {
			return hiHr;
		}
		public void setHiHr(int hiHr) {
			this.hiHr = hiHr;
		}
		public double getRatio() {
			return ratio;
		}
		public void setRatio(double ratio) {
			this.ratio = ratio;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public CountByHrForCluster(int lowHr, int hiHr, double ratio, int count) {
			super();
			this.lowHr = lowHr;
			this.hiHr = hiHr;
			this.ratio = ratio;
			this.count = count;
		}		
	}
   static public class UserInputForCluster {
	   private int id;
	   private int repId;
	   private double lon;
	   private double lat;
	   private String comment;
	   private int commentCat;
	   private int userId;
	   private long createdOn;
	   private String userName;
	   public static ArrayList<UserInputForCluster> getResults(Connection conn,double lon, double lat) {
		   Pair<SegmentCluster, Boolean> nearest = getNearestCluster(conn, lon, lat);
		   return getResults(conn, nearest == null || nearest.first == null ? Misc.getUndefInt() : nearest.first.repId);
	   }
	 public static ArrayList<UserInputForCluster> getResults(Connection conn, int repId) {
		 ArrayList<UserInputForCluster> result = new ArrayList<UserInputForCluster>();
		 	PreparedStatement ps = null;
		 	ResultSet rs = null;
			try {
				ps = conn.prepareStatement("select cluster_user_info.id, repid, longitude, latitude, comments, comment_cat, user_id, created_on, users.name from cluster_user_info left outer join users on (users.id = user_id) where repid = ? order by created_on desc");
				ps.setInt(1, repId);
				rs = ps.executeQuery();
				int count = 0;
				while (rs.next()) {
					UserInputForCluster userInfo = new UserInputForCluster(Misc.getRsetInt(rs,1), Misc.getRsetInt(rs, 2), Misc.getRsetDouble(rs, 3), Misc.getRsetDouble(rs,4), rs.getString(5),  Misc.getRsetInt(rs,6), Misc.getRsetInt(rs, 7), Misc.sqlToLong(rs.getTimestamp(8)), rs.getString(9));
					result.add(userInfo);
				}
				rs= Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			finally {
				rs= Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			return result;
	 }
	public static int saveUserComment(Connection conn, int repId, double longitude, double latitude, String comment, int cat, int userId) {
		int result = Misc.getUndefInt();
	 	PreparedStatement ps = null;
	 	ResultSet rs = null;
		try {
			ps = conn.prepareStatement("insert into cluster_user_info(repid, longitude, latitude, comments, comment_cat, user_id, created_on) values (?,?,?,?,?,?,now())");
			Misc.setParamInt(ps, repId, 1);
			Misc.setParamDouble(ps, longitude, 2);
			Misc.setParamDouble(ps, latitude, 3);
			ps.setString(4, comment);
			Misc.setParamInt(ps, cat, 5);
			Misc.setParamInt(ps, userId, 6);
			ps.execute();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				result = rs.getInt(1);
			}
			
			rs= Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs= Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return result;
	}
	public UserInputForCluster(int id, int repId, double lon, double lat, String comment,
			int commentCat, int userId, long createdOn, String userName) {
		super();
		this.id = id;
		this.repId = repId;
		this.lon = lon;
		this.lat = lat;
		this.comment = comment;
		this.commentCat = commentCat;
		this.userId = userId;
		this.createdOn = createdOn;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRepId() {
		return repId;
	}
	public void setRepId(int repId) {
		this.repId = repId;
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
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getCommentCat() {
		return commentCat;
	}
	public void setCommentCat(int commentCat) {
		this.commentCat = commentCat;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public long getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	   
   }
}

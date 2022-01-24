package com.ipssi.segAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;

public class ConvexHull {
	public static class PointWithId implements Clusterable {
		//double first;
		//double second;
		public double first() { return point[0];}
		public double second() { return point[1];}
		double[] point = null;
		int id;
		public boolean equals(PointWithId rhs) {
			return rhs.id == this.id;
		}
		public int getId() {
			return id;
		}
		public double[] getPoint() {
			return point;
		}
		public PointWithId(double first, double second, int id) {
			point = new double[2];
			point[0] = first;
			point[1] = second;
			this.id = id;
		}
	}
	
	//public static int indexOf(List<PointWithId> theList, PointWithId point) {
	//	for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++) {
	//		PointWithId pt = theList.get(i);
	//		if (pt.id == point.id)
	//			return i;
	//	}
	//	return -1;
	//}
	
	//http://www.sanfoundry.com/java-program-implement-quick-hull-algorithm-find-convex-hull/
	//This is a java program to find a points in convex hull using quick hull method
	//source: Alexander Hrishov's website
	//URL: http://www.ahristov.com/tutorial/geometry-games/convex-hull.html
	public List<PointWithId> quickHull(List<PointWithId> points)
    {
        List<PointWithId> convexHull = new ArrayList<PointWithId>();
        if (points.size() < 3) {
        	ArrayList<PointWithId> retval = new ArrayList<PointWithId>();
        	for (PointWithId item:points)
        		retval.add(item);
            return retval;
        }
 
        int minPoint = -1, maxPoint = -1;
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        PointWithId A = null;
        PointWithId B = null;
        for (PointWithId point: points) {
            if (point.first() < minX) {
                minX = point.first();
                A = point;
            }
            if (point.first() > maxX)
            {
                maxX = point.first();
                B = point;
            }
        }
        convexHull.add(A);
        convexHull.add(B);
        points.remove(A);
        points.remove(B);
 
        ArrayList<PointWithId> leftSet = new ArrayList<PointWithId>();
        ArrayList<PointWithId> rightSet = new ArrayList<PointWithId>();
 
        for (PointWithId p:points)
        {
            if (pointLocation(A, B, p) == -1)
                leftSet.add(p);
            else if (pointLocation(A, B, p) == 1)
                rightSet.add(p);
        }
        hullSet(A, B, rightSet, convexHull);
        hullSet(B, A, leftSet, convexHull);
 
        return convexHull;
    }
 
    public double distance(PointWithId A, PointWithId B, PointWithId C)
    {
        double ABx = B.first() - A.first();
        double ABy = B.second() - A.second();
        double num = ABx * (A.second() - C.second()) - ABy * (A.first() - C.first());
        if (num < 0)
            num = -num;
        return num;
    }
 
    public void hullSet(PointWithId A, PointWithId B, List<PointWithId> set, List<PointWithId> hull)
    {
        int insertPosition = hull.indexOf(B);//indexOf(hull,B);//hull.indexOf(B);
        if (set.size() == 0)
            return;
        if (set.size() == 1)
        {
        	PointWithId p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }
        double dist = Double.MIN_VALUE;
        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++)
        {
        	PointWithId p = set.get(i);
            double distance = distance(A, B, p);
            if (distance > dist)
            {
                dist = distance;
                furthestPoint = i;
            }
        }
        PointWithId P = set.get(furthestPoint);
        set.remove(furthestPoint);
        hull.add(insertPosition, P);
 
        // Determine who's to the left of AP
        ArrayList<PointWithId> leftSetAP = new ArrayList<PointWithId>();
        for (int i = 0; i < set.size(); i++)
        {
        	PointWithId M = set.get(i);
            if (pointLocation(A, P, M) == 1)
            {
                leftSetAP.add(M);
            }
        }
 
        // Determine who's to the left of PB
        ArrayList<PointWithId> leftSetPB = new ArrayList<PointWithId>();
        for (int i = 0; i < set.size(); i++)
        {
        	PointWithId M = set.get(i);
            if (pointLocation(P, B, M) == 1)
            {
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);
 
    }
 
    public int pointLocation(PointWithId A, PointWithId B, PointWithId P)
    {
        double cp1 = (B.first() - A.first()) * (P.second() - A.second()) - (B.second() - A.second()) * (P.first() - A.first());
        if (cp1 > 0)
            return 1;
        else if (cp1 == 0)
            return 0;
        else
            return -1;
    }

    public static void doCalc(Connection conn, ArrayList<Segment> segList, int repId) {
    	System.out.println("[CONVEX] doing convexhull for:"+repId+" Points:"+segList.size());
    	ArrayList<PointWithId> points = new ArrayList<PointWithId>();
    	for (Segment seg:segList) {
    		points.add(new PointWithId(seg.start.first, seg.start.second, seg.id));
    		points.add(new PointWithId(seg.end.first, seg.end.second, -1*seg.id));
    	}
    	 ConvexHull qh = new ConvexHull();
    	 System.out.println("[CONVEX] done preprocess for convexhull for:"+repId+" Points:"+segList.size());
         List<PointWithId> p = qh.quickHull(points);         
         System.out.println("[CONVEX] saving for convexhull for:"+repId+" Points:"+segList.size()+ " Ploygon Pts:"+p.size());
         saveResult(conn, p, repId);
         
         System.out.println("[CONVEX] saved for convexhull for:"+repId+" Points:"+segList.size()+ " Ploygon Pts:"+p.size());
    }
    
    public static void doCalcFromDB(Connection conn, String table) {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		if (table == null)
    			table = "engine_events_slow_ee";
    		
    		ps = conn.prepareStatement("select ee.crit_eval_id, ee.id, ee.event_begin_longitude, ee.event_begin_latitude, ee.event_end_longitude, ee.event_end_latitude, ee.event_begin_name, ee.event_end_name from "+table+" ee where ee.crit_eval_id > 0 order by crit_eval_id, event_begin_longitude, event_begin_latitude desc");
    		rs = ps.executeQuery();
    		int prevRepId = Misc.getUndefInt();
    		ArrayList<Segment> segList = null;
    		while (rs.next()) {
    			int repId = rs.getInt(1);
    			if (repId != prevRepId) {
    				if (!Misc.isUndef(prevRepId)) {
    					doCalc(conn, segList, prevRepId);
    				}
    				segList = new ArrayList<Segment>();
    				prevRepId = repId;
    			}
    			segList.add(new Segment(rs.getInt(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), rs.getString(7), rs.getString(8)));
    		}
    		if (!Misc.isUndef(prevRepId)) {
    			doCalc(conn, segList, prevRepId);
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
    public static void removeAllResult(Connection conn) {
    	PreparedStatement ps = null;
    	try {
    		ps = conn.prepareStatement("truncate ee_convexhull");
    		ps.execute();
    		ps = Misc.closePS(ps);
    		ps = conn.prepareStatement("truncate ee_convexhull_points");
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
    public static void saveResult(Connection conn, List<PointWithId> ptList, int repId) {
    	PreparedStatement ps = null;
    	try {
    		ps = conn.prepareStatement("delete from ee_convexhull where repid = ?");
    		ps.setInt(1, repId);
    		ps.execute();
    		ps = Misc.closePS(ps);
    		ps = conn.prepareStatement("delete from ee_convexhull_points where repid = ?");
    		ps.setInt(1, repId);
    		ps.execute();
    		ps = Misc.closePS(ps);
    		ps = conn.prepareStatement("insert into ee_convexhull_points (repid, lon,lat,seq) values (?,?,?,?)");
    		int seq = 0;
    		for (PointWithId pt:ptList) {
    			ps.setInt(1, repId);
    			ps.setDouble(2, pt.first());
    			ps.setDouble(3, pt.second());
    			ps.setDouble(4, seq++);
    			ps.addBatch();
    		}
    		ps.executeBatch();
    		ps = Misc.closePS(ps);
    		ps = conn.prepareStatement("insert into ee_convexhull (repid, shape) values (?,geomfromtext(?))");
    		ps.setInt(1, repId);
    		//POLYGON((86.3264465332031 23.5639871284512,86.6670227050781 23.5098485289984,86.5447998046875 23.3170355766987,86.1740112304688 23.346038460872,86.3264465332031 23.5639871284512))
    		StringBuilder sb = new StringBuilder();
    		boolean isFirst = true;
    		for (PointWithId pt : ptList) {
    			if (isFirst) {
    				sb.append("POLYGON((");
    				isFirst = false;
    			}
    			else {
    				sb.append(",");
    			}
    			sb.append(pt.first()).append(" ").append(pt.second());
    		}
    		sb.append(",").append(ptList.get(0).first()).append(" ").append(ptList.get(0).second()).append("))");
    		ps.setString(2, sb.toString());
    		ps.execute();
    		ps = Misc.closePS(ps);
    		if (!conn.getAutoCommit()) {
           	 conn.commit();
            }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	finally {
    		ps = Misc.closePS(ps);
    	}
    }
    public static void main(String args[])
    {
    	Connection conn = null;
    	try {
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		doCalcFromDB(conn, "engine_events_slow_ee_norm");
    	}
    	catch (Exception e) {
    		
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
       // ArrayList<PointWithId> points = null;
       // ConvexHull qh = new ConvexHull();
       // ArrayList<PointWithId> p = qh.quickHull(points);
        //System.out
        //        .println("The points in the Convex hull using Quick Hull are: ");
        //for (int i = 0; i < p.size(); i++)
        //    System.out.println("(" + p.get(i).first() + ", " + p.get(i).second() + ")");
    }
}

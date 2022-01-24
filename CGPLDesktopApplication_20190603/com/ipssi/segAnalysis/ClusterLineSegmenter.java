package com.ipssi.segAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;

public class ClusterLineSegmenter {
	
	public static Pair<Boolean, ArrayList<Triple<Double, Double, String>>> doRegression(SegmentCluster sc, double maxThreshForSegmentKM) {
		//0. get sz of diagnoal
		//1 check if x dim is longer or y dim is longer .. the longer one becomes X, the other becomes Y
		//diagonal/maxThresh is number of segments we divide X into that and do linear regression
		// for each intermediate point we get value from left and value from right and we average that .. 
		double dist = Point.fastGeoDistance(sc.minBound.first, sc.minBound.second, sc.maxBound.first, sc.maxBound.second);
		int numSegment = (int) Math.ceil(dist/maxThreshForSegmentKM);
		if (numSegment == 0)
			numSegment = 1;
		boolean isXLongerThanY = sc.maxBound.first - sc.minBound.first >= sc.maxBound.second - sc.minBound.second;
		double delta = (sc.maxBound.first - sc.minBound.first)/numSegment;
		double lo = sc.minBound.first;
		
		if (isXLongerThanY) {
		}
		else {
			delta = (sc.maxBound.second-sc.minBound.second)/numSegment;
			lo = sc.minBound.second;
		}
		int prevPosn = -1;
		ArrayList<Triple<Double, Double, String>> results = new ArrayList<Triple<Double, Double, String>>();
		System.out.println("[CLUSTER] Segment calc:"+sc.repId+" Pts:"+sc.segList.size()+" XLarger:"+isXLongerThanY+" Dist:"+dist+" NumSegment:"+numSegment+" Delta:"+delta);
		for (int i=0;i<numSegment;i++) {
			double hi = lo + delta;
			//count pts in between lo and hi
			SimpleRegression model = new SimpleRegression(true);
			double aPtX = Misc.getUndefDouble();
			double aPtY = Misc.getUndefDouble();
			double nearestLo = hi;
			String nearestLoName = null;
			double nearestHi = lo;
			String nearestHiName = null; 
			
			for (Segment seg : sc.segList) {
				if (isXLongerThanY) {
					double v = seg.start.first;
					if (v >= lo && v <= hi) {
						aPtX =v;
						aPtY = seg.start.second;
						model.addData(v, seg.start.second);
						if (nearestLoName == null || v < nearestLo) {
							nearestLoName = seg.startName;
							nearestLo = v;
						}
						if (nearestHiName == null || v > nearestHi) {
							nearestHiName = seg.startName;
							nearestHi = v;
						}
					}
					v = seg.end.first;
					if (v >= lo && v <= hi) {
						aPtX =v;
						aPtY = seg.end.second;
						model.addData(v, seg.end.second);
						if (nearestLoName == null || v < nearestLo) {
							nearestLoName = seg.endName;
							nearestLo = v;
						}
						if (nearestHiName == null || v > nearestHi) {
							nearestHiName = seg.endName;
							nearestHi = v;
						}
					}
				}
				else {
					double v = seg.start.second;
					if (v >= lo && v <= hi) {
						aPtX =v;
						aPtY = seg.start.first;
						model.addData(v, seg.start.first);
						if (nearestLoName == null || v < nearestLo) {
							nearestLoName = seg.startName;
							nearestLo = v;
						}
						if (nearestHiName == null || v > nearestHi) {
							nearestHiName = seg.startName;
							nearestHi = v;
						}
					}
					v = seg.end.second;
					if (v >= lo && v <= hi) {
						aPtX =v;
						aPtY = seg.end.first;
						model.addData(v, seg.end.first);
						if (nearestLoName == null || v < nearestLo) {
							nearestLoName = seg.endName;
							nearestLo = v;
						}
						if (nearestHiName == null || v > nearestHi) {
							nearestHiName = seg.endName;
							nearestHi = v;
						}
					}
				}
			}
			//now do regression
			double x1 = 0;
			double y1 = 0;
			double x2 = 0;
			double y2 = 0;
			int numpts = (int) model.getN();
			if (numpts == 0) {
				lo = hi;
				continue;
			}
			else if (numpts == 1) {
				if (results.size() > 0) {
					x1 = results.get(results.size()-1).first;
					y1 = results.get(results.size()-1).second;
					x2 = aPtX;
					y2 = aPtY;
					hi = x2;
				}
			}
			else {
				double m = model.getSlope();//y = mx+c
				double c = model.getIntercept();//
				x1 = lo;
				y1 = m*lo+c;
				x2 = hi;
				y2 = m*hi+c;
			}
			if (results.size() == 0) {
				results.add(new Triple<Double, Double, String>(x1,y1,nearestLoName));
			}
			if (results.size() > 0) {
				double y0 = results.get(results.size()-1).second;
				y0 = (y0+y1)/2;
				results.get(results.size()-1).second = y0;
			}
			results.add(new Triple<Double, Double, String>(x2,y2, nearestHiName));
			lo = hi;
		}//for each segment
		System.out.println("[CLUSTER] Segment calc done:"+sc.repId+" Pts:"+sc.segList.size()+" XLarger:"+isXLongerThanY+" Dist:"+dist+" NumSegment:"+numSegment+" Delta:"+delta+" Results:"+results);
		return new Pair<Boolean, ArrayList<Triple<Double, Double, String>>>(isXLongerThanY, results);
	}
	
	public static void save(Connection conn, int clusterId, boolean firstIsLon, ArrayList<Triple<Double,Double,String>> results)  {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("delete from ee_slow_cluster_segments where repid = ?");
			ps.setInt(1, clusterId);
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("delete from ee_slow_cluster_points where repid = ?");
			ps.setInt(1, clusterId);
			ps.execute();
			ps = Misc.closePS(ps);
			
			ps = conn.prepareStatement("insert into ee_slow_cluster_points (repid, lon,lat,seq,name) values (?,?,?,?,?)");
    		int seq = 0;
    		for (Triple<Double, Double, String> pt:results) {
    			ps.setInt(1, clusterId);
    			ps.setDouble(2, firstIsLon ? pt.first : pt.second);
    			ps.setDouble(3, firstIsLon ? pt.second : pt.first);
    			ps.setDouble(4, seq++);
    			ps.setString(5, pt.third);
    			ps.addBatch();
    		}
    		ps.executeBatch();
    		ps = Misc.closePS(ps);
    		ps = conn.prepareStatement("insert into ee_slow_cluster_segments (repid, shape) values (?,geomfromtext(?))");
    		ps.setInt(1, clusterId);
    		//POLYGON((86.3264465332031 23.5639871284512,86.6670227050781 23.5098485289984,86.5447998046875 23.3170355766987,86.1740112304688 23.346038460872,86.3264465332031 23.5639871284512))
    		StringBuilder sb = new StringBuilder();
    		boolean isFirst = true;
    		for (Triple<Double, Double, String> pt : results) {
    			if (isFirst) {
    				sb.append("POLYGON((");
    				isFirst = false;
    			}
    			else {
    				sb.append(",");
    			}
    			sb.append(firstIsLon ? pt.first : pt.second).append(" ").append(firstIsLon ? pt.second : pt.first);
    		}
    		sb.append(",").append(firstIsLon ? results.get(0).first : results.get(0).second).append(" ").append(firstIsLon ? results.get(0).second : results.get(0).first).append("))");
    		ps.setString(2, sb.toString());
    		ps.execute();
    		ps = Misc.closePS(ps);
    		if (!conn.getAutoCommit()) {
    			conn.commit();
            }
		}
		catch (Exception e) {
			
		}
		finally {
			
		}
	}
	
}

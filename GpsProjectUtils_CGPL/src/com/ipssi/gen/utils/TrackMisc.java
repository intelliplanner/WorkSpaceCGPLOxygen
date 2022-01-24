package com.ipssi.gen.utils;

import java.sql.*;
import java.util.*;
//import java.io.*; //import oracle.xml.parser.v2.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import javax.servlet.jsp.*;
//import org.xml.sax.*;
//import org.w3c.dom.*;
//import org.apache.xpath.*;

import com.ipssi.RegionTest.GeometryTest;
import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.PointDescription;
import com.ipssi.geometry.Region;
import com.ipssi.map.utils.ApplicationConstants;

//import java.io.*;
//import java.text.*;

public class TrackMisc {
	public static double g_degToRadFactor = Math.PI / 180.0;

	public static boolean isEqual(Point pt1, Point pt2, double thresh) {
		double dist = getSimpleDistance(pt1, pt2);
		return dist <= thresh;
	}

	public static double getSimpleDistance(Point pt1, Point pt2) { // this assumes lat/lon are very close - does not get great circle distance
		// various computational improvements can be done to get even smaller approx - esp if we have to guess if distance is
		// approx small - do delta on on lon/lat and get sqrt of dist. (ignore the cos factor below)

		// various degrees of approximation can be done esp to check if points same or points less than x dist without requiring
		// cos or sqrt func call
		// we need to benchmark to see if there is any incremental improvement -
		// one for example would be to keep cos factor for India's latitude (say 5 degree bucketing).
		// the other would be essentially do max of delta lon/delta lat and (mult by R cos lon/R)

		// See also getSimpleDistAndAizmuth
        return pt1.fastGeoDistance(pt2);
	}


	public static ArrayList<PointDescription> getIntersectingPointsInBoxAroundPoint(Connection dbConn, int portNodeId, Point point, Point rangePoint)
			throws Exception {
		// Since we are doing innodb - we really can't rely on Rtree indexing - so we will drive our query to be such that
		// we search for portNodeId then we search for points whose lon is between lon+/- lonRange
		// Re search in lonRange & latRange, we will need to keep Btree index for latRange - not sure if that is performant or
		// iterate in java
		try {
			ArrayList<PointDescription> retval = new ArrayList<PointDescription>();
			PreparedStatement ps = dbConn.prepareStatement(TrackQuery.GET_POINTS_IN_RANGE);
			ps.setInt(1, portNodeId);
			ps.setDouble(2, point.getLongitude()-rangePoint.getLongitude());
			ps.setDouble(3, point.getLongitude()+rangePoint.getLongitude());
			
			ps.setDouble(4, point.getLatitude()-rangePoint.getLatitude());
			ps.setDouble(5, point.getLatitude()+rangePoint.getLatitude());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
			//	int pointId = rs.getInt(1);
				String ptname = rs.getString(2);
				double ptlon = rs.getDouble(3);
				double ptlat = rs.getDouble(4);
				PointDescription pt = new PointDescription(ptlon, ptlat, ptname);
				retval.add(pt);
			}
			rs.close();
			ps.close();
			return retval;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*public static boolean isPointInRegion(Region reg, double lon, double lat) {// hack ..
		return true;
	}*/
		public static ArrayList<Region> getRegionsContainingPoint(Connection dbConn, int portNodeId, Point point) throws Exception {
			// Since we are doing innodb - we really can't rely Rtree indexing - so we will drive our query to be such that
			// we search for portNodeId then we search for region whose mbr x1,x2 contains lon and mbr y1,y2 contains lat.
			// there is no efficient way to do the above easily (and we will have to rely on trial/error to determin
			ArrayList<Region> retval = new ArrayList<Region>();
			try {
				
				ArrayList<RegionTestHelper> rthl = RegionTest.getRegionsContaining(dbConn, point, portNodeId, Misc.getUndefInt());
				
				if (rthl != null && rthl.size() > 0) {
					for (int i=0,is=rthl.size();i<is;i++) {
						retval.add(rthl.get(i).region);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//eat it;
			}
			return retval;
		}

	public static ArrayList<Region> getRegionsContainingPointOld(Connection dbConn, int portNodeId, Point point) throws Exception {
		// Since we are doing innodb - we really can't rely Rtree indexing - so we will drive our query to be such that
		// we search for portNodeId then we search for region whose mbr x1,x2 contains lon and mbr y1,y2 contains lat.
		// there is no efficient way to do the above easily (and we will have to rely on trial/error to determin

		try {
			ArrayList<Region> retval = new ArrayList<Region>();
			PreparedStatement ps = dbConn.prepareStatement(TrackQuery.GET_REGIONS_CONTAINING_POINT);
			ps.setInt(1, portNodeId);
			ps.setDouble(2, point.getLongitude());
			ps.setDouble(3, point.getLongitude());
			ps.setDouble(4, point.getLatitude());
			ps.setDouble(5, point.getLatitude());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				//int regionId = rs.getInt(1);
				String regionname = rs.getString(2);
				// regions.id, regions.name, regions.mbr_same, regions.ll_lon, regions.ll_lat, regions.ur_lon, regions.ur_lat, regions.wkt
				boolean ismbrSame = rs.getInt(3) == 1;

				double llLon = rs.getDouble(4);
				double llLat = rs.getDouble(5);
				double urLon = rs.getDouble(5);
				double urLat = rs.getDouble(6);
				String wkt = rs.getString(7);
				Region reg = new Region();
				reg.m_name = regionname;
				reg.m_llCoord = new Point(llLon, llLat);
				reg.m_urCoord = new Point(urLon, urLat);
				// reg.m_definition = wkt;
				reg.m_isMBRSameAsRegion = ismbrSame;
				if (ismbrSame || GeometryTest.pointInRegion(point, wkt) ) {
					retval.add(reg);
				}
			}
			rs.close();
			ps.close();
			return retval;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static com.ipssi.geometry.Point getBoxAroundRange(Point point, double distanceKM) {

		double horizCircleRadius = ApplicationConstants.EARTH_RADIUS * Math.cos(point.getLatitude() / 180.0 * Math.PI);
		// double delta x = r cos theta * delta lon = distance => delta lon = distance/r*cos theta
		// double delta y = r delta lat = distance => delta lat = distance/r
		double deltaLat = distanceKM / ApplicationConstants.EARTH_RADIUS * 180.0 / Math.PI;
		double deltaLon = distanceKM / horizCircleRadius * 180.0 / Math.PI;
		return new com.ipssi.geometry.Point(deltaLon, deltaLat);
	}

	public static Pair<Double,Double> getSimpleDistAndAizmuth(Point refPoint, Point point) //first = dist, second = lat 
	{
		double horizCircleFactor = Math.cos((refPoint.getLatitude() + point.getLatitude()) / 2 * g_degToRadFactor);
		double deltaLon = (point.getLongitude() - refPoint.getLongitude()) * g_degToRadFactor * horizCircleFactor;
		double deltaLat = (point.getLatitude() - refPoint.getLatitude()) * g_degToRadFactor;
		double dist = ApplicationConstants.EARTH_RADIUS * Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat);
		double orient = Math.atan2(deltaLat, deltaLon);// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
		if (orient < 0)
			orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
		orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
		// now convert to aizmuth - one it is against y axis, it also goes clockwise

		orient = 360 - orient; // do clockwise:
		orient += 90;
		if (orient > 360)
			orient = orient - 360;
		return new Pair<Double, Double>(dist, orient);

	}

	
	public static String getStringForAzimuth(double azimuth) //gets 8 quad direction N, NE, E, SE, S, SW, W, NW
	  {
	//  0	   22.5	N  0 1 
	// 22.5	 67.5	NE 1 3
	// 67.5	112.5	E  3 5
	//112.5	157.5	SE 5 7
	//157.5	202.5	S  7 9
	//202.5	247.5	SW 9 11
	//247.5	292.5	W  11 13
	//292.5	337.5	NW 13 15
	//337.5	360	  N  15 16
	      
	      if (azimuth >= 22.5 && azimuth < 67.5) return "NE";
	      else if (azimuth >= 67.5 && azimuth < 112.5) return "E";
	      else if (azimuth >= 112.5 && azimuth < 157.5) return "SE";
	      else if (azimuth >= 157.5 && azimuth < 202.5) return "S";
	      else if (azimuth >= 202.5 && azimuth < 247.5) return "SW";
	      else if (azimuth >= 247.5 && azimuth < 292.5) return "W";
	      else if (azimuth >= 292.5 && azimuth < 337.5) return "NW";
	      else return "N";
	}
	
	public static void main(String[] args) {
		for (int art=0;art<2;art++) {
			Point p1 = new Point(77.3304,28.6513);
			Point p2 = new Point(77.33,28.6519);
//			Point p1 = new Point(83.536148,22.1253);
//			Point p2 = new Point(83.5360,22.1254);
			Pair<Double,Double> r1 = getSimpleDistAndAizmuth(p1,p2);
			String str = getStringForAzimuth(r1.second);
			p1 = p1.toCartesian();
			p2 = p2.toCartesian();
			double d1 = Math.sqrt(p1.squaredDistance(p2));
			double orient = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
			if (orient < 0)
				orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
			orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
			// now convert to aizmuth - one it is against y axis, it also goes clockwise

			orient = 360 - orient; // do clockwise:
			orient += 90;
			if (orient > 360)
				orient = orient - 360;			
			int dbg =1;
			dbg++;
		}		
		
		for (int art=0;art<2;art++) {
			Point p1 = new Point(83.402000,22.082558);
			Point p2 = new Point(83.405754,22.081755);
			Pair<Double,Double> r1 = getSimpleDistAndAizmuth(p1,p2);
			String str = getStringForAzimuth(r1.second);
			p1 = p1.toCartesian();
			p2 = p2.toCartesian();
			double d1 = Math.sqrt(p1.squaredDistance(p2));
			double orient = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
			if (orient < 0)
				orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
			orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
			// now convert to aizmuth - one it is against y axis, it also goes clockwise

			orient = 360 - orient; // do clockwise:
			orient += 90;
			if (orient > 360)
				orient = orient - 360;			
			int dbg =1;
			dbg++;
		}		
		for (int art=0;art<2;art++) {			
			Point p1 = new Point(83.405754,22.081755);
			Point p2 = new Point(83.410332,22.080090);
			Pair<Double,Double> r1 = getSimpleDistAndAizmuth(p1,p2);
			String str = getStringForAzimuth(r1.second);
			p1 = p1.toCartesian();
			p2 = p2.toCartesian();
			double d1 = Math.sqrt(p1.squaredDistance(p2));
			double orient = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
			if (orient < 0)
				orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
			orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
			// now convert to aizmuth - one it is against y axis, it also goes clockwise

			orient = 360 - orient; // do clockwise:
			orient += 90;
			if (orient > 360)
				orient = orient - 360;			
			int dbg =1;
			dbg++;
		}		
		for (int art=0;art<2;art++) {
			Point p1 = new Point(83.410332,22.080090);
			Point p2 = new Point(83.419685,22.079432);
			Pair<Double,Double> r1 = getSimpleDistAndAizmuth(p1,p2);
			String str = getStringForAzimuth(r1.second);
			p1 = p1.toCartesian();
			p2 = p2.toCartesian();
			double d1 = Math.sqrt(p1.squaredDistance(p2));
			double orient = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
			if (orient < 0)
				orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
			orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
			// now convert to aizmuth - one it is against y axis, it also goes clockwise

			orient = 360 - orient; // do clockwise:
			orient += 90;
			if (orient > 360)
				orient = orient - 360;			
			int dbg =1;
			dbg++;
		}		
		for (int art=0;art<2;art++) {
			Point p1 = new Point(83.419685,22.079432);
			Point p2 = new Point(83.422508,22.080160);
			Pair<Double,Double> r1 = getSimpleDistAndAizmuth(p1,p2);
			String str = getStringForAzimuth(r1.second);
			p1 = p1.toCartesian();
			p2 = p2.toCartesian();
			double d1 = Math.sqrt(p1.squaredDistance(p2));
			double orient = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
			if (orient < 0)
				orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
			orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
			// now convert to aizmuth - one it is against y axis, it also goes clockwise

			orient = 360 - orient; // do clockwise:
			orient += 90;
			if (orient > 360)
				orient = orient - 360;			
			int dbg =1;
			dbg++;
		}		
		for (int art=0;art<2;art++) {
			Point p1 = new Point(83.422508,22.080160);
			Point p2 = new Point(83.430481,22.083654);
			Pair<Double,Double> r1 = getSimpleDistAndAizmuth(p1,p2);
			String str = getStringForAzimuth(r1.second);
			p1 = p1.toCartesian();
			p2 = p2.toCartesian();
			double d1 = Math.sqrt(p1.squaredDistance(p2));
			double orient = Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX());// above the x axis in counter clock is 0 to pi and below the x axis in clockwise direction
			if (orient < 0)
				orient += 2 * Math.PI; // now 0 to 2Pi in counter clockwise
			orient /= g_degToRadFactor;// now 0 to 360 in counter clockwise
			// now convert to aizmuth - one it is against y axis, it also goes clockwise

			orient = 360 - orient; // do clockwise:
			orient += 90;
			if (orient > 360)
				orient = orient - 360;			
			int dbg =1;
			dbg++;
		}		
	}
}
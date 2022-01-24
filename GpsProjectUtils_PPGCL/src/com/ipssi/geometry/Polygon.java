package com.ipssi.geometry;

import java.util.ArrayList;



import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.routemonitor.RouteDef;

public class Polygon {
	public ArrayList<Point> points = new ArrayList<Point>();		
	public double getShortestDistFromEdge(double lon, double lat) {
		double minDist = Double.MAX_VALUE;
		double startLon = 0;
		double startLat = 0;
		if (points.size() > 0) {
			startLon = points.get(0).getLongitude();
			startLat = points.get(0).getLatitude();
			minDist = Point.fastGeoDistance(lon,lat, startLon, startLat);
			for (int i=1,is=points.size();i<is;i++) {
				double endLon = points.get(i == is-1 ? 0 : i).getLongitude();
				double endLat = points.get(i == is-1 ? 0 : i).getLatitude();
				Pair<Double, Double> alphaDist = RouteDef.checkWhereInSegment(lon, lat, startLon, startLat, endLon, endLat);
				double dist = alphaDist.second;
				double d1 = Point.fastGeoDistance(lon, lat, startLon, startLat);
				double d2 = Point.fastGeoDistance(lon, lat, endLon, endLat);
				if (alphaDist.first < -0.0000005 || alphaDist.first > 1.000000005) {
					dist = d1;
				}
				if (d1 < dist)
					dist = d1;
				if (d2 < dist)
					dist = d2;
				if (dist < minDist)
					minDist = dist;
				startLon = endLon;
				startLat = endLat;
			}
		}
		return minDist;
	}
	
	public static double getDistanceFromCenter(double centerLon, double centerLat, double prevLon, double prevLat, double currLon, double currLat) {
		Pair<Double, Double> alphaDist = RouteDef.checkWhereInSegment(centerLon, centerLat, prevLon, prevLat, currLon, currLat);
		return alphaDist.second;
	}
	
	public Polygon(String s){
		formPolygonFromText(s);
	}

	public Polygon(){
		
	}
	
	public void formPolygonFromText(String s){
		s = s.substring(s.indexOf("((")+2);
		while(s.contains(",")){
			String temp = s.substring(0,s.indexOf(','));
			Point p = new Point();
			String xy[] = temp.split(" ");
			p.setX(Double.parseDouble(xy[0]));
			p.setY(Double.parseDouble(xy[1]));
			this.addPoint(p);
			s = s.substring(s.indexOf(',')+1);
		} 
	}
	
	public void addPoint(Point p){
		points.add(p);
	}
	
	public int getSize(){
		return points.size();
	}
	
	public Point getIndex(int index) throws IndexOutOfBoundsException {
			return points.get(index);
	}
	
	public String toWKT(){
		StringBuilder s = new StringBuilder();
		s.append("Polygon((");
		int sz = points.size();
		for (int i=0,is=sz;i<is;i++) {
			if (i != 0)
				s.append(",");
			s.append(points.get(i).getX()).append(" ").append(points.get(i).getY());
		}
		if (sz > 1)
			s.append(",");
		if (sz > 0) {
			s.append(points.get(0).getX()).append(" ").append(points.get(0).getY());
		}
		s.append("))");
		return s.toString();		
	}
	
	public Polygon toCaretesian(){
		Polygon r = new Polygon();
		
		for(int i = 0; i < this.getSize(); i++){
			r.addPoint( (this.getIndex(i)).toCartesian() );
		}
		return r;
	}
	
	public static void main(String a[]){
		Polygon r = new Polygon("Polygon((0 0,1 1,3 0,3 3,3 0,0 0");
		System.out.println(r.toWKT());
		
	}
	
}

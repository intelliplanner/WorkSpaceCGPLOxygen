package com.ipssi.geometry;

import java.io.Serializable;

import com.ipssi.gen.utils.Misc;
import com.ipssi.map.utils.ApplicationConstants;


public class Point implements Serializable{
	private static final long serialVersionUID = 1L;
	public static double g_degToRadFactor = Math.PI/180.0;

	private double x;
	private double y;

	public Point() {
		x = 0.0;
		y = 0.0;
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public double getLongitude() {
		return x;
	}
	
	public double getLatitude() {
		return y;
	}
	
	public void setLatitude( double val) {
		y= val;
	}
	
	public void setLongitude(double val) {
		x=val;
	}
	
	public String toSqlString() {
		return ("Point(" + this.x + " " + this.y + ")");
	}
	
	public Point getDiff(Point rhs) {
		return new Point(x-rhs.x, y-rhs.y);
	}
	
	public void add(Point rhs) {
		x += rhs.getX();
		y += rhs.getY();
	}
	
	public Point toCartesian(){ //TODO check longitude is x, latitude is y
		Point p = new Point();
		p.setX(ApplicationConstants.RADIUS * (Math.sin(Math.toRadians(x))) * (Math.cos(Math.toRadians(y))));
		p.setY(ApplicationConstants.RADIUS * (Math.sin(Math.toRadians(x))) * (Math.sin(Math.toRadians(y))));
		return p;
	}

	public double squaredDistance(Point endPoint) {
		double val = Math.pow(this.x - endPoint.getX(), 2) +  Math.pow(this.y - endPoint.getY(), 2); 
		return val;
	}
	
	public double distance(Point endPoint) {
	    //return Math.sqrt(squaredDistance(endPoint));
		return fastGeoDistance(x,y, endPoint.getX(), endPoint.getY());
	}
	public double fastGeoDistance(Point endPoint) {		  
	      return fastGeoDistance(x, y, endPoint.getLongitude(), endPoint.getLatitude());
	}
	public static double fastGeoDistance(double meLon, double meLat, double lon, double lat) {		  
	      double horizCircleFactor = Math.cos((meLat+lat)/2*g_degToRadFactor);
	      double deltaLon = (lon-meLon)*g_degToRadFactor*horizCircleFactor;
	      double deltaLat = (lat-meLat)*g_degToRadFactor;
	      double dist = ApplicationConstants.RADIUS*Math.sqrt(deltaLon*deltaLon+deltaLat*deltaLat);
	      return dist;
		
	}
	public double fastGeoDistance(double lon, double lat) {
		return fastGeoDistance(getLongitude(), getLatitude(), lon, lat);		
	}
	
	public boolean equals(Point rhs) {
		return rhs != null && Misc.isEqual(y, rhs.getY()) && Misc.isEqual(x, rhs.getX());
	}
	
	public String toString() {
		return "("+x+","+y+")";
	}
}

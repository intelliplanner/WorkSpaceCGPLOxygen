package com.ipssi.RegionTest;

import java.awt.Point;
import java.awt.Polygon;
import java.text.SimpleDateFormat;

public class PolyJunkJava {
	public static void main(String a[]) {
		/*
		 * Point pt = new Point(0,-2); pt.setLocation(0, 1.3); Polygon p = new
		 * Polygon(); p.addPoint(0, 0); p.addPoint(0,1); p.addPoint(3, 3);
		 * p.addPoint(3, 0); p.addPoint(0, 0);
		 * System.out.println(pt.toString()+'\t'+p.contains(pt));
		 */
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy HH:mm");
		java.util.Date date = new java.util.Date((new java.util.Date()).toLocaleString());
		
		//System.out.println(sdf.format(date));
	}
}

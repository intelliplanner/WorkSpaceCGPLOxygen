package com.ipssi.RegionTest;

import com.ipssi.geometry.Point;
import com.ipssi.geometry.Polygon;

public class GeometryTest {
	public static boolean pointInRegion(Point point, String shape) {
		Polygon region = new Polygon(shape);
		return pointInRegion(point,region);
	}
	
	public static boolean pointInRegion(Point point, Polygon region) {
		if (region.getSize() == 0) {
			return false;
		}

		int j = 0;
		boolean inside_flag, xflag0;
		double dv0;
		int crossings = 0;
		boolean yflag0;
		boolean yflag1 = false;
		Point vertex0 = new Point();
		Point vertex1 = new Point();

		vertex0 = region.getIndex(region.getSize() - 1);
		vertex1 = region.getIndex(0);

		yflag0 = (dv0 = vertex0.getY() - point.getY()) >= 0.0;

		crossings = 0;
		try {
			for (j = 0; j < region.getSize(); j++) {
				if (j % 2 == 1) {
					vertex0 = region.getIndex(j);
					yflag0 = (dv0 = vertex0.getY() - point.getY()) >= 0.0;
				} else {
					vertex1 = region.getIndex(j);
					yflag1 = (vertex1.getY() >= point.getY());
				}

				if (yflag0 != yflag1) {
					/* check if points on same side of Y axis */
					if ((xflag0 = (vertex0.getX() >= point.getX())) == (vertex1.getX() >= point.getX())) {

						if (xflag0)
							crossings++;
					} else {
						/*
						 * compute intersection of pgon segment with X ray, note if > point's X.
						 */
						boolean temp = ((vertex0.getX() - dv0 * (vertex1.getX() - vertex0.getX()) / (vertex1.getY() - vertex0.getY())) >= point.getX());
						if (temp) {
							crossings = crossings + 1;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		inside_flag = (crossings % 2 == 1) ? true : false;

		return inside_flag;

	}

}

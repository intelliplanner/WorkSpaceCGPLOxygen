/**
 * 
 */
package com.ipssi.geometry;

import com.ipssi.gen.utils.Misc;
import com.ipssi.geometry.Point;
import com.ipssi.map.utils.ApplicationConstants;

/**
 * @author jai
 * 
 */
public class Region extends Polygon {
	public int id;
	public String m_name;
	public Point m_llCoord;
	public Point m_urCoord;
	public boolean m_isMBRSameAsRegion;
	private Point center = null; //lazy eval to cache
	private int portNodeId = Misc.G_TOP_LEVEL_PORT;
	private int regionType = Misc.getUndefInt();
//	public WKTPolygon m_definition;
	public Region() {
		
	}
    public Region(int id, String name, String shape, Point llCoord, Point urCoord, boolean mbrSame, int portNodeId) {
    	super(shape);
    	this.id = id;
    	this.m_name = name;
    	this.m_llCoord = llCoord;
    	this.m_urCoord = urCoord;
    	this.m_isMBRSameAsRegion = mbrSame;
    	this.portNodeId = portNodeId;
    }
	public double getDiagonal() {
		double deltaY = m_urCoord.getY() - m_llCoord.getY();
		double deltaX = m_urCoord.getY() - m_llCoord.getX();
		return Math.sqrt(deltaY * deltaY + deltaX * deltaX);
	}

	public Point getCentroid() // not needed
	{
		return getCenter(); 
	}
	
	public void shiftTo(Point p) {
		Point gap = p.getDiff(getCenter());
		shiftBy(gap);
	}
	
	public void shiftBy(Point gap) {
		for (int i=0,is=points.size();i<is;i++) {
			points.get(i).add(gap);
		}
		center.add(gap);
		m_llCoord.add(gap);
		m_urCoord.add(gap);
	}
	
	
	public Point getCenter() { //lazy eval
		if (center == null) {
			double x = 0;
			double y = 0;
			for (int i=0,is=points.size();i<is;i++) {
				x += points.get(i).getX();
				y += points.get(i).getY();
			}
			if (points.size() > 0) {
				x = x/((double)points.size());
				y = y/((double)points.size());
			}
			center = new Point(x,y);
			return center;
		}
		else {
			return center;
		}
	}
	
	public static Region getLongLatBoxAround(Point point, double distanceKM) {
		double horizCircleRadius = ApplicationConstants.EARTH_RADIUS * Math.cos(point.getLatitude() / 180.0 * Math.PI);
		// double delta x = r cos theta * delta lon = distance => delta lon = distance/r*cos theta
		// double delta y = r delta lat = distance => delta lat = distance/r
		double deltaLat = distanceKM / ApplicationConstants.EARTH_RADIUS * 180.0 / Math.PI;
		double deltaLon = distanceKM / horizCircleRadius * 180.0 / Math.PI;
        Region retval = new Region();
        
        double lon = point.getLongitude();
        double lat = point.getLatitude();
		
		retval.addPoint(new Point(lon-deltaLon, lat+deltaLat));
		retval.addPoint(new Point(lon+deltaLon, lat+deltaLat));
		retval.addPoint(new Point(lon+deltaLon, lat-deltaLat));
		retval.addPoint(new Point(lon-deltaLon, lat-deltaLat));
		retval.m_llCoord = new Point(lon-deltaLon, lat-deltaLat);
		retval.m_urCoord = new Point(lon+deltaLon, lat+deltaLat);
		retval.m_isMBRSameAsRegion = true;
		
		return retval;
	}
	public int getRegionType() {
		return regionType;
	}
	public void setRegionType(int regionType) {
		this.regionType = regionType;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
}

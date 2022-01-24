package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.RegionTest.GeometryTest;
import com.ipssi.RegionTest.RegionTest;
import com.ipssi.cache.ShiftPlanHistory;
import com.ipssi.cache.ShiftPlanMgmt;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Region;
import com.ipssi.mapguideutils.PrintIdAll;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.miningOpt.SiteStats;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

public class PlanInventoryInfo {
	public static PlanInventoryInfo getPlanInfo(int id) {
		for (int i=0,is=g_cachedPlanList.length;i<is && g_cachedPlanList[i] != null;i++) {
			if (g_cachedPlanList[i].planId == id)
				return g_cachedPlanList[i];
		}
		return null;
	}
	public static PlanInventoryInfo getPlanInfo(Connection conn, int planId, long planTS, ArrayList<Integer> siteList) {
		PlanInventoryInfo retval = getPlanInfo(planId);
		if (retval != null)
			return retval;
		Pair<ArrayList<SiteStats.InvPile>, ArrayList<Region>> analysisResult = PlanInventoryInfo.createVoronoi(conn, siteList);
		retval = new PlanInventoryInfo();
		retval.planId = planId;
		retval.invPiles = analysisResult.first;
		retval.planTS = planTS;
		retval.vornoiRegionPiles = analysisResult.second;
		int lastNonNullIndx = g_cachedPlanList.length-1;
		synchronized (g_cachedPlanList) {
			for (;lastNonNullIndx >= 0;lastNonNullIndx--)
				if (g_cachedPlanList[lastNonNullIndx] != null)
					break;
			if (lastNonNullIndx == g_cachedPlanList.length-1) {
				for (int i=0,is=g_cachedPlanList.length-1;i<is;i++)
					g_cachedPlanList[i] = g_cachedPlanList[i+1];
				lastNonNullIndx = g_cachedPlanList.length-2;
			}
			g_cachedPlanList[lastNonNullIndx+1] = retval;
		}
		return retval;
	}
	public int getIndex(int pileId) {
		for (int i=0,is=this.invPiles.size();i<is;i++)
			if (pileId == this.invPiles.get(i).getId())
				return i;
		return -1;
	}
	
	public static boolean isInInvPile(double lon, double lat, SiteStats.InvPile pile, Region vornoiRegion) throws Exception {
		com.ipssi.geometry.Point pt = new com.ipssi.geometry.Point(lon,lat);
		if (pile != null && pile.getReg() != null && RegionTest.PointIn(null, pt, pile.getReg().region))
			return true;
		if (vornoiRegion != null && RegionTest.PointIn(null, pt, vornoiRegion))
			return true;
		return false;
	}
	public Pair<SiteStats.InvPile, Region> getAppropRegion(double lon, double lat) throws Exception {
		SiteStats.InvPile pile = null;
		Region vornoiRegion = null;
		com.ipssi.geometry.Point pt = new com.ipssi.geometry.Point(lon,lat);
		boolean found = false;
		for (int i=0,is=this.invPiles.size();i<is;i++) {
			SiteStats.InvPile tempPile = this.invPiles.get(i);
			Region tempRegion = this.vornoiRegionPiles.get(i);
			if (tempPile != null && tempPile.getReg() != null && RegionTest.PointIn(null, pt, tempPile.getReg().region)) {
				found = true;
				pile = tempPile;
				vornoiRegion = tempRegion;
				break;
			}
		}
		if (!found) {
			for (int i=0,is=this.invPiles.size();i<is;i++) {
				SiteStats.InvPile tempPile = this.invPiles.get(i);
				Region tempRegion = this.vornoiRegionPiles.get(i);
				if (tempRegion != null && RegionTest.PointIn(null, pt, tempRegion)) {
					found = true;
					pile = tempPile;
					vornoiRegion = tempRegion;
					break;
				}
			}
		}
		if (!found) {
			double minDist = Double.MAX_VALUE;
			for (int i=0,is=this.invPiles.size();i<is;i++) {
				SiteStats.InvPile tempPile = this.invPiles.get(i);
				if (tempPile != null) {
					double plon = tempPile.getLon();
					double plat = tempPile.getLat();
					double d = Point.fastGeoDistance(plon, plat, lon, lat);
					if (d < minDist) {
						minDist = d;
						pile = tempPile;
						vornoiRegion = this.vornoiRegionPiles.get(i);
					}
				}
			}
		}
		return new Pair<SiteStats.InvPile, Region>(pile, vornoiRegion);
	}
	
	private long planTS;
	private int planId;
	private ArrayList<SiteStats.InvPile> invPiles;
	private ArrayList<Region> vornoiRegionPiles;
	private static PlanInventoryInfo g_cachedPlanList[] = new PlanInventoryInfo[5];
	
	public static Pair<ArrayList<SiteStats.InvPile>, ArrayList<Region>> createVoronoi(Connection conn, ArrayList<Integer> invList) {
		
		ArrayList<SiteStats.InvPile> inventoryList = new ArrayList<SiteStats.InvPile>();
		ArrayList<Region> retvalVornoiRegionByInvId = new  ArrayList<Region>();
		
		try {
			ArrayList<Coordinate> ptList = new ArrayList<Coordinate>();
			Envelope envp = null;
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			for (int i=0,is=invList == null ? 0 : invList.size();i<is;i++) {
				int id = invList.get(i);
				SiteStats.InvPile pile = SiteStats.getInvPile(id);
				inventoryList.add(pile);
				if (pile == null) {
					ptList.add(null);
					continue;
				}
				double lon = pile.getLon();
				double lat = pile.getLat();
				
				if (pile.getReg() != null) {
					com.ipssi.geometry.Point p = pile.getReg().region.getCentroid();
					lon = p.getLongitude();
					lat = p.getLatitude();
				}
				Coordinate pt = new Coordinate();
				pt.x = lon;
				pt.y = lat;
				
				ptList.add(pt);
				if (lon > maxX)
					maxX = lon;
				if (lat > maxY)
					maxY = lat;
				if (lon < minX)
					minX = lon;
				if (lat < minY)
					minY = lat;
			}//for each inventory item
			double delta = RTreeUtils.helpConvertKMInDegree(2);
			Coordinate pt1 = new Coordinate();
			pt1.x = minX-delta;
			pt1.y = minY-delta;
			Coordinate pt2 = new Coordinate();
			pt2.x = maxX+delta;
			pt2.y = maxY+delta;
			envp = new Envelope(pt1,pt2);
			Geometry voronoiResult = getVoronoi(ptList, envp);
			ArrayList<Region> vrList = new ArrayList<Region>();
			for (int i=0,is = voronoiResult.getNumGeometries(); i<is;i++) {
				Geometry geom = voronoiResult.getGeometryN(i);
				Coordinate coord = (Coordinate) geom.getUserData();
				com.ipssi.geometry.Region plygn = new com.ipssi.geometry.Region();
				Coordinate coordinates[] = geom.getCoordinates();
				minX = Double.MAX_VALUE;
				minY = Double.MAX_VALUE;
				maxX = Double.MIN_VALUE;
				maxY = Double.MIN_VALUE;
				for (int j=0,js=coordinates.length;j<js;j++) {
					if (coordinates[j].x < minX)
						minX = coordinates[j].x;
					if (coordinates[j].x > maxX)
						maxX = coordinates[j].x;
					if (coordinates[j].y < minY)
						minY = coordinates[j].y;
					if (coordinates[j].y > maxY)
						maxY = coordinates[j].y;
					
					plygn.addPoint(new com.ipssi.geometry.Point(coordinates[j].x, coordinates[j].y));
				}
				plygn.m_llCoord = new com.ipssi.geometry.Point(minX, minY);
				plygn.m_urCoord = new com.ipssi.geometry.Point(maxX, maxY);
				
				vrList.add(plygn);
			}
			for (int i=0,is=ptList.size();i<is;i++) {
				Coordinate c = ptList.get(i);
				if (c == null) {
					retvalVornoiRegionByInvId.add(null);		
					continue;
				}
				double lon = c.x;
				double lat = c.y;
				int idx = -1;
				for (int j=0,js=vrList.size();j<js;j++) {
					Region plygn = vrList.get(j);
					if (RegionTest.PointIn(null, new com.ipssi.geometry.Point(lon,lat), plygn)) {
						idx = j;
						break;
					}
				}
				if (idx < 0)
					retvalVornoiRegionByInvId.add(null);
				else
					retvalVornoiRegionByInvId.add(vrList.get(idx));
			}//for each pt
		}////end of tri
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return new Pair<ArrayList<SiteStats.InvPile>, ArrayList<Region>> (inventoryList,retvalVornoiRegionByInvId);
	}
	
	public static Geometry getVoronoi(ArrayList<Coordinate> ptList, Envelope envp) {
		VoronoiDiagramBuilder vdb = new VoronoiDiagramBuilder();
		vdb.setClipEnvelope(envp);
		vdb.setSites(ptList);
		 GeometryFactory factory = new GeometryFactory();
		return vdb.getDiagram(factory);
	}
}

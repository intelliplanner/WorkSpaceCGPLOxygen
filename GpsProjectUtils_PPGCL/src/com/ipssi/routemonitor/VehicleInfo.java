package com.ipssi.routemonitor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;

public class VehicleInfo {
	public static class GeofencingInfo {
		
	};
	private int vehicleId;
	private RouteDef.PointInfo lastPointInfo = null;
	private long lastGpsRecTime = Misc.getUndefInt();
	private double lastLon = Misc.getUndefDouble();
	private double lastLat = Misc.getUndefDouble();
	private static ConcurrentHashMap<Integer,VehicleInfo> g_vehicleInfos = new ConcurrentHashMap<Integer,VehicleInfo>();
	
	public static void clearLastPointInfo() {
		Collection<VehicleInfo> vehicleInfos = g_vehicleInfos.values();
		for (VehicleInfo v:vehicleInfos) {
			v.lastPointInfo = null;
		}
	}

	public boolean updateCurrRouteSeg(Connection conn, long gpsRecTime, int checkIfGapMoreThanXSec, double lon, double lat) {
		return false;
		/*
		if (Misc.isEqual(lon, lastLon) && Misc.isEqual(lat, lastLat))
			return false;
		if (lastGpsRecTime > 0) {
			int gapSec = (int)((gpsRecTime - lastGpsRecTime)/1000);
			if (gapSec < checkIfGapMoreThanXSec)
				return false;
		}
		boolean changed = false;
		if (lastPointInfo != null && lastPointInfo.getRouteIndex() >= 0 && lastPointInfo.getSegmentIndex() >= 0) {
			//check if we can find it in neibouring segment
			RouteDef rd = RouteDef.getRouteDefByIndex(lastPointInfo.getRouteIndex());
			int segIndex = lastPointInfo.getSegmentIndex();
			if (segIndex < 0)
				segIndex = 0;
			RouteDef.Segment segment = rd.getSegments().get(segIndex);
	
			for (int art=0;art<2;art++) {
			 //art = 0 going forward ... includin current
				
			}
			
		}
		RouteDef.Segment segment = null;
		int where = Misc.getUndefInt();
		if (lastRoute != null && lastSegment >= 0 && lastSegment < lastRoute.getSegments().size()) {
			segment = lastRoute.getSegments().get(lastSegment);
			where = checkWhereInSegment(lon,lat, segment.getStartLon(), segment.getStartLat(), segment.getEndLon(), segment.getEndLat());
			if (where != lastWhere)
				changed = true;
		}
		if (changed || where != 0) {
			//reestimate .
			RouteDef bestRoute = lastRoute != null ? lastRoute : routeDefs.get(0);
			
			if (segment != null && ((lastSegment == 0 && lastWhere < 0) || (lastSegment == lastRoute.getSegments().size()-1 && lastWhere > 0))) {
				//reget the best route - TODO later
				bestRoute = routeDefs.get(0);
			}
		}
		return changed;
		*/
	}
	
	
}

package com.ipssi.dispatchoptimization.vo;

import com.ipssi.gen.utils.Misc;

public class RouteVo {
	public static int DIST_FROM_USER = 100;
	public static int DIST_FROM_GPS_EXACT = 90;
	public static int DIST_FROM_GPS_REG_MATCH = 80;
	public static int SWAG = 20;
	private int routeId;
	private int pitId;
	private LoadSiteVO loadSite;
	private UnloadSiteVo unloadSite;
	private ShovelInfoVo shovel;
	private double distance = Misc.getUndefDouble();
	private int distSrc = SWAG;
	private double difficulty = Misc.getUndefDouble();
	private int routeLevel = Misc.getUndefInt();
	
	public int getPitId() {
		return pitId;
	}
	public void setPitId(int pitId) {
		this.pitId = pitId;
	}
	public int getRouteId() {
		return routeId;
	}
	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	public LoadSiteVO getLoadSite() {
		return loadSite;
	}
	public void setLoadSite(LoadSiteVO loadSite) {
		this.loadSite = loadSite;
	}
	
	public double getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(double difficulty) {
		this.difficulty = difficulty;
	}
	public int getRouteLevel() {
		return routeLevel;
	}
	public void setRouteLevel(int routeLevel) {
		this.routeLevel = routeLevel;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public int getDistSrc() {
		return distSrc;
	}
	public void setDistSrc(int distSrc) {
		this.distSrc = distSrc;
	}
	public UnloadSiteVo getUnloadSite() {
		return unloadSite;
	}
	public void setUnloadSite(UnloadSiteVo unloadSite) {
		this.unloadSite = unloadSite;
	}
	
	
}

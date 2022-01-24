package com.ipssi.dispatchoptimization.vo;

public class LiveAssignmentDTO {
	private int pitId;
	private int routeId;
	private LoadSiteVO loadSite;
	private UnloadSiteVo unloadSite;
	private ShovelInfoVo shovel;
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
	public UnloadSiteVo getUnloadSite() {
		return unloadSite;
	}
	public void setUnloadSite(UnloadSiteVo unloadSite) {
		this.unloadSite = unloadSite;
	}
	public ShovelInfoVo getShovel() {
		return shovel;
	}
	public void setShovel(ShovelInfoVo shovel) {
		this.shovel = shovel;
	}
	
	
}

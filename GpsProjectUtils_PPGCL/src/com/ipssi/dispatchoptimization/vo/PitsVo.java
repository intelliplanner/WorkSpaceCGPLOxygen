package com.ipssi.dispatchoptimization.vo;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class PitsVo {
	private int id;
	private String name;
	private ArrayList<RouteVo> routes = new ArrayList<RouteVo>();
	
	public PitsVo(int id, String name) {
		this.id = id;
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<RouteVo> getRoutes() {
		return routes;
	}
	public void setRoutes(ArrayList<RouteVo> routes) {
		this.routes = routes;
	}
	public void addRoute(RouteVo route) {
		this.routes.add(route);
	}
	public void delRoute(RouteVo route) {
		this.routes.remove(route);
	}
}

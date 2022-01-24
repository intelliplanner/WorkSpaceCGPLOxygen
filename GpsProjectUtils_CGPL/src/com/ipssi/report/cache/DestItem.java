package com.ipssi.report.cache;

import java.sql.Connection;

public class DestItem {
	private int id;
	private int type;
	private String name; //eventually to be replaced
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getName(Connection conn) { //for future
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DestItem(int id, int type, String name) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
	}
}

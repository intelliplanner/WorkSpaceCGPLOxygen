package com.ipssi.reporting.customize;

import java.util.ArrayList;

public class DBConfig {
	private int id;
	private int forPortNodeId;
	private int forUserId;
	private String tag;
	private ArrayList<DBItem> dbItems;
	public boolean isEmpty() { 
		return dbItems == null || dbItems.size() == 0;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getForPortNodeId() {
		return forPortNodeId;
	}
	public void setForPortNodeId(int forPortNodeId) {
		this.forPortNodeId = forPortNodeId;
	}
	public int getForUserId() {
		return forUserId;
	}
	public void setForUserId(int forUserId) {
		this.forUserId = forUserId;
	}
	public ArrayList<DBItem> getDbItems() {
		return dbItems;
	}
	public void setDbItems(ArrayList<DBItem> dbItems) {
		this.dbItems = dbItems;
	}
	
}

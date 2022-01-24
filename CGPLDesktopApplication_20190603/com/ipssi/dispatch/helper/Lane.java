package com.ipssi.dispatch.helper;

import java.util.ArrayList;

public class Lane {
	private int id;
	private Icon source;
	private Icon destination;
	private ArrayList<Icon> vehicleList;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Icon getSource() {
		return source;
	}
	public void setSource(Icon source) {
		this.source = source;
	}
	public Icon getDestination() {
		return destination;
	}
	public void setDestination(Icon destination) {
		this.destination = destination;
	}
	public ArrayList<Icon> getVehicleList() {
		return vehicleList;
	}
	public void setVehicleList(ArrayList<Icon> vehicleList) {
		this.vehicleList = vehicleList;
	}
	
	
	
}

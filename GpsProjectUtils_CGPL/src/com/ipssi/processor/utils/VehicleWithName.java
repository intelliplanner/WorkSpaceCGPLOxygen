package com.ipssi.processor.utils;

public class VehicleWithName extends Vehicle {
	private String name = null;
	public VehicleWithName(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}

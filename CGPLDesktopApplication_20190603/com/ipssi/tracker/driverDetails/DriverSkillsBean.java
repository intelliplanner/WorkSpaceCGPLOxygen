package com.ipssi.tracker.driverDetails;

public class DriverSkillsBean {
	
	private int id;
	private int key;
	private int value;
	private String valueName;
	
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getKey(){
		return this.key;
	}
	public void setKey(int key){
		this.key = key;
	}
	public int getValue(){
		return this.value;
	}
	public void setValue(int value){
		this.value = value;
	}
	public String getValueName(){
		return this.valueName;
	}
	public void setValueName(String valueName){
		this.valueName = valueName;
	}
	
}

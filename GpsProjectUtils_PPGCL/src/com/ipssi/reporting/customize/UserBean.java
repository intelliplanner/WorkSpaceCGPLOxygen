package com.ipssi.reporting.customize;


public class UserBean{
	private String name;
	private int id;	
	private int orgId;
	private String orgName;
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return this.name;
	}
	public void setOrgName(String orgName){
		this.orgName = orgName;
	}
	public String getOrgName(){
		return this.orgName;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getId(){
		return this.id;
	}
	public void setOrgId(int orgId){
		this.orgId = orgId;
	}
	public int getOrgId(){
		return this.orgId;
	}
}

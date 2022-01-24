package com.ipssi.dispatchoptimization.vo;

public class Material {
	private int id;
	private String name;
	private String materialCat;
	private double percentage;
	private double sg;
	public Material(int id, String name, String matetrialCat, double percentage, double sg) {
		this.id = id;
		this.name = name;
		this.materialCat = materialCat;
		this.percentage = percentage;
		this.sg = sg;
		
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMaterialCat() {
		return materialCat;
	}
	public void setMaterialCat(String materialCat) {
		this.materialCat = materialCat;
	}
	public double getPercentage() {
		return percentage;
	}
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getSg() {
		return sg;
	}
	public void setSg(double sg) {
		this.sg = sg;
	}
	 
}

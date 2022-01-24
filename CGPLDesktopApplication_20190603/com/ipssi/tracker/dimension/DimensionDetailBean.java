/**
 * 
 */
package com.ipssi.tracker.dimension;

import java.util.HashMap;

/**
 * @author jai
 *
 */
public class DimensionDetailBean {
	private int id;
	
	private int portNodeId ;
	private String name;
	private String description;
	private HashMap<Double , Double> typeMap;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DimensionDetailBean(){
		typeMap = new HashMap<Double, Double>();
	}

	public int getPortNodeId() {
		return portNodeId;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public HashMap<Double, Double> getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(HashMap<Double, Double> typeMap) {
		this.typeMap = typeMap;
	}
	
	public void addToTypeMap(double reading,double value){
		this.typeMap.put(reading, value);
	}
	
}

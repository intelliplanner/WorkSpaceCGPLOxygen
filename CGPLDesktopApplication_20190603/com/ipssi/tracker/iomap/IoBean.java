package com.ipssi.tracker.iomap;

import java.util.HashMap;

import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class IoBean {
	private String name;
	private int ioMapInfoId;
	private int ioId;
	private int attributeId;
	private String description;
	private int id;
	private int status;
	private String modelName;
	private int deviceModelInfoId;
	private int organization;
	private HashMap<Integer,Integer> ioAttributeMap = new HashMap<Integer,Integer>();
	private HashMap<Integer,Triple<Integer, Double, Double>> attributeDimensionMap = new HashMap<Integer, Triple<Integer, Double, Double>>();
	private HashMap<Integer,Pair<Integer,Integer>> transientandvalidonpowerattributeDimensionMap = new HashMap<Integer,Pair<Integer,Integer>>();
	
	public void addToAttributeDimensionMap(int io, Triple<Integer, Double, Double> dimension){
		this.attributeDimensionMap.put(io, dimension);
	}
	
	public HashMap<Integer, Triple<Integer, Double, Double>> getAttributeDimensionMap() {
		return this.attributeDimensionMap;
	}

	public HashMap<Integer, Pair<Integer, Integer>> getTransientandvalidonpowerattributeDimensionMap() {
		return this.transientandvalidonpowerattributeDimensionMap;
	}

	public void setTransientandvalidonpowerattributeDimensionMap(HashMap<Integer, Pair<Integer, Integer>> transientandvalidonpowerattributeDimensionMap) {
		this.transientandvalidonpowerattributeDimensionMap = transientandvalidonpowerattributeDimensionMap;
	}
	
	public void addToTransientandvalidonpowerDimensionMap(int io, Pair<Integer, Integer> pair){
		this.transientandvalidonpowerattributeDimensionMap.put(io, pair);
	}

	
	public void setAttributeDimensionMap(HashMap<Integer, Triple<Integer, Double, Double>> attributeDimensionMap) {
		this.attributeDimensionMap = attributeDimensionMap;
	}

	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getOrganization(){
		return this.organization;
	}
	
	public void setOrganization(int organization){
		this.organization = organization;
	}
	public String getModelName(){
		return this.modelName;
	}
	
	public void setModelName(String modelName){
		this.modelName = modelName;
	}
	
	public int getIoMapInfoId(){
		return this.ioMapInfoId;
	}
	
	public void setIoMapInfoId(int ioMapInfoId){
		this.ioMapInfoId = ioMapInfoId;  
	}
	
	public int getIoId(){
		return this.ioId;
	}
	
	public void setIoId(int ioId){
		this.ioId = ioId;
	}
	
	public int getAttributeId(){
		return attributeId;
				
	}
	
	public void setAttributeId(int attributeId){
		this.attributeId = attributeId;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getStatus(){
		return this.status;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public int getDeviceModelInfoId(){
		return this.deviceModelInfoId;
	}
	
	public void setDeviceModelInfoId(int deviceModelInfoId){
		this.deviceModelInfoId = deviceModelInfoId;
	}
	
	public HashMap<Integer,Integer> getIoAttributeMap(){
		return this.ioAttributeMap;
	}
	
	public int getIoAttributevalur(int ioId){
		return ((this.ioAttributeMap).get(new Integer(ioId)));
	}
	
	public void setIoAttributeValue(int ioId, int attributeId){
		(this.ioAttributeMap).put(ioId, attributeId);
	}
}

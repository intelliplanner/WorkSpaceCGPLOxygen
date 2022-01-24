package com.ipssi.tracker.jrm;

import com.ipssi.geometry.Point;

public class JRMBean {

	
	
	private String name;
	private String geometery;
	private Point lowerPoint;
	private Point upperPoint;
	private int portNodeId;
	private String description;
	private int regionType;
	
	
	private int id;
	private int landmark_region_seg_id;
	private int category;
	private int risk_level;
	private int landmark_type;
	private int start_hour;
	private int start_min;
	private int end_hour;
	private int end_min;
	private int status;
	private String category_type;
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getGeometery() {
		return geometery;
	}
	public void setGeometery(String geometery) {
		this.geometery = geometery;
	}
	public Point getLowerPoint() {
		return lowerPoint;
	}
	public void setLowerPoint(Point lowerPoint) {
		this.lowerPoint = lowerPoint;
	}
	public Point getUpperPoint() {
		return upperPoint;
	}
	public void setUpperPoint(Point upperPoint) {
		this.upperPoint = upperPoint;
	}
	public int getRegionType() {
		return regionType;
	}
	public void setRegionType(int regionType) {
		this.regionType = regionType;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLandmark_region_seg_id() {
		return landmark_region_seg_id;
	}

	public void setLandmark_region_seg_id(int landmark_region_seg_id) {
		this.landmark_region_seg_id = landmark_region_seg_id;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getRisk_level() {
		return risk_level;
	}

	public void setRisk_level(int risk_level) {
		this.risk_level = risk_level;
	}

	public int getLandmark_type() {
		return landmark_type;
	}

	public void setLandmark_type(int landmark_type) {
		this.landmark_type = landmark_type;
	}

	public int getStart_hour() {
		return start_hour;
	}

	public void setStart_hour(int start_hour) {
		this.start_hour = start_hour;
	}

	public int getStart_min() {
		return start_min;
	}

	public void setStart_min(int start_min) {
		this.start_min = start_min;
	}

	public int getEnd_hour() {
		return end_hour;
	}

	public void setEnd_hour(int end_hour) {
		this.end_hour = end_hour;
	}

	public int getEnd_min() {
		return end_min;
	}

	public void setEnd_min(int end_min) {
		this.end_min = end_min;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCategory_type() {
		return category_type;
	}

	public void setCategory_type(String category_type) {
		this.category_type = category_type;
	}

}

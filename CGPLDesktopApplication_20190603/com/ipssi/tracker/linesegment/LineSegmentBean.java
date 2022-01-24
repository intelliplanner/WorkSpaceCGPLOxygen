package com.ipssi.tracker.linesegment;

import com.ipssi.geometry.Point;

public class LineSegmentBean {
	private int id;
	private String name;
	private String geometery;
	private Point lowerPoint;
	private Point upperPoint;
	private String description;
	private int stateId;
	private String districtName;
	private String alignWith;
	private int associatedWith;
	private String roadName;
	private int roadId;
	private int lineSegmentOrder;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getStateId() {
		return stateId;
	}
	public void setStateId(int stateId) {
		this.stateId = stateId;
	}
	public String getDistrictName() {
		return districtName;
	}
	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}
	public String getAlignWith() {
		return alignWith;
	}
	public void setAlignWith(String alignWith) {
		this.alignWith = alignWith;
	}
	public int getAssociatedWith() {
		return associatedWith;
	}
	public void setAssociatedWith(int associatedWith) {
		this.associatedWith = associatedWith;
	}
	public String getRoadName() {
		return roadName;
	}
	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}
	public int getLineSegmentOrder() {
		return lineSegmentOrder;
	}
	public void setLineSegmentOrder(int lineSegmentOrder) {
		this.lineSegmentOrder = lineSegmentOrder;
	}
	public int getRoadId() {
		return roadId;
	}
	public void setRoadId(int roadId) {
		this.roadId = roadId;
	}
	
	

}

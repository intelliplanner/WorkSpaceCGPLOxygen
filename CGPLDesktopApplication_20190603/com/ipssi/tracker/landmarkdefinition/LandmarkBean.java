/**
 * 
 */
package com.ipssi.tracker.landmarkdefinition;

import com.ipssi.geometry.Point ;

/**
 * @author jai
 *
 */
public class LandmarkBean {
	private int id;
	private String name;
	private String geometery;
	private Point lowerPoint;
	private Point upperPoint;
	private int portNodeId;
	private String description;
	private String userDescription;
	private String destCode;
	private int landmarkType; 
	private Point point;
	
	//added by Balwant
	private String stateName;
	private String distName;
	
	
	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	public String getDistName() {
		return distName;
	}
	public void setDistName(String distName) {
		this.distName = distName;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUserDescription() {
		return userDescription;
	}
	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}
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
	public void setPoint(Point point) {
		this.point = point;
	}
	public Point getPoint() {
		return point;
	}
	public String getDestCode() {
		return destCode;
	}
	public void setDestCode(String destCode) {
		this.destCode = destCode;
	}
	public int getLandmarkType() {
		return landmarkType;
	}
	public void setLandmarkType(int landmarkType) {
		this.landmarkType = landmarkType;
	}
	
	
}

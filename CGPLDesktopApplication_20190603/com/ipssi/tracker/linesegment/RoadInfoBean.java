package com.ipssi.tracker.linesegment;

import java.util.ArrayList;

public class RoadInfoBean {

	private int roadId;
	private String roadName;
	private ArrayList<Integer> lineSegmentId;
	public int getRoadId() {
		return roadId;
	}
	public void setRoadId(int roadId) {
		this.roadId = roadId;
	}
	public String getRoadName() {
		return roadName;
	}
	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}
	public ArrayList<Integer> getLineSegmentId() {
		return lineSegmentId;
	}
	public void setLineSegmentId(ArrayList<Integer> lineSegmentId) {
		this.lineSegmentId = lineSegmentId;
	}
	
	
}

package com.ipssi.geometry;

public class LineSegment {
	private Point start ;
	private Point end ;
	
	public LineSegment(){
		
	}
	
	public LineSegment(Point start, Point end){
		this.start = start;
		this.end = end;
	}
	
	public LineSegment(double startX, double startY, double endX, double endY){
		this.start = new Point(startX,startY);
		this.end = new Point(endX,endY);
	}
	
	public Point startPoint(){
		return this.start;
	}
	
	public Point endPoint(){
		return this.end;
	}
}

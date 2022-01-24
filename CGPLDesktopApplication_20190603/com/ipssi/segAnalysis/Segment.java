package com.ipssi.segAnalysis;

import java.util.Comparator;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.routemonitor.RouteDef;

public class Segment {
	int id;
	MiscInner.PairDouble start = null;
	MiscInner.PairDouble end = null;
	long startTs = Misc.getUndefInt();
	long endTs = Misc.getUndefInt();
	String startName = null;
	String endName = null;
	public String toString() {
		return start + "->" + end;
	}
	public Segment(int id, double stLon, double stLat, double enLon, double enLat, String startName, String endName) {
		this.id = id;
		this.start = new MiscInner.PairDouble(stLon, stLat);
		this.end = new MiscInner.PairDouble(enLon, enLat);
		this.startName = startName;
		this.endName = endName;
	}

	public Segment(int id, double stLon, double stLat, double enLon, double enLat, long stTs, long enTs, String startName, String endName) {
		this.id = id;
		this.start = new MiscInner.PairDouble(stLon, stLat);
		this.end = new MiscInner.PairDouble(enLon, enLat);
		this.startTs = stTs;
		this.endTs = enTs;
		this.startName = startName;
		this.endName = endName;
	}
	
	public static void updateMinMaxBound(MiscInner.PairDouble min, MiscInner.PairDouble max, MiscInner.PairDouble forPt) {
		if (min.first > forPt.first)
			min.first = forPt.first;
		if (min.second > forPt.second)
			min.second = forPt.second;
		if (max.first < forPt.first)
			max.first = forPt.first;
		if (max.second < forPt.second)
			max.second = forPt.second;
	}
	public  void updateMinMaxBound(MiscInner.PairDouble min, MiscInner.PairDouble max) {
		updateMinMaxBound(min, max, start);
		updateMinMaxBound(min,max,end);		
	}
	public static double getMinDistanceOfPtToSeg(MiscInner.PairDouble start1, MiscInner.PairDouble start2, MiscInner.PairDouble end2) {
		double min = Double.MAX_VALUE;
		Pair<Double, Double> pt1StLoc = RouteDef.checkWhereInSegment(start1.first, start1.second, start2.first, start2.second, end2.first, end2.second);
		if (pt1StLoc.first >= 0 && pt1StLoc.first <= 1) {
			double dinDeg = RTreeUtils.helpConvertKMInDegree(pt1StLoc.second);
			if (dinDeg < min)
				min = dinDeg;
		}
		{
			double dx = start1.first-start2.first;
			double dy = start1.second-start2.second;
			double d = Math.sqrt(dx*dx+dy*dy);
			if (d < min)
				min = d;
			dx = start1.first-end2.first;
			dy = start1.second-end2.second;
			d = Math.sqrt(dx*dx+dy*dy);
			if (d < min)
				min = d;
		}
		return min;
	}
	public double getMinDistanceBetweenSeg(Segment rhs) {
		return getMinDistanceBetweenSeg(this.start, this.end, rhs.start, rhs.end);
	}
	public boolean sameDir(Segment rhs) {
		double angle = Math.abs(angleBetween(rhs) * 180/Math.PI);
		
		if (angle > 90)
			angle = 180-angle;
		return Math.abs(angle) < 30;
	}
	public double angleBetween(Segment rhs) {
		double mdx = this.start.first - this.end.first;
		double mdy = this.start.second - this.end.second;
		double rdx = rhs.start.first - rhs.end.first;
		double rdy = rhs.start.second - rhs.end.second;
		double msz = Math.sqrt(mdx*mdx+mdy*mdy);
		double rsz = Math.sqrt(rdx*rdx+rdy*rdy);
		double dot = mdx*rdx+mdy*rdy;
		double rat = dot/(msz*rsz);
		return Math.acos(rat);
		
	}
	public static double getMinDistanceBetweenSeg(MiscInner.PairDouble start1, MiscInner.PairDouble end1, MiscInner.PairDouble start2, MiscInner.PairDouble end2) {
		//
		double d1 = getMinDistanceOfPtToSeg(start1, start2, end2);
		double d2 = getMinDistanceOfPtToSeg(end1, start2, end2);
		double d3 = getMinDistanceOfPtToSeg(start2, start1, end1);
		double d4 = getMinDistanceOfPtToSeg(end2, start1, end1);
		double retval = d1;
		if (d2<retval)
			retval = d2;
		if (d3 < retval)
			retval = d3;
		if (d4 < retval)
			retval  = d4;
		return retval;
	}
}

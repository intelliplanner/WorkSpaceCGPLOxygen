package com.ipssi.common.ds.trip;

import java.io.Serializable;

import com.ipssi.cache.DirResult;
import com.ipssi.gen.utils.Misc;
import com.ipssi.geometry.Point;

public class LUItemAddnl { //implements Serializable {
	//private static final long serialVersionUID = 1L;
	private double exitLon = Misc.getUndefDouble();
	private double exitLat = Misc.getUndefDouble();
	private int relEntryIndex = -1; //relative to index of the point ... actually it is - of this value 
	private int relExitIndex = -1;
	private double lonBound = Misc.getUndefDouble();
	private double latBound = Misc.getUndefDouble();
	private int seqIndex = -1;
	private int stopStartItemIndex = -1;
	public LUItemAddnl(double exitLon, double exitLat, int relEntryIndex, int relExitIndex, double lonBound, double latBound, int seqIndex, int stopStartItemIndex) {
		super();
		this.exitLon = exitLon;
		this.exitLat = exitLat;
		this.relEntryIndex = relEntryIndex;
		this.relExitIndex = relExitIndex;
		this.lonBound = lonBound;
		this.latBound = latBound;
		this.stopStartItemIndex = stopStartItemIndex;
		this.seqIndex = seqIndex;
	}
	
	public void populateFromDirResult(DirResult dirResult, Point box) {
		relEntryIndex = (dirResult.getPrevHiEntryIndex());
		relExitIndex = (dirResult.getPrevHiExitIndex());
		if (dirResult.getHiAvgInOutPoint() != null) {
			exitLon = (dirResult.getHiAvgInOutPoint().getLongitude());
			exitLat = (dirResult.getHiAvgInOutPoint().getLatitude());
		}
		else {
			exitLon = Misc.getUndefDouble();
			exitLat = Misc.getUndefDouble();
		}
		lonBound = (box.getLongitude());
		latBound = (box.getLatitude());	
	}
	public DirResult populateToDirResult() {
		DirResult dirResult = new DirResult(Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble(),
				Misc.getUndefDouble(), null, null, relEntryIndex, relExitIndex, false, false);
		return dirResult;
	}
	
	public double getExitLon() {
		return exitLon;
	}
	public void setExitLon(double exitLon) {
		this.exitLon = exitLon;
	}
	public double getExitLat() {
		return exitLat;
	}
	public void setExitLat(double exitLat) {
		this.exitLat = exitLat;
	}
	public int getRelEntryIndex() {
		return relEntryIndex;
	}
	public void setRelEntryIndex(int relEntryIndex) {
		this.relEntryIndex = relEntryIndex;
	}
	public int getRelExitIndex() {
		return relExitIndex;
	}
	public void setRelExitIndex(int relExitIndex) {
		this.relExitIndex = relExitIndex;
	}
	public double getLonBound() {
		return lonBound;
	}
	public void setLonBound(double lonBound) {
		this.lonBound = lonBound;
	}
	public double getLatBound() {
		return latBound;
	}
	public void setLatBound(double latBound) {
		this.latBound = latBound;
	}
	public int getSeqIndex() {
		return seqIndex;
	}
	public void setSeqIndex(int seqIndex) {
		this.seqIndex = seqIndex;
	}
	public int getStopStartItemIndex() {
		return stopStartItemIndex;
	}
	public void setStopStartItemIndex(int stopStartItemIndex) {
		this.stopStartItemIndex = stopStartItemIndex;
	}
	
}

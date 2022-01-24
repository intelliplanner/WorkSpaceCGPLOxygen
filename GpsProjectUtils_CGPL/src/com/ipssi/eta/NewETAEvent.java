package com.ipssi.eta;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import com.ipssi.gen.utils.Misc;

public class NewETAEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int ofIndex = Misc.getUndefInt(); //-1 => of src, -2 of dest, >= 0 of intermediate waypoints
	private long inTime = Misc.getUndefInt();
	private long outTime = Misc.getUndefInt();
	public String toString() {
		SimpleDateFormat indep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return toString(indep);
	}
	public String toString(SimpleDateFormat indep) {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(ofIndex).append(",").append(inTime <= 0 ? "" : indep.format(new java.util.Date(inTime))).append(",").append(outTime <= 0 ? "" : indep.format(new java.util.Date(outTime))).append("]");
		return sb.toString();
	}
	public NewETAEvent(int ofIndex, long inTime, long outTime) {
		this.ofIndex = ofIndex;
		this.inTime = inTime;
		this.outTime = outTime;
	}
	public int getOfIndex() {
		return ofIndex;
	}
	public void setOfIndex(int ofIndex) {
		this.ofIndex = ofIndex;
	}
	public long getInTime() {
		return inTime;
	}
	public void setInTime(long inTime) {
		this.inTime = inTime;
	}
	public long getOutTime() {
		return outTime;
	}
	public void setOutTime(long outTime) {
		this.outTime = outTime;
	}
}

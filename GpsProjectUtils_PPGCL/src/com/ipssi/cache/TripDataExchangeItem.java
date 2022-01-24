package com.ipssi.cache;

import com.ipssi.gen.utils.Misc;

public class TripDataExchangeItem implements Comparable {
	private int tripId = Misc.getUndefInt();
	private int vehicleId = Misc.getUndefInt();
	private long lgin = Misc.getUndefInt();
	private long lgout = Misc.getUndefInt();
	private long ugin = Misc.getUndefInt();
	private long ugout = Misc.getUndefInt();
	private int lopid = Misc.getUndefInt();
	private int lopType = Misc.getUndefInt();
	private int lopSubtype = Misc.getUndefInt();
	private String lopName = null;
	private int uopid = Misc.getUndefInt();
	private int uopType = Misc.getUndefInt();
	private int uopSubtype = Misc.getUndefInt();
	private String uopName = null;
	private byte processedBy = 0; //0x0 by RP, 
	private boolean deleted = false;
	public TripDataExchangeItem(){
		
	}
	public TripDataExchangeItem(int vehicleId,long loadGateIn){
		this.vehicleId = vehicleId;
		this.lgin = loadGateIn;
	}
	
	public boolean equals(Object r) {
		if (r == null)
			return false;
		TripDataExchangeItem rhs = (TripDataExchangeItem) r;
		return lgin == rhs.lgin && lgout == rhs.lgout && ugin == rhs.ugin && ugout == rhs.ugout
		 && lopid == rhs.lopid && uopid == rhs.uopid 
		 && deleted == rhs.deleted
		 ;
	}
	public long getEarliestTime() {
		return lgin > 0 ? lgin : lgout > 0 ? lgout : ugin > 0 ? ugin : ugout;
	}
	public long getLatestTime() {
		return ugout > 0 ? ugout : ugin > 0 ? ugin : lgout > 0 ? lgout : lgin;
	}
	public boolean isRPComp() {
		return (processedBy & 0x1) > 0;
	}
	public void setRPComp() {
		processedBy |=  0x1;
	}
	
	public int compareTo(Object r) {
		if (r != null) {
			TripDataExchangeItem rhs = (TripDataExchangeItem) r;
			long meTime = this.getEarliestTime();
			long rhsTime = this.getEarliestTime();
			long retval = meTime - rhsTime;
			if (retval == 0)
				if (deleted != rhs.isDeleted()) {
					retval = deleted ? 0 : 1;
				}
			return retval < 0 ? -1 : retval > 0 ? 1 : 0;
		}
		return 1;
	}
	
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public long getLgin() {
		return lgin;
	}
	public void setLgin(long lgin) {
		this.lgin = lgin;
	}
	public long getLgout() {
		return lgout;
	}
	public void setLgout(long lgout) {
		this.lgout = lgout;
	}
	public long getUgin() {
		return ugin;
	}
	public void setUgin(long ugin) {
		this.ugin = ugin;
	}
	public long getUgout() {
		return ugout;
	}
	public void setUgout(long ugout) {
		this.ugout = ugout;
	}
	public byte getProcessedBy() {
		return processedBy;
	}
	public void setProcessedBy(byte processedBy) {
		this.processedBy = processedBy;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public int getLopid() {
		return lopid;
	}
	public void setLopid(int lopid) {
		this.lopid = lopid;
	}
	public int getLopType() {
		return lopType;
	}
	public void setLopType(int lopType) {
		this.lopType = lopType;
	}
	public int getLopSubtype() {
		return lopSubtype;
	}
	public void setLopSubtype(int lopSubtype) {
		this.lopSubtype = lopSubtype;
	}
	public String getLopName() {
		return lopName;
	}
	public void setLopName(String lopName) {
		this.lopName = lopName;
	}
	public int getUopid() {
		return uopid;
	}
	public void setUopid(int uopid) {
		this.uopid = uopid;
	}
	public int getUopType() {
		return uopType;
	}
	public void setUopType(int uopType) {
		this.uopType = uopType;
	}
	public int getUopSubtype() {
		return uopSubtype;
	}
	public void setUopSubtype(int uopSubtype) {
		this.uopSubtype = uopSubtype;
	}
	public String getUopName() {
		return uopName;
	}
	public void setUopName(String uopName) {
		this.uopName = uopName;
	}
	
	
}

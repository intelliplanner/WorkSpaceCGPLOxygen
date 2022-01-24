package com.ipssi.common.ds.trip;

import java.io.Serializable;

import com.ipssi.gen.utils.Misc;

public class TripStat implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int EVENT_NONE = 0;
	public static final int EVENT_NEAR_SHOVEL = 1;
	public static final int EVENT_LOAD_CYCLE = 2;
	public static final int EVENT_LOAD_COMPLETE_WAITING = 3;
	public static final int EVENT_LOAD_COMPLETE_MOVING = 4;
	public static final int EVENT_UNLOAD_INSIDE = 5;
	public static final int EVENT_UNLOAD_START = 6;
	public static final int EVENT_UNLOAD_END_WAITING = 7;
	public static final int EVENT_UNLOAD_COMPLETE_MOVING = 8;
	int shovelId = Misc.getUndefInt();
	
	int destSiteId = Misc.getUndefInt();
	double lead = Misc.getUndefDouble();
	long lwin;
	long lgin;
	long lgout;
	long lwout;
	int numCycles;
	int avgCycleSec;
	int avgCycleExclMinMaxSec;
	int loadQuality = 0; //See ShovelSequence
	boolean srcOfCycleFromStrike;
	boolean srcUnloadFromDala;
	int unloadOpId = Misc.getUndefInt();
	long uwin;
	long ugin;
	long ugout;
	long uwout;
	long latestEvent;
	int prevEventCode = EVENT_NONE;//See static final above ..
	int invPileId = Misc.getUndefInt();
	int dumperId = Misc.getUndefInt();
	long adjlgin;	
	long adjlgout;
	int prevShovelId = Misc.getUndefInt();
	int tripId = Misc.getUndefInt();
	boolean hasStopEnded = true;
	boolean facedCongestion = false; 
	byte inShovelSeq = 1;
	public int getInvPileId() {
		return invPileId;
	}
	public void setInvPileId(int invPileId) {
		this.invPileId = invPileId;
	}
	public int getShovelId() {
		return shovelId;
	}
	public void setShovelId(int shovelId) {
		this.shovelId = shovelId;
	}
	public long getLwin() {
		return lwin;
	}
	public void setLwin(long lwin) {
		this.lwin = lwin;
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
	public long getLwout() {
		return lwout;
	}
	public void setLwout(long lwout) {
		this.lwout = lwout;
	}
	public int getNumCycles() {
		return numCycles;
	}
	public void setNumCycles(int numCycles) {
		this.numCycles = numCycles;
	}
	public int getAvgCycleSec() {
		return avgCycleSec;
	}
	public void setAvgCycleSec(int avgCycleSec) {
		this.avgCycleSec = avgCycleSec;
	}
	public int getAvgCycleExclMinMaxSec() {
		return avgCycleExclMinMaxSec;
	}
	public void setAvgCycleExclMinMaxSec(int avgCycleExclMinMaxSec) {
		this.avgCycleExclMinMaxSec = avgCycleExclMinMaxSec;
	}
	public int getLoadQuality() {
		return loadQuality;
	}
	public void setLoadQuality(int loadQuality) {
		this.loadQuality = loadQuality;
	}
	public boolean isSrcOfCycleFromStrike() {
		return srcOfCycleFromStrike;
	}
	public void setSrcOfCycleFromStrike(boolean srcOfCycleFromStrike) {
		this.srcOfCycleFromStrike = srcOfCycleFromStrike;
	}
	public boolean isSrcUnloadFromDala() {
		return srcUnloadFromDala;
	}
	public void setSrcUnloadFromDala(boolean srcUnloadFromDala) {
		this.srcUnloadFromDala = srcUnloadFromDala;
	}
	public int getUnloadOpId() {
		return unloadOpId;
	}
	public void setUnloadOpId(int unloadOpId) {
		this.unloadOpId = unloadOpId;
	}
	public long getUwin() {
		return uwin;
	}
	public void setUwin(long uwin) {
		this.uwin = uwin;
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
	public long getUwout() {
		return uwout;
	}
	public void setUwout(long uwout) {
		this.uwout = uwout;
	}
	
	public int getPrevEventCode() {
		return prevEventCode;
	}
	public void setPrevEventCode(int prevEventCode) {
		this.prevEventCode = prevEventCode;
	}
	public int getCalcLatestEvent() {
		return this.getUwout() > 0 ? TripStat.EVENT_UNLOAD_COMPLETE_MOVING : this.getUgout() > 0 ? TripStat.EVENT_UNLOAD_END_WAITING
				: this.getUgin() > 0 ? TripStat.EVENT_UNLOAD_START : this.getUwin() > 0 ? TripStat.EVENT_UNLOAD_INSIDE
				: this.getLwout() > 0 ? TripStat.EVENT_LOAD_COMPLETE_MOVING : this.getLgout() > 0 ? TripStat.EVENT_LOAD_COMPLETE_WAITING
				: this.getLgin() > 0 ? TripStat.EVENT_LOAD_CYCLE
				: this.getLwin() > 0 ? TripStat.EVENT_NEAR_SHOVEL
				: TripStat.EVENT_NONE
				;
						
	}
	public int getDumperId() {
		return dumperId;
	}
	public void setDumperId(int dumperId) {
		this.dumperId = dumperId;
	}
	public long getAdjlgin() {
		//if (adjlgin > 0) {
			return adjlgin;
		/*} else {
			return lgin;
		}*/
	}
	public void setAdjlgin(long adjlgin) {
		this.adjlgin = adjlgin;
	}
	public long getAdjlgout() {
		//if (adjlgout > 0) {
			return adjlgout;
		/*} else {
			return lgout;
		}*/
	}
	public void setAdjlgout(long adjlgout) {
		this.adjlgout = adjlgout;
	}
	
	public boolean isFacedCongestion() {
		return facedCongestion;
	}
	public void setFacedCongestion(boolean facedCongestion) {
		this.facedCongestion = facedCongestion;
	}
	int loadSiteId = Misc.getUndefInt();
	public int getLoadSiteId() {
		return loadSiteId;
	}
	public void setLoadSiteId(int loadSiteId) {
		this.loadSiteId = loadSiteId;
	}
	public int getDestSiteId() {
		return destSiteId;
	}
	public void setDestSiteId(int destSiteId) {
		this.destSiteId = destSiteId;
	}
	public double getLead() {
		return lead;
	}
	public void setLead(double lead) {
		this.lead = lead;
	}
	public long getLatestEvent() {
		return latestEvent;
	}
	public void setLatestEvent(long latestEvent) {
		this.latestEvent = latestEvent;
	}
	public int getPrevShovelId() {
		return prevShovelId;
	}
	public void setPrevShovelId(int prevShovelId) {
		this.prevShovelId = prevShovelId;
	}
	public boolean hasStopEnded() {
		return hasStopEnded;
	}
	public void setHasStopEnded(boolean hasStopEnded) {
		this.hasStopEnded = hasStopEnded;
	}
	public byte getInShovelSeq() {
		return inShovelSeq;
	}
	public void setInShovelSeq(byte inShovelSeq) {
		this.inShovelSeq = inShovelSeq;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	
}

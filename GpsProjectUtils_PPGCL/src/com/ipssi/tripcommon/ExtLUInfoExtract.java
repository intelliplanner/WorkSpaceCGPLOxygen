package com.ipssi.tripcommon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;
public class ExtLUInfoExtract extends LUInfoExtract  {
	public static int SRC_DEST_FROM_SHOVEL_MATCH_ASSIGNMENT = 0;
	public static int SRC_DEST_FROM_SHOVEL_NON_MATCH_ASSIGNMENT = 1;
	public static int SRC_DEST_FROM_SITE_NON_MATCH_ASSIGNMENT = 2;
	public static int SRC_DEST_FROM_ASSIGNMENT_FORCE = 3;
	public static int SRC_DEST_FROM_PREV = 4;
	public static int SRC_DEST_FROM_PATH_SHOVEL_IN = 5;
	public static int SRC_DEST_FROM_PATH_SITE_IN = 6;
	public static int SRC_DEST_FROM_PATH_SHOVEL_NEAR =7;
	public static int SRC_DEST_FROM_PATH_SITE_NEAR = 8;

	public static class MiningInfo {
		private int siteId = Misc.getUndefInt();
		private int destId = Misc.getUndefInt();
		private int srcOfSiteId = Misc.getUndefInt();
		private int routeId = Misc.getUndefInt();
		private int shovelId = Misc.getUndefInt();
		private long dataOfPathMatch = Misc.getUndefInt();
		private double distIfNotExact = Misc.getUndefDouble();
		private long shovelTimeStamp = Misc.getUndefInt();
		private double shovelDist = Misc.getUndefDouble();
		private int shovelCycles = Misc.getUndefInt();
		private int strikeCycles = Misc.getUndefInt();
		private int secOfStopPrior = Misc.getUndefInt();
		private double avgShovelCycleDur = Misc.getUndefDouble();
		private double avgSholvelCycleDurExclPeaks = Misc.getUndefDouble();
		private double avgStrikeDur = Misc.getUndefDouble();
		private boolean dala = false;
		private byte loadQuality = 0;
		private long adjLgin;
		private long adjLgout;
		private short firstTrueZeroSecRel = Misc.getUndefShort();
		private short secondTrueZeroSecRel = Misc.getUndefShort();
		private short earliestPreArrivalWhenMeArrive = Misc.getUndefShort();
		private short latestArrivalWhenMeLeave = Misc.getUndefShort();
		private byte numAheadWhenArrive = Misc.getUndefByte();
		private byte numWaitingWhenLeave = Misc.getUndefByte();
	
		private short initActStartRel = Misc.getUndefShort();
		private short initActEndRel = Misc.getUndefShort();
		public short getInitActStartRel() {
			return initActStartRel;
		}
		public void setInitActStartRel(short initActStartRel) {
			this.initActStartRel = initActStartRel;
		}
		public short getInitActEndRel() {
			return initActEndRel;
		}
		public void setInitActEndRel(short initActEndRel) {
			this.initActEndRel = initActEndRel;
		}
		public short getFirstTrueZeroSecRel() {
			return firstTrueZeroSecRel;
		}
		public void setFirstTrueZeroSecRel(short firstTrueZeroSecRel) {
			this.firstTrueZeroSecRel = firstTrueZeroSecRel;
		}
		public short getSecondTrueZeroSecRel() {
			return secondTrueZeroSecRel;
		}
		public void setSecondTrueZeroSecRel(short secondTrueZeroSecRel) {
			this.secondTrueZeroSecRel = secondTrueZeroSecRel;
		}
		public short getEarliestPreArrivalWhenMeArrive() {
			return earliestPreArrivalWhenMeArrive;
		}
		public void setEarliestPreArrivalWhenMeArrive(
				short earliestPreArrivalWhenMeArrive) {
			this.earliestPreArrivalWhenMeArrive = earliestPreArrivalWhenMeArrive;
		}
		public short getLatestArrivalWhenMeLeave() {
			return latestArrivalWhenMeLeave;
		}
		public void setLatestArrivalWhenMeLeave(short latestArrivalWhenMeLeave) {
			this.latestArrivalWhenMeLeave = latestArrivalWhenMeLeave;
		}
		public byte getNumAheadWhenArrive() {
			return numAheadWhenArrive;
		}
		public void setNumAheadWhenArrive(byte numAheadWhenArrive) {
			this.numAheadWhenArrive = numAheadWhenArrive;
		}
		public byte getNumWaitingWhenLeave() {
			return numWaitingWhenLeave;
		}
		public void setNumWaitingWhenLeave(byte numWaitingWhenLeave) {
			this.numWaitingWhenLeave = numWaitingWhenLeave;
		}
		public boolean coreEquals(MiningInfo rhs) {
			
			return rhs != null && shovelId == rhs.shovelId
			&& shovelCycles == rhs.shovelCycles
			&& strikeCycles == rhs.strikeCycles
			&& dala == rhs.dala
			&& this.loadQuality == rhs.loadQuality
			;
		}
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MiningInfo))
				return false;
			MiningInfo rhs = (MiningInfo) o;
			return 
			siteId == rhs.siteId ||
			destId == rhs.destId ||
			srcOfSiteId == rhs.srcOfSiteId ||
			routeId == rhs.routeId ||
			shovelId == rhs.shovelId ||
			dataOfPathMatch == rhs.dataOfPathMatch ||
			distIfNotExact == rhs.distIfNotExact ||
			shovelTimeStamp == rhs.shovelTimeStamp ||
			Misc.isEqual(shovelDist,rhs.shovelDist)
			;
		}
		public MiningInfo() {
			
		}
		public String toString() {
			return "Src:"+siteId+", Dest:"+destId+", Route:"+routeId+", Quality:"+srcOfSiteId+", dist:"+distIfNotExact+", time if prev:"
			+ExtLUInfoExtract.dbgFormat(dataOfPathMatch)
			+", Shovel:"+shovelId+", ShovelDist:"+shovelDist+", ShovelTime:"+ExtLUInfoExtract.dbgFormat(shovelTimeStamp)
			;
			
		}
		public void copy(MiningInfo rhs) {
			siteId = rhs.siteId;
			destId = rhs.destId;
			srcOfSiteId = rhs.srcOfSiteId;
			routeId = rhs.routeId;
			shovelId = rhs.shovelId;
			dataOfPathMatch = rhs.dataOfPathMatch;
			distIfNotExact = rhs.distIfNotExact;
			shovelTimeStamp = rhs.shovelTimeStamp;
			shovelDist = rhs.shovelDist;
			this.shovelCycles = rhs.shovelCycles;
			this.strikeCycles = rhs.strikeCycles;
			this.secOfStopPrior = rhs.secOfStopPrior;
			this.avgShovelCycleDur = rhs.avgShovelCycleDur;
			this.avgSholvelCycleDurExclPeaks = rhs.avgSholvelCycleDurExclPeaks;
			this.avgStrikeDur = rhs.avgStrikeDur;
			this.dala = rhs.dala;
			this.loadQuality = rhs.loadQuality;
			this.adjLgin = rhs.adjLgin;
			this.adjLgout = rhs.adjLgout;
			this.initActStartRel = rhs.initActStartRel;
			this.initActEndRel = rhs.initActEndRel;
		}
		
		public int getSiteId() {
			return siteId;
		}
		public void setSiteId(int siteId) {
			this.siteId = siteId;
		}
		public int getDestId() {
			return destId;
		}
		public void setDestId(int destId) {
			this.destId = destId;
		}
		public int getSrcOfSiteId() {
			return srcOfSiteId;
		}
		public void setSrcOfSiteId(int srcOfSiteId) {
			this.srcOfSiteId = srcOfSiteId;
		}
		public int getRouteId() {
			return routeId;
		}
		public void setRouteId(int routeId) {
			this.routeId = routeId;
		}
		public int getShovelId() {
			return shovelId;
		}
		public void setShovelId(int shovelId) {
			this.shovelId = shovelId;
		}
		public long getDataOfPathMatch() {
			return dataOfPathMatch;
		}
		public void setDataOfPathMatch(long dataOfPathMatch) {
			this.dataOfPathMatch = dataOfPathMatch;
		}
		public double getDistIfNotExact() {
			return distIfNotExact;
		}
		public void setDistIfNotExact(double distIfNotExact) {
			this.distIfNotExact = distIfNotExact;
		}
		public long getShovelTimeStamp() {
			return shovelTimeStamp;
		}
		public void setShovelTimeStamp(long shovelTimeStamp) {
			this.shovelTimeStamp = shovelTimeStamp;
		}
		public double getShovelDist() {
			return shovelDist;
		}
		public void setShovelDist(double shovelDist) {
			this.shovelDist = shovelDist;
		}
		public MiningInfo(int siteId, int destId, int srcOfSiteId, int routeId,
				int shovelId, long dataOfPathMatch, double distIfNotExact,
				long shovelTimeStamp, double shovelDist) {
			super();
			this.siteId = siteId;
			this.destId = destId;
			this.srcOfSiteId = srcOfSiteId;
			this.routeId = routeId;
			this.shovelId = shovelId;
			this.dataOfPathMatch = dataOfPathMatch;
			this.distIfNotExact = distIfNotExact;
			this.shovelTimeStamp = shovelTimeStamp;
			this.shovelDist = shovelDist;
		}
		public int getShovelCycles() {
			return shovelCycles;
		}
		public void setShovelCycles(int shovelCycles) {
			this.shovelCycles = shovelCycles;
		}
		public int getStrikeCycles() {
			return strikeCycles;
		}
		public void setStrikeCycles(int strikeCycles) {
			this.strikeCycles = strikeCycles;
		}
		public int getSecOfStopPrior() {
			return secOfStopPrior;
		}
		public void setSecOfStopPrior(int secOfStopPrior) {
			this.secOfStopPrior = secOfStopPrior;
		}
		public double getAvgShovelCycleDur() {
			return avgShovelCycleDur;
		}
		public void setAvgShovelCycleDur(double avgShovelCycleDur) {
			this.avgShovelCycleDur = avgShovelCycleDur;
		}
		public double getAvgSholvelCycleDurExclPeaks() {
			return avgSholvelCycleDurExclPeaks;
		}
		public void setAvgSholvelCycleDurExclPeaks(double avgSholvelCycleDurExclPeaks) {
			this.avgSholvelCycleDurExclPeaks = avgSholvelCycleDurExclPeaks;
		}
		public double getAvgStrikeDur() {
			return avgStrikeDur;
		}
		public void setAvgStrikeDur(double avgStrikeDur) {
			this.avgStrikeDur = avgStrikeDur;
		}
		public boolean isDala() {
			return dala;
		}
		public void setDala(boolean dala) {
			this.dala = dala;
		}
		public byte getLoadQuality() {
			return loadQuality;
		}
		public void setLoadQuality(byte loadQuality) {
			this.loadQuality = loadQuality;
		}
		public long getAdjLgin() {
			return adjLgin;
		}
		public void setAdjLgin(long adjLgin) {
			this.adjLgin = adjLgin;
		}
		public long getAdjLgout() {
			return adjLgout;
		}
		public void setAdjLgout(long adjLgout) {
			this.adjLgout = adjLgout;
		}
				
	}
    public static class WBInfo {
    	private long wb1In = Misc.getUndefInt();
    	private long wb2In = Misc.getUndefInt();
    	private long wb3In = Misc.getUndefInt();
    	private long wb1Out = Misc.getUndefInt();
    	private long wb2Out = Misc.getUndefInt();
    	private long wb3Out = Misc.getUndefInt();
    	private int wb1Id = Misc.getUndefInt();
    	private int wb2Id = Misc.getUndefInt();
    	private int wb3Id = Misc.getUndefInt();
    	public void copy(WBInfo rhs) {
    		if (rhs == null) {
    			wb1In =  Misc.getUndefInt();
    			wb2In =  Misc.getUndefInt();
    			wb3In =  Misc.getUndefInt();
    			wb1Out =  Misc.getUndefInt();
    			wb2Out =  Misc.getUndefInt();
    			wb3Out =  Misc.getUndefInt();
    			wb1Id =  Misc.getUndefInt();
    			wb2Id = Misc.getUndefInt();
    			wb3Id = Misc.getUndefInt();
    		}
    		else {
    			wb1In =  rhs.wb1In;
    			wb2In =  rhs.wb2In;
    			wb3In =  rhs.wb3In;
    			wb1Out = rhs.wb1Out;
    			wb2Out = rhs.wb2Out;
    			wb3Out = rhs.wb3Out;
    			wb1Id = rhs.wb1Id;
    			wb2Id = rhs.wb2Id;
    			wb3Id = rhs.wb3Id;
    		}
    	}
    	public boolean equals(WBInfo rhs) {
        	return (
        			rhs != null &&
        	((!Misc.isUndef(wb1In) && !Misc.isUndef(rhs.wb1In) && wb1In == rhs.wb1In) || (Misc.isUndef(wb1In) && Misc.isUndef(rhs.wb1In))) &&
        	((!Misc.isUndef(wb2In) && !Misc.isUndef(rhs.wb2In) && wb2In == rhs.wb2In) || (Misc.isUndef(wb2In) && Misc.isUndef(rhs.wb2In))) &&
        	((!Misc.isUndef(wb3In) && !Misc.isUndef(rhs.wb3In) && wb3In == rhs.wb3In) || (Misc.isUndef(wb3In) && Misc.isUndef(rhs.wb3In))) &&
        	((!Misc.isUndef(wb1Out) && !Misc.isUndef(rhs.wb1Out) && wb1Out == rhs.wb1Out) || (Misc.isUndef(wb1Out) && Misc.isUndef(rhs.wb1Out))) &&
        	((!Misc.isUndef(wb2Out) && !Misc.isUndef(rhs.wb2Out) && wb2Out == rhs.wb2Out) || (Misc.isUndef(wb2Out) && Misc.isUndef(rhs.wb2Out))) &&
        	((!Misc.isUndef(wb3Out) && !Misc.isUndef(rhs.wb3Out) && wb3Out == rhs.wb3Out) || (Misc.isUndef(wb3Out) && Misc.isUndef(rhs.wb3Out))) &&
        	(wb1Id == rhs.wb1Id) &&
        	(wb2Id == rhs.wb2Id) &&
        	(wb3Id == rhs.wb3Id)
        	)
        	;    	
        }

    	public String toString() {
	    	return	
	    	   "WB1:"+ExtLUInfoExtract.getRegionName(wb1Id)+"WB1in:"+ExtLUInfoExtract.dbgFormat(wb1In)+"WB1out:"+dbgFormat(wb1Out)
	    	+"WB2:"+ExtLUInfoExtract.getRegionName(wb2Id)+"WB2in:"+ExtLUInfoExtract.dbgFormat(wb2In)+"WB2out:"+dbgFormat(wb2Out)
	    	+"WB3:"+ExtLUInfoExtract.getRegionName(wb3Id)+"WB3in:"+ExtLUInfoExtract.dbgFormat(wb3In)+"WB3out:"+dbgFormat(wb3Out)
	    	;
    	}
		public long getWb1In() {
			return wb1In;
		}
		public void setWb1In(long wb1In) {
			this.wb1In = wb1In;
		}
		public long getWb2In() {
			return wb2In;
		}
		public void setWb2In(long wb2In) {
			this.wb2In = wb2In;
		}
		public long getWb3In() {
			return wb3In;
		}
		public void setWb3In(long wb3In) {
			this.wb3In = wb3In;
		}
		public long getWb1Out() {
			return wb1Out;
		}
		public void setWb1Out(long wb1Out) {
			this.wb1Out = wb1Out;
		}
		public long getWb2Out() {
			return wb2Out;
		}
		public void setWb2Out(long wb2Out) {
			this.wb2Out = wb2Out;
		}
		public long getWb3Out() {
			return wb3Out;
		}
		public void setWb3Out(long wb3Out) {
			this.wb3Out = wb3Out;
		}
		public int getWb1Id() {
			return wb1Id;
		}
		public void setWb1Id(int wb1Id) {
			this.wb1Id = wb1Id;
		}
		public int getWb2Id() {
			return wb2Id;
		}
		public void setWb2Id(int wb2Id) {
			this.wb2Id = wb2Id;
		}
		public int getWb3Id() {
			return wb3Id;
		}
		public void setWb3Id(int wb3Id) {
			this.wb3Id = wb3Id;
		}
    }
	private long gateIn = Misc.getUndefInt();
	private long areaIn = Misc.getUndefInt();
	private long areaOut = Misc.getUndefInt();
	private long gateOut = Misc.getUndefInt();
	private int areaId = Misc.getUndefInt();
	private int tripId = Misc.getUndefInt();
	private int materialId = Misc.getUndefInt();
	private WBInfo wbInfo = null;
	private MiningInfo miningInfo = null;
	private ArrayList<Integer> alternateMaterialList = null;
	public short getInitActStartRel() {
		return this.miningInfo == null ? Misc.getUndefShort() : miningInfo.getInitActStartRel();
	}
	public void setInitActStartRel(short initActStartRel) {
		
		if (!Misc.isUndef(initActStartRel) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.initActStartRel = initActStartRel;
	}
	public short getInitActEndRel() {
		return this.miningInfo == null ? Misc.getUndefShort() : miningInfo.getInitActEndRel();
	}
	public void setInitActEndRel(short initActEndRel) {
		if (!Misc.isUndef(initActEndRel) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.initActEndRel = initActEndRel;
	}
	public int getSiteId() {
		return miningInfo == null ? Misc.getUndefInt() : miningInfo.getSiteId();
	}
	public void setSiteId(int siteId) {
		if (!Misc.isUndef(siteId) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.siteId = siteId;
	}
	public int getDestId() {
		return miningInfo == null ? Misc.getUndefInt() : miningInfo.getDestId();
	}
	public void setDestId(int destId) {
		if (!Misc.isUndef(destId) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.destId = destId;
	}
	
	public int getShovelCycles() {
		return miningInfo == null ? Misc.getUndefInt() : miningInfo.shovelCycles;
	}
	public void setShovelCycles(int shovelCycles) {
		if (!Misc.isUndef(shovelCycles) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.shovelCycles = shovelCycles;
	}
	public int getStrikeCycles() {
		return miningInfo == null ? Misc.getUndefInt() : miningInfo.strikeCycles;
	}
	public void setStrikeCycles(int strikeCycles) {
		if (!Misc.isUndef(strikeCycles) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.strikeCycles = strikeCycles;
	}
	public int getSecOfStopPrior() {
		return miningInfo == null ? Misc.getUndefInt() : miningInfo.secOfStopPrior;
	}
	public void setSecOfStopPrior(int secOfStopPrior) {
		if (!Misc.isUndef(secOfStopPrior) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.secOfStopPrior = secOfStopPrior;
	}
	public double getAvgShovelCycleDur() {
		return miningInfo == null ? Misc.getUndefDouble() : miningInfo.avgShovelCycleDur;
	}
	public void setAvgShovelCycleDur(double avgShovelCycleDur) {
		if (!Misc.isUndef(avgShovelCycleDur) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.avgShovelCycleDur = avgShovelCycleDur;
	}
	public double getAvgSholvelCycleDurExclPeaks() {
		return miningInfo == null ? Misc.getUndefDouble() : miningInfo.avgSholvelCycleDurExclPeaks;
	}
	public void setAvgSholvelCycleDurExclPeaks(double avgSholvelCycleDurExclPeaks) {
		if (!Misc.isUndef(avgSholvelCycleDurExclPeaks) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.avgSholvelCycleDurExclPeaks = avgSholvelCycleDurExclPeaks;
	}
	public double getAvgStrikeDur() {
		return miningInfo == null ? Misc.getUndefInt() : miningInfo.avgStrikeDur;
	}
	public boolean isDala() {
		return miningInfo != null && miningInfo.isDala();
	}
	public void setDala(boolean val) {
		if (val && miningInfo == null)
			this.miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.setDala(val);
	}
	public byte getLoadQuality() {
		return miningInfo == null ? 0 : miningInfo.getLoadQuality();
	}
	public void setLoadQuality(byte val) {
		if (val != 0 && miningInfo == null)
			this.miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.setLoadQuality(val);
	}
	public void setAvgStrikeDur(double avgStrikeDur) {
		if (!Misc.isUndef(avgStrikeDur) && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null)
			miningInfo.avgStrikeDur = avgStrikeDur;
	}
    
	public short getFirstTrueZeroSecRel() {
		return miningInfo == null ? Misc.getUndefShort() : miningInfo.getFirstTrueZeroSecRel();
	}
	public void setFirstTrueZeroSecRel(short firstTrueZeroSecRel) {
		if (miningInfo != null)
			miningInfo.setFirstTrueZeroSecRel(firstTrueZeroSecRel);
		else if (!Misc.isUndef(firstTrueZeroSecRel)) {
			miningInfo = new MiningInfo();
			miningInfo.setFirstTrueZeroSecRel(firstTrueZeroSecRel);
		}
	}
	public short getSecondTrueZeroSecRel() {
		return miningInfo == null ? Misc.getUndefShort() : miningInfo.getSecondTrueZeroSecRel();
	}
	public void setSecondTrueZeroSecRel(short secondTrueZeroSecRel) {
		if (miningInfo != null)
			miningInfo.setSecondTrueZeroSecRel(secondTrueZeroSecRel);
		else if (!Misc.isUndef(secondTrueZeroSecRel)) {
			miningInfo = new MiningInfo();
			miningInfo.setSecondTrueZeroSecRel(secondTrueZeroSecRel);
		}
	}
	public short getEarliestPreArrivalWhenMeArrive() {
		return miningInfo == null ? Misc.getUndefShort() : miningInfo.getEarliestPreArrivalWhenMeArrive();
	}
	public void setEarliestPreArrivalWhenMeArrive(short earliestPreArrivalWhenMeArrive) {
		if (miningInfo != null) {
			miningInfo.earliestPreArrivalWhenMeArrive = earliestPreArrivalWhenMeArrive;
		}
		else if (!Misc.isUndef(earliestPreArrivalWhenMeArrive)) {
			miningInfo = new MiningInfo();
			miningInfo.earliestPreArrivalWhenMeArrive = earliestPreArrivalWhenMeArrive;
		}
	}
	public short getLatestArrivalWhenMeLeave() {
		return miningInfo == null ? Misc.getUndefShort() : miningInfo.latestArrivalWhenMeLeave;
	}
	public void setLatestArrivalWhenMeLeave(short latestArrivalWhenMeLeave) {
		if (miningInfo != null) {
			miningInfo.latestArrivalWhenMeLeave = latestArrivalWhenMeLeave;
		}
		else if (!Misc.isUndef(latestArrivalWhenMeLeave)) {
			miningInfo = new MiningInfo();
			miningInfo.latestArrivalWhenMeLeave = latestArrivalWhenMeLeave;
		}
	}
	public byte getNumAheadWhenArrive() {
		return miningInfo == null ? Misc.getUndefByte(): miningInfo.numAheadWhenArrive;
	}
	public void setNumAheadWhenArrive(byte numAheadWhenArrive) {
		if (miningInfo != null) {
			miningInfo.numAheadWhenArrive = numAheadWhenArrive;
		}
		else if (!Misc.isUndef(numAheadWhenArrive)) {
			miningInfo = new MiningInfo();
			miningInfo.numAheadWhenArrive = numAheadWhenArrive;
		}
	}
	public byte getNumWaitingWhenLeave() {
		return miningInfo == null ? Misc.getUndefByte() : miningInfo.numWaitingWhenLeave;
	}
	public void setNumWaitingWhenLeave(byte numWaitingWhenLeave) {
		if (miningInfo != null) {
			miningInfo.setNumWaitingWhenLeave(numWaitingWhenLeave);
		}
		else if (!Misc.isUndef(numWaitingWhenLeave)) {
			miningInfo = new MiningInfo();
			miningInfo.setNumWaitingWhenLeave(numWaitingWhenLeave);
		}
	}
	
    public void clear() {
    	super.clear();
    	gateIn = Misc.getUndefInt();
    	areaIn = Misc.getUndefInt();
    	areaOut = Misc.getUndefInt();
    	gateOut = Misc.getUndefInt();
    	materialId = Misc.getUndefInt();
    	tripId = Misc.getUndefInt();
    	wbInfo = null;
    	miningInfo = null;
    }
    
    private static SimpleDateFormat dbgFormatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    private static String dbgFormat(Date dt) {
    	return dt == null ? "null" : dbgFormatter.format(dt);
    }
    public static String dbgFormat(long dt) {
    	Date tempDt = null;
    	if(!Misc.isUndef(dt))
    		tempDt = new Date(dt);
    	return tempDt == null ? "null" : dbgFormatter.format(tempDt);
    }
    private static String getRegionName(int regionId) {
    	RegionTestHelper rt = null;
    	try {
    		rt = RegionTest.getRegionInfo(regionId, null);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it;
    	}
    	return rt == null ? "null" : rt.region.m_name;
    	//RegionTest.g_regionInfos.get(regionId) != null ? RegionTest.g_regionInfos.get(regionId).region.m_name : "(null)";
    }
    public String toString() {
    	synchronized (dbgFormatter) {
    		
    		return 
    		//2014_06_20 		+ "Parkin:"+dbgFormat(parkIn)+"ParkOut:"+dbgFormat(parkOut)    		
    		//2014_06_20 	+"Park:"+getRegionName(parkId)
    		super.toString()
    			+ "Gin:"+dbgFormat(gateIn)
    			+ (wbInfo == null ? "" : wbInfo.toString())
    		+ "Area:"+getRegionName(areaId)+"Ain:"+dbgFormat(areaIn)+"event:"
    		+"Aout:"+dbgFormat(areaOut)
    		+"Gout:"+dbgFormat(gateOut)
    		;
    	}
    }
    
    public boolean equals(LUInfoExtract rhsPassed) {
    	if (rhsPassed == null || !(rhsPassed instanceof ExtLUInfoExtract))
    		return false;
    	ExtLUInfoExtract rhs = (ExtLUInfoExtract) rhsPassed;
    		return  
    	super.equals(rhs) &&
    	((!Misc.isUndef(gateIn) && !Misc.isUndef(rhs.gateIn) && gateIn == rhs.gateIn) || (Misc.isUndef(gateIn) && Misc.isUndef(rhs.gateIn))) &&
    	((!Misc.isUndef(areaIn) && !Misc.isUndef(rhs.areaIn) && areaIn == rhs.areaIn) || (Misc.isUndef(areaIn) && Misc.isUndef(rhs.areaIn))) &&
    	((!Misc.isUndef(areaOut) && !Misc.isUndef(rhs.areaOut) && areaOut == rhs.areaOut) || (Misc.isUndef(areaOut) && Misc.isUndef(rhs.areaOut))) &&
    	((!Misc.isUndef(gateOut) && !Misc.isUndef(rhs.gateOut) && gateOut == rhs.gateOut) || (Misc.isUndef(gateOut) && Misc.isUndef(rhs.gateOut))) &&
    	(areaId == rhs.areaId) && 
    	((wbInfo == null && rhs.wbInfo == null) || (wbInfo != null && rhs.wbInfo != null && wbInfo.equals(rhs.wbInfo)))
    	&& ((this.miningInfo == null && rhs.miningInfo == null) || this.miningInfo.coreEquals(rhs.miningInfo))
    	
    	;    	
    }

    
    public void copy(LUInfoExtract rhsPassed) {
    	if (rhsPassed == null)
    		return;
    	//super.copy(rhsPassed);
    	if (rhsPassed instanceof ExtLUInfoExtract) {
    		ExtLUInfoExtract rhs = (ExtLUInfoExtract) rhsPassed;
	    	gateIn = rhs.gateIn;
	    	areaIn = rhs.areaIn;
	    	areaOut = rhs.areaOut;
	    	gateOut = rhs.gateOut;
	    	areaId = rhs.areaId;
	    	tripId = rhs.tripId;
	    	materialId = rhs.materialId;
	    	if (rhs.miningInfo == null)
	    		this.miningInfo = null;
	    	else {
	    		if (this.miningInfo == null)
	    			this.miningInfo = new MiningInfo();
	    		this.miningInfo.copy(rhs.miningInfo);
	    		
	    	}

	    	if (rhs.wbInfo == null)
	    		this.wbInfo = null;
	    	else {
	    		if (this.wbInfo == null)
	    			this.wbInfo = new WBInfo();
	    		this.wbInfo.copy(rhs.wbInfo);
	    	}    	
    	}
    	else {
    		this.clear();
    		//super.copy(rhsPassed);
    	}
    	super.copy(rhsPassed);
    	
    }
    
    public String getState(){
    	if(!Misc.isUndef(getWaitOut())){
    		return "waitOut";
    	}else if(!Misc.isUndef(gateOut)){
    		return "gateOut";
    	}else if(!Misc.isUndef(areaOut)){
    		return "areaOut";
    	}else if(!Misc.isUndef(areaIn)){
    		return "areaIn";
    	}else if(!Misc.isUndef(gateIn)){
    		return "gateIn";
    	}else if(!Misc.isUndef(getWaitIn())){
    		return "waitIn";
    	}
	    else if (!Misc.isUndef(getWb1In())){
			return "WB1In";
		}
	    else if (!Misc.isUndef(getWb2In())){
			return "WB2In";
		}
	    else if (!Misc.isUndef(getWb3In())){
			return "WB3In";
		}
    	else {
    		return "";
    	}
    }
    public long getLatestEventDateTime(){
    	if(getWaitOut() > 0) {
    		return getWaitOut();
    	}else if(gateOut > 0){
    		return gateOut;
    	}
    	else if (getWb3Out() > 0) {
    		return getWb3Out();
    	}
    	else if (getWb3In() > 0){
    		return getWb3In();
    	}
    	else if (getWb2Out() >0){
    		return getWb2Out();
    	}
    	else if (getWb2In() > 0){
    		return getWb2In();
    	}
    	else if(areaOut > 0){
    		return areaOut;
    	}
    	else if(areaIn > 0){
    		return areaIn;
    	}
    	else if (getWb1Out() > 0){
    		return getWb1Out();
    	}
    	else if (getWb1In() > 0){
    		return getWb1In();
    	}
    	else if(gateIn > 0){
    		return gateIn;
    	}
    	else if(getWaitIn() > 0){
    		return getWaitIn();
    	}else {
    		return Misc.getUndefInt();
    	}
    }
    public long getEarliestEventDateTime(){
    	if(!Misc.isUndef(getWaitIn())){
    		return getWaitIn();
    	}else if(!Misc.isUndef(gateIn)){
    		return gateIn;
    	}
    	else if (!Misc.isUndef(getWb1In())){
        		return getWb1In();
    	}
    	else if (!Misc.isUndef(getWb1Out())){
    		return getWb1Out();
    	}
    	else if(!Misc.isUndef(areaIn)){
    		return areaIn;
    	}
    	else if(!Misc.isUndef(areaOut)){
    		return areaOut;
    	} 
    	else if (!Misc.isUndef(getWb2In())){
    		return getWb2In();
    	}
    	else if (!Misc.isUndef(getWb2Out())){
    		return getWb2Out();
    	}
    	else if (!Misc.isUndef(getWb3In())){
    		return getWb3In();
    	}
    	else if (!Misc.isUndef(getWb3Out())){
    		return getWb3Out();
    	}

    	else if(!Misc.isUndef(gateOut)){
    		return gateOut;
    	}
    	else if(!Misc.isUndef(getWaitOut())){
    		return getWaitOut();
    	}else {
    		return Misc.getUndefInt();
    	}
    }
	public boolean isCalcComplete() {
		return true;
	}
	public long getGateIn() {
		return gateIn;
	}
	public void setGateIn(long gateIn) {
		this.gateIn = gateIn;
	}
	public long getAreaIn() {
		return areaIn;
	}
	public void setAreaIn(long areaIn) {
		this.areaIn = areaIn;
	}
	public long getAreaOut() {
		return areaOut;
	}
	public void setAreaOut(long areaOut) {
		this.areaOut = areaOut;
	}
	public long getGateOut() {
		return gateOut;
	}
	public void setGateOut(long gateOut) {
		this.gateOut = gateOut;
	}
	public int getAreaId() {
		return areaId;
	}
	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public long getWb1In() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb1In();
	}
	public void setWb1In(long wb1In) {
		if (wb1In > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb1In(wb1In);
		}
	}
	public long getWb2In() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb2In();
	}
	public void setWb2In(long wb2In) {
		if (wb2In > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb2In(wb2In);
		}
	}
	public long getWb3In() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb3In();
	}
	public void setWb3In(long wb3In) {
		if (wb3In > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb3In(wb3In);
		}
	}
	public int getWb1Id() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb1Id();
	}
	public void setWb1Id(int wb1Id) {
		if (wb1Id > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb1Id(wb1Id);
		}
	}
	public int getWb2Id() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb2Id();
	}
	public void setWb2Id(int wb2Id) {
		if (wb2Id > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb2Id(wb2Id);
		}
	}
	public int getWb3Id() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb3Id();
	}
	public void setWb3Id(int wb3Id) {
		if (wb3Id > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb3Id(wb3Id);
		}
	}
	public long getWb1Out() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb1Out();
	}
	public void setWb1Out(long wb1Out) {
		if (wb1Out > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb1Out(wb1Out);
		}
	}
	public long getWb2Out() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb2Out();
	}
	public void setWb2Out(long wb2Out) {
		if (wb2Out > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb2Out(wb2Out);
		}
	}
	public long getWb3Out() {
		return wbInfo == null ? Misc.getUndefInt() : wbInfo.getWb3Out();
	}
	public void setWb3Out(long wb3Out) {
		if (wb3Out > 0) {
			if (wbInfo == null )
				wbInfo = new WBInfo();
			wbInfo.setWb3Out(wb3Out);
		}
	}


	public WBInfo getWbInfo() {
		return wbInfo;
	}
	public void setWbInfo(WBInfo wbInfo) {
		this.wbInfo = wbInfo;
	}
	public ArrayList<Integer> getAlternateMaterialList() {
		return alternateMaterialList;
	}
	public void setAlternateMaterialList(ArrayList<Integer> alternateMaterialList) {
		this.alternateMaterialList = alternateMaterialList;
	}
	public MiningInfo getMiningInfo() {
		return miningInfo;
	}
	public void setMiningInfo(MiningInfo miningInfo) {
		this.miningInfo = miningInfo;
	}
	
	public long getAdjLgin() {
		return miningInfo != null ? miningInfo.getAdjLgin() : Misc.getUndefInt();
	}
	public long getAdjLgout() {
		return miningInfo != null ? miningInfo.getAdjLgout() : Misc.getUndefInt();
	}
	public void setAdjLgin(long lgin) {
		if (lgin > 0 && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null) {
			miningInfo.adjLgin = lgin;
		}
	}
	public void setAdjLgout(long lgout) {
		if (lgout > 0 && miningInfo == null)
			miningInfo = new MiningInfo();
		if (miningInfo != null) {
			miningInfo.adjLgout = lgout;
		}
	}
	
	
}

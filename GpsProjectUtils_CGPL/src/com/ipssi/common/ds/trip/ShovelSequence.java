package com.ipssi.common.ds.trip;

import java.io.Serializable;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


import com.ipssi.cache.BLEInfo;
import com.ipssi.cache.ExcLoadEvent;
import com.ipssi.cache.NewBLEMgmt;
import com.ipssi.cache.NewExcLoadEventMgmt;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;

public class ShovelSequence  implements Comparable<ShovelSequence>, Serializable {
	public static final boolean SHOVELSEQ_BY_ACT = false;//true has issues ..
	public static final long END_TO_END_GAP_MILLI = (5*20+10)*1000;
	private static final long serialVersionUID = 1L;
	public final static boolean IGNORE_STRIKE = false;
	/* orig not use
	public static final int BLE_EXCLOAD_HIQSTRIKE_MATCH = 15;

	public static final int BLE_EXCLOAD_LOQSTRIKE_MATCH = 13;
	public static final int BLE_EXCLOAD_STRIKE_NOMATCH = 12;
	public static final int BLE_EXCLOAD_ONLY = 11;
	public static final int BLE_NOEXCLOAD_MAYBESTRIKE = 10;
	public static final int LEAST_BLE_INDICATOR_Q = 10;
	public static final int NEAR_EXCLOAD_HIQSTRIKE_MATCH = 7;
	public static final int NEAR_EXCLOAD_LOQSTRIKE_MATCH = 6;
	public static final int NEAR_EXCLOAD_STRIKE_NOMATCH = 5;
	public static final int NEAR_NOEXCLOAD_STRIKE_ONLY = 4;
	public static final int NEAR_EXCLOAD_ONLY = 3;
	public static final int NEAR_ONLY = 1;
	*/
	// with like BLE differentiation
	public static final int BLE_EXCLOAD_HIQSTRIKE_MATCH = 15;
	public static final int BLE_EXCLOAD_LOQSTRIKE_MATCH = 14;
	public static final int BLE_EXCLOAD_STRIKE_NOMATCH = 13;
	public static final int BLE_EXCLOAD_ONLY = 12;
	public static final int BLE_NOEXCLOAD_MAYBESTRIKE = 11;
	
	public static final int LEAST_BLE_INDICATOR_Q = 11;
	
	public static final int LBLE_EXCLOAD_HIQSTRIKE_MATCH = 10;
	public static final int LBLE_EXCLOAD_LOQSTRIKE_MATCH = 9;
	public static final int LBLE_EXCLOAD_STRIKE_NOMATCH = 8;
	public static final int LBLE_EXCLOAD_ONLY = 7;
	public static final int LBLE_NOEXCLOAD_MAYBESTRIKE = 6;

	public static final int NEAR_EXCLOAD_HIQSTRIKE_MATCH = 5;
	public static final int NEAR_EXCLOAD_LOQSTRIKE_MATCH = 5;
	public static final int NEAR_EXCLOAD_STRIKE_NOMATCH = 4;
	public static final int NEAR_NOEXCLOAD_STRIKE_ONLY = 3;
	public static final int NEAR_EXCLOAD_ONLY = 2;
	public static final int NEAR_ONLY = 1;

	/*  //ignoring strike ..
	public static final int BLE_EXCLOAD_HIQSTRIKE_MATCH = 8;
	public static final int BLE_EXCLOAD_LOQSTRIKE_MATCH = 8;
	public static final int BLE_EXCLOAD_STRIKE_NOMATCH = 8;
	public static final int BLE_EXCLOAD_ONLY = 7;
	public static final int BLE_NOEXCLOAD_MAYBESTRIKE = 6;
	
	public static final int LEAST_BLE_INDICATOR_Q = 11;
	
	public static final int LBLE_EXCLOAD_HIQSTRIKE_MATCH = 8;
	public static final int LBLE_EXCLOAD_LOQSTRIKE_MATCH = 8;
	public static final int LBLE_EXCLOAD_STRIKE_NOMATCH = 8;
	public static final int LBLE_EXCLOAD_ONLY = 7;
	public static final int LBLE_NOEXCLOAD_MAYBESTRIKE = 6;

	public static final int NEAR_EXCLOAD_HIQSTRIKE_MATCH = 4;
	public static final int NEAR_EXCLOAD_LOQSTRIKE_MATCH = 4;
	public static final int NEAR_EXCLOAD_STRIKE_NOMATCH = 4;
	public static final int NEAR_NOEXCLOAD_STRIKE_ONLY = 3;
	public static final int NEAR_EXCLOAD_ONLY = 2;
	public static final int NEAR_ONLY = 1;
*/
    public static final int G_LOADEVENT_MUST_EXIST_IF_VALID_MILLI = 20*60*1000;
    public static final int DO_TIMING_ADJUSTMENT_APPROACH = 2;//0 = min 4, max 5. 1 = adjust with improper at LHS and set min to 5, 2 => natural break
    public static final boolean DO_GAP_BY_DEVIATION = true;
	//
    public boolean strictNoLoad() {
    	return this.getCycleCount() == 0 && this.getActEndTS() == this.getActStartTS();
    }
    public boolean hasLoadStarted() {
    	//return this.hasValidExcEventsAroundPeriod ? this.cycleCount >= 2 : this.getActEndTS()-this.getActStartTS() >= ThreadContextCache.G_DOLASTSTOP_PROC_THRESH_MILLI;
    	return this.hasValidExcEventsAroundPeriod ? this.getActEndTS() - this.getActStartTS() > 0 && this.getCycleCount() > 0
    			: this.getActEndTS()-this.getActStartTS() >= ThreadContextCache.G_DOLASTSTOP_PROC_THRESH_MILLI_NOTMATCH
    			;
    }
    
    public boolean maybeLoadEnded() {
    	return this.cycleCount >= ShovelSequence.PREFERRED_SHOVEL_CYCLES || this.isStopEnded() || (!this.hasValidExcEventsAroundPeriod && (this.getActEndTS()-this.getActStartTS()) >= ShovelSequence.G_NON_OVERLAP_MILLI_FOR_LOAD); 
    }
	public int meBetterThanByQualityOnly(ShovelSequence bestAssignableMe) {
		//1 if me better, -1 if worse, 0 same
		int toSwap = bestAssignableMe == null ? 1 : 0;
		
		if (toSwap == 0) {
			byte meQ = this.getDumperBestQuality();
			byte bestQ = bestAssignableMe.getDumperBestQuality();
			toSwap = meQ > bestQ ? 1 : meQ < bestQ ? -1 : 0;
		}
		return toSwap;
	}
	public int meBetterThan(ShovelSequence bestAssignableMe, int meShovelId, int bestShovelId, int prevAssignedShovelId) {
		int toSwap = this.meBetterThanByQualityOnly(bestAssignableMe);
		return toSwap;
	}
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		StringBuilder sb = new StringBuilder();
		sb.append("Dumper:").append(dumperId).append(" Q:").append(dumperBestQuality).append(" sc:").append(this.cycleCount)//NEWAPPROACH.append(" Qcaus:").append(this.maxQCausingTimeChange)
		.append(" St:").append(sdf.format(Misc.longToUtilDate(this.startTS)))
		.append(" En:").append(sdf.format(Misc.longToUtilDate(this.endTS)))
		.append(" ASt:").append(sdf.format(Misc.longToUtilDate(this.getActStartTS())))
		.append(" AEn:").append(sdf.format(Misc.longToUtilDate(this.startTS+(Misc.isUndef(this.endActLoadRelStartSec) ? 0 : this.endActLoadRelStartSec)*1000)))
		.append(" Strike:").append(this.startStrikeRelStartSec).append(",").append(this.endStrikeRelStartSec)
		.append(" BLE:").append(this.startBLERelStartSec).append(",").append(this.endBLERelStartSec)
		.append(" LE:").append(this.startLoadEventRelStartSec).append(",").append(this.endLoadEventRelStartSec)
		.append(" Act:").append(this.getSecGap(this.getActStartTS())).append(",").append(this.endActLoadRelStartSec)
		.append(" Base:").append(this.baseActStartRelStartSec).append(",").append(this.baseActEndRelStartSec)
		
		.append(" StrikeCnt:").append(this.strikeCount).append(", MatchStrikeCnt:").append(this.loadActivityStrikeMatchCount)
		//.append(" CntBefMe:").append(countOutBeforeMe)
		;
		
		return sb.toString();
	}
	private int dumperId;
	private byte dumperBestQuality = 0;
	private byte strikeCount = 0;
	private byte likeBLE = 0; // 0 no ble, 1 very near but no BLE, 2 has ble
	private boolean firm = false;
	private long startTS;
	private long actStartTS = 0;
	private long endTS;
	private short startStrikeRelStartSec = Misc.getUndefShort();
	private short endStrikeRelStartSec = Misc.getUndefShort();
	private short startBLERelStartSec = Misc.getUndefShort();
	private short endBLERelStartSec = Misc.getUndefShort();
	private short startLoadEventRelStartSec = Misc.getUndefShort();
	private short loadStGpsStart = Misc.getUndefShort();
	private short endLoadEventRelStartSec = Misc.getUndefShort();
	private short endActLoadRelStartSec = Misc.getUndefShort();
	private short firstMatchingStrikeRelStartSec = Misc.getUndefShort();
	private short lastMatchingStrikeRelStartSec = Misc.getUndefShort();
	private short baseActStartRelStartSec = Misc.getUndefShort();
	private short baseActEndRelStartSec = Misc.getUndefShort();
	private byte loadActivityStrikeMatchCount = 0;
	private byte cycleCount = 0;
	private byte baseCycleCount = 0;
	private boolean prevPtFromBLE = false;
	private byte currLeftInCount = 0;
	private byte currRightInCount = 0;
	private byte avgDur = 0;
	private byte deviation = 0;
	public byte multShoveFeasibleCnt = 0;
	private byte dist5;
	private boolean hasMultipleSubWindow = false;
	private boolean stopEnded = false;
	private boolean hasInactivityAfterBefStop = false;
	private boolean hasValidExcEventsAroundPeriod = false;
	
	
	private byte[] durs = null;
	
	public boolean hasInactivityAfterBefStop() {
		return hasInactivityAfterBefStop;
	}
	public void setHasInactivityAfterBefStop(boolean v) {
		this.hasInactivityAfterBefStop = v;
	}
	public void setDurs(byte[] durs) {
		this.durs = durs;
	}
	public boolean hasMultipleSubWindow() {
		return hasMultipleSubWindow;
	}
	public void setHasMultipleSubWindow(boolean v) {
		this.hasMultipleSubWindow = v;
	}
	public byte[] getDurs() {
		return this.durs;
	}
	public boolean modifyTimingAsPerWindow(long leftIncl, long rightIncl) {
		short leftSec = this.getSecGap(leftIncl);
		short rightSec = this.getSecGap(rightIncl);
		int leftGap = leftSec - this.startLoadEventRelStartSec;
		int rightGap = this.endLoadEventRelStartSec - rightSec;
		int leftIndex = this.currLeftInCount;
		int rightIndex = this.durs == null ? -1 : this.durs.length-1-this.currRightInCount;
		boolean modified = false;
		for (;leftIndex<rightIndex && leftGap > 0;leftIndex++) {
			int dur = this.durs[leftIndex+1];
			leftGap -= dur;
			modified = true;
		}
		for (;rightIndex > leftIndex && rightGap > 0;rightIndex--) {
			int dur = this.durs[rightIndex];
			rightGap -= dur;
			modified = true;
		}
		if (!modified)
			return true;
		if (this.cycleCount < ShovelSequence.MIN_SHOVEL_CYCLES && modified)
			return false;
		
		int adjCnt = rightIndex-leftIndex+1;
		if (adjCnt < ShovelSequence.MIN_SHOVEL_CYCLES)
			return false;
		this.cycleCount = (byte) adjCnt;
		short cummStart = 0;
		short cummEnd = 0;
		for (int i=0;i<=rightIndex;i++) {
			byte dur = durs[i];
			if (i <= leftIndex)
				cummStart += dur;
			cummEnd += dur;
		}
		this.endLoadEventRelStartSec = (short) (this.startLoadEventRelStartSec+cummEnd);
		this.startLoadEventRelStartSec = (short) (this.startLoadEventRelStartSec + cummStart);
		MiscInner.TripleDoubleDoubleInt avgDev = getAvgDevMax(durs,leftIndex+1, rightIndex, true);
		this.avgDur = (byte) (avgDev.first > Byte.MAX_VALUE-1 ? Byte.MAX_VALUE : Math.round(avgDev.first));
		this.deviation = (byte) (avgDev.second > Byte.MAX_VALUE-1 ? Byte.MAX_VALUE : Math.round(avgDev.second));
		this.currLeftInCount = (byte) leftIndex;
		this.currRightInCount = (byte) (durs.length-1-rightIndex);//will be fixed

		this.updateGrossActLoadTiming();
		return true;
	}
	public void copyFrom(ShovelSequence rhs) {
		this.hasMultipleSubWindow = rhs.hasMultipleSubWindow;
		this.dumperBestQuality = rhs.dumperBestQuality;
		this.strikeCount = rhs.strikeCount;
		this.startStrikeRelStartSec = rhs.startStrikeRelStartSec;
		this.endStrikeRelStartSec = rhs.endStrikeRelStartSec;
		this.startBLERelStartSec = rhs.startBLERelStartSec;
		this.endBLERelStartSec = rhs.endBLERelStartSec;
		this.startLoadEventRelStartSec = rhs.startLoadEventRelStartSec;
		this.endLoadEventRelStartSec = rhs.endLoadEventRelStartSec;
		this.actStartTS = rhs.actStartTS;
		this.endActLoadRelStartSec = rhs.endActLoadRelStartSec;
		this.firstMatchingStrikeRelStartSec =rhs.firstMatchingStrikeRelStartSec;
		this.lastMatchingStrikeRelStartSec = rhs.lastMatchingStrikeRelStartSec;
		this.baseActStartRelStartSec = rhs.baseActStartRelStartSec;
		this.baseActEndRelStartSec =rhs.baseActEndRelStartSec;
		this.loadActivityStrikeMatchCount = rhs.loadActivityStrikeMatchCount = 0;
		this.cycleCount = rhs.cycleCount;
		this.baseCycleCount = rhs.baseCycleCount;
		this.currLeftInCount = rhs.currLeftInCount;
		this.currRightInCount = rhs.currRightInCount;
		this.avgDur = rhs.avgDur;
		this.deviation = rhs.deviation;
		this.multShoveFeasibleCnt = rhs.multShoveFeasibleCnt;
		this.hasValidExcEventsAroundPeriod = rhs.hasValidExcEventsAroundPeriod;
		this.hasInactivityAfterBefStop = rhs.hasInactivityAfterBefStop;
		this.durs = null;
		if (rhs.durs != null && rhs.durs.length != 0) {
			this.durs = new byte[rhs.durs.length];
			for (int i=0,is=rhs.durs.length;i<is;i++)
				this.durs[i] = rhs.durs[i];
		}
			
	}
	public boolean isPrevPtFromBLE() {
		return prevPtFromBLE;
	}
	public void setPrevPtFromBLE(boolean v) {
		this.prevPtFromBLE = v;
	}
	
	public void setDist(double d) {
		int dist = (int)Math.round(d*1000);
		if (dist > Byte.MAX_VALUE)
			this.dist5 = Byte.MAX_VALUE;
		else
			this.dist5 = (byte) dist;
	}
	public byte getDist5() {
		return this.dist5;
	}
	public static int getDistComparison(byte d1, byte d2) {
		return d1 < d2-23 ? -1 : d1 > d2+23 ? 1 : 0;
	}
	public int getDistComparison(byte d2) {
		return getDistComparison(this.dist5, d2);
	}
	public int getDistComparison(ShovelSequence d2) {
		return getDistComparison(this.dist5, d2.dist5);
	}
	
	public byte isMultShoveFeasible() {
		return multShoveFeasibleCnt;
	}
	public void setMultShoveFeasible(byte multShoveFeasible) {
		this.multShoveFeasibleCnt = multShoveFeasible;
	}
	public boolean isStopEnded() {
		return stopEnded;
	}
	public void setStopEnded(boolean stopEnded) {
		this.stopEnded = stopEnded;
	}
	public boolean isHasValidExcEventsAroundPeriod() {
		return hasValidExcEventsAroundPeriod;
	}
	public void setHasValidExcEventsAroundPeriod(boolean hasValidExcEventsAroundPeriod) {
		this.hasValidExcEventsAroundPeriod = hasValidExcEventsAroundPeriod;
	}
	
	public byte getCurrLeftInCount() {
		return currLeftInCount;
	}
	public void setCurrLeftInCount(byte currLeftInCount) {
		this.currLeftInCount = currLeftInCount;
	}
	public byte getCurrRightInCount() {
		return currRightInCount;
	}
	public void setCurrRightInCount(byte currRightInCount) {
		this.currRightInCount = currRightInCount;
	}
	
	public byte getAvgDur() {
		return avgDur;
	}
	public void setAvgDur(byte avgDur) {
		this.avgDur = avgDur;
	}
	
	public short getLoadStGpsStart() {
		return loadStGpsStart;
	}
	public void setLoadStGpsStart(short loadStGpsStart) {
		this.loadStGpsStart = loadStGpsStart;
	}
	public void resetBaseAct() {
		this.baseActEndRelStartSec = Misc.getUndefShort();
		this.baseActEndRelStartSec = Misc.getUndefShort();
	}
	public void setBaseAct() {
		this.baseActStartRelStartSec =this.getSecGap(this.getActStartTS());
		this.baseActEndRelStartSec = this.endActLoadRelStartSec;
	}
	public short getBaseActStartRelSec() {
		return this.baseActStartRelStartSec;
	}
	public long getBaseActStartTS() {
		return Misc.isUndef(baseActStartRelStartSec) ? this.startTS  : this.startTS + 1000*this.baseActStartRelStartSec;
	}
	public long getBaseActEndTS() {
		return Misc.isUndef(baseActEndRelStartSec) ? this.endTS  : this.startTS + 1000*this.baseActEndRelStartSec;
	} 

	public static final long G_NON_OVERLAP_MILLI_FOR_LOAD = 110*1000;
	public static final int  BLE_LEFT_GRACE_SEC = 40;
	public static final int BLE_RIGHT_GRACE_SEC = 90;
	public final static int MAX_STRIKE_GAP_PRIOR_OK = -1;//only after
	public final static int MAX_STRIKE_GAP_AFTER_OK = 4;
	public final static int MAX_GUARD_GAP_MILLI = 15*1000;
	public final static long MAX_MARGIN_DIFF_DEVICE_SRC = 20*1000;
	public final static long MAX_MARGIN_SAME_DEVICE_SRC = 3*1000;
	public final static int MAX_LOADEVENT_PRIOR_STOP = 21;//was 10 before 20190108
	public final static int MAX_GAP_BETWEEN_IDLING_PT_BEF_ASSUMING_ACTIVE = 10*60*1000;
	public final static int MIN_GAP_BETWEEN_IDLINGPT_BEF_ASSUMINGACTIVE = 10*1000;
	public static final int PREFERRED_SHOVEL_CYCLES = 6;
	public static final int MIN_SHOVEL_CYCLES = 4; //5
	public static final int MIN_SHOVEL_LO_CYCLES = 2;//4
	public static final int MIN_SHOVEL_WITH_BLE = 2;
	public static final int MIN_SHOVEL_ONGOING = 2;
	public static final int MIN_SHOVEL_NOCYCLE_ONGOING_MINTS = 45*1000;
	public static double MIN_MAXGAP_AVG_RATIO_FOR_MISSING = 1.7;
	public static boolean isLoadEventOKRelStop(long loadEventTS, long stopTS,boolean strict) {
		return strict ? loadEventTS-stopTS >= 0  : loadEventTS-stopTS >= -MAX_LOADEVENT_PRIOR_STOP*1000;
	}
	
	public static boolean isStrikeOKRelLoadEvent(long loadEventTS, long strikeEventTS) {
		if (strikeEventTS == loadEventTS)
			return true;
		if (strikeEventTS < loadEventTS)
			return (loadEventTS-strikeEventTS) <= MAX_STRIKE_GAP_PRIOR_OK*1000;
		else
			return (strikeEventTS - loadEventTS) <= MAX_STRIKE_GAP_AFTER_OK*1000;
	}
	
	public static byte calcDumperQuality(int isBLE, boolean isStrike, boolean isLoadEvent, int loadActivityStrikeMatchCount) {
		int quality = 1;//we will at least be near
		
		if (isBLE > 0) {
			boolean trueBLE = isBLE > 1;
			if (loadActivityStrikeMatchCount > 1)
				quality = trueBLE ? ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH : ShovelSequence.LBLE_EXCLOAD_HIQSTRIKE_MATCH;
			else if (loadActivityStrikeMatchCount > 0)
				quality = trueBLE ? BLE_EXCLOAD_LOQSTRIKE_MATCH : ShovelSequence.LBLE_EXCLOAD_LOQSTRIKE_MATCH;
			else if (isStrike && isLoadEvent)
				quality = trueBLE ? BLE_EXCLOAD_STRIKE_NOMATCH : ShovelSequence.LBLE_EXCLOAD_STRIKE_NOMATCH;
			else if (isLoadEvent)//strike not there
				quality = trueBLE ? BLE_EXCLOAD_ONLY : ShovelSequence.LBLE_EXCLOAD_ONLY;
			else
				quality = trueBLE ? BLE_NOEXCLOAD_MAYBESTRIKE : ShovelSequence.LBLE_NOEXCLOAD_MAYBESTRIKE;
		}
		else {
			//do purely as near
			if (loadActivityStrikeMatchCount > 1)
				quality = ShovelSequence.NEAR_EXCLOAD_HIQSTRIKE_MATCH;
			
			else if (loadActivityStrikeMatchCount > 0)
				quality = NEAR_EXCLOAD_LOQSTRIKE_MATCH;
			else if (isStrike && isLoadEvent)
				quality = NEAR_EXCLOAD_STRIKE_NOMATCH;
			else if (isStrike)
				quality = NEAR_NOEXCLOAD_STRIKE_ONLY;
			else if (isLoadEvent)
				quality = NEAR_EXCLOAD_ONLY;
		}
		return (byte) quality;
	}
	public static boolean isBLEOverlapWithWindow(int bleStartSec, int bleEndSec, int actStartSec, int actEndSec) {
		if (!Misc.isUndef(bleStartSec) && !Misc.isUndef(actStartSec)) {
			int bleStartAdj = bleStartSec- (ShovelSequence.BLE_LEFT_GRACE_SEC);
			int bleEndAdj = bleEndSec+ (ShovelSequence.BLE_RIGHT_GRACE_SEC);
			if (bleEndAdj >= actStartSec && bleStartAdj <= actEndSec)
				return true;
		}
		return false;
	}
	public int updateDumperQuality(boolean ignoreBLESeen) {
		int isBLE = 	!Misc.isUndef(startBLERelStartSec) ? ignoreBLESeen ? 1 :  2 : this.likeBLE;
		short startActLoadSec = this.getSecGap(this.getActStartTS());//this.startActLoadRelStartSec
		boolean isStrike = !Misc.isUndef(startStrikeRelStartSec) && 
		    ShovelSequence.isTimeWindowPossiblyOverlapping(startActLoadSec, this.endActLoadRelStartSec, this.startStrikeRelStartSec, this.endStrikeRelStartSec,MAX_MARGIN_DIFF_DEVICE_SRC*1000);
		boolean isLoadEvent = !Misc.isUndef(startLoadEventRelStartSec);
		byte quality = calcDumperQuality(isBLE, isStrike, isLoadEvent, this.loadActivityStrikeMatchCount);
		this.dumperBestQuality = quality;
		return quality;
	}
	
	public void updateGrossActLoadTiming() {
		
       if (this.actStartTS <= 0) {//(Misc.isUndef(this.startActLoadRelStartSec)) {
    	   long adjStartTS = startTS;
   			long adjEndTS = endTS;
		   if (false && !Misc.isUndef(startBLERelStartSec)) {//instead we get load in stop to end and then adjust for ble read in window
				adjStartTS = Math.max(startTS, startTS+(startBLERelStartSec-BLE_LEFT_GRACE_SEC)*1000);
				adjEndTS = Math.min(endTS, startTS+(endBLERelStartSec+BLE_RIGHT_GRACE_SEC)*1000);
			}
			else {
				adjStartTS = startTS;
				adjEndTS = endTS;
			}
		   this.actStartTS = adjStartTS;
		   //this.startActLoadRelStartSec = (short)((adjStartTS-this.startTS)/1000);
		   this.endActLoadRelStartSec = this.getSecGap(adjEndTS);
       }
       if (!Misc.isUndef(this.startLoadEventRelStartSec)) {
    	   this.actStartTS = this.startTS+1000*this.startLoadEventRelStartSec;
    	   //this.startActLoadRelStartSec = this.startLoadEventRelStartSec;
    	   this.endActLoadRelStartSec = this.endLoadEventRelStartSec;
       }
     
	}
	public static boolean isTimeWindowPossiblyOverlapping(long tsStart1, long tsEnd1, long tsStart2, long tsEnd2, long margin) {
		return
		(tsEnd2 >= tsStart1-margin && tsEnd2 <= tsEnd1+margin) 
		|| (tsStart2 >= tsStart1-margin && tsStart2 <= tsEnd1+margin)
		|| (tsEnd1 >= tsStart2-margin && tsEnd1 <= tsEnd2+margin) 
		|| (tsStart1 >= tsStart2-margin && tsStart1 <= tsEnd2+margin)
		;
	}
	public static boolean isTimeWindowDefinitelyOverlapping(long tsStart1, long tsEnd1, long tsStart2, long tsEnd2, long margin) {
		return
				(tsEnd2 >= tsStart1+margin && tsEnd2 <= tsEnd1-margin) 
				|| (tsStart2 >= tsStart1+margin && tsStart2 <= tsEnd1-margin)
				|| (tsEnd1 >= tsStart2+margin && tsEnd1 <= tsEnd2-margin) 
				|| (tsStart1 >= tsStart2+margin && tsStart1 <= tsEnd2-margin)
		;
	}
	
	//private static MiscInner.TripleIntBoolBool getSuggestedMinAndAllowedLeftRight(long tsStart, int baseCount, short baseActStart, short baseActEnd, int currCount, short currActStart, short currActEnd) {
		//first: suggestedMin cycle, allowLeft - allow left to move in, allowRight - allow right to move in
		//int suggestedMin = 0;
		//boolean allowLeft
//	}
	public static long getSuitablePriorNotProper(long ts, long tsSeqStart, NewExcLoadEventMgmt loadEvents, Connection conn, int milliMaxLookBack) {
		ExcLoadEvent dummy = new ExcLoadEvent(ts);
		ExcLoadEvent bestPossiblePre = null;
		
		for (int i=-1;;i--) {
			ExcLoadEvent prev = loadEvents.get(conn, dummy,i);
			if (prev == null)
				break;
			long gap = ts-prev.getGpsRecordTime();
			if (prev.getGpsRecordTime() < tsSeqStart || prev.isProperLoad() || gap > milliMaxLookBack)
				break;
			if (gap < ExcLoadEvent.MIN_CYCLE_GAP_MS)
				continue;
			ExcLoadEvent.QInterpret qinfo = prev.getQInterpret();
			if (!qinfo.isOpenOPPattern() && !qinfo.isOpenOTPattern() && !qinfo.isUp())
				continue;
			if (bestPossiblePre == null)
				bestPossiblePre = prev;
			
			else if (prev.isMeBetterThan(bestPossiblePre)) {
				bestPossiblePre = prev;
			}
			if (qinfo.isOpenOPPattern() && qinfo.isOpenOTPattern() && qinfo.isUp())
				break;
		}
		return bestPossiblePre != null ? bestPossiblePre.getGpsRecordTime() : -1;
	}
	public boolean breakAbleCycleAfter(Connection conn, NewExcLoadEventMgmt loadEvents, NewVehicleData shovelIdling) {
		long actEndTS = this.getActEndTS();
		ExcLoadEvent dummy = new ExcLoadEvent(actEndTS);
		long aftTS =  0;
		for (int i=1;;i++) {
			ExcLoadEvent evt  = loadEvents.get(conn, dummy,i);
			if (evt == null)
				break;
			aftTS = evt.getGpsRecordTime();
			if (aftTS-actEndTS > ExcLoadEvent.MAX_CYCLE_GAP_MS || evt.isProperLoad())
				break;
		}
		if (aftTS != 0 && (aftTS-actEndTS) >= (this.avgDur+2.0*this.deviation)*1000)
			return true;
		
		if (false) {//dont worry about idling ..
			GpsData dummyGps = new GpsData(actEndTS);
			GpsData nexIdlingSt = null;
			for (int i=0;;i++) {
				GpsData temp = shovelIdling.get(conn, dummyGps,i);
				if (temp == null || temp.getGps_Record_Time()-actEndTS > ExcLoadEvent.MAX_CYCLE_GAP_MS) {
					break;
				}
				if (temp.getValue() > 0.5) {
					nexIdlingSt = temp;
					break;
				}
			}
			if (nexIdlingSt != null && nexIdlingSt.getGps_Record_Time() < aftTS) {
				aftTS = Long.MAX_VALUE;
			}
		}
		return (aftTS != 0 && this.cycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES && (aftTS-actEndTS) >= (this.avgDur+this.deviation)*1000);
	}
	public static MiscInner.TripleDoubleDoubleInt getAvgDevMax(byte[] durs, int start, int endIncl, boolean doTrueDev) {
		double durTot = 0;
		int max = Integer.MIN_VALUE;
		int cnt = 0;
		for (int i=start,is=endIncl;i<=is;i++) {
			int v = durs[i];
			if (v >= max) {
				max = v;
			}
			cnt++;
			durTot += v;
		}
		int cntOfMax = 0;
		for (int i=start,is=endIncl;i<=is;i++) {
			int v = durs[i];
			if (v == max) {
				cntOfMax++;
			}
		}
		
		double durTotExclMax = durTot-max*cntOfMax;
		int cntExclMax = cnt-cntOfMax;
		
		double avgD = durTot/cnt;
		double avgDExclMax = cntExclMax > 0 ? durTotExclMax/cntExclMax : 0;;
		boolean doByExclMax = false;//(max > 1.7 * avgDExclMax || max >= 45) && cntOfMax == 1;
		if (doByExclMax)
			avgD =  avgDExclMax;
		double deviation = (0.2)*avgD;
		double meanSqr = 0;
		int sqrcnt = 0;
		for (int i=start,is=endIncl;i<=endIncl;i++) {
			int v = durs[i];
			if (!doByExclMax || v != max) { 
				meanSqr += (v-avgD)*(v-avgD);
				sqrcnt++;
			}
		}
		deviation = sqrcnt == 1 ? 0 : Math.sqrt(meanSqr/sqrcnt);
		return new MiscInner.TripleDoubleDoubleInt(avgD, deviation, max);
	}
	
	public static class ShovelActivityInfo {
		long tsStartOverAll;
		long tsEndInclOverAll;
		short startLoadRelStartSec = Misc.getUndefShort();
		short endLoadRelStartSec = Misc.getUndefShort();
		short baseStartLoadRelStartSec = Misc.getUndefShort();
		short baseEndLoadRelStartSec = Misc.getUndefShort();
		int count = 0;
		int baseCount = 0;
		int countInBLEWindow = 0;
		int strikeCount = 0; 
		int matchingStrikeCount = 0;
		int matchingStrikeSmallestAbsGapSec = Integer.MAX_VALUE;
		short firstMatchingStrikeRelStartSec = Misc.getUndefShort();
		short lastMatchingStrikeRelStartSec = Misc.getUndefShort();
		byte dumperQuality = 0;
		ArrayList<Integer> durs = new ArrayList<Integer>(); //durs(i) is the dur between i-1 and i
		byte currLeftInCount = 0;
		byte currRightInCount = 0;
		byte avgDur = 0;
		byte deviation = 0;
		byte canGoFromLeftTillHereThoughLoCycle = -1;
		boolean feasibleCountWise = false;
		byte firstNotProperSecBefFirstProperLoad = 0;
		public static final boolean doTrueDev = true;
		
		public void setLikelyStartEnd(long seqStart, short bleStartSec, short bleEndSec, NewExcLoadEventMgmt loadEvents, Connection conn) {
			int bestEndIndex = -1;
			int bestStartIndex = -1;
			int bestCount = 0;
			int bestBleWindowCount = 0;
			
			int currIndex = 0;
			int currCount = 0;
			double breakThresh = (this.avgDur+(doTrueDev ? 1:1)*this.deviation);
			
			int prevEndIndex = durs.size()-1;
			boolean oldHasProperBLE = this.countInBLEWindow >= ShovelSequence.MIN_SHOVEL_WITH_BLE;
			ArrayList<Integer> cycleCountInBLEWindow = oldHasProperBLE ? new ArrayList<Integer>() : null;
			ArrayList<Integer> cummDurs = null;
			if (false) {
				cummDurs = new ArrayList<Integer>();
				int tot = 0;
				for (int i=0,is=durs.size();i<is;i++) {
					tot += durs.get(i);
					cummDurs.add(tot);
				}
			}
			if (oldHasProperBLE) {
				cycleCountInBLEWindow = new ArrayList<Integer>();
				int base = this.startLoadRelStartSec;
				int windowCycleCount = 0;
				for (int i=0,is=durs.size();i<is;i++) {
					base += durs.get(i);
					if (base >= bleStartSec && base <= bleEndSec)
						windowCycleCount++;
					cycleCountInBLEWindow.add(windowCycleCount);
				}
			}
			
			long leftBoundaryIfLoCnt=0;
			long bestLeftBoundaryIfLoCnt = 0;
			for (int i=durs.size()-1;i>=0;i--) {
				int dur = durs.get(i);
				currCount++;
				if (dur >= breakThresh || i == 0) {
					
					if (currCount >= ShovelSequence.MIN_SHOVEL_CYCLES) {//20190126 was MIN_SHOVEL_CYCLES
						int bleActivityInThisWindow =oldHasProperBLE ? cycleCountInBLEWindow.get(prevEndIndex) - (i == 0 ? 0 : cycleCountInBLEWindow.get(i-1))
								: 0;
						boolean isMeProperBleWise = bleActivityInThisWindow >= ShovelSequence.MIN_SHOVEL_WITH_BLE;
						boolean cycCountProper = true;
						//below not happen because of currCout >= Min_shovel_cycles;
						if (currCount >= ShovelSequence.MIN_SHOVEL_LO_CYCLES && currCount < ShovelSequence.MIN_SHOVEL_CYCLES) {//not used
							isMeProperBleWise =  bleActivityInThisWindow >= MIN_SHOVEL_WITH_BLE-1;
							//check if feasibke to extend to left
							leftBoundaryIfLoCnt = i ==0? 0: ShovelSequence.getSuitablePriorNotProper(seqStart+1000*(this.startLoadRelStartSec+cummDurs.get(i)), seqStart-MAX_LOADEVENT_PRIOR_STOP*1000, loadEvents, conn, (int) (this.avgDur*1000.0*1.3));
							   //at i ==0 alreadyChecked for left boundary and accordingly adjusted
							if (leftBoundaryIfLoCnt <= 0) {
								cycCountProper = false;
							}
							else {
								bleActivityInThisWindow++;
							}
						}
						else {
							leftBoundaryIfLoCnt = 0;
						}
						if (cycCountProper) {
							if ((bestEndIndex < 0 || isMeProperBleWise)) { 
								bestStartIndex = i;
								bestEndIndex = prevEndIndex;
								bestCount = currCount;
								bestBleWindowCount = bleActivityInThisWindow;
								bestLeftBoundaryIfLoCnt = leftBoundaryIfLoCnt;
							}
							if (cycCountProper && (isMeProperBleWise || !oldHasProperBLE))
								break;
						}
					}
					prevEndIndex = i-1;
					currCount = 0;
				}
			}
			//below not used because our lo lt at MIN_SHOVEL 
			if (bestEndIndex >= 0 && bestStartIndex >= 0 && bestCount >= ShovelSequence.MIN_SHOVEL_LO_CYCLES && bestCount < ShovelSequence.MIN_SHOVEL_CYCLES && bestLeftBoundaryIfLoCnt > 0) {
				//adjust dur etc .. will invalidate cummDurs
				long tsOfOldCycle = seqStart+1000*(this.startLoadRelStartSec+cummDurs.get(bestStartIndex));
				
				int oldDur = durs.get(bestStartIndex);
				int newDurRelNewEvt = (int)((tsOfOldCycle -   bestLeftBoundaryIfLoCnt)/1000);
				if (newDurRelNewEvt > oldDur) //should not happen
					newDurRelNewEvt = oldDur;
				durs.set(bestStartIndex, newDurRelNewEvt);
				durs.add(bestStartIndex, oldDur - newDurRelNewEvt);
				bestEndIndex++;
				bestCount++;
				this.count++;
				cummDurs = null; //cause exception
			}
			if (bestEndIndex < 0 || bestStartIndex < 0) {
				if (bestEndIndex < 0)
					bestEndIndex = durs.size()-1;
				if (bestStartIndex < 0)
					bestStartIndex = 0;
				bestCount = bestEndIndex-bestStartIndex+1;
				bestBleWindowCount = oldHasProperBLE ? cycleCountInBLEWindow.get(bestEndIndex) - (bestStartIndex == 0 ? 0 : cycleCountInBLEWindow.get(bestStartIndex-1))
						: 0;
			}
			if (bestStartIndex > 0 || bestEndIndex < durs.size()-1) {
				int cummStartSec = 0;
				int cummEndSec = 0;
				for (int i=0,is=bestEndIndex;i<=is;i++) {
					int dur = durs.get(i);
					if (i <= bestStartIndex)
						cummStartSec += dur;
					cummEndSec += dur;
				}
				this.endLoadRelStartSec = (short) (this.startLoadRelStartSec + cummEndSec);
				this.startLoadRelStartSec += (short) cummStartSec;
				this.baseCount = this.count;
				this.count = bestCount;
				this.countInBLEWindow = bestBleWindowCount;
				this.currLeftInCount = (byte)(bestStartIndex);
				this.currRightInCount = (byte)(durs.size()-1-bestEndIndex);
				MiscInner.TripleDoubleDoubleInt avgDev = getAvgDevMax(durs,bestStartIndex+1, bestEndIndex, doTrueDev);
				this.avgDur = (byte) (avgDev.first > Byte.MAX_VALUE-1 ? Byte.MAX_VALUE : Math.round(avgDev.first));
				this.deviation = (byte) (avgDev.second > Byte.MAX_VALUE-1 ? Byte.MAX_VALUE : Math.round(avgDev.second));
			}
		}
		
		public void setLikelyStartEndMustBreak(long seqStart, short bleStartSec, short bleEndSec) {
			int bestEndIndex = -1;
			int bestStartIndex = 0;
			int bestCount = 0;
			int bestBleWindowCount = 0;
			boolean bestBLEProper = false;
			MiscInner.TripleDoubleDoubleInt avgEtc = this.getAvgDevMax(durs, 0, durs.size()-1, true);
			double breakThresh =2*(avgEtc.first)+(avgEtc.second);// (this.avgDur+(doTrueDev ? 1.5: 2.0)*this.deviation);
			boolean oldHasProperBLE = this.countInBLEWindow >= ShovelSequence.MIN_SHOVEL_WITH_BLE;
			ArrayList<Integer> cycleCountInBLEWindow = oldHasProperBLE ? new ArrayList<Integer>() : null;
			if (oldHasProperBLE) {
				cycleCountInBLEWindow = new ArrayList<Integer>();
				int base = this.startLoadRelStartSec;
				int windowCycleCount = 0;
				for (int i=0,is=durs.size();i<is;i++) {
					base += durs.get(i);
					if (base >= bleStartSec && base <= bleEndSec)
						windowCycleCount++;
					cycleCountInBLEWindow.add(windowCycleCount);
				}
			}
			int currCount = 0;
			int currStartIndex = 0;
			for (int i=0,is=this.durs.size()-1;i<=is;i++) {
				int dur = durs.get(i);
				if (dur > breakThresh || i == is) {
					int bleActivityInThisWindow =oldHasProperBLE ? 
							cycleCountInBLEWindow.get(i-1) - (currStartIndex == 0 ? 0 :cycleCountInBLEWindow.get(currStartIndex-1))
							: 0
						;
					boolean meBLEProper = 	bleActivityInThisWindow >= ShovelSequence.MIN_SHOVEL_WITH_BLE;
					if ((currCount >= bestCount && meBLEProper == bestBLEProper) || (meBLEProper && !bestBLEProper) ) {
						bestStartIndex = currStartIndex;
						bestEndIndex = i-1;
						bestCount = currCount;
						bestBLEProper = meBLEProper;
						bestBleWindowCount = bleActivityInThisWindow;
					}
					currStartIndex = i;
					currCount = 0;
				}
				currCount++;
			}
			if (bestStartIndex != 0 || bestEndIndex !=this.durs.size()-1) {
				int cummStartSec = 0;
				int cummEndSec = 0;
				for (int i=0,is=bestEndIndex;i<=is;i++) {
					int dur = durs.get(i);
					if (i <= bestStartIndex)
						cummStartSec += dur;
					cummEndSec += dur;
				}
				this.endLoadRelStartSec = (short) (this.startLoadRelStartSec + cummEndSec);
				this.startLoadRelStartSec += (short) cummStartSec;
				this.count = bestCount;
				this.countInBLEWindow = bestBleWindowCount;
				
				if (bestStartIndex != 0) {
					for (int i=bestStartIndex;i<=bestEndIndex;i++)
						this.durs.set(i-bestStartIndex, durs.get(i));
				}
				for (int i=durs.size()-1,is=bestCount;i>is;i--)
					durs.remove(i);
			}
		}
		public static MiscInner.TripleDoubleDoubleInt getAvgDevMax(ArrayList<Integer> durs, int start, int endIncl, boolean doTrueDev) {
			double durTot = 0;
			int max = Integer.MIN_VALUE;
			int cnt = 0;	
			for (int i=start,is=endIncl;i<=is;i++) {
				int v = durs.get(i);
				if (v >= max) {
					max = v;
				}
				durTot += v;
				cnt++;
			}
			int cntOfMax = 0;
			for (int i=start,is=endIncl;i<=is;i++) {
				int v = durs.get(i);
				if (v == max) {
					cntOfMax++;
				}
			}
			
			
			double durTotExclMax = durTot-max*cntOfMax;
			int cntExclMax = cnt-cntOfMax;
			
			double avgD = durTot/cnt;
			double avgDExclMax = cntExclMax > 0 ? durTotExclMax/cntExclMax : 0;;
			boolean doByExclMax = false;//(max > 1.7 * avgDExclMax || max > 45) && cntOfMax == 1;
			if (doByExclMax)
				avgD =  avgDExclMax;
			double deviation = (0.2)*avgD;
			double meanSqr = 0;
			int sqrcnt = 0;
			for (int i=start,is=endIncl;i<=endIncl;i++) {
				int v = durs.get(i);
				if (!doByExclMax || v != max) { 
					meanSqr += (v-avgD)*(v-avgD);
					sqrcnt++;
				}
			}
			deviation = sqrcnt == 1 ? 0 : Math.sqrt(meanSqr/sqrcnt);
			return new MiscInner.TripleDoubleDoubleInt(avgD, deviation, max);
		}
		
		public void setAvgDurLeftNonProperIfNeededAndNaturalBreaks(long seqStart, long seqEnd, short bleStartSec, short bleEndSec, NewExcLoadEventMgmt loadEvents, Connection conn, boolean dontExtendForImproper, boolean isStopEnded) {
			
			//setLikelyStartEndMustBreak(seqStart, bleStartSec, bleEndSec);
			if ((this.count >= ShovelSequence.MIN_SHOVEL_LO_CYCLES) || (!isStopEnded && this.count >= ShovelSequence.MIN_SHOVEL_ONGOING))
				this.feasibleCountWise = true;
			
			long cycEnd = seqStart + 1000*this.endLoadRelStartSec;
			if (seqEnd-cycEnd > ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS) {
				this.feasibleCountWise = false;
				return;
			}
			MiscInner.TripleDoubleDoubleInt avgDevMax = getAvgDevMax(durs, 1, durs.size()-1, doTrueDev);
			double avgD = avgDevMax.first;
			double deviation = avgDevMax.second;
			this.currLeftInCount = this.currRightInCount = 0;

			
			
			if (avgD > 127)
				avgD = 126.01;
			else if (avgD < 1)
				avgD = 0.99;
			this.avgDur = (byte) Math.round(avgD);
			if (deviation > 127)
				deviation = 126.01;
			else if (deviation < 1)
				deviation = 0.99;
			this.deviation = (byte) Math.round(deviation);
			long leftBoundary = ShovelSequence.getSuitablePriorNotProper(seqStart+1000*this.startLoadRelStartSec, seqStart-MAX_LOADEVENT_PRIOR_STOP*1000, loadEvents, conn, (int) (avgD*1000.0*1.3));
			if (leftBoundary <= 0) {
				if (count < ShovelSequence.MIN_SHOVEL_CYCLES && isStopEnded )
					this.feasibleCountWise = false;
			}
			else { //we have possibility to steal a loadStart from left
				int leftStartPossibleSec = (int)((leftBoundary-seqStart)/1000);
				if (!dontExtendForImproper) {
					
					this.firstNotProperSecBefFirstProperLoad = (byte)(this.startLoadRelStartSec-leftStartPossibleSec);
					this.durs.set(0, this.startLoadRelStartSec-leftStartPossibleSec);
					this.durs.add(0, 0);
					this.baseStartLoadRelStartSec = this.startLoadRelStartSec = (short)leftStartPossibleSec;
					this.count++;
				}
			}
			
		}
		
		byte setAndGetDumperQuality(int isBLE) {
			this.dumperQuality = calcDumperQuality(isBLE, strikeCount > 0, !Misc.isUndef(startLoadRelStartSec), matchingStrikeCount);
			return this.dumperQuality;
		}
		byte getDumperQuality() {
			return dumperQuality;
		}
		
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			java.util.Date dt1 = new java.util.Date(tsStartOverAll);
			java.util.Date dt2 = new java.util.Date(tsEndInclOverAll);
			sb.append("St:").append(dt1).append(" End:").append(dt2).append(" startLoadSec:").append(startLoadRelStartSec).append(" endLoad:").append(this.endLoadRelStartSec).append(" Count:").append(count).append(" StrikeCnt:").append(this.strikeCount).append(" MatchStrike:").append(this.matchingStrikeCount);
			return sb.toString();
		}
	}
	public int meBetterThanActivity(ShovelActivityInfo currActivityInfo) {
		//1 if me better, -1 if worse, 0 same
		int toSwap = currActivityInfo == null ? 1 : 0;
		if (toSwap == 0) {
			byte meQ = this.getDumperBestQuality();
			byte bestQ = currActivityInfo.getDumperQuality();
			if (toSwap == 0)
				toSwap = meQ > bestQ ? 1 : meQ < bestQ ? -1 : 0;
		}
		return toSwap;
	}
	public void quiclResetToInit() {
		this.cycleCount = 0;
		this.setStartLoadEventRelStartSec(Misc.getUndefShort());
		this.setEndLoadEventRelStartSec(Misc.getUndefShort());
		this.setActStartTS(0);
		this.setEndActLoadRelStartSec(Misc.getUndefShort());
		this.resetBaseAct();
		this.currLeftInCount = 0;
		this.currRightInCount = 0;
		this.updateGrossActLoadTiming();
		this.updateDumperQuality(false);
	}
	public void quickResetToBase() {
		this.cycleCount = this.baseCycleCount;
		this.startLoadEventRelStartSec = this.baseActEndRelStartSec;
		this.endActLoadRelStartSec = this.baseActEndRelStartSec;
		this.actStartTS = this.startTS + 1000*this.baseActStartRelStartSec;
		//this.startActLoadRelStartSec = this.baseActStartRelStartSec;
		this.endActLoadRelStartSec = this.baseActEndRelStartSec;
		this.currLeftInCount = 0;
		this.currRightInCount = 0;
	}
	public void setThisTimingToShovelActivityInfo(ShovelActivityInfo bestActivityInfo, boolean bestActivityInfoIndexNotAtEnd, boolean doLikelyStartEnd, boolean doBaseStartEnd, Connection conn, NewExcLoadEventMgmt loadEvents, NewVehicleData shovelIdling, boolean toCalcNewStrike) {
		if (bestActivityInfo.count == 0) {
			bestActivityInfo.startLoadRelStartSec = bestActivityInfo.baseStartLoadRelStartSec = 0;
			bestActivityInfo.endLoadRelStartSec = bestActivityInfo.baseEndLoadRelStartSec = -1;
		}
		else if (doLikelyStartEnd) {
			bestActivityInfo.setLikelyStartEnd(this.getStartTS(), (short)(this.getStartBLERelStartSec()-ShovelSequence.BLE_LEFT_GRACE_SEC), (short)(this.getEndBLERelStartSec()+ShovelSequence.BLE_RIGHT_GRACE_SEC),loadEvents,conn);
		}
		this.cycleCount = (byte) bestActivityInfo.count;
		
		
		this.startLoadEventRelStartSec = bestActivityInfo.startLoadRelStartSec;
		this.endLoadEventRelStartSec = bestActivityInfo.endLoadRelStartSec;
		if (toCalcNewStrike) {
			this.firstMatchingStrikeRelStartSec = bestActivityInfo.firstMatchingStrikeRelStartSec;
			this.lastMatchingStrikeRelStartSec = bestActivityInfo.lastMatchingStrikeRelStartSec;
			if (this.firstMatchingStrikeRelStartSec < this.startStrikeRelStartSec)
				this.startStrikeRelStartSec = this.firstMatchingStrikeRelStartSec;
			if (this.lastMatchingStrikeRelStartSec > this.endStrikeRelStartSec)
				this.endStrikeRelStartSec = this.lastMatchingStrikeRelStartSec;
			
			this.loadActivityStrikeMatchCount = (byte)(bestActivityInfo.matchingStrikeCount > 15 ? 15 : bestActivityInfo.matchingStrikeCount);
		}
		if (DO_TIMING_ADJUSTMENT_APPROACH != 0) {
			this.currLeftInCount = bestActivityInfo.currLeftInCount;
			this.currRightInCount = bestActivityInfo.currRightInCount;
			this.avgDur = bestActivityInfo.avgDur;
			this.deviation = bestActivityInfo.deviation;
		}
		this.updateGrossActLoadTiming();
		if (doBaseStartEnd) {
			this.baseActStartRelStartSec = bestActivityInfo.baseStartLoadRelStartSec;
			this.baseActEndRelStartSec = bestActivityInfo.baseEndLoadRelStartSec;
			this.baseCycleCount = (byte) bestActivityInfo.baseCount;
		}
		
		this.updateDumperQuality(bestActivityInfo.getDumperQuality() < ShovelSequence.LEAST_BLE_INDICATOR_Q);
		if (bestActivityInfo.durs != null && bestActivityInfo.durs.size() > 0) {
			this.durs = new byte[bestActivityInfo.durs.size()];
			for (int i=0,is = bestActivityInfo.durs.size();i<is;i++)
				this.durs[i] = (bestActivityInfo.durs.get(i).byteValue());
		}
	}
	public boolean startShiftableToRight(int index, byte thresh) {
		if (true)
			return true;
		int sz = durs.length;
		int idx1 = index+1;
		int idx2 = index+2;
		return index >= 0 && ((idx1 < sz && durs[idx1] >= thresh) || (idx2 < sz && durs[idx2] >= thresh)); 
	}
	public boolean endShiftableToLeft(int index, byte thresh) {
		if (true)
			return true;
		int sz = durs.length-1;
		int idx1 = sz-index;
		int idx2 = idx1-1;
		return index >= 0 && index <=sz && ((idx1 >=0 && durs[idx1] >= thresh) || (idx2 >= 0 && durs[idx2] >= thresh));
	}
	
	public boolean updateWithLoadEvent(Connection conn, NewVehicleData shovelIdling, NewVehicleData strikeVDT, NewExcLoadEventMgmt loadEvents, NewBLEMgmt bleReads, boolean strictBound, boolean doRHSEndCheck, boolean doBaseStartEnd, boolean doLikelyStartEnd, ArrayList<ShovelActivityInfo> otherAltShovelActivityInfoExclBest) {
		//first = whether feasible or not, 2nd = alt shovelActicityInfo that could be feasible
		//doLikelyStartEnd = false;
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		
		if (otherAltShovelActivityInfoExclBest != null)
			otherAltShovelActivityInfoExclBest.clear();
		boolean rhsAlreadyShifted = !Misc.isUndef(this.baseActStartRelStartSec) && !Misc.isUndef(this.startLoadEventRelStartSec) && this.startLoadEventRelStartSec > this.baseActStartRelStartSec;
		//init
			this.cycleCount = 0;
			this.startLoadEventRelStartSec = Misc.getUndefShort();
			this.endLoadEventRelStartSec = Misc.getUndefShort();
			boolean toCalcNewStrike = strikeVDT != null;
			if (toCalcNewStrike) {
				this.firstMatchingStrikeRelStartSec = Misc.getUndefShort();
				this.lastMatchingStrikeRelStartSec = Misc.getUndefShort();
				
				
				this.loadActivityStrikeMatchCount = 0;
				this.strikeCount = 0;
			}
			//init
		long milliUnAv = 0;
		int startLoadSec = Misc.getUndefInt();
		int endLoadSec = Misc.getUndefInt();
		long tsStart =this.getActStartTS();
		long tsEnd = Misc.isUndef(this.endActLoadRelStartSec) ? this.endTS : this.endActLoadRelStartSec*1000 + this.startTS;
		if (tsStart == tsEnd) {
			tsStart = tsEnd = this.startTS;
		}
		short initStartLoadSec = this.getSecGap(tsStart);
		if (doBaseStartEnd) {
			this.baseActStartRelStartSec = this.getSecGap(this.getActStartTS());
			this.baseActEndRelStartSec = this.endActLoadRelStartSec;
		}
		long bleStartGrace = 0;
		long bleEndGrace = 0;
		
		if (!Misc.isUndef(this.startBLERelStartSec)) {
			bleStartGrace = this.startTS+1000*(this.startBLERelStartSec-ShovelSequence.BLE_LEFT_GRACE_SEC);
			bleEndGrace = this.startTS+1000*(this.endBLERelStartSec+ShovelSequence.BLE_RIGHT_GRACE_SEC);
		}
		GpsData refIdlingPt = new GpsData(tsStart);
		GpsData prev = null;
		long strikeStartTS = Misc.getUndefInt();
		long strikeEndTS = Misc.getUndefInt();
		short strikeStartRelStart = this.startStrikeRelStartSec;
		short strikeEndRelStart = this.endStrikeRelStartSec;
		if (!Misc.isUndef(this.startStrikeRelStartSec)) {
			strikeStartTS = this.startTS + 1000*this.startStrikeRelStartSec;
			strikeEndTS = this.startTS + 1000*strikeEndRelStart;
		}
		
		ArrayList<ShovelActivityInfo> nonidlingInfo = new ArrayList<ShovelActivityInfo>();
		long startOfIdling = -1;
		ShovelActivityInfo currActivityInfo = null;
		
		int idlingPtCount = 0;
		for(int i=0;;i++) {
			GpsData curr = shovelIdling.get(conn, refIdlingPt,i);
			if (i == 0 && curr == null) {
				currActivityInfo = new ShovelActivityInfo();
				currActivityInfo.tsStartOverAll = tsStart;
				nonidlingInfo.add(currActivityInfo);
				continue;
			}
			if (curr == null)
				break;
			
			boolean isIdling = curr.getValue() > 0.5;
			idlingPtCount++;
			if (isIdling) {
				GpsData next = shovelIdling.get(conn, refIdlingPt, i+1);
				
				long tsToCmp = next == null ? (tsStart > curr.getGps_Record_Time() ? tsStart : tsEnd)
						: next.getGps_Record_Time()//(curr.isFWPoint() ? curr.getGps_Record_Time() : next.getGps_Record_Time())
						;
				
				long tempGap = tsToCmp - curr.getGps_Record_Time();
				isIdling = tempGap < MAX_GAP_BETWEEN_IDLING_PT_BEF_ASSUMING_ACTIVE && tempGap >= MIN_GAP_BETWEEN_IDLINGPT_BEF_ASSUMINGACTIVE;
				
			}
			
			
			if (curr.getGps_Record_Time() > tsEnd || (this.getEndTS()-curr.getGps_Record_Time()) < ExcLoadEvent.MIN_ENDCYCLE_STOPEND_MS) {
				break;
			}
			if (!isIdling) {
				
				if (currActivityInfo == null) {
					currActivityInfo = new ShovelActivityInfo();
					currActivityInfo.tsStartOverAll = Math.max(curr.getGps_Record_Time(), tsStart);
					nonidlingInfo.add(currActivityInfo);
				}
				if (startOfIdling > 0) {
					//AFTER validation .. make margin to be lower because idling and strike are generated on demand
					if (false && isTimeWindowDefinitelyOverlapping(startOfIdling, curr.getGps_Record_Time(), strikeStartTS, strikeEndTS, ShovelSequence.MAX_MARGIN_DIFF_DEVICE_SRC))
						return false;
					milliUnAv += curr.getGps_Record_Time()-Math.max(startOfIdling, tsStart);
				}
				startOfIdling = -1;
			}
			else {
				//check if we have data loss etc
				if (startOfIdling <= 0)
					startOfIdling = curr.getGps_Record_Time();
				if (currActivityInfo != null) {
					currActivityInfo.tsEndInclOverAll = curr.getGps_Record_Time();
					currActivityInfo = null;
				}
			}
			prev = curr;
		}
		if (false && startOfIdling > 0 && toCalcNewStrike  &&  isTimeWindowDefinitelyOverlapping(startOfIdling, tsEnd, strikeStartTS, strikeEndTS, ShovelSequence.MAX_MARGIN_DIFF_DEVICE_SRC)) {
			return false;
		}
		if (currActivityInfo != null) {
			currActivityInfo.tsEndInclOverAll = tsEnd;
		}
		double avgDurCycle = loadEvents.getAvgDurMilli(conn,tsStart,tsEnd);
		long threshMilliBreakLongTrip = (long)(avgDurCycle*2);
		//if (threshMilliBreakLongTrip>60000)
		//	threshMilliBreakLongTrip = 60000;
		ExcLoadEvent refEvent = new ExcLoadEvent(tsStart);
		ExcLoadEvent prevEvent = null;
		ExcLoadEvent prevProperEvent = null;
		int currActivityInfoIndex = 0;
		long tsToCheckForStrikeLokadEventBelonging = (!strictBound && (tsStart < this.startTS)) ? this.startTS : tsStart;
		int loadEventPtCount = 0;
		GpsData prevStrikeMatch = null;
		ExcLoadEvent prevEventThatMatchedStrike = null;
		
		
		if (nonidlingInfo.size() != 0) {//some non idle seq exists
			currActivityInfo = nonidlingInfo.get(0);
			long prevStrikeMatchingCounted = -1;
			boolean toCreateBrandNewActivityInfo = false;
			
			for(int i=0;;i++) {
				ExcLoadEvent currEvent = loadEvents.get(conn, refEvent,i);
				if (i == 0 && currEvent == null) {
					prevEvent = currEvent;
					continue;
				}
				if (currEvent == null)
					break;
				loadEventPtCount++;
				if  ((this.getEndTS()-currEvent.getGpsRecordTime()) < ExcLoadEvent.MIN_ENDCYCLE_STOPEND_MS || currEvent.getGpsRecordTime() > tsEnd)
					break;
				byte ignoreCurrEvent = currEvent.getIgnoreBecauseNeighbour(conn, loadEvents);
					
				//if (currEvent.getGpsRecordTime() < currActivityInfo.tsStart) {
				//	prevEvent = currEvent;
				//	continue;
				//}
				if ((ignoreCurrEvent & ExcLoadEvent.EVENT_LEFT_IGNORE_MASK) != 0)
					toCreateBrandNewActivityInfo = true;
				
				boolean toIgnore = (ignoreCurrEvent & 0x0F) != 0;
				if (!toIgnore) {
					if (!isLoadEventOKRelStop(currEvent.getGpsRecordTime(), tsToCheckForStrikeLokadEventBelonging, strictBound)) {
						toIgnore = true;
					}
				}
				if (prevProperEvent != null && currEvent.getGpsRecordTime()-prevProperEvent.getGpsRecordTime() > threshMilliBreakLongTrip)
					toCreateBrandNewActivityInfo = true;
				if (!toIgnore) {
					
					//check if can add to same currActivity
					if (currEvent.getGpsRecordTime() > currActivityInfo.tsEndInclOverAll) {
						toCreateBrandNewActivityInfo = false;
						currActivityInfo = null;
						prevStrikeMatch = null;
						prevEventThatMatchedStrike = null;
						for (int sz = nonidlingInfo.size(); currActivityInfoIndex < sz; currActivityInfoIndex++) {
							ShovelActivityInfo temp = nonidlingInfo.get(currActivityInfoIndex);
							//if (temp.tsStartOverAll <= currEvent.getGpsRecordTime() && temp.tsEndInclOverAll >= currEvent.getGpsRecordTime()) {
							//bug event 08:29:00, while inactivity ends at 08:29:01
							if (temp.tsEndInclOverAll >= currEvent.getGpsRecordTime()) {
								currActivityInfo = temp;
								break;
							}
						}	
					}
				}
				if (currActivityInfo == null)
					break;
				if (toCreateBrandNewActivityInfo && !toIgnore) {
					toCreateBrandNewActivityInfo = false;
					prevStrikeMatch = null;
					prevEventThatMatchedStrike = null;
					ShovelActivityInfo newEntry = new ShovelActivityInfo();
					newEntry.tsStartOverAll = currEvent.getGpsRecordTime();
					newEntry.tsEndInclOverAll = currActivityInfo.tsEndInclOverAll;
					currActivityInfo.tsEndInclOverAll =  prevEvent == null ? -1 : prevEvent.getGpsRecordTime();
					if (currActivityInfoIndex == nonidlingInfo.size()-1)
						nonidlingInfo.add(newEntry);
					else
						nonidlingInfo.add(currActivityInfoIndex+1, newEntry);
					currActivityInfo = newEntry;
					currActivityInfoIndex++;
				}
				if ((ignoreCurrEvent & ExcLoadEvent.EVENT_RIGHT_IGNORE_MASK) != 0) {
					toCreateBrandNewActivityInfo = true;
				}
				
				if (toIgnore) {
					prevEvent = currEvent;
					continue;
				}
				
				if (currActivityInfo != null) {
					if (currActivityInfo.count == 0 || prevProperEvent == null) {
						currActivityInfo.durs.add(0);
					}
					else {
						currActivityInfo.durs.add((int)((currEvent.getGpsRecordTime()-prevProperEvent.getGpsRecordTime())/1000));
					}
					prevProperEvent = currEvent;
					currActivityInfo.count++;
					if (currEvent.getGpsRecordTime() >= bleStartGrace && currEvent.getGpsRecordTime() <= bleEndGrace) {
						currActivityInfo.countInBLEWindow++;
					}
					
					short secGap = (short)((currEvent.getGpsRecordTime()-this.startTS)/1000);	
					currActivityInfo.baseEndLoadRelStartSec = currActivityInfo.endLoadRelStartSec = secGap;
					if (Misc.isUndef(currActivityInfo.startLoadRelStartSec)) {
						currActivityInfo.baseStartLoadRelStartSec = currActivityInfo.startLoadRelStartSec = secGap;
					}
					GpsData refStrike = new GpsData(currEvent.getGpsRecordTime());
					GpsData strikePrev = IGNORE_STRIKE || strikeVDT == null ? null : strikeVDT.get(conn, refStrike);
					GpsData strikeNext = IGNORE_STRIKE || strikeVDT == null ? null : strikeVDT.get(conn, refStrike,1);
					boolean prevStrikeMatching = strikePrev != null && isStrikeOKRelLoadEvent(currEvent.getGpsRecordTime(),strikePrev.getGps_Record_Time());
					boolean nextStrikeMatching = strikeNext != null && isStrikeOKRelLoadEvent(currEvent.getGpsRecordTime(),strikeNext.getGps_Record_Time());
					//BASICALLY we want to identify consecutive matching strike and start time from first matching ... but if not matching
					//then first matching till last matching. In the latter case the timing is not important
					if (strikeNext != null && strikeNext.getGps_Record_Time() < currActivityInfo.tsEndInclOverAll+20*1000)
						this.strikeCount++;
					int prevStrikeMatchingCount = currActivityInfo.matchingStrikeCount;
					if (prevStrikeMatching || nextStrikeMatching) {
						
						if (nextStrikeMatching && strikePrev != null && prevStrikeMatch != null && strikePrev.getGps_Record_Time() == prevStrikeMatch.getGps_Record_Time())
							currActivityInfo.matchingStrikeCount++;
						else if (currActivityInfo.matchingStrikeCount == 0)
							currActivityInfo.matchingStrikeCount = 1;
						if (nextStrikeMatching || strikePrev.getGps_Record_Time() != prevStrikeMatchingCounted) {
							
							currActivityInfo.matchingStrikeSmallestAbsGapSec = Math.min(currActivityInfo.matchingStrikeSmallestAbsGapSec,(int) (nextStrikeMatching ? Math.abs((currEvent.getGpsRecordTime()-strikeNext.getGps_Record_Time())/1000)
									: Math.abs((currEvent.getGpsRecordTime()-strikePrev.getGps_Record_Time())/1000)))
									;
							prevStrikeMatchingCounted = nextStrikeMatching ? strikeNext.getGps_Record_Time() : strikePrev.getGps_Record_Time();
						}
						
						int gapMatchStartRel = (int)((currEvent.getGpsRecordTime() - this.startTS)/1000);
								
						if (gapMatchStartRel > Short.MAX_VALUE)
							gapMatchStartRel = Short.MAX_VALUE;
						if (gapMatchStartRel < Short.MIN_VALUE)
							gapMatchStartRel = Misc.getUndefShort()+2;
						if (prevStrikeMatchingCount < 2 && currActivityInfo.matchingStrikeCount == 2) {//first time consecutive match .. so start from prevExcEvent
							int gapMatchStartRelBeg = (int)((prevEventThatMatchedStrike.getGpsRecordTime() - this.startTS)/1000);
							if (gapMatchStartRelBeg > Short.MAX_VALUE)
								gapMatchStartRelBeg = Short.MAX_VALUE;
							if (gapMatchStartRelBeg < Short.MIN_VALUE)
								gapMatchStartRelBeg = Misc.getUndefShort()+2;
							currActivityInfo.firstMatchingStrikeRelStartSec =(short) gapMatchStartRelBeg;
							currActivityInfo.lastMatchingStrikeRelStartSec =(short) gapMatchStartRel;
						}
						else {
							if (Misc.isUndef(currActivityInfo.firstMatchingStrikeRelStartSec) || gapMatchStartRel < currActivityInfo.firstMatchingStrikeRelStartSec)
								currActivityInfo.firstMatchingStrikeRelStartSec = (short) gapMatchStartRel;
							if (Misc.isUndef(currActivityInfo.lastMatchingStrikeRelStartSec) || gapMatchStartRel > currActivityInfo.firstMatchingStrikeRelStartSec)
								currActivityInfo.lastMatchingStrikeRelStartSec = (short) gapMatchStartRel;
						}
						if (nextStrikeMatching) {
							prevStrikeMatch = strikeNext;
							prevEventThatMatchedStrike = currEvent;
						}
					}
					
				}//if can be insereted in an activity seq
				prevEvent = currEvent;
			}//for each event
		}
		boolean loadEventDataOK = idlingPtCount >= 1 && loadEventPtCount > 1;
		if (!loadEventDataOK) {
			loadEventDataOK = loadEvents.isWorkingInWindow(conn,tsEnd);
		}
		
		
		this.setHasValidExcEventsAroundPeriod(loadEventDataOK);
		boolean isStopEnded = this.isStopEnded();
		if (loadEventDataOK) {//if shovel was not idling then at least one idlng info
			ShovelActivityInfo bestActivityInfo = null;
			int bestActivityInfoIndex = -1;
			int currBestActivityInRetValAtIndex = -1;
			for (int i=0,is=nonidlingInfo.size();i<is;i++) {
				ShovelActivityInfo temp = nonidlingInfo.get(i);
				if (temp.tsEndInclOverAll <= 0 || temp.tsEndInclOverAll <= temp.tsStartOverAll)
					continue;
				if (ShovelSequence.DO_TIMING_ADJUSTMENT_APPROACH != 0) {
					temp.setAvgDurLeftNonProperIfNeededAndNaturalBreaks(this.getStartTS(), this.getEndTS(), (short)(this.getStartBLERelStartSec()-ShovelSequence.BLE_LEFT_GRACE_SEC), (short)(this.getEndBLERelStartSec()+ShovelSequence.BLE_RIGHT_GRACE_SEC), loadEvents, conn,rhsAlreadyShifted, isStopEnded);
					if (loadEventDataOK && !temp.feasibleCountWise)
						continue;
				}
				else {
					if (isStopEnded && loadEventDataOK && temp.count < ShovelSequence.MIN_SHOVEL_LO_CYCLES)
						continue;
				}
				temp.baseCount = temp.count;
				temp.baseStartLoadRelStartSec = temp.startLoadRelStartSec;
				temp.baseEndLoadRelStartSec = temp.endLoadRelStartSec;
				
				//if (loadEventDataOK && temp.count < ShovelSequence.MIN_SHOVEL_LO_CYCLES)
				//	continue;
				
				boolean enoughInBLEWindow = (temp.countInBLEWindow >= ShovelSequence.MIN_SHOVEL_WITH_BLE);
				boolean bestEnoughInBLEWinfow = bestActivityInfo != null && bestActivityInfo.countInBLEWindow >=  ShovelSequence.MIN_SHOVEL_WITH_BLE;
				if (!isStopEnded) {
					if (!enoughInBLEWindow) {
						if (temp.endLoadRelStartSec >= this.startBLERelStartSec- ShovelSequence.BLE_LEFT_GRACE_SEC && temp.startLoadRelStartSec <= this.endBLERelStartSec+ShovelSequence.BLE_RIGHT_GRACE_SEC)
							enoughInBLEWindow = true;
					}
					if (!bestEnoughInBLEWinfow && bestActivityInfo != null) {
						if (bestActivityInfo.endLoadRelStartSec >= this.startBLERelStartSec- ShovelSequence.BLE_LEFT_GRACE_SEC && bestActivityInfo.startLoadRelStartSec <= this.endBLERelStartSec+ShovelSequence.BLE_RIGHT_GRACE_SEC)
							bestEnoughInBLEWinfow = true;
					}
				}
				
				temp.setAndGetDumperQuality(!Misc.isUndef(this.startBLERelStartSec) ? enoughInBLEWindow ? 2 : 1 : this.likeBLE);
				int toSwap = bestActivityInfo == null ? 1 : enoughInBLEWindow != bestEnoughInBLEWinfow ? (enoughInBLEWindow ? 1 : -1) : 
					(temp.count >= ShovelSequence.MIN_SHOVEL_CYCLES || !isStopEnded) && bestActivityInfo.count < MIN_SHOVEL_CYCLES ? 1
							: bestActivityInfo.endLoadRelStartSec >= 0 &&
							    (this.getEndTS()- bestActivityInfo.endLoadRelStartSec*1000-this.getStartTS()) > ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS ? 1
							:
								temp.getDumperQuality() > bestActivityInfo.getDumperQuality() ? 1 : temp.getDumperQuality() < bestActivityInfo.getDumperQuality() ? -1 : 0;
				if (toSwap == 0) {
					toSwap = bestActivityInfo.matchingStrikeSmallestAbsGapSec < temp.matchingStrikeSmallestAbsGapSec ? -1 : bestActivityInfo.matchingStrikeSmallestAbsGapSec > temp.matchingStrikeSmallestAbsGapSec ? 1 : 0;
				}
				if (toSwap == 0) {
					toSwap = bestActivityInfo.matchingStrikeCount < temp.matchingStrikeCount ? 1 : bestActivityInfo.matchingStrikeCount > temp.matchingStrikeCount ? -1 : 0;
				}
				if (toSwap == 0)
					toSwap = 1; //give preference to later
				if (toSwap == 0)
					toSwap = bestActivityInfo.count < temp.count ? 1 : bestActivityInfo.count == temp.count ? -1 : 0;
				if (toSwap == 0 && !Misc.isUndef(temp.startLoadRelStartSec)) {
					toSwap = 1;
				}
				if (otherAltShovelActivityInfoExclBest != null) {
					otherAltShovelActivityInfoExclBest.add(temp);
				}
				if (toSwap == 1) {
					bestActivityInfo = temp;
					bestActivityInfoIndex = i;
					currBestActivityInRetValAtIndex = otherAltShovelActivityInfoExclBest == null ? -1 : otherAltShovelActivityInfoExclBest.size()-1;
				}
			}
			
			
			if (bestActivityInfo != null) {
				this.setThisTimingToShovelActivityInfo(bestActivityInfo, bestActivityInfoIndex < nonidlingInfo.size()-1, doLikelyStartEnd, doBaseStartEnd, conn, loadEvents, shovelIdling, toCalcNewStrike);
				if (bestActivityInfoIndex < nonidlingInfo.size() -1 || this.currRightInCount > 0 || bestActivityInfo.tsEndInclOverAll < (this.endTS-ExcLoadEvent.MIN_ENDCYCLE_STOPEND_MS-5000)) {
					this.hasInactivityAfterBefStop = true;
				}
				if (otherAltShovelActivityInfoExclBest != null && currBestActivityInRetValAtIndex >= 0)	
					otherAltShovelActivityInfoExclBest.remove(currBestActivityInRetValAtIndex);
			}
			else {
				if (ThreadContextCache.g_shovelStat != null)
					ThreadContextCache.g_shovelStat.updateWithEvent(System.nanoTime()-timingMarker1);

				return false;
			}
			boolean isAv = ((bestActivityInfo != null && bestActivityInfo.feasibleCountWise)) 
				&&   (!doRHSEndCheck || (this.getEndTS()- this.getActEndTS()) <= ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS);
			if (ThreadContextCache.g_shovelStat != null)
				ThreadContextCache.g_shovelStat.updateWithEvent(System.nanoTime()-timingMarker1);
			
			return isAv;
		}//if loadEventDataOK
		else {
			boolean isAv = false;
			long loadMilli = tsEnd-tsStart-milliUnAv;
			isAv =(!isStopEnded && loadMilli >= ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS) || (loadMilli >= ShovelSequence.G_NON_OVERLAP_MILLI_FOR_LOAD);
			if (ThreadContextCache.g_shovelStat != null)
				ThreadContextCache.g_shovelStat.updateWithEvent(System.nanoTime()-timingMarker1);
			
			return isAv;
		}//if !loadEventDataOK
		//return new MiscInner.PairIntBool((int)(gap/1000),isAv);
	}
	
	public static int getStrikeQLevelIgnoringLBLE(byte q) {
		int retval =  (q == ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH || q == ShovelSequence.LBLE_EXCLOAD_HIQSTRIKE_MATCH)
		 ? BLE_EXCLOAD_HIQSTRIKE_MATCH
		 : (q == ShovelSequence.BLE_EXCLOAD_LOQSTRIKE_MATCH || q == ShovelSequence.LBLE_EXCLOAD_LOQSTRIKE_MATCH) 
		 ? BLE_EXCLOAD_LOQSTRIKE_MATCH
		:  (q == ShovelSequence.BLE_EXCLOAD_STRIKE_NOMATCH || q == ShovelSequence.LBLE_EXCLOAD_STRIKE_NOMATCH)
		? ShovelSequence.BLE_EXCLOAD_STRIKE_NOMATCH
		: ShovelSequence.BLE_NOEXCLOAD_MAYBESTRIKE
		;
		
		return retval;
	}
	public static int getNearBLEEtcQLevel(byte q) {
		int retval =  (q == ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH) ? BLE_EXCLOAD_HIQSTRIKE_MATCH
				: (q >= ShovelSequence.BLE_NOEXCLOAD_MAYBESTRIKE) ? BLE_NOEXCLOAD_MAYBESTRIKE
				: (q == ShovelSequence.LBLE_EXCLOAD_HIQSTRIKE_MATCH) ? NEAR_EXCLOAD_HIQSTRIKE_MATCH
				: (q >= ShovelSequence.LBLE_NOEXCLOAD_MAYBESTRIKE) ? LBLE_NOEXCLOAD_MAYBESTRIKE
				: (q >= ShovelSequence.NEAR_EXCLOAD_HIQSTRIKE_MATCH) ? LBLE_NOEXCLOAD_MAYBESTRIKE
				: ShovelSequence.NEAR_ONLY
				;
		return retval;
	}
	public static boolean otherBetterThanNew(ShovelSequence best, ShovelSequence other, ArrayList<ShovelSequence> bestOtherToUnfirm, ArrayList<ShovelSequence> otherOtherToUnfirm, ArrayList<ShovelSequence.OverlapTimingChangeInfo> bestOtherTimingChanges, ArrayList<ShovelSequence.OverlapTimingChangeInfo> otherOtherTimingChanges) {
		//toSwap = 1 means other better, -1 means best better
		int toSwap = 0;
		if (best == null)
			toSwap = 1;
		if (toSwap == 0) {
			int bestQ = getNearBLEEtcQLevel(best.getDumperBestQuality());
			int otherQ = getNearBLEEtcQLevel(other.getDumperBestQuality());
			if (bestQ < otherQ)
				toSwap = 1;
			else if (bestQ > otherQ)
				toSwap = -1;
		}
		if (toSwap == 0) {
			int distCmp = other.getDistComparison(best); //returns -1 if otherDist < bestDist
			if (distCmp < 0)
				toSwap = 1;
			else if (distCmp > 0)
				toSwap = -1;
		}
		if (toSwap != 0)
			return toSwap > 0;
		long rhsGapBetweenLoadCompleteAndEndTS = best.getEndTS()-best.getActEndTS();
		long meGapBetweenLoadCompleteAndEndTS = other.getEndTS()-other.getActEndTS();
		long gap = meGapBetweenLoadCompleteAndEndTS - rhsGapBetweenLoadCompleteAndEndTS;
		if (toSwap == 0) {
			if (gap > 20000) {//me seems worse
				toSwap = -1;
			}
			else if (gap < -20000) {
				toSwap = 1;
			}
		}
		if (toSwap == 0) {
			//check q of impact ..
			int bestUnfirmCnt = 0;
			int otherUnfirmCnt = 0;
			int bestTimingChangesCnt = 0;
			int otherTimingChangesCnt = 0;
			for (int art = 0;art<2;art++) {
				ArrayList<ShovelSequence> unfirmlist = art == 0 ? bestOtherToUnfirm : otherOtherToUnfirm;
				int cnt = 0;
				for (int i=0,is=unfirmlist == null ? 0 : unfirmlist.size(); i<is; i++) {
					cnt++;
				}
				if (art == 0)
					bestUnfirmCnt = cnt;
				else
					otherUnfirmCnt = cnt;
			}
			
			for (int art = 0;art<2;art++) {
				ArrayList<ShovelSequence.OverlapTimingChangeInfo> list = art == 0 ? bestOtherTimingChanges : otherOtherTimingChanges;
				int cnt = 0;
				for (int i=0,is=list == null ? 0 : list.size(); i<is; i++) {
					if (!Misc.isUndef(list.get(i).rhsActStartSec))
						cnt++;
				}
				if (art == 0)
					bestTimingChangesCnt = cnt;
				else
					otherTimingChangesCnt = cnt;
			}
			int bestToughness = 5*bestUnfirmCnt+bestTimingChangesCnt;
			int otherToughness = 5*otherUnfirmCnt+otherTimingChangesCnt;
			if (bestToughness < otherToughness)
				toSwap = -1;
			else if (bestToughness > otherToughness)
				toSwap = 1;
		}
		if (toSwap == 0) {
			
			if (gap > 10000) {//me seems worse
				toSwap = -1;
			}
			else if (gap < -10000) {
				toSwap = 1;
			}
		}
		if (toSwap == 0) {
			long startgap = other.getActStartTS() - best.getActStartTS();
			toSwap = startgap > 10000 ? 1 : startgap < -10000 ? -1 : 0;
		}
		if (toSwap == 0) {
			toSwap = other.cycleCount > best.cycleCount ? 1 : other.cycleCount < best.cycleCount ? -1 : 0;
		}
		return toSwap >= 0;
	}
	public static int checkIfMatchingPrevShovelId(int prevGuessedShovelId, int currManualAssignedShovelId, int prevTripShovelId, int meShovelId, int rhsShovelId) {
		int toSwap = 0;
				
		if (toSwap == 0) {
			if (meShovelId == currManualAssignedShovelId)
				toSwap = 1;
			else if (rhsShovelId == currManualAssignedShovelId)
				toSwap = -1;
		}
		if (toSwap == 0) {
			if (meShovelId == prevGuessedShovelId)
				toSwap = 1;
			else if (rhsShovelId == prevGuessedShovelId)
				toSwap = -1;
		}
		if (toSwap == 0) {
			if (meShovelId == prevTripShovelId)
				toSwap = 1;
			else if (rhsShovelId == prevTripShovelId)
				toSwap = -1;
		}
		return toSwap;
	}
	public static final boolean CONSERVATIVE_RANKING_OF_BESTSHOVEL = true; //will put shovelThresh to 0 if cant be differentiated
	public static final int CONSERVATIVE_THRESH_MILLI_END = CONSERVATIVE_RANKING_OF_BESTSHOVEL ? 40000 : 20000;
	
	static int bleStuffMeBetter(Connection conn, int meShovelId, int otherShovelId, long meActEndTS, long otherActEndTS) {
		//me better then 1, if equal = 0, else -1
		int retval = 0;
		NewBLEMgmt meBLE = NewBLEMgmt.getBLEReadList(conn, meShovelId);
		NewBLEMgmt otherBLE = NewBLEMgmt.getBLEReadList(conn, otherShovelId);
		BLEInfo mePrior = meBLE == null ? null : meBLE.getImmIsPressent(conn, meActEndTS);
		BLEInfo otherPrior = otherBLE == null ? null : otherBLE.getImmIsPressent(conn, otherActEndTS);
		long meGap = mePrior == null ? Long.MAX_VALUE : meActEndTS-mePrior.getGpsRecordTime();
		long otherGap = otherPrior == null ? Long.MAX_VALUE : otherActEndTS-otherPrior.getGpsRecordTime();
		long gap = meGap-otherGap;
		if (gap < -45000)
			retval = -1;
		else if (gap > 45000)
			retval = 1;
		return retval;
	}
	public static int meBetterThanExt(boolean meProperAct, byte meCycleCount, byte meQuality, byte meDist, long meActEndTS, long meEndTS, long meActStartTS, boolean rhsProperAct, byte rhsCycleCount, byte rhsQuality, byte rhsDist, long rhsActEndTS, long rhsEndTS, long rhsActStartTS
			, int prevGuessedShovelId, int currManualAssignedShovelId, int prevTripShovelId, int meShovelId, int rhsShovelId
			, boolean isStoppedOngoing, Connection conn) {
		boolean givePrefToPrior = isStoppedOngoing; 
		int toSwap = 0;
		if (givePrefToPrior) {
			toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
			boolean toIgnorePrev = false;
			long rhsGapBetweenLoadCompleteAndEndTS = rhsEndTS-rhsActEndTS;
			long meGapBetweenLoadCompleteAndEndTS = meEndTS-meActEndTS;

			if (toSwap == 1) {//me matches ... make sure that rhs is not really bettern me
				
				boolean rhsDefinitelyBetter = meDefinitelyBetterThanExt(rhsProperAct, rhsCycleCount, rhsQuality, rhsDist, meProperAct, meCycleCount, meQuality, meDist
						, isStoppedOngoing, rhsGapBetweenLoadCompleteAndEndTS, meGapBetweenLoadCompleteAndEndTS);
				if (rhsDefinitelyBetter)
					return -1;
				else
					return 1;
			}
			else if (toSwap == -1) {//me matches ... make sure that rhs is not really bettern me
				boolean meDefinitelyBetter = meDefinitelyBetterThanExt(meProperAct, meCycleCount, meQuality, meDist, rhsProperAct, rhsCycleCount, rhsQuality, rhsDist
						, isStoppedOngoing, meGapBetweenLoadCompleteAndEndTS, rhsGapBetweenLoadCompleteAndEndTS);
				if (meDefinitelyBetter)
					return 1;
				else
					return -1;
			}
		}
		
		//boolean meProperAct = this.isHasValidExcEventsAroundPeriod();
		//boolean rhsProperAct = rhs.isHasValidExcEventsAroundPeriod();
		int meQLevel = ShovelSequence.getNearBLEEtcQLevel(meQuality);
		int rhsQLevel = ShovelSequence.getNearBLEEtcQLevel(rhsQuality);
		if (meQLevel != rhsQLevel)
			return meQLevel - rhsQLevel;
		if (meProperAct == rhsProperAct && meProperAct) {
			if (meCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES && rhsCycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES) 
				return -1;
			else if (meCycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES && rhsCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES)
				return 1;
			
			if (toSwap == 0 && (meCycleCount != 0 && rhsCycleCount != 0)) {
				toSwap = meQuality > rhsQuality ? 1 : meQuality < rhsQuality ? -1 : 0;
			}
			if (meQLevel >= ShovelSequence.BLE_NOEXCLOAD_MAYBESTRIKE) {//both appear to be same
				toSwap = ShovelSequence.bleStuffMeBetter(conn, meShovelId, rhsShovelId, meActEndTS, rhsActEndTS);				
			}
			if (toSwap == 0) {
				int distCmp = ShovelSequence.getDistComparison(meDist, rhsDist);
				if (distCmp < 0)
					toSwap = 1;
				else if (distCmp > 0)
					toSwap = -1;
			}
			
			if (toSwap == 0 && !isStoppedOngoing) {
				long rhsGapBetweenLoadCompleteAndEndTS = rhsEndTS-rhsActEndTS;
				long meGapBetweenLoadCompleteAndEndTS = meEndTS-meActEndTS;
				long gap = meGapBetweenLoadCompleteAndEndTS - rhsGapBetweenLoadCompleteAndEndTS;
				if (gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END) {//me seems worse //10 earlier
					toSwap = -1;
				}
				else if (gap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END) {//10 earlier
					toSwap = 1;
				}
 
				if (toSwap == 0) {
					toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
				}
				if (!ShovelSequence.CONSERVATIVE_RANKING_OF_BESTSHOVEL) {//we will randomly pick
					if (toSwap == 0) {
						gap = meActStartTS - rhsActStartTS;
						toSwap = gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? 1 : gap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? -1 : 0;
					}
					if (toSwap == 0) {
						toSwap = meCycleCount > rhsCycleCount ? 1 : meCycleCount < rhsCycleCount ? -1 : 0;
					}
				}
			}
			if (toSwap == 0 && isStoppedOngoing) {
				boolean meHasValidTime = meCycleCount >= ShovelSequence.MIN_SHOVEL_LO_CYCLES; 
				boolean rhsHasValidTime = rhsCycleCount >= ShovelSequence.MIN_SHOVEL_LO_CYCLES;
				if (meHasValidTime != rhsHasValidTime) {
					toSwap = meHasValidTime ? 1 : -1;
				}
				
				if (toSwap == 0 && Math.abs(rhsCycleCount-meCycleCount) <= 1) {//to be done later
					toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
				}
				if (!ShovelSequence.CONSERVATIVE_RANKING_OF_BESTSHOVEL) {
					if (toSwap == 0) {
						long gap = meActStartTS - rhsActStartTS;
						toSwap = gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? 1 : gap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? -1 : 0;
					}
	
					if (toSwap == 0 && meCycleCount != rhsCycleCount) {
						toSwap = meCycleCount > rhsCycleCount ? 1 : -1;
					}
				}
			}
			//start/end will be same
			return toSwap;
		}
		else { //ignore cycleCount stuff
			//QLevels are same
			if (meProperAct && meCycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES)
				return 1;
			if (rhsProperAct && rhsCycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES)
				return -1;
			//now ignore cycle count stuff and quality stuff
			if (meQLevel >= ShovelSequence.BLE_NOEXCLOAD_MAYBESTRIKE) {//both appear to be same
				toSwap = ShovelSequence.bleStuffMeBetter(conn, meShovelId, rhsShovelId, meActEndTS, rhsActEndTS);				
			}
			if (toSwap == 0) {
				int distCmp = ShovelSequence.getDistComparison(meDist, rhsDist);
				if (distCmp < 0)
					toSwap = 1;
				else if (distCmp > 0)
					toSwap = -1;
			}
			long meDur = meActEndTS-meActStartTS;
			long rhsDur = rhsActEndTS-rhsActStartTS;
			
			boolean meHasValidTime = meDur >= ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS ;
			boolean rhsHasValidTime = rhsDur >= ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS;
			if (meHasValidTime != rhsHasValidTime) {
				toSwap = meHasValidTime ? 1 : -1;
			}
			
			if (toSwap == 0 && Math.abs(rhsCycleCount-meCycleCount) <= 1) {//to be done later
				toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
			}
			if (toSwap == 0 && !isStoppedOngoing) {
				long rhsGapBetweenLoadCompleteAndEndTS = rhsEndTS-rhsActEndTS;
				long meGapBetweenLoadCompleteAndEndTS = meEndTS-meActEndTS;
				long gap = meGapBetweenLoadCompleteAndEndTS - rhsGapBetweenLoadCompleteAndEndTS;
				if (gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END) {//me seems worse
					toSwap = -1;
				}
				else if (gap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END) {
					toSwap = 1;
				}
				if (toSwap == 0) {
					toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
				}
				if (!ShovelSequence.CONSERVATIVE_RANKING_OF_BESTSHOVEL) {
					if (toSwap == 0) {
						gap = meActStartTS - rhsActStartTS;
						toSwap = gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? 1 : gap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? -1 : 0;
					}
					if (toSwap == 0) {
						long durGap = meDur - rhsDur; 
						toSwap = durGap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? 1 : durGap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? -1 : 0;
					}
				}
			}
			if (toSwap == 0 && isStoppedOngoing) {
				if (toSwap == 0 && Math.abs(meDur-rhsDur) <= ThreadContextCache.G_DOLASTSTOP_PROC_THRESH_MILLI) {
					toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
				}
				if (!ShovelSequence.CONSERVATIVE_RANKING_OF_BESTSHOVEL) {
					if (toSwap == 0) {
						long gap = meActStartTS - rhsActStartTS;
						toSwap = gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? 1 : gap < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? -1 : 0;
					}
					if (toSwap == 0 && meDur != rhsDur) {
						toSwap = meDur - rhsDur > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? 1 : meDur-rhsDur < -ShovelSequence.CONSERVATIVE_THRESH_MILLI_END/2 ? -1 : 0;
					}
				}
			}
			
			//start/end will be same
			return toSwap;
		}
	}
	public static boolean meDefinitelyBetterThanExt(boolean meProperAct, byte meCycleCount, byte meQuality, byte meDist, boolean rhsProperAct, byte rhsCycleCount, byte rhsQuality, byte rhsDist, boolean isStoppedOngoing, long meGapBetweenLoadCompleteAndEndTS, long rhsGapBetweenLoadCompleteAndEndTS) {
		//will return true if me is definitelyBetter ... will return false if it is lesser OR maybeSimilar
		
		//boolean meProperAct = this.isHasValidExcEventsAroundPeriod();
		//boolean rhsProperAct = rhs.isHasValidExcEventsAroundPeriod();
		int meQLevel = ShovelSequence.getNearBLEEtcQLevel(meQuality);
		int rhsQLevel = ShovelSequence.getNearBLEEtcQLevel(rhsQuality);
		if (meQLevel >= ShovelSequence.LBLE_NOEXCLOAD_MAYBESTRIKE && rhsQLevel < ShovelSequence.LBLE_NOEXCLOAD_MAYBESTRIKE)
			return true;
		int distCmp = ShovelSequence.getDistComparison(meDist, rhsDist);
		if (distCmp < 0)
			return true;
		if (meProperAct) {
			if (meCycleCount >= ShovelSequence.PREFERRED_SHOVEL_CYCLES && rhsCycleCount <= ShovelSequence.MIN_SHOVEL_LO_CYCLES)
				return true;
			int meStrikeLevel = ShovelSequence.getStrikeQLevelIgnoringLBLE(meQuality);
			int rhsStrikeLevel = ShovelSequence.getStrikeQLevelIgnoringLBLE(rhsQuality);
			if ((meStrikeLevel == ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH) || (meStrikeLevel == ShovelSequence.BLE_EXCLOAD_LOQSTRIKE_MATCH && rhsProperAct && rhsStrikeLevel == ShovelSequence.BLE_EXCLOAD_STRIKE_NOMATCH))
				return true;
			if (meCycleCount >= (rhsCycleCount+2) && rhsProperAct)
				return true;
			if (!isStoppedOngoing && rhsProperAct) {
				long gap = meGapBetweenLoadCompleteAndEndTS - rhsGapBetweenLoadCompleteAndEndTS;
				if (gap > ShovelSequence.CONSERVATIVE_THRESH_MILLI_END) {//me seems worse //10 earlier
					return true;
				}
			}
		}
		return false;
	}

	public int meBetterThanExt(ShovelSequence rhs, int prevGuessedShovelId, int currManualAssignedShovelId, int shovelIdForPrevTrip, int meShovelId, int rhsShovelId, Connection conn) {
		if (rhs == null)
			return 1;
		int toSwap = this.meBetterThanExt(this.isHasValidExcEventsAroundPeriod(), this.getCycleCount(),this.getDumperBestQuality(), this.getDist5()
				, this.getActEndTS(), this.getEndTS(), this.getActStartTS()
				,rhs.isHasValidExcEventsAroundPeriod(), rhs.getCycleCount(),rhs.getDumperBestQuality(), rhs.getDist5()
				, rhs.getActEndTS(), rhs.getEndTS(), rhs.getActStartTS()
				, prevGuessedShovelId, currManualAssignedShovelId, shovelIdForPrevTrip, meShovelId, rhsShovelId
				,!this.isStopEnded()
				,conn
				);
		return toSwap;
	}
	
	public static int infeasibleMeBetterThanExt(byte meQuality, byte meDist, byte rhsQuality, byte rhsDist
			, int prevGuessedShovelId, int currManualAssignedShovelId, int prevTripShovelId, int meShovelId, int rhsShovelId) {
		int meQLevel = getNearBLEEtcQLevel(meQuality);
		int rhsQLevel = getNearBLEEtcQLevel(rhsQuality);
		if (meQLevel != rhsQLevel)
			return meQLevel - rhsQLevel;
		int toSwap = 0;
		if (toSwap == 0) {
			int distCmp = ShovelSequence.getDistComparison(meDist, rhsDist);
			if (distCmp < 0)
				toSwap = 1;
			else if (distCmp > 0)
				toSwap = -1;
		}
		if (toSwap == 0) {
			toSwap = ShovelSequence.checkIfMatchingPrevShovelId(prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
		}
		return toSwap;
	}

	public int infeasibleMeBetterThanExt(ShovelSequence rhs, int prevGuessedShovelId, int currManualAssignedShovelId, int prevTripShovelId, int meShovelId, int rhsShovelId) {
		if (rhs == null)
			return 1;
		return infeasibleMeBetterThanExt(this.getDumperBestQuality(), this.getDist5(), rhs.getDumperBestQuality(), rhs.getDist5()
				, prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, meShovelId, rhsShovelId);
	}

	public boolean meBetterThanExtOld(ShovelSequence rhs) {
		if (rhs == null)
			return true;
		else if (true && this.cycleCount < ShovelSequence.MIN_SHOVEL_CYCLES && rhs.cycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES) 
			return false;
		else if (true && this.cycleCount >= ShovelSequence.MIN_SHOVEL_CYCLES && rhs.cycleCount < ShovelSequence.MIN_SHOVEL_CYCLES)
			return true;
		
		int toSwap = this.meBetterThan(rhs);
		if (toSwap == 0) {
			int distCmp = this.getDistComparison(rhs);
			if (distCmp < 0)
				toSwap = 1;
			else if (distCmp > 0)
				toSwap = -1;
		}
		if (false && toSwap == 0) {
			int cycGap = this.cycleCount - rhs.cycleCount;
			toSwap = cycGap == 0 ? 0 : cycGap > 0 ? 1 : -1;
		}
		if (toSwap == 0) {
			long rhsGapBetweenLoadCompleteAndEndTS = rhs.getEndTS()-rhs.getActEndTS();
			long meGapBetweenLoadCompleteAndEndTS = this.getEndTS()-this.getActEndTS();
			long gap = meGapBetweenLoadCompleteAndEndTS - rhsGapBetweenLoadCompleteAndEndTS;
			if (gap > 10000) {//me seems worse
				toSwap = -1;
			}
			else if (gap < -10000) {
				toSwap = 1;
			}
		}
		if (toSwap == 0) {
			long gap = this.getActStartTS() - rhs.getActStartTS();
			toSwap = gap > 10000 ? 1 : gap < -10000 ? -1 : 0;
		}
		if (toSwap == 0) {
			toSwap = this.cycleCount > rhs.cycleCount ? 1 : this.cycleCount < rhs.cycleCount ? -1 : 0;
		}
		//start/end will be same
		return toSwap >= 0;
	}
	public int meBetterThan(ShovelSequence rhs) {
		return this.meBetterThan(rhs, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt());		
	}
	public static short getSecGap(long ts, long forThis) {
		long sec = (forThis-ts)/1000;
		if (sec > Short.MAX_VALUE)
			sec = Short.MAX_VALUE;
		else if (sec < Misc.getUndefShort()+2)
			sec = (Misc.getUndefShort()+2);
		return (short)sec;
	}
	public short getSecGap(long ts) {
		return ShovelSequence.getSecGap(this.startTS, ts);
	}
	public static class TimingAdjustmentInfo {
		public long actStartTS;
		public long actEndTS;
		public byte cycleCount;
		public byte currInLeft;
		public byte currInRight;
		public TimingAdjustmentInfo(long actStartTS, long actEndTS, byte cycleCount, byte currInLeft, byte currInRight) {
			this.actStartTS = actStartTS;
			this.actEndTS = actEndTS;
			this.cycleCount = cycleCount;
			this.currInLeft = currInLeft;
			this.currInRight = currInRight;
		}
	}
	
	public TimingAdjustmentInfo getTimingAsPerNew(long start, long end) {
		byte currCycle = getCycleCount();
		byte currLeft = getCurrLeftInCount();
		byte currRight = getCurrRightInCount();
		long currStartTS = this.getActStartTS();
		byte newCurrLeft = currLeft;
		for (int i=currLeft,is=this.durs == null ? 0 : this.durs.length;i<is;i++) {
			currStartTS += (i == currLeft ? 0 : this.durs[i]);
			if (currStartTS < start) {
				newCurrLeft++;
				currCycle--;
			}
			else {
				break;
			}
		}
		long currEndTS = this.getActEndTS();
		byte newCurrRight = currRight;
		for (int i=this.durs == null ? -1 : this.durs.length-1-currRight;i>=0;i--) {
			if (currEndTS > end) {
				newCurrRight++;
				currCycle--;
				currEndTS -= this.durs[i];
			}
			else {
				break;
			}
		}
		return new TimingAdjustmentInfo(currStartTS, currEndTS, currCycle, newCurrLeft, newCurrRight);
		
	}
	public static class OverlapTimingChangeInfo {
		public ShovelSequence other = null;
		public ShovelSequence replaceOtherWithThisIfNotNull = null;
		public short rhsActStartSec = Misc.getUndefShort();
		public short rhsActEndSec = Misc.getUndefShort();
		public byte cycleCount = 0;
		public byte currLeftInCount = 0;
		public byte currRightInCount = 0;
		public int newShovelId = Misc.getUndefInt();
		public static OverlapTimingChangeInfo getChangeInfo(long desiredStart, long desiredEnd, ShovelSequence other, int newShovelId) {
			short initStart = other.getStartActLoadRelStartSec();
			short initEnd = other.getEndActLoadRelStartSec();
			byte initCycle = other.getCycleCount();
			TimingAdjustmentInfo adjust = other.getTimingAsPerNew(desiredStart, desiredEnd);
			short newStart = other.getSecGap(adjust.actStartTS);
			short newEnd = other.getSecGap(adjust.actEndTS);
			byte newCycle = adjust.cycleCount;
			short propStart = newStart != initStart || newEnd != initEnd ? newStart : Misc.getUndefShort();
			short propEnd = newStart != initStart || newEnd != initEnd ? newEnd : Misc.getUndefShort();
			byte propCycle = newCycle != initCycle ? newCycle : Misc.getUndefByte();
			OverlapTimingChangeInfo retval = new OverlapTimingChangeInfo(other, propStart
					,propEnd, propCycle, adjust.currInLeft, adjust.currInRight, null, newShovelId);
			return retval;
		}
		//NEWAPPROACH public boolean rhsChangeWithFullData = false;
		boolean equal(OverlapTimingChangeInfo rhs) {
			return ((other != null && rhs.other != null && other.compareTo(rhs.other) == 0) || (other == null && rhs.other == null))
			 && (rhs.rhsActStartSec == this.rhsActStartSec)
			 && (rhs.rhsActEndSec == this.rhsActEndSec)
			 ;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
			sb.append("Dumper:").append(other.getDumperId()).append("Dt:").append(sdf.format(Misc.longToUtilDate(other.getStartTS()))).append(" Start:").append(rhsActStartSec).append(" End:").append(rhsActEndSec).append(" Cnt:").append(this.cycleCount);
			return sb.toString();
		}

		public OverlapTimingChangeInfo(ShovelSequence other, short rhsActStartSec,
				short rhsActEndSec, byte cycleCount, byte currLeftInCount, byte currRightInCount, ShovelSequence replaceOtherWithThisIfNotNull, int newShovelId) {
			super();
			this.other = other;
			this.rhsActStartSec = rhsActStartSec;
			this.rhsActEndSec = rhsActEndSec;
			this.cycleCount = cycleCount;
			this.currLeftInCount = currLeftInCount;
			this.currRightInCount = currRightInCount;
			this.replaceOtherWithThisIfNotNull = replaceOtherWithThisIfNotNull;
			this.newShovelId = newShovelId;
		}	
	}
	public static class OverlapChangeInfo {
		public boolean makeRHSUnfirm = false;
		public boolean meNotFeasible = true;
		public OverlapTimingChangeInfo timingChangeInfo = null;
		public ArrayList<ShovelSequence> otherToBeUnfirmed = null;
		
		public int overlapSec = -1;
		public int overlapCycleCount = -1;
		public String dbgString;
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(timingChangeInfo).append(" rhsUnfirm:").append(this.makeRHSUnfirm).append(" NotFeasible").append(this.meNotFeasible).append(" dbg:").append(dbgString);
			return sb.toString();
		}

		public OverlapChangeInfo(boolean makeRHSUnfirm, boolean meNotFeasible,
				short rhsActStartSec, short rhsActEndSec, byte cycleCount, ShovelSequence other, int currLeftInCount, int currRightInCount, ShovelSequence replaceOtherWithThisIfNotNull) {
			super();
			this.makeRHSUnfirm = makeRHSUnfirm;
			this.meNotFeasible = meNotFeasible;
			if (!Misc.isUndef(rhsActStartSec) || !Misc.isUndef(rhsActEndSec)) {
				this.timingChangeInfo = new OverlapTimingChangeInfo(other, rhsActStartSec, rhsActEndSec, cycleCount, (byte)currLeftInCount, (byte) currRightInCount,replaceOtherWithThisIfNotNull, Misc.getUndefInt());
			}
		}
	}
	/////
	// me's timing needs to be changed to assess what happened ..
	public  ShovelSequence quickCopyForRegetTime() {
		ShovelSequence me = new ShovelSequence(this.startTS);
		me.dumperId = this.dumperId;
		me.hasMultipleSubWindow = this.hasMultipleSubWindow;
		me.stopEnded = this.stopEnded;
		me.dist5 = this.dist5;
		me.dumperId = this.dumperId;
		me.endTS = this.endTS;
		me.startBLERelStartSec = this.startBLERelStartSec;
		me.endBLERelStartSec = this.endBLERelStartSec;
		me.startStrikeRelStartSec = this.startStrikeRelStartSec;
		me.endStrikeRelStartSec = this.endStrikeRelStartSec;
		me.updateGrossActLoadTiming();	
		me.updateDumperQuality(false);
		return me;
	}
	public static boolean isValidFromStrike(long startTS, long endTS, long startHiMatch, long endHiMatch, long startStrikeMatch, long endStrikeMatch) {
		//valid himatch then must be contained between startTS/endTS
		//some overlap with startTS/endTS for strike if valid
	//	if (startHiMatch > 0 && !(startHiMatch >= startTS && endHiMatch <= endTS))
		if (startHiMatch > 0 && (endHiMatch < startTS || startStrikeMatch > endTS))
			return false;
		if (startStrikeMatch > 0 && (endStrikeMatch < startTS || startStrikeMatch > endTS))
			return false;
			
		return true;
	}
	public static boolean isShiftOfOtherOK(ShovelSequenceHolder shovelInfo, byte otherQ, long rhsPropTS, boolean shiftLeft) {
		//shift left =>start shifted left.  -!shift left => endShiftedRight
		boolean retval = true;
		ShovelSequence dummy = new ShovelSequence(rhsPropTS);
		int idx = shovelInfo.indexOf(dummy).first;
		if (shiftLeft) {
			//check if the seq containing rhsPropTS
			for (int i=idx; i>=0; i--) {
				ShovelSequence seq = shovelInfo.get(i);
				if (seq == null || seq.endTS < rhsPropTS)
					break;
				if (seq.getDumperBestQuality() >= otherQ && !Misc.isUndef(seq.getStartLoadEventRelStartSec())
						&& rhsPropTS >= (seq.startTS+1000*seq.getStartLoadEventRelStartSec())
						&& rhsPropTS <= (seq.startTS+1000*seq.getEndLoadEventRelStartSec())
								)
								return false;
			}
		}
		else {
			for (int i=idx < 0 ? 0 : idx, is=shovelInfo.size(); i<is; i++) {
				ShovelSequence seq = shovelInfo.get(i);
				if (seq == null || seq.startTS > rhsPropTS)
					break;
				if (seq.getDumperBestQuality() > otherQ && !Misc.isUndef(seq.getStartLoadEventRelStartSec())
						&& rhsPropTS >= (seq.startTS+1000*seq.getStartLoadEventRelStartSec())
						&& rhsPropTS <= (seq.startTS+1000*seq.getEndLoadEventRelStartSec())
								)
								return false;
			}

		}
		return true;
		
	}
	//We only move me or right inwards (ie start move to right and end moved to left ...
	public static final boolean DO_TRY_EQUAL_SHOVEL_COUNT = false ;//true doesny give any better results .. see NoGood procedures

	public static final int ABS_MIN_TRIPGAP_MILLI_WHEN_PROPER = 20*1000;
	public static final int ABS_MIN_TRIPGAP_MILLI_WHEN_LO = 40*1000; //must be left
	public static final int ABS_MAX_TRIPGAP_MILLI_WHEN_PROPER = 30*1000;
	public static final int ABS_MAX_TRIPGAP_MILLI_WHEN_LO = 70*1000; //must be left
	public static final double SUGGESTED_TRIPGAP_CYCLETIME = 1.0; //use 1.0 for doTrueDev else 1.2
	public long getMinFeasibleLeftOfEnd(long tsStart, NewExcLoadEventMgmt loadEvents, Connection conn, long leftSideEndTS) {
		ShovelSequence rhs = this;//copy-n-paste
		long rhsActEndTS = rhs.getActEndTS();
		long rhsActStartTS = rhs.getActStartTS();
		byte rhsCycleCount = rhs.getCycleCount();
		byte rhsMinCycleCount = rhsCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES ? rhsCycleCount : ShovelSequence.MIN_SHOVEL_CYCLES;
		byte rhsCurrRightInCount = rhs.getCurrRightInCount();
		double avgCycleDur = rhs.avgDur;
		double deviation = rhs.deviation;
		
		long targetTripGapMilliForShift = (long)((avgCycleDur+SUGGESTED_TRIPGAP_CYCLETIME*deviation)*1000);//(long)(avgD*1.2*1000.0);//TODO parameterize (long)(avgD*SUGGESTED_TRIPGAP_CYCLETIME*1000.0);
		
		long deltaMilli = (long)(SUGGESTED_TRIPGAP_CYCLETIME*deviation*1000);
		if (deltaMilli < 1000)
			deltaMilli = 1000;
		else if (deltaMilli > 6000)
			deltaMilli = 6000;
		targetTripGapMilliForShift = (long)(avgCycleDur*1000+deltaMilli);
		if (targetTripGapMilliForShift > ABS_MAX_TRIPGAP_MILLI_WHEN_PROPER)
			targetTripGapMilliForShift = ABS_MAX_TRIPGAP_MILLI_WHEN_PROPER;
		
		//targetTripGapMilliForShift = (long)((avgCycleDur+2)*1000);//(long)(avgD*1.2*1000.0);//TODO parameterize (long)(avgD*SUGGESTED_TRIPGAP_CYCLETIME*1000.0);

		byte targetGapSecForShift = (byte)(targetTripGapMilliForShift/1000);
		boolean canMoveRhsEndToLeft = (rhsCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
			|| (rhs.endShiftableToLeft(rhsCurrRightInCount, targetGapSecForShift));
		if (rhsActEndTS >= tsStart && rhsCycleCount > rhsMinCycleCount && canMoveRhsEndToLeft) {
			ExcLoadEvent dummy = new ExcLoadEvent(rhsActEndTS);
			for (int i=0;;i--) {//move rhs end to left
				ExcLoadEvent currEvent = loadEvents.get(conn, dummy, i);
				if (currEvent == null || currEvent.getGpsRecordTime() < rhsActStartTS)
					break;
				if (currEvent.getGpsRecordTime() == dummy.getGpsRecordTime())
					continue;
				if ((currEvent.getIgnoreBecauseNeighbour(conn, loadEvents) & 0x0F) != 0)
					continue;
				
				if (this.endTS-currEvent.getGpsRecordTime() >  ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS)
					break;
				if (currEvent.getGpsRecordTime()-leftSideEndTS < ShovelSequence.END_TO_END_GAP_MILLI) //make sure we dont go too left
					break;

				
				rhsActEndTS = currEvent.getGpsRecordTime();
				rhsCycleCount--;
				rhsCurrRightInCount ++;
				canMoveRhsEndToLeft = (rhsCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
				|| (rhs.endShiftableToLeft(rhsCurrRightInCount, targetGapSecForShift));
				if (!canMoveRhsEndToLeft || rhsCycleCount <= rhsMinCycleCount    
						|| (tsStart > rhsActEndTS ))
					break;
			}
		}
		return rhsActEndTS;
	}
	public static  OverlapChangeInfo adjustTimingForLoadEventWhenOtherBetter(ShovelSequenceHolder shovelInfo, ShovelSequence me, ShovelSequence rhs, NewExcLoadEventMgmt loadEvents, Connection conn, NewVehicleData vdp, NewBLEMgmt bleReads, NewVehicleData strikeVDT, NewVehicleData shovelIdling, boolean isMeBefore
			,boolean canShiftMeToLeft, boolean canShiftMeToRight, boolean canShiftRhsToLeft, boolean canShoftRhsToRight
			, int meLeftGapSec, int rhsLeftGapSec, boolean getAnyAsFeasible, long leftSideEndTS) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();

		boolean isMeEnded = me.isStopEnded();// || me.hasInactivityAfterBefStop();
		boolean rhsEnded = rhs.isStopEnded();// || rhs.hasInactivityAfterBefStop();
		
		boolean toRecalcTimingOfRHS = false;
		boolean toRecalcTimingOfLHS = false;
		
		short rhsActStartSec = Misc.getUndefShort();
		short rhsActEndSec = Misc.getUndefShort();
		long meActStartTS = me.getActStartTS();
		long meActEndTS = me.getActEndTS();
		long rhsActStartTS = rhs.getActStartTS();
		long rhsActEndTS = rhs.getActEndTS();
		
		int meCycleCount = me.cycleCount;
		int rhsCycleCount = rhs.cycleCount;
		double avgCycleDur = ((double)me.avgDur+(double)rhs.avgDur)/2.0;
		
		if (DO_TIMING_ADJUSTMENT_APPROACH == 0) {
			avgCycleDur = (double)((rhsActEndSec-rhsActStartSec)+(meActEndTS-meActStartTS))/((double)(meCycleCount+rhsCycleCount-2)*1000.0);
		}
		double deviation = ShovelSequence.DO_GAP_BY_DEVIATION ? ((double)me.deviation+(double)rhs.deviation)/2.0 : (0.2)*avgCycleDur;
		long targetMilliSecWhenProper = ((long)(avgCycleDur+1*deviation*SUGGESTED_TRIPGAP_CYCLETIME))*1000;//((long)(avgCycleDur*SUGGESTED_TRIPGAP_CYCLETIME))*1000;
		
		
		long targetMilliSecWhenLo = ((long)(avgCycleDur+0*deviation*SUGGESTED_TRIPGAP_CYCLETIME+avgCycleDur))*1000;
				
		targetMilliSecWhenProper = 0;//
		targetMilliSecWhenLo = targetMilliSecWhenProper;
		boolean alreadyFeasible  = false;
		if (!alreadyFeasible && ( (!isMeEnded && me.cycleCount == 0) || (!rhsEnded && rhs.cycleCount == 0)))
			alreadyFeasible = true;
		if (!alreadyFeasible && meActEndTS < rhsActStartTS 
				&& (!rhsEnded || !isMeEnded || (rhsActEndTS-meActEndTS) >= ShovelSequence.END_TO_END_GAP_MILLI)
				&& (!isMeEnded || meActEndTS-leftSideEndTS >= ShovelSequence.END_TO_END_GAP_MILLI)) {//check if already feasible
			long targetGap = rhsCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES ? targetMilliSecWhenLo : targetMilliSecWhenProper;
			if (meActEndTS < rhsActStartTS - targetGap)
				alreadyFeasible = true;
		}
		else if (!alreadyFeasible && rhsActEndTS < meActStartTS 
				     &&(! isMeEnded || !rhsEnded || (meActEndTS-rhsActEndTS) >= ShovelSequence.END_TO_END_GAP_MILLI)
				     && (!rhsEnded || rhsActEndTS-leftSideEndTS >= ShovelSequence.END_TO_END_GAP_MILLI)) {
			long targetGap = meCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES ? targetMilliSecWhenLo : targetMilliSecWhenProper;
			if (rhsActEndTS < meActStartTS - targetGap)
				alreadyFeasible = true;
		}
		
		if (alreadyFeasible) {
			return new ShovelSequence.OverlapChangeInfo(false, false, Misc.getUndefShort(), Misc.getUndefShort() , (byte) Misc.getUndefByte(), rhs, 0, 0, null);
		}
		if ((isMeEnded && meActEndTS <= rhsActEndTS && meActEndTS-leftSideEndTS < END_TO_END_GAP_MILLI) 
				|| (rhsEnded && rhsActEndTS < meActEndTS && rhsActEndTS-leftSideEndTS < END_TO_END_GAP_MILLI)) {
			return new ShovelSequence.OverlapChangeInfo(false, true, Misc.getUndefShort(), Misc.getUndefShort() , (byte) Misc.getUndefByte(), rhs, 0, 0, null);
		}
		boolean rhsStartShifted = false;
		boolean meStartShifted = false;
		boolean meNotFeasible = true;
		byte meQ = me.getDumperBestQuality();
		byte rhsQ = rhs.getDumperBestQuality();
		long metsMatchHiStrikeLow = -1;
		long metsMatchHiStrikeHigh = -1;
		long rhstsMatchHiStrikeLow = -1;
		long rhstsMatchHiStrikeHigh = -1;
		long metsStrikeLow = -1;
		long metsStrikeHigh = -1;
		long rhstsStrikeLow = -1;
		long rhstsStrikeHigh = -1;
		//byte meMinCycleCount =  me.cycleCount < MIN_SHOVEL_CYCLES ? me.cycleCount : MIN_SHOVEL_CYCLES;
		//byte rhsMinCycleCount =  rhs.cycleCount < MIN_SHOVEL_CYCLES ? rhs.cycleCount : MIN_SHOVEL_CYCLES;
		int overlapSec = -1;
		int overlapCycle = -1;
		byte meMinCycleCount = MIN_SHOVEL_CYCLES;
		byte rhsMinCycleCount =  MIN_SHOVEL_CYCLES;
		meMinCycleCount = (byte) (me.cycleCount < MIN_SHOVEL_CYCLES ? me.cycleCount : MIN_SHOVEL_CYCLES);
		rhsMinCycleCount = (byte) (rhs.cycleCount < MIN_SHOVEL_CYCLES ? rhs.cycleCount : MIN_SHOVEL_CYCLES);
		
		boolean doDiffHiqLoQ = BLE_EXCLOAD_HIQSTRIKE_MATCH != BLE_EXCLOAD_LOQSTRIKE_MATCH;
		
		if (!Misc.isUndef(me.startStrikeRelStartSec)) {
			metsStrikeLow = me.startTS+1000*me.startStrikeRelStartSec;
			metsStrikeHigh = me.startTS+1000*me.endStrikeRelStartSec;
			if (metsStrikeLow > meActEndTS || metsStrikeHigh < meActStartTS) {
				metsStrikeLow = -1;
				metsStrikeHigh = -1;
			}
		}
		if (!Misc.isUndef(rhs.startStrikeRelStartSec)) {
			rhstsStrikeLow = rhs.startTS+1000*rhs.startStrikeRelStartSec;
			rhstsStrikeHigh = rhs.startTS+1000*rhs.endStrikeRelStartSec;
			if (rhstsStrikeLow > rhsActEndTS || rhstsStrikeHigh < rhsActStartTS) {
				rhstsStrikeLow = -1;
				rhstsStrikeHigh = -1;
			}
		}
			
		if (doDiffHiqLoQ && (meQ== ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH || meQ == ShovelSequence.LBLE_EXCLOAD_HIQSTRIKE_MATCH)) {
			metsMatchHiStrikeLow = me.startTS+1000*me.firstMatchingStrikeRelStartSec;
			metsMatchHiStrikeHigh = me.startTS+1000*me.lastMatchingStrikeRelStartSec;
			if (metsMatchHiStrikeLow > meActEndTS || metsMatchHiStrikeHigh < meActStartTS) {
				metsMatchHiStrikeLow = -1;
				metsMatchHiStrikeHigh = -1;
			}
		}
		if (doDiffHiqLoQ && (rhsQ== ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH || rhsQ == ShovelSequence.LBLE_EXCLOAD_HIQSTRIKE_MATCH)) {
			rhstsMatchHiStrikeLow = rhs.startTS+1000*rhs.firstMatchingStrikeRelStartSec;
			rhstsMatchHiStrikeHigh = rhs.startTS+1000*rhs.lastMatchingStrikeRelStartSec;
			if (rhstsMatchHiStrikeLow > rhsActEndTS || rhstsMatchHiStrikeHigh < rhsActStartTS) {
				rhstsMatchHiStrikeLow = -1;
				rhstsMatchHiStrikeHigh = -1;
			}
		}
		long targetTripGapMilliForShift = (long)((avgCycleDur+SUGGESTED_TRIPGAP_CYCLETIME*deviation)*1000);//(long)(avgD*1.2*1000.0);//TODO parameterize (long)(avgD*SUGGESTED_TRIPGAP_CYCLETIME*1000.0);
		
		long deltaMilli = (long)(SUGGESTED_TRIPGAP_CYCLETIME*deviation*1000);
		if (deltaMilli < 1000)
			deltaMilli = 1000;
		else if (deltaMilli > 6000)
			deltaMilli = 6000;
		targetTripGapMilliForShift = (long)(avgCycleDur*1000+deltaMilli);
		if (targetTripGapMilliForShift > ABS_MAX_TRIPGAP_MILLI_WHEN_PROPER)
			targetTripGapMilliForShift = ABS_MAX_TRIPGAP_MILLI_WHEN_PROPER;
		
		//targetTripGapMilliForShift = (long)((avgCycleDur+2)*1000);//(long)(avgD*1.2*1000.0);//TODO parameterize (long)(avgD*SUGGESTED_TRIPGAP_CYCLETIME*1000.0);

		byte targetGapSecForShift = (byte)(targetTripGapMilliForShift/1000);

		int meCurrRightInCount = me.currRightInCount;
		int rhsCurrLeftInCount = rhs.currLeftInCount;
		int meCurrLeftInCount = me.currLeftInCount;
		int rhsCurrRightInCount = rhs.currRightInCount;
		byte durThreshForMissing = (byte) (2*avgCycleDur);
		
		if (durThreshForMissing > ABS_MIN_TRIPGAP_MILLI_WHEN_LO/1000)
			durThreshForMissing = (byte)(ABS_MIN_TRIPGAP_MILLI_WHEN_LO/1000);
		//give preference to moving start to right and then end to left
		if (isMeBefore) {//move me end to left if there are cycles .. else move rhs start to right if there are cycles
			
			int moveMeFirst = 1;
			//me to left of rhs. we will be shifting meEnd to left and/or rhsStart to right. We want to ensure proper gap between trips
			//gap between me and rhs taken care by targetGapBetweenTrip
			//but because of shifting of meEnd to left, # of cycles may become lo and in which case we want to have larger gap between me and one to left
			//if gap is not proper then we cannot shift
			boolean canMoveMeEndToLeft = true;
			boolean canMoveRHSStartToRight = true;
			
			if (!isMeEnded || !rhsEnded) {
				if (!rhsEnded) { //rhs after Me ... so can goto 0 .. while we keep whatever we can of me
					rhsMinCycleCount = ShovelSequence.MIN_SHOVEL_LO_CYCLES;
				}
				else {//should not happen .. if !meEnded and rhsEnded then isMeBefore = true cant happen
					meMinCycleCount = ShovelSequence.MIN_SHOVEL_LO_CYCLES;
				}
			}
			canMoveMeEndToLeft = (meCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) || me.endShiftableToLeft(meCurrRightInCount, targetGapSecForShift); 
			canMoveRHSStartToRight = (rhsCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) || rhs.startShiftableToRight(rhsCurrLeftInCount, targetGapSecForShift);
			
			long targetGapBetweenTrip = rhsCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES ? targetMilliSecWhenLo : targetMilliSecWhenProper;
			
			if (!isMeEnded && meCycleCount <= ShovelSequence.PREFERRED_SHOVEL_CYCLES) {
				canMoveMeEndToLeft = false;
			}
			if (!rhsEnded)
				canMoveRHSStartToRight = true;
			
			for (int art=0,arts = getAnyAsFeasible ? 3 : 2;art<arts;art++) {
				if (art == 2) {
					canMoveMeEndToLeft = true;
					meMinCycleCount = 1;//isMeEnded ? ShovelSequence.MIN_SHOVEL_LO_CYCLES-1 : (byte) 0;
				}
				//moving end of me to left .. we need to ensure minimum gap between end of me to rhsEnd
				if (canMoveMeEndToLeft && (moveMeFirst == art || art == 2) 
						&& (meActEndTS >= rhsActStartTS-targetGapBetweenTrip ||  (rhsEnded && isMeEnded && rhsActEndTS >= meActEndTS && rhsActEndTS-meActEndTS < END_TO_END_GAP_MILLI)) 
						&& meCycleCount > meMinCycleCount) {
					ExcLoadEvent dummy = new ExcLoadEvent(meActEndTS);
					boolean strikeMismatchSeenOnce = false;
					
					for (int i=0;;i--) {//moving end of me to left
						
						ExcLoadEvent currEvent = loadEvents.get(conn, dummy, i);
						if (currEvent == null || currEvent.getGpsRecordTime() < meActStartTS)
							break;
						if (currEvent.getGpsRecordTime() == dummy.getGpsRecordTime())
							continue;
						if ((currEvent.getIgnoreBecauseNeighbour(conn, loadEvents) &0x0F) != 0)
							continue;
						if (!isValidFromStrike(meActStartTS, currEvent.getGpsRecordTime(), metsMatchHiStrikeLow, metsMatchHiStrikeHigh, metsStrikeLow, metsStrikeHigh)) {
							if (strikeMismatchSeenOnce)
								break;
							strikeMismatchSeenOnce = true;
						}
						if (me.endTS-currEvent.getGpsRecordTime() >  ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS)
							break;
						if (isMeEnded && currEvent.getGpsRecordTime()-leftSideEndTS < ShovelSequence.END_TO_END_GAP_MILLI) //make sure we dont go too left
							break;
						
						toRecalcTimingOfLHS = true;
						meActEndTS = currEvent.getGpsRecordTime();
						meCycleCount--;
						meCurrRightInCount++;
						canMoveMeEndToLeft =art == 2  || !isMeEnded ||  (meCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
						   || (isMeEnded && me.endShiftableToLeft(meCurrRightInCount, targetGapSecForShift)); 
						if (!isMeEnded && meCycleCount <= ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
							canMoveMeEndToLeft = false;
										
						if (!canMoveMeEndToLeft || (meActEndTS < rhsActStartTS-targetGapBetweenTrip && (!rhsEnded || !isMeEnded || rhsActEndTS-meActEndTS >= END_TO_END_GAP_MILLI)) || meCycleCount <= meMinCycleCount)
							break;
					}
				}
				//we will be moving rhs start to right .. since end pt doesnt change so either end to end gap must be proper or one of them not ended
				if (canMoveRHSStartToRight && moveMeFirst != art 
						&& meActEndTS >= rhsActStartTS-targetGapBetweenTrip 
						//&&  (!rhsEnded || !isMeEnded || rhsActEndTS-meActEndTS >= END_TO_END_GAP_MILLI)
						&& rhsCycleCount > rhsMinCycleCount) {//move rhs to right
					ExcLoadEvent dummy = new ExcLoadEvent(rhsActStartTS);
					boolean strikeMismatchSeenOnce = false;
					for (int i=0;;i++) {//moving start of rhs to right
						ExcLoadEvent currEvent = loadEvents.get(conn, dummy, i);
						if (currEvent == null || currEvent.getGpsRecordTime() > rhsActEndTS)
							break;
						if (currEvent.getGpsRecordTime() == dummy.getGpsRecordTime())
							continue;
						if ((currEvent.getIgnoreBecauseNeighbour(conn, loadEvents) & 0x0F) != 0)
							continue;
		
						if (!isValidFromStrike(currEvent.getGpsRecordTime(), rhsActEndTS, rhstsMatchHiStrikeLow, rhstsMatchHiStrikeHigh, rhstsStrikeLow, rhstsStrikeHigh)) {
							if (strikeMismatchSeenOnce)
								break;
							strikeMismatchSeenOnce = true;
						}
	
						rhsActStartTS = currEvent.getGpsRecordTime();
						rhsCycleCount--;
						toRecalcTimingOfRHS = true;
						rhsCurrLeftInCount++;
						rhsStartShifted = true;
						canMoveRHSStartToRight = !rhsEnded || (rhsCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
						|| (rhsEnded && rhs.startShiftableToRight(rhsCurrLeftInCount, targetGapSecForShift));
						
						if (!canMoveRHSStartToRight || rhsCycleCount <= rhsMinCycleCount 
								|| (meActEndTS < rhsActStartTS-targetGapBetweenTrip)
								) {
							break;
						}
					}
				}
			}
			
			if ((meCycleCount == 0 && !isMeEnded) 
					|| (meActEndTS < rhsActStartTS-targetGapBetweenTrip && (!isMeEnded || !rhsEnded || rhsActEndTS-meActEndTS >= END_TO_END_GAP_MILLI) 
							&& meCycleCount >= meMinCycleCount)) {//feasible ... therefore update stuff
				meNotFeasible = false;
				if (toRecalcTimingOfLHS) {
					me.endLoadEventRelStartSec = me.getSecGap(meActEndTS);
					me.endActLoadRelStartSec = me.endLoadEventRelStartSec;
					if (meCycleCount == 0) {
						me.startLoadEventRelStartSec = me.endLoadEventRelStartSec = 0;
						me.endActLoadRelStartSec = 0;
						me.setActStartTS(me.startTS);
						me.currLeftInCount = 0;
					}
					me.cycleCount = (byte) meCycleCount;
					me.currRightInCount = (byte)meCurrRightInCount;
					//me end moved to left .. so reget left improper when recalculating
					meNotFeasible = !me.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleReads,false, false, false, true, null);
				}
				if (toRecalcTimingOfRHS) {
					
					rhsActStartSec = rhs.getSecGap(rhsActStartTS);
					rhsActEndSec = rhs.endLoadEventRelStartSec;
					if (rhsCycleCount == 0) {
						rhsActEndSec = rhsActStartSec = 0;
						rhsCurrRightInCount = rhsCurrLeftInCount = 0;
					}
				}
			}
			else {
				meNotFeasible = true;
				overlapCycle = 0;
				overlapSec = (int)((meActEndTS - (rhsActStartTS-targetGapBetweenTrip))/1000);
				if (meActStartTS >= rhsActStartTS || overlapSec < 0) {
					overlapSec = (int)((meActEndTS-meActStartTS)/1000);
				}
				int durTot = 0;
				if (me.durs != null && me.durs.length > 0 && meCurrRightInCount >= 0 && meCurrLeftInCount >= 0) {
					for (int i=me.durs.length-1-meCurrRightInCount;i>=meCurrLeftInCount;i--) {
						if (i < 0 || i >= me.durs.length)
							break;
						durTot += me.durs[i];
						overlapCycle++;
						if (durTot > overlapSec) {
							break;
						}
					}
				}
			}
		}
		else {//me is after rhs ... shift me start to right and optionally shift rhs end to left
			int moveMeFirst = 0;
			long targetGapBetweenTrip = meCycleCount < ShovelSequence.MIN_SHOVEL_CYCLES ? targetMilliSecWhenLo : targetMilliSecWhenProper;
			
			//rhs to left of me - we will be shifting rhs end to left and/or meStart to right. When shifting rhs to left we cannot let cycle count becoming min if the gap is < left
			boolean canMoveRhsEndToLeft = true;
			boolean canMoveMeStartToRight = true;
			if (!isMeEnded || !rhsEnded) {
				if (!isMeEnded) {
					meMinCycleCount = ShovelSequence.MIN_SHOVEL_LO_CYCLES;
				}
				else {//should not happen if !rhsEnded && isMeEnded then isMeBefore = true
					rhsMinCycleCount = ShovelSequence.MIN_SHOVEL_LO_CYCLES;
				}
					
			}
			canMoveMeStartToRight = (meCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) || me.startShiftableToRight(meCurrLeftInCount, targetGapSecForShift); 
			canMoveRhsEndToLeft = (rhsCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) || rhs.endShiftableToLeft(rhsCurrRightInCount, targetGapSecForShift);
			
			if (!rhsEnded && rhsCycleCount <= ShovelSequence.PREFERRED_SHOVEL_CYCLES) {
				canMoveRhsEndToLeft = false;
			}
			if (!isMeEnded)
				canMoveMeStartToRight = true;
			
			for (int art=0,arts = getAnyAsFeasible ? 3 : 2;art<arts;art++) {
				if (art == 2) {
					canMoveMeStartToRight = true;
					meMinCycleCount = 1;//isMeEnded ? ShovelSequence.MIN_SHOVEL_LO_CYCLES-1 : (byte) 0;
				}
				//moving meStartToRight .. end cant change so no check - but still ActEnd to rhsEnd gap must be valid
				if (canMoveMeStartToRight && (art == 2 || moveMeFirst == art)
						   && rhsActEndTS >= meActStartTS-targetGapBetweenTrip 
						   //&& (!rhsEnded || !isMeEnded || meActEndTS-rhsActEndTS >=   END_TO_END_GAP_MILLI) 
						   && meCycleCount > meMinCycleCount) {
					ExcLoadEvent dummy = new ExcLoadEvent(meActStartTS);
					boolean strikeMismatchSeenOnce = false;
					for (int i=0;;i++) {//moving me start to right
						ExcLoadEvent currEvent = loadEvents.get(conn, dummy, i);
						if (currEvent == null || currEvent.getGpsRecordTime() > meActEndTS)
							break;
						if (currEvent.getGpsRecordTime() == dummy.getGpsRecordTime())
							continue;
						if ((currEvent.getIgnoreBecauseNeighbour(conn, loadEvents) & 0x0F) != 0)
							continue;
						if (!isValidFromStrike(currEvent.getGpsRecordTime(), meActEndTS, metsMatchHiStrikeLow, metsMatchHiStrikeHigh, metsStrikeLow, metsStrikeHigh)) {
							if (strikeMismatchSeenOnce)
								break;
							strikeMismatchSeenOnce = true;
						}
	
						meActStartTS = currEvent.getGpsRecordTime();
						meCycleCount--;
						toRecalcTimingOfLHS = true;
						meCurrLeftInCount++;
						meStartShifted = true;
						canMoveMeStartToRight = art == 2 || !isMeEnded || (meCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
						   || (isMeEnded && me.startShiftableToRight(meCurrLeftInCount, targetGapSecForShift)); 
						
						if (!canMoveMeStartToRight || rhsActEndTS < meActStartTS-targetGapBetweenTrip || meCycleCount <= meMinCycleCount) {
							break;
						}
					}
				}
				//rhsEnd to be moved to left - need to ensure that we dont break the leftEndBndConstraint
				if (canMoveRhsEndToLeft  && moveMeFirst != art 
						&& ((rhsActEndTS >= (meActStartTS-targetGapBetweenTrip) || (meActEndTS > rhsActEndTS && isMeEnded && rhsEnded && (meActEndTS-rhsActEndTS) < ShovelSequence.END_TO_END_GAP_MILLI)))  
								&& rhsCycleCount > rhsMinCycleCount) {//move rhs end to left
					ExcLoadEvent dummy = new ExcLoadEvent(rhsActEndTS);
					boolean strikeMismatchSeenOnce = false;
					for (int i=0;;i--) {//move rhs end to left
						ExcLoadEvent currEvent = loadEvents.get(conn, dummy, i);
						if (currEvent == null || currEvent.getGpsRecordTime() < rhsActStartTS)
							break;
						if (currEvent.getGpsRecordTime() == dummy.getGpsRecordTime())
							continue;
						if ((currEvent.getIgnoreBecauseNeighbour(conn, loadEvents) & 0x0F) != 0)
							continue;
						if (!isValidFromStrike(rhsActStartTS, currEvent.getGpsRecordTime(), rhstsMatchHiStrikeLow, rhstsMatchHiStrikeHigh, rhstsStrikeLow, rhstsStrikeHigh)) {
							if (strikeMismatchSeenOnce)
								break;
							strikeMismatchSeenOnce = true;
						}
						if (rhs.endTS-currEvent.getGpsRecordTime() >  ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS)
							break;
						if (rhsEnded && currEvent.getGpsRecordTime()-leftSideEndTS < ShovelSequence.END_TO_END_GAP_MILLI) //make sure we dont go too left
							break;

						
						rhsActEndTS = currEvent.getGpsRecordTime();
						rhsCycleCount--;
						toRecalcTimingOfRHS = true;
						rhsCurrRightInCount ++;
						canMoveRhsEndToLeft = !rhsEnded || (rhsCycleCount > ShovelSequence.PREFERRED_SHOVEL_CYCLES) 
						|| (rhsEnded && rhs.endShiftableToLeft(rhsCurrRightInCount, targetGapSecForShift));
						if (!rhsEnded && rhsCycleCount <= ShovelSequence.PREFERRED_SHOVEL_CYCLES) {
							canMoveRhsEndToLeft = false;
						}
						if (!canMoveRhsEndToLeft || rhsCycleCount <= rhsMinCycleCount    
								|| (meActStartTS > rhsActEndTS+targetGapBetweenTrip && (!isMeEnded || !rhsEnded || meActEndTS-rhsActEndTS >= ShovelSequence.END_TO_END_GAP_MILLI)))
							break;
					}
				}
		    }
			if ((meCycleCount == 0 && !isMeEnded) || (meActStartTS > rhsActEndTS+targetGapBetweenTrip && (!isMeEnded || !rhsEnded || meActEndTS-rhsActEndTS > ShovelSequence.END_TO_END_GAP_MILLI) && meCycleCount >= meMinCycleCount)) {//feasible ... therefore update stuff
				meNotFeasible = false;
				if (toRecalcTimingOfLHS) {
					me.startLoadEventRelStartSec = me.getSecGap(meActStartTS);
					me.setActStartTS(meActStartTS);

					if (meCycleCount == 0) {
						me.endActLoadRelStartSec = me.endLoadEventRelStartSec = me.startLoadEventRelStartSec = 0;
						me.setActStartTS(me.startTS);
						me.currRightInCount = 0;
					}
					me.cycleCount = (byte) meCycleCount;
					me.currLeftInCount = (meCycleCount == 0 && !isMeEnded) ? - 1 : (byte) meCurrLeftInCount;
					meNotFeasible = !me.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleReads,false, false, false, true, null);
				}
				if (toRecalcTimingOfRHS) {
					rhsActStartSec = rhs.startLoadEventRelStartSec;
					rhsActEndSec =  rhs.getSecGap(rhsActEndTS);
					if (rhsCycleCount == 0) {
						rhsActStartSec = rhsActEndSec = 0;
						rhsCurrLeftInCount = rhsCurrRightInCount = 0;
					}
					
				}
			}
			else {
				meNotFeasible = true;
				overlapCycle = 0;
				overlapSec = (int)((rhsActEndTS+targetGapBetweenTrip - meActStartTS)/1000);
				if (meActEndTS <= rhsActEndTS || overlapSec < 0) {
					overlapSec = (int)((meActEndTS-meActStartTS)/1000);
				}
				int durTot = 0;
				
				if (me.durs != null && me.durs.length > 0) {
					for (int i=meCurrLeftInCount+1,is = me.durs.length-1-meCurrRightInCount;i<=is;i++) {
						if (i < 0 || i >= me.durs.length)
							break;
						durTot += me.durs[i];
						overlapCycle++;
						if (durTot > overlapSec) {
							break;
						}
					}
				}
				
			}
		}//if me is after rhs
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.adjustTiming(System.nanoTime()-timingMarker1);

		ShovelSequence.OverlapChangeInfo retval =  new ShovelSequence.OverlapChangeInfo(false, meNotFeasible, rhsActStartSec, rhsActEndSec , (byte) rhsCycleCount, rhs, rhsCurrLeftInCount, rhsCurrRightInCount, null);
		retval.overlapSec = overlapSec;
		retval.overlapCycleCount = overlapCycle;
		return retval;
	}

	public OverlapChangeInfo updateMeComboForOverlapAndCheckOverlapNeedsRecalc(ShovelSequenceHolder shovelInfo, ShovelSequence rhs, NewExcLoadEventMgmt loadEvents, Connection conn, NewVehicleData vdp, NewBLEMgmt bleReads, NewVehicleData strikeVDT, NewVehicleData shovelIdling, long otherHardLeft, long otherHardRight, int meLeftGapSec, int rhsLeftGapSec, boolean getAnyAsFeasible) {
		//first = true if rhs needs to be unfirmed ..
		//second = true if me is not feasible here
		//third = second is false ... ie. me can be fitted and rhs need not be unfirmed, but its timing is changed
	//useEndForDetermingEarlier - generally true ... but if both have load event and actStart < while actEnd >, we will try the alt approach too
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();

		int meQReRHS = this.meBetterThan(rhs);
   		long meStartTS = this.getActStartTS();
   		short meActStartSec = this.getStartActLoadRelStartSec();
		long meEndTS = this.startTS + this.endActLoadRelStartSec*1000;
		long rhsStartTS = rhs.getStartTS();
		short rhsActStartSec = rhs.getStartActLoadRelStartSec();
		long rhsEndTS = rhs.startTS + rhs.endActLoadRelStartSec*1000;
		long meStrikeTS = Misc.isUndef(this.startStrikeRelStartSec) ? -1 : this.startTS+this.startStrikeRelStartSec*1000;
		long rhsStrikeTS = Misc.isUndef(rhs.startStrikeRelStartSec) ? -1 : rhs.startTS+rhs.startStrikeRelStartSec*1000;
		
		long meRhsEndGap =( this.endActLoadRelStartSec*1000+this.startTS) - (rhs.endActLoadRelStartSec*1000+rhs.startTS);
		if (meRhsEndGap == 0) 
			meRhsEndGap = this.endTS - rhs.endTS;
		if (meRhsEndGap == 0)
			meRhsEndGap = ( meStartTS) - (rhsStartTS);
		if (meRhsEndGap == 0) 
			meRhsEndGap = this.startTS - rhs.startTS;
		boolean isMeBeforeByEnd = meRhsEndGap < 0;
		/* torem
		long meRhsStartGap =( meStartTS) - (rhsStartTS);
		if (meRhsStartGap == 0) 
			meRhsStartGap = this.startTS - rhs.startTS;
		if (meRhsStartGap == 0)
			meRhsStartGap = ( this.endActLoadRelStartSec*1000+this.startTS) - (rhs.endActLoadRelStartSec*1000+rhs.startTS);
		if (meRhsStartGap == 0) 
			meRhsStartGap = this.endTS - rhs.endTS;
		boolean isMeBeforeByStart = meRhsStartGap < 0; //not really needed
		boolean isMeBefore = isMeBeforeByEnd;
		*/
		//if (rhs.endTS < this.endTS && rhs.getStartTS() >= this.startTS)
		//	this.countOutBeforeMe++;
		boolean rhsUnfirm = false;
		byte rhsCycleCount = rhs.cycleCount;
		boolean meNotFeasible = true;
		
		boolean done = false;
		boolean recalcActLoadTiming = false;
		
		short initMeActLoadStart = this.getStartActLoadRelStartSec();
		short initMeActLoadEnd = this.endActLoadRelStartSec;
		short initMeLoadEventStart = this.startLoadEventRelStartSec;
		short initMeLoadEventEnd = this.endLoadEventRelStartSec;
		byte initMeCycleCount = this.cycleCount;
		byte initMeDumperQ = this.getDumperBestQuality();
		byte initMeLeft = this.currLeftInCount;
		byte initMeRight = this.currRightInCount;
		boolean isMeEnded = this.isStopEnded();// || this.hasInactivityAfterBefStop();
		boolean rhsEnded = rhs.isStopEnded();// || rhs.hasInactivityAfterBefStop();

		if (isMeEnded && !rhsEnded) {
			//isMeBefore = isMeBeforeByEnd = isMeBeforeByStart = true;
			isMeBeforeByEnd = true;
		}
		else if (!isMeEnded && rhsEnded) {
			isMeBeforeByEnd = false;//isMeBefore = isMeBeforeByEnd = isMeBeforeByStart = false;
		}
		else if (!isMeEnded && !rhsEnded) {
			//isMeBefore = isMeBeforeByEnd = isMeBeforeByStart = this.startTS <= rhs.startTS;
			if (this.startTS != this.endTS) {
				isMeBeforeByEnd = this.startTS < rhs.startTS;
			}
			else if (meStartTS != rhsStartTS){
				isMeBeforeByEnd = meStartTS < rhsStartTS;
			}
			else if (meEndTS < rhsEndTS){
				isMeBeforeByEnd = meEndTS < rhsEndTS;
			}
			else {
				isMeBeforeByEnd = this.endTS < rhs.endTS;
			}
		}
		
		//if (!done && !Misc.isUndef(this.startLoadEventRelStartSec) && !Misc.isUndef(rhs.startLoadEventRelStartSec)) {
		int minOverlapSec = -1;
		int minOverlapCycle = -1;
		boolean canMakeRHSUnfirm = !getAnyAsFeasible && (isMeBeforeByEnd || rhs.multShoveFeasibleCnt > 0);////20190130 was > 0
		if (canMakeRHSUnfirm) {
			boolean wasUnfirmedBy = shovelInfo.wasUnfirmed(this, rhs);
			if (!wasUnfirmedBy)
				wasUnfirmedBy = shovelInfo.hadUnfirmedMultipleTimes(this, rhs);
			if (wasUnfirmedBy)
				canMakeRHSUnfirm = false;
		}
		////20190130
		if (false && canMakeRHSUnfirm && meQReRHS < 0 && (rhs.getDumperBestQuality() == ShovelSequence.BLE_EXCLOAD_HIQSTRIKE_MATCH || rhs.getDumperBestQuality() == ShovelSequence.NEAR_EXCLOAD_HIQSTRIKE_MATCH))
				canMakeRHSUnfirm = false;
		////20190130
		if (true && canMakeRHSUnfirm && rhs.multShoveFeasibleCnt <= 0 && this.multShoveFeasibleCnt > 0)
			canMakeRHSUnfirm = false;
		if (canMakeRHSUnfirm) {
			meNotFeasible = false;
			rhsUnfirm = true;
			done = true;
			OverlapChangeInfo retval =  new OverlapChangeInfo(rhsUnfirm, meNotFeasible, Misc.getUndefShort(), Misc.getUndefShort() ,rhsCycleCount, rhs,0,0, null);
			return retval;
		}
		long leftEndTS = 0;
		if (leftEndTS <= 0 || otherHardLeft < leftEndTS)
			leftEndTS = otherHardLeft;
		if (leftEndTS <= 0 || (meLeftGapSec >= 0 && (this.getActStartTS()-meLeftGapSec*1000) < leftEndTS))
			leftEndTS = (this.getActStartTS()-meLeftGapSec*1000);
		if (leftEndTS <= 0 || (rhsLeftGapSec >= 0 && (rhs.getActStartTS()-rhsLeftGapSec*1000) < leftEndTS))
			leftEndTS = (rhs.getActStartTS()-rhsLeftGapSec*1000);

		if (!done && (this.isHasValidExcEventsAroundPeriod()) && (rhs.isHasValidExcEventsAroundPeriod())) {
						
			//check if there is overlap ... and if meIsBetter  or before then meFeasible, rhsNotFeasibel
			done = true;
			OverlapChangeInfo retval = adjustTimingForLoadEventWhenOtherBetter(shovelInfo, this, rhs, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, isMeBeforeByEnd, true,true,true,true, meLeftGapSec, rhsLeftGapSec, false,leftEndTS);
			if (retval.meNotFeasible) {// &&  isMeBeforeByStart != isMeBeforeByEnd) {
				if (retval.overlapCycleCount < minOverlapCycle || minOverlapCycle < 0) {
					minOverlapCycle = retval.overlapCycleCount;
					minOverlapSec= retval.overlapSec;
				}
				//if either is NotEnded then no point in flipping priority
				if (isMeEnded && rhsEnded)
					retval = adjustTimingForLoadEventWhenOtherBetter(shovelInfo, this, rhs, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, !isMeBeforeByEnd, true,true,true,true, meLeftGapSec, rhsLeftGapSec, false,leftEndTS); 
			}
			if (retval.meNotFeasible && getAnyAsFeasible) {
				retval = adjustTimingForLoadEventWhenOtherBetter(shovelInfo, this, rhs, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, isMeBeforeByEnd, true,true,true,true, meLeftGapSec, rhsLeftGapSec, true,leftEndTS);
			}
			if (retval.meNotFeasible) {
				if (retval.overlapCycleCount < minOverlapCycle || minOverlapCycle < 0) {
					minOverlapCycle = retval.overlapCycleCount;
					minOverlapSec= retval.overlapSec;
				}
			}
			//if (retval.meNotFeasible && rhs.leastQCausingTimeChange < 127 && rhs.leastQCausingTimeChange < this.getDumperBestQuality()) {
			if (!retval.meNotFeasible)
				retval.dbgString = "Regular";
			
			if (retval.meNotFeasible && !getAnyAsFeasible && (rhs.hasMultipleSubWindow() || rhs.baseActStartRelStartSec != rhsActStartSec || rhs.baseActEndRelStartSec != rhs.endActLoadRelStartSec) ) {
				if (true) {//new code
					ShovelSequence tempRHS = rhs.quickCopyForRegetTime();
					ArrayList<ShovelSequence.ShovelActivityInfo> otherAltShovelActivityInfoExclBest = new ArrayList<ShovelSequence.ShovelActivityInfo>();	
					//dont do Likely
					boolean isLoadEventCompatible = tempRHS.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleReads, false, true, true, false,otherAltShovelActivityInfoExclBest);
					
					if (isLoadEventCompatible) {
						int szotherAltShovelActivityInfoExclBest = otherAltShovelActivityInfoExclBest == null ? 0 : otherAltShovelActivityInfoExclBest.size();
						boolean foundFeasibleOverlapWithSub = false;
						for (int tryAttempt = szotherAltShovelActivityInfoExclBest;tryAttempt>=0 && !foundFeasibleOverlapWithSub;tryAttempt--) {
							if (tryAttempt < szotherAltShovelActivityInfoExclBest) {
								ShovelSequence.ShovelActivityInfo tempActivityInfo = otherAltShovelActivityInfoExclBest.get(tryAttempt);
								tempRHS.setThisTimingToShovelActivityInfo(tempActivityInfo, true, false, true, conn, loadEvents, shovelIdling, strikeVDT != null);
								tempRHS.setHasInactivityAfterBefStop(true);
							}
							boolean valid = tempRHS.modifyTimingAsPerWindow(otherHardLeft, otherHardRight);
							if (!valid)
								continue;
							OverlapChangeInfo temRetval =adjustTimingForLoadEventWhenOtherBetter(shovelInfo, this, tempRHS, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, isMeBeforeByEnd, true, true, true, true, meLeftGapSec, rhsLeftGapSec, false,leftEndTS);
							if (temRetval.meNotFeasible) {//  &&  isMeBeforeByStart != isMeBeforeByEnd) {
								temRetval = adjustTimingForLoadEventWhenOtherBetter(shovelInfo, this, tempRHS, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, !isMeBeforeByEnd, true,true,true,true, meLeftGapSec, rhsLeftGapSec, false,leftEndTS); 
							}
							if (!temRetval.meNotFeasible) {
								foundFeasibleOverlapWithSub = true;
								short initRhsStartSec = rhsActStartSec;
								short initRhsEndSec = rhs.endActLoadRelStartSec;
								short propRhsStartSec = tempRHS.getStartActLoadRelStartSec();
								short propRhsEndSec = tempRHS.endActLoadRelStartSec;
								byte propCurrLeftInCount = tempRHS.currLeftInCount;
								byte propCurrRightInCount = tempRHS.currRightInCount;
								if (temRetval.timingChangeInfo != null && !Misc.isUndef(temRetval.timingChangeInfo.rhsActStartSec)) {
									propRhsStartSec = temRetval.timingChangeInfo.rhsActStartSec;
									propRhsEndSec = temRetval.timingChangeInfo.rhsActEndSec;
									propCurrLeftInCount = temRetval.timingChangeInfo.currLeftInCount;
									propCurrRightInCount = temRetval.timingChangeInfo.currRightInCount;
								}
								if (initRhsStartSec != propRhsStartSec || initRhsEndSec != propRhsEndSec) {
									if (temRetval.timingChangeInfo == null) {
										temRetval.timingChangeInfo = new ShovelSequence.OverlapTimingChangeInfo(rhs,propRhsStartSec, propRhsEndSec, tempRHS.cycleCount,propCurrLeftInCount, propCurrRightInCount, tempRHS, Misc.getUndefInt());
										//NEWAPPROACH temRetval.timingChangeInfo.rhsChangeWithFullData = true;
									}
									else {
										temRetval.timingChangeInfo.other = rhs;
										temRetval.timingChangeInfo.replaceOtherWithThisIfNotNull = tempRHS;
									}
								}
								retval = temRetval;
							}
						}//for each try attempt
					}//initial getTiming of tempRHS was OK
				}//end of new code
			}//if can be checked with diff window
			
			
			if (ThreadContextCache.g_shovelStat != null)
				ThreadContextCache.g_shovelStat.overlapCheck(System.nanoTime()-timingMarker1);
			retval.overlapCycleCount = minOverlapCycle;
			retval.overlapSec = minOverlapSec;
			return retval;
		}//if both have valid load events
		
		OverlapChangeInfo retval = adjustTimingForNonLoadEventWhenOtherBetter(shovelInfo, this, rhs, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, isMeBeforeByEnd);
		if (retval.meNotFeasible) {
			if (retval.overlapCycleCount < minOverlapCycle || minOverlapCycle < 0) {
				minOverlapCycle = retval.overlapCycleCount;
				minOverlapSec= retval.overlapSec;
			}
			if (isMeEnded && rhsEnded) {//isMeBeforeByStart != isMeBeforeByEnd) {
				retval = adjustTimingForNonLoadEventWhenOtherBetter(shovelInfo, this, rhs, loadEvents, conn, vdp, bleReads, strikeVDT, shovelIdling, !isMeBeforeByEnd);
				if (retval.overlapCycleCount < minOverlapCycle || minOverlapCycle < 0) {
					minOverlapCycle = retval.overlapCycleCount;
					minOverlapSec= retval.overlapSec;
				}
			}
		}
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.overlapCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	
	public static  OverlapChangeInfo adjustTimingForNonLoadEventWhenOtherBetter(ShovelSequenceHolder shovelInfo, ShovelSequence me, ShovelSequence rhs, NewExcLoadEventMgmt loadEvents, Connection conn, NewVehicleData vdp, NewBLEMgmt bleReads, NewVehicleData strikeVDT, NewVehicleData shovelIdling, boolean isMeBefore) {
		long meStartTS = me.getActStartTS();
		long meEndTS = me.getActEndTS();
		long rhsStartTS = rhs.getActStartTS();
		long rhsEndTS = rhs.getActEndTS();
		boolean isMeEnded = me.isStopEnded();// || me.hasInactivityAfterBefStop();
		boolean rhsEnded = rhs.isStopEnded();// || rhs.hasInactivityAfterBefStop();
	
		ShovelSequence tempRhs = null;
		long threshGapMilli = 25*1000;//20*1000;//TODO parameterize
		boolean canChangeMe = !me.isHasValidExcEventsAroundPeriod() 
		    && ((meEndTS-meStartTS) > (isMeEnded ? G_NON_OVERLAP_MILLI_FOR_LOAD : ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS));
		boolean canChangeRHS = !rhs.isHasValidExcEventsAroundPeriod() 
		    && ((rhsEndTS-rhsStartTS) > (rhsEnded ? G_NON_OVERLAP_MILLI_FOR_LOAD : ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS));
		
		if (!isMeEnded || !rhsEnded) {
			if (isMeBefore) {
				if (meEndTS-meStartTS < G_NON_OVERLAP_MILLI_FOR_LOAD)
					canChangeMe = false;
			}
			else {
				if (rhsEndTS-rhsStartTS < G_NON_OVERLAP_MILLI_FOR_LOAD)
					canChangeRHS = false;
			}
		}
		boolean feasible = false;
		boolean changeRHS = false;
		boolean changeLHS = false;
		short propRhsStartSec = Misc.getUndefShort();
		short propRhsEndSec = Misc.getUndefShort();
		int overlapSec = -1;
		int overlapCount = -1;
		long meCompletionThresh = isMeEnded ? G_NON_OVERLAP_MILLI_FOR_LOAD : ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS;
		long rhsCompletionThresh = rhsEnded ? G_NON_OVERLAP_MILLI_FOR_LOAD : ShovelSequence.MIN_SHOVEL_NOCYCLE_ONGOING_MINTS;
		
		if (isMeBefore) {
			if (meEndTS <= rhsStartTS-threshGapMilli) {
				feasible = true;
			}
			else {
				if (canChangeMe) {
					meEndTS = rhsStartTS - threshGapMilli;
					changeLHS = true;
					if ((meEndTS - meStartTS) < meCompletionThresh) {
						//can't shift that much ..
						
						meEndTS = Math.min(me.endTS, meStartTS+G_NON_OVERLAP_MILLI_FOR_LOAD);
						if (canChangeRHS) {
							rhsStartTS = Math.min(Math.max(rhs.startTS, meEndTS+threshGapMilli), rhs.endTS);
							
							if (rhsEndTS-rhsStartTS < rhsCompletionThresh) {
								feasible = false;
								rhsStartTS = rhs.getActStartTS();
							}
							else {
								feasible = true;
								changeRHS = true;
							}
						}
						
					}
					else {
						feasible = true;
					}
				}
				else { //can only changeRHS 
					rhsStartTS = Math.min(Math.max(rhs.startTS, meEndTS+threshGapMilli), rhs.endTS);
					if (rhsEndTS-rhsStartTS < rhsCompletionThresh) { 
						feasible = false;
						rhsStartTS = rhs.getActStartTS();
					}
					else {
						feasible = true;
						changeRHS = true;
					}
				}
			}//was initially not feasible
			if (!feasible) {//meBefore
				overlapSec = (int)((meEndTS-rhsStartTS+threshGapMilli)/1000);
				if (overlapSec < 0 || rhsStartTS <= meStartTS) {
					overlapSec = (int)((meEndTS-meStartTS+threshGapMilli)/1000);
				}
			}
		} //was before
		else { //was after
			if (meStartTS >= rhsEndTS+threshGapMilli) {//  || (meStartTS == meEndTS && !isMeEnded) || (rhsStartTS == rhsEndTS && !rhsEnded)) {
				feasible = true;
			}
			else {//rhsBefore Me
				if (canChangeRHS) {
					rhsEndTS = meStartTS - threshGapMilli;
					changeRHS = true;
					if ((rhsEndTS - rhsStartTS) < rhsCompletionThresh) {
						//can't shift that much ..
						
						rhsEndTS = Math.min(rhs.endTS, rhsStartTS+G_NON_OVERLAP_MILLI_FOR_LOAD);
						if (canChangeMe) {
							meStartTS = Math.min(Math.max(me.startTS, rhsEndTS+threshGapMilli), me.endTS);
							
							if (meEndTS-meStartTS < meCompletionThresh) {
								feasible = false;
								meStartTS = me.getActStartTS();
							}
							else {
								feasible = true;
								changeLHS = true;
							}
						}
						
					}
					else {
						feasible = true;
					}
				}
				else { //can only changeLHS 
					meStartTS = Math.min(Math.max(me.startTS, rhsEndTS+threshGapMilli), me.endTS);
					if (meEndTS-meStartTS < meCompletionThresh) { 
						feasible = false;
						meStartTS = me.getActStartTS();
					}
					else {
						feasible = true;
						changeLHS = true;
					}
				}
			}//was not initially feasible
			if (!feasible) {//get overlapAmount
				overlapSec = (int)((rhsEndTS-meStartTS+threshGapMilli)/1000);
				if (overlapSec < 0 || rhsStartTS >= meStartTS) {
					overlapSec = (int)((meEndTS-meStartTS+threshGapMilli)/1000);
				}
			}
		}//if not before
		if (feasible) {
			if (changeLHS) {
				if (meStartTS == meEndTS) {
					meStartTS = meEndTS = me.startTS;
				}
				me.setActStartTS(meStartTS);
				me.endActLoadRelStartSec = me.getSecGap(meEndTS);
				feasible = me.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleReads, false, false, false, true, null);
			}
			if (feasible && changeRHS) {
				if (rhsStartTS == rhsEndTS)
					rhsStartTS = rhsEndTS = rhs.startTS;
				propRhsStartSec = rhs.getSecGap(rhsStartTS);
				propRhsEndSec = rhs.getSecGap(rhsEndTS);
			}
		}
		ShovelSequence.OverlapChangeInfo retval =  new ShovelSequence.OverlapChangeInfo(false, !feasible, propRhsStartSec, propRhsEndSec , (byte) rhs.getCycleCount(), rhs, 0, 0, null);
		retval.overlapSec = overlapSec;
		retval.overlapCycleCount = -1;
		return retval;
	}
	
	public ShovelSequence(long startTS) {
		this.dumperId = Misc.getUndefInt();
		this.startTS = startTS;
	}
	public ShovelSequence (int dumperId, long startTS, long endTS) {
		this.dumperId = dumperId;
		this.startTS = startTS;
		this.endTS = endTS;
	}
	
	public boolean isEnoughTime() {
		return (this.getActEndTS()-this.getActStartTS()) < ShovelSequence.G_NON_OVERLAP_MILLI_FOR_LOAD;
		//return (this.endActLoadRelStartSec-this.startActLoadRelStartSec)*1000 < ShovelSequence.G_NON_OVERLAP_MILLI_FOR_LOAD;
	}
	
	public ShovelSequence (int dumperId, long startTS, long endTS, byte strikeCount, short startStrikeRelStartSec, short endStrikeRelStartSec) {
		this.dumperId = dumperId;
		this.startTS = startTS;
		this.endTS = endTS;
		this.strikeCount = strikeCount;
		this.startStrikeRelStartSec = startStrikeRelStartSec;
		this.endStrikeRelStartSec = endStrikeRelStartSec;
	}
	
	public int getDumperId() {
		return dumperId;
	}
	public void setDumperId(int dumperId) {
		this.dumperId = dumperId;
	}
	
	public int compareTo(ShovelSequence rhs) {
		long res = SHOVELSEQ_BY_ACT ? this.actStartTS-rhs.actStartTS : this.startTS - rhs.startTS;//this.startTS - rhs.startTS;
		if (res == 0)
			res = this.dumperId < rhs.dumperId ? -1 : this.dumperId == rhs.dumperId ? 0 : 1;
		
		return res < 0 ? -1 : res > 0 ? 1 : 0;
	}
	
	public byte getDumperBestQuality() {
		return dumperBestQuality;
	}
	public boolean isDumperQFromBLE() {
		return dumperBestQuality >= BLE_NOEXCLOAD_MAYBESTRIKE;
	}
	public void setDumperBestQuality(byte dumperBestQuality) {
		this.dumperBestQuality = dumperBestQuality;
	}
	public byte getStrikeCount() {
		return strikeCount;
	}
	public void setStrikeCount(byte strikeCount) {
		this.strikeCount = strikeCount;
	}
	
	public long getStartTSActAdj() {
		return this.actStartTS < this.startTS ? this.actStartTS : this.startTS;
		//return this.startActLoadRelStartSec < 0 ? this.startTS+1000*this.startActLoadRelStartSec : this.startTS;
	}
	public long getEndTSActAdj() {
		long en = this.getActEndTS();
		return en < this.endTS ? this.endTS : en;
	}

	public long getStartTS() {
		return startTS;
	}
	public void setStartTS(long startTS) {
		this.startTS = startTS;
	}
	public long getEndTS() {
		return endTS;
	}
	public void setEndTS(long endTS) {
		this.endTS = endTS;
	}
	public short getStartStrikeRelStartSec() {
		return startStrikeRelStartSec;
	}
	public void setStartStrikeRelStartSec(short startStrikeRelStartSec) {
		this.startStrikeRelStartSec = startStrikeRelStartSec;
	}
	public short getEndStrikeRelEndSec() {
		return endStrikeRelStartSec;
	}
	public void setEndStrikeRelStartSec(short endStrikeRelStartSec) {
		this.endStrikeRelStartSec = endStrikeRelStartSec;
	}
	
	public short getEndStrikeRelStartSec() {
		return endStrikeRelStartSec;
	}
	
	public short getStartBLERelStartSec() {
		return startBLERelStartSec;
	}
	public void setStartBLERelStartSec(short startBLERelStartSec) {
		this.startBLERelStartSec = startBLERelStartSec;
	}
	public short getEndBLERelStartSec() {
		return endBLERelStartSec;
	}
	public void setEndBLERelStartSec(short endBLERelStartSec) {
		this.endBLERelStartSec = endBLERelStartSec;
	}
	public short getStartLoadEventRelStartSec() {
		return startLoadEventRelStartSec;
	}
	public void setStartLoadEventRelStartSec(short startLoadEventRelStartSec) {
		this.startLoadEventRelStartSec = startLoadEventRelStartSec;
	}
	public short getEndLoadEventRelStartSec() {
		return endLoadEventRelStartSec;
	}
	public void setEndLoadEventRelStartSec(short endLoadEventRelStartSec) {
		this.endLoadEventRelStartSec = endLoadEventRelStartSec;
	}
	
	public short getComboEndRelStartSec() {
		return endActLoadRelStartSec;
	}
	public void setComboEndRelStartSec(short comboEndRelStartSec) {
		this.endActLoadRelStartSec = comboEndRelStartSec;
	}

	
	
	public short getStartActLoadRelStartSec() {
		return this.getSecGap(this.actStartTS);
	}
	public void setStartActLoadRelStartSec(short sec) {
		this.setActStartTS(sec*1000+this.startTS);
	}
	public long getActStartTS() {
		return actStartTS <= 0 ? this.startTS : this.actStartTS;
		//return Misc.isUndef(startActLoadRelStartSec) ? this.startTS : this.startTS+this.startActLoadRelStartSec*1000;
	}
	public void setActStartTS(long ts) {
		this.actStartTS = ts;
	}
	public long getActEndTS() {
		return Misc.isUndef(endActLoadRelStartSec) ? this.startTS : this.startTS+this.endActLoadRelStartSec*1000;
	}



	public short getEndActLoadRelStartSec() {
		return endActLoadRelStartSec;
	}

	public void setEndActLoadRelStartSec(short endActLoadRelStartSec) {
		this.endActLoadRelStartSec = endActLoadRelStartSec;
	}

	public short getFirstMatchingStrikeRelStartSec() {
		return firstMatchingStrikeRelStartSec;
	}

	public void setFirstMatchingStrikeRelStartSec(
			short firstMatchingStrikeRelStartSec) {
		this.firstMatchingStrikeRelStartSec = firstMatchingStrikeRelStartSec;
	}

	public short getLastMatchingStrikeRelStartSec() {
		return lastMatchingStrikeRelStartSec;
	}

	public void setLastMatchingStrikeRelStartSec(short lastMatchingStrikeRelStartSec) {
		this.lastMatchingStrikeRelStartSec = lastMatchingStrikeRelStartSec;
	}

	public byte getLoadActivityStrikeMatchCount() {
		return loadActivityStrikeMatchCount;
	}

	public void setLoadActivityStrikeMatchCount(byte loadActivityStrikeMatchCount) {
		this.loadActivityStrikeMatchCount = loadActivityStrikeMatchCount;
	}
	public byte getBaseCycleCount() {
		return baseCycleCount;
	}
	public byte getCycleCount() {
		return cycleCount;
	}
	public void setCycleCount(byte cycleCount) {
		this.cycleCount = cycleCount;
	}
	
	
	public boolean isFirm() {
		return firm;
	}
	public void setFirm(boolean firm) {
		this.firm = firm;
	}
	
	
	
	
	public byte getLikeBLE() {
		return likeBLE;
	}
	public void setLikeBLE(byte likeBLE) {
		this.likeBLE = likeBLE;
	}	
	
	public static class CurrAdditionalInfo {
		public ArrayList<MiscInner.PairShort> bleList = new ArrayList<MiscInner.PairShort>();
		public ArrayList<Pair<Integer, ShovelSequence>> candidateShovels = new ArrayList<Pair<Integer, ShovelSequence>>();
		public int currentlyAssignedShovelId = Misc.getUndefInt();
	}
	 
}

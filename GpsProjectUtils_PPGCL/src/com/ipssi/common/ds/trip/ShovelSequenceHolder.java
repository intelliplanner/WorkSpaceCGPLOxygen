package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.ipssi.cache.ExcLoadEvent;
import com.ipssi.cache.NewExcLoadEventMgmt;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.GpsData;

public class ShovelSequenceHolder extends FastList<ShovelSequence>{
	
	protected static boolean g_doStartOnlyCompare = true;
	private static ConcurrentHashMap<Integer, ShovelSequenceHolder> g_shovelInfos = new ConcurrentHashMap<Integer, ShovelSequenceHolder>();
	private static volatile long g_currMaxTS =  0;
	private static volatile long g_currMinAccessed = 0;
	private static long g_maxGapMilliRelMax = (long)(10*60*60*1000);
	private static long g_maxGapMilliRelAccessed = (long)(10*60*60*1000);
	private static int g_avoidStaleIfSzChangeLessThan = 5;

	private int shovelId;
	private int szAftLastClean = 0;
	private int unfirmedszAftLastClean = 0;
	private FastList<UnfirmedBy> unfirmedBy = new FastList<UnfirmedBy>();
	private int rejectedszAftLastClean = 0;
	private FastList<UnfirmedBy> rejectedBy = new FastList<UnfirmedBy>();

	private int reinstatedOnceFullSz = 0;
	
	//START TANUJ tp sync
	public static boolean isLatestImmWorkingInWindow(int shovelId) {//TANUJ Use THIS
		ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(shovelId);
		return shovelInfo.isLatestImmWorkingInWindow();
	}
	public static int getExtraordinaryCleaningNowSecond(int shovelId) {//TANUJ Use THIS
		ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(shovelId);
		return shovelInfo.getExtraordinaryCleaningNowSecond();
	}
	public static final long IMMCHECK_WINDOW_MILLI = 60*1000;
	public static final long THRESH_BACK_DATA_MS = 3*60*1000;
	public static final long IMMCHECK_WINDOW_NOCYCLE = 5*60*1000;
	private boolean isWorkingImm = true;
	
	public synchronized boolean  isLatestImmWorkingInWindow() {
		return isWorkingImm;
	}

	public void updateLatestImmWorking(Connection conn, NewVehicleData shovelVDT, NewExcLoadEventMgmt loadEvents)  {
		GpsData last = shovelVDT.getLast(conn);
		ExcLoadEvent lastEvent = loadEvents.getLast(conn);
		long tsEndGps = last == null ? 0 : last.getGps_Record_Time();
		long tsEndEvent = lastEvent == null ? 0 : lastEvent.getGpsRecordTime();
		long tsEndAct = tsEndGps > tsEndEvent ? tsEndGps : tsEndEvent;
		long currTS = ThreadContextCache.getCurrTS();
		if (currTS-tsEndAct > THRESH_BACK_DATA_MS) {//latest info not available ... presume working
			this.isWorkingImm = true;
			return;
		}
		boolean eventSeemsProper = lastEvent != null && tsEndAct-tsEndEvent < ShovelSequence.MAX_GAP_BETWEEN_IDLING_PT_BEF_ASSUMING_ACTIVE;
		if (eventSeemsProper) {
			//event in last 10 min .. make sure that in last 1 minute we have two events
			boolean hasImmEvent=  (tsEndAct-tsEndEvent <= IMMCHECK_WINDOW_MILLI);
			if (hasImmEvent) {
				ExcLoadEvent lastTolast = loadEvents.get(conn, lastEvent, true);
				hasImmEvent = lastTolast != null && (tsEndAct - lastTolast.getGpsRecordTime()) <=IMMCHECK_WINDOW_MILLI;  
			}
			if (!hasImmEvent) {
				
				//check if there is a shovel standing in IMMCHECK_WINDOW - if there is then consider non-working
				CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(shovelId);
				if (currShovelInfo.getDumpers().size() > 0) {
					CurrDumperInfo currDumperInfo = currShovelInfo.getDumpers().get(0);
					long endTS = currDumperInfo.getSelectedShovelSeq().getEndTS();//looking at endData of dumper instead of latest time to handle backdata
					if (endTS - currShovelInfo.getDumpers().get(0).getStartTS() > IMMCHECK_WINDOW_MILLI)
						hasImmEvent = false;
				}
				else
					hasImmEvent = true;
			}
			this.isWorkingImm = hasImmEvent;
		}
		else {//window doesnt seem proper ... if we have a dumper standing for more than X minute then we will declare non-working
				CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(shovelId);
				CurrDumperInfo currDumperInfo = currShovelInfo.getDumpers().get(0);
				long endTS = currDumperInfo.getSelectedShovelSeq().getEndTS();//looking at endData of dumper instead of latest time to handle backdata
				if (endTS - currShovelInfo.getDumpers().get(0).getStartTS() > IMMCHECK_WINDOW_NOCYCLE)
					this.isWorkingImm = false;
				else
					this.isWorkingImm = true;
		}
	}

	private static int MUST_CLEAN_AFTER_TRIP = 4;
	private static int MUST_CLEAN_DUR_SEC = 120;
	
	private int extraOrdinaryCleaningNowSecond = 0;
	public synchronized int getExtraordinaryCleaningNowSecond() {
		return this.extraOrdinaryCleaningNowSecond;
	}
	public void estimateExtraordinaryCleaningNow(Connection conn) {
		estimateExtraordinaryCleaningNow(conn, MUST_CLEAN_AFTER_TRIP, MUST_CLEAN_DUR_SEC);
	}
	public void estimateExtraordinaryCleaningNow(Connection conn, int numTrips, int cleanSec) {
		ArrayList<ShovelSequence> recentTrips = new ArrayList<ShovelSequence>();
		//recent trips is desc order (most recent first)
		for (int i=this.size()-1;i>=0;i--) {
			ShovelSequence seq = this.get(i);
			
			int addAfter = recentTrips.size()-1;
			for (;addAfter>=0;addAfter--) {
				if (recentTrips.get(addAfter).getActStartTS() > seq.getActStartTS()) {
					break;
				}
			}
			if (addAfter == recentTrips.size() -1)
				recentTrips.add(seq);
			else
				recentTrips.add(addAfter+1, seq);
			if (recentTrips.size() >= MUST_CLEAN_AFTER_TRIP)
				break;
		}
		
		CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(this.shovelId);
		long tsStart = ThreadContextCache.getCurrTS();
		CurrDumperInfo currDumperInfo = currShovelInfo.getDumpers().size() > 0 ? currShovelInfo.getDumpers().get(0) : null;
		if (currDumperInfo != null && currDumperInfo.getNumCycles() > 0) {
			tsStart = currDumperInfo.getActStartTS();
		}
		this.extraOrdinaryCleaningNowSecond = 0;
		if (recentTrips.size() >= MUST_CLEAN_AFTER_TRIP) {
			int gapTot = 0;
			int cnt = 0;
			for (int i=0,is=recentTrips.size();i<is;i++) {
				byte avg = recentTrips.get(i).getAvgDur();
				if (avg > 30)
					avg = 30;
				int gap = (int)(((i == 0 ? tsStart : recentTrips.get(i-1).getActStartTS()) - recentTrips.get(i).getActEndTS())/1000) - avg;
				if (gap < 0)
					gap = 0;
				gapTot += gap;
			}
			if (MUST_CLEAN_DUR_SEC > gapTot)
			    this.extraOrdinaryCleaningNowSecond = MUST_CLEAN_DUR_SEC-gapTot;
			else
				this.extraOrdinaryCleaningNowSecond = 0;
		}
	}
	//TANUJ end sync
	
	public static class PtFromTrip implements Comparable<PtFromTrip>{
		public int lon;
		

		public int lat;
		public long ts;
		public boolean bleValid = false;
		public PtFromTrip(long ts) {
			this.ts = ts;
		}
		public PtFromTrip(int lon, int lat, long ts, boolean bleValid) {
			super();
			this.lon = lon;
			this.lat = lat;
			this.ts = ts;
			this.bleValid = bleValid;
		}
		public int compareTo(PtFromTrip arg0) {
			// TODO Auto-generated method stub
			long gap = this.ts - arg0.ts;
			return gap < 0 ? -1 : gap == 0 ? 0 : 1;
		}
	
	}
	private FastList<PtFromTrip> ptsFromTrip = new FastList<PtFromTrip>();
	private int ptsFromTripLastClean = 0;
	private FastList<UnfirmedBy> reinstatedOnceFull = new FastList<UnfirmedBy>();
	private FastList<Long> reinstatedGoodPrevData = new FastList<Long>();
	private int reinstatedGoodPrevDataSz = 0;
	
	private ReentrantLock lockForProcess = new ReentrantLock();
	private static int g_currPlanId = Misc.getUndefInt();
	private InvPileLookupHelper.InvLookupInfo lastInvLookup = null;
	private InvPileLookupHelper.InvLookupInfo currPlanInvLookupShovelPos = null;
	public InvPileLookupHelper.InvLookupInfo getLastInvLookup() {//synchronized externally
		return lastInvLookup;
	}
	public InvPileLookupHelper.InvLookupInfo setLastInvLookup(InvPileLookupHelper.InvLookupInfo lastInvLookup) {//synchrnoized externally
		return this.lastInvLookup = lastInvLookup;
	}
	public InvPileLookupHelper.InvLookupInfo getCurrPlanInvLookupShovelPos() {//synchronized externally
		return currPlanInvLookupShovelPos;
	}
	public InvPileLookupHelper.InvLookupInfo setCurrPlanInvLookupShovelPos(InvPileLookupHelper.InvLookupInfo lastInvLookup) {//synchrnoized externally
		return this.currPlanInvLookupShovelPos = lastInvLookup;
	}	
	
	public void lock() {
		try {
			lockForProcess.lock();
		}
		catch (Exception e) {
			//eat it
		}
	}
	public void unlock() {
		try {
			lockForProcess.unlock();
		}
		catch (Exception e) {
			
		}
	}
	private static final int G_MAX_MILLISEC_PRIOR_FOR_GPSPOS_TO_CONSIDER_VALID = 20*60*1000;
	private static final int G_MIN_Q_FORGPSPOS = 11;
	private static final int G_WT_ON_15_FORGPSPOS = 5;
	private static final int G_WT_ON_13_FORGPSPOS = 3;
	private static final int G_WT_ON_OTH_FORGPSPOS = 2;
	public static class DbgInOut implements Comparable<DbgInOut>  {
		private int idx = 0;
		private long ts = 0;
		private long gin = 0;
		private long gout = 0;
		public DbgInOut(int idx, long ts, long gin, long gout) {
			this.idx = idx;
			this.ts = ts;
			this.gin = gin;
			this.gout = gout;
		}
		public String toString() {
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(idx).append(",").append(sdf.format(new java.util.Date(ts))).append(",").append(sdf.format(new java.util.Date(gin))).append(",").append(sdf.format(new java.util.Date(gout)));
			return sb.toString();
		}
		public int compareTo(DbgInOut arg0) {
			// TODO Auto-generated method stub
			return this.gin - arg0.gin < 0 ? -1 : this.gin == arg0.gin ? 0 : 1;
		}
		public int compare(DbgInOut arg0, DbgInOut arg1) {
			// TODO Auto-generated method stub
			return arg0.compareTo(arg1);
		}
	}
	public boolean dbgCheckSanity() {
		
		DbgInOut theList[] = new DbgInOut[size()];
		for (int i=0,is=size();i<is;i++) {
			ShovelSequence entry = get(i);
			theList[i] = (new DbgInOut(i, entry.getStartTS(), entry.getActStartTS(), entry.getActEndTS()));
		}
		Arrays.sort(theList);
		long prevGout = 0;
		for (int i=0,is=size();i<is;i++) {
			DbgInOut item = theList[i];
			if (prevGout > 0 && item.gin <= prevGout) {
				int dbg = 1;
				dbg++;
				return false;
			}
			prevGout = item.gout;
		}
		return true;
	}
	public static final int BLECNT_THRESH = 6;
	public static final int ALLCNT_THRESH = 10;
	public void addPtsFromTrip(double lon, double lat, long ts, boolean bleVal) {
		PtFromTrip pt = new PtFromTrip((int)(lon*100000),(int)(lat*100000),ts,bleVal);
		Pair<Integer, Boolean> idx = ptsFromTrip.indexOf(pt);
		if (idx.second) {
			ptsFromTrip.replaceAt(idx.first, pt);
		}
		else {
			ptsFromTrip.add(pt);
		}
	}
	public ArrayList<ShovelSequence> getRedoListBecauseEnoughData(long ts) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		
		ShovelSequence ref = new ShovelSequence(ts+1000);
		ArrayList<ShovelSequence> retval = null;
		for (int i=0;;i--) {
			ShovelSequence pt = this.get(ref, i);
			if (pt == null)
				break;
			if (pt.isPrevPtFromBLE() || ts-pt.getEndTS() < G_MAX_MILLISEC_PRIOR_FOR_GPSPOS_TO_CONSIDER_VALID)
				break;
			if (this.wasReinstatedGoodPrevData(pt))
				continue;
			
			if (retval == null) {
				retval = new ArrayList<ShovelSequence>();
			}
			retval.add(pt);
		}
		for (int i=1;;i++) {
			ShovelSequence pt = this.get(ref, i);
			if (pt == null)
				break;
			if (pt.isPrevPtFromBLE() || pt.getEndTS()-ts > G_MAX_MILLISEC_PRIOR_FOR_GPSPOS_TO_CONSIDER_VALID)
				break;
			if (retval == null) {
				retval = new ArrayList<ShovelSequence>();
			}
			retval.add(pt);
		}
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	public Pair<Point, Point> getLonLatFromPosNew(long ts) {//first is of BLE, second is of noBLE
		PtFromTrip  ref = new PtFromTrip(ts);
		double blelon = 0;
		double blelat = 0;
		double allLon = 0;
		double allLat = 0;
		int blecnt = 0;
		int allcnt = 0;
		boolean canGoBack = true;
		boolean canGoForw = true;
		for(int i=0;;i++) {
			for (int art=0;art<(i == 0 ? 1 : 2);art++) {
				if ((art == 0 && !canGoBack) || (art == 1 && !canGoForw))
					continue;
				PtFromTrip seq = this.ptsFromTrip.get(ref,(art == 0 ? -1:1)*i);
				if (seq == null || (art == 0 && (seq.ts < ts-G_MAX_MILLISEC_PRIOR_FOR_GPSPOS_TO_CONSIDER_VALID)) || (art == 1 && (seq.ts > ts+G_MAX_MILLISEC_PRIOR_FOR_GPSPOS_TO_CONSIDER_VALID))) {
					if (art == 0)
						canGoBack = false;
					else
						canGoForw = false;
					continue;
				}
				
				boolean isBLEQ = seq.bleValid;
				int lonInt = seq.lon;
				if (Misc.isUndef(lonInt) || lonInt == 0) //if sequence from dataLoss then set to Undef
					continue;
				double lo = (double)lonInt/100000.0;
				double la = (double) seq.lat/100000.0;
				if (isBLEQ) {
					blelon += lo;
					blelat += la;
					blecnt += 1;
				}
				allLon += lo;
				allLat += la;
				allcnt += 1;
				if (blecnt >= BLECNT_THRESH || allcnt >= ALLCNT_THRESH) {
					canGoForw = false;
					canGoBack = false;
					break;
				}
			}
			if (!canGoBack && !canGoForw)
				break;
		}
		
		if (blecnt >= BLECNT_THRESH) {
			blelon = blelon/blecnt;
			blelat = blelat/blecnt;
		}
		else {
			blelon = 0;
			blelat = 0;
		}
		if (allcnt >= BLECNT_THRESH) {
			allLon = allLon/allcnt;
			allLat = allLat/allcnt;
		}
		else {
			allLon = 0;
			allLat = 0;
		}
		return new Pair<Point, Point>(new Point(blelon,blelat), new Point(allLon,allLat)); 
	}
	
	
	private ArrayList<MiscInner.TripleIntLongLong> helperGetUnfirmingsCausedBy(ShovelSequence unfirmedByDumperSeq, FastList<UnfirmedBy> theList) {
		ArrayList<MiscInner.TripleIntLongLong> retval = null;
		long st = unfirmedByDumperSeq.getStartTS();
		long en = unfirmedByDumperSeq.getEndTS();
		UnfirmedBy dummy = new UnfirmedBy(st-g_maxToLookBackForOverlapping);
		int idx =  theList.indexOf(dummy).first;
		
		
		for (int i=idx,is=theList.size();idx<is;idx++) {
			UnfirmedBy entry = theList.get(idx);
			if (i == idx && entry == null)
				continue;
			if (entry == null)
				break;
			if (entry.unfirmedSeqStartTS > en)
				break;
			if (entry.unfirmedByDumperId == unfirmedByDumperSeq.getDumperId() && (!ShovelSequenceHolder.g_doStartOnlyCompare || st == entry.unfirmedBySeqStartTS) 
				&&	(entry.unfirmedSeqEndTS >= st && entry.unfirmedSeqStartTS < en)) {
				if (retval == null)
					retval = new ArrayList<MiscInner.TripleIntLongLong>();
				retval.add(new MiscInner.TripleIntLongLong(entry.unfirmedDumperId, entry.unfirmedSeqStartTS,entry.unfirmedSeqEndTS));
			}
		}
		return retval;
	}
	
	private int getUnfirmedCount(ShovelSequence meSeq, ShovelSequence other, FastList<UnfirmedBy> theList, boolean checkAtAct) {
		//UnfirmedBy(long unfirmedSeqStartTS, int unfirmedDumperId, long unfirmedBySeqStartTS,  int unfirmedByDumperId, long unfirmedSeqEndTS, long unfirmedBySeqEndTS)
	//copy-past- hack from wasUnfirmed - so swapping meSeq and other
	
		ShovelSequence temp = meSeq;
		meSeq=  other;
		other = temp;
		if (other == null)
			other = new ShovelSequence(-1);
		Pair<Integer, Boolean> idx = null;
		if (checkAtAct)
			idx =  theList.indexOf(new UnfirmedBy(meSeq.getActStartTS(), meSeq.getDumperId(),other.getActStartTS(),other.getDumperId(),meSeq.getActEndTS(),other.getActEndTS(), meSeq.getActStartTS(), meSeq.getActEndTS()));
		else
			idx =  theList.indexOf(new UnfirmedBy(meSeq.getStartTS(), meSeq.getDumperId(),other.getStartTS(),other.getDumperId(),meSeq.getEndTS(),other.getEndTS(), meSeq.getActStartTS(), meSeq.getActEndTS()));
		if (idx != null && idx.second) {
			UnfirmedBy entry = theList.get(idx.first);
			if (entry != null)
				return entry.count;
		}
		return 0;
	}

	private MiscInner.PairIntBool hadUnfirmedMultipleTimes(ShovelSequence meSeq, ShovelSequence other, FastList<UnfirmedBy> theList, boolean checkAtAct, int cntThresh) {
			//UnfirmedBy(long unfirmedSeqStartTS, int unfirmedDumperId, long unfirmedBySeqStartTS,  int unfirmedByDumperId, long unfirmedSeqEndTS, long unfirmedBySeqEndTS)
		//copy-past- hack from wasUnfirmed - so swapping meSeq and other
		int cnt = getUnfirmedCount(meSeq, other,  theList,  checkAtAct);
		return new MiscInner.PairIntBool(cnt, cnt>cntThresh);
		
 	}
	
	private boolean helperWasUnfirmed(ShovelSequence meSeq, ShovelSequence other, FastList<UnfirmedBy> theList, boolean checkAtAct) {
		//if (ShovelSequenceHolder.g_doStartOnlyCompare) {
			//UnfirmedBy(long unfirmedSeqStartTS, int unfirmedDumperId, long unfirmedBySeqStartTS,  int unfirmedByDumperId, long unfirmedSeqEndTS, long unfirmedBySeqEndTS)
			Pair<Integer,Boolean> idx = null;
			if (checkAtAct)
				idx = theList.indexOf(new UnfirmedBy(meSeq.getActStartTS(), meSeq.getDumperId(),other.getActStartTS(),other.getDumperId(),meSeq.getActEndTS(),other.getActEndTS(), meSeq.getActStartTS(), meSeq.getActEndTS()));
			else
				idx =  theList.indexOf(new UnfirmedBy(meSeq.getStartTS(), meSeq.getDumperId(),other.getStartTS(),other.getDumperId(),meSeq.getEndTS(),other.getEndTS(), meSeq.getActStartTS(), meSeq.getActEndTS()));
			boolean retval = idx.second;
			if (retval) {
				UnfirmedBy entry = theList.get(idx.first);
				retval = entry.unfirmedByEnded == meSeq.isStopEnded() && entry.unfirmedEnded == other.isStopEnded();
			}
			return retval;
		//}
		
	}
	public boolean wasUnfirmed(ShovelSequence meSeq, ShovelSequence other) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		boolean retval =  helperWasUnfirmed(meSeq, other, this.unfirmedBy, false);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	
	private boolean wasReinstatedGoodPrevData(ShovelSequence seq) {
		return reinstatedGoodPrevData.get(new Long(seq.getStartTS())) != null;
	}
	public void addReinstatedGoodPrevData(long ts) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		reinstatedGoodPrevData.add(new Long(ts));
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
	}
	public static final int MAX_UNFIRMING = 6;
	public boolean hadUnfirmedMultipleTimes(ShovelSequence meSeq, ShovelSequence other) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		MiscInner.PairIntBool retval = hadUnfirmedMultipleTimes(meSeq, other, this.unfirmedBy, false,MAX_UNFIRMING);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval.second;
	}
	public MiscInner.PairIntBool lastRedoOfUnfirmed(ShovelSequence meSeq) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		MiscInner.PairIntBool retval = hadUnfirmedMultipleTimes(null, meSeq, this.unfirmedBy, false,MAX_UNFIRMING-1);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	
	public boolean wasRejected(ShovelSequence meSeq, ShovelSequence other) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		boolean retval = helperWasUnfirmed(meSeq, other, this.rejectedBy,false);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	public boolean wasReinstatedOnceFull(ShovelSequence meSeq, ShovelSequence other) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		boolean retval = helperWasUnfirmed(meSeq, other, this.reinstatedOnceFull,false);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	
	public ArrayList<MiscInner.TripleIntLongLong> getRejectedList(ShovelSequence seq) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		ArrayList<MiscInner.TripleIntLongLong> retval = helperGetUnfirmingsCausedBy(seq, this.rejectedBy);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
		return retval;
	}
	private void helperAddUnfirmed(ShovelSequence beingUnfirmed, ShovelSequence unfirming, FastList<UnfirmedBy> theList, boolean checkAtAct) {
		//UnfirmedBy(long unfirmedSeqStartTS, int unfirmedDumperId, long unfirmedBySeqStartTS,  int unfirmedByDumperId, long unfirmedSeqEndTS, long unfirmedBySeqEndTS) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		if (unfirming == null)
			unfirming = new ShovelSequence(-1);
		UnfirmedBy unfirmed = checkAtAct ?new UnfirmedBy(beingUnfirmed.getActStartTS(), beingUnfirmed.getDumperId(), unfirming.getActStartTS(), unfirming.getDumperId(), beingUnfirmed.getActEndTS(), unfirming.getActEndTS(), beingUnfirmed.getActStartTS(), beingUnfirmed.getActEndTS()) 
				: new UnfirmedBy(beingUnfirmed.getStartTS(), beingUnfirmed.getDumperId(), unfirming.getStartTS(), unfirming.getDumperId(), beingUnfirmed.getEndTS(), unfirming.getEndTS(), beingUnfirmed.getActStartTS(), beingUnfirmed.getActEndTS());
		Pair<Integer, Boolean> existingEntry = theList.indexOf(unfirmed);
		if (existingEntry.second) {
			UnfirmedBy entry = theList.get(existingEntry.first);
			if (entry.unfirmedByEnded != unfirming.isStopEnded() || entry.unfirmedEnded != beingUnfirmed.isStopEnded()) {
				entry.count = 0;
				entry.unfirmedByEnded = unfirming.isStopEnded();
				entry.unfirmedEnded = beingUnfirmed.isStopEnded();
			}
			entry.count++;
			theList.get(existingEntry.first).count++;
			
		}
		else {
			unfirmed.count = 0;
			unfirmed.unfirmedByEnded = unfirming.isStopEnded();
			unfirmed.unfirmedEnded = beingUnfirmed.isStopEnded();
			unfirmed.count++;
			theList.add(unfirmed);
		}
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.unfirmCheck(System.nanoTime()-timingMarker1);
	}
	public void addRejected(ShovelSequence beingRejected, ShovelSequence causingRejection) {
		helperAddUnfirmed(beingRejected, causingRejection, this.rejectedBy,false);
	}
	public void addReinstatedOnce(ShovelSequence beingRejected, ShovelSequence causingRejection) {
		helperAddUnfirmed(beingRejected, causingRejection, this.reinstatedOnceFull,false);
	}
	public void addUnfirmed(ShovelSequence beingUnfirmed, ShovelSequence unfirming) {
		//if (beingUnfirmed.isStopEnded() && unfirming.isStopEnded()) {
			helperAddUnfirmed(beingUnfirmed, unfirming, this.unfirmedBy,false);
		//}
	}
	public static void setLatestInserted(LUSequence seq) {
		long mxts = seq.getEndTimeSimple();
		if (g_currMaxTS < mxts)
			g_currMaxTS = mxts;
	}
	
	public boolean matchingDumperSeq(ShovelSequence seq) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		Pair<Integer, Boolean> idx= this.indexOf(seq);
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.shovelSequenceGet(System.nanoTime()-timingMarker1);
		return idx.second;
	}
	public ShovelSequence removeExact(ShovelSequence seq) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		Pair<Integer, Boolean> idx= this.indexOf(seq);
		ShovelSequence retval  = null;
		if (idx.second && idx.first >= 0) {
			retval = this.get(idx.first);
			this.remove(idx.first);
		}
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.shovelSequenceGet(System.nanoTime()-timingMarker1);
		return retval;
	}
	
	public static void removeSeqForDumperId(Connection conn,  boolean removeNonFirmOnly,  int dumperId, long mustCleanupShovelsTill, ArrayList<Long> cleanupShovelsList, ThreadContextCache threadContextCache, List<OpStationBean> opslist, boolean takeLock) throws Exception  {
		if (mustCleanupShovelsTill <= 0 && cleanupShovelsList.size() == 0)
			return;
		long tsStart = cleanupShovelsList.size() > 0 ? cleanupShovelsList.get(0) : mustCleanupShovelsTill;
		if (tsStart <= 0)
			return;
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		for (OpStationBean op : opslist) {
			if (op == null)
				continue;
			
			ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(op.getLinkedVehicleId());
			try {
				if (takeLock)
					threadContextCache.acquireLock(shovelInfo); //will be released later in VehicleLUSequences
				
				int posInCleanupShovelsList = cleanupShovelsList.size()-1;
				for (int i=shovelInfo.size()-1; i>=0; i--) {
					ShovelSequence seq = shovelInfo.get(i);
					if (seq.getEndTS() < tsStart)
						break;
					if (seq.getDumperId() == dumperId) {// && ((removeNonFirmOnly && seq.isFirm()) || !seq.isFirm())) {
						long seqStart = seq.getStartTS();
						boolean toremove = seqStart >= mustCleanupShovelsTill && mustCleanupShovelsTill > 0;
						if (!toremove) {
							for (;posInCleanupShovelsList >= 0 ;posInCleanupShovelsList--) {
								long ts = cleanupShovelsList.get(posInCleanupShovelsList);
								if (ts == seqStart)
									toremove = true;
								if (ts < seqStart)
									break;
							}
						}
						if (toremove)
							shovelInfo.remove(i);
					}
				}
			}
			catch (Exception e) {
				
			}
			finally {
				if (takeLock)
					threadContextCache.releaseLock(shovelInfo);
			}
		}//for each shovel
		if (ThreadContextCache.g_shovelStat != null)
			ThreadContextCache.g_shovelStat.shovelSequenceGet(System.nanoTime()-timingMarker1);
	}
	public static final long g_maxToLookBackForOverlapping = 30*60*1000;
	private static boolean isUnfirmed(ShovelSequence entry, ArrayList<ShovelSequence> alreadyUnfirmed) {
		boolean isUnfirmed = false;
		for (int j=0,js=alreadyUnfirmed == null ? 0 : alreadyUnfirmed.size();j<js;j++) {
			ShovelSequence unfirmed = alreadyUnfirmed.get(j);
			if (unfirmed.getDumperId() == entry.getDumperId() && unfirmed.getStartTS() == entry.getStartTS()) {
				isUnfirmed = true;
				break;
			}
		}
		return isUnfirmed;
	}
	private ShovelSequence getPriorFirmed(int meIndex, ArrayList<ShovelSequence> alreadyUnfirmed) {
		for (int i=meIndex-1;i>=0;i--) {
			ShovelSequence entry = this.get(i);
			if (entry == null)
				break;
			boolean isUnfirmed = isUnfirmed(entry, alreadyUnfirmed);
			if (!isUnfirmed)
				return entry;
		}
		return null;
	}
	private ShovelSequence getNextFirmed(int meIndex, ArrayList<ShovelSequence> alreadyUnfirmed) {
		for (int i=meIndex+1,is = this.size();i<is;i++) {
			ShovelSequence entry = this.get(i);
			if (entry == null)
				break;
			boolean isUnfirmed = isUnfirmed(entry,alreadyUnfirmed);
			if (!isUnfirmed)
				return entry;
		}
		return null;
	}
	private static MiscInner.PairLong getRhsOverlapChangedTimed(ShovelSequence entry, ArrayList<ShovelSequence.OverlapTimingChangeInfo> overlapTimingChanges) {
		ShovelSequence.OverlapTimingChangeInfo changesToEntry = null;
		for (int i1=0,i1s=overlapTimingChanges == null ? 0 : overlapTimingChanges.size(); i1<i1s;i1++) {
			ShovelSequence.OverlapTimingChangeInfo temp = overlapTimingChanges.get(i1);
			if (temp.other.getDumperId() == entry.getDumperId() && temp.other.getStartTS() == entry.getStartTS()) {
				changesToEntry = temp;
				break;
			}
		}
		long st = entry.getActStartTS();
		long en = entry.getActEndTS();
		if (changesToEntry != null && !Misc.isUndef(changesToEntry.rhsActStartSec)) {
			st = entry.getStartTS() + changesToEntry.rhsActStartSec*1000;
			en = entry.getStartTS() + changesToEntry.rhsActEndSec*1000;
		}
		return new MiscInner.PairLong(st, en);
	}
	public MiscInner.TripleIntLongLong getLeftAndRightActEnd(ShovelSequence meSeq, byte exclQLesser, ArrayList<ShovelSequence.OverlapTimingChangeInfo> overlapTimingChanges, ArrayList<ShovelSequence> alreadyUnfirmed) {
		//first is gap relative to left
		//1st = 0 => same as base,
		if (!ShovelSequence.SHOVELSEQ_BY_ACT) {
			return getLeftAndRightActEndWithStartTS(meSeq,exclQLesser, overlapTimingChanges, alreadyUnfirmed);
		}
		int toLeftGap = Misc.getUndefInt();
		int meDumperId = meSeq.getDumperId();
		long baseStart = meSeq.getBaseActStartTS();
		long baseEnd = meSeq.getBaseActEndTS();
		long leftHardBound = baseStart;
		long rightHardBound = baseEnd;
		leftHardBound = meSeq.getStartTS()-ShovelSequence.MAX_LOADEVENT_PRIOR_STOP*1000;
		rightHardBound = meSeq.getEndTS();
		boolean incrLeft = false;
		boolean decrRight = false;
		long meActStartTS = meSeq.getActStartTS();
		ShovelSequence dummyCheckFrom = new ShovelSequence(meActStartTS);
		
		Pair<Integer, Boolean> idx = this.indexOf(dummyCheckFrom);
		int index = idx.first;
		ShovelSequence prior = this.getPriorFirmed(index, alreadyUnfirmed);//could cause error if the rhs was completely changed ..
		if (prior != null) {
			MiscInner.PairLong temp = getRhsOverlapChangedTimed(prior, overlapTimingChanges);
			long pend = temp.second;
			if (pend >= leftHardBound) {
				leftHardBound = pend+1000;
				toLeftGap = (int)((meActStartTS - pend)/1000);
			}
		}
		ShovelSequence next = this.getNextFirmed(index, alreadyUnfirmed);//could cause error if the rhs was completely changed ..
		if (next != null) {
			MiscInner.PairLong temp = getRhsOverlapChangedTimed(next, overlapTimingChanges);
			long pstart = temp.second;
			if (pstart <= rightHardBound) {
				rightHardBound = pstart-1000;
			}
		}
		//now incase the other was completely changed .. we will also look at overlap list directly
		for (int i1=0,i1s=overlapTimingChanges == null ? 0 : overlapTimingChanges.size(); i1<i1s;i1++) {
			ShovelSequence.OverlapTimingChangeInfo temp = overlapTimingChanges.get(i1);
			if (!Misc.isUndef(temp.rhsActStartSec)) {
				long pstart = temp.other.getStartTS()+1000*temp.rhsActStartSec;
				long pend = temp.other.getStartTS()+1000*temp.rhsActEndSec;
				if (pend >= leftHardBound) {
					leftHardBound = pend+1000;
					toLeftGap = (int)((meActStartTS - pend)/1000);
				}
				if (pstart <= rightHardBound) {
					rightHardBound = pstart-1000;
				}
			}
		}
		if (Misc.isUndef(toLeftGap))
			toLeftGap = Integer.MAX_VALUE;
		return new MiscInner.TripleIntLongLong(toLeftGap, leftHardBound,rightHardBound);
	}	

	public long getLeftEndTSBefore(long tsStart) {
		long maxLeft = 0;
		
		ShovelSequence dummyCheckFrom = new ShovelSequence(tsStart-g_maxToLookBackForOverlapping);
		Pair<Integer, Boolean> idx = this.indexOf(dummyCheckFrom);
		int indexStart = idx.first;
		
		
		for (int i=indexStart,is=size();i<is;i++) {
			ShovelSequence entry = this.get(i);
			if (entry == null && i == indexStart)
				continue;
			if (entry == null || entry.getStartTS() > tsStart)
				break;
			long entryEnd = entry.getActEndTS();
			if (entryEnd < tsStart && entryEnd > maxLeft)
				maxLeft = entryEnd;
		}
		return maxLeft;
	}
	
	public MiscInner.TripleIntLongLong getLeftAndRightActEndWithStartTS(ShovelSequence meSeq, byte exclQLesser, ArrayList<ShovelSequence.OverlapTimingChangeInfo> overlapTimingChanges, ArrayList<ShovelSequence> alreadyUnfirmed) {
		//first is gap relative to left
		//1st = 0 => same as base,

		int toLeftGap = Misc.getUndefInt();
		int meDumperId = meSeq.getDumperId();
		long baseStart = meSeq.getBaseActStartTS();
		long baseEnd = meSeq.getBaseActEndTS();
		long leftHardBound = baseStart;
		long rightHardBound = baseEnd;
		if (true) {//20190201 was false .. set it to maxRange ... does not work for tempRHS thingy in ShovelSequence ... ble not incorporated in regetting time
			leftHardBound = meSeq.getStartTS()-ShovelSequence.MAX_LOADEVENT_PRIOR_STOP*1000-1000;
			rightHardBound = meSeq.getEndTS();
		}
		boolean incrLeft = false;
		boolean decrRight = false;
		ShovelSequence dummyCheckFrom = new ShovelSequence(meSeq.getStartTS()-g_maxToLookBackForOverlapping);
		Pair<Integer, Boolean> idx = this.indexOf(dummyCheckFrom);
		long meStart = meSeq.getStartTS();
		long meEnd = meSeq.getEndTS();
		long meCurrStartTS = meSeq.getActStartTS();;
		long meCurrEndTS = meSeq.getActEndTS();
		int indexStart = idx.first;
		
		
		for (int i=indexStart,is=size();i<is;i++) {
			ShovelSequence entry = this.get(i);
			if (entry == null && i == indexStart)
				continue;
			if (entry == null || entry.getStartTS() > meEnd)
				break;
			
			if (entry.getDumperId() == meDumperId && entry.getStartTS() == meStart)
				continue;
			boolean isUnfirmed = false;
			for (int t1=0,t1s=alreadyUnfirmed == null ? 0 : alreadyUnfirmed.size(); t1<t1s; t1++) {
				ShovelSequence temp = alreadyUnfirmed.get(t1);
				if (temp.getDumperId() == entry.getDumperId() && temp.getStartTS() == entry.getStartTS()) {
					isUnfirmed = true;
					break;
				}
			}
			if (isUnfirmed)
				continue;
			if (entry.getActEndTS() < meCurrStartTS) {
				int sec = (int)((meCurrStartTS - entry.getActEndTS())/1000);
				if (Misc.isUndef(toLeftGap) || toLeftGap > sec)
					toLeftGap = sec;
			}
			if (entry.getDumperBestQuality() >= exclQLesser) {
				long entryActStart = -1;
				long entryActEnd = -1;
				ShovelSequence.OverlapTimingChangeInfo changesToEntry = null;
				for (int i1=0,i1s=overlapTimingChanges == null ? 0 : overlapTimingChanges.size(); i1<i1s;i1++) {
					ShovelSequence.OverlapTimingChangeInfo temp = overlapTimingChanges.get(i1);
					if (temp.other.getDumperId() == entry.getDumperId() && temp.other.getStartTS() == entry.getStartTS()) {
						changesToEntry = temp;
						break;
					}
				}
				if (changesToEntry == null) {
					entryActStart = entry.getActStartTS();
					entryActEnd = entry.getActEndTS();
				}
				else {
					entryActStart = entry.getStartTS()+changesToEntry.rhsActStartSec*1000;
					entryActEnd = entry.getStartTS()+changesToEntry.rhsActEndSec*1000;
				}
				if (entryActStart < meCurrStartTS) {
					if (entryActEnd >= leftHardBound) {
						leftHardBound = entryActEnd;
						incrLeft = true;
					}
				}
				else if (entryActStart > meCurrStartTS) {
					if (entryActStart <= rightHardBound) {
						rightHardBound = entryActStart;
						decrRight = true;
					}
				}
			}
		}
		if (incrLeft)
			leftHardBound += 1000;
		if (decrRight)
			rightHardBound -= 1000;
		if (Misc.isUndef(toLeftGap))
			toLeftGap = Integer.MAX_VALUE;
		return new MiscInner.TripleIntLongLong(toLeftGap, leftHardBound,rightHardBound);
	}	
	public MiscInner.Triple getOverlappingOther(int dumperId, long tsStartAct, long tsEndAct, long tsActStart, long leftActOverlapMS, long rightActOverlapMS, ArrayList<MiscInner.PairIntLong> sortedIndices) {
		//first/second are bounds of overlap, third is gap relative to latest on left
		if (!ShovelSequence.SHOVELSEQ_BY_ACT) {
			return getOverlappingOtherWithStartTS(dumperId, tsStartAct, tsEndAct, tsActStart, leftActOverlapMS, rightActOverlapMS, sortedIndices);
		}
		int first = -1;
		int second = -1;
		int third = Misc.getUndefInt();
		int idx = this.indexOf(new ShovelSequence(tsStartAct)).first;
		for (int i=idx;i>=0;i--) {
			ShovelSequence seq = this.get(i);
			if (seq == null)
				break;
			long seqEndTS = seq.getActEndTS();
			if (seqEndTS < tsStartAct) {
				int sec = (int)((tsStartAct-seqEndTS)/1000);
				if (Misc.isUndef(third) || (sec < third))
					third = sec;
			}
			if (seqEndTS < leftActOverlapMS && (tsEndAct-seqEndTS) >= ShovelSequence.END_TO_END_GAP_MILLI)
				break;
			
			if (first < 0 || i < first) {
				first = i;
			}
		}
		for (int i=idx,is=this.size();i<is;i++) {
			ShovelSequence seq = this.get(i);
			if (seq == null)
				continue;
			long seqStartTS = seq.getActStartTS();
			if (seqStartTS > rightActOverlapMS && (seq.getActEndTS()-tsEndAct) >= ShovelSequence.END_TO_END_GAP_MILLI)
				break;
			
			if (second < 0 || i > second) {
				second = i;
			}
		}
		if (Misc.isUndef(third))
			third = Integer.MAX_VALUE;
		if (sortedIndices != null) {
			sortedIndices.clear();
			populateSortedListHelper(first, second, tsStartAct, tsEndAct, sortedIndices, leftActOverlapMS, rightActOverlapMS) ;
		}
		return new MiscInner.Triple(first,second, third);
	}
	public MiscInner.Triple getOverlappingOtherWithStartTS(int dumperId, long tsStart, long tsEnd, long tsActStart, long leftActOverlapMS, long rightActOverlapMS, ArrayList<MiscInner.PairIntLong> sortedIndices) {
		//first/second are bounds of overlap, third is gap relative to latest on left
		int first = -1;
		int second = -1;
		int third = Misc.getUndefInt();
		int idx = this.indexOf(new ShovelSequence(tsStart-g_maxToLookBackForOverlapping)).first;
		for (int i=idx,is=this.size();i<is;i++) {
			ShovelSequence entry = this.get(i);
			if (entry == null && i == idx)
				continue;
			if (entry == null)
				break;
			if (entry.strictNoLoad())
				continue;
			long adjSt = entry.getStartTSActAdj();
			long adjEn = entry.getEndTSActAdj();
			if (adjSt > tsEnd)
				break;
			if (entry.getDumperId() == dumperId && tsStart == entry.getStartTS())
				continue;
			boolean overlapping = !(adjEn < tsStart || adjSt > tsEnd) || (tsEnd < adjEn && (adjEn-tsEnd) < ShovelSequence.END_TO_END_GAP_MILLI) || (adjEn < tsEnd && (tsEnd-adjEn) < ShovelSequence.END_TO_END_GAP_MILLI);
			if (!overlapping) {
				//check if overlapping because of buffer requirements
				overlapping = !(adjEn < leftActOverlapMS || adjSt > rightActOverlapMS);
			}
			if (overlapping) {
				second = i;
				if (first < 0)
					first = i;
			}
			if (adjEn < tsActStart) {
				int sec = (int)((tsActStart-adjEn)/1000);
				if (Misc.isUndef(third) || third > sec)
					third = sec;
			}
		}
		if (Misc.isUndef(third))
			third = Integer.MAX_VALUE;
		if (sortedIndices != null) {
			sortedIndices.clear();
			populateSortedListHelper(first, second, tsStart, tsEnd, sortedIndices, leftActOverlapMS, rightActOverlapMS) ;
			
		}
		return new MiscInner.Triple(first,second, third);
	}
	
	public ShovelSequence getLatestOverlapping(int dumperId, long tsStart) {
		//second = ActEndBefore Latest Other
		
		ShovelSequence retval = null;
		long en = ThreadContextCache.getCurrTS();
		int idx = this.indexOf(new ShovelSequence(en)).first;
		for (int i=idx;i>=0;i--) {
			ShovelSequence entry = this.get(i);
			if (entry == null)
				break;
			if (entry.strictNoLoad())
				continue;
			if (entry.getEndTS() < tsStart)
				break;
			if (entry.getDumperId() == dumperId && tsStart == entry.getStartTS())
				continue;
			long adjEn = entry.getEndTSActAdj();
			
			boolean overlapping = !(adjEn < tsStart) ;
			
			if (overlapping) {
				if (retval == null || retval.getActEndTS() < entry.getActEndTS())
					retval = entry;
			}
		}
		return retval;
	}
	
	public MiscInner.Triple getOverlappingOtherWithStartTSNew(ShovelSequence meMaybeNotAdded, ArrayList<MiscInner.PairIntLong> sortedIndices) {
		//first/second are bounds of overlap, third is gap relative to latest on left
		int first = -1;
		int second = -1;
		int third = Misc.getUndefInt();
		long tsStart = meMaybeNotAdded.getStartTS();
		long tsActStart = meMaybeNotAdded.getActStartTS();
		long tsEnd = meMaybeNotAdded.getEndTS();
		long tsActEnd = meMaybeNotAdded.getActEndTS();
		int idx = this.indexOf(new ShovelSequence(tsStart-g_maxToLookBackForOverlapping)).first;
		int dumperId = meMaybeNotAdded.getDumperId();
		for (int i=idx,is=this.size();i<is;i++) {
			ShovelSequence entry = this.get(i);
			if (entry == null && i == idx)
				continue;
			if (entry == null)
				break;
			if (entry.getStartTS() > tsEnd)
				break;
			if (entry.getDumperId() == dumperId && tsStart == entry.getStartTS())
				continue;
			long entryEnd = entry.getActEndTS();
			long entryStart = entry.getActStartTS();
			boolean overlapping =!(entryEnd < tsActStart-ShovelSequence.END_TO_END_GAP_MILLI || entryStart > tsActEnd+ShovelSequence.END_TO_END_GAP_MILLI);
			if (!overlapping) {
				//check if overlapping because of buffer requirements
				overlapping = entryEnd < tsActEnd ? (tsActEnd-entryEnd) < ShovelSequence.END_TO_END_GAP_MILLI
						: (entryEnd-tsActEnd) < ShovelSequence.END_TO_END_GAP_MILLI
						;
			}
			if (overlapping) {
				second = i;
				if (first < 0)
					first = i;
			}
			if (entryEnd < tsActStart) {
				int sec = (int)((tsActStart-entryEnd)/1000);
				if (Misc.isUndef(third) || third > sec)
					third = sec;
			}
		}
		if (Misc.isUndef(third))
			third = Integer.MAX_VALUE;
		if (sortedIndices != null) {
			sortedIndices.clear();
			populateSortedListHelper(first, second, tsActStart, tsActEnd, sortedIndices, tsActStart-ShovelSequence.END_TO_END_GAP_MILLI, tsActEnd+ShovelSequence.END_TO_END_GAP_MILLI) ;
			
		}
		return new MiscInner.Triple(first,second, third);
	}
	public void populateSortedListHelper(int first, int second, long tsStartAct, long tsEndAct, ArrayList<MiscInner.PairIntLong> sortedIndices, long leftActOverlapMS, long rightActOverlapMS) {
		for (int i=first;i<=second;i++) {
			ShovelSequence other = this.get(i);
			if (other == null)
				continue;
			long otherActStart = other.getActStartTS();//other.getStartTSActAdj();
			long otherActEnd = other.getActEndTS();//other.getEndTSActAdj();
			long overlapMilli = -10000;
			
			if (tsEndAct  >= otherActStart) {
				long gap = tsEndAct-otherActStart;
				if (gap > overlapMilli)
					overlapMilli = gap;
			}
			if (tsStartAct  <= otherActEnd) {
				long gap = otherActEnd-tsStartAct;
				if (gap > overlapMilli)
					overlapMilli = gap;
			}
			if (overlapMilli < 0) {
				if (leftActOverlapMS <= otherActEnd)
					overlapMilli = -2500;
				else if (rightActOverlapMS >= otherActStart)
					overlapMilli = -5000;
			}
			sortedIndices.add(new MiscInner.PairIntLong(i, overlapMilli));
		}
		OverlapSortHelper sorter = new OverlapSortHelper(this);
		Collections.sort(sortedIndices, sorter);
		
	}
	public static class OverlapSortHelper implements Comparator<MiscInner.PairIntLong> {
		ShovelSequenceHolder me = null;
		public OverlapSortHelper(ShovelSequenceHolder me) {
			this.me = me;
		}
		
		public int compare(MiscInner.PairIntLong arg0, MiscInner.PairIntLong arg1) {
			ShovelSequence lhs = me.get(arg0.first);
			ShovelSequence rhs = me.get(arg1.first);
			int retval = lhs.getDumperBestQuality()-rhs.getDumperBestQuality();
			if (retval == 0) {
				long overlap = arg0.second - arg1.second; //higher overlap first
				if (overlap >= ExcLoadEvent.MIN_CYCLE_GAP_MS)
					retval = -1;
				else if (overlap <= -1*ExcLoadEvent.MIN_CYCLE_GAP_MS)
					retval = 1;
			}
			if (retval == 0)
				retval = arg0.first - arg1.first;
				
			return retval;
		}
		
	}
	public Pair<ShovelSequence, Boolean> add(ShovelSequence seq) {
		long timingMarker1 = ThreadContextCache.g_shovelStat == null ? 0 : System.nanoTime();
		long mxts = seq.getEndTS();
		if (g_currMaxTS < mxts)
			g_currMaxTS = mxts;
		Pair<ShovelSequence, Boolean> retval = super.addWithReplacement(seq);
		removeStale();
		if (ThreadContextCache.g_shovelStat != null) 
			ThreadContextCache.g_shovelStat.shovelSequenceAdd(System.nanoTime()-timingMarker1);
		return retval;
	}
	public Pair<ShovelSequence, Boolean> add(int dumperId, long tsStart, long tsEnd) {
		ShovelSequence sseq = new ShovelSequence(dumperId, tsStart, tsEnd);
		return add(sseq);
	}
	public void removeStale() {
		if ((size()-this.szAftLastClean) > g_avoidStaleIfSzChangeLessThan) {
			long removeTill = g_currMaxTS-g_maxGapMilliRelMax;
			ShovelSequence dummySeq = new ShovelSequence(removeTill);
			Pair<Integer, Boolean> idx = this.indexOf(dummySeq);
			if (idx.first >= 0) {
				this.removeFromStart(idx.second ? idx.first-1 : idx.first);
			}
			this.szAftLastClean = size();
			
		}
		if ((unfirmedBy.size()-this.unfirmedszAftLastClean) > g_avoidStaleIfSzChangeLessThan) {
			long removeTill = g_currMaxTS-g_maxGapMilliRelMax;
			UnfirmedBy dummySeq = new UnfirmedBy(removeTill);
			Pair<Integer, Boolean> idx = unfirmedBy.indexOf(dummySeq);
			if (idx.first >= 0) {
				unfirmedBy.removeFromStart(idx.second ? idx.first-1 : idx.first);
			}
			this.unfirmedszAftLastClean = unfirmedBy.size();
			
		}
		if ((rejectedBy.size()-this.rejectedszAftLastClean) > g_avoidStaleIfSzChangeLessThan) {
			long removeTill = g_currMaxTS-g_maxGapMilliRelMax;
			UnfirmedBy dummySeq = new UnfirmedBy(removeTill);
			Pair<Integer, Boolean> idx = rejectedBy.indexOf(dummySeq);
			if (idx.first >= 0) {
				rejectedBy.removeFromStart(idx.second ? idx.first-1 : idx.first);
			}
			this.rejectedszAftLastClean = rejectedBy.size();
		}
		if ((reinstatedOnceFull.size()-this.reinstatedOnceFullSz) > g_avoidStaleIfSzChangeLessThan) {
			long removeTill = g_currMaxTS-g_maxGapMilliRelMax;
			UnfirmedBy dummySeq = new UnfirmedBy(removeTill);
			Pair<Integer, Boolean> idx = reinstatedOnceFull.indexOf(dummySeq);
			if (idx.first >= 0) {
				reinstatedOnceFull.removeFromStart(idx.second ? idx.first-1 : idx.first);
			}
			this.reinstatedOnceFullSz = reinstatedOnceFull.size();
		}
		if ((ptsFromTrip.size()-this.ptsFromTripLastClean) > g_avoidStaleIfSzChangeLessThan) {
			long removeTill = g_currMaxTS-g_maxGapMilliRelMax;
			PtFromTrip dummySeq = new PtFromTrip(removeTill);
			Pair<Integer, Boolean> idx = ptsFromTrip.indexOf(dummySeq);
			if (idx.first >= 0) {
				ptsFromTrip.removeFromStart(idx.second ? idx.first-1 : idx.first);
			}
			this.ptsFromTripLastClean = ptsFromTrip.size();
			
		}
		if ((reinstatedGoodPrevData.size()-this.reinstatedGoodPrevDataSz) > g_avoidStaleIfSzChangeLessThan) {
			long removeTill = g_currMaxTS-g_maxGapMilliRelMax;
			Long dummySeq = new Long(removeTill);
			Pair<Integer, Boolean> idx = reinstatedGoodPrevData.indexOf(dummySeq);
			if (idx.first >= 0) {
				reinstatedGoodPrevData.removeFromStart(idx.second ? idx.first-1 : idx.first);
			}
			this.reinstatedGoodPrevDataSz = reinstatedGoodPrevData.size();
			
		}
		
	}
	
	
	public static ShovelSequenceHolder getShovelInfo(int shovelId) {
		ShovelSequenceHolder retval = g_shovelInfos.get(shovelId);
		if (retval == null) {
			retval = new ShovelSequenceHolder();
			retval.shovelId = shovelId;
			g_shovelInfos.put(shovelId, retval);
		}
		return retval; 
	}
	public int getShovelId() {
		return shovelId;
	}
	public void setShovelId(int shovelId) {
		this.shovelId = shovelId;
	}
	public static class CurrInfo {
		long lastIdlingSince = 0;
		FastList<ShovelSequence> currList = new FastList<ShovelSequence>();
		long lastCompletedTripGout = 0;
		
	}
}

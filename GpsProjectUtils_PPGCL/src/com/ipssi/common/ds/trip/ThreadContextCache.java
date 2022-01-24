package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.MiscInner.TripleIntIntBool;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.ExtLUInfoExtract;
import com.ipssi.cache.BLEInfo;
import com.ipssi.cache.ExcLoadEvent;
import com.ipssi.cache.NewBLEMgmt;
import com.ipssi.cache.NewExcLoadEventMgmt;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.ShiftPlanMgmt;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.common.ds.trip.TripInfoConstants;
import java.sql.PreparedStatement;

public class ThreadContextCache {
	
	private static long g_currTS = 0;
	public static final long G_DOLASTSTOP_PROC_THRESH_MILLI = 29*1000;
	public static final long G_DOLASTSTOP_PROC_THRESH_MILLI_NOTMATCH = 59*1000;
	public static final boolean USE_NONFEASIBLE_SHOVELSEQ  = true;
	public static class ShovelStat {
		private int shovelGetCount = 0;
		private long lockAcquireTime = 0;
		private int lockAcquireCount = 0;
		private long unfirmReinstateCheck = 0;
		private int unfirmReinstateCheckCount = 0;
		private long nearnessCheckInShovel = 0;
		private long otherSuffinShovel = 0;
		private long updateWithLoadEvent = 0;
		private int updateWithLoadEventCount = 0;
		private long overlappingAndOtherCheck = 0;
		private int overlappingAndOtherCheckCount = 0;
		private long adjustTiming = 0;
		private int adjustTimingCheckCount = 0;
		private long shovelSequenceLookup = 0;
		private int shovelSequenceLookupCount = 0;
		private long shovelSequenceAddRemove = 0;
		private int shovelSequenceAddRemoveCount = 0;
		private long dirLUTimingChange = 0;
		private int dirLUTimingChangeCount = 0;
		public synchronized String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ShovelGetCnt:").append(this.shovelGetCount)
			.append(" LockAcqDur:").append(this.lockAcquireTime/1000000)
			.append(" LockAcqCnt:").append(this.lockAcquireCount)
			.append(" UnfirmDur:").append(this.unfirmReinstateCheck /1000000)
			.append("\n")
			.append(" UnfirmCnt:").append(this.unfirmReinstateCheckCount )
			.append(" NearCheckDur:").append(this.nearnessCheckInShovel/1000000)
			.append(" OtherShovelDur:").append(this.otherSuffinShovel/1000000)
			.append(" UpdLoadEventDur:").append(this.updateWithLoadEvent/1000000)
			.append("\n")
			.append(" UpdLoadEventCnt:").append(this.updateWithLoadEventCount)
			.append(" OverlapCheckDur:").append(this.overlappingAndOtherCheck/1000000)
			.append(" OverlapCheckCnt:").append(this.overlappingAndOtherCheckCount)
			.append(" AdjTimingDur:").append(this.adjustTiming /1000000)
			.append("\n")
			.append(" AdjTimingCnt:").append(this.adjustTimingCheckCount)
			.append(" SeqGetDur:").append(this.shovelSequenceLookup/1000000)
			.append(" SeqGetCnt:").append(this.shovelSequenceLookupCount)
			.append(" SeqAddDur:").append(this.shovelSequenceAddRemove/1000000)
			.append("\n")
			.append(" SeqAddCnt:").append(this.shovelSequenceAddRemoveCount)
			.append(" DirUpdatingDBDur:").append(this.dirLUTimingChange)
			.append(" DirUpdatingDBCnt:").append(this.dirLUTimingChangeCount)
			;
			return sb.toString();
		}
		public synchronized void dirLUTimingChange(long delta) {
			this.dirLUTimingChange += delta;
			this.dirLUTimingChangeCount++;
		}
		public synchronized void shovelGet() {
			shovelGetCount++;
		}
		public synchronized void lockAcquire(long delta) {
			lockAcquireTime += delta;
			lockAcquireCount++;
		}
		public synchronized void unfirmCheck(long delta) {
			unfirmReinstateCheck  += delta;
			unfirmReinstateCheckCount ++;
		}
		public synchronized void nearNessCheck(long delta) {
			nearnessCheckInShovel += delta;
		}
		public synchronized void otherSuffinShovel (long delta) {
			otherSuffinShovel  += delta;
		}
		public synchronized void updateWithEvent(long delta) {
			updateWithLoadEvent += delta;
			updateWithLoadEventCount++;
		}
		public synchronized void overlapCheck(long delta) {
			overlappingAndOtherCheck += delta;
			overlappingAndOtherCheckCount++;
		}
		public synchronized void adjustTiming(long delta) {
			adjustTiming += delta;
			adjustTimingCheckCount ++;
		}
		public synchronized void shovelSequenceGet(long delta) {
			shovelSequenceLookup += delta;
			shovelSequenceLookupCount++;
		}
		public synchronized void shovelSequenceAdd(long delta) {
			shovelSequenceAddRemove  += delta;
			shovelSequenceAddRemoveCount ++;
		}
	}
	public static ShovelStat g_shovelStat = new ShovelStat();
	
	public int dbgIndex1 = Misc.getUndefInt();
	public int dbgIndex2 = Misc.getUndefInt();
	public int dbgIndex3 = Misc.getUndefInt();
	public int dbgIndex4 = Misc.getUndefInt();
	public int dbgIndex5 = Misc.getUndefInt();
	public int dbgIndex6 = Misc.getUndefInt();
	public int dbgIndex7 = Misc.getUndefInt();
	public int dbgIndex8 = Misc.getUndefInt();
	public int dbgIndex9 = Misc.getUndefInt();
	public int dbgIndex0 = Misc.getUndefInt();
	public String toStringDbgIndices() {
		StringBuilder sb = new StringBuilder();
		sb.append(dbgIndex0)
		.append(",").append(dbgIndex1)
		.append(",").append(dbgIndex2)
		.append(",").append(dbgIndex3)
		.append(",").append(dbgIndex4)
		.append(",").append(dbgIndex5)
		.append(",").append(dbgIndex6)
		.append(",").append(dbgIndex7)
		.append(",").append(dbgIndex8)
		.append(",").append(dbgIndex9)
		;
		return sb.toString();
	}
	public String toString() {
		return toStringDbgIndices();
	}
	public void dbgClearIndex() {
		dbgIndex0 = Misc.getUndefInt();
		dbgIndex1 = Misc.getUndefInt();
		dbgIndex2 = Misc.getUndefInt();
		dbgIndex3 = Misc.getUndefInt();
		dbgIndex4 = Misc.getUndefInt();
		dbgIndex5 = Misc.getUndefInt();
		dbgIndex6 = Misc.getUndefInt();
		dbgIndex7 = Misc.getUndefInt();
		dbgIndex8 = Misc.getUndefInt();
		dbgIndex9 = Misc.getUndefInt();
		
	}
	public static final boolean g_inNewShovelPutUnfirm = false;//true case not yet proper .. need to think of cleanAll
	public synchronized static long setCurrTS(long ts) {
		if (g_currTS == 0 || g_currTS < ts)
			g_currTS = ts;
		return g_currTS;
	}
	public synchronized static long getCurrTS() {
		return g_currTS;
	}
	
	//Note on usage current: 2013-01-08
	//Calling functions could be getRegionsContaining - this will get list of fixed regions that contain the point
	//                                                getMovingOpStationContaing(opStationBean) - it will get if OpstationBean when centered contains the point
	//                                                getMovingOpStationContaining() .. this willget arraylist of opstationBean when centered containing the point
	// All of these calls will check if cached lon/lat is same as new pt and if so will return previously evaluated result else will reevaluate. Internally the first function fills
	// ArrayList<RegionTestHelper>
	
	// HOWEVER getMovignOpstationContaining behaves a bit differently - if opStationBean is given and if ArrayList<SimpleMoving> has not been fully evaluated, it will evaluate 
	//                   for that opStationBean (center and evaluate) and partially populate ArrayList<SimpleMoving>
	// if no Bean is given then will evaluate for all relevant opStations
	//To help with above bookkeeping - what has been evaluated we keep following flags:
	//searchResultValid - the ArrayList<RegionHelper> has been evaluated for the lon/lat  
	//movingResultValid - the ArrayList<SimpleRegion> has been evaluate for some OpStation for the lon/lat
	//movingResultFullyEvaluated - the ArrayList<SimpleRegion> has been evaluated for ALL OpStation of the lon/lat
	//movingSingleOpStationChecked - the opstationId for which ArrayList<SimpleRegion> been evaluated 
	//when getMovingOpStationContaining(opStationBean) is called the mvoingResultValid is true but the movingSingleOpStationChecked is different from opStationBean
	//then we assume that we have to evaluate for all opstation
	
	private InvPileLookupHelper.InvLookupInfo lastInvLookup = null;
	public InvPileLookupHelper.InvLookupInfo getLastInvLookup() {
		return lastInvLookup;
	}
	public InvPileLookupHelper.InvLookupInfo setLastInvLookup(InvPileLookupHelper.InvLookupInfo lastInvLookup) {
		return this.lastInvLookup = lastInvLookup;
	}
	public static class BestShovelGetResult {
		public ShovelSequenceHolder bestAssignableShovel;
		public ShovelSequence bestAssignableMe;
		public ArrayList<ShovelSequence> otherRequiringFirmChanges = null;
		public ArrayList<ShovelSequence.OverlapTimingChangeInfo> otherRequiringTimingChanges = null;
		public boolean toAddToShovelSeq = true;
		public boolean qualityIsProper = true;
		public ArrayList<MiscInner.TripleIntLongLong> rejectedList = null;
		public ArrayList<ShovelSequence> redoBecauseNowEnoughData = null;
	}
	public static ArrayList<ShovelSequence.OverlapTimingChangeInfo> helperAddTimingChange(ArrayList<ShovelSequence.OverlapTimingChangeInfo> theList, ShovelSequenceHolder shovelInfo, ShovelSequence meSeqCausingAdjustment, ShovelSequence sequenceBeingAdjusted, ShovelSequence.OverlapTimingChangeInfo entry) {
		if (theList == null)
			theList = new ArrayList<ShovelSequence.OverlapTimingChangeInfo>();
		for (int i=theList.size()-1;i>=0;i--) {
			if (theList.get(i).equal(entry))
				return theList;
		}
		//NOT DOING ADJUSTED_BY shovelInfo.addAdjustedBy(sequenceBeingAdjusted, meSeqCausingAdjustment);
		theList.add(entry);
		return theList;
	}
	public static ArrayList<ShovelSequence>  helperAddShovelSequence(ArrayList<ShovelSequence> theList, ShovelSequence entry) {
		if (theList == null)
			theList = new ArrayList<ShovelSequence>();
		for (int i=theList.size()-1;i>=0;i--)
			if (theList.get(i).compareTo(entry) == 0)
				return theList;
		theList.add(entry);
		return theList;
	}
	private static class CandidateShovel {
		ShovelSequenceHolder shovelSequenceHolder = null;
		ShovelSequence shovelSequence = null;
		ArrayList<ShovelSequence> otherToUnfirm = null;
		boolean feasible = true;
		ArrayList<ShovelSequence.OverlapTimingChangeInfo> overlapTimingChanges = null;
		
		public CandidateShovel(ShovelSequenceHolder shovelSequenceHolder,
				ShovelSequence shovelSequence,
				ArrayList<ShovelSequence> otherToUnfirm, boolean feasible, ArrayList<ShovelSequence.OverlapTimingChangeInfo> overlapTimingChanges) {
			super();
			this.shovelSequenceHolder = shovelSequenceHolder;
			this.shovelSequence = shovelSequence;
			this.otherToUnfirm = otherToUnfirm;
			this.feasible = feasible;
			this.overlapTimingChanges = overlapTimingChanges;
		}
	}
	public static long g_dbgDt1 = (new java.util.Date(118,10,27,12,0,0)).getTime();
	public static long g_dbgDt2 = (new java.util.Date(119,12,27,23,0,0)).getTime();
	//public static int g_dbgDumperId[] = {27301,27338,27371,27378,27332};
	public static int g_dbgDumperId[] = {27405};
	public static int g_dbgShovelId[] = {27356};
	
	//public static int g_dbgDumperId[] = {27405,27339,27378,27332,27371,27301};
	//public static int g_dbgShovelId[] = {27353,27401,27351,27355};
	public boolean dbgCheckSanity() {
		for (int i=0,is=acquiredLocks == null ? 0 : acquiredLocks.size();i<is;i++) {
			boolean ok = acquiredLocks.get(i).dbgCheckSanity();
			if (!ok)
				return false;
		}
		return true;
	}
	public static boolean dbgOfInterest(LUSequence seq, int dumperId, int shovelId) {
		boolean ofInterest = false;
		for (int i=0,is=g_dbgShovelId.length; i<is; i++) {
			if (g_dbgShovelId[i] == shovelId) {
				ofInterest = true;
				break;
			}
		}
		if (!ofInterest && !Misc.isUndef(shovelId))
			return false;
		for (int i=0,is=g_dbgDumperId.length; i<is; i++) {
			if (g_dbgDumperId[i] == dumperId) {
				ofInterest = true;
				break;
			}
		}
		if (!ofInterest && !Misc.isUndef(dumperId))
			return false;
		if (seq != null) {
			long st = seq.getStartTime();
			long en = seq.getEndTime();
			if (en < g_dbgDt1-500 || st > g_dbgDt2+500)
				return false;
		}
		return true;
	}
	public static void dbgSpecialPrint(ShovelSequenceHolder shovelInfo,StringBuilder sb) {
		if (sb == null)
			return;
		boolean ofInterest = false;
		for (int i=0,is=g_dbgShovelId.length; i<is; i++) {
			if (g_dbgShovelId[i] == shovelInfo.getShovelId()) {
				ofInterest = true;
				break;
			}
		}
		if (!ofInterest)
			return;
		ShovelSequence ref = new ShovelSequence(g_dbgDt1);
		boolean printedOnce = false;
		for (int i = 0; ; i++) {
			ShovelSequence seq = shovelInfo.get(i);
			if (seq == null && i > 0)
				break;
			if (seq == null || seq.getEndTS() <= g_dbgDt1)
				continue;
			if (seq.getStartTS() > g_dbgDt2)
				break;
			if (!printedOnce) {
				sb.append("[SHOVELSEQ]").append(shovelInfo.getShovelId());
				sb.append("\n");
				printedOnce = true;
			}
			sb.append(seq.toString());
			sb.append("\n");
		}
		
	}
	
	public ArrayList<MiscInner.TripleIntLongLong> helperSetItemsToPossiblyReinstate(List<OpStationBean> opslist,  int rejectingShovelId, int rejectingDumperId, long rejectingSt, long rejectingEn, long rejectingActStIfNotFullyremoved, long rejectingActEnIfNotFullyRemoved) {
		ShovelSequenceHolder rejectingShovelInfo = ShovelSequenceHolder.getShovelInfo(rejectingShovelId);
		if (rejectingShovelInfo == null)
			return null;
		ShovelSequence rejectingDumperSeq = new ShovelSequence(rejectingDumperId, rejectingSt, rejectingEn);
		ArrayList<MiscInner.TripleIntLongLong> rejectList = rejectingShovelInfo.getRejectedList(rejectingDumperSeq);
		ArrayList<MiscInner.TripleIntLongLong> rejectListNotOverlappingCurr = null;
		for (int i=rejectList == null ? -1 : rejectList.size()-1;i>=0;i--) {
				MiscInner.TripleIntLongLong item = rejectList.get(i);
				int rejectedDumperId = item.first;
				long st = item.second;
				long en = item.third;
				ShovelSequence entrySeq = new ShovelSequence(rejectedDumperId, st, en);
				boolean toRemove = false;
				
				if (!toRemove && rejectingShovelInfo.wasReinstatedOnceFull(entrySeq, rejectingDumperSeq))
					toRemove = true;
				if (!toRemove && opslist != null && opslist.size() != 0) {
					//check if the entry is any of ther other shovel in an overlapping manner - if so we are not going to reinstate
					for (OpStationBean bean : opslist) {
						ShovelSequenceHolder otherShovel = ShovelSequenceHolder.getShovelInfo(bean.getLinkedVehicleId());
						if (otherShovel == null)
							continue;
						if (otherShovel.matchingDumperSeq(entrySeq)) {
	//							if (!otherShovel.wasUnfirmed(rejectingDumperSeq,entrySeq)) {
	//								bestAssignableOtherUnfirm.add(rejectingDumperSeq);
	//								otherShovel.addUnfirmed(entrySeq, rejectingDumperSeq);
	//							}
							toRemove = true;
							break;
						}
					}
				}
				if (!toRemove && rejectingActEnIfNotFullyRemoved > 0 && !((en < rejectingActStIfNotFullyremoved) || (rejectingActEnIfNotFullyRemoved < st)))
						toRemove = true;
				if (toRemove) {
					rejectList.remove(i);
				}
			}
			return rejectList;
		}
	//Reservoir random sampling where there is ambiguity about best shovel - whenever we have toSwap = 0, we incr coount,
	//then we get select this one with prob of 1/count. We do it for both candidate and  be unfeasible solution
	public static final boolean DO_NEW_STOPPED_SHOVEL = true;
	public BestShovelGetResult newGetBestShovelFor(Connection conn, int ownerOrgId, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup, NewVehicleData vdp, NewVehicleData strikeVDT, GpsData loadGpsData, int dumperId, LUSequence seq, int currManualAssignedShovelId, List<OpStationBean> opslist, ReentrantLock hackLockUsedByVehicleLUSequences, int dbgRecheckMode, int prevTripShovelId) throws Exception {
		if (g_shovelStat != null) {
			g_shovelStat.shovelGet();
		}
		if (ShovelSequence.IGNORE_STRIKE)
			strikeVDT = null;
		
		if (seq.tempShovelsSeenList == null)
			seq.tempShovelsSeenList = new ArrayList<LUSequence.TempShovelInfo>(); 
		else
			seq.tempShovelsSeenList.clear();
		LUItem startItem = seq.getStartItemExt();
		long tsStart = startItem == null ? Misc.getUndefInt() : startItem.getGpsDataRecordTime();
		boolean isStartFromDataLoss = startItem.isDataLossSource();
		tsStart = TripInfoConstants.getArtificialIgnoreTime(tsStart);
		LUItem endItem = seq.getEndLUItem();
		
		long tsEnd = seq.getEndTimeSimple();
		tsEnd = TripInfoConstants.getArtificialIgnoreTime(tsEnd);
		if (tsEnd <= 0)
			tsEnd = Long.MAX_VALUE;
	//	List<OpStationBean> opslist = TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, ownerOrgId, TripInfoConstants.LOAD, 1, vehicleControlling);
		StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
		ArrayList<Integer> stopRegionList = new ArrayList<Integer>();
		stopRegionList.add(TripInfoConstants.REST_AREA_REGION);
		ArrayList<ExcLoadEvent> dummyChangeList = new ArrayList<ExcLoadEvent>();
		short startStrikeRelStart = seq.getStartStrikeRelStart();
		short endStrikeRelStart = seq.getEndStrikeRelStart();
		ArrayList<CandidateShovel> candidateShovels = new ArrayList<CandidateShovel>();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		StringBuilder sb = null;//new StringBuilder();
		boolean dbgShovelSeen = false;
		if (true && dbgOfInterest(seq,Misc.getUndefInt(), Misc.getUndefInt())) {
			sb = new StringBuilder();
		}
		if (sb == null && dbgRecheckMode == 1) {
			sb = new StringBuilder();
		}
		if (sb != null && dbgRecheckMode == 1) {
			sb.append("[RECHECK] Dumper:").append(dumperId).append("\n");
		}
		int prevBestShovelIdAssigned = seq.getPrevShovelId();
		
			
		if (false) 
			System.out.println("[DBG]###### "+dumperId+" [Thread]"+Thread.currentThread().getId()+" CurrBestShovel:"+prevBestShovelIdAssigned+" Assigned:"+currManualAssignedShovelId+" PrevTrip:"+prevTripShovelId+" Data:"+loadGpsData+"\n");
		else if (true && sb != null)
			sb.append("[DBG]###### ").append(dumperId).append(" [Thread]").append(Thread.currentThread().getId()).append(" CurrBestShovel:").append(prevBestShovelIdAssigned).append(" Assigned:").append(currManualAssignedShovelId).append(" PrevTrip:").append(prevTripShovelId).append(" Data:").append(loadGpsData).append("\n");
		ArrayList<MiscInner.PairIntLong> sortedIndicesByOverlap = new ArrayList<MiscInner.PairIntLong>();
		boolean dbgSpecialDebug = false;//dbgRecheckMode != 1;//true;
		int firstShovelSeen = Misc.getUndefInt();
		boolean onlyOneShovelSeen = false;
		boolean isStopEnded = endItem.isStopEnd();
		long stopDur = tsEnd-tsStart;
		
		ArrayList<ShovelSequence.ShovelActivityInfo> otherAltShovelActivityInfoExclBest = new ArrayList<ShovelSequence.ShovelActivityInfo>();
		long timingNearTot = 0;
		long timingOtherTot = 0;
		long timingMarker1 = 0;
		long timingMarker2 = 0;
		
		ShovelSequenceHolder bestShovelIgnoringFeasibility = null;
		ShovelSequence bestShovelSeqIgnoringFeasibility = null;
		int ignoringFeasibilitySameLevelCount = 0;
		
		int bestOverlapSec = 0;
		int bestOverlapCycle = 0;
		boolean bestIsLastRedo = false;
		
		Pair<CurrShovelInfo, CurrDumperInfo> currEntry = null;
		
		CurrDumperInfo currDumperInfo = null;
		if (DO_NEW_STOPPED_SHOVEL) {
			
			if (!isStopEnded) {//remove old entry if exists and clear candidateList, else create brand new but not add it yet
				currDumperInfo = new CurrDumperInfo(dumperId, tsStart);
				currDumperInfo.setManualAssignedShovelId(currManualAssignedShovelId);
				currDumperInfo.setPrevShovelId(seq.getPrevShovelId());
				currDumperInfo.setPrevTripShovelId(prevTripShovelId);
			}
		}
		
		for (OpStationBean op : opslist) {
			try {
				if (g_shovelStat != null) {
					timingMarker1 = System.nanoTime();
				}
				int shovelId = op.getLinkedVehicleId();
				boolean usefulShovel = sb != null && ThreadContextCache.dbgOfInterest(seq, Misc.getUndefInt(), shovelId);
				if (usefulShovel)
					dbgShovelSeen = true;
				
				VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, shovelId, false, false);
				NewExcLoadEventMgmt loadEvents = NewExcLoadEventMgmt.getLoadEventList(conn, shovelId, false);
				
				NewBLEMgmt bleEvents = NewBLEMgmt.getBLEReadList(conn, shovelId);
				ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(shovelId);
				if (shovelInfo == null)
					continue;
				if (hackLockUsedByVehicleLUSequences != null)
					hackLockUsedByVehicleLUSequences.unlock();
				//DONE ELSEWHERE this.acquireLock(shovelInfo);
				if (hackLockUsedByVehicleLUSequences != null)
					hackLockUsedByVehicleLUSequences.lock();
				if (false && sb != null && usefulShovel)
					dbgSpecialPrint(shovelInfo,sb);
				if (vdf != null) {
					synchronized (vdf) {
						//NewVehicleData shovelDataPt = vdf.getDataList(conn, shovelId, 0, true);
						//GpsData shovelDataLast = shovelDataPt == null ? null : shovelDataPt.getLast(conn);
						long currTS = getCurrTS();
						long veryOldThresh = currTS - (long)(9*60*60*1000);//DEBUG18  (long)(0.9*24*60*60*1000);
						boolean veryOldData = tsStart < veryOldThresh; 
						//cant really make changes
						if (false && !loadEvents.isValidNear(conn, tsEnd, tsStart))//DEBUG15
							continue;
						ArrayList<MiscInner.PairShortBool> detailedBLERead = isStopEnded ? null : new ArrayList<MiscInner.PairShortBool>();
						MiscInner.Triple bleRelTSStart = bleEvents.getRelBLEForDumperRelTSStart(conn, dumperId, tsStart, tsEnd,detailedBLERead);
						short bleStartSec = LUSequence.intToShortSec(bleRelTSStart.first);
						short bleEndSec = LUSequence.intToShortSec(bleRelTSStart.second);
						boolean hasBLE = bleRelTSStart.first >= 0 || bleRelTSStart.second >= 0;
						boolean likeBLE = false;
						boolean bleWorking = hasBLE || bleEvents.isBLEWorking(conn, tsStart);
						
						NewVehicleData shovelIdling = vdf.getDataList(conn, shovelId, Misc.EXC_IDLING_DIM_ID, true);
						//check against pre conf load
						Point shovelPt = null;
						
						Pair<Point,Point> lonFromPrevTrip = shovelInfo.getLonLatFromPosNew(!isStartFromDataLoss ? loadGpsData.getGps_Record_Time() : tsStart);
						Point prevBlePt = lonFromPrevTrip.first;
						Point prevAllPt = lonFromPrevTrip.second;
						boolean prevBlePtValid = !Misc.isEqual(prevBlePt.getX(), 0);
						boolean prevAllPtValid = !Misc.isEqual(prevAllPt.getX(), 0);
						GpsData centerPt = op.positionOpStationAtTime(conn, loadGpsData, true, stopDirControl);
						shovelPt =  centerPt == null ? null : centerPt.getPoint();
						boolean shovelPtValid = shovelPt != null && !Misc.isEqual(shovelPt.getX(),0);
						Point center = prevBlePtValid ? prevBlePt : prevAllPtValid ? prevAllPt : shovelPt; 
						
						ArrayList<OpStationBean> restOps = center == null ? null : getFixedOpstationsContaining(center, stopRegionList, ownerOrgId, conn, vehicleControlling, loadGpsData.getGps_Record_Time());
						boolean notInRest = restOps == null || restOps.size() == 0;
						if (!notInRest)
							continue;
						double threshDistForNear = 0.07;//bleWorking ? 0.03 : 0.06; // 0.06;//true || bleWorking ? 0.03 : 0.06;
						double distFromBLE = prevBlePtValid ? loadGpsData.distance(prevBlePt.getLongitude(), prevBlePt.getLatitude()) : 1000;
						boolean toAddBLE = prevBlePtValid &&  distFromBLE <= threshDistForNear;
						double distFromNear = prevAllPtValid ? loadGpsData.distance(prevAllPt.getLongitude(), prevAllPt.getLatitude()) : 1000; 
						double distFromShovel1 =shovelPtValid ?  loadGpsData.distance(shovelPt.getLongitude(), shovelPt.getLatitude()) : 1000;
						boolean toAddPrevTrip = toAddBLE || (prevAllPtValid && distFromNear <= threshDistForNear);
						
						boolean toAddShovelPt = toAddBLE || toAddPrevTrip || (shovelPtValid && distFromShovel1 <= threshDistForNear);

						boolean toAddCombo = toAddBLE || toAddPrevTrip || toAddShovelPt;
						double relevantDist = 1000;
						if (true) {//20190130
							relevantDist = Math.min(distFromBLE, distFromShovel1);
							relevantDist = Math.min(distFromNear, relevantDist);
							
						}
						else {
							relevantDist = prevBlePtValid ? distFromBLE : prevAllPtValid ? distFromNear : distFromShovel1;
						}
						if (false && relevantDist > threshDistForNear) {
							hasBLE = false;
							bleStartSec = Misc.getUndefShort();
							bleEndSec = Misc.getUndefShort();

						}

						//double distFromRef = center == null ? Misc.LARGE_NUMBER : data.distance(center.getX(), center.getY());
						//toAdd = center != null &&  distFromRef < threshDistForNear;//TODO //op.isPointLinkedWait(conn, data.getPoint(), center, stopDirControl);
						if (!toAddCombo && !hasBLE)
							continue;

						if (false) {
							if (relevantDist > threshDistForNear) {
								hasBLE = false;
								bleStartSec = Misc.getUndefShort();
								bleEndSec = Misc.getUndefShort();
	
							}
						}
						if (sb != null && usefulShovel)
							sb.append("[DBG]###### ").append(dumperId).append(" [Thread]").append(Thread.currentThread().getId()).append(" CurrBestShovel:").append(prevBestShovelIdAssigned).append(" Data:").append(loadGpsData).append("\n");
						//if (true && !hasBLE && toAddBLE && distFromBLE < 0.04) { //20190126 .. consider all pt
						if (true && !hasBLE && (toAddBLE ? distFromBLE : toAddPrevTrip ? distFromNear : distFromShovel1 ) < 0.03) {
							likeBLE = true;
						}
						boolean tempExists = false;
						if (Misc.isUndef(firstShovelSeen)) {
							firstShovelSeen = shovelId;
							onlyOneShovelSeen = true;
						}
						else {
							onlyOneShovelSeen = false;
						}
						
						
						
						ShovelSequence shovelSeq = new ShovelSequence(dumperId, tsStart, tsEnd);
						shovelSeq.setDist(relevantDist);
						shovelSeq.setStopEnded(isStopEnded);
						shovelSeq.setLikeBLE( (byte)(hasBLE ? 2 : likeBLE ? 1 : 0));
						
						
						if (prevBlePtValid || prevAllPtValid)
							shovelSeq.setPrevPtFromBLE(true);
						shovelSeq.setStartBLERelStartSec(bleStartSec);
						shovelSeq.setEndBLERelStartSec(bleEndSec);
						shovelSeq.setLoadStGpsStart(seq.getLoadStGpsStart());
						shovelSeq.setStartStrikeRelStartSec(startStrikeRelStart);
						shovelSeq.setEndStrikeRelStartSec(endStrikeRelStart);
						shovelSeq.setStrikeCount((byte)seq.getStrikeCount());
						shovelSeq.updateGrossActLoadTiming(); //first get start from ble/
						shovelSeq.updateDumperQuality(false);
						if (DO_NEW_STOPPED_SHOVEL && !isStopEnded) {
							CurrDumperInfo.CandidateInfo currCandidateInfo = currDumperInfo.addCandidateInfo(shovelSeq, shovelId, detailedBLERead);
							currCandidateInfo.updateCandidateInfo(conn, shovelInfo, shovelSeq, bleEvents, loadEvents, shovelIdling, strikeVDT,  sb);
							continue;
						}
						short dbgInitAct = shovelSeq.getStartActLoadRelStartSec();
						short dbgIntiEn = shovelSeq.getEndActLoadRelStartSec();
						MiscInner.PairIntBool isLastRedoOfUnfirmed = new MiscInner.PairIntBool(0,false);//shovelInfo.lastRedoOfUnfirmed(shovelSeq);
						boolean isLoadEventCompatible = shovelSeq.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleEvents, false, true, true, true,otherAltShovelActivityInfoExclBest);
						if (otherAltShovelActivityInfoExclBest != null && otherAltShovelActivityInfoExclBest.size() > 0)
							shovelSeq.setHasMultipleSubWindow(true);
						
						
						LUSequence.TempShovelInfo tempShovelInfo = LUSequence.TempShovelInfo.add(seq.tempShovelsSeenList, new LUSequence.TempShovelInfo(shovelId, shovelSeq.getDumperBestQuality(), shovelSeq.getDist5()));
						
						
						if (false) {
							shovelSeq.setStartActLoadRelStartSec(dbgInitAct);
							shovelSeq.setEndActLoadRelStartSec(dbgIntiEn);
							boolean isLoadEventCompatible2 = shovelSeq.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleEvents, false, true, true, true, null);
						}
						//shovelSeq.setBaseAct(); Already being done
						if (sb != null && usefulShovel) {
							sb.append("##[NewShovel]").append(shovelId).append(" Dumper:").append(dumperId).append(" Seq Start:").append(sdf.format(Misc.longToUtilDate(seq.getStartTimeExt())))
						
							.append("\nShovelSeq:").append(shovelSeq)
							.append(" isLoadEvenvtCompatible").append(isLoadEventCompatible)
							.append("\n");
							;
						}
						if (g_shovelStat != null) {
							timingMarker2 = System.nanoTime();
							g_shovelStat.nearNessCheck(timingMarker2-timingMarker1);
							timingMarker1 = timingMarker2;
						}
						if (!isLoadEventCompatible) {
							tempShovelInfo.setAdditionalInfo(shovelSeq.getCycleCount(), (byte)1, (byte)0);
							continue;
						}
						if (sb != null) {
							sb.append("###CheckingShovel:").append(shovelId).append("\n");
							
						}
						int szotherAltShovelActivityInfoExclBest = otherAltShovelActivityInfoExclBest == null ? 0 : otherAltShovelActivityInfoExclBest.size();
						if (!isStopEnded)
							szotherAltShovelActivityInfoExclBest = 0;
						boolean isCandidate = true;
						
						byte forThisShovelBestDumperQ = Byte.MIN_VALUE;
						int forThisShovelMinOverlapCycleCount = Integer.MAX_VALUE;
						int forThisShovelMinOverlapCycleSec= Integer.MAX_VALUE;
						ShovelSequence firstShovelSeqForThis = shovelSeq.quickCopyForRegetTime();
						firstShovelSeqForThis.copyFrom(shovelSeq);
						for (int tryAttempt = szotherAltShovelActivityInfoExclBest;tryAttempt>=0;tryAttempt--) {
							
							
							if (tryAttempt < szotherAltShovelActivityInfoExclBest) {
								ShovelSequence.ShovelActivityInfo tempActivityInfo = otherAltShovelActivityInfoExclBest.get(tryAttempt);
								shovelSeq.setThisTimingToShovelActivityInfo(tempActivityInfo, true, true, true, conn, loadEvents, shovelIdling, strikeVDT != null);
								shovelSeq.setHasInactivityAfterBefStop(true);
							}
							boolean baseActDiff = shovelSeq.getStartActLoadRelStartSec() != shovelSeq.getBaseActStartRelSec();
							if (!isStopEnded)
								baseActDiff = false;
							byte[] unchangedDurs = shovelSeq.getDurs();
							for (int artLikelyVsBase=0, artLikelyVsBaseCnt = baseActDiff ? 2 : 1;artLikelyVsBase<artLikelyVsBaseCnt;artLikelyVsBase++) {
								if (artLikelyVsBase == 1) {
									shovelSeq.quickResetToBase();
									shovelSeq.setDurs(unchangedDurs);
								}
								isCandidate = true;
								
								long meActStart = shovelSeq.getStartTSActAdj();
								long meActEnd = shovelSeq.getEndTSActAdj();
								
								MiscInner.Triple overlaps = shovelInfo.getOverlappingOtherWithStartTSNew(shovelSeq,sortedIndicesByOverlap);
								  //shovelInfo.getOverlappingOther(dumperId, meActStart, meActEnd,meActStart,meActStart-ShovelSequence.ABS_MAX_TRIPGAP_MILLI_WHEN_LO,meActEnd+ShovelSequence.ABS_MAX_TRIPGAP_MILLI_WHEN_LO,sortedIndicesByOverlap);
								
								
								ArrayList<ShovelSequence> overlapsToUnfirm = null;
								ArrayList<ShovelSequence.OverlapTimingChangeInfo> overlapTimingChanges = null;
								//BYSORTED for (int j=overlaps.first,js = overlaps.second; j >= 0 && j<=js;j++) {
								for (int j1=0,j1s = sortedIndicesByOverlap.size();j1<j1s;j1++) {
									int j = sortedIndicesByOverlap.get(j1).first;
									ShovelSequence other = shovelInfo.get(j);
									//if (!other.isFirm() || other.getDumperId() == dumperId || (other.getEndTS() < shovelSeq.getStartTS() || (other.getStartTS() > shovelSeq.getEndTS())))
									if (!other.isFirm() || other.getDumperId() == dumperId 
											|| (other.getActEndTS() < shovelSeq.getActStartTS() && shovelSeq.getActEndTS()-other.getActEndTS() >= ShovelSequence.END_TO_END_GAP_MILLI) 
											|| (other.getActStartTS() > shovelSeq.getActEndTS() && other.getActEndTS()-shovelSeq.getActEndTS() >= ShovelSequence.END_TO_END_GAP_MILLI)
											)
										continue;
									//boolean wasOtherRejectedByMe = shovelInfo.wasRejected(other, shovelSeq);
									//if (wasOtherRejectedByMe) {//continue to cause rejection 
									//	
									//}
									if (veryOldData || (other.getStartTS() < veryOldThresh && other.getStartTS() > 0)) {
										isCandidate = false;
										break;
									}
									MiscInner.TripleIntLongLong rhsHardLeftRight = shovelInfo.getLeftAndRightActEnd(other, (byte)0/*shovelSeq.getDumperBestQuality()*/, overlapTimingChanges, overlapsToUnfirm);
									//Me's start can only be moved right Or me's end can only be moved to left ... so dont have to worry about allowed movement
									ShovelSequence.OverlapChangeInfo overlapChangeInfo = shovelSeq.updateMeComboForOverlapAndCheckOverlapNeedsRecalc(shovelInfo, other, loadEvents, conn, vdp, bleEvents, strikeVDT, shovelIdling,rhsHardLeftRight.second, rhsHardLeftRight.third, overlaps.third, rhsHardLeftRight.first,isLastRedoOfUnfirmed.second);
									if (true && overlapChangeInfo.meNotFeasible) {
										int dbg = 1;
		
										ShovelSequence.OverlapChangeInfo overlapChangeInf2 = shovelSeq.updateMeComboForOverlapAndCheckOverlapNeedsRecalc(shovelInfo, other, loadEvents, conn, vdp, bleEvents, strikeVDT, shovelIdling,rhsHardLeftRight.second, rhsHardLeftRight.third, overlaps.third, rhsHardLeftRight.first,isLastRedoOfUnfirmed.second);
										dbg++;
									}
									boolean feasibleButTooEarly = shovelSeq.getEndTS()-shovelSeq.getActEndTS() > ExcLoadEvent.MAX_ENDCYCLE_STOPEND_MS;
									if (sb != null) {// && usefulShovel) {
										sb.append(" After processing overlap of other:");
										sb.append(other).append("\n");
										sb.append(" RHSUnfirm,MeNotFeasible:").append(overlapChangeInfo.makeRHSUnfirm).append(",").append(overlapChangeInfo.meNotFeasible);
										sb.append(" TooEarly:").append(feasibleButTooEarly);
										sb.append(" dbg:").append(overlapChangeInfo.dbgString);
										sb.append("\n");
										if (overlapChangeInfo.otherToBeUnfirmed != null && overlapChangeInfo.otherToBeUnfirmed.size() > 0) {
											for (int i1=0,i1s=overlapChangeInfo.otherToBeUnfirmed == null ? 0 : overlapChangeInfo.otherToBeUnfirmed.size(); i1<i1s; i1++) {
												if (i1 == 0)
													sb.append("OtherUnfirm:").append(overlapChangeInfo.otherToBeUnfirmed.get(i1)).append("\n");
											}
										}
										
										
										//.append(",").append(overlapChangeInfo.rhsActStartSec).append(",").append(overlapChangeInfo.rhsActEndSec)
										sb.append("   update shoveData ").append(shovelSeq)
										;
										if (overlapChangeInfo.timingChangeInfo != null)
											sb.append("\nNeed to change RHS:").append(overlapChangeInfo.timingChangeInfo.rhsActStartSec).append(",").append(overlapChangeInfo.timingChangeInfo.rhsActEndSec)
											.append(",").append(sdf.format(other.getStartTS()+1000*overlapChangeInfo.timingChangeInfo.rhsActStartSec))
											.append(",").append(sdf.format(other.getStartTS()+1000*overlapChangeInfo.timingChangeInfo.rhsActEndSec))
											;
										sb.append("\n");
									;
									}
									//meSeq being rejected by Other ... remember it
									if (feasibleButTooEarly) {
										overlapChangeInfo.meNotFeasible = true;
										
									}
									if (overlapChangeInfo.meNotFeasible) {//meIsNotFeasible
										tempShovelInfo.setAdditionalInfo(shovelSeq.getCycleCount(),(byte)0, feasibleButTooEarly ? (byte)2 : (byte) 1);
										isCandidate = false;
										if (forThisShovelBestDumperQ < shovelSeq.getDumperBestQuality())
											forThisShovelBestDumperQ = shovelSeq.getDumperBestQuality();
										if (forThisShovelMinOverlapCycleCount > overlapChangeInfo.overlapCycleCount)
											forThisShovelMinOverlapCycleCount = overlapChangeInfo.overlapCycleCount;
										if (forThisShovelMinOverlapCycleSec > overlapChangeInfo.overlapSec)
											forThisShovelMinOverlapCycleSec = overlapChangeInfo.overlapSec;
										
										boolean wasRejected = shovelInfo.wasRejected(other, shovelSeq);
										if (!wasRejected)
											shovelInfo.addRejected(shovelSeq, other);
										break;
									}
									if (overlapChangeInfo.makeRHSUnfirm) {
										overlapsToUnfirm = helperAddShovelSequence(overlapsToUnfirm, other);
									}
									if (overlapChangeInfo.otherToBeUnfirmed != null && overlapChangeInfo.otherToBeUnfirmed.size() > 0) {
										for (int i1=0,i1s=overlapChangeInfo.otherToBeUnfirmed == null ? 0 : overlapChangeInfo.otherToBeUnfirmed.size(); i1<i1s; i1++) {
											boolean wasUnfirmedBy = shovelInfo.wasUnfirmed(shovelSeq, overlapChangeInfo.otherToBeUnfirmed.get(i1));
											if (!wasUnfirmedBy) {
												wasUnfirmedBy = shovelInfo.hadUnfirmedMultipleTimes(shovelSeq, overlapChangeInfo.otherToBeUnfirmed.get(i1));
											}
											if (wasUnfirmedBy) {
												isCandidate = false;
												break;
											}
											overlapsToUnfirm = helperAddShovelSequence(overlapsToUnfirm, overlapChangeInfo.otherToBeUnfirmed.get(i1));
										}
										if (!isCandidate)
											break;
									}
									if (overlapChangeInfo.timingChangeInfo != null && overlapChangeInfo.timingChangeInfo.other != null) {
										overlapTimingChanges = helperAddTimingChange(overlapTimingChanges, shovelInfo, shovelSeq, other, overlapChangeInfo.timingChangeInfo);
									}
								}//for each overlap
								if (!isCandidate && !isStopEnded && tryAttempt == 0 && artLikelyVsBase == 0) {
									//check if there is some time between end etc
									long maxActEnd = -1;
									for (int j1=0,j1s = sortedIndicesByOverlap.size();j1<j1s;j1++) {
										int j = sortedIndicesByOverlap.get(j1).first;
										ShovelSequence other = shovelInfo.get(j);
										if (other.getActEndTS() > maxActEnd && other.getActEndTS() != other.getActStartTS())
											maxActEnd =  other.getActEndTS();
									}
									if (tsEnd > maxActEnd+20*1000) {
										isCandidate = true;
										shovelSeq.setActStartTS(maxActEnd+20*1000);
										shovelSeq.setEndActLoadRelStartSec(shovelSeq.getSecGap(shovelSeq.getActStartTS()));
										shovelSeq.updateGrossActLoadTiming();
										shovelSeq.setCycleCount((byte)0);
									}
								}
								if (isCandidate) {
									if (sb != null) {
										sb.append("Candidate Shovel:").append(shovelId).append(" ShoveSeq:").append(shovelSeq).append("\n");
									}
									tempShovelInfo.setAdditionalInfo(shovelSeq.getCycleCount(), (byte)0, (byte)0);
									candidateShovels.add(new CandidateShovel(shovelInfo, shovelSeq, overlapsToUnfirm, isCandidate,overlapTimingChanges));
									if (!isStartFromDataLoss) {
										shovelInfo.addPtsFromTrip(loadGpsData.getLongitude(), loadGpsData.getLatitude(), loadGpsData.getGps_Record_Time(),hasBLE);
									}
									break;
								}
							}//for act vs best effort
							if (isCandidate)
								break;
						}//for each tryAttempts
						if (isCandidate) {
							
						}
						else {
							
							int toSwap = bestShovelSeqIgnoringFeasibility == null ? 1 
									: ShovelSequence.infeasibleMeBetterThanExt(forThisShovelBestDumperQ, firstShovelSeqForThis.getDist5(), bestShovelSeqIgnoringFeasibility.getDumperBestQuality(), bestShovelSeqIgnoringFeasibility.getDist5(), prevBestShovelIdAssigned, currManualAssignedShovelId, prevTripShovelId, shovelInfo.getShovelId(), bestShovelIgnoringFeasibility.getShovelId());
							boolean useThis = toSwap > 0;
							if (useThis) {
								ignoringFeasibilitySameLevelCount = 1;
							}
							else if (toSwap == 0) {
								ignoringFeasibilitySameLevelCount++;
								double rnd = Math.random();
								double toSelProbThresh = 1.0/(double)ignoringFeasibilitySameLevelCount;
								if (rnd <= toSelProbThresh) {
									useThis = true;
								}
							}
							if (sb != null) {
								sb.append("Infeasible shovel:"+shovelId+ " CurrBest:"+(bestShovelIgnoringFeasibility == null? Misc.getUndefInt() : bestShovelIgnoringFeasibility.getShovelId())+" UseMe:"+useThis+" Seq:"+firstShovelSeqForThis);
								sb.append("\n");
							}
							
							if (useThis) {
								bestShovelIgnoringFeasibility = shovelInfo;	
								bestShovelSeqIgnoringFeasibility = firstShovelSeqForThis;
							}
						}
					}//synchronized (vdf)
				}//if vdf != null
			}//end of try
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}//end of catch
			finally {
				
			}
			if (g_shovelStat != null) {
				timingMarker2 = System.nanoTime();
				g_shovelStat.otherSuffinShovel(timingMarker2-timingMarker1);
				timingMarker1 = timingMarker2;
			}
		}//end of going thru all shovels ...
		

		//now that we have possible shovels (and LoadEvents that may need to be updated ..
		//we get the best shovel by first finding something that has the best qualitty
		//first check if there is best for this that is not yet assigned...
		if (g_shovelStat != null) {
			timingMarker2 = System.nanoTime();
			g_shovelStat.otherSuffinShovel(timingMarker2-timingMarker1);
			timingMarker1 = timingMarker2;
		}
		ShovelSequenceHolder bestQualityShovelAssignable  = null;
		ShovelSequence bestAssignableMe = null;
		ArrayList<ShovelSequence> bestAssignableOtherUnfirm = null;
		ArrayList<ShovelSequence.OverlapTimingChangeInfo> bestAssignableOtherTimingChange = null;
		int feasibleCandCount = 0;
		boolean toAddBestToShovelSeq = true;
		boolean bestIsOverlapping = false;
		if (DO_NEW_STOPPED_SHOVEL) {
			if (!isStopEnded) {
				 Triple<ShovelSequenceHolder, ShovelSequence, ArrayList<ShovelSequence.OverlapTimingChangeInfo>> result
				  = CurrShovelDumperMgmt.handleAdd(conn, currDumperInfo,sb);
				 toAddBestToShovelSeq = false;
				 bestIsOverlapping = false;
				 bestQualityShovelAssignable  = result.first;
				 bestAssignableMe = result.second;
				 bestAssignableOtherTimingChange = result.third;
			}
			
		}
		if (!DO_NEW_STOPPED_SHOVEL || isStopEnded) {
			for (int i=0,is=candidateShovels.size();i<is;i++) {
				CandidateShovel candidateEntry = candidateShovels.get(i);
				ShovelSequenceHolder candidate = candidateEntry.shovelSequenceHolder;
				int shovelId = candidate.getShovelId();
				
				ShovelSequence me = candidateEntry.shovelSequence;
				if (!candidateEntry.feasible)
					continue;
				int toSwap = bestAssignableMe == null ? 1 : me.meBetterThanExt(bestAssignableMe, prevBestShovelIdAssigned, currManualAssignedShovelId, prevTripShovelId, candidate.getShovelId(), bestQualityShovelAssignable == null ? Misc.getUndefInt() : bestQualityShovelAssignable.getShovelId(), conn);
				boolean useMe = toSwap > 0;
				if (useMe) {
					feasibleCandCount = 1;
				}
				else if (toSwap == 0) {
					feasibleCandCount++;
					double rnd = Math.random();
					double toSelProbThresh = 1.0/(double)feasibleCandCount;
					if (rnd <= toSelProbThresh) {
						useMe = true;
					}
				}
				if (useMe) {
					if (dbgShovelSeen && sb != null) {
						sb.append(" Replacing curr best: Shovel:").append(shovelId).append(" Best").append(bestAssignableMe).append("\n").append(" By:").append(me).append("\n");
					}
					bestQualityShovelAssignable = candidate;
					bestAssignableMe = me;
					bestAssignableOtherUnfirm = candidateEntry.otherToUnfirm;
					bestAssignableOtherTimingChange = candidateEntry.overlapTimingChanges;
				}
			}//evaluated all candidate shovels
			int tempCnt = 0;
			
			if (bestAssignableMe != null) {
				if (bestAssignableMe.strictNoLoad()) {
					toAddBestToShovelSeq = false;
					bestAssignableOtherUnfirm = null;
					bestAssignableOtherTimingChange = null;
				}
			}
			else if (USE_NONFEASIBLE_SHOVELSEQ) {
				bestIsOverlapping = true;
				toAddBestToShovelSeq = false;
				bestQualityShovelAssignable = bestShovelIgnoringFeasibility;
				bestAssignableMe = bestShovelSeqIgnoringFeasibility;
				bestAssignableOtherUnfirm = null;
				bestAssignableOtherTimingChange = null;
				if (sb != null) {
					sb.append("Selected Infeasible shovel:"+(bestShovelIgnoringFeasibility == null? Misc.getUndefInt() : bestShovelIgnoringFeasibility.getShovelId())+"  Seq:"+bestShovelSeqIgnoringFeasibility);
					sb.append("\n");
				}
				
				if (true && bestShovelIgnoringFeasibility != null) {
					for (int i=0,is=seq.tempShovelsSeenList == null ? 0 : seq.tempShovelsSeenList.size();i<is;i++) {
						if (seq.tempShovelsSeenList.get(i).shovelId == bestShovelIgnoringFeasibility.getShovelId()) {
							if (i != 0) {
								LUSequence.TempShovelInfo temp = seq.tempShovelsSeenList.get(i);
								seq.tempShovelsSeenList.remove(i);
								seq.tempShovelsSeenList.add(0,temp);
							}
							break;
						}
					}
				}
			}
			if (bestAssignableMe != null && !bestIsOverlapping) {//check if feasible in more than one with same Q
				int bestQ = bestAssignableMe.getDumperBestQuality();
				
				int bestAssignableShovelId =bestQualityShovelAssignable.getShovelId(); 
				for (int i=0,is=candidateShovels.size();i<is;i++) {
					CandidateShovel candidateEntry = candidateShovels.get(i);
					ShovelSequenceHolder candidate = candidateEntry.shovelSequenceHolder;
					int shovelId = candidate.getShovelId();
					
					ShovelSequence me = candidateEntry.shovelSequence;
					if (candidateEntry.feasible && candidateEntry.shovelSequenceHolder.getShovelId() != bestAssignableShovelId && (me.getDumperBestQuality() >= bestQ)) {
						tempCnt++;
					}
				}
				if (tempCnt > 0)
					bestAssignableMe.setMultShoveFeasible((byte)tempCnt);
			}
		}
		
		
		
		BestShovelGetResult retval = new BestShovelGetResult();
		retval.toAddToShovelSeq = 	toAddBestToShovelSeq;
		retval.qualityIsProper = !bestIsOverlapping; 
		retval.bestAssignableShovel = bestQualityShovelAssignable;
		retval.otherRequiringFirmChanges = bestAssignableOtherUnfirm;
		retval.bestAssignableMe = bestAssignableMe;
		retval.otherRequiringTimingChanges = bestAssignableOtherTimingChange;
		 if (bestAssignableMe != null && bestAssignableMe.isPrevPtFromBLE() && !isStartFromDataLoss) {
			 retval.redoBecauseNowEnoughData = bestQualityShovelAssignable.getRedoListBecauseEnoughData(loadGpsData.getGps_Record_Time()); 
		 }
		 
		
		if (prevBestShovelIdAssigned > 0 && seq.markedForShovelGet() != LUSequence.SHOVEL_REGET_NOT_INSHOVELSEQ) {
			ShovelSequenceHolder bestShovelToUse =bestQualityShovelAssignable;
			ShovelSequence bestSequenceToUse = bestAssignableMe;
			boolean meShovelSeqBeingRemoved = prevBestShovelIdAssigned > 0 && (bestShovelToUse == null || bestShovelToUse.getShovelId() != prevBestShovelIdAssigned);
			 
			retval.rejectedList = helperSetItemsToPossiblyReinstate(opslist, prevBestShovelIdAssigned, dumperId, tsStart, tsEnd, meShovelSeqBeingRemoved || bestSequenceToUse == null ? -1 : bestSequenceToUse.getActStartTS(), meShovelSeqBeingRemoved || bestSequenceToUse == null ? -1 : bestSequenceToUse.getActEndTS());
			//if (meShovelSeqBeingRemoved)
			//	retval.rejectedList = helperSetItemsToPossiblyReinstate(opslist, prevBestShovelIdAssigned, dumperId, tsStart, tsEnd, meShovelSeqBeingRemoved || bestSequenceToUse == null ? -1 : bestSequenceToUse.getActStartTS(), meShovelSeqBeingRemoved || bestSequenceToUse == null ? -1 : bestSequenceToUse.getActEndTS());
		}
		if (g_shovelStat != null) {
			timingMarker2 = System.nanoTime();
			g_shovelStat.otherSuffinShovel(timingMarker2-timingMarker1);
			timingMarker1 = timingMarker2;
		}
		if (sb != null) {
			sb.append("[FIN]").append(dumperId).append(" TS:").append(sdf.format(Misc.longToUtilDate(loadGpsData.getGps_Record_Time()))).append(" BestShovel:").append(retval.bestAssignableShovel == null ? Misc.getUndefInt() : retval.bestAssignableShovel.getShovelId())
					.append(" SeqEnded:").append(isStopEnded).append(" QProp:").append(retval.qualityIsProper)
					.append(" InShovSeq:").append(retval.toAddToShovelSeq)
					.append("\n")
					.append(retval.bestAssignableMe)
				;
			
			sb.append("\n######## end ");
			System.out.println(sb);
		}
		
		if (ThreadContextCache.g_dbLogActivity) {
			MiscInner.PairIntLong calcAssignment = ShiftPlanMgmt.getLatestAssignment(conn, dumperId, tsStart);
			addShovelSelectionActivity(retval, prevBestShovelIdAssigned, currManualAssignedShovelId, calcAssignment == null ? Misc.getUndefInt() : calcAssignment.first, calcAssignment == null ? 0 : calcAssignment.second);
		}
		return retval;
	}	

	
	
	public static class SimpleMoving {
		private OpStationBean opstationBean;
		private Point center;
		public SimpleMoving(OpStationBean opstationBean, Point center) {
			this.opstationBean = opstationBean;
			this.center = center;
		}
		public OpStationBean getOpstationBean() {
			return opstationBean;
		}
		public void setOpstationBean(OpStationBean opstationBean) {
			this.opstationBean = opstationBean;
		}
		public Point getCenter() {
			return center;
		}
		public void setCenter(Point center) {
			this.center = center;
		}
	}	
	private double lon = Misc.getUndefDouble();
	private double lat = Misc.getUndefDouble();
	private ArrayList<RegionTestHelper> searchResult = null;
	private ArrayList<ShovelSequenceHolder> acquiredLocks = new ArrayList<ShovelSequenceHolder>();
	public void releaseLockExclThese(ArrayList<ShovelSequenceHolder> exclList) {
		for (int i=acquiredLocks.size()-1;i>=0;i--) {
			int shovelId = acquiredLocks.get(i).getShovelId();
			boolean toExcl = true;
			for (int j=0,js=exclList == null ? 0 : exclList.size(); j<js;j++)
				if (exclList.get(j).getShovelId() == shovelId) {
					toExcl = false;
					break;
				}
			if (toExcl) {
				acquiredLocks.get(i).unlock();
				acquiredLocks.remove(i);
			}
		}
	}
	public void releaseLock(ShovelSequenceHolder shovelInfo) {
		int shovelId = shovelInfo.getShovelId();
		for (int i=acquiredLocks.size()-1;i>=0;i--)
			if (acquiredLocks.get(i).getShovelId() == shovelId) {
				//System.out.println("Thread:"+Thread.currentThread().getId()+" Releasing lock:"+shovelId);
				acquiredLocks.get(i).unlock();
				acquiredLocks.remove(i);
				break;
			}
	}
	public void printAcquiredLocks() {
		StringBuilder sb = new StringBuilder();
		sb.append("Thread:]").append(Thread.currentThread().getId()).append(": AcquiredLocks:");
		for (int i=0,is=acquiredLocks == null ? 0 : acquiredLocks.size();i<is;i++) {
			if (i != 0)
				sb.append(",");
			sb.append(acquiredLocks.get(i).getShovelId());
		}
		System.out.println(sb);
	}
	public boolean isLocked(int shovelId) {
		for (int i=0,is=acquiredLocks.size(); i<is;i++)
			if (acquiredLocks.get(i).getShovelId() == shovelId) {
				return true;
			}
		return false;
	}
	public void acquireLock(ShovelSequenceHolder shovelInfo) {
		//long timingMarker1 = g_shovelStat == null ? 0 : System.nanoTime();
		boolean added = false;
		int shovelId = shovelInfo.getShovelId();
		for (int i=0,is=acquiredLocks.size(); i<is;i++)
			if (acquiredLocks.get(i).getShovelId() == shovelId) {
				added = true;
				break;
			}
		if (!added) {
			acquiredLocks.add(shovelInfo); 
		//	System.out.println("Thread:"+Thread.currentThread().getId()+" Acquiring lock:"+shovelId);
			shovelInfo.lock();
		}
		//if (g_shovelStat != null) {
		//	g_shovelStat.lockAcquire(System.nanoTime()-timingMarker1);
		//}
	}
	private boolean searchResultValid = false; //will be set to true if searchResult has been evaluated at lon/lat 

	
	private ArrayList<SimpleMoving> movingResult = null;
	private boolean movingFullyEvaluated = false;
	private int movingSingleOpStationChecked = Misc.getUndefInt();
	private boolean movingResultValid = false;
	public boolean checkAndResetValidity(Point pt) {
		return checkAndResetValidity(pt.getLongitude(), pt.getLatitude());
	}
	public boolean checkAndResetValidity(double x, double y) {
		if (!Misc.isEqual(x, this.lon) || !Misc.isEqual(y, this.lat)) {
			searchResultValid = false;
			movingResultValid = false;
			movingFullyEvaluated = false;
			searchResult = null;
			movingResult = null;
			movingSingleOpStationChecked = Misc.getUndefInt();
			this.lon = x;
			this.lat = y;
			return false;
		}
		return true;
	}

	public boolean isPointInRegion(Point pt, int regionId) {
		ArrayList<RegionTestHelper> result = getRegionsContaining(pt);
		for (int i=0,is=result == null ? null : result.size();i<is;i++) {
			RegionTestHelper rth  = result.get(i);
			if (rth.region.id  == regionId)
				return true;
		}
		return false;
	}
	
	public static boolean isInSomeOpStation(Connection conn, double lon, double lat, int vehiclePortNodeId, ArrayList<Integer> optypes, int vehicleId, long dt) throws Exception {
			Pair<Boolean, ArrayList<RegionTestHelper>> retval = isInOpStationAndAllRegIn(conn,  lon,  lat,  vehiclePortNodeId,  optypes,  vehicleId,  dt);
			return retval != null && retval.first;
	}
	public static Pair<Boolean, ArrayList<RegionTestHelper>> isInOpStationAndAllRegIn(Connection conn, double lon, double lat, int vehiclePortNodeId, ArrayList<Integer> optypes, int vehicleId, long dt) throws Exception {
		VehicleControlling vehicleControlling = NewProfileCache.getOrCreateControlling(vehicleId);
		if (optypes == null || optypes.size() == 0)
			optypes = TripInfoCacheHelper.getAppropFixedType(true);
		ArrayList<RegionTestHelper> result = RTreeSearch.getContainingRegions(new Point(lon, lat));
		if (result == null)
			result = new ArrayList<RegionTestHelper>(); //so as to know nothing found

		Cache cache = Cache.getCacheInstance(conn);
		for (int i=0,is=result == null ? null : result.size();i<is;i++) {
			RegionTestHelper rth  = result.get(i);
			ArrayList<Integer> oplist = TripInfoCacheHelper.getOpListForWait(rth.region.id);
			for (int j=0,js = oplist == null ? 0 : oplist.size(); j<js;j++) {
				int opstationId = oplist.get(j);
				OpStationBean opStation = TripInfoCacheHelper.getOpStation(opstationId);
				if (opStation == null)
					continue;
				//check if matching type and then check if belongs from opstation point of view ..
				int opstationType  = opStation.getOpStationType(conn, cache, vehiclePortNodeId, vehicleControlling);
				boolean matchingType = false;
				for (int k=0, ks=optypes.size();k<ks;k++) {
					if (optypes.get(k) == opstationType && (dt <= 0 || opStation.isValidByDate(dt))) {
						return new Pair<Boolean, ArrayList<RegionTestHelper>> (true, result);
					}
				}
			}//for each opstation that are mapped to waitareaid o
		}//for each region that contain the point
		return new Pair<Boolean, ArrayList<RegionTestHelper>> (false, result);
	}

	public ArrayList<OpStationBean> getFixedOpstationsContaining(Point pt, ArrayList<Integer> optypes, int vehiclePortNodeId, Connection conn, VehicleControlling vehicleControlling, long dt) throws Exception {	
		ArrayList<RegionTestHelper> result = getRegionsContaining(pt);
		ArrayList<OpStationBean> retval = null;
		Cache cache = Cache.getCacheInstance(conn);
		for (int i=0,is=result == null ? null : result.size();i<is;i++) {
			RegionTestHelper rth  = result.get(i);
			ArrayList<Integer> oplist = TripInfoCacheHelper.getOpListForWait(rth.region.id);
			for (int j=0,js = oplist == null ? 0 : oplist.size(); j<js;j++) {
				int opstationId = oplist.get(j);
				OpStationBean opStation = TripInfoCacheHelper.getOpStation(opstationId);
				if (opStation == null)
					continue;
				//check if matching type and then check if belongs from opstation point of view ..
				int opstationType  = opStation.getOpStationType(conn, cache, vehiclePortNodeId, vehicleControlling);
				boolean matchingType = false;
				for (int k=0, ks=optypes.size();k<ks;k++) {
					if (optypes.get(k) == opstationType) {
						matchingType = true;
						break;
					}
				}
				if (matchingType && dt > 0) 
					matchingType = opStation.isValidByDate(dt);
				if (matchingType) {
					if (retval == null) {
						retval = new ArrayList<OpStationBean>();
					}
					retval.add(opStation);
				}//if type was matching ... 
			}//for each opstation that are mapped to waitareaid o
		}//for each region that contain the point
		return retval;
	}

	public ArrayList<RegionTestHelper> getRegionsContaining(Point pt)  {
		if (!checkAndResetValidity(pt) || !searchResultValid) {
			//do search
			try {
				searchResult = RTreeSearch.getContainingRegions(pt);
				searchResultValid = true;
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			this.lon = pt.getLongitude();
			this.lat = pt.getLatitude();
		}
		return searchResult;
	}
	public OpStationBean getNearestMovingOpStationContaining(Connection conn, int ownerOrgId, GpsData data, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) {
		ArrayList<SimpleMoving> movingList = getMovingOpStationContaining(conn, ownerOrgId, data, vehicleControlling, vehSetup);
		double mindist = 0;
		OpStationBean retval = null;
		for (int i=0,is = movingList == null ? 0 : movingList.size(); i<is;i++) {
			SimpleMoving movingInfo = movingList.get(i);
			OpStationBean bean = movingInfo.getOpstationBean();
			if (bean == null)
				continue;
			double d = movingInfo.center.distance(data.getPoint());
			if (retval == null || d < mindist) {
				retval = bean;
				mindist = d;
			}
		}
		return retval;
	}
	
	public ArrayList<SimpleMoving> getMovingOpStationContaining(Connection conn, int ownerOrgId, GpsData data, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) {
		try {
			
			if (!checkAndResetValidity(data.getPoint()) || !movingResultValid || !movingFullyEvaluated) {
				//HACK currently moving assumed to be of Type LOAD
				Cache cache = Cache.getCacheInstance(conn);
				List<OpStationBean> opslist = TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, ownerOrgId, TripInfoConstants.LOAD, 1, vehicleControlling);
				StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
				ArrayList<Integer> stopRegionList = new ArrayList<Integer>();
				stopRegionList.add(TripInfoConstants.REST_AREA_REGION);
				Collections.sort(opslist);
				for (OpStationBean op : opslist) {
					if (op.getOpStationId() != this.movingSingleOpStationChecked) {
						try {
							op.tryLock();
							GpsData centerPt = op.positionOpStationAtTime(conn, data, true, stopDirControl);
							Point center = centerPt == null ? null : centerPt.getPoint();
							//double dbgD = center == null ? Misc.getUndefDouble() : centerPt.distance(data.getLongitude(), data.getLatitude());
							boolean toAdd = center != null && op.isPointLinkedWait(conn, data.getPoint(), center, stopDirControl);
							
							if (center != null && op.isPointLinkedWait(conn, data.getPoint(), center, stopDirControl)) {
								ArrayList<OpStationBean> restOps = this.getFixedOpstationsContaining(center, stopRegionList, ownerOrgId, conn, vehicleControlling, data.getGps_Record_Time());
								if (restOps == null || restOps.size() == 0)
									addMovingResult(op, center);
							}
						}
						catch (Exception e1) {
							//eat it
						}
						finally {
							op.unlock();
						}
					}
				}
				movingFullyEvaluated = true;
				movingResultValid = true;
			}
			return this.movingResult;
		}
		catch (Exception e) {
			
		}
		return null;
	}
	
	public SimpleMoving getMovingOpStationContaining(Connection conn, int ownerOrgId, GpsData data, OpStationBean op, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) {
		try {
			boolean isOldPt = checkAndResetValidity(data.getPoint());
			boolean lookupFromMovingResult = false;
			if ( isOldPt && movingResultValid && movingFullyEvaluated) {
				lookupFromMovingResult = true;
			}
			else if (isOldPt && movingResultValid && op.getOpStationId() == this.movingSingleOpStationChecked) {
				lookupFromMovingResult = true;
			}
			else if (isOldPt && !movingResultValid && Misc.isUndef(this.movingSingleOpStationChecked)) {
				try {
					StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
					op.tryLock();
					
					GpsData centerPt = op.positionOpStationAtTime(conn, data, true, stopDirControl);
					Point center = centerPt == null ? null : centerPt.getPoint();
					if (centerPt != null && op.isPointLinkedWait(conn, data.getPoint(), center, stopDirControl)) {
						addMovingResult(op, center);
					}
				}
				catch (Exception e1) {
					//eat it
				}
				finally {
					op.unlock();
				}
				movingResultValid = true;
				movingSingleOpStationChecked = op.getOpStationId(); 
				lookupFromMovingResult = true;
			}
			if (!lookupFromMovingResult) {
				getMovingOpStationContaining(conn, ownerOrgId, data, vehicleControlling, vehSetup);
			}
			for (int i=0,is=movingResult == null ? 0 : movingResult.size(); i<is; i++) {
				if (movingResult.get(i).getOpstationBean().getOpStationId() == op.getOpStationId())
					return movingResult.get(i);
			}
			return null;
			
		}
		catch (Exception e) {
			
		}
		return null;
	}
	
	public void addMovingResult(OpStationBean bean, Point center) {
		if (movingResult == null)
			movingResult = new ArrayList<SimpleMoving>();
		movingResult.add(new SimpleMoving(bean, center));
	}
	public static final byte SHOVEL_SELECTION_NORMAL = 0;
	public static final byte SHOVEL_SELECTION_ONGOING_LOCYCLE = 1;
	public static final byte SHOVEL_SELECTION_NOT_INSEQ = 2;
	private static class DbgLogActivity {
		
		public int dumperId;
		public int shovelId;
		public int prevShovelId;
		public int numCycles;
		public int shovelCycleDur;
		public int tripId;
		public long tsStart;
		public long tsEnd;
		public long actStartTS;
		public long actEndTS;
		public long latestGRTSeen;
		public int unloadOpId;
		public boolean hasEnded;
		public boolean fromFinTrip;
		public byte shovelSelectionType;
		public long updatedOn;
		public int manualShovelId = Misc.getUndefInt();
		public int calcManualShovelId = Misc.getUndefInt();
		public long calcManualAssignTS = 0;
		public DbgLogActivity(int dumperId, int shovelId, int prevShovelId, int unloadOpId,
				int numCycles, int shovelCycleDur, int tripId, long tsStart,
				long tsEnd, long actStartTS, long actEndTS, long latestGRTSeen, boolean hasEnded, boolean activityType, byte shovelSelectionType) {
			super();
			this.updatedOn = System.currentTimeMillis();
			this.dumperId = dumperId;
			this.shovelId = shovelId;
			this.prevShovelId = prevShovelId;
			this.numCycles = numCycles;
			this.shovelCycleDur = shovelCycleDur;
			this.tripId = tripId;
			this.tsStart = tsStart;
			this.tsEnd = tsEnd;
			this.actStartTS = actStartTS;
			this.actEndTS = actEndTS;
			this.hasEnded = hasEnded;
			this.fromFinTrip = activityType;
			this.latestGRTSeen = latestGRTSeen;
			this.shovelSelectionType = shovelSelectionType;
		}
		
	}
	private ArrayList<DbgLogActivity> dbgActivityList = new ArrayList<DbgLogActivity>();
	public static final boolean g_dbLogActivity = true;
	public void clearActivityLog() {
		this.dbgActivityList.clear();
	}
	public void addShovelSelectionActivity(ThreadContextCache.BestShovelGetResult result, int prevShovelId, int manualShovelId, int calcManualShovelId, long calcAssignmentTS) {
		ShovelSequence shovelSeq = result.bestAssignableMe;
		if (shovelSeq == null)
			return;
		DbgLogActivity activity = new DbgLogActivity(shovelSeq.getDumperId(), result.bestAssignableShovel.getShovelId(), prevShovelId, Misc.getUndefInt(), 
				shovelSeq.getCycleCount(), shovelSeq.getAvgDur(), Misc.getUndefInt(), shovelSeq.getStartTS(),
				shovelSeq.getEndTS(), shovelSeq.getActStartTS(), shovelSeq.getActEndTS(), ThreadContextCache.getCurrTS(), shovelSeq.isStopEnded(),false, (byte)(result.toAddToShovelSeq ? SHOVEL_SELECTION_NORMAL : result.qualityIsProper ?  ThreadContextCache.SHOVEL_SELECTION_ONGOING_LOCYCLE : ThreadContextCache.SHOVEL_SELECTION_NOT_INSEQ));
		activity.manualShovelId = manualShovelId;
		activity.calcManualShovelId = calcManualShovelId;
		activity.calcManualAssignTS = calcAssignmentTS;
		this.dbgActivityList.add(activity);
	}
	public void addTripFinalActivity(ArrayList<TripStat> stats) {
		for (int i=0,is=stats == null ? 0 : stats.size();i<is;i++) {
			TripStat stat = stats.get(i);
			DbgLogActivity activity = new DbgLogActivity(stat.getDumperId(), stat.getShovelId(), stat.getPrevShovelId(), stat.getUnloadOpId(), 
					stat.getNumCycles(), stat.getAvgCycleSec(), stat.getTripId(), stat.getAdjlgin(),
					stat.getAdjlgout(), stat.getLgin(), stat.getLgout(), ThreadContextCache.getCurrTS(), stat.hasStopEnded(), true, stat.getInShovelSeq());
			this.dbgActivityList.add(activity);
		}
		
		
	}
	public void saveAndClearActivityLog(Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("insert into dbg_get_best_shovel_result (dumperId, shovelId, prevShovelId, numCycles, shovelCycleDur, tripId "+
					   ",tsStart, tsEnd, actTSStart, actTSEnd "+
					   ",curr_grt, unloadopId, hasEnded, fromFinTrip, shovelSelectionType, updated_on, manual_shovel, calc_manual_shovel, calc_manual_ts) values ("+
					   "?, ?, ?, ?, ?, ? "+
					   ",?, ?, ?, ? "+
					   ",?, ?, ?, ?, ?,?,?,?,?)"
					   )
					   ;
			for (int i=0,is=this.dbgActivityList.size();i<is;i++) {
				DbgLogActivity activity = this.dbgActivityList.get(i);
				Misc.setParamInt(ps, activity.dumperId, 1);
				Misc.setParamInt(ps, activity.shovelId, 2);
				Misc.setParamInt(ps, activity.prevShovelId, 3);
				Misc.setParamInt(ps, activity.numCycles, 4);
				Misc.setParamInt(ps, activity.shovelCycleDur, 5);
				Misc.setParamInt(ps, activity.tripId, 6);
				ps.setTimestamp(7,Misc.longToSqlDate(activity.tsStart));
				ps.setTimestamp(8,Misc.longToSqlDate(activity.tsEnd));
				ps.setTimestamp(9,Misc.longToSqlDate(activity.actStartTS));
				ps.setTimestamp(10,Misc.longToSqlDate(activity.actEndTS));
				ps.setTimestamp(11,Misc.longToSqlDate(activity.latestGRTSeen));
				ps.setTimestamp(12,Misc.longToSqlDate(activity.unloadOpId));
				ps.setInt(13, activity.hasEnded ? 1 : 0);
				ps.setInt(14, activity.fromFinTrip ? 1 : 0);
				ps.setInt(15, activity.shovelSelectionType);
				ps.setLong(16, activity.updatedOn);
				ps.setInt(17, activity.manualShovelId);
				ps.setInt(18, activity.calcManualShovelId);
				ps.setTimestamp(19, Misc.longToSqlDate(activity.calcManualAssignTS));
				ps.addBatch();
			}
			if (dbgActivityList.size() > 0)
				ps.executeBatch();
			ps = Misc.closePS(ps);
			this.dbgActivityList.clear();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
			
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
}

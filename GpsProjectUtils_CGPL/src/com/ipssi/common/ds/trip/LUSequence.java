package com.ipssi.common.ds.trip;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.ShapeFileBean;
import com.ipssi.mining.BestMergeDistResult;
import com.ipssi.mining.ManagementUnit;
import com.ipssi.miningOpt.InvRTree;
import com.ipssi.miningOpt.LoadSite;
import com.ipssi.miningOpt.NewMU;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.routemonitor.RouteDef;
import com.ipssi.tripcommon.ExtLUInfoExtract;
import com.ipssi.tripcommon.LUInfoExtract;
import com.ipssi.cache.BLEInfo;
import com.ipssi.cache.Dala01Mgmt;
import com.ipssi.cache.DirResult;
import com.ipssi.cache.NewBLEMgmt;
import com.ipssi.cache.NewExcLoadEventMgmt;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.ThreadContextCache.BestShovelGetResult;
import com.ipssi.userNameUtils.IdInfo;

public class LUSequence extends FastList<LUItem> implements Comparable<LUSequence>, Serializable {
	private static final long serialVersionUID = 1L;
	public static final byte SHOVEL_REGET_NONEED = 0;
	public static final byte SHOVEL_REGET_NORMAL = 1;
	public static final byte SHOVEL_REGET_CHECKINSHOVEL = 2;
	public static final byte SHOVEL_REGET_NOT_INSHOVELSEQ = 3;
	public static final int g_ruleIdForDirChangeLo = 502;
	public static final int g_ruleIdForDirChangeHi = 503;
	public static final int g_ruleIdForDirChangeBoth = 504;
	public static final int g_ruleIdForDirChangeSuper = 505;
	public static final int g_ruleIdForDirChangeSuperBoth = 506;
	
	public static final int g_ruleIdForDirChangeLoSupp = 507;
	public static final int g_ruleIdForDirChangeHiSupp = 508;
	public static final int g_ruleIdForDirChangeBothSupp = 509;
	public static final int g_ruleIdForDirChangeSuperSupp = 510;
	public static final int g_ruleIdForDirChangeSuperBothSupp = 511;
	public static final int g_ruleIdForDirChangeNone = 512;
	public static final String g_allRuleIdCSVForDirChange = g_ruleIdForDirChangeLo+","+g_ruleIdForDirChangeHi+","+g_ruleIdForDirChangeBoth+","
	+g_ruleIdForDirChangeSuper+","+g_ruleIdForDirChangeSuperBoth+","+g_ruleIdForDirChangeLoSupp+","
	+g_ruleIdForDirChangeHiSupp+","+g_ruleIdForDirChangeBothSupp+","+g_ruleIdForDirChangeSuperSupp+","
	+g_ruleIdForDirChangeSuperBothSupp+","+g_ruleIdForDirChangeNone
	;
	
	public static class HelperTrackTripUse implements Comparable {
		private int tripId;
		private long loadGIn = Misc.getUndefInt();
		private int artOpStationId = Misc.getUndefInt();
		public String toString() {
			return"tripid:"+tripId+" Gin:"+Misc.longToUtilDate(loadGIn)+" Ls:"+loadSeen+" Us:"+unloadSeen;
		}
		public int compareTo(Object obj) {
			HelperTrackTripUse p = (HelperTrackTripUse)obj;
			return this.tripId < p.tripId ? -1 : this.tripId > p.tripId ? 1 : 0;		
		}
		public HelperTrackTripUse(int tripId, long loadGin, int artOpStationId) {
			super();
			this.tripId = tripId;
			this.loadSeen = false;
			this.unloadSeen = false;
			this.loadGIn = loadGin;
			this.artOpStationId = artOpStationId;
		}
		private boolean loadSeen;
		private boolean unloadSeen;
		public int getTripId() {
			return tripId;
		}
		public void setTripId(int tripId) {
			this.tripId = tripId;
		}
		public boolean isLoadSeen() {
			return loadSeen;
		}
		public void setLoadSeen(boolean loadSeen) {
			this.loadSeen = loadSeen;
		}
		public boolean isUnloadSeen() {
			return unloadSeen;
		}
		public void setUnloadSeen(boolean unloadSeen) {
			this.unloadSeen = unloadSeen;
		}
		public long getLoadGIn() {
			return loadGIn;
		}
		public int getArtOpStationId() {
			return artOpStationId;
		}
	}
	public class AddnlInfo implements Serializable {
		private static final long serialVersionUID = 1L;



//	public static class AddnlInfo implements Serializable {
//		private static final long serialVersionUID = 1L;
		private AddnlMiningInfo miningInfo = null;
		private int tripId = Misc.getUndefInt();
		private int rightTripId = Misc.getUndefInt();
		private int intermediateLUId = Misc.getUndefInt();
		private int intermediateParentTripId = Misc.getUndefInt();
		private int cachedNextTripId = Misc.getUndefInt();//cant make transient ... will lead to issues when recovering
		private int cachedPrevTripId = Misc.getUndefInt();
		private int opBelonging = Misc.getUndefInt();

		transient private LUInfoExtract cachedTripDataLU = null; //have to make it transient ... else leads non-serializable
		transient private int cachedLUOfTripId = Misc.getUndefInt();
		
		//2014_06_20		transient private boolean reextractDependentUponNext = false;
		transient private LUInfoExtract cachedIntermediateLUInfoExtract = null; // the LUInfoExtract whose info is kept in cachedIntermediateLUInfoExtract ... will be updated in
		//2014_06_20		transient boolean markedAsMerged = false;
		//2014_06_20		transient boolean markedAsDeleted  = false; //NOT USED
		ArrayList<Integer> otherMergedTripId = null;
		//ArrayList<Integer> otherIntermediateLUId = null;
		private long challanDateFromInternalProc = 0;//dont make it undef
		transient private OpStationBean opBelongingBean = null;
		// to keep material data
		private transient ArrayList<Pair<Integer, Integer>> materialLUItemIndexList = null; //1st = luitem index, 2nd = materialid
		private transient ArrayList<Triple<Integer, Integer, Integer>> blockComboMaterialList = null; //1st = seq index, 2nd = luitem index, 3rd = material
		//private long challanDate = Misc.getUndefInt();
		private ArrayList<Long> challanInfoList = null;
		
		transient private boolean markForExitOffWorkArea = false;
		
		public void getMemoryUsageNested(StringBuilder sb) {
			if (miningInfo != null) {
				sb.append(" miningInfo NOT NULL:");
				miningInfo.getMemoryUsageNested(sb);
			}
			if (cachedTripDataLU != null) {
				sb.append(" cachedTripDataLU NOT NULL:");
			}
			if (cachedIntermediateLUInfoExtract != null) {
				sb.append(" cachedIntermediateLUInfoExtract NOT NULL");
			}
			sb.append(" OtherMergedTripIdSz:").append(otherMergedTripId == null ? 0 : otherMergedTripId.size());
			sb.append(" opBelongingBean:").append(this.opBelongingBean == null ? "null" :"Not NULL");
			sb.append(" materialLUItemIndexListSz:").append(materialLUItemIndexList == null ? 0 : materialLUItemIndexList.size());
			sb.append(" blockComboMaterialListSz:").append(blockComboMaterialList == null ? 0 : blockComboMaterialList.size());
			sb.append(" challanInfoListSz:").append(challanInfoList == null ? 0 : challanInfoList.size());
		}
		public int getCachedPrevTripId() {
			return cachedPrevTripId;
		}

		public void setCachedPrevTripId(int cachedPrevTripId) {
			this.cachedPrevTripId = cachedPrevTripId;
		}

		public int getCachedLUOfTripId() {
			return cachedLUOfTripId;
		}

		public void setCachedLUOfTripId(int cachedLUOfTripId) {
			this.cachedLUOfTripId = cachedLUOfTripId;
		}

		public AddnlMiningInfo getMiningInfo() {
			return miningInfo;
		}

		public void setMiningInfo(AddnlMiningInfo miningInfo) {
			this.miningInfo = miningInfo;
		}
	}
	public static class TempShovelInfo {
		public int shovelId;
		public byte shovelCycles;
		public byte loadQuality;
		public byte noEvent = 0;
		public byte notCompatible = 0;
		public byte dist = 0;
		public static TempShovelInfo add(ArrayList<TempShovelInfo> list, TempShovelInfo entry) {
			for (int i=0,is=list.size();i<is;i++) {
				if (list.get(i).shovelId == entry.shovelId) {
					return list.get(i);
				}
			}
			int addAt = 0;
			for (int is=list.size();addAt<is;addAt++) {
				TempShovelInfo le = list.get(addAt);
				if (le.loadQuality < entry.loadQuality || (le.loadQuality == entry.loadQuality && le.dist > entry.dist)) {
					break;
				}
			}
			if (addAt == list.size())
				list.add(entry);
			else
				list.add(addAt,entry);
			return entry;
		}
		public TempShovelInfo(int shovelId, byte loadQuality, byte dist) {
			this.loadQuality = loadQuality;
			this.dist = dist;
			this.shovelId = shovelId;
		}
		public static TempShovelInfo getEntry(ArrayList<TempShovelInfo> list, int shovelId) {
			for (int i=0,is=list == null ? 0 : list.size(); i<is;i++)
				if (list.get(i).shovelId == shovelId)
					return list.get(i);
			return null;
		}
		public void setAdditionalInfo(byte shovelCycles,  byte noEvent, byte notCompatible) {
			this.shovelCycles = shovelCycles;
			this.noEvent = noEvent;
			this.notCompatible = notCompatible;
		}
		
	}
	private AddnlInfo addnlInfo = null;
	
	private int opStationId;
	private int prefOrMovingOpStationId;//because of initialization from deserialization
	transient private LUInfoExtract luInfoExtract = null;
	//transient private boolean hackTransientInit = false;
	transient private byte luInfoExtractDirty = 0;//because of initialization issue set as NotDirty
	private byte guaranteedLoadType = -1; //0 = load, 1 = unload, 2 = hybrid or its variant, 3 pre load iM, 4 post load IM, 5  IM
	private byte guessLoadType = -1; //0 = load, 1 = unload, 2 = hybrid or its variant
	private boolean amBestSeqInBlock = false;
	private byte isStopOpStationType = 0; // bit 0 is not stop , bit 1 is if hybrid split ul , bit 2 if it has been guessed with non null op
	                                                                       // bit 3 if in RestArea
	                                                                      //bit 4 if in StopIgnore
	                                                                      //but 5 if restare is transit type
	private byte disposition = -1;//1: Load, 3: LU, 2: U, 4:UL
	
	private byte stopLoadType = -1; //-1 = unknown, 0 => load, 1 => unload

	private byte isChallanMarked = -1; //-1->ub=nknown, 1->marked, 0=not Marked
	private byte dalaStrikeEtc = 0; //bit 8765 = strikeCount, bit 1 if raised, bit 2 if in near shovel, bit 3 if strikeAfterDala, bit 4 of bleRead, 
	private byte shovelAssignQuality = 0; //bit 0123 ..> for quality, bit 4 whether in shovelSeq, bit 5 even if not in seq, nooverlap 
	transient public byte doOnlyShovelGet = 0;
	private byte dist5=0;
	private short startDalaRelStart = Misc.getUndefShort();
	private short endDalaRelStart = Misc.getUndefShort();
	private short startStrikeRelStart = Misc.getUndefShort();
	private short endStrikeRelStart = Misc.getUndefShort();
	private short loadStGpsStart = Misc.getUndefShort();
	private short firstLoadStGpsStart = Misc.getUndefShort();//loadStGpsStart is time when 2nd consecutive 0 seen,
	private transient GpsData loadStGpsPt = null;
	public static int MIN_FIRMED_PT_MILLI = 30*1000;
	public static final int MIN_GAP_BETWEEN_LOAD_GPS_AND_END_SECOND = 90;
	private byte shovelCycleCount = Misc.getUndefByte();
	
	transient public boolean needsUpdateOfEngineEventId = false;
    transient public BestShovelGetResult currResultOfShovelGet = null;
    transient private int prevShovelId = 0;
    private int manualAssignedShovelId = 0;
	transient public ArrayList<TempShovelInfo> tempShovelsSeenList = new ArrayList<TempShovelInfo>();
	public int getManualAssignedShovelId() {
		return manualAssignedShovelId;
	}
	public void setManualAssignedShovelId(int manualShovelAssignedId) {
		this.manualAssignedShovelId = manualShovelAssignedId;
	}
	public byte getDist5() {
		return dist5;
	}
	public void setDist5(byte dist5) {
		this.dist5 = dist5;
	}
	public void markForShovelGet(byte val) {
		this.doOnlyShovelGet = val;
	}
	public byte markedForShovelGet() {
		return this.doOnlyShovelGet;
	}
	public void getMemoryUsageNested(StringBuilder sb) {
		sb.append(" Items:").append(size());
		if (addnlInfo != null) {
			sb.append(" AddnlInfo not null:");
			addnlInfo.getMemoryUsageNested(sb);
		}
		sb.append(" loadStGpsPt:").append(loadStGpsPt == null ? "null" : "not null");
		sb.append(" currResultOfShovelGet:").append(currResultOfShovelGet == null ? "null" : "not null");
		sb.append(" tempShovelsSeenListSz:").append(tempShovelsSeenList == null ? 0 : tempShovelsSeenList.size());
	}
	public short getFirstLoadStGpsStart() {
		return firstLoadStGpsStart;
	}
	public void setFirstLoadStGpsStart(short firstLoadStGpsStart) {
		this.firstLoadStGpsStart = firstLoadStGpsStart;
	}

	public short getLoadStGpsStart() {
		return loadStGpsStart;
	}
	
	public GpsData getLoadStGpsStart(Connection conn, NewVehicleData vdp) {
		if (loadStGpsPt != null)
			return loadStGpsPt;
		short tsec = this.getLoadStGpsStart();
		if (Misc.isUndef(tsec)) {
			this.calcAndSetLoadStGpsStart(conn, vdp);
			tsec = this.getLoadStGpsStart();
		}
		if (Misc.isUndef(tsec))
			tsec = 0;
		LUItem stopStartItem = this.getStartItemExt();
		long ts = stopStartItem == null ? 0 : TripInfoConstants.getArtificialIgnoreTime(stopStartItem.getGpsDataRecordTime());
		GpsData retval = null;
		if (ts > 0 && tsec > 0) {
			retval = vdp.get(conn, new GpsData(ts+1000*tsec));
		}
		if (retval == null && stopStartItem != null)
			retval = stopStartItem.getGpsData(conn, vdp);
		this.loadStGpsPt = retval;
		return retval;
	}
	
	public void calcAndSetLoadStGpsStart(Connection conn, NewVehicleData vdp) {
		this.loadStGpsPt = null;
		this.loadStGpsStart = Misc.getUndefShort();
		this.firstLoadStGpsStart = Misc.getUndefShort();
		
		long endTime = TripInfoConstants.getArtificialIgnoreTime(this.getEndTimeSimple());
		long stTime = TripInfoConstants.getArtificialIgnoreTime(this.getStartTime());
		
		GpsData ref = new GpsData(endTime);
		double absStillThresh = 0.01;
		long loadingThresh = 70*1000;
		
		GpsData blkStartPt = null;
		GpsData blkEndPt = null;
		
		GpsData prev = vdp.get(conn, ref);
		GpsData loadPt = null;
		long loadGap = 0;
		GpsData prevShortValidGapThreshPt = null;
		GpsData latestShortValidGapThreshPt = null;
		blkEndPt = blkStartPt = prev;
		for (int i=-1;;i--) {
			GpsData pt = vdp.get(conn, ref, i);
			if (pt == null || pt.getGps_Record_Time() < stTime)
				break;
			if (prev == null) {
				prev = pt;
				continue;
			}
			double d=  pt.fastGeoDistance(prev);
			if (d > absStillThresh) {//just saw end of a movement block
				long gap = blkEndPt != null && blkStartPt != null ? (blkStartPt.getGps_Record_Time()-blkEndPt.getGps_Record_Time()) : 0;
				boolean gapValidShortThresh = gap >= 10*1000;
				boolean gapValidLoad = gap >= loadingThresh;
				if (gapValidLoad && loadGap < loadingThresh) {
					loadPt = blkEndPt;
					loadGap = gap;
				}
				else if (gapValidShortThresh && loadGap < loadingThresh && gap >= loadGap) {
					loadPt = blkEndPt;
					loadGap = gap;
				}
				if (gapValidShortThresh) {
					prevShortValidGapThreshPt = latestShortValidGapThreshPt;
					latestShortValidGapThreshPt = blkEndPt;
				}
				blkEndPt = blkStartPt = pt;
			}
			else {
				
				blkEndPt = pt;
			}
			prev = pt;
		}//for pts in seq
		long gap = blkEndPt != null && blkStartPt != null ? (blkStartPt.getGps_Record_Time()-blkEndPt.getGps_Record_Time()) : 0;
		boolean gapValidShortThresh = gap >= 10*1000;
		boolean gapValidLoad = gap >= loadingThresh;
		if (gapValidLoad && loadGap < loadingThresh) {
			loadPt = blkEndPt;
			loadGap = gap;
		}
		else if (gapValidShortThresh && loadGap < loadingThresh && gap >= loadGap) {
			loadPt = blkEndPt;
			loadGap = gap;
		}
		if (gapValidShortThresh) {
			prevShortValidGapThreshPt = latestShortValidGapThreshPt;
			latestShortValidGapThreshPt = blkEndPt;
		}
		if (latestShortValidGapThreshPt == null) {
			GpsData stRef = new GpsData(stTime);
			GpsData stPt1 = vdp.get(conn, stRef,1);
			if (stPt1 == null || stPt1.getGps_Record_Time() >= endTime) {
				stPt1 = vdp.get(conn, stRef);
			}
			latestShortValidGapThreshPt = stPt1;
		}
		if (prevShortValidGapThreshPt == null)
			prevShortValidGapThreshPt = latestShortValidGapThreshPt;
		if (loadPt == null) {
			loadPt = prevShortValidGapThreshPt;
		}
		this.loadStGpsPt = loadPt;
		gap = (loadPt.getGps_Record_Time()-stTime)/1000;
		this.loadStGpsStart = gap > Short.MAX_VALUE ? Short.MAX_VALUE : (short) gap;
		gap = (prevShortValidGapThreshPt.getGps_Record_Time()-stTime)/1000;
		this.firstLoadStGpsStart = gap > Short.MAX_VALUE ? Short.MAX_VALUE : (short) gap;
		if (false) {
			calcAndSetLoadStGpsStartOld(conn, vdp);
			this.getLoadStGpsStart(conn, vdp);
			if (this.loadStGpsPt == null) {
				int dbg =1;
				dbg++;
				calcAndSetLoadStGpsStartOld(conn, vdp);
			}
			if (this.loadStGpsPt.fastGeoDistance(loadPt) > 0.01) {
				int dbg = 1;
				dbg++;
			}
		}
	}
	
	public void calcAndSetLoadStGpsStartOld(Connection conn, NewVehicleData vdp) {
		this.loadStGpsPt = null;
		this.loadStGpsStart = Misc.getUndefShort();
		this.firstLoadStGpsStart = Misc.getUndefShort();
		
		LUItem stItem = this.getStartItemExt();
		long tsEnd = this.getEndTimeSimple();
		if (stItem == null)
			return;
		GpsData refPt = stItem.getGpsData(conn, vdp);
		int cntFWPointSeen = 0;
		GpsData fwPt = refPt;
		GpsData prevPt = null;
		GpsData firstFwPt = refPt;
		for (int i=0;;i++) {
			GpsData pt = vdp.get(conn, refPt, i);
			if (pt == null || pt.getGps_Record_Time() > tsEnd)
				break;
			if (prevPt != null && prevPt.isFWPoint() && pt.getGps_Record_Time()-prevPt.getGps_Record_Time() > MIN_FIRMED_PT_MILLI) {
				cntFWPointSeen++;
				fwPt = prevPt;
				if (cntFWPointSeen == 1)
					firstFwPt = fwPt;
			}
			
			prevPt = pt;
			if (cntFWPointSeen == 2)
				break;
			
		}
		//check sanity of 2nd Pt
		int sec2nd = (int)((fwPt.getGps_Record_Time()-TripInfoConstants.getArtificialIgnoreTime(stItem.getGpsDataRecordTime()))/1000);
		if (sec2nd > Short.MAX_VALUE)
			sec2nd = Short.MAX_VALUE;
		if (sec2nd < 0)
			sec2nd = 0;
		int sec1st = sec2nd;
		if (firstFwPt.getGps_Record_Time() != fwPt.getGps_Record_Time()) {
			sec1st = (int)((fwPt.getGps_Record_Time()-TripInfoConstants.getArtificialIgnoreTime(stItem.getGpsDataRecordTime()))/1000);
			if (sec1st > Short.MAX_VALUE)
				sec1st = Short.MAX_VALUE;
			if (sec1st < 0)
				sec1st = 0;
		}
		int secDur = (int)((tsEnd-refPt.getGps_Record_Time())/1000);
		if ((secDur-sec2nd) < LUSequence.MIN_GAP_BETWEEN_LOAD_GPS_AND_END_SECOND) {
			sec2nd = sec1st;
			if ((secDur-sec2nd) < LUSequence.MIN_GAP_BETWEEN_LOAD_GPS_AND_END_SECOND) {
				sec1st = sec2nd = 0;
			}
		}
		this.loadStGpsStart = (short)sec2nd;
		this.firstLoadStGpsStart = (short)sec1st;
	}
	
	
	public byte getShovelCycleCount() {
		return shovelCycleCount;
	}
	public void setShovelCycleCount(byte shovelCycleCount) {
		this.shovelCycleCount = shovelCycleCount;
	}
    
    public void setOpBelongingOnly(int opId) {
    	
    }
    public void clearOpBelongingReShovelSequence() {
    	this.setOpBelonging(Misc.getUndefInt(), null, true, true);
    	this.setPrefOrMovingOpStationId(Misc.getUndefInt());
    	this.setGuessedFromPreferred(false);
    }
    public void rememberIntoPrevShovelId() {
    	this.prevShovelId =  this.getOpBelogingBean().getLinkedVehicleId();
    }
    public int getPrevShovelId() {
    	return this.prevShovelId;
    }
    public void forceSetPrevShovelId(int prevShovelId) {
    	this.prevShovelId = prevShovelId;
    }
	public ArrayList<Pair<Integer, Integer>>  getMaterialLUItemIndexList() {
		return this.addnlInfo == null ? null : this.addnlInfo.materialLUItemIndexList;
	}
	public void setMaterialLUItemIndexList(ArrayList<Pair<Integer, Integer>> entry) {
		if (this.addnlInfo == null)
			this.addnlInfo = new AddnlInfo();
		this.addnlInfo.materialLUItemIndexList = entry;
	}
	
	public AddnlMiningInfo getMiningInfo() {
		return addnlInfo == null ? null : addnlInfo.getMiningInfo();
	}
	public void setMiningInfo(AddnlMiningInfo miningInfo) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.setMiningInfo(miningInfo);
	}
	public AddnlMiningInfo calculateAndSetMiningInfo(Connection conn, NewVehicleData vdp, HelperAssignment assignmentCache
			, ThreadContextCache threadContextCache, VehicleControlling vehicleControlling,CacheTrack.VehicleSetup vehSetup, int portNodeId) throws Exception {
		LUItem stopStart = null;
		if (this.isOpStationForStop()) {
			int stopStartIndex = getStopStartIndex();
			stopStart = get(stopStartIndex);
		}
		else {
			stopStart = this.getStartItemExt();
		}
		if (stopStart == null) {
			this.setMiningInfo(null);
			return null;
		}
		AddnlMiningInfo miningInfo = new AddnlMiningInfo();
		
		GpsData stopStartGpsData = stopStart.getGpsData(conn, vdp);
		if (stopStartGpsData == null)
			return null;
		Point point = stopStartGpsData.getPoint();
		OpStationBean moving = null;
		assignmentCache.update(conn, stopStartGpsData.getGps_Record_Time());
		ArrayList<Pair<Integer, Boolean>> allSitesWithLU = InvRTree.getAllSitesContaining(conn, point, stopStartGpsData.getGps_Record_Time(), assignmentCache.getNewmu(), 2);
		ArrayList<Integer> allSites = null;
		if (allSitesWithLU != null && allSitesWithLU.size() > 0) {
			allSites = new ArrayList<Integer>();
			for (Pair<Integer, Boolean> item : allSitesWithLU) {
				allSites.add(item.first);
				if (item.second)
					miningInfo.setHasLoad();
				else
					miningInfo.setHasUnload();
					
			}
		}
		ArrayList<ThreadContextCache.SimpleMoving> movingList = threadContextCache.getMovingOpStationContaining(conn, portNodeId, stopStartGpsData, vehicleControlling, vehSetup);
		ArrayList<Integer> allShovelsVehicleId = null;
		if (movingList != null && movingList.size() > 0) {
			allShovelsVehicleId = new ArrayList<Integer>();
			for (ThreadContextCache.SimpleMoving item : movingList) {
				if (item.getOpstationBean() != null) {
					allShovelsVehicleId.add(item.getOpstationBean().getLinkedVehicleId());
					miningInfo.setHasLoad();
				}
			}
		}
		
		miningInfo.setAllSites(allSites);
		miningInfo.setAllShovelsVehicleId(allShovelsVehicleId);
		miningInfo.setSiteId(assignmentCache.getLoadSite());
		miningInfo.setDestId(assignmentCache.getUnloadSite());
		miningInfo.setShovelsPerAssignment(assignmentCache.getShovelInfo());
		this.setMiningInfo(miningInfo);
		return miningInfo;
	}
	
	
	public void mergeTripRelatedInfo(LUSequence rhs) {//20140712_change
		this.setTripId(rhs.getTripId(), rhs.getRightTripId(), false, rhs.getCachedTripDataLU(), rhs.getCachedNextTripId(), rhs.getCachedPrevTripId(), rhs.getCachedLUOfTripId());
	}
	public void clearCachedGps() {
		if (this.isOpStationForStop())
			return; // just for the sake of efficiency - we dont ever getGpsData for stop or dir 
		int stIndex = getThisOrNextUsefulIndex(0);
		for (int i=0,is=size();i<is;i++) {
			LUItem item = get(i);
			if ( i != stIndex && !item.isStopStartExt())
				item.clearCachedGpsData();
		}
	}
	
	public boolean isLoadOrPreLGuaranteed() {
		//initTransient();
		return guaranteedLoadType == 0 || guaranteedLoadType == 3 ;
	}
	public boolean isLoadOrPreLGuess() {
		//initTransient();
		return guessLoadType == 0 || guessLoadType == 3;
	}
	public boolean isUnloadOrPreUGuaranteed() {
		//initTransient();
		return guaranteedLoadType == 1 || guaranteedLoadType == 4;
	}
	public boolean isUnloadOrPreUGuess() {
		//initTransient();
		return guessLoadType == 1 || guessLoadType == 4;
	}
	public boolean isUnknownLUGuaranteed() {
		//initTransient();
		return guaranteedLoadType < 0 || guaranteedLoadType == 2;
	}
	
	public boolean isUnknownLUGuess() {
		//initTransient();
		return guessLoadType < 0 || guessLoadType == 2;
	}
	
	public boolean isIntermediatePreL() {
		//initTransient();
		return guaranteedLoadType == 3;
	}
	public boolean isIntermediatePreLGuess() {
		//initTransient();
		return guessLoadType == 3;
	}
	public boolean isIntermediatePreU() {
		//initTransient();
		return guaranteedLoadType == 4;
	}
	public boolean isIntermediatePreUGuess() {
		//initTransient();
		return guessLoadType == 4;
	}
	public boolean isIntermediateIM() {
		//initTransient();
		return guaranteedLoadType == 5;
	}
	public boolean isIntermediateIMGuess() {
		//initTransient();
		return guessLoadType == 5;
	}
	public boolean isIntermediate() {
		return guaranteedLoadType == 5 || guaranteedLoadType == 4 || guaranteedLoadType == 3;
	}
	public boolean isIntermediateGuess() {
		return guessLoadType == 5 || guessLoadType == 4 || guessLoadType == 3;
	}
	public int getShovelId() {
		OpStationBean opBelonging = this.getOpBelogingBean();
		return opBelonging == null ? Misc.getUndefInt() : opBelonging.getLinkedVehicleId();
	}
	public void setOpBelonging(int opStationId, byte guaranteedLoadType, byte guessLoadType, AddnlMiningInfo miningInfo, boolean toSetMiningInfoIfNull) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.opBelonging = opStationId;
		OpStationBean bean = TripInfoCacheHelper.getOpStation(opStationId);
		addnlInfo.opBelongingBean = bean;
		
		if (this.luInfoExtract != null)
			luInfoExtract.setOfOpStationId(opStationId);
		if (miningInfo != null || toSetMiningInfoIfNull)
			addnlInfo.miningInfo = miningInfo;
		
		this.guaranteedLoadType = guaranteedLoadType;
		
		this.guessLoadType = guessLoadType;
		if (guaranteedLoadType !=2 && guaranteedLoadType != -1)
			this.guessLoadType = guaranteedLoadType;
	}
	
	public void setOpBelonging(int opStationId, AddnlMiningInfo miningInfo, boolean toSetMiningIfNull, boolean dontChangeLoadType) {
		if (Misc.isUndef(opStationId) && addnlInfo == null)
			return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.opBelonging = opStationId;
		OpStationBean bean = TripInfoCacheHelper.getOpStation(opStationId);
		addnlInfo.opBelongingBean = bean;
		if (!dontChangeLoadType) {
			guaranteedLoadType = -1;
			guessLoadType = -1;
		}
		if (miningInfo != null || toSetMiningIfNull)
			addnlInfo.miningInfo = miningInfo;
	}
	public int getOpBelonging() {
		if (addnlInfo == null)
			return Misc.getUndefInt();
		//initTransient();
		return addnlInfo.opBelonging;
	}
	public OpStationBean getOpBelogingBean() {
		if (addnlInfo == null)
			return getOpStation();
		//initTransient();
		if (addnlInfo.opBelonging <= 0)
			return getOpStation();
		OpStationBean retval = addnlInfo.opBelongingBean;
		if (retval != null)
			return retval;
		if (addnlInfo.opBelonging == opStationId)
			retval = addnlInfo.opBelongingBean = this.getOpStation();
		if (retval == null && !Misc.isUndef(addnlInfo.opBelonging))
			retval  = addnlInfo.opBelongingBean = TripInfoCacheHelper.getOpStation(addnlInfo.opBelonging);
		if (retval == null)
			return getOpStation();
		return retval;
	}
	public void setOpStationId(int opStationId) {
		int prevOpstationId = this.opStationId;
		
		this.opStationId = opStationId;
		if (prevOpstationId != opStationId)
			setOpBelonging(Misc.getUndefInt(), null, true, false);
		if (this.luInfoExtract != null)
			luInfoExtract.setOfOpStationId(opStationId);
	}
	public boolean isGuessedFromArtOp() {
		LUInfoExtract ext = this.getLUInfoExtractPlain();
		return ext != null && !Misc.isUndef(ext.getArtOpStationId()) && ext.getArtOpStationMatch() > 0;
	}
	
	public boolean isGuessedFromPreferred() {
		return (this.isStopOpStationType & 0x4) != 0;
	}
	public void setGuessedFromPreferred(boolean isPreferred) {
		if (isPreferred)
			this.isStopOpStationType = (byte) (this.isStopOpStationType | 0x4);
		else
			this.isStopOpStationType = (byte)(this.isStopOpStationType & 0xFB);
	}
	public void setRestAreaTransitType(boolean val) {
		if (val)
			this.isStopOpStationType |= 0x010;
		else
			this.isStopOpStationType &= ~(0x10);
	}
	public boolean isRestAreaTransitType() {
		return (this.isStopOpStationType & 0x10) != 0;
	}
	public boolean isRestAreaType() {
		return (this.isStopOpStationType & 0x8) != 0;
	}
	public boolean isIgnoreAreaType() {
		return (this.isStopOpStationType & 0x10) != 0;
	}
	
	
	
	public int getStrikeCount() {
		int strikeCount = 0x0F0 & this.dalaStrikeEtc;
		strikeCount = strikeCount >> 4;
		return strikeCount;
	}
	public static short intToShortSec(int v) {
		return Misc.isUndef(v) ? Misc.getUndefShort() : v < Short.MIN_VALUE ? -1000 : v > Short.MAX_VALUE ? 1000 : (short) v;
	}
	public void setStrikeCount(int v, int startStrikeSecRelStart, int endStrikeSecRelStart, boolean strikeAfterDala) {
	//public void setStrikeCount(int v) {
		if (v > 15)
			v = 15;
		v = v << 4;
		
		this.startStrikeRelStart = intToShortSec(startStrikeSecRelStart);
		this.endStrikeRelStart = intToShortSec(endStrikeSecRelStart);
		this.dalaStrikeEtc |= v;
		this.setStrikeAfterDalaUp(strikeAfterDala);
	}
	public boolean isDala() {
		return (0x01 & dalaStrikeEtc) != 0;
	}
	public void setDalaRaise(boolean set) {
		if (set)
			this.dalaStrikeEtc |= 0x01;
		else
			this.dalaStrikeEtc &= 0xFE;
	}
	public boolean isNearShovel() {
		return (0x02 & dalaStrikeEtc) != 0;
	}
	public void setNearShovel(boolean set) {
		if (set)
			this.dalaStrikeEtc |= 0x02;
		else
			this.dalaStrikeEtc &= 0xFD;
	}
	public boolean isStrikeAfterDalaUp() {
		return (0x04 & dalaStrikeEtc) != 0;
	}
	public void setStrikeAfterDalaUp(boolean set) {
		if (set)
			this.dalaStrikeEtc |= 0x04;
		else
			this.dalaStrikeEtc &= 0xFB;
	}
	public boolean isBLERead() {
		return (0x08 & dalaStrikeEtc) != 0;
	}
	public void setBLERead(boolean set) {
		if (set)
			this.dalaStrikeEtc |= 0x08;
		else
			this.dalaStrikeEtc &= 0xF7;
	}
	
	public String getDalaStrikeCoding() {
		StringBuilder sb = new StringBuilder();
		sb.append("StrikeCount:").append(getStrikeCount())
		.append(" Raise:").append(isDala() ? 1 : 0)
		.append(" NearShovel:").append(isNearShovel() ? 1 : 0)
		.append(" StrikeAftDala:").append(this.isStrikeAfterDalaUp() ? 1 : 0)
		.append(" BleRead:").append(isBLERead() ? 1 : 0)
		//.append(" ShovelLoad:").append(isShovelLoad() ? 1 : 0)
		;
		return sb.toString();
	}
	
	public void setRestAreaType(boolean isPreferred) {
		if (isPreferred)
			this.isStopOpStationType = (byte) (this.isStopOpStationType | 0x8);
		else
			this.isStopOpStationType = (byte)(this.isStopOpStationType & 0xF7);
	}
	public void setIgnoreAreaType(boolean isPreferred) {
		if (isPreferred) {
			this.isStopOpStationType = (byte) (this.isStopOpStationType | 0x10);
			if (this.luInfoExtract != null)
				this.luInfoExtract.setOpComplete(false);
		}
		else
			this.isStopOpStationType = (byte)(this.isStopOpStationType & 0xEF);
	}
	
	public void setAsStopOpStationType(int opstationId, boolean setrecalcAll, long dontTouchBefore) {
		if (this.size() > 0 && this.get(0).getGpsDataRecordTime() <= dontTouchBefore)
			return;
		boolean oldIsStopHybridSplit = (this.isStopOpStationType & 0x2) != 0;
		boolean oldIsStop = (this.isStopOpStationType & 0x1) != 0;
		int oldOpStationId = this.opStationId;
		if (oldOpStationId == opstationId && oldIsStop)
			setrecalcAll = false;
		this.isStopOpStationType = (byte) (isStopOpStationType | 0x1);
		this.opStationId = opstationId;
		if (luInfoExtract != null)
			luInfoExtract.setOfOpStationId(opstationId);
		if (setrecalcAll) {
			this.setOpBelonging(Misc.getUndefInt(),(byte) -1, (byte)-1, null, true);
			this.setLUInfoReextractDirty(dontTouchBefore);
		}
		else {
			this.setOpBelonging(opstationId,this.getGuaranteedLoadType(), this.getGuessLoadType(), null, false);
		}
	}
	public void resetAsStopOpStationType(long dontTouchBefore) {
		if (this.size() > 0 && this.get(0).getGpsDataRecordTime() <= dontTouchBefore)
			return;
		this.isStopOpStationType = (byte) (isStopOpStationType &  0xFE);
		this.setOpBelonging(Misc.getUndefInt(),(byte) -1, (byte)-1, null, true);
		this.setLUInfoReextractDirty(dontTouchBefore);
	}
	public int getStopLoadType() {
		return stopLoadType;
	}
	public void setStopLoadType(int loadType, StopDirControl stopDirControl) {
		if (stopDirControl == null || loadType != -1) {
			stopLoadType = (byte) loadType;
			return;
		}
		
		int allowedLoadTypes = 1;//stopDirControl.allowedLoadTypes();
		if (allowedLoadTypes != -1)
			stopLoadType = (byte) allowedLoadTypes;
		else {//TODO_STOP ... need to guess based upon area
		}
	}
	// handleRecordIntermediate

	public String getRegionName(int regionId){
		try {
		return RegionTest.getRegionInfo(regionId, null) != null ? RegionTest.getRegionInfo(regionId, null).region.m_name : "(null)";
		}
		catch (Exception e) {
			return "region:"+regionId;
		}
	}
	public String toString() {
		// return "[OpId:"+opStationId+",TripId:"+tripId+",Dirty:"+(toReextractLUInfo ? "T" : "F")+",Extract:"+(luInfoExtract == null ?
		// "n/a":luInfoExtract.toString())+"Seq:"+super.toString()+"]";
		StringBuilder sb = new StringBuilder("");
		LUInfoExtract ext = this.getLUInfoExtractPlain();
		long gin = ext == null ? 0 : ext.getGateIn();
		long gout = ext == null ? 0 : ext.getGateOut();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		String gind = gin > 0 ? sdf.format(new java.util.Date(gin)) : "";
		String goutd = gout > 0 ? sdf.format(new java.util.Date(gout)) : "";
		OpStationBean prefBean = TripInfoCacheHelper.getOpStation(this.getPrefOrMovingOpStationId());
		
		sb.append("LUSequence hashcode $$ [ ").append(this.hashCode()).append(" ] $$ ").append(isLoad()? "[L]" : "[U]");
		sb.append("[GOpName:").append(this.getOpBelogingBean()).append(" , Guarantee:").append(this.guaranteedLoadType).append(" ,Guess:").append(this.guessLoadType).append("]");
		sb.append("\n[OpName:").append(TripInfoCacheHelper.getOpStation(opStationId)).append(",pref:").append(prefBean == null ? "null" : prefBean.getOpStationName()).append(",isComplete:").append( luInfoExtract == null ? "Undef " : luInfoExtract.isOpComplete())
		.append(",isBest:").append(this.amBestSeqInBlock)
		.append(",LTripId:").append(getTripId() == Misc.getUndefInt() ? "Undef" : getTripId())
		.append(",RTripId:").append(getRightTripId() == Misc.getUndefInt() ? "Undef" : getRightTripId());
		sb.append(",Gin:").append(gind).append(",Gout:").append(goutd).append("Dir:").append(ext == null ? 0 : ext.getDirChanged())
		.append("Challan:").append(getChallanInfoList() == null ? "null":(new java.util.Date(getChallanInfoList().get(0))).toString()).append(", StartTime : ").append(getStartTime()).append(", EndTime : ").append(getEndTime())
		.append(",LUId:").append(getIntermediateLUId() == Misc.getUndefInt() ? "Undef" : getIntermediateLUId())
		.append(",").append(this.getDalaStrikeCoding())
		.append(",isRest:").append(this.isRestAreaType())
		.append(",isIgnore:").append(isIgnoreAreaType())
		.append(",isDala:").append(isDala())
		.append(",strikeCount:").append(getStrikeCount())
		.append(",shovelQ:").append(this.getShovelAssignQuality())
		.append(",InSeq:").append(this.isInShovelSeq())
		.append(",ProperQ:").append(this.isQualityIsProper())
		.append(",\n\rSeq:");
		
		// sb.append(super.toString());
		for (int i = 0, is = size(); i < is; i++) {
			LUItem item = get(i);
			if (i != 0)
				sb.append("\r\n");
			sb.append(i).append("  ").append(item.toString());
		}
		sb.append("]");
		return sb.toString();
	}
	//public void initTransient() {
	//	if (!this.hackTransientInit) {
	//		hackTransientInit = true;
	//		this.setCachedNextTripId(Misc.getUndefInt());
			//guessedSimpleOpType = -1;
			//opBelonging = Misc.getUndefInt();
			//guaranteedLoadType = -1; //0 = load, 1 = unload, 2 = hybrid or its variant, 3 pre load iM, 4 post load IM, 5  IM
			//guessLoadType = -1; //0 = load, 1 = unload, 2 = hybrid or its variant
	//	}
	//}
	public LUSequence() {
		super();
		this.opStationId = Misc.getUndefInt();
		//initTransient();
		
	}
	public LUSequence(int opStationId) {
		super();
		this.opStationId = opStationId;
		//initTransient();
	}

	public LUSequence(int opStationId, LUItem luItem) {
		super();
		this.opStationId = opStationId;
		//initTransient();
		add(luItem);
	}
	public LUSequence( LUItem luItem) {
		super();
		this.opStationId = Misc.getUndefInt();
		//initTransient();
		add(luItem);
	}
	
	public int getTripId() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.tripId;
	}

	public void setTripId(int leftTripId) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.tripId = leftTripId;
	}

	public int getRightTripId() {
		if (addnlInfo == null)
			return Misc.getUndefInt();
		if (Misc.isUndef(addnlInfo.rightTripId)) {
			addnlInfo.rightTripId = addnlInfo.tripId;
		}
		return addnlInfo.rightTripId;
	}

	public void setRightTripId(int rightTripId) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.rightTripId = rightTripId;
	}

	public int getIntermediateLUId() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.intermediateLUId;
	}

	public void setIntermediateLUId(int luId, LUInfoExtract cached, int intermediateParentTripId) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.intermediateLUId = luId;
		if (addnlInfo.cachedIntermediateLUInfoExtract == null)
			addnlInfo.cachedIntermediateLUInfoExtract = new LUInfoExtract();
		addnlInfo.cachedIntermediateLUInfoExtract.copy(cached);
		addnlInfo.intermediateParentTripId = intermediateParentTripId;
	}

	public int getCachedNextTripId() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.cachedNextTripId;
	}
	public void setCachedNextTripId(int dt) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.cachedNextTripId = dt;
	}

	public int getCachedPrevTripId() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.cachedPrevTripId;
	}
	public void setCachedPrevTripId(int dt) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.cachedPrevTripId = dt;
	}
	public int getCachedLUOfTripId() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.getCachedLUOfTripId();
	}

	public LUInfoExtract getCachedTripDataLU() {
		return addnlInfo == null ? null : addnlInfo.cachedTripDataLU;
	}

	public void setCachedTripDataLU(LUInfoExtract data, int ofTripId) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.cachedTripDataLU = data;
		addnlInfo.cachedLUOfTripId = ofTripId;
	}

	public void setCachedDirty() {
		if (addnlInfo == null)
			return;
		addnlInfo.cachedIntermediateLUInfoExtract = null;
		addnlInfo.cachedNextTripId = Misc.getUndefInt();
		addnlInfo.cachedTripDataLU = null;
		addnlInfo.cachedLUOfTripId = Misc.getUndefInt();
	}
	public void setTripId(int leftTripId, int rightTripId, boolean tripCreatedNew, LUInfoExtract cachedTripDataLU, int cachedNextTripId, int cachedPrevTripId, int cachedOfTripId) {
		this.setTripId(leftTripId);
		this.setRightTripId(rightTripId);
		//2014_06_20	this.tripCreatedNew = tripCreatedNew;
		this.setCachedTripDataLU(cachedTripDataLU, cachedOfTripId);
		this.setCachedNextTripId(cachedNextTripId);
		this.setCachedPrevTripId(cachedPrevTripId);
		// to associate corresponding trip with LUInfoExtract
		if (addnlInfo != null && addnlInfo.cachedTripDataLU != null)
			 addnlInfo.cachedTripDataLU.setTripId(leftTripId);
	}
	public void setTripId(int leftTripId, int rightTripId) {
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.tripId = leftTripId;
		addnlInfo.rightTripId = rightTripId;
		if (addnlInfo.cachedTripDataLU != null)
			addnlInfo.cachedTripDataLU.setTripId(leftTripId);
	}

	//2014_06_20	public boolean isTripCreatedNew() {
	//2014_06_20		return tripCreatedNew;
	//2014_06_20	}

	public void resetTripId() {
		if (addnlInfo != null) {
			addnlInfo.tripId = Misc.getUndefInt();
			addnlInfo.rightTripId = Misc.getUndefInt(); 
			//2014_06_20		tripCreatedNew = false;
			addnlInfo.cachedTripDataLU = null;
			addnlInfo.cachedLUOfTripId = Misc.getUndefInt();
			addnlInfo.cachedNextTripId = Misc.getUndefInt();
			addnlInfo.cachedIntermediateLUInfoExtract = null;
		}
	}
	public void forceSetLUInfoExtract(ExtLUInfoExtract ext) {
		this.luInfoExtract = ext;
		this.luInfoExtractDirty = 0;
	}
	public void setLUInfoReextractDirty(long dontTouchBefore) {
		if (this.size() > 0 && this.get(0).getGpsDataRecordTime() <= dontTouchBefore)
			return;
		//this.markForShovelGet(false);
		this.loadStGpsPt = null;
		this.loadStGpsStart = Misc.getUndefShort();

		this.luInfoExtractDirty = 1;
		//luInfoExtract = null;
		if (addnlInfo != null)
			addnlInfo.materialLUItemIndexList = null;
	}

	public boolean isLUInfoReextractDirty() {
		return this.luInfoExtractDirty == 1 || luInfoExtract == null;
		//return luInfoExtract == null;
	}
	public void resetLUInfoReextractDirty() {
		this.luInfoExtractDirty = 0;
	}
	
	private int helpGetBegOfWB(int index) {
		LUItem item = get(index);
		int itemRegionId = item.getRegionId();
		int breakOutIndex = index;
		for (int i = index - 1; i >= 0; i--) {
			LUItem next = get(i);
			if (next.isNonRegionEvent())
				continue;
			if (!next.isAreaIn() && !next.isAreaOut()) {
				break;
			}
			if (next.isAreaIn() && next.getRegionId() != itemRegionId) {
				break;
			}
			if (next.isAreaIn()) {
				breakOutIndex = i;
				continue;
			}
		}
		return breakOutIndex;
	}

	private int helpGetEndOfWB(int index, int seqSize) {
		LUItem item = get(index);
		int itemRegionId = item.getRegionId();
		int breakOutIndex = -1;
		for (index++; index < seqSize; index++) {
			LUItem next = get(index);
			if (next.isNonRegionEvent())
				continue;
			if (!next.isAreaIn() && !next.isAreaOut()) {
				break;
			}
			if (next.isAreaOut()) {
				breakOutIndex = index;
				continue;
			}
			if (next.isAreaIn() && next.getRegionId() != itemRegionId) {
				break;
			}
			if (next.isAreaIn() && (next.getRegionId() == itemRegionId)) {
				breakOutIndex = -1;
				continue;
			}
		}
		if (breakOutIndex != -1)
			index = breakOutIndex;
		return index;
	}

	private boolean helpIsProperWB(OpStationBean ops, int index, int seqSize, long end, int wbEndIndex) {
		LUItem item = get(index);
		if (false && !item.hasStopped()) // TO_VERIFY IF WORKS
			return false;
		int j = wbEndIndex;
		long gap = j == seqSize ? Misc.isUndef(end) ? 1000000 : end - item.getTime() : get(j).getTime()
				- get(index).getTime();
		return gap >= item.getThreshold(ops);
	}

	private int[] getPossibleWB2(OpStationBean ops, long end) {// same size as LU Sequence.
		// Final WB2 maybe different from possible found here if after final areaOut there are only one WB2
		// Will get the index of WB2TypeMatch (or AnyWB) that could meet match requirement of WB2 after index i s.t there is a WB after this index
		int seqSize = size();
		int[] retval = new int[seqSize];
		int latestWB2 = seqSize;
		boolean latestWasWB2Type = false;

		boolean seenWB = false;
		for (int i = seqSize - 1; i >= 0; i--) {
			LUItem item = get(i);
			retval[i] = latestWB2;
			if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
				int tempWBBeg = helpGetBegOfWB(i);
				if (tempWBBeg != i) {
					for (int j = i; j >= tempWBBeg; j--) {
						retval[j] = latestWB2;
					}
					i = tempWBBeg;
				}
				int tempWBEnd = helpGetEndOfWB(i, seqSize);
				if (helpIsProperWB(ops, i, seqSize, end, tempWBEnd)) {
					boolean isWB2 = item.isWB2();
					if (isWB2)
						seenWB = true;
					if (seenWB) {
						if (isWB2) {
							latestWB2 = i;
							latestWasWB2Type = true;
						} else if (!latestWasWB2Type) {
							latestWB2 = i;
						}
					}
					seenWB = true;
				}
			}
		}
		return retval;
	}

	private int helperGetLatestMatchingWB2Index(OpStationBean ops, int wb1Index, int wb1Id, long end) {
		int seqSize = size();
		for (int i = seqSize - 1; i > wb1Index; i--) {
			LUItem item = get(i);
			if (item.isAreaIn() && item.isWB() && !item.isInsideWait() && item.getRegionId() == wb1Id) {
				i = helpGetBegOfWB(i);
				int tempWBEnd = helpGetEndOfWB(i, seqSize);
				if (helpIsProperWB(ops, i, seqSize, end, tempWBEnd)) {
					return i;
				}
			}
		}
		return seqSize;
	}

	private ArrayList<Pair<Integer, Integer>> getMaximalMatchingWB1WB2(OpStationBean ops, long end) {
		// same size as size()
		// entry at pos x tells the nearest WB at left s.t matching WB at the end is farthest (nearest from RHS) with the following twist

		// WB entries of type WB1 will be given preference over entries of non-WB1 type

		int seqSize = size();
		ArrayList<Pair<Integer, Integer>> retval = new ArrayList<Pair<Integer, Integer>>();

		boolean latestWasWB1Type = false;
		Pair<Integer, Integer> latest = null;
		int latestIndex = -1;
		int latestId = -1;
		for (int i = 0; i < seqSize; i++) {
			LUItem item = get(i);
			retval.add(latest);
			if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
				int tempWBEnd = helpGetEndOfWB(i, seqSize);
				for (int j = i + 1; j < tempWBEnd; j++)
					retval.add(latest);
				if (helpIsProperWB(ops, i, seqSize, end, tempWBEnd)) {
					boolean isWB1 = item.isWB1();
					int currId = item.getRegionId();
					if (isWB1) {
						latestIndex = i;
					} else if (!latestWasWB1Type) {
						latestIndex = i;
					}
					if (latest == null || latestIndex != latest.first) {// may need to create new one
						int matchIndex = latest == null || latestId != currId ? this.helperGetLatestMatchingWB2Index(ops, latestIndex, currId, end) : latest.second;
						if (matchIndex <= latestIndex)
							matchIndex = seqSize;
						// we want to create new one if there was none, or first isWB1 type or if same matchIndex then latestIndex (when currId == latestId)
						// or if previous's matchIndex was non-existent (= seqSize) or the currentMatchIndex is to right or previous one)
						if (latest == null || (!latestWasWB1Type && isWB1) // means we are seeing the first WB1 type ... use it
								|| (latest.second == seqSize) || (matchIndex != seqSize && matchIndex > latest.second) // meaning for the currWB1, the latestWB1 is after previous
								// one ... so switchover to new late
								|| (latestId == currId)) {
							latest = new Pair<Integer, Integer>(latestIndex, matchIndex);
							if (isWB1)
								latestWasWB1Type = true;
							latestId = currId;
						}
					}
				}
				i = tempWBEnd - 1; // because incremented at end of loop
			}
		}
		return retval;
	}

	private void helpPopulateWB(OpStationBean ops, LUInfoExtract curr, int ainIndex, int aoutIndex, int[] possibleWB2, ArrayList<Pair<Integer, Integer>> matchwb1wb2Pairs) {
		int seqSize = size();
		int wb2Index = seqSize;
		int wb1Index = -1;
		int wb3Index = -1;
		try {
			if (curr != null && ainIndex >= 0 && !Misc.isUndef(curr.getAreaIn())) {
	
				// 1. populate WB1 by going backward from ainIndex
				// 2. populate WB3 by going backward from end
				// 3. populate WB2 by looking at possibleWB2 and using WB1
				// 1. populating WB1
				Pair<Integer, Integer> wb1wb2 = matchwb1wb2Pairs.get(ainIndex);
				wb1Index = wb1wb2 != null ? wb1wb2.first : -1;
				if (wb1Index != -1 && wb1wb2.second != -1 && wb1wb2.second != seqSize && aoutIndex != -1 && aoutIndex != seqSize) {
					// because of the way maximal are found, following can happen
					// w(x) .. w(y) Ain Aout w(y) .. w(x)
					// the maximal pair at Ain will be w(x)..w(x) and not w(y)..w(y) ... while the logic of tightest, while matching will require us to choose
					// w(y)..w(y)
					// therefore we still have to loop and collect
					int currWb2Max = wb1wb2.second;
					int possWB1 = -1;
					int possWB2 = -1;
					for (int i = ainIndex - 1; i >= wb1Index; i--) {
						LUItem item = get(i);
						int itemRegionId = item.getRegionId();
						if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
							i = helpGetBegOfWB(i);
							int tempWBEnd = helpGetEndOfWB(i, seqSize);
							if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
	
								int matchIndex = -1;
								for (int j = aoutIndex; j <= currWb2Max; j++) {
									LUItem item2 = get(j);
									if (itemRegionId == item2.getRegionId() && item2.isAreaIn() && item2.isWB() && !item2.isInsideWait()) {
										int tempWBEnd2 = helpGetEndOfWB(j, seqSize);
										if (helpIsProperWB(ops, j, seqSize, curr.getGateOut(), tempWBEnd2)) {
											matchIndex = j;
											break;
										}
									}
								}
	
								if (matchIndex != -1) {
									boolean isWB1 = item.isWB1();
									if (isWB1 || possWB1 < 0) {
										possWB1 = i;
										possWB2 = matchIndex;
									}
									if (isWB1)
										break;
								}
							}
						}
					}
					if (possWB1 >= 0) {
						wb1Index = possWB1;
						wb2Index = possWB2;
					}
				}
				if (wb1Index == -1) {
					for (int i = ainIndex - 1; i >= 0; i--) {
						LUItem item = get(i);
						if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
							i = helpGetBegOfWB(i);
							int tempWBEnd = helpGetEndOfWB(i, seqSize);
							if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
								boolean isWB1 = item.isWB1();
								if (wb1Index == -1 || wb1Index == seqSize || isWB1)
									wb1Index = i;
								if (isWB1)
									break;
							}
						}
					}
				}
				int wb1Id = wb1Index >= 0 ? get(wb1Index).getRegionId() : -1;
				// now get WB2
	
				if (aoutIndex != -1 && aoutIndex != seqSize) {
					if (wb2Index == -1 || wb2Index == seqSize) {
						int wb2MaxPos = wb1wb2 == null ? seqSize : wb1wb2.second;
						if (wb2MaxPos == seqSize)
							wb2MaxPos--;
	
						int firstPossibleWb2 = -1;
						boolean seenWb2Type = false;
						for (int i = aoutIndex; i <= wb2MaxPos; i++) {
							LUItem item = get(i);
							if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
								int tempWBEnd = helpGetEndOfWB(i, seqSize);
								if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
									if (item.getRegionId() == wb1Id) {
										wb2Index = i;
										break;
									}
									boolean isWB2 = item.isWB2();
									if ((isWB2 && !seenWb2Type) || firstPossibleWb2 == -1) {
										firstPossibleWb2 = i;
										if (isWB2)
											seenWb2Type = true;
									}
								}
							}
						}
						if (wb2Index == -1 || wb2Index == seqSize)
							wb2Index = firstPossibleWb2;
					}
	
					// get WB3
					for (int i = seqSize - 1, is = wb2Index >= aoutIndex ? wb2Index : aoutIndex - 1; i > is; i--) {
						LUItem item = get(i);
						if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
							i = helpGetBegOfWB(i);
							if (i <= is)
								break;
							int tempWBEnd = helpGetEndOfWB(i, seqSize);
							if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
								boolean isWB3 = item.isWB3();
								if (wb3Index == seqSize || wb3Index == -1 || isWB3)
									wb3Index = i;
								if (isWB3)
									break;
							}
						}
					}// WB3
				} // if aout has been defined
			}// if ain has been defined
			else {
				// we will set WB2 to as late as possible and then use that to estimate WB1 etc
	
				for (int i = seqSize - 1; i >= 0; i--) {
					if (possibleWB2[i] != seqSize) {
						wb2Index = possibleWB2[i];
						break;
					}
				}
	
				if (wb2Index == seqSize || wb2Index == -1) { // 1 or 0 wb
					for (int i = 0; i < seqSize; i++) {
						LUItem item = get(i);
						if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
							int tempWBEnd = helpGetEndOfWB(i, seqSize);
							if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
								boolean isWB2 = item.isWB2();
								if (wb2Index == seqSize || isWB2)
									wb2Index = i;
								if (isWB2) {
									break;
								}
							}
						}
					}
				}
				// we will set WB1 to as soon as possible, but different from WB2
				for (int i = 0; i < wb2Index; i++) {
					LUItem item = get(i);
					if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
						int tempWBEnd = helpGetEndOfWB(i, seqSize);
						if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
							boolean isWB1 = item.isWB1();
							if (wb1Index == -1 || isWB1)
								wb1Index = i;
							if (isWB1)
								break;
						}
					}
				}
				for (int i = seqSize - 1, is = wb2Index >= aoutIndex ? wb2Index : aoutIndex - 1; i > is; i--) {
					LUItem item = get(i);
					if (item.isAreaIn() && item.isWB() && !item.isInsideWait()) {
						i = helpGetBegOfWB(i);
						if (i <= is)
							break;
						int tempWBEnd = helpGetEndOfWB(i, seqSize);
						if (helpIsProperWB(ops, i, seqSize, curr.getGateOut(), tempWBEnd)) {
							boolean isWB3 = item.isWB3();
							if (wb3Index == -1 || isWB3)
								wb3Index = i;
							if (isWB3)
								break;
						}
					}
				}
			}// no ain
			if (wb1Index != -1 && wb1Index != seqSize) {
				LUItem item = get(wb1Index);
				curr.setWb1Id(item.getRegionId());
				curr.setWb1In(item.getTime());
				//CHANGE13 curr.refWb1InEvent = item.getGpsData();
				//2014-06-20 curr.wb1IsProper = item.isWB1();
			}
			if (wb2Index != -1 && wb2Index != seqSize) {
				LUItem item = get(wb2Index);
				curr.setWb2Id(item.getRegionId());
				curr.setWb2In(item.getTime());
				//CHANGE13 curr.refWb2InEvent = item.getGpsData();
				//2014-06-20 curr.wb2IsProper = item.isWB2();
			}
			if (wb3Index != -1 && wb3Index != seqSize) {
				LUItem item = get(wb3Index);
				curr.setWb3Id(item.getRegionId());
				curr.setWb3In(item.getTime());
				//CHANGE13 curr.refWb3InEvent = item.getGpsData();
				//2014-06-20 curr.wb3IsProper = item.isWB3();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	
	private void helpSetExtLookBackForwForMergeableOp(LUInfoExtract luInfoExtract, Connection conn, NewVehicleData vdp, StopDirControl stopDirControl, FastList<LUSequence> seqList, int seqIndex, LUItem stItem, boolean setOutNullIfNotExit) {
		OpStationBean op = this.getOpStation();
		if (op == null) {
			  op = new OpStationBean();
			  op.setIntVal(8,1000);
		}
		setOutNullIfNotExit = true;
		if (op == null || this.isOpStationForStop())
			return;
		int mergeDistInt = op.getSeqMergeSeqMtr();
		double mergeDist = Misc.isUndef(mergeDistInt) ? Misc.getUndefDouble() : mergeDistInt;
		int breakDistInt = op.getLLUUBreakDistMtr();
		double breakDist = Misc.isUndef(breakDistInt) ? Misc.getUndefDouble() : breakDistInt;
		long gapThreshAsPerOpIfDir = op.getThreshCompletionDirChangeMilliSec()/3;
		
		long breakMilli = op.getLLUUBreakTimeMin()*60*1000/10;
		long mergeTimeMilli = breakMilli <= 0 && gapThreshAsPerOpIfDir <= 0 ? Misc.getUndefInt() : breakMilli <= 0 ? gapThreshAsPerOpIfDir : gapThreshAsPerOpIfDir <= 0 ? breakMilli : Math.min(gapThreshAsPerOpIfDir, breakMilli);
		
		if (mergeDist <=0 && breakDist > 0) {
			mergeDist = breakDist * 0.1;
		}

		if (mergeDist <= 0 && mergeTimeMilli <= 0) {
			 LUSequence seq= this;
			 LUItem item = seq == null ? null : seq.get(seq.getEndLUItemIndex());
			 if (item != null && setOutNullIfNotExit && seqIndex == seqList.size() - 1) {
					if (!(item.isWaitOutOrConfirmatory() || item.isGateOut())) {//neither wout or gout
						luInfoExtract.setWaitOut(Misc.getUndefInt());
						luInfoExtract.setGateOut(Misc.getUndefInt());	
					}
					else if (item.isGateOut() && !item.isWaitOutOnly() && !seq.isOpStationForStop()) {
						OpStationBean opb = seq.getOpBelogingBean();
						if (opb.getWaitAreaId() != opb.getGateAreaId())
							luInfoExtract.setWaitOut(Misc.getUndefInt());
					}
				}
		 }

		double distThresh = mergeDist/1000.0;
		double distThreshHalf = distThresh/2.0;
		
		if (seqIndex < 0)
			seqIndex = seqList.indexOf(this).first;
		int backSeqIndex = seqIndex;
		int forwSeqIndex = seqIndex;
		
		GpsData stiGpsData = stItem.getGpsData(conn, vdp);
		LUInfoExtract currExt = luInfoExtract;//this.getLUInfoExtractPlain();
		if (currExt == null)
			return;
		boolean breakForwBecauseOfOther = false;
		boolean breakBackBecauseOfOther = false;
		int backSeqIndexHalf = seqIndex;
		int forwSeqIndexHalf = seqIndex;
		for (int art = 0;art<2;art++) {
			int incr = art == 0 ? -1 : 1;
			LUSequence prev = null;
			for (int i=seqIndex+art; ; i+=incr) {
				LUSequence seq = seqList.get(i);
				if (seq == null)
					break;
				if (!seq.isOpStationForStop() && seq.getOpStationId() != this.getOpStationId()) {
					if (art == 0)
						breakBackBecauseOfOther = true;
					else
						breakForwBecauseOfOther = true;
					break;
				}
				boolean mergeAble = false;
				if (mergeTimeMilli > 0 && prev != null) {
				      long gap = art == 0 ? prev.getStartTime() - seq.getEndTime() : seq.getStartTime() - prev.getEndTime();
				      if (gap < mergeTimeMilli)
				    	  mergeAble = true;
				}
				GpsData seqGpsData = seq.getStartItemExt().getGpsData(conn, vdp);
				double d = (art == 0 ? 1 : -1) * (stiGpsData.getValue() - seqGpsData.getValue());
				if (d > distThresh)// || !mergeAble)
					break;
				if (seq.getOpStationId() == this.getOpStationId()) {// || seq.isOpStationForStop()) {
					if (art == 0)
						backSeqIndex = i;
					else
						forwSeqIndex = i;
					prev = seq;
				}
			}//go back or forw
			
		}//art loop for back/forw
		if (backSeqIndex != seqIndex) {
			LUSequence seq= seqList.get(backSeqIndex);
			LUItem item = seq.get(seq.getThisOrNextUsefulIndex(0));
			LUInfoExtract prevExt = seq.getLUInfoExtractPlain();
			long prevwin = prevExt == null ? item.getTime() : prevExt.getWaitIn();
			long prevgin = prevExt == null ? item.getTime() : prevExt.getGateIn();
			if (prevwin > currExt.getWaitIn())
				prevwin = currExt.getWaitIn();
			if (prevgin > currExt.getGateIn())
				prevgin = currExt.getGateIn();
			currExt.setWaitIn(prevwin);
			currExt.setGateIn(prevgin);
		}
		if (true || forwSeqIndex != seqIndex) {
			LUSequence seq= seqList.get(forwSeqIndex);
			int seqEndIndex = seq.getEndLUItemIndex();
			LUItem item = seq == null ? null : seq.get(seqEndIndex);
			
			if (item != null && currExt.getWaitOut() > 0 && item.getTime() > currExt.getWaitOut()) {
				currExt.setWaitOut(item.getTime());
				int prevItemIndex = seq.getEndLUItemIndex(seqEndIndex);
				LUItem prevItem = seq.get(prevItemIndex);
				if (prevItem == null || !prevItem.isGateOut()) {
					currExt.setGateOut(currExt.getWaitOut());
				}
				else {
					currExt.setGateOut(prevItem.getTime());
				}
			}
			if (item != null && setOutNullIfNotExit && forwSeqIndex == seqList.size() - 1) {
				if (!(item.isWaitOutOrConfirmatory() || item.isGateOut())) {
					currExt.setWaitOut(Misc.getUndefInt());
					currExt.setGateOut(Misc.getUndefInt());	
				}
				else if (item.isGateOut() && !item.isWaitOutOnly() && !seq.isOpStationForStop()) {
					OpStationBean opb = seq.getOpBelogingBean();
					if (opb.getWaitAreaId() != opb.getGateAreaId())
						currExt.setWaitOut(Misc.getUndefInt());
				}
			}
		}
	}

	private void helpSetExtLookBackForw(LUInfoExtract luInfoExtract, Connection conn, NewVehicleData vdp, StopDirControl stopDirControl, FastList<LUSequence> seqList, LUItem stItem, int seqIndex, int stItemIndex, LUItem enItem, int enItemIndex, long prevBlkEndTS, long nextBlkStartTS) {
		if (seqIndex < 0)
			seqIndex = seqList.indexOf(this).first;
		if (seqIndex < 0)
			return;
		if (stItemIndex < 0)
			stItemIndex = this.indexOf(stItem).first;
		if (enItemIndex < 0 && enItem != null)
			enItemIndex = this.indexOf(enItem).first;
		if (enItemIndex < 0)
			enItemIndex = stItemIndex;
		if (stItemIndex < 0)
			return;
		boolean isStopOpStation = this.isOpStationForStop();
		boolean isSetGinToWinInForwBack = stopDirControl.isSetGinToWinInForwBack();
		Triple<Integer, Integer, Boolean> back = helpGetPrevOrForwStoppageWithinDist(conn, vdp, stopDirControl, seqList, stItem, seqIndex, stItemIndex, true, prevBlkEndTS);
		Triple<Integer, Integer, Boolean> forw = enItemIndex < 0 ? null : helpGetPrevOrForwStoppageWithinDist(conn, vdp, stopDirControl, seqList, stItem, seqIndex, enItemIndex, false, nextBlkStartTS);
		if (back == null)
			back = new Triple<Integer, Integer, Boolean>(seqIndex, stItemIndex, true);
		if (forw == null)
			forw = new Triple<Integer, Integer, Boolean>(seqIndex, enItemIndex, true);
		if (forw != null && !forw.third) {
			double thresh = stopDirControl == null ? Misc.getUndefDouble() : stopDirControl.getLookForwDistMaxThreshForWaitKM();
			if (thresh < 0)
				thresh = stopDirControl == null ? Misc.getUndefDouble() : stopDirControl.getLookBackDistMaxThreshForWaitKM();
			if (thresh > 0) {
				forw.third = helperGetTrueBreak(conn, vdp, stopDirControl, seqList, stItem.getGpsData(conn, vdp), forw.first, forw.second, false, thresh);
			}
			else {
				forw.third = true;
			}
		}
		long gateIn = luInfoExtract.getGateIn();
		long waitIn = luInfoExtract.getWaitIn();
		long gateOut = luInfoExtract.getWaitOut();
		long waitOut = luInfoExtract.getGateOut();
		long areaIn = -1;
		long areaOut = -1;
		
		LUItem innerIn = stItem;
		LUSequence outerInSeq = back == null ? null : seqList.get(back.first);
		LUItem outerIn = outerInSeq == null ? null : outerInSeq.get(back.second);
		if (outerIn == null)
			outerIn = innerIn;
		
		int innerOutIndex =getEndLUItemIndex();
		LUItem innerOut = this.get(innerOutIndex);
		if (innerOut != null && (((isStopOpStation && innerOut.isStopStartExt()) ||(!isStopOpStation && ! innerOut.isOut())) && seqIndex == seqList.size() -1))
			innerOut = null;
		
		LUItem outerOut = null;
		if (forw == null)
			outerOut = innerOut;
		if (forw != null && forw.third) {
			LUSequence outerOutSeq = seqList.get(forw.first);
			int outerOutIndex = outerOutSeq == null ? -1 : outerOutSeq.getNextStopEndIndex(forw.second);
			if (outerOutIndex < 0 && outerOutSeq != null && forw.first < seqList.size()-1)
				outerOutIndex = outerOutSeq.size()-1;
			outerOut = outerOutSeq == null ? null : outerOutSeq.get(outerOutIndex);
			if (outerOut == null)
				outerOut = innerOut;
		}
		if (back != null) {
			waitIn = TripInfoConstants.getArtificialIgnoreTime(outerIn.getGpsDataRecordTime());
			if (isSetGinToWinInForwBack) {
				gateIn = waitIn;
				areaIn = TripInfoConstants.getArtificialIgnoreTime(innerIn.getGpsDataRecordTime());
				areaOut = innerOut == null ? Misc.getUndefInt() : TripInfoConstants.getArtificialIgnoreTime(innerOut.getGpsDataRecordTime());
			}
			else if (gateIn <= 0) {
				gateIn = TripInfoConstants.getArtificialIgnoreTime(innerIn.getGpsDataRecordTime());
			}
		}
		if (forw != null) {
			if (isSetGinToWinInForwBack)
				gateOut = outerOut == null ? Misc.getUndefInt() : TripInfoConstants.getArtificialIgnoreTime(outerOut.getGpsDataRecordTime());
			else if (innerOut == null)
				gateOut = Misc.getUndefInt();
			else if (gateOut <= 0)
				gateOut = TripInfoConstants.getArtificialIgnoreTime(innerOut.getGpsDataRecordTime());
			waitOut = outerOut == null ? Misc.getUndefInt() : TripInfoConstants.getArtificialIgnoreTime(outerOut.getGpsDataRecordTime());
		}
		luInfoExtract.setWaitIn(waitIn);
		luInfoExtract.setWaitOut(waitOut);
		luInfoExtract.setGateIn(gateIn);
		luInfoExtract.setGateOut(gateOut);
		luInfoExtract.setAreaIn(areaIn);
		luInfoExtract.setAreaOut(areaOut);
	}
	
		
	private Triple<Integer, Integer, Boolean> helpGetPrevOrForwStoppageWithinDist(Connection conn, NewVehicleData vdp, StopDirControl stopDirControl, FastList<LUSequence> seqList, LUItem stItem, int seqIndex, int itemIndex, boolean doBack, long relevantBlkEndpointTS) {
		//first = seqIndex, second = itemIndex, third if it saw true break ... false means there were not enough items to cause break 
		LUItem prevStopStart = null;
		if (stopDirControl == null || stopDirControl.getDoStopProcessing() == 0)
			return null;
		double lookBackDistMaxThreshKM = doBack ? stopDirControl.getLookBackDistMaxThreshForWaitKM()
				: stopDirControl.getLookForwDistMaxThreshForWaitKM(); 
		double lookBackDistBetweenStopKM = doBack ? stopDirControl.getLookBackDistBetweenStopKM() 
				: stopDirControl.getLookForwDistMaxThreshForWaitKM(); 
		boolean doTravelDistInsteadGeo = stopDirControl.isDoTravelDistanceInsteadOfGeo(); 
		if (Misc.isUndef(lookBackDistMaxThreshKM) || Misc.isUndef(lookBackDistBetweenStopKM))
			return null;
		LUItem selPrevStartStop = stItem;
		int meLoad = this.getGuessLoadType();
		GpsData stiGpsData = null;
		GpsData selPrevStartStopGpsData = null;
		int incr = doBack ? -1 : 1;
		int retvalSeqIndex = seqIndex;
		int retvalItemIndex = itemIndex;
		boolean retvalTrueBreak = false;
		int prevStopStartIndex = -1;
		if (stiGpsData == null) {
			//CHANGE_LUSEQ_START LUItem tf = this.getStartItemExt();
			//CHANGE_LUSEQ_START stiGpsData = tf == null || tf.getTime() != stItem.getTime() ? stItem.getGpsData(conn, vdp) : this.getStartGps(conn, vdp);
			stiGpsData = stItem.getGpsData(conn, vdp);
		}
		int origOpStationId = this.getOpStationId();
		boolean breakingBecasueOfNonDistFactor = false;
		ArrayList<Triple<Double, Integer, Integer>> halfWayMarkDetector = new ArrayList<Triple<Double, Integer, Integer>>();
		halfWayMarkDetector.add(new Triple<Double, Integer, Integer>(0.0, seqIndex, itemIndex));
		LUSequence prevSeq = null;
		for (int cnt = 0; ; cnt += incr) {
			LUSequence seq = seqList.get(this, cnt);
			if (seq == null)
				break;
			boolean toBreak = false;
			int seqG = seq.getGuessLoadType();
			if ((doBack && seq.getStartTime() <= relevantBlkEndpointTS) || (!doBack && seq.getEndTimeSimple() >= relevantBlkEndpointTS)) {
				toBreak = true;
				breakingBecasueOfNonDistFactor = true;
			}
			if (seq.isRestAreaType() || seq.isIgnoreAreaType())
				seqG = meLoad;
			if (!seq.isOpStationForStop() && origOpStationId != seq.getOpStationId()) {
				toBreak = true;
				breakingBecasueOfNonDistFactor = true;
			}
			else if (seq.isOpStationForStop()) { //new stuff
				if (seqG != meLoad) {
					//dont break if seq has no preferred region
					if (seq.isGuessedFromPreferred() || seq.isGuessedFromArtOp()) {
						toBreak = true;
						breakingBecasueOfNonDistFactor = true;
					}
					if (relevantBlkEndpointTS > 0) {
						if ((doBack && seq.getStartTime() <= relevantBlkEndpointTS) || (!doBack && seq.getEndTimeSimple() >= relevantBlkEndpointTS)) {
							toBreak = true;
							breakingBecasueOfNonDistFactor = true;
						}
					}
				}
			}
			else if (seqG != meLoad) {
				toBreak =true;
				breakingBecasueOfNonDistFactor = true;
			}
			if (!toBreak) {
				//if (doBack) {
				//	if (seq.isDala() && !seq.isDalaAtLeft())
				//		break;
				//}
				//else {
				//	if (seq.isDala() && !seq.isDalaAtRight())
				//		break;
				//}
				if (prevSeq != null) { 
					if (prevSeq.isDala() != seq.isDala())
						break;
					//TODO dala down in between ...
				}
				
			}
			if (toBreak) {
				retvalTrueBreak = true;
				break;
			}
			int lookBefore = doBack ? seq.size()-1 : 0;
			if (cnt == 0) {
				lookBefore =  itemIndex+incr;
			}
			boolean lookedBackEnough = false;
			int j=lookBefore;
			for (LUItem luitem = seq.get(j);luitem != null;) {//decr/incr happens inside - break of seenAllItem
				for (;luitem != null;luitem = seq.get(j += incr)) {
					if (luitem.isStopEnd() && prevStopStart != null) {
						break;
					}
					if (luitem.isStopStartExt() && (prevStopStart == null || doBack)) {
						prevStopStart = luitem;
						prevStopStartIndex = j;
					}
				}
				if (prevStopStart != null) {
					if (selPrevStartStopGpsData == null)
						selPrevStartStopGpsData = stiGpsData;
					//CHANGE_LUSEQ_START LUItem seqFirstItem = seq.getStartItemExt();
					//CHANGE_LUSEQ_START GpsData prevStopStartGpsData = seqFirstItem == null || seqFirstItem.getTime() != prevStopStart.getTime() ? prevStopStart.getGpsData(conn, vdp)
					//CHANGE_LUSEQ_START 		: seq.getStartGps(conn, vdp);
					GpsData prevStopStartGpsData = prevStopStart.getGpsData(conn, vdp);
					
					double dincr = doTravelDistInsteadGeo ? selPrevStartStopGpsData.getValue() - prevStopStartGpsData.getValue()
							: selPrevStartStopGpsData.distance(prevStopStartGpsData.getLongitude(), prevStopStartGpsData.getLatitude());
					if (doTravelDistInsteadGeo && !doBack)
						dincr *= -1;
					if (dincr > lookBackDistBetweenStopKM) {
						lookedBackEnough = true;
						retvalTrueBreak = true;
						break;
					}
					double dtot = doTravelDistInsteadGeo? stiGpsData.getValue() - prevStopStartGpsData.getValue()
							: stiGpsData.distance(prevStopStartGpsData.getLongitude(), prevStopStartGpsData.getLatitude());
					if (doTravelDistInsteadGeo && !doBack)
						dtot *= -1;
					
					if (dtot > lookBackDistMaxThreshKM) {
						lookedBackEnough = true;
						retvalTrueBreak = true;
						break;
					}
					
					selPrevStartStop = prevStopStart;
					selPrevStartStopGpsData = prevStopStartGpsData;
					retvalSeqIndex = seqIndex+cnt;
					retvalItemIndex = prevStopStartIndex;
					halfWayMarkDetector.add(new Triple<Double, Integer, Integer>(dtot, retvalSeqIndex, retvalItemIndex));
					prevStopStart = null;
					prevStopStartGpsData = null;
					prevStopStartIndex = -1;
					
				}//inner loop finding true beg of start within seq
				if (lookedBackEnough)
					break;
			}//prev se
			if (lookedBackEnough)
				break;
		}//go back in seq list to get prev start marker
		if (selPrevStartStop == null) {
			retvalSeqIndex = seqIndex;
			retvalItemIndex = itemIndex;
		}
		else if (breakingBecasueOfNonDistFactor) {
			//get the max d
			double maxd = -1;
			for (Triple<Double, Integer, Integer> hm: halfWayMarkDetector ) {
				if (hm.first > maxd)
					maxd = hm.first;
			}
			maxd = maxd/2;
			for (Triple<Double, Integer, Integer> hm: halfWayMarkDetector ) {
				if (hm.first > maxd || Misc.isEqual(hm.first, maxd)) {
					break;
				}
				retvalSeqIndex = hm.second;
				retvalItemIndex = hm.third;
			}
		}
		if (!retvalTrueBreak && !doBack) {
			retvalTrueBreak = helperGetTrueBreak(conn, vdp, stopDirControl, seqList, stiGpsData, retvalSeqIndex, retvalItemIndex, doBack, lookBackDistMaxThreshKM);
		}
		else if (doBack) {
			retvalTrueBreak = true;
		}
		
		return new Triple<Integer, Integer, Boolean>(retvalSeqIndex, retvalItemIndex, retvalTrueBreak);
	}
	
	private boolean helperGetTrueBreak(Connection conn, NewVehicleData vdp, StopDirControl stopDirControl, FastList<LUSequence> seqList, GpsData stiGpsData, int retvalSeqIndex, int retvalItemIndex, boolean doBack, double lookBackDistMaxThreshKM) {
		int incr = doBack ? -1 : 1;
		boolean retvalTrueBreak = false;
		boolean doTravelDistInsteadGeo = stopDirControl.isDoTravelDistanceInsteadOfGeo(); 
		int meLoad = this.getGuessLoadType();
		int origOpStationId = this.getOpStationId();
		
		for (int i=retvalSeqIndex+incr ; ; i+=incr) {
			LUSequence seq = seqList.get(i);
			if (seq == null)
				break;
			int seqG = seq.getGuessLoadType();
			if (seq.isRestAreaType() || seq.isIgnoreAreaType())
				seqG = meLoad;
			if (!seq.isOpStationForStop() && origOpStationId != seq.getOpStationId()) {
				retvalTrueBreak = true;
			}
			else if (seq.isOpStationForStop()) { //new stuff
				if (seqG != meLoad) {
					//dont break if seq has no preferred region
					if (seq.isGuessedFromPreferred() || seq.isGuessedFromArtOp())
						retvalTrueBreak = true;
				}
			}
			else if (seqG != meLoad) {
				retvalTrueBreak =true;
			}
			if (retvalTrueBreak) {
				break;
			}
			
			LUItem luItem = seq.getStartItemExt();
			GpsData start = luItem.getGpsData(conn, vdp);
			double dtot = doTravelDistInsteadGeo? stiGpsData.getValue() - start.getValue()
					: stiGpsData.distance(start.getLongitude(), start.getLatitude());
			if (doTravelDistInsteadGeo && !doBack)
				dtot *= -1;
			if (dtot > lookBackDistMaxThreshKM) {
				retvalTrueBreak = true;
			}
			if (retvalTrueBreak)
				break;
		}
		if (!retvalTrueBreak) {
			GpsData last = vdp.getLast(conn);
			if (last != null) {
				double dtot = doTravelDistInsteadGeo? stiGpsData.getValue() - last.getValue()
						: stiGpsData.distance(last.getLongitude(), last.getLatitude());
				if (doTravelDistInsteadGeo && !doBack)
					dtot *= -1;
				if (dtot > lookBackDistMaxThreshKM) {
					retvalTrueBreak = true;
				}
			}
		}
		return retvalTrueBreak;
	}
	
	public ExtLUInfoExtract getLUInfoExtractOfStop(StopDirControl stopDirControl, boolean hasNext, FastList<LUSequence> seqList, Connection conn, NewVehicleData vdp, ManagementUnit mu, LUInfoExtract origExtract, CacheTrack.VehicleSetup vehSetup, long prevBlkEndTS, long nextBlkStartTS) throws Exception {
		int st = this.getStopStartIndex();
		int en = this.getStopEndIndex();
		int enForTiming = en < 0 ? getThisOrPrevUsefulIndex(size()-1) : en;
		if (en < 0 && hasNext) {
			en = enForTiming;
		}
		LUItem stItem = get(st);
		LUItem enItem = get(en);
		boolean hasEnded = enItem != null && (this.isOpStationForStop() ? enItem.isStopEnd() : enItem.isWaitOutOrConfirmatory());
		
		long enTime = enItem != null ? enItem.getGpsDataRecordTime() : vdp.getMaxTime();//
		luInfoExtract.setOpComplete(false);
		if (!Misc.isUndef(luInfoExtract.getPrefOrMovingOpId())) {
			OpStationBean prefBean = TripInfoCacheHelper.getOpStation(luInfoExtract.getPrefOrMovingOpId());
			if (prefBean != null)
				luInfoExtract.setMaterialId(prefBean.getPrefMaterial());
		}
		
		if (origExtract != null) {
			luInfoExtract.specialSetArtOpStationId(stItem.getTime(), origExtract);
		}
        byte artOpStationMatch = luInfoExtract.getArtOpStationMatch();
        
        if (mu != null && this.isOpStationForStop()) {
        	//we need to check if it might be near an art opstation
        	if (this.prefOrMovingOpStationId > 0)
				artOpStationMatch = 1;
			else if (artOpStationMatch < 0){
				BestMergeDistResult artResult = mu.getNearestStation(conn, stItem.getGpsData(conn, vdp), vdp, true);
				int artStationId = Misc.getUndefInt();
				if (artResult == null || artResult.bestIndex < 0 || !artResult.isWithingRange) {
					artOpStationMatch = 0;
				}
				else {
					artOpStationMatch = (byte)artResult.support;
					artStationId = artResult.bestIndex;
					if (!Misc.isUndef(artStationId) && artOpStationMatch > 0) {
						int movingOpId = artResult.movingOpStationIds != null && artResult.movingOpStationIds.size() > 0 ? artResult.movingOpStationIds.get(artResult.movingOpStationIds.size()-1)
								: Misc.getUndefInt();
						this.setOpBelonging(movingOpId, null, false, true);
						//seq.setGuaranteedLoadType((byte)OpStationBean.getGuaranteedLoadTypeFromOpType(TripInfoConstants.LOAD));
						
						this.setGuessLoadType((byte)OpStationBean.getGuaranteedLoadTypeFromOpType(TripInfoConstants.LOAD));
						this.setGuessedFromPreferred(true);
						this.setPrefOrMovingOpStationId(movingOpId);
					}
					if (artOpStationMatch > 0 && this.isUnknownLUGuaranteed()  && this.isUnknownLUGuess()) {
						this.setGuessLoadType((byte)0);
					}
				}
				luInfoExtract.setArtOpStationId(artStationId);
				luInfoExtract.setArtOpStationIdWasActuallyCreated((byte)0);
			}
        	luInfoExtract.setArtOpStationMatch(artOpStationMatch);
        }
        boolean complete = stItem != null && !isIgnoreAreaType();
        int dirChange0123 = this.getDirChange0123();
		boolean dirChangeProper = stopDirControl.isForProperBothDirMust() ? (dirChange0123 == 3 || dirChange0123 == 8 || dirChange0123 == 9) 
                : stopDirControl.isForProperHiDirMust() ? (dirChange0123 !=0 && dirChange0123 != 1 && dirChange0123 != 5) 
                : stopDirControl.isForProperLoDirMust() ? dirChange0123 == 1 || dirChange0123 == 3 || dirChange0123 == 5 || dirChange0123 == 8 || dirChange0123 == 9 
                : dirChange0123 > 0;
        if (!hasEnded)
        	dirChangeProper = true;
        if (complete) {
        	
     	   long gapThresh = 0;
    	   if (dirChangeProper)
    		   gapThresh = (long)(stopDirControl.getStopThreshTobeOpstationIfDirMin()*60*1000);
    	   else
    		  gapThresh = (long)(stopDirControl.getStopThreshTobeOpstationMin()*60*1000);
    	   OpStationBean bean = this.getOpBelogingBean();
    	   long gapThreshAsPerOp = bean.getIntVal(5);
    	   if (gapThreshAsPerOp < 0) {
    		   for (int i=0,is=bean.getRegionIdsListDontAdd() == null ? 0 : bean.getRegionIdsListDontAdd().size(); i<is; i++) {
    			   OpArea area = bean.getRegionIdsListDontAdd().get(i);
    			   if (area != null && area.thresholdMilliSec > 0) {
    				   if (gapThreshAsPerOp < 0)
    					   gapThreshAsPerOp = area.thresholdMilliSec;
    				   else if (gapThreshAsPerOp > area.thresholdMilliSec)
    					   gapThreshAsPerOp = area.thresholdMilliSec;
    			   }
    		   }
    	   }
    	   long gapThreshAsPerOpIfDir = bean.getThreshCompletionDirChangeMilliSec();
    	   if (!Misc.isUndef(gapThreshAsPerOpIfDir) && !dirChangeProper && stopDirControl.getDoStopProcessing() >= 1) {
   			//DEBUG13 .. orig is below .. above is try for hind - needs change of meaning of getThreshCompletionDirChangeMilliSec
   		   // to getThreshCompletionIfNoDirChangeMilliSec
    		   //if (!Misc.isUndef(gapThreshAsPerOpIfDir) && dirChangeProper)
    		   gapThreshAsPerOp = gapThreshAsPerOpIfDir;
    	   }
    	   if (gapThreshAsPerOp > gapThresh)
    		   gapThresh = gapThreshAsPerOp;
    	   if (stopDirControl != null && this.isOpStationForStop() && stopDirControl.getExtStopDurSecComplete()*1000 > gapThresh)
    		   gapThresh = stopDirControl.getExtStopDurSecComplete()*1000;
    	   boolean lookBackForWait = true;
   		   helpSetExtLookBackForw(luInfoExtract, conn, vdp, stopDirControl, seqList, stItem, Misc.getUndefInt(), st, enItem, en, prevBlkEndTS, nextBlkStartTS);
   		//    long enTime = retval.getGateOut() > 0 ? retval.getGateOut() : vdp.getMaxTime();
   		   long ot = luInfoExtract.getGateOut() > 0 ? luInfoExtract.getGateOut() : vdp.getMaxTime();
   		   long gap = ot - luInfoExtract.getGateIn();
		   complete = gap > gapThresh && !isIgnoreAreaType(); //for ignoreAreaType
			if(!Misc.isUndef(stopDirControl.ignoreOpNearLandmarkTypes) && !isIgnoreAreaType() ){
				Pair<ShapeFileBean, Double> nearPointPair = RTreeSearch.getNearestNSpecialLM(stItem.getGpsData(conn, vdp).getPoint(), vehSetup.m_ownerOrgId, stopDirControl.ignoreLandmarkDistThresh, null, stopDirControl.ignoreOpNearLandmarkTypes);
				if(nearPointPair != null)
					complete = false;
			}
			if (complete) {
				if (stopDirControl.getExtStopDurSecComplete() > 0 && gap/1000 < stopDirControl.getExtStopDurSecComplete())
					complete = false;
			}
			
			if (complete && hackDoStrictSeqTimeMatch(stopDirControl) && enItem != null) {
				long threshMilli = hackGetAdjustedHackThreshAsMilli(stopDirControl, !hasNext,0);
				long gaploc = enTime - stItem.getTime();
				if (gaploc < threshMilli)
					complete = false;
			}
			luInfoExtract.setOpComplete(complete);
        }
		
		if (this.isDala() || this.getStrikeCount() > 1 || (this.getShovelAssignQuality() > 0))
			luInfoExtract.setOpComplete(true);
		luInfoExtract.setDirChanged(dirChange0123);
		
			
	   if (stopDirControl != null && stopDirControl.getDoStopProcessing() < 3 && this.isOpStationForStop())
		   luInfoExtract.setOpComplete(false);
		return (ExtLUInfoExtract) luInfoExtract;
	}
	public LUInfoExtract getLUInfoExtractPlain() {
		return luInfoExtract;
	}
	public static long g_dbgDt1 = new java.util.Date(113,9,29,22,0,0).getTime();
	public static long g_dbgDt2 = new java.util.Date(113,9,29,23,0,0).getTime();
	public static int g_minDalaUpSec = 24;
	public static int g_distInValidImpactSec = 20*60;
	public static int g_maxDalaUpSec = (int)(8*60);
	public static int g_maxDalaUpToCheckBefStartMilli = g_minDalaUpSec*1000;
	public static boolean isDalaUpLikeRest(int durSec) {
		return durSec > LUSequence.g_maxDalaUpSec;
	}
	public boolean isDalaUpLikeRest() {
		int durSec = (Misc.isUndef(this.endDalaRelStart) ? (int)((this.getEndTimeSimple()-this.getStartTime())/1000) : this.endDalaRelStart) - this.startDalaRelStart;
		return isDalaUpLikeRest(durSec);
	}
	private MiscInner.Pair getDalaRaiseInfo(Connection conn, Dala01Mgmt dala01Mgmt, NewVehicleData dalaVDT, NewVehicleData vdp, StopDirControl stopDirControl) {
		//rel to start when the dala was raised and second - brought down
		int first = -1;
		int second = -1;
		if (dalaVDT != null) {
			LUItem stItem = getStartItemExt();
			LUItem enItem = getEndLUItem();
			long tsStart = TripInfoConstants.getArtificialIgnoreTime(stItem == null ? 0 : stItem.getGpsDataRecordTime());
			long tsEnd = TripInfoConstants.getArtificialIgnoreTime(enItem == null ? Long.MAX_VALUE : enItem.getGpsDataRecordTime());
			GpsData lastDala = dalaVDT.getLast(conn);
			MiscInner.TripleLongLongBoolean dala = dala01Mgmt.getValidBetween(conn, stopDirControl, tsStart, tsEnd, dalaVDT, vdp,lastDala);
			if (dala.first > 0 && dala.second > 0) {
				first = (int)((dala.first - tsStart)/1000);
				 second = (int)((dala.second - tsStart)/1000);
			}
			boolean ongoing = dala.third;
			if (ongoing)
				second = Misc.getUndefInt();
		}
		 return new MiscInner.Pair(first, second);
	}
	
	public MiscInner.Pair getDalaRaiseInfoOld(Connection conn, Dala01Mgmt dala01Mgmt, NewVehicleData dalaVDT, NewVehicleData vdp, StopDirControl stopDirControl) {
		//rel to start when the dala was raised and second - brought down
		int first = -1;
		int second = -1;
		if (dalaVDT != null) {
			double speedLt = stopDirControl.getStopThreshSpeedKMPH();
			if (speedLt < 0.0001)
				speedLt = 5;
			LUItem stItem = getStartItemExt();
			LUItem enItem = getEndLUItem();
			long tsStart = TripInfoConstants.getArtificialIgnoreTime(stItem == null ? 0 : stItem.getGpsDataRecordTime());
			long tsEnd = TripInfoConstants.getArtificialIgnoreTime(enItem == null ? Long.MAX_VALUE : enItem.getGpsDataRecordTime());
			GpsData ref = new GpsData(tsStart);
			//prior pt must be 1 and if 0 no more than X milli sec prior
			GpsData prior1pt = null;
			GpsData prior0pt = null;
			for (int i=0;;i--) {
				GpsData curr = dalaVDT.get(conn, ref, i);
				if (curr == null)
					break;
				boolean isUp = curr.getValue() < 0.5;
				//because of noise - we want to be sure that this happened at less than threshold speed
				if (isUp) {
					GpsData distPt = vdp.get(conn, curr);
					if (distPt != null && distPt.getSpeed() > speedLt)
						isUp = false;
				}
				if (!isUp) {
					prior0pt = curr;
					break;
				}
				else 
					prior1pt = curr;
			}
			if (prior1pt != null && (prior0pt == null || prior0pt.getGps_Record_Time() < prior1pt.getGps_Record_Time()) 
					&& (tsStart-prior1pt.getGps_Record_Time()) > LUSequence.g_maxDalaUpToCheckBefStartMilli) {
				return new MiscInner.Pair(-1,-1);
			}
			long starting1PtInCurrBlock = prior1pt == null ? 0 : prior1pt.getGps_Record_Time();
			long ending1PtInCurrBlock = starting1PtInCurrBlock;
			long bestStarting1PtInCurrBlock = 0;
			long bestEnding1PtInCurrBlock = 0;
			int bestDurSec = 0;
			for (int i=0;;i++) {
				GpsData curr = dalaVDT.get(conn, ref, i);
				if (curr == null && i == 0)
					continue;
				if (curr == null)
					break;
				if (curr.getGps_Record_Time() < tsStart)
					continue;
				boolean isUp = curr.getValue() < 0.5;
				if (isUp) {
					GpsData distPt = vdp.get(conn, curr);
					if (distPt != null && distPt.getSpeed() > speedLt)
						isUp = false;
				}

				if (!isUp) {
					//ending1PtInCurrBlock = curr;
					if (starting1PtInCurrBlock >0) {
						int durSecOfCur1 =  (int)((ending1PtInCurrBlock -starting1PtInCurrBlock)/1000)
								   ;
						 boolean durSecValid = durSecOfCur1 >= g_minDalaUpSec&& durSecOfCur1 <g_maxDalaUpSec;
						 if (durSecValid && durSecOfCur1 > bestDurSec) {
							 bestStarting1PtInCurrBlock = starting1PtInCurrBlock;
							 bestEnding1PtInCurrBlock = ending1PtInCurrBlock;
							 bestDurSec = durSecOfCur1;
						 }
					}
					 starting1PtInCurrBlock = 0;
					 ending1PtInCurrBlock = 0;
				}
				else {
					if (starting1PtInCurrBlock == 0)
						starting1PtInCurrBlock = curr.getGps_Record_Time();
					ending1PtInCurrBlock = curr.getGps_Record_Time();
				}
				if (curr.getGps_Record_Time() >= (tsEnd+LUSequence.g_maxDalaUpToCheckBefStartMilli))
					break;
			}
			if (starting1PtInCurrBlock > 0) {
				if (ending1PtInCurrBlock == starting1PtInCurrBlock) {
					ending1PtInCurrBlock = tsEnd; 
				}
				int durSecOfCur1 = 
				    (int)((ending1PtInCurrBlock -starting1PtInCurrBlock)/1000)
						   ;
				 boolean durSecValid = durSecOfCur1 >= g_minDalaUpSec && durSecOfCur1 <g_maxDalaUpSec;
				 if (durSecValid && durSecOfCur1 > bestDurSec) {
					 bestStarting1PtInCurrBlock = starting1PtInCurrBlock;
					 bestEnding1PtInCurrBlock = ending1PtInCurrBlock;
					 bestDurSec = durSecOfCur1;
				 }
			}
			 if (bestDurSec >= g_minDalaUpSec && bestDurSec <g_maxDalaUpSec) {
				 first = (int)((bestStarting1PtInCurrBlock - tsStart)/1000);
				 second = (int)((bestEnding1PtInCurrBlock - tsStart)/1000);
			 }
		}
		 return new MiscInner.Pair(first, second);
	}
	public void setDalaRaise(Connection conn, Dala01Mgmt dala01Mgmt, NewVehicleData dalaVDT, NewVehicleData vdp, StopDirControl stopDirControl) {

		MiscInner.Pair dalaInfo = getDalaRaiseInfo(conn, dala01Mgmt, dalaVDT, vdp, stopDirControl);
		int dalaSt = dalaInfo.first;
		int dalaEn = dalaInfo.second;
		if (dalaSt > Short.MAX_VALUE)
			dalaSt = Short.MAX_VALUE;
		if (dalaSt <= Misc.getUndefShort())
			dalaSt = Misc.getUndefShort()+10;
		if (dalaEn > Short.MAX_VALUE)
			dalaEn = Short.MAX_VALUE;
		if (Misc.isUndef(dalaEn))
			dalaEn = Misc.getUndefShort();
		if (dalaInfo.first != dalaInfo.second) {
			this.startDalaRelStart = (short) dalaSt;
			this.endDalaRelStart = (short) dalaEn;
		}
		this.setDalaRaise((dalaInfo.first != dalaInfo.second));
	}
	public static final long g_milliStrikePreAllowed = 10000;
	public static final long g_maxGapBetweenStrikes = (long)(3.5*60*1000);
	public static final boolean g_useGpsLoad = true;
	public void setStrikeCount(Connection conn, NewVehicleData strikeVDT) {
		int bestStart = Misc.getUndefInt();
		int bestEnd = Misc.getUndefInt();
		int bestCount = 0;
		int bestStartAfterDala = Misc.getUndefInt();
		int bestEndAfterDala = Misc.getUndefInt();
		int bestCountAfterDala = Misc.getUndefInt();
		
		int bestStartAfterGpsLoad = Misc.getUndefInt();
		int bestEndAfterGpsLoad = Misc.getUndefInt();
		int bestCountAfterGpsLoad = Misc.getUndefInt();
		
		if (!ShovelSequence.IGNORE_STRIKE && strikeVDT != null) {
			LUItem stItem = getStartItemExt();
			LUItem enItem = getEndLUItem();
			long tsStart = TripInfoConstants.getArtificialIgnoreTime(stItem == null ? 0 : stItem.getGpsDataRecordTime());
			long tsEnd = TripInfoConstants.getArtificialIgnoreTime(enItem == null ? Long.MAX_VALUE : enItem.getGpsDataRecordTime());
			long tsDalaUpLast = !Misc.isUndef(this.endDalaRelStart) ? tsStart + 1000*this.endDalaRelStart -g_milliStrikePreAllowed: this.getEndTimeSimple();
			long tsGpsLoad = tsStart+1000*(this.loadStGpsStart < 0 ? 0 : this.loadStGpsStart)-g_milliStrikePreAllowed;
			
			GpsData en = new GpsData(tsEnd);
			
			int currStart = Misc.getUndefInt();
			int currEnd = Misc.getUndefInt();
			int count = 0;
			GpsData ptBeforeCurr = null;
			boolean isAfterDalaUp = false;
			boolean isAfterGpsLoad = false;
			for (int i=0;true;i--) {
				
				GpsData currPt = ptBeforeCurr == null ? strikeVDT.get(conn, en, i) : ptBeforeCurr;
				if (currPt == null || currPt.getGps_Record_Time() < (tsStart-g_milliStrikePreAllowed))
					break;
				ptBeforeCurr = strikeVDT.get(conn, en, i-1);
				
				
				boolean ptBeforeCurrBeforDala = ptBeforeCurr != null && ptBeforeCurr.getGps_Record_Time() <= tsDalaUpLast;
				boolean ptBeforeCurrBeforGpsLoad = ptBeforeCurr != null && ptBeforeCurr.getGps_Record_Time() <= tsGpsLoad; 
				isAfterDalaUp = currPt.getGps_Record_Time() > tsDalaUpLast;
				isAfterGpsLoad = currPt.getGps_Record_Time() > tsGpsLoad;
				if (isAfterDalaUp && ptBeforeCurrBeforDala) {
					if (count > bestCountAfterDala) {
						bestStartAfterDala = currStart;
						bestEndAfterDala = currEnd;
						bestCountAfterDala = count;
					}
				}
				if (isAfterGpsLoad && ptBeforeCurrBeforGpsLoad) {
					if (count > bestCountAfterGpsLoad) {
						bestStartAfterGpsLoad = currStart;
						bestEndAfterGpsLoad = currEnd;
						bestCountAfterGpsLoad = count;
					}
				}
				

				long secGapLong = (currPt.getGps_Record_Time() - tsStart)/1000;
				short secGap = secGapLong > Short.MAX_VALUE ? Short.MAX_VALUE : secGapLong < Short.MIN_VALUE ?  (short)(Misc.getUndefShort()+2)
						: (short)secGapLong
							;
				if (Misc.isUndef(currEnd))
						currEnd = secGap;
					currStart = secGap;
					count++;

				boolean currIsAcceptable = ptBeforeCurr == null || (currPt.getGps_Record_Time()-ptBeforeCurr.getGps_Record_Time()) <=g_maxGapBetweenStrikes;
				if (!currIsAcceptable) {
					
					if (count > 0 && bestCount == 0) { //if (count > bestCount) {
						bestStart = currStart;
						bestEnd = currEnd;
						bestCount = count;
					}
					if (isAfterDalaUp) {
						if (count > 0 && bestCountAfterDala == 0) { //(count > bestCountAfterDala) {
							bestStartAfterDala = currStart;
							bestEndAfterDala = currEnd;
							bestCountAfterDala = count;
						}
					}
					if (isAfterGpsLoad) {
						if (count > 0 && bestCountAfterGpsLoad == 0) { //(count > bestCountAfterDala) {
							bestStartAfterGpsLoad = currStart;
							bestEndAfterGpsLoad = currEnd;
							bestCountAfterGpsLoad = count;
						}
					}
					currStart = Misc.getUndefInt();
					currEnd = Misc.getUndefInt();
					count = 0;
					
				}
			}//end of for
			if (count > 0 && bestCount == 0) {
				bestStart = currStart;
				bestEnd = currEnd;
				bestCount = count;
			}
			if (isAfterDalaUp) {
				if (count > bestCountAfterDala && bestCountAfterDala == 0) {
					bestStartAfterDala = currStart;
					bestEndAfterDala = currEnd;
					bestCountAfterDala = count;
				}
			}
			if (isAfterGpsLoad) {
				if (count > bestCountAfterGpsLoad && bestCountAfterGpsLoad == 0) {
					bestStartAfterGpsLoad = currStart;
					bestEndAfterGpsLoad = currEnd;
					bestCountAfterGpsLoad = count;
				}
			}
		}
		if (bestCountAfterDala > 2) {
			bestCount = bestCountAfterDala;
			bestStart = bestStartAfterDala;
			bestEnd = bestEndAfterDala;
		}
		else if (g_useGpsLoad && bestCountAfterGpsLoad >= 2) {
			bestCount = bestCountAfterGpsLoad;
			bestStart = bestStartAfterGpsLoad;
			bestEnd = bestEndAfterGpsLoad;
		}
		this.setStrikeCount(bestCount, bestStart, bestEnd, bestCountAfterDala > 2 || (g_useGpsLoad && bestCountAfterGpsLoad >= 2));
	}
	
	public long hackGetAdjustedHackThreshAsMilli(StopDirControl stopDirControl, boolean isAtEnd, int mustAsLoad) {
		if (isAtEnd)
			return 29*1000;
		boolean isLoad = mustAsLoad == 1 || this.getGuessLoadType() == 0;
		boolean isUnload = mustAsLoad == -1 || this.getGuessLoadType() == 1;
		return isLoad ? (long)(1.25*60*1000-1000) :  (long)(0.5*60*1000-1000); 

	}
	
	public double hackGetAdjustedHackThreshAsMin(StopDirControl stopDirControl, boolean isAtEnd, int mustAsLoad, double currThresh) {
		return (double)(hackGetAdjustedHackThreshAsMin(stopDirControl, isAtEnd, mustAsLoad, (long)(currThresh*60*1000)))/(60*1000);
	}
	
	public boolean hackDoStrictSeqTimeMatch(StopDirControl stopDirControl) {
		return stopDirControl != null && stopDirControl.getDoSpecialAlgoForBestSeq() == 1;
	}
	public boolean quickCheckStopCompleteDur(StopDirControl stopDirControl, int asLoad) {
		if (this.isDala())
			return true;
		LUItem stItem = this.getStartItemExt();
		LUItem enItem = this.getEndLUItem();
		if (hackDoStrictSeqTimeMatch(stopDirControl) && enItem != null) {
			long threshMilli = hackGetAdjustedHackThreshAsMilli(stopDirControl, false,asLoad);
			long gap = enItem.getTime() - stItem.getTime();
			return (gap >= threshMilli);
		}
		return true;
	
	}
	
	public LUInfoExtract getQuickLUInfoExtract(CacheTrack.VehicleSetup vehSetup, StopDirControl stopDirControl, FastList<LUSequence> seqList, int thisSeqIndex, Connection conn, NewVehicleData vdp, ManagementUnit mu) throws Exception {//TODO_VERIFY
	    boolean hasNext = thisSeqIndex != seqList.size()-1;	
		LUInfoExtract retval = this.luInfoExtract;
		if (!this.isLUInfoReextractDirty())
			return retval;
		this.resetLUInfoReextractDirty();
		LUInfoExtract orig = retval;
		luInfoExtract = retval = new LUInfoExtract();
		retval.setPrefOrMovingOpId(this.prefOrMovingOpStationId);
		if (!Misc.isUndef(luInfoExtract.getPrefOrMovingOpId())) {
			OpStationBean prefBean = TripInfoCacheHelper.getOpStation(luInfoExtract.getPrefOrMovingOpId());
			if (prefBean != null)
				luInfoExtract.setMaterialId(prefBean.getPrefMaterial());
		}
		retval.setOpComplete(false);
		int stIndex = getThisOrNextUsefulIndex(0);
		int enIndex = getEndLUItemIndex();
		
		LUItem stItem = get(stIndex);
		LUItem enItem = get(enIndex);
		if (stItem == null)
			return retval;
		if (enItem == null)
			enItem = stItem;
		if (stItem == null)
			return retval;
		boolean hasEnded = this.isOpStationForStop() ? enItem.isStopEnd() : enItem.isWaitOutOrConfirmatory();
		int dirChange0123 = this.getDirChange0123();
		boolean dirChangeProper = stopDirControl.isForProperBothDirMust() ? (dirChange0123 == 3 || dirChange0123 == 8 || dirChange0123 == 9) 
                : stopDirControl.isForProperHiDirMust() ? (dirChange0123 !=0 && dirChange0123 != 1 && dirChange0123 != 5) 
                : stopDirControl.isForProperLoDirMust() ? dirChange0123 == 1 || dirChange0123 == 3 || dirChange0123 == 5 || dirChange0123 == 8 || dirChange0123 == 9 
                : dirChange0123 > 0;
        if (!hasEnded)
        	dirChangeProper = true;
        if (orig != null)
        	retval.specialSetArtOpStationId(stItem.getTime(), orig);
        byte artOpStationMatch = retval.getArtOpStationMatch();
        
        if (mu != null && this.isOpStationForStop()) {
        	//we need to check if it might be near an art opstation
        	if (this.prefOrMovingOpStationId > 0)
				artOpStationMatch = 1;
			else if (artOpStationMatch < 0){
				BestMergeDistResult artResult = mu.getNearestStation(conn, stItem.getGpsData(conn, vdp), vdp, true);
				int artStationId = Misc.getUndefInt();
				if (artResult == null || artResult.bestIndex < 0 || !artResult.isWithingRange) {
					artOpStationMatch = 0;
				}
				else {
					artOpStationMatch = (byte)artResult.support;
					artStationId = artResult.bestIndex;
					if (artOpStationMatch > 0 && this.isUnknownLUGuaranteed() && this.isUnknownLUGuess()) {
						this.setGuessLoadType((byte)0);
					}
					if (!Misc.isUndef(artStationId) && artOpStationMatch > 0) {
						int movingOpId = artResult.movingOpStationIds != null && artResult.movingOpStationIds.size() > 0 ? artResult.movingOpStationIds.get(artResult.movingOpStationIds.size()-1)
								: Misc.getUndefInt();
						this.setOpBelonging(movingOpId, null, false, true);//TODO for miningInfo
						//seq.setGuaranteedLoadType((byte)OpStationBean.getGuaranteedLoadTypeFromOpType(TripInfoConstants.LOAD));
						
						this.setGuessLoadType((byte)OpStationBean.getGuaranteedLoadTypeFromOpType(TripInfoConstants.LOAD));
						this.setGuessedFromPreferred(true);
						this.setPrefOrMovingOpStationId(movingOpId);
					}
				}
				retval.setArtOpStationId(artStationId);
				retval.setArtOpStationIdWasActuallyCreated((byte)0);
			}
        	if (artOpStationMatch > 0) {
        		dirChangeProper = true;
        	}
        	retval.setArtOpStationMatch(artOpStationMatch);
        }
        
  	   retval.setWaitIn(stItem == null ? Misc.getUndefInt() : stItem.getTime());
	   retval.setWaitOut(!hasEnded || enItem == null ? Misc.getUndefInt() : enItem.getTime());
	   long gapThresh = 0;
	   if (dirChangeProper)
		   gapThresh = (long)(stopDirControl.getStopThreshTobeOpstationIfDirMin()*60*1000);
	   else
		  gapThresh = (long)(stopDirControl.getStopThreshTobeOpstationMin()*60*1000);

  
	   OpStationBean bean = this.getOpBelogingBean();
	   long gapThreshAsPerOp = bean.getIntVal(5);
	   if (gapThreshAsPerOp < 0) {
		   for (int i=0,is=bean.getRegionIdsListDontAdd() == null ? 0 : bean.getRegionIdsListDontAdd().size(); i<is; i++) {
			   OpArea area = bean.getRegionIdsListDontAdd().get(i);
			   if (area != null && area.thresholdMilliSec > 0) {
				   if (gapThreshAsPerOp < 0)
					   gapThreshAsPerOp = area.thresholdMilliSec;
				   else if (gapThreshAsPerOp > area.thresholdMilliSec)
					   gapThreshAsPerOp = area.thresholdMilliSec;
			   }
		   }
	   }
	   long gapThreshAsPerOpIfDir = bean.getThreshCompletionDirChangeMilliSec();
	   if (!Misc.isUndef(gapThreshAsPerOpIfDir) && !dirChangeProper && stopDirControl.getDoStopProcessing() >= 1) {
			//DEBUG13 .. orig is below .. above is try for hind - needs change of meaning of getThreshCompletionDirChangeMilliSec
		   // to getThreshCompletionIfNoDirChangeMilliSec
	   //if (!Misc.isUndef(gapThreshAsPerOpIfDir) && dirChangeProper)
		   gapThreshAsPerOp = gapThreshAsPerOpIfDir;
	   }
	   if (!this.isOpStationForStop())
		   gapThresh = gapThreshAsPerOp;
	   else if (gapThreshAsPerOp > gapThresh)
		   gapThresh = gapThreshAsPerOp;
	   if (stopDirControl != null && this.isOpStationForStop() && stopDirControl.getExtStopDurSecComplete()*1000 > gapThresh)
		   gapThresh = stopDirControl.getExtStopDurSecComplete()*1000;
		if (this.isOpStationForStop() || stopDirControl.isDoForwBackForRegularOp()) { //earlier was just for isOpStationForStop
			helpSetExtLookBackForw(retval, conn, vdp, stopDirControl, seqList, stItem, Misc.getUndefInt(), stIndex, hasEnded ? enItem : null, hasEnded ? enIndex : -1,-1,-1);
		}
		if (!this.isOpStationForStop())
			helpSetExtLookBackForwForMergeableOp(retval, conn, vdp, stopDirControl, seqList, Misc.getUndefInt(), stItem, false);
		long enTime = retval.getGateOut() > 0 ? retval.getGateOut() : vdp.getMaxTime();
		long gap = enTime - retval.getGateIn();
		boolean complete = gap > gapThresh && !isIgnoreAreaType(); //for ignoreAreaType
		
		   //changes for ignore opstation near landmarks & region specified by user
		if(!Misc.isUndef(stopDirControl.ignoreOpNearLandmarkTypes) && !isIgnoreAreaType() ){
			Pair<ShapeFileBean, Double> nearPointPair = RTreeSearch.getNearestNSpecialLM(stItem.getGpsData(conn, vdp).getPoint(), vehSetup.m_ownerOrgId, stopDirControl.ignoreLandmarkDistThresh, null, stopDirControl.ignoreOpNearLandmarkTypes);
			if(nearPointPair != null)
				complete = false;
		}
		if (complete) {
			if (stopDirControl.getExtStopDurSecComplete() > 0 && gap/1000 < stopDirControl.getExtStopDurSecComplete())
				complete = false;
		}
		
		if (complete && hackDoStrictSeqTimeMatch(stopDirControl) && enItem != null) {
			long threshMilli = hackGetAdjustedHackThreshAsMilli(stopDirControl, !hasEnded ,0);
			long gaploc = enItem.getTime() - stItem.getTime();
			if (gaploc < threshMilli)
				complete = false;
		}
		
		luInfoExtract.setOpComplete(true); //will be reupdated after getting extended gate in
		if (this.isDala() || this.getStrikeCount() > 1|| (this.getShovelAssignQuality() > 0))
			complete = true;
	   retval.setDirChanged(dirChange0123);
	   retval.setOpComplete(complete);
	   //CHANGE13 retval.refGateInEvent = retval.refWaitInEvent = stItem == null ? null : stItem.getGpsData();
	   //CHANGE13 retval.refGateOutEvent = retval.refWaitOutEvent = enItem == null ? null : enItem.getGpsData();
	   if (stopDirControl != null && stopDirControl.getDoStopProcessing() < 3 && this.isOpStationForStop())
		   retval.setOpComplete(false);
	 //  if (this.isOpStationForStop() && this.isIgnoreAreaType())
		 //  retval.setOpComplete(false);
		return retval;
		
	}
	public LUInfoExtract getLUInfoExtract(LUInfoExtract nextItemsLUInfo, boolean nextItemIsSameLoadType, boolean redo, CacheTrack.VehicleSetup vehSetup,
			long challanDate, int specificArea, int useSimpleApproach, StopDirControl stopDirControl, NewVehicleData vdp, Connection conn, FastList<LUSequence> seqList, int seqIndex, ManagementUnit mu, long prevBlkEndTS, long nextBlkStartTS) throws Exception { //useSimpleApproach = 0 => old Approach, 1 => has LUSeq after this .. so calc Outs, -1 => none after
		
		// this gets the best estimates of Win,Gin,Ain,Aout,Gout,Wout. It does so as follows
		// 1. get the best Ain/Aout. In case no Aout for the same area as Ain is possible then will look at nextIfSameLoadType and use that waitIn
		// 2. get earliest Gin before Ain.
		// 3. get earlies Win before Gin (if no Gin foundin 2 then Ain)
		// 4. if Gin is is null then same as Win and if that also null then Ain
		// 5 if Win is is null then same as Gin and if that also null then Ain
		// 6 Get Gout as first Gout after Aout and simlarly
		// 7 get Wout as first Wout aftet Gout (and no Gout then Aout)
		// 8 if no Wout found then take from Gout and if still null then take as waitin of next (& if that is null, then Aout)
		// 9 if no Gout then same as adjusted Wout of step 8

		// WILL Set reextraceDependenyUponNext if ever the need was felt to inquire the next sequence.

		// pass the extracted info from the next sequence in timeList if that is of same type, and pass true/false if that is of same Load/Unload type
		// else pass null if there is none after this

		// redo == true will cause re-finding of LU Info fields.

		// To make best use of this function - start from the last Item in the L/U sequential set. and note if it is dirty. do a getLUInfoExtract(null, true, false)
		// and use the dirty parameter of this on the previous L/U item's getLUInfoExtract's redo parameter

		// Updates for WB in/out etc.. Note that when finding Ain-Aout we 'merge' continous sequence of Ain-Aout of same if they are the only ones and correspond to same area
		// WB are incorporated as follows: We get the latest possible WB2, WB3. And will merge Ain-Aout so long as intervening items are WB's only.
		//
		// 
		//2014_06_14  if ((useSimpleApproach == 0 &&nextItemsLUInfo == null) || useSimpleApproach == -1)
			//2014_06_14 reextractDependentUponNext = true;
		CacheTrack.VehicleSetup.DistCalcControl distCalc = vehSetup != null ? vehSetup.getDistCalcControl(conn) : null;
		LUInfoExtract origExt = luInfoExtract;
		if (redo || this.isLUInfoReextractDirty())
			luInfoExtract = null;
		if (luInfoExtract != null &&!(luInfoExtract instanceof ExtLUInfoExtract)) {
			luInfoExtract = null;
		}
		if (luInfoExtract == null) {
			luInfoExtract = new ExtLUInfoExtract();
			luInfoExtract.setOfOpStationId(this.opStationId);
			luInfoExtract.setPrefOrMovingOpId(this.prefOrMovingOpStationId);
		}
		else {
			return luInfoExtract;
		}
		this.resetLUInfoReextractDirty();
		boolean hasWaitInsideDespEtc = false;
		
		if (isOpStationForStop()) {
			luInfoExtract = getLUInfoExtractOfStop(stopDirControl, useSimpleApproach == 1 || (useSimpleApproach == 0 && nextItemsLUInfo != null), seqList, conn, vdp, mu, origExt, vehSetup, prevBlkEndTS, nextBlkStartTS);
			return luInfoExtract;
		}
		GpsData refAreaInEvent = null;
		GpsData refAreaOutEvent = null;
		boolean amLoadType = isLoad();
		boolean pick1stArea = false;
		int firstAreaId = Misc.getUndefInt();
		Date firstAreaIn = null;
		Date firstAreaOut = null;
		boolean pickLastArea = false;
		boolean lookForChallanData = false;
		boolean foundChallanData = false;
		OpStationBean opStation = TripInfoCacheHelper.getOpStation(opStationId);
		boolean givePreferenceToDirChangeinArea = true;//TODO make it configurable ..?
		if (opStation != null) { // 1st pick from opstation definition
			if (opStation.m_bestAreaIsFirst)
				pick1stArea = true;
			else if (opStation.m_bestAreaIsLast)
				pickLastArea = true;
		}
		if (!pick1stArea && !pickLastArea && distCalc != null) {// if still default then pick from Org 
			if (amLoadType) {
				if (stopDirControl.isBestLoadFirst())
					pick1stArea = true;
				else if (stopDirControl.isBestLoadLast())
					pickLastArea = true;
			} else {
				if (stopDirControl.isBestUnloadFirst())
					pick1stArea = true;
				else if (stopDirControl.isBestUnloadFirst())
					pickLastArea = true;
			}
		}
		if (opStation != null) {
			lookForChallanData = opStation.m_lookForChallan;
		}
		
		//2014_06_14 reextractDependentUponNext = false; // will be set to true if we feel the need to look at next's item (regardless of that being there or not)
		// 1. get bfest Ain/Aout pos.
		// 2. if no Aout found then use next's Win as value
		// 3.
		if (seqIndex < 0)
			seqIndex = seqList.indexOf(this).first;
		boolean isLastSeq = seqIndex == seqList.size()-1;
		
		int currBestAinIndex = -1;
		int currBestAoutIndex = -1;
		long currBestAoutTime = Misc.getUndefInt(); // this will either be Aout's time or if Aout points to last entry and is really not Aout, then depending upon next's entry
		int currBestPriority = Misc.getUndefInt(); // this may actually point to Win of the next one!!
		long currBestTimeDiff = 0;
		boolean currBestHasMatchingWB1WB2 = false;
		int currBestOpCompleteThreshold = Misc.getUndefInt();
		int firstAinAt = -1;
		
		// getting - best Ain/Aout - the one with maximum duration of same opAreaId. We ignore sequence of
		//
		// int[] possibleWB1 = getPossibleWB1(); //not used currently
		
		int lastItemIndex = this.getEndLUItemIndex();
		int seqSize = lastItemIndex+1;
		LUItem lastItem = this.get(lastItemIndex);
		//CHANGE13 GpsData endData = useSimpleApproach == 0 ? (nextItemsLUInfo == null ? null : nextItemsLUInfo.refWaitInEvent) : useSimpleApproach == 1 ?  lastItem.getGpsData() : null;
		//CHANGE13 long endTime = endData == null ? Misc.getUndefInt() : endData.getGps_Record_Time();
		long endTime = lastItem.getTime();
		
		int[] possibleWB2 = getPossibleWB2(opStation, endTime);
		ArrayList<Pair<Integer, Integer>> wb1wb2Pairs = getMaximalMatchingWB1WB2(opStation, endTime);

		// We will skip over intermediate Ain/Aout ... but will stop skipping if there was a valid WB2 at beginning and somewhere in between we stop
		// finding valid WB2
		// get last WB ... if aout is found to be later than this then we consider last WB
		int lastWBInIndex = seqSize;

		for (int i = seqSize-1; i >= 0; i--) {
			LUItem item = get(i);
			if (item.isNonRegionEvent())
				continue;
			if (item.isAreaIn()) {
				hasWaitInsideDespEtc = !item.isNormal();
				if (item.isWB() && !item.isInsideWait()) {
					lastWBInIndex = helpGetBegOfWB(i);
				}
				break;
			}
		}
		boolean initFirstAreaIn = false;
		boolean initFirstAreaOut = false;
		LUItem bestDirChange = null;//this tells areawis
		int firstInsideWaitInIndex = -1;
		int lastInsideWaitInIndex = -1;
		LUItem bestDirChangeIgnArea = null;
		for (int i = 0, is = seqSize; i < is; i++) {
			LUItem item = get(i);
			if (item.isDirChange()) {
				if (bestDirChangeIgnArea == null || item.isMeBetterDirChange(bestDirChangeIgnArea) > 0)
					bestDirChangeIgnArea = item;
				continue;
			}
			if (item.isNonRegionEvent())
				continue;
			//CHANGE13 GpsData aoutData = null;
			boolean itemIsAreaIn = item.isAreaIn();
			if (itemIsAreaIn && item.isInsideWait()) {
				if (firstInsideWaitInIndex < 0)
					firstInsideWaitInIndex = i;
				lastInsideWaitInIndex = i;
				int j=i+1;
				for (;j<is;j++) {
					LUItem jitem = this.get(j);
					if (jitem.isNonRegionEvent())
						continue;
					if (jitem.isAreaIn() && jitem.getRegionId() == item.getRegionId())
						continue;
					if (jitem.isAreaOut()) {
						j=i+1;
					}
					break;
				}
				i = j-1;
				continue;
			}
			
			
			if (itemIsAreaIn && item.isNormal()) {
				//first Area In
				//first Area Out
				
				if ( !initFirstAreaIn ){
					//2014-06-20 	luInfoExtract.firstAreaIn = item.getTime();
					//2014-06-20 luInfoExtract.firstLoadAreaGuessId = item.getRegionId();
					//2014-06-20 luInfoExtract.firstAreaOut = item.getTime();
					initFirstAreaIn  = true;
				} 
				
				
				// go until we get to Area In/Area out of another area or non Area related event
				boolean existsValidWB2 = possibleWB2[i] != seqSize;
				Pair<Integer, Integer> wb1wb2 = wb1wb2Pairs.get(i);
					
				int maxwb2Index = wb1wb2 == null ? seqSize : wb1wb2.second;
				boolean thisHasMatchingWb1Wb2 = wb1wb2 != null && wb1wb2.first >= 0 && maxwb2Index < seqSize;
				if (firstAinAt < 0)
					firstAinAt = i;
				int currAreaId = item.getRegionId();
				int currOpCompleteThreshold = item.getThreshold(opStation);

				int j = i + 1;
				int breakOutIndex = -1;
				boolean lastWasWBIn = false;
				if (lastWBInIndex < j)
					lastWBInIndex = seqSize;
				LUItem areaBestDirChange = null;
				
				for (; j < is; j++) {
					
					if (j >= maxwb2Index || j >= lastWBInIndex)
						break;
					if (existsValidWB2 && possibleWB2[j] == seqSize)
						break;
					LUItem nextItem = get(j);
					LUItem dirChangeItem = nextItem.isDirChange() ? nextItem : null;
					int dirChange0123 = dirChangeItem == null ? 0 : LUItem.getDir0123(dirChangeItem.getEventId());
					boolean dirChangeProper = dirChange0123 > 0;
					if (dirChange0123 <= 0)
						dirChangeItem = null;
					if (dirChangeItem != null) {
						if (bestDirChangeIgnArea == null || dirChangeItem.isMeBetterDirChange(bestDirChangeIgnArea) > 0)
							bestDirChangeIgnArea = dirChangeItem;
					}
					int meDirBetter = 
									                           dirChangeItem != null && areaBestDirChange == null ? 1 : 
									                           dirChangeItem != null && areaBestDirChange != null ? dirChangeItem.isMeBetterDirChange(areaBestDirChange) : 
									                           dirChangeItem == null && areaBestDirChange != null ? -1 : 
									                           0;
			        if (meDirBetter == 1)
			        	areaBestDirChange = dirChangeItem;
					if (nextItem.isNonRegionEvent())
						continue;
					if (false && nextItem.hasMoved()) {// TO_VERIFY if works
						break;
					}
					if (!nextItem.isAreaIn() && !nextItem.isAreaOut()) {
						break;
					}
					
					if (nextItem.isAreaOut()) {
						breakOutIndex = j;
						continue;
					}
					if (nextItem.isAreaIn() && nextItem.isNormal() && nextItem.getRegionId() != currAreaId) {
						break;
					}
					if (nextItem.isAreaIn() && nextItem.isWB()) {
						lastWasWBIn = true; // not used
						continue;
					}

					if (nextItem.isAreaIn() && (nextItem.getRegionId() == currAreaId)) {
						breakOutIndex = -1;
						continue;
					}
				}
				int meDirBetter = 
					areaBestDirChange != null && bestDirChange == null ? 1 : 
					areaBestDirChange != null && bestDirChange != null ? areaBestDirChange.isMeBetterDirChange(bestDirChange) : 
					areaBestDirChange == null && bestDirChange != null ? -1 : 
                    0;
				if (breakOutIndex != -1)
					j = breakOutIndex;

				long currAin = get(i).getTime();
				long currAout = Misc.getUndefInt();
				if (j != is || !isLastSeq) {
					currAout = get(j == is ? j-1 : j).getTime();
					//CHANGE13 aoutData = get(j).getGpsData();
				} 
				
				//2014_06_14 if (j == is) {
				//2014_06_14 reextractDependentUponNext = true;
				//2014_06_14 }
				long tempEn = currAout;
				if (Misc.isUndef(currAout) && vdp != null)
					tempEn = vdp.getMaxTime();
				long currTimeDiff = !Misc.isUndef(currAin) && !Misc.isUndef(tempEn) ? tempEn - currAin : Misc.getUndefInt();
				if (Misc.isUndef(currTimeDiff))
					currTimeDiff = 100000000;// One lakh sec
				if (currTimeDiff < item.getThreshold(opStation)) {
					i = j - 1;
					continue;
				}
				// above ensures that bestAreaSelection will only happen if the timespent in the area exceeds threshold
				
				boolean toReplaceBest = false;
				
				if (currBestAinIndex == -1|| (thisHasMatchingWb1Wb2 && !currBestHasMatchingWB1WB2)) {
					toReplaceBest = true;
				}
				else if ((thisHasMatchingWb1Wb2 && currBestHasMatchingWB1WB2) || (!thisHasMatchingWb1Wb2 && !currBestHasMatchingWB1WB2)) {
					if (!Misc.isUndef(specificArea)  && specificArea == currAreaId  && !Misc.isUndef(challanDate) && item.getTime() < challanDate) {
						toReplaceBest = true;
					}
					else if (meDirBetter > 0) {
						toReplaceBest = true;
					}
					else if (meDirBetter == 0){
						if (pick1stArea) {// do nothing

						} else if ((pickLastArea && (Misc.isUndef(challanDate) || item.getTime() < challanDate))  ) {
							toReplaceBest = true;
						}
						else if (item.getPriority(opStation) < currBestPriority) {
							toReplaceBest = true;
						}
						else if (currTimeDiff > currBestTimeDiff) {
							toReplaceBest = true;
						}
					}
				}
				if (toReplaceBest) {
					currBestAinIndex = i;
					currBestPriority = item.getPriority(opStation);
					currBestAoutTime = currAout;
					currBestTimeDiff = currTimeDiff;
					currBestOpCompleteThreshold = currOpCompleteThreshold;
					//CHANGE13 refAreaInEvent = item.getGpsData();
					//CHANGE13 refAreaOutEvent = aoutData;
					currBestAoutIndex = j;
					if ( initFirstAreaIn && !initFirstAreaOut ) {
						//2014-06-20 luInfoExtract.firstAreaOut = currAout;//get(j).getTime();//item.getTime();
						initFirstAreaOut = true;//2014-06-20 ((item.getRegionId() != luInfoExtract.firstLoadAreaGuessId));
					}
					if (thisHasMatchingWb1Wb2)
						currBestHasMatchingWB1WB2 = true;
					bestDirChange = areaBestDirChange;
				}
				i = j - 1; // ++ happens at end of loop!!
			} else {
				
			}

		}
		
		// getting Gin ... 1st GateIn before Ain
		int ginIndex = -1;
		int winIndex = -1;
		int goutIndex = -1;
		int woutIndex = -1;
		//winIndex = 0;
		//ginIndex = 0;
		for (int i = 0, is = currBestAinIndex < 0 ? seqSize : currBestAinIndex+1; i < is; i++) {
			LUItem item = get(i);
			if (item.isNonRegionEvent())
				continue;
			if (item.isAreaIn()) {
				if (ginIndex < 0) 
					ginIndex = i;
				if (winIndex < 0)
					winIndex = i;
				break;
			}
			if (item.isGateIn() && ginIndex < 0) {
				ginIndex = i;
				if (winIndex < 0)
					winIndex = i;
			}
			if (item.isWaitIn()) {
				winIndex = i;
			}
			if (ginIndex >= 0)
				break;
		}
		if (currBestAoutIndex >= size())
			currBestAoutIndex = -1;
		if (ginIndex < 0)
			ginIndex = winIndex;
		if (ginIndex < 0)
			ginIndex =winIndex = this.getThisOrNextUsefulIndex(0);
		if (winIndex < 0)
			winIndex = ginIndex;
		int ltIndex = this.getEndLUItemIndex();
		LUItem ltItem = this.get(ltIndex);
		if (ltItem != null) {
			if (seqIndex != seqList.size()-1) {//not at last .. if last item is not gateout/waitout then waitOutTime = gateOutTime = lastTime
				woutIndex = ltIndex;
				if (ltItem.isGateOut() || !ltItem.isWaitOutOrConfirmatory())
					goutIndex = ltIndex;
				else {//last item is waitOut
					int prevLtItemIndex = this.getEndLUItemIndex(ltIndex);
					LUItem prevLtItem = this.get(prevLtItemIndex);
					if (prevLtItem == null || !prevLtItem.isGateOut())
						goutIndex = woutIndex;
					else
						goutIndex = prevLtItemIndex;
				}
			}
			else if (!(ltItem.isWaitOutOrConfirmatory() || ltItem.isGateOut())) {//at end last item is neither Gout Or Wout ... 
				woutIndex = goutIndex = -1;
			}
			else {//end is gout or wout
				OpStationBean opb = this.getOpBelogingBean();
				if (opb.getGateAreaId() == opb.getWaitAreaId())
					goutIndex = woutIndex = ltIndex;
				else {
					if (ltItem.isGateOut()) {
						goutIndex = ltIndex;
						woutIndex = -1;
					}
					else {
						woutIndex = ltIndex;
						int prevLtIndex = this.getEndLUItemIndex(ltIndex);
						LUItem prevLtItem = this.get(prevLtIndex);
						if (prevLtItem != null && !prevLtItem.isGateOut())
							goutIndex = woutIndex;
						else
							goutIndex = prevLtIndex;
					}
				}
			}
		}
		else {
			goutIndex = woutIndex = -1;
		}
		
		//CHANGE13 GpsData goutData = null;
		//CHANGE13 GpsData woutData = null;
		long woutDate = Misc.getUndefInt();
		long goutDate = Misc.getUndefInt();
		if (woutIndex < goutIndex)
			woutIndex = goutIndex;
		
		if (woutIndex >= 0) {
			woutDate = get(woutIndex).getTime();
			//CHANGE13 woutData = get(woutIndex).getGpsData();
		} 
		if (goutIndex >= 0) {
			goutDate = get(goutIndex).getTime();
			//CHANGE13 goutData = get(goutIndex).getGpsData();
		}
		// now fill value
		OpStationBean tempOps = TripInfoCacheHelper.getOpStation(opStationId);
		
		if (currBestAinIndex >= 0 && !isIgnoreAreaType()) {
			luInfoExtract.setAreaIn(get(currBestAinIndex).getTime());
			luInfoExtract.setAreaId(get(currBestAinIndex).getRegionId());
			luInfoExtract.setOpComplete(( currBestTimeDiff >= currBestOpCompleteThreshold));
			int materialId = Misc.getUndefInt();
			try {
				Integer materialIdInteger = tempOps == null ? null : tempOps.getMaterial(luInfoExtract.getAreaId());
				materialId = materialIdInteger != null ? materialIdInteger.intValue() : tempOps.getPrefMaterial();
			} catch (Exception e) {
				System.out.println("LUSequence.getLUInfoExtract() : opStationId :  " + opStationId + "  :  areaId :  " + luInfoExtract.getAreaId());
				e.printStackTrace();
			}
			luInfoExtract.setMaterialId(materialId);
			//2014-06-20 luInfoExtract.material = TripInfoCache.getMaterialCache().get(materialId);
			luInfoExtract.setAreaOut(currBestAoutTime);
			//CHANGE13 luInfoExtract.refAreaInEvent = refAreaInEvent;
			//CHANGE13 luInfoExtract.refAreaOutEvent = refAreaOutEvent;
		} else {
			luInfoExtract.setAreaIn(Misc.getUndefInt());
			luInfoExtract.setAreaId(Misc.getUndefInt());
			//2014-06-20 luInfoExtract.material = null;
			luInfoExtract.setMaterialId(tempOps.getPrefMaterial());
			luInfoExtract.setAreaOut(Misc.getUndefInt());;
			//CHANGE13 luInfoExtract.refAreaInEvent = null;
			//CHANGE13 luInfoExtract.refAreaOutEvent = null;
		}
		if (ginIndex >= 0) {
			luInfoExtract.setGateIn(get(ginIndex).getTime());
			//CHANGE13 luInfoExtract.refGateInEvent = get(ginIndex).getGpsData();
		}
		if (winIndex >= 0) {
			luInfoExtract.setWaitIn(get(winIndex).getTime());
			//CHANGE13 luInfoExtract.refWaitInEvent = get(winIndex).getGpsData();
		}
		luInfoExtract.setGateOut(goutDate);
		//CHANGE13 luInfoExtract.refGateOutEvent = goutData;
		luInfoExtract.setWaitOut(woutDate);
		//CHANGE13 luInfoExtract.refWaitOutEvent = woutData;
			//long gap = item.getTime() - insideWaitAin.getTime();
			//if (gap > item.getThreshold(opStation))
			//	insideWaitAout = item;
		
		
		// TODO to associate trip id with luInfoExtractor
		//$$luInfoExtract.tripId = this.tripId;
		if (stopDirControl != null && stopDirControl.getDoStopProcessing() > 0 && stopDirControl.isDoForwBackForRegularOp())
			helpSetExtLookBackForw(luInfoExtract, conn, vdp, stopDirControl, seqList, get(ginIndex), Misc.getUndefInt(), ginIndex, get(goutIndex), goutIndex, prevBlkEndTS, nextBlkStartTS);
		helpSetExtLookBackForwForMergeableOp(luInfoExtract, conn, vdp, stopDirControl, seqList, Misc.getUndefInt(), get(winIndex), false);

		if (!isIgnoreAreaType() && (!luInfoExtract.isOpComplete() || (Misc.isUndef(luInfoExtract.getAreaId()) && !Misc.isUndef(luInfoExtract.getGateIn())))){
			long tempEn = luInfoExtract.getGateOut();
			if (Misc.isUndef(tempEn) && vdp != null)
					tempEn = vdp.getMaxTime();
			if (Misc.isUndef(tempEn)) {
				luInfoExtract.setOpComplete(true); // assume ... there foreever
				//alternate approach, if op_station has minimum opertaing threshold in that case cannot assume  
				//you to be standing there forever. So, in case OpStation has minimum threshold will check the gps_record_time diff
//					OpStationBean tempOps = TripInfoCache.getOpStation(opStationId);
//					luInfoExtract.isOpComplete = ( this.getEndTime().getTime() - luInfoExtract.gateIn.getTime()) > tempOps.getIntVal(5);
			}
			else{
			// estimate from max threshold of region
				
				if (tempOps != null) {
					int mxTh = tempOps.getMaxOpThreshold();
					if (luInfoExtract.getGateOut() < 0 && mxTh > 600000)
						mxTh = 600000;
					long tdiff = tempEn - luInfoExtract.getGateIn();
					luInfoExtract.setOpComplete(tdiff >= mxTh);
				}
			}
		}
		
		// now populate WB1,WB2,WB3
		if (true)
			helpPopulateWB(opStation, luInfoExtract, currBestAinIndex, currBestAoutIndex, possibleWB2, wb1wb2Pairs);
		
		if (firstInsideWaitInIndex >= 0) {
			LUItem insideWait = this.get(firstInsideWaitInIndex);
			long cmp = luInfoExtract.getAreaIn();
			if (luInfoExtract.getWb1In() > 0 && (cmp <= 0 || luInfoExtract.getWb1In() < cmp))
				cmp = luInfoExtract.getWb1In();
			if (insideWait.getTime() <= cmp || cmp <= 0) {
				int j=firstInsideWaitInIndex+1;
				boolean inWait = true;
				int endIndex = -1;
				for(;j<seqSize;j++) {
					LUItem jitem = this.get(j);
					if (jitem.isNonRegionEvent())
						continue;
					if (jitem.isAreaIn() && jitem.isInsideWait()) {
						inWait = true;
					}
					if (jitem.isAreaOut() && inWait) {
						inWait = false;
						endIndex = j;
						break;
					}
					if (endIndex < 0)
						endIndex = j;
					if (jitem.getTime() >= cmp && cmp > 0)
						break;
				}
				long insideWout = endIndex >= 0 ? this.get(endIndex).getTime() : cmp > 0 ? cmp : luInfoExtract.getLatestEventDateTime();
				luInfoExtract.setGateIn(insideWout);
			}//firstInside before 1st WB or Ain
		}
		if (lastInsideWaitInIndex > firstInsideWaitInIndex) {
			LUItem insideWait = this.get(lastInsideWaitInIndex);
			long cmp = luInfoExtract.getAreaOut();
			if (luInfoExtract.getWb2Out() > 0 && (cmp <= 0 || luInfoExtract.getWb2Out() > cmp))
				cmp = luInfoExtract.getWb2Out();
			if ((insideWait.getTime() >= cmp && cmp > 0) || (cmp < 0 && firstInsideWaitInIndex != lastInsideWaitInIndex)) {
				luInfoExtract.setGateOut(insideWait.getTime());
			}//firstInside before 1st WB or Ain
		}
		if (false && hasWaitInsideDespEtc) {
			helpPopulateInsideWaitEtc(luInfoExtract);
		}
		int dirChange =  (bestDirChange != null  && !LUItem.isSuppressedEvent(bestDirChange.getEventId()) && bestDirChange.getEventId() != TripInfoConstants.DIR_CHANGE_EVENT_NONE) ? bestDirChange.getDir0123(bestDirChange.getEventId()) 
				: bestDirChangeIgnArea != null ? bestDirChangeIgnArea.getDir0123(bestDirChangeIgnArea.getEventId())
				: getDirChange(-1,-1,null);
		luInfoExtract.setDirChanged(dirChange);
		return luInfoExtract;
	}
	
	public void helpPopulateInsideWaitEtc(LUInfoExtract luInfoExtract) {
		
	}
	public void setAsPerDisposition(int disposition) {
		if (disposition == 0) {
			return;
		}
		boolean amLoad = isLoad();
		boolean newIsLoad = disposition == 1 || disposition == 3;
		if (this.isOpStationForStop()) {
			this.setStopLoadType(disposition == 1 ? 0 : 1, null);
			return;
		}
		if (amLoad != newIsLoad) {
			for (int q = 0; q < size() ; q++) {
				LUItem item = get(q);
				item.flip(amLoad);
			}
		}
	}
	public Pair<Integer, Boolean> indexOfUseful(LUItem compVal) {
		return indexOfUseful(compVal, false);
	}
	
	public Pair<Integer, Boolean> indexOfUseful(LUItem compVal, boolean strictlyLess) {
		Pair<Integer, Boolean> idx = indexOf(compVal);
		int index = idx.first;
		int origIndex = index;
		if (strictlyLess && idx.second)
			index--;
		boolean secVal = idx.second;
		if (this.isOpStationForStop()) {
			for (;index >= 0; index--) {
				LUItem temp = get(index);
				if (temp == null ||temp.isStop())
					break;
			}
		}
		else {
			for (;index >= 0; index--) {
				LUItem temp = get(index);
				if (temp == null ||! temp.isNonRegionEvent())
					break;
			}
		}
		return index == origIndex ? idx : new Pair<Integer, Boolean>(index, false); //just trying to avoid creation of unnecessary objects
	}
	
	public LUItem getPrevUseful(LUItem compVal) {
		return getPrevUseful(compVal, false);
	}
	
	public LUItem getPrevUseful(LUItem compVal, boolean strictlyLess) {
		Pair<Integer, Boolean> idx = indexOfUseful(compVal, strictlyLess);
		return get(idx.first);
	}
	
	public LUItem getPrevUseful(int index) {
		return get(getThisOrPrevUsefulIndex(index));
	}
	
	public int getThisOrNextUsefulIndex(int curr) {
		if (this.isOpStationForStop()) {
			for (int is=size();curr<is;curr++) {
				LUItem temp = get(curr);
				if (temp.isStop())
					break;
			}
			return curr;
		}
		for (int is = size();curr<is;curr++) {
			LUItem temp = get(curr);
			if (!temp.isNonRegionEvent())
				break;
		}
		return curr;
	}
	public int getThisOrPrevRegionIndex(int curr) {
		for (;curr>=0;curr--){
			LUItem temp = get(curr);
			if (!temp.isNonRegionEvent())
				return curr;;
		}
		return -1;
	}
	
	public LUItem getThisOrPrevRegionItem(int curr){
		return get(getThisOrPrevRegionIndex(curr));
	}
	public int getThisOrPrevUsefulIndex(int curr) {
		if (this.isOpStationForStop()) {
			for (;curr>=0;curr--) {
				LUItem temp = get(curr);
				if (temp.isStop())
					break;
			}
			return curr;
		}
		for (;curr >= 0;curr--) {
			LUItem temp = get(curr);
			if (temp.isStopStartExt() || !temp.isNonRegionEvent())
				break;
		}
		return curr;
	}
	
	public boolean isBetterThan(Connection conn, LUSequence rhs, ShiftPlanInfo.ShiftLookupInfo shiftInfo, int vehicleId, long atTime, boolean bestLUIsValidExtremum,
			CacheTrack.VehicleSetup.DistCalcControl distCalc, boolean pickFurthest, LUInfoExtract prior, ArrayList<ChallanInfo> priorChallanUniqueDest
			, GpsData priorPt, GpsData mePt, GpsData rhsPt, int prefForFixedLoad, int prefForFixedUnload
			, StopDirControl stopDirControl, int doSpecialAlgo, Pair<ChallanInfo, ChallanInfo> meRhsBestChallanInfoMatch
			, ArrayList<ChallanInfo> currentChallanList, int priorMaterial, boolean challanIsLikeUnload, NewMU newmu
			,IsBetterThanChallanRelatedResult addnlChallanInfo) throws Exception {// assumes getLUInfoExtract has been properly called earlier
		//NOTE: currentChallanList will be null if doing for unloadOp
		//NOTE: priorChallan will be null if doing for loadOp
		
		// the one that has better Ain/Aout gap. If neither has Ain/Aout gap, then the one that is of longer duration
		
		//StringBuilder dbgsb = null;//new StringBuilder();
		//if (dbgsb != null) {
		//	dbgsb.append("CHECKBEST:").append(new java.util.Date(this.getStartTimeExt())).append(" Temp:").append((new java.util.Date(rhs.getStartTimeExt())));
		//	System.out.println(dbgsb);
		//}
		double absRangeHi = 30;//TODO parameterize
		double absRangeLo = 10; //TODO paramterize
		double absRangeWithDir = 40;//TODO parameterize
		double absTravelDistGapReInvoiceDistKmWhenOK = 100;
		double hiRelativeTravelDistGapReInvoiceDistKmWhenOK  = 2;
		double loRelativeTravelDistGapReInvoiceDistKmWhenOK  = 0.33;
		
		boolean doMultiLoad = stopDirControl.isDoMultiLoadMultiLU();
		boolean doMultiUnload = stopDirControl.isDoMultiUnloadMultiLU();
		boolean multiFurthest = false && pickFurthest &&(doMultiUnload || doMultiLoad);
		if (meRhsBestChallanInfoMatch != null) {
			meRhsBestChallanInfoMatch.first = null;
			meRhsBestChallanInfoMatch.second = null;
		}
		boolean thisOp = true;
		boolean rhsOp = true;
		if (Misc.isUndef(atTime)) {
			atTime = this.getStartTimeExt();
		}
		if (shiftInfo != null && shiftInfo.shiftPlan != null) {
			thisOp = shiftInfo.shiftPlan.isOpStationOperational(this.opStationId, vehicleId, atTime);
			rhsOp = shiftInfo.shiftPlan.isOpStationOperational(rhs.getOpStationId(), vehicleId, atTime);
		}
		if (thisOp != rhsOp)
			return thisOp;
		
		LUInfoExtract myExtract = getLUInfoExtractPlain();
		LUInfoExtract rhsExtract = rhs.getLUInfoExtractPlain();
		int lhsHasDirChange = myExtract.getDirChanged();
		int rhsHasDirChange = rhsExtract.getDirChanged();
		boolean meDalaRaise = this.isDala();
		int meStrikeCount = this.getStrikeCount();
		boolean rhsDalaRaise = rhs.isDala();
		int rhsStrikeCount = rhs.getStrikeCount();
		int meShovelQuality = this.getShovelAssignQuality();
		int rhsShovelQuality = rhs.getShovelAssignQuality();
		boolean meShovelQProper = this.isQualityIsProper();
		boolean rhsShovelQProper = rhs.isQualityIsProper();
		int meShovelId = this.getShovelId();
		int rhsShovelId = rhs.getShovelId();
		boolean meShovelMatchAssigned = meShovelId == this.getManualAssignedShovelId();
		boolean rhsShovelMatchAssigned = rhsShovelId == rhs.getManualAssignedShovelId();
		boolean meIgnoreShovel = false;
		boolean rhsIgnoreShovel = false;
		boolean rhsEnded = rhs.getStopEndIndex() >= 0;
		if (meShovelId <= 0) {
			meIgnoreShovel = true;
			meShovelQProper = false;
			meShovelQuality = 0;
		}
		if (rhsShovelId <= 0) {
			rhsIgnoreShovel = true;
			rhsShovelQProper = false;
			rhsShovelQuality = 0;
		}
		if (meShovelQProper != rhsShovelQProper) {
			if (!meShovelQProper) 
				meIgnoreShovel = true;
			if (!rhsShovelQProper)
				rhsIgnoreShovel = true;
		}
		if (!rhsShovelMatchAssigned && !rhsEnded) {//stop ongoing ... to
			if (rhs.getShovelCycleCount() < ShovelSequence.MIN_SHOVEL_LO_CYCLES)
				rhsIgnoreShovel = true;
		}
		byte meCycle = this.getShovelCycleCount();
		byte rhsCycle = rhs.getShovelCycleCount();
		if (meIgnoreShovel) {
			meStrikeCount = 0;
			meShovelQuality = 0;
			meCycle = 0;
		}
		if (rhsIgnoreShovel) {
			rhsStrikeCount = 0;
			rhsShovelQuality = 0;
			rhsCycle = 0;
		}
		if (meStrikeCount > 4)
			meStrikeCount = 4;
		if (rhsStrikeCount > 4)
			rhsStrikeCount = 4;
		byte lhsArtOpStationMatch = myExtract.getArtOpStationMatch();
		if (lhsArtOpStationMatch <= 0)
			lhsArtOpStationMatch = 0;
		byte rhsArtOpStationMatch = myExtract.getArtOpStationMatch();
		if (rhsArtOpStationMatch <= 0)
			rhsArtOpStationMatch = 0;
		boolean isMyOpComplete = myExtract.isOpComplete();
		boolean isRhsOpComplete =  rhsExtract.isOpComplete();
		OpStationBean opStationMe = this.getOpBelogingBean();//TripInfoCache.getOpStation(this.opStationId);
		OpStationBean opStationRhs = rhs.getOpBelogingBean();//TripInfoCache.getOpStation(rhs.getOpStationId());
	//	if ((opStationMe != null && opStationRhs != null) && (opStationMe.getOpStationId() == 74 && opStationRhs.getOpStationId() == 391)
	//			|| ((opStationMe.getOpStationId() == 361 || opStationMe.getOpStationId() == 362 || opStationMe.getOpStationId() == 530) && (opStationRhs.getOpStationId() == 391 || opStationRhs.getOpStationId() == 74))
	//			) {
	//		int dbg = 1;
	//		dbg++;
	//	}
		
		
		boolean meMatchFromChallan = false;
		boolean rhsMatchFromChallan = false;
		long meRefChallanDate = Misc.getUndefInt();
		long rhsRefChallanDate = Misc.getUndefInt();
		
		long meBeg = this.getStartTimeExt();
		long meEnd = this.getEndTime();
		long rhsBeg = rhs.getStartTimeExt();
		long rhsEnd = rhs.getEndTime();
		
		
		if (isMyOpComplete == isRhsOpComplete) {
			if (!Misc.isUndef(priorMaterial)) {
				
				int meMaterial = opStationMe == null ? Misc.getUndefInt() : opStationMe.getPrefMaterial();
				int rhsMaterial = opStationRhs == null ? Misc.getUndefInt() : opStationRhs.getPrefMaterial();
				
				boolean meMatMatch = priorMaterial == meMaterial || priorMaterial <= 0 || meMaterial <= 0;;
				boolean rhsMatMatch = priorMaterial == rhsMaterial || priorMaterial <= 0 || rhsMaterial <= 0;;
				if (meMatMatch != rhsMatMatch)
					return meMatMatch;
			}
			boolean meIsIntermediate = this.isIntermediate();
			boolean rhsIsIntermediate = rhs.isIntermediate();
			if (meIsIntermediate != rhsIsIntermediate) {
				return !meIsIntermediate;
			}
			
		}
		// Test code - rajeev sir
		// if (rhsExtract.waitOut == null || (myExtract.waitIn != null && myExtract.waitOut != null && (myExtract.waitOut.getTime()-myExtract.waitIn.getTime()) < 20*60*1000))
		// return false;
		if (false && doSpecialAlgo == 2) {//gevra
			
			if (priorPt != null && mePt != null && rhsPt != null) {
				Pair<Double, Double> intersectPt = RouteDef.checkWhereInSegment(mePt.getLongitude(), mePt.getLatitude(), priorPt.getLongitude(), priorPt.getLatitude(), rhsPt.getLongitude(), rhsPt.getLatitude());
				if (intersectPt.first > -0.001 && intersectPt.second < 1.001) { //me between prior and rhs .. rhs is better
					return false;
				}
				else {
					return true;
				}
				
			}
			
		}
		AddnlMiningInfo lhsMiningInfo = this.getMiningInfo();
		AddnlMiningInfo rhsMiningInfo = rhs.getMiningInfo();
		int lhsMiningCmp = lhsMiningInfo == null ? 0 : lhsMiningInfo.getScoreForComp(this.isLoad());
		int rhsMiningCmp = rhsMiningInfo == null ? 0 : rhsMiningInfo.getScoreForComp(this.isLoad());
		if (doSpecialAlgo == 2 || doSpecialAlgo == 1) {
			if (lhsMiningCmp != rhsMiningCmp)
				return lhsMiningCmp > rhsMiningCmp;
		}
		
		if (doSpecialAlgo == 2) { //for mining
			
			
			boolean meRestOrIgnore = this.isRestAreaType() || this.isIgnoreAreaType();
			boolean rhsRestOrIgnore = rhs.isRestAreaType() || rhs.isIgnoreAreaType();
			if (meRestOrIgnore != rhsRestOrIgnore)
				return !meRestOrIgnore;
			if (!this.isLoad() && meDalaRaise != rhsDalaRaise) {
				return meDalaRaise;
			}
			if (this.isLoad() && !meIgnoreShovel && meShovelQProper != rhsShovelQProper) {
				return meShovelQProper;
			}
			if (this.isLoad() && meShovelQuality != rhsShovelQuality) {
				return meShovelQuality > rhsShovelQuality;
			}
			
			if (this.isLoad() && meStrikeCount != rhsStrikeCount) {
				return meStrikeCount > rhsStrikeCount;
			}
			
			boolean areAtSimilarDistFromLoad = false;
			int mePriority = opStationMe == null || this.isOpStationForStop() ? 0 : opStationMe.getIntVal(11);
			int rhsPriority = opStationRhs == null || rhs.isOpStationForStop() ? 0 : opStationRhs.getIntVal(11);
			if (mePriority < 0)
				mePriority  = 0;
			if (rhsPriority < 0)
				rhsPriority = 0;
			if (mePriority != rhsPriority)
				return mePriority > rhsPriority;
			boolean meNotStopOrLinked = !this.isOpStationForStop() || !Misc.isUndef(opStationMe.getLinkedVehicleId());
			boolean rhsNotStopOrLinked = !rhs.isOpStationForStop() || !Misc.isUndef(opStationRhs.getLinkedVehicleId());
			
			if (lhsHasDirChange != rhsHasDirChange) {
				return lhsHasDirChange > rhsHasDirChange;
			}
			//check if mePt in between priorPt and rhsPt
			if (priorPt != null && mePt != null && rhsPt != null) {
				Pair<Double, Double> intersectPt = RouteDef.checkWhereInSegment(mePt.getLongitude(), mePt.getLatitude(), priorPt.getLongitude(), priorPt.getLatitude(), rhsPt.getLongitude(), rhsPt.getLatitude());
				if (intersectPt.first > -0.001 && intersectPt.second < 1.001) {
					return false;
				}
				else {
					return true;
				}
				
			}
			return false; //give pref to later
		    /*
			double d1 = Misc.getUndefDouble();
			double d2 = Misc.getUndefDouble();
			if (priorPt != null && mePt != null && rhsPt != null && !challanByDeliveryDate) {
				d1 = priorPt.distance(mePt.getLongitude(),mePt.getLatitude());
			    d2 = priorPt.distance(rhsPt.getLongitude(),rhsPt.getLatitude());
			}
			
			if (!Misc.isEqual(d1,0) && !Misc.isEqual(d2, 0)) {
				double ratio = d1 < d2 ? d1/d2 : d2/d1;
				areAtSimilarDistFromLoad = ratio >= 0.90;
			}
			if (!areAtSimilarDistFromLoad) {
				if (lhsHasDirChange != rhsHasDirChange) {
					//	if (dbgsb != null) {dbgsb.append("return ").append(lhsHasDirChange > rhsHasDirChange).append(" because diff dir:").append(lhsHasDirChange).append(",").append(rhsHasDirChange); System.out.println(dbgsb);}
						return lhsHasDirChange > rhsHasDirChange;
				}
				else {
					return d1 > d2; 
				}
			}
			else {
				//check if any of these linked to fixed or moving vehicle
				//atleast has dir change
				if (lhsHasDirChange != rhsHasDirChange) {
					if (lhsHasDirChange == 0)
						return false;
					else if (rhsHasDirChange == 0)
						return true;
				}
				boolean meNotStopOrLinked = !this.isOpStationForStop() || !Misc.isUndef(opStationMe.getLinkedVehicleId());
				boolean rhsNotStopOrLinked = !rhs.isOpStationForStop() || !Misc.isUndef(opStationRhs.getLinkedVehicleId());
				if (meNotStopOrLinked != rhsNotStopOrLinked) {
					return meNotStopOrLinked;
				}
				if (lhsHasDirChange != rhsHasDirChange)
					return lhsHasDirChange > rhsHasDirChange;
			   return false; //give preference to later one ..	
			}
			*/
		}
		
		if (doSpecialAlgo == 1 || doSpecialAlgo == 2) { //for mining
			boolean meRestOrIgnore = this.isRestAreaType() || this.isIgnoreAreaType();
			boolean rhsRestOrIgnore = rhs.isRestAreaType() || rhs.isIgnoreAreaType();
			if (meRestOrIgnore != rhsRestOrIgnore)
				return !meRestOrIgnore;
			if (!this.isLoad() && meDalaRaise != rhsDalaRaise) {
				return meDalaRaise;
			}
			if (this.isLoad() && meShovelQProper != rhsShovelQProper) {
				return meShovelQProper;
			}
			if (this.isLoad() && meShovelQuality != rhsShovelQuality) {
				return meShovelQuality > rhsShovelQuality;
			}
			if (this.isLoad() && meShovelQuality > 0 && rhsShovelQuality > 0) {
				
				//if (meCycle != rhsCycle) //20190130
				//	return meCycle > rhsCycle;
				if (rhsCycle >= ShovelSequence.MIN_SHOVEL_CYCLES && meCycle < ShovelSequence.MIN_SHOVEL_CYCLES)
					return false;
				else if (rhsCycle < ShovelSequence.MIN_SHOVEL_CYCLES && meCycle >= ShovelSequence.MIN_SHOVEL_CYCLES)
					return true;
				else if (meCycle >= ShovelSequence.MIN_SHOVEL_CYCLES && rhsCycle >= ShovelSequence.MIN_SHOVEL_CYCLES 
						//meCycle > 0 && rhsCycle > 0
						) {
					//give pref to one on right
					return false;
				}
				else if (rhsCycle > 0 && !rhsEnded)
					return false;
			}
			if (this.isLoad() && meCycle == 0 && rhsCycle == 0 && meStrikeCount != rhsStrikeCount) {
				return meStrikeCount > rhsStrikeCount;
			}
			boolean areAtSimilarDistFromLoad = false;
			boolean meNotStopOrLinked = !this.isOpStationForStop() || !Misc.isUndef(opStationMe.getLinkedVehicleId());
			boolean rhsNotStopOrLinked = !rhs.isOpStationForStop() || !Misc.isUndef(opStationRhs.getLinkedVehicleId());
			double d1 = Misc.getUndefDouble();
			double d2 = Misc.getUndefDouble();
			if (priorPt != null && mePt != null && rhsPt != null) {
				d1 = priorPt.distance(mePt.getLongitude(),mePt.getLatitude());
			    d2 = priorPt.distance(rhsPt.getLongitude(),rhsPt.getLatitude());
			}
			
			if (!Misc.isEqual(d1,0) && !Misc.isEqual(d2, 0)) {
				double ratio = d1 < d2 ? d1/d2 : d2/d1;
				areAtSimilarDistFromLoad = ratio >= 0.85 || Math.abs(d1-d2) <= stopDirControl.getDirChangeDetectThreshKMHi();
			}
			if (!areAtSimilarDistFromLoad) {
				return d1 > d2;
			}
			else {
				if (lhsHasDirChange != rhsHasDirChange || meNotStopOrLinked != rhsNotStopOrLinked || lhsArtOpStationMatch != rhsArtOpStationMatch) {
					if (meNotStopOrLinked != rhsNotStopOrLinked && lhsHasDirChange != 0 && rhsHasDirChange != 0 && mePt != null && rhsPt != null) {
						//in case turn happens elsewhere but otherwise the two are close to each other
						double d = stopDirControl.isDoTravelDistanceInsteadOfGeo() ? (rhsPt.getValue() - mePt.getValue()) : mePt.distance(rhsPt.getLongitude(), rhsPt.getLatitude());
						if (d <= stopDirControl.getDirChangeDetectThreshKMHi())
							return meNotStopOrLinked;
					}
					if (lhsHasDirChange != rhsHasDirChange)
						return lhsHasDirChange > rhsHasDirChange;
					if (lhsArtOpStationMatch != rhsArtOpStationMatch) {
						if (TripInfoConstants.g_recordImpactOfArt) {
							System.out.println("[MU_DBG] Choosing better because of match: LHS:"+this+" RHS:"+rhs);
						}
						return lhsArtOpStationMatch > rhsArtOpStationMatch;
					}
				}
			}
			//check if mePt in between priorPt and rhsPt
			if (priorPt != null && mePt != null && rhsPt != null) {
				Pair<Double, Double> intersectPt = RouteDef.checkWhereInSegment(mePt.getLongitude(), mePt.getLatitude(), priorPt.getLongitude(), priorPt.getLatitude(), rhsPt.getLongitude(), rhsPt.getLatitude());
				if (intersectPt.first > -0.001 && intersectPt.second < 1.001) {
					return false;
				}
				else {
					return true;
				}
				
			}
			return false; //give pref to later
		    /*
			double d1 = Misc.getUndefDouble();
			double d2 = Misc.getUndefDouble();
			if (priorPt != null && mePt != null && rhsPt != null && !challanByDeliveryDate) {
				d1 = priorPt.distance(mePt.getLongitude(),mePt.getLatitude());
			    d2 = priorPt.distance(rhsPt.getLongitude(),rhsPt.getLatitude());
			}
			
			if (!Misc.isEqual(d1,0) && !Misc.isEqual(d2, 0)) {
				double ratio = d1 < d2 ? d1/d2 : d2/d1;
				areAtSimilarDistFromLoad = ratio >= 0.90;
			}
			if (!areAtSimilarDistFromLoad) {
				if (lhsHasDirChange != rhsHasDirChange) {
					//	if (dbgsb != null) {dbgsb.append("return ").append(lhsHasDirChange > rhsHasDirChange).append(" because diff dir:").append(lhsHasDirChange).append(",").append(rhsHasDirChange); System.out.println(dbgsb);}
						return lhsHasDirChange > rhsHasDirChange;
				}
				else {
					return d1 > d2; 
				}
			}
			else {
				//check if any of these linked to fixed or moving vehicle
				//atleast has dir change
				if (lhsHasDirChange != rhsHasDirChange) {
					if (lhsHasDirChange == 0)
						return false;
					else if (rhsHasDirChange == 0)
						return true;
				}
				boolean meNotStopOrLinked = !this.isOpStationForStop() || !Misc.isUndef(opStationMe.getLinkedVehicleId());
				boolean rhsNotStopOrLinked = !rhs.isOpStationForStop() || !Misc.isUndef(opStationRhs.getLinkedVehicleId());
				if (meNotStopOrLinked != rhsNotStopOrLinked) {
					return meNotStopOrLinked;
				}
				if (lhsHasDirChange != rhsHasDirChange)
					return lhsHasDirChange > rhsHasDirChange;
			   return false; //give preference to later one ..	
			}
			*/
		}
		if (isMyOpComplete && !isRhsOpComplete) {
			//if (dbgsb != null) {dbgsb.append("return true because me comp, rhs not comp"); System.out.println(dbgsb);}
			return true;
		}
		else if (!isMyOpComplete && isRhsOpComplete) {
			//if (dbgsb != null) {dbgsb.append("return false because me not comp, rhs comp"); System.out.println(dbgsb);}
			return false;
		}
		if (addnlChallanInfo != null) {
			addnlChallanInfo.challanIsAtOp = false;
			addnlChallanInfo.challanIsValid = false;
			addnlChallanInfo.meChallanMatches = false;
			addnlChallanInfo.rhsChallanMatches = false;
			addnlChallanInfo.meDistRelInvoiceOK = true;
			addnlChallanInfo.rhsDistRelInvoiceOK = true;
			addnlChallanInfo.meFixed = false;
			addnlChallanInfo.rhsFixed = false;
			addnlChallanInfo.meCloserThanAbsLo = false;
			addnlChallanInfo.rhsCloserThanAbsLo = false;
		}
		double d1 = Misc.getUndefDouble();
		double d2 = Misc.getUndefDouble();
		boolean meInRange = false;
		boolean rhsInRange = false;
		double nearestMeChallanDist = Misc.LARGE_NUMBER; //this is under some condition of being within range
		double nearestRHSChallanDist = Misc.LARGE_NUMBER; //this is under some condition of being within range
		double simpleMeChallanDist = Misc.getUndefDouble();
		double simpleRHSChallanDist = Misc.getUndefDouble();
		this.isChallanMarked = -1;
		double g1 = Double.MAX_VALUE;
		double g2 = Double.MAX_VALUE;
		boolean meMatchesChallanAtOp = false;
		boolean rhsMatchesChallanAtOp = false;
		boolean challanIsAtOp = false;
		boolean challanDestValid = false;
		double invoiceDistKM = Misc.getUndefDouble();
		double meTravelDist = Misc.getUndefDouble();
		double rhsTravelDist = Misc.getUndefDouble();
		if (priorPt != null && mePt != null && rhsPt != null && !challanIsLikeUnload) {
			d1 = priorPt.distance(mePt.getLongitude(),mePt.getLatitude());
		    d2 = priorPt.distance(rhsPt.getLongitude(),rhsPt.getLatitude());
		    meTravelDist = mePt.getValue() - priorPt.getValue();
		    rhsTravelDist = rhsPt.getValue() - priorPt.getValue();
		    for (int l1=0,l1s = priorChallanUniqueDest == null ? 0 : priorChallanUniqueDest.size(); l1<l1s;l1++) {
		    	ChallanInfo priorChallan = priorChallanUniqueDest.get(l1);
		    	double currInvoiceDistkm = priorChallan == null ? Misc.getUndefDouble() : priorChallan.getInvoiceDistKM(conn, stopDirControl);
		    	if (!Misc.isUndef(currInvoiceDistkm) && (Misc.isUndef(invoiceDistKM) || invoiceDistKM < currInvoiceDistkm)) {
		    		invoiceDistKM = currInvoiceDistkm;
		    	}
		    	
		    	IdInfo targetDest = priorChallan == null ? null : priorChallan.getIdInfoWithCalc(conn, true, stopDirControl);
		    	if (targetDest != null && targetDest.getLongitude() > 0) {
		    		challanDestValid = true;
		    		if (addnlChallanInfo != null)
		    			addnlChallanInfo.challanIsValid = true;
		    	}
		    	double targetDist = targetDest != null && targetDest.getLongitude() > 0 ? priorPt.distance(targetDest.getLongitude(), targetDest.getLatitude()):Misc.getUndefDouble() ;
		    	if (targetDist > 0) {
		    		double r1 = d1/targetDist;
		    		double r2 = d2/targetDist;
		    		double meLowRange =  absRangeLo;//lhsHasDirChange > 0 ? absRangeWithDir : absRangeLo;
		    		double rhsLowRange =  absRangeLo;//rhsHasDirChange > 0 ? absRangeWithDir : absRangeLo;
				
		    		g1 = mePt.distance(targetDest.getLongitude(), targetDest.getLatitude());
		    		g2 = rhsPt.distance(targetDest.getLongitude(), targetDest.getLatitude());
		    		if (targetDest.getDestIdType() == 3) {
		    			challanIsAtOp = true;
			    		if (addnlChallanInfo != null)
			    			addnlChallanInfo.challanIsAtOp = true;
		    		}
		    		else {
		    			challanIsAtOp = false;
		    		}
		    		if (targetDest.getDestIdType() == 3 && targetDest.getDestId() == opStationMe.getOpStationId()) {
		    			g1 = 0;
		    			meMatchesChallanAtOp = true;
		    			if (addnlChallanInfo != null)
		    				addnlChallanInfo.meChallanMatches = true;
		    		}
		    		if (targetDest.getDestIdType() == 3 && targetDest.getDestId() == opStationRhs.getOpStationId()) {
		    			g2 = 0;
		    			rhsMatchesChallanAtOp = true;
		    			if (addnlChallanInfo != null)
		    				addnlChallanInfo.rhsChallanMatches = true;
		    		}
		    		boolean inner_meInRange = (r1 > 0.8 && r1 < 1.2 && g1 < absRangeHi) || g1 < meLowRange;
		    		boolean inner_rhsInRange = (r2 > 0.8 && r2 < 1.2 && g2 < absRangeHi) || g2 < rhsLowRange;
		    		if (inner_meInRange != inner_rhsInRange && lhsHasDirChange != rhsHasDirChange && (rhsPt.getValue()-mePt.getValue()) < absRangeHi) {
		    			if (inner_meInRange && lhsHasDirChange == 0) {
		    				inner_rhsInRange = true;
		    			}
		    			else if (inner_rhsInRange && rhsHasDirChange == 0) {
		    				inner_meInRange = true;
		    			}
		    		}
		    		meInRange = meInRange || inner_meInRange;
		    		rhsInRange = rhsInRange || inner_rhsInRange;
		    		if (targetDest != null && mePt != null && (Misc.isUndef(simpleMeChallanDist) || simpleMeChallanDist > g1))
		    			simpleMeChallanDist = g1;
		    		if (targetDest != null && rhsPt != null && (Misc.isUndef(simpleRHSChallanDist) || simpleRHSChallanDist > g2))
		    			simpleRHSChallanDist = g2;
		    		if (inner_meInRange && meRhsBestChallanInfoMatch != null) {
		    			double distFromChallan = targetDest == null || mePt == null ? Misc.getUndefDouble() : g1;
		    			if (distFromChallan >= -0.0005 && distFromChallan < nearestMeChallanDist) {
		    				nearestMeChallanDist = distFromChallan;
		    				meRhsBestChallanInfoMatch.first = priorChallan;
		    				isChallanMarked = 1;
		    			}
		    		}
		    		if (inner_rhsInRange && meRhsBestChallanInfoMatch != null) {
		    			double distFromChallan = targetDest == null || rhsPt == null ? Misc.getUndefDouble() : g2;
		    			if (distFromChallan >= -0.0005 && distFromChallan < nearestRHSChallanDist) {
		    				nearestRHSChallanDist = distFromChallan;
		    				meRhsBestChallanInfoMatch.second = priorChallan;
		    				isChallanMarked = 1;
		    			}
		    		}
		    	}
		    }
		}
		boolean meInAbsRange = nearestMeChallanDist < absRangeLo && lhsHasDirChange > 0;
		boolean rhsInAbsRange = nearestRHSChallanDist < absRangeLo && rhsHasDirChange > 0;
		if (addnlChallanInfo != null) {
			addnlChallanInfo.meCloserThanAbsLo = nearestMeChallanDist < absRangeLo;
			addnlChallanInfo.rhsCloserThanAbsLo = nearestRHSChallanDist < absRangeLo;
		}

		boolean meValidFromChallanDist = true;
		boolean rhsValidFromChallanDist = true;
		if (!Misc.isUndef(invoiceDistKM) && invoiceDistKM > 0.1 && !Misc.isUndef(meTravelDist) && !Misc.isUndef(rhsTravelDist)) {
			double meTravelRatio = meTravelDist/invoiceDistKM;
			double rhsTravelRatio = rhsTravelDist/invoiceDistKM;
			
			if (Math.abs(meTravelDist-invoiceDistKM) > absTravelDistGapReInvoiceDistKmWhenOK
			 && (meTravelRatio <  loRelativeTravelDistGapReInvoiceDistKmWhenOK || meTravelRatio > hiRelativeTravelDistGapReInvoiceDistKmWhenOK)) {
				meValidFromChallanDist = false;
				if (addnlChallanInfo != null)
					addnlChallanInfo.meDistRelInvoiceOK = false;
			}
			if (Math.abs(rhsTravelDist-invoiceDistKM) > absTravelDistGapReInvoiceDistKmWhenOK
			 && (rhsTravelRatio <  loRelativeTravelDistGapReInvoiceDistKmWhenOK || rhsTravelRatio > hiRelativeTravelDistGapReInvoiceDistKmWhenOK)) {
			  rhsValidFromChallanDist = false;
				if (addnlChallanInfo != null)
					addnlChallanInfo.rhsDistRelInvoiceOK = false;
			}
		}
		
		if (meValidFromChallanDist != rhsValidFromChallanDist && isMyOpComplete == isRhsOpComplete) {
			return meValidFromChallanDist;
		}
		
		boolean pick1stArea = false;
		boolean pickLastArea = false;
		boolean meFixed = Misc.isUndef(opStationMe.getLinkedVehicleId());
		boolean rhsFixed = Misc.isUndef(opStationRhs.getLinkedVehicleId());
		
		if (meFixed == rhsFixed) { //both are linked or unlinked
			if (meFixed) {
				meFixed =! this.isOpStationForStop();
				rhsFixed =!rhs.isOpStationForStop();
				boolean amLoad = this.isLoad();
				if (!amLoad) {
	 				if ((challanIsAtOp && !meMatchesChallanAtOp) ||( challanDestValid && !challanIsAtOp))
						meFixed = false;
					if ((challanIsAtOp && !rhsMatchesChallanAtOp) ||( challanDestValid && !challanIsAtOp))
						rhsFixed = false;
				}
			}
		}
		if (addnlChallanInfo != null) {
			addnlChallanInfo.meFixed = meFixed;
			addnlChallanInfo.rhsFixed = rhsFixed;
		}
		boolean meHybrid = this.getGuaranteedLoadType() == 2 || this.getGuaranteedLoadType() == -1;
		boolean rhsHybrid = rhs.getGuaranteedLoadType() == 2 ||  rhs.getGuaranteedLoadType() == -1;
	
		if (meFixed != rhsFixed) {
			boolean amLoad = this.isLoad();
			if (meFixed && meHybrid && (prefForFixedLoad == 1 || prefForFixedUnload == 1))
				return true;
			if (rhsFixed && rhsHybrid && (prefForFixedLoad == 1 || prefForFixedUnload == 1))
				return false;
			if ((amLoad && prefForFixedLoad == 1) || (!amLoad && prefForFixedUnload == 1))
				return meFixed;
		}
		ArrayList<ChallanInfo> checkIfChallanDateInSeq = challanIsLikeUnload ? priorChallanUniqueDest :  currentChallanList;
		
		if (checkIfChallanDateInSeq != null && checkIfChallanDateInSeq.size() > 0) {
			for (ChallanInfo ch: checkIfChallanDateInSeq) {
				IdInfo src = challanIsLikeUnload ? ch.getIdInfoWithCalc(conn, true, stopDirControl) : ch.getSrcIdInfoWithCalc(conn, true, stopDirControl);
				if (src == null || src.getLongitude() <= 0)
					continue;
				if (src.getDestIdType() == 3) {
					meMatchFromChallan = meMatchFromChallan || opStationMe.getOpStationId() == src.getDestId();
					rhsMatchFromChallan = rhsMatchFromChallan || opStationRhs.getOpStationId() == src.getDestId();
				}
				else {
		    		meMatchFromChallan = meMatchFromChallan || mePt.distance(src.getLongitude(), src.getLatitude()) < absRangeLo;
		    		rhsMatchFromChallan = rhsMatchFromChallan || rhsPt.distance(src.getLongitude(), src.getLatitude()) < absRangeLo;
				}
				if (meMatchFromChallan && rhsMatchFromChallan)
					break;
			}
			if (meMatchFromChallan == rhsMatchFromChallan ) {
				meMatchFromChallan = false;
				rhsMatchFromChallan = false;
				for (ChallanInfo ch: checkIfChallanDateInSeq) {
					meMatchFromChallan = meMatchFromChallan || (ch.getChallanDate() >= meBeg && ch.getChallanDate() <= meEnd);
					rhsMatchFromChallan = rhsMatchFromChallan || (ch.getChallanDate() >= rhsBeg && ch.getChallanDate() <= rhsEnd);
					if (meMatchFromChallan && rhsMatchFromChallan)
						break;
				}
			}
		}
		if (meMatchFromChallan != rhsMatchFromChallan) {
			return meMatchFromChallan;
		}
		
		
		
		boolean amLoadType = isLoad();
		// 0=> no, 1=> only in micro view, 2 => only in big view, 3 => both in micro and big view .. we give pref to both, then long range, then short
		if (doSpecialAlgo == 1) {
			if (!this.isLoad() && meDalaRaise != rhsDalaRaise) {
				return meDalaRaise;
			}
			
			if (this.isLoad() && meStrikeCount != rhsStrikeCount) {
				return meStrikeCount > rhsStrikeCount;
			}
			if (lhsHasDirChange != rhsHasDirChange) {
				//	if (dbgsb != null) {dbgsb.append("return ").append(lhsHasDirChange > rhsHasDirChange).append(" because diff dir:").append(lhsHasDirChange).append(",").append(rhsHasDirChange); System.out.println(dbgsb);}
					return lhsHasDirChange > rhsHasDirChange;
			}
			if (lhsArtOpStationMatch != rhsArtOpStationMatch)
				return lhsArtOpStationMatch > rhsArtOpStationMatch;
			boolean areAtSimilarDistFromLoad = false;
			if (!Misc.isEqual(d1,0) && !Misc.isEqual(d2, 0)) {
				double ratio = d1 < d2 ? d1/d2 : d2/d1;
				areAtSimilarDistFromLoad = ratio >= 0.90;
			}
			if (!areAtSimilarDistFromLoad) {
				//if (dbgsb != null) {dbgsb.append("return ").append(d1 > d2).append(" because not at similarDist"); System.out.println(dbgsb);}
				return d1 > d2; 
			}
			
		}
		if (multiFurthest) {
			boolean amLoad = this.isLoad();
			if (meFixed != rhsFixed) {
				if (meFixed && meHybrid && (prefForFixedLoad == 2 || prefForFixedUnload == 2))
					return true;
				if (rhsFixed && rhsHybrid && (prefForFixedLoad == 2 || prefForFixedUnload == 2))
					return false;
		
				if ((amLoad && prefForFixedLoad == 2) || (!amLoad && prefForFixedUnload == 2))
					return meFixed;
			}
			if (amLoad) 
				return true;
			else
				return false;
		}
		if (prior != null && pickFurthest && doSpecialAlgo != 1) {
//			GpsData priorPt = prior.refWaitOutEvent;
//			GpsData mePt = this.getStartGpsExt();
//			GpsData rhsPt = rhs.getStartGps();
			
			if (priorPt != null && mePt != null && rhsPt != null) {
				if (meInRange != rhsInRange) {
					//if (dbgsb != null) {dbgsb.append("return ").append(meInRange).append(" because meInRange != rhsInRange"); System.out.println(dbgsb);}
					return meInRange;
				}
				
				if (meInRange) { //both are in range ... give pref to fixed if prefToFixed == 2
					if (meFixed != rhsFixed) {
						boolean amLoad = this.isLoad();
						if (meFixed && meHybrid && (prefForFixedLoad == 2 || prefForFixedUnload == 2))
							return true;
						if (rhsFixed && rhsHybrid && (prefForFixedLoad == 2 || prefForFixedUnload == 2))
							return false;
				
						if ((amLoad && prefForFixedLoad == 2) || (!amLoad && prefForFixedUnload == 2))
							return meFixed;
					}
					if (meInAbsRange != rhsInAbsRange) {
						return meInAbsRange;
					}
				}
				if (!meInRange) { //both are away .... see if the points not close to each other .. in that case pick the furthest
					//check if are at dissimilar distance from unload
					
					if (!Misc.isUndef(simpleMeChallanDist) && !Misc.isUndef(simpleRHSChallanDist)) {//check if me is closer to target and simpleRHSChallanDist much further
						double ratio = Misc.isEqual(simpleRHSChallanDist, 0) ? Misc.LARGE_NUMBER : simpleMeChallanDist/simpleRHSChallanDist;
						//if ((simpleMeChallanDist <= (absRangeHi+0.001) && simpleRHSChallanDist > (absRangeHi+0.001)) || ratio < 0.5)
					if ((simpleMeChallanDist < simpleRHSChallanDist) && ((simpleMeChallanDist < absRangeHi && simpleMeChallanDist > absRangeLo && ratio < 0.5)||(simpleMeChallanDist >= absRangeHi-0.001 && ratio < 0.75)))
							return true;
					}
					boolean areAtSimilarDistFromLoad = false;
					if (!Misc.isEqual(d1,0) && !Misc.isEqual(d2, 0)) {
						double ratio = d1 < d2 ? d1/d2 : d2/d1;
						areAtSimilarDistFromLoad = ratio > 0.8;
					}
					if (!areAtSimilarDistFromLoad) {
						//if (dbgsb != null) {dbgsb.append("return ").append(d1 > d2).append(" because not at similarDist"); System.out.println(dbgsb);}
						return d1 > d2; 
					}
					else {
						if (meFixed != rhsFixed) {
							boolean amLoad = this.isLoad();
							if (meFixed && meHybrid && (prefForFixedLoad == 2 || prefForFixedUnload == 2))
								return true;
							if (rhsFixed && rhsHybrid && (prefForFixedLoad == 2 || prefForFixedUnload == 2))
								return false;
					
							if ((amLoad && prefForFixedLoad == 2) || (!amLoad && prefForFixedUnload == 2))
								return meFixed;
						}
					}
				}
				//either both in Range or both are at Similar DistFromLoad ... now pick using best direction etc
			}
		}
		if (meFixed && rhsFixed && challanIsAtOp) {
			//give pref first
			return true;
		}
		if (lhsHasDirChange != rhsHasDirChange) {
		//	if (dbgsb != null) {dbgsb.append("return ").append(lhsHasDirChange > rhsHasDirChange).append(" because diff dir:").append(lhsHasDirChange).append(",").append(rhsHasDirChange); System.out.println(dbgsb);}
			return lhsHasDirChange > rhsHasDirChange;
		}
		if (meInRange) {
			return nearestMeChallanDist < nearestRHSChallanDist;
		}
		if (lhsArtOpStationMatch != rhsArtOpStationMatch)
			return lhsArtOpStationMatch > rhsArtOpStationMatch;
		if (pickFurthest && isMyOpComplete && isRhsOpComplete && priorPt != null && mePt != null && rhsPt != null && !amLoadType //last just to be safe for SECL  
				&& (
						(opStationMe.getOpStationId() != opStationRhs.getOpStationId())
						|| (this.isOpStationForStop() && rhs.isOpStationForStop()
								)
				)
			) {
			//see if me lies on prio
			Pair<Double, Double> res = RouteDef.checkWhereInSegment(rhsPt.getLongitude(), rhsPt.getLatitude(), priorPt.getLongitude(), priorPt.getLatitude(), mePt.getLongitude(), mePt.getLatitude());
			if (res.first > 1 && !Misc.isEqual(res.first, 1)) {//to the right
				return false;
			}
			else { 
				return true;
			}
		}
		 if (opStationMe == null || opStationRhs == null || opStationMe.getOpStationId() != opStationRhs.getOpStationId()) {
			//check if overlapping ... then too pick the furthest
			long meStart = myExtract.getEarliestEventDateTime();
			long meEn = myExtract.getLatestEventDateTime();
			long rhsStart = rhsExtract.getEarliestEventDateTime();
			long rhsEn = rhsExtract.getLatestEventDateTime();
			if ((meStart >= rhsStart && meStart < rhsEn) || (rhsStart >= meStart && rhsStart < meEn)) {//TODO_VERIFY FIX HERE FOR SIMILAR DIST
				//boolean areAtSimilarDistFromLoad = false;
				//if (!Misc.isEqual(d1,0) && !Misc.isEqual(d2, 0)) {
				//	double ratio = d1 < d2 ? d1/d2 : d2/d1;
				//	areAtSimilarDistFromLoad = ratio > 0.95;
				//}
				//if (!areAtSimilarDistFromLoad)
					//return d1 > d2; 					
			}
		}
		if (opStationMe != null && opStationRhs != null && opStationMe.getOpStationId() == opStationRhs.getOpStationId()) { // 1st pick from opstation definition
			if (opStationMe.m_bestAreaIsFirst)
				pick1stArea = true;
			else if (opStationMe.m_bestAreaIsLast)
				pickLastArea = true;
		}
		if (!pick1stArea && !pickLastArea && distCalc != null) {// if still default then pick from Org 
			if (amLoadType) {
				if (stopDirControl.isBestLoadFirst())
					pick1stArea = true;
				else if (stopDirControl.isBestLoadLast())
					pickLastArea = true;
			} else {
				if (stopDirControl.isBestUnloadFirst())
					pick1stArea = true;
				else if (stopDirControl.isBestUnloadFirst())
					pickLastArea = true;
			}
		}
		if (pick1stArea) {
			if (isMyOpComplete)
				return true;
			else if (isRhsOpComplete)
				return false;
			// else pick the longest time spent
		}
		if (pickLastArea
				&& !Misc.isUndef(myExtract.getAreaIn()) && !Misc.isUndef(rhsExtract.getAreaIn())) {
			if (isRhsOpComplete)
				return true;
			else if (isMyOpComplete)
				return false;
			// else pick the longest time spent
		}

		if (bestLUIsValidExtremum) {
			boolean myIsLoad = isLoad();
			if (isMyOpComplete || isRhsOpComplete) {
				if (myIsLoad) {
					if (isRhsOpComplete)
						return false;
					else
						return true;
				} else {
					if (isMyOpComplete)
						return true;
					else
						return false;
				}
			}
		}
		long myGap = !Misc.isUndef(myExtract.getAreaIn()) && !Misc.isUndef(myExtract.getAreaOut()) ? myExtract.getAreaOut() - myExtract.getAreaIn() : Misc.getUndefInt();
		if (Misc.isUndef(myGap)) {
			if (!Misc.isUndef(myExtract.getAreaIn()))
				myGap = 10000000;
		}
		long rhsGap = !Misc.isUndef(rhsExtract.getAreaIn()) && !Misc.isUndef(rhsExtract.getAreaOut()) ? rhsExtract.getAreaOut() - rhsExtract.getAreaIn() : Misc.getUndefInt();
		if (Misc.isUndef(rhsGap)) {
			if (!Misc.isUndef(rhsExtract.getAreaIn()))
				rhsGap = 10000000;
		}
		// TODO also adjust for whether the timeGap is sufficient to mark as work done and whether there was stoppage within
		if (!Misc.isUndef(myGap) && !Misc.isUndef(rhsGap) && rhsGap != myGap) {
//			if (dbgsb != null) {dbgsb.append("return ").append(myGap > rhsGap).append(" because AreaIn Gap:").append(myGap).append(",").append(rhsGap); System.out.println(dbgsb);}
			return myGap > rhsGap;
		} else if (rhsGap != myGap) {
			if (!Misc.isUndef(myGap)) {
//				if (dbgsb != null) {dbgsb.append("return ").append(true).append(" because AreaIn Gap:").append(myGap).append(",").append(rhsGap); System.out.println(dbgsb);}
				return true;
			}
			else {
//				if (dbgsb != null) {dbgsb.append("return ").append(false).append(" because AreaIn Gap:").append(myGap).append(",").append(rhsGap); System.out.println(dbgsb);}
				return false;
			}
		} else {// both undefined .. pick the one that has the longest Win/Wout
			myGap = !Misc.isUndef(myExtract.getWaitOut()) && !Misc.isUndef(myExtract.getWaitIn()) ? myExtract.getWaitOut() - myExtract.getWaitIn() : Misc.getUndefInt();
			if (Misc.isUndef(myGap)) {
				if (!Misc.isUndef(myExtract.getWaitIn()))
					myGap = 10000000;
			}
			rhsGap = !Misc.isUndef(rhsExtract.getWaitOut()) && !Misc.isUndef(rhsExtract.getWaitIn()) ? rhsExtract.getWaitOut() - rhsExtract.getWaitIn() : Misc.getUndefInt();
			if (Misc.isUndef(rhsGap)) {
				if (!Misc.isUndef(rhsExtract.getWaitIn()))
					rhsGap = 10000000;
			}
//			if (dbgsb != null) {dbgsb.append("return ").append(myGap > rhsGap).append(" because Wait Gap:").append(myGap).append(",").append(rhsGap); System.out.println(dbgsb);}

			return myGap > rhsGap;
		}
	}

	//2014_06_14 public boolean isExtractDependentOnNext() {
	//2014_06_14 	return reextractDependentUponNext;
	//2014_06_14 }

	public boolean isLoad() {
		if (this.isOpStationForStop()) {
			return this.stopLoadType == 0 ? true : false; //treating unknown as unload type //TODO_STOP
		}
		LUItem item = get(getThisOrNextUsefulIndex(0));
		return item == null ? false : item.isLoad();
	}
	
	public LUItem getStartItemExt() {
		LUItem firstItem = this.get(getThisOrNextUsefulIndex(0));
		return firstItem;
		
	}
	
	
	//CHANGE13 public GpsData getStartGpsExt() {
	//CHANGE13 	LUItem firstItem = getStartItemExt();
	//CHANGE13 	return firstItem == null ? null : firstItem.getGpsData();
	//CHANGE13 }
	public long getStartTimeExt() {
		LUItem firstItem = getStartItemExt();
		return firstItem == null ? Misc.getUndefInt() : firstItem.getTime();
	}
	
	//CHANGE13 public GpsData getStartGps() {
	//CHANGE13 	LUItem firstItem = get(0);
	//CHANGE13 	return firstItem == null ? null : firstItem.getGpsData();
	//CHANGE13 }
	public long getStartTime() {
		LUItem firstItem = get(0);
		return firstItem == null ? Misc.getUndefInt() : firstItem.getTime();
	}
	
	
	public boolean hasGoneOutOfWorkArea() {
		OpStationBean opsdef = TripInfoCacheHelper.getOpStation(opStationId);
		if (opsdef != null && !Misc.isUndef(opsdef.getAreaOfWork())) {
			for (int i=size()-1;i>=0;i--) {
				LUItem item = get(i);
				if (item.isConfirmatory())
					return true;
			}
		}
		return false;
	}
	
	public int getEndLUItemIndex() {
		return getEndLUItemIndex(super.size());
	}
	
	public int getEndLUItemIndex(int fromPos) {
		if (this.isOpStationForStop()) {
			return this.getThisOrPrevStopLikeIndex(fromPos);//this.getStopEndIndex();
		}
		int index = -1;;
		LUItem lastItem = null;
		for (int i=fromPos-1;i>=0;i--) {
			LUItem temp = get(i);
			if (temp.isNonRegionEvent() && !temp.isStop())
				continue;
			if (index < 0) {
				index = i;
				lastItem = temp;
			}
			if (!temp.isOut() || temp.isStopStartExt()) //get the last one consider ain ain ain being artficially inserteds
				break;
			if (temp.isSameEvent(lastItem)) {
				index = i;
				lastItem = temp;
			}
			else {
				break;
			}
		}
		return index;
	}
	
	public int getRegionEndIndex() {
		int index = -1;;
		LUItem lastItem = null;
		for (int i=super.size()-1;i>=0;i--) {
			LUItem temp = get(i);
			if (temp.isNonRegionEvent())
				continue;
			if (index < 0) {
				index = i;
				lastItem = temp;
			}
			if (!temp.isOut()) //get the last one consider ain ain ain being artficially inserteds
				break;
			if (temp.isSameEvent(lastItem)) {
				index = i;
				lastItem = temp;
			}
			else {
				break;
			}
		}
		return index;
	}
	public int getEndLUItemIndexOrig() {
		int index = super.size()-1;
		LUItem lastItem = get(index);
		for (int i=super.size()-2;i>=0;i--) {
			LUItem temp = get(i);
			if (temp.isNonRegionEvent())
				continue;
			if (!temp.isOut()) //get the last one consider ain ain ain being artficially inserteds
				break;
			if (temp.isSameEvent(lastItem)) {
				index = i;
				lastItem = temp;
			}
			else {
				break;
			}
		}
		return index;
	}
	public LUItem getEndLUItem() {
		int index = getEndLUItemIndex();
		return get(index);
	}
	
	public LUItem getNextStopLike(LUItem item) {
		Pair<Integer, Boolean> idx = indexOf(item);
		return getNextStopLike(idx.first+1);
	}
	
	public LUItem getPrevStopLike(LUItem item) {
		Pair<Integer, Boolean> idx = indexOf(item);
		return getPrevStopLike(idx.first);
	}
	public LUItem getNextStopLike(int index) {
		for (int i=index, is = size(); i<is;i++) {
			LUItem item = get(i);
			if (item != null && item.isStop())
				return item;
		}
		return null;
	}
	public int getThisOrPrevStopLikeIndex(int index) {
		for (int i=index; i>=0;i--) {
			LUItem item = get(i);
			if (item != null && item.isStop())
				return i;
		}
		return -1;
	}
	public LUItem getPrevStopLike(int index) {
		return get(getThisOrPrevStopLikeIndex(index));
	}
	
	public int getStopStartIndex() {
		for (int i=0, is = size(); i<is;i++) {
			if (get(i).isStopStartExt())
				return i;
		}
		return -1;
	}
	public int getStopEndIndex() {
		return getStopEndIndex(size());
	}
	public int getStopEndIndex(int fromPos) {
		int lastStop = -1;
		for (int i=fromPos-1;i>= 0;i--) {
			if (get(i).isStopStartExt())
				return lastStop;// == -1 ? i : lastStop;
			else if (get(i).isStopEnd())
				lastStop = i;
		}
		return lastStop;
	}
	
	public boolean hasDirChange() {
		int dir = getDirChange0123();
		return dir != 0;
	}
	
	public int getDirChange0123() {
		LUItem best = null;
		for (int i=0,is=size();i<is;i++) {
			LUItem item = get(i);
			if (item.isDirChange() && (best == null || item.isMeBetterDirChange(best) > 0))
				best = item;
		}
		return best == null ? 0 : best.getDir0123(best.getEventId());
	}
	
	//CHANGE13 public GpsData getEndGps() {
	//CHANGE13 	LUItem lastItem = getEndLUItem();
	//CHANGE13 	return lastItem == null ? null : lastItem.getGpsData();
	//CHANGE13 }
	
	public long getEndTime() {
		LUItem last = getEndLUItem();
		return last == null ? Misc.getUndefInt() : last.getTime();
	}
	
	public long getEndTimeSimple() {
		LUItem lastItem = get(super.size() - 1);
		return lastItem == null ? Misc.getUndefInt() : lastItem.getTime();
	}

	public int compareTo(LUSequence rhs) {
		return getStartTime() < rhs.getStartTime() ? -1 : getStartTime() > rhs.getStartTime() ? 1 : 0;
	}

	public int getOpStationId() {
		return opStationId;
	}
	
	public OpStationBean getOpStation() {
		return TripInfoCacheHelper.getOpStation(opStationId);
	}
	
	//public static boolean isOpStationForStop(OpStationBean opStationBean) {
	//	return opStationBean == null || opStationBean.getOpStationType() == TripInfoConstants.STOP_BASED_OPSTATION;
	//}

	public  boolean isOpStationForStop() {
		return (this.isStopOpStationType & 0x1) != 0;
	}
	
	public  boolean isOpStationForStopULSplitAlway() {
		return (this.isStopOpStationType & 0x2) != 0;
	}
	public  void setOpStationForStopULSplitAlway() {
		this.isStopOpStationType = (byte) (this.isStopOpStationType | 0x2);
	}
	public  void resetOpStationForStopULSplitAlway() {
		this.isStopOpStationType = (byte) (this.isStopOpStationType & ~0x2);
	}
	public Pair<LUItem, Boolean> add(LUItem item, int itemOpStationId) {
		Pair<LUItem, Boolean> retval = super.add(item);
		if (!item.isWaitOutOrConfirmatory()) {
			opStationId = itemOpStationId;
			if (this.luInfoExtract != null)
				this.luInfoExtract.setOfOpStationId(opStationId);
		}
		return retval;

	}

	public void merge(LUSequence rhs, long dontTouchBefore) {//TODO_FOR_CHALLAN
		if (rhs == null)
			return;
		//this.markForShovelGet(false);
		this.loadStGpsPt = null;
		this.loadStGpsStart = Misc.getUndefShort();
		boolean dontTouch =this.size() > 0 && this.get(0).getGpsDataRecordTime() <= dontTouchBefore;
		AddnlInfo thisa = this.addnlInfo;
		AddnlInfo rhsa = rhs.addnlInfo;
		if (dontTouch && rhsa != null) {
			if (thisa == null)
				thisa = this.addnlInfo = new AddnlInfo();
			if (!Misc.isUndef(rhsa.tripId) || !Misc.isUndef(rhsa.rightTripId) || (rhsa.otherMergedTripId != null && rhsa.otherMergedTripId.size() > 0 )) {
				if (thisa.otherMergedTripId == null) {
					thisa.otherMergedTripId = new ArrayList<Integer>();
				}
				if (!Misc.isUndef(rhsa.tripId))
					thisa.otherMergedTripId.add(rhsa.tripId);
				if (!Misc.isUndef(rhsa.rightTripId))
					thisa.otherMergedTripId.add(rhsa.rightTripId);
				for (int k=0,ks = rhsa.otherMergedTripId == null ? 0 : rhsa.otherMergedTripId.size(); k<ks;k++)
					thisa.otherMergedTripId.add(rhsa.otherMergedTripId.get(k));
			}
			return;
		}
		this.loadStGpsStart = Misc.getUndefShort();
		// int rhsOpStationId = rhs.getOpStationId(); unnecessary
		boolean actuallyAmAtLeft = rhs.size() > 0 && this.size() > 0 && rhs.getEndTimeSimple() >= this.getStartTime();
		
		if (!actuallyAmAtLeft) {
			List<LUItem> rhsLUItems = rhs.getUnderlyingList();
			if (rhsLUItems != null) {
				
				boolean seenNonEnd = false;
				for (LUItem item : rhsLUItems) {
					boolean isEndLike = (item.isWaitOutOrConfirmatory() || item.isStopEnd());
					if (!seenNonEnd && isEndLike)
							continue;
					seenNonEnd = true;
					add(item);// , rhsOpStationId);
				}
			}
		}
		if (rhs.addnlInfo == null)
			return;
		if (this.addnlInfo == null)
			this.addnlInfo = new AddnlInfo();
		
		if (!Misc.isUndef(rhsa.tripId) && (Misc.isUndef(thisa.tripId) || thisa.tripId == rhsa.tripId)) {
			thisa.tripId = rhsa.tripId;
			rhsa.tripId = Misc.getUndefInt();
		}
		if (!Misc.isUndef(rhsa.rightTripId) && (Misc.isUndef(thisa.rightTripId) ||thisa.rightTripId == rhsa.rightTripId)) {
			thisa.rightTripId = rhsa.rightTripId;
			rhsa.rightTripId = Misc.getUndefInt();
		}
		if (!Misc.isUndef(rhsa.intermediateLUId) && (Misc.isUndef(thisa.intermediateLUId) || thisa.intermediateLUId == rhsa.intermediateLUId)) {
			thisa.intermediateLUId = rhsa.intermediateLUId;
			rhsa.intermediateLUId = Misc.getUndefInt();
		}
		if (!Misc.isUndef(rhsa.intermediateParentTripId) && (Misc.isUndef(thisa.intermediateParentTripId) || thisa.intermediateParentTripId == rhsa.intermediateParentTripId)) {
			thisa.intermediateParentTripId = rhsa.intermediateParentTripId;
			rhsa.intermediateParentTripId = Misc.getUndefInt();
		}
		if (!Misc.isUndef(rhsa.tripId) || !Misc.isUndef(rhsa.rightTripId) || (rhsa.otherMergedTripId != null && rhsa.otherMergedTripId.size() > 0 )) {
			if (thisa.otherMergedTripId == null) {
				thisa.otherMergedTripId = new ArrayList<Integer>();
			}
			if (!Misc.isUndef(rhsa.tripId))
				thisa.otherMergedTripId.add(rhsa.tripId);
			if (!Misc.isUndef(rhsa.rightTripId))
				thisa.otherMergedTripId.add(rhsa.rightTripId);
			for (int k=0,ks = rhsa.otherMergedTripId == null ? 0 : rhsa.otherMergedTripId.size(); k<ks;k++)
				thisa.otherMergedTripId.add(rhsa.otherMergedTripId.get(k));
		}
		//if (!Misc.isUndef(rhsa.intermediateLUId) || (rhsa.otherIntermediateLUId != null && rhsa.otherIntermediateLUId.size() > 0 )) {
		//	if (thisa.otherIntermediateLUId == null) {
		//		thisa.otherIntermediateLUId = new ArrayList<Integer>();
		//	}
		//	if (!Misc.isUndef(rhsa.intermediateLUId))
		//		thisa.otherIntermediateLUId.add(rhsa.intermediateLUId);
		//	for (int k=0,ks = rhsa.otherIntermediateLUId == null ? 0 : rhsa.otherIntermediateLUId.size(); k<ks;k++)
		//		thisa.otherIntermediateLUId.add(rhsa.otherIntermediateLUId.get(k));
		//}
		
		//TODO challan merging
		return;
	}

//	public LUInfoExtract getLUInfoExtractOrigNotUsed(LUInfoExtract nextItemsLUInfo, boolean nextItemIsSameLoadType, boolean redo) {
//		// this gets the best estimates of Win,Gin,Ain,Aout,Gout,Wout. It does so as follows
//		// 1. get the best Ain/Aout. In case no Aout for the same area as Ain is possible then will look at nextIfSameLoadType and use that waitIn
//		// 2. get earliest Gin before Ain.
//		// 3. get earlies Win before Gin (if no Gin foundin 2 then Ain)
//		// 4. if Gin is is null then same as Win and if that also null then Ain
//		// 5 if Win is is null then same as Gin and if that also null then Ain
//		// 6 Get Gout as first Gout after Aout and simlarly
//		// 7 get Wout as first Wout aftet Gout (and no Gout then Aout)
//		// 8 if no Wout found then take from Gout and if still null then take as waitin of next (& if that is null, then Aout)
//		// 9 if no Gout then same as adjusted Wout of step 8
//
//		// WILL Set reextraceDependenyUponNext if ever the need was felt to inquire the next sequence.
//
//		// pass the extracted info from the next sequence in timeList if that is of same type, and pass true/false if that is of same Load/Unload type
//		// else pass null if there is none after this
//
//		// redo == true will cause re-finding of LU Info fields.
//
//		// To make best use of this function - start from the last Item in the L/U sequential set. and note if it is dirty. do a getLUInfoExtract(null, true, false)
//		// and use the dirty parameter of this on the previous L/U item's getLUInfoExtract's redo parameter
//		if (nextItemsLUInfo == null)
//			reextractDependentUponNext = true;
//		if (redo)
//			toReextractLUInfo = true;
//		if (luInfoExtract == null) {
//			luInfoExtract = new LUInfoExtract();
//			toReextractLUInfo = true;
//		} else if (toReextractLUInfo) {
//			// luInfoExtract.clear();
//			luInfoExtract = new LUInfoExtract();
//		}
//		if (toReextractLUInfo) {
//			GpsData refWaitInEvent = null;
//			GpsData refGateInEvent = null;
//			GpsData refAreaInEvent = null;
//			GpsData refAreaOutEvent = null;
//			GpsData refGateOutEvent = null;
//			GpsData refWaitOutEvent = null;
//
//			reextractDependentUponNext = false; // will be set to true if we feel the need to look at next's item (regardless of that being there or not)
//			// 1. get best Ain/Aout pos.
//			// 2. if no Aout found then use next's Win as value
//			// 3.
//
//			// getting - best Ain/Aout - the one with maximum duration of same region id
//			int currBestAinIndex = -1;
//			Date currBestAoutTime = null; // this will either be Aout's time or if Aout points to last entry and is really not Aout, then depending upon next's entry
//			int currBestPriority = Misc.getUndefInt(); // this may actually point to Win of the next one!!
//			long currBestTimeDiff = 0;
//			int firstAinAt = -1;
//			for (int i = 0, is = size(); i < is; i++) {
//				LUItem item = get(i);
//				GpsData aoutData = null;
//				if (item.isAreaIn()) {
//					// go until we get to Area In/Area out of another area or non Area related event
//					if (firstAinAt < 0)
//						firstAinAt = i;
//					int currAreaId = item.getRegionId();
//					int j = i + 1;
//					int breakOutIndex = -1;
//					for (; j < is; j++) {
//						LUItem nextItem = get(j);
//						if (nextItem.isAreaOut()) {
//							breakOutIndex = j;
//							continue;
//						}
//						if (nextItem.isAreaIn() && nextItem.getRegionId() == currAreaId) {
//							breakOutIndex = -1;
//							continue;
//						}
//						// if (nextItem.getRegionId() != currAreaId || (!nextItem.isAreaIn() && !nextItem.isAreaOut()) || nextItem.isAreaOut()) {
//						if (nextItem.getRegionId() != currAreaId || (!nextItem.isAreaIn() && !nextItem.isAreaOut())) {
//							break;
//						}
//					}
//					if (breakOutIndex != -1)
//						j = breakOutIndex;
//					Date currAin = get(i).getTime();
//					Date currAout = null;
//					if (j != is) {
//						currAout = get(j).getTime();
//						aoutData = get(j).getGpsData();
//					} else {
//						if (nextItemsLUInfo != null) {
//							if (nextItemIsSameLoadType) { // take Aout as same as WaitIn of next
//								currAout = nextItemsLUInfo.waitIn;
//								aoutData = nextItemsLUInfo.refAreaOutEvent;
//							} else { // don't know the best approach - but be conservative and set as same as Ain
//								currAout = currAin;
//								aoutData = item.getGpsData();
//							}
//						}
//					}
//
//					if (j == is) {
//						reextractDependentUponNext = true;
//					}
//					long currTimeDiff = currAin != null && currAout != null ? currAout.getTime() - currAin.getTime() : Misc.getUndefInt();
//					if (!Misc.isUndef(currTimeDiff) && currTimeDiff < item.getThreshold()) {
//						i = j - 1;
//						continue;
//					}
//					if (Misc.isUndef(currTimeDiff))
//						currTimeDiff = 0;
//
//					if (currBestAinIndex == -1) {
//						currBestAinIndex = i;
//						currBestPriority = item.getPriority();
//						currBestAoutTime = currAout;
//						currBestTimeDiff = currTimeDiff;
//						refAreaInEvent = item.getGpsData();
//						refAreaOutEvent = aoutData;
//					} else if (currAout != null) {
//						if (item.getPriority() == currBestPriority) {
//							if (currTimeDiff > currBestTimeDiff) {
//								currBestTimeDiff = currTimeDiff;
//								currBestAinIndex = i;
//								currBestAoutTime = currAout;
//								refAreaInEvent = item.getGpsData();
//								refAreaOutEvent = aoutData;
//							}
//						} else if (item.getPriority() < currBestPriority) {
//							currBestAinIndex = i;
//							currBestTimeDiff = currTimeDiff;
//							currBestPriority = item.getPriority();
//							currBestAoutTime = currAout;
//							refAreaInEvent = item.getGpsData();
//							refAreaOutEvent = aoutData;
//						}
//					}
//					i = j - 1; // ++ happens at end of loop!!
//				}
//			}
//			// getting Gin ... 1st GateIn before Ain
//			int ginIndex = -1;
//			for (int i = 0, is = currBestAinIndex < 0 ? size() : currBestAinIndex; i < is; i++) {
//				LUItem item = get(i);
//				if (item.isGateIn()) {
//					ginIndex = i;
//					break;
//				}
//			}
//			// getting Win.. 1st Win before Gin or if no Gin then before Ain
//			int winIndex = -1;
//			for (int i = 0, is = ginIndex >= 0 ? ginIndex : currBestAinIndex < 0 ? size() : currBestAinIndex; i < is; i++) {
//				LUItem item = get(i);
//				if (item.isWaitIn()) {
//					winIndex = i;
//					break;
//				}
//			}
//			// if giindex < 0, first est from wiindex and then from Ain
//			if (ginIndex < 0) {
//				if (winIndex >= 0)
//					ginIndex = winIndex;
//				else
//					ginIndex = firstAinAt;
//			}
//			if (winIndex < 0)
//				if (ginIndex >= 0)
//					winIndex = ginIndex;
//				else
//					winIndex = firstAinAt;
//
//			// getting Gout ... 1st Gateout after Aout
//			GpsData goutData = null;
//			GpsData woutData = null;
//			int goutIndex = -1;
//			for (int i = currBestAinIndex < 0 ? 0 : currBestAinIndex + 1, is = size(); i < is; i++) {
//				LUItem item = get(i);
//				if (item.isGateOut()) {
//					goutIndex = i;
//					goutData = item.getGpsData();
//					break;
//				}
//			}
//			// getting Wout .. 1st Wout after Gout or if no Gout then Aout
//			int woutIndex = -1;
//			for (int i = goutIndex >= 0 ? goutIndex + 1 : currBestAinIndex >= 0 ? currBestAinIndex + 1 : 0, is = size(); i < is; i++) {
//				LUItem item = get(i);
//				if (item.isWaitOutOrConfirmatory()) {
//					woutIndex = i;
//					woutData = item.getGpsData();
//					break;
//				}
//			}
//			Date woutDate = null;
//			if (woutIndex < 0)
//				if (goutIndex >= 0) {
//					woutIndex = goutIndex;
//					woutData = goutData;
//				}
//			if (woutIndex >= 0) {
//				woutDate = get(woutIndex).getTime();
//				woutData = get(woutIndex).getGpsData();
//			} else if (nextItemsLUInfo != null) {
//				if (nextItemIsSameLoadType) {
//					woutDate = nextItemsLUInfo.waitIn;
//					woutData = nextItemsLUInfo.refWaitInEvent;
//				} else {
//					woutDate = currBestAoutTime;
//					woutData = refAreaOutEvent;
//					if (woutDate == null) {
//						woutDate = (ginIndex >= 0) ? get(ginIndex).getTime() : winIndex >= 0 ? get(winIndex).getTime() : null;
//						woutData = (ginIndex >= 0) ? get(ginIndex).getGpsData() : winIndex >= 0 ? get(winIndex).getGpsData() : null;
//					}
//
//				}
//				reextractDependentUponNext = true;
//			} else { // if nothing after and no wout then no wout
//				// woutDate = currBestAoutTime;
//				// reextractDependentUponNext = true;
//			}
//			Date goutDate = null;
//			if (goutIndex >= 0) {
//				goutDate = get(goutIndex).getTime();
//				goutData = get(goutIndex).getGpsData();
//			} else {
//				goutDate = woutDate;
//				goutData = woutData;
//			}
//			// now fill value
//			if (currBestAinIndex >= 0) {
//				luInfoExtract.areaIn = get(currBestAinIndex).getTime();
//				luInfoExtract.areaId = get(currBestAinIndex).getRegionId();
//				int materialId = Misc.getUndefInt();
//				try {
//					materialId = (TripInfoCache.getOpStation(opStationId) != null && !Misc.isUndef(luInfoExtract.areaId)) ? TripInfoCache.getOpStation(opStationId).getMaterial(
//							luInfoExtract.areaId) == null ? Misc.getUndefInt() : TripInfoCache.getOpStation(opStationId).getMaterial(luInfoExtract.areaId) : Misc.getUndefInt();
//				} catch (Exception e) {
//					System.out.println("LUSequence.getLUInfoExtract() : opStationId :  " + opStationId + "  :  areaId :  " + luInfoExtract.areaId);
//					e.printStackTrace();
//				}
//				luInfoExtract.materialId = materialId;
//				luInfoExtract.material = TripInfoCache.getMaterialCache().get(materialId);
//				luInfoExtract.areaOut = currBestAoutTime;
//				luInfoExtract.refAreaInEvent = refAreaInEvent;
//				luInfoExtract.refAreaOutEvent = refAreaOutEvent;
//			} else {
//				luInfoExtract.areaIn = null;
//				luInfoExtract.areaId = Misc.getUndefInt();
//				luInfoExtract.material = null;
//				luInfoExtract.materialId = Misc.getUndefInt();
//				luInfoExtract.areaOut = null;
//				luInfoExtract.refAreaInEvent = null;
//				luInfoExtract.refAreaOutEvent = null;
//			}
//			if (ginIndex >= 0) {
//				luInfoExtract.gateIn = get(ginIndex).getTime();
//				luInfoExtract.refGateInEvent = refGateInEvent;
//			}
//			if (winIndex >= 0) {
//				luInfoExtract.waitIn = get(winIndex).getTime();
//				luInfoExtract.refWaitInEvent = refWaitInEvent;
//			}
//			luInfoExtract.gateOut = goutDate;
//			luInfoExtract.refGateOutEvent = refGateOutEvent;
//			luInfoExtract.waitOut = woutDate;
//			luInfoExtract.refWaitOutEvent = refWaitOutEvent;
//
//			// TODO to associate trip id with luInfoExtractor
//			//luInfoExtract.tripId = this.tripId;
//
//			toReextractLUInfo = false;
//		}
//
//		return luInfoExtract;
//	}
//
	public void setCachedIntermediateLUInfoExtract(LUInfoExtract cachedIntermediateLUInfoExtract) {
		if (cachedIntermediateLUInfoExtract == null && this.addnlInfo == null)
			return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.cachedIntermediateLUInfoExtract = cachedIntermediateLUInfoExtract;
	}

	public LUInfoExtract getCachedIntermediateLUInfoExtract() {
		return addnlInfo == null ? null : addnlInfo.cachedIntermediateLUInfoExtract;
	}

	public void setIntermediateParentTripId(int intermediateParentTripId) {
		if (Misc.isUndef(intermediateParentTripId) && addnlInfo == null)
			return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		this.addnlInfo.intermediateParentTripId = intermediateParentTripId;
	}

	public int getIntermediateParentTripId() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.intermediateParentTripId;
	}
    public int getRawDisposition() {
    	return disposition;
    }
    
    public void removeDupliEventsBug() {
		try {
			LUItem prev = null;
			int prevStartIndex = -1;
			
			for (int i=0,is=size();i<is;i++) {
				LUItem curr = get(i);
				if (prev != null && prev.getEventId() != curr.getEventId() && ((prev.getRegionId() < 0 && curr.getRegionId()<0) || prev.getRegionId() != curr.getRegionId())) {
					for (int j=prevStartIndex+1,js=i-1;j<js;j++) {
						remove(prevStartIndex+1);
						i--;
						is--;
					}
					prevStartIndex = i;
				}
				prev = curr;
			}
		}
		catch (Exception e2) {
			//eat it
		}
	}

	public byte getDisposition() {
		return disposition < 0 ? (isLoad() ? (byte)1 : (byte)2) : disposition ;
	}
	
	
	public byte getDispositionSimple() {
		return disposition;
	}

	public void setDisposition(byte disposition) {
		this.disposition = disposition;
	}
	// to get next stopEnd event from a given index
	public int getNextStopEndIndex(int index){
		for (; index < super.size(); index++) {
			if(this.get(index).isStopEnd())
				return index;
		}
		return Misc.getUndefInt();
	}
	// to get next areaOut event from a given index
	public int getNextAreaOutIndex(int index){
		int firstReg = get(index).getRegionId();
		int prevAout = -1;
		for (; index < super.size(); index++) {
			LUItem item = get(index);
			if (item.isAreaOut())
				prevAout = index;
			else if (item.isAreaIn() && item.getRegionId() == firstReg) {
				prevAout = -1;
			}
			else if (!item.isNonRegionEvent()) {
				break;
			}
		}
		if (prevAout >= 0)
			return prevAout;
		else if (index < size())
			return index;
		else
			return Misc.getUndefInt();
	}
	// to get duration of stopStart stopEnd in seconds
	public int getStopStartStopEndDuration(int index){
		if(this.get(index).isStopStartExt()){
			if(!Misc.isUndef(getNextStopEndIndex(index))){
				return (int)(this.get(getNextStopEndIndex(index)).getTime() - this.get(index).getTime())/1000;
			}else{
				return (int)(this.get(getEndLUItemIndex()).getTime() - this.get(index).getTime())/1000;
			}
		}
		return Misc.getUndefInt();
	}
	// to get duration of Aout-Ain in seconds
	public int getAreaOutAreaInDuration(int index){
		if(this.get(index).isAreaIn()){
			int aoutIndex = Misc.getUndefInt();
			if (!Misc.isUndef(aoutIndex = getNextAreaOutIndex(index))){
				return (int)(this.get(aoutIndex).getTime() - this.get(index).getTime())/1000;
			} 
			else {
				return (int)(this.get(getEndLUItemIndex()).getTime() - this.get(index).getTime())/1000;
			}
		}
		return Misc.getUndefInt();
	}

	public void setBlockComboMaterialList(ArrayList<Triple<Integer, Integer, Integer>> blockComboMaterialList) {
		if (addnlInfo == null && (blockComboMaterialList == null || blockComboMaterialList.size() == 0))
				return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		this.addnlInfo.blockComboMaterialList = blockComboMaterialList;
	}

	public ArrayList<Triple<Integer, Integer, Integer>> getBlockComboMaterialList() {
		return addnlInfo == null ? null : addnlInfo.blockComboMaterialList;
	}
	public long getChallanDate() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.challanInfoList == null ? Misc.getUndefInt() : addnlInfo.challanInfoList.get(addnlInfo.challanInfoList.size()-1);
	}
	public ArrayList<Long> getChallanInfoList() {
		return this.addnlInfo == null ? null : addnlInfo.challanInfoList ;
	}
	public static boolean isSameAddressMoveToGpsProjectUtils(Connection conn, ChallanInfo lhs, ChallanInfo rhs, boolean doDest, StopDirControl stopDirControl) throws Exception {
		if (lhs == null || rhs == null)
			return false;
		IdInfo lhsd = doDest? lhs.getIdInfoWithCalc(conn, false, stopDirControl) : lhs.getSrcIdInfoWithCalc(conn, false, stopDirControl);
		IdInfo rhsd = doDest ? rhs.getIdInfoWithCalc(conn, false, stopDirControl) : lhs.getSrcIdInfoWithCalc(conn, false, stopDirControl);
		if (lhsd == null || rhsd == null || Misc.isUndef(lhsd.getLongitude()) || Misc.isUndef(lhsd.getLatitude()) || Misc.isUndef(rhsd.getLongitude()) || Misc.isUndef(rhsd.getLatitude()))
			return false;
		return Misc.isEqual(lhsd.getLongitude(), rhsd.getLongitude()) && Misc.isEqual(lhsd.getLatitude(), rhsd.getLatitude());
	}
	public static ArrayList<ChallanInfo> getChallanInfoListUniqueDest(Connection conn, ArrayList<ChallanInfo> fullChallanInfoList, boolean doDest, StopDirControl stopDirControl) throws Exception {
		if (fullChallanInfoList == null || fullChallanInfoList.size() <= 1) {
			return fullChallanInfoList;
		}
		ArrayList<ChallanInfo> retval = new ArrayList<ChallanInfo>();
		for (int i=0, is=fullChallanInfoList.size();i<is;i++) {
			boolean foundMatch = false;
			ChallanInfo ch = fullChallanInfoList.get(i);
			for (int j=0,js = retval.size();j<js;j++) {
				boolean same = isSameAddressMoveToGpsProjectUtils(conn, ch, retval.get(j), doDest, stopDirControl);
				if (same) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch)
				retval.add(ch);
		}
		return retval;
	}
	
	public ArrayList<ChallanInfo> getChallanInfoList(Connection conn, NewChallanMgmt extChallanList) {
		if (addnlInfo == null || addnlInfo.challanInfoList == null || addnlInfo.challanInfoList.size() == 0 || extChallanList == null)
			return null;
		ArrayList<ChallanInfo> retval = new ArrayList<ChallanInfo>();
		ChallanInfo dummy = new ChallanInfo(Misc.getUndefInt());
		for (Long chdt : addnlInfo.challanInfoList) {
			if (chdt == null || chdt.longValue() <= 0)
				continue;
			dummy.setChallanDate(chdt.longValue());
			ChallanInfo entry = extChallanList.get(conn, dummy);
			if (entry != null)
				retval.add(entry);
		}
		return retval;
	}
	public void setChallanInfoList(ArrayList<ChallanInfo> challanInfoList) {
		if (addnlInfo == null && (challanInfoList == null || challanInfoList.size() == 0)) 
			return;
		if ((challanInfoList == null || challanInfoList.size() == 0)) {
			this.addnlInfo.challanInfoList = null;
			return;
		}
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		if (addnlInfo.challanInfoList == null)
			addnlInfo.challanInfoList = new ArrayList<Long>();
		else
			addnlInfo.challanInfoList.clear();
		for (ChallanInfo entry : challanInfoList)
			this.addnlInfo.challanInfoList.add(entry.getChallanDate());
	}
	
	
	
	
	public int getDirChangeFromEvent(int st, int enIncl, StopDirControl stopDirControl) {
		LUItem bestDirChange = null;
		for (int i=st,is = enIncl;i<=is;i++) {
			LUItem dirChangeItem = get(i);
			if (dirChangeItem.isDirChange()) {
				//double distFromExit = nestSeqEndData.getValue() - luitem.getGpsData().getValue();
				int dirChange0123 = dirChangeItem == null ? 0 : LUItem.getDir0123(dirChangeItem.getEventId());
				boolean dirChangeProper = stopDirControl.isForProperBothDirMust() ? (dirChange0123 == 3 || dirChange0123 == 8 || dirChange0123 == 9) 
						                                                 : stopDirControl.isForProperHiDirMust() ? (dirChange0123 !=0 && dirChange0123 != 1 && dirChange0123 != 5) 
						                                                 : stopDirControl.isForProperLoDirMust() ? dirChange0123 == 1 || dirChange0123 == 3 || dirChange0123 == 5 || dirChange0123 == 8 || dirChange0123 == 9 
						                                                 : dirChange0123 > 0;
				if (dirChange0123 <= 0)
					dirChangeItem = null;
				int meDirBetter =     
								                           dirChangeItem != null && bestDirChange == null ? 1 : 
								                           dirChangeItem != null && bestDirChange != null ? dirChangeItem.isMeBetterDirChange(bestDirChange) : 
								                           dirChangeItem == null && bestDirChange != null ? -1 : 
								                           0;
				if (meDirBetter > 0)
					bestDirChange = dirChangeItem;
			}
		}
		if (bestDirChange != null)
			return bestDirChange.getDir0123(bestDirChange.getEventId());
		return 0;
	}
	public int getDirChange(int st, int enIncl, StopDirControl stopDirControl) {
		if (st >= 0 && stopDirControl != null) {//get it from event
			return getDirChangeFromEvent(st, enIncl, stopDirControl);
		}
		else {
			return 0;
		}
	}	
	
	private static boolean isEqualWithinArtificial(LUItem item, GpsData d2) {
		return (long)(item.getTime()/1000) ==  (long) (d2.getGps_Record_Time()/1000);
	}
	
	private int removeFullItem(GpsData data) {//returns cnt removed
		Pair<Integer, Boolean> idx = indexOf(new LUItem(data));
		long refSec = data.getGps_Record_Time()/1000;
		int cnt = 0;
		for (int i=idx.first,is = size();i<is;i++) {
			LUItem item = get(i);
			if (item == null)
				continue;
			
			long sec = item.getTime()/1000;
			if (sec > refSec)
				break;
			if (item.isStop() || item.isDirChange()) {
				remove(i);
				i--;
				is--;
				cnt++;
			}
		}
		return cnt;
	}
	
	private Pair<LUItem, Integer> getImmPointCurrOrEqualIgnoreArtificial(GpsData pt, int lastSearchIndex) {
		long dsec = pt.getGps_Record_Time()/1000;
		int i=lastSearchIndex+1;
		int is = size();
		LUItem latestStop = null;
		for (; i<is;i++) {
			LUItem item = get(i);
			if (item == null)
				continue;
			
			long tsec = item.getTime()/1000;
			if (tsec > dsec)
				return new Pair<LUItem, Integer>(latestStop, i-1);
			if (item.isStop())
				latestStop = item;
		}
		return new Pair<LUItem, Integer>(latestStop, is-1);
	}
	
	
	
	public int getMergeTillIndexExcl(int index, FastList<LUSequence> luSequences, int blockEndIndexExcl, StopDirControl stopDirControl, Connection conn, NewVehicleData vdp) {//NOT USED
		int i=index+1;
		LUSequence withRefTo = luSequences.get(index);
		boolean withRefToIsStop = withRefTo.isOpStationForStop();
		double distThreshForMergeKM = 0;
		LUItem distWithRefToForNonStopMustBeEndButForEfficiency =
			withRefTo.getStartItemExt();
			//withRefTo.get(withRefToIsStop ? withRefTo.getStopStartIndex() : withRefTo.getThisOrPrevUsefulIndex(withRefTo.size()-1));
		
		if (withRefToIsStop) {
			distThreshForMergeKM = stopDirControl.getMergeStopsSeqIfDistKM();
		}
		else {
			OpStationBean lhsBean = withRefTo.getOpBelogingBean();
			distThreshForMergeKM = (double)lhsBean.getIntVal(8)/1000.0;
		}
		if (distThreshForMergeKM < 0 || Misc.isEqual(distThreshForMergeKM, 0))
			return i;
		GpsData distWithRefToGpsData =  null;//distWithRefToForNonStopMustBeEndButForEfficiency == null ? null : withRefTo.getStartGps(conn, vdp);
		for (;i < blockEndIndexExcl;i++) {
			LUSequence rhs = luSequences.get(i);
			boolean rhsIsStop = rhs.isOpStationForStop();
			if (rhsIsStop != withRefToIsStop)
				break;
			LUItem rhsSt = rhs.get(rhsIsStop ? rhs.getStopStartIndex() : rhs.getThisOrNextUsefulIndex(0));
			
			if (distWithRefToForNonStopMustBeEndButForEfficiency == null || rhsSt == null)
				break;
			//CHANGE_LUSEQ_START if (distWithRefToGpsData == null)
			//CHANGE_LUSEQ_START 	distWithRefToGpsData = distWithRefToForNonStopMustBeEndButForEfficiency == null ? null : withRefTo.getStartGps(conn, vdp);
			//CHANGE_LUSEQ_START GpsData rhsGpsData = rhs.getStartGps(conn, vdp);
			if (distWithRefToGpsData == null)
				distWithRefToGpsData = distWithRefToForNonStopMustBeEndButForEfficiency.getGpsData(conn, vdp);
			GpsData rhsGpsData = rhsSt.getGpsData(conn, vdp);
			double d = rhsGpsData.getValue() - distWithRefToGpsData.getValue();
			    // orig rhsSt.getValue() - distWithRefToForNonStopMustBeEndButForEfficiency.getValue();
			if (d > distThreshForMergeKM)
				break;
			if (rhsIsStop) {
				withRefTo = rhs;
				distWithRefToForNonStopMustBeEndButForEfficiency = rhsSt;
				distWithRefToGpsData = rhsGpsData; 
			}
			else {
				withRefTo = rhs;
				distWithRefToForNonStopMustBeEndButForEfficiency = rhsSt; //rhs.get(rhs.getThisOrPrevUsefulIndex(rhs.size()-1));
				distWithRefToGpsData = rhsGpsData;
			}
		}
		return i;
	}
	public byte getGuaranteedLoadType() {
		return guaranteedLoadType;
	}
	public void setGuaranteedLoadType(byte guaranteedLoadType) {
		this.guaranteedLoadType = guaranteedLoadType;
		if (guaranteedLoadType !=2 && guaranteedLoadType != -1) {
			this.guessLoadType = guaranteedLoadType;
		}
	}
	public byte getGuessLoadType() {
		return guessLoadType == 3 ? 0 : guessLoadType == 4 ? 1 : guessLoadType;
	}
	public void setGuessLoadType(byte guessLoadType) {
		this.guessLoadType = guessLoadType;
	}
	public boolean isAmBestSeqInBlock() {
		return amBestSeqInBlock;
	}
	public void setAmBestSeqInBlock(boolean amBestSeqInBlock) {
		this.amBestSeqInBlock = amBestSeqInBlock;
	}
	public long getChallanDateFromInternalProc() {
		return addnlInfo == null ? Misc.getUndefInt() : addnlInfo.challanDateFromInternalProc;
	}
	public void setChallanDateFromInternalProc(long challanDateFromInternalProc) {
		if (challanDateFromInternalProc <= 0 && addnlInfo == null)
			return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		this.addnlInfo.challanDateFromInternalProc = challanDateFromInternalProc;
	}
	public boolean getMarkForExitOffWorkArea() {
		return this.addnlInfo == null ? false : addnlInfo.markForExitOffWorkArea; 
	}
	public void setMarkForExitOffWorkArea(boolean markForExitOffWorkArea) {
		if (addnlInfo == null && !markForExitOffWorkArea)
			return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.markForExitOffWorkArea = markForExitOffWorkArea;
	}
	public ArrayList<Integer> getOtherMergedTripid() {
		return addnlInfo == null ? null : addnlInfo.otherMergedTripId;
	}
	public void setOtherMergedTripId(ArrayList<Integer> otherMergedTripId) {
		if (addnlInfo == null && (otherMergedTripId == null || otherMergedTripId.size() == 0))
			return;
		if (addnlInfo == null)
			addnlInfo = new AddnlInfo();
		addnlInfo.otherMergedTripId = otherMergedTripId;
	}
	public int getPrefOrMovingOpStationId() {
		return prefOrMovingOpStationId;
	}
	public void setPrefOrMovingOpStationId(int prefOrMovingOpStationId) {
		this.prefOrMovingOpStationId = prefOrMovingOpStationId;
	}
	public int getAdjRefOpStationId() {
		int retval = Misc.getUndefInt();
		if (prefOrMovingOpStationId > 0) {
			OpStationBean opsb = TripInfoCacheHelper.getOpStation(prefOrMovingOpStationId);
			if (opsb != null)
				retval = opsb.getRefOpStationId();
		}
		if (Misc.isUndef(retval)) {
			OpStationBean opsb =this.getOpStation();
			if (opsb != null)
				retval = opsb.getRefOpStationId();
		}
		return retval;
	}
	public byte getIsChallanMarked() {
		return isChallanMarked;
	}
	public void setIsChallanMarked(byte isChallanMarked) {
		this.isChallanMarked = isChallanMarked;
	}
	public short getStartStrikeRelStart() {
		return startStrikeRelStart;
	}
	public void setStartStrikeRelStart(short startStrikeRelStart) {
		this.startStrikeRelStart = startStrikeRelStart;
	}
	public short getEndStrikeRelStart() {
		return endStrikeRelStart;
	}
	public void setEndStrikeRelStart(short endStrikeRelStart) {
		this.endStrikeRelStart = endStrikeRelStart;
	}
	public byte getShovelAssignQuality() {
		return (byte) (shovelAssignQuality & 0x0F);
	}
	public boolean isInShovelSeq() {
		return (shovelAssignQuality &0x10) != 0;
	}
	public boolean isQualityIsProper() {
		return (shovelAssignQuality &0x20) != 0;
	}
	public void setInShovelSeq(boolean val) {
		if (!val)
			shovelAssignQuality = (byte) (shovelAssignQuality & ~(0x010));
		else
			shovelAssignQuality = (byte) (shovelAssignQuality | (0x010));
	}
	public void setQualityIsProper(boolean val) {
		if (!val)
			shovelAssignQuality = (byte) (shovelAssignQuality & ~(0x020));
		else
			shovelAssignQuality = (byte) (shovelAssignQuality | (0x020));
	}
	public void clearAllShovelStuff() {
		this.shovelAssignQuality = 0;
		this.shovelCycleCount = 0;
	}
	public void setShovelAssignQuality(byte shovelAssignQuality) {
		if (shovelAssignQuality < 0)
			shovelAssignQuality = 0;
		if (shovelAssignQuality > 15)
			shovelAssignQuality = 15;
		this.shovelAssignQuality &= 0xF0;
		
		this.shovelAssignQuality |= shovelAssignQuality;
	}
	public static final long g_maxPreBLEToLookFor =15*60*1000;
	public static final long g_maxPreSeqLoadEventOK = 10*1000;
	public static final long g_maxPreSeqBLEOK = 60*1000;
	public static final long g_maxPostSeqLoadEventOK = 0*1000;
	public static final long g_maxPostSeqBLEEventOK = 10*1000;
	public short getStartDalaRelStart() {
		return startDalaRelStart;
	}
	public void setStartDalaRelStart(short startDalaRelStart) {
		this.startDalaRelStart = startDalaRelStart;
	}
	public short getEndDalaRelStart() {
		return endDalaRelStart;
	}
	public void setEndDalaRelStart(short endDalaRelStart) {
		this.endDalaRelStart = endDalaRelStart;
	}
}

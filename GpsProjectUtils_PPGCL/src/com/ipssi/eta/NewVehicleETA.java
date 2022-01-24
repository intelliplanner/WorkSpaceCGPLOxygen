package com.ipssi.eta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ipssi.eta.NewETAforSrcDestItem.AlertDetails;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.NameLocationLookUp;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.report.cache.CacheValue.LatestEventInfo;
import com.ipssi.routemonitor.RouteDef;
import com.ipssi.tripcommon.LUInfoExtract;
import com.ipssi.userNameUtils.IdInfo;
import com.ipssi.RegionTest.*;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.OrgBasedLovBean;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.*;

public class NewVehicleETA implements Serializable {
//	public final static int ALERT_NEARING_SRC_BACK = 10;
//	public final static int ALERT_STOPPAGE_FORW = 11;
//	public final static int ALERT_STOPPAGE_BACKW = 12;
	private static ArrayList<Integer> g_traceList = new ArrayList<Integer>();
	public static boolean toTrace(int vehicleId) {
		return g_traceList.indexOf(vehicleId) >= 0;
	}
	public static void addToTrace(int vehicleId) {
		if (!toTrace(vehicleId))
			g_traceList.add(vehicleId);
	}
	public static void removeFromTrace(int vehicleId) {
		int idx = g_traceList.indexOf(vehicleId);
		if (idx >= 0)
			g_traceList.remove(idx);
	}
	public static class NextCheck implements Comparable{
		private long ts;
		private int vehicleId;
		public NextCheck(int vehicleId, long l) {
			this.vehicleId = vehicleId;
			this.ts = l;
		}
		public int compareTo(Object o) {
			NextCheck rhs = (NextCheck) o;
			if (o == null)
				return -1;
			long diff = ts-rhs.ts;
			return diff < 0 ? -1 : diff == 0 ? (vehicleId-rhs.vehicleId) : 1;
		}
	}
	private static ConcurrentHashMap<Integer, NewVehicleETA> g_vehicleETACache = new ConcurrentHashMap<Integer, NewVehicleETA>();
	private static boolean g_doAsyncNextCheck = true;	
	private static final long serialVersionUID = 1L;
	
	private int vehicleId = Misc.getUndefInt();
	private ArrayList<MiscInner.PairIntBool> currMasterSrcDestList = null;
	private ArrayList<MiscInner.PairIntBool> currPossibleSrcDestList = null;
	private ArrayList<Pair<NewETAforSrcDestItem, Boolean>> etaInfo = null;
	
	private int currFromOpStationId = Misc.getUndefInt();
	private int currToOpStationId = Misc.getUndefInt();
	private long currFromOpStationInTime = Misc.getUndefInt();
	private long currFroOpStationOutTime = Misc.getUndefInt();
	private long currSrcDestChallanDate = Misc.getUndefInt();
	private long currSrcDestChallanUpdDate = Misc.getUndefInt();
	private long currChallanAssignTime = Misc.getUndefInt();
	private long currToInTime = Misc.getUndefInt();
	private long currToOutTime = Misc.getUndefInt();
	private long lastProcessedGRT = Misc.getUndefInt();
	private ArrayList<MiscInner.Triple> notificationIdsForReceiver = null; //1st = eventTy, 2 = notificationType, 3 = notificationId
	private ArrayList<MiscInner.Triple> notificationIdsForSender = null;//1st = eventTy, 2 = notificationType, 3 = notificationId
	private ArrayList<MiscInner.Triple> notificationIdsForTransporter = null;//1st = eventTy, 2 = notificationType, 3 = notificationId
	private transient GpsData startGpsData = null;
	private transient int dataOpCount = 0;
	private transient boolean dirty = false;
	private transient boolean alertSentThisTime = false;//for trace msg printing
	private transient byte cachedFromNameMode = 0;
	private transient byte cachedToNameMode = 0;
	private transient String cachedFromName = null;
	private transient String cachedToName = null;
	private long lastProcessedEstCal = Misc.getUndefInt();
	private transient boolean mustProcessNextPoint = false;
	public NewVehicleETA(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public static NewVehicleETA getETAObj(Connection conn, int vehicleId)  {
		synchronized (g_vehicleETACache) {
			if (NewVehicleETA.g_vehicleETACache.size() == 0) {
				NewVehicleETA.loadState(conn, null);
			}
		}
		NewVehicleETA retval = NewVehicleETA.g_vehicleETACache.get(vehicleId);
		if (retval == null) {
			NewVehicleETA.checkAndCreateState(conn, vehicleId);
			retval = new NewVehicleETA(vehicleId);
			NewVehicleETA.g_vehicleETACache.put(vehicleId, retval);
		}
		return retval;
	}
	public boolean doUpdateForTrip(Connection conn, int vehicleId, CacheTrack.VehicleSetup  vehSetup, LUInfoExtract fromExt, LUInfoExtract toExt, ChallanInfo challanInfo, StopDirControl stopDirControl, NewVehicleData vdp, long nowTime)  {
		//return true if succ completed
		boolean retval = true;
		try {
			if (nowTime < 0)
				nowTime = System.currentTimeMillis();
			
			StringBuilder traceStr = toTrace(vehicleId) ? new StringBuilder() : null;
			CacheTrack.VehicleSetup.DistCalcControl distCalcControl = vehSetup.getDistCalcControl(conn);
			int doETA = distCalcControl == null ? 0 : distCalcControl.doETAProcesingNew;
			if (doETA == 0)
				return retval;

			boolean toClear = false;
			int fromStationId = fromExt == null ? Misc.getUndefInt() : fromExt.getOfOpStationId();
			int toStationId = toExt == null ? Misc.getUndefInt() : toExt.getOfOpStationId();
			long fromIn = fromExt == null ? Misc.getUndefInt() : fromExt.getGateIn();
			long fromOut = fromExt == null ? Misc.getUndefInt() : fromExt.getWaitOut();
			long toIn = toExt == null ? Misc.getUndefInt() : toExt.getGateIn();
			long toOut = toExt == null ? Misc.getUndefInt() : toExt.getWaitOut();
			if (!tripControlParamsChanged(fromStationId, toStationId, fromIn, fromOut, challanInfo, toIn, toOut)) {
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" no updates doUpdateTrip:").append("From:").append(fromStationId).append(",To:").append(toStationId).append(",Ch:").append(challanInfo == null ? "null" : new java.util.Date(challanInfo.getChallanDate()))
					.append(", FIn:").append(fromIn > 0 ? new java.util.Date(fromIn) : "N/A")
					.append(", FO:").append(fromIn > 0 ? new java.util.Date(fromOut) : "N/A")
					.append(", TIn:").append(fromIn > 0 ? new java.util.Date(toIn) : "N/A")
					.append(", TO:").append(fromIn > 0 ? new java.util.Date(toOut) : "N/A")
					;
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				return retval;
			}
			
			if (challanInfo != null) {
				long newChallanDate = challanInfo.getChallanDate();
				long oldChallanDate = this.currSrcDestChallanDate;
				//if (oldChallanDate > 0 && newChallanDate != oldChallanDate)
				//	toClear = true;
			}
			if (!toClear) {
				if (fromIn != this.currFromOpStationInTime || fromStationId != this.currFromOpStationId)
					toClear = true;
			}
			if (toClear) {
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" Clearing in updateTrip:").append("From:").append(fromStationId).append(",To:").append(toStationId).append(",Ch:").append(challanInfo == null ? "null" : new java.util.Date(challanInfo.getChallanDate()))
					.append(", FIn:").append(fromIn > 0 ? new java.util.Date(fromIn) : "N/A")
					.append(", FO:").append(fromIn > 0 ? new java.util.Date(fromOut) : "N/A")
					.append(", TIn:").append(fromIn > 0 ? new java.util.Date(toIn) : "N/A")
					.append(", TO:").append(fromIn > 0 ? new java.util.Date(toOut) : "N/A")
					;
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				clear();
			}
			boolean toRecalc = toRecalcPossibleETA(fromStationId, toStationId, fromIn, challanInfo);
			//this to be done after checking for toRecalc 
			setInitETAGetParams(fromStationId, toStationId, fromIn, fromOut, challanInfo, toIn, toOut, nowTime);
			if (toRecalc) {
				mustProcessNextPoint = true;
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" Doing Recalc in updateTrip:").append("From:").append(fromStationId).append(",To:").append(toStationId).append(",Ch:").append(challanInfo == null ? "null" : new java.util.Date(challanInfo.getChallanDate()))
					.append(", FIn:").append(fromIn > 0 ? new java.util.Date(fromIn) : "N/A")
					.append(", FO:").append(fromIn > 0 ? new java.util.Date(fromOut) : "N/A")
					.append(", TIn:").append(fromIn > 0 ? new java.util.Date(toIn) : "N/A")
					.append(", TO:").append(fromIn > 0 ? new java.util.Date(toOut) : "N/A")
					;
					traceStr.append("\n[ETA:").append(vehicleId).append(" Pre Master:").append(this.currMasterSrcDestList).append(" Pre Possible:").append(this.currPossibleSrcDestList);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}

				boolean doBackw = true;
				MiscInner.PortInfo ownerPortInfo = Cache.getCacheInstance(conn).getPortInfo(vehSetup == null ? Misc.G_TOP_LEVEL_PORT : vehSetup.m_ownerOrgId, conn);
				if (ownerPortInfo != null) {
					ArrayList<Integer> dbl = ownerPortInfo.getIntParams(OrgConst.ID_DO_ETA_SRCDEST_BACK);
					if (dbl != null && dbl.size() > 0)
						doBackw = 1 == dbl.get(0);
				}
				
				setupRecalc(conn, vehicleId, vehSetup, fromStationId, toStationId, challanInfo, stopDirControl, doBackw, fromOut > 0 ? fromOut : fromIn, toIn > 0 ? toIn : toOut, vdp);
				if (traceStr != null) {
					traceStr.append("\n[ETA:").append(vehicleId).append(" Post Master:").append(this.currMasterSrcDestList).append(" Post Possible:").append(this.currPossibleSrcDestList);
					traceStr.append("\n");
					traceStr.append(this);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}

				saveState(conn, vdp, true, false, nowTime);
			}
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		return retval;
		
	}
	
	public boolean processAllData(Connection conn, long ts, NewVehicleData vdp, StopDirControl stopDirControl, VehicleDataInfo vdf, CacheTrack.VehicleSetup vehSetup, boolean sendAlert, long nowTime)  {
		//returns true if succ completed
		boolean retval = true;
		if (nowTime < 0)
			nowTime = System.currentTimeMillis();
		StringBuilder traceStr = toTrace(vehicleId) ? new StringBuilder() : null;
		
		alertSentThisTime = false;
		int dbgOldBest = this.currPossibleSrcDestList == null || this.currPossibleSrcDestList.size() == 0 ? Misc.getUndefInt() : this.currPossibleSrcDestList.get(0).first;
		try {
			CacheTrack.VehicleSetup.DistCalcControl distCalcControl = vehSetup.getDistCalcControl(conn);
			int doETA = distCalcControl == null ? 0 : distCalcControl.doETAProcesingNew;
			if (doETA == 0)
				return retval;

			boolean processFrom1PtAfter = false;
			boolean mustProcess = this.mustProcessNextPoint;
			this.mustProcessNextPoint = false;
			if (this.lastProcessedGRT > 0 && lastProcessedGRT < ts) {
				ts = lastProcessedGRT;
				processFrom1PtAfter = true;
			}
			if (this.lastProcessedGRT <= 0 && this.currFromOpStationInTime > 0) {
				ts = this.currFromOpStationInTime;
				mustProcess = true;
			}
				
			if (ts < this.currFromOpStationInTime && this.currFromOpStationInTime > 0)
				ts = this.currFromOpStationInTime;
			double distGapThresh = 0.1;
			int minGapThresh = 15;
			if (this.currFromOpStationInTime < 0) {//nothing to process //TODOOUTSIDEPROCESS
				
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" nothing to process:").append(lastProcessedGRT).append(" ts:").append(ts).append(" InTime:").append(this.currFromOpStationInTime);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				
				GpsData prev = vdp.get(conn, new GpsData(lastProcessedGRT));
				GpsData data = vdp.getLast(conn);
				double distGap = prev == null || this.mustProcessNextPoint ? Misc.LARGE_NUMBER : data.getValue() - prev.getValue();
				int minGap = prev == null ? Integer.MAX_VALUE : (int) ((data.getGps_Record_Time() - prev.getGps_Record_Time())/60000);
				if (distGap > distGapThresh ||minGap > minGapThresh) {
					if (traceStr != null) {
						traceStr.append("[ETA:").append(vehicleId).append(" Going to non src dest process for:").append(data);
						System.out.println(traceStr);
						traceStr.setLength(0);
					}
					retval = this.processAndSendNearSrcStoppageAlert(conn, data, vehSetup, stopDirControl, sendAlert, nowTime, vdf) && retval;
					this.mustProcessNextPoint = false;
					this.lastProcessedGRT = data.getGps_Record_Time();
				}
				this.saveState(conn, vdp, false, false, nowTime);//at least keep of lastProcessedGRT
				return retval;
			}
			
			GpsData dummy = new GpsData(ts);
			if (dummy == null)
				return retval;
			GpsData prev = vdp.get(conn, dummy, -1);
			if (processFrom1PtAfter) {
				prev = vdp.get(conn, dummy);
			}
			 
			boolean procData = false;
			int firstSrcDestItemId = this.currPossibleSrcDestList == null || this.currPossibleSrcDestList.size() == 0 ? Misc.getUndefInt() : this.currPossibleSrcDestList.get(0).first;
			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, firstSrcDestItemId);					
			int gapRecurCheck = srcDestInfo == null ? 15 : (int) (Math.round(srcDestInfo.getCheckContDelayHrFreq() < 0.005 ? 1*15 : srcDestInfo.getCheckContDelayHrFreq()*60));

			if (!mustProcess) {//for processing even if not moving for someTime
				if(currToInTime <= 0){
					long prevSentTime = getLastProcessedEstCal();
					int minGap = (int) (prevSentTime > 0 ? (int)((nowTime - prevSentTime)/60000) : Misc.LARGE_NUMBER);
					if (minGap >= gapRecurCheck) {
						mustProcess = true;
					}
				}
			}
			for (int i=processFrom1PtAfter ? 1 : 0;true;i++) {//mustProcess changed after making sure 1st one gets processed
				GpsData data = vdp.get(conn, dummy, i);
				if (data == null)
					break;
				double distGap = prev == null || mustProcess ? Misc.LARGE_NUMBER : data.getValue() - prev.getValue();
				int minGap = prev == null ? Integer.MAX_VALUE : (int) ((data.getGps_Record_Time() - prev.getGps_Record_Time())/60);
				mustProcess = false;
				if (distGap > distGapThresh ||minGap > minGapThresh) {
					if (traceStr != null) {
						traceStr.append("[ETA:").append(vehicleId).append(" Going to process for:").append(data);
						System.out.println(traceStr);
						traceStr.setLength(0);
					}
					retval = processSingleDataPoint(conn, data) && retval;
					procData = true;
					prev = data;
				}
				else {
					if (traceStr != null) {
						traceStr.append("[ETA:").append(vehicleId).append(" Skipped process for:").append(data);
						System.out.println(traceStr);
						traceStr.setLength(0);
					}
				}
			}
			this.lastProcessedGRT = vdp.getLast(conn).getGps_Record_Time();
			if (procData) {
				firstSrcDestItemId = this.currPossibleSrcDestList == null || this.currPossibleSrcDestList.size() == 0 ? Misc.getUndefInt() : this.currPossibleSrcDestList.get(0).first;
				int etaIndex = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, firstSrcDestItemId);
				GpsData lastPoint = null;
				lastPoint = vdp.getLast(conn);
				if (etaIndex >= 0) {
					NewETAforSrcDestItem specificETA = this.etaInfo.get(etaIndex).first;
					retval = processAndSendAlert(conn, specificETA, this.currPossibleSrcDestList.get(0).second, vdp, lastPoint, vehSetup, stopDirControl, vdf, sendAlert, nowTime) && retval;
				}
				else {
					if (traceStr != null) {
						traceStr.append("[ETA:").append(vehicleId).append(" Going to non src dest process(though within process loop though etaInfo is null for:").append(lastPoint);
						System.out.println(traceStr);
						traceStr.setLength(0);
					}
					
				}
				int dbgNewBest = this.currPossibleSrcDestList == null || this.currPossibleSrcDestList.size() == 0 ? Misc.getUndefInt() : this.currPossibleSrcDestList.get(0).first;
				if (traceStr != null) {
					if (dbgNewBest != dbgOldBest || this.alertSentThisTime) {
						traceStr.append("[ETA:").append(vehicleId).append(" After processing: oldBest:").append(dbgOldBest).append(" newBest:")
						.append(dbgNewBest).append(", alertSent:").append(this.alertSentThisTime).append(" Last Pt:").append(lastPoint);
						traceStr.append("\n").append(this);
					}
					else {
						traceStr.append("[ETA:").append(vehicleId).append(" After processing: no change Best:").append(dbgNewBest).append(" Last Pt:").append(lastPoint);
					}
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" Going to non src dest process(though within process loop though etaInfo is null for:").append(lastPoint);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				retval = this.processAndSendNearSrcStoppageAlert(conn, lastPoint, vehSetup, stopDirControl, sendAlert, nowTime, vdf) && retval;
			}
			else {
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" No points to process:").append(this.lastProcessedGRT > 0 ? new java.util.Date(this.lastProcessedGRT): "N/A");
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
			}
			saveState(conn, vdp, false, false, nowTime);
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	private void setupRecalc(Connection conn, int vehicleId, CacheTrack.VehicleSetup vehSetup, NewVehicleData vdp) throws Exception {
		int fromStationId = this.currFromOpStationId;
		int toStationId = this.currToOpStationId;
		ChallanInfo ch = this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
		StopDirControl stopDirControl = vehSetup.getStopDirControl(conn);
		boolean doBackw = true;
		MiscInner.PortInfo ownerPortInfo = Cache.getCacheInstance(conn).getPortInfo(vehSetup == null ? Misc.G_TOP_LEVEL_PORT : vehSetup.m_ownerOrgId, conn);
		if (ownerPortInfo != null) {
			ArrayList<Integer> dbl = ownerPortInfo.getIntParams(OrgConst.ID_DO_ETA_SRCDEST_BACK);
			if (dbl != null && dbl.size() > 0)
				doBackw = 1 == dbl.get(0);
		}
		
		setupRecalc(conn, vehicleId, vehSetup, fromStationId, toStationId, ch, stopDirControl, doBackw, this.currFroOpStationOutTime > 0 ? this.currFroOpStationOutTime : this.currFromOpStationInTime, this.currToInTime > 0 ? this.currToInTime : this.currToOutTime, vdp);	
	}
	private void setupRecalc(Connection conn, int vehicleId, CacheTrack.VehicleSetup vehSetup, int fromStationId, int toStationId, ChallanInfo challanInfo, StopDirControl stopDirControl, boolean doBackw, long fromRefTime, long toRefTime, NewVehicleData vdp) throws Exception {
		ArrayList<Pair<SrcDestInfo, Boolean>> srcDestList = getPossibleSrcDest(conn, vehicleId, vehSetup, fromStationId, toStationId, challanInfo, stopDirControl, doBackw, fromRefTime, toRefTime, vdp);
		adjustEtaInfoForNewGet(srcDestList);
		ArrayList<MiscInner.PairIntBool> srcDestAsInt = NewVehicleETA.helperConvertSrcDestInfoListToId(srcDestList);
		ArrayList<MiscInner.PairIntBool> oldmaster = this.currMasterSrcDestList;
		ArrayList<MiscInner.PairIntBool> oldPossible = this.currPossibleSrcDestList;
		if ((currMasterSrcDestList == null && srcDestAsInt != null) || (currMasterSrcDestList != null && srcDestAsInt == null) || (currMasterSrcDestList != null && srcDestAsInt != null && currMasterSrcDestList.size() != srcDestAsInt.size()))
			dirty = true;
		if (!dirty) {
			for (int i=0,is=currMasterSrcDestList == null ? 0 : currMasterSrcDestList.size(); i<is; i++) {
				if (currMasterSrcDestList.get(i).first != srcDestAsInt.get(i).first || currMasterSrcDestList.get(i).second != srcDestAsInt.get(i).second) {
					dirty = true;
					break;
				}
			}
		}
		this.currMasterSrcDestList = srcDestAsInt;
		boolean possibleInitialized = oldPossible != null;
		this.currPossibleSrcDestList = new ArrayList<MiscInner.PairIntBool>();
		for (int i=0,is=srcDestAsInt == null ? 0 : srcDestAsInt.size(); i<is; i++) {
			int srcDestId = srcDestAsInt.get(i).first;
			if (possibleInitialized) {
				//we will add to possible only if either is new (ie not is oldMaster or was in oldPossible
				boolean isinOldMaster = NewVehicleETA.getSrcDestIndexInIntegerList(oldmaster, srcDestId) >= 0;
				if (!isinOldMaster) {
					currPossibleSrcDestList.add(new MiscInner.PairIntBool(srcDestId, srcDestAsInt.get(i).second));
					dirty = true;
				}
				else {
					boolean isinOldPossible = NewVehicleETA.getSrcDestIndexInIntegerList(oldPossible, srcDestId) >= 0;
					if (isinOldPossible) {
						currPossibleSrcDestList.add(new MiscInner.PairIntBool(srcDestId, srcDestAsInt.get(i).second));
						dirty = true;
					}
				}
			}
			else {
				currPossibleSrcDestList.add(new MiscInner.PairIntBool(srcDestId, srcDestAsInt.get(i).second));
			}
		}
		for (int i=0,is=etaInfo == null ? 0 : etaInfo.size(); i<is; i++) {
			setupTransitDistTime(conn, etaInfo.get(i).first, stopDirControl, vehSetup, vdp);
			dirty = etaInfo.get(i).first.migrateAlertEtcListToNew(conn, null, true, etaInfo.get(i).second) || dirty;
		}
	}
	
	private boolean processForStoppage(int stoppageType, Connection conn, GpsData lastPoint, CacheTrack.VehicleSetup vehSetup, StopDirControl stopDirControl, boolean sendAlert, long nowTime, VehicleDataInfo vdf) {
		boolean retval = true;
		int srcDestId = this.currPossibleSrcDestList != null && this.currPossibleSrcDestList.size() > 0 ? this.currPossibleSrcDestList.get(0).first : Misc.getUndefInt();
		if (srcDestId < 0) {
			//get a srcDestList for the vehicle that has stoppage based
			srcDestId = 21; //HACK for orient //DEBUG13
		}
		if (srcDestId < 0) {
			return retval;
		}
		SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, srcDestId);
		if (srcDestInfo == null)
			return retval;
		StringBuilder traceStr = toTrace(vehicleId) ? new StringBuilder() : null;
		
		Point pt = lastPoint.getPoint();
		boolean inForwTransit = (stoppageType == 1 || stoppageType == 2);// &&  this.currFroOpStationOutTime > 0 && this.currToInTime <= 0;
		boolean inBackwTransit = (stoppageType == 0 || stoppageType == 2);// && (this.currFromOpStationInTime <= 0 || this.currToOutTime > 0);
		boolean hasForwAlert = inForwTransit && srcDestInfo.hasAlertOfType(conn, SrcDestInfo.ALERT_STOPPAGE_FORW);
		boolean hasBackwAlert = inBackwTransit && srcDestInfo.hasAlertOfType(conn, SrcDestInfo.ALERT_STOPPAGE_BACKW);
		if (!hasForwAlert && !hasBackwAlert)
			return retval;
		//check if stopped
		int etaIndex = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, srcDestId);
		if (etaIndex < 0) {
			this.addETAEntry(srcDestInfo, true);
			etaIndex = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, srcDestId);
			dirty = true;
		}
		if (etaIndex >= 0) {
			NewETAforSrcDestItem etaItem = etaIndex >= 0 ? this.etaInfo.get(etaIndex).first : null;

			if (nowTime < 0)
				nowTime = Misc.getSystemTime();
			long currTime = lastPoint.getGps_Record_Time() < nowTime ? nowTime : lastPoint.getGps_Record_Time();
			int idx = -1;
			boolean doForChallanOnly = false;
			LatestEventInfo latestEvent = CacheValue.getLatestEvent(this.getVehicleId(), srcDestInfo.getStoppageRuleId());
			ArrayList<AlertDetails> tempList = etaItem.getAlertDetailsFor(hasForwAlert ? SrcDestInfo.ALERT_STOPPAGE_FORW : SrcDestInfo.ALERT_STOPPAGE_BACKW);
			boolean didSendAlert = false;
			if (latestEvent != null && latestEvent.getEndTime() <= 0) {
				int minGap = (int)((currTime - latestEvent.getStartTime())/60000);	
				long nextAlertDueAt = -1;
				if (minGap >= 0) {
					for (int i=0,is=tempList == null ? 0 : tempList.size(); i<is; i++) {
						if (minGap < tempList.get(i).getFlexId()) 
							break;
						idx = i;
					}
					if (idx >= 0) {
						NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
						long prevSentTime = entry.getAlertSentTime();
						if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
							doForChallanOnly = prevSentTime > 0;
							ChallanInfo challanInfo =  this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : null;
							Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, hasForwAlert ? SrcDestInfo.ALERT_STOPPAGE_FORW : SrcDestInfo.ALERT_STOPPAGE_BACKW, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
							retval = alertResult.first && retval;
							helperRememberNotification(conn,hasForwAlert ? SrcDestInfo.ALERT_STOPPAGE_FORW : SrcDestInfo.ALERT_STOPPAGE_BACKW,alertResult.second);
							entry.setAlertSentTime(currTime);
							dirty = true;
							didSendAlert = true;
						}
					}
					nextAlertDueAt = getNextMinAlertDueAt(latestEvent.getStartTime(), idx+1, tempList, nextAlertDueAt);
				}//if an alert needs to be generated
				
			}//if an ongoing event exists
			else {
				for (int t1=0, t1s = tempList == null ? 0 : tempList.size(); t1<t1s; t1++) {
					tempList.get(t1).setAlertSentTime(-1);
				}
				this.removeNotificationId(conn, hasForwAlert ? SrcDestInfo.ALERT_STOPPAGE_FORW : SrcDestInfo.ALERT_STOPPAGE_BACKW, Misc.getUndefInt(), Misc.getUndefInt());
			}
			
			if (traceStr != null) {
				//TODOtraceStr.append("[ETA:").append(vehicleId).append(" Going to nearing src dest:").append(lastPoint).append(" SrcDest:").append(srcDestId).append(" LatestEvent St:").append(Misc.printDate);
				System.out.println(traceStr);
				traceStr.setLength(0);
			}

		}
		return retval;
	}
	
	private boolean processForBackwNearing(Connection conn, GpsData lastPoint, CacheTrack.VehicleSetup vehSetup, StopDirControl stopDirControl, boolean sendAlert, long nowTime, VehicleDataInfo vdf) {
		//1. check if nearing - if so send Alert
		Point pt = lastPoint.getPoint();
		StringBuilder traceStr = toTrace(vehicleId) ? new StringBuilder() : null;
		
		SrcDestInfo nearing = SrcDestHelper.getPossibleNearSrc(conn, vehicleId, pt.getLongitude(), pt.getLatitude());
		boolean retval = true;
		if (traceStr != null) {
			traceStr.append("[ETA:").append(vehicleId).append(" Going to nearing src dest:").append(lastPoint).append(" SrcDest:").append(nearing == null ? Misc.getUndefInt(): nearing.getId());
			System.out.println(traceStr);
			traceStr.setLength(0);
		}
		if (nearing == null)
			return retval;
		if (nowTime < 0)
			nowTime = Misc.getSystemTime();
		
		long currTime = lastPoint.getGps_Record_Time() < nowTime ? nowTime : lastPoint.getGps_Record_Time();
		
		int srcDestId = nearing.getId();
		//1. TBD make sure this src is in Master and is in currPossible and is at beginning and
		if (false) {
			ArrayList<MiscInner.PairIntBool> oldmaster = this.currMasterSrcDestList;
			ArrayList<MiscInner.PairIntBool> oldPossible = this.currPossibleSrcDestList;
			int indexInMaster = -1;
			int indexInPossible = -1;
			for (int i=0,is = oldmaster == null ? 0 : oldmaster.size(); i<is; i++) {
				if (oldmaster.get(i).first == srcDestId) {
					indexInMaster = i;
					break;
				}
			}
			for (int i=0,is = oldPossible == null ? 0 : oldPossible.size(); i<is; i++) {
				if (oldPossible.get(i).first == srcDestId) {
					indexInPossible = i;
					break;
				}
			}
			if (indexInMaster < 0) {
				oldmaster.add(new MiscInner.PairIntBool(srcDestId, true));
				indexInMaster = oldmaster.size()-1;
				dirty = true;
			}
			if (indexInPossible < 0) {
				oldPossible.add(new MiscInner.PairIntBool(srcDestId, true));
				indexInPossible = oldPossible.size()-1;
				dirty = true;
			}
		}

		int etaIndex = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, srcDestId);
		if (etaIndex < 0) {
			this.addETAEntry(nearing, true);
			etaIndex = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, srcDestId);
			dirty = true;
		}
		
		if (etaIndex >= 0) {
			NewETAforSrcDestItem specificETA = this.etaInfo.get(etaIndex).first;
			double distFromSrc = Misc.isUndef(specificETA.getSrcLon()) ? Misc.getUndefDouble() : pt.fastGeoDistance(specificETA.getSrcLon(), specificETA.getSrcLat());
			boolean didSendAlert = false;
			if (!Misc.isUndef(distFromSrc)) {
				ArrayList<AlertDetails> tempList = specificETA.getAlertDetailsFor(SrcDestInfo.ALERT_NEARING_SRC_BACK);
				int idx = tempList == null ? 0 : tempList.size();
				for (int i= tempList == null ? -1 : tempList.size()-1; i>=0; i--) {
					if (distFromSrc >= tempList.get(i).getFlexId()) {
						break;
					}
					idx = i;
				}
				if (tempList != null && idx < tempList.size()) {
					NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
					long prevSentTime = entry.getAlertSentTime();
					if (prevSentTime <= 0) {//no challan
						Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, specificETA, null, false, SrcDestInfo.ALERT_NEARING_SRC_BACK, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
						retval = alertResult.first && retval;
						helperRememberNotification(conn,SrcDestInfo.ALERT_NEARING_SRC_BACK, alertResult.second);
						entry.setAlertSentTime(currTime);
						didSendAlert = true;
						dirty = true;
					}
				}
				if (traceStr != null) {
					traceStr.append("[ETA:").append(vehicleId).append(" Going to nearing src dest:").append(lastPoint).append(" SrcDest:").append(nearing == null ? Misc.getUndefInt(): nearing.getId()).append(" distFromSrc:").append(distFromSrc).append(" Index:").append(idx).append(" didSendAlert:").append(didSendAlert);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
			}
		}
		if (nearing == null) {
			this.removeNotificationId(conn, SrcDestInfo.ALERT_NEARING_SRC_BACK, Misc.getUndefInt(), Misc.getUndefInt());
		}
		return retval;
	}
	public boolean processAndSendNearSrcStoppageAlert(Connection conn, GpsData lastPoint, CacheTrack.VehicleSetup vehSetup, StopDirControl stopDirControl, boolean sendAlert, long nowTime, VehicleDataInfo vdf)  {
		boolean retval = true;
		boolean backHaul = this.currFromOpStationInTime <= 0 || this.currToOutTime > 0;
		boolean forwHaul = this.currFroOpStationOutTime > 0 && currToOutTime < 0;
		
		if (forwHaul && (this.currPossibleSrcDestList == null || this.currPossibleSrcDestList.size() == 0)) {
			forwHaul = false;
			backHaul = true;
		}
		if (forwHaul) {
			if (this.currToInTime > 0) {//make sure that notification for currDelayedAlertFromDest is closed
				boolean isOpen = isOpenNotificationByEventTy(conn, SrcDestInfo.ALERT_DELAYED_DEST_EXIT);
				if (isOpen)
					forwHaul = false;
			}
		}
		if (backHaul)
			retval =this.processForBackwNearing(conn, lastPoint, vehSetup, stopDirControl, sendAlert, nowTime, vdf) && retval;
		if (forwHaul || backHaul) 
			retval = this.processForStoppage(forwHaul && backHaul ? 2 : forwHaul ? 1 : 0, conn, lastPoint, vehSetup, stopDirControl, sendAlert, nowTime, vdf) && retval; //
		return retval;
	}
	public boolean processAndSendAlert(Connection conn, NewETAforSrcDestItem etaItem, boolean forw, NewVehicleData vdp, GpsData lastPoint, CacheTrack.VehicleSetup vehSetup, StopDirControl stopDirControl, VehicleDataInfo vdf, boolean sendAlert, long nowTime)  {
		boolean retval = true;
		try {
			long nextAlertDueAt = -1;
			if (nowTime < 0)
				nowTime = Misc.getSystemTime();
			long currTime = lastPoint.getGps_Record_Time() < nowTime ? nowTime : lastPoint.getGps_Record_Time();
			if (this.currFromOpStationInTime <= 0) {
				return retval;
			}
			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, etaItem.getSrcDestId());
			if (srcDestInfo == null)
				return retval;
			//see if to send srcAlert
			ArrayList<NewETAforSrcDestItem.AlertDetails> tempList = null;
			boolean srcExited = this.currFroOpStationOutTime > 0;
			
			int idx = -1;
			Point pt = lastPoint.getPoint();
			ChallanInfo challanInfo = null;
			boolean doForChallanOnly = false;
			if (!srcExited) { //No processing for stoppage or nearing src
				idx = -1;
				doForChallanOnly = false;
				tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_DELAYED_SRC_EXIT);
				if (tempList != null && this.currFroOpStationOutTime <= 0 && this.currFromOpStationInTime > 0) {
					int minGap = (int)((currTime - this.currFromOpStationInTime)/60000);
					if (minGap >= 0) {
						for (int i=0,is=tempList == null ? 0 : tempList.size(); i<is; i++) {
							if (minGap < tempList.get(i).getFlexId()) {
								break;
							}
							idx = i;
						}
					}
					if (idx >= 0) {
						NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
						long prevSentTime = entry.getAlertSentTime();
						if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
							doForChallanOnly = prevSentTime > 0;
							challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
							Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_DELAYED_SRC_EXIT, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
							 retval = alertResult.first && retval;
							helperRememberNotification(conn,SrcDestInfo.ALERT_DELAYED_SRC_EXIT, alertResult.second);

							entry.setAlertSentTime(currTime);
							dirty = true;
						}
					}
					nextAlertDueAt = getNextMinAlertDueAt(this.currFromOpStationInTime, idx+1, tempList, nextAlertDueAt); 
				}
				NewVehicleETA.udapteNextAsyncCheck(vehicleId, nextAlertDueAt);
				return retval; 
			}
			else {
				this.removeNotificationId(conn, SrcDestInfo.ALERT_DELAYED_SRC_EXIT, Misc.getUndefInt(), Misc.getUndefInt());
			}
			NewETAEvent eventForDest = null;//etaItem.getETAEvent(forw ? -2 : -1); //do dest reachability based on opStationOut
			if (eventForDest == null && this.currToInTime > 0) {
				eventForDest = new NewETAEvent(-2, this.currToInTime, this.currToOutTime);
			}
			boolean reachedDest = false;
			boolean exittedDest = false;
			if (eventForDest != null) {
				reachedDest = true;
				if (eventForDest.getOutTime() > 0)
					exittedDest = true;
				if (reachedDest) {//generate skipped for last pt
					//skipped intermediate
					ArrayList<SrcDestInfo.WayPoint> wplist = srcDestInfo.getWaypoints(); 
					int prevIndex = forw ? (wplist == null ? -1 : wplist.size()-1) : (wplist == null || wplist.size() == 0 ? -1 : 0);
					NewETAEvent prevEvent = etaItem.getETAEvent(prevIndex);
					if (wplist != null && prevIndex >= 0 && prevIndex < wplist.size() && prevEvent == null) {
						tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE);
						idx = -1;
						for (int i=0,is=tempList == null ? 0 : tempList.size(); i < is; i++) {
							if (prevIndex == tempList.get(i).getFlexId()) {
								idx = i;
								break;
							}
						}
						if (idx >= 0) {
							NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
							long prevSentTime = entry.getAlertSentTime();
							if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
								doForChallanOnly = entry.getAlertSentTime() > 0;
								challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
								Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
								 retval = alertResult.first && retval;
								helperRememberNotification(conn,SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE, alertResult.second);

								entry.setAlertSentTime(currTime);
								dirty = true;
							}
						}
					}
				}//end processing for missed reaching last intermed
				if (exittedDest) {
					//send alert for exit out of dest
					idx = -1;
					doForChallanOnly = false;
					tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_ONEXIT_DEST);
					if (tempList != null && tempList.size() > 0) {
						idx = 0;
					}
					if (idx >= 0) {
						NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
						if (entry != null && (entry.getAlertSentTime() <= 0 || entry.getAlertSentTime() < this.currChallanAssignTime)) {
							doForChallanOnly = entry.getAlertSentTime() > 0;
							challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
							Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_ONEXIT_DEST, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
							 retval = alertResult.first && retval;
							helperRememberNotification(conn,SrcDestInfo.ALERT_ONEXIT_DEST, alertResult.second);

							entry.setAlertSentTime(currTime);
							dirty = true;
						}
					}
					nextAlertDueAt = -1; //no more testing
				}
				if (exittedDest) {
					this.removeNotificationId(conn, SrcDestInfo.ALERT_DELAYED_DEST_EXIT, Misc.getUndefInt(), Misc.getUndefInt());
					NewVehicleETA.udapteNextAsyncCheck(vehicleId, nextAlertDueAt);
					return retval;
				}
				//now for detention ..
				idx = -1;
				doForChallanOnly = false;
				tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_DELAYED_DEST_EXIT);
				if (tempList != null) {
					int minGap = Misc.isUndef(eventForDest.getInTime()) ? Misc.getUndefInt() :  (int)((currTime - eventForDest.getInTime())/60000);
					if (minGap >= 0) {
						for (int i=0,is=tempList == null ? 0 : tempList.size(); i<is; i++) {
							if (minGap < tempList.get(i).getFlexId()) {
								break;
							}
							idx = i;
						}
					}
					if (idx >= 0) {
						NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
						long prevSentTime = entry.getAlertSentTime();
						if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
							doForChallanOnly = entry.getAlertSentTime() > 0;
							challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
							Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult  = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_DELAYED_DEST_EXIT, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
							 retval = alertResult.first && retval;
							helperRememberNotification(conn,SrcDestInfo.ALERT_DELAYED_DEST_EXIT, alertResult.second);

							entry.setAlertSentTime(currTime);
							dirty = true;
						}
					}
					nextAlertDueAt = eventForDest == null ? nextAlertDueAt : getNextMinAlertDueAt(eventForDest.getInTime(), idx+1, tempList, nextAlertDueAt);
				}	
			}// if there was entry for dest reach
			
			if (reachedDest) {
				//TODO - if not notification for delayed dest exit is closed then stoppage alert
				NewVehicleETA.udapteNextAsyncCheck(vehicleId, nextAlertDueAt);
				return retval;
			}
			//generate alerts for dist from src
			tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_SRC);
			idx = -1;
			doForChallanOnly = false;
			double distFromSrc = Misc.isUndef(etaItem.getSrcLon()) ? Misc.getUndefDouble() : pt.fastGeoDistance(etaItem.getSrcLon(), etaItem.getSrcLat());
			int distFromSrcMtr = Misc.isUndef(distFromSrc) ? Misc.getUndefInt() : (int)(distFromSrc*1000);
			for (int i=0,is=tempList == null ? 0 : tempList.size(); i<is; i++) {
				if (distFromSrcMtr < tempList.get(i).getFlexId()) {
					break;
				}
				idx = i;
			}
			if (idx >= 0) {
				NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
				long prevSentTime = entry.getAlertSentTime();
				if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
					doForChallanOnly = entry.getAlertSentTime() > 0;
					challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
					Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_SRC, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
					 retval = alertResult.first && retval;
					helperRememberNotification(conn,SrcDestInfo.ALERT_SRC, alertResult.second);

					entry.setAlertSentTime(currTime);
					dirty = true;
				}
			}
			//generate alerts for dest reachability
			tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_DEST);
			doForChallanOnly = false;
			idx = tempList == null ? 0 : tempList.size();
			double distFromDest = Misc.isUndef(etaItem.getDestLon()) ? Misc.getUndefDouble() : pt.fastGeoDistance(etaItem.getDestLon(), etaItem.getDestLat());
			int distFromDestMtr = Misc.isUndef(distFromDest) ? Misc.getUndefInt() : (int)(distFromDest*1000);
			if (distFromDestMtr >= 0) {
				for (int i= tempList == null ? -1 : tempList.size()-1; i>=0; i--) {
					if (distFromDestMtr >= tempList.get(i).getFlexId()) {
						break;
					}
					idx = i;
				}
			}
			if (tempList != null && idx < tempList.size()) {
				NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
				long prevSentTime = entry.getAlertSentTime();
				if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
					doForChallanOnly = entry.getAlertSentTime() > 0;
					challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
					Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_DEST, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
					 retval = alertResult.first && retval;
					helperRememberNotification(conn,SrcDestInfo.ALERT_DEST, alertResult.second);

					entry.setAlertSentTime(currTime);
					dirty = true;
				}
			}
			
			//now generate alert for intermediates ..
			NewETAEvent latestEvent = etaItem.getLatestEvent();
			int ofIndex = latestEvent == null ? (forw ? -1 : srcDestInfo.getWaypoints() == null ? 0 : srcDestInfo.getWaypoints().size())
					: latestEvent.getOfIndex();
			tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_REACH_INTERMEDIATE);
			idx = -1;
			doForChallanOnly = false;
			for (int i=0,is=tempList == null ? 0 : tempList.size(); i < is; i++) {
				if (ofIndex == tempList.get(i).getFlexId()) {
					idx = i;
					break;
				}
			}
			if (idx >= 0) {
				NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
				long prevSentTime = entry.getAlertSentTime();
				if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
					doForChallanOnly = entry.getAlertSentTime() > 0;
					challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
					Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_REACH_INTERMEDIATE, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
					 retval = alertResult.first && retval;
					helperRememberNotification(conn,SrcDestInfo.ALERT_REACH_INTERMEDIATE, alertResult.second);

					entry.setAlertSentTime(currTime);
					dirty = true;
				}
			}
			int nextIndex =forw ? ofIndex+1 : ofIndex-1;
			long etaForInter = getIntermediateETA(etaItem, nextIndex);
			
			if (!Misc.isUndef(etaForInter)) {
				int mingap = etaForInter > 0 ? (int)((currTime-etaForInter)/60000) : Misc.getUndefInt();
				boolean isDelayed =  (mingap > srcDestInfo.getNotReachThresholdHr()*60);
				if (isDelayed) {
					tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_DELAY_INTERMEDIATE);
					idx = -1;
					for (int i=0,is=tempList == null ? 0 : tempList.size(); i < is; i++) {
						if (nextIndex == tempList.get(i).getFlexId()) {
							idx = i;
							break;
						}
					}
					if (idx >= 0) {
						NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
						long prevSentTime = entry.getAlertSentTime();
						if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
							long estETA = this.getEstETA(etaItem);
							if (estETA < nowTime)
								this.calcEstETA(etaItem, conn, vehSetup, vdp, lastPoint, nowTime);
							doForChallanOnly = entry.getAlertSentTime() > 0;
							challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
							Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_DELAY_INTERMEDIATE, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
							 retval = alertResult.first && retval;
							helperRememberNotification(conn,SrcDestInfo.ALERT_DELAY_INTERMEDIATE, alertResult.second);

							entry.setAlertSentTime(currTime);
							dirty = true;
						}
					}
				}
				else {
					long templ1 = etaForInter + (long)(srcDestInfo.getNotReachThresholdHr()*3600*1000);
					if (nextAlertDueAt <= 0 || templ1 < nextAlertDueAt)
						nextAlertDueAt = templ1;
				}
			}
			
			//skipped intermediate
			int prevIndex = forw ? ofIndex-1 : ofIndex+1;
			NewETAEvent prevEvent = etaItem.getETAEvent(prevIndex);
			if (srcDestInfo.getWaypoints() != null && prevIndex >= 0 && prevIndex < srcDestInfo.getWaypoints().size() && prevEvent == null) {
				tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE);
				idx = -1;
				for (int i=0,is=tempList == null ? 0 : tempList.size(); i < is; i++) {
					if (prevIndex == tempList.get(i).getFlexId()) {
						idx = i;
						break;
					}
				}
				if (idx >= 0) {
					NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
					long prevSentTime = entry.getAlertSentTime();
					if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
						doForChallanOnly = entry.getAlertSentTime() > 0;
						challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
						Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
						 retval = alertResult.first && retval;
						helperRememberNotification(conn,SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE, alertResult.second);

						entry.setAlertSentTime(currTime);
						dirty = true;
					}
				}
			}
	
			
			//generate alert for non reachability
			long eta = this.getCurrETA(etaItem);
			int minGap = Misc.isUndef(eta) ? Misc.getUndefInt() :  (int) (currTime-eta)/(60000);
			tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_NONREACH_DEST);
			idx = -1;
			if (!Misc.isUndef(eta) && minGap >= 0) {
				for (int i=0,is=tempList == null ? 0 : tempList.size(); i<is; i++) {
					if (minGap < tempList.get(i).getFlexId()) {
						break;
					}
					idx = i;
				}
			}
			if (idx >= 0) {
				
				NewETAforSrcDestItem.AlertDetails entry = tempList.get(idx);
				long prevSentTime = entry.getAlertSentTime();
				if (prevSentTime <= 0 || prevSentTime < this.currChallanAssignTime) {
					long estETA = this.getEstETA(etaItem);
					if (estETA < nowTime)
						this.calcEstETA(etaItem, conn, vehSetup, vdp, lastPoint, nowTime);
					doForChallanOnly = entry.getAlertSentTime() > 0;
					challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
					Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_NONREACH_DEST, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
					 retval = alertResult.first && retval;
					helperRememberNotification(conn,SrcDestInfo.ALERT_NONREACH_DEST, alertResult.second);

					entry.setAlertSentTime(currTime);
					dirty = true;
				}
			}
			nextAlertDueAt = this.currFroOpStationOutTime <= 0 ? nextAlertDueAt : getNextMinAlertDueAt(this.currFroOpStationOutTime, idx+1, tempList, nextAlertDueAt);
			
			
			//new for re-calculate after given freq
			if(currToInTime <= 0){
				int gapRecurCheck = (int) (Math.round(srcDestInfo.getCheckContDelayHrFreq() < 0.005 ? 1*30 : srcDestInfo.getCheckContDelayHrFreq()*60));
				long prevSentTime = getLastProcessedEstCal();
				minGap = (int) (prevSentTime > 0 ? (int)((currTime - prevSentTime)/60000) : Misc.LARGE_NUMBER);
				boolean runningLate = false;
				int minETAGap = Misc.getUndefInt();
				if (minGap >= gapRecurCheck) {
					long currEstETA = this.calcEstETA(etaItem, conn, vehSetup, vdp, lastPoint, nowTime);
					long specETA = getCurrETA(etaItem);
					if(!Misc.isUndef(currEstETA) && !Misc.isUndef(specETA)){
						minGap = (int) ((currEstETA - specETA)/60000);
						runningLate = minGap > srcDestInfo.getNotReachThresholdHr()*60;
						eta = this.getCurrETA(etaItem);
						minETAGap = (int) (currTime-eta)/(60000);
					}
					setLastProcessedEstCal(currTime);
				}
				idx = -1;
				tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_DELAY_CONT);
				idx = tempList == null || tempList.size() == 0 ? -1 : 0;
				NewETAforSrcDestItem.AlertDetails entry = idx >= 0 ? tempList.get(idx) : null;
				if (entry != null) {
					//reestimate
					if (runningLate && minETAGap <= 0 && !Misc.isUndef(minETAGap)) {
						doForChallanOnly = false;
						challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
						Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_DELAY_CONT, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
						 retval = alertResult.first && retval;
						helperRememberNotification(conn,SrcDestInfo.ALERT_DELAY_CONT, alertResult.second);

						dirty = true;
					}	
					entry.setAlertSentTime(currTime);//note that we are updating even if not send .. so that next check can happen correctly
					long tempNext = entry.getAlertSentTime() + entry.getAlertSentTime()*60000;
					if (nextAlertDueAt < 0 || tempNext < nextAlertDueAt)
						nextAlertDueAt = tempNext;
				}
			}
			
			if(false){
				idx = -1;
				tempList = etaItem.getAlertDetailsFor(SrcDestInfo.ALERT_DELAY_CONT);
				idx = tempList == null || tempList.size() == 0 ? -1 : 0;
				NewETAforSrcDestItem.AlertDetails entry = idx >= 0 ? tempList.get(idx) : null;
				if (entry != null) {

					long prevSentTime = entry.getAlertSentTime();
					minGap = (int) (prevSentTime > 0 ? (int)((currTime - prevSentTime)/60000) : Misc.LARGE_NUMBER);
					int gapRecurCheck = (int) (Math.round(srcDestInfo.getCheckContDelayHrFreq() < 0.005 ? 1*60 : srcDestInfo.getCheckContDelayHrFreq()*60)); 
					if (minGap >= gapRecurCheck) {
						//reestimate
						long currEstETA = this.calcEstETA(etaItem, conn, vehSetup, vdp, lastPoint, nowTime);
						long specETA = getCurrETA(etaItem);
						minGap = (int) ((currEstETA - specETA)/60000);
						boolean runningLate = currEstETA >0 && specETA > 0 && minGap > srcDestInfo.getNotReachThresholdHr()*60;
						eta = this.getCurrETA(etaItem);
						int minETAGap = (int) (currTime-eta)/(60000);
						if (runningLate && minETAGap <= 0) {
							doForChallanOnly = false;
							challanInfo = challanInfo == null && this.currSrcDestChallanDate > 0 ? this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate) : challanInfo;
							Pair<Boolean, ArrayList<MiscInner.Triple>>alertResult = NewETAAlertHelper.handleSendAlertAndEvent(conn, vehicleId, this, etaItem, challanInfo, doForChallanOnly, SrcDestInfo.ALERT_DELAY_CONT, entry.getFlexId(), nowTime, lastPoint, stopDirControl, vdf, sendAlert, vehSetup);
							 retval = alertResult.first && retval;
							helperRememberNotification(conn,SrcDestInfo.ALERT_DELAY_CONT, alertResult.second);

							dirty = true;
						}	
						entry.setAlertSentTime(currTime);//note that we are updating even if not send .. so that next check can happen correctly

					}
					long tempNext = entry.getAlertSentTime() + entry.getAlertSentTime()*60000;
					if (nextAlertDueAt < 0 || tempNext < nextAlertDueAt)
						nextAlertDueAt = tempNext;
				}
			}
			NewVehicleETA.udapteNextAsyncCheck(vehicleId, nextAlertDueAt);
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		
		return retval;
		
		//whew ... done
	}
	public ChallanInfo getChallanInfo(Connection conn, ChallanInfo ifCalcEarlier) {
		if (ifCalcEarlier != null)
			return ifCalcEarlier;
		return this.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
	}
	
	public double calcDistTravelled(Connection conn, NewVehicleData vdp, GpsData lastPoint) {
		if (lastPoint == null) {
			lastPoint = vdp.getLast(conn);
		}
		GpsData start = this.getStartGpsData(conn, vdp);
		double distTravelled = start == null || lastPoint == null ? Misc.getUndefDouble() : lastPoint.getValue() - start.getValue();
		return distTravelled;
	}
	public double calcDistRemaining(NewETAforSrcDestItem etaItem, Connection conn, CacheTrack.VehicleSetup vehSetup, NewVehicleData vdp, GpsData lastPoint, double distTravelled) {
		if (etaItem == null)
			return Misc.getUndefDouble();
		if (Misc.isUndef(distTravelled))
			distTravelled = calcDistTravelled(conn, vdp, lastPoint); 
		double invoiceDist = etaItem.getTransitDist();
		Triple<Double, Double, Double> distMultKmPerDayMaxDurPerDelivery = ChallanUpdHelper.getDistFactorKmPerDayDeliveryTime(conn, vehSetup);
		double distMultFactor = distMultKmPerDayMaxDurPerDelivery == null ? 1.3 : distMultKmPerDayMaxDurPerDelivery.first;
		if (Misc.isUndef(distMultFactor))
			distMultFactor = 1.3;
		double distToDest = !Misc.isUndef(lastPoint.getLongitude()) && !Misc.isUndef(etaItem.getDestLon()) ?
				Point.fastGeoDistance(lastPoint.getLongitude(), lastPoint.getLatitude(), etaItem.getDestLon(), etaItem.getDestLat()) * distMultFactor
				: Misc.getUndefDouble();
		double distRemaining = Misc.isUndef(invoiceDist) || Misc.isUndef(etaItem.getTransitDist()) ? Misc.getUndefDouble() : etaItem.getTransitDist() - distTravelled;
		double estDistRemaining = distRemaining < 0.0005 ? distToDest : distRemaining < distToDest ? distToDest : distRemaining;
		if (estDistRemaining < 0.0005)
			estDistRemaining = Misc.getUndefDouble();
		return estDistRemaining;
	}
	
	public long calcEstETA(NewETAforSrcDestItem etaItem, Connection conn, CacheTrack.VehicleSetup vehSetup, NewVehicleData vdp, GpsData lastPoint, long nowTime) {
		if (etaItem == null)
			return Misc.getUndefInt();
		double invoiceDist = etaItem.getTransitDist();
		GpsData start = this.getStartGpsData(conn, vdp);
		if (lastPoint == null)
			lastPoint = vdp.getLast(conn);
		
		double distTravelled = this.calcDistTravelled(conn, vdp, lastPoint);
		double estDistRemaining = this.calcDistRemaining(etaItem, conn, vehSetup, vdp, lastPoint, distTravelled);
		
		Triple<Double, Double, Double> distMultKmPerDayMaxDurPerDelivery = ChallanUpdHelper.getDistFactorKmPerDayDeliveryTime(conn, vehSetup);
		double distMultFactor = distMultKmPerDayMaxDurPerDelivery == null ? 1.3 : distMultKmPerDayMaxDurPerDelivery.first;
		double kmPerDay = distMultKmPerDayMaxDurPerDelivery == null ? Misc.getUndefDouble() : distMultKmPerDayMaxDurPerDelivery.second;
		double dayRemaining = Misc.getUndefDouble();
		
		if (estDistRemaining > 0.005) {
			kmPerDay = NewVehicleETA.getKMPerDay(conn, vehSetup, estDistRemaining, kmPerDay);
			dayRemaining = estDistRemaining/kmPerDay;
			dayRemaining /= etaItem.ourOverUserTransitEstMult;
		}
		if (!Misc.isUndef(dayRemaining)) {
			long ts = this.currFroOpStationOutTime;
			if (ts <= 0)
				ts = this.currFromOpStationInTime;
			//System.out.println("[ETAEST]"+new java.util.Date(lastPoint.getGps_Record_Time())+","+new java.util.Date(ts)+","+(double)(lastPoint.getGps_Record_Time()-ts)/(24*3600*1000));
			if(ts > 0 && lastPoint.getGps_Record_Time() > ts)
				dayRemaining += (double)(lastPoint.getGps_Record_Time() - ts)/(double)(24*60*60*1000);
		}
		etaItem.setLatestEstTransitTime(dayRemaining);
		if (Misc.isUndef(dayRemaining))
			return Misc.getUndefInt();
		return this.getEstETA(etaItem);
	}
	
	public int isDelayed(Connection conn, NewETAforSrcDestItem specificETA) {//0 if not delayed, 1 if est > curr by thres, 2 if curr > nowTime
		if (specificETA == null)
			return 0;
		long estETA = this.getEstETA(specificETA);
		long currETA = this.getCurrETA(specificETA);
		if (currETA > 0 && currETA < Misc.getSystemTime())
			return 2;
		if (estETA > 0 && currETA > 0 && estETA > currETA) {
			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, specificETA.getSrcDestId());
			if (srcDestInfo != null && (estETA-currETA)/(1000*60) >= srcDestInfo.getNotReachThresholdHr()) {
				return 1;
			}
		}
		return 0;
	}
	public GpsData getStartGpsData(Connection conn, NewVehicleData vdp) {
		GpsData toLookFor = startGpsData;
		boolean returnPassedData = true;
		long ts = currFroOpStationOutTime;
		if (ts <= 0)
			ts = this.currFromOpStationInTime;
		if (toLookFor == null) {
			returnPassedData = false;
			toLookFor = new GpsData(ts);
		}
		return vdp.getSinglePoint(conn, toLookFor, returnPassedData);
	}
	
	public void setupTransitDistTimeBeingCorrected(Connection conn, NewETAforSrcDestItem etaItem, StopDirControl stopDirControl, CacheTrack.VehicleSetup vehSetup, NewVehicleData vdp)  {
		//estimate distances, estimate transit time (dest as well as intermediates)
		//Motivation: Use from Setup (the opstation as the start pt). First check if invoices given in challan, then use this and adjust
		// if distance given in SrcDestInfo, then use that and adjust accordingly ..
		
		SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, etaItem.getSrcDestId());
		if (srcDestInfo == null)
			return;
		int srcDestId = srcDestInfo.getId();
		int idxInMaster = NewVehicleETA.getSrcDestIndexInIntegerList(this.currMasterSrcDestList, srcDestId);
		if (idxInMaster < 0)
			return; //something screwed up
		if (vehSetup == null)
			return;
		//first get src Lon/lat and dest lon/lat
		//How - if obtained from challan then that is it, else if objtained from setup thats it else from SrcDest
		//         - Note that if  obtained from challan ... then actual current from setup might be different and will
		//         delta will need to be updated accordingly ...
		int fromStationIdSetup = this.currFromOpStationId;
		int toStationIdSetup = this.currToOpStationId; //may be undef
		MiscInner.PairDouble tempd = SrcDestHelper.getLonLat(conn, fromStationIdSetup, Misc.getUndefInt());
		double fromLonSetup = tempd.first;
		double fromLatSetup = tempd.second;
		if (Misc.isUndef(fromLonSetup)) {
			long refTs = this.currFroOpStationOutTime > 0 ? this.currFroOpStationOutTime : this.currFromOpStationInTime;
			if (refTs > 0) {
				GpsData refdata = vdp.getSinglePoint(conn, new GpsData(refTs+1));
				if (refdata != null) {
					fromLonSetup = refdata.getLongitude();
					fromLatSetup = refdata.getLatitude();
				}
			}
		}
		
		tempd = SrcDestHelper.getLonLat(conn, toStationIdSetup, Misc.getUndefInt());
		double toLonSetup = tempd.first;
		double toLatSetup = tempd.second;
		if (Misc.isUndef(toLonSetup)) {
			long refTs = this.currToInTime > 0 ? this.currToInTime : this.currToOutTime;
			if (refTs > 0) {
				GpsData refdata = vdp.getSinglePoint(conn, new GpsData(refTs+1));
				if (refdata != null) {
					toLonSetup = refdata.getLongitude();
					toLatSetup = refdata.getLatitude();
				}
			}
		}
		
		boolean doForw = this.currMasterSrcDestList.get(idxInMaster).second;
		double fromLonSD = srcDestInfo.getSrcLong();
		double fromLatSD = srcDestInfo.getSrcLat();
		double fromBufSD = srcDestInfo.getSrcBuffer();
		double toLonSD = srcDestInfo.getDestLong();
		double toLatSD = srcDestInfo.getDestLat();
		double toBufSD = srcDestInfo.getDestBuffer();
		
		if (!doForw) {
			double tempd1 = fromLonSD;
			fromLonSD = toLonSD;
			toLonSD = tempd1;
			
			tempd1 = fromLatSD;
			fromLatSD = toLatSD;
			toLatSD = tempd1;
			
			tempd1 = fromBufSD;
			fromBufSD = toBufSD;
			toBufSD = tempd1;
		}
		
		ChallanInfo challanInfo = null;
		if (this.currSrcDestChallanDate > 0) {
			challanInfo = getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
		}
		IdInfo srcIdInfo = challanInfo == null ? null : challanInfo.getSrcIdInfoWithCalc(conn, false, stopDirControl);
		IdInfo destIdInfo = challanInfo == null ? null : challanInfo.getIdInfoWithCalc(conn, false, stopDirControl);
		
		int fromStationChallan = challanInfo != null ? challanInfo.getFromStationId() : Misc.getUndefInt();
		int toStationChallan = challanInfo != null ? challanInfo.getToStationId() : Misc.getUndefInt();
		double fromLonChallan = srcIdInfo != null ? srcIdInfo.getLongitude() : Misc.getUndefDouble();
		double fromLatChallan = srcIdInfo != null ? srcIdInfo.getLatitude() : Misc.getUndefDouble();
		double toLonChallan = destIdInfo != null ? destIdInfo.getLongitude() : Misc.getUndefDouble();
		double toLatChallan = destIdInfo != null ? destIdInfo.getLatitude() : Misc.getUndefDouble();
		
		
		double useSrcLon = fromLonSD;
		double useSrcLat = fromLatSD;
		double useDestLon = toLonSD;
		double useDestLat = toLatSD;
		int fromPointType = 0; //0 => from SrcDest, 1=>set up, 2=> from challan 
		//... fromPointType == 2 => there might be already travelled dist and already travelled time
		int toPointType = 0;
		
		if (!Misc.isUndef(fromLonChallan)) {
			useSrcLon = fromLonChallan;
			useSrcLat = fromLatChallan;
			fromPointType = 2;
		}
		else if (!Misc.isUndef(fromLonSetup)) {
			useSrcLon = fromLonSetup;
			useSrcLat = fromLatSetup;
			fromPointType = 1;
		}
		
		if (!Misc.isUndef(toLonChallan)) {
			useDestLon = toLonChallan;
			useDestLat = toLatChallan;
			toPointType = 2;
		}
		else if (!Misc.isUndef(toLonSetup)) {
			useDestLon = toLonSetup;
			useDestLat = toLatSetup;
			toPointType = 1;
		}
		etaItem.setSrcLon(useSrcLon);
		etaItem.setSrcLat(useSrcLat);
		etaItem.setDestLon(useDestLon);
		etaItem.setDestLat(useDestLat);
		double invoiceDist = challanInfo == null ? Misc.getUndefDouble() : challanInfo.getInvoiceDistKM(conn, stopDirControl);
		
		
		if (invoiceDist < 0.005)
			invoiceDist = Misc.getUndefDouble();
		
		Triple<Double, Double, Double> distMultKmPerDayMaxDurPerDelivery = ChallanUpdHelper.getDistFactorKmPerDayDeliveryTime(conn, vehSetup);
		
		double distMultFactor = distMultKmPerDayMaxDurPerDelivery == null ? 1.3 : distMultKmPerDayMaxDurPerDelivery.first;
		double adjustStart = adjustForOffPoint(useSrcLon, useSrcLat, fromLonSD, fromLatSD, toLonSD, toLatSD, distMultFactor);
		double adjustEnd = adjustForOffPoint(useDestLon, useDestLat, toLonSD, toLatSD, fromLonSD, fromLatSD, distMultFactor);
		double invoiceDistSD = srcDestInfo.getTransitDist();
		if (Misc.isUndef(fromLonSD) || Misc.isUndef(toLonSD))
			invoiceDistSD = Misc.getUndefDouble();
		if (invoiceDistSD < 0.005)
			invoiceDistSD = Misc.getUndefDouble();
		
		if (Misc.isUndef(invoiceDist)) {
			if (distMultFactor < 0.005)
				distMultFactor = 1.3;
			double distPerEndPt = invoiceDistSD;
			if (!Misc.isUndef(useSrcLon) && !Misc.isUndef(useDestLon)) {
				distPerEndPt = Point.fastGeoDistance(useSrcLon, useSrcLat, useDestLon, useDestLat);
				distPerEndPt *= distMultFactor;
			}
			if (Misc.isUndef(invoiceDistSD) || (distPerEndPt < 0.6*invoiceDistSD) || (distPerEndPt >1.4*invoiceDistSD)) {
				invoiceDist = distPerEndPt;
			}
			else {//use invoiceDistSD as basis but adjust for distance of useSrc to SD src and useDest to SD dest
				invoiceDist = invoiceDistSD;
				invoiceDist += adjustStart;
				invoiceDist += adjustEnd;
				if (!Misc.isUndef(distPerEndPt) && (distPerEndPt < 0.6*invoiceDist || distPerEndPt > 1.4*invoiceDist)) {
					invoiceDist = distPerEndPt;
				}
			}
		}
		if (invoiceDist < 0.005)
			invoiceDist = Misc.getUndefDouble();
		
		//Now we need to calculate transitTime .... but first we check if SD is usable for intermediate points
		boolean isSDUsable = false;
		if (!Misc.isUndef(fromLonSD) && !Misc.isUndef(toLonSD)) {
			//will be usable if absolute of adjustStart is not too off, adjustEnd is not too off
			if (!Misc.isUndef(invoiceDistSD) && !Misc.isUndef(invoiceDist)) {
				if (invoiceDist >= 0.6*invoiceDistSD && invoiceDist <= 1.4*invoiceDistSD)
					isSDUsable = true;
			}
		}
		//Now we have invoiceDist and need to calculate exit time from src and transitTime
		double alreadyTavelledDist = 0;
		if (fromPointType == 2 && !Misc.isUndef(invoiceDist)) {//got from from Challan ... so adjust for already travelled
			alreadyTavelledDist = adjustForOffPoint(fromLonSetup, fromLatSetup, useSrcLon, useSrcLat, useDestLon, useDestLat, distMultFactor);
			invoiceDist += alreadyTavelledDist;
			adjustStart += alreadyTavelledDist;
		}
		double userInvoiceDist = challanInfo == null ? Misc.getUndefDouble() : challanInfo.getInvoiceDistKM(conn, stopDirControl);
		if (userInvoiceDist < 0.0001 && isSDUsable)
			userInvoiceDist = srcDestInfo.getTransitDist();
		double userTransitDay =challanInfo ==null ? Misc.getUndefDouble() : challanInfo.getAuthTimeMin(conn, stopDirControl)/(24*60);
		double transitSD = srcDestInfo.getTransitTime();
		if (userTransitDay < 0.0001 && isSDUsable)
			userTransitDay = transitSD;
		double transitDays = Misc.getUndefDouble();
		if (userTransitDay < 0.0001)
			userTransitDay = Misc.getUndefDouble();
		if (!Misc.isUndef(userTransitDay) && !Misc.isUndef(userInvoiceDist)) {
			transitDays = userTransitDay * invoiceDist/userInvoiceDist;
			double kmPerDay = distMultKmPerDayMaxDurPerDelivery == null ? Misc.getUndefDouble() : distMultKmPerDayMaxDurPerDelivery.second;
			kmPerDay = getKMPerDay(conn, vehSetup, userInvoiceDist, kmPerDay);
			double userTransitPerUs = userInvoiceDist/kmPerDay;
			etaItem.ourOverUserTransitEstMult = userTransitPerUs/transitDays;
		}
		else if (!Misc.isUndef(invoiceDist)) {
			double kmPerDay = distMultKmPerDayMaxDurPerDelivery == null ? Misc.getUndefDouble() : distMultKmPerDayMaxDurPerDelivery.second;
			kmPerDay = getKMPerDay(conn, vehSetup, invoiceDist, kmPerDay);
			transitDays = invoiceDist/kmPerDay; 
		}
		etaItem.setTransitDist(invoiceDist);
		etaItem.setTransitTime(transitDays);
		etaItem.setSdUsable(isSDUsable);
		ArrayList<SrcDestInfo.WayPoint> waypointList = srcDestInfo.getWaypoints();
		if (waypointList != null && waypointList.size() > 0) {
			ArrayList<NewETAforSrcDestItem.WayPointETA> intermediateList = etaItem.getIntermediateTransit();
			if (intermediateList == null) {
				intermediateList = new ArrayList<NewETAforSrcDestItem.WayPointETA>();
				etaItem.setIntermediateTransit(intermediateList);
			}
			for (int i=0,is=waypointList.size(); i<is; i++) {
				intermediateList.add(new NewETAforSrcDestItem.WayPointETA (Misc.getUndefDouble(), waypointList.get(i).getLongitude(), waypointList.get(i).getLatitude()));
			}
			if (!Misc.isUndef(invoiceDistSD) && isSDUsable) { 
				double adjustStartDays = adjustStart/invoiceDistSD*transitSD;
				if (doForw) {
					
					for (int i=0,is=waypointList.size(); i<is; i++) {
						if (Misc.isUndef(waypointList.get(i).getTransit()))
							continue;	
						double adjTransit = waypointList.get(i).getTransit()+adjustStartDays;
						if (adjTransit < 0.005)
							adjTransit = 0;
						intermediateList.get(i).setTransitTimeFromSrc(adjTransit);
					}	
				}
				else {
					for (int i=0,is=waypointList.size(); i<is; i++) {
						double adjTransit = transitSD - waypointList.get(i).getTransit()+adjustStartDays;
						if (adjTransit < 0.005)
							adjTransit = 0;
						intermediateList.get(i).setTransitTimeFromSrc(adjTransit);
					}
				}
			}
		}
	}

	public void setupTransitDistTime(Connection conn, NewETAforSrcDestItem etaItem, StopDirControl stopDirControl, CacheTrack.VehicleSetup vehSetup, NewVehicleData vdp)  {
		//estimate distances, estimate transit time (dest as well as intermediates)
		SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, etaItem.getSrcDestId());
		if (srcDestInfo == null)
			return;
		int srcDestId = srcDestInfo.getId();
		int idxInMaster = NewVehicleETA.getSrcDestIndexInIntegerList(this.currMasterSrcDestList, srcDestId);
		if (idxInMaster < 0)
			return; //something screwed up
		if (vehSetup == null)
			return;
		//first get src Lon/lat and dest lon/lat
		//How - if obtained from challan then that is it, else if objtained from setup thats it else from SrcDest
		//         - Note that if  obtained from challan ... then actual current from setup might be different and will
		//         delta will need to be updated accordingly ...
		int fromStationIdSetup = this.currFromOpStationId;
		int toStationIdSetup = this.currToOpStationId; //may be undef
		MiscInner.PairDouble tempd = SrcDestHelper.getLonLat(conn, fromStationIdSetup, Misc.getUndefInt());
		double fromLonSetup = tempd.first;
		double fromLatSetup = tempd.second;
		if (Misc.isUndef(fromLonSetup)) {
			long refTs = this.currFroOpStationOutTime > 0 ? this.currFroOpStationOutTime : this.currFromOpStationInTime;
			if (refTs > 0) {
				GpsData refdata = vdp.getSinglePoint(conn, new GpsData(refTs+1));
				if (refdata != null) {
					fromLonSetup = refdata.getLongitude();
					fromLatSetup = refdata.getLatitude();
				}
			}
		}
		
		tempd = SrcDestHelper.getLonLat(conn, toStationIdSetup, Misc.getUndefInt());
		double toLonSetup = tempd.first;
		double toLatSetup = tempd.second;
		if (Misc.isUndef(toLonSetup)) {
			long refTs = this.currToInTime > 0 ? this.currToInTime : this.currToOutTime;
			if (refTs > 0) {
				GpsData refdata = vdp.getSinglePoint(conn, new GpsData(refTs+1));
				if (refdata != null) {
					toLonSetup = refdata.getLongitude();
					toLatSetup = refdata.getLatitude();
				}
			}
		}
		
		boolean doForw = this.currMasterSrcDestList.get(idxInMaster).second;
		double fromLonSD = srcDestInfo.getSrcLong();
		double fromLatSD = srcDestInfo.getSrcLat();
		double fromBufSD = srcDestInfo.getSrcBuffer();
		double toLonSD = srcDestInfo.getDestLong();
		double toLatSD = srcDestInfo.getDestLat();
		double toBufSD = srcDestInfo.getDestBuffer();
		
		if (!doForw) {
			double tempd1 = fromLonSD;
			fromLonSD = toLonSD;
			toLonSD = tempd1;
			
			tempd1 = fromLatSD;
			fromLatSD = toLatSD;
			toLatSD = tempd1;
			
			tempd1 = fromBufSD;
			fromBufSD = toBufSD;
			toBufSD = tempd1;
		}
		
		ChallanInfo challanInfo = null;
		if (this.currSrcDestChallanDate > 0) {
			challanInfo = getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
		}
		IdInfo srcIdInfo = challanInfo == null ? null : challanInfo.getSrcIdInfoWithCalc(conn, false, stopDirControl);
		IdInfo destIdInfo = challanInfo == null ? null : challanInfo.getIdInfoWithCalc(conn, false, stopDirControl);
		
		int fromStationChallan = challanInfo != null ? challanInfo.getFromStationId() : Misc.getUndefInt();
		int toStationChallan = challanInfo != null ? challanInfo.getToStationId() : Misc.getUndefInt();
		double fromLonChallan = srcIdInfo != null ? srcIdInfo.getLongitude() : Misc.getUndefDouble();
		double fromLatChallan = srcIdInfo != null ? srcIdInfo.getLatitude() : Misc.getUndefDouble();
		double toLonChallan = destIdInfo != null ? destIdInfo.getLongitude() : Misc.getUndefDouble();
		double toLatChallan = destIdInfo != null ? destIdInfo.getLatitude() : Misc.getUndefDouble();
		
		double useSrcLon = fromLonSD;
		double useSrcLat = fromLatSD;
		double useDestLon = toLonSD;
		double useDestLat = toLatSD;
		int fromPointType = 0; //0 => from SrcDest, 1=>set up, 2=> from challan 
		//... fromPointType == 2 => there might be already travelled dist and already travelled time
		int toPointType = 0;
		
		if (!Misc.isUndef(fromLonChallan)) {
			useSrcLon = fromLonChallan;
			useSrcLat = fromLatChallan;
			fromPointType = 2;
		}
		else if (!Misc.isUndef(fromLonSetup)) {
			useSrcLon = fromLonSetup;
			useSrcLat = fromLatSetup;
			fromPointType = 1;
		}
		
		if (!Misc.isUndef(toLonChallan)) {
			useDestLon = toLonChallan;
			useDestLat = toLatChallan;
			toPointType = 2;
		}
		else if (!Misc.isUndef(toLonSetup)) {
			useDestLon = toLonSetup;
			useDestLat = toLatSetup;
			toPointType = 1;
		}
		etaItem.setSrcLon(useSrcLon);
		etaItem.setSrcLat(useSrcLat);
		etaItem.setDestLon(useDestLon);
		etaItem.setDestLat(useDestLat);
		double invoiceDist = challanInfo == null ? Misc.getUndefDouble() : challanInfo.getInvoiceDistKM(conn, stopDirControl);
		
		
		if (invoiceDist < 0.005)
			invoiceDist = Misc.getUndefDouble();
		
		Triple<Double, Double, Double> distMultKmPerDayMaxDurPerDelivery = ChallanUpdHelper.getDistFactorKmPerDayDeliveryTime(conn, vehSetup);
		
		double distMultFactor = distMultKmPerDayMaxDurPerDelivery == null ? 1.3 : distMultKmPerDayMaxDurPerDelivery.first;
		double adjustStart = adjustForOffPoint(useSrcLon, useSrcLat, fromLonSD, fromLatSD, toLonSD, toLatSD, distMultFactor);
		double adjustEnd = adjustForOffPoint(useDestLon, useDestLat, toLonSD, toLatSD, fromLonSD, fromLatSD, distMultFactor);
		double invoiceDistSD = srcDestInfo.getTransitDist();
		if (Misc.isUndef(fromLonSD) || Misc.isUndef(toLonSD))
			invoiceDistSD = Misc.getUndefDouble();
		if (invoiceDistSD < 0.005)
			invoiceDistSD = Misc.getUndefDouble();
		
		if (Misc.isUndef(invoiceDist)) {
			if (distMultFactor < 0.005)
				distMultFactor = 1.3;
			double distPerEndPt = invoiceDistSD;
			if (!Misc.isUndef(useSrcLon) && !Misc.isUndef(useDestLon)) {
				distPerEndPt = Point.fastGeoDistance(useSrcLon, useSrcLat, useDestLon, useDestLat);
				distPerEndPt *= distMultFactor;
			}
			if (Misc.isUndef(invoiceDistSD) || (distPerEndPt < 0.6*invoiceDistSD) || (distPerEndPt >1.4*invoiceDistSD)) {
				invoiceDist = distPerEndPt;
			}
			else {//use invoiceDistSD as basis but adjust for distance of useSrc to SD src and useDest to SD dest
				invoiceDist = invoiceDistSD;
				if (adjustStart < 0 && -1*adjustStart > invoiceDist)
					adjustStart = 0;
				if (adjustEnd < 0 && -1*adjustEnd > invoiceDist)
					adjustEnd = 0;
				invoiceDist += adjustStart;
				invoiceDist += adjustEnd;
				if (!Misc.isUndef(distPerEndPt) && (distPerEndPt < 0.6*invoiceDist || distPerEndPt > 1.4*invoiceDist)) {
					invoiceDist = distPerEndPt;
				}
			}
		}
		if (invoiceDist < 0.005)
			invoiceDist = Misc.getUndefDouble();
		
		//Now we need to calculate transitTime .... but first we check if SD is usable for intermediate points
		boolean isSDUsable = false;
		if (!Misc.isUndef(fromLonSD) && !Misc.isUndef(toLonSD)) {
			//will be usable if absolute of adjustStart is not too off, adjustEnd is not too off
			if (!Misc.isUndef(invoiceDistSD) && !Misc.isUndef(invoiceDist)) {
				if (invoiceDist >= 0.6*invoiceDistSD && invoiceDist <= 1.4*invoiceDistSD)
					isSDUsable = true;
			}
		}
		//Now we have invoiceDist and need to calculate exit time from src and transitTime
		double alreadyTavelledDist = 0;
		if (fromPointType == 2 && !Misc.isUndef(invoiceDist)) {//got from from Challan ... so adjust for already travelled
			alreadyTavelledDist = adjustForOffPoint(fromLonSetup, fromLatSetup, useSrcLon, useSrcLat, useDestLon, useDestLat, distMultFactor);
			if (alreadyTavelledDist < 0 && -1*alreadyTavelledDist > invoiceDist)
				alreadyTavelledDist = 0;
			invoiceDist += alreadyTavelledDist;
			adjustStart += alreadyTavelledDist;
		}
		double userInvoiceDist = challanInfo == null ? Misc.getUndefDouble() : challanInfo.getInvoiceDistKM(conn, stopDirControl);
		if (userInvoiceDist < 0.0001 && isSDUsable)
			userInvoiceDist = srcDestInfo.getTransitDist();
		double userTransitDay =challanInfo ==null ? Misc.getUndefDouble() : challanInfo.getAuthTimeMin(conn, stopDirControl)/(24*60);
		double transitSD = srcDestInfo.getTransitTime();
		if (userTransitDay < 0.0001 && isSDUsable)
			userTransitDay = transitSD;
		double transitDays = Misc.getUndefDouble();
		if (userTransitDay < 0.0001)
			userTransitDay = Misc.getUndefDouble();
		if (!Misc.isUndef(userTransitDay) && !Misc.isUndef(userInvoiceDist)) {
			transitDays = userTransitDay * invoiceDist/userInvoiceDist;
			double kmPerDay = distMultKmPerDayMaxDurPerDelivery == null ? Misc.getUndefDouble() : distMultKmPerDayMaxDurPerDelivery.second;
			kmPerDay = getKMPerDay(conn, vehSetup, userInvoiceDist, kmPerDay);
			double userTransitPerUs = userInvoiceDist/kmPerDay;
			etaItem.ourOverUserTransitEstMult = userTransitPerUs/transitDays;
		}
		else if (!Misc.isUndef(invoiceDist)) {
			double kmPerDay = distMultKmPerDayMaxDurPerDelivery == null ? Misc.getUndefDouble() : distMultKmPerDayMaxDurPerDelivery.second;
			kmPerDay = getKMPerDay(conn, vehSetup, invoiceDist, kmPerDay);
			transitDays = invoiceDist/kmPerDay; 
		}
		etaItem.setTransitDist(invoiceDist);
		etaItem.setTransitTime(transitDays);
		etaItem.setSdUsable(isSDUsable);
		ArrayList<SrcDestInfo.WayPoint> waypointList = srcDestInfo.getWaypoints();
		if (waypointList != null && waypointList.size() > 0) {
			ArrayList<NewETAforSrcDestItem.WayPointETA> intermediateList = etaItem.getIntermediateTransit();
			if (intermediateList == null) {
				intermediateList = new ArrayList<NewETAforSrcDestItem.WayPointETA>();
				etaItem.setIntermediateTransit(intermediateList);
			}
			for (int i=0,is=waypointList.size(); i<is; i++) {
				intermediateList.add(new NewETAforSrcDestItem.WayPointETA (Misc.getUndefDouble(), waypointList.get(i).getLongitude(), waypointList.get(i).getLatitude()));
			}
			if (!Misc.isUndef(invoiceDistSD) && isSDUsable) { 
				double adjustStartDays = adjustStart/invoiceDistSD*transitSD;
				if (doForw) {
					
					for (int i=0,is=waypointList.size(); i<is; i++) {
						if (Misc.isUndef(waypointList.get(i).getTransit()))
							continue;	
						double adjTransit = waypointList.get(i).getTransit()+adjustStartDays;
						if (adjTransit < 0.005)
							adjTransit = 0;
						intermediateList.get(i).setTransitTimeFromSrc(adjTransit);
					}	
				}
				else {
					for (int i=0,is=waypointList.size(); i<is; i++) {
						double adjTransit = transitSD - waypointList.get(i).getTransit()+adjustStartDays;
						if (adjTransit < 0.005)
							adjTransit = 0;
						intermediateList.get(i).setTransitTimeFromSrc(adjTransit);
					}
				}
			}
		}
	}

	public static double getKMPerDay(Connection conn, CacheTrack.VehicleSetup vehSetup, double invoiceDist, double defaultKMPerDay) {
		if (Misc.isUndef(defaultKMPerDay))
			defaultKMPerDay = 250; 
		try {
			ArrayList<OrgBasedLovBean> beanList = OrgBasedLovBean.getLovListFor(conn, vehSetup == null ? Misc.G_TOP_LEVEL_PORT : vehSetup.m_ownerOrgId, 407, false);
			double glbValDist = Misc.getUndefDouble();
			double glbValSpeed = Misc.getUndefDouble();
			for (int i=0,is=beanList == null ? 0 : beanList.size(); i<is; i++) {
				OrgBasedLovBean bean = beanList.get(i);
				double speed = Misc.getParamAsDouble(bean.getClassificationStr(1));
				double dist = Misc.getParamAsDouble(bean.getClassificationStr(0));
				if (dist < invoiceDist || Misc.isEqual(dist, invoiceDist)) {
					if (Misc.isUndef(glbValDist) || glbValDist < dist) {
						glbValSpeed = speed;
						glbValDist = dist;
					}
				}
			}
			return Misc.isUndef(glbValSpeed) ? defaultKMPerDay : glbValSpeed * 24;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultKMPerDay; 
	}
	public static double adjustForOffPoint(double actPtLon, double actPointLat, double fromLon, double fromLat, double toLon, double toLat, double distMultFactor) {
		//check if actPt in between from and to. If yes then adjustDist 
		//   assume we get to rount from fromLon, toLon by nearest path ... so adjust dist = from pt to intersect pt
		//But if not in between then we assume path from pt to by extending
		//return negative if distance needs to be shortened or positive if distance needs to be extended
		
		double adjustDist = 0;
		if (!Misc.isUndef(actPtLon) && !Misc.isUndef(fromLon)) {
			double d = Point.fastGeoDistance(actPtLon, actPointLat, fromLon, fromLat);
			if (!Misc.isUndef(toLon)) {
				Pair<Double, Double> intersectPt = RouteDef.checkWhereInSegment(actPtLon, actPointLat, fromLon, fromLat, toLon, toLat);
				double alpha = intersectPt.first;
				double dist = intersectPt.second;
				
				if (alpha >= 0 && alpha <= 1) {
					double distToIntersectPtSqr = d*d - dist*dist;
					double distToIntersectPt = distToIntersectPtSqr > 0 ? Math.sqrt(distToIntersectPtSqr) : 0;
					adjustDist = -1*distToIntersectPt;
				}
				else {
					adjustDist = d;
				}
			}
			else {
				adjustDist = d;
			}
			adjustDist *= distMultFactor;
		}
		return adjustDist;
	}
	
	public static ChallanInfo getChallanInfo(Connection conn, int vehicleId, long challanDate) {
		if (challanDate <= 0)
			return null;
		NewChallanMgmt extList = NewChallanMgmt.getExtChallanList(conn, vehicleId, true);
		if (extList != null) {
			return extList.get(conn, new ChallanInfo(challanDate));
		}
		return null;
	}
	
	
	public boolean processSingleDataPoint(Connection conn, GpsData data)  {//returns true if succ completed
		boolean retval = true;
		StringBuilder traceStr = toTrace(vehicleId) ? new StringBuilder() : null;
		try {
			Point pt = data.getPoint();
			
			Triple<ArrayList<Pair<SrcDestInfo, Integer>>, ArrayList<SrcDestInfo>, ArrayList<SrcDestInfo>> comboListPlusAreaOfOpsNearSrc = SrcDestHelper.getComboInInfo(conn,pt, this.currMasterSrcDestList);
			ArrayList<Pair<SrcDestInfo, Integer>> comboList = comboListPlusAreaOfOpsNearSrc.first;
			ArrayList<SrcDestInfo> areaOfOps = comboListPlusAreaOfOpsNearSrc.second;
			//if non-null comboList  ... then possible taken from these ... else will be filtered by areaOfOps (provided Src had at least one areaOps)
			if (traceStr != null) {
				traceStr.append("[ETA:").append(vehicleId).append(" ComboIn res:");
				for (int i=0,is=comboList == null ? 0 : comboList.size(); i<is; i++) {
					traceStr.append("(").append(comboList.get(i).first.getId()).append(",").append(comboList.get(i).second).append(")");
				}
				traceStr.append(" AreaOfOpsResult:");
				for (int i=0,is=areaOfOps == null ? 0 : areaOfOps.size(); i<is; i++) {
					if (i > 0)
						traceStr.append(",");
					traceStr.append(areaOfOps.get(i).getId());
				}
				System.out.println(traceStr);
				traceStr.setLength(0);
			}
			for (int i=0,is=etaInfo == null ? 0 : etaInfo.size(); i<is; i++) {
				NewETAforSrcDestItem curr = etaInfo.get(i).first;
				int resultIdx = getIndexInIntermediateList(comboList, curr.getSrcDestId());
				Pair<SrcDestInfo, Integer> entry = resultIdx < 0 ? null : comboList.get(resultIdx);
				dirty = curr.closeOutOthers(data.getGps_Record_Time(), entry == null ? Misc.getUndefInt() : entry.second) || dirty;
				if (entry != null)
					dirty = curr.addETAEvent(entry.second, data.getGps_Record_Time()) || dirty;
			}
			
			//now update possible SrcDestList ... basically .... only those found now will be considered as valid
			int oldBestSrcDestId = this.currPossibleSrcDestList != null && this.currPossibleSrcDestList.size() > 0 ? this.currPossibleSrcDestList.get(0).first : Misc.getUndefInt();
			for (int i=comboList == null ? -1 : comboList.size()-1; i>=0; i--) {
				int srcDestId = comboList.get(i).first.getId();
				int idxInETA = NewVehicleETA.getSrcDestIndexInETAInfo(etaInfo, srcDestId);
				NewETAforSrcDestItem etaForSrc = idxInETA < 0 ? null : etaInfo.get(idxInETA).first;
				boolean dirPerSD = idxInETA < 0 ? null : etaInfo.get(idxInETA).second;
				if (etaForSrc == null) //shouldnt be the case
					continue; 
				int dir = etaForSrc.getDirection();
				boolean valid = true;
				if ((dir == 1 && !dirPerSD) || (dir == -1 && dirPerSD)) {
					valid = false;
				}
				if (!valid) {
					//remove from comboList and remove from possible ..
					comboList.remove(i);
					int tempIdx = NewVehicleETA.getSrcDestIndexInIntegerList(this.currPossibleSrcDestList, srcDestId);
					if (tempIdx >= 0) {
						this.currPossibleSrcDestList.remove(tempIdx);
						dirty = true;
					}
				}
			}
			
			//now only those in validList will appear here ..
			int currPossibleFirst = this.currPossibleSrcDestList != null && this.currPossibleSrcDestList.size() > 0 ? this.currPossibleSrcDestList.get(0).first : Misc.getUndefInt();
			if (comboList != null && comboList.size() > 0) {
				if (this.currPossibleSrcDestList == null)
					this.currPossibleSrcDestList = new ArrayList<MiscInner.PairIntBool>();
				else
					this.currPossibleSrcDestList.clear();
				for (int i=0,is=this.currMasterSrcDestList == null ? 0 : this.currMasterSrcDestList.size(); i<is; i++) {
					int srcDestId = currMasterSrcDestList.get(i).first;
					int idxInCombo = NewVehicleETA.getIndexInIntermediateList(comboList, srcDestId);
					if (idxInCombo >= 0) {
						this.currPossibleSrcDestList.add(currMasterSrcDestList.get(i));
					}
				}
			}
			else {//use areaOfOps as an additional filter 
				for (int i=this.currPossibleSrcDestList == null ? -1 : this.currPossibleSrcDestList.size()-1;i>=0;i--) {
					int srcDestId = this.currPossibleSrcDestList.get(i).first;
					boolean toremove = false;
					if (NewVehicleETA.getSrcDestIndexInSimpleList(areaOfOps, srcDestId) < 0) {
						SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, srcDestId);
						if (srcDestInfo == null || (srcDestInfo.getAreaOfOpRegions() != null && srcDestInfo.getAreaOfOpRegions().size() > 0))
							toremove = true;
					}
					if (toremove) {
						currPossibleSrcDestList.remove(i);
						dirty = true;
					}
				}
			}
			if ((currPossibleSrcDestList == null || currPossibleSrcDestList.size() == 0) && (areaOfOps != null && areaOfOps.size() > 0)) {
				//get those coming from areaofOps pt of view
				if (this.currPossibleSrcDestList == null)
					this.currPossibleSrcDestList = new ArrayList<MiscInner.PairIntBool>();
				else
					this.currPossibleSrcDestList.clear();
				for (int i=0,is=this.currMasterSrcDestList == null ? 0 : this.currMasterSrcDestList.size(); i<is; i++) {
					int srcDestId = currMasterSrcDestList.get(i).first;
					int idxInCombo = NewVehicleETA.getSrcDestIndexInSimpleList(areaOfOps, srcDestId);
					if (idxInCombo >= 0) {
						this.currPossibleSrcDestList.add(currMasterSrcDestList.get(i));
					}
				}
			}
			int newPossibleFirst = this.currPossibleSrcDestList != null && this.currPossibleSrcDestList.size() > 0 ? this.currPossibleSrcDestList.get(0).first : Misc.getUndefInt();
			if (currPossibleFirst != newPossibleFirst) {
				int oldIdx = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, currPossibleFirst);
				int newIdx = NewVehicleETA.getSrcDestIndexInETAInfo(this.etaInfo, newPossibleFirst);
				Pair<NewETAforSrcDestItem, Boolean> oldItem = oldIdx < 0 ? null : etaInfo.get(oldIdx);
				Pair<NewETAforSrcDestItem, Boolean> newItem = newIdx < 0 ? null : etaInfo.get(newIdx);
				if (newItem != null && oldItem != null) {
					newItem.first.migrateAlertEtcListToNew(conn, oldItem.first, oldItem.second, newItem.second);
				}
				dirty = true;
				this.cachedFromName = null;
				this.cachedToName = null;
			}
			if (traceStr != null) {
				traceStr.append("[ETA:").append(vehicleId).append(" After Proces Old:").append(currPossibleFirst).append(" New:").append(newPossibleFirst);
				System.out.println(traceStr);
				traceStr.setLength(0);
			}
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	private void addETAEntry(SrcDestInfo srcDestInfo, boolean dir) {
		int srcDestId = srcDestInfo.getId();
		boolean found = NewVehicleETA.getSrcDestIndexInETAInfo(etaInfo, srcDestId) >= 0;
		if (!found) {
			NewETAforSrcDestItem item = new NewETAforSrcDestItem(srcDestId);
			if (etaInfo == null)
				etaInfo = new ArrayList<Pair<NewETAforSrcDestItem, Boolean>>();
			etaInfo.add(new Pair<NewETAforSrcDestItem, Boolean>(item, dir));
			dirty = true;
		}
	}
	 
	private void adjustEtaInfoForNewGet(ArrayList<Pair<SrcDestInfo, Boolean>> srcDestList) {
		for (int i=etaInfo == null ? -1 : etaInfo.size()-1; i>=0; i--) {
			int srcDestId = etaInfo.get(i).first.getSrcDestId();
			boolean found = NewVehicleETA.getSrcDestIndexInList(srcDestList, srcDestId) >= 0;
			if (!found) {
				etaInfo.remove(i);
				dirty = true;
			}
		}
		//now make sure that we have etaInfo entry for each of the srcDest
		for (int j=0,js=srcDestList == null ? 0 : srcDestList.size(); j<js; j++) {
			int srcDestId = srcDestList.get(j) == null || srcDestList.get(j).first == null ? Misc.getUndefInt() : srcDestList.get(j).first.getId();
			if (Misc.isUndef(srcDestId)) {//shouldnt happen
				System.out.println("[ETA ERROR in adjustETA");
				continue;
			}
			boolean found = NewVehicleETA.getSrcDestIndexInETAInfo(etaInfo, srcDestId) >= 0;
			if (!found) {
				NewETAforSrcDestItem item = new NewETAforSrcDestItem(srcDestId);
				if (etaInfo == null)
					etaInfo = new ArrayList<Pair<NewETAforSrcDestItem, Boolean>>();
				etaInfo.add(new Pair<NewETAforSrcDestItem, Boolean>(item, srcDestList.get(j).second));
				dirty = true;
			}
		}
	}
	private boolean tripControlParamsChanged(int fromStationId, int toStationId, long fromIn, long fromOut, ChallanInfo challanInfo, long toIn, long toOut) {
		return  currFromOpStationId != fromStationId 
	    || currToOpStationId != toStationId
	    || !((fromIn <= 0 && currFromOpStationInTime <= 0) || (fromIn == currFromOpStationInTime))
	    ||!((fromOut <= 0 && currFroOpStationOutTime <= 0) || (fromOut == currFroOpStationOutTime))
	    ||!((challanInfo == null && currSrcDestChallanDate <= 0) || (challanInfo != null && challanInfo.getChallanDate() == currSrcDestChallanDate))
	    ||!((challanInfo == null && currSrcDestChallanUpdDate <= 0) || (challanInfo != null && challanInfo.getChallanRecvTime() == currSrcDestChallanUpdDate))
	    || !((toIn <= 0 && currToInTime <= 0) || (fromIn == currToInTime))
	    ||!((toOut <= 0 && currToOutTime <= 0) || (fromOut == currToOutTime))
    ;
	}
	private void setInitETAGetParams(int fromStationId, int toStationId, long fromIn, long fromOut, ChallanInfo challanInfo, long toIn, long toOut, long nowTime) {
		//if (fromIn != this.currFromOpStationInTime)
		//	this.lastProcessedGRT = fromIn;

		currFromOpStationId = fromStationId;
		currToOpStationId = toStationId;
		currFromOpStationInTime = fromIn;
		currFroOpStationOutTime = fromOut;
		currToInTime = toIn;
		currToOutTime = toOut;
		currChallanAssignTime = challanInfo == null ? Misc.getUndefInt() : challanInfo.getChallanDate() == this.currSrcDestChallanDate ? this.currChallanAssignTime : nowTime;
		currSrcDestChallanDate = challanInfo == null ? Misc.getUndefInt() : challanInfo.getChallanDate();
		currSrcDestChallanUpdDate = challanInfo == null ? Misc.getUndefInt() : challanInfo.getChallanRecvTime();
		cachedFromName = null;
		cachedToName = null;
		dirty = true;
		lastProcessedEstCal = Misc.getUndefInt();
	}
	public void clear() {
		this.dirty = true;
		this.currMasterSrcDestList = null;
		this.etaInfo = null;
		currFromOpStationId = Misc.getUndefInt();
		currToOpStationId = Misc.getUndefInt();
		currFromOpStationInTime = Misc.getUndefInt();
		currSrcDestChallanDate = Misc.getUndefInt();
		currSrcDestChallanUpdDate = Misc.getUndefInt();
		currChallanAssignTime = Misc.getUndefInt();
		this.lastProcessedGRT = Misc.getUndefInt();
		this.cachedFromName = null;
		this.cachedToName = null;
		this.lastProcessedGRT = Misc.getUndefInt(); //if an when we process we process from loadin time
	}
	 
	private boolean toRecalcPossibleETA(int fromOpStationId, int toStationId, long fromInTime, ChallanInfo challanInfo) {
				return
				fromOpStationId != currFromOpStationId
				|| toStationId != currToOpStationId
				|| currFromOpStationInTime != fromInTime
				|| currSrcDestChallanDate != (challanInfo == null ? Misc.getUndefInt() : challanInfo.getChallanDate())
				|| currSrcDestChallanUpdDate != (challanInfo == null ? Misc.getUndefInt() : challanInfo.getChallanRecvTime())
				;
	}
	
	public static ArrayList<Pair<SrcDestInfo, Boolean>> getPossibleSrcDest(Connection conn, int vehicleId, CacheTrack.VehicleSetup vehSetup, int fromOpStationId, int toOpStationId, ChallanInfo challanInfo, StopDirControl stopDirControl, boolean doBackw, long fromRefTime, long toRefTime, NewVehicleData vdp) {
		try {
			
			//get from src & remvoe not applicable
			//get from dest & remove not applicable
			//merge (remove duplic) & then score and then resort according to priority
			//Scoring:
			Cache cache = Cache.getCacheInstance(conn);
			Triple<Integer, Double, Double> srcLongLat = helperGetSrcOrDestLongLat(conn, true, fromOpStationId, challanInfo, stopDirControl, fromRefTime, vdp);
			Point srcPoint = new Point(srcLongLat.second, srcLongLat.third);
			SrcDestHelper.InInfoResult holder = null;
			 holder = SrcDestHelper.getInInfo(conn, srcPoint, cache, vehicleId, true, true, false, false);
			 
			ArrayList<Pair<SrcDestInfo, Boolean>> srcList = null;
			srcList = mergePlainList(srcList, holder.srcList, true);
			if (doBackw)
				srcList = mergePlainList(srcList, holder.destList, false);
			
		    
			Triple<Integer, Double, Double> destLongLat = helperGetSrcOrDestLongLat(conn, false, toOpStationId, challanInfo, stopDirControl, toRefTime, vdp);
			Point destPoint = new Point(destLongLat.second, destLongLat.third);
			holder = SrcDestHelper.getInInfo(conn, destPoint, cache, vehicleId, true, true, false, false);
			ArrayList<Pair<SrcDestInfo, Boolean>> destList = null;
			destList = mergePlainList(destList, holder.destList, true);
			if (doBackw)
				destList = mergePlainList(destList, holder.srcList, false);
				
			//Step 1: Now if there are common elements then we work only with these, else if srcList is nonEmpty then srcList else destList
			ArrayList<Pair<SrcDestInfo,Boolean>> workingList = srcList;
			boolean foundNonNullIntersection = false;
			if (srcList != null && srcList.size() != 0 && destList != null && destList.size() != 0) {
				for (int i=srcList.size()-1; i>=0;i--) {
					SrcDestInfo src = srcList.get(i).first;
					boolean foundMatch = NewVehicleETA.getSrcDestIndexInList(destList, src.getId()) >= 0;
					if (foundMatch) {
						if (!foundNonNullIntersection) { //all till now were non matching and can be removed
							for (int k=srcList.size()-1; k>i; k--)
								srcList.remove(k);
						}
						foundNonNullIntersection = true;
					}
					else {
						if (foundNonNullIntersection) {
							//we can remove .. this item
							srcList.remove(i);
						}
					}
				}
			}
			if (srcList == null || srcList.size() == 0)
				workingList = destList;
			
			//Step 2: rearrange list so that the closes match is at beginning
			ArrayList<MiscInner.PairDouble> helperInfo = new ArrayList<MiscInner.PairDouble>();
			for (int i=0,is=workingList == null ? 0 : workingList.size(); i<is; i++) {
				Pair<SrcDestInfo, Boolean> itemBeingConsideredPr = workingList.get(i);
				SrcDestInfo itemBeingConsidered = itemBeingConsideredPr.first;
				boolean doingForw = itemBeingConsideredPr.second;
				 MiscInner.PairDouble itemScore = assessQualityOfSrcDestInfo(conn, itemBeingConsidered, srcLongLat.first, destLongLat.first, doingForw);
				 //Quality assessment: first - match rel to (true) src, second match rel to (true) dest (i.e if !doingForw ... will first will refer to dest part, second will refer to src part
				 // if stationId to stationid match then -1, else radius of the region of Src/Dest
				 helperInfo.add(itemScore);
				int moveTo = -1;
				double zero = -0.005;
				int bestBeforeIndex = -1;
				int moveAt = -1;
				double bestBeforeIfNotClearCut = Misc.LARGE_NUMBER;
				for (int j=0; j<i; j++) {
					MiscInner.PairDouble compScore = helperInfo.get(j);
					int comp = 0;
					if (comp == 0)
						comp = compScore.first < zero && itemScore.first >=zero ? -1 : itemScore.first < zero && compScore.first >= zero ? 1 : 0;
					if (comp == 0) {
						int itemPriority = itemBeingConsidered.getPriority();
						int cmpPriority = workingList.get(j).first.getPriority();
						comp = -1*(cmpPriority-itemPriority);
					}
					if (comp == 0) {
						 comp = doingForw == workingList.get(j).second ? 0 : doingForw ? 1 : -1;
					}
					
					if (comp == 0) {
						//no clear cut ... goto second
						boolean firstEqual = Misc.isEqual(itemScore.first, compScore.first, 0.1,0.05);
						boolean secondEqual = Misc.isEqual(itemScore.second, compScore.second, 0.1,0.05);
						if (firstEqual && secondEqual) { //src/dest match 
							//leave as it is
							comp = -1;
						}
						else if (firstEqual) {//src match .. second does not the tighter of it
							double dbl = compScore.second - itemScore.second;
							comp = dbl < 0.005 ? -1 : 1;
						}
						else if (secondEqual) {//
							double dbl = compScore.first - itemScore.first;
							comp = dbl < 0.005 ? -1 : 1;
						}
						else {
							double dbl = compScore.first - itemScore.first + compScore.second - itemScore.second;
							comp = dbl < 0.005 ? -1 : 1;
						}
					}
					if (comp <= 0) {
						//dont move
					}
					else if (comp > 0) {
						moveTo = j;
					}
					if (moveTo >= 0)
						break;
				}
				if (moveTo >= 0) {
					for (int k=i;k>moveTo;k--) {
						workingList.set(k, workingList.get(k-1));
						helperInfo.set(k, helperInfo.get(k-1));
					}
					workingList.set(moveTo, itemBeingConsideredPr);
					helperInfo.set(moveTo, itemScore);
				}
			}
			//rearranged list
			if (workingList == null || workingList.size() == 0) {
				int defaultSrc = SrcDestInfo.getDefaultSrcDest(conn, vehSetup.m_ownerOrgId);
				SrcDestInfo defaultSrcInfo = SrcDestInfo.getSrcDestInfo(conn, defaultSrc);
				if (defaultSrcInfo != null) {
					if (workingList == null)
						workingList = new ArrayList<Pair<SrcDestInfo, Boolean>>();
					workingList.add(new Pair<SrcDestInfo, Boolean>(defaultSrcInfo, true));
				}
			}
			//debug ... because of error imm after
			for (int i=workingList == null ? -1 :workingList.size()-1; i>=0; i--) {
				if (workingList.get(i) == null || workingList.get(i).first == null) {
					System.out.println("NEWVEHICLETA error: i emptry:"+i+" wkglist.get(i)"+workingList.get(i));
					workingList.remove(i);
				}
			}
			return workingList;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return null;
	}

	
	private static ArrayList<Pair<SrcDestInfo, Boolean>> mergePlainList(ArrayList<Pair<SrcDestInfo, Boolean>> retval, ArrayList<SrcDestInfo> newList, boolean asForw) {
		if (newList != null && newList.size() > 0) {
			if (retval == null) 
				retval = new ArrayList<Pair<SrcDestInfo, Boolean>>();
			for (SrcDestInfo item:newList) {
				if (getSrcDestIndexInList(retval, item.getId()) < 0)
					retval.add(new Pair<SrcDestInfo, Boolean>(item, asForw));
			}
		}
		return retval;
	}
	
	private static int getIndexInIntermediateList(ArrayList<Pair<SrcDestInfo, Integer>> theList, int srcDestId) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (theList.get(i).first.getId() == srcDestId)
				return i;
		return -1;
	}
	
	private static int getSrcDestIndexInETAInfo(ArrayList<Pair<NewETAforSrcDestItem, Boolean>> theList, int srcDestId) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (theList.get(i).first.getSrcDestId() == srcDestId)
				return i;
		return -1;
	}
	
	static int getSrcDestIndexInIntegerList(ArrayList<MiscInner.PairIntBool> theList, int srcDestId) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (theList.get(i).first == srcDestId)
				return i;
		return -1;
	}
	
	private static int getSrcDestIndexInList(ArrayList<Pair<SrcDestInfo, Boolean>> theList, int srcDestId) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (theList.get(i).first.getId() == srcDestId)
				return i;
		return -1;
	}
	
	private static int getSrcDestIndexInSimpleList(ArrayList<SrcDestInfo> theList, int srcDestId) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++)
			if (theList.get(i).getId() == srcDestId)
				return i;
		return -1;
	}
	private static ArrayList<MiscInner.PairIntBool> helperConvertSrcDestInfoListToId(ArrayList<Pair<SrcDestInfo, Boolean>> inp) {
		if (inp == null || inp.size() == 0)
			return null;
		ArrayList<MiscInner.PairIntBool> retval = new ArrayList<MiscInner.PairIntBool>();
		for (Pair<SrcDestInfo, Boolean> srcDestInfoPr : inp) {
			int srcDestId = srcDestInfoPr == null || srcDestInfoPr.first == null ? Misc.getUndefInt() : srcDestInfoPr.first.getId();
			if (Misc.isUndef(srcDestId)) {//shouldnt happen
				System.out.println("[ETA ERROR in convert ot int");
				continue;
			}
			retval.add(new MiscInner.PairIntBool(srcDestInfoPr.first.getId(), srcDestInfoPr.second));
		}
		return retval;
	}
	private static MiscInner.PairDouble assessQualityOfSrcDestInfo(Connection conn, SrcDestInfo srcDestInfo, int fromOpStationId, int toOpStationId, boolean doForw) {
		//first = srcQuality, second = destQuality
		//if opStationMatch ... then -1
		double fromRadius = srcDestInfo.getSrcRadius(conn);
		double toRadius = srcDestInfo.getDestRadius(conn);
		try {
			int srcDestFromOpId = srcDestInfo.getSrcType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
			int srcDestToOpId = srcDestInfo.getDestType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
			if (!doForw) {
				int temp = srcDestFromOpId;
				srcDestFromOpId = srcDestToOpId;
				srcDestToOpId = temp;
				double tempd = fromRadius;
				fromRadius = toRadius;
				toRadius = tempd;
			}
			if (!Misc.isUndef(fromOpStationId)) 
				fromRadius =  fromOpStationId == srcDestFromOpId ? -1 : fromRadius;
			if (!Misc.isUndef(toOpStationId)) 
				toRadius = toOpStationId == srcDestToOpId ? -1 : toRadius;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return new MiscInner.PairDouble(fromRadius, toRadius);
	}
	
	public static Triple<Integer, Double, Double> helperGetSrcOrDestLongLat(Connection conn, boolean doSrc, int opStationId, ChallanInfo challanInfo, StopDirControl stopDirControl, long refTime, NewVehicleData vdp) {
		double lon = Misc.getUndefDouble();
		double lat = Misc.getUndefDouble();
		try {
			if (challanInfo != null) {
				if (doSrc && !Misc.isUndef(challanInfo.getFromStationId()))
					opStationId = challanInfo.getFromStationId();
				else if (!doSrc && !Misc.isUndef(challanInfo.getToStationId()))
					opStationId = challanInfo.getToStationId();
				else {
					IdInfo src = doSrc ? challanInfo.getSrcIdInfoWithCalc(conn, false, stopDirControl) : challanInfo.getIdInfoWithCalc(conn, false, stopDirControl);
					if (src != null) {
						if (src.getDestIdType() == 3) {
							opStationId = src.getDestId();
						}
						else {
							lon = src.getLongitude();
							lat = src.getLatitude();
						}
					}
				}
			}
			if (!Misc.isUndef(opStationId)) {
				OpStationBean opbean = TripInfoCacheHelper.getOpStation(opStationId);
				if (opbean != null) {
				    RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(opbean.getGateAreaId(), conn);
				    if (rth != null) {
				    	lon = rth.region.getCenter().getLongitude();
				    	lat = rth.region.getCenter().getLatitude();
				    }
				}
			}
			if (Misc.isUndef(lon) && refTime > 0) {
				GpsData dataAtRefTimePlus = vdp.getSinglePoint(conn, new GpsData(refTime+1));
				if (dataAtRefTimePlus != null) {
					lon = dataAtRefTimePlus.getLongitude();
					lat = dataAtRefTimePlus.getLatitude();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		
		return new Triple<Integer, Double, Double>(opStationId, lon, lat);
	}
	
	public static ArrayList<Integer> getVehicleList() {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		Collection<NewVehicleETA> vehicleList = NewVehicleETA.g_vehicleETACache.values();
		for (NewVehicleETA vehicleETA : vehicleList) {
			retval.add(vehicleETA.vehicleId);
		}
		return retval;
	}
	
	public static boolean handleSrcDestInfoChange(Connection conn, int vehicleId, int srcDestId, boolean toRemove)  {//returns true if succ else false
		System.out.println("[ETA doing changes to curr ETA vehicleId:"+vehicleId+", SD:"+srcDestId+", toremove:"+toRemove);
		boolean retval = true;
		NewVehicleETA vehicleETA = NewVehicleETA.getETAObj(conn, vehicleId);
		if (vehicleETA == null)
			return retval;
		try {
			StringBuilder traceStr = toTrace(vehicleETA.vehicleId) ? new StringBuilder() : null;
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleETA.vehicleId, conn);
			if (vehSetup == null)
				return retval;
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleETA.vehicleId, false, false);
			if (vdf == null)
				return retval;
			synchronized (vdf) {
				if (traceStr != null) {
					traceStr.append("[ETA_CH:").append(vehicleETA.vehicleId).append(" changing for SD:").append(srcDestId).append(" toremove:").append(toRemove);
					traceStr.append("\n").append(vehicleETA);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				ArrayList<MiscInner.PairIntBool> templ = vehicleETA.currMasterSrcDestList;
				if (!Misc.isUndef(srcDestId) && toRemove) {
					for (int i=templ == null ? -1 : templ.size()-1; i>=0; i--) {
						if (templ.get(i).first == srcDestId) {
							vehicleETA.dirty = true;
							templ.remove(i);
						}
					}
				}
				else {
					
					NewVehicleData vdp = vdf.getDataList(conn, vehicleETA.getVehicleId(), 0, true);
					vehicleETA.setupRecalc(conn, vehicleETA.vehicleId, vehSetup, vdp);
				}
				if (traceStr != null) {
					traceStr.append("[ETA_CH_END:").append(vehicleETA.vehicleId).append(" changing for SD:").append(srcDestId).append(" toremove:").append(toRemove);
					traceStr.append("\n").append(vehicleETA);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
			}//end of sync block
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		finally {
			
		}
		return retval;
	}//end of func
	
	public static boolean redoSingleVehicle(Connection conn, int vehicleId, boolean sendAlert, long nowTime)  {//returns true if succ else false
		System.out.println("[ETA doing redo single vehicle:");
		boolean retval = true;
		
		try {
			NewVehicleETA vehicleETA = NewVehicleETA.getETAObj(conn, vehicleId);
			if (vehicleETA == null)
				return retval;
			StringBuilder traceStr = toTrace(vehicleETA.vehicleId) ? new StringBuilder() : null;
			CacheTrack.VehicleSetup vehsetup = CacheTrack.VehicleSetup.getSetup(vehicleETA.getVehicleId(), conn);
			if (vehsetup == null)
				return retval;
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleETA.vehicleId, false, false);
			if (vdf == null)
				return retval;
			
			StopDirControl stopDirControl = vehsetup.getStopDirControl(conn);
			synchronized (vdf) {
				if (traceStr != null) {
					traceStr.append("[ETA_CH:").append(vehicleETA.vehicleId).append(" redoing vehicle:");
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
				vehicleETA.removeAlertsOngoing(conn, false);
				vehicleETA.clear();
				vehicleETA.setLastProcessedGRT(Misc.getUndefInt());
				LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vehicleId);
				NewVehicleData vdp = vdf.getDataList(conn, vehicleETA.getVehicleId(), 0, true);
				if (nowTime < 0)
					nowTime = vdp.getLatestReceivedTime();
				if (traceStr != null) {
					traceStr.append("[ETA_CH:").append(vehicleETA.vehicleId).append(" changing vehicle:");
					traceStr.append("\n").append(vehicleETA);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}

				if (latest != null) {
					LUInfoExtract lext = latest.getLoad();
					LUInfoExtract uext = latest.getUnload();
					ChallanInfo ch = latest.getChallanInfo();
					
					if (lext != null || uext != null || ch != null) {
						vehicleETA.doUpdateForTrip(conn, vehicleId, vehsetup, lext, uext, ch, stopDirControl, vdp, nowTime);
						vehicleETA.processAllData(conn, -1, vdp, stopDirControl, vdf, vehsetup, sendAlert, nowTime);
					}
				}
				
				if (traceStr != null) {
					traceStr.append("[ETA_CH_END:").append(vehicleETA.vehicleId).append(" redo vehicle:");
					traceStr.append("\n").append(vehicleETA);
					System.out.println(traceStr);
					traceStr.setLength(0);
				}
			}//end of sync block
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		finally {
			
		}
		return retval;
	}//end of func

	public boolean saveState(Connection conn, NewVehicleData vdp, boolean must, boolean treatAsDirty, long nowTime) {//return true if scc
		boolean retval = true;
		PreparedStatement ps = null;
		ObjectOutputStream oos = null;
		try {
			this.dataOpCount++;
			if (dataOpCount > 120 || must) {
				if (dirty || treatAsDirty) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					oos = new ObjectOutputStream(baos);
					oos.writeObject(this);
					oos.flush();
					oos = Misc.closeObjOut(oos);
					byte[] data = baos.toByteArray();
					ps = conn.prepareStatement("update eta_obj_state set latest_grt=?, saved_at_time=?, object = ? where vehicle_id = ?");
					ps.setTimestamp(1, Misc.longToSqlDate(this.lastProcessedGRT));
					ps.setTimestamp(2, Misc.longToSqlDate(nowTime));
					ps.setObject(3, data);
					ps.setInt(4, this.vehicleId);
					ps.executeUpdate();
					ps = Misc.closePS(ps);
					if (!conn.getAutoCommit())
						conn.commit();
					dirty = false;
					
				}
				else {
					ps = conn.prepareStatement("update eta_obj_state set latest_grt=?, saved_at_time=? where vehicle_id = ?");
					ps.setTimestamp(1, Misc.longToSqlDate(this.lastProcessedGRT));
					ps.setTimestamp(2, Misc.longToSqlDate(nowTime));
					ps.setInt(3, this.vehicleId);
					ps.executeUpdate();
					ps = Misc.closePS(ps);
					if (!conn.getAutoCommit())
						conn.commit();
				}
				
				dataOpCount = 0;
			}
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
		}
		finally {
			oos = Misc.closeObjOut(oos);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	private static boolean checkAndCreateState(Connection conn, int vehicleId)  {//returns true if succ
		boolean retval = true;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select 1 from eta_obj_state where vehicle_id=?");
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			boolean found = rs.next();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (!found) {
				ps = conn.prepareStatement("insert into eta_obj_state (vehicle_id) values (?)");
				ps.setInt(1, vehicleId);
				ps.executeUpdate();
				ps = Misc.closePS(ps);
			}
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	private static boolean loadState(Connection conn, ArrayList<Integer> vehicleIds)  {//returns true if succ
		PreparedStatement ps = null;
		ResultSet rs =null;
		boolean retval = true;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select eta_obj_state.vehicle_id, eta_obj_state.latest_grt, eta_obj_state.object from vehicle join eta_obj_state on (vehicle.id = eta_obj_state.vehicle_id and vehicle.status in (1)) ");
			if (vehicleIds != null && vehicleIds.size() > 0) {
				sb.append(" where vehicle.id in (");
				Misc.convertInListToStr(vehicleIds, sb);
				sb.append(")");
			}
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				long grt = rs.getInt(2);
				NewVehicleETA data = null;
				ObjectInputStream in = null;
				try {
					byte[] obj = (byte[]) rs.getObject("object");
					
					if (obj != null && obj.length != 0) {
						ByteArrayInputStream bais = new ByteArrayInputStream(obj);
						 in = new ObjectInputStream(bais);
						data = (NewVehicleETA) in.readObject();
						in = Misc.closeObjInp(in);
					} else {
						data = new NewVehicleETA(vehicleId);
					}
					if (grt > 0)
						data.setLastProcessedGRT(grt);
				}
				catch (Exception e) {
					retval = false;
					e.printStackTrace();
					//eat it
				}
				finally {
					in = Misc.closeObjInp(in);
				}
				if (data == null)
					data = new NewVehicleETA(vehicleId);
				NewVehicleETA.g_vehicleETACache.put(vehicleId, data);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	public String toString() {
		SimpleDateFormat indep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return toString(indep);	
	}
	public String toString(SimpleDateFormat indep) {
		StringBuilder sb = new StringBuilder();
		sb.append("ETA:").append(vehicleId)
		  .append(" FromStn:").append(this.currFromOpStationId)
		  .append(" In:").append(this.currFromOpStationInTime <= 0 ? "N/A" : indep.format(new java.util.Date(this.currFromOpStationInTime)))
		  .append(" Out:").append(this.currFroOpStationOutTime <= 0 ? "N/A" : indep.format(new java.util.Date(this.currFroOpStationOutTime)))
		  .append("\n")
		  .append(" ToStn:").append(this.currToOpStationId)
		  .append(" In:").append(this.currToInTime <= 0 ? "N/A" : indep.format(new java.util.Date(this.currToInTime)))
		  .append(" Out:").append(this.currToOutTime <= 0 ? "N/A" : indep.format(new java.util.Date(this.currToOutTime)))
		  .append("\n")
		  .append(" Challan:").append(this.currSrcDestChallanDate <= 0 ? "N/A" : indep.format(new java.util.Date(this.currSrcDestChallanDate)))
		  .append(" ChUpd:").append(this.currSrcDestChallanUpdDate <= 0 ? "N/A" : indep.format(new java.util.Date(this.currSrcDestChallanUpdDate)))
		  .append(" ChAssign:").append(this.currChallanAssignTime <= 0 ? "N/A" : indep.format(new java.util.Date(this.currChallanAssignTime)))
		  .append("\n")
		  .append(" LastProc:").append(this.lastProcessedGRT <= 0 ? "N/A" : indep.format(new java.util.Date(this.lastProcessedGRT)))
		  .append(" CountDirty:").append(this.dataOpCount).append(" Dirty:").append(dirty).append(" StartGps:").append(startGpsData)
		  ;
		sb.append("\n");
		sb.append(" Possible:").append(currPossibleSrcDestList);
		sb.append("\n");
		sb.append(" Master:").append(currMasterSrcDestList);
		sb.append("\n");
		sb.append(" ETACalc:").append(etaInfo);
		return sb.toString();
	}
	public NewETAforSrcDestItem getSpecificETA() {
		int srcDestId = this.currPossibleSrcDestList == null || this.currPossibleSrcDestList.size() == 0 ? Misc.getUndefInt() : this.currPossibleSrcDestList.get(0).first;
		int idx = NewVehicleETA.getSrcDestIndexInETAInfo(etaInfo, srcDestId);
		return idx < 0 || etaInfo == null || idx >= etaInfo.size() ? null : etaInfo.get(idx).first;
	}
	
	
	public String getFrom(Connection conn, byte mode) throws Exception {
		if (this.cachedFromName != null && this.cachedFromNameMode == mode)
			return cachedFromName;
		
		ChallanInfo ch = null;
		String name = null;
		if (this.currSrcDestChallanDate > 0)
			ch = NewVehicleETA.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
		if (ch != null) {
			name = ch.getFromLoc();
			if (mode == 1) {//get consignor
				if (ch.getSrcTextInfo() != null && ch.getSrcTextInfo().getCustName() != null)
					name = ch.getSrcTextInfo().getCustName();
			}
		}
		if (name == null) {
			name = SrcDestHelper.getName(conn, vehicleId, this.currFromOpStationId, Misc.getUndefInt(), Misc.getUndefDouble(), Misc.getUndefDouble(), null);
		}
		if (name == null) {
			NewETAforSrcDestItem etaItem = this.getSpecificETA();
			int srcDestId = etaItem == null ? Misc.getUndefInt() : etaItem.getSrcDestId();

			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, srcDestId);
			if (srcDestInfo != null)
				name = srcDestInfo.getSrcName();
		}
		cachedFromName = name;
		this.cachedFromNameMode = mode;
		return name;
	}
	
	public String getTo(Connection conn, byte mode) throws Exception {
		if (this.cachedToName != null && mode == this.cachedToNameMode)
			return cachedToName;
		
		ChallanInfo ch = null;
		String name = null;
		if (this.currSrcDestChallanDate > 0)
			ch = NewVehicleETA.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
		if (ch != null) {
			name = ch.getToLoc();
			if (mode == 1) {//get consignor
				if (ch.getTextInfo() != null && ch.getTextInfo().getCustName() != null)
					name = ch.getTextInfo().getCustName();
			} 
		}
		if (name == null) {
			name = SrcDestHelper.getName(conn, vehicleId, this.currToOpStationId, Misc.getUndefInt(), Misc.getUndefDouble(), Misc.getUndefDouble(), null);
		}
		if (name == null) {
			NewETAforSrcDestItem etaItem = this.getSpecificETA();
			int srcDestId = etaItem == null ? Misc.getUndefInt() : etaItem.getSrcDestId();
			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, srcDestId);
			if (srcDestInfo != null)
				name = srcDestInfo.getDestName();
		}
		cachedToName = name;
		this.cachedToNameMode = mode;
		return name;
	}
	public long getIntermediateETA(NewETAforSrcDestItem specificETA, int ofIndex) {
		NewETAforSrcDestItem.WayPointETA nextWayPointETA = specificETA.getIntermediateTransitInfo(ofIndex);
		boolean useful = nextWayPointETA != null && this.currFromOpStationInTime > 0;
		
		if (useful && nextWayPointETA.getTransitTimeFromSrc() < 0.0005) {
			useful = false;
		}
		if (useful) {
			return this.currFroOpStationOutTime + (long)(24*3600*1000*nextWayPointETA.getTransitTimeFromSrc());
		}
		return Misc.getUndefInt();
	}
	public long getCurrETA(NewETAforSrcDestItem specificETA) {
		long ts = this.currFroOpStationOutTime;
		if (ts <= 0)
			ts = this.currFromOpStationInTime;
		double transit = specificETA == null ? Misc.getUndefDouble() : specificETA.getTransitTime();
		if (ts <= 0 || transit < 0)
			return Misc.getUndefInt();
		ts = (long) (ts+transit*24*3600*1000);
		return ts;
	}
	public long getEstETA(NewETAforSrcDestItem specificETA) {
		long ts = this.currFroOpStationOutTime;
		if (ts <= 0)
			ts = this.currFromOpStationInTime;
		//System.out.println("{asd}"+new java.util.Date(ts));
		double transit = specificETA == null ? Misc.getUndefDouble() : Misc.isUndef(specificETA.getLatestEstTransitTime()) ? specificETA.getTransitTime() : specificETA.getLatestEstTransitTime();
		if (ts <= 0 || transit < 0)
			return Misc.getUndefInt();
		//if (transit < specificETA.getTransitTime())
		//	transit = specificETA.getTransitTime();
		ts = (long) (ts+transit*24*3600*1000);
		return ts;
		
	}

	private static long getNextMinAlertDueAt(long baseTime, int idx, ArrayList<NewETAforSrcDestItem.AlertDetails> adlist, long currMin) {
		long retval = -1;
		if (baseTime > 0 && idx >= 0 && adlist != null && idx < adlist.size()) {
			retval = baseTime + 60000*adlist.get(idx).getFlexId();
		}
		return retval > 0 && (currMin <= 0 || currMin > retval) ? retval : currMin;
	}

	private static void udapteNextAsyncCheck(int vehicleId, long nextAlertDueAt) {
		if (!g_doAsyncNextCheck)
			return;
		Long tsLng = g_vehToNextTSCheck.get(vehicleId);
		long tsLngAsLong = tsLng == null ? Misc.getUndefInt() : tsLng.longValue();
		if (tsLng != null) {
			NextCheck chk = new NextCheck(vehicleId, tsLng.longValue());
			synchronized (g_nextCheckForVeh) {
				int idx = (int) g_nextCheckForVeh.indexOf(chk).first;
				g_nextCheckForVeh.remove(idx);
			}
			g_vehToNextTSCheck.remove(vehicleId);
		}
		if (nextAlertDueAt > 0) {
			//give a gap of 8 min for data to come in
			
			if (tsLngAsLong > 0 && tsLngAsLong < nextAlertDueAt)
				nextAlertDueAt = tsLngAsLong;
			long now = System.currentTimeMillis();
			if ((nextAlertDueAt-now) < 8*60*1000)
				nextAlertDueAt = now + 8*60*1000;
			
			synchronized (g_nextCheckForVeh) {
				g_nextCheckForVeh.add(new NextCheck(vehicleId, nextAlertDueAt));
			}
			g_vehToNextTSCheck.put(vehicleId, new Long(nextAlertDueAt));
		}
		else {
			//do nothing ... it has already been removed
		}
	}
//Async helper
	
	private static FastList<NextCheck> g_nextCheckForVeh = new FastList<NextCheck>();//will contain items sorted by next check for being late
	private static ConcurrentHashMap<Integer, Long> g_vehToNextTSCheck = new ConcurrentHashMap<Integer, Long>();
	private static ScheduledExecutorService g_asyncChecker = null;
	public static void initAsyncChecker() {
		if (NewVehicleETA.g_doAsyncNextCheck) {
			//go thru all vehicleETA, 
			Connection conn = null;
			boolean destroyIt = false;
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				ArrayList<Integer> vehList = new ArrayList<Integer>();
				Collection<NewVehicleETA> etaList = NewVehicleETA.g_vehicleETACache.values();
				for (NewVehicleETA vehicleETA : etaList)
					vehList.add(vehicleETA.getVehicleId());
				long nowTime = Misc.getSystemTime();
				for (Integer vehicleId : vehList) {
					try { //check and and send alert and set up nextTodoList
						helperAsnycChecker(conn, vehicleId, nowTime, false);
					}
					catch (Exception e) {
						e.printStackTrace();
						if (conn != null) {
							DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
							conn = null;
							conn = DBConnectionPool.getConnectionFromPoolNonWeb();
						}//close and get connection
					}//end of catch
				}//for each veh
			}
			catch (Exception e) {
				
			}
			finally {
				if (conn != null) {
					try {
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
					}
					catch (Exception e2) {
						
					}
				}
				
			}
			g_asyncChecker = Executors.newScheduledThreadPool(1);
			g_asyncChecker.scheduleWithFixedDelay(new AsyncRunner(), 10, 10, TimeUnit.MINUTES);
		}
	}
	
	public void removeAlertsOngoing(Connection conn, boolean fullRedo) {
		PreparedStatement ps = null;
		try {
			if (fullRedo) {
				ps = conn.prepareStatement("delete from eta_alerts_new where vehicle_id=?");
				ps.setInt(1, vehicleId);
			}
			else {
				ps = conn.prepareStatement("delete from eta_alerts_new where vehicle_id=? and trip_from_in >= ?");
				ps.setInt(1, vehicleId);
				ps.setTimestamp(2, Misc.longToSqlDate(this.currFromOpStationInTime));
			}
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	public static class AsyncRunner implements Runnable {
		public void run() {
			long currTime = Misc.getSystemTime();
			NewVehicleETA.doAsyncCheck(currTime);
		}
	}
	
	
	private static void doAsyncCheck(long currTime) {
		boolean destroyIt = false;
		Connection conn = null;
		try {
			ArrayList<Integer> vehToProcess = new ArrayList<Integer>();
			synchronized (g_nextCheckForVeh) { //to avoid deadlock ... since while process vdf we may need to take lock on this
				int i=0;
				for (int is = g_nextCheckForVeh.size(); i<is; i++) {
					if (g_nextCheckForVeh.get(i).ts <= currTime) {
						vehToProcess.add(g_nextCheckForVeh.get(i).vehicleId);
					}
					else {
						break;
					}
				}
				g_nextCheckForVeh.removeFromStart(i);
			}
			if (vehToProcess != null && vehToProcess.size() > 0) {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				for (Integer vehicleId : vehToProcess) {
					try {
						g_vehToNextTSCheck.remove(vehicleId);//now consistent .. with HashMap .. from which vehEntry was removed
						helperAsnycChecker(conn, vehicleId, currTime, true);
					}
					catch (Exception e2) {
						destroyIt = true;
						e2.printStackTrace();
					}
				}//for each vehicle whoe time is past
			}//if there were vheicle to process
		}//end try
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				conn = null;
			}
			catch (Exception e2) {
				
			}
		}
	}

	private static void helperAsnycChecker(Connection conn, int vehicleId, long nowTime, boolean sendAlert) throws Exception {
		CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
		if (vehSetup == null)
			return;
		VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false);
		if (vdf == null)
			return;
		synchronized (vdf) {
			boolean toTraceVehicle = NewVehicleETA.toTrace(vehicleId); 
			if (toTraceVehicle) {
				System.out.println("[ETA_ASYNC"+vehicleId+ " Now:"+new java.util.Date(nowTime));
			}
			NewVehicleETA vehicleETA = NewVehicleETA.getETAObj(conn, vehicleId);
			if (vehicleETA ==  null)
				return;
			if (vehicleETA.currPossibleSrcDestList == null || vehicleETA.currPossibleSrcDestList.size() == 0)
				return;
			int etaEntryIndex = NewVehicleETA.getSrcDestIndexInETAInfo(vehicleETA.etaInfo, vehicleETA.currPossibleSrcDestList.get(0).first);
			if (etaEntryIndex < 0)
				return;
			Pair<NewETAforSrcDestItem, Boolean> etaEntry = vehicleETA.etaInfo.get(etaEntryIndex);
			
			StopDirControl stopDirControl = vehSetup.getStopDirControl(conn);
			NewVehicleData vdp = vdf.getDataList(conn, vehicleId, 0, true);
			GpsData lastPoint = vdp.getLast(conn);
			vehicleETA.processAndSendAlert(conn, etaEntry.first, etaEntry.second, vdp, lastPoint, vehSetup, stopDirControl, vdf, sendAlert, nowTime);
			if (toTraceVehicle) {
				System.out.println("[ETA_ASYNC:"+vehicleId+ " Done Now:"+new java.util.Date(nowTime));
			}
		}//sync block
	}
	// get Orient specific customer name
	public String getCustomer(Connection conn, int vehicleId) {
		if (Misc.isUndef(vehicleId))
			return null;
		
		ChallanInfo ch = null;
		String name = null;
		if (this.currSrcDestChallanDate > 0)
			ch = NewVehicleETA.getChallanInfo(conn, vehicleId, this.currSrcDestChallanDate);
		if (ch != null) {
			name = ch.getCustomer();
		}
		return name;
	}
	public static void main(String[] args) throws Exception {
		Tester.callMain(args);
	}
	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public ArrayList<MiscInner.PairIntBool> getCurrMasterSrcDestList() {
		return currMasterSrcDestList;
	}

	public void setCurrMasterSrcDestList(
			ArrayList<MiscInner.PairIntBool> currMasterSrcDestList) {
		this.currMasterSrcDestList = currMasterSrcDestList;
	}

	public ArrayList<MiscInner.PairIntBool> getCurrPossibleSrcDestList() {
		return currPossibleSrcDestList;
	}

	public void setCurrPossibleSrcDestList(
			ArrayList<MiscInner.PairIntBool> currPossibleSrcDestList) {
		this.currPossibleSrcDestList = currPossibleSrcDestList;
	}

	public ArrayList<Pair<NewETAforSrcDestItem, Boolean>> getEtaInfo() {
		return etaInfo;
	}

	public void setEtaInfo(ArrayList<Pair<NewETAforSrcDestItem, Boolean>> etaInfo) {
		this.etaInfo = etaInfo;
	}

	public int getCurrFromOpStationId() {
		return currFromOpStationId;
	}

	public void setCurrFromOpStationId(int currFromOpStationId) {
		this.currFromOpStationId = currFromOpStationId;
	}

	public int getCurrToOpStationId() {
		return currToOpStationId;
	}

	public void setCurrToOpStationId(int currToOpStationId) {
		this.currToOpStationId = currToOpStationId;
	}

	public long getCurrFromOpStationInTime() {
		return currFromOpStationInTime;
	}

	public void setCurrFromOpStationInTime(long currFromOpStationInTime) {
		this.currFromOpStationInTime = currFromOpStationInTime;
	}

	public long getCurrFroOpStationOutTime() {
		return currFroOpStationOutTime;
	}

	public void setCurrFroOpStationOutTime(long currFroOpStationOutTime) {
		this.currFroOpStationOutTime = currFroOpStationOutTime;
	}

	public long getCurrSrcDestChallanDate() {
		return currSrcDestChallanDate;
	}

	public void setCurrSrcDestChallanDate(long currSrcDestChallanDate) {
		this.currSrcDestChallanDate = currSrcDestChallanDate;
	}

	public long getCurrSrcDestChallanUpdDate() {
		return currSrcDestChallanUpdDate;
	}

	public void setCurrSrcDestChallanUpdDate(long currSrcDestChallanUpdDate) {
		this.currSrcDestChallanUpdDate = currSrcDestChallanUpdDate;
	}

	public long getCurrChallanAssignTime() {
		return currChallanAssignTime;
	}

	public void setCurrChallanAssignTime(long currChallanAssignTime) {
		this.currChallanAssignTime = currChallanAssignTime;
	}

	public long getCurrToInTime() {
		return currToInTime;
	}

	public void setCurrToInTime(long currToInTime) {
		this.currToInTime = currToInTime;
	}

	public long getCurrToOutTime() {
		return currToOutTime;
	}

	public void setCurrToOutTime(long currToOutTime) {
		this.currToOutTime = currToOutTime;
	}

	public long getLastProcessedGRT() {
		return lastProcessedGRT;
	}

	public void setLastProcessedGRT(long lastProcessedGRT) {
		this.lastProcessedGRT = lastProcessedGRT;
	}

	public GpsData getStartGpsData() {
		return startGpsData;
	}

	public void setStartGpsData(GpsData startGpsData) {
		this.startGpsData = startGpsData;
	}

	public int getDataOpCount() {
		return dataOpCount;
	}

	public void setDataOpCount(int dataOpCountSinceDirty) {
		this.dataOpCount = dataOpCountSinceDirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	public boolean isAlertSentThisTime() {
		return alertSentThisTime;
	}
	public void setAlertSentThisTime(boolean alertSentThisTime) {
		this.alertSentThisTime = alertSentThisTime;
	}
	public long getLastProcessedEstCal() {
		return lastProcessedEstCal;
	}
	public void setLastProcessedEstCal(long lastProcessedEstCal) {
		this.lastProcessedEstCal = lastProcessedEstCal;
	}
	
	private int removeNotificationId(Connection conn, int forAlertType, int forTargetType, int notificationTypeId) {
		if (forTargetType < 0) {
			removeNotificationId(conn, forAlertType, SrcDestInfo.G_TARGET_RECEIVER, notificationTypeId);
			removeNotificationId(conn, forAlertType, SrcDestInfo.G_TARGET_SENDER, notificationTypeId);
			removeNotificationId(conn, forAlertType, SrcDestInfo.G_TARGET_TRANSPORTER, notificationTypeId);
			return Misc.getUndefInt();
		}
		ArrayList<MiscInner.Triple> list = forTargetType == SrcDestInfo.G_TARGET_RECEIVER ? notificationIdsForReceiver : forTargetType == SrcDestInfo.G_TARGET_SENDER ? notificationIdsForSender : notificationIdsForTransporter;
		int retval = Misc.getUndefInt();
		for (int i=list == null ? -1 : list.size()-1; i>=0; i--) {
			if ((list.get(i).first == forAlertType || forAlertType < 0) && (list.get(i).second == notificationTypeId || notificationTypeId < 0)) {
				retval = list.get(i).third;
				NewETAAlertHelper.closeNotification(conn, retval);
				list.remove(i);
			}
		}
		return retval;
	}
	
	private void addNotificationId(Connection conn, int forAlertType, int forTargetType, int notificationTypeId, int id) {
		ArrayList<MiscInner.Triple> list = forTargetType == SrcDestInfo.G_TARGET_RECEIVER ? notificationIdsForReceiver : forTargetType == SrcDestInfo.G_TARGET_SENDER ? notificationIdsForSender : notificationIdsForTransporter;
		if (list == null) {
			if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER) 
				list = this.notificationIdsForReceiver = new ArrayList<MiscInner.Triple>();
			else if (forTargetType == SrcDestInfo.G_TARGET_SENDER) 
				list = this.notificationIdsForSender = new ArrayList<MiscInner.Triple>();
			else
					list = this.notificationIdsForTransporter = new ArrayList<MiscInner.Triple>();
		}
		MiscInner.Triple entry = null;
		for (int i=0,is=list == null ? 0 : list.size(); i<is; i++) {
			if (list.get(i).first == forAlertType && list.get(i).second == notificationTypeId) {
				entry = list.get(i);
				break;
			}
		}
		if (entry == null) {
			list.add(new MiscInner.Triple(forAlertType, notificationTypeId, id));
		}
		else {
			//already removed
		//	NewETAAlertHelper.closeNotification(conn, forAlertType, forTargetType, notificationTypeId, entry.second);
			entry.second = id; 
		}
	}
	private void helperRememberNotification(Connection conn, int forAlertType, ArrayList<MiscInner.Triple> notificationTargetIdList) {
		
		for (int i=0,is=notificationTargetIdList == null ? 0 : notificationTargetIdList.size(); i<is; i++) {
			this.removeNotificationId(conn, forAlertType, notificationTargetIdList.get(i).first, Misc.getUndefInt());
		}
		for (int i=0,is=notificationTargetIdList == null ? 0 : notificationTargetIdList.size(); i<is; i++) {
			this.addNotificationId(conn, forAlertType, notificationTargetIdList.get(i).first, notificationTargetIdList.get(i).second, notificationTargetIdList.get(i).third);
		}
	}
	
	private boolean isOpenNotificationByEventTy(Connection conn, int forAlertTy) {
		for (int art=0;art<3;art++) {
			ArrayList<MiscInner.Triple> theList = art == 0 ? this.notificationIdsForSender : art == 1 ? this.notificationIdsForTransporter : this.notificationIdsForReceiver;
			for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++) {
				if (NewETAAlertHelper.isOpenNotification(conn, theList.get(i).third))
					return true;
			}
		}
		return false;
	}
}

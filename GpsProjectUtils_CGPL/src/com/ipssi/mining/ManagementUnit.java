package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.sql.ResultSet;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.common.ds.trip.NewProfileCache;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.Region;
import com.ipssi.common.ds.trip.ThreadContextCache;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.common.ds.trip.TripInfoConstants;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.LUInfoExtract;
import com.ipssi.geometry.Point;

public class ManagementUnit {
	private static long baseLineMaxTSBack = 4*60*60*1000;
	private static long cleanupFreqMilli = 20*60*1000;
	
	private static ConcurrentHashMap<Integer, ManagementUnit> g_allManagementUnits = new ConcurrentHashMap<Integer, ManagementUnit>();
	
	public synchronized static ManagementUnit getManagementUnit(int portNodeId) {
		try {
			if (Misc.isUndef(portNodeId))
				return null;
			ManagementUnit retval = g_allManagementUnits.get(portNodeId);
			if (retval == null) {
				retval = new ManagementUnit(portNodeId);//dont ensure all here now ... we will do in prepForRedo
				g_allManagementUnits.put(portNodeId, retval);
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private int portNodeId = Misc.G_TOP_LEVEL_PORT;
	
	private long lastLoadFromInclTS = Misc.getUndefInt();
	private long lastCleanupAt = Misc.getUndefInt();//these two to help with clean up .. basically cleanup will happen x min after prev clean or succEnsureAt
	private long lastSuccEnsureDataAt = Misc.getUndefInt();	
	private ArrayList<Station> loadStations = new ArrayList<Station>();
	private Map<Integer, Integer> stationIndexById = new HashMap<Integer, Integer>();
	private ArrayList<CurrentStation> mergedStationList = new ArrayList<CurrentStation>();
	private Map<Integer, Pair<Station, RecentVehicleInfo>> currentVehicleToStation = new HashMap<Integer, Pair<Station, RecentVehicleInfo>>();
	private Map<Integer, UnloadStation> unloadCache = new HashMap<Integer, UnloadStation>();
	private Map<Integer, GpsData> lastPointsOfVehicle = new ConcurrentHashMap<Integer, GpsData>();//to avoid deadlock we cant expect to get from NewVehicleData
	private boolean toInitEnsure = true;
	public static class Statistics {
		public long statCountBestLookup = 0;
		public long statWithIdSuccLookup = 0;
		public long statCountAddPt = 0;
		public long statStationCountMi = 0;
		public long statStationCountMx = 0;
		public long statCurrStationCountMi = 0;
		public long statCurrStationCountMx = 0;
		public long statSaveStationCount = 0;
		public long statEnsureDataReq = 0;
		
		public long statCountBestLookupCumm = 0;
		public long statWithIdSuccLookupCumm = 0;
		public long statCountAddPtCumm = 0;
		public long statStationCountMiCumm = 0;
		public long statStationCountMxCumm = 0;
		public long statCurrStationCountMiCumm = 0;
		public long statCurrStationCountMxCumm = 0;
		public long statSaveStationCountCumm = 0;
		public long statEnsureDataReqCumm = 0;
		
		public void updBestLookup(boolean hasId) {
			this.statCountBestLookup++;
			this.statCountBestLookupCumm++;
			if (hasId) {
				this.statWithIdSuccLookup++;
				this.statWithIdSuccLookupCumm++;
			}
		}
		
		public void updAddLoadPoint() {
			this.statCountAddPt++;
			this.statCountAddPtCumm++;
		}
		
		public void updAddStation(ManagementUnit mu) {
			int cnt = mu.loadStations.size();
			if (cnt < this.statStationCountMi)
				this.statStationCountMi = cnt;
			if (cnt > this.statStationCountMx)
				this.statStationCountMx = cnt;
			if (cnt < this.statStationCountMiCumm)
				this.statStationCountMiCumm = cnt;
			if (cnt > this.statStationCountMxCumm)
				this.statStationCountMxCumm = cnt;
		}
		
		public void updAddCurrStation(ManagementUnit mu) {
			int cnt = mu.mergedStationList.size();
			if (cnt < this.statCurrStationCountMi)
				this.statCurrStationCountMi = cnt;
			if (cnt > this.statCurrStationCountMx)
				this.statCurrStationCountMx = cnt;
			if (cnt < this.statCurrStationCountMiCumm)
				this.statCurrStationCountMiCumm = cnt;
			if (cnt > this.statCurrStationCountMxCumm)
				this.statCurrStationCountMxCumm = cnt;
		}
		
		public void updSaveStation() {
			this.statSaveStationCount++;
			this.statSaveStationCountCumm++;
		}
		
		public void updEnsureData() {
			this.statEnsureDataReq++;
			this.statEnsureDataReqCumm++;
		}
		
		public void reset(ManagementUnit mu) {
			statCountBestLookup = 0;
			statWithIdSuccLookup = 0;
			statCountAddPt = 0;
			statStationCountMi = statStationCountMx = mu.loadStations.size();
			statCurrStationCountMi = statCurrStationCountMx = mu.mergedStationList.size();
			statSaveStationCount = 0;
			statEnsureDataReq = 0;
		}
		public String toString() {
			return toString(false, null);
		}
		public String toString(boolean toReset, ManagementUnit mu) {
			StringBuilder sb = new StringBuilder();
			sb.append("[MU Stats]").append(new java.util.Date()).append(" ")
			.append(" BestLookupCount:").append(statCountBestLookup).append(",").append(statCountBestLookupCumm)
			.append(" SuccLookupWithId:").append(statWithIdSuccLookup).append(",").append(statWithIdSuccLookupCumm)
			.append(" AddPt").append(statCountAddPt).append(",").append(statCountAddPtCumm)
			.append(" MinStation:").append(statStationCountMi).append(",").append(statStationCountMiCumm)
			.append(" MaxStation:").append(statStationCountMx).append(",").append(statStationCountMxCumm)
			.append(" MinCurrStation:").append(statCurrStationCountMi).append(",").append(statCurrStationCountMiCumm)
			.append(" MaxCurrStation:").append(statCurrStationCountMx).append(",").append(statCurrStationCountMxCumm)
			.append(" SaveStation:").append(statSaveStationCount).append(",").append(statSaveStationCountCumm)
			.append(" DataReq:").append(statEnsureDataReq).append(",").append(statEnsureDataReqCumm)
			;
			if (mu != null)
				sb.append(" Latest Stations Count:").append(mu.loadStations.size()).append(" Current Stations:").append(mu.mergedStationList.size());
			if (toReset && mu != null)
				this.reset(mu);
			return sb.toString();
		}
	}
	private Statistics statistics = new Statistics();
	
	
	private void reset() {
		loadStations.clear();
		this.unloadCache.clear();
		this.currentVehicleToStation.clear();
		this.mergedStationList.clear();
		lastLoadFromInclTS = -1;
		lastCleanupAt = -1;
		this.lastSuccEnsureDataAt = -1;
		this.stationIndexById.clear();
		this.toInitEnsure = true;
	}

	//call below on every pt receipt in TP
	public void setLastPoint(int vehicleId, GpsData data) { //dont synchronize
		this.lastPointsOfVehicle.put(vehicleId, data);
	}
	public GpsData getLastPoint(int vehicleId) {//dont synchronize
		return this.lastPointsOfVehicle.get(vehicleId);
	}
	//call below to check if given pt is near an opstation and its support
	public synchronized BestMergeDistResult getNearestStation(Connection conn, GpsData data, NewVehicleData vdp, boolean lowDistThresh) {
		BestMergeDistResult retval = this.getBestMergeByDist(new GpsDataLike(data), Misc.getUndefInt(), true, lowDistThresh);
		if (retval.bestIndex >= 0 && retval.isWithingRange) {
			Station stn = loadStations.get(retval.bestIndex);
			retval.bestIndex = stn.getId();
		}
		else {
			retval.bestIndex = Misc.getUndefInt();
		}
		return retval;
	}
	public static synchronized void initOnFirstTripVehicleProcess(Connection conn, VehicleControlling vehicleControlling) {
		Collection<ManagementUnit> mulist = ManagementUnit.g_allManagementUnits.values();
		for (ManagementUnit mu : mulist) {
			if (mu.toInitEnsure) {
				mu.initDistParamsAsBox(conn, vehicleControlling);
				mu.ensureDataFrom(conn, -1);
				mu.toInitEnsure = false;
			}
		}
	}

	private synchronized void initDistParamsAsBox(Connection conn, VehicleControlling vehicleControlling) {
		try {
			List<OpStationBean> beanList = TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, this.portNodeId, TripInfoConstants.PREFERRED_UNLOAD_HIPRIORITY, 0, vehicleControlling);
			double typicalLon = 0;
			double typicalLat = 0;
			int count = 0;
			for (OpStationBean bean:beanList) {
				int gateId = bean.getGateAreaId();
				RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(gateId, conn);
				if (rth == null || rth.region == null)
					continue;
				com.ipssi.geometry.Point pt = rth.region.getCenter();
				typicalLon += pt.getX();
				typicalLat += pt.getY();
				count++;
			}
			typicalLon /= (double) count;
			typicalLat /= (double) count;
			if (count == 0) {
				typicalLon = 82.5;
				typicalLat = 23.5;
			}
			GpsDataLike.refCenterLon = (int)(Math.round(typicalLon*GpsDataLike.doubleToIntMult));
			GpsDataLike.refCenterLat = (int)(Math.round(typicalLat*GpsDataLike.doubleToIntMult));
			com.ipssi.geometry.Region reg = com.ipssi.geometry.Region.getLongLatBoxAround(new Point(typicalLon, typicalLat), GpsDataLike.lowDistThreshKM);
			GpsDataLike.lowDistThreshDeltaLon = (int)Math.round((reg.m_urCoord.getX()-typicalLon) * GpsDataLike.doubleToIntMult);
			GpsDataLike.lowDistThreshDeltaLat = (int)Math.round((reg.m_urCoord.getY()-typicalLat) * GpsDataLike.doubleToIntMult);
			GpsDataLike.lowSqr = GpsDataLike.lowDistThreshDeltaLon*GpsDataLike.lowDistThreshDeltaLon + GpsDataLike.lowDistThreshDeltaLat+GpsDataLike.lowDistThreshDeltaLat;
			reg = com.ipssi.geometry.Region.getLongLatBoxAround(new Point(typicalLon, typicalLat), GpsDataLike.highDistThreshKM);
			GpsDataLike.highDistThreshDeltaLon = (int)((reg.m_urCoord.getX()-typicalLon) * GpsDataLike.doubleToIntMult);
			GpsDataLike.highDistThreshDeltaLat = (int)((reg.m_urCoord.getY()-typicalLat) * GpsDataLike.doubleToIntMult);

			reg = com.ipssi.geometry.Region.getLongLatBoxAround(new Point(typicalLon, typicalLat), CurrentStation.currMergeKM);
			CurrentStation.currMergeLonDeltaInt = (int)((reg.m_urCoord.getX()-typicalLon) * GpsDataLike.doubleToIntMult);
			CurrentStation.currMergeLatDeltaInt = (int)((reg.m_urCoord.getY()-typicalLat) * GpsDataLike.doubleToIntMult);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	//call below in TP before initiating Recovery
	public synchronized void prepBeforeFullRedo(Connection conn, int vehicleId) {
		//0. assumes all relevant trips deleted of vehicles being redone ... else we will have dupli stuff
		//if vehicleId is given ... then remove from RecentStuff ... else simply clear stuff 
		//1. delete all art_load_opstations and pts
		//2. delete all trip_alerts
		try {
			if (Misc.isUndef(vehicleId)) {
				this.reset();			
				
				PreparedStatement ps = conn.prepareStatement("delete from trip_alerts where port_node_id=?");
				ps.setInt(1, portNodeId);
				ps.execute();
				ps = Misc.closePS(ps);
	
				ps = conn.prepareStatement("delete from art_load_opstation_points using art_load_opstation_points join art_load_opstations on (art_load_opstation_points.art_load_opstation_id = art_load_opstations.id) where art_load_opstations.port_node_id=?");
				ps.setInt(1, portNodeId);
				ps.execute();
				ps = Misc.closePS(ps);
	
				ps = conn.prepareStatement("delete from art_load_opstations where port_node_id=?");
				ps.setInt(1, portNodeId);
				ps.execute();
				ps = Misc.closePS(ps);
	
			}
			else {
				Pair<Station, RecentVehicleInfo> currRecent = this.currentVehicleToStation.get(vehicleId);
				if (currRecent != null && currRecent.second == null) {
					currRecent = null;
					currentVehicleToStation.remove(vehicleId);
				}
				if (currRecent != null) {
					if (currRecent.first != null)
						currRecent.first.removeLatestEntryForVehicle(vehicleId, this);
					if (currRecent.second.getUnload() != null) {
						UnloadStation ustn = this.getUnloadStation(currRecent.second.getUnload());
						if (ustn != null)
							ustn.removeRecentVehicleInfo(vehicleId);
					}
				}
			}//if doing selective vehicle fullRedo
			//this.ensureDataFrom(conn, -1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	//call below in TP when at end of Recovery
	public synchronized void handleUpdateAfterRecovery(Connection conn, int vehicleId, LUInfoExtract load, LUInfoExtract unload, NewVehicleData vdp) {
		try {
			handleUpdateOnLoad(conn, vehicleId, load, null, Misc.getUndefInt(), vdp,true, false);
			handleUpdateOnUnload(conn, vehicleId, unload, null, Misc.getUndefInt(), load, vdp, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public synchronized static void handleUpdateAfterAllRecoveryAllMU(Connection conn) {
		Collection<ManagementUnit> mulist = ManagementUnit.g_allManagementUnits.values();
		for (ManagementUnit mu : mulist) {
			mu.handleUpdateAfterAllRecovery(conn);
		}
	}

	public synchronized static void printStatsAllMU() {
		Collection<ManagementUnit> mulist = ManagementUnit.g_allManagementUnits.values();
		for (ManagementUnit mu : mulist) {
			mu.printStats(true);
		}
	}
	
	public synchronized static void removeUnnecessaryPointsAllMU(Connection conn)  {
		Collection<ManagementUnit> mulist = ManagementUnit.g_allManagementUnits.values();
		for (ManagementUnit mu : mulist) {
			try {
				mu.removeUnnecessaryPts(conn, true);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
		}
	}
	public void printStats(boolean toReset) {
		System.out.println(this.statistics.toString(toReset, this));
	}
	public  synchronized static void saveAllStationPoints(Connection conn) {
		try {
			Collection<ManagementUnit> allmus = ManagementUnit.g_allManagementUnits.values();
			for (ManagementUnit mu: allmus) {
				mu.saveStationPoints(conn);
			}
		}
		catch (Exception e) {
			
		}
	}
	public synchronized void handleUpdateAfterAllRecovery(Connection conn) {
		try {
			saveStationPoints(conn);
			this.removeUnnecessaryPts(conn, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void handleRemoveOnLoad(Connection conn, int vehicleId, long oldGateInTS, NewVehicleData vdp, int givenStationId) {
		try {
			GpsData oldPt = vdp.get(conn, new GpsData(oldGateInTS));
			if (oldPt != null) {
				this.removeLoadPt(conn, oldGateInTS, oldPt.getLongitude(), oldPt.getLatitude(), vehicleId, givenStationId);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	//call below on Trip Processor on new load station being formed always
	public synchronized BestMergeDistResult handleUpdateOnLoad(Connection conn, int vehicleId, LUInfoExtract curr, GpsData ginGpsData, long oldGateInTS, NewVehicleData vdp, boolean isLatest, boolean doStationPts)  {
		//will add to Station.points db only if reasonably confirmed ..and return the same
		//doStationPts
		// .... when ensureDataPoints is called it will directly write to station points ...
		// .... when doingRecovery ... we will in newProcessControl (ie for each point) pass doStationPts as true
		//                                                   and isLatest as false
		//....              once recovery is done, we will make one more call with LatestTripInfo but pass doStationPts as false and doingLatest as true
		//... otherwise in normal circumstances we will set doStationPts to true and set latest to true at last LU
		//... pts should be saved whenever latest is true
		//... pts should be attempted to be cleaned when recovery complete as must or in normal course on latest
		//
		//if oldGateInTS > 0 => we are updating an existing entry due to whatever reasons
		//get station to add to ..
		BestMergeDistResult retval = null;
		if (curr == null)
			return null;
		try {
			boolean doingLastStepOfRecovery =!doStationPts && isLatest;
			
			long dataNeededFrom = curr.getGateIn();
			if (oldGateInTS > 0 && oldGateInTS < dataNeededFrom)
				dataNeededFrom = oldGateInTS;
			this.ensureDataFrom(conn, dataNeededFrom);
			Station removed = null;
			Station addedTo = null;
			int givenStationId = curr.getArtOpStationId();
			GpsData pt = ginGpsData == null ? vdp.get(conn, new GpsData(curr.getGateIn())) : ginGpsData;
			boolean isConfirmed = this.isConfirmedStation(curr);
			if (!isConfirmed)
				doStationPts = false;
			boolean luChanging = oldGateInTS > 0 && oldGateInTS != curr.getGateIn();
			
			if (doStationPts) {
				if (luChanging) {
					//remove previous stuff
					GpsData oldPt = vdp.get(conn, new GpsData(oldGateInTS));
					if (oldPt != null) {
						removed = this.removeLoadPt(conn, oldGateInTS, oldPt.getLongitude(), oldPt.getLatitude(), vehicleId, givenStationId);
					}
				}
				//now add stations list trend
				if (pt != null) {
					retval = this.addLoadPt(conn, curr.getGateIn(), pt.getLongitude(), pt.getLatitude(), vehicleId, isLatest, givenStationId, Misc.getUndefInt());
					if (retval.bestIndex >= 0 && retval.isWithingRange) {
						addedTo = loadStations.get(retval.bestIndex);
						retval.bestIndex = addedTo.getId();
					}
					else {
						retval.bestIndex = Misc.getUndefInt();
					}
				}
			}
			else {
				retval = this.getBestMergeByDist(new GpsDataLike(pt), Misc.getUndefInt(), true, false);
				if (retval.bestIndex >= 0 && retval.isWithingRange) {
					addedTo = loadStations.get(retval.bestIndex);
					retval.bestIndex = addedTo.getId();
				}
				else {
					retval.bestIndex = Misc.getUndefInt();
				}
			}
			if (addedTo == null)
				return retval;
			if (isLatest) {//handle changes to current view of things
				//1. if previous pt was removed .. remove from latestEntry and 
				//1.1also rearrange station around removed
				//2. close out previous latest if any
				//3. rearrange
				//4. add to recent latestEntry
				
				if (luChanging) {
					//1. if previous pt was removed .. remove from latestEntry
					
					Pair<Station, RecentVehicleInfo> inVehToStatationLatestMapPr = this.currentVehicleToStation.get(vehicleId);
					if (inVehToStatationLatestMapPr != null && inVehToStatationLatestMapPr.second == null) {//should not happen
						inVehToStatationLatestMapPr = null;
						currentVehicleToStation.remove(vehicleId);
					}
					Station inVehToStatationLatestMap = inVehToStatationLatestMapPr == null ? null : inVehToStatationLatestMapPr.first;
					if (inVehToStatationLatestMap != null) {
						inVehToStatationLatestMap.removeLatestEntryForVehicle(vehicleId, this);
						UnloadStation ustn = getUnloadStation(inVehToStatationLatestMapPr.second.getUnload());
						if (ustn != null) {
							ustn.removeRecentVehicleInfo(vehicleId);
						}
						currentVehicleToStation.remove(vehicleId);
					}
					
					if (inVehToStatationLatestMap != null) {
						MiscInner.Pair removedPos = getIndexOfStationInCurrentStations(inVehToStatationLatestMap);
						if (removedPos != null && removedPos.first >= 0) {
							rearrangeCurrentStationList(removedPos, inVehToStatationLatestMap);
						}	
					}
				}
	
				//2. close out previous latest if any
				Pair<Station, RecentVehicleInfo> inVehToStatationLatestMapPr = this.currentVehicleToStation.get(vehicleId);
				if (inVehToStatationLatestMapPr != null && inVehToStatationLatestMapPr.second == null) {//should not happen
					inVehToStatationLatestMapPr = null;
					currentVehicleToStation.remove(vehicleId);
				}
	
				Station inVehToStatationLatestMap = inVehToStatationLatestMapPr == null ? null : inVehToStatationLatestMapPr.first;
				if (inVehToStatationLatestMap != null) {
					RecentVehicleInfo recentEntry = inVehToStatationLatestMapPr.second; 
					if (recentEntry != null) {
						//handle updates to lead etc
						recentEntry.updateLeadEtc(conn, curr, true, pt, vdp);
						inVehToStatationLatestMap.makeDirty(this);
					}
					//mark unloadStn if it exits to dirty (i.e calc to be done ... but otherwise lead etc calc above
					UnloadStation unstn = this.getUnloadStation(inVehToStatationLatestMapPr.second.getUnload());
					if (unstn != null)
						unstn.setDirty(true);
				}
				
				//3. rearrange
				if (addedTo != null) {
					MiscInner.Pair addedToPos = getIndexOfStationInCurrentStations(addedTo);
					int newAddedToPos = rearrangeCurrentStationList(addedToPos, addedTo);
					
					//4. add to recent latestEntry			
					CurrentStation addedToCurr = this.mergedStationList.get(newAddedToPos);
					RecentVehicleInfo recentVehicleInfo = new RecentVehicleInfo(vehicleId, curr, null); 
					addedTo.addRecentVehicle(recentVehicleInfo, this);
					currentVehicleToStation.put(vehicleId, new Pair<Station, RecentVehicleInfo>(addedTo, recentVehicleInfo));
				}
				if (doStationPts) //otherwise there will be no changes!!
					saveStationPoints(conn);
				cleanupCurrentOpList();
				//assumes .... that
			}//if latest
			return !doStationPts || addedTo == null ? null : retval;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//call below on Trip Processor on latest being unload
	public synchronized void handleUpdateOnUnload(Connection conn, int vehicleId, LUInfoExtract curr, GpsData ginGpsData, long oldGateInTS, LUInfoExtract loadIfExists, NewVehicleData vdp, boolean isLatest) {
		//TODO U if startingnew trip though prev is L
		try {
			if (curr == null)
				return;
			Pair<Station, RecentVehicleInfo> inVehToStatationLatestMapPr = this.currentVehicleToStation.get(vehicleId);
			RecentVehicleInfo old = inVehToStatationLatestMapPr == null ? null : inVehToStatationLatestMapPr.second;
			if (old == null) {
				//brand new entry ..
				RecentVehicleInfo entry = new RecentVehicleInfo(vehicleId, null, curr);
				UnloadStation ustn = getUnloadStation(curr);
				if (ustn != null) {
					ustn.addRecentVehicleInfo(entry);
					this.currentVehicleToStation.put(vehicleId, new Pair<Station, RecentVehicleInfo>(null, entry));
				}
			}//brand new entry for RecentVehicleInfo
			else {
				if (old.getUnload() == null) {
					old.updateLeadEtc(conn, curr, false, ginGpsData, vdp);
					UnloadStation ustn = this.getUnloadStation(old.getUnload());
					if (ustn != null)
						ustn.addRecentVehicleInfo(old);				
				}//previous was not yet unloaded ... so we updated that
				else {//either u is being updated .. or trip without l being created
					//if being updated then oldGateInTS will be non-zero
					if (oldGateInTS <=0) { //new entry starting with U
						old.updateLeadEtc(conn, curr, true, ginGpsData, vdp);//though curr is unload we just want to confirm prior
						if (inVehToStatationLatestMapPr != null && inVehToStatationLatestMapPr.first != null)
							inVehToStatationLatestMapPr.first.makeDirty(this);
						RecentVehicleInfo entry = new RecentVehicleInfo(vehicleId, null, curr);
						UnloadStation ustn = getUnloadStation(curr);
						if (ustn != null) {
							ustn.addRecentVehicleInfo(entry);
							this.currentVehicleToStation.put(vehicleId, new Pair<Station, RecentVehicleInfo>(null, entry));
						}		
					}//new trip starting with unload
					else {
						if (oldGateInTS != curr.getGateIn() || (old.getUnload() != null && old.getUnload().getPrefOrMovingOpId() != curr.getPrefOrMovingOpId())) {//being changed ... so remove
							UnloadStation ustn = getUnloadStation(old.getUnload());
							if (ustn != null) {
								ustn.removeRecentVehicleInfo(vehicleId);
							}
							old.updateLeadEtc(conn, curr, false, ginGpsData, vdp);
							ustn = getUnloadStation(curr);
							if (ustn != null)
								ustn.addRecentVehicleInfo(old);
							if (inVehToStatationLatestMapPr != null && inVehToStatationLatestMapPr.first != null)
								inVehToStatationLatestMapPr.first.makeDirty(this);
						}
						else { //some calc changes but otherwise no diff
							old.updateLeadEtc(conn, curr, false, ginGpsData, vdp);//though curr is unload we just want to confirm prior
						}
					}//updated to older unload entry
				}//exiting entry had unloaded (old.unload != null)
			}//an existing RecentVehicleInfo exists
			cleanupCurrentOpList();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}

	private static boolean isConfirmedStation(LUInfoExtract ext) {
		return ext.getWaitOut() > 0;
	}
	//call this to load etc ... actually called implicitly
	private synchronized void ensureDataFrom(Connection conn, long fromTS) { 
		//will ensure that we have data from ... thereafter it will be synch as trips are made
		PreparedStatement ps =null;
		ResultSet rs = null;
		
		try {
			StringBuilder sb = new StringBuilder();
			if (lastLoadFromInclTS >= 0 && fromTS >= lastLoadFromInclTS)
				return;
			
			long loadDataFrom = fromTS;
			long loadDataTo = lastLoadFromInclTS;
			if (loadDataFrom < 0 || loadDataTo < 0) {
				//get min/max
				sb.append("select min(trip_info.load_gate_in), max(trip_info.load_gate_in) from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)")
				.append(" join trip_info on (trip_info.vehicle_id = vehicle.id) where vehicle.status=1");
				ps = conn.prepareStatement(sb.toString());
				ps.setInt(1, this.portNodeId);
				rs = ps.executeQuery();
				long miTS = 0;
				long mxTS = 0;
				if (rs.next()) {
					miTS = Misc.sqlToLong(rs.getTimestamp(1));
					mxTS = Misc.sqlToLong(rs.getTimestamp(2));
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				sb.setLength(0);
				if (loadDataFrom <0)
					loadDataFrom = mxTS - ManagementUnit.baseLineMaxTSBack;
				if (loadDataTo < 0)
					loadDataTo = mxTS+1000;
				if (loadDataFrom < 0)
					loadDataFrom = 0;
				this.lastLoadFromInclTS = loadDataFrom;
				if (loadDataFrom <= 0) 
					return; //nothing to load
			}
			saveStationPoints(conn);
			sb.append("select lgd.vehicle_id, lgd.gps_record_time, lgd.longitude, lgd.latitude, trip_info.load_art_opstation_id from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)")
			.append(" join trip_info on (trip_info.vehicle_id = vehicle.id) join logged_data lgd on (lgd.vehicle_id = trip_info.vehicle_id and lgd.attribute_id=0 and lgd.gps_record_time = trip_info.load_gate_in)")
			.append(" where vehicle.status=1 and trip_info.load_gate_in >= ? and trip_info.load_gate_in < ? order by lgd.gps_record_time")
			;
			System.out.println("[MU Ensure] fromTS:"+(Misc.longToUtilDate(fromTS))+ " From:"+Misc.longToUtilDate(loadDataFrom) +" To:"+Misc.longToUtilDate(loadDataTo));
			this.statistics.updEnsureData();
			ps = conn.prepareStatement(sb.toString());
			ps.setInt(1, this.portNodeId);
			ps.setTimestamp(2, Misc.longToSqlDate(loadDataFrom));
			ps.setTimestamp(3,Misc.longToSqlDate(loadDataTo));
			rs = ps.executeQuery();
			boolean foundData = false;
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				long ts = Misc.sqlToLong(rs.getTimestamp(2));
				double lon = rs.getDouble(3);
				double lat = rs.getDouble(4);
				int priorLoadStationId = Misc.getRsetInt(rs, 5);
				this.addLoadPt(conn, ts, lon, lat, vehicleId, false, priorLoadStationId, Misc.getUndefInt());
				foundData = true;
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			//load data from moving stations ...
			List<OpStationBean> movingOp = TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, this.getPortNodeId(), 0, 1, null);
			if (movingOp != null) {
				ps = conn.prepareStatement("select logged_data.longitude, logged_data.latitude, logged_data.gps_record_time from logged_data where vehicle_id=? and attribute_id=0 and gps_record_time >= ? and gps_record_time < ? order by gps_record_time");
				ps.setTimestamp(2, Misc.longToSqlDate(loadDataFrom));
				ps.setTimestamp(3,Misc.longToSqlDate(loadDataTo));
				ThreadContextCache threadContextCache = new ThreadContextCache();
				for (OpStationBean opb : movingOp) {
					ps.setInt(1, opb.getLinkedVehicleId());
					CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(opb.getLinkedVehicleId(), conn);
					VehicleControlling vehicleControlling = NewProfileCache.getOrCreateControlling(opb.getLinkedVehicleId());
					rs = ps.executeQuery();
					while (rs.next()) {
						double lon = Misc.getRsetDouble(rs, 1);
						double lat = Misc.getRsetDouble(rs, 2);
						long ts = Misc.sqlToLong(rs.getTimestamp(3));
						GpsData dummy = new GpsData(ts);
						dummy.setLongitude(lon);
						dummy.setLatitude(lat);
						dummy.setDimensionInfo(0,0);
						if (opb.isOperational(conn, threadContextCache, dummy, this.getPortNodeId(), vehicleControlling, vehSetup))
							this.addLoadPt(conn, ts, lon, lat, opb.getLinkedVehicleId(), false, Misc.getUndefInt(), opb.getOpStationId());
					}
					rs = Misc.closeRS(rs);
				}
				ps = Misc.closePS(ps);
			}
			
			this.lastSuccEnsureDataAt = Misc.getSystemTime();
			this.resetMiMxOfStation(); //no point in saving pts already saved
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	private synchronized void cleanupCurrentOpList() {
		for (int i=this.mergedStationList.size()-1; i>=0;i--) {
			if (this.mergedStationList.get(i).getStations().size() == 0) {
				this.mergedStationList.remove(i);
			}
		}
	}

	private synchronized UnloadStation getUnloadStation(LUInfoExtract ext) {
		return ext == null || Misc.isUndef(ext.getPrefOrMovingOpId()) ? null : getUnloadStation(ext.getPrefOrMovingOpId());
	}
	
	private synchronized UnloadStation getUnloadStation(int opStationId) {
		if (Misc.isUndef(opStationId))
			return null;
		UnloadStation retval = unloadCache.get(opStationId);
		if (retval == null) {
			retval = new UnloadStation(opStationId);
			unloadCache.put(opStationId, retval);
		}
		return retval;
	}
	private synchronized int rearrangeCurrentStationList(MiscInner.Pair currEntry, Station station) {
		//returns the new positions
		int bestIndex= -1;
		for (int i=mergedStationList.size()-1; i>=0; i--) {
			CurrentStation entry = this.mergedStationList.get(i);
			if (entry.getStations().size() == 0) {
				continue;
			}
			Station repStn = entry.getStations().get(0);
			if (repStn.isCurrMergeable(station)) {
				bestIndex = i;
				break;
			}
		}
		if (currEntry != null && currEntry.first != bestIndex && currEntry.first >= 0) {
			CurrentStation currentMerged = this.mergedStationList.get(currEntry.first);
			currentMerged.removeStation(station);
		}
		if (currEntry != null && currEntry.first == bestIndex) {
			//no changes
		}
		else {
			CurrentStation toAddTo = bestIndex >= 0 ? this.mergedStationList.get(bestIndex) : null;
			if (toAddTo == null) {
				toAddTo = new CurrentStation();
				this.mergedStationList.add(toAddTo);
				bestIndex = this.mergedStationList.size()-1;
			}
			toAddTo.addStation(station);
		}
		this.statistics.updAddCurrStation(this);
		return bestIndex;
	}
	synchronized CurrentStation getCurrentStationForStation(Station station) {
		MiscInner.Pair pr = getIndexOfStationInCurrentStations(station);
		if (pr != null && pr.first >= 0)
			return this.mergedStationList.get(pr.first);
		return null;
	}
	private synchronized MiscInner.Pair getIndexOfStationInCurrentStations(Station station) {//first = index in megedStationList, second index within that's opstation list
		for (int i=0,is=this.mergedStationList == null ? 0 : mergedStationList.size(); i<is; i++) {
			CurrentStation cs = this.mergedStationList.get(i);
			ArrayList<Station> slist = cs.getStations();
			for (int j=0,js=slist.size(); j<js; j++) {
				if (slist.get(j).getId() == station.getId()) {
					return new MiscInner.Pair(i,j);
				}
			}
		}
		return null;
	}
	
	
	//call with must=true during redo after each set of 10K pts
	public synchronized void removeUnnecessaryPts(Connection conn, boolean must) throws Exception {
		long now = Misc.getSystemTime();
		if (!must) {
			if (lastCleanupAt < 0)
				lastCleanupAt = now;
			long gap = now - (lastSuccEnsureDataAt > lastCleanupAt ? lastSuccEnsureDataAt : lastCleanupAt);
			must = gap > cleanupFreqMilli;
		}
		if (!must)
			return;
		//get global max ... then esure that we have base
		long currMax = -1;
		boolean succDelete = false;
		long deleteBefore = -1;
		long actualDeleteBefore = lastLoadFromInclTS;
		synchronized (loadStations) {
			for (Station station:loadStations) {
				if (station.points.size() == 0)
					continue;
				GpsDataLike last = station.points.get(station.points.size()-1);
				if (last == null) {
				//	int dbg=1;
					continue;
				}
				if (last.getEndGpsRecordTimeIncl() > currMax)
					currMax = last.getEndGpsRecordTimeIncl();
			}
			deleteBefore = currMax - baseLineMaxTSBack;
			
			if (deleteBefore > 0) {
				GpsDataLike dummy = new GpsDataLike(deleteBefore);
				boolean stationRemoved = false;
				for (int i=loadStations.size()-1; i>=0 ;i--) {
					Station station = loadStations.get(i);
					Pair<Integer, Boolean> index= station.points.indexOf(dummy);
					int idx = index.second ? index.first-1 : index.first;
					if (idx > 0) {
						station.savePoints(conn);
						System.out.println("[MU removeUnnec:]"+station.getId()+" Pt:"+station.points.get(idx));
						GpsDataLike ptBeingRemoved = station.points.get(idx-1);
						if (actualDeleteBefore < 0 || actualDeleteBefore < ptBeingRemoved.getEndGpsRecordTimeIncl()) {
							actualDeleteBefore = ptBeingRemoved.getEndGpsRecordTimeIncl()+100;//
						}
						station.points.removeFromStart(idx);
						succDelete = true;
					}
					if (station.points.size() == 0) {
						loadStations.remove(i);
						stationRemoved = true;
					}
					
				}
				if (stationRemoved) {
					this.statistics.updAddStation(this);
					this.repopulateIndexLookup();
				}
			}
		}
		this.lastCleanupAt = now;
		if (succDelete) {
			this.lastLoadFromInclTS = actualDeleteBefore;
		}
	}
	
	private synchronized void repopulateIndexLookup() {
		this.stationIndexById.clear();
		for (int i=loadStations.size()-1; i>=0 ;i--) {
			Station station = loadStations.get(i);
			stationIndexById.put(station.getId(), i);
		}
	}
	
	private synchronized Station removeLoadPt(Connection conn, long ts, double lon, double lat, int vehicleId, int givenStationId) {
		Station retval = null;
		try {
			boolean stationRemoved = false;
			GpsDataLike data = new GpsDataLike(ts, lon, lat);
			BestMergeDistResult temp = getBestMergeByDist(data, givenStationId, true, true);
			retval = temp.bestIndex >= 0 && temp.isWithingRange ? loadStations.get(temp.bestIndex) : null;
			if (retval != null) {
				retval.removeFromList(conn, data, temp);
				//retval.dbgCheckSupport();
				if (retval.points.size() == 0) {
					stationRemoved = true;
					retval.savePoints(conn);
					loadStations.remove(temp.bestIndex);
				}
			}
			if (stationRemoved) {
				this.statistics.updAddStation(this);
				this.repopulateIndexLookup();
			}
	
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	public synchronized BestMergeDistResult addMovingOpstation(Connection conn, int opstationId, int vehicleId, GpsData dataPt) {
		try {
			return this.addLoadPt(conn, dataPt.getGps_Record_Time(), dataPt.getLongitude(), dataPt.getLatitude(), vehicleId, true, Misc.getUndefInt(), opstationId);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return null;
	}
	private synchronized BestMergeDistResult addLoadPt(Connection conn, long ts, double lon, double lat, int vehicleId, boolean cleanIfNeeded, int givenStationId, int movingOpStationId) {
		BestMergeDistResult retval = null;
		Station station= null;
		try {
			GpsDataLike data = new GpsDataLike(ts, lon, lat);
			retval = getBestMergeByDist(data, givenStationId, true, false);
			int bestMergePlace = retval.bestIndex;
			//if (bestMergePlace < 0 || !retval.isWithingRange) {
			//	int dbg = 1;
			//	getBestMergeByDist(data, givenStationId);
			//	dbg++;
			//}
			station = bestMergePlace >=0 ? loadStations.get(bestMergePlace) : null;
			if (station == null || retval == null || !retval.isWithingRange) {
				if (retval == null)
					retval = new BestMergeDistResult();
				else
					retval.reinit();
				station = addNewStation(conn, givenStationId, vehicleId, ts, retval== null ? Misc.getUndefInt() : retval.deltaLonToNearest, retval== null ? Misc.getUndefInt() : retval.deltaLatToNearest, station == null ? Misc.getUndefInt() : station.getId());
				retval.bestIndex = this.getStationIndexById(station.getId());//generally will be at end
				retval.isWithingRange = true;
				retval.support = 1;
				retval.ptIndexBefore = -1;
				retval.deltaLatToNearest = 0;
				retval.deltaLonToNearest = 0;
				retval.refPtIsAfter = true;
				retval.tsGap = 0;
				if (!Misc.isUndef(movingOpStationId)) {
					retval.movingOpStationIds = new ArrayList<Integer>();
					retval.movingOpStationIds.add(movingOpStationId);
				}
			}
			System.out.println("[DBG adpt]" + data);
			station.addToList(conn, data, retval, movingOpStationId);
			if (cleanIfNeeded) {
				this.removeUnnecessaryPts(conn, false);
			}
			//station.dbgCheckSupport();
			this.statistics.updAddLoadPoint();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}
	
	private synchronized int getNewStationId(Connection conn, int givenStationId, int forVehicleId, long forGRT, double distNearest, int nearestOtherOpId) throws Exception {
		if (Misc.isUndef(givenStationId)) {
			PreparedStatement ps = conn.prepareStatement("insert into art_load_opstations(port_node_id, created_at_vehicle_id, created_at_grt, created_at_min_dist, nearest_station_id) values (?,?,?,?,?)");
			ps.setInt(1, portNodeId);
			Misc.setParamInt(ps, forVehicleId, 2);
			ps.setTimestamp(3, Misc.longToSqlDate(forGRT));
			Misc.setParamDouble(ps, distNearest, 4);
			Misc.setParamInt(ps, nearestOtherOpId, 5);
			ps.execute();
			ResultSet rs = ps.getGeneratedKeys();
			int retval = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return retval;
		}
		else {
			return givenStationId;	
		}
	}
	private synchronized int getStationIndexById(int id) {
		Integer iv = this.stationIndexById.get(id);
		if (iv == null)
			return Misc.getUndefInt();
		return iv.intValue();
	}
	private synchronized Station addNewStation(Connection conn, int givenId, int forVehicleId, long forGRT, int deltaLonIntNearest, int deltaLatIntNearest, int nearestOtherOpId) throws Exception {
		double distNearest = Misc.getUndefDouble();
		if (!Misc.isUndef(deltaLonIntNearest)) {
			double deltaLon = (double)deltaLonIntNearest/(double)GpsDataLike.doubleToIntMult;
			double deltaLat = (double)deltaLatIntNearest/(double)GpsDataLike.doubleToIntMult;
			distNearest = Point.fastGeoDistance(0, 0, deltaLon, deltaLat);
		}
		int id = getNewStationId(conn, givenId, forVehicleId, forGRT, distNearest, nearestOtherOpId);
		int idx = this.getStationIndexById(id);
		
		Station retval = idx < 0 ? new Station(id) : loadStations.get(idx);
		if (idx < 0) {
			loadStations.add(retval);
			this.stationIndexById.put(id, loadStations.size()-1);
			this.statistics.updAddStation(this);
		}
		return retval;
	}
	//private synchronized BestMergeDistResult getBestMergeByDist(GpsDataLike data, int givenStationId) {
		//1st = stationId, 2nd = support
	//	return getBestMergeByDist(data, givenStationId, true);
	//}
	private synchronized BestMergeDistResult getBestMergeByDist(GpsDataLike data, int givenStationId, boolean tightTime, boolean lowDistThresh) {
		BestMergeDistResult retval = new BestMergeDistResult();
		if (!Misc.isUndef(givenStationId)) {
			int stationIndex = this.getStationIndexById(givenStationId);
			Station station = stationIndex < 0 ? null : loadStations.get(stationIndex);
			if (station != null) {
				this.helperGetDistTimeForPtInStation(data, station, retval);
				long ts = retval.tsGap;
				long gap = Math.abs(ts-data.getGpsRecordTime());
				boolean timeThreshMatches = !tightTime ||  gap <= GpsDataLike.nearnessInTimeMatchMilli;
				boolean distThreshMatches = lowDistThresh ? 
						GpsDataLike.isWithinLowDistThresh(retval.deltaLonToNearest, retval.deltaLatToNearest)
						: GpsDataLike.isWithinHighDistThresh(retval.deltaLonToNearest, retval.deltaLatToNearest);
				if (timeThreshMatches && distThreshMatches) {
					retval.isWithingRange = true;
					//retval.bestIndex = stationIndex;
					this.statistics.updBestLookup(true);
					return retval;
				}
			}
		}
		int bestIndex = -1;
		int bestLonDelta = Misc.getUndefInt();
		int bestLatDelta = Misc.getUndefInt();
		long bestGap = (long) Misc.LARGE_NUMBER;
		int bestSupport = -1;
		boolean bestThreshMatches = false;
		BestMergeDistResult temp = new BestMergeDistResult();
		for (int i=0,is = loadStations == null ? 0 : loadStations.size(); i<is; i++) {
			temp.reinit();
			Station station = loadStations.get(i);
			this.helperGetDistTimeForPtInStation(data, station, temp);
			long ts = temp.tsGap;
			int support = temp.support;
			long gap = Math.abs(ts-data.getGpsRecordTime());
			boolean timeThreshMatches = !tightTime ||  gap <= GpsDataLike.nearnessInTimeMatchMilli;
			boolean distThreshMatches = lowDistThresh ?
					GpsDataLike.isWithinLowDistThresh(temp.deltaLonToNearest, temp.deltaLatToNearest)
					:
					GpsDataLike.isWithinHighDistThresh(temp.deltaLonToNearest, temp.deltaLatToNearest);
			
			boolean threshMatches = timeThreshMatches && distThreshMatches;
			int suppGap = support - bestSupport;
			int suppCmp = (bestSupport >= 6 && support >= 6) || (suppGap >= -1 && suppGap <= 1) ? 0 : suppGap > 0 ? -1 : 1; 
			int distCmp =suppCmp != 0 ? suppCmp : GpsDataLike.cmpFuzzyDist(temp.deltaLonToNearest, temp.deltaLatToNearest, bestLonDelta, bestLatDelta);
			if (bestThreshMatches && !threshMatches)
				distCmp = 1;
			else if (!bestThreshMatches && threshMatches)
				distCmp = -1;
			if ( bestIndex < 0 || distCmp < 0 || (distCmp == 0 && gap < bestGap)) {
				bestIndex = i;
				bestLonDelta = temp.deltaLonToNearest;
				bestLatDelta = temp.deltaLatToNearest;
				bestGap = gap;
				bestSupport = support;
				bestThreshMatches = threshMatches;
				retval.copy(temp);
			}
		}//for each station
		retval.isWithingRange = bestThreshMatches;
		this.statistics.updBestLookup(false);
		return retval;
	}
	
	private synchronized void helperGetDistTimeForPtInStation(GpsDataLike data, Station station, BestMergeDistResult retval) {
		retval.bestIndex = this.getStationIndexById(station.getId());
		if (retval.bestIndex < 0)
			return;
		
		Pair<Integer,Boolean> pos = station.points.indexOf(data);
		int ptIndex = pos.first;
		retval.ptIndexBefore = ptIndex;
		long ts = Misc.getUndefInt();
		GpsDataLike refPt = station.points.get(ptIndex);
		GpsDataLike nextPt = station.points.get(ptIndex+1);
		int deltaLon = Misc.getUndefInt();
		int deltaLat = Misc.getUndefInt();

		if (nextPt != null) {
			if (refPt == null) {
				refPt = nextPt;
				deltaLon =refPt.getAvgLonInt() - data.getAvgLonInt();
				deltaLat =refPt.getAvgLatInt() - data.getAvgLatInt();
				retval.refPtIsAfter = true;
			}
			else {
				int nextLonDeltaInt = data.getAvgLonInt() - nextPt.getAvgLonInt();
				int nextLatDeltaInt = data.getAvgLatInt() - nextPt.getAvgLatInt();
				deltaLon =refPt.getAvgLonInt() - data.getAvgLonInt();
				deltaLat =refPt.getAvgLatInt() - data.getAvgLatInt();

				long prevd = deltaLat * deltaLat + deltaLon * deltaLon;
				long nextd = nextLatDeltaInt*nextLatDeltaInt + nextLonDeltaInt*nextLonDeltaInt;
				if (nextd < prevd && !Misc.isEqual(nextd, prevd)) {
					refPt = nextPt;
					deltaLon = nextLonDeltaInt;
					deltaLat = nextLatDeltaInt;
					retval.refPtIsAfter = true;
				}
				else {
				}
			}
		}
		else if (refPt != null) {
			deltaLon =refPt.getAvgLonInt() - data.getAvgLonInt();
			deltaLat =refPt.getAvgLatInt() - data.getAvgLatInt();			
		}
		if (refPt != null) {
			long gp1 = Math.abs(refPt.getGpsRecordTime() - data.getGpsRecordTime());
			long gp2 = Math.abs(refPt.getEndGpsRecordTimeIncl() - data.getGpsRecordTime());
			if (gp1 < gp2)
				ts = refPt.getGpsRecordTime();
			else
				ts = refPt.getEndGpsRecordTimeIncl();
		}
		retval.support = refPt == null ? Misc.getUndefInt() : refPt.getAvgSupport();
		retval.deltaLonToNearest = deltaLon;
		retval.deltaLatToNearest = deltaLat;
		retval.tsGap = ts;
		retval.movingOpStationIds = refPt.getMovingOpStationId();
	}
	
	private synchronized void resetMiMxOfStation() {
		for (Station station : loadStations) {
			station.resetMinMax();
		}
	}
	private synchronized void saveStationPoints(Connection conn) throws Exception {
		for (Station station : loadStations) {
			boolean saved = station.savePoints(conn);
			if (saved)
				this.statistics.updSaveStation();
		}
	}
	
	public ManagementUnit(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
}

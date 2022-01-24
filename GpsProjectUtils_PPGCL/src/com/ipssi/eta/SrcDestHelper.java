package com.ipssi.eta;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.eta.SrcDestInfo.AlertSetting;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.MiscInner.PairIntBool;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Region;
import com.ipssi.mapguideutils.NameLocationLookUp;
import com.ipssi.mapguideutils.PrintIdAll;
import com.ipssi.mapguideutils.RTreesAndInformation;

public class SrcDestHelper {
	public static class InInfoResult {
		public ArrayList<SrcDestInfo> srcList; //the src
		public ArrayList<SrcDestInfo> destList; 
		public ArrayList<Pair<SrcDestInfo, Integer>> intermediateList;
		public ArrayList<SrcDestInfo> areaOfOps;
		public ArrayList<SrcDestInfo> nearSrcList;
		public InInfoResult(ArrayList<SrcDestInfo> srcList, ArrayList<SrcDestInfo> destList, ArrayList<Pair<SrcDestInfo, Integer>> intermediateList, ArrayList<SrcDestInfo> areaOfOps, ArrayList<SrcDestInfo> nearSrcList) {
			this.srcList = srcList;
			this.destList = destList;
			this.intermediateList = intermediateList;
			this.areaOfOps = areaOfOps;
			this.nearSrcList = nearSrcList;
		}
	}
	public static InInfoResult getInInfo(Connection conn, Point pt, Cache cache, int vehicleId, boolean doSrc, boolean doDest, boolean doIntermediate, boolean doAreaOfOps) {
		ArrayList<SrcDestInfo> srcList = null;
		ArrayList<SrcDestInfo> destList = null;
		ArrayList<Pair<SrcDestInfo, Integer>> intermediateList = null;
		ArrayList<SrcDestInfo> areaOfOps = null;
		ArrayList<SrcDestInfo> nearSrcList = null;
		ArrayList<Pair<SrcDestInfo, Integer>> fullList = SrcDestHelper.getSrcDestListForIntermediatePoints(conn, pt);
		NewSrcDestProfileCache.Helper lookupHelper = new NewSrcDestProfileCache.Helper(conn, vehicleId);
		
		for (int i=0,is=fullList == null ? 0 : fullList.size(); i<is; i++) {
			Pair<SrcDestInfo, Integer> pr = fullList.get(i);
			SrcDestInfo info = pr.first;
			int idx = pr.second;
			if (doSrc && idx == -1) {
				if (lookupHelper.isInProfile(conn, info)) {
					if (srcList == null)
						srcList = new ArrayList<SrcDestInfo>();
					srcList.add(info);
				}
			}
			else if (doDest && idx == -2) {
				if (lookupHelper.isInProfile(conn, info)) {
					if (destList == null)
						destList = new ArrayList<SrcDestInfo>();
					destList.add(info);
				}
			}
			else if (idx <= SrcDestHelper.nearSrcOffset) {
				if (lookupHelper.isInProfile(conn, info)) {
					if (nearSrcList == null)
						nearSrcList = new ArrayList<SrcDestInfo>();
					nearSrcList.add(info);
				}
			}
			else if (doAreaOfOps && idx <= SrcDestHelper.areaOfOpsBasedOffset) {
				if (lookupHelper.isInProfile(conn, info)) {
					if (areaOfOps == null)
						areaOfOps = new ArrayList<SrcDestInfo>();
					areaOfOps.add(info);
				}
			}
			else if (doIntermediate && idx >= 0) {
				if (lookupHelper.isInProfile(conn, info)) {
					if (intermediateList == null)
						intermediateList = new ArrayList<Pair<SrcDestInfo, Integer>>();
					intermediateList.add(pr);
				}
			}
		}
		return new InInfoResult (srcList, destList, intermediateList, areaOfOps, nearSrcList);
	}
	
	public static Triple<ArrayList<Pair<SrcDestInfo, Integer>>, ArrayList<SrcDestInfo>, ArrayList<SrcDestInfo>> getComboInInfo(Connection conn, Point pt, ArrayList<PairIntBool> refSrcDestList) {
		//first = internediates found, second SrcDest in whose area of Ops it lies, third = list of srcDest to which it is near
		ArrayList<Pair<SrcDestInfo, Integer>> fullList = SrcDestHelper.getSrcDestListForIntermediatePoints(conn, pt);
		ArrayList<SrcDestInfo> areaOfOps = null;
		ArrayList<SrcDestInfo> near = null;
		for (int i=fullList == null ? -1 : fullList.size()-1; i>=0 ; i--) {
			Pair<SrcDestInfo, Integer> pr = fullList.get(i);
			SrcDestInfo info = pr.first;
			int idx = pr.second;
			
			if (NewVehicleETA.getSrcDestIndexInIntegerList(refSrcDestList, info.getId()) < 0) {
				fullList.remove(i);
			}
			if (idx <= SrcDestHelper.nearSrcOffset) {
				if (near == null)
					near = new ArrayList<SrcDestInfo>();
				near.add(info);
				fullList.remove(i);
			}
			else if (idx <= SrcDestHelper.areaOfOpsBasedOffset && idx > SrcDestHelper.nearSrcOffset) {
				if (areaOfOps == null) {
					areaOfOps = new ArrayList<SrcDestInfo>();
				}
				areaOfOps.add(info);
				fullList.remove(i);
			}
		}
		if (fullList != null)
			removeDupliSrcDest(fullList,  refSrcDestList);
		return new Triple<ArrayList<Pair<SrcDestInfo, Integer>>, ArrayList<SrcDestInfo>, ArrayList<SrcDestInfo>> (fullList, areaOfOps, near);
	}
	public static void handleChange(Connection conn, SrcDestInfo old, SrcDestInfo newItem, boolean noVehicleETA) {
		try {
			if (old == null && newItem == null)
				return;
			int srcDestId = old == null ? newItem.getId() : old.getId(); 
			newItem = SrcDestInfo.getSrcDestInfo(conn, srcDestId);
			if (newItem != null && newItem.getStatus() != 1)
				newItem = null;
			if (old != null)
				SrcDestHelper.removeSrcDestInfo(conn, old);
			if (newItem != null)
				SrcDestHelper.addSrcDestInfo(conn, newItem);
			//NOW do changes in NewVehicleETA
			if (!noVehicleETA &&(old != null || newItem != null)) {
				if (conn.getAutoCommit())
					conn.commit();
				Thread th = new RedoHelper(null, RedoHelper.G_UPDATE_SRC_DEST, newItem != null ? newItem.getId() : old.getId(), newItem == null);
				th.start();
				//NewVehicleETA.handleSrcDestInfoChange(conn, newItem != null ? newItem.getId() : old.getId(), newItem == null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static void addSrcDestInfo(Connection conn, SrcDestInfo srcDestInfo) {
		if (srcDestInfo == null)
			return;
		
		int opstationId = Misc.getUndefInt();
		int regionId = Misc.getUndefInt();
		double lon = Misc.getUndefDouble();
		double lat = Misc.getUndefDouble();
		double buffer = Misc.getUndefDouble();
		double maxNearSrcDist = Misc.getUndefDouble();
		ArrayList<AlertSetting> nearSrcReq = srcDestInfo.getAlertSettingCalc(conn, SrcDestInfo.ALERT_NEARING_SRC_BACK);
		for (int i=0,is=nearSrcReq == null ? 0 : nearSrcReq.size(); i<is; i++) {
			AlertSetting setting = nearSrcReq.get(i);
			if (setting.getDist() > -0.00005) {
				if (Misc.isUndef(maxNearSrcDist) || maxNearSrcDist < setting.getDist()) {
					maxNearSrcDist = setting.getDist();
				}
			}
		}
		srcDestInfo.setCalcMaxNearSrcDist(maxNearSrcDist);
		ArrayList<SrcDestInfo.WayPoint> waypointList = srcDestInfo.getWaypoints();
		for (int i=-2,is=waypointList == null ? 0 : waypointList.size(); i<is; i++) {
			if (i == -1) {//src
				opstationId = srcDestInfo.getSrcType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
				regionId = srcDestInfo.getSrcType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
				if (!Misc.isUndef(opstationId) || !Misc.isUndef(regionId)) {
					MiscInner.PairDouble lonLatOfStnReg = SrcDestHelper.getLonLat(conn, opstationId, regionId);
					lon = lonLatOfStnReg.first;
					lat = lonLatOfStnReg.second;
					srcDestInfo.setSrcLong(lon);
					srcDestInfo.setSrcLat(lat);

				}
				else {
					lon = srcDestInfo.getSrcLong();
					lat = srcDestInfo.getSrcLat();
				}
				srcDestInfo.setSrcName(getName(conn, Misc.getUndefInt(), srcDestInfo.getSrcType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt()
						, srcDestInfo.getSrcType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt()
						, srcDestInfo.getSrcLong(), srcDestInfo.getSrcLat(), srcDestInfo.getSrcName()));

				buffer = srcDestInfo.getSrcBuffer();
				if (Misc.isUndef(buffer)) {
					buffer = 20;
					srcDestInfo.setSrcBuffer(20);
				}
			}
			else if (i == -2) {//dest
				opstationId = srcDestInfo.getDestType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
				regionId = srcDestInfo.getDestType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
				if (!Misc.isUndef(opstationId) || !Misc.isUndef(regionId)) {
					MiscInner.PairDouble lonLatOfStnReg = SrcDestHelper.getLonLat(conn, opstationId, regionId);
					lon = lonLatOfStnReg.first;
					lat = lonLatOfStnReg.second;
					srcDestInfo.setDestLong(lon);
					srcDestInfo.setDestLat(lat);
				}
				else {
					lon = srcDestInfo.getDestLong();
					lat = srcDestInfo.getDestLat();
				}
				buffer = srcDestInfo.getDestBuffer();
				if (Misc.isUndef(buffer)) {
					buffer = 20;
					srcDestInfo.setDestBuffer(20);
				}
				srcDestInfo.setDestName(getName(conn, Misc.getUndefInt(), srcDestInfo.getDestType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt()
						, srcDestInfo.getDestType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt()
						, srcDestInfo.getDestLong(), srcDestInfo.getDestLat(), srcDestInfo.getDestName()));

			}
			else {
				SrcDestInfo.WayPoint waypoint = waypointList.get(i);
				opstationId = Misc.getUndefInt();
				regionId = waypoint.getRegionId();
				if (!Misc.isUndef(opstationId) || !Misc.isUndef(regionId)) {
					MiscInner.PairDouble lonLatOfStnReg = SrcDestHelper.getLonLat(conn, opstationId, regionId);
					lon = lonLatOfStnReg.first;
					lat = lonLatOfStnReg.second;
					waypoint.setLongitude(lon);
					waypoint.setLatitude(lat);
				}
				else {
					lon = waypoint.getLongitude();
					lat = waypoint.getLatitude();
				}
				buffer = waypoint.getBuffer();
				if (Misc.isUndef(buffer)) {
					buffer = 20;
					waypoint.setBuffer(20);
				}
				waypoint.setName(getName(conn, Misc.getUndefInt(), Misc.getUndefInt()
						, regionId
						, waypoint.getLongitude(), waypoint.getLatitude(), waypoint.getName()));

			}
			SrcDestHelper.addRtreeForIntermediate(conn, srcDestInfo.getId(), i, opstationId, regionId, lon, lat, buffer);
			if (i == -1) {
				if (!Misc.isUndef(maxNearSrcDist))
					SrcDestHelper.addRtreeForIntermediate(conn, srcDestInfo.getId(), SrcDestHelper.nearSrcOffset, Misc.getUndefInt(), Misc.getUndefInt(), lon, lat, maxNearSrcDist);
			}
		}
		ArrayList<Integer> areaOfOps = srcDestInfo.getAreaOfOpRegions();
		for (int i=0,is=areaOfOps == null ? 0 : areaOfOps.size(); i<is; i++) {
			regionId = areaOfOps.get(i);
			lon = Misc.getUndefDouble();
			lat = Misc.getUndefDouble();
			buffer = Misc.getUndefDouble();
			SrcDestHelper.addRtreeForIntermediate(conn, srcDestInfo.getId(), areaOfOpsBasedOffset-i, opstationId, regionId, lon, lat, buffer);
		}
	}

	public static String getName(Connection conn, int vehicleId, int opstationId, int regionId, double lon, double lat, String existingName) {
		String retval = null;
		if (!Misc.isUndef(opstationId)) {
			OpStationBean bean = TripInfoCacheHelper.getOpStation(opstationId);
			if (bean != null) {
				return bean.getOpStationName();
			}
		}
		if (!Misc.isUndef(regionId)) {
			RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regionId, conn);
			if (rth != null) {
				return rth.region.m_name;
			}
		}
		if (existingName != null)
			return existingName;
		if (!Misc.isUndef(lon) && !Misc.isUndef(lat)) {
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			try {
				return NameLocationLookUp.fetchLocationName(conn, vehSetup, new Point(lon, lat), vehSetup == null ? null : vehSetup.getDistCalcControl(conn));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	private static int areaOfOpsBasedOffset = -10000;
	private static int nearSrcOffset = -100000;
	public static void removeSrcDestInfo(Connection conn, SrcDestInfo srcDestInfo) {
		if (srcDestInfo == null)
			return;
		
		int opstationId = Misc.getUndefInt();
		int regionId = Misc.getUndefInt();
		double lon = Misc.getUndefDouble();
		double lat = Misc.getUndefDouble();
		double buffer = Misc.getUndefDouble();
		double maxNearSrcDist = Misc.getUndefDouble();
		ArrayList<AlertSetting> nearSrcReq = srcDestInfo.getAlertSettingCalc(conn, SrcDestInfo.ALERT_NEARING_SRC_BACK);
		for (int i=0,is=nearSrcReq == null ? 0 : nearSrcReq.size(); i<is; i++) {
			AlertSetting setting = nearSrcReq.get(i);
			if (setting.getDist() > -0.00005) {
				if (Misc.isUndef(maxNearSrcDist) || maxNearSrcDist < setting.getDist()) {
					maxNearSrcDist = setting.getDist();
				}
			}
		}
		ArrayList<SrcDestInfo.WayPoint> waypointList = srcDestInfo.getWaypoints();
		
		for (int i=-2,is=waypointList == null ? 0 : waypointList.size(); true; i++) {//will keep on going until in intermediate key -> id is not found
			if (i == -1) {//src
				opstationId = srcDestInfo.getSrcType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
				regionId = srcDestInfo.getSrcType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
				lon = srcDestInfo.getSrcLong();
				lat = srcDestInfo.getSrcLat();
				buffer = srcDestInfo.getSrcBuffer();
			}
			else if (i == -2) {//dest
				opstationId = srcDestInfo.getDestType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
				regionId = srcDestInfo.getDestType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
				lon = srcDestInfo.getDestLong();
				lat = srcDestInfo.getDestLat();
				buffer = srcDestInfo.getDestBuffer();
			}
			else if (i<is) {
				SrcDestInfo.WayPoint waypoint = waypointList.get(i) ;
				opstationId = Misc.getUndefInt();
				regionId = waypoint.getRegionId();
				lon = waypoint.getLongitude();
				lat = waypoint.getLatitude();
				buffer = waypoint.getBuffer();
			}
			if (i < is) {
				SrcDestHelper.removeRtreeForIntermediate(conn, srcDestInfo.getId(), i, opstationId, regionId, lon, lat, buffer);
				if (i == -1) {
					SrcDestHelper.removeRtreeForIntermediate(conn, srcDestInfo.getId(), SrcDestHelper.nearSrcOffset, Misc.getUndefInt(), Misc.getUndefInt(), lon, lat, maxNearSrcDist);
				}
			}
			else {
				String key = getComboKey(srcDestInfo.getId(), i);
				if (intermediatePairToId.containsKey(key)) {
					intermediatePairToId.remove(key);
				}
				else {
					break;
				}
			}
		}
		ArrayList<Integer> areaOfOps = srcDestInfo.getAreaOfOpRegions();
		for (int i=0,is=areaOfOps == null ? 0 : areaOfOps.size(); true; i++) {
			if (i < is) {
				regionId = areaOfOps.get(i);
				lon = Misc.getUndefDouble();
				lat = Misc.getUndefDouble();
				buffer = Misc.getUndefDouble();
			}
			if (i < is) {
				SrcDestHelper.removeRtreeForIntermediate(conn, srcDestInfo.getId(), areaOfOpsBasedOffset-i, opstationId, regionId, lon, lat, buffer);
			}
			else {
				String key = getComboKey(srcDestInfo.getId(), areaOfOpsBasedOffset-i);
				if (intermediatePairToId.containsKey(key)) {
					intermediatePairToId.remove(key);
				}
				else {
					break;
				}
			}
		}
	}

	public static SrcDestInfo getPossibleNearSrc(Connection conn, int vehicleId,  double lon, double lat) {
		try {
			Cache cache = Cache.getCacheInstance(conn);
			Point pt = new Point(lon, lat);
			ArrayList<Pair<SrcDestInfo, Integer>> fullList = SrcDestHelper.getSrcDestListForIntermediatePoints(conn, pt);
			int bestPriority = Integer.MAX_VALUE;
			double bestDist = Misc.LARGE_NUMBER;
			SrcDestInfo retval = null;
			NewSrcDestProfileCache.Helper lookupHelper = new NewSrcDestProfileCache.Helper(conn, vehicleId);
			
			
			for (int i=0,is = fullList == null ? 0 : fullList.size(); i<is;i++) {
				if (fullList.get(i).second <= SrcDestHelper.nearSrcOffset) {
					SrcDestInfo item = fullList.get(i).first;
					if (!lookupHelper.isInProfile(conn, item))
						continue;
					double myDist = pt.fastGeoDistance(new Point(item.getSrcLong(), item.getSrcLat()));
					int myPriority = item.getPriority();
					if ((myPriority < bestPriority) || (myPriority == bestPriority && bestDist > myDist)) {
						bestPriority = myPriority;
						bestDist = myDist;
						retval = item;
					}
				}
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return null;
	}
	
	private static ArrayList<Pair<SrcDestInfo, Integer>> getSrcDestListForIntermediatePoints(Connection conn, Point pt) {
		ArrayList<Pair<SrcDestInfo, Integer>> retval = null;
		double dbgd1 =pt.distance(new Point(75.68230456429714D,21.020681481994576D)); 
		if (dbgd1 < 10) {
			int dbg=1;
			dbg++;
		}
		try {
			List<Integer> arrayOfIds= null;
			PrintIdAll pid=new PrintIdAll(); 
			RTree rtree=SrcDestHelper.getIntermediateRTree();
			float flon = (float)pt.getX();
			float flat = (float)pt.getY();
			Rectangle rect = new Rectangle(flon, flat, flon, flat);
			synchronized (rtree) {
				rtree.intersects(rect, pid);
				arrayOfIds = pid.getArrayOfIds();
			}
			if (arrayOfIds != null) {
				for (Integer i:arrayOfIds) {
					MiscInner.Pair srcDestIntermediateIndexPr = SrcDestHelper.getIntermediateInfo(i);
					if (srcDestIntermediateIndexPr == null)
						continue;
					SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, srcDestIntermediateIndexPr.first);
					if (srcDestInfo == null)
						continue;
					int intermediateIndex = srcDestIntermediateIndexPr.second;
					int opId = Misc.getUndefInt();
					int regId = Misc.getUndefInt();
					if (intermediateIndex == -1) {
						opId = srcDestInfo.getSrcType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
						regId = srcDestInfo.getSrcType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt();
					}
					else if (intermediateIndex == -2) {
						opId = srcDestInfo.getDestType() == SrcDestInfo.G_OP_STATION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
						regId = srcDestInfo.getDestType() == SrcDestInfo.G_REGION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt();
					}
					else if (intermediateIndex <= SrcDestHelper.nearSrcOffset) {
						opId = Misc.getUndefInt();
						regId = Misc.getUndefInt();
					}
					else if (intermediateIndex <= SrcDestHelper.areaOfOpsBasedOffset) {
						int idx = SrcDestHelper.areaOfOpsBasedOffset-intermediateIndex;
						ArrayList<Integer> areaOfOpList = srcDestInfo == null ? null : srcDestInfo.getAreaOfOpRegions();
						if (areaOfOpList == null || idx < 0 || idx >= areaOfOpList.size()) 
							continue;
						regId = areaOfOpList.get(idx);
					}
					else {
						ArrayList<SrcDestInfo.WayPoint> waypointList = srcDestInfo == null ? null : srcDestInfo.getWaypoints();
						SrcDestInfo.WayPoint waypoint = waypointList == null || intermediateIndex < 0 || intermediateIndex >= waypointList.size() ? null : waypointList.get(intermediateIndex);
						if (waypoint == null)
							continue;
						regId = waypoint.getRegionId();
					}
					if (!Misc.isUndef(opId)) {
						OpStationBean opbean = TripInfoCacheHelper.getOpStation(opId);
						if (opbean != null)
							regId = opbean.getGateAreaId();
					}
					boolean toAdd = false;
					if (!Misc.isUndef(regId)) {
						RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regId, conn);
						if (rth != null) {
							toAdd = RegionTest.PointIn(conn, pt, regId);
						}
					}
					else {
						toAdd = true;
					}
					if (toAdd) {
						if (retval == null)
							retval = new ArrayList<Pair<SrcDestInfo, Integer>>();
						retval.add(new Pair<SrcDestInfo, Integer>(srcDestInfo, intermediateIndex));
					}
				}//for each src/dest found
			}//if arrayIds is not null
			if (retval != null) {
	 			Collections.sort(retval, new Comparator<Pair<SrcDestInfo, Integer>>() {
			        public int compare(Pair<SrcDestInfo, Integer> lhs, Pair<SrcDestInfo, Integer> rhs) {
			        	int srcDestIdCmp = lhs.first.compareTo(rhs.first);
			        	if (srcDestIdCmp != 0)
			        		return srcDestIdCmp;
			        	int lidx = lhs.second;
			        	int ridx = rhs.second;
			        	if (lidx == -2)
			        		lidx = lhs.first.getWaypoints() == null ? 0 : lhs.first.getWaypoints().size();
			        	if (ridx == -2)
			        		ridx = rhs.first.getWaypoints() == null ? 0 : rhs.first.getWaypoints().size();
			        	return lidx-ridx;
			        }
			    });
	 			//for each SrcDestItemInfo ... only last one must remain
	 			
			}
		}//end of try
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	private static void removeDupliSrcDest(ArrayList<Pair<SrcDestInfo, Integer>> thisList, ArrayList<MiscInner.PairIntBool> refList) {
		int prevSrcDestId = Misc.getUndefInt();
		MiscInner.PairIntBool prevRefEntry = null;
		for (int i=thisList.size()-1;i>=0;i--) {
			Pair<SrcDestInfo, Integer> pr = thisList.get(i);
			int currSrcDestId = pr.first.getId();
			if (currSrcDestId == prevSrcDestId) {
				if (prevRefEntry == null) {
					if (refList != null) {
						int idx = NewVehicleETA.getSrcDestIndexInIntegerList(refList, currSrcDestId);
						prevRefEntry = idx >= 0 ? refList.get(idx) : null;
					}
				}
				boolean doingForw = prevRefEntry == null || prevRefEntry.second;
				if (doingForw)
					thisList.remove(i);
				else
					thisList.remove(i+1);
			}
			else {
				prevSrcDestId = currSrcDestId;
				prevRefEntry = null;
			}
		}
	}
	private static MiscInner.Pair getIntermediateInfo(int intermediateId) {
		return intermediateToSrcDest.get(intermediateId);
	}
	private static int getIntermediateInfo(int srcDestId, int intermediateIndex) {
		Integer iv = intermediatePairToId.get(getComboKey(srcDestId, intermediateIndex));
		return iv == null ? Misc.getUndefInt() : iv.intValue();
	}
	private static void removeIntermediateInfo(int srcDestId, int intermediateIndex) {
		intermediatePairToId.remove(getComboKey(srcDestId, intermediateIndex));
	}
	
	private static RTree intermediateRTree = null;
	private static int glbSrcInterPairToId = -1;
	private static ConcurrentHashMap<Integer, MiscInner.Pair> intermediateToSrcDest = new ConcurrentHashMap<Integer, MiscInner.Pair>();
	private static ConcurrentHashMap<String, Integer> intermediatePairToId = new ConcurrentHashMap<String, Integer>();
	public static void reinitAllRtree() {
		intermediateToSrcDest.clear();
		intermediateToSrcDest.clear();
		glbSrcInterPairToId = 0;
		intermediateRTree = null;
	}
	private static String getComboKey(int srcDestId, int intermediateIndex) {
		return srcDestId+"_"+intermediateIndex;
	}
	private static int addAndGetIntermediateIndex(int srcDestId, int intermediateIndex) {
		int nextIndex = 0;
		synchronized(intermediateToSrcDest) {
			nextIndex = ++(SrcDestHelper.glbSrcInterPairToId);
		}
		intermediateToSrcDest.put(nextIndex, new MiscInner.Pair(srcDestId, intermediateIndex));
		intermediatePairToId.put(getComboKey(srcDestId, intermediateIndex), nextIndex);
		return nextIndex;
	}
	
	static {
		getIntermediateRTree();
	}
	private static RTree getIntermediateRTree() {
		if (intermediateRTree == null) {
			Properties properties = new Properties();
			properties.put("MinNodeEntries", 5);
			properties.put("MaxNodeEntries", 10);
			RTree rtree = new RTree();
			rtree.init(properties);
			intermediateRTree = rtree;
		}
		return intermediateRTree;
	}
	public static MiscInner.PairDouble getLonLat(Connection conn, int opstationId, int regionId)  {
		double lon = Misc.getUndefDouble();
		double lat = Misc.getUndefDouble();
		if (!Misc.isUndef(opstationId)) {
			OpStationBean opbean = TripInfoCacheHelper.getOpStation(opstationId);
			if (opbean != null)
				regionId = opbean.getGateAreaId();
		}
		RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regionId, conn);
		if (rth != null) {
			lon = rth.region.getCenter().getLongitude();
			lat = rth.region.getCenter().getLatitude();
		}
		return new MiscInner.PairDouble(lon, lat);
	}
	private static Region specializedGetRegion(Connection conn,  int opstationId, int regionId, double lon, double lat, double buffer) {
		Region toAdd = null;
		
		if (!Misc.isUndef(opstationId)) {
			OpStationBean opBean = TripInfoCacheHelper.getOpStation(opstationId);
			if (opBean != null) {
				int regId = opBean.getGateAreaId();
				RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regId, conn);
				if (rth != null) {
					toAdd = rth.region;
				}
			}
		}
		else if (!Misc.isUndef(regionId)) {
			RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regionId, conn);
			if (rth != null) {
				toAdd = rth.region;
			}
		}
		else if (!Misc.isUndef(lon) && !Misc.isUndef(lat) && !Misc.isUndef(buffer)) {
			toAdd = com.ipssi.geometry.Region.getLongLatBoxAround(new Point(lon, lat), buffer/1000.0);
		}
		return toAdd;
	}
	
	
	private static void addRtreeForIntermediate(Connection conn, int srcDestId, int intermediateIndex, int opstationId, int regionId, double lon, double lat, double buffer)  {
		try {
			RTree rtree = getIntermediateRTree();
			int intermediateId = addAndGetIntermediateIndex(srcDestId, intermediateIndex);
			Region toAdd = specializedGetRegion(conn, opstationId, regionId, lon, lat, buffer*1000);
			
			
			if (toAdd != null) {
				Rectangle rectangle = new Rectangle();
				 rectangle.set((float)toAdd.m_llCoord.getX(), (float)toAdd.m_llCoord.getY(), (float)toAdd.m_urCoord.getX(), (float)toAdd.m_urCoord.getY()); 
				 
				 synchronized (rtree) {
					 rtree.add(rectangle, intermediateId);
				 }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}

	private static void removeRtreeForIntermediate(Connection conn, int srcDestId, int intermediateIndex, int opstationId, int regionId, double lon, double lat, double buffer)  {
		try {
			RTree rtree = getIntermediateRTree();
			int intermediateId = SrcDestHelper.getIntermediateInfo(srcDestId, intermediateIndex);
			removeIntermediateInfo(srcDestId, intermediateIndex);
			Region toAdd = specializedGetRegion(conn, opstationId, regionId, lon, lat, buffer);
			
			
			if (toAdd != null) {
				Rectangle rectangle = new Rectangle();
				 rectangle.set((float)toAdd.m_llCoord.getX(), (float)toAdd.m_llCoord.getY(), (float)toAdd.m_urCoord.getX(), (float)toAdd.m_urCoord.getY()); 
				 
				 synchronized (rtree) {
					 rtree.delete(rectangle, intermediateId);
				 }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static class DifferenceResult {
		public boolean srcDiff = false;
		public boolean destDiff = false;
		public boolean isSame = false;
		public ArrayList<Integer> oldToNewIntermediateMapping = new ArrayList<Integer>();
	}
	
	public static DifferenceResult getDifference(SrcDestInfo old, SrcDestInfo newItem) {
		if (old == null || newItem == null)
			return null;
		DifferenceResult retval = new DifferenceResult();
		retval.srcDiff =  !Misc.isEqual(old.getSrcLong(), newItem.getSrcLong()) || !Misc.isEqual(old.getSrcLat(), newItem.getSrcLat());
		retval.destDiff =  !Misc.isEqual(old.getDestLong(), newItem.getDestLong()) || !Misc.isEqual(old.getDestLat(), newItem.getDestLat());
		ArrayList<SrcDestInfo.WayPoint> oldWPList = old.getWaypoints();
		ArrayList<SrcDestInfo.WayPoint> newWPList = newItem.getWaypoints();
		boolean seenWPIndexDiff = false;
		for (int i=0,is=oldWPList == null ? 0 : oldWPList.size(); i<is; i++) {
			SrcDestInfo.WayPoint oldwp = oldWPList.get(i);
			int idx = -1;
			for (int j=0,js=newWPList == null ? 0 : newWPList.size(); j<js; j++) {
				SrcDestInfo.WayPoint newwp = newWPList.get(j);
				if (Misc.isEqual(newwp.getLongitude(), oldwp.getLongitude()) && Misc.isEqual(newwp.getLatitude(), oldwp.getLatitude())) {
					idx = i;
					break;
				}
			}
			if (idx != i)
				seenWPIndexDiff = true;
			retval.oldToNewIntermediateMapping.add(idx);
		}
		if (!retval.srcDiff && !retval.destDiff && !seenWPIndexDiff && ((newWPList == null && oldWPList == null) || (newWPList.size() == oldWPList.size()))) {
			retval.isSame = true;
		}
		return retval;
	}
	public static void main(String[] args) throws Exception {
		Tester.callMain(args);
	}
}

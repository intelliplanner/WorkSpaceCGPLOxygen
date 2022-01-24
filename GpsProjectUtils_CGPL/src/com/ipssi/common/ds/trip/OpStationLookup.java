package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;

public class OpStationLookup {
	private ArrayList<Integer> fixedOpstationList = new ArrayList<Integer>();
	private ArrayList<Integer> movingOpstationList =  null;
	private volatile HashMap<Integer, Integer> waitRegToOpIdLookup = null; //lazy evaluation
	public synchronized void addStation(int id, Connection conn, boolean dontOptimize, boolean ignoreSize) throws Exception {
		OpStationBean bean = TripInfoCacheHelper.getOpStation(id);
		if (bean == null)
			return;
		if (!Misc.isUndef(bean.getLinkedVehicleId())) {
			if (movingOpstationList == null)
				movingOpstationList = new ArrayList<Integer>();
			movingOpstationList.add(bean.getOpStationId());
		}
		else {
			fixedOpstationList.add(bean.getOpStationId());
			markWaitToOpIdLookupDirty();
		}
	}
	
	public boolean contains(int opId) {
		for (Integer i:fixedOpstationList)
			if (opId == i)
				return true;
		if (movingOpstationList != null) {
			for (Integer i:movingOpstationList)
				if (opId == i)
					return true;
		}
		return false;
	}
	public void getOpListIgnoreBelonging(Connection conn, ArrayList<OpStationBean> retval,  int opstationType, int fixedOrMoving) throws Exception {
		//fixedOrMvoing = 0 => fixed, 1 => moving, 2 = both
		//nonFast + intersection of list fastX, fastY
		if (fixedOrMoving != 1) {
			for (int i=0,is=fixedOpstationList == null ? 0 : fixedOpstationList.size(); i<is;i++) {
				OpStationBean bean = TripInfoCacheHelper.getOpStation(fixedOpstationList.get(i));
				if (bean != null)
					retval.add(bean);
			}
		}
		if (fixedOrMoving != 0) {
			for (int i=0,is=movingOpstationList == null ? 0 : movingOpstationList.size(); i<is;i++) {
				OpStationBean bean = TripInfoCacheHelper.getOpStation(movingOpstationList.get(i));
				if (bean != null)
					retval.add(bean);
			}
		}//if there are moving stuff to be done
	}
	
	public void getOpList(Connection conn, ArrayList<OpStationBean> retval,  int opstationType, GpsData data, boolean ignoreSize, ThreadContextCache cache, int fixedOrMoving, int ownerOrgId, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws Exception {
		//fixedOrMvoing = 0 => fixed, 1 => moving, 2 = both
		//nonFast + intersection of list fastX, fastY
		if (fixedOrMoving ==0 || fixedOrMoving == 2) {
			if (data != null) {
				ArrayList<RegionTestHelper> containingRegion = cache.getRegionsContaining(data.getPoint());
				addOpStationFromRegions(retval, containingRegion, this.fixedOpstationList);;
			}
			else {
				if (fixedOrMoving == 0 || fixedOrMoving == 2) {
					for (Integer i: fixedOpstationList) {
						OpStationBean bean = TripInfoCacheHelper.getOpStation(i);
						if (bean != null)
							retval.add(bean);
					}
				}
			}
		}
		
		if ((fixedOrMoving == 1 || fixedOrMoving == 2) && movingOpstationList != null && movingOpstationList.size() != 0) {
			ArrayList<ThreadContextCache.SimpleMoving> movingList = cache.getMovingOpStationContaining(conn, ownerOrgId, data, vehicleControlling, vehSetup);
			this.addOpStationFromMoving(retval, movingList, movingOpstationList);
		}//if there are moving stuff to be done
	}

	private void markWaitToOpIdLookupDirty() {
		waitRegToOpIdLookup = null;
	}
	
	private  HashMap<Integer, Integer> getWaitOrRegToOpIdLookup() {
		if (waitRegToOpIdLookup != null)
			return waitRegToOpIdLookup;
		synchronized (this) {
			HashMap<Integer, Integer> retval = new HashMap<Integer, Integer>((int)(this.fixedOpstationList.size()*1.4), 0.75f);
			for (Integer i:fixedOpstationList) {
				OpStationBean bean = TripInfoCacheHelper.getOpStation(i);
				if (bean != null) {
					retval.put(bean.getWaitAreaId(), i);
				}
			}
			waitRegToOpIdLookup = retval;
		}
		return waitRegToOpIdLookup;
	}
	
	private void addOpStationFromMoving(ArrayList<OpStationBean> retval, ArrayList<ThreadContextCache.SimpleMoving> rtList, ArrayList<Integer> opList) {
		if (rtList == null || opList == null || rtList.size() == 0 || opList.size() == 0)
			return;
		HashMap<Integer, Integer> waitToOpId = getWaitOrRegToOpIdLookup();
		for (ThreadContextCache.SimpleMoving rt : rtList) {
			retval.add(rt.getOpstationBean());
		}
	}
	
	private void addOpStationFromRegions(ArrayList<OpStationBean> retval, ArrayList<RegionTestHelper> rtList, ArrayList<Integer> opList) {
		if (rtList == null || opList == null || rtList.size() == 0 || opList.size() == 0)
			return;
		HashMap<Integer, Integer> waitToOpId = getWaitOrRegToOpIdLookup();
		for (RegionTestHelper rt : rtList) {
			Integer opid = waitToOpId.get(rt.region.id);
			if (opid != null) {
				OpStationBean bean = TripInfoCacheHelper.getOpStation(opid);
				if (bean != null)
					retval.add(bean);
			}
		}
	}
	/* Valid but before RTree based implementation
	public static class CenterPlusOpId implements Comparable<CenterPlusOpId> {
		public double pos;
		public ArrayList<Integer> opId = null;;
		public CenterPlusOpId(double pos, int opId) {
			this.pos = pos;
			if (!Misc.isUndef(opId)) {
				this.opId = new ArrayList<Integer>();
				this.opId.add(opId);
			}
		}
		public void setPos(double pos) {
			this.pos = pos;
		}
		public ArrayList<Integer> getOpId() {
			if (opId == null)
				opId = new ArrayList<Integer>();
			return opId;
		}
		public void addOpId(int opId) {
			if (!Misc.isUndef(opId)) {
				this.opId = new ArrayList<Integer>();
				this.opId.add(opId);
			}
		}
		
		public int compareTo(CenterPlusOpId rhs) {
			boolean eq = Misc.isEqual(pos, rhs.pos);
			if (eq)
				return 0;
			return pos < rhs.pos ? -1 : 1;
		}
	}
	private ArrayList<Integer> nonFastList = new ArrayList<Integer>();
	private ArrayList<Integer> fastList = null;//new ArrayList<Integer>();
	private FastList<CenterPlusOpId> fastX = null; //new FastList<CenterPlusOpId>();
	private FastList<CenterPlusOpId> fastY = null; //new FastList<CenterPlusOpId>();
	 
	private double maxXsz = -1;
	private double maxYsz = -1;
	private FastList<Integer> tempOpList = new FastList<Integer>(); //for helping with calc
	private static final int g_threshForFastLookup = 10;
	
	public void makeFastListDirty() {
		fastX = null;
		fastY = null;
		maxXsz = -1;
		maxYsz = -1;
	}
	
	public void addStation(int id, Connection conn, boolean dontOptimize, boolean ignoreSize) throws Exception {
		OpStationBean bean = TripInfoCache.getOpStation(id);
		if (bean == null)
			return;
		if (dontOptimize) {
			this.nonFastList.add(id);
			return;
		}
		RegionTest.RegionTestHelper rh = this.helperGetRegionIfFastAndPrep(conn, bean, ignoreSize);
		if (rh != null) {
			this.helperAddFast(rh, bean);
		}
		else {
			this.nonFastList.add(id);
		}
	}
	
	public boolean contains(int opId) {
		for (Integer i:nonFastList)
			if (opId == i)
				return true;
		if (fastList != null) {
			for (Integer i:fastList)
				if (opId == i)
					return true;
		}
		return false;
	}

	public void getOpList(Connection conn, ArrayList<OpStationBean> retval, GpsData data, boolean ignoreSize) throws Exception {
		//nonFast + intersection of list fastX, fastY
		for (int i=0, is = nonFastList.size();i<is;i++) {
			OpStationBean bean = TripInfoCache.getOpStation(nonFastList.get(i));
			if (bean != null) {
				retval.add(bean);
			}
		}
		if (data == null || fastList == null || fastList.size() <= g_threshForFastLookup) {
			for (int i=0, is = fastList == null ? 0 : fastList.size();i<is;i++) {
				OpStationBean bean = TripInfoCache.getOpStation(fastList.get(i));
				if (bean != null) {
					retval.add(bean);
				}
			}	
			return;
		}
		if (fastX == null || fastY == null) {
			helperRepopulateFastList(conn, ignoreSize);
		}
		if (fastX != null && fastY != null) {
			CenterPlusOpId lookup = new CenterPlusOpId(0, Misc.getUndefInt());
			Pair<Integer, Boolean> index = null;
			double x = data.getLongitude();
			double y = data.getLatitude();
			tempOpList.clear();
			lookup.setPos(x-this.maxXsz);
			index = fastX.indexOf(lookup);
			int lo = index.second ? index.first : index.first+1;
			lookup.setPos(x+this.maxXsz);
			index = fastX.indexOf(lookup);
			int hi = index.first;
			for (int i=lo;i<=hi;i++) {
				CenterPlusOpId entry = fastX.get(i);
				if (entry == null)
					continue;
				for (Integer opId : entry.getOpId()) {
					tempOpList.add(opId);
				}
			}
			//now get y and if OpId exists in fastlist then add
			lookup.setPos(y-this.maxYsz);
			index = fastY.indexOf(lookup);
			lo = index.second ? index.first : index.first+1;
			lookup.setPos(y+this.maxYsz);
			index = fastY.indexOf(lookup);
			hi = index.first;
			for (int i=lo;i<=hi;i++) {
				CenterPlusOpId entry = fastY.get(i);
				if (entry == null)
					continue;
				for (Integer opId : entry.getOpId()) {
					index = tempOpList.indexOf(opId);
					if (index.second) {
						OpStationBean bean = TripInfoCache.getOpStation(opId);
						if (bean != null)
							retval.add(bean);
					}
				}
			}
		}
	}
	
	private RegionTest.RegionTestHelper helperGetRegionIfFastAndPrep(Connection conn, OpStationBean bean, boolean ignoreSize) throws Exception {
		if (Misc.isUndef(bean.getLinkedVehicleId())) {
			RegionTest.RegionTestHelper rh = RegionTest.getRegionInfo(bean.getWaitAreaId(), conn);
			if (rh != null && rh.region != null) {
				if (rh.region.m_isMBRSameAsRegion) {
					
					double xsz = rh.region.m_urCoord.getX() - rh.region.m_llCoord.getX();
					double ysz = rh.region.m_urCoord.getY() - rh.region.m_llCoord.getY();
					if (ignoreSize || (xsz < 0.0013 && ysz < 0.0011)) { //the consts is max() for DB-* names roughly 120 mt in sz
						if (xsz > maxXsz)
							maxXsz = xsz;
						if (ysz > maxYsz)
							maxYsz = ysz;
						return rh;
					}
				}
			}
		}
		return null;
	}
	
	private void helperAddFast(RegionTest.RegionTestHelper rh, OpStationBean bean) {
		int id = bean.getOpStationId();
		if (fastList == null)
			fastList = new ArrayList<Integer>();
		fastList.add(id);
		if (fastX == null)
			fastX = new FastList<CenterPlusOpId>();
		if (fastY == null)
			fastY = new FastList<CenterPlusOpId>();
		CenterPlusOpId t = new CenterPlusOpId(rh.region.getCenter().getX(), Misc.getUndefInt());
		Pair<Integer,Boolean> index = fastX.indexOf(t);
		if (index.second) {
			fastX.get(index.first).addOpId(id);
		}
		else {
			t.addOpId(id);
			fastX.add(t);
		}
		t = new CenterPlusOpId(rh.region.getCenter().getY(), Misc.getUndefInt());
		index = fastY.indexOf(t);
		if (index.second) {
			fastY.get(index.first).addOpId(id);
		}
		else {
			t.addOpId(id);
			fastY.add(t);
		}
	}
	
	private void helperRepopulateFastList(Connection conn, boolean ignoreSize) throws Exception{
		if (fastList == null)
			return;
		makeFastListDirty();
		for (Integer id:fastList) {
			OpStationBean bean = TripInfoCache.getOpStation(id);
			if (bean == null)
				continue;
			RegionTest.RegionTestHelper rh = this.helperGetRegionIfFastAndPrep(conn, bean, ignoreSize);
			helperAddFast(rh, bean);
		}
	}
	*/
}

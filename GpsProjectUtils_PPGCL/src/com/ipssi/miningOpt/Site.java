package com.ipssi.miningOpt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.geometry.Point;
import com.ipssi.miningOpt.Predictor.SimulInfo;
import com.ipssi.tripcommon.ExtLUInfoExtract;

public class Site {
	  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	  private NewMU ownerMU = null;
	  
	   private int id;
	   private String name;
	   private int status;
	   private double difficulty;
	   private double lon;
	   private double lat;
	   private double lowRadius;
	   private RegionTest.RegionTestHelper region;
	   private double minQty;
	   private double maxQty;
	   private int materialId;
	   private int pitId;
	   private long startFrom;
	   private long endTill;
	   
	   private ArrayList<Integer> notAllowedDumperTypes = new ArrayList<Integer>();
		private ArrayList<Integer> notAllowedLoaderTypes = new ArrayList<Integer>();

		private WaitQueue queue = new WaitQueue(10);
		private WaitQueue predictedQueue = new WaitQueue(10);
		private WaitQueue statsQueue = new WaitQueue(5);
		private NewEventDismissMgmt eventDismissMgmt = null;
		private int totTripsInShift;
		private double totTonnesInShift;
		private ArrayList<Integer> assignedDumpers = new ArrayList<Integer>();
		private long latestProcessedAt = -1;
		public boolean isAtBegOfQ(int vehicleId) {
			return queue.isAtBegOfQ(vehicleId);
		}
		public int getNumDumpersAssigned(Connection conn) throws Exception {
			int tot = 0;
			try {
				this.getReadLock();
				tot += this.assignedDumpers.size();
			}
			catch (Exception e) {
				
			}
			finally {
				this.releaseReadLock();
			}
			return tot;
		}
		
		public void removeFromAllQueue(int vehicleId , boolean removeFromStat) {
			queue.remove(vehicleId);
			if (this.predictedQueue != null)
				this.predictedQueue.remove(vehicleId);
			if (removeFromStat)
				statsQueue.removeLast(vehicleId);
		}
		
		public static void updatePredictedBothEnd(Site loadSite, Site unloadSite, ExtLUInfoExtract lext, ExtLUInfoExtract uext, long grt, DumperInfo dumper) {
			long simulGRT = dumper.predicted == null ? -1 : dumper.predicted.simulGRT;
			if (simulGRT <= 0)
				return;
			boolean inEventCloseBy = Predictor.reachedTargetTS(dumper.getLastLUEventTime(), simulGRT);
			if (inEventCloseBy) {
				boolean isTripLoadLike = dumper.isTripStatusLikeLoad(dumper.getCurrentLoadStatus());
				boolean isCompleted = dumper.isOpCompleted(dumper.getCurrentLoadStatus());
				if (loadSite != null && (!isTripLoadLike || isCompleted))
					loadSite.predictedQueue.remove(dumper.getId());
				if (unloadSite != null && (isTripLoadLike || isCompleted))
					unloadSite.predictedQueue.remove(dumper.getId());
				if (loadSite != null && isTripLoadLike && !isCompleted)
					loadSite.addToPredicted(lext, dumper.getId());
				if (unloadSite != null && !isTripLoadLike && !isCompleted)
					unloadSite.addToPredicted(uext, dumper.getId());
			}
		}
		
		public void addToPredicted(ExtLUInfoExtract ext, int vehicleId) {//returns true if there were additions made to this Q
			WaitItem item = predictedQueue.getByVehicleId(vehicleId);
			if (item == null) {
				item = new WaitItem(vehicleId, ext);
				predictedQueue.add(item);
			}
			else {
				item.update(ext);
			}
		}
		
		public void addToPredicted(SimulInfo ext, int vehicleId) {//returns true if there were additions made to this Q
			WaitItem item = predictedQueue.getByVehicleId(vehicleId);
			if (item == null) {
				item = new WaitItem(vehicleId ,ext);
				predictedQueue.add(item);
			}
			else {
				item.update(ext);
			}
		}
		public double dist(double lon, double lat) {
			com.ipssi.geometry.Point center = this.region != null ? this.region.region.getCenter() : null;
			if (center == null)
				center = new Point(this.getLon(), this.getLat());
			return center.fastGeoDistance(lon, lat);
		}
		public void toString(StringBuilder sb, boolean doAllProp) {
			Helper.putDBGProp(sb, "id", id);
			Helper.putDBGProp(sb, "name", name);
			if (doAllProp) {
				Helper.putDBGProp(sb, "status", status);
				Helper.putDBGProp(sb, "difficulty", difficulty);
				Helper.putDBGProp(sb, "lon", lon);
				Helper.putDBGProp(sb, "lat", lat);
				Helper.putDBGProp(sb, "radius", lowRadius);
				Helper.putDBGProp(sb,"region_id", region == null ? Misc.getUndefInt() : region.region.id);
				Helper.putDBGProp(sb, "min_qty", minQty);
				Helper.putDBGProp(sb, "max_qty", maxQty);
				Helper.putDBGProp(sb, "mat", materialId);
				Helper.putDBGProp(sb, "pit", pitId);
				Helper.putDBGProp(sb, "start_from", startFrom);
				Helper.putDBGProp(sb, "end_till", endTill);
				
				Helper.putDBGProp(sb, "not_allowed_dumper_types", notAllowedDumperTypes);
				Helper.putDBGProp(sb, "not_allowed_shovel_types", notAllowedLoaderTypes);
			}
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			toString(sb, true);
			return sb.toString();
		}
		public Site(int id, NewMU ownerMU) {
			this.id = id;
			this.ownerMU = ownerMU;
			
		}
	   public boolean equals(Site s) {
		   return s != null && s.id == this.id;
	   }
	   public boolean isDateValid(long ts) {
		   return (this.startFrom <= 0 || ts >= startFrom) && (this.endTill <= 0 || ts < this.endTill);
	   }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getStartFrom() {
		return startFrom;
	}
	public void setStartFrom(long startFrom) {
		this.startFrom = startFrom;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public double getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(double difficulty) {
		this.difficulty = difficulty;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLowRadius() {
		return lowRadius;
	}
	public void setLowRadius(double lowRadius) {
		this.lowRadius = lowRadius;
	}
	public RegionTest.RegionTestHelper getRegion() {
		return region;
	}
	public void setRegion(RegionTest.RegionTestHelper region) {
		this.region = region;
	}
	public double getMinQty() {
		return minQty;
	}
	public void setMinQty(double minQty) {
		this.minQty = minQty;
	}
	public double getMaxQty() {
		return maxQty;
	}
	public void setMaxQty(double maxQty) {
		this.maxQty = maxQty;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public int getPitId() {
		return pitId;
	}
	public void setPitId(int pitId) {
		this.pitId = pitId;
	}
	public ArrayList<Integer> getNotAllowedDumperTypes() {
		return notAllowedDumperTypes;
	}
	public void setNotAllowedDumperTypes(ArrayList<Integer> notAllowedDumperTypes) {
		this.notAllowedDumperTypes = notAllowedDumperTypes;
	}
	public ArrayList<Integer> getNotAllowedLoaderTypes() {
		return notAllowedLoaderTypes;
	}
	public void setNotAllowedLoaderTypes(ArrayList<Integer> notAllowedLoaderTypes) {
		this.notAllowedLoaderTypes = notAllowedLoaderTypes;
	}
	public long getEndTill() {
		return endTill;
	}
	public void setEndTill(long endTill) {
		this.endTill = endTill;
	}
	public WaitQueue getQueue() {
		return queue;
	}
	
	public WaitQueue getStatsQueue() {
		return statsQueue;
	}
	
	
	public void getReadLock() {
		lock.readLock().lock();
	}
	public void releaseReadLock() {
		lock.readLock().unlock();
	}
	public void getWriteLock() {
		lock.writeLock().lock();
	}
	public void releaseWriteLock() {
		lock.writeLock().unlock();
	}


	public NewMU getOwnerMU() {
		return ownerMU;
	}


	public void setOwnerMU(NewMU ownerMU) {
		this.ownerMU = ownerMU;
	}


	public int getTotTripsInShift() {
		return totTripsInShift;
	}


	public void setTotTripsInShift(int totTripsInShift) {
		this.totTripsInShift = totTripsInShift;
	}


	public double getTotTonnesInShift() {
		return totTonnesInShift;
	}


	public void setTotTonnesInShift(double totTonnesInShift) {
		this.totTonnesInShift = totTonnesInShift;
	}


	public NewEventDismissMgmt getEventDismissMgmt(Connection conn) {
		if (eventDismissMgmt == null) {
			this.eventDismissMgmt = NewEventDismissMgmt.create(conn, 1, id);
		}
		return eventDismissMgmt;
	}
	public void addDumperIfNotExist(int val) {
		try {
			getWriteLock();
			for (int i=0,is=this.assignedDumpers == null ? 0 : this.assignedDumpers.size(); i<is; i++)
				if (val == assignedDumpers.get(i).intValue())
					return;
			assignedDumpers.add(val);
		}
		catch (Exception e2) {
			
		}
		finally {
			releaseWriteLock();
		}
	}
	
	public boolean removeDumperVal(int val) {
		try {
			getWriteLock();
		
			for (int i=0,is=this.assignedDumpers == null ? 0 : assignedDumpers.size(); i<is; i++)
				if (val == assignedDumpers.get(i).intValue()) {
					assignedDumpers.remove(i);
					return true;
				}
			return false;
		}
		catch (Exception e) {
			
		}
		finally {
			releaseWriteLock();
		}
		return false;
	}

	public ArrayList<Integer> getAssignedDumpers() {
		return assignedDumpers;
	}

	public void setAssignedDumpers(ArrayList<Integer> assignedDumpers) {
		this.assignedDumpers = assignedDumpers;
	}
	public long getLatestProcessedAt() {
		return latestProcessedAt;
	}
	public void setLatestProcessedAt(long latestProcessedAt) {
		this.latestProcessedAt = latestProcessedAt;
	}
	public WaitQueue getPredictedQueue() {
		return predictedQueue;
	}
	public void setPredictedQueue(WaitQueue predictedQueue) {
		this.predictedQueue = predictedQueue;
	}
}

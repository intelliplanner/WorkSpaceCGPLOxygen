package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;



import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.LatestTripInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.miningOpt.DynOptimizer.ResultSingleVehicle;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.ExtLUInfoExtract;

public class DumperInfo extends CoreVehicleInfo {
	public final static int L_WAIT = 0;
	public final static int L_BEING_OP = 1;
	public final static int L_WAIT_AFT = 2;
	public final static int L_ENROUTE = 3;
	public final static int U_WAIT = 4;
	public final static int U_BEING_OP = 5;
	public final static int U_WAIT_AFT = 6;
	public final static int U_ENROUTE = 7;
	
	public final static int NORMAL_OP = 0;
	public final static int DIVERTED_TO_LU = 1;
	public final static int DIVERTED_TO_REST = 2;
	public final static int IN_BD = 3;
	public final static int IN_REST = 4;
	public final static int SHOULD_BE_AT_TARGET = 5;
	public final static int OVERSHOT_TARGET = 6;

	private double assignedPlusUsageRatePerKM = Misc.getUndefDouble();
	private double avgFuelConsumptionRate = Misc.getUndefDouble();
	private double avgOpSpeedPerKM = Misc.getUndefDouble(); //mapped to cycle_time_sec param
	private double avgUnloadTimeSec = 90; //double_field1;
	
	private int assignedRoute = Misc.getUndefInt();
	private int estimatedRoute = Misc.getUndefInt();
	private int currentLoadStatus = Misc.getUndefInt();	
	private int currentAddnlOpStatus = Misc.getUndefInt();
	
	private double distTravelledSinceLastOp;
	
	private double distMarkerAtLastOp;
	private double distMarkerAtPrevToLastOp;
	private double totDistToTargetOp;
	private double estDistToTargetOp;
	
	private long lastLUEventTime;
	
	private int numberTripsSinceReset;
	private double loadKMSinceReset;
	private double distMarkerAtReset;
	
	public long latestGRT;
	public static class Predicted {
		public long simulGRT;
		public int currentLoadStatus;
		public double frac;
		public long entryAt;
		public int processWaitSec;
		public int deltaTrips;
	}
	public Predicted predicted = new Predicted();
	private int optimizeSrcSiteId = -1;
	private int optimizeDestSiteId = -1;
	private int optimizeForDumperId = -1;
	private boolean optimizeRecommended = false;
	private long dynOptimizerRunAt = -1;
	private long lastPredictionRunAt = -1;
	private double prevTripLeadAvg; //currently as previous
	private double prevLoadLon = Misc.getUndefDouble(); //not yet populated
	private double prevLoadLat = Misc.getUndefDouble(); //not yet populated
	public void setLatestGRT(long grt) {
		if (grt > latestGRT) {
			latestGRT = grt;
		}
		if (predicted == null)
			predicted = new Predicted();
		if (false) {
			if (latestGRT > predicted.simulGRT || predicted.simulGRT <= 0) {
				predicted.simulGRT = grt;
				predicted.currentLoadStatus = this.currentLoadStatus;
				predicted.deltaTrips = 0;
				predicted.entryAt = !this.isOpCompleted(currentLoadStatus) ? this.lastLUEventTime : -1;
				predicted.processWaitSec = 0;
				predicted.frac = this.getPercentLegCompleted();
			}
		}
	}
	
	
	protected void onUpdateNewData(Connection conn, NewMU newmu, GpsData gpsData) throws Exception {
		boolean toTrace = true;//DEBUG13
		if (gpsData != null && gpsData.getDimId() == 0) {
			this.getOwnerMU().setSimulationNow(gpsData.getGps_Record_Time());
			this.setLatestGRT(gpsData.getGps_Record_Time());
			boolean hasExitted = this.isOpCompleted(this.currentLoadStatus);
			if (hasExitted) {
				this.distTravelledSinceLastOp = (gpsData.getValue() - this.distMarkerAtLastOp)*1.05;
				System.out.println("[MDB_PT_UPD] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" DistTravelled"+distTravelledSinceLastOp);
		
			}
		}
	}
	protected  void onUpdateTripChange(Connection conn, NewMU newmu, LatestTripInfo latestTrip, boolean isInRest,  boolean doOptimizer, VehicleDataInfo vdf) throws Exception {
		this.setInRest(isInRest);
		boolean toTrace = true;//this.getId() == 41453;
		
		if (latestTrip == null ||(latestTrip.getLoad() == null && latestTrip.getUnload() == null))
			return;
		ExtLUInfoExtract lext = (ExtLUInfoExtract) latestTrip.getLoad();
		ExtLUInfoExtract uext = (ExtLUInfoExtract) latestTrip.getUnload();
		ExtLUInfoExtract.MiningInfo miningInfo = uext != null ? uext.getMiningInfo()  : lext.getMiningInfo();
		System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" lext"+lext);
		System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" uext"+uext);
		System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" Mining:"+miningInfo);

		Route oldRoute = newmu.getRouteInfo(this.assignedRoute);
		int oldSrcSiteId = oldRoute != null ? oldRoute.getLoadSite() : Misc.getUndefInt();
		int oldDestSiteId = oldRoute != null ? oldRoute.getUnloadSite() : Misc.getUndefInt();
		LoadSite oldLoadSite = newmu.getLoadSite(conn, oldSrcSiteId);
		UnloadSite  oldUnloadSite = newmu.getUnloadSite(conn, oldDestSiteId);
		int oldLoadStatus = this.currentLoadStatus;
		int oldAddnlStaus = this.currentAddnlOpStatus;
		int oldAssignment = this.getAssignmentStatus();
		if (toTrace) {
			System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" OldSrc:"+oldSrcSiteId+" OldDest:"+oldDestSiteId+" Old Load:"+oldLoadStatus+" OldAss:"+oldAssignment);
		}
		
		int srcSiteId = miningInfo == null ? Misc.getUndefInt() : miningInfo.getSiteId();
		if (Misc.isUndef(srcSiteId))
			srcSiteId = -1;
		
		int destSiteId = miningInfo == null ? Misc.getUndefInt() : miningInfo.getDestId();
		if (Misc.isUndef(destSiteId))
			destSiteId = -2;
		
		Route newRoute = newmu.getRoute(conn, srcSiteId, destSiteId, true);
		int newLoadStatus = this.calcNewLoadStatus(lext, uext);
		this.setCurrentLoadStatus(newLoadStatus);
		LoadSite newLoadSite = newmu.getLoadSite(conn, srcSiteId);
		UnloadSite  newUnloadSite = newmu.getUnloadSite(conn, destSiteId);
		int newAssignment = CoreVehicleInfo.ASSIGNED;
		if (toTrace) {
			System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" NewSrc:"+srcSiteId+" NewDest:"+destSiteId+" New Load:"+newLoadStatus+" NewAss:"+newAssignment);
		}
		
		boolean changedSrc = oldSrcSiteId != srcSiteId && ( srcSiteId > 0 && destSiteId > 0);
		boolean changedDest = oldDestSiteId != destSiteId  && (srcSiteId > 0 && destSiteId > 0);
		
		boolean doUpdateAssignment = (newmu.parameters.autoUpdateAssignmentOnLoadChange && changedSrc) || (newmu.parameters.autoUpdateAssignmentOnUnloadChange && changedDest);
		
		boolean newLoadLikeLoad  = isTripStatusLikeLoad(newLoadStatus);
		boolean oldLoadLikeLoad  = isTripStatusLikeLoad(oldLoadStatus);
		NewVehicleData vdp = vdf.getDataList(conn, vdf.getVehicleId(), 0, true);
		GpsData latestGpsData = null;
		synchronized (vdf) {
			latestGpsData = vdp.getLast(conn);
		}
		if (changedSrc && oldLoadSite != null)
			oldLoadSite.removeFromAllQueue(this.getId(), newLoadLikeLoad == oldLoadLikeLoad);
		if (changedDest && oldUnloadSite != null)
			oldUnloadSite.removeFromAllQueue(this.getId(), newLoadLikeLoad == oldLoadLikeLoad);
		boolean movingFromLUOrUL = newLoadLikeLoad != oldLoadLikeLoad;
		if (movingFromLUOrUL) {
			this.distMarkerAtPrevToLastOp = distMarkerAtLastOp;
			if (toTrace) {
				System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" Remembering up distmarker for beg:"+distMarkerAtPrevToLastOp);
			}

		}
		else {
			
		}
	
		boolean hasOpCompleted = this.isOpCompleted(newLoadStatus);
		boolean hasExitted = this.isExittedOp(newLoadStatus, oldLoadStatus);
		boolean justCompleted = hasOpCompleted && (!isOpCompleted(oldLoadStatus) || oldLoadStatus < 0 || srcSiteId != oldSrcSiteId);
		if (toTrace) {
			System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" isOpCompleted:"+hasOpCompleted+" hasExitted:"+hasExitted+" justComp:"+justCompleted);
		}
	
		if (newLoadLikeLoad) { //inLoad			... lext will be not null
			WaitItem entry = newLoadSite == null ? null : newLoadSite.getQueue().getByVehicleId(getId());
			boolean noEntryInQ = entry == null;
			if (noEntryInQ) {
				entry = new WaitItem(getId(), lext);
			}
			else {
				entry.update(lext);
			}
			if (oldLoadStatus != newLoadStatus) {
				this.lastLUEventTime = lext.getLatestEventDateTime();
				if (toTrace) {
					System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Setting lastLUEventTime:"+Misc.longToUtilDate(this.lastLUEventTime)); 
				}

			}
			if (newLoadSite != null) {
				if (!hasOpCompleted) {
					if (noEntryInQ)
						newLoadSite.getQueue().add(entry);
				}
				else {
					if (!noEntryInQ)
						newLoadSite.getQueue().remove(getId());
					newLoadSite.getStatsQueue().add(entry);
				}
				
			}
			if (justCompleted && (newLoadSite.getLatestProcessedAt() <= 0 || newLoadSite.getLatestProcessedAt() < lext.getGateOut())) {
					newLoadSite.setLatestProcessedAt(lext.getGateOut());
					if (toTrace) {
						System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Setting last processed for site:"+newLoadSite +" to:"+Misc.longToUtilDate(lext.getGateOut())); 
					}
			}
			if (justCompleted || !hasOpCompleted) {
				GpsData gpsDataAtGin = null;
				synchronized (vdf) {
					gpsDataAtGin = lext.getGateIn() <= 0 && lext.getWaitIn() <= 0 ? null : vdp.get(conn, new GpsData(lext.getGateIn() > 0 ? lext.getGateIn() : lext.getWaitIn()));
				}
				if (gpsDataAtGin == null)
					gpsDataAtGin = latestGpsData;
				this.distMarkerAtLastOp = gpsDataAtGin == null ? 0 : gpsDataAtGin.getValue();
				if (toTrace) {
					System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Setting distMarker:"+this.distMarkerAtLastOp); 
				}
				
				this.totDistToTargetOp = newRoute == null ? Route.estDist(conn, srcSiteId, destSiteId).first : newRoute.getDistance();
				if (srcSiteId < 0 || destSiteId < 0)  {
					if (this.prevTripLeadAvg >= 0.0005)
						this.totDistToTargetOp = this.prevTripLeadAvg;
					else if (oldRoute != null)
						this.totDistToTargetOp = oldRoute.getDistance();
				}
				this.estDistToTargetOp = this.totDistToTargetOp;
			}
		}
		else  { //inUnload
			WaitItem entry = newUnloadSite == null ? null : newUnloadSite.getQueue().getByVehicleId(getId());
			boolean noEntryInQ = entry == null;
			if (noEntryInQ) {
				entry = new WaitItem(getId(), uext);
			}
			else {
				entry.update(uext);
			}
			if (oldLoadStatus != newLoadStatus)
				this.lastLUEventTime = uext == null ? -1 : uext.getLatestEventDateTime();
			if (newUnloadSite != null) {
				if (!hasOpCompleted) {
					if (noEntryInQ)
						newUnloadSite.getQueue().add(entry);
				}
				else {
					if (!noEntryInQ)
						newUnloadSite.getQueue().remove(getId());
					newUnloadSite.getStatsQueue().add(entry);
				}				
			}
			Site.updatePredictedBothEnd(newLoadSite, newUnloadSite, lext, uext, this.lastLUEventTime, this);
			if (justCompleted || !hasOpCompleted) {
				GpsData gpsDataAtGin = null;
				synchronized (vdf) {
					gpsDataAtGin = uext == null || (uext.getWaitIn() <=0 && uext.getGateIn() <= 0) ? null : vdp.get(conn, new GpsData(uext.getGateIn() > 0 ? uext.getGateIn() : uext.getWaitIn()));
				}
				if (gpsDataAtGin == null)
					gpsDataAtGin = latestGpsData;
				double prevMarker = this.distMarkerAtLastOp;
				this.distMarkerAtLastOp = gpsDataAtGin == null ? 0 : gpsDataAtGin.getValue();
				double deltaLoadLeg = 0;
				 if (movingFromLUOrUL) {
					deltaLoadLeg = this.distMarkerAtLastOp <=0.0005 || this.distMarkerAtPrevToLastOp <= 0.0005 || (this.distMarkerAtLastOp-this.distMarkerAtPrevToLastOp) <= 0.0005 ? 0 : this.distMarkerAtLastOp - this.distMarkerAtPrevToLastOp; 
				 }
				 else {
					 deltaLoadLeg =  this.distMarkerAtLastOp <=0.0005 || prevMarker <= 0.0005 || (this.distMarkerAtLastOp-prevMarker) <= 0.0005 ? 0 :  distMarkerAtLastOp - prevMarker; // -(prevMarker - this.distMarkerAtPrevToLastOp)+(distMarkerAtLastOp-distMarkerAtPrevToLastOp)
				 }
				double currEstOfDist = this.distMarkerAtLastOp <=0.0005 || this.distMarkerAtPrevToLastOp <= 0.0005 || (this.distMarkerAtLastOp-this.distMarkerAtPrevToLastOp) <= 0.0005 ? (newRoute == null ? 0 : newRoute.getDistance()) : (distMarkerAtLastOp - distMarkerAtPrevToLastOp)*1.05;
				
				this.prevTripLeadAvg = currEstOfDist;
				if (toTrace) {
					System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Setting updated on unload EstOfDist:"+currEstOfDist+" Delta for travel:"+deltaLoadLeg+" prevMarker:"+distMarkerAtPrevToLastOp+" OpMarker:"+this.distMarkerAtLastOp); 
				}
				
				 
				if (oldRoute != null && newRoute != null && oldRoute.getId() == newRoute.getId()) {
					if (currEstOfDist > 1.2 * newRoute.getDistance() || currEstOfDist < 0.8*newRoute.getDistance()) {
						if (toTrace) {
							System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" Setting Dist:"+newRoute+" newDist:"+currEstOfDist);
						}

						newRoute.setDistance(currEstOfDist);
					}
				}
				this.loadKMSinceReset += deltaLoadLeg;
				if (toTrace) {
					System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Tot dist travelled:"+this.loadKMSinceReset+" Delta:"+deltaLoadLeg); 
				}
				
				
				this.totDistToTargetOp = newRoute == null ? Route.estDist(conn, srcSiteId, destSiteId).first : newRoute.getDistance();
				if (srcSiteId < 0 || destSiteId < 0)  {
					if (this.prevTripLeadAvg >= 0.0005)
						this.totDistToTargetOp = this.prevTripLeadAvg;
					else if (oldRoute != null)
						this.totDistToTargetOp = oldRoute.getDistance();
				}
				this.estDistToTargetOp = this.totDistToTargetOp;
				//Now update info of loadSite and unloadSite + count of trips
				 if (movingFromLUOrUL) {
					 this.numberTripsSinceReset++;
					 if (newLoadSite != null) {
						 newLoadSite.setTotTripsInShift( newLoadSite.getTotTripsInShift()+1);
						 newLoadSite.setTotTonnesInShift(newLoadSite.getTotTonnesInShift()+1*this.getCapacityWt());
					 }
					 if (newUnloadSite != null) {
						 newUnloadSite.setTotTripsInShift( newUnloadSite.getTotTripsInShift()+1);
						 newUnloadSite.setTotTonnesInShift(newUnloadSite.getTotTonnesInShift()+1*this.getCapacityWt());
					 }
				 }
			}
		}
		if (toTrace) {
			System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" opMarker:"+this.distMarkerAtLastOp+" TotDist:"
					+totDistToTargetOp+" Dist Marker Prev:"+distMarkerAtPrevToLastOp);
		}
		
		if (doUpdateAssignment) {
			long ts = lext != null ? lext.getEarliestEventDateTime() : uext.getEarliestEventDateTime();
			newmu.setAssignment(conn, getId(), srcSiteId, destSiteId, ts,CoreVehicleInfo.ASSIGNED, false, 100);
			if (toTrace) {
				System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Setting New assignment"+this.getAssignedRoute()); 
			}

			try {
			DumperInfo.logMessage(conn, this.getId(), newLoadSite == null ? null : newLoadSite.getName(), newUnloadSite == null ? null : newUnloadSite.getName(), "Being auto reassigned because different est route from Src:"+(oldLoadSite == null ? "UNK" : oldLoadSite.getName())+" to Dest:"+(oldUnloadSite == null ? "UNK" : oldUnloadSite.getName()), ts, null,0);
			
			}
			catch (Exception e2) {
				
			}
		}
		this.estimatedRoute =  newRoute == null ? Misc.getUndefInt() : newRoute.getId();
		
		onUpdateNewData(conn, newmu, latestGpsData);
	//	this.setLatestGRT(latestGpsData.getGps_Record_Time()); called with onUpdateNew
		if (doOptimizer) {
		//Calcuate predicted
			if ((newmu.parameters.g_doPredictedQLenAtLExit || newmu.parameters.g_doOptimizationAtLExit) && newLoadLikeLoad && justCompleted && newLoadSite != null) {
				int leadSec = (int) (newRoute == null ? 7*60 : newRoute.getDistance()/this.getAvgOpSpeedPerKM()*3600);
				int unloadSec = (int) this.getAvgUnloadTimeSec();
				long meMax = this.latestGRT;
				double fracTravelled = this.getPercentLegCompleted();
				if (fracTravelled > 1)
					fracTravelled = 0.99;
				int timeRemainingTobeBack = (int) ((1-fracTravelled)*leadSec+unloadSec+leadSec);
				long targetTS = meMax+timeRemainingTobeBack*1000;//this.lastLUEventTime + (2 * leadSec+unloadSec)*1000;
				long maxGRT = newmu.getSimulationNow();
				if (targetTS > maxGRT) {
					if (toTrace) {
						System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  doing forward projection:"+" Latest LU:"+Misc.longToUtilDate(this.lastLUEventTime)+" target:"+Misc.longToUtilDate(targetTS)
								+ " maxG:"+Misc.longToUtilDate(maxGRT)+" meMax:"+Misc.longToUtilDate(latestGpsData.getGps_Record_Time())
								+" lExt:"+lext); 
					}
	
					Predictor predictor = new Predictor(newLoadSite);
					boolean doApproxForPrediction = true;
					predictor.bringSiteToTargetTS(maxGRT, doApproxForPrediction);
					predictor.writeBackPrediction();
					newLoadSite.setLastPredictionRunAt(System.currentTimeMillis());
					predictor.bringSiteToTargetTS(targetTS, doApproxForPrediction);
					int predictedQLen = predictor.getQLen(this.getId());
					newLoadSite.setPredictedQLenWhenLatestDumperOutComesBack(predictedQLen);
					if (predictedQLen > newmu.parameters.OPTIMIZE_Q_THRESHOLD) {
						try {
							if (toTrace) {
								System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Q len >2"+predictedQLen); 
							}
	
							DumperInfo.logMessage(conn, this.getId(), newLoadSite == null ? null : newLoadSite.getName(), newUnloadSite == null ? null : newUnloadSite.getName(), "Predicted Q Length exceed 2:"+predictedQLen, targetTS, null,2);
						}
						catch (Exception e2) {
							
						}
					}
					else {
						DumperInfo.logMessage(conn, this.getId(), newLoadSite == null ? null : newLoadSite.getName(), newUnloadSite == null ? null : newUnloadSite.getName(), "Rep Predicted Q Length :"+predictedQLen, targetTS, null,80);
					}
				}		
			}
			//TODO DEBUG13 move optimization in separate from core data processing etc.
			if ((newmu.parameters.g_doOptimizationAtLExit) && newLoadLikeLoad && justCompleted && newLoadSite != null) {
				boolean needsOptimization = true || newLoadSite.getPredictedQLenWhenLatestDumperOutComesBack() > newmu.parameters.OPTIMIZE_Q_THRESHOLD;
				if (needsOptimization) {
					if (toTrace) {
						System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+"  Running optimizer:"+newmu.parameters.OPTIMIZE_APPROACH); 
					}
	
					DynOptimizer optimizer = new DynOptimizer(conn, newmu, newmu.parameters.OPTIMIZE_APPROACH);
					ResultSingleVehicle result = optimizer.optimizeForSingleVehicle(this.getId());
					if (result != null) {
						int resSrcSiteId = result.srcSiteId;
						int resDestSiteId = result.destId;
						boolean todoOptimize = resSrcSiteId != srcSiteId || resDestSiteId != destSiteId;
						if (todoOptimize) {
							if (toTrace) {
								System.out.println("[MDB] Thread:"+Thread.currentThread().getName()+" Veh:"+this.getId()+" After Optimize Old Src:"+newLoadSite.getId()+"  New Site:"+resSrcSiteId+" Results:"+result); 
							}
	
							newLoadSite.setOptimizeSrcSiteId(resSrcSiteId);
							newLoadSite.setOptimizeDestSiteId(resDestSiteId);
							newLoadSite.setOptimizeRecommended(todoOptimize);
							newLoadSite.setOptimizeForDumperId(this.getId());
							newLoadSite.setDynOptimizerRunAt(newmu.getSimulationNow());
							this.setOptimizeSrcSiteId(resSrcSiteId);
							this.setOptimizeDestSiteId(resDestSiteId);
							this.setOptimizeRecommended(todoOptimize);
							this.setOptimizeForDumperId(this.getId());
							this.setDynOptimizerRunAt(newmu.getSimulationNow());
							Site resSite = newmu.getSiteInfo(resSrcSiteId);
							Site resDest = newmu.getSiteInfo(resDestSiteId);
							String resName = resSite == null ? null : resSite.getName();
							String destName = resDest == null ? null : resDest.getName();
							System.out.println("[Optimize] dumper:"+this.getName()+" Old Src:"+newLoadSite.getId()+"  New Site:"+resSrcSiteId);
							try {
							DumperInfo.logMessage(conn, this.getId(), resName, destName, "Optimization recommendation ..at exit from shovel of dumper ... divert ..", this.getLastLUEventTime(), result == null ? null : result.toString(),3);
							}
							catch (Exception e2) {
								
							}
	
						}
					}
				}
			}
		} //if doOptimizer
	}
	private void setDynOptimizerRunAt(long simulationNow) {
		// TODO Auto-generated method stub
		this.dynOptimizerRunAt = simulationNow;
	}
	public long  getDynOptimizerRunAt() {
		// TODO Auto-generated method stub
		return this.dynOptimizerRunAt;
	}

	public double getPercentLegByPredicted() {
		long mx = this.getOwnerMU().getSimulationNow();
		double currFrac = this.predicted == null ? this.getPercentLegCompleted() : this.predicted.frac;
		double speed = this.getAvgOpSpeedPerKM();
		double totLead = this.totDistToTargetOp;
		int gapSec = (int) ((mx-this.predicted.simulGRT)/1000);
		double deltaFrac = gapSec < 0 ? 0 : (speed*gapSec/3600.0)/(double)totLead;
		currFrac += deltaFrac;
		if (currFrac > 1)
			currFrac = 0.05;
		return currFrac;
	}
	
	public static boolean isTripStatusLikeLoad(int tripStatus) {
		return tripStatus == L_WAIT || tripStatus == L_BEING_OP || tripStatus == L_WAIT_AFT || tripStatus == L_ENROUTE;
	}
	
	public static boolean isOpCompleted(int tripStatus) {
		return tripStatus == L_WAIT_AFT || tripStatus == L_ENROUTE || tripStatus == U_WAIT_AFT || tripStatus == U_ENROUTE;
	}
	
	public static boolean isExittedOp(int tripStatus, int oldTripStatus) {
		return tripStatus ==L_ENROUTE || tripStatus == U_ENROUTE || (isTripStatusLikeLoad(oldTripStatus) != isTripStatusLikeLoad(tripStatus)) ;
	}
	public void toString(StringBuilder sb, boolean doAll) {
		if (doAll) {
			Helper.putDBGProp(sb, "load_status", currentLoadStatus);
			Helper.putDBGProp(sb, "perc_leg", getPercentLegCompleted());
			Helper.putDBGProp(sb, "assigned_route", assignedRoute);
			Helper.putDBGProp(sb, "est_route", estimatedRoute);
			Helper.putDBGProp(sb, "last_event", lastLUEventTime);
			Helper.putDBGProp(sb, "addnl_status", currentAddnlOpStatus);
			Helper.putDBGProp(sb, "dist_travel", distTravelledSinceLastOp);
		}
		super.toString(sb, doAll);
		if (doAll) {
			Helper.putDBGProp(sb, "assign_rate_perkm", assignedPlusUsageRatePerKM);
			Helper.putDBGProp(sb, "avg_fuel_km", avgFuelConsumptionRate);
			Helper.putDBGProp(sb, "avg_speed", avgOpSpeedPerKM);
			Helper.putDBGProp(sb, "num_trips", numberTripsSinceReset);
			Helper.putDBGProp(sb, "load_km", this.loadKMSinceReset);
			Helper.putDBGProp(sb, "distmarker_reset", distMarkerAtReset);
		//	Helper.putDBGProp(sb, "dist", distTravelledSinceLastOp);
			Helper.putDBGProp(sb, "dist_marker", distMarkerAtLastOp);
			Helper.putDBGProp(sb, "tot_dist_target", totDistToTargetOp);
			Helper.putDBGProp(sb, "rem_dist_target", estDistToTargetOp);
			
		}
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, true);
		return sb.toString();
	}
	public DumperInfo(int id, NewMU ownerMU) {
		super(id, ownerMU);
	}
	public void populateInfo(ResultSet rs) throws Exception {
		super.populateInfo(rs);
		this.avgFuelConsumptionRate = Misc.getRsetDouble(rs, "fuel_consumption_rate_km");
		this.assignedPlusUsageRatePerKM = Misc.getRsetDouble(rs, "assigned_rate_use_perkm"); 
		this.avgOpSpeedPerKM = Misc.getRsetDouble(rs, "cycle_time_second", 40);
		this.avgUnloadTimeSec = Misc.getRsetDouble(rs, "double_field1", 90);
		if (this.avgUnloadTimeSec <= 0.0005)
			this.avgUnloadTimeSec = 90;
	}
	
	public double getAssignedPlusUsageRatePerKM() {
		return assignedPlusUsageRatePerKM;
	}
	public void setAssignedPlusUsageRatePerKM(double assignedPlusUsageRatePerKM) {
		this.assignedPlusUsageRatePerKM = assignedPlusUsageRatePerKM;
	}
	public double getAvgFuelConsumptionRate() {
		return avgFuelConsumptionRate;
	}
	public void setAvgFuelConsumptionRate(double avgFuelConsumptionRate) {
		this.avgFuelConsumptionRate = avgFuelConsumptionRate;
	}
	public int getAssignedRoute() {
		return assignedRoute;
	}
	public void setAssignedRoute(int assignedRoute) {
		this.assignedRoute = assignedRoute;
	}
	public int getEstimatedRoute() {
		return estimatedRoute;
	}
	public void setEstimatedRoute(int estimatedRoute) {
		this.estimatedRoute = estimatedRoute;
	}
	public int getCurrentLoadStatusWithPredicted() {
		if (this.predictedUseful()) {
			return this.predicted.currentLoadStatus;
		}
		return this.currentLoadStatus;
	}
	public int getCurrentLoadStatus() {
		return currentLoadStatus;
	}
	public void setCurrentLoadStatus(int currentLoadStatus) {
		this.currentLoadStatus = currentLoadStatus;
	}
	public boolean predictedUseful() {
		return (getOwnerMU().parameters.g_usePredictedToShowCurrStuff && this.predicted != null && !Predictor.reachedTargetTS(this.latestGRT, predicted.simulGRT));
	}
	public double getPercentLegCompleted() {
		if (predictedUseful())
			return this.getPercentLegByPredicted();
		else
			return getPercentLegCompletedInt(false);
	}
	public double getPercentLegCompletedInt(boolean getAct) {
		double retval = 0;
		boolean hasExitted = this.isExittedOp(this.currentLoadStatus, this.currentLoadStatus);
		if (hasExitted) {
			double numer = this.distTravelledSinceLastOp;
			double denom = this.totDistToTargetOp;
//			if (numer > denom) {
//				numer = denom - this.estDistToTargetOp;
//			}
			retval = Misc.isEqual(denom, 0) ? 0 : numer/denom;
			if (!getAct && retval > 1.00)
				retval = 0.95;
			if (!getAct && retval < 0.01)
				retval = 0.05;
		}
		else {
			retval = 0;
		}
		return retval;
	}
	
	public int getNumberTripsSinceReset() {
		return numberTripsSinceReset;
	}
	public void setNumberTripsSinceReset(int numberTripsSinceReset) {
		this.numberTripsSinceReset = numberTripsSinceReset;
	}
	
	public int getCurrentAddnlOpStatus() {
		return currentAddnlOpStatus;
	}
	public void setCurrentAddnlOpStatus(int currentAddnlOpStatus) {
		this.currentAddnlOpStatus = currentAddnlOpStatus;
	}
	public double getDistMarkerAtLastOp() {
		return distMarkerAtLastOp;
	}
	public void setDistMarkerAtLastOp(double distMarkerAtLastOp) {
		this.distMarkerAtLastOp = distMarkerAtLastOp;
	}
	public double getTotDistToTargetOp() {
		return totDistToTargetOp;
	}
	public void setTotDistToTargetOp(double totDistToTargetOp) {
		this.totDistToTargetOp = totDistToTargetOp;
	}
	public double getEstDistToTargetOp() {
		return estDistToTargetOp;
	}
	public void setEstDistToTargetOp(double estDistToTargetOp) {
		this.estDistToTargetOp = estDistToTargetOp;
	}
	public long getLastLUEventTime() {
		return lastLUEventTime;
	}
	public void setLastLUEventTime(long lastLUEventTime) {
		this.lastLUEventTime = lastLUEventTime;
	}
	
	public double getAvgOpSpeedPerKM() {
		return avgOpSpeedPerKM;
	}
	public void setAvgOpSpeedPerKM(double avgOpSpeedPerKM) {
		this.avgOpSpeedPerKM = avgOpSpeedPerKM;
	}
	
	private static int calcNewLoadStatus(ExtLUInfoExtract lext, ExtLUInfoExtract uext) {
		int retval = DumperInfo.U_ENROUTE;
		if (uext != null) {
			if (uext.getWaitOut() > 0)
				retval = DumperInfo.U_ENROUTE;
			else if (uext.getGateOut() > 0)
				retval = DumperInfo.U_WAIT_AFT;
			else if (uext.getGateIn() > 0)
				retval = DumperInfo.U_BEING_OP;
			else
				retval = DumperInfo.U_WAIT;
		}
		else if (lext != null) {
			if (lext.getWaitOut() > 0)
				retval = DumperInfo.L_ENROUTE;
			else if (lext.getGateOut() > 0)
				retval = DumperInfo.L_WAIT_AFT;
			else if (lext.getGateIn() > 0)
				retval = DumperInfo.L_BEING_OP;
			else
				retval = DumperInfo.L_WAIT;
		}
		return retval;
	}
	public double getDistMarkerAtReset() {
		return distMarkerAtReset;
	}
	public void setDistMarkerAtReset(double distMarkerAtReset) {
		this.distMarkerAtReset = distMarkerAtReset;
	}
	public String getTripStatusString() {
		String retval = "N/A";
		if (this.isInRest()) {
			return "Going to/In Rest";
		}
		switch (currentLoadStatus) {
			case L_WAIT : return "Wait For Load";
			case L_BEING_OP : return "Being Loaded";
			case L_WAIT_AFT : return "Waitng After Load";
			case L_ENROUTE : return "Going to Unload";
			case U_WAIT : return "Wait For Unload";
			case U_BEING_OP : return "Being Unloaded";
			case U_WAIT_AFT : return "Waitng After Unload";
			case U_ENROUTE : return "Going to Load";
		
		}
		return "N/A";
	}
	
	public String getHoverText(Connection conn, int fromPerspectiveOf) {//0 - when getting name of route ignore src, 1= ignore dest, 2 = full Src/Site
		StringBuilder sb = new StringBuilder();
		String tripStatStr = this.getTripStatusString();
		sb.append(getName()).append(" Trip Status:").append(tripStatStr);
		sb.append("<br/>");
		NewMU newmu = this.getOwnerMU();
		Route route = newmu.getRouteInfo(this.getAssignedRoute());
		Route estRoute = newmu.getRouteInfo(this.getEstimatedRoute());
		if (route != null) {
			sb.append("Assigned:").append(route.getName(conn, fromPerspectiveOf));
		}
		if (this.getAssignedRoute() != this.getEstimatedRoute() && estRoute != null) {
			sb.append("Est Route:").append(estRoute.getName(conn, fromPerspectiveOf));
		}
		return sb.toString();
	}
	public boolean pointingToSrc() {
		boolean toSrc = this.currentLoadStatus == DumperInfo.L_WAIT || currentLoadStatus == DumperInfo.L_BEING_OP || currentLoadStatus == DumperInfo.U_WAIT_AFT || currentLoadStatus == DumperInfo.U_ENROUTE;
		return toSrc;
	}
	public double getLoadKMSinceReset() {
		return loadKMSinceReset;
	}
	public void setLoadKMSinceReset(double loadKMSinceReset) {
		this.loadKMSinceReset = loadKMSinceReset;
	}
	public double getDistTravelledSinceLastOp() {
		return distTravelledSinceLastOp;
	}
	public void setDistTravelledSinceLastOp(double distTravelledSinceLastOp) {
		this.distTravelledSinceLastOp = distTravelledSinceLastOp;
	}
	public double getAvgUnloadTimeSec() {
		return avgUnloadTimeSec;
	}
	public void setAvgUnloadTimeSec(double avgUnloadTimeSec) {
		this.avgUnloadTimeSec = avgUnloadTimeSec;
	}
	public double getDistMarkerAtPrevToLastOp() {
		return distMarkerAtPrevToLastOp;
	}
	public void setDistMarkerAtPrevToLastOp(double distMarkerAtPrevToLastOp) {
		this.distMarkerAtPrevToLastOp = distMarkerAtPrevToLastOp;
	}


	public Predicted getPredicted() {
		return predicted;
	}


	public void setPredicted(Predicted predicted) {
		this.predicted = predicted;
	}


	public int getOptimizeSrcSiteId() {
		return optimizeSrcSiteId;
	}


	public void setOptimizeSrcSiteId(int optimizeSrcSiteId) {
		this.optimizeSrcSiteId = optimizeSrcSiteId;
	}


	public int getOptimizeDestSiteId() {
		return optimizeDestSiteId;
	}


	public void setOptimizeDestSiteId(int optimizeDestSiteId) {
		this.optimizeDestSiteId = optimizeDestSiteId;
	}


	public int getOptimizeForDumperId() {
		return optimizeForDumperId;
	}


	public void setOptimizeForDumperId(int optimizeForDumperId) {
		this.optimizeForDumperId = optimizeForDumperId;
	}


	public boolean isOptimizeRecommended() {
		return optimizeRecommended;
	}


	public void setOptimizeRecommended(boolean optimizeRecommended) {
		this.optimizeRecommended = optimizeRecommended;
	}


	public long getLastPredictionRunAt() {
		return lastPredictionRunAt;
	}


	public void setLastPredictionRunAt(long lastPredictionRunAt) {
		this.lastPredictionRunAt = lastPredictionRunAt;
	}


	public long getLatestGRT() {
		return latestGRT;
	}
	public static void logMessage(Connection conn, int vehicleId, String src, String dest, String action, long ts, String addnl_message, int actionType) {
		PreparedStatement ps = null;
		try {
			if (ts <= 0)
				ts = System.currentTimeMillis();
			ps = conn.prepareStatement("insert into mining_log_message(vehicle_id, at_time, action, site_name, dest_name, desc_mesg, recv_time, action_type) values(?,?,?,?,?,?, now(),?)");
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.longToSqlDate(ts));
			ps.setString(3, action);
			ps.setString(4, src);
			ps.setString(5, dest);
			ps.setString(6, addnl_message);
			ps.setInt(6, actionType);
			ps.execute();
			ps = Misc.closePS(ps);
			if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (Exception e){
			
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
}

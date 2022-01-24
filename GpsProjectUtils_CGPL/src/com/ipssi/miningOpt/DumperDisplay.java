package com.ipssi.miningOpt;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;

public class DumperDisplay {
	private DumperInfo dumperInfo = null;
	public String getBlinkRate() {
		return this.critEvent ? "800" : "0";
	}
	public void getIconHoverText(StringBuilder sb) {
		sb.append(dumperInfo.getName());
	//	if (this.critEvent) {
	//		sb.append(BR).append(this.critText);
	//	}
	//	sb.append(",Capacity :").append(Misc.printDouble(dumperInfo.getCapacityWt(), "N/A", 1)).append(" Tonnes");
	}
	
	public int getCurrentLoadStatus() {
		return dumperInfo.getCurrentLoadStatus();
	}
	
	public String getIconHoverText() {
		StringBuilder sb = new StringBuilder();
		getIconHoverText(sb);
		return sb.toString();
	}
	public String getIconName() {
		//green - if working, yellow - if there is non crit alert, red if not working, gray if data delayed
		long now = dumperInfo.getOwnerMU().getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		int DELAY_THRESH = 10;
		int tripStatus = dumperInfo.getCurrentLoadStatusWithPredicted();
		boolean toSrc = dumperInfo.pointingToSrc();
		
		boolean delayedData =( now-this.latestDataAt)/(60*1000) > DELAY_THRESH;
		String retvalPrefix = "dumper_"+(toSrc ? "left_" : "right_");
		String retvalSuffix = this.wrong ? "_cross" : "";
		retvalSuffix += this.critEvent ?"_blink.png" : ".png";
		String retval = "green";
		if (delayedData)
			retval = "gray";
		/*else if (this.normEvent || this.showOpt)
			retval = "yellow";*/
		else if (this.notWorking)
			retval = "red";
		return retvalPrefix+retval+retvalSuffix;
	}
	public static String BR =",";
	public static String HR=",";
	public String getIconPopHoverText() {// shovels hover info
		StringBuilder sb = new StringBuilder();
		getIconHoverText(sb);
		
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_TIME_ONLY_FORMAT);
		if (this.notWorking) {
			sb.append("").append("Stopped Since:").append(Misc.printDate(sdf, this.stoppedSince, "N/A"));
		}
		sb.append(BR);
		sb.append("Load Status:").append(dumperInfo.getTripStatusString());
		sb.append(BR);
		sb.append("Last Op Time:").append(Misc.printDate(sdf, dumperInfo.getLastLUEventTime(), "N/A")).append(BR);
	    sb.append("Fuel level:").append(Misc.printDouble(this.fuelLevel, "N/A",1)).append(BR);	
	    sb.append("Refuel at:").append(Misc.printDate(sdf,this.fuellingNeededAt,"N/A")).append(BR);
	    sb.append("Pos at:").append(this.latestPostAt != null ? this.latestPostAt : "N/A").append(BR);
	    sb.append("Recent Data at:").append(Misc.printDate(sdf, this.latestDataAt, "N/A")).append(BR);
	    sb.append("Engine On:").append(this.ignOn == null ? "N/A": this.ignOn).append(BR);

	    sb.append("Shift # Trips:").append(Misc.printInt(dumperInfo.getNumberTripsSinceReset(), "N/A")).append(BR);
		sb.append("Load KM:").append(Misc.printDouble(dumperInfo.getLoadKMSinceReset(),"N/A", 1));
		if (this.wrong) {
			sb.append(BR).append("Wrong Route:").append(this.wrongText);
		}
		
		if (this.normEvent) {
			sb.append(BR).append("Other Event:").append(BR).append(this.normText);
		}
		return sb.toString();
	}
	
	public DumperDisplay(DumperInfo dumperInfo, Connection conn, long now) throws Exception {
		this.dumperInfo = dumperInfo;
		calc(conn, now);
	}
	private void calc(Connection conn, long now) throws Exception {
		NewMU newmu = dumperInfo.getOwnerMU();
		calcNamesFuellingEtc(conn, newmu, now);
		this.calcEvent(conn, newmu, now);
		this.calcWrong(conn, newmu, now);
		this.calcNotWorking(conn, newmu, now);
	}
	
	private void calcNamesFuellingEtc(Connection conn, NewMU newmu, long now) throws Exception {
		if (now <= 0)
			now = newmu.getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		//getting Names
		StringBuilder sb = new StringBuilder();
		long minFuelWillLastTill =-1;
		VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, dumperInfo.getId(),true, false);
		GpsData lastPosData = null;
		if (vdf != null) {
			synchronized (vdf) {
				NewVehicleData vdt = vdf.getDataList(conn, dumperInfo.getId(), 0, false);
				GpsData last = vdt.getLast(conn);
				lastPosData = last;
				this.latestPostAt = last == null ? null : last.calcName(conn, dumperInfo.getId(), CacheTrack.VehicleSetup.getSetup(dumperInfo.getId(), conn));
				this.latestDataAt = last == null ? -1 : last.getGps_Record_Time();
				vdt = vdf.getDataList(conn, dumperInfo.getId(), 3, false);
				last = vdt.getLast(conn);
				double level = last == null ? Misc.getUndefDouble() : last.getValue();
				double kmtravellable = dumperInfo.getAvgFuelConsumptionRate() > 0.00005 ? level/dumperInfo.getAvgFuelConsumptionRate() : Misc.getUndefDouble();
				double hrs = !Misc.isUndef(kmtravellable) && dumperInfo.getAvgOpSpeedPerKM() > 0.00005 ? kmtravellable/dumperInfo.getAvgOpSpeedPerKM() : Misc.getUndefDouble();
				long ts = last == null ? -1 : last.getGps_Record_Time();
				long willLastTill = Misc.isUndef(level) ? -1 : ts + (long) (hrs*3600*1000);
				this.fuelLevel = level;
				this.fuellingNeededAt = willLastTill;
				
				vdt = vdf.getDataList(conn, dumperInfo.getId(), 1, false); //IGN ON/OFF
				last = vdt.getLast(conn);
				this.ignOn = last == null ? null : last.getValue() > 0.5 ? "ON" : "OFF";
			}
		}
		//calculate of exceeded target etc
		
		
	}
	String latestPostAt = null;
	long latestDataAt = -1;
	double fuelLevel = Misc.getUndefDouble();
	long fuellingNeededAt = -1;
	String ignOn = null;
	boolean critEvent = false;
	boolean normEvent = false;
	boolean notWorking = false;
	StringBuilder critText = new StringBuilder();
	StringBuilder normText = new StringBuilder();
	boolean wrong = false;
	String wrongText = null;
	long stoppedSince = -1;
	boolean showOpt = false;
	String optimizeInstruction = null;
	
	private void calcEvent(Connection conn, NewMU newmu,  long now) throws Exception {
		if (now <= 0)
			now = newmu.getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		int vehicleId = dumperInfo.getId();
		NewEventDismissMgmt eventDismissMgmt = dumperInfo.getEventDismissMgmt(conn);
		EventDismiss eventDismiss = eventDismissMgmt.getLast(conn);
		SimpleDateFormat sdf= new SimpleDateFormat(Misc.G_DEFAULT_TIME_ONLY_FORMAT);
		if (eventDismiss == null || eventDismiss.getAtTime() < this.dumperInfo.getDynOptimizerRunAt()) {
			
			Site olsite = newmu.getSiteInfo(this.dumperInfo.getOptimizeSrcSiteId());
			Site ousite = newmu.getSiteInfo(this.dumperInfo.getOptimizeDestSiteId());
			if (olsite != null && ousite != null) {
				this.critEvent = true;
				this.showOpt = true;
				critText.append(HR);
				this.optimizeInstruction = "Divert "+this.dumperInfo.getName()+" to Shovel:"+olsite.getName()+"  Dest:"+ousite.getName();
				critText.append(this.optimizeInstruction);
				critText.append(HR);
			}
		}
		int THRESH_REFUELLING_MIN = 10;
		if ((this.fuellingNeededAt > 0 && (this.fuellingNeededAt-now)/(60*1000) < THRESH_REFUELLING_MIN)
		&&(eventDismiss == null || eventDismiss.getAtTime() <= now-THRESH_REFUELLING_MIN*60*1000)
		) {
			this.critEvent = true;
			critText.append("Refuelling needed in ").append(THRESH_REFUELLING_MIN).append(" min");
		}
		for (int art=0;art<2;art++) {
			ArrayList<Integer> ruleIds = art == 0 ? newmu.parameters.CRIT_EVENTS_TO_TRACK_DUMPER : newmu.parameters.NORM_EVENTS_TO_TRACK_DUMPER;
			StringBuilder sb = art == 0 ? this.critText : this.normText;
			for (int j=0,js=ruleIds.size(); j<js; j++) {
				int ruleId = ruleIds.get(j);
				if (ruleId == 1)
					continue;
				CacheValue.LatestEventInfo latestEvent = CacheValue.getLatestOpenEvent(vehicleId, ruleId);
				if (latestEvent != null &&(eventDismiss == null || eventDismiss.getAtTime() <=  latestEvent.getStartTime())) {
					if (sb.length() != 0)
						sb.append(BR);
					sb.append(latestEvent.getRuleName()).append(" @ ").append(sdf.format(Misc.longToUtilDate(latestEvent.getStartTime())));	
				}
			}
			if (art == 0) {
				this.critEvent = true;
			}
			else {
				this.normEvent = true;
			}
		}
	}
	private void calcWrong(Connection conn, NewMU newmu, long now) throws Exception {
		int assignedRoute = dumperInfo.getAssignedRoute();
		int estRoute = dumperInfo.getEstimatedRoute();
		boolean diffRoute = assignedRoute != estRoute;
		double distTravelled = this.dumperInfo.getDistTravelledSinceLastOp();
		double distToTravel = this.dumperInfo.getTotDistToTargetOp();
		boolean overshot = false;
		if (distTravelled > 1.2*distToTravel) {
			overshot = true;
		}
		if (diffRoute || overshot) {
			this.wrong = true;
		}
		if (diffRoute) {
			
			Route estRouteInfo = newmu.getRouteInfo(dumperInfo.getEstimatedRoute());
			this.wrongText = "On Diff Route:"+(estRouteInfo == null ? "N/A" : estRouteInfo.getName(conn, 2));
		}
		else if (overshot) {
			this.wrongText = "Prob overshot Dest";
		}
		else {
			this.wrongText= null;
		}
		
	}
	private void calcNotWorking(Connection conn, NewMU newmu, long now) throws Exception {
		this.notWorking = false;
		this.stoppedSince = -1;
		if (dumperInfo.getAssignmentStatus() != CoreVehicleInfo.ASSIGNED || dumperInfo.isInRest())
			return;
		int loadStatus = this.dumperInfo.getCurrentLoadStatus();
		boolean isLoadLike = this.dumperInfo.isTripStatusLikeLoad(loadStatus);
		boolean opCompleted = this.dumperInfo.isOpCompleted(loadStatus);
		int THRESH_NO_PERF_SEC = 5*60;
		Triple<Long, Double, Double> stp = CacheTrack.getStopSinceTimeLonLat(conn, dumperInfo.getId());
		long stopBase = -1;
		if (now <= 0)
			now = newmu.getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();

		if (stp != null && stp.first != null && stp.first > 0) {
			int gapSec = (int)((now - stp.first.longValue())/1000.0);
			stopBase = stp.first.longValue();
			if (!opCompleted) {//check if it is at beginning of Q
				Route route = newmu.getRouteInfo(this.dumperInfo.getAssignedRoute());
				if (route != null) {
					Site site = isLoadLike ? newmu.getLoadSite(conn, route.getLoadSite()) : newmu.getUnloadSite(conn, route.getUnloadSite());
					if (site != null && site.isAtBegOfQ(dumperInfo.getId())) {
						WaitItem item = site.getQueue().getByVehicleId(dumperInfo.getId());
						//int cycleTimeSec = site.get
						//check how long waiting, and if that is gapThresh+
						long cmpAgainst =Math.max(item.getLoadBeginAt(), item.getEntryAt());
						cmpAgainst = Math.max(cmpAgainst, site.getLatestProcessedAt());
						gapSec = (int)((now - cmpAgainst)/1000.0);
						stopBase = cmpAgainst;
					}
				}
			}
			stoppedSince = stopBase;
			if (gapSec > THRESH_NO_PERF_SEC && opCompleted) {
				this.notWorking = true;
			}
		}
	}
}

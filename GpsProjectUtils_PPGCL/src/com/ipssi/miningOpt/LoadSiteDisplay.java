package com.ipssi.miningOpt;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;

public class LoadSiteDisplay {
	private LoadSite site = null;

	public String getShovelName() {
		return finData[0];
	}
	public String getIconName() {
		//green - if working, yellow - if there is non crit alert, red if not working, gray if data delayed
		long now = site.getOwnerMU().getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		int DELAY_THRESH = 10;
		boolean delayedData =( now-this.latestDataAt)/(60*1000) > DELAY_THRESH;
		String suffix = critEvent ? "_blink.gif" : ".png";
		String retval = "shovel_green"+suffix;
		if (delayedData)
			retval = "shovel_gray"+suffix;
		/*else if (this.normEvent || this.showOpt)
			retval = "shovel_yellow"+suffix;*/
		else if (this.notWorking)
			retval = "shovel_red"+suffix;
		return retval;
	}
	public String getLeftHoverText() {
		StringBuilder sb = new StringBuilder();
		sb.append("Shovel Name");
		sb.append("<hr noshade size='1' width='100%'>");
		sb.append("Site name [Pit Name] if diff from shovel");
		sb.append("<hr noshade size='1' width='100%'>");
		sb.append("# Vehicles Assigned / # of Vehicles in Wait / Recent Avg cycle time(s)");
		sb.append("<hr noshade size='1' width='100%'>");
		sb.append("# Wait when current loaded comes back / Recent Tonnes/Hr Desp / Shift Tonnage Desp");
		return sb.toString();
		//line 2:# Vehicles Assigned, # waits, Avg cycle time 
		//line 3:# Waits in future, recent rate of tonnes/Hr, cumm tonnes in Shift [LATER]
	}
	public String getRightHoverText() {
		StringBuilder sb = new StringBuilder();
		ArrayList<Integer> dumpersAssigned = site.getAssignedDumpers();
		try {
			site.getReadLock();
			for (int i=0,is=dumpersAssigned.size(); i<is; i++) {
				if (i != 0) {
					sb.append("<hr noshade size='1' width='100%'>");
				}
				DumperInfo dumperInfo =(DumperInfo) site.getOwnerMU().getVehicleInfo(dumpersAssigned.get(i));
				sb.append(dumperInfo.getHoverText(null, 0));
			}
		}
		catch (Exception e) {
			
		}
		finally {
			site.releaseReadLock();
		}
		return sb.toString();
		//line 2:# Vehicles Assigned, # waits, Avg cycle time 
		//line 3:# Waits in future, recent rate of tonnes/Hr, cumm tonnes in Shift [LATER]
	}
	public String getIconHoverText() {// shovels hover info
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_TIME_ONLY_FORMAT);
		if (siteName != null && siteName.length() > 0 && !siteName.equals(shovelName) && detailedShovelInfo.size() > 1) {
			sb.append(siteName).append((pitName != null && pitName.length() > 0) ? (" ["+pitName+"]") : "");
			sb.append("<hr noshade size='1' width='100%'>");
			
		}
		for (int i=0,is=detailedShovelInfo.size(); i<is; i++) {
			ShovelDisplay shovelDisplay = detailedShovelInfo.get(i);
			if (shovelDisplay.critEvent != null && shovelDisplay.critEvent.length() > 0) {
				sb.append("<b>Crit Event:").append(shovelDisplay.getShovel().getName()).append("</b><br/>");
				sb.append(shovelDisplay.critEvent);
				sb.append("<hr noshade size='1' width='100%'>");
			}
		}
		sb.append("# Veh Assigned   :").append(Misc.printInt(this.vehiclesAssigned, "N/A")).append("<br/>");
		sb.append("Curr QLen            :").append(Misc.printInt(this.currWait,"N/A")).append("<br/>");
		sb.append("Cycle time(s)       :").append(Misc.printInt(this.avgCycleTime, "N/A")).append("<br/>");
		sb.append("Curr Disp (T/hr)  :").append(Misc.printDouble(this.tonnesPerHour, "N/A", 1)).append("<br/>");
		sb.append("Curr Trips (#/hr)  :").append(Misc.printDouble(this.loadsPerHour, "N/A", 1)).append("<br/>");
		sb.append("Shift Tonne           :").append(Misc.printDouble(this.totTonnesInShift, "N/A", 1)).append("<br/>");
		sb.append("Last desp at         :").append(Misc.printDate(sdf, this.latestDispAt, "N/A")).append("<br/>");
		sb.append("Exp Q next cycle  :").append(Misc.printInt(this.qlenWhenMeLeavesAndComesBack, "N/A")).append("<br/>");
		for (int i=0,is=detailedShovelInfo.size(); i<is; i++) {
			ShovelDisplay shovelDisplay = detailedShovelInfo.get(i);
			sb.append("<hr noshade size='1' width='100%'>");
			sb.append("<b>").append(shovelDisplay.getShovel().getName()).append("</b>").append("<br/>");
			sb.append("Capacity             :").append(Misc.printDouble(shovelDisplay.getShovel().getCapacityVol(), "N/A", 1)).append(" cu m<br/>");
		    sb.append("Fuel level            :").append(Misc.printDouble(shovelDisplay.fuelLevel, "N/A",1)).append("<br/>");	
		    sb.append("Refuel at            :").append(Misc.printDate(sdf,shovelDisplay.fuellingNeededAt,"N/A")).append("<br/>");
		    sb.append("Pos at                  :").append(shovelDisplay.locAt != null ? shovelDisplay.locAt : "N/A").append("<br/>");
		    sb.append("Recent Data at:").append(Misc.printDate(sdf, shovelDisplay.dataAt, "N/A")).append("<br/>");
		    sb.append("Engine On          :").append(shovelDisplay.ignOn == null ? "N/A": shovelDisplay.ignOn).append("<br/>");
		    
		    sb.append("Other Evt:").append("<br/>");
		    sb.append(shovelDisplay.normEvent);
		}
		return sb.toString();
	}
	
	
	public String getBlinkRate() {// blink rate else boolean doBlink
		return this.critEvent ? "100" : "0";
	}
	public String getFirstLineStatistics() {// SiteName [PitName]
		return finData[1];
	}
	public String getSecondLineStatistics() {// Second line Statistics
		return finData[2];
	}
	public String getThirdLineStatistics() {// Third line Statistics, if any
		return finData[3];
	}
	private String[] finData = null;
	private String shovelName = null;
	private String pitName = null;
	private String siteName = null;
	private int vehiclesAssigned = 0;
	private int currWait = 0;
	private int qlenWhenMeLeavesAndComesBack = 0;
	private int avgCycleTime;
	private double loadsPerHour;
	private double tonnesPerHour;
	private int totTripsInShift;
	private double totTonnesInShift;
	private long fuellingNeededAt;
	private long latestDispAt;
	private boolean normEvent = false;
	private boolean critEvent = false;;
	private boolean notWorking = false;
	private ArrayList<ShovelDisplay> detailedShovelInfo = new ArrayList<ShovelDisplay>();
	private long latestDataAt = -1;
	private String latestPosAt = null;
	private boolean showOpt = false;
	private String optimizationInstruction = null;
	public String helpCalcFinDataLine2() {
		//line 2:# Vehicles Assigned, # waits, Avg cycle time 
		//line 3:# Waits in future, recent rate of tonnes/Hr, cumm tonnes in Shift [LATER]
		StringBuilder sb = new StringBuilder();
		sb.append(Misc.printInt(this.vehiclesAssigned, "N/A")).append(" / ");
		sb.append(Misc.printInt(this.currWait, "N/A")).append(" / ");
		sb.append(Misc.printInt(this.avgCycleTime, "N/A")).append("s ");
	    
	    return sb.toString();
	}
	public String helpCalcFinDataLine3() {
		//line 2:# Vehicles Assigned, # waits, Avg cycle time 
		//line 3:# Waits in future, recent rate of tonnes/Hr, cumm tonnes in Shift [LATER]
		StringBuilder sb = new StringBuilder();
		sb.append(Misc.printInt(this.qlenWhenMeLeavesAndComesBack, "N/A")).append(" / ");
		sb.append(Misc.printDouble(this.tonnesPerHour, "N/A",1)).append(" / ");
		sb.append(Misc.printDouble(this.totTonnesInShift, "N/A",1));
		return sb.toString();
	}
	public LoadSiteDisplay(LoadSite site, Connection conn, long now) throws Exception {
		this.site = site;
		calc(conn, now);
		setupDisp();
	}
	private void setupDisp() {
		finData = new String[4];
		finData[0] = null;
		finData[1] = null;
		finData[2] = null;
		finData[3] = null;
		int idxUsed = 0;
		if (shovelName != null && shovelName.length() > 0) 
			finData[idxUsed++] = shovelName;
		if (siteName != null && siteName.length() > 0 && !siteName.equals(shovelName)) {
			finData[idxUsed++] = siteName + (pitName != null && pitName.length() > 0 ? ("["+pitName+"]") : "");
		}
		finData[idxUsed++] = helpCalcFinDataLine2();
		finData[idxUsed++] = helpCalcFinDataLine3();
	}
	private void calc(Connection conn, long now) throws Exception {
		NewMU newmu = site.getOwnerMU();
		try {
			site.getReadLock();
			ArrayList<Integer> shovelsList = site.getAssignedShovels();
			if (shovelsList != null && shovelsList.size() > 0) {
				for (int i=0,is=shovelsList == null ? 0 : shovelsList.size(); i<is; i++) {
					int shovelId = shovelsList.get(i);
					CoreVehicleInfo vehicleInfo = newmu.getVehicleInfo(shovelId);
					if (vehicleInfo != null && vehicleInfo instanceof ShovelInfo)
						detailedShovelInfo.add(new ShovelDisplay((ShovelInfo) vehicleInfo));
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			site.releaseReadLock();
		}
		this.calcNamesFuellingEtc(conn, newmu, detailedShovelInfo, now);
		this.calcPerformanceStats(newmu, detailedShovelInfo);
		this.calcEvent(conn, newmu, detailedShovelInfo, now);
		this.calcWaitQ(conn, newmu, detailedShovelInfo, now);
	}
	
	private void calcNamesFuellingEtc(Connection conn, NewMU newmu, ArrayList<ShovelDisplay> detailedShovelInfo, long now) throws Exception {
		if (now <= 0)
			now = newmu.getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		//getting Names
		StringBuilder sb = new StringBuilder();
		long minFuelWillLastTill =-1;
		
		for (int i=0,is=detailedShovelInfo == null ? 0 : detailedShovelInfo.size(); i<is; i++) {
			ShovelDisplay shovelDisplay = detailedShovelInfo.get(i);
			if (shovelDisplay == null)
				continue;
			if (sb.length() == 0) {
				if (is > 1)
				sb.append("[").append(is).append("]");
			}
			else 
				sb.append(",");
			
			ShovelInfo shovelInfo = shovelDisplay.getShovel();
			shovelDisplay.name = shovelInfo.getName();
			sb.append(shovelInfo.getName());
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, shovelInfo.getId(), false, false);
			if (vdf != null) {
				synchronized (vdf) {
					NewVehicleData vdt = vdf.getDataList(conn, shovelInfo.getId(), NewMU.FUEL_DIM_ID, false);
					GpsData last = vdt == null ? null : vdt.getLast(conn);
					double level = last == null ? Misc.getUndefDouble() : last.getValue();
					long ts = last == null ? -1 : last.getGps_Record_Time();
					long willLastTill = Misc.isUndef(level) ? -1 : ts + (long) (level/shovelInfo.getAvgFuelConsumptionRate()*3600*1000);
					shovelDisplay.fuelLevel = level;
					shovelDisplay.fuellingNeededAt = willLastTill;
					if (willLastTill > 0 && (minFuelWillLastTill <= 0 || willLastTill < minFuelWillLastTill))
						minFuelWillLastTill = willLastTill;
					vdt = vdf.getDataList(conn, shovelInfo.getId(), 0, false);
					last = vdt == null ? null : vdt.getLast(conn);
					shovelDisplay.dataAt = last == null ? -1 : last.getGps_Record_Time();
					shovelDisplay.locAt = last == null ? null : last.calcName(conn, shovelInfo.getId(), CacheTrack.VehicleSetup.getSetup(shovelInfo.getId(), conn));
					if (this.latestDataAt < shovelDisplay.dataAt) {
						this.latestDataAt = shovelDisplay.dataAt;
						this.latestPosAt = shovelDisplay.locAt;
					}
					vdt = vdf.getDataList(conn, shovelInfo.getId(), 1, false); //IGN ON/OFF
					last = vdt.getLast(conn);
					shovelDisplay.ignOn = last == null ? null : last.getValue() > 0.5 ? "ON" : "OFF";
				}
			}
		}
		this.shovelName = sb.toString();
		Pits pit = newmu.getPit(conn, site.getPitId());
		this.pitName = pit == null ? null : pit.getName();
		this.siteName = site.getName();
		this.fuellingNeededAt = minFuelWillLastTill;
	}
	
	private void calcEvent(Connection conn, NewMU newmu, ArrayList<ShovelDisplay> detailedShovelInfo, long now) throws Exception {
		if (now <= 0)
			now = newmu.getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		
		for (int i=0,is=detailedShovelInfo == null ? 0 : detailedShovelInfo.size(); i<is;i++) {
			ShovelDisplay shovelDisplay = detailedShovelInfo.get(i);
			ShovelInfo shovelInfo = shovelDisplay.getShovel();
			int shovelId = shovelDisplay.getShovel().getId();
			NewEventDismissMgmt eventDismissMgmt = shovelInfo.getEventDismissMgmt(conn);
			EventDismiss eventDismiss = eventDismissMgmt.getLast(conn);
			SimpleDateFormat sdf= new SimpleDateFormat(Misc.G_DEFAULT_TIME_ONLY_FORMAT);
			for (int art=0;art<2;art++) {
				ArrayList<Integer> ruleIds = art == 0 ? newmu.parameters.CRIT_EVENTS_TO_TRACK_SHOVEL : newmu.parameters.NORM_EVENTS_TO_TRACK_SHOVEL;
				StringBuilder sb = new StringBuilder();
				if (art == 0) {
					if (eventDismiss == null || eventDismiss.getAtTime() < this.site.getDynOptimizerRunAt()) {
						
						Site olsite = newmu.getSiteInfo(this.site.getOptimizeSrcSiteId());
						Site ousite = newmu.getSiteInfo(this.site.getOptimizeDestSiteId());
						CoreVehicleInfo divertedTruck = newmu.getVehicleInfo(this.site.getOptimizeForDumperId());
						if (olsite != null && ousite != null && divertedTruck != null) {
							this.critEvent = true;
							this.showOpt = true;
							sb.append("<hr noshade size='1' width='100%'>");
							this.optimizationInstruction = "Divert "+ divertedTruck.getName()+" to Shovel:"+olsite.getName()+"  Dest:"+ousite.getName();
							sb.append(optimizationInstruction);
							sb.append("<hr noshade size='1' width='100%'>");
						}
					}
					int THRESH_REFUELLING_MIN = 10;
					if ((shovelDisplay.fuellingNeededAt > 0 && (shovelDisplay.fuellingNeededAt-now)/(60*1000) < THRESH_REFUELLING_MIN)
					&&(eventDismiss == null || eventDismiss.getAtTime() <= now-THRESH_REFUELLING_MIN*60*1000)
					) {
						this.critEvent = true;
						sb.append("Refuelling needed in ").append(THRESH_REFUELLING_MIN).append(" min");
					}
				}
				for (int j=0,js=ruleIds.size(); j<js; j++) {
					int ruleId = ruleIds.get(j);
					if (ruleId == 1)
						continue;
					CacheValue.LatestEventInfo latestEvent = CacheValue.getLatestOpenEvent(shovelId, ruleId);
					if (latestEvent != null &&(eventDismiss == null || eventDismiss.getAtTime() <=  latestEvent.getStartTime())) {
						if (sb.length() != 0)
							sb.append("<br/>");
						sb.append(latestEvent.getRuleName()).append(" @ ").append(sdf.format(Misc.longToUtilDate(latestEvent.getStartTime())));	
					}
				}
				if (art == 0) {
					shovelDisplay.critEvent = sb;
					this.critEvent = true;
				}
				else {
					shovelDisplay.normEvent = sb;
					this.normEvent = true;
				}
			}
		}
	}
	private void calcPerformanceStats(NewMU newmu, ArrayList<ShovelDisplay> detailedShovelInfo) {
		WaitQueue statsQueue = site.getStatsQueue();
		long minTS = Long.MAX_VALUE;
		long maxTS = Long.MIN_VALUE;
		int totStatTripDisp = 0;
		int totStatTonnDisp = 0;
		int totStatNumCycles = 0;
		int totStatNumCycleSec = 0;
		synchronized (statsQueue) {
			for (WaitQueue.Iterator iter = statsQueue.iterator(); iter.hasNext();) {
				WaitItem waitItem = iter.next();
				if (waitItem.getLoadBeginAt() <= 0 || waitItem.getLoadEndAt() <= 0)
					continue;
				ShovelInfo opShovelInfo = (ShovelInfo) newmu.getVehicleInfo(waitItem.getShovelId());
				DumperInfo opDumperInfo = (DumperInfo) newmu.getVehicleInfo(waitItem.getVehicleId());
				int secsToLoad = (int)(waitItem.getLoadEndAt()-waitItem.getLoadBeginAt())/1000;
				int numCycles = waitItem.numCyclesForLoad(opShovelInfo, opDumperInfo);
				double tonnage = waitItem.tonnes(opDumperInfo);
				ShovelDisplay display = this.getShovelDisplay(waitItem.getShovelId(), detailedShovelInfo);
				if (display != null) {
					display.totStatNumCycles += numCycles;
					display.totStatNumCycleSec += secsToLoad;
					display.totStatTonnDisp += tonnage;
					display.totStatTripDisp += 1;
					if (waitItem.getLoadEndAt() > display.latestDispAt)
						display.latestDispAt = waitItem.getLoadEndAt();
				}
				totStatNumCycles += numCycles;
				totStatNumCycleSec += secsToLoad;
				totStatTonnDisp += tonnage;
				totStatTripDisp += 1;
				if (minTS > waitItem.getLoadBeginAt())
					minTS = waitItem.getLoadBeginAt();
				if (maxTS < waitItem.getLoadEndAt())
					maxTS = waitItem.getLoadEndAt();
			}
		}
		this.avgCycleTime = totStatNumCycles <= 0  ? Misc.getUndefInt() : (int) Math.round((double) totStatNumCycleSec/(double) totStatNumCycles);
		this.loadsPerHour = maxTS-minTS <= 0 ? Misc.getUndefDouble() : (double) totStatTripDisp/(double)(maxTS-minTS)*1000*3600;
		this.tonnesPerHour = maxTS-minTS <= 0 ? Misc.getUndefDouble() : (double) totStatTonnDisp/(double)(maxTS-minTS)*1000*3600;
		this.totTripsInShift = this.site.getTotTripsInShift();
		this.totTonnesInShift = this.site.getTotTonnesInShift();
		this.latestDispAt = maxTS;
	}
	
	private void calcWaitQ(Connection conn, NewMU newmu, ArrayList<ShovelDisplay> detailedShovelInfo, long now) throws Exception {//TODO
		if (now <= 0)
			now = newmu.getSimulationNow();
		if (now <= 0)
			now = System.currentTimeMillis();
		this.vehiclesAssigned = site.getNumDumpersAssigned(conn);
		int waiting = 0;
		WaitQueue queue = site.getQueue();
		if (newmu.parameters.g_usePredictedToShowCurrStuff) {
			queue = site.getPredictedQueue();
		}
		synchronized (queue) {
			for (WaitQueue.Iterator iter = queue.iterator(); iter.hasNext();) {
				WaitItem waitItem = iter.next();
				waiting += 1;
			}
		}
		this.currWait = waiting;
		this.qlenWhenMeLeavesAndComesBack = site.getPredictedQLenWhenLatestDumperOutComesBack();
		if (this.currWait > 0 && (this.vehiclesAssigned > 0 || this.detailedShovelInfo.size() > 0)) {
			int THRESH_NO_PERF_SEC = 10*60;
			if (now - this.latestDispAt > THRESH_NO_PERF_SEC*1000)
				this.notWorking = true;
		}
	}
	
	private static int getPosInList(int shovelId, ArrayList<ShovelDisplay> theList) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is;i++)
			if (theList.get(i).getShovel().getId() == shovelId)
				return i;
		return -1;
	}
	private static ShovelDisplay getShovelDisplay(int shovelId, ArrayList<ShovelDisplay> theList) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is;i++)
			if (theList.get(i).getShovel().getId() == shovelId)
				return theList.get(i);
		return null;
	}
	public ArrayList<ShovelDisplay> getDetailedShovelInfo() {
		return detailedShovelInfo;
	}
	
}

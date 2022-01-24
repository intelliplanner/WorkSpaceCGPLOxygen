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

public class UnloadSiteDisplay {
	private UnloadSite site = null;

	public String getShovelName() {
		return finData[0];
	}
	public String getIconName() {
		//green - if working, yellow - if there is non crit alert, red if not working, gray if data delayed
		return "icon_unload.png";		
	}
	public String getLeftHoverText() {
		StringBuilder sb = new StringBuilder();
		sb.append("Site Name");
		sb.append("<hr noshade size='1' width='100%'>");
		sb.append("# Vehicles Assigned / # of Vehicles inside / Recent Avg Turnaround time(s)");
		sb.append("<hr noshade size='1' width='100%'>");
		sb.append("Recent Tonnes/Hr recvd / Shift Tonnage Desp");
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
				sb.append(dumperInfo.getHoverText(null, 1));
			}
		}
		catch (Exception e) {
			
		}
		finally {
			site.releaseReadLock();
		}
		return sb.toString();
		
	}
	public String getIconHoverText() {// shovels hover info
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_TIME_ONLY_FORMAT);
		sb.append(siteName);
		
		
		sb.append("# Veh Assigned   :").append(Misc.printInt(this.vehiclesAssigned, "N/A")).append("<br/>");
		sb.append("Curr QLen            :").append(Misc.printInt(this.currWait,"N/A")).append("<br/>");
		sb.append("Cycle time(s)       :").append(Misc.printInt(this.avgCycleTime, "N/A")).append("<br/>");
		sb.append("Curr Recv (T/hr)  :").append(Misc.printDouble(this.tonnesPerHour, "N/A", 1)).append("<br/>");
		sb.append("Curr Recv (#/hr)  :").append(Misc.printDouble(this.loadsPerHour, "N/A", 1)).append("<br/>");
		sb.append("Shift Tonne           :").append(Misc.printDouble(this.totTonnesInShift, "N/A", 1)).append("<br/>");
		sb.append("Last Exit at         :").append(Misc.printDate(sdf, this.latestDispAt, "N/A")).append("<br/>");
		
		
		return sb.toString();
	}
	
	
	public String getBlinkRate() {// blink rate else boolean doBlink
		return  "0";
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
	private String siteName = null;
	private int vehiclesAssigned = 0;
	private int currWait = 0;
	private int avgCycleTime;
	private double loadsPerHour;
	private double tonnesPerHour;
	private int totTripsInShift;
	private double totTonnesInShift;
	private long latestDispAt;
	private boolean notWorking = false;
	public String helpCalcFinDataLine2() {
//		sb.append("# Vehicles Assigned / # of Vehicles inside / Recent Avg Turnaround time(s)");
//		sb.append("<hr noshade size='1' width='100%'>");
//		sb.append("Recent Tonnes/Hr recvd / Shift Tonnage Desp");
		StringBuilder sb = new StringBuilder();
		sb.append(Misc.printInt(this.vehiclesAssigned, "N/A")).append(" / ");
		sb.append(Misc.printInt(this.currWait, "N/A")).append(" / ");
		sb.append(Misc.printInt(this.avgCycleTime, "N/A")).append("s ");
	    
	    return sb.toString();
	}
	public String helpCalcFinDataLine3() {
//		sb.append("# Vehicles Assigned / # of Vehicles inside / Recent Avg Turnaround time(s)");
//		sb.append("<hr noshade size='1' width='100%'>");
//		sb.append("Recent Tonnes/Hr recvd / Shift Tonnage Desp");
		StringBuilder sb = new StringBuilder();
		sb.append(Misc.printDouble(this.tonnesPerHour, "N/A",1)).append(" / ");
		sb.append(Misc.printDouble(this.totTonnesInShift, "N/A",1));
		return sb.toString();
	}
	public UnloadSiteDisplay(UnloadSite site, Connection conn, long now) throws Exception {
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
		if (siteName != null && siteName.length() > 0) {
			finData[idxUsed++] = siteName;
		}
		finData[idxUsed++] = helpCalcFinDataLine2();
		finData[idxUsed++] = helpCalcFinDataLine3();
	}
	private void calc(Connection conn, long now) throws Exception {
		NewMU newmu = site.getOwnerMU();
		
		this.calcPerformanceStats(newmu);
		this.calcWaitQ(conn, newmu, now);
	}
	
	private void calcPerformanceStats(NewMU newmu) {
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
				DumperInfo opDumperInfo = (DumperInfo) newmu.getVehicleInfo(waitItem.getVehicleId());
				int secsToLoad = (int)(waitItem.getExitAt()-waitItem.getEntryAt())/1000;
				int numCycles = 1;
				double tonnage = waitItem.tonnes(opDumperInfo);
				
				totStatNumCycles += numCycles;
				totStatNumCycleSec += secsToLoad;
				totStatTonnDisp += tonnage;
				totStatTripDisp += 1;
				if (minTS > waitItem.getEntryAt())
					minTS = waitItem.getEntryAt();
				if (maxTS < waitItem.getExitAt())
					maxTS = waitItem.getExitAt();
			}
		}
		this.avgCycleTime = totStatNumCycles <= 0  ? Misc.getUndefInt() : (int) Math.round((double) totStatNumCycleSec/(double) totStatNumCycles);
		this.loadsPerHour = maxTS-minTS <= 0 ? Misc.getUndefDouble() : (double) totStatTripDisp/(double)(maxTS-minTS)*1000*3600;
		this.tonnesPerHour = maxTS-minTS <= 0 ? Misc.getUndefDouble() : (double) totStatTonnDisp/(double)(maxTS-minTS)*1000*3600;
		this.totTripsInShift = this.site.getTotTripsInShift();
		this.totTonnesInShift = this.site.getTotTonnesInShift();
		this.latestDispAt = maxTS;
	}
	
	private void calcWaitQ(Connection conn, NewMU newmu, long now) throws Exception {//TODO
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
		if (this.currWait > 0 && this.vehiclesAssigned > 0 ) {
			int THRESH_NO_PERF_SEC = 10*60;
			if (now - this.latestDispAt > THRESH_NO_PERF_SEC*1000)
				this.notWorking = true;
		}
	}
	
	
}

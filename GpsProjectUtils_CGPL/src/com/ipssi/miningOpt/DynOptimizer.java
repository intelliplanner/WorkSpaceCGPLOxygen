package com.ipssi.miningOpt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.miningOpt.Predictor.AnalysisResult;

public class DynOptimizer {
	public static int OPTIMIZE_STICKY = 2;
	public static int OPTIMIZE_PREF_SHOVEL = 0;
	public static int OPTIMIZE_PREF_FIRST_TO_LOAD = 1;
	public static int OPTIMIZE_PREF_FIRST_TO_RETURN = 3;//NOT IMPLEMENTED
	private int approach = 0;
	private NewMU newmu = null;
	private Connection conn = null;
	public static class ResultSingleVehicle {
		int srcSiteId;
		int destId;
		int qLenForOwn;
		long timeToLoadOwn;
		long lastProcessedTime;
		int lastProcessedSiteId;
		int shovelLoadSiteId;
		long shovelLoadTime;
		long optimizeAt;
		int dumperId;
		public ResultSingleVehicle(int dumperId, long optimizeAt) {
			this.dumperId = dumperId;
			this.optimizeAt = optimizeAt;
		}
		public String toString() {
			return " Dumper:"+this.dumperId+" Opt At:"+Misc.longToUtilDate(optimizeAt)+" Site:"+srcSiteId+" Dest:"+destId
			+" OwnQ:"+qLenForOwn+" Time to loadOwn:"+Misc.longToUtilDate(this.timeToLoadOwn)+" LastProcSite:"+Misc.longToUtilDate(lastProcessedSiteId)
			+" BestShovelByFirstLoadId:"+shovelLoadSiteId+" BestShovelByFirstLoadId:"+Misc.longToUtilDate(this.shovelLoadTime)
			;
		}
	}
	public DynOptimizer(Connection conn, NewMU newmu, int approach) {
		this.conn = conn;
		this.newmu = newmu;
		this.approach = approach;
	}
	public ResultSingleVehicle optimizeForSingleVehicle(int dumperId) throws Exception {
		DumperInfo dumper = (DumperInfo) newmu.getVehicleInfo(dumperId);
		if (dumper == null)
			return null;
		Route route = newmu.getRouteInfo(dumper.getAssignedRoute());
		if (route == null)
			return null;
		LoadSite loadSite = newmu.getLoadSite(conn, route.getLoadSite());
		if (loadSite == null)
			return null;
		long baseTS = dumper.getLastLUEventTime();
		int leadSec = (int) (route == null ? 7*60 : route.getDistance()/dumper.getAvgOpSpeedPerKM()*3600);
		int unloadSec = (int) dumper.getAvgUnloadTimeSec();
		ArrayList<Triple<LoadSite, Integer, Predictor>> targetTS = new ArrayList<Triple<LoadSite, Integer, Predictor>>();//2nd = time req for this to get to that site
		
		Collection<LoadSite> allLoadSites = newmu.getAllLoadSites();
		
		for (java.util.Iterator<LoadSite> iter = allLoadSites.iterator(); iter.hasNext();) {
			LoadSite site = iter.next();
			if (site.noDumpersAssigned())
				continue;
			Route alt = newmu.getRoute(conn, site.getId(), route.getUnloadSite(), false);
			double d = alt == null ? Misc.getUndefDouble() : alt.getDistance();
			if (d < 0.0005) {
				Pair<Double, Integer> t1 = Route.estDist(conn, site.getId(), route.getUnloadSite());
				d = t1.first;
			}
			int returnLead = (int) (d/dumper.getAvgOpSpeedPerKM()*3600);
			targetTS.add(new Triple<LoadSite, Integer, Predictor>(site, returnLead+leadSec+unloadSec, new Predictor(site)));
		}
		for (int i=0,is=targetTS.size();i<is;i++) {
			targetTS.get(i).third.bringSiteToTargetTS(targetTS.get(i).second*1000+baseTS, true);
		}
		ArrayList<AnalysisResult> results = new ArrayList<AnalysisResult>();
		for (int i=0,is=targetTS.size();i<is;i++) {
			results.add(targetTS.get(i).third.analyze(dumper));//this gets time To load only but not transit
			results.get(i).timeToLoad = (results.get(i).timeToLoad+targetTS.get(i).second)*1000+baseTS;
		}
		//check if there is a shovel that is qLen = 0 and i
		ResultSingleVehicle retval = new ResultSingleVehicle(dumper.getId(), baseTS);
		int freeShovelIndex = -1;
		long lastProcFree = -1;
		int bestLoadDone = -1;
		long bestLoadDoneTime = -1;
		AnalysisResult me = null;
		for (int i=0,is=results.size();i<is;i++) {
			if (targetTS.get(i).first.getId() == loadSite.getId()) {
				me = results.get(i);
			}
			if (results.get(i).qLenExclMe <= 0) {
				if (lastProcFree <= 0 || results.get(i).latestLoadOut < lastProcFree || targetTS.get(i).first.getId() == loadSite.getId()) {
					freeShovelIndex = i;
					lastProcFree = results.get(i).latestLoadOut;
				}
				if (bestLoadDone < 0 || bestLoadDoneTime > results.get(i).timeToLoad || (bestLoadDoneTime >= results.get(i).timeToLoad+90*1000 && targetTS.get(i).first.getId() == loadSite.getId())) {
					bestLoadDone = i;
					bestLoadDoneTime = results.get(i).timeToLoad;
				}
			}
		}
		retval.qLenForOwn = me.qLenExclMe;
		retval.timeToLoadOwn = me.latestLoadOut;
		retval.lastProcessedSiteId = freeShovelIndex >= 0 ? targetTS.get(freeShovelIndex).first.getId() : Misc.getUndefInt();
		retval.lastProcessedTime = freeShovelIndex >= 0 ? lastProcFree : Misc.getUndefInt();
		retval.shovelLoadSiteId =  targetTS.get(bestLoadDone).first.getId();
		retval.shovelLoadTime = bestLoadDoneTime;
		
		int resultSiteId = loadSite.getId();
		int resultDestId = route.getUnloadSite();
		boolean meAcceptable = me.qLenExclMe < newmu.parameters.OPTIMIZE_Q_THRESHOLD;
		boolean madeDecision = false;
		if (!madeDecision && approach == OPTIMIZE_STICKY) {//give pref to 
			if (meAcceptable) {
				resultSiteId = loadSite.getId();
				madeDecision = true;
			}
		}
		if (!madeDecision ) {
			if (approach != DynOptimizer.OPTIMIZE_PREF_FIRST_TO_LOAD && freeShovelIndex > 0) {
				resultSiteId = targetTS.get(freeShovelIndex).first.getId();
				madeDecision = true;
			}
		}
		if (!madeDecision) {
			if (approach != DynOptimizer.OPTIMIZE_PREF_FIRST_TO_RETURN && bestLoadDone >= 0 ) {//..but first to return not impelemented
				resultSiteId = targetTS.get(bestLoadDone).first.getId();
				madeDecision = true;
			}
		}
		
		
		retval.srcSiteId = resultSiteId;
		retval.destId = resultDestId;
		
		return retval;
	}
	
}

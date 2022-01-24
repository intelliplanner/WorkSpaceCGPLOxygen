package com.ipssi.miningOpt;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.ipssi.gen.utils.Misc;

public class Predictor {
	LoadSite loadSite = null;
	public Predictor(LoadSite loadSite) {
		this.loadSite = loadSite;
		this.lastProcessedAt = loadSite.getLatestProcessedAt();
		try {
			NewMU newmu = loadSite.getOwnerMU();
			loadSite.getReadLock();
			ArrayList<Integer> dumpersAssigned = loadSite.getAssignedDumpers();
			this.processedList = new ArrayList<SimulInfo>(dumpersAssigned.size());
			for (int i=0,is=dumpersAssigned.size();i<is;i++) {
				int dumperId = dumpersAssigned.get(i);
				SimulInfo simul = new SimulInfo(newmu, loadSite, dumperId);
				this.processedList.add(simul);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			loadSite.releaseReadLock();
		}
	}
	public static ArrayList<Predictor> bringSiteToTargetTS(Connection conn, Collection<LoadSite> siteList, long targetTS, boolean doApprox) {
		ArrayList<Predictor> retval = new ArrayList<Predictor>();
		for (Iterator<LoadSite> iter = siteList.iterator(); iter.hasNext();) {
			LoadSite site= iter.next();
			Predictor predictor = new Predictor(site);
			retval.add(predictor);
			
		}
		for (int i=0,is=retval.size(); i<is; i++) {
			retval.get(i).bringSiteToTargetTS(targetTS, doApprox);
		}
		return retval;
	}
	
	public void bringSiteToTargetTS(long targetTS, boolean doApprox) {
		//doApprox = false;
		if (doApprox) {
			for (int i=0,is=processedList.size();i<is;i++) {
				SimulInfo info = processedList.get(i);
				if (!info.reachedTargetTS(targetTS)) {
					SimulInfo orig = (SimulInfo) (info.copy());
					this.bringItemToTargetTSApprox(info, targetTS);
					if (info.isDifferent(orig)) {
						int dbg = 1;
						java.util.Date dt2 = new java.util.Date(targetTS);
					
						dbg++;
						this.bringItemToTargetTSApprox(orig, targetTS);
							
					}
				}
			}
		}
		else {
			this.backAtQ = new ArrayList<SimulInfo>(processedList.size());
			this.backNotAtQ = new ArrayList<SimulInfo>(processedList.size());
			for (int i=processedList.size()-1;i>=0;i--) {
				SimulInfo info = processedList.get(i);
				if (!info.reachedTargetTS(targetTS)) {
					if (info.state == 0)
						this.insertInBackAtQ(info);
					else
						this.insertInBackNotAtQ(info);
					processedList.remove(i);
				}
			}
			this.bringOldToTimeExact(targetTS);
		}
	}
	public int getQLen(int dumperIdIn0) {
		int retval = 0;
		boolean dumperFound = false;
		for (int i=0,is=this.processedList.size();i<is;i++)
			if (this.processedList.get(i).state == 0) {
				retval++;
				if (processedList.get(i).dumper.getId() == dumperIdIn0)
					dumperFound = true;
			}
		if (!dumperFound)
			retval++;
		return retval;
	}
	public void writeBackPrediction() {
		
		for (int i=0,is=this.processedList.size();i<is;i++) {
			SimulInfo info = this.processedList.get(i);
			info.writeBackPrediction(loadSite);
			
		}
	}
	
	public void bringItemToTargetTSApprox(SimulInfo info, long targetTS) {			
		double avgFactor = 1.8;
		int adjLoadTimeSec = (int) (avgFactor*info.loadTimeSec);
		int fullCycle = info.leadTimeSec*2+info.unloadTimeSec+adjLoadTimeSec;
		int gapSec = (int)(targetTS - info.simulGRT)/1000;
		int fullTrips = gapSec/fullCycle;
		
		info.simulGRT += fullTrips*fullCycle*1000;
		gapSec = gapSec - fullTrips*fullCycle;//we will have less than 1 full cycle remaining ..
		if (info.state == 0) {
			int timeNeededToGetToState1 = adjLoadTimeSec;
			int timeNeededToGetToState2 = timeNeededToGetToState1+info.leadTimeSec;
			int timeNeededToGetToState3 = timeNeededToGetToState2+info.unloadTimeSec;
			int timeNeededToGetToState0 = timeNeededToGetToState3+info.unloadTimeSec;
			setLatestProcessingAt(info.simulGRT);
			if (gapSec >= timeNeededToGetToState0) {
				info.entryAt = info.simulGRT+timeNeededToGetToState0*1000;
				setLatestProcessingAt(info.entryAt);
				info.state = 0;
				info.simulGRT = targetTS;
				info.nearnessToSite = 0;
				info.processWaitSec = 0;
			}
			else if (gapSec >= timeNeededToGetToState3) {
				info.simulGRT = targetTS;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState3)/(double)info.leadTimeSec;
				info.state = 3;
				if (info.nearnessToSite >= 1)
					info.nearnessToSite = 0.95;
			}
			else if (gapSec >= timeNeededToGetToState2) {
				info.entryAt = info.simulGRT+timeNeededToGetToState2*1000;
				info.simulGRT = targetTS;
				info.nearnessToSite = 0;
				info.state = 2;
				info.processWaitSec = 0;
			}
			else if (gapSec >= timeNeededToGetToState1) {
				info.simulGRT = targetTS;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState1)/(double)info.leadTimeSec;
				info.state = 1;
				if (info.nearnessToSite >= 1)
					info.nearnessToSite = 0.95;
			}
			else {
				info.simulGRT = targetTS;
			}
		}
		else if (info.state == 1) {
			int left0SecAgo = (int) (info.nearnessToSite*info.leadTimeSec);
			setLatestProcessingAt(info.simulGRT-left0SecAgo);
			int timeNeededToGetToState2 = (int) ((1-info.nearnessToSite)*info.leadTimeSec);
			int timeNeededToGetToState3 = timeNeededToGetToState2+info.unloadTimeSec;
			int timeNeededToGetToState0 = timeNeededToGetToState3+info.leadTimeSec;
			int timeNeededToGetToState1 = timeNeededToGetToState0+adjLoadTimeSec;
			if (gapSec >= timeNeededToGetToState1) {
				setLatestProcessingAt(info.simulGRT+timeNeededToGetToState0*1000);
				info.state = 1;
				info.simulGRT = targetTS;
				info.entryAt = -1;
				info.processWaitSec = 0;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState1)/(double)info.leadTimeSec;
			}
			else if (gapSec >= timeNeededToGetToState0) {
				
				info.state = 0;
				info.entryAt = info.simulGRT + timeNeededToGetToState0*1000;
				setLatestProcessingAt(info.entryAt);
				info.simulGRT = targetTS;
				info.nearnessToSite = 0;
				info.processWaitSec = 0;
			}
			else if (gapSec >= timeNeededToGetToState3) {
				info.simulGRT = targetTS;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState3)/(double)info.leadTimeSec;
				info.state = 3;
				if (info.nearnessToSite >= 1)
					info.nearnessToSite = 0.95;
			}
			else if (gapSec >= timeNeededToGetToState2) {
				info.entryAt = info.simulGRT+timeNeededToGetToState2*1000;
				info.simulGRT = targetTS;
				info.nearnessToSite = 0;
				info.state = 2;
				info.processWaitSec = 0;
			}
			else {
				info.simulGRT = targetTS;
				info.nearnessToSite += (double)(gapSec)/(double)info.leadTimeSec;
				info.state = 1;
				if (info.nearnessToSite >= 1)
					info.nearnessToSite = 0.95;
			}
		}
		else if (info.state == 2) {
			setLatestProcessingAt((info.entryAt <= 0 ? info.simulGRT : info.entryAt) - info.leadTimeSec*1000);
			int timeNeededToGetToState3 = (int) (info.unloadTimeSec -( info.entryAt <= 0 ? 0 : (info.simulGRT-info.entryAt)/1000));
			if (timeNeededToGetToState3 < 0)
				timeNeededToGetToState3 = 0;
			int timeNeededToGetToState0 = timeNeededToGetToState3+info.leadTimeSec;
			int timeNeededToGetToState1 = timeNeededToGetToState0+adjLoadTimeSec;
			int timeNeededToGetToState2 = timeNeededToGetToState1+adjLoadTimeSec;
			if (gapSec >= timeNeededToGetToState2) {
				setLatestProcessingAt(info.simulGRT+timeNeededToGetToState0*1000);
				info.entryAt = info.simulGRT+timeNeededToGetToState2*1000;
				info.simulGRT = targetTS;
				info.nearnessToSite =0;
				info.state = 2;
				info.nearnessToSite =0;
				info.processWaitSec =0;
			}
			else if (gapSec >= timeNeededToGetToState1) {
				setLatestProcessingAt(info.simulGRT+timeNeededToGetToState0*1000);
				info.simulGRT = targetTS;
				info.nearnessToSite =(double)( gapSec-timeNeededToGetToState1)/(double)info.leadTimeSec;
				info.state = 1;
				if (info.nearnessToSite >= 1)
					info.nearnessToSite = 0.95;
			}
			else if (gapSec >= timeNeededToGetToState0) {
				info.entryAt = info.simulGRT+timeNeededToGetToState0*1000;
				setLatestProcessingAt(info.entryAt);
				info.simulGRT = targetTS;
				info.nearnessToSite = 0;
				info.state = 0;
			}
			else if (gapSec >= timeNeededToGetToState3) {
				info.simulGRT = targetTS;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState3)/(double)info.leadTimeSec;
				info.state = 3;
				if (info.nearnessToSite >= 1)
					info.nearnessToSite = 0.95;
			}
			else {
				info.simulGRT = targetTS;
			}
		}//if curr state is 2
		else if (info.state == 3) {
			setLatestProcessingAt(info.simulGRT - (long)(info.nearnessToSite*info.leadTimeSec+info.unloadTimeSec+info.loadTimeSec)*1000);
			int timeNeededToGetToState0 = (int) ((1-info.nearnessToSite)*info.leadTimeSec);
			int timeNeededToGetToState1 = timeNeededToGetToState0+adjLoadTimeSec;
			int timeNeededToGetToState2 = timeNeededToGetToState1+info.leadTimeSec;
			int timeNeededToGetToState3 = timeNeededToGetToState2+info.unloadTimeSec;
			if (gapSec >= timeNeededToGetToState3) {
				setLatestProcessingAt(info.simulGRT +timeNeededToGetToState0*1000);
				info.state = 3;
				info.simulGRT = targetTS;
				info.entryAt = -1;
				info.processWaitSec = 0;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState3)/(double)info.leadTimeSec;
				if (info.nearnessToSite >= 0.95)
					info.nearnessToSite = 0.95;
			}
			else if (gapSec >= timeNeededToGetToState2) {
				setLatestProcessingAt(info.simulGRT +timeNeededToGetToState0*1000);
				info.state = 2;
				info.entryAt = info.simulGRT+timeNeededToGetToState2*1000;
				info.simulGRT = targetTS;
				info.processWaitSec = 0;
				info.nearnessToSite =0;
			}
			else if (gapSec >= timeNeededToGetToState1) {
				setLatestProcessingAt(info.simulGRT +timeNeededToGetToState0*1000);
				info.state = 1;
				info.simulGRT = targetTS;
				info.entryAt = -1;
				info.processWaitSec = 0;
				info.nearnessToSite = (double)(gapSec-timeNeededToGetToState1)/(double)info.leadTimeSec;
				if (info.nearnessToSite >= 1.0)
					info.nearnessToSite = 0.95;
			}
			else if (gapSec >= timeNeededToGetToState0) {
				info.state = 0;
				info.entryAt = info.simulGRT+timeNeededToGetToState0*1000;
				setLatestProcessingAt(info.entryAt);
				info.simulGRT = targetTS;
				info.processWaitSec = 0;
				info.nearnessToSite =0;
			}
			else  {
				info.nearnessToSite += (double) gapSec/(double)info.loadTimeSec;
			}
		}//if in sate 3

	}

	ArrayList<SimulInfo> backAtQ = null; //by entry timing
	ArrayList<SimulInfo> backNotAtQ = null; //no ordering necessary
	ArrayList<SimulInfo> processedList = null;//no ordering necessary
	long lastProcessedAt = -1;
	private void insertInBackAtQ(SimulInfo data) {
		int i=0,is = backAtQ.size();
		boolean torepl = false;
		for (;i<is;) {
			SimulInfo rhs = backAtQ.get(i);
			int cmp = rhs.compareByEntry(rhs);
			if (cmp > 0)
				break;
			else if (cmp == 0)
				break;
		}
		if (torepl)
			backAtQ.set(i, data);
		else if (i <is)
			backAtQ.add(i, data);
		else
			backAtQ.add(data);
	}
	private void insertInBackNotAtQ(SimulInfo data) {
		backNotAtQ.add(data);
	}
	private void insertInProcessedList(SimulInfo data) {
		processedList.add(data);
	}

	public void bringOldToTimeExact(long targetTS) {
		//bring back to Q that are not in Q.. then process Q one at a time and process 
		 while (true) {
			 boolean someProcDone = false;
			 for (int i=backNotAtQ.size()-1;i>=0;i--) {
				SimulInfo minGRT = backNotAtQ.get(i);
				backNotAtQ.remove(i);
				if (minGRT.reachedTargetTS(targetTS)) {
					this.insertInProcessedList(minGRT);
					continue;
				}
				else if (minGRT.state == 0) {
					this.insertInBackAtQ(minGRT);
					continue;
				}
				int gapSec =(int) (( targetTS-minGRT.simulGRT)/1000);
				
				if (minGRT.state == 1) {
					int timeNeededToGetToState2 = (int)((1-minGRT.nearnessToSite)*minGRT.leadTimeSec);
					int timeNeededToGetToState3 =timeNeededToGetToState2+minGRT.unloadTimeSec;
					int timeNeededToGetToState0 =timeNeededToGetToState3+minGRT.leadTimeSec;
					if (gapSec >= timeNeededToGetToState0) {
						minGRT.simulGRT += timeNeededToGetToState0*1000;
						minGRT.state = 0;
						minGRT.entryAt = minGRT.simulGRT;
						minGRT.processWaitSec = 0;
						minGRT.nearnessToSite = 0;
					}
					else if (gapSec >= timeNeededToGetToState3) {
						minGRT.simulGRT += gapSec*1000;
						minGRT.state = 3;
						minGRT.entryAt = -1;
						minGRT.processWaitSec = 0;
						minGRT.nearnessToSite = (gapSec-timeNeededToGetToState3)/minGRT.leadTimeSec;
					}
					else if (gapSec >= timeNeededToGetToState2) {
						minGRT.processWaitSec = 0;
						minGRT.entryAt = minGRT.simulGRT + timeNeededToGetToState2*1000;
						minGRT.simulGRT += gapSec*1000;
						minGRT.state = 2;
						
						minGRT.nearnessToSite = 0;
					}
					else {
						minGRT.processWaitSec = -1;
						minGRT.simulGRT += gapSec*1000;
						minGRT.state = 1;
						minGRT.entryAt = -1;
						minGRT.nearnessToSite = minGRT.nearnessToSite+(double)gapSec/(double)minGRT.leadTimeSec;
						if (minGRT.nearnessToSite >= 1)
							minGRT.nearnessToSite = 0.95;
					}
				}
				else if (minGRT.state == 2) {
					int timeNeededToGetToState2 = minGRT.unloadTimeSec + minGRT.processWaitSec-(int)( (minGRT.simulGRT - minGRT.entryAt)/1000);
					if (timeNeededToGetToState2 < 0)
						timeNeededToGetToState2 = 0;
					int timeNeededToGetToState3 =timeNeededToGetToState2+minGRT.unloadTimeSec;
					int timeNeededToGetToState0 =timeNeededToGetToState3+minGRT.leadTimeSec;
					if (gapSec >= timeNeededToGetToState0) {
						minGRT.simulGRT += timeNeededToGetToState0*1000;
						minGRT.state = 0;
						minGRT.entryAt = minGRT.simulGRT;
						minGRT.processWaitSec = 0;
						minGRT.nearnessToSite = 0;
					}
					else if (gapSec >= timeNeededToGetToState3) {
						minGRT.simulGRT += gapSec*1000;
						minGRT.state = 3;
						minGRT.entryAt = -1;
						minGRT.processWaitSec = 0;
						minGRT.nearnessToSite = (gapSec-timeNeededToGetToState3)/minGRT.leadTimeSec;
					}
					else {
						minGRT.simulGRT += gapSec*1000;
					}
				}
				else if (minGRT.state == 3) {
					int timeNeededToGetToState0 =(int)((1-minGRT.nearnessToSite)*minGRT.leadTimeSec);
					if (gapSec >= timeNeededToGetToState0) {
						minGRT.simulGRT += timeNeededToGetToState0*1000;
						minGRT.state = 0;
						minGRT.entryAt = minGRT.simulGRT;
						minGRT.processWaitSec = 0;
						minGRT.nearnessToSite = 0;
					}
					else {
						minGRT.simulGRT += gapSec*1000;
						minGRT.state = 3;
						minGRT.entryAt = -1;
						minGRT.nearnessToSite = minGRT.nearnessToSite+(double)gapSec/(double)minGRT.leadTimeSec;
						if (minGRT.nearnessToSite >= 1)
							minGRT.nearnessToSite = 0.95;
					}
				}//state == 3
				if (!minGRT.reachedTargetTS(targetTS)) {
					//will be in state 0
					this.insertInBackAtQ(minGRT);
				}
				else {
					this.insertInProcessedList(minGRT);
				}
			}//bringing every one to Q or if targetReached then marked as processed
			for (int i=0,is=this.backAtQ.size();i<is;i++) {
				SimulInfo minGRT = backAtQ.get(i);
				long willCompleteAt = minGRT.entryAt+(minGRT.processWaitSec+minGRT.loadTimeSec)*1000;
				long origGRT = minGRT.simulGRT;
				this.backAtQ.remove(i);
				is--;
				if (willCompleteAt <= targetTS) {
					minGRT.simulGRT = willCompleteAt;
					minGRT.state = 1;
					minGRT.nearnessToSite = 0;
					minGRT.entryAt = -1;
					minGRT.processWaitSec = 0;
				}
				else {
					minGRT.simulGRT = targetTS;
				}
				int deltaTimeElapsedInProcessing =(int)( (minGRT.simulGRT-origGRT)/1000);
				
				for (int j=0,js=backAtQ.size(); j<js;j++) {
					SimulInfo otherIn0 = backAtQ.get(j);
					int meGapRelFirstSec =(int)( otherIn0.simulGRT - origGRT)/1000;
					int addForProc = deltaTimeElapsedInProcessing - meGapRelFirstSec;
					if (addForProc > 0)
						otherIn0.processWaitSec += addForProc;
				}
				if (minGRT.reachedTargetTS(targetTS)) {
					this.insertInProcessedList(minGRT);
				}
				else {//will not be in state 0
					this.insertInBackNotAtQ(minGRT);
				}
			}//for each item in Q
			if (this.backAtQ.size() == 0 && this.backNotAtQ.size() == 0)
				break;
		 }
	}//end of func
	public static class SimulInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		DumperInfo dumper;
		LoadSite loadSite;
		int loadTimeSec;
		int leadTimeSec;
		int unloadTimeSec;
		double nearnessToSite;
		long simulGRT;
		int state;
		long entryAt;
		int processWaitSec = 0;
		int deltaTrips = 0;
		public SimulInfo copy() {
			SimulInfo retval = new SimulInfo(loadSite.getOwnerMU(), loadSite,this.dumper.getId());
			retval.dumper = this.dumper;
			retval.loadSite = this.loadSite;
			retval.loadTimeSec = this.loadTimeSec;
			retval.leadTimeSec = this.leadTimeSec;;
			retval.unloadTimeSec = this.unloadTimeSec;
			retval.nearnessToSite = this.nearnessToSite;
			retval.simulGRT = this.simulGRT;
			retval.state = this.state;
			retval.entryAt = this.entryAt;
			retval.processWaitSec = this.processWaitSec;
			retval.deltaTrips = this.deltaTrips;
			return retval;
		}
		public String toString() {
			return dumper.getName()+" state:"+state+" Wait:"+this.processWaitSec+" near:"+nearnessToSite+" entry:"+Misc.longToUtilDate(entryAt)+" grt:"+Misc.longToUtilDate(simulGRT);
		}
		
		public boolean isDifferent(SimulInfo rhs) {
			return this.state != rhs.state || this.entryAt != rhs.entryAt || this.simulGRT != rhs.simulGRT || !Misc.isEqual(rhs.nearnessToSite,this.nearnessToSite)
			|| this.processWaitSec != rhs.processWaitSec;
			
		}
		public SimulInfo(NewMU newmu, LoadSite loadSite, int dumperId) {
			this.dumper =(DumperInfo) newmu.getVehicleInfo(dumperId);
			this.loadSite = loadSite;
			double capWt = dumper.getCapacityWt();
			double cycs = loadSite.getBlendedCapWt();
			double numCycNeeded = capWt/cycs;
			int adjNumCycNeeded = (int) Math.floor(numCycNeeded);
			if (numCycNeeded-adjNumCycNeeded > 0.25)
				adjNumCycNeeded++;
			this.loadTimeSec = (int) ((adjNumCycNeeded+1)*cycs);//+1 for positioning of dumper .. hack TODO - use param at dumper for spoitting & positioning
			Route route = newmu.getRouteInfo(dumper.getAssignedRoute());
			if (route != null) {
				double dist = route.getDistance();
				this.leadTimeSec = (int) (dist*3600/dumper.getAvgOpSpeedPerKM());
			}
			else 
				this.leadTimeSec = 7*60;
			this.unloadTimeSec = (int) dumper.getAvgUnloadTimeSec();
			int currTripState = dumper.getCurrentLoadStatus();
			this.entryAt = -1;
			this.processWaitSec = 0;
			this.nearnessToSite = 0;
			
			if (currTripState == DumperInfo.L_WAIT || currTripState == DumperInfo.L_BEING_OP) {
				state = 0;
				this.entryAt = dumper.getLastLUEventTime();
			}
			else if (currTripState == DumperInfo.U_WAIT || currTripState == DumperInfo.U_BEING_OP) {
				state = 2;
				this.entryAt = dumper.getLastLUEventTime();
			}
			else if (currTripState == DumperInfo.L_ENROUTE || currTripState == DumperInfo.L_WAIT_AFT) {
				state = 1;
				this.nearnessToSite = dumper.getPercentLegCompleted();
			}
			else if (currTripState == DumperInfo.L_ENROUTE || currTripState == DumperInfo.L_WAIT_AFT) {
				state = 3;
				this.nearnessToSite = dumper.getPercentLegCompleted();
			}
			this.simulGRT = dumper.latestGRT;
		}
		boolean reachedTargetTS(long targetTS) {
			return Predictor.reachedTargetTS(simulGRT, targetTS);
		}
		int compareByEntry(SimulInfo o2) {
			SimulInfo o1 = this;
			int retval = 0;
			long gap = o1.entryAt-o2.entryAt;
			retval = gap < 0 ? -1 : gap > 0 ? 1 :0;
			if (retval == 0) {
				gap = o1.simulGRT - o2.simulGRT;
				retval = gap < 0 ? -1 : gap > 0 ? 1 : 0;
				if (retval == 0) {
					gap = o1.dumper.getId() - o2.dumper.getId();
					retval = gap < 0 ? -1 : gap > 0 ? 1 : 0;
				}
			}
			return retval;
		}
		public void writeBackPrediction(LoadSite loadSite) {
			if (dumper.predicted == null)
				dumper.predicted = new DumperInfo.Predicted();
			DumperInfo.Predicted predicted = new DumperInfo.Predicted();
			Route route = dumper.getOwnerMU().getRouteInfo(dumper.getAssignedRoute());
			UnloadSite unloadSite = dumper.getOwnerMU().getUnloadSite(null, route.getUnloadSite());
			
			
			predicted.simulGRT = simulGRT;
			predicted.currentLoadStatus = state == 0 ? DumperInfo.L_WAIT : state == 1 ? DumperInfo.L_ENROUTE : state == 2 ? DumperInfo.U_WAIT : DumperInfo.U_ENROUTE;
			predicted.deltaTrips = deltaTrips;
			predicted.entryAt = entryAt;
			predicted.processWaitSec = processWaitSec;
			predicted.frac =nearnessToSite;
			
			if (loadSite != null)
				loadSite.getPredictedQueue().remove(dumper.getId());
			if (unloadSite != null)
				unloadSite.getPredictedQueue().remove(dumper.getId());
			
			if (state == 0 && loadSite != null) {
				loadSite.getPredictedQueue().add(new WaitItem(dumper.getId(), this));
			}
			if (state == 2 && unloadSite != null) {
				unloadSite.getPredictedQueue().add(new WaitItem(dumper.getId(), this));
			}
		}
	}
	public static boolean reachedTargetTS(long simulGRT, long targetTS) {
		return targetTS-simulGRT <= 3*60*1000;
	}
	private void setLatestProcessingAt(long ts) {
		if (this.lastProcessedAt < 0 || this.lastProcessedAt < ts)
			this.lastProcessedAt = ts;
	}
	public class AnalysisResult {
		long latestLoadOut = -1;
		long timeToLoad = -1;
		long roundTrip = -1;
		int qLenExclMe = -1;
		int destForBestRoundTripFromSite=-1;
	}
	public AnalysisResult analyze(DumperInfo plusDumper){
		AnalysisResult retval = new AnalysisResult();
		retval.latestLoadOut = this.lastProcessedAt;
		int timeRequired = 0;
		boolean seenMeDumper = false;
		int timeForDumper = 0;
		int qLenExclMe = 0;
		for (int i=0,is=this.processedList.size();i<is;i++) {
			SimulInfo item = this.processedList.get(i);
			if (item.state == 0) {
				if (i == 0) {//for first dumper special stuff
					int timeAlreadySpent = (int)( item.simulGRT-(item.entryAt <= 0 ? 0 : item.entryAt))/1000;
					int timeNeeded = item.loadTimeSec-timeAlreadySpent;
					if (timeNeeded < 0)
						timeNeeded = 0;
					timeRequired += timeNeeded;
				}
				else {
					timeRequired += item.loadTimeSec;
				}
				if (item.dumper.getId() == plusDumper.getId())
					seenMeDumper = true;
				else
					qLenExclMe++;
			}
			if (item.dumper.getId() == plusDumper.getId())
				timeForDumper = item.loadTimeSec;
		}
		if (!seenMeDumper) {
			timeRequired += timeForDumper;
		}
		retval.timeToLoad = timeRequired;
		retval.qLenExclMe = qLenExclMe;
		return retval;
	}
}

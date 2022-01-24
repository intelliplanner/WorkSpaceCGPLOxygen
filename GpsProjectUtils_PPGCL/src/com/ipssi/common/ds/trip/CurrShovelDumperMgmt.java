package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.ipssi.common.ds.trip.CurrDumperInfo.CandidateInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class CurrShovelDumperMgmt {
	public static CurrShovelInfo getCurrShovelInfo(int shovelId) {
		CurrShovelInfo currShovelInfo = null;
		if (!Misc.isUndef(shovelId)) {
			currShovelInfo = g_currShovelInfo.get(shovelId);
			if (currShovelInfo == null) {
				currShovelInfo = new CurrShovelInfo(shovelId);
				g_currShovelInfo.put(shovelId, currShovelInfo);
			}
		}
		return currShovelInfo;
	}
	private static Pair<CurrShovelInfo, CurrDumperInfo> getDumperEntry(int dumperId) {
		return g_dumperMap.get(dumperId);
	}
	private static void setDumperEntry(CurrDumperInfo dumperInfo, CurrShovelInfo shovelInfo) {
		g_dumperMap.put(dumperInfo.getDumperId(), new Pair<CurrShovelInfo, CurrDumperInfo>(shovelInfo, dumperInfo));
	}
	private static void removeDumperEntryFromMap(int dumperId) {
		g_dumperMap.remove(dumperId);
	}
	public static void handleSimpleRemove(int dumperId) {
		Pair<CurrShovelInfo, CurrDumperInfo> currEntry = getDumperEntry(dumperId);
		if (currEntry == null)
			return;
		CurrShovelInfo currShovelInfo = currEntry.first;
		CurrDumperInfo currDumperInfo = currEntry.second;
		CurrShovelDumperMgmt.removeDumperEntryFromMap(dumperId);
		currShovelInfo.removeDumperInfoAndGetNext(dumperId);
	}
	public static ArrayList<ShovelSequence.OverlapTimingChangeInfo> handleRemove(Connection conn, int dumperId) throws Exception {
		Pair<CurrShovelInfo, CurrDumperInfo> currEntry = getDumperEntry(dumperId);
		if (currEntry == null)
			return null;
		CurrShovelInfo currShovelInfo = currEntry.first;
		CurrDumperInfo currDumperInfo = currEntry.second;
		ArrayList<ShovelSequence.OverlapTimingChangeInfo> timingChanges = new ArrayList<ShovelSequence.OverlapTimingChangeInfo>();
		
		CurrShovelDumperMgmt.removeDumperEntryFromMap(currDumperInfo.getDumperId());
		CurrDumperInfo next = currShovelInfo.removeDumperInfoAndGetNext(currDumperInfo.getDumperId());
		if (next != null) {
			CurrShovelDumperMgmt.removeDumperEntryFromMap(next.getDumperId());
			next.getCurrAssignedShovelInfo().removeDumperInfoAndGetNext(next.getDumperId());
			HashMap<Integer, Integer> alreadyProcessed = new HashMap<Integer, Integer>();
			next.prepDumperBeforeBestSelection(conn);
			doAssignmentLocal(next, conn, alreadyProcessed, timingChanges, null);
		}
		return timingChanges;
	}
	public static Triple<ShovelSequenceHolder, ShovelSequence, ArrayList<ShovelSequence.OverlapTimingChangeInfo>> handleAdd(Connection conn, CurrDumperInfo dumperInfo, StringBuilder dbgSB) throws Exception {
		if (dumperInfo.getCandidateShovels().size() == 0) {
			return new Triple<ShovelSequenceHolder, ShovelSequence, ArrayList<ShovelSequence.OverlapTimingChangeInfo>>(null,null,null);
		}
		ArrayList<ShovelSequence.OverlapTimingChangeInfo> timingChanges = new ArrayList<ShovelSequence.OverlapTimingChangeInfo>();
		HashMap<Integer, Integer> alreadyProcessed = new HashMap<Integer, Integer>();
		//remove first ...
		Pair<CurrShovelInfo, CurrDumperInfo> currEntry = getDumperEntry(dumperInfo.getDumperId());
		if (currEntry != null) {
			CurrShovelDumperMgmt.removeDumperEntryFromMap(dumperInfo.getDumperId());
			currEntry.first.removeDumperInfoAndGetNext(dumperInfo.getDumperId());
		}
		boolean okay = doAssignmentLocal(dumperInfo, conn, alreadyProcessed, timingChanges, dbgSB);
		if (!okay)
			return new Triple<ShovelSequenceHolder, ShovelSequence, ArrayList<ShovelSequence.OverlapTimingChangeInfo>>(null,null,null);
		Pair<CurrShovelInfo, CurrDumperInfo> entry = getDumperEntry(dumperInfo.getDumperId());
		ShovelSequence bestShovelSeq = entry.second.getSelectedShovelSeq();
		ShovelSequenceHolder bestShovel = ShovelSequenceHolder.getShovelInfo(entry.first.getShovelId());		
		return new  Triple<ShovelSequenceHolder, ShovelSequence, ArrayList<ShovelSequence.OverlapTimingChangeInfo>>(bestShovel, bestShovelSeq, timingChanges);
	}
	
	private static boolean doAssignmentLocal(CurrDumperInfo dumperInfo, Connection conn, HashMap<Integer, Integer> alreadyProcessed, ArrayList<ShovelSequence.OverlapTimingChangeInfo> timingChanges, StringBuilder dbgSB) throws Exception {
		if (alreadyProcessed.containsKey(dumperInfo.getDumperId()))
			return true;
		alreadyProcessed.put(dumperInfo.getDumperId(), dumperInfo.getDumperId());
		//1. getBest
		CandidateInfo best = null;
		ArrayList<CandidateInfo> candidates = dumperInfo.getCandidateShovels();
		if (dbgSB != null) {
			dbgSB.append("CurrProc:").append(dumperInfo.getDumperId()).append("\n");
		}
		for (int i=0,is=candidates == null ? 0 : candidates.size(); i<is;i++) {
			CandidateInfo candidate = candidates.get(i);
			//betterThan(CandidateInfo other, Connection conn, int prevGuessedShovelId, int currManualAssignedShovelId, int prevTripShovelId)
			if (dbgSB != null) {
				dbgSB.append("  New Shovel:").append(candidate.getCurrShovelInfo()).append("\n").append("       ShovelSeq:").append(candidate.getSeq()).append("\n");
			}
			if (best == null || candidate.betterThan(best, conn, dumperInfo.getPrevShovelId()
					,dumperInfo.getManualAssignedShovelId(), dumperInfo.getPrevTripShovelId())) {
				best = candidate;
				if (dbgSB != null) {
					dbgSB.append("      SelectedAsBest").append("\n");
				}
			}
		}
		
		CurrShovelInfo bestShovel = best.getCurrShovelInfo();
		ShovelSequence bestSeq = best.getSeq();
		if (bestSeq.getDumperBestQuality() < ShovelSequence.LBLE_NOEXCLOAD_MAYBESTRIKE)
			return false;
		boolean considerLoaded = bestSeq.getCycleCount() >= ShovelSequence.PREFERRED_SHOVEL_CYCLES;
		dumperInfo.setSelectedShovelSeq(bestSeq);
		dumperInfo.setCurrAssignedShovelInfo(bestShovel);
		CurrShovelDumperMgmt.setDumperEntry(dumperInfo, bestShovel);
		dumperInfo.setState(considerLoaded ? CurrDumperInfo.StateEnum.CONSIDER_LOADED : best.isFeasible() ? CurrDumperInfo.StateEnum.BEING_LOADED : CurrDumperInfo.StateEnum.WAITING);
		ArrayList<CurrDumperInfo> dumpers = bestShovel.getDumpers();
		dumperInfo.setPrevShovelId(bestShovel.getShovelId());
		int addAt = bestShovel.getAddAtPos(dumperInfo);
		if (addAt == dumpers.size()) {
			dumpers.add(dumperInfo);
		}
		else {
			dumpers.add(addAt, dumperInfo);
		}
		//apply change timing of left hand side (deferred in overlapTimingChangeInfo
		if (best.getPriorSeqActEnd() > 0 && best.getPriorSeqNeedingAdjustment() != null && best.getPriorSeqActEnd() < Long.MAX_VALUE) {
			ShovelSequence.OverlapTimingChangeInfo timingChange = null;
			timingChange = ShovelSequence.OverlapTimingChangeInfo.getChangeInfo(best.getPriorSeqNeedingAdjustment().getActStartTS(), best.getPriorSeqActEnd(), best.getPriorSeqNeedingAdjustment(), Misc.getUndefInt());
			timingChanges.add(timingChange);
		}
		//"unfirm" the rhs and "reput"
		for (int i=addAt+1; i<dumpers.size();i++) {
			CurrDumperInfo moreDumper = dumpers.get(i);
			if (moreDumper.getState() == CurrDumperInfo.StateEnum.BEING_LOADED && !alreadyProcessed.containsKey(moreDumper.getDumperId())) {
				CurrShovelDumperMgmt.removeDumperEntryFromMap(moreDumper.getDumperId());
				dumpers.remove(i);
				i--;
				moreDumper.prepDumperBeforeBestSelection(conn);
				//TODO recalc CandidateInfo
				if (dbgSB != null) {
					dbgSB.append("InnerCurrProc:").append(dumperInfo.getDumperId()).append("\n");
				}

				doAssignmentLocal(moreDumper, conn, alreadyProcessed, timingChanges, dbgSB);
			}
			else {
				break;
			}
		}
		return true;
	}
	private ArrayList<CandidateInfo> getSortedCandidateSubsetNotImplemented(CurrDumperInfo dumper) {
		//1. check if all have not yet started
		
		ArrayList<CandidateInfo> candidates = dumper.getCandidateShovels();
		
		
		int manualAssigned = dumper.getManualAssignedShovelId();
		ArrayList<CandidateInfo> retval = new ArrayList<CandidateInfo>();
		
		int highestLevelQ = -1;
		int highestQ = -1;
		boolean seenFeasible = false;
		CandidateInfo manualAssignedCandidate = null;
		for (int i=0,is=candidates.size();i<is;i++) {
			CandidateInfo candidate = candidates.get(i);
			ShovelSequence seq = candidate.getSeq();
			CurrShovelInfo currShovelInfo = candidate.getCurrShovelInfo();
			byte q = seq.getDumperBestQuality();
			int qlevel = seq.getNearBLEEtcQLevel(q);
			seenFeasible = seenFeasible || candidate.isFeasible();
			if (currShovelInfo.getShovelId() == manualAssigned) {
				retval.add(candidate);
				manualAssignedCandidate = candidate;
			}
			if (qlevel > highestLevelQ)
				highestLevelQ = qlevel;
			if (q > highestQ)
				highestQ = q;
		}
		boolean done = manualAssignedCandidate != null && highestLevelQ == ShovelSequence.getNearBLEEtcQLevel(manualAssignedCandidate.getSeq().getDumperBestQuality());
		if (done)
			return retval;
		return retval;
	}
	private void doReassignmentGlobalNotImplemented(CurrShovelInfo startShovelInfo, CurrDumperInfo startDumperInfo) {
		//initDumperInfo may or may not be in the list
		boolean startDumperInList = startShovelInfo != null && startDumperInfo != null;
		if (startDumperInfo == null) {
			if (startShovelInfo != null && startShovelInfo.getDumpers().size() > 0)
				startDumperInfo =startShovelInfo.getDumpers().get(0);
		}
		if (startDumperInfo == null) {
			return; //TODO later
		}
		HashMap<Integer, CurrShovelInfo> seenShovels = new HashMap<Integer, CurrShovelInfo>();
		HashMap<Integer, CurrDumperInfo> seenDumpers = new HashMap<Integer, CurrDumperInfo>();
		
		getImpactedShovelsAndDumpersNotChecked(seenShovels, seenDumpers, startDumperInfo) ;
		Collection<CurrShovelInfo> shovelsList = seenShovels.values();
		Collection<CurrDumperInfo> dumpersList = seenDumpers.values();
		//clear up current assignment ..
		for (Iterator<CurrShovelInfo> iter = shovelsList.iterator();iter.hasNext();) {
			CurrShovelInfo s = iter.next();
			s.clearDumpers();
		}
		
		ArrayList<CurrDumperInfo> sortedDumpersList = new ArrayList<CurrDumperInfo>();
		for (Iterator<CurrDumperInfo> iter = dumpersList.iterator();iter.hasNext();) {
			CurrDumperInfo d = iter.next();
			int addAt = 0;
			for (int is = sortedDumpersList.size(); addAt < is && sortedDumpersList.get(addAt).getStartTS() < d.getStartTS();addAt++) {
				//do nothing
			}
			if (addAt == sortedDumpersList.size())
				sortedDumpersList.add(d);
			else
				sortedDumpersList.add(addAt, d);
		}
		for (int i=0,is=sortedDumpersList.size();i<is;i++) {
			CurrDumperInfo d = sortedDumpersList.get(i);
		//	Pair<CurrShovelInfo, ShovelSequence> best = d.getLocalBest();
		//	best.first.setDumperWithShovelSequence(d, best.second);
		}
	}
	private static void getImpactedShovelsAndDumpersNotChecked(HashMap<Integer, CurrShovelInfo> seenShovels, HashMap<Integer, CurrDumperInfo> seenDumpers, CurrDumperInfo starter) {
		if (seenDumpers.containsKey(starter.getDumperId()))
			return;
		seenDumpers.put(starter.getDumperId(), starter);
		ArrayList<CandidateInfo> candidateList = starter.getCandidateShovels();
		for (int i=0,is=candidateList == null ? 0 : candidateList.size(); i<is;i++) {
			CandidateInfo candidate = candidateList.get(i);
			CurrShovelInfo currShovelInfo = candidate.getCurrShovelInfo();
			if (!seenShovels.containsKey(currShovelInfo.getShovelId())) {
				seenShovels.put(currShovelInfo.getShovelId(), currShovelInfo);
				getImpactedShovelsAndDumpersNotChecked(seenShovels, seenDumpers, starter);
				ArrayList<CurrDumperInfo> dassigned = currShovelInfo.getDumpers();
				for (int j=0,js=dassigned == null ? 0 : dassigned.size();j<js;j++) {
					CurrDumperInfo dump = dassigned.get(j);
					if (!seenDumpers.containsKey(dump.getDumperId())) {
						seenDumpers.put(dump.getDumperId(), dump);
						getImpactedShovelsAndDumpersNotChecked(seenShovels, seenDumpers, dump);
					}
				}
			}
		}
	}
	
	private static ConcurrentHashMap<Integer, CurrShovelInfo> g_currShovelInfo = new ConcurrentHashMap<Integer, CurrShovelInfo>();
	private static ConcurrentHashMap<Integer, Pair<CurrShovelInfo, CurrDumperInfo>> g_dumperMap = new ConcurrentHashMap<Integer, Pair<CurrShovelInfo, CurrDumperInfo>>();
	private static ReentrantLock lockForSelection = new ReentrantLock();
	public static void lockSel() {
		lockForSelection.lock();
	}
	public static void unlockSel() {
		lockForSelection.unlock();
	}
}

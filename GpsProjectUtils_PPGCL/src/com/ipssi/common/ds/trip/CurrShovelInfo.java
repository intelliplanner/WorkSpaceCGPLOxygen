package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.cache.NewExcLoadEventMgmt;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class CurrShovelInfo {
	//To read - get readLock in CurrShovelDumperMgmt
	public int getCleaningSec(int numEnroute, int avgCleaningSec, int cleanAfterCycle) {
		return avgCleaningSec/cleanAfterCycle;//TODO
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(shovelId).append(":");
		for (int i=0,is=this.dumpers.size();i<is;i++) {
			sb.append("[");
			sb.append(this.dumpers.get(i).toStringShort());
			sb.append("]");
		}
		return sb.toString();
	}
	public int getAddAtPos(CurrDumperInfo dumperInfo) {
		return getAddAtPos(dumperInfo.getStartTS());
	}
	public int getAddAtPos(long startTS) {
		int addAt = 0;
		for (int is=dumpers.size();addAt < is && dumpers.get(addAt).getStartTS() < startTS;addAt++) {
			//do nothing
		}
		return addAt;
	}
	public MiscInner.Pair getNumWaitingAndCyclesOfBeingLoaded() {//eventually should be replaced to get actual loadSec (need, shovel, avg etc
		int waiting = 0;
		int cyclesOfBeingLoaded = 0;
		for (int i=0,is=this.dumpers.size();i<is;i++) {
			CurrDumperInfo dumper = this.dumpers.get(i);
			if (dumper.getState() == CurrDumperInfo.StateEnum.BEING_LOADED) {
				cyclesOfBeingLoaded = dumper.getNumCycles();
			}
			else if (dumper.getState() == CurrDumperInfo.StateEnum.WAITING) {
				waiting++;
			}
		}
		return new MiscInner.Pair(waiting, cyclesOfBeingLoaded);
	}
	public CurrShovelInfo(int shovelId) {
		this.shovelId = shovelId;
	}
	public CurrDumperInfo getCurrDumperInfo(int dumperId) {
		for (int i=0,is=this.dumpers.size();i<is;i++) {
			if (this.dumpers.get(i).getDumperId() == dumperId)
				return this.dumpers.get(i);
		}
		return null;
	}
	public CurrDumperInfo removeDumperInfoAndGetNext(int dumperId) {
		for (int i=0,is=this.dumpers.size();i<is;i++) {
			if (this.dumpers.get(i).getDumperId() == dumperId) {
				CurrDumperInfo retval = this.dumpers.get(i);
				this.dumpers.remove(i);
				if (i < dumpers.size())
					return this.dumpers.get(i);
			}
		}
		return null;
	}
	
	public ArrayList<CurrDumperInfo> getDumpers() {
		return this.dumpers;
	}
	protected Pair<Long, ShovelSequence> getTSOfLastOtherLoadCycle(long tsStart, ShovelSequenceHolder shovelInfo, NewExcLoadEventMgmt loadEvents, Connection conn) {
		//first = the TS of last cycle that was assigned elsewhere - if no such sequence that precludes then 0, if all cycles 'used' then MAX_VALUE
		//second = the shovelSequence
		//if there is a current sequence immediately before this and whose cycleCount < PreferredShovelCycleCount, then this sequence cannot have cycles
		//    else we will shift the end of curr to left so that we have preferred shovel cycle count
		//if there is no current sequence before this then will look at ended sequence and do the same for that ..
		//if other has noValidActivity then will presume that we cannot start load until
		long first = 0;
		ShovelSequence second = null;
		CurrDumperInfo immBefore = null;
		long leftEndTSBeforeSecond = 0;
		CurrDumperInfo priorToImmBefore = null;
		ShovelSequence seqBeforeSecond = null;
		for (int i=0,is=this.dumpers.size();i<is;i++) {
			if (this.dumpers.get(i).getStartTS() >= tsStart)
				break;
			priorToImmBefore = immBefore;
			immBefore = this.dumpers.get(i);
		}
		if (immBefore != null) {
			if (immBefore.getNumCycles() > ShovelSequence.PREFERRED_SHOVEL_CYCLES) {
				second = immBefore.getSelectedShovelSeq();
				if (priorToImmBefore == null) {
					leftEndTSBeforeSecond = shovelInfo.getLeftEndTSBefore(second.getActStartTS());
				}
				else if (!priorToImmBefore.getSelectedShovelSeq().strictNoLoad()) {
					leftEndTSBeforeSecond = priorToImmBefore.getSelectedShovelSeq().getActEndTS(); 
				}
			}
			else {
				leftEndTSBeforeSecond = Long.MAX_VALUE;
			}
		}
		else {
			second = shovelInfo.getLatestOverlapping(Misc.getUndefInt(), tsStart);
			if (second != null)
				leftEndTSBeforeSecond = shovelInfo.getLeftEndTSBefore(second.getActStartTS());
		}
		if (leftEndTSBeforeSecond == Long.MAX_VALUE) {
			first = Long.MAX_VALUE;
			second = null;
		}
		else if (second != null) {
			first = second.getMinFeasibleLeftOfEnd(tsStart, loadEvents, conn, leftEndTSBeforeSecond);
		}
		else {
			first = 0;
		}
		return new Pair<Long, ShovelSequence>(first, second);
	}
	public int getShovelId() {
		return this.shovelId;
	}
	public void clearDumpers() {
		this.dumpers.clear();
	}
	private int shovelId;
	private ArrayList<CurrDumperInfo> dumpers = new ArrayList<CurrDumperInfo>();
}

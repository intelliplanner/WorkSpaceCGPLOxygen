package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ipssi.cache.ExcLoadEvent;
import com.ipssi.cache.NewBLEMgmt;
import com.ipssi.cache.NewExcLoadEventMgmt;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;

public class CurrDumperInfo {
	public enum StateEnum {
		WAITING((byte)0), BEING_LOADED((byte)1), CONSIDER_LOADED((byte)2);
		byte ordinal;
		private StateEnum(byte ordinal) {
			this.ordinal = ordinal;
		}
		public String toString() {
			return ordinal == 0 ? "W" : ordinal == 1 ? "B" : "L";
		}
	}
	public static String getDate(SimpleDateFormat sdf, long ts) {
		return ts <= 0 ? "null" : sdf.format(Misc.longToUtilDate(ts));
	}
	public String toStringFull() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append(this.dumperId).append(" Cycles:").append(this.getNumCycles())
		.append(this.state.toString()).append(" St:").append(getDate(sdf, this.getStartTS()))
		.append(" ASt:").append(getDate(sdf, this.getActStartTS()))
		.append(" AEn:").append(getDate(sdf, this.getActEndTS()))
		.append(" Cand:").append(this.candidateShovels)
		.append(" Shovel:").append(this.currAssignedShovelInfo == null ? Misc.getUndefInt() : this.currAssignedShovelInfo.getShovelId())
		;
		return sb.toString();
	}
	public String toStringShort() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append(this.dumperId).append(" Cycles:").append(this.getNumCycles())
		.append(this.state.toString()).append(" St:").append(getDate(sdf, this.getStartTS()))
		.append(" ASt:").append(getDate(sdf, this.getActStartTS()))
		.append(" AEn:").append(getDate(sdf, this.getActEndTS()))
		;
		return sb.toString();
	}
	public String toString() {
		return toStringFull();
	}
	public long getStartTS() {
		return this.startTS;
	}
	
	public byte getNumCycles() {
		return this.selectedShovelSeq == null ? Misc.getUndefByte() : this.selectedShovelSeq.getCycleCount();
	}
	public long getActStartTS() {
		return this.selectedShovelSeq == null ? 0: this.selectedShovelSeq.getActStartTS();
	}
	public long getActEndTS() {
		return this.selectedShovelSeq == null ? 0 : this.selectedShovelSeq.getActEndTS();
	}
	public CurrDumperInfo.StateEnum getState() {
		return this.state;
	}
	private long startTS = 0;
	private int manualAssignedShovelId = Misc.getUndefInt();
	private int prevShovelId = Misc.getUndefInt();
	private int prevTripShovelId = Misc.getUndefInt();
	private StateEnum state = StateEnum.WAITING;
	private int dumperId = Misc.getUndefInt();
	private ShovelSequence selectedShovelSeq = null;
	private CurrShovelInfo currAssignedShovelInfo = null;
	public void setSelectedShovelSeq(ShovelSequence selectedShovelSeq) {
		this.selectedShovelSeq = selectedShovelSeq;
	}
	public int getDumperId() {
		return this.dumperId;
	}
	public CurrDumperInfo(int dumperId, long startTS) {
		this.dumperId = dumperId;
		this.startTS = startTS;
	}
	public ShovelSequence getSelectedShovelSeq() {
		return selectedShovelSeq;
	}
	public CurrShovelInfo getCurrAssignedShovelInfo() {
		return this.currAssignedShovelInfo;
	}
	
	public CandidateInfo addCandidateInfo(ShovelSequence shovelSeq, int shovelId, ArrayList<MiscInner.PairShortBool> detailedBLERead) {
		CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(shovelId);
		CandidateInfo retval = new CandidateInfo(currShovelInfo, shovelSeq, detailedBLERead);
		boolean added = false;
		for (int i=0,is=this.candidateShovels.size();i<is;i++) 
			if (candidateShovels.get(i).getCurrShovelInfo().getShovelId() == shovelId) {
				candidateShovels.set(i, retval);
				added = true;
				break;
			}
		
		if (!added) 
			candidateShovels.add(retval);
		return retval;
	}
	public int getManualAssignedShovelId() {
		return this.manualAssignedShovelId;
	}
	public int getPrevShovelId() {
		return this.prevShovelId;
	}
	public int getPrevTripShovelId() {
		return this.prevTripShovelId;
	}
	public void setManualAssignedShovelId(int shovelId) {
		this.manualAssignedShovelId = shovelId;
	}
	public void setPrevShovelId(int shovelId) {
		this.prevShovelId = shovelId;
	}
	public void setPrevTripShovelId(int shovelId) {
		this.prevTripShovelId = shovelId;
	}
	public ArrayList<CandidateInfo> getCandidateShovels() {
		return this.candidateShovels;
	}
	public void clearCandidateShovels() {
		this.candidateShovels.clear();
	}
	public void setState(CurrDumperInfo.StateEnum val) {
		this.state = val;
	}
	public void setCurrAssignedShovelInfo(CurrShovelInfo currAssignedShovelInfo) {
		this.currAssignedShovelInfo = currAssignedShovelInfo;
	}
	public void prepDumperBeforeBestSelection(Connection conn) throws Exception {
		StringBuilder sb = null;
		for (int i=0,is=candidateShovels.size();i<is;i++) {
			CandidateInfo candidate = candidateShovels.get(i);
			int shovelId = candidate.getCurrShovelInfo().getShovelId();
			ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(shovelId);
			NewBLEMgmt bleEvents = NewBLEMgmt.getBLEReadList(conn, shovelId);
			NewExcLoadEventMgmt loadEvents = NewExcLoadEventMgmt.getLoadEventList(conn, shovelId);
			NewVehicleData strikeVDT = null;
			VehicleDataInfo shovelVDF = VehicleDataInfo.getVehicleDataInfo(conn, shovelId,true, false);
			NewVehicleData shovelIdling = shovelVDF.getDataList(conn, shovelId, Misc.EXC_IDLING_DIM_ID, true); 
			candidate.updateCandidateInfo(conn, shovelInfo, null, bleEvents, loadEvents, shovelIdling, strikeVDT, sb);
		}
	}
	private ArrayList<CandidateInfo> candidateShovels = new ArrayList<CandidateInfo>();
	public static class CandidateInfo {
		private CurrShovelInfo currShovelInfo;
		private ShovelSequence seq;
		private boolean feasible;
		private ArrayList<MiscInner.PairShortBool> bleSeen = null;
		private long priorSeqActEnd = 0;
		private ShovelSequence priorSeqNeedingAdjustment = null;
		public String toStringShort() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			StringBuilder sb = new StringBuilder(); 
			sb.append("S:").append(this.currShovelInfo.getShovelId()).append(" Q:").append(this.seq.getDumperBestQuality())
			.append(" SC:").append(this.seq.getCycleCount())
			.append(" f:").append(this.feasible)
			.append(" Prior:").append(getDate(sdf,priorSeqActEnd))
			.append(" PriorSeq:").append(priorSeqNeedingAdjustment == null ? Misc.getUndefInt() : priorSeqNeedingAdjustment.getDumperId())
			.append(", ").append(getDate(sdf,priorSeqNeedingAdjustment == null ? 0: priorSeqNeedingAdjustment.getActEndTS()))
			;
			return sb.toString();
		}
		public String toString() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			StringBuilder sb = new StringBuilder(); 
			sb.append("S:").append(this.currShovelInfo.getShovelId()).append(" Q:").append(this.seq.getDumperBestQuality())
			.append(" SC:").append(this.seq.getCycleCount())
			.append(" f:").append(this.feasible)
			.append(" Prior:").append(getDate(sdf,priorSeqActEnd))
			.append(" PriorSeq:").append(priorSeqNeedingAdjustment == null ? Misc.getUndefInt() : priorSeqNeedingAdjustment.getDumperId())
			.append(", ").append(getDate(sdf,priorSeqNeedingAdjustment == null ? 0: priorSeqNeedingAdjustment.getActEndTS()))
			.append(" FullSeq:").append(this.seq);
			;
			return sb.toString();
		}
		public void updateCandidateInfo(Connection conn, ShovelSequenceHolder shovelInfo, ShovelSequence shovelSeq, NewBLEMgmt bleEvents, NewExcLoadEventMgmt loadEvents, NewVehicleData shovelIdling, NewVehicleData strikeVDT, StringBuilder sb) throws Exception {
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
			if (shovelSeq == null) {
				shovelSeq = this.getSeq();
				shovelSeq.quiclResetToInit();
			}
			
			long tsStart = shovelSeq.getStartTS();
			int dumperId = shovelSeq.getDumperId();
			int shovelId = shovelInfo.getShovelId();
			CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(shovelId);
			Pair<Long, ShovelSequence> otherPrior = currShovelInfo.getTSOfLastOtherLoadCycle(shovelSeq.getStartTS(), shovelInfo, loadEvents, conn);
			long priorMilli = otherPrior.first;
			boolean feasible = false;
			if (priorMilli >= 0 && priorMilli < Long.MAX_VALUE) {
				if (priorMilli > 0) {
					shovelSeq.setActStartTS(priorMilli+1000);
				}
				feasible = shovelSeq.updateWithLoadEvent(conn, shovelIdling, strikeVDT, loadEvents, bleEvents, priorMilli > 0, false, true, true, null);
				if (feasible && shovelSeq.getCycleCount() == 0) {
					feasible = false;
					priorMilli = Long.MAX_VALUE;
					otherPrior.second = null;
				}
			}
			else {
				shovelSeq.setHasValidExcEventsAroundPeriod(loadEvents.isWorkingInWindow(conn, shovelSeq.getEndTS()));
			}
			if (!feasible) {
				byte zeroByte = 0;
				if (shovelSeq.getActStartTS() > shovelSeq.getEndTS())
					shovelSeq.setActStartTS(shovelSeq.getStartTS());
				shovelSeq.setEndActLoadRelStartSec(shovelSeq.getSecGap(shovelSeq.getActStartTS()));
				shovelSeq.setStartLoadEventRelStartSec(Misc.getUndefShort());
				shovelSeq.setEndLoadEventRelStartSec(Misc.getUndefShort());
				shovelSeq.setCycleCount(zeroByte);
				shovelSeq.setCurrLeftInCount(zeroByte);
				shovelSeq.setCurrRightInCount(zeroByte);
			}
			if (feasible && otherPrior.second != null) {
				long otherActEnd = otherPrior.second.getActEndTS();
				if (otherActEnd == priorMilli || otherActEnd < shovelSeq.getActStartTS()) {
					priorMilli = 0;
					otherPrior.second = null;
				}
			}
			this.feasible = feasible;
			this.priorSeqActEnd = priorMilli;
			this.priorSeqNeedingAdjustment = otherPrior.second;
		}
		public CandidateInfo(CurrShovelInfo currShovelInfo, ShovelSequence seq, ArrayList<MiscInner.PairShortBool> bleSeen) {
			this.currShovelInfo = currShovelInfo;
			this.seq = seq;
			this.bleSeen = bleSeen;
		}
		
		public ArrayList<MiscInner.PairShortBool> getBleSeen() {
			return bleSeen;
		}

		public CurrShovelInfo getCurrShovelInfo() {
			return currShovelInfo;
		}

		public ShovelSequence getSeq() {
			return seq;
		}

		public boolean isFeasible() {
			return feasible;
		}

		public long getPriorSeqActEnd() {
			return priorSeqActEnd;
		}

		public ShovelSequence getPriorSeqNeedingAdjustment() {
			return priorSeqNeedingAdjustment;
		}
		
		public boolean betterThan(CandidateInfo other, Connection conn, int prevGuessedShovelId, int currManualAssignedShovelId, int prevTripShovelId) {
			ShovelSequence meSeq = this.getSeq();
			ShovelSequence otherSeq = other.getSeq();
			int toSwap = ShovelSequence.meBetterThanExt(meSeq.isHasValidExcEventsAroundPeriod(), meSeq.getCycleCount(), meSeq.getDumperBestQuality(), meSeq.getDist5(), meSeq.getActEndTS(), meSeq.getEndTS(), meSeq.getActStartTS()
					, otherSeq.isHasValidExcEventsAroundPeriod(), otherSeq.getCycleCount(), otherSeq.getDumperBestQuality(), otherSeq.getDist5(), otherSeq.getActEndTS(), otherSeq.getEndTS(), otherSeq.getActStartTS(), prevGuessedShovelId, currManualAssignedShovelId, prevTripShovelId, this.getCurrShovelInfo().getShovelId(), other.getCurrShovelInfo().getShovelId(), !meSeq.isStopEnded(), conn);
			if (toSwap == 0) {
				int meAddAt = this.getCurrShovelInfo().getAddAtPos(meSeq.getStartTS());
				int otherAddAt = other.getCurrShovelInfo().getAddAtPos(otherSeq.getStartTS());
				if (meAddAt < otherAddAt)
					toSwap = 1;
				else if (meAddAt > otherAddAt)
					toSwap = -1;
			}
			return toSwap > 0;
		}
	}
	
}

package com.ipssi.common.ds.trip;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.ipssi.gen.utils.Misc;

public class UnfirmedBy implements Comparable<UnfirmedBy>, Serializable {
	private static final long serialVersionUID = 1L;
	public long unfirmedSeqStartTS;
	public long unfirmedSeqEndTS;
	public long unfirmedBySeqStartTS;
	public long unfirmedBySeqEndTS;
	public int unfirmedDumperId;
	public int unfirmedByDumperId;
	public long unfirmedSeqActStartTS;
	public long unfirmedSeqActEndTS;
	public byte count = 0;
	public boolean unfirmedEnded = false;
	public boolean unfirmedByEnded = false;
	public String toString() {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		sb.append("U:").append(this.unfirmedDumperId).append(" at:" ).append(sdf.format(new java.util.Date(this.unfirmedSeqStartTS)))
		.append(" to:" ).append(sdf.format(new java.util.Date(this.unfirmedSeqEndTS)))
		.append(" Act:").append(this.unfirmedSeqActStartTS <= 0 ? "null" : sdf.format(new java.util.Date(this.unfirmedSeqActStartTS))).append(" - ").append(this.unfirmedSeqActEndTS <= 0 ? "null" : sdf.format(new java.util.Date(this.unfirmedSeqActEndTS)))
		.append(" By:").append(this.unfirmedByDumperId).append(" At:").append(sdf.format(new java.util.Date(this.unfirmedBySeqStartTS)))
		.append(" to:" ).append(sdf.format(new java.util.Date(this.unfirmedBySeqEndTS)))
		;
		return sb.toString();
	}
	public UnfirmedBy(long ts) {
		this.unfirmedSeqStartTS = ts;
	}
	public UnfirmedBy(long unfirmedSeqStartTS, int unfirmedDumperId, long unfirmedBySeqStartTS,  int unfirmedByDumperId, long unfirmedSeqEndTS, long unfirmedBySeqEndTS, long unfirmedSeqActStartTS, long unfirmedSeqActEndTS) {
		super();
		this.unfirmedSeqStartTS = unfirmedSeqStartTS;
		this.unfirmedBySeqStartTS = unfirmedBySeqStartTS;
		this.unfirmedSeqActStartTS = unfirmedSeqActStartTS;
		this.unfirmedSeqActEndTS = unfirmedSeqActEndTS;
		this.unfirmedDumperId = unfirmedDumperId;
		this.unfirmedByDumperId = unfirmedByDumperId;
		this.unfirmedSeqEndTS = unfirmedSeqEndTS;
		this.unfirmedBySeqEndTS = unfirmedBySeqEndTS;
	}	
	public int compareTo(UnfirmedBy rhs) {
		long res = this.unfirmedSeqStartTS - rhs.unfirmedSeqStartTS;
		if (res == 0)
			res = this.unfirmedDumperId < rhs.unfirmedDumperId ? -1 : this.unfirmedDumperId == rhs.unfirmedDumperId ? 0 : 1;
		if (res == 0 && !ShovelSequenceHolder.g_doStartOnlyCompare)
			res = this.unfirmedSeqEndTS - rhs.unfirmedSeqEndTS;
		if (res == 0)
			res = this.unfirmedBySeqStartTS - rhs.unfirmedBySeqStartTS ;
		if (res == 0 && !ShovelSequenceHolder.g_doStartOnlyCompare)
			res = this.unfirmedBySeqEndTS - rhs.unfirmedBySeqEndTS ;
		if (res == 0)
			res = this.unfirmedByDumperId < rhs.unfirmedByDumperId ? -1 : this.unfirmedByDumperId == rhs.unfirmedByDumperId ? 0 : 1;
		return res < 0 ? -1 : res > 0 ? 1 : 0;
	}

	public long getUnfirmedSeqStartTS() {
		return unfirmedSeqStartTS;
	}

	public void setUnfirmedSeqStartTS(long unfirmedSeqStartTS) {
		this.unfirmedSeqStartTS = unfirmedSeqStartTS;
	}

	public long getUnfirmedBySeqStartTS() {
		return unfirmedBySeqStartTS;
	}

	public void setUnfirmedBySeqStartTS(long unfirmedBySeqStartTS) {
		this.unfirmedBySeqStartTS = unfirmedBySeqStartTS;
	}

	public int getUnfirmedDumperId() {
		return unfirmedDumperId;
	}

	public void setUnfirmedDumperId(int unfirmedDumperId) {
		this.unfirmedDumperId = unfirmedDumperId;
	}

	public int getUnfirmedByDumperId() {
		return unfirmedByDumperId;
	}

	public void setUnfirmedByDumperId(int unfirmedByDumperId) {
		this.unfirmedByDumperId = unfirmedByDumperId;
	}

	

}

package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.ResultSet;

import com.ipssi.gen.utils.Misc;

public class EventDismiss  implements Comparable {
	private int id;//vehicleId
	private int type; //0 = vehicle_id, 1 = site
	private long atTime;
	public int compareTo(Object o) {
		EventDismiss rhs = (EventDismiss) o;
		long gap = rhs == null ? -1 : atTime-rhs.atTime;
		return gap < 0 ? -1 : gap > 0 ? 1 : 0;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getAtTime() {
		return atTime;
	}
	public void setAtTime(long atTime) {
		this.atTime = atTime;
	}
	public EventDismiss(int id, int type, long atTime) {
		super();
		this.id = id;
		this.type = type;
		this.atTime = atTime;
	}
	public EventDismiss(long atTime) {
		this.atTime = atTime;
	}
	public static EventDismiss read(Connection conn, ResultSet rs) throws Exception {
		return new EventDismiss(rs.getInt(1), Misc.getRsetInt(rs,2), Misc.sqlToLong(rs.getTimestamp(3)));
	}
}

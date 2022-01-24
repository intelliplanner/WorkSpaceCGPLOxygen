package com.ipssi.manualTrip.sand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;

public class SimpleTripData implements Comparable {
	private int tripId;
	private int lop;
	private long lgin;
	private long lgout;
	private int uop;
	private long ugin;
	private long ugout;
	private int assignedToReq = Misc.getUndefInt(); //-1 => not usable, otherwise id of requirement
	private int modAction = Misc.getUndefInt();
	private int materialId = Misc.getUndefInt();
	public static int G_DROP_LOAD = 1;
	public static int G_DROP_UNLOAD = 2;
	public static int G_BREAK_TRIP = 3;
	public static int G_DELETE_TRIP = 4;
	public long getDur() {
		return getLatest() - getEarliest();
	}
	public boolean canBreak(Map<Integer, Integer>opsOfInterest) {
		
		boolean retval = (getDur() > Analysis.maxIN_OUT_TIME) ;//&& (opsOfInterest.containsKey(this.lop) || opsOfInterest.containsKey(this.uop));
		return retval;
	}
	public long getEarliest() {
		return lgin > 0 ? lgin : lgout > 0 ? lgout : ugin > 0 ? ugin : ugout;
	}
	public long getLatest() {
		return ugout > 0 ? ugout : ugin > 0 ? ugin : lgout > 0 ? lgout : lgin;
	}
	public long getUgInOrSuitable() {
		return ugin > 0 ? ugin : lgout > 0 ? lgout : lgin > 0 ? lgin : ugout;
		//return lgin > 0 ? lgin : ugin;
	}
	public static FastList<SimpleTripData> getTrips(Connection conn, int vehicleId, long start, long end, Map<Integer, Integer> opsOfInterest) {
		//if lop or uop is stop then will get back null i.e assume no load or unload resp. Also if lop or uop is not of interest but is otherwise valid then tripAssigned set to -1
		PreparedStatement ps = null;
		ResultSet rs = null;
		FastList<SimpleTripData> retval = new FastList<SimpleTripData>();
		try {
			ps = conn.prepareStatement("select trip_info.id, (case when lopo.name like '%-stop' then null else load_gate_op end), load_gate_in, load_gate_out, (case when uopo.name like '%-stop' then null else unload_gate_op end), unload_gate_in, unload_gate_out, (case when load_material_guess is not null then load_material_guess when unload_material_guess is not null then unload_material_guess else load_material_manual end), mta.manual_trip_desired_id from trip_info left outer join manual_trip_assigned mta on (mta.trip_info_id = trip_info.id) left outer join op_station lopo on (lopo.id = load_gate_op) left outer join op_station uopo on (uopo.id = unload_gate_op) where trip_info.vehicle_id = ? and combo_end > ? and combo_start <= ? order by combo_start");
			ps.setInt(1, vehicleId);
			java.sql.Timestamp st = Misc.longToSqlDate(start);
			java.sql.Timestamp en = Misc.longToSqlDate(end);
			ps.setTimestamp(2, st);
			ps.setTimestamp(3, en);
			rs = ps.executeQuery();
			while (rs.next()) {
				SimpleTripData dat = new SimpleTripData(rs.getInt(1), Misc.getRsetInt(rs, 2), Misc.sqlToLong(rs.getTimestamp(3)), Misc.sqlToLong(rs.getTimestamp(4)), Misc.getRsetInt(rs, 5), Misc.sqlToLong(rs.getTimestamp(6)),Misc.sqlToLong(rs.getTimestamp(7)), Misc.getRsetInt(rs, 8), Misc.getRsetInt(rs, 9));
				retval.add(dat);
				if (Misc.isUndef(dat.lop)) {
					dat.lgin = Misc.getUndefInt();
					dat.lgout = Misc.getUndefInt();
				}
				if (Misc.isUndef(dat.uop)) {
					dat.ugin = Misc.getUndefInt();
					dat.ugout = Misc.getUndefInt();
				}
				boolean ofInterest = true;
				if (opsOfInterest != null) {
					ofInterest = false;
					if (!Misc.isUndef(dat.lop) && opsOfInterest.containsKey(dat.lop))
						ofInterest = true;
					if (!Misc.isUndef(dat.uop) && opsOfInterest.containsKey(dat.uop))
						ofInterest = true;
				}
				if (!ofInterest)
					dat.setAssignedToReq(-1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	public SimpleTripData(long ts) {
		this.ugin = ts;
	}
	public SimpleTripData(int tripId, int lop, long lgin, long lgout, int uop,long ugin, long ugout, int materialId, int assignedToReq) {
		super();
		this.tripId = tripId;
		this.lop = lop;
		this.lgin = lgin;
		this.lgout = lgout;
		this.uop = uop;
		this.ugin = ugin;
		this.ugout = ugout;
		this.materialId = materialId;
		this.assignedToReq = assignedToReq;
	}
	
	public static SimpleTripData getTripDataById(Connection conn, int tripId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		SimpleTripData dat = null;
		try {
			ps = conn.prepareStatement("select trip_info.id, (case when lopo.name like '%-stop' then null else load_gate_op end), load_gate_in, load_gate_out, (case when uopo.name like '%-stop' then null else unload_gate_op end), unload_gate_in, unload_gate_out, (case when load_material_guess is not null then load_material_guess when unload_material_guess is not null then unload_material_guess else load_material_manual end), mta.manual_trip_desired_id from trip_info left outer join manual_trip_assigned mta on (mta.trip_info_id = trip_info.id) left outer join op_station lopo on (lopo.id = load_gate_op) left outer join op_station uopo on (uopo.id = unload_gate_op) where trip_info.id = ?");
			ps.setInt(1, tripId);
			rs = ps.executeQuery();
			while (rs.next()) {
				dat = new SimpleTripData(rs.getInt(1), Misc.getRsetInt(rs, 2), Misc.sqlToLong(rs.getTimestamp(3)), Misc.sqlToLong(rs.getTimestamp(4)), Misc.getRsetInt(rs, 5), Misc.sqlToLong(rs.getTimestamp(6)),Misc.sqlToLong(rs.getTimestamp(7)), Misc.getRsetInt(rs, 8), Misc.getRsetInt(rs, 9));
				if (Misc.isUndef(dat.lop)) {
					dat.lgin = Misc.getUndefInt();
					dat.lgout = Misc.getUndefInt();
				}
				if (Misc.isUndef(dat.uop)) {
					dat.ugin = Misc.getUndefInt();
					dat.ugout = Misc.getUndefInt();
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return dat;
	}
	
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		StringBuilder sb = new StringBuilder();
		sb.append(tripId).append(",").append(lop).append(",").append(lgin > 0 ? sdf.format(Misc.longToUtilDate(lgin)) : "null")
		.append(",").append(lgout > 0 ? sdf.format(Misc.longToUtilDate(lgout)) : "null")
		.append(",").append(uop).append(",").append(ugin > 0 ? sdf.format(Misc.longToUtilDate(ugin)) : "null")
		.append(",").append(ugout > 0 ? sdf.format(Misc.longToUtilDate(ugout)) : "null")
		;
		return sb.toString();
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getLop() {
		return lop;
	}
	public void setLop(int lop) {
		this.lop = lop;
	}
	public long getLgin() {
		return lgin;
	}
	public void setLgin(long lgin) {
		this.lgin = lgin;
	}
	public long getLgout() {
		return lgout;
	}
	public void setLgout(long lgout) {
		this.lgout = lgout;
	}
	public int getUop() {
		return uop;
	}
	public void setUop(int uop) {
		this.uop = uop;
	}
	public long getUgin() {
		return ugin;
	}
	public void setUgin(long ugin) {
		this.ugin = ugin;
	}
	public long getUgout() {
		return ugout;
	}
	public void setUgout(long ugout) {
		this.ugout = ugout;
	}
	@Override
	public int compareTo(Object o) {
		SimpleTripData rhs = (SimpleTripData) o;
		long st = this.getUgInOrSuitable();
		long en = rhs.getUgInOrSuitable();
		return st < en ? -1 : st == en ? 0 : 1;
	}
	public int getAssignedToReq() {
		return assignedToReq;
	}
	public void setAssignedToReq(int assignedToReq) {
		this.assignedToReq = assignedToReq;
	}
	public int getModAction() {
		return modAction;
	}
	public void setModAction(int modAction) {
		this.modAction = modAction;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	
}

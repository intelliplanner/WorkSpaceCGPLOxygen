package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.GpsData;

public class Assignment implements Comparable {
	private int vehicleId;
	private int siteId;
	private int destId;
	private int assignmentStatus;
	private int srcOfAssignment;
	private long atTime;
	
	public int compareTo(Object o) {
		Assignment rhs = (Assignment) o;
		long gap = rhs == null ? -1 : atTime-rhs.atTime;
		return gap < 0 ? -1 : gap > 0 ? 1 : 0;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getSiteId() {
		return siteId;
	}
	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}
	public int getDestId() {
		return destId;
	}
	public void setDestId(int destId) {
		this.destId = destId;
	}
	public long getAtTime() {
		return atTime;
	}
	public void setAtTime(long atTime) {
		this.atTime = atTime;
	}
	public Assignment(long atTime) {
		this.atTime = atTime;
	}
	public Assignment(int vehicleId, int siteId, int destId,int assignmentStatus, long atTime, int srcOfAssignment) {
		super();
		this.vehicleId = vehicleId;
		this.siteId = siteId;
		this.destId = destId;
		this.assignmentStatus = assignmentStatus;
		this.atTime = atTime;
		this.srcOfAssignment = srcOfAssignment;
	}
	public int getAssignmentStatus() {
		return assignmentStatus;
	}
	public void setAssignmentStatus(int assignmentStatus) {
		this.assignmentStatus = assignmentStatus;
	}
	public static Assignment read(Connection conn, ResultSet rs) throws Exception {
		return new Assignment(rs.getInt(1), Misc.getRsetInt(rs,2), Misc.getRsetInt(rs, 3), Misc.getRsetInt(rs, 4), Misc.sqlToLong(rs.getTimestamp(5)), rs.getInt(6));
	}
	public int getSrcOfAssignment() {
		return srcOfAssignment;
	}
	public void setSrcOfAssignment(int srcOfAssignment) {
		this.srcOfAssignment = srcOfAssignment;
	}
	
}

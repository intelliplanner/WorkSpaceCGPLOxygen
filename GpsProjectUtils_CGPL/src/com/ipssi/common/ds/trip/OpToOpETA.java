package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;

public class OpToOpETA {
	private static ConcurrentHashMap<MiscInner.Pair, OpToOpETA> opToOpETA = new ConcurrentHashMap<MiscInner.Pair, OpToOpETA>();
	private int lopid = Misc.getUndefInt();
	private int uopid = Misc.getUndefInt();
	private double loadLeadDist;
	private double unloadLeadDist;
	private double loadLeadTime;
	private double unloadLeadTime;
	
	public static OpToOpETA get(Connection conn, int fromOpId, int toOpId) throws Exception {
		OpToOpETA retval = null; 
		try {
			MiscInner.Pair key = new MiscInner.Pair(fromOpId, toOpId);
			loadOpToOpETA(conn, false);
			retval = opToOpETA.get(key);
			if (retval != null)
				return retval;
			key.first = toOpId;
			key.second = fromOpId;
			retval = opToOpETA.get(key);
			if (retval != null)
				return retval;
			key.first = fromOpId;
			key.second = Misc.getUndefInt();
			retval = opToOpETA.get(key);
			if (retval != null)
				return retval;
			key.first = toOpId;
			key.second = Misc.getUndefInt();
			retval = opToOpETA.get(key);
			if (retval != null)
				return retval;
			key.first = Misc.getUndefInt();
			key.second = Misc.getUndefInt();
			retval = opToOpETA.get(key);
			if (retval != null)
				return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return null;
		
	}
	
	public static void loadOpToOpETA(Connection conn, boolean reload) {
		try {
			if (reload)
				opToOpETA.clear();
			if (opToOpETA.isEmpty()) {
				PreparedStatement ps = conn.prepareStatement("select lopid, uopid, load_lead_dist, unload_lead_dist, load_lead_minute, unload_lead_minute from eta_setup_op_to_op");
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					int lopid = Misc.getRsetInt(rs, 1);
					int uopId = Misc.getRsetInt(rs, 2);
					double loadLeadDist = Misc.getRsetDouble(rs, 3);
					double unloadDist = Misc.getRsetDouble(rs, 4);
					double loadLeadTime = Misc.getRsetDouble(rs, 5);
					double unloadLeadTime = Misc.getRsetDouble(rs, 6);
					OpToOpETA eta = new OpToOpETA(lopid, uopId, loadLeadDist, unloadDist, loadLeadTime, unloadLeadTime);
					opToOpETA.put(new MiscInner.Pair(lopid, uopId), eta);
				}
				rs.close();
				ps.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eait it
		}
	}

	public int getLopid() {
		return lopid;
	}

	public void setLopid(int lopid) {
		this.lopid = lopid;
	}

	public int getUopid() {
		return uopid;
	}

	public void setUopid(int uopid) {
		this.uopid = uopid;
	}

	public double getLoadLeadDist() {
		return loadLeadDist;
	}

	public void setLoadLeadDist(double loadLeadDist) {
		this.loadLeadDist = loadLeadDist;
	}

	public double getUnloadLeadDist() {
		return unloadLeadDist;
	}

	public void setUnloadLeadDist(double unloadLeadDist) {
		this.unloadLeadDist = unloadLeadDist;
	}

	public double getLoadLeadTime() {
		return loadLeadTime;
	}

	public void setLoadLeadTime(double loadLeadTime) {
		this.loadLeadTime = loadLeadTime;
	}

	public double getUnloadLeadTime() {
		return unloadLeadTime;
	}

	public void setUnloadLeadTime(double unloadLeadTime) {
		this.unloadLeadTime = unloadLeadTime;
	}

	public OpToOpETA(int lopid, int uopid, double loadLeadDist,
			double unloadLeadDist, double loadLeadTime, double unloadLeadTime) {
		super();
		this.lopid = lopid;
		this.uopid = uopid;
		this.loadLeadDist = loadLeadDist;
		this.unloadLeadDist = unloadLeadDist;
		this.loadLeadTime = loadLeadTime;
		this.unloadLeadTime = unloadLeadTime;
	}
	
}

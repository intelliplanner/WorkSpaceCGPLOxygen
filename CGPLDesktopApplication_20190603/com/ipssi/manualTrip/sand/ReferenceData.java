package com.ipssi.manualTrip.sand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.GpsDataResultSetReader;
import com.ipssi.processor.utils.Vehicle;

public class ReferenceData {
	private int fromId;
	private int toId;
	private long lgin;
	private long lgout;
	private long ugin;
	private long ugout;
	private int refVehicleId;
	private int tripId;
	private long adjustedAgainstLginUgout = Misc.getUndefInt();
	private double baseFactor = 1;
	public long getAdjustedAgainstLginUgout() {
		return adjustedAgainstLginUgout;
	}
	public void setAdjustedAgainstLginUgout(long adjustedAgainstLginUgout) {
		this.adjustedAgainstLginUgout = adjustedAgainstLginUgout;
	}
	public double getBaseFactor() {
		return baseFactor;
	}
	public void setBaseFactor(double baseFactor) {
		this.baseFactor = baseFactor;
	}
	private ArrayList<GpsData> dataList = new ArrayList<GpsData>();
	
	public void getLoggedData(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement("select manual_ref_logged_data.vehicle_id,  cast(manual_ref_logged_data.longitude as DECIMAL(9,6)) longitude, cast(manual_ref_logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed, gps_id from manual_ref_trip join manual_ref_logged_data on (manual_ref_logged_data.vehicle_id = manual_ref_trip.vehicle_id and manual_ref_logged_data.attribute_id=0 and manual_ref_logged_data.gps_record_time between manual_ref_trip.load_gate_out and manual_ref_trip.unload_gate_in) where manual_ref_trip.id = ? order by gps_record_time");
			ps.setInt(1, this.getTripId());
			rs = ps.executeQuery();
			GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
			Vehicle vehicle = null;
    		while ((vehicle = reader.readGpsData()) !=null) {
    			this.addGpsData(vehicle.getGpsData());
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
	}
	public static ReferenceData createDummy(int fromId, int toId, double leadFactor) {
		long ugin = System.currentTimeMillis();
		long ugout = ugin+Analysis.baseProcessTime;
		long lgout = ugin - (long)(leadFactor*Analysis.maxLeadTime);
		long lgin = lgout - Analysis.baseProcessTime;
		int tripId = Misc.getUndefInt();
		int refVehicleId = Misc.getUndefInt();
		ReferenceData retval = new ReferenceData(fromId, toId, lgin, lgout, ugin, ugout, refVehicleId, tripId, leadFactor);
		return retval;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append((int)((lgout-lgin)/(1000*60)))
		.append(",").append((int)((ugin-lgout)/(1000*60)))
		.append(",").append((int)((ugout-ugin)/(1000*60)))
		;
		return sb.toString();
	}
	public ReferenceData(int fromId, int toId, long lgin, long lgout,
			long ugin, long ugout, int refVehicleId, int tripId, double leadFactor) {
		super();
		/*
		long cycle = ugout-lgin;
		long maxDesiredCycle = (long)(leadFactor*Analysis.maxLeadTime) + 2 * Analysis.baseProcessTime; //TODO make it more parameterizable/dist driven
		if (cycle > maxDesiredCycle) {//adjust it
			double frac = (double)maxDesiredCycle/(double)cycle;
			long origLgap = lgout - lgin;
			ugout = ugin+(long)((ugout-ugin)*frac);
			lgout = ugin-(long)((ugin-lgout)*frac);
			lgin = lgout-(long)(origLgap*frac);
			this.adjustedAgainstLginUgout = maxDesiredCycle;
		}
		*/
		this.fromId = fromId;
		this.toId = toId;
		this.lgin = lgin;
		this.lgout = lgout;
		this.ugin = ugin;
		this.ugout = ugout;
		this.refVehicleId = refVehicleId;
		this.tripId = tripId;
		this.baseFactor = leadFactor;
	}
	public int getFromId() {
		return fromId;
	}
	public void setFromId(int fromId) {
		this.fromId = fromId;
	}
	public int getToId() {
		return toId;
	}
	public void setToId(int toId) {
		this.toId = toId;
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
	public int getRefVehicleId() {
		return refVehicleId;
	}
	public void setRefVehicleId(int refVehicleId) {
		this.refVehicleId = refVehicleId;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public ArrayList<GpsData> getDataList() {
		return dataList;
	}
	public void setDataList(ArrayList<GpsData> dataList) {
		this.dataList = dataList;
	}
	public void addGpsData(GpsData data) {
		dataList.add(data);
	}
}

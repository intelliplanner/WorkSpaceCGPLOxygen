package com.ipssi.modeler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;


public class LevelChangeEvent extends GpsData {
	private int eventId = Misc.getUndefInt();
	private double amtChange = 0;
	private boolean isDirty = false;
	private GpsData endPt = null;
	private byte refuelEvent = -1; //-1 means unknown, 0 => no, 1 => yes
	public boolean isDirty() {
		return isDirty;
	}
	public void setIsDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	public LevelChangeEvent (int eventId, GpsData data, double amtChange, GpsData endPt) {
		super(data);
		if (endPt == null)
			endPt = data;
		this.endPt = endPt;
		this.setEventId(eventId);
		this.amtChange = amtChange;
	}
	
	public LevelChangeEvent (GpsData data, GpsData endPt) {
		super(data);
		if (endPt == null)
			endPt = data;
		this.endPt = endPt;
	}

	public String toString() {
		return "Evt:"+eventId+" Amt:"+amtChange+" Data:"+super.toString();
	}
	
	public void saveEvent(Connection conn, int vehicleId, int attributeId, int ruleId, ModelSpec spec, VehicleSpecific vehicleSpecificParam, CacheTrack.VehicleSetup vehSetup) throws Exception {
		try {
			boolean isRefuelLike = spec.mayConsiderAsRefuelling(this.amtChange, vehicleSpecificParam);
			int refuelRuleId = Misc.getUndefInt(); //not used
			if (eventId > 0)
				updateEvent(conn, isRefuelLike, refuelRuleId);
			else if (!Misc.isUndef(eventId) && eventId < 0)
				deleteEvent(conn, isRefuelLike, refuelRuleId);
			else {
			    PreparedStatement ps = conn.prepareStatement(
			    		"insert into engine_events (vehicle_id, rule_id, event_begin_longitude, event_begin_latitude, event_end_longitude, event_end_latitude, event_start_time, event_stop_time, attribute_id, attribute_value, updated_on, event_create_recvtime, event_end_name, event_begin_name, refrence_rule_id, addnl_value1, is_refuel)" +
			    		" values(?,?,?,?,?,?,?,?,?,?,now(),?,?,?,?,?,?)"
			    		);
			    int colIndex = 1;
			    ps.setInt(colIndex++, vehicleId);
			    ps.setInt(colIndex++, ruleId);
			    ps.setDouble(colIndex++, getLongitude());
			    ps.setDouble(colIndex++, getLatitude());
			    ps.setDouble(colIndex++, this.getEndPt().getLongitude());
			    ps.setDouble(colIndex++, this.getEndPt().getLatitude());
//			    ps.setTimestamp(colIndex++, Misc.utilToSqlDate(getGps_Record_Time()));
//			    ps.setTimestamp(colIndex++, Misc.utilToSqlDate(getGps_Record_Time()));
			    ps.setTimestamp(colIndex++, Misc.longToSqlDate(getGps_Record_Time()));
			    ps.setTimestamp(colIndex++, Misc.longToSqlDate(this.getEndPt().getGps_Record_Time()));
			    ps.setInt(colIndex++, attributeId);
			    ps.setDouble(colIndex++, getValue());
			    ps.setTimestamp(colIndex++, Misc.longToSqlDate(getGpsRecvTime()));
			    ps.setString(colIndex++,calcName(conn,vehicleId, vehSetup));
			    ps.setString(colIndex++, null);//this.getEndPt().getName(conn,vehicleId));
			    ps.setInt(colIndex++, ruleId);
			    ps.setDouble(colIndex++, this.amtChange);
			    ps.setInt(colIndex++, isRefuelLike ? 1 : 0);
			    ps.executeUpdate();
			    //ON_THE_FLY_NAME setName(null);
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					this.setEventId(rs.getInt(1));
				}
				rs.close();
			    ps.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void deleteEvent(Connection conn, boolean isRefuel, int refuelRuleId) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("delete from engine_events where id = ?");
			ps.setInt(1, getEventId() < 0 ? -1*getEventId() : getEventId());
			ps.execute();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void updateEvent(Connection conn, boolean isRefuelLike, int refuleRuleId) throws Exception {
		try {
		    PreparedStatement ps = conn.prepareStatement(
		    		//"update engine_events set event_begin_longitude=?, event_begin_latitude=?, event_end_longitude=?, event_end_latitude=?, event_start_time=?, event_stop_time=?, attribute_value=?, updated_on=now(), event_create_recvtime=?, event_end_name=?, event_begin_name=?,  addnl_value1=? where id=?)"
		    		"update engine_events set event_start_time=?, event_stop_time=?, attribute_value=?, updated_on=now(),  addnl_value1=?, is_refuel=? where id=?"
		    		);
		    int colIndex = 1;
		    //ps.setDouble(colIndex++, getPoint().getLongitude());
		    //ps.setDouble(colIndex++, getPoint().getLatitude());
		    //ps.setDouble(colIndex++, getPoint().getLongitude());
		    //ps.setDouble(colIndex++, getPoint().getLatitude());
		    ps.setTimestamp(colIndex++, Misc.longToSqlDate(getGps_Record_Time()));
		    ps.setTimestamp(colIndex++, Misc.longToSqlDate(endPt.getGps_Record_Time()));
		    ps.setDouble(colIndex++, getValue());
		    //ps.setTimestamp(colIndex++, Misc.utilToSqlDate(getGpsRecvTime()));
		    //ps.setString(colIndex++, getName());
		    //ps.setString(colIndex++, getName());
		    ps.setDouble(colIndex++, this.amtChange);
		    ps.setInt(colIndex++, isRefuelLike ?1 : 0);
		    ps.setInt(colIndex++, getEventId());
		    
		    ps.execute();
		    ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getEventId() {
		return eventId;
	}
	
	public void setAmtChange(double amtChange) {
		this.amtChange = amtChange;
	}

	public double getAmtChange() {
		return amtChange;
	}
	public GpsData getEndPt() {
		return endPt;
	}
	public void setEndPt(GpsData endPt) {
		this.endPt = endPt;
	}
	public byte getRefuelEvent() {
		return refuelEvent;
	}
	public void setRefuelEvent(byte refuelEvent) {
		this.refuelEvent = refuelEvent;
	}
}

package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class VehicleChangeLog {
	public static void changePlant(Connection conn, int vehicleId, int newPlantId, boolean forTrip, Date forDate) throws Exception {
		Date mi = null;
		Date mx = null;
		Pair<Date, Date> byTrip = null;
		if (forDate == null)
			forDate = new java.util.Date();
		if (forTrip)
			byTrip = getDateSpanningTripDate(conn, vehicleId, forDate);
		if (byTrip != null) {
			if (byTrip.first == null)
				mi = forDate;
			else
				mi = byTrip.first;
			mx = byTrip.second;
		}
		else {
			mi = forDate;
			mx = null;
		}
		changePlant(conn, vehicleId, newPlantId, mi, mx);
	}
	
	public static void changeDriver(Connection conn, int vehicleId, int newDriverId, boolean forTrip, Date forDate) throws Exception {
		Date mi = null;
		Date mx = null;
		Pair<Date, Date> byTrip = null;
		if (forTrip)
			byTrip = getDateSpanningTripDate(conn, vehicleId, forDate);
		if (byTrip != null) {
			if (byTrip.first == null)
				mi = forDate;
			else
				mi = byTrip.first;
			mx = byTrip.second;
		}
		else {
			mi = forDate;
			mx = null;
		}
		changeDriver(conn, vehicleId, newDriverId,mi,mx);
	}
	
	public static void changeDriverFromManageCallBefUpd(Connection conn, int vehicleId, int driverId) throws Exception {
		//check if existing info is same... if so then dont do change
		PreparedStatement ps = conn.prepareStatement("select 1 from driver_details where id = ? and vehicle_id_1 = ?");
		ps.setInt(1, driverId);
		ps.setInt(2, vehicleId);
		ResultSet rs = ps.executeQuery();
		boolean isSame = rs.next();
		rs.close();
		ps.close();
		if (!isSame) {
			changeDriver(conn, vehicleId, driverId, new Date(), null);
		}
	}
	public static void changePlantFromManageCallBefUpd(Connection conn, int vehicleId, int plantId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select 1 from vehicle_extended where vehicle_id = ? and plant = ?");
		ps.setInt(1, vehicleId);
		ps.setInt(2, plantId);
		ResultSet rs = ps.executeQuery();
		boolean isSame = rs.next();
		rs.close();
		ps.close();
		if (!isSame) {
			changePlant(conn, vehicleId, plantId, new Date(), null);
		}
	}
	public static void changePlant(Connection conn, int vehicleId, int plantId, Date mi, Date mx) throws Exception {
		if (mi == null)
			mi = new Date(90,0,1);
		if (mx == null)
			mx = new Date(127,0,1);
		java.sql.Timestamp mits = Misc.utilToSqlDate(mi);
		java.sql.Timestamp mxts = Misc.utilToSqlDate(mx);
		
		PreparedStatement psDel = conn.prepareStatement("delete from plant_log where vehicle_id=? and start_time >= ? and end_time <= ?");
		psDel.setInt(1, vehicleId);
		psDel.setTimestamp(2, mits);
		psDel.setTimestamp(3, mxts);
		psDel.execute();
		psDel.close();
		PreparedStatement psUpdBef = conn.prepareStatement("update plant_log set end_time = ? where vehicle_id=? and start_time < ? and end_time > ?");
		psUpdBef.setTimestamp(1, mits);
		psUpdBef.setInt(2, vehicleId);
		psUpdBef.setTimestamp(3, mits);
		psUpdBef.setTimestamp(4, mits);
		psUpdBef.execute();
		psUpdBef.close();
		PreparedStatement psUpdAft = conn.prepareStatement("update plant_log set start_time = ? where vehicle_id=? and start_time < ? and end_time > ?");
		psUpdAft.setTimestamp(1, mxts);
		psUpdAft.setInt(2, vehicleId);
		psUpdAft.setTimestamp(3, mxts);
		psUpdAft.setTimestamp(4, mxts);
		psUpdAft.execute();
		psUpdAft.close();
		PreparedStatement psIns = conn.prepareStatement("insert into plant_log (vehicle_id, plant_id, start_time, end_time) values (?,?,?,?)");
		psIns.setInt(1, vehicleId);
		psIns.setInt(2, plantId);
		psIns.setTimestamp(3, mits);
		psIns.setTimestamp(4, mxts);
		psIns.execute();
		psIns.close();
	}
	
	public static void changeDriver(Connection conn, int vehicleId, int driverId, Date mi, Date mx) throws Exception {
		if (mi == null)
			mi = new Date(90,0,1);
		if (mx == null)
			mx = new Date(127,0,1);
		java.sql.Timestamp mits = Misc.utilToSqlDate(mi);
		java.sql.Timestamp mxts = Misc.utilToSqlDate(mx);
		
		PreparedStatement psDel = conn.prepareStatement("delete from driver_log where vehicle_id=? and start_time >= ? and end_time <= ?");
		psDel.setInt(1, vehicleId);
		psDel.setTimestamp(2, mits);
		psDel.setTimestamp(3, mxts);
		psDel.execute();
		psDel.close();
		PreparedStatement psUpdBef = conn.prepareStatement("update driver_log set end_time = ? where vehicle_id=? and start_time < ? and end_time > ?");
		psUpdBef.setTimestamp(1, mits);
		psUpdBef.setInt(2, vehicleId);
		psUpdBef.setTimestamp(3, mits);
		psUpdBef.setTimestamp(4, mits);
		psUpdBef.execute();
		psUpdBef.close();
		PreparedStatement psUpdAft = conn.prepareStatement("update driver_log set start_time = ? where vehicle_id=? and start_time < ? and end_time > ?");
		psUpdAft.setTimestamp(1, mxts);
		psUpdAft.setInt(2, vehicleId);
		psUpdAft.setTimestamp(3, mxts);
		psUpdAft.setTimestamp(4, mxts);
		psUpdAft.execute();
		psUpdAft.close();
		//symmetric driver cannot be also op on multiple veh
		psDel = conn.prepareStatement("delete from driver_log where driver_id=? and start_time >= ? and end_time <= ?");
		psDel.setInt(1, driverId);
		psDel.setTimestamp(2, mits);
		psDel.setTimestamp(3, mxts);
		psDel.execute();
		psDel.close();
		psUpdBef = conn.prepareStatement("update driver_log set end_time = ? where driver_id=? and start_time < ? and end_time > ?");
		psUpdBef.setTimestamp(1, mits);
		psUpdBef.setInt(2, driverId);
		psUpdBef.setTimestamp(3, mits);
		psUpdBef.setTimestamp(4, mits);
		psUpdBef.execute();
		psUpdBef.close();
		psUpdAft = conn.prepareStatement("update driver_log set start_time = ? where driver_id=? and start_time < ? and end_time > ?");
		psUpdAft.setTimestamp(1, mxts);
		psUpdAft.setInt(2, driverId);
		psUpdAft.setTimestamp(3, mxts);
		psUpdAft.setTimestamp(4, mxts);
		psUpdAft.execute();
		psUpdAft.close();
		
		PreparedStatement psIns = conn.prepareStatement("insert into driver_log (vehicle_id, driver_id, start_time, end_time) values (?,?,?,?)");
		psIns.setInt(1, vehicleId);
		psIns.setInt(2, driverId);
		psIns.setTimestamp(3, mits);
		psIns.setTimestamp(4, mxts);
		psIns.execute();
		psIns.close();
	}
	
	private static Pair<Date, Date> getDateSpanningTripDate(Connection conn, int vehicleId, Date forDate) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("select ti.combo_start, ti.confirm_time from trip_info ti where vehicle_id=? and combo_start <= ? and (confirm_time is null or confirm_time > ?) ");
			ps.setInt(1, vehicleId);
			if (forDate == null)
				forDate = new Date();
			java.sql.Timestamp ts = Misc.utilToSqlDate(forDate);
			ps.setTimestamp(2, ts);
			ps.setTimestamp(3, ts);
			ResultSet rs = ps.executeQuery();
			Date mi = null;
			Date mx = null;
			while (rs.next()) {
				mi = Misc.sqlToUtilDate(rs.getTimestamp(1));
				mx = Misc.sqlToUtilDate(rs.getTimestamp(2));
			}
			rs.close();
			ps.close();
			return new Pair<Date, Date>(mi,mx);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}

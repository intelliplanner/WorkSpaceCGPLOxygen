package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class SchedulerHelper {

	public static Integer GPS_OK = 1;
	public static Integer RF_OK = 2;
	public static Integer PAPER_OK = 3;
	public static Integer BLACKLISTED = 4;
	public static long GPS_CHECK_DURATION = 300*60*1000;	// 5 hr
	public static long PAPER_CHECK_DURATION = 14400*60*1000;	// 24 hr
	public static HashMap<Integer, Date> taskTrackerMap = new HashMap<Integer, Date>();// should contain key and value in minutes.
	static{
		Date dt = new Date();
		long dtLong = dt.getTime();
		taskTrackerMap.put(GPS_OK, new Date(dtLong - (GPS_CHECK_DURATION+120*1000)));
		taskTrackerMap.put(PAPER_OK, new Date(dtLong - (PAPER_CHECK_DURATION+120*1000)));
	}
	
	public static synchronized void updateVehicleBlockStatus(Connection conn){
		Date dt = new Date();
		long dtLong = dt.getTime();
		Date gpsOkPair = taskTrackerMap.get(GPS_OK);
		if(gpsOkPair != null && (dtLong-gpsOkPair.getTime()) > GPS_CHECK_DURATION){
			// update vehicle_tpr_block_status for gps_ok.
			updateVehicleTPRBlockStatusForGPS(conn);
			taskTrackerMap.put(GPS_OK, dt);
		}
		Date paperOkPair = taskTrackerMap.get(PAPER_OK);
		if(paperOkPair != null && (dtLong-paperOkPair.getTime()) > PAPER_CHECK_DURATION){
			// update vehicle_tpr_block_status for paper_ok.
			updateVehicleTPRBlockStatusForPaper(conn);
			taskTrackerMap.put(PAPER_OK, dt);
		}
	}
	
	public static void updateVehicleTPRBlockStatusForPaper(Connection conn){
		Boolean retval = true;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection localConn = null;
		PreparedStatement localPs = null;
		//		String query = "select id " +
		//			//	", gps_record_time, now(), TimeStampDiff(minute,gps_record_time, now())" +
		//				" from vehicle left outer join current_data on (vehicle.id = current_data.vehicle_id) " +
		//				" where status = 1 and attribute_id = 0 " +
		//				" and TimeStampDiff(minute,case when gps_record_time is null then now() else gps_record_time end, now()) > ("+GPS_CHECK_DURATION+")/(60*1000)";
		String query = "select  vehicle.id, vehicle.name, TIMESTAMPDIFF(minute,now(),insurance_number_expiry) insurance_dur,insurance_number_expiry, " +
				" TIMESTAMPDIFF(minute,now(),registeration_number_expiry)  registration_dur, registeration_number_expiry, " +
				" TIMESTAMPDIFF(minute,now(),permit1_number_expiry) permit1_dur, permit1_number_expiry " +
				" from  vehicle left outer join vehicle_extended on (vehicle.id = vehicle_extended.vehicle_id)  where vehicle.status in (1)";
		String localQuery = "insert into vehicle_tpr_block_status (vehicle_id, is_paper_ok) " +
		" values (?,?) on duplicate key update is_paper_ok = ? ";
		try {
			localConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			localPs = localConn.prepareStatement(localQuery);
			rs = ps.executeQuery();
			int vehicleId = Misc.getUndefInt();
			int insuranceDiffInMinutes = Misc.getUndefInt();
			int registrationDiffInMinutes = Misc.getUndefInt();
			int permit1DiffInMinutes = Misc.getUndefInt();
			while (rs.next()) {
				vehicleId = rs.getInt("id");
				insuranceDiffInMinutes = rs.getInt("insurance_dur");
				registrationDiffInMinutes = rs.getInt("registration_dur");
				permit1DiffInMinutes = rs.getInt("permit1_dur");
				
				int param = 1;// if fails then 0(not ok) else 1(ok)
				if(insuranceDiffInMinutes < 0 || registrationDiffInMinutes < 0 || permit1DiffInMinutes < 0){
					localPs.setInt(param++, vehicleId);
					localPs.setInt(param++, 0);
					localPs.setInt(param++, 0);
				}else{
					localPs.setInt(param++, vehicleId);
					localPs.setInt(param++, 1);
					localPs.setInt(param++, 1);
				}
				localPs.execute();
				localConn.commit();
			}
			localPs.close();
			rs.close();
			ps.close();
		}
		catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		}
		finally{
			try {
				if (localConn != null && localConn.getAutoCommit())
					localConn.setAutoCommit(false);
				DBConnectionPool.returnConnectionToPoolNonWeb(localConn, destroyIt);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	public static void updateVehicleTPRBlockStatusForGPS(Connection conn){
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection localConn = null;
		PreparedStatement localPs = null;
		//		String query = "select id " +
		//			//	", gps_record_time, now(), TimeStampDiff(minute,gps_record_time, now())" +
		//				" from vehicle left outer join current_data on (vehicle.id = current_data.vehicle_id) " +
		//				" where status = 1 and attribute_id = 0 " +
		//				" and TimeStampDiff(minute,case when gps_record_time is null then now() else gps_record_time end, now()) > ("+GPS_CHECK_DURATION+")/(60*1000)";
		String query = "select id , gps_record_time , now(), TimeStampDiff(minute,gps_record_time, now()) diff " +
		" from vehicle left outer join current_data on (vehicle.id = current_data.vehicle_id and attribute_id = 0 )  " + 
		" where status = 1";
		// insert into vehicle_tpr_block_status (vehicle_id, is_blacklisted, is_paper_ok, is_gps_ok) values (20142,1,0,1) on duplicate key update is_blacklisted = 0, is_paper_ok= 1, is_gps_ok = 1;
		String localQuery = "insert into vehicle_tpr_block_status (vehicle_id, is_gps_ok) " +
		" values (?,?) on duplicate key update is_gps_ok = ? ";
		try {
			localConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			localPs = localConn.prepareStatement(localQuery);
			rs = ps.executeQuery();
			int vehicleId = Misc.getUndefInt();
			int diffInMinutes = Misc.getUndefInt();
			while (rs.next()) {
				vehicleId = rs.getInt("id");
				diffInMinutes = rs.getInt("diff");

				int param = 1;
				if(diffInMinutes > GPS_CHECK_DURATION){ // if fails then 0(not ok) else 1(ok)
					localPs.setInt(param++, vehicleId);
					localPs.setInt(param++, 0);
					localPs.setInt(param++, 0);
				}else{
					localPs.setInt(param++, vehicleId);
					localPs.setInt(param++, 1);
					localPs.setInt(param++, 1);
				}
				localPs.execute();
				localConn.commit();
			}
			localPs.close();
			rs.close();
			ps.close();
		}
		catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		}
		finally{
			try {
				if (localConn != null && localConn.getAutoCommit())
					localConn.setAutoCommit(false);
				DBConnectionPool.returnConnectionToPoolNonWeb(localConn, destroyIt);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

package com.ipssi.report.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Region;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.ReadShapeFile;
import com.ipssi.processor.utils.GpsData;

public class ScanSupport {
	private static ConcurrentHashMap<Integer, ArrayList<Pair<Long,String>>> g_currentScansByVehicle = new ConcurrentHashMap<Integer, ArrayList<Pair<Long,String>>>();
	public static ArrayList<Pair<Long,String>> getScans(Connection conn, int vehicleId) {
		if (g_currentScansByVehicle.size() == 0)
			loadScansByVehicle(conn);
		ArrayList<Pair<Long,String>> currScan = g_currentScansByVehicle.get(vehicleId);
		return currScan;
	}
	
	private static void removeCachedScanForVehicle(Connection conn, int vehicleId) {
		if (g_currentScansByVehicle.size() == 0)
			loadScansByVehicle(conn);
		ArrayList<Pair<Long,String>> currScan = g_currentScansByVehicle.get(vehicleId);
		if (currScan != null) {
			currScan.clear();
		}
	}

	private static void removeCachedScanForVehicle(Connection conn, int vehicleId, String scan) {
		if (g_currentScansByVehicle.size() == 0)
			loadScansByVehicle(conn);
		ArrayList<Pair<Long,String>> currScan = g_currentScansByVehicle.get(vehicleId);
		for (int i=0,is=currScan == null ? 0 : currScan.size(); i<is; i++) {
			if (currScan.get(i).second.equals(scan)) {
				currScan.remove(i);
				break;
			}
		}
	}
	
	private static void addCachedScanForVehicle(Connection conn, int vehicleId, String scan, long ts) {
		if (g_currentScansByVehicle.size() == 0)
			loadScansByVehicle(conn);
		ArrayList<Pair<Long,String>> currScan = g_currentScansByVehicle.get(vehicleId);
		boolean found = false;
		for (int i=0,is=currScan == null ? 0 : currScan.size(); i<is; i++) {
			if (currScan.get(i).equals(scan)) {
				found = true;
				break;
			}
		}
		if (!found) {
			if (currScan == null) {
				currScan = new ArrayList<Pair<Long,String>>();
				g_currentScansByVehicle.put(vehicleId, currScan);
			}
			currScan.add(new Pair<Long, String>(ts,scan));
		}
	}
	
	public static void loadScansByVehicle(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select first_scan_vehicle_id, scan, first_scan_ts from current_scans where first_scan_vehicle_id is not null and second_scan_vehicle_id is null order by first_scan_vehicle_id");
			rs = ps.executeQuery();
			int prevVehicleId = Misc.getUndefInt();
			ArrayList<Pair<Long,String>> currScan = null;
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				String scan = rs.getString(2);
				long ts = rs.getLong(3);
				if (vehicleId != prevVehicleId) {
					currScan = null;
					prevVehicleId = vehicleId;
				}
				if (currScan == null) {
					currScan = g_currentScansByVehicle.get(vehicleId);
					if (currScan == null) {
						currScan = new ArrayList<Pair<Long,String>>();
						g_currentScansByVehicle.put(vehicleId, currScan);
					}
				}
				currScan.add(new Pair<Long, String>(ts,scan));
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (Exception e2) {
					
				}
			}
			if (ps != null) {
				try {
					ps.close();
				}
				catch (Exception e2) {
					
				}
			}
		}
	}
	public static void clearCache(Connection conn, int vehicleId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ScanSupport.removeCachedScanForVehicle(conn, vehicleId);
			ps = conn.prepareStatement("update current_scans set first_scan_vehicle_id = null, first_scan_ts=null, second_scan_vehicle_id=null, second_scan_ts=null where first_scan_vehicle_id = ?");
			ps.setInt(1, vehicleId);
			ps.execute();
			ps.close();
			ps = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (Exception e2) {
					
				}
			}
			if (ps != null) {
				try {
					ps.close();
				}
				catch (Exception e2) {
					
				}
			}
		}
	}
	public static void clearCache(Connection conn, String id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select first_scan_vehicle_id from current_scans where scan = ? and second_scan_vehicle_id is null");
			ps.setString(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				int vehId = Misc.getRsetInt(rs, 1);
				ScanSupport.removeCachedScanForVehicle(conn, vehId, id);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			 ps = conn.prepareStatement("update current_scans set first_scan_vehicle_id=null, first_scan_ts=null, second_scan_vehicle_id=null, second_scan_ts=null, updated_on = now() where scan = ?");
			 ps.setString(1, id);
			 ps.execute();
			 ps.close();
			 ps = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (Exception e2) {
					
				}
			}
			if (ps != null) {
				try {
					ps.close();
				}
				catch (Exception e2) {
					
				}
			}
		}
	}
	private static Triple<Integer, Long, Long> getCurrScan(Connection conn, String scan) throws Exception {
		//will create entry in current scan if it does not already exist
		int vehicleId = Misc.getUndefInt();
		long firstTS = Misc.getUndefInt();
		long secondTS = Misc.getUndefInt();

		try {
			
			PreparedStatement ps = conn.prepareStatement("select first_scan_vehicle_id, first_scan_ts, second_scan_ts from current_scans where scan = ?");
			ps.setString(1, scan);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				vehicleId = Misc.getRsetInt(rs, 1);
				firstTS = Misc.getRsetLong(rs, 2);
				secondTS = Misc.getRsetLong(rs, 3);
			}
			else {
				PreparedStatement ps2 = conn.prepareStatement("insert into current_scans (scan) values (?)");
				ps2.setString(1, scan);
				ps2.execute();
				ps2.close();
				ps2 = null;
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			return new Triple<Integer, Long, Long>(vehicleId, firstTS, secondTS);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}		
	}
	
	private static boolean reversFirstScan(Connection conn, int prevVehicleId, String scan) throws Exception {
		try {
			//remove from cache of prevVehicleId
			//check if count becomes 0 ..
			//if so remove the trip for the vehicleId
			ArrayList<Pair<Long, String>> currScans = ScanSupport.getScans(conn, prevVehicleId);
			boolean retval = false;
			if (currScans.size() == 1) {//trip to be removed if count is 
				PreparedStatement ps = conn.prepareStatement("select mx_cnt, id from demo_scan_trip where vehicle_id = ? and ? between combo_start and combo_end");
				ps.setInt(1, prevVehicleId);
				ps.setTimestamp(2, Misc.longToSqlDate(currScans.get(0).first));
				ResultSet rs = ps.executeQuery();
				int cnt = 0;
				int tripid = Misc.getUndefInt();
				if (rs.next()) {
					cnt = rs.getInt(1);
					tripid = rs.getInt(2);
				}
				rs.close();
				rs = null;
				ps.close();
				ps = null;
				if (cnt == 1) {
					ps = conn.prepareStatement("delete from demo_scan_trip where id = ?");
					ps.setInt(1, tripid);
					ps.execute();
					ps.close();
					ps = null;
					retval = true;
				}
				else {
					
				}
			}
			ScanSupport.removeCachedScanForVehicle(conn, prevVehicleId, scan);
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void redo(Connection conn, int vehicleId) {
		try {
			ReadShapeFile.loadRTree(conn);
			StringBuilder sb = new StringBuilder();
			sb.append("delete from demo_scan_trip ");
			if (!Misc.isUndef(vehicleId)) {
				sb.append(" where vehicle_id in (").append(vehicleId).append(")");
			}
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ps.execute();
			ps.close();
			ps = null;
			sb.setLength(0);
			sb.append("delete from current_scans ");
			if (!Misc.isUndef(vehicleId)) {
				sb.append(" where first_scan_vehicle_id in (").append(vehicleId).append(")");
			}
			ps = conn.prepareStatement(sb.toString());
			ps.execute();
			ps.close();
			ps = null;
			sb.setLength(0);
			
			
			sb.append("select id from vehicle where status=1 ");
			if (!Misc.isUndef(vehicleId)) {
				sb.append(" and id in (").append(vehicleId).append(")");
			}
			ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			ArrayList<Integer> vehList = new ArrayList<Integer>();
			while (rs.next()) {
				int vehId = rs.getInt(1);
				vehList.add(vehId);
				ScanSupport.removeCachedScanForVehicle(conn, vehId);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			ps = conn.prepareStatement("select l1.vehicle_id, l1.attribute_id, l1.gps_record_time, l1.longitude, l1.latitude, l1.attribute_value,  l2.scan, l1.name from logged_data l1 left outer join logged_scans l2 on (l1.vehicle_id = l2.vehicle_id and l1.attribute_id=0 and l2.attribute_id=0 and l1.gps_record_time = l2.gps_record_time)  where l1.vehicle_id=? and l1.attribute_id=0 order by l1.gps_record_time");
			for (int i=0,is = vehList.size(); i<is;i++) {
				VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehList.get(i), true, false);
				if (vdf == null)
					continue;
				synchronized (vdf) {
					NewVehicleData vdt = vdf.getDataList(conn, vehList.get(i), 0, true);
					if (vdt == null)
						continue;
					synchronized (vdt) {
						ps.setInt(1, vehList.get(i));
						
						rs = ps.executeQuery();
						while (rs.next()) {
							GpsData distGpsData = new GpsData(Misc.sqlToLong(rs.getTimestamp(3)));
							distGpsData.setPoint(rs.getDouble(4), rs.getDouble(5));
							distGpsData.setDimensionInfo(0, rs.getDouble(6));
							distGpsData.setStrData(rs.getString(7));
							String locName = rs.getString(8);
							updateCacheAndCheckIfFirst(conn, distGpsData.getStrData(), vehList.get(i), distGpsData.getGps_Record_Time(), distGpsData, locName, vdt);
						}
						rs.close();
						rs = null;
					}//sync vdt
				}//sync vdf
			}//for each vehicle
			ps.close();
			ps = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	private static Triple<Integer, Integer, ArrayList<Integer>> getControlParams(Connection conn, int ownerOrgId) throws Exception {
		int minTimeMin = 15;
		int maxTimeMin = 180;
		ArrayList<Integer> regIdList = null;
		Cache cache = Cache.getCacheInstance(conn);
		MiscInner.PortInfo portInfo = cache.getPortInfo(ownerOrgId, conn);
		ArrayList<Integer> minTimeList = portInfo.getIntParams(OrgConst.ID_INT_SCAN_MIN_TIME, true);
		ArrayList<Integer> maxTimeList = portInfo.getIntParams(OrgConst.ID_INT_SCAN_MAX_TIME, true);
		 regIdList = portInfo.getIntParams(OrgConst.ID_INT_SCAN_OFFICE_REGION_ID, true);
		minTimeMin = minTimeList != null && minTimeList.size() > 0 ? minTimeList.get(0) : Misc.getUndefInt();
		if (minTimeMin < 0)
			minTimeMin = 15;
		maxTimeMin = maxTimeList != null && maxTimeList.size() > 0 ? maxTimeList.get(0) : Misc.getUndefInt();
		if (maxTimeMin < 0)
			maxTimeMin = 180;
		return new Triple<Integer, Integer, ArrayList<Integer>>(minTimeMin, maxTimeMin, regIdList);
	}
	
	private static void clearCurrScanDB(Connection conn, int vehicleId, String scan) throws Exception {
		updateCurrScanDB(conn, vehicleId, scan, (long)Misc.getUndefInt(), (long)Misc.getUndefInt());
	}
	private static void updateCurrScanDB(Connection conn, int vehicleId, String scan, long firstScanTS, long secondTS) throws Exception {
		PreparedStatement ps = conn.prepareStatement("update current_scans set first_scan_vehicle_id = ?, first_scan_ts=?, second_scan_ts = ? where (scan=? or ? is null)");
		ps.setInt(1, vehicleId);
		ps.setTimestamp(2, Misc.longToSqlDate(firstScanTS));
		ps.setTimestamp(3, Misc.longToSqlDate(firstScanTS));
		ps.setString(4, scan);
		ps.setString(5, scan);
		ps.execute();
		ps.close();
		ps = null;
		
	}
	public static void updateCacheAndCheckIfFirst(Connection conn, String id, int vehicleId, long scanTS, GpsData distGpsData, String locName, NewVehicleData vehicleData) {
		//First we will adjust the currentScans
		//The purely based on region in/out or passage of time or changes in curr scan we close/create trip
		//Adjusting currentScan
		//1. if str -> 
		//      check if currScan vehicle Id != prevVehicleId - if so close trip of prevVehicle. Also remove currentScan of previous ...
		//      check if valid scan (ie scan of same card more than minimum time after)
		//       if it is not validscan -  ignore
		//       if it is validscan -  if same then mark as second scan or first scan
		//       
		//       Now check if just exited office area - in which case close previous and start with new trip
		//        
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isFirst = true;
		try {
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			if (vehSetup == null)
				return;
			int newCurrCount = 0;
			int oldCurrCount = 0;
			int ownerOrgId = vehSetup.m_ownerOrgId;
			Triple<Integer, Integer, ArrayList<Integer>> ctrlParam = getControlParams(conn, ownerOrgId);
			int minTimeMin = ctrlParam.first;
			int maxTimeMin = ctrlParam.second;
			ArrayList<Integer> regIdList = ctrlParam.third;
			int prevVehicleId = Misc.getUndefInt(); //if undef means we removed scan from previous vehicle
			ArrayList<Pair<Long, String>> currScan = ScanSupport.getScans(conn, vehicleId);
			if (currScan != null)
				oldCurrCount = currScan.size();
			if (id != null && !id.startsWith("0x00")) {
				Triple<Integer, Long, Long> currExistingScan = ScanSupport.getCurrScan(conn, id);
				prevVehicleId = currExistingScan.first;
				long firstTS = currExistingScan.second;
				long secondTS = currExistingScan.third;
				if (!Misc.isUndef(prevVehicleId) && prevVehicleId != vehicleId && firstTS > 0) {
					//to avoid deadlock we cant take lock on vehicleDataInfo of another vehicle ... so look from db
					ps = conn.prepareStatement("select attribute_value, gps_record_time, name from logged_data where vehicle_id=? and attribute_id=0 and gps_record_time <= ? order by gps_record_time desc limit 1");
					ps.setInt(1, vehicleId);
					ps.setTimestamp(2, Misc.longToSqlDate(scanTS));
					
					double prevav = Misc.getUndefDouble();
					String prevLocName = null;
					long ts1 = scanTS;
					rs  = ps.executeQuery();
					if (rs.next()) {
						prevav = Misc.getRsetDouble(rs, 1);
						ts1 = Misc.sqlToLong(rs.getTimestamp(2));
						prevLocName = rs.getString(3);
					}
					rs.close();
					rs = null;
					ps.close();
					ps = null;
					GpsData pv = new GpsData(ts1);
					pv.setDimensionInfo(0, prevav);
					ScanSupport.closeCurrentTrip(conn, prevVehicleId, pv, prevLocName, id+" scanned in new vehicle:"+vehicleId);
					ScanSupport.clearCurrScanDB(conn, prevVehicleId, null);
					ScanSupport.removeCachedScanForVehicle(conn, prevVehicleId);
				}
				boolean validScan = true;
				if (!Misc.isUndef(prevVehicleId) && prevVehicleId == vehicleId) {
					long ts = secondTS;
					if (ts <= 0)
						ts = firstTS;
					if (ts > 0) {
						int gapMin = (int) ((scanTS - ts)/(60*1000));
						validScan = gapMin > minTimeMin;
					}
				}
				long newFirstTS = Misc.getUndefInt();
				long newSecondTS = Misc.getUndefInt();
				boolean toUpdateCurrentScan = false;
				if (!validScan) {
				}
				else {
					toUpdateCurrentScan = true;
					if (!Misc.isUndef(prevVehicleId) && firstTS > 0 && secondTS > 0) {
						ScanSupport.removeCachedScanForVehicle(conn, vehicleId, id);
						newFirstTS = firstTS;
						newSecondTS = scanTS;
					}
					else {
						ScanSupport.addCachedScanForVehicle(conn, vehicleId, id, scanTS);
						newFirstTS = scanTS;
					}
				}
				if (toUpdateCurrentScan) {
					ScanSupport.updateCurrScanDB(conn, vehicleId, id, newFirstTS, newSecondTS);
				}
			}//if valid string
		
			//now check if just exited region ... brute force approach
			GpsData prev = vehicleData.get(conn, distGpsData, -1);
			int prevRegId = Misc.getUndefInt();
			if (prev != null) {
				prevRegId = getRegionIn(conn, regIdList, prev.getPoint());
			}
			int currRegId = getRegionIn(conn, regIdList, distGpsData.getPoint());
			currScan = ScanSupport.getScans(conn, vehicleId);
			newCurrCount = currScan == null ? 0 : currScan.size();
			if (!Misc.isUndef(prevRegId) && prevRegId != currRegId) {
				//exited out of office region ..
				//now look at the points still within region ... and if these would have occurred before more than 2*min then these must be closed ... previous trip closed etc
				ScanSupport.closeCurrentTrip(conn, vehicleId, distGpsData, locName, "Exited region:"+prevRegId);
				
				int minThresh = minTimeMin*2;
				for (int i1=currScan == null ? -1 : currScan.size()-1; i1>=0;i1--) {
					Pair<Long, String> entry = currScan.get(i1);
					long gap = (int) ((scanTS - entry.first)/(60000.0));
					if (gap > minThresh) {
						ScanSupport.removeCachedScanForVehicle(conn, vehicleId, entry.second);
						ScanSupport.clearCurrScanDB(conn, vehicleId, entry.second);
					}
				}
				GpsData useForCreateTrip = distGpsData;
				String useName = locName;
				if (currScan != null && currScan.size() > 0) {
					useForCreateTrip = vehicleData.get(conn, new GpsData(currScan.get(0).first));
					useName = useForCreateTrip.getName(conn, vehicleId, vehSetup);
				}
				int newTripId = createNewTrip(conn, vehicleId, useForCreateTrip, useName, currScan == null ? 0 : currScan.size(), "Exited region. First scan:"+(currScan != null && currScan.size() > 0 ? currScan.get(0).second : " No scan"));
			}
			else if (Misc.isUndef(currRegId)) { //see if there is enough
				//see if enough time has passed - if so we close ..
				long lastTime = currScan == null || currScan.size() == 0 ? (long) Misc.getUndefInt() : currScan.get(currScan.size()-1).first;
				int gapMin = (int) (scanTS - lastTime)/60000;
				if (gapMin > maxTimeMin) {
					ScanSupport.removeCachedScanForVehicle(conn, vehicleId);
					ScanSupport.clearCurrScanDB(conn, vehicleId, null);
					ScanSupport.closeCurrentTrip(conn, vehicleId, distGpsData, locName, "Excess time passed");
				}
				else if (newCurrCount != oldCurrCount) {
					int tripToUpdate = Misc.getUndefInt();
					if (newCurrCount == 0 && oldCurrCount != 0) { //close it
						ScanSupport.closeCurrentTrip(conn, vehicleId, distGpsData, locName, "Scan count going to 0:"+id);
					}
					else if (oldCurrCount == 0 && newCurrCount > 0) {
						//if there exists an existing trip - update it else create new
						int oldTripId = getCurrTripId(conn, vehicleId, scanTS);
						if (!Misc.isUndef(oldTripId)) {
							tripToUpdate = oldTripId;
						}
						else {
							ScanSupport.createNewTrip(conn, vehicleId, distGpsData, locName, newCurrCount, "Scan count increasing:"+id);
						}
					}
					else if (newCurrCount > oldCurrCount) {
						tripToUpdate = getCurrTripId(conn, vehicleId, scanTS);						
					}
					if (!Misc.isUndef(tripToUpdate)) {
						ps = conn.prepareStatement("update demo_scan_trip set mx_cnt = (case when ? > mx_cnt then ? else mx_cnt end) where id = ?");
						ps.setInt(1, newCurrCount);
						ps.setInt(2, newCurrCount);
						ps.setInt(3, tripToUpdate);
						ps.execute();
						ps.close();
						ps = null;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (Exception e2) {
					
				}
			}
			if (ps != null) {
				try {
					ps.close();
				}
				catch (Exception e2) {
					
				}
			}
		}
		
	}
	
	private static int getCurrTripId(Connection conn, int vehicleId, long scanTS) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("select max(id) from demo_scan_trip where vehicle_id = ? and combo_start <= ? and combo_end is null");
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.longToSqlDate(scanTS));
			ResultSet rs = ps.executeQuery();
			int tripId = Misc.getUndefInt();
			if (rs.next()) {
				tripId = Misc.getRsetInt(rs,1);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			return tripId;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static int createNewTrip(Connection conn, int vehicleId, GpsData currData, String locName, int cnt, String createReason) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("insert into demo_scan_trip(combo_start, start_odo, start_loc, vehicle_id, mx_cnt, create_reason) values (?,?,?,?,?,?)");
			ps.setTimestamp(1, Misc.longToSqlDate(currData.getGps_Record_Time()));
			ps.setDouble(2, currData.getValue());
			ps.setString(3, locName);
			ps.setInt(4, vehicleId);
			ps.setInt(5, cnt);
			ps.setString(6, createReason);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			rs.next();
			int tripId = rs.getInt(1);
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			return tripId;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private static void closeCurrentTrip(Connection conn, int vehicleId, GpsData currData, String locName, String reason) throws Exception {
		try {
			int tripId = getCurrTripId(conn, vehicleId, currData.getGps_Record_Time());
			if (!Misc.isUndef(tripId)) {
				PreparedStatement ps = conn.prepareStatement("update demo_scan_trip set combo_end = ?, end_odo=?, end_loc=?, close_reason=? where id=?");
				ps.setTimestamp(1, Misc.longToSqlDate(currData.getGps_Record_Time()));
				ps.setDouble(2, currData.getValue());
				ps.setString(3, locName);
				ps.setString(4, reason);
				ps.setInt(5, tripId);
				ps.execute();
				ps.close();
				ps = null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private static int getRegionIn(Connection conn, ArrayList<Integer> regList, Point point) throws Exception {
		ArrayList<RegionTestHelper> result = RTreeSearch.getContainingRegions(point);
		for (int i=0,is = result == null ? 0 : result.size(); i<is; i++) {
			int fndId = result.get(i).region.id;
			for (int j=0,js = regList.size(); j<js;j++) {
				if (fndId == regList.get(j))
					return fndId;
			}
		}
		return Misc.getUndefInt();
	}
	

	public static void main(String[] args) {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			redo(conn, Misc.getUndefInt());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, false);
			}
			catch (Exception e2) {
				
			}
		}
	}
}

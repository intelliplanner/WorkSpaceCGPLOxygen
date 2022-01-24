package com.ipssi.eta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.ChallanInfo;
import com.ipssi.common.ds.trip.NewChallanMgmt;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.ExtLUInfoExtract;
import com.ipssi.tripcommon.LUInfoExtract;

public class Tester {
	public static void main(String[] args) throws Exception {
		callMain(args);
	}
	public static void callMain(String[] args) throws Exception {
		testBasics(args);
	}

	public static void testBasics(String[] args) throws Exception {
		Connection conn = null;
		try {/*
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int vehicleId = 25286;
			//basically have set up two Src+default+common starting at 1 place .. going to two
			//will start from 1 op
			// goto intermediate pt of first - which still does not differentiate
			// then second which distinguishes
			//then switch to other one to see if things are happening 
			//and then finally opstation reach .. will exercise paths of path selection, migration etc and some alerts
			
			NewSrcDestProfileCache.Helper helper = new NewSrcDestProfileCache.Helper(conn, vehicleId);
			ArrayList<Integer> allsd = helper.dbgHelperGetAllSD(conn);
			removeEmptySD(conn, allsd);
			Collections.sort(allsd);
			SrcDestInfo firstSD = allsd == null || allsd.size() < 1 ? null : SrcDestInfo.getSrcDestInfo(conn, allsd.get(0));
			SrcDestInfo secondSD = allsd == null || allsd.size() < 2 ? null : SrcDestInfo.getSrcDestInfo(conn, allsd.get(1));
			boolean testingReverse = true;
			long now = Misc.getSystemTime();
			Tester.cleanup(conn, vehicleId, true, true, true, true);
			if (!conn.getAutoCommit())
				conn.commit();
			long grt = addDays(now, -2);
			long recv = -1; //if -1 is passed then automatically in insert updated on taken as grt
			double lon = Misc.getUndefDouble();
			double lat = Misc.getUndefDouble();
			double speed = 10;
			double attributeValue = 0;
			double distIncr = 30;
			GpsData data = null;
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false);
			NewVehicleData vdp = vdf.getDataList(conn, vehicleId, 0, true);
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			StopDirControl stopDirControl = vehSetup.getStopDirControl(conn);
			NewVehicleETA vehicleETA = NewVehicleETA.getETAObj(conn, vehicleId);
			//test saving func
			vehicleETA.saveState(conn, vdp, true, true, grt);
			vehicleETA.saveState(conn, vdp, true, false, grt);
			LUInfoExtract inExt = new ExtLUInfoExtract();
			LUInfoExtract outExt = new ExtLUInfoExtract();
			inExt.setOfOpStationId(testingReverse ? firstSD.getDestId() : firstSD.getSrcId());
			outExt.setOfOpStationId(testingReverse ? firstSD.getSrcId() : firstSD.getDestId());
			
			ChallanInfo challanInfo = null;
			String ptName = null;
			//inside
			lon = testingReverse ? firstSD.getDestLong() : firstSD.getSrcLong();
			lat = testingReverse ? firstSD.getDestLat() : firstSD.getSrcLat();
			ptName = "Inside Src Pt1";
			System.out.println("[DBG Start] "+ptName);
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			inExt.setGateIn(data.getGps_Record_Time());
			vehicleETA.doUpdateForTrip(conn, vehicleId, vehSetup, inExt, null, challanInfo, stopDirControl, vdp, vdp.getLatestReceivedTime());
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);

			//still inside
			ptName = "Still Inside Src Pt2";
			System.out.println("[DBG Start] "+ptName);
			attributeValue += distIncr;
			grt = addMinutes(grt, 90);
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);

			//exitted src
			ptName = "Exitted Src";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 120);
			attributeValue += distIncr;
			lon = (lon+firstSD.getWaypoints().get(testingReverse ? 1 : 0).getLongitude())/2;
			lat = (lat+firstSD.getWaypoints().get(testingReverse ? 1 : 0).getLatitude())/2;
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			inExt.setGateOut(data.getGps_Record_Time());
			vehicleETA.doUpdateForTrip(conn, vehicleId, vehSetup, inExt, null, challanInfo, stopDirControl, vdp, vdp.getLatestReceivedTime());
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);
			
			//did not reach intermediate1
			ptName = "Did not reach SD1's IM1";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 430);
			attributeValue += distIncr;
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);
			
			boolean createChallan = false;
			if (createChallan) { //we should see a switch and thereafter that is the only remains ... 
				int fromStationId = vehicleETA.getCurrFromOpStationId();
				int currPossibleSDId = vehicleETA.getCurrPossibleSrcDestList().get(0).first;
				int toStationId = currPossibleSDId == firstSD.getId() ? secondSD.getDestId() : firstSD.getDestId();
				challanInfo = createChallan(conn, vehicleId, fromStationId, toStationId, addMinutes(vehicleETA.getCurrFromOpStationInTime(), 5));
				System.out.println("[DBG Begin Challan] ");
				vehicleETA.doUpdateForTrip(conn, vehicleId, vehSetup, inExt, null, challanInfo, stopDirControl, vdp, vdp.getLatestReceivedTime());
				data = vdp.getLast(conn);
				vehicleETA.processAllData(conn, data.getGps_Record_Time()-1000, vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
				System.out.println(vehicleETA);
				System.out.println("[DBG End After Challan] ");

				//reprocess for old pt ..we should see additional alert for exitted src
			}
			
			boolean skipFirst = false;
			boolean skipSecond = false;

			//reached intermediate1
			if (!skipFirst) {//if skipped we should see only 1 skipped intermediate alert - when switching to other, this one's alert shld copy over
				ptName = "Reached SD1's IM1";
				System.out.println("[DBG Start] "+ptName);
				grt = addMinutes(grt, 420);
				attributeValue += distIncr;
				lon = firstSD.getWaypoints().get(testingReverse ?1:0).getLongitude();
				lat = firstSD.getWaypoints().get(testingReverse ?1:0).getLatitude();
				data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
				vdp.add(conn, data);
				vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
				System.out.println(vehicleETA);
				System.out.println("[DBG End] "+ptName);
			}
			//we should see only 1 skipped intermediate alert - when switching to other, this one's alert shld copy over
			//reached secondIntermediate
			if (!skipSecond) {//if skipped we shoud see skipped intermediate when dest reached ...
				ptName = "Reached SD1's IM2 - now 1 only possible";
				System.out.println("[DBG Start] "+ptName);
				grt = addMinutes(grt, 420);
				attributeValue += distIncr;
				lon = firstSD.getWaypoints().get(testingReverse ? 0 : 1).getLongitude();
				lat = firstSD.getWaypoints().get(testingReverse ? 0 : 1).getLatitude();
				data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
				vdp.add(conn, data);
				vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
				System.out.println(vehicleETA);
				System.out.println("[DBG End] "+ptName);
			}

			//switching 2nd SD
			ptName = "Reached SD2's IM2 - switch to SD2";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 420);
			attributeValue += distIncr;
			lon = secondSD.getWaypoints().get(testingReverse ? 0 : 1).getLongitude();
			lat = secondSD.getWaypoints().get(testingReverse ? 0 : 1).getLatitude();
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);

			//not reached Dest
			ptName = "Did not reach dest aft IM2";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 120);
			attributeValue += distIncr;
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);

			//reached Dest (of firstSD!! - switching back)
			ptName = "Reaching Dest of SD1";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 120);
			attributeValue += distIncr;
			lon = testingReverse ? firstSD.getSrcLong() : firstSD.getDestLong();
			lat = testingReverse ? firstSD.getSrcLat() : firstSD.getDestLat();
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			outExt.setGateIn(data.getGps_Record_Time());
			vehicleETA.doUpdateForTrip(conn, vehicleId, vehSetup, inExt, outExt, challanInfo, stopDirControl, vdp, vdp.getLatestReceivedTime());
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);
			
			//still inside
			ptName = "Still inside dest of SD1";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 120);
			attributeValue += distIncr;
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);
			System.out.println("[DBG End] "+ptName);

			//now exiting
			ptName = "Exitted dest, at IM2 of SD2";
			System.out.println("[DBG Start] "+ptName);
			grt = addMinutes(grt, 120);
			attributeValue += distIncr;
			lon = secondSD.getWaypoints().get(testingReverse ? 0 : 1).getLongitude();
			lat =  secondSD.getWaypoints().get(testingReverse ? 0: 1).getLatitude();
			data = Tester.insertIntoLogData(conn, vehicleId, lon, lat, grt, attributeValue, speed, recv, ptName);
			vdp.add(conn, data);
			outExt.setGateOut(data.getGps_Record_Time());
			vehicleETA.doUpdateForTrip(conn, vehicleId, vehSetup, inExt, outExt, challanInfo, stopDirControl, vdp, vdp.getLatestReceivedTime());
			vehicleETA.processAllData(conn, data.getGps_Record_Time(), vdp, stopDirControl, vdf, vehSetup, true, vdp.getLatestReceivedTime());
			System.out.println(vehicleETA);			
			System.out.println("[DBG End] "+ptName);

			int dbg=1;
			dbg++;
		*/}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null)
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
			conn = null;
		}
	}
	private static ChallanInfo createChallan(Connection conn, int vehicleId, int fromStationId, int toStationId, long ts) throws Exception {
		OpStationBean fromOp = TripInfoCacheHelper.getOpStation(fromStationId);
		OpStationBean toOp = TripInfoCacheHelper.getOpStation(toStationId);
		String fromLoc = fromOp.getOpStationName();
		String toLoc = toOp.getOpStationName();
		PreparedStatement ps = conn.prepareStatement("insert into challan_details(vehicle_id, gr_no_, challan_date, updated_on, challan_rec_date "+
				" , from_station_id, to_station_id, from_location, to_location, trip_status, port_node_id) "+
				" values(?,?,?,?,?"+
				" ,?,?,?,?,?,?)"
				);
	   int colIndex = 1;
	   ps.setInt(colIndex++, vehicleId);
	   ps.setInt(colIndex++, 1);
	   ps.setTimestamp(colIndex++, Misc.longToSqlDate(ts));
	   ps.setTimestamp(colIndex++, Misc.longToSqlDate(ts));
	   ps.setTimestamp(colIndex++, Misc.longToSqlDate(ts));
	   ps.setInt(colIndex++, fromStationId);
	   ps.setInt(colIndex++, toStationId);
	   ps.setString(colIndex++, fromLoc);
	   ps.setString(colIndex++, toLoc);
	   ps.setInt(colIndex++,1);
	   ps.setInt(colIndex++, 2);
	   ps.executeUpdate();
	   ResultSet rs = ps.getGeneratedKeys();
	   int chId = Misc.getUndefInt();
		if (rs.next()) {
			chId = rs.getInt(1);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ArrayList<Integer> lst = new ArrayList<Integer>();
		lst.add(chId);
		ArrayList<Pair<Integer, ChallanInfo>> res = ChallanInfo.read(conn, lst);
		ChallanInfo retval = res == null || res.size() == 0? null : res.get(0).second;
		NewChallanMgmt chmgt = NewChallanMgmt.getExtChallanList(conn, vehicleId);
		chmgt.add(conn, retval);
		return retval;
		
	}
	private static long addDays(long curr, double days) {
		curr += days*24*3600*1000;
		curr = (curr/1000)*1000;
		return curr;
	}
	private static long addMinutes(long curr, double minute) {
		curr += minute*60*1000;
		curr = (curr/1000)*1000;
		return curr;
	}
	private static GpsData insertIntoLogData(Connection conn, int vehicleId, double lon, double lat, long grt, double attributeValue, double speed, long recv, String name) throws Exception {
		PreparedStatement ps = conn.prepareStatement("insert ignore into logged_data (vehicle_id, attribute_id, gps_record_time, longitude, latitude, attribute_value, speed, updated_on, name, source) "+
				" values(?,?,?,?,?,?,?,?,?,?)");
		int colIndex = 1;
		if (recv < 0)
			recv = grt;
		Misc.setParamInt(ps, vehicleId, colIndex++);
		Misc.setParamInt(ps, 0, colIndex++);
		ps.setTimestamp(colIndex++, Misc.longToSqlDate(grt));
		Misc.setParamDouble(ps, lon, colIndex++);
		Misc.setParamDouble(ps, lat, colIndex++);
		Misc.setParamDouble(ps, attributeValue, colIndex++);
		Misc.setParamDouble(ps, speed, colIndex++);
		ps.setTimestamp(colIndex++, Misc.longToSqlDate(grt));
		ps.setString(colIndex++, name);
		Misc.setParamInt(ps, 0, colIndex++);
		ps.execute();
		ps.close();
		GpsData retval = new GpsData(grt);
		retval.setPoint(lon, lat);
		retval.setDimensionInfo(0, attributeValue);
		retval.setSpeed(speed);
		retval.setSourceChannel(ChannelTypeEnum.CURRENT);
		retval.setGpsRecvTime(recv);
		return retval;
	}
	private static void cleanup(Connection conn, int vehicleId, boolean doLog, boolean doAlert, boolean doState, boolean doChallan) throws Exception {
		if (doLog) {
			PreparedStatement ps = conn.prepareStatement("delete from logged_data where vehicle_id=?");
			ps.setInt(1, vehicleId);
			ps.executeUpdate();
			ps.close();
		}
		if (doAlert) {
			PreparedStatement ps = conn.prepareStatement("delete from eta_alerts_new where vehicle_id=?");
			ps.setInt(1, vehicleId);
			ps.executeUpdate();
			ps.close();
		}
		if (doState) {
			PreparedStatement ps = conn.prepareStatement("delete from eta_obj_state where vehicle_id=?");
			ps.setInt(1, vehicleId);
			ps.executeUpdate();
			ps.close();
		}
		if (doChallan) {
			PreparedStatement ps = conn.prepareStatement("delete from challan_details where vehicle_id=?");
			ps.setInt(1, vehicleId);
			ps.executeUpdate();
			ps.close();
		}
	}
	private static void removeEmptySD(Connection conn, ArrayList<Integer> sdlist) {
		for (int i=sdlist == null ? -1 : sdlist.size()-1; i>=0; i--) {
			SrcDestInfo sd = SrcDestInfo.getSrcDestInfo(conn, sdlist.get(i));
			if (Misc.isUndef(sd.getSrcLong()) && Misc.isUndef(sd.getDestLong())) {
				sdlist.remove(i);
			}
		}
	}
}

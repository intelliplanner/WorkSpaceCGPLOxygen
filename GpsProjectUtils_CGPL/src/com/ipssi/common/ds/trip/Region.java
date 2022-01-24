package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.Vehicle;

public class Region {
	public static boolean checkInOutRegion(Connection conn, int regionId,Vehicle vehicle, ThreadContextCache threadContextCache, OpStationBean inThisOpStation, int ownerOrgId, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws SQLException, GenericException, Exception{
		return checkInOutRegion(conn, regionId, vehicle.getGpsData(), threadContextCache, inThisOpStation, ownerOrgId, vehicleControlling, vehSetup);
	}
	
	public static boolean checkInOutRegion(Connection conn, int regionId, GpsData data, ThreadContextCache threadContextCache, OpStationBean inThisOpStation, int ownerOrgId, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws SQLException, GenericException, Exception{
		if (inThisOpStation != null && !Misc.isUndef(inThisOpStation.getLinkedVehicleId())) {
			//center and get it from there
			StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);

			boolean isWait = regionId == inThisOpStation.getWaitAreaId();
			ThreadContextCache.SimpleMoving movingInfo = threadContextCache.getMovingOpStationContaining(conn, ownerOrgId, data, inThisOpStation, vehicleControlling, vehSetup);
			if (movingInfo != null) {
				if (isWait)
					return true;
				return inThisOpStation.isPointLinkedGate(conn, data.getPoint(), movingInfo.getCenter(), stopDirControl);
			}
			return false;
		}
		return RegionTest.PointIn(conn,data.getPoint(), (int)regionId);
	}
	
	public static int checkInOutRegion(Connection conn, List<OpStationBean> opStList, Vehicle vehicle, ThreadContextCache threadContextCache, OpStationBean inThisOpStation) throws SQLException, GenericException, Exception{
		if(opStList == null || opStList.size() <= 0)
			return Misc.getUndefInt();
		else{
			for (int i = 0; i < opStList.size(); i++) {
				if (RegionTest.PointIn(conn,vehicle.getGpsData().getPoint(), (int)opStList.get(i).getWaitAreaId())) {
				    return i;	
				}
			}
			return Misc.getUndefInt();
		}
	}

	public static OpArea checkInOutRegionList(Connection conn, List<OpArea> opStList,Vehicle vehicle) throws SQLException, GenericException, Exception{
		if(opStList == null || opStList.size() <= 0)
			return null;
		else{
			for (int i = 0; i < opStList.size(); i++) {
				if (opStList.get(i).isValidForDate(vehicle.getGpsData().getGps_Record_Time()) && RegionTest.PointIn(conn, vehicle.getGpsData().getPoint(), opStList.get(i).id)) {
				    return opStList.get(i);//new Triple<Integer,Integer,Integer>(opStList.get(i).first, opStList.get(i).third, opStList.get(i).fourth);	
				}
			}
			return null;
			//for (int i = 0; i < opStList.size(); i++) {
			//	int l = opStList.get(i);
			//	opStListConverted.add((int)l);
			//}
			//return (int) RegionTest.PointIn(conn, new Point(vehicle.getGpsData().getPoint().getLongitude(), vehicle.getGpsData().getPoint().getLatitude()), opStListConverted, true);
		}
	}
	public double getShortestDistFromEdge(double lon, double lat) {
		return this.getShortestDistFromEdge(lon, lat);
	}
	public static Pair<Boolean, Integer> checkInOutRegionList(Connection conn, List<Integer> opStList,Vehicle vehicle, ThreadContextCache threadContextCache, OpStationBean inThisOpStation) throws SQLException, GenericException, Exception{
		if(opStList == null || opStList.size() <= 0)
			return new Pair<Boolean, Integer> (false,Misc.getUndefInt());
		else{
			for (int i = 0; i < opStList.size(); i++) {
				if (RegionTest.PointIn(conn, vehicle.getGpsData().getPoint(), opStList.get(i))) {
				    return new Pair<Boolean, Integer> (true,opStList.get(i));//returns first overlapping regionId from list, priority based handling can be done for more accuracy;	
				}
			}
			return new Pair<Boolean, Integer> (false,Misc.getUndefInt());
		}
	}
	
}

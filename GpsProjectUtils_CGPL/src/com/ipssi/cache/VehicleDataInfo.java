package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.processor.utils.GpsData;

public class VehicleDataInfo {
	public static final String GET_ODOMETER_REC_TIME = "select vehicle.id, attribute_id, odometer_day_rec_time, odometer_week_rec_time, odometer_month_rec_time from vehicle left outer join current_data on vehicle.id = current_data.vehicle_id where vehicle.status in (1) ";//and current_data.attribute_id in (0,2,21)";
	//above different from old DP code - also notes down if there is current record
	public static final String GET_INFO_FROM_VEHICLE_1 = "select vehicle.id, vehicle.io_set_id, (case when device_internal_id is null then device_serial_number else device_internal_id end) device_id, vehicle.redirect_url, lgd.attribute_id, lgd.mi, lgd.mx from vehicle left outer join (select vehicle_id, attribute_id, min(gps_record_time) mi, max(gps_record_time) mx from logged_data "; 
	public static final String GET_INFO_FROM_VEHICLE_2 = " group by vehicle_id,  attribute_id) lgd on (lgd.vehicle_id = vehicle.id) where vehicle.status in (1) ";
	public static final String GET_INFO_FROM_VEHICLE_GROUP_PART = " order by vehicle.id, lgd.attribute_id ";
	//public static final String GET_INFO_FROM_VEHICLE = "select vehicle.id, vehicle.io_set_id, (case when device_internal_id is null then device_serial_number else device_internal_id end) device_id, vehicle.redirect_url, lgd.attribute_id, min(lgd.gps_record_time), max(lgd.gps_record_time) from vehicle left outer join logged_data lgd on (lgd.vehicle_id = vehicle.id) where vehicle.status in (1) ";
	//public static final String GET_INFO_FROM_VEHICLE_GROUP_PART = " group by vehicle.id, vehicle.io_set_id, device_internal_id, device_serial_number,  vehicle.redirect_url, lgd.attribute_id order by vehicle.id, lgd.attribute_id ";
	
	private static int G_VEHICLE_COUNT = 4000;
	private static int G_VEHICLE_UPDATE_FREQ = 1;
	private String deviceId = null;
	private String redirectUrl = null;
	private int ioMapSetId = Misc.getUndefInt();
	private int vehicleId;
	private boolean inRecoveryMode = false;
	private byte useMode = 0;
	private boolean cummDistDelta = false;
	private boolean cummDistSensorBased = false;
	private ArrayList<Pair<Integer, NewVehicleData>> dataList = new ArrayList<Pair<Integer, NewVehicleData>>();
	private static ConcurrentHashMap<Integer, VehicleDataInfo> g_data = new ConcurrentHashMap<Integer, VehicleDataInfo>(G_VEHICLE_COUNT, 0.75f,G_VEHICLE_UPDATE_FREQ);
	private static ConcurrentHashMap<String, VehicleDataInfo> g_dataByDeviceId = new ConcurrentHashMap<String, VehicleDataInfo> (G_VEHICLE_COUNT, 0.75f,G_VEHICLE_UPDATE_FREQ);
	
	private static ConcurrentHashMap<Integer, VehicleDataInfo> g_dataRP = new ConcurrentHashMap<Integer, VehicleDataInfo>(G_VEHICLE_COUNT, 0.75f,G_VEHICLE_UPDATE_FREQ);
	private static ConcurrentHashMap<Integer, VehicleDataInfo> g_dataTP = new ConcurrentHashMap<Integer, VehicleDataInfo>(G_VEHICLE_COUNT, 0.75f,G_VEHICLE_UPDATE_FREQ);
	
	private static boolean g_initDone = false; //not used ... to be used for preloading stuff
	private static boolean g_incrementalMode = true; //not used
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Vehicle:").append(vehicleId)
		.append(" ioMapSetId:").append(ioMapSetId)
		.append(" deviceId:").append(deviceId)
		.append("\n");
		for (int i=0,is=dataList.size();i<is;i++) {
			sb.append(dataList.get(i).second).append("\n");
		}
		return sb.toString();
	}
	public void setUseMode(int mode) {
		useMode = (byte)mode;
		for (Pair<Integer, NewVehicleData> entry: dataList)
			if (entry != null && entry.second != null)
				entry.second.setUseMode(mode);
	}
	public static VehicleDataInfo getVehicleDataInfoByDeviceId(String deviceId) {
		return deviceId == null || deviceId.length() == 0 ? null : g_dataByDeviceId.get(deviceId);
	}
	
	public static VehicleDataInfo getVehicleDataInfoTP(Connection conn, int vehicleId, boolean createIfMissing, boolean inRecoveryMode) throws Exception {
		return getVehicleDataInfo(conn, vehicleId, createIfMissing, g_data, inRecoveryMode);//g_dataTP);
	}
	public static VehicleDataInfo getVehicleDataInfoRP(Connection conn, int vehicleId, boolean createIfMissing, boolean inRecoveryMode) throws Exception {
		return getVehicleDataInfo(conn, vehicleId, createIfMissing, g_data, inRecoveryMode);//g_dataRP);
	}
	public static VehicleDataInfo getVehicleDataInfoDP(Connection conn, int vehicleId, boolean createIfMissing, boolean inRecoveryMode) throws Exception {
		return getVehicleDataInfo(conn, vehicleId, createIfMissing, g_data, inRecoveryMode);
	}
		
	public static VehicleDataInfo getVehicleDataInfo(Connection conn, int vehicleId, boolean createIfMissing, boolean inRecoveryMode) throws Exception {
		return getVehicleDataInfo(conn, vehicleId, createIfMissing, g_data, inRecoveryMode);
	}
	
	private static VehicleDataInfo getVehicleDataInfo(Connection conn, int vehicleId, boolean createIfMissing, ConcurrentHashMap<Integer, VehicleDataInfo> thisCache, boolean inRecoveryMode) throws Exception {
		createIfMissing = true; //ignore even if being asked to not create new
		Integer vehInt = new Integer(vehicleId);
		VehicleDataInfo retval = thisCache.get(vehInt);
		
		if (retval == null && createIfMissing) {
			synchronized (thisCache){
				System.out.println("[DBG] getVehicleDataInfo is null for:"+vehInt.intValue());
				retval = thisCache.get(vehInt);
				if (retval != null)
					return retval;
				ArrayList<Integer> vehicleList = null;
				if (!thisCache.isEmpty()) {
					vehicleList = new ArrayList<Integer>();
					vehicleList.add(vehInt);
				}
				loadVehicle(vehicleList, conn, thisCache, inRecoveryMode);
				retval = thisCache.get(vehInt);
				System.out.println("[DBG] getVehicleDataInfo return:"+retval);
			}
		}
		return retval;
	}
	
	private GpsData getLatestReceivedData() {
		GpsData retval = null;
		
		if (this.dataList.size() > 0) {
			NewVehicleData vdt = dataList.get(0).second;
		retval = vdt.getLatestReceivedData();
		}
		return retval;
	}
	public GpsData getPrevReceivedData() {
		GpsData retval = null;
		
		if (this.dataList.size() > 0) {
			NewVehicleData vdt = dataList.get(0).second;
		retval = vdt.getLatestReceivedData();
		}
		return retval;
	}
	

	private void removeVehicleDataInfoDontUse(int vehicleId) {
		Integer vehInt = new Integer(vehicleId);
		VehicleDataInfo vehInfo = g_data.get(vehInt);
		if (vehInfo != null && vehInfo.deviceId != null && vehInfo.deviceId.length() != 0) {
			g_dataByDeviceId.remove(vehInfo.deviceId);
			g_data.remove(vehicleId);
		}
	}
	
	
	public void updateDeviceIdEtc(Connection conn, String deviceId, int ioMapSetId, String redirectUrl, boolean cummDistDelta, boolean cummDistSensorBased) {
		this.deviceId = deviceId;
		if (deviceId != null && deviceId.length() != 0)
			VehicleDataInfo.g_dataByDeviceId.put(deviceId, this);
		this.ioMapSetId = ioMapSetId;
		this.redirectUrl = redirectUrl;
		this.cummDistDelta = cummDistDelta;
		this.cummDistSensorBased = cummDistSensorBased;
	}
	
	
	public NewVehicleData getDataList(Connection conn, int vehicleId, int attributeId, boolean createIfMissing) {
		createIfMissing = true;
		Pair<Integer, NewVehicleData> entry = null;
		int idx = 0;
		for (int is=dataList.size();idx<is;idx++) {
			entry = dataList.get(idx);
			if (entry.first == attributeId)
				break;
			else if (entry.first > attributeId) {
				entry = null;
				break;
			}
			else {
				entry = null;
			}
		}
		if (entry == null && createIfMissing) {
			NewVehicleData data = new NewVehicleData(vehicleId, attributeId, (long)Misc.getUndefInt(), (long)Misc.getUndefInt(), inRecoveryMode, useMode);
			PreparedStatement ps2 = null;
			ResultSet rs2 = null;
			if (attributeId == 0) {
				try {
					
					ps2 = conn.prepareStatement("select 1 from op_station where vehicle_id=? and status=1");
					ps2.setInt(1, vehicleId);
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						data.setMaxCachedPtCountMultiple((byte)30);
					}
					rs2 = Misc.closeRS(rs2);
					ps2 = Misc.closePS(ps2);
				}
				catch (Exception e2) {
					
				}
				finally {
					rs2 = Misc.closeRS(rs2);
					ps2 = Misc.closePS(ps2);
				}
			}
			data.setGpsIdDelta(this.cummDistDelta);
			data.setCummDistSensorBased(cummDistSensorBased);
			entry = new Pair<Integer, NewVehicleData>(attributeId, data);
			if (idx==dataList.size())
				dataList.add(entry);
			else
				dataList.add(idx, entry);
		}
		if (entry != null && entry.second != null) {
			entry.second.setUseMode(useMode);
		}
		return entry == null ? null : entry.second;
	}
	
	public static VehicleDataInfo getStandaloneVehicleDataInfo(int vehicleId, int useMode) {
		return new VehicleDataInfo(vehicleId, true);
	}
	public synchronized static void loadVehicle(ArrayList<Integer> vehicleIDs, Connection conn, ConcurrentHashMap<Integer, VehicleDataInfo> putInThis, boolean inRecoveryMode) throws Exception {// will NOT call loadMapSets in turn
		boolean doingDP = putInThis == g_data;
		HashMap<Integer, Integer> vehInOpStation = null;
		if (false) {
			vehInOpStation = new HashMap<Integer, Integer>();
		
			PreparedStatement ps2 = null;
			ResultSet rs2 = null;
			try {
				ps2 = conn.prepareStatement("select distinct vehicle_id from op_station where status=1");
				rs2 = ps2.executeQuery();
				while (rs2.next()) {
					int vid = Misc.getRsetInt(rs2, 1);
					if (!Misc.isUndef(vid)) {
						Integer v = new Integer(vid);
						vehInOpStation.put(v, v);
					}
				}
				rs2 = Misc.closeRS(rs2);
				ps2 = Misc.closePS(ps2);
			}
			catch (Exception e2) {
				
			}
			finally {
				rs2 = Misc.closeRS(rs2);
				ps2 = Misc.closePS(ps2);
			}
		}
		StringBuilder query = new StringBuilder(GET_INFO_FROM_VEHICLE_1);
		StringBuilder odometerQuery = new StringBuilder(GET_ODOMETER_REC_TIME); 
		if (vehicleIDs != null && vehicleIDs.size() != 0) {
			query.append(" where vehicle_id ");
			if (vehicleIDs.size() == 1){
				query.append(" = ? ");
			}
			else {
				query.append(" in (");
				Misc.convertInListToStr(vehicleIDs, query);
				query.append(") ");
			}
		}
		query.append(GET_INFO_FROM_VEHICLE_2);
		if (vehicleIDs != null && vehicleIDs.size() != 0) {
			query.append(" and vehicle.id ");
			odometerQuery.append(" and vehicle.id ");
			if (vehicleIDs.size() == 1){
				query.append(" = ? ");
				odometerQuery.append(" = ? ");
			}
			else {
				query.append(" in (");
				odometerQuery.append(" in (");
				Misc.convertInListToStr(vehicleIDs, query);
				Misc.convertInListToStr(vehicleIDs, odometerQuery);
				query.append(") ");
				odometerQuery.append(") ");
			}
		}
		query.append(GET_INFO_FROM_VEHICLE_GROUP_PART);
		PreparedStatement ps = conn.prepareStatement(query.toString());
		
		if (vehicleIDs != null && vehicleIDs.size() == 1) {
			ps.setInt(1, vehicleIDs.get(0));
			ps.setInt(2,vehicleIDs.get(0));
		}
		ResultSet rs = ps.executeQuery();
		int prevVehicleId = Misc.getUndefInt();
		VehicleDataInfo vehicleDataInfo = null;
		HashMap<Integer, Integer> seenVehicle = new HashMap<Integer, Integer>();
		while (rs.next()) {
			int vehicleId = rs.getInt(1);
			Integer vehicleIdInt = new Integer(vehicleId);
			seenVehicle.put(vehicleIdInt, vehicleIdInt);
			boolean toCreateNew = prevVehicleId != vehicleId || vehicleDataInfo == null; 
			if (toCreateNew) {
			    vehicleDataInfo= new VehicleDataInfo(vehicleId, inRecoveryMode);
			    vehicleDataInfo.ioMapSetId = Misc.getRsetInt(rs, 2);
			    vehicleDataInfo.deviceId = rs.getString(3);
			    vehicleDataInfo.redirectUrl = rs.getString(4);
			    putInThis.put(vehicleIdInt, vehicleDataInfo);
			    if (putInThis == g_data && vehicleDataInfo.deviceId != null && vehicleDataInfo.deviceId.length() != 0)
			    	g_dataByDeviceId.put(vehicleDataInfo.deviceId, vehicleDataInfo);
			    prevVehicleId = vehicleId;
			}
			int attributeId = rs.getInt(5);
			long mi = Misc.sqlToLong(rs.getTimestamp(6));
			long mx = Misc.sqlToLong(rs.getTimestamp(7));
			if (!doingDP) {//dont assume any pt available
				mx = mi = Misc.getUndefInt();
			}
			NewVehicleData vehicleData = new NewVehicleData(vehicleId, attributeId, mi, mx, inRecoveryMode,0);
			if (attributeId == 0 && vehInOpStation != null && vehInOpStation.containsKey(vehicleId))
				vehicleData.setMaxCachedPtCountMultiple((byte)30);
			vehicleDataInfo.dataList.add(new Pair<Integer, NewVehicleData>(attributeId, vehicleData));
		}
		rs.close();
		ps.close();
		for (int i1=0,i1s=vehicleIDs == null ? 0 : vehicleIDs.size(); i1<i1s;i1++) {
			Integer vidInt = vehicleIDs.get(i1);
			if (!seenVehicle.containsKey(vidInt)) {
				vehicleDataInfo= new VehicleDataInfo(vidInt.intValue(), inRecoveryMode);
			    vehicleDataInfo.ioMapSetId = Misc.getUndefInt();
			    vehicleDataInfo.deviceId = null;
			    vehicleDataInfo.redirectUrl = null;
			    putInThis.put(vidInt, vehicleDataInfo);
			}
		}
		
		PreparedStatement ops = conn.prepareStatement(odometerQuery.toString());
		if (vehicleIDs != null && vehicleIDs.size() == 1) {
			ops.setInt(1, vehicleIDs.get(0));
		}
		ResultSet ors = ops.executeQuery();
		VehicleDataInfo vdt = null;
		while (ors.next()) {
			int vehicleId = ors.getInt(1);
			int attributeId = Misc.getRsetInt(ors, 2);
			if (Misc.isUndef(attributeId))
				continue;
			if (vdt == null || vdt.vehicleId != vehicleId) {
				vdt = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, putInThis, inRecoveryMode);
			}
			if (vdt == null)
				continue;
			NewVehicleData dataItem = vdt.getDataList(conn, vehicleId, attributeId, false);
			if (dataItem == null)
				continue;
			
			long odoDayRecTime = Misc.sqlToLong(ors.getTimestamp("odometer_day_rec_time"));
			long odoWeekRecTime = Misc.sqlToLong(ors.getTimestamp("odometer_week_rec_time"));
			long odoMonthRecTime = Misc.sqlToLong(ors.getTimestamp("odometer_month_rec_time"));
			dataItem.setOdometerDayRecTime(odoDayRecTime);
			dataItem.setOdometerWeekRecTime(odoWeekRecTime);
			dataItem.setOdometerMonthRecTime(odoMonthRecTime);
			dataItem.setHasCurrentRecord();
		}
		ors.close();
		ops.close();
		
	}

	private VehicleDataInfo(int vehicleId, boolean recoveryMode) {
		super();
		this.vehicleId = vehicleId;
		this.inRecoveryMode = recoveryMode;
	}

	public int getIoMapSetId() {
		return ioMapSetId;
	}

	public void setIoMapSetId(int ioMapSetId) {
		this.ioMapSetId = ioMapSetId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public void dumpStat(StringBuilder sb) {
		for (Pair<Integer, NewVehicleData> vdt : dataList) {
			vdt.second.dumptStat(sb);
		}
	}
	public void resetStat() {
		for (Pair<Integer, NewVehicleData> vdt : dataList) {
			vdt.second.resetStat();
		}
	}
	
	public static StringBuilder dumpStat(int vehicleId, ConcurrentHashMap<Integer, VehicleDataInfo> thisCache) {
		StringBuilder sb= new StringBuilder();
		if (!Misc.isUndef(vehicleId)) {
			VehicleDataInfo vdf = thisCache.get(vehicleId);
			if (vdf != null) {
				vdf.dumpStat(sb);
			}
		}
		else {
			Collection<VehicleDataInfo> entries =thisCache.values();
			for (VehicleDataInfo vdf : entries) {
				vdf.dumpStat(sb);
			}
		}
		return sb;
	}

	public boolean isInRecoveryMode() {
		return inRecoveryMode;
	}

	public void setInRecoveryMode(boolean inRecoveryMode) {
		this.inRecoveryMode = inRecoveryMode;
	}
	
	public void setToPtCount(MiscInner.Pair cnt) {
		for (Pair<Integer, NewVehicleData> entry : dataList) {
			entry.second.setToPtCount(cnt);
		}
	}
	public void reinit(Connection conn, CacheTrack.VehicleSetup vsetup) {
		int cummDistProvided = vsetup == null ? 0 : vsetup.cummDistProvided;// 0 dont. 1 complex, 2 simple
		int cummDistCalcApproach = vsetup == null ? -1 : vsetup.cummDistCalcApproach;// 0 dont. 1 complex, 2 simple
		boolean cummDistSensorBased = cummDistCalcApproach == 2 || cummDistProvided == 3;
		
		if (vsetup != null) {
			updateDeviceIdEtc(conn, vsetup.getDeviceId(), vsetup.getIoMapSetId(), vsetup.getRedirectUrl(), cummDistProvided == 2, cummDistProvided == 3 || cummDistSensorBased);
		}

		for (Pair<Integer, NewVehicleData> pr : dataList) {
			pr.second.reinit(conn);
			pr.second.setGpsIdDelta(cummDistProvided == 2);
			pr.second.setCummDistSensorBased(cummDistProvided == 3 || cummDistSensorBased);
		}
	}
	public Dala01Mgmt getDalaUpMgmt() {
		NewVehicleData dalaVDT = null;
		for (int idx=0, is=dataList.size();idx<is;idx++) {
			if (dataList.get(idx).first == Misc.DALAUP_DIM_ID) {
				dalaVDT = dataList.get(idx).second;
				break;
			}
		}
		return dalaVDT == null ? null : dalaVDT.getDalaUpMgmt();
	}
	
}

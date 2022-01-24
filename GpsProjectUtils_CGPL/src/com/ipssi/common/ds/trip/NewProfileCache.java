package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TripParams;

public class NewProfileCache {
	
	private static HashMap<Integer, Pair<ArrayList<String>, ArrayList<Pair<Integer,Integer>>>> opprofileToMaps = new HashMap<Integer, Pair<ArrayList<String>, ArrayList<Pair<Integer, Integer>>>>();
	//above: key = opprofileId, value = second: List of opstation and mapping specified in that
	//                                               value = first = string consisting of distinct sets of profiles which are combined to get combo Mapping together
	//                                                and is kept in the variable below ..
	private static HashMap<String, OpMapping> calculatedOpMapping = new HashMap<String, OpMapping>();	
	private static HashMap<Integer, VehicleControlling> vehicleControlling = new HashMap<Integer, VehicleControlling>();
	
	private static HashMap<Integer, Pair<ArrayList<String>, TripParams>> stopParamToMaps = new HashMap<Integer, Pair<ArrayList<String>, TripParams>>();
	private static HashMap<String, StopDirControl> calculatedStopParamMapping = new HashMap<String, StopDirControl>();
	public static void reset() {
		NewProfileCache.calculatedOpMapping.clear();
		NewProfileCache.calculatedStopParamMapping.clear();
		NewProfileCache.opprofileToMaps.clear();
		NewProfileCache.stopParamToMaps.clear();
		NewProfileCache.vehicleControlling.clear();
	}
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			loadProfiles(conn, null);
			loadVehicleProfileMapping(conn, null);
			loadStopParams(conn, null);
			int vehicleId = 18196;
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			VehicleControlling vc = NewProfileCache.getOrCreateControlling(vehicleId);
			
			StopDirControl sdc = vc.getStopDirControl(conn, vehSetup);
			OpMapping mapping = vc.getOpMapping();
		}
		catch (Exception e) {
			destroyIt = true;
		}
		finally {
			
		}
	}
	public static VehicleControlling getControlling(int vehicleId) {
		return vehicleControlling.get(vehicleId);
	}
	public static VehicleControlling getOrCreateControlling(int vehicleId) {
		VehicleControlling retval = vehicleControlling.get(vehicleId);
		if (retval == null) {
			retval = new VehicleControlling(vehicleId);
			vehicleControlling.put(vehicleId, retval);
		}
		return retval;
	}
	public static void loadProfiles(Connection conn, ArrayList<Integer>loadThese) throws Exception {
		try {
			StringBuilder sb = new StringBuilder("select distinct opstation_profile_id, opstation_id, type, opstation_profiles.status from opstation_profile_details join opstation_profiles on (opstation_profiles.id = opstation_profile_details.opstation_profile_id) where ");
			if (loadThese != null && loadThese.size() != 0) {
				sb.append(" opstation_profile_id in (");
				Misc.convertInListToStr(loadThese, sb);
				sb.append(") and ");
			}
			sb.append(" opstation_profile_details.status in (1) order by opstation_profile_id, type, opstation_id desc ");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			int prevId = Misc.getUndefInt();
			int prevProfStatus = 1;
			ArrayList<Pair<Integer, Integer>> entry = null;
			while (rs.next()) {
				int id = rs.getInt(1);
				int opid = rs.getInt(2);
				int ty = rs.getInt(3);
				int profStatus = rs.getInt(4);
				if (id != prevId) {
					if (entry != null) {
						if (prevProfStatus != 1)
							entry.clear();
						addOrUpdateOpProfile(prevId, entry);
					}
					entry = null;
					prevId = id;
					prevProfStatus = profStatus;
				}
				if (entry == null) {
					entry = new ArrayList<Pair<Integer, Integer>>();
				}
				entry.add(new Pair<Integer, Integer>(opid, ty));
			}//for each
			if (prevId >= 0) {
				if (prevProfStatus != 1 && entry != null)
					entry.clear();
				addOrUpdateOpProfile(prevId, entry);
			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void loadVehicleProfileMapping(Connection conn, ArrayList<Integer> loadThese) throws Exception {
		try {
			StringBuilder sb = new StringBuilder("select distinct vehicle_id, opstation_profile_id, vehicle.status from vehicle_opstation_profiles join vehicle on (vehicle.id = vehicle_id) join opstation_profiles on (opstation_profiles.id = opstation_profile_id)  where ");
			if (loadThese != null && loadThese.size() != 0) {
				sb.append(" vehicle.id in (");
				Misc.convertInListToStr(loadThese, sb);
				sb.append(") and ");
			}
			sb.append(" opstation_profiles.status in (1) order by vehicle_id, opstation_profile_id ");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			int prevId = Misc.getUndefInt();
			int prevStatus = 1;
			ArrayList<Integer> entry = null;
			while (rs.next()) {
				int id = rs.getInt(1);
				int opid = rs.getInt(2);
				int status = rs.getInt(3);
				if (id != prevId) {
					if (entry != null) {
						if (prevStatus != 1) {
							vehicleControlling.remove(prevId);
						}
						else { 
							String key = addCalculatedMapping(entry);
							VehicleControlling curr = vehicleControlling.get(prevId);
							if (curr == null) {
								curr = new VehicleControlling(prevId);
								vehicleControlling.put(prevId, curr);
							}
							if (curr != null) {
								curr.setOpProfileMergeId(key);
								curr.setOpMapping(calculatedOpMapping.get(key));
							}
						}
					}
					entry = null;
					prevId = id;
					prevStatus = status;
				}				
				if (entry == null)
					entry = new ArrayList<Integer>();
				entry.add(opid);
			}//for each
			if (prevId >= 0) {
				if (prevStatus != 1) {
					vehicleControlling.remove(prevId);
				}
				else { 
					String key = addCalculatedMapping(entry);
					VehicleControlling curr = vehicleControlling.get(prevId);
					if (curr == null) {
						curr = new VehicleControlling(prevId);
						vehicleControlling.put(prevId, curr);
					}
					if (curr != null) {
						curr.setOpProfileMergeId(key);
						curr.setOpMapping(calculatedOpMapping.get(key));
					}
				}
			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static ArrayList<Pair<Integer, Integer>> getRawMappingsForProfileId(int id) {
		Pair<ArrayList<String>, ArrayList<Pair<Integer,Integer>>> retval = opprofileToMaps.get(id);
		return retval == null ? null : retval.second;
	}
	public static void loadStopParams(Connection conn, ArrayList<Integer>loadThese) throws Exception {
		try {
			if (NewProfileCache.getRawParamsForProfileId(Misc.getUndefInt()) == null) {
				addOrUpdateTripParam(conn, Misc.getUndefInt(), null);
			}
			
			StringBuilder sb = new StringBuilder("select distinct tripparam_profiles.id, tripparam_profiles.status from tripparam_profiles ");
			if (loadThese != null && loadThese.size() != 0) {
				sb.append(" where tripparam_profiles.id in (");
				Misc.convertInListToStr(loadThese, sb);
				sb.append(")  ");
			}
			sb.append(" order by tripparam_profiles.id ");
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				int profStatus = rs.getInt(2);
				if (profStatus == 1) {
					TripParams param = TripParams.getDetails(conn, id);
					addOrUpdateTripParam(conn, id, param);
				}
				else {
					addOrUpdateTripParam(conn, id, null);
				}
			}//for each
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static TripParams getRawParamsForProfileId(int id) {
		Pair<ArrayList<String>, TripParams> retval = stopParamToMaps.get(id);
		return retval == null ? null : retval.second;
	}
	
	private static String addCalculatedMapping(ArrayList<Integer> profiles) { //use this for each vehicle mapping found
		if (profiles == null)
			profiles = new ArrayList<Integer>();
		StringBuilder ksb = new StringBuilder();
		Misc.convertInListToStr(profiles, ksb);
		String key = ksb.toString();
		OpMapping mapping = calculatedOpMapping.get(key); 
		if (mapping == null) {
			mapping = new OpMapping();
			calculatedOpMapping.put(key, mapping);
		}
		else  {
			return key;
		}
		mapping.calculate(profiles);
		for (Integer profile:profiles) {
			Pair<ArrayList<String>, ArrayList<Pair<Integer,Integer>>> entry = opprofileToMaps.get(profile);
			if (entry == null) //something screwed up
				continue;
			ArrayList<String> keysUsingProfileDef = entry.first;
			boolean exists = false;
			for (String s:keysUsingProfileDef) {
				if (s.equals(key)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				keysUsingProfileDef.add(key);
			}
		}
		return key;
	}
	
	private static void addOrUpdateOpProfile(int profileId, ArrayList<Pair<Integer, Integer>> opList) {
		Pair<ArrayList<String>, ArrayList<Pair<Integer,Integer>>> currEntry = opprofileToMaps.get(profileId);
		if (currEntry == null) {
			currEntry = new Pair<ArrayList<String>, ArrayList<Pair<Integer,Integer>>>(new ArrayList<String>(), opList);
			opprofileToMaps.put(profileId, currEntry);
		}
		ArrayList<String> currMapsUsingThis = currEntry.first;
		for (String s : currMapsUsingThis) {
			OpMapping mapping =calculatedOpMapping.get(s);
			ArrayList<Integer> idsAsArray = new ArrayList<Integer>();
			Misc.convertValToVector(s, idsAsArray, false);
			mapping.calculate(idsAsArray);
		}
	}
	
	private static void addOrUpdateTripParam(Connection conn, int profileId, TripParams param) throws Exception {
		Pair<ArrayList<String>, TripParams> currEntry = stopParamToMaps.get(profileId);
		if (currEntry == null) {
			currEntry = new Pair<ArrayList<String>, TripParams>(new ArrayList<String>(),param);
			stopParamToMaps.put(profileId, currEntry);
		}
		ArrayList<String> currMapsUsingThis = currEntry.first;
		Cache cache = Cache.getCacheInstance(conn);
		
		for (String s : currMapsUsingThis) {
			StopDirControl mapping =NewProfileCache.calculatedStopParamMapping.get(s);
			ArrayList<Integer> idsAsArray = new ArrayList<Integer>();
			Misc.convertValToVector(s, idsAsArray, false);
			MiscInner.PortInfo portInfo = cache.getPortInfo(idsAsArray.get(1), conn);
			StopDirControl.getControl(mapping, param, portInfo);
		}
	}
	
	public synchronized static StopDirControl addCalculatedStopDirControl(Connection conn, int profileId, int portId) throws Exception { //use this for each vehicle mapping found
		String key = Integer.toString(profileId)+","+Integer.toString(portId);
		StopDirControl mapping = calculatedStopParamMapping.get(key); 
		if (mapping == null) {
			mapping = new StopDirControl();
			calculatedStopParamMapping.put(key, mapping);
		}
		else  {
			return mapping;
		}
		Pair<ArrayList<String>, TripParams> entry = NewProfileCache.stopParamToMaps.get(profileId);
		TripParams params=null;
		if(entry!=null)
		 params = entry.second;
		Cache cache = Cache.getCacheInstance(conn);
		MiscInner.PortInfo portInfo = cache.getPortInfo(portId, conn);
		StopDirControl.getControl(mapping, params, portInfo);
		ArrayList<String> keyDefsUsing=null;
		if(entry!=null)
		 keyDefsUsing = entry.first;
		boolean exists = false;
		if(keyDefsUsing!=null)
		{
		for (String s : keyDefsUsing) {
			if (s.equals(key)) {
				exists = true;
				break;
			}
		}
		if (!exists)
			keyDefsUsing.add(key);
		}
		return mapping;
	}
	//steps: 1 - load opprofiles
	
	
}

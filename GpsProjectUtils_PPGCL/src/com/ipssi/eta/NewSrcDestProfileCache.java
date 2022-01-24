package com.ipssi.eta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;

public class NewSrcDestProfileCache {
	private static ConcurrentHashMap<Integer, HashMap<Integer, Integer>> vehicleToSrcDestId = null;
	   //1st  = vehicle Id, 2nd list of HashMap of SrcDestId, SrcDestId (effectively is SrcDestId inside the map
		//strategy - loading will be for a vehicle or for all
	public static class Helper {
		private HashMap<Integer, Integer> cachedSD = null;
		private int vehicleId = Misc.getUndefInt();
		private MiscInner.PortInfo ownerPort = null;
		private Cache cache = null;
		public Helper(Connection conn, int vehicleId) {
			this.vehicleId = vehicleId;
			cachedSD = getAllSD(conn, vehicleId);
			if (cachedSD == null) {
				try {
					cache = Cache.getCacheInstance(conn);
					CacheTrack.VehicleSetup vehsetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
					int ownerOrg = vehsetup == null ? Misc.getUndefInt() : vehsetup.m_ownerOrgId;
					ownerPort = cache == null ? null : cache.getPortInfo(ownerOrg, conn);
				}
				catch (Exception e) {
				}
			}
		}
		
		public ArrayList<Integer> dbgHelperGetAllSD(Connection conn) {
			ArrayList<Integer> retval = new ArrayList<Integer>();
			if (cachedSD != null) {
				Collection<Integer> sdList = cachedSD.values();
				for (Integer iv : sdList) {
					retval.add(iv);
				}
			}
			else {
				SrcDestInfo.getSrcDestInfo(conn,1);//just to initialize stuff
				Collection<SrcDestInfo> sdList = SrcDestInfo.dbgHelperGetAllSD();
				for (SrcDestInfo sd : sdList) {
					if (isInProfile(conn, sd)) { 
						retval.add(sd.getId());
					}
				}
			}
			return retval;
		}
		
		public boolean isInProfile(Connection conn, SrcDestInfo srcDestInfo) {
			if (cachedSD != null)
				return cachedSD.containsKey(srcDestInfo.getId());
			int portOfSD = srcDestInfo.getPortNodeId();
			for (MiscInner.PortInfo port=ownerPort; port != null; port = port.m_parent)
				if (port.m_id == portOfSD)
					return true;
			return false;
		}
	}

	private static HashMap<Integer, Integer> getAllSD(Connection conn, int vehicleId) {
		if (vehicleToSrcDestId == null) {
			vehicleToSrcDestId = new ConcurrentHashMap<Integer, HashMap<Integer, Integer>>();
			load(conn, null, false);
		}
		return vehicleToSrcDestId.get(vehicleId);
	}
	
	public static synchronized void load(Connection conn, ArrayList<Integer> vehicleIds, boolean doETAStateRecalc) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct vm.vehicle_id, sd.id from vehicle_srcdest_profiles vm join srcdest_profile_details sdp on (vm.srcdest_profile_id = sdp.srcdest_profile_id and sdp.status in (1)) ")
			.append(" join srcdest_profiles sp on (sp.id = sdp.srcdest_profile_id and sp.status in (1)) join src_dest_items sd on (sd.id = sdp.src_dest_item_id and sd.status in (1)) ")
			;
			if (vehicleIds != null && vehicleIds.size() != 0) {
			    sb.append(" where vm.vehicle_id in (");
			    Misc.convertInListToStr(vehicleIds, sb);
			    sb.append(") ");
			}
			sb.append(" order by vm.vehicle_id" );
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			int prevVehicleId = Misc.getUndefInt();
			HashMap<Integer, Integer> prv = null;
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				int sdId = rs.getInt(2);
				if (vehicleId != prevVehicleId || prv == null) {
					prv = vehicleToSrcDestId.get(vehicleId);
					if (prv == null) {
						prv = new HashMap<Integer, Integer>();
						vehicleToSrcDestId.put(vehicleId, prv);
					}
					else {
						prv.clear(); //reget all
					}
					prevVehicleId = vehicleId;
				}
				prv.put(sdId, sdId);
			}
			rs.close();
			ps.close();
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
			//Uggh ... needs to be called explicitly on changes thru UI RedoHelper.execAction(vehicleIds, RedoHelper.G_RECALC_STATE_VEHICLE, Misc.getUndefInt(), false);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
}

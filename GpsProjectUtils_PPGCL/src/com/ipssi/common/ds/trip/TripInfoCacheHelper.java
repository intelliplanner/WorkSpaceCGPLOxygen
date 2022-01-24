package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.LocalNameHelperRTree;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.mapguideutils.ReadShapeFile;
import com.ipssi.processor.utils.GpsData;

public class TripInfoCacheHelper {
	private static String FETCH_OPSTATION_MAPPING = "select port_node_id, type, op_station_id from opstation_mapping join op_station on (op_station.id = opstation_mapping.op_station_id and op_station.status in (1)) #AND_CLAUSE union" +
	" select port_node_id, type, op_station_id from opstation_mapping_addnl join op_station on (op_station.id = opstation_mapping_addnl.op_station_id and op_station.status in (1))  #AND_CLAUSE ";//added in query order by port_node_id, type";
	private static String FETCH_OPSTATION= " SELECT op_station.id, null, wait_reg_id, gate_reg_id, region_id, vehicle_id, int_val1, int_val2, int_val3, int_val4, int_val5, int_val6, int_val7, int_val8, int_val9, int_val10, int_val11, int_val12, hybrid_flip_only, pick_first,pick_last,look_up_challan, op_station.name, opstations_opareas.material material_id, material.name material, opstations_opareas.priority priority, opstations_opareas.threshold threshold, opstations_opareas.oparea_type oparea_type, op_station.work_area_id, op_station.confirm_exit_id, opstations_opareas.start_date, opstations_opareas.end_date, op_station.sub_type, null,op_station.material_id opm_material_id, op_station.start_date op_start_date, op_station.end_date op_end_date, op_station.ref_opstation_id "+
	" ,alert_email_l1_customer,alert_email_l2_customer,alert_phone_l1_customer,alert_phone_l2_customer,alert_user_l1_customer,alert_user_l2_customer "+
	" ,alert_email_l1_transporter,alert_email_l2_transporter,alert_phone_l1_transporter,alert_phone_l2_transporter,alert_user_l1_transporter,alert_user_l2_transporter "+
	" ,alert_email_l1_sender,alert_email_l2_sender,alert_phone_l1_sender,alert_phone_l2_sender,alert_user_l1_sender,alert_user_l2_sender "+
	", op_station.short_code opstation_code "+
	
	" FROM op_station  LEFT JOIN opstations_opareas ON op_station.id = opstations_opareas.op_station_id  LEFT JOIN material ON opstations_opareas.material = material.id WHERE op_station.STATUS IN (1)";
	//private static String FETCH_OPSTATION_DATA_MIN_MAX = " select op_station.id, op_station.vehicle_id, min(gps_record_time), max(gps_record_time) from op_station join logged_data on (op_station.vehicle_id = logged_data.vehicle_id and logged_data.attribute_id=0) where op_station.status in (1) ";
	//private static String FETCH_OPSTATION_WITH_OVERLAPPING_OPAREA = " select op_station.id from op_station where op_station.int_val11 = 1 and op_station.status in (1) ";

	  // TripInfoCache Stuff start
	private volatile static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, OpStationLookup>> customerOpStationCache = null;
	private volatile static ConcurrentHashMap<Integer, ArrayList<Integer>> waitIdToOpStationId = null;
	private  volatile static ConcurrentHashMap<Integer, OpStationBean> opStationCache = null; // Definition of Opstation
	private volatile static ConcurrentHashMap<Integer, ArrayList<Integer>> opstationIdLinkedToVehicle = null;// key = vehicleId, value = list of opstationId's linked to this vehicle Id
	private volatile static ConcurrentHashMap<Integer, Integer> siteIdToOpstationid = new ConcurrentHashMap<Integer, Integer>(100, 0.75f);
	private volatile static ConcurrentHashMap<Integer, Integer> opstationIdToSiteId = new ConcurrentHashMap<Integer, Integer>(100, 0.75f);
	
	   // public static boolean g_inRecoveryMode = false;
	    public static int g_numLoopToSeeIfDataSavedWhileRecovering = 3;
	    private static ArrayList<Integer> g_opStationTypesForTripNonStop = new ArrayList<Integer>(); //Load,Unload .. see below
	    private static ArrayList<Integer> g_opStationTypesForTripStop = new ArrayList<Integer>(); //Load,Unload .. see below
		public static int getSiteForOpstationid(int opstationId) {
			Integer v = opstationIdToSiteId.get(opstationId);
			return v == null ? Misc.getUndefInt() : v.intValue();
		}
		public static int getOpstationIdForSite(int siteId) {
			Integer v = siteIdToOpstationid.get(siteId);
			return v == null ? Misc.getUndefInt() : v.intValue();
		}
		private static String g_siteOpQuery = "select dos_inventory_piles.id, op_station.id from dos_inventory_piles join op_station on (dos_inventory_piles.region_id=op_station.gate_reg_id) where dos_inventory_piles.status in (1,2) and op_station.status in (1,2) ";
		public static void loadOpstationToSite(Connection conn) throws Exception {
			siteIdToOpstationid.clear();
			opstationIdToSiteId.clear();
			PreparedStatement ps = conn.prepareStatement(g_siteOpQuery);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int siteid = rs.getInt(1);
				int opid = rs.getInt(2);
				opstationIdToSiteId.put(opid, siteid);
				siteIdToOpstationid.put(siteid, opid);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	    public static ArrayList<Integer> getAppropFixedType(boolean ofStop) {
	    	return ofStop ? g_opStationTypesForTripStop : g_opStationTypesForTripNonStop;
	    }
	    private static ArrayList<Integer> g_opStationTypesForTripRenamedExt = new ArrayList<Integer>();
	    public static ArrayList<Integer> getOpStationTypeExt() {
	    	return g_opStationTypesForTripRenamedExt;
	    }
	    private static ArrayList<Integer> g_opStationTypesForTripPlusIMNonStop = new ArrayList<Integer>();
	    private static ArrayList<Integer> g_opStationTypesForTripPlusIMStop = new ArrayList<Integer>();
	    public static ArrayList<Integer> getAppropFixedTypePlusIM(boolean ofStop) {
	    	return ofStop ? g_opStationTypesForTripPlusIMStop : g_opStationTypesForTripPlusIMNonStop;
	    }
	    public static ArrayList<Integer> g_opStationTypesForIntermediate = new ArrayList<Integer>();
	    static {
	    	g_opStationTypesForTripStop.add(TripInfoConstants.LOAD);
	    	g_opStationTypesForTripStop.add(TripInfoConstants.UNLOAD);
	    	g_opStationTypesForTripStop.add(TripInfoConstants.HYBRID_UL);
	    	g_opStationTypesForTripStop.add(TripInfoConstants.HYBRID_LU);
	    	g_opStationTypesForTripStop.add(TripInfoConstants.HYBRID_ALL);
	    	g_opStationTypesForTripStop.add(TripInfoConstants.HYBRID_NONE);
	    	g_opStationTypesForTripStop.add(TripInfoConstants.HYBRID_UL_ALWAY);
	    	
	    	//g_opStationTypesForTrip.add(TripInfoConstants.REST_AREA_REGION);
	    	g_opStationTypesForIntermediate.add(TripInfoConstants.PRE_LOAD_IM);
	    	g_opStationTypesForIntermediate.add(TripInfoConstants.POST_IM);
	    	g_opStationTypesForIntermediate.add(TripInfoConstants.PRE_UNLOAD_IM);
	    	g_opStationTypesForTripRenamedExt.add(TripInfoConstants.PREFERRED_LOAD_LOWPRIORITY);
	    	g_opStationTypesForTripRenamedExt.add(TripInfoConstants.PREFERRED_UNLOAD_LOWPRIORITY);
	    	g_opStationTypesForTripRenamedExt.add(TripInfoConstants.PREFERRED_LOAD_HIPRIORITY);
	    	g_opStationTypesForTripRenamedExt.add(TripInfoConstants.PREFERRED_UNLOAD_HIPRIORITY);
	    	g_opStationTypesForTripRenamedExt.add(TripInfoConstants.REST_AREA_REGION);
	    	g_opStationTypesForTripRenamedExt.add(TripInfoConstants.STOP_IGNORE);
	    	
	    	for (Integer i:g_opStationTypesForTripStop) { 
	    		g_opStationTypesForTripNonStop.add(i);
			}
	    	g_opStationTypesForTripNonStop.add(TripInfoConstants.REST_AREA_REGION);
	    	for (int art=0;art<2;art++) {
	    		ArrayList<Integer> optypel = art == 0 ? g_opStationTypesForTripStop : g_opStationTypesForIntermediate;
	    		
	    		for (Integer i:optypel) { 
	    			g_opStationTypesForTripPlusIMStop.add(i);
	    		}
	    	}
	    	for (int art=0;art<2;art++) {
	    		ArrayList<Integer> optypel = art == 0 ? g_opStationTypesForTripNonStop : g_opStationTypesForIntermediate;
	    		
	    		for (Integer i:optypel) { 
	    			g_opStationTypesForTripPlusIMNonStop.add(i);
	    		}
	    	}


	    }

	
	public static ArrayList<Integer> getOpStationList() {
		Collection<OpStationBean> list = opStationCache == null ? null : opStationCache.values();
		if (list != null) {
			ArrayList<Integer> retval = new ArrayList<Integer>();
			for (OpStationBean bean: list) {
				retval.add(bean.getOpStationId());
			}
			return retval;
		}
		return null;
	}
	public static Collection<OpStationBean> getOpStationCollection() {
		return opStationCache == null ? null : opStationCache.values();
	}
	public synchronized static ArrayList<OpStationBean> initOpsBeanRelatedCache(Connection conn, ArrayList<Integer> opStationIDs) throws Exception {
		//return delted list
		ArrayList<OpStationBean> deleted = null;
		//currently being called from TripInfoCache.initCache and expected to be taken lock outside ... later on this may change
		try {
			loadOpstationToSite(conn);
			boolean toReset = opStationIDs == null;
			if (TripInfoCacheHelper.opStationCache == null) {
				toReset = true;
				opStationIDs = null;
			}
			else {
				if (opStationIDs != null && opStationIDs.size() == 0)
					return null;
			}
			if(!LocalNameHelperRTree.isRTreeLoaded()){
				ReadShapeFile.loadRTree(conn);
				LocalNameHelperRTree.setRTreeLoaded();
			}
			
			ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> customerOpStationCachePutInThis = null;
			ConcurrentHashMap<Integer, OpStationBean> opStationCachePutInThis = null;
			ConcurrentHashMap<Integer, ArrayList<Integer>> opstationIdLinkedToVehiclePutInThis = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
			// HashMap<opStationId, HashMap<regRuleId, HashMap<notificationType, RegionAlertVo>>>

			opStationCachePutInThis = getAllOpStation(conn, opStationIDs);
			Set<Entry<Integer, OpStationBean>> opstationList = opStationCachePutInThis.entrySet();
			for (Entry<Integer, OpStationBean> entry : opstationList) {
				int opId = entry.getKey();
				OpStationBean opstation = entry.getValue();
				if (Misc.isUndef(opstation.getLinkedVehicleId()))
					continue;
				ArrayList<Integer> opslinked = opstationIdLinkedToVehiclePutInThis.get(opstation.getLinkedVehicleId());
				if (opslinked == null) {
					opslinked = new ArrayList<Integer>();
					opstationIdLinkedToVehiclePutInThis.put(opstation.getLinkedVehicleId(), opslinked);
				}
				opslinked.add(opId);
			}
			if (toReset) {
				opStationCache = null;
				waitIdToOpStationId = null;
			}
			if (opStationCache == null) {
				opStationCache = new ConcurrentHashMap<Integer, OpStationBean>();
			}			
			if (waitIdToOpStationId == null || (opStationIDs == null || opStationIDs.size() == 0)) {
				waitIdToOpStationId = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
			}
			// remove things which are deleted
			ArrayList<Integer> specialRegionsMaybeDeleted = null;
			ArrayList<Integer> specialRegionsAdded = null;
			
			for (int i = 0, is = opStationIDs == null ? 0 : opStationIDs.size(); i < is; i++) {
				Integer i1 = opStationIDs.get(i);
				if (!opStationCachePutInThis.containsKey(i1) && opStationCache.containsKey(i1)) {
					OpStationBean deletedOp = opStationCache.get(i1);
					ArrayList<Integer> opstationUsingWait = waitIdToOpStationId.get(deletedOp.getWaitAreaId());
					if (deletedOp.getSubType() > 0) {
						if (specialRegionsMaybeDeleted == null) {
							specialRegionsMaybeDeleted = new ArrayList<Integer>();
						}
						specialRegionsMaybeDeleted.add(deletedOp.getWaitAreaId());
					}
					for (int t1=0,t1s = opstationUsingWait == null ? 0 : opstationUsingWait.size(); t1<t1s; t1++) {
						int opid = opstationUsingWait.get(t1);
						if (deletedOp.getOpStationId() == opid) {
							opstationUsingWait.remove(t1);
							break;
						}
					}
					if (opstationUsingWait != null && opstationUsingWait.size() == 0)
						waitIdToOpStationId.remove(deletedOp.getWaitAreaId());
					if (deleted == null)
						deleted = new ArrayList<OpStationBean>();
					deleted.add(deletedOp);
					opStationCache.remove(i1);
				}
			}
			//this needs to be done before putting
			//Populate the waitIdToOpStationId
			opstationList = opStationCachePutInThis.entrySet();
			for (Entry<Integer, OpStationBean> entry : opstationList) {
				int opId = entry.getKey();
				OpStationBean opstation = entry.getValue();
				OpStationBean oldopStation = opStationCache.get(opId);
				if (oldopStation != null && opstation.getWaitAreaId() != oldopStation.getWaitAreaId()) {
					if (oldopStation.getSubType() > 0) {
						if (specialRegionsMaybeDeleted == null) {
							specialRegionsMaybeDeleted = new ArrayList<Integer>();
						}
						specialRegionsMaybeDeleted.add(oldopStation.getWaitAreaId());
					}
					ArrayList<Integer> list = waitIdToOpStationId.get(oldopStation.getWaitAreaId());
					
					if (list != null) {
						for (int t1=0,t1s=list.size();t1 < t1s; t1++) {
							if (list.get(t1) == opId) {
								list.remove(t1);
								break;
							}
						}
					}
				}//cleaned up old info
				if (opstation.getSubType() > 0) {
					if (specialRegionsAdded == null) {
						specialRegionsAdded = new ArrayList<Integer>();
					}
					specialRegionsAdded.add(opstation.getWaitAreaId());
				}
				ArrayList<Integer> list = waitIdToOpStationId.get(opstation.getWaitAreaId());
				if (list == null) {
					list = new ArrayList<Integer>();
					waitIdToOpStationId.put(opstation.getWaitAreaId(), list);
				}
				boolean found = false;
				for (int t1=0,t1s=list == null ? 0 : list.size();t1 < t1s; t1++) {
					if (list.get(t1) == opId) {
						found = true;
						break;
					}
				}
				if (!found)
					list.add(opId);
			}
			opStationCache.putAll(opStationCachePutInThis);// TODO_MAKE_OPSTATION_SINGLETON
			for (int i=0,is=specialRegionsMaybeDeleted == null ? 0 : specialRegionsMaybeDeleted.size(); i<is; i++) {
				int regid = specialRegionsMaybeDeleted.get(i);
				ArrayList<Integer> oplist = waitIdToOpStationId.get(regid);
				for (int j=0,js=oplist == null ? 0 : oplist.size(); j<js; j++) {
					OpStationBean bean = opStationCache.get(j);
					if (bean != null && bean.getSubType() > 0) {
						specialRegionsMaybeDeleted.remove(i);
						i--;
						is--;
						break;
					}
				}
			}
			RTreesAndInformation.updateSpecialRegionsRTree(specialRegionsMaybeDeleted, specialRegionsAdded, conn);
			if (toReset) {
				opstationIdLinkedToVehicle = null;
			}
			if (opstationIdLinkedToVehicle == null) {
				opstationIdLinkedToVehicle = opstationIdLinkedToVehiclePutInThis;
			} else {
				ConcurrentHashMap<Integer, ArrayList<Integer>> temp = opstationIdLinkedToVehicle;
				if (deleted != null) {
					for (OpStationBean ops : deleted) {
						temp.remove(ops.getLinkedVehicleId());
					}
				}
				temp.putAll(opstationIdLinkedToVehiclePutInThis);
				opstationIdLinkedToVehicle = temp;
			}
			customerOpStationCachePutInThis = getAllCustomerOpStation(conn, opStationIDs);

			helperSetCustomerOpStationCache(conn, customerOpStationCachePutInThis);
			/*
			 * FAST_OP_LOOKUP if (customerOpStationCache == null) { customerOpStationCache = customerOpStationCachePutInThis; } else { // HashMap<Integer, Map<Integer,
			 * List<Integer>>> temp = (HashMap<Integer, Map<Integer, List<Integer>>>) customerOpStationCache.clone(); // temp.putAll(customerOpStationCachePutInThis); //
			 * customerOpStationCache = temp; // code - dev to add opstationId properly
			 * 
			 * Set<Entry<Integer, Map<Integer, List<Integer>>>> customerOpStationCacheTemp = customerOpStationCachePutInThis.entrySet(); for (Entry<Integer, Map<Integer,
			 * List<Integer>>> entry: customerOpStationCacheTemp) { int custId = entry.getKey(); Map<Integer, List<Integer>> typeOpstationIdMapTemp = entry.getValue();
			 * if(customerOpStationCache.containsKey(custId)){ Map<Integer, List<Integer>> typeOpstationIdMap = customerOpStationCache.get(custId); Set<Entry<Integer,
			 * List<Integer>>> typeOpstationTemp = typeOpstationIdMapTemp.entrySet(); for (Entry<Integer, List<Integer>> entry1: typeOpstationTemp) { int typeKey = entry1.getKey();
			 * List<Integer> opStationIdListTemp = entry1.getValue(); if(typeOpstationIdMap.containsKey(typeKey)){ List<Integer> opStationIdList = typeOpstationIdMap.get(typeKey);
			 * for (Iterator<Integer> iterator = opStationIdListTemp.iterator(); iterator.hasNext();) { Integer opStationId = iterator.next();
			 * if(!opStationIdList.contains(opStationId)) opStationIdList.add(opStationId); } } } } } }
			 */
			if (opStationIDs == null) {
				if (toReset)
					NewProfileCache.reset();
				NewProfileCache.loadProfiles(conn, null);
				NewProfileCache.loadVehicleProfileMapping(conn, null);
				NewProfileCache.loadStopParams(conn, null);
			}
			return deleted;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> getAllCustomerOpStation(Connection conn, ArrayList<Integer> opStationIDs) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> opStationMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>>(300, 0.75f);
		String query = FETCH_OPSTATION_MAPPING;
		if (opStationIDs != null && opStationIDs.size() != 0) {
			StringBuilder t = new StringBuilder();
			t.append(" where op_station_id in (");
			Misc.convertInListToStr(opStationIDs, t);
			t.append(")");
			query = query.replaceAll("#AND_CLAUSE", t.toString());
		}
		else {
			query = query.replaceAll("#AND_CLAUSE", "");
		}
			
		try {
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			int prevId = Misc.getUndefInt();
			int id = Misc.getUndefInt();
			int prevType = Misc.getUndefInt();
			int type = Misc.getUndefInt();
			ConcurrentHashMap<Integer, List<Integer>> innerMap = null;
			List<Integer> opStationList = null;
			while (rs.next()) {
				id = rs.getInt("port_node_id");
				type = rs.getInt("type");
				if (id != prevId) {
					innerMap = null;
					opStationList = null;
				}
				if (type != prevType)
					opStationList = null;
				if (innerMap == null) {
					Integer idAsInt = new Integer(id);
					innerMap = opStationMap.get(idAsInt);
					if (innerMap == null) {
						innerMap = new ConcurrentHashMap<Integer, List<Integer>>();
						opStationMap.put(idAsInt, innerMap);
					}
				}
				if (opStationList == null) {
					opStationList = innerMap.get(type);
					if (opStationList == null) {
						opStationList = new ArrayList<Integer>();
						innerMap.put(type, opStationList);
					}
				}
				int opStationId = rs.getInt("op_station_id");
				OpStationBean bean = getOpStation(opStationId);
				if (bean != null)
					bean.setOpStationType(id, type);
				opStationList.add(opStationId);
				prevType = type;
				prevId = id;
			}
			rs.close();
			stmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return opStationMap;
	}
	private static void helperSetCustomerOpStationCache(Connection conn, ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, List<Integer>>> customerOpStationCachePutInThis) throws Exception {
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, OpStationLookup>> useMe = null;
		if (customerOpStationCache == null) {
			useMe = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, OpStationLookup>>();
			customerOpStationCache = useMe;
		} else {
			useMe = customerOpStationCache;
		}
		Set<Entry<Integer, ConcurrentHashMap<Integer, List<Integer>>>> customerOpStationCacheTemp = customerOpStationCachePutInThis.entrySet();
		for (Entry<Integer, ConcurrentHashMap<Integer, List<Integer>>> entry : customerOpStationCacheTemp) {
			int custId = entry.getKey();
			Map<Integer, List<Integer>> typeOpstationIdMapTemp = entry.getValue();
			if (!useMe.containsKey(custId)) {
				useMe.put(custId, new ConcurrentHashMap<Integer, OpStationLookup>());
			}
			Map<Integer, OpStationLookup> typeOpstationIdMap = useMe.get(custId);
			Set<Entry<Integer, List<Integer>>> typeOpstationTemp = typeOpstationIdMapTemp.entrySet();
			for (Entry<Integer, List<Integer>> entry1 : typeOpstationTemp) {
				int typeKey = entry1.getKey();
				List<Integer> opStationIdListTemp = entry1.getValue();
				if (!typeOpstationIdMap.containsKey(typeKey)) {
					typeOpstationIdMap.put(typeKey, new OpStationLookup());
				}
				OpStationLookup lookup = typeOpstationIdMap.get(typeKey);
				boolean ignoreSize = typeKey == TripInfoConstants.STOP_BASED_OPSTATION || typeKey == TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE || typeKey == TripInfoConstants.STOP_IGNORE;
				for (Iterator<Integer> iterator = opStationIdListTemp.iterator(); iterator.hasNext();) {
					Integer opStationId = iterator.next();
					if (!lookup.contains(opStationId)) {
						lookup.addStation(opStationId, conn, false,ignoreSize);
					}
				}
			}
		}
	}

	public static ConcurrentHashMap<Integer, OpStationBean> getAllOpStation(Connection conn, ArrayList<Integer> opStationIDs) throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		ConcurrentHashMap<Integer, OpStationBean> opStationMap = new ConcurrentHashMap<Integer, OpStationBean>();
		OpStationBean opStationBean = null;
		StringBuilder query = new StringBuilder(FETCH_OPSTATION);
		//StringBuilder minMaxQ = new StringBuilder(FETCH_OPSTATION_DATA_MIN_MAX);
		//StringBuilder overlappingOpareaes = new StringBuilder(FETCH_OPSTATION_WITH_OVERLAPPING_OPAREA);
		if (opStationIDs != null && opStationIDs.size() != 0) {
			query.append(" and op_station.id in (");
			Misc.convertInListToStr(opStationIDs, query);
			query.append(") ");
			//minMaxQ.append(" and op_station.id in (");
			//Misc.convertInListToStr(opStationIDs, minMaxQ);
			//minMaxQ.append(") ");
			//overlappingOpareaes.append(" and op_station.id in (");
			//Misc.convertInListToStr(opStationIDs, overlappingOpareaes);
			//overlappingOpareaes.append(") ");
		}
		//minMaxQ.append(" group by op_station.id, op_station.vehicle_id ");
		try {
			stmt = conn.prepareStatement(query.toString());
			rs = stmt.executeQuery();
			int prevId = Misc.getUndefInt();
			int id = Misc.getUndefInt();
			while (rs.next()) {
				id = rs.getInt("id");
				if (id == prevId){
					if(!Misc.isUndef(Misc.getRsetInt(rs, "region_id")))
						opStationBean.addOpArea(rs.getInt("region_id"), rs.getInt("material_id"), rs.getInt("priority"), (int) (rs.getDouble("threshold") * 1000), rs
								.getInt("oparea_type"), Misc.getDateInLong(rs, "start_date"), Misc.getDateInLong(rs, "end_date"));
				}else {
					opStationBean = new OpStationBean();
					opStationMap.put(id, opStationBean);
					opStationBean.setOpStationId(id);
					//opStationBean.setPortNodeId(rs.getInt("port_node_id"));
					//opStationBean.setOpStationType(rs.getInt("type"));
					opStationBean.setWaitAreaId(rs.getInt("wait_reg_id"), conn);
					opStationBean.setGateAreaId(rs.getInt("gate_reg_id"));
					opStationBean.setOpStationName(Misc.getParamAsString(rs.getString("name")));
					opStationBean.setLinkedVehicleId(Misc.getRsetInt(rs, "vehicle_id"));
					opStationBean.setShortCode(Misc.getRsetString(rs, "opstation_code",null));
					opStationBean.setSubType(Misc.getRsetInt(rs, "sub_type"));
					opStationBean.setIntVal(0, Misc.getRsetInt(rs, "int_val1"));
					opStationBean.setIntVal(1, Misc.getRsetInt(rs, "int_val2"));
					opStationBean.setIntVal(2, Misc.getRsetInt(rs, "int_val3"));
					opStationBean.setIntVal(3, Misc.getRsetInt(rs, "int_val4"));
					opStationBean.setIntVal(4, Misc.getRsetInt(rs, "int_val5"));
					opStationBean.setIntVal(5, Misc.getRsetInt(rs, "int_val6"));
					opStationBean.setIntVal(6, Misc.getRsetInt(rs, "int_val7"));
					opStationBean.setIntVal(7, Misc.getRsetInt(rs, "int_val8"));
					opStationBean.setIntVal(8, Misc.getRsetInt(rs, "int_val9"));
					opStationBean.setIntVal(9, Misc.getRsetInt(rs, "int_val10"));
					opStationBean.setIntVal(10, Misc.getRsetInt(rs, "int_val11"));
					opStationBean.setIntVal(11, Misc.getRsetInt(rs, "int_val12"));
					opStationBean.setHasOverlappingOpArea(Misc.getRsetInt(rs, "int_val11")==1);
					opStationBean.setHybridFlipOnly(Misc.getRsetInt(rs, "hybrid_flip_only")==1);
					opStationBean.setBestAreaIsFirst(Misc.getRsetInt(rs, "pick_first") == 1);
					opStationBean.setBestAreaIsLast(Misc.getRsetInt(rs, "pick_last") == 1);
					opStationBean.setLookForChallan(Misc.getRsetInt(rs, "look_up_challan") == 1);
					opStationBean.setAreaOfWork(Misc.getRsetInt(rs, "work_area_id"));
					opStationBean.setConfirmIfExitArea(Misc.getRsetInt(rs, "confirm_exit_id"));
					opStationBean.addOplevelMaterial(Misc.getRsetInt(rs, "opm_material_id"));
					opStationBean.setStartDate(Misc.sqlToLong(rs.getTimestamp("op_start_date")));
					opStationBean.setEndDate(Misc.sqlToLong(rs.getTimestamp("op_end_date")));
					if(!Misc.isUndef(Misc.getRsetInt(rs, "region_id"))){
						opStationBean.addOpArea(rs.getInt("region_id"), rs.getInt("material_id"), rs.getInt("priority"), (int) (rs.getDouble("threshold") * 1000), rs
								.getInt("oparea_type"), Misc.getDateInLong(rs, "start_date"), Misc.getDateInLong(rs, "end_date"));
						}
//					startDate = Misc.getDate(rs, "start_date");
//					endDate = Misc.getDate(rs, "end_date");
					opStationBean.setRefOpStationId(Misc.getRsetInt(rs, "ref_opstation_id"));
					opStationBean.setAlertEmailL1Customer(rs.getString("alert_email_l1_customer"));
					opStationBean.setAlertEmailL2Customer(rs.getString("alert_email_l2_customer"));
					opStationBean.setAlertPhoneL1Customer(rs.getString("alert_phone_l1_customer"));
					opStationBean.setAlertPhoneL2Customer(rs.getString("alert_phone_l2_customer"));
					opStationBean.setAlertUserL1Customer(rs.getString("alert_user_l1_customer"));
					opStationBean.setAlertUserL2Customer(rs.getString("alert_user_l2_customer"));
					opStationBean.setAlertEmailL1Transporter(rs.getString("alert_email_l1_transporter"));
					opStationBean.setAlertEmailL2Transporter(rs.getString("alert_email_l2_transporter"));
					opStationBean.setAlertPhoneL1Transporter(rs.getString("alert_phone_l1_transporter"));
					opStationBean.setAlertPhoneL2Transporter(rs.getString("alert_phone_l2_transporter"));
					opStationBean.setAlertUserL1Transporter(rs.getString("alert_user_l1_transporter"));
					opStationBean.setAlertUserL2Transporter(rs.getString("alert_user_l2_transporter"));
					opStationBean.setAlertEmailL1Sender(rs.getString("alert_email_l1_sender"));
					opStationBean.setAlertEmailL2Sender(rs.getString("alert_email_l2_sender"));
					opStationBean.setAlertPhoneL1Sender(rs.getString("alert_phone_l1_sender"));
					opStationBean.setAlertPhoneL2Sender(rs.getString("alert_phone_l2_sender"));
					opStationBean.setAlertUserL1Sender(rs.getString("alert_user_l1_sender"));
					opStationBean.setAlertUserL2Sender(rs.getString("alert_user_l2_sender"));
					
					prevId = id;
				}
			}
			rs.close();
			stmt.close();
			
			//stmt = conn.prepareStatement(minMaxQ.toString());
			//rs = stmt.executeQuery();
			//while (rs.next()) {
			//	opStationBean = opStationMap.get(rs.getInt(1));
			//	if (opStationBean != null) {
			//		opStationBean.setDataRangeLoIncl(Misc.sqlToLong(rs.getTimestamp(3)));
			//		opStationBean.setDataRangeHiIncl(Misc.sqlToLong(rs.getTimestamp(4)));
			//	}
			//}
			//rs.close();
			//stmt.close();
			//stmt = conn.prepareStatement(overlappingOpareaes.toString());
			//rs = stmt.executeQuery();
			//while (rs.next()) {
			//	opStationBean = opStationMap.get(rs.getInt(1));
			//	if (opStationBean != null) {
			//		List<OpArea> opStList = opStationBean.getRegionIdsListDontAdd();
			//		for (int i = 0, is = opStList.size(); i < is; i++) {
			//			int regId1 = opStList.get(i).id;
			//			for (int j = i+1, is1 = opStList.size(); j < is1; j++) {
			//				int regId2 = opStList.get(j).id;
			//				if(RegionTest.overlapRegionsCheck(conn, regId1, regId2)){
			//					opStList.get(i).addOverLappingArea(opStList.get(j));
			//					opStList.get(j).addOverLappingArea(opStList.get(i));
			//				}
			//			}
			//		}
			//	}
			//}
			//rs.close();
			//stmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return opStationMap;
	}

	public static Pair<OpStationBean, Double> getNearestSpecialOpstation(Point pt, ArrayList<Integer> subTypes, int subType, int vehiclePortNodeId, double distThresh, Connection conn, VehicleControlling vehicleControlling) throws Exception {
		ArrayList<Integer> nearRegions = RTreeSearch.getNearestRegions(pt, distThresh);
		double minDist = 0;
		OpStationBean retval = null;
		Cache cache = Cache.getCacheInstance(conn);
		if (subTypes != null && subTypes.size() == 1 && Misc.isUndef(subTypes.get(0))) {
			subType = subTypes.get(0);
			subTypes = null;
		}
		for (int i=0,is=nearRegions == null ? null : nearRegions.size();i<is;i++) {
			ArrayList<Integer> oplist = getOpListForWait(nearRegions.get(i));
			for (int j=0,js = oplist == null ? 0 : oplist.size(); j<js;j++) {
				int opstationId = oplist.get(j);
				OpStationBean opStation = getOpStation(opstationId);
				if (opStation == null)
					continue;
				for (int k=0,ks=subTypes == null ? 1 : subTypes.size(); k<ks; k++) {
				//check if matching type and then check if belongs from opstation point of view ..
					int st = subTypes == null ? subType : subTypes.get(k);
					if ((st == opStation.getSubType() || st == 0) && opStation.getSubType() != 0) {
						int opstationType  = opStation.getOpStationType(conn, cache, vehiclePortNodeId, vehicleControlling);
						if (!Misc.isUndef(opstationType)) {//is available
							double dist = pt.fastGeoDistance((opStation.getUpperX()+opStation.getLowerX())/2.0, (opStation.getUpperY()+opStation.getLowerY())/2.0);
							if (retval == null || minDist > dist) {
								retval = opStation;
								minDist = dist;
							}
						}
					}//if subtype maches
				}//for each subType
			}//for each opstation that has the region
		}//nearest regions
		return new Pair<OpStationBean, Double> (retval, minDist);
	}
	public static ArrayList<Integer> getOpListForWait(int waitAreaId) {
		return waitIdToOpStationId.get(waitAreaId);
	}
	public static List<OpStationBean> getOpStationsForVehicle(Connection conn, int orgId, ArrayList<Integer> typeOfOpStation, GpsData data, ThreadContextCache threadContextCache, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws Exception {
		return getOpStationsForVehicle(conn, orgId, typeOfOpStation, data, null, threadContextCache, vehicleControlling, vehSetup);
	}
	
	public static List<OpStationBean> getOpStationsForVehicle(Connection conn, int orgId, ArrayList<Integer> typeOfOpStation, GpsData data, HashMap<Integer, Integer> opstIdList, ThreadContextCache threadContextCache, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws Exception {
		List<OpStationBean> trackRegionList = new ArrayList<OpStationBean>();
		for (int i = 0, is = typeOfOpStation == null ? 0 : typeOfOpStation.size(); i < is; i++) {
			List<OpStationBean> temp = getOpStationsForVehicle(conn, orgId, typeOfOpStation.get(i), data, threadContextCache, 2, vehicleControlling, vehSetup);
			if (temp != null && temp.size() != 0)
				if (opstIdList == null)
					trackRegionList.addAll(temp);
				else {
					for (OpStationBean op: temp) {
						if (op == null)
							continue;
						if (opstIdList.containsKey(op.getOpStationId()))
							trackRegionList.add(op);
							
					}
				}
		}
		return trackRegionList;
	}

	public static List<OpStationBean> getOpStationsForVehicle(Connection conn, CacheTrack.VehicleSetup vehSetup, int typeOfOpStation, GpsData data, ThreadContextCache threadContext, int fixedOrMoving, VehicleControlling vehicleControlling) throws Exception {
		return vehSetup == null ? null : getOpStationsForVehicle(conn, vehSetup.m_ownerOrgId, typeOfOpStation, data, threadContext, fixedOrMoving, vehicleControlling, vehSetup);
	}

	
	public static List<OpStationBean>  getOpStationsForVehicle(Connection conn, int orgId, int typeOfOpStation, GpsData data, ThreadContextCache threadContextCache, int fixedOrMoving, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws Exception {
		//fixedOrMoving = 0 => get fixed only, 1 => get moving only, 2 => get both
		ArrayList<OpStationBean> retval = new ArrayList<OpStationBean>();
		OpMapping opMapping = vehicleControlling == null ? null : vehicleControlling.getOpMapping();
		Cache cache = Cache.getCacheInstance(conn);
		
		if (opMapping != null && !TripInfoConstants.isStopType(typeOfOpStation)) { //get it from this list rather than looking up from port nodes
			if (fixedOrMoving ==0 || fixedOrMoving == 2) {
				if (data != null) {
					ArrayList<RegionTestHelper> containingRegion = threadContextCache.getRegionsContaining(data.getPoint());
					ArrayList<Integer> fixedList = opMapping.getOpListForType(typeOfOpStation, 0);					
					addOpStationFromRegionsNew(retval, containingRegion, fixedList, data.getGps_Record_Time());
				}
				else {
					ArrayList<Integer> temp = opMapping.getOpListForType(typeOfOpStation, 0);
					for (int i1=0,i1s = temp == null ? 0 : temp.size(); i1<i1s;i1++) {
						OpStationBean bean = getOpStation(temp.get(i1));
						if (bean != null)
							retval.add(bean);
					}
				}
			}
			if (fixedOrMoving == 1 || fixedOrMoving == 2) {
				ArrayList<Integer> movingOpList = opMapping.getOpListForType(typeOfOpStation, 1);
				if (movingOpList != null && movingOpList.size() != 0) {
					ArrayList<ThreadContextCache.SimpleMoving> movingList = threadContextCache.getMovingOpStationContaining(conn, orgId, data, vehicleControlling, vehSetup);//orgId effectively not used
					for (ThreadContextCache.SimpleMoving rt : movingList) {
						retval.add(rt.getOpstationBean());
					}
				}
			}//if there are moving stuff to be done
			return retval;
		}

		 // essentially we return the sum of the lists from all anc. Try to be smart - if only one of the anc has
		// opstation assigned then we just return that list
		// else we will create a freshly allocated arrayList and put all these opstations into that

		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, OpStationLookup>> lookup = customerOpStationCache;
		
		for (MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn); portInfo != null; portInfo = portInfo.m_parent) {
			//portInfo.getIntParams(OrgConst., arg1)
			boolean toGoup =  portInfo.getIntParamImm(OrgConst.ID_INT_TO_STOP_GOINGUP_FOR_OPSTATION) < 1;//TODO make it general
			Map<Integer, OpStationLookup> opentry = portInfo == null ? null : lookup.get(portInfo.m_id);
			if (opentry == null) {
				if (!toGoup)
					break;
				else
					continue;
			}
			OpStationLookup atPortList = opentry.get(typeOfOpStation);
			boolean ignoreSize = typeOfOpStation == TripInfoConstants.STOP_BASED_OPSTATION || typeOfOpStation == TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE || typeOfOpStation == TripInfoConstants.STOP_IGNORE;
			if (atPortList != null) {
				atPortList.getOpList(conn, retval, typeOfOpStation, data, ignoreSize, threadContextCache, fixedOrMoving, portInfo.m_id, vehicleControlling, vehSetup);
			}
			if (!toGoup)
				break;
		}
		return retval;
	}

	private static void addOpStationFromRegionsNew(ArrayList<OpStationBean> retval, ArrayList<RegionTestHelper> containingRegion, ArrayList<Integer> masterList, long dt) {
		if (containingRegion == null)
			return;
		for (RegionTestHelper rt : containingRegion) {
			ArrayList<Integer> opList = getOpListForWait(rt.region.id);
			if (opList != null) {
				for (Integer opidInt : opList) {
					OpStationBean foundBean = null;
					int opid = opidInt.intValue();
					for (int i=0,is = masterList == null ? 0 : masterList.size() ;i <is;i++) {
						if (masterList.get(i) == opid) {
							foundBean = getOpStation(masterList.get(i));
							break;
						}
					}
					if (foundBean != null) {
						if (dt < 0 || foundBean.isValidByDate(dt))
							retval.add(foundBean);
					}
				}//for each opId that uses wait id
			}//if there is a opId list using wait id
		}//for each region contraining pt
	}

	public static OpStationBean getLinkedOpstationBean(Connection conn, int orgId,int shovelId, VehicleControlling vehicleControlling) throws Exception {
		// opstation assigned then we just return that list
		// else we will create a freshly allocated arrayList and put all these opstations into that
		ArrayList<Integer> opIdOfLinked = TripInfoCacheHelper.getLinkedOpStations(shovelId);
		Cache cache = Cache.getCacheInstance(conn);
		for (int i=0,is=opIdOfLinked == null ? 0 : opIdOfLinked.size(); i<is; i++) {
			int opid = opIdOfLinked.get(i);
			OpStationBean opb = TripInfoCacheHelper.getOpStation(opid);
			if (opb != null && opb.getOpStationType(conn, cache, orgId,  vehicleControlling) > 0)
				return opb;
		}
		return null;
	}
	public static List<OpStationBean> getOpStationsForVehicleIgnoreBelonging(Connection conn, int orgId, ArrayList<Integer> typeOfOpStation, int fixedOrMoving, VehicleControlling vehicleControlling) throws Exception {
		ArrayList<OpStationBean> retval = new ArrayList<OpStationBean>();
		for (int  i=0,is=typeOfOpStation.size(); i<is;i++) {
			List<OpStationBean> temp = getOpStationsForVehicleIgnoreBelonging(conn,orgId, typeOfOpStation.get(i), fixedOrMoving, vehicleControlling);
			for (int j=0,js=temp == null ? 0 :temp.size(); j<js; j++) {
				boolean found = false;
				int topid = temp.get(j).getOpStationId();
				for (int k=0,ks=retval.size(); k<ks; k++) {
					if (retval.get(k).getOpStationId() == topid) {
						found = true;
						break;
					}
				}
				if (!found)
					retval.add(temp.get(j));
			}
		}
		return retval;
	}
	
	public static List<OpStationBean> getOpStationsForVehicleIgnoreBelonging(Connection conn, int orgId, int typeOfOpStation, int fixedOrMoving, VehicleControlling vehicleControlling) throws Exception {
		// opstation assigned then we just return that list
		// else we will create a freshly allocated arrayList and put all these opstations into that
		ArrayList<OpStationBean> retval = new ArrayList<OpStationBean>();
		OpMapping opMapping = vehicleControlling == null ? null : vehicleControlling.getOpMapping();
		if (opMapping != null && !TripInfoConstants.isStopType(typeOfOpStation)) { //get it from this list rather than looking up from port nodes
			ArrayList<Integer> temp =opMapping.getOpListForType(typeOfOpStation, fixedOrMoving);
			for (int i1=0,i1s = temp == null ? 0 : temp.size(); i1<i1s;i1++) {
				OpStationBean bean = getOpStation(temp.get(i1));
				if (bean != null)
					retval.add(bean);
			}
			return retval;
		}

		Cache cache = Cache.getCacheInstance(conn);
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, OpStationLookup>> lookup = customerOpStationCache;
		
		for (MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn); portInfo != null; portInfo = portInfo.m_parent) {
			//portInfo.getIntParams(OrgConst., arg1)
			boolean toGoup =  portInfo.getIntParamImm(OrgConst.ID_INT_TO_STOP_GOINGUP_FOR_OPSTATION) <= 0;//TODO make it general
			Map<Integer, OpStationLookup> opentry = portInfo == null ? null : lookup.get(portInfo.m_id);
			if (opentry == null) {
				if (!toGoup)
					break;
				else
					continue;
			}
			OpStationLookup atPortList = opentry.get(typeOfOpStation);
			if (atPortList != null) {
				atPortList.getOpListIgnoreBelonging(conn, retval, typeOfOpStation, fixedOrMoving);
			}
			if (!toGoup)
				break;
		}
		return retval;
	}

	
    public static OpStationBean getOpStation(int opStationId) { 
    	if (TripInfoCacheHelper.opStationCache == null) {
    		Connection conn = null;
    		try {
    			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    			TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());
    		}
    		catch (Exception e) {
    			
    		}
    		finally {
    			if (conn != null) {
    				try {
    					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
    					conn = null;
    				}
    				catch (Exception e) {
    					
    				}
    			}
    		}
    	}
    	return TripInfoCacheHelper.opStationCache.get(opStationId);
    }
    
    public static ArrayList<Integer> getLinkedOpStations(int vehicleId) {
		Map<Integer, ArrayList<Integer>> lookup = opstationIdLinkedToVehicle;
		return lookup == null ? null : lookup.get(vehicleId);
	}

	public static int getOpstationType(int opstationId, Connection conn, Cache cache, int vehiclePortNodeId, VehicleControlling vehicleControlling) {
		OpStationBean bean = opStationCache.get(opstationId);
		return bean != null ? bean.getOpStationType(conn, cache, vehiclePortNodeId, vehicleControlling) : Misc.getUndefInt();
	}
	public static String getOpstationTypeString(int opstationId, Connection conn, Cache cache, int vehiclePortNodeId, VehicleControlling vehicleControlling){
		switch(getOpstationType(opstationId, conn, cache, vehiclePortNodeId, vehicleControlling)){
		case TripInfoConstants.LOAD:
			return "[LOAD]";
		case TripInfoConstants.UNLOAD:
			return "[UNLOAD]";
		case TripInfoConstants.HYBRID_UL:
			return "[HYBRID_UL]";
		case TripInfoConstants.HYBRID_LU:
			return "[HYBRID_LU]";
		case TripInfoConstants.HYBRID_ALL:
			return "[HYBRID_ALL]";
		case TripInfoConstants.HYBRID_NONE:
			return "[HYBRID_NONE]";
		case TripInfoConstants.HYBRID_UL_ALWAY:
			return "[HYBRID_UL_ALWAYS]";
			default :
				return "[UNKNWOWN]";
		}
	}

	public static int getStopOpStationTemplate(Connection conn, GpsData data, int portNodeId, StopDirControl stopDirControl,int vehicleId, ThreadContextCache threadContextCache, int prevOpStationId, VehicleControlling vehicleControlling) throws Exception {
		try {
			portNodeId = Misc.G_TOP_LEVEL_PORT;
			ArrayList<Integer> typeOfStation = new ArrayList<Integer>();
			typeOfStation.add(TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE);
			 List<OpStationBean> list = TripInfoCacheHelper.getOpStationsForVehicleIgnoreBelonging(conn, portNodeId, TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE, 0, vehicleControlling);
			 boolean toCreate = false;
			 int listsz = list.size();
			 int id1 = listsz >= 1 ? list.get(0).getOpStationId() : Misc.getUndefInt();
			 int id2 = listsz >= 2 ? list.get(1).getOpStationId() : Misc.getUndefInt();
			 int idRet = id1;
			 if (prevOpStationId == id1)
				 idRet = id2;
			 toCreate = Misc.isUndef(idRet);
			 if (toCreate) {
				 Point pt = data == null ? null : data.getPoint();
				 String name = data == null ? "Stop Template (Artificial)" : "Stop Template: "+" DUMMY";//TODO_DEBUG data.calcName(conn,vehicleId);
				 idRet = createOpStation(conn, pt, name, TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE, null ,portNodeId,  stopDirControl);
			 }
			 return idRet;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static int createOpStation(Connection conn, Point pt, String name, int opstationType, OpStationBean refOpStationBean, int portNodeId, StopDirControl stopDirControl) throws Exception {
		try {
			int retval = Misc.getUndefInt();			
			Pair<Integer, com.ipssi.geometry.Region> reg = RegionTest.saveAndGetBoxRegion(conn, pt, name, portNodeId, stopDirControl.getMergeStopOpstationIfInKMRange()*1000, true);
			OpStationBean bean = refOpStationBean == null ? new OpStationBean() : refOpStationBean.topLevelCopy();
			if (bean == null) {
				bean = new OpStationBean();	
			}
			bean.setGateAreaId(reg.first);
			bean.setWaitAreaId(reg.first, conn);
			bean.setOpStationName(name);
			bean.setOpStationType(portNodeId, opstationType);
			bean.setSubType(0);
			//now save ...
			PreparedStatement ps = conn.prepareStatement("insert into op_station (wait_reg_id, gate_reg_id, name, status, description, vehicle_id, int_val1, int_val2, int_val3, int_val4, int_val5, int_val6, int_val7, int_val8, int_val9, int_val10, int_val11, int_val12, pick_first, pick_last, look_up_challan, hybrid_flip_only, work_area_id, confirm_exit_id, longitude, latitude, width, height, sub_type) "+
			" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
			int colIndex = 1;
			ps.setInt(colIndex++, bean.getWaitAreaId());
			ps.setInt(colIndex++, bean.getGateAreaId());
			ps.setString(colIndex++, bean.getOpStationName());
			ps.setInt(colIndex++,1);
			ps.setString(colIndex++, null);
			Misc.setParamInt(ps, bean.getLinkedVehicleId(), colIndex++);
			for (int i=0,is=bean.getFlexParamSize();i<is;i++) {
				Misc.setParamInt(ps, bean.getIntVal(i), colIndex++);
			}
			Misc.setParamInt(ps, bean.m_bestAreaIsFirst ? 1 : 0, colIndex++);
			Misc.setParamInt(ps, bean.m_bestAreaIsLast ? 1 : 0, colIndex++);
			Misc.setParamInt(ps, bean.m_lookForChallan ? 1 : 0, colIndex++);
			Misc.setParamInt(ps, bean.m_hybridFlipOnly ? 1 : 0, colIndex++);
			Misc.setParamInt(ps, bean.getAreaOfWork(), colIndex++);
			Misc.setParamInt(ps,bean.getConfirmIfExitArea(), colIndex++);
			Misc.setParamDouble(ps, pt == null ? Misc.getUndefDouble() : pt.getLongitude(),  colIndex++);
			Misc.setParamDouble(ps, pt == null ? Misc.getUndefDouble() : pt.getLatitude(),  colIndex++);
			Misc.setParamDouble(ps, reg == null || reg.second == null ? Misc.getUndefDouble() : reg.second.m_urCoord.getLongitude() - reg.second.m_llCoord.getLongitude(),  colIndex++);
			Misc.setParamDouble(ps, reg == null || reg.second == null ? Misc.getUndefDouble() : reg.second.m_urCoord.getLatitude() - reg.second.m_llCoord.getLatitude(),  colIndex++);
			Misc.setParamInt(ps, bean.getSubType(), colIndex++);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				bean.setOpStationId(rs.getInt(1));
			}
			rs.close();
			ps.close();
			retval = bean.getOpStationId();
			
			ps = conn.prepareStatement("insert into opstation_mapping (op_station_id, port_node_id, type) values (?,?,?)");
			ps.setInt(1, bean.getOpStationId());
			ps.setInt(2, portNodeId);
			ps.setInt(3, opstationType);
			ps.execute();
			ps.close();
			
			//now add to cache ..
			if (opStationCache != null)
				opStationCache.put(bean.getOpStationId(), bean);
			if (customerOpStationCache != null) {
				ConcurrentHashMap<Integer, OpStationLookup> addTo = customerOpStationCache.get(portNodeId);
				if (addTo == null) {
					addTo = new ConcurrentHashMap<Integer, OpStationLookup>();
					customerOpStationCache.put(portNodeId, addTo);
				}
				OpStationLookup theList = addTo.get(opstationType);
				if (theList == null) {
					theList = new OpStationLookup();
					addTo.put(opstationType, theList);
				}
				theList.addStation(bean.getOpStationId(), conn, opstationType == TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE, true);
			}			
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static void main(String[] args) {
		OpStationBean bean = getOpStation(3469029);
		OpStationBean bean2 = getOpStation(3462698);
		int dbg=1;
		dbg++;
		bean.setAlertEmailL2Customer(null);
		bean.setAlertEmailL2Sender(null);
		bean.setAlertEmailL2Transporter(null);
		bean.setAlertPhoneL2Customer(null);
		bean.setAlertPhoneL2Sender(null);
		bean.setAlertPhoneL2Transporter(null);
		bean.setAlertUserL2Customer(null);
		bean.setAlertUserL2Sender(null);
		bean.setAlertUserL2Transporter(null);
		
		bean2.setAlertEmailL2Customer("b2CE2");
		bean2.setAlertEmailL2Sender("b2SE2");
		bean2.setAlertEmailL2Transporter("b2TE2");
		bean2.setAlertPhoneL2Customer("b2CP2");
		bean2.setAlertPhoneL2Sender("b2SP2");
		bean2.setAlertPhoneL2Transporter("b2TP2");
		bean2.setAlertUserL2Customer("b2CU2");
		bean2.setAlertUserL2Sender("b2SU2");
		bean2.setAlertUserL2Transporter("b2TU2");
		
		System.out.println(bean.getAlertEmailL1Customer());
		System.out.println(bean.getAlertEmailL2Customer());
		System.out.println(bean.getAlertPhoneL1Customer());
		System.out.println(bean.getAlertPhoneL2Customer());
		System.out.println(bean.getAlertUserL1Customer());
		System.out.println(bean.getAlertUserL2Customer());

		System.out.println(bean.getAlertEmailL1Transporter());
		System.out.println(bean.getAlertEmailL2Transporter());
		System.out.println(bean.getAlertPhoneL1Transporter());
		System.out.println(bean.getAlertPhoneL2Transporter());
		System.out.println(bean.getAlertUserL1Transporter());
		System.out.println(bean.getAlertUserL2Transporter());
		
		System.out.println(bean.getAlertEmailL1Sender());
		System.out.println(bean.getAlertEmailL2Sender());
		System.out.println(bean.getAlertPhoneL1Sender());
		System.out.println(bean.getAlertPhoneL2Sender());
		System.out.println(bean.getAlertUserL1Sender());
		System.out.println(bean.getAlertUserL2Sender());
		
		System.out.println(bean2.getAlertEmailL1Customer());
		System.out.println(bean2.getAlertEmailL2Customer());
		System.out.println(bean2.getAlertPhoneL1Customer());
		System.out.println(bean2.getAlertPhoneL2Customer());
		System.out.println(bean2.getAlertUserL1Customer());
		System.out.println(bean2.getAlertUserL2Customer());

		System.out.println(bean2.getAlertEmailL1Transporter());
		System.out.println(bean2.getAlertEmailL2Transporter());
		System.out.println(bean2.getAlertPhoneL1Transporter());
		System.out.println(bean2.getAlertPhoneL2Transporter());
		System.out.println(bean2.getAlertUserL1Transporter());
		System.out.println(bean2.getAlertUserL2Transporter());
		
		System.out.println(bean2.getAlertEmailL1Sender());
		System.out.println(bean2.getAlertEmailL2Sender());
		System.out.println(bean2.getAlertPhoneL1Sender());
		System.out.println(bean2.getAlertPhoneL2Sender());
		System.out.println(bean2.getAlertUserL1Sender());
		System.out.println(bean2.getAlertUserL2Sender());
	}
    //TripInfoCache end

}

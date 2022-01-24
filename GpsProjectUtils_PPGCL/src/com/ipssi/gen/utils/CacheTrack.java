/*
General note on looking in hashmap: need to evaluate this against use of ConcurrentHashMap
In the code below we keep information on a per vehicle basis in HashMap - some of these are from Setup Page. While another updates by looking into incoming data. 

In general additions/deletions to the hashmap is supposed to be rare. Concurrent gets are fine including changing of value for key
is fine - but concurrent get and put/remove is not fine. Undefined behaviour including exception, infinite loops can happen

Therefore to avoid synchronization penalty in look up we follow the following pattern
 1. Keep a global HashMap 
 To look value for a key
 2. make a local var point to the hashmap
 3. if it is null do a sync load from the database (see 6)
 4. do a get on this local var. if the vehicle is found in the database, we are done
 5. if the get returns null, then we need to do an incremental load (incremental load also can be called explicitly) 
 6 This incremental load happens in synchronized (class def sync) as follows:
 7. clone the hashmap (cloning will do a shallow copy i.e. keys/values are not cloned)
 8. load the incremental stuff
 9. set the global stuff to cloned hashmap
 10. go back to step 4.


 Data structure:
 HashMap of VehicleSetup - this keyed by vehicle id and points to setupInfo for vehicle
                           Plus any other 'calculated' setup info
            Currently this keeps ownerOrgId of the vehicle, plus a distCalcControl. Others can be added
 HashMap of PosTextHelper - this keyed by vehicle id and points to last info looked up for vehicle            
 */
package com.ipssi.gen.utils;

import static com.ipssi.gen.utils.Cache.DATE_TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.cache.DeviceModel;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.deviceMessaging.MessageCache;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.mapguideutils.ShapeFileBean;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.tripprocessor.dashboard.bean.TrackRegionInfoVO;
import com.ipssi.tripprocessor.dashboard.bean.VOInterface;
import com.sun.org.apache.xerces.internal.impl.RevalidationHandler;


public class CacheTrack {
	private static ConcurrentHashMap<Integer, Triple<Long, Double, Double>> vehicleAndStopSinceTime = new ConcurrentHashMap<Integer,Triple<Long, Double, Double>>();
	public static  Triple<Long, Double, Double> getStopSinceTimeLonLat(Connection conn, int vehicleId) throws Exception{
    	Map<Integer, Triple<Long, Double, Double>> lookup = vehicleAndStopSinceTime;
    	Triple<Long, Double, Double> retval = null;
    	if (lookup != null) {
    		retval = lookup.get(vehicleId);
    	}
    	if (retval == null) {
    		lookup = loadVehicleStopSinceTimeLongLat(conn);
    		retval = lookup.get(vehicleId);
    	}
    	return retval;
    }
	public synchronized static Map<Integer, Triple<Long, Double, Double>> loadVehicleStopSinceTimeLongLat(Connection conn) throws Exception{
		Map<Integer, Triple<Long, Double, Double>> stopSince = vehicleAndStopSinceTime;
		String query = "select vehicle_id,stop_since,stop_since_log,stop_since_lat from current_data where attribute_id=0";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				int vehicleId = rs.getInt(1);
				Timestamp stopSinceTime = rs.getTimestamp(2);
				Long time = null;
				if (!rs.wasNull()) {
					time = stopSinceTime.getTime();
				}
				double lon = rs.getDouble(3);
				double lat = rs.getDouble(4);
				Triple<Long, Double, Double> timeLatLong = new Triple<Long, Double, Double>(time,lon,lat);
				stopSince.put(vehicleId,timeLatLong);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return stopSince;
	}
	public static String standardizeNameNew(String name) {//for vehicle like 0up80ac5566
			if(name == null || name.length() <= 0)
				return name;
			if(!name.matches("-?\\d+(\\.\\d+)?")){
				while(!Character.isLetter(name.charAt(0)) )
					name = name.substring(1);
			}
	    	return (name == null ? null : name.replaceAll("[^A-Za-z0-9_]", "").toUpperCase());
	}
	public static String standardizeName(String name) {//duplicate code ... copied from VehicleBean
    	return (name == null ? null : name.replaceAll("[^A-Za-z0-9_]", "").toUpperCase());
    }
	public static class VehicleSetup
	// this essentially is supposed to encapsulate the VehicleSetup page. Currently it does only org setup
	// In addition it loads up a DistCalcControl which will be used to help in distance calcuation control
	{
		public static long g_lastLoadSetup = 0;
		public volatile static boolean g_doDynamicLoadSetup = true; //will be set to false when TcpServer starts ... so as to avoid unnecessary loads

		public static int g_forceCleanUpIfPtsCountExceeds = 1750; //about 1.2*1440
		public static int g_ptsAfterForcedCleanup = 1450; //lil more than 1440
		public static int g_minGpsRecTimeSecRelMxToLoad = 103680; //1.2*seconds in day
		public static int g_considerAsDataIfCurrentMoreThanXMsContinous = 8*3600000;
		public static int g_minContinousCurrentDataCountRecvBeforeAssumeNoDC = 300;
		
		private volatile static ConcurrentHashMap<Integer, VehicleSetup> g_setupInfo = null;//new ConcurrentHashMap<Integer, VehicleSetup>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
		// //getSetup is the accessor
		private volatile static ConcurrentHashMap<String, Integer> g_setupVehcileIdByDeviceId = null;//new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f,4);
		private volatile static ConcurrentHashMap<String, Integer> g_setupVehicleIdByStdName =  null;//new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f,4);
		private volatile static ConcurrentHashMap<String, Integer> g_setupVehicleIdBy4digitStdName =  null;//new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f,4);
		private volatile static ConcurrentHashMap<String, Integer> g_setupVehicleIdByBLETag =  null;//new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f,4);
		// 4);
		private static Object g_junkSyncVariable = new Object();
		public boolean m_strictForwarding = false;
		public boolean m_strictAckBack = false;
		public boolean m_mdtFlag = false;
		public byte dalaOrIdling = -1;
		public byte strikeOrCycle = 2;

		public static class DistCalcControl {
			public double m_distThreshSame = 0.050; // if next point is within this much dist then dont calculate name - ID_CHECK_USER_DEFINED_LANDMARK_INT
			public boolean m_doCheckUDLandmark = true; // to look up name from user defined landmarks - ID_CHECK_USER_DEFINED_REGION_INT
			public boolean m_doCheckUDRegionLayer = true;// to look up name from user defined regions
			public ArrayList<String> m_baseMapLandmarkLayers = null; // list of land mark layers on base map - multiple can be given - ID_BASEMAP_LANDMARK_LAYERS_STR
			public ArrayList<String> m_baseMapRoadLayers = null; // ID_BASEMAP_ROAD_LAYERS_STR
			public ArrayList<String> m_baseMapGetRoadNameFirst = null; // in case of ACC/Mines, the name ignores landmark name if it is near a
			private ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> m_baseMapLandmarkRTree = null;//will be populated on demand and assumes that loading has been done which will be done if being loaded on demand
			private ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> m_baseMapRoadRTree = null; // ID_BASEMAP_ROAD_LAYERS_STR
			private ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> m_baseMapGetRoadNameFirstRTree = null; // in case of ACC/Mines, the name ignores landmark name if it is near a
			
			
			public ArrayList<String> m_baseMapRegionSequenceLayers = null;// when calculating district, state, country - the layer names that have these
			// ..ID_BASEMAP_REGIONSEQUENCE_LAYERS_STR

			public double[] m_UDDistThresholdKM = { 0.1, 0.5, 2 }; // when looking in user defined layers use the distances in this array progressively to find points in the box.
			// Only 3.ID_USER_DEFINED_DIST_RANGE_DBL
			public double[] m_baseDistThresholdKM = { 5, 10, 50 }; // same when base mapy location names
			public double[] m_roadDistThresholdKM = { 0.1, 1, 10 }; // same for fidning nearest distance
			// road.ID_BASEMAP_ROAD_LAYERS_GETFIRST_STR
			public double[] m_roadFirstThresholdKM = { 0.02, 0.1, 0 }; // how close the road should be (for ACC/Mines) ID_BASEMAP_ROAD_RANGE_GETFIRST_DBL
			public double m_tellDistanceIfGreaterKM = 0.3;// name is like xyz a km NW ID_SHOW_DIST_IF_GREATER
			public String m_distanceFormat = "###,###.##";
			public double m_distThresholdForStopped = 0.050; // used stop/moving control ID_STOP_DIST_MARGIN
			public double m_distThresholdForRetainingData = 0.003; // if curr point distance from prev < this distnce then dont write in DB. Currently not implemented - to use for
			public double m_tellDistrictStateInfoIfDistGreaterThanKM = 3;// name is like xyz a km NW ID_SHOW_DIST_IF_GREATER
			// retaining/ignoring data

			public double m_linkedGateWaitBoxMtr = 60;
			public double m_linkedOpAreaBoxMtr = 15;
			public double m_linkedShiftDistExceedsMtr = 6;
			public double m_orientationCalcThreshold = 50;
			public static java.text.DecimalFormat m_distanceFormatter = new java.text.DecimalFormat("###,###.##");
			public double m_processTripIfDistExceedsMtr = 6;
			public double m_tripMovingIfSpeedExceedsKMPH = 5;
			public double m_tripStoppedIfSpeedLessKMPH = 1;
			public boolean toSendMessage = false;
			public boolean bestLUisValidExtremum = false;
			public boolean toTrackIntermediateLU = false;
			public boolean m_bestLoadAreaIsFirst = false;
			public boolean m_bestLoadAreaIsLast = false;
			public boolean m_bestUnloadAreaIsFirst = false;
			public boolean m_bestUnloadAreaIsLast = false;
			public boolean m_luMustBeProper = false;
			// Shift Date
			public boolean m_markLoadWaitInAsShiftDate = false;
			public boolean m_markLoadGateInAsShiftDate = false;
			public boolean m_markLoadAreaInAsShiftDate = false;
			public boolean m_markLoadAreaOutAsShiftDate = false;
			public boolean m_markLoadGateOutAsShiftDate = false;
			public boolean m_markLoadWaitOutAsShiftDate = false;

			public boolean m_markUnloadWaitInAsShiftDate = false;
			public boolean m_markUnloadGateInAsShiftDate = false;
			public boolean m_markUnloadAreaInAsShiftDate = false;
			public boolean m_markUnloadAreaOutAsShiftDate = false;
			public boolean m_markUnloadGateOutAsShiftDate = false;
			public boolean m_markUnloadWaitOutAsShiftDate = false;
			public boolean m_forceLookUpLocation = false;
			// Challan
			public int m_lookForChallan = 0;
			public boolean m_showBaseMapDetails = true;
			public int m_todeviceMessageRemoval = MessageCache.REMOVE_ALL_ACKNOWLEDGED ;
			public boolean m_doMergeConsecutiveLLUU = true;
			public boolean m_doShortTripPoorNW = false;
			public boolean m_doStopMarker = false;
			public boolean m_doPrevStateFromLUSequence = false;
			public int m_materialLookUpApproach = 0;
			public boolean m_pickFurthestUnloadOpstation = false;
			public boolean getBestAmongstSameOpIdMultiLU = true; //for ambuja false 
			public boolean doMultiLoadMultiLU = true; //for ambuja false
			public boolean doMultiUnloadMultiLU = false; //for ambuja true
			public boolean m_PreLoadProce_PreIsIn = false;//false for balaji, true form ambuja ... eventually get rid of it and process it based on 
			public boolean m_togupOrgforOpStation = true;
			public int m_afterUnloadAuto = 0; //gobackToFrom station, 1 wait as is, 2 goto base station
			public boolean m_doETA = false;
			public boolean m_doMinOfGpsSpeedAndCalcSpeed = false;
			public boolean m_doForOnlyLoad = false;
			public boolean m_doNewApproachForTrip = true;
			public int m_getNearestOpForStop = -1;//if -1 then dont get (default), if 0 then get of any type, else get nearest Opstation of sub type
			public int m_getNearstSpecialLM = -1;//if 0 then dont get (default), 0 get of any type, ese get of nearest sub type
			public double m_threshKMForNearestOp = 50;
			public double m_threshKMForNearestLM = 50;
			public int m_sendTPDataToRP = 0; //NOT USED
			public boolean m_useChallanInPreferredLU = true;
			public boolean m_usePrevLUInPreferredLU = true;
			public boolean m_doMRSstyleForHybridLUAlway = false; //NOT USED
			public int doScanProcessing = 0;
			public int doETAProcesingNew = 0;
			public int managementUnitPortNodeId = Misc.getUndefInt();
			public int teltonikaSkipSec = 9;

//			public static int ID_INT_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE = 156; //if -1 then dont get (default), if 0 then get of any type, else get nearest Opstation of sub type
//			public static int ID_INT_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE = 157; //if 0 then dont get (default), if 1 get of type
//			public static int ID_DOUBLE_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE_MAXDIST = 30; //default = 50
//			public static int ID_DOUBLE_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE_MAXDIST = 31; //default = 50
			
			private ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> helpGetRTree(ArrayList<String> forThis) {
				ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> retval = new ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>>();
				for (int i=0,is = forThis.size();i<is;i++) {
					String n = forThis.get(i);
					Pair<RTree, Map<Integer, ShapeFileBean>> info = RTreesAndInformation.getRTreeForShp(n);
					if (info != null)
						retval.add(info);
				}
				return retval;
			}
			public void clearBaseMapRTreeMapping() {
				m_baseMapLandmarkRTree = null;//will be populated on demand and assumes that loading has been done which will be done if being loaded on demand
				m_baseMapRoadRTree = null; // ID_BASEMAP_ROAD_LAYERS_STR
				m_baseMapGetRoadNameFirstRTree = null; // in case of ACC/Mines, the name ignores landmark name if it is near a
			}
			public ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> getBaseMapLandmarkRTree() {
				if (m_baseMapLandmarkRTree == null) {
					m_baseMapLandmarkRTree = helpGetRTree(m_baseMapLandmarkLayers);
				}
				return m_baseMapLandmarkRTree;
			}
			public ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> getBaseMapRoadRTree() {
				if (m_baseMapRoadRTree == null) {
					m_baseMapRoadRTree = helpGetRTree(m_baseMapRoadLayers);
				}
				return m_baseMapRoadRTree;
			}
			public ArrayList<Pair<RTree, Map<Integer, ShapeFileBean>>> getBaseMapGetRoadNameFirstRTree() {
				if (m_baseMapGetRoadNameFirstRTree == null) {
					m_baseMapGetRoadNameFirstRTree = helpGetRTree(m_baseMapGetRoadNameFirst);
				}
				return m_baseMapGetRoadNameFirstRTree;
			}			
		}

		volatile private DistCalcControl m_distCalcControl = null; // a cache copy is kept so that we dont have to move thru the org tree
		volatile private StopDirControl m_stopDirControl = null;
		
		private boolean hasCummDist = false;
		private boolean hasSpeed = false;
		private boolean hasOrientation = false;
		private double base = 0;
		private String deviceId = null;
		private String bleTag1 = null;
		private String bleTag2 = null;
		private String shortCode = null;
		private String simNumber = null;
		private int deviceVersion;
		private ArrayList<String> commandWords = new ArrayList<String>();
		public boolean m_sendToRule = true; // should be populated using vehiclesetup page
		public boolean m_sendToTrip = true; // should be populated using vehiclesetup page
		public int m_backPtsToRetain = 30;//because of data channel stuff 
		public int m_manual_adj_tz_min = Misc.getUndefInt();

		private int rebootFreq = Misc.getUndefInt();
		private int dropConnectFreq = Misc.getUndefInt();
		private int workArea = Misc.getUndefInt();
		private int lastIndex = Misc.getUndefInt(); //only for teltonika devices
		private long lastGpsRecordTime = Misc.getUndefInt();//only for teltonika devices
		private long lastGpsReceiveTime = Misc.getUndefInt();//only for teltonika devices
		
		//to forward datapoint to another ip and port...
		private ArrayList<Pair<String,Integer>> intServerIpPort = new ArrayList<Pair<String,Integer>>();
		private ArrayList<Pair<String,Integer>> serverIpPort = null;
		//to check data processed from gpsdata_backup_mode table ...
		private Pair<Date, Date> lastGpsDataProcessTime = new Pair<Date, Date>(null,null);
		private int ioMapSetId = Misc.getUndefInt();
		private String redirectUrl = null;
		public int deviceModelInfoId = Misc.getUndefInt();
		public int cummDistProvided; // 0 not there, 1 cumm, 2 delta, 3 canbus
		public int cummDistCalcApproach;// 0 dont. 1 complex, 2 simple
		public double cummDataToMeter;
		public double distAdjFactor;
		public String getShortCode() {
			return this.shortCode;
		}
		public static void invalidateCachedLayerInfo() {
			System.out.println("[TRACE] Clearing Cached Layer to Rtree Map Info");
			ConcurrentHashMap<Integer, VehicleSetup> setupInfo = g_setupInfo;
			if (setupInfo != null) {
				Collection<VehicleSetup> values = setupInfo.values();
				if (values != null) {
					for (VehicleSetup setup : values) {
						if (setup.m_distCalcControl != null)
							setup.m_distCalcControl.clearBaseMapRTreeMapping();
					}
				}
			}
		}
		public static void makeDistCalcControlDirtyForAll() {
			ConcurrentHashMap<Integer, VehicleSetup> setupInfo = g_setupInfo;
			if (setupInfo != null) {
				Collection<VehicleSetup> values = setupInfo.values();
				if (values != null) {
					for (VehicleSetup setup : values) {
						setup.makeDistCalcControlDirty();
					}
				}
			}
		}

		public void makeDistCalcControlDirty() {
			m_distCalcControl = null;
		}
		public StopDirControl getStopDirControl(Connection conn) throws Exception {
			if (m_stopDirControl == null) 
				m_stopDirControl = StopDirControl.getControlFromOrg(Cache.getCacheInstance(conn).getPortInfo(m_ownerOrgId, conn));
			return m_stopDirControl;
		}
		
		public DistCalcControl getDistCalcControl(Connection conn) throws Exception {
			if (m_distCalcControl == null) {
				m_distCalcControl = loadDistCalcControl(conn);
			}
			return m_distCalcControl;
		}
		
		public int m_vehicleId = Misc.getUndefInt();
		public int m_ownerOrgId = Misc.getUndefInt();
		public ArrayList<Integer> m_accessGroups = null;
		public String m_name = null;
		public String m_customer_name = null;
		public int m_type = Misc.getUndefInt();
//		public Date m_ignoreBefore = null;
//		public Date m_ignoreAfter = null;
		public long m_ignoreBefore = Misc.getUndefInt();
		public long m_ignoreAfter = Misc.getUndefInt();
		// Related to vehicle status and extended status
		public int m_status = Misc.getUndefInt();
		public int m_extended_status = Misc.getUndefInt();
		public int m_other_vehicle_id = Misc.getUndefInt();
		public int m_tripParamProfileId = Misc.getUndefInt();
		// Trip Alert related mail and phone
		public String alert_mail_id = null;
		public String alert_phone = null;
		
		// Related to Current Data Cache
		// detailed_status,miscellaneous,fieldone,fieldtwo,fieldthree,fieldfour,fieldfive,fieldsix,fieldseven,fieldeight,fieldnine,fieldten,
		// fieldeleven,fieldtwelve,fieldthirteen,fieldfourteen,work_area
		
		public int m_detailed_status = Misc.getUndefInt();
		/*public String m_miscellaneous = null;
		public String m_fieldone = null;
		public String m_fieldtwo = null;
		public String m_fieldthree = null;
		public String m_fieldfour = null;
		public String m_fieldfive = null;
		public String m_fieldsix = null;
		public String m_fieldseven = null;
		public String m_fieldeight = null;
		public String m_fieldnine = null;
		public String m_fieldten = null;
		public String m_fieldeleven = null;
		public String m_fieldtwelve = null;
		public String m_fieldthirteen = null;
		public String m_fieldfourteen = null;
		public String m_work_area = null;
		// plant,registeration_number,registeration_number_expiry,insurance_number,insurance_number_expiry,permit1_number,
		// permit1_number_expiry,permit1_desc,permit2_number,permit2_number_expiry,permit2_desc
		public int m_plant = Misc.getUndefInt();
		public String m_registeration_number = null;
		public long m_registeration_number_expiry = Misc.getUndefInt();
		public String m_insurance_number = null;
		public long m_insurance_number_expiry = Misc.getUndefInt();
		public String m_permit1_number = null;
		public long m_permit1_number_expiry = Misc.getUndefInt();
		public String m_permit1_desc = null;
		public String m_permit2_number = null;
		public long m_permit2_number_expiry = Misc.getUndefInt();
		public String m_permit2_desc = null;*/
		public int m_flag = 0;//being used for flagging
		public boolean shovelType = false;
		private Date endDt;
		public int m_subType = Misc.getUndefInt();
		
		public static VehicleSetup getSetup(int vehicleId, Connection dbConn) {
			return getSetup(new Integer(vehicleId), dbConn);
		}
		public static int getSetupBy4Digit(String name, Connection conn) throws Exception {
			try {
				Map<String, Integer> setup = VehicleSetup.g_setupVehicleIdBy4digitStdName;
				if (setup == null) {
					loadSetup(conn, null);
					setup = g_setupVehicleIdBy4digitStdName;
				}

				if (setup.containsKey(name)) {
					return setup.get(name);
				} else if (g_doDynamicLoadSetup) {
					long now = System.currentTimeMillis();
					if (now-VehicleSetup.g_lastLoadSetup > 600000){//!checkName.equals(VehicleSetup.lastLoadDevOrVeh)) {
						VehicleSetup.loadSetup(conn, null, false);
						VehicleSetup.g_lastLoadSetup = now;
						Integer rv = setup.get(name);
						return rv == null ? Misc.getUndefInt() : rv.intValue();
					}
				}
				return Misc.UNDEF_VALUE;

			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		public static int getSetupByStdName(String name, Connection dbConn) throws Exception {
			try {

				Map<String, Integer> setup = g_setupVehicleIdByStdName;
				if (setup == null) {
					loadSetup(dbConn, null);
					setup = g_setupVehicleIdByStdName;
				}

				if (setup.containsKey(name)) {
					return setup.get(name);
				} else if (g_doDynamicLoadSetup) {
					long now = System.currentTimeMillis();
					if (now-VehicleSetup.g_lastLoadSetup > 600000){//!checkName.equals(VehicleSetup.lastLoadDevOrVeh)) {
						VehicleSetup.loadSetup(dbConn, null, false);
						VehicleSetup.g_lastLoadSetup = now;
						Integer rv = setup.get(name);
						return rv == null ? Misc.getUndefInt() : rv.intValue();
					}
				}
				return Misc.UNDEF_VALUE;

			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		public static int getSetupByBLE(String tag, Connection dbConn) throws Exception {
			try {

				Map<String, Integer> setup = g_setupVehicleIdByBLETag;
				if (setup == null) {
					loadSetup(dbConn, null);
					setup = g_setupVehicleIdByBLETag;
				}

				if (setup.containsKey(tag)) {
					return setup.get(tag);
				} else if (g_doDynamicLoadSetup) {
					long now = System.currentTimeMillis();
					if (now-VehicleSetup.g_lastLoadSetup > 600000){//!checkName.equals(VehicleSetup.lastLoadDevOrVeh)) {
						VehicleSetup.loadSetup(dbConn, null, false);
						VehicleSetup.g_lastLoadSetup = now;
						Integer rv = setup.get(tag);
						return rv == null ? Misc.getUndefInt() : rv.intValue();
					}
				}
				return Misc.getUndefInt();

			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			
		}
		public static int getSetup(String deviceId, Connection dbConn) throws Exception {
			try {

				Map<String, Integer> setup = g_setupVehcileIdByDeviceId;
				if (setup == null) {
					loadSetup(dbConn, null);
					setup = g_setupVehcileIdByDeviceId;
				}

				if (setup.containsKey(deviceId)) {
					return setup.get(deviceId);
				} else if (g_doDynamicLoadSetup) {
					long now = System.currentTimeMillis();
					if (now-VehicleSetup.g_lastLoadSetup > 600000){//!checkName.equals(VehicleSetup.lastLoadDevOrVeh)) {
						VehicleSetup.loadSetup(dbConn, null, false);
						VehicleSetup.g_lastLoadSetup = now;
						Integer rv = setup.get(deviceId);
						return rv == null ? Misc.getUndefInt() : rv.intValue();
					}
				}
				return Misc.UNDEF_VALUE;

			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		public static VehicleSetup getSetup(Integer vehicleId, Connection dbConn) {
			try {
				VehicleSetup retval = null;
				Map<Integer, VehicleSetup> setupInfo = g_setupInfo;
				if (setupInfo == null) {
					loadSetup(dbConn, null, true);
					setupInfo = g_setupInfo;
				}
				if (setupInfo != null) {
					retval = setupInfo.get(vehicleId);
					if (retval == null && g_doDynamicLoadSetup) {
						long now = System.currentTimeMillis();
						if (now-VehicleSetup.g_lastLoadSetup > 600000){//!checkName.equals(VehicleSetup.lastLoadDevOrVeh)) {
							VehicleSetup.loadSetup(dbConn, null, false);
							VehicleSetup.g_lastLoadSetup = now;
							retval = setupInfo.get(vehicleId);
						}
					}
					return retval;
					// if (retval != null)
					// return retval;
				}
				// either no data for the vehicle found or the data has not been loaded
				
				return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		/*private boolean filterVehicle(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper,int vehicleId){
			boolean doVehicle = false;
			Connection conn = null;
			Value val = null;
			String vehicleName;
			String orgId;
			Date startDt = null,endDt = null;
			boolean isValid = false;
			try{
				if (searchBoxHelper == null || Misc.isUndef(vehicleId))
					return true;
				if(_session != null)
					conn = _session.getConnection();
				String topPageContext = searchBoxHelper.m_topPageContext;
				boolean seenVehicleStatus = false;
				SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
				SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
				for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
					ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
					int colSize = rowInfo.size();
					for (int j=0;j<colSize;j++) {
						isValid = true;
						DimConfigInfo dimConfig = rowInfo.get(j);
						if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
							continue;
						boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
						boolean isTime = "20506".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype);
						int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
						String tempVarName = is123 ? "pv123" : topPageContext+paramId;
						String tempVal = _session.getAttribute(tempVarName);
						doVehicle = paramId == 9008 || paramId == 9002 || paramId == 9003;
						if(paramId == 20159)
							continue;
						if(paramId == 20495){
							if(tempVal == null || "null".equalsIgnoreCase(tempVal) || "-1".equals(tempVal))
								continue;
						}
						seenVehicleStatus = seenVehicleStatus || (dimConfig.m_dimCalc.m_dimInfo.m_id == 9008 && tempVal != null && !"-1000".equals(tempVal) 
								&& tempVal.length() > 0) || (dimConfig.m_dimCalc.m_dimInfo.m_subsetOf == 9000 && tempVal != null && !"-1000".equals(tempVal));
						if(paramId == 20450 && "-1000".equals(tempVal)){
							ArrayList dimList = DimInfo.getDimInfo(20450).getValList();
							StringBuilder sb = new StringBuilder();
							ArrayList inList = new ArrayList();
							inList.add(0);
							for (int k = 0; k < dimList.size(); k++) {
								inList.add(((ValInfo)dimList.get(k)).m_id);
							}
							Misc.convertInListToStr(inList, sb);
							tempVal = sb.toString();
							_session.setAttribute(tempVarName, tempVal, false);
						}
						else if(paramId == 20450 && tempVal.length() > 0){
							tempVal = "0,"+tempVal;
							_session.setAttribute(tempVarName, tempVal, false);
						}
						if(is123 && tempVal != null){
							orgId = tempVal;
						}
						if(paramId == 9002){
							vehicleName = tempVal;
						}
						if((!is123 && tempVal != null && !"".equals(tempVal) && !"-1000".equals(tempVal))){
							int type = dimConfig.m_dimCalc.m_dimInfo.m_type;
							String tName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
							if (tName.equalsIgnoreCase("Dummy"))
								continue;
							if (dimConfig.m_numeric_filter && (tempVal == null || tempVal.length() == 0))
								continue;
							if(!("trick".equals(tName) || "Singleton".equals(tName))){

								boolean isStartDateId = false;
								boolean isEndDateId = false;
								val = CacheValue.getValueInternal(conn, vehicleId, paramId);
								if(type == Cache.DATE_TYPE){
									try {
										Date dt = null;
										if (isTime) {
											try {
												dt = sdfTime.parse(tempVal);
											}
											catch (ParseException e2) {
												dt = sdf.parse(tempVal);
											}
										}
										else {
											try {
												dt = sdf.parse(tempVal);
											}
											catch (ParseException e2) {
												dt = sdfTime.parse(tempVal);
											}
										}
										if(isStartDateId)
											startDt = new java.util.Date( dt.getTime());
										if(isEndDateId)
											endDt = new java.util.Date( dt.getTime());
									} catch (ParseException e) {
										e.printStackTrace();
										throw e;
									}

								}
								if(dimConfig.m_numeric_filter)
								{
									String aggregateOp = null;
									if (dimConfig.m_aggregate) {
										String aggParamName = searchBoxHelper == null ? "p20053": searchBoxHelper.m_topPageContext+"20053";
										int aggDesired =Misc.getParamAsInt(_session.getParameter(aggParamName));
										DimInfo aggDim = DimInfo.getDimInfo(20053);
										if (aggDim != null) {
											DimInfo.ValInfo valInfo = aggDim.getValInfo(aggDesired);
											if (valInfo != null) {
												aggregateOp = valInfo.getOtherProperty("op_text");
											}
										}
										if (dimConfig.m_default != null && !"".equals(dimConfig.m_default))
											aggregateOp = dimConfig.m_default;
										if (aggregateOp == null || aggregateOp.length() == 0)
											aggregateOp = "sum";
									}
									String tempVarNameOperator = tempVarName + "_operator";
									String tempVarNameOperandFirst = tempVarName + "_operand_first";
									String tempVarNameOperandSecond = tempVarName + "_operand_second";
									String paramValOperator = _session.getAttribute(tempVarNameOperator);
									String paramValOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
									String paramValOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
									StringBuilder sb = new StringBuilder();
									if(paramValOperator != null && paramValOperator.length() > 0 && tempVarNameOperandFirst != null
											&& tempVarNameOperandFirst.length() > 0){
										int operator = Misc.getParamAsInt(paramValOperator);
										double  operandFirst = Misc.getParamAsDouble(paramValOperandFirst);
										double operandSecond = Misc.getParamAsDouble(paramValOperandSecond);
										//int operandFirst = Misc.getParamAsInt(paramValOperandFirst);
										switch(operator){
										case 1:
											isValid = val.getDoubleVal() > operandFirst;
											break;
										case 2:
											isValid = val.getDoubleVal()  < operandFirst;
											break;
										case 3:
											isValid = val.getDoubleVal()  == operandFirst;
											break;
										case 4:
											isValid = val.getDoubleVal()  >= operandFirst;
											break;
										case 5:
											isValid = val.getDoubleVal()  <= operandFirst;
											break;
										case 6:
											isValid = val.getDoubleVal()  != operandFirst;
											break;
										case 7:
											isValid = val.getDoubleVal()  >= operandFirst && val.getDoubleVal()  <= operandSecond;
											break;
										}
										continue;
									}
								}
								if (dimConfig.m_dimCalc.m_dimInfo.m_type == Cache.STRING_TYPE) {
									for (int l=0,ls = dimConfig.m_addnlDimInfoNew == null  ? 1 : dimConfig.m_addnlDimInfoNew.size()+1; l<ls; l++) {
										DimInfo useme = l == 0 ? dimConfig.m_dimCalc.m_dimInfo : ((DimCalc) dimConfig.m_addnlDimInfoNew.get(l-1)).m_dimInfo;
										String [] tempValArray = tempVal.split(",");
										boolean first = true;
										if(tempValArray != null && tempValArray.length > 0)
											for (int k = 0; k < tempValArray.length; k++) {
												if (tempValArray[k] == null)
													continue;
												tempValArray[k] = tempValArray[k].trim();
												if (tempValArray[k].length() == 0)
													continue;
												if ("null".equals(tempValArray[k])){
													isValid = val.getStringVal() == null;
												}
												else {
													isValid = val.getStringVal() != null && val.getStringVal().contains(tempValArray[k]);
												}
												if(isValid)
													break;
											}
									}//for each multi valued
								}//for each orable dim to search
								int paramInt = Misc.getUndefInt();
								int i_Val = val != null ? val.getIntVal() : Misc.getUndefInt();
								if(type != Cache.DATE_TYPE && type != Cache.STRING_TYPE)
									paramInt = Misc.getParamAsInt(tempVal);
								switch(type){

								case LOV_NO_VAL_TYPE : 
										isValid = i_Val == paramInt;
										break;
									case INTEGER_TYPE : 
										isValid = i_Val == paramInt;
										break;
								case DATE_TYPE :
									isStartDateId = PageHeader.isStartDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
									isEndDateId = PageHeader.isEndDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
									if (isStartDateId)
									{
										isValid = val != null && val.getDateVal() != null && val.getDateVal().getTime() >= startDt.getTime();
									}
									else if(isEndDateId){
										isValid = val != null && val.getDateVal() != null && val.getDateVal().getTime() <= endDt.getTime();
									}
									break;
								default : 
									String [] tempValArray = tempVal.split(",");
									if(tempValArray != null && tempValArray.length > 0)
										for (int k = 0; k < tempValArray.length; k++) {
											if (tempValArray[k] == null)
												continue;
											tempValArray[k] = tempValArray[k].trim();
											if (tempValArray[k].length() == 0)
												continue;
											if ("null".equals(tempValArray[k])){
												isValid = val.getStringVal() == null;
											}
											else {
												isValid = i_Val == Misc.getParamAsInt(tempValArray[k]);
											}
											if(isValid)
												break;
										}

									break;
								}
								if(!isValid){
									break;
								}

							}

						}
					}
					if(!isValid)
						break;
				}
				val = CacheValue.getValueInternal(conn, vehicleId, 20173);
				isValid = val == null || val.getDateValLong() <= 0 ;
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return isValid;
		}*/
		public static class VehicleInfoSortHelper implements Comparator<Pair<Integer,Long>> {
			
			public int compare(Pair<Integer,Long> v1, Pair<Integer,Long> v2) {
				return v2.second.compareTo(v1.second);
			}
		}
		public static Set<Entry<Integer, VehicleSetup>> getVehicleSet(Connection conn) throws Exception {
			Map<Integer, VehicleSetup> setup = g_setupInfo;
			if (setup == null) {
				loadSetup(conn, null);
				setup = g_setupInfo;
			}
			if(setup == null || setup.size() == 0)
				return null;
			Set<Entry<Integer, VehicleSetup>> entries = setup.entrySet();
			return entries;
		}
		
		public static ArrayList<Pair<Integer, Long>> getVehicleList(Connection conn,Cache cache,int portNodeId){ //move this to GeneralizedQueryBuilder make it filterable
			//for migration purposes method signature kept same but newer method will return null in long indicating
			//that sorting will be done elsewhere
			ArrayList<Pair<Integer, Long>> retval = null;
			Map<Integer, VehicleSetup> setup = g_setupInfo;
			try{
				if (setup == null) {
					loadSetup(conn, null);
					setup = g_setupInfo;
				}
				if(setup == null || setup.size() == 0 || Misc.isUndef(portNodeId))
					return null;
				Set<Entry<Integer, VehicleSetup>> entries = setup.entrySet();
				for (Entry<Integer, VehicleSetup> entry : entries) {
					VehicleSetup vehSetup = entry.getValue();
				   if (vehSetup.isAccessibleByOrg(portNodeId,cache,conn)) {
					   if(retval == null)
							retval = new ArrayList<Pair<Integer,Long>>();
						retval.add( new Pair<Integer, Long>(entry.getKey(), null));
				   }
				}
			}
			catch(Exception ex){	
				ex.printStackTrace();
			}
			return retval;
		}
		
		public static ArrayList<Pair<Integer, Long>> getVehicleListOld(Connection conn,Cache cache,int portNodeId){ //move this to GeneralizedQueryBuilder make it filterable
			//NOT TO BE USED - for migration purposes method signature kept same but newer method will return null in long indicating
			//that sorting will be done elsewhere
			ArrayList<Pair<Integer, Long>> retval = null;
			Map<Integer, VehicleSetup> setup = g_setupInfo;
			try{
			if (setup == null) {
				loadSetup(conn, null);
				setup = g_setupInfo;
			}
			if(setup == null || setup.size() == 0 || Misc.isUndef(portNodeId))
				return null;
			Set<Entry<Integer, VehicleSetup>> entries = setup.entrySet();
			for (Entry<Integer, VehicleSetup> entry : entries) {
					VehicleSetup vehSetup = entry.getValue();
				   if(vehSetup.isAccessibleByOrg(portNodeId,cache,conn)){
					   VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, entry.getKey(), false, false);
					   if (vdf == null)
						   continue;
					   Value val = null;
					   synchronized (vdf) {
						   val = CacheValue.getValueInternal(conn, entry.getKey(), 20173, vehSetup, vdf,null,null);
					   }
						if(val != null && val.getDateValLong() > 0 ){
							if(retval == null)
								retval = new ArrayList<Pair<Integer,Long>>();
							retval.add( new Pair<Integer, Long>(entry.getKey(),val.getDateValLong() ) );
						}
				   }
				}
			if(retval != null && retval.size() > 0)
				Collections.sort(retval, new VehicleInfoSortHelper());
			}
			catch(Exception ex){	
				ex.printStackTrace();
			}
			return retval;
		}
		private boolean isAccessibleByOrg(int portNodeId,Cache cache,Connection dbConn){
			if(Misc.isUndef(portNodeId))
				return false;
			try {
				MiscInner.PortInfo custleaf = cache.getPortInfo(this.m_ownerOrgId, dbConn);
				MiscInner.PortInfo anc = cache.getPortInfo(portNodeId, dbConn);
				if(custleaf != null && anc != null && anc.m_lhsNumber <= custleaf.m_lhsNumber && anc.m_rhsNumber >= custleaf.m_rhsNumber)
					return true;
				if(this.m_accessGroups != null && this.m_accessGroups.size() > 0){
					MiscInner.PortInfo leaf = null;
				for(Integer i : this.m_accessGroups){
					if(Misc.isUndef(i))
						continue;
					leaf = cache.getPortInfo(i, dbConn);
					if(leaf != null && anc != null && anc.m_lhsNumber <= leaf.m_lhsNumber && anc.m_rhsNumber >= leaf.m_rhsNumber)
						return true;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
        private static ArrayList<Integer> getVehicleAccessGroups(int vehicleId,Connection conn){
        	ArrayList<Integer> retval = null;
        	String query = " select port_node_id from vehicle_access_groups where vehicle_id=?";
        	ResultSet rs = null;
        	PreparedStatement ps = null;
        	if(conn != null){
        	try{
        		ps = conn.prepareStatement(query);
        		Misc.setParamInt(ps, vehicleId, 1);
        		rs = ps.executeQuery();
        		while (rs.next()) {
					if(retval == null)
						retval = new ArrayList<Integer>();
					retval.add(Misc.getRsetInt(rs, "port_node_id"));
				}
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}finally{
        		try{
        		if(rs != null)
        			rs.close();
        		if(ps != null)
        			ps.close();
        		}
        		catch(Exception ex){
        			ex.printStackTrace();
        			}
        		}
        	}
        	return retval;
        }
		synchronized public static void loadSetup(Connection dbConn, ArrayList<Integer> refreshTheseVehicles) throws Exception {
			loadSetup(dbConn, refreshTheseVehicles, true, false);
		}
		 public static void loadSetup(Connection dbConn, ArrayList<Integer> refreshTheseVehicles, boolean dontInitIfInited) throws Exception {
			 loadSetup(dbConn, refreshTheseVehicles, dontInitIfInited, false);
		 }
		 public static void loadSetup(Connection dbConn, ArrayList<Integer> refreshTheseVehicles, boolean dontInitIfInited, boolean fullErase) throws Exception {
			try {
				if (dontInitIfInited && (g_setupInfo != null && !g_setupInfo.isEmpty() && (refreshTheseVehicles == null || refreshTheseVehicles.size() == 0) ))
					return;
				synchronized (CacheTrack.VehicleSetup.g_junkSyncVariable) {
					if (dontInitIfInited && (g_setupInfo != null && !g_setupInfo.isEmpty() && (refreshTheseVehicles == null || refreshTheseVehicles.size() == 0)))
						return;
					StringBuilder dbgString = refreshTheseVehicles == null ? null : new StringBuilder("$$$[CTRefresh .. ops results]");
					Map<Integer, VehicleSetup> putInThis = null;
					Map<String, Integer> dummyPutInThis = null;
					Map<String, Integer> dummyIdByName = null;
					Map<String, Integer> dummyByBLE = null;
					Map<String, Integer> dummyIdBy4DigitName = null;
					if (fullErase && g_setupInfo != null && (refreshTheseVehicles == null || refreshTheseVehicles.size() == 0)) {
						g_setupInfo = null;;
						g_setupVehcileIdByDeviceId = null;
						g_setupVehicleIdByStdName = null;
						g_setupVehicleIdBy4digitStdName = null;
						g_setupVehicleIdByBLETag = null;
					}
					if (g_setupInfo == null) {
						putInThis = new ConcurrentHashMap<Integer, VehicleSetup>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
						dummyPutInThis = new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
						dummyIdByName = new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
						dummyIdBy4DigitName = new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
						dummyByBLE =  new ConcurrentHashMap<String, Integer>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
					}
					else {
						putInThis = g_setupInfo;
						dummyPutInThis = g_setupVehcileIdByDeviceId;
						dummyIdByName = g_setupVehicleIdByStdName;
						dummyIdBy4DigitName = g_setupVehicleIdBy4digitStdName;
						dummyByBLE = g_setupVehicleIdByBLETag;
					}
					
					//Map<Integer, VehicleSetup> putInThis = g_setupInfo;
					//Map<String, Integer> dummyPutInThis = g_setupVehcileIdByDeviceId;
					//Map<String, Integer> dummyIdByName = g_setupVehicleIdByStdName;
					
					HashMap<Integer, Integer> currListByVehicleId = new HashMap<Integer, Integer> (OrgConst.G_NUMBER_VEHICLES, 0.75f);
					rememberSetupMap(putInThis, currListByVehicleId);
	
					
					StringBuilder q = new StringBuilder(
							"SELECT vehicle.id,  vehicle.device_internal_id ,vehicle.customer_id, port_nodes.name, vehicle.name, vehicle.type, " +
							" vehicle.ignore_before, vehicle.ignore_after, device_model_info.has_cumm_dist, device_model_info.has_speed, " +
							" device_model_info.has_orientation,vehicle.cumm_base, sim_number, device_model_info.device_version," +
							" device_model_info.command_word, vehicle.do_rule, vehicle.do_trip, vehicle.reboot_freq, vehicle.drop_conn_freq," +
							" vehicle.work_area, back_points, vehicle.manual_adj_tz_min,vehicle.last_index,server_ip_1,server_port_1,server_ip_2,server_port_2, " +
							" vehicle.status status, vehicle_extended.extended_status extended_status, vehicle_extended.other_vehicle_id, " +
							" vehicle.tripparam_profile_id, vehicle.detailed_status, " +
							
							/*" vehicle.miscellaneous, vehicle.fieldone, vehicle.fieldtwo, " +
							" vehicle.fieldthree, vehicle.fieldfour, vehicle.fieldfive, vehicle.fieldsix, vehicle.fieldseven, vehicle.fieldeight, " +
							" vehicle.fieldnine,vehicle.fieldten, vehicle.fieldeleven, vehicle.fieldtwelve, vehicle.fieldthirteen, " +
							" vehicle.fieldfourteen, regions.short_code work_area, " +
							" vehicle_extended.plant, vehicle_extended.registeration_number, vehicle_extended.registeration_number_expiry," +
							" vehicle_extended.insurance_number, vehicle_extended.insurance_number_expiry, vehicle_extended.permit1_number," +
							" vehicle_extended.permit1_number_expiry, vehicle_extended.permit1_desc, vehicle_extended.permit2_number, " +
							" vehicle_extended.permit2_number_expiry,vehicle_extended.permit2_desc, " +*/
							
							" (case when vehicle.status = 0 then 10 else (case when vehicle.status = 2 then 9 else (case when vehicle.status = 1 then 8 end)end)end) stat, " +
							" vehicle.alert_mail_id, vehicle.alert_phone, vehicle.redirect_url, vehicle.io_set_id" +
							" , vehicle.flag "+
							", vehicle.cumm_dist, vehicle.use_cumm_dist, vehicle.cumm_data_to_meter, vehicle.device_model_info_id, vehicle.dist_adj_factor "+
							", vehicle.strict_forwarding, vehicle.strict_ackback, vehicle.mdt_flag, vehicle.sub_type "+
							",ble_tag1, ble_tag2 "+
							",dala_or_idling,strike_or_cycle, vehicle.short_code vehicle_code "+
							" FROM vehicle JOIN port_nodes ON (vehicle.customer_id = port_nodes.id) " +
							" left outer join device_model_info on (vehicle.device_model_info_id = device_model_info.id) " +
							" left outer join vehicle_extended on (vehicle.id = vehicle_extended.vehicle_id) " +
							/*" left outer join regions on (regions.id = vehicle.work_area)  " +*/
							//" left outer join generic_params on (generic_params.id = vehicle_extended.plant)  " +
							" WHERE (vehicle.status IN (1) or vehicle_extended.extended_status in (1)) and vehicle.device_internal_id is not null "
//							" and vehicle.customer_id between 803 and 818 "
							);


					if (refreshTheseVehicles != null && refreshTheseVehicles.size() > 0) {
						q.append(" and vehicle.id in (");
						Misc.convertInListToStr(refreshTheseVehicles, q);
						q.append(")");
					}
					q.append(" order by vehicle.device_internal_id,stat desc ");
					System.out.println("VehicleSetup.loadSetup() Query : "+q.toString());
					PreparedStatement ps = dbConn.prepareStatement(q.toString());
					ResultSet rs = ps.executeQuery();
					if (dbgString != null)
						dbgString.append("[Add to]:");
					while (rs.next()) {
						int vehId = rs.getInt(1);
						Integer vehIdInteger = new Integer(vehId);
						VehicleSetup vehInfo = new VehicleSetup();
						vehInfo.m_vehicleId = vehId;
						String td = rs.getString(2);
						if (td == null || td.length() == 0)
							td = "v" + vehId;
						vehInfo.deviceId = td;
						if (dbgString != null) {
							dbgString.append("(").append(vehId).append(",").append(rs.getString(2)).append(")");
						}
						vehInfo.m_accessGroups = getVehicleAccessGroups(vehId, dbConn);
						vehInfo.m_ownerOrgId = rs.getInt(3);
						vehInfo.m_customer_name = rs.getString(4);
						vehInfo.m_name = rs.getString(5);
						String stdName = vehInfo.m_name;
						stdName = standardizeName(vehInfo.m_name);
						String fourDigit = stdName == null || stdName.length() <= 4 ? stdName : stdName.substring(stdName.length()-4, stdName.length());
						if (stdName == null || stdName.length() == 0) {
							fourDigit = stdName = Integer.toString(vehId);
						}
						vehInfo.m_type = rs.getInt(6); // default = 0;
						vehInfo.m_ignoreBefore = Misc.sqlToLong(rs.getTimestamp(7));
						vehInfo.m_ignoreAfter = Misc.sqlToLong(rs.getTimestamp(8));
						vehInfo.hasCummDist = 1 == rs.getInt(9); // null == 0 is fine
						vehInfo.hasSpeed = 1 == rs.getInt(10); // null == 0 is fine
						vehInfo.hasOrientation = 1 == rs.getInt(11); // null == 0 is fine
						vehInfo.base = Misc.getRsetDouble(rs, 12, 0);
						vehInfo.deviceId = rs.getString(2);
						vehInfo.simNumber = rs.getString("sim_number");
						vehInfo.deviceVersion = rs.getInt("device_version");
						
						String commandWord = rs.getString("command_word");
						if (commandWord != null) {
							String token[] = commandWord.split(",");
							for (int i = 0,is = token.length; i < is; i++) {
								vehInfo.commandWords.add(token[i]);
							}
						}
						vehInfo.m_sendToRule = 1 == rs.getInt("do_rule");
						vehInfo.m_sendToTrip = 1 == rs.getInt("do_trip");
						vehInfo.setDropConnectFreq(Misc.getRsetInt(rs, "drop_conn_freq"));
						vehInfo.setRebootFreq(Misc.getRsetInt(rs, "reboot_freq"));
						vehInfo.setWorkArea(Misc.getRsetInt(rs, "work_area"));
						vehInfo.m_backPtsToRetain = rs.getInt("back_points");
						if (vehInfo.m_backPtsToRetain >= 0)
							vehInfo.m_backPtsToRetain = 1;
						else
							vehInfo.m_backPtsToRetain = 0;
						vehInfo.m_manual_adj_tz_min = Misc.getRsetInt(rs, "manual_adj_tz_min");
						vehInfo.lastIndex = Misc.getRsetInt(rs, "last_index");
						String redirectUrl = rs.getString("redirect_url");
						int ioMapSetId = Misc.getRsetInt(rs, "io_set_id");
						vehInfo.ioMapSetId = ioMapSetId;
						vehInfo.redirectUrl = redirectUrl;
						vehInfo.dalaOrIdling = (byte)Misc.getRsetInt(rs, "dala_or_idling",-1);
						vehInfo.strikeOrCycle = (byte)Misc.getRsetInt(rs,"strike_or_cycle",2);
						for (int i = 1; i < 3; i++) {
							String serverIp = Misc.getRsetString(rs, "server_ip_"+i);
							int serverPort = Misc.getRsetInt(rs, "server_port_"+i);
							Pair<String, Integer> pair = new Pair<String, Integer>(serverIp,serverPort);
	
							vehInfo.intServerIpPort.add(pair);	
						}
						vehInfo.getServerIpPort();
						vehInfo.m_status = Misc.getRsetInt(rs, "status");
						vehInfo.m_extended_status = Misc.getRsetInt(rs, "extended_status", vehInfo.m_status);
						vehInfo.m_other_vehicle_id = Misc.getRsetInt(rs, "other_vehicle_id");
						vehInfo.m_tripParamProfileId = Misc.getRsetInt(rs, "tripparam_profile_id");
						vehInfo.alert_mail_id = rs.getString("alert_mail_id");
						vehInfo.alert_phone = rs.getString("alert_phone");
						
						// detailed_status,miscellaneous,fieldone,fieldtwo,fieldthree,fieldfour,fieldfive,fieldsix,fieldseven,fieldeight,fieldnine,fieldten,
						// fieldeleven,fieldtwelve,fieldthirteen,fieldfourteen,work_area
						vehInfo.m_detailed_status = Misc.getRsetInt(rs, "detailed_status");
						/*vehInfo.m_miscellaneous = rs.getString("miscellaneous");
						vehInfo.m_fieldone = rs.getString("fieldone");
						vehInfo.m_fieldtwo = rs.getString("fieldtwo");
						vehInfo.m_fieldthree = rs.getString("fieldthree");
						vehInfo.m_fieldfour = rs.getString("fieldfour");
						vehInfo.m_fieldfive = rs.getString("fieldfive");
						vehInfo.m_fieldsix = rs.getString("fieldsix");
						vehInfo.m_fieldseven = rs.getString("fieldseven");
						vehInfo.m_fieldeight = rs.getString("fieldeight");
						vehInfo.m_fieldnine = rs.getString("fieldnine");
						vehInfo.m_fieldten = rs.getString("fieldten");
						vehInfo.m_fieldeleven = rs.getString("fieldeleven");
						vehInfo.m_fieldtwelve = rs.getString("fieldtwelve");
						vehInfo.m_fieldthirteen = rs.getString("fieldthirteen");
						vehInfo.m_fieldfourteen = rs.getString("fieldfourteen");
						vehInfo.m_work_area = rs.getString("work_area");
						
						// plant,registeration_number,registeration_number_expiry,insurance_number,insurance_number_expiry,permit1_number,
						// permit1_number_expiry,permit1_desc,permit2_number,permit2_number_expiry,permit2_desc
						vehInfo.m_plant = Misc.getRsetInt(rs, "plant");
						vehInfo.m_registeration_number = rs.getString("registeration_number");
						vehInfo.m_registeration_number_expiry = Misc.sqlToLong(rs.getTimestamp("registeration_number_expiry"));
						vehInfo.m_insurance_number = rs.getString("insurance_number");
						vehInfo.m_insurance_number_expiry = Misc.sqlToLong(rs.getTimestamp("insurance_number_expiry"));
						vehInfo.m_permit1_number = rs.getString("permit1_number");
						vehInfo.m_permit1_number_expiry = Misc.sqlToLong(rs.getTimestamp("permit1_number_expiry"));
						vehInfo.m_permit1_desc = rs.getString("permit1_desc");
						vehInfo.m_permit2_number = rs.getString("permit2_number");
						vehInfo.m_permit2_number_expiry = Misc.sqlToLong(rs.getTimestamp("permit2_number_expiry"));
						vehInfo.m_permit2_desc = rs.getString("permit2_desc");*/
						vehInfo.m_flag = Misc.getRsetInt(rs, "flag");
						vehInfo.deviceModelInfoId = Misc.getRsetInt(rs, "device_model_info_id");
						DeviceModel deviceModel = DeviceModel.getDeviceModel(dbConn, vehInfo.deviceModelInfoId);
						vehInfo.cummDistProvided = Misc.getRsetInt(rs, "cumm_dist",deviceModel == null ? 0 : deviceModel.getCummDistProvided());
						vehInfo.cummDistCalcApproach = Misc.getRsetInt(rs, "use_cumm_dist", deviceModel == null ? 0 : deviceModel.getCummDistCalcApproach());
						vehInfo.cummDataToMeter = Misc.getRsetDouble(rs,"cumm_data_to_meter", deviceModel == null ? 0 : deviceModel.getCummDataToMeter());
						vehInfo.distAdjFactor = Misc.getRsetDouble(rs, "dist_adj_factor", deviceModel == null?1:deviceModel.getDistAdjFactor());
						vehInfo.m_strictAckBack = 1 == rs.getInt("strict_ackback");
						vehInfo.m_strictForwarding = 1 == rs.getInt("strict_forwarding");
						vehInfo.m_mdtFlag = 1 == Misc.getRsetInt(rs, "mdt_flag");
						vehInfo.m_subType = Misc.getRsetInt(rs, "sub_type");
						vehInfo.bleTag1 = Misc.getRsetString(rs, "ble_tag1");
						vehInfo.bleTag2 = Misc.getRsetString(rs, "ble_tag2");
						vehInfo.shortCode = Misc.getRsetString(rs,"vehicle_code",null);
						if (deviceModel != null && Misc.isUndef(vehInfo.m_manual_adj_tz_min))
							vehInfo.m_manual_adj_tz_min = deviceModel.getManualAdjTZMin(); 
						VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(dbConn, vehInfo.m_vehicleId, false, false);
						if (vdf != null) {
							//synchronized (vdf) {//leads to deadlock ... on other hand we are sync statically for load setup so only 1 could be updating
							//there might however be partial update of fields while others are using it.
							int cummDistProvided = vehInfo.cummDistProvided;// 0 not there, 1 cumm, 2 delta, 3 canbus
							int cummDistCalcApproach = vehInfo.cummDistCalcApproach;// 0 dont. 1 complex, 2 simple
							boolean cummDistSensorBased = cummDistCalcApproach == 2 || cummDistProvided == 3;
							vdf.updateDeviceIdEtc(dbConn, vehInfo.deviceId, ioMapSetId, redirectUrl, cummDistProvided == 2, cummDistProvided == 3 || cummDistSensorBased);
							//}
						}
						vehInfo.loadDistCalcControl(dbConn);
						if (true) {
							Cache cache = Cache.getCacheInstance(dbConn);
							MiscInner.PortInfo portInfo = cache.getPortInfo(vehInfo.m_ownerOrgId, dbConn);
							ArrayList<Integer> valList = portInfo == null ? null : portInfo.getIntParams(OrgConst.ID_INT_SHOVEL_TYPES, true);
							if(valList != null){
								for (int i=0,is=valList.size(); i<is; i++) {
									if (valList.get(i) == vehInfo.m_type) {
										vehInfo.shovelType = true;
										break;
									}
								}
							}
						}	
						VehicleSetup.helperAddVeh(vehIdInteger, vehInfo, putInThis, currListByVehicleId, dummyPutInThis, dummyIdByName, dummyIdBy4DigitName, dummyByBLE, dbgString);
					}
					rs.close();
					ps.close();
					
					if (true) { // with concurrentHashMap dont have to worry about removal etc ... infact below removal was not even needed
						if (g_setupInfo == null) {
							g_setupInfo = (ConcurrentHashMap<Integer, VehicleSetup>) putInThis;
							g_setupVehcileIdByDeviceId = (ConcurrentHashMap<String, Integer>) dummyPutInThis;
							g_setupVehicleIdByStdName = (ConcurrentHashMap<String, Integer>) dummyIdByName;
							VehicleSetup.g_setupVehicleIdBy4digitStdName = (ConcurrentHashMap<String, Integer>) dummyIdBy4DigitName;
							g_setupVehicleIdByBLETag = (ConcurrentHashMap<String, Integer>) dummyByBLE;
						}
						else {
							VehicleSetup.cleanupSetupMap(currListByVehicleId, putInThis,  dummyPutInThis, dummyIdByName, dummyIdBy4DigitName, dummyByBLE, dbgString, refreshTheseVehicles);
						}
					}
					
					if (dbgString != null)
						System.out.println(dbgString);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		 
		 private static void helperAddVeh(Integer vehicleId, VehicleSetup newStuff, Map<Integer, VehicleSetup> oldL, Map<Integer, Integer> newL
				 ,Map<String, Integer> byDevice, Map<String, Integer> byStdName, Map<String, Integer> by4DigitName, Map<String, Integer> byBLE, StringBuilder traceMsg) {
			 VehicleSetup old = oldL.get(vehicleId);
			 newL.remove(vehicleId);
			 if (traceMsg != null)
				 traceMsg.append("AddCache:").append(vehicleId).append(",");
			 oldL.put(vehicleId, newStuff);
			 String oldDevId = old == null ? null : old.deviceId;
			 String oldStdName = old == null ? null : standardizeName(old.m_name);
			 String old4DigitName = oldStdName == null || oldStdName.length() <= 4 ? oldStdName : oldStdName.substring(oldStdName.length()-4, oldStdName.length());
			 String newDevId = newStuff == null ? null : newStuff.deviceId;
			 String newStdName = newStuff == null ? null : standardizeName(newStuff.m_name);
			 String new4DigitName = newStdName == null || newStdName.length() <= 4 ? newStdName : newStdName.substring(newStdName.length()-4, newStdName.length());
			 
			 if (oldDevId != null && (newDevId == null || !oldDevId.equals(newDevId))) {
				 byDevice.remove(oldDevId);
				 if (traceMsg != null)
					 traceMsg.append("CacheRemDevId:").append(oldDevId).append(",");
			 }
			 String oldTag = null;
			 String newTag = null;
			 oldTag = old == null ? null : old.bleTag1;
			 newTag = newStuff == null ? null : newStuff.bleTag1;
			 if (oldTag != null && (oldTag == null || !oldTag.equals(newTag))) {
				 byBLE.remove(oldTag);
				 if (traceMsg != null)
					 traceMsg.append("CacheRemBLEId1:").append(oldTag).append(",");
			 }
			 oldTag = old == null ? null : old.bleTag2;
			 newTag = newStuff == null ? null : newStuff.bleTag2;
			 if (oldTag != null && (oldTag == null || !oldTag.equals(newTag))) {
				 byBLE.remove(oldTag);
				 if (traceMsg != null)
					 traceMsg.append("CacheRemBLEId2:").append(oldTag).append(",");
			 }
			 
			 if (oldStdName != null && (newStdName == null || !oldStdName.equals(newStdName))) {
				 byStdName.remove(oldStdName);
			 }
			 if (old4DigitName != null && (new4DigitName == null || !old4DigitName.equals(new4DigitName))) {
				 by4DigitName.remove(old4DigitName);
			 }
			 if (newDevId != null && !newDevId.equals(oldDevId)) {
				 if (traceMsg != null)
					 traceMsg.append("CacheAddDevId:").append(oldDevId).append(",").append(vehicleId).append(",");
				 byDevice.put(newDevId, vehicleId);
			 }
			
			 oldTag = old == null ? null : old.bleTag1;
			 newTag = newStuff == null ? null : newStuff.bleTag1;
			 if (newTag != null && !newTag.equals(oldTag)) {
				 if (traceMsg != null)
					 traceMsg.append("CacheAddTagId1:").append(newTag).append(",").append(vehicleId).append(",");
				 byBLE.put(newTag, vehicleId);
			 }
			 oldTag = old == null ? null : old.bleTag2;
			 newTag = newStuff == null ? null : newStuff.bleTag2;
			 if (newTag != null && !newTag.equals(oldTag)) {
				 if (traceMsg != null)
					 traceMsg.append("CacheAddTagId2:").append(newTag).append(",").append(vehicleId).append(",");
				 byBLE.put(newTag, vehicleId);
			 }
			 
			 if (newStdName != null) {
				 Integer existingVehicleId = byStdName.get(newStdName);
				 if (existingVehicleId != null && existingVehicleId.intValue() != vehicleId) {
					 if (newStuff != null && newStuff.m_status == 1)
						 byStdName.put(newStdName, vehicleId);
				 }
				 else {
					 byStdName.put(newStdName, vehicleId);
				 }
			 }
			 if (new4DigitName != null) {
				 Integer existingVehicleId = by4DigitName.get(new4DigitName);
				 if (existingVehicleId != null && existingVehicleId.intValue() != vehicleId) {
					 if (newStuff != null && newStuff.m_status == 1)
						 by4DigitName.put(new4DigitName, vehicleId);
				 }
				 else {
					 by4DigitName.put(new4DigitName, vehicleId);
				 }
			 }
		 }
		private static void helperRemoveVeh(Integer vehicleId, Map<Integer, VehicleSetup> oldL, Map<String, Integer> byDevice, Map<String, Integer> byStdName, Map<String, Integer> by4DigitName, Map<String, Integer> byBLE, StringBuilder traceMsg) {
			VehicleSetup old = oldL.get(vehicleId);
			if (old != null) {
				if (traceMsg != null) {
					 traceMsg.append("RemVehCache:").append(vehicleId).append(",");
				 }
				int vehInt = vehicleId.intValue();
				oldL.remove(vehicleId);
				
				String devId = old.deviceId;
				Integer nv = devId == null ? null : byDevice.get(devId);
				int nvInt = nv == null ? Misc.getUndefInt() : nv.intValue();
				if (nvInt == vehInt) {
					byDevice.remove(devId);
				}
		
				String bleTag = old.bleTag1;
				nv = bleTag == null ? null : byBLE.get(bleTag);
				nvInt = nv == null ? Misc.getUndefInt() : nv.intValue();
				if (nvInt == vehInt) {
					byDevice.remove(bleTag);
				}
				bleTag = old.bleTag2;
				nv = bleTag == null ? null : byBLE.get(bleTag);
				nvInt = nv == null ? Misc.getUndefInt() : nv.intValue();
				if (nvInt == vehInt) {
					byDevice.remove(bleTag);
				}
		
				devId = standardizeName(old.m_name);
				nv = devId == null ? null : byStdName.get(devId);
				nvInt = nv == null ? Misc.getUndefInt() : nv.intValue();
				if (nvInt == vehInt) {
					byStdName.remove(devId);
				}
				devId = devId == null || devId.length() <= 4 ? devId : devId.substring(devId.length()-4, devId.length());
				nv = devId == null ? null : by4DigitName.get(devId);
				nvInt = nv == null ? Misc.getUndefInt() : nv.intValue();
				if (nvInt == vehInt) {
					by4DigitName.remove(devId);
				}
				
			}
		}

		private static void cleanupSetupMap(Map<Integer, Integer> newL, Map<Integer, VehicleSetup> oldL, Map<String, Integer> byDevice, Map<String, Integer> byStdName, Map<String, Integer> by4DigitName, Map<String, Integer>byBLE, StringBuilder traceMsg) {
			 //idea is that we initially remember all in newL... then as updated ones found we remove from newLand finally iterate
			 //through newL and remove from oldL any remaining
			//But when list of vehicles we iterate through list of vehicles, see if it is still there in newL and if so remove from oldL
			 Set<Integer> keys = newL.keySet();
			 for (Integer key : keys) {
				 helperRemoveVeh(key, oldL, byDevice, byStdName, by4DigitName, byBLE, traceMsg);
			 }
		 }
		private static void cleanupSetupMap(Map<Integer, Integer> newL, Map<Integer, VehicleSetup> oldL, Map<String, Integer> byDevice, Map<String, Integer> byStdName, Map<String, Integer> by4DigitName, Map<String, Integer> byBLE, StringBuilder traceMsg, ArrayList<Integer> refreshThese) {
			 //idea is that we initially remember all in newL... then as updated ones found we remove from newLand finally iterate
			 //through newL and remove from oldL any remaining
			//But when list of vehicles we iterate through list of vehicles, see if it is still there in newL and if so remove from oldL
			if (refreshThese == null || refreshThese.size() == 0) {
				cleanupSetupMap(newL, oldL, byDevice, byStdName, by4DigitName, byBLE, traceMsg);
				return;
			}
				
			 for (Integer iv : refreshThese) {
				 if (newL.containsKey(iv)) {
					 helperRemoveVeh(iv, oldL, byDevice, byStdName, by4DigitName, byBLE, traceMsg);
				 }
			 }
		 }
		 
		 private static void rememberSetupMap(Map<Integer, VehicleSetup> oldL, Map<Integer, Integer> newL) {
			 Set<Integer> keys = oldL.keySet();
			 for (Integer key : keys) {
				 newL.put(key, key);
			 }
		 }

		 private static void removeFromHashMap(ArrayList<String> list, Map<String, Integer> dummyPutInThis) {
			if (list == null || dummyPutInThis == null) {
				return;
			}
			for (int i = 0; i < list.size(); i++) {
				dummyPutInThis.remove(list.get(i));
			}

		}

		public static void setForceLookLocation() {
			ConcurrentHashMap<Integer, VehicleSetup> vehSetup = g_setupInfo;
			Connection conn = null;
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				if (g_setupInfo == null) {
					loadSetup(conn, null);
				}
				if (g_setupInfo != null) {
					 vehSetup = g_setupInfo;
					Set<Entry<Integer, VehicleSetup>> s = vehSetup.entrySet();
					Iterator<Entry<Integer, VehicleSetup>> itMap = s.iterator();
					while (itMap.hasNext()) {

						Map.Entry<Integer, VehicleSetup> meMap = (Map.Entry<Integer, VehicleSetup>) itMap.next();
						int key = ((Integer) meMap.getKey()).intValue();
						VehicleSetup vs = g_setupInfo.get(key);
						vs.m_distCalcControl.m_forceLookUpLocation = true;

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						DBConnectionPool.returnConnectionToPoolNonWeb(conn);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		public DistCalcControl loadDistCalcControl(Connection dbConn) throws Exception {
			this.m_distCalcControl = loadDistCalcControl(dbConn, this.m_ownerOrgId);
			return m_distCalcControl;
		}
		public static DistCalcControl loadDistCalcControl(Connection dbConn, int ownerOrgId) throws Exception {
			DistCalcControl distCalcControl = new DistCalcControl();
			Cache cache = Cache.getCacheInstance(dbConn);
			MiscInner.PortInfo portInfo = cache.getPortInfo(ownerOrgId, dbConn);
			
			if (portInfo != null) {
				ArrayList valList;// temp var

				valList = portInfo.getDoubleParams(OrgConst.ID_THRESHOLD_DIST_TO_AVOID_LOOKUP_DBL);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_distThreshSame = ((Double) valList.get(0)).doubleValue();
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_TRIP_DATA_EXCHANGE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_sendTPDataToRP =  ((Integer) valList.get(0)).intValue();

				valList = portInfo.getIntParams(OrgConst.ID_CHECK_USER_DEFINED_LANDMARK_INT);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doCheckUDLandmark = 1 == ((Integer) valList.get(0)).intValue();

				valList = portInfo.getIntParams(OrgConst.ID_CHECK_USER_DEFINED_REGION_INT);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doCheckUDRegionLayer = 1 == ((Integer) valList.get(0)).intValue();

				distCalcControl.m_baseMapLandmarkLayers = portInfo.getStringParams(OrgConst.ID_BASEMAP_LANDMARK_LAYERS_STR);
				distCalcControl.m_baseMapRoadLayers = portInfo.getStringParams(OrgConst.ID_BASEMAP_ROAD_LAYERS_STR);
				distCalcControl.m_baseMapRegionSequenceLayers = portInfo.getStringParams(OrgConst.ID_BASEMAP_REGIONSEQUENCE_LAYERS_STR);
				distCalcControl.m_baseMapGetRoadNameFirst = portInfo.getStringParams(OrgConst.ID_BASEMAP_ROAD_LAYERS_GETFIRST_STR);

				valList = portInfo.getDoubleParams(OrgConst.ID_BASEMAP_ROAD_RANGE_GETFIRST_DBL);
				for (int i = 0, is = valList == null ? 0 : valList.size() > 3 ? 3 : valList.size(); i < is; i++) {
					distCalcControl.m_roadFirstThresholdKM[i] = ((Double) valList.get(i)).doubleValue();
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_USER_DEFINED_DIST_RANGE_DBL);
				for (int i = 0, is = valList == null ? 0 : valList.size() > 3 ? 3 : valList.size(); i < is; i++) {
					distCalcControl.m_UDDistThresholdKM[i] = ((Double) valList.get(i)).doubleValue();
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_BASEMAP_DIST_RANGE_DBL);
				for (int i = 0, is = valList == null ? 0 : valList.size() > 3 ? 3 : valList.size(); i < is; i++) {
					distCalcControl.m_baseDistThresholdKM[i] = ((Double) valList.get(i)).doubleValue();
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_BASEMAP_ROAD_RANGE_DBL);
				for (int i = 0, is = valList == null ? 0 : valList.size() > 3 ? 3 : valList.size(); i < is; i++) {
					distCalcControl.m_roadDistThresholdKM[i] = ((Double) valList.get(i)).doubleValue();
				}
				valList = portInfo.getDoubleParams(OrgConst.ID_SHOW_DIST_IF_GREATER);
				if (valList != null && valList.size() != 0) {
					distCalcControl.m_tellDistanceIfGreaterKM = ((Double) valList.get(0)).doubleValue();
				}
				valList = portInfo.getDoubleParams(OrgConst.ID_SHOW_DISTRICT_STATE_NAMES_IF_DIST_GRT_THAN);
				if (valList != null && valList.size() != 0) {
					distCalcControl.m_tellDistrictStateInfoIfDistGreaterThanKM = ((Double) valList.get(0)).doubleValue();
				}

				valList = portInfo.getStringParams(OrgConst.ID_DIST_FORMAT_STR);
				if (valList != null && valList.size() != 0) {
					distCalcControl.m_distanceFormat = ((String) valList.get(0));
					distCalcControl.m_distanceFormatter = new java.text.DecimalFormat(distCalcControl.m_distanceFormat);
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_STOP_DIST_MARGIN);
				if (valList != null && valList.size() != 0) {
					Double dd = ((Double) valList.get(0)).doubleValue();
					double dv = dd == null ? Misc.getUndefDouble() : dd.doubleValue();
					if (!Misc.isUndef(dv))
						distCalcControl.m_distThresholdForStopped = dv;
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_RETAIN_DIST_MARGIN);
				if (valList != null && valList.size() != 0) {
					Double dd = ((Double) valList.get(0)).doubleValue();
					double dv = dd == null ? Misc.getUndefDouble() : dd.doubleValue();
					if (!Misc.isUndef(dv))
						distCalcControl.m_distThresholdForRetainingData = dv;
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_LINKED_VEHICLE_GATEWAIT_AREA_BOX_MTR);
				if (valList != null && valList.size() != 0) {
					Double dd = ((Double) valList.get(0)).doubleValue();
					double dv = dd == null ? Misc.getUndefDouble() : dd.doubleValue();
					if (!Misc.isUndef(dv))
						distCalcControl.m_linkedGateWaitBoxMtr = dv;
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_LINKED_VEHICLE_OPAREA_BOX_MTR);
				if (valList != null && valList.size() != 0) {
					Double dd = ((Double) valList.get(0)).doubleValue();
					double dv = dd == null ? Misc.getUndefDouble() : dd.doubleValue();
					if (!Misc.isUndef(dv))
						distCalcControl.m_linkedOpAreaBoxMtr = dv;
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_LINKED_VEHICLE_SHIFT_DIST_EXCEEDS_MTR);
				if (valList != null && valList.size() != 0) {
					Double dd = ((Double) valList.get(0)).doubleValue();
					double dv = dd == null ? Misc.getUndefDouble() : dd.doubleValue();
					if (!Misc.isUndef(dv))
						distCalcControl.m_linkedShiftDistExceedsMtr = dv;
				}
				valList = portInfo.getDoubleParams(OrgConst.ID_RECALC_ORIENTATION_IF_DIST_EXCEEDS_MTR);
				if (valList != null && valList.size() != 0) {
					Double dd = ((Double) valList.get(0)).doubleValue();
					double dv = dd == null ? Misc.getUndefDouble() : dd.doubleValue();
					if (!Misc.isUndef(dv))
						distCalcControl.m_orientationCalcThreshold = dv;
				}

				valList = portInfo.getDoubleParams(OrgConst.ID_TRIP_PROCESS_IF_DIST_EXCEEDS_DBL);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_processTripIfDistExceedsMtr = ((Double) valList.get(0)).doubleValue();
				valList = portInfo.getDoubleParams(OrgConst.ID_TRIP_MOVING_IF_SPEED_EXCEEDS_DBL);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_tripMovingIfSpeedExceedsKMPH = ((Double) valList.get(0)).doubleValue();
				valList = portInfo.getDoubleParams(OrgConst.ID_TRIP_STOPPED_IF_SPEED_LESS_DBL);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_tripStoppedIfSpeedLessKMPH = ((Double) valList.get(0)).doubleValue();
				valList = portInfo.getIntParams(OrgConst.ID_TO_SEND_TRIP_MESSAGE_INT);
				if (valList != null && valList.size() > 0)
					distCalcControl.toSendMessage = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DO_EXTREMUM_VALID_LU);
				if (valList != null && valList.size() > 0)
					distCalcControl.bestLUisValidExtremum = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DO_TRACK_MULTI_LU);
				if (valList != null && valList.size() > 0)
					distCalcControl.toTrackIntermediateLU = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DO_LOAD_FIRST);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_bestLoadAreaIsFirst = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DO_LOAD_LAST);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_bestLoadAreaIsLast = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DO_UNLOAD_FIRST);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_bestUnloadAreaIsFirst = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_INT_MRS_STYLE_HYBRID_ALWAY);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doMRSstyleForHybridLUAlway = 1 == ((Integer) valList.get(0)).intValue();
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_SCAN);
				if (valList != null && valList.size() > 0)
					distCalcControl.doScanProcessing =  ((Integer) valList.get(0)).intValue();
				
				valList = portInfo.getIntParams(OrgConst.ID_DO_UNLOAD_LAST);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_bestUnloadAreaIsFirst = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DO_LU_MUST_BE_PROPER);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_luMustBeProper = 1 == ((Integer) valList.get(0)).intValue();

				valList = portInfo.getIntParams(OrgConst.ID_MARK_LOAD_WAIT_IN_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markLoadWaitInAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_LOAD_GATE_IN_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markLoadGateInAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_LOAD_AREA_IN_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markLoadWaitInAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_LOAD_AREA_OUT_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markLoadAreaOutAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_LOAD_GATE_OUT_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markLoadGateOutAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_LOAD_WAIT_OUT_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markLoadWaitOutAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();

				valList = portInfo.getIntParams(OrgConst.ID_MARK_UNLOAD_WAIT_IN_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markUnloadWaitInAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_UNLOAD_GATE_IN_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markUnloadGateInAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_UNLOAD_AREA_IN_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markUnloadWaitInAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_UNLOAD_AREA_OUT_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markUnloadAreaOutAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_UNLOAD_GATE_OUT_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markUnloadGateOutAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_MARK_UNLOAD_WAIT_OUT_SHIFT_DATE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_markUnloadWaitOutAsShiftDate = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_LOOK_FOR_CHALLAN);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_lookForChallan = ((Integer) valList.get(0)).intValue();
				
				valList = portInfo.getIntParams(OrgConst.ID_MATERIAL_LOOKUP_APPROACH);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_materialLookUpApproach =  ((Integer) valList.get(0)).intValue();
				
				valList = portInfo.getIntParams(OrgConst.ID_SHOW_BASE_MAP_DETAILS);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_showBaseMapDetails = 1 == ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_DEVICE_MESSAGE_REMOVE_APPROACH);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_todeviceMessageRemoval =  ((Integer) valList.get(0)).intValue();
				
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_MERGE_CONSECUTIVE_LLUU);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doMergeConsecutiveLLUU=  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_SHORT_TRIP_POOR_NW);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doShortTripPoorNW =  ((Integer) valList.get(0)).intValue() != 0;

				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_STOP_MARKER);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doStopMarker =  ((Integer) valList.get(0)).intValue() != 0;

				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_PREV_STATE_FROM_LUSEQ);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doPrevStateFromLUSequence =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_PICK_FURTHEST_UNLOAD_OPSTATION);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_pickFurthestUnloadOpstation =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_GETBEST_AMONGS_SAME_OPID_MULTILU);
				if (valList != null && valList.size() > 0)
					distCalcControl.getBestAmongstSameOpIdMultiLU =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_MULTILOAD_MULTILU);
				if (valList != null && valList.size() > 0)
					distCalcControl.doMultiLoadMultiLU =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DO_MULTIUNLOAD_MULTILU);
				if (valList != null && valList.size() > 0)
					distCalcControl.doMultiUnloadMultiLU =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_INPRELOAD_PROCESSING_PREISINSIDE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_PreLoadProce_PreIsIn =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_TOGOUP_ORG_FOR_OPSTATION);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_togupOrgforOpStation =  ((Integer) valList.get(0)).intValue() != 0;
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_AFTER_UNLOAD_GEN_INSTR);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_afterUnloadAuto =  ((Integer) valList.get(0)).intValue();
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_DOETA);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doETA = 1 == ( ((Integer) valList.get(0)).intValue());
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_MIN_GPS_SPEED_CALC_SPEED);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doMinOfGpsSpeedAndCalcSpeed = 1 == ( ((Integer) valList.get(0)).intValue());
				
				valList = portInfo.getIntParams(OrgConst.ID_INT_FOR_LOAD_ONLY);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doForOnlyLoad = 1 == ( ((Integer) valList.get(0)).intValue());

				valList = portInfo.getIntParams(OrgConst.ID_INT_TRIP_CALC_NEW_APPROACH);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_doNewApproachForTrip = 1 == ( ((Integer) valList.get(0)).intValue());

				valList = portInfo.getIntParams(OrgConst.ID_INT_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_getNearestOpForStop = ( ((Integer) valList.get(0)).intValue());

				valList = portInfo.getIntParams(OrgConst.ID_INT_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_getNearstSpecialLM = ( ((Integer) valList.get(0)).intValue());

				valList = portInfo.getDoubleParams(OrgConst.ID_DOUBLE_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE_MAXDIST);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_threshKMForNearestOp = ( ((Double) valList.get(0)).doubleValue());

				valList = portInfo.getDoubleParams(OrgConst.ID_DOUBLE_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE_MAXDIST);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_threshKMForNearestLM = ( ((Double) valList.get(0)).doubleValue());
				
				valList = portInfo.getIntParams(OrgConst.ID_STOPDIR_INT_USECHALLAN_FOR_LUBREAK_ID);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_useChallanInPreferredLU = 0 != ( ((Integer) valList.get(0)).intValue());
				
				valList = portInfo.getIntParams(OrgConst.ID_STOPDIR_INT_USEPREVLU_FOR_LUBREAK_ID);
				if (valList != null && valList.size() > 0)
					distCalcControl.m_usePrevLUInPreferredLU = 0 != ( ((Integer) valList.get(0)).intValue());

				valList = portInfo.getIntParams(OrgConst.ID_DO_ETA_PROC_NEW);
				if (valList != null && valList.size() > 0)
					distCalcControl.doETAProcesingNew = ((Integer) valList.get(0)).intValue();
				valList = portInfo.getIntParams(OrgConst.ID_INT_TELTONIKA_GAP);
				if (valList != null && valList.size() > 0)
					distCalcControl.teltonikaSkipSec = ((Integer) valList.get(0)).intValue();
								
				int immPortWithMining = Misc.getUndefInt(); //basically get port with this param set 
				for (MiscInner.PortInfo curr = portInfo; curr != null; curr = curr.m_parent) {
					ArrayList templ = curr.m_orgFlexParams == null ? null :  curr.m_orgFlexParams.getIntParams(OrgConst.ID_DO_MININIG_UNIT_PROCESSING);
					if (templ != null && templ.size() > 0) {
						if (((Integer) templ.get(0)).intValue() == 1) {
							immPortWithMining = curr.m_id;
						}
						break;
					}
				}
				distCalcControl.managementUnitPortNodeId = immPortWithMining;
			}
			return distCalcControl;
		}

		/**
		 * @param hasCummDist
		 *            the hasCummDist to set
		 */
		public void setHasCummDist(boolean hasCummDist) {
			this.hasCummDist = hasCummDist;
		}

		/**
		 * @return the hasCummDist
		 */
		public boolean isHasCummDist() {
			return hasCummDist;
		}

		/**
		 * @param hasSpeed
		 *            the hasSpeed to set
		 */
		public void setHasSpeed(boolean hasSpeed) {
			this.hasSpeed = hasSpeed;
		}

		/**
		 * @return the hasSpeed
		 */
		public boolean isHasSpeed() {
			return hasSpeed;
		}

		/**
		 * @param hasOrientation
		 *            the hasOrientation to set
		 */
		public void setHasOrientation(boolean hasOrientation) {
			this.hasOrientation = hasOrientation;
		}

		/**
		 * @return the hasOrientation
		 */
		public boolean isHasOrientation() {
			return hasOrientation;
		}

		/**
		 * @param distUnit
		 *            the distUnit to set
		 */

		/**
		 * @param base
		 *            the base to set
		 */
		public void setBase(double base) {
			this.base = base;
		}

		/**
		 * @return the base
		 */
		public double getBase() {
			return base;
		}

		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}

		public String getSimNumber() {
			return simNumber;
		}

		public void setSimNumber(String simNumber) {
			this.simNumber = simNumber;
		}

		public int getDeviceVersion() {
			return deviceVersion;
		}

		public void setDeviceVersion(int deviceVersion) {
			this.deviceVersion = deviceVersion;
		}
		public ArrayList<String> getCommandWords() {
			return commandWords;
		}

		public int getRebootFreq() {
			return rebootFreq;
		}

		public void setRebootFreq(int rebootFreq) {
			this.rebootFreq = rebootFreq;
		}

		public int getDropConnectFreq() {
			return dropConnectFreq;
		}

		public void setDropConnectFreq(int dropConnectFreq) {
			this.dropConnectFreq = dropConnectFreq;
		}

		public int getWorkArea() {
			return workArea;
		}

		public void setWorkArea(int workArea) {
			this.workArea = workArea;
		}

		public int getLastIndex() {
			return lastIndex;
		}

		public void setLastIndex(int lastIndex) {
			this.lastIndex = lastIndex;
		}

		public long getLastGpsRecordTime() {
			return lastGpsRecordTime;
		}

		public void setLastGpsRecordTime(long lastGpsRecordTime) {
			this.lastGpsRecordTime = lastGpsRecordTime;
		}

		public long getLastGpsReceiveTime() {
			return lastGpsReceiveTime;
		}

		public void setLastGpsReceiveTime(long lastGpsReceiveTime) {
			this.lastGpsReceiveTime = lastGpsReceiveTime;
		}

		public ArrayList<Pair<String, Integer>> getServerIpPort() {
			if (serverIpPort == null) {
				serverIpPort = new ArrayList<Pair<String, Integer>>();
				ArrayList<Pair<String,Integer>> serverInfo =intServerIpPort;
				boolean saveToDBIfNeeded = m_strictForwarding;
				boolean waitForAck = m_strictAckBack;
				for (int i = 0; i < serverInfo.size(); i++) {
					Pair<String,Integer> pair = serverInfo.get(i);
					if (pair.first!= null && pair.first.length() > 7 && pair.second != Misc.UNDEF_VALUE ) {
						if (!Misc.isSameIpAsMe(pair.first)) {
							serverIpPort.add(pair);
						}
					}
				}
			}
			return serverIpPort;
		}

		public Pair<Date, Date> getLastGpsDataProcessTime() {
			return lastGpsDataProcessTime;
		}

		public void setLastGpsDataProcessTime(Pair<Date, Date> lastGpsDataProcessTime) {
			this.lastGpsDataProcessTime = lastGpsDataProcessTime;
		}

		public int getIoMapSetId() {
			return ioMapSetId;
		}

		public void setIoMapSetId(int ioMapSetId) {
			this.ioMapSetId = ioMapSetId;
		}

		public String getRedirectUrl() {
			return redirectUrl;
		}

		public void setRedirectUrl(String redirectUrl) {
			this.redirectUrl = redirectUrl;
		}
		public boolean isShovelType() {
			return shovelType;
		}
		public void setShovelType(boolean shovelType) {
			this.shovelType = shovelType;
		}

		 
 
		
	}

	public static void refreshVehicles(ArrayList<Integer> vehicleIDs ,String severName) throws Exception { // called from outside so has its own conn
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ArrayList<Pair<Integer, String>> dbgDeviceIds = null;
			boolean doDBG = true;
			if (doDBG) {
				dbgDeviceIds = new ArrayList<Pair<Integer, String>>();
				StringBuilder tempQ = new StringBuilder("select id, device_internal_id from vehicle where id in (");
				StringBuilder vehIdList = new StringBuilder("$$$ [CTRefresh] Doing refresh of vehicles in CacheTrack: ");
				for (int i = 0, is = vehicleIDs == null ? 0 : vehicleIDs.size(); i < is; i++) {
					vehIdList.append(vehicleIDs.get(i)).append(",");
					if (i != 0)
						tempQ.append(",");
					tempQ.append(vehicleIDs.get(i));
				}
				tempQ.append(")");
				PreparedStatement ps1 = conn.prepareStatement(tempQ.toString());
				ResultSet rs1 = ps1.executeQuery();
				while (rs1.next()) {
					dbgDeviceIds.add(new Pair<Integer, String>(rs1.getInt(1), rs1.getString(2)));
				}
				rs1.close();
				ps1.close();
				System.out.println(vehIdList);
				System.out.println("CacheTrack.refreshVehicles() Has Been Called From Server ="+severName);
			}
			CacheTrack.VehicleSetup.loadSetup(conn, vehicleIDs, vehicleIDs != null && vehicleIDs.size() != 0 ? false : true);
			if (doDBG) {
				StringBuilder vehIdList = new StringBuilder("$$$ [CTRefresh] After refresh of vehicles in CacheTrack : ");
				for (int i = 0, is = dbgDeviceIds == null ? 0 : dbgDeviceIds.size(); i < is; i++) {
					int id = dbgDeviceIds.get(i).first;
					String dev = dbgDeviceIds.get(i).second;
					VehicleSetup setup = CacheTrack.VehicleSetup.getSetup(id, conn);
					int correspId = CacheTrack.VehicleSetup.getSetup(dev, conn);
					if (id != correspId || setup == null) {
						vehIdList.append("Missing:").append(id).append(" ").append(setup).append("  ");
					}
				}
				System.out.println(vehIdList);
				System.out.println("CacheTrack.refreshVehicles() Has Been Called From Server ="+severName);
			}
		} catch (Exception e) {
			destroyIt = true;
			throw e;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
				// eat it
			}
		}
	}

	public static String getAdvancedComboName(String locNameFromRoadFirst, double locDistance, double locAzimuth, String roadName, StringBuilder inName,
			VehicleSetup.DistCalcControl distCalcControl) {
		StringBuilder retval = new StringBuilder();
		{
			if (locNameFromRoadFirst != null) {
				retval.append(locNameFromRoadFirst);
			}

			if (locDistance > distCalcControl.m_tellDistanceIfGreaterKM) {
				retval.append(",").append(distCalcControl.m_distanceFormatter.format(locDistance)).append("KM ").append(TrackMisc.getStringForAzimuth(locAzimuth));
			}
			if (roadName != null && roadName.length() != 0) {
				retval.append(" on ").append(roadName);
			}
		}
		if (inName != null && inName.length() != 0) {
			retval.append(" in ").append(inName);
		}
		return retval.toString();
	}

	public static String getComboName(String locNameFromRoadFirst, String locName, double locDistance, double locAzimuth, String roadName, StringBuilder inName,
			VehicleSetup.DistCalcControl distCalcControl) {
		StringBuilder retval = new StringBuilder();
		if (locNameFromRoadFirst != null) {
			retval.append(locNameFromRoadFirst);
		} else {
			if (locName == null) {
				retval.append("Unknown");
			} else {
				retval.append(locName);
			}
			if (locDistance > distCalcControl.m_tellDistanceIfGreaterKM) {
				retval.append(",").append(distCalcControl.m_distanceFormatter.format(locDistance)).append("KM ").append(TrackMisc.getStringForAzimuth(locAzimuth));
			}
			if (roadName != null && roadName.length() != 0) {
				retval.append(" on ").append(roadName);
			}
		}
		if (inName != null && inName.length() != 0) {
			retval.append(" in ").append(inName);
		}
		return retval.toString();
	}
}
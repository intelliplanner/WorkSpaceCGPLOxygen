package com.ipssi.modeler;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

//DEBUG13 import com.ipssi.cache.NewVehicleData;
//DEBUG13 import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.Dimension;
import com.ipssi.processor.utils.GpsData;

public class VehicleModelInfo {
	public static int g_medianWindow = 9;//HACK ... model specific medianwindow is set to this ... thus only one median window supported
	public static int g_maxMedianWindow = 1001;
	public static double g_maxMedianDistanceKM = 40;//3 for rewa, 40 for bhushan/cars
	public static boolean g_changeJumpValueIfAllChanges = false; //for large median window make it false else true
	public static boolean g_tryToDoForwardMedian = false; //true doesn't work currently
	public static class Data extends FastList<GpsData> {
//		private Date dcSaveGpsTime; 
//		private Date ccSaveGpsTime; 
//		private Date dcGpsTime;
//		private Date ccGpsTime;
//		private Date latestRecvdTime;
		private long dcSaveGpsTime = Misc.getUndefInt(); 
		private long ccSaveGpsTime = Misc.getUndefInt(); 
		private long dcGpsTime = Misc.getUndefInt();
		private long ccGpsTime = Misc.getUndefInt();
		private long latestRecvdTime = Misc.getUndefInt();
		private int dcPos = -1; // In Simple approach ... this is intermediate .... else dc points are synced to this index (incl)
	    
		//for stats ... ultimately we really need ArrayList<ModelStats> ... but feeling too lazy to change code
		//CENT_CACHE_DOWNGRADE public ArrayList<ArrayList<ModelStats.StatsItem>> stats = null; //will be initialized on as needed basis
	    //CENT_CACHE_DOWNGRADE public ModelStats.StatsItem statRecoveryToSameVal = null;
	    
	    //for event detection ... TBD
	    public int ongoingStopDurSec = -1;
	    public boolean seenNonViolation = false;
	    public int prevDCPos = -1;
		public GpsData remove(int index) {
			GpsData pt = get(index);
			super.remove(index);
			if (dcPos >= index)
				dcPos--;
			return pt;
		}
		public Pair<Integer, Boolean> validateUpdateGpsTimeAndGetPosToAdd(GpsData data, int vehicleId, Connection conn) throws Exception {
//			Date gpsTime = data.getGps_Record_Time();
//			Date recvTime = data.getGpsRecvTime();
			long gpsTime = data.getGps_Record_Time();
			long recvTime = data.getGpsRecvTime();
			ChannelTypeEnum chType = data.getSourceChannel();
			boolean isDC = ChannelTypeEnum.isDataChannel(chType);
			boolean isCC = ChannelTypeEnum.isCurrentChannel(chType);
			CacheTrack.VehicleSetup vsetup = CacheTrack.VehicleSetup.getSetup(vehicleId,conn);
			long adjGpsTime = gpsTime;
			if (vsetup != null && vsetup.m_backPtsToRetain > 0) {
				//minute = point
				adjGpsTime += vsetup.m_backPtsToRetain*60*1000;
			}
			if (isDC && !Misc.isUndef(dcGpsTime) && dcGpsTime > adjGpsTime) {
				return null;
			}
			if (isCC && !Misc.isUndef(ccGpsTime) && ccGpsTime > adjGpsTime) {
				return null;
			}
			if (isDC)
				dcGpsTime = gpsTime;
			if (isCC)
				ccGpsTime = gpsTime;
			if (!Misc.isUndef(latestRecvdTime) || latestRecvdTime < data.getGpsRecvTime())
					latestRecvdTime = data.getGpsRecvTime();
			Pair<Integer, Boolean> retval = super.indexOf(data);
			return retval;
		}
		
		public long getDcSaveGpsTime() {
			return dcSaveGpsTime;
		}
		public void setDcSaveGpsTime(long dcSaveGpsTime) {
			this.dcSaveGpsTime = dcSaveGpsTime;
		}
		public long getCcSaveGpsTime() {
			return ccSaveGpsTime;
		}
		public void setCcSaveGpsTime(long ccSaveGpsTime) {
			this.ccSaveGpsTime = ccSaveGpsTime;
		}
		public long getDcGpsTime() {
			return dcGpsTime;
		}
		public void setDcGpsTime(long dcGpsTime) {
			this.dcGpsTime = dcGpsTime;
		}
		public long getCcGpsTime() {
			return ccGpsTime;
		}
		public void setCcGpsTime(long ccGpsTime) {
			this.ccGpsTime = ccGpsTime;
		}
		public int getDcPos() {
			return dcPos;
		}
		public void setDcPos(int dcPos) {
			this.dcPos = dcPos;
		}
	}
    private int vehicleId;
    private boolean cacheRebuilt = false;
    public int m_medianWindow = g_medianWindow;
    public int m_maxMedianWindow = g_maxMedianWindow;
    public double m_maxMedianDistanceKM = VehicleModelInfo.g_maxMedianDistanceKM;
    public double[] medianWork1 = new double[m_maxMedianWindow];
    public GpsData[] medianPoints = new GpsData[m_maxMedianWindow];
    public void setMedianWindow(int medianWindow, int maxMedianWindow, double maxDistanceKM) {
    	if (!Misc.isUndef(medianWindow))
    		m_medianWindow = medianWindow;
    	if (!Misc.isUndef(maxMedianWindow))
    		m_maxMedianWindow = maxMedianWindow;
    	if (!Misc.isUndef(maxDistanceKM))
    		m_maxMedianDistanceKM = maxDistanceKM;
    	if (!Misc.isUndef(maxMedianWindow)) {
	    	medianWork1 = new double[m_maxMedianWindow];
	    	medianPoints = new GpsData[m_maxMedianWindow];
    	}
    }
    
    public SSpList startStopList = new SSpList(); //for fuel to help with start/stop 
    public SSpList ignOffStartStopList = new SSpList();
    public transient int tempLookupIndexInIgnOffStartStopList = -1;
    private void helpMedianWorkSort(double medianWork[], int i, int j) {
    	if (medianWork[i] > medianWork[j]) {
    		double t = medianWork[i];
    		medianWork[i] = medianWork[j];
    		medianWork[j] = t;
    	}
    }
    
    //public double getMedianFromWork() {
    //	return getMedianFromWork(medianWork1, m_medianWindow);
    //}
    public static void fillWithLarge(double medianWork[], int medianWindowSz) {
    	for (int i=medianWindowSz,is=medianWork.length;i<is;i++) {
    		medianWork[i] = Misc.LARGE_NUMBER;
    	}
    }
    public double getMedianFromWork(double medianWork[], int medianWindowSz) {
    	boolean doMoving = true; //true seems better for fluctuating stuff
    	if (doMoving) {
    		double s = 0;
    		for (int i=0,is=medianWindowSz;i<is;i++) {
    			s += medianWork[i];
    		}
    		return s/(double)medianWindowSz;
    	}
    	int percentile = 50;
    	if (percentile != 50) {
    		fillWithLarge(medianWork, medianWindowSz);
    		Arrays.sort(medianWork);
    		int pos = (percentile*medianWindowSz)/100;
    		return medianWork[pos];
    	}
    	if (medianWindowSz == 1) {
    		return medianWork[0];
    	}
    	else if (medianWindowSz == 3) {
	    	helpMedianWorkSort(medianWork,0,1);
	    	helpMedianWorkSort(medianWork,1,2);
	    	helpMedianWorkSort(medianWork,0,1);
	    	return medianWork[1];
    	}
    	else if (medianWindowSz == 5) {
	    	helpMedianWorkSort(medianWork,0,1);
	    	helpMedianWorkSort(medianWork,3,4);
	    	helpMedianWorkSort(medianWork,0,3);
	    	helpMedianWorkSort(medianWork,1,4);
	    	helpMedianWorkSort(medianWork,1,2);
	    	helpMedianWorkSort(medianWork,2,3);
	    	helpMedianWorkSort(medianWork,1,2);
	    	return medianWork[2];
    	}
    	else if (medianWindowSz == 7) {
	    	helpMedianWorkSort(medianWork,0,5);
	    	helpMedianWorkSort(medianWork,0,3);
	    	helpMedianWorkSort(medianWork,1,6);
	    	helpMedianWorkSort(medianWork,2,4);
	    	helpMedianWorkSort(medianWork,0,1);
	    	helpMedianWorkSort(medianWork,3,5);
	    	helpMedianWorkSort(medianWork,2,6);
	    	helpMedianWorkSort(medianWork,2,3);
	    	helpMedianWorkSort(medianWork,3,6);
	    	helpMedianWorkSort(medianWork,4,5);
	    	helpMedianWorkSort(medianWork,1,4);
	    	helpMedianWorkSort(medianWork,1,3);
	    	helpMedianWorkSort(medianWork,3,4);	
	    	return medianWork[3];
    	}
    	else if (medianWindowSz == 9) {
    		helpMedianWorkSort(medianWork,1,2);
    		helpMedianWorkSort(medianWork,4,5);
    		helpMedianWorkSort(medianWork,7,8);
    		helpMedianWorkSort(medianWork,0,1);
    		helpMedianWorkSort(medianWork,3,4);
    		helpMedianWorkSort(medianWork,6,7);
    		helpMedianWorkSort(medianWork,1,2);
    		helpMedianWorkSort(medianWork,4,5);
    		helpMedianWorkSort(medianWork,7,8);
    		helpMedianWorkSort(medianWork,0,3);
    		helpMedianWorkSort(medianWork,5,8);
    		helpMedianWorkSort(medianWork,4,7);
    		helpMedianWorkSort(medianWork,3,6);
    		helpMedianWorkSort(medianWork,1,4);
    		helpMedianWorkSort(medianWork,2,5);
    		helpMedianWorkSort(medianWork,4,7);
    		helpMedianWorkSort(medianWork,4,2);
    		helpMedianWorkSort(medianWork,6,4);
    		helpMedianWorkSort(medianWork,4,2);
    		return medianWork[4];
    	}
    	else  if (medianWindowSz == 25) {
    		helpMedianWorkSort(medianWork,0,1); helpMedianWorkSort(medianWork,3,4); helpMedianWorkSort(medianWork,2,4);
    		helpMedianWorkSort(medianWork,2,3); helpMedianWorkSort(medianWork,6,7); helpMedianWorkSort(medianWork,5,7);
    		helpMedianWorkSort(medianWork,5,6); helpMedianWorkSort(medianWork,9,10); helpMedianWorkSort(medianWork,8,10);
    		helpMedianWorkSort(medianWork,8,9); helpMedianWorkSort(medianWork,12,13); helpMedianWorkSort(medianWork,11,13);
    		helpMedianWorkSort(medianWork,11,12); helpMedianWorkSort(medianWork,15,16); helpMedianWorkSort(medianWork,14,16);
    		helpMedianWorkSort(medianWork,14,15); helpMedianWorkSort(medianWork,18,19); helpMedianWorkSort(medianWork,17,19);
    		helpMedianWorkSort(medianWork,17,18); helpMedianWorkSort(medianWork,21,22); helpMedianWorkSort(medianWork,20,22);
    		helpMedianWorkSort(medianWork,20,21); helpMedianWorkSort(medianWork,23,24); helpMedianWorkSort(medianWork,2,5);
    		helpMedianWorkSort(medianWork,3,6); helpMedianWorkSort(medianWork,0,6); helpMedianWorkSort(medianWork,0,3);
    		helpMedianWorkSort(medianWork,4,7); helpMedianWorkSort(medianWork,1,7); helpMedianWorkSort(medianWork,1,4);
    		
    		helpMedianWorkSort(medianWork,11,14); helpMedianWorkSort(medianWork,8,14); helpMedianWorkSort(medianWork,8,11);
    		helpMedianWorkSort(medianWork,12,15); helpMedianWorkSort(medianWork,9,15); helpMedianWorkSort(medianWork,9,12);
    		helpMedianWorkSort(medianWork,13,16); helpMedianWorkSort(medianWork,10,16); helpMedianWorkSort(medianWork,10,13);
    		helpMedianWorkSort(medianWork,20,23); helpMedianWorkSort(medianWork,17,23); helpMedianWorkSort(medianWork,17,20);
    		helpMedianWorkSort(medianWork,21,24); helpMedianWorkSort(medianWork,18,24); helpMedianWorkSort(medianWork,18,21);
    		helpMedianWorkSort(medianWork,19,22); helpMedianWorkSort(medianWork,8,17); helpMedianWorkSort(medianWork,9,18);
    		helpMedianWorkSort(medianWork,0,18); helpMedianWorkSort(medianWork,0,9); helpMedianWorkSort(medianWork,10,19);
    		helpMedianWorkSort(medianWork,1,19); helpMedianWorkSort(medianWork,1,10); helpMedianWorkSort(medianWork,11,20);
    		helpMedianWorkSort(medianWork,2,20); helpMedianWorkSort(medianWork,2,11); helpMedianWorkSort(medianWork,12,21);
    		helpMedianWorkSort(medianWork,3,21); helpMedianWorkSort(medianWork,3,12); helpMedianWorkSort(medianWork,13,22);
    		helpMedianWorkSort(medianWork,4,22); helpMedianWorkSort(medianWork,4,13); helpMedianWorkSort(medianWork,14,23);
    		helpMedianWorkSort(medianWork,5,23); helpMedianWorkSort(medianWork,5,14); helpMedianWorkSort(medianWork,15,24);
    		helpMedianWorkSort(medianWork,6,24); helpMedianWorkSort(medianWork,6,15); helpMedianWorkSort(medianWork,7,16);
    		helpMedianWorkSort(medianWork,7,19); helpMedianWorkSort(medianWork,13,21); helpMedianWorkSort(medianWork,15,23);
    		helpMedianWorkSort(medianWork,7,13); helpMedianWorkSort(medianWork,7,15); helpMedianWorkSort(medianWork,1,9);
    		helpMedianWorkSort(medianWork,3,11); helpMedianWorkSort(medianWork,5,17); helpMedianWorkSort(medianWork,11,17);
    		helpMedianWorkSort(medianWork,9,17); helpMedianWorkSort(medianWork,4,10); helpMedianWorkSort(medianWork,6,12);
    		helpMedianWorkSort(medianWork,7,14); helpMedianWorkSort(medianWork,4,6); helpMedianWorkSort(medianWork,4,7);
    		helpMedianWorkSort(medianWork,12,14); helpMedianWorkSort(medianWork,10,14); helpMedianWorkSort(medianWork,6,7);
    		helpMedianWorkSort(medianWork,10,12); helpMedianWorkSort(medianWork,6,10); helpMedianWorkSort(medianWork,6,17);
    		helpMedianWorkSort(medianWork,12,17); helpMedianWorkSort(medianWork,7,17); helpMedianWorkSort(medianWork,7,10);
    		helpMedianWorkSort(medianWork,12,18); helpMedianWorkSort(medianWork,7,12); helpMedianWorkSort(medianWork,10,18);
    		helpMedianWorkSort(medianWork,12,20); helpMedianWorkSort(medianWork,10,20); helpMedianWorkSort(medianWork,10,12);
    		return medianWork[12];


    		
    	}
    	else {
    		fillWithLarge(medianWork, medianWindowSz);
    		Arrays.sort(medianWork);
    		return medianWork[medianWindowSz/2];
    	}
    }
    
	public boolean isCacheRebuilt() {
		return cacheRebuilt;
	}
	public void setCacheRebuilt() {
		cacheRebuilt = true;
	}
	public void resetCacheRebuilt() {
		cacheRebuilt = false;
	}
	

   //CENT_CACHE  private ArrayList<Pair<Integer,Data>> supportDataList = new ArrayList<Pair<Integer,Data>>();
    
   //DEBUG13  private NewVehicleData distDimDataList = null; //because needed very frequently
    //DEBUG13 private NewVehicleData lastDimDataList = null;
    
    
    private ArrayList<Pair<Integer, ModelSpec>> modelSpecList = new ArrayList<Pair<Integer, ModelSpec>>();
    private ArrayList<Pair<Integer, VehicleSpecific>> vehicleParamList = new ArrayList<Pair<Integer, VehicleSpecific>>();
    private ArrayList<Pair<Integer, LevelChangeList>> levelChangeList = new ArrayList<Pair<Integer, LevelChangeList>> ();
    
    private ModelSpec lastDimModelSpec = null;
    private VehicleSpecific lastVehicleParam = null;
    private LevelChangeList lastLevelChangeList = null;
    private int lastDimIdAsked = Misc.getUndefInt();
    
    private static ConcurrentHashMap<Integer, VehicleModelInfo> vehicleModelInfos = new ConcurrentHashMap<Integer, VehicleModelInfo>(OrgConst.G_NUMBER_VEHICLES, 0.75f, 4);
    private static ConcurrentHashMap<Integer, ModelSpec> modelProfiles = new ConcurrentHashMap<Integer, ModelSpec> (100, 0.75f, 4);
    
    
    public static VehicleModelInfo getVehicleModelInfo(Connection conn, Integer vehicleId) throws Exception {
    	VehicleModelInfo retval =  vehicleModelInfos.get(vehicleId);
    	if (conn != null && ModelProcessor.g_doLazyCacheBuild && (retval == null || !retval.isCacheRebuilt())) {
    		ArrayList<Integer> vehicleIds = new ArrayList<Integer>(1);
    		vehicleIds.add(vehicleId);
    		if (retval == null) {
    			VehicleModelInfo.loadVehicleSpecificModelSpec(conn, vehicleIds);
    			retval = vehicleModelInfos.get(vehicleId);
    			if (retval == null)
    				return null;
    		}
    		retval.setCacheRebuilt();//so that we dont keep on rebuilding cache during buildCache
    		//DEBUG13 ModelProcessor.buildCache(conn, vehicleIds);
    	}
    	return retval;
    }
    
    public void remove() {
    	remove(this.vehicleId);
    }
    
    public static void remove(Integer vehicleId)  {
    	vehicleModelInfos.remove(vehicleId);
    }
    
    public static VehicleModelInfo getVehicleModelInfo(Connection conn, int vehicleId) throws Exception {
    	return getVehicleModelInfo(conn, new Integer(vehicleId));
    }
    
    public VehicleModelInfo(int vehicleId) {
    	this.vehicleId = vehicleId;
    }
    
    public int getVehicleId() {
    	return vehicleId;
    }
    /*DEBUG13
    public NewVehicleData getDataList(Connection conn, int dimId) throws Exception {
    	if (dimId == 0)
    		return distDimDataList;
    	if (dimId == lastDimIdAsked)
    		return lastDimDataList;
    	VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false);
    	NewVehicleData dl = vdf.getDataList(conn, vehicleId, dimId, false);
    	lastDimIdAsked = dimId;
    	lastDimDataList = dl;
    	lastDimIdAsked = dimId;
		lastDimModelSpec = null;
		lastVehicleParam = null;
		lastLevelChangeList = null;
		this.lastDimDataList = dl;
		for (Pair<Integer, ModelSpec> mspentry : modelSpecList) {
			if (mspentry.first == dimId) {
				lastDimModelSpec = mspentry.second;
				break;
			}
		}
		for (Pair<Integer, VehicleSpecific> mspentry : vehicleParamList) {
			if (mspentry.first == dimId) {
				lastVehicleParam = mspentry.second;
				break;
			}
		}
		for (Pair<Integer, LevelChangeList> mspentry : levelChangeList) {
			if (mspentry.first == dimId) {
				lastLevelChangeList = mspentry.second;
				break;
			}
		}
    	return dl;
    }
    
    public ModelSpec getModelSpec(Connection conn, int dimId) throws Exception {
    	if (dimId == lastDimIdAsked)
    		return lastDimModelSpec;
    	for (Pair<Integer, ModelSpec> entry : modelSpecList) {
    		if (entry.first == dimId) {
    			ModelSpec retval = entry.second;
    			lastDimIdAsked = dimId;
    			lastDimDataList = null;
    			lastVehicleParam = null;
    			lastLevelChangeList = null;
    			this.lastDimModelSpec = retval;
    			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false);
    			lastDimDataList = vdf.getDataList(conn, vehicleId, dimId, false);
    	    	
    			for (Pair<Integer, VehicleSpecific> mspentry : vehicleParamList) {
    				if (mspentry.first == dimId) {
    					lastVehicleParam = mspentry.second;
    					break;
    				}
    			}
    			for (Pair<Integer,LevelChangeList> mspentry : levelChangeList) {
    				if (mspentry.first == dimId) {
    					lastLevelChangeList = mspentry.second;
    					break;
    				}
    			}
    			return retval;
    		}
    	}
    	return null;
    }
    
    public VehicleSpecific getVehicleParam(Connection conn, int dimId) throws Exception {
    	if (dimId == lastDimIdAsked && lastVehicleParam != null)
    		return lastVehicleParam;
    	for (Pair<Integer, VehicleSpecific> entry : vehicleParamList) {
    		if (entry.first == dimId) {
    			VehicleSpecific retval = entry.second;
    			lastDimIdAsked = dimId;
    			lastDimDataList = null;
    			lastVehicleParam = entry.second;
    			this.lastDimModelSpec = null;
    			lastLevelChangeList = null;
    			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false);
    			lastDimDataList = vdf.getDataList(conn, vehicleId, dimId, false);
    	    	
    			for (Pair<Integer, ModelSpec> mspentry : modelSpecList) {
    				if (mspentry.first == dimId) {
    					lastDimModelSpec = mspentry.second;
    					break;
    				}
    			}
    			for (Pair<Integer, LevelChangeList> mspentry : levelChangeList) {
    				if (mspentry.first == dimId) {
    					lastLevelChangeList = mspentry.second;
    					break;
    				}
    			}
    			return retval;
    		}
    	}
    	return null;
    }
    
    public LevelChangeList getLevelChangeList(Connection conn, int dimId) throws Exception {
    	if (dimId == lastDimIdAsked)
    		return lastLevelChangeList;
    	for (Pair<Integer, LevelChangeList> entry : levelChangeList) {
    		if (entry.first == dimId) {
    			LevelChangeList retval = entry.second;
    			lastDimIdAsked = dimId;
    			lastDimDataList = null;
    			lastVehicleParam = null;
    			this.lastDimModelSpec = null;
    			lastLevelChangeList = null;
    			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false);
    			lastDimDataList = vdf.getDataList(conn, vehicleId, dimId, false);
    			for (Pair<Integer, ModelSpec> mspentry : modelSpecList) {
    				if (mspentry.first == dimId) {
    					lastDimModelSpec = mspentry.second;
    					break;
    				}
    			}
    			for (Pair<Integer, VehicleSpecific> mspentry : this.vehicleParamList) {
    				if (mspentry.first == dimId) {
    					lastVehicleParam = mspentry.second;
    					break;
    				}
    			}
    			return retval;
    		}
    	}
    	return null;
    }
    DEBUG13 */
    /* CENT_CACHE
    public Data addDataList(int dimId) {//assumes ... does not exist
    	Data retval = new Data();
    	if (dimId == 0)
    		distDimDataList = retval;
    	supportDataList.add(new Pair<Integer, Data>(dimId, retval));
    	lastDimIdAsked = Misc.getUndefInt();
    	return retval;
    }
    */
    
    public LevelChangeList addLevelChangeList(int dimId) {//assumes ... does not exist
    	LevelChangeList retval = new LevelChangeList();
    	levelChangeList.add(new Pair<Integer, LevelChangeList>(dimId, retval));
    	lastDimIdAsked = Misc.getUndefInt();
    	return retval;
    }
    
    public void addLevelChangeList(int dimId, LevelChangeList levelChangeList) {
    	this.levelChangeList.add(new Pair<Integer, LevelChangeList> (dimId, levelChangeList));
    	lastDimIdAsked = Misc.getUndefInt();
    }
    
    private void addModelSpec(int dimId, ModelSpec modelSpec) {//assumes ... does not exist
    	modelSpecList.add(new Pair<Integer, ModelSpec>(dimId, modelSpec));
    	lastDimIdAsked = Misc.getUndefInt();
    }
    
    private void removeModelSpec(int dimId) {//assumes ... does not exist
    	for (int i=0,is=modelSpecList.size();i<is;i++) {
    		if (modelSpecList.get(i).first == dimId) {
    			modelSpecList.remove(i);
    			break;
    		}
    	}
    	lastDimIdAsked = Misc.getUndefInt();
    }
    
    private void replaceModelSpec(int dimId, ModelSpec modelSpec) {//assumes ... does not exist
    	for (int i=0,is=modelSpecList.size();i<is;i++) {
    		if (modelSpecList.get(i).first == dimId) {
    			modelSpecList.get(i).second = modelSpec;
    			break;
    		}
    	}
    	lastDimIdAsked = Misc.getUndefInt();
    }
    
    private void addVehicleParam(int dimId, VehicleSpecific vehicleParam) {//assumes ... does not exist
    	vehicleParamList.add(new Pair<Integer, VehicleSpecific>(dimId, vehicleParam));
    	lastDimIdAsked = Misc.getUndefInt();
    }
    
    /*CENT_CACHE
    private static int g_doCleanAfterEveryXPoints = 20;//TODO make it property driven
    private int ptsSinceClean = 0;
    
    public  void clearUnneeded(Connection conn, boolean force, boolean generateStats, boolean doDetectLevelChangeTillEnd) throws Exception {
    	ptsSinceClean++;
    	if (!force && ptsSinceClean < g_doCleanAfterEveryXPoints)
    		return;
    	ptsSinceClean = 0;
    	GpsData minDateforIgn = null;
    	ArrayList<Pair<Integer, GpsData>> minDateForDelta = new ArrayList<Pair<Integer, GpsData>>();
    	for (Pair<Integer, ModelSpec> e1:modelSpecList) {
    		ModelSpec spec = e1.second;
    		Data dimData = this.getDataList(e1.first);
    		if (dimData == null) //havent seen any point from this
    			continue;
			long ccLast = dimData.ccGpsTime;
			long dcLast = dimData.dcGpsTime;
			long miLastTime = ccLast < dcLast ? ccLast : dcLast;
			//check if this falls in between an ongoing start/stop ... if so then move it backward
			{//remove entries from startStopList
				SGpsData dummy = new SGpsData(new GpsData(miLastTime));				
				Pair<Integer, Boolean> index2 = this.startStopList.indexOf(dummy);
				int index2Int = index2.first;
				if (index2.second)
					index2Int--;
				int keepFromThisPointIdx = index2Int;
				if (index2Int >= 0) {
					if (!this.startStopList.get(index2Int).isTrue()) {
						keepFromThisPointIdx--;
					}
					else if (startStopList.get(index2Int).getGps_Record_Time() == miLastTime){
						keepFromThisPointIdx -= 2;
					}
					if (keepFromThisPointIdx < 0)
						keepFromThisPointIdx = 0;
					miLastTime = startStopList.get(keepFromThisPointIdx).getGps_Record_Time(); //
				}				
			}
			
    		Pair<Integer, Boolean> index = Misc.isUndef(miLastTime) ? new Pair<Integer, Boolean>(-1,false) : dimData.indexOf(new GpsData(miLastTime));
    		int pos = index.second ? index.first : index.first-1;
    		CacheTrack.VehicleSetup vsetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
    		if (vsetup != null && vsetup.m_backPtsToRetain > 0)
    			pos -= vsetup.m_backPtsToRetain;
    		int sz = dimData.size();
    		if ((sz-pos) > CacheTrack.VehicleSetup.g_forceCleanUpIfPtsCountExceeds) {
    			pos = sz-CacheTrack.VehicleSetup.g_ptsAfterForcedCleanup-1;
    			//make sure we are not loosing points between start stop
    			
    			GpsData ptatPos = dimData.get(pos); 
    			//make sure that this is not in stop list
    			{//remove entries from startStopList
    				SGpsData dummy = new SGpsData(ptatPos);
    				SGpsData atDummy   = this.startStopList.get(dummy);
    				SGpsData keepFrom = atDummy;
    				if (atDummy != null) {
    					if (atDummy.isTrue()) {
    						if (atDummy.getGps_Record_Time() == ptatPos.getGps_Record_Time())
    							keepFrom = startStopList.get(atDummy,-2);
    					}
    					else {
    						keepFrom = startStopList.get(atDummy,-1);
    					}
    				}
    				if (keepFrom == null)
    					keepFrom = atDummy;
    				if (keepFrom != null) {
    					pos = dimData.indexOf(keepFrom).first;
    					ptatPos = dimData.get(pos);
    				}
    			}
    			if (dcLast <= miLastTime) {
    				dimData.dcGpsTime = ptatPos.getGps_Record_Time();
    			}
    			if (ccLast <= miLastTime) {
    				dimData.ccGpsTime = ptatPos.getGps_Record_Time();
    			}
    			//dimData.dcPos = -1;
    		}
    		pos -= 1100;//was spec.ptsToLookBackForLevelChange; //make it parameterizable
    		if (spec.modelType == ModelSpec.MEDIAN_PLUS) {
    			pos -= m_medianWindow;
    		}
    		
    		if ((pos >= 1 || doDetectLevelChangeTillEnd) && LevelChangeList.g_checkAfterNPoints > 1) {
    			LevelChangeUtil.detectLevelChange(conn, this, e1.first, 0, doDetectLevelChangeTillEnd ? dimData.size()-1 : pos-1, true);
    			if (generateStats) {
    				ModelStats.generateStats(this, dimData, spec, pos);
    			}
    		}
    		if (pos >= 1) {
    			GpsData t1 = dimData.get(pos);
    			if (dimData.dcGpsTime < t1.getGps_Record_Time())
    				dimData.dcGpsTime = t1.getGps_Record_Time();
    			if (dimData.ccGpsTime < t1.getGps_Record_Time())
    				dimData.ccGpsTime = t1.getGps_Record_Time();
    			
    			dimData.removeFromStart(pos);
    			dimData.dcPos -= pos;
    			if (dimData.dcPos < -1)
    				dimData.dcPos = -1;
    			GpsData first = dimData.get(0);
    			if (minDateforIgn == null || minDateforIgn.getGps_Record_Time() > first.getGps_Record_Time())
    				minDateforIgn = first;
    			{//remove entries in levelChangeList
    				LevelChangeList l1 = this.getLevelChangeList(e1.first);
    				if (l1 != null) {
    					if (first != null) {
	    					Pair<Integer, Boolean> index2 = l1.indexOf(first);
	    					int pos2 = index2.first;
	    					if (index2.second)
	    						pos2--;
	    					if (pos2 > 0)
	    						l1.removeFromStart(pos2);
    					}
    				}
    			}
    			{//remove entries from startStopList
    				SGpsData dummy = new SGpsData(first);
    				Pair<Integer, Boolean> index2 = this.startStopList.indexOf(dummy);
    				int index2Int = index2.second ? index2.first-1 : index2.first;
    				if (index2Int > 0)
    					startStopList.removeFromStart(index2Int);
    			}
    			{
    				SGpsData dummy = new SGpsData(first);
    				Pair<Integer, Boolean> index2 = this.ignOffStartStopList.indexOf(dummy);
    				int index2Int = index2.second ? index2.first-1 : index2.first;
    				if (index2Int > 0)
    					ignOffStartStopList.removeFromStart(index2Int);
    			}
	    		boolean deltaEntryFound = spec.deltaByTime || spec.deltaDimId < 0;//being smart ... we dont want to search if there is no deltaDim
	    		
	    		if (!deltaEntryFound) {
	    			
	    			if (first != null) {
			    		for (Pair<Integer, GpsData> e2:minDateForDelta) {
			    			if (e2.first == spec.deltaDimId) {
			    				deltaEntryFound = true;
			    				if (e2.second.getGps_Record_Time() > first.getGps_Record_Time())
			    					e2.second = first;
			    				break;
			    			}
			    		}
			    		if (!deltaEntryFound) {
			    			minDateForDelta.add(new Pair<Integer, GpsData>(spec.deltaDimId, first));
			    		}
	    			}//if there was non-zero first gps data
	    		}//if valid deltaDimId
    		}//if there was changes in asked data ..
    	}//for each asked ddata
    	for (Pair<Integer, GpsData> e1:minDateForDelta) {
    		Data dataList = getDataList(e1.first);
    		if (dataList == null)
    			continue;
    		Pair<Integer, Boolean> index = dataList.indexOf(e1.second);
    		int retainTill = index.second ? index.first : index.first-1;
    		dataList.removeFromStart(retainTill);
    	}
    	if (minDateforIgn != null) {
    		Data dataList = getDataList(ApplicationConstants.IGN_ONOFF);
    		if (dataList != null) {
    			Pair<Integer, Boolean> index = dataList.indexOf(minDateforIgn);
        		int retainTill = index.second ? index.first : index.first-1;
        		if (retainTill > 0)
        			dataList.removeFromStart(retainTill);
    		}
    	}
	}

    public void saveStats(Connection conn) throws Exception {
    	try {
    		//1st delete ..
    		PreparedStatement ps = conn.prepareStatement("delete from model_stats where vehicle_id = ?");
    		ps.setInt(1, getVehicleId());
    		ps.execute();
    		ps.close();
    		
    		ps = conn.prepareStatement("insert into model_stats (vehicle_id, dim_id, n, residue_bucket, mean, deviation, max, min, count) values (?,?,?,?,?,?,?,?,?)");
    		for (Pair<Integer, ModelSpec> e1:modelSpecList) {
	    		NewVehicleData dataList = getDataList(conn, e1.first);
	    		if (dataList == null || dataList.stats == null)
	    			continue;
	    		for (int i=0,is = dataList.stats.size();i<is;i++) {
	    			ArrayList<ModelStats.StatsItem> row = dataList.stats.get(i);
	    			int n = ModelStats.g_numPointsToLookForward.get(i);
	    			for (int j=0,js = row.size(); j<js; j++) {
	    				ModelStats.StatsItem item = row.get(j);
	    				if (item.n == 0)
	    					continue;
	    				ps.setInt(1, getVehicleId());
	    				ps.setInt(2, e1.first);
	    				ps.setInt(3, n);
	    				ps.setDouble(4, ModelStats.g_residueBucket.get(j));
	    				ps.setDouble(5, item.mean);
	    				ps.setDouble(6, Math.sqrt(item.variance < 0 ? 0 : item.variance));
	    				ps.setDouble(7, item.max);
	    				ps.setDouble(8, item.min);
	    				ps.setInt(9, item.n);
	    				ps.addBatch();
	    			}
	    		}
	    		if (dataList.statRecoveryToSameVal != null) {
	    			ModelStats.StatsItem item =dataList.statRecoveryToSameVal;
    				ps.setInt(1, getVehicleId());
    				ps.setInt(2, e1.first);
    				ps.setInt(3, 0);
    				ps.setDouble(4, 0);
    				ps.setDouble(5, item.mean);
    				ps.setDouble(6, Math.sqrt(item.variance < 0 ? 0 : item.variance));
    				ps.setDouble(7, item.max);
    				ps.setDouble(8, item.min);
    				ps.setInt(9, item.n);
    				ps.addBatch();
	    		}
    		}
    		ps.executeBatch();
    		ps.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
	CENT_CACHE */    
    private static int g_doSaveAfterEveryXPoints = 100;//TODO make it property driven
    private int ptsSinceSave = 0;
    /*DEBUG13
	public  void saveState(Connection conn, boolean force) throws Exception {
		try {
	    	ptsSinceSave++;
	    	if (!force && ptsSinceSave < g_doSaveAfterEveryXPoints)
	    		return;
	    	ptsSinceSave = 0;
	    	
	    	PreparedStatement updateState = conn.prepareStatement("update model_state set saved_at=?, firstpoint=?, object=? where vehicle_id = ? and attribute_id=?");
	    	for (Pair<Integer, ModelSpec> e1:modelSpecList) {
	    		NewVehicleData dataList = getDataList(conn, e1.first);
	    		if (dataList == null)
	    			continue;
	    		GpsData firstPoint = dataList.getMpMarker(); //was dataList.get(0)
	    		//TOCHECK if (firstPoint == null)
	    		//TOCHECK	firstPoint = dataList.getLatestData();
	    		if (firstPoint == null)
	    			continue;
	    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(firstPoint.getModelState(e1.second));
				oos.flush();
				oos.close();
				byte[] data = baos.toByteArray();
				Timestamp firstPointTS =Misc.longToSqlDate(firstPoint.getGps_Record_Time());
				Timestamp savedAtTS = Misc.longToSqlDate(firstPoint.getGps_Record_Time());
				updateState.setTimestamp(1, savedAtTS);
				updateState.setTimestamp(2, firstPointTS);
				updateState.setObject(3, data);
				updateState.setInt(4, vehicleId);
				updateState.setInt(5, e1.first);
				updateState.execute();
	    	}
	    	updateState.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	DEBUG13*/
	public void prepForStats(Data dimData) {
		try {
			
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public  void generateStats(Data dimData, int pos) {
		try {
			
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
    public static void loadVehicleSpecificModelSpec(Connection conn, ArrayList<Integer> vehicleIds) {
    	try {
    	   //1.get profiles (integer -> hashmap of name/value pair)
    	   //2.get all/relevant vehices ... set if not default
    	   //3.get vehicle param - 
    		PreparedStatement ps = conn.prepareStatement("select model_profile_id, dim_id, param_name, param_val from model_profiles join model_profile_params on (model_profiles.id = model_profile_params.model_profile_id) order by model_profile_id");
    		int prev = Misc.getUndefInt();
    		int prevDimId = Misc.getUndefInt();
    		HashMap<String, Double> prevset = null;
    		ResultSet rs = ps.executeQuery();
    		while (rs.next()) {
    			int modelProfileId = rs.getInt(1);
    			int dimId = rs.getInt(2);
    			String name = rs.getString(3);
    			double val = Misc.getRsetDouble(rs, 4);
    			if (Misc.isUndef(val))
    				continue;
    			if (modelProfileId != prev || prevDimId != dimId) {
    				if (prevset != null) {
    				
    					Dimension dim = Dimension.getDimInfo(prevDimId);
        				if (dim == null || dim.getModelSpec() == null)
        					continue;
        				ModelSpec spec = modelProfiles.get(prev);
        				if (spec != null) {
        					spec.copyFrom(dim.getModelSpec());
        				}
        				else {
        					spec = (ModelSpec) dim.getModelSpec().clone();
        					if (spec.modelType == ModelSpec.MEDIAN_PLUS) {
    		//					((MedianPlusSpec)spec).setMedianWindow(Misc.getUndefInt());
    		//					((MedianPlusSpec)spec).setMaxMedianWindow(Misc.getUndefInt());
    		//					((MedianPlusSpec)spec).setMaxMedianDistKM(Misc.getUndefDouble());
    						}
        					modelProfiles.put(prev, spec);
        				}
    					spec.updateWithDynParams(prevset);
        				spec.updateWithDynParamModelSpecific(prevset);
    				}
    				prevset = null;
    			}
    			if (prevset == null) {
    				prevset = new HashMap<String, Double>();
    			}
    			prevset.put(name, val);
    			prev = modelProfileId;
    			prevDimId = dimId;
    		}
    		rs.close();
    		ps.close();
    		if (prevset != null) {
				
				Dimension dim = Dimension.getDimInfo(prevDimId);
				if (dim != null && dim.getModelSpec() != null) {
					ModelSpec spec = modelProfiles.get(prev);
					if (spec != null) {
						spec.copyFrom(dim.getModelSpec());
					}
					else {
						spec = (ModelSpec) dim.getModelSpec().clone();
						if (spec.modelType == ModelSpec.MEDIAN_PLUS) {
			//				((MedianPlusSpec)spec).setMedianWindow(Misc.getUndefInt());
			//				((MedianPlusSpec)spec).setMaxMedianWindow(Misc.getUndefInt());
			//				((MedianPlusSpec)spec).setMaxMedianDistKM(Misc.getUndefDouble());
						}
						modelProfiles.put(prev, spec);
					}
					spec.updateWithDynParams(prevset);
					spec.updateWithDynParamModelSpecific(prevset);
				}
			}
    		//now read the vehicle's profile map;
    		StringBuilder q = new StringBuilder("select vehicle.id, vmp.dim_id, vmp.model_profile_id from vehicle left outer join vehicle_model_profile_map vmp on (vehicle.status in (1) ");
    		if (vehicleIds != null && vehicleIds.size() > 0) {
    			q.append(" and vehicle.id in (");
    			Misc.convertInListToStr(vehicleIds, q);
    			q.append(")");
    		}
    		q.append(") order by vehicle.id, vmp.dim_id ");
    		ps = conn.prepareStatement(q.toString());
    		rs = ps.executeQuery();
    		int prevVehicle = Misc.getUndefInt();
    		VehicleModelInfo prevInfo = null;
    		ArrayList<Dimension> dimsWithModel = Dimension.getDimsWithModel();
    		while (rs.next()) {
    			int vehicleId = rs.getInt(1);
    			int dimId = Misc.getRsetInt(rs, 2);
    			int profileId = Misc.getRsetInt(rs, 3);
    			if (prevVehicle != vehicleId)
    				prevInfo = null;
    		    
    			if (prevInfo == null) {
    				CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
    				int orgLevelMedianWindow = Misc.getUndefInt();
    				int orgLevelMaxMedianWindow = Misc.getUndefInt();
    				double orgLevelMaxDistKM = Misc.getUndefDouble();
    				//NOT USED
    				/*
    				double orgLevelKMPL = Misc.getUndefDouble();
    				double orgLevelReset = Misc.getUndefDouble();
    				double orgLevelResidueChange  = Misc.getUndefDouble();
    				double orgLevelPosNegThresh = Misc.getUndefDouble();
    				double orgLevelPosNegProp = Misc.getUndefDouble();
    				double orgLevelPosNegWindow = Misc.getUndefDouble();
    				*/
    				if (vehSetup != null) {
    					int ownerOrg = vehSetup.m_ownerOrgId;
    					MiscInner.PortInfo portInfo = Cache.getCacheInstance(conn).getPortInfo(ownerOrg, conn);
    					ArrayList<Integer> plist = portInfo.getIntParams(OrgConst.ID_INT_FUEL_WINDOW, true);
    					if (plist != null && plist.size() != 0) {
    						orgLevelMedianWindow = plist.get(0);
    					}
    					plist = portInfo.getIntParams(OrgConst.ID_INT_MAX_FUEL_WINDOW, true);
    					if (plist != null && plist.size() != 0) {
    						orgLevelMaxMedianWindow = plist.get(0);
    					}
    					ArrayList<Double> dlist = portInfo.getIntParams(OrgConst.ID_FUEL_MAX_DISTKM, true);
    					if (dlist != null && dlist.size() != 0) {
    						orgLevelMaxDistKM = dlist.get(0);
    					}
    					/*
    					dlist = portInfo.getIntParams(OrgConst.ID_FUEL_KMPL, true);
    					if (dlist != null && dlist.size() != 0) {
    						orgLevelKMPL = dlist.get(0);
    					}
    					dlist = portInfo.getIntParams(OrgConst.ID_FUEL_RESET_VAL_CHANGE, true);
    					if (dlist != null && dlist.size() != 0) {
    						orgLevelReset = dlist.get(0);
    					}
    					dlist = portInfo.getIntParams(OrgConst.ID_FUEL_RESIDUE_EXCEEDS, true);
    					if (dlist != null && dlist.size() != 0) {
    						orgLevelResidueChange = dlist.get(0);
    					}
    					dlist = portInfo.getIntParams(OrgConst.ID_FUEL_POS_NEG_THRESH, true);
    					if (dlist != null && dlist.size() != 0) {
    						orgLevelPosNegThresh = dlist.get(0);
    					}
    					dlist = portInfo.getIntParams(OrgConst.ID_FUEL_POS_NEG_LOOKAHEAD__PROP_EXCEEDS, true);
    					if (dlist != null && dlist.size() != 0) {
    						orgLevelPosNegProp = dlist.get(0);
    					}
    					plist = portInfo.getIntParams(OrgConst.ID_INT_FUEL_POS_NEG_WINDOW, true);
    					if (plist != null && plist.size() != 0) {
    						orgLevelPosNegWindow = plist.get(0);
    					}
    					*/

    				}
    				
    				if (Misc.isUndef(orgLevelMedianWindow)) {
						orgLevelMedianWindow = VehicleModelInfo.g_medianWindow;
					}
    				if (Misc.isUndef(orgLevelMaxMedianWindow)) {
						orgLevelMaxMedianWindow = VehicleModelInfo.g_maxMedianWindow;
					}
    				if (Misc.isUndef(orgLevelMaxDistKM)) {
    					orgLevelMaxDistKM = VehicleModelInfo.g_maxMedianDistanceKM;
					}
    				Integer vehicleIdInt = new Integer(vehicleId);
    				prevInfo = getVehicleModelInfo(null, vehicleIdInt);//conn is passed as null because we dont want to build fullCache now
    				if (prevInfo == null) {
    					prevInfo = new VehicleModelInfo(vehicleId);
    					vehicleModelInfos.put(vehicleIdInt, prevInfo);
    				}
    				prevInfo.setMedianWindow(orgLevelMedianWindow, orgLevelMaxMedianWindow,orgLevelMaxDistKM);
    				
    				
					for (Dimension dim:dimsWithModel) {
						prevInfo.removeModelSpec(dim.getId());
						prevInfo.addModelSpec(dim.getId(), dim.getModelSpec());
					}
    			}
    			ModelSpec profileSpec = modelProfiles.get(profileId);
    			if (profileSpec == null)
    				continue;
    			prevInfo.replaceModelSpec(dimId, profileSpec);
    			if (profileSpec.modelType == ModelSpec.MEDIAN_PLUS) {
    				MedianPlusSpec msp = (MedianPlusSpec)profileSpec;
    				prevInfo.setMedianWindow(msp.getMedianWindow(), msp.getMaxMedianWindow(), msp.getMaxMedianDistKM());
    			}
    			
    			prevVehicle = vehicleId;
    		}
    		rs.close();
    		ps.close();
    		
    		loadVehicleMinMax(conn, vehicleIds);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    	}
    }
    
    public static void loadVehicleMinMax(Connection conn, ArrayList<Integer> vehicleIds) throws Exception {
    	try {
    		StringBuilder q = new StringBuilder();
    		q.append("select vehicle.id, io_map.attribute_id, io_map.min_val, io_map.max_val, min(dimension_value_map.value), max(dimension_value_map.value) ")
    		  .append(" from vehicle join io_map on (vehicle.io_set_id = io_map.io_map_info_id) left outer join dimension_value_map on (dimension_value_map_info_id = dimension_reading_id) ")
    		  .append(" where attribute_id in (");
    		boolean first = true;
    		for (Dimension dim:Dimension.getDimsWithModel()) {
			   if (!first) {
				   q.append(",");
			   }
			   else
				   first = false;
			   q.append(dim.getId());
			}
    		q.append(") ");
    		q.append(" and vehicle.status in (1) "); 
    		if (vehicleIds != null && vehicleIds.size() > 0) {
    			q.append(" and vehicle.id in (");
    			Misc.convertInListToStr(vehicleIds, q);
    			q.append(")");
    		}
    		q.append(" group by vehicle.id, io_map.attribute_id, io_map.min_val, io_map.max_val order by vehicle.id, io_map.attribute_id ");
    		PreparedStatement ps = conn.prepareStatement(q.toString());
    		ResultSet rs = ps.executeQuery();
    		while (rs.next()) {
    			int vehicleId = rs.getInt(1);
    			int dimId = rs.getInt(2);
    			if (vehicleId == 15390) {
    				int dbg = 1;
    				dbg++;
    			}
    			double mi = Misc.getRsetDouble(rs, 3);
    			double mx = Misc.getRsetDouble(rs, 4);
    			double rmi = Misc.getRsetDouble(rs, 5);
    			double rmx = Misc.getRsetDouble(rs, 6);
    			if (Misc.isUndef(mi))
    				mi = rmi;
    			if (Misc.isUndef(mx))
    				mx = rmx;
    			if (!Misc.isUndef(mi) && !Misc.isUndef(mx)) {
    				VehicleModelInfo veh = VehicleModelInfo.getVehicleModelInfo(null, vehicleId);
    				VehicleSpecific vehicleParam = new VehicleSpecific();
    				vehicleParam.setMin(mi);
    				vehicleParam.setMax(mx);
    				veh.addVehicleParam(dimId, vehicleParam);
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
   
}

package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.geometry.Point;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.mapguideutils.NameLocationLookUp;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.ShapeFileBean;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.GpsDataResultSetReader;
import com.ipssi.processor.utils.Vehicle;
import com.ipssi.common.ds.trip.OpArea;
import com.ipssi.common.ds.trip.OpMapping;
import com.ipssi.common.ds.trip.TripInfoConstants;
import com.ipssi.common.ds.trip.VehicleControlling;


public class OpStationBean implements Comparable {
	public static class AlertInfo {
		public String emailL1 = null;
		public String phoneL1 = null;
		public String userL1 = null;
		public String emailL2 = null;
		public String phoneL2 = null;
		public String userL2 = null;
	}
	private int opStationId;
	private ArrayList<MiscInner.Pair> portNodeIds = new ArrayList<MiscInner.Pair>(); //first = portNodeId, second = opstation type
	private String opStationName = null;
	private String shortCode = null;
	private int waitAreaId;
	private int gateAreaId;
	private long startDate = Misc.getUndefInt();
	private long endDate = Misc.getUndefInt();
	//private ArrayList<Quad<Integer,Integer,Integer,Integer>> regionIdsList = new ArrayList<Quad<Integer,Integer,Integer,Integer>>();
	private ArrayList<OpArea> regionIdsList = new ArrayList<OpArea>();
    private int[] intVal = new int[12];
    private int linkedVehicleId = Misc.getUndefInt();
    private boolean isOperational = true;
    //private int opStationType;
    private double lowerX;
    private double lowerY;
    private double upperX;
    private double upperY;
    public boolean m_bestAreaIsFirst = false;
    public boolean m_bestAreaIsLast = false;
    public boolean m_lookForChallan = false;
    public boolean m_hybridFlipOnly = false;
    public boolean m_hasOverlappingOpArea = false;
    private int areaOfWork = Misc.getUndefInt();
    private int subType = 0;
    private int confirmIfExitArea = Misc.getUndefInt();
    private double linkedWaitDelX = Misc.getUndefDouble();
    private double linkedWaitDelY = Misc.getUndefDouble();
    private double linkedGateDelX = Misc.getUndefDouble();
    private double linkedGateDelY = Misc.getUndefDouble();
    private int nearestOpId = Misc.getUndefInt();
    private int nearestLMId = Misc.getUndefInt();
    private double nearestOpDist = Misc.getUndefDouble();
    private double nearestLMDist = Misc.getUndefDouble();
    private int prefMaterial = Misc.getUndefInt();// undef ... hasnt been set, -1 means set but is undef ... will be set from material_id field of opstation and region list
    private int refOpStationId = Misc.getUndefInt();
    private int refMinesId = Misc.getUndefInt();
    private AlertInfo forTransporter = null;
    private AlertInfo forSender = null;
    private AlertInfo forCustomer = null;
    public int getPrefMaterial() {
    	int materialId = prefMaterial;
    	if (Misc.isUndef(prefMaterial)) {
    		prefMaterial = -1;
    		for (int i=0,is=this.regionIdsList == null ? 0 : regionIdsList.size(); i<is; i++) {
    			if (regionIdsList.get(i).materialId >= 0) {
    				prefMaterial =  regionIdsList.get(i).materialId;
    				break;
    			}
    		}
    	}
    	return prefMaterial < 0 ? Misc.getUndefInt() : prefMaterial;
    }
    public void addOplevelMaterial(int materialId) {
    	if (!Misc.isUndef(materialId)) {
    		prefMaterial = materialId;
    	}
    }
   public boolean isRestAreaTransit(int opType) {
	   return this.subType == TripInfoConstants.REST_TRANSIT_SUBTYPE;
   }
    public int getFlexParamSize() {
    	return intVal.length;
    }
    public boolean isIntermediate(Connection conn, Cache cache, int portNodeId, VehicleControlling vehicleControlling) {
    	int opStationType = this.getOpStationType(conn, cache, portNodeId, vehicleControlling);
    	return opStationType == TripInfoConstants.PRE_LOAD_IM || opStationType == TripInfoConstants.PRE_UNLOAD_IM || opStationType == TripInfoConstants.POST_IM;
    }
    public int getGuaranteedLoadTypeFromOpType(Connection conn, Cache cache, int portNodeId, VehicleControlling vehicleControlling) {
    	int opStationType = this.getOpStationType(conn, cache, portNodeId, vehicleControlling);
    	return opStationType == TripInfoConstants.LOAD ? 0 : opStationType == TripInfoConstants.UNLOAD ? 1 : opStationType == TripInfoConstants.PRE_LOAD_IM ? 3 : opStationType == TripInfoConstants.PRE_UNLOAD_IM ? 4 : opStationType == TripInfoConstants.POST_IM ? 5 : 2;
    }
    public static int getGuaranteedLoadTypeFromOpType(int opStationType) {
    	return opStationType == TripInfoConstants.LOAD ? 0 : opStationType == TripInfoConstants.UNLOAD ? 1 : opStationType == TripInfoConstants.PRE_LOAD_IM ? 3 : opStationType == TripInfoConstants.PRE_UNLOAD_IM ? 4 : opStationType == TripInfoConstants.POST_IM ? 5 : 2;
    }
    public void setLinkedDelta(StopDirControl stopDirControl, boolean force) {
    	if (force || Misc.isUndef(linkedWaitDelX)) {
    		linkedWaitDelX = stopDirControl.getM_linkedGateWaitBoxMtr()/1000.0;
    	}
    }
    public boolean isPointLinkedWait(Connection conn, Point gpsPoint, Point center, StopDirControl stopDirControl) {
    	setLinkedDelta(stopDirControl, false);
    	if (center == null)
    		return false;
    	double d1 = center.distance(gpsPoint);
    	return d1 < linkedWaitDelX || Misc.isEqual(d1, linkedWaitDelX);
    }
    
    public boolean isPointLinkedGate(Connection conn, Point gpsPoint, Point center, StopDirControl stopDirControl) {
    	return isPointLinkedWait(conn, gpsPoint, center, stopDirControl);
    }
    public boolean isPointLinkedWaitOrig(Connection conn, Point gpsPoint, Point center) {
    	setLinkedDeltaOrig(conn, false);
    	if (center == null)
    		return false;
    	double xmin = center.getX()-linkedWaitDelX;
    	double xmax = center.getX()+linkedWaitDelX;
    	if (xmin > gpsPoint.getX() || xmax < gpsPoint.getX())
    		return false;
    	double ymin = center.getY()-linkedWaitDelY;
    	double ymax = center.getY()+linkedWaitDelY;
    	if (ymin > gpsPoint.getY() || ymax < gpsPoint.getY())
    		return false;
    	return true;
    }
    public boolean isPointLinkedGateOrig(Connection conn, Point gpsPoint, Point center) {
    	setLinkedDeltaOrig(conn, false);
    	if (center == null)
    		return false;
    	double xmin = center.getX()-linkedGateDelX;
    	double xmax = center.getX()+linkedGateDelX;
    	if (xmin > gpsPoint.getX() || xmax < gpsPoint.getX())
    		return false;
    	double ymin = center.getY()-linkedGateDelY;
    	double ymax = center.getY()+linkedGateDelY;
    	if (ymin > gpsPoint.getY() || ymax < gpsPoint.getY())
    		return false;
    	return true;
    }
    public void setLinkedDeltaOrig(Connection conn, boolean force) {
    	if (force || Misc.isUndef(linkedWaitDelX)) {
    		try {
    			RegionTest.RegionTestHelper rh = RegionTest.getRegionInfo(this.waitAreaId, conn);
    			if (rh == null)
    				return;
    			this.linkedWaitDelX = (rh.region.m_urCoord.getX()-rh.region.m_llCoord.getX())/2.0;
    			this.linkedWaitDelY = (rh.region.m_urCoord.getY()-rh.region.m_llCoord.getY())/2.0;
    			rh = RegionTest.getRegionInfo(this.gateAreaId, conn);
    			if (rh == null)
    				return;
    			this.linkedGateDelX = (rh.region.m_urCoord.getX()-rh.region.m_llCoord.getX())/2.0;
    			this.linkedGateDelY = (rh.region.m_urCoord.getY()-rh.region.m_llCoord.getY())/2.0;
    		}
    		catch (Exception e) {
    			//eat it
    		}
    	}
    }
    public OpStationBean topLevelCopy() {
    	OpStationBean retval = new OpStationBean();
    	retval.opStationId  = this.opStationId;
    	retval.shortCode = this.shortCode;
    	for (MiscInner.Pair entry: this.portNodeIds)
    		retval.portNodeIds.add(new MiscInner.Pair(entry.first, entry.second));
    	retval.opStationName = this.opStationName;
    	retval.waitAreaId = this.waitAreaId;
    	retval.gateAreaId = this.gateAreaId;
    	//for (OpArea op : regionIdsList) {
    	//	retval.regionIdsList.add(op);
    	//}
    	for (int i=0,is = intVal.length;i<is;i++)
    		retval.intVal[i] = this.intVal[i];
    	retval.linkedVehicleId = this.linkedVehicleId;
    	retval.isOperational = this.isOperational;
    	retval.lowerX = this.lowerX;
    	retval.lowerY = this.lowerY;
    	retval.upperX = this.upperX;
    	retval.upperX = this.upperY;
    	retval.m_bestAreaIsFirst = this.m_bestAreaIsFirst;
    	retval.m_bestAreaIsLast = this.m_bestAreaIsLast;
    	retval.m_hasOverlappingOpArea = this.m_hasOverlappingOpArea;
    	retval.m_hybridFlipOnly = this.m_hybridFlipOnly;
    	retval.areaOfWork = this.areaOfWork;
    	retval.confirmIfExitArea = this.confirmIfExitArea;
    	retval.subType = this.subType;
    	retval.linkedGateDelX = this.linkedGateDelX;
    	retval.linkedGateDelY = this.linkedGateDelY;
    	retval.linkedWaitDelX = this.linkedWaitDelX;
    	retval.linkedGateDelY = this.linkedWaitDelY;
    	retval.forCustomer = copyAlertInfo(this.forCustomer);
    	retval.forTransporter = copyAlertInfo(this.forTransporter);
    	retval.forSender = copyAlertInfo(this.forSender);
    	return retval;
    }
    
    public boolean hasOverlappingOpArea() {
		return m_hasOverlappingOpArea;
	}

	public void setHasOverlappingOpArea(boolean hasOverlappingOpArea) {
		this.m_hasOverlappingOpArea = hasOverlappingOpArea;
	}

	public boolean isHybridFlipOnly() {
		return m_hybridFlipOnly;
	}
    
    public boolean isHybrid(Connection conn, Cache cache, int portNodeId, VehicleControlling vehicleControlling) {
    	int opStationType = getOpStationType(conn, cache, portNodeId, vehicleControlling);
    	return isHybrid(opStationType);
    }
    
    public static boolean isHybrid(int opStationType) {
    	return opStationType == TripInfoConstants.HYBRID_UL || opStationType == TripInfoConstants.HYBRID_LU || opStationType == TripInfoConstants.HYBRID_ALL || opStationType == TripInfoConstants.HYBRID_NONE || opStationType == TripInfoConstants.HYBRID_UL_ALWAY;
    }
    
	public void setHybridFlipOnly(boolean flipOnly) {
		m_hybridFlipOnly = flipOnly;
	}

	private ReentrantLock forPosition = null;
    final public static int g_waitForLockMS = 1331;
	
    //bunch of fields and methods related to linkedVehicle is at the end simple func at end
    public OpStationBean() {    	
    	for (int i=0,is=intVal.length; i<is;i++)
    		intVal[i] = Misc.getUndefInt();
    }
    
    public void copy(OpStationBean rhs) {
    	this.opStationId = rhs.opStationId;
    	this.shortCode = rhs.shortCode;
    	for (MiscInner.Pair entry: rhs.portNodeIds)
    		this.portNodeIds.add(new MiscInner.Pair(entry.first, entry.second));
    	this.opStationName = rhs.opStationName;
    	this.waitAreaId = rhs.waitAreaId;
    	this.gateAreaId = rhs.gateAreaId;
    	this.regionIdsList = rhs.regionIdsList;
    	this.prefMaterial = rhs.prefMaterial;
    	this.intVal = rhs.intVal;
    	this.linkedVehicleId = rhs.linkedVehicleId;
    	this.isOperational = rhs.isOperational;
    	this.lowerX = rhs.lowerX;
    	this.lowerY = rhs.lowerY;
    	this.upperX = rhs.upperX;
    	this.upperY = rhs.upperY;
    	this.lastCenteredAt = rhs.lastCenteredAt;
    	this.maxDataIsAt = rhs.maxDataIsAt; 
    	this.minDataIsAt = rhs.minDataIsAt;
    	this.dataRangeLoIncl = rhs.dataRangeLoIncl;
    	this.dataRangeHiIncl = rhs.dataRangeHiIncl;
    	
    	this.cachedPos = rhs.cachedPos;
    	this.linkedVehicleSetup = rhs.linkedVehicleSetup;
    	this.subType = rhs.subType;
    	this.linkedGateDelX = rhs.linkedGateDelX;
    	this.linkedGateDelY = rhs.linkedGateDelY;
    	this.linkedWaitDelX = rhs.linkedWaitDelX;
    	this.linkedGateDelY = rhs.linkedWaitDelY;
    	this.forCustomer = copyAlertInfo(rhs.forCustomer);
    	this.forTransporter = copyAlertInfo(rhs.forTransporter);
    	this.forSender = copyAlertInfo(rhs.forSender);
    }
    
    
    public boolean isOperational(Connection conn, ThreadContextCache threadContextCache, GpsData data, int ownerOrgId, VehicleControlling vehicleControlling, CacheTrack.VehicleSetup vehSetup) throws Exception {
    	boolean isopFromDate = this.isValidByDate(data.getGps_Record_Time());
    	if (!isopFromDate)
    		return false;
    	if (!Misc.isUndef(linkedVehicleId)) {
    	    //check if it lies within restArea - how do we know which? Org Const ... get the one that is of type
    		List<OpStationBean> restAreasList = TripInfoCacheHelper.getOpStationsForVehicle(conn, this.getLinkedVehicleSetup(conn), TripInfoConstants.REST_AREA_REGION, null, threadContextCache,0, vehicleControlling);
            for (int i=0,is= restAreasList == null ? 0 : restAreasList.size(); i<is; i++) {
            	OpStationBean opstnBean = restAreasList.get(i);
            	//if (lastCenteredAt == null || Region.checkInOutRegion(conn, opstnBean.getWaitAreaId(), lastCenteredAt.getPoint(), th)){
            	ThreadContextCache.SimpleMoving movingInfo = threadContextCache.getMovingOpStationContaining(conn, ownerOrgId, data, this, vehicleControlling, vehSetup);
            	if (movingInfo == null || RegionTest.PointIn(conn, movingInfo.getCenter(), opstnBean.getWaitAreaId())) {
            		return false;
            	}
            }
    	}
    	return isOperational;
    }
    
    public void setOperationalStatus(Connection conn, boolean status) throws Exception {
    	isOperational = status;
    }
    
    public int getLinkedVehicleId() {
    	return linkedVehicleId;
    }
    
    public void setLinkedVehicleId(int vehicleId) throws Exception {
    	boolean lockAttempt = false;
    	try {
    		if (forPosition != null)
    			lockAttempt = tryLock();
    		if (lockAttempt || forPosition == null) {
		        this.linkedVehicleId = vehicleId;
		        this.linkedVehicleSetup = null;
		        if (Misc.isUndef(linkedVehicleId) && forPosition != null) {
		        	forPosition.unlock();
		        	lockAttempt = false;
		        }
		        else if (!Misc.isUndef(linkedVehicleId) && forPosition == null) {
		        	forPosition = new ReentrantLock();
		        }
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    	finally {
    		if (lockAttempt)
    			unlock();
    	}
    }
    
    public int getLLUUBreakTimeMin() {
    	return intVal[0];
    }
    public int getQLengthThreshold() {
    	return intVal[1];
    }
    public int getStrandedThresholdSec() {
    	return intVal[2];
    }
    public int getExcessProcessingTimeThresholdSec() {
    	return intVal[3];
    }
    public int getNotOperatingThresholdSec() {
    	return intVal[4];
    }
    public int getThreshCompletionNoDirMilliSec() {
    	return intVal[5];
    }
    public int getLLUUBreakDistMtr() {
    	int d= intVal[6];
    	if (!Misc.isUndef(d))
    		d += 5000; //DEBUG13 - doing start to start ... add distance of max diagonal
    	return d;
    }
    public int getAreaMergeMtr() {
    	int d = intVal[7];
    	if (!Misc.isUndef(d))
    		d += 5000; //DEBUG13 - doing start to start ... add distance of max diagonal
    	return d;

    }
    public int getSeqMergeSeqMtr() {
    	int retval = intVal[8];
    	if (retval > 0)
    		retval *= 2;
    	return retval;//HACK_DEBUG13 .. instead of end-start dist we do start to start    	
    }
    public int getThreshDistToTakeWinNextMtr() {
    	return intVal[9];
    }
    public int getCheckForOverlappingArea() {
    	return intVal[10];
    }
    public int getThreshCompletionDirChangeMilliSec() {
    	return intVal[11];
    }

    public int getIntVal(int index) {
    	return  (index < 0 || index >= intVal.length) ? Misc.getUndefInt() : intVal[index];
    }
    
    public void setIntVal(int index, int val) {
    	if (index >= 0 && index < intVal.length)
    		intVal[index] = val;
    }
	public boolean isStopTemplate(Connection conn, Cache cache, int vehiclePortNodeId, VehicleControlling vehicleControlling) {
		if (conn == null || cache == null || Misc.isUndef(vehiclePortNodeId))
			return portNodeIds.size() > 0 ? portNodeIds.get(0).second == TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE : false;
		boolean found = false;
		try {
			for (MiscInner.Pair entry: portNodeIds) {
				int portofOp = entry.first;
				for (MiscInner.PortInfo portInfo = cache.getPortInfo(vehiclePortNodeId, conn); portInfo != null; portInfo = portInfo.m_parent) {
					if (portInfo.m_id == portofOp) {
						found  = true;
						break;
					}
					boolean toGoup =  portInfo.getIntParamImm(OrgConst.ID_INT_TO_STOP_GOINGUP_FOR_OPSTATION) <= 0;//TODO make it general
					if (!toGoup)
						break;
				}
				if (found) {
					return entry.second == TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return portNodeIds.size() > 0 ? portNodeIds.get(0).second == TripInfoConstants.STOP_BASED_OPSTATION_TEMPLATE : false;
			//eat it
		}
		return false;
		
	}
	public int getOpStationType(Connection conn, Cache cache, int vehiclePortNodeId, VehicleControlling vehicleControlling) { //returns undef if it is not a valid opstation type for the
		OpMapping opm = vehicleControlling == null ? null : vehicleControlling.getOpMapping();
		if (opm != null) {
			return opm.getTypeFor(opStationId);
		}
		if (conn == null || cache == null || Misc.isUndef(vehiclePortNodeId))
			return portNodeIds.size() > 0 ? portNodeIds.get(0).second : Misc.getUndefInt();
		boolean found = false;
		try {
		for (MiscInner.Pair entry: portNodeIds) {
			int portofOp = entry.first;
			for (MiscInner.PortInfo portInfo = cache.getPortInfo(vehiclePortNodeId, conn); portInfo != null; portInfo = portInfo.m_parent) {
				if (portInfo.m_id == portofOp) {
					found  = true;
					break;
				}
				boolean toGoup =  portInfo.getIntParamImm(OrgConst.ID_INT_TO_STOP_GOINGUP_FOR_OPSTATION) <= 0;//TODO make it general
				if (!toGoup)
					break;
			}
			if (found) {
				return entry.second;
			}
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			return portNodeIds.get(0).second;
			//eat it
		}
		return Misc.getUndefInt();
	}

	public void setOpStationType(int portNodeId, int opStationType) {
		for (MiscInner.Pair entry: portNodeIds) {
			if (entry.first == portNodeId) {
				entry.second = opStationType;
				return;
			}
		}
		portNodeIds.add(new MiscInner.Pair(portNodeId, opStationType));
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	public int getOpStationId() {
		return opStationId;
	}
	public void setOpStationId(int opStationId) {
		this.opStationId = opStationId;
	}
	public String getOpStationName() {
		return opStationName;
	}
	public void setOpStationName(String opStationName) {
		this.opStationName = opStationName;
	}
	public int getWaitAreaId() {
		return waitAreaId;
	}
	
	public void setWaitAreaId(int waitAreaId, Connection conn) throws Exception {
		this.waitAreaId = waitAreaId;
		RegionTest.RegionTestHelper region = RegionTest.getRegionInfo(waitAreaId, conn);
		if (region != null) {
			lowerX = region.region.m_llCoord.getX();
			lowerY = region.region.m_llCoord.getY();
			upperX = region.region.m_urCoord.getX();
			upperY = region.region.m_urCoord.getY();
		}
	}
	
	public int getGateAreaId() {
		return gateAreaId;
	}
	public void setGateAreaId(int gateAreaId) {
		this.gateAreaId = gateAreaId;
	}
	public ArrayList<OpArea> getRegionIdsListDontAdd() { //cant add to arraylist dir - need to make sure materialList is proper
		return regionIdsList;
	}
	public OpArea checkInOutOpArea(Connection conn, Vehicle vehicle) throws Exception {
	    return Region.checkInOutRegionList(conn, regionIdsList, vehicle);
	}
	public int getMaxOpThreshold() {
		int mxTh = -1;
		if (mxTh < 0)
			mxTh = getIntVal(5);
		
		for (int l1=0,l1s = regionIdsList == null ? 0 : regionIdsList.size(); l1<l1s;l1++) {
			OpArea area = regionIdsList.get(l1);
			if (!area.isNormal())
				continue;
			if (area.thresholdMilliSec > mxTh)
				mxTh = area.thresholdMilliSec;
		}
		return mxTh;
	}
	public void setRegionIdsList(ArrayList<OpArea> regionIdsList) {
		this.regionIdsList = regionIdsList;
		if (this.prefMaterial < 0) {
			for (OpArea oparea: regionIdsList) {
				if (oparea.materialId >= 0) {
					prefMaterial = oparea.materialId;
					break;
				}
			}
		}
	}
	public void addOpArea(OpArea area) {
		regionIdsList.add(area);
		if (area.materialId >= 0) {
			if (this.prefMaterial < 0)
				this.prefMaterial = area.materialId;
		}
	}
	
	public void addOpArea(int id, int materialId, int priority, int thresholdMilliSec, int opAreaType) {
		addOpArea(new OpArea(id, materialId, priority, thresholdMilliSec, opAreaType));
	}
	public void addOpArea(int id, int materialId, int priority, int thresholdMilliSec, int opAreaType, long start, long end) {
		addOpArea(new OpArea(id, materialId, priority, thresholdMilliSec, opAreaType, start, end));
	}
	
	//bunch of fields and methods related to linkedVehicle start here
	//Note on centering the opstation at the point
	//we keep a FastList of GpsDataPoint in memory  points are added if only they are different from the previous point in the list by a threshold.
	//the size of the list is fixed to approximately g_maxCachedPos. When we add a point and realize that size has become more than this we remove 
	//fraction of total ( g_cleanFractionBy) from the beginning till the gps point just added
	//We keep 4 date point (each gps_record_time):
	// minDataIsAt and maxDataIsAt represent the min/max of FastList but take into account that maxDataIsAt is increased even if the last point 
	//was not really added
	// dataRangeLoIncl and dataRangeHiIncl are the min/max data timestamp that exist in the db
	// when we get point (refData) and the point is within minDataAt and maxDataAt - we just look up
	// if refData is outside of maxDataAt but is also after current DataRangeHiIncl then we add to list, update the DataRangeHiIncl and maxDataAt
	// , else we load data from maxDataAt till at least the datapoint being asked
	// if data is less than minDataAt then we load from date lesser than date being added till minDataAt

	
	
	private GpsData lastCenteredAt = null;
	private long maxDataIsAt = Misc.getUndefInt(); 
	private long minDataIsAt = Misc.getUndefInt();
	private long dataRangeLoIncl = Misc.getUndefInt();
	private long dataRangeHiIncl = Misc.getUndefInt();
	
	private FastList<GpsData> cachedPos = new FastList<GpsData>();
	private CacheTrack.VehicleSetup linkedVehicleSetup = null;
	static int g_maxCachedPos = 5700; //TODO make it property driven ... enough to hold 1 day of data twice
	static int g_keepLastNPointsRelToCurrPoint = 500;
	static int g_cleanFractionBy = 3;
	static int g_truncateListIfDateRangeExceedsDays = 2;
	public static int g_estMaxLoggedDataToPrePopulateCache = g_maxCachedPos; //public because used in Query ... TODO make it property driven
	
	public long getDataRangeLoIncl() {
		return dataRangeLoIncl;
	}
	public long getDataRangeHiIncl() {
		return dataRangeHiIncl;
	}
	public void setDataRangeLoIncl(long dataRangeLoIncl) {
		if (!Misc.isUndef(dataRangeLoIncl) && (Misc.isUndef(this.dataRangeLoIncl) || this.dataRangeLoIncl > dataRangeLoIncl))
			this.dataRangeLoIncl = dataRangeLoIncl;
	}
	public void setDataRangeHiIncl(long dataRangeHiIncl) {
		if (!Misc.isUndef(dataRangeHiIncl) && (Misc.isUndef(this.dataRangeHiIncl) || this.dataRangeHiIncl < dataRangeHiIncl))
		 this.dataRangeHiIncl = dataRangeHiIncl;
	}
	
	
	public CacheTrack.VehicleSetup getLinkedVehicleSetup(Connection conn) throws Exception {
		if (linkedVehicleSetup == null)
			linkedVehicleSetup = CacheTrack.VehicleSetup.getSetup(linkedVehicleId,conn);
		return linkedVehicleSetup;
	}
	
	public void addToCachedPos(Connection conn, GpsData refData, StopDirControl stopDirControl) throws Exception {
        double linkedShiftDistExceedsMtr = stopDirControl.getM_linkedShiftDistExceedsMtr();
        linkedShiftDistExceedsMtr = linkedShiftDistExceedsMtr/1000.0;
        addToCachedPos(refData, linkedShiftDistExceedsMtr);
	}
	
	public void addToCachedPos(GpsData refData, double linkedShiftDistExceedsMtr) {
		GpsData prev = cachedPos.get(refData);
		if (true || prev == null || prev.distance(refData.getLongitude(),refData.getLatitude()) > linkedShiftDistExceedsMtr) {
			cachedPos.add(refData);
		}
		long gpsTime = refData.getGps_Record_Time();
		if (Misc.isUndef(minDataIsAt) || minDataIsAt > gpsTime)
			minDataIsAt = gpsTime;
		if (Misc.isUndef(maxDataIsAt) || maxDataIsAt < gpsTime)
			maxDataIsAt = gpsTime;
		this.setDataRangeLoIncl(gpsTime);
		this.setDataRangeHiIncl(gpsTime);
	}
	
	public void cleanUpIfNeeded(int lastUsedAt) {
	    if (cachedPos.size() > g_maxCachedPos)	{
	    	lastUsedAt -= g_keepLastNPointsRelToCurrPoint;
	    	int torem = g_maxCachedPos/g_cleanFractionBy;
		    if (torem > lastUsedAt)
		    	torem = lastUsedAt;
		    if (torem > 0) {
		    	cachedPos.removeFromStart(torem);
		    	minDataIsAt = cachedPos.get(0).getGps_Record_Time();
		    }
	    }
	}
	
	public void truncateCachedPos() {
		cachedPos.clear();
		minDataIsAt = Misc.getUndefInt();
		maxDataIsAt = Misc.getUndefInt();
	}

	public GpsData getLastCenteredAt(Connection conn, GpsData gpsData) throws Exception {
		return positionOpStationAtTime(conn, gpsData, true, null);
	}
	public GpsData positionOpStationAtTime(Connection conn, GpsData refData, boolean justReturnCenter, StopDirControl stopDirControl) throws Exception {//will return center only if justReturnCenter is true and in which case will not center it
		return getBestPtBefOrAft(conn, this.linkedVehicleId, refData, 3600, 3600);
	}
	public static GpsData getBestPtBefOrAft(Connection conn, int linkedVehicleId, GpsData refData,  int leftThreshSec, int rightThreshSec) throws Exception {//will return center only if justReturnCenter is true and in which case will not center it
		int vehicleId = linkedVehicleId;
		VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
		GpsData centerData = null;
		if (vdf != null) {
			synchronized (vdf) {
				NewVehicleData vdt = vdf.getDataList(conn, vehicleId, 0, false);
				if (vdt != null) {
					GpsData ptBef = vdt.get(conn, refData);
					GpsData ptAft = vdt.get(conn, refData, 1);
					if (ptBef != null && ptAft != null) {
						if (ptBef.isMergeable(ptAft, vdt.isGpsIdDelta(), vdt.isCummDistSensorBased())) {
							centerData = ptBef;
						}
					}
					if (centerData == null) {
						int gapSecBef = ptBef == null ? Misc.getUndefInt() : (int)((refData.getGps_Record_Time()- ptBef.getGps_Record_Time())/1000);
						//if ( gap >= -10*60*1000 && gap <=  60*60*1000)
						if (!Misc.isUndef(gapSecBef) && gapSecBef < leftThreshSec) {
							centerData = ptBef;
						}
						else {
							int gapSecAft = ptAft == null ? Misc.getUndefInt() : (int)((ptAft.getGps_Record_Time() - refData.getGps_Record_Time())/1000);
							if (!Misc.isUndef(gapSecAft) && gapSecAft < rightThreshSec)
								centerData = ptAft;
						}
					}//if pt asked does lie between stoped pt					
				}//end if vdt != null
			}//end of sync block
		}//end fof if vdf != null
		//if (centerData == null || justReturnCenter)
		return centerData;
		/*
        if (lastCenteredAt != null &&  lastCenteredAt.isValidPoint() && centerData.isValidPoint()) { 			
	       	 double dist = lastCenteredAt.distance(centerData.getLongitude(),centerData.getLatitude());
	       	 double linkedShiftDistExceedsMtr = stopDirControl.getM_linkedShiftDistExceedsMtr();
	         linkedShiftDistExceedsMtr = linkedShiftDistExceedsMtr/1000.0;
	        
			if (dist <= linkedShiftDistExceedsMtr) {
				//no need to shift!
				return null;
			}
		}
       	shiftCenterTo(conn, centerData.getPoint()); 
       lastCenteredAt = centerData;
       return centerData;
       */
	}
	
	private void shiftCenterTo(Connection conn, Point pt) throws Exception {
		RegionTest.RegionTestHelper rh = RegionTest.getRegionInfo(this.waitAreaId, conn);
		rh.region.shiftTo(pt);
		lowerX = rh.region.m_llCoord.getX();
		lowerY = rh.region.m_llCoord.getY();
		upperX = rh.region.m_urCoord.getX();
		upperY = rh.region.m_urCoord.getY();
		
		if (waitAreaId != gateAreaId) {
			rh = RegionTest.getRegionInfo(gateAreaId, conn);
			rh.region.shiftTo(pt);
		}
		for (OpArea rid : regionIdsList) {
			 if (rid.id != gateAreaId && rid.isNormal()) {
				 rh = RegionTest.getRegionInfo(rid.id, conn);
				 rh.region.shiftTo(pt);
			 }
		}
	}
	public Integer getMaterial(int areaId){
		for (OpArea rid : regionIdsList) {
			if(rid.id == areaId)
				return rid.materialId;
		}
		return null;
	}
	
	public boolean tryLock() {
		boolean retval = true;
		if (forPosition != null) {
			try {
				retval = forPosition.tryLock(this.g_waitForLockMS, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				
			}
		}
		return retval;
	}
	
	public void unlock() {
		try {
			if (forPosition != null)
				forPosition.unlock();
			}
		catch (Exception e) {
			
		}
	}
	
	public boolean isHeldByCurrentThread() {
		return forPosition != null ? forPosition.isHeldByCurrentThread() : true;
	}
	
	public void dumpStats(StringBuilder retval) {
		retval.append("\n[TPD] Opstation:").append(this.opStationId).append(" ").append(cachedPos.size());
		if (cachedPos.size() > 0) {
			retval.append(cachedPos.get(0));
		}
		if (cachedPos.size() > 1) {
			retval.append(" @#@ ").append(cachedPos.get(cachedPos.size()-1));
		}
	}
	public String toString(){
		return " [OpStation : " + opStationName+ "]";
	}
	public GpsData getLastCenteredAtOld(GpsData gpsData) {
		return cachedPos.get(gpsData, true);
	}

	public double getLowerX() {
		return lowerX;
	}

	public double getLowerY() {
		return lowerY;
	}

	public double getUpperX() {
		return upperX;
	}

	public double getUpperY() {
		return upperY;
	}

	public boolean isBestAreaIsFirst() {
		return m_bestAreaIsFirst;
	}

	public void setBestAreaIsFirst(boolean areaIsFirst) {
		m_bestAreaIsFirst = areaIsFirst;
	}

	public boolean isBestAreaIsLast() {
		return m_bestAreaIsLast;
	}

	public void setBestAreaIsLast(boolean areaIsLast) {
		m_bestAreaIsLast = areaIsLast;
	}

	public boolean isLookForChallan() {
		return m_lookForChallan;
	}

	public void setLookForChallan(boolean forChallan) {
		m_lookForChallan = forChallan;
	}

	public void setAreaOfWork(int areaOfWork) {
		this.areaOfWork = areaOfWork;
	}

	public int getAreaOfWork() {
		return areaOfWork;
	}

	public void setConfirmIfExitArea(int confirmIfExitArea) {
		this.confirmIfExitArea = confirmIfExitArea;
	}

	public int getConfirmIfExitArea() {
		return confirmIfExitArea;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}
	public int getNearestOpId() {
		return nearestOpId;
	}
	public void setNearestOpId(int nearestOpId) {
		this.nearestOpId = nearestOpId;
	}
	public int getNearestLMId() {
		return nearestLMId;
	}
	public void setNearestLMId(int nearestLMId) {
		this.nearestLMId = nearestLMId;
	}
	public double getNearestOpDist() {
		return nearestOpDist;
	}
	public void setNearestOpDist(double nearestOpDist) {
		this.nearestOpDist = nearestOpDist;
	}
	public double getNearestLMDist() {
		return nearestLMDist;
	}
	public void setNearestLMDist(double nearestLMDist) {
		this.nearestLMDist = nearestLMDist;
	}
	public void setNearestOpLMEtc(Connection conn, Point pt, CacheTrack.VehicleSetup vehSetup, VehicleControlling vehicleControlling) {
		try {
			int vehiclePortNodeId = vehSetup.m_ownerOrgId;
			StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
			if (stopDirControl.getM_getNearestOpForStop() >= 0) {
				Pair<OpStationBean, Double> info = TripInfoCacheHelper.getNearestSpecialOpstation(pt, null, stopDirControl.getM_getNearestOpForStop(), vehiclePortNodeId, stopDirControl.getM_threshKMForNearestOp(), conn, vehicleControlling);
				if (info != null && info.first != null) {
					this.nearestOpId = info.first.getOpStationId();
					this.nearestOpDist = info.second;
				}
				else {
					this.nearestOpId = Misc.getUndefInt();
					this.nearestOpDist = Misc.getUndefDouble();					
				}
			}
			if (stopDirControl.getM_getNearstSpecialLM() >= 0) {
				Pair<ShapeFileBean, Double> info = NameLocationLookUp.getSpecialLMName(conn, vehSetup, 481, pt, null, stopDirControl.getM_getNearstSpecialLM());//DEBUG13, LAFARGE_HACK
				if (info != null && info.first != null) {
					this.nearestLMId = info.first.getId();
					this.nearestLMDist = info.second;
				}
				else {
					this.nearestLMId = Misc.getUndefInt();
					this.nearestLMDist = Misc.getUndefDouble();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		
	}
	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		if (startDate <= 0)
			startDate =1;
		this.startDate = startDate;
	}
	public long getEndDate() {
		
		return endDate;
	}
	public void setEndDate(long endDate) {
		if (endDate <= 0)
			endDate = Long.MAX_VALUE;
		this.endDate = endDate;
	}
	
	public boolean isValidByDate(long dt) {
		return dt <= 0 || (startDate <= dt && dt <= endDate);
	}
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		OpStationBean opb = (OpStationBean) arg0;
		return this.opStationId-opb.opStationId;
	}
	public int getRefOpStationId() {
		return refOpStationId;
	}
	public void setRefOpStationId(int refOpStationId) {
		this.refOpStationId = refOpStationId;
	}
	public int getRefMinesId() {
		return refMinesId;
	}
	public void setRefMinesId(int refMinesId) {
		this.refMinesId = refMinesId;
	}
	
	public String getAlertPhoneL1Customer() {
		return this.forCustomer == null ? null : this.forCustomer.phoneL1;
	}

	public void setAlertPhoneL1Customer(String alertPhoneL1Customer) {
		if (alertPhoneL1Customer != null && this.forCustomer == null) {
			this.forCustomer = new AlertInfo();
		}
		if (this.forCustomer != null) {
			this.forCustomer.phoneL1 = alertPhoneL1Customer;
		}
	}

	public String getAlertPhoneL2Customer() {
		return this.forCustomer == null ? null : this.forCustomer.phoneL2 != null ? this.forCustomer.phoneL2 : this.forCustomer.phoneL1;
	}

	public void setAlertPhoneL2Customer(String alertPhoneL2Customer) {
		if (alertPhoneL2Customer != null && this.forCustomer == null) {
			this.forCustomer = new AlertInfo();
		}
		if (this.forCustomer != null) {
			this.forCustomer.phoneL2 = alertPhoneL2Customer;
		}
	}

	public String getAlertEmailL1Customer() {
		return this.forCustomer == null ? null : this.forCustomer.emailL1;
	}

	public void setAlertEmailL1Customer(String alertEmailL1Customer) {
		if (alertEmailL1Customer != null && this.forCustomer == null) {
			this.forCustomer = new AlertInfo();
		}
		if (this.forCustomer != null) {
			this.forCustomer.emailL1 = alertEmailL1Customer;
		}
	}

	public String getAlertEmailL2Customer() {
		return this.forCustomer == null ? null : this.forCustomer.emailL2 != null ? this.forCustomer.emailL2 : this.forCustomer.emailL1;
	}

	public void setAlertEmailL2Customer(String alertEmailL2Customer) {
		if (alertEmailL2Customer != null && this.forCustomer == null) {
			this.forCustomer = new AlertInfo();
		}
		if (this.forCustomer != null) {
			this.forCustomer.emailL2 = alertEmailL2Customer;
		}
	}

	public String getAlertUserL1Customer() {
		return this.forCustomer == null ? null : this.forCustomer.userL1;
	}

	public void setAlertUserL1Customer(String alertUserL1Customer) {
		if (alertUserL1Customer != null && this.forCustomer == null) {
			this.forCustomer = new AlertInfo();
		}
		if (this.forCustomer != null) {
			this.forCustomer.userL1 = alertUserL1Customer;
		}
	}

	public String getAlertUserL2Customer() {
		return this.forCustomer == null ? null : this.forCustomer.userL2 != null ? this.forCustomer.userL2 : this.forCustomer.userL1;
	}

	public void setAlertUserL2Customer(String alertUserL2Customer) {
		if (alertUserL2Customer != null && this.forCustomer == null) {
			this.forCustomer = new AlertInfo();
		}
		if (this.forCustomer != null) {
			this.forCustomer.userL2 = alertUserL2Customer;
		}
	}

	public String getAlertPhoneL1Transporter() {
		return this.forTransporter == null ? null : this.forTransporter.phoneL1;
	}

	public void setAlertPhoneL1Transporter(String alertPhoneL1Transporter) {
		if (alertPhoneL1Transporter != null && this.forTransporter == null) {
			this.forTransporter = new AlertInfo();
		}
		if (this.forTransporter != null) {
			this.forTransporter.phoneL1 = alertPhoneL1Transporter;
		}
	}

	public String getAlertPhoneL2Transporter() {
		return this.forTransporter == null ? null : this.forTransporter.phoneL2 != null ? this.forTransporter.phoneL2 : this.forTransporter.phoneL1;
	}

	public void setAlertPhoneL2Transporter(String alertPhoneL2Transporter) {
		if (alertPhoneL2Transporter != null && this.forTransporter == null) {
			this.forTransporter = new AlertInfo();
		}
		if (this.forTransporter != null) {
			this.forTransporter.phoneL2 = alertPhoneL2Transporter;
		}
	}

	public String getAlertEmailL1Transporter() {
		return this.forTransporter == null ? null : this.forTransporter.emailL1;
	}

	public void setAlertEmailL1Transporter(String alertEmailL1Transporter) {
		if (alertEmailL1Transporter != null && this.forTransporter == null) {
			this.forTransporter = new AlertInfo();
		}
		if (this.forTransporter != null) {
			this.forTransporter.emailL1 = alertEmailL1Transporter;
		}
	}

	public String getAlertEmailL2Transporter() {
		return this.forTransporter == null ? null : this.forTransporter.emailL2 != null ? this.forTransporter.emailL2 : this.forTransporter.emailL1;
	}

	public void setAlertEmailL2Transporter(String alertEmailL2Transporter) {
		if (alertEmailL2Transporter != null && this.forTransporter == null) {
			this.forTransporter = new AlertInfo();
		}
		if (this.forTransporter != null) {
			this.forTransporter.emailL2 = alertEmailL2Transporter;
		}
	}

	public String getAlertUserL1Transporter() {
		return this.forTransporter == null ? null : this.forTransporter.userL1;
	}

	public void setAlertUserL1Transporter(String alertUserL1Transporter) {
		if (alertUserL1Transporter != null && this.forTransporter == null) {
			this.forTransporter = new AlertInfo();
		}
		if (this.forTransporter != null) {
			this.forTransporter.userL1 = alertUserL1Transporter;
		}
	}

	public String getAlertUserL2Transporter() {
		return this.forTransporter == null ? null : this.forTransporter.userL2 != null ? this.forTransporter.userL2 : this.forTransporter.userL1;
	}

	public void setAlertUserL2Transporter(String alertUserL2Transporter) {
		if (alertUserL2Transporter != null && this.forTransporter == null) {
			this.forTransporter = new AlertInfo();
		}
		if (this.forTransporter != null) {
			this.forTransporter.userL2 = alertUserL2Transporter;
		}
	}
	
	public String getAlertPhoneL1Sender() {
		return this.forSender == null ? null : this.forSender.phoneL1;
	}

	public void setAlertPhoneL1Sender(String alertPhoneL1Sender) {
		if (alertPhoneL1Sender != null && this.forSender == null) {
			this.forSender = new AlertInfo();
		}
		if (this.forSender != null) {
			this.forSender.phoneL1 = alertPhoneL1Sender;
		}
	}

	public String getAlertPhoneL2Sender() {
		return this.forSender == null ? null : this.forSender.phoneL2 != null ? this.forSender.phoneL2 : this.forSender.phoneL1;
	}

	public void setAlertPhoneL2Sender(String alertPhoneL2Sender) {
		if (alertPhoneL2Sender != null && this.forSender == null) {
			this.forSender = new AlertInfo();
		}
		if (this.forSender != null) {
			this.forSender.phoneL2 = alertPhoneL2Sender;
		}
	}

	public String getAlertEmailL1Sender() {
		return this.forSender == null ? null : this.forSender.emailL1;
	}

	public void setAlertEmailL1Sender(String alertEmailL1Sender) {
		if (alertEmailL1Sender != null && this.forSender == null) {
			this.forSender = new AlertInfo();
		}
		if (this.forSender != null) {
			this.forSender.emailL1 = alertEmailL1Sender;
		}
	}

	public String getAlertEmailL2Sender() {
		return this.forSender == null ? null : this.forSender.emailL2 != null ? this.forSender.emailL2 : this.forSender.emailL1;
	}

	public void setAlertEmailL2Sender(String alertEmailL2Sender) {
		if (alertEmailL2Sender != null && this.forSender == null) {
			this.forSender = new AlertInfo();
		}
		if (this.forSender != null) {
			this.forSender.emailL2 = alertEmailL2Sender;
		}
	}

	public String getAlertUserL1Sender() {
		return this.forSender == null ? null : this.forSender.userL1;
	}

	public void setAlertUserL1Sender(String alertUserL1Sender) {
		if (alertUserL1Sender != null && this.forSender == null) {
			this.forSender = new AlertInfo();
		}
		if (this.forSender != null) {
			this.forSender.userL1 = alertUserL1Sender;
		}
	}

	public String getAlertUserL2Sender() {
		return this.forSender == null ? null : this.forSender.userL2 != null ? this.forSender.userL2 : this.forSender.userL1;
	}

	public void setAlertUserL2Sender(String alertUserL2Sender) {
		if (alertUserL2Sender != null && this.forSender == null) {
			this.forSender = new AlertInfo();
		}
		if (this.forSender != null) {
			this.forSender.userL2 = alertUserL2Sender;
		}
	}
	
	private AlertInfo copyAlertInfo(AlertInfo alertInfo) {
		if (alertInfo == null || (alertInfo.emailL1 == null && alertInfo.emailL2 == null && alertInfo.phoneL1 == null && alertInfo.phoneL2 == null && alertInfo.userL1 == null && alertInfo.userL2 == null))
			return null;
		AlertInfo retval = new AlertInfo();
		retval.emailL1 = alertInfo.emailL1;
		retval.emailL2 = alertInfo.emailL2;
		retval.phoneL1 = alertInfo.phoneL1;
		retval.phoneL2 = alertInfo.phoneL2;
		retval.userL1 = alertInfo.userL1;
		retval.userL2 = alertInfo.userL2;
		return retval;
	}
	
}

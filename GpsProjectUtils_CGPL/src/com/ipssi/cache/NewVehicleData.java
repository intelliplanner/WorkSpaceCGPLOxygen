package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;


import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.PerfStat;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.geometry.Point;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.mapguideutils.LocalNameHelper;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.GpsDataResultSetReader;
import com.ipssi.processor.utils.Vehicle;
import com.ipssi.reporting.common.util.Common;
import com.ipssi.cache.DirTimeInfo;

public class NewVehicleData {
	public final static String GET_GPS_DATA_SIMPLE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, cast(logged_data.attribute_value as decimal(10,3)) attribute_value, gps_record_time, source, name,updated_on,speed, gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time >= ?";
	public final static String GET_GPS_DATA_SIMPLE_IMM_BEFORE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, cast(attribute_value as decimal(10,3)) attribute_value, gps_record_time, source, name,updated_on,speed, gps_id from logged_data join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ? group by vehicle_id, attribute_id) mx on (mx.vehicle_id = logged_data.vehicle_id and logged_data.attribute_id = mx.attribute_id and logged_data.gps_record_time = mx.grt) ";
	
	
	public final static String GET_GPS_DATA_BOUND = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed,gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? and gps_record_time < ?  order by gps_record_time ";
	public final static String GET_GPS_DATA_BOUND_LOWER = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed,gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? and gps_record_time < ?  order by gps_record_time desc ";		
	public final static String GET_GPS_DATA_BY_COUNT_HIGHER = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed,gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ?    order by gps_record_time limit ? ";
	public final static String GET_GPS_DATA_BY_COUNT_LOWER = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed,gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ?   order by gps_record_time desc limit ? ";	

	public final static String GET_GPS_DATA_IMM_OLD = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed,logged_data.gps_id from logged_data " +
	" left outer join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ? group by vehicle_id, attribute_id) mx on (mx.vehicle_id = logged_data.vehicle_id and mx.attribute_id = logged_data.attribute_id) "+	
	" left outer join (select vehicle_id, attribute_id, min(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? group by vehicle_id, attribute_id) mi on (mi.vehicle_id = logged_data.vehicle_id and mi.attribute_id = logged_data.attribute_id) "+
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and (logged_data.gps_record_time = mi.grt or logged_data.gps_record_time=mx.grt) order by logged_data.gps_record_time "
	;

	public final static String GET_GPS_DATA_SINGLE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed, logged_data.gps_id from logged_data " +
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and logged_data.gps_record_time <= ? order by vehicle_id, attribute_id, gps_record_time desc limit 1"
	;
	public final static String GET_GPS_DATA_SINGLE_AFTER_OR_EQUAL = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed, logged_data.gps_id from logged_data " +
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and logged_data.gps_record_time >= ? order by vehicle_id, attribute_id, gps_record_time  limit 1"
	;
	
	//public final static String GET_GPS_DATA_SINGLE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed "+
	//" from (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id = ? and attribute_id = ? and gps_record_time >= ? group by vehicle_id, attribute_id) mx join logged_data on (logged_data.vehicle_id = mx.vehicle_id and logged_data.attribute_id = mx.attribute_id and logged_data.gps_record_time = mx.grt) "
	//;

	public final static String GET_GPS_DATA_BOUND_BY_UPDATE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed, logged_data.gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? and gps_record_time < ?  and (? is null or updated_on <? or (updated_on=? and gps_record_time < ?)) order by gps_record_time ";
	public final static String GET_GPS_DATA_BOUND_LOWER_BY_UPDATE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed, logged_data.gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? and gps_record_time < ?    and (? is null or updated_on <? or (updated_on=? and gps_record_time < ?)) order by gps_record_time desc ";	
	public final static String GET_GPS_DATA_BY_COUNT_HIGHER_BY_UPDATE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed, logged_data.gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ?     and (? is null or updated_on <? or (updated_on=? and gps_record_time < ?)) order by gps_record_time limit ? ";
	public final static String GET_GPS_DATA_BY_COUNT_LOWER_BY_UPDATE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed, logged_data.gps_id from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ?    and (? is null or updated_on <? or (updated_on=? and gps_record_time < ?)) order by gps_record_time desc limit ? ";
	
	public final static String GET_GPS_DATA_IMM = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed,logged_data.gps_id from logged_data " +
	" left outer join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ? group by vehicle_id, attribute_id) mx on (mx.vehicle_id = logged_data.vehicle_id and mx.attribute_id = logged_data.attribute_id) "+
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and (logged_data.gps_record_time=mx.grt) "+
	" union all "+
	"select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed,logged_data.gps_id from logged_data " +
	" left outer join (select vehicle_id, attribute_id, min(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? group by vehicle_id, attribute_id) mi on (mi.vehicle_id = logged_data.vehicle_id and mi.attribute_id = logged_data.attribute_id) "+
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and (logged_data.gps_record_time = mi.grt) "
	;
	
	public final static String GET_GPS_DATA_IMM_BY_UPDATE = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed,logged_data.gps_id from logged_data " +
	" left outer join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ? and gps_record_time > ? and (updated_on < ? or updated_on = ? and gps_record_time < ?) group by vehicle_id, attribute_id) mx on (mx.vehicle_id = logged_data.vehicle_id and mx.attribute_id = logged_data.attribute_id) "+
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and (logged_data.gps_record_time=mx.grt) "+
	" union all "+
	"select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed,logged_data.gps_id from logged_data " +
	" left outer join (select vehicle_id, attribute_id, min(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ? and gps_record_time < ? and (updated_on < ? or updated_on = ? and gps_record_time < ?) group by vehicle_id, attribute_id) mi on (mi.vehicle_id = logged_data.vehicle_id and mi.attribute_id = logged_data.attribute_id) "+
	" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and (logged_data.gps_record_time = mi.grt) "
	;

	public final static String GET_GPS_DATA_IMM_BY_UPDATE_OLD = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, logged_data.attribute_value, logged_data.gps_record_time, logged_data.source, logged_data.name,logged_data.updated_on,logged_data.speed, logged_data.gps_id from logged_data " +
			" left outer join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time < ?   and gps_record_time > ? and (? is null or updated_on <? or (updated_on=? and gps_record_time < ?)) group by vehicle_id, attribute_id) mx on (mx.vehicle_id = logged_data.vehicle_id and mx.attribute_id = logged_data.attribute_id) "+
			" left outer join (select vehicle_id, attribute_id, min(gps_record_time) grt from logged_data where vehicle_id=? and attribute_id=? and gps_record_time > ?    and gps_record_time > ?  and (? is null or updated_on <? or (updated_on=? and gps_record_time < ?)) group by vehicle_id, attribute_id) mi on (mi.vehicle_id = logged_data.vehicle_id and mi.attribute_id = logged_data.attribute_id) "+
			" where logged_data.vehicle_id = ? and logged_data.attribute_id=? and (logged_data.gps_record_time = mi.grt or logged_data.gps_record_time=mx.grt) order by logged_data.gps_record_time "
			;
	public static boolean g_doingSeparateCache = true;
	public static boolean g_getDataFromDBByUpdatedOn = false;//DEBUG_13 .. in prod before 20140318 was true
	public static boolean g_ignoreUpdatedOn = true;
	public static long g_gapAfterPtOKMilli = 10*1000;
	public static long g_infinite_future = System.currentTimeMillis()+2*365*24*3600*1000L;  
	public static long g_infinite_past = System.currentTimeMillis()-2*365*24*3600*1000L;
	public static long g_dcAtMostXMillisecBehindReg = (long)(3*24*60*60*1000);//(long)(1*10*60*60*1000);
	public static long g_dcAtMostXMillisecBehindSuper = (long)(7*24*60*60*1000);//(long)(1*10*60*60*1000);

	public static long g_dcAtMostXMillisecBehind = g_dcAtMostXMillisecBehindReg; //NOT to be used - for backward
	public static long g_backPtsAllowedMillisecBehind = (long)(1*10*60*60*1000); //NOT to be used - for backward
	public static long g_maxTimeOfDataAllowedMilli = (long)(1*10*60*60*1000); //NOT to be used - for backward
	
	public static int g_reg_maxPoints = 300;//make it divisble by 3 and 4 to be easier
	public static int g_reg_maxPointsForForceClean =(int)(g_reg_maxPoints*10);//14400;//if the number of points after add becomes this much ... then clean up ... either
	public static int g_reg_deltaLookAheadSec = 600;//g_maxPoints < 500 ? g_maxPoints/30*60*10 : ;//5*50;//10*60;
	public static int g_reg_deltaLookAheadCount = 100;//g_maxPoints/15*10;//3;//20;
	
	public static int g_reco_maxPoints = 1000;//make it divisble by 3 and 4 to be easier
	public static int g_reco_maxPointsForForceClean =(int)(g_reco_maxPoints*3);//14400;//if the number of points after add becomes this much ... then clean up ... either
	public static int g_reco_deltaLookAheadSec = 1800;//g_maxPoints < 500 ? g_maxPoints/30*60*10 : ;//5*50;//10*60;
	public static int g_reco_deltaLookAheadCount = 300;//g_maxPoints/15*10;//3;//20;
	//private static boolean g_dontLoadRight = true;
	private int vehicleId = Misc.getUndefInt();
	private int attributeId = 0;
	private long dbReads = 0;
	private long readReq = 0;
	private long mxLen = 0;
	private long minTime = -1;
	private long maxTime = -1;
	private int prevAddAt = -1;
	private int prevReadAt = -1;
	private FastList<GpsData> dataList = new FastList<GpsData>();
	private long odometerWeekRecTime = Misc.getUndefInt();
	private long odometerDayRecTime = Misc.getUndefInt();
	private long odometerMonthRecTime = Misc.getUndefInt();
	
	private GpsData latestDataChannelData = null;
	private GpsData latestCurrentChannelData = null;
	private GpsData prevLatestDataChannelData = null;
	private GpsData prevLatestCurrentChannelData = null;
	private GpsData latestReceivedData = null;
	private GpsData prevLatestReceivedData = null;
	private String latestDataChannelName = null;
	private String latestCurrentChannelName = null;
	private String prevOverallName = null;
	private GpsData prevNameAtPt = null;
	private GpsData prevPrevNameAtPt = null;
	private String prevPrevOverallName = null;
	private String prevLatestDataChannelName = null;
	private String prevLatestCurrentChannelName = null;
	private boolean regetMiMxDone = false;
	private boolean reinitDoneInDPInNonRecoveryOnce = false;
	private AddnlPointsOnAdd addnlPointsOnAdd = new AddnlPointsOnAdd();
	
	private long internalLatestRecvTime = Misc.getUndefInt();
	private long internalLatestRecordTime = Misc.getUndefInt();
	private ArrayList<GpsData> transientList = null;
	private long unsavedDataSince = 0;
	private Dala01Mgmt dalaUpMgmt = new Dala01Mgmt();	
	private static byte G_CURRENT_RECORD_FLAG = (byte)0x1;
	private static byte G_INRECOVERY_MODE_FLAG = (byte)0x2;
	private static byte G_CUMMDIST_GPSID_DELTA = (byte) 0x4;
	private static byte G_CUMMDIST_GPSID_SENSOR_BASED = (byte)0x8;
	private byte variousFlags = 0; 
	private byte useMode = 0;// 0 = DP, 1 = RP, 2 = TP;
	private byte maxCachedPtCountMultiple = 1;
	private short currPointsSinceResetRP = 0;
	private short currPointsSinceResetTP = 0;

	private short pointsSinceResetRP = 0;
	private short pointsSinceResetTP = 0;
	private static String NEW_UPDATE_DATA_INFO = "update logged_data set attribute_value=?, speed=? where vehicle_id=? and gps_record_time=? and attribute_id=?"; //Must match Modeler.DBQueries.NEW_UPDATE_DATA_INFO
	private static String NEW_UPDATE_DATA_INFO_DIST = "update logged_data set attribute_value=?, speed=? where vehicle_id=? and gps_record_time >? and gps_record_time < ? and attribute_id=?"; //Must match Modeler.DBQueries.NEW_UPDATE_DATA_INFO
   	
	
	public static boolean hasAcceptableGap(GpsData prev, GpsData data) {
		return prev == null || data.getGps_Record_Time() - prev.getGps_Record_Time() >= g_gapAfterPtOKMilli;
	}
	public boolean hasAcceptableGap(Connection conn, GpsData data) {
		GpsData prev = get(conn, data);
		if (prev != null && prev.getGps_Record_Time() == data.getGps_Record_Time())
			prev = get(conn, data, -1);
		return hasAcceptableGap(prev, data);
	}
	public void setUseMode(int mode) {
		useMode = (byte) mode;
	}
	public boolean isProcessedForUse(Connection conn, GpsData data) {
		if (useMode == 0)
			return false;
		GpsData currEntry = get(conn, data);
		boolean gpsIdDelta = this.isGpsIdDelta();
		boolean gpsIdSensorBased = this.isCummDistSensorBased();
		boolean meDone = currEntry != null && ((useMode == 1 && currEntry.isRPDone()) || (useMode == 2 && currEntry.isTPDone())) && currEntry.isMergeable(data, gpsIdDelta, gpsIdSensorBased);
		if (!meDone || currEntry.getGps_Record_Time() == data.getGps_Record_Time())
			return meDone;
		GpsData currEntryPlus1 = get(conn, data,1);
		meDone = currEntryPlus1 != null && ((useMode == 1 && currEntryPlus1.isRPDone()) || (useMode == 2 && currEntryPlus1.isTPDone()));
		return meDone;
	}
	
	public void resetMarkProcessedForUse(Connection conn, GpsData data) {
		if (useMode == 0)
			return;
		GpsData currEntry = get(conn, data);
		if (currEntry != null && currEntry.getGps_Record_Time() == data.getGps_Record_Time()) {
			if (useMode == 1)
				currEntry.setRPDone(false);
			else if (useMode == 2)
				currEntry.setTPDone(false);
		}
	}
	
	public void resetMarkProcessedForUse(Connection conn) {
		if (useMode == 0)
			return;
		for (int i=0,is=dataList.size();i<is;i++) {
			GpsData data = dataList.get(i);
			if (data != null)
				if (useMode == 1)
					data.setRPDone(false);
				else if (useMode == 2)
					data.setTPDone(false);
		}
	}
	
	public void markProcessedForUse(Connection conn, GpsData data) {
		if (useMode == 0)
			return;
		GpsData currEntry = get(conn, data);
		if (currEntry != null && currEntry.getGps_Record_Time() == data.getGps_Record_Time()) {
			if (useMode == 1)
				currEntry.setRPDone(true);
			else if (useMode == 2)
				currEntry.setTPDone(true);
		}
	}
	
	public MiscInner.Pair getPtCount() {
		if (useMode == 1) {
			return new MiscInner.Pair(currPointsSinceResetRP, pointsSinceResetRP);
		}
		else if (useMode == 2) {
			return new MiscInner.Pair(currPointsSinceResetTP, pointsSinceResetTP);
		}
		else 
			return null;
	}
	
	public void setToPtCount(MiscInner.Pair cnt) {
		if (cnt != null) {
			short v1 = (short) cnt.first;
			short v2 = (short) cnt.second;
			if (useMode == 1) {
				if (v1 > currPointsSinceResetRP)
					currPointsSinceResetRP = v1;
				if (v2 > pointsSinceResetRP)
					pointsSinceResetRP =  v2;
			}
			else if (useMode == 2) {
				if (v1 > currPointsSinceResetTP)
					currPointsSinceResetTP = v1;
				if (v2 > pointsSinceResetTP)
					pointsSinceResetTP =  v2;
			}
		}
	}
	
	public void resetPtCount() {
		if (useMode == 1) {
			currPointsSinceResetRP = 0;
			pointsSinceResetRP = 0;			
		}
		else if (useMode == 2) {
			currPointsSinceResetTP = 0;
			pointsSinceResetTP = 0;			
		}
	}

	public String toString() {
		return "["+vehicleId+","+attributeId+","+(minTime <= 0 ? Long.toString(minTime) : (new java.util.Date(minTime)).toString())+","+(maxTime <= 0 ? Long.toString(maxTime) : (new java.util.Date(maxTime)).toString())+","+dataList.toString();
	}
	public boolean isValidDataSuper(GpsData data, long backTSAllowed) {//if backTSAllowed >= 0 then need to check also if behind channel
	    boolean retval =  maxTime <= 0 || (maxTime-data.getGps_Record_Time()) < (backTSAllowed < g_dcAtMostXMillisecBehindSuper ? g_dcAtMostXMillisecBehindSuper : backTSAllowed);
	    return retval; //RAJEEV 20150310 ... now just relying on 6 days worth of gap
	}
	public boolean isValidDataReg(GpsData data, long backTSAllowed) {//if backTSAllowed >= 0 then need to check also if behind channel
	    boolean retval =  maxTime <= 0 || (maxTime-data.getGps_Record_Time()) < (backTSAllowed < g_dcAtMostXMillisecBehindReg ? g_dcAtMostXMillisecBehindReg : backTSAllowed);
	    return retval; //RAJEEV 20150310 ... now just relying on 6 days worth of gap
	}
	public boolean isValidData(GpsData data, long backTSAllowed) {//NOT to be used backward
		return isValidDataReg(data, backTSAllowed);
	}
	
	
	public void recordTimes(GpsData data, boolean doIfApprop) {
		long ts = data.getGps_Record_Time();
		long ms = ts%1000;
		if (ms != 0) {
			ts = ts/1000 * 1000;
			if (ms > 500)
				ts++;
			data = new GpsData(data);
			data.setGps_Record_Time(ts);
		}
		if (minTime <= 0 || minTime > ts)
			minTime = ts;
		if (maxTime <= 0 || maxTime < ts)
			maxTime = ts;
		ChannelTypeEnum ch = data.getSourceChannel(); 
		if (ChannelTypeEnum.isDataChannel(ch) && (!doIfApprop || latestDataChannelData == null || latestDataChannelData.getGps_Record_Time() < ts)) {
			prevLatestDataChannelData = latestDataChannelData;
			latestDataChannelData = data;
			prevLatestDataChannelName = latestDataChannelName;
			latestDataChannelName = null;
		}
		if (ChannelTypeEnum.isCurrentChannel(ch) && (!doIfApprop || latestCurrentChannelData == null || latestCurrentChannelData.getGps_Record_Time() < ts)) {
			prevLatestCurrentChannelData = latestCurrentChannelData;
			latestCurrentChannelData = data;
			prevLatestCurrentChannelName = latestCurrentChannelName;
			latestCurrentChannelName = null;
		}
		if (!doIfApprop || latestReceivedData == null || latestReceivedData.getGpsRecvTime() < data.getGpsRecvTime()) {
			prevLatestReceivedData = latestReceivedData;
			latestReceivedData = data;
		}
	}
	public void recordName(GpsData data, String name) {//DONT USE
		recordName(null, data, name);
	}
	public void clearRecordName() {
		prevPrevOverallName = null;
		prevPrevNameAtPt = null;
		prevOverallName = null;
		prevNameAtPt = null;
		latestCurrentChannelName = null;
		latestDataChannelName = null;
		prevLatestCurrentChannelName = null;
		prevLatestDataChannelName = null;
	}
	
	public void recordName(Connection conn, GpsData data, String name) {
		if (data == null)
			return;
		GpsData ref = conn == null ? data : get(conn, data);
		if (ref != null && prevNameAtPt != null && ref.getGps_Record_Time() != prevNameAtPt.getGps_Record_Time()) {
			prevPrevOverallName = prevOverallName;
			prevPrevNameAtPt = prevNameAtPt;
		}
		prevOverallName = name;
		prevNameAtPt = ref;

		long ts = data.getGps_Record_Time();
		if (latestCurrentChannelData != null && latestCurrentChannelData.getGps_Record_Time() == ts)
			latestCurrentChannelName = name;
		if (latestDataChannelData != null && latestDataChannelData.getGps_Record_Time() == ts)
			latestDataChannelName = name;
		if (prevLatestCurrentChannelData != null && prevLatestCurrentChannelData.getGps_Record_Time() == ts)
			prevLatestCurrentChannelName = name;
		if (prevLatestDataChannelData != null && prevLatestDataChannelData.getGps_Record_Time() == ts)
			prevLatestDataChannelName = name;
	}
	
	public String getPreviousName(GpsData data) {
		String name = null;
		if (data == null)
			return name;
		long ts = data.getGps_Record_Time();

		if (prevPrevOverallName != null && prevPrevNameAtPt != null && prevPrevNameAtPt.getGps_Record_Time() == data.getGps_Record_Time()) {
			name = prevPrevOverallName;
		}
		else if (prevOverallName != null && prevNameAtPt != null && prevNameAtPt.getGps_Record_Time() == data.getGps_Record_Time()) {
			name = prevOverallName;
		}
		else if (prevLatestCurrentChannelName != null && prevLatestCurrentChannelData != null && prevLatestCurrentChannelData.getGps_Record_Time() == ts)
			name = prevLatestCurrentChannelName;
		else if (prevLatestDataChannelName != null && prevLatestDataChannelData != null && prevLatestDataChannelData.getGps_Record_Time() == ts)
			name = prevLatestDataChannelName;
		else if (latestCurrentChannelName != null && latestCurrentChannelData != null && latestCurrentChannelData.getGps_Record_Time() == ts)
			name = latestCurrentChannelName;
		else if (latestDataChannelName != null && latestDataChannelData != null && latestDataChannelData.getGps_Record_Time() == ts)
			name = latestDataChannelName;
		
		return name;
	}

	public String getPreviousNameIncorrect(GpsData data) {
		String name = null;
		if (data == null)
			return name;
		long ts = data.getGps_Record_Time();

		if (prevNameAtPt != null && prevNameAtPt.getGps_Record_Time() == data.getGps_Record_Time()) {
			prevOverallName = name;
		}
		if (prevLatestCurrentChannelData != null && prevLatestCurrentChannelData.getGps_Record_Time() == ts)
			name = prevLatestCurrentChannelName;
		else if (prevLatestDataChannelData != null && prevLatestDataChannelData.getGps_Record_Time() == ts)
			name = prevLatestDataChannelName;
		else if (latestCurrentChannelData != null && latestCurrentChannelData.getGps_Record_Time() == ts)
			name = latestCurrentChannelName;
		else if (latestDataChannelData != null && latestDataChannelData.getGps_Record_Time() == ts)
			name = latestDataChannelName;
		
		return name;
	}
	
	public long getMinTime() {
		return minTime;
	}
	
	public long getMaxTime() {
		return maxTime;
	}
	
	public long getMinChannelTimeAdjForMaxReg(boolean considerBackPoints) {
		if (Misc.isUndef(maxTime))
			return Misc.getUndefInt();
	
		long currentChannelTime = latestCurrentChannelData != null ? latestCurrentChannelData.getGps_Record_Time() : Misc.getUndefInt();
		long dataChannelTime = latestDataChannelData != null ? latestDataChannelData.getGps_Record_Time() : Misc.getUndefInt();
		
		long mi = currentChannelTime < dataChannelTime ? currentChannelTime : dataChannelTime;
		mi = maxTime - g_dcAtMostXMillisecBehindReg;
		if (mi< minTime || mi <= 0)
			mi = Misc.getUndefInt();
		return mi;
	}
	public long getMinChannelTimeAdjForMaxSuper(boolean considerBackPoints) {
		if (Misc.isUndef(maxTime))
			return Misc.getUndefInt();
	
		long currentChannelTime = latestCurrentChannelData != null ? latestCurrentChannelData.getGps_Record_Time() : Misc.getUndefInt();
		long dataChannelTime = latestDataChannelData != null ? latestDataChannelData.getGps_Record_Time() : Misc.getUndefInt();
		
		long mi = currentChannelTime < dataChannelTime ? currentChannelTime : dataChannelTime;
		mi = maxTime - g_dcAtMostXMillisecBehindSuper;
		
		if (mi< minTime || mi <= 0)
			mi = Misc.getUndefInt();
		return mi;
	}
	public long getMinChannelTimeAdjForMax(boolean considerBackPoints) {//NOT to be used for backward
		return getMinChannelTimeAdjForMaxReg(considerBackPoints);
	}
	public boolean isGpsIdDelta() {
		return (variousFlags & G_CUMMDIST_GPSID_DELTA) != 0;
	}
	
	public boolean isCummDistSensorBased() {
		return (variousFlags & G_CUMMDIST_GPSID_SENSOR_BASED) != 0;
	}
	
	public void setGpsIdDelta(boolean val) {
		if (val) {
			variousFlags |= G_CUMMDIST_GPSID_DELTA;
		}
		else {
			variousFlags &= ~G_CUMMDIST_GPSID_DELTA;
		}
	}
	
	public void setCummDistSensorBased(boolean val) {
		if (val) {
			variousFlags |= G_CUMMDIST_GPSID_SENSOR_BASED;
		}
		else {
			variousFlags &= ~G_CUMMDIST_GPSID_SENSOR_BASED;
		}
	}
	
	public boolean hasCurrentRecord() {
		return (variousFlags & G_CURRENT_RECORD_FLAG) > 0;
	}
	public void setHasCurrentRecord() {
		variousFlags |= G_CURRENT_RECORD_FLAG;
	}
	public void resetHasCurrentRecord() {
		variousFlags &= ~G_CURRENT_RECORD_FLAG;
	}
	
	public long getLatestReceive() {
		return latestReceivedData == null ? Misc.getUndefInt() : latestReceivedData.getGpsRecvTime();
	}
	
	public long getPrevLatestReceive() {
		return prevLatestReceivedData == null ? Misc.getUndefInt() : prevLatestReceivedData.getGpsRecvTime();
	}
	
	//TODO TEMPORARY - need to redo ... we are seeing lots of prev stuff
	//----------------- TEMPORARY -------------------------
	//Below is the last seen - whether transient or otherwise.. AND IS VALID ONLY FOR DIM=0 
	private transient GpsData lastDataChannelTemp = null;
	private transient GpsData lastCurrentChannelTemp = null;
	
	public void setLastDataTemp(GpsData data) {//TEMPORARY ... see latestCurrentChannelData
		if (data == null)
			return;
		if (lastCurrentChannelTemp == null) {
			lastCurrentChannelTemp = data;
			return;
		}
		if (lastDataChannelTemp == null) {
			lastDataChannelTemp = data;
			return;
		}
		//both are non null
		long gapRelDC = data.getGps_Record_Time() - lastDataChannelTemp.getGps_Record_Time();
		long gapRelCC = data.getGps_Record_Time() - lastCurrentChannelTemp.getGps_Record_Time();
		if (gapRelDC == gapRelCC) {
			lastCurrentChannelTemp = data;
		}
		else if (gapRelDC == 0)
			lastDataChannelTemp = data;
		else if (gapRelCC == 0)
			lastCurrentChannelTemp = data;
		else if (gapRelDC > 0 && gapRelCC < 0) {
			lastDataChannelTemp = data;
		}
		else if (gapRelDC < 0 && gapRelCC > 0) {
			lastCurrentChannelTemp = data;
		}
		else if (gapRelDC < 0 && gapRelCC < 0) {
			if (gapRelDC < gapRelCC)
				lastCurrentChannelTemp = data;
			else
				lastDataChannelTemp = data;
		}
		else { //gapRelDC > 0 && gapRelCC > 0
			if (gapRelDC < gapRelCC)
				lastDataChannelTemp = data;
			else
				lastCurrentChannelTemp = data;
		}
		/* old code ... gets screwed up
		if (ChannelTypeEnum.isDataChannel(data.getSourceChannel()))
			lastDataChannelTemp = data;
		if (ChannelTypeEnum.isCurrentChannel(data.getSourceChannel()))
			lastCurrentChannelTemp = data;
			*/
	}
	public GpsData getLastDataTransientSmartTemp(GpsData data) {
		//1 is the last point
		if (data == null)
			return null;
		if (lastDataChannelTemp == null && lastCurrentChannelTemp == null)
			return null;
		if (lastDataChannelTemp == null)
			return lastCurrentChannelTemp.getGps_Record_Time() <= data.getGps_Record_Time() ? lastCurrentChannelTemp : null;
		if (lastCurrentChannelTemp == null)
			return lastDataChannelTemp.getGps_Record_Time() <= data.getGps_Record_Time() ? lastDataChannelTemp : null;
		//bith non null
		long gapRelDC = data.getGps_Record_Time() - lastDataChannelTemp.getGps_Record_Time();
		long gapRelCC = data.getGps_Record_Time() - lastCurrentChannelTemp.getGps_Record_Time();
		if (gapRelDC == gapRelCC) {
			return gapRelDC >=0? lastCurrentChannelTemp : null;
		}
		else if (gapRelDC == 0)
			return lastDataChannelTemp;
		else if (gapRelCC == 0)
			return lastCurrentChannelTemp;
		else if (gapRelDC > 0 && gapRelCC < 0) {
			return lastDataChannelTemp;
		}
		else if (gapRelDC < 0 && gapRelCC > 0) {
			return lastCurrentChannelTemp;
		}
		else if (gapRelDC < 0 && gapRelCC < 0) {
			return null;
		}
		else { //gapRelDC > 0 && gapRelCC > 0
			if (gapRelDC < gapRelCC)
				return lastDataChannelTemp;
			else
				return lastCurrentChannelTemp;
		}
		/*
		GpsData last = null;
		if (ChannelTypeEnum.isDataChannel(data.getSourceChannel()))
			last = lastDataChannelTemp;
		if (ChannelTypeEnum.isCurrentChannel(data.getSourceChannel())) {
			if (last == null)
				last = lastCurrentChannelTemp;
			else if (lastCurrentChannelTemp != null && lastCurrentChannelTemp.getGps_Record_Time() > last.getGps_Record_Time()) {//later of the two
				last = lastCurrentChannelTemp;
			}
		}
		return last;
		*/
	}
	
	// End------------------- TEMPORARY ------------------------------
	public boolean isOdometerSet() {
		return this.odometerDayRecTime >= 0;
	}
	public void setCurrentOdometerStatus(long day, long week, long month) {
		this.odometerDayRecTime = day;
		this.odometerWeekRecTime = week;
		this.odometerMonthRecTime = month;
	}

	public NewVehicleData(int vehicleId, int attributeId, long mi, long mx, boolean inRecoveryMode, int useMode) {
		this.vehicleId = vehicleId;
		this.attributeId = attributeId;
		this.minTime = mi;
		this.maxTime = mx;
		variousFlags = 0;
		setInRecoveryMode(inRecoveryMode);
		this.useMode = (byte)useMode;
		//if (g_dontLoadRight)
		//	maxTime = minTime;
	}
	
	public void clean() {
		int sz = dataList.size();
		int maxPoints = g_reg_maxPoints*maxCachedPtCountMultiple;
		int maxPointsForForceClean = g_reg_maxPointsForForceClean*maxCachedPtCountMultiple;
		if (isInRecoveryMode()) {
			maxPoints = g_reco_maxPoints*maxCachedPtCountMultiple;
			maxPointsForForceClean = g_reco_maxPointsForForceClean*maxCachedPtCountMultiple;
		}
		
		if (sz > maxPoints) {
			
			int desiredLHSCountBeforeAdd = maxPoints/6;
			
			if (prevAddAt == 0)
				return;
			int indexToRemoveFrom = prevAddAt;
			boolean doCleanUp = sz > maxPointsForForceClean;
			if (!doCleanUp) {
				//1. we must be going forward
				if (this.latestReceivedData != null && this.prevLatestReceivedData != null) {
					long gp = this.latestReceivedData.getGps_Record_Time() - this.prevLatestReceivedData.getGps_Record_Time();
					if (gp > 0 && prevAddAt >= sz-desiredLHSCountBeforeAdd) {
						//either gap < 10 min or these two points must be consecutive
						if (gp >= 10*60*1000)
							doCleanUp = true;
						else {
							GpsData prevToAdd = dataList.get(prevAddAt-1);
							if (prevToAdd != null && prevLatestReceivedData.getGps_Record_Time() == prevToAdd.getGps_Record_Time()) {
								doCleanUp = true;
							}
						}
						if (doCleanUp)
							indexToRemoveFrom = prevReadAt;
						if (indexToRemoveFrom < prevAddAt)
							indexToRemoveFrom = prevAddAt;
					}
				}
			}
			if (!doCleanUp)
				return;
			int ptsToRemove = indexToRemoveFrom - desiredLHSCountBeforeAdd;
			if (ptsToRemove <= 0)
				ptsToRemove = 1;
			else if (ptsToRemove > (sz - maxPoints*2/3))
				ptsToRemove = sz - maxPoints*2/3;
			if (ptsToRemove > 0) {
				dataList.removeFromStart(ptsToRemove);
				prevAddAt -= ptsToRemove;
				prevReadAt -= ptsToRemove;
			}
		}
	}
	
	public void remove(GpsData data) {
		Pair<Integer, Boolean> idx = dataList.indexOf(data);
		if (idx.second)
			dataList.remove(idx.first);
	}
	
	public final static boolean  G_DO_OPTIMIZED = true;
	public static class AddnlPointsOnAdd {
		private GpsData leftPtBeingReplaced = null;
		private GpsData righPtBeingReplaced = null;
		private GpsData leftPtToBeAdded = null;
		private GpsData rightPtToBeAdded = null;
		private GpsData oldPtBeingReplaced = null;
		private boolean noPtToBeAdded = false;
		private boolean ptAlreadyExists = false;
		private boolean leftPtMayBeImpacted = false;
		public void clear() {
			leftPtBeingReplaced = null;
			righPtBeingReplaced = null;
			leftPtToBeAdded = null;
			rightPtToBeAdded = null;
			oldPtBeingReplaced = null;
			noPtToBeAdded = false;
			ptAlreadyExists = false;
			this.leftPtMayBeImpacted = false;
		}
		
		
		public GpsData getLeftPtBeingReplaced() {
			return leftPtBeingReplaced;
		}
		public void setLeftPtBeingReplaced(GpsData leftPtBeingReplaced) {
			this.leftPtBeingReplaced = leftPtBeingReplaced;
		}
		public GpsData getRighPtBeingReplaced() {
			return righPtBeingReplaced;
		}
		public void setRighPtBeingReplaced(GpsData righPtBeingReplaced) {
			this.righPtBeingReplaced = righPtBeingReplaced;
		}
		public GpsData getLeftPtToBeAdded() {
			return leftPtToBeAdded;
		}
		public void setLeftPtToBeAdded(GpsData leftPtToBeAdded) {
			this.leftPtToBeAdded = leftPtToBeAdded;
		}
		public GpsData getRightPtToBeAdded() {
			return rightPtToBeAdded;
		}
		public void setRightPtToBeAdded(GpsData rightPtToBeAdded) {
			this.rightPtToBeAdded = rightPtToBeAdded;
		}
		public boolean isNoPtToBeAdded() {
			return noPtToBeAdded;
		}
		public void setNoPtToBeAdded(boolean noPtToBeAdded) {
			this.noPtToBeAdded = noPtToBeAdded;
		}
		public GpsData getOldPtBeingReplaced() {
			return oldPtBeingReplaced;
		}
		public void setOldPtBeingReplaced(GpsData oldPtBeingReplaced) {
			this.oldPtBeingReplaced = oldPtBeingReplaced;
		}
		public boolean isPtAlreadyExists() {
			return ptAlreadyExists;
		}
		public void setPtAlreadyExists(boolean ptAlreadyExists) {
			this.ptAlreadyExists = ptAlreadyExists;
		}


		public boolean isLeftPtMayBeImpacted() {
			return leftPtMayBeImpacted;
		}


		public void setLeftPtMayBeImpacted(boolean leftPtMayBeImpacted) {
			this.leftPtMayBeImpacted = leftPtMayBeImpacted;
		}
	}
	private void validateAdd(GpsData data) {
		boolean gpsIdDelta = this.isGpsIdDelta();
		boolean gpsIdSensorBased = this.isCummDistSensorBased();
		if (data != null) {
			Pair<Integer, Boolean> idx = dataList.indexOf(data);
			if (!idx.second) {
				GpsData prev = dataList.get(idx.first);
				GpsData next = dataList.get(idx.first+1);
				if (prev == null || next == null || !prev.isMergeable(data, gpsIdDelta, gpsIdSensorBased) || !next.isMergeable(data, gpsIdDelta, gpsIdSensorBased)) {
					int dbg = 1;
					dbg++;
					//System.out.println("[ADD_ERROR]"+data+" [p]"+prev+" [n]"+next);
				}
			}
		}
		GpsData prev = null;
		for (int i=0,is=dataList.size();i<is;i++) {
			GpsData curr = dataList.get(i);
			GpsData next = dataList.get(i+1);
			if (prev != null && next != null && prev.isMergeable(curr, gpsIdDelta, gpsIdSensorBased) && next.isMergeable(curr, gpsIdDelta, gpsIdSensorBased)) {
				int dbg = 1;
				dbg++;
			}
			prev = curr;
		}
	}
	
	
	public long getPrevAttributeValueChangeTime(Connection conn, GpsData curr) {
		for (int i=0;;i--) {
			GpsData prev = get(conn,curr,i);
			if (prev == null)
				return Misc.getUndefInt();
			else if (!Misc.isEqual(prev.getValue(),curr.getValue()))
				return prev.getGps_Record_Time();
		}
	}
	
	public void addTransient(GpsData data) {
		if (transientList == null)
			transientList = new ArrayList<GpsData>();
		int addAfter = transientList.size()-1;
		GpsData prev = null;
		for (;addAfter>=0;addAfter--) {
			GpsData temp = transientList.get(addAfter);
			if (temp.getGps_Record_Time() <= data.getGps_Record_Time()) {
				prev = temp;
				break;
			}
			prev = temp;
		}
		
		if (prev != null && prev.getGps_Record_Time() == data.getGps_Record_Time())
			return;
		if (prev != null && !Misc.isEqual(prev.getValue(), data.getValue())) {
			transientList.clear();
			transientList.add(data);
			return;
		}
		if (addAfter == transientList.size()-1)
			transientList.add(data);
		else
			transientList.add(addAfter+1, data);
	}
	public void clearTransient(GpsData data) {
		if (transientList != null)
			transientList.clear();
	}
	public boolean isTransient(Connection conn, GpsData thisData, int transientSec) {
		double v = thisData.getValue();
		GpsData latestPtOfEqualValue = null;
		int currTransSec = 0;
		Pair<Integer, Boolean> idx = indexOf(conn, thisData);
		int pos = idx.first;
		GpsData ptInListPrev = this.dataList.get(pos);
		
		for (int i = transientList == null ? -1 : transientList.size()-1; i>=0; i--) {
			GpsData prevData = transientList.get(i);
			if (ptInListPrev != null && ptInListPrev.getGps_Record_Time() > prevData.getGps_Record_Time())
				break;
			if (Misc.isEqual(prevData.getValue(), v)) {
				latestPtOfEqualValue = prevData;
				int gap = (int)(thisData.getGps_Record_Time() - prevData.getGps_Record_Time())/1000;
				currTransSec += gap;
				if (currTransSec > transientSec)
					break;
			}
		}
		if (currTransSec > transientSec)
			return false;
		int prevIndex = 0;
		for(GpsData prevData = get(conn, thisData); prevData != null; prevData = get(conn, thisData, --prevIndex)) {
			if (Misc.isEqual(prevData.getValue(), v)) {
				int gap = (int)(thisData.getGps_Record_Time() - prevData.getGps_Record_Time())/1000;
				currTransSec += gap;
				if (currTransSec > transientSec)
					break;
			}
			else 
				break;
		}
		if (currTransSec > transientSec) {//we dont need to remove
			return false;
		}
		return true;
	}
	
	public void add(Connection conn, GpsData data) {
		if (data == null)
			return;
		long perf0 = System.nanoTime();
		if (this.transientList != null)
			transientList.clear();
		boolean gpsIdDelta = this.isGpsIdDelta();
		boolean gpsIdSensorBased = this.isCummDistSensorBased();
		if (isAtEnd(data)) {
			if (this.currPointsSinceResetRP < Short.MAX_VALUE)
				this.currPointsSinceResetRP++;
			if (this.currPointsSinceResetTP < Short.MAX_VALUE)
				this.currPointsSinceResetTP++;
		}
		else {
			if (this.pointsSinceResetRP < Short.MAX_VALUE)
				this.pointsSinceResetRP++;
			if (this.pointsSinceResetTP < Short.MAX_VALUE)
				this.pointsSinceResetTP++;
		}
		internalLatestRecvTime = data.getGpsRecvTime();
		internalLatestRecordTime = data.getGps_Record_Time();
		AddnlPointsOnAdd retval = addnlPointsOnAdd;
		retval.clear();
		
		Pair<Integer, Boolean> idx = indexOf(conn, data);
		int pos = idx.first;
		
		boolean isSame = idx.second;
		if (!isSame) {
			this.get(conn, pos, +2, data, true);
			idx = indexOf(conn, data);
			pos = idx.first;
			isSame = idx.second;
		}
		if (!isSame) {
			this.get(conn, pos, -1, data, true);
			idx = indexOf(conn, data);
			pos = idx.first;
			isSame = idx.second;
		}
		boolean done = false;
		if (isSame) {
			GpsData currPt = dataList.get(pos);
			if (false && currPt != null && currPt.isDifferent(data)) {//TODO - fix for different point at same time - best to remove it and add new pt
				                                                                                                           //but need to make changes in DataCache 
				prevAddAt = pos-1;
				dataList.remove(pos);
				if (retval != null)
					retval.setOldPtBeingReplaced(currPt);
				isSame = false;
				idx.first = pos-1;
				idx.second = false;
			}
			else {
				prevAddAt = pos;
				done = true;
				if (retval != null)
					retval.setPtAlreadyExists(true);
			}
		}
		if (!done) {
			if (G_DO_OPTIMIZED && (attributeId == 0 || !isInRecoveryMode())) {//DEBUG15 till 20190106 did not have after && clause
				GpsData leftBeingReplaced = null;
				GpsData rightBeingReplaced = null;
				GpsData leftToBeAdded = null;
				GpsData rightToBeAdded = null;
				
				//esnure that there is at least a point  after
				
				GpsData prev = dataList.get(pos);
				GpsData next = dataList.get(pos+1);
				boolean prevMergeAble = prev != null && prev.isMergeable(data, gpsIdDelta, gpsIdSensorBased);
				boolean nextMergeAble = next != null && next.isMergeable(data, gpsIdDelta, gpsIdSensorBased);
				if (!prevMergeAble &&!nextMergeAble) {
					//this point different from prev and next .. 
					boolean prevMergeAbleWithNext = prev != null && next != null && prev.isMergeable(next, gpsIdDelta, gpsIdSensorBased);
					if (prevMergeAbleWithNext) { //we are going to add intervening point - so let us insert the latest point that could have same value and
						if (prev.isFWPoint()) { 
							//get from DB the point just before current and the point just after current ... then get added to List
							boolean ptsAdded = this.helperAddDifferentValueBetweenMergeValues(conn, pos,data, true/*!isInRecoveryMode()*/, retval);
							if (ptsAdded)
								pos++;
						}
					}
				}
				else if (prevMergeAble&& !nextMergeAble) {
					GpsData prevPrev = dataList.get(pos-1);
					boolean prevPrevMergeAble = prevPrev != null && prevPrev.isMergeable(prev, gpsIdDelta, gpsIdSensorBased);
					if (prevPrevMergeAble) {
						//replace prev with current
						if (!Misc.isEqual(prevPrev.getValue(), prev.getValue()))
							retval.setLeftPtMayBeImpacted(true);
						leftBeingReplaced = dataList.get(pos);
						dataList.replaceAt(pos, data);
						prevPrev.setFWPoint(true);
						done = true;
						prevAddAt = pos;
					}
					else {
						//need to add
					}
				}
				else if (!prevMergeAble && nextMergeAble) {
					this.get(conn, pos, +2, data, true); //ensure that point is loaded if necessary
					GpsData nextNext = dataList.get(pos+2);
					boolean nextNextMergeAble = nextNext != null && nextNext.isMergeable(next, gpsIdDelta, gpsIdSensorBased);
					if (nextNextMergeAble) {
						//replace prev with current
						rightBeingReplaced = dataList.get(pos+1);
						dataList.replaceAt(pos+1, data);
						data.setFWPoint(true);
						//next.setFWPoint(true);
						done = true;
						prevAddAt = pos+1;
					}
					else {
						//need to add
					}
				}
				else if (prevMergeAble && nextMergeAble) { //do nothing ..
					prev.setFWPoint(true);
					prevAddAt  = pos;
					done = true;
					if (retval != null && data.getDimId() != 0)
						retval.setNoPtToBeAdded(true);
				}
				if (retval != null && (leftBeingReplaced != null || rightBeingReplaced !=null)) {
					retval.setLeftPtBeingReplaced(leftBeingReplaced);
					retval.setRighPtBeingReplaced(rightBeingReplaced);
				}
			}
		}
		if (!done) {			
			dataList.addAtIndex(pos+1, data);
			prevAddAt = pos+1;
		}
		recordTimes(data, false);
		clean();
		prevReadAt = prevAddAt;
		//validateAdd(data);
		if (this.attributeId == Misc.DALAUP_DIM_ID && this.dalaUpMgmt != null) {
			this.dalaUpMgmt.resetForNewDalaPt(conn, data.getGps_Record_Time(), this);
		}
		long perf100 = System.nanoTime();
		PerfStat.addVDFFindAndAdd(perf100-perf0);
		return;
	}

	public GpsData getSinglePoint(Connection conn, GpsData data)  {
		return getSinglePoint(conn, data, false);
	}
	public GpsData getSinglePoint(Connection conn, GpsData data, boolean returnPassedData)  {
		return getSinglePoint(conn, data, returnPassedData, false);
	}
	public GpsData getSinglePoint(Connection conn, GpsData data, boolean returnPassedData, boolean doAfterOrEqual)  {
		this.readReq++;
		GpsData retval = null;
		long tsToLookup = data.getGps_Record_Time();
		if (returnPassedData) {
			if (Misc.isUndef(data.getLongitude()) || Misc.isUndef(data.getValue()))
				returnPassedData = false;
		}
		try {
			Pair<Integer, Boolean> idx = null;
			idx = dataList.indexOf(data);
			
			if (!idx.second) {
				int idxInt = idx.first;
				if (doAfterOrEqual && idxInt >= 0)
					idxInt++;
				boolean toLookup = false;
				if (idxInt < 0 && minTime > data.getGps_Record_Time() && minTime > 0 && doAfterOrEqual) {
					tsToLookup = minTime;
					toLookup = true;
				}
				else if (idxInt < 0 && minTime <= data.getGps_Record_Time() && minTime > 0) {
					toLookup = true;
				}
				else if (!doAfterOrEqual && maxTime <= data.getGps_Record_Time() && maxTime > 0) {
					toLookup = true;
					tsToLookup = maxTime;
				}
				else if (idxInt >= dataList.size()-1 && maxTime >= data.getGps_Record_Time() && minTime > 0) {//NOT VERIFIED
					toLookup = true;
				}
				if (toLookup && returnPassedData) {
					retval = data;
				}
				else if (toLookup) {
					long perf0 = System.nanoTime();
					PreparedStatement ps = conn.prepareStatement(!doAfterOrEqual ? NewVehicleData.GET_GPS_DATA_SINGLE : NewVehicleData.GET_GPS_DATA_SINGLE_AFTER_OR_EQUAL);
					ps.setInt(1, vehicleId);
					ps.setInt(2, attributeId);
					ps.setTimestamp(3, Misc.longToSqlDate(tsToLookup));
					ResultSet rs = ps.executeQuery();
					Vehicle vehicle = null;
					GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
					if ((vehicle = reader.readGpsData()) !=null) {
						//vehicle.getGpsData().setName(null);
						retval = vehicle.getGpsData();
					}
					rs.close();
					ps.close();
					PerfStat.addVDFQuery(System.nanoTime() - perf0);
				}
				else {
					retval = dataList.get(idxInt);
				}
			}
			else {
				retval = dataList.get(idx.first); 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	
	public GpsData getSinglePointAfterOrEqual(Connection conn, GpsData data, boolean returnPassedData)  {
		this.readReq++;
		GpsData retval = null;
		if (returnPassedData) {
			if (Misc.isUndef(data.getLongitude()) || Misc.isUndef(data.getValue()))
				returnPassedData = false;
		}
		try {
			Pair<Integer, Boolean> idx = null;
			idx = dataList.indexOf(data);
			
			if (!idx.second) {
				int idxInt = idx.first;
				boolean toLookup = false;
				if (idxInt < 0 && minTime <= data.getGps_Record_Time() && minTime > 0) {
					toLookup = true;
				}
				else if (idxInt >= dataList.size()-1 && maxTime >= data.getGps_Record_Time() && minTime > 0) {//NOT VERIFIED
					toLookup = true;
				}
				if (toLookup && returnPassedData) {
					retval = data;
				}
				else if (toLookup) {
					long perf0 = System.nanoTime();
					PreparedStatement ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SINGLE);
					ps.setInt(1, vehicleId);
					ps.setInt(2, attributeId);
					ps.setTimestamp(3, Misc.longToSqlDate(data.getGps_Record_Time()));
					ResultSet rs = ps.executeQuery();
					Vehicle vehicle = null;
					GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
					if ((vehicle = reader.readGpsData()) !=null) {
						//vehicle.getGpsData().setName(null);
						retval = vehicle.getGpsData();
					}
					rs.close();
					ps.close();
					PerfStat.addVDFQuery(System.nanoTime() - perf0);
				}
				else {
					retval = dataList.get(idxInt);
				}
			}
			else {
				retval = dataList.get(idx.first); 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	public Pair<Integer, Boolean> indexOf(Connection conn, GpsData data) {
		this.readReq++;
		Pair<Integer, Boolean> retval = null;
		retval = dataList.indexOf(data);
		int deltaLookAheadCount = g_reg_deltaLookAheadCount*maxCachedPtCountMultiple;
		if (isInRecoveryMode()) {
			deltaLookAheadCount = g_reco_deltaLookAheadCount*maxCachedPtCountMultiple;
		}

		if (!retval.second) {
			int idxInt = retval.first;
			if (idxInt < 0 && minTime <= data.getGps_Record_Time() && minTime > 0) {
				long currMi = estimateMinDate(data.getGps_Record_Time());
				readDataToLeft(conn, currMi,  data.getGps_Record_Time());
				retval = dataList.indexOf(data);
				idxInt = retval.first;
				if (idxInt < 0 && minTime <= data.getGps_Record_Time() && minTime > 0) 
				{ //read at least 1 pt before so that we can return the prior pt .. dont call get() ... may lead to infinite recursion
					long refTime = dataList.size() == 0 ? data.getGps_Record_Time()+1000 : dataList.get(0).getGps_Record_Time();
					if (minTime > 0 && (dataList.size() == 0 || minTime < dataList.get(0).getGps_Record_Time()) && minTime <= data.getGps_Record_Time()) {
						ArrayList<GpsData> readData = readHelperByCount(conn, refTime, 1, deltaLookAheadCount, false);
						//validateAdd(null);
						
						dataList.mergeAtLeftReverse(readData);
						//validateAdd(null);
						
						prevAddAt += readData.size();
						prevReadAt += readData.size();
					}
					retval = dataList.indexOf(data);
				}
			}
			else if (idxInt >= dataList.size()-1 && maxTime >= data.getGps_Record_Time() && minTime > 0) {//NOT VERIFIED
				long currMx = estimateMaxDate(data.getGps_Record_Time());
				readDataToRight(conn, currMx, data.getGps_Record_Time());
				retval = dataList.indexOf(data);
				idxInt = retval.first;
				int sz = dataList.size();
				if (!retval.second && idxInt >= sz-1  && (sz == 0 || maxTime > dataList.get(sz-1).getGps_Record_Time()) && maxTime >= data.getGps_Record_Time() && minTime > 0) {
					long refTime = sz == 0 ? data.getGps_Record_Time()-1000 : dataList.get(sz-1).getGps_Record_Time();
					//validateAdd(null);
					
					ArrayList<GpsData> readData = readHelperByCount(conn, refTime, 1, deltaLookAheadCount, true);
					dataList.mergeAtRight(readData);
					//validateAdd(null);
					
				}
				retval = dataList.indexOf(data);
			}
		}
		
		if (dataList.size() > this.mxLen)
			mxLen = dataList.size();
		return retval;
	}
	
	public GpsData get(Connection conn, GpsData data) {
		long perf0 = System.nanoTime();
		GpsData retval = get(conn, data, false);
		PerfStat.addVDFGet(System.nanoTime() - perf0);
		return retval;
	}
	
	public GpsData get(Connection conn, GpsData data, boolean strictLess) {
		long perf0 = System.nanoTime();
		Pair<Integer,Boolean> idx = indexOf(conn, data);
		int idxInt =  idx.first;
		if (strictLess && idx.second) {
			GpsData retval = get(conn, idxInt, -1, data, false);
			PerfStat.addVDFGet(System.nanoTime() - perf0);
			return retval;
		}
		else {
			if (idxInt < prevReadAt)
				prevReadAt = idxInt;
			GpsData retval = idxInt < 0 || idxInt >= dataList.size() ? null : dataList.get(idxInt);
			PerfStat.addVDFGet(System.nanoTime() - perf0);
			return retval;
		}
	}
	
	public GpsData get(Connection conn, GpsData data, int relIndex) {
		long perf0 = System.nanoTime();
		Pair<Integer,Boolean> idx = indexOf(conn, data);
		int idxInt =  idx.first;
		GpsData retval = get(conn, idxInt, relIndex, data, false);
		PerfStat.addVDFGet(System.nanoTime() - perf0);
		return retval;
	}
	
	public GpsData get(Connection conn, int dataIndex, int relIndex, GpsData data, boolean addMode) { //this gets regardless of recv time constraint
		
		if (true) {//g_ignoreUpdatedOn ||  this.currentTimingsAreOf == 0 || this.latestReceivedData == null)
			long perf0 = System.nanoTime();
			GpsData retval = getWithoutRecvConstraint(conn, dataIndex, relIndex, data, addMode);
			PerfStat.addVDFGet(System.nanoTime() - perf0);
			return retval;
		}
		int deltaLookAheadCount = g_reg_deltaLookAheadCount*maxCachedPtCountMultiple;
		if (isInRecoveryMode()) {
			deltaLookAheadCount = g_reco_deltaLookAheadCount*maxCachedPtCountMultiple;
		}

		int retvalIndex = dataIndex;
		long lrcv = latestReceivedData.getGpsRecvTime();
		long lgrt = latestReceivedData.getGps_Record_Time();

		if (relIndex == 0) {
		}
		else if (relIndex < 0) {
			relIndex = -1*relIndex;
			int cnt = 0;
			
			while (relIndex > 0) { //changed inside
				cnt++;
				int idx = dataIndex-cnt;
				if (idx < 0) {
					int sz = dataList.size();
					long refTime = sz == 0 ? data.getGps_Record_Time()+1000 : dataList.get(0).getGps_Record_Time();
					if (minTime > 0 && (sz == 0 || minTime < dataList.get(0).getGps_Record_Time()) && minTime <= data.getGps_Record_Time()) {
						ArrayList<GpsData> readData = readHelperByCount(conn, refTime, relIndex, deltaLookAheadCount, false);
						dataList.mergeAtLeftReverse(readData);
						idx += readData.size();
						dataIndex += readData.size();
						prevAddAt += readData.size();
						prevReadAt += readData.size();
					}
				}//if needed to read
				GpsData temp = dataList.get(idx);
				if (temp == null)
					break;
				long drcv = temp.getGpsRecvTime();
				long dgrt = temp.getGps_Record_Time();
				if (drcv < lrcv || (drcv == lrcv && dgrt <= lgrt)) {
					relIndex--;
				}
			}
			retvalIndex = dataIndex - cnt;
		}//if asking data from left
		else {//relIndex > 0
			int cnt = 0;
			while (relIndex > 0) { //changed inside
				cnt++;
				int idx = dataIndex+cnt;
				int sz = dataList.size();
				
				if (idx >= sz) {
					long refTime = sz == 0 ? data.getGps_Record_Time()-1000 : dataList.get(sz-1).getGps_Record_Time();
					if (maxTime > 0 && maxTime > dataList.get(sz-1).getGps_Record_Time() && maxTime >= data.getGps_Record_Time()) {
						ArrayList<GpsData> readData = readHelperByCount(conn, refTime, relIndex, deltaLookAheadCount, true);
						dataList.mergeAtRight(readData);
					}
				}//if needed to read
				GpsData temp = dataList.get(idx);
				if (temp == null)
					break;
				long drcv = temp.getGpsRecvTime();
				long dgrt = temp.getGps_Record_Time();
				if (drcv < lrcv || (drcv == lrcv && dgrt <= lgrt)) {
					relIndex--;
				}
			}
			retvalIndex = dataIndex + cnt;
		}//if asking daa from right
		
		int ti  = retvalIndex<0 || retvalIndex>=dataList.size() ? prevReadAt : retvalIndex;
		if (ti < prevReadAt)
			prevReadAt = ti;
		return retvalIndex < 0 || retvalIndex >= dataList.size() ? null : dataList.get(retvalIndex);
	}
	
	public GpsData getWithoutRecvConstraint(Connection conn, int dataIndex, int relIndex, GpsData data, boolean addMode) { //this gets regardless of recv time constraint
		int ask = dataIndex+relIndex;
		int sz = dataList.size();
		GpsData retval = null;
		int deltaLookAheadCount = g_reg_deltaLookAheadCount*maxCachedPtCountMultiple;
		if (isInRecoveryMode()) {
			deltaLookAheadCount = g_reco_deltaLookAheadCount*maxCachedPtCountMultiple;
		}

		if (ask < 0){
			long refTime = sz == 0 ? data.getGps_Record_Time()+1000 : dataList.get(0).getGps_Record_Time();
			if (minTime > 0 && (sz == 0 || minTime < dataList.get(0).getGps_Record_Time()) && minTime <= data.getGps_Record_Time()) {
				ArrayList<GpsData> readData = readHelperByCount(conn, refTime, -1*ask, deltaLookAheadCount, false);
				//validateAdd(null);
				
				dataList.mergeAtLeftReverse(readData);
				//validateAdd(null);
				ask += readData.size();
				prevAddAt += readData.size();
				prevReadAt += readData.size();
			}
		}
		else if (ask >= sz) {
			long refTime = sz == 0 ? data.getGps_Record_Time()-1000 : dataList.get(sz-1).getGps_Record_Time();
			if (maxTime > 0 && (sz == 0 || maxTime > dataList.get(sz-1).getGps_Record_Time()) && maxTime >= data.getGps_Record_Time()) {
				ArrayList<GpsData> readData = readHelperByCount(conn, refTime, ask-sz-1, deltaLookAheadCount, true);
				//validateAdd(null);
				
				dataList.mergeAtRight(readData);
				//validateAdd(null);
				
			}
		}
		
		int ti  = ask<0 || ask>=dataList.size() ? prevReadAt : ask;
		if (ti < prevReadAt)
			prevReadAt = ti;
		retval =ask < 0 || ask >= dataList.size() ? null : dataList.get(ask);
		if (retval == null)// && !addMode)
			this.regetMiMx(conn);
		return retval;
	}
	
	public static double getDirectionBetween(Point prev, Point curr) {
		Pair<Double,Double> distOrient = TrackMisc.getSimpleDistAndAizmuth(prev, curr);
		return distOrient.second;	
	}
	
	public DirResult helpGetDirChange(Connection conn, GpsData ref, double lonBoundLo, double latBoundLo, double lonBoundHi, double latBoundHi, DirTimeInfo refTimeInfo, boolean doingLoHi, double distThresh, long timeThreshMS) {
		//returns 0 if no change, 1 if at lo dist, 2 if at hi dist, 3 if both lo and hi
		GpsData loEntry = null;
		GpsData hiEntry = null;
		GpsData loExit = null;
		GpsData hiExit = null;
		if (refTimeInfo != null) {
			if (doingLoHi) {
				loEntry = refTimeInfo.getInLoDir(conn, this);
				hiEntry = refTimeInfo.getInHiDir(conn, this);
				loExit = refTimeInfo.getOutLoDir(conn, this);
				hiExit = refTimeInfo.getOutHiDir(conn, this);
			}
			else {
				loEntry = refTimeInfo.getInHiDir(conn, this);
				hiEntry = refTimeInfo.getInSuperDir(conn, this);
				loExit = refTimeInfo.getOutHiDir(conn, this);
				hiExit = refTimeInfo.getOutSuperDir(conn, this);
			}
		}
		//Get entry points
		int prevIndex = loEntry == null ? -1 : 0;
		double refDist = ref.getValue();
		long refTS = ref.getGps_Record_Time();
		boolean loTermThresh = false;
		boolean hiTermThresh = false;
		if (loEntry == null || hiEntry == null) {
			GpsData prev = loEntry;
			GpsData prevWithRef = loEntry == null ? ref : loEntry;
			GpsData lastPoint = null;
			for (prev = get(conn, prevWithRef,prevIndex); prev != null; prev = get(conn, prevWithRef, prevIndex)) {
				if (loEntry == null) {
					if (!isGpsInBound(prev, ref, latBoundLo, lonBoundLo)) {
						loEntry = prev;
					}
				}
				if (loEntry != null) {
					if (!isGpsInBound(prev, ref, latBoundHi, lonBoundHi)) {
						hiEntry = prev;
					}
				}
				if (hiEntry != null)
					break;
				if ((refDist > 0 && (refDist-prev.getValue()) > distThresh) || (refTS > 0 && (refTS-prev.getGps_Record_Time()) > timeThreshMS)) {
					if (loEntry == null)
						loTermThresh = true;
					if (hiEntry == null)
						hiTermThresh = true;
					break;
				}

				prevIndex--;
				lastPoint = prev;
				
				
			}
	//		if (hiEntry == null)
	//			hiEntry = lastPoint;
	//		if (loEntry == null)
	//			loEntry = lastPoint;
			
		}
		int hiEntryIndex = -1*prevIndex;
		prevIndex = loExit == null ? 1 : 0;
		if (loExit == null || hiExit == null) {
			GpsData prev = loExit;
			GpsData lastPoint = null;
			GpsData prevWithRef = loExit == null ? ref : loExit;
			for (prev = get(conn, prevWithRef,prevIndex); prev != null; prev = get(conn, prevWithRef, prevIndex)) {

				if (loExit == null) {
					if (!isGpsInBound(prev, ref, latBoundLo, lonBoundLo)) {
						loExit = prev;
					}
				}
				if (loExit != null) {
					if (!isGpsInBound(prev, ref, latBoundHi, lonBoundHi)) {
						hiExit = prev;
					}
				}
				if (hiExit != null)
					break;
				if ((refDist > 0 && (prev.getValue()-refDist) > distThresh) || (refTS > 0 && (prev.getGps_Record_Time()-refTS) > timeThreshMS)) {
					if (loExit == null)
						loTermThresh = true;
					if (hiExit == null)
						hiTermThresh = true;
					break;
				}

				prevIndex++;
				lastPoint = prev;
			}
	//		if (hiExit == null)
	//			hiExit = lastPoint;
	//		if (loExit == null)
	//			loExit = lastPoint;
	//		if (hiEntry == null || hiExit == null)
	//			return null;
		}
		int hiExitIndex = prevIndex;
		if (refTimeInfo !=null){
			refTimeInfo.setInHiDir(hiEntry);
			refTimeInfo.setInLoDir(loEntry);
			refTimeInfo.setOutHiDir(hiExit);
			refTimeInfo.setOutLoDir(loExit);
			refTimeInfo.setInLoInvalid(false);
			refTimeInfo.setInHiInvalid(false);
		}

		Point intersectLoEntry = loEntry == null ? null : getIntersectingPoint(conn, ref, loEntry, lonBoundLo, latBoundLo, true);
		Point intersectHiEntry = hiEntry == null ? null : getIntersectingPoint(conn, ref, hiEntry, lonBoundHi, latBoundHi, true);
		Point intersectLoExit = loExit == null ? null : getIntersectingPoint(conn, ref, loExit, lonBoundLo, latBoundLo, false);
		Point intersectHiExit = hiExit == null ? null : getIntersectingPoint(conn, ref, hiExit, lonBoundHi, latBoundHi, false);
		Point refPoint = ref.getPoint();
		double loInDir = intersectLoEntry == null ? Misc.getUndefDouble() : getDirectionBetween(intersectLoEntry, refPoint);
		double hiInDir = intersectHiEntry == null ? Misc.getUndefDouble() : getDirectionBetween(intersectHiEntry, refPoint);
		double loOutDir = intersectLoExit == null ? Misc.getUndefDouble() : getDirectionBetween(refPoint, intersectLoExit);
		double hiOutDir = intersectHiExit == null ? Misc.getUndefDouble() : getDirectionBetween(refPoint, intersectHiExit);
		//reusing intersectLoEntry & intersectHiEntry
		if (intersectLoEntry != null && intersectLoExit != null) { 
			double tlx = (intersectLoEntry.getX()+intersectLoExit.getX())/2.0;
			double tly = (intersectLoEntry.getY()+intersectLoExit.getY())/2.0;
			intersectLoExit.setX(tlx);
			intersectLoExit.setY(tly);
		}
		else {
			intersectLoEntry = null;
		}
		if (intersectHiEntry != null && intersectHiExit != null) {
			double thx = (intersectHiEntry.getX()+intersectHiExit.getX())/2.0;
			double thy = (intersectHiEntry.getY()+intersectHiExit.getY())/2.0;
			intersectHiExit.setX(thx);
			intersectHiExit.setY(thy);
		}
		else {
			intersectHiEntry = null;
		}
		DirResult retval = new DirResult(loInDir, hiInDir, loOutDir, hiOutDir, intersectLoExit, intersectHiExit, hiEntryIndex, hiExitIndex, loTermThresh, hiTermThresh);
		return retval;
	}
	
	public void helpGetNextHiDirChange(Connection conn, GpsData ref, double lonBound, double latBound, DirResult result, DirTimeInfo refTimeInfo, double distThresh, long timeThreshMS) {
		//returns 0 if no change, 1 if at lo dist, 2 if at hi dist, 3 if both lo and hi
		if (result == null)
			return;
		GpsData hiEntry = null;
		GpsData hiExit = null;
		if (refTimeInfo != null) {
			hiEntry = refTimeInfo.getInSuperDir(conn, this);
			hiExit = refTimeInfo.getOutSuperDir(conn, this);
		}
		//Get entry points
		int hiEntryIndex = -1;
		double refDist = ref.getValue();
		double refTS = ref.getGps_Record_Time();
		boolean hiTermThresh = false;
		if (hiEntry == null) {
			GpsData prev = null;
			GpsData lastPoint = null;
			int prevIndex = result.getPrevHiEntryIndex() <  0 ? -1 :-1* result.getPrevHiEntryIndex();
			GpsData refPrev =ref;
			GpsData inHiDir = refTimeInfo == null ? null : refTimeInfo.getInHiDir(conn, this);
			if (prevIndex < 0 && inHiDir != null) {
				prevIndex= 0;
				refPrev = inHiDir;
			}
			
			for (prev = get(conn, refPrev,prevIndex); prev != null; prev = get(conn, refPrev, prevIndex)) {
				if (!isGpsInBound(prev, ref, latBound, lonBound)) {
					hiEntry = prev;
					break;
				}
				if ((refDist > 0 && (refDist - prev.getValue()) > distThresh) || (refTS > 0 && (refTS - prev.getGps_Record_Time()) > timeThreshMS)) {
					
					if (hiEntry == null)
						hiTermThresh = true;
					break;
				}

				prevIndex--;
				lastPoint = prev;
			}
			hiEntryIndex = prevIndex;
		}
		int hiExitIndex = -1;

		if (hiExit == null) {
			GpsData prev = null;
			GpsData lastPoint = null;
			int prevIndex = result.getPrevHiExitIndex() <  0 ? 1 :1* result.getPrevHiExitIndex();
			GpsData refPrev =ref;
			GpsData outHiDir = refTimeInfo != null ? refTimeInfo.getOutHiDir(conn, this) : null;
			if (prevIndex < 0 && outHiDir != null) {
				prevIndex= 0;
				refPrev = outHiDir;
			}
			for (prev = get(conn, refPrev,prevIndex); prev != null; prev = get(conn, refPrev, prevIndex)) {
				if (!isGpsInBound(prev, ref, latBound, lonBound)) {
					hiExit = prev;
				}
				if (hiExit != null)
					break;
				if ((refDist > 0 && (prev.getValue()-refDist) > distThresh) || (refTS > 0 && (prev.getGps_Record_Time()-refTS) > timeThreshMS)) {
					
					if (hiExit == null)
						hiTermThresh = true;
					break;
				}
				prevIndex++;
				lastPoint = prev;
			}
			hiExitIndex = prevIndex;
		}
		if (refTimeInfo != null) {
			refTimeInfo.setInSuperDir(hiEntry);
			refTimeInfo.setOutSuperDir(hiExit);
			refTimeInfo.setInSuperInvalid(false);
		}
		Point intersectHiEntry = hiEntry == null ? null : getIntersectingPoint(conn, ref, hiEntry, lonBound, latBound, true);
		Point intersectHiExit = hiExit == null ? null : getIntersectingPoint(conn, ref, hiExit, lonBound, latBound, false);
		Point refPoint = ref.getPoint();
		double hiInDir = intersectHiEntry == null ? Misc.getUndefDouble() : getDirectionBetween(intersectHiEntry, refPoint);
		double hiOutDir = intersectHiExit == null ? Misc.getUndefDouble(): getDirectionBetween(refPoint, intersectHiExit);
		//reusing intersectLoEntry & intersectHiEntry
		if (intersectHiEntry != null && intersectHiExit != null) {
			double thx = (intersectHiEntry.getX()+intersectHiExit.getX())/2.0;
			double thy = (intersectHiEntry.getY()+intersectHiExit.getY())/2.0;
			intersectHiEntry.setX(thx);
			intersectHiEntry.setY(thy);
		}
		else {
			intersectHiEntry = null;
		}
		result.setHiInDir(hiInDir);
		result.setHiOutDir(hiOutDir);
		result.setPrevHiEntryIndex(hiEntryIndex);
		result.setPrevHiExitIndex(hiExitIndex);
		result.setHiAvgInOutPoint(intersectHiEntry);
		result.resetAllDirTerm();
		if (hiTermThresh)
			result.setHiDirTerm();
	}
	
	public Pair<GpsData, Integer> getEntryOrExitPoint(Connection conn, GpsData ref, double latBound, double lonBound, boolean doEntry, double distThresh, long timeThreshMS) {
		return getEntryOrExitPoint(conn, ref, latBound, lonBound, doEntry, true, Misc.getUndefInt(), distThresh, timeThreshMS);
	}
	public Pair<GpsData, Integer> getEntryOrExitPoint(Connection conn, GpsData ref, double latBound, double lonBound, boolean doEntry, boolean doStrict, int prevIndex, double distThresh, long timeThreshMS) {
		if (Misc.isUndef(prevIndex) || (doEntry && prevIndex >= 0) || (!doEntry && prevIndex <= 0))
			prevIndex = doEntry ? -1 : 1;
		int incr = doEntry ? -1 : 1;
		GpsData prev = null;
		GpsData lastPoint = null;
		for (prev = get(conn, ref,prevIndex); prev != null; prev = get(conn, ref, prevIndex)) {
			if ((doEntry && !isGpsInBound(prev, ref, latBound, lonBound)) || (!doEntry && isGpsInBound(prev, ref, latBound, lonBound)))
				return new Pair<GpsData, Integer>(prev, prevIndex);
			prevIndex += incr;
			lastPoint = prev;
		}
		return new Pair<GpsData, Integer>(doStrict ? null : lastPoint, prevIndex);
	}
	
	public Point getIntersectingPoint(Connection conn, GpsData ref, GpsData justOutside, Point box, boolean doEntry) {
		return getIntersectingPoint(conn, ref, justOutside, box.getLongitude(), box.getLatitude(), doEntry);
	}
	
	public Point getIntersectingPoint(Connection conn, GpsData ref, GpsData justOutside, double deltaLon, double deltaLat, boolean doEntry) {
		GpsData justInside = this.get(conn, justOutside,doEntry ?1:-1);
		if (justInside == null)
			return justOutside.getPoint();
		Point retval = null;
		
		double x1 = justInside.getLongitude();
		double y1 = justInside.getLatitude();
		double x2 = justOutside.getLongitude();
		double y2 = justOutside.getLatitude();
		double delY = y2-y1;
		double delX = x2-x1;
		boolean delY0 = Misc.isEqual(delY, 0);
		boolean delX0 = Misc.isEqual(delX, 0);
		
		
		if (delY0 && delX0)
			return justOutside.getPoint();
		double boxXL = ref.getLongitude()-deltaLon;
		double boxXU = ref.getLongitude()+deltaLon;
		double boxYL = ref.getLatitude()-deltaLat;
		double boxYU = ref.getLatitude()+deltaLat;
		
		boolean x2OnLeft = x2 < boxXL && !Misc.isEqual(x2,boxXL);
		boolean x2OnRight = x2 > boxXU && !Misc.isEqual(x2,boxXU);
		
		boolean y2OnBottom = y2 < boxYL && !Misc.isEqual(y2, boxYL); 
		boolean y2OnTop = y2 > boxYU && !Misc.isEqual(y2, boxYU); 
		
		if (delY0) { //horizontal line ..
			return new Point(x2OnLeft ? boxXL : boxXU, y2);
		}
		if (delX0) { //vertical line ..
			return new Point(x2, y2OnBottom? boxYL : boxYU);
		}
		double m = delY/delX;
		double c = y2-m*x2;
		//check against vertical boundaries x=xf
		//left boundary & right boundary
		if (x2OnLeft) {
			double yi = m*boxXL+c;
			if ((yi > boxYL || Misc.isEqual(yi, boxYL)) && (yi < boxYU || Misc.isEqual(yi, boxYU)))
				return new Point(boxXL, yi);
					
		}
		if (x2OnRight) {
			double yi = m*boxXU+c;
			if ((yi > boxYL || Misc.isEqual(yi, boxYL)) && (yi < boxYU || Misc.isEqual(yi, boxYU)))
				return new Point(boxXU, yi);
			
		}
		if (y2OnBottom) {
			double xi = (boxYL-c)/m;
			if ((xi > boxXL || Misc.isEqual(xi, boxXL)) && (xi < boxXU || Misc.isEqual(xi, boxXU)))
				return new Point(xi, boxYL);
		}
		if (y2OnTop) {
			double xi = (boxYU-c)/m;
			if ((xi > boxXL || Misc.isEqual(xi, boxXL)) && (xi < boxXU || Misc.isEqual(xi, boxXU)))
				return new Point(xi, boxYU);
		}
		
		return justOutside.getPoint(); //really should not happen
	}
	public static boolean isGpsInBound(GpsData prev, GpsData ref, double latBound, double lonBound) {
		double minLat = ref.getLatitude()-latBound;
		double maxLat = ref.getLatitude()+latBound;
		double minLon = ref.getLongitude()-lonBound;
		double maxLon = ref.getLongitude()+lonBound;
		double lat = prev.getLatitude();
		double lon = prev.getLongitude();
		return (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon);
	}
	
	public GpsData getEndData() {
		return this.dataList.get(dataList.size()-1);
	}
	public boolean isAtEnd(GpsData data) {
		//kind of complex - if in recoveryMode or mode <= 0 or mode > 2 then simply data.getGps_Record_Time() >= maxTime
		//else if mode = 1 (for RP) - will return true only if all points after this are not RP processed
		//else if mode = 2 (for TP) - will return true only if all points adter this are not TP processed
		long drt = data.getGps_Record_Time();
		return drt >= this.maxTime;
		/*
		if (!inRecoveryMode || mode <= 0 || mode > 2) {
			return drt >= maxTime;
		}
		else if (mode == 1) {
			int prevIndex = 0;
			for (GpsData curr = get(conn, data, prevIndex); curr != null; curr = get(conn, data, ++prevIndex)) {
				if (curr.getGps_Record_Time() >= drt) {
					if (curr.isRPDone())
						return false;
				}
			}
			return true;
		}
		else {
			int prevIndex = 0;
			for (GpsData curr = get(conn, data, prevIndex); curr != null; curr = get(conn, data, ++prevIndex)) {
				if (curr.getGps_Record_Time() >= drt) {
					if (curr.isTPDone())
						return false;
				}
			}
			return true;
		}
		*/
	}
	public GpsData getLast(Connection conn) { //Assume
		GpsData last =  dataList.get(dataList.size()-1);
		if ((last == null && maxTime > 0) || (last != null && last.getGps_Record_Time() < maxTime)) {
			GpsData dummy = new GpsData(maxTime);
			last =  get(conn, dummy);	
		}
		return last;
	}
	public GpsData getFirst(Connection conn) { //Assume
		GpsData first =  dataList.get(0);
		if ((first == null && minTime > 0) || (first != null && first.getGps_Record_Time() > minTime)) {
			GpsData dummy = new GpsData(minTime);
			first =  get(conn, dummy);	
		}
		return first;
	}

	public GpsData simpleGet(int index) {
		return dataList.get(index);
	}
	public void simpleReplaceAt(int index, GpsData data) {
		dataList.replaceAt(index,data);
	}
	public int simpleSize() {
		return dataList.size();
	}
	public void resetTimes() {
		odometerWeekRecTime = Misc.getUndefInt();
		odometerDayRecTime = Misc.getUndefInt();
		odometerMonthRecTime = Misc.getUndefInt();
		this.odometerDayGps = null;
		this.odometerMonthGps = null;
		this.odometerWeekGps = null;

		minTime = -1;
		maxTime = -1;
		this.internalLatestRecordTime = -1;
		this.internalLatestRecvTime = -1;
		this.prevAddAt = -1;
		this.prevLatestCurrentChannelData = null;
		this.prevLatestCurrentChannelName = null;
		this.prevLatestDataChannelData = null;
		this.prevLatestDataChannelName = null;
		this.prevLatestReceivedData = null;
		this.prevReadAt = -1;
		this.lastCurrentChannelTemp = null;
		this.lastDataChannelTemp = null;
		this.latestCurrentChannelData = null;
		this.latestCurrentChannelName = null;
		this.latestDataChannelData = null;
		this.latestDataChannelName = null;
		this.latestReceivedData = null;
		dataList.clear();
	}
	public void setRegetMiMx() {
		regetMiMxDone = false;
	}
	public void regetMiMx(Connection conn) {
		if (this.regetMiMxDone)
			return;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			long perf0 = System.nanoTime();
			ps=conn.prepareStatement("select min(gps_record_time), max(gps_record_time) from logged_data where vehicle_id=? and attribute_id=?");
			ps.setInt(1, vehicleId);
			ps.setInt(2, attributeId);
			long mi = 0;
			long mx = 0;
			rs = ps.executeQuery();
			if (rs.next()) {
				mi = Misc.sqlToLong(rs.getTimestamp(1));
				mx = Misc.sqlToLong(rs.getTimestamp(2));
			}
			if (mi > this.minTime)
				minTime = mi;
			if (mx < this.maxTime)
				maxTime = mx;
			this.regetMiMxDone = true;
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
	}
	public void reinit(Connection conn) {
		resetTimes();
		dataList.clear();
		init(conn);
	}
	public void setMinMaxTime(long mi, long mx) {
		this.minTime = mi;
		this.maxTime = mx;
	}
	public void init(Connection conn) {
		if (minTime >= 0 || maxTime >= 0)
			return;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			long perf0 = System.nanoTime();
			ps=conn.prepareStatement("select min(gps_record_time), max(gps_record_time) from logged_data where vehicle_id=? and attribute_id=?");
			ps.setInt(1, vehicleId);
			ps.setInt(2, attributeId);
			minTime = 0;
			maxTime = 0;
			rs = ps.executeQuery();
			if (rs.next()) {
				minTime = Misc.sqlToLong(rs.getTimestamp(1));
				maxTime = Misc.sqlToLong(rs.getTimestamp(2));
			}
			
			rs.close();
			ps.close();
			rs = null;
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
	}
	
	private long estimateMinDate(long askTime) {
		int deltaLookAheadCount = g_reg_deltaLookAheadCount*maxCachedPtCountMultiple;
		if (isInRecoveryMode()) {
			deltaLookAheadCount = g_reco_deltaLookAheadCount*maxCachedPtCountMultiple;
		}

		return askTime - (isInRecoveryMode() ? g_reco_deltaLookAheadSec*maxCachedPtCountMultiple : g_reg_deltaLookAheadSec*maxCachedPtCountMultiple)*1000;
	}
	private long estimateMaxDate(long askTime) {
		return askTime +(isInRecoveryMode() ? g_reco_deltaLookAheadSec*maxCachedPtCountMultiple : g_reg_deltaLookAheadSec*maxCachedPtCountMultiple)*1000;
	}
	
	private void readDataToLeft(Connection conn, long mi, long ref) {
		long mx = dataList.size() == 0 ? ref+1000 : dataList.get(0).getGps_Record_Time();
		ArrayList<GpsData> data = readHelperByTimeBound(conn, mi, mx, false);
		//validateAdd(null);
		
		dataList.mergeAtLeftReverse(data);
		//validateAdd(null);
		if (dataList.size() > 0) {//in case of recovery
			long ti = dataList.get(0).getGps_Record_Time();
			if (ti < this.minTime || this.minTime <= 0)
				this.minTime = ti;
		}
		prevAddAt += data.size();
		prevReadAt += data.size();
	}
	private void readDataToRight(Connection conn, long mx, long ref) {
		long mi = dataList.size() == 0 ? ref-1000 : dataList.get(dataList.size()-1).getGps_Record_Time();
		//validateAdd(null);
		
		ArrayList<GpsData> data = readHelperByTimeBound(conn, mi, mx, true);
		//validateAdd(null);
		
		dataList.mergeAtRight(data);
		if (dataList.size() > 0) { //in case of recovery
			long ti = dataList.get(dataList.size()-1).getGps_Record_Time();
			if (ti > this.maxTime || this.maxTime <= 0)
				this.maxTime = ti;
		}
	}
	
		
	@SuppressWarnings("deprecation")
	public Pair<GpsData, GpsData> getImmDataPointsNotUsed(Connection conn, long refTime, int vehicleId, int attributeId, boolean prevPoint) { //for non-zero we will use 0-dim data and then make adjustments
		this.dbReads++;
		PreparedStatement ps = null;
		ResultSet rs = null;
		GpsData prior = null;
		GpsData after = null;

		try {
			long perf0 = System.nanoTime();
			ps = conn.prepareStatement(g_getDataFromDBByUpdatedOn? GET_GPS_DATA_IMM_BY_UPDATE : GET_GPS_DATA_IMM);
			java.sql.Timestamp ts = Misc.utilToSqlDate(refTime);
			java.sql.Timestamp past = Misc.utilToSqlDate(new java.util.Date(90,0,1));
			java.sql.Timestamp tsLatestrecv=null;
			java.sql.Timestamp tsgrtofLatest=null;
			if (g_getDataFromDBByUpdatedOn && internalLatestRecvTime > 0) {
				tsLatestrecv = Misc.utilToSqlDate(internalLatestRecvTime);
				tsgrtofLatest = Misc.utilToSqlDate(internalLatestRecordTime);
			}
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			ps.setTimestamp(colIndex++, ts);
			if (g_getDataFromDBByUpdatedOn) {
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsgrtofLatest);
			}
			ps.setInt(colIndex++, vehicleId); //whereby
			ps.setInt(colIndex++, attributeId);//whereby
			
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			ps.setTimestamp(colIndex++, ts);
			if (g_getDataFromDBByUpdatedOn) {
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsgrtofLatest);
			}
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
			//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
			rs = ps.executeQuery();
			Vehicle vehicle = null;
			GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
			while ((vehicle = reader.readGpsData()) !=null) {
				//vehicle.getGpsData().setName(null);
				GpsData temp = vehicle.getGpsData();
				if (temp.getGps_Record_Time() < refTime)
					prior = temp;
				else
					after = temp;
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
		return new Pair<GpsData, GpsData>(prior, after);
	}

	private Pair<GpsData, GpsData>getImmDataPoints(Connection conn, long refTime, int vehicleId, int attributeId, GpsData leftRangeForImmLookUp, GpsData rightRangeForImmLookUp) { //for non-zero we will use 0-dim data and then make adjustments
		this.dbReads++;
		PreparedStatement ps = null;
		ResultSet rs = null;
		GpsData prior = null;
		GpsData after = null;

		try {
			long perf0 = System.nanoTime();
			ps = conn.prepareStatement(g_getDataFromDBByUpdatedOn? GET_GPS_DATA_IMM_BY_UPDATE : GET_GPS_DATA_IMM);
			java.sql.Timestamp past = Misc.longToSqlDate(System.currentTimeMillis()-2*365*24*3600*1000);
			java.sql.Timestamp future = Misc.longToSqlDate(System.currentTimeMillis()+2*365*24*3600*1000);
			java.sql.Timestamp tsLatestrecv = internalLatestRecvTime > 0 ? Misc.utilToSqlDate(internalLatestRecvTime) : future;
			java.sql.Timestamp lowerBoundGPS = leftRangeForImmLookUp != null ? Misc.longToSqlDate(leftRangeForImmLookUp.getGps_Record_Time())
					 : past;
			java.sql.Timestamp upperBoundGPS = rightRangeForImmLookUp != null ? Misc.longToSqlDate(rightRangeForImmLookUp.getGps_Record_Time())
					 : future;
			
			java.sql.Timestamp tsgrtofLatest = null;

			java.sql.Timestamp ts = Misc.utilToSqlDate(refTime);
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			ps.setTimestamp(colIndex++, ts);

			if (g_getDataFromDBByUpdatedOn) {
				ps.setTimestamp(colIndex++, lowerBoundGPS);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, ts);
			}
			
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			ps.setTimestamp(colIndex++, ts);
			if (g_getDataFromDBByUpdatedOn) {
				ps.setTimestamp(colIndex++, upperBoundGPS);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, ts);
			}
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
			//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
			rs = ps.executeQuery();
			Vehicle vehicle = null;
			GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
			while ((vehicle = reader.readGpsData()) !=null) {
				//vehicle.getGpsData().setName(null);
				GpsData temp = vehicle.getGpsData();
				if (temp.getGps_Record_Time() < refTime)
					prior = temp;
				else
					after = temp;
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
		return new Pair<GpsData, GpsData>(prior, after);
	}
	
	private boolean  helperAddDifferentValueBetweenMergeValues(Connection conn, int pos, GpsData refGpsData, boolean putDiffPtsIfNeeded, AddnlPointsOnAdd addnlPointsOnAdd)  {
		//putDiffPtsIfNeeded = true - if attributeId = 0 then will read from DB imm prior points and add to list,
		//                                                         if attributeId <> 0 then will read from DB imm priot points and add to list
		//if false then obviously nothing to be done
		//will return true if a pt was added to left of pt under consideration (i.e. left changed
		if (!putDiffPtsIfNeeded)
			return false;
		GpsData leftPtBeingReplaced = null;
		GpsData rightPtBeingReplaced = null;
		GpsData leftPtToBeAdded = null;
		GpsData rightPtToBeAdded = null;
		
		boolean addAfterPosChanged = false;
		GpsData leftRangeForImmLookUp = dataList.get(pos-1);
		GpsData rightRangeForImmLookUp = dataList.get(pos+2);
		Pair<GpsData, GpsData> newPoints = getImmDataPoints(conn, refGpsData.getGps_Record_Time(),vehicleId, attributeId, leftRangeForImmLookUp, rightRangeForImmLookUp);
		GpsData priorInList = dataList.get(pos);
		GpsData afterInList = dataList.get(pos+1);
		if (refGpsData.getDimId() == 0) {
			if (priorInList == null || (newPoints.first != null && priorInList.getGps_Record_Time() >= newPoints.first.getGps_Record_Time())) { //priorInList == null => newPoints.first will be null
				if (priorInList != null)
					priorInList.setFWPoint(false);
			}
			else if (newPoints.first != null) {
				addAfterPosChanged = true;
				dataList.addAtIndex(++pos, newPoints.first);
			}
			if (afterInList == null || (newPoints.second != null && afterInList.getGps_Record_Time() <= newPoints.second.getGps_Record_Time())) { //priorInList == null => retval.first will be null
				if (afterInList != null)
					refGpsData.setFWPoint(false);
			}
			else if (newPoints.second != null){
				dataList.addAtIndex(pos+1, newPoints.second);
				newPoints.second.setFWPoint(true);
			}
			return addAfterPosChanged;
		}
		//if dbMayNot contain real truth and attributeId != 0
		Pair<GpsData, GpsData> imm0dim = getImmDataPoints(conn, refGpsData.getGps_Record_Time(),vehicleId, 0,leftRangeForImmLookUp,rightRangeForImmLookUp);
		double vnew = priorInList == null ? afterInList != null ? afterInList.getValue() : Misc.getUndefDouble() : priorInList.getValue();
		
		if (newPoints.first == null ||(imm0dim.first != null && newPoints.first.getGps_Record_Time() < imm0dim.first.getGps_Record_Time())) {
			newPoints.first = imm0dim.first;
			if (newPoints.first != null) {
				newPoints.first.setDimensionInfo(attributeId, Misc.isUndef(vnew) ? newPoints.first.getDimId() == attributeId ? newPoints.first.getValue() : Misc.getUndefDouble():vnew);
				//20140909 TODO speed value .. for special cases .. currently setting to value
				newPoints.first.setSpeed(newPoints.first.getSpeed());
				newPoints.first.setGpsRecordingId(Misc.getUndefInt());
			}
		}
		if (newPoints.second == null ||(imm0dim.second != null && newPoints.second.getGps_Record_Time() > imm0dim.second.getGps_Record_Time())) {
			newPoints.second = imm0dim.second;
			if (newPoints.second != null) {
				newPoints.second.setDimensionInfo(attributeId, Misc.isUndef(vnew) ? newPoints.second.getDimId() == attributeId ? newPoints.second.getValue() : Misc.getUndefDouble():vnew);
				//20140909 TODO speed value
				newPoints.second.setSpeed(newPoints.second.getSpeed());
				newPoints.second.setGpsRecordingId(Misc.getUndefInt());
			}
		}
		
		if (priorInList != null && newPoints.first != null && priorInList.getGps_Record_Time() >= newPoints.first.getGps_Record_Time()) { //priorInList == null => newPoints.first will be null
			priorInList.setFWPoint(false);
		}
		else if (newPoints.first != null) {
			addAfterPosChanged = true;
			dataList.addAtIndex(++pos, newPoints.first);
			leftPtToBeAdded = newPoints.first;
		}
		if (afterInList != null && newPoints.second != null && afterInList.getGps_Record_Time() <= newPoints.second.getGps_Record_Time()) { //priorInList == null => retval.first will be null
			refGpsData.setFWPoint(false);
		}
		else if (newPoints.second != null){
			dataList.addAtIndex(pos+1, newPoints.second);
			newPoints.second.setFWPoint(true);
			rightPtToBeAdded = newPoints.second;
		}
		if (addnlPointsOnAdd != null && (leftPtToBeAdded != null || rightPtToBeAdded != null)) {
			addnlPointsOnAdd.setLeftPtToBeAdded(leftPtToBeAdded);
			addnlPointsOnAdd.setRightPtToBeAdded(rightPtToBeAdded);
		}
		return addAfterPosChanged;
	}

	private ArrayList<GpsData> readHelperByTimeBound(Connection conn, long mi, long mx, boolean getHigher) {
		this.dbReads++;
		boolean gpsIdDelta = this.isGpsIdDelta();
		boolean gpsIdSensorBased = this.isCummDistSensorBased();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<GpsData> retval = new ArrayList<GpsData>((isInRecoveryMode() ? g_reco_maxPoints*maxCachedPtCountMultiple : g_reg_maxPoints*maxCachedPtCountMultiple)/3);
		try {
			long perf0 = System.nanoTime();
			ps = conn.prepareStatement(getHigher ? (g_getDataFromDBByUpdatedOn ? GET_GPS_DATA_BOUND_BY_UPDATE : GET_GPS_DATA_BOUND) : (g_getDataFromDBByUpdatedOn ? GET_GPS_DATA_BOUND_LOWER_BY_UPDATE : GET_GPS_DATA_BOUND_LOWER));
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mi));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mx));
			{
	//			
				if ((mx-mi) >= (11*24*3600*1000) || (this.maxTime > 0 && (maxTime-mi) > (16*24*3600*1000))) {
					System.out.println("[TooManyPointsAsked:]"+this.vehicleId+","+this.attributeId+",Th:"+Thread.currentThread().getId()+","+(new java.util.Date(mx))+ (new java.util.Date(mi)));
					int dbg = 1;
					dbg++;
					(new Exception("Too many Pts being asked")).printStackTrace();
					
				}
			}
			java.sql.Timestamp tsLatestrecv = null;
			java.sql.Timestamp tsgrtofLatest = null;
			if (g_getDataFromDBByUpdatedOn && internalLatestRecvTime > 0) {
				tsLatestrecv = Misc.utilToSqlDate(internalLatestRecvTime);
				tsgrtofLatest = Misc.utilToSqlDate(internalLatestRecordTime);
			}
			if (g_getDataFromDBByUpdatedOn) {
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsgrtofLatest);
			}
			//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
			//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
			rs = ps.executeQuery();
			Vehicle vehicle = null;
			GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
			int dataListSz = dataList.size();
			GpsData prev = null;
			GpsData prevPrev = null;
			if (G_DO_OPTIMIZED) {
				prev = getHigher ? (dataListSz > 0 ? dataList.get(dataListSz-1) : null) : (dataListSz > 0 ? dataList.get(0) : null);
				prevPrev = getHigher ? (dataListSz > 1 ? dataList.get(dataListSz-2) : null) : (dataListSz > 0 ? dataList.get(1) : null);
			}
			boolean prevIsInDataList = true;
			while ((vehicle = reader.readGpsData()) !=null) {
				//vehicle.getGpsData().setName(null);
				GpsData curr = vehicle.getGpsData();
				if (!G_DO_OPTIMIZED || prev == null || prevPrev == null || !prevPrev.isMergeable(prev, gpsIdDelta, gpsIdSensorBased) || !prev.isMergeable(curr, gpsIdDelta, gpsIdSensorBased)) {
					retval.add(curr);
					prevIsInDataList = false;
					prevPrev = prev;
					prev = curr;		
				}
				else {
					if (prevIsInDataList) {
						if (getHigher) {
							dataList.replaceAt(dataListSz-1, curr);
						}
						else
							dataList.replaceAt(0, curr);
					}
					else {
						retval.set(retval.size()-1, curr);
					}
					if (!getHigher)
						curr.setFWPoint(true);
					else 
						prevPrev.setFWPoint(true);
					prev = curr;
				}
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
		return retval;
	}
	
	private ArrayList<GpsData> readHelperByCount(Connection conn, long timeref, int minCnt, int deltaCount, boolean getHigher) {
		this.dbReads++;
		boolean gpsIdDelta = this.isGpsIdDelta();
		boolean gpsIdSensorBased = this.isCummDistSensorBased();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<GpsData> retval = new ArrayList<GpsData>((isInRecoveryMode() ? g_reco_maxPoints*maxCachedPtCountMultiple : g_reg_maxPoints*maxCachedPtCountMultiple)/3);
		try {
			long perf0 = System.nanoTime();
			ps = conn.prepareStatement(getHigher? (g_getDataFromDBByUpdatedOn ? GET_GPS_DATA_BY_COUNT_HIGHER_BY_UPDATE : GET_GPS_DATA_BY_COUNT_HIGHER) : (g_getDataFromDBByUpdatedOn ? GET_GPS_DATA_BY_COUNT_LOWER_BY_UPDATE : GET_GPS_DATA_BY_COUNT_LOWER));
			boolean done = false;
			int cnt = minCnt+deltaCount;
			while (!done) {
				int colIndex = 1;
				ps.setInt(colIndex++, vehicleId);
				ps.setInt(colIndex++, attributeId);
				ps.setTimestamp(colIndex++, Misc.utilToSqlDate(timeref));//timeref updated later
				//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
				//ps.setTimestamp(4, Misc.utilToSqlDate(internalLatestRecvTime)); 
				java.sql.Timestamp tsLatestrecv = null;
				java.sql.Timestamp tsgrtofLatest = null;
				if (g_getDataFromDBByUpdatedOn && internalLatestRecvTime > 0) {
					tsLatestrecv = Misc.utilToSqlDate(internalLatestRecvTime);
					tsgrtofLatest = Misc.utilToSqlDate(internalLatestRecordTime);
				}
				if (g_getDataFromDBByUpdatedOn) {
					ps.setTimestamp(colIndex++, tsLatestrecv);
					ps.setTimestamp(colIndex++, tsLatestrecv);
					ps.setTimestamp(colIndex++, tsLatestrecv);
					ps.setTimestamp(colIndex++, tsgrtofLatest);
				}
				ps.setInt(colIndex++, cnt);
				rs = ps.executeQuery();
				Vehicle vehicle = null;
				GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
				int ptsRead = 0;
				int dataListSz = dataList.size();
				GpsData prev = null;
				GpsData prevPrev = null;
				if (G_DO_OPTIMIZED) {
					int retvalSz = retval.size();
					prev = retvalSz > 0 ? retval.get(retvalSz-1)
							: getHigher ? (dataListSz > 0 ? dataList.get(dataListSz-1) : null) : (dataListSz > 0 ? dataList.get(0) : null);
					prevPrev = retvalSz > 1 ? retval.get(retvalSz - 2) 
							:getHigher ? (dataListSz > 1 ? dataList.get(dataListSz-2) : null) : (dataListSz > 0 ? dataList.get(1) : null);
				}
				boolean prevIsInDataList = true;
				{
					if (cnt > 100) {
						int dbg = 1;
						dbg++;
					}
				}
				
				while ((vehicle = reader.readGpsData()) !=null) {
					//vehicle.getGpsData().setName(null);
					GpsData curr = vehicle.getGpsData();
					if (!G_DO_OPTIMIZED || prev == null || prevPrev == null || !prev.isMergeable(curr, gpsIdDelta, gpsIdSensorBased) || !prevPrev.isMergeable(curr, gpsIdDelta, gpsIdSensorBased)) {
						retval.add(curr);
						prevPrev = prev;
						prev = curr;		
					}
					else {
						if (retval.size() == 0) {
							if (getHigher) {
								dataList.replaceAt(dataListSz-1, curr);
							}
							else
								dataList.replaceAt(0, curr);
						}
						else {
							retval.set(retval.size()-1, curr);
						}
						if (!getHigher)
							curr.setFWPoint(true);
						else 
							prevPrev.setFWPoint(true);
					}
					ptsRead++;
					timeref = curr.getGps_Record_Time();
					//if (retval.size() >= cnt)
					//	break;
					
				}
				rs.close();
				rs = null;
				if (retval.size() >= minCnt || ptsRead < cnt) 
					done = true;
			}//end of while !done loop
			ps.close();
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
		return retval;
	}

	public long getOdometerWeekRecTime() {
		return odometerWeekRecTime;
	}

	public void setOdometerWeekRecTime(long odometerWeekRecTime) {
		if(this.odometerWeekRecTime != odometerWeekRecTime){
			this.odometerWeekGps = null;
		}
		this.odometerWeekRecTime = odometerWeekRecTime;
	}

	public long getOdometerDayRecTime() {
		return odometerDayRecTime;
	}

	public void setOdometerDayRecTime(long odometerDayRecTime) {
		if(this.odometerDayRecTime != odometerDayRecTime){
			this.odometerDayGps = null;
		}
		this.odometerDayRecTime = odometerDayRecTime;
	}

	public long getOdometerMonthRecTime() {
		return odometerMonthRecTime;
	}

	public void setOdometerMonthRecTime(long odometerMonthRecTime) {
		if(this.odometerMonthRecTime != odometerMonthRecTime){
			this.odometerMonthGps = null;
		}
		this.odometerMonthRecTime = odometerMonthRecTime;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public static boolean isDataBehindChannelSuper(long ts, long maxTime) {
		boolean retval =  maxTime <= 0 || (maxTime-ts) < g_dcAtMostXMillisecBehindSuper;
	    return retval; //RAJEEV 20150310 ... now just relying on 6 days worth of gap
	}
	public static boolean isDataBehindChannelReg(long ts, long maxTime) {
		boolean retval =  maxTime <= 0 || (maxTime-ts) < g_dcAtMostXMillisecBehindReg;
	    return retval; //RAJEEV 20150310 ... now just relying on 6 days worth of gap
	}

	public boolean isDataBehindChannelSuper(GpsData data, Connection conn, CacheTrack.VehicleSetup vehSetup) throws Exception {
		return !isValidDataSuper(data, vehSetup.m_backPtsToRetain);
	}
	public boolean isDataBehindChannelReg(GpsData data, Connection conn, CacheTrack.VehicleSetup vehSetup) throws Exception {
		if (vehSetup != null && vehSetup.m_backPtsToRetain > 0) {
			//behind only if outside of pts in cache
			return !isValidDataReg(data, vehSetup.m_backPtsToRetain);
		}
		if (ChannelTypeEnum.isCurrentChannel(data.getSourceChannel())) {
			if (this.latestCurrentChannelData != null) {
				if (data.getGps_Record_Time() <= this.latestCurrentChannelData.getGps_Record_Time() || data.getGpsRecvTime() < this.latestCurrentChannelData.getGpsRecvTime())
					return true;
			}
		}
		if (ChannelTypeEnum.isDataChannel(data.getSourceChannel())) {
			if (this.latestDataChannelData != null) {
				if (data.getGps_Record_Time() <= this.latestDataChannelData.getGps_Record_Time() || data.getGpsRecvTime() < this.latestDataChannelData.getGpsRecvTime())
					return true;
			}
			
		}
		return false;
	}
	public boolean isDataBehindChannel(GpsData data, Connection conn, CacheTrack.VehicleSetup vehSetup) throws Exception {//NOT to be used - for backward
		return !isValidData(data, vehSetup.m_backPtsToRetain);
	}

	/*public boolean isDataBehindChannel(GpsData data, Connection conn) throws Exception {
		CacheTrack.VehicleSetup vsetup = this.getVehicleSetup(conn);
		if (vsetup != null && vsetup.m_backPtsToRetain > 0) {
			//behind only if outside of pts in cache
			GpsData firstPoint = dataList.get(0);
			if (firstPoint != null && data.getGps_Record_Time() >= firstPoint.getGps_Record_Time())
				return false;
		}
		if (ChannelTypeEnum.isCurrentChannel(data.getSourceChannel())) {
			if (this.latestCurrentChannelData != null) {
				if (data.getGps_Record_Time() <= this.latestCurrentChannelData.getGps_Record_Time() || data.getGpsRecvTime() <= this.latestCurrentChannelData.getGpsRecvTime())
					return true;
			}
		}
		if (ChannelTypeEnum.isDataChannel(data.getSourceChannel())) {
			if (this.latestDataChannelData != null) {
				if (data.getGps_Record_Time() <= this.latestDataChannelData.getGps_Record_Time() || data.getGpsRecvTime() <= this.latestDataChannelData.getGpsRecvTime())
					return true;
			}
			
		}
		return false;
	}*/
	public long getLatestDataChannelRecord() {
		return this.latestDataChannelData == null ? Misc.getUndefInt() : this.latestDataChannelData.getGps_Record_Time();
		//return latestDataChannelRecord;
	}
	public long getLatestCurrentChannelRecord() {
		return this.latestCurrentChannelData == null ? Misc.getUndefInt() : this.latestCurrentChannelData.getGps_Record_Time();
		//return latestDataChannelRecord;
	}

	public GpsData getLatestDataChannelData() {
		return latestDataChannelData;
	}

	public void setLatestDataChannelData(GpsData latestDataChannelData) {
		this.latestDataChannelData = latestDataChannelData;
	}

	public GpsData getLatestCurrentChannelData() {
		return latestCurrentChannelData;
	}

	public void setLatestCurrentChannelData(GpsData latestCurrentChannelData) {
		this.latestCurrentChannelData = latestCurrentChannelData;
	}

	public GpsData getPrevLatestDataChannelData() {
		return prevLatestDataChannelData;
	}

	public void setPrevLatestDataChannelData(GpsData prevLatestDataChannelData) {
		this.prevLatestDataChannelData = prevLatestDataChannelData;
	}

	public GpsData getPrevLatestCurrentChannelData() {
		return prevLatestCurrentChannelData;
	}

	public void setPrevLatestCurrentChannelData(GpsData prevLatestCurrentChannelData) {
		this.prevLatestCurrentChannelData = prevLatestCurrentChannelData;
	}
	public GpsData getLatestData() {
		GpsData retval = this.latestCurrentChannelData;
		if (retval == null || (latestDataChannelData != null && retval.getGps_Record_Time() < latestDataChannelData.getGps_Record_Time()))
			retval = latestDataChannelData;
		return retval;
	}
	public long getLatestReceivedTime() {
		return (latestReceivedData == null) ? Misc.getUndefInt() : latestReceivedData.getGpsRecvTime();
			
	}
	public GpsData getLatestReceivedData() {
		return latestReceivedData;
	}

	public void setLatestReceivedData(GpsData latestReceivedData) {
		this.latestReceivedData = latestReceivedData;
	}

	public GpsData getPrevLatestReceivedData() {
		return prevLatestReceivedData;
	}

	public void setPrevLatestReceivedData(GpsData prevLatestReceivedData) {
		this.prevLatestReceivedData = prevLatestReceivedData;
	}

	public long getDbReads() {
		return dbReads;
	}
	public long getReadReq() {
		return readReq;
	}
	public long getMxLen() {
		return mxLen;
	}
	public void resetStat() {
		dbReads = 0;
		readReq = 0;
		mxLen =0;
	}
	public void dumptStat(StringBuilder sb) {
		sb.append("[").append(vehicleId).append(",").append(attributeId).append(":").append(readReq).append(",").append(dbReads).append(",").append(mxLen).append(",").append(dataList.size()).append("]\n");
	}
	public static void main(String[] args) {
		//2013-06-07 09:09:04
		try {
			java.util.Date dt0 = new java.util.Date(1465800913460L);
			System.out.println(dt0);
			System.out.println("adad");
			NewVehicleData.updateAllDistCalc(41278, (new java.util.Date(116,2,31,0,0,24)).getTime(), 0);
			if (true)
				return;
			java.util.Date dt1 = new java.util.Date(113,5,7,9,9,4);
			GpsData gd1 = new GpsData(dt1);
			gd1.setGpsRecvTime(new java.util.Date(113,5,7,9,3,7));
			int vehicleId = 16930;
			int attributeId = 0;
			gd1.setDimensionInfo(attributeId, 100);
			Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfoDP(conn, vehicleId, true, false);
			NewVehicleData vdt = vdf.getDataList(conn, vehicleId, attributeId, true);
			
			if (true)
				return;
			vdt.add(conn, gd1);
			GpsData lk1 = vdt.get(conn, gd1);
			System.out.println(lk1);
			GpsData lk2 = vdt.get(conn, gd1, -30);
			System.out.println(lk2);
			java.util.Date dt11 = new java.util.Date(113,5,7,9,39,55);
			GpsData gd11 = new GpsData(dt11);
			gd11.setGpsRecvTime(new java.util.Date(113,5,7,9,33,58));
			gd11.setDimensionInfo(attributeId, 100);
			vdt.add(conn, gd11);
			GpsData lk3 = vdt.get(conn, gd1, 30);
			System.out.println(lk3);
		//2013-06-01 05:32:18
			java.util.Date dt2 = new java.util.Date(113,5,1,5,32,18);
			GpsData gd2 = new GpsData(dt2);
			gd2.setDimensionInfo(attributeId, 100);
			GpsData lk4 = vdt.get(conn, gd2);
			System.out.println(lk4);
			GpsData lk5 = vdt.get(conn, gd2,-1);
			System.out.println(lk5);
			GpsData lk6 = vdt.get(conn, gd2, 1);
			System.out.println(lk6);
			
		//2013-07-27 19:24:25
			java.util.Date dt3 = new java.util.Date(113,6,27,19,24,25);
			GpsData gd3 = new GpsData(dt3);
			gd2.setDimensionInfo(attributeId, 100);
			GpsData lk7 = vdt.get(conn, gd3);
			System.out.println(lk7);
			GpsData lk8 = vdt.get(conn, gd3,-1);
			System.out.println(lk8);
			GpsData lk9 = vdt.get(conn, gd3, 1);
			System.out.println(lk9);
			int dbg = 1;
			dbg++;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public boolean isInRecoveryMode() {
		return (variousFlags & G_INRECOVERY_MODE_FLAG) > 0;
	}
	public void setInRecoveryMode(boolean inRecoveryMode) {
		if (inRecoveryMode) {
			variousFlags |= G_INRECOVERY_MODE_FLAG;
		}
		else {
			variousFlags &= ~G_INRECOVERY_MODE_FLAG;
		}
	}
	public AddnlPointsOnAdd getAddnlPointsOnAdd() {
		return addnlPointsOnAdd;
	}
	public short getCurrPointsSinceResetRP() {
		return currPointsSinceResetRP;
	}
	public short getCurrPointsSinceResetTP() {
		return currPointsSinceResetTP;
	}
	public short getPointsSinceResetRP() {
		return pointsSinceResetRP;
	}
	public short getPointsSinceResetTP() {
		return pointsSinceResetTP;
	}
	public int getAttributeId() {
		return attributeId;
	}
	
	private ArrayList<GpsData> readHelperByCountOrig(Connection conn, long timeref, int cnt, boolean getHigher) {
		this.dbReads++;
		boolean gpsIdDelta = this.isGpsIdDelta();
		boolean gpsIdSensorBased = this.isCummDistSensorBased();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<GpsData> retval = new ArrayList<GpsData>((isInRecoveryMode() ? g_reco_maxPoints*maxCachedPtCountMultiple : g_reg_maxPoints*maxCachedPtCountMultiple)/3);
		try {
			long perf0 = System.nanoTime();
			ps = conn.prepareStatement(getHigher? (g_getDataFromDBByUpdatedOn ? GET_GPS_DATA_BY_COUNT_HIGHER_BY_UPDATE : GET_GPS_DATA_BY_COUNT_HIGHER) : (g_getDataFromDBByUpdatedOn ? GET_GPS_DATA_BY_COUNT_LOWER_BY_UPDATE : GET_GPS_DATA_BY_COUNT_LOWER));
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, attributeId);
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(timeref));
			//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
			//ps.setTimestamp(4, Misc.utilToSqlDate(internalLatestRecvTime)); 
			java.sql.Timestamp tsLatestrecv = null;
			java.sql.Timestamp tsgrtofLatest = null;
			if (g_getDataFromDBByUpdatedOn && internalLatestRecvTime > 0) {
				tsLatestrecv = Misc.utilToSqlDate(internalLatestRecvTime);
				tsgrtofLatest = Misc.utilToSqlDate(internalLatestRecordTime);
			}
			if (g_getDataFromDBByUpdatedOn) {
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsLatestrecv);
				ps.setTimestamp(colIndex++, tsgrtofLatest);
			}
			ps.setInt(colIndex++, cnt);
			rs = ps.executeQuery();
			Vehicle vehicle = null;
			GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
			int ptsRead = 0;
			int dataListSz = dataList.size();
			GpsData prev = null;
			GpsData prevPrev = null;
			if (G_DO_OPTIMIZED) {
				prev = getHigher ? (dataListSz > 0 ? dataList.get(dataListSz-1) : null) : (dataListSz > 0 ? dataList.get(0) : null);
				prevPrev = getHigher ? (dataListSz > 1 ? dataList.get(dataListSz-2) : null) : (dataListSz > 0 ? dataList.get(1) : null);
			}
			boolean prevIsInDataList = true;
			while ((vehicle = reader.readGpsData()) !=null) {
				//vehicle.getGpsData().setName(null);
				GpsData curr = vehicle.getGpsData();
				if (!G_DO_OPTIMIZED || prev == null || prevPrev == null || !prev.isMergeable(curr, gpsIdDelta, gpsIdSensorBased) || !prevPrev.isMergeable(curr, gpsIdDelta, gpsIdSensorBased)) {
					retval.add(curr);
					prevIsInDataList = false;
					prevPrev = prev;
					prev = curr;		
				}
				else {
					if (prevIsInDataList) {
						if (getHigher) {
							dataList.replaceAt(dataListSz-1, curr);
						}
						else
							dataList.replaceAt(0, curr);
					}
					else {
						retval.set(retval.size()-1, curr);
					}
					prevPrev.setFWPoint(true);
				}
				ptsRead++;
				if (ptsRead >= cnt)
					break;
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			PerfStat.addVDFQuery(System.nanoTime() - perf0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
		return retval;
	}
	private GpsData odometerWeekGps = null; //will be null if the odo values is cache - otherwise last looked up value
	private GpsData odometerDayGps = null;//will be null if the odo values is cache - otherwise last looked up value
	private GpsData odometerMonthGps = null;//will be null if the odo values is cache - otherwise last looked up value
	
	public GpsData getOdoDayGps(Connection conn) {
		GpsData ref = new GpsData(odometerDayRecTime);
		GpsData dt = dataList.get(ref);
		if (dt == null) {
			dt = this.odometerDayGps;
		}
//		if(dt != null && dt.getGps_Record_Time() != odometerDayRecTime){
//			dt = null;
//		}
		if (dt == null) {
			dt = this.getSinglePoint(conn, ref);
			this.odometerDayGps = dt;
		}
		return dt;		
	}
	
	public GpsData getOdoWeekGps(Connection conn) {
		GpsData ref = new GpsData(odometerWeekRecTime);
		GpsData dt = dataList.get(ref);
		if (dt == null) {
			dt = this.odometerWeekGps;
		}
		if (dt == null) {
			dt = this.getSinglePoint(conn, ref);
			this.odometerWeekGps = dt;
		}
		return dt;		
	}
	
	public GpsData getOdoMonthGps(Connection conn) {
		GpsData ref = new GpsData(odometerMonthRecTime);
		GpsData dt = dataList.get(ref);
		if (dt == null) {
			dt = this.odometerMonthGps;
		}
		if (dt == null) {
			dt = this.getSinglePoint(conn, ref);
			this.odometerMonthGps = dt;
		}
		return dt;		
	}

	public static void newUpdateDist(Connection conn, CacheTrack.VehicleSetup vehSetup, GpsData prev, GpsData curr, double distRelPrev, double orgMaxSpeed, double orgThresholdSpeed) throws Exception {
		boolean isIgn = curr.getDimId() == ApplicationConstants.IGN_ONOFF;
		boolean isRPM = curr.getDimId() == ApplicationConstants.RPM;
		boolean isDist = curr.getDimId() == 0;
		double distLimit = 0.05;//DEBUG13
		//orgThresholdSpeed = -1;//DEBUG13
		if (isIgn) {
			boolean prevIsTrue = prev != null && prev.getValue() >= 0.5;
			double msgapHr = prevIsTrue ? (double)(curr.getGps_Record_Time() - prev.getGps_Record_Time())/(3600000.0) : 0;
			if (msgapHr > 0.0834 && !prev.isFWPoint() ) //5 min
				msgapHr = 0.0834;
			curr.setSpeed((prev == null ? 0 : prev.getSpeed())+msgapHr);
		}
		else if (isRPM) {
			boolean prevIsTrue = prev != null && prev.getValue() >= 0.5;
			double msgapHr = prevIsTrue ? (double)(curr.getGps_Record_Time() - prev.getGps_Record_Time())/(3600000.0) : 0;
			curr.setSpeed((prev == null ? 0 : prev.getSpeed())+msgapHr);
		}
		else if (!isDist) {
			curr.setSpeed(curr.getValue() - (prev == null ? 0 : prev.getValue()));
		}
		else {
			//			//moved to newUpdateDist
			//if(speed < orgThresholdSpeed && prevSpeed < orgThresholdSpeed && deltaDist < 0.15)  // TODO : hack make it org controlled  
			//	gpsData.setValue(prev == null ? gpsData.getValue() : prev.getValue());
			int cummDistProvided = vehSetup.cummDistProvided;// 0 not there, 1 cumm, 2 delta, 3 canbus
			int cummDistCalcApproach = vehSetup.cummDistCalcApproach;// 0 dont. 1 complex, 2 simple
			boolean cummDistSensorBased = cummDistCalcApproach == 2 || cummDistProvided == 3;
			if (prev != null && prev.isMergeable(curr, cummDistProvided == 2, cummDistSensorBased)) {
				curr.setValue(prev.getValue());
				curr.setSpeed(0);
				return;
			}
			if (curr.getGpsRecordingId() < 0)
				curr.setGpsRecordingId(Misc.getUndefInt());
			if (cummDistProvided == 2 && curr.getGpsRecordingId() > 100000)
				curr.setGpsRecordingId(Misc.getUndefInt());
			if (cummDistProvided == 3)
				cummDistCalcApproach = 2; //simple
			int vehicleId = vehSetup.m_vehicleId;
			if (prev != null && distRelPrev < distLimit) {
				double impliedSpeed = distRelPrev*3600.0*1000.0/(double)(curr.getGps_Record_Time()-prev.getGps_Record_Time());
				if ((impliedSpeed < orgThresholdSpeed && prev.getSpeed() < orgThresholdSpeed && (vehSetup.isHasSpeed() && !Misc.isUndef(curr.getSpeed()) && curr.getSpeed() < orgThresholdSpeed))
						) {
					distRelPrev = 0;
				}
			}
			if (cummDistCalcApproach == 0 || cummDistProvided == 0 || Misc.isUndef(curr.getGpsRecordingId())) {
				double cummDist = distRelPrev + (prev == null ? 0 : prev.getValue());
				if (prev == null) {
					System.out.println("###[DP] prev is null for vehicle:"+vehicleId+" GpsData:"+ curr);
				}
				if(distRelPrev>10) 
					System.out.println("####Relative Distance > 10 km ####"+distRelPrev+"for Vehicle:"+vehicleId+ " GpsData:"+curr);
				curr.setValue(cummDist);
			}
			else if (cummDistCalcApproach == 2) {
				double prevCumm = prev == null ? 0 : prev.getValue();
				//boolean validRecordingId = Misc.isUndef(curr.getGpsRecordingId()); ... not needed vals will be negative
				double currCumm = ((double) curr.getGpsRecordingId())/1000.0;
				if (cummDistProvided == 2) { //delta ... currCumm is delta - but within becomes cumm
					//if (!validRecordingId)
					//	currCumm = distRelPrev;
					if (currCumm < distRelPrev || currCumm < 0)
						currCumm = prevCumm + distRelPrev;
					else
						currCumm = prevCumm + currCumm;
					curr.setValue(currCumm);
				}
				else {//cumm 
					//if (!validRecordingId) {
					//	currCumm = prevCumm + distRelPrev; 
					//}
					double distRelCum = prevCumm+distRelPrev;
					currCumm = currCumm < prevCumm || currCumm < 0 || currCumm < distRelCum ? distRelCum : currCumm;
					curr.setValue(currCumm);
				}
			}
			else if (cummDistCalcApproach == 1) { //complex summation
				double prevCumm = prev == null ? Misc.getUndefDouble() : prev.getValue();
				double currCumm = ((double) curr.getGpsRecordingId())/1000.0;
				double givenDelta = cummDistProvided == 2 ? currCumm 
						: prev == null ? 0
								: Misc.isUndef(prev.getGpsRecordingId()) ? 
										distRelPrev 
										: (currCumm - (double)prev.getGpsRecordingId()/1000.0);
				if (prev != null) {
					if (prev != null && givenDelta < distLimit) {
						double impliedSpeed = givenDelta*3600.0*1000.0/(double)(curr.getGps_Record_Time()-prev.getGps_Record_Time());
						if ((impliedSpeed < orgThresholdSpeed && prev.getSpeed() < orgThresholdSpeed && (vehSetup.isHasSpeed() && !Misc.isUndef(curr.getSpeed()) && curr.getSpeed() < orgThresholdSpeed)) 
								){
							givenDelta = 0;
						}
					}
				}
				if (givenDelta < distRelPrev)
					givenDelta = distRelPrev;
				currCumm = prev == null ? cummDistProvided == 2 ? 0 : currCumm : prev.getValue();
				currCumm += givenDelta;
				curr.setValue(currCumm);
			}
		}//else doing dist
	}
	
	public void recalcAndUpdateOdoAndCurr(Connection conn, CacheTrack.VehicleSetup vehSetup) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			GpsData gpsData = this.getLast(conn);
			this.odometerDayGps = null;
			this.odometerDayRecTime = Misc.getUndefInt();
			this.odometerMonthGps = null;
			this.odometerMonthRecTime = Misc.getUndefInt();
			this.odometerWeekGps = null;
			this.odometerWeekRecTime = Misc.getUndefInt();
			if (gpsData != null) {
				boolean isIgn = gpsData.getDimId() == ApplicationConstants.IGN_ONOFF;
				boolean isRPM = gpsData.getDimId() == ApplicationConstants.RPM;
				boolean isDist = gpsData.getDimId() == 0;
				long recTime = gpsData.getGps_Record_Time();			
				long begOfDayRec = TimePeriodHelper.getBegOfDate(recTime, Misc.SCOPE_DAY);
				long begOfWeekRec = TimePeriodHelper.getBegOfDate(recTime, Misc.SCOPE_WEEK);
				long begOfMonthRec = TimePeriodHelper.getBegOfDate(recTime, Misc.SCOPE_MONTH);
				GpsData dayGps = this.getSinglePoint(conn, new GpsData(begOfDayRec), false, true);
				GpsData weekGps = this.getSinglePoint(conn, new GpsData(begOfWeekRec), false, true);
				GpsData monthGps = this.getSinglePoint(conn, new GpsData(begOfMonthRec), false, true);
				this.setCurrentOdometerStatus(dayGps == null ? Misc.getUndefInt() : dayGps.getGps_Record_Time(), weekGps == null ? Misc.getUndefInt() : weekGps.getGps_Record_Time(), monthGps == null ? Misc.getUndefInt() : monthGps.getGps_Record_Time());
				this.odometerDayGps = dayGps;
				this.odometerMonthGps = monthGps;
				this.odometerWeekGps = weekGps;
				ps = conn.prepareStatement("select 1 from current_data where vehicle_id=? and attribute_id=?");
				ps.setInt(1, vehicleId);
				ps.setInt(2, attributeId);
				rs  = ps.executeQuery();
				boolean hasRecord = rs.next();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				if (!hasRecord) {
					ps = conn.prepareStatement("insert into current_data(vehicle_id, attribute_id, longitude, latitude, gps_record_time, name) values (?,?,?,?,?,?)");
					ps.setInt(1, vehicleId);
					ps.setInt(2, attributeId);
					ps.setDouble(3, gpsData.getLongitude());
					ps.setDouble(4, gpsData.getLatitude());
					ps.setTimestamp(5, Misc.longToSqlDate(gpsData.getGps_Record_Time()));
					String n = gpsData.calcName(conn, vehicleId, vehSetup);
					ps.setString(6, n);
					ps.execute();
					ps = Misc.closePS(ps);
				}
				ps = conn.prepareStatement("update current_data set attribute_value = ?, speed = ?, odometer_day = ? , odometer_day_rec_time = ?, odometer_week = ? , odometer_week_rec_time = ?, odometer_month = ? , odometer_month_rec_time = ? where vehicle_id=? and attribute_id=?");
				ps.setDouble(1, gpsData.getValue());
				ps.setDouble(2, gpsData.getSpeed());
				Misc.setParamDouble(ps, dayGps == null ? Misc.getUndefDouble() : isIgn || isRPM ? dayGps.getSpeed() : dayGps.getValue(), 3);
				ps.setTimestamp(4, Misc.longToSqlDate(dayGps == null ? Misc.getUndefInt() : dayGps.getGps_Record_Time()));
				Misc.setParamDouble(ps, weekGps == null ? Misc.getUndefDouble() : isIgn || isRPM ? weekGps.getSpeed() : weekGps.getValue(), 5);
				ps.setTimestamp(6, Misc.longToSqlDate(weekGps == null ? Misc.getUndefInt() : weekGps.getGps_Record_Time()));
				Misc.setParamDouble(ps, monthGps == null ? Misc.getUndefDouble() : isIgn || isRPM ? monthGps.getSpeed() : monthGps.getValue(), 7);
				ps.setTimestamp(8, Misc.longToSqlDate(monthGps == null ? Misc.getUndefInt() : monthGps.getGps_Record_Time()));
				
				ps.setInt(9, vehicleId);
				ps.setInt(10, attributeId);
				ps.execute();
				ps = Misc.closePS(ps);
			}//if gpsData is not null
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	public static void updateAllDistCalcAndLocation(int vehicleId, long fromTSIncl, int attributeId, double minDistMovedForNameCalc) {
		updateAllDistCalcAndLocation(vehicleId, fromTSIncl, attributeId, minDistMovedForNameCalc, false, null); 	
	}
	public static void updateAllDistCalcAndLocation(int vehicleId, long fromTSIncl, int attributeId, double minDistMovedForNameCalc, boolean giveWtToOlderAVIfHigher, java.util.Date mergeDate) {
		//giveWtToOlderAVIfHigher
		//mergeDate
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		boolean destroyIt = false;
		System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" @ :"+new Date());
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false);
			synchronized (vdf) {
				double orgMaxSpeed = -1 ;
				double orgThresholdSpeed = -1;
				try {
					Cache _cache = Cache.getCacheInstance(conn);
					int userOrgControlId = vehSetup.m_ownerOrgId;
					MiscInner.PortInfo userOrgControlOrg = _cache.getPortInfo(userOrgControlId, conn);
					ArrayList orgMaxSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_MAX_SPEED);
					ArrayList orgThresholdSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_LEAST_SPEED);
					orgMaxSpeed = orgMaxSpeedList == null || orgMaxSpeedList.size() == 0 || Common.isNull(orgMaxSpeedList. get(0)) ? 100 : ((Double) orgMaxSpeedList
							.get(0));
					orgThresholdSpeed = orgThresholdSpeedList == null || orgThresholdSpeedList.size() == 0 || Common.isNull(orgThresholdSpeedList. get(0)) ? 4.8 : ((Double) orgThresholdSpeedList
							.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
				ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SIMPLE_IMM_BEFORE);
				ps.setFetchSize(Integer.MIN_VALUE);
				ps.setInt(1, vehicleId);
				ps.setInt(2, attributeId);
				java.sql.Timestamp ts = Misc.longToSqlDate(fromTSIncl);
				ps.setTimestamp(3, ts);
				rs = ps.executeQuery();
				GpsDataResultSetReader prevrs = new GpsDataResultSetReader(rs, true);
				Vehicle prev = prevrs.readGpsData();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				int limit = 30000;
				ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SIMPLE + " order by gps_record_time limit "+limit);
	//			System.out.println(ps);
				int count = 0;
				while (true) {
					boolean foundData = false;
					ps.setInt(1, vehicleId);
					ps.setInt(2, attributeId);
					ps.setTimestamp(3, ts);
					System.out.println(ps);
					rs = ps.executeQuery();
					GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
					Vehicle vehicle = null;
					String loc = null;
					ps2 = conn.prepareStatement("update logged_data set attribute_value=?, speed=?, name=? where vehicle_id=? and attribute_id=? and gps_record_time=?");
					long mergeTime = mergeDate == null ? -1 : mergeDate.getTime();
					boolean seenAfterMerge = false;
					double baseDeltaAfterMerge = 0;
					while ((vehicle = reader.readGpsData()) !=null) {
						try {
							boolean isAfterMerge = mergeTime <= 0 || vehicle.getGpsData().getGps_Record_Time() >= mergeTime;
							double currAV = vehicle.getGpsData().getValue();
							if (currAV < -0.0005)
								currAV = Misc.getUndefDouble();
							else
								currAV += baseDeltaAfterMerge;
							foundData = true;
							
							double distRelPrev = 0;
							if (vehicle.getGpsData().getDimId() == 0 && prev != null && prev.getGpsData() != null && prev.getGpsData().isValidPoint() && vehicle.getGpsData().isValidPoint()) {
								distRelPrev = prev.getGpsData().fastGeoDistance(vehicle.getGpsData());
							}
							if(loc == null || (distRelPrev > minDistMovedForNameCalc)){
								loc = vehicle.getGpsData().getName(conn, vehicleId, vehSetup);
//								System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" loc:"+loc);
//								Pair<ArrayList<Object>, String> pair = LocalNameHelper.calcGetNameHelper(vehicleId, vehicle.getGpsData().getLongitude(), vehicle.getGpsData().getLatitude());
//								System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" pair:"+pair);
							}
							NewVehicleData.newUpdateDist(conn, vehSetup, prev == null ? null : prev.getGpsData(), vehicle.getGpsData(), distRelPrev, orgMaxSpeed, orgThresholdSpeed);
							if (currAV > -0.0005 && seenAfterMerge && giveWtToOlderAVIfHigher && vehicle.getGpsData().getValue() < currAV)
								vehicle.getGpsData().setValue(currAV);
							
							if (isAfterMerge) {
								if (!seenAfterMerge)
									baseDeltaAfterMerge =  vehicle.getGpsData().getValue() - currAV;
								seenAfterMerge = true;
							}
							//if (Misc.isEqual(baseDeltaAfterMerge,0) && currAV >  -0.00005 && Misc.isEqual(vehicle.getGpsData().getValue(), currAV))
							//		continue;
							count++;
							ps2.setDouble(1, vehicle.getGpsData().getValue());
							ps2.setDouble(2, vehicle.getGpsData().getSpeed());
							ps2.setString(3, loc);
							ps2.setInt(4, vehicleId);
							ps2.setInt(5, attributeId);
							ps2.setTimestamp(6, Misc.longToSqlDate(vehicle.getGpsData().getGps_Record_Time()));
							ps2.addBatch();
							
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						prev = vehicle;
					}
					rs = Misc.closeRS(rs);
					if (foundData) {
						ps2.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
						System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" Pts:"+count);
						ps2 = Misc.closePS(ps2);
						ts = Misc.longToSqlDate(prev.getGpsData().getGps_Record_Time()+1000);
					}
					else {
						ps2 = Misc.closePS(ps2);
						break;
					}
				}
				ps = Misc.closePS(ps);
				vdf.reinit(conn, vehSetup);
				
				NewVehicleData vdt = vdf.getDataList(conn, vehicleId, attributeId, true);
				GpsData last = vdt.getLast(conn);
				vdt.clearRecordName();
				
				vdt.recalcAndUpdateOdoAndCurr(conn, vehSetup);
				System.out.println("[UPD_ODO_NAME_CALC]"+vehicleId+" Pts:"+count);
				System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" @ :"+new Date());
			}//end of sync
			
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps2 = Misc.closePS(ps2);
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}
			catch (Exception e2) {
				
			}
		}
	}
	public static void updateAllDistCalc(int vehicleId, long fromTSIncl, int attributeId) {
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		PreparedStatement del = null;
		
		ResultSet rs = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false);
			synchronized (vdf) {
				double orgMaxSpeed = -1 ;
				double orgThresholdSpeed = -1;
				try {
					Cache _cache = Cache.getCacheInstance(conn);
					int userOrgControlId = vehSetup.m_ownerOrgId;
					MiscInner.PortInfo userOrgControlOrg = _cache.getPortInfo(userOrgControlId, conn);
					ArrayList orgMaxSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_MAX_SPEED);
					ArrayList orgThresholdSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_LEAST_SPEED);
					orgMaxSpeed = orgMaxSpeedList == null || orgMaxSpeedList.size() == 0 || Common.isNull(orgMaxSpeedList. get(0)) ? 100 : ((Double) orgMaxSpeedList
							.get(0));
					orgThresholdSpeed = orgThresholdSpeedList == null || orgThresholdSpeedList.size() == 0 || Common.isNull(orgThresholdSpeedList. get(0)) ? 4.8 : ((Double) orgThresholdSpeedList
							.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
				ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SIMPLE_IMM_BEFORE);
				ps.setFetchSize(Integer.MIN_VALUE);
				ps.setInt(1, vehicleId);
				ps.setInt(2, attributeId);
				java.sql.Timestamp ts = Misc.longToSqlDate(fromTSIncl);
				ps.setTimestamp(3, ts);
				rs = ps.executeQuery();
				GpsDataResultSetReader prevrs = new GpsDataResultSetReader(rs, true);
				Vehicle prev = prevrs.readGpsData();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				int limit = 10000;
				ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SIMPLE + " order by gps_record_time limit "+limit);
				del = conn.prepareStatement("delete from logged_data where vehicle_id=? and attribute_id=? and gps_record_time=?");
				boolean toDel = false;
	//			System.out.println(ps);
				int count = 0;
				while (true) {
					boolean foundData = false;
					ps.setInt(1, vehicleId);
					ps.setInt(2, attributeId);
					ps.setTimestamp(3, ts);
					System.out.println(ps);
					rs = ps.executeQuery();
					GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
					Vehicle vehicle = null;
					ps2 = conn.prepareStatement("update logged_data set attribute_value=?, speed=? where vehicle_id=? and attribute_id=? and gps_record_time=?");
					
					while ((vehicle = reader.readGpsData()) !=null) {
						try {
							if (vehicle.getGpsData().getLongitude() < 0.05) {
								del.setInt(1, vehicleId);
								del.setInt(2, attributeId);
								del.setTimestamp(3, Misc.longToSqlDate(vehicle.getGpsData().getGps_Record_Time()));
								del.addBatch();
								toDel = true;
								continue;
							}
							foundData = true;
							count++;
							double distRelPrev = 0;
							if (vehicle.getGpsData().getDimId() == 0 && prev != null && prev.getGpsData() != null && prev.getGpsData().isValidPoint() && vehicle.getGpsData().isValidPoint()) {
								distRelPrev = prev.getGpsData().fastGeoDistance(vehicle.getGpsData());
							}
							NewVehicleData.newUpdateDist(conn, vehSetup, prev == null ? null : prev.getGpsData(), vehicle.getGpsData(), distRelPrev, orgMaxSpeed, orgThresholdSpeed);
					//		ps2.setDouble(1, vehicle.getGpsData().getValue());
					//		java.math.BigDecimal valD = new java.math.BigDecimal(vehicle.getGpsData().getValue()); 
							ps2.setDouble(1, vehicle.getGpsData().getValue());
							ps2.setDouble(2, vehicle.getGpsData().getSpeed());
							ps2.setInt(3, vehicleId);
							ps2.setInt(4, attributeId);
							ps2.setTimestamp(5, Misc.longToSqlDate(vehicle.getGpsData().getGps_Record_Time()));
							ps2.addBatch();							
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						prev = vehicle;
					}
					rs = Misc.closeRS(rs);
					
					if (foundData) {
						ps2.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
						System.out.println("[UPD_DIST_CALC]"+vehicleId+" Pts:"+count);
						ps2 = Misc.closePS(ps2);
						ts = Misc.longToSqlDate(prev.getGpsData().getGps_Record_Time()+1000);
					}
					else {
						ps2 = Misc.closePS(ps2);
						break;
					}
				}
				ps = Misc.closePS(ps);
				vdf.reinit(conn, vehSetup);
				
				NewVehicleData vdt = vdf.getDataList(conn, vehicleId, attributeId, true);
				GpsData last = vdt.getLast(conn);
				vdt.clearRecordName();
				if (toDel)
					del.executeBatch();
				del = Misc.closePS(del);
				vdt.recalcAndUpdateOdoAndCurr(conn, vehSetup);
				System.out.println("[UPD_ODO_CALC]"+vehicleId+" Pts:"+count);
			}//end of sync
			
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps2 = Misc.closePS(ps2);
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}
			catch (Exception e2) {
				
			}
		}
	}
	public byte getMaxCachedPtCountMultiple() {
		return maxCachedPtCountMultiple;
	}
	public void setMaxCachedPtCountMultiple(byte maxCachedPtCountMultiple) {
		this.maxCachedPtCountMultiple = maxCachedPtCountMultiple;
	}
	public static void updateLocation(int vehicleId, long fromTSIncl, int attributeId, double minDistMovedForNameCalc) {
		updateLocation(vehicleId, fromTSIncl, attributeId, minDistMovedForNameCalc, false, null); 	
	}
	public static void updateLocation(int vehicleId, long fromTSIncl, int attributeId, double minDistMovedForNameCalc, boolean giveWtToOlderAVIfHigher, java.util.Date mergeDate) {
		//giveWtToOlderAVIfHigher
		//mergeDate
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		boolean destroyIt = false;
		System.out.println("[UPD_NAME_CALC]"+vehicleId+" @ :"+new Date()+"Location only");
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false);
			synchronized (vdf) {
				double orgMaxSpeed = -1 ;
				double orgThresholdSpeed = -1;
				try {
					Cache _cache = Cache.getCacheInstance(conn);
					int userOrgControlId = vehSetup.m_ownerOrgId;
					MiscInner.PortInfo userOrgControlOrg = _cache.getPortInfo(userOrgControlId, conn);
					ArrayList orgMaxSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_MAX_SPEED);
					ArrayList orgThresholdSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_LEAST_SPEED);
					orgMaxSpeed = orgMaxSpeedList == null || orgMaxSpeedList.size() == 0 || Common.isNull(orgMaxSpeedList. get(0)) ? 100 : ((Double) orgMaxSpeedList
							.get(0));
					orgThresholdSpeed = orgThresholdSpeedList == null || orgThresholdSpeedList.size() == 0 || Common.isNull(orgThresholdSpeedList. get(0)) ? 4.8 : ((Double) orgThresholdSpeedList
							.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
				ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SIMPLE_IMM_BEFORE);
				ps.setFetchSize(Integer.MIN_VALUE);
				ps.setInt(1, vehicleId);
				ps.setInt(2, attributeId);
				java.sql.Timestamp ts = Misc.longToSqlDate(fromTSIncl);
				ps.setTimestamp(3, ts);
				rs = ps.executeQuery();
				GpsDataResultSetReader prevrs = new GpsDataResultSetReader(rs, true);
				Vehicle prev = prevrs.readGpsData();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				int limit = 30000;
				ps = conn.prepareStatement(NewVehicleData.GET_GPS_DATA_SIMPLE + " order by gps_record_time limit "+limit);
	//			System.out.println(ps);
				int count = 0;
				while (true) {
					boolean foundData = false;
					ps.setInt(1, vehicleId);
					ps.setInt(2, attributeId);
					ps.setTimestamp(3, ts);
					System.out.println(ps);
					rs = ps.executeQuery();
					GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
					Vehicle vehicle = null;
					String loc = null;
					ps2 = conn.prepareStatement("update logged_data set attribute_value=?, speed=?, name=? where vehicle_id=? and attribute_id=? and gps_record_time=?");
					long mergeTime = mergeDate == null ? -1 : mergeDate.getTime();
					boolean seenAfterMerge = false;
					double baseDeltaAfterMerge = 0;
					while ((vehicle = reader.readGpsData()) !=null) {
						try {
							boolean isAfterMerge = mergeTime <= 0 || vehicle.getGpsData().getGps_Record_Time() >= mergeTime;
							double currAV = vehicle.getGpsData().getValue();
							if (currAV < -0.0005)
								currAV = Misc.getUndefDouble();
							else
								currAV += baseDeltaAfterMerge;
							foundData = true;
							
							double distRelPrev = 0;
							if (vehicle.getGpsData().getDimId() == 0 && prev != null && prev.getGpsData() != null && prev.getGpsData().isValidPoint() && vehicle.getGpsData().isValidPoint()) {
								distRelPrev = prev.getGpsData().fastGeoDistance(vehicle.getGpsData());
							}
							if(loc == null || (distRelPrev > minDistMovedForNameCalc)){
								loc = vehicle.getGpsData().getName(conn, vehicleId, vehSetup);
//								System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" loc:"+loc);
//								Pair<ArrayList<Object>, String> pair = LocalNameHelper.calcGetNameHelper(vehicleId, vehicle.getGpsData().getLongitude(), vehicle.getGpsData().getLatitude());
//								System.out.println("[UPD_DIST_NAME_CALC]"+vehicleId+" pair:"+pair);
							}
							NewVehicleData.newUpdateDist(conn, vehSetup, prev == null ? null : prev.getGpsData(), vehicle.getGpsData(), distRelPrev, orgMaxSpeed, orgThresholdSpeed);
							if (currAV > -0.0005 && seenAfterMerge && giveWtToOlderAVIfHigher && vehicle.getGpsData().getValue() < currAV)
								vehicle.getGpsData().setValue(currAV);
							
							if (isAfterMerge) {
								if (!seenAfterMerge)
									baseDeltaAfterMerge =  vehicle.getGpsData().getValue() - currAV;
								seenAfterMerge = true;
							}
							//if (Misc.isEqual(baseDeltaAfterMerge,0) && currAV >  -0.00005 && Misc.isEqual(vehicle.getGpsData().getValue(), currAV))
							//		continue;
							count++;
							ps2.setDouble(1, vehicle.getGpsData().getValue());
							ps2.setDouble(2, vehicle.getGpsData().getSpeed());
							ps2.setString(3, loc);
							ps2.setInt(4, vehicleId);
							ps2.setInt(5, attributeId);
							ps2.setTimestamp(6, Misc.longToSqlDate(vehicle.getGpsData().getGps_Record_Time()));
							ps2.addBatch();
							
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						prev = vehicle;
					}
					rs = Misc.closeRS(rs);
					if (foundData) {
						ps2.executeBatch();
						if (!conn.getAutoCommit())
							conn.commit();
						System.out.println("[UPD_NAME_CALC]"+vehicleId+" Pts:"+count);
						ps2 = Misc.closePS(ps2);
						ts = Misc.longToSqlDate(prev.getGpsData().getGps_Record_Time()+1000);
					}
					else {
						ps2 = Misc.closePS(ps2);
						break;
					}
				}
				ps = Misc.closePS(ps);
				vdf.reinit(conn, vehSetup);
				
				NewVehicleData vdt = vdf.getDataList(conn, vehicleId, attributeId, true);
				GpsData last = vdt.getLast(conn);
				vdt.clearRecordName();
				
				vdt.recalcAndUpdateOdoAndCurr(conn, vehSetup);
				System.out.println("[UPD_NAME_CALC]"+vehicleId+" Pts:"+count);
				System.out.println("[UPD_NAME_CALC]"+vehicleId+" @ :"+new Date());
			}//end of sync
			
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps2 = Misc.closePS(ps2);
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}
			catch (Exception e2) {
				
			}
		}
	}
	public Dala01Mgmt getDalaUpMgmt() {
		return dalaUpMgmt;
	}
}

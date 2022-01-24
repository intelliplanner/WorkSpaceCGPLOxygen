package com.ipssi.report.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.eta.NewETAforSrcDestItem;
import com.ipssi.eta.NewVehicleETA;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.DriverExtendedInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.processor.utils.GpsData;

public class CacheValue {
	private static int numVehicles = 3000;
	private static int numDims = 250;
	private static float loadFactor = 0.75f;
	private static int concurrencyLevel = 8;
	private static boolean initialized = false;
	private static final int INSIDE_LOAD = 0;
	private static final int TRANSIT_LOAD = 1;
	private static final int INSIDE_UNLOAD = 2;
	private static final int TRANSIT_UNLOAD = 3;
	private static final int GREATER_THAN = 0;
	private static final int LESS_THAN = 1;
	private static final int EQUALS_TO = 2;
	private static final int GREATER_THAN_EQUALS_TO = 3;
	private static final int LESS_THAN_EQUALS_TO = 4;
	private static final int NOT_EQUALS_TO = 5;
	private static final int BETWEEN = 6;

	private static final int MAX_EVENTS_PER_RULE_TO_KEEP = 5;

	//Implementation notes:
	//two options to store - one approach is to have 1 ConcurrentHashMap with Pair<VehicleId, DimId> as key - the table will be huge
	// another is to have ConcurrentHashMap<vehicleId, HashMap<Integer, CacheValueItem>> - note the second can be hashMap
	// if we can ensure that initial load is going to be synchroized and populates all dim for a particular vehicle.

	//Now adding value - any one updating current info should call add whenever there value changes
	//However consider id="21161"	name="MPL Distance Remaining"
	//this depends upon current GpsData - or we may depend upon java.util.Date(). Former can still be handled by making update
	//run on every pt - but that would be slow.
	//Instead for these we will calculate these values here 

	//add 21188, 21189, 21190 to curr op when loading these
	public static class LatestEventInfo {
		private int ruleId = Misc.getUndefInt();
		private String ruleName = null;//DEBUG13 temp until we move ruleCache to top
		private long startTime = Misc.getUndefInt();
		private long endTime = Misc.getUndefInt();
		private String startName = null;
		private String endName = null;
		public String toString() {
			return "("+ruleId+","+Misc.longToUtilDate(startTime)+","+Misc.longToUtilDate(endTime)+","+startName+","+endName+")";
		}
		public LatestEventInfo(int ruleId, String ruleName, long startTime, long endTime, String startName, String endName) {
			super();
			this.ruleId = ruleId;
			this.ruleName = ruleName;
			this.startTime = startTime;
			this.endTime = endTime;
			this.startName = startName;
			this.endName = endName;
		}
		
		public int getRuleId() {
			return ruleId;
		}
		public void setRuleId(int ruleId) {
			this.ruleId = ruleId;
		}
		public long getStartTime() {
			return startTime;
		}
		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}
		public long getEndTime() {
			return endTime;
		}
		public void setEndTime(long endTime) {
			this.endTime = endTime;
		}
		public String getStartName() {
			return startName;
		}
		public void setStartName(String startName) {
			this.startName = startName;
		}
		public String getEndName() {
			return endName;
		}
		public void setEndName(String endName) {
			this.endName = endName;
		}
		public String getRuleName() {
			return ruleName;
		}
		public void setRuleName(String ruleName) {
			this.ruleName = ruleName;
		}		
	
	}
	private static ConcurrentHashMap<Integer, ArrayList<LatestEventInfo>> latestEvents = new ConcurrentHashMap<Integer, ArrayList<LatestEventInfo>>();
	private static ConcurrentHashMap<MiscInner.Pair, Value> currentCache = new ConcurrentHashMap<MiscInner.Pair, Value>(numVehicles*numDims, loadFactor, concurrencyLevel);
	public static LatestEventInfo getLatestEvent(int vehicleId, int ruleId) {
		ArrayList<LatestEventInfo> curr = latestEvents.get(vehicleId);
		LatestEventInfo latest = null;
		for (int i=0,is=curr == null ? 0 : curr.size(); i<is; i++) {
			if (curr.get(i).getRuleId() == ruleId) {
				return curr.get(i);
			}
			if (latest == null || latest.getStartTime() < curr.get(i).getStartTime())//DEBUG13 put priority col in rule
				latest = curr.get(i);
		}
		if (Misc.isUndef(ruleId))
			return latest;
		return null;
	}
	
	public static LatestEventInfo getLatestOpenEvent(int vehicleId, int ruleId) {
		ArrayList<LatestEventInfo> curr = latestEvents.get(vehicleId);
		LatestEventInfo latest = null;
		for (int i=0,is=curr == null ? 0 : curr.size(); i<is; i++) {
			
			if (curr.get(i).getRuleId() == ruleId && curr.get(i).getStartTime()>0&&curr.get(i).getEndTime()<=0)// &&curr.get(i).getEndTime()<=0) 
				{
				
				return curr.get(i);
			}
		}
		if (Misc.isUndef(ruleId))
			return latest;
		return null;
	}
	
	public static void removeLatestEvent(int vehicleId, int ruleId) {
		ArrayList<LatestEventInfo> curr = latestEvents.get(vehicleId);
		for (int i=0,is=curr == null ? 0 : curr.size(); i<is; i++) {
			if (curr.get(i).getRuleId() == ruleId) {
				curr.remove(i);
				break;
			}
		}
		if (curr != null && curr.size() == 0)
			latestEvents.remove(vehicleId);
	}
	public static void addLatestEvent(int vehicleId, int ruleId, long st, long en, String stName, String enName, String ruleName) {
		ArrayList<LatestEventInfo> curr = latestEvents.get(vehicleId);
		if (curr == null) {
			curr = new ArrayList<LatestEventInfo>();
			latestEvents.put(vehicleId, curr);
		}
		LatestEventInfo entry = null;
		for (LatestEventInfo entryTemp : curr) {
			if (entryTemp.getRuleId() == ruleId) {
				entry = entryTemp;
				break;
			}
		}
		if (entry == null) {
			entry = new LatestEventInfo(ruleId, ruleName, st, en, stName, enName);
			curr.add(entry);
		}
		else {
			entry.setStartTime(st);
			entry.setEndTime(en);
			entry.setStartName(stName);
			entry.setEndName(enName);
		}
	}
	
	public static void add(int vehicleId, int dimId, int ival, double dval, String strVal, long dateVal,int type) {
		Value val = new Value();
		if(type == Cache.DATE_TYPE){
			val.setValue(dateVal);
		}else if(type == Cache.INTEGER_TYPE || type == Cache.LOV_TYPE){
			val.setValue(ival);
		}else if(type == Cache.NUMBER_TYPE){
			val.setValue(dval);
		}else{
			val.setValue(strVal);
		}
		MiscInner.Pair key = new MiscInner.Pair(vehicleId, dimId);
		currentCache.put(key, val);
	}
	public static void incrInt(int vehicleId, int dimId) {
		MiscInner.Pair key = new MiscInner.Pair(vehicleId, dimId);
		Value val = currentCache.get(key);
		if (val == null) {
			val = new Value(0);
			currentCache.put(key, val);
		}
		int oldValue = val.getIntVal();
		if (Misc.isUndef(oldValue))
			oldValue = 0;
		oldValue++;
		val.setValue(oldValue);
		currentCache.put(key, val);
	}

	public static void setToMin(int vehicleId, int dimId, long ts) {
		MiscInner.Pair key = new MiscInner.Pair(vehicleId, dimId);
		Value val = currentCache.get(key);
		if (val == null) {
			val = new Value(0L);
			currentCache.put(key, val);
		}
		long oldValue = val.getDateValLong();
		if (ts < oldValue || oldValue <= 0)
			oldValue = ts;
		val.setValue(oldValue);
		currentCache.put(key, val);
	}
	public static Value getValueInternal(Connection conn, int vehicleId, int dimId, CacheTrack.VehicleSetup vehSetup, VehicleDataInfo vdf) throws Exception {
		return getValueInternal(conn, vehicleId, dimId, vehSetup, vdf,null,null);
	}
	
	public static Value getValueInternal(Connection conn, int vehicleId, int dimId, CacheTrack.VehicleSetup vehSetup, VehicleDataInfo vdf,VehicleExtendedInfo vehicleExt,DriverExtendedInfo driverExt) throws Exception {
		MiscInner.Pair key = new MiscInner.Pair(vehicleId, dimId);
		Value valItem = null;
		if (valItem != null)
			return valItem;
		DimInfo dimInfo = DimInfo.getDimInfo(dimId);
		String tableName = null;
		GpsData currData = null;
		NewVehicleData _vdp  = null;
		Value loadStatus = null;
		int loadStatusVal = Misc.getUndefInt();
		if(dimInfo != null && dimInfo.m_colMap != null && dimInfo.m_colMap != null)
			tableName = dimInfo.m_colMap.table;
		if (dimInfo != null && dimInfo.m_id == 20765) {
			//stopped since from recent current data
			Triple<Long, Double, Double> stp = CacheTrack.getStopSinceTimeLonLat(conn, vehicleId);
			double dval = 0;
			if (stp != null && stp.first != null && !Misc.isUndef(stp.first)) {
				if (vdf == null)
					vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
				if (vdf != null) {
					long now = System.currentTimeMillis();
					NewVehicleData vdt = vdf.getDataList(conn, vehicleId, 0, false);
					long mxTs = vdt.getMaxTime();
					double gap = (double)(now - mxTs)/60000.0;
					if (gap > 240)
						dval = gap;
					else {
						dval = (double)(now - stp.first)/60000.0;
						if (dval < 0)
							dval = 0;
					}
				}
			}
			return new Value(dval);
		}
		if("current_data".equalsIgnoreCase(tableName) || "f$current_data".equalsIgnoreCase(tableName)){
			//vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
			if (vdf != null) {
				_vdp = vdf.getDataList(conn, vehicleId, 0, false);
				currData = _vdp.getLast(conn);
			}
		}
		if("summary_latest_trip_info".equalsIgnoreCase(tableName) && dimId != 20355){
			loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
		}
		switch (dimId) {
		case 37001: 
			ArrayList<Pair<Long,String>> scansList = ScanSupport.getScans(conn, vehicleId);
			valItem = new Value (scansList == null ? 0 : scansList.size());
			break;
		case 1 : valItem = new Value(vehSetup != null ? vehSetup.m_ownerOrgId : Misc.getUndefInt());
		break;
		//cacheTrack related fields
		
		case 20274 : valItem = new Value(vehicleId);
		break;
		case 9002 : valItem = new Value(vehSetup != null ? vehSetup.m_name : null);
		break;
		case 9003 : valItem = new Value(vehSetup != null ? vehSetup.m_type : Misc.getUndefInt());
		break;
		case 9005 : valItem = new Value(vehSetup != null ? vehSetup.getDeviceId() : null);
		break;
		case 9006 : valItem = new Value(vehSetup != null ? vehSetup.getSimNumber() : null);
		break;
		case 9008 : valItem = new Value(vehSetup != null ? vehSetup.m_detailed_status : Misc.getUndefInt());
		break;
		case 9083 : valItem = new Value(vehSetup != null ? vehSetup.m_detailed_status : null);//check
		break;
		case 20814 :  valItem = new Value(vehSetup != null ? vehSetup.m_subType : null);
		break;
		case 9080 : valItem = new Value(vehicleExt != null ? vehicleExt.getMiscellaneous() : null);
		break;
		case 9081 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldone() : null);
		break;
		case 9082 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldtwo() : null);
		break;
		case 20512 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldthree() : null);
		break;
		case 20513 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldfour() : null);
		break;
		case 20815 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldfive() : null);
		break;
		case 20816 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldsix() : null);
		break;
		case 20817 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldseven() : null);
		break;
		case 20818 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldeight() : null);
		break;
		case 20819 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldnine() : null);
		break;
		case 20820 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldten() : null);
		break;
		case 20821 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldeleven() : null);
		break;
		case 20822 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldtwelve() : null);
		break;
		case 20823 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldthirteen() : null);
		break;
		case 20824 : valItem = new Value(vehicleExt != null ? vehicleExt.getFieldfourteen() : null);
		break;
		case 20514 : valItem = new Value(vehicleExt != null ? vehicleExt.getDriver() : null);
			break;
		case 12001 : 
			//valItem = new Value(vehicleExt != null ? vehicleExt.m_work_area : null);
		break;
		//vehicle Extended fields
		
		
		case 20854 : valItem = new Value(vehicleExt != null ? vehicleExt.getCapacity() : Misc.getUndefInt());
		break;
		case 20855 : valItem = new Value(vehicleExt != null ? vehicleExt.getRegisterationNumber() : null);
		break;
		case 20856 : valItem = new Value(vehicleExt != null ? vehicleExt.getRegisterationNumberExpiry() : Misc.getUndefInt());
		break;
		case 20857 : valItem = new Value(vehicleExt != null ? vehicleExt.getInsuranceNumber() : null);
		break;
		case 20858 : valItem = new Value(vehicleExt != null ? vehicleExt.getInsuranceNumberExpiry() :  Misc.getUndefInt());
		break;
		case 20859 : valItem = new Value(vehicleExt != null ? vehicleExt.getPermit1Number(): null);
		break;
		case 20860 : valItem = new Value(vehicleExt != null ? vehicleExt.getPermit1NumberExpiry() :  Misc.getUndefInt());
		break;
		case 20861 : valItem = new Value(vehicleExt != null ? vehicleExt.getPermit1Desc() : null);
		break;
		case 20862 : valItem = new Value(vehicleExt != null ? vehicleExt.getPermit2Number() : null);
		break;
		case 20863 : valItem = new Value(vehicleExt != null ? vehicleExt.getPermit2NumberExpiry():  Misc.getUndefInt());
		break;
		case 20864 : valItem = new Value(vehicleExt != null ? vehicleExt.getPermit2Desc(): null);
		break;
		case 20865 : valItem = new Value(vehicleExt != null ? vehicleExt.getWorkingHrs(): Misc.getUndefInt());
		break;
		case 20866 : valItem = new Value(vehicleExt != null ? vehicleExt.getHiredFrom(): Misc.getUndefInt());
		break;
		case 20867 : valItem = new Value(vehicleExt != null ? vehicleExt.getRentalRateUsage(): Misc.getUndefInt());
		break;
		case 20868 : valItem = new Value(vehicleExt != null ? vehicleExt.getRentalRateRetainer(): Misc.getUndefInt());
		break;
		case 20869 : valItem = new Value(vehicleExt != null ? vehicleExt.getAcquisitionDate(): Misc.getUndefInt());
		break;
		case 20870 : valItem = new Value(vehicleExt != null ? vehicleExt.getReleaseDate(): Misc.getUndefInt());
		break;
		case 20871 : valItem = new Value(vehicleExt != null ? vehicleExt.getNotes(): null);
		break;
		case 20872 : valItem = new Value(vehicleExt != null ? vehicleExt.getPlant() : Misc.getUndefInt());
		break;
		case 20873 : valItem = new Value(vehicleExt != null ? vehicleExt.getPurpose() : Misc.getUndefInt());
		break;
		case 20874 : valItem = new Value(vehicleExt != null ? vehicleExt.getTransporterId() : Misc.getUndefInt());
		break;
		case 20875 : valItem = new Value(vehSetup != null ? vehSetup.m_customer_name : null);
		break;
		case 20876 : 
			//to do
		break;
		case 20877 : 
			//to do
		break;
		case 20878 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField1() : Misc.getUndefInt());
		break;
		case 20879 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField2() : Misc.getUndefInt());
		break;
		case 20880 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField3() : Misc.getUndefInt());
		break;
		case 20881 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField4() : Misc.getUndefInt());
		break;
		case 20882 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField5() : Misc.getUndefInt());
		break;
		case 20883 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField6() : Misc.getUndefInt());
		break;
		case 20884 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField7() : Misc.getUndefInt());
		break;
		case 20885 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField8() : Misc.getUndefInt());
		break;
		case 20886 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField9() : Misc.getUndefInt());
		break;
		case 20887 : valItem = new Value(vehicleExt != null ? vehicleExt.getLovField10() : Misc.getUndefInt());
		break;
		case 20888 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField1() : null);
		break;
		case 20889 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField2() : null);
		break;
		case 20890 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField3() : null);
		break;
		case 20891 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField4() : null);
		break;
		case 20892 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField5() : null);
		break;
		case 20893 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField6() : null);
		break;
		case 20894 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField7() : null);
		break;
		case 20895 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField8() : null);
		break;
		case 20896 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField9() : null);
		break;
		case 20897 : valItem = new Value(vehicleExt != null ? vehicleExt.getStrField10() : null);
		break;
		case 20831 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField1() : Misc.getUndefInt());
		break;
		case 20832 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField2() : Misc.getUndefInt());
		break;
		case 20833 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField3() : Misc.getUndefInt());
		break;
		case 20834 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField4() : Misc.getUndefInt());
		break;
		case 20835 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField5() : Misc.getUndefInt());
		break;
		case 20836 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField6() : Misc.getUndefInt());
		break;
		case 20837 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField7() : Misc.getUndefInt());
		break;
		case 20838 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField8() : Misc.getUndefInt());
		break;
		case 20839 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField9() : Misc.getUndefInt());
		break;
		case 20840 : valItem = new Value(vehicleExt != null ? vehicleExt.getDoubleField10() : Misc.getUndefInt());
		break;
		case 20841 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField1() : Misc.getUndefInt());
		break;
		case 20842 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField2() : Misc.getUndefInt());
		break;
		case 20843 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField3() : Misc.getUndefInt());
		break;
		case 20844 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField4() : Misc.getUndefInt());
		break;
		case 20845 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField5() : Misc.getUndefInt());
		break;
		case 20846 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField6() : Misc.getUndefInt());
		break;
		case 20847 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField7() : Misc.getUndefInt());
		break;
		case 20848 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField8() : Misc.getUndefInt());
		break;
		case 20849 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField9() : Misc.getUndefInt());
		break;
		case 20850 : valItem = new Value(vehicleExt != null ? vehicleExt.getDateField10() : Misc.getUndefInt());
		break;
        //latest vehicle interaction notes
		case 20461 : valItem = new Value(vehicleExt != null ? vehicleExt.getLastComment() : null);
		break;
		case 20462 : valItem = new Value(vehicleExt != null ? vehicleExt.getLastCommentTime() : Misc.getUndefInt());
		break;
		case 20463 : valItem = new Value(vehicleExt != null ? vehicleExt.getCommentUser() : null);
		break;
		case 21422 : valItem = new Value(vehicleExt != null ? vehicleExt.getNextFollowTime() : Misc.getUndefInt());
		break;
		
		
		
		//driver Extended
		case 20900 : valItem = new Value(driverExt != null ? driverExt.getId() : null);
		break;
		case 20901 : valItem = new Value(driverExt != null ? driverExt.getPortNodeId() : Misc.getUndefInt());
		break;
		case 20902 : valItem = new Value(driverExt != null ? driverExt.getDriverUID() : null);
		break;
		case 20903 : valItem = new Value(driverExt != null ? driverExt.getDriverName() : null);
		break;
		case 20904 : valItem = new Value(driverExt != null ? driverExt.getDriverDLNumber() : null);
		break;
		case 20905 : valItem = new Value(driverExt != null ? driverExt.getDriverMobileOne() : null);
		break;
		case 20906 : valItem = new Value(driverExt != null ? driverExt.getDriverMobileTwo() : null);
		break;
		case 20907 : valItem = new Value(driverExt != null ? driverExt.getDriverAddressOne() : null);
		break;
		case 20908 : valItem = new Value(driverExt != null ? driverExt.getDriverAddressTwo() : null);
		break;
		case 20909 : valItem = new Value(driverExt != null ? driverExt.getDriverInsuranceOne() : null);
		break;
		case 20910 : valItem = new Value(driverExt != null ? driverExt.getDriverInsuranceTwo() : null);
		break;
		case 20911 : valItem = new Value(driverExt != null ? driverExt.getInsuranceOneDate() : Misc.getUndefInt());
		break;
		case 20912 : valItem = new Value(driverExt != null ? driverExt.getInsuranceTwoDate() : Misc.getUndefInt());
		break;
		case 20913 : valItem = new Value(driverExt != null ? driverExt.getDlExpiryDate() : null);
		break;
		case 20914 : valItem = new Value(driverExt != null ? driverExt.getVehicleIdOne() : null);
		break;
		case 20915 : valItem = new Value(driverExt != null ? driverExt.getProvidedUID() : null);
		break;
		case 20916 : valItem = new Value(driverExt != null ? driverExt.getStatus() : null);
		break;
		case 20917 : 
			//valItem = new Value(driverExt != null ? driverExt.getDdtTraining() : null);
		break;
		case 20918 : 
			//valItem = new Value(driverExt != null ? driverExt.get : null);
		break;
		case 20919 : valItem = new Value(vehSetup != null ? vehSetup.m_name : null);
		break;
		case 20920 : 
			//valItem = new Value(driverExt != null ? driverExt.getDriverName() : null);
		break;
		case 20921 : 
			//valItem = new Value(driverExt != null ? driverExt.getDriverName() : null);
		break;
		case 21854 : {
			Value expiryVal = getValueInternal(conn, vehicleId, 20858, vehSetup, vdf);
			double dur = expiryVal != null && !Misc.isUndef(expiryVal.getDateValLong()) ? ((expiryVal.getDateValLong() - System.currentTimeMillis())/60000) :0;
			valItem = new Value(dur);
		}
		break;
		case 21855 : {
			//rc remaining
			Value expiryVal = getValueInternal(conn, vehicleId, 20856, vehSetup, vdf);
			double dur = expiryVal != null && !Misc.isUndef(expiryVal.getDateValLong()) ? ((expiryVal.getDateValLong() - System.currentTimeMillis())/60000) :0;
			valItem = new Value(dur);
		}
		break;
		case 21856 : {
			//permit 1 remaining
			Value expiryVal = getValueInternal(conn, vehicleId, 20860, vehSetup, vdf);
			double dur = expiryVal != null && !Misc.isUndef(expiryVal.getDateValLong()) ? ((expiryVal.getDateValLong() - System.currentTimeMillis())/60000) :0;
			valItem = new Value(dur);
		}
		break;
		case 21857 : {
			//permit 2 remaining
			Value expiryVal = getValueInternal(conn, vehicleId, 20863, vehSetup, vdf);
			double dur = expiryVal != null && !Misc.isUndef(expiryVal.getDateValLong()) ? ((expiryVal.getDateValLong() - System.currentTimeMillis())/60000) :0;
			valItem = new Value(dur);
		}
		break;
		case 20172: {
			//orientation
			valItem = new Value(currData != null ? currData.getOrientation() : 0.0);
			break;
		}
		//current data related fields
		case 20173 : {
			//gps record time
			valItem = getCurrentData(conn, vehicleId, dimId, currData, vehSetup.getDistCalcControl(conn).m_distThreshSame, vehSetup);
		}
		break;
		case 20165 : {
			//gps latitude
			valItem = getCurrentData(conn, vehicleId, dimId, currData, vehSetup.getDistCalcControl(conn).m_distThreshSame, vehSetup);
		}
		break;
		case 20166 : {
			//gps longitude
			valItem = getCurrentData(conn, vehicleId, dimId, currData, vehSetup.getDistCalcControl(conn).m_distThreshSame, vehSetup);
		}
		break;
		case 20167 : {
			//gps location
			valItem = getCurrentData(conn, vehicleId, dimId, currData, vehSetup.getDistCalcControl(conn).m_distThreshSame, vehSetup);
		}
		break;
		case 20168 : {
			//gps attribute value for attribute_id=0
			if(currData != null)
				valItem = new Value(currData.getValue());
		}
		break;
		case 20169 : {
			//speed
			if(currData != null)
				valItem = new Value(currData.getSpeed());
		}
		break;
		case 20261 : {
			//odometer day
			valItem = getGapData(conn, vdf, vehicleId, dimId, 0, Misc.SCOPE_DAY, false);
		}
		break;
		case 20294 : {
			valItem = getGapData(conn, vdf, vehicleId, dimId, 0, Misc.SCOPE_WEEK, false);
			//odometer week
		}
		break;
		case 20295 : {
			valItem = getGapData(conn, vdf, vehicleId, dimId, 0, Misc.SCOPE_MONTH, false);
			//odometer month
		}
		break;
		case 20296 : {
			//odometer day
			valItem = getGapData(conn, vdf, vehicleId, dimId, 0, Misc.SCOPE_DAY, false);
		}
		break;
		case 20297 : {
			//odometer week 
			valItem = getGapData(conn, vdf, vehicleId, dimId, 0, Misc.SCOPE_WEEK, false);
		}
		break;
		case 20298 : {
			//odometer month
			valItem = getGapData(conn, vdf, vehicleId, dimId, 0, Misc.SCOPE_MONTH, false);
		}
		break;
		case 20410 : {
			Value latestGpsRecordTime = getValueInternal(conn, vehicleId, 20173, vehSetup, vdf);
			if(latestGpsRecordTime != null && !Misc.isUndef(latestGpsRecordTime.getDateValLong()))
				valItem = new Value((System.currentTimeMillis() - latestGpsRecordTime.getDateValLong())/60000);
			//duration from last track
		}
		break;
		case 20266 : {
			//Ign On/Off column=attribute_value_2
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 2, false);
		}
		break;
		case 20267 : {
			//low ext battery column=attribute_value_24
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 24, false);
		}
		break;
		case 20275 : {
			//ext power on/off column=attribute_value_1
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1, false);
		}
		break;
		case 20284 : {
			//fuel level column=attribute_value_3
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 3, false);
		}
		break;
		case 20312 : {
			//ac on column=attribute_value_1200
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1200, false);
		}
		break;
		case 20313 : {
			//door closed column=attribute_value_1201
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1201, false);
		}
		break;
		case 20314 : {
			//immobilizer engaged column=attribute_value_1202
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1202, false);
		}
		break;
		case 20315 : {
			//alarm engaged column=attribute_value_1203
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1203, false);
		}
		break;
		case 20316 : {
			//seatbelt engaged column=attribute_value_1204
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1204, false);
		}
		break;
		case 20317 : {
			//raw fuel level column=speed_3
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 3, true);
		}
		break;
		case 20369 : {
			//air filter choked column=attribute_value_1208
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1208, false);
		}
		break;
		case 20370 : {
			//water-oil mixed column=attribute_value_1209
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1209, false);
		}
		break;
		case 20371 : {
			//high water temp column=attribute_value_1210
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1210, false);
		}
		break;
		case 20372 : {
			//high oil transmission temp column=attribute_value_1211
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 1211, false);
		}
		break;
		case 20373 : {
			//RPM column=attribute_value_21
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 21, false);
		}
		break;
		case 20720 : {
			//current engine on hours(ign based) column=speed_2
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 2, true);
		}
		break;
		case 20721 : {
			//current engine on hours(rpm based) column=speed_21
			valItem = getAttributeValue(conn, vdf, vehicleId, dimId, 21, true);
		}
		break;
		case 20724 : {
			//today engine on hours(ign based) column=gap_day_speed_2
			valItem = getGapData(conn, vdf, vehicleId, dimId, 2, Misc.SCOPE_DAY, true);
		}
		break;
		case 20730 : {
			//today engine off hours(ign based) column=gap_inverted_day_speed_2
			Value engineOnHours = getValueInternal(conn, vehicleId, 20724, vehSetup, vdf);
			if(engineOnHours != null && !Misc.isUndef(engineOnHours.getDateValLong())){
				long millisPased = Misc.getUndefInt();
				valItem = new Value((millisPased - engineOnHours.getDateValLong())/60000);
			}
		}
		break;
		case 20725 : {
			//week engine on hours(ign based) column=gap_week_speed_2
			valItem = getGapData(conn, vdf, vehicleId, dimId, 2, Misc.SCOPE_WEEK, true);
		}
		break;
		case 20726 : {
			//month engine on hours(ign based) column=gap_month_speed_2
			valItem = getGapData(conn, vdf, vehicleId, dimId, 2, Misc.SCOPE_MONTH, true);
		}
		break;
		case 20727 : {
			//today engine on hours(rpm based) column=gap_day_speed_21
			valItem = getGapData(conn, vdf, vehicleId, dimId, 21, Misc.SCOPE_DAY, true);
		}
		break;
		case 20728 : {
			//week engine on hours(rpm based) column=gap_week_speed_21
			valItem = getGapData(conn, vdf, vehicleId, dimId, 21, Misc.SCOPE_WEEK, true);
		}
		break;
		case 20729 : {
			//month engine on hours(rpm based) column=gap_month_speed_21
			valItem = getGapData(conn, vdf, vehicleId, dimId, 21, Misc.SCOPE_MONTH, true);
		}
		break;
		//end of current data fields
		case 21163: valItem = new Value(Misc.getUndefDouble());
			break;
		case 21161: { //dist remaining to dest
			Value totDistItem = getValueInternal(conn, vehicleId, 21163, vehSetup, vdf);
			Value startMarkerItem = getValueInternal(conn, vehicleId, 21188, vehSetup, vdf);
			double totDist = totDistItem == null ? Misc.getUndefDouble() : totDistItem.getDoubleVal();
			double startDist = startMarkerItem == null ? Misc.getUndefDouble() : startMarkerItem.getDoubleVal();
			double dval = Misc.getUndefDouble();

			if (!Misc.isUndef(totDist) && !Misc.isUndef(startDist)) {
				if(vdf == null)
					vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
				NewVehicleData vdp0 = null;
				GpsData en = null;
				if (vdf != null) {
					vdp0 = vdf.getDataList(conn, vehicleId, 0, false);
					en = vdp0.getLast(conn);
					dval = totDist+startDist - en.getValue();
				}
			}
			valItem = new Value(dval);
			break;
		}
		case 21436: {//Recent/Ongoing critical event
			Value startItem = getValueInternal(conn, vehicleId, 21431, vehSetup, vdf);
			int val = 0;
			if (startItem != null) {
				long dt = startItem.getDateValLong();
				if (dt > 0) {
					val = (System.currentTimeMillis()-dt) > 30*60*1000 ? 0 : 1;
				}
			}
			valItem = new Value(val);
			break;
			
		}
		
		case 21162 : { //IS MPL DElayed
			Value totDistItem = getValueInternal(conn, vehicleId, 21163, vehSetup, vdf);
			Value startMarkerItem = getValueInternal(conn, vehicleId, 21188, vehSetup, vdf);
			double totDist = totDistItem == null ? Misc.getUndefDouble() : totDistItem.getDoubleVal();
			double startDist = startMarkerItem == null ? Misc.getUndefDouble() : startMarkerItem.getDoubleVal();
			GpsData en = null;
			int ival = 0;
			if(vdf == null)
				vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
			NewVehicleData vdp0 = null;
			if (vdf != null) {
				vdp0 = vdf.getDataList(conn, vehicleId, 0, false);
				en = vdp0.getLast(conn);
			}
			if (!Misc.isUndef(totDist) && !Misc.isUndef(startDist) && en != null) {
				double gap = en.getValue() - totDist - startDist;
				ival = gap > (totDist > 60 ? 30 : 15) ? 1 : 0; 
			}
			if (ival == 0) {
				if(loadStatus == null)
					loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
				loadStatusVal = loadStatus == null ? 0 : loadStatus.getIntVal();
				Value relGout = null;
				if (loadStatusVal == 1) {
					relGout = getValueInternal(conn, vehicleId, 22044, vehSetup, vdf);
				}
				else if (loadStatusVal == 3) {
					relGout = getValueInternal(conn, vehicleId, 20496, vehSetup, vdf);
				}
				long relGoutTime = relGout == null ? Misc.getUndefInt() : relGout.getDateValLong();
				Value leadTimeItem = getValueInternal(conn, vehicleId, 21190, vehSetup, vdf);
				double leadTime = leadTimeItem == null ? Misc.getUndefDouble() : leadTimeItem.getDoubleVal();
				if (!Misc.isUndef(leadTime) && !Misc.isUndef(relGoutTime) && en != null) {
					double gap = en.getGps_Record_Time() - relGoutTime;
					ival = gap > (1.3*leadTime) ? 1 : 0;
				}
			}
			valItem = new Value(ival);
			break;
		}
		case 21164 : { //name="Is Delayed[On-Way]"
			int ival = 0;
			if(loadStatus == null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			if (loadStatus != null && loadStatus.getIntVal() == 1) {
				Value delayedItem = getValueInternal(conn, vehicleId, 21162, vehSetup, vdf);
				if (delayedItem != null && delayedItem.getIntVal() == 1)
					ival = 1;
			}
			valItem = new Value(ival);
			break;
		}
		case 21165 : { //name="Is Delayed[Way-Back]"
			int ival = 0;
			if(loadStatus == null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			if (loadStatus != null && loadStatus.getIntVal() == 3) {
				Value delayedItem = getValueInternal(conn, vehicleId, 21162, vehSetup, vdf);
				if (delayedItem != null && delayedItem.getIntVal() == 1)
					ival = 1;
			}
			valItem = new Value(ival);
			break;
		}
		case 21465 :{//name="MRS Latest Waiting Since"
			//Timestampdiff(minute, now(), 
			//     (case when summary_latest_trip_info.load_gate_out is null then summary_latest_trip_info.load_gate_in 
			//     when summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null 
			//                then summary_latest_trip_info.unload_gate_in 
			//      else null end))
			long now = System.currentTimeMillis();
			long sub = 0;
			Value lginItem = getValueInternal(conn, vehicleId,21191, vehSetup, vdf);			
			Value lgoutItem = getValueInternal(conn, vehicleId, 22044, vehSetup, vdf);
			if (lginItem != null && lginItem.getDateValLong() > 0 && (lgoutItem == null || lgoutItem.getDateValLong() <= 0)) {
				sub = lginItem.getDateValLong();
			}
			else {
				lginItem = getValueInternal(conn, vehicleId,21192, vehSetup, vdf);
				lgoutItem = getValueInternal(conn, vehicleId, 20496, vehSetup, vdf);
				if (lginItem != null && lginItem.getDateValLong() > 0 && (lgoutItem == null || lgoutItem.getDateValLong() <= 0)) {
					sub = lginItem.getDateValLong();
				}
			}
			int ival = Misc.getUndefInt();
			if (sub > 0) {
				ival = (int) ((now-sub)/60000);
			}
			valItem = new Value(ival);
			break;
		}
		case 21446 : {//name="MRS Latest Dist Travelled"
			//column=(case when current_data.attribute_value &gt; lgd21446.attribute_value then 1.05*(current_data.attribute_value-lgd21446.attribute_value) else 0 end) 
			if(loadStatus == null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus == null ? 0 : loadStatus.getIntVal();
			double dval = Misc.getUndefDouble();
			if (!Misc.isUndef(loadStatusVal)) {
				Value loadUnloadMarker = getValueInternal(conn, vehicleId, 21188, vehSetup, vdf);
				double startMarker = loadUnloadMarker != null ? loadUnloadMarker.getDoubleVal() : Misc.getUndefDouble();
				if (!Misc.isUndef(startMarker)) {
					if(vdf == null)
						vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
					NewVehicleData vdp0 = null;
					GpsData en = null;
					if (vdf != null) {
						vdp0 = vdf.getDataList(conn, vehicleId, 0, false);
						en = vdp0.getLast(conn);
						dval = en.getValue()-startMarker;
					}	
				}
			}
			valItem = new Value(dval);
			break;
		}
		case 21447 : {//name="MRS Dist Remaining"
			//column=(case when current_data.attribute_value &gt; lgd21446.attribute_value then 1.05*(current_data.attribute_value-lgd21446.attribute_value) else 0 end) 
			if(loadStatus == null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus == null ? 0 : loadStatus.getIntVal();
			double dval = Misc.getUndefDouble();
			if (!Misc.isUndef(loadStatusVal)) {
				Value loadUnloadMarker = getValueInternal(conn, vehicleId, 21188, vehSetup, vdf);
				Value invoiceDistKM = getValueInternal(conn, vehicleId, 21445, vehSetup, vdf);
				double startMarker = loadUnloadMarker != null ? loadUnloadMarker.getDoubleVal() : Misc.getUndefDouble();
				double totDist = invoiceDistKM == null ? Misc.getUndefDouble() : invoiceDistKM.getDoubleVal();
				if (!Misc.isUndef(startMarker) && !Misc.isUndef(totDist)) {
					if(vdf == null)
						vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
					NewVehicleData vdp0 = null;
					GpsData en = null;
					if (vdf != null) {
						vdp0 = vdf.getDataList(conn, vehicleId, 0, false);
						en = vdp0.getLast(conn);
						dval = totDist - en.getValue()+startMarker;
					}	
				}
			}
			valItem = new Value(dval);
			break;
		}
		case 21448: {//name="MRS Running Late"
			//distRemaining/350 = days needed to cover
			//days alloted = invoiceDistKm/350
			//days remaining = current time - loadGateOut or currentTime - loadGateOut
			double speed = 350;
			Value totDistItem = getValueInternal(conn, vehicleId, 21445, vehSetup, vdf);
			Value distRemainingItem= getValueInternal(conn, vehicleId, 21147, vehSetup, vdf);
			double totDist = totDistItem == null ? Misc.getUndefDouble() : totDistItem.getDoubleVal();
			double distRemaining = distRemainingItem == null ? Misc.getUndefDouble() : distRemainingItem.getDoubleVal();
			GpsData en = null;
			int ival = 0;
			if(vdf == null)
				vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
			NewVehicleData vdp0 = null;
			Value lgoutItem = getValueInternal(conn, vehicleId, 22044, vehSetup, vdf);
			Value ugoutItem = getValueInternal(conn, vehicleId, 20496, vehSetup, vdf);
			long exitTime = ugoutItem != null && ugoutItem.getDateValLong() > 0 ? ugoutItem.getDateValLong()
					: lgoutItem != null && lgoutItem.getDateValLong() > 0 ? lgoutItem.getDateValLong() 
							: Misc.getUndefInt();
					if (vdf != null) {
						vdp0 = vdf.getDataList(conn, vehicleId, 0, false);
						en = vdp0.getLast(conn);
					}
					if (!Misc.isUndef(totDist) && !Misc.isUndef(distRemaining) && en != null && exitTime > 0) {
						double daysRequiredToCover = distRemaining/speed;
						double daysAllotted = totDist/speed;
						double daysUsed = (double)(System.currentTimeMillis() - exitTime)/(double)(1000*3600*24);
						double daysAvailable = daysAllotted - daysUsed;
						if (daysRequiredToCover < 0.9 * daysAvailable) 
							ival = 1;
					}
					valItem = new Value(ival);
					break;
		}
		case 20458 : {
			int ccrVal = 0;
			if(loadStatus == null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			Value stopSince = getValueInternal(conn, vehicleId, 20255, vehSetup, vdf);
			if (stopSince != null && stopSince.m_dateVal != 0 &&  loadStatus != null && !Misc.isUndef(loadStatus.m_iVal)) {
				if(loadStatus.m_iVal == 0)
					ccrVal = 0;
				else if(loadStatus.m_iVal == 1 && stopSince.m_dateVal <= 0)
					ccrVal = 4;
				else if(loadStatus.m_iVal == 1 && stopSince.m_dateVal > 0)
					ccrVal = 5;
				else if(loadStatus.m_iVal == 2)
					ccrVal = 3;
				else if(loadStatus.m_iVal == 3 && stopSince.m_dateVal <= 0)
					ccrVal = 6;
				else if(loadStatus.m_iVal == 3 && stopSince.m_dateVal > 0)
					ccrVal = 7;
				else
					ccrVal = loadStatus.m_iVal;
				
			}
			valItem = new Value(ccrVal);
			break;
		}
		case 20255 :
		case 20256: { //stopped since/off since
			double dval = Misc.getUndefDouble();
			boolean doRule = vehSetup == null ? false : vehSetup.m_sendToRule;
			if (doRule) {
				dval = 0;
				Value stopTime = getValueInternal(conn, vehicleId, dimId == 20255 ? 21194 : 21195, vehSetup, vdf);
				long now = System.currentTimeMillis();
				if (vdf == null)
					vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
				if (vdf != null) {
					NewVehicleData vdt = vdf.getDataList(conn, vehicleId, 0, false);
					long mxTs = vdt.getMaxTime();
					double gap = (double)(now - mxTs)/60000.0;
					if (gap > 240)
						dval = gap;
					else {
						dval = (stopTime != null && stopTime.getDateValLong() > 0) ? (double)(now - stopTime.getDateValLong())/60000.0
								: 0;
						if (dval < 0)
							dval = 0;
					}
				}
			}
			valItem = new Value(dval);
			break;
		}
		case 83143: { //stopped since/off since and Idling Since
			double dval = Misc.getUndefDouble();
			if(vehSetup.m_type==65){
				if (vdf == null)
					vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
				if (vdf != null) {
					NewVehicleData vdt = vdf.getDataList(conn, vehicleId, 903, false);
					GpsData latestData = vdt.getLast(conn);
					Value attVal = null;
					if(latestData != null)
						attVal = new Value(latestData.getValue());
					if(attVal!=null && attVal.getIntVal()==1){
						long currTs = latestData.getGps_Record_Time();
						long startTs = vdt.getPrevAttributeValueChangeTime(conn,latestData);
						dval = (double)(Misc.isUndef(startTs)?(System.currentTimeMillis()-currTs):(currTs - startTs)/60000.0);
					}
				}
			}else{
				//for dumpers
				dval=getValueInternal(conn, vehicleId, 20255, vehSetup, vdf).getDoubleVal();
			}
			
			valItem = new Value(dval);
			break;
		}
		case 21431:
		case 21432:
		case 21433:
		case 21434: {
			valItem = currentCache.get(key);
			Value tripStatusItem = CacheValue.getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			if (tripStatusItem != null && tripStatusItem.getIntVal() != 1) {
				valItem = dimId == 21431 || dimId == 21432 ? new Value(0L) : new Value((String)null);
			}
			break;
		}
		case 21167:
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			if(!Misc.isUndef(loadStatusVal) && (loadStatusVal == INSIDE_LOAD || loadStatusVal == INSIDE_UNLOAD))
				valItem = getDetention(conn, vdf, vehicleId, dimId, 360*60*1000, Misc.getUndefInt(), LESS_THAN_EQUALS_TO, loadStatusVal, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) &lt;= 360) or (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null  and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) &lt;= 360) then 1 else 0 end)
			break;
		case 21168: 
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			valItem = new Value(!Misc.isUndef(loadStatusVal) && (loadStatusVal == TRANSIT_LOAD || loadStatusVal == TRANSIT_UNLOAD) ? 1 : 0);
			//(case when (summary_latest_trip_info.load_gate_out is not null and summary_latest_trip_info.unload_gate_in is null) or (summary_latest_trip_info.unload_gate_out is not null and summary_latest_trip_info.confirm_time is null) then 1 else 0 end);
			
			break;
		case 21169: 
			
			//(case when summary_latest_trip_info.unload_gate_in is null then summary_latest_trip_info.load_gate_op else summary_latest_trip_info.unload_gate_op end) 
			break;
		case 21171: 
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			if(!Misc.isUndef(loadStatusVal) && (loadStatusVal == INSIDE_LOAD || loadStatusVal == INSIDE_UNLOAD))
				valItem = getDetention(conn, vdf, vehicleId, dimId, 360*60*1000, 720*60*1000, BETWEEN, loadStatusVal, vehSetup);

			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) between 360.0001 and 720) or (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null  and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) between 360.0001 and 720) then 1 else 0 end)
			break;
		case 21172:
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			if(!Misc.isUndef(loadStatusVal) && (loadStatusVal == INSIDE_LOAD || loadStatusVal == INSIDE_UNLOAD))
				valItem = getDetention(conn, vdf, vehicleId, dimId, 720*60*1000, 1440*60*1000, BETWEEN, loadStatusVal, vehSetup);
			// ="(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) between 720.0001 and 1440) or (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null  and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) between 720.0001 and 1440) then 1 else 0 end)
			break;
		case 21173:
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			if(!Misc.isUndef(loadStatusVal) && (loadStatusVal == INSIDE_LOAD || loadStatusVal == INSIDE_UNLOAD))
				valItem = getDetention(conn, vdf, vehicleId, dimId, 1440*60*1000, 2880*60*1000, BETWEEN, loadStatusVal, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) between 1440.0001 and 2880) or (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null  and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) between 1440.001 and 2880) then 1 else 0 end)
			break;
		case 21174: 
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			if(!Misc.isUndef(loadStatusVal) && (loadStatusVal == INSIDE_LOAD || loadStatusVal == INSIDE_UNLOAD))
				valItem = getDetention(conn, vdf, vehicleId, dimId, 2880*60*1000, Misc.getUndefInt(), GREATER_THAN_EQUALS_TO, loadStatusVal, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) &gt; 2880) or (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null  and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) &gt; 2880) then 1 else 0 end)
			break;
		case 21175: 
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			valItem = new Value(!Misc.isUndef(loadStatusVal) && (loadStatusVal == INSIDE_LOAD || loadStatusVal == INSIDE_UNLOAD) ? 1 : 0);

			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null) or (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null) then 1 else 0 end)
			break;
		case 21176: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 360*60*1000, Misc.getUndefInt(), LESS_THAN_EQUALS_TO, INSIDE_LOAD, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) &lt;= 360) then 1 else 0 end)
			break;
		case 21177: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 360*60*1000, 720*60*1000, BETWEEN, INSIDE_LOAD, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) between 360.0001 and 720)  then 1 else 0 end)
			break;
		case 21178: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 720*60*1000, 1440*60*1000, BETWEEN, INSIDE_LOAD, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) between 720.0001 and 1440) then 1 else 0 end)
			break;
		case 21179: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 1440*60*1000, 2880*60*1000, BETWEEN, INSIDE_LOAD, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) between 1440.0001 and 2880) then 1 else 0 end)
			break;
		case 21180: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 2880*60*1000, Misc.getUndefInt(), GREATER_THAN_EQUALS_TO, INSIDE_LOAD, vehSetup);
			//(case when (summary_latest_trip_info.load_gate_in is not null and summary_latest_trip_info.load_gate_out is null and timestampdiff(minute, summary_latest_trip_info.load_gate_in, current_data.gps_record_time) &gt; 2880) then 1 else 0 end)
			break;
		case 21181: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 360*60*1000, Misc.getUndefInt(), LESS_THAN_EQUALS_TO, INSIDE_UNLOAD, vehSetup);
			//(case when (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) &lt;= 360) then 1 else 0 end)
			break;
		case 21182: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 360*60*1000, 720*60*1000, BETWEEN, INSIDE_UNLOAD, vehSetup);
			//(case when (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) between 360.0001 and 720)  then 1 else 0 end)
			break;
		case 21183: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 720*60*1000, 1440*60*1000, BETWEEN, INSIDE_UNLOAD, vehSetup);
			//(case when (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) between 720.0001 and 1440) then 1 else 0 end)
			break;
		case 21184: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 1440*60*1000, 2880*60*1000, BETWEEN, INSIDE_UNLOAD, vehSetup);
			//(case when (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) between 1440.0001 and 2880) then 1 else 0 end)
			break;
		case 21185: 
			valItem = getDetention(conn, vdf, vehicleId, dimId, 2880*60*1000, Misc.getUndefInt(), GREATER_THAN_EQUALS_TO, INSIDE_UNLOAD, vehSetup);
			//(case when (summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null and timestampdiff(minute, summary_latest_trip_info.unload_gate_in, current_data.gps_record_time) &gt; 2880) then 1 else 0 end)
			break;
		case 21186: 
			if(loadStatus != null)
				loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			loadStatusVal = loadStatus != null ? loadStatus.getIntVal() : Misc.getUndefInt();
			valItem = new Value(!Misc.isUndef(loadStatusVal) && loadStatusVal == INSIDE_UNLOAD ? 1 : 0);
			//(case when summary_latest_trip_info.unload_gate_in is not null and summary_latest_trip_info.unload_gate_out is null then 1 else 0 end) 
			break;
		case 21187: //load_gate_op
			
			break;
		
		case 21424: //followUpNeeded
			Value lastComentOnVehicle = getValueInternal(conn, vehicleId, 20462, vehSetup, vdf);
			Value nextFollowUp = getValueInternal(conn, vehicleId, 21422, vehSetup, vdf);
			if(lastComentOnVehicle == null || lastComentOnVehicle.getDateValLong() <= 0 || nextFollowUp == null || nextFollowUp.getDateValLong() <= 0)
				valItem = new Value(0);
			else{
				double changeVal = (System.currentTimeMillis() - lastComentOnVehicle.getDateValLong())/(nextFollowUp.getDateValLong()-lastComentOnVehicle.getDateValLong());
				int result = Double.compare(changeVal, 0.99);
				if(result < 0)
					result = 0;
				else
					result = 1;
				valItem = new Value(result);
			}
			//21424//(case when last_vehicle_interaction_notes.next_follow_time is null then 0 when Timestampdiff(minute, last_vehicle_interaction_notes.updated_on, now())/Timestampdiff(minute, last_vehicle_interaction_notes.updated_on, last_vehicle_interaction_notes.next_follow_time) &gt; 0.99 then 1 else 0 end)
			break;
		default : {
			valItem = currentCache.get(key); 
		}
		}
		return valItem;			
	}

	public static void dump(Connection conn, int vehicleId) {
		try {
			//1. get curr op status.xml raw
			//for each vehicle iterate through the dimConfig and print
			ArrayList<Integer> vehicleList = new ArrayList<Integer>();
			if (Misc.isUndef(vehicleId)) {
				PreparedStatement ps = conn.prepareStatement("select vehicle.id from vehicle where status=1");
				ResultSet rs = ps.executeQuery();

				while (rs.next()) 
					vehicleList.add(rs.getInt(1));
				rs.close();
				ps.close();

			}
			else {
				vehicleList.add(vehicleId);
			}
			FrontPageInfo currPageInfo = FrontPageInfo.getFrontPage("curr_op_status.xml", true, conn, Cache.getCacheInstance(conn));
			ArrayList<DimConfigInfo> dcList = currPageInfo.m_frontInfoList;
			StringBuilder sb = new StringBuilder();
			for (Integer vehInteger:vehicleList) {
				vehicleId = vehInteger.intValue();
				CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
				VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
				boolean foundValue = false;
				for (int i=0,is=dcList.size(); i<is; i++) {
					DimConfigInfo dci = dcList.get(i);
					DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
					if (dimInfo != null) {
						int dimId = dimInfo.m_id;
						Value value = CacheValue.getValueInternal(conn, vehicleId, dimId, vehSetup, vdf);
						String str = null;
						if (value != null) {

							if (dimInfo.m_type == Cache.DATE_TYPE) {
								long dt = value.getDateValLong();
								if (dt > 0)
									str = (new java.util.Date(dt)).toString();
							}
							else if (dimInfo.m_type == Cache.NUMBER_TYPE) {
								double dv  = value.getDoubleVal();
								if (!Misc.isUndef(dv)) {
									str = Double.toString(dv);
								}
							}
							else if (dimInfo.m_type == Cache.STRING_TYPE) {
								str = value.getStringVal();
							}
							else {
								int iv = value.getIntVal();
								if (!Misc.isUndef(iv))
									str = Integer.toString(iv);
							}
						}//if value is not null
						if (str != null) {
							if (!foundValue) {
								sb.append("[CURR_CACHE] Vehicle:").append(vehicleId);
							}
							sb.append("(").append(dimId).append(",").append(dimInfo.m_type).append(",").append(str).append(")");
							foundValue = true;
						}//if val for dim found
					}//if valid dim
				}//for each dc
				if (foundValue) {
					System.out.println(sb);
					sb.setLength(0);
				}
			}//for each vehicle
		}
		catch (Exception e) {

		}

	}
	private static Value getDetention(Connection conn, VehicleDataInfo vdf, int vehicleId, int dimId,long thresholdOne,long thresholdTwo, int operator,int filterLoadStatus, CacheTrack.VehicleSetup vehSetup ){//
		Value retval = null;
		GpsData latestData = null;
		boolean isValid = false;
		long val = Misc.getUndefInt();
		Value startItem = null;
		try{
			Value loadStatus = getValueInternal(conn, vehicleId, 20355, vehSetup, vdf);
			if(loadStatus == null || Misc.isUndef(loadStatus.getIntVal()) || loadStatus.getIntVal() != filterLoadStatus)
				return null;
			NewVehicleData  vdp = vdf.getDataList(conn, vehicleId, 0, false);
			latestData = vdp.getLast(conn);
			switch(filterLoadStatus){
			case INSIDE_LOAD :
				startItem = getValueInternal(conn, vehicleId, 21191, vehSetup, vdf);
				break;
			case TRANSIT_LOAD :
				startItem = getValueInternal(conn, vehicleId, 22044, vehSetup, vdf);
				break;
			case INSIDE_UNLOAD :
				startItem = getValueInternal(conn, vehicleId, 21192, vehSetup, vdf);
				break;
			case TRANSIT_UNLOAD :
				startItem = getValueInternal(conn, vehicleId, 20497, vehSetup, vdf);
				break;
			}
			if(startItem != null && startItem.getDateValLong() > 0 && latestData != null && latestData.getGps_Record_Time() > 0){
				val = latestData.getGps_Record_Time() - startItem.getDateValLong();
			}
			if(val > 0){
				switch(operator){
				case GREATER_THAN:
					isValid = val > thresholdOne;
					break;
				case LESS_THAN:
					isValid = val < thresholdOne;
					break;
				case EQUALS_TO:
					isValid = val == thresholdOne;
					break;
				case GREATER_THAN_EQUALS_TO:
					isValid = val >= thresholdOne;
					break;
				case LESS_THAN_EQUALS_TO:
					isValid = val <= thresholdOne;
					break;
				case NOT_EQUALS_TO:
					isValid = val != thresholdOne;
					break;
				case BETWEEN:
					isValid = val >= thresholdOne && val <= thresholdTwo;
					break;
				}
			}
			retval = new Value(isValid ? 1 : 0);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	private static Value getAttributeValue(Connection conn, VehicleDataInfo vdf, int vehicleId, int dimId, int attributeId, boolean useSpeedVal ){//
		Value retval = null;
		GpsData latestData = null;
		try{
			NewVehicleData  vdp = vdf.getDataList(conn, vehicleId, attributeId, false);
			latestData = vdp.getLast(conn);
			if(latestData != null){
				if(useSpeedVal){
					retval = new Value(latestData.getSpeed());
				}else{
					if(attributeId == 0)
						retval = new Value(latestData.getValue()*1.05);
					else
						retval = new Value(latestData.getValue());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	private static Value getGapData(Connection conn, VehicleDataInfo vdf, int vehicleId, int dimId, int attributeId, int granularity, boolean useSpeedVal ){//
		Value retval = null;
		long refrenceTime = Misc.getUndefInt();
		GpsData referenceData = null;
		GpsData latestData = null;
		try{
			NewVehicleData  vdp = vdf.getDataList(conn, vehicleId, attributeId, false);
			switch (granularity) {
			case Misc.SCOPE_MONTH:
				refrenceTime = vdp.getOdometerMonthRecTime();
				break;
			case Misc.SCOPE_WEEK:
				refrenceTime = vdp.getOdometerWeekRecTime();
				break;
			default:
				refrenceTime = vdp.getOdometerDayRecTime();
				break;
			}
			Date begOfRefrence = TimePeriodHelper.getBegOfDate(new Date(refrenceTime), granularity);
			Date begOfDate = TimePeriodHelper.getBegOfDate(new Date(System.currentTimeMillis()), granularity);
			if (begOfDate.getTime() != begOfRefrence.getTime()) {
				referenceData = null;
			}
			else {
				referenceData = granularity == Misc.SCOPE_DAY ? vdp.getOdoDayGps(conn) : granularity == Misc.SCOPE_MONTH ? vdp.getOdoMonthGps(conn) : vdp.getOdoWeekGps(conn);
			}
			latestData = vdp.getLast(conn);
			
			if(referenceData != null && latestData != null){
				if(useSpeedVal){
					retval = new Value(latestData.getSpeed() - referenceData.getSpeed());
				}else{
					if(attributeId == 0)
						retval = new Value((latestData.getValue() - referenceData.getValue())*1.05);
					else
						retval = new Value((latestData.getValue() - referenceData.getValue()));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	private static Value getCurrentData(Connection conn, int vehicleId, int dimId, GpsData gpsData, double distThresh, CacheTrack.VehicleSetup vehSetup){
		Value retval = null;
		long inCacheTime = Misc.getUndefInt() ;
		long latestTimeFromGpsData = Misc.getUndefInt();
		double lonFromCache = Misc.getUndefDouble();
		double latFromCache = Misc.getUndefDouble();
		if(Misc.isUndef(distThresh))
			distThresh = 0.05;
		try{
			inCacheTime = currentCache.get(new MiscInner.Pair(vehicleId, 20173)) != null ? currentCache.get(new MiscInner.Pair(vehicleId, 20173)).getDateValLong() : Misc.getUndefInt();
			if(gpsData != null){
				latestTimeFromGpsData = gpsData.getGps_Record_Time();
				if (inCacheTime == latestTimeFromGpsData) {
					//do notthing
					//retval = new Value(latestTimeFromGpsData);
				}else {
					if(currentCache.get(new MiscInner.Pair(vehicleId, 20166)) != null)
						lonFromCache = currentCache.get(new MiscInner.Pair(vehicleId, 20166)).getDoubleVal();
					if(currentCache.get(new MiscInner.Pair(vehicleId, 20165)) != null)
						latFromCache = currentCache.get(new MiscInner.Pair(vehicleId, 20165)).getDoubleVal();
					boolean undefLatLong = Misc.isUndef(lonFromCache)  || Misc.isUndef(latFromCache) ;
					boolean prevStartsWithZero =  Double.compare(lonFromCache, 0.0) == 0 || Double.compare(latFromCache, 0.0) == 0;
					boolean currStartsWithZero = gpsData.isZeroCoord();
					if (undefLatLong || (prevStartsWithZero == currStartsWithZero && Double.compare(gpsData.distance(lonFromCache, latFromCache), distThresh) > 0)) {
						// update all cache
						currentCache.put(new MiscInner.Pair(vehicleId, 20173), new Value(latestTimeFromGpsData));
						currentCache.put(new MiscInner.Pair(vehicleId, 20165), new Value(gpsData.getLatitude()));
						currentCache.put(new MiscInner.Pair(vehicleId, 20166), new Value(gpsData.getLongitude()));
						currentCache.put(new MiscInner.Pair(vehicleId, 20167), new Value(gpsData.getName(conn, vehicleId, vehSetup)));
					}else {
						//update the time in cache
						currentCache.put(new MiscInner.Pair(vehicleId, 20173), new Value(latestTimeFromGpsData));
					}
				}
			}
			retval = currentCache.get(new MiscInner.Pair(vehicleId, dimId));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static boolean isInitialized(){
		return initialized;
	}
	/*public static void initReportCache(int cacheId,ResultInfo resultInfo,FrontPageInfo fpi,int vehicleIndex){
		ArrayList<DimConfigInfo> fpList = null;
		int cols = 0;
		int vehicleId = Misc.getUndefInt();
		DimInfo dim = null;
		if(resultInfo == null || fpi == null )
			return;
		fpList = fpi.m_frontInfoList;
		if(fpList != null)
			cols = fpList.size();
		if(Misc.isUndef(cols))
			return;
		try{
			while(resultInfo.next()){
				vehicleId = resultInfo.getVal(vehicleIndex).m_iVal;
				for(int i=0; i<cols; i++) {
					dim = fpList != null && fpList.get(i) != null && fpList.get(i).m_dimCalc != null ? fpList.get(i).m_dimCalc.m_dimInfo : null;
					if(fpList == null || fpList.get(i) == null || fpList.get(i).m_dimCalc == null || fpList.get(i).m_dimCalc.m_dimInfo == null || Misc.isUndef(fpList.get(i).m_dimCalc.m_dimInfo.m_id) || !fpList.get(i).m_isCached)
						continue;
					Value val = resultInfo.getVal(i);
					add(vehicleId, fpList.get(i).m_dimCalc.m_dimInfo.m_id, val.m_iVal, val.m_dVal, val.m_strVal, (val.m_dateVal == null ? Misc.UNDEF_VALUE : val.m_dateVal.getTime()),dim.m_type);
					//setDimVal(vehicleId, cacheId, fpList.get(i).m_dimCalc.m_dimInfo.m_id, resultInfo.getVal(i));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			initialized = true;
		}
	}*/
	
	private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,ArrayList<LatestEventInfo>>> latestEventsList = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,ArrayList<LatestEventInfo>>>();
	public static ArrayList<LatestEventInfo> getLatestEventList(int vehicleId, int ruleId, long time) {
		try {
			ArrayList<LatestEventInfo> result = new ArrayList<LatestEventInfo> ();
			ArrayList<LatestEventInfo> remove = new ArrayList<LatestEventInfo> ();			
			ConcurrentHashMap<Integer,ArrayList<LatestEventInfo>> currHashMap = latestEventsList.get(vehicleId);
			if (currHashMap != null) {
				ArrayList<LatestEventInfo> curr = currHashMap.get(ruleId);
				if (curr != null) {
					for (int i=(curr.size()-1); i>0; i--) {
						LatestEventInfo eventInfo = curr.get(i);
						long startTime = eventInfo.getStartTime();
						if (startTime < time) {
							remove.add(eventInfo);
						} else {
							result.add(eventInfo);
						}
					}		
					Iterator<LatestEventInfo> itr = remove.iterator();
					while (itr.hasNext()) {
						LatestEventInfo li = itr.next();
						curr.remove(li);
					}
				}
			}
			return result;
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/*public static void addToLatestEventList(int vehicleId, int ruleId, long st, long en, String stName, String enName, String ruleName) {
		try {
			HashMap<Integer, ArrayList<LatestEventInfo>> currHashMap = latestEventsList.get(vehicleId);
			if (currHashMap == null) {
				currHashMap = new HashMap<Integer,ArrayList<LatestEventInfo>> ();
				ArrayList<LatestEventInfo> curr = new ArrayList<LatestEventInfo> (); 
				currHashMap.put(ruleId, curr);			
				latestEventsList.put(vehicleId, currHashMap);
			}
					
			ArrayList<LatestEventInfo> curr = currHashMap.get(ruleId);		
			LatestEventInfo entry = new LatestEventInfo(ruleId, ruleName, st, en, stName, enName);
			curr.add(entry);	
		} catch (Exception e){
			e.printStackTrace();
		}
	}	*/
	
	public static void addToLatestEventList(int vehicleId, int ruleId, long st, long en, String stName, String enName, String ruleName) {
        try {
                       ConcurrentHashMap<Integer, ArrayList<LatestEventInfo>> currHashMap = latestEventsList.get(vehicleId);
                       if (currHashMap == null) {
                          currHashMap = new ConcurrentHashMap<Integer,ArrayList<LatestEventInfo>> ();
                          ArrayList<LatestEventInfo> curr = new ArrayList<LatestEventInfo> (); 
                          currHashMap.put(ruleId, curr);                                   
                          latestEventsList.put(vehicleId, currHashMap);
                       }
                                                     
                       ArrayList<LatestEventInfo> curr = currHashMap.get(ruleId);                           
                       LatestEventInfo entry = new LatestEventInfo(ruleId, ruleName, st, en, stName, enName);
                       if (curr.size() > CacheValue.MAX_EVENTS_PER_RULE_TO_KEEP) {
                            for (int i1=0,i1s=curr.size()-1;i1<i1s;i1++)
                                        curr.set(i1+1, curr.get(i1));
                                      curr.remove(curr.size()-1);
                       }
                       curr.add(entry);  
        } catch (Exception e){
                       e.printStackTrace();
        }
}

	
}

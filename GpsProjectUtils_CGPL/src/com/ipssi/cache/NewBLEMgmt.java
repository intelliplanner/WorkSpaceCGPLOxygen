package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.common.ds.trip.ShovelSequence;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;

public class NewBLEMgmt {
	public final static String GET_BLE_BOUND = BLEInfo.GET_BLE_SEL  +
	" from ble_read_event  where vehicle_id=?  and gps_record_time > ? and gps_record_time < ?  order by gps_record_time ";
public final static String GET_BLE_BOUND_LOWER =  BLEInfo.GET_BLE_SEL +
	" from ble_read_event where vehicle_id=? and gps_record_time > ? and gps_record_time < ?    order by gps_record_time desc ";	
public final static String GET_BLE_BY_COUNT_HIGHER =  BLEInfo.GET_BLE_SEL +
	" from ble_read_event  where vehicle_id=? and gps_record_time > ?     order by gps_record_time  limit ? ";
public final static String GET_BLE_BY_COUNT_LOWER = BLEInfo.GET_BLE_SEL +
" from ble_read_event  where vehicle_id=? and gps_record_time < ?    order by gps_record_time desc limit ? ";
private static final long G_MAX_MILLISEC_PRIOR_FOR_BLETOBE_WORKING = 20*60*1000;
public static final long MAX_PRE_BLE_TO_LOOK_FOR_MILLI = 5*60*1000; //was 15
public static final long MAX_01_BLE_GAP_TO_IGNORE_0 = 90*1000;
public static long g_infinite_future = System.currentTimeMillis()+7*24*3600*1000L;  
public static long g_infinite_past = System.currentTimeMillis()-7*24*3600*1000L;
public static int g_reg_maxPoints = 300;//make it divisble by 3 and 4 to be easier
public static int g_reg_maxPointsForForceClean =(int)(g_reg_maxPoints*10);//14400;//if the number of points after add becomes this much ... then clean up ... either
public static int g_reg_deltaLookAheadSec = 3600*1*5;//g_maxPoints < 500 ? g_maxPoints/30*60*10 : ;//5*50;//10*60;
public static int g_reg_deltaLookAheadCount = 50;//g_maxPoints/15*10;//3;//20;

private int vehicleId = Misc.getUndefInt();
private long minTime = -1;
private long maxTime = -1;
private int prevAddAt = -1;
private int prevReadAt = -1;
private FastList<BLEInfo> dataList = new FastList<BLEInfo>();

private BLEInfo latestReceivedData = null;
private BLEInfo prevLatestReceivedData = null;

private long internalLatestRecvTime = Misc.getUndefInt();
private long internalLatestRecordTime = Misc.getUndefInt();
private long internalLastMinMaxCheckAt = -1;

private static ConcurrentHashMap<Integer, NewBLEMgmt> vehicleCache = new ConcurrentHashMap<Integer, NewBLEMgmt>();
public static NewBLEMgmt getBLEReadList(Connection conn, int vehicleId) {
	return getBLEReadList(conn, vehicleId, false); //unlike reg, we are allowing things to be created
}
public static NewBLEMgmt getBLEReadList(Connection conn, int vehicleId, boolean dontCreate) {
	NewBLEMgmt retval = vehicleCache.get(vehicleId);
	if (retval == null && !dontCreate) {
		retval = new NewBLEMgmt(vehicleId, -1, -1);
		retval.init(conn);
		vehicleCache.put(vehicleId, retval);
	}
	return retval;
}
public String toString() {
	return "["+vehicleId+","+minTime+","+maxTime+","+dataList.toString();
}


public NewBLEMgmt(int vehicleId, long mi, long mx) {
	this.vehicleId = vehicleId;
	this.minTime = mi;
	this.maxTime = mx;
}

public void clean() {//suggestedMin is the earliest from LUSequence
	int sz = dataList.size();
	int maxPoints = g_reg_maxPoints;
	int maxPointsForForceClean = g_reg_maxPointsForForceClean;
	if (sz > maxPoints) {
		int desiredLHSCountBeforeAdd = maxPoints/6;
		if (prevAddAt == 0)
			return;
		boolean doCleanUp = sz > maxPointsForForceClean;
		int indexToRemoveFrom = prevAddAt;
		
		if (!doCleanUp) {
			//1. we must be going forward
			if (this.latestReceivedData != null && this.prevLatestReceivedData != null) {
				long gp = this.latestReceivedData.getGpsRecvTime() - this.prevLatestReceivedData.getGpsRecvTime();
				if (gp > 0 && prevAddAt >= sz-desiredLHSCountBeforeAdd) {
					//either gap < 10 min or these two points must be consecutive
					if (gp >= 10*60*1000)
						doCleanUp = true;
					else {
						BLEInfo prevToAdd = dataList.get(prevAddAt-1);
						if (prevToAdd != null && prevLatestReceivedData.getGpsRecordTime() == prevToAdd.getGpsRecordTime()) {
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
		}
	}
}

public void remove(BLEInfo data) {
	Pair<Integer, Boolean> idx = dataList.indexOf(data);
	if (idx.second)
		dataList.remove(idx.first);
}

public void recordTimes(BLEInfo data, boolean doIfApprop) {
	long ts = data.getGpsRecordTime();
	if (minTime <= 0 || minTime > ts)
		minTime = ts;
	if (maxTime <= 0 || maxTime < ts)
		maxTime = ts;
	if (!doIfApprop || latestReceivedData == null || latestReceivedData.getGpsRecvTime() < data.getGpsRecvTime()) {
		prevLatestReceivedData = latestReceivedData;
		latestReceivedData = data;
	}
}


public void add(Connection conn, BLEInfo data, MiscInner.PairLong loadEventChangeTrack) {
	internalLatestRecvTime = data.getGpsRecvTime();
	internalLatestRecordTime = data.getGpsRecvTime();

	Pair<Integer, Boolean> idx = indexOf(conn, data);
	int pos = idx.first;
	
	boolean isSame = idx.second;
	boolean done = false;
	BLEInfo currPtBeingReplaced = null;;
	
	if (isSame) {
		prevAddAt = pos;
		done = true;
	}
	boolean added = false;
	if (!done) {			
		dataList.addAtIndex(pos+1, data);
		prevAddAt = pos+1;
		added = true;
	}
	recordTimes(data, false);
	clean();
	prevReadAt = prevAddAt;
	//validateAdd(data);
	
	return;
}



public Pair<Integer, Boolean> indexOf(Connection conn, BLEInfo data) {
	Pair<Integer, Boolean> retval = null;
	retval = dataList.indexOf(data);
	int deltaLookAheadCount = g_reg_deltaLookAheadCount;

	if (!retval.second) {
		int idxInt = retval.first;
		if (idxInt < 0 && minTime <= data.getGpsRecordTime() && minTime > 0) {
			long currMi = estimateMinDate(data.getGpsRecordTime());
			readDataToLeft(conn, currMi,  data.getGpsRecordTime());
			retval = dataList.indexOf(data);
			idxInt = retval.first;
			if (idxInt < 0 && minTime <= data.getGpsRecordTime() && minTime > 0) { //read at least 1 pt before so that we can return the prior pt .. dont call get() ... may lead to infinite recursion
				long refTime = dataList.size() == 0 ? data.getGpsRecordTime()+1000 : dataList.get(0).getGpsRecordTime();
				if (minTime > 0 && (dataList.size() == 0 || minTime < dataList.get(0).getGpsRecordTime()) && minTime <= data.getGpsRecordTime()) {
					ArrayList<BLEInfo> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, false);
					//dataList.mergeAtLeftReverse(readData);
					//prevAddAt += readData.size();
					//prevReadAt += readData.size();
					int addCnt = specializedMerge(readData,true);
					prevAddAt += addCnt;
					prevReadAt += addCnt;
				}
				retval = dataList.indexOf(data);
			}
		}
		else if (idxInt >= dataList.size()-1 && maxTime >= data.getGpsRecordTime() && minTime > 0) {//NOT VERIFIED
			long currMx = estimateMaxDate(data.getGpsRecordTime());
			readDataToRight(conn, currMx, data.getGpsRecordTime());
			retval = dataList.indexOf(data);
			idxInt = retval.first;
			int sz = dataList.size();
			if (!retval.second && idxInt >= sz-1  && (sz == 0 || maxTime > dataList.get(sz-1).getGpsRecordTime()) && maxTime >= data.getGpsRecordTime() && minTime > 0) {
				long refTime = sz == 0 ? data.getGpsRecordTime()-1000 : dataList.get(sz-1).getGpsRecordTime();
				ArrayList<BLEInfo> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, true);
				//dataList.mergeAtRight(readData);
				int addCnt = specializedMerge(readData,false);
			}
			retval = dataList.indexOf(data);
		}
	}
	return retval;
}

private int specializedMerge(ArrayList<BLEInfo> readData, boolean isReverse) {
	int itemAddedCount = 0;
	for (int i=isReverse? readData.size()-1 : 0, is = isReverse?0:readData.size()-1, incr = isReverse?-1:1;
		isReverse ? i>=is : i <= is; i+=incr) {
		BLEInfo itemToAdd = readData.get(i);
		Pair<Integer, Boolean> posToAdd = this.dataList.indexOf(itemToAdd);
		if (posToAdd.second) { //found matching ..
			dataList.replaceAt(posToAdd.first, itemToAdd);
		}
		else {
			dataList.add(itemToAdd);
			itemAddedCount++;
		}
	}
	return itemAddedCount;
}

public BLEInfo get(Connection conn, BLEInfo data) {
	return get(conn, data, false);
}

public BLEInfo get(Connection conn, BLEInfo data, boolean strictLess) {
	Pair<Integer,Boolean> idx = indexOf(conn, data);
	int idxInt =  idx.first;
	if (strictLess && idx.second) {
		return get(conn, idxInt, -1, data);
	}
	else {
		if (idxInt < prevReadAt)
			prevReadAt = idxInt;
		return idxInt < 0 || idxInt >= dataList.size() ? null : dataList.get(idxInt);
	}
}

public BLEInfo get(Connection conn, BLEInfo data, int relIndex) {
	Pair<Integer,Boolean> idx = indexOf(conn, data);
	int idxInt =  idx.first;
	return get(conn, idxInt, relIndex, data);
}

public BLEInfo get(Connection conn, int dataIndex, int relIndex, BLEInfo data) { //this gets regardless of recv time constraint
	if (true) //g_ignoreUpdatedOn ||  this.currentTimingsAreOf == 0 || this.latestReceivedData == null)
		return getWithoutRecvConstraint(conn, dataIndex, relIndex, data);
	return null;
}

public BLEInfo getWithoutRecvConstraint(Connection conn, int dataIndex, int relIndex, BLEInfo data) { //this gets regardless of recv time constraint
	int ask = dataIndex+relIndex;
	int sz = dataList.size();
	BLEInfo retval = null;
	int deltaLookAheadCount = g_reg_deltaLookAheadCount;

	if (ask < 0){
		long refTime = sz == 0 ? data.getGpsRecordTime()+1000 : dataList.get(0).getGpsRecordTime();
		if (minTime > 0 && (sz == 0 || minTime < dataList.get(0).getGpsRecordTime()) && minTime <= data.getGpsRecordTime()) {
			ArrayList<BLEInfo> readData = readHelperByCount(conn, refTime, -1*ask+deltaLookAheadCount, false);
			//dataList.mergeAtLeftReverse(readData);
			//ask += readData.size();
			//prevAddAt += readData.size();
			//prevReadAt += readData.size();
			int addCnt = specializedMerge(readData,true);
			ask += addCnt;
			prevAddAt += addCnt;
			prevReadAt += addCnt;
		}
	}
	else if (ask >= sz) {
		long refTime = sz == 0 ? data.getGpsRecordTime()-1000 : dataList.get(sz-1).getGpsRecordTime();
		if (maxTime > 0 && (sz == 0 || maxTime > dataList.get(sz-1).getGpsRecordTime()) && maxTime >= data.getGpsRecordTime()) {
			ArrayList<BLEInfo> readData = readHelperByCount(conn, refTime, ask-sz-1+deltaLookAheadCount, true);
			//dataList.mergeAtRight(readData);
			int addCnt = specializedMerge(readData,false);
		}
	}
	
	int ti  = ask<0 || ask>=dataList.size() ? prevReadAt : ask;
	if (ti < prevReadAt)
		prevReadAt = ti;
	return ask < 0 || ask >= dataList.size() ? null : dataList.get(ask);
}

public BLEInfo getEndData() {
	return this.dataList.get(dataList.size()-1);
}
public boolean isAtEnd(BLEInfo data) {
	//kind of complex - if in recoveryMode or mode <= 0 or mode > 2 then simply data.getGps_Record_Time() >= maxTime
	//else if mode = 1 (for RP) - will return true only if all points after this are not RP processed
	//else if mode = 2 (for TP) - will return true only if all points adter this are not TP processed
	long drt = data.getGpsRecordTime();
	return drt >= this.maxTime;
}
public BLEInfo getLast(Connection conn) { //Assume
	BLEInfo last =  dataList.get(dataList.size()-1);
	if (last == null || last.getGpsRecordTime() >= maxTime)
		return last;
	BLEInfo dummy = new BLEInfo(maxTime);
	return get(conn, dummy);
}
public BLEInfo simpleGet(int index) {
	return dataList.get(index);
}
public void simpleReplaceAt(int index, BLEInfo data) {
	dataList.replaceAt(index,data);
}
public int simpleSize() {
	return dataList.size();
}
public void resetTimes() {
	minTime = -1;
	maxTime = -1;
	this.internalLastMinMaxCheckAt = -1;
	this.internalLatestRecordTime = -1;
	this.internalLatestRecvTime = -1;
	this.prevAddAt = -1;
	this.prevLatestReceivedData = null;
	this.latestReceivedData = null;
	dataList.clear();
}
public void reinit(Connection conn) {
	resetTimes();
	init(conn);
}
public void init(Connection conn) {
	if (minTime >= 0 || maxTime >= 0)
		return;
	PreparedStatement ps = null;
	ResultSet rs = null;
	try {
		ps = conn.prepareStatement("select min(gps_record_time), max(gps_record_time) from ble_read_event where vehicle_id = ?");
		ps.setInt(1, vehicleId);			
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
	int deltaLookAheadCount = g_reg_deltaLookAheadCount;
	return askTime - (g_reg_deltaLookAheadSec)*1000;
}
private long estimateMaxDate(long askTime) {
	return askTime +( g_reg_deltaLookAheadSec)*1000;
}

private void readDataToLeft(Connection conn, long mi, long ref) {
	long mx = dataList.size() == 0 ? ref+1000 : dataList.get(0).getGpsRecordTime();
	ArrayList<BLEInfo> data = readHelperByTimeBound(conn, mi, mx, false);
	//dataList.mergeAtLeftReverse(data);
	//prevAddAt += data.size();
	//prevReadAt += data.size();
	int addCnt = specializedMerge(data,true);
	prevAddAt += addCnt;
	prevReadAt += addCnt;
}
private void readDataToRight(Connection conn, long mx, long ref) {
	long mi = dataList.size() == 0 ? ref-1000 : dataList.get(dataList.size()-1).getGpsRecordTime();
	ArrayList<BLEInfo> data = readHelperByTimeBound(conn, mi, mx, true);
	//dataList.mergeAtRight(data);
	int addCnt = specializedMerge(data,false);
}


private ArrayList<BLEInfo> readHelperByTimeBound(Connection conn, long mi, long mx, boolean getHigher) {
	PreparedStatement ps = null;
	ResultSet rs = null;
	ArrayList<BLEInfo> retval = new ArrayList<BLEInfo>(g_reg_maxPoints/3);
	try {
//		public final static String GET_BLE_BOUND = "select ch.vehicle_id,  ch.port_node_id, ch.gps_record_time, ch.LOAD_EVENT_rec_date, ch.chd_id, ch.consignee, ch.dest_code, ch.dest_addr_1, ch.dest_addr_2, ch.dest_addr_3, ch.dest_addr_4, ch.dest_city, ch.dest_state, ch.material_id "+
//		" from ble_read_event where vehicle_id=?  and gps_record_time > ? and gps_record_time < ?  order by gps_record_time ";
		
		ps = conn.prepareStatement(getHigher ? (GET_BLE_BOUND) : (GET_BLE_BOUND_LOWER));
		
		int colIndex = 1;
		ps.setInt(colIndex++, vehicleId);
		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mi));
		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mx));

		//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
		//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
		rs = ps.executeQuery();
		boolean prevIsInDataList = true;
		while (rs.next()) {
			BLEInfo curr = BLEInfo.read(conn, rs);
			retval.add(curr);
		}
		rs.close();
		rs = null;
		ps.close();
		ps = null;
		for (int i=0,is=retval == null ? 0 : retval.size();i<is;i++) {
			BLEInfo curr = retval.get(i);
			if (curr.getGpsRecvTime() > 0 && curr.getGpsRecvTime() > this.internalLatestRecvTime) {
				this.internalLatestRecvTime = curr.getGpsRecvTime();
			}
		}
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

private ArrayList<BLEInfo> readHelperByCount(Connection conn, long timeref, int cnt, boolean getHigher) {
	PreparedStatement ps = null;
	ResultSet rs = null;
	ArrayList<BLEInfo> retval = new ArrayList<BLEInfo>(g_reg_maxPoints/3);
	try {
		ps = conn.prepareStatement(getHigher? (GET_BLE_BY_COUNT_HIGHER) : (GET_BLE_BY_COUNT_LOWER));
		int colIndex = 1;
		ps.setInt(colIndex++, vehicleId);
		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(timeref));
		ps.setInt(colIndex++, cnt);
		rs = ps.executeQuery();
		int ptsRead = 0;
		int dataListSz = dataList.size();
		while (rs.next()) {
			//vehicle.getGpsData().setName(null);
			BLEInfo curr = BLEInfo.read(conn, rs);
			retval.add(curr);
			ptsRead++;
			if (ptsRead >= cnt)
				break;
		}
		rs.close();
		rs = null;
		ps.close();
		ps = null;
		for (int i=0,is=retval == null ? 0 : retval.size();i<is;i++) {
			BLEInfo curr = retval.get(i);
			if (curr.getGpsRecvTime() > 0 && curr.getGpsRecvTime() > this.internalLatestRecvTime) {
				this.internalLatestRecvTime = curr.getGpsRecvTime();
			}
		}

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
public boolean isBLEWorking(Connection conn, long refTS) {
	BLEInfo ref = this.get(conn, new BLEInfo(refTS));
	return ref != null && refTS-ref.getGpsRecordTime() < NewBLEMgmt.G_MAX_MILLISEC_PRIOR_FOR_BLETOBE_WORKING;
	
}
private static void addSortedShortBoolToList(ArrayList<MiscInner.PairShortBool> theList, MiscInner.PairShortBool item) {
	int addAt = 0;
	for (int is=theList == null ? 0 : theList.size(); addAt<is && theList.get(addAt).first < item.first; addAt++) {
		//do nothing
	}
	if (addAt == theList.size())
		theList.add(item);
	else
		theList.add(addAt, item);
}

public  MiscInner.Triple getRelBLEForDumperRelTSStart(Connection conn, int dumperId, long tsStart, long tsEnd, ArrayList<MiscInner.PairShortBool> detailedRead) {
	//first - rel to tsStart - the start, second, rel to tsEnd the first start
	if (tsEnd <= 0)
		tsEnd = Long.MAX_VALUE;
	BLEInfo ref = new BLEInfo(tsStart);
	int first = Misc.getUndefInt();
	int second = Misc.getUndefInt();
	int minRSSI = 127;
	//we will first go forward and find the first present ... and ending given MAX_01_BLE_GAP_TO_IGNORE_0
	//then we go back using first as reference
	long tsFirstP = 0;
	long tsLastP = 0;
	long tsLastNP = 0;
	
	for (int i=0;;i++) {
		BLEInfo curr = get(conn, ref, i);
		if (curr == null && i == 0)
			continue;
		if (curr == null || curr.getGpsRecordTime() > tsEnd)
			break;
		if (curr.getTruckVehicleId() != dumperId)
			continue;
		if (curr.getGpsRecordTime() < tsStart)
			continue;
		boolean isPresent = curr.isPresent();
		if (isPresent) {
			if (tsFirstP <= 0) {
				if (curr.getGpsRecordTime() > tsEnd)
					break;
				tsFirstP = curr.getGpsRecordTime();
				tsLastP = tsFirstP;
			}
			if (tsLastNP <= 0 || (tsLastP-tsLastNP) < NewBLEMgmt.MAX_01_BLE_GAP_TO_IGNORE_0) {
				tsLastP = curr.getGpsRecordTime();
				tsLastNP = 0;
				if (curr.getRssi() < minRSSI)
					minRSSI = curr.getRssi();
			}
			else {
				if (curr.getGpsRecordTime() > tsEnd)
					break;
			}
		}
		else {
			if (curr.getRssi() < minRSSI)
				minRSSI = curr.getRssi();
			if (tsLastNP <= 0)
				tsLastNP = curr.getGpsRecordTime(); 
		}
		if (detailedRead != null) {
			short secGap = ShovelSequence.getSecGap(tsStart, curr.getGpsRecordTime());
			NewBLEMgmt.addSortedShortBoolToList(detailedRead, new MiscInner.PairShortBool(secGap, isPresent));
		}
	}
	if (tsFirstP > 0 && tsLastNP <= 0)
		tsLastNP = tsEnd;
	//now 
	long tsBackLastP = tsFirstP;
	for (int i=0;;i--) {
		BLEInfo curr = get(conn, ref, i);
		
		if (curr == null)
			break;
		if (curr.getGpsRecordTime() < tsStart-NewBLEMgmt.MAX_PRE_BLE_TO_LOOK_FOR_MILLI)
			break;
		if (curr.getTruckVehicleId() != dumperId)
			continue;
		boolean isPresent = curr.isPresent();
		if (!isPresent) {
			if (tsBackLastP <= 0 || (tsBackLastP-curr.getGpsRecordTime()) >= MAX_01_BLE_GAP_TO_IGNORE_0)
				break;
			else {
				if (curr.getRssi() < minRSSI)
					minRSSI = curr.getRssi();		
			}
		}
		else {
			tsBackLastP = curr.getGpsRecordTime();
			if (curr.getRssi() < minRSSI)
				minRSSI = curr.getRssi();
			if (detailedRead != null) {
				short secGap = ShovelSequence.getSecGap(tsStart, curr.getGpsRecordTime());
				NewBLEMgmt.addSortedShortBoolToList(detailedRead, new MiscInner.PairShortBool(secGap, isPresent));
			}
		}
		

	}
	if (tsBackLastP > 0) {
		first = (int)((tsBackLastP-tsStart)/1000);
		second = (int)(((tsLastNP <= 0 ? tsEnd : tsLastNP)-tsStart)/1000);
	}
	
	return new MiscInner.Triple(first,second, minRSSI);	
}
public BLEInfo getImmIsPressent(Connection conn, long ts) {
	BLEInfo ref = new BLEInfo(ts);
	for (int i=0;;i--) {
		BLEInfo val = this.get(conn, ref, i);
		if (val == null)
			break;
		if (val != null && val.isPresent())
			return val;
	}
	return null;
}
public static void main(String[] args) {
	try {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		NewBLEMgmt chl = new  NewBLEMgmt(26130, -1,-1);
		chl.init(conn);
//		long t = chl.checkForUpdates(conn);

		BLEInfo dummy = null;
		BLEInfo retval = null;
		//2017-06-23 00:24:00
		dummy = new BLEInfo((new java.util.Date(117,5,23,0,24,1)).getTime());
		retval = chl.get(conn, dummy);

		retval = chl.get(conn, dummy,1);

//		t = chl.checkForUpdates(conn);
		int dbg=1;
		dbg++;

	}
	catch (Exception e) {
		e.printStackTrace();
		//eat it
	}
}


}

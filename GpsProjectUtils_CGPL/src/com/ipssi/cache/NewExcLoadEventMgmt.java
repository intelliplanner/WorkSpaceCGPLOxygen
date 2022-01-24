package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.common.ds.trip.CurrDumperInfo;
import com.ipssi.common.ds.trip.CurrShovelDumperMgmt;
import com.ipssi.common.ds.trip.CurrShovelInfo;
import com.ipssi.common.ds.trip.ShovelSequence;
import com.ipssi.common.ds.trip.ShovelSequenceHolder;
import com.ipssi.common.ds.trip.ThreadContextCache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;

public class NewExcLoadEventMgmt {
	public final static String GET_LOAD_EVENT_BOUND = ExcLoadEvent.GET_LOAD_EVENT_SEL  +
	" from exc_load_event  where vehicle_id=?  and gps_record_time > ? and gps_record_time < ?  order by gps_record_time ";
public final static String GET_LOAD_EVENT_BOUND_LOWER =  ExcLoadEvent.GET_LOAD_EVENT_SEL +
	" from exc_load_event  where vehicle_id=? and gps_record_time > ? and gps_record_time < ?    order by gps_record_time desc ";	
public final static String GET_LOAD_EVENT_BY_COUNT_HIGHER =  ExcLoadEvent.GET_LOAD_EVENT_SEL +
	" from exc_load_event  where vehicle_id=? and gps_record_time > ?     order by gps_record_time  limit ? ";
public final static String GET_LOAD_EVENT_BY_COUNT_LOWER = ExcLoadEvent.GET_LOAD_EVENT_SEL +
" from exc_load_event  where vehicle_id=? and gps_record_time < ?    order by gps_record_time desc limit ? ";


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
private FastList<ExcLoadEvent> dataList = new FastList<ExcLoadEvent>();

private ExcLoadEvent latestReceivedData = null;
private ExcLoadEvent prevLatestReceivedData = null;

private long internalLatestRecvTime = Misc.getUndefInt();
private long internalLatestRecordTime = Misc.getUndefInt();
private long internalLastMinMaxCheckAt = -1;

private static ConcurrentHashMap<Integer, NewExcLoadEventMgmt> vehicleCache = new ConcurrentHashMap<Integer, NewExcLoadEventMgmt>();


public static NewExcLoadEventMgmt getLoadEventList(Connection conn, int vehicleId) {
	return getLoadEventList(conn, vehicleId, false);//we are allowing to be created if not existing
}
public static NewExcLoadEventMgmt getLoadEventList(Connection conn, int vehicleId, boolean dontCreate) {
	NewExcLoadEventMgmt retval = vehicleCache.get(vehicleId);
	if (retval == null && !dontCreate) {
		retval = new NewExcLoadEventMgmt(vehicleId, -1, -1);
		retval.init(conn);
		vehicleCache.put(vehicleId, retval);
	}
	return retval;
}
public String toString() {
	return "["+vehicleId+","+minTime+","+maxTime+","+dataList.toString();
}

public NewExcLoadEventMgmt(int vehicleId, long mi, long mx) {
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
						ExcLoadEvent prevToAdd = dataList.get(prevAddAt-1);
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

public void remove(ExcLoadEvent data) {
	Pair<Integer, Boolean> idx = dataList.indexOf(data);
	if (idx.second)
		dataList.remove(idx.first);
}

public void recordTimes(ExcLoadEvent data, boolean doIfApprop) {
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


public void add(Connection conn, ExcLoadEvent data) {
	internalLatestRecvTime = data.getGpsRecvTime();
	internalLatestRecordTime = data.getGpsRecvTime();

	Pair<Integer, Boolean> idx = indexOf(conn, data);
	int pos = idx.first;
	
	boolean isSame = idx.second;
	boolean done = false;
	ExcLoadEvent currPtBeingReplaced = null;;
	
	if (isSame) {
		prevAddAt = pos;
		done = true;
	}
	boolean added = false;
	if (!done) {			
		added = true;
		dataList.addAtIndex(pos+1, data);
		byte min1 = (byte)-1;
		long dataTS = data.getGpsRecordTime();
		for (int i=pos+1;i>=0;i--) {
			ExcLoadEvent pt = dataList.get(i);
			if (pt == null)
				continue;
			if ((dataTS - pt.getGpsRecordTime()) > ExcLoadEvent.MAX_CYCLE_GAP_MS)
				break;
			pt.setIgnoreBecauseNeighbour(min1);
		}
		for (int i=pos+2,is=dataList.size();i<is;i++) {
			ExcLoadEvent pt = dataList.get(i);
			if (pt == null)
				continue;
			if ((pt.getGpsRecordTime()-dataTS) > ExcLoadEvent.MAX_CYCLE_GAP_MS)
				break;
			pt.setIgnoreBecauseNeighbour(min1);
		}
		prevAddAt = pos+1;
	}
	recordTimes(data, false);
	clean();
	prevReadAt = prevAddAt;
	//validateAdd(data);
		
	return;
}



public Pair<Integer, Boolean> indexOf(Connection conn, ExcLoadEvent data) {
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
					ArrayList<ExcLoadEvent> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, false);
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
				ArrayList<ExcLoadEvent> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, true);
				//dataList.mergeAtRight(readData);
				int addCnt = specializedMerge(readData,false);
			}
			retval = dataList.indexOf(data);
		}
	}
	return retval;
}

private int specializedMerge(ArrayList<ExcLoadEvent> readData, boolean isReverse) {
	int itemAddedCount = 0;
	for (int i=isReverse? readData.size()-1 : 0, is = isReverse?0:readData.size()-1, incr = isReverse?-1:1;
		isReverse ? i>=is : i <= is; i+=incr) {
		ExcLoadEvent itemToAdd = readData.get(i);
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

public ExcLoadEvent get(Connection conn, ExcLoadEvent data) {
	return get(conn, data, false);
}

public ExcLoadEvent get(Connection conn, ExcLoadEvent data, boolean strictLess) {
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

public ExcLoadEvent get(Connection conn, ExcLoadEvent data, int relIndex) {
	Pair<Integer,Boolean> idx = indexOf(conn, data);
	int idxInt =  idx.first;
	return get(conn, idxInt, relIndex, data);
}

public ExcLoadEvent get(Connection conn, int dataIndex, int relIndex, ExcLoadEvent data) { //this gets regardless of recv time constraint
	if (true) //g_ignoreUpdatedOn ||  this.currentTimingsAreOf == 0 || this.latestReceivedData == null)
		return getWithoutRecvConstraint(conn, dataIndex, relIndex, data);
	return null;
}

public static final long IMMCHECK_WINDOW_MILLI = 60*1000;
public static final long THRESH_BACK_DATA_MS = 3*60*1000;
public static final long IMMCHECK_WINDOW_NOCYCLE = 5*60*1000;

public static boolean isLatestImmWorkingInWindow(Connection conn, int shovelId) {
	NewExcLoadEventMgmt loadEvents = NewExcLoadEventMgmt.getLoadEventList(conn, shovelId);
	return loadEvents != null && loadEvents.isLatestImmWorkingInWindow(conn);
}
public boolean isLatestImmWorkingInWindow(Connection conn)  {
	try {
		VehicleDataInfo shovelVDF = VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, false, false);
		if (shovelVDF == null)
			return false;
		synchronized (shovelVDF) {//NEWSYNC remove
			NewVehicleData shovelVDT = shovelVDF.getDataList(conn, vehicleId, 0, false);
			if (shovelVDT == null)
				return false;
			GpsData last = shovelVDT.getLast(conn);
			ExcLoadEvent lastEvent = this.getLast(conn);
			long tsEndGps = last == null ? 0 : last.getGps_Record_Time();
			long tsEndEvent = lastEvent == null ? 0 : lastEvent.getGpsRecordTime();
			long tsEndAct = tsEndGps > tsEndEvent ? tsEndGps : tsEndEvent;
			long currTS = ThreadContextCache.getCurrTS();
			if (currTS-tsEndAct > THRESH_BACK_DATA_MS) {//latest info not available ... presume working
				return true;
			}
			
			boolean eventSeemsProper = lastEvent != null && tsEndAct-tsEndEvent < ShovelSequence.MAX_GAP_BETWEEN_IDLING_PT_BEF_ASSUMING_ACTIVE;
			if (eventSeemsProper) {
				//event in last 10 min .. make sure that in last 1 minute we have two events
				boolean hasImmEvent=  (tsEndAct-tsEndEvent <= IMMCHECK_WINDOW_MILLI);
				if (hasImmEvent) {
					ExcLoadEvent lastTolast = this.get(conn, lastEvent, true);
					hasImmEvent = lastTolast != null && (tsEndAct - lastTolast.getGpsRecordTime()) <=IMMCHECK_WINDOW_MILLI;  
				}
				if (!hasImmEvent) {
					//check if there is a shovel standing in IMMCHECK_WINDOW - if there is then consider non-working
					ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(vehicleId);
					shovelInfo.lock();
					try {
						CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(vehicleId);
						if (currShovelInfo.getDumpers().size() > 0) {
							CurrDumperInfo currDumperInfo = currShovelInfo.getDumpers().get(0);
							long endTS = currDumperInfo.getSelectedShovelSeq().getEndTS();//looking at endData of dumper instead of latest time to handle backdata
							if (endTS - currShovelInfo.getDumpers().get(0).getStartTS() > IMMCHECK_WINDOW_MILLI)
								return false;
						}
					}
					finally {
						shovelInfo.unlock();
					}
				}
				return true;
			 } else {//window doesnt seem proper ... if we have a dumper standing for more than X minute then we will declare non-working
				ShovelSequenceHolder shovelInfo = ShovelSequenceHolder.getShovelInfo(vehicleId);
				shovelInfo.lock();
				try {
					CurrShovelInfo currShovelInfo = CurrShovelDumperMgmt.getCurrShovelInfo(vehicleId);
					CurrDumperInfo currDumperInfo = currShovelInfo.getDumpers().get(0);
					long endTS = currDumperInfo.getSelectedShovelSeq().getEndTS();//looking at endData of dumper instead of latest time to handle backdata
					if (endTS - currShovelInfo.getDumpers().get(0).getStartTS() > IMMCHECK_WINDOW_NOCYCLE)
						return false;
					}
					finally {
						shovelInfo.unlock();
					}
					return true;
			 	}
		}//end of sync
	} catch (Exception e) {
		e.printStackTrace();
		//eat it
	}
	return true;
}

public boolean isWorkingInWindow(Connection conn, long tsEnd) {
	ExcLoadEvent ref = new ExcLoadEvent(tsEnd);
	int loadEventPtCount = 0;
	for (int i=0;;i--) {
		ExcLoadEvent pt = this.get(conn, ref,i);
		if (pt == null || (tsEnd-pt.getGpsRecordTime()) > ShovelSequence.MAX_GAP_BETWEEN_IDLING_PT_BEF_ASSUMING_ACTIVE)
			break;
		if (pt.isProperLoad())
			loadEventPtCount++;
		if (loadEventPtCount > 1)
			break;
	}
	if (loadEventPtCount <= 1) {
		for (int i=1;;i++) {
			ExcLoadEvent pt = this.get(conn, ref,i);
			if (pt == null || (pt.getGpsRecordTime()-tsEnd) > ShovelSequence.MAX_GAP_BETWEEN_IDLING_PT_BEF_ASSUMING_ACTIVE)
				break;
			if (pt.isProperLoad())
				loadEventPtCount++;
			if (loadEventPtCount > 1)
				break;
		}
	}
	return loadEventPtCount > 1;
}
public double getAvgDurMilli(Connection conn, long tsStart, long tsEnd) {
	ExcLoadEvent refEvent = new ExcLoadEvent(tsStart);
	double durTot = 0;
	int max = 0;
	int cnt = 0;
	ExcLoadEvent prevEvent = null;
	for (int i=0;;i++) {
		ExcLoadEvent e = this.get(conn, refEvent, i);
		if (e == null && i == 0)
			continue;
		if (e == null || e.getGpsRecordTime() > tsEnd)
			break;
		if (e.isProperLoad()) {
			int dur = prevEvent == null ? 0 : (int)( (e.getGpsRecordTime()-prevEvent.getGpsRecordTime()));
			
			if (dur <= ExcLoadEvent.MAX_CYCLE_GAP_MS && dur >= ExcLoadEvent.MIN_CYCLE_GAP_MS) {
				durTot += dur;
				if (dur > max)
					max = dur;
				cnt++;
			}
			prevEvent = e;
		}
	}
	if (cnt > 1) {
		durTot -= max;
		cnt--;
	}
	return durTot/cnt;
}

public boolean isValidNear(Connection conn, long tsEnd, long tsStart) {
	ExcLoadEvent refEvent = new ExcLoadEvent(tsEnd);
	long tsThresh = Math.min(tsEnd-ShovelSequence.G_LOADEVENT_MUST_EXIST_IF_VALID_MILLI, tsStart);
	boolean seenLoadEventInWindow = false;
	for(int i=0;;i--) {
		ExcLoadEvent currEvent = this.get(conn, refEvent,i);
		if (currEvent == null || currEvent.getGpsRecordTime() < tsThresh)
			return false;
		else if (currEvent.isProperLoad())
			return true;
	}
}

public ExcLoadEvent getWithoutRecvConstraint(Connection conn, int dataIndex, int relIndex, ExcLoadEvent data) { //this gets regardless of recv time constraint
	int ask = dataIndex+relIndex;
	int sz = dataList.size();
	ExcLoadEvent retval = null;
	int deltaLookAheadCount = g_reg_deltaLookAheadCount;

	if (ask < 0){
		long refTime = sz == 0 ? data.getGpsRecordTime()+1000 : dataList.get(0).getGpsRecordTime();
		if (minTime > 0 && (sz == 0 || minTime < dataList.get(0).getGpsRecordTime()) && minTime <= data.getGpsRecordTime()) {
			ArrayList<ExcLoadEvent> readData = readHelperByCount(conn, refTime, -1*ask+deltaLookAheadCount, false);
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
			ArrayList<ExcLoadEvent> readData = readHelperByCount(conn, refTime, ask-sz-1+deltaLookAheadCount, true);
			//dataList.mergeAtRight(readData);
			int addCnt = specializedMerge(readData,false);
		}
	}
	
	int ti  = ask<0 || ask>=dataList.size() ? prevReadAt : ask;
	if (ti < prevReadAt)
		prevReadAt = ti;
	return ask < 0 || ask >= dataList.size() ? null : dataList.get(ask);
}

public ExcLoadEvent getEndData() {
	return this.dataList.get(dataList.size()-1);
}
public boolean isAtEnd(ExcLoadEvent data) {
	//kind of complex - if in recoveryMode or mode <= 0 or mode > 2 then simply data.getGps_Record_Time() >= maxTime
	//else if mode = 1 (for RP) - will return true only if all points after this are not RP processed
	//else if mode = 2 (for TP) - will return true only if all points adter this are not TP processed
	long drt = data.getGpsRecordTime();
	return drt >= this.maxTime;
}
public ExcLoadEvent getLast(Connection conn) { //Assume
	ExcLoadEvent last =  dataList.get(dataList.size()-1);
	if (last == null || last.getGpsRecordTime() >= maxTime)
		return last;
	ExcLoadEvent dummy = new ExcLoadEvent(maxTime);
	return get(conn, dummy);
}
public ExcLoadEvent simpleGet(int index) {
	return dataList.get(index);
}
public void simpleReplaceAt(int index, ExcLoadEvent data) {
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
		ps = conn.prepareStatement("select min(gps_record_time), max(gps_record_time) from exc_load_event where vehicle_id = ?");
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
	ArrayList<ExcLoadEvent> data = readHelperByTimeBound(conn, mi, mx, false);
	//dataList.mergeAtLeftReverse(data);
	//prevAddAt += data.size();
	//prevReadAt += data.size();
	int addCnt = specializedMerge(data,true);
	prevAddAt += addCnt;
	prevReadAt += addCnt;
}
private void readDataToRight(Connection conn, long mx, long ref) {
	long mi = dataList.size() == 0 ? ref-1000 : dataList.get(dataList.size()-1).getGpsRecordTime();
	ArrayList<ExcLoadEvent> data = readHelperByTimeBound(conn, mi, mx, true);
	//dataList.mergeAtRight(data);
	int addCnt = specializedMerge(data,false);
}


private ArrayList<ExcLoadEvent> readHelperByTimeBound(Connection conn, long mi, long mx, boolean getHigher) {
	PreparedStatement ps = null;
	ResultSet rs = null;
	ArrayList<ExcLoadEvent> retval = new ArrayList<ExcLoadEvent>(g_reg_maxPoints/3);
	try {
//		public final static String GET_LOAD_EVENT_BOUND = "select ch.vehicle_id,  ch.port_node_id, ch.gps_record_time, ch.LOAD_EVENT_rec_date, ch.chd_id, ch.consignee, ch.dest_code, ch.dest_addr_1, ch.dest_addr_2, ch.dest_addr_3, ch.dest_addr_4, ch.dest_city, ch.dest_state, ch.material_id "+
//		" from exc_load_event where vehicle_id=?  and gps_record_time > ? and gps_record_time < ?  order by gps_record_time ";
		
		ps = conn.prepareStatement(getHigher ? (GET_LOAD_EVENT_BOUND) : (GET_LOAD_EVENT_BOUND_LOWER));
		
		int colIndex = 1;
		ps.setInt(colIndex++, vehicleId);
		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mi));
		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mx));

		//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
		//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
		rs = ps.executeQuery();
		boolean prevIsInDataList = true;
		while (rs.next()) {
			ExcLoadEvent curr = ExcLoadEvent.read(conn, rs);
			retval.add(curr);
		}
		rs.close();
		rs = null;
		ps.close();
		ps = null;
		for (int i=0,is=retval == null ? 0 : retval.size();i<is;i++) {
			ExcLoadEvent curr = retval.get(i);
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

private ArrayList<ExcLoadEvent> readHelperByCount(Connection conn, long timeref, int cnt, boolean getHigher) {
	PreparedStatement ps = null;
	ResultSet rs = null;
	ArrayList<ExcLoadEvent> retval = new ArrayList<ExcLoadEvent>(g_reg_maxPoints/3);
	try {
		ps = conn.prepareStatement(getHigher? (GET_LOAD_EVENT_BY_COUNT_HIGHER) : (GET_LOAD_EVENT_BY_COUNT_LOWER));
		int colIndex = 1;
		ps.setInt(colIndex++, vehicleId);
		ps.setTimestamp(colIndex++, Misc.utilToSqlDate(timeref));
		ps.setInt(colIndex++, cnt);
		rs = ps.executeQuery();
		int ptsRead = 0;
		int dataListSz = dataList.size();
		while (rs.next()) {
			//vehicle.getGpsData().setName(null);
			ExcLoadEvent curr = ExcLoadEvent.read(conn, rs);
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
			ExcLoadEvent curr = retval.get(i);
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
public static void updateChangeTrack(MiscInner.PairLong changeTrack, long ts) {
	if (changeTrack == null)
		return;
	if (changeTrack.first <= 0 || changeTrack.first > ts)
		changeTrack.first = ts;
	if (changeTrack.second <= 0 || changeTrack.second < ts)
		changeTrack.second = ts;
}
public static int helperGetBLEEntryInCandidateList(ArrayList<BLEInfo> candidateList, BLEInfo item) {
	for (int i=candidateList.size()-1;i>=0;i--) {
		BLEInfo toCheck = candidateList.get(i);
		if (item.getTag().equals(toCheck.getTag()))
			return i;
	}
	return -1;
}


public static void main(String[] args) {
	try {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		NewExcLoadEventMgmt chl = new  NewExcLoadEventMgmt(26130, -1,-1);
		chl.init(conn);
//		long t = chl.checkForUpdates(conn);

		ExcLoadEvent dummy = null;
		ExcLoadEvent retval = null;
		//2017-06-23 00:24:00
		dummy = new ExcLoadEvent((new java.util.Date(117,5,23,0,24,1)).getTime());
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

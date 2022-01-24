package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class NewEventDismissMgmt {
	public final static String GET_EVENTDISMISS_SEL = "select vehcile_or_site_id,  type, at_time ";
	public final static String GET_EVENTDISMISS_BOUND = GET_EVENTDISMISS_SEL +
		" from mining_event_dismiss  where vehcile_or_site_id=?   and type = ? and at_time > ? and at_time < ? order by at_time ";
	public final static String GET_EVENTDISMISS_BOUND_LOWER =  GET_EVENTDISMISS_SEL +
		" from mining_event_dismiss  where vehcile_or_site_id=?  and type = ? and at_time > ?  order by at_time desc ";	
	public final static String GET_EVENTDISMISS_BY_COUNT_HIGHER =  GET_EVENTDISMISS_SEL +
		" from mining_event_dismiss  where vehcile_or_site_id=?  and type = ? and at_time > ?     order by at_time limit ? ";
	public final static String GET_EVENTDISMISS_BY_COUNT_LOWER = GET_EVENTDISMISS_SEL +
	" from mining_event_dismiss  where vehcile_or_site_id=?  and type = ? and at_time < ? order by at_time limit ? ";
	
	public static long g_infinite_future = System.currentTimeMillis()+2*365*24*3600*1000L;  
	public static long g_infinite_past = System.currentTimeMillis()-2*365*24*3600*1000L;
	public static int g_reg_maxPoints = 12;//make it divisble by 3 and 4 to be easier
	public static int g_reg_maxPointsForForceClean =(int)(g_reg_maxPoints*10);//14400;//if the number of points after add becomes this much ... then clean up ... either
	public static int g_reg_deltaLookAheadSec = 3600*24*5;//g_maxPoints < 500 ? g_maxPoints/30*60*10 : ;//5*50;//10*60;
	public static int g_reg_deltaLookAheadCount = 2;//g_maxPoints/15*10;//3;//20;
	
	private int vehicleId = Misc.getUndefInt();
	private int type = 0;
	private long minTime = -1;
	private long maxTime = -1;
	private int prevAddAt = -1;
	private int prevReadAt = -1;
	private FastList<EventDismiss> dataList = new FastList<EventDismiss>();
	
	private long internalLatestRecvTime = Misc.getUndefInt();
	private long internalLatestRecordTime = Misc.getUndefInt();
	private long internalLastMinMaxCheckAt = -1;
	
//	
	public static NewEventDismissMgmt create(Connection conn, int type, int vehicleId) {
		NewEventDismissMgmt retval = null;
		retval = new NewEventDismissMgmt(vehicleId,type, -1, -1);
		retval.init(conn);
		return retval;
	}
	public String toString() {
		return "["+vehicleId+","+minTime+","+maxTime+","+dataList.toString();
	}
	
	
	public NewEventDismissMgmt(int vehicleId, int type, long mi, long mx) {
		this.vehicleId = vehicleId;
		this.type = type;
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
	
	public void remove(EventDismiss data) {
		Pair<Integer, Boolean> idx = dataList.indexOf(data);
		if (idx.second)
			dataList.remove(idx.first);
	}
	
	public void recordTimes(EventDismiss data, boolean doIfApprop) {
		long ts = data.getAtTime();
		if (minTime <= 0 || minTime > ts)
			minTime = ts;
		if (maxTime <= 0 || maxTime < ts)
			maxTime = ts;
	}
	
	public static void add(NewMU newmu, Connection conn, int vehicleId, int siteId, long ts) throws SQLException {
		NewEventDismissMgmt mgmt = null;
		CoreVehicleInfo vehicleInfo = newmu.getVehicleInfo(vehicleId);
		int type = 0;
		int id = vehicleId;
		if (vehicleInfo != null) {
			mgmt = vehicleInfo.getEventDismissMgmt(conn);
		}
		else {
			Site site = newmu.getSiteInfo(siteId);
			if (site != null) {
				mgmt = site.getEventDismissMgmt(conn);
				type = 1;
				id = siteId;
			}
		}
		if (mgmt != null) {
			PreparedStatement ps = conn.prepareStatement("insert ignore into mining_event_dismiss(vehicle_or_site_id, type, at_time) values (?,?,?)");
			ps.setInt(1, type == 0 ? vehicleId : siteId);
			ps.setInt(2, type);
			ps.setTimestamp(3, Misc.longToSqlDate(ts));
			ps.execute();
			ps = Misc.closePS(ps);
			EventDismiss item = new EventDismiss(type == 0 ? vehicleId : siteId, type, ts);
			mgmt.add(conn, item);
		}
	}
	
	public void add(Connection conn, EventDismiss data) {
		internalLatestRecordTime = data.getAtTime();
	
		Pair<Integer, Boolean> idx = indexOf(conn, data);
		int pos = idx.first;
		
		boolean isSame = idx.second;
		boolean done = false;
		EventDismiss currPtBeingReplaced = null;;
		
		if (isSame) {
			prevAddAt = pos;
			dataList.addAtIndex(pos, data);
			done = true;
		}
		
		if (!done) {			
			dataList.addAtIndex(pos+1, data);
			prevAddAt = pos+1;
		}
		recordTimes(data, false);
		clean();
		prevReadAt = prevAddAt;
		//validateAdd(data);
		return;
	}
	
	
	public Pair<Integer, Boolean> indexOf(Connection conn, EventDismiss data) {
		Pair<Integer, Boolean> retval = null;
		retval = dataList.indexOf(data);
		int deltaLookAheadCount = g_reg_deltaLookAheadCount;

		if (!retval.second) {
			int idxInt = retval.first;
			if (idxInt < 0 && minTime <= data.getAtTime() && minTime > 0) {
				long currMi = estimateMinDate(data.getAtTime());
				readDataToLeft(conn, currMi,  data.getAtTime());
				retval = dataList.indexOf(data);
				idxInt = retval.first;
				if (idxInt < 0 && minTime <= data.getAtTime() && minTime > 0) { //read at least 1 pt before so that we can return the prior pt .. dont call get() ... may lead to infinite recursion
					long refTime = dataList.size() == 0 ? data.getAtTime()+1000 : dataList.get(0).getAtTime();
					if (minTime > 0 && (dataList.size() == 0 || minTime < dataList.get(0).getAtTime()) && minTime <= data.getAtTime()) {
						ArrayList<EventDismiss> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, false);
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
			else if (idxInt >= dataList.size()-1 && maxTime >= data.getAtTime() && minTime > 0) {//NOT VERIFIED
				long currMx = estimateMaxDate(data.getAtTime());
				readDataToRight(conn, currMx, data.getAtTime());
				retval = dataList.indexOf(data);
				idxInt = retval.first;
				int sz = dataList.size();
				if (!retval.second && idxInt >= sz-1  && (sz == 0 || maxTime > dataList.get(sz-1).getAtTime()) && maxTime >= data.getAtTime() && minTime > 0) {
					long refTime = sz == 0 ? data.getAtTime()-1000 : dataList.get(sz-1).getAtTime();
					ArrayList<EventDismiss> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, true);
					//dataList.mergeAtRight(readData);
					int addCnt = specializedMerge(readData,false);
				}
				retval = dataList.indexOf(data);
			}
		}
		return retval;
	}
	
	private int specializedMerge(ArrayList<EventDismiss> readData, boolean isReverse) {
		int itemAddedCount = 0;
		for (int i=isReverse? readData.size()-1 : 0, is = isReverse?0:readData.size()-1, incr = isReverse?-1:1;
			isReverse ? i>=is : i <= is; i+=incr) {
			EventDismiss itemToAdd = readData.get(i);
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
	
	public EventDismiss get(Connection conn, EventDismiss data) {
		return get(conn, data, false);
	}
	
	public EventDismiss get(Connection conn, EventDismiss data, boolean strictLess) {
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
	
	public EventDismiss get(Connection conn, EventDismiss data, int relIndex) {
		Pair<Integer,Boolean> idx = indexOf(conn, data);
		int idxInt =  idx.first;
		return get(conn, idxInt, relIndex, data);
	}
	
	public EventDismiss get(Connection conn, int dataIndex, int relIndex, EventDismiss data) { //this gets regardless of recv time constraint
		if (true) {//g_ignoreUpdatedOn ||  this.currentTimingsAreOf == 0 || this.latestReceivedData == null)
			 getWithoutRecvConstraint(conn, dataIndex, relIndex, data);
		}
		return null;
	}
	
	public EventDismiss getWithoutRecvConstraint(Connection conn, int dataIndex, int relIndex, EventDismiss data) { //this gets regardless of recv time constraint
		int ask = dataIndex+relIndex;
		int sz = dataList.size();
		EventDismiss retval = null;
		int deltaLookAheadCount = g_reg_deltaLookAheadCount;

		if (ask < 0){
			long refTime = sz == 0 ? data.getAtTime()+1000 : dataList.get(0).getAtTime();
			if (minTime > 0 && (sz == 0 || minTime < dataList.get(0).getAtTime()) && minTime <= data.getAtTime()) {
				ArrayList<EventDismiss> readData = readHelperByCount(conn, refTime, -1*ask+deltaLookAheadCount, false);
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
			long refTime = sz == 0 ? data.getAtTime()-1000 : dataList.get(sz-1).getAtTime();
			if (maxTime > 0 && (sz == 0 || maxTime > dataList.get(sz-1).getAtTime()) && maxTime >= data.getAtTime()) {
				ArrayList<EventDismiss> readData = readHelperByCount(conn, refTime, ask-sz-1+deltaLookAheadCount, true);
				//dataList.mergeAtRight(readData);
				int addCnt = specializedMerge(readData,false);
			}
		}
		
		int ti  = ask<0 || ask>=dataList.size() ? prevReadAt : ask;
		if (ti < prevReadAt)
			prevReadAt = ti;
		return ask < 0 || ask >= dataList.size() ? null : dataList.get(ask);
	}
	
	public EventDismiss getEndData() {
		return this.dataList.get(dataList.size()-1);
	}
	public boolean isAtEnd(EventDismiss data) {
		//kind of complex - if in recoveryMode or mode <= 0 or mode > 2 then simply data.getGps_Record_Time() >= maxTime
		//else if mode = 1 (for RP) - will return true only if all points after this are not RP processed
		//else if mode = 2 (for TP) - will return true only if all points adter this are not TP processed
		long drt = data.getAtTime();
		return drt >= this.maxTime;
	}
	public EventDismiss getLast(Connection conn) { //Assume
		EventDismiss last =  dataList.get(dataList.size()-1);
		if (last == null || last.getAtTime() >= maxTime)
			return last;
		EventDismiss dummy = new EventDismiss(maxTime);
		return get(conn, dummy);
	}
	public EventDismiss simpleGet(int index) {
		return dataList.get(index);
	}
	public void simpleReplaceAt(int index, EventDismiss data) {
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
			ps = conn.prepareStatement("select min(challan_date), max(challan_date) from challan_details where vehicle_id = ?");
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
		long mx = dataList.size() == 0 ? ref+1000 : dataList.get(0).getAtTime();
		ArrayList<EventDismiss> data = readHelperByTimeBound(conn, mi, mx, false);
		//dataList.mergeAtLeftReverse(data);
		//prevAddAt += data.size();
		//prevReadAt += data.size();
		int addCnt = specializedMerge(data,true);
		prevAddAt += addCnt;
		prevReadAt += addCnt;
	}
	private void readDataToRight(Connection conn, long mx, long ref) {
		long mi = dataList.size() == 0 ? ref-1000 : dataList.get(dataList.size()-1).getAtTime();
		ArrayList<EventDismiss> data = readHelperByTimeBound(conn, mi, mx, true);
		//dataList.mergeAtRight(data);
		int addCnt = specializedMerge(data,false);
	}
	

	private ArrayList<EventDismiss> readHelperByTimeBound(Connection conn, long mi, long mx, boolean getHigher) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<EventDismiss> retval = new ArrayList<EventDismiss>(g_reg_maxPoints/3);
		try {
//			public final static String GET_ASSIGNMENT_BOUND = "select ch.vehicle_id,  ch.port_node_id, ch.challan_date, ch.challan_rec_date, ch.chd_id, ch.consignee, ch.dest_code, ch.dest_addr_1, ch.dest_addr_2, ch.dest_addr_3, ch.dest_addr_4, ch.dest_city, ch.dest_state, ch.material_id "+
//			" from challan_details where vehicle_id=?  and challan_date > ? and challan_date < ?  order by challan_date ";
			
			ps = conn.prepareStatement(getHigher ? (GET_EVENTDISMISS_BOUND) : (GET_EVENTDISMISS_BOUND_LOWER));
			
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, this.type);
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mi));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mx));
	
			//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
			//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
			rs = ps.executeQuery();
			boolean prevIsInDataList = true;
			while (rs.next()) {
				EventDismiss curr = EventDismiss.read(conn, rs);
				retval.add(curr);
			}
			rs.close();
			rs = null;
			ps.close();
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
		return retval;
	}
	
	private ArrayList<EventDismiss> readHelperByCount(Connection conn, long timeref, int cnt, boolean getHigher) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<EventDismiss> retval = new ArrayList<EventDismiss>(g_reg_maxPoints/3);
		try {
			ps = conn.prepareStatement(getHigher? (GET_EVENTDISMISS_BY_COUNT_HIGHER) : (GET_EVENTDISMISS_BY_COUNT_LOWER));
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, this.type);
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(timeref));
			ps.setInt(colIndex++, cnt);
			rs = ps.executeQuery();
			int ptsRead = 0;
			int dataListSz = dataList.size();
			while (rs.next()) {
				//vehicle.getGpsData().setName(null);
				EventDismiss curr = EventDismiss.read(conn, rs);
				retval.add(curr);
				ptsRead++;
				if (ptsRead >= cnt)
					break;
			}
			rs.close();
			rs = null;
			ps.close();
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
		return retval;
	}
	
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		NewMU mu = NewMU.getManagementUnit(conn, 816);
		NewEventDismissMgmt.add(mu, conn, Misc.getUndefInt(), 1, System.currentTimeMillis());
		int dbg = 1;
		Site site = mu.getSiteInfo(1);
		
		dbg++;
		NewEventDismissMgmt.add(mu, conn, 27650, Misc.getUndefInt(), System.currentTimeMillis());
		CoreVehicleInfo vehicleInfo = mu.getVehicleInfo(27650);
		dbg++;
		
		}
		catch (Exception e) {
			
		}
	}
}

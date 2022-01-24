package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.GpsData;

public class NewChallanMgmt {
	
	public final static String GET_CHALLAN_DATA_BOUND = ChallanInfo.GET_CHALLAN_DATA_SEL +
		" from challan_details ch where vehicle_id=?  and challan_date > ? and challan_date < ? and ch.trip_status in (1,2) order by challan_date,ch.trip_status, ch.updated_on,ch.id ";
	public final static String GET_CHALLAN_DATA_BOUND_LOWER =  ChallanInfo.GET_CHALLAN_DATA_SEL +
		" from challan_details ch where vehicle_id=? and challan_date > ? and challan_date < ?   and ch.trip_status in (1,2)  order by challan_date desc, ch.trip_status desc, ch.updated_on desc, ch.id desc ";	
	public final static String GET_CHALLAN_DATA_BY_COUNT_HIGHER =  ChallanInfo.GET_CHALLAN_DATA_SEL +
		" from challan_details ch where vehicle_id=? and challan_date > ?   and ch.trip_status in (1,2)   order by challan_date, ch.trip_status, ch.updated_on, ch.id limit ? ";
	public final static String GET_CHALLAN_DATA_BY_COUNT_LOWER = ChallanInfo.GET_CHALLAN_DATA_SEL +
	" from challan_details ch where vehicle_id=? and challan_date < ?   and ch.trip_status in (1,2)  order by challan_date desc, ch.trip_status desc, ch.updated_on desc, ch.id desc limit ? ";
	
	//public final static String GET_TPR_DATA_BOUND = ChallanInfo.GET_TPR_DATA_SEL +
	//" from challan_details ch where vehicle_id=?  and challan_date > ? and challan_date < ? and ch.trip_status in (1,2) order by challan_date,ch.trip_status, ch.updated_on,ch.id ";
	//public final static String GET_TPR_DATA_BOUND_LOWER =  ChallanInfo.GET_TPR_DATA_SEL +
	//" from challan_details ch where vehicle_id=? and challan_date > ? and challan_date < ?   and ch.trip_status in (1,2)  order by challan_date desc, ch.trip_status desc, ch.updated_on desc, ch.id desc ";	
	//public final static String GET_TPR_DATA_BY_COUNT_HIGHER =  ChallanInfo.GET_TPR_DATA_SEL +
	//" from challan_details ch where vehicle_id=? and challan_date > ?   and ch.trip_status in (1,2)   order by challan_date, ch.trip_status, ch.updated_on, ch.id limit ? ";
	//public final static String GET_TPR_DATA_BY_COUNT_LOWER = ChallanInfo.GET_TPR_DATA_SEL +
//" from challan_details ch where vehicle_id=? and challan_date < ?   and ch.trip_status in (1,2)  order by challan_date desc, ch.trip_status desc, ch.updated_on desc, ch.id desc limit ? ";

	public static long g_infinite_future = System.currentTimeMillis()+2*365*24*3600*1000L;  
	public static long g_infinite_past = System.currentTimeMillis()-2*365*24*3600*1000L;
	public static int g_reg_maxPoints = 12;//make it divisble by 3 and 4 to be easier
	public static int g_reg_maxPointsForForceClean =(int)(g_reg_maxPoints*10);//14400;//if the number of points after add becomes this much ... then clean up ... either
	public static int g_reg_deltaLookAheadSec = 3600*24*5;//g_maxPoints < 500 ? g_maxPoints/30*60*10 : ;//5*50;//10*60;
	public static int g_reg_deltaLookAheadCount = 2;//g_maxPoints/15*10;//3;//20;
	
	private int vehicleId = Misc.getUndefInt();
	private long minTime = -1;
	private long maxTime = -1;
	private int prevAddAt = -1;
	private int prevReadAt = -1;
	private FastList<ChallanInfo> dataList = new FastList<ChallanInfo>();
	
	private ChallanInfo latestReceivedData = null;
	private ChallanInfo prevLatestReceivedData = null;
	
	private long internalLatestRecvTime = Misc.getUndefInt();
	private long internalLatestRecordTime = Misc.getUndefInt();
	private long internalLastMinMaxCheckAt = -1;
	
	private static ConcurrentHashMap<Integer, NewChallanMgmt> vehicleCache = new ConcurrentHashMap<Integer, NewChallanMgmt>();
	public static NewChallanMgmt getExtChallanList(Connection conn, int vehicleId) {
		return getExtChallanList(conn, vehicleId, false);
	}
	public static NewChallanMgmt getExtChallanList(Connection conn, int vehicleId, boolean dontCreate) {
		NewChallanMgmt retval = vehicleCache.get(vehicleId);
		if (retval == null && !dontCreate) {
			retval = new NewChallanMgmt(vehicleId, -1, -1);
			retval.init(conn);
			vehicleCache.put(vehicleId, retval);
		}
		return retval;
	}
	public String toString() {
		return "["+vehicleId+","+minTime+","+maxTime+","+dataList.toString();
	}
	
	
	public NewChallanMgmt(int vehicleId, long mi, long mx) {
		this.vehicleId = vehicleId;
		this.minTime = mi;
		this.maxTime = mx;
	}
	public void resetIdInfoEtc() {
		for (int i=0,is = dataList.size();i<is;i++) {
			ChallanInfo c = dataList.get(i);
			if (c != null) {
				c.setIdInfo(null);
				c.setSrcIdInfo(null);
			}
		}
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
					long gp = this.latestReceivedData.getChallanRecvTime() - this.prevLatestReceivedData.getChallanRecvTime();
					if (gp > 0 && prevAddAt >= sz-desiredLHSCountBeforeAdd) {
						//either gap < 10 min or these two points must be consecutive
						if (gp >= 10*60*1000)
							doCleanUp = true;
						else {
							ChallanInfo prevToAdd = dataList.get(prevAddAt-1);
							if (prevToAdd != null && prevLatestReceivedData.getChallanDate() == prevToAdd.getChallanDate()) {
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
	
	public void remove(ChallanInfo data) {
		Pair<Integer, Boolean> idx = dataList.indexOf(data);
		if (idx.second)
			dataList.remove(idx.first);
	}
	
	public void recordTimes(ChallanInfo data, boolean doIfApprop) {
		long ts = data.getChallanDate();
		if (minTime <= 0 || minTime > ts)
			minTime = ts;
		if (maxTime <= 0 || maxTime < ts)
			maxTime = ts;
		if (!doIfApprop || latestReceivedData == null || latestReceivedData.getChallanRecvTime() < data.getChallanRecvTime()) {
			prevLatestReceivedData = latestReceivedData;
			latestReceivedData = data;
		}
	}
	
	
	public void add(Connection conn, ChallanInfo data) {
		internalLatestRecvTime = data.getChallanRecvTime();
		internalLatestRecordTime = data.getChallanDate();
	
		Pair<Integer, Boolean> idx = indexOf(conn, data);
		int pos = idx.first;
		
		boolean isSame = idx.second;
		boolean done = false;
		ChallanInfo currPtBeingReplaced = null;;
		
		if (isSame) {
			prevAddAt = pos;
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
	
	
	public long checkForUpdates(Connection conn, boolean mustCheck) {//returns the time from which new ChallanInfoMight Have Appeared
		long retval = -1;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			long curr = System.currentTimeMillis();
			java.util.Date dbgDate = Misc.longToUtilDate(internalLastMinMaxCheckAt);
			java.util.Date dbgDate2 = Misc.longToUtilDate(this.internalLatestRecvTime);
			java.util.Date dbgDate3 = Misc.longToUtilDate(this.internalLatestRecordTime);
			
			boolean check = mustCheck || this.internalLastMinMaxCheckAt <= 0 || (curr - internalLastMinMaxCheckAt) > 5*3600*1000L;
			if (check) {
				long checkFrom = internalLastMinMaxCheckAt <= 0 ? Misc.G_REF_DATE_FOR_WEEK.getTime() : internalLastMinMaxCheckAt;
				if (mustCheck)
					checkFrom -= 3*60*60*1000L; 
				ps = conn.prepareStatement("select min(challan_date), max(challan_date) from challan_details where vehicle_id = ? and updated_on > ?");
				ps.setInt(1, vehicleId);
				ps.setTimestamp(2, Misc.utilToSqlDate(checkFrom));
				if (!mustCheck)
					this.internalLastMinMaxCheckAt = curr-1000L;

				rs = ps.executeQuery();
				if (rs.next()) {
					long mi = Misc.sqlToLong(rs.getTimestamp(1));
					long mx = Misc.sqlToLong(rs.getTimestamp(2));
					if (mi > 0 && (mi <= this.maxTime || maxTime <= 0)) { //remove entries including and before mi ... so that when we read we have complete list
						ChallanInfo dummy = new ChallanInfo(mi);
						Pair<Integer, Boolean> idx = this.dataList.indexOf(dummy);
						int removeIncl = idx.second ? idx.first+1 : idx.first;
						if (removeIncl < 0)
							removeIncl = 0;
						for (int i=dataList.size()-1;i>=removeIncl;i--)
							dataList.remove(i);
						if (mi < this.minTime || this.minTime <= 0)
							 this.minTime = mi;
						
					}
					retval = mi;
					if (mx > 0) {
						if (maxTime <= 0 || mx > maxTime)
							this.maxTime = mx;
					}
				}
				rs.close();
				rs = null;
				ps.close();
				ps = null;
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
			catch (Exception e1) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e1) {
				
			}
		}
		return retval;
	}
	public Pair<Integer, Boolean> indexOf(Connection conn, ChallanInfo data) {
		Pair<Integer, Boolean> retval = null;
		retval = dataList.indexOf(data);
		int deltaLookAheadCount = g_reg_deltaLookAheadCount;

		if (!retval.second) {
			int idxInt = retval.first;
			if (idxInt < 0 && minTime <= data.getChallanDate() && minTime > 0) {
				long currMi = estimateMinDate(data.getChallanDate());
				readDataToLeft(conn, currMi,  data.getChallanDate());
				retval = dataList.indexOf(data);
				idxInt = retval.first;
				if (idxInt < 0 && minTime <= data.getChallanDate() && minTime > 0) { //read at least 1 pt before so that we can return the prior pt .. dont call get() ... may lead to infinite recursion
					long refTime = dataList.size() == 0 ? data.getChallanDate()+1000 : dataList.get(0).getChallanDate();
					if (minTime > 0 && (dataList.size() == 0 || minTime < dataList.get(0).getChallanDate()) && minTime <= data.getChallanDate()) {
						ArrayList<ChallanInfo> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, false);
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
			else if (idxInt >= dataList.size()-1 && maxTime >= data.getChallanDate() && minTime > 0) {//NOT VERIFIED
				long currMx = estimateMaxDate(data.getChallanDate());
				readDataToRight(conn, currMx, data.getChallanDate());
				retval = dataList.indexOf(data);
				idxInt = retval.first;
				int sz = dataList.size();
				if (!retval.second && idxInt >= sz-1  && (sz == 0 || maxTime > dataList.get(sz-1).getChallanDate()) && maxTime >= data.getChallanDate() && minTime > 0) {
					long refTime = sz == 0 ? data.getChallanDate()-1000 : dataList.get(sz-1).getChallanDate();
					ArrayList<ChallanInfo> readData = readHelperByCount(conn, refTime, 1+deltaLookAheadCount, true);
					//dataList.mergeAtRight(readData);
					int addCnt = specializedMerge(readData,false);
				}
				retval = dataList.indexOf(data);
			}
		}
		return retval;
	}
	
	private int specializedMerge(ArrayList<ChallanInfo> readData, boolean isReverse) {
		int itemAddedCount = 0;
		for (int i=isReverse? readData.size()-1 : 0, is = isReverse?0:readData.size()-1, incr = isReverse?-1:1;
			isReverse ? i>=is : i <= is; i+=incr) {
			ChallanInfo itemToAdd = readData.get(i);
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
	
	public ChallanInfo get(Connection conn, ChallanInfo data) {
		return get(conn, data, false);
	}
	
	public ChallanInfo get(Connection conn, ChallanInfo data, boolean strictLess) {
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
	
	public ChallanInfo get(Connection conn, ChallanInfo data, int relIndex) {
		Pair<Integer,Boolean> idx = indexOf(conn, data);
		int idxInt =  idx.first;
		return get(conn, idxInt, relIndex, data);
	}
	
	public ChallanInfo get(Connection conn, int dataIndex, int relIndex, ChallanInfo data) { //this gets regardless of recv time constraint
		if (true) //g_ignoreUpdatedOn ||  this.currentTimingsAreOf == 0 || this.latestReceivedData == null)
			return getWithoutRecvConstraint(conn, dataIndex, relIndex, data);
		return null;
	}
	
	public ChallanInfo getWithoutRecvConstraint(Connection conn, int dataIndex, int relIndex, ChallanInfo data) { //this gets regardless of recv time constraint
		int ask = dataIndex+relIndex;
		int sz = dataList.size();
		ChallanInfo retval = null;
		int deltaLookAheadCount = g_reg_deltaLookAheadCount;

		if (ask < 0){
			long refTime = sz == 0 ? data.getChallanDate()+1000 : dataList.get(0).getChallanDate();
			if (minTime > 0 && (sz == 0 || minTime < dataList.get(0).getChallanDate()) && minTime <= data.getChallanDate()) {
				ArrayList<ChallanInfo> readData = readHelperByCount(conn, refTime, -1*ask+deltaLookAheadCount, false);
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
			long refTime = sz == 0 ? data.getChallanDate()-1000 : dataList.get(sz-1).getChallanDate();
			if (maxTime > 0 && (sz == 0 || maxTime > dataList.get(sz-1).getChallanDate()) && maxTime >= data.getChallanDate()) {
				ArrayList<ChallanInfo> readData = readHelperByCount(conn, refTime, ask-sz-1+deltaLookAheadCount, true);
				//dataList.mergeAtRight(readData);
				int addCnt = specializedMerge(readData,false);
			}
		}
		
		int ti  = ask<0 || ask>=dataList.size() ? prevReadAt : ask;
		if (ti < prevReadAt)
			prevReadAt = ti;
		return ask < 0 || ask >= dataList.size() ? null : dataList.get(ask);
	}
	
	public ChallanInfo getEndData() {
		return this.dataList.get(dataList.size()-1);
	}
	public boolean isAtEnd(ChallanInfo data) {
		//kind of complex - if in recoveryMode or mode <= 0 or mode > 2 then simply data.getGps_Record_Time() >= maxTime
		//else if mode = 1 (for RP) - will return true only if all points after this are not RP processed
		//else if mode = 2 (for TP) - will return true only if all points adter this are not TP processed
		long drt = data.getChallanDate();
		return drt >= this.maxTime;
	}
	public ChallanInfo getLast(Connection conn) { //Assume
		ChallanInfo last =  dataList.get(dataList.size()-1);
		if (last == null || last.getChallanDate() >= maxTime)
			return last;
		ChallanInfo dummy = new ChallanInfo(maxTime);
		return get(conn, dummy);
	}
	public ChallanInfo simpleGet(int index) {
		return dataList.get(index);
	}
	public void simpleReplaceAt(int index, ChallanInfo data) {
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
		long mx = dataList.size() == 0 ? ref+1000 : dataList.get(0).getChallanDate();
		ArrayList<ChallanInfo> data = readHelperByTimeBound(conn, mi, mx, false);
		//dataList.mergeAtLeftReverse(data);
		//prevAddAt += data.size();
		//prevReadAt += data.size();
		int addCnt = specializedMerge(data,true);
		prevAddAt += addCnt;
		prevReadAt += addCnt;
	}
	private void readDataToRight(Connection conn, long mx, long ref) {
		long mi = dataList.size() == 0 ? ref-1000 : dataList.get(dataList.size()-1).getChallanDate();
		ArrayList<ChallanInfo> data = readHelperByTimeBound(conn, mi, mx, true);
		//dataList.mergeAtRight(data);
		int addCnt = specializedMerge(data,false);
	}
	

	private ArrayList<ChallanInfo> readHelperByTimeBound(Connection conn, long mi, long mx, boolean getHigher) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ChallanInfo> retval = new ArrayList<ChallanInfo>(g_reg_maxPoints/3);
		try {
//			public final static String GET_CHALLAN_DATA_BOUND = "select ch.vehicle_id,  ch.port_node_id, ch.challan_date, ch.challan_rec_date, ch.chd_id, ch.consignee, ch.dest_code, ch.dest_addr_1, ch.dest_addr_2, ch.dest_addr_3, ch.dest_addr_4, ch.dest_city, ch.dest_state, ch.material_id "+
//			" from challan_details where vehicle_id=?  and challan_date > ? and challan_date < ?  order by challan_date ";
			
			ps = conn.prepareStatement(getHigher ? (GET_CHALLAN_DATA_BOUND) : (GET_CHALLAN_DATA_BOUND_LOWER));
			
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mi));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(mx));
	
			//long internalLatestRecvTime = this.latestReceivedData == null ? Misc.getUndefInt() : this.latestReceivedData.getGpsRecvTime();
			//ps.setTimestamp(5, Misc.utilToSqlDate(internalLatestRecvTime));
			rs = ps.executeQuery();
			boolean prevIsInDataList = true;
			while (rs.next()) {
				ChallanInfo curr = ChallanInfo.read(conn, rs);
				retval.add(curr);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			for (int i=0,is=retval == null ? 0 : retval.size();i<is;i++) {
				ChallanInfo curr = retval.get(i);
				if (curr.getChallanRecvTime() > 0 && curr.getChallanRecvTime() > this.internalLatestRecvTime) {
					this.internalLatestRecvTime = curr.getChallanRecvTime();
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
	
	private ArrayList<ChallanInfo> readHelperByCount(Connection conn, long timeref, int cnt, boolean getHigher) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ChallanInfo> retval = new ArrayList<ChallanInfo>(g_reg_maxPoints/3);
		try {
			ps = conn.prepareStatement(getHigher? (GET_CHALLAN_DATA_BY_COUNT_HIGHER) : (GET_CHALLAN_DATA_BY_COUNT_LOWER));
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(timeref));
			ps.setInt(colIndex++, cnt);
			rs = ps.executeQuery();
			int ptsRead = 0;
			int dataListSz = dataList.size();
			while (rs.next()) {
				//vehicle.getGpsData().setName(null);
				ChallanInfo curr = ChallanInfo.read(conn, rs);
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
				ChallanInfo curr = retval.get(i);
				if (curr.getChallanRecvTime() > 0 && curr.getChallanRecvTime() > this.internalLatestRecvTime) {
					this.internalLatestRecvTime = curr.getChallanRecvTime();
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
	public ChallanInfo getFirstChallanAfterDtButBefEn(Connection conn, long dt, long en, boolean dofuzzy, int tripIdIfAny, long prevMappedChallan) {
		return getFirstChallanAfterDtButBefEn(conn, dt, en, dofuzzy, tripIdIfAny, prevMappedChallan,0,0); 
	}
	public ChallanInfo getFirstChallanAfterDtButBefEn(Connection conn, long dt, long en, boolean dofuzzy, int tripIdIfAny, long prevMappedChallan, int leftThresh, int rightThresh) {
		ChallanInfo dummy = new ChallanInfo(dt);
		java.util.Date dbgDt = new java.util.Date(dt);
		java.util.Date dbgEnDt = new java.util.Date(en);
		ChallanInfo retval = this.get(conn, dummy);
		ChallanInfo orig = retval;
		
		if (retval == null || retval.getChallanDate() < dt) {
			retval = this.get(conn, dummy, 1);
		}
		if (prevMappedChallan <= 0)
			prevMappedChallan = dt-60*60*1000;
		java.util.Date dbgDt3 = new java.util.Date(prevMappedChallan);
		
		if (retval != null && en > 0 && retval.getChallanDate() > en)
			retval = null;
		if (false && retval != null && orig != null && orig.getChallanDate() != retval.getChallanDate() && dofuzzy && orig.getChallanDate() < dt && orig.getChallanDate() > prevMappedChallan && retval.getChallanDate() > (3*60*60*1000 + dt)) {
			//see which one is better - if previous i
			System.out.println("[CHALLAN] returning older though in range exists: Older:"+Misc.longToUtilDate(orig.getChallanDate())
					+" PrevMapped:"+Misc.longToUtilDate(prevMappedChallan) 
					+" Newer:"+Misc.longToUtilDate(retval.getChallanDate())
					+" DtBeg:"+Misc.longToUtilDate(dt)
					+" DtEnd:"+Misc.longToUtilDate(en)
							);
			return orig;
		}
		if (retval == null && dofuzzy) {
			//DEBUG13 - todo currently if prevMappedChallan <=0 we will not allow challan more than 1 hr prior  -ideally we should look back and if still not found then look it up from the db
			
			retval = orig != null && orig.compareTo(dummy) <= 0 ? orig : get(conn, dummy, -1);
			if (retval != null && ((prevMappedChallan > 0 && retval.getChallanDate() <= prevMappedChallan))) {// || (retval.getTripId() > 0 && retval.getTripId() != tripIdIfAny
					retval = null;
			}
		}
		return retval;
	}
	public ChallanInfo getLastChallanAfterDtButBefEn(Connection conn, long dt, long en, boolean dofuzzy, int tripIdIfAny, long prevMappedChallan) {
		return getLastChallanAfterDtButBefEn(conn, dt, en, dofuzzy, tripIdIfAny, prevMappedChallan,0,0); 
	}
	public ChallanInfo getLastChallanAfterDtButBefEn(Connection conn, long dt, long en, boolean dofuzzy, int tripIdIfAny, long prevMappedChallan, int leftThresh, int rightThresh) {
		ChallanInfo dummy = new ChallanInfo(dt);
		java.util.Date dbgDt = new java.util.Date(dt);
		java.util.Date dbgEnDt = new java.util.Date(en);
		ChallanInfo retval = this.get(conn, dummy);
		ChallanInfo orig = retval;
		
		if (retval == null || retval.getChallanDate() < dt) {
			retval = this.get(conn, dummy, 1);
		}
		if (retval != null) {
			for (int i=1;;i++) {
				ChallanInfo ch = this.get(conn, dummy, i);
				if (ch == null || ch.getChallanDate() >= en)
					break;
				retval = ch;
			}
		}
		if (prevMappedChallan <= 0)
			prevMappedChallan = dt-60*60*1000;
		java.util.Date dbgDt3 = new java.util.Date(prevMappedChallan);
		
		if (retval != null && en > 0 && retval.getChallanDate() > en)
			retval = null;
		
		if (retval == null && dofuzzy) {
			//DEBUG13 - todo currently if prevMappedChallan <=0 we will not allow challan more than 1 hr prior  -ideally we should look back and if still not found then look it up from the db
			
			retval = orig != null && orig.compareTo(dummy) <= 0 ? orig : get(conn, dummy, -1);
			if (retval != null && ((prevMappedChallan > 0 && retval.getChallanDate() <= prevMappedChallan))) {// || (retval.getTripId() > 0 && retval.getTripId() != tripIdIfAny
					retval = null;
			}
		}
		return retval;
	}
	public static void main(String[] args) {
		try {
			Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			NewChallanMgmt chl = new  NewChallanMgmt(26130, -1,-1);
			chl.init(conn);
//			long t = chl.checkForUpdates(conn);

			ChallanInfo dummy = null;
			ChallanInfo retval = null;
			//2017-06-23 00:24:00
			dummy = new ChallanInfo((new java.util.Date(117,5,23,0,24,1)).getTime());
			retval = chl.get(conn, dummy);
			System.out.println(retval.getAlertEmailL1Customer());
			System.out.println(retval.getAlertEmailL2Customer());
			System.out.println(retval.getAlertPhoneL1Customer());
			System.out.println(retval.getAlertPhoneL2Customer());
			System.out.println(retval.getAlertUserL1Customer());
			System.out.println(retval.getAlertUserL2Customer());

			System.out.println(retval.getAlertEmailL1Transporter());
			System.out.println(retval.getAlertEmailL2Transporter());
			System.out.println(retval.getAlertPhoneL1Transporter());
			System.out.println(retval.getAlertPhoneL2Transporter());
			System.out.println(retval.getAlertUserL1Transporter());
			System.out.println(retval.getAlertUserL2Transporter());
			
			System.out.println(retval.getAlertEmailL1Sender());
			System.out.println(retval.getAlertEmailL2Sender());
			System.out.println(retval.getAlertPhoneL1Sender());
			System.out.println(retval.getAlertPhoneL2Sender());
			System.out.println(retval.getAlertUserL1Sender());
			System.out.println(retval.getAlertUserL2Sender());

			retval = chl.get(conn, dummy,1);
			System.out.println(retval.getAlertEmailL1Customer());
			System.out.println(retval.getAlertEmailL2Customer());
			System.out.println(retval.getAlertPhoneL1Customer());
			System.out.println(retval.getAlertPhoneL2Customer());
			System.out.println(retval.getAlertUserL1Customer());
			System.out.println(retval.getAlertUserL2Customer());

			System.out.println(retval.getAlertEmailL1Transporter());
			System.out.println(retval.getAlertEmailL2Transporter());
			System.out.println(retval.getAlertPhoneL1Transporter());
			System.out.println(retval.getAlertPhoneL2Transporter());
			System.out.println(retval.getAlertUserL1Transporter());
			System.out.println(retval.getAlertUserL2Transporter());
			
			System.out.println(retval.getAlertEmailL1Sender());
			System.out.println(retval.getAlertEmailL2Sender());
			System.out.println(retval.getAlertPhoneL1Sender());
			System.out.println(retval.getAlertPhoneL2Sender());
			System.out.println(retval.getAlertUserL1Sender());
			System.out.println(retval.getAlertUserL2Sender());

//			t = chl.checkForUpdates(conn);
			int dbg=1;
			dbg++;

		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}


}

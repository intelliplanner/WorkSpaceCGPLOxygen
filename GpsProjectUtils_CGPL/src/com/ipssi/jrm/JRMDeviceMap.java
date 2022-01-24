package com.ipssi.jrm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.jrm.JRMList.JRMPoint;

public class JRMDeviceMap  {
	public static int g_maxSendOnAdd = (int) 1450/76;
	public static int g_maxSendOnDel = (int) 1450/9;
	private static Map<String, JRMDeviceMap> g_deviceJRMMap  = new ConcurrentHashMap<String, JRMDeviceMap>();
	
	public static JRMDeviceMap getJRMDeviceMap(String deviceId) {
		JRMDeviceMap retval = g_deviceJRMMap.get(deviceId);
		if (retval == null) {
			retval = new JRMDeviceMap(deviceId);
			g_deviceJRMMap.put(deviceId, retval);
		}
		return retval;
	}
	public static void setAllRefreshRecalc(boolean refresh, boolean recalc) {
		Collection<JRMDeviceMap> values = g_deviceJRMMap.values();
		for (java.util.Iterator<JRMDeviceMap> iter = values.iterator();iter.hasNext();) {
			JRMDeviceMap item = iter.next();
			if (refresh)
				item.setRefresh(true);
			if (recalc)
				item.setIgnDistThresh(true);
		}
	}
	
	private String deviceId = null;
	private boolean refresh = false;
	private boolean ignDistThresh = false;
	private double lon = Misc.getUndefDouble();
	private double lat = Misc.getUndefDouble();
	private JRMList prevJRMList = null;
	private JRMList currJRMList = null;
	private int addSentTill = -1;//when newList is obtained ... we have to new - old (look at new and see if in old, if not then send, addSentTill is indexTill Which processed
	private int delSentTill = -1;//when newList is obtained ... we have to old - new (look at old and see if in new, if not then send, delSentTill is indexTill Which processed
	private int cntInDev = 0;
	private int tempAddSentTill = -1;
	private int tempDelSentTill = -1;
	private int tempCntInDev = 0;
	private long lastReq = -1;
	private int numShortDurGapAckFail = 0;
	private int numLongDurGapAckFail = 0;
	public String getDeviceStateString(int paramCnt, double paramLon, double paramLat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Device:").append(deviceId).append(" Refresh:").append(refresh).append(" IgnDist:").append(ignDistThresh)
		.append(" Lon:").append(lon).append(" Lat:").append(lat).append(" addSentTill:").append(addSentTill).append(" delSentTill:")
		.append(delSentTill).append(" cntInDev:").append(cntInDev)
		.append(" tempAddSentTill:").append(tempAddSentTill).append(" tempDelSentTill:")
		.append(tempDelSentTill).append(" tempCntInDev:").append(tempCntInDev)
		.append(" moreAdd:").append(this.moreAddToSend()).append(" moreDel:").append(this.moreDelToSend());
		if (paramCnt >= 0)
			sb.append(" Inconsistent:").append(this.isInconsistent(paramCnt));
		if (paramLon > 0 && paramLat > 0)
			sb.append(" RecalcGuidance:").append(this.recalcGuidance(paramLon, paramLat));
		return sb.toString();
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getDeviceStateString(-1, -1, -1))
		.append("\nCurrJRM:").append(this.currJRMList)
		.append("\nPrevJRM:").append(this.prevJRMList)
		;
		return sb.toString();
	}
	public void rememberState() {
		tempAddSentTill = addSentTill;
		tempDelSentTill = delSentTill;
		tempCntInDev = cntInDev;
	}
	public void revertState() {
		addSentTill = tempAddSentTill;
		delSentTill = tempDelSentTill;
		cntInDev = tempCntInDev;
	}
	public void confirmState() {
		tempAddSentTill = addSentTill;
		tempDelSentTill = delSentTill;
		tempCntInDev = cntInDev;
	}
	public boolean addWillBeRefresh() {
		return (this.prevJRMList == null || this.prevJRMList.size() == 0) && (this.addSentTill < 0);
	}
	public String getAddString() {
		return getAddString(JRMDeviceMap.g_maxSendOnAdd);
	}
	public String getAddString(int maxToGet) {
		StringBuilder sb = new StringBuilder();
		Pair<Integer, ArrayList<JRMPoint>> deltaList = currJRMList == null ? null : currJRMList.getMeMinusRHS(prevJRMList, addSentTill,  maxToGet);
		addSentTill = deltaList == null ? -1 : deltaList.first;
		this.cntInDev += deltaList == null ? 0 : deltaList.second.size();
		if (deltaList == null || deltaList.second.size() == 0)
			return  null;
		else
			return JRMList.toString(deltaList.second, false, true);
	}
	public String getDelString() {
		return getDelString(JRMDeviceMap.g_maxSendOnDel);
	}
	public String getDelString(int maxToGet) {
		StringBuilder sb = new StringBuilder();
		Pair<Integer, ArrayList<JRMPoint>> deltaList = prevJRMList == null ? null : prevJRMList.getMeMinusRHS(currJRMList, delSentTill,  maxToGet);
		delSentTill = deltaList == null ? -1 : deltaList.first;
		this.cntInDev -= deltaList == null ? 0 : deltaList.second.size();
		if (deltaList == null || deltaList.second.size() == 0)
			return  null;
		else
			return JRMList.toString(deltaList.second, true, true);
	}
	public int recalcGuidance(double newLon, double newLat) {//0 => dont, 1 => recalc, 2=>refresh
		double d = Point.fastGeoDistance(newLon, newLat, lon, lat);
		return d < 5 ? 0 : d > 30 ? 2 : 1;//DEBUG13 ... must be in prod
		//return d < 0.5 ? 0 : d > 10 ? 2 : 1;
	}
	public boolean isInconsistent(int totCnt) {
		return cntInDev >= 0 && totCnt >= 0 && totCnt != cntInDev;
	}
	public boolean moreAddToSend() {
		int currJrmListSz = currJRMList == null ? 0 : currJRMList.size();
		return currJrmListSz > (addSentTill+1);
	}
	public boolean moreDelToSend() {
		int prevJrmListSz = prevJRMList == null ? 0 : prevJRMList.size();
		return prevJrmListSz > (delSentTill+1);
	}
	public void prepForRefresh() {
		prevJRMList = null;
		addSentTill = -1;
		delSentTill = -1;		
		cntInDev = 0;
	}
	
	public void setJRM(JRMList newList, double lon, double lat) {
		prevJRMList = null;
		currJRMList = newList;
		addSentTill = -1;
		this.lon = lon;
		this.lat = lat;
		delSentTill = -1;
		//miInDev = Integer.MAX_VALUE;
		//mxInDev = Integer.MIN_VALUE;
		//cntInDev = 0;
	}
	public void replaceJRM(JRMList newList, double lon, double lat) {
		prevJRMList = currJRMList;
		currJRMList = newList;
		addSentTill = -1;
		this.lon = lon;
		this.lat = lat;
		delSentTill = -1;
	}
	
	public JRMDeviceMap(String deviceId) {
		super();
		this.deviceId = deviceId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public boolean isRefresh() {
		return refresh;
	}
	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}
	public boolean isIgnDistThresh() {
		return ignDistThresh;
	}
	public void setIgnDistThresh(boolean ignDistThresh) {
		this.ignDistThresh = ignDistThresh;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public JRMList getCurrJRMList() {
		return currJRMList;
	}
	public void setCurrJRMList(JRMList currJRMList) {
		this.currJRMList = currJRMList;
	}
	public int getAddSentTill() {
		return addSentTill;
	}
	public void setAddSentTill(int addSentTill) {
		this.addSentTill = addSentTill;
	}
	public int getDelSentTill() {
		return delSentTill;
	}
	public void setDelSentTill(int delSentTill) {
		this.delSentTill = delSentTill;
	}
	public long getLastReq() {
		return lastReq;
	}
	public void setLastReq(long lastReq) {
		this.lastReq = lastReq;
	}
	public int getNumShortDurGapAckFail() {
		return numShortDurGapAckFail;
	}
	public void setNumShortDurGapAckFail(int numShortDurGapAckFail) {
		this.numShortDurGapAckFail = numShortDurGapAckFail;
	}
	public int getNumLongDurGapAckFail() {
		return numLongDurGapAckFail;
	}
	public void setNumLongDurGapAckFail(int numLongDurGapAckFail) {
		this.numLongDurGapAckFail = numLongDurGapAckFail;
	}
	
}

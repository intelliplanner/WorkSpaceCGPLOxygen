package com.ipssi.common.ds.trip;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Iterator;

public class ShovelBackDataMgmt {
	private static class PerShovelInfo {
		public long mostBackData = -1;
		public ConcurrentHashMap<Integer, Integer> dumpersSeen = new ConcurrentHashMap<Integer, Integer>();
	}
	 
	private static ConcurrentHashMap<Integer, PerShovelInfo> shovelBackInfo = new ConcurrentHashMap<Integer, PerShovelInfo>();
	private static ConcurrentHashMap<Integer, Long> dumperBackUnprocTS = new ConcurrentHashMap<Integer, Long>();
	
	private static long backTSRunAt = -1;
	private static final long THRESH_FOR_CHECK = 60*60*1000;
	public static long getBackTSRunAt() {
		return backTSRunAt;
	}
	public static void setBackTSRunAt(long nowTime) {
		backTSRunAt = nowTime;
	}
	public static long getMinShovelDataTimeNeedingProc() {
		long miShovel = -1;
		Collection<PerShovelInfo> shovels = shovelBackInfo.values();
		for(Iterator<PerShovelInfo> iter = shovels.iterator(); iter.hasNext();) {
			PerShovelInfo shovelInfo = iter.next();
			long ts = shovelInfo.mostBackData;
			if (ts > 0 && (miShovel <= 0 || miShovel < ts)) {
				miShovel = ts;
			}
		}
		return miShovel;
	}
	
	public static void setMinDumperProcTimeSinceLastAllProc(int dumperId, long ts) {	
		Long entry = dumperBackUnprocTS.get(dumperId);
		long entryVal = entry == null ? -1 : entry.longValue();
		ts = entry == null || entryVal > ts || entryVal <= 0 ? ts : entry.longValue();
		dumperBackUnprocTS.put(dumperId, ts);
	}
	
	public static void setShovelMinTimeSinceLastAllProc(int shovelId, long ts) {
		long curr = ThreadContextCache.getCurrTS();
		if (ts > curr)
			return;
		PerShovelInfo entry = shovelBackInfo.get(shovelId);
		long lastBackD = entry == null ? -1 : entry.mostBackData;
		if (ts > 0 && (lastBackD <= 0 || lastBackD > ts)) {
			if (entry == null) {
				entry = new PerShovelInfo();
				shovelBackInfo.put(shovelId, entry);
			}
			entry.mostBackData = ts;
		}
	}
	public static void resetShovelToAllProc() {
		Collection<PerShovelInfo> shovels = shovelBackInfo.values();
		for (Iterator<PerShovelInfo> iter = shovels.iterator(); iter.hasNext();) {
			PerShovelInfo shovel = iter.next();
			shovel.mostBackData = -1;
		}
	}
	
	public static void setDumperAsOfInterest(int shovelId, int dumperId) {//Not used 
		PerShovelInfo entry = shovelBackInfo.get(shovelId);
		if (entry == null) {
			entry = new PerShovelInfo();
			shovelBackInfo.put(shovelId, entry);
		}
		entry.dumpersSeen.put(dumperId, dumperId);
	}
}

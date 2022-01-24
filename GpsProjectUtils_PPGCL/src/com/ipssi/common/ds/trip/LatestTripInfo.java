package com.ipssi.common.ds.trip;

import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.tripcommon.LUInfoExtract;

public class LatestTripInfo {
	private static ConcurrentHashMap<Integer, LatestTripInfo> g_latestTripInfo = new ConcurrentHashMap<Integer, LatestTripInfo>();
	public static LatestTripInfo getLatestTripInfo(int vehicleId) {
		return g_latestTripInfo.get(vehicleId);
	}
	public static void setLatestTripInfo(int vehicleId, LUInfoExtract load, LUInfoExtract unload, ChallanInfo challanInfo, LUInfoExtract rest, long ongoingStop) {
		LatestTripInfo latest = g_latestTripInfo.get(vehicleId);
		if (latest == null) {
			latest = new LatestTripInfo(load, unload, challanInfo, rest, ongoingStop);
			g_latestTripInfo.put(vehicleId, latest);
		}
		else {
			latest.setLoad(load);
			latest.setUnload(unload);
			latest.setChallanInfo(challanInfo);
		}
	}
	private LUInfoExtract load;
	private LUInfoExtract unload;
	private LUInfoExtract ifRest;
	private long ongoingTransitStopStart;
	
	private ChallanInfo challanInfo;
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Load:");
		if (load != null) {
			sb.append(load.getOfOpStationId()).append(" In:").append(new java.util.Date(load.getGateIn()))
			.append(" Out:").append(new java.util.Date(load.getGateOut()));
		}
		sb.append(" Unload:");
		if (unload != null) {
			sb.append(unload.getOfOpStationId()).append(" In:").append(new java.util.Date(unload.getGateIn()))
			.append(" Out:").append(new java.util.Date(unload.getGateOut()));
		}
		sb.append(" Challan:");
		if (challanInfo != null) {
			sb.append(new java.util.Date(challanInfo.getChallanDate()));
		}
		return sb.toString();
	}
	
	public LatestTripInfo(LUInfoExtract load, LUInfoExtract unload, ChallanInfo challanInfo, LUInfoExtract restSeq, long ongoingStop) {
		this.load = load;
		this.unload = unload;
		this.challanInfo = challanInfo;
		this.ifRest = restSeq;
		this.ongoingTransitStopStart = ongoingStop;
	}
	public LUInfoExtract getLoad() {
		return load;
	}
	public void setLoad(LUInfoExtract load) {
		this.load = load;
	}
	public LUInfoExtract getUnload() {
		return unload;
	}
	public void setUnload(LUInfoExtract unload) {
		this.unload = unload;
	}
	public ChallanInfo getChallanInfo() {
		return challanInfo;
	}
	public void setChallanInfo(ChallanInfo challanInfo) {
		this.challanInfo = challanInfo;
	}
	public LUInfoExtract getIfRest() {
		return ifRest;
	}
	public void setIfRest(LUInfoExtract ifRest) {
		this.ifRest = ifRest;
	}
	public long getOngoingTransitStopStart() {
		return ongoingTransitStopStart;
	}
	public void setOngoingTransitStopStart(long ongoingStopStart) {
		this.ongoingTransitStopStart = ongoingTransitStopStart;
	}
}

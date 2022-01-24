package com.ipssi.miningOpt;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.miningOpt.Predictor.SimulInfo;
import com.ipssi.tripcommon.ExtLUInfoExtract;

public class WaitItem implements Comparable {
	private int vehicleId = Misc.getUndefInt();
	private long entryAt = Misc.getUndefInt();
	private long loadBeginAt = Misc.getUndefInt();
	private long loadEndAt = Misc.getUndefInt();
	private long exitAt = Misc.getUndefInt();
	private int shovelId = Misc.getUndefInt();
	public WaitItem(int vehicleId, SimulInfo info) {
		this.vehicleId = vehicleId;
		this.entryAt = info.entryAt;
	}
	public WaitItem(int vehicleId, ExtLUInfoExtract ext) {
		this.vehicleId = vehicleId;
		this.entryAt = ext.getWaitIn();
		this.loadBeginAt = ext.getGateIn();
		this.loadEndAt = ext.getGateOut();
		this.exitAt = ext.getWaitOut();
		this.shovelId = ext.getMiningInfo() == null ? Misc.getUndefInt() : ext.getMiningInfo().getShovelId();
	}
	public void update(ExtLUInfoExtract ext) {
		this.entryAt = ext.getWaitIn();
		this.loadBeginAt = ext.getGateIn();
		this.loadEndAt = ext.getGateOut();
		this.exitAt = ext.getWaitOut();
		this.shovelId = ext.getMiningInfo() == null ? Misc.getUndefInt() : ext.getMiningInfo().getShovelId();
	}
	public void update(SimulInfo ext) {
		this.entryAt = ext.entryAt;
	}
	public boolean equals(Object o) {
		WaitItem rhs = (WaitItem) o;
		return rhs != null && ((WaitItem) rhs).getVehicleId() == this.vehicleId;
	}
	public int compareTo(Object o) {
		WaitItem rhs = (WaitItem) o;
		long diff = rhs.getEntryAt() - this.getEntryAt();
		int retval = diff > 0 ? 1 : diff == 0 ? 0 : -1;
		if (diff ==0)
			retval = rhs.getVehicleId() - vehicleId;
		return retval;
	}
	
	
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getStatus() {
		return exitAt > 0  ? 3 : loadEndAt > 0 ? 2 : loadBeginAt > 0 ? 1 : 0;
	}
	public long getEntryAt() {
		return entryAt;
	}
	public void setEntryAt(long entryAt) {
		this.entryAt = entryAt;
	}
	public long getLoadBeginAt() {
		return loadBeginAt;
	}
	public void setLoadBeginAt(long loadBeginAt) {
		this.loadBeginAt = loadBeginAt;
	}
	public long getLoadEndAt() {
		return loadEndAt;
	}
	public void setLoadEndAt(long loadEndAt) {
		this.loadEndAt = loadEndAt;
	}
	public long getExitAt() {
		return exitAt;
	}
	public void setExitAt(long exitAt) {
		this.exitAt = exitAt;
	}
	public int getShovelId() {
		return shovelId;
	}
	public void setShovelId(int shovelId) {
		this.shovelId = shovelId;
	}
	public int numCyclesForLoad(ShovelInfo shovelInfo, DumperInfo dumperInfo) {//TODO
		double dumperCapVol = dumperInfo == null ? 15 : (double) dumperInfo.getCapacityVol();
		double shovelCapVol = shovelInfo == null ? 3 : (double) shovelInfo.getCapacityVol();
		int num =   (int) Math.round(dumperCapVol/shovelCapVol);
		if (num < 1)
			num = 1;
		return num;
	}
	public double tonnes(DumperInfo dumperInfo) {//TODO
		return dumperInfo == null ? 22 : dumperInfo.getCapacityWt();
		
	}
	
}

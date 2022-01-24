package com.ipssi.miningOpt;

import com.ipssi.gen.utils.Misc;

public class ShovelDisplay {
	private ShovelInfo shovel;
	int totStatTripDisp = 0;
	int totStatTonnDisp = 0;
	int totStatNumCycles = 0;
	int totStatNumCycleSec = 0;
	long latestDispAt = Long.MIN_VALUE;
	long minTS = Long.MAX_VALUE;
	long maxTS = Long.MIN_VALUE;
	String name;
	double fuelLevel = Misc.getUndefDouble();
	long fuellingNeededAt = -1;
	long dataAt = -1;
	String locAt = null;
	String ignOn = null;
	StringBuilder normEvent = null;
	StringBuilder critEvent = null;;
	
	public ShovelDisplay(ShovelInfo shovelInfo) {
		this.shovel = shovelInfo;
	}
	public void calc() {
		
	}
	public ShovelInfo getShovel() {
		return shovel;
	}
	public void setShovel(ShovelInfo shovel) {
		this.shovel = shovel;
	}
}

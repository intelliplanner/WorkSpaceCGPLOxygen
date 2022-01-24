package com.ipssi.miningOpt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;

public class UnloadSite extends Site {
	public UnloadSite(int id, NewMU ownerMU) {
		super(id, ownerMU);
	}
	public void toString(StringBuilder sb, boolean doAllProp) {
		super.toString(sb, doAllProp);
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, true);
		return sb.toString();
	}
}

package com.ipssi.report.cache;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;

public class AddnlVehicleInfo {
	private static ConcurrentHashMap<Integer, AddnlVehicleInfo> g_addnlInfoForCalc = new ConcurrentHashMap<Integer, AddnlVehicleInfo>();
	private int routeId = Misc.getUndefInt();
	private int regionId = Misc.getUndefInt();
	private byte dirty = 0;
	private ArrayList<DestItem> destItems = null;
}

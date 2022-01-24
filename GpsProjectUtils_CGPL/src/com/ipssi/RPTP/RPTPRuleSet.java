package com.ipssi.RPTP;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;

public class RPTPRuleSet {
	public static ConcurrentHashMap<Integer, RPTPRuleSet> g_allRPTPRuleSet = new ConcurrentHashMap<Integer, RPTPRuleSet>();
	public ArrayList<RPTPRuleItem> rulesList = null;
	public static void loadRPTPRuleSet(Connection conn, int rulesetId, boolean doReload) throws Exception {
		
	}
	/*
	public StringBuilder generateEventUpdateQuery(int vehicleId) {
		StringBuilder sb = new StringBuilder();
		sb.append("update engine_events ee join vehicle v on (v.vehicle_id = ee.vehicle_id and v.rptp_id = ?) join trip_info tt on (");
		boolean joinAndNeeded = false;
		if (!Misc.isUndef(vehicleId)) {
			sb.append("ee.vehicle_id = tt.vehicle_id and ee.vehicle_id = ? ");
		}
		else {
			
		}
	}
	*/
}

package com.ipssi.mining;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tripcommon.LUInfoExtract;

public class CurrentStation extends BaseStat {
	
	private ArrayList<Station> stations = new ArrayList<Station>();
	boolean dirty = true;
	public Pair<Long, Long> getMinMaxTS(Connection conn, ManagementUnit mu) throws Exception {
		Pair<Long, Long> retval = null;
		for (Station s: stations) {
			retval = BaseStat.getMinMaxTS(conn, s.recentVehicleList, retval, true, mu);
		}
		return retval;
	}
	public static long windowForRateMilli = 30*60*1000;
	public static double currMergeKM = 0.050;
	public static int currMergeLonDeltaInt = Misc.getUndefInt();
	public static int currMergeLatDeltaInt = Misc.getUndefInt();
	public static int defaultLoadSec = 150;

	public void updateStats(Connection conn, ManagementUnit mu) throws Exception {
		if (!dirty)
			return;
		Pair<Long, Long> mimxTS = getMinMaxTS(conn, mu);
		long mi = mimxTS.first;
		long mx = mimxTS.second;
		long desiredMi = mx-windowForRateMilli;
		
		super.setWindowSec((int)((mx-windowForRateMilli)/1000));
		super.reset();
		
		for (Station s: stations) {
			ArrayList<RecentVehicleInfo> vehList = s.recentVehicleList;
			super.updateStats(vehList, desiredMi, true, mu);
		}
		dirty = false;
	}
	
	public void removeStation(Station station) {
		for (int i=0,is=stations == null ? 0 : stations.size(); i<is; i++) {
			if (stations.get(i).getId() == station.getId()) {
				stations.remove(i);
				dirty  = true;
				break;
			}
		}
	}
	public void addStation(Station station) {
		if (stations == null)
			stations = new ArrayList<Station>();
		stations.add(station);
		dirty = true;
	}

	public ArrayList<Station> getStations() {
		return stations;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
}

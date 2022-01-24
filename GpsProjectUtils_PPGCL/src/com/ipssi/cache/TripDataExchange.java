package com.ipssi.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.tripcommon.ExtLUInfoExtract;
import com.ipssi.tripcommon.LUInfoExtract;

public class TripDataExchange {
	private static ConcurrentHashMap<Integer, TripDataExchange> g_tripDataExchange = new ConcurrentHashMap<Integer, TripDataExchange>(OrgConst.G_NUMBER_VEHICLES, 0.75f);
	private FastList<TripDataExchangeItem> m_list = new FastList<TripDataExchangeItem>();
	private long cleanTillRP = Misc.getUndefInt();
	public void addTripDataExchange(TripDataExchangeItem tripData) {
		Pair<Integer, Boolean> pos = m_list.indexOf(tripData);
		if (pos.second) {
			TripDataExchangeItem old = m_list.get(pos.first);
			if (tripData.equals(old))
				return;
			m_list.replaceAt(pos.first, tripData);
		}
		else {
			m_list.add(tripData);
		}
	}
	
	public void cleanTripDataExchange(int pos, long maxTimeTill, boolean isRP) {//TODO - currently only for RP ... to do for DP user .. if needed
		cleanTillRP = maxTimeTill;
		long cleanTill = cleanTillRP;
		for (int i= m_list == null ? -1 : m_list.size()-1; i>= pos; i--) {
			TripDataExchangeItem tripData = m_list.get(i);
			if (tripData == null) {
				m_list.remove(i);
				continue;
			}
			long latestTime = tripData.getLatestTime();
			if (latestTime <= 0)
				m_list.remove(i);
			if (tripData.isRPComp() && latestTime <= cleanTill)
				m_list.remove(i);
		}
	}
	
	public static void updateTripDataExchangeItem(int blockTripId, ExtLUInfoExtract luInfoExt, int vehicleId, boolean isDeleted, boolean isLoad){
		
	}
	
	public static TripDataExchangeItem getTripDataExchangeItem(int vehicleId, long gpsRecordTime){
		TripDataExchange tripDataExchange = getTripDataExchange(vehicleId);
		TripDataExchangeItem tripDataExchangeItem = null;
		if(tripDataExchange.m_list != null && tripDataExchange.m_list.size() > 0){
			FastList<TripDataExchangeItem> tripDataList = tripDataExchange.m_list;
			tripDataExchangeItem = tripDataList.get(new TripDataExchangeItem(vehicleId, gpsRecordTime));
		}
		return tripDataExchangeItem;
	}
	
	public static TripDataExchange getTripDataExchange(int vehicleId){
		TripDataExchange tripDataExchange = g_tripDataExchange.get(vehicleId);
		if(tripDataExchange == null){
			tripDataExchange = new TripDataExchange();
			g_tripDataExchange.put(vehicleId, tripDataExchange);
		}
		return tripDataExchange;
	}
	
	public static TripDataExchangeItem copyLuInfoExtract(LUInfoExtract luInfoExt){
		TripDataExchangeItem tripDataItem = new TripDataExchangeItem();
		tripDataItem.setLgin(luInfoExt.getGateIn());
		return tripDataItem;
	}
}

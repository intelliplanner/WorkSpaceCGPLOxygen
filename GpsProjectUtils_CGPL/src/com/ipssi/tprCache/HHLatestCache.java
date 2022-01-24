package com.ipssi.tprCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class HHLatestCache {
	private static ConcurrentHashMap<Integer, Long> g_ByMinesIdLatestTS = new ConcurrentHashMap<Integer, Long>();
	public static long getLatest(int minesId) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			Long retval =g_ByMinesIdLatestTS.get(minesId); 
			return retval == null ? Misc.getUndefInt() : retval.longValue();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e) {
					
				}
			}
		}
		return Misc.getUndefInt();
	}
	public static void load(Connection conn) throws Exception {
		try {
			System.out.println("[TPRBUILDCACHE] "+Thread.currentThread().getId()+" Building HH Latest Log Cache");
			PreparedStatement ps = conn.prepareStatement("select mines, max(record_time) from rfid_handheld_log where status = 1 group by mines");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int mid = rs.getInt(1);
				long ts = Misc.sqlToLong(rs.getTimestamp(2));
				g_ByMinesIdLatestTS.put(mid, ts);
			}
			System.out.println("[TPRBUILDCACHE] "+Thread.currentThread().getId()+" Done Building HH Latest Log Cache");
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}

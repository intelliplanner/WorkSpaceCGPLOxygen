package com.ipssi.common.ds.OpsTPR;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;

public class OpsToTPRMines {
	private static ConcurrentHashMap<Integer, ArrayList<Integer>> opsToMines = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	private static ConcurrentHashMap<Integer, Integer> minesToOps = new ConcurrentHashMap<Integer, Integer>();
	private static ConcurrentHashMap<Integer, String> minesName = new ConcurrentHashMap<Integer, String>();
	private static ConcurrentHashMap<Integer, Integer> minesToLatestCurrDOId = new ConcurrentHashMap<Integer, Integer>();//key = minesId, value = latest do that is of status =1
	private volatile static boolean loadedMinesToOpsOnce = false;
	private volatile static long lastLoadOfLatestDO = -1;
	public static int g_freqOfDOCheckMilli = 6*3600*1000;
	public static String getMinesName(Connection conn, int minesId) {
		if (!loadedMinesToOpsOnce)
			loadMinesOpsMappingFromDB(conn);
		return minesName.get(minesId);
	}
	public static ArrayList<Integer> getMinesListForOps(Connection conn, int opsId, boolean checkAgainstDO) {
		if (!loadedMinesToOpsOnce)
			loadMinesOpsMappingFromDB(conn);
		ArrayList<Integer> retval = opsToMines.get(opsId);
		if (retval == null || retval.size() == 0 || !checkAgainstDO) {
			return retval;
		}
		ArrayList<Integer> actRetval = new ArrayList<Integer>();
		if (lastLoadOfLatestDO <= 0 || (System.currentTimeMillis()-lastLoadOfLatestDO) > g_freqOfDOCheckMilli) {
			loadLatestDOForMinesFromDB(conn);
		}
		for (Integer minesId:retval) {
			if (minesToLatestCurrDOId.contains(minesId)) {
				actRetval.add(minesId);
			}
		}
		return actRetval;
	}
	public static boolean isOperativeOp(Connection conn, int opsId, boolean checkAgainstDO) {//there is a valid Mines for the OpStation and there are valid DO current
		ArrayList<Integer> minesList = getMinesListForOps(conn, opsId, false);
		boolean retval = minesList != null && minesList.size() > 0;
		if (checkAgainstDO && retval) {
			if (lastLoadOfLatestDO <= 0 || (System.currentTimeMillis()-lastLoadOfLatestDO) > g_freqOfDOCheckMilli) {
				loadLatestDOForMinesFromDB(conn);
			}
			for (Integer minesId:minesList) {
				if (minesToLatestCurrDOId.contains(minesId)) {
					retval = true;
					break;
				}
			}
		}
		return retval;
	}
	
	public static boolean isOperativeOp(Connection conn, int opsId, int checkForMinesId, boolean checkAgainstDO) {//there is a valid Mines for the OpStation and there are valid DO current
		ArrayList<Integer> minesList = getMinesListForOps(conn, opsId, false);
		boolean retval = minesList != null && minesList.size() > 0;
		if (checkAgainstDO && retval) {
			if (lastLoadOfLatestDO <= 0 || (System.currentTimeMillis()-lastLoadOfLatestDO) > g_freqOfDOCheckMilli) {
				loadLatestDOForMinesFromDB(conn);
			}
			for (Integer minesId:minesList) {
				if (checkForMinesId == minesId) {
					if (minesToLatestCurrDOId.contains(minesId)) {
						retval = true;
					}
					break;
				}
			}
		}
		return retval;
	}
	
	public static void loadLatestDOForMinesFromDB(Connection conn) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			minesToLatestCurrDOId.clear();
			ps = conn.prepareStatement("select mines_id, status, max(id) from do_rr_details where status=1 group by mines_id, status");
			rs = ps.executeQuery();
			while (rs.next()) {
				int minesId = Misc.getRsetInt(rs, 1);
				int maxDoId = Misc.getRsetInt(rs, 3);
				minesToLatestCurrDOId.put(minesId, maxDoId);			
			}
			lastLoadOfLatestDO = System.currentTimeMillis();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			
		}
	}
	public static void loadMinesOpsMappingFromDB(Connection conn) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			String q = "select mines_details.id, op_station.id, mines_details.name from mines_details left outer join op_station on (mines_details.opstation_id = op_station.id and op_station.status=1) where mines_details.status=1";
			opsToMines.clear();
			minesToOps.clear();
			minesName.clear();
			ps = conn.prepareStatement(q);
			rs = ps.executeQuery();
			while (rs.next()) {
				int dataMinesId = rs.getInt(1);
				int opsId = Misc.getRsetInt(rs, 2);
				String name = rs.getString(3);
				if (!Misc.isUndef(opsId)) {
					ArrayList<Integer> minesEntryForOp = opsToMines.get(opsId);
					if (minesEntryForOp == null) {
						minesEntryForOp = new ArrayList<Integer>();
						opsToMines.put(opsId, minesEntryForOp);
					}
					minesEntryForOp.add(dataMinesId);
					minesToOps.put(dataMinesId, opsId);
				}
				minesName.put(dataMinesId, name);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			loadedMinesToOpsOnce = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
}

package com.ipssi.tprCache;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.coalSampling.RFIDSampleCache;
import com.ipssi.common.ds.OpsTPR.OpsToTPRMines;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DriverExtendedInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;

public class Loader implements Runnable {
	public static volatile ScheduledExecutorService  g_getRFIDCache = null;
	public static volatile String g_status = null;
	public static volatile int g_loadFreqSec = 5*60;
	public static volatile Date prevDate = null;
	public static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
	//Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(processEligibleRuleList(), intialDelay, delay, TimeUnit.SECONDS);
	
	public void run() {
		// TODO Auto-generated method stub
		executeLoad();
	}
	
	public static void executeLoad() {
		Connection conn = null;
		boolean destroyIt = false;
		Date currDate = null;
		
		try {
			if(Misc.g_doMPL || Misc.g_doSCCL){
			setStatus("Doing Load of TPR at:"+new java.util.Date());
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			}
			if(Misc.g_doMPL || Misc.g_doSCCL){
			TPRLatestCache.load(conn);
			}
			if(Misc.g_doMPL){
			setStatus("Doing Load of HH");
			HHLatestCache.load(conn);
			
			setStatus("Completed Loading at:"+new java.util.Date());
			currDate = dateFormatter.parse(dateFormatter.format(new Date().getTime()));
		
			if(prevDate==null || prevDate.before(currDate)){
				System.out.println("##### [Loader][Auto Sub-Lot/Lot Process Run] #####");
				RFIDSampleCache.autoGenerateSubLots(conn,true);
				RFIDSampleCache.autoGeneratePostLots(conn,true);
				prevDate = currDate;
			}
			
			}
		}
		catch (Exception e) {
			destroyIt = true;
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
	}
	public static void loadNow() {
		executeLoad();
	}
	public static synchronized void setLoadFreq(int freq) {
		if (freq != g_loadFreqSec) {
			stop();
			g_loadFreqSec = freq;
			start();
		}
	}
	public static synchronized void stop() {
		if (g_getRFIDCache != null) {
			g_getRFIDCache.shutdownNow();
			g_status = "ShutDown";
		}
		g_getRFIDCache = null;    	
	}
	public static synchronized void start() {
		if (g_getRFIDCache == null) {
			g_getRFIDCache = Executors.newScheduledThreadPool(1);
			g_getRFIDCache.scheduleWithFixedDelay(new Loader(), g_loadFreqSec, g_loadFreqSec, TimeUnit.SECONDS);
			g_status = "Started";
		}		
	}
	public static synchronized String status() {
		return g_status;
	}
	private static void setStatus(String str) {
		g_status = str;
	}

	public static void main(String[] args) throws GenericException {
		executeLoad();
		Connection conn = null;
		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		int vehicleId = 43656;//43875
		int dimId = 90174;
		System.out.println(OpsToTPRMines.getMinesName(conn, 1));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 358, true));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 358, false));
		System.out.println(OpsToTPRMines.getMinesName(conn, 4));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 360, true));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 360, false));
		System.out.println(TPRLatestCache.getLatest(43656));
		System.out.println(TPRLatestCache.getLatest(43875));
		System.out.println(Misc.longToUtilDate(HHLatestCache.getLatest(1)));
		System.out.println(Misc.longToSqlDate(HHLatestCache.getLatest(5)));
	//	ResultInfo.getCachedValueWithHack(conn, vehicleId, dimId, CacheTrack.VehicleSetup.getSetup(vehicleId, conn), VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false), VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null);
		DBConnectionPool.returnConnectionToPoolNonWeb(conn);
	}
}

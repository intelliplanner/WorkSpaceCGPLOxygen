package com.ipssi.mobilenotification;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.mobilenotification.NotificationHelper;

public class NotificationExecutor implements Runnable {
	public static volatile ScheduledExecutorService g_getRFIDCache = null;
	public static volatile String g_status = null;
	public static volatile int g_loadFreqSec = 1 * 60;

	// Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(processEligibleRuleList(),
	// intialDelay, delay, TimeUnit.SECONDS);

	public void run() {
		executeLoad();
	}

	public static void executeLoad() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			setStatus("Sending Notification at:" + new java.util.Date());
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			NotificationHelper.prepareAndSendNotification(conn);
		} catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
			// eat it
		} finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn,	destroyIt);
				} catch (Exception e) {

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
			g_getRFIDCache.scheduleWithFixedDelay(new NotificationExecutor(),
					g_loadFreqSec, g_loadFreqSec, TimeUnit.SECONDS);
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
//		Connection conn = null;
//		conn = DBConnectionPool.getConnectionFromPoolNonWeb();

		// ResultInfo.getCachedValueWithHack(conn, vehicleId, dimId,
		// CacheTrack.VehicleSetup.getSetup(vehicleId, conn),
		// VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false),
		// VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null);
//		DBConnectionPool.returnConnectionToPoolNonWeb(conn);
	}
}

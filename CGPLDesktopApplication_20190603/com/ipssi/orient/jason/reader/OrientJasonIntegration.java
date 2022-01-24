package com.ipssi.orient.jason.reader;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ipssi.gen.utils.DBConnectionPool;
/**
 * Below Entry required on new_conn.property file.
 * default.orient.tcp.ip=203.197.197.17
 * default.orient.tcp.port=5205
 * default.orient.vehicle.data.url=http://202.143.97.118/jsp/Service_vehicle.jsp?user=koyal&pass=123456
 * @author Praveen
 *
 */
public class OrientJasonIntegration implements Runnable {
	public static volatile ScheduledExecutorService  g_service = null;
	public static volatile String g_status = null;
	public static int simTrackingCount = 0;
	public static volatile int g_fetchFreqSec =OrientUtility.urlInterval;//from new_con.property
	public void run() {
		System.out.println("OrientJasonIntegration.run() Start at:"+new java.util.Date());
		execute();
		System.out.println("OrientJasonIntegration.run() End at:"+new java.util.Date());
	}
	
	public static void execute() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			
			OrientUtility.getVehicleJsonData();
			OrientUtility.sendToTcp();
			OrientUtility.getTrackinggenieData();
			OrientUtility.sendTrackinggenieToTcp();	
			
			if(simTrackingCount==5){
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				TraceMateHelper.locateUser(conn);
				simTrackingCount=0;
			}
			simTrackingCount++;
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
	
	public static void main(String[] args) {
		simTrackingCount=5;
		execute();
	}
	
	public static synchronized void stop() {
		if (g_service != null) {
			g_service.shutdownNow();
			g_status = "ShutDown";
		}
		g_service = null;    	
	}
	public static synchronized void start() {
		if (g_service == null) {
			g_service = Executors.newScheduledThreadPool(1);
			g_service.scheduleWithFixedDelay(new OrientJasonIntegration(), g_fetchFreqSec, g_fetchFreqSec, TimeUnit.SECONDS);
			g_status = "Started";
		}		
	}
	
}

package com.ipssi.eta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class RedoHelper extends Thread {
	public static int G_REDO_VEHICLE = 1;
	public static int G_RECALC_STATE_VEHICLE = 2;
	public static int G_UPDATE_SRC_DEST = 3;
	private static final int numMaxMsgPermissible = 100;
	
	private ArrayList<Integer> givenVehicleIds = null;
	private int srcDestId;
	private boolean srcDestRemoved;
	private String serverName = null;
	private int action = Misc.getUndefInt();
	
	public static void execAction(ArrayList<Integer> vehicleIds, int action, int srcDestId, boolean srcDestRemoved) {
		Thread th = new RedoHelper(vehicleIds, action, srcDestId, srcDestRemoved);
		th.start();
	}
	public RedoHelper(ArrayList<Integer> vehicleIds, int action, int srcDestId, boolean srcDestRemoved) {
		this.action = action;
		this.givenVehicleIds = vehicleIds;
		this.serverName = Misc.getServerName();
		this.srcDestId = srcDestId;
		this.srcDestRemoved = srcDestRemoved;
	}
	
	public void run() {
		process();
	}
	
	public void process() {
		//0. get Connection
		//1. get list of vehicles that may be impacted
		Connection conn = null;
		Connection jmsConn = null;
		boolean destroyIt = false;
		try {
			String prefix = "[ETA]  Thread id:"+Thread.currentThread().getId()+" :";
			System.out.println(prefix+"Controller started");
			
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (action == G_UPDATE_SRC_DEST || givenVehicleIds == null || givenVehicleIds.size() == 0)
				this.givenVehicleIds = NewVehicleETA.getVehicleList();
			int curr = 0;
			int perLoopVeh = 20;
			while (true) {
				boolean exitOutOfOuterLoop = false;				
				for (int is = curr+perLoopVeh < givenVehicleIds.size() ? curr+perLoopVeh : givenVehicleIds.size(); curr<is; curr++) {
					if (action == G_REDO_VEHICLE) {
						NewVehicleETA.redoSingleVehicle(conn, givenVehicleIds.get(curr), false, -1);
					}
					else if (action == G_RECALC_STATE_VEHICLE) {
						NewVehicleETA.handleSrcDestInfoChange(conn, givenVehicleIds.get(curr), this.srcDestId, this.srcDestRemoved);
					}
					else if (action == G_UPDATE_SRC_DEST) {
						NewVehicleETA.handleSrcDestInfoChange(conn, givenVehicleIds.get(curr), this.srcDestId, this.srcDestRemoved);
					}
					else {
						exitOutOfOuterLoop = true;
						break;
					}
				}
				if (exitOutOfOuterLoop)
					break;
				System.out.println(prefix+"Processed vehicle "+curr+" of "+givenVehicleIds.size());
				if (curr >= givenVehicleIds.size()) {
					if (jmsConn != null)
						Misc.closeJMS_DB_CONN();
					jmsConn = null;
					break;
				}
				//now sleep until message pile up goes low
				//try getting jmsConn twice .. if cant exit

				
				while (true) {
					jmsConn = Misc.getJMS_DB_CONN();
					if (jmsConn == null || !testConn(jmsConn)) {
						Misc.closeJMS_DB_CONN();
						jmsConn = null;
						try {
							Thread.sleep(10000);
						}
						catch (Exception e) {
							//eat it
						}
					}
					if (jmsConn == null) {
						jmsConn = Misc.getJMS_DB_CONN();
						if (jmsConn == null || !testConn(jmsConn)) {
							Misc.closeJMS_DB_CONN();
							jmsConn = null;
							exitOutOfOuterLoop = false;
							System.out.println(prefix+"Controller exiting because error in JMS conn");
							break;
						}
					}
					int currMessageCount = getPendingMessages(jmsConn);
					if (currMessageCount < 0) {
						Misc.closeJMS_DB_CONN();
						jmsConn = null;
						exitOutOfOuterLoop = true;
						System.out.println(prefix+"Controller exiting because error in JMS getMessageCnt");
						break;
					}
					if (currMessageCount < numMaxMsgPermissible) {
						System.out.println(prefix+"Controller continuing because few gpsQ messages:"+currMessageCount);
						break;
					}
					else {
						System.out.println(prefix+"Controller sleeping because pending gpsQ messages:"+currMessageCount);
						long sleepDurMilli = currMessageCount*60*2;//1000 messages per min
						if (sleepDurMilli < 10000)
							sleepDurMilli = 10000;
						try {
							Thread.sleep(sleepDurMilli);
						}
						catch (Exception e) {
							
						}
					}
				}//end if loop waiting for messages to go low
				if (exitOutOfOuterLoop)
					break;
			}//while !done
		}
		catch (Exception e) {
			
		}
		finally {
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
				conn = null;
			}
			catch (Exception e) {
				
			}
			try {
				if (jmsConn != null)
					Misc.closeJMS_DB_CONN();
				jmsConn = null;
			}
			catch (Exception e) {
				
			}
		}//finally
		
	}
	
	public int getPendingMessages(Connection jmsConn) { //returns -1 if there is an exception ... meaning there is no point in trying to process
		PreparedStatement ps = null;
		try {
			ps = jmsConn.prepareStatement("select count(*) from JMS_MESSAGES where DESTINATION like '%gpsQueue%'");
			ResultSet rs = ps.executeQuery();
			int retval = 0;
			if (rs.next()) {
				retval = rs.getInt(1);
			}
			rs.close();
			ps.close();
			ps = null;
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		finally {
			try {
				if (ps != null) 
					ps.close();
			}
			catch (Exception e4) {
				e4.printStackTrace();				
			}
		}
	}

	private static boolean testConn(Connection conn) {
		boolean retval = false;
		if (conn != null) {
			try {
				PreparedStatement ps = conn.prepareStatement("select 1");
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					retval = true;
				}
				rs.close();
				ps.close();
			}
			catch (Exception e) {
				//eat it
			}
		}
		return retval;
	}	
}

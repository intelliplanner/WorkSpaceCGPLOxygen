package com.ipssi.dyn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.util.RuleProcessorGateway;
import com.ipssi.tracker.common.util.TripProcessorGateway;

public class RedoNonQ extends Thread {
	private ArrayList<Integer> vehicleIds = null;
	private static final int numQ = 11;
	private static final int numMaxMsgPermissible = 100;
	private static final int numMaxMsgPerQToSend = 3;
	private int currIndex[] = new int[numQ];
	private String serverName = null;
	private int actionCode = Misc.getUndefInt();
	public RedoNonQ(ArrayList<Integer> vehicleIds, int actionCode) {
		this.actionCode = actionCode;
		this.vehicleIds = vehicleIds;
		this.serverName = Misc.getServerName();
	}
	
	public void run() {
		process();
	}
	public void process() {
		Connection jmsConn = null;
		try {
			if (vehicleIds == null || vehicleIds.size() == 0)
				return;
			String prefix = "[LOC_GEN_REDO_THREAD] "+actionCode +" Thread id:"+Thread.currentThread().getId()+" :";
			System.out.println(prefix+"Controller started");
			boolean done = false;
			int totVehProcessed = 0;
			int totVehToProcess = vehicleIds.size(); 
			
			for (int i=0,is = vehicleIds.size();i<is;i++) {
				try {
					int vehicleId = vehicleIds.get(i);
					System.out.println(prefix+"Stating processing:("+totVehProcessed+","+totVehToProcess+"):"+vehicleId);
					if (actionCode == 1) {
						//put addnl action here
						NewVehicleData.updateAllDistCalc(vehicleId, (new java.util.Date(101,0,1)).getTime(), 0);
					}
					totVehProcessed++;
					System.out.println(prefix+"Completed processing:("+totVehProcessed+","+totVehToProcess+"):"+vehicleId);
					
					//now sleep untill all messages are nearly processed
			
					if (jmsConn == null || !testConn(jmsConn)) {
						Misc.closeJMS_DB_CONN();
						jmsConn = Misc.getJMS_DB_CONN();
						if (jmsConn == null || !testConn(jmsConn)) {
							Misc.closeJMS_DB_CONN();
							jmsConn = null;
							System.out.println(prefix+"Controller exiting because error in JMS conn");
							break;
						}
					}
					boolean exitOutOfOuterLoop = false;
					while (true) {
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
							long sleepDurMilli = currMessageCount*60;//1000 messages per min
							if (sleepDurMilli < 10000)
								sleepDurMilli = 10000;
							try {
								Thread.sleep(sleepDurMilli);
							}
							catch (Exception e) {
								
							}
						}
					}
					if (exitOutOfOuterLoop)
						break;
				}
				catch (Exception e1) {
					e1.printStackTrace();
					//	eat it
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			if (jmsConn != null) {
				Misc.closeJMS_DB_CONN();
			}
		}
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

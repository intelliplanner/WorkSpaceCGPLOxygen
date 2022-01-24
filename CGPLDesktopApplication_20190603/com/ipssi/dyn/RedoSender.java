package com.ipssi.dyn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.util.RuleProcessorGateway;
import com.ipssi.tracker.common.util.TripProcessorGateway;

public class RedoSender extends Thread {
	private ArrayList<ArrayList<Integer>> vehicleIds = new ArrayList<ArrayList<Integer>>();//1st is number of Q, second is vehicled for the Q
	private static final int numQ = 11;
	private static final int numMaxMsgPermissible = 100;
	private static final int numMaxMsgPerQToSend = 3;
	private int currIndex[] = new int[numQ];
	private ArrayList<ArrayList<Integer>> dataHolderForSend = new ArrayList<ArrayList<Integer>>();//1st is number of Q, 2nd is single entry already init
	private String serverName = null;
	private boolean sendToRP = false;
	public RedoSender(ArrayList<Integer> vehicleIds, boolean sendToRP) {
		this.sendToRP = sendToRP;
		this.serverName = Misc.getServerName();
		for (int i=0;i<numQ;i++) {
			currIndex[i] = 0;
			ArrayList<Integer> entry = new ArrayList(numMaxMsgPerQToSend);
			dataHolderForSend.add(entry);
			ArrayList<Integer> temp = new ArrayList();
			this.vehicleIds.add(temp);
		}
		for (int i=0,is = vehicleIds == null ? 0 : vehicleIds.size();i<is;i++) {
			int v = vehicleIds.get(i);
			int idx = v % numQ;
			this.vehicleIds.get(idx).add(v);
		}
	}
	
	public void run() {
		process();
	}
	public void process() {
		Connection jmsConn = null;
		try {
			String prefix = "[LOCRPTP] "+sendToRP +" Thread id:"+Thread.currentThread().getId()+" :";
			System.out.println(prefix+"Controller started");
			boolean done = false;
			int totVehProcessed = 0;
			int totVehToProcess = 0;
			for (int i=0,is=vehicleIds.size();i<is;i++)
				totVehToProcess += vehicleIds.get(i).size();
			while (!done) {
				try {
					
					int currMax = 0;
					int totVeh = 0;
					for (int i=0,is = vehicleIds.size();i<is;i++) {
						ArrayList<Integer> veh = vehicleIds.get(i);
						int idx = currIndex[i]; 
						ArrayList<Integer> toSend = this.dataHolderForSend.get(i);
						toSend.clear();
						for (int j=idx,js = veh.size(), jl = idx+numMaxMsgPerQToSend;j<js && j<jl;j++) {
							toSend.add(veh.get(j));
						}
						int sz = toSend.size();
						totVeh += sz;
						currIndex[i] += sz;
						if (currMax < sz)
							currMax = sz;
					}
					if (totVeh == 0) {
						done = true;
						System.out.println(prefix+"Completed Processing:("+totVehProcessed+","+totVehToProcess+")");
						break;
					}
					else {
						for (int i=0,is = vehicleIds.size();i<is;i++) {
							ArrayList<Integer> toSend = this.dataHolderForSend.get(i);
							if (toSend.size() > 0) {
								if (this.sendToRP) {
									System.out.println(prefix+"sending rule redo:("+i+","+toSend.size()+","+totVehProcessed+","+totVehToProcess+")"+toSend);
									RuleProcessorGateway.redo(toSend, serverName);
								}
								else {
									System.out.println(prefix+"sending trip redo:("+i+","+toSend.size()+","+totVehProcessed+","+totVehToProcess+")"+toSend);
									TripProcessorGateway.redo(toSend, serverName);
								}
							}
						}
						//sleep giving 10s for each vehicle
						try {
							Thread.sleep(7*RedoSender.numMaxMsgPerQToSend*1000);
						}
						catch (Exception e11) {
							
						}
					}
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
							Thread.sleep(sleepDurMilli);
						}
					}
					if (exitOutOfOuterLoop)
						break;
					totVehProcessed += totVeh;
				}
				catch (Exception e1) {
					e1.printStackTrace();
					//eat it
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

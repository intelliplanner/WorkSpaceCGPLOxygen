package com.ipssi.rfid.ui.secl.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.ConnectionStatusI;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralType;
import com.ipssi.rfid.readers.RFIDMaster;
import com.ipssi.rfid.ui.secl.controller.PropertyManager;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;


public class NodeDBConnectionChecker implements Runnable{
	private String nodeName = "remote";
	private long freq = 1000;
	private long minNotifyThreshold = 5*60*1000;
	private long lastSuccess = Misc.getUndefInt();
	private ConnectionStatusI connectionStatusHandler;
	public NodeDBConnectionChecker(String nodeName,long freq, long minNotifyThreshold,ConnectionStatusI connectionStatusHandler){
		this.nodeName = nodeName;
		this.freq = freq;
		this.minNotifyThreshold = minNotifyThreshold;
		this.connectionStatusHandler = connectionStatusHandler;
	}
	@Override
	public void run() {
		while(true){
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			boolean connected = false;
			try{
				conn = getDBConnection(nodeName);
				if(conn != null){
					ps = conn.prepareStatement("select 1");
					rs = ps.executeQuery();
					if(rs.next()){
						if(Misc.isUndef(lastSuccess)){
							lastSuccess = System.currentTimeMillis();
						}
						/*else{
							if((System.currentTimeMillis() - lastSuccess) > minNotifyThreshold){
								//notify
								if(connectionStatusHandler != null)
									connectionStatusHandler.setConnectionStatus(Misc.getUndefInt(), PeripheralType.DATABASE, PeripheralStatus.CONNECTED);
							}
						}*/
						connected = true;
					}
					Misc.closeRS(rs);
					Misc.closePS(ps);
					conn.close();
				}
			}catch(Exception ex){
				lastSuccess = Misc.getUndefInt();
				System.out.println("unable to connect to server");
				//ex.printStackTrace();
			}finally{
				if(connectionStatusHandler != null)
					connectionStatusHandler.setConnectionStatus(Misc.getUndefInt(), PeripheralType.DATABASE, connected ? PeripheralStatus.CONNECTED : PeripheralStatus.DISCONNECTED);
				try {
					Thread.sleep(freq);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// TODO Auto-generated method stub
	}
	Thread mThread = null;
	Object obj = new Object();
	public void start() {
		stop();
		mThread = new Thread(this);
		mThread.setName("Monitor DB Connection");
		mThread.start();
	}

	public void stop() {
		synchronized (obj) {
			try {
				if (mThread != null) {
					mThread.interrupt();
					mThread = null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static Connection getDBConnection(String nodeName) throws SQLException {
		// MSSQL
		Properties prop = PropertyManager.getProperty(PropertyType.Database);
		if(prop == null)
			return null;
		String server = prop.getProperty(nodeName+".DBConn.host");
		int port = Misc.getParamAsInt(prop.getProperty(nodeName+".DBConn.port"));
		String database = prop.getProperty(nodeName+".DBConn.Database");
		String user = prop.getProperty(nodeName+".DBConn.userName");
		String pass = prop.getProperty(nodeName+".DBConn.password");
		System.out.println("instance:"+nodeName+","+server+","+port+","+database+","+user);
		if(Utils.isNull(server) || Utils.isNull(port))
			return null;
		boolean isReachable = RFIDMaster.ishostAvailable(server, port);
		if(!isReachable)
			return null;
		String connectString = "jdbc:mysql://" + server + ":" + port + "/"
				+ database +"?zeroDateTimeBehavior=convertToNull&"
				;
		Connection retval = ((DriverManager.getConnection(connectString, user, pass)));
		retval.setAutoCommit(false);
		return retval;
	}
	public static void returnConnection(Connection conn, boolean destroyIt){
		if(conn != null){
			try{
				if(destroyIt)
					conn.rollback();
				else
					conn.commit();
				conn.close();
			}catch(Exception ex){
				
			}
		}
	}
}

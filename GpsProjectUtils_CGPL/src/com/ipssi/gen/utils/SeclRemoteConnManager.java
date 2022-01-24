package com.ipssi.gen.utils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.exception.GenericException;


public class SeclRemoteConnManager {
	
	public static class Station {
		private String code;
		private String ip;
		private String port;
		private String db;
		private String user;
		private String password;
		private int sameAsMeMachine = -1;//not initialized
		private String altIP = null;
		private ArrayList<String> otherIPsIfLocal = null;
		
		public void setAltIpsIfLocal() {
			try {
				boolean knownLocal = "127.0.0.1".equals(ip) || "localhost".equals(ip);
				if (knownLocal) {
					Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();
					while(network.hasMoreElements()) {
						Enumeration<InetAddress> inetAddresses = network.nextElement().getInetAddresses();
						while (inetAddresses.hasMoreElements()) {
							InetAddress inetAddress = inetAddresses.nextElement();
							String meIp = inetAddress.getHostAddress();
							if (meIp == null || meIp.equals(ip) || meIp.equals(altIP))
								continue;
							if (!meIp.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+"))
								continue;
							if (otherIPsIfLocal == null)
								otherIPsIfLocal = new ArrayList<String>();
							otherIPsIfLocal.add(meIp);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		public boolean isSameAsMeMachine(Connection conn) throws SocketException, SQLException, MalformedURLException {
			if (sameAsMeMachine != -1)
				return sameAsMeMachine > 0;
			sameAsMeMachine = 0;
			Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();
			while(network.hasMoreElements()) {
				Enumeration<InetAddress> inetAddresses = network.nextElement().getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					String meIp = inetAddress.getHostAddress();
					if (meIp == null || !(meIp.equals(ip) || meIp.equals(altIP)))
						continue;
					String connString =conn.getMetaData().getURL();
					connString = connString.substring("jdbc:mysql://".length());
					connString = connString.substring(connString.indexOf(":")+1);
					int p1 = connString.indexOf("/");
					String dbPort = connString.substring(0, p1);
					int p2 = connString.indexOf("?");
					String dbName = connString.substring(p1+1, p2 < 0 ? connString.length() : p2);
					if (dbName != null && dbName.equals(db) && dbPort != null && dbPort.equals(port)) {
						sameAsMeMachine = 1;
						break;
					}
				}
				if (sameAsMeMachine == 1)
					break;
			}
			return sameAsMeMachine > 0;
		}
		public String getCode() {
			return code == null ? "__srv" : code;
		}
		public void setCode(String code) {
			if (code == null)
				code = "__srv";
			this.code = code;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
			if (ip != null)
				ip = ip.trim().toLowerCase();
			if (ip != null && ip.equals("127.0.0.1"))
				this.altIP = "localhost";
			else if (ip != null && ip.equals("localhost"))
			   altIP = "127.0.0.1";
			 else
				   altIP = null;
			setAltIpsIfLocal();
		}
		public String getPort() {
			return port;
		}
		public void setPort(String port) {
			this.port = port;
		}
		public String getDb() {
			return db;
		}
		public void setDb(String db) {
			this.db = db;
		}
		public String getUser() {
			return user;
		}
		public void setUser(String user) {
			this.user = user;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public Station(String code, String ip, String port, String db,
				String user, String password) {
			super();
			if (code == null)
				code = "__srv";
			this.code = code;
			this.ip = ip;
			this.port = port;
			this.db = db;
			this.user = user;
			this.password = password;
			if (ip != null)
				ip = ip.trim().toLowerCase();
			if (ip != null && ip.equals("127.0.0.1"))
				this.altIP = "localhost";
			else if (ip != null && ip.equals("localhost"))
			   altIP = "127.0.0.1";
			 else
				   altIP = null;
			setAltIpsIfLocal();
		}
		public Connection getConnection() {
			String unmangledPassword = DBConnectionPool.unmangleString(password, Misc.g_doPasswordMangling);
			String connectString = "jdbc:mysql://" + ip + ":" + port + "/"
			//		+ connProps.getProperty("DBConn.Database", "ipssi")+"?zeroDateTimeBehavior=convertToNull&";// ";databaseName="
			+db +"?zeroDateTimeBehavior=convertToNull"
			;
			Connection retval = null;
			try {
				retval = ((DriverManager.getConnection(connectString, user, unmangledPassword)));
				retval.setAutoCommit(false);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			return retval;
		}
		public String getAltIP() {
			return altIP;
		}
		public void setAltIP(String altIP) {
			this.altIP = altIP;
			setAltIpsIfLocal();
		}
	}
	
	
	public static ArrayList<Station> getImmDescendentExclMe(Connection conn, Station station) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ArrayList<Station> retval = new ArrayList<Station>();
			StringBuilder sb = new StringBuilder("select sw.id, sw.name, sw.code, sp.ip, sp.port, sp.db, sp.user_id, sp.password from secl_workstation_details sw join secl_ip_details sp on (sw.uid=sp.mac_id) where sw.status=1 and sp.status=1 and (sp.server_ip=? or sp.server_ip=? ");
			if (station.otherIPsIfLocal != null && station.otherIPsIfLocal.size() > 0) {
				
				for (int i=0,is=station.otherIPsIfLocal.size();i<is;i++) {
					sb.append(" or sp.server_ip = '").append(station.otherIPsIfLocal.get(i)).append("' ");
				}
			}
			sb.append(") and sp.server_port=? and sp.server_db=? order by sw.id, sp.updated_on desc");
			ps = conn.prepareStatement(sb.toString());
			ps.setString(1, station.getIp());
			ps.setString(2, station.getAltIP());
			ps.setString(3, station.getPort());
			ps.setString(4, station.getDb());
			rs = ps.executeQuery();
			String prevCode=  null;
			while (rs.next()) {
				Station stn = new Station(rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),rs.getString(7), rs.getString(8));
				if (stn.getCode() != null && stn.getCode().equals(prevCode))
					continue;
				prevCode = stn.getCode();
				retval.add(stn);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	
	public static String getMyCode(Connection conn) throws Exception {
	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select code from secl_workstation_details where id=?");
			ps.setInt(1,Misc.getRecordSrcId(conn));
			rs = ps.executeQuery();
			String retval = null;
			if (rs.next()) {
				retval = rs.getString(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (retval == null)
				retval = "__srv";
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	public static ArrayList<Station> getStationAndParentInfo(Connection conn, String wbCode) throws Exception {
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		//1st get the IP and then 
		ArrayList<Station> retval = new ArrayList<Station>();
		try {
			ps = conn.prepareStatement("select code, ip, port, db, user_id, password, server_ip, server_port, server_db, server_user_id, server_password from secl_workstation_details join secl_ip_details on (secl_workstation_details.uid = secl_ip_details.mac_id and secl_workstation_details.status=1 and secl_ip_details.status=1) where secl_workstation_details.code=?  order by secl_workstation_details.id limit 1");
			ps2 = conn.prepareStatement("select code from secl_workstation_details join secl_ip_details on (secl_workstation_details.uid = secl_ip_details.mac_id and secl_workstation_details.status=1 and secl_ip_details.status=1) where (secl_ip_details.ip=? or secl_ip_details.ip=?)  and secl_ip_details.port=? and secl_ip_details.db=? order by secl_ip_details.id desc limit 1");
			String code = wbCode;
			String ip = null;
			String port = null;
			String db = null;
			String user = null;
			String password = null;
			String serverIp = null;
			String serverPort = null;
			String serverDB = null;
			String serverUser = null;
			String serverPassword = null;
			
			do {
				try {
					ps.setString(1, code);
					rs = ps.executeQuery();
				}
				catch (Exception e) {
					e.printStackTrace();
					rs = Misc.closeRS(rs);
					ps = Misc.closePS(ps);
					ps2 = Misc.closePS(ps2);
					ps = conn.prepareStatement("select code, ip, port, db, user_id, password, null as server_ip, null as server_port, null as server_db, null as server_user_id, null as server_password from secl_workstation_details join secl_ip_details on (secl_workstation_details.uid = secl_ip_details.mac_id and secl_workstation_details.status=1 and secl_ip_details.status=1) where secl_workstation_details.code=?  order by secl_workstation_details.id limit 1");
					ps.setString(1, code);
					rs = ps.executeQuery();
				}
				//hack in case server cols not yet added
				if (rs.next()) {
					ip = rs.getString(2);
					port = rs.getString(3);
					db = rs.getString(4);
					user = rs.getString(5);
					password = rs.getString(6);
					serverIp = rs.getString(7);
					serverPort = rs.getString(8);
					serverDB = rs.getString(9);
					serverUser = rs.getString(10);
					serverPassword = rs.getString(11);
					Station station = new Station(code, ip,port,db,user,password);
					retval.add(station);
					code = null;
					//check if server already seen so as to prevent loop
					for (int i1=0,i1s=retval.size();i1<i1s;i1++) {
						if (retval.get(i1).getIp().equals(serverIp) && retval.get(i1).getPort().equals(serverPort) && retval.get(i1).getDb().equals(serverDB)) {
							serverIp = null;
							break;
						}
					}
					if (serverIp != null && ps2 != null) {
						ps2.setString(1, serverIp);
						String altServerIp = null;
						if ("localhost".equals(serverIp.toLowerCase()))
							altServerIp = "127.0.0.1";
						else if ("127.0.0.1".equals(serverIp.toLowerCase()))
							altServerIp = "localhost";
						ps2.setString(2, altServerIp);
						ps2.setString(3, serverPort);
						ps2.setString(4, serverDB);
						rs2 = ps2.executeQuery();
						if (rs2.next()) {
							code = rs2.getString(1);
						}
						rs2 = Misc.closeRS(rs2);
					}
				}
				else {
					code = null;
				}
				rs = Misc.closeRS(rs);
				if (code == null && serverIp != null) {
					Station server = new Station(null,serverIp, serverPort, serverDB, serverUser, serverPassword);
					retval.add(server);
				}
			}
			while (code != null);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			rs2 = Misc.closeRS(rs2);
			ps = Misc.closePS(ps);
			ps2 = Misc.closePS(ps2);
		}
		if (retval.size() == 0) {//possibly doing at server
			String connString =conn.getMetaData().getURL();
			connString = connString.substring("jdbc:mysql://".length());
			int p0 = connString.indexOf(":");
			String server = connString.substring(0,p0);
			connString = connString.substring(p0+1);
			int p1 = connString.indexOf("/");
			String dbPort = connString.substring(0, p1);
			int p2 = connString.indexOf("?");
			String dbName = connString.substring(p1+1, p2 < 0 ? connString.length() : p2);
			DBConnectionPool dbConnPool = null;
			dbConnPool = DBConnectionPool.getDBConnectionPool();
			String user = dbConnPool.getUserName();
			String password = dbConnPool.getPassword();
			Station src = new Station(null, server, dbPort, dbName, user, password);
			retval.add(src);
		}
		return retval;
	}
	
	public static ArrayList<Station> getStationAndParentInfo(Connection conn, ArrayList<String> wbCodes) throws Exception {
		ArrayList<Station> retval = null;
		for (int i=0,is=wbCodes.size();i<is;i++) {
			if (i == 0)
				retval = getStationAndParentInfo(conn, wbCodes.get(i));
			else {
				ArrayList<Station> workList = getStationAndParentInfo(conn, wbCodes.get(i));
				for (int j=0,js=workList.size(); j<js;j++) {
					Station addThis = workList.get(j);
					String addThisCode = addThis.getCode();
					boolean added = false;
					for (int k=0,ks=retval.size(); k<ks;k++) {
						String currCode = retval.get(k).getCode();
						if ((currCode == null && addThisCode == null) || (currCode != null && currCode.equals(addThisCode))) {
							added = true;
							break;
						}
					}
					if (!added) {
						retval.add(addThis);
					}
				}//for each station in second list
			}//if multiple wb
		}//for each wbCode asked
		return retval;
	}
	
	public static class StationTree {
		public StationTree parent;
		public Station me;
		public ArrayList<StationTree> children;
		public ArrayList<String> wbReachable;
		public int locTPRId = Misc.getUndefInt();
		public StationTree(StationTree parent, Station me, ArrayList<StationTree> children) {
			this.parent = parent;
			this.me = me;
			this.children = children;
		}
		public  void addChild(StationTree child) {
			boolean found = false;
			if (child.parent != null &&! child.parent.me.getCode().equals(this.me.getCode()))
				child.parent.removeChild(child);
			child.parent = this;
			if (children == null)
				children = new ArrayList<StationTree>();
			children.add(child);
		}

		public  void removeChild(StationTree child) {
			for (int i=0,is=children == null ? 0 : children.size(); i<is; i++) {
				if (children.get(i).me.getCode().equals(child.me.getCode())) {
					children.get(i).parent = null;
					children.remove(i);
					break;
				}
			}
		}
	}

	public static StationTree getRelevantPartOfTree(Connection conn, Station me, ArrayList<String> wbCodes) throws Exception {
		HashMap<String, StationTree> nodes = new HashMap<String, StationTree>();
		StationTree root = getTreeAtMe(me, conn, nodes);
		weedOutNodesNotReachingWB(root, wbCodes);
		return root;
	}
	
	private static void weedOutNodesNotReachingWB(StationTree root, ArrayList<String> wbCodes) {
		boolean alreadyAdded = false;
		String wb = root.me.getCode();
		if (wb == null)
			return;
		for (int k=0,ks=root.wbReachable == null ? 0 : root.wbReachable.size(); k<ks;k++) {
			if (root.wbReachable.get(k).equals(wb)) {
				alreadyAdded = true;
				break;
			}
		}
		if (!alreadyAdded) {
			boolean isOfUse = wbCodes == null;
			if (!isOfUse) {//check specific
				for (int i=0,is=wbCodes.size(); i<is; i++) {
					if (wb.equals(wbCodes.get(i))) {
						isOfUse = true;
						break;
					}
				}
			}
			if (isOfUse) {
				if (root.wbReachable == null)
					root.wbReachable = new ArrayList<String>();
				root.wbReachable.add(wb);
			}
		}
		for (int i=root.children == null ? -1 : root.children.size()-1; i >= 0; i--) {//reverse because we may remove a child 
			weedOutNodesNotReachingWB(root.children.get(i), wbCodes);
			ArrayList<String> childWB = root.children.get(i).wbReachable;
			for (int j=0,js=childWB == null ? 0 : childWB.size(); j<js; j++) {
				alreadyAdded = false;
				wb = childWB.get(j);
				for (int k=0,ks=root.wbReachable == null ? 0 : root.wbReachable.size(); k<ks;k++) {
					if (root.wbReachable.get(k).equals(wb)) {
						alreadyAdded = true;
						break;
					}
				}
				if (!alreadyAdded) {
					if (root.wbReachable == null)
						root.wbReachable = new ArrayList<String>();
					root.wbReachable.add(wb);
				}
			}//for each WB child of root
			if (root.children.get(i).wbReachable == null || root.children.get(i).wbReachable.size() == 0) {
				root.children.get(i).parent = null;
				root.children.remove(i);
			}
		}//for each child
		
		
	}
	private static StationTree getTreeAtMe (Station me, Connection conn, HashMap<String, StationTree> nodes) throws Exception {
		StationTree root = nodes.get(me.getCode());
		if (root != null)
			return null;
		root = new StationTree(null, me, null);
		nodes.put(me.getCode(), root);
		ArrayList<Station> immDesc = SeclRemoteConnManager.getImmDescendentExclMe(conn, me);
		for (int i=0,is=immDesc.size(); i<is;i++) {
			StationTree child = getTreeAtMe(immDesc.get(i), conn, nodes);
			if (child != null) {
				root.addChild(child);
			}
		}
		return root;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		ArrayList<Station> stations = SeclRemoteConnManager.getStationAndParentInfo(conn,"GEV_TEST_WBX");
		stations = SeclRemoteConnManager.getStationAndParentInfo(conn,"_srv");
		stations = getImmDescendentExclMe(conn, stations.get(0));
		Connection remoteConn = stations.get(0).getConnection();
		boolean test1 = stations.get(0).isSameAsMeMachine(conn);
		boolean test2 = stations.get(1).isSameAsMeMachine(conn);
		int dbg = 1;
		
		
	}
}

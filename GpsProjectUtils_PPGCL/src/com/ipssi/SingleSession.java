package com.ipssi;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class SingleSession {
	
	public volatile static int g_todoSingleSignon = -1; //-1 => not init, 0 => none, 1 => web only, 2=> destkop only, 3 => both separate, 4 => either Default is 3 (both separate)
	public static final int g_singleSignonControlId = -1;
	
	private int userId = Misc.getUndefInt();
	private java.sql.Timestamp webStart = null;
	private java.sql.Timestamp webEnd = null;
	private java.sql.Timestamp desktopStart = null;
	private java.sql.Timestamp desktopEnd = null;
	private String webIP = null;
	private String webMAC = null;
	private String webOther = null;
	private String webSessionId = null;
	private String desktopIP = null;
	private String desktopMAC = null;
	private String desktopCode = null;
	private String desktopOther = null;
	private String desktopSessionId = null;
	public static String createSessionId() {
		return Integer.toString(((int)(Math.random()*100000)));
	}
	public static String getMachineIP() {
		String res = null;
	    try {
	        String localhost = InetAddress.getLocalHost().getHostAddress();
	        
	        System.out.println("LocalIpAddress : "+localhost);
	        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
	        while (e.hasMoreElements()) {
	            NetworkInterface ni = (NetworkInterface) e.nextElement();
	            if(ni.isLoopback())
	                continue;
	            if(ni.isPointToPoint())
	                continue;
	            Enumeration<InetAddress> addresses = ni.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress address = (InetAddress) addresses.nextElement();
	                if(address instanceof Inet4Address) {
	                    String ip = address.getHostAddress();
	                    if(!ip.equals(localhost)) {
	                    	res = ip;
	                        System.out.println("PublicIpAddress : "+(res));
	                    }
	                }
	            }
	        }
	        if (res == null)
	        	res = localhost;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return res;	
	}
	public static String getRequestIP(HttpServletRequest request) {
		return request == null ? null : request.getRemoteHost();	
	}
	public static void initSingleSessionDB(Connection conn, int control)  {
		if (!Misc.g_doSingleSession) {
			SingleSession.g_todoSingleSignon = 0;
			return;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select int_val1 from generic_params where param_id=? and status=1");
			ps.setInt(1, g_singleSignonControlId);
			rs = ps.executeQuery();
			boolean found = false;
			if (rs.next()) {
				found = true;
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
			if (!found) {
				ps = conn.prepareStatement("insert into generic_params(int_val1, param_id, port_node_id, status) values(?,?,2,1)");
			}
			else {
				ps = conn.prepareStatement("update generic_params set int_val1=? where param_id=?");
			}
			ps.setInt(1, control);
			ps.setInt(2, g_singleSignonControlId);
			ps.executeUpdate();
			ps = Misc.closePS(ps);
			
			//now set up the table
			ps = conn.prepareStatement("create table user_session_ext(user_id int, web_start timestamp null default null, web_end timestamp null default null, web_ip varchar(32), web_mac varchar(32), web_other varchar(32), web_session_id varchar(32), desktop_start timestamp null default null, desktop_end timestamp null default null, desktop_ip varchar(32), desktop_mac varchar(32), desktop_code varchar(32), desktop_other varchar(32), desktop_session_id varchar(32), primary key(user_id))");
			ps.executeUpdate();
			ps = Misc.closePS(ps);
		}		
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			g_todoSingleSignon = control;
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	public static int getTodoSingleSignon(Connection conn, boolean reload) {
		if (!Misc.g_doSingleSession) {
			return 0;
		}
		if (reload)
			g_todoSingleSignon = -1;
		int retval = 0;
		if (g_todoSingleSignon < 0) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = conn.prepareStatement("select int_val1 from generic_params where param_id=? and status=1");
				ps.setInt(1, g_singleSignonControlId);
				rs = ps.executeQuery();
				if (rs.next()) {
					retval = rs.getInt(1);
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			finally {
				g_todoSingleSignon = retval;
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
			
		}
		return g_todoSingleSignon;
	}
	
	public static String getCurrSessionId(Connection conn, int userId, int srcType) {
		if (!Misc.g_doSingleSession) {
			return "0";
		}
		SingleSession currSession = SingleSession.getCurrSession(conn, userId);
		String currSessionId = null;
		if (currSession != null) {
			if (srcType == 0 && currSession.getWebStart() != null && currSession.getWebEnd() == null)
				currSessionId = currSession.getWebSessionId();
			else if (srcType == 1 && currSession.getDesktopStart() != null && currSession.getDesktopEnd() == null)
				currSessionId = currSession.getDesktopSessionId();
		}
		return currSessionId;
	}
	public static Triple<Boolean, String, String> checkAndUpdateSingleSignonResult(Connection conn, int userId, int srcType, String ip, String mac, String code, String sessionId) {
		//srcType = 0 => from web, srcType = 1 => desktop
		if (!Misc.g_doSingleSession) {
			return new Triple<Boolean, String, String>(true,null,null);
		}
			
		boolean allow = true;
		String reason = null;
		String currSessionId = null;
		
		int policy = getTodoSingleSignon(conn, false);
		if (policy > 0) {
			SingleSession currSession = SingleSession.getCurrSession(conn, userId);
			if (currSession != null) {
				if (srcType == 0)
					currSessionId = currSession.getWebSessionId();
				else
					currSessionId = currSession.getDesktopSessionId();
				boolean webOpen = currSession.getWebStart() != null && currSession.getWebEnd() == null;
				if (webOpen && (sessionId != null && sessionId.equals(currSession.getWebSessionId())))
						webOpen = false;
				boolean desktopOpen = currSession.getDesktopStart() != null && currSession.getDesktopEnd() == null;
				if (desktopOpen && (
						(sessionId != null && sessionId.equals(currSession.getDesktopSessionId())) 
						|| 
						(code != null && code.equals(currSession.getDesktopCode()))
						)
						)
					desktopOpen = false;

				if (policy == 1 && srcType == 0) {
					if (webOpen) {
						allow = false;
						reason = "Already logged in from "+currSession.toStringWeb();
					}
				}
				else if (policy == 2 && srcType == 1) {
					if (desktopOpen) {
						allow = false;
						reason = "Already logged in from "+currSession.toStringDesktop();
					}
				}
				else if (policy == 3) {
					if (srcType == 0 && webOpen) {
						allow = false;
						reason = "Already logged in from "+currSession.toStringWeb();
					}
					else if (srcType == 1 && desktopOpen) {
						allow = false;
						reason = "Already logged in from "+currSession.toStringDesktop();
					}
				}
				else if (policy == 4) {
					if (webOpen) {
						allow = false;
						reason = "Already logged in from Web "+currSession.toStringWeb();
					}
					else if (desktopOpen) {
						allow = false;
						reason = "Already logged in from Desktop "+currSession.toStringDesktop();
					}
				}
			}//currSession != null
		}//if enforce policy
		if (allow) {
			if (srcType == 0) {
				currSessionId = SingleSession.openCurrWebSession(conn, userId, ip, mac);
			}
			else {
				currSessionId = SingleSession.openCurrDesktopSession(conn, userId, ip, mac, code);
			}
		}
		return new Triple<Boolean, String,String>(allow, reason,currSessionId);
	}
	
	public String toStringWeb() {
		StringBuilder sb = new StringBuilder();
		sb.append("IP").append(this.webIP == null ? "N/A" : webIP).append("  MAC:").append(webMAC == null ? "N/A" : webMAC);
		return sb.toString();
	}
	public String toStringDesktop() {
		StringBuilder sb = new StringBuilder();
		sb.append("IP").append(this.desktopIP == null ? "N/A" : desktopIP).append("  MAC:").append(desktopMAC == null ? "N/A" : desktopMAC).append(" Code:").append(desktopCode == null ? "N/A" : desktopCode);
		return sb.toString();
	}
	//public static void forceLogout(Connection conn, int userId, int srcType) {//srcType = 0 => from web, srcType = 1 => desktop, 2=> both
	//	forceLogout(conn, userId, srcType, null);
	//}
	public static void forceLogout(Connection conn, int userId, int srcType, String sessionIdOrCode) {//srcType = 0 => from web, srcType = 1 => desktop, 2=> both
		if (!Misc.g_doSingleSession)
			return;
		if (srcType == 0 || srcType == 2)
			SingleSession.closeCurrWebSession(conn, userId, sessionIdOrCode);
		if (srcType == 1 || srcType == 2)
			SingleSession.closeCurrDesktopSession(conn, userId, sessionIdOrCode);
		try {
		if (!conn.getAutoCommit())
			conn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String GET_SINGLE_SESSION_INFO = "select user_id, web_start, web_end, web_ip, web_mac, web_other,web_session_id, desktop_start, desktop_end, desktop_ip, desktop_mac, desktop_code, desktop_other, desktop_session_id from user_session_ext where user_id=?";
	private static String INSERT_SINGLE_SESSION_INFO_WEB_OPEN = "insert into user_session_ext(web_start, web_end, web_ip, web_mac, web_other, web_session_id,user_id) values (now(),null,?,?,?,?,?)";
	private static String UPDATE_SINGLE_SESSION_INFO_WEB_OPEN = "update user_session_ext set web_start=now(), web_end=null, web_ip=?, web_mac=?, web_other=?, web_session_id=? where user_id=?";
	private static String INSERT_SINGLE_SESSION_INFO_DESKTOP_OPEN = "insert into user_session_ext(desktop_start, desktop_end, desktop_ip, desktop_mac, desktop_code, desktop_other, desktop_session_id, user_id) values (now(),null,?,?,?,?,?)";
	private static String UPDATE_SINGLE_SESSION_INFO_DESKTOP_OPEN = "update user_session_ext set desktop_start=now(), desktop_end=null, desktop_ip=?, desktop_mac=?, desktop_code=?, desktop_other=?, desktop_session_id=? where user_id=?";
	private static String UPDATE_SINGLE_SESSION_INFO_WEB_CLOSE = "update user_session_ext set web_end=now() where user_id=? and (? is null or web_session_id=?)";
	private static String UPDATE_SINGLE_SESSION_INFO_DESKTOP_CLOSE = "update user_session_ext set desktop_end=now() where user_id=?  and (? is null or desktop_code=? or desktop_session_id=?)";
	private static String CHECK_RECORD_EXIST = "select 1 from user_session_ext where user_id=?";
	
	private static  String openCurrWebSession(Connection conn, int userId, String ip, String mac) {
		String other = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(CHECK_RECORD_EXIST);
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			boolean exist = rs.next();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement(exist ? UPDATE_SINGLE_SESSION_INFO_WEB_OPEN : INSERT_SINGLE_SESSION_INFO_WEB_OPEN);
			int colIndex = 1;
			ps.setString(colIndex++,ip);
			ps.setString(colIndex++,mac);
			ps.setString(colIndex++,other);
			String sessionId = createSessionId();
			ps.setString(colIndex++, sessionId);
			ps.setInt(colIndex++,userId);
			ps.execute();
			ps = Misc.closePS(ps);
			return sessionId;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
			return null;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	private static  void closeCurrWebSession(Connection conn, int userId, String sessionIdOrCode) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement(UPDATE_SINGLE_SESSION_INFO_WEB_CLOSE);
			ps.setInt(1, userId);
			ps.setString(2, sessionIdOrCode);
			ps.setString(3, sessionIdOrCode);
			ps.execute();
			ps = Misc.closePS(ps);
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
	
	
	
	private static  String openCurrDesktopSession(Connection conn, int userId, String ip, String mac, String code) {
		String other = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(CHECK_RECORD_EXIST);
			ps.setInt(1, userId);
			boolean exist = rs.next();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement(exist ? UPDATE_SINGLE_SESSION_INFO_DESKTOP_OPEN : INSERT_SINGLE_SESSION_INFO_DESKTOP_OPEN);
			int colIndex = 1;
			ps.setString(colIndex++,ip);
			ps.setString(colIndex++,mac);
			ps.setString(colIndex++, code);
			ps.setString(colIndex++,other);
			String sessionId = createSessionId();
			ps.setString(colIndex++, sessionId);

			ps.setInt(colIndex++,userId);

			ps.execute();
			ps = Misc.closePS(ps);
			return sessionId;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
			return null;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	private static  void closeCurrDesktopSession(Connection conn, int userId, String sessionIdOrCode) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement(UPDATE_SINGLE_SESSION_INFO_DESKTOP_CLOSE);
			ps.setInt(1, userId);
			ps.setString(2, sessionIdOrCode);
			ps.setString(3, sessionIdOrCode);
			ps.setString(4, sessionIdOrCode);
			ps.execute();
			ps = Misc.closePS(ps);
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
	private static SingleSession getCurrSession(Connection conn, int userId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		SingleSession retval = null;
		try {
			ps = conn.prepareStatement(GET_SINGLE_SESSION_INFO);
			ps.setInt(1, userId);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = new SingleSession(rs.getInt(1), rs.getTimestamp(2), rs.getTimestamp(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8), rs.getTimestamp(9), rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		if (retval == null)
			retval = new SingleSession(userId);
		return retval;
	}
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public java.sql.Timestamp getWebStart() {
		return webStart;
	}

	public void setWebStart(java.sql.Timestamp webStart) {
		this.webStart = webStart;
	}

	public java.sql.Timestamp getWebEnd() {
		return webEnd;
	}

	public void setWebEnd(java.sql.Timestamp webEnd) {
		this.webEnd = webEnd;
	}

	public java.sql.Timestamp getDesktopStart() {
		return desktopStart;
	}

	public void setDesktopStart(java.sql.Timestamp desktopStart) {
		this.desktopStart = desktopStart;
	}

	public java.sql.Timestamp getDesktopEnd() {
		return desktopEnd;
	}

	public void setDesktopEnd(java.sql.Timestamp desktopEnd) {
		this.desktopEnd = desktopEnd;
	}

	public String getWebIP() {
		return webIP;
	}

	public void setWebIP(String webIP) {
		this.webIP = webIP;
	}

	public String getWebMAC() {
		return webMAC;
	}

	public void setWebMAC(String webMAC) {
		this.webMAC = webMAC;
	}

	public String getWebOther() {
		return webOther;
	}

	public void setWebOther(String webOther) {
		this.webOther = webOther;
	}

	public String getDesktopIP() {
		return desktopIP;
	}

	public void setDesktopIP(String desktopIP) {
		this.desktopIP = desktopIP;
	}

	public String getDesktopMAC() {
		return desktopMAC;
	}

	public void setDesktopMAC(String desktopMAC) {
		this.desktopMAC = desktopMAC;
	}

	public String getDesktopCode() {
		return desktopCode;
	}

	public void setDesktopCode(String desktopCode) {
		this.desktopCode = desktopCode;
	}

	public String getDesktopOther() {
		return desktopOther;
	}

	public void setDesktopOther(String desktopOther) {
		this.desktopOther = desktopOther;
	}

	public SingleSession(int userId) {
		this.userId = userId;
	}
	public SingleSession(int userId, Timestamp webStart, Timestamp webEnd,
			 String webIP, String webMAC, String webOther, String webSessionId,
			 Timestamp desktopStart, Timestamp desktopEnd,
			 String desktopIP, String desktopMAC, String desktopCode, String desktopOther, String desktopSessionId) {
		super();
		this.userId = userId;
		this.webStart = webStart;
		this.webEnd = webEnd;
		this.desktopStart = desktopStart;
		this.desktopEnd = desktopEnd;
		this.webIP = webIP;
		this.webMAC = webMAC;
		this.webOther = webOther;
		this.desktopIP = desktopIP;
		this.desktopMAC = desktopMAC;
		this.desktopCode = desktopCode;
		this.desktopOther = desktopOther;
		this.webSessionId = webSessionId;
		this.desktopSessionId = desktopSessionId;
	}
	public String getWebSessionId() {
		return webSessionId;
	}
	public void setWebSessionId(String webSessionId) {
		this.webSessionId = webSessionId;
	}
	public String getDesktopSessionId() {
		return desktopSessionId;
	}
	public void setDesktopSessionId(String desktopSessionId) {
		this.desktopSessionId = desktopSessionId;
	}
}

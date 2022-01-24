package com.ipssi.dbconnection;

public class DBConnectionPool {
//	
//	public static Connection getConnectionFromPoolNonWeb() throws SQLException {
//		Connection retval = null;
//		String connectionUrl = "jdbc:sqlserver://"+TokenManager.DB_HOST+":"+TokenManager.DB_PORT+";" 
//								+ "database="+TokenManager.DB_NAME+";"
//								+ "user="+TokenManager.DB_USER_NAME+";" + "password="+TokenManager.DB_PASSWORD+";";
//		System.out.println("Connection URL:" +connectionUrl);
//		retval = DriverManager.getConnection(connectionUrl);
//		return retval;
//	}
//
//	public static void returnConnectionToPoolNonWeb(Connection conn, boolean destroyIt) {
//		try {
//            if (conn != null && !conn.isClosed()) {
//                conn.close();
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//	}
//
//	public static void returnConnectionToPoolNonWeb(Connection conn) {
//		if (conn == null)
//			return;
//		try {
//			conn.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
}

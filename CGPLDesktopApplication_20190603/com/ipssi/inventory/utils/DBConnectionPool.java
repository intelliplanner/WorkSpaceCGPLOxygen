package com.ipssi.inventory.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.ipssi.gen.utils.ConnectionTimeoutException;
import com.ipssi.gen.utils.FullQueueException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Queue;
import com.ipssi.gen.utils.TooBigQueueException;

public class DBConnectionPool {
	public static DBConnectionPool g_DBConnectionPool = null;
	public synchronized static DBConnectionPool getDBConnectionPool() throws Exception {
		if (g_DBConnectionPool == null)
			g_DBConnectionPool = new DBConnectionPool();
		return g_DBConnectionPool;
	}

	//static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DBConnectionPool.class);
	private DataSource g_DataSource = null;
	private boolean m_usingTomcat = true; // will be false once it fails to do
	// the regular stuff
	public static boolean g_dontUseTomcat = false;

	private int maxConnection = 37;
	String serverName = "inventory";//Misc.getServerName();
	//private String propertiesFile = Cache.serverConfigPath + System.getProperty("file.separator") + "conn.property";
	private String propertiesFile = Misc.CONN_PROPERTY;// + System.getProperty("file.separator") + "conn.property";
	// MSSQL
	private String connectString = Misc.G_DO_ORACLE ? "jdbc:oracle:thin:@" : "jdbc:mysql://";// "jdbc:sqlserver://";
	// //2005
	private String database = Misc.G_DO_ORACLE ? "" : "node1".equalsIgnoreCase(serverName) ?"ipssi2":"ipssi";
	
  private String userName = "root";
  private String password = "root";
	//private String userName = "jboss";
	//private String password = "redhat";
	// MSSQL

	private int retryInterval = 1000; // in ms
	private int maxRetryAttempts = 100;
	private Queue connQueue;

	private DBConnectionPool() throws SQLException {
		super();
		init(null);
		// initializePool();
	}

	private DBConnectionPool(String propertiesFile) throws SQLException {
		super();
		init(propertiesFile);
	}

	private void initializePool() {
		if (!g_dontUseTomcat) {
			try {
				InputStream inputStream = DBConnectionPool.class.getClassLoader().getResourceAsStream("DBPool.properties");
				Properties props = new Properties();
				props.load(inputStream);
				Context ctx = new InitialContext(props);
				g_DataSource = (DataSource) ctx.lookup(props.getProperty("remote.jndi"));

                //below for TomCat Only deployment of CapEx				
				//InitialContext initCtx = new InitialContext();
				//Context envCtx = (Context) initCtx.lookup("java:comp/env");
				//g_DataSource = (DataSource) envCtx.lookup("jdbc/IntelliDb");
				if (g_DataSource != null) {
					Connection retval = g_DataSource.getConnection();
					retval.setAutoCommit(false);
					if (retval != null)
						retval.close();
				}
			} catch (Exception e) {
				// e.printStackTrace();
				m_usingTomcat = false;
			}
		} else {
			m_usingTomcat = false;
		}
		if (g_DataSource == null) {
			m_usingTomcat = false;
		}

	}

	public final synchronized void init(String pFileName) throws SQLException {
		if (pFileName != null)
			propertiesFile = pFileName;

		Properties connProps = new Properties();
		try {
			connProps.load(new BufferedInputStream(new FileInputStream(propertiesFile)));
			g_dontUseTomcat = "1".equals(connProps.getProperty("DBConn.dontUseTomcat","0"));
		} catch (IOException e) { // don't do anything go with the default
		}
		Misc.setNotCalcName("1".equals(connProps.getProperty("DontCalcName.always")));//will then be dynamically reset or after RP has processed message
		Misc.setNotCalcNameForInnerPoint("1".equals(connProps.getProperty("DontCalcName.inner_point")));
		
		initializePool();
		if (m_usingTomcat)
			return;

		// MSSQL
		// This is actual connection string to get values from properties file
		if (Misc.G_DO_ORACLE) {
			connectString = connectString + connProps.getProperty("DBConn.host", "localhost") + ":" + connProps.getProperty("DBConn.port", "1521") + ":"
					+ connProps.getProperty("DBConn.sid", "ORCL");
		} else {
			String dataBase = null;
			
			
			dataBase = connProps.getProperty(serverName+".DBConn.Database","ipssi");
			
			connectString = connectString + connProps.getProperty(serverName+".DBConn.host", "127.0.0.1") + ":" + connProps.getProperty(serverName+".DBConn.port", "3306") + "/"
			//		+ connProps.getProperty("DBConn.Database", "ipssi")+"?zeroDateTimeBehavior=convertToNull&";// ";databaseName="
			+dataBase +"?zeroDateTimeBehavior=convertToNull&";
			
			
			System.out.println("Connection String = "+ connectString);
//			if("true".equalsIgnoreCase(connProps.getProperty("DBConn.Database")))
//			connectString = connectString + "?useCursorFetch=true";
			// +
			// connProps.getProperty("DBConn.Database",
			// "ipssi")
			// ;//TODO_FOR_SQL_SERVER+
			// ";responseBuffering=adaptive;";//SelectMethod=Cursor;
		}
		userName = connProps.getProperty(serverName+".DBConn.userName", userName);
		password = connProps.getProperty(serverName+".DBConn.password", password);
		System.out.println("DB User Name ="+userName);
		System.out.println("DB Password ="+password);
		// MSSQL
		maxConnection = Integer.parseInt(connProps.getProperty(serverName+".DBConn.maxConnection", Integer.toString(maxConnection)));
		
		
		maxRetryAttempts = Integer.parseInt(connProps.getProperty(serverName+".DBConn.maxRetryAttempts", Integer.toString(maxRetryAttempts)));
		retryInterval = Integer.parseInt(connProps.getProperty(serverName+".DBConn.retryInterval", Integer.toString(retryInterval)));
		connProps = null;
		// now get connections for the maxConnection

		try {
			connQueue = new Queue(maxConnection+10);
		} catch (TooBigQueueException e) {
			try {
				connQueue = new Queue();
			} catch (TooBigQueueException ne) {
				ne.printStackTrace();
			}
		}
		try {
			// MSSQL
			// DriverManager.registerDriver(new
			// com.microsoft.jdbc.sqlserver.SQLServerDriver()); //2000
			if (Misc.G_DO_ORACLE) {
				DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			} else {
				// DriverManager.registerDriver(new
				// com.microsoft.sqlserver.jdbc.SQLServerDriver()); //2005
				DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			}
			// MSSQL
			for (int i = 0; i < maxConnection; i++)
				connQueue.write((Object) getDBConnection());
		} catch (FullQueueException e) { // do nothing - won't happen
			e.printStackTrace();
		}
	}

	public Connection getConnection() throws ConnectionTimeoutException, SQLException {
		Connection retval = null;

		if (m_usingTomcat) {
			try {
				retval = g_DataSource.getConnection();
				if (!Misc.G_DO_ORACLE) {
					// TODO_SQL_SERVER
					// retval.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}// will throw exception later
		} else {
			int retryCount = 0;
			do {
				try {//DEBUG13 do with new approach
				retval = (Connection) connQueue.read();
				}
				catch (Exception e) {
					
				}
				if (retval != null) {
					// System.out.println("Found non null Connection");

					if (isValidConnection(retval)) {
						break;
					} else {
						returnConnection(retval, true);
						retval = null;
					}
				}
				retryCount++;
				try {
					Thread.sleep(retryInterval);
				} catch (Exception e) {
				}
			} while (retryCount < maxRetryAttempts);
		}

		if (retval == null)
			throw new ConnectionTimeoutException();
//		if (Logger.getGlobalLoggingLevel() >= 15) {
			//StringBuilder errMsg = new StringBuilder();
			//errMsg.append("Gave Connection ...(").append(retval.hashCode()).append(")");
			//System.err.println(errMsg);
//			if (!m_usingTomcat){
//				System.out.println(errMsg);
//			}
//		}
        retval.setAutoCommit(false);
        System.out.println("[@Getting Connection:]"+retval+" [Thread:]"+Thread.currentThread().getId()+(connQueue.debug ? getRelevantCallProc() : ""));
        return (retval);
	}

	public boolean isValidConnection(Connection conn)  {
		try {
			boolean isClosed = conn.isClosed();
			if(!isClosed){
				try{
					java.sql.Statement st = conn.createStatement();
					st.execute("select 1");
					st.close();
				}catch(Exception e){
					e.printStackTrace();
					isClosed = true;
				}
				
			}
			return (!isClosed);
		}
		catch (Exception e) {
			
		}
		return false;
	}

	public  void returnConnection(Connection retConn, boolean destroyIt)  {
		if (retConn == null)
			return;
	
		boolean isClosed = false;
		boolean toCreateNew = false;
		try {
			isClosed = retConn.isClosed();
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		//if not already closed - rollback or commit as needed
		if (!isClosed) {
			try {
				if (!retConn.getAutoCommit()) {
					if (destroyIt) {
						retConn.rollback();
						if (!Misc.G_DO_ORACLE) {
							// releaseConnResources(retConn);
						}
					}//if to destroy
					else {
						retConn.commit();
					}//if to commit
				}//was not autco
			}
			catch (Exception e) {
				destroyIt = true;
				e.printStackTrace();
			}
		}//if not already closed - rollback or commit as needed
		
		//if not to be destroyed check if there are open statements - if so still close
		if (!destroyIt) {
			try {
				if (!Misc.G_DO_ORACLE) {
					com.mysql.jdbc.Connection mysqlConn = (com.mysql.jdbc.Connection) retConn;
					if (mysqlConn.getActiveStatementCount() > 0) {
						System.out.println("DBCONN connection being returned has open statements:"+retConn.hashCode()+" cnt:"+mysqlConn.getActiveStatementCount());
//						(new Exception()).printStackTrace();
					    destroyIt = true;
					}
				}
			}
			catch (Exception e) {
				destroyIt = true;
				e.printStackTrace();
			}
		}
		
		//if not already closed then close it
		toCreateNew = isClosed;
		if (destroyIt && !isClosed) {
			try {
				retConn.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				toCreateNew = true;
			}
		}
		
		if (toCreateNew && !m_usingTomcat) {
			try {
				connQueue.removeBusy(retConn);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				connQueue.write((Object) getDBConnection());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (!m_usingTomcat) {
			try {
				connQueue.write(retConn);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("[@Return Connection:](closed, destroyit, toCreateNew)("+isClosed+","+destroyIt+","+toCreateNew+",)"+retConn
				+" [Thread:]"+Thread.currentThread().getId()+(connQueue.debug ? getRelevantCallProc() : ""));
	}

	public  void returnConnectionOld(Connection retConn, boolean destroyIt) throws SQLException {
		// will destroy the connection - assuming that the connection is not
		// valid anymore
//		if (Logger.getGlobalLoggingLevel() >= 15) {
//			if (destroyIt) {
//				int dbg = 1;
//			}
			//StringBuilder errMsg = new StringBuilder();
			//errMsg.append("... Got Connection (").append(destroyIt ? "destroy" : "normal").append(")(");
			//if (retConn == null)
			//	errMsg.append("NULL").append(")");
			//else
			//	errMsg.append(retConn.hashCode()).append(")");
			//System.err.println(errMsg);
//			if (!m_usingTomcat){
//				System.out.println(errMsg);
//			}
//		}
       
		if (destroyIt) { // some error happened so destroy it and add a new
			// Connection to the Queue			
			try {
				if(!retConn.getAutoCommit())
					retConn.rollback();
				if (!Misc.G_DO_ORACLE) {
					// releaseConnResources(retConn);
				}

				if (m_usingTomcat) {
					//System.out.println("Using tomcat");
					// without reallyClose()
					if (true) {
						retConn.close();
						retConn = null;
					} else {
						// with really close
						try {
							// This returns a
							// org.apache.commons.dbcp.PoolableConnection ... to
							// prevent compilation this commented
							// Connection pc =
							// ((org.apache.commons.dbcp.DelegatingConnection)retConn).getDelegate();
							// ((org.apache.commons.dbcp.PoolableConnection)pc).reallyClose();
							//System.out.println("Successfully really closed it");
							retConn = null;
						} catch (Exception e1) {
							e1.printStackTrace();
							retConn.close();
							retConn = null;
						}
					}
				} else {
//					System.out.println("Not Using tomcat");
					retConn.close();
					retConn = null;
					connQueue.write((Object) getDBConnection());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (retConn != null) {
					try {
						retConn.close();
					} catch (Exception e) {

					}
					retConn = null;
				}
			}
		} else { // asking for regular release
			try {
				// if (! Misc.G_DO_ORACLE)
				// releaseConnResources(retConn);
				if (!retConn.getAutoCommit())
					retConn.commit();
				
				if (m_usingTomcat) {
					retConn.close();
					retConn = null;
				} else {
					boolean towritebackToQ = true;
					if (!Misc.G_DO_ORACLE) {
						try {
							com.mysql.jdbc.Connection mysqlConn = (com.mysql.jdbc.Connection) retConn;
							if (mysqlConn.getActiveStatementCount() > 0) {
								System.out.println("DBCONN connection being returned has open statements:"+retConn.hashCode()+" cnt:"+mysqlConn.getActiveStatementCount());
//								(new Exception()).printStackTrace();
							    retConn.close();
							    towritebackToQ = false;
							    connQueue.write((Object) getDBConnection());
							}
						}
						catch (Exception e2) {
							//eat it
						}
					}
					if (towritebackToQ)
						connQueue.write((Object) retConn);
					retConn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (retConn != null) {
					try {
						retConn.close();
					} catch (Exception e) {

					}
					retConn = null;
				}
			}
		}
	}

	public  void returnConnection(Connection retConn) throws Exception {
		returnConnection(retConn, false);
	}

	private Connection getDBConnection() throws SQLException {
		// MSSQL
		Connection retval = ((DriverManager.getConnection(connectString, userName, password)));
		if (!Misc.G_DO_ORACLE) {
			// retval.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		}
		return retval;
		// return
		// DriverManager.getConnection("jdbc:microsoft:sqlserver://127.0.0.1:1433;databaseName=ipssi;",
		// "amdemo", "amdemo");
		// MSSQL

	}

	// public static void main (String args []) throws SQLException
	// {
	// DBConnectionPool dbConnPool = new DBConnectionPool();
	// Connection[] conn = new Connection[12];
	// for (int i=0;i<10;i++)
	// {
	// try {
	// conn[i] = dbConnPool.getConnection();
	// Statement stmt = conn[i].createStatement ();
	// ResultSet rset = stmt.executeQuery ("select * from DUAL");
	// // Iterate through the result and print the employee names
	// System.out.println(i);
	// while (rset.next ())
	// System.out.println (rset.getString (1));
	// //dbConnPool.returnConnection(conn[i]);

	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// for (int i=0;i<11;i++)
	// {
	// try {
	// conn[i] = dbConnPool.getConnection();
	// Statement stmt = conn[i].createStatement ();
	// ResultSet rset = stmt.executeQuery ("select * from DUAL");
	// // Iterate through the result and print the employee names
	// System.out.println(i);
	// while (rset.next ())
	// System.out.println (rset.getString (1));
	// //dbConnPool.returnConnection(conn[i]);

	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private void releaseConnResources(Connection retConn) {
		CallableStatement cstmt = null;
		try {
			String str = "{call sys.sp_reset_connection }";
			cstmt = retConn.prepareCall(str);
			cstmt.execute();
			cstmt.close();
			cstmt = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (Exception e) {

				}
				cstmt = null;
			}
		}
	}
  
	public static Connection getConnectionFromPoolNonWeb() throws Exception { //
		Connection conn = null;
		try {
			DBConnectionPool dbConnPool = null;
			dbConnPool = DBConnectionPool.getDBConnectionPool();
			conn = dbConnPool.getConnection();
			//conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			//logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new Exception(e);
		} catch (ConnectionTimeoutException e) {
			e.printStackTrace();
			//logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new Exception(e);
		} catch (Exception e) {
			e.printStackTrace();
			//logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new Exception(e);
		}
		return conn;
	}

	public static void returnConnectionToPoolNonWeb(Connection conn) throws Exception {
		returnConnectionToPoolNonWeb(conn, false);
		return;		
	}
	
	public static void returnConnectionToPoolNonWeb(Connection conn, boolean destroyIt) throws Exception {
		try {
			if (conn == null) {
				return;
			}

			DBConnectionPool dbPool = DBConnectionPool.getDBConnectionPool();
			//conn.rollback();
			//conn.setAutoCommit(true);
			dbPool.returnConnection(conn, destroyIt);
		} catch (Exception e) {
			e.printStackTrace();
		//	logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new Exception(e);
		}
	}
	public static String ignoreStck[] = {"com.ipssi.gen.utils.DBConnectionPool", "com.ipssi.gen.utils.InitHelper", "com.ipssi.gen.utils.DBConnectionPool"};
	public static String getRelevantCallProc() {
		StackTraceElement[] stckTrc = Thread.currentThread().getStackTrace();
		for (int i=0,is=stckTrc == null ? 0 :stckTrc.length; i<is;i++) {
			StackTraceElement elem = stckTrc[i];
			String className = elem.getClassName();
			if (className == null || className.startsWith("java."))
				continue;
			boolean toCont = false;
			for (int j=0,js=ignoreStck.length;j<js;j++) {
				if (className.startsWith(ignoreStck[j])) {
					toCont = true;
					break;
				}
			}
			if (toCont)
				continue;
			return " "+className+";"+elem.getLineNumber();
		}
		return "";
	}
	
	//debug code
	
	
		
}

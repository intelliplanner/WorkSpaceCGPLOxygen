package com.ipssi.rfid.ui.secl.data;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.ConnectionTimeoutException;
import com.ipssi.gen.utils.EmptyQueueException;
import com.ipssi.gen.utils.FullQueueException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Queue;
import com.ipssi.gen.utils.TooBigQueueException;
import com.ipssi.rfid.processor.Utils;

public class DbConnectionManager {
	public static DbConnectionManager g_DBConnectionPool = null;
	public static ConcurrentHashMap<String,DbConnectionManager> g_DBConnectionPoolMap = null;//for multiple instance connection
	public synchronized static DbConnectionManager getDBConnectionPool(String instance) throws Exception {
		if(!Utils.isNull(instance)){
			if(g_DBConnectionPoolMap == null)
				g_DBConnectionPoolMap = new ConcurrentHashMap<String, DbConnectionManager>();
			if(!g_DBConnectionPoolMap.containsKey(instance))
				g_DBConnectionPoolMap.put(instance, new DbConnectionManager(null,instance));
			return g_DBConnectionPoolMap.get(instance);
		}
		return null;
	}

	//static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DBConnectionPool.class);
	private DataSource g_DataSource = null;
	private boolean m_usingTomcat = true; // will be false once it fails to do
	// the regular stuff
	public static boolean g_dontUseTomcat = false;

	private int maxConnection = 37;
	String serverName = Misc.getServerName();
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

	private DbConnectionManager() throws SQLException {
		super();
		init(null);
		// initializePool();
	}
	private DbConnectionManager(String propertiesFile,String instanceName) throws SQLException {
		super();
		this.serverName = instanceName;
		init(propertiesFile);
	}

	private DbConnectionManager(String propertiesFile) throws SQLException {
		super();
		init(propertiesFile);
	}

	private void initializePool() {
		if (!g_dontUseTomcat) {
			try {
				InputStream inputStream = DbConnectionManager.class.getClassLoader().getResourceAsStream("DBPool.properties");
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
		int maxConnectionHack = Misc.getUndefInt();
		if (pFileName != null)
			propertiesFile = pFileName;
		Properties connProps = new Properties();
		//comment for server deploy
		//serverName = "desktop";//for desktop applications
		//propertiesFile = "C:\\ipssi\\properties\\new_conn.property";
		//		propertiesFile = "/home/pi/ipssi/config_server/new_conn.property";
		try {
			connProps.load(new BufferedInputStream(new FileInputStream(propertiesFile)));
			g_dontUseTomcat = "1".equals(connProps.getProperty("DBConn.dontUseTomcat","0"));
		} catch (IOException e) { // don't do anything go with the default
		}
		Misc.setNotCalcName("1".equals(connProps.getProperty("DontCalcName.always")));//will then be dynamically reset or after RP has processed message
		Misc.setNotCalcNameForInnerPoint("1".equals(connProps.getProperty("DontCalcName.inner_point")));
		DbConnectionManager.check = connProps.getProperty("DBConn.checkQuery", check);
		DbConnectionManager.reInitialzeConnAfterSec = Misc.getParamAsLong(connProps.getProperty(serverName+".DBConn.reInitialzeConnAfterSec", ""+reInitialzeConnAfterSec));
		String str = connProps.getProperty("DBConn.checkConnectionMilliGapIfMulti");
		if (str != null) {
			try {
				int t1 = Integer.parseInt(str);
				if (t1 > 0) {
					DbConnectionManager.checkConnectionMilliGapIfMulti = t1; 
				}
			}
			catch (Exception e2) {
				DbConnectionManager.checkConnectionMilliGapIfMulti = 0;
			}
		}
		str = connProps.getProperty("DBConn.addnlparam");
		if (str != null && str.trim().length() != 0)
			DbConnectionManager.addnURLParam = str;

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
					+dataBase +"?zeroDateTimeBehavior=convertToNull&"
					+"traceProtocol="+"1".equals(connProps.getProperty(serverName+".DBConn.trace"))
					+(DbConnectionManager.addnURLParam != null ? "&"+DbConnectionManager.addnURLParam : "")
					;
			System.out.println("Connection String = "+ connectString);
		}
		userName = connProps.getProperty(serverName+".DBConn.userName", userName);
		password = connProps.getProperty(serverName+".DBConn.password", password);
		System.out.println("DB User Name ="+userName);
		System.out.println("DB Password ="+password);
		// MSSQL
		maxConnection = Integer.parseInt(connProps.getProperty(serverName+".DBConn.maxConnection", Integer.toString(maxConnection)));
		maxRetryAttempts = Integer.parseInt(connProps.getProperty(serverName+".DBConn.maxRetryAttempts", Integer.toString(maxRetryAttempts)));
		retryInterval = Integer.parseInt(connProps.getProperty(serverName+".DBConn.retryInterval", Integer.toString(retryInterval)));
		Misc.g_doRollupAtJava = !"0".equals(connProps.getProperty(serverName+".rollup_java"));
		connProps = null;
		try {
			if(!Misc.isUndef(maxConnectionHack)){
				connQueue = new Queue(maxConnectionHack);
			}else{
				connQueue = new Queue(maxConnection+10);
			}
		} catch (TooBigQueueException e) {
			try {
				connQueue = new Queue();
			} catch (TooBigQueueException ne) {
				ne.printStackTrace();
			}
		}
		try {
			if (Misc.G_DO_ORACLE) {
			} else {
				DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			}
			for (int i = 0; i < maxConnection; i++) {
				try {
					this.helperActualGetConnection(true);
				}
				catch (Exception e) {
				}
			}
		} catch (Exception  e) { // do nothing - won't happen
			e.printStackTrace();
		}
	}

	public Connection getConnection() throws ConnectionTimeoutException, SQLException, FullQueueException {
		Connection retval = null;
		boolean connNotPossibleRightNow = false;
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
				boolean emptyQ = false;
				try {
					retval = (Connection) connQueue.read();
				}
				catch (EmptyQueueException eq) {
					emptyQ = true;
					retval = null;
				}

				if (retval == null) {
					//if emptyQ ... then sleep for connections to be available
					//else get connection ... and the
					if (!emptyQ) {//we couldnt get conn early ... get new connection, write to Q (at end), get new connection in loop without sleeping
						try {
							retval = this.helperActualGetConnection(false);//we already took out the connection ... so if we get we use this ... 
							//and put back in Q when returning
							if (connQueue.debug) {
								connQueue.recordBusy(retval);
							}
						}
						catch (SQLException e) {
							connNotPossibleRightNow = true;
							connQueue.write(null);//return the read conn
							e.printStackTrace();
							throw e; //no point trying get more connections
						}
						catch (FullQueueException e) {
							connNotPossibleRightNow = true;
							e.printStackTrace();
							throw e; //no point trying to get more connections
						}
					}
				}
				if (retval != null) {
					// System.out.println("Found non null Connection");


					if (testValidAndSetAutoOff(retval)) {
						break;
					} else {
						returnConnection(retval, true);
						retval = null;
						continue; //check next if that is usable .. without sleeping
					}
				}
				retryCount++;
				try {
					//sleep only if emptyQ ... else try getting next conn asap
					if (emptyQ) {
						Thread.sleep(retryInterval);
					}
				} 
				catch (Exception e) {
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
		//retval.setAutoCommit(false); ... already done in test


		System.out.println("[@Getting Connection:]"+retval+" [Thread:]"+Thread.currentThread().getId()+"[QLength:]"+(connQueue.getQlength())+(connQueue.debug ? getRelevantCallProc() : ""));
		//retval.setAutoCommit(true);//DEBUG13 must not be there
		return (retval);
	}
	public static String check = "/* ping */ SELECT 1";
	public static int checkConnectionMilliGapIfMulti = 0; //on MPL 59 - somehow first is succeeding ..
	public static String addnURLParam = null;

	public static long reInitialzeConnAfterSec = -1l;

	public boolean testValidAndSetAutoOff(Connection conn)  {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(check);
			rs = ps.executeQuery();
			rs.close();
			rs = null;

			if (DbConnectionManager.checkConnectionMilliGapIfMulti > 0) {
				Thread.sleep(checkConnectionMilliGapIfMulti);
				rs = ps.executeQuery();
				rs.close();
				rs = null;
			}
			ps.close();
			ps = null;
			if (conn.getAutoCommit())
				conn.setAutoCommit(false);

			return reInitialzeConnAfterSec > 0 ? connQueue.validateConn(conn) : true;
			//return (!(conn.isClosed()));
		}
		catch (Exception e) {
			System.out.println("[DBPOOL_ERROR] Getting error connection:"+conn+" [Thread:]"+Thread.currentThread().getId()+"[QLength:]"+(connQueue.getQlength())+(connQueue.debug ? getRelevantCallProc() : ""));
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {

			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e3) {

			}
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
				// remove from connMap
				connQueue.removeConn(retConn);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				this.helperActualGetConnection(true);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (!m_usingTomcat) {
			try {
				connQueue.write(retConn);
				// add to connMap
				connQueue.addConn(retConn);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("[@Return Connection:](closed, destroyit, toCreateNew)("+isClosed+","+destroyIt+","+toCreateNew+",)"+retConn
				+" [Thread:]"+Thread.currentThread().getId()+"[QLength:]"+(connQueue.getQlength())+(connQueue.debug ? getRelevantCallProc() : ""));
	}

	private Connection helperActualGetConnection(boolean writeToQ) throws SQLException, FullQueueException {
		int numTries = 1;//5; //fail fast ... sleep will be done in getConnFromPool if it is possibke to connect
		int sleepBetweenTryMilli = 0;//2000;
		Connection retval = null;
		SQLException sqlExcp = null;
		for (int i=0;i<numTries;i++) {
			try {
				retval = getDBConnection(); 
			}
			catch (SQLException e) {
				System.out.println("[DBCONN] Issue connecting to DB while getting getDBConnection, try attempt:"+i);
				retval = null;
				sqlExcp = e;
				e.printStackTrace();
			}
			if (retval == null && sleepBetweenTryMilli > 0) {
				try {
					Thread.sleep(sleepBetweenTryMilli);
				}
				catch (Exception e) {

				}
			}
		}
		if (writeToQ) {
			try {
				connQueue.write(retval);
				connQueue.addConn(retval);
			}
			catch (FullQueueException excp) {
				//need to close retval ... 
				try {
					retval.close();
				}
				catch (Exception e2) {

				}
				retval = null;
				throw excp;
			}
		}
		if (retval == null)
			throw sqlExcp == null ? new SQLException("Unknown SQLException") : sqlExcp;
			return retval;
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
					this.helperActualGetConnection(true);
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

	public static Connection getConnectionFromPoolNonWeb(String instance) throws GenericException { //
		Connection conn = null;
		try {
			DbConnectionManager dbConnPool = null;
			dbConnPool = DbConnectionManager.getDBConnectionPool(instance);
			conn = dbConnPool.getConnection();
			//	conn.setAutoCommit(true);//DEBUG13 .. remove from prod
		} catch (SQLException e) {
			e.printStackTrace();
			//logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new GenericException(e);
		} catch (ConnectionTimeoutException e) {
			e.printStackTrace();
			//logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new GenericException(e);
		} catch (Exception e) {
			e.printStackTrace();
			//logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new GenericException(e);
		}
		return conn;
	}

	public static void returnConnectionToPoolNonWeb(String instance,Connection conn) throws GenericException {
		returnConnectionToPoolNonWeb(instance,conn, false);
		return;		
	}

	public static void returnConnectionToPoolNonWeb(String instace,Connection conn, boolean destroyIt) throws GenericException {
		try {
			if (conn == null) {
				return;
			}
			DbConnectionManager dbPool = DbConnectionManager.getDBConnectionPool(instace);
			//conn.rollback();
			//conn.setAutoCommit(true);
			dbPool.returnConnection(conn, destroyIt);
		} catch (Exception e) {
			e.printStackTrace();
			//	logger.error(ExceptionMessages.DB_CONN_PROBLEM, e);
			throw new GenericException(e);
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
	public static int getCancellableRelevantCallProc() {
		try {
			StackTraceElement[] stckTrc = Thread.currentThread().getStackTrace();
			for (int i=0,is=stckTrc == null ? 0 :stckTrc.length; i<is;i++) {
				StackTraceElement elem = stckTrc[i];
				String className = elem.getClassName();
				if (className == null || className.startsWith("java."))
					continue;
				boolean toCont = false;
				if (className.startsWith("com.ipssi.gen.utils.InitHelper")) {
					return 1;
				}
				if (toCont)
					continue;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return 0;
	}	
	//debug code
	public static void main(String[] args) {

	}
}

//Title:        Your Product Name
//Version:
//Copyright:   Copyright (c) 1999
//Author:      Your Name
//Company:     Your Company
//Description:
package com.ipssi.gen.utils;
//import com.ipssi.gen.cache.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.AttributedString;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.rfid.beans.SECLWorkstationDetails;



public final class Misc {
	public static final int STRIKE_DIM_ID = 901;
	public static final int DALAUP_DIM_ID = 902;
	public static final int EXC_IDLING_DIM_ID = 903;
	public static final int BLE_PLACEHOLDER_DIMID = 904;
	public static final int SHOVEL_PLACEHOLDER_DIMID = 905;
	
	volatile public static boolean g_doOrgAssignmentTimeBased = false;
	volatile public static boolean g_hasvehicle_org_time_assignments = false;
	volatile public static int g_doDalaUp = 2;//0 - dont do, 1 always, 2 only near unload/garage
	volatile public static String g_userSuppliedServerName = null;
	volatile public static String g_scriptVersion = "500";
	volatile public static String  g_uidPrefix = null;
	volatile public static boolean g_doSingleSession = false;
	volatile public static boolean g_doMPL = false;
	volatile public static boolean g_doSCCL = false;
	volatile public static int g_doPasswordMangling = 0; //0 => not do, 1 means actual password is by shifting alternate chars  1 to the right and 1 to left 
	volatile public static boolean g_doAREACODE = false; // if true then include area_code also in join
	
	   volatile private static boolean g_dontCalcName = false;
	   volatile public static int g_intermediateDistUpdMode = 0;//0=> old approach, 1=> delta, 2=>as needed
	   volatile public static int g_passwordStrengthPolicy = 0; //0 => none, 1 => strong (only one implemented)
	   volatile public static int g_passwordExpiryDays = Misc.getUndefInt();
	   volatile public static int g_nonUseExpiry = Misc.getUndefInt();
	   public static String g_passwordStrengthHint = "Must contain non-alphanumeric, a letter and a number. Length minimum 7 characters";
	   public static boolean isPasswordStrong(String password) {
		   if (g_passwordStrengthPolicy == 0)
			   return true;
		   boolean retval = password != null && password.length() >= 7;
		   if (!retval)
			   return retval;
		   boolean hasNonAlpha = false;
		   boolean hasLetter = false;
		   boolean hasNumber = false;
		   for (int i=0,is=password.length(); i<is;i++) {
			   int c = password.charAt(i);
			   if (Character.isDigit(c))
				   hasNumber = true;
			   else if (Character.isLetter(c))
				   hasLetter = true;
			   else 
				   hasNonAlpha = true;
		   }
		   return hasNonAlpha && hasLetter && hasNumber;
	   }
	   
	   public static Pair<String, Boolean> getValidityPassword(int userId, Connection conn) {
		   if (g_passwordExpiryDays <= 0)
			   return new Pair<String, Boolean>(null, true);
		   String msg = null;
		   boolean valid = true;
		   //check if password is strong
		   //check if expired or nearing expiry
		   PreparedStatement ps = null;
		   ResultSet rs = null;
		   try {
			   ps = conn.prepareStatement("select PASSWORD, last_password_change from users where id=?");
			   ps.setInt(1, userId);
			   rs = ps.executeQuery();
			   boolean passwordStrong = true;
			   boolean passwordValid = true;
			   boolean passwordNeedRemineder = false;
			   int changeInDays = 0;
			   if (rs.next()) {
				   String password = rs.getString(1);
				   long ts = Misc.sqlToLong(rs.getTimestamp(2));
				   passwordStrong = Misc.isPasswordStrong(password);
				   changeInDays = ts > 0 ?Misc.g_passwordExpiryDays - (int) Math.ceil( (System.currentTimeMillis() - ts)/(24*3600*1000) )  : -1;
				   if (changeInDays <= 0)
					   passwordValid = false;
				   if (changeInDays < 3)
					   passwordNeedRemineder = true;
			   }
			   rs = Misc.closeRS(rs);
			   ps = Misc.closePS(ps);
			   if (!passwordStrong) {
				   valid = false;
				   msg = "Please change your password to make it strong.</br>" +
				   		" # Password must contain at least one digit[0-9].</br>" +
				   		" #   must contain at least one alphabet[a-z]." +
				   		"</br> #   must contain at least one special symbol[!@#$%^&*].</br>";
			   }
			   else if (!passwordValid) {
				   valid = false;
				   msg = "Please change your password now";
			   }
			   else if (passwordNeedRemineder) {
				   valid = true;
				   msg = "Your password will expire in "+changeInDays+ " Please change it by going to your preferences.</br>" +
				   		" # Password must contain at least one digit[0-9].</br>" +
				   		" #   must contain at least one alphabet[a-z]." +
				   		"</br> #   must contain at least one special symbol[!@#$%^&*}.</br>"; 
			   }
			   
		   }
		   catch (Exception e) {
			   e.printStackTrace();
			   //eat it
		   }
		   finally {
			   
		   }
		   return new Pair<String, Boolean> (msg, valid);
	   }
	   public static boolean toNotCalcName() {
		   return g_dontCalcName;
	   }
	   synchronized public static void setNotCalcName(boolean val) {//to force sync
		   g_dontCalcName = val;
	   }
	   volatile private static boolean g_dontCalcNameForInnerPoint = false;
	   public static boolean toNotCalcNameForInnerPoint() {
		   return g_dontCalcNameForInnerPoint;
	   }
	  synchronized  public static void setNotCalcNameForInnerPoint(boolean val) {//to force sync
		   g_dontCalcNameForInnerPoint = val;
	   }
	 volatile private static int ruleRedoRecoveryDone= 0;
	 volatile private static int tripRedoRecoveryDone= 0;
	 volatile private static Integer isSPNDirty= 0;
		
	 public static   int getRuleRedoRecovery(){
         synchronized(Misc.class){
		 return ruleRedoRecoveryDone;
         }
	 }
	 
	public static int getIsSPNDirty() {
		return isSPNDirty;
	}
	public static void setIsSPNDirty(int isSPNDirty) {
		synchronized(Misc.isSPNDirty){
			Misc.isSPNDirty = isSPNDirty;
		}
	}
	public static synchronized void incrementRuleRedoCount() {
		  ruleRedoRecoveryDone++;
	    }
	  public static  int getTripRedoRecovery(){
		  synchronized(Misc.class){
			 return tripRedoRecoveryDone;
		 }
		 }
	  public static synchronized void incrementTripRedoCount() {
		  tripRedoRecoveryDone++;
	    }
	    public static boolean g_doRollupAtJava = true;
	  	public static int g_jniPort = 1099;
	    public static int g_CBSEPort = 5101;
	    public static int g_mtnioServerUDPPort = 6605;
	    public static int g_mtnioCurrentPort = 5205;
	    public static int g_mtnioDataPort = 5030;
	    public static int g_mtnioNovireCurrentPort = 5420;
	    public static int g_mtnioMeitrack310Port = 5206;
	    public static int g_mtnioTeltonikaRegPort = 5410;
	    public static int g_mtnioGalileoRegPort = 11410;
	    public static int g_mtnioPhytecPort = 12410;
	    public static int g_mtnioTeltonikaConfigPort = 5408;
	    public static int g_mtnioFastTrackPort = 6665;
	    public static int g_mtnioATrackAY5Port = 5215;
	    public static int g_mtnioACTeckPort = 5207;
	    public static int g_mtnioAndroidPort = 15317;
	    public static int g_conCoxPort = 15314;
	    public static int g_rpPort = 15354;
	    public static int g_gdcPort = 21108;
	    
	    public static int g_mtnioAISPort = 13410; 

	   public static int G_TOP_LEVEL_PORT = 2; //TODO make it 1 later
		public static String G_IMAGES_BASE = "/static/images/";
		public static String G_SCRIPT_BASE = "/static/scripts/";
		public static String G_APP_1_BASE = "/LocTracker/";
		public static String G_APP_2_BASE = "/mapguide/javasamples/";
		public static String G_COMMON_BASE = "/static/common/";
		public static String G_JQUERY_BASE = "/static/jquery/";
		public static String G_TYPESCRIPT_BASE = "/static/TypeScripts/";
	   public static String NO_FIELD_ACCESS_MSG = "No Privilege";
	   public static String G_DEFAULT_DATE_FORMAT = MiscInner.PortInfo.G_DEFAULT_LOCALEID == 0 ? "MM/dd/yy" : "dd/MM/yy"; //ALSO BE SURE TO SET VARIABLE IN PROFILE.JS and C++
	   public static String G_DEFAULT_DATE_FORMAT_HHMM = G_DEFAULT_DATE_FORMAT+" HH:mm";
	   public static String G_DEFAULT_TIME_ONLY_FORMAT = "HH:mm:ss";
	   public static boolean G_DO_NO_LHS = false;//G_INCL_PURCHASING_CHANGE;//false means will have lhs menu;
	   public static String G_NEW_MENU_SCRIPT = true /*was G_DO_NO_LHS*/ ? "onMouseOver='showMulti(this)' onMouseOut='unshowMulti(this)'" : "";
	   public static boolean G_DOING_IT_SIMPLE = false;
	   
	   public static final String emptyString = "";
	   public static final String nbspString = "&nbsp;";
	   public static String G_ALT_LABEL = "Alternative";
	    public static String G_ALT_MENU_LABEL = "Alternative: ";
	    public static boolean G_TAKE_PRJ_LOCK=false;
	  //MS SQL
	    public static final boolean G_DO_ORACLE = false;
	    public static final boolean G_DO_SQL_SERVER = true;
	    public static boolean G_NEXTBESTBUTTON_ONLY = false;
	    public static boolean G_WIZARD_AT_BOTTOM = true;
	  //MS SQL
	    public static int G_LAUNCH_MS = -1;
	    
	    public static int VEHICLE_ID_DIM = 20274;
	    public static int DRIVER_ID_DIM = 20900;


	  public static int G_HACKANYVAL = -1000;
	  
	  public static int G_HACK_ISNULL_LOV = Misc.getUndefInt()-1;
	  public static int G_HACK_ISNOTNULL_LOV = Misc.getUndefInt()-2;
	  public static String G_HACK_ISNULL_LOVSTR = Integer.toString(G_HACK_ISNULL_LOV);
	  public static String G_HACK_ISNOTNULL_LOVSTR = Integer.toString(G_HACK_ISNOTNULL_LOV);
	  
	  public static int PERIOD_TO_SHOW_ALL = 0;
	  public static int PERIOD_TO_SHOW_PAST_ONLY = 1;
	  public static int PERIOD_TO_SHOW_FUTURE_ONLY = 2;
	  public static int DEFAULT_PERIODS[] = {12,5,12,13};
	  public static int OVERRIDE_EDIT_PRIV = 0;


	  public static boolean G_MITTAL_DOING = true;
	  public static boolean G_COVANCE_DOING = false;
	  public static boolean G_WATSON_DOING = false;
	  public static boolean G_BD_DOING = false;//true;
	  public static boolean G_SCHERING_HACK = false;
	  public static boolean G_DOING_SCHERING = false;

		public static String ATTR_SIMPLE_VALIDATION = "__simpleValResult";

		// For file size limit
		public static int G_UPLOAD_FILE_LIMIT = 10000000;

	 
	  
	  
	  public static java.util.Date G_CURRENT_ACTUAL_DATE = null;
	  
	  public static String CFG_ICONFIG_PATH = null;
	  

		//Report Types
		public static final int PDF = 0;
		public static final int EXCEL = 1;
		public static final int HTML = 2;
	    public static final int BLOCK_HTML = 3;
	    public static final int XML=4;
	    public static final int JSON=5;
	    public static final int DOTMATRIX=6;
	    public static final int CHART=7;
	    public static final int ANGULAR = 12;
	    public static java.util.Date G_REF_DATE_FOR_WEEK = new java.util.Date(90,0,1);
	    private static String CFG_CONFIG_SERVER = null;
	    private static int G_DPRECO_THREAD = 1;
	    private static int G_DPLOG_THREAD = 1;
	    private static String INSTANCE_NAME = "";
	    private static int SERVER_MODE = Misc.UNDEF_VALUE;// 0 -- normal mode 1 -- backup mode
	    //private static Connection DATABACKUPSERVERCONN = null;//use to back up log data or something from DP........etc
	    private static Connection BACKUP_MODE_SERVER_DB_CONN = null;
	    private static String BACKUP_MODE_SERVER_DB_NAME = null;
		private static String BACKUP_MODE_SERVER_HOST = null;
		private static String BACKUP_MODE_SERVER_PORT = null;
		private static String BACKUP_MODE_SERVER_USER = null;
		private static String BACKUP_MODE_SERVER_PASSWORD = null;

	    private static Connection JMS_SERVER_DB_CONN = null;
	    private static String JMS_SERVER_DB_NAME = null;
		private static String JMS_SERVER_HOST = null;
		private static String JMS_SERVER_PORT = null;
		private static String JMS_SERVER_USER = null;
		private static String JMS_SERVER_PASSWORD = null;

	    private static String DATA_RECOVERY_FILE_LOCATION = null; //having flag value either 0 or 1 or 2 to indicate DP in Primary Node To Stop Recovery Data....  
        private static boolean G_CONFIG_SERVER_LOAD = false;
        private static String G_BACKUP_SERVER_NAME = null;
        public static int g_rptpInitSeq = 3; // 0=> old first DP, 0x1 => first TP, 0x2 => first RP, 0x3, RPTP together
        
        private static long currSystemTime = Misc.getUndefInt();
        private static String emailReportServer;
        public static void setSystemTime(long ts) {
        	currSystemTime = ts;
        }
        // to load property file and make available for other app
        public static Properties newConnProp = null;
      
        //just pick one of the following
      public static long getSystemTime() { return System.currentTimeMillis();} //... in prod
      //  public static long getSystemTime() { return currSystemTime <= 0 ? System.currentTimeMillis() : currSystemTime;}
     //   
      //just pick one of the following
      public static final String ABSOLUTE_IMAGE_PATH = "/home/jboss/static/images/";
//      public static final String ABSOLUTE_IMAGE_PATH = "G:\\Working\\EclipseWorkspace\\static\\images\\";
        //just pick one of the following
//     private static String SYSTEM_BASE_CFG_SERVER = "/home/jboss/";
    //private static String SYSTEM_BASE_CFG_SERVER = "C:\\IPSSI\\LocTracker\\";
//      private static String SYSTEM_BASE_CFG_SERVER = "/home/pi/ipssi/";
//     public static String SYSTEM_BASE_CFG_SERVER = "D:\\ipssi\\";
     public static String SYSTEM_BASE_CFG_SERVER = "C:\\Working\\EclipseWorkspace\\LocTracker\\";
//       private static String SYSTEM_BASE_CFG_SERVER = "C:\\jboss-4.2.3.GA\\";
        private static String BASE_CFG_CONFIG_SERVER = SYSTEM_BASE_CFG_SERVER;
        
    	//public static String CFG_CONFIG_SERVER = "C:\\IPSSI\\LocTracker\\config_server";
        // public static String CONN_PROPERTY = "C:\\Working\\EclipseWorkspace\\LocTracker\\config_server\\new_conn.property";
         public static String CONN_PROPERTY = SYSTEM_BASE_CFG_SERVER+"config_server"+System.getProperty("file.separator")+"new_conn.property";

	  public static String CFG_CONFIG_WEB = null;
	  public static String CFG_USERFILES_SAVE = null;
	  public static String CFG_USERFILES_VP = null;
	  public static String CFG_CONFIG_WEB_VP = null;



	  public static int NUM_DAYS_PER_YEAR = 360;
	  final  public static int SCOPE_QTR = 0;
	  final  public static int SCOPE_ANNUAL = 1;
	  final  public static int SCOPE_MONTH = 2;
	  final  public static int SCOPE_WEEK = 3;
	  final  public static int SCOPE_DAY = 4;
	  final  public static int SCOPE_CUSTOM = 5;
	  final  public static int SCOPE_SHIFT = 6;
	  final  public static int SCOPE_HOUR = 7;
	  final  public static int SCOPE_HOUR_RELATIVE = 8;	  
	  public static int LOWEST_PARTIAL_DATA_SCOPE = 3;
	  public static String escapeJson(String str) {
		  str = str.replaceAll("\"","\\\\\"");
		  str = str.replaceAll("\r\n", ",");
		  str = str.replaceAll("\r", ",");
		  str = str.replaceAll("\n", ",");
		  return str;
	  }
	  public static boolean isLHSHigherScope(int lhs, int rhs) {
		  
		  if (lhs < 0 || lhs >= 100)
			  return false;
		  else if (rhs < 0 || rhs >= 100)
			  return true;
		  else if (lhs == SCOPE_TILL_DATE)
			  return true;
		  else if (rhs == SCOPE_TILL_DATE)
			  return false;
		  else if ((lhs >= 50 && lhs < 95) || (rhs >= 50 && rhs < 95))
			  return lhs > rhs;
		  if (lhs == SCOPE_TILL_DATE)
			  return true;
		  if (lhs == SCOPE_ANNUAL)
			  return rhs != SCOPE_TILL_DATE;
		  else if (lhs == SCOPE_QTR)
			  return rhs != SCOPE_TILL_DATE && rhs != SCOPE_ANNUAL;
		  else if (lhs == SCOPE_MONTH)
			  return (rhs != SCOPE_TILL_DATE && rhs != SCOPE_QTR && rhs != SCOPE_ANNUAL);
		  else if (lhs == SCOPE_WEEK)
			  return (rhs == SCOPE_DAY || rhs == SCOPE_SHIFT || rhs == SCOPE_HOUR);
		  else if (lhs == SCOPE_DAY)
			  return (rhs == SCOPE_SHIFT || rhs == SCOPE_HOUR);
		  else if (lhs == SCOPE_SHIFT)
			  return (rhs == SCOPE_HOUR);
		  else 
			  return false;
		  
	  }
	  final  public static int SCOPE_MTD = 9;
	  final public static int SCOPE_USER_PERIOD = 10;
	  final public static int SCOPE_TILL_DATE = 11;
	  public static int EOFMARKER = -1000;
	  public static double LARGE_NUMBER = (double) 1e12;
	  public static double TOLERANCE = (double)0.000001;


	  public static  long UNASSIGNED_PORTFOLIO_ID = (long) 0;
	  public static String LINE_SEPARATOR_STRING = System.getProperty("line.separator");
	  public static final int NOT_FOUND = -10000;
	  public static final int MAX_YEAR = 2000;
	  public static final int UNDEF_VALUE = -1111111;//-1999999999;
	  public static final int UNDEF_SHORT_VALUE = -11111;
	  public static final int UNDEF_BYTE_VALUE = -1;
	  public static final double UNDEF_FLOAT_VALUE = -1e12f;//UNDEF_VALUE * 100000;
	  public static final double UNDEF_FLOAT_VALUE_CMP = -1e11f;//UNDEF_VALUE * 100000;
	  public static final int MAX_PROFILE_OUTCOMES = 25;
	  public static final int MAX_MILESTONES = 100;
	  public static double MAX_VALUE = 1e25f;
	  public static int MAX_INT_VALUE = Integer.MAX_VALUE;
	  
	  private static String serverName = null;
	  public static ResultSet closeRS(ResultSet rs) {
		  try {
			  if (rs != null)
				  rs.close();
			  rs = null;
		  }
		  catch (Exception e) {
			  rs = null;
		  }
		  return rs;
	  }
	  public static Statement closeStmt(Statement ps) {
		  try {
			  if (ps != null)
				  ps.close();
			  ps = null;
		  }
		  catch (Exception e) {
			  ps = null;
		  }
		  return ps;
	  }
	  public static PreparedStatement closePS(PreparedStatement ps) {
		  try {
			  if (ps != null)
				  ps.close();
			  ps = null;
		  }
		  catch (Exception e) {
			  ps = null;
		  }
		  return ps;
	  }
	  public static ObjectInputStream closeObjInp(ObjectInputStream in) {
		  try {
			  if (in != null)
				  in.close();
		  }
		  catch (Exception e) {
			  in = null;
		  }
		  return in;
	  }
	  public static ObjectOutputStream closeObjOut(ObjectOutputStream oos) {
		  try {
			 if (oos != null)
				 oos.close();
		  }
		  catch (Exception e) {
			  oos = null;
		  }
		  return oos;
	  }
	  public static String getServerName() {
		  if (serverName == null) {
			  if (g_userSuppliedServerName != null)
				  serverName = g_userSuppliedServerName;
			  else
				  serverName = System.getProperty("jboss.server.name");
			  if (serverName == null)
				  serverName = "default";
		  }
		  return serverName;
			  
	  }
	  public static void setServerName(String _serverName) {
		  serverName = _serverName;
	  }
	  public static void setConnProperty(String _connPropertyFile) {
		  CONN_PROPERTY = _connPropertyFile;
	  }
	  public static boolean isUndef(short val) {
		  return val == UNDEF_SHORT_VALUE;
	  }
	  public static boolean isUndef(int val) { 
	     return val == UNDEF_VALUE;
	  }  
	  public static boolean isUndef(byte b) {
		  return b == UNDEF_BYTE_VALUE;
	  }
	  public static boolean isUndef(long val) { 
	     return val == UNDEF_VALUE;
	  }
	  public static boolean isUndef(double val) { 
	     return val <= UNDEF_FLOAT_VALUE_CMP ;
	  }
	  public static boolean isExplicitAny(int val) { return val == (UNDEF_VALUE+1);}
	  public static short getUndefShort() { return UNDEF_SHORT_VALUE;}
	  public static int getUndefInt() { return (int) UNDEF_VALUE; }
	  public static byte getUndefByte() { return UNDEF_BYTE_VALUE;};
	  public static int getExplicitAny() { return (int) UNDEF_VALUE+1; }

	  public static double getUndefDouble() { return (double) UNDEF_FLOAT_VALUE;}
	  public static final int ETERNITY = 100000;
	  public static int MAX_NUM_YEARS = 40*4; //TODO - read from cache must be non final
	//  public static double DISCOUNT_RATE = 0.015f; //TODO - read from cache must be non final
	  //These values AND Functions are for managing conversion of years into appropriate quarters
	  
	  public static int getQuarter(java.util.Date date) {
	     return getQuarter(date.getYear()+1900)+date.getMonth()/3; //because of the java thingy
	  }
	  public static int getQuarter(int year) {
	     return (year-1900)*4; //we are fixing ourselves also to 1900
	  }

	  public static java.sql.Date getCurrentTime() {
	    java.sql.Date retval = new java.sql.Date(System.currentTimeMillis());
	    return retval;
	  }
	  
	  //Some Labels being defined here to facilitate in Cookie Manipulation
	//  public static String LABEL_ACTIVE_WORKSPACE_ID = "workspace_id"; //was wrkspId
	//  public static String LABEL_ACTIVE_PROJECT_ID = "project_id"; //was prjId
	  //these were defined in tipsmart
	  
	  public static  String DBCONN_LABEL = "_dbConnection";
	  public static  String USER_PARAMETER_LABEL = "u";
	  public static  String UID_PARAMETER_LABEL = "uid";
	  public static  int USERSTATUS_DUMMY = 4; // must match constants.sql
	  public static int USERSTATUS_ACTIVE = 1;
	  public static int USERSTATUS_ONLYSALE = 2;
	  public static String DUMMY_NAME = "DUMMY";

	  public static boolean isEqual(double a, double b) {
	     return Math.abs((a-b)) < TOLERANCE;
	  }

	  public static boolean isEqual(double a, double b, double tol, double perc) {
	     double diff = Math.abs(a-b);
	     if (diff < tol)
	        return true;
	     double sum = Math.abs((a+b))/2.0f;
	     double prop = diff/sum;
	     return prop < perc;
	  }

	  
	  public static int executeScalar(String query, Connection conn, int undefVal, int param1, int param2) throws SQLException {
	     PreparedStatement pStmt = conn.prepareStatement(query);
	     if (!Misc.isUndef(param1))
	        pStmt.setInt(1, param1);
	     if (!Misc.isUndef(param2))
	        pStmt.setInt(2, param2);
	     ResultSet rset = pStmt.executeQuery();
	     int retval = undefVal;
	     if (rset.next()) {
	        retval = rset.getInt(1);
	     }
	     rset.close();
	     pStmt.close();
	     return retval;
	  }


	  public static void getDateParamInElem(java.util.Date date, Element elem, String prefix) {
	     if (date != null) {
	        elem.setAttribute(prefix+"day", Integer.toString(date.getDate()));
	        elem.setAttribute(prefix+"month", Integer.toString(date.getMonth()));
	        elem.setAttribute(prefix+"year", Integer.toString(date.getYear()+1900)); //because of the java thingy
	        elem.setAttribute(prefix+"date", getPrintableDateSimple(date));
	     }
	  }

	  public static double getParamAsDouble(String str, double undefVal) {
	     if (str == null)
	        return (double) undefVal;
	     try {
	        str = trim(str);
	        str = str.replaceAll(",","");
	        double f = Double.parseDouble(str);
	        if (Misc.isUndef(f))
	           return undefVal;
	        return f;
	     }
	     catch (Exception e) {
	        return (double) undefVal;
	     }
	  }

	  public static double getParamAsDouble(String str) { return getParamAsDouble(str,getUndefDouble()); }


	  public static long getParamAsLong(String str, long undefVal) {
	     if (str == null)
	        return (long) undefVal;
	     try {
	        str = trim(str);
	        long l = Long.parseLong(str);
	        if (Misc.isUndef(l))
	           return undefVal;
	        return l;
	     }
	     catch (Exception e) {
	        return (long) undefVal;
	     }
	  }

	  public static long getParamAsLong(String str) { return getParamAsLong(str,getUndefInt()); }
	  public static String trim(String s) {
		  return s.replaceAll("^[\\u00A0\\s]+|[\\u00A0\\s]+$", "");
	  }
	  
	  public static int getParamAsInt(String str, int undefVal) {
	     if (str == null)
	        return undefVal;
	     try {
	        str = trim(str);
	        str = str.replaceAll(",","");
	        int i = Integer.parseInt(str);
	        if (Misc.isUndef(i))
	           return undefVal;
	        return i;
	     }
	     catch (Exception e) {
	        return undefVal;
	     }
	  }

	  public static void setAttributeInt(ResultSet rset, int index, Element elem, String attribName, int undefVal) throws SQLException{
	     int val = rset.getInt(index);
	     if (rset.wasNull())
	        val = undefVal;
	     if (!Misc.isUndef(val))
	        elem.setAttribute(attribName, Integer.toString(val));
	  }

	  public static void setAttributeDouble(ResultSet rset, int index, Element elem, String attribName, double undefVal)  throws SQLException{
	     double val = rset.getDouble(index);
	     if (rset.wasNull())
	        val = undefVal;
	     if (!Misc.isUndef(val))
	        elem.setAttribute(attribName, Double.toString(val));
	  }

	  public static void setAttributeString(ResultSet rset, int index, Element elem, String attribName, String undefVal)  throws SQLException{
	     String val = rset.getString(index);
	     if (rset.wasNull())
	        val = undefVal;
	     if (val != null && val.length() != 0)
	        elem.setAttribute(attribName,val);
	  }

	  public static void setAttributeString(Element elem, String attribName, String attribVal, String undefVal){
	     if (attribVal == null || attribVal.length() == 0)
	          attribVal = undefVal;
	     if (attribVal != null && attribVal.length() != 0) {
	        elem.setAttribute(attribName, attribVal);
	     }
	     else
	        elem.removeAttribute(attribName);
	  }

	  public static void setAttributeInt(Element elem, String attribName, int attribVal, int undefVal){
	     if (Misc.isUndef(attribVal))
	          attribVal = undefVal;
	     if (!Misc.isUndef(attribVal)) {
	        elem.setAttribute(attribName, Integer.toString(attribVal));
	     }
	     else {
	        elem.removeAttribute(attribName);
	     }
	  }

	  public static void setAttributeDate(ResultSet rset, int index, Element elem, String attribName, String undefVal) throws SQLException {
	     java.sql.Date date = rset.getDate(index);
	     String val = rset.wasNull() ? undefVal : getPrintableDateSimple(date);
	     if (val != null && val.length() != 0)
	        elem.setAttribute(attribName,val);
	  }

	  public static String getParamAsString(String str, String undefVal) {
		  return getParamAsString(str, undefVal, true);
	  }

	  public static String getParamAsString(String str, String undefVal, boolean doTrim) {
		  if (str != null && doTrim)
			  str = trim(str);
	     if (str == null || str.length() == 0)
	        return undefVal;
	     return str;
	  }

	  public static String getParamAsString(String str) {
	     return getParamAsString(str,"");
	  }

	  public static int getParamAsInt(String str) { return getParamAsInt(str, getUndefInt()); }

	  public static void setParamDouble(CallableStatement cStmt, double val, int atPos) throws SQLException {
	    if (isUndef(val))
	      cStmt.setNull(atPos, Types.FLOAT);
	    else
	      cStmt.setDouble(atPos, val);
	  }

	  public static void setParamDouble(CallableStatement cStmt, String str, int atPos, double undefValue) throws SQLException {
	     double par = getParamAsDouble(str,undefValue);
	     setParamDouble(cStmt, par, atPos);
	  }

	  public static void setParamInt(CallableStatement cStmt, int val, int atPos) throws SQLException{
	    if (isUndef(val))
	      cStmt.setNull(atPos, Types.INTEGER);
	    else
	      cStmt.setInt(atPos, val);
	  }

	  public static void setParamInt(PreparedStatement cStmt, int val, int atPos) throws SQLException{
	    if (isUndef(val))
	      cStmt.setNull(atPos, Types.BIGINT);
	    else
	      cStmt.setInt(atPos, val);
	  }

	  public static void setParamInt(CallableStatement cStmt, String str, int atPos, int undefValue) throws SQLException {
	     int par = getParamAsInt(str, undefValue);
	     if ("-1111111".equals(str)) { //TODO_VERIFY ... if ill consequences
	        par = Misc.getUndefInt();
	        cStmt.setInt(atPos, par);
	        return;
	     }
	     setParamInt(cStmt,par,atPos);
	  }


	  public static void setParamInt(CallableStatement cStmt, String str, int atPos) throws SQLException  {
	     setParamInt(cStmt,str,atPos,getUndefInt());
	  }


	  public static void setParamLong(CallableStatement cStmt, long val, int atPos) throws SQLException{
	    if (isUndef(val))
	      cStmt.setNull(atPos, Types.BIGINT);
	    else
	      cStmt.setLong(atPos, val);
	  }

	  public static void setParamLong(PreparedStatement cStmt, long val, int atPos) throws SQLException{
	    if (isUndef(val))
	      cStmt.setNull(atPos, Types.BIGINT);
	    else
	      cStmt.setLong(atPos, val);
	  }


	  public static void setParamLong(CallableStatement cStmt, String str, int atPos, int undefValue)  throws SQLException {
	     long par = getParamAsLong(str, undefValue);
	     setParamLong(cStmt, par, atPos);
	  }

	  public static void setParamLong(CallableStatement cStmt, String str, int atPos) throws SQLException  {
	     setParamLong(cStmt,str,atPos,getUndefInt());
	  }

	  public static java.sql.Date getParamAsDate(Element dateElement, String prefix, java.util.Date defaultDate) {
	     return getParamAsDate(dateElement.getAttribute(prefix+"day"),
	                           dateElement.getAttribute(prefix+"month"),
	                           dateElement.getAttribute(prefix+"year"), defaultDate
	                           );
	  }
	  public static java.util.Date getParamAsDateFull(String date) {
		  SimpleDateFormat withSec = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		  SimpleDateFormat withMinOnly = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		  SimpleDateFormat withDateOnly = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		  SimpleDateFormat stdFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  return getParamAsDate(date, withSec, withMinOnly, withDateOnly, stdFmt);
	  }
	  public static java.util.Date getParamAsDate(String date, SimpleDateFormat withSec, SimpleDateFormat withMinOnly, SimpleDateFormat withDateOnly, SimpleDateFormat stdFmt) {
		  java.util.Date dt = null;
		  if (dt == null && withMinOnly != null) {
			  try {
				  dt = withMinOnly.parse(date);
			  }
			  catch (Exception e){
			  
			  }
		  }
		  if (dt == null && withSec != null) {
			  try {
				  dt = withSec.parse(date);
			  }
			  catch (Exception e){
				  
			  }  
		  }
		  if (dt == null && withDateOnly != null) {
			  try {
				  dt = withDateOnly.parse(date);
			  }
			  catch (Exception e){
			  
			  }
		  }
		  if (dt == null && stdFmt != null) {
			  try {
				  dt = stdFmt.parse(date);
			  }
			  catch (Exception e){
			  
			  }
		  }
		  return dt;
	  }

	  public static java.sql.Date getParamAsDate(String dateStr, java.util.Date defaultDate) {
	     return getParamAsDate(dateStr, defaultDate, (FmtI.Date) null);
	  }
	  public static java.sql.Date getParamAsDate(String dateStr, java.util.Date defaultDate, FmtI.Date formatter) {
	     java.sql.Date retval = null;
	     if (formatter != null) 
	        retval = formatter.getDate(dateStr, defaultDate);
	     else {
	       try {
	           synchronized (m_dateFormatter) {
 	              java.util.Date dt1 = m_dateFormatter.parse(dateStr);
	              if (dt1 != null)
	                 retval = new java.sql.Date(dt1.getTime());
	           }
	       }
	       
	       catch (Exception e) {
	       }
	     }
	     if (retval == null && defaultDate != null)
	       retval = new java.sql.Date(defaultDate.getTime());
	     return retval;     
	  }
	  
	  public static java.util.Date getParamAsUtilDate(String dateStr, java.util.Date defaultDate, FmtI.Date formatter) {
		     java.util.Date retval = null;
		     if (formatter != null) {
		        retval = formatter.getDate(dateStr, defaultDate);
		        retval = retval != null ? new java.util.Date(retval.getTime()) : null;
		     }
		     else {
		       try {
		           synchronized (m_dateFormatter) {
		        	   retval = m_dateFormatter.parse(dateStr);
		           }
		       }
		       catch (Exception e) {
		       }
		     }
		     if (retval == null && defaultDate != null)
		       retval = defaultDate;
		     return retval;     
		  }

	  public static java.sql.Date getParamAsDate(String day, String month, String year, java.util.Date defaultDate) {
	     java.util.Date date = defaultDate;
	     if ((day != null) && (month != null) && (year != null)) {
	        try {
	           date = new java.util.Date (Integer.parseInt(year)-1900, Integer.parseInt(month), Integer.parseInt(day));
	        }
	        catch (Exception e) {
	        }
	     }
	     if (date != null)
	       return new java.sql.Date(date.getTime());
	     else
	       return null;
	  }
	  public static void setParamDate(CallableStatement cStmt, int atPos, java.sql.Date date) throws SQLException {
	    if (date == null ) {
	        cStmt.setNull(atPos, Types.DATE);
	     }
	     else {
	        cStmt.setDate(atPos, new java.sql.Date(date.getTime()));
	     }
	  }

	  public static void setParamDate(CallableStatement cStmt, String day, String month, String year, int atPos, java.util.Date defaultDate) throws SQLException{
	     java.sql.Date date = getParamAsDate(day, month,year,defaultDate);
	     setParamDate(cStmt,atPos, date);
	  }

	  public static void setParamDate(CallableStatement cStmt, String day, String month, String year, int atPos) throws SQLException{
	     setParamDate(cStmt,day,month,year,atPos,null);
	  }


	  public static void addMonths(java.util.Date date, int months) {
	      int newMon = date.getMonth() + months;
	      date.setMonth(newMon % 12);
	      date.setYear(date.getYear()+ (newMon/12));
	  }
	  public static void addMonths(java.util.Date date, double months) {
	      double newMon = date.getMonth() + months;
	      date.setMonth((int) (newMon % 12));
	      date.setYear((int) (date.getYear()+ (newMon/12)));
	  }

	  public static void addDays(java.util.Date date, int days) {
	     date.setTime(date.getTime()+(long)days*24L*60L*60L*1000L);
	  }
	  
	  public static void addSeconds(java.util.Date date, int seconds) {
		   date.setTime(date.getTime()+(long)seconds*1000L);
	  }
		  
	  public static long addDays(long date, int days) {
		    return (date+(long)days*24L*60L*60L*1000L);
	  }
	  
	  public static long addDays(long date, double days) {
		     return (date+(long)(days*24L*60L*60L*1000L));
	  }
	  
	  public static void addDays(java.util.Date date, double days) {
		     date.setTime(date.getTime()+(long)(days*24L*60L*60L*1000L));
	  }

	  public static void addWeekDaysIncl(java.util.Date date, int days) {
	     int adjDays = days/5*7+days%5;
	     if (adjDays != 0)
	        adjDays--;

	     addDays(date, adjDays);
	     date.setTime(date.getTime()+(long)adjDays*24L*60L*60L*1000L);
	     int wday = date.getDay();
	     int incr = 0;
	     if (wday ==0)
	        incr = 1;
	     else if (wday == 6)
	        incr = 2;
	     if (incr != 0)
	        addDays(date, incr);

	  }

	  public static void addQuarters(java.util.Date date, int qtrs) {
	     addMonths(date, qtrs*3);
	  }
	  public static String getPrintableQtr(java.util.Date date) {
	     StringBuilder buf = new StringBuilder();
	     buf.append("Q").append(date.getMonth()/3 + 1).append(" '").append(Integer.toString(date.getYear()+1900).substring(2));
	     return buf.toString();
	  }

	  public static String getPrintableDateSimple(java.util.Date date) {
	     if (date == null) return "";
	     return m_dateFormatter.format(date);
	     //return (Integer.toString(date.getMonth()+1) + "/" + Integer.toString(date.getDate()) + "/" + Integer.toString(date.getYear()+1900));
	  }

	  public static String getPrintableDateSimpleShort(java.util.Date date) {
	     return getPrintableDateSimple(date);
	     //if (date == null) return "";     
	     //return (Integer.toString(date.getMonth()+1) + "/" + Integer.toString(date.getDate()) + "/" + (Integer.toString(date.getYear()+1900)).substring(2));
	  }

	  public static String getPrintableDateSimpleMonthYear(java.util.Date date) {
	     if (date == null) return "";
	     return (Integer.toString(date.getMonth()+1) + "/" + (Integer.toString(date.getYear()+1900)).substring(2));
	  }

	  public static String getPrintableDate(java.util.Date date) {
	     if (date == null) return "";
	     return (date.toString());
	  }

	  public static String getMonYearString(java.util.Date date) {
	    if (date == null) return "";
	    int year = 1900+date.getYear();
	    if (year < 2000)
	       year -= 1900;
	    else
	       year -= 2000;
	    return Integer.toString(date.getMonth()+1)+"/"+(year < 10 ? "0"+Integer.toString(year) : Integer.toString(year));
	  }

	  public static int getDaysDiff(java.util.Date end_date, java.util.Date start_date) {
	    if (end_date == null || start_date == null)
	       return 0;

//	    long day1000Gap = (end_date.getTime() - start_date.getTime())/(24L*60L*60L);
	//add an hour so as to get around pesky little issues of 00 times
	     return (int)((end_date.getTime()+3600000L-start_date.getTime())/(24L*60L*60L*1000L));
	  }
	  public static int getDaysDiff(long end_date, long start_date) {
		    if (Misc.isUndef(end_date) || Misc.isUndef(start_date))
		       return 0;

//		    long day1000Gap = (end_date.getTime() - start_date.getTime())/(24L*60L*60L);
		//add an hour so as to get around pesky little issues of 00 times
		     return (int)((end_date+3600000L-start_date)/(24L*60L*60L*1000L));
		  }
	  public static int getDaysDiffIncl(java.util.Date end_date, java.util.Date start_date) {
	    if (end_date == null || start_date == null)
	       return 0;

//	    long day1000Gap = (end_date.getTime() - start_date.getTime())/(24L*60L*60L);
	//add an hour so as to get around pesky little issues of 00 times
	    int dur =(int)((end_date.getTime()+3600000L-start_date.getTime())/(24L*60L*60L*1000L));
	    if (dur > 1)
	       dur++;
	    return dur;
	  }

	  public static int getMonthsDiff(java.util.Date end_date, java.util.Date start_date) {
	     if (end_date == null || start_date == null)
	       return 0;
	     return (end_date.getYear()-start_date.getYear())*12+end_date.getMonth()-start_date.getMonth();
	  }

	  public static int getWeeksDiff(java.util.Date end_date, java.util.Date start_date) {
	     return getDaysDiff(end_date, start_date)/7;
	  }

	  

	  

	  public static String getPrintableCurrency(double value, int units) {
	     String retval = Double.toString(value/units);
	     return retval;
	  }

	  public static int[] getMultiSelectFromDB(ResultSet rset) throws SQLException { //rset needs to be positioned just at the beginning
	    //i.e. i will call rset.next() to get the first row
	    int temp[] = new int[100];
	    int curr = 0;
	    while (rset.next()) {
	       temp[curr++] = rset.getInt(1);
	    }
	    if (curr > 0) {
	       int retval[] = new int[curr];
	       for (int i=0;i<curr;i++)
	          retval[i] = temp[i];
	       return retval;
	    }
	    return null;
	  }

	  public static void  printCheckedBoxList(ResultSet rset, JspWriter out, int numCols ,String paramName) throws SQLException, IOException {
	     //first element should be ID and the 2nd element should be String, 3rd element if non-null means checked - the name
	     //doesn't print the beginning <table>, </table>
	     int colPrinted = numCols;
	     boolean firstRowPrinted = false;
	     while (rset.next()) {
	        String idStr = Misc.getRsetLongAsString(rset,1);
	        String nameStr = Misc.getRsetStringAsString(rset,2);
	        long dummy = rset.getLong(3);
	        boolean isChecked = !rset.wasNull();

	        if (colPrinted == numCols) {
	           if (firstRowPrinted) //close row
	              out.println("</tr>");
	           out.println("<tr>");
	           colPrinted = 0;
	        }
	        firstRowPrinted = true;
	        colPrinted++;
	        out.println("<td>"+nameStr+"</td>");
	        out.println("<td width=7><input type=\"checkbox\" name=\""+paramName+"\" value=\""+idStr+"\""+(isChecked?"checked":"")+"></td>");
	        out.println("<td width=7>&nbsp;</td>");
	     }
	     if (firstRowPrinted) {
	        for (int i=colPrinted;i<numCols;i++)
	           out.println("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
	        out.println("</tr>");
	     }
	  }

	 

	  

	  static public int printSelectOptions(Connection conn, String query,  int selId, JspWriter out) throws SQLException, IOException {
	      PreparedStatement pStmt = conn.prepareStatement(query);

	      ResultSet rset = pStmt.executeQuery();
	      int retval = printSelectOptions(rset, selId, out);
	      rset.close();
	      pStmt.close();
	      return retval;
	  }

	  static public int printSelectOptions(ResultSet rset, long selId, JspWriter out) throws SQLException, IOException {
	      //1st is String 2ns is value
	      int retval = (int) selId;
	      boolean isFirst = true;
	       while (rset.next()) {
	          String name = rset.getString(1);
	          long id = rset.getLong(2);
	          if (isFirst) {
	             retval = (int) id;
	             isFirst = false;
	          }
	          if (id == selId)
	             retval = (int) selId;

	          out.print("<OPTION value=\""+Long.toString(id)+"\""+(id == selId ?" Selected ":"") +">"+name+"</OPTION>");
	       }
	       return retval;
	  }



	  
	  public Misc() {
	    super();
	    System.out.println("Server Name : " + Misc.getServerName());
	  }
	 
	  public static long getNextId(Connection conn, String seqName) throws Exception {
		long retval = 0;
		Statement stmt = null;
		ResultSet rset = null;
	  try {
	  
	  if (Misc.G_DO_ORACLE)
	  {
	    StringBuilder queryString = new StringBuilder("select ");
	    queryString.append(seqName).append(".nextval from dual");
	    stmt = conn.createStatement();
	    rset = stmt.executeQuery(queryString.toString());
	    if (rset.next())
	    {
	      retval = rset.getLong(1);
	    }
	    rset.close();
	    stmt.close();
	    return(retval);
	    
	  }
	  else
	  {
	    retval = Misc.getUndefInt();
	    boolean mimicOrclSeq = Sequence.FILES.equals(seqName);
	    if (mimicOrclSeq) {
	       String stmtQuery = "insert into "+seqName + " (dummy_1) values (1)";
	       PreparedStatement cs = conn.prepareStatement(stmtQuery, Statement.RETURN_GENERATED_KEYS);
	       //cs.setInt(1,1);
	       //cs.setInt(2,12);
	       retval = Misc.executeGetId(cs, retval, stmtQuery);
	       cs.close();
	    }    
	    return retval;
	    
	  /*
	  	String str = "{call intelli.getNextId(?, ?)}";
			CallableStatement cstmt = conn.prepareCall(str);
			cstmt.setString(1, seqName);
			cstmt.registerOutParameter(2, Types.INTEGER);
			cstmt.execute();
			retval = cstmt.getInt(2);
	    cstmt.close();
	    return retval;
	  */
	    
	  }
			
	    }
	    catch (SQLException e) {
			if (rset != null) rset.close();
			if (stmt != null) stmt.close();
	    e.printStackTrace();
			throw e;
	    }
	    
	  }

	  public static void loadConstants(){
	  }

	  public static long[] parseIdArray(String s, int num) {
	     if (num == 0) return null;
	     long[] retval = new long[num];
	     StringTokenizer strtok = new StringTokenizer(s,"_",false);
	     int i = 0;
	     while(strtok.hasMoreTokens()) {
	        try {
	           retval[i++] = Long.parseLong(strtok.nextToken());
	        }
	        catch (Exception e) {
	        // something wrong with the cookie - let us just blindly proceed but log it.
	           e.printStackTrace();
	        }
	     }
	     return retval;
	  }


	  

	  

	  public static String getFormattedDate(java.util.Date date) {//old dont use
	     if (date != null)
	        return (date.toString());
	     else {
	        date = new java.sql.Date(System.currentTimeMillis());
	        return(date.toString());
	     }
	  }

	  public static String getFormattedDate(java.sql.Date date, int addDays) {
	     if (date != null)
	        return (date.toString());
	     else {
	        return(getTodayDate().toString());
	     }

	  }
	  

	  public static java.sql.Date getTodayDate() {
	      return (new java.sql.Date(System.currentTimeMillis()));
	  }
	  public static void putPlayback(Connection conn, int tripId) {
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String baseImageFullName = "c:\\temp\\test2_40.png";
		  double lx = 86.05245979;
		  double ux = 86.97874458;
		  double ly = 23.348007446;
		  double uy = 23.831281774;
		  double xfactor = ux-lx;
		  double yfactor = uy-ly;
		  Graphics2D graphics = null;
		  try {
			  SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm");
			  ps = conn.prepareStatement("select vehicle.name, vehicle.id, lop.name, load_gate_in, load_gate_out, uop.name, unload_gate_in, unload_gate_out from trip_info join vehicle on (vehicle.id = trip_info.vehicle_id) left outer join op_station lop on (lop.id=load_gate_op) left outer join op_station uop on (uop.id = unload_gate_op) where trip_info.id=?");
			  ps.setInt(1, tripId);
			  rs = ps.executeQuery();
			  java.util.Date lin = null;
			  java.util.Date lout = null;
			  java.util.Date uin = null;
			  java.util.Date uout = null;
			  String vehicleName = null;
			  String lopName = null;
			  String uopName = null;
			  int vehicleId = Misc.getUndefInt();
			  if (rs.next()) {
				  vehicleName = rs.getString(1);
				  vehicleId = Misc.getRsetInt(rs, 2);
				  lopName = rs.getString(3);
				  lin = Misc.sqlToUtilDate(rs.getTimestamp(4));
				  lout = Misc.sqlToUtilDate(rs.getTimestamp(5));
				  uopName = rs.getString(6);
				  uin = Misc.sqlToUtilDate(rs.getTimestamp(7)); 
				  uout = Misc.sqlToUtilDate(rs.getTimestamp(8));
			  }
			  rs  = Misc.closeRS(rs);
			  ps = Misc.closePS(ps);
			  if (lout != null && uin != null) {
				  
				  BufferedImage baseImage = ImageIO.read(new File(baseImageFullName));
				  int width = baseImage.getWidth(); //x
				  int height = baseImage.getHeight();//y
				  
				  
				  ps = conn.prepareStatement("select longitude, latitude, gps_record_time, attribute_value from logged_data where vehicle_id=? and attribute_id=0 and gps_record_time between ? and ? order by gps_record_time");
				  ps.setInt(1, vehicleId);
				  ps.setTimestamp(2, Misc.utilToSqlDate(lout));
				  ps.setTimestamp(3, Misc.utilToSqlDate(uin));
				  rs = ps.executeQuery();
				  ArrayList<Integer> lonList = new ArrayList<Integer>();
				  ArrayList<Integer> latList = new ArrayList<Integer>();
				  
				  while (rs.next()) {
					  double lon = Misc.getRsetDouble(rs, 1);
					  double lat = Misc.getRsetDouble(rs,2);
					  if (Misc.isUndef(lon) || Misc.isUndef(lat))
						  continue;
					  int adjLon = (int)(Math.round((lon-lx)/xfactor*width));
					  int adjLat = (int)(Math.round((lat-ly)/yfactor*height));
					  int lonInCoord = adjLon-4; //top left of screen is origin and goes right and down, while adjLat goes up
					  int latInCoord = height-adjLat;
					  if (lonInCoord < 0)
						  lonInCoord = 0;
					  if (lonInCoord >= width)
						  lonInCoord = width-1;
					  if (latInCoord < 0)
						  latInCoord = 0;
					  if (latInCoord >= height)
						  latInCoord = height;
					  lonList.add(lonInCoord);
					  latList.add(latInCoord);
					  
				  }
				  rs = Misc.closeRS(rs);
				  ps = Misc.closePS(ps);
				  
				  graphics = baseImage.createGraphics();
				  BasicStroke stroke = new BasicStroke(2);
				  graphics.setStroke(stroke);
				  Font font = new Font("Arial", Font.BOLD, 12);
				  graphics.setFont(font);
				  graphics.setColor(Color.BLUE);
				  if (lonList.size() > 0) {
					  int[] lonArray = new int[lonList.size()];
					  int[] latArray = new int[latList.size()];
					  for (int i1=0,i1s=lonList.size(); i1<i1s;i1++) {
						  lonArray[i1] = lonList.get(i1);
						  latArray[i1] = latList.get(i1);
					  }
					  graphics.drawPolyline(lonArray, latArray, lonList.size());
				  }
				  String label1 = vehicleName + " ["+sdf.format(lout)+" - " + sdf.format(uin)+"]";
				  AttributedString as1 = new AttributedString(label1);
				    as1.addAttribute(TextAttribute.BACKGROUND, Color.WHITE, 0, label1.length());
				  graphics.drawString(as1.getIterator(), width-300, 20);
				  //AffineTransform at = AffineTransform.getScaleInstance(0.33, 0.33);
			      //graphics.drawRenderedImage(baseImage, at);
				  ImageIO.write(baseImage, "PNG", new File("c:\\temp\\test_rw5.png"));
			  }
			  
		  }
		  catch (Exception e) {
			  e.printStackTrace();
		  }
		  finally {
			  rs = Misc.closeRS(rs);
			  ps = Misc.closePS(ps);
			  try {
				  if (graphics != null)
					  graphics.dispose();
			  }
			  catch (Exception e) {
				  
			  }
		  }  
	  }
	  public static void main(String[] args) {
		  int tripId = 811518;
		  
		  Connection conn =  null;
		  try {
			  conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			  putPlayback(conn, tripId);
		  }
		  catch (Exception e) {
			  e.printStackTrace();
		  }
		  finally {
			  if (conn != null) {
				  try {
					  DBConnectionPool.returnConnectionToPoolNonWeb(conn);
				  }
				  catch (Exception e2) {
					  
				  }
				  conn = null;
			  }
		  }
	  }
	  
	  public static boolean isSameIpAsMe(String ipAddress) {
		  try {
			  if ("localhost".equals(ipAddress) || "127.0.0.1".equals(ipAddress)) 
				  return true;
				Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();
				while(network.hasMoreElements()) {
					Enumeration<InetAddress> inetAddresses = network.nextElement().getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress inetAddress = inetAddresses.nextElement();
						String meIp = inetAddress.getHostAddress();
						if (meIp == null || !meIp.equals(ipAddress)) 
							continue;
						return true;
					}
				}
		  }
		  catch (Exception e) {
			  e.printStackTrace();
			  //eat it
		  }			
		  return false;
	  }
	  
	  public static boolean isSameDate(java.util.Date date1, java.util.Date date2) {
	     if (date1== null && date2 == null)
	        return true;
	     if (date1 == null || date2 == null) //got it? if doing and check first ensures that this will evaluae only if one of them is true
	        return false;
	      return (date1.getMonth() == date2.getMonth()) && (date1.getYear() == date2.getYear()) && (date1.getDate() == date2.getDate());
	  }

	  public static boolean m_doingMultiTime = true;

	  public static java.text.DecimalFormat m_formatter = new java.text.DecimalFormat("###,###.##");
	  public static java.text.DecimalFormat frac_formatter = new java.text.DecimalFormat("0.000");
	  public static java.text.DecimalFormat perc_formatter = new java.text.DecimalFormat("0");
	  public static java.text.DecimalFormat m_currency_formatter = new java.text.DecimalFormat("0.00");
	  private static java.text.SimpleDateFormat m_dateFormatter = new java.text.SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
	  public static java.text.SimpleDateFormat m_indepFormatterFull = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
	  public static String indepDateFormat(java.util.Date dt) {
		  
		  synchronized (m_indepFormatterFull) {
			  return m_indepFormatterFull.format(dt);
		  }
	  }
	 

	  public static int DEBUG_LEVEL = -10;
	  public static Document saveXMLInp(HttpServletRequest request) throws Exception {
	      return saveXMLInp(request, "jinp.xml");
	  }

	  public static Document saveXMLInp(HttpServletRequest request, String fileName) throws Exception {
	     try {
	    	 
	         java.io.InputStream inp = request.getInputStream();
	         FileWriter fout = null;
	         PrintWriter outw = null;
	         if (false && DEBUG_LEVEL <= 0) {
	            fout = new FileWriter(getServerConfigPath()+"\\intellitestoutput\\"+fileName);
	            outw = new PrintWriter(fout, true);
	         }
	         com.ipssi.gen.utils.MyXMLHelper helper = new com.ipssi.gen.utils.MyXMLHelper(inp,outw);
	         org.w3c.dom.Document xmlDoc = helper.load();
	         if (false && DEBUG_LEVEL <= 0) {
	            helper.save(xmlDoc);
	            outw.close();
	         }
	         return xmlDoc;
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }
	  // Added rajeev 021605
	  public static String getUserFilesSavePath() {
	     return CFG_USERFILES_SAVE;
	  }

	  public static String getUserFilesVirtualPath() {
	     return CFG_USERFILES_VP;
	  }


	  public  static String getServerConfigPath() { //make sure to update getRootPathJScript
	       if (CFG_CONFIG_SERVER == null) {
	    	   loadCFGConfigServerProp();			
		   }
		  return CFG_CONFIG_SERVER;
	  }

	  public synchronized static void setCFG_CONFIG_SERVER(String cfg_config_server) {
		CFG_CONFIG_SERVER = cfg_config_server;
	  }

	  public static String getWebConfigPath() {
	     return CFG_CONFIG_WEB;
	  }

	  public static String getWebConfigVirtualPath() {
	     return CFG_CONFIG_WEB_VP;
	  }


	  public static String getBASE_CFG_CONFIG_SERVER() {
		return BASE_CFG_CONFIG_SERVER;
	  }
	  
	 // public static Connection getDataBackUpConnection() {
	//	  if (DATABACKUPSERVERCONN == null) {
	//		loadCFGConfigServerProp();
	//	}
	//	return DATABACKUPSERVERCONN;
	//}
	  
	public static int getSERVER_MODE() {
		if (SERVER_MODE == Misc.UNDEF_VALUE) {
			loadCFGConfigServerProp();
		}
		return SERVER_MODE;
	}
	
	public static String getINSTANCE_NAME() {
		return INSTANCE_NAME;
	}
	public static void setINSTANCE_NAME(String instance_name) {
		INSTANCE_NAME = instance_name;
	}
	public static String getG_BACKUP_SERVER_NAME() {
	      if (G_BACKUP_SERVER_NAME == null) {
			loadCFGConfigServerProp();
		}	
		return G_BACKUP_SERVER_NAME;
	}
	public static void closeBACKUP_MODE_SERVER_DB_CONN() {
		try {
			if (BACKUP_MODE_SERVER_DB_CONN != null)
				BACKUP_MODE_SERVER_DB_CONN.close();
		}
		catch (Exception e) {
		}
		finally {
			BACKUP_MODE_SERVER_DB_CONN = null;
		}
	}
	public static boolean testConn(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select 1");
			rs = ps.executeQuery();
			return true;
		}
		catch (Exception e) {
			
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e) {
				
			}
		}
		return false;
	}
	
	public static Connection getBackupFreshConn() { //use this in multi request/multi thread mode
		loadCFGConfigServerProp();
		Connection retval = null;
		String connURL = "jdbc:mysql://"+ BACKUP_MODE_SERVER_HOST + ":" + BACKUP_MODE_SERVER_PORT +"/"+BACKUP_MODE_SERVER_DB_NAME
		+"?zeroDateTimeBehavior=convertToNull&"
		;
		//+"?user="+BACKUP_MODE_SERVER_USER+"&password="+BACKUP_MODE_SERVER_PASSWORD;
		try{
			  retval = DriverManager.getConnection(connURL,BACKUP_MODE_SERVER_USER,BACKUP_MODE_SERVER_PASSWORD);
		}
		catch (Exception e) {
			System.out.println("Failed connection to backup server database:"+connURL);
			e.printStackTrace();
		}
		return retval;
	}
	
	public static Connection getBACKUP_MODE_SERVER_DB_CONN() {
		if (BACKUP_MODE_SERVER_DB_CONN != null) {
			if (!testConn(BACKUP_MODE_SERVER_DB_CONN))
				Misc.closeBACKUP_MODE_SERVER_DB_CONN();
		}
		if (BACKUP_MODE_SERVER_DB_CONN != null) {
			return BACKUP_MODE_SERVER_DB_CONN;
		}
		loadCFGConfigServerProp();
	  
		String connURL = "jdbc:mysql://"+ BACKUP_MODE_SERVER_HOST + ":" + BACKUP_MODE_SERVER_PORT +"/"+BACKUP_MODE_SERVER_DB_NAME+"?user="+BACKUP_MODE_SERVER_USER+"&password="+BACKUP_MODE_SERVER_PASSWORD;
		try{
			  BACKUP_MODE_SERVER_DB_CONN = DriverManager.getConnection(connURL);
		}
		catch (Exception e) {
			System.out.println("Failed connection to backup server database:"+connURL);
		}
		return BACKUP_MODE_SERVER_DB_CONN;
	}
	public static void closeJMS_DB_CONN() {
		try {
			if (JMS_SERVER_DB_CONN != null)
				JMS_SERVER_DB_CONN.close();
		}
		catch (Exception e) {
			
		}
		finally {
			JMS_SERVER_DB_CONN = null;
		}
	}
	public static Connection getJMS_DB_CONN() {
		if (JMS_SERVER_DB_CONN != null)
			return JMS_SERVER_DB_CONN;
		loadCFGConfigServerProp();

		String connURL = "jdbc:mysql://"+ JMS_SERVER_HOST + ":" + JMS_SERVER_PORT +"/"+JMS_SERVER_DB_NAME+"?user="+JMS_SERVER_USER+"&password="+JMS_SERVER_PASSWORD;
		try{
			JMS_SERVER_DB_CONN = DriverManager.getConnection(connURL);
		}
		catch (Exception e) {
			System.out.println("Failed connection to JMS server database:"+connURL);
		}
		return JMS_SERVER_DB_CONN;
	}
	
	public static String getBACKUP_MODE_SERVER_DB_NAME() {
		if (BACKUP_MODE_SERVER_DB_NAME == null) {
			loadCFGConfigServerProp();
		}
		return BACKUP_MODE_SERVER_DB_NAME;
	}
	public static String getFilePathToRecoveryDataInDP(){
		if (DATA_RECOVERY_FILE_LOCATION == null) {
			loadCFGConfigServerProp();
		}
		return DATA_RECOVERY_FILE_LOCATION;
	}
	public static int getDPRecoMaxThread() {
		loadCFGConfigServerProp();
		return G_DPRECO_THREAD;
	}
	
	public static int getDPLogMaxThread() {
		loadCFGConfigServerProp();
		return G_DPLOG_THREAD;
	}
	
	public synchronized static void loadCFGConfigServerProp(){
	 	if (G_CONFIG_SERVER_LOAD) {
			return;
		}
	 	System.out.println("Loading CFG Server Prop");
	 	  Properties prop = new Properties();
		  String instanceName = Misc.getServerName();
		  try {
			  System.out.println("@#@#@"+CONN_PROPERTY+"@#@#@#"+SYSTEM_BASE_CFG_SERVER);
			  prop.load(new BufferedInputStream(new FileInputStream(CONN_PROPERTY)));
			  newConnProp = prop;
			  //set config server path
			  String suffix = prop.getProperty(instanceName+".CFG_CONFIG_SERVER_SUFFIX");
			  if (suffix == null)
				  CFG_CONFIG_SERVER = BASE_CFG_CONFIG_SERVER;
			  else
				  CFG_CONFIG_SERVER = BASE_CFG_CONFIG_SERVER +""+prop.getProperty(instanceName+".CFG_CONFIG_SERVER_SUFFIX");
			  //set server mode 
			  g_doAREACODE = "1".equals(prop.getProperty("DO_AREACODE"));
			  INSTANCE_NAME = prop.getProperty(instanceName+".INSTANCE_NAME");
			  SERVER_MODE = Misc.getParamAsInt(prop.getProperty(instanceName+".backupmode"),0);
			  emailReportServer = prop.getProperty("EMAIL_REPORT_SERVER","http://203.197.197.18:9180/");
			  String prodDB  = prop.getProperty(instanceName+".DBConn.Database","ipssi2");
				 String prodHost = prop.getProperty(instanceName+".DBConn.host", "127.0.0.1");
				 String prodPort = prop.getProperty(instanceName+".DBConn.port", "3306");
				 String prodUser = prop.getProperty(instanceName+".DBConn.userName", "jboss");
				 String prodPassword = prop.getProperty(instanceName+".DBConn.password", "redhat");
				 G_BACKUP_SERVER_NAME = prop.getProperty(instanceName+".backupServer.name");
				 BACKUP_MODE_SERVER_DB_NAME = prop.getProperty(G_BACKUP_SERVER_NAME+".DBConn.Database",prodDB);
				 BACKUP_MODE_SERVER_HOST = prop.getProperty(G_BACKUP_SERVER_NAME+".DBConn.host", prodHost);
				 BACKUP_MODE_SERVER_PORT = prop.getProperty(G_BACKUP_SERVER_NAME+".DBConn.port", prodPort);
				 BACKUP_MODE_SERVER_USER = prop.getProperty(G_BACKUP_SERVER_NAME+".DBConn.userName", prodUser);
				 BACKUP_MODE_SERVER_PASSWORD = prop.getProperty(G_BACKUP_SERVER_NAME+".DBConn.password", prodPassword);
				 
			  if (SERVER_MODE == 0) {
				  G_DPRECO_THREAD = Misc.getParamAsInt(prop.getProperty("GLOBAL.DPRECO.maxThread","1"),1);
				  G_DPLOG_THREAD = Misc.getParamAsInt(prop.getProperty("GLOBAL.DPLOG.maxThread","1"),1);
				  g_rptpInitSeq = Misc.getParamAsInt(prop.getProperty("GLOBAL.rptp_sequence"),3);
				  
				  String fileName = prop.getProperty(instanceName+".dataRecovery.file");
				  if (fileName == null) {
					  DATA_RECOVERY_FILE_LOCATION = prop.getProperty("default.dataRecovery.file");
				  }
				  else
					  DATA_RECOVERY_FILE_LOCATION = fileName;
				 
				 JMS_SERVER_DB_NAME = prop.getProperty(instanceName+".jms"+".DBConn.Database",prodDB);
				 JMS_SERVER_HOST = prop.getProperty(instanceName+".jms"+".DBConn.host", prodHost);
				 JMS_SERVER_PORT = prop.getProperty(instanceName+".jms"+".DBConn.port", prodPort);
				 JMS_SERVER_USER = prop.getProperty(instanceName+".jms"+".DBConn.userName", prodUser);
				 JMS_SERVER_PASSWORD = prop.getProperty(instanceName+".jms"+".DBConn.password", prodPassword);
			  }	
			  boolean doShortACBLikeNewVehicleParams = "1".equals(prop.getProperty(instanceName+".do_short_cache","0")) || "node_bsnl".equals(instanceName);
			  if (doShortACBLikeNewVehicleParams) {

				  NewVehicleData.g_dcAtMostXMillisecBehind = (long)(1*10*60*60*1000);
				  NewVehicleData.g_backPtsAllowedMillisecBehind = (long)(1*10*60*60*1000);
				  NewVehicleData.g_maxTimeOfDataAllowedMilli = (long)(1*10*60*60*1000);
			  }
			 //Below is backward compatability ... in case parameters are not set up ... but if parameters 
			 //are set up then we need not worry about adding specific stuff
			 String serverName = Misc.getServerName();
			if ("demo".equalsIgnoreCase(serverName)) {
				g_jniPort = 1299;
			}
			if ("backup_demo".equalsIgnoreCase(serverName)) {
				g_jniPort = 1299;//TODO
			}
			else if ("node_lafarge".equalsIgnoreCase(serverName)) {
				g_jniPort = 1499;
			}
			else if ("backup_lafarge".equalsIgnoreCase(serverName)) {
				g_jniPort = 1699;
			}
			else if ("node_gw".equalsIgnoreCase(serverName)) {
				g_jniPort = 1399;
			}
			else if ("backup_gw".equalsIgnoreCase(serverName)) {
				g_jniPort = 1599;
			}			
			else if ("node_west".equalsIgnoreCase(serverName)) {
				g_jniPort = 1799;
			}
			else if ("backup_west".equalsIgnoreCase(serverName)) {
				g_jniPort = 1899;
			}
			else if ("node_maithon".equalsIgnoreCase(serverName)) {
				g_jniPort = 1999;
			}
			else if ("backup_maithon".equalsIgnoreCase(serverName)) {
				g_jniPort = 2099;
			}
			else if ("node_gati".equalsIgnoreCase(serverName)) {
				g_jniPort = 2399;
			}
			else if ("backup_gati".equalsIgnoreCase(serverName)) {
				g_jniPort = 2499;
			}
			else if ("default".equalsIgnoreCase(serverName)) {
				g_jniPort = 1099;
			}
			else if ("node1".equalsIgnoreCase(serverName)) {
				g_jniPort = 1099;//TODO
			}
			else if ("node_stag".equalsIgnoreCase(serverName)) {
				g_jniPort = 2599;
			}else if ("node_bsnl".equalsIgnoreCase(serverName)) {
				g_jniPort = 2699;
			}else if ("node_bcl".equalsIgnoreCase(serverName)) {
				g_jniPort = 2799;
			}else if ("node_test2".equalsIgnoreCase(serverName)) {
				g_jniPort = 2999;
			}else if ("node_test3".equalsIgnoreCase(serverName)) {
				g_jniPort = 3099;
			}else if ("node_test4".equalsIgnoreCase(serverName)) {
				g_jniPort = 3199;
			}
			else {
				g_jniPort = 1099;
			}
			g_mtnioServerUDPPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 7205 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName))  ? 7105 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 7005 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 6905 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 6805 
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 7305 
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 6705: g_mtnioServerUDPPort ;
			g_mtnioCurrentPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5805 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5705 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5605 
					: ("node_gw".equalsIgnoreCase(serverName) ||"backup_gw".equalsIgnoreCase(serverName) )? 5505 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 5405
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5905
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5305 :  g_mtnioCurrentPort;
			g_mtnioDataPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5830 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5730 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5630 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 5530 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 5430
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5930
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5330 :   g_mtnioDataPort ;
			g_mtnioNovireCurrentPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5820 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5720 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5620 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 5520 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 5220
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5920
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5320  : g_mtnioNovireCurrentPort;
			g_mtnioMeitrack310Port = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5806 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5706 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5606 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 5506 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName)) ? 5406
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5906
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5306 : g_mtnioMeitrack310Port;
			g_mtnioTeltonikaRegPort =("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5810 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5710 
					:  ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5610 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 5510 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 5210
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5910
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5310  : g_mtnioTeltonikaRegPort;
			g_mtnioGalileoRegPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 11810 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 11710 
							:  ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 11610 
							: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 11510 
							: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 11210
							: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 11910
							: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 11310  : g_mtnioGalileoRegPort;
			g_mtnioPhytecPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 12810 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 12710 
							:  ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 12610 
							: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 12510 
							: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 12210
							: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 12910
							: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 12310  : g_mtnioPhytecPort;
			g_mtnioTeltonikaConfigPort =("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5808 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5708 
					:  ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5608 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 5508 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName) ) ? 5208
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5908
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5308  : g_mtnioTeltonikaConfigPort;
			g_mtnioFastTrackPort = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 6671 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 6670 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) ) ? 6669 
					: ("node_gw".equalsIgnoreCase(serverName) || "backup_gw".equalsIgnoreCase(serverName))? 6668 
					: ("node_lafarge".equalsIgnoreCase(serverName) ||"backup_lafarge".equalsIgnoreCase(serverName) ) ? 6667
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 6672
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 6666  : g_mtnioFastTrackPort;
			g_mtnioATrackAY5Port = ("node_gati".equalsIgnoreCase(serverName) ||"backup_gati".equalsIgnoreCase(serverName) ) ? 5815 
					: ("node_maithon".equalsIgnoreCase(serverName) ||"backup_maithon".equalsIgnoreCase(serverName) ) ? 5715 
					: ("node_west".equalsIgnoreCase(serverName) ||"backup_west".equalsIgnoreCase(serverName) )? 5615 
					: ("node_gw".equalsIgnoreCase(serverName) ||"backup_gw".equalsIgnoreCase(serverName) )? 5515 
					: ("node_lafarge".equalsIgnoreCase(serverName) || "backup_lafarge".equalsIgnoreCase(serverName))? 5415
					: ("node_stag".equalsIgnoreCase(serverName) || "backup_stag".equalsIgnoreCase(serverName))? 5915
					: ("demo".equalsIgnoreCase(serverName) || "backup_demo".equalsIgnoreCase(serverName)) ? 5315 :  g_mtnioATrackAY5Port;
			g_mtnioAISPort = ("node_test".equalsIgnoreCase(serverName) ||"backup_test".equalsIgnoreCase(serverName) ) ? 13142
			 : ("node_test2".equalsIgnoreCase(serverName) ||"backup_test2".equalsIgnoreCase(serverName) ) ? 13410
			 : 13411
			 ;

			g_mtnioACTeckPort = ("node_bsnl".equalsIgnoreCase(serverName) ||"backup_bsnl".equalsIgnoreCase(serverName) ) ? 5207 
					:  g_mtnioACTeckPort;
			Misc.g_doOrgAssignmentTimeBased = "1".equals(prop.getProperty(instanceName+".do_org_assignment_time_based"));
			g_doDalaUp = Misc.getParamAsInt(prop.getProperty(instanceName+".do_dala"),2);
			g_jniPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.jni"), g_jniPort);
			g_CBSEPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.cbse"), g_CBSEPort);
			g_mtnioServerUDPPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.udp"), g_mtnioServerUDPPort);
			g_mtnioCurrentPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.current"), g_mtnioCurrentPort);
			g_mtnioDataPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.data"), g_mtnioDataPort);
			g_mtnioNovireCurrentPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.novire_current"), Misc.getParamAsInt(prop.getProperty(instanceName+".port.novire"),g_mtnioNovireCurrentPort));
			g_mtnioMeitrack310Port = Misc.getParamAsInt(prop.getProperty(instanceName+".port.meitrack310"), g_mtnioMeitrack310Port);
			g_mtnioTeltonikaRegPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.teltonika_regular"), g_mtnioTeltonikaRegPort);
			g_mtnioGalileoRegPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.galileo"), g_mtnioGalileoRegPort);
			g_mtnioPhytecPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.phytec"), g_mtnioPhytecPort);
			
			g_mtnioTeltonikaConfigPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.teltonika_config"), g_mtnioTeltonikaConfigPort);
			g_mtnioFastTrackPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.fasttrack"), g_mtnioFastTrackPort);
			g_mtnioATrackAY5Port = Misc.getParamAsInt(prop.getProperty(instanceName+".port.ay5"), g_mtnioATrackAY5Port);
			g_mtnioACTeckPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.acteck"), g_mtnioACTeckPort);
			g_mtnioAndroidPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.android"), g_mtnioAndroidPort);
			g_conCoxPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.concox"), g_conCoxPort);
			g_rpPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.rp"), g_rpPort);
			g_mtnioAISPort =  Misc.getParamAsInt(prop.getProperty(instanceName+".port.ais"), g_mtnioAISPort);
			g_gdcPort = Misc.getParamAsInt(prop.getProperty(instanceName+".port.gdc"), g_gdcPort);
			;

			String uidPrefix = prop.getProperty(instanceName+".uidprefix");
			if (uidPrefix != null)
				uidPrefix = uidPrefix.trim();
			if (uidPrefix == null || uidPrefix.length() == 0)
				uidPrefix = null;
			Misc.g_uidPrefix = uidPrefix;
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			g_hasvehicle_org_time_assignments = false;
			boolean destroyIt = false;
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				ps = conn.prepareStatement("select min(vehicle_id) from vehicle_org_time_assignments");
				rs = ps.executeQuery();
				if (rs.next()) {
					
				}
				g_hasvehicle_org_time_assignments = true;
			}
			catch (Exception e2) {
				//eat it
				g_hasvehicle_org_time_assignments = false;
				destroyIt = true;
			}
			finally {
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				try {
					if (conn != null)
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e3) {
					
				}
			}
			  G_CONFIG_SERVER_LOAD = true;
			  if (!Misc.g_hasvehicle_org_time_assignments) {
				  Misc.g_doOrgAssignmentTimeBased = false;
			  }
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }
	
	public static java.util.Date getFirstOfMonth(java.util.Date thisDate) {
	     java.util.Date retval = new java.util.Date (thisDate.getTime());
	     retval.setDate(1);
	     return retval;
	  }

	  public static java.util.Date getLastOfMonth(java.util.Date thisDate) {
	     java.util.Date retval = getFirstOfMonth(thisDate);
	     addMonths(retval, 1);
	     addDays(retval, -1);
	     return retval;
	  }

	  public static boolean isTBDDate(java.util.Date thisDate) {
	     return getMonthsDiff(thisDate, getCurrentDate()) > 240;
	  }

	  public static int getWorkingDurInDays(java.util.Date start, java.util.Date end) {
	      if (start == null || end == null)
	         return 20;
	      String debug1;
	      long debug2;
	      java.util.Date startDate = new java.util.Date (start.getTime());
	      debug1 = Misc.getPrintableDateSimple(startDate);
	      java.util.Date endDate = new java.util.Date (end.getTime());
	      debug1 = Misc.getPrintableDateSimple(endDate);
	      int dayOfWeek = startDate.getDay();
	      debug2 = endDate.getTime();
	      addDays(endDate, 1);
	      debug1 = Misc.getPrintableDateSimple(endDate);
	      debug2 = endDate.getTime();
	      if (dayOfWeek == 0)
	         addDays(startDate,1);
	      else if (dayOfWeek == 6)
	         addDays(startDate, 2);
	      debug1 = Misc.getPrintableDateSimple(startDate);
	      int diffInDays = getDaysDiff(endDate, startDate);
	      long debug10 = startDate.getTime();
	      long debug11 = endDate.getTime();
	      long debug12 = debug11 - debug10;
	      long debug13 = debug12/(24*60*60);
	      int wk = diffInDays/7;
	      debug2 = startDate.getTime();
//	      startDate.setTime(endDate.getTime());
//	      addDays(startDate, -1*(diffInDays%7));
	      addDays(startDate, wk*7);
	      long debug20 = Long.MAX_VALUE;
	      long debug21 = Long.MIN_VALUE;
	      int addnlDays = 0;
	      long debug3 = startDate.getTime();
	      long debug4 = (debug3 - debug2)/24*60*60*1000;
	      debug1 = Misc.getPrintableDateSimple(startDate);
	      while (getDaysDiff(endDate, startDate) > 0) {

	         debug1 = Misc.getPrintableDateSimple(startDate);
	         dayOfWeek = startDate.getDay();
	         if (dayOfWeek > 0 && dayOfWeek < 6)
	           addnlDays++;
	         addDays(startDate, 1);
	      }
	      return wk*5+addnlDays;
	  }
	  /*
		    * Code addition -- Sameer 02082005
		    */

		   public static String getMonVerbalAndYearString(java.util.Date date) {
	       java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMM-yy");
	       return (formatter.format(date));
		   }

		   /*
		    * End of code addition -- Sameer 02082005
	      */
	    //rajeev 031606
	    public static void  printCheckedBoxList(ResultSet rset, DimInfo dimInfo, JspWriter out, int numCols ,String paramName) throws SQLException, IOException {
	       ArrayList currList = new ArrayList();
	       
	       while (rset.next())
	          currList.add(new Integer(rset.getInt(1)));
	       int colPrinted = numCols;
	       boolean firstRowPrinted = false;
	       ArrayList valList = dimInfo.getValList();
	       for (int v=0,vs = valList == null?0:valList.size();v<vs;v++) {
	          DimInfo.ValInfo valInfo = (DimInfo.ValInfo)valList.get(v);
	          if (colPrinted == numCols) {
	           if (firstRowPrinted) //close row
	              out.println("</tr>");
	              out.println("<tr>");
	              colPrinted = 0;
	           }
	           firstRowPrinted = true;
	           colPrinted++;
	           String nameStr = valInfo.m_name;
	           String idStr = Integer.toString(valInfo.m_id);
	           int id = valInfo.m_id;
	           boolean isChecked = currList.contains(new Integer(id));
	           out.println("<td width=7><input type=\"checkbox\" name=\""+paramName+"\" value=\""+idStr+"\""+(isChecked?"checked":"")+"></td>");
	           out.println("<td>"+nameStr+"</td>");
	           out.println("<td width=7>&nbsp;</td>");
	       }
	       if (firstRowPrinted) {
	        for (int i=colPrinted;i<numCols;i++)
	           out.println("<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>");
	        out.println("</tr>");
	       }
	    }
	  public static void  printCheckedBoxList(Cache cache, Connection conn, String query, int refId, String elemName, JspWriter out, int numCols ,String paramName) throws SQLException, IOException {
	     //first element should be ID
	     PreparedStatement pStmt = conn.prepareStatement(query);
	     pStmt.setInt(1, refId);
	     ResultSet rset = pStmt.executeQuery();
	     DimInfo dimInfo = DimInfo.getDimInfo(elemName);
	     printCheckedBoxList(rset, dimInfo, out, numCols, paramName);
	     rset.close();
	     pStmt.close();
	  }

	  public static void saveCheckedBox(CallableStatement cStmt, HttpServletRequest request, String paramName) throws SQLException {
	     String parameterValues[] = request.getParameterValues(paramName);
	     for (int i=0,is = parameterValues == null ? 0 : parameterValues.length;i<is;i++) {
	        int v = com.ipssi.gen.utils.Misc.getParamAsInt(parameterValues[i]);
	        cStmt.setInt(2, v);
	        cStmt.execute();
	     }
	  }

	  public static void saveCheckedBox(Connection conn, String query, String delQuery, HttpServletRequest request, int refId, String paramName) throws SQLException {
	     CallableStatement cStmt = conn.prepareCall(delQuery);
	     cStmt.setInt(1, refId);
	     cStmt.execute();
	     cStmt.close();

	     cStmt = conn.prepareCall(query);
	     cStmt.setInt(1,refId);
	     saveCheckedBox(cStmt, request, paramName);
	     cStmt.close();

	  }

	  public static String getPrintableInt(int val) { return Misc.isUndef(val) ? "" : Integer.toString(val);} //dont use
	  public static String getPrintablePercent(double val) { return Misc.isUndef(val) ? "" : perc_formatter.format(val*100);} //dont use
	  public static String getPrintableDouble(double val) { return Misc.isUndef(val) ? "" : m_currency_formatter.format(val);} //dont use
	  public static String getPrintableString(String val) { return val == null ? "":val;} //dont use

		// sameer 04032006
		
	     //rajeev 062506
	     public static boolean g_fteDataIgnoresWeekend = false;
	     public static int getWorkingDaysDiff(java.util.Date endDate, java.util.Date startDate, boolean doIncl) {
	   	   int factor = 1;
	        if (startDate == null || endDate == null)
	           return 0;
		   if (startDate.after(endDate)) {
			java.util.Date temp = startDate;
			startDate = endDate;
			endDate = temp;
			factor = -1;
	  	   }
		   int numDays = getDaysDiff(endDate, startDate);
	        if (doIncl)
	           numDays++;
		   int wholeWeeks = numDays/7;
		   int remainingDays = numDays - wholeWeeks * 7;
		   int remainingWDays = remainingDays;
		   if (remainingDays != 0) {

			int wday = startDate.getDay();
			for (int t1=0;t1<remainingDays;t1++) {
				int nwday = wday+t1;
				if (nwday == 0 || nwday == 6) {
					remainingWDays--;
				}
			}
		   }
		   return factor*(wholeWeeks*5+remainingWDays);
	   }


	   public static int getDurationIncl(java.util.Date endDate, java.util.Date startDate) {
	       if (endDate == null || startDate == null)
	          return 0;

	       return g_fteDataIgnoresWeekend ? getDaysDiff(endDate, startDate)+1 : getWorkingDaysDiff(endDate, startDate, true);
	   }

	   public static int getDurationExcl(java.util.Date endDate, java.util.Date startDate) {
	       if (endDate == null || startDate == null)
	          return 0;
	       return g_fteDataIgnoresWeekend ? getDaysDiff(endDate, startDate) : getWorkingDaysDiff(endDate, startDate, false);
	   }

	  
	   public static String getFormattedVal(double f, java.text.DecimalFormat fmt, String undef) {
	      if (Misc.isUndef(f))
	         return undef;
	      return fmt.format(f).toString();

	   }
	   public static String doubleToString(double f,  String undef) {
	      if (Misc.isUndef(f))
	         return undef;
	      return Double.toString(f);

	   }

	   public static String getPathAdjName(String fname, String target) { //not sure if relevance to track
	        if (fname != null) {
	            fname = fname.toLowerCase();

	        if (fname.startsWith("c:\\iconfig\\"))  {
	            fname = fname.substring("c:\\iconfig\\".length());
	            target = "iconfig";
	        }
	        else if (fname.startsWith("file://c:\\iconfig\\"))  {
	            fname = fname.substring("file://c:\\iconfig\\".length());
	            target = "iconfig";
	        }
	        else if (fname.startsWith("file://"))  {
	            fname = fname.substring("file://".length());
	            return fname;
	        }

	            if (target == null)
	               target = "server";
	            target = target.toLowerCase();
	            if (target.equals("server") || target.equals("1"))
	               fname = getServerConfigPath()+System.getProperty("file.separator")+fname;
	            else if (target.equals("web") || target.equals("2"))
	               fname = getWebConfigPath()+System.getProperty("file.separator")+fname;
	            else if (target.equals("iconfig") || target.equals("0"))
	               fname = CFG_ICONFIG_PATH+System.getProperty("file.separator")+fname;
	        }
	        return fname;
	   }

	   public static Document getXML(String fname, String target) throws Exception  {
	      try {
	         String fullname = getPathAdjName(fname, target);
	         if (fullname != null) {
	            org.w3c.dom.Document xmlDoc = null;
	            try {
	               java.io.FileInputStream finp = new java.io.FileInputStream(fullname);
	               com.ipssi.gen.utils.MyXMLHelper xmlHelper = new com.ipssi.gen.utils.MyXMLHelper(finp,null);
	               xmlDoc = xmlHelper.load();
	               finp.close();
	            }
	            catch (Exception e) {
	            }
	            if (xmlDoc == null) {
	               java.io.File file = new java.io.File(fullname);
	               String bakupDir = file.getParent()+System.getProperty("file.separator")+"bakups";
	               java.io.File bakupFile = new java.io.File(bakupDir+System.getProperty("file.separator")+file.getName());
	               java.io.FileInputStream finp = new java.io.FileInputStream(bakupFile);
	               com.ipssi.gen.utils.MyXMLHelper xmlHelper = new com.ipssi.gen.utils.MyXMLHelper(finp,null);
	               xmlDoc = xmlHelper.load();
	               finp.close();
	            }
	            return xmlDoc;
	         }
	         return null;
	      }
	      catch (Exception e) {
	          e.printStackTrace();
	          throw e;
	      }
	   }

	   public static boolean restoreXMLInp(String fname, String target, String reload, ServletContext context)  {
	      try {
	         boolean toReload = !"0".equals(reload);
	         String fullname = getPathAdjName(fname, target);
	         if (fullname != null) {
	            java.io.File file = new java.io.File(fullname);
	            String bakupDir = file.getParent()+System.getProperty("file.separator")+"bakups";
	            java.io.File bakupFile = new java.io.File(bakupDir+System.getProperty("file.separator")+file.getName());
	            if (bakupFile != null && bakupFile.exists()) {
	               //check and create an appropriate bakups in the directory ..
//	               System.out.println("renaming:"+bakupDir+System.getProperty("file.separator")+file.getName()+ " TO:"+fullname);
	               if (file.exists())
	                  file.delete();
	               bakupFile.renameTo(file);
//	               System.out.println("renamed:"+bakupDir+System.getProperty("file.separator")+file.getName()+ " TO:"+fullname);
	               if (toReload)
	                  Cache.makeCacheDirty(context);
	               return true;
	            }
	         }
	      }
	      catch (Exception e) {
	          e.printStackTrace();
	      }
	      return false;
	   }

	   public static boolean moveXMLInp(String fullTempname,ServletContext context)  {
	     try {

	         FileWriter fout = null;
	         PrintWriter outw = null;
	         java.io.FileInputStream finp = new java.io.FileInputStream(fullTempname);
//	         System.out.println("Temp:"+fullTempname);
	         com.ipssi.gen.utils.MyXMLHelper helper = new com.ipssi.gen.utils.MyXMLHelper(finp,outw);
	         org.w3c.dom.Document xmlDoc = null;

	         xmlDoc = helper.load();
	         String fname = null;
	         String target = null;
	         boolean toReload = true;
	         if (xmlDoc != null && xmlDoc.getDocumentElement() != null) {
	            fname = xmlDoc.getDocumentElement().getAttribute("_ufname");
	            target = xmlDoc.getDocumentElement().getAttribute("_utarget");
	            toReload = !"0".equals(xmlDoc.getDocumentElement().getAttribute("_ureload"));
	         }
	         else {
	         }
	         String fullname = getPathAdjName(fname, target);
	         if (fullname != null) {
	            java.io.File file = new java.io.File(fullname);
//	            System.out.println("File:"+fullname);
	            if (file != null && file.exists()) {
	               //check and create an appropriate bakups in the directory ..

	               String bakupDir = file.getParent()+System.getProperty("file.separator")+"bakups";
//	               System.out.println("BakupDir:"+bakupDir);
	               java.io.File dir = new java.io.File(bakupDir);
	               if (dir != null && !dir.exists()) {
//	               System.out.println("Created BakupDir:"+bakupDir);
	                  dir.mkdirs();
	               }
	               //now copy over the file ..
	               String renamedFile = bakupDir+System.getProperty("file.separator")+file.getName();
//	               System.out.println("rename to:"+renamedFile);
	               java.io.File renamedObj = new java.io.File(renamedFile);
	               if (renamedObj.exists())
	                  renamedObj.delete();
	               file.renameTo(renamedObj);
//	               System.out.println("renamed");

	            }
	            fout = new FileWriter(fullname);
	            outw = new PrintWriter(fout, true);
	            if (outw != null) {
	               helper.setOut(outw);
	               helper.save(xmlDoc);
//	               System.out.println("saved xml");
	               outw.close();
	               if (toReload) {//reload the config files ...
	                  Cache.makeCacheDirty(context);
	               }
	               return true;
	            }
	         }
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	     }
	     return false;
	  }

	   public static boolean uploadXMLInp(HttpServletRequest request, ServletContext context)  {
	     try {
	         java.io.InputStream inp = request.getInputStream();
	         FileWriter fout = null;
	         PrintWriter outw = null;

	         com.ipssi.gen.utils.MyXMLHelper helper = new com.ipssi.gen.utils.MyXMLHelper(inp,outw);
	         org.w3c.dom.Document xmlDoc = null;

	         xmlDoc = helper.load();
	         String fname = null;
	         String target = null;
	         boolean toReload = true;
	         if (xmlDoc != null && xmlDoc.getDocumentElement() != null) {
	            fname = xmlDoc.getDocumentElement().getAttribute("_ufname");
	            target = xmlDoc.getDocumentElement().getAttribute("_utarget");
	            toReload = !"0".equals(xmlDoc.getDocumentElement().getAttribute("_ureload"));
	         }
	         else {
	         }
	         String fullname = getPathAdjName(fname, target);
	         if (fullname != null) {
	            java.io.File file = new java.io.File(fullname);
	            if (file != null && file.exists()) {
	               //check and create an appropriate bakups in the directory ..

	               String bakupDir = file.getParent()+System.getProperty("file.separator")+"bakups";
	               java.io.File dir = new java.io.File(bakupDir);
	               if (dir != null && !dir.exists()) {
	                  dir.mkdirs();
	               }
	               //now copy over the file ..
	               String renamedFile = bakupDir+System.getProperty("file.separator")+file.getName();
	               java.io.File renamedObj = new java.io.File(renamedFile);
	               if (renamedObj.exists())
	                  renamedObj.delete();
	               file.renameTo(renamedObj);
	            }
	            fout = new FileWriter(fullname);
	            outw = new PrintWriter(fout, true);
	            if (outw != null) {
	               helper.setOut(outw);
	               helper.save(xmlDoc);
	               outw.close();
	               if (toReload) {//reload the config files ...
	                  Cache.makeCacheDirty(context);
	               }
	               return true;
	            }
	         }
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	     }
	     return false;
	  }
	  
	   
	   
	   public static void convertInListOfStrToStr(ArrayList<String> inList, StringBuilder retval) {
	      for (int i=0,is=inList.size();i<is;i++) {
	         if ( i != 0 ) {
	            retval.append(",");
	         }
	         retval.append(inList.get(i).toString());
	      }
	  }
	  public static void convertInListToStr(ArrayList inList, StringBuilder retval) {
	      for (int i=0,is=inList.size();i<is;i++) {
	         if ( i != 0 ) {
	            retval.append(",");
	         }
	         retval.append(inList.get(i).toString());
	      }
	  }
	  public static void convertInListToStr(String[] inList, StringBuilder retval) {
	      for (int i=0,is=inList == null ? 0 : inList.length;i<is;i++) {
	         if ( i != 0 ) {
	            retval.append(",");
	         }
	         retval.append(inList[i]);
	      }
	  }
	  public static boolean isInList(String[] inList, String str) {
	      if (str == null || inList == null)
	         return false;
	      for (int i=0,is=inList.length;i<is;i++) {
	         if (str.equals(inList[i]))
	            return true;         
	      }
	      return false;
	  }
	  
	  public static int[] convertValToArrayIntSorted(String listStr) { //020407 rajeev
	       if (listStr == null || listStr.length() == 0)
	           return null;
	       StringTokenizer strTok = new StringTokenizer(listStr, ",", false);
	       int count = 0;
	       
	       while (strTok.hasMoreTokens()) {
	          String tok = strTok.nextToken();
	          if (tok != null && tok.length() != 0)
	            count++;       
	       }
	       int[] retval = count == 0 ? null : new int[count];
	       strTok = new StringTokenizer(listStr, ",", false);
	       count = 0;       
	       while (strTok.hasMoreTokens()) {         
	          String tok = strTok.nextToken();
	          if (tok != null && tok.length() != 0) {
	            int v = Misc.getParamAsInt(trim(tok));
	            int pos = 0;
	            for (pos=0;pos<count;pos++) {
	               if (retval[pos] > v)
	                  break;
	            }
	            if (pos < count) {
	               for (int j=count-1;j>= pos;j--)
	                  retval[j+1] = retval[j];
	               retval[pos] = v;
	               count++;
	            }
	            else
	               retval[count++] = v;            
	          }
	       }
	       return retval;
	  }
	  
	  public static String[] convertValToArray(String listStr) {
	       if (listStr == null || listStr.length() == 0)
	           return null;
	       StringTokenizer strTok = new StringTokenizer(listStr, ",", false);
	       int count = 0;
	       
	       while (strTok.hasMoreTokens()) {
	          String tok = strTok.nextToken();
	          if (tok != null && tok.length() != 0)
	            count++;       
	       }
	       String[] retval = count == 0 ? null : new String[count];
	       strTok = new StringTokenizer(listStr, ",", false);
	       count = 0;       
	       while (strTok.hasMoreTokens()) {         
	          String tok = strTok.nextToken();
	          if (tok != null && tok.length() != 0)
	            retval[count++] = trim(tok);          
	       }
	       return retval;
	   }
	  public static void convertValToVector(String listStr, ArrayList retval) {
		  convertValToVector(listStr, retval, true);
	  }
	   public static void convertValToVector(String listStr, ArrayList retval, boolean ignoreUndef) {
		   if (listStr == null || listStr.length() == 0)
			   return;
	       StringTokenizer strTok = new StringTokenizer(listStr, ",", false);
	       boolean first = true;
	       while (strTok.hasMoreTokens()) {
	          String tok = strTok.nextToken();
	          int v1 = Misc.getParamAsInt(tok);
	          if (!Misc.isUndef(v1) || !ignoreUndef)
	             retval.add(new Integer(v1));
	       }
	   }
	   public static void convertValToStrVector(String listStr, ArrayList<String> retval) {
		   if (listStr == null || listStr.length() == 0)
			   return;
	       StringTokenizer strTok = new StringTokenizer(listStr, ",", false);
	       boolean first = true;
	       while (strTok.hasMoreTokens()) {
	          String tok = strTok.nextToken();
	         if (tok != null)
	        	 tok = trim(tok);
	         if (tok != null && tok.length() != 0)
	        	 retval.add(tok);
	       }
	   }

	   public static boolean isInList(ArrayList theList, int item) {
	       if (theList == null || theList.size() == 0)
	          return true;
	       for (int i=0,is=theList.size();i<is;i++) {
	          if (((Integer)theList.get(i)).intValue() == item)
	             return true;
	       }
	       return false;
	   }
	   
	   public static boolean isInList(ArrayList<String> theList, String item) {
	       if (theList == null || theList.size() == 0)
	          return true;
	       for (int i=0,is=theList.size();i<is;i++) {
	          if (theList.get(i).equals(item))
	             return true;
	       }
	       return false;
	   }

	   public static boolean G_CURRENT_IS_MANUAL = false;
	   public static int G_CURRENT_LAG = 0;
	   public static boolean G_BUDGET_ON = false;
	   public static int G_BUDGET_YEAR = 2006;

	   public static void setGlobalData(Connection dbConn, int dataCode, int[] intField, java.sql.Date[] dateField, SessionManager session) throws Exception {
	       try {
	          PreparedStatement ps = dbConn.prepareStatement(Queries.GET_GLOBAL_DATA);
	          ps.setInt(1, dataCode);
	          ResultSet rs = ps.executeQuery();
	          boolean exists = rs.next();
	          rs.close();
	          ps.close();
	          CallableStatement cs = dbConn.prepareCall(exists ? Queries.UPDATE_GLOBAL_DATA : Queries.INSERT_GLOBAL_DATA) ;
	          int numClassifies = 5;
	          int currIndex = 1;
	          int numFieldIndexStart = currIndex;
	          for (int i=0;i<numClassifies;i++)
	             setParamInt(cs, Misc.getUndefInt(), currIndex++);
	          int dateFieldIndexStart = currIndex;
	          for (int i=0;i<numClassifies;i++)
	             cs.setDate(currIndex++, null);
	          for (int i=0,is=intField.length;i<is;i++)
	             setParamInt(cs, intField[i], numFieldIndexStart+i);
	          for (int i=0,is=dateField.length;i<is;i++)
	             cs.setDate(dateFieldIndexStart+i, dateField[i]);

	          cs.setInt(currIndex++, dataCode);
	          cs.execute();
	          cs.close();
	          int if1 = intField[0];
	          int if2 = intField[1];
	          java.sql.Date dt1 = dateField[0];
	          boolean oldBudgetOn = G_BUDGET_ON;
	          
	          switch (dataCode) {
	             case 1: { //budget cycle
	                   G_BUDGET_ON = if1 == 1;
	                   G_BUDGET_YEAR = if2;
	                   if (if2 <= 0) {
	                     if (CURRENT_FISCAL_MONTH > 9)
	                        G_BUDGET_YEAR = CURRENT_FISCAL_YEAR+1;
	                     else
	                        G_BUDGET_YEAR =  CURRENT_FISCAL_YEAR;
	                   }
	                   break;
	                }
	             case 2: { //current date treatment
	                   G_CURRENT_IS_MANUAL = if1 == 1;
	                   G_CURRENT_LAG = if2;
	                   if (!G_CURRENT_IS_MANUAL || dt1 == null)
	                      dt1 = getTodayDate();
	                   if (!G_CURRENT_IS_MANUAL) {
	                      addDays(dt1, -1*if2);
	                   }

	                   CURRENT_FISCAL_YEAR = 1900+dt1.getYear();
	                   CURRENT_FISCAL_MONTH = dt1.getMonth();

	                   break;
	                }
	             case 3: { //global disc rate
	                   Misc.DISCOUNT_RATE = (double)if1/100.0f;
	                   break;
	                }
	             case 4: {//create/delete free priv
	                 //G_CREATE_FREE_PRIV = if1;
	                 //G_DELETE_FREE_PRIV = if2;
	                 break;
	             }
	          }//end of switch
	          if (oldBudgetOn != G_BUDGET_ON && !G_BUDGET_ON) {
	            // CapEx CustomImpl.budgetProcessClose(dbConn, session);
	          }

	       }
	       catch (Exception e) {
	          e.printStackTrace();
	          throw e;
	       }
	   }

	   public static void syncGlobal(Connection dbConn) throws Exception {
	      //will read the global data and put things in the Misc constants ...
	      try {
	          PreparedStatement ps = dbConn.prepareStatement(Queries.GET_GLOBAL_DATA_ALL);
	          ResultSet rs = ps.executeQuery();
	          while (rs.next()) {
	             int dataCode = rs.getInt(1);
	             int if1 = rs.getInt(2);
	             int if2 = rs.getInt(3);
	             java.sql.Date dt1 = rs.getDate(7);
	             switch (dataCode) {
	                case 1: { //budget cycle
	                   G_BUDGET_ON = if1 == 1;
	                   G_BUDGET_YEAR = if2;
	                   if (if2 <= 0) {
	                     if (CURRENT_FISCAL_MONTH > 9)
	                        G_BUDGET_YEAR =  CURRENT_FISCAL_YEAR+1;
	                     else
	                        G_BUDGET_YEAR =  CURRENT_FISCAL_YEAR;
	                   }
	                   break;
	                }
	                case 2: { //current date treatment
	                   G_CURRENT_IS_MANUAL = if1 == 1;
	                   G_CURRENT_LAG = if2;
	                   if (!G_CURRENT_IS_MANUAL || dt1 == null)
	                      dt1 = getTodayDate();
	                   if (!G_CURRENT_IS_MANUAL) {
	                      addDays(dt1, -1*if2);
	                   }

	                   CURRENT_FISCAL_YEAR = 1900+dt1.getYear();
	                   CURRENT_FISCAL_MONTH = dt1.getMonth();

	                   break;
	                }
	                case 3: { //global disc rate
	                   Misc.DISCOUNT_RATE = (double)if1/100.0f;
	                   break;
	                }
	                case 4: { //free create/delete
	                   //G_CREATE_FREE_PRIV = if1;
	                   //G_DELETE_FREE_PRIV = if2;
	                   break;
	                }
	             }
	          }
	          rs.close();
	          ps.close();
	          //CAPEX_REMOVE loadCurrentCurrencyListIds(dbConn);
	          
	      }
	      catch (Exception e) {
	          e.printStackTrace();
	          throw e;
	      }
	   }

	   
	   
	   final static private String g_printHelpButton1 = "&nbsp;&nbsp;&nbsp;<img src=\""+Misc.G_IMAGES_BASE+"help.gif\" onclick=\"showHelp('";
	   final static private String g_printHelpButton2 = "')\">";

	   static public String getHelpText(String helpTag) {
	      if (helpTag == null || helpTag.length() == 0)
	         return "";
	      return g_printHelpButton1 + helpTag + g_printHelpButton2;
	   }
	   
	   
	   public static int getContextOrgId(SessionManager session, Connection dbConn, Cache cache, Logger log, User user) throws Exception {
	      int cntxtOrg = Misc.getParamAsInt(session.getAttribute("_cntxt_org"));
	      if (Misc.isUndef(cntxtOrg)) {
	         PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc(session, dbConn, cache, log, user);
	         cntxtOrg = pageMenuHeadCalc.getPropertyInt(123);
	      }
	      if (Misc.isUndef(cntxtOrg)) {
	         cntxtOrg = (int) session.getPortfolioId();
	      }
	      if (Misc.isUndef(cntxtOrg))
	         cntxtOrg = 1;      
	      session.setAttribute("_cntxt_org", Integer.toString(cntxtOrg),false);
	      return cntxtOrg;
	   }
	   
	   public static MiscInner.ContextInfo getContextInfo(Locale locale, int currencySpec, int unitSpec, int measureId, SessionManager session, Connection dbConn, Cache cache, Logger log, User user) throws Exception {
	      if (Misc.isUndef(currencySpec)) 
	         currencySpec = 10005; //budget currency at org level
	      
	      if (Misc.isUndef(unitSpec))        
	         unitSpec = 10001; //unit level currency specs
	           
	      //Locale locale = cache.getLocale(localeId);
	      MiscInner.CurrencyInfo currencyInfo = null;
	      MiscInner.UnitInfo unitInfo = null;
	      int currId = Misc.getUndefInt();//MiscInner.CountryInfo.g_defaultCurrencyCode;
	      int contextOrgId = getContextOrgId(session, dbConn, cache, log, user);
	      if (currencySpec < 10000) {
	         currId = currencySpec;
	      }
	      else {         
	         if (currencySpec == 10000) {
	            currencySpec = Misc.getParamAsInt(session.getParameter("_page_currency"));  
	            if (Misc.isUndef(currencySpec)) {
	               currencySpec = 10005; //budget currency from org
	            }
	         }
	         else if (currencySpec == 10001 || currencySpec == 10004) { //do appropriate
	            DimInfo measure = DimInfo.getDimInfo(measureId);
	            if (measure != null) {
	               if (measure.m_useRepCurrencyByDefault) {
	                  currencySpec = currencySpec == 10001 ? 10003 : 10006;
	               }
	               else {
	                  currencySpec = currencySpec == 10001 ? 10002 : 10005;
	               }
	            }
	            else {
	               currencySpec = 10002;
	            }
	         }
	         switch (currencySpec) {
	            
	            case 10003: {//reporting currency at project level
	               PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc(session, dbConn, cache, log, user);
	               currId = pageMenuHeadCalc.getPropertyInt(253);
	               if (!Misc.isUndef(currId))
	                  break;
	            }
	             
	            case 10006: { //report at org             
	               MiscInner.PortInfo portInfo = cache.getPortInfo(contextOrgId, dbConn);
	               if (portInfo != null) {
	                  currId = portInfo.m_repCurrency;
	               }
	               if (!Misc.isUndef(currId))
	                 break;                             
	            }
	            
	            case 10002:
	            case 10005:
	            case 10001: //should not occur ... changed it to appropriate stuff before hand
	            case 10004: //should not occur ... changed it to appropraite spec before hand
	            case 10000: //should not occur ... changed it to appropriate spec before hand
	            default: {//budget at unit currenly for 10002, 10005
	               MiscInner.PortInfo portInfo = cache.getPortInfo(contextOrgId, dbConn);
	               if (portInfo != null) {
	                  currId = portInfo.m_budCurrency;
	               }
	            } //default
	         }//switch
	         if (Misc.isUndef(currId))
	            currId = MiscInner.CurrencyInfo.g_defaultCurrencyCode;
	         
	      }//get currency     
	      currencyInfo = cache.getCurrencyInfo(currId);
	      int unitId = Misc.getUndefInt();
	      if (unitSpec < 10000) {
	         unitId = unitSpec;
	      }
	      else {         
	         if (unitSpec == 10000) {
	            unitSpec = Misc.getParamAsInt(session.getParameter("_page_unit"));  
	            if (Misc.isUndef(unitSpec)) {
	               unitSpec = 10001; //budget currency from org
	            }
	         }         
	         switch (unitSpec) {
	            
	            case 10001: {//from org level
	               MiscInner.PortInfo portInfo = cache.getPortInfo(contextOrgId, dbConn);
	               if (portInfo != null) {
	                  unitId = portInfo.m_currUnitCode;                  
	               }
	               if (!Misc.isUndef(unitId))
	                 break;                                         
	               
	            }
	            case 10002 : { //from currency
	               MiscInner.CurrencyInfo currInfo = cache.getCurrencyInfo(currId);
	               if (currInfo != null) {
	                  unitId = currInfo.m_unitCode;
	               }
	            }
	         }
	         if (Misc.isUndef(unitId))
	            unitId = MiscInner.UnitInfo.g_defaultUnit.m_id;
	         
	      }//get currency     
	      unitInfo = cache.getUnitInfo(DimInfo.QTY_CURRENCY, unitId);
	      return new MiscInner.ContextInfo(locale, unitInfo, currencyInfo);
	      
	   }
	   public static MiscInner.ContextInfo getContextInfo(SessionManager session, Connection dbConn, Cache cache, Logger log, User user) throws Exception {
	       MiscInner.ContextInfo contextInfo = (MiscInner.ContextInfo) session.getAttributeObj("_context_info");
	       if (contextInfo == null) {
	          int localeId = Misc.getParamAsInt(session.getParameter("_page_locale"));
	          int currencySpec = Misc.getParamAsInt(session.getParameter("_page_currency"));
	          int unitSpec = Misc.getParamAsInt(session.getParameter("_page_unit"));
	          boolean undefLocale = Misc.isUndef(localeId);
	          boolean undefCurrencySpec = Misc.isUndef(currencySpec);
	          boolean undefUnitSpec = Misc.isUndef(unitSpec);
	          Locale locale = null;
	          MiscInner.CurrencyInfo currencyInfo = null;
	          MiscInner.UnitInfo unitInfo = null;
	          if (undefLocale || undefCurrencySpec || undefUnitSpec) {
	              if (undefLocale) {
	                  localeId = MiscInner.PortInfo.G_DEFAULT_LOCALEID;
	                  session.setAttribute("_page_locale", Integer.toString(localeId), true);
	              }
	          //   int contextOrgId = Misc.getContextOrgId(session, dbConn, cache, log, user);   
//	             MiscInner.PortInfo portInfo = cache.getPortInfo(contextOrgId, dbConn);
//	             if (undefLocale) {
//	                if (portInfo != null) {
//	                    localeId = portInfo.m_localeId;
//	                }
//	                else {
//	                    localeId = MiscInner.PortInfo.G_DEFAULT_LOCALEID;
//	                }
//	             } 
	             if (undefCurrencySpec) {
	                currencySpec = 10005; //budget currency at org level
	             }
	             if (undefUnitSpec) {
	                 unitSpec = MiscInner.UnitInfo.g_defaultUnitCode;
	                 session.setAttribute("_page_unit", Integer.toString(MiscInner.UnitInfo.g_defaultUnitCode), true);
	              //  unitSpec = 10001; //unit level currency specs
	                //unitSpec = MiscInner.
	             }
	          }
	          
	          contextInfo = getContextInfo(cache.getLocale(localeId), currencySpec, unitSpec, Misc.getUndefInt(), session, dbConn, cache, log, user);
	          session.setAttributeObj("_context_info", contextInfo);
	       }
	       return contextInfo;       
	   }
	   

	    public static String getMD5EncodedString(String inputStr) throws Exception
	    {
	            MessageDigest md = null;
	            try
	            {
	                    // Get the message digest and update it with the string to be hashed
	                    md = MessageDigest.getInstance("MD5");
	                    md.update(inputStr.getBytes("ASCII"));
	            }
	            catch (Exception ex)
	            {
	                    return null;
	            }

	            byte rawBytes[] = md.digest();
	            char[] charArray = Hex.encodeHex(rawBytes);
	            String hashedStr = new String(charArray);
	            return hashedStr;
	    }
	    
	    
	    static public boolean execIfExistStmt(PreparedStatement ps) throws Exception {
	       ResultSet rs = ps.executeQuery();
	       boolean retval = false;
	       if (rs.next())
	          retval = true;
	       rs.close();
	       return retval;       
	    }
	    
	    public static int getOrgAdminPageOrgId(String pageContext, SessionManager session, String label123, int privToCheckFor, DimInfo orgDimInfo) throws Exception {
	       int userSpec = Misc.getParamAsInt(session.request.getParameter(label123));       
	       String topLevelContext = UserGen.helpGetAndSetMainMenuLevelContext(session, pageContext);       
	       if (Misc.isUndef(userSpec)) {           
	           userSpec = Misc.getParamAsInt(session.getAttribute(topLevelContext+"v123"));
	           if (Misc.isUndef(userSpec))
	              userSpec = 1;
	           userSpec = session.getUser().getUserSpecificDefaultPort(session, userSpec, privToCheckFor, orgDimInfo);
	           if (Misc.isUndef(userSpec))
	              userSpec = 1;
	       }
	       session.setAttribute(topLevelContext+"v123",Integer.toString(userSpec),true);
	       return userSpec;       
	  }
	   
	   
	   

		public static int executeGetId(PreparedStatement cStmt, boolean toGetId, String queryStr) throws Exception{
			int retVal = getUndefInt();
			if (!toGetId) {
				cStmt.execute();
			}
			else {
				cStmt.executeUpdate();
				ResultSet rSet = cStmt.getGeneratedKeys();
				while (rSet.next()) {
					retVal = rSet.getInt(1);
				}
				closeResultSet(rSet);
				rSet = null;
			}
			return retVal;
		}
	  
	  static public int executeGetId(PreparedStatement cs, long returnThisAsApprop, String queryString) throws Exception{
	    if (G_DO_ORACLE) {
	      cs.execute();  
	      return (int) returnThisAsApprop;
	    }
	    else {
	      return executeGetId(cs, true, queryString);
	    }
	  }

		public static void closeResultSet(ResultSet rSet) throws Exception{
			if (rSet != null)
				rSet.close();
			rSet = null;
		}

		public static void closeStatement(Statement stmt) throws Exception{
			if (stmt != null)
				stmt.close();
			stmt = null;
		}

		public static void setParamDouble(PreparedStatement cStmt, double val, int atPos) throws SQLException {
			if (isUndef(val))
				cStmt.setNull(atPos, Types.FLOAT);
			else
				cStmt.setDouble(atPos, val);
		}

		public static void setParamDouble(PreparedStatement cStmt, String str, int atPos, double undefValue) throws SQLException {
			double par = getParamAsDouble(str, undefValue);
			setParamDouble(cStmt, par, atPos);
		}

		public static void setParamInt(PreparedStatement cStmt, String str, int atPos, int undefValue) throws SQLException {
			int par = getParamAsInt(str, undefValue);
			if ("-1111111".equals(str)) { //TODO_VERIFY ... if ill consequences
				par = Misc.getUndefInt();
				cStmt.setInt(atPos, par);
				return;
			}
			setParamInt(cStmt, par, atPos);
		}

		public static void setParamInt(PreparedStatement cStmt, String str, int atPos) throws SQLException {
			setParamInt(cStmt, str, atPos, getUndefInt());
		}

		public static void setParamLong(PreparedStatement cStmt, String str, int atPos, int undefValue)  throws SQLException {
			long par = getParamAsLong(str, undefValue);
			setParamLong(cStmt, par, atPos);
		}

		public static void setParamLong(PreparedStatement cStmt, String str, int atPos) throws SQLException {
			setParamLong(cStmt, str, atPos, getUndefInt());
		}

		public static void setParamDate(PreparedStatement cStmt, int atPos, java.sql.Date date) throws SQLException {
			if (date == null) {
				cStmt.setNull(atPos, Types.DATE);
			}
			else {
				cStmt.setDate(atPos, new java.sql.Date(date.getTime()));
			}
		}
	  
	  
	  
	  
	  
	  

		public static Document convertVectorToXml(ArrayList vals, String tagName, String attribName) throws Exception {
			Document charXml = MyXMLHelper.loadFromString("<data></data>");
			try {
				Element topElement = charXml.getDocumentElement();
				if (vals != null) {
					for (int i = 0; i < vals.size(); i++) {
						int itemId = ((Integer)vals.get(i)).intValue();
						if (!Misc.isUndef(itemId)) {
							Element itemXml = charXml.createElement(tagName);
							itemXml.setAttribute(attribName, Integer.toString(itemId));
							topElement.appendChild(itemXml);
						}
					}
				}
			}
			catch (Exception ex) {
				throw ex;
			}
			return charXml;
		}
	  
	  private static long g_lastTimeMilli = 0;
	  private static StringBuilder g_traceMsgBuf = new StringBuilder();
	  public static void trace(String msg, Logger log) {
	     long curr = System.currentTimeMillis();
	     
	     g_traceMsgBuf.setLength(0);
	     g_traceMsgBuf.append(curr-g_lastTimeMilli).append(":");
	     g_lastTimeMilli = curr;
	     g_traceMsgBuf.append(msg);
	     if (log == null) {
	        System.out.println(g_traceMsgBuf);
	     }
	     else if (log.getLoggingLevel()>=15) {
	        System.out.println(g_traceMsgBuf);
	        log.log(g_traceMsgBuf, 15);
	     }
	     
	  }
	    
	// TO_PORT_FORWARD
	  
	   public static void getQueryHint(Cache cache) {
	   
	      try {
	        synchronized (cache) {
	           org.w3c.dom.Document sysConfig = null;
	           FileInputStream inp = null;
	           FileWriter fout = null;
	           PrintWriter outw = null;
	           MyXMLHelper test = null;
	           try {              
	               inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"system_config.xml");
	           }
	           catch (Exception e) {
	               inp = null;
	           }
	           try {
	           }
	           catch (Exception e) {
	              fout = null;
	              outw = null;
	           }
	           try {
	               test = new MyXMLHelper(inp, outw);
	               sysConfig = test.load();
	           }
	           catch (Exception e) { 
	               test = null;
	               sysConfig = null;
	           }
	           if (inp != null)
	              inp.close(); 
	           if (sysConfig != null && sysConfig.getDocumentElement() == null)
	              sysConfig = null;
	           if (sysConfig == null || sysConfig.getDocumentElement() == null)
	              sysConfig = MyXMLHelper.loadFromString("<data/>");
	           Element topElem = sysConfig.getDocumentElement();
	           
	           G_HINT_ORDER_OF_JOIN = "1".equals(Misc.getParamAsString(topElem.getAttribute("order_of_join"), G_HINT_ORDER_OF_JOIN ? "1" : "0"));
	           G_HINT_FORCE_PLAN = "1".equals(Misc.getParamAsString(topElem.getAttribute("force_plan"), G_HINT_FORCE_PLAN ? "1" : "0"));
	           G_HINT_LOOP_MEASURE = "1".equals(Misc.getParamAsString(topElem.getAttribute("loop_for_measure"), G_HINT_LOOP_MEASURE ? "1" : "0"));
	           G_HINT_INCLUDE_USER_PRIV = "1".equals(Misc.getParamAsString(topElem.getAttribute("include_user_priv"), G_HINT_INCLUDE_USER_PRIV ? "1" : "0"));
	           G_HINT_LOOP_ALL = "1".equals(Misc.getParamAsString(topElem.getAttribute("loop_for_all"), G_HINT_LOOP_ALL ? "1" : "0"));
	           G_HINT_RECOMPILE = "1".equals(Misc.getParamAsString(topElem.getAttribute("recompile"), G_HINT_RECOMPILE ? "1" : "0"));
	           G_HINT_OPTION_CLAUSE = Misc.getParamAsString(topElem.getAttribute("option_part"), null);
	        }
	      }
	      catch (Exception e) {
	         //dont do anything
	      }
	   }
	   
	   public static void saveQueryHint(SessionManager session, Cache cache) {
	      try {          
	        synchronized (cache) {
	           org.w3c.dom.Document sysConfig = null;
	           FileInputStream inp = null;
	           FileWriter fout = null;
	           PrintWriter outw = null;
	           MyXMLHelper test = null;
	           try {              
	               inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"system_config.xml");
	           }
	           catch (Exception e) {
	               inp = null;
	           }
	           try {
	               test = new MyXMLHelper(inp, outw);
	               sysConfig = test.load();
	           }
	           catch (Exception e) { 
	               test = null;
	               sysConfig = null;
	           }
	           if (inp != null)
	              inp.close(); 
	           if (sysConfig != null && sysConfig.getDocumentElement() == null)
	              sysConfig = null;
	           if (sysConfig == null || sysConfig.getDocumentElement() == null)
	              sysConfig = MyXMLHelper.loadFromString("<data/>");
	           Element topElem = sysConfig.getDocumentElement();
	           
	           G_HINT_ORDER_OF_JOIN = "1".equals(Misc.getParamAsString(session.getParameter("order_of_join")));
	           G_HINT_FORCE_PLAN = "1".equals(Misc.getParamAsString(session.getParameter("force_plan")));
	           G_HINT_LOOP_MEASURE = "1".equals(Misc.getParamAsString(session.getParameter("loop_for_measure")));
	           G_HINT_INCLUDE_USER_PRIV = "1".equals(Misc.getParamAsString(session.getParameter("include_user_priv")));
	           G_HINT_LOOP_ALL = "1".equals(Misc.getParamAsString(session.getParameter("loop_for_all")));
	           G_HINT_RECOMPILE = "1".equals(Misc.getParamAsString(session.getParameter("recompile")));
	           G_HINT_OPTION_CLAUSE = Misc.getParamAsString(session.getParameter("option_part"), null);
	           
	           topElem.setAttribute("order_of_join", G_HINT_ORDER_OF_JOIN ? "1" : "0");
	           topElem.setAttribute("force_plan", G_HINT_FORCE_PLAN ? "1" : "0");
	           topElem.setAttribute("loop_for_measure", G_HINT_LOOP_MEASURE ? "1" : "0");
	           topElem.setAttribute("include_user_priv", G_HINT_INCLUDE_USER_PRIV ? "1" : "0");
	           topElem.setAttribute("loop_for_all", G_HINT_LOOP_ALL ? "1" : "0");
	           topElem.setAttribute("recompile", G_HINT_RECOMPILE ? "1" : "0");
	           if (G_HINT_OPTION_CLAUSE == null || G_HINT_OPTION_CLAUSE.length() == 0)
	               topElem.removeAttribute("option_part");
	           else           
	              topElem.setAttribute("option_part", G_HINT_OPTION_CLAUSE);
	           try {
	               fout = new FileWriter(Cache.serverConfigPath+System.getProperty("file.separator")+"system_config.xml");
	               outw = new PrintWriter(fout, true);
	               test.setOut(outw);
	               test.save(sysConfig);
	               outw.close();
	           }
	           catch (Exception e) {
	              fout = null;
	              outw = null;
	           }                
	        }
	      }
	      catch (Exception e) {
	         //dont do anything
	      }
	   }
	   
	  

	   public static void recordUserLogin(Connection dbConn, int u_id, String hostIp) {
		   try {
			   String query = Queries.INSERT_USER_LOGIN_TRACK; //"insert into user_login_track (user_id, ts) values (?, ?) ";
			   PreparedStatement pStmt = dbConn.prepareStatement(query);
			   int param = 1;
			   pStmt.setInt(param++, u_id);
			   pStmt.setTimestamp(param++, getCurrentTimestamp());
			   pStmt.setString(param++, hostIp);
			   pStmt.execute();
			   if (pStmt != null) {
				   pStmt.close();
			   }
			   pStmt = null;
		   }
		   catch (Exception ex) {
			   ex.printStackTrace();
		   }
	   }

	   public static String getUserLastLogin(Connection dbConn, int u_id) {
		   String lastLoginStr = "";
		   try {
			   String query = Queries.GET_USER_LAST_LOGIN;
			   PreparedStatement pStmt = dbConn.prepareStatement(query);
			   int param = 1;
			   pStmt.setInt(param++, u_id);
			   ResultSet rSet = pStmt.executeQuery();

			   while (rSet.next()) {
				   java.sql.Timestamp rsTs = rSet.getTimestamp(1);
				   if (rSet.wasNull()) {
					   lastLoginStr = "You are logging in for the first time in the system";
				   }
				   else {
					   lastLoginStr = "Last Login " + DateFormat.getDateTimeInstance().format(new java.util.Date(rsTs.getTime()));
				   }
			   }
			   rSet.close();
			   pStmt.close();
		   }
		   catch (Exception ex) {
			   ex.printStackTrace();
		   }
		   return lastLoginStr;
	   }

	   public static java.sql.Timestamp getCurrentTimestamp() {
		   return new java.sql.Timestamp(getCurrentTime().getTime());
	   }

	   public static void updateObjectCounter(Connection dbConn, int objId, int objType, int userId, String section) {
		   try {
			   String queryStr = Queries.INSERT_OBJECT_COUNTERS;
			   PreparedStatement pStmt = dbConn.prepareStatement(queryStr);
			   int param = 1;

			   pStmt.setInt(param++, objId);
			   pStmt.setInt(param++, objType);
			   pStmt.setInt(param++, userId);
			   pStmt.setString(param++, section);
			   pStmt.setTimestamp(param++, Misc.getCurrentTimestamp());

			   pStmt.execute();
			   if (pStmt != null)
				   pStmt.close();
			   pStmt = null;
		   }
		   catch (Exception ex) {
			   ex.printStackTrace();
		   }
	   }

		//Method added to check if a process is running -- Sameer 09232008
		public static boolean checkProcess(String processName) {
			String str = "";
			try {
				Runtime rt = Runtime.getRuntime();
				
				// Get all the processes
				InputStream inpStream = rt.exec("TASKLIST").getInputStream();
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(inpStream));
				while ((str = bufReader.readLine()) != null) {
					if (str.indexOf(processName) != -1) {
						return true;
					}
				}
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			return false;
		}
	  
	  
	   
	   public static void saveFileToDB(Connection dbConn, String fname, InputStream is, int len) throws Exception {
	      try {
	    	  if (true)
	    		  return;
	         //delete if it exists
	         //insert
	         len = -1; //not sure if httpservlet stream gives good value
	         PreparedStatement ps = dbConn.prepareStatement("delete from bin_files where name = ?");
	         ps.setString(1, fname);
	         ps.execute();
	         ps.close();
	         ps = dbConn.prepareStatement("insert into bin_files (name, data) values (?,?)");
	         ps.setString(1, fname);
	         ps.setBinaryStream(2, is, len);
	         ps.execute();
	         ps.close();
	         
	      }
	      
	      catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	      }
	      
	   }
	   
	   public static String getUserUploadFileURL(String fileName) {
	      return "test_html/GenBinGetter?do_db=1&file_name="+fileName;
	   }
	   public static String getUserUploadFileURL(int fileId) {
	      return "test_html/GenBinGetter?do_db=1&file_id="+Integer.toString(fileId);
	   }
	   
	   public static void tempUploadUserFiles(Connection dbConn, ServletContext context) throws Exception {
	      try {
	         //lazy approach ... load all files on filesystem to db
	         PreparedStatement ps = dbConn.prepareStatement("select name from file_names");
	         ResultSet rs = ps.executeQuery();
	         String fpath = context.getRealPath("/") + "user_files/business_plans"+"/";
	         while (rs.next()) {              
	            String fileName = rs.getString(1);
	            String inpFile = fpath+fileName;
	            try {
	              java.io.FileInputStream finp = new java.io.FileInputStream(inpFile);
	              java.io.File f = new java.io.File(inpFile);
	              int len = (int)f.length();
	              com.ipssi.gen.utils.Misc.saveFileToDB(dbConn, fileName, finp, len);            
	              finp.close();
	              dbConn.commit();
	            }
	            catch (Exception e2) {
	               e2.printStackTrace();
	            }
	         }
	         rs.close();
	         ps.close();
	      }      
	      catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	      }
	      
	   }      
	   
	   //For getting files smartly AFTER_MERGE
	   public static Document getFileFromDB(Connection _dbConnection, String inpFile, boolean saveToDBifNotFoundInDB, boolean saveToOut, Writer out) throws Exception { ////CHANGE_AFTER_MERGE
	        boolean foundFromDB = false;
	        org.w3c.dom.Document xmlDoc = null;
	        if (false && _dbConnection != null) {//$TRACK
	            try {
	               PreparedStatement ps = _dbConnection.prepareStatement("select data from bin_files where name=?");
	               ps.setString(1, inpFile);
	               ResultSet rs = ps.executeQuery();
	               if (rs.next()) {
	                   InputStream is = rs.getBinaryStream(1);
	                   com.ipssi.gen.utils.MyXMLHelper xmlHelper = new com.ipssi.gen.utils.MyXMLHelper(is,out);
	                   xmlDoc = xmlHelper.load();
	                   if (saveToOut && out !=  null)
	                      xmlHelper.save(xmlDoc);
	                   is.close();
	                   foundFromDB = true;
	               }
	               rs.close();
	               ps.close();
	            }
	            catch (Exception e) {
	               e.printStackTrace();
	            //   throw e; .. dont
	            }
	        }
	        //create and spit it out into the stream ..
	        if (!foundFromDB) {
	          java.io.FileInputStream finp = new java.io.FileInputStream(inpFile);
	          com.ipssi.gen.utils.MyXMLHelper xmlHelper = new com.ipssi.gen.utils.MyXMLHelper(finp,out);
	          xmlDoc = xmlHelper.load();
	          if (saveToOut && out !=  null)
	              xmlHelper.save(xmlDoc);
	          finp.close();
	          if (saveToDBifNotFoundInDB) {
	              try {
	                  finp = new java.io.FileInputStream(inpFile);
	                  java.io.File f = new java.io.File(inpFile);
	                  int len = (int)f.length();
	                  
	                  Misc.saveFileToDB(_dbConnection, inpFile, finp, len);
	                  finp.close();
	              }
	              catch (Exception e) {
	                  e.printStackTrace();
	              }
	          }          
	      }
	      return xmlDoc;
	   }
	   //AFTER_MERGE
	   public static int getVersionId(Connection dbConn, int objType, int objId, int byUser, String forTag) throws Exception {
	       try {
	           int versionId = (int) com.ipssi.gen.utils.Misc.getNextId(dbConn, com.ipssi.gen.utils.Sequence.SEQ_VERSION_GENERATOR);
	           PreparedStatement cStmt = Misc.G_DO_ORACLE ? dbConn.prepareStatement(com.ipssi.gen.utils.Queries.VERSION_GENERATOR) : dbConn.prepareStatement(com.ipssi.gen.utils.Queries.VERSION_GENERATOR, Statement.RETURN_GENERATED_KEYS);
	           int paramIndex = 1;
	           if (Misc.G_DO_ORACLE)
			          cStmt.setInt(paramIndex++, versionId);
	           cStmt.setInt(paramIndex++, objType);
	           cStmt.setInt(paramIndex++, objId);
	           cStmt.setInt(paramIndex++, byUser);
	           cStmt.setString(paramIndex++, forTag);           
	           versionId = Misc.executeGetId(cStmt,versionId, Queries.VERSION_GENERATOR);
	           cStmt.close();
	           return versionId;           
	       }
	       catch (Exception e) {
	           e.printStackTrace();
	           throw e;
	       }
	   }

	   public static boolean saveXMLStringDB(Connection dbConn, String fname, String inpStr) throws Exception {
	   
	      try {
	          if (inpStr == null || inpStr.length() == 0)
	             return false;
	          ByteArrayInputStream xmlDataStream = null;
	          xmlDataStream = new ByteArrayInputStream(inpStr.getBytes("ISO-8859-1"));
	          saveFileToDB(dbConn, fname, xmlDataStream, inpStr.length());
	          xmlDataStream.close();
	          return true;
	      }
	      catch (Exception e) {
	          e.printStackTrace();
	          throw e;
	      }
	   }
	   
	public static int getPosCustomLineLevelClassify(int measureId, int dimId, Cache cache) {// -1 if non existent
	     //almost similar code in getCustomNumLineLevelClassify
	     //almost similar logic (i.e. first of phase or oc is mapped directly else in classify in project_alternetive_measure_new.jsp

	     int retval = 0;
	     boolean foundOCorPhase = false;
	     boolean foundSkillOrCost = false;
	     ArrayList classifyAsInt = DimInfo.getDimInfo(measureId).m_classifyDimListInteger;
	     for (int n1=0,n1s=classifyAsInt.size();n1<n1s;n1++) {	      
	        int clDim = ((Integer)classifyAsInt.get(n1)).intValue();
	        if (clDim == 23 || clDim == 9)
	           continue;
	        if (measureId == 26 && (clDim == 22 || clDim == 59 || clDim == 140 || clDim == 193))
	           continue;
	        else if (measureId == 42 && (clDim == 20 || clDim == 146 || clDim == 9))
	           continue;
	        else if (measureId == 19 && (clDim == 146 || clDim == 9))
	           continue;
	        else if (measureId == 41 && (clDim == 20 || clDim == 59))
	           continue;
	                //the first of phase  or OC
	        //the  first of skill or Cost Center
	        else if (!foundOCorPhase  && (clDim == 59 || clDim == 9)){
	           foundOCorPhase = true;
	           continue;
	        }
	        else if  (!foundSkillOrCost  && (clDim == 22 || clDim == 20)){
	           foundSkillOrCost = true;
	           continue;
	        }
	        if (clDim == dimId)
	           return retval;
	        retval++;
	     }
	     return -1;
	  }

	  public static int getCustomNumLineLevelCalssify(int measureId, Cache cache) {
	     //almost similar code in getPosCustomLineLevelClassify
	     //almost similar logic (i.e. first of phase or oc is mapped directly else in classify in project_alternetive_measure_new.jsp
	     
	     int retval = 0;
	     boolean foundOCorPhase = false;
	     boolean foundSkillOrCost = false;
	     ArrayList classifyAsInt = DimInfo.getDimInfo(measureId).m_classifyDimListInteger;
	     for (int n1=0,n1s=classifyAsInt.size();n1<n1s;n1++) {	      
	        int clDim = ((Integer)classifyAsInt.get(n1)).intValue();        
	        if (clDim == 23 || clDim == 9) //target_market .. or scenario id
	           continue;
	        if (measureId == 26 && (clDim == 22 || clDim == 59 || clDim == 140 || clDim == 193))
	           continue;
	        else if (measureId == 42 && (clDim == 20 || clDim == 146 || clDim == 9))
	           continue;
	        else if (measureId == 19 && (clDim == 146 || clDim == 9))
	           continue;
	        else if (measureId == 41 && (clDim == 20 || clDim == 59))
	           continue;
	        //the first of phase  or OC
	        //the  first of skill or Cost Center
	        else if (!foundOCorPhase  && (clDim == 59 || clDim == 9)){
	           foundOCorPhase = true;
	           continue;
	        }
	        else if  (!foundSkillOrCost  && (clDim == 22 || clDim == 20)){
	           foundSkillOrCost = true;
	           continue;
	        }
	        retval++;
	     }
	     return retval;
	  }
	  
	  
	  public static String getRsetDimBased(Connection dbConn, ResultSet rset, int index, DimConfigInfo dimConfigInfo, Cache cache) throws Exception {
	      return getRsetDimBased(dbConn, rset, index, dimConfigInfo.m_dimCalc.m_dimInfo, cache);
	  }
	  public static String getRsetDimBased(Connection dbConn, ResultSet rset, int index, DimInfo dimInfo, Cache cache) throws Exception {
	      int type = dimInfo.getAttribType();
	      
	      if (type == Cache.LOV_TYPE) {
	         int valId = getRsetInt(rset, index, dimInfo.getDefaultInt());
	         int retval = cache.getParentDimValId(dbConn, dimInfo, valId);
	         return cache.getAttribDisplayName(dimInfo, retval);
	      }
	      else if (type == Cache.NUMBER_TYPE) {
	         double retval = getRsetDouble(rset, index, dimInfo.getDefaultDouble());
	         return Misc.isUndef(retval) ? "" : Double.toString(retval);
	      }
	      else if (type == Cache.DATE_TYPE) {
	         java.sql.Date retval = rset.getDate(index);
	         if (rset.wasNull()) {
	            retval = dimInfo.getDefaultDate();
	         }

	         return retval != null ? Misc.getPrintableDateSimple(retval) : "";
	      }
		  else if (type == Cache.LOV_NO_VAL_TYPE) {
			  int valId = getRsetInt(rset, index);
			  return Misc.isUndef(valId) ? "" : Integer.toString(valId);
		  }
	      else {
	         String retval = getRsetString(rset, index, dimInfo.getDefaultString());
	         return retval != null ? retval : "";
	      }
	  }

	  public static int getRsetInt(ResultSet rset, int index) throws SQLException {
	     return getRsetInt(rset,index, Misc.getUndefInt());
	  }

	  public static int getRsetInt(ResultSet rset, int index, int undefValue) throws SQLException {
	     int i = rset.getInt(index);
	     if (rset.wasNull())
	        return undefValue;
	     return i;
	  }


	  public static long getRsetLong(ResultSet rset, int index) throws SQLException {
	     long i = rset.getLong(index);
	     if (rset.wasNull())
	        return Misc.getUndefInt();
	     return i;
	  }
	  public static double getRsetDouble(ResultSet rset, int index) throws SQLException {
	     return getRsetDouble(rset,index, Misc.getUndefDouble());
	  }
	  public static double getRsetDouble(ResultSet rset, int index, double undefValue) throws SQLException {
	     double i = rset.getDouble(index);
	     if (rset.wasNull())
	        return undefValue;
	     return i;
	  }
	  public static double getRsetFloat(ResultSet rset, int index) throws SQLException {
	     return getRsetFloat(rset,index, (float)Misc.getUndefDouble());
	  }
	  public static double getRsetFloat(ResultSet rset, int index, float undefValue) throws SQLException {
	     float i = rset.getFloat(index);
	     if (rset.wasNull())
	        return undefValue;
	     return i;
	  }




	  public static String getRsetString(ResultSet rset, int index) throws SQLException { //returns "" for null
	     return getRsetString(rset,index,"");
	  }

	  public static String getRsetString(ResultSet rset, int index, String undefVal) throws SQLException { //returns "" for null
	     String i = rset.getString(index);
	     if (rset.wasNull())
	        return undefVal;
	     return i;
	  }

	  public static String getRsetIntAsString(ResultSet rset, int index,String nullval) throws SQLException { // if not found then gives "-"
	     int i = rset.getInt(index);
	     if (rset.wasNull())
	        return nullval;
	     else
	        return Integer.toString(i);
	  }

	  

	  public static String getRsetLongAsString(ResultSet rset, int index) throws SQLException { // if not found then gives "-"
	     long i = rset.getLong(index);
	     if (rset.wasNull())
	        return "-";
	     else
	        return Long.toString(i);
	  }


	  public static String getRsetStringAsString(ResultSet rset, int index) throws SQLException { // if not found then gives "-"
	     String i = rset.getString(index);
	     if (rset.wasNull())
	        return "-";
	     else
	        return i;
	  }
	  
	  public static boolean helperMatchesCondition(HashMap criteriaInfo, ArrayList conditions, Logger log, Connection dbConn, SessionManager session, int projectId, int workspaceId, int alternativeId) throws Exception 
	    {
	        if (criteriaInfo == null)
	           return true;
	        int budgetYear = Misc.getBudgetYear(dbConn);
	        boolean doLogging = log.getLoggingLevel() >= 15;
	        helpDbgPrintCond(conditions, log);
	        Cache cache = session.getCache();
	        for(int i=0,is = conditions == null ? 0 : conditions.size(); i < is; i++)
	        {
	            WkspStepNew.CondPair condPair = (WkspStepNew.CondPair)conditions.get(i);
	            int dimId = condPair.m_dimId;
	            
	            Integer dimIdi = new Integer(condPair.m_dimId);
	            Integer vi = (Integer)criteriaInfo.get(dimIdi);
	            //if(vi == null)
	             //   continue;
	            int v = vi == null ? Misc.getUndefInt() : vi.intValue();
	            if (dimId == 5079 && !Misc.isUndef(v)) { //budget year ... relatively number it
	               v = v - budgetYear - 1001;
	            }
	            boolean match = false;
	            
	            for (int j=0,js=condPair.m_vals.size();j<js;j++) {
	                int cv = ((Integer)condPair.m_vals.get(j)).intValue();
	                if (dimId == 260) { //custom check!!
	                   //capex if (CustomImpl.wkfCheckConditionMet(cv, dbConn, session, projectId, workspaceId, alternativeId)) {
	                   //   match = true;
	                   //   break;
	                   //}
	                }
	                if (dimId == 123) {
	                    if (cache.isAncestor(dbConn, v, cv)) { 
	                       match = true;
	                       break;
	                    }
	                } 
	                if(cv == Misc.G_HACKANYVAL || cv == v)
	                {
	                    match = true;
	                    break;
	                }
	            }
	            if(!match) {
	                log.log("Not match for "+dimIdi.toString()+"["+Integer.toString(v)+"]");
	                return false;
	            }
	        }
	        if (doLogging)
	           log.log("Matches all criteriaInfo");
	        return true;
	    }
	public static void helpDbgPrintCritInfo(HashMap criteriaInfo, Logger log) throws Exception {
	        if (criteriaInfo == null)
	           return;
	        if (log.getLoggingLevel() >= 17) {
	           StringBuilder logInfo = new StringBuilder();
	           logInfo.append("Data Criteria is: Data is:\n");
	           Set ds = criteriaInfo.entrySet();
	           Iterator iter = ds.iterator();
	           Cache cache = Cache.getCacheInstance(null);
	           while (iter.hasNext()) {
	              Map.Entry entry = (Map.Entry)iter.next();
	              Integer key = (Integer) entry.getKey();
	              Integer val = (Integer) entry.getValue();
	              DimInfo dim = DimInfo.getDimInfo(key.intValue());
	              String dimName = dim.m_name;
	              String valName = cache.getAttribDisplayName(dim, val.intValue());
	              logInfo.append(" Dim(").append(key.toString()).append(")").append(dimName).append(" Val(").append(val.toString()).append(")").append(valName);
	           }
	           logInfo.append("\nEnd of Data Criteria");
	           log.log(logInfo,15);
	        }
	    }

	    public static void helpDbgPrintCond(ArrayList conditions, Logger log) throws Exception {
	        if (log.getLoggingLevel() >= 15) {
	           StringBuilder logInfo = new StringBuilder();
	           logInfo.append("Condition being checked:\n");
	           Cache cache = Cache.getCacheInstance(null);
	           for(int i=0,is = conditions == null ? 0 : conditions.size(); i < is; i++)
	           {
	              WkspStepNew.CondPair condPair = (WkspStepNew.CondPair)conditions.get(i);
	              int dimId = condPair.m_dimId;
	              DimInfo dim = DimInfo.getDimInfo(dimId);
	              String dimName = dim.m_name;
	              logInfo.append(dimName).append("(").append(dimId).append(")[");
	              for (int j=0,js=condPair.m_vals.size();j<js;j++) {
	                if (j != 0)
	                  logInfo.append(",");
	                int cv = ((Integer)condPair.m_vals.get(j)).intValue();
	                if (cv == Misc.G_HACKANYVAL) {
	                  logInfo.append("Any");
	                }
	                else {
	                   String valName = cache.getAttribDisplayName(dim, cv);
	                   logInfo.append(valName).append("(").append(cv).append(")");
	                }
	              }
	              logInfo.append("]");
	           }
	           logInfo.append("\nEnd of Condition");
	           log.log(logInfo,15);
	        }
	    }    
	  /// *****************************************************************
	  ///FOLLOWING ARE RELEVANT FOR CAPEX AND INCLUDED TO ENSURE Other code compiles
	    public static final int G_FOR_PROJECT = 0;
	    public static final int G_FOR_ORDER = 1;
	    public static final int G_FOR_SUPPLIER = 2;
	    public static final int G_FOR_CCBS = 3;
	    public static final int G_FOR_INDEQUIP = 4;      
	    public static double DISCOUNT_RATE = 0.15f; //TODO - read from cache must be non final
	public static java.util.Date FISCAL_START = new java.util.Date(2002, 0, 1); // 1st of Jan - the year will be ignored
	//  public static int CURRENT_FISCAL_YEAR = 2002;
	public static int CURRENT_FISCAL_YEAR =  Misc.getCurrentTime().getYear() + 1900;
	  public static int CURRENT_FISCAL_QUARTER = 0; // Inclusively to start counting time period from current year, current fiscal quarter
	  //public static int CURRENT_FISCAL_MONTH = 0; //1 .. 12
	  public static int CURRENT_FISCAL_MONTH = Misc.getCurrentTime().getMonth();//-12;
	  public static java.sql.Date getCurrentDate() {
	     return new java.sql.Date(CURRENT_FISCAL_YEAR-1900, CURRENT_FISCAL_MONTH, 1);
	  }
	  public static int getBudgetYear(Connection dbConn) throws Exception {
	     try {
	        if (true)
	           return Misc.G_BUDGET_YEAR;
	        if (CURRENT_FISCAL_MONTH > 9)
	           return CURRENT_FISCAL_YEAR+1;
	        else
	           return CURRENT_FISCAL_YEAR;
	     }
	     catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	     }
	  }
	  
	  public static int getBudgetYearTimeId(Connection dbConn) throws Exception {
	     int by = getBudgetYear(dbConn);
	     return (by-1900)*12*35;
	  }

	  public static int getCurrentYearTimeId() {
	     return (CURRENT_FISCAL_YEAR-1900)*12*35;
	  }

	  public static java.sql.Date getBudgetDate(Connection dbConn) throws Exception {
	     try {
	        int year = getBudgetYear(dbConn);

	        return new java.sql.Date(year-1900,0,1);
	     }
	     catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	     }
	  }
	  public static boolean G_HINT_ORDER_OF_JOIN = false;
	   public static boolean G_HINT_FORCE_PLAN = false;
	   public static boolean G_HINT_LOOP_MEASURE = true;
	   public static boolean G_HINT_INCLUDE_USER_PRIV = false;
	   public static boolean G_HINT_LOOP_ALL = false;
	   public static String G_HINT_OPTION_CLAUSE = null;   
	   public static boolean G_HINT_RECOMPILE = false;
	  
	  
	  
	  public static ArrayList<MiscInner.PairIntStr> getLovList(Connection dbConn, String query, int addnlParam) throws Exception { //query 1st col id, 2nd col name. Return of ArrayList of MiscInner.PairIntStr
		  try {
			 PreparedStatement ps = dbConn.prepareStatement(query);
			 if (!Misc.isUndef(addnlParam))
				 ps.setInt(1, addnlParam);
			 ArrayList<MiscInner.PairIntStr> retval = getLovList(ps);
			 ps.close();
			 return retval;
		  }
		  catch (Exception e) {
			  e.printStackTrace();
			  throw e;
		  }
	  }
	  public static ArrayList<MiscInner.PairIntStr> getLovList(PreparedStatement ps) throws Exception { //query 1st col id, 2nd col name. Return of ArrayList of MiscInner.PairIntStr
		  try {
			 
			 ArrayList<MiscInner.PairIntStr> retval = new ArrayList<MiscInner.PairIntStr>();
			 ResultSet rs = ps.executeQuery();
			 while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				retval.add(new MiscInner.PairIntStr(id,name));
			 }
			 rs.close();
			 return retval;
		  }
		  catch (Exception e) {
			  e.printStackTrace();
			  throw e;
		  }
	  }
	  
	  public static void mergePairIntStrArray(ArrayList<MiscInner.PairIntStr> toList, ArrayList<MiscInner.PairIntStr> fromList) {
		  //assumes toList is unique
		  int toListSz = toList.size();
		  for (int i=0,is = fromList.size();i<is;i++) {
			  int id = fromList.get(i).first;
			  boolean found = false;
			  for (int j=0;j<toListSz;j++) {
				  if (toList.get(j).first == id) {
					  found = true;
					  break;
				  }
			  }
			  if (!found)
				  toList.add(fromList.get(i));
		  }
	  }
	  public static String getNameFromArrayPairIntStr(ArrayList<MiscInner.PairIntStr> valList, int selectedId) {
		  for (int i=0,is=valList == null ? 0 : valList.size(); i<is; i++) {
			  if (valList.get(i).first == selectedId)
				  return valList.get(i).second;
		  }
		  return Integer.toString(selectedId);
	  }
	  
	  public static StringBuilder printOptionsArrayPairIntStr(ArrayList<MiscInner.PairIntStr> valList, int selectedId, boolean doAny, String anyInstruct) {
		     StringBuilder retvalBuf = new StringBuilder();
		     int retvalId = Misc.getUndefInt();
		     boolean isFirst = true;	     
		     boolean doFirst = false;
		     if (Misc.isUndef(selectedId)) {
		        doFirst = true;
		     }
		     
		     if (doAny) {
		    	 if (anyInstruct == null || anyInstruct.length() == 0)
		    		 anyInstruct = "&lt; Select &gt;";
		    	
		    	 retvalBuf.append("<option value=\"").append("").append("\" ").append(doFirst?"selected":"").append(">").append(anyInstruct).append("</option>");
		    	 retvalId = -1000;
		    	 isFirst = false;
		     }
		     
		     for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {
		        
		        MiscInner.PairIntStr valInfo = (MiscInner.PairIntStr) valList.get(i);
		        int id = valInfo.first;
		        String nameStr = valInfo.second;
		          if (isFirst) {
		             retvalId = id;
		          }
		          boolean doSelected = id == selectedId;
		          if (doSelected)
		             retvalId = id;
		          if (isFirst && !doSelected && doFirst)
		             doSelected = true;
		          isFirst = false;	        
		        retvalBuf.append("<option value=\"").append(id).append("\" ").append(doSelected?"selected":"").append(">").append(nameStr).append("</option>");
		     }	     
		     return retvalBuf;	  	  
	  }
	  public static StringBuilder printXMLPairIntStr(ArrayList<MiscInner.PairIntStr> valList) {
		     StringBuilder retvalBuf = new StringBuilder();
		     retvalBuf.append("<t>");
		     
		     for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {
		        
		        MiscInner.PairIntStr valInfo = (MiscInner.PairIntStr) valList.get(i);
		        int id = valInfo.first;
		        String nameStr = valInfo.second;	          	        
		        retvalBuf.append("<d i=\"").append(id).append("\" n=\"").append(nameStr).append("\"/>");
		     }	     
		     retvalBuf.append("</t>");
		     
		     return retvalBuf;	  	  
	   }
	  
	  public static void printCSVPairIntStr(ArrayList<MiscInner.PairIntStr> valList, StringBuilder retvalBuf) {
		     boolean isFirst = true;
		     for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {
		        MiscInner.PairIntStr valInfo = (MiscInner.PairIntStr) valList.get(i);
		        int id = valInfo.first;
		        String nameStr = valInfo.second;
		        if (!isFirst)
		        	retvalBuf.append(",");
		        retvalBuf.append(id).append(",").append(nameStr);
		        isFirst = false;
		     }	     
	  }
	  public static java.sql.Timestamp getParamAsTimestamp(String dateStr, java.util.Date defaultDate, SimpleDateFormat formatter) {
		     java.sql.Timestamp retval = null;
		     try {
			     if (formatter != null) { 
			         retval = new java.sql.Timestamp(formatter.parse(dateStr).getTime());
			     }
		         else {
		        	 java.util.Date dt1 = null;
		        	 synchronized (Misc.m_dateFormatter) {
		                 dt1 = Misc.m_dateFormatter.parse(dateStr);
		        	 }
		            if (dt1 != null)
		                retval = new java.sql.Timestamp(dt1.getTime());	       
		         }
		     }
		     catch (Exception e) {
		    	 
		     }
		     if (retval == null && defaultDate != null)
		       retval = new java.sql.Timestamp(defaultDate.getTime());
		     return retval;     
		  }
	  public static java.util.Date getParamAsDate(String dateStr, java.util.Date defaultDate, SimpleDateFormat formatter) {
		  return getParamAsDate(dateStr, defaultDate, formatter, null);
	  }
	  	public static java.util.Date getParamAsDate(String dateStr, java.util.Date defaultDate, SimpleDateFormat formatter, SimpleDateFormat altFormatter) {
		     java.util.Date retval = null;
		     String origDateStr = dateStr;
		     try {
		    	 if (formatter == null)
		    		 formatter = m_dateFormatter;
			     if (formatter != null) { 
			    	 try {
			    		 retval = formatter.parse(dateStr);
			    	 }
			    	 catch (Exception e) {
			    		 //issue of missing time part or having time part...
			    		 int posColon = dateStr == null || dateStr.length() == 0 ? -1 : dateStr.indexOf(':');
			    		 if (posColon != -1) {//may be formatter does not have hh:mm:ss
			    			 dateStr = dateStr.substring(0, dateStr.indexOf(' '));
			    		 }
			    		 else {//may be formatter has hh:mm:ss
			    			 dateStr = dateStr+" 00:00";
			    		 }
			    		 retval = formatter.parse(dateStr);
			    	 }
			     }
		     }
		     catch (Exception e) {
		     }
		     if (retval == null && altFormatter != null) {
		    	 retval = getParamAsDate(origDateStr, defaultDate, altFormatter, null);
		     }
		     if (retval == null && defaultDate != null)
		       retval = new java.util.Date(defaultDate.getTime());
		     if (retval != null) {
		    	 retval.setTime(((long)retval.getTime()/1000)*1000);
		    	 retval.setSeconds(retval.getSeconds());
		     }
		     return retval;     
		  }

	  	public static String printDateWithInpReadWriteMode(SimpleDateFormat df, java.util.Date dt, boolean readOnly, String inpName, boolean doingHHMM) {
	  		return printDateWithInpReadWriteMode(df, dt, readOnly, inpName, doingHHMM, null);
	  		  	
	  	}
	  	
	  	public static String printDateWithInpReadWriteMode(SimpleDateFormat df, java.util.Date dt, boolean readOnly, String inpName, boolean doingHHMM, String jscript) {
	  		StringBuilder sb = new StringBuilder();
	  		if (readOnly) {
	  			sb.append("<input type='hidden' name='").append(inpName).append("' value='").append(printDate(df, dt)).append("'/>").append(printDate(df,dt));
	  		}
	  		else {
	  			sb.append("<input ").append(jscript == null ? "" : jscript).append("readonly='readonly' class='tn'  type='text' size='10' name='").append(inpName).append("' value='").append(printDate(df, dt)).append("'");
	  			sb.append(" onclick='callCalendar(this,this, \"").append(inpName).append("\", \"").append(Misc.G_DEFAULT_DATE_FORMAT).append("\", ").append(doingHHMM ?"true": "false").append(")'/>");
	  		}
	  		return sb.toString();
	  	}
	  	
	  	public static String printDate(SimpleDateFormat df, long dt) {
	  		return printDate(df, Misc.isUndef(dt) ? null : new java.util.Date(dt));
	  	}
	  	
	  	public static String printDate(SimpleDateFormat df, java.util.Date dt) {
	  		if (df == null)
	  			return Misc.getPrintableDateSimple(dt);
	  		else if (dt == null)
	  	    	return "";
	  	    else
	  	    	return df.format(dt);
	  	}
	  	
	  	public static int getRsetInt(ResultSet rset, String colName) throws SQLException {
	  	     return getRsetInt(rset,colName, Misc.getUndefInt());
	  	  }

	  	  public static int getRsetInt(ResultSet rset, String colName, int undefValue) throws SQLException {
	  	     int i = rset.getInt(colName);
	  	     if (rset.wasNull())
	  	        return undefValue;
	  	     return i;
	  	  }


	  	  public static long getRsetLong(ResultSet rset, String colName) throws SQLException {
	  	     long i = rset.getLong(colName);
	  	     if (rset.wasNull())
	  	        return Misc.getUndefInt();
	  	     return i;
	  	  }
	  	  
	  	  public static double getRsetDouble(ResultSet rset, String colName) throws SQLException {
	  	     return getRsetDouble(rset, colName, Misc.getUndefDouble());
	  	  }
	  	  public static double getRsetDouble(ResultSet rset, String colName, double undefValue) throws SQLException {
	  	     double i = rset.getDouble(colName);
	  	     if (rset.wasNull())
	  	        return undefValue;
	  	     return i;
	  	  }
	  	  public static double getRsetFloat(ResultSet rset, String colName) throws SQLException {
	  	     return getRsetFloat(rset,colName, (float)Misc.getUndefDouble());
	  	  }
	  	  public static double getRsetFloat(ResultSet rset, String colName, float undefValue) throws SQLException {
	  	     float i = rset.getFloat(colName);
	  	     if (rset.wasNull())
	  	        return undefValue;
	  	     return i;
	  	  }




	  	  public static String getRsetString(ResultSet rset, String colName) throws SQLException { //returns "" for null
	  	     return getRsetString(rset,colName,"");
	  	  }

	  	  public static String getRsetString(ResultSet rset, String colName, String undefVal) throws SQLException { //returns "" for null
	  	     String i = rset.getString(colName);
	  	     if (rset.wasNull())
	  	        return undefVal;
	  	     return i;
	  	  }

	  	  public static String getRsetIntAsString(ResultSet rset, String colName, String nullval) throws SQLException { // if not found then gives "-"
	  	     int i = rset.getInt(colName);
	  	     if (rset.wasNull())
	  	        return nullval;
	  	     else
	  	        return Integer.toString(i);
	  	  }

	  	  

	  	  public static String getRsetLongAsString(ResultSet rset, String colName) throws SQLException { // if not found then gives "-"
	  	     long i = rset.getLong(colName);
	  	     if (rset.wasNull())
	  	        return "-";
	  	     else
	  	        return Long.toString(i);
	  	  }


	  	  public static String getRsetStringAsString(ResultSet rset, String colName) throws SQLException { // if not found then gives "-"
	  	     String i = rset.getString(colName);
	  	     if (rset.wasNull())
	  	        return "-";
	  	     else
	  	        return i;
	  	  }
	  	  public static String getRsetDateAsString(ResultSet rset, String colName,SimpleDateFormat sdf) throws SQLException { // if not found then gives "-"
	  	     Timestamp i = rset.getTimestamp(colName);
	  	     if (rset.wasNull())
	  	        return "";
	  	     else{
	  	    	if(sdf == null)
	  	    		sdf = new SimpleDateFormat(G_DEFAULT_DATE_FORMAT_HHMM);
	  	        return sdf.format(new java.util.Date(i.getTime()));
	  	     }
	  	  }
	  	  
	  	  public static int getUserTrackControlOrg(SessionManager session) throws Exception {
	  		 int retval = Misc.getParamAsInt(session.getParameter("pv9016"));
	  		 if (Misc.isUndef(retval)) {
	   			  PrivInfo.TagInfo rwTagInfo = session.getCache().getPrivId("tr_usercontrol_org");
	  			  int privIdForOrg = rwTagInfo == null ? Misc.getUndefInt() : rwTagInfo.m_read;
	  			  User user = session.getUser();
	  			  retval = user.getUserSpecificDefaultPort(session, Misc.G_TOP_LEVEL_PORT, privIdForOrg, DimInfo.getDimInfo(2));
	  			  user.setUserPrefField("pv9016", Integer.toString(retval));
	  			  session.setAttribute("pv9016", Integer.toString(retval), false);  			
	  		 }
	  		 return retval;
	  	  }
	  	  
	  	  public static long utilToLong(java.util.Date utilDate) {
	  		  return utilDate == null ? Misc.getUndefInt() : utilDate.getTime();
	  	  }
	  	public static java.sql.Timestamp utilToSqlDate(java.util.Date utilDate) {
	       
			if (utilDate != null && utilDate instanceof java.util.Date) {
				return new java.sql.Timestamp(utilDate.getTime());
			}
			return null;
		}
	  	
		public static java.sql.Timestamp utilToSqlDate(long utilDate) {
		       
			if (utilDate > 0) {
				return new java.sql.Timestamp(utilDate);
			}
			return null;
		}
	  	
		public static java.sql.Timestamp utilToSqlDate(Long utilDate) {
			return utilToSqlDate(utilDate == null ? Misc.getUndefInt() : utilDate.longValue());
		}
	  	
	  	
	  	public static java.sql.Timestamp longToSqlDate(long longDate) {
		       
			if (longDate > 0) {
				return new java.sql.Timestamp(longDate);
			}
			return null;
		}
	  	
	  	public static java.util.Date longToUtilDate(long longDate) {
	  		if (longDate > 0) {
				return new java.util.Date(longDate);
			}
			return null;
	  	}
	  	
	  	public static java.util.Date sqlToUtilDate(java.sql.Timestamp sqlTimeStamp) {
	  		if (sqlTimeStamp != null) {
	  			long ts = sqlTimeStamp.getTime();
	  			ts = ((long)(ts/1000)) * 1000; //get rid of any milli second part
	  			java.util.Date retval = new java.util.Date(ts);
	  			retval.setSeconds(retval.getSeconds()); //some how fastime is still getting to be set
	  			return retval;
	  			
	  		}
	  		return null;
	  	}
	  	
	  	public static long sqlToLong(java.sql.Timestamp sqlTimeStamp) {
	  		if (sqlTimeStamp != null) {
	  			long ts = sqlTimeStamp.getTime();
	  			ts = ((long)(ts/1000)) * 1000; //get rid of any milli second part
//	  			java.util.Date retval = new java.util.Date(ts);
//	  			retval.setSeconds(retval.getSeconds()); //some how fastime is still getting to be set
	  			return ts;
	  			
	  		}
	  		return Misc.getUndefInt();
	  	}
	  	public static long sqlToLong(java.sql.Date dt) {
	  		if (dt != null) {
	  			long ts = dt.getTime();
	  			ts = ((long)(ts/1000)) * 1000; //get rid of any milli second part
//	  			java.util.Date retval = new java.util.Date(ts);
//	  			retval.setSeconds(retval.getSeconds()); //some how fastime is still getting to be set
	  			return ts;
	  			
	  		}
	  		return Misc.getUndefInt();
	  	}
	  	
	  	public static java.util.Date getDate(ResultSet rs, int index) throws Exception {
	  		return sqlToUtilDate(rs.getTimestamp(index));
	  	}
	  	
	  	public static java.util.Date getDate(ResultSet rs, String colName) throws Exception {
	  		return sqlToUtilDate(rs.getTimestamp(colName));
	  	}
	  	
	  	public static long getDateInLong(ResultSet rs, int index) throws Exception {
	  		return sqlToLong(rs.getTimestamp(index));
	  	}
	  	
	  	public static long getDateInLong(ResultSet rs, String colName) throws Exception {
	  		return sqlToLong(rs.getTimestamp(colName));
	  	}

	  	public static String printString(String s) {
	  		return printString(s, true);
	  	}
	  	
	  	public static String printString(String s, boolean doNBSP) {
	  		return s != null ? s : doNBSP ? nbspString : emptyString;
	  	}
	  	
	  	public static String printDouble(double d) {
	  		return printDouble(d, true);
	  	}
	  	public static String printDouble(double d, boolean doNBSP) {
	  		return Misc.isUndef(d) ? (doNBSP ? nbspString : emptyString) : Double.toString(d);
	  	}
	  	public static String printDouble(double d, String defaultStr, int decAfter) {
	  		if (defaultStr == null)
	  			defaultStr = "";
	  		if (Misc.isUndef(d))
	  			return defaultStr;
	  		return String.format("%."+decAfter+"f", d);	  		
	  	}
	  	public static String printDate(SimpleDateFormat sdf, Date dt, String defaultStr) {
	  		if (dt == null)
	  			return defaultStr;
	  		return sdf.format(dt);
	  	}
	  	public static String printDate(SimpleDateFormat sdf, long dt, String defaultStr) {
	  		if (dt <=0 )
	  			return defaultStr;
	  		return sdf.format(new java.util.Date(dt));
	  	}
	  	public static String printInt(int i) {
	  		return printInt(i, true);
	  	}
	  	
	  	public static String printInt(int i, boolean doNBSP) {
	  		return Misc.isUndef(i) ? (doNBSP ? nbspString : emptyString) : Integer.toString(i);
	  	}
	  	public static String printInt(int i, String defaultStr) {
	  		return Misc.isUndef(i) ? (defaultStr == null ? "" : defaultStr) : Integer.toString(i);
	  	}
	    public static String getDisplayName(ArrayList<MiscInner.PairIntStr> theList, int selVal, String unMatchedString) {//if unMatchedString is null and no match then retrun Integer.toString(selVal) if not found in list
	    	for (int i=0,is = theList == null ? 0 : theList.size(); i < is; i++) {
	    		if (selVal == theList.get(i).first)
	    			return theList.get(i).second;
	    	}
	    	return unMatchedString == null ? Integer.toString(selVal) : unMatchedString;
	    }
	    
	    public static String convertJavaDataFormatToMySQL(String dateFormat) {//TODO completely
	    	StringBuilder retval = new StringBuilder();
	    	String[] parts = dateFormat.split("/");
	    	for (int i=0,is=parts.length;i<is;i++) {
	    		if (i != 0)
	    			retval.append("/");
	    	    if (parts[i].startsWith("d"))
	    	    	retval.append("%d");
	    	    else if (parts[i].startsWith("M"))
	    	    	retval.append("%m");
	    	    else 
	    	    	retval.append("%y");
	    	}
	    	return retval.toString();
	    }
	    
	    public static int  junkCanRemByteArrMatches(byte[] prev, byte [] curr, byte[] matchThis, int prevSz, int prevStart, int currSz, int currStart) {
	    	//will return the pos in curr that matches matchThis - but will assume upto matchThis.length-1 bytes may be in prev if prevSz > 0
	    	//if no match found then will return Misc.getUndefInt();
	    	int matchThisSz = matchThis.length;
	    	int maxThatCanBeLookedInPrev = (matchThisSz-1) <= (prevSz-prevStart) ? (matchThisSz-1) : prevSz-prevStart;
	    	for (int i=currStart-maxThatCanBeLookedInPrev;i<currSz;i++) {
	    		boolean matches = true;
	    		for (int j=0;j<matchThisSz;j++) {
	    			byte rh = matchThis[j];
	    			byte lh = -1;
	    			boolean lhValid = false;
	    			int lhDesired = i+j;
	    			if (lhDesired<currStart) {//look in prev
	    				lhValid = true;
	    				lh = prev[prevSz+lhDesired-currStart];
	    			}
	    			else if (lhDesired < currSz) {
	    				lh = curr[lhDesired];
	    				lhValid = true;;
	    			}
	    			if (!lhValid || lh != rh) {
	    				matches = false;
	    				break;
	    			}
	    		}
	    		if (matches)
	    			return i;
	    	}
	    	return Misc.getUndefInt();
	    }
	    
	    public static void junkCanRemCopySqlFile(String fullf1, String fullf2) {
	    	if (fullf1 == null)
	    		fullf1 = "E:\\temp\\logged_data_laf\\lgd.sql";
	    	if (fullf2 == null)
	    		fullf2 = "E:\\temp\\logged_data_laf\\lgd_mod.sql";
	    	try {
		      FileInputStream inp = new FileInputStream(fullf1);
		      FileOutputStream outp = new FileOutputStream(fullf2);
		      FileOutputStream outp2 = new FileOutputStream(fullf2+"_trc");
		      byte buf1[] = new byte[8192];
		      byte buf2[] = new byte[8192];
		      boolean bufIs1 = true;
		      byte buf[] = buf1;
		      int prevSz = 0;
		      int prevStart = 0;
		      
	          boolean done = false;
	          String fm = "INSERT INTO `log";
	          byte fmb[] = fm.getBytes();
	          String em = "/*";
	          byte emb[] = em.getBytes();
	          
	          boolean fmSeen = false;
	          
	          for(int currStart = 0,  currSz = 0;!done;buf = bufIs1 ? buf2 : buf1, bufIs1 = !bufIs1, prevStart = currStart, prevSz = currSz) {	        	  
	             int bRead = inp.read(buf,0,8192);
	             currStart = 0;
	             currSz = bRead;
	             boolean toWriteOutp2 = false;
	             if (bRead == 0 || bRead == -1)
	               done = true;
	            else {
	            	if (!fmSeen) {
	            		int writeFrom = junkCanRemByteArrMatches(bufIs1 ? buf2 : buf1, bufIs1 ? buf1 : buf2, fmb, prevSz, prevStart, currSz, currStart);
	            		if (Misc.isUndef(writeFrom))
	            			continue;
	            		fmSeen = true;
	            		toWriteOutp2 = true;
	            		if (writeFrom < currStart) {
	            			outp.write(bufIs1 ? buf2 : buf1, prevSz-(currStart-writeFrom),currStart-writeFrom);
	            			if (toWriteOutp2)
	            				outp2.write(bufIs1 ? buf2 : buf1, prevSz-(currStart-writeFrom),currSz-currStart);
	            			writeFrom = 0;
	            		}
	            		currStart = writeFrom;
	            	}
	            	int emMarker = junkCanRemByteArrMatches(bufIs1 ? buf2 : buf1, bufIs1 ? buf1 : buf2, emb, prevSz, prevStart, currSz, currStart);
	            	if (!Misc.isUndef(emMarker)) {
	            		//done = true;
	            		fmSeen = false;
	            		if (emMarker < currStart) {//we need to do * */
	            			byte dummy[] = "* */".getBytes();
	            			outp.write(dummy, 0,dummy.length);
	            			continue;
	            		}
	            		currSz = emMarker;
	            	}
	            	if (currSz-currStart>0) {
	            		outp.write(bufIs1 ? buf1 : buf2, currStart,currSz-currStart);
            			if (toWriteOutp2)
            				outp2.write(bufIs1 ? buf2 : buf1, currStart,currSz-currStart);

	            	}
	            }
	 		}
	        inp.close();
	        outp.close();
	        outp2.close();
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    		//eat it
	    	}
		}//end of func
	    public static com.ipssi.gen.utils.MiscInner.SearchBoxHelper preprocessForSearchBeforeView(HttpServletRequest request, String defaultPageContext) throws Exception {
			  SessionManager _session = InitHelper.helpGetSession(request);
			   
			  MiscInner.SearchBoxHelper searchBoxHelper = (MiscInner.SearchBoxHelper) _session.getAttributeObj("_search_box_helper");
			  if (searchBoxHelper != null)
				  return searchBoxHelper;
			  User _user = InitHelper.helpGetUser(request);
			  String pgContext = Misc.getParamAsString(_session.getParameter("page_context"), defaultPageContext);	  
				 
			  _user.loadParamsFromMenuSpec(_session, pgContext); //load any parameters that may have been set at menu level	  
			 
			
			  int privIdForOrg = _user.getPrivToCheckForOrg(_session, pgContext); //this tells the privilege to use for showing the Org tree
			  //is obtained from a complicated set of rules - the priv id might be mentioned as parameter or it might be associated with top
			  //menu level tag or might be the current menu tag
			   
			   String pageSearchConfigFile = Misc.getParamAsString(_session.getParameter("_page_search"), "tr_std_search.xml");		  
			   com.ipssi.gen.utils.PageHeader pageSearchHeader = pageSearchConfigFile == null || pageSearchConfigFile.length() == 0 ? null : com.ipssi.gen.utils.PageHeader.getPageHeader(pageSearchConfigFile, _session.getConnection(), _session.getCache());
			   ArrayList paramConfigInfo = pageSearchHeader != null ? pageSearchHeader.m_allRows : null;
			   com.ipssi.gen.utils.FrontGetValHelper  valGetter = pageSearchHeader != null ? pageSearchHeader.m_valGetter : null; //dont worry about this struct
			   searchBoxHelper = PageHeader.processSearchBox(_session, privIdForOrg, pgContext, paramConfigInfo, valGetter);
		      _session.setAttributeObj("_search_box_helper", searchBoxHelper);
		      return searchBoxHelper;
		  }
	    public static String getEmailReportServer(){
	    	return emailReportServer;
	    }
	    
	    public static String getFullURLStringReqFirst(SessionManager session, boolean getOnlyQuery, String urlToUse) {
	    	StringBuilder sb = new StringBuilder();
	    	Map paramvals = session.request.getParameterMap();
	    	Set s = paramvals.entrySet();
	    	Iterator it = s.iterator();
	    	HashMap<String, String> valseen = new HashMap<String, String>();
	    	while (it.hasNext()) {
	    		Map.Entry<String,String[]> entry = (Map.Entry<String,String[]>)it.next();
	    		String key = entry.getKey();
	    		String val[] = entry.getValue();
	    		if (val == null || val.length == 0 || key == null || key.length() == 0)
	    			continue;
	    		valseen.put(key, key);
	    		key = java.net.URLEncoder.encode(key);
	    		for (int i=0,is=val.length; i<is;i++) {
	    			String v = val[i];
	    			if (v == null || v.length() == 0)
	    				continue;
	    			v = java.net.URLEncoder.encode(v);
	    			if (sb.length() != 0)
	    				sb.append("&");
	    			sb.append(key).append("=").append(v);
	    		}
	    	}
	    	session.getSessionAsQueryString(sb, valseen);
	    	if (getOnlyQuery)
	    		return sb.toString();
	    	StringBuilder retval = new StringBuilder();
	    	retval.append(session.request.getScheme()).append("://").append(session.request.getServerName()).append(":").append(session.request.getServerPort());
	    	if (urlToUse == null)
	    		urlToUse =session.request.getRequestURI(); 
	    	retval.append(urlToUse);
	    	;
	    	if (sb.length() > 0) {
	    		if (urlToUse.indexOf('?') >= 0)
	    			retval.append("&");
	    		else
	    			retval.append("?");
	    		retval.append(sb);
	    	}
	    	return retval.toString();
	    }
	    
	    public static int g_recordSrcId = -1;
		
		public static int getRecordSrcId(Connection conn) {
			if (g_recordSrcId < 0) {
				PreparedStatement ps = null;
				ResultSet rs = null;
				if ("/".equals(System.getProperty("file.separator")))
					g_recordSrcId = Integer.MAX_VALUE;
				else {
					try {
						Misc.loadCFGConfigServerProp();
						g_recordSrcId = Integer.MAX_VALUE;
						String uid = SECLWorkstationDetails.getUID(Misc.g_uidPrefix);
						ps =  conn.prepareStatement("select id from secl_workstation_details where uid = ? and status=1");
						ps.setString(1, uid);
						rs=  ps.executeQuery();
						if (rs.next()) {
							g_recordSrcId = Misc.getRsetInt(rs, 1, Integer.MAX_VALUE);
						}
						rs = Misc.closeRS(rs);
						ps = Misc.closePS(ps);
					}
					catch (Exception e) {
						g_recordSrcId = Integer.MAX_VALUE;
					}
					finally {
						rs = Misc.closeRS(rs);
						ps = Misc.closePS(ps);
					}
				}
			}
			return g_recordSrcId;
		}
		
		public static void resetAllowNextLogin(Connection conn, int userId) {
			PreparedStatement ps = null;
			try {
				ps = conn.prepareStatement("update users set allow_next_login=0 where id=?");
				ps.setInt(1, userId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
			catch (Exception e) {
				e.printStackTrace();
				
			}
			finally {
				ps = Misc.closePS(ps);
			}
		}
		
		public static boolean checkIfUserComingAfterLongTime(Connection conn, int userId) {
			boolean retval = false;
			if (Misc.g_nonUseExpiry <= 0)
				return retval;
			//returns true if the user is coming back after long time now-last login > Misc.g_nonUseExpiry and allow_next_login is null or 1
			//if returning false then will set allow_next_login to 0 ... else will let it be
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = conn.prepareStatement("select allow_next_login, max(ts), created_on from users left outer join user_login_track on (users.id = user_id) where id = ? ");
				ps.setInt(1, userId);
				rs = ps.executeQuery();
				long lastLogin = -1;
				int allowed = 0;
				if (rs.next()) {
					allowed = rs.getInt(1);
					lastLogin = Misc.sqlToLong(rs.getTimestamp(2));
					if (lastLogin <= 0)
						lastLogin = Misc.sqlToLong(rs.getTimestamp(3));
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				if (allowed == 0 && (lastLogin == 0 || (double)(System.currentTimeMillis() - lastLogin)/(24.0*3600.0*1000.0) >Misc.g_nonUseExpiry))
					retval = true;
			}
			catch (Exception e) {
				e.printStackTrace();
				
			}
			finally {
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
		    return retval;
		}
		
		public static ArrayList<MiscInner.PairIntStr> getNotificationTypes(Connection conn) {
			//dummy
			ArrayList<MiscInner.PairIntStr> retval = new ArrayList<MiscInner.PairIntStr>();
			retval.add(new MiscInner.PairIntStr(3,"Reaching Plant"));//1 cannot be used because we keep minus in sel box and somehow -1 is harcoded to be treated as undef ie no selection
			retval.add(new MiscInner.PairIntStr(2,"Delayed Enroute"));
			return retval;
		}
		
		private static HashMap<Integer,Integer> g_ruleWindowSizeList = new HashMap<Integer,Integer>();


		public static void addRuleWindowSize(int ruleId, int windowSize) {			
			g_ruleWindowSizeList.put(ruleId, windowSize);			
		}

		public static ArrayList<Pair<Integer,Integer>> getRuleWindowSizeList() {
			ArrayList<Pair<Integer,Integer>> list = new ArrayList<Pair<Integer,Integer>> ();
			Iterator<Entry<Integer,Integer>> itr = g_ruleWindowSizeList.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Integer,Integer> entry = itr.next();
				list.add(new Pair<Integer,Integer> (entry.getKey(), entry.getValue()));
			}
			return list;
		}
		
}//end of class

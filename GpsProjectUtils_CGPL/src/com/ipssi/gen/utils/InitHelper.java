package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;import javax.servlet.*;
import javax.servlet.http.*;

final public class InitHelper {
   
   static public void  init(HttpServletRequest request, ServletContext application) throws Exception {

      try {
         Logger _log = null;
         String queryInstanceName = request.getParameter("db_server_name") != null && request.getParameter("db_server_name").length() > 0 ? request.getParameter("db_server_name") : Misc.getServerName();
         if ((_log = (Logger) request.getAttribute("_log")) == null) {
             _log = new com.ipssi.gen.utils.Logger(application);
             request.setAttribute("_log", _log);
         }
         _log.context = application;
		    _log.log(request.getRequestURI()+" IN:"+Long.toString(System.currentTimeMillis()),15);
         _log.log("Entering Connection Pooling", 15);
         ThreadAttributes.set("db_server_name",queryInstanceName);// for multi instance connection
         if (Misc.getServerConfigPath() == null) {
            Misc.setCFG_CONFIG_SERVER(application.getInitParameter("config_server"));
            //Misc.CFG_CONFIG_SERVER = application.getInitParameter("config_server");
            Misc.CFG_CONFIG_WEB = application.getInitParameter("config_web");
            Misc.CFG_USERFILES_SAVE = application.getInitParameter("userfiles_save");
            Misc.CFG_USERFILES_VP = application.getInitParameter("userfiles_vp");
            Misc.CFG_CONFIG_WEB_VP = application.getInitParameter("configweb_vp");
            Misc.CFG_ICONFIG_PATH = application.getInitParameter("iconfig_path"); //010706
            String temp = application.getInitParameter("data_ignores_weekend"); //rajeev 062506
            if (temp != null && "".equals(temp)) //rajeev 062506
                Misc.g_fteDataIgnoresWeekend = "1".equals(temp); //rajeev 062506

            if (Misc.CFG_ICONFIG_PATH == null || "".equals(Misc.CFG_ICONFIG_PATH)) {//010706
               Misc.CFG_ICONFIG_PATH = "c:\\iconfig";//010706
            }//010706


            if (Misc.getServerConfigPath() == null || "".equals(Misc.getServerConfigPath())) {
               _log.log("** Not found config path",8);
               //Misc.CFG_CONFIG_SERVER = "C:\\IPSSI\\LocTracker\\config_server";
               Misc.setCFG_CONFIG_SERVER("C:\\IPSSI\\LocTracker\\config_server");
               DBConnectionPool.g_dontUseTomcat = true;
            }
            if (Misc.CFG_CONFIG_WEB == null || "".equals(Misc.CFG_CONFIG_WEB)) {
               Misc.CFG_CONFIG_WEB = "C:\\IPSSI\\LocTracker\\config_web";
               _log.log("** Not found config web",8);
            }
            if (Misc.CFG_USERFILES_SAVE == null || "".equals(Misc.CFG_USERFILES_SAVE)) {
               Misc.CFG_USERFILES_SAVE = "C:\\IPSSI\\LocTracker\\WebContent\\user_files";
               _log.log("** Not found user save",8);
            }
            if (Misc.CFG_USERFILES_VP == null || "".equals(Misc.CFG_USERFILES_VP)) {
               Misc.CFG_USERFILES_VP = "/user_files";
               _log.log("** Not found user save vp",8);
            }
            if (Misc.CFG_CONFIG_WEB_VP == null || "".equals(Misc.CFG_CONFIG_WEB_VP)) {
               Misc.CFG_CONFIG_WEB_VP = "/config_web";
               _log.log("** Not config web vp",8);
            }
            //check if the directories required in iconfig (temp, bakups are there - if not create)
            File tempDir = new File(Misc.CFG_ICONFIG_PATH+"\\temp");
            File bakupDir =new File(Misc.CFG_ICONFIG_PATH+"\\bakups");
            if (!tempDir.exists()) {
               tempDir.mkdir();
            }
            if (!bakupDir.exists()) {
               bakupDir.mkdir();
            }

         }
         DBConnectionPool _dbConnPool = (com.ipssi.gen.utils.DBConnectionPool) application.getAttribute("_dbConnPool");
         if (_dbConnPool == null || (!_dbConnPool.serverName.equalsIgnoreCase(queryInstanceName))) {
//             _dbConnPool = new com.ipssi.gen.utils.DBConnectionPool();
             _dbConnPool = DBConnectionPool.getDBConnectionPool();
             //application.setAttribute("_dbServerName", ThreadAttributes.get("db_server_name"));
             application.setAttribute("_dbConnPool", _dbConnPool);
             Cache.g_cache = null;
         }

         Connection _dbConnection = helpGetConnection(_log, request, _dbConnPool);

         Cache _cache = null;
         _cache = Cache.getCacheInstance(_dbConnection);
         application.setAttribute("_cache", _cache);
//         if ((_cache = (Cache) application.getAttribute("_cache")) == null) {
//             _cache = Cache.getCacheInstance();
//             application.setAttribute("_cache", _cache);
//         }


         SessionManager _session = null;
         if ((_session = (SessionManager) request.getAttribute("_session")) == null) {
             _session = new com.ipssi.gen.utils.SessionManager(request, application);
             request.setAttribute("_session", _session);
             _log.setSession(_session);
             String logLevelString = null;
             _log.log("created new session", 15);

//             if ((logLevelString = _session.getAttribute(com.ipssi.gen.utils.Logger.LOGGING_LEVEL_LABEL)) != null)
//                try {
//                   _log.setLoggingLevel(Integer.parseInt(logLevelString));
//                }
//                catch (Exception e) {
				// throw e;
//				}
          }
          _session.request = request;
          _session.context = application;
          _log.log("got a session", 15);

          //Intercept Project comparison related stuff ....
          handleCompStuff(request,_session);

          User _user;
          _user = (com.ipssi.gen.utils.User) request.getAttribute("_user");
          int uid = 0;
          if(request.getParameter("isAndroid") != null && request.getParameter("isAndroid").equalsIgnoreCase("1"))
        	  uid = Misc.getParamAsInt(request.getParameter("user_id"),0);
          else if(request.getParameter("httpClient") != null && request.getParameter("httpClient").equalsIgnoreCase("report"))
        	  uid = Misc.getParamAsInt(request.getParameter("userId"),0);
          else if(request.getParameter("action") != null && request.getParameter("action").equalsIgnoreCase("xmlData")){
        	  _session.doLogout();
        	  _session.doLogin(request.getParameter("username"), request.getParameter("password"));
        	  uid = (int) _session.getUserId();
          }
          else
        	  uid = (int) _session.getUserId();
          if (_user != null && uid != _user.getUserId())
             _user = null;
          //the above case would arise when handle_login happens ... the first time around
          //_user is junk, but then it goes to become a valid user if successful
          if (_user == null) {
             _user = _cache.loadUserInfo(uid, _session.getUserName(), request, application);
          }
          //_user.request = request;
          //_user.context = application;
          _session.setUserPref(_dbConnection);

		  //For Last Login
		  //_session.setAttribute("_lastlogin", Misc.getUserLastLogin(_dbConnection, _user), false);
		  // Call to update the last login in db
		  //Misc.recordUserLogin(_dbConnection, _user);
       }
       catch (com.ipssi.gen.utils.ConnectionTimeoutException e) {
	   System.out.println(request.getRequestURI()+" IN:"+Long.toString(System.currentTimeMillis()));
           e.printStackTrace();
	         throw e;
       }
       catch (SQLException e) {
	   System.out.println(request.getRequestURI()+" IN:"+Long.toString(System.currentTimeMillis()));
           e.printStackTrace();
	         throw e;
       }
   }
   
   static public void handleCompStuff(HttpServletRequest request, SessionManager session) throws Exception  {
     //_clbsp_comp => 2=label, 3=wksp, 4=scenario
     //_clbsp_item => id of the comp being compared
     //_clbsp_prj  => item being compared
     
      int prjId = (int)session.getProjectId();
      int cmpPrjId = Misc.getParamAsInt(session.getAttribute("_clbsp_prj"));
      
      String pjcmp = request.getParameter("_clbsp_comp"); //val of comp      
      int actionId = Misc.getParamAsInt(request.getParameter("_clbsp_action"));
      
      boolean toStopPjCmp = "0".equals(pjcmp);
      boolean doingNew = pjcmp != null;
      
      
      if (pjcmp == null && prjId != cmpPrjId) 
         toStopPjCmp = true;
      if (toStopPjCmp) {         
         session.removeAttribute("_clbsp_item");
         session.removeAttribute("_clbsp_prj");      
         session.removeAttribute("_clbsp_comp");
         return;
      }
      boolean doNothing = pjcmp == null || "-1".equals(pjcmp);
      if (doNothing)
         return;
      boolean doLabel = "2".equals(pjcmp);
      boolean doWksp = "3".equals(pjcmp);
      boolean doScenario = "4".equals(pjcmp);
      boolean doAccept = "100".equals(pjcmp);
      if (doAccept) { //make the attributes non permanent
         String t1 = session.getAttribute("_clbsp_item");
         String t2 = session.getAttribute("_clbsp_prj");      
         String t3 = session.getAttribute("_clbsp_comp");
         
         session.removeAttribute("_clbsp_item");
         session.removeAttribute("_clbsp_prj");      
         session.removeAttribute("_clbsp_comp");
         
         session.setAttribute("_clbsp_item",t1,false);
         session.setAttribute("_clbsp_prj",t2,false);      
         session.setAttribute("_clbsp_comp",t3,false);
         
         return;
      }
      if (!doLabel && !doWksp && !doScenario)
         return;
      
      
      String itemId = request.getParameter("_clbsp_item");
      session.setAttribute("_clbsp_item",itemId,true);
      session.setAttribute("_clbsp_prj",Integer.toString(prjId),true);      
      session.setAttribute("_clbsp_comp",pjcmp,true);
   }
   
   static public Connection regetConnection(HttpServletRequest request, ServletContext application) throws Exception {//will always return null       
       Logger _log = (Logger) request.getAttribute("_log");
       Connection _dbConnection = null;
       DBConnectionPool _dbConnPool = null;
       if ((_dbConnPool = (com.ipssi.gen.utils.DBConnectionPool) application.getAttribute("_dbConnPool")) == null) {
//             _dbConnPool = new com.ipssi.gen.utils.DBConnectionPool();
             _dbConnPool = DBConnectionPool.getDBConnectionPool();
             application.setAttribute("_dbConnPool", _dbConnPool);
       }
       
       if ((_dbConnection = (Connection) request.getAttribute("_dbConnection")) == null) {
            _dbConnection = _dbConnPool.getConnection();
            request.setAttribute("_dbConnection", _dbConnection);
            _dbConnection.setAutoCommit(false); //will be set to true in stopInclude //debug

            _log.log("Got a new db connection",15);
       }
       return _dbConnection;       
   }
   
   static public Connection returnConnectionTemporarily(HttpServletRequest request, ServletContext application) throws Exception {//will always return null
       Logger _log = null;           
      _log = (Logger) request.getAttribute("_log");
      if (_log != null)
         _log.log(request.getRequestURI()+" PAUSE:"+Long.toString(System.currentTimeMillis()),15);
      return helperReturnConnection(_log, request, application);      
   }
   static private Connection helpGetConnection(Logger _log, HttpServletRequest request, DBConnectionPool _dbConnPool) throws Exception {//will always return null
         Connection _dbConnection = null;
         if ((_dbConnection = (Connection) request.getAttribute("_dbConnection")) == null) {
            _dbConnection = _dbConnPool.getConnection();
            request.setAttribute("_dbConnection", _dbConnection);
            _dbConnection.setAutoCommit(false); //will be set to true in stopInclude //debug

            _log.log("Got a new db connection",15);
         }
         return _dbConnection;       
   }
   
   static private Connection helperReturnConnection(Logger _log, HttpServletRequest request, ServletContext application) throws Exception {//will always return null
   
      try {
         DBConnectionPool _dbConnPool = null;
         if ((_dbConnPool = (com.ipssi.gen.utils.DBConnectionPool) application.getAttribute("_dbConnPool")) != null) {
             Connection _dbConnection = null;
             if ((_dbConnection = (Connection) request.getAttribute("_dbConnection")) != null) {
                 if (! _dbConnection.isClosed()) {
                     _dbConnection.commit();
                     _dbConnPool.returnConnection(_dbConnection, false);
                     if (_log != null)
                      _log.log("returned connection (valid)",15);
                 }
                 else {
                     _dbConnPool.returnConnection(_dbConnection, true);
                        if (_log != null)
                         _log.log("returned connection (invalid)",8);
                 }          
             }             
         }
         request.setAttribute("_dbConnection", null);
         return null;         
      }
      catch (Exception e) {
          e.printStackTrace();
          throw e;
      }
   }
   static public void  close(HttpServletRequest request, ServletContext application) throws Exception {
     Logger _log = null;
     _log = (Logger) request.getAttribute("_log");
     if (_log != null)
        _log.log(request.getRequestURI()+" OUT:"+Long.toString(System.currentTimeMillis()),15);
      try {
         User _user = null;
          _user = (com.ipssi.gen.utils.User) request.getAttribute("_user");
          if (_user != null) {
             _user.resetInUse();
          }
         helperReturnConnection(_log, request, application);
         //Runtime currRT = Runtime.getRuntime();
         //if (currRT.freeMemory()/currRT.totalMemory() < 0.4)
          //  System.gc();
      }
      catch (Exception e) {
          e.printStackTrace();
          throw e;
      }
   }
   //following assumes that init has been called
   static public SessionManager helpGetSession(HttpServletRequest request) {
	   return (SessionManager) request.getAttribute("_session");
   }

   static public User helpGetUser(HttpServletRequest request) {
	   return (User) request.getAttribute("_user");
   }
   
   static public Connection helpGetDBConn(HttpServletRequest request) {
       return (Connection) request.getAttribute("_dbConnection");	   
   }
}

/*
 * This sample shows how to list all the names from the EMP table
 *
 *
 */

// You need to import the java.sql package to use JDBC

package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;import javax.servlet.*;
import javax.servlet.http.*;

final public class Logger {

   public static final String LOGGING_LEVEL_LABEL ="_ll";
   public static ServletContext context;
   public static final int ERROR_LOGGING = 0;
   public static final int EXCEPTION_LOGGING = 1;
   public static final int EVENT_LOGGING = 2;
   public static final int STAGE_LOGGING = 3;
   public static final int TIMING_LOGGING = 4;
   public static final int TRACE_LOOGING_LEVEL = 15;
   public static final int DEFAULT_LOGGING_LEVEL = 9;// change later


   //some strings to make operations more efficient
   private static final String tempUID = "uid=";
   private static final String tempUser = " user=";
   private static final String tempSessionId = " SessId=";
   private static final String tempTimeStamp = " time=";

   private static int globalLoggingLevel = 15; // change later to 10

   private static int currentLoggingLevel = globalLoggingLevel;
   private SessionManager session = null;



   public Logger(ServletContext context) {
       super();
       this.context = context;
   }

   public Logger(ServletContext context, int logLevel) {
       super();
       this.context = context;
       this.currentLoggingLevel = logLevel;
   }

   public static void setGlobalLoggingLevel (int level) { globalLoggingLevel = level;}
   public static int getGlobalLoggingLevel() { return globalLoggingLevel; }

   public void setSession(SessionManager session) { this.session = session; }
   public void reset() {
      setLoggingLevel(globalLoggingLevel);
   }
   public int getLoggingLevel() { return currentLoggingLevel; }
   public void setLoggingLevel(int level) {
      if (currentLoggingLevel < level)
         log("Changed Logging level to:"+Integer.toString(level)+ " from:"+Integer.toString(currentLoggingLevel));
      currentLoggingLevel = level;

   }

   public void log(StringBuilder msg, int level) {
      if (msg != null)
      log(msg.toString(), level);
   }
   public void log(int i, int level) {

      log(Integer.toString(i), level);
   }
   public void log(double i, int level) {
      log(Double.toString(i), level);
   }

   public void log(String msg, int level) {
      if (msg != null)
           if (level <= currentLoggingLevel)
              this.log(msg);
   }

   private String getLogString(String msg) {
       try {
          return((session == null) || (session.getAttribute(Misc.UID_PARAMETER_LABEL) == null))? tempTimeStamp+(new java.util.Date()).toString()+ msg : tempUID + session.getUserId() + tempUser + session.getUserName() + tempSessionId  + tempTimeStamp + (new java.util.Date()).toString() + " " + msg;
       }
       catch (Exception e) {
          context.log(msg, e);
          return ("");
       }
   }

   public void log(String msg) {
      if (msg != null)
      context.log(getLogString(msg));
   }

   public void log(String msg, Exception e) {
      if (msg != null)
      context.log(getLogString(msg), e);
   }
}



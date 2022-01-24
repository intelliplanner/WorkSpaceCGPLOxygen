/*
  This includes functions to manage the session.
  It creates a session if the request does not have a session.
  It also provides helper  applications to manage session information

  Significant changes as compared to tips mart
   - Eventually need a validation mechanism to ensure that cookie spoofing doesn't work
   - Cookies are in-memory
   - Are created at start of login and removed at session expiry (yet to be done) or
    at log out (or the browser being terminated)
 */



package com.ipssi.gen.utils;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.SingleSession;

public final class SessionManager{
    /** Constants that are available everywhere **/
    public static final int COOKIE_TYPE = 1;
    public static final int URL_TYPE = 2;
    public static final String INFO_STORED_IN_DB = "1";
    public static final String INFO_STORED_USUAL = "0";
    public static final String COOKIE_NAME_LABEL = "_ipc";

    //Some internal constants for processing the stuff

    private static final String LOGGED_IN = "1";
    private static final String NOT_LOGGED_IN_YET = "0";
    private static final String GUEST_ACC_LABEL = "_gc";
    private static final String GUEST_ACC_MD5_label = "_gm";
    private static final String SECRET_ID = "p3$j!l5c";
    public static String getSecretId() {
    	return SECRET_ID;
    }
    
    private static final String IN_DB_LABEL = "_d";
    private static final String USER_NAME_LABEL  = "_u";
    private static final String LOGGED_IN_LABEL = "_l";
    private static final String LAST_TIME_CONTACTED_LABEL = "_t";
    private static final String USER_ID_LABEL = "_i";
    private static final String SESSION_ID_LABEL = "_ss";
//    private static final String CURRENT_BASE_LABEL = "_c";
//    private static final String CURRENT_SESSION_ID_LABEL = "_s"; // see currentBase, currentCount def below

    private static final int HASH_TABLE_SIZE = 151;

    //Some parameters to determine how to tackle the tracking and numbering of session
//    private static String currentBase;  // the session Id's are tracked by two values - currentBase that
                                        // is obtained from the Database, while currentCount is for
                                        // numbering the session
//    private static int currentSessionID = 0;
    private static int sessionTrackingMechanism = COOKIE_TYPE;
    volatile private static long timeOutValue = Integer.MAX_VALUE; // in mS - in the properties this will be specified in min. and converted
                               // into milliseconds
    private FlexPriv sessionLevelFlexPriv = null;
    private boolean guestParamOK = false;
    public FlexPriv getFlexPriv() {
    	return sessionLevelFlexPriv;
    }
    public boolean isGuestAccessAllowed() {
    	return guestParamOK && sessionLevelFlexPriv != null && sessionLevelFlexPriv.getMenuTag() != null;
    }
    public boolean validateAndSetGuestAccess() {
    	String paramString = this.getParameter(SessionManager.GUEST_ACC_LABEL);
		String md5String = this.getParameter(SessionManager.GUEST_ACC_MD5_label);
		return validateAndSetGuestAccess(paramString, md5String);
    }
    public boolean validateAndSetGuestAccess(String paramString, String md5String)  {//returns false if not a valid string
    	try {
    		
	    	if (paramString != null)
	    		paramString = paramString.trim();
	    	if (md5String != null)
	    		md5String = md5String.trim();
	    	
	    	if (paramString == null || md5String == null || paramString.length() == 0 || md5String.length() == 0)
	    		return false;
	    		
	    	String secret = getSecretId();
	    	String combo = secret+paramString+secret;
	    	String md5calc = Misc.getMD5EncodedString(combo);
	    	if (!md5calc.equals(md5String)) {
	    		guestParamOK = false;
	    		return false;
	    	}
	    	FlexPriv ulevel = this.user == null ? null : this.user.getFlexPriv();
	    	FlexPriv slevel = null;
	    	
	    	String nameValPair[] = paramString.split("&");
	    	for (int i=0,is=nameValPair == null ? 0 : nameValPair.length; i<is; i++) {
	    		String np = nameValPair[i];
	    		if (np != null)
	    			np = np.trim();
	    		if (np == null || np.length() == 0)
	    			continue;
	    		String comp[] = np.split("=");
	    		String n = comp.length != 2 ? null : comp[0];
	    		String p = comp.length != 2 ? null : comp[1];
	    		if (n != null)
	    			n = n.trim();
	    		if (p != null)
	    			p = p.trim();
	    		if (n == null || n.length() == 0 || p == null || p.length() == 0)
	    			continue;
	    		if (slevel == null) {
	    			slevel = ulevel == null ? new FlexPriv() : ulevel.clone();
	    		}
	    		slevel.add(n,p,false);
	    	}
	    	this.sessionLevelFlexPriv = slevel;
	    	return true;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		return false;
    	}
    }
    public static void setTimeOutValue(int timeOutMin) {
    	SessionManager.timeOutValue = timeOutMin <= 0 ? Integer.MAX_VALUE : timeOutMin*60*1000;
    }

   // static {
    //    init();
    //}

    // Some instance variables for keeping track of the session
    private HashMap<String, Object> valueTable = new HashMap (HASH_TABLE_SIZE,(float) 0.2); // key is string values are objects
    private HashMap<String, Object> rememberValueTable = null;
    private HashMap<String, Object> tempAttributeObjTable = null; //will be initialized and used if request == null
    private HashMap<String, String> tempAttributeStrTable = null; //will be initialized and used if request == null
    private User user = null; //will be used to store the user for which session is being created in case the request == null
    private boolean cookieSupported = false;
    public HttpServletRequest request;
    public ServletContext context;
    private boolean newSession = false;
    private boolean resetSession = false;
    private boolean anonymousUser = false;
    public boolean defaultSearchParamsDirty = false;
   
    public SessionManager(HttpServletRequest request, ServletContext context) throws SQLException{
    /**
       - checks if the current request is part of previous
    **/
        super();
        String sessionParameter;
        // get the current Base for the whole class
//        if (currentBase == null) {
//            currentBase = Long.toString(Misc.getNextId((Connection)request.getAttribute(Misc.DBCONN_LABEL), Sequence.MISC_SEQ));
//        }
        this.request = request;
        this.context = context;
        if (request == null) {
        	tempAttributeObjTable = new HashMap<String, Object>(100, 0.75f);
            tempAttributeStrTable = new HashMap<String, String>(100, 0.75f);
        }


        if ((sessionParameter = this.getSessionEncodingString()) == null) // need to create a new session;
            this.createNewSession(); //this will set up the time contacted
        else {
            this.loadSessionValues(sessionParameter);
            boolean ignoreLastTimeContacted = "1".equals((String)request.getAttribute("_ign_last_time"));
            long lastTimeContacted = Misc.getParamAsLong(this.getAttribute(SessionManager.LAST_TIME_CONTACTED_LABEL));
            if (!ignoreLastTimeContacted) {
            
	            if (Misc.isUndef(lastTimeContacted)) {
	                this.resetSession();
	            }
	            else {
	                if ((System.currentTimeMillis() - lastTimeContacted) > timeOutValue)
	                   this.resetSession(); // this will set up the time contacted
	                else
	                   this.setAttribute(SessionManager.LAST_TIME_CONTACTED_LABEL, Long.toString(System.currentTimeMillis()), true);
	            }
            }
            else {
            	this.setAttribute(SessionManager.LAST_TIME_CONTACTED_LABEL, Long.toString(lastTimeContacted), true);
            }
        }
        // cleanup
        removeAttribute("_errMsg");
        removeAttribute("_errCode");        
        
    }
    
    public boolean isSessionIdSame() {
    	User user = this.getUser();
    	return user.getSessionId() != null && user.getSessionId().equals(getParameter(SESSION_ID_LABEL));
    }
    public long getPortfolioId() {
    	
		long retval = Misc.getParamAsLong(getParameter("portfolio_id"));
		if (Misc.isUndef(retval))
  	      retval = Misc.getParamAsInt(getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		if (Misc.isUndef(retval))
			retval = Misc.getParamAsInt(getParameter("v123"));
		if (Misc.isUndef(retval))
			retval = Misc.getParamAsInt(getParameter("_cntxt_org"));
		if (Misc.G_MITTAL_DOING && Misc.isUndef(retval))
			retval = Misc.getParamAsLong(getParameter("v2"));
		if (Misc.G_MITTAL_DOING && Misc.isUndef(retval))
			retval = Misc.getParamAsLong(getParameter("v262"));
		if (Misc.G_MITTAL_DOING && Misc.isUndef(retval))
			retval = Misc.getParamAsLong(getParameter("v263"));
		if (Misc.G_MITTAL_DOING && Misc.isUndef(retval))
			retval = Misc.getParamAsLong(getParameter("v261"));
    
		return retval;
    }

    public long getProjectId() throws Exception {
       long retval = Misc.getParamAsLong(getParameter("project_id"));
       if (Misc.isUndef(retval)) {
          long alternativeId = Misc.getParamAsLong(getParameter("alternative_id"));
          long workspaceId = Misc.getParamAsLong(getParameter("workspaceId"));
          long orderId = Misc.getParamAsLong(getParameter("order_id"));
          String qs = null;
          long idToUse = orderId;
          if (!Misc.isUndef(orderId)) { //TODO look up project id from order id
             qs = Queries.GET_PRJ_IDETC_FROM_ORDER_ID;
             idToUse = orderId;
          }
          else if (!Misc.isUndef(alternativeId)) {
             qs = Queries.GET_PRJ_IDETC_FROM_ALTERNATIVE_ID;
             idToUse = alternativeId;
          }
          else if (!Misc.isUndef(workspaceId)) {
             qs = Queries.GET_PRJ_IDETC_FROM_WORKSPACE_ID;
             idToUse = workspaceId;
          }
          if (qs != null) {             
             try {
                Connection conn = this.getConnection();
                PreparedStatement ps = conn.prepareStatement(qs);
                ps.setLong(1, idToUse);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                   retval = Misc.getRsetInt(rs,1);
                   alternativeId = Misc.getRsetLong(rs,2);
                   workspaceId = Misc.getRsetLong(rs,3);
                }
                rs.close();
                ps.close();
             }
             catch (Exception e) {
                e.printStackTrace();
                throw e;
             }
          }
          
          if (!Misc.isUndef(retval)) {
              setAttribute("project_id", Long.toString(retval), false);              
          }
          if (!Misc.isUndef(alternativeId)) {
              setAttribute("alternative_id", Long.toString(alternativeId), false);
          }
          if (!Misc.isUndef(workspaceId)) {
              setAttribute("workspace_id", Long.toString(workspaceId), false);
          }
       }
       
       /* don't bother about this
       if (Misc.isUndef(retval)) { // look in session
          retval = Misc.getParamAsLong(this.getAttribute("project_id"));
       }
       */
      // return 1; //TODO temp debug
       return retval;
    }

    public long getWorkspaceId() throws Exception {
       long retval = Misc.getParamAsLong(getParameter("workspace_id"));

       if (Misc.isUndef(retval)) {
          long prjId = this.getProjectId();
          if  (!Misc.isUndef(prjId)) { //get the default workspace
             if (Misc.isUndef(retval)) { // look in session
                retval = Misc.getParamAsLong(this.getAttribute("workspace_id"));
                if (!Misc.isUndef(retval))
                   return retval;
             }

          //TODO - need to look for worksapces to which the current user has access to
             Connection dbConn = (Connection)request.getAttribute(Misc.DBCONN_LABEL);
             if (dbConn == null)
                return Misc.getUndefInt();
             try {
                PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_DEFAULT_PRJ_WORKSPACE);
                pStmt.setLong(1,prjId);
                ResultSet rset = pStmt.executeQuery();
                if (rset.next()) {//ignore all else;
                   retval = rset.getLong(1);
                   this.setAttribute("workspace_id",Long.toString(retval),false); //remember it for future use
                                            //only for the duration of this request
                }
                rset.close();
                pStmt.close();
             }
             catch (Exception e) {
               throw e;
               //e.printStackTrace();

             }
          }
       }

//       return 1; //TODO temp debug
       return retval;
    }

    public void unsetMenuTemplateId()  {
       this.removeAttribute("menu_template_id");
    }


    public Triple<Long, String, Integer> doLoginExt(String uname, String password, boolean forceOther) { //TODO
    	//returns null if uname does not exist or isinactive
    	//userId in int and server/port if uname exists and password matches - userId is the mapped userId in other system
    	//userId is null, if uname exits and is active but password does not match ... the user will be prompted to login again
    	
        Connection conn = request == null ? null : (Connection) request.getAttribute("_dbConnection") ;
        PreparedStatement pStmt = null;
        ResultSet rset = null;
        try {
            pStmt = conn.prepareStatement(Queries.GET_USER_PASSWORD_MATCH_EXT);
            pStmt.setString(1, password);
            pStmt.setString(2, uname);
            rset = pStmt.executeQuery();
            long userId = Misc.getUndefInt();
            String homeServer =  null;
            int homePort = Misc.getUndefInt();
            long homeUid = Misc.getUndefInt();
            boolean match = false;
            String userName = null;
            if (rset.next()) {
                userId = rset.getLong(1);
                userName = rset.getString(2);
                homeServer = rset.getString(3);
                homePort = Misc.getRsetInt(rset, 4);
                homeUid = Misc.getRsetLong(rset,5);
                match = 1 == rset.getInt(6);
            }
            rset.close();
            pStmt.close();
            
            if (match) {
            	Pair<String, Boolean> policyValidity = Misc.getValidityPassword((int) userId, conn);
            	if (policyValidity != null) {
            		if (policyValidity.second &&policyValidity.first != null ) {
            			this.setAttribute("_errMsg", policyValidity.first, false);
            		}
            		else if (!policyValidity.second) {
            			match = false;
            			this.setAttribute("_errMsg", policyValidity.first, false);
            			this.setAttribute("_reget_password","1", false);
            		}
            	}
            }
            if (match) {
            	//check if
            	match = !Misc.checkIfUserComingAfterLongTime(conn, (int) userId);
            	if (!match) {
            		this.setAttribute("_errMsg", "You are logging after a long time. Ask your administrator to reactivate your account", false);
            	}
            }
            String sessionId = null;
            if (match) {
            	if (forceOther)
            		SingleSession.forceLogout(conn, (int)userId, 0, null);
            	String ip = SingleSession.getRequestIP(request);
            	String cookieSessionId = this.getParameter(SESSION_ID_LABEL);
            	Triple<Boolean, String, String> singleResult = SingleSession.checkAndUpdateSingleSignonResult(conn, (int) userId, 0, ip, null, null, cookieSessionId);
            	if (singleResult != null && !singleResult.first) {
            		userId = Misc.getUndefInt();
                    homeServer =  null;
                    homePort = Misc.getUndefInt();
                    homeUid = Misc.getUndefInt();
                    match = false;
                    userName = null;
                    this.setAttribute("_singleSessionMsg", singleResult.second, false);
                    this.setAttribute("_errMsg", singleResult.second, false);
                    this.setAttribute(SESSION_ID_LABEL,"", true);
            	}
            	else {
            		sessionId = singleResult.third;
            		this.setAttribute(SESSION_ID_LABEL,singleResult.third, true);
            	}
            }
         	String currServer = request.getServerName();
        	int currPort = request.getServerPort();
        	if (Misc.isUndef(homePort))
        		homePort = currPort;
        	if (homeServer != null && (currPort == homePort) && (homeServer.equals("127.0.0.1") || homeServer.equals("localhost") || homeServer.equals(currServer))) {
        		homeServer = null;
        		homePort = Misc.getUndefInt();
        		homeUid = userId;
        	}
        	if (Misc.isUndef(homeUid))
        		homeUid = userId;
        	if (!match) {
        		homeUid = Misc.getUndefInt();
        	}
        	else {
        		Misc.resetAllowNextLogin(conn, (int)userId);
        	}
        	
            if (homeServer == null && Misc.isUndef(homeUid)) {
            //put additional processing here - todo
               return null;
            }
            else {
            //put additional processing here - todo
            	if (homeServer == null) {
            		this.setUser(userName, userId, true, sessionId);
            	}
            	return new Triple<Long, String, Integer> (homeUid, homeServer, homePort);               
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean doLogin(String uname, String password) { //TODO
        Connection conn = request == null ? null : (Connection) request.getAttribute("_dbConnection") ;
        PreparedStatement pStmt = null;
        ResultSet rset = null;
        try {
            pStmt = conn.prepareStatement(Queries.GET_USER_PASSWORD_MATCH);
            pStmt.setString(1, uname);
            pStmt.setString(2, password);
            rset = pStmt.executeQuery();
            long userId = Misc.getUndefInt();
            String userName = null;
            if (rset.next()) {
                userId = rset.getLong(1);
                userName = rset.getString(2);
            }
            rset.close();
            pStmt.close();

            if (Misc.isUndef(userId)) {
            //put additional processing here - todo
               return false;
            }
            else {
            //put additional processing here - todo
               this.setUser(userName, userId, true, null);
               return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean doLogin(String uname)
    {
            Connection conn = request == null ? null : (Connection)request.getAttribute("_dbConnection");
            PreparedStatement pStmt = null;
            ResultSet rset = null;
            try
            {
                    pStmt = conn.prepareStatement(Queries.GET_USER_MATCH);
                    pStmt.setString(1, uname);
                    rset = pStmt.executeQuery();
                    long userId = Misc.getUndefInt();
                    String userName = null;
                    if (rset.next())
                    {
                            userId = rset.getLong(1);
                            userName = rset.getString(2);
                    }
                    rset.close();
                    pStmt.close();

                    if (Misc.isUndef(userId))
                    {
                            //put additional processing here - todo
                            return false;
                    }
                    else
                    {
                            //put additional processing here - todo
                            this.setUser(userName, userId, true, null);
                            return true;
                    }
            }
            catch (Exception e)
            {
                    e.printStackTrace();
                    return false;
            }
    }

    public void doLogout() {
        this.resetSession();
    }

    public String getParameter(String name) {
    	if (name == null)
    		return null;
       String retval = getAttribute(name);
       if (retval == null && request != null) 
          return request.getParameter(name);
       else
          return retval;
    }
    public String getParameterRequestFirst(String name) {
    	String retval = null;
    	if (name == null)
    		return null;
    	if(request != null)
    		retval = request.getParameter(name);
    	if (retval == null) 
    		return getAttribute(name);
    	else
    		return retval;
    }

    public long getAlternativeId() throws Exception {
       long retval = Misc.getParamAsLong(getParameter("alternative_id"));
       if (!Misc.isUndef(retval))
          return retval;
       if (Misc.isUndef(retval)) {
          retval = Misc.getParamAsLong(this.getAttribute("alternative_id"));
          if (!Misc.isUndef(retval))
             return retval;
       }
       long prjId = getProjectId();
       if (Misc.isUndef(prjId))
          return Misc.getUndefInt();
       Connection dbConn = (Connection)request.getAttribute(Misc.DBCONN_LABEL);
       if (dbConn == null)
           return Misc.getUndefInt();
       try {
           PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_PRIM_ALT_FROM_ALTERNATIVES);
           pStmt.setLong(1,prjId);
           ResultSet rset = pStmt.executeQuery();
           if (rset.next()) {//ignore all else;
              retval = rset.getLong(1);
              this.setAttribute("alternative_id",Long.toString(retval),false); //remember it for future use
                                            //only for the duration of this request
           }
           rset.close();
           pStmt.close();
       }
       catch (Exception e) {
               e.printStackTrace();
               throw e;
               //e.printStackTrace();

       }



       /* - don't bother about that
       if (Misc.isUndef(retval)) { // look in session
          retval = Misc.getParamAsLong(this.getAttribute("alternative_id"));
       }
       */

       return retval;

//       return retval;
    }

    public boolean getWizardStatus() {
       int retval = Misc.getParamAsInt(getParameter("wizard_on"),0);
       if (retval == 0)
         retval = Misc.getParamAsInt(this.getAttribute("wizard_on"),0);
       return (retval != 0);
    }

    public long getPortWorkspaceId(long portRsetId) throws Exception {
       PreparedStatement pStmt = null;
       ResultSet rset = null;
       long retval = Misc.getUndefInt();
       try {
         if (!Misc.isUndef(portRsetId)) {
           Connection dbConn = (Connection)request.getAttribute(Misc.DBCONN_LABEL);
           pStmt = dbConn.prepareStatement(Queries.GET_PORT_WORKSPACE_FROM_RSET);
           pStmt.setLong(1, portRsetId);
           rset = pStmt.executeQuery();
           if (rset.next())
              retval = Misc.getRsetLong(rset,1);
           rset.close();
           pStmt.close();
        }
       }
       catch (Exception e) {
               throw e;
               //e.printStackTrace();

       }
       return retval;
    }
    
    public Cache getCache() {
    	try {
    		Cache _cache = context == null ? Cache.getCacheInstance() : (Cache) context.getAttribute("_cache");
    		return _cache;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    		return null;
    	}
    }
    public Connection getConnection() {
       Connection conn = request == null ? null : (Connection) request.getAttribute("_dbConnection");
       return conn;
    }
    
    public Logger getLogger() {
       Logger log = request == null ? null : (Logger) request.getAttribute("_log");
       return log;
    }
    
    public User getUser() {
       User user = request == null ? this.user : (User) request.getAttribute("_user");
       return user;
    }
    
    public UserGen.Menu getMenu() throws Exception {
       UserGen.Menu menu = (UserGen.Menu) this.getAttributeObj("_menu_gen");
       if (menu == null) {
          menu = UserGen.getMenu(getUser(), this);
          setAttributeObj("_menu_gen", menu);          
       }
       return menu;
    }
    public void removeMenu() throws Exception {
       removeAttribute("_menu_gen");
       removeAttribute("_prep_menu");       
       
    }
    
    public long getPortResultSetId() throws Exception { // if the port rset doesn't
       //exist for the portWorkspaceId, then will create it
       long retval = Misc.getParamAsLong(getParameter("port_resultset_id"));
       if (!Misc.isUndef(retval))
          return retval;
       int majVer = Misc.getParamAsInt(getParameter("major_version"));
       int minVer = Misc.getParamAsInt(getParameter("minor_version"));
       if (!Misc.isUndef(majVer) && !Misc.isUndef(minVer)) {
          User user = (User) request.getAttribute("_user");
          retval = getCache().getPortRsetId(getConnection(), majVer, minVer);
          if (!Misc.isUndef(retval))
              return retval;
       }

       boolean lookInDB = "1".equals(getParameter("read_from_db"));
       if (!lookInDB) {
          retval = Misc.getParamAsLong(this.getAttribute("port_resultset_id"));
          if (!Misc.isUndef(retval))
             return retval;
       }
       long portWorkspaceId = getPortWorkspaceId();
       PortHelper resultSetHelper = new PortHelper(request,context);
       //CapEx retval = resultSetHelper.getDefaultPortSet(portWorkspaceId);
       if (!Misc.isUndef(retval)) // remember it
          this.setAttribute("port_resultset_id", Long.toString(retval), false);
       return retval;

    }

     public long getPortWorkspaceId() throws Exception {
       //first look in request,
       //then checks if portRset (port_resultset_id and/or major/minor version given - if so then will get the portWorkspace
       //from that
       //the following was initial behaviour now skipped
            //then if request tells get default if passed in cookie, then the "default workspace as found in DB", then looks in cookie and
            //if not found then looks up the "default workspace" as found in DB as follows:
            //   if there is a current then the default
            //   if there are no currents then the latest proposed one created

       long retval = Misc.getParamAsLong(getParameter("port_workspace_id"));
       if (!Misc.isUndef(retval))
          return retval;

       long portRsetId = Misc.getParamAsLong(getParameter("port_resultset_id"));
       if (Misc.isUndef(portRsetId)) {
          int majVer = Misc.getParamAsInt(getParameter("major_version"));
          int minVer = Misc.getParamAsInt(getParameter("minor_version"));
          if (!Misc.isUndef(majVer) && !Misc.isUndef(minVer)) {
              User user = (User) request.getAttribute("_user");
              portRsetId = getCache().getPortRsetId(getConnection(), majVer, minVer);
          }
       }

       if (!Misc.isUndef(portRsetId)) {
           retval = getPortWorkspaceId(portRsetId);
       }
       return retval;

       //pre-revamp of port workspaces


//       if (!Misc.isUndef(retval))
//          return retval;

//       boolean lookInDB = "1".equals(getParameter("read_from_db"));
//       if (!lookInDB) {
//          retval = Misc.getParamAsLong(this.getAttribute("port_workspace_id"));
//          if (!Misc.isUndef(retval))
//             return retval;
//       }
       // look in the db
//       Connection dbConn = (Connection)request.getAttribute(Misc.DBCONN_LABEL);
//       PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_PORT_WORKSPACE);
//       ResultSet rset = pStmt.executeQuery();
//       if (rset.next())
//          retval = rset.getLong(2);
//       rset.close();
//       pStmt.close();
//       if (!Misc.isUndef(retval))
//          this.setAttribute("port_workspace_id", Long.toString(retval), false);
//       return retval;
    }

//    public void setAttribute(String attributeName, String attributeValue){ //leads to forgetting
//       setAttribute(attributeName,attributeValue,true);
//    }

    public void setAttribute(String attributeName, String attributeValue, boolean permanent){ //will not remember even temporarily if request is null
    /**
        Will add the name, value pair to be tracked across sessions in cookie
    **/
        if ("prj_basic_env".equals(attributeName) || "_page_unit".equals(attributeName)) {
           int dbg = 1;
        }
        if (attributeName == null)
           return;
        if (attributeName.equals("alternative_id") || attributeName.equals("workspace_id")) {
           int dbg=1;
        }
        removeAttribute(attributeName);
        if ((attributeValue == null) || attributeValue.length() == 0) {
            return;
        }

        if (permanent)
          valueTable.put(attributeName, new SessionValueType(attributeValue, false));
        else if (request != null)
          request.setAttribute(attributeName, attributeValue);
        else
          tempAttributeStrTable.put(attributeName, attributeValue);
    }

    public void setAttributeObj(String attribName, Object attributeValue) {//non-permanent//will not remember even temporarily if request is null
        if (attribName == null)
           return;
        if ((attributeValue == null) || attributeValue == null) {
            removeAttribute(attribName);
            return;
        }
        if (request != null)
           request.setAttribute(attribName, attributeValue);
        else
        	tempAttributeObjTable.put(attribName, attributeValue);

    }
    public Object getAttributeObj(String attribName) {//non-permanent
        return request == null ? tempAttributeObjTable.get(attribName) : request.getAttribute(attribName);
    }

    public void removeAttribute() {
       valueTable = null;
       valueTable = new HashMap<String, Object> (HASH_TABLE_SIZE,(float) 0.2);
    }

    public void removeAttribute(String attributeName) {
       valueTable.remove(attributeName);
       if (request != null) 
    	   request.removeAttribute(attributeName);
       else {
    	   tempAttributeStrTable.remove(attributeName);
    	   tempAttributeObjTable.remove(attributeName);
       }
    	   
       /*
       if (valueTable.get(attributeName) != null)
          valueTable.remove(attributeName);
       else
          request.removeAttribute(attributeName);
       */
    }

    public String getAttribute(String attributeName){
    /**
    **/
        if (attributeName == null) return null;
        String retval = (String) (request == null ? tempAttributeStrTable.get(attributeName) :  request.getAttribute(attributeName));
        if (retval != null)
           return retval;

        SessionValueType val = (SessionValueType) valueTable.get(attributeName);
        if (val != null)
           return val.s;
        return null;
     }
    public boolean isNew(){
    /**
    **/
        return(newSession);
    }
    public int sessionSupportApproach(){
    /**
    **/
        return (sessionTrackingMechanism);
    }
    public String getUserName() { //$$TODO
    /**
    **/
//    return ("tuser1");

        return(getAttribute(USER_NAME_LABEL));

    }
    public long getUserId()  {//$$TODO
    /**
    **/
//    return 1; //FOR DEBUG
    long uid = Misc.getParamAsLong(getAttribute(USER_ID_LABEL),0);
    return uid;
    //TODO - FOR TESTING COMMENTED OUT - IPSSI
    }
    
    public void setUser(User user){
    	this.user = user;
    }
    
    public void setUser(String user, long UID, boolean loggedIn, String sessionId) {
    /**se
    **/
    	 //_session.getAttribute("_singleSessionMsg") != null
        removeAttribute();
        setAttribute(USER_NAME_LABEL, user, true);
        setAttribute(USER_ID_LABEL, Long.toString(UID),true);
        setAttribute(LOGGED_IN_LABEL, loggedIn?LOGGED_IN:NOT_LOGGED_IN_YET,true);
        setAttribute(SessionManager.LAST_TIME_CONTACTED_LABEL, Long.toString(System.currentTimeMillis()),true);
        if (sessionId == null && loggedIn) {
        	sessionId = SingleSession.getCurrSessionId(this.getConnection(), (int)UID, 0);
        }
        if (sessionId != null)
        	setAttribute(SESSION_ID_LABEL, sessionId, true);
        if (request != null) {
        	updateLastLogin((int)UID);
        	try {
        	this.user = this.getCache().loadUserInfo((int)UID, user, request, context);
        	this.user.setSessionId(sessionId);
        	//this.setAttribute(SESSION_ID_LABEL, this.user.getSessionId(), true);
        	}
        	catch (Exception e3) {
        		e3.printStackTrace();
        		//eat it
        	}
        }
        if (request == null) {
        	Connection conn = null;
        	boolean destroyIt = false;
        	try {
        		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        		Cache cache = Cache.getCacheInstance(conn);
        		this.user = cache.loadUserInfo((int)UID, user, request, context);
        	}
        	catch (Exception e) {
        		e.printStackTrace();
        		destroyIt = true;
        		//eat it
        	}
        	finally {
        		try {
        			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
        		}
        		catch (Exception e2) {
        			e2.printStackTrace();
        			//eat it
        		}
        	}
        }
        
    }
    public void setUser(String user, int UID) {
    /**
       Similar to setUser(user, uid, loggedIn) except the user is logged in
    **/
        setUser(user,UID, true, null);
    }
    public void setUser(String user, int UID, boolean loggedIn) {
    	setUser(user,UID, loggedIn, null);
    }

    public boolean isCookieSupported(){
    /**
      Checks and returns true if the the current browser supports cookies.
      Takes a pessimistic approach that if it can't find cookie it will assume that URL encoding
      is the approach.
    **/
        return(sessionTrackingMechanism == COOKIE_TYPE );
    }
//    public String getSessionIdAsString() {
//       return getAttribute(CURRENT_BASE_LABEL)+getAttribute(CURRENT_SESSION_ID_LABEL);
//    }
    private final static  int age = 45*8*24*3600;
    
    private void helperWriteCookie(HttpServletResponse response, StringBuilder currCookieVal, int currCookieIndex, boolean isPerm) {         
        isPerm = false;
        Cookie cookie = new Cookie ("_ipc"+(isPerm ? "" : Integer.toString(currCookieIndex)), java.net.URLEncoder.encode(currCookieVal.toString()));     
        cookie.setPath("/");
        cookie.setMaxAge(isPerm ? age : -1);        
        response.addCookie(cookie);     
    }
    
    public boolean isPermTypeAttrib(String str) {
       //if ("pv123".equals(str) || "_page_unit".equals(str) || "_page_locale".equals(str))
       //  return true;
       //else
         return false;
    }
    public void getSessionAsQueryString(StringBuilder result, Map<String, String> ignThis) {
    	//will ignore param names that are in ignThis
        
        String key;
        Set<Map.Entry<String, Object>> entries = valueTable.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
           key = entry.getKey();
           SessionValueType val = (SessionValueType)(entry.getValue());
           if (val.s == null || val.s.length() == 0 || (ignThis != null && ignThis.containsKey(key)))
              continue;
           if (result.length() != 0)
              result.append("&");
           result.append(java.net.URLEncoder.encode(key)).append("=").append(java.net.URLEncoder.encode(val.s));           
        }
    }
    
    public void saveSessionValuesBack(HttpServletResponse response, boolean saveInDBValues) {//@#@#@#
/**
      saveInDBValues - If there are values that need to be saved in the database,
      true means that the values need to be stored back in the db.
      It will return the String that contains the appropriate set of values that needs to be
      then encoded in the URL or in Hidden Form or in cookie.
**/
/**
     Currently storing back in DB is not implemented - instead all values are stored in the cookie string
**/
        
        int currCookieIndex = 0;
        int currCookieSize = 0;
        int maxCookieSize = 480; //just be safe
        
        String key;
        Set<Map.Entry<String, Object>> entries = valueTable.entrySet();
        StringBuilder currCookieVal = new StringBuilder();
        StringBuilder realPermCookie = new StringBuilder();
        for (Map.Entry<String, Object> entry : entries) {
           key = entry.getKey();
           SessionValueType val = (SessionValueType)(entry.getValue());
           if (val.s == null || val.s.length() == 0)
              continue;
           //StringBuilder toUse = isPermTypeAttrib(key) ? realPermCookie : currCookieVal;
           
           if (currCookieVal.length() > maxCookieSize) {
              helperWriteCookie(response, currCookieVal, currCookieIndex, false);
              currCookieVal.setLength(0);
              currCookieIndex++;
           }

           if (currCookieVal.length() != 0)
              currCookieVal.append("&");
           currCookieVal.append(key).append("=").append(val.s);           
           if (isPermTypeAttrib(key)) {
               if (realPermCookie.length() != 0)
                  realPermCookie.append("&");
               realPermCookie.append(key).append("=").append(val.s);                      
           }
        }
        if (currCookieVal.length()>0) {
              helperWriteCookie(response, currCookieVal, currCookieIndex, false);
              currCookieVal.setLength(0);
              currCookieIndex++;
        }                  
        if (realPermCookie.length()>0) {
              helperWriteCookie(response, realPermCookie, currCookieIndex, true);
              realPermCookie.setLength(0);
              currCookieIndex++;
        }
        removeOldCookies(response,"_ipc",currCookieIndex - 1,realPermCookie.length()==0);
        removeOldCookies(response);
        
    }
    public void rememberSessionVars() {
    	this.rememberValueTable = valueTable == null ? null : (HashMap<String, Object>)(valueTable.clone());
    }
    public void setToRememberedVars() {
    	this.valueTable = this.rememberValueTable;
    }
    public String getSessionValuesEncodingString(boolean saveInDBValues){
    /**
      saveInDBValues - If there are values that need to be saved in the database,
      true means that the values need to be stored back in the db.
      It will return the String that contains the appropriate set of values that needs to be
      then encoded in the URL or in Hidden Form or in cookie.
    **/
    /**
     Currently storing back in DB is not implemented - instead all values are stored in the cookie string
    **/
        String retval = null;
        String key;
        
        Set<Map.Entry<String, Object>> entries = valueTable.entrySet();
        for (Map.Entry<String, Object> entry: entries) {
           key = (String) entry.getKey();
           SessionValueType val = (SessionValueType)(entry.getValue());
           if (val.s == null || val.s.length() == 0)
              continue;

           if (retval != null)
              retval += "&";
           else
              retval = "";
           retval = retval + key + "=" + (val).s;
        }
        if (retval == null)
           retval = "";
        return (java.net.URLEncoder.encode(retval));
    }

    public boolean getLoggedInStatus() {

    return true; //TODO DEBUG
//       String t = getAttribute(LOGGED_IN_LABEL);
  //     return(LOGGED_IN.equals(t));


    }

    public String getStyleSheet() {return "/common/tmcss.css";}
    public String getLogoSrc() { return "/uijsp/header.jsp?_t1=0";}
    public String getAdSrc() { return "http://www.yahoo.com";}
    public String getLogoImage() { return "/"+Misc.G_IMAGES_BASE+"transparent_sp.gif" ;}
    public String getAdImage() { return "/"+Misc.G_IMAGES_BASE+"icons2.gif";}
    private void loadValuesFromDatabase(){
    /*
    */
    }
    public void loadAdditionalSessionValuesFromNameValue(String s) { //string must be of form of a=v&b=v2&c=v3 etc
    	loadAdditionalSessionValuesFromNameValue(s, false);
    }
    
    public void loadAdditionalSessionValuesFromNameValue(String s, boolean isPerm) { //string must be of form of a=v&b=v2&c=v3 etc
        if (s == null) return;
        StringTokenizer strtok = new StringTokenizer(s,"&=",false);
//        System.out.println("Load session:"+s);
        while(strtok.hasMoreTokens()) {
           try {
              String firstToken = strtok.nextToken();
              if (strtok.hasMoreTokens()) {
                 String val = strtok.nextToken();
                 if (val == null || val.length() == 0)
                	 removeAttribute(firstToken);
                 else
                    setAttribute(firstToken, val, isPerm);                 
              }
           }
           catch (Exception e) {
           }
        }
    }
    private void loadSessionValues(String s){
    // loads the values of the quesryString s into a HashMap and also
    // loads the database
        String tempString;
        if (s == null) return;
        StringTokenizer strtok = new StringTokenizer(s,"&=",false);
//        System.out.println("Load session:"+s);
        while(strtok.hasMoreTokens()) {
           try {
              String firstToken = strtok.nextToken();
//              System.out.print("  cookie param:"+firstToken);
              if (strtok.hasMoreTokens()) {
                 String val = strtok.nextToken();
//                 System.out.print("  cookie val:"+val);
                 valueTable.put(firstToken, new SessionValueType(val,false));
              }
           }
           catch (Exception e) {
           // something wrong with the cookie - let us just blindly proceed but log it.
           Logger _log = (Logger) request.getAttribute("_log");
           if (_log != null)
              _log.log("Invalid Cookie", e);
           else // logger has not been yet set up - simply log
              context.log("Invalid Cookie", e);
           }
        }
//        System.out.println("Done loading values");
        if (((tempString = getAttribute(IN_DB_LABEL)) != null) && tempString.equals(INFO_STORED_IN_DB))
            loadValuesFromDatabase();
        this.validateAndSetGuestAccess();
    }
    public void removeOldCookies(HttpServletResponse response) {
      removeOldCookies(response, "_tpc",-1,true);
    }
    
    
    	public void removeOldCookies(HttpServletResponse response, String prefix, int afterIndex, boolean removeNoNum) {
		try {
			Cookie[] cookies = request.getCookies();
			int prefixLength = prefix.length();
			if (cookies != null) {
				StringBuilder tempRetval = new StringBuilder();
				for (int i = 0; i < cookies.length; i++) {
					Cookie cookie = cookies[i];
					if ((cookie.getName()).startsWith(prefix)) {
						if ((afterIndex < -1) || (removeNoNum && cookie.getName().equals(prefix))
								|| (Misc.getParamAsInt(cookie.getName().substring(prefixLength, prefixLength + 1)) > afterIndex)) {
//							cookie.setMaxAge(0);
							cookie.setPath("/");
							cookie.setValue("");
							response.addCookie(cookie);
						}
					}
				}
			}
		} catch (Exception e) {
			// eat it
		}
	}

    
    
//    public void removeOldCookies(HttpServletResponse response, String prefix,int afterIndex,boolean ) {
//      try {
//    	  if (request == null)
//    		  return ;
//         Cookie[] cookies = request.getCookies();
//         if (cookies != null) {
//            StringBuilder tempRetval = new StringBuilder();
//            for (int i=0; i<cookies.length; i++) {
//               Cookie cookie = cookies[i];
//               if ((cookie.getName()).startsWith(prefix)) {
//                  cookie.setMaxAge(0);
//                  response.addCookie(cookie);     
//               }
//            }            
//         }
//      }
//      catch (Exception e) {
//         //eat it
//      }
//    }
    private String getSessionEncodingString()  {
    //It returns, if any string that has been returned as part of hidden form or
    // URL or cookie - at least one of the values will be set.

         String retval = null;
         retval = getParameter("_cookie");
//         System.out.println("Session - cookie param:"+retval);
         if (retval == null || retval.length() == 0) {
             Cookie[] cookies = request == null ? null : request.getCookies();
             if (cookies != null) {
                StringBuilder tempRetval = new StringBuilder();
                
                Logger log = getLogger();
                boolean doLogging = log.getLoggingLevel() >= 15;
                StringBuilder cookieDbg = doLogging ? new StringBuilder() : null;
                for (int i=0; i<cookies.length; i++) {
                   Cookie cookie = cookies[i];
                   if ((cookie.getName()).startsWith(COOKIE_NAME_LABEL)) {
                       if (doLogging) {                          
                          cookieDbg.append("[CookieName:").append(cookie.getName()).append(": path:").append(cookie.getPath()).append(": val:").append(cookie.getValue());                          
                       }
                       String t1 = cookie.getValue();
                       if (t1 != null && t1.length() != 0) {
                          if (tempRetval.length() > 0) {
                             tempRetval.append("&");
                          }
                          tempRetval.append(t1);
                       }
                   }
                }
                if (doLogging)
                   log.log(cookieDbg, 15);
                retval = tempRetval.toString();
             }
         }
         try {
            if (retval != null && retval.length() == 0)
               retval = null;
            else {
               retval = java.net.URLDecoder.decode(retval);
            }
         }
         catch (Exception e) {
         }
         return retval;
    }
    private void createNewSession() throws SQLException {
    // Will create a new session with new IDs etc. and if their a user name/password passed in
    // the URL then that will be added to the value set for the session
        // set the user name value
/* //FOR IPSSI -
        String tempString;
        this.setAttribute(SessionManager.USER_NAME_LABEL, getParameter(Misc.USER_PARAMETER_LABEL));
        // set the UID parameter
        if ((tempString= getParameter(Misc.UID_PARAMETER_LABEL)) != null)
           this.setAttribute(SessionManager.USER_ID_LABEL, tempString);
        else {
           this.setAttribute(SessionManager.USER_ID_LABEL, Long.toString(this.getUserId()));
           anonymousUser = true;
        }
*/
        // set the session ID(s)
//        this.setAttribute(SessionManager.CURRENT_BASE_LABEL, (currentBase));
//        this.setAttribute(SessionManager.CURRENT_SESSION_ID_LABEL, Integer.toString(currentSessionID++));
        // set the time being visited to the current lime
        this.setAttribute(SessionManager.LAST_TIME_CONTACTED_LABEL, Long.toString(System.currentTimeMillis()),true);
        this.setAttribute(LOGGED_IN_LABEL, NOT_LOGGED_IN_YET,true);
        newSession = true;
    }


    public void resetSession() {
//        this.setAttribute(SessionManager.CURRENT_BASE_LABEL, (currentBase));
//        this.setAttribute(SessionManager.CURRENT_SESSION_ID_LABEL, Integer.toString(currentSessionID++));
        // set the time being visited to the current lime
        int uid = (int) this.getUserId();
        String sessionId = this.getAttribute(SESSION_ID_LABEL);
        this.removeAttribute(SESSION_ID_LABEL);
        
        if (uid > 0) {
        	SingleSession.forceLogout(this.getConnection(), (int)uid, 0, sessionId);
           Cache _cache = getCache();
           _cache.getUserMap().remove(uid);
           User u =(User) _cache.getUserMap().get(0);
           
           if (u != null && request == null)
              request.setAttribute("_user", _cache.getUserMap().get(0));
           
//           removeAttribute(USER_NAME_LABEL);
//          removeAttribute(USER_ID_LABEL);
           removeAttribute(); //remove all
          // this.user = null;
           
        }
        else {
        	removeAttribute();
        }
        resetSession = true;
        newSession = true;
    }


    static private void initNotUsed() {//time out initialized via new_conn.property
    // will do the initialization required for session manager including set up of
    // base number etc.

         // figure out how sessionTracking is to be done;
         Properties connProps = new Properties();
         try {
            connProps.load (new BufferedInputStream(new FileInputStream("d:\\trial2\\java\\session.property")));
         }
         catch (IOException e){ // don't do anything go with the default
         }
         sessionTrackingMechanism = Integer.parseInt(connProps.getProperty("Session.sessionTrackMechanism", Integer.toString(COOKIE_TYPE)));
         timeOutValue = (Long.parseLong(connProps.getProperty("Session.timeOutValue", Integer.toString(20)))) * 60*1000;
         connProps = null;
    }

	public MiscInner.SimpleValidationInfo getSimpleValidationInfoObj() {
		MiscInner.SimpleValidationInfo simpleValidationInfo = (MiscInner.SimpleValidationInfo)this.getAttributeObj(Misc.ATTR_SIMPLE_VALIDATION);
		if (simpleValidationInfo == null) {
			simpleValidationInfo = new MiscInner.SimpleValidationInfo();
			this.setAttributeObj(Misc.ATTR_SIMPLE_VALIDATION, simpleValidationInfo);
		}
		return simpleValidationInfo;
	}

	public void clearSimpleValidationInfoObj() {
		this.removeAttribute(Misc.ATTR_SIMPLE_VALIDATION);
	}
  
	public void setUserPref(Connection dbConn){
		setUserPref(dbConn,false);
	}
	
  //042908 begin ..
  public void setUserPref(Connection dbConn,boolean force) {
      User user = getUser();
      if (user != null) {
         HashMap pref = user.getUserPreference(dbConn, false);
         if (pref != null) {
             Set ds = pref.entrySet();
             Iterator iter = ds.iterator();      
             while (iter.hasNext()) {
                 Map.Entry entry = (Map.Entry)iter.next();
                 String name = (String) entry.getKey();
                 String value = (String) entry.getValue();
                 String oldval = getParameter(name);
                 if (force || oldval == null || oldval.length() == 0)
                     setAttribute(name, value, false);
             }
         }
      }
  }
  //.. end 042908

	public void updateLastLogin(int u_id) {
		//For Last Login
		setAttribute("_lastlogin", Misc.getUserLastLogin((Connection)request.getAttribute("_dbConnection"), u_id), false);
		// Call to update the last login in db
		Misc.recordUserLogin((Connection)request.getAttribute("_dbConnection"), u_id,(String) request.getRemoteAddr());
	}

    static public class CachedIdEtc {
        public int m_5047 = Misc.getUndefInt();
        public int m_5047Curr = Misc.getUndefInt();
        public int m_5037 = Misc.getUndefInt();
        public int m_5037Curr = Misc.getUndefInt();
        public int m_5048 = Misc.getUndefInt();
        public int m_5048Curr = Misc.getUndefInt();
        public int m_54 = Misc.getUndefInt();
        public int m_54Curr = Misc.getUndefInt();        
    }
    
    public CachedIdEtc m_cachedIdEtc = new CachedIdEtc();
    public void resetCachedIdEtc() {
        m_cachedIdEtc.m_5047 = Misc.getUndefInt();
        m_cachedIdEtc.m_5047Curr = Misc.getUndefInt();
        m_cachedIdEtc.m_5037 = Misc.getUndefInt();
        m_cachedIdEtc.m_5037Curr = Misc.getUndefInt();
        m_cachedIdEtc.m_5048 = Misc.getUndefInt();
        m_cachedIdEtc.m_5048Curr = Misc.getUndefInt();
        m_cachedIdEtc.m_54 = Misc.getUndefInt();
        m_cachedIdEtc.m_54Curr = Misc.getUndefInt();        
    }
    
}

final class  SessionValueType {
   public String s;
   public boolean storedInDB;
   public SessionValueType(String s,boolean storedInDB) {
      this.s = s;
      this.storedInDB = storedInDB;
   }
}




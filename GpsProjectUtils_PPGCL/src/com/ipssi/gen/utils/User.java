
// Copyright (c) 2000 IntelliPlanner Software Systems,  Inc.
package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import com.ipssi.SingleSession;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
/*

*/


public class User extends Object {
  private Cache m_cache = null;
  private FlexPriv userLevelPriv = null;
  public FlexPriv getFlexPriv() {
	  return userLevelPriv;
  }
  public Cache getCache() { return m_cache;}
  public void  setCache(Cache cache) { m_cache = cache;}
  public HashMap<String, HashMap<String, String>> pagePrefs = new HashMap<String, HashMap<String, String>>();
  private String sessionId = null;
  public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
  
  public  void resetInUse() {
   //for backward/forward compat
  }
  //// WILL NOT USE
  /*
  private boolean m_inUse = false;
  public synchronized boolean getAndSetIfPossibleInUse() {
     if (!m_inUse) {
        m_inUse = true;
        return false;
     }
     return true;
  }
  public synchronized void resetInUse() {
     m_inUse = true;     
  }
  */
  //// WILL NOT USE till here
  private int uid = 0;
  private boolean m_isSuperUser = false;
  private String m_uname = "";//rajeev 032205
  
  
  
  public int getUserId() { return uid;}
  public boolean isSuperUser() {
      return uid == 1 || m_isSuperUser;
  }
  public String getUserName() { return m_uname;}//rajeev 032205
  public void setUserName(String uname) { m_uname = uname;}//rajeev 032205

  public int hashcode() { return uid; }
  public boolean equals(User rhs) { return uid == rhs.getUserId(); }

  
  
  

  

  
  public User(long uid, Connection dbConnection) {
  }
  // Dev to support desktop app
  public User (int uid, Connection dbConnection, Cache cache) throws Exception {
	  this.m_cache = cache;
	     this.uid = uid;
	     Connection dbConn = dbConnection; //do not make this member or else change setUser ... because connection could change from request to request
	     
	     try {
	         PreparedStatement ps = dbConn.prepareStatement("select 1 from user_roles where user_1_id = ? and role_id = ?");
	         ps.setInt(1, uid);
	         ps.setInt(2, 1); //role id 1 is super user role
	         ResultSet rs = ps.executeQuery();
	         if (rs.next())
	            this.m_isSuperUser = true;
	         rs.close();
	         ps.close();
//	         SessionManager session = (SessionManager) request.getAttribute("_session");
	         loadUserPriv(dbConn); //do not do this on demand
	     }
	     
	     catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	     }
  }

  public User(HttpServletRequest request, ServletContext context, int uid) throws Exception {
     this.m_cache = context == null ? null : (Cache) context.getAttribute("_cache");
     this.uid = uid;
     Connection dbConn = (Connection) request.getAttribute("_dbConnection"); //do not make this member or else change setUser ... because connection could change from request to request
     
     try {
         PreparedStatement ps = dbConn.prepareStatement("select 1 from user_roles where user_1_id = ? and role_id = ?");
         ps.setInt(1, uid);
         ps.setInt(2, 1); //role id 1 is super user role
         ResultSet rs = ps.executeQuery();
         if (rs.next())
            this.m_isSuperUser = true;
         rs.close();
         ps.close();
         SessionManager session = (SessionManager) request.getAttribute("_session");
         loadUserPriv(session); //do not do this on demand
         String currSessionId = SingleSession.getCurrSessionId(dbConn,uid, 0);
         this.setSessionId(currSessionId);
     }
     
     catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
  }

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    User user = new User(1,null);
  }

   

  public void loadUserPriv(SessionManager session) throws Exception { //best to call as part of initial User Load .. MAY NOT BE NEEDED

     try {
//         m_privList = new HashMap();
    	 this.userLevelPriv = FlexPriv.loadFromDB(session.getConnection(), this.getUserId());
         boolean superUser = isSuperUser();
         if (superUser) {
         //   return; //rajeev 040808 ... load all priv ... as to check for explicit grant
         } //if super user

         m_privList.clear();         
         Connection dbConn = session.getConnection();
         PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_ALL_PRIV_FOR_USER);
         pStmt.setLong(1, getUserId());
         ResultSet rset = pStmt.executeQuery();
         int prevPrivId = Misc.getUndefInt();
         PrivInfo privObj = null;
         while (rset.next()) {
//         public static String GET_ALL_PRIV_FOR_USER = "select priv_id, prj_id, port_node_id, wspace_id, all_scope from user_roles, role_privs, user_roles_scope where user_roles.user_1_id = ? and role_privs.role_id = user_roles.role_id and user_roles_scope.user_role_id = user_roles.id"
            int privId = rset.getInt(1);
            int prjId = Misc.getRsetInt(rset,2);
            int portId = Misc.getRsetInt(rset,3);
            int wspaceId = Misc.getRsetInt(rset, 4);

            boolean allScope = Misc.getRsetInt(rset,5) == 1;
            if (prevPrivId != privId) {
                Integer privIdObj = new Integer(privId);
                privObj = (PrivInfo) m_privList.get(privIdObj);
                if (privObj == null) {
                    privObj = new PrivInfo();
                    m_privList.put(privIdObj, privObj);
                }
            }

            //the order has to be more specific to less specific - not sure if needed though
            if (allScope) {
                privObj.isGlobal = true;
            }
            else if (!Misc.isUndef(portId)) {
               privObj.m_portList.add(new Integer(portId));
            }
            else if (!Misc.isUndef(prjId)) {
               privObj.m_prjList.add(new Integer(prjId));
            }
            else if (!Misc.isUndef(wspaceId)) {
               privObj.m_workspaceList.add(new Integer(wspaceId));
            }
            privId = prevPrivId;
         }
         rset.close();
         rset= null;
         pStmt.close();
         pStmt = null;
         pStmt = dbConn.prepareStatement(Queries.GET_NEW_MENU_REPORT_INFO);
         rset = pStmt.executeQuery();
         while (rset.next()) {
			//report_definitions.id,for_port_node_id,for_user_id,user_preferences.value
			int reportId = rset.getInt(1);
			int portId = rset.getInt(2);
			int userId = rset.getInt(3);
			int userDefaultPort = Misc.getParamAsInt(rset.getString(4));
			int artificialPrivId = Misc.getUndefInt();
			if (userId != Misc.getUndefInt() && portId == Misc.getUndefInt()) {
				artificialPrivId = 1000000 + reportId;
				 Integer privIdObj1 = new Integer(artificialPrivId);
		         privObj = (PrivInfo) m_privList.get(privIdObj1);
		         if (privObj == null) {
		             privObj = new PrivInfo();
		             m_privList.put(privIdObj1, privObj);
		         }
		         privObj.m_portList.add(new Integer(userDefaultPort));		
			   }else if(userId == Misc.getUndefInt() && portId != Misc.getUndefInt()) {
				artificialPrivId = 3000000 + reportId;
				 Integer privIdObj1 = new Integer(artificialPrivId);
		         privObj = (PrivInfo) m_privList.get(privIdObj1);
		         if (privObj == null) {
		             privObj = new PrivInfo();
		             m_privList.put(privIdObj1, privObj);
		         }
		         privObj.m_portList.add(new Integer(portId));
			}
		}
        
         rset.close();
         pStmt.close();
         
         pStmt = dbConn.prepareStatement(Queries.GET_GEN_PRIV_AVAILABLE);
         pStmt.setInt(1, getUserId());
         rset = pStmt.executeQuery();
         prevPrivId = Misc.getUndefInt();
         privObj = null;
         while (rset.next()) {
             int privId = rset.getInt(1);
             int objType = rset.getInt(2);
             int objId = rset.getInt(3);
             if (prevPrivId != privId) {
                Integer privIdObj = new Integer(privId);
                privObj = (PrivInfo) m_privList.get(privIdObj);
                if (privObj == null) {
                    privObj = new PrivInfo();
                    m_privList.put(privIdObj, privObj);
                }
             }
             privObj.addGenObj(objType, objId);
             prevPrivId = privId;
         }
         rset.close();
         pStmt.close();
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
 // Dev to support desktop app 
  public void loadUserPriv(Connection conn) throws Exception { //best to call as part of initial User Load .. MAY NOT BE NEEDED

	     try {
//	         m_privList = new HashMap();
	         boolean superUser = isSuperUser();
	         if (superUser) {
	         //   return; //rajeev 040808 ... load all priv ... as to check for explicit grant
	         } //if super user

	         m_privList.clear();         
	         Connection dbConn = conn;
	         PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_ALL_PRIV_FOR_USER);
	         pStmt.setLong(1, getUserId());
	         ResultSet rset = pStmt.executeQuery();
	         int prevPrivId = Misc.getUndefInt();
	         PrivInfo privObj = null;
	         while (rset.next()) {
//	         public static String GET_ALL_PRIV_FOR_USER = "select priv_id, prj_id, port_node_id, wspace_id, all_scope from user_roles, role_privs, user_roles_scope where user_roles.user_1_id = ? and role_privs.role_id = user_roles.role_id and user_roles_scope.user_role_id = user_roles.id"
	            int privId = rset.getInt(1);
	            int prjId = Misc.getRsetInt(rset,2);
	            int portId = Misc.getRsetInt(rset,3);
	            int wspaceId = Misc.getRsetInt(rset, 4);

	            boolean allScope = Misc.getRsetInt(rset,5) == 1;
	            if (prevPrivId != privId) {
	                Integer privIdObj = new Integer(privId);
	                privObj = (PrivInfo) m_privList.get(privIdObj);
	                if (privObj == null) {
	                    privObj = new PrivInfo();
	                    m_privList.put(privIdObj, privObj);
	                }
	            }

	            //the order has to be more specific to less specific - not sure if needed though
	            if (allScope) {
	                privObj.isGlobal = true;
	            }
	            else if (!Misc.isUndef(portId)) {
	               privObj.m_portList.add(new Integer(portId));
	            }
	            else if (!Misc.isUndef(prjId)) {
	               privObj.m_prjList.add(new Integer(prjId));
	            }
	            else if (!Misc.isUndef(wspaceId)) {
	               privObj.m_workspaceList.add(new Integer(wspaceId));
	            }
	            privId = prevPrivId;
	         }
	         rset.close();
	         rset= null;
	         pStmt.close();
	         pStmt = null;
	         pStmt = dbConn.prepareStatement(Queries.GET_NEW_MENU_REPORT_INFO);
	         rset = pStmt.executeQuery();
	         while (rset.next()) {
				//report_definitions.id,for_port_node_id,for_user_id,user_preferences.value
				int reportId = rset.getInt(1);
				int portId = rset.getInt(2);
				int userId = rset.getInt(3);
				int userDefaultPort = Misc.getParamAsInt(rset.getString(4));
				int artificialPrivId = Misc.getUndefInt();
				if (userId != Misc.getUndefInt() && portId == Misc.getUndefInt()) {
					artificialPrivId = 1000000 + reportId;
					 Integer privIdObj1 = new Integer(artificialPrivId);
			         privObj = (PrivInfo) m_privList.get(privIdObj1);
			         if (privObj == null) {
			             privObj = new PrivInfo();
			             m_privList.put(privIdObj1, privObj);
			         }
			         privObj.m_portList.add(new Integer(userDefaultPort));		
				   }else if(userId == Misc.getUndefInt() && portId != Misc.getUndefInt()) {
					artificialPrivId = 3000000 + reportId;
					 Integer privIdObj1 = new Integer(artificialPrivId);
			         privObj = (PrivInfo) m_privList.get(privIdObj1);
			         if (privObj == null) {
			             privObj = new PrivInfo();
			             m_privList.put(privIdObj1, privObj);
			         }
			         privObj.m_portList.add(new Integer(portId));
				}
			}
	        
	         rset.close();
	         pStmt.close();
	         
	         pStmt = dbConn.prepareStatement(Queries.GET_GEN_PRIV_AVAILABLE);
	         pStmt.setInt(1, getUserId());
	         rset = pStmt.executeQuery();
	         prevPrivId = Misc.getUndefInt();
	         privObj = null;
	         while (rset.next()) {
	             int privId = rset.getInt(1);
	             int objType = rset.getInt(2);
	             int objId = rset.getInt(3);
	             if (prevPrivId != privId) {
	                Integer privIdObj = new Integer(privId);
	                privObj = (PrivInfo) m_privList.get(privIdObj);
	                if (privObj == null) {
	                    privObj = new PrivInfo();
	                    m_privList.put(privIdObj, privObj);
	                }
	             }
	             privObj.addGenObj(objType, objId);
	             prevPrivId = privId;
	         }
	         rset.close();
	         pStmt.close();
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }
  
  public ArrayList getPortNodeAtWhichPriv(int privId, SessionManager session) throws Exception {//if not granted then assumes grant at 1
     ArrayList retval = new ArrayList();
     if (isSuperUser())
        retval.add(new Integer(1));
     else {
        if (m_privList.isEmpty() && uid > 0) //uid > 0 added by rajeev 032205
          loadUserPriv(session);              
        PrivInfo privInfo = (PrivInfo) m_privList.get(new Integer(privId));        
        if (privInfo != null && !privInfo.isGlobal) {      
           Iterator iter = privInfo.m_portList.iterator();
           while (iter.hasNext()) {
               Integer portObj = (Integer)(iter.next());
               retval.add(portObj);    
           }//
        }//meaningful priv
     }//not super user
     if (retval.size() == 0)
        retval.add(new Integer(1));
     return retval;
  }
  
  int m_cachedProjectId = Misc.getUndefInt();
  int m_cachedPortNodeOfProjectId = Misc.getUndefInt();
  int m_cachedOrderId = Misc.getUndefInt();
  int m_cachedPortNodeOfOrderId = Misc.getUndefInt();
  
  public boolean isPrivAvailable(SessionManager session, int privId, int projectId, int workspaceId, int portfolioId, boolean checkFromMenuPointOfView, String dynamicMenuTag) throws Exception{
     return isPrivAvailable(session, privId, projectId, workspaceId, portfolioId, checkFromMenuPointOfView, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(),dynamicMenuTag);
  }
  //rajeev 040808 ....
  public boolean isPrivAvailable(SessionManager session, int privId, int projectId, int workspaceId, int portfolioId, boolean checkFromMenuPointOfView, int projectPortfolioId, int objType, int objId, int objPortfolioId, String dynamicMenuTag) throws Exception {
     return isPrivAvailable(session, privId, projectId, workspaceId, portfolioId, checkFromMenuPointOfView, projectPortfolioId, objType, objId, objPortfolioId, false, dynamicMenuTag); 
  }
  //... rajeev 040808
  public boolean isPrivAvailable(SessionManager session, int privId, int projectId, int workspaceId, int portfolioId, boolean checkFromMenuPointOfView,int projectPortfolioId, int objType, int objId, int objPortfolioId, boolean checkExplicitGrant,String dynamicMenuTag) throws Exception{//only implemented for Misc.G_FOR_ORDER
  //how ... for the load priv for project, load priv for portfolio, (anything available to this or to portfolio), load things that are
  //granted globally
   
      //added by balwant for artifical menuTag and privilege check............
    /* int reportId = Misc.getUndefInt();
     int orgId = Misc.getUndefInt();
     if (!Misc.isUndef(privId)) {
 	  if(privId > 1000000 && privId < 3000000) {
          	    reportId = privId - 1000000;
 	}else if (privId > 3000000) {
 		reportId = privId - 3000000;
 		orgId = m_cache.getOrgIdBasedOnReportId(reportId);
 		if (checkForPrivsOf == null) {
 		   return m_cache.isAncestor(session.getConnection(), Misc.getParamAsInt((String)m_userPreference.get("pv123")), orgId);
		}
 	}
    }*/
	  if (!checkExplicitGrant && isSuperUser()) //040808
	        return true;
	  
     if (dynamicMenuTag != null) {
    	  
     Triple<Integer, Integer, Integer> val = m_cache.getPrivListAvForMenuTagArtificial(dynamicMenuTag);
      if (val != null) {
	   int privsId  = val.first.intValue();  // need to verify how can we use ...otherwise remove it use Pair<T1,T2>
      
		   if (val.second.intValue() != Misc.getUndefInt() && val.third.intValue() == Misc.getUndefInt()) {
			   if (isSuperUser()) {
				return true;
			}else{
		      return val.second.intValue() == getUserId();
			}
		}else if (val.second.intValue() == Misc.getUndefInt() && val.third.intValue() != Misc.getUndefInt()) {
		   //return isPrivelegeForPort(session.getConnection(),getUserId(),val.third.intValue(),true);
		   return m_cache.isAncestor(session.getConnection(), Misc.getParamAsInt((String)m_userPreference.get("pv123")), val.third.intValue());
		}
	}
   }
      //..........end

	  
	  
      if (m_privList.isEmpty() && uid > 0) //uid > 0 added by rajeev 032205
         loadUserPriv(session);
      
      
      //else need to check for the particular projectId/workspaceId
      PrivInfo privInfo = (PrivInfo) m_privList.get(new Integer(privId));
      if (privInfo == null)
         return false;
      if (privInfo.isGlobal)
         return true;
      if (objType == Misc.G_FOR_PROJECT && !Misc.isUndef(objId)) {
         objType = Misc.getUndefInt();
         projectId = objId;
         projectPortfolioId = objPortfolioId;
      }
      
      if (objType == Misc.G_FOR_ORDER && !Misc.isUndef(objId)) {
         //check first in portNodeIdForProject       
         Connection dbConn = session.getConnection();
         
         if (!Misc.isUndef(objPortfolioId)) {
            m_cachedOrderId = objId;
            m_cachedPortNodeOfOrderId = objPortfolioId;
         }
         else if (m_cachedOrderId != objId || Misc.isUndef(m_cachedPortNodeOfOrderId)) {
         
            PreparedStatement ps = dbConn.prepareStatement(Queries.GET_PORT_NODE_FOR_ORDER);
            ps.setInt(1, objId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                m_cachedPortNodeOfOrderId = rs.getInt(1);                
            }
            rs.close();
            ps.close();
            m_cachedOrderId = objId;            
         }
         int portNodeIdForOrder = m_cachedPortNodeOfOrderId;
         
         
         Iterator iter = privInfo.m_portList.iterator();
         while (iter.hasNext()) {
             Integer portObj = (Integer)(iter.next());
             int tempPort = portObj.intValue();
             if (m_cache.isAncestorOrg(dbConn, portNodeIdForOrder, tempPort))
                return true;
         }
         //1st check if available at prj level //042508
         if (privInfo.isGenPrivAv(Misc.G_FOR_PROJECT, projectId)) //0425
            return true;//0425
         return privInfo.isGenPrivAv(objType, objId);
      }
      if (!Misc.isUndef(projectId)) {
         //check first in portNodeIdForProject       
         Connection dbConn = session.getConnection();
         if (!Misc.isUndef(projectPortfolioId)) {
            m_cachedProjectId = projectId;
            m_cachedPortNodeOfProjectId = projectPortfolioId;
         }
         else if (m_cachedProjectId != projectId || Misc.isUndef(m_cachedPortNodeOfProjectId)) {
         
            PreparedStatement ps = dbConn.prepareStatement(Queries.GET_PORT_NODE_FOR_PRJ);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                m_cachedPortNodeOfProjectId = rs.getInt(1);                
            }
            rs.close();
            ps.close();
            m_cachedProjectId = projectId;            
         }
         int portNodeIdForProject = m_cachedPortNodeOfProjectId;
         
         Iterator iter = privInfo.m_portList.iterator();
         while (iter.hasNext()) {
             Integer portObj = (Integer)(iter.next());
             int tempPort = portObj.intValue();
             if (m_cache.isAncestorOrg(dbConn, portNodeIdForProject, tempPort))
                return true;
         }
         boolean fromNewApproach = privInfo.isGenPrivAv(Misc.G_FOR_PROJECT, projectId);
         if (fromNewApproach)
            return true;
         iter = privInfo.m_prjList.iterator();
         while (iter.hasNext()) {
           Integer portObj = (Integer)(iter.next());
           if (portObj.intValue() == projectId)
             return true;
         }
         return false;
      }
      if (!Misc.isUndef(portfolioId)) 
      {
        if (checkFromMenuPointOfView) 
        {
            if (!privInfo.m_portList.isEmpty()) 
            {
                return true;
            }
        }
        
         Iterator iter = privInfo.m_portList.iterator();
         Connection dbConn = session.getConnection();         
         while (iter.hasNext()) {
             Integer portObj = (Integer)(iter.next());
             int tempPort = portObj.intValue();
             if (m_cache.isAncestorOrg(dbConn, portfolioId, tempPort))
                return true;
         }
         //not to worry ... check if there is a pri for some project
         if (!checkFromMenuPointOfView)  
            return false;
         //check if the 
      }
      //at this point both project/portfolio are undef
      //if
      //we are looking at things from menu implicitly 
      if (!privInfo.m_prjList.isEmpty() || !privInfo.m_portList.isEmpty() || privInfo.hasSomeObjInGen())
         return true;
      return false;
  }
  // // Dev to support desktop app : replaced session with db connection
  public boolean isPrivAvailable(Connection conn, int privId, int projectId, int workspaceId, int portfolioId, boolean checkFromMenuPointOfView,int projectPortfolioId, int objType, int objId, int objPortfolioId, boolean checkExplicitGrant,String dynamicMenuTag) throws Exception{//only implemented for Misc.G_FOR_ORDER
	  //how ... for the load priv for project, load priv for portfolio, (anything available to this or to portfolio), load things that are
	  //granted globally
	   
	      //added by balwant for artifical menuTag and privilege check............
	    /* int reportId = Misc.getUndefInt();
	     int orgId = Misc.getUndefInt();
	     if (!Misc.isUndef(privId)) {
	 	  if(privId > 1000000 && privId < 3000000) {
	          	    reportId = privId - 1000000;
	 	}else if (privId > 3000000) {
	 		reportId = privId - 3000000;
	 		orgId = m_cache.getOrgIdBasedOnReportId(reportId);
	 		if (checkForPrivsOf == null) {
	 		   return m_cache.isAncestor(session.getConnection(), Misc.getParamAsInt((String)m_userPreference.get("pv123")), orgId);
			}
	 	}
	    }*/
		  if (!checkExplicitGrant && isSuperUser()) //040808
		        return true;
		  
	     if (dynamicMenuTag != null) {
	    	  
	     Triple<Integer, Integer, Integer> val = m_cache.getPrivListAvForMenuTagArtificial(dynamicMenuTag);
	      if (val != null) {
		   int privsId  = val.first.intValue();  // need to verify how can we use ...otherwise remove it use Pair<T1,T2>
	      
			   if (val.second.intValue() != Misc.getUndefInt() && val.third.intValue() == Misc.getUndefInt()) {
				   if (isSuperUser()) {
					return true;
				}else{
			      return val.second.intValue() == getUserId();
				}
			}else if (val.second.intValue() == Misc.getUndefInt() && val.third.intValue() != Misc.getUndefInt()) {
			   //return isPrivelegeForPort(session.getConnection(),getUserId(),val.third.intValue(),true);
			   return m_cache.isAncestor(conn, Misc.getParamAsInt((String)m_userPreference.get("pv123")), val.third.intValue());
			}
		}
	   }
	      //..........end

		  
		  
	      if (m_privList.isEmpty() && uid > 0) //uid > 0 added by rajeev 032205
	         loadUserPriv(conn);
	      
	      
	      //else need to check for the particular projectId/workspaceId
	      PrivInfo privInfo = (PrivInfo) m_privList.get(new Integer(privId));
	      if (privInfo == null)
	         return false;
	      if (privInfo.isGlobal)
	         return true;
	      if (objType == Misc.G_FOR_PROJECT && !Misc.isUndef(objId)) {
	         objType = Misc.getUndefInt();
	         projectId = objId;
	         projectPortfolioId = objPortfolioId;
	      }
	      
	      if (objType == Misc.G_FOR_ORDER && !Misc.isUndef(objId)) {
	         //check first in portNodeIdForProject       
	         Connection dbConn = conn;
	         
	         if (!Misc.isUndef(objPortfolioId)) {
	            m_cachedOrderId = objId;
	            m_cachedPortNodeOfOrderId = objPortfolioId;
	         }
	         else if (m_cachedOrderId != objId || Misc.isUndef(m_cachedPortNodeOfOrderId)) {
	         
	            PreparedStatement ps = dbConn.prepareStatement(Queries.GET_PORT_NODE_FOR_ORDER);
	            ps.setInt(1, objId);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                m_cachedPortNodeOfOrderId = rs.getInt(1);                
	            }
	            rs.close();
	            ps.close();
	            m_cachedOrderId = objId;            
	         }
	         int portNodeIdForOrder = m_cachedPortNodeOfOrderId;
	         
	         
	         Iterator iter = privInfo.m_portList.iterator();
	         while (iter.hasNext()) {
	             Integer portObj = (Integer)(iter.next());
	             int tempPort = portObj.intValue();
	             if (m_cache.isAncestorOrg(dbConn, portNodeIdForOrder, tempPort))
	                return true;
	         }
	         //1st check if available at prj level //042508
	         if (privInfo.isGenPrivAv(Misc.G_FOR_PROJECT, projectId)) //0425
	            return true;//0425
	         return privInfo.isGenPrivAv(objType, objId);
	      }
	      if (!Misc.isUndef(projectId)) {
	         //check first in portNodeIdForProject       
	         Connection dbConn = conn;
	         if (!Misc.isUndef(projectPortfolioId)) {
	            m_cachedProjectId = projectId;
	            m_cachedPortNodeOfProjectId = projectPortfolioId;
	         }
	         else if (m_cachedProjectId != projectId || Misc.isUndef(m_cachedPortNodeOfProjectId)) {
	         
	            PreparedStatement ps = dbConn.prepareStatement(Queries.GET_PORT_NODE_FOR_PRJ);
	            ps.setInt(1, projectId);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                m_cachedPortNodeOfProjectId = rs.getInt(1);                
	            }
	            rs.close();
	            ps.close();
	            m_cachedProjectId = projectId;            
	         }
	         int portNodeIdForProject = m_cachedPortNodeOfProjectId;
	         
	         Iterator iter = privInfo.m_portList.iterator();
	         while (iter.hasNext()) {
	             Integer portObj = (Integer)(iter.next());
	             int tempPort = portObj.intValue();
	             if (m_cache.isAncestorOrg(dbConn, portNodeIdForProject, tempPort))
	                return true;
	         }
	         boolean fromNewApproach = privInfo.isGenPrivAv(Misc.G_FOR_PROJECT, projectId);
	         if (fromNewApproach)
	            return true;
	         iter = privInfo.m_prjList.iterator();
	         while (iter.hasNext()) {
	           Integer portObj = (Integer)(iter.next());
	           if (portObj.intValue() == projectId)
	             return true;
	         }
	         return false;
	      }
	      if (!Misc.isUndef(portfolioId)) 
	      {
	        if (checkFromMenuPointOfView) 
	        {
	            if (!privInfo.m_portList.isEmpty()) 
	            {
	                return true;
	            }
	        }
	        
	         Iterator iter = privInfo.m_portList.iterator();
	         Connection dbConn = conn;         
	         while (iter.hasNext()) {
	             Integer portObj = (Integer)(iter.next());
	             int tempPort = portObj.intValue();
	             if (m_cache.isAncestorOrg(dbConn, portfolioId, tempPort))
	                return true;
	         }
	         //not to worry ... check if there is a pri for some project
	         if (!checkFromMenuPointOfView)  
	            return false;
	         //check if the 
	      }
	      //at this point both project/portfolio are undef
	      //if
	      //we are looking at things from menu implicitly 
	      if (!privInfo.m_prjList.isEmpty() || !privInfo.m_portList.isEmpty() || privInfo.hasSomeObjInGen())
	         return true;
	      return false;
	  }
  
  public boolean isPrivAvailable(SessionManager session, int privId, int objType, int objId) throws Exception { //checks for the current portfolio/project/workspace
     int portId = (int) session.getPortfolioId();
     int wspaceId = (int) session.getWorkspaceId();
     int prjId  = (int) session.getProjectId();
     return isPrivAvailable(session, privId, prjId, wspaceId, portId, true, Misc.getUndefInt(), objType, objId, Misc.getUndefInt(), null);
  }
  public boolean isPrivAvailable(SessionManager session, int privId) throws Exception { //checks for the current portfolio/project/workspace

     int portId = (int) session.getPortfolioId();
     int wspaceId = (int) session.getWorkspaceId();
     int prjId  = (int) session.getProjectId();
     return isPrivAvailable(session, privId, prjId, wspaceId, portId, true, null);
  }
  public boolean isPrivAvailable(SessionManager session, String tag) throws Exception 
  {      
     int portId = (int) session.getPortfolioId();
     int wspaceId = (int) session.getWorkspaceId();
     int prjId  = (int) session.getProjectId();
     return isPrivAvailable(session, tag, true, prjId, wspaceId, portId, Misc.getUndefInt(), Misc.getUndefInt());
      
      
  }
  public boolean isPrivAvailable(SessionManager session, String tag, boolean checkRead, int projectId, int workspaceId, int portfolioId, int objType, int objId) throws Exception {
     //if read/write doesn't make sense then check for read ...
      if (getUserId() == 1)
         return true;
      PrivInfo.TagInfo tagInfo = m_cache.getPrivId(tag);
      if (tagInfo == null)
         return true;
      boolean retval = isPrivAvailable(session, checkRead ? tagInfo.m_read : tagInfo.m_write, projectId, workspaceId, portfolioId,true, Misc.getUndefInt(), objType, objId, Misc.getUndefInt(), null);
      if (checkRead && !retval)
         retval = isPrivAvailable(session, tagInfo.m_write, projectId, workspaceId, portfolioId,true,Misc.getUndefInt(), objType, objId, Misc.getUndefInt(), null);
      return retval;
  }
  
  private HashMap m_privList = new HashMap(1000,0.75f);//null; // not to be used new HashMap(); //Keyed by UserId & contains Info in PrivInfo = null;
  
  public HashMap getPrivList(){
	  return m_privList;
  }
  private static Integer g_dummyPrivLookup = new Integer(1);
  private void helperReadPrivFromRS(PreparedStatement ps, HashMap result) throws Exception {
     try {
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
             int privId = Misc.getRsetInt(rs,1);
             if (!Misc.isUndef(privId)) {
                Integer privInt = new Integer(privId);
                result.put(privInt, g_dummyPrivLookup); //dont want to put in null here 
                
             }
         }
         rs.close();
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  private void loadPrivForProject(Connection dbConn, int projectId, HashMap result) throws Exception { //union of priv at project, portfolio and global
     try {
         
         PreparedStatement ps = dbConn.prepareStatement(Queries.PRIVS_AVAILABLE_AT_PROJECT_LEVEL);
         ps.setInt(1, uid);
         ps.setInt(2, projectId);
         ps.setInt(3, uid);
         ps.setInt(4, projectId);
         ps.setInt(5, uid);
         helperReadPrivFromRS(ps, result);
         ps.close();
         
     }
     catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
  }
  private void loadPrivForPortfolio(Connection dbConn, int portfolioId, HashMap result) throws Exception { //union of priv at portfolio, global
     
     try {         
        // Connection dbConn = (Connection) request.getAttribute("_dbConnection");
         ArrayList ancestorPath = m_cache.getAncestorPath(dbConn, portfolioId);
         StringBuilder queryStr = new StringBuilder();
         queryStr.append(Queries.PRIVS_AVAILABLE_AT_PORTFOLIO_LEVEL_1);
         if (ancestorPath == null || ancestorPath.size() == 0) {
            queryStr.append("1");
         }
         else {
            Misc.convertInListToStr(ancestorPath, queryStr);
         }
         queryStr.append(Queries.PRIVS_AVAILABLE_AT_PORTFOLIO_LEVEL_2);
         
         PreparedStatement ps = dbConn.prepareStatement(queryStr.toString());
         ps.setInt(1, uid);
         ps.setInt(2, uid);         
         helperReadPrivFromRS(ps, result);
         ps.close();
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  
  private void loadPrivForGlobal(Connection dbConn, HashMap result) throws Exception {
     try {
         PreparedStatement ps = dbConn.prepareStatement(Queries.PRIVS_AVAILABLE_AT_GLOBAL_LEVEL);
         ps.setInt(1, uid);                
         helperReadPrivFromRS(ps, result);
         ps.close();
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  
  

  public boolean isPrivAvailable(SessionManager session, int privId, int portfolioId) throws Exception {
      //return isPrivAvailableOld(privId, portfolioId);
      return isPrivAvailable(session, privId, Misc.getUndefInt(), Misc.getUndefInt(), portfolioId, true, null);
      
    
  }

  //In PrepareMenuXML, if the project menu is expanded then first set helpInitMenuXMLForPrj()
  // and then set helpSetPrjSpecificMenuState
  //then elements that have "c" or "m" as _ism attribute are to be printed - those that do not have those attribute are not to be printed
  
  
  
  
  
// CAPEX_REMOVE public String getSectionName(int sectionId) { //move this out
//     if (sectionId == Misc.SECTION_PJ_BASIC)
//         return "Project Summary";
//     else if (sectionId == Misc.SECTION_ALT_DATE) 
//         return "Milestones";
//     else if (sectionId == Misc.SECTION_ALT_WORK)
//         return "Detailed Dates";
//     return null;
//  }
  public static void loadParamsFromMenuSpec(SessionManager session, String pgContext) throws Exception {
     UserGen.loadParamsFromMenuSpec(session, pgContext);
  }

  
    //Some Helper Classes to Help in DashBoarding


  

 
  
  private Element helperGetUserSpecificPort(SessionManager session, MiscInner.PortInfo masterElem, Document resultDoc, ArrayList forPrivs, int singlePriv, DimInfo forDim, boolean topPrivAvailable, int descOrg, boolean topIsSelectable, boolean getLowerLevelIfTopSelectable, boolean doOnlyMatchingSelectable, boolean includeExtended, boolean topAncTillFound, boolean doSpecialForUnassigned) throws Exception {
     if (masterElem == null)
        return null;
     Element retval = null;
     boolean isUnassigned =    masterElem.m_id == 0;

     boolean typeMatch = false;


      int mot = masterElem.m_orgType;
      
      typeMatch = forDim == null || forDim.inRefOrg(mot);
      topAncTillFound = typeMatch || topAncTillFound;
      if (!topAncTillFound) {
         topAncTillFound = forDim.inRefAncTill(mot); //forDim will not be null if this code needs to be exec
      }
      boolean descSpecLevelReached = forDim == null || forDim.inRefDescTill(mot);
      
      if (!topPrivAvailable) {
         int portId = masterElem.m_id;
         if (!Misc.isUndef(portId)) {            
            if (forPrivs != null && forPrivs.size() > 0) {
                for (int t1=0,t1s=forPrivs.size();t1<t1s;t1++) {
                   if (isPrivAvailable(session, ((Integer)forPrivs.get(t1)).intValue(), Misc.getUndefInt(), Misc.getUndefInt(), portId,false, null)) {
                     topPrivAvailable = true;
                     break;
                   }
                }
            }
            else if (!Misc.isUndef(singlePriv)) {
                if (isPrivAvailable(session, singlePriv, Misc.getUndefInt(), Misc.getUndefInt(), portId,false, null))
                   topPrivAvailable = true;
            }
            else {
                topPrivAvailable = true;
            }
         }
      }

     boolean thisNodeMatches = false;
     boolean thisNodeSelectable = false;
     //Kind of messy - if doOnlyMatchingSelectable then obvious - only matching nodes with priv are selectable
     //otherwise the code below makes all desc of top level avaialble as selectable. later on we set all priv node
     //to be selectable (makes it easier from UI point of view, where we will still set the initial default)
     if (topIsSelectable)
        thisNodeSelectable = topIsSelectable && topAncTillFound;
     if (!Misc.isUndef(descOrg)) {
         if (descOrg == masterElem.m_id) {
            thisNodeMatches = true;
            thisNodeSelectable = true;
            topAncTillFound = true; //override whatever is spec at dimension level
         }
     }
     else {
         thisNodeMatches = typeMatch && topPrivAvailable;
         if (thisNodeMatches)
            thisNodeSelectable = true;
     }
     if (doOnlyMatchingSelectable)
        thisNodeSelectable = thisNodeMatches;
     else
        thisNodeSelectable = topPrivAvailable; //see comments earlier for why
     if (getLowerLevelIfTopSelectable && thisNodeSelectable)
        topAncTillFound = true;
     thisNodeSelectable = thisNodeSelectable && topAncTillFound;
     
     ArrayList chList = masterElem.m_children;
     if (!descSpecLevelReached) {
       if (!isUnassigned || !doSpecialForUnassigned) {
           for (int n1=0,n1s=chList == null ? 0 : chList.size();n1 <n1s;n1++) {        
              MiscInner.PortInfo e = (MiscInner.PortInfo)chList.get(n1);          
              boolean isChUnassigned = e.m_id == 0;
              boolean toContinue = false;
              if (e.m_isExtended && !includeExtended) {
                 toContinue = true;
              }
              if (!isChUnassigned && toContinue)
                 continue;
              
              Element chRetval = helperGetUserSpecificPort(session, e, resultDoc, forPrivs, singlePriv, forDim, topPrivAvailable, descOrg, thisNodeSelectable,getLowerLevelIfTopSelectable, doOnlyMatchingSelectable, includeExtended, topAncTillFound, isChUnassigned && toContinue);
              
              if (chRetval != null) {        
                 if (retval == null) {
                    retval = resultDoc.createElement("n");
                 }
                 retval.appendChild(chRetval);
              }
           }
       }
     }
     if (getLowerLevelIfTopSelectable && thisNodeSelectable)
        thisNodeMatches = true;
     if (!thisNodeMatches && isUnassigned) {
        thisNodeMatches = true;
     }
     if (thisNodeMatches) {
        if (retval == null) {
            retval = resultDoc.createElement("n");
        }
     }
     if (retval != null) {
        retval.setAttribute("i", Integer.toString(masterElem.m_id));
        retval.setAttribute("n", masterElem.m_name);
        retval.setAttribute("ec", masterElem.m_extRef);
        retval.setAttribute("ot", Integer.toString(masterElem.m_orgType));
        retval.setAttribute("rc", Integer.toString(masterElem.m_repCurrency));
        retval.setAttribute("di", Integer.toString(masterElem.m_budCurrency));
        if (thisNodeSelectable)
           retval.setAttribute("_s", "1");
        else
           retval.setAttribute("_s","0");
        
        retval.setAttribute("v249", Integer.toString(masterElem.m_countryCode));
        if (masterElem.m_strField2 != null && masterElem.m_strField2.length() != 0)        
            retval.setAttribute("v5100", masterElem.m_strField2);
        else
            retval.removeAttribute("v5100");
        
        if (!Misc.isUndef(masterElem.m_thresh))
            retval.setAttribute("v5111", Double.toString(masterElem.m_thresh));
        if (!Misc.isUndef(masterElem.m_consolidationStatus))
            retval.setAttribute("v259", Integer.toString(masterElem.m_consolidationStatus));
     }
     
     return retval;
  }

  

  public Document getUserSpecificPort(SessionManager session, MiscInner.PortInfo rootElement, ArrayList forPrivs, int singlePriv, DimInfo forDim, int descOrg, boolean getLowerLevel, boolean doOnlyMatchingSelectable, boolean includeExtended) throws Exception {
  
     MyXMLHelper myXMLHelper = new MyXMLHelper(null,null);
     Document retval = myXMLHelper.create();
     
     
     Element oElem = retval.createElement("o");
     retval.appendChild(oElem);
     Element vElem = retval.createElement("v");
     oElem.appendChild(vElem);     
     Element topElem = helperGetUserSpecificPort(session, rootElement, retval, forPrivs, singlePriv, forDim, false, descOrg, false, getLowerLevel, doOnlyMatchingSelectable, includeExtended, false, false);
     if (topElem != null)
        vElem.appendChild(topElem);
     return retval;
  }
  
  
  public int getUserSpecificDefaultPort(SessionManager session, int currPortId, int priv, DimInfo forDim) throws Exception {
     //check if there is a match going down and for which priv is available 
  
     Connection dbConn = session.getConnection();
     MiscInner.PortInfo me = m_cache.getPortNodeExt(dbConn, currPortId);
     if (me == null)
        me = m_cache.getPortNodeExt(dbConn,Misc.G_TOP_LEVEL_PORT);
     int retval = getUserSpecificDefaultPortDown(session, me, null, priv, forDim, false, Misc.getUndefInt(),false);
     if (Misc.isUndef(retval)) {//check going up
         for (MiscInner.PortInfo e=me.m_parent; e != null; e = e.m_parent) {
             
             int ot = e.m_orgType;
             int portId = e.m_id;
             if ((forDim == null || forDim.inRefOrg(ot)) && isPrivAvailable(session, priv, Misc.getUndefInt(), Misc.getUndefInt(), portId, false, null)) {
                 retval = portId;
                 break;
             }
         }    
     }
     if (Misc.isUndef(retval)) {
       me = m_cache.getPortNodeExt(dbConn,Misc.G_TOP_LEVEL_PORT);
       retval = getUserSpecificDefaultPortDown(session, me, null, priv, forDim, false, Misc.getUndefInt(),false);
       if (Misc.isUndef(retval))
          retval = 1;
     }
     return retval;     
  }
  
  public int getUserSpecificDefaultPortDown (SessionManager session, MiscInner.PortInfo masterElem,  ArrayList forPrivs, int singlePriv, DimInfo forDim, boolean topPrivAvailable, int descOrg, boolean topTypeMatch) throws Exception {//hack forDim set to null for LocTracker
     if (masterElem == null)
        return Misc.getUndefInt();
     forDim = null;//hack set to null for LocTracker

     boolean typeMatch = false;
     int retval = Misc.getUndefInt();

      int mot = masterElem.m_orgType;
      typeMatch = forDim == null || forDim.inRefOrg(mot) || topTypeMatch;
      if (!topPrivAvailable) {
         int portId = masterElem.m_id;
         if (!Misc.isUndef(portId)) {
            if (forPrivs != null && forPrivs.size() > 0) {
                for (int t1=0,t1s=forPrivs.size();t1<t1s;t1++) {
                   if (isPrivAvailable(session, ((Integer)forPrivs.get(t1)).intValue(), Misc.getUndefInt(), Misc.getUndefInt(), portId,false, null)) {
                     topPrivAvailable = true;
                     break;
                   }
                }
            }
            else if (!Misc.isUndef(singlePriv)) {
                if (isPrivAvailable(session, singlePriv, Misc.getUndefInt(), Misc.getUndefInt(), portId,false, null)) {
                   topPrivAvailable = true;
                }
            }
            else {
                topPrivAvailable = true;
            }
         }
      }

     boolean thisNodeMatches = false;

     if (!Misc.isUndef(descOrg)) {
         if (descOrg == masterElem.m_id)
            thisNodeMatches = true;
     }
     else {
         thisNodeMatches = typeMatch && topPrivAvailable;
     }

     if (thisNodeMatches)
       return masterElem.m_id;
     ArrayList chList = masterElem.m_children;
     for (int n1=0,n1s=chList == null ? 0 : chList.size();n1<n1s;n1++) {
        MiscInner.PortInfo e = (MiscInner.PortInfo)chList.get(n1);
        if (e.m_isExtended)
           continue;
        int chRetval = getUserSpecificDefaultPortDown(session, e, forPrivs, singlePriv, forDim, topPrivAvailable, descOrg, typeMatch);
        if (!Misc.isUndef(chRetval))
           return chRetval;
     }
     return Misc.getUndefInt();
  }
  public int getPrivToCheckForOrg(SessionManager session, String pgContext) throws Exception {     
     return getPrivToCheckForOrg(session, pgContext, null);
  }
//CAPEX_REMOVE_REPLACE  public int getPrivToCheckForOrg(SessionManager session, String pgContext, FrontPageInfo frontPage) throws Exception {
  public int getPrivToCheckForOrg(SessionManager session, String pgContext, String[] frontPagePrivTagToCheck) throws Exception {
     int orgPrivCheck = Misc.getParamAsInt(session.getAttribute("org_priv_check"));
     if (Misc.isUndef(orgPrivCheck)) {      
          String paramTag = (session.getParameter("org_priv_check_tag"));
          Cache cache = session.getCache();
          if (paramTag != null && paramTag.length() != 0) {
             PrivInfo.TagInfo tag = cache.getPrivId(paramTag);
             if (tag != null)
                orgPrivCheck = tag.m_read;
          }
     }
     //CAPEX_REMOVE_REPLACE if (Misc.isUndef(orgPrivCheck) && frontPage != null && frontPage.m_privTagToCheck != null) {
     if (Misc.isUndef(orgPrivCheck) && frontPagePrivTagToCheck != null) {
        for (int i=0,is=frontPagePrivTagToCheck.length;i<is;i++) {
          PrivInfo.TagInfo tag = m_cache.getPrivId(frontPagePrivTagToCheck[i]);
          if (tag != null) {
             orgPrivCheck = tag.m_read;          
             break;
          }
        }
     }
     if (Misc.isUndef(orgPrivCheck)) {
        String mainPageContext = UserGen.helpGetAndSetMainMenuLevelContext(session, pgContext);
        if (mainPageContext != null && mainPageContext.length() != 0) {
          PrivInfo.TagInfo tag = m_cache.getPrivId(mainPageContext);
          orgPrivCheck = (tag != null) ? tag.m_read : Misc.getUndefInt();          
        }
     }
     
     if (Misc.isUndef(orgPrivCheck)) {
        String mainPageContext = pgContext;
        if (mainPageContext != null && mainPageContext.length() != 0) {
          PrivInfo.TagInfo tag = m_cache.getPrivId(mainPageContext);
          orgPrivCheck = (tag != null) ? tag.m_read : Misc.getUndefInt();          
        }
     }
     
     if (Misc.isUndef(orgPrivCheck)) {
        PrivInfo.TagInfo tag = m_cache.getPrivId("tr_usercontrol_org");
        orgPrivCheck = (tag != null) ? tag.m_read : Misc.getUndefInt();          
     }
     return orgPrivCheck;
  }
//042908 ..begin
  private HashMap m_userPreference = null; 
  public HashMap getUserPreference(Connection dbConn, boolean reload) {
      try {
          if (m_userPreference != null && !reload)
             return m_userPreference;             
          synchronized (this) {
              HashMap retval = loadUserPreference(dbConn, this.getUserId());
              m_userPreference = retval;
          }
          return m_userPreference;
      }
      catch (Exception e) {
          e.printStackTrace();
          //dont throw ..
          return null;
      }
  }
  synchronized public void setUserPrefField(String name, String val) {
	  m_userPreference.put(name, val);
  }
  
  public static HashMap loadUserPreference(Connection dbConn, long uid) {
      HashMap retval = new HashMap(13,0.75f);
      try {              
          PreparedStatement ps = dbConn.prepareStatement("select user_1_id, name, value from user_preferences where user_1_id = ? ");
          ps.setLong(1, uid);
          ResultSet rs = ps.executeQuery();
          while (rs.next()) { 
              String name = rs.getString(2);
              String val = rs.getString(3);
              retval.put(name, val);
          }
          rs.close();
          ps.close();
          return retval;
      }
      catch (Exception e) {
         e.printStackTrace();
         //dont throw
         return null;
      }
  }
    
  public  boolean isPrivelegeForPort(Connection dbConn, int userId, int portNodeId,boolean checkFromMenuPointOfView) throws SQLException{
	return  isPrivelegeForPort(  dbConn,  userId,  portNodeId, checkFromMenuPointOfView,false);
  }
  public  boolean isPrivelegeForPort(Connection dbConn, int userId, int portNodeId,boolean checkFromMenuPointOfView,boolean checkExplicitGrant) throws SQLException{
	  if (!checkExplicitGrant && isSuperUser()) //040808
	        return true;
	     ArrayList list = new ArrayList();
	  int portNode = Misc.getUndefInt();
	  PreparedStatement ps = null;
	  ResultSet rset = null;
	  try{
		   ps = dbConn.prepareStatement(Queries.GET_ALL_DESENDENT_PORT_NODE);
		   ps.setInt(1, portNodeId);
	       rset = ps.executeQuery();
	         while (rset.next()) {
	        	 portNode = rset.getInt(1);
	        	 list.add(portNode);
	         }	  
	         rset.close();
	         rset = null;
	         ps.close();
	         ps = null;
	  }catch (Exception e) {
		e.printStackTrace();
	}finally{
		if (ps != null) {
			ps.close();
		}
		if (rset != null) {
			rset.close();
		}
	}
	if (list.size() > 0) {
     Integer defaultPort = Misc.getParamAsInt((String)m_userPreference.get("pv123"));
     return list.contains(defaultPort);
	}
	  return false;
  }
//.. end 042908
// *********************** OLD CODES *************************************

}





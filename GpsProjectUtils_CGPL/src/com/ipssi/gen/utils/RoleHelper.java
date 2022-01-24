package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.xpath.*;

import com.ipssi.SingleSession;

public class RoleHelper {
    Connection dbConn;
    Cache cache;

    public  Document getAllRoleUserList(String portRoleLabel, String prjRoleLabel) throws Exception {
       return getAllRoleUserList(portRoleLabel, prjRoleLabel, false, false, 1, null);
    }

    public  Document getAllRoleUserList(String portRoleLabel, String prjRoleLabel, boolean doRoleOnly, boolean notDoAll, int portfolioId, SessionManager session) throws Exception {
        boolean getAllRoles = true;
        User userObj = null;
        if (session != null) {
           userObj = session.getUser();
           if (userObj.isPrivAvailable(session, "grant_all_role"))
              getAllRoles = true;
           else
              getAllRoles = false;           
              
        }
        Document retval = MyXMLHelper.create();
        Element rootElem = retval.createElement("data");
        retval.appendChild(rootElem);
        Element allScopeElem = null;
        Element portScopeElem = null;
        Element prjScopeElem = null;
        Element userElem = null;
        if (!notDoAll) {
           allScopeElem = retval.createElement("all_scope");
           rootElem.appendChild(allScopeElem);
        }
        if (portRoleLabel != null) {
           portScopeElem = retval.createElement(portRoleLabel); //port_scope
           rootElem.appendChild(portScopeElem);
        }
        if (prjRoleLabel != null) {
           prjScopeElem = retval.createElement(prjRoleLabel); //prj_scope
           rootElem.appendChild(prjScopeElem);
        }
        if (!doRoleOnly) {
           userElem = retval.createElement("users");
           rootElem.appendChild(userElem);
        }

        try {
           PreparedStatement pStmt = null;
           if (getAllRoles) 
              pStmt = dbConn.prepareStatement(Queries.GET_ALL_ROLES);
           else { //will happen only if session != null && userObj != null
              pStmt = dbConn.prepareStatement(Queries.GET_ROLE_LIMITED);
              pStmt.setInt(1, userObj.getUserId());
              
              int projectId = (int) session.getProjectId();
              Misc.setParamInt(pStmt, portfolioId, 2);
              Misc.setParamInt(pStmt, portfolioId, 3);
              Misc.setParamInt(pStmt, projectId, 4);
              Misc.setParamInt(pStmt, projectId, 5);
              
           }
           ResultSet rset = pStmt.executeQuery();
           while (rset.next()) {
               long id = rset.getLong(1);
               String name = rset.getString(2);
               String desc = rset.getString(3);
               long scope = rset.getInt(4);
               Element toAddTo = (scope == 3) ? prjScopeElem : (scope == 2) ? portScopeElem : allScopeElem;
               if (toAddTo == null)
                  continue;
               Element role = retval.createElement("role");
               toAddTo.appendChild(role);
               role.setAttribute("n", name);
               role.setAttribute("id", Long.toString(id));
               role.setAttribute("desc", desc);
           }
           rset.close();
           pStmt.close();
           if (!doRoleOnly) {
              pStmt = dbConn.prepareStatement(Queries.GET_ALL_USERS);
              rset = pStmt.executeQuery();
              while (rset.next()) {
                long id = rset.getLong(1);
                String name = rset.getString(2);
                String email = rset.getString(3);
                Element user = retval.createElement("user");
                userElem.appendChild(user);
                user.setAttribute("n", name);
                user.setAttribute("id", Long.toString(id));
                user.setAttribute("email", email);
              }
              rset.close();
              pStmt.close();
           }
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
        return retval;
    }

    public Document getUserAllRoleInfo(long userId) throws Exception {
        Document retval = MyXMLHelper.create();
        Element rootElem = retval.createElement("data");
        retval.appendChild(rootElem);
        Element allScopeElem = retval.createElement("all_scope");
        rootElem.appendChild(allScopeElem);
        Element portScopeElem = retval.createElement("port_scope");
        rootElem.appendChild(portScopeElem);
        Element prjScopeElem = retval.createElement("prj_scope");
        rootElem.appendChild(prjScopeElem);

        try {
            addUserDetail(userId, rootElem);
            addUserAllRoles(userId, allScopeElem, retval);

            PreparedStatement pStmt = null;
            ResultSet rset = null;
            long prevRole = Misc.getUndefInt();
            Element currRoleElem = null;
            if (false) { //TODO - for Track this cant be done
	            pStmt = dbConn.prepareStatement(Queries.GET_USER_ROLES_PRJ);
	            pStmt.setLong(1,userId);
	            rset = pStmt.executeQuery();
	            
	            while (rset.next()) {
	                long roleId = rset.getLong(1);
	                if (roleId != prevRole) {
	                    currRoleElem = retval.createElement("role");
	                    currRoleElem.setAttribute("rid", Long.toString(roleId));
	                    prjScopeElem.appendChild(currRoleElem);
	                }
	                prevRole = roleId;
	                Element item = retval.createElement("item");
	                currRoleElem.appendChild(item);
	                item.setAttribute("id", Long.toString(rset.getLong(2)));
	                item.setAttribute("n", rset.getString(3));
	            }
	            rset.close();
	            pStmt.close();
            }

            pStmt = dbConn.prepareStatement(Queries.GET_USER_ROLES_PORT);
            pStmt.setLong(1,userId);
            rset = pStmt.executeQuery();
            prevRole = Misc.getUndefInt();
            currRoleElem = null;
            while (rset.next()) {
                long roleId = rset.getLong(1);
                if (roleId != prevRole) {
                    currRoleElem = retval.createElement("role");
                    currRoleElem.setAttribute("rid", Long.toString(roleId));
                    portScopeElem.appendChild(currRoleElem);
                }
                prevRole = roleId;
                Element item = retval.createElement("item");
                currRoleElem.appendChild(item);
                item.setAttribute("id", Long.toString(rset.getLong(2)));
                item.setAttribute("n", cache.getFullPortName(dbConn, (int)rset.getLong(2), null));
            }
            rset.close();
            pStmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return retval;
    }

    public  Document getPrjRoleUserList(long projectId, long workspaceId) throws Exception {
        Document retval = this.getAllRoleUserList("port_scope", "item_scope");
        Element itemUserElem = retval.createElement("item_users");
        Element rootElem = retval.getDocumentElement();
        rootElem.appendChild(itemUserElem);
        try {
           PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_USER_LIST_FOR_PRJ);
           pStmt.setLong(1,projectId);
           pStmt.setLong(2,projectId);
           pStmt.setLong(3,workspaceId);
           ResultSet rset = pStmt.executeQuery();
           while (rset.next()) {
               long id = rset.getLong(1);
               String name = rset.getString(2);
               String email = rset.getString(3);

               Element user = retval.createElement("user");
               itemUserElem.appendChild(user);
               user.setAttribute("n", name);
               user.setAttribute("id", Long.toString(id));
               user.setAttribute("email", email);
           }
           rset.close();
           pStmt.close();
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
        return retval;
    }

    public Document getUserPrjInfo(long userId, long projectId, long workspaceId) throws Exception {
        Document retval = MyXMLHelper.create();
        Element rootElem = retval.createElement("data");
        retval.appendChild(rootElem);
        Element allScopeElem = retval.createElement("all_scope");
        rootElem.appendChild(allScopeElem);
        Element portScopeElem = retval.createElement("port_scope");
        rootElem.appendChild(portScopeElem);
        Element prjScopeElem = retval.createElement("item_scope");
        rootElem.appendChild(prjScopeElem);

        try {
            addUserDetail(userId, rootElem);

            addUserAllRoles(userId, allScopeElem,retval);

            PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_USER_ROLES_PRJ_SEL);
            pStmt.setLong(1,userId);
            pStmt.setLong(2,projectId);
            pStmt.setLong(3,workspaceId);
            ResultSet rset = pStmt.executeQuery();
            long prevRole = Misc.getUndefInt();
            Element currRoleElem = null;
            while (rset.next()) {
                long roleId = rset.getLong(1);
                if (roleId != prevRole) { // by mistake there might role for a workspace and for a project - obviously project level takes precedent
                    currRoleElem = retval.createElement("role");
                    currRoleElem.setAttribute("rid", Long.toString(roleId));
                    prjScopeElem.appendChild(currRoleElem);
                    prevRole = roleId;
                    currRoleElem.setAttribute("wkspid", Misc.isUndef(Misc.getRsetLong(rset,2))?"0":"1");
              }
            }
            rset.close();
            pStmt.close();

            pStmt = dbConn.prepareStatement(Queries.GET_USER_ROLES_PORT_SEL_FOR_PRJ);
            pStmt.setLong(1,userId);
            pStmt.setLong(2,projectId);
            //pStmt.setLong(3,workspaceId);
            rset = pStmt.executeQuery();
            prevRole = Misc.getUndefInt();
            currRoleElem = null;
            while (rset.next()) {
                long roleId = rset.getLong(1);
                if (roleId != prevRole) {
                    currRoleElem = retval.createElement("role");
                    currRoleElem.setAttribute("rid", Long.toString(roleId));
                    portScopeElem.appendChild(currRoleElem);
                }
                prevRole = roleId;
                Element item = retval.createElement("item");
                currRoleElem.appendChild(item);
                item.setAttribute("id", Long.toString(rset.getLong(2)));
                item.setAttribute("n", cache.getFullPortName(dbConn, (int)rset.getLong(2), null));
            }
            rset.close();
            pStmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return retval;
    }

    public  Document getPortRoleUserList(long portNodeId) throws Exception {
        Document retval = this.getAllRoleUserList("item_scope", "prj_scope");
        Element itemUserElem = retval.createElement("item_users");
        Element rootElem = retval.getDocumentElement();
        rootElem.appendChild(itemUserElem);
        try {
           PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_USER_LIST_FOR_PORT);
           pStmt.setLong(1,portNodeId);
           ResultSet rset = pStmt.executeQuery();
           while (rset.next()) {
               long id = rset.getLong(1);
               String name = rset.getString(2);
               String email = rset.getString(3);

               Element user = retval.createElement("user");
               itemUserElem.appendChild(user);
               user.setAttribute("n", name);
               user.setAttribute("id", Long.toString(id));
               user.setAttribute("email", email);
           }
           rset.close();
           pStmt.close();
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
        return retval;
    }

    public Document getUserPortInfo(long userId, long portNodeId) throws Exception {
        Document retval = MyXMLHelper.create();
        Element rootElem = retval.createElement("data");
        retval.appendChild(rootElem);
        Element allScopeElem = retval.createElement("all_scope");
        rootElem.appendChild(allScopeElem);
        Element portScopeElem = retval.createElement("item_scope");
        rootElem.appendChild(portScopeElem);
        Element prjScopeElem = retval.createElement("prj_scope"); //not needed
        rootElem.appendChild(prjScopeElem); //not needed

        try {
            addUserDetail(userId, rootElem);
            addUserAllRoles(userId, allScopeElem, retval);

            PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_USER_ROLES_PORT_SEL_FOR_PORT);
            pStmt.setLong(1,userId);
            pStmt.setLong(2,portNodeId);
            ResultSet rset = pStmt.executeQuery();
            long prevRole = Misc.getUndefInt();
            Element currRoleElem = null;
            while (rset.next()) {
                long roleId = rset.getLong(1);
//                if (roleId != prevRole) {
                    currRoleElem = retval.createElement("role");
                    currRoleElem.setAttribute("rid", Long.toString(roleId));
                    portScopeElem.appendChild(currRoleElem);
//                }
//                prevRole = roleId;
//                Element item = retval.createElement("item");
//                currRoleElem.appendChild(item);
//                item.setAttribute("id", Long.toString(rset.getLong(2)));
            }
            rset.close();
            pStmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return retval;
    }

    public Document getAllPriveleges() throws Exception {
        FileInputStream inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"privelege.xml");
            //DEBUG
            //FileWriter fout = new FileWriter("c:\\test\\cache.xml");
            //PrintWriter outw = new PrintWriter(fout, true);
        MyXMLHelper test = new MyXMLHelper(inp, null);
        Document retval = test.load();
        Element roleElem = retval.createElement("roles");
        retval.getDocumentElement().appendChild(roleElem);
        try {
           PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_ALL_ROLES);
           ResultSet rset = pStmt.executeQuery();
           while (rset.next()) {
               long id = rset.getLong(1);
               String name = rset.getString(2);
               String desc = rset.getString(3);
               long scope = rset.getInt(4);
               Element role = retval.createElement("role");
               roleElem.appendChild(role);
               role.setAttribute("n", name);
               role.setAttribute("id", Long.toString(id));
               role.setAttribute("desc", desc);
           }
           rset.close();
           pStmt.close();

        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
        return retval;
    }

    public Document getRoleDetail(long roleId) throws Exception  {
        Document retval = MyXMLHelper.create();
        Element rootElem = retval.createElement("data");
        retval.appendChild(rootElem);
        Element priveleges = retval.createElement("priveleges");
        rootElem.appendChild(priveleges);
        try {
            PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_ROLE_DETAIL);
            pStmt.setLong(1, roleId);
            ResultSet rset = pStmt.executeQuery();
            while (rset.next()) {
                String name = rset.getString(2);
                String desc = rset.getString(3);
                int scope   = Misc.getRsetInt(rset,4,1);
                String extCode = rset.getString(5);
                rootElem.setAttribute("name", name);
                rootElem.setAttribute("desc", desc);
                rootElem.setAttribute("scope", Integer.toString(scope));
                if (extCode != null)
                   rootElem.setAttribute("ext_code", extCode);
            }
            rset.close();
            pStmt.close();

            pStmt = dbConn.prepareStatement(Queries.GET_PRIV_LIST_FOR_ROLE);
            pStmt.setLong(1, roleId);
            rset = pStmt.executeQuery();
            while (rset.next()) {
                long id = rset.getLong(1);
                Element priv = retval.createElement("priv");
                priv.setAttribute("id", Long.toString(id));
                priveleges.appendChild(priv);
            }
            rset.close();
            pStmt.close();


        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return retval;
    }

    public void deleteUser(long userId, int byUserId) throws Exception {
        try {
            if (Misc.isUndef(userId))
               return;
            PreparedStatement cStmt = dbConn.prepareStatement(Queries.DELETE_USER);
            cStmt.setLong(1,userId);
            cStmt.execute();
            cStmt.close();
            cStmt = dbConn.prepareStatement(Queries.DELETE_ROLE_FOR_USER);
            cStmt.setLong(1, userId);
            cStmt.execute();
            cStmt.close();
			recordUserAdminActivity(dbConn, (int) userId, "Deleted User", byUserId);

            cache.invalidateUserMap((int)userId);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public long updateUser(Document userInfoXML, int byUserId) throws Exception {
        //if failed will return undef
        //otherwise will return the userId
        //if new will create
        try {
            Element rootElem = userInfoXML.getDocumentElement();
            long userId = Misc.getParamAsLong(rootElem.getAttribute("uid"));
            String name = rootElem.getAttribute("name");
            String username = rootElem.getAttribute("lname");
            String password = rootElem.getAttribute("password");
            String email = rootElem.getAttribute("email");
            String phone = rootElem.getAttribute("phone");
            boolean newUser = Misc.isUndef(userId);
            if (Misc.isUndef(userId)) {
				//Check if the username exists
				boolean userPresent = false;
				PreparedStatement pStmt = dbConn.prepareStatement(Queries.CHECK_USER_NAME);
				pStmt.setString(1, username);
				ResultSet rSet = pStmt.executeQuery();
				while ( rSet.next() )
				{
					userPresent = true;
					userId = rSet.getInt(1);
				}
				rSet.close();
				pStmt.close();

				if (!userPresent) {
					//create user
					userId = Misc.getNextId(dbConn, Sequence.USERS); //sqlchanged
					PreparedStatement cStmt = Misc.G_DO_ORACLE ? dbConn.prepareStatement(Queries.CREATE_USER) : dbConn.prepareStatement(Queries.CREATE_USER, Statement.RETURN_GENERATED_KEYS);
          int paramIndex=1;
          if (Misc.G_DO_ORACLE)
              cStmt.setLong(paramIndex++, userId);
					cStmt.setString(paramIndex++, name);
					cStmt.setString(paramIndex++, password);
					cStmt.setString(paramIndex++, username);
					cStmt.setString(paramIndex++, email);
					cStmt.setString(paramIndex++, phone);
					userId = Misc.executeGetId(cStmt, userId, Queries.CREATE_USER);
					cStmt.close();
				}
				else {
					if (! Misc.isUndef(userId)) {
						// update the user info
						/*PreparedStatement cStmt = dbConn.prepareStatement(Queries.UPDATE_USER);
						cStmt.setString(1, name);
						cStmt.setString(2, password);
						cStmt.setString(3, email);
						cStmt.setString(4, phone);
						cStmt.setLong(5, userId);
						cStmt.execute();
						cStmt.close();*/
						return Misc.getUndefInt();
					}
				}
            }
            else {
				PreparedStatement cStmt = dbConn.prepareStatement(Queries.UPDATE_USER);
				cStmt.setString(1, name);
				cStmt.setString(2, password);
				cStmt.setString(3, email);
				cStmt.setString(4, phone);
				cStmt.setLong(5, userId);
				cStmt.execute();
				cStmt.close();
            }
            
            //drop roles (with scope all obviously) and then insert each role again)
            PreparedStatement cStmt = dbConn.prepareStatement(Queries.DROP_USER_ALL_ROLE);
            cStmt.setLong(1, userId);
            cStmt.execute();
            cStmt.close();
            //Now insert the all roles
            NodeList roleList = ((Element)(userInfoXML.getElementsByTagName("all_scope").item(0))).getElementsByTagName("role");
            for (int i=0,count = roleList.getLength();i < count; i++) {
                Element role = (Element) roleList.item(i);
                long insertId = helperInsertRoleForUser(userId, Misc.getParamAsLong(role.getAttribute("rid")));
                helperInsertDataMapping(insertId, 1, 1, Misc.getUndefInt(),false);//TODO ... make the grantable parameter dynamic
            }
            boolean activateOnce = "1".equals(rootElem.getAttribute("reactivate"));
            boolean forceLogout = "1".equals(rootElem.getAttribute("force_logout"));
			if (activateOnce) {
				PreparedStatement ps = dbConn.prepareStatement("update users set allow_next_login=1 where id=?");
				ps.setLong(1, userId);
				ps.execute();
				ps = Misc.closePS(ps);
				if (newUser) {
					recordUserAdminActivity(dbConn, (int) userId, "Reactivating User", byUserId);
				}

			}
			if (forceLogout && Misc.g_doSingleSession) {
				SingleSession.forceLogout(dbConn, (int)userId, 2, null);
				if (newUser) {
					recordUserAdminActivity(dbConn, (int) userId, "Force Logout", byUserId);
				}

			}
			if (newUser) {
				recordUserAdminActivity(dbConn, (int) userId, "New User Created", byUserId);
			}
            cache.invalidateUserMap((int)userId);
            return userId;
        }
        catch (Exception e) {
			e.printStackTrace();
			throw e;
        }
    }
    public void recordUserAdminActivity(Connection conn, int userId, String activity, int byUser) throws Exception {
    	PreparedStatement ps = conn.prepareStatement("insert user_mgmt_track (user_id, ts, activity, by_user_id) values (?,now(), ?, ?) " );
    	ps.setInt(1, userId);
    	ps.setString(2, activity);
    	ps.setInt(3, byUser);
    	ps.execute();
    	ps = Misc.closePS(ps);
    }
    public void deleteUserPrj(long userId, long projectId, long workspaceId) throws Exception {
          //drop roles (with scope all obviously) and then insert each role again)
          try {
            PreparedStatement cStmt = dbConn.prepareStatement(Queries.DROP_USER_PRJ_ROLE);
            cStmt.setLong(1, userId);
            cStmt.setLong(2, projectId);
            cStmt.setLong(3, workspaceId);
            cStmt.execute();
            cStmt.close();
            cache.invalidateUserMap((int)userId);
          }
          catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
    }

    public long updateUserPrj(Document userInfoXML) throws Exception {
        try {
            Element rootElem = userInfoXML.getDocumentElement();
            long userId = Misc.getParamAsLong(rootElem.getAttribute("uid"));
            long projectId = Misc.getParamAsLong(rootElem.getAttribute("project_id"));
            long workspaceId = Misc.getParamAsLong(rootElem.getAttribute("workspace_id"));

            //drop roles (with scope all obviously) and then insert each role again)
            deleteUserPrj(userId, projectId, workspaceId);
            //Now inser the all roles
            NodeList roleList = ((Element)(userInfoXML.getElementsByTagName("item_scope").item(0))).getElementsByTagName("role");
            for (int i=0,count = roleList.getLength();i < count; i++) {
                Element role = (Element) roleList.item(i);
                long insertId = helperInsertRoleForUser(userId, Misc.getParamAsLong(role.getAttribute("rid")));
                long todo_workspaceId = "1".equals(role.getAttribute("wkspid"))? workspaceId: Misc.getUndefInt();
                helperInsertDataMapping(insertId, 3, projectId, todo_workspaceId, false); //TODO make the grantable dynamic
            }
            cache.invalidateUserMap((int)userId);
            return userId;
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
    }

    public void deleteUserPort(long userId, long portNodeId) throws Exception {
          //drop roles (with scope all obviously) and then insert each role again)
          try {
            PreparedStatement cStmt = dbConn.prepareStatement(Queries.DROP_USER_PORT_ROLE);
            Misc.setParamLong(cStmt, userId,1);
            Misc.setParamLong(cStmt, userId,2);
            cStmt.setLong(3, portNodeId);
            cStmt.execute();
            cStmt.close();
            cache.invalidateUserMap((int)userId);
          }
          catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
    }

    public long updateUserPort(Document userInfoXML) throws Exception {
        try {
            Element rootElem = userInfoXML.getDocumentElement();
            long userId = Misc.getParamAsLong(rootElem.getAttribute("uid"));
            long portNodeId = Misc.getParamAsLong(rootElem.getAttribute("portfolio_id"));
            if (Misc.isUndef(portNodeId))
               return Misc.getUndefInt();
            if (portNodeId == 1) { //add the privilege on a global scope level .. TODO later
            
            }
            //drop roles (with scope all obviously) and then insert each role again)
            deleteUserPort(userId, portNodeId);
            //Now inser the all roles
            NodeList roleList = ((Element)(userInfoXML.getElementsByTagName("item_scope").item(0))).getElementsByTagName("role");
            for (int i=0,count = roleList.getLength();i < count; i++) {
                Element role = (Element) roleList.item(i);
                userId = Misc.getParamAsLong(role.getAttribute("uid"), userId);
                long rid = Misc.getParamAsLong(role.getAttribute("rid"));
                if (Misc.isUndef(rid) || Misc.isUndef(userId)) { 
                   continue;
                }
                long insertId = helperInsertRoleForUser(userId, Misc.getParamAsLong(role.getAttribute("rid")));
                boolean grantable = "1".equals(role.getAttribute("grantable"));
                helperInsertDataMapping(insertId, 2, portNodeId, Misc.getUndefInt(), grantable);
            }
            cache.invalidateUserMap((int)userId);
            return userId;
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
    }

    public void deleteRole(long roleId) throws Exception {
        try {
            if (Misc.isUndef(roleId))
               return;
            PreparedStatement cStmt = dbConn.prepareStatement(Queries.DELETE_ROLE);
            cStmt.setLong(1,roleId);
            cStmt.execute();
            cStmt.close();
            cache.invalidateUserMap(-1);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public long updateRole(Document userInfoXML) throws Exception {
        //if failed will return undef
        //otherwise will return the userId
        //if new will create
        try {
            Element rootElem = userInfoXML.getDocumentElement();
            long roleId = Misc.getParamAsLong(rootElem.getAttribute("rid"));
            String name = rootElem.getAttribute("name");
            String desc = rootElem.getAttribute("desc");
            int scope = Misc.getParamAsInt(rootElem.getAttribute("scope"), 1);
            String extCode = Misc.getParamAsString(rootElem.getAttribute("ext_code"), null);

            if (Misc.isUndef(roleId)) {
               //create role
               roleId = Misc.getNextId(dbConn, Sequence.ROLES);//sqlchanged
               PreparedStatement cStmt = Misc.G_DO_ORACLE ? dbConn.prepareStatement(Queries.CREATE_ROLE):dbConn.prepareStatement(Queries.CREATE_ROLE, Statement.RETURN_GENERATED_KEYS);
               int paramIndex = 1;
               if (Misc.G_DO_ORACLE)
                  cStmt.setLong(paramIndex++,roleId);
               cStmt.setString(paramIndex++, name);
               cStmt.setString(paramIndex++, desc);
               cStmt.setInt(paramIndex++, scope);
               cStmt.setString(paramIndex++, extCode);
               roleId = Misc.executeGetId(cStmt, roleId, Queries.CREATE_ROLE);
               cStmt.close();
            }
            else {
               PreparedStatement cStmt = dbConn.prepareStatement(Queries.UPDATE_ROLE);
               cStmt.setString(1, name);
               cStmt.setString(2, desc);
               cStmt.setString(3, extCode);
               cStmt.setLong(4, roleId);
               
               cStmt.execute();
               cStmt.close();
            }
            //drop roles (with scope all obviously) and then insert each role again)
            PreparedStatement cStmt = dbConn.prepareStatement(Queries.DROP_ROLE_PRIV);
            cStmt.setLong(1, roleId);
            cStmt.execute();
            cStmt.close();
            //Now insert the all roles
            NodeList privList = ((Element)(userInfoXML.getElementsByTagName("priveleges").item(0))).getElementsByTagName("priv");
            cStmt = dbConn.prepareStatement(Queries.INSERT_ROLE_PRIV);
            cStmt.setLong(1, roleId);
            for (int i=0,count = privList.getLength();i < count; i++) {
                Element priv = (Element) privList.item(i);
                cStmt.setLong(2,Misc.getParamAsLong(priv.getAttribute("pid")));
                cStmt.execute();
            }
            cStmt.close();
            cache.invalidateUserMap(-1);
            return roleId;
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
    }


    public long helperInsertRoleForUser(long userId, long roleId) throws Exception {
        try {
            long retval = Misc.getNextId(dbConn,Sequence.USER_ROLES);//sqlchanged
            PreparedStatement cStmt = Misc.G_DO_ORACLE ? dbConn.prepareStatement(Queries.INSERT_USER_ROLE) : dbConn.prepareStatement(Queries.INSERT_USER_ROLE, Statement.RETURN_GENERATED_KEYS);
            int paramIndex = 1;
            if (Misc.G_DO_ORACLE)
               cStmt.setLong(paramIndex++, retval);
            cStmt.setLong(paramIndex++, userId);
            cStmt.setLong(paramIndex++, roleId);
            retval = Misc.executeGetId(cStmt, retval, Queries.INSERT_USER_ROLE);
            cStmt.close();
            return retval;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void helperInsertDataMapping(long userRoleEntry, int typeOfMapping, long mainItemId, long secItemId, boolean grantable) throws Exception {
       // typeOfMapping 1 ==> all, 2 ==> portfolio, 3 ==> project
       // mainItemId  - for 2,3 ==> port_node_id, project_id resp
       // secItemId   = for 3 only ==> workspaceId
       try {
           PreparedStatement cStmt = dbConn.prepareStatement(Queries.INSERT_USER_ROLE_SCOPE);
           cStmt.setLong(1, userRoleEntry);
           if (typeOfMapping == 1)
              cStmt.setInt(2, 1);
           else
              cStmt.setNull(2, Types.BIGINT);

           if (typeOfMapping == 2)
              cStmt.setLong(3, mainItemId);
           else
              cStmt.setNull(3, Types.BIGINT);

           if (typeOfMapping == 3) {
              cStmt.setLong(4, mainItemId);
              if (!Misc.isUndef(secItemId)) {
                 cStmt.setLong(5, secItemId);
              }
              else {
                 cStmt.setNull(5, Types.BIGINT);
              }
           }
           else {
              cStmt.setNull(4, Types.BIGINT);
              cStmt.setNull(5, Types.BIGINT);
           }
           cStmt.setInt(6, grantable ? 1 : 0);
           cStmt.execute();
           cStmt.close();
       }
       catch (Exception e) {
           e.printStackTrace();
           throw e;
       }
    }

    public void addUserDetail(long userId, Element rootElem) throws Exception {
        try {
            PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_USER_DETAIL);
            pStmt.setLong(1, userId);
            ResultSet rset = pStmt.executeQuery();
            if (rset.next()) {
                rootElem.setAttribute("uid", Long.toString(rset.getLong(1)));
                rootElem.setAttribute("name", Misc.getRsetString(rset, 2));
                rootElem.setAttribute("email", Misc.getRsetString(rset,3));
                rootElem.setAttribute("lname",Misc.getRsetString(rset,4));
                rootElem.setAttribute("phone",Misc.getRsetString(rset,5));
                rootElem.setAttribute("password",Misc.getRsetString(rset,6));
            }
            rset.close();
            pStmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void addUserAllRoles(long userId, Element allScopeElem, Document addToThis) throws Exception {
         try {
            PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_USER_ROLES_ALL);
            pStmt.setLong(1,userId);
            ResultSet rset = pStmt.executeQuery();
            Element currRoleElem = null;
            while (rset.next()) {
                long roleId = rset.getLong(1);
                currRoleElem = addToThis.createElement("role");
                currRoleElem.setAttribute("rid", Long.toString(roleId));
                allScopeElem.appendChild(currRoleElem);
            }
            rset.close();
            pStmt.close();
            cache.invalidateUserMap((int)userId);
         }
         catch (Exception e) {
            e.printStackTrace();
            throw e;
         }

    }

    public  RoleHelper(Connection dbConn, Cache cache){
       this.dbConn = dbConn;
       this.cache  = cache;
    }
	
	/**
	 * For reassigning pending approvals for a user.
	 * 
	 * @param approvalInfoXML 	Xml Document containing information about reassigning.
	 * @param drvUserId 		User id for which approvals have to be reassigned.
	 * @throws Exception
	 */
	public void reassignUserApprovals(Document approvalInfoXML, int drvUserId) throws Exception {
	    // The structure of the document passed is 
		// <approvals_list>
		//		<approval reassign_chk="" uid="" wf_id="" wf_step="" approver_role="" prj_id=""/>
	    //		..
	    // </approvals_list>
		
		PreparedStatement pStmt = null;
		String queryStr = null;
		queryStr = Queries.REASSIGN_PENDING_APPROVAL_FOR_USER;
		try {
			Element rootElem = approvalInfoXML.getDocumentElement();
			
			// Only process nodes for which reassign is required
			String searchString = "//approval[@reassign_chk=\"on\"]";
			NodeList approvalNodes = XPathAPI.selectNodeList(rootElem, searchString);
			if (approvalNodes != null) {
			    pStmt = dbConn.prepareStatement(queryStr);
			    pStmt.setInt(2, drvUserId);
				for (int i = 0; i < approvalNodes.getLength(); i++) {
					Element approvalElem = (Element) approvalNodes.item(i);
					int userId = Misc.getParamAsInt(approvalElem.getAttribute("uid"));
					if (! Misc.isUndef(userId)) {
						int wspaceId = Misc.getParamAsInt(approvalElem.getAttribute("wf_id"));
						int wfStep = Misc.getParamAsInt(approvalElem.getAttribute("wf_step"));
						int approverRole = Misc.getParamAsInt(approvalElem.getAttribute("approver_role"));
						
						pStmt.setInt(1, userId);
						pStmt.setInt(3, wspaceId);
						pStmt.setInt(4, wfStep);
						pStmt.setInt(5, approverRole);
						pStmt.execute();
						
						cache.invalidateUserMap(userId);
					}
				}
				if (pStmt != null)
					pStmt.close();
			    pStmt = null;
			}
			cache.invalidateUserMap(drvUserId);
		}
		catch (Exception e) {
		   e.printStackTrace();
		   throw e;
		}
	}
	
	/**
	 * For reassigning approving roles for a user.
	 * 
	 * @param approvalInfoXML	Xml document containing information about reassigning.
	 * @param drvUserId			User id for which approving roles have to be reassigned.
	 * @param portNodeId		Port node id for which roles have to be reassigned.
	 * @throws Exception
	 */
	public void reassignUserApprovingRoles(Document approvalInfoXML, int drvUserId, int portNodeId) throws Exception {
	    // The structure of the document passed is 
	    // <approvals_roles_list>
	    //      <approval_roles reassign_chk="" wf_role_id="" classify1="" classify2="" classify3="" 
		//			classify4="" classify5="" thresh="" user_name="" uid=""/>
	    //      ..
	    // </approvals_roles_list>
		
		PreparedStatement pStmt = null;
		String queryStr = null;
		queryStr = Queries.REASSIGN_APPROVING_ROLE_FOR_USER;
		try {
			Element rootElem = approvalInfoXML.getDocumentElement();
			
			// Only process nodes for which reassignment is required.
			String searchString = "//approval_roles[@reassign_chk=\"on\"]";
			NodeList approvingRoleNodes = XPathAPI.selectNodeList(rootElem, searchString);
			if (approvingRoleNodes != null) {
			    pStmt = dbConn.prepareStatement(queryStr);
				pStmt.setInt(8, drvUserId);
				pStmt.setInt(10, portNodeId);
				for (int i = 0; i < approvingRoleNodes.getLength(); i++) {
					Element approvingRoleElem = (Element) approvingRoleNodes.item(i);
					int userId = Misc.getParamAsInt(approvingRoleElem.getAttribute("uid"));
					if (! Misc.isUndef(userId)) {
						int wfRoleId = Misc.getParamAsInt(approvingRoleElem.getAttribute("wf_role_id"));
						if (wfRoleId < 0)
							continue;
						int thresh = Misc.getParamAsInt(approvingRoleElem.getAttribute("thresh"));	
					    
						pStmt.setInt(1, userId);
					    for (int j = 0;j < 5;j++)
					       Misc.setParamInt(pStmt, approvingRoleElem.getAttribute("classify"+Integer.toString(j + 1)), 2 + j);
						Misc.setParamInt(pStmt, thresh, 2 + 5);
						pStmt.setInt(9, wfRoleId);
						pStmt.execute();
						
						cache.invalidateUserMap(userId);
					}
				}
				if (pStmt != null)
				    pStmt.close();
				pStmt = null;
			}
			cache.invalidateUserMap(drvUserId);
		}
		catch (Exception e) {
		   e.printStackTrace();
		   throw e;
		}
	}
}


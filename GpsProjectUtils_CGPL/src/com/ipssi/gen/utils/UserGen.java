
// Copyright (c) 2000 IntelliPlanner Software Systems,  Inc.
package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public class UserGen {
  int m_forUser = Misc.getUndefInt(); //For future
  int m_forProjectId = Misc.getUndefInt(); //For future
  int m_forWorkspaceId = Misc.getUndefInt();//For future
  boolean m_isDirty = false; //For future
  
  public static class Menu {
      public MenuItem m_menu;
      public String m_idVal = null;
      public String m_labelIfGen = null;
      public Menu m_parent = null;
      public ArrayList m_children = null;
      char m_ismFlag = 'm'; //dont know what it is ... but is imp from old code
      public boolean m_overrideToHome = false;      
      public boolean m_isSelected = false;
      public boolean m_isGenerated = false; //if generated then do not generate against it
      public String toString() {
    	  StringBuilder sb = new StringBuilder();
    	  sb.append("<m n=").append(m_menu).append(">");
    	  for (int i=0,is=m_children == null ? 0 : m_children.size(); i<is; i++) {
    		  sb.append(m_children.get(i).toString());
    	  }
    	  sb.append("</m>");
    	  return sb.toString();
      }
      public Menu(MenuItem menu) {
         m_menu = menu;         
      }
      public void addAsChild(Menu chMenu) {
         if (chMenu == null)
            return;
         if (m_children == null)
            m_children = new ArrayList(12);
         m_children.add(chMenu);
         chMenu.m_parent = this;
      }
      public void removeChild(Menu chMenu) {
         m_children.remove(chMenu);
         chMenu.m_parent = null;
      }
      void insertBefore(Menu item, Menu beforeThis) {
          if (item == null)
             return;
          if (beforeThis == null)
             addAsChild(item);
          item.m_parent = this;
          if (m_children == null)
             m_children = new ArrayList();
          int pos = m_children.size();
          for (int i=0,is = pos;i<is;i++) {
              if (beforeThis == (Menu) m_children.get(i)) {
                 pos = i;
                 break;
              }
          }
          m_children.add(null);
          for (int i=m_children.size()-2;i>=pos;i--)
             m_children.set(i+1, m_children.get(i));
          m_children.set(pos, item);
     }
      public Menu cloneNode(boolean doDeep) {
         Menu retval = new Menu(m_menu);
         retval.m_ismFlag = m_ismFlag;
         retval.m_overrideToHome = m_overrideToHome;
         retval.m_idVal = m_idVal;
         retval.m_labelIfGen = m_labelIfGen;
         retval.m_isGenerated = m_isGenerated;
         if (doDeep) {
            ArrayList children = m_children;
            for (int i=0,is = children == null ? 0 : children.size();i<is;i++) {
               Menu ch = ((Menu)children.get(i)).cloneNode(doDeep);
               retval.addAsChild(ch);
            }
         }
         return retval;
      }
  }//end of inner Menu class
  
  public static class MenuFind {
     public Menu m_menu = null;
     public boolean m_matchQuality = false;
     public MenuFind(Menu menu, boolean matchQuality) {
        m_menu = menu;
        m_matchQuality = matchQuality;        
     }
  }
  
  private static Menu generatePrivBasedCopy(MenuItem forItem, User user, SessionManager session, PageMenuHeadCalc pageMenuHeadCalc) throws Exception {        
     Menu retval = new Menu(forItem);
     ArrayList children = forItem.m_children;
     boolean doingTopLevel = forItem.m_parent == null;
     boolean doingUnloggedUser = user.getUserId() <= 0;
     boolean match = false;
     
     for (int i=0,is=children == null ? 0 : children.size();i<is;i++) {
        MenuItem chItem = (MenuItem) children.get(i);
        if (doingUnloggedUser) {           
           Menu chMenu = new Menu(chItem);
           chMenu.m_ismFlag = 'm';
           chMenu.m_overrideToHome = true;
           retval.addAsChild(chMenu);
           continue;
        }
        Menu chMenu = generatePrivBasedCopy(chItem, user, session, pageMenuHeadCalc);
        if (chMenu != null) {
           match = true;
           retval.addAsChild(chMenu);
        }
        else {
           
        }
     }//for each child
     
     if (doingUnloggedUser)
        match = true;     
     if (!match) {
        Cache cache = session.getCache();
        String tag = forItem.m_tag;
        if ("home".equals(tag))
           match = true;
        else {
           String checkForPrivsOf = forItem.m_accCheck;
           if (checkForPrivsOf == null || checkForPrivsOf.length() == 0)
             checkForPrivsOf = tag;
           if ("balwant_test1".equals(checkForPrivsOf)  || "status_1".equals(tag)) {
              int dbg=1;
              System.out.println("sdsdsdf");
           }
           
               ArrayList privListForNode = cache.getPrivListAvForMenuTag(checkForPrivsOf);//was currElem.getTagName TEST 072507
               if (privListForNode != null) {
                  Iterator iter=privListForNode.iterator();
                  while (iter.hasNext()) {
                    Integer privId = (Integer) iter.next();
                    match = user.isPrivAvailable(session, privId.intValue(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), true, checkForPrivsOf);
                    if (match)
                       break;
                 }//for each priv for the tag
               }//if there exists priv for the tag			
		

        }//not doing home
     }//children did not tell of match
     if (!match) {
        retval = null;
        pageMenuHeadCalc.setMenuUnavailable(forItem);
     }
     else {
        retval.m_ismFlag = user.isSuperUser() ? 'm' : 'c'; //why ... dont know        
     }
     return retval;
  }
  private static Menu generateMenuNodes(Menu similarNode, User user, SessionManager session) throws Exception { //returns the Element from where traversal should happen again .. will clean up everything appropriately
     //don't forget to set the attributes for the ID(s)
     // and the names
     //Implemented only for nodeName == "alt_list"
     //TODO throw exception if nodeName != alt_list/plan_list
   try {
     String nodeName = similarNode.m_menu.m_tag;
     if (nodeName.equals("alt_list")) {        
        Connection dbConn = session.getConnection();
        PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_ALT_LIST);        
        pStmt.setLong(1,session.getWorkspaceId());
        ResultSet rset = pStmt.executeQuery();
        int count = 0;
        while (rset.next()) {           
           long altId = rset.getLong(1);           
           String name = rset.getString(2);
           Menu nodeToAdd =  similarNode.cloneNode(true);
           nodeToAdd.m_isGenerated = true;
           count++;
           nodeToAdd.m_idVal =  Long.toString(altId);
           nodeToAdd.m_labelIfGen =  Misc.G_ALT_MENU_LABEL+name;
           similarNode.m_parent.addAsChild(nodeToAdd);           
        }
        rset.close();
        pStmt.close();
        rset = null;
        pStmt = null;
        session.setAttribute("_alt_count",Integer.toString(count),false);


        //hierarchy is (..) -> Alternatives -> Alt1, Alt2, Alt3, SimilarNode etc.
        //if altCount <= 1 then
        //   (...) -> sub children of Alt1 except excluding alt_basic node
        //if altCount > then
        //   (...) -> Alt1, Alt2 etc.
        if (count <= 1) {
           Menu parent = similarNode.m_parent; //this is the alternatives node
           Menu grandParent = parent.m_parent; //this is the projects
           parent.removeChild(similarNode);
           String idNameString = null;
           String idValString = null;
           ArrayList altListLikeChList = parent.m_children;
           for (int i=0,is = altListLikeChList == null ? 0 : altListLikeChList.size();i<is;i++) {
               Menu altListLikeElem = (Menu) altListLikeChList.get(i);
               
               idNameString = altListLikeElem.m_menu.m_idParam;
               if (idNameString == null || idNameString.length() == 0)
                  continue;
               idValString = altListLikeElem.m_idVal;

               ArrayList childrenOfAltListLikeElem = altListLikeElem.m_children;
               int itemNotToMoveToGP = -1;
               for (int j=0,js = childrenOfAltListLikeElem == null ? 0:childrenOfAltListLikeElem.size();j<js;j++) {                  
                   Menu chElem = (Menu) childrenOfAltListLikeElem.get(j);
                   if (chElem.m_menu.m_tag.equals("alt_basic"))
                      continue;
                   //alt1LikeCh.removeChild(chElem); not needed the way we have coded
                  grandParent.insertBefore(chElem, parent);
              }
           }
           if (idNameString != null && idValString != null)
               grandParent.m_idVal = idValString;               
           grandParent.removeChild(parent);
           return grandParent;
        }
        else {
           Menu parent = similarNode.m_parent;
           Menu grandParent = parent.m_parent;           
           
           parent.removeChild(similarNode);
           ArrayList chElemList = parent.m_children;
           for (int i=0,is = chElemList == null ? 0 : chElemList.size();i<is;i++) {
               Menu chElem = (Menu) chElemList.get(i);
               String idNameString = chElem.m_menu.m_idParam;
               if (idNameString == null || idNameString.length() == 0)
                  continue;
               grandParent.insertBefore(chElem, parent);
           }
           grandParent.removeChild(parent);
           return grandParent;
        }
     }
     /*
     else if (nodeName.equals("plan_list")) {
        Connection dbConn = (Connection) request.getAttribute("_dbConnection");
        PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_PLAN_LIST);
        ResultSet rset = pStmt.executeQuery();
        while (rset.next()) {
           long planId = rset.getLong(1);
           String name = rset.getString(2);
           Element nodeToAdd = (Element) similarNode.cloneNode(true);
           nodeToAdd.setAttribute(nodeToAdd.getAttribute("id"), Long.toString(planId));
           nodeToAdd.setAttribute("name", name);
           similarNode.getParentNode().appendChild(nodeToAdd);
        }
        Element parent = (Element) similarNode.getParentNode();
        parent.removeChild(similarNode);

        rset.close();
        pStmt.close();
        rset = null;
        pStmt = null;
        return parent;

     }
     */
     else {
        return similarNode;
     }
   }
   catch (Exception e) {
      e.printStackTrace();
      throw e;
   }
  }//end of generate nodes function
  
  private static void expandMenu(Menu currentNode, User user, SessionManager session) throws Exception {
     if (currentNode.m_menu.m_isGenerated && !currentNode.m_isGenerated) {
        //currentNode.removeAttribute("type"); //must - but leads to hack in appendMenuChild though
                                             //all generated copies are traversed down for expansion
        currentNode = generateMenuNodes(currentNode, user, session);
     }
     ArrayList childNodes = currentNode.m_children;     
     if (childNodes != null) {
         for (int i=0;i<childNodes.size();i++) {
            Menu temp = (Menu) childNodes.get(i);        
            expandMenu(temp, user, session);        
         }
     }
  }
  
  public static Menu getMenu(User user, SessionManager session) throws Exception { //returns the MenuTree
     try {  
         MenuItem item = MenuItem.getMenuInfo("main");
         PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc((int)session.getProjectId(), (int)session.getWorkspaceId(), (int)session.getAlternativeId(), session, session.getUser(), session.getCache(), session.getLogger(), session.getConnection());
         pageMenuHeadCalc.clearMenuLookupTags();
         Menu retval = generatePrivBasedCopy(item,user,session, pageMenuHeadCalc);
         expandMenu(retval, user, session);
         return retval;
     }  
     catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
  }
  private static MenuFind helpMenuGetElem(User user, SessionManager session, Menu lookUnder, String menuTagName, String idMatch, String idParamFromTop, ArrayList reversePathWay, int depthFromTop, boolean ignoreIdMatch) throws Exception {
     
     if (lookUnder.m_menu.m_tag.equals(menuTagName)) {
        return new MenuFind(lookUnder, idMatch == null || ignoreIdMatch || idMatch.equals(idParamFromTop));
     }
     ArrayList children = lookUnder.m_children;
     Menu tryThis = null;
     MenuFind bestFind = null;
     for (int i=0,is=children == null ? 0:children.size();i<is;i++) {
         Menu child = (Menu) children.get(i);
         MenuItem childMenuItem = child.m_menu;
         int ps = reversePathWay.size()-1;
         MenuFind currFind = null;
         String idParamForTopCurr = child.m_idVal;
         if (idParamForTopCurr == null)
            idParamForTopCurr = idParamFromTop;
         for (int j=ps-depthFromTop;j>=0;j--) {
             if (childMenuItem == (MenuItem)reversePathWay.get(j)) {                
                currFind = helpMenuGetElem(user,session,child, menuTagName, idMatch, idParamForTopCurr, reversePathWay, ps-j+1,ignoreIdMatch);
                break;
             }
         }
         if (currFind != null) {
             if (currFind.m_matchQuality) {
                  bestFind = currFind;
                  break;
             }
             else if (bestFind == null)
                bestFind = currFind;
         }
     }
     return bestFind;
  }
  public static Menu menuGetElem(User user, SessionManager session, Menu menu, String menuTagName, String altMenuTagName, String parentTag) throws Exception {   
     return menuGetElem(user, session, menu, menuTagName, altMenuTagName, parentTag, false);
  }
  public static Menu menuGetElem(User user, SessionManager session, Menu menu, String menuTagName, String altMenuTagName, String parentTag, boolean ignoreIdMatch) throws Exception {   
     if (menuTagName == null || menuTagName.length() == 0)
        menuTagName = "home";
     MenuItem menuItem = MenuItem.getMenuInfo(menuTagName);
     if (menuItem == null) {
        menuTagName = altMenuTagName;
        menuItem = MenuItem.getMenuInfo(menuTagName);
     }
     
     if (menuItem == null) {
        menuTagName = "home";
        menuItem = MenuItem.getMenuInfo(menuTagName);
     }
     String idMatch = null;
     ArrayList reversePathWay = new ArrayList(10);
     
     for (MenuItem t = menuItem; t!=null ;t = t.m_parent) {
        if (t.m_isGenerated && t.m_idParam != null && idMatch == null) {
           idMatch = session.getParameter(t.m_idParam);
           if (idMatch != null && idMatch.length() == 0)
              idMatch = null;
        }
        reversePathWay.add(t);
     }
     
     Menu lookUnder = menu;
     if (parentTag != null && parentTag.length() != 0) {
         ArrayList children = lookUnder.m_children;
         for (int i=0,is=children == null ? 0:children.size();i<is;i++) {
             Menu t = (Menu) children.get(i);
             if (t.m_menu.m_tag.equals(parentTag)) {
                lookUnder = t;
                break;
             }
         }
     }     
     MenuFind find = helpMenuGetElem(user, session, lookUnder, menuTagName, idMatch, lookUnder.m_idVal, reversePathWay, 1,ignoreIdMatch);          
     if (find == null && altMenuTagName != null && !altMenuTagName.equals(menuTagName)) {
        Menu retval = menuGetElem(user, session, lookUnder, altMenuTagName, null,null,ignoreIdMatch);
        if (retval != null)
           return retval;
           
     }
     if (find == null) {
        for (;lookUnder.m_parent != null;lookUnder = lookUnder.m_parent); //yup nothing in between
        Menu retval = menuGetElem(user, session, lookUnder, "home", null,null,ignoreIdMatch);
        if (retval != null)  
           return retval;
           
     }
     return find.m_menu;
  }
  
  private static boolean helpSetPrjSpecificMenuState(Menu currElem, int projectId, int workspaceId, int portfolioId, Cache cache, Connection dbConn, SessionManager session, int alternativeId, PageMenuHeadCalc pageMenuHeadCalc) throws Exception {
      boolean isRequired = true;
      User user = session.getUser();
      
      boolean isShowAll = pageMenuHeadCalc != null && pageMenuHeadCalc.isShowAllMenu();
      
      if (pageMenuHeadCalc != null && !pageMenuHeadCalc.isMenuAvailable(currElem.m_menu, false, dbConn, session, projectId, workspaceId, alternativeId) && !isShowAll) {
         isRequired = false; //top level is not ruled out
       }

      if (isRequired) //022807 ... for flex template true || clause added
      { //only then does it make sense to process downward
         isRequired = false; //now we will make this visible if all children are visible
         ArrayList children = currElem.m_children;
         for (int i=0,is = children == null ? 0 : children.size();i<is;i++) {
             Menu elem = (Menu) children.get(i);             
             boolean temp = helpSetPrjSpecificMenuState(elem, projectId, workspaceId, portfolioId, cache, dbConn, session, alternativeId, pageMenuHeadCalc);
             isRequired = isRequired || temp;
         } //end of looking at all children
         if (!isRequired) {
            String checkForPrivsOf = currElem.m_menu.m_accCheck;
            if (checkForPrivsOf == null || checkForPrivsOf.length() == 0)
               checkForPrivsOf = currElem.m_menu.m_tag;
//           if ("upload_template".equals(checkForPrivsOf)  || currElem.m_menu.m_tag.equals("status_1")) {
//              int dbg=1;
//           }
               
            ArrayList privListForNode = cache.getPrivListAvForMenuTag(checkForPrivsOf);//was currElem.getTagName()
            if (privListForNode != null) {
               Iterator iter=privListForNode.iterator();
               while (iter.hasNext()) {
                  Integer privId = (Integer) iter.next();
                  isRequired = isRequired || user.isPrivAvailable(session, privId.intValue(), projectId, workspaceId, portfolioId,true, null);
                  if (isRequired)
                    break;
               }
            } //if a valid privlist
         }
      }//if there is a possibility of node being useful
      //this piece of code is not going to be used
      String currElemTag = currElem.m_menu.m_tag;
       if (currElemTag.startsWith("prj_basic")) {
          if (Misc.isUndef(projectId)) {
             isRequired = true;
             pageMenuHeadCalc.setMenuAvailable(currElemTag);
//           m_pageMenuHeadCalc.m_menuTagLookup.put(currElemTag, m_pageMenuHeadCalc.m_oneInteger);
          }
       }


      if (isRequired) {
         currElem.m_ismFlag = 'c';       
      }
      else {
         currElem.m_ismFlag = 'n';         
         pageMenuHeadCalc.setMenuUnavailable(currElem.m_menu);
      }
      return isRequired;
  }
  
  public static Menu prepareMenuForPrint(Menu menu, User user, SessionManager session, String menuTagName, String altMenuTagName, MenuItem.Button buttonURLs[], String parentTag)  throws Exception {//returns the helpTag
     //Will process the menuTree to identify the selected nodes
     //The menuTree is changed because of that - the selected attrib as set to 1
     //Also fills in the array with the URLs of buttons - array is null terminated
   try {

     Cache cache = session.getCache();
     Connection dbConn = session.getConnection();
     Logger log = session.getLogger();
        
     PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc((int)session.getProjectId(), (int)session.getWorkspaceId(), (int)session.getAlternativeId(), session, user, cache, log, dbConn);

     if (menuTagName == null || menuTagName.length() == 0)
        menuTagName = "home";
     
     Menu selElem = menuGetElem(user, session, menu, menuTagName, altMenuTagName, parentTag);
     Menu retval = selElem;
     String helpTag = selElem.m_menu.m_helpTag;

     //Now starting from selElem until either "main" or "top" is found, mark the node as selected
     //In the process also collect the list of buttons that needs to be generated - the order of
     // the buttons is bottom is Lefter than top

     for (int i=0, is = buttonURLs == null ? 0 : buttonURLs.length;i<is;i++)
        buttonURLs[i] = null;
     int buttonNum = 0;
     String prevURL = null;     
     Menu prjElem = selElem;     
     while(selElem != null) {
        selElem.m_isSelected = true;        
        Menu parent = selElem.m_parent;
        if (buttonURLs != null) {
            ArrayList buttonList = selElem.m_menu.m_buttons;
            
            for (int i=0, count = buttonList == null ? 0 : buttonList.size(); i<count;i++) {           
               MenuItem.Button button = (MenuItem.Button)buttonList.get(i);
               buttonURLs[buttonNum++] = button;           
            }
        }
        if (selElem.m_parent != null)
           prjElem = selElem;        
        selElem = parent;
        
     }
           
     if (prjElem != null) {
        if (prjElem.m_menu.m_needsCheck){
  //            helpInitMenuXMLForPrj(prjElem);              
              helpSetPrjSpecificMenuState(prjElem, (int)session.getProjectId(), (int)session.getWorkspaceId(), (int)session.getPortfolioId(), cache, session.getConnection(), session, (int) session.getAlternativeId(), pageMenuHeadCalc);
        }
     }     
     return retval;
   }
   catch (Exception e) {
     e.printStackTrace();
     throw e;               
   }
  }
  
  public static boolean isMenuSelected(Menu menu) {
     return menu != null && menu.m_isSelected;
  }

  public static boolean toExpandMenu(Menu menu) {//true if the lower level menu is to expanded
    return menu.m_menu.m_url == null && menu.m_children != null && menu.m_children.size() > 0;
  }

  public static void  undoMenuSelected(Menu menu) {
     menu.m_isSelected = false;     
  }

  public static boolean isMenuToPrint(Menu menu) throws Exception {
     return isMenuToPrint(menu,false);
  }
  public static boolean isMenuToPrint(Menu menu, boolean wizardMode) throws Exception {
     if (menu.m_menu.m_tag.equals("home"))
         return true;
     char ismAttrib = menu.m_ismFlag;     
     boolean retval = ('c' == ismAttrib || 'm' == ismAttrib); 
     if (retval && wizardMode) {
        retval = !menu.m_menu.m_disableIfNoPrj;        
     }
     return retval;
  }

  
  public static void loadParamsFromMenuSpec(SessionManager session) throws Exception {
      String pgContext = session.getParameter("page_context");
      if (pgContext != null)
         loadParamsFromMenuSpec(session, pgContext);      
  }
  public static void loadParamsFromMenuSpec(SessionManager session, String pgContext) throws Exception {
      Menu menu = menuGetElem(session.getUser(), session, session.getMenu(), pgContext, null, null);      
      loadParamsFromMenuSpec(menu, session);
  }
  private static void loadParamsFromMenuSpec(Menu menuElem, SessionManager session) throws Exception {
     if (menuElem == null)
        return;
     ArrayList params = menuElem.m_menu.m_params;
     for (int i=0,is = params == null ? 0 : params.size();i<is;i++) { 
        MenuItem.Param param = (MenuItem.Param) params.get(i);
        if (param.m_isFixed) {
           String paramName = param.m_name;
           String paramVal = param.m_value;
           if (paramVal != null && paramVal.length() != 0)
              session.setAttribute(paramName, paramVal, false);
        }        
     }
     return;     
  }
  
  public static ArrayList<Pair<String, String>> getParamsFromMenuSpec(String pgContext, SessionManager session) throws Exception {
	     if (pgContext == null)
	        return null;
	     Menu menuElem = menuGetElem(session.getUser(), session, session.getMenu(), pgContext, null, null);
	     if (menuElem == null)
	    	 return null;
	     ArrayList<Pair<String, String>> retval = new ArrayList<Pair<String, String>>();
	     
	     ArrayList params = menuElem.m_menu.m_params;
	     for (int i=0,is = params == null ? 0 : params.size();i<is;i++) { 
	        MenuItem.Param param = (MenuItem.Param) params.get(i);
	        if (param.m_isFixed) {
	           String paramName = param.m_name;
	           String paramVal = param.m_value;
	           if (paramVal != null && paramVal.length() != 0)
	        	   retval.add(new Pair<String, String>(paramName, paramVal));
	        }        
	     }
	     return retval;     
	  }
  public static String getParamFor(SessionManager session, String pgContext, String param) throws Exception {
      Menu menu = menuGetElem(session.getUser(), session, session.getMenu(), pgContext, null, null);            
      return getParamFor(menu, param);
  }
  public static String getParamFor(Menu menu, String paramName) throws Exception {
     if (menu == null)
        return null;
     ArrayList params = menu.m_menu.m_params;
     for (int i=0,is = params == null ? 0 : params.size();i<is;i++) { 
        MenuItem.Param param = (MenuItem.Param) params.get(i);
        if (param.m_name.equals(paramName))
            return param.m_value;        
     }
     return null;     
  }
  
  public static String getParamForPrivSeq(SessionManager session, String pgContext, String paramPrefix, Cache cache, String defaultVal) throws Exception {
     // to understand consider prefix of: front_template_id. Basically the goal is to find the appropriate param value available for user
     // param names = prefix+N, except for N = 0, no suffix
     // priv controlling param = priv_+param Name. If N = 0 then always available
     // first check in session ... and get the maximum privId available 
     // then check in MenuParam
     
      User user = session.getUser();
      Menu menu = menuGetElem(session.getUser(), session, session.getMenu(), pgContext, null, null);
      if (menu == null)
         return null;
      int currMaxForWhichExist = 0;
      //first check in session
      String paramValFound = null;
      if (paramValFound != null && paramValFound.length() != 0)
         return paramValFound;
      while (true) {
         String paramName = currMaxForWhichExist == 0 ? paramPrefix : paramPrefix+Integer.toString(currMaxForWhichExist);
         String paramVal = session.getParameter(paramName);
         if (paramVal != null && paramVal.length() != 0) {
             if (currMaxForWhichExist != 0) {
                String privTagParamName = "priv_"+paramName;
                String privTag = getParamFor(menu, privTagParamName);
                if (privTag != null) { //040808 ...
                   PrivInfo.TagInfo tagInfo = cache.getPrivId(privTag);//040808
                   if (tagInfo == null || user.isPrivAvailable(session, tagInfo.m_read, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(),true,Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), true, null))                   
                       paramValFound = paramVal;
                }//...040808 ..replaces older user.isPrivAvailable portion
             }
             else {
                paramValFound = paramVal;
             }
             currMaxForWhichExist++;
         }
         else {
             break;
         }
      }
      if (paramValFound != null)
         return paramValFound;
      paramValFound = defaultVal;
      if (paramValFound != null && paramValFound.length() != 0)
         return paramValFound;
      while (true) {
         String paramName = currMaxForWhichExist == 0 ? paramPrefix : paramPrefix+Integer.toString(currMaxForWhichExist);
         String paramVal = getParamFor(menu, paramName);
         if (paramVal != null && paramVal.length() != 0) {
             if (currMaxForWhichExist != 0) {
                String privTagParamName = "priv_"+paramName;
                String privTag = getParamFor(menu, privTagParamName);
                if (privTag != null) { //040808 ...
                   PrivInfo.TagInfo tagInfo = cache.getPrivId(privTag);//040808
                   if (tagInfo == null || user.isPrivAvailable(session, tagInfo.m_read, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(),true,Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), true, null))                   
                       paramValFound = paramVal;
                }//...040808 ..replaces older user.isPrivAvailable portion                
             }
             else {
                paramValFound = paramVal;
             }
             currMaxForWhichExist++;
         }
         else {
             break;
         }
      }
      return paramValFound;
  }
  
  
  
  public static String helpGetAndSetMainMenuLevelContext(SessionManager session, String menuTagName)throws Exception {
     String retval = (String)session.getAttribute("_main_page_context");
     if (retval != null && retval.length() !=0 )
        return retval;
     Connection dbConn = session.getConnection();
     Logger log = session.getLogger();
     Cache cache = session.getCache();
     User user = session.getUser();
     
     PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc((int)session.getProjectId(), (int)session.getWorkspaceId(), (int)session.getAlternativeId(), session, user, cache, log, dbConn);

     if (menuTagName == null || menuTagName.length() == 0)
        menuTagName = "home";
     Menu menu = session.getMenu();
     Menu selElem = menuGetElem(user, session, menu, menuTagName,null,null);
     if (selElem == null)
        return null;
     boolean done = false;
     Menu prjElem = selElem;
     while(!done) {
        //selElem.setAttribute("_sel", "1"); //is selected label
        Menu parent = selElem.m_parent;

        selElem = parent;
        String selElemName = selElem.m_menu.m_tag;        
        if (selElemName.equals("main")) {
           done = true;
           retval = prjElem.m_menu.m_tag;
           session.setAttribute("_main_page_context", retval,false);           
        }        
        if (!done) 
          prjElem = selElem;        
     }
     return retval;  
  }
  public static final String unexpandedImage = ""+Misc.G_IMAGES_BASE+"arrowright.gif";
  public static final String expandedImage = ""+Misc.G_IMAGES_BASE+"arrowdown.gif";
  public static final String leafLevelImage = ""+Misc.G_IMAGES_BASE+"arrowright.gif";
  public static final String selLeafLevelImage = ""+Misc.G_IMAGES_BASE+"arrow_white.gif";

  public static boolean isLeafLevelMenu(Menu elem) {
     return elem.m_menu.m_children == null || elem.m_menu.m_children.size() == 0;     
  }
  
  public static void getListPageMenuDiv(SessionManager session, Menu elem, StringBuilder retval, boolean sameLevel, boolean doFirstURLOnly, boolean doPopup) throws Exception {
     //see duplication getListPageMenuDivByMenuItem
     if (elem == null) 
        return;
     User user = session.getUser();
     //ArrayList children = elem.m_menu.m_children;
     ArrayList children = elem.m_children;
     for (int j=0,js=sameLevel ? 1 : children == null ? 0:children.size();j<js;j++) {
         Menu chMenu = sameLevel ? elem : (Menu) children.get(j);
         if (chMenu == null)
            continue;
         if (!user.isPrivAvailable(session, chMenu.m_menu.m_tag))
            continue;
         String name = chMenu.m_labelIfGen != null ? chMenu.m_labelIfGen : chMenu.m_menu.m_name;         
         String url = getMenuURL(session, chMenu, false, true, false, false);     
         if (doFirstURLOnly) {
        	 retval.append(url);
        	 return;
         }
         else {
        	 retval.append("<DIV onmouseover='highlightMenu(this)' onMouseOut='unhighlightMenu(this)' STYLE='font-family:verdana; font-size:70%; color:#000066; font-weight:normal; height:20px; background:#DBDBE2; border:1px solid #000066; padding-left:10px;  cursor:hand; filter:; padding-right:3px; padding-top:2px; padding-bottom:2px'>")
               .append("<SPAN ONCLICK=\"gotoPageLink('")
               .append(url)
               .append("',").append(doPopup)
               .append(")\">")
               .append(name)
               .append("</SPAN></DIV>");
         }
     }
     
     return;
  }
  
  
  public static void getListPageMenuDivByMenuItem(SessionManager session, MenuItem elem, StringBuilder retval, boolean sameLevel, boolean doFirstURLOnly, boolean doPopup) throws Exception {
     User user = session.getUser();
     if (elem == null || !user.isPrivAvailable(session,elem.m_tag)) 
        return;
    
     //ArrayList children = elem.m_menu.m_children;
     ArrayList children = elem.m_children;
     for (int j=0,js=sameLevel ? 1 : children == null ? 0:children.size();j<js;j++) {
         MenuItem chMenu = sameLevel ? elem : (MenuItem) children.get(j);
         if (chMenu == null)
            continue;
         if (!user.isPrivAvailable(session,chMenu.m_tag))
            continue;
         String name = chMenu.m_name;     
         String url = getMenuURLFromMenuItem(session, chMenu, false, true, false, false, user);     
         if (doFirstURLOnly) {
        	 retval.append(url);
        	 return;
         }
         else {
	         retval.append("<DIV onmouseover='highlightMenu(this)' onMouseOut='unhighlightMenu(this)' STYLE='font-family:verdana; font-size:70%; color:#000066; font-weight:normal; height:20px; background:#DBDBE2; border:1px solid #000066; padding-left:10px;  cursor:hand; filter:; padding-right:3px; padding-top:2px; padding-bottom:2px'>")
	               .append("<SPAN ONCLICK=\"gotoPageLink('")
	               .append(url)
	               .append("',").append(doPopup)
	               .append(")\">")
	               .append(name)
	               .append("</SPAN></DIV>");
         }
     }
     
     return;
  }
  public static String replaceAll(String source, String toReplace, String replacement) {
     int idx = source.lastIndexOf( toReplace );
     if ( idx != -1 ) {
       StringBuilder ret = new StringBuilder( source );
       ret.replace( idx, idx+toReplace.length(), replacement );
       while( (idx=source.lastIndexOf(toReplace, idx-1)) != -1 ) {
         ret.replace( idx, idx+toReplace.length(), replacement );
       }
       source = ret.toString();
    }
    return source;
  }
  
  public static boolean helpPrintMenuXML(Menu elem, StringBuilder retval, boolean wizardMode, SessionManager session, boolean useListName, String idParamForTagAppend) throws Exception {
     String tagName = elem.m_menu.m_tag;
     String url = null;
     boolean isHashURL = false;
     if (wizardMode && tagName.startsWith("prj_basic")) {
        isHashURL = true;
        url = "#";
     }
     else
        url = getMenuURL(session, elem, false, false, true, true);     //get at current level
    
     String scriptPart = getMenuScript(elem);
     scriptPart = MyXMLHelper.escapedStr(scriptPart);
     
     String name = elem.m_labelIfGen != null ? elem.m_labelIfGen : useListName ? elem.m_menu.m_nameIfShownInPopup : elem.m_menu.m_name;
     name = MyXMLHelper.escapedStr(name);
     
     if (elem.m_isGenerated && elem.m_idVal != null && elem.m_idVal.length() != 0)
        idParamForTagAppend = elem.m_idVal;
     if (!isHashURL) {
        retval.append("<m t=\"").append(tagName).append(useListName ? "0" : "").append(idParamForTagAppend == null ? "" : idParamForTagAppend).append("\" ");
        if (url != null)
           retval.append(" h=\"").append(url).append("\" ");
        if (scriptPart != null)
           retval.append(" s=\"").append(scriptPart).append("\" ");
        retval.append(" n=\"").append(name).append("\" ");
        retval.append(">");
     }
     return !isHashURL;
  }
  
  public static boolean printLHSMenuAsXML(SessionManager session, Menu elem, StringBuilder retval, boolean wizardMode, String idParamForTagAppend, Menu printListElemAlso) throws Exception {
//     if (elem != null && (elem.m_menu.m_tag.equals("status_1"))) {
//        int dbg=1;
//     }
     if (!isMenuToPrint(elem, wizardMode)) {
         return false;
     }
     if (elem == null)
       return false;     
     boolean isSelected = isMenuSelected(elem);
     
     boolean isLeafLevel = isLeafLevelMenu(elem);

     String pageSpec = elem.m_menu.m_url; 
     boolean gobelow = true;
     
     if (pageSpec != null && !isLeafLevel) {
      //check if any of the childs are not selected ... then don't expand

          gobelow = false;
          //if (!doNOLHSStyle) {
          {
             ArrayList children = elem.m_children;
             for (int i=0,is = children == null ? 0 : children.size(); i < is;i++) {          
                Menu e1 = (Menu)children.get(i);
                if (e1.m_isSelected) {
                   gobelow = true;
                   break;
                }
             }
          }
     }
     if (printListElemAlso != null) {
         helpPrintMenuXML(printListElemAlso, retval, wizardMode, session, true, idParamForTagAppend);
         retval.append("</m>");
     }
     if (elem.m_isGenerated && elem.m_idVal != null && elem.m_idVal.length() != 0)
        idParamForTagAppend = elem.m_idVal;
     boolean printed = helpPrintMenuXML(elem, retval, wizardMode, session, false, idParamForTagAppend);
     printListElemAlso = pageSpec != null ? elem : null;     
     if (gobelow) {        
        Menu childToGoThru = null;
        if (wizardMode) {
           //check if there is node with prj_basic .. if so then just print that
           ArrayList children = elem.m_children;           
           for (int j=0,js=children == null ? 0:children.size();j<js;j++) {
              
              Menu e = (Menu) children.get(j);
              if (e.m_menu.m_tag.startsWith("prj_basic")) {
                 childToGoThru = e;
                 break;
              }
           }
        }
        if (childToGoThru == null) {
           ArrayList children = elem.m_children;           
           for (int j=0,js=children == null ? 0:children.size();j<js;j++) {
               boolean wasPrinted = printLHSMenuAsXML(session, (Menu)children.get(j), retval, wizardMode, idParamForTagAppend, printListElemAlso);
               if (wasPrinted)
                  printListElemAlso = null;               
            }
        }
        else {
            //do print the prj_basic
            printLHSMenuAsXML(session, childToGoThru, retval, wizardMode, idParamForTagAppend, printListElemAlso);            
        }
     }
     if (printed)
        retval.append("</m>");
     undoMenuSelected(elem);
     
     return true;
  }
  public static void printLHSMenu(SessionManager session, Menu elem, int currLevel, StringBuilder retval, boolean wizardMode, boolean doNOLHSStyle) throws Exception {

     if (!isMenuToPrint(elem, wizardMode)) {
         return;
     }
     if (elem == null)
       return;

     
     boolean isSelected = isMenuSelected(elem);
     
     boolean isLeafLevel = isLeafLevelMenu(elem);

     String pageSpec = elem.m_menu.m_url; 
     boolean gobelow = true;
     if (pageSpec != null && !isLeafLevel) {
      //check if any of the childs are not selected ... then don't expand

      gobelow = false;
      if (!doNOLHSStyle) {
         ArrayList children = elem.m_children;
         for (int i=0,is = children == null ? 0 : children.size(); i < is;i++) {          
            Menu e1 = (Menu)children.get(i);
            if (e1.m_isSelected) {
               gobelow = true;
               break;
            }
         }
      }
      if (!gobelow)
         isLeafLevel = true;
     }

     String tagName = elem.m_menu.m_tag;
     String url = null;
     if (wizardMode && tagName.startsWith("prj_basic"))
        url = "#";
     else
        url = getMenuURL(session, elem);     
     String scriptPart = getMenuScript(elem);
     String name = elem.m_labelIfGen != null ? elem.m_labelIfGen : elem.m_menu.m_name;
     if (doNOLHSStyle) {
        if (!wizardMode) {
           retval.append("<td width='10'>&nbsp;</td>");
           retval.append("<td nowrap class='tn' ")               
               .append(" >")
               .append("<a target='_top' href=\""+url+"\"" + (scriptPart != null ? " onclick='"+scriptPart+"' ": "") + " >")
               .append(name)
               .append("</a>")
               .append("</td>");
   //       retval.append("<td width='20'>&nbsp;</td>");
        }
     }
     else  {//i.e. printinng LHS
         retval.append("<tr>");
    
         boolean toPrintLeafLevelImage = (currLevel == 0) && !isSelected;
         boolean toPrintSelectedBG = isSelected && isLeafLevel;
    
         int colWidth = 12;
    //     int totWidth = 200;
         String strColWidth = Integer.toString(colWidth);
         int currentWidthTaken = 0;
    
         for (int i=0;i<currLevel;i++) {
            retval.append("<td width=\""+strColWidth+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":"")+">&nbsp;</td>");
    //        currentWidthTaken += colWidth;
         }
         //White image
         if (isLeafLevel && isSelected) {
            retval.append("<td  width=\""+strColWidth+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":"")+"><img src=\""+selLeafLevelImage+"\"></td>");
         }
         else /*end of addition - sorry for the comment in else if*/ if (isLeafLevel) {
            if (toPrintLeafLevelImage)
               retval.append("<td  width=\""+strColWidth+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":"")+"><img src=\""+leafLevelImage+"\"></td>");
            else
               retval.append("<td width=\""+strColWidth+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":"")+">&nbsp;</td>");
         }
         else {
            if (isSelected)
               retval.append("<td width=\""+strColWidth+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":"")+"><img src=\""+expandedImage+"\"></td>");
            else
               retval.append("<td width=\""+strColWidth+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":"")+"><img src=\""+unexpandedImage+"\"></td>");
         }
    //     currentWidthTaken += colWidth;
         String styleForLeafLevel = (isSelected && isLeafLevel)?"tmNavL5Selected":"tmNavL"+Integer.toString(currLevel+3)+(isSelected?"Selected":"Deselected");
    //     retval.append("<td width=\""+Integer.toString(totWidth-currentWidthTaken)+"\""+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":""))
         String nowrapString = isLeafLevel?" nowrap ":"";
         retval.append("<td "+nowrapString+(toPrintSelectedBG?" class=\"tmNavSelectedbg\" ":""))
               .append(" colspan=\""+(Integer.toString(3-currLevel))+"\" ")
               .append(" >")
               .append("<a target='_top' href=\""+url+"\" class=\""+styleForLeafLevel+"\"" + (scriptPart != null ? " onclick='"+scriptPart+"' ": "") + " >")
               .append(name)
               .append("</a>")
               .append("</td>");
         retval.append("</tr>");
     }
     if (isSelected && !isLeafLevel && gobelow) {        
        Menu childToGoThru = null;
        if (wizardMode) {
           //check if there is node with prj_basic .. if so then just print that
           ArrayList children = elem.m_children;           
           for (int j=0,js=children == null ? 0:children.size();j<js;j++) {
              
              Menu e = (Menu) children.get(j);
              if (e.m_menu.m_tag.startsWith("prj_basic")) {
                 childToGoThru = e;
                 break;
              }
           }
        }
        if (childToGoThru == null) {
           ArrayList children = elem.m_children;           
           for (int j=0,js=children == null ? 0:children.size();j<js;j++) {
               printLHSMenu(session, (Menu)children.get(j), currLevel+1,retval, wizardMode,doNOLHSStyle);
            }
        }
        else {
            printLHSMenu(session, childToGoThru, currLevel+1, retval, wizardMode,doNOLHSStyle);
        }
     }
     undoMenuSelected(elem);
     return;
  }

  private static String getButtonString(SessionManager session, MenuItem.Button button) throws Exception {
     String targetPage = button.m_url;
     if (targetPage != null && targetPage.length() != 0) {
        //String url = getMenuURL(session, button); to
        String url =""; //TODO
        String name = Misc.getParamAsString(button.m_name,"");
        return ("<input type=\"button\" name=\"top_level_button\" value=\""+name+"\" onClick=\"MM_goToURL('parent','"+url+"');return document.MM_returnValue\" class=\"Input_buttons\">&nbsp;&nbsp;");
     }
     else {
        String script = button.m_script; 
        String name = Misc.getParamAsString(button.m_name,"");
        return ("<input type=\"button\" name=\"top_level_button\" value=\""+name+"\" onClick=\""+script+"\" class=\"Input_buttons\">&nbsp;&nbsp;");
     }
  }

  public static String getMenuScript(Menu selElem) {
      String scriptSpec = selElem.m_menu.m_script;
      if (scriptSpec != null)
         return "javascript:"+scriptSpec+"";
      else
         return null;
  }
  public static String getMenuURL(SessionManager session, Menu selElem) throws Exception {
     return getMenuURL(session, selElem, false, false, false, false);
  }
  public static String getMenuURL(SessionManager session, Menu selElem, boolean putInSession) throws Exception {  
     return getMenuURL(session,selElem,putInSession,false, false, false);
  }
  
  public static String getMenuURL(SessionManager session, Menu selElem, boolean putInSession, boolean gettingForListMenu, boolean getAtCurrentLevel) throws Exception {
     return getMenuURL(session, selElem, putInSession, gettingForListMenu,getAtCurrentLevel, false);
  }
  public static String getMenuURL(SessionManager session, Menu selElem, boolean putInSession, boolean gettingForListMenu, boolean getAtCurrentLevel, boolean escapeAnd) throws Exception {
     //see duplication getMenuURLMenuItem
     //Will return the URL
     if (selElem == null)
        return null;
     String url = null;
     String pageSpec = selElem.m_overrideToHome ? Misc.G_APP_1_BASE+"home.jsp" : selElem.m_menu.m_url;
     String altPageSpec = selElem.m_menu.m_altPage;
     if (pageSpec == null && !getAtCurrentLevel) { //go down to get the URL - from the child Nodes
         //get the first non-button & non-param child
         ArrayList chList = selElem.m_children;
         Menu chNode = null;
         for (int i=0,is = chList == null ? 0 : chList.size(); i<is;i++) { 
            chNode = (Menu)chList.get(i);
            if (isMenuToPrint(chNode, false))
               break;
         }
         String downURL  = getMenuURL(session, chNode, putInSession, gettingForListMenu, getAtCurrentLevel, escapeAnd);
         if (downURL != null)
            url = downURL;
         else if (selElem.m_menu.m_altPage != null)
          //get it from altPage - otherwise make the url as home.jsp
           pageSpec = altPageSpec;
        else
          url =  Misc.G_APP_1_BASE+"home.jsp";
     }
     if (url != null)
        return url;
     if (pageSpec == null)
        pageSpec = altPageSpec;
     if (pageSpec == null)
        return null;
     // pagespec is either non-null or pagespec == null and there us a valid url - obviosuly we have returned that
     ArrayList paramList = selElem.m_menu.m_params;
     
     StringBuilder param = new StringBuilder();
     param.append(pageSpec);
     boolean firstParam = true;
     boolean foundPageContextParam = false;//will be checked for gettingForListMenu
     for (int i=0,count = paramList == null ? 0 : paramList.size(); i< count;i++) {
        MenuItem.Param paramElement = (MenuItem.Param) paramList.get(i);
        String paramName = paramElement.m_name;
        String paramVal = null;
        int foundAt = paramElement.m_foundAt;
        if (gettingForListMenu) {
            if (0 == foundAt) {
               paramVal = paramElement.m_value;
            }
            if ("page_context".equals(paramName))
               foundPageContextParam = true;
        }
        else {
            if (Misc.isUndef(foundAt)) {
               paramVal = session.getParameter(paramName);
            }
            else if (0 == foundAt) {
               paramVal = paramElement.m_value;
            }
            else {
               for (Menu tn = selElem.m_parent;  tn != null; tn = tn.m_parent) {
                  paramVal = tn.m_idVal;
                  if (paramVal != null && (paramName.equals(tn.m_menu.m_idParam)))
                     break;
               }
            }
            if (paramVal == null)
               paramVal = Misc.getParamAsString(session.getParameter(paramName),null);
        }
        
        
        
        if (!putInSession && paramVal != null) {
            if (firstParam)
               param.append("?");
            else {
               if (escapeAnd)
                  param.append("&amp;");
               else
                  param.append("&");
            }
            if (escapeAnd)
                paramVal = MyXMLHelper.escapedStr(paramVal);
            param.append(paramName).append("=").append(paramVal);
            firstParam = false;
        }
        else if (putInSession && paramVal != null) {
            session.setAttribute(paramName, paramVal, false);
        }
     }
     
//     session.setAttribute("page_context", selElem.getTagName(),false);
     if (gettingForListMenu && !foundPageContextParam) {
        if (!firstParam) {
           if (escapeAnd)
              param.append("&amp;");
          else
              param.append("&");
        }
        else
           param.append("?");
        param.append("page_context=").append(selElem.m_menu.m_tag);
        
     }
        
     url = param.toString();
     return url;
  }
  
  public static String getMenuURLFromMenuItem(SessionManager session, MenuItem selElem, boolean putInSession, boolean gettingForListMenu, boolean getAtCurrentLevel, boolean escapeAnd, User user) throws Exception {
     //see duplication relative to getMenuURL
     //Will return the URL
     if (selElem == null)
        return null;
     String url = null;
     String pageSpec = selElem.m_url;
     String altPageSpec = selElem.m_altPage;
     
     if (pageSpec == null && !getAtCurrentLevel) { //go down to get the URL - from the child Nodes
         //get the first non-button & non-param child
         ArrayList chList = selElem.m_children;
         MenuItem chNode = null;
         for (int i=0,is = chList == null ? 0 : chList.size(); i<is;i++) { 
            chNode = (MenuItem)chList.get(i);
            if (user.isPrivAvailable(session, chNode.m_tag))
               break;
         }
         String downURL  = getMenuURLFromMenuItem(session, chNode, putInSession, gettingForListMenu, getAtCurrentLevel, escapeAnd, user);
         if (downURL != null)
            url = downURL;
         else if (selElem.m_altPage != null)
          //get it from altPage - otherwise make the url as home.jsp
           pageSpec = altPageSpec;
        else
          url =  Misc.G_APP_1_BASE+"home.jsp";
     }
     if (url != null)
        return url;
     if (pageSpec == null)
        pageSpec = altPageSpec;
     if (pageSpec == null)
        return null;
     // pagespec is either non-null or pagespec == null and there us a valid url - obviosuly we have returned that
     ArrayList paramList = selElem.m_params;
     
     StringBuilder param = new StringBuilder();
     param.append(pageSpec);
     boolean firstParam = true;
     boolean foundPageContextParam = false;//will be checked for gettingForListMenu
     for (int i=0,count = paramList == null ? 0 : paramList.size(); i< count;i++) {
        MenuItem.Param paramElement = (MenuItem.Param) paramList.get(i);
        String paramName = paramElement.m_name;
        String paramVal = null;
        int foundAt = paramElement.m_foundAt;
        if (gettingForListMenu) {
            if (0 == foundAt) {
               paramVal = paramElement.m_value;
            }
            if ("page_context".equals(paramName))
               foundPageContextParam = true;
        }
        else {
            if (Misc.isUndef(foundAt)) {
               paramVal = session.getParameter(paramName);
            }
            else if (0 == foundAt) {
               paramVal = paramElement.m_value;
            }
            else {
               //cant locate from menuItem
            }
            if (paramVal == null)
               paramVal = Misc.getParamAsString(session.getParameter(paramName),null);
        }
        
        
        
        if (!putInSession && paramVal != null) {
            if (firstParam)
               param.append("?");
            else {
               if (escapeAnd)
                  param.append("&amp;");
               else
                  param.append("&");
            }
            if (escapeAnd)
                paramVal = MyXMLHelper.escapedStr(paramVal);
            param.append(paramName).append("=").append(paramVal);
            firstParam = false;
        }
        else if (putInSession && paramVal != null) {
            session.setAttribute(paramName, paramVal, false);
        }
     }
     
//     session.setAttribute("page_context", selElem.getTagName(),false);
     if (gettingForListMenu && !foundPageContextParam) {
        if (!firstParam) {
           if (escapeAnd)
              param.append("&amp;");
          else
              param.append("&");
        }
        else
           param.append("?");
        param.append("page_context=").append(selElem.m_tag);
        
     }
        
     url = param.toString();
     return url;
  }

  
}//end of class
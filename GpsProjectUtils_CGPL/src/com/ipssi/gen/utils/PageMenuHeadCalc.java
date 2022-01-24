// Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
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
/*
An object to be kept in request ... i.e. no caching done from request to request (need to figure out ways
to improve it ...
Will be created as first step in prepareMenuForPrint
menu will be able to call it check if a particularMenuTag is available to print ... if not, the system will automatically move all the
lower children to not print......

*/
   public class PageMenuHeadCalc {
       public HashMap m_criteriaInfo = null;

       
       
      // public ArrayList m_rejectStates = null;
     //  public ArrayList m_nextStates = null;
//       public WkspStepNew.StateMap m_nextPrimState = null;
       
       public Cache m_cache = null;
       public Logger m_log = null; 
       private HashMap m_menuTagLookup = new HashMap(300,0.75f);
       public void clearMenuLookupTags() { 
          m_menuTagLookup.clear(); 
       }
       private Integer m_oneInteger = new Integer(1);
       private Integer m_zeroInteger = new Integer(0);
       public int m_projectId = Misc.getUndefInt();
       public int m_alternativeId = Misc.getUndefInt();
       public int m_workspaceId = Misc.getUndefInt();
       public String m_prjCmpLabel = null;
       private SessionManager m_session = null;
       HashMap m_sectionInfo = new HashMap(15); //key = section name, value = MiscInner.PairBool (first is read/write, second is completeness)


       public boolean pendingApproval() {
          return false;//m_currApproval == WkspStepNew.CURR_LAST_APPROVAL || m_currApproval == WkspStepNew.CURR_PENDING_APPROVAL;
       }
       public boolean isMenuAvailableLookup(String tag) { //will just do lookup
          Integer seen = (Integer) m_menuTagLookup.get(tag);
          if (seen != null) {
             return (seen.intValue() == 1);
          }
          else
             return true;
       }
       public boolean isMenuAvailable(MenuItem menuItem, boolean justLookup, Connection dbConn, SessionManager session, int projectId, int workspaceId, int alternativeId) throws Exception {//will set children to unavailable if menuItem is not available
          boolean doLogging = m_log.getLoggingLevel() >= 15;
          boolean lookInAccCheck = false;
          String tagName = menuItem.m_accCheck;
          if (tagName == null || tagName.length() == 0)
             tagName = menuItem.m_tag;
          else {
             lookInAccCheck = true;
             int dbg = 1;
          }
          Integer seen = (Integer) m_menuTagLookup.get(tagName);
          if (seen != null) {
             return (seen.intValue() == 1);
          }
          else if (justLookup)
             return true;
          boolean retval = false;
          if (tagName.equals("projects")) { //return based upon ism priv
             return true; //only visible ones are available
             //String ism = menuItem.getAttribute("_ism");
             //retval = "c".equals(ism) || "m".equals(ism);
          }
          else {//take an optimistic view ....if at the level you do not find that it is available, then it is available
             MenuItem menuInfo = menuItem;
             if (lookInAccCheck && menuInfo != null) {
                menuInfo = MenuItem.getMenuInfo(menuItem.m_accCheck);
             }
             ArrayList conditions = menuInfo == null ? null : menuInfo.m_visibilityRules;//(ArrayList)m_cache.m_menuVisibilityRules.get(tagName);
             if (conditions == null || conditions.size() == 0) {
                retval = true;
             }
             else {
             
                //being CapEx comment out
                retval = true;
                /*
                if (doLogging) {
                   m_log.log("Checking menu avail for:"+tagName);
                   m_wkspStepMgr.helpDbgPrintCritInfo(m_criteriaInfo, m_log);
                }
                for (int i=0,is = conditions.size();i<is;i++) {
                   m_log.log("Checking condn no:"+Integer.toString(i));
                   ArrayList specificCondition = (ArrayList) conditions.get(i);
                   if (m_wkspStepMgr.helperMatchesCondition(m_criteriaInfo, specificCondition, m_log, dbConn, session, projectId, workspaceId, alternativeId)) {
                      retval = true;
                      break;
                   }
                }//for each condition (which itself is and of dimval
                */
             }//if there were some conditions
          }//need to evaluate
          if (retval == false) {
             setMenuUnavailableRecursive(menuItem);
          }
          else {
             m_menuTagLookup.put(tagName, m_oneInteger);
          }
          return retval;

       }//end of func
       public void setMenuAvailable(String tagName) {
          m_menuTagLookup.put(tagName, m_oneInteger);
       }
       public void setMenuUnavailableRecursive(MenuItem menuItem) { //will happen if the caller realizes that no children are available
          String tagName = menuItem.m_tag;
          m_menuTagLookup.put(tagName, m_zeroInteger);
          ArrayList children = menuItem.m_children;
          for (int n1=0,n1s=children == null ? 0 : children.size();n1<n1s;n1++) {
              MenuItem chItem = (MenuItem) children.get(n1);
              setMenuUnavailableRecursive(chItem);
          }
       }

       public String getBestPgContextForProjectDetail(Connection dbConn, User user, String pgContext, SessionManager session, int projectId, int workspaceId, int alternativeId) throws Exception {
          String retval = session.getParameter("_page_detail");
          if (retval != null && retval.length() != 0)
             return retval;
          if (pgContext == null)
              pgContext = "prj_basic";
          String topTag = session.getParameter("_top_menu");
          if (topTag == null || topTag.length() == 0)
             return null;
          if ("0".equals(session.getParameter("_top_auto")))
             return null;
          retval = null;
          retval = getGotoElem(dbConn, user, pgContext, null, null, true, session, projectId, workspaceId, alternativeId);
          String prjBasicRetval = null;
          if (retval == null) { //just get the first available & visible page below this
             UserGen.Menu topElem = UserGen.menuGetElem(user,session, session.getMenu(), topTag, null, null);
             ArrayList children = topElem.m_children;
             for (int i=0,is= children == null ? 0:children.size();i<is;i++) {                
                UserGen.Menu e = (UserGen.Menu)children.get(i);
                String tagName = e.m_menu.m_tag;
                if (tagName.startsWith("_"))
                   continue;
                if (isMenuAvailable(e.m_menu, true, dbConn, session, projectId, workspaceId, alternativeId)) {
                   if (tagName.startsWith("prj_basic")) {
                       prjBasicRetval = retval;
                       continue;
                   }
                   retval = tagName;
                   break;
                }
             }//for each children
          }//not otherwise found
          if (retval == null && prjBasicRetval != null)
             retval = prjBasicRetval;
          return retval;
       }

       public String getGotoElem(Connection dbConn, User user, String pgContext, ArrayList saveList, ArrayList gotoOnlyList, SessionManager session, int projectId, int workspaceId, int alternativeId) throws Exception { //may return null if the first goto is same page as pgContext ..
          return getGotoElem(dbConn, user, pgContext, saveList, gotoOnlyList, false, session, projectId, workspaceId, alternativeId);
       }
       public String getGotoElem(Connection dbConn, User user, String pgContext, ArrayList saveList, ArrayList gotoOnlyList, boolean justGetFirstGoto, SessionManager session, int projectId, int workspaceId, int alternativeId) throws Exception { //may return null if the first goto is same page as pgContext ..
       //HACK ... to handle values determined by top_context (for env creation, env => 2 even though prj not created
          if ("prj_basic_env".equals(pgContext)) {
             Integer d233 = new Integer(233);
             Integer one = new Integer(1);
             if (m_criteriaInfo != null)
                m_criteriaInfo.put(d233, one);
             
          }
          else if ("prj_basic_cat6".equals(pgContext)) {
             Integer d233 = new Integer(233);
             Integer three = new Integer(3);
             if (m_criteriaInfo != null)
                 m_criteriaInfo.put(d233, three);
             
          }
          else if ("prj_basic_cat61".equals(pgContext)) {
             Integer d233 = new Integer(233);
             Integer four = new Integer(4);
             if (m_criteriaInfo != null)
                 m_criteriaInfo.put(d233,four);
             
          }
          ArrayList nextPossibles = MenuItem.getMenuInfo(pgContext).m_actionRules;// //m_cache.m_menuActionRules.get(pgContext);
          boolean doLogging = m_log.getLoggingLevel() >= 15;
          if (doLogging) {
             m_log.log("Checking next for:"+pgContext);
          }
          ArrayList incompletes = new ArrayList();
          //m_wkspStepMgr.getInfoComplete(dbConn, m_cache, m_projectId, m_workspaceId, m_alternativeId, m_currStateInfo, null, null,m_log, incompletes, session);
          boolean treatSaveAsComplete = incompletes.size() == 0 || incompletes.size() == 1 && ((String)incompletes.get(0)).equals(pgContext);
          boolean treatGotoAsComplete = incompletes.size() == 0;
          String retval = null;
          String firstAvailable = null;
          for (int i=0,is = nextPossibles == null ? 0 : nextPossibles.size();i<is;i++) {
             WkspStepNew.MenuNext next = (WkspStepNew.MenuNext) nextPossibles.get(i);
             String tag = next.m_tag;
             //check if not already added
             //make sure that condition is matched
             //then goto other checks ...
             boolean canAdd = isMenuAvailableLookup(tag);
             if (doLogging) {
                m_log.log("No add because menu unavail:"+tag);
             }

             if (!canAdd)
                continue;
             for (int j=0,js = saveList == null ? 0 : saveList.size();j<js;j++) {

                if (((String)saveList.get(j)).equals(tag)) {
                   canAdd = false;
                   break;
                }
             }
             if (!canAdd) {
                if (doLogging) {
                   m_log.log("No add because item already added:"+tag);
                }

                continue;
             }
             if (doLogging) {
                m_log.log("Going to check if condition for visib met");
             }
             if (!Misc.helperMatchesCondition(m_criteriaInfo, next.m_conditions, m_log, dbConn, session, projectId, workspaceId, alternativeId)) {
             //CAEPX_REMOVE replaced WkspStepMgr.helperMatchesCondition 
                canAdd = false;
                if (doLogging) {
                   m_log.log("No add because item's rule is not met:"+tag);
                }

                continue;
             }
             
             if (tag.startsWith(WkspStepNew.MenuNext.g_approveTag)){ //check if can goto approval page
                if (doLogging) {
                   m_log.log("Going to check for pending approval .. then Info Completeion:"+tag);
                }
                if (pendingApproval())
                   continue;
                if (treatSaveAsComplete) {
                   if (saveList != null)
                      saveList.add(tag);
                }
                if (treatGotoAsComplete) {
                   if (gotoOnlyList != null)
                      gotoOnlyList.add(tag);
//                   if (justGetFirstGoto)
//                      return tag;

                }
                continue;
             }
             else if (tag.equals(WkspStepNew.MenuNext.g_selfTag) || tag.equals(pgContext)) {
                //nothing ... canAdd = true
                canAdd = false;
             }
             else { //make sure that page is not filled up
             //check if the section exists in incomplete ... only then goto to that page
                if (firstAvailable == null)
                    firstAvailable = tag;
                for (int j=0,js=incompletes.size();j<js;j++) {
                   if (((String)incompletes.get(j)).equals(tag)) {
                      if (justGetFirstGoto)
                         return tag;

                      saveList.add(tag);
                      gotoOnlyList.add(tag);
                      break;
                   }
                }
                if (doLogging) {
                   m_log.log("Going to check if data filled up:"+tag);
                }

             } //if doing tag that is neither approval nor self
          }//for each next possible states
          
          if (saveList != null) {             
             if (saveList.size() == 0 && firstAvailable != null)
                saveList.add(firstAvailable);
             saveList.add(pgContext);
          }
          if (gotoOnlyList != null) {
             if (gotoOnlyList.size() == 0 && firstAvailable != null)
                gotoOnlyList.add(firstAvailable);
             gotoOnlyList.add(pgContext);
          }

          return null;

       }//end pffinct

       public static void printHiddenVarsForGoRefresh(JspWriter out) throws IOException {
          out.print("<input type='hidden' name='__next'>");
          out.print("<input type='hidden' name='__save'>");
       }
       public static void printNoPrivMessage(JspWriter out) throws IOException {
          out.print("<img src='"+Misc.G_IMAGES_BASE+"warn_1.gif' height='16px' width='16px'/>&nbsp;&nbsp;<span class='tmTip'>You do not have Change Rights</span>");        
       }
       

       public void setMenuUnavailable(MenuItem menuItem) { //will happen if the caller realizes that no children are available
          m_menuTagLookup.put(menuItem.m_tag, m_zeroInteger);
       }
       
      
      public static void discard(SessionManager session) {
         session.removeAttribute("_menu_head_calc");
      }
      public static PageMenuHeadCalc getPageMenuHeadCalcQuick(SessionManager session, Connection dbConn, Logger log, User user) {//this could return null
         return (PageMenuHeadCalc) session.getAttributeObj("_menu_head_calc");
      }
      public static PageMenuHeadCalc getPageMenuHeadCalc(SessionManager session, Connection dbConn, Cache cache, Logger log, User user) throws Exception {
         return getPageMenuHeadCalc((int) session.getProjectId(), (int) session.getWorkspaceId(), (int) session.getAlternativeId(), session, user, cache, log, dbConn);
      }
      public static PageMenuHeadCalc getPageMenuHeadCalc(int projectId, int workspaceId, int alternativeId, SessionManager session, User user, Cache cache, Logger log, Connection dbConn) throws Exception {
         PageMenuHeadCalc pageMenuHeadCalc = (PageMenuHeadCalc) session.getAttributeObj("_menu_head_calc");
            if (pageMenuHeadCalc == null) {
               pageMenuHeadCalc = new PageMenuHeadCalc();
               
               pageMenuHeadCalc.loadInfoForHeaderMenuButton(projectId, workspaceId, alternativeId, session, user, cache, log, dbConn);
              //CapEx  if (!Misc.isUndef(projectId))
               //CapEx    pageMenuHeadCalc.m_prjCmpLabel = WorkspaceMeta.getPrjCmpLabel(dbConn, session);
               
               session.setAttributeObj("_menu_head_calc", pageMenuHeadCalc);               
               pageMenuHeadCalc.m_session = session;
            }
            
            return pageMenuHeadCalc;
      }
      public static void makePageHeadDirty(SessionManager session) {
         session.setAttributeObj("_menu_head_calc", null);
      }

       public void loadInfoForHeaderMenuButton(int projectId, int workspaceId, int alternativeId, SessionManager session, User user, Cache cache, Logger log, Connection dbConn) throws Exception {//PageHeader is currently unused
         try {
           //load info needed for deciding what menu label to be shown, the header info, the next steps to be shown on
           //CurrStateInfo
           //NextStates
           //RejectStates
           //By grouping the states
           //Full linear map going forward
           //Section Info ... read/completion ...           
           //info needed to evaluate the various conditions in menu, workspaces etc. etc.
           m_cache = cache;
           m_projectId = projectId;
           m_workspaceId = workspaceId;
           m_alternativeId = alternativeId;
           this.m_log = log;
           boolean isUndefPrj = Misc.isUndef(projectId);
           //m_wkspStepMgr = com.ipssi.gen.utils.WkspStepMgr.getWkspStepMgr(null, cache, dbConn);
           if (isUndefPrj) {
             //CapEx m_currStateInfo = m_wkspStepMgr.getPlaceHolderForNonOrNewPrj();
             //CapEx m_criteriaInfo = m_wkspStepMgr.getPlaceHolderForNonOrNewPrj();//m_currStateInfo.m_criteriaInfo;
           }
           else {
              //CapEx m_criteriaInfo = Misc.getPjAttribInfo(cache.m_wkpjInfo, cache.m_wktimeReq, dbConn, cache, cache.m_workflowValGetter, projectId, workspaceId, alternativeId, true);           
              //CapEx m_currStateInfo = m_wkspStepMgr.getCurrentStateMap(dbConn, (int)projectId, (int)workspaceId, alternativeId, cache, log, user);
              //CapEx m_criteriaInfo = m_wkspStepMgr.getPlaceHolderForNonOrNewPrj();//m_currStateInfo.m_criteriaInfo;
           
//           m_rejectStates = new ArrayList();
//           m_nextStates = m_wkspStepMgr.getNextStateMap(dbConn, (int)projectId, (int)workspaceId, (int)alternativeId, cache, log, user, m_currStateInfo, m_rejectStates, false);
        
              //CapEx m_currApproval = m_wkspStepMgr.getCurrApprovalStatus(m_currStateInfo.m_current);

               HashMap visited = new HashMap(30,0.75f);
    //           m_nextPrimState = m_wkspStepMgr.getNextStateMapSpecial(dbConn, m_criteriaInfo, cache, log, user, m_currStateInfo.m_current.m_stateInfo, visited, session, projectId, workspaceId, alternativeId);
    
               PreparedStatement ps = dbConn.prepareStatement(Queries.GET_INFO_COMPLETE);
               ps.setInt(1, projectId);
               ps.setString(2, null);
               ps.setString(3, null);
               ps.setInt(4, Misc.G_FOR_PROJECT); //rajeev 021908
               ResultSet rset = ps.executeQuery();
               while (rset.next()) {
                  String cntxt = rset.getString(2);
                  boolean info = 1 == rset.getInt(3);
                  boolean read = 1 == rset.getInt(4);
                  m_sectionInfo.put(cntxt, new MiscInner.PairBool(read, info));
               }
               rset.close();
               ps.close();
              
           }
           
           if (m_criteriaInfo == null)
              m_criteriaInfo = new HashMap();
           Integer d247 = new Integer(247); //prj comp status
           int prjComp = Misc.getParamAsInt(session.getParameter("_clbsp_comp"),0);
           if (prjComp < 0 || prjComp >= 100)
              prjComp = 0; 
            if (m_criteriaInfo != null) 
               m_criteriaInfo.put(d247, new Integer(prjComp));           
         }
         catch (Exception e) {
           e.printStackTrace();
           throw e;
         }
    }
    
    public boolean isReadOnly(String pgContext) {
       MiscInner.PairBool record = (MiscInner.PairBool) m_sectionInfo.get(pgContext);
       if (record != null && record.first)
          return true;
       return false;
    }
	public int getPropertyInt(int dimId) { //081808 changes
		if (m_criteriaInfo == null)
			return Misc.getUndefInt();
		Integer over = null;
		try {
			over = (Integer)m_criteriaInfo.get(new Integer(dimId));
		}
		catch (Exception e) {
			//yup dont throw
		}
		if (over != null)
			return over.intValue();
		return Misc.getUndefInt();
	}
	public java.sql.Date getPropertyDate(int dimId) {//081808 changes
		if (m_criteriaInfo == null)
			return null;
		try {
			java.sql.Date over = (java.sql.Date)m_criteriaInfo.get(new Integer(dimId));
			return over;
		}
		catch (Exception e) {
			//yup dont throw
			return null;
		}
	}
    public boolean isEditOverride() {
        return getPropertyInt(245) == 1;        
    }
    
    public boolean isShowAllMenu() {
        return getPropertyInt(248) == 1;
    }
    
    public void setEditOverride(boolean enable) {
       if (m_criteriaInfo != null)
          m_criteriaInfo.put(new Integer(245), new Integer(enable?1:0));       
    }
    
    public void setMenuAll(boolean enable) {
       if (m_criteriaInfo != null)
          m_criteriaInfo.put(new Integer(248), new Integer(enable?1:0));       
    }

	   public void setSectionReadWrite(String pgContext, boolean isRead) {//hack to make pur project overview read as false rajeev 072108
		   MiscInner.PairBool record = (MiscInner.PairBool)m_sectionInfo.get(pgContext);
		   if (record != null)
			   record.first = isRead;
		   else {
			   record = new MiscInner.PairBool(isRead, false);
			   m_sectionInfo.put(pgContext, record);
		   }

	   }
public void printSaveButton(JspWriter out, ArrayList saveList, ArrayList gotoOnlyList, boolean nextOnly, SessionManager session, boolean wizardMode, String addnlScriptFn, StringBuilder addnlMenuChoices) throws Exception {
           printSaveButton(out, saveList, gotoOnlyList, nextOnly, session, wizardMode, addnlScriptFn, addnlMenuChoices, false);
       }
       public void printSaveButton(JspWriter out, ArrayList saveList, ArrayList gotoOnlyList, boolean nextOnly, SessionManager session, boolean wizardMode, String addnlScriptFn, StringBuilder addnlMenuChoices, boolean printSecondTime) throws Exception {
          //print the menu holders for goto and saveAndgoto
          //printSecondTime - if the buttons are being printed second time on same page .. will not print the hidden/div etc.
          boolean viewingVer = m_prjCmpLabel != null;
          boolean doingSpecial = isEditOverride() || isShowAllMenu();
          
          String specialButtonName = null;
          StringBuilder specialButton = null;
          if (viewingVer) {
             specialButtonName = "Compare Options";
          }
          else if (doingSpecial) {
             specialButtonName = "Special Options";
          }
          if (specialButtonName != null) {
             
             specialButton = new StringBuilder();
             specialButton.append("<input type='button' name='special_button' class='Input_buttons' value='").append(specialButtonName).append("' onclick=\"").append("MM_goToURL('parent', '").append(Misc.G_APP_1_BASE).append("project_collaboration_work.jsp?project_id=").append(m_session.getProjectId()).append("&workspace_id=").append(m_session.getWorkspaceId()).append("')\"/>&nbsp;");             
          }
          if (specialButton != null) {
              out.print(specialButton);
          }
          if (!printSecondTime) {
              out.print("<input type='hidden' name='__next'>");
              out.print("<input type='hidden' name='__save'>");
          }

          String pgContext = (String)saveList.get(saveList.size()-1);
          if (!isEditOverride() && isReadOnly(pgContext)) {             
             session.setAttribute("_priv_msg","Project is under lockdown pending review", false);
             return;
          }
          String onclickfn ="javascript:" + (addnlScriptFn != null ? addnlScriptFn+";" : "") + "hideButtonBeforeAction(document.forms[0].save_info, document.forms[0].goto_info, document.forms[0].top_level_button2, null);";//AFTER_MERGE
          onclickfn +="_saveGoNext(\"";
          if (!printSecondTime) {
              
    
              
              
              out.println("<iframe scrolling='no' frameborder='0' style='position:absolute; top:0px;left:0px;display:none;zIndex:99' id='AllMenuHolderFrame' src='blank.html'></iframe>");
              out.println("<DIV style='position:absolute;display:hidden;zIndex:100' id='AllMenuHolder'>");
              if (addnlMenuChoices != null && addnlMenuChoices.length() > 0)
                 out.println(addnlMenuChoices);
              //print save and goto ...
              out.println("    <DIV ID='save_and_goto' STYLE='display:none;' >");
              out.println("      <div class='menuholder'>");
              for (int i=0,is=saveList.size();i<is;i++) {
                  String tag = (String)saveList.get(i);//menuElem.getTagName();
                  String friendlyName = m_cache.getMenuNextFriendlyName(tag);
                  if (i != is-1) {
                      friendlyName = "Save &amp; Goto "+friendlyName;
                  }
                  else {
                      friendlyName = "Save Only";
                  }
                  String url = tag;
    
                  out.println("<DIV  onmouseover='highlightMenu(this)' onmouseout='unhighlightMenu(this)' STYLE='font-family:verdana; font-size:9px; color:#000066; font-weight:normal; height:20px; background:#DBDBE2; border:1px solid #000066; padding-left:10px;  cursor:hand; filter:; padding-right:3px; padding-top:2px; padding-bottom:2px'>");
                  out.println("<SPAN ONCLICK='"+onclickfn+url+"\");'>"+friendlyName+"</SPAN>");
                  out.println("</DIV>");
              }//for each target
              out.println("        </div>");
              out.println("      </DIV>");
              //end of save and goto
              //goto only
              out.println("    <DIV ID='only_goto' STYLE='display:none;' >");
              out.println("      <div class='menuholder'>");
              for (int i=0,is=gotoOnlyList.size();i<is;i++) {
                  String tag = (String)gotoOnlyList.get(i);//menuElem.getTagName();
                  String friendlyName = m_cache.getMenuNextFriendlyName(tag);
                  friendlyName = "Goto "+friendlyName;
                  if (i == is-1)
                     friendlyName = "Refresh Only";
                  String url = tag;
    
                  out.println("<DIV  onmouseover='highlightMenu(this)' onmouseout='unhighlightMenu(this)' STYLE='font-family:verdana; font-size:9px; color:#000066; font-weight:normal; height:20px; background:#DBDBE2; border:1px solid #000066; padding-left:10px;  cursor:hand; filter:; padding-right:3px; padding-top:2px; padding-bottom:2px'>");
                  out.println("<SPAN ONCLICK='"+"_saveGoNext(\""+url+"\",0)'>"+friendlyName+"</SPAN>");
                  out.println("</DIV>");
              }//for each target
              out.println("        </div>");
              out.println("      </DIV>");
    
              out.println("</DIV>");
          }
          

//          out.print("<input type='button' name='top_level_button2' value='Save Changes &gt;&gt;' class='Input_buttons' onMouseout=\"closeDropDown('manage_options')\"  onMouseOver=\"openDropDown('manage_options', 200)\"/>&nbsp;");
          
          if (!wizardMode) {
//             out.print("<input type='submit' name='discard_info' class='input_buttons' value='Discard Changes' >&nbsp;");
             if (gotoOnlyList.size() > 1) { //as drop down
                out.print("<input type='button' name='goto_info' value='Discard Changes &gt;&gt;' class='Input_buttons' onMouseout=\"closeDropDown('only_goto')\"  onMouseOver=\"openDropDown('only_goto', 200)\"/>&nbsp;");
             }
             else if (gotoOnlyList.size() > 0) {//as button
              String tag = (String)gotoOnlyList.get(0);//menuElem.getTagName();
              String friendlyName = m_cache.getMenuNextFriendlyName(tag);
              friendlyName = "Discard Changes";
              String url = tag;

               out.print("<input type='button' name='goto_info' class='input_buttons' value='"+friendlyName+"' onclick='"+"_saveGoNext(\""+url+"\",0)'>&nbsp;");
             }
          }
          if (saveList.size() == 1) {
//             out.print("<input type='submit' name='save_info' class='input_buttons' value='Save Changes' >&nbsp;");
               out.print("<input type='button' name='save_info' class='input_buttons' value='Save Changes' onclick='"+onclickfn+pgContext+"\");'>&nbsp;");
          }
          else {
             if (nextOnly || wizardMode) {

                String tag = (String)saveList.get(0);//menuElem.getTagName();
                String friendlyName = m_cache.getMenuNextFriendlyName(tag);
                if (!wizardMode)
                    out.print("<input type='button' name='save_info' class='input_buttons' value='Save Changes' onclick='"+onclickfn+pgContext+"\");'>&nbsp;");
                String url = tag;
                out.print("<input type='button' name='save_info' class='input_buttons' value='Next &gt;&gt;"+""+"' onclick='"+onclickfn+url+"\");'>&nbsp;");

//                out.print("<input type='hidden' name='__next' value='"+url+"'>");
//                out.print("&nbsp;");
             }
             else {
                out.print("<input type='button' name='top_level_button2' value='Save Changes &gt;&gt;' class='Input_buttons' onMouseout=\"closeDropDown('save_and_goto')\"  onMouseOver=\"openDropDown('save_and_goto', 200)\"/>&nbsp;");

             }//if doing drop down
          }//if target elem > 1
       }//end of func
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
   }

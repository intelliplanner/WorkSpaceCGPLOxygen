// Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
package com.ipssi.gen.utils;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.shift.ShiftBean;
import com.ipssi.shift.ShiftInformation;


   public class PageHeader {
      
      public ArrayList m_allRows = null;//new ArrayList(); //of ArrayList of DimConfigInfo
      public int m_rhsRowStartIndex = 0; //in the above ArrayList
      //public ArrayList m_rhsRows = null; // of ArrayList of DimConfigInfo
      public boolean m_showCompInfo = true; //will be further controlled by printer
      public boolean m_showWkspChange = true; //will be further controlled by printer
      public boolean m_showViewVer = true; //will be further controlled by printer
      public static ArrayList g_pageHeaderList = new ArrayList();
      public String m_name = null;
      public int m_1stBlockHeaderWidth = 150;
      public int m_2ndBlockHeaderWidth = 80;
      public FrontGetValHelper m_valGetter = null;
      private String m_query = null;
      public int m_printApprovalStatusCode = 0;
        //printApprovalStatusCode == 0 => do noting, 1=> at top with summary line of bubbles
        //  2 => at bottom with summary line of bubbles
        //  3 => at top no summary line
        //  4 => at bottom no summary line

//      private ArrayList m_paramCodeDate = null;
      public int m_currencySpec = 10005;//budget currency at unit level
      public int m_unitSpec = 10002;// the defaults appropriate for the currency in which being viewed
      
      private MiscInner.TimeReq m_queryTimeReq = null;

	  public ArrayList m_prjMultiAttribDims = null; //populated in createEntry ArrayList of DimInfo that map to prj_multi_attrib
public static class MultiAttribData {//TO BE MOVED TO PAGEHEADER
     public java.sql.Date m_createDate = null;
     public int m_createUser = Misc.getUndefInt();
     public String m_userName = null;
     public ArrayList m_classifyVal = null; //of string
     //int m_objectType = 0;
     //int m_id = Misc.getUndefInt();
     //int m_instanceId = Misc.getUndefInt(); //not used anymore
     public Object m_val = null;
     public int m_id = Misc.getUndefInt(); //for extensibility may be 
     
     public String toString() {
        if (m_val != null)
           return m_val.toString();
        else
           return null;
     }
     
     public String getClassifyVal(int index) {
        if (m_classifyVal == null || m_classifyVal.size() <= index || index < 0)
           return null;
        return (String) m_classifyVal.get(index);
     }
     
     
     public int getIntVal() {
        if (m_val == null)
           return Misc.getUndefInt();
        return ((Integer)m_val).intValue();
     }
     
     public double getDoubleVal() {
        if (m_val == null)
           return Misc.getUndefInt();
        return ((Double)m_val).doubleValue();
     }
     
     public java.sql.Date getDateVal() {
        return ((java.sql.Date)m_val);
     }
     
     public String getStringVal() {  
        return (String)m_val;            
     }         
  }      
      
      
      
      
      public static int helperGetVal(HashMap criteriaInfo, int dimId, int defval) {
         Integer v = (Integer)criteriaInfo.get(new Integer(dimId));
         if (v == null)
            return defval;
         int vret = v.intValue();
         if (Misc.isUndef(vret))
            return defval;
         return vret;
      }
      private boolean toPrintStatusBar() {
         return m_printApprovalStatusCode != 0;
      }

      private boolean toPrintSummLine() {
         return m_printApprovalStatusCode == 1 || m_printApprovalStatusCode == 2;
      }

      private int toPrintApprovalWhere() { //-1 at top, 0 do not, 1 at top
         if (m_printApprovalStatusCode == 1 || m_printApprovalStatusCode == 3)
            return -1;
         else if (m_printApprovalStatusCode == 0)
            return 0;
         return  +1;
      }

      private void printSummary(Connection dbConn, int projectId, int workspaceId, int alternativeId, int askedYear, SessionManager session, Cache cache, User user,  StringBuilder addToThis) throws Exception {
         Logger log = (Logger) session.request.getAttribute("_log");
         PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc(projectId, workspaceId, alternativeId, session, user, cache, log, dbConn);
         HashMap criteriaInfo = pageMenuHeadCalc.m_criteriaInfo;
         boolean pending = pageMenuHeadCalc.pendingApproval();
         StringBuilder buf = new StringBuilder();
         boolean complete = true;//pageMenuHeadCalc.m_wkspStepMgr.getInfoComplete(dbConn, cache, projectId, workspaceId, alternativeId, pageMenuHeadCalc.m_currStateInfo, buf, null, log, null, session);
         if (pending) {
             addToThis.append("Approvals are Pending.");
         }
 //   public boolean getInfoComplete(Connection dbConn, Cache cache, int projectId, int workspaceId, int alternativeId, WkspStepNew.CurrStateInfo currStateInfo, StringBuilder missingInfo, String assumeThisDone, Logger log, ArrayList missingSections) throws Exception {
         else if (complete) {
             addToThis.append("Information Complete for approval");
         }
         else {
             addToThis.append(buf).append("<br> Needed for approval");
         }
      }
     
      public void printApprovalStatus(Connection dbConn, int projectId, int workspaceId, int alternativeId, int askedYear, SessionManager session, Cache cache, User user,  StringBuilder addToThis) throws Exception { //returns the final approval step which translates to auth
         //printApprovalStatusCode == 0 => do noting, 1=> at top with summary line of bubbles
         //  2 => at bottom with summary line of bubbles
         //  3 => at top no summary line
         //  4 => at bottom no summary line
         Logger log = (Logger) session.request.getAttribute("_log");
         PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc(projectId, workspaceId, alternativeId, session, user, cache, log, dbConn);
         HashMap criteriaInfo = pageMenuHeadCalc.m_criteriaInfo;

         boolean budApproved = 1 == helperGetVal(criteriaInfo, 5058, 0);
         boolean authorized = 1 == helperGetVal(criteriaInfo, 5061, 0);
         boolean completed = 1 == helperGetVal(criteriaInfo, 5075, 0);
         boolean reviewCompleted = 1 == helperGetVal(criteriaInfo, 5076, 0);
         int imgIndex = 0;
         if (budApproved)
            imgIndex = 1;
         if (authorized)
            imgIndex = 2;
         if (completed)
            imgIndex = 3;
         if (reviewCompleted)
            imgIndex = 4;
         int prop[] = {0,0,0,0}; //currently count of steps found
         int countReq[] = {0,0,0,0};
         DimInfo groupDim = DimInfo.getDimInfo(5077);
// commented 073107 .... being hardcoded instead
//         for (WkspStepNew.StateMap start = pageMenuHeadCalc.m_currStateInfo.m_start; start != null; start = (start.m_acceptNext == null || start.m_acceptNext.size() == 0) ? null : (WkspStepNew.StateMap) start.m_acceptNext.get(0)) {
//            int stateId = start.m_stateInfo.m_stepId;
//            int pg = cache.getParentDimValId(dbConn, groupDim, start.m_stateInfo.m_stepId);
//            if (pg >= 0 && pg < prop.length)
//               prop[pg]++;
//         }
         
         int currState = Misc.getUndefInt();//get_from_user_pref//;pageMenuHeadCalc.m_currStateInfo.m_current.m_stateInfo.m_stepId;
         
         DimInfo groupingDim = DimInfo.getDimInfo(5077);
         ArrayList groupHelper = groupingDim.getRHSValidValsForDim(231);
         

         for (int i=0,is = countReq.length;i<is;i++) {
            countReq[i] = ((DimInfo.RHSVals)groupHelper.get(i)).m_rhsVals.size()+1;
         }
         //AM HACK
         if (Misc.G_MITTAL_DOING) {
            int invClass = pageMenuHeadCalc.getPropertyInt(4035);
            if (invClass == 0) {
               countReq[1] = 5;
            }
            else if (invClass == 1) {
               countReq[1] = 4;               
            }
            else if (invClass == 2) {
               countReq[1] = 3;
            }
            else {
               countReq[1] = 1;
            }
         }
         
         int groupState = cache.getParentDimValId(dbConn, groupDim, currState);
         
         //int count1 = prop[1];
         //commented: 073107
         //for (WkspStepNew.StateMap start = (WkspStepNew.StateMap) pageMenuHeadCalc.m_nextPrimState; start != null; start = (start.m_acceptNext == null || start.m_acceptNext.size() == 0) ? null : (WkspStepNew.StateMap) start.m_acceptNext.get(0)) {
         //   int pg = cache.getParentDimValId(dbConn, groupDim, start.m_stateInfo.m_stepId);
         //   if (pg == 1)
         //      count1++;
         //   else
         //     break;
         //}
         
         //if (groupState == 1)
         //   countReq[1] = count1+1;
         
         if (groupState == 1)
            countReq[1]++;
         
         if (groupState == 0) {
            prop[0] = currState-1+1;
         }
         else if (groupState == 1) {
            prop[0] = countReq[0];
            prop[1] = currState-8+1;
         }
         else if (groupState == 2) {
            prop[0] = countReq[0];
            prop[1] = countReq[1];
            prop[2] = currState-13+1;
         }
         else if (groupState == 3) {
            prop[0] = countReq[0];
            prop[1] = countReq[1];
            prop[2] = countReq[2];
            prop[3] = currState-16+1;
         }
         else if (groupState == 5) {
            if (budApproved)
               prop[0] = countReq[0];
            else if (authorized)
               if (currState == 21)
                  prop[2] = 1;
         }
         
         if (groupState != 0 && groupState != 4 && budApproved) {
             prop[0] = countReq[0];
         }
         if (groupState == 2 || groupState == 3 || groupState == 6) {
             prop[1] = countReq[1];
         }
         if (groupState == 3 || groupState == 6) {
             prop[2] = countReq[2];
         }
         if (groupState == 6)
             prop[3] = countReq[3];

         if (authorized && prop[2] == 0 && countReq[2] != 0) {
            prop[2] = 1;
         }

         //now we know the proportion ...
         addToThis.append("<table border='0' cellspacing='0' cellpadding='0'><tr><td><img src='"+Misc.G_IMAGES_BASE+"transparent_sp.gif' height='1' width='354' /></td><td></td>");

         addToThis.append("</tr><tr><td height='18' nowrap='nowrap' background='"+Misc.G_IMAGES_BASE+"bud"+Integer.toString(imgIndex+1)+".gif' >");
         int width[] = {69,69,69,70};
         int margin[] = {14,14,14,14};
         for (int i=0,is=width.length;i<is;i++) {
            int mainimgWidth = (prop[i]*width[i])/(countReq[i]);
//            mainimgWidth = width[i];
            int transpWidth = width[i]-mainimgWidth;
            addToThis.append("<img src='"+Misc.G_IMAGES_BASE+"status_prog.gif' width='"+Integer.toString(mainimgWidth)+"' height='13px' style='margin-left:15px;filter: alpha(opacity=25);' />");
            if (transpWidth > 0)
                addToThis.append("<img src='"+Misc.G_IMAGES_BASE+"transparent_sp.gif' width='"+Integer.toString(transpWidth)+"' height='13'/>");
         }
         addToThis.append("</td><td>");
         boolean pending = pageMenuHeadCalc.pendingApproval();
         StringBuilder buf = new StringBuilder();
         boolean complete =  false;//pageMenuHeadCalc.m_wkspStepMgr.getInfoComplete(dbConn, cache, projectId, workspaceId, alternativeId, pageMenuHeadCalc.m_currStateInfo, buf, null, log, null,session);
         if (pending) {
             addToThis.append("<img height='18' border='0' src='"+Misc.G_IMAGES_BASE+"am_warn.jpg' title='Approvals are pending'>");
         }
 //   public boolean getInfoComplete(Connection dbConn, Cache cache, int projectId, int workspaceId, int alternativeId, WkspStepNew.CurrStateInfo currStateInfo, StringBuilder missingInfo, String assumeThisDone, Logger log, ArrayList missingSections) throws Exception {
         else if (complete) {
             addToThis.append("<img  height='18' border='0' src='"+Misc.G_IMAGES_BASE+"am_green_go.jpg' title='Information complete for getting approval'>");
         }
         else {
             addToThis.append("<img  height='18' border='0' src='"+Misc.G_IMAGES_BASE+"am_stop.jpg' title='").append(buf).append(" need(s) to be completed for getting approval'>");
         }
         addToThis.append("</td></tr></table>");

      } //end of function
      
      
      public static String helperGetStrValForDate(MiscInner.ContextInfo currencyEtcContext, java.util.Date dt) {
         if (dt == null)
            return null;
         if (currencyEtcContext == null)
            return Misc.getPrintableDateSimple(dt);
         FmtI.Date dtfmt = currencyEtcContext.getDateFormatter();
         return dtfmt.format(dt);
      }

      public static String helperGetDateFormatSpec(MiscInner.ContextInfo currencyEtcContext) {
         if (currencyEtcContext == null) 
            return Misc.G_DEFAULT_DATE_FORMAT;                  
         return currencyEtcContext.getDatePattern();         
      }
      
      public static MiscInner.TripleStrStrStr helperGetStrValForDouble(MiscInner.ContextInfo currencyEtcContext, int adornmentSpec, int currencyIdIfAny, double v, DimInfo dimInfo, int dimConfigLevelUnit) {
         if (Misc.isUndef(v))
            v = 0;
         
         boolean doCurrency = dimInfo != null && dimInfo.m_qtyType == 0;
         MiscInner.UnitInfo dimConfigLevelUnitInfo = !Misc.isUndef(dimConfigLevelUnit) ? Cache.getUnitInfo(dimInfo != null ? dimInfo.m_qtyType : 0, dimConfigLevelUnit) : null;
         
         
         if (dimInfo != null && dimInfo.m_qtyType != 0) {
            double scale = dimConfigLevelUnitInfo == null ? dimInfo.m_scale : dimConfigLevelUnitInfo.m_unit;
            v /= dimInfo.m_scale;
         }
         if (currencyEtcContext == null) {
            return new MiscInner.TripleStrStrStr(null, Misc.m_formatter.format(v), null);
         }
         String valstr = null;
         String prefix = null;
         String suffix = null;
         
         if (doCurrency) {
            if (dimConfigLevelUnitInfo != null) {               
               currencyEtcContext = new MiscInner.ContextInfo(currencyEtcContext.m_locale, dimConfigLevelUnitInfo, currencyEtcContext.m_currencyInfo);
            }
         }
                  
         if (adornmentSpec == 3 && doCurrency) {
            MiscInner.UnitInfo unitInfo = currencyEtcContext.m_unitInfo;
            suffix = unitInfo.m_suffix;
            int currencyId = currencyIdIfAny;
            if (Misc.isUndef(currencyId))
               currencyId = currencyEtcContext.m_currencyInfo.m_id;
            FmtI.Currency fmt = currencyEtcContext.getCurrencyFormatter(currencyId);
            valstr = fmt.format(v, false, true, true);            
         }
         else if (adornmentSpec == 0) {
            FmtI.Number fmt = doCurrency ? currencyEtcContext.getNumberFormatter() : currencyEtcContext.getUnscaledFormatter();
            valstr = fmt.format(v);
         }
         else if (adornmentSpec == 1) {
            if (doCurrency) { //take unit from unitInfo .... else take it from the old approach ...
               MiscInner.UnitInfo unitInfo = currencyEtcContext.m_unitInfo;
               suffix = unitInfo.m_suffix;
            }
            else {
              suffix = dimConfigLevelUnitInfo != null ? dimConfigLevelUnitInfo.m_suffix : dimInfo.m_unitString; //HACK ... todo
            }
            FmtI.Number fmt = doCurrency ? currencyEtcContext.getNumberFormatter() : currencyEtcContext.getUnscaledFormatter();
            valstr = fmt.format(v);
         }
         else { //2 or 3 .. currency is Currency + valStr + unit, others valStr + unit + qty name.. the latter is hacked and becomes just unitString
            if (doCurrency) {
               int currencyId = currencyIdIfAny;
               if (Misc.isUndef(currencyId))
                  currencyId = currencyEtcContext.m_currencyInfo.m_id;   
               MiscInner.CurrencyInfo currencyInfo = Cache.getCurrencyInfo(currencyId);
               prefix = currencyInfo.m_iso3digitCode;
               MiscInner.UnitInfo unitInfo = currencyEtcContext.m_unitInfo;
               suffix = unitInfo.m_suffix;               
            }
            else {
               suffix = dimConfigLevelUnitInfo != null ? dimConfigLevelUnitInfo.m_suffix : dimInfo.m_unitString;
            }
            FmtI.Number fmt = doCurrency ? currencyEtcContext.getNumberFormatter() : currencyEtcContext.getUnscaledFormatter();
            valstr = fmt.format(v);
         }
         return new MiscInner.TripleStrStrStr(prefix,valstr,suffix);        
      }
      
      public static String helperGetStrFromValTriple(MiscInner.TripleStrStrStr val) {
         if (val == null)
            return "";
         String prefix = val.first;
         String valstr = val.second;
         String suffix = val.third;
         String retval = "";
         if (prefix != null && prefix.length() != 0) {
            retval += prefix +" ";
         }
         retval += valstr;
         if (suffix != null && suffix.length() != 0) {
            retval += " " + suffix;
         }         
         return retval;
      }
      
      
      public static int getPageDefaultInt(DimConfigInfo dimConfig,  SessionManager session) throws Exception { //will look from request, then from session
         DimInfo dimInfo = dimConfig == null || dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
         if (dimInfo == null)
             dimInfo = dimConfig.m_ifClassifyIsTypeOfDim;
         if (dimInfo == null)
            return Misc.getUndefInt();
         if (dimConfig.m_hidden)
            return Misc.getParamAsInt(dimConfig.m_default);
         String idAsStr = Integer.toString(dimInfo.m_id);
         boolean is123 = dimInfo.m_descDataDimId == 123;
         String sessionParamName = is123 ? "pv123" : "pjv"+idAsStr;
         String reqParamName = is123 ? "v123" : "v"+idAsStr;
         String val = session.getParameter(reqParamName);
         
         if (val == null || val.length() == 0) {
            if (is123) {            
                val = session.getParameter(sessionParamName);                
                User user = session.getUser();
                int vi = Misc.getParamAsInt(val, Misc.G_TOP_LEVEL_PORT);

          	  int privToCheckForPort = Misc.getUndefInt();
              if (dimConfig.m_accessPriv != null) {
            	  PrivInfo.TagInfo rwTagInfo = session.getCache().getPrivId(dimConfig.m_accessPriv);
            	  if (rwTagInfo != null)
            		  privToCheckForPort = rwTagInfo.m_read;
              }
              if (privToCheckForPort < 0)
                	privToCheckForPort =   user.getPrivToCheckForOrg(session, session.getParameter("page_context")); //this tells the privilege to use for showing the Org tree
                if (privToCheckForPort < 0)
                	privToCheckForPort = 1;
                int newvi = user.getUserSpecificDefaultPort(session, vi, privToCheckForPort, dimInfo);
                val = Integer.toString(newvi);
                session.setAttribute(sessionParamName, val, true);//though this true will not have any effect ... since cookie already written                   
            }
            else {
               val = session.getParameter(sessionParamName);
            }
         }
         if (val == null || val.length() == 0) {
            return dimInfo.getDefaultInt();
         }
         else {
            return Misc.getParamAsInt(val);
         }         
      }
      public static String getPageDefaultString(DimConfigInfo dimConfig,  SessionManager session) { //will look from request, then from session 
         DimInfo dimInfo = dimConfig == null || dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
         if (dimInfo == null)
            return null;
         if (dimConfig.m_hidden)
            return dimConfig.m_default;
         String idAsStr = Integer.toString(dimInfo.m_id);
         String sessionParamName = "pjv"+idAsStr;
         String reqParamName = "v"+idAsStr;
         String val = session.getParameter(reqParamName);
         if (val == null || val.length() == 0) {
            val = session.getParameter(sessionParamName);
         }
         if (val == null || val.length() == 0) {
            return dimInfo.getDefaultString();
         }
         else {
            return val;
         }
      }
      
      public static int printBlock(Connection dbConn, User user, int projectId, int workspaceId, ArrayList configInfo, int startIndex, 
               int endIndexExcl, int defaultLabelWidth, ResultSet rset, FrontGetValHelper valHelper, StringBuilder printInfo, Cache cache, 
               String wkspChangeUri, String compLabel, String advUri, String borderColor, int readWriteMode, int hackMake1stNRowsReadonly,
               StringBuilder reqFieldVarList, StringBuilder reqFieldLabelList, StringBuilder reqFieldTypeList, boolean printHelp,
               boolean printApprovalAtTop, StringBuilder bar, StringBuilder line, String labelStyle, String dataStyle, ResultSet compRset, PageMenuHeadCalc pageMenuHeadCalc
               , MiscInner.ContextInfo currencyEtcContext
               , SessionManager session
               , HashMap multiAttribVals
               ,StringBuilder multiAttribVarsRequiringCollectOnSave //will pretend that we are filling in jg_multi_attrib_vars_collect_on_save
               //,PFM 
               ,JspWriter out // shit ... needed for print of PFM, but otherwise not needed
               ,int orderId //for checking property editability etc.
               ,int supplierId //for checking property editability etc
               ,MiscInner.ItemAccControlInfo itemAccControlInfo               
               ) throws Exception {//returns the number of cols in table                        
         //0 => read all, -1=> write all, +1, read if config to be read in edit mode
         try {
            int objectType = Misc.G_FOR_PROJECT; //For checking CustomVisibility and readability TODO - instead be smart about which property is being asked and determine the right object type and id
            int objectId = projectId;
            if (!Misc.isUndef(orderId)) {
                objectType = Misc.G_FOR_ORDER;
                objectId = orderId;
            }
            boolean isNewObject = rset == null;
            int maxCol = -1;
            FmtI.Date dtfmt = currencyEtcContext.getDateFormatter();
            boolean isOverride = pageMenuHeadCalc == null ? false : pageMenuHeadCalc.isEditOverride();
            for (int i=startIndex, is=endIndexExcl;i<is;i++) {
               ArrayList rowInfo = (ArrayList) configInfo.get(i);
               int currColCount = 0;
               int colSize = rowInfo.size();
               for (int j=0;j<colSize;j++) {
                  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                  
                  boolean hidden = dimConfig.m_hidden;
                  //if (dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_id == 5056 && !Misc.G_BUDGET_ON) {
                  //   hidden = true;
                  //}
                  
                  if (!hidden && dimConfig.m_hiddenSpecialControl != DimConfigInfo.G_READHIDE_NEVER) {
                     if (isNewObject && dimConfig.m_hiddenSpecialControl == DimConfigInfo.G_READHIDE_PRECREATE)
                        hidden = true;
                     if (!isNewObject && dimConfig.m_hiddenSpecialControl == DimConfigInfo.G_READHIDE_POSTCREATE)
                        hidden = true;
                     if (dimConfig.m_hiddenSpecialControl == DimConfigInfo.G_READHIDE_ALWAYS)
                       hidden = true;
                  }
                  if (!hidden && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null) {
                     hidden = false; //CapEx CustomImpl.isPropertyVisible(dimConfig.m_dimCalc.m_dimInfo.m_id, projectId, pageMenuHeadCalc);
                  }
                  if (!hidden) {
                     int span = dimConfig.m_dataSpan;
                     if (span < 0)
                       span = 1;
                     currColCount += span;
                  }
               }
               if (currColCount  > maxCol)
                  maxCol = currColCount;
            }
            //
            if (printApprovalAtTop && bar != null) {
               printInfo.append("<tr><td colspan='").append(2*maxCol).append("'");
               if (borderColor != null)
                  printInfo.append(" bordercolor='").append(borderColor).append("' ");
               printInfo.append(">");

               printInfo.append(bar);
               printInfo.append("</td></tr>");

            }
            //

            StringBuilder prevHiddens = new StringBuilder();
            for (int i=startIndex, is=endIndexExcl;i<is;i++,hackMake1stNRowsReadonly--) {
               if (hackMake1stNRowsReadonly > 0) 
                  printInfo.append("<tr style=\"display:none\">");
               else
                  printInfo.append("<tr>");

               ArrayList rowInfo = (ArrayList) configInfo.get(i);
               int colSize = rowInfo.size();
               int totColSpan = maxCol*2;
               int colSpanPrinted = 0;
               for (int j=0;j<colSize;j++) {
                  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                  
                  boolean hidden = dimConfig.m_hidden;
                  boolean printHiddenValue = false;
                  com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc == null ? null : dimConfig.m_dimCalc.m_dimInfo;
                  
                  //if (dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_id == 5056 && !Misc.G_BUDGET_ON) {
                  //  hidden = true;
                  if (!hidden && dimConfig.m_hiddenSpecialControl != DimConfigInfo.G_READHIDE_NEVER) {
                     if (isNewObject && dimConfig.m_hiddenSpecialControl == DimConfigInfo.G_READHIDE_PRECREATE)
                        hidden = true;
                     if (!isNewObject && dimConfig.m_hiddenSpecialControl == DimConfigInfo.G_READHIDE_POSTCREATE)
                        hidden = true;
                     if (dimConfig.m_hiddenSpecialControl == DimConfigInfo.G_READHIDE_ALWAYS)
                       hidden = true;

                  }
                  if (!hidden && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null) {
                     hidden = true;//!CustomImpl.isPropertyVisible(dimConfig.m_dimCalc.m_dimInfo.m_id, projectId, pageMenuHeadCalc);
                  }

                  String tempLabel = dimConfig.m_name;
                  if (hidden || tempLabel == null || tempLabel.length() == 0)
                     tempLabel = "&nbsp;";
                  else
                     tempLabel += ":";
                  boolean toAddInMandInfo = false;
                  if (dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_isMandatory) {
                     tempLabel = "<span class=\"tmRequiredFieldAsterisk\">*</span>"+tempLabel;
                     if (dimConfig.m_dimCalc.m_dimInfo != null)
                        toAddInMandInfo = true;
                  }
                  
                  String tempVarName = null;
                  String tempVal = null;
                  String compTempVal = null;
                  boolean printText = true;
                  boolean doDate = false;                  
                  boolean doDouble = false;
                  boolean doLOV = false;                  
                  double unformattedNumber = Misc.getUndefDouble();
                  boolean putUnformattedNumberInHidden = false; //basically for numbers that are being shown in larger units
                                                                //remembers the underlying data so that we do not loose the decimal ..
                  int attribType = Cache.LOV_TYPE;
                  if (dimInfo != null) {
                       attribType = dimInfo.getAttribType();
                       if (attribType == Cache.DATE_TYPE)
                          doDate = true;
                       if (attribType == Cache.NUMBER_TYPE) {                        
                             doDouble = true;                        
                       }
                       if (attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && !doDate) {
                          printText = false;
                       }
                       if (attribType == Cache.LOV_TYPE)
                          doLOV = true;
                       
                  }
                  boolean doingMultiAttribVal = dimInfo != null && dimInfo.m_colMap != null && ("prj_multi_attrib".equals(dimInfo.m_colMap.table) || "order_multi_attrib".equals(dimInfo.m_colMap.table) || "supplier_multi_attrib".equals(dimInfo.m_colMap.table));
                  
                  
                  tempVarName = "v"+Integer.toString(dimInfo == null ? 0 : dimInfo.m_id);
                  tempVal = null;


                  int height = 1;
                  int width = 30;
                  int labelWidth = defaultLabelWidth;

                  int tempValInt = Misc.getUndefInt();
                  int compTempValInt = Misc.getUndefInt();
                  int adornmentSpec = valHelper == null ? 0 : valHelper.getAdornmentSpec(i,j);
                  int currencyId = Misc.getUndefInt();
                  MiscInner.TripleStrStrStr fmtDblWithPrefixEtc = null;
                  if (false) {
                  }
                  else {
                      if (dimInfo != null) {
                      
                         
                         if (adornmentSpec == 2 || adornmentSpec == 3) {
                            currencyId = valHelper.getCurrencyFor(dbConn, rset, i, j, cache);
                         }
                         
                         tempVal = null;
                         compTempVal = null;
                         
                         if (!doingMultiAttribVal) {
                             if (!printText) {
                                 tempValInt = rset != null ? valHelper.getValInt(dbConn, rset, i, j, cache) : getPageDefaultInt(dimConfig, session);//dimInfo.getDefaultInt();//: Misc.getUndefInt();
                                 compTempValInt = compRset != null ? valHelper.getValInt(dbConn, compRset, i, j, cache) : Misc.getUndefInt();
                             }                                                          
                             
                             if (doDate && rset != null) {
                                 java.util.Date dt = valHelper.getValDate(dbConn, rset, i, j, cache);
                                 tempVal = helperGetStrValForDate(currencyEtcContext, dt);
                             }
                             else if (doDouble) {
                                 unformattedNumber = valHelper.getValDouble(dbConn, rset, i, j, cache);                                 
                                 fmtDblWithPrefixEtc = helperGetStrValForDouble(currencyEtcContext, adornmentSpec, currencyId, unformattedNumber, dimInfo, dimConfig.m_refUnit);
                                 //@#@#@#tempVal = helperGetStrFromValTriple(fmtDblWithPrefixEtc); ... instead based upon read/writability we will show formatted tempVal
                             }
                             else if (rset != null) {
                                 tempVal = valHelper.getVal(dbConn, rset, i, j, cache);
                             }
        
                             if (doDate && compRset != null) {
                                 java.util.Date dt = valHelper.getValDate(dbConn, compRset, i, j, cache);
                                 compTempVal = helperGetStrValForDate(currencyEtcContext, dt);                     
                             }
                             if (doDouble && compRset != null) {
                                 double v = valHelper.getValDouble(dbConn, compRset, i, j, cache);
                                 compTempVal = helperGetStrFromValTriple(helperGetStrValForDouble(currencyEtcContext, adornmentSpec, currencyId, v, dimInfo, dimConfig.m_refUnit));                                 
                             }
                             else if (compRset != null) {
                                 compTempVal = valHelper.getVal(dbConn, compRset, i, j, cache);
                             }
                         }//!doingMultiAttrib                         
                      }
                  }

                  height = dimConfig.m_height;
                  width = dimConfig.m_width;
                  String cellLabelStyle = dimConfig.m_labelStyleClass == null ? labelStyle : dimConfig.m_labelStyleClass;
                  String cellDataStyle = dimConfig.m_valStyleClass == null ? dataStyle : dimConfig.m_valStyleClass;
                  if (tempVal == null) {
                    if (printText && !doingMultiAttribVal) {
                       tempVal = getPageDefaultString(dimConfig, session);
                    }
                    if (tempVal == null)
                       tempVal = "";
                  }
                  boolean doingMeasure = false;//capex modelPage != null && (dimConfig.m_refMasterBlockInPFM != null || dimConfig.m_refBlockInPFM != null);    
                  int dataSpan = (dimConfig.m_dataSpan-1)*2+1;
                  int colSpanForDp = j < (colSize-1)? dataSpan :(totColSpan-colSpanPrinted-1);
                  int colSpanForHeader = 1;
                  if (dimConfig.m_dimCalc.m_dimInfo == null && !doingMeasure && !doingMultiAttribVal)
                     colSpanForHeader += colSpanForDp;
                   int dc1Id = dimConfig.m_dimCalc.m_dimInfo != null ? dimConfig.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
                   if (printText && tempVal.length() == 0 && dimConfig.m_default != null)
                      tempVal = dimConfig.m_default;
                  
                  if (dc1Id != 236) { //special for workspace change in header
                      if (!hidden) {
                          printInfo.append("<td colspan=\"").append(colSpanForHeader).append("\"");
                          if (borderColor != null)
                              printInfo.append(" bordercolor=\"").append(borderColor).append("\" ");
                          printInfo.append(" class=\""+cellLabelStyle+"\" ");
                          if (colSpanForHeader == 1) {
                             if (dimConfig.m_labelNowrap) {
                               printInfo.append(" align=\"right\" nowrap ");                                
                             }
                             else {
                               labelWidth = dimConfig.m_labelWidth <= 0? defaultLabelWidth : dimConfig.m_labelWidth;
                               printInfo.append(" align=\"right\" width=\"").append(labelWidth).append("\"");
                             }
                          }
                          printInfo.append(" valign=\"middle\">").append(tempLabel).append("</td>");
                          if (dimConfig.m_dimCalc.m_dimInfo == null && !doingMeasure && !doingMultiAttribVal) {
                             colSpanPrinted += colSpanForHeader + colSpanForDp;
                             continue;
                          }
                          printInfo.append("<td ");
                          if (dimConfig.m_nowrap)
                              printInfo.append(" nowrap ");

                          if (borderColor != null)
                              printInfo.append(" bordercolor=\"").append(borderColor).append("\" ");
                          //printInfo.append(" class=\""+dataStyle+"\" valign=\"middle\" colspan=\"").append(colSpanForDp).append("\">");
						  // CHANGE: To take into account data_width
						  printInfo.append(" class=\"" + cellDataStyle + "\" valign=\"middle\" colspan=\"").append(colSpanForDp).append("\" ").append((dimConfig.m_dataWidth <= 0) ? "" : "width=\"" + Integer.toString(dimConfig.m_dataWidth) + "\"").append(">");
                      }
                      int perItemReadWriteMode = readWriteMode;
                      

                      int perItemDimConfigReadSpecial = dimConfig.m_readSpecialControl;
                      if (perItemDimConfigReadSpecial == DimConfigInfo.G_READHIDE_POSTCREATE_BUT_NOT_IN_OVERRIDE)
                         perItemDimConfigReadSpecial = isOverride ? DimConfigInfo.G_READHIDE_NEVER : DimConfigInfo.G_READHIDE_POSTCREATE;
                      
                      if (perItemReadWriteMode == 1 || (perItemDimConfigReadSpecial != DimConfigInfo.G_READHIDE_NEVER && perItemReadWriteMode == -1)) {                   
                         if (isNewObject && perItemDimConfigReadSpecial == DimConfigInfo.G_READHIDE_PRECREATE) {
                            perItemReadWriteMode = 0;
                            printHiddenValue = true;
                         }
                         if (!isNewObject && perItemDimConfigReadSpecial == DimConfigInfo.G_READHIDE_POSTCREATE) {
                            perItemReadWriteMode = 0;
                            printHiddenValue = true;
                         }
                         if ((perItemReadWriteMode == 1 || perItemReadWriteMode == -1) && (dimConfig.m_readOnly || perItemDimConfigReadSpecial == DimConfigInfo.G_READHIDE_ALWAYS)) {
                            perItemReadWriteMode = 0;
                            printHiddenValue = true;                         
                         }
                         
                         if (perItemReadWriteMode == 1 || perItemReadWriteMode == -1) {
                            boolean custom = false;
                            if (dimConfig.m_dimCalc.m_dimInfo != null) {//todo do the right way by looking at what property is it
                                                                                                                                                                                                                                                                             }
                            if (!custom) {
                               perItemReadWriteMode = 0;
                               printHiddenValue = true;
                            }
                         }
                         if (perItemReadWriteMode == 1)
                            perItemReadWriteMode = -1;
                      }
                      if (perItemReadWriteMode == 0) {
                         if (tempVal == null || tempVal.length() == 0)
                            tempVal = "&nbsp;";
                         toAddInMandInfo = false;
                      }
                      StringBuilder putValsInThis = printInfo;
                      if (hidden || printHiddenValue) {
                         perItemReadWriteMode = 1;
                      }
                      if (perItemDimConfigReadSpecial == DimConfigInfo.G_READHIDE_ALWAYS)//also need to check if dimConfig.m_readOnly ... but needs checking
                         perItemReadWriteMode = 0;
                      
                      boolean printMultiAttribAsSingle = false;
                      if (doingMultiAttribVal) {
                           MiscInner.TripleBoolIntStr mavals = checkAndGetSingleValForMultiAttrib(dimConfig, dimInfo, perItemReadWriteMode, multiAttribVals, session, currencyEtcContext, adornmentSpec, currencyId, dbConn, cache, user);
                           if (mavals.first) {
                              printMultiAttribAsSingle = true;
                              tempValInt = mavals.second;
                              tempVal = mavals.third;
                           }                                                
                      }
                      //security check ...
                      boolean isAccessible = true;
                      if (dimInfo != null && dimInfo.m_accCheck != null && itemAccControlInfo != null) {
                         isAccessible = //CAPEX_REMOVE printMultiAttribAsSingle || !doingMultiAttribVal ?
                                        //CAPEX_REMOVE FrontPageInfo.helperHasValAccess(dimInfo, itemAccControlInfo, user, valHelper, null, cache, rset, dbConn, session, true)
                                        //CAPEX_REMOVE :
                                        true
                                        ;
                      }
                      
                      if (doingMeasure) {
                         
                         String pgContext = session.getParameter("page_context");
                         out.println(printInfo);
                         printInfo.setLength(0);
                         //modelPage.helpPrint(out, pgContext, false, false, false, dimConfig.m_refMasterBlockInPFM, "0px");

                      }                      
                      else if (!printMultiAttribAsSingle && doingMultiAttribVal)
                          //printMultiAttribVal(DimConfigInfo dimConfig, DimInfo dimInfo, int perItemReadWriteMode, StringBuilder printInfo, StringBuilder multiAttribVarsRequiringCollectOnSave, HashMap multiAttribVals, SessionManager session, boolean isOverrideMode, MiscInner.ContextInfo currencyEtcContext, int adornmentSpec, int currencyId, String dataStyle, boolean printHelp)
                          printMultiAttribVal(dimConfig, dimInfo, perItemReadWriteMode, printInfo, multiAttribVarsRequiringCollectOnSave, multiAttribVals, session, false, currencyEtcContext, adornmentSpec, currencyId, cellDataStyle, printHelp, valHelper, itemAccControlInfo, rset);
                      else {
                          if (!isAccessible) {
                             if (perItemReadWriteMode == -1) //writable ... then make it hidden
                                perItemReadWriteMode = 1; 
                          }
                          if (perItemReadWriteMode == 0) {
                              if (doDouble)
                                  tempVal = helperGetStrFromValTriple(fmtDblWithPrefixEtc);
                              if (!isAccessible) {
                                  tempVal = Misc.NO_FIELD_ACCESS_MSG;
                                  compTempVal = null;
                              }
                              
                              if (attribType == Cache.LOV_NO_VAL_TYPE ||  attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE || dimInfo.m_subsetOf == 203) {
                                StringBuilder t1 = new StringBuilder();
                                cache.printDimVals(dbConn, user, dimInfo, tempValInt, null, t1, null, false, null, false, Misc.getUndefInt(), 1, 30
                                    , false, null, false, false, true, null, null, Misc.getUndefInt(), Misc.getUndefInt(), null);								
								tempVal = t1.toString();
								
                              }                              
                              if (!(dimConfig.m_readSpecialControl == DimConfigInfo.G_READHIDE_ALWAYS && hidden)) //hack basically when read only set this way and hidden then not want to see ... instead have notion of load only dims
                                   printInfo.append(tempVal.length() == 0 ? "&nbsp;" : tempVal);
                              if (Misc.G_MITTAL_DOING && dimInfo.m_id == 152) {
                                 //print also the approving auth
                                 if (pageMenuHeadCalc != null) {
                                    Integer wkf = (Integer) pageMenuHeadCalc.m_criteriaInfo.get(new Integer(231));
                                    if (wkf != null) {
                                        DimInfo groupDim = DimInfo.getDimInfo(5077);
                                        int groupState = cache.getParentDimValId(dbConn, groupDim, wkf.intValue());
                                        String approvalAuth = null;
                                        if (groupState == 1) {
                                            int invClass = pageMenuHeadCalc.getPropertyInt(4035);
                                            
                                            if (!Misc.isUndef(invClass)) {
                                               DimInfo am_approval_auth = DimInfo.getDimInfo("am_approval_auth");
                                               
                                               if (am_approval_auth != null)
                                                  approvalAuth = cache.getAttribDisplayName(am_approval_auth, invClass);
                                            }
                                        }

                                        if (approvalAuth != null && approvalAuth.length() != 0) {
                                           printInfo.append(approvalAuth);
                                        }                                
                                    }
                                 }
                              }
                              if (compRset != null && compTempVal != null && !compTempVal.equals(tempVal)) {
                                 printInfo.append("&nbsp;<span class='tmCompStyle'>").append(compTempVal).append("</span>");
                              }
                              if (printHelp) {
                                 printInfo.append(Misc.getHelpText(dimConfig.m_helpTag));
                              }
                          }
                          else if (perItemReadWriteMode == -1) { //make it writable
                              if (!isAccessible) {//will not occur ... if writable then isAccessible becomes hidden
                                  tempVal = Misc.NO_FIELD_ACCESS_MSG;
                                  compTempVal = null;
                                  printText = true;
                                  doDate = false;
                                  doDouble = false;
                              }
                              if (doDouble) {                                 
                                 tempVal = fmtDblWithPrefixEtc == null ? "" : fmtDblWithPrefixEtc.second;
                              }
                              if (dimConfig.m_prefixBeforeEntry != null && dimConfig.m_prefixBeforeEntry.length() != 0) {
                                 printInfo.append(dimConfig.m_prefixBeforeEntry);
                              }
                              
                              if (printText) {
                                  if (height > 1 && !doDate) {
                                      printInfo.append("<textarea class=\""+cellDataStyle+"\" name=\"").append(tempVarName).append("\" rows=\"").append(height).append("\" cols=\"").append(width).append("\">").append(tempVal).append("</textarea>");
                                      if (compRset != null && compTempVal != null && !compTempVal.equals(tempVal)) {
                                          printInfo.append("<br><span class='tmCompStyle'>").append(compTempVal).append("</span>");
                                      }
                                      
                                  }
                                  else {
                                      printInfo.append("<input ").append(doDouble ? "onChange='_makeCellDirty()'" : "").append(" class=\""+ (doDate ? "datetimepicker" : cellDataStyle)+"\" type=\"text\" name=\"").append(tempVarName).append("\" size=\"").append(width).append("\" value=\"").append(tempVal).append("\"");
                                      /*if (doDate)
										  printInfo.append(" readonly=\"readonly\" "); */
                                      printInfo.append(">");
                                      
                                      if (doDouble) {
                                         if (fmtDblWithPrefixEtc.third != null && fmtDblWithPrefixEtc.third.length() != 0)
                                              printInfo.append("&nbsp;").append(fmtDblWithPrefixEtc.third).append("&nbsp;");
                                         printInfo.append("<input type='hidden' name='").append(tempVarName).append("_o").append("' value='").append(unformattedNumber).append("'/>");
                                      }
                                      if (doDate) {
                                          //printInfo.append("<img src=\""+Misc.G_IMAGES_BASE+"calendar.gif\" onClick='popUpCalendar(this, forms[0].").append(tempVarName).append(",  \"").append(helperGetDateFormatSpec(currencyEtcContext)).append("\")'  name=\"imgCalendar\" border=\"0\" width=\"16\" height=\"16\">");
										  printInfo.append("&nbsp;");
										  printInfo.append("<img src=\""+Misc.G_IMAGES_BASE+"undo_orange.gif\" onClick='clearCalendar(forms[0].").append(tempVarName).append(")' alt=\"Clear Date\" name=\"imgClearDate\" border=\"0\" width=\"16\" height=\"16\">");
										  printInfo.append("</div>");
                                      }
                                      if (compRset != null && compTempVal != null && !compTempVal.equals(tempVal)) {
                                         printInfo.append("&nbsp;<span class='tmCompStyle'>").append(compTempVal).append("</span>");
                                      }
                                      
                                  }//end of text box
                              }
                              else {
                              //printDimVals ..
                                  int privIdForOrg = Misc.getUndefInt();
                                  String addnlParam = null;
                                  if (dimInfo.m_descDataDimId == 123) {
                                    privIdForOrg = Misc.getUndefInt(); //CapEx ProjectCreateUpdate.getAndSetPrivForProjectCreate(session, null);//
                                    boolean mustMatch = "1".equals(session.getAttribute("org_must_match"));
                                    if (mustMatch)
                                      addnlParam = "do_match_only=1";
                                  }
                                  cache.printDimVals(dbConn, user, dimInfo, tempValInt, null, printInfo, tempVarName, false, null, false, privIdForOrg, dimConfig.m_height, dimConfig.m_width, false, addnlParam
                                  , false, false, false, null, null, Misc.getUndefInt(), Misc.getUndefInt(), currencyEtcContext);//dimInfo.m_id == 8003 ? 8003 : dimInfo.m_id == 8042 ? 8042 : Misc.getUndefInt()                                  
                              }
                              if (dimConfig.m_suffixAfterEntry != null && dimConfig.m_suffixAfterEntry.length() != 0) {
                                 printInfo.append(dimConfig.m_suffixAfterEntry);
                              }
                              if (compRset != null && !Misc.isUndef(compTempValInt) && compTempValInt != tempValInt) {
                                 printInfo.append("&nbsp;<span class='tmCompStyle'>").append(compTempVal).append("</span>");
                              }
    
                              if (printHelp)
                                 printInfo.append(Misc.getHelpText(dimConfig.m_helpTag));
                              if (printHelp && (dimConfig.m_helpTag == null || dimConfig.m_helpTag.length() == 0) && dimConfig.m_tip != null && dimConfig.m_tip.length() != 0)
                                 printInfo.append("&nbsp;<span class='tmTip'>").append(dimConfig.m_tip).append("</span>");
                          }
                          else { //make it read but with hidden vals
                              if (doDouble)
                                  tempVal = helperGetStrFromValTriple(fmtDblWithPrefixEtc);
                              if (attribType == Cache.LOV_NO_VAL_TYPE ||  attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE || dimInfo.m_subsetOf == 203) { //042508
                                StringBuilder t1 = new StringBuilder();
                                cache.printDimVals(dbConn, user, dimInfo, tempValInt, null, t1, null, false, null, false, Misc.getUndefInt(), 1, 30
                                    , false, null, false, false, true, null, null, Misc.getUndefInt(), Misc.getUndefInt(), null);
								
								tempVal = t1.toString();
								
                              }
                              StringBuilder putValsIn = hidden ?  printInfo : prevHiddens;                              
                              putValsIn.append("<input type=\"hidden\" name=\"").append(tempVarName).append(doDouble ? "_o" : "").append("\" size=\"").append(width).append("\" value=\"");
                              if (false && dimConfig.m_default != null && dimConfig.m_default.length() > 0) {
                                  putValsIn.append(dimConfig.m_default);
                              }
                              else {
                                  if (doDouble)
                                     putValsIn.append(unformattedNumber);                                  
                                  else if (printText)
                                      putValsIn.append(tempVal);
                                  else
                                      putValsIn.append(tempValInt);
                              }
                              
                              putValsIn.append("\">");
                              if (!hidden) {
                                 if (!isAccessible) {
                                    tempVal = Misc.NO_FIELD_ACCESS_MSG;
                                 }
                                 printInfo.append(tempVal);
                                 if (printHelp) {
                                    printInfo.append(Misc.getHelpText(dimConfig.m_helpTag));
                                 }
                              }
                          }
                      }//else of doing multi attrib val ... i.e. reg stuff
                      if (!hidden) {
                         printInfo.append(prevHiddens);
                         prevHiddens.setLength(0);
                         printInfo.append("</td>");
                      }
                      if (toAddInMandInfo && reqFieldVarList != null) {
                         if (reqFieldVarList.length() != 0) {
                            reqFieldVarList.append(",");
                            reqFieldLabelList.append(",");
                            reqFieldTypeList.append(",");
                         }
                         reqFieldVarList.append("'").append(tempVarName).append("'");
                         reqFieldLabelList.append("'").append(dimConfig.m_name).append("'");
                         if (printText && !doDate)
                            reqFieldTypeList.append("'t'");
                         else if (printText)
                            reqFieldTypeList.append("'d'");
                         else
                            reqFieldTypeList.append("'n'");
                      }
                  }
                  else { //special for workspace change
                      if (line != null) {
                          printInfo.append("<td ");
                          if (dimConfig.m_nowrap)
                             printInfo.append(" nowrap ");
                          if (borderColor != null)
                             printInfo.append(" bordercolor=\"").append(borderColor).append("\" ");

                          printInfo.append(" class=\"tmTip\" valign=\"middle\" colspan=\"");
                          printInfo.append(colSpanForDp+colSpanForHeader).append("\">");
                          printInfo.append(line).append("</td>");
                      }
                      else if (compLabel != null && compLabel.length() != 0) {
                          printInfo.append("<td ");
                          if (dimConfig.m_nowrap)
                             printInfo.append(" nowrap ");
                          if (borderColor != null)
                             printInfo.append(" bordercolor=\"").append(borderColor).append("\" ");

                          printInfo.append(" class=\""+cellDataStyle+"\" valign=\"middle\" colspan=\"");
                          printInfo.append(colSpanForDp+colSpanForHeader).append("\">");
                          printInfo.append("Viewing ").append(compLabel).append("</td>");
                      }
                      else {
                          printInfo.append("<td ");
                          if (borderColor != null)
                             printInfo.append(" bordercolor=\"").append(borderColor).append("\" ");
                          printInfo.append(" class=\""+cellLabelStyle+"\" align=\"right\" width=\"").append(labelWidth).append("\" valign=\"middle\">").append(tempLabel).append("</td>");
                          printInfo.append("<td ");
                          if (dimConfig.m_nowrap)
                             printInfo.append(" nowrap ");
                          if (borderColor != null)
                             printInfo.append(" bordercolor=\"").append(borderColor).append("\" ");

                          printInfo.append(" class=\""+cellDataStyle+"\" valign=\"middle\" colspan=\"").append(colSpanForDp).append("\">");

                          if (wkspChangeUri == null || wkspChangeUri.length() == 0)
                             wkspChangeUri = Misc.G_APP_1_BASE+"project_detail.jsp";
                          String onChangeURL = "handle_workspace_change('"+wkspChangeUri+"', "+Integer.toString(projectId)+","+Integer.toString(Misc.getUndefInt())+")";
                          printInfo.append("<select ID=\"workspace_selector\" class=\""+cellDataStyle+"\" name=\"workspace_selector\" onchange=\"").append(onChangeURL).append("\">");
                          //CAPEX_REMOVE com.ipssi.gen.utils.Misc.printPrjWorkspaces(dbConn, projectId, printInfo, workspaceId);
                          printInfo.append("</select>");
                          printInfo.append("</td>");
                      }
                  }
                  if (!hidden)
                      colSpanPrinted += colSpanForDp + colSpanForHeader;
               } //looped thru all cols
               printInfo.append("</tr>");
            } //looped thru all row
            //
            if (!printApprovalAtTop && bar != null) {
               printInfo.append("<tr><td colspan='").append(2*maxCol).append("'");
               if (borderColor != null)
                  printInfo.append(" bordercolor='").append(borderColor).append("' ");
               printInfo.append(">");

               printInfo.append(bar);
               printInfo.append("</td></tr>");

            }
            //

            return 2*maxCol;
         }//end of try
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
      }
      
      public static MiscInner.PairIntStr helperGetValFromMultiAttribData(DimConfigInfo dimConfig, DimInfo dimInfo, PageHeader.MultiAttribData mudata, SessionManager session, MiscInner.ContextInfo currencyEtcContext, int adornmentSpec, int currencyId, boolean getDefaultIfNullData, Connection dbConn, Cache cache,User user ) throws Exception {
         String tempVal = null;
         int tempValInt = Misc.getUndefInt();
         if (mudata == null && !getDefaultIfNullData)
            return new MiscInner.PairIntStr(tempValInt, tempVal);
         int attribType = dimInfo.getAttribType();
         boolean doDate = false;
         boolean printText = true;
         boolean doDouble = false;
         boolean doLOV = false;
         boolean doFile = attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE;
         if (attribType == Cache.DATE_TYPE)
            doDate = true;
         if (attribType == Cache.NUMBER_TYPE) {                        
               doDouble = true;                        
         }
         if (attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && !doDate) {
            printText = false;
         }
         if (attribType == Cache.LOV_TYPE) {
            ArrayList vl = dimInfo.getValList();
            if (vl != null && vl.size() != 0)
               doLOV = true;
         }

         if (!printText) {
             tempValInt = mudata != null ? mudata.getIntVal() : getPageDefaultInt(dimConfig, session);//dimInfo.getDefaultInt();//: Misc.getUndefInt();             
         }
         if (doLOV) {
             DimInfo.ValInfo vinf = dimInfo.getValInfo(tempValInt);
             if (vinf != null)
                tempVal = vinf.m_name;
         }
         else if (attribType == Cache.LOV_NO_VAL_TYPE || attribType == Cache.LOV_TYPE || attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE) {
            StringBuilder t1 = new StringBuilder();
            cache.printDimVals(dbConn, user, dimInfo, tempValInt, null, t1, null, false, null, false, Misc.getUndefInt(), 1, 30
                  , false, null, false, false, true, null, null, Misc.getUndefInt(), Misc.getUndefInt(), null);
            tempVal = t1.toString();
  
         }
         else if (doDate && mudata != null) {
             java.util.Date dt = mudata.getDateVal();
             tempVal = helperGetStrValForDate(currencyEtcContext, dt);
         }
         else if (doDouble && mudata != null) {
             double v = mudata.getDoubleVal();
             tempVal = helperGetStrFromValTriple(helperGetStrValForDouble(currencyEtcContext, adornmentSpec, currencyId, v, dimInfo, dimConfig.m_refUnit));
         }
         
         else if (mudata != null) {
             tempVal = mudata.toString();
         }
         if (tempVal == null)
            tempVal = getPageDefaultString(dimConfig, session);
         if (tempVal == null)
            tempVal = "";          
         return new MiscInner.PairIntStr(tempValInt, tempVal);
      }
      
      public static MiscInner.TripleBoolIntStr checkAndGetSingleValForMultiAttrib(DimConfigInfo dimConfig, DimInfo dimInfo, int perItemReadWriteMode, HashMap multiAttribVals, SessionManager session, MiscInner.ContextInfo currencyEtcContext, int adornmentSpec, int currencyId, Connection dbConn, Cache cache, User user) throws Exception  {
         
         ArrayList attribValList = multiAttribVals == null ? null : (ArrayList) multiAttribVals.get(new Integer(dimInfo.m_id));
         boolean doingMulti = (attribValList != null && attribValList.size() > 1) || (dimConfig.m_addRow && perItemReadWriteMode == -1) || (dimConfig.m_multiRowSubColList != null && dimConfig.m_multiRowSubColList.size() > 0); //rajeev 012708 ... changes > 1 to > 0
         PageHeader.MultiAttribData mudata = null;
         if (attribValList != null && attribValList.size() > 0) {
             mudata = (PageHeader.MultiAttribData) attribValList.get(0);
         }
         MiscInner.PairIntStr vals = helperGetValFromMultiAttribData(dimConfig, dimInfo, mudata, session, currencyEtcContext, adornmentSpec, currencyId, true, dbConn, cache, user);
         return new MiscInner.TripleBoolIntStr(!doingMulti, vals.first, vals.second);
      }

      public static void printMultiAttribVal(DimConfigInfo dimConfig, DimInfo dimInfo, int perItemReadWriteMode, StringBuilder printInfo, StringBuilder multiAttribVarsRequiringCollectOnSave, HashMap multiAttribVals, SessionManager session, boolean isOverrideMode, MiscInner.ContextInfo currencyEtcContext, int adornmentSpec, int currencyId, String dataStyle, boolean printHelp, FrontGetValHelper topLevelValHelper, MiscInner.ItemAccControlInfo itemAccControlInfo, ResultSet topLevelRset) throws Exception {
         //perItemReadWriteMode: 0 = read, -1 is write, 1 is read but with hidden
         //TODO BUG when showing file, project etc. in read only mode we need to get the display name otherwise will show the id ... this is fixed
         Cache cache = session.getCache();
         Connection dbConn = session.getConnection();
         User user = session.getUser();
         
         ArrayList attribValList = multiAttribVals == null ? null : (ArrayList) multiAttribVals.get(new Integer(dimInfo.m_id));
         int attribCount = attribValList == null ? 0 : attribValList.size();
         boolean canEditPast = (dimInfo.m_editPastEditMon != 0 && !dimConfig.m_multiRowOldReadOnly)|| isOverrideMode;
         int attribType = dimInfo.getAttribType();
         
         FmtI.Date dtfmt = currencyEtcContext.getDateFormatter();         
         ArrayList subCols = dimConfig.m_multiRowSubColList;
         if (subCols != null && subCols.size() == 0)
            subCols = null;
         //cellpadding="3" bordercolor="#003366"
         printInfo.append("<TABLE  cellspacing='0' cellpadding='").append(perItemReadWriteMode == -1 ? 0 : 2).append("' border='1' bordercolor='#003366' ID='").append("v").append(dimInfo.m_id).append("_table").append("'>");
         if (subCols != null && subCols.size() > 1) {
             printInfo.append("<THEAD><TR>");
             for (int s=0,ss = subCols == null ? 0 : subCols.size();s < ss; s++) {
                DimConfigInfo ds = (DimConfigInfo)subCols.get(s);
                printInfo.append("<TD class='tshc' ");
                if (ds.m_labelWidth > 0)
                   printInfo.append(" width='").append(ds.m_labelWidth).append("'");
                printInfo.append(">");
                
                printInfo.append(ds.m_name);
                printInfo.append("</td>");
             }
             if (perItemReadWriteMode == -1)// && dimConfig.m_addRow)
                printInfo.append("<td class='tshc'>&nbsp;</td>");
             printInfo.append("</TR></THEAD>");
         }
         printInfo.append("<TBODY>");
         StringBuilder hiddenVal = new StringBuilder();
         
         boolean doDate = false;
         boolean printText = true;
         boolean doDouble = false;
         boolean doLOV = false;
         if (attribType == Cache.DATE_TYPE)
            doDate = true;
         if (attribType == Cache.NUMBER_TYPE) {                        
               doDouble = true;                        
         }
         if (attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && !doDate) {
            printText = false;
         }
         if (attribType == Cache.LOV_TYPE)
            doLOV = true;
         
         int numRowsToPrint = perItemReadWriteMode != -1 ? attribCount : attribCount == 0 ? 1 : (canEditPast && attribType != Cache.FILE_TYPE && attribType != Cache.IMAGE_TYPE) ? attribCount : attribCount+1; //CHANGE: condition added for FILE_TYPE 08282008
         for (int i=0,is=numRowsToPrint;i<is;i++) {             
             boolean doingAddRow = perItemReadWriteMode == -1 && i == is-1;
             //PrjTemplateHelper.MultiAttribData mudata = !doingAddRow ? (PrjTemplateHelper.MultiAttribData) attribValList.get(i) : null;
             PageHeader.MultiAttribData mudata = i < attribCount ? (PageHeader.MultiAttribData) attribValList.get(i) : null;
             boolean fillInWithDefault = mudata == null && attribCount == 0 ;    
             
             printInfo.append("<TR>");
             for (int s=0,ss = subCols == null ? 1 : subCols.size();s < ss; s++) {
                  DimConfigInfo ds = subCols != null ? (DimConfigInfo)subCols.get(s) : dimConfig;
                  int height = ds.m_height;
                  int width = ds.m_width;
                  
                  printInfo.append("<TD class='cn' ");
                  if (ds.m_labelWidth > 0)
                     printInfo.append(" width='").append(ds.m_labelWidth).append("'");
                  printInfo.append(">");
                  String varName = ds.m_columnName;
                  
                  if (varName == null || varName.length() == 0)
                     if (subCols == null)
                        varName = "val";
                     else
                        varName ="z"; //some junk
                  boolean doingValCol = "val".equals(varName);
                  DimInfo dsDim = doingValCol ? dimInfo : ds.m_ifClassifyIsTypeOfDim;
                  int eachCellReadWriteMode = perItemReadWriteMode;
                  if (!canEditPast && !doingAddRow)
                     eachCellReadWriteMode = 0;
                  boolean eachCellPrintText = true;                
                  boolean eachCellDoDate = false;
                  boolean eachCellDoDouble = false;
                  String tempVal = "";
                  int tempValInt = Misc.getUndefInt();
                  if (doingValCol)
                     varName = "v"+dimInfo.m_id;
                  hiddenVal.setLength(0);                
                  boolean isAccessible = true;
                  if (dsDim != null && dsDim.m_accCheck != null && itemAccControlInfo != null) {
                     isAccessible = true;//CAPEX_REMOVE FrontPageInfo.helperHasValAccess(dsDim, itemAccControlInfo, session.getUser(), topLevelValHelper, null, session.getCache(), topLevelRset, session.getConnection(), session, true);
                  }
                  if (!isAccessible) {
                     if (eachCellReadWriteMode == -1)
                        eachCellReadWriteMode = 1;
                  }
                  if (doingValCol) {
                     if (eachCellReadWriteMode != 0)
                         hiddenVal.append("<input type='hidden' value='").append(mudata == null || mudata.m_createDate == null ? "" : helperGetStrValForDate(currencyEtcContext, mudata.m_createDate))
                         .append("' name='created_on'><input type='hidden' name='created_by' value='")
                         .append(mudata == null || Misc.isUndef(mudata.m_createUser) ? "" : Integer.toString(mudata.m_createUser))
                         .append("'>");
                  
                     
                     MiscInner.PairIntStr vals = helperGetValFromMultiAttribData(dimConfig, dimInfo, mudata, session, currencyEtcContext, adornmentSpec, currencyId, fillInWithDefault, dbConn, cache, user);                   
                     tempValInt = vals.first;
                     tempVal = vals.second;
                     if (!isAccessible)
                        tempVal = Misc.NO_FIELD_ACCESS_MSG;
                     if (tempVal == null)
                        tempVal = "";
                     
                     eachCellPrintText = printText;
                     eachCellDoDate = doDate;
                     eachCellDoDouble = doDouble;
                     
                     if (eachCellReadWriteMode == 1) {
                        hiddenVal.append("<input type='hidden' name='").append(varName).append("'").append(" value='");
                        if (!printText)
                           hiddenVal.append(tempValInt);
                        else
                           hiddenVal.append(tempVal);
                        hiddenVal.append("'>");
                     }
                  }
                  else if ("created_on".equals(varName)) {
                     eachCellReadWriteMode = 0;
                     tempVal = mudata == null || mudata.m_createDate == null ? "" : helperGetStrValForDate(currencyEtcContext, mudata.m_createDate) ;
                  }
                  else if ("created_by".equals(varName)) {
                     eachCellReadWriteMode = 0;
                     tempVal = mudata == null || mudata.m_userName == null ? "" : mudata.m_userName;
                  }
                  else if (varName != null && varName.startsWith("classify")){
                     int index = Misc.getParamAsInt(varName.substring(8))-1;
                     
                     boolean clPrintText = dsDim == null || dsDim.m_type != Cache.LOV_TYPE;
                     tempVal = mudata == null ? null : mudata.getClassifyVal(index);
                     eachCellPrintText = dsDim == null || dsDim.m_type != Cache.LOV_TYPE;                
                    
                  
                     if (tempVal == null && fillInWithDefault) {
                        if (eachCellPrintText) {
                           tempVal  = getPageDefaultString(ds, session);
                        }
                        else {
                           tempValInt = getPageDefaultInt(ds, session);
                           if (dsDim.m_descDataDimId == 123) {
                              tempVal = cache.getFullPortName(dbConn, tempValInt, null);
                           }
                           else {
                              tempVal = cache.getAttribDisplayName(dsDim, tempValInt);
                           }
                        }
                        if (!isAccessible)
                           tempVal = Misc.NO_FIELD_ACCESS_MSG;
                     }
                     else if (tempVal != null) {
                        if (!eachCellPrintText) {
                           tempValInt = Misc.getParamAsInt(tempVal);
                           if (dsDim.m_descDataDimId == 123)
                              tempVal = cache.getFullPortName(dbConn, tempValInt, null);
                           else
                              tempVal = cache.getAttribDisplayName(dsDim, tempValInt);
                        }
                     }
                     if (tempVal == null)
                        tempVal = "";
                     if (eachCellReadWriteMode == 1 && ((eachCellPrintText && tempVal != null && tempVal.length() > 0) ||  (!eachCellPrintText && !Misc.isUndef(tempValInt))))
                        hiddenVal.append("<input type='hidden' name='").append(varName).append("' value='").append(!eachCellPrintText ? Integer.toString(tempValInt) : tempVal == null ? "" : tempVal).append("'>");
                  }
                  if (eachCellReadWriteMode == 0) {
                     if (!isAccessible)
                        tempVal = Misc.NO_FIELD_ACCESS_MSG;     
                     printInfo.append(tempVal);
                  }
                  else if (eachCellReadWriteMode == -1) {
                     if (eachCellPrintText) {
                          if (height > 1 && !eachCellDoDate) {
                              printInfo.append("<textarea class=\""+dataStyle+"\" name=\"").append(varName).append("\" rows=\"").append(height).append("\" cols=\"").append(width).append("\">").append(tempVal).append("</textarea>");                            
                          }
                          else {
                              printInfo.append("<input class=\""+(eachCellDoDate ? "datetimepicker" : dataStyle)+"\" type=\"text\" name=\"").append(varName).append("\" size=\"").append(width).append("\" value=\"").append(tempVal);
                              /*if (eachCellDoDate)
								  printInfo.append(" readonly=\"readonly\" "); */
                                      
                              printInfo.append("\">");
                              
                              if (eachCellDoDate) {
                                  //printInfo.append("<img src=\""+Misc.G_IMAGES_BASE+"calendar.gif\" onClick='popUpCalendar(this, forms[0].").append(varName).append(",  \"").append(helperGetDateFormatSpec(currencyEtcContext)).append("\")'  name=\"imgCalendar\" border=\"0\" width=\"16\" height=\"16\">");
								  printInfo.append("&nbsp;");
								  printInfo.append("<img src=\""+Misc.G_IMAGES_BASE+"undo_orange.gif\" onClick='clearCalendar(forms[0].").append(varName).append(")' alt=\"Clear Date\" name=\"imgClearDate\" border=\"0\" width=\"16\" height=\"16\">");
								  printInfo.append("</div>");
                              }                         
                          }//end of text box
                      }
                      else {
                      //printDimVals ..
                          int privIdForOrg = Misc.getUndefInt();
                          String addnlParam = null;
                          if (dsDim != null && dsDim.m_descDataDimId == 123) {
                            privIdForOrg = Misc.getUndefInt();//CapEx ProjectCreateUpdate.getAndSetPrivForProjectCreate(session, null);//
                            boolean mustMatch = "1".equals(session.getAttribute("org_must_match"));
                            if (mustMatch)
                              addnlParam = "do_match_only=1";
                          }
                          cache.printDimVals(dbConn, user, dsDim, tempValInt, null, printInfo, varName, false, null, false, privIdForOrg, ds.m_height, ds.m_width, false, addnlParam, true, true, false, null,null, Misc.getUndefInt(), Misc.getUndefInt(), currencyEtcContext);
                      }
                      if (doingValCol) {
                          if (dimConfig.m_suffixAfterEntry != null && dimConfig.m_suffixAfterEntry.length() != 0) {
                             printInfo.append(dimConfig.m_suffixAfterEntry);
                          }
                      }
                     printInfo.append(hiddenVal);
                  }
                  else  {
                     if (!isAccessible)
                        tempVal = Misc.NO_FIELD_ACCESS_MSG;
                     printInfo.append(tempVal);
                     printInfo.append(hiddenVal);             
                  }
                  
                  printInfo.append("</td>");
             }//for each col
             if (perItemReadWriteMode == -1) { //print add/remove button
                  if (doingAddRow) {
                     if (attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE)
                        printInfo.append("<td class='cn'><img title='Add another row' src='"+Misc.G_IMAGES_BASE+"green_check.gif'  onClick='callOpenFileUploadPopup(\"").append("v").append(dimInfo.m_id).append("\",true)'></td>");
                     else {
                        printInfo.append("<td class='cn'><img title='Add another row' src='"+Misc.G_IMAGES_BASE+"green_check.gif'  onClick='");
                        if (dimConfig.m_multiColAddRowScript != null && dimConfig.m_multiColAddRowScript.length() != 0) {
                           printInfo.append(dimConfig.m_multiColAddRowScript);
                        }
                        else {
                           printInfo.append("addRowMulti(event.srcElement)");
                        }
                        printInfo.append("'></td>");
                     }
                  }
                  else {
                     if (canEditPast)
                        printInfo.append("<td class='cn'><img title='Remove row' src='"+Misc.G_IMAGES_BASE+"cancel.gif'  onClick='removeRowHelper(event.srcElement)'></td>");
                     else
                        printInfo.append("<td class='cn'>&nbsp;</td>");
                  }                  
             }
             printInfo.append("</tr>");
         }//for each row of data
         printInfo.append("</tbody></table>");
         if (perItemReadWriteMode != 0 && multiAttribVarsRequiringCollectOnSave != null) {
             multiAttribVarsRequiringCollectOnSave.append(" jg_multi_attrib_vars_collect_on_save[jg_multi_attrib_vars_collect_on_save_count++] = ").append(dimInfo.m_id).append("; ");
             String xmlName = "v"+Integer.toString(dimInfo.m_id)+"_xml";
             printInfo.append("<input type='hidden' ID='").append(xmlName).append("' name='").append(xmlName).append("'>");
         }
         
         if (printHelp)
            printInfo.append(Misc.getHelpText(dimConfig.m_helpTag));
         if (printHelp && (dimConfig.m_helpTag == null || dimConfig.m_helpTag.length() == 0) && dimConfig.m_tip != null && dimConfig.m_tip.length() != 0)
            printInfo.append("&nbsp;<span class='tmTip'>").append(dimConfig.m_tip).append("</span>");

      
      }
      

      static public MiscInner.SearchBoxHelper processSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter) throws Exception {//return maxColWidth
         String searchButton = _session.getParameter("SearchButton");         
         boolean searchButtonPressed = searchButton != null && searchButton.length() != 0;
         return processSearchBox(_session, privIdForOrg, pgContext, configInfo, valGetter, searchButtonPressed, false);   
      }
      static public MiscInner.SearchBoxHelper processSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, boolean neverSetParamAsPerm) throws Exception {//return maxColWidth
    	  String searchButton = _session.getParameter("SearchButton");         
          boolean searchButtonPressed = searchButton != null && searchButton.length() != 0;
          return processSearchBox(_session, privIdForOrg, pgContext, configInfo, valGetter, searchButtonPressed, neverSetParamAsPerm);   
         
      }
      static public String[] cleanupParamVals(String[] paramVals) {
    	  ArrayList<String> retval = null;
    	  for (int i=0,is = paramVals == null ? 0 : paramVals.length;i<is;i++) {
    		  String t = paramVals[i];
    		  t = t == null ? null : t.trim();
    		  if (t == null || t.length() == 0) {
    			  t = null;
    			  if (retval == null) {
    				  retval = new ArrayList<String>();
    				  for (int j=0,js=i; j<js; j++)
    					  retval.add(paramVals[j]);
    			  }
    			  continue;
    		  }
    		  if (t != null && t.indexOf(",") >= 0) {
    			  String temp[] = Misc.convertValToArray(t);
    			  if (retval == null) {
    				  retval = new ArrayList<String>();
    				  for (int j=0,js=i; j<js; j++)
    					  retval.add(paramVals[j]);
    			  }
    			  for (int j=0,js=temp == null ? 0 : temp.length; j<js ;j++) {
    				  retval.add(temp[j]);
    			  }
    			  continue;
    		  }
    		  if (retval != null)
    			  retval.add(t);
    	  }
    	  if (retval == null)
    		  return paramVals;
    	  else if (retval.size() == 0)
    		  return null;
    	  else {
    		paramVals = new String[retval.size()];
    		for (int j=0,js = retval.size();j<js;j++)
    			paramVals[j] = retval.get(j);
    		return paramVals;
    	  }
      }
      static public MiscInner.SearchBoxHelper processSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, boolean searchButtonPressed, boolean neverSetParamAsPerm) throws Exception {//return maxColWidth
         User _user = _session.getUser();
         Connection _dbConnection = _session.getConnection();
         Cache _cache = _session.getCache();
         HttpServletRequest request = _session.request;
         
         if (configInfo == null || configInfo.size() == 0)
            return null;
         boolean retainPreviousAsMuchAsPossible = "1".equals(_session.getParameter("_from_link"));
         boolean ignoreParametersProvided = false;
         //if retain  then will try to get parameters also from prevTopPageContext
         String prevTopPageContext = Misc.getParamAsString(_session.getParameter("_prev_top_page_context"),null);
         if (!retainPreviousAsMuchAsPossible)
        	 prevTopPageContext = null;
         
         int specialMode = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("special_mode"));
         boolean launchInPrjPickMode = specialMode == 1;
         boolean launchInPrjCmpMode = specialMode == 2;
         boolean launchInWorkflowMode = specialMode == 3;
            
         
         int matchToProjectId = (int)_session.getProjectId();
         int matchToWorkspaceId = (int)_session.getWorkspaceId();
         int matchToAlternativeId = (int)_session.getAlternativeId();
         int pickPrjModeOrigOrg = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("pick_prj_mode_org"));
         if (com.ipssi.gen.utils.Misc.isUndef(pickPrjModeOrigOrg))
             pickPrjModeOrigOrg = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("v123"));   
         String paramNameFor123Sel = null;      
   
         int hackAnyVal = com.ipssi.gen.utils.Misc.G_HACKANYVAL;
   

         String topPageContext = UserGen.helpGetAndSetMainMenuLevelContext(_session, pgContext);
         if (configInfo == null || configInfo.size() == 0) {
        	 return  new MiscInner.SearchBoxHelper(0, topPageContext == null ? "p" : topPageContext, configInfo, valGetter, privIdForOrg);
         }
         int saveResetMode = Misc.getParamAsInt(_session.getParameter("_search_save_etc"),-1);
         //saveResetMode = -1 ... do nothing
         //                              =   0 ... load
         //                              =   1 ... save
         //                              =   2 ... reset
         //                              =   3 ... specialSearch;
         ArrayList<Integer> dimsToRetainInSpecialSearch = null;
         if (saveResetMode == 3) {
        	 PageHeader.resetPagePref(_user,_session, pgContext, topPageContext, configInfo, _dbConnection, true);
        	 dimsToRetainInSpecialSearch = new ArrayList<Integer>();
        	 String drssStr = _session.getParameter("dims_retain_search");
        	 Misc.convertValToVector(drssStr, dimsToRetainInSpecialSearch);
        	 searchButtonPressed = true;
        	 _session.setAttribute("SearchButton","1", false);
         } 
         else if (saveResetMode == 2) {
        	 PageHeader.resetPagePref(_user,_session, pgContext, topPageContext, configInfo, _dbConnection, true);
        	 ignoreParametersProvided = true;
        	 searchButtonPressed = true;
        	 _session.setAttribute("SearchButton","1", false);
         }
         else if (saveResetMode == 0) {
        	 PageHeader.loadPagePref(_user,_session, pgContext, topPageContext, configInfo, _dbConnection, true);
        	 ignoreParametersProvided = true;
        	 searchButtonPressed = true;
        	 _session.setAttribute("SearchButton","1", false);

         }         
         else if (saveResetMode == 1) {
        	 searchButtonPressed = true;
        	 _session.setAttribute("SearchButton","1", false);

        	 //rest handled later
         }
        else if (!retainPreviousAsMuchAsPossible) {
            PageHeader.loadPagePref(_user,_session, pgContext, topPageContext, configInfo, _dbConnection, false);
        }
         _session.setAttribute(pgContext+"_load","1", true);

   
         int maxCol = -1;
   
         HashMap cmpPrjInfo = null;
         if (!searchButtonPressed && launchInPrjCmpMode) { //find the initial attributes with which values should be populated for matching
			 //CAPEX_REMOVE cmpPrjInfo = com.ipssi.gen.utils.Misc.getPjAttribInfo(_dbConnection, _cache, valGetter, matchToProjectId, matchToWorkspaceId, matchToAlternativeId, false);//081808
         }
         
         for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
               ArrayList rowInfo = (ArrayList) configInfo.get(i);
               int currColCount = 0;
               int colSize = rowInfo.size();
               for (int j=0;j<colSize;j++) {
                  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                  if (!dimConfig.m_hidden) {
                     int span = dimConfig.m_dataSpan;
                     if (span < 0)
                        span = 1;
                     currColCount += span;
                  }
                  if (dimConfig.m_dimCalc.m_dimInfo == null) {
      
                     continue;
                  }
                  boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
                  
      
                  String paramId = Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id);
                  //String paramName = dimConfig.m_paramName;
                  //if (paramName == null || paramName.length() == 0) {
                  
                  if (topPageContext == null)
                     topPageContext = "p";
                  
                  String paramName = is123 ? "pv123" : 
                	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                					  :
                			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                			  ;
                  String addnlParamName = topPageContext+paramId; //essentially everywhere else this was supposed to be the way to access the values, therefore these will also be populated if necessary
                  if (addnlParamName.equals(paramName))
                	  addnlParamName = null;
                  String linkedInParamName = "pv"+paramId;
                  if (paramName.equals(linkedInParamName))
                	  linkedInParamName = null;
                  String retainParamName = (prevTopPageContext == null ? topPageContext : prevTopPageContext)+paramId;//prevTopPageContext == null ? null : prevTopPageContext+paramId;
                  if (saveResetMode == 3) {
                	  boolean toProcess = true;
                	 if (!is123) {
                		 toProcess = false;
                		 //check if exists in dimsToRetainInSpecialSearch
                		 for (int v1=0,v1s = dimsToRetainInSpecialSearch.size();v1<v1s;v1++) {
                			 if (dimsToRetainInSpecialSearch.get(v1) == dimConfig.m_dimCalc.m_dimInfo.m_id) {
                				 toProcess = true;
                				 break;
                			 }
                		 }
                		 if (!toProcess) {
                			 _session.removeAttribute(paramName);
                			 //_session.setAttribute(paramName, "", false);
                			 //_session.setAttribute(addnlParamName, "", false);
                			 continue;
                		 }
                	 }
                  }
                  if (saveResetMode == 2)
                	  retainParamName = null;
                  String paramVals[] = null;
                  String operator = null;
                  String operand2 = null;
                  boolean ignoreThisParam = ignoreParametersProvided;
                  if (!retainPreviousAsMuchAsPossible && dimConfig.m_hidden)
                	  ignoreThisParam = true;
                  if (!ignoreThisParam) {
                	  if (is123) {
                		  paramVals = request.getParameterValues(paramName);
                		  paramVals = cleanupParamVals(paramVals);
                		  if (paramVals == null || paramVals.length == 0) {
                			  String tv = _session.getParameter(paramName);
                			  if (tv != null && tv.length() != 0) {
                				  paramVals = new String[1];
                				  paramVals[0] = tv;
                			  }
                		  }
                	  }
                	  else {
                       	  if (linkedInParamName != null && (paramVals == null || paramVals.length == 0)) {
	                		  paramVals = request.getParameterValues(linkedInParamName);
	                		  paramVals = cleanupParamVals(paramVals);
	                		  if (paramVals == null || paramVals.length == 0) {
	                			  String tv = _session.getParameter(linkedInParamName);
	                			  if (tv != null && tv.length() != 0) {
	                				  paramVals = new String[1];
	                				  paramVals[0] = tv;
	                			  }
	                		  }
	                		  if (dimConfig.m_numeric_filter && (paramVals == null || paramVals.length == 0)) {
	                			  paramVals = request.getParameterValues(linkedInParamName+"_operand_first");
	                		  }
	                		  if (dimConfig.m_numeric_filter && (paramVals != null && paramVals.length != 0)) {
	            				  operand2 = _session.getParameter(linkedInParamName+"_operand_second");
	            				  operator = _session.getParameter(linkedInParamName+"_operator");
	            			  }
	                	  }
	 
	                	  if (retainPreviousAsMuchAsPossible && retainParamName != null && (paramVals == null || paramVals.length == 0)) {
	                		  String tv = _session.getParameter(retainParamName);
	                		  
	                		  if (dimConfig.m_numeric_filter && (tv == null || tv.length() == 0)) {
	                			  tv = _session.getParameter(retainParamName+"_operand_first");
	                		  }
	                		  if (tv != null && tv.length() != 0) {
	                			  paramVals = new String[1];
	                			  paramVals[0] = tv;
	                			  if (dimConfig.m_numeric_filter) {
	                				  operand2 = _session.getParameter(retainParamName+"_operand_second");
	                				  operator = _session.getParameter(retainParamName+"_operator");
	                			  }
	                		  }                		  
	                	  }
	                	  if (paramName != null && (paramVals == null || paramVals.length == 0)) {
	                		  paramVals = request.getParameterValues(paramName);
	                		  paramVals = cleanupParamVals(paramVals);
	                		  if (dimConfig.m_numeric_filter && (paramVals == null || paramVals.length == 0)) {
	                			  paramVals = request.getParameterValues(paramName+"_operand_first");
	                		  }
	                		  if (dimConfig.m_numeric_filter && (paramVals != null && paramVals.length != 0)) {
	            				  operand2 = request.getParameter(paramName+"_operand_second");
	            				  operator = request.getParameter(paramName+"_operator");
	            			  }
	                	  }
	                	  if (addnlParamName !=null && (paramVals == null || paramVals.length == 0)) {
	                		  paramVals = request.getParameterValues(addnlParamName);
	                		  paramVals = cleanupParamVals(paramVals);
	                		  if (dimConfig.m_numeric_filter && (paramVals == null || paramVals.length == 0)) {
	                			  paramVals = request.getParameterValues(addnlParamName+"_operand_first");
	                		  }
	                		  if (dimConfig.m_numeric_filter && (paramVals != null && paramVals.length != 0)) {
	            				  operand2 = request.getParameter(addnlParamName+"_operand_second");
	            				  operator = request.getParameter(addnlParamName+"_operator");
	            			  }
	                	  }
                	  }
                  }//if not to ignore Parameter
                  
                  boolean setAsPerm = !neverSetParamAsPerm;
                  if (dimConfig.m_hidden || retainPreviousAsMuchAsPossible) {
                	  setAsPerm = false;
                  }
                  if (paramVals == null || paramVals.length == 0) {
                	  String tv = dimConfig.m_default;
                	  if (tv != null && tv.length() != 0) {
                		  paramVals = new String[1];
                		  paramVals[0] = tv;
                	  }
                	  setAsPerm = false;
                  }
                  if (dimConfig.m_numeric_filter) {
                	  if (operator == null || operator.length() == 0 || "-1000".equals(operator)) {
                		  String tv = dimConfig.m_defaultOperator;
                       	  if (tv == null || tv.length() == 0) {
                       		  tv = dimConfig.m_forDateApplyGreater ? "1" : "2";
                       	  }
                       	  operator = tv;
                	  }
                	  if (operand2 == null || operand2.length() == 0) {
                		  operand2 = dimConfig.m_rightOperand;
                	  }
                  }
                  int attribType = dimConfig.m_dimCalc.m_dimInfo.m_type;
                  boolean isIntLike =  attribType != Cache.STRING_TYPE && attribType != Cache.NUMBER_TYPE && attribType != Cache.DATE_TYPE;
                  if (isIntLike) {
                	  if (paramVals != null && paramVals.length > 0) {
                		  if ("_".equals(paramVals[0]))
                			  paramVals[0] = Integer.toString(Misc.G_HACK_ISNOTNULL_LOV);
                		  else if ("null".equals(paramVals[0]))
                			  paramVals[0] = Integer.toString(Misc.G_HACK_ISNULL_LOV);
                	  }
                  }
                  
                  if (dimConfig.m_hidden && ignoreThisParam) {
                     _session.removeAttribute(paramName);
                     if (dimConfig.m_dimCalc.m_dimInfo.m_id == 99) {
                        _session.setAttribute(paramName, Long.toString(_session.getProjectId()), false);
                     }
                     if (dimConfig.m_dimCalc.m_dimInfo.m_id == 5082) //approving user)
                        _session.setAttribute(paramName, Integer.toString(_user.getUserId()), false);
                     else if (paramVals != null && paramVals.length != 0) 
                        _session.setAttribute(paramName, paramVals[0],false);
                     if (dimConfig.m_numeric_filter) {
                   	   	 String tempVarNameOperator = paramName + "_operator";
                         String tempVarNameOperandFirst = paramName + "_operand_first";
                         String tempVarNameOperandSecond = paramName + "_operand_second";
                         _session.removeAttribute(tempVarNameOperator);
                         _session.removeAttribute(tempVarNameOperandFirst);
                         _session.removeAttribute(tempVarNameOperandSecond);
                         if (paramVals != null && paramVals.length != 0) {
                        	 _session.setAttribute(tempVarNameOperandFirst, paramVals[0], false);
                         }
                         if (operator != null && operator.length() != 0) {
                        	 _session.setAttribute(tempVarNameOperator, operator, false);
                         }
                         if (operand2 != null && operand2.length() != 0) {
                        	 _session.setAttribute(tempVarNameOperandSecond, operand2, false);
                         }
                     }
                     
                     continue;
                  }
                  if (dimConfig.m_numeric_filter) {
                	  String paramValStored = paramVals == null || paramVals.length == 0 ? null : paramVals[0];
                	  
                	  String tempVarNameOperator = paramName + "_operator";
                      String tempVarNameOperandFirst = paramName + "_operand_first";
                      String tempVarNameOperandSecond = paramName + "_operand_second";
                      
                      _session.removeAttribute(paramName);
                      _session.removeAttribute(tempVarNameOperator);
                      _session.removeAttribute(tempVarNameOperandFirst);
                      _session.removeAttribute(tempVarNameOperandSecond);
                      if (paramValStored != null && paramValStored.length() != 0) {
                    	  _session.setAttribute(tempVarNameOperandFirst, paramValStored, setAsPerm);
                    	  _session.setAttribute(paramName, paramValStored, setAsPerm);
                    	  _session.setAttribute(tempVarNameOperator, operator, setAsPerm);
                    	  _session.setAttribute(tempVarNameOperandSecond, operand2, setAsPerm);
                      }
                      continue;
                  }
                  
                  boolean hasEndDate = false;
                  if (searchButtonPressed) {
                     _session.removeAttribute(paramName);
                     String paramVal = null;
                     for (int t1=0,t1s=paramVals == null ? 0 : paramVals.length;t1<t1s;t1++) {
                         if (t1 == 0)
                            paramVal = paramVals[t1];
                         else {
                            paramVal += ",";
                            paramVal += paramVals[t1];
                         }
                     }
                     if (paramVal != null) {
                         _session.setAttribute(paramName, paramVal, setAsPerm);
                     }
                  }
                  else if (launchInPrjCmpMode) { //being launched to find comparable projects .... if
                      _session.removeAttribute(paramName);
                      int tempDimId = dimConfig.m_dimCalc.m_dimInfo == null ? com.ipssi.gen.utils.Misc.getUndefInt() : dimConfig.m_dimCalc.m_dimInfo.m_id;
                      Integer defaultValInteger = (Integer) cmpPrjInfo.get(new Integer(tempDimId));
                      if (defaultValInteger != null && !Misc.isUndef(defaultValInteger.intValue())) {
                        _session.setAttribute(paramName, defaultValInteger.toString(),false);
                     }
                  }
                  String paramValStored = _session.getAttribute(paramName);
                  if (is123) {//rajeev 012008 .. get val selected whenever && !searchButtonPressed) {
                      ArrayList temp = new ArrayList();
                      if (paramValStored != null)
                          Misc.convertValToVector(paramValStored, temp);
                      if (temp.size() == 0)
                         temp.add(new Integer(Misc.getUndefInt()));
                      boolean changed = false;
                      for (int l=0,ls = temp.size();l<ls;l++) {
                          int defValInt = ((Integer)temp.get(l)).intValue();
                          DimInfo forCheckDim = null; //earlier was dimConfig.m_dimCalc.m_dimInfo;
                          int origDefValInt = defValInt;
                          if (Misc.isUndef(defValInt)) {
                             defValInt = Misc.G_TOP_LEVEL_PORT;
                             forCheckDim = DimInfo.getDimInfo(123);//dimConfig.m_dimCalc.m_dimInfo;
                          }
                              
                          int newDefValInt = _user.getUserSpecificDefaultPort(_session, defValInt, privIdForOrg, forCheckDim);
                          
                          if (newDefValInt != origDefValInt && 1!= Misc.getParamAsInt(request.getParameter("noLogin"))) {
                             changed = true;
                             temp.set(l, new Integer(newDefValInt));
                          }
                      }
                      if (changed) {
                          StringBuilder t2 = new StringBuilder();
                          Misc.convertInListToStr(temp,t2);
                          paramValStored = t2.toString();
                      }
                  }                  
                  
                  
                  if (paramValStored != null && paramValStored.length() != 0) {
                      _session.setAttribute(paramName, paramValStored, setAsPerm);
                      if (addnlParamName != null && !paramName.equals(addnlParamName))
                    	  _session.setAttribute(addnlParamName, paramValStored,false);
                      if ("20872".equals(paramId))
                    	  _session.setAttribute("pv20872", paramValStored, neverSetParamAsPerm ? false : true);
                  }
                  if (dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123) {
                     _session.setAttribute("_cntxt_org", paramValStored, false);
                  }            
                  if (dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 && paramValStored != null && paramValStored.length() != 0) {
                     boolean doPerm =  !("_page_currency".equals(dimConfig.m_paramName)); //only store this permanently
                     _session.setAttribute(dimConfig.m_paramName, paramValStored, neverSetParamAsPerm ? false : doPerm);
                  }            
               } //end of col
               if (currColCount > maxCol)
                  maxCol = currColCount;
          }//end of row
         if (false && !"1".equals(_session.getParameter("ign_vehicle_id"))) {//hack to get around showing details for vehicle_id
        	 String vehicleIds[] = _session.request.getParameterValues("vehicle_id");
        	 if (vehicleIds != null && vehicleIds.length != 0) {
        		 String d9002Name =  topPageContext+9002;
        		 String v9002 = _session.getParameter(d9002Name);
        		 if (v9002 != null && v9002.length() != 0) {
        			 _session.setAttribute(d9002Name+"_old", v9002, false);
        			 v9002 = null;
        		 }
        		 if (v9002 == null || v9002.length() == 0) {
        			 //need to read names from db and put that in session
        			 StringBuilder q = new StringBuilder("select name from vehicle where id in (");
        			 Misc.convertInListToStr(vehicleIds, q);
        			 q.append(")");
        			 PreparedStatement ps1 = _dbConnection.prepareStatement(q.toString());
        			 ResultSet rs1 = ps1.executeQuery();
        			 q.setLength(0);
        			 boolean first = true;
        			 while (rs1.next()) {
        				 if (!first)
        					 q.append(",");
        				 q.append(rs1.getString(1));
        				 first = false;
        			 }
        			 rs1.close();
        			 ps1.close();
        			 _session.setAttribute(d9002Name, q.toString(), false);
        		 }        		 
        	 }
         }
         MiscInner.SearchBoxHelper retval =  new MiscInner.SearchBoxHelper(maxCol, topPageContext, configInfo, valGetter, privIdForOrg);
         MiscInner.ContextInfo contextInfo = Misc.getContextInfo(_session, _dbConnection, _session.getCache(), _session.getLogger(), _session.getUser());
         hackPostProcessSearchBox(retval, _session, contextInfo, neverSetParamAsPerm); //currently will set start/end date if they are needed and any of these are missing
         if (saveResetMode == 1) {
        	 PageHeader.savePagePref(_user,_session, pgContext, topPageContext, configInfo, _dbConnection, true);
         }
         return retval;
      }//end of printSearchBoxGet
      
      public static ArrayList<Integer> g_startDateId = new ArrayList<Integer>();
      public static ArrayList<Integer> g_endDateId = new ArrayList<Integer>();
      static {
    	  g_startDateId.add(20023);
    	  g_startDateId.add(20054);
    	  g_startDateId.add(20056);
    	  g_startDateId.add(20035);
    	  g_startDateId.add(20196);
    	  g_startDateId.add(20183);
    	  g_startDateId.add(20223);
    	  g_startDateId.add(20400);
    	  
    	  g_endDateId.add(20224);
    	  g_endDateId.add(20209);
    	  g_endDateId.add(20197);
    	  g_endDateId.add(20034);
    	  g_endDateId.add(20055);
    	  g_endDateId.add(20057);
    	  g_endDateId.add(20036);
    	  g_endDateId.add(20401);
      }
      public static boolean isStartDateId(int dimId) {
    	  return g_startDateId.indexOf(dimId) != -1;    	  
      }
      
      public static boolean isEndDateId(int dimId) {
    	  return g_endDateId.indexOf(dimId) != -1;    	  
      }
      public static void hackPostProcessSearchBox(MiscInner.SearchBoxHelper boxHelper, SessionManager session, MiscInner.ContextInfo contextInfo, boolean neverSetParamAsPerm) throws Exception {
    	  hackPostProcessSearchBox(boxHelper, session, contextInfo, neverSetParamAsPerm, -1, false, 0);
      }
      public static void hackPostProcessSearchBox(MiscInner.SearchBoxHelper boxHelper, SessionManager session, MiscInner.ContextInfo contextInfo, boolean neverSetParamAsPerm, int granForOutside, boolean ignSessionParamForDate, int deltaForRelative) throws Exception {
    	  String startParamName = null;
    	  String endParamName = null;
    	  
    	  TreeMap<Integer,String> startEndParamNames=new TreeMap<Integer, String>();
    	  String granParamName = null;
    	  String defaultTimeSpecParamName = null;
    	  String doTillDateParamName = null;
    	  String timeForCurParamName = null;
    	  java.util.Date timeForCur = null;
    	  boolean canEndBeyondCurrent = true;

    	  int shiftDimId = 20001;
    	  int altShiftDimId = 81238;
    	  int shiftIdAsked = Misc.getUndefInt();
    	  String shiftParamName = null;
    	  ShiftBean shiftBean = null;
    	  int timeSettingByShiftForCurVal = Misc.getUndefInt();
    	
    	  int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
      	  double timeRelStart = Misc.getUndefDouble();
      	  double timeRelEnd = Misc.getUndefDouble();
   		  FmtI.Date dateFmt = contextInfo.getDateTimeFormetter();
		  SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
		  SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-M-d");
		  SimpleDateFormat indepForma = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	  int mapCounter=0;
    	  for (int i=0,is=boxHelper == null || boxHelper.m_searchParams == null ? 0 : boxHelper.m_searchParams.size();i<is;i++) {
    		  ArrayList rowInfo = (ArrayList)boxHelper.m_searchParams.get(i);
    		  for (int j=0,js=rowInfo.size();j<js;j++) {
    			  DimConfigInfo dimConfig = (DimConfigInfo)rowInfo.get(j);
    			  DimInfo dimInfo = dimConfig != null && dimConfig.m_dimCalc != null ? dimConfig.m_dimCalc.m_dimInfo : null;
    			  if (dimInfo == null)
    				  continue;
    			  int paramId= dimInfo.m_id;
    			  boolean isStart = Misc.isInList(g_startDateId, paramId);
    			  boolean isEnd = !isStart ? Misc.isInList(g_endDateId, paramId) : false;
    			  if (isEnd) {
    				  int t11 = Misc.getParamAsInt(dimConfig.m_default);
    				  if (t11 > 0)
    					  canEndBeyondCurrent = true;
    			  }
    			  boolean isGran = paramId == 20051;
    			  boolean isTillDate = paramId == 20060;
    			  boolean isShiftId = paramId == shiftDimId || paramId == altShiftDimId;
    			  boolean isDefaultTimeSpec = paramId == 20062;
    			  boolean isTimeForCur = paramId == 20073;
    			  boolean isTimeSettingByShiftForCur = paramId == 20074;
    			  boolean isRelevant = isStart || isGran || isEnd || isShiftId || isTillDate || isDefaultTimeSpec || isTimeForCur || isTimeSettingByShiftForCur;
    			  if (isRelevant) {
    				  boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
    				  String paramName = is123 ? "pv123" : 
    					  	dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
    					  			dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : boxHelper.m_topPageContext+dimConfig.m_paramName
            					  :
            						  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : boxHelper.m_topPageContext+paramId
            			  ;
    	              if (isStart) {
                         startParamName = paramName;
                         timeRelStart = Misc.getParamAsDouble(dimConfig.m_default);
                       //  startEndParamNames.put(startParamName, timeRelStart);
                      }
                      else if (isEnd) {
                    	  endParamName = paramName;
                          timeRelEnd = Misc.getParamAsDouble(dimConfig.m_default);
                          StringBuilder val=new StringBuilder();
                          val.append(startParamName).append("=").append(timeRelStart).append(";").append(endParamName).append("=").append(timeRelEnd);
                          startEndParamNames.put(mapCounter,val.toString());
                          mapCounter++;
                      }
                      else if (isGran) {
                    	  granParamName = paramName;
                      }
                      else if (isDefaultTimeSpec) {
                    	  defaultTimeSpecParamName = paramName;
                      }
                      else if (isTillDate) {
                    	  doTillDateParamName = paramName;
                      }
                      else if (isShiftId) {  
                    	  shiftIdAsked = Misc.getParamAsInt(session.getParameter(paramName));
                      }
                      else if (isTimeSettingByShiftForCur) {
                    	  timeSettingByShiftForCurVal = Misc.getParamAsInt(session.getParameter(paramName));
                      }
                      else if (isTimeForCur) {
                    	  String timeForCurStr = session.getParameter(paramName);
                    	  timeForCur = Misc.getParamAsDate(timeForCurStr, null, indepForma);
                		  if (timeForCur == null)
                			  timeForCur = Misc.getParamAsDate(timeForCurStr, null , sdf, sdf1);
                      }
    			  }
    		  }//end of each row
    	  }//end of all searchConfig
    	  
    	  for (Entry<Integer, String> entry : startEndParamNames.entrySet()) {
    		  String split[]=entry.getValue().split(";");
    		  String spn= split[0].split("=")[0];
    		  Double spv=Misc.getParamAsDouble(split[0].split("=")[1]);
    		  String epn= split[1].split("=")[0];
    		  Double epv= Misc.getParamAsDouble(split[1].split("=")[1]);
    		 extendedSearchBoxProcessing(session,spn,epn,granParamName,defaultTimeSpecParamName	,doTillDateParamName,timeForCurParamName,
    				timeForCur,shiftIdAsked , shiftParamName ,shiftBean ,timeSettingByShiftForCurVal,spv ,epv, pv123,neverSetParamAsPerm,granForOutside,
    				ignSessionParamForDate,deltaForRelative,canEndBeyondCurrent) ;
			
		}
    	 /**
    	  
    	  int doTillDate = 0;;
    	  String s11 = null;
    	  if (doTillDateParamName != null) {
    		  s11 = session.getParameter(doTillDateParamName);
    		  doTillDate = Misc.getParamAsInt(s11, doTillDate);
    	  }
    	  //handling of missing start/end param - assumption is that granParam is alway set if needed
    	  int defaultTimeSpec = Misc.getUndefInt();
    	  if (defaultTimeSpecParamName != null) {
    		  defaultTimeSpec = Misc.getParamAsInt(session.getParameter(defaultTimeSpecParamName));    		
    	  }
    	  
    	  if (startParamName != null || endParamName != null || granParamName != null) {
    		  int granParam = Misc.getUndefInt();
    		  granParam = Misc.getParamAsInt(session.getParameter(granParamName));
 			 
			  if(session.getParameter("is_link") != null && "1".equals(session.getParameter("is_link"))){
				  granParam = Misc.getParamAsInt(session.getParameter("tr_analysis20051"), Misc.SCOPE_DAY);
			  }
			  
			  if (!Misc.isUndef(defaultTimeSpec) && Misc.isLHSHigherScope(defaultTimeSpec, granParam)) {
				  granParam = defaultTimeSpec;
			  }
			  if (granForOutside >= 0)
				  granParam = granForOutside;
			  boolean externalTimeForCurProvided = timeForCur != null;
			  if (Misc.isUndef(timeSettingByShiftForCurVal)) {
				  timeSettingByShiftForCurVal = shiftIdAsked;
			  }
			  if (timeSettingByShiftForCurVal == 0) {//want for current shift
				  timeForCur = new java.util.Date();
				  externalTimeForCurProvided = false;
			  }
		
	    	  if (!Misc.isUndef(timeSettingByShiftForCurVal)) {
	    		  int shiftIdForSettingTimeForCur = timeSettingByShiftForCurVal;
	    		  if (timeSettingByShiftForCurVal == 0) {//use shift id of current time
	    			  shiftIdForSettingTimeForCur = ShiftInformation.getFirstShiftId(pv123, session.getConnection(), granParam, new Date());
	    		  }
	    		  if (shiftIdForSettingTimeForCur <= 0)
	    			  shiftIdForSettingTimeForCur = ShiftInformation.getFirstShiftIdInDay(pv123, session.getConnection());
	    		  if (shiftIdForSettingTimeForCur > 0) {
	    			  ShiftBean beanForShiftIdForSettingTimeForCur = ShiftInformation.getShiftById(pv123, shiftIdForSettingTimeForCur, session.getConnection());
	    			  if (beanForShiftIdForSettingTimeForCur != null && timeForCur != null) {
	    				  System.out.println("PageHeader.hackPostProcessSearchBox() beanForShiftIdForSettingTimeForCur : " +beanForShiftIdForSettingTimeForCur + " timeForCur : "+timeForCur);
	    				  timeForCur.setHours(beanForShiftIdForSettingTimeForCur.getStartHour());
	    				  timeForCur.setMinutes(beanForShiftIdForSettingTimeForCur.getStartMin());
	    				  timeForCur.setSeconds(0);
	    			  }
	    		  }
	    	  }

			  if (granParam != Misc.SCOPE_SHIFT) {
				  shiftIdAsked = ShiftInformation.getFirstShiftIdInDay(pv123, session.getConnection());
			  }
			  else if (Misc.isUndef(shiftIdAsked)) {
				  shiftIdAsked = ShiftInformation.getFirstShiftId(pv123, session.getConnection(), granParam, !externalTimeForCurProvided ? new Date() : timeForCur);
				  // guessShiftId(pv123, session, granParam);//if gran is more than equal to day then 1st shift else shift that 
			  }
			  if (!Misc.isUndef(shiftIdAsked))
				  shiftBean = ShiftInformation.getShiftById(pv123, shiftIdAsked, session.getConnection()); 
    		  java.util.Date now = new Date();
    		  String startStr = startParamName == null ? null : Misc.getParamAsString(session.getParameter(startParamName), null);
    		  double relStart = Misc.getParamAsDouble(startStr, timeRelStart);
    		  if (Misc.isUndef(relStart))
    			  relStart = 0;
			  String endStr = endParamName == null ? null : Misc.getParamAsString(session.getParameter(endParamName), null);
    		  double relEnd = Misc.getParamAsDouble(endStr, timeRelEnd);
    		  if (Misc.isUndef(relEnd)) {
    			  relEnd = 1;
    		  }
              boolean emailRelatedReporting = Misc.getParamAsInt(session.getParameter("email_report")) == 1;
              int emailStartHr = 0;
              int emailStartMin = 0;
    		  if (emailRelatedReporting) {
    			  
            	  relStart = Misc.getParamAsDouble(session.getParameter("email_report_relative_start"), 0);
            	  relEnd = Misc.getParamAsDouble(session.getParameter("email_report_relative_end"), 1);
            	  relEnd--;
            	  granParam = Misc.getParamAsInt(session.getParameter("email_report_granularity"), Misc.SCOPE_DAY);
            	  emailStartHr = Misc.getParamAsInt(session.getParameter("email_report_startHr"), 0);
            	  emailStartMin = Misc.getParamAsInt(session.getParameter("email_report_startMin"), 0);
            	  
    			  if (granForOutside >= 0)
    				  granParam = granForOutside;

              }
    		  if (granParam == Misc.SCOPE_HOUR_RELATIVE) {
    			  relStart -= 1;
    			  relEnd -=1;
    		  }
    		  if (!Misc.isUndef(relStart))
    			  relStart += deltaForRelative;
    		  if (!Misc.isUndef(relEnd))
    			  relEnd += deltaForRelative;
    		  java.util.Date startParam = null;
    		  startParam = externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(startStr, null, indepForma);
    		  
    		  if (startParam == null)
    			  startParam = externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(startStr, null , sdf, sdf1);
    		  else {
    			  startStr = sdf.format(startParam);
    			  session.setAttribute(startParamName, startStr, false);
    		  }
    		  java.util.Date endParam = null;
    		  endParam =externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(endStr, null, indepForma);
    		  if (endParam == null)
    			  endParam = externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(endStr, null , sdf, sdf1);
    		  else {
    			  endStr = sdf.format(endParam);
    			  session.setAttribute(endParamName, endStr, false);
    		  }
    		  
			  if (startParam == null || endParam == null) { //to correct stuff
				//TimePeriodHelper.setTimeBegOfDate(startParam);
                  int numHoursInShift = 24;
                  boolean isShiftGran = granParam == Misc.SCOPE_SHIFT;
                  if (isShiftGran && shiftBean != null) { //incomplete for min/hr etc
                 	 int eh = shiftBean.getStopHour()+shiftBean.getStopMin()/60;
                 	 //int em = shiftBean.getStopMin();
                 	 int sh = shiftBean.getStartHour()+shiftBean.getStartMin()/60;
                 	 //int sm = shiftBean.getStartMin();
                 	 numHoursInShift = eh-sh;
                 	 if (numHoursInShift <= 0) {
                 		 numHoursInShift = 24+eh-sh;
                 	 }
                  }
				  if (startParam == null && endParam == null) {
                        startParam = new java.util.Date(externalTimeForCurProvided ? timeForCur.getTime() : now.getTime()); //in prod must by currentTimeMillis
					  
                         if (shiftBean != null)
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam, shiftBean);
                         else
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam);
                         
                         if (!Misc.isUndef(relStart)) {
                        	 //if SHIFT based, then use NumofHoursInShift
                        	 TimePeriodHelper.addScopedDur(startParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, relStart * (isShiftGran ? numHoursInShift : 1));
                         }
                         //TODO
 					    if(emailRelatedReporting && !(granParam == Misc.SCOPE_HOUR || granParam == Misc.SCOPE_HOUR_RELATIVE || granParam == Misc.SCOPE_SHIFT)){
					    	startParam.setHours(emailStartHr);
					    	startParam.setMinutes(emailStartMin);
					    }
                         endParam = new java.util.Date(startParam.getTime());
                         TimePeriodHelper.addScopedDur(endParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, (relEnd-relStart+(emailRelatedReporting ? 0 : 1))*(isShiftGran ? numHoursInShift :1));
                         
                         if (endParam.after(now) && !canEndBeyondCurrent){
                        	 endParam = now;
                        	 if(granParam == Misc.SCOPE_MTD){
                        		endParam.setHours(0);
                        		endParam.setMinutes(0);
                        	 }
                         }
                         endParam = new java.util.Date(endParam.getTime() -1000L);
                         String startParamStr = sdf.format(startParam);
                         String endParamStr = sdf.format(endParam); 
                         session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);
                         session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);                        
                     }
                     else if (startParam != null && endParam == null) {
                    	 endParam = new java.util.Date(startParam.getTime());
                    	 TimePeriodHelper.addScopedDur(endParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, (relEnd-relStart+1)*(isShiftGran ? numHoursInShift :1));
                    	 
                    	 
                    	 if (endParam.after(now) && ! canEndBeyondCurrent){
                           	 endParam = now;
                        	 if(granParam == Misc.SCOPE_MTD){
                        		endParam.setHours(0);
                        		endParam.setMinutes(0);
                        	 }
                    	 }
                    	 endParam = new java.util.Date(endParam.getTime() -1000L);
                         String endParamStr = sdf.format(endParam);  
//                         if(endParamStr != null && (endParamStr.indexOf(" AM") > 0 || endParamStr.indexOf(" PM") > 0))
//                        	 endParamStr = endParamStr.substring(0, endParamStr.indexOf(" AM") > 0 ? endParamStr.indexOf(" AM") 
//                        			 : endParamStr.indexOf(" PM") > 0 ? endParamStr.indexOf(" PM") : endParamStr.length());
                         session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                         
                     }
                     else {//startParam == null && endParam != null
                    	 //startParam = new java.util.Date(endParam.getTime());
                    	 startParam = new java.util.Date(endParam.getTime());
                    	 TimePeriodHelper.addScopedDur(startParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, -1*(relEnd-relStart)*(isShiftGran ? numHoursInShift :1));
                    	 if (shiftBean != null)
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam, shiftBean);
                         else
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam);
                    	 endParam = new java.util.Date(endParam.getTime() - 1000L);

//                    	 TimePeriodHelper.addSeconds(startParam, 1);
                    	 if (endParam.after(now) && !canEndBeyondCurrent) {
                        	 endParam = now;
	                    	 String endParamStr = sdf.format(endParam);  
	                    	 session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                    	 }
                    	 String startParamStr = sdf.format(startParam);    
//                         if(startParamStr != null && (startParamStr.indexOf(" AM") > 0 || startParamStr.indexOf("PM") > 0))
//                        	 startParamStr = startParamStr.substring(0, startParamStr.indexOf(" AM") > 0 ? startParamStr.indexOf(" AM") 
//                        			 : startParamStr.indexOf(" PM") > 0 ? startParamStr.indexOf(" PM") : startParamStr.length());                                              
                         session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);                         
                                               
                     }
    		  }
    		  else {
    			  if (endParam.after(now) && !canEndBeyondCurrent) {
                 	 endParam = now;
                 	 String endParamStr = sdf.format(endParam);  
                 	 session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
             	 }
    		  } 
			  if (doTillDate != 0 && granParam != Misc.SCOPE_SHIFT && granParam != Misc.SCOPE_HOUR) {
				  
	              TimePeriodHelper.setBegOfDate(startParam, granParam);
	              if (doTillDate == 2) {
	            	  TimePeriodHelper.setBegOfDate(endParam, Misc.SCOPE_DAY);
	            	  endParam = new java.util.Date(endParam.getTime() - 1000L);//make it 1 s
	              }
	              if (!startParam.before(endParam)) {
	            	  startParam = new Date(endParam.getTime());
	            	  TimePeriodHelper.setBegOfDate(startParam, granParam);
	              }
	              String startParamStr = sdf.format(startParam);
	              session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);
	              String endParamStr = sdf.format(endParam); 
	              session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
			  }



    	  }//end of doing special case for startParamName/endParam etc
    	  */
    	
      }
      
      public static void extendedSearchBoxProcessing(SessionManager session,String startParamName,String endParamName,String granParamName,String defaultTimeSpecParamName
    		  		,String doTillDateParamName,String timeForCurParamName,java.util.Date timeForCur,int shiftIdAsked
, String shiftParamName ,ShiftBean shiftBean ,int timeSettingByShiftForCurVal, double timeRelStart ,double timeRelEnd,
int pv123, boolean neverSetParamAsPerm, int granForOutside, boolean ignSessionParamForDate, int deltaForRelative, boolean canEndBeyondCurrent) throws Exception{
    	  SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
		  SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-M-d");
		  SimpleDateFormat indepForma = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		  
    	  int doTillDate = 0;
    	  String s11 = null;
    	  if (doTillDateParamName != null) {
    		  s11 = session.getParameter(doTillDateParamName);
    		  doTillDate = Misc.getParamAsInt(s11, doTillDate);
    	  }
    	  //handling of missing start/end param - assumption is that granParam is alway set if needed
    	  int defaultTimeSpec = Misc.getUndefInt();
    	  if (defaultTimeSpecParamName != null) {
    		  defaultTimeSpec=Misc.getParamAsInt(session.getParameter(defaultTimeSpecParamName));
    	  }
    	  
    	  if (startParamName != null || endParamName != null || granParamName != null) {
    		  int granParam = Misc.getUndefInt();
    		  granParam = Misc.getParamAsInt(session.getParameter(granParamName));
 			 
			  if(session.getParameter("is_link") != null && "1".equals(session.getParameter("is_link"))){
				  granParam = Misc.getParamAsInt(session.getParameter("tr_analysis20051"), Misc.SCOPE_DAY);
			  }
			  
			  if (!Misc.isUndef(defaultTimeSpec) && Misc.isLHSHigherScope(defaultTimeSpec, granParam)) {
				  granParam = defaultTimeSpec;
			  }
			  if (granForOutside >= 0)
				  granParam = granForOutside;
			  boolean externalTimeForCurProvided = timeForCur != null;
			  if (Misc.isUndef(timeSettingByShiftForCurVal)) {
				  timeSettingByShiftForCurVal = shiftIdAsked;
			  }
			  if (timeSettingByShiftForCurVal == 0) {//want for current shift
				  timeForCur = new java.util.Date();
				  externalTimeForCurProvided = false;
			  }
		
	    	  if (!Misc.isUndef(timeSettingByShiftForCurVal)) {
	    		  int shiftIdForSettingTimeForCur = timeSettingByShiftForCurVal;
	    		  if (timeSettingByShiftForCurVal == 0) {//use shift id of current time
	    			  shiftIdForSettingTimeForCur = ShiftInformation.getFirstShiftId(pv123, session.getConnection(), granParam, new Date());
	    		  }
	    		  if (shiftIdForSettingTimeForCur <= 0)
	    			  shiftIdForSettingTimeForCur = ShiftInformation.getFirstShiftIdInDay(pv123, session.getConnection());
	    		  if (shiftIdForSettingTimeForCur > 0) {
	    			  ShiftBean beanForShiftIdForSettingTimeForCur = ShiftInformation.getShiftById(pv123, shiftIdForSettingTimeForCur, session.getConnection());
	    			  if (beanForShiftIdForSettingTimeForCur != null && timeForCur != null) {
	    				  System.out.println("PageHeader.hackPostProcessSearchBox() beanForShiftIdForSettingTimeForCur : " +beanForShiftIdForSettingTimeForCur + " timeForCur : "+timeForCur);
	    				  timeForCur.setHours(beanForShiftIdForSettingTimeForCur.getStartHour());
	    				  timeForCur.setMinutes(beanForShiftIdForSettingTimeForCur.getStartMin());
	    				  timeForCur.setSeconds(0);
	    			  }
	    		  }
	    	  }

			  if (granParam != Misc.SCOPE_SHIFT) {
				  shiftIdAsked = ShiftInformation.getFirstShiftIdInDay(pv123, session.getConnection());
			  }
			  else if (Misc.isUndef(shiftIdAsked)) {
				  shiftIdAsked = ShiftInformation.getFirstShiftId(pv123, session.getConnection(), granParam, !externalTimeForCurProvided ? new Date() : timeForCur);
				  // guessShiftId(pv123, session, granParam);//if gran is more than equal to day then 1st shift else shift that 
			  }
			  if (!Misc.isUndef(shiftIdAsked))
				  shiftBean = ShiftInformation.getShiftById(pv123, shiftIdAsked, session.getConnection()); 
    		  java.util.Date now = new Date();
    		  String startStr = startParamName == null ? null : Misc.getParamAsString(session.getParameter(startParamName), null);
    		  double relStart = Misc.getParamAsDouble(startStr, timeRelStart);
    		  if (Misc.isUndef(relStart))
    			  relStart = 0;
			  String endStr = endParamName == null ? null : Misc.getParamAsString(session.getParameter(endParamName), null);
    		  double relEnd = Misc.getParamAsDouble(endStr, timeRelEnd);
    		  if (Misc.isUndef(relEnd)) {
    			  relEnd = 1;
    		  }
              boolean emailRelatedReporting = Misc.getParamAsInt(session.getParameter("email_report")) == 1;
              int emailStartHr = 0;
              int emailStartMin = 0;
    		  if (emailRelatedReporting) {
    			  
            	  relStart = Misc.getParamAsDouble(session.getParameter("email_report_relative_start"), 0);
            	  relEnd = Misc.getParamAsDouble(session.getParameter("email_report_relative_end"), 1);
            	  relEnd--;
            	  granParam = Misc.getParamAsInt(session.getParameter("email_report_granularity"), Misc.SCOPE_DAY);
            	  emailStartHr = Misc.getParamAsInt(session.getParameter("email_report_startHr"), 0);
            	  emailStartMin = Misc.getParamAsInt(session.getParameter("email_report_startMin"), 0);
            	  /*if(granParam != Misc.SCOPE_MTD)
            		  relStart = relStart - 1;
            	  relEnd = relStart + 1;*/
    			  if (granForOutside >= 0)
    				  granParam = granForOutside;

              }
    		  if (granParam == Misc.SCOPE_HOUR_RELATIVE) {
    			  relStart -= 1;
    			  relEnd -=1;
    		  }
    		  if (!Misc.isUndef(relStart))
    			  relStart += deltaForRelative;
    		  if (!Misc.isUndef(relEnd))
    			  relEnd += deltaForRelative;
    		  java.util.Date startParam = null;
    		  startParam = externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(startStr, null, indepForma);
    		  
    		  if (startParam == null)
    			  startParam = externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(startStr, null , sdf, sdf1);
    		  else {
    			  startStr = sdf.format(startParam);
    			  session.setAttribute(startParamName, startStr, false);
    		  }
    		  java.util.Date endParam = null;
    		  endParam =externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(endStr, null, indepForma);
    		  if (endParam == null)
    			  endParam = externalTimeForCurProvided || ignSessionParamForDate ? null : Misc.getParamAsDate(endStr, null , sdf, sdf1);
    		  else {
    			  endStr = sdf.format(endParam);
    			  session.setAttribute(endParamName, endStr, false);
    		  }
    		  
			  if (startParam == null || endParam == null) { //to correct stuff
				//TimePeriodHelper.setTimeBegOfDate(startParam);
                  int numHoursInShift = 24;
                  boolean isShiftGran = granParam == Misc.SCOPE_SHIFT;
                  if (isShiftGran && shiftBean != null) { //incomplete for min/hr etc
                 	 int eh = shiftBean.getStopHour()+shiftBean.getStopMin()/60;
                 	 //int em = shiftBean.getStopMin();
                 	 int sh = shiftBean.getStartHour()+shiftBean.getStartMin()/60;
                 	 //int sm = shiftBean.getStartMin();
                 	 numHoursInShift = eh-sh;
                 	 if (numHoursInShift <= 0) {
                 		 numHoursInShift = 24+eh-sh;
                 	 }
                  }
				  if (startParam == null && endParam == null) {
                        startParam = new java.util.Date(externalTimeForCurProvided ? timeForCur.getTime() : now.getTime()); //in prod must by currentTimeMillis
					  
                         if (shiftBean != null)
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam, shiftBean);
                         else
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam);
                         
                         if (!Misc.isUndef(relStart)) {
                        	 //if SHIFT based, then use NumofHoursInShift
                        	 TimePeriodHelper.addScopedDur(startParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, relStart * (isShiftGran ? numHoursInShift : 1));
                         }
                         //TODO
 					    if(emailRelatedReporting && !(granParam == Misc.SCOPE_HOUR || granParam == Misc.SCOPE_HOUR_RELATIVE || granParam == Misc.SCOPE_SHIFT)){
					    	startParam.setHours(emailStartHr);
					    	startParam.setMinutes(emailStartMin);
					    }
                         endParam = new java.util.Date(startParam.getTime());
                         TimePeriodHelper.addScopedDur(endParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, (relEnd-relStart+(emailRelatedReporting ? 0 : 1))*(isShiftGran ? numHoursInShift :1));
                         
                         if (endParam.after(now) && !canEndBeyondCurrent){
                        	 endParam = now;
                        	 if(granParam == Misc.SCOPE_MTD){
                        		endParam.setHours(0);
                        		endParam.setMinutes(0);
                        	 }
                         }
                         endParam = new java.util.Date(endParam.getTime() -1000L);
                         String startParamStr = sdf.format(startParam);
                         String endParamStr = sdf.format(endParam); 
                         session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);
                         session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);                        
                     }
                     else if (startParam != null && endParam == null) {
                    	 endParam = new java.util.Date(startParam.getTime());
                    	 TimePeriodHelper.addScopedDur(endParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, (relEnd-relStart+1)*(isShiftGran ? numHoursInShift :1));
                    	 
                    	 
                    	 if (endParam.after(now) && ! canEndBeyondCurrent){
                           	 endParam = now;
                        	 if(granParam == Misc.SCOPE_MTD){
                        		endParam.setHours(0);
                        		endParam.setMinutes(0);
                        	 }
                    	 }
                    	 endParam = new java.util.Date(endParam.getTime() -1000L);
                         String endParamStr = sdf.format(endParam);  
//                         if(endParamStr != null && (endParamStr.indexOf(" AM") > 0 || endParamStr.indexOf(" PM") > 0))
//                        	 endParamStr = endParamStr.substring(0, endParamStr.indexOf(" AM") > 0 ? endParamStr.indexOf(" AM") 
//                        			 : endParamStr.indexOf(" PM") > 0 ? endParamStr.indexOf(" PM") : endParamStr.length());
                         session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                         
                     }
                     else {//startParam == null && endParam != null
                    	 //startParam = new java.util.Date(endParam.getTime());
                    	 startParam = new java.util.Date(endParam.getTime());
                    	 TimePeriodHelper.addScopedDur(startParam, isShiftGran ? Misc.SCOPE_HOUR : granParam, -1*(relEnd-relStart)*(isShiftGran ? numHoursInShift :1));
                    	 if (shiftBean != null)
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam, shiftBean);
                         else
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam);
                    	 endParam = new java.util.Date(endParam.getTime() - 1000L);

//                    	 TimePeriodHelper.addSeconds(startParam, 1);
                    	 if (endParam.after(now) && !canEndBeyondCurrent) {
                        	 endParam = now;
	                    	 String endParamStr = sdf.format(endParam);  
	                    	 session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                    	 }
                    	 String startParamStr = sdf.format(startParam);    
//                         if(startParamStr != null && (startParamStr.indexOf(" AM") > 0 || startParamStr.indexOf("PM") > 0))
//                        	 startParamStr = startParamStr.substring(0, startParamStr.indexOf(" AM") > 0 ? startParamStr.indexOf(" AM") 
//                        			 : startParamStr.indexOf(" PM") > 0 ? startParamStr.indexOf(" PM") : startParamStr.length());                                              
                         session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);                         
                                               
                     }
    		  }
    		  else {
    			  if (endParam.after(now) && !canEndBeyondCurrent) {
                 	 endParam = now;
                 	 String endParamStr = sdf.format(endParam);  
                 	 session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
             	 }
    		  } 
			  if (doTillDate != 0 && granParam != Misc.SCOPE_SHIFT && granParam != Misc.SCOPE_HOUR) {
				  
	              TimePeriodHelper.setBegOfDate(startParam, granParam);
	              if (doTillDate == 2) {
	            	  TimePeriodHelper.setBegOfDate(endParam, Misc.SCOPE_DAY);
	            	  endParam = new java.util.Date(endParam.getTime() - 1000L);//make it 1 s
	              }
	              if (!startParam.before(endParam)) {
	            	  startParam = new Date(endParam.getTime());
	            	  TimePeriodHelper.setBegOfDate(startParam, granParam);
	              }
	              String startParamStr = sdf.format(startParam);
	              session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);
	              String endParamStr = sdf.format(endParam); 
	              session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
			  }



    	  }//end of doing special case for startParamName/endParam etc
      }
      
      
      
      public static void hackPostProcessSearchBoxOrig(MiscInner.SearchBoxHelper boxHelper, SessionManager session, MiscInner.ContextInfo contextInfo, boolean neverSetParamAsPerm) throws Exception {
    	  String startParamName = null;
    	  String endParamName = null;
    	  String granParamName = null;
    	  String defaultTimeSpecParamName = null;
    	  String altStartParamName = null;
    	  String altEndParamName = null;
    	  String altGranParamName = null;
    	  String altDefaultTimeSpecParamName = null;
    	  String doTillDateParamName = null;
    	  String altTillDateParamName = null;
    	  boolean isShiftSchedRelated = false;
    	  boolean canEndBeyondCurrent = false;
    	  int shiftDimId = 20001;
    	  int shiftIdAsked = Misc.getUndefInt();
    	  String shiftParamName = null;
    	  String addnlShiftParamName = null;
    	  ShiftBean shiftBean = null;
    	  
    	  boolean retainPreviousAsMuchAsPossible = "1".equals(session.getParameter("_from_link"));
          //if retain  then will try to get parameters also from prevTopPageContext
          String prevTopPageContext = Misc.getParamAsString(session.getParameter("_prev_top_page_context"),null);
          if (!retainPreviousAsMuchAsPossible)
         	 prevTopPageContext = null;
        String retainStartParamName = null;
        String retainEndParamName = null;
        String retainShiftParamName = null;
        String retainGranParamName = null;
        String retainDefaultTimeSpecParamName = null;
        
    	  int pv123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
      	  double timeRelStart = Misc.getUndefDouble();
      	  double timeRelEnd = Misc.getUndefDouble();
    	  
    	  for (int i=0,is=boxHelper == null || boxHelper.m_searchParams == null ? 0 : boxHelper.m_searchParams.size();i<is;i++) {
    		  ArrayList rowInfo = (ArrayList)boxHelper.m_searchParams.get(i);
    		  for (int j=0,js=rowInfo.size();j<js;j++) {
    			  DimConfigInfo dimConfig = (DimConfigInfo)rowInfo.get(j);
    			  DimInfo dimInfo = dimConfig != null && dimConfig.m_dimCalc != null ? dimConfig.m_dimCalc.m_dimInfo : null;
    			  if (dimInfo == null)
    				  continue;
    			  int paramId= dimInfo.m_id;
    			  boolean isStart = Misc.isInList(g_startDateId, paramId);
    			  boolean isEnd = !isStart ? Misc.isInList(g_endDateId, paramId) : false;
    			  if (isEnd) {
    				  int t11 = Misc.getParamAsInt(dimConfig.m_default);
    				  if (t11 > 0)
    					  canEndBeyondCurrent = true;
    				  if (dimConfig.m_canEndBeyondCurrent)
    					  canEndBeyondCurrent = true;
    			  }
    			  boolean isGran = paramId == 20051;
    			  boolean isTillDate = paramId == 20060;
    			  boolean isShiftId = paramId == shiftDimId;
    			  boolean isDefaultTimeSpec = paramId == 20062;
      			  isShiftSchedRelated = isShiftSchedRelated || dimInfo.m_colMap != null ? "shift_schedule_info".equals(dimInfo.m_colMap.table) : false;
    			  boolean isRelevant = isStart || isGran || isEnd || isShiftId || isTillDate || isDefaultTimeSpec;
    			  if (isRelevant) {
    				  boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
    				  String paramName = is123 ? "pv123" : 
    					  	dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
    					  			dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : boxHelper.m_topPageContext+dimConfig.m_paramName
            					  :
            						  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : boxHelper.m_topPageContext+paramId
            			  ;
    				  String addnlParamName = boxHelper.m_topPageContext+paramId; //essentially everywhere else this was supposed to be the way to access the values, therefore these will also be populated if necessary
    				  String retainParamName = prevTopPageContext == null ? null : prevTopPageContext+paramId;
                      if (isStart) {
                         startParamName = paramName;
                         altStartParamName = addnlParamName;
                         retainStartParamName = retainParamName;
                         timeRelStart = Misc.getParamAsDouble(dimConfig.m_default);
                      }
                      else if (isEnd) {
                    	  endParamName = paramName;
                          altEndParamName = addnlParamName;
                          retainEndParamName = retainParamName;
                          timeRelEnd = Misc.getParamAsDouble(dimConfig.m_default);
                      }
                      else if (isGran) {
                    	  granParamName = paramName;
                          altGranParamName = addnlParamName;
                          retainGranParamName = retainParamName;
                      }
                      else if (isDefaultTimeSpec) {
                    	  defaultTimeSpecParamName = paramName;
                    	  altDefaultTimeSpecParamName = addnlParamName;
                    	  retainDefaultTimeSpecParamName = retainParamName;
                      }
                      else if (isTillDate) {
                    	  doTillDateParamName = paramName;
                    	  altTillDateParamName = addnlParamName;
                      }
                      else if (isShiftId) {  
                    	  shiftIdAsked = Misc.getParamAsInt(session.getParameter(paramName));
                    	  if (Misc.isUndef(shiftIdAsked))
                    		  shiftIdAsked = Misc.getParamAsInt(session.getParameter(addnlParamName));
                    	  if (Misc.isUndef(shiftIdAsked) && retainParamName != null)
                    		  shiftIdAsked = Misc.getParamAsInt(session.getParameter(retainParamName));
                      }
    			  }
    		  }//end of each row
    	  }//end of all searchConfig
    	  
    	  int doTillDate = 0;;
    	  String s11 = null;
    	  if (doTillDateParamName != null) {
    		  s11 = session.getParameter(doTillDateParamName);
    		  if ((s11 == null || s11.length() != 0) && altTillDateParamName != null) {
    			  s11 = session.getParameter(altTillDateParamName);
    		  }
    		  doTillDate = Misc.getParamAsInt(s11, doTillDate);
    	  }
    	  //handling of missing start/end param - assumption is that granParam is alway set if needed
    	  int defaultTimeSpec = Misc.getUndefInt();
    	  if (defaultTimeSpecParamName != null) {
    		  Misc.getParamAsInt(session.getParameter(defaultTimeSpecParamName));
    		  if (Misc.isUndef(defaultTimeSpec))
    			  defaultTimeSpec = Misc.getParamAsInt(session.getParameter(altDefaultTimeSpecParamName));
    		  if (Misc.isUndef(defaultTimeSpec))
    			  defaultTimeSpec = Misc.getParamAsInt(session.getParameter(retainDefaultTimeSpecParamName));
    	  }
    	  
    	  if (startParamName != null || endParamName != null || granParamName != null) {
    		  int granParam = Misc.getUndefInt();
			  if(session.getParameter("is_link") != null && "1".equals(session.getParameter("is_link"))){
				  granParam = Misc.getParamAsInt(session.getParameter("tr_analysis20051"));
			  }
			  if(Misc.isUndef(granParam)) {
				  String  s1 = retainGranParamName == null ? null : Misc.getParamAsString(session.getParameter(retainGranParamName), null);
				  String s2 = granParamName == null ? null : Misc.getParamAsString(session.getParameter(granParamName), null);
				  if (s2 == null && altGranParamName != null)
					  s2 = Misc.getParamAsString(session.getParameter(altGranParamName), null);
				  if (s2 == null)
					  s2 = s1;
    		      granParam = Misc.getParamAsInt(s2, Misc.SCOPE_DAY);
			  }
			  if (!Misc.isUndef(defaultTimeSpec) && Misc.isLHSHigherScope(defaultTimeSpec, granParam)) {
				  granParam = defaultTimeSpec;
			  }
			  if (granParam != Misc.SCOPE_SHIFT)
				  shiftIdAsked = Misc.getUndefInt();
			  if (Misc.isUndef(shiftIdAsked)) {
				  shiftIdAsked = guessShiftId(pv123, session, granParam);//if gran is more than equal to day then 1st shift else shift that 
				 //encompasses current time.
			  }
			  if (!Misc.isUndef(shiftIdAsked))
				  shiftBean = ShiftInformation.getShiftById(pv123, shiftIdAsked, session.getConnection()); 
    		  java.util.Date now = new Date();
    		  String  s1 = retainStartParamName == null ? null : Misc.getParamAsString(session.getParameter(retainStartParamName), null);
			  String s2 = startParamName == null ? null : Misc.getParamAsString(session.getParameter(startParamName), null);
			  if (s2 == null &&  altStartParamName != null)
				  s2 = Misc.getParamAsString(session.getParameter(altStartParamName), null);
			  if (s2 == null)
				  s2 = s1;
			  String startStr = s2;
    		  double relStart = Misc.getParamAsDouble(startStr, timeRelStart);
    		  if (Misc.isUndef(relStart))
    			  relStart = 0;
    		  s1 = retainEndParamName == null ? null : Misc.getParamAsString(session.getParameter(retainEndParamName), null);
			  s2 = endParamName == null ? null : Misc.getParamAsString(session.getParameter(endParamName), null);
			  if (s2 == null && altEndParamName != null)
				  s2 = Misc.getParamAsString(session.getParameter(altEndParamName), null);
			  if (s2 == null)
				  s2 = s1;
			  String endStr = s2;
    		  double relEnd = Misc.getParamAsDouble(endStr, timeRelEnd);
    		  if (Misc.isUndef(relEnd)) {
    			  relEnd = 1;
    		  }
              boolean emailRelatedReporting = Misc.getParamAsInt(session.getParameter("email_report")) == 1;
    		  if(emailRelatedReporting){
            	  relStart = Misc.getParamAsDouble(session.getParameter("email_report_relative_start"), 0);
            	  if(granParam != Misc.SCOPE_MTD)
            		  relStart = relStart - 1;
            	  relEnd = relStart + 1;
              }
    		  if (granParam == Misc.SCOPE_HOUR_RELATIVE) {
    			  relStart -= 1;
    			  relEnd -=1;
    		  }
       		  FmtI.Date dateFmt = contextInfo.getDateTimeFormetter();
    		  SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy HH:mm");
    		  SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-M-d");
    		  SimpleDateFormat indepForma = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    		  java.util.Date startParam = null;
    		  startParam = Misc.getParamAsDate(startStr, null, indepForma);
    		  if (startParam == null)
    			  startParam = Misc.getParamAsDate(startStr, null , sdf, sdf1);
    		  else {
    			  startStr = sdf.format(startParam);
    			  session.setAttribute(startParamName, startStr, false);
    		  }
    		  java.util.Date endParam = null;
    		  endParam = Misc.getParamAsDate(endStr, null, indepForma);
    		  if (endParam == null)
    			  endParam = Misc.getParamAsDate(endStr, null , sdf, sdf1);
    		  else {
    			  endStr = sdf.format(endParam);
    			  session.setAttribute(endParamName, endStr, false);
    		  }
    		  
			  if (startParam == null || endParam == null) { //to correct stuff    			     
				  if (startParam == null && endParam == null) {
                        startParam = new java.util.Date(System.currentTimeMillis()); //in prod must by currentTimeMillis
					  
                         if (shiftBean != null)
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam, shiftBean);
                         else
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam);
                         //TimePeriodHelper.setTimeBegOfDate(startParam);
                         if (!Misc.isUndef(relStart)) {
                        	 TimePeriodHelper.addScopedDur(startParam, granParam, isShiftSchedRelated? - 1 : relStart);
                         }
                         else if (isShiftSchedRelated) {
                        	 Misc.addDays(startParam, -1);
                         }
                         endParam = new java.util.Date(startParam.getTime());
                         if (granParam == Misc.SCOPE_SHIFT && shiftBean != null) { //incomplete for min/hr etc
                        	 int eh = shiftBean.getStopHour()+shiftBean.getStopMin()/60;
                        	 //int em = shiftBean.getStopMin();
                        	 int sh = shiftBean.getStartHour()+shiftBean.getStartMin()/60;
                        	 //int sm = shiftBean.getStartMin();
                        	 int hrsgap = eh-sh;
                        	 if (hrsgap <= 0) {
                        		 hrsgap = 24-eh+sh;
                        	 }
                        	 TimePeriodHelper.addScopedDur(endParam, Misc.SCOPE_HOUR, (relEnd-relStart)*hrsgap);
                         }
                         else
                        	 TimePeriodHelper.addScopedDur(endParam, granParam, isShiftSchedRelated ? 3 : Misc.isUndef(relEnd) ? 1 : relEnd - relStart);
                         if (endParam.after(now) && !canEndBeyondCurrent){
                        	 endParam = now;
                        	 if(granParam == Misc.SCOPE_MTD){
                        		endParam.setHours(0);
                        		endParam.setMinutes(0);
                        	 }
                         }
                         endParam = new java.util.Date(endParam.getTime() -1000L);
                         String startParamStr = sdf.format(startParam);
                         String endParamStr = sdf.format(endParam); 
                         session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);
                         session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                         if (startParamName != null && !startParamName.equals(altStartParamName))
                        	 session.setAttribute(altStartParamName, startParamStr, neverSetParamAsPerm ? false : true);
                         if (endParamName != null && !endParamName.equals(altEndParamName))
                        	 session.setAttribute(altEndParamName, endParamStr, neverSetParamAsPerm ? false : true);
                     }
                     else if (startParam != null && endParam == null) {
                    	 endParam = new java.util.Date(startParam.getTime());
                    	 TimePeriodHelper.addScopedDur(endParam, granParam, isShiftSchedRelated ? 1 : relEnd - relStart);
//                    	 TimePeriodHelper.addSeconds(endParam, -1);
                    	 if(granParam == Misc.SCOPE_SHIFT && shiftBean != null)
                         {
                        	 endParam.setHours(shiftBean.getStopHour()); 
                             endParam.setMinutes(shiftBean.getStopMin());
                         }
                    	 endParam = new java.util.Date(endParam.getTime() -1000L);
                    	 if (endParam.after(now) && ! canEndBeyondCurrent){
                           	 endParam = now;
                        	 if(granParam == Misc.SCOPE_MTD){
                        		endParam.setHours(0);
                        		endParam.setMinutes(0);
                        	 }
                    	 }
                         String endParamStr = sdf.format(endParam);  
//                         if(endParamStr != null && (endParamStr.indexOf(" AM") > 0 || endParamStr.indexOf(" PM") > 0))
//                        	 endParamStr = endParamStr.substring(0, endParamStr.indexOf(" AM") > 0 ? endParamStr.indexOf(" AM") 
//                        			 : endParamStr.indexOf(" PM") > 0 ? endParamStr.indexOf(" PM") : endParamStr.length());
                         session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                         
                         if (!endParamName.equals(altEndParamName))
                        	 session.setAttribute(altEndParamName, endParamStr, neverSetParamAsPerm ? false : true);
                     }
                     else {//startParam == null && endParam != null
                    	 //startParam = new java.util.Date(endParam.getTime());
                    	 startParam = new java.util.Date(endParam.getTime());
                    	 TimePeriodHelper.addScopedDur(startParam, granParam, isShiftSchedRelated ? -3 : relStart-relEnd);
                    	 if (shiftBean != null)
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam, shiftBean);
                         else
                        	 TimePeriodHelper.setBegOfDate(startParam, granParam);
                    	 
//                    	 TimePeriodHelper.addSeconds(startParam, 1);
                    	 endParam = new java.util.Date(endParam.getTime() - 1000L);
                    	 if (endParam.after(now) && !canEndBeyondCurrent) {
                        	 endParam = now;
	                    	 String endParamStr = sdf.format(endParam);  
	                    	 session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
                    	 }
                    	 String startParamStr = sdf.format(startParam);    
//                         if(startParamStr != null && (startParamStr.indexOf(" AM") > 0 || startParamStr.indexOf("PM") > 0))
//                        	 startParamStr = startParamStr.substring(0, startParamStr.indexOf(" AM") > 0 ? startParamStr.indexOf(" AM") 
//                        			 : startParamStr.indexOf(" PM") > 0 ? startParamStr.indexOf(" PM") : startParamStr.length());                                              
                         session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);                         
                         if (!startParamName.equals(altStartParamName))
                        	 session.setAttribute(altStartParamName, startParamStr, neverSetParamAsPerm ? false : true);                        
                     }
    		  }
    		  else {
    			  if (endParam.after(now) && !canEndBeyondCurrent) {
                 	 endParam = now;
                 	 String endParamStr = sdf.format(endParam);  
                 	 session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
             	 }
    		  } 
			  if (doTillDate != 0 && granParam != Misc.SCOPE_SHIFT && granParam != Misc.SCOPE_HOUR) {
				  
	              TimePeriodHelper.setBegOfDate(startParam, granParam);
	              if (doTillDate == 2) {
	            	  TimePeriodHelper.setBegOfDate(endParam, Misc.SCOPE_DAY);
	            	  endParam = new java.util.Date(endParam.getTime() - 1000L);//make it 1 s
	              }
	              if (!startParam.before(endParam)) {
	            	  startParam = new Date(endParam.getTime());
	            	  TimePeriodHelper.setBegOfDate(startParam, granParam);
	              }
	              String startParamStr = sdf.format(startParam);
	              session.setAttribute(startParamName, startParamStr, neverSetParamAsPerm ? false : true);
	              if (startParamName != null && !startParamName.equals(altStartParamName))
	              	 session.setAttribute(altStartParamName, startParamStr, neverSetParamAsPerm ? false : true);
	              String endParamStr = sdf.format(endParam); 
	              session.setAttribute(endParamName, endParamStr, neverSetParamAsPerm ? false : true);
	              if (endParamName != null && !endParamName.equals(altEndParamName))
	             	 session.setAttribute(altEndParamName, endParamStr, neverSetParamAsPerm ? false : true);
			  }



    	  }//end of doing special case for startParamName/endParam etc
      }
      
      private static int guessShiftId(int pv123,SessionManager session, int granParam) throws Exception {
    	return ShiftInformation.getFirstShiftId(pv123, session.getConnection(), granParam);
      }
	public static StringBuilder printSearchBox(SessionManager _session,  MiscInner.SearchBoxHelper searchBoxHelper, String searchLabel, MiscInner.ContextInfo contextInfo) throws Exception {
    	  StringBuilder outp = new StringBuilder();
    	  printSearchBox(_session, searchBoxHelper.m_privIdForOrg, null,  searchBoxHelper.m_searchParams, searchBoxHelper.m_valGetter, searchBoxHelper, outp, null, searchLabel, true, null, contextInfo);
    	  return outp;
      }
      
      public static void printSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, String addnlClauseFor123, String searchLabel, boolean printBorder, StringBuilder convertedForAWB, MiscInner.ContextInfo contextInfo) throws Exception {//pgContext is not used
    	  printSearchBox(_session, privIdForOrg, pgContext, configInfo, valGetter, searchBoxHelper, outp, addnlClauseFor123, searchLabel, printBorder, convertedForAWB, contextInfo, null);
      }
      public static void printSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, String addnlClauseFor123, String searchLabel, boolean printBorder, StringBuilder convertedForAWB, MiscInner.ContextInfo contextInfo,String onChangeHandler) throws Exception {//pgContext is not used
    	  printSearchBox(_session, privIdForOrg, pgContext, configInfo, valGetter, searchBoxHelper, outp, addnlClauseFor123, searchLabel, printBorder, convertedForAWB, contextInfo,onChangeHandler, null, null, false);
      }  
      public static void printSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, String addnlClauseFor123, String searchLabel, boolean printBorder, StringBuilder convertedForAWB, MiscInner.ContextInfo contextInfo,String onChangeHandler, String addnlSearchButton, String dimsRetainForAddnlSearchButton) throws Exception {//pgContext is not used
          printSearchBox(_session, privIdForOrg, pgContext, configInfo, valGetter, searchBoxHelper, outp, addnlClauseFor123, searchLabel, printBorder, convertedForAWB, contextInfo, onChangeHandler, addnlSearchButton, dimsRetainForAddnlSearchButton, false);

      }
      
      	
      public static void printSearchBox(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, String addnlClauseFor123, String searchLabel, boolean printBorder, StringBuilder convertedForAWB, MiscInner.ContextInfo contextInfo,String onChangeHandler, String addnlSearchButton, String dimsRetainForAddnlSearchButton, boolean toHide) throws Exception {//pgContext is not used
    	  //regularOrHTMLOrText
          try {  
        	  
              int projectId = (int) _session.getProjectId();
              String separator = "&";
              User _user = _session.getUser();
              Connection _dbConnection = _session.getConnection();
              Cache _cache = _session.getCache();
              HttpServletRequest request = _session.request;
              int pickPrjModeOrigOrg = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("pick_prj_mode_org"));
             
              if (configInfo == null || configInfo.size() == 0)
                  return;
              int specialMode = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("special_mode"));
              boolean launchInPrjPickMode = specialMode == 1;
              boolean launchInPrjCmpMode = specialMode == 2;
              boolean launchInWorkflowMode = specialMode == 3;
              int hackAnyVal = Misc.G_HACKANYVAL;
                
              ArrayList otherSels = new ArrayList();
              if (searchBoxHelper == null)
                 return;
              int maxCol = searchBoxHelper.m_maxCol;
              String topPageContext = searchBoxHelper.m_topPageContext;
              
              if (printBorder) {
                  outp.append("<table ID='_searchBox' style='").append(toHide ? "display:none;" : "").append("margin-left:15px' border='1px' cellspacing='0' cellpadding='0' bordercolor='#003366' >")
	                    .append("<tr>")
		                  .append("<td bordercolor='ffffff'>");
              }
              boolean doingNoTop = "1".equals(_session.getParameter("_no_top_level"));
              boolean printGoInSameRow = configInfo != null && configInfo.size() == 1;
              outp.append("<table ").append(printBorder ? "" : " ID='_searchBox' ").append(!printBorder && toHide ? "style='display:none'" : "").append(" border='0' cellspacing='0' cellpadding='3' >");
              for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            	  //check if all cols are hidden
            	  ArrayList rowInfo = (ArrayList) configInfo.get(i);
            	  boolean hasNonHiddenCol = false;
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
            		  if (dimConfig != null && !dimConfig.m_hidden) {
            			  hasNonHiddenCol = true;
            			  break;
            		  }
            	  }
            	  if (!hasNonHiddenCol)
            		  continue;
                  outp.append("<tr>")
                      .append("<td width='6'  class='sh'>&nbsp;</td>");
                  
                  int colSize = rowInfo.size();
                  int totColSpan = maxCol*2+1;
                  int colSpanPrinted = 1;
                  for (int j=0;j<colSize;j++) {
                      com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                      if (dimConfig.m_hidden)
                          continue;
                      if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0) {
			                      int dataSpan = (dimConfig.m_dataSpan-1)*2+1+1;
			                	  if (j == colSize-1)
			                		  dataSpan = colSize-colSpanPrinted;
			                	  outp.append("<td class='").append(dimConfig.m_valStyleClass == null ? "tn" : dimConfig.m_valStyleClass)
			                	  .append("' colspan='").append(dataSpan).append("'>").append(dimConfig.m_disp).append("</td>");
			                	  colSpanPrinted += dataSpan;
			                	  continue;
							}
                    }
                      String tempLabel = dimConfig.m_name;
                      if (tempLabel == null || tempLabel.length() == 0)
                         tempLabel = "&nbsp;";
                      else
                         tempLabel += ":";
                     
                      com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
                      
                      boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
                      int paramId = dimInfo == null ? -1 : dimConfig.m_dimCalc.m_dimInfo.m_id;
                      String tempVarName = is123 ? "pv123" : 
                    	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                    			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                    					  :
                    			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                    			  ;

                      String tempVal = _session.getAttribute(tempVarName);//pgContext+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id));
                      
                      
                      boolean printText = true;
                      int height = 1;
                      int width = 30;
                      boolean doDate = false;
                      boolean doTime = false;
                      int tempValInt = hackAnyVal;
                      if (dimInfo != null) {
                         if (dimInfo.getAttribType() == _cache.DATE_TYPE) {
                            doDate = true;
                            doTime = "20506".equals(dimInfo.m_subtype);
                         }
                         
                         ArrayList valList = dimInfo.getValList();
                         if(valList == null)
                        	 valList = dimInfo.getValList(_dbConnection, _session);
                         
                         if (dimInfo.m_subsetOf == 99 || dimInfo.m_descDataDimId == 123 || (dimInfo.getAttribType() == _cache.LOV_TYPE && valList != null && valList.size() > 0)) {
                            printText = false;
                            tempValInt = com.ipssi.gen.utils.Misc.getParamAsInt(tempVal, hackAnyVal);
                         } 
                      }
                      height = dimConfig.m_height;
                      width = dimConfig.m_width;           
                      int labelWidth = dimConfig.m_labelWidth;
                      String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";
                      if (tempVal == null)
                         tempVal = "";
                      int dataSpan = (dimConfig.m_dataSpan-1)*2+1;     
                      outp.append("<td class='sh'  ").append(widthStr).append(" valign='top'>");
                      outp.append(tempLabel);
                      outp.append("</td>");
                      String valClass = dimConfig.m_valStyleClass != null ? dimConfig.m_valStyleClass : "tn";
                      outp.append("<td class='").append(valClass).append("' valign='top' colspan='").append(j < (colSize-1)? dataSpan :(totColSpan-colSpanPrinted-1)).append("'>");
                  
                      if (dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_numeric_filter) {
                          colSpanPrinted += 1+dataSpan;
                          String tempVarNameOperator = tempVarName + "_operator";
                          String tempValOperator = _session.getAttribute(tempVarNameOperator);
                          if (!dimConfig.m_showOperator)
                        	  tempValOperator = null;
                          tempValInt = com.ipssi.gen.utils.Misc.getParamAsInt(tempValOperator, hackAnyVal);
                          if (tempValOperator == null)
                        	  tempValOperator = "";
                          
                          otherSels.clear();
                          if (tempValOperator != null && tempValOperator.length() != 0) {
                             com.ipssi.gen.utils.Misc.convertValToVector(tempValOperator, otherSels);
                             if (otherSels.size() > 0)
                                tempValInt = ((Integer)otherSels.get(0)).intValue();
                          }
                          String addnlParams = null;
                          if (dimInfo.m_descDataDimId == 123 && launchInPrjPickMode && !com.ipssi.gen.utils.Misc.isUndef(pickPrjModeOrigOrg)) {
                              addnlParams = "desc_org="+Integer.toString(pickPrjModeOrigOrg);
                          }
                          if (dimInfo.m_descDataDimId == 123) {
                             if (addnlParams == null)
                                addnlParams = addnlClauseFor123;
                             else if (addnlClauseFor123 != null)
                                addnlParams += ("&"+addnlClauseFor123);
                          }
                          //outp.append("<table border='0' cellspacing='0' cellpadding='3' >");
                          //outp.append("<tr>");
                          //outp.append("<td class='tn' valign='top' >");
                          DimInfo tempDimInfo = DimInfo.getDimInfo("operator_lov");
                          //if (!dimConfig.m_showOperator) {
                        //	  outp.append("<span style='display:none'>");
                          //}
                          if (dimConfig.m_showOperator)
                        		  _cache.printDimVals(_session, _dbConnection, _user, tempDimInfo, tempValInt, otherSels, outp, tempVarNameOperator, dimConfig.m_multiSelect, "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, false, false, "modifyOperand(this)", null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
                          //if (!dimConfig.m_showOperator) {
                        //	  outp.append("</span>");
                          //}
                          
                          String tempVarNameOperandFirst = tempVarName + "_operand_first";
                          String tempValOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
                          
                          if (tempValOperandFirst == null)
                        	  tempValOperandFirst = "";
                          if (!printText) {
                        	  otherSels.clear();
                              if (tempValOperandFirst != null && tempValOperandFirst.length() != 0) {
                                 com.ipssi.gen.utils.Misc.convertValToVector(tempValOperandFirst, otherSels);
                                 if (otherSels.size() > 0)
                                    tempValInt = ((Integer)otherSels.get(0)).intValue();
                              }
                              else {
                            	  tempValInt = Misc.getUndefInt();
                              }
                          	 _cache.printDimVals(_session, _dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName,dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, contextInfo,1);
                           }
                           else {
                        	   outp.append("<input class='tn' type='text' name='").append(tempVarNameOperandFirst).append("' id='").append(tempVarNameOperandFirst).append("' size='").append(width).append("' value='").append(tempValOperandFirst).append("' />");
                           }
                          
                          String tempVarNameOperandSecond = tempVarName + "_operand_second";
                          String tempValOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
                          
                          if (tempValOperandSecond == null)
                        	  tempValOperandSecond = "";
                         
                         outp.append("<input class='tn' type='text' name='").append(tempVarNameOperandSecond).append("' id='").append(tempVarNameOperandSecond).append("' size='").append(width).append("' value='").append(tempValOperandSecond).append("' ");
                          if("7".equalsIgnoreCase(tempValOperator)){
                        	  outp.append(" />");
                          }else{
                        	  outp.append(" style='display:none' />");
                          }
                       }
                       else if (dimConfig.m_dimCalc.m_dimInfo != null) {
                         colSpanPrinted += 1+dataSpan;
                          if (printText) {
                             if (height > 1) {    
                                outp.append("<textarea class='tn' name='").append(tempVarName).append("' rows='").append(height).append("' cols='").append(width).append("'>").append(tempVal).append("</textarea>");
                             } //end of printing text area
                             else {
                                outp.append("<input class='").append(doDate ? "datetimepicker" : "tn").append("' type='text' name='").append(tempVarName).append("' size='").append(width).append("' value='").append(tempVal).append("' ");
                                /*if (doDate) {
                                	String timePart = doTime ? ",true" : ",false";
									outp.append(" readonly=\"readonly\" ");
                                }*/ //end of img for date
                                outp.append("/>");
                                if (doDate) {
                                	String timePart = doTime ? ",true" : ",false";
                              	  	//outp.append("<img src='"+Misc.G_IMAGES_BASE+"calendar.gif' alt='date picker'  name='imgCalendar' width='16' height='16' border='0' id='imgCalendar' onclick='popUpCalendar(this, forms[0].").append(tempVarName).append(",  \""+PageHeader.helperGetDateFormatSpec(contextInfo)+ "\""+(timePart)+")' />");
                              	  	outp.append("&nbsp;");
                              	  	outp.append("<img src=\""+Misc.G_IMAGES_BASE+"undo_orange.gif\" onClick='clearCalendar(forms[0].").append(tempVarName).append(")' alt=\"Clear Date\" name=\"imgClearDate\" border=\"0\" width=\"16\" height=\"16\">");
                              	  	outp.append("</div>");
                                } //end of img for date
                             }//end of text box
                          }//end of printing text
                          else {
                             otherSels.clear();
                             if (tempVal != null && tempVal.length() != 0) {
                                com.ipssi.gen.utils.Misc.convertValToVector(tempVal, otherSels);
                                if (otherSels.size() > 0)
                                   tempValInt = ((Integer)otherSels.get(0)).intValue();
                             }
                             String addnlParams = null;
                             if (dimInfo.m_descDataDimId == 123 && launchInPrjPickMode && !com.ipssi.gen.utils.Misc.isUndef(pickPrjModeOrigOrg)) {
                                 addnlParams = "desc_org="+Integer.toString(pickPrjModeOrigOrg);
                             }
                             if (dimInfo.m_descDataDimId == 123) {
                                if (addnlParams == null)
                                   addnlParams = addnlClauseFor123;
                                else if (addnlClauseFor123 != null)
                                   addnlParams += ("&"+addnlClauseFor123);
                             }
                             if((is123 && onChangeHandler != null && onChangeHandler.length() > 0) || (dimConfig.m_onChange_handler != null && dimConfig.m_onChange_handler.length() > 0)){
                            	 if(is123 && onChangeHandler != null && onChangeHandler.length() > 0){
                            	 String onChHandler = onChangeHandler + "(\"" + (Misc.isUndef(dimConfig.m_on_change_id) ? "" : topPageContext+dimConfig.m_on_change_id) + "\")";
                            	 _cache.printDimVals(_dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName, dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, true, false, onChHandler/* "markDirty(event.srcElement)"*/, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
                            	 }
                            	 else
                            	 {
                            		 String onChHandler = dimConfig.m_onChange_handler + "(this, \"" + (Misc.isUndef(dimConfig.m_on_change_id) ? "" : topPageContext+dimConfig.m_on_change_id) + "\",\""+((dimConfig.m_onChange_add_params != null && dimConfig.m_onChange_add_params.length() >0) ? dimConfig.m_onChange_add_params : "")+"\",\""+((dimConfig.m_onChange_url != null && dimConfig.m_onChange_url.length() >0) ? dimConfig.m_onChange_url : "")+"\",\""+topPageContext+"\")";
                            		 _cache.printDimVals(_dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName, dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, true, false, onChHandler/* "markDirty(event.srcElement)"*/, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo,1);
                            	 }
                            	 //_cache.printDimVals(_session, _dbConnection, _user, tempDimInfo, tempValInt, otherSels, outp, tempVarNameOperator, dimConfig.m_multiSelect, "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, false, false, "modifyOperand(this)", null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
                             }else
                            	 _cache.printDimVals(_session, _dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName,dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, contextInfo,1);
                             if (convertedForAWB != null) {
                               if (convertedForAWB.length() > 0) {
                                  convertedForAWB.append(separator);
                               }
                               convertedForAWB.append("d").append(dimInfo.m_id).append("=").append(tempVal);                               
                            }
                          } //end of printing select or check
                      }
                      else {
                          outp.append("&nbsp;");
                      }
                      outp.append("</td>");
                  } //looped thru all cols
                  if (printGoInSameRow) {
                	  outp.append("<td class='tmTip' v  colspan='1'><input  onclick='displayProgBar(\"Please wait ... this might take 5-30s and sometimes more \")' type='submit' name='SearchButton' value='").append(searchLabel).append("' class='Input_buttons' />                  </td>");  	  
                  }
                  outp.append("</tr>");
              } //looped thru all rows
              

              if (!printGoInSameRow) {
            	  int colspanForTip = 2;//maxCol > 1 ? 2 : 1;
	              outp.append("<tr>")
//	                 .append("<td class='sh' >&nbsp;</td>")
	//                 .append("<td class='tmTip' valign='bottom' colspan='").append(colspanForTip).append("'>");//was 2*maxCol-1
		//			      outp.append("Ctrl-Click to select multiple</td>");
//
	              ;
			              outp.append("<td class='tmTip' nowrap valign='bottom'  colspan='").append(2*maxCol).append("'>");
			              if (!doingNoTop) {
			            	  outp.append("<span class='tmTip'>To search for no value enter null and to search for some value enter _</span>");
				              outp.append("<a  style='margin-left:150px' href='#' onclick='handleSaveResetSearchEtc(0)'>Load Saved Search Preferences</a>&nbsp;&nbsp;&nbsp;");
				              outp.append("<input type='hidden' name='_search_save_etc' value='-1'/>");
	
				              outp.append("<a href='#' onclick='handleSaveResetSearchEtc(1)'>Save Search Preferences</a>&nbsp;&nbsp;&nbsp;");
				              outp.append("<a href='#' onclick='handleSaveResetSearchEtc(2)'>Reset to Default</a>&nbsp;&nbsp;&nbsp;");
				              //, String addnlSearchButton, String dimsRetainForAddnlSearchButton
				              if (addnlSearchButton != null) {
				            	  outp.append("<input type='hidden' name='dims_retain_search' value='").append(dimsRetainForAddnlSearchButton == null ? "" : dimsRetainForAddnlSearchButton).append("'/>");
					              //outp.append("&nbsp;&nbsp;<input  onclick='handleSaveResetSearchEtc(3)' type='button' name='junkbut' value='").append(addnlSearchButton).append("' class='Input_buttons' />");
				            	  outp.append("<a href='#' onclick='handleSaveResetSearchEtc(3)'>").append(addnlSearchButton).append("</a>&nbsp;&nbsp&nbsp;");
				              }
			              }
			              
			              outp.append("<input  onclick='displayProgBar(\"Please wait ... this might take 5-30s and sometimes more \")' type='submit' name='SearchButton' value='").append(searchLabel).append("' class='Input_buttons' />                  </td>");

//					      
	              //outp.append("<td class='tmTip' valign='bottom'  colspan='1'><input  onclick='displayProgBar(\"Please wait ... this might take 5-30s and sometimes more \")' type='submit' name='SearchButton' value='").append(searchLabel).append("' class='Input_buttons' />                  </td>");
	              outp.append("</tr>");
              }
              outp.append("</table>");
              if (printBorder) {
                  outp.append("</td></tr></table>");
              }
              if (convertedForAWB != null && Misc.isUndef(projectId)) {
                 if (convertedForAWB.length() > 0) {
                    convertedForAWB.append(separator);
                 }
                 convertedForAWB.append("d").append(99).append("=").append(projectId);                               
              }
          } 
          catch (Exception e){
             e.printStackTrace();
             throw e;              
          }
      }

      public static void printSearchBoxForLink(SessionManager _session, ArrayList configInfo, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp) throws Exception {//pgContext is not used
    	  //regularOrHTMLOrText
          try {  
        	  String separator = "&";
              User _user = _session.getUser();
              Connection _dbConnection = _session.getConnection();
              Cache _cache = _session.getCache();
              HttpServletRequest request = _session.request;
             
              if (configInfo == null || configInfo.size() == 0)
                  return;
              int hackAnyVal = Misc.G_HACKANYVAL;
                
              ArrayList otherSels = new ArrayList();
              if (searchBoxHelper == null)
                 return;
              String anyStr = Integer.toString(hackAnyVal);
              String topPageContext = searchBoxHelper.m_topPageContext;
              for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            	  ArrayList rowInfo = (ArrayList) configInfo.get(i);
            	  
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
            		  if (dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) 
            			  continue;
                      com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;

            		  boolean is123 = dimInfo.m_descDataDimId == 123;
                      int paramId = dimInfo.m_id;
                      String tempVarName = is123 ? "pv123" : 
                    	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                    			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                    					  :
                    			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                    			  ;
                      String tempVal = _session.getAttribute(tempVarName);//pgContext+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id));
                      String tempVarNameOperator = tempVarName + "_operator";
                      String tempVarNameOperandFirst = tempVarName + "_operand_first";
                      String tempVarNameOperandSecond = tempVarName + "_operand_second";
                      String valOperator = _session.getAttribute(tempVarNameOperator);
                      String valOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
                      String valOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
                      
                      String savetempVarName = is123 ? "pv123" : "pv"+paramId;
                      String savetempVarNameOperator = tempVarName + "_operator";
                      String savetempVarNameOperandFirst = tempVarName + "_operand_first";
                      String savetempVarNameOperandSecond = tempVarName + "_operand_second";
                      if (dimConfig.m_numeric_filter) {
                     	if (tempVal == null || tempVal.length() == 0)
                     		tempVal = valOperandFirst;
                      }
                      if (tempVal == null || tempVal.length() == 0)
                    	  continue;
                      if (outp.length() != 0) {
                    	  outp.append("&");
                      }
                      outp.append(savetempVarName);
                      String tv = java.net.URLEncoder.encode(tempVal);
                      outp.append("=").append(tv);
                      if (dimConfig.m_numeric_filter) {
                    	  outp.append("&").append(savetempVarNameOperator).append("=").append(valOperator);
                    	  outp.append("&").append(savetempVarNameOperandSecond).append("=").append(valOperandSecond);
                      }
            	  }//each col
              }//each row
          } 
          catch (Exception e){
             e.printStackTrace();
             throw e;              
          }
      }

      public static int printSearchBoxForHeader(SessionManager _session, ArrayList configInfo, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, int mode) throws Exception {//pgContext is not used
    	  //regularOrHTMLOrText
          try {  
        	  boolean asTextText = mode == 0;
        	  int linesAdded = 0;
        	 
        	  String separator = "&";
              User _user = _session.getUser();
              Connection _dbConnection = _session.getConnection();
              Cache _cache = _session.getCache();
              HttpServletRequest request = _session.request;
             
              if (configInfo == null || configInfo.size() == 0)
                  return 0;
              int hackAnyVal = Misc.G_HACKANYVAL;
                
              ArrayList otherSels = new ArrayList();
              if (searchBoxHelper == null)
                 return 0;
        	  String topPageContext = searchBoxHelper.m_topPageContext;
        	  if (topPageContext == null)
        		  topPageContext = "pv";
        	  String anyStr = Integer.toString(Misc.G_HACKANYVAL);
              DimInfo operatorDim = DimInfo.getDimInfo("operator_lov");;
              for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            	  ArrayList rowInfo = (ArrayList) configInfo.get(i);
            	  
            	  boolean spaceNeeded = false;
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
            		  if (dimConfig == null || dimConfig.m_hidden || dimConfig.m_dimCalc == null) 
            			  continue;
                      com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
                      boolean mergeLabelAndVal = false;
                      if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0) {
		                    	  if (spaceNeeded) {
		                        	  if (asTextText)
		                        		  outp.append("            ");
		                        	  else 
		                        		  outp.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		                          }
		                          spaceNeeded = true;
		                          if (asTextText)
		                    		  outp.append("            ");
		                    	  else 
		                    		  outp.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		                          outp.append(dimConfig.m_disp);
		                    	  continue;
							}
                      }
                      if (dimInfo == null)
                    	  continue;
            		  boolean is123 = dimInfo.m_descDataDimId == 123;
                      int paramId = dimInfo.m_id;
                      String tempVarName = is123 ? "pv123" : 
                    	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                    			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                    					  :
                    			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                    			  ;

                      String tempVal = _session.getAttribute(tempVarName);//pgContext+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id));
                      String tempVarNameOperator = tempVarName + "_operator";
                      String tempVarNameOperandFirst = tempVarName + "_operand_first";
                      String tempVarNameOperandSecond = tempVarName + "_operand_second";
                      String valOperator = _session.getAttribute(tempVarNameOperator);
                      String valOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
                      String valOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
                      if (dimConfig.m_numeric_filter) {
                     	if (tempVal == null || tempVal.length() == 0)
                     		tempVal = valOperandFirst;
                      }
                      if (tempVal == null || tempVal.length() == 0 || tempVal.equals(anyStr))
                    	  continue;
                      StringBuilder valStr = new StringBuilder();
                      int iv = Misc.getParamAsInt(tempVal);
                      if (is123) {
                    	  if (Misc.isUndef(iv))
                    		  continue;
                    	  valStr.append(_cache.getPortName(_dbConnection, iv));
                      }
                      ArrayList<Integer> valList = new ArrayList<Integer>();
                	  Misc.convertValToVector(tempVal, valList);
                	  
                	  if (is123) {
                		  
                	  }
                	  else if (dimConfig.m_numeric_filter) {
                		  
                		  if (dimConfig.m_showOperator) {
                			  valStr.append( _cache.getAttribDisplayNameFull(_session, _dbConnection, operatorDim, Misc.getParamAsInt(valOperator)));
                			  valStr.append(asTextText ? " " : "&nbsp;");
                		  }
                		  if (valOperandFirst != null && valOperandFirst.length() != 0)
                			  valStr.append(valOperandFirst);
                		  if (valOperandSecond != null && valOperandSecond.length() != 0)
                			  valStr.append(asTextText ? " " : "&nbsp;").append(valOperandSecond);
                	  }
                	  else if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) { //vehicle
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getVehicleDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) { //vehicle
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getVehicleDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM) { //user
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getDriverDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_subsetOf == 70201) { //DOR) { //user
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getDORRDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_type != Cache.LOV_TYPE) {
                		  valStr.append(tempVal);
                      }
                      else {
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  String v = _cache.getAttribDisplayNameFull(_session, _dbConnection, dimInfo, valList.get(t));
            				  if (valStr.length() != 0)
            					  valStr.append(";");
            				  valStr.append(v);
                		  }
                      }
                      if (spaceNeeded) {
                    	  if (asTextText)
                    		  outp.append("            ");
                    	  else 
                    		  outp.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                      }
                      
                      spaceNeeded = true;
                      String tempLabel = dimConfig.m_name;
                      if (tempLabel == null || tempLabel.length() == 0)
                         tempLabel = asTextText ? " " : "&nbsp;";
                      else
                         tempLabel += ":";
                      outp.append(!asTextText? "<b>" : "").append(tempLabel).append(asTextText? " ":"&nbsp;").append(!asTextText ? "</b>":"");
                      outp.append(valStr);
            	  }//each col
            	  if (spaceNeeded) {
            		  outp.append(asTextText ? "\n" : "<br/>\n");
            		  linesAdded++;
            	  }
              }//each row
              return linesAdded;
          } 
          catch (Exception e){
             e.printStackTrace();
             throw e;              
          }
      }

      public static int printSearchBoxForHeaderOld(SessionManager _session, ArrayList configInfo, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, int mode) throws Exception {//pgContext is not used
    	  //regularOrHTMLOrText
          try {  
        	  boolean asTextText = mode == 0;
        	  int linesAdded = 0;
        	 
        	  String separator = "&";
              User _user = _session.getUser();
              Connection _dbConnection = _session.getConnection();
              Cache _cache = _session.getCache();
              HttpServletRequest request = _session.request;
             
              if (configInfo == null || configInfo.size() == 0)
                  return 0;
              int hackAnyVal = Misc.G_HACKANYVAL;
                
              ArrayList otherSels = new ArrayList();
              if (searchBoxHelper == null)
                 return 0;
            //1. get the maxCol displayable ... we will show that many cols
        	  int maxCount = 0;
        	  String topPageContext = searchBoxHelper.m_topPageContext;
        	  if (topPageContext == null)
        		  topPageContext = "pv";
        	  String anyStr = Integer.toString(Misc.G_HACKANYVAL);
              for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            	  //check if all cols are hidden
            	  ArrayList rowInfo = (ArrayList) configInfo.get(i);
            	  int colInRos = 0;
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                      if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0)
								colInRos++;
							continue;
                      }
            		  if (dimConfig == null || dimConfig.m_hidden || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) 
            			  continue;
                      com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
                      
            		  boolean is123 = dimInfo.m_descDataDimId == 123;
                      int paramId = dimInfo == null ? -1 : dimInfo.m_id;
                      String tempVarName = is123 ? "pv123" : 
                    	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                    			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                    					  :
                    			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                    			  ;
                      String tempVal = _session.getAttribute(tempVarName);//pgContext+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id));
                      if (tempVal == null || tempVal.length() == 0 || tempVal.equals(anyStr))
                    	  continue;
                      colInRos++;                      
            	  }
            	  if (colInRos > maxCount)
            		  maxCount = colInRos;
              }
              int colIndex = 0;
              boolean spaceNeeded = false;
              DimInfo operatorDim = DimInfo.getDimInfo("operator_lov");;
              for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            	  ArrayList rowInfo = (ArrayList) configInfo.get(i);
            	  //change of approach we will print rows as is given in header ..
            	  //earlier we printed upto maxCount in a row and then created a new row
            	  maxCount = 0;
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
            		  if (dimConfig == null || dimConfig.m_hidden || dimConfig.m_dimCalc == null) 
            			  continue;
            		  if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0) {
								maxCount++;
								continue;
							}
            		  }
            		  if (dimConfig.m_dimCalc.m_dimInfo == null)
            			  continue;
            		  maxCount++;
            	  }
            	  
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
            		  if (dimConfig == null || dimConfig.m_hidden || dimConfig.m_dimCalc == null) 
            			  continue;
                      com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
                      boolean mergeLabelAndVal = false;
                      if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0) {
		                    	  colIndex++;
		                    	  if (spaceNeeded) {
		                        	  if (asTextText)
		                        		  outp.append("            ");
		                        	  else 
		                        		  outp.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		                          }
		                          spaceNeeded = true;
		                          if (asTextText)
		                    		  outp.append("            ");
		                    	  else 
		                    		  outp.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		                          outp.append(dimConfig.m_disp);
		                          
		                          if (colIndex >= maxCount) {
		                        	  outp.append(asTextText ? "\n" : "<br/>\n");
		                        	  linesAdded++;
		                        	  colIndex = 0;
		                        	  spaceNeeded = false;
		                          }
		                    	  continue;
							}
                      }
                      if (dimInfo == null)
                    	  continue;
            		  boolean is123 = dimInfo.m_descDataDimId == 123;
                      int paramId = dimInfo.m_id;
                      String tempVarName = is123 ? "pv123" : 
                    	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                    			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                    					  :
                    			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                    			  ;

                      String tempVal = _session.getAttribute(tempVarName);//pgContext+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id));
                      String tempVarNameOperator = tempVarName + "_operator";
                      String tempVarNameOperandFirst = tempVarName + "_operand_first";
                      String tempVarNameOperandSecond = tempVarName + "_operand_second";
                      String valOperator = _session.getAttribute(tempVarNameOperator);
                      String valOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
                      String valOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
                      if (dimConfig.m_numeric_filter) {
                     	if (tempVal == null || tempVal.length() == 0)
                     		tempVal = valOperandFirst;
                      }
                      if (tempVal == null || tempVal.length() == 0 || tempVal.equals(anyStr))
                    	  continue;
                      StringBuilder valStr = new StringBuilder();
                      int iv = Misc.getParamAsInt(tempVal);
                      if (is123) {
                    	  if (Misc.isUndef(iv))
                    		  continue;
                    	  valStr.append(_cache.getPortName(_dbConnection, iv));
                      }
                      ArrayList<Integer> valList = new ArrayList<Integer>();
                	  Misc.convertValToVector(tempVal, valList);
                	  
                	  if (is123) {
                		  
                	  }
                	  else if (dimConfig.m_numeric_filter) {
                		  
                		  if (dimConfig.m_showOperator) {
                			  valStr.append( _cache.getAttribDisplayNameFull(_session, _dbConnection, operatorDim, Misc.getParamAsInt(valOperator)));
                			  valStr.append(asTextText ? " " : "&nbsp;");
                		  }
                		  if (valOperandFirst != null && valOperandFirst.length() != 0)
                			  valStr.append(valOperandFirst);
                		  if (valOperandSecond != null && valOperandSecond.length() != 0)
                			  valStr.append(asTextText ? " " : "&nbsp;").append(valOperandSecond);
                	  }
                	  else if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) { //vehicle
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getVehicleDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) { //vehicle
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getVehicleDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM) { //user
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getDriverDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_subsetOf == 70201) { //DOR) { //user
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  MiscInner.PairStr projectInfo = _cache.getDORRDisplayInfo(_dbConnection, valList.get(t));
                			  if (projectInfo != null && projectInfo.first != null) {
                				  if (valStr.length() != 0)
                					  valStr.append(";");
                				  valStr.append(projectInfo.first);
                			  }
                		  }
                	  }
                	  else if (dimInfo.m_type != Cache.LOV_TYPE) {
                		  valStr.append(tempVal);
                      }
                      else {
                		  for (int t=0,ts=valList.size(); t<ts; t++) {
                			  String v = _cache.getAttribDisplayNameFull(_session, _dbConnection, dimInfo, valList.get(t));
            				  if (valStr.length() != 0)
            					  valStr.append(";");
            				  valStr.append(v);
                		  }
                      }
                      colIndex++;
                      if (spaceNeeded) {
                    	  if (asTextText)
                    		  outp.append("            ");
                    	  else 
                    		  outp.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                      }
                      
                      spaceNeeded = true;
                      String tempLabel = dimConfig.m_name;
                      if (tempLabel == null || tempLabel.length() == 0)
                         tempLabel = asTextText ? " " : "&nbsp;";
                      else
                         tempLabel += ":";
                      outp.append(!asTextText? "<b>" : "").append(tempLabel).append(asTextText? " ":"&nbsp;").append(!asTextText ? "</b>":"");
                      outp.append(valStr);
                      if (colIndex >= maxCount) {
                    	  outp.append(asTextText ? "\n" : "<br/>\n");
                    	  linesAdded++;
                    	  colIndex = 0;
                    	  spaceNeeded = false;
                      }
            	  }//each col
              }//each row
              if (colIndex > 0 && colIndex < maxCount) {
            	  outp.append(asTextText ? "\n" : "<br/>\n");
            	  linesAdded++;
              }
              return linesAdded;
          } 
          catch (Exception e){
             e.printStackTrace();
             throw e;              
          }
      }


      /*public String displayVal(DimInfo dimInfo, com.ipssi.gen.utils.Pair<Double, Double> multScaleFactor, FmtI.AllFmt formatter, SessionManager session, Cache cache, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
  	    String retval = null;
  	    if (multScaleFactor == null) {
  	    	multScaleFactor = new Pair<Double, Double>(1.0,1.0);
  	    }
  	    if (dimInfo == null)
  	    	return toString(); //convert as is
  		if (dimInfo != null ) {
  			int attribType = dimInfo.getAttribType();
  			int subType = Misc.getParamAsInt(dimInfo.m_subtype);
  			double addFactor = 0;
  			double mulFactor = 1;
  			boolean uProfile = false;
  			
  			if (attribType == Cache.LOV_TYPE) {
  				retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, m_iVal);
  			}				
  			else if (attribType == Cache.NUMBER_TYPE) {
  				double internalVal = getDoubleVal();
  				
  				double dval = uProfile ? internalVal*mulFactor+addFactor : internalVal;
  				if (subType == 20510)
  					retval = formatTimeInterval(dval);
  				else if ( subType == 20502)
  					retval = Misc.m_currency_formatter.format(dval);
  				else if (formatter != null) {
  					retval = formatter == null ? Double.toString(dval) : ((FmtI.Number)formatter).format(dval);
  				}
  				else {						
  					retval = Double.toString(dval);
  				}
  			}
  			else if (attribType == Cache.DATE_TYPE) {
  				Date internalVal = this.getDateVal();
  				if (formatter != null) {
  					retval = ((FmtI.Date)formatter).format(internalVal);
  				}
  				else {
  					retval = sdf.format(internalVal);
  				}
  			}				
  			else if (attribType == Cache.INTEGER_TYPE || attribType == Cache.LOV_NO_VAL_TYPE) {
  				int internalVal = this.getIntVal();
  				int ival = uProfile ?(int) (internalVal*mulFactor+addFactor) : internalVal;
  				if (subType == 20510)
  					retval = formatTimeInterval(ival);
  				else
  					retval = Integer.toString(ival);
  			}				
  			else if (attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE ) {
  				retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, m_iVal);//TODO fully implement this function	
  			}							
  		}//if valid DimInfo				    
  	    if (retval == null)
  	    	retval = toString();
  	    return retval;
     }*/
      public static void printSearchBoxForPrint(SessionManager _session, int privIdForOrg, String pgContext, ArrayList configInfo, FrontGetValHelper valGetter, MiscInner.SearchBoxHelper searchBoxHelper, StringBuilder outp, String addnlClauseFor123, String searchLabel, boolean printBorder, StringBuilder convertedForAWB, MiscInner.ContextInfo contextInfo,String onChangeHandler, String addnlSearchButton, String dimsRetainForAddnlSearchButton, boolean toHide) throws Exception {//pgContext is not used
          try {  

              int projectId = (int) _session.getProjectId();
              String separator = "&";
              User _user = _session.getUser();
              Connection _dbConnection = _session.getConnection();
              Cache _cache = _session.getCache();
              HttpServletRequest request = _session.request;
              int pickPrjModeOrigOrg = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("pick_prj_mode_org"));
             
              if (configInfo == null || configInfo.size() == 0)
                  return;
              int specialMode = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("special_mode"));
              boolean launchInPrjPickMode = specialMode == 1;
              boolean launchInPrjCmpMode = specialMode == 2;
              boolean launchInWorkflowMode = specialMode == 3;
              int hackAnyVal = Misc.G_HACKANYVAL;
                
              ArrayList otherSels = new ArrayList();
              if (searchBoxHelper == null)
                 return;
              int maxCol = searchBoxHelper.m_maxCol;
              String topPageContext = searchBoxHelper.m_topPageContext;
              
              if (printBorder) {
                  outp.append("<table ID='_searchBox' style='").append(toHide ? "display:none;" : "").append("margin-left:15px' border='1px' cellspacing='0' cellpadding='0' bordercolor='#003366' >")
	                    .append("<tr>")
		                  .append("<td bordercolor='ffffff'>");
              }
              boolean doingNoTop = "1".equals(_session.getParameter("_no_top_level"));
              boolean printGoInSameRow = configInfo != null && configInfo.size() == 1;
              outp.append("<table ").append(printBorder ? "" : " ID='_searchBox' ").append(!printBorder && toHide ? "style='display:none'" : "").append(" border='0' cellspacing='0' cellpadding='3' >");
              for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            	  //check if all cols are hidden
            	  ArrayList rowInfo = (ArrayList) configInfo.get(i);
            	  boolean hasNonHiddenCol = false;
            	  for (int j=0,js=rowInfo == null ? 0 : rowInfo.size();j<js;j++) {
            		  com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
            		  if (dimConfig != null && !dimConfig.m_hidden) {
            			  hasNonHiddenCol = true;
            			  break;
            		  }
            	  }
            	  if (!hasNonHiddenCol)
            		  continue;
                  outp.append("<tr>")
                      .append("<td width='6'  class='sh'>&nbsp;</td>");
                  
                  int colSize = rowInfo.size();
                  int totColSpan = maxCol*2+1;
                  int colSpanPrinted = 1;
                  for (int j=0;j<colSize;j++) {
                      com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                      if (dimConfig.m_hidden)
                          continue;
                      String tempLabel = dimConfig.m_name;
                      if (dimConfig.m_name == null || dimConfig.m_name.length() == 0) {
							if (dimConfig.m_disp != null && dimConfig.m_disp.length() != 0) {
								 int dataSpan = (dimConfig.m_dataSpan-1)*2+1+1;
		                    	  if (j == colSize-1)
		                    		  dataSpan = colSize-colSpanPrinted;
		                    	  outp.append("<td class='").append(dimConfig.m_valStyleClass == null ? "tn" : dimConfig.m_valStyleClass)
		                    	  .append("' colspan='").append(dataSpan).append("'>").append(dimConfig.m_disp).append("</td>");
		                    	  colSpanPrinted += dataSpan;
		                    	  continue;		
							}
                      }
                      if (tempLabel == null || tempLabel.length() == 0)
                         tempLabel = "&nbsp;";
                      else
                         tempLabel += ":";           
                      boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
                      int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
                      String tempVarName = is123 ? "pv123" : 
                    	  dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0 ? 
                    			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : topPageContext+dimConfig.m_paramName
                    					  :
                    			  dimConfig.m_setAsPermInSearchBox ? "pv"+paramId : topPageContext+paramId
                    			  ;

                      String tempVal = _session.getAttribute(tempVarName);//pgContext+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id));
                      if(tempVal == null || "".equals(tempVal) || "-1000".equals(tempVal))
                    	  continue;
                      com.ipssi.gen.utils.DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
                      
                      boolean printText = true;
                      int height = 1;
                      int width = 30;
                      boolean doDate = false;
                      boolean doTime = false;
                      int tempValInt = hackAnyVal;
                      if (dimInfo != null) {
                         if (dimInfo.getAttribType() == _cache.DATE_TYPE) {
                            doDate = true;
                            doTime = "20506".equals(dimInfo.m_subtype);
                         }
                         
                         ArrayList valList = dimInfo.getValList();
                         if(valList == null)
                        	 valList = dimInfo.getValList(_dbConnection, _session);
                         
                         if (dimInfo.m_subsetOf == 99 || dimInfo.m_descDataDimId == 123 || (dimInfo.getAttribType() == _cache.LOV_TYPE && valList != null && valList.size() > 0)) {
                            printText = false;
                            tempValInt = com.ipssi.gen.utils.Misc.getParamAsInt(tempVal, hackAnyVal);
                         } 
                      }
                      height = dimConfig.m_height;
                      width = dimConfig.m_width;           
                      int labelWidth = dimConfig.m_labelWidth;
                      String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";
                      if (tempVal == null)
                         tempVal = "";
                      int dataSpan = (dimConfig.m_dataSpan-1)*2+1;     
                  
                      outp.append("<td class='sh'  ").append(widthStr).append(" valign='top'>");
                      outp.append(tempLabel);
                      outp.append("</td>");
				     
                      outp.append("<td class='tn' valign='top' colspan='").append(j < (colSize-1)? dataSpan :(totColSpan-colSpanPrinted-1)).append("'>");
                  
                      if (dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_numeric_filter) {
                          colSpanPrinted += 1+dataSpan;
                          String tempVarNameOperator = tempVarName + "_operator";
                          String tempValOperator = _session.getAttribute(tempVarNameOperator);
                          if (!dimConfig.m_showOperator)
                        	  tempValOperator = null;
                          tempValInt = com.ipssi.gen.utils.Misc.getParamAsInt(tempValOperator, hackAnyVal);
                          if (tempValOperator == null)
                        	  tempValOperator = "";
                          
                          otherSels.clear();
                          if (tempValOperator != null && tempValOperator.length() != 0) {
                             com.ipssi.gen.utils.Misc.convertValToVector(tempValOperator, otherSels);
                             if (otherSels.size() > 0)
                                tempValInt = ((Integer)otherSels.get(0)).intValue();
                          }
                          String addnlParams = null;
                          if (dimInfo.m_descDataDimId == 123 && launchInPrjPickMode && !com.ipssi.gen.utils.Misc.isUndef(pickPrjModeOrigOrg)) {
                              addnlParams = "desc_org="+Integer.toString(pickPrjModeOrigOrg);
                          }
                          if (dimInfo.m_descDataDimId == 123) {
                             if (addnlParams == null)
                                addnlParams = addnlClauseFor123;
                             else if (addnlClauseFor123 != null)
                                addnlParams += ("&"+addnlClauseFor123);
                          }
                          //outp.append("<table border='0' cellspacing='0' cellpadding='3' >");
                          //outp.append("<tr>");
                          //outp.append("<td class='tn' valign='top' >");
                          DimInfo tempDimInfo = DimInfo.getDimInfo("operator_lov");
                          //if (!dimConfig.m_showOperator) {
                        //	  outp.append("<span style='display:none'>");
                          //}
                          if (dimConfig.m_showOperator)
                        		  _cache.printDimVals(_session, _dbConnection, _user, tempDimInfo, tempValInt, otherSels, outp, tempVarNameOperator, dimConfig.m_multiSelect, "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, false, false, "modifyOperand(this)", null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
                          //if (!dimConfig.m_showOperator) {
                        //	  outp.append("</span>");
                          //}
                          
                          String tempVarNameOperandFirst = tempVarName + "_operand_first";
                          String tempValOperandFirst = _session.getAttribute(tempVarNameOperandFirst);
                          
                          if (tempValOperandFirst == null)
                        	  tempValOperandFirst = "";
                          if (!printText) {
                        	  otherSels.clear();
                              if (tempValOperandFirst != null && tempValOperandFirst.length() != 0) {
                                 com.ipssi.gen.utils.Misc.convertValToVector(tempValOperandFirst, otherSels);
                                 if (otherSels.size() > 0)
                                    tempValInt = ((Integer)otherSels.get(0)).intValue();
                              }
                              else {
                            	  tempValInt = Misc.getUndefInt();
                              }
                          	 _cache.printDimVals(_session, _dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName,dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, contextInfo,1);
                           }
                           else {
                        	   outp.append("<input class='tn' type='text' name='").append(tempVarNameOperandFirst).append("' id='").append(tempVarNameOperandFirst).append("' size='").append(width).append("' value='").append(tempValOperandFirst).append("' />");
                           }
                          
                          String tempVarNameOperandSecond = tempVarName + "_operand_second";
                          String tempValOperandSecond = _session.getAttribute(tempVarNameOperandSecond);
                          
                          if (tempValOperandSecond == null)
                        	  tempValOperandSecond = "";
                         
                         outp.append("<input class='tn' type='text' name='").append(tempVarNameOperandSecond).append("' id='").append(tempVarNameOperandSecond).append("' size='").append(width).append("' value='").append(tempValOperandSecond).append("' ");
                          if("7".equalsIgnoreCase(tempValOperator)){
                        	  outp.append(" />");
                          }else{
                        	  outp.append(" style='display:none' />");
                          }
                       }
                       else if (dimConfig.m_dimCalc.m_dimInfo != null) {
                         colSpanPrinted += 1+dataSpan;
                          if (printText) {
                             if (height > 1) {    
                                outp.append("<textarea class='tn' name='").append(tempVarName).append("' rows='").append(height).append("' cols='").append(width).append("'>").append(tempVal).append("</textarea>");
                             } //end of printing text area
                             else {
                                outp.append("<input class='").append(doDate ? "datetimepicker" : "tn").append("' type='text' name='").append(tempVarName).append("' size='").append(width).append("' value='").append(tempVal).append("' ");
                                /*if (doDate) {
                                	String timePart = doTime ? ",true" : ",false";
									outp.append(" readonly=\"readonly\" ");
                                }*/ //end of img for date
                                outp.append("/>");
                                if (doDate) {
                                	String timePart = doTime ? ",true" : ",false";
                              	  	//outp.append("<img src='"+Misc.G_IMAGES_BASE+"calendar.gif' alt='date picker'  name='imgCalendar' width='16' height='16' border='0' id='imgCalendar' onclick='popUpCalendar(this, forms[0].").append(tempVarName).append(",  \""+PageHeader.helperGetDateFormatSpec(contextInfo)+ "\""+(timePart)+")' />");
                              	  	outp.append("&nbsp;");
                              	  	outp.append("<img src=\""+Misc.G_IMAGES_BASE+"undo_orange.gif\" onClick='clearCalendar(forms[0].").append(tempVarName).append(")' alt=\"Clear Date\" name=\"imgClearDate\" border=\"0\" width=\"16\" height=\"16\">");
                              	  	outp.append("</div>");
                                } //end of img for date
                             }//end of text box
                          }//end of printing text
                          else {
                             otherSels.clear();
                             if (tempVal != null && tempVal.length() != 0) {
                                com.ipssi.gen.utils.Misc.convertValToVector(tempVal, otherSels);
                                if (otherSels.size() > 0)
                                   tempValInt = ((Integer)otherSels.get(0)).intValue();
                             }
                             String addnlParams = null;
                             if (dimInfo.m_descDataDimId == 123 && launchInPrjPickMode && !com.ipssi.gen.utils.Misc.isUndef(pickPrjModeOrigOrg)) {
                                 addnlParams = "desc_org="+Integer.toString(pickPrjModeOrigOrg);
                             }
                             if (dimInfo.m_descDataDimId == 123) {
                                if (addnlParams == null)
                                   addnlParams = addnlClauseFor123;
                                else if (addnlClauseFor123 != null)
                                   addnlParams += ("&"+addnlClauseFor123);
                             }
                             if((is123 && onChangeHandler != null && onChangeHandler.length() > 0) || (dimConfig.m_onChange_handler != null && dimConfig.m_onChange_handler.length() > 0)){
                            	 if(is123 && onChangeHandler != null && onChangeHandler.length() > 0){
                            	 String onChHandler = onChangeHandler + "(\"" + (Misc.isUndef(dimConfig.m_on_change_id) ? "" : topPageContext+dimConfig.m_on_change_id) + "\")";
                            	 _cache.printDimVals(_dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName, dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, true, false, onChHandler/* "markDirty(event.srcElement)"*/, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
                            	 }
                            	 else
                            	 {
                            		 String onChHandler = dimConfig.m_onChange_handler + "(this, \"" + (Misc.isUndef(dimConfig.m_on_change_id) ? "" : topPageContext+dimConfig.m_on_change_id) + "\",\""+((dimConfig.m_onChange_add_params != null && dimConfig.m_onChange_add_params.length() >0) ? dimConfig.m_onChange_add_params : "")+"\",\""+((dimConfig.m_onChange_url != null && dimConfig.m_onChange_url.length() >0) ? dimConfig.m_onChange_url : "")+"\",\""+topPageContext+"\")";
                            		 _cache.printDimVals(_dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName, dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, true, false, onChHandler/* "markDirty(event.srcElement)"*/, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo,1);
                            	 }
                            	 //_cache.printDimVals(_session, _dbConnection, _user, tempDimInfo, tempValInt, otherSels, outp, tempVarNameOperator, dimConfig.m_multiSelect, "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, false, false, false, "modifyOperand(this)", null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
                             }else
                            	 _cache.printDimVals(_session, _dbConnection, _user, dimConfig.m_dimCalc.m_dimInfo, tempValInt, otherSels, outp, tempVarName,dimConfig.m_multiSelect,  "Any", dimConfig.m_multiSelect, privIdForOrg, height, width, false, addnlParams, contextInfo,1);
                             if (convertedForAWB != null) {
                               if (convertedForAWB.length() > 0) {
                                  convertedForAWB.append(separator);
                               }
                               convertedForAWB.append("d").append(dimInfo.m_id).append("=").append(tempVal);                               
                            }
                          } //end of printing select or check
                      }
                      else {
                          outp.append("&nbsp;");
                      }
                      outp.append("</td>");
                  } //looped thru all cols
                  if (printGoInSameRow) {
                	  outp.append("<td class='tmTip' v  colspan='1'><input  onclick='displayProgBar(\"Please wait ... this might take 5-30s and sometimes more \")' type='submit' name='SearchButton' value='").append(searchLabel).append("' class='Input_buttons' />                  </td>");  	  
                  }
                  outp.append("</tr>");
              } //looped thru all rows
              

              if (!printGoInSameRow) {
            	  int colspanForTip = 2;//maxCol > 1 ? 2 : 1;
	              outp.append("<tr>")
//	                 .append("<td class='sh' >&nbsp;</td>")
	//                 .append("<td class='tmTip' valign='bottom' colspan='").append(colspanForTip).append("'>");//was 2*maxCol-1
		//			      outp.append("Ctrl-Click to select multiple</td>");
//
	              ;
			              outp.append("<td class='tmTip' nowrap valign='bottom'  colspan='").append(2*maxCol).append("'>");
			              if (!doingNoTop) {
			            	  outp.append("<span class='tmTip'>To search for no value enter null and to search for some value enter _</span>");
				              outp.append("<a  style='margin-left:150px' href='#' onclick='handleSaveResetSearchEtc(0)'>Load Saved Search Preferences</a>&nbsp;&nbsp;&nbsp;");
				              outp.append("<input type='hidden' name='_search_save_etc' value='-1'/>");
	
				              outp.append("<a href='#' onclick='handleSaveResetSearchEtc(1)'>Save Search Preferences</a>&nbsp;&nbsp;&nbsp;");
				              outp.append("<a href='#' onclick='handleSaveResetSearchEtc(2)'>Reset to Default</a>&nbsp;&nbsp;&nbsp;");
				              //, String addnlSearchButton, String dimsRetainForAddnlSearchButton
				              if (addnlSearchButton != null) {
				            	  outp.append("<input type='hidden' name='dims_retain_search' value='").append(dimsRetainForAddnlSearchButton == null ? "" : dimsRetainForAddnlSearchButton).append("'/>");
					              //outp.append("&nbsp;&nbsp;<input  onclick='handleSaveResetSearchEtc(3)' type='button' name='junkbut' value='").append(addnlSearchButton).append("' class='Input_buttons' />");
				            	  outp.append("<a href='#' onclick='handleSaveResetSearchEtc(3)'>").append(addnlSearchButton).append("</a>&nbsp;&nbsp&nbsp;");
				              }
			              }
			              
			              outp.append("<input  onclick='displayProgBar(\"Please wait ... this might take 5-30s and sometimes more \")' type='submit' name='SearchButton' value='").append(searchLabel).append("' class='Input_buttons' />                  </td>");

//					      
	              //outp.append("<td class='tmTip' valign='bottom'  colspan='1'><input  onclick='displayProgBar(\"Please wait ... this might take 5-30s and sometimes more \")' type='submit' name='SearchButton' value='").append(searchLabel).append("' class='Input_buttons' />                  </td>");
	              outp.append("</tr>");
              }
              outp.append("</table>");
              if (printBorder) {
                  outp.append("</td></tr></table>");
              }
              if (convertedForAWB != null && Misc.isUndef(projectId)) {
                 if (convertedForAWB.length() > 0) {
                    convertedForAWB.append(separator);
                 }
                 convertedForAWB.append("d").append(99).append("=").append(projectId);                               
              }
          } 
          catch (Exception e){
             e.printStackTrace();
             throw e;              
          }
      } //end of func printSearchBox        
      public static void savePagePref(User user, SessionManager session, String pgContext, String topPageContext, ArrayList<ArrayList<DimConfigInfo>> configInfo, Connection conn, boolean mandatory) throws Exception {
    	  
 	   		try {
	   	   		//1.Get header id (if any). If not exist create, else delete details   	   		
	   	   		//3.Clear info in memory .. as well as save in prod
	   	   	
	        
	        
	   	   		PreparedStatement ps = conn.prepareStatement(Queries.GET_USER_PAGE_PREF_HEADER);
	   	   		ps.setString(1,pgContext);
	   	   		ps.setInt(2,user.getUserId());
	   	   		ResultSet rs = ps.executeQuery();
	   	   		int headerId = Misc.getUndefInt();
	   	   		if (rs.next()) {
	   	   			headerId = rs.getInt(1);
	   	   		}
	   	   		rs.close();
	   	   		ps.close();
	   	   		if (Misc.isUndef(headerId)) {
		   	   	    ps = conn.prepareStatement(Queries.INSERT_USER_PAGE_PREF_HEADER);
		   	   	    int paramIndex = 1;
		   	   	    ps.setString(paramIndex++, pgContext);
		   	   	    ps.setInt(paramIndex++,user.getUserId());
		   	   	    ps.executeUpdate();
		   	   	    rs = ps.getGeneratedKeys();
		   	   	    if (rs.next()) {
		   	   	    	headerId = rs.getInt(1);
		   	   	    }
		   	   	    rs.close();
		   	   	    ps.close();
	   	   		}
	   	   		else {
	   	   			ps = conn.prepareStatement(Queries.DELETE_USER_PAGE_PREF_DETAILS);
	   	   			ps.setInt(1, headerId);
	   	   			ps.execute();
	   	   			ps.close();
	   	   		}
 	   	        ps = conn.prepareStatement(Queries.INSERT_USER_PAGE_PREF_DETAILS);
 	   	        ps.setInt(1,headerId);
 	            HashMap<String,String> inmemVal = new HashMap<String, String>();
		   		for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
		            ArrayList rowInfo = (ArrayList) configInfo.get(i);              
		            
		            for (int j=0,js=rowInfo.size();j<js;j++) {
		                com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
		                if (dimConfig.m_hidden || dimConfig.m_dimCalc.m_dimInfo == null)
		                	continue;
		                
		                String paramId = Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id);
		                if (topPageContext == null)
		                    topPageContext = "p";
		                boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
		                String paramNameForVar = is123 ? "123" : paramId;		              
		                String paramNameForSession = is123 ? "pv123" : topPageContext+paramId;
		                String v = session.getAttribute(paramNameForSession);
		                ps.setString(2, paramNameForVar);
		                ps.setString(3, v);
		                ps.execute();
		                inmemVal.put(paramNameForVar, v);
		            }
		   		}
		        ps.close();
		        synchronized (user) {
		        	user.pagePrefs.put(pgContext, inmemVal);
		        }
 	   	}
 	   	catch (Exception e) {
 		   e.printStackTrace();
 		   throw e;
 	   	}	   
    }
    
    public static void resetPagePref(User user, SessionManager session, String pgContext, String topPageContext, ArrayList<ArrayList<DimConfigInfo>> configInfo, Connection conn, boolean mandatory) throws Exception {
  	  for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
            ArrayList rowInfo = (ArrayList) configInfo.get(i);              
            
            for (int j=0,js=rowInfo.size();j<js;j++) {
                com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                if (dimConfig.m_hidden || dimConfig.m_dimCalc.m_dimInfo == null)
                	continue;
                
                String paramId = Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id);
              if (topPageContext == null)
                 topPageContext = "p";
              boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
              String paramNameForSession = is123 ? "pv123" : topPageContext+paramId;
              session.removeAttribute(paramNameForSession);
             
           } //end of col               
      }//end of row
  	session.setUserPref(conn, true);
    }
    
    public static void loadPagePref(User user, SessionManager session, String pgContext, String topPageContext, ArrayList<ArrayList<DimConfigInfo>> configInfo, Connection conn, boolean mandatory) throws Exception {
 	   try {
 		   //first check if the user has the info in mem -
 		   boolean toLoad = mandatory ||!"1".equals(session.getParameter(pgContext+"_load")); 
 		   if (!toLoad)
 			   return;

 		   HashMap<String, String> prefList = null;
 		   synchronized (user) {
 			   prefList = user.pagePrefs.get(pgContext);
 			   if (prefList == null) {
 				   prefList = new HashMap<String, String>();
 				   PreparedStatement ps = conn.prepareStatement(Queries.GET_USER_PAGE_PREF);
 				   ps.setInt(1, user.getUserId());
 				   ps.setString(2, pgContext);
 				   ResultSet rs = ps.executeQuery();
 				   while (rs.next()) {
 					   String m = rs.getString(1);
 					   String n = rs.getString(2);
 					   String v = rs.getString(3);
 					   prefList.put(n, v);
 				   }
 				   rs.close();
 				   ps.close();
 			   }
 		   }
 		   if (prefList == null || prefList.isEmpty())
 			   return;
 		   session.setAttribute(pgContext+"_load","1",true);
 		   session.setAttribute("_def_param_set","1",false);
 		   for (int i=0, is=configInfo == null ? 0 : configInfo.size();i<is;i++) {
                 ArrayList rowInfo = (ArrayList) configInfo.get(i);              
                 
                 for (int j=0,js=rowInfo.size();j<js;j++) {
                     com.ipssi.gen.utils.DimConfigInfo dimConfig = (com.ipssi.gen.utils.DimConfigInfo) rowInfo.get(j);
                     if (dimConfig.m_hidden || dimConfig.m_dimCalc.m_dimInfo == null)
                     	continue;
                     
                     String paramId = Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id);
                   if (topPageContext == null)
                      topPageContext = "p";
                   boolean is123 = dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
                   String paramNameForSession = is123 ? "pv123" : topPageContext+paramId;
                   String paramNameForVar = is123 ? "123" : paramId;
                   String v = prefList.get(paramNameForVar);
                   if (v != null && v.length() != 0)
                 	  session.setAttribute(paramNameForSession, v,false);
                } //end of col               
           }//end of row		   		   
 	   }
 	   catch (Exception e) {
 		   e.printStackTrace();
 		   throw e;
 	   }
    }
      
      private void load(String fileName, Connection dbConn, Cache cache) throws Exception {
         try {
          
            m_name = fileName;
            Document configDoc = null;
            FileInputStream inp = null;
            
              inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName);
              MyXMLHelper test = new MyXMLHelper(inp, null);
              configDoc =  test.load();
              inp.close();
            
            int blockCount = 0;
            
            Element topElem = configDoc != null ? configDoc.getDocumentElement() : null;
            if (topElem != null) {
               m_currencySpec = Misc.getParamAsInt(topElem.getAttribute("currency_spec"), m_currencySpec);
               m_unitSpec = Misc.getParamAsInt(topElem.getAttribute("unit_spec"), m_unitSpec);               
            }
            for (Node n = configDoc != null && configDoc.getDocumentElement() != null ? configDoc.getDocumentElement().getFirstChild() : null ;n!=null;n=n.getNextSibling()) {
                if (n.getNodeType() != 1)
                   continue;
                Element e = (Element) n;
                if (blockCount == 0) {
                   m_allRows = DimConfigInfo.readRowColInfo(e, true);
                   m_1stBlockHeaderWidth = Misc.getParamAsInt(e.getAttribute("width"), m_1stBlockHeaderWidth);
                   // TODO check added
                   if(m_allRows != null)
                	   m_rhsRowStartIndex = m_allRows.size();
                }
                else {
                   ArrayList temp = DimConfigInfo.readRowColInfo(e, true);
                  // TODO temp null check added
                  if(temp != null )
                   for (int i=0, is = temp.size(); i<is;i++) {
                      m_allRows.add(temp.get(i));
                   }
                   m_2ndBlockHeaderWidth = Misc.getParamAsInt(e.getAttribute("width"), m_2ndBlockHeaderWidth);
                   m_showCompInfo = !"0".equals(e.getAttribute("show_comp_with"));
                   m_showWkspChange = !"0".equals(e.getAttribute("show_wksp_change"));
                   m_showViewVer = !"0".equals(e.getAttribute("show_view_ver"));
                }
                blockCount++;
            }
            m_printApprovalStatusCode = Misc.getParamAsInt(configDoc.getDocumentElement().getAttribute("print_approval_status"), 0);

        //printApprovalStatusCode == 0 => do noting, 1=> at top with summary line of bubbles
        //  2 => at bottom with summary line of bubbles
        //  3 => at top no summary line
        //  4 => at bottom no summary line

            m_valGetter = new FrontGetValHelper(m_allRows, true);
            StringBuilder selClause = new StringBuilder();
            StringBuilder fromClause = new StringBuilder();
            StringBuilder joinClause = new StringBuilder();
            //m_paramCodeDate = new ArrayList();
			 // for supplier
            //CAPEX_REMOVE_START            
            //m_queryTimeReq = Misc.getEnhPrjSelAttrib(m_valGetter.getToAskDimListNew(), selClause, fromClause, joinClause, false, false, dbConn, cache, false, false, false, false, false);


            //if (m_queryTimeReq.m_attribSelAreNotPrjDep) {
            //   m_query = "select "+selClause+" from "+fromClause;
            //   if (joinClause.length() > 0)
            //      m_query += " where "+joinClause;
            //}
            //else {                  
            //   m_query = "select "+selClause+" from projects "+fromClause+" where projects.id = ? and pj_map_items.wspace_id = ? and alternatives.id = ? and "+joinClause;
            //}
            
            //CAPEX_REMOVE_END

//            System.out.println(m_query);
            
			 //Load multi attribs
			if (m_allRows != null) {
				for (int rowCnt = 0; rowCnt < m_allRows.size(); rowCnt++) {
					ArrayList colList = (ArrayList)m_allRows.get(rowCnt);
					if (colList != null) {
						for (int colCnt = 0; colCnt < colList.size(); colCnt++) {
							DimConfigInfo colConfigInfo = (DimConfigInfo)colList.get(colCnt);
							if (colConfigInfo != null) {
                if (colConfigInfo.m_dimCalc == null || colConfigInfo.m_dimCalc.m_dimInfo == null)
                   continue;
								if (colConfigInfo.m_dimCalc.m_dimInfo.m_colMap.table != null && colConfigInfo.m_dimCalc.m_dimInfo.m_colMap.table.equals("prj_multi_attrib")) {
									if (m_prjMultiAttribDims == null)
										m_prjMultiAttribDims = new ArrayList();
									m_prjMultiAttribDims.add(colConfigInfo.m_dimCalc.m_dimInfo);
								}
							}
						}
					}
				}
			}
         }
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
      }
      
      public  synchronized static PageHeader getPageHeader(String fileName, Connection dbConn, Cache cache) throws Exception {
         try {
             if (fileName == null)
                fileName = "header.xml";

             for (int i=0,is = g_pageHeaderList.size();i<is;i++) {
                PageHeader pageHeader = (PageHeader) g_pageHeaderList.get(i);
                if (pageHeader.m_name.equals(fileName))
                   return pageHeader;
             }
             PageHeader pageHeader = new PageHeader();
             pageHeader.load(fileName, dbConn, cache);
             g_pageHeaderList.add(pageHeader);
             return pageHeader;
         }
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }

      }
      /********************* remove for CAPEX_REMOVE
static public void printFPHeader(FrontPageInfo frontPage, User user, JspWriter out, Connection dbConn, Cache cache, SessionManager session, String pgContext, MiscInner.ContextInfo currencyEtcContext, String addnlFilterClause, ArrayList addnlParam) throws Exception {
        try {
          MiscInner.TimeReq timeReq = frontPage.m_headerTimeReq;
          if (timeReq == null)
             return;
          Logger log = (Logger) session.request.getAttribute("_log");
          boolean doTiming = log.getLoggingLevel() >= 15;
          StringBuilder timeMsg = doTiming ? new StringBuilder() : null;
          String confirmMessage = session.getAttribute("_confirm_msg"); //rajeev 040808
          if (confirmMessage != null)
             session.removeAttribute("_confirm_msg");
          ArrayList paramsFromFilter = new ArrayList();
          ArrayList paramForPort = new ArrayList();//TO_PORT_FORWARD
          FmtI.Date dtfmt = currencyEtcContext != null ? currencyEtcContext.getDateFormatter() : null;
          String query = PrjTemplateHelper.getFrontHeaderQuery(frontPage, session, dbConn, cache, pgContext, null, paramsFromFilter, addnlFilterClause, paramForPort, dtfmt);//TO_PORT_FORWARD
          if (doTiming)
             log.log("[FP]:"+query,15);
          PreparedStatement ps = dbConn.prepareStatement(query);
          int psIndex = 1;
//old way of doing
//          if (m_paramCodeDate != null && m_paramCodeDate.size() > 0) {


//          int timeId = Misc.getTimeIdParamForFlexQuery(dbConn, m_queryTimeReq, askedYear);
          
          int undef = Misc.getUndefInt();
          int askedYear = Misc.getParamAsInt(session.getParameter("asked_year"));
          if (askedYear > 300)
             askedYear -= 1900;
          int currTimeId = Misc.isUndef(askedYear) ? Misc.getCurrentYearTimeId() : TimePeriodHelper.getTimeId(Misc.SCOPE_ANNUAL, askedYear);;
          int budTimeId = Misc.getBudgetYearTimeId(dbConn);  //TO_PORT_FORWARD
          java.sql.Date currDate = com.ipssi.gen.utils.Misc.getCurrentDate(); //TO_PORT_FORWARD
          psIndex = Misc.putParams(timeReq.m_paramRequiredSel, ps, psIndex, undef, undef, undef, currTimeId, budTimeId, currDate, 1, currencyEtcContext == null ? MiscInner.CurrencyInfo.g_defaultCurrencyCode : currencyEtcContext.m_currencyInfo.m_id, currencyEtcContext == null ? MiscInner.UnitInfo.g_defaultUnit.m_id : currencyEtcContext.m_unitInfo.m_id, dbConn, cache, timeMsg);//TO_PORT_FORWARD
          if (paramForPort != null && paramForPort.size() > 0) {//TO_PORT_FORWARD
             ps.setInt(psIndex++, ((Integer)paramForPort.get(0)).intValue());
             if (doTiming)
                 timeMsg.append(",").append(((Integer)paramForPort.get(0)).intValue());
          }
          psIndex = Misc.putParams(timeReq.m_paramRequired, ps, psIndex, undef, undef, undef, currTimeId, budTimeId, currDate, 1, currencyEtcContext == null ? MiscInner.CurrencyInfo.g_defaultCurrencyCode : currencyEtcContext.m_currencyInfo.m_id, currencyEtcContext == null ? MiscInner.UnitInfo.g_defaultUnit.m_id : currencyEtcContext.m_unitInfo.m_id, dbConn, cache, timeMsg);//TO_PORT_FORWARD
          
          
          for (int j=0,js = paramsFromFilter == null ? 0 : paramsFromFilter.size();j<js;j++) {
              ps.setInt(psIndex++, ((Integer)paramsFromFilter.get(j)).intValue());
              if (doTiming)
                 timeMsg.append(",").append(((Integer)paramsFromFilter.get(j)).intValue());
          }
          for (int j=0, js = addnlParam == null ? 0 : addnlParam.size();j<js;j++) {
              ps.setInt(psIndex++, ((Integer)addnlParam.get(j)).intValue());
              if (doTiming)
                 timeMsg.append(",").append(((Integer)addnlParam.get(j)).intValue());
          }
          if (doTiming) {
             log.log(timeMsg, 15);
             timeMsg.setLength(0);
          }
          long currTimeMillis = 0;
          if (doTiming) { 
             currTimeMillis = System.currentTimeMillis();
             timeMsg.append("[FP Timing],In,").append(currTimeMillis);
          }
          ResultSet rs = ps.executeQuery();
          if (doTiming) { 
             long oldTime = currTimeMillis;
             currTimeMillis = System.currentTimeMillis();
             timeMsg.append(",Exec_Delta,").append(currTimeMillis - oldTime);
          }
          boolean hasRows = rs.next();
          StringBuilder printInfo = new StringBuilder();
          if (confirmMessage != null) {//040808
              printInfo.append("<div class=tmTip>").append(confirmMessage).append("</div>"); //040808
          }//040808
          printInfo.append("<table  colspacing=\"0\" cellpadding=\"0\" border=\"0\">")
                   .append("    <tr><td class=tmTip><img src=\""+Misc.G_IMAGES_BASE+"transparent_sp.gif\" height=5></td></tr>")
                   .append("   <tr><td align=\"left\"><table cellpadding=\"2\" cellspacing=\"0\">");
          //PRINT the LEFT BLOCK ... this is the only block FP
//printBlock(Connection dbConn, User user, int projectId, int workspaceId, ArrayList configInfo, int startIndex, int endIndexExcl, int labelWidth, ResultSet rset, FrontGetValHelper valHelper, StringBuilder printInfo, Cache cache, String wkspChangeUri, String compLabel, String advUri, String borderColor, int readWriteMode, int hackMake1stNRowsReadonly, StringBuilder reqFieldVarList, StringBuilder reqFieldLabelList, StringBuilder reqFieldTypeList, boolean printHelp, boolean printApprovalAtTop, StringBuilder bar, StringBuilder line, String labelStyle, String dataStyle)
          printBlock(dbConn, user, undef, undef, frontPage.m_headerInfo, 0, frontPage.m_headerInfo.size(), 120, rs, frontPage.m_headerValGetter, printInfo, cache, null, null, null, null, 0, 0, null, null, null, false, false, null, null
          , "tmFPLabel", "tmFPData", null, null, currencyEtcContext, session, null, null, out, Misc.getUndefInt(), Misc.getUndefInt(), null);
//          printBlock(dbConn, user, projectId, workspaceId, m_allRows, 0, this.m_rhsRowStartIndex, this.m_1stBlockHeaderWidth, hasRows ? rs : null, m_valGetter, printInfo, cache, wkspChangeUri, compLabel, advUri, null, 0, 0,null,null,null, null);

          printInfo.append("</table></td>");
          printInfo.append("</tr></table>");
          out.println(printInfo);
          
          rs.close();
          ps.close();
          if (doTiming) { 
             long oldTime = currTimeMillis;
             currTimeMillis = System.currentTimeMillis();
             timeMsg.append(",Print_Delta,").append(currTimeMillis - oldTime);
             log.log(timeMsg,15);
          }
        }//try
        catch (Exception e) {
          e.printStackTrace();
          throw e;
        }
      }      
      public void printHeader(Connection dbConn, User user, int projectId, int workspaceId, int alternativeId, int askedYear, JspWriter out, Cache cache, String wkspChangeUri, String compLabel, String advUri, SessionManager session) throws Exception {
         printHeader(dbConn, user, projectId, workspaceId, alternativeId, askedYear, out, cache, wkspChangeUri, compLabel, advUri, session, null);
      }
      
      public void printHeader(Connection dbConn, User user, int projectId, int workspaceId, int alternativeId, int askedYear, JspWriter out, Cache cache, String wkspChangeUri, String compLabel, String advUri, SessionManager session, MiscInner.ContextInfo currencyEtcContext) throws Exception {
        try {
          Logger log = session.getLogger();
          boolean doTiming = log.getLoggingLevel() >= 15;
          StringBuilder timeMsg = doTiming ? new StringBuilder() : null; 
          
          PageMenuHeadCalc pageMenuHeadCalc = PageMenuHeadCalc.getPageMenuHeadCalc(projectId, workspaceId, alternativeId, session, user, cache, log, dbConn);
          String confirmMessage = session.getAttribute("_confirm_msg");
          if (currencyEtcContext == null) {
          
             MiscInner.ContextInfo pageContextInfo = Misc.getContextInfo(session, dbConn, cache, log, user);

             currencyEtcContext = Misc.getContextInfo(pageContextInfo.m_locale, m_currencySpec, m_unitSpec, Misc.getUndefInt(), session, dbConn, cache, log, user);             
             //get the currencyInfo from the header.xml's currencySpec
             //get the unitInfo from the header.xml's 
          }
          if (confirmMessage != null) {
             session.removeAttribute("_confirm_msg");
          }
          else {
             StringBuilder newMsg = null;
             if (pageMenuHeadCalc.m_prjCmpLabel != null) {
                newMsg = new StringBuilder();
                newMsg.append(pageMenuHeadCalc.m_prjCmpLabel);                                
                boolean versionFullData = "1".equals(session.getAttribute("version_full_data")); //for model_data ..
                if (!versionFullData)
                   newMsg.append(". Differences (only) are shown in <span class='tmCompStyle'>this style</span>.");
                else
                   newMsg.append(". The compared data is shown in <span class='tmCompStyle'>this style</span>.");
             }
             boolean isOverride = pageMenuHeadCalc.isEditOverride();
             boolean isShowAll = pageMenuHeadCalc.isShowAllMenu();
             
             if (isOverride || isShowAll) {
                if (newMsg == null) {
                   newMsg = new StringBuilder();                   
                }
                else {
                   newMsg.append(" ");                   
                }
                if (isOverride && isShowAll)
                   newMsg.append("You are in override edit mode and have enabled view of all information for the project.");
                else if (isOverride)
                   newMsg.append("You are in override edit mode.");
                else if (isShowAll)
                   newMsg.append("You have enabled view of all information for the project.");
             }
             //if (newMsg != null) {
             //   newMsg.append(" Use <b>Collaboration -> Specials</b> menu for additional options/actions.");                
             //}
             confirmMessage = newMsg != null ? newMsg.toString() : null;
          }
          String privMessage = session.getAttribute("_priv_msg");
          if (privMessage != null) {
             if (confirmMessage != null)
                confirmMessage += privMessage;
             else
                confirmMessage = privMessage;
          }
          if (doTiming)
             log.log("[PageHeader Query:]"+m_query,15);
          PreparedStatement ps = dbConn.prepareStatement(m_query);
          int psIndex = 1;
//old way of doing
//          if (m_paramCodeDate != null && m_paramCodeDate.size() > 0) {


//          int timeId = Misc.getTimeIdParamForFlexQuery(dbConn, m_queryTimeReq, askedYear);
          askedYear = Misc.isUndef(askedYear) ? Misc.getParamAsInt(session.getParameter("asked_year")) : askedYear;
          if (askedYear > 300)
             askedYear -= 1900;
          int currTimeId = Misc.isUndef(askedYear) ? Misc.getCurrentYearTimeId() : TimePeriodHelper.getTimeId(Misc.SCOPE_ANNUAL, askedYear);;
          //TO_PORT_FORWARD

              int budTimeId = Misc.getBudgetYearTimeId(dbConn);


              java.sql.Date currDate = com.ipssi.gen.utils.Misc.getCurrentDate();
          psIndex = Misc.putParams(m_queryTimeReq.m_paramRequiredSel, ps, psIndex, projectId, workspaceId, alternativeId, currTimeId, budTimeId, currDate, 1, currencyEtcContext == null ? MiscInner.CurrencyInfo.g_defaultCurrencyCode : currencyEtcContext.m_currencyInfo.m_id, currencyEtcContext == null ? MiscInner.UnitInfo.g_defaultUnit.m_id : currencyEtcContext.m_unitInfo.m_id, dbConn, cache, timeMsg);
          psIndex = Misc.putParams(m_queryTimeReq.m_paramRequired, ps, psIndex, projectId, workspaceId, alternativeId, currTimeId, budTimeId, currDate, 1, currencyEtcContext == null ? MiscInner.CurrencyInfo.g_defaultCurrencyCode : currencyEtcContext.m_currencyInfo.m_id, currencyEtcContext == null ? MiscInner.UnitInfo.g_defaultUnit.m_id : currencyEtcContext.m_unitInfo.m_id, dbConn, cache, timeMsg);          
          if (!m_queryTimeReq.m_attribSelAreNotPrjDep) {
              ps.setInt(psIndex++, projectId);
              ps.setInt(psIndex++, workspaceId);
              ps.setInt(psIndex++, alternativeId);
          }
          long currTimeMillis = 0;
          if (doTiming) {
             timeMsg.append(",").append(projectId).append(",").append(workspaceId).append(",").append(alternativeId);
             log.log(timeMsg,15);
             timeMsg.setLength(0);
             currTimeMillis = System.currentTimeMillis();
             timeMsg.append("[PageDetailExec],In,").append(currTimeMillis);
          }
//          if (m_queryTimeReq.m_desiredScope >= 0) {
//             ps.setInt(psIndex++, timeId);
//          }
         

          ResultSet rs = ps.executeQuery();
          if (doTiming) {
             long oldTime = currTimeMillis;
             currTimeMillis = System.currentTimeMillis();
             timeMsg.append(",Exec,").append(currTimeMillis-oldTime);
          }
          boolean hasRows = rs.next();
          StringBuilder printInfo = new StringBuilder();
          printInfo.append("<table width=\"100%\" colspacing=\"0\" cellpadding=\"0\" border=\"0\">");
//          printInfo.append("    <tr><td height='5px' colspan=2 class=tmTip><img src=\""+Misc.G_IMAGES_BASE+"transparent_sp.gif\" height='5px' width='1px'></td></tr>");
          if (confirmMessage != null) {
              printInfo.append("<tr><td colspan=2 class=tmTip>").append(confirmMessage).append("</td></tr>");
          }
          printInfo.append("   <tr><td align=\"left\"><table cellpadding=\"2\" cellspacing=\"0\">");
          //PRINT the LEFT BLOCK
	      //printBlock(Connection dbConn, User user, int projectId, int workspaceId, ArrayList configInfo, int startIndex, int endIndexExcl, int labelWidth, ResultSet rset, FrontGetValHelper valHelper, StringBuilder printInfo, Cache cache, String wkspChangeUri, String compLabel, String advUri, String borderColor, int readWriteMode, int hackMake1stNRowsReadonly, StringBuilder reqFieldVarList, StringBuilder reqFieldLabelList, StringBuilder reqFieldTypeList, boolean printHelp, boolean printApprovalAtTop, StringBuilder bar, StringBuilder line, String labelStyle, String dataStyle)
		  
			// Find out the multi attribs
			HashMap multiAttribVals = null;
		  multiAttribVals = PrjTemplateHelper.loadMultiAttribData(session, this.m_prjMultiAttribDims, 0, (int)projectId, multiAttribVals, currencyEtcContext);

		  printBlock(dbConn, user, projectId, workspaceId, m_allRows, 0, this.m_rhsRowStartIndex, this.m_1stBlockHeaderWidth, hasRows ? rs : null, m_valGetter, printInfo, cache, wkspChangeUri, compLabel, advUri, null, 0, 0, null, null, null, false, true, null, null, "tmPHLabel", "tmPHData", null, pageMenuHeadCalc, currencyEtcContext, session, multiAttribVals, null, out, Misc.getUndefInt(), Misc.getUndefInt(), null);
			//printBlock(dbConn, user, projectId, workspaceId, m_allRows, 0, this.m_rhsRowStartIndex, this.m_1stBlockHeaderWidth, hasRows ? rs : null, m_valGetter, printInfo, cache, wkspChangeUri, compLabel, advUri, null, 0, 0,null,null,null, false, true, null,null, "tmPHLabel", "tmPHData", null, pageMenuHeadCalc, currencyEtcContext, session, null, null, null,out, Misc.getUndefInt(), Misc.getUndefInt(), null);

          printInfo.append("</table></td><td align=\"right\"><table class=\"version_bg\" bordercolor=\"#003366\" border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
          //print the RIGHT BLOCK
          StringBuilder bar = toPrintStatusBar() ? new StringBuilder() : null;

          StringBuilder summaryLine = false && toPrintSummLine() ? new StringBuilder() : null;
          int approvalWhere = toPrintApprovalWhere();
          boolean printAtTop = approvalWhere < 0;
          if (bar != null) {
              printApprovalStatus(dbConn, projectId, workspaceId, alternativeId, askedYear, session, cache, user,  bar);
          }
          if (summaryLine != null) {
              printSummary(dbConn, projectId, workspaceId, alternativeId, askedYear, session, cache, user, summaryLine);
          }
////printBlock(Connection dbConn, User user, int projectId, int workspaceId, ArrayList configInfo, int startIndex, int endIndexExcl, int labelWidth, ResultSet rset, FrontGetValHelper valHelper, StringBuilder printInfo, Cache cache, String wkspChangeUri, String compLabel, String advUri, String borderColor, int readWriteMode, int hackMake1stNRowsReadonly, StringBuilder reqFieldVarList, StringBuilder reqFieldLabelList, StringBuilder reqFieldTypeList, boolean printHelp, boolean printApprovalAtTop, StringBuilder bar, StringBuilder line, String labelStyle, String dataStyle)
//#ECE6E6 was regular "#F1F1FD"
          int maxCol = printBlock(dbConn, user, projectId, workspaceId, m_allRows, this.m_rhsRowStartIndex, m_allRows == null ? 0 : this.m_allRows.size(), this.m_2ndBlockHeaderWidth, hasRows ? rs : null, m_valGetter, printInfo, cache, wkspChangeUri, compLabel, advUri, "#ECE6E6", 0,0, null,null,null, false, printAtTop, bar, summaryLine, "tmPHLabel", "tmPHData", null, pageMenuHeadCalc, currencyEtcContext, session, null, null, out, Misc.getUndefInt(), Misc.getUndefInt(), null);
          //Now print the workspace change and other paraphernelia


          printInfo.append("</table></td></tr></table>");
          out.println(printInfo);
          rs.close();
          ps.close();
          if (doTiming) {
             long oldTime = currTimeMillis;
             currTimeMillis = System.currentTimeMillis();
             timeMsg.append(",Print,").append(currTimeMillis-oldTime);
             log.log(timeMsg,15);
          }

        }//try
        catch (Exception e) {
          e.printStackTrace();
          throw e;
        }
      }
      
      private void load(String fileName, Connection dbConn, Cache cache) throws Exception {
         try {
          
            m_name = fileName;
            Document configDoc = null;
            FileInputStream inp = null;
            
              inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName);
              MyXMLHelper test = new MyXMLHelper(inp, null);
              configDoc =  test.load();
              inp.close();
            
            int blockCount = 0;
            
            Element topElem = configDoc != null ? configDoc.getDocumentElement() : null;
            if (topElem != null) {
               m_currencySpec = Misc.getParamAsInt(topElem.getAttribute("currency_spec"), m_currencySpec);
               m_unitSpec = Misc.getParamAsInt(topElem.getAttribute("unit_spec"), m_unitSpec);               
            }
            for (Node n = configDoc != null && configDoc.getDocumentElement() != null ? configDoc.getDocumentElement().getFirstChild() : null ;n!=null;n=n.getNextSibling()) {
                if (n.getNodeType() != 1)
                   continue;
                Element e = (Element) n;
                if (blockCount == 0) {
                   m_allRows = PrjTemplateHelper.readRowColInfo(e, true);
                   m_1stBlockHeaderWidth = Misc.getParamAsInt(e.getAttribute("width"), m_1stBlockHeaderWidth);
                   m_rhsRowStartIndex = m_allRows.size();
                }
                else {
                   ArrayList temp = PrjTemplateHelper.readRowColInfo(e, true);
                   for (int i=0,is = temp.size();i<is;i++) {
                      m_allRows.add(temp.get(i));
                   }
                   m_2ndBlockHeaderWidth = Misc.getParamAsInt(e.getAttribute("width"), m_2ndBlockHeaderWidth);
                   m_showCompInfo = !"0".equals(e.getAttribute("show_comp_with"));
                   m_showWkspChange = !"0".equals(e.getAttribute("show_wksp_change"));
                   m_showViewVer = !"0".equals(e.getAttribute("show_view_ver"));
                }
                blockCount++;
            }
            m_printApprovalStatusCode = Misc.getParamAsInt(configDoc.getDocumentElement().getAttribute("print_approval_status"), 0);

        //printApprovalStatusCode == 0 => do noting, 1=> at top with summary line of bubbles
        //  2 => at bottom with summary line of bubbles
        //  3 => at top no summary line
        //  4 => at bottom no summary line

            m_valGetter = new FrontGetValHelper(m_allRows, true);
            StringBuilder selClause = new StringBuilder();
            StringBuilder fromClause = new StringBuilder();
            StringBuilder joinClause = new StringBuilder();
            //m_paramCodeDate = new ArrayList();
			 // for supplier
            m_queryTimeReq = Misc.getEnhPrjSelAttrib(m_valGetter.getToAskDimListNew(), selClause, fromClause, joinClause, false, false, dbConn, cache, false, false, false, false, false);
            //TO_PORT_FORWARD
            if (m_queryTimeReq.m_attribSelAreNotPrjDep) {
               m_query = "select "+selClause+" from "+fromClause;
               if (joinClause.length() > 0)
                  m_query += " where "+joinClause;
            }
            else {                  
               m_query = "select "+selClause+" from projects "+fromClause+" where projects.id = ? and pj_map_items.wspace_id = ? and alternatives.id = ? and "+joinClause;
            }

//            System.out.println(m_query);
            
			 //Load multi attribs
			if (m_allRows != null) {
				for (int rowCnt = 0; rowCnt < m_allRows.size(); rowCnt++) {
					ArrayList colList = (ArrayList)m_allRows.get(rowCnt);
					if (colList != null) {
						for (int colCnt = 0; colCnt < colList.size(); colCnt++) {
							DimConfigInfo colConfigInfo = (DimConfigInfo)colList.get(colCnt);
							if (colConfigInfo != null) {
                if (colConfigInfo.m_dimCalc == null || colConfigInfo.m_dimCalc.m_dimInfo == null)
                   continue;
								if (colConfigInfo.m_dimCalc.m_dimInfo.m_colMap.table != null && colConfigInfo.m_dimCalc.m_dimInfo.m_colMap.table.equals(Misc.PRJ_MULTI_ATTRIB_TABLE)) {
									if (m_prjMultiAttribDims == null)
										m_prjMultiAttribDims = new ArrayList();
									m_prjMultiAttribDims.add(colConfigInfo.m_dimCalc.m_dimInfo);
								}
							}
						}
					}
				}
			}
         }
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
      }
      
      public  synchronized static PageHeader getPageHeader(String fileName, Connection dbConn, Cache cache) throws Exception {
         try {
             if (fileName == null)
                fileName = "header.xml";

             for (int i=0,is = g_pageHeaderList.size();i<is;i++) {
                PageHeader pageHeader = (PageHeader) g_pageHeaderList.get(i);
                if (pageHeader.m_name.equals(fileName))
                   return pageHeader;
             }
             PageHeader pageHeader = new PageHeader();
             pageHeader.load(fileName, dbConn, cache);
             g_pageHeaderList.add(pageHeader);
             return pageHeader;
         }
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }

      }
      
      public static void preLoadHeaderPFM(Connection dbConn, Cache cache) throws Exception {//will preload this as well PFM files!!
        try {
          FileInputStream inp = null;
          Document templateDoc = null;
          try {
            inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+"preload_list.xml");
            MyXMLHelper test = new MyXMLHelper(inp, null);
            templateDoc =  test.load();
            inp.close();
          }
          catch (Exception e) {
             try {
                 if (inp != null)
                    inp.close();
                 templateDoc = null;
                 return;
             }
             catch (Exception e2) {
             }
          }
          if (templateDoc == null || templateDoc.getDocumentElement() == null)
             return;
          for (Node n = templateDoc.getDocumentElement().getFirstChild();n != null;n=n.getNextSibling()) {
             if (n.getNodeType() != 1) 
                continue;
             Element e = (Element) n;
             if ("pfm".equals(e.getTagName())) {
                //PFM.create(e.getAttribute("id")); //from CapEx
             }
             else {
                PageHeader.getPageHeader(e.getAttribute("id"), dbConn, cache);
             }
          }  
        }
        catch (Exception e) { //dont do anything
        }
      }
      ************/

} //end of inner class

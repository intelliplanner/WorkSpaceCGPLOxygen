//This really should have been named collaboration_workspace_helper
//PJ_WF_STATUS IS NOT USED
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

public class UIHelper extends Object {
   private SessionManager session = null;
   private HttpServletRequest request = null;
   static public int g_overrideEditId = 9999;
   private String m_loLabel[] = {"_loqtr","_loyear","_lomon","_loweek"};
   private String m_hiLabel[] = {"_hiqtr","_hiyear","_himon","_hiweek"};
   public StringBuilder m_hidden = new StringBuilder();
   public StringBuilder m_qs = new StringBuilder();
   public String m_pgContext = null;
   public String m_pgTitle = null;
   private double m_unit[] = {1,1,1,1,1}; //must be same As types of qtys (here 5)
   public int m_scope = Misc.SCOPE_ANNUAL;
   public int m_lo[] = {Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt()};
   public int m_hi[] = {Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt()};
   public int m_editPrivId = 1;
   public boolean m_overrideEdit = false;
   public boolean m_pastLineEditable = false;
   public boolean m_pastDataEditable = false;
   public boolean m_pastLineEditableParam = false;
   public boolean m_pastDataEditableParam = false;

   public boolean m_showAll = false;
   public boolean m_forceScope = false;
   public int m_currTimeVal = 0;
   public boolean m_hasOutOfWindowData = false;
   public boolean m_intPastOnly = false;
   //pastDataEditable => any data in window being shown is editable
   //all Show => all data being shown
   //  Otherwise m_lo[m_scope], m_hi[m_scope] is a fair bet about the presence/abscence of time in data


   public UIHelper (SessionManager session, HttpServletRequest request, String pgContext, String pgTitle, int editPriv) {
      this.session = session;
      this.request = request;
      m_pgContext = getStr("page_context", pgContext);
      String pgdetail = getStr("_page_detail", null);
      String tpm = getStr("_top_menu", null);

      m_pgTitle = getStr("page_name", pgTitle);
      for (int i=0,is=m_unit.length;i<is;i++) {
         m_unit[i] = getDouble("_unit"+Integer.toString(i), 1);
      }

      m_editPrivId = getInt("_edit", editPriv);
      m_overrideEdit = getBool("_override", false, false);
      setParam("_override", m_overrideEdit?"1":"0", false); //will be added separately to QS
      m_showAll = getBool("_show_all", false, false);
      setParam("_show_all", m_showAll ? "1":"0", false);

      m_pastLineEditableParam = m_pastLineEditable = getBool("_past_line", false);
      m_pastDataEditableParam = m_pastDataEditable = getBool("_past_data", false);
      //will set the params separately
      if (m_overrideEdit) {
         m_pastLineEditable = true;
         m_pastDataEditable = true;
      }



      for (int i=0;i<4;i++) {
        m_lo[i] = getInt(m_loLabel[i], m_lo[i]);
        m_hi[i] = getInt(m_hiLabel[i], m_hi[i]);
      }
      int minPeriod = 1000000;
      int maxPeriod = -1000000;
      int minScopeIndex = -1;
      int maxScopeIndex = -1;
      for (int i=0;i<4;i++) {
         if (!Misc.isUndef(m_lo[i])) {
            if (m_lo[i] < minPeriod) {
               minPeriod = m_lo[i];
               minScopeIndex = i;
            }
         }
         if (!Misc.isUndef(m_hi[i])) {
            if (m_hi[i] > maxPeriod) {
               maxPeriod = m_hi[i];
               maxScopeIndex = i;
            }
         }
      }
      if (minScopeIndex < 0 && maxScopeIndex < 0) {
         //assume all to be default and looking forward to future only
         for (int i=0;i<4;i++) {
            m_lo[i] = 0;
            m_hi[i] = com.ipssi.gen.utils.Misc.DEFAULT_PERIODS[i];
         }
      }
      else if (minScopeIndex < 0 && maxScopeIndex >= 0) {
         m_scope = maxScopeIndex;
         if (maxPeriod <= 0) { //interested in past data
            m_intPastOnly = true;
            for (int i=0;i<4;i++) {
               m_lo[i] = -1*com.ipssi.gen.utils.Misc.DEFAULT_PERIODS[i];
               if (Misc.isUndef(m_hi[i])) {
                  m_hi[i] = maxPeriod;
               }
            }
         }
         else { //we want to do future
            for (int i=0;i<4;i++) {
               m_lo[i] = 0;
               if (Misc.isUndef(m_hi[i]))
                  m_hi[i] = com.ipssi.gen.utils.Misc.DEFAULT_PERIODS[i];
            }
         }
      }
      else if (minScopeIndex >= 0 && maxScopeIndex < 0) {
         m_scope = minScopeIndex;
         if (minPeriod >= 0) { //interested in future
            for (int i=0;i<4;i++) {
               m_hi[i] = minPeriod+com.ipssi.gen.utils.Misc.DEFAULT_PERIODS[i];
               if (Misc.isUndef(m_lo[i])) {
                  m_lo[i] = minPeriod;
               }
            }
         }
         else { //we want to do past
            m_intPastOnly = true;
            for (int i=0;i<4;i++) {
               m_hi[i] = 0;
               if (Misc.isUndef(m_lo[i]))
                  m_lo[i] = -1*com.ipssi.gen.utils.Misc.DEFAULT_PERIODS[i];
            }
         }
      }
      else {
         m_scope = minScopeIndex;
         if (maxPeriod <= 0) {
            m_intPastOnly = true;
         }
         for (int i=0;i<4;i++) {
            if (Misc.isUndef(m_lo[i]))
               m_lo[i] = minPeriod;
            if (Misc.isUndef(m_hi[i]))
               m_hi[i] = maxPeriod;
         }
      }

      m_scope = getInt("_sugg_scope",m_scope);
      m_forceScope = getBool("_force_scope", false);
      if (!m_forceScope) {
         m_scope = getInt("des_scope",m_scope,false); //yup dont add ...
      }
      m_currTimeVal = TimePeriodHelper.getTimeVal(m_scope, (int)TimePeriodHelper.getTimeId(Misc.getCurrentDate()));
   }
   public double[] getUnitArray() { return m_unit;}
   public double getUnit() { return getUnit(0);}
   public void setUnit(int qtyType, double v) {
      if (qtyType >= 0 && qtyType < m_unit.length) {
          m_unit[qtyType] = v;
      }
   }
   public double getUnit(int qtyType) {
      if (qtyType >= 100)
         return 1;
      if (qtyType < 0 || qtyType >= m_unit.length)
          return m_unit[0];
      else return m_unit[qtyType];
   }
   public String getUnitText() {
      return getUnitText(0);
   }
   public String getUnitText(int qtyType) {
      if (qtyType >= 100) {
         return m_scope == Misc.SCOPE_ANNUAL ? "Person Years" : m_scope == Misc.SCOPE_QTR ? "Person Qtrs" : m_scope == Misc.SCOPE_MONTH ? "Person Months" : "Person Weeks";

      }
      double unit = getUnit(qtyType);
      String unitText = "";

      if (Misc.isEqual(unit,1)) {
         unitText =  "";
      }
      else if (Misc.isEqual(unit, 100)) {
         unitText =  "Hundred";
      }
      else if (Misc.isEqual(unit, 1000)) {
         unitText =  "Thousand";
      }
      else if (Misc.isEqual(unit, 1000000)) {
         unitText =  "Million";
      }
      else if (Misc.isEqual(unit, 0.01)) {
         unitText = "%";
      }
      else {
         unitText =  Misc.perc_formatter.format(unit);
      }
      String qtyText = DimInfo.getTextQtyType(qtyType);
      String spacer = unitText.length() > 0 && qtyText.length() > 0 ? " " : "";
      return unitText+spacer+qtyText;
   }
   public String getRemainderQS() {
      return("&_show_all="+(m_showAll?"1":"0")+"&_override="+(m_overrideEdit?"1":"0"));
   }

   public String getPartQS() {
      return m_qs.toString();
   }
   
   public MiscInner.Pair getSetDataWin(int dataMinTimeId, int dataMaxTimeId) {
      int suggMin = m_currTimeVal+m_lo[m_scope];
      int suggMax = m_currTimeVal+m_hi[m_scope];

      if (!Misc.isUndef(dataMinTimeId) && !Misc.isUndef(dataMaxTimeId)) {
          int dataMin = TimePeriodHelper.getTimeVal(m_scope, dataMinTimeId);
          int dataMax = TimePeriodHelper.getTimeVal(m_scope, dataMaxTimeId);
          if (dataMin < suggMin || dataMax > suggMax) {
             m_hasOutOfWindowData = true;
          }
          if (m_showAll) {
             suggMin = dataMin;//dataMin-1;
             suggMax = dataMax;//dataMax+1;
          }
          else {
             if (dataMin > suggMin && suggMax > m_currTimeVal) {//interested in future ... just use the datamin
                suggMin = dataMin;
             }
          }
      }
      return new MiscInner.Pair(suggMin, suggMax);
   }
   public boolean isReadData(int timeYear) {
      return isReadData(timeYear, false);
   }
   public boolean isReadData(int timeYear, boolean forAdderRow) {
     if (m_overrideEdit)
       return false;
     if (!m_pastLineEditable && !forAdderRow) {
        return true;
     }
     if (!m_pastDataEditable) {
        if (m_intPastOnly)
           return timeYear > m_currTimeVal;
        return timeYear < m_currTimeVal;
     }
     return false;
   }
   private void setParam(String name, String val) {
      setParam(name, val, true);
   }

   private void setParam(String name, String val, boolean toAddToQS) {
      m_hidden.append("<input type=\"hidden\" name=\"").append(name).append("\" value=\"").append(val).append("\"/>");
      if (toAddToQS)
         m_qs.append("&").append(name).append("=").append(val);
   }

   private String getStr(String name, String def) {
      return getStr(name, def, true);
   }
   private String getStr(String name, String def, boolean toAdd) {
      String retval = session.getParameter(name);
      if (toAdd && retval != null && retval.length() != 0)
        setParam(name, retval);
      return (Misc.getParamAsString(retval, def));
   }

   private int getInt(String name, int def) {
      return getInt(name, def, true);
   }
   private int getInt(String name, int def, boolean toAdd) {
      int retval = Misc.getParamAsInt(session.getParameter(name));
      if (toAdd && !Misc.isUndef(retval))
        setParam(name, Integer.toString(retval));
      if (Misc.isUndef(retval))
        retval = def;
      return retval;
   }

   private double getDouble(String name, double def) {
      return getDouble(name, def, true);
   }
   private double getDouble(String name, double def, boolean toAdd) {
      double retval = Misc.getParamAsDouble(session.getParameter(name));
      if (toAdd && !Misc.isUndef(retval))
        setParam(name, Double.toString(retval));
      if (Misc.isUndef(retval))
        retval = def;
      return retval;
   }

   private boolean getBool(String name, boolean def) {
      return getBool(name, def, true);
   }
   private boolean getBool(String name, boolean def, boolean toAdd) {
      String retval = session.getParameter(name);
      boolean boolret = def;
      if (def)
        boolret = !"0".equals(retval);
      else
        boolret = "1".equals(retval);
      if (toAdd && retval != null && retval.length() != 0)
         setParam(name,retval);
      return boolret;
   }

   public static boolean writeGetOverride(SessionManager session, HttpServletRequest request, User user, int projectId, JspWriter out) throws Exception {
        if (true)
           return false;
        else
           return writeGetOverride(session, request, user, projectId, out, Misc.getUndefInt());
   }

   public static boolean writeGetOverride(SessionManager session, HttpServletRequest request, User user, int projectId, JspWriter out, int privId) throws Exception {
      boolean changeMode = "1".equals(session.getParameter("_change_override"));
      boolean override = "1".equals(session.getParameter("_override"));
      if (Misc.isUndef(privId))
         privId = g_overrideEditId;
	 boolean hasOverride = user.isPrivAvailable(session, g_overrideEditId, projectId, Misc.getUndefInt(), Misc.getUndefInt(), false, null);
      if (!hasOverride)
         override = false;
      if (hasOverride) {
      boolean doingButton = false;
         out.print("<input type='hidden' name='_change_override' value='0'>");
         out.print("<input type='hidden' name='_override' value='"+Integer.toString(override?1:0)+"'>");
//         out.print("&nbsp;&nbsp;<a href='#' onclick='_setOverride("+Integer.toString(override?0:1)+")'>"
//         +(override ?"Normal Edit Mode" : "Override Edit Mode")
//         +"</a>&nbsp;&nbsp;");
         if (doingButton) {
            out.print("&nbsp;&nbsp;<input title='You have privileges to override usual edit behaviour' class='input_buttons' type='button' onclick='_setOverride("+Integer.toString(override?0:1)+")' name='over_ride' value='"
            +(override ?"Back to Normal Mode" : "Goto Override Mode")
            +"'>&nbsp;&nbsp;");
         }
         else {
            out.print("&nbsp;&nbsp;<a href='#' title='You have privileges to override usual edit behaviour' class='tn'  onclick='_setOverride("+Integer.toString(override?0:1)+")'>"
            +(override ?"Back to Normal Mode" : "Goto Override Mode")
            +"</a>&nbsp;&nbsp;");
         }


      }
      return override;
   }


}
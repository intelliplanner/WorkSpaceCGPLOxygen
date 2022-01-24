//Title:        Your Product Name
//Version:
//Copyright:   Copyright (c) 1999
//Author:      Your Name
//Company:     Your Company
//Description:
package com.ipssi.gen.utils;
//import com.ipssi.gen.cache.*;
import java.sql.*;
import java.util.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.text.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;


public final class MiscInner  implements Serializable {
	private static final long serialVersionUID = 1L;
    /**
	 * 
	 */
	static public class PairShortBool {
		public short first;
		public boolean second;
		public PairShortBool(short first, boolean second) {
			this.first = first;
			this.second = second;
		}
	}
	static public class PairDouble implements Serializable {
		private static final long serialVersionUID = 1L;
		public double first;
		public double second;
		public PairDouble(double first, double second) {
			this.first = first;
			this.second = second;
		}
		public String toString() {
			return "("+first +","+second+")";
		}
	}
	static public class PairFloat implements Serializable {
		private static final long serialVersionUID = 1L;
		public float first;
		public float second;
		public PairFloat(float first, float second) {
			this.first = first;
			this.second = second;
		}
	}
   static public class PairIntBool implements Serializable {
		private static final long serialVersionUID = 1L;
      public int first;
      public boolean second;
      public PairIntBool(int vf, boolean vs) {
         first = vf;
         second = vs;
      }
      public String toString() {
    	  return "("+first+","+second+")";
      }
   }
   static public class PairIntStr  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int first;
      public String second;
      public PairIntStr(int vf, String vs) {
         first = vf;
         second = vs;
      }
   }
   static public class PairLong {
	   public long first = -1;
	   public long second = -1;
	   public PairLong(long f, long s) {
		   this.first = f;
		   this.second = s;
	   }
   }
   static public class PairShort {
	   public short first;
	   public short second;
	   public PairShort(short first, short second) {
		   this.first = first;
		   this.second = second;
	   }
   }
   static public class TripleIntLongLong {
	   public int first;
	   public long second;
	   public long third;
	   public String toString() {
		   return first+","+second+","+third;
	   }
	   public TripleIntLongLong(int first, long second, long third) {
		   this.first = first;
		   this.second = second;
		   this.third = third;
	   }
   }
   
   static public class TripleIntIntBool {
	   public int first;
	   public int second;
	   public boolean third;
	   public TripleIntIntBool(int first, int second, boolean third) {
		   this.first = first;
		   this.second = second;
		   this.third = third;
	   }
   }
   static public class TripleBool {
	   public boolean first;
	   public boolean second;
	   public boolean third;
	   public TripleBool(boolean first, boolean second, boolean third) {
		   this.first = first;
		   this.second = second;
		   this.third = third;
	   }
   }
   static public class Triple implements Serializable {
		private static final long serialVersionUID = 1L;
	   public int first;
	   public int second;
	   public int third;
	   public Triple(int vf, int vs, int vt) {
	         first = vf;
	         second = vs;
	         third = vt;
	    }
   }
   static public class TripleStrStrStr  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public String first;
      public String second;
      public String third;
      public TripleStrStrStr(String vf, String vs, String vt) {
         first = vf;
         second = vs;
         third = vt;
      }
   }
	static public class TripleArrayList  implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L; //081808
		public ArrayList first;
		public ArrayList second;
		public ArrayList third;
		public TripleArrayList(ArrayList vf, ArrayList vs, ArrayList vt) {
			first = vf;
			second = vs;
			third = vt;
		}
	}
   static public class TripleBoolIntStr  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public boolean first;
      public int second;
      public String third;
      public TripleBoolIntStr(boolean vf, int vs, String vt) {
         first = vf;
         second = vs;
         third = vt;
      }
   }
   static public class TripleIntDoubleInt  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int first;
      public double second;
      public int third;
      public TripleIntDoubleInt(int vf, double vs, int ec) {
         first = vf;
         second = vs;
         third = ec;
      }
   }
   static public class TripleDoubleDoubleInt {
	   public double first;
	   public double second;
	   public int third;
	   public TripleDoubleDoubleInt(double first, double second, int third) {
		   this.first = first;
		   this.second = second;
		   this.third = third;
	   }
   }
   static public class TripleLongLongBoolean {
	   public long first;
	   public long second;
	   public boolean third;
	   public TripleLongLongBoolean(long first, long second, boolean third) {
		   this.first = first;
		   this.second = second;
		   this.third = third;
	   }
   }
   static public class TripleStrStrInt  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public String first;
      public String second;
      public int third;
      public TripleStrStrInt(String vf, String vs, int vt) {
         first = vf;
         second = vs;
         third = vt;
      }
   }
   static public class TripleIntBoolBool {
	   public int first;
	   public boolean second;
	   public boolean third;
	   public TripleIntBoolBool(int first, boolean second, boolean third) {
		   this.first = first;
		   this.second = second;
		   this.third = third;
	   }
   }
   static public class Pair implements Comparable, Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int first;
      public int second;
      public Pair(int vf, int vs) {
         first = vf;
         second = vs;
      }
      public int compareTo(Object vo) {
         Pair rhs = (Pair) vo;
         if (rhs.first != first)
            return first - rhs.first;
         else
            return
              second - rhs.second;         
      }
      public int hashCode() {
          long t = first * 31;
          long t2 = second * 31;
          return (int) (t ^ t2);
          
      }
      public boolean equals(Object vo) {
          Pair rhs = (Pair) vo;
          return first == rhs.first && second == rhs.second;
      }
      public String toString() {
    	  return "("+first+","+second+")";
      }
   }
   static public class PairIntLong {
	   public int first;
	   public long second;
	   public PairIntLong(int f, long s) {
		   this.first = f;
		   this.second = s;
	   }
   }
   static public class PairStrBool  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public String first;
      public boolean second;
      public PairStrBool(String f, boolean s) { first = f; second =s;}
   }
   static public class PairStr  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public String first;
      public String second;
      public PairStr(String vf, String vs) {
         first = vf;
         second = vs;
      }
   }
  static public class ApplyFilterResult  implements Serializable {
      /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
     public boolean m_found35 = false;
     public boolean m_found123 = false;
     public ArrayList m_v123 = null;     
     public ApplyFilterResult(boolean found35, boolean found123, ArrayList v123) { 
        m_found35 = found35;
        m_found123 = found123;
        m_v123 = v123;
     }
     public StringBuilder m_prjMultiFilter = null;
     public StringBuilder m_orderMultiFilter = null;
     public StringBuilder m_supplierMultiFilter = null;
     public boolean m_requiresProjects = false;
     public boolean m_requiresOrders = false;
     public boolean m_requiresSuppliers = false;
     public ArrayList m_customJoin = null; //array list of PairIntStr ...only operational for order filter ...in project (my orders view)
  }
   static public class PairBool  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public boolean first;
      public boolean second;
      public PairBool(boolean vf, boolean vs) {
         first = vf;
         second = vs;
      }
   }
   static public class PairElemInt  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public Element first;
      public int second;
      public PairElemInt(Element e, int t) {
         first = e;
         second = t;
      }
   }
   
   static public class GroupHelper  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public StringBuilder m_selClause = new StringBuilder();
      public StringBuilder m_fromClause = new StringBuilder();
      public StringBuilder m_andClause = new StringBuilder();
      public StringBuilder m_groupClause = new StringBuilder();            
   }
   
   
   
   static public class FilterHelper  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      //public StringBuilder m_casePart = new StringBuilder(); //for project and pj_basic stuff
      
      
      public ArrayList m_filterList = null;
      public int m_desiredScope = -1; //scope if year etc. is required
      public int m_yearDesired = Misc.getUndefInt(); // -1001 => budget year, 0 => CY, -1002 => as provided by user
      public boolean m_usesPjBasic = false;
      public boolean m_usesAltBasic = false;
      public boolean m_hackNeedsPortfolioId = false;
      public int m_timeCalcRule = 0; // 060407 ... 0=>proforma, 1=>remaining, 2=>ytd, 3=>till 1st of the time, 4=>from 1st till foreever (ignore_past_or_future entry in lov)
      public int m_useCurrencySpec = 10000; //use at page level
      public int m_useUnitSpec = 10000;//use at page level
      public int m_adornmentSpec = 0; //no
      public int m_currencyRateList = Misc.getUndefInt(); //spot/current rate list
      
      public void appendLineSpecificFilterClause(StringBuilder buf, Connection dbConn, Cache cache) throws Exception {        
      
          for (int i=0,is=m_filterList == null ? 0 : m_filterList.size();i<is;i++) {
              DimInfo.DimValList dv = (DimInfo.DimValList)(m_filterList.get(i));
              ArrayList valList = dv.getValList();
              DimInfo dimInfo = dv.m_dimInfo;
              if (dimInfo == null)
                continue;
              
              if (dv.getValList() == null || dv.getValList().size() == 0) { //read from session
                continue;
              }
              else {
                 
                 ColumnMappingHelper colHelper = dimInfo.m_colMap;
                 if (colHelper == null)
                    continue;
                 String tabName = colHelper.table.toLowerCase();
                 String colName = colHelper.column.toLowerCase();
                 if (tabName.equals("cost_items") || tabName.equals("measure_case_index") || tabName.equals("pur_cost_item_data")) {
                    buf.append(" and ");
                    buf.append(tabName).append(".").append(colName).append(" in (");
                    ArrayList selVal = cache.getInList(dbConn, dimInfo, Misc.getUndefInt(), valList, true);
                    if (selVal == null || selVal.size() == 0)
                        continue;
                    Misc.convertInListToStr(selVal, buf);
                    buf.append(") ");                    
                 }
                 
              }//if valid dimInfo in dimCalc
          }//for each dimCalc          
      }
      public static int appendAndGetCountLineSpecificSelClause(StringBuilder selBuf, StringBuilder groupBuf, ArrayList filterList) { //returns number of cnt of line specific items
          int index = 0;
          for (int i=0,is=filterList == null ? 0 : filterList.size();i<is;i++) {
              DimInfo.DimValList dv = (DimInfo.DimValList)(filterList.get(i));
              ArrayList valList = dv.getValList();
              DimInfo dimInfo = dv.m_dimInfo;
              if (dimInfo == null)
                continue;
              
              if (dv.getValList() == null || dv.getValList().size() == 0) { //read from session
                continue;
              }
              else {
                 
                 ColumnMappingHelper colHelper = dimInfo.m_colMap;
                 if (colHelper == null)
                    continue;
                 String tabName = colHelper.table.toLowerCase();
                 String colName = colHelper.column.toLowerCase();
                 if (tabName.equals("cost_items") || tabName.equals("measure_case_index")) {
                    if (index != 0) {
                       selBuf.append(",");
                       groupBuf.append(",");
                    }
                    selBuf.append(tabName).append(".").append(colName).append(" ").append("cl").append(index++);
                    groupBuf.append(tabName).append(".").append(colName);
                 }                 
              }//if valid dimInfo in dimCalc
          }//for each dimCalc          
          return index;
      }
      public static void appendCasePart(StringBuilder buf, String dataTabName, Connection dbConn, Cache cache, boolean incorporateLineSpecific, ArrayList filterList) throws Exception { 
          
          StringBuilder addTo = buf;
          boolean first = true;
          int szOfBeforeAdd = addTo.length();
          addTo.append(" case when ( "); //) then 1 else 0 end
          int lineSpecificIndex = 0;
          for (int i=0,is=filterList == null ? 0 : filterList.size();i<is;i++) {
              DimInfo.DimValList dv = (DimInfo.DimValList)(filterList.get(i));
              ArrayList valList = dv.getValList();
              DimInfo dimInfo = dv.m_dimInfo;
              if (dimInfo == null)
                continue;
              
              if (dv.getValList() == null || dv.getValList().size() == 0) { //read from session
                continue;

              }
              else  {

                 ColumnMappingHelper colHelper = dimInfo.m_colMap;
                 if (colHelper == null)
                    continue;
                 String tabName = colHelper.table.toLowerCase();
                 String colName = colHelper.column.toLowerCase();
                 int dimId = dimInfo.m_id;
                 
                 if (
                    "projects".equals(tabName) || "pj_basics".equals(tabName) || "alternatives".equals(tabName) ||
                    "workspaces".equals(tabName) || "alt_basics".equals(tabName) ||
                    "alt_map_items".equals(tabName) || "pj_map_items".equals(tabName) ||
                    "prj_plan_date_helper".equals(tabName)                        
                     && (!"dummy".equals(colName))) {
                    ArrayList selVal = cache.getInList(dbConn, dimInfo, Misc.getUndefInt(), valList, true);
                    if (selVal == null || selVal.size() == 0)
                        continue;

                    if (!first) {
                        addTo.append(" and ");
                    }
                    first = false;
                    addTo.append(tabName).append(".").append(colName).append(" in (");
                    Misc.convertInListToStr(selVal, addTo);
                    addTo.append(") ");                        
                 }//if not doing time related dims       
                 else if (incorporateLineSpecific && ( 
                    ("cost_items".equals(tabName) || "measure_case_index".equals(tabName))
                     && (!"dummy".equals(colName)))) {
                    ArrayList selVal = cache.getInList(dbConn, dimInfo, Misc.getUndefInt(), valList, true);
                    if (selVal == null || selVal.size() == 0)
                        continue;

                    if (!first) {
                        addTo.append(" and ");
                    }
                    first = false;
                    addTo.append(dataTabName).append(".").append("cl").append(lineSpecificIndex++).append(" in (");
                    Misc.convertInListToStr(selVal, addTo);
                    addTo.append(") ");                        
                 }//if not doing time related dims       
              }//if valid dimInfo in dimCalc
          }//for each dimCalc
          if (first) {
             addTo.setLength(szOfBeforeAdd);
          }
          else {
             addTo.append(" ) then 1 else 0 end ");
          }              
          
      }//end of func
   }//end of MiscInner.FilterHelper class

   static public class TimeWindow  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L; //relative to current
      public int m_loShow = 0;
      public int m_hiShow = 0;

      public int m_loEdit = 0;
      public int m_hiEdit = 0;
      public int m_showScope = 0; //lo and hi are in this scope e.g. year in devpage1.xml
      public int m_dataScope = 0; //eidts are in this scope, presumably lower e.g. qtr or mon
      boolean m_hasData = false;
      boolean m_hasMS = false;
   }

   static public class ProjectSummInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int m_projectId = Misc.getUndefInt();
      public int m_workspaceId = Misc.getUndefInt();
      public int m_alternativeId = Misc.getUndefInt();
      public int m_projectStatus = Misc.getUndefInt();
      public int m_simpleScenIncl = 1;
      public int m_budgetYear = Misc.getUndefInt();
      public ProjectSummInfo(int prjId, int wkspId, int altId, int prjStatus, int scenIncl, int budgetYear) {
          m_projectId = prjId;
          m_workspaceId =  wkspId;
          m_alternativeId = altId;
          m_projectStatus = prjStatus;
          m_simpleScenIncl = scenIncl;                
          m_budgetYear = budgetYear;
      }
   }
   
   public static class MultiAttribQueryParts  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L; //will be created in getEnhMulti
      public StringBuilder m_selClause = new StringBuilder();
      public StringBuilder m_fromClause = new StringBuilder();      
      public StringBuilder m_andClause = new StringBuilder();
      public StringBuilder m_groupClause = new StringBuilder();      
      public ArrayList m_controlDimInfo = new ArrayList(); //of DimInfo, the dimInfo whose LOV or type the values are  .. if null then this is String
      public int m_primaryObjectType = -1;
      public ArrayList m_paramRequired = new ArrayList();
   }
      
   
   
   
   static public class TimeReq  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;//for a query
//      public int m_numCurrDateReq = 0;
      final public static int G_CURRDATE = 100;
      final public static int G_CURRYEAR = 101;
      final public static int G_BUDYEAR = 102;
      final public static int G_PROJECT = 103;
      final public static int G_ALTERNATIVE = 104;
      final public static int G_WORKSPACE = 105;
      final public static int G_PORTFOLIO_ID = 106;
      //final public static int G_PAGE_CURRENCY_SEL =107;
      //final public static int G_ORGCONTEXT_REP_CURRENCY=108;
      //final public static int G_ORGCONTEXT_BUD_CURRENCY=109;
      final public static int G_OUTER_CURRENCY_TOUSE = 107;
      final public static int G_OUTER_UNIT_TOUSE = 108;
      final public static int G_CURRENCY_BUDGET = 109;
      final public static int G_CURRENCY_CURRENT = 110;
      final public static int G_CURRENCY_AUTH = 111;
      final public static int G_CURRENCY_SPOT = 115;
      final public static int G_BUDGET_ON = 120;
      
      
      //TODO -- add codes for currency, ui spec etc.

      public ArrayList m_paramRequired = new ArrayList(); //ArrayList of following codes: 100 => currdate, 101 => year timeid, 102 => budget year timeid, 103 projectId, 104 workspaceId, 105 alternativeId
      public ArrayList m_paramRequiredSel = new ArrayList();//TO_PORT_FORWARD
      public boolean m_attribSelAreNotPrjDep = false;
      //public boolean m_hasPrjPortfolioMap = false; //no longer  used ...
      public boolean m_hasOrdersTable = false;
      public boolean m_hasSuppliersTable = false;
      public boolean m_doAdjustmentForOrgAlloc = false; 
      public int m_numUserIdReqForPriv = 0; //3 the most common case, for priv subquery the # of userid params to set
      
//      public int m_desiredScope = -1; //for post set up of params!!
//      public int m_typeOfTimeReq = Misc.getUndefInt(); // -1001 => budget year, 0 => CY, -1002 => as provided by user
      
   }

   static public class LoadDataHelper implements Cloneable, Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public boolean m_editPastLine = false;
      public boolean m_editPastData = false;
      public boolean m_showAll = false;
      public int m_lo = 0;
      public int m_hi = 0;
      public int m_scope = Misc.SCOPE_ANNUAL;
      public int m_currTimeVal = 0;
      private double m_unit[] = null;
      public double getUnit(int qtyType) {
        if (m_unit == null || m_unit.length == 0)
           return 1;
        if (qtyType < 0 || qtyType >= m_unit.length)
          return m_unit[0];
        else return m_unit[qtyType];
      }

   //pastDataEditable => any data in window being shown is editable ..
   //all Show => all data being shown
   //  Otherwise m_lo[m_scope], m_hi[m_scope] is a fair bet about the presence/abscence of time in data

      public LoadDataHelper() {
         m_currTimeVal = TimePeriodHelper.getTimeVal(m_scope, (int) TimePeriodHelper.getTimeId(Misc.getCurrentDate()));
      }
      public LoadDataHelper(UIHelper uiHelper) {
         m_editPastLine = uiHelper.m_pastLineEditable;
         m_editPastData = uiHelper.m_pastDataEditable;
         m_scope = uiHelper.m_scope;
         m_lo = uiHelper.m_lo[m_scope];
         m_hi = uiHelper.m_hi[m_scope];
         m_showAll = uiHelper.m_showAll;
         m_currTimeVal = TimePeriodHelper.getTimeVal(m_scope, (int)TimePeriodHelper.getTimeId(Misc.getCurrentDate()));
         m_unit = new double[uiHelper.getUnitArray().length];
         for (int i=0,is=m_unit.length;i<is;i++)
            m_unit[i] = uiHelper.getUnit(i);
      }


      public void copyParamToElem(Element topElem) {
         topElem.setAttribute("_past_line", m_editPastLine ? "1" : "0");
         topElem.setAttribute("_past_data", m_editPastData ? "1" : "0");
         topElem.setAttribute("_scope", Integer.toString(m_scope));
         topElem.setAttribute("_lo", Integer.toString(m_lo));
         topElem.setAttribute("_hi", Integer.toString(m_hi));
         topElem.setAttribute("_show_all", m_showAll ? "1" : "0");
         for (int i=0,is = m_unit.length;i<is;i++)
            topElem.setAttribute("_unit"+Integer.toString(i), Double.toString(m_unit[i]));
      }

      public LoadDataHelper(Element topElem) { //if parameter does not exist then assume that pastEditable etc.!!
         m_editPastLine = !"0".equals(topElem.getAttribute("_past_line"));
         m_editPastData = !"0".equals(topElem.getAttribute("_past_data"));
         m_scope = Misc.getParamAsInt(topElem.getAttribute("_scope"), Misc.SCOPE_ANNUAL);
         m_lo = Misc.getParamAsInt(topElem.getAttribute("_lo"));
         m_hi = Misc.getParamAsInt(topElem.getAttribute("_hi"));
         m_showAll = !"0".equals(topElem.getAttribute("_show_all"));
         m_currTimeVal = TimePeriodHelper.getTimeVal(m_scope, (int)TimePeriodHelper.getTimeId(Misc.getCurrentDate()));
         m_unit = new double[5];
         for (int i=0,is = m_unit.length;i<is;i++)
            m_unit[i] = Misc.getParamAsDouble(topElem.getAttribute("_unit"+Integer.toString(i)),1);
      }

      public boolean copyPriorLines() {
         return !m_editPastLine;
      }

      public Pair getMinMax() {
         int mi = Misc.getUndefInt();
         int mx = Misc.getUndefInt();
         if (!m_showAll) {
            mi = m_lo;
            mx = m_hi;
         }
         if (!m_editPastData) {
           mi = 0;
         }
         return new Pair(mi,mx);
      }

	   public Object clone() {
		   Object clonedObj = null;
		   try {
			   clonedObj = super.clone();
		   }
		   catch (Exception ex) {
			   ex.printStackTrace();
		   }
		   return clonedObj;
	   }
   }
   
   public static class CurrencyInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public static int g_defaultCurrencyCode = 0;//JUBL 2;
      public int m_id = g_defaultCurrencyCode;
      public String m_iso3digitCode = null;
      public String m_2digitCode = null;
      public String m_userFriendlyName = null;
      java.util.Currency m_currency = null;
      public int m_unitCode = 0;
      public CurrencyInfo(int id, String iso3digitCode, String twoDigitCode, String userFriendlyName, int unitCode) {
          m_id = id;
          m_iso3digitCode = iso3digitCode;
          m_2digitCode = twoDigitCode;
          m_userFriendlyName = userFriendlyName;
          try {
            m_currency = Currency.getInstance(iso3digitCode);
          }
          catch(Exception ex) {
              m_iso3digitCode = "USD";
              m_currency = Currency.getInstance("USD");
          }
          m_unitCode = unitCode;
      }
            
   }
   
   public static class CountryInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public static int g_defaultCountryCode = 0;//JUBL 37;
      public int m_id = g_defaultCountryCode;
      public String m_iso2digitCode = null;
      public String m_3digitCode = null;
      public String m_userFriendlyName = null;
//      public String m_localeLangCode = null;
//      public Locale m_locale = null;
      
      
      public CountryInfo(int id, String iso2digitCode, String threeDigitCode, String userFriendlyName) {
         m_id = id;
         m_iso2digitCode = iso2digitCode;
         m_3digitCode = threeDigitCode;
         m_userFriendlyName = userFriendlyName;
      //   m_localeLangCode = localeLangCode;
      //   m_locale = locale;
      }
      
      
      
   }
   
   public static class UnitInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
       public int m_id = 0;
       public int m_qtyType = DimInfo.QTY_CURRENCY;
       public double m_unit = 1;
       public int m_decimal = 0;
       public int m_minDecimal = 0;
       public String m_prefix = Misc.emptyString;
       public String m_suffix = Misc.emptyString;
       public String m_displayName = Misc.emptyString;
       public String m_helperPattern = Misc.emptyString;
       
       public UnitInfo(int id, int qtyType, double unit, int decimal, String prefix, String suffix, String displayName, int minDecimal) {
          if (prefix == null)
             prefix = Misc.emptyString;
          if (suffix == null)
            suffix = Misc.emptyString;
          if (displayName == null)
            displayName = Misc.emptyString;
          m_id = id;
          m_qtyType = qtyType;
          m_unit = unit;
          m_decimal = decimal;
          m_prefix = prefix;
          m_suffix = suffix;
          m_displayName = displayName;
          m_minDecimal = minDecimal;
          if (m_minDecimal < 0)
        	  m_minDecimal = 0;
          if (m_minDecimal > m_decimal)
        	  m_minDecimal = m_decimal;
          StringBuilder temp = new StringBuilder("###,###");
          if (m_decimal > 0)
             temp.append(".");
          for (int i=0;i<m_minDecimal;i++) {
              temp.append("0");
           }
          
          for (int i=m_minDecimal;i<m_decimal;i++) {
             temp.append("#");
          }
          m_helperPattern = temp.toString();
       }
       public static UnitInfo g_defaultUnit = new UnitInfo(0,0,1,0, null,null,null,0);
       public static int g_defaultUnitCode = 1;
         
   }   
   
   public static class PortInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
   //these are to help map the attrib that are printed in the portfolio tree
      public static final String M_ID = "i";
      public static final String M_REPCURRENCY = "rc";
      public static final String M_BUDCURRENCY = "di";
      public static final String M_COUNTRYCODE = "cc";
      public static final String M_NAME = "n";
      public static final String M_EXTREF = "ec";
      public static final String M_ORGTYPE = "ot";
      public static final String M_THRESH = "th";
      public static final String M_SN = "sn";
      public static final String M_SF2 = "sf2";
      public static final int G_DEFAULT_LOCALEID = 2; //JUBL 2;
      
      public int m_id = -1;
      public String m_name = null;
      public String m_extRef = null;      
      public int m_localeId = PortInfo.G_DEFAULT_LOCALEID; 
      public Locale m_locale = null;
      public int m_repCurrency = CurrencyInfo.g_defaultCurrencyCode; //attrub = 
      public int m_budCurrency = CurrencyInfo.g_defaultCurrencyCode;
      public int m_countryCode = CountryInfo.g_defaultCountryCode;
      public int m_currUnitCode = MiscInner.UnitInfo.g_defaultUnitCode;      
      public int m_orgType = 0;
      public double m_thresh = 0;
      public String m_sn = null;
      public String m_strField2 = null;
      public int m_parentId = Misc.getUndefInt(); //although data exists - used for constructing
      public PortInfo m_parent = null;
      public ArrayList m_children = null;      
      public boolean m_isExtended = false;
      public int m_consolidationStatus = Misc.getUndefInt();
      public int m_lhsNumber = -1;
      public int m_rhsNumber = 0;
      public int m_parentOneBelowAll = Misc.getUndefInt();
      public boolean m_hasSpatialTableCreated = false;
      public int getHighestParentOrg() {
    	  if (Misc.isUndef(this.m_parentOneBelowAll)) {
    		PortInfo curr = this;
  			for (; curr.m_parent != null && curr.m_parent.m_parent != null; curr = curr.m_parent) {
  				//yes empty
  			}
  			this.m_parentOneBelowAll = curr.m_id;
    	  }
    	  return this.m_parentOneBelowAll;
  	}
      
      public void addAsChildTo(PortInfo parent) {
          m_parent = parent;          
          if (parent.m_children == null)
             parent.m_children = new ArrayList();
          parent.m_children.add(this);          
      } 
      public PortInfo(int id) {
          m_id = id;
      }
      public void read(ResultSet rset) throws Exception {
        try {
          m_isExtended = 1 != rset.getInt(1);
          m_id = rset.getInt(2);
          m_name = rset.getString(3);
          m_parentId = Misc.getRsetInt(rset,4);
          m_extRef = rset.getString(5);
          m_orgType = Misc.getRsetInt(rset,6,0);
          m_budCurrency = Misc.getRsetInt(rset,7,MiscInner.CurrencyInfo.g_defaultCurrencyCode);
          m_repCurrency = Misc.getRsetInt(rset,8,m_budCurrency);             
          m_currUnitCode = Misc.getRsetInt(rset,10,MiscInner.UnitInfo.g_defaultUnitCode); //now it is the currency' unitCode             
          m_thresh = Misc.getRsetDouble(rset, 11);
          m_countryCode = Misc.getRsetInt(rset,12,MiscInner.CountryInfo.g_defaultCountryCode);
          m_sn = rset.getString(13);//4 digidt code
          if (m_sn == null || m_sn.length() == 0)
             m_sn = m_name;
          m_strField2 = rset.getString(14); //str
          m_consolidationStatus = Misc.getRsetInt(rset,15);
//             elem.setAttribute("t","pr");
//             elem.setAttribute("s","s");
          m_lhsNumber = Misc.getRsetInt(rset, 16);
          m_rhsNumber = Misc.getRsetInt(rset, 17);
        }
        catch (Exception e) {
           e.printStackTrace();
           throw e;
        }
      }
      //New for Tracking
      public static class OrgFlexParams 
      {//we could have parameters by Name, but I want to avoid the hashing/look up on string
       //instead the parameters are going to be indexed
    	  public static String g_emptyMarker = "$@$";
          public ArrayList m_intParams = new ArrayList(); //ArrayList of ArrayList of Integer
          public ArrayList m_stringParams = new ArrayList(); //ArrayList of ArrayList of String
          public ArrayList m_doubleParams = new ArrayList(); 
          //others as needed
          public ArrayList getIntParams(int index) { 
             return index >= 0 && index < m_intParams.size() ? (ArrayList)m_intParams.get(index) : null;
          }
          public ArrayList getStringParams(int index) { 
             return index >= 0 && index < m_stringParams.size() ? (ArrayList)m_stringParams.get(index) : null;
          }            
          public ArrayList getDoubleParams(int index) { 
             return index >= 0 && index < m_doubleParams.size() ? (ArrayList)m_doubleParams.get(index) : null;
          }
      }
      public OrgFlexParams m_orgFlexParams = null;      
      private boolean checkIfMergeUpForParamVal(int ty, int index) {
    	  DimInfo dimInfo = DimInfo.getDimInfo(ty == Cache.STRING_TYPE ? 383 : ty == Cache.NUMBER_TYPE ? 382 : 381);
    	  DimInfo.ValInfo vinfo = dimInfo == null ? null : dimInfo.getValInfo(index);
    	  if (vinfo != null) {
    		  return "1".equals(vinfo.getOtherProperty("merge_up"));
    	  }
    	  return false;
      }
      
      public int getIntParamImm(int index) {
    	  ArrayList temp = this.m_orgFlexParams == null ? null :  this.m_orgFlexParams.getIntParams(index);
          return temp  == null || temp.size() == 0 ? Misc.getUndefInt() : ((Integer) temp.get(0)).intValue();
      }
      public ArrayList getIntParams(int index) {
    	  return getIntParams(index, checkIfMergeUpForParamVal(Cache.LOV_TYPE, index));
      }
      public ArrayList getStringParams(int index) {
    	  return getStringParams(index, checkIfMergeUpForParamVal(Cache.NUMBER_TYPE, index));
      }
      public ArrayList getDoubleParams(int index) {
    	  return getDoubleParams(index, checkIfMergeUpForParamVal(Cache.STRING_TYPE, index));
      }
      
      public ArrayList getIntParams(int index, boolean mergeUp) 
      {
    	  ArrayList retval = null;
    	  boolean toCreateRetval = true;
         for (PortInfo curr = this; curr != null; curr = curr.m_parent) 
         {
            ArrayList temp = curr.m_orgFlexParams == null ? null :  curr.m_orgFlexParams.getIntParams(index);
            if (temp != null && temp.size() > 0) {
            	if (!mergeUp)
            		return temp;
            	else {
            		if (retval == null)
            			retval = temp;
            		else {
            			if (toCreateRetval) {
            				ArrayList temp1 = new ArrayList(retval);
                			temp1.addAll(retval);
                			toCreateRetval = false;
            			}
            			retval.addAll(temp);
            		}
            	}
            }
         }
         return retval;
      }
      public ArrayList getStringParams(int index, boolean mergeUp) 
      {
    	 ArrayList retval = null;
    	 boolean toCreateRetval = true;
         for (PortInfo curr = this; curr != null; curr = curr.m_parent) 
         {
            ArrayList temp = curr.m_orgFlexParams == null ? null :  curr.m_orgFlexParams.getStringParams(index);
            if (temp != null && temp.size() > 0) {
            	if (!mergeUp)
            		return temp;
            	else {
            		if (retval == null)
            			retval = temp;
            		else {
            			if (toCreateRetval) {
            				ArrayList temp1 = new ArrayList(retval);
                			temp1.addAll(retval);
                			toCreateRetval = false;
            			}
            			retval.addAll(temp);
            		}
            	}
            }
         }
         return retval;
      }   
      public ArrayList getDoubleParams(int index, boolean mergeUp) 
      {
     	 ArrayList retval = null;
    	 boolean toCreateRetval = true;
         for (PortInfo curr = this; curr != null; curr = curr.m_parent) 
         {
            ArrayList temp = curr.m_orgFlexParams == null ? null :  curr.m_orgFlexParams.getDoubleParams(index);
            if (temp != null && temp.size() > 0) {
            	if (!mergeUp)
            		return temp;
            	else {
            		if (retval == null)
            			retval = temp;
            		else {
            			if (toCreateRetval) {
            				ArrayList temp1 = new ArrayList(retval);
                			temp1.addAll(retval);
                			toCreateRetval = false;
            			}
            			retval.addAll(temp);
            		}
            	}
            }
         }
         return retval;
      }
      
      public static void loadGeneralParams(HashMap portInfoHashMap, Connection dbConn) throws Exception
      {
         try 
         {
            for (int art=0;art<3;art++)  //art == 0 => lov, 1 => string, 2=> double
            {
               
                String q = art == 0 ? "select port_node_id, param_id, param_val from org_lov_params order by port_node_id, param_id, seq" 
                           : art == 1 ?  "select port_node_id, param_id, param_val from org_string_params order by port_node_id, param_id, seq"
                           :  "select port_node_id, param_id, param_val from org_double_params order by port_node_id, param_id, seq"
                                    ;
                PreparedStatement ps = dbConn.prepareStatement(q);
                ResultSet rs = ps.executeQuery();
                int prevPortId = Misc.getUndefInt();
                ArrayList prevInfo = null;
                while (rs.next()) 
                {
                   int portId = rs.getInt(1);
                   if (prevPortId != portId)
                      prevInfo = null;
                   prevPortId = portId;
                   int paramId = Misc.getRsetInt(rs, 2);
                   if (Misc.isUndef(paramId))
                      continue;
                   if (prevInfo == null) 
                   {                      
                      PortInfo portInfo = (PortInfo) portInfoHashMap.get(new Integer(portId));
                      if (portInfo == null)
                         continue;                      
                      if (portInfo.m_orgFlexParams == null)
                         portInfo.m_orgFlexParams = new OrgFlexParams();                      
                      prevInfo = art == 0 ? portInfo.m_orgFlexParams.m_intParams : art == 1 ? portInfo.m_orgFlexParams.m_stringParams : portInfo.m_orgFlexParams.m_doubleParams;                      
                   }
                   //get enough space in the array
                   for (int i1=prevInfo.size()-1;i1<paramId;i1++) 
                   {
                      prevInfo.add(null);
                   }
                   if (art == 0) 
                   {
                      int paramVal = Misc.getRsetInt(rs,3);
                      if (Misc.isUndef(paramVal))
                         continue;
                      ArrayList addToThis = (ArrayList) prevInfo.get(paramId);
                      
                      if (addToThis == null) {
                    	  addToThis = new ArrayList();
                    	  prevInfo.set(paramId, addToThis);
                      }
                      addToThis.add(new Integer(paramVal));
                   }
                   else if (art == 1)
                   {
                     String paramVal = Misc.getRsetString(rs,3);
                      if (paramVal == null)
                          continue;
                      ArrayList addToThis = (ArrayList) prevInfo.get(paramId);
                      
                      if (addToThis == null) {
                    	  addToThis = new ArrayList();
                    	  prevInfo.set(paramId, addToThis);
                      }
                      addToThis.add(paramVal);

                   }
                   else 
                   {
                      double paramVal = Misc.getRsetDouble(rs,3);
                      if (Misc.isUndef(paramVal))
                         continue;
                      ArrayList addToThis = (ArrayList) prevInfo.get(paramId);
                      
                      if (addToThis == null) {
                    	  addToThis = new ArrayList();
                    	  prevInfo.set(paramId, addToThis);
                      }
                      addToThis.add(new Double(paramVal));
                   }
                }//end of rs
                rs.close();
                ps.close();
            }//end of art loop
            //end of func
         }
         catch (Exception e) 
         {
            e.printStackTrace();
            throw e;
         }
      }
      //End of new for tracking

      
      public static PortInfo read(Element elem) { //to be gotten rid of
         if (elem == null)
            return null;
         PortInfo retval = new PortInfo(0);
         retval.m_id = Misc.getParamAsInt(elem.getAttribute("i"));
         retval.m_repCurrency = Misc.getParamAsInt(elem.getAttribute("rc"));
         retval.m_budCurrency = Misc.getParamAsInt(elem.getAttribute("dc"));
         retval.m_currUnitCode = Misc.getParamAsInt(elem.getAttribute("cu"));
         retval.m_countryCode = Misc.getParamAsInt(elem.getAttribute("cc"));
         int localeId = Misc.getParamAsInt(elem.getAttribute("loc"),PortInfo.G_DEFAULT_LOCALEID);
         retval.m_localeId = localeId;
         retval.m_locale = Cache.getLocale(localeId);
         return retval;
         
         
      }
   }
   
   public static class ContextInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      
      public Locale m_locale = null;
      //public CountryInfo m_countryInfo = null;
      public UnitInfo m_unitInfo = null;
      public CurrencyInfo m_currencyInfo = null;
      private FmtI.Date m_dateFormatter = null;
      private FmtI.Date m_dateTimeFormatter = null;
      
      private FmtI.Number m_numberFormatter = null;
      private FmtI.Currency m_currencyFormatter = null;
      private FmtI.Number m_unscaledFormatter = null;
      public ContextInfo (Locale locale, UnitInfo unit, CurrencyInfo currency) {
          m_locale = locale;
          m_unitInfo = unit;
          m_currencyInfo = currency;
          m_dateFormatter = new FmtI.Date(m_locale);
          m_dateTimeFormatter = new FmtI.Date(m_locale, true);
          m_numberFormatter = new FmtI.Number(m_locale, m_unitInfo);
          //m_currencyFormatter = new FmtI.Currency(m_locale, m_unitInfo, m_currencyInfo); will create this on demand since used only rarely
      }
      
      public String getCurrency() {
          return m_currencyInfo.m_iso3digitCode;
      }
      
      public String getUnitsCurrency() {
         String retval = m_unitInfo.m_displayName;
         if (retval != null && retval.length() != 0)
            retval += " ";
         retval += m_currencyInfo.m_iso3digitCode;
         return retval;
      }
      public FmtI.Date getDateFormatter() {
          return m_dateFormatter;
      }
      public FmtI.Date getDateTimeFormetter() {
    	  return m_dateTimeFormatter;
      }
      public String getDatePattern() {
         return m_dateFormatter.getPattern();         
      }
      public String getDateTimePatter() {
    	  return m_dateTimeFormatter.getPattern();
      }
      public FmtI.Number getNumberFormatter() {
          return m_numberFormatter;
      }
      public FmtI.Currency getCurrencyFormatter() {
          if (m_currencyFormatter == null)
             m_currencyFormatter = new FmtI.Currency(m_locale, m_currencyInfo, m_unitInfo);
          return m_currencyFormatter;
      }
      public FmtI.Currency getCurrencyFormatter(int currencyId) {
         if (m_currencyFormatter == null)
             m_currencyFormatter = new FmtI.Currency(m_locale, m_currencyInfo,m_unitInfo);
         if (currencyId == m_currencyInfo.m_id)
            return m_currencyFormatter;
         else
            return new FmtI.Currency(m_locale, Cache.getCurrencyInfo(currencyId), m_unitInfo);
      }
      public FmtI.Number getUnscaledFormatter() {
         if (m_unscaledFormatter == null) {         
            m_unscaledFormatter = new FmtI.Number(m_locale);
         }
         return m_unscaledFormatter;
      }
   }
   
   public static class QueryCache  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int m_measureId = Misc.getUndefInt();
      public StringBuilder m_queryPart = new StringBuilder();
      public ArrayList m_paramPart = new ArrayList();
      public String m_name = null;
      boolean matches(QueryCache rhs) {
         boolean retval = false;
         if (m_measureId == rhs.m_measureId && m_paramPart.size() == rhs.m_paramPart.size() && m_queryPart.length() == rhs.m_queryPart.length()) {
            //do a more thorough check
            for (int i=0,is = m_paramPart.size();i<is;i++) {
               Integer l = (Integer)m_paramPart.get(i);
               Integer r = (Integer)rhs.m_paramPart.get(i);
               if (l.intValue() != r.intValue())
                  return false;
            }
            if (m_queryPart.toString().equals(rhs.m_queryPart.toString()))
                return true;            
         }         
         return false;         
      }    
   }
   
   public static class QueryCacheList  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public static class Result  implements Serializable {
          /**
  		 * 
  		 */
  		private static final long serialVersionUID = 1L;
         public boolean m_isNew = false;
         public QueryCache m_queryCache = null;
         public Result(boolean isNew, QueryCache queryCache) {
            m_queryCache = queryCache;
            m_isNew = isNew;
         }
      }
      public ArrayList m_list = null;
      public Result getAndOptionallyAdd(QueryCache newQueryInfo) {
         if (m_list == null) {
            m_list = new ArrayList();            
         }
         QueryCache found = null;
         for (int i=0,is=m_list.size();i<is;i++) {
            QueryCache qc = (QueryCache) m_list.get(i);
            if (newQueryInfo.matches(qc)) {
               found = qc;
               break;               
            }
         }
         boolean isNew = false;
         if (found == null) {
            isNew = true;
            m_list.add(newQueryInfo);
            newQueryInfo.m_name = "tc"+Integer.toString(m_list.size());            
            found = newQueryInfo;
         }
         return new Result(isNew, found);         
      }
   }
   /* commented out ... relevant for CapEx
   public static class NPVHelper {
       public SimpleFTESet m_rev = null;
       public SimpleFTESet m_devCost = null;
       public SimpleFTESet m_opCost = null;
       public HashMap m_npvSpecParam = null;
       public int m_revModelId = Misc.getUndefInt();
       public int m_opCostModelId = Misc.getUndefInt();
       public int m_devCostModelId = Misc.getUndefInt();
       public int m_startYear = Misc.getUndefInt();
       public int m_endYearExcl = Misc.getUndefInt();
       
   }
   */
   public static class ModelDataHelper  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
       public String m_dataQuery = null;
       public String m_dataQuery2 = null;
       public String m_fileVerQuery = null;
       public String m_modelIdPos = null;
       public String m_minMaxQuery = null;
   }
   
   public static class SearchBoxHelper  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
       public int m_maxCol = 0;
       public String m_topPageContext = null;
       public ArrayList m_searchParams = null; //ArrayList of ArrayList of DimConfigInfo - for example pageHeader.m_allRows;
       public FrontGetValHelper m_valGetter = null;
       public int m_privIdForOrg = Misc.getUndefInt();
       public SearchBoxHelper(int maxCol, String topPageContext, ArrayList searchParams, FrontGetValHelper valGetter, int privIdForOrg) {
           m_maxCol = maxCol;
           m_topPageContext = topPageContext;
           m_searchParams = searchParams;
           m_valGetter = valGetter;
           m_privIdForOrg = privIdForOrg;
       }
       
       public DimConfigInfo getDimConfigInfo(int dimId) {
    	   return getDimConfigInfo("d"+dimId);
       }
       public DimConfigInfo getDimConfigInfo(String colName) {
    	   for (int i=0,is=m_searchParams == null ? 0 : m_searchParams.size(); i<is;i++) {
    		   ArrayList row = (ArrayList) m_searchParams.get(i);
    		   for (int j=0,js=row == null ? 0 : row.size();j<js;j++) {
    			   DimConfigInfo dc = (DimConfigInfo)row.get(j);
    			   if (colName.equals(dc.m_columnName) || (dc.m_dimCalc != null && dc.m_dimCalc.m_dimInfo != null && ("d"+dc.m_dimCalc.m_dimInfo.m_id).equals(dc)) || dc.equals(dc.m_paramName))
    				   return dc;
    		   }
    	   }
    	   return null;
       }
       public MiscInner.PairIntStr getSearchValue(SessionManager session, int dimId, boolean bySubsetOf) {//bySubsetof == true => get value for dim that may be subset of asked dim
    	   if (!bySubsetOf)
    		   return getSearchValue(session, dimId);
    	   for (int i=0,is = m_searchParams== null ? 0 : m_searchParams.size(); i<is;i++) {
    		   ArrayList<DimConfigInfo> row = (ArrayList<DimConfigInfo>) m_searchParams.get(i);
    		   for (int j=0,js =row == null ? 0 : row.size(); j<js;j++) {
    			   DimConfigInfo dc  = row.get(j);
    			   if (dc != null && dc.m_dimCalc != null && dc.m_dimCalc.m_dimInfo != null && dc.m_dimCalc.m_dimInfo.m_subsetOf == dimId)
    				   return getSearchValue(session, dimId, dc);
    		   }
    	   }
    	   return null;
       }

       public MiscInner.PairIntStr  getSearchValue(SessionManager session, int dimId) {
    	   return getSearchValue(session, dimId, null);
       }
       
       public MiscInner.PairIntStr  getSearchValue(SessionManager session, int dimId, DimConfigInfo dimConfig) {
    	   DimInfo dimInfo = DimInfo.getDimInfo(dimId);
    	   if (dimInfo == null)
    		   return null;
    	   int paramId = dimInfo.m_id;
    	   boolean is123 = dimInfo.m_descDataDimId == 123;
           String paramName = m_topPageContext+paramId;
           String paramVal = session.getParameter(paramName);
           if (paramVal != null && paramVal.length() != 0)
        	   return new MiscInner.PairIntStr(paramId, paramVal);
           paramName = "pv"+paramId;
           paramVal = session.getParameter(paramName);
           if (paramVal != null && paramVal.length() != 0)
        	   return new MiscInner.PairIntStr(paramId, paramVal);
           if (dimConfig == null)
        	   dimConfig = getDimConfigInfo(dimId);
    	   if (dimConfig != null && dimConfig.m_paramName != null && dimConfig.m_paramName.length() != 0) {
    		   paramName =	 
         			  dimConfig.m_setAsPermInSearchBox ? dimConfig.m_paramName : m_topPageContext+dimConfig.m_paramName
         			  ;
    		   paramVal = session.getParameter(paramName);
    	   }
    	   return new MiscInner.PairIntStr(paramId, paramVal);
       }
       //public SearchBoxHelper(int maxCol, String topPageContext) { //for backward compatability  
       //   m_maxCol = maxCol;
       //    m_topPageContext = topPageContext;
       //    m_searchParams = null;
       //    m_valGetter = null;
       //}
   }
   
   public static class PurSearchCondPart  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;       
       public int m_condType = 0;
       public String m_v1Val = null;
       public String m_v2Val = null;       
   }
   
   public static class PurSearchItemPart  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;      
       public DimInfo m_dimInfo = null;
       public ArrayList m_purConds = null; //of PurSearchCondPart
   }
   
   public static class PurJoinNeeded  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
   /*
    <val id="0" name="Orders - Awarded"/>
        <val id="1" name="CCBS - Awarded"/>
        <val id="2" name="Orders - Associated"/>
        <val id="3" name="CCBS - Associated"/> 
                <val id="0" name="Projects"/>
        <val id="1" name="CCBS"/>

    */
      public boolean m_suppToOrdersAwarded = false; 
      public boolean m_suppToCCBSAwarded = false;      
      public boolean m_suppToOrdersAssoc = false; 
      public boolean m_suppToCCBSAssoc = false;      
            
      public boolean m_orderToCCBS = false;
   }
   
   public static class PurFilterInterpret  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public StringBuilder m_query = null;
      public ArrayList m_paramList = null;
      public ArrayList m_paramType = null;
      
      public PurJoinNeeded m_joinNeeded = null;
   }
   
   
   public static class PurSearchTablesUsed  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L; //will need to get rid of this
       public boolean m_projects = false;
       public boolean m_prj_basics = false;
       public boolean m_alt_basics = false;
       public boolean m_alternatives_basics = false;
       public boolean m_projects_summary = false;
       public boolean m_pur_cost_items = false;
       public boolean m_pur_cost_item_details = false;
       public boolean m_pur_cost_items_summary = false;
       public boolean m_orders = false;
       public boolean m_order_details = false;
       public boolean m_suppliers = false;
       public boolean m_supplier_details = false;
   }
   
   public static class PairIntDimConfigInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
       public int first = 0;
       public DimConfigInfo second = null;
       public PairIntDimConfigInfo(int vf, DimConfigInfo vs) {
          first = vf;
          second = vs;
       }
   }
   
   
   public static class PurViewCriteria  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public ArrayList m_dimConfigList = new ArrayList(); 
      //public ArrayList m_ccbsParamList = new ArrayList(); //array list of param id(s)
      //public ArrayList m_ccbsRatioParamList = new ArrayList();
      public int m_viewCCBSId = -1;
      //public boolean m_hasCostComponent = false; //not sure if this is useful
      //public boolean m_hasCostComponentSubCCBS = false; //not sure if this is useful
      //public boolean m_hasRatioParamCalc = false; //not sure if this is useful
      //public boolean m_hasParamAsk = false; //not sure if this is useful
      public int m_searchType = 0;
      public FrontGetValHelper m_valGetter = null;
      public HashMap m_accessCheckControlInfo = null;
   }
   
   public static class PurQueryGen  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public String m_mainQuery = null;
      public ArrayList m_multiAttribQueryParts = null;      
      public ArrayList m_paramList = null;
   }
   
   public static class TablesUsed  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
     public HashMap m_tablesUsed = new HashMap(30,0.75f);
     public ArrayList m_orderedTablesUsed = new ArrayList();
     public void put(String tableName, String val) {
        if (!m_tablesUsed.containsKey(tableName)) {
          m_tablesUsed.put(tableName, val);
          m_orderedTablesUsed.add(tableName);
        }
     }
     public void add(String tableName) {
        put(tableName, tableName);
        //m_tablesUsed.put(tableName, tableName);
     }
     public boolean isUsed(String tableName) {
        return m_tablesUsed.containsKey(tableName);
     }
     public boolean containsKey(String table) {
        return m_tablesUsed.containsKey(table);
     }
     public String get(String table) {
        return (String) m_tablesUsed.get(table);
     }
   }
   
   public static class PurResults  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public ResultSet m_rset = null;
      public PurViewCriteria m_purViewCriteria = null;
      public PreparedStatement m_pstmt = null;
      public ArrayList m_multiAttribDataList = null; //Array of hashmap, indexed by dimConfig, hashMap key = itemId value = array of array of string
      
   
      public void cleanup() throws Exception {
         try {
             if (m_rset != null)
               m_rset.close();
             if (m_pstmt != null)
               m_pstmt.close();
         }
         catch (Exception e) {
            e.printStackTrace();
            throw e;
         }
      }      
   }

   public static class SimpleValidationInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	   public boolean m_faNegative = false;
	   public double m_changeInAuth = 0;
	   public double m_maxRevenueVal = 0;
	   public double m_maxActualsVal = 0;
	   public double m_maxForecastVal = 0;
   }

   public static class PairBoolDouble  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	   public boolean boolVal;
	   public double doubleVal;
	   public PairBoolDouble(boolean vf, double vs) {
		   boolVal = vf;
		   doubleVal = vs;
	   }
   }

   public static class ProjectValidationInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	   public int m_locationId;
	   public int m_projectId;
	   public int m_workspaceId;
	   public int m_alternativeId;
	   public String m_simpleValidationStr;
	   public int m_labelId;
	   public ProjectValidationInfo(int locationId, int prjId, int wkspId, int altId, String validationStr, int labelId) {
		   m_locationId = locationId;
		   m_projectId = prjId;
		   m_workspaceId = wkspId;
		   m_alternativeId = altId;
		   m_simpleValidationStr = validationStr;
		   m_labelId = labelId;
	   }
   }
   
   
   
   public static class DimReadVisible  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int m_dimId = Misc.getUndefInt();
      public boolean m_isRead = false;
      public boolean m_isHidden = false;
      public DimReadVisible() {
      }
      public DimReadVisible(int dimid) {
          m_dimId = dimid;
      }
      public DimReadVisible(int dimid, boolean isread, boolean ishidden) {
          m_dimId = dimid;
          m_isRead = isread;
          m_isHidden =  ishidden;          
      }
   }
   
   
   public static class ItemAccControlInfo  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
      public int m_objType = -1;
      public MiscInner.Pair m_objIndex = null; //if objtype == Prj, objIndex will have projectId , pair of rowIndex, colIndex in which to get val
      public boolean m_objInOuter = true;
      public MiscInner.Pair m_prjIdIndex = null;//if objtype == order then the prj id of the order , pair of rowIndex, colIndex in which to get val
      public boolean m_prjInOuter = true;
      public MiscInner.Pair m_orgIdIndex = null; //org of the obj type , pair of rowIndex, colIndex in which to get val      
      public MiscInner.Pair m_orgIdOfPrj = null; //if objType = prj then the prj's org id index , pair of rowIndex, colIndex in which to get val
      
    
   }
   
   public static class FrontPagePerRowPrivCheckColIndex  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;//0425
      public int m_prjRow = -1;
      public int m_prjCol = -1;
      public int m_prjOrgRow = -1;
      public int m_prjOrgCol = -1;
      public int m_objRow = -1;
      public int m_objCol = -1;
      public int m_objOrgRow = -1;
      public int m_objOrgCol = -1;      
      public FrontPagePerRowPrivCheckColIndex(int prjRow, int prjCol, int prjOrgRow, int prjOrgCol, int objRow, int objCol, int objOrgRow, int objOrgCol) {
          m_prjRow = prjRow;
          m_prjCol = prjCol;
          m_prjOrgRow = prjOrgRow;
          m_prjOrgCol = prjOrgCol;
          m_objRow = objRow;
          m_objCol = objCol;
          m_objOrgRow = objOrgRow;
          m_objOrgCol = objOrgCol;      
      }
   }
}

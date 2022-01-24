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

import org.w3c.dom.*;

   public class FrontGetValHelper  implements Serializable {
       /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
       private ArrayList m_dimConfigAs2d = new ArrayList(); //  of ArrayList of DimCOnfigInfo
       public ArrayList getDimConfigInfoList() { return m_dimConfigAs2d;} //earlier was DimInfo
       private int m_indexMap[][] = null; //2d array
       private ArrayList m_translateMap[][] = null; //2d array of ArrayList ... where most values are going to be null
       private ArrayList m_toAskDimListNew = new ArrayList(); //of DimCalc ... earlier was DimInfo
       public ArrayList m_adornmentSpec = new ArrayList(); // if one of the data points requires adornment with currency/qty etc.

       public ArrayList getToAskDimListNew() {
          return m_toAskDimListNew;
       }

       public FrontGetValHelper(ArrayList dimConfigInfoList, boolean is2d) {
          int rowCount = is2d ? dimConfigInfoList == null ? 0 : dimConfigInfoList.size() : 1;
          m_indexMap = new int[rowCount][];
          m_translateMap = new ArrayList[rowCount][];
          if (is2d) {
             for (int i=0,is = dimConfigInfoList == null ? 0 : dimConfigInfoList.size();i<is;i++) {

                ArrayList dl = (ArrayList) dimConfigInfoList.get(i);
                m_dimConfigAs2d.add(dl);
                if (dl == null || dl.size() == 0) {
                   m_indexMap[i] = null;
                   m_translateMap[i] = null;
                   continue;
                }
                m_indexMap[i] = new int[dl.size()];
                m_translateMap[i] = new ArrayList[dl.size()];
                convertInfoRequestInQueryAsk(dl, m_toAskDimListNew, m_indexMap[i], m_translateMap[i], m_adornmentSpec);
             }
          }
          else {
             ArrayList dl = dimConfigInfoList;
             m_dimConfigAs2d.add(dl);
             if (dl == null || dl.size() == 0) {
                m_indexMap[0] = null;
                m_translateMap[0] = null;
             }
             else {
                m_indexMap[0] = new int[dl.size()];
                m_translateMap[0] = new ArrayList[dl.size()];
                convertInfoRequestInQueryAsk(dl, m_toAskDimListNew, m_indexMap[0], m_translateMap[0], m_adornmentSpec);
             }
          }
       }

       public int getRsetColCount() {
          return m_toAskDimListNew.size();
       }
       public int getAdornmentSpec(int i, int j) {
          int index = m_indexMap[i][j];
          if (index >= 0 && m_adornmentSpec != null && m_adornmentSpec.size() > index) {
             Integer spec = ((Integer)m_adornmentSpec.get(index));             
             if (spec != null) {
                return spec.intValue();   
             }
          }
          return 0;
       }
       
       public int getCurrencyFor(Connection dbConn, ResultSet rset, int i, int j, Cache cache) {
          int retval = Misc.getUndefInt();
          try {
             int index = m_indexMap[i][j-1]; //yes we want imm prior
             if (index >= 0 ) {
                retval = Misc.getRsetInt(rset, index+1); 
             }      
          }
          catch (Exception e) { //just be safe
          }
          return retval;
       }
       public int getIndexInRset(int row, int col) {
           return m_indexMap[row][col];
       }
       public int getValInt(Connection dbConn, ResultSet rset, int row, int col, Cache cache) throws Exception {
          try {
             int retval = 0;
             int index = m_indexMap[row][col];
             ArrayList translate = m_translateMap[row][col];
             ArrayList dimConfigList = (ArrayList) m_dimConfigAs2d.get(row);
             DimConfigInfo dimConfig = (DimConfigInfo) dimConfigList.get(col);
             DimInfo dimInfo = dimConfig == null ? null : dimConfig.m_dimCalc.m_dimInfo;
             if (dimConfig.m_calcByMap != null && dimConfig.m_calcByMap.size() > 0) {
                int v = 0;
                for (int t1=0,t1s = dimConfig.m_calcByMap.size();t1<t1s;t1++) {
                   DimConfigInfo.Pair pos = (DimConfigInfo.Pair) dimConfig.m_calcByMap.get(t1);
                   v += getValInt(dbConn, rset, pos.m_rowIndex, pos.m_colIndex, cache);
                }
                return v;
             }
             if (dimInfo == null)
                return 0;
             int descDimId = dimInfo.m_descDataDimId;
             if (translate == null || translate.size() < 1) {
                retval = index < 0 ? 0 : rset.getInt(index+1);
                if (rset.wasNull()) {
                   retval = cache.getDefaultDimVal(descDimId);
                }
                else {
                   retval = cache.getValidatedDimVal(descDimId, retval);
                }
                
                
                retval = cache.getParentDimValId(dbConn, dimInfo, retval);

                return retval;
             }
             else { //TODO
               //m_toAskDimList contains the DimCalc for the dims asked ... look at getRsetDimBased and do the calculation
             }
             return 0;
          }
          catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
       }
       public java.sql.Date getValDate(Connection dbConn, ResultSet rset, int row, int col, Cache cache) throws Exception {
          try {
             int index = m_indexMap[row][col];
             java.sql.Date retval = index < 0 ? null : rset.getDate(index+1);
             return retval;
          }
          catch (Exception e) {
             e.printStackTrace();
             throw e;
          }
       }
       public double getValDouble(Connection dbConn, ResultSet rset, int row, int col, Cache cache) throws Exception {
          try {
             double retval = 0;
             if (rset == null)
                return retval;
             int index = m_indexMap[row][col];
             ArrayList translate = m_translateMap[row][col];
             ArrayList dimConfigList = (ArrayList) m_dimConfigAs2d.get(row);
             DimConfigInfo dimConfig = (DimConfigInfo) dimConfigList.get(col);
             DimInfo dimInfo = dimConfig == null ? null : dimConfig.m_dimCalc.m_dimInfo;
             if (dimConfig.m_calcByMap != null && dimConfig.m_calcByMap.size() > 0) {

                for (int t1=0,t1s = dimConfig.m_calcByMap.size();t1<t1s;t1++) {
                   DimConfigInfo.Pair pos = (DimConfigInfo.Pair) dimConfig.m_calcByMap.get(t1);
                   retval += getValDouble(dbConn, rset, pos.m_rowIndex, pos.m_colIndex, cache);
                }
                return retval;
             }
             if (dimInfo == null)
                return 0;


             int dimId = dimInfo.m_id;
             if (translate == null || translate.size() < 1) {
                retval = index < 0 ? 0 : rset.getDouble(index+1);
                if (rset.wasNull()) {
                   retval = dimInfo.getDefaultDouble();
                }
                return retval;
             }
             else { //TODO
               //m_toAskDimList contains the DimCalc for the dims asked ... look at getRsetDimBased and do the calculation
             }
             return retval;
          }
          catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
       }

       public String getVal(Connection dbConn, ResultSet rset, int row, int col, Cache cache) throws Exception {
          try {
             String retval = null;
             int index = m_indexMap[row][col];
             ArrayList translate = m_translateMap[row][col];
             ArrayList dimConfigList = (ArrayList) m_dimConfigAs2d.get(row);
             DimConfigInfo dimConfig = (DimConfigInfo) dimConfigList.get(col);
             DimInfo dimInfo = dimConfig == null ? null : dimConfig.m_dimCalc.m_dimInfo;
             if (dimConfig.m_calcByMap != null && dimConfig.m_calcByMap.size() > 0) {
                double v = 0;
                for (int t1=0,t1s = dimConfig.m_calcByMap.size();t1<t1s;t1++) {
                   DimConfigInfo.Pair pos = (DimConfigInfo.Pair) dimConfig.m_calcByMap.get(t1);
                   v += getValDouble(dbConn, rset, pos.m_rowIndex, pos.m_colIndex, cache);
                }
                return Misc.m_currency_formatter.format(v);
             }
//@#@#@             if (dimConfig.m_i
             if (dimInfo == null)
                return "";

             int dimId = dimInfo.m_id;
             if (translate == null || translate.size() < 1) {
                retval = index < 0 ? "" : Misc.getRsetDimBased(dbConn, rset, index+1, dimInfo, cache);
                return retval;
             }
             else { //TODO
               //m_toAskDimList contains the DimCalc for the dims asked ... look at getRsetDimBased and do the calculation
             }
             return "";
          }
          catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
       }

       private static ArrayList hackTranslate(int dimId) {
         return null;
       }

       private static void convertInfoRequestInQueryAsk(ArrayList dimConfigInfoList, ArrayList dimCalcList, int indexMap[], ArrayList translateMap[], ArrayList adornmentSpec) { //will read the dimConfigInfoList ... and according to hack for some combo measures/properties will get you the dimList of integer
          int nextIndex = dimCalcList.size();
          for (int i=0,is = dimConfigInfoList.size();i<is;i++) {
             DimConfigInfo dimConfig = (DimConfigInfo) dimConfigInfoList.get(i);
             int adornForThis = 0;//none
             DimCalc tdimCalc = dimConfig != null ? dimConfig.m_dimCalc : null;
             if (tdimCalc != null && tdimCalc.m_filterBy != null && tdimCalc.m_filterBy.size() > 0 && adornmentSpec != null) {
                for (int j=0,js=tdimCalc.m_filterBy.size();j<js;j++) {
                   DimInfo.DimValList dv = (DimInfo.DimValList)(tdimCalc.m_filterBy.get(j));
                   ArrayList valList = dv.getValList();
                   DimInfo dimInfo = dv.m_dimInfo;
                   if (dimInfo == null)
                     continue;
                   int dimId = dimInfo.m_id;
                   if (dimId == 252) { //adornmentSpec
                       adornForThis = ((Integer)valList.get(0)).intValue();
                   }                 
                }//if not doing time related dims
             }//for each
             adornmentSpec.add(new Integer(adornForThis));
        
             int dimId = dimConfig == null || dimConfig.m_dimCalc.m_dimInfo == null  ? Misc.getUndefInt() : dimConfig.m_dimCalc.m_dimInfo.m_id;

             indexMap[i] = nextIndex;
             ArrayList translate = hackTranslate(dimId);
             translateMap[i] = translate;
             if (dimConfig.m_calcByMap != null && dimConfig.m_calcByMap.size() > 0) {
                indexMap[i] = -1;
             }
             else if (translate == null || translate.size() <= 1) {
                dimCalcList.add(dimConfig.m_dimCalc);
                nextIndex++;
             }
             else {
                for (int j=0,js = translate.size();j<js;j++) {
                  DimInfo dimInfo = DimInfo.getDimInfo(((Integer)translate.get(j)).intValue());
                  DimCalc dimCalc = new DimCalc(dimInfo, null, null);
                  dimCalcList.add(dimCalc);
                }
                nextIndex += translate.size();
             }//if not translate
          }//for each dimAsked
       }//end of func
   } //end of inner class

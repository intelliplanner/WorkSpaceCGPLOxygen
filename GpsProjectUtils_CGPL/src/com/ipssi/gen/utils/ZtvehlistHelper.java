//Title:        Your Product Name
//Version:
//Copyright:   Copyright (c) 1999
//Author:      Your Name
//Company:     Your Company
//Description:
package com.ipssi.gen.utils;

import java.sql.*;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.xpath.*;

public class ZtvehlistHelper {
	private HttpServletRequest m_request = null;
	private ServletContext m_context = null;
	public SessionManager m_session = null;
	private User m_user = null;
	private Cache m_cache = null;
	private Logger m_log = null;
	private Connection dbConn = null;

	public ZtvehlistHelper(HttpServletRequest request, ServletContext context) {
		m_request = request;
		m_context = context;
		m_cache = (Cache)context.getAttribute("_cache");
		m_session = (SessionManager)request.getAttribute("_session");
		m_user = (User)request.getAttribute("_user");
		m_log = (Logger)request.getAttribute("_log");
		dbConn = (Connection)request.getAttribute("_dbConnection");
	}
  
  public static MiscInner.PairBool getFilterPart(SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper, ArrayList searchRowColInfos, StringBuilder fromPart, StringBuilder wherePart, ArrayList paramList, ArrayList paramType, MiscInner.ContextInfo contextInfo) throws Exception {    
  // returns pair of (UsesVehicleInfo in from, UsesVehicleAccessGroup in from)
     //for the case in consideration, all items from the filter part will come from vehicle_info and vehicle_access_group
     //so we use booleans, but in general case we use HashMap for knowing the tables that have been put in fromClause
     
     boolean putVehicleInfoInFrom = false;
     boolean putVehicleAccessInFrom = false;
     //suppose we have both then the from would look like
     // port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)
     //join vehicle_access_group on (vehcile_access_group.port_node_id = leaf.id)
     //join vehicle_info on (vehicle_info.id = vehicle_access_group.vehicle_id)
     
     //Note we are putting the anc.id = ? or anc.id in () inside the join clause. Technically there is no reason for that, but for
     //SQL Server this approach was found to be more performant
     //Similarly you will notice that the vehicleInfo has been put as the last join - technically there is no reason, but the order
     //of join clauses is such as to indicate the best order for loop join hint (most selective being the first to be evaluated)
     
     //If there are multiple values for filter we will do in construct (and put the the values in the query) other we will use ?
     //Texts are going to use like operator and put into the query
     
     //Approach: go through each dim, make sure there is a value and that value is not Any (or 1 for Org based filter)
     //          note down tables are needed by setting the put boolean variables above
     //          keep on building the where part and parmList and paramType (as mentioned the parameters for org are treated separately)
     
     
     
     FmtI.Number unscaledFmt = contextInfo.getUnscaledFormatter(); //in our case we will mostly use this (numberFormatter is for currency)
     FmtI.Date dtFmt = contextInfo.getDateFormatter();
     
     Cache cache = session.getCache();
     Connection dbConn = session.getConnection();
     ArrayList v123ParamList = null; //as noted above the cond clause for anc is kept in the join part itself ... so this is to remember that
     for (int i=0,is=searchRowColInfos.size();i<is;i++) {
        ArrayList searchRow = (ArrayList) searchRowColInfos.get(i);
        for (int j=0,js=searchRow.size();j<js;j++) {
           DimConfigInfo dimConfig = (DimConfigInfo)searchRow.get(j);
           DimInfo dimInfo = dimConfig != null && dimConfig.m_dimCalc != null ? dimConfig.m_dimCalc.m_dimInfo : null;
           ColumnMappingHelper colMap = dimInfo == null ? dimInfo.m_colMap : null;
           if (dimInfo != null && !colMap.table.equals("Dummy") && !colMap.column.equals("Dummmy")) { //conventionally "Dummy" indicates an attribute that doesn't map to db
              boolean is123 = false;
              if (dimInfo.m_descDataDimId == 123) {//all organizations dims are subset of 123 and ancestors of 123, so to check if you are dealing with
                                                   //organization then use this construct                    
                  is123 = true;
              }
              
              String paramName = is123 ? "pv123" : searchBoxHelper.m_topPageContext + Integer.toString(dimInfo.m_id);                            
              String paramVal = session.getParameter(paramName);
              if (paramVal == null || paramVal.length() == 0)
                 continue;
              
              int dimAttribType = dimInfo.getAttribType();
              ArrayList actualInListByLookingAtDesc = null;
              java.sql.Date paramDate = null;
              double paramDouble = Misc.getUndefDouble();
              if (dimAttribType == Cache.LOV_NO_VAL_TYPE || dimAttribType == Cache.LOV_TYPE || dimAttribType == Cache.INTEGER_TYPE) {                
                  ArrayList paramValList = new ArrayList();
                  Misc.convertValToVector(paramVal, paramValList);
                  
                  if (paramValList == null || paramValList.size() == 0 || Misc.isInList(paramValList, is123 ? 1 : Misc.G_HACKANYVAL)) {//why continue .. these cover all cases
                     continue;
                  }                  
                  //what the heck is below? well dimensions can be grouped into hierarchy (for example Quarter -> month), data will be at
                  //lowest level .. so the code below converts the inlist at upper level to inlist at lower level                  
                  actualInListByLookingAtDesc = cache.getInList(dbConn, dimInfo, Misc.getUndefInt(), paramValList, dimInfo.m_descDataDimId != 123);
                  if (actualInListByLookingAtDesc == null || actualInListByLookingAtDesc.size() == 0) {
                     continue;
                  }
                  if (is123) {
                     v123ParamList = actualInListByLookingAtDesc;
                     putVehicleAccessInFrom = true;
                     continue;
                  }                  
              }
              else if (dimAttribType == Cache.DATE_TYPE) {
                 paramDate = dtFmt.getDate(paramVal, null);
                 if (paramDate == null)
                    continue;
              }
              else if (dimAttribType == Cache.NUMBER_TYPE) {
                 paramDouble = unscaledFmt.getDouble(paramVal, Misc.getUndefDouble());
                 if (Misc.isUndef(paramDouble))
                    continue;
              }
              if (colMap.table.equals("vehicle_info"))
                 putVehicleInfoInFrom = true;
              else if (colMap.table.equals("vehicle_access_group"))
                 putVehicleAccessInFrom = true;
              else
                 continue; //dont know how to filter
              if (wherePart.length() != 0)
                 wherePart.append(" and ");                            
              wherePart.append(colMap.table).append(".").append(colMap.column); //risky the table/cols can be complex
              
              if (dimAttribType == Cache.LOV_NO_VAL_TYPE || dimAttribType == Cache.LOV_TYPE || dimAttribType == Cache.INTEGER_TYPE) {                              
                  if (actualInListByLookingAtDesc.size() > 1) { //do as in
                     wherePart.append(" in (");
                     Misc.convertInListToStr(actualInListByLookingAtDesc, wherePart);
                     wherePart.append(" ) ");
                  }
                  else {
                     wherePart.append(" = ? ");
                     paramList.add(actualInListByLookingAtDesc.get(0));
                     paramType.add(new Integer(Cache.LOV_TYPE));
                  }
              }              
              else if (dimAttribType == Cache.STRING_TYPE){//doing string 
                  wherePart.append(" like '%").append(paramVal).append("%' ");
              }
              else if (dimAttribType == Cache.DATE_TYPE) {                 
                  wherePart.append(dimConfig.m_forDateApplyGreater ? " >= " : " <= ").append("? ");                  
                  paramList.add(paramDate);
                  paramType.add(new Integer(Cache.DATE_TYPE));
              }
              else if (dimAttribType == Cache.NUMBER_TYPE) {
                  wherePart.append(dimConfig.m_forDateApplyGreater ? " >= " : " <= ").append("? ");                  
                  paramList.add(new Double(paramDouble));
                  paramType.add(new Integer(Cache.NUMBER_TYPE));
              }
              else { //dont know how to filter ... just continue
                  continue;
              }
           }//if valid dimInfo
        }//for each col
     }//for each row   
     //now get the from part
     if (putVehicleAccessInFrom) {
     // port_nodes anc join port_nodes leaf on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number)
     //join vehicle_access_group on (vehcile_access_group.port_node_id = leaf.id)
     //join vehicle_info on (vehicle_info.id = vehicle_access_group.vehicle_id)
         fromPart.append(" port_nodes anc join port_nodes leaf on (anc.id");
         if (v123ParamList.size() > 1) {
            fromPart.append(" in (");
            Misc.convertInListToStr(v123ParamList, fromPart);
            fromPart.append(") ");
         }
         else {
            fromPart.append(" = ? ");
            paramList.add(0, v123ParamList.get(0));
            paramType.add(0, new Integer(Cache.INTEGER_TYPE));
         }
         fromPart.append(" and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join vehicle_access_group on (vehcile_access_group.port_node_id = leaf.id) ");
     }
     if (putVehicleInfoInFrom) {
        if (putVehicleAccessInFrom) {
           fromPart.append(" join vehicle_info on (vehicle_info.id = vehicle_access_group.vehicle_id) ");        
        }
        else {
           if (fromPart.length() > 0)
              fromPart.append(",");
           fromPart.append("vehicle_info ");
        }
     }
     return new MiscInner.PairBool(putVehicleInfoInFrom, putVehicleAccessInFrom);
  
  }//end of function
  
  public static void helperCombineQueries(String selClause, StringBuilder retval, StringBuilder filterFromPart, StringBuilder filterWherePart, String mainTable, MiscInner.PairBool tablesInFilterFromPart, String addnlJoinClause) {
     retval.append(selClause);
     retval.append(" from ");
     retval.append(filterFromPart);
     
     if (mainTable.equals("vehicle_info")) {     
        if (tablesInFilterFromPart.first) { //do nothing
        }
        else if (tablesInFilterFromPart.second) { //need to join
           retval.append(" join vehicle_info on (vehicle_info.id = vehicle_access_group.vehicle_id) ");        
        }
        else {//just put
           retval.append(" vehicle_info ");
        }
     }
     else if (mainTable.equals("vehicle_access_group")) {
        
        if (tablesInFilterFromPart.second) { //do nothing           
        }
        if (tablesInFilterFromPart.first) { // join with vehcile_id
           retval.append(" join vehicle_access_group on (vehicle_info.id = vehicle_access_group.vehicle_id) ");        
        }
        else {//just put
           retval.append(" vehicle_access_group ");
        }
     }
     else {
        if (tablesInFilterFromPart.first) { 
           retval.append(" join ").append(mainTable).append(" on (").append(mainTable).append(".vehicle_id = vehicle_info.id) ");
        }
        else if (tablesInFilterFromPart.second) { //need to join           
           retval.append(" join ").append(mainTable).append(" on (").append(mainTable).append(".vehicle_id = vehicle_access_group.vehicle_id) ");
        }
        else {//just put
           retval.append(mainTable);
        }
     }
     if (addnlJoinClause != null && addnlJoinClause.length() != 0)
        retval.append(addnlJoinClause);
     if (filterWherePart.length() > 0) {
        retval.append(" where ").append(filterWherePart);
     }
     retval.append(" order by ");
     if (tablesInFilterFromPart.first || mainTable.equals("vehicle_info"))
        retval.append("vehicle_info.id ");
     else if (tablesInFilterFromPart.second)
        retval.append("vehicle_access_group.vehicle_id ");
     else
        retval.append(mainTable).append(".").append("vehicle_id ");
     
  }
  
  public static void putParams(PreparedStatement ps, int paramIndex, ArrayList paramList, ArrayList paramTypeList) throws Exception {
     for (int i=0,is=paramList.size();i<is;i++) {
         int paramType = ((Integer)paramTypeList.get(i)).intValue();
         switch (paramType) {
           case Cache.INTEGER_TYPE:
              Misc.setParamInt(ps,((Integer)paramList.get(i)).intValue(),paramIndex++); //not that the position of where to insert is at last
              break;
           case Cache.DATE_TYPE:
              ps.setDate(paramIndex++, (java.sql.Date)paramList.get(i));
              break;
           case Cache.NUMBER_TYPE:
              Misc.setParamDouble(ps,((Double)paramList.get(i)).doubleValue(),paramIndex++); //not that the position of where to insert is at last
              break;
           default:           
         }
     }
  }
}//end of class

	
package com.ipssi.gen.utils;
import java.io.Serializable;




  public class ColumnMappingHelper  implements Serializable {
      /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

     public String table = "";
     public String column = "";
     public int ty = Cache.LOV_TYPE;
     public String and_clause = "";
     public String base_table = "";
     public String joinClause = "";
     public String purJoinHint = ""; //order_to_ccbs, order_to_prj, supp_to_ccbs_awarded, supp_to_ccbs_assoc, supp_to_order_assoc, supp_to_order_awarded
     public boolean useColumnOnlyForName = false;
     public boolean doHavingInFilter = false;
     boolean columnHasAggregation = false;
     public String idField = null;
     public String nameField = null;
     public boolean appendTableColName(StringBuilder selClause, StringBuilder qJoinClause) {
        return appendTableColName(selClause, qJoinClause, null, null);
     }
     public boolean appendTableColName(StringBuilder selClause, StringBuilder qJoinClause, StringBuilder groupClause) {
        return appendTableColName(selClause, qJoinClause, groupClause, null);
     }
     public boolean  appendTableColName(StringBuilder selClause, StringBuilder qJoinClause, StringBuilder groupClause, StringBuilder andClause) {
        return appendTableColName(selClause, qJoinClause, groupClause, andClause, doHavingInFilter ? "sum" : null);
     }
     public boolean appendTableColName(StringBuilder selClause, StringBuilder qJoinClause, StringBuilder groupClause, StringBuilder andClause, String aggregateOp) {
    	 return appendTableColName(selClause, qJoinClause, groupClause, andClause, aggregateOp, null, null, true,null,false,true,-1,-1, null, null, null);
     }
     
     public boolean appendTableColName(StringBuilder selClause, StringBuilder qJoinClause, StringBuilder groupClause, StringBuilder andClause, String aggregateOp, String useThisForColumnName, StringBuilder orderByClause, boolean doingMySQL, String orderingInfo,boolean skipGroupBy,boolean addJoinCluase, int ytdScopeAgg, int endScopeForYtd, String dateColForYtd, StringBuilder aliasedOrderByClause, String qualifiedExternalAlias) { //retrns true if added to groupClause
        boolean retval = false;
        StringBuilder tempColName = new StringBuilder();
        
        if (true || useColumnOnlyForName || useThisForColumnName != null) {
        	if("dummy".equalsIgnoreCase(column)){
        		selClause.append("sum(dummy)");
        	}
        	else {
		           String colName = useThisForColumnName != null ? useThisForColumnName : useColumnOnlyForName ? column : table+"."+column;
		           boolean putAggregateOp = !columnHasAggregation && aggregateOp != null && !colName.startsWith(aggregateOp) && aggregateOp.length() != 0;
		           if (putAggregateOp) 
		        	   tempColName.append(aggregateOp).append("(");
		           if (ytdScopeAgg >= 0 && dateColForYtd != null) {
		        	   
		        	   //isInYTDPeriod(dateOfData, ytdScopeAgg, tillScope, endOfPeriod) is in DB
		        	   String startDtCl = (ytdScopeAgg < 100 ? "_ytdlookup_"+ytdScopeAgg : "@period")+".start_time";
		        	   String endDtCl = (endScopeForYtd < 100 ? "_ytdlookup_"+endScopeForYtd : "@period")+".start_time";
		        	   if (ytdScopeAgg == endScopeForYtd)
		        		   endDtCl = (endScopeForYtd < 100 ? "_ytdlookup_"+endScopeForYtd : "@period")+".end_time";
		        	   if (ytdScopeAgg != Misc.SCOPE_TILL_DATE)
		        		   tempColName.append("case when ").append(dateColForYtd).append(">= ").append(startDtCl);
		        	   else
		        		   tempColName.append("case when 1=1 ");
		        	   if (endScopeForYtd >= 0) {
		        		   tempColName.append(" and ").append(dateColForYtd).append(endScopeForYtd == ytdScopeAgg ? " <= " : " < ").append(endDtCl);
		        	   }
		        	   else {
		        		   tempColName.append(" and ").append(dateColForYtd).append(" <= @END_PERIOD");
		        	   }
		        	   tempColName.append(" then ").append(colName).append(" else null end ");
		           }
		           else {
		        	   tempColName.append(colName);
		           }
		           if (putAggregateOp)
		        	   tempColName.append(")");
		           selClause.append(tempColName); 
		           
		           if (aggregateOp == null && groupClause != null && !columnHasAggregation && !skipGroupBy) {
		              if (groupClause.length() != 0 && groupClause.length() != " group by ".length())
		                 groupClause.append(",");
		              groupClause.append(colName);
		              retval = true;
		           }
                   
		           if (orderingInfo != null) {
		        	   //if mysql, if added in group by then append
		        	   if (doingMySQL) {
		        		   if (retval)
		        			   groupClause.append(" ").append(orderingInfo);
		        		   else {
		        			   if (orderByClause.length() != 0)
		        				   orderByClause.append(",");
		        			   orderByClause.append(tempColName).append(" ").append(orderingInfo);
		        		   }//if not grouped
		        	   }//if doing mySQl
		        	   else {
		        		   if (orderByClause.length() != 0)
	        				   orderByClause.append(",");
	        			   orderByClause.append(tempColName).append(" ").append(orderingInfo);
		        	   }
		           }//if there is a orderingInfo
		           if ((doingMySQL && retval) || orderingInfo != null) {//mysql has implicit order by on group and we want to carry upwards
			           if (aliasedOrderByClause != null && qualifiedExternalAlias != null) {
		        		   if (aliasedOrderByClause.length() != 0)
		        			   aliasedOrderByClause.append(",");
		        		   aliasedOrderByClause.append(qualifiedExternalAlias).append(" ").append(orderingInfo == null ? "" : orderingInfo);
		        	   }
		           }
        	}
        }
        else {
        	if("dummy".equalsIgnoreCase(column)){
        		selClause.append("sum(dummy)");
        	}
        	else{
	           boolean putAggregateOp = aggregateOp != null && !column.startsWith(aggregateOp);
	           if (putAggregateOp)
	        	   tempColName.append(aggregateOp).append("(");
	           
	           tempColName.append(table).append(".").append(column);
	           
	           if (putAggregateOp)
	        	   tempColName.append(")");
	           selClause.append(tempColName);
	           
	           if (aggregateOp == null && groupClause != null && !skipGroupBy) {
	              if (groupClause.length() != 0 && groupClause.length() != " group by ".length())
	                 groupClause.append(",");
	              groupClause.append(table).append(".").append(column);
	              retval = true;
	           }
	           if (orderingInfo != null) {
	        	   //if mysql, if added in group by then append
	        	   if (doingMySQL) {
	        		   if (retval)
	        			   groupClause.append(" ").append(orderingInfo);
	        		   else {
	        			   if (orderByClause.length() != 0)
	        				   orderByClause.append(",");
	        			   orderByClause.append(tempColName).append(" ").append(orderingInfo);
	        		   }//if not grouped
	        	   }//if doing mySQl
	        	   else {
	        		   if (orderByClause.length() != 0)
        				   orderByClause.append(",");
        			   orderByClause.append(tempColName).append(" ").append(orderingInfo);
	        	   }
	           }//if there is a orderingInfo
	           if ((doingMySQL && retval) || orderingInfo != null) {//mysql has implicit order by on group and we want to carry upwards
		           if (aliasedOrderByClause != null && qualifiedExternalAlias != null) {
	        		   if (aliasedOrderByClause.length() != 0)
	        			   aliasedOrderByClause.append(",");
	        		   aliasedOrderByClause.append(qualifiedExternalAlias).append(" ").append(orderingInfo == null ? "" : orderingInfo);
	        	   }
	           }
	        }//non dual col name
        }//use table/col
        if (qJoinClause != null && joinClause != null && joinClause.length() > 0 && addJoinCluase)
           qJoinClause.append(" ").append(joinClause).append(" ");
        if (andClause != null && and_clause != null && and_clause.length() != 0) {
           if (andClause.length() != 0)
              andClause.append(" and ");
           andClause.append(and_clause);
        }
        return retval;
     }
     
     public ColumnMappingHelper(String pTable, String pColumn, int pType, String pAnd_clause, String pBase_table, String pJoinClause, boolean pUseColumnOnlyForName, boolean pDoHavingInFilter, String pLinkHint, boolean colNameHasAgg,String pIdField, String pNameField) {
        table = pTable;
        column = pColumn;
        ty = pType;
        and_clause = pAnd_clause;
        base_table = pBase_table;
        joinClause = pJoinClause;
        useColumnOnlyForName = pUseColumnOnlyForName;
        doHavingInFilter = pDoHavingInFilter;
        purJoinHint = pLinkHint;
        columnHasAggregation = colNameHasAgg;
        idField = pIdField;
        nameField = pNameField;
     }
  }

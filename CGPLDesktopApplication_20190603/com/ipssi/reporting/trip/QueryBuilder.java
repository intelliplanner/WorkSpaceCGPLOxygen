package com.ipssi.reporting.trip;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.ipssi.gen.utils.*;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;

import static com.ipssi.gen.utils.Cache.*;

public class QueryBuilder {

	String sel = " select ";
	String from = " from vehicle left join vehicle_access_groups vag on vehicle.id = vag.vehicle_id left join vehicle_type on vehicle.type = vehicle_type.id ";
	String grp = " group by ";
	String whr = " where ";
	String rollup = " with rollup ";

	static public class ParamRequired {
		public Object m_Value=null;
		public int m_type;
	}
	static public class QueryParts {
		public StringBuilder m_selClause = new StringBuilder();
		public StringBuilder m_fromClause = new StringBuilder();
		public StringBuilder m_whereClause = new StringBuilder();
		public StringBuilder m_groupByClause = new StringBuilder();
		public StringBuilder m_havingClause = new StringBuilder();
		public StringBuilder m_rollupClause = new StringBuilder();
	}

	public QueryParts buildQueryParts (FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper){
		QueryParts qp = new QueryParts();
		qp.m_selClause.append(sel);
		qp.m_fromClause.append(from);
		qp.m_groupByClause.append(grp);
		ArrayList<DimConfigInfo> dimConfigList = fpi.m_frontInfoList;
		ArrayList<ArrayList<DimConfigInfo>> searchBox = fpi.m_frontSearchCriteria;
		qp.m_whereClause.append(getWhrQuery(session, searchBox, searchBoxHelper));
		
		HashMap<String, String> tList = new HashMap<String, String>();
		tList.put("vehicle", "vehicle");
		tList.put("vehicle_access_groups", "vehicle_access_groups");
		tList.put("vehicle_type", "vehicle_type");
		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);
			if (!qp.m_selClause.toString().equals(sel))
				qp.m_selClause.append(", ");
			if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) {
				qp.m_selClause.append("null");
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.length() == 0 || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("Dummy")) {
				qp.m_selClause.append("null");
				continue;
			}
			qp.m_selClause.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
			if(dimConfig.m_dimCalc.m_dimInfo.m_type != INTEGER_TYPE && dimConfig.m_dimCalc.m_dimInfo.m_type != NUMBER_TYPE){
				if (!qp.m_groupByClause.toString().equals(grp))
					qp.m_groupByClause.append(", ");
				qp.m_groupByClause.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
			}
			if (!tList.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)) {
				qp.m_fromClause.append(" left join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" on ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(".vehicle_id = vehicle.id ");
				tList.put(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table, dimConfig.m_dimCalc.m_dimInfo.m_colMap.table);
			}
		}
		return qp;
	}
	
	public String buildQuery(QueryParts qp){
		String query = (qp.m_selClause.toString().equals(sel) ? "" : qp.m_selClause.toString()) + qp.m_fromClause.toString() + (qp.m_whereClause.toString().equals("null") ? "" : qp.m_whereClause.toString()) + (qp.m_groupByClause.toString().equals(grp) ? "" : qp.m_groupByClause.toString() + qp.m_rollupClause.toString());
		return query;
	}

	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper){
		StringBuilder sb = new StringBuilder();
		//		printSearchBlock(fpiList, session);
		sb.append("<table class='cn_ipssi'>");
		printTableHeader(sb, fpi);
		QueryParts qp = buildQueryParts(fpi, session, searchBoxHelper);
		String query = buildQuery(qp);
		System.out.println("#############"+query);
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			printTable(rs, sb, fpi);
			rs.close();
			stmt.close();
			sb.append("</table>");
			
		} catch (Exception e){
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void printTableHeader(StringBuilder sb, FrontPageInfo fpi){
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		if(fpList != null)
			cols = fpList.size();
		sb.append("<tr class='tshc'>");
		for(int i=0; i<cols; i++){
			DimConfigInfo dci = fpList.get(i);
			if(!dci.m_hidden){
			sb.append("<td>" + dci.m_name + "</td>");
			}
		}
		sb.append("</tr>");
	}

	public void printTable(ResultSet rs, StringBuilder sb, FrontPageInfo fpi){
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		int type = 0;
		String disp = null;
		String col = null;
		if(fpList != null)
			cols = fpList.size();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		try {
			while(rs.next()){
				sb.append("<tr>");
				for(int i=0; i<cols; i++){
					DimConfigInfo dci = fpList.get(i);
					type = dci.m_dimCalc.m_dimInfo.m_type;
					col = dci.m_dimCalc.m_dimInfo.m_colMap.table + "." + dci.m_dimCalc.m_dimInfo.m_colMap.column;
					if(!dci.m_hidden){
						switch (type){
						case LOV_TYPE : disp = "" + rs.getInt(col);
						break;
						case STRING_TYPE : disp = rs.getString(col);
						break;
						case NUMBER_TYPE : disp = "" + rs.getDouble(col);
						break;
						case LOV_NO_VAL_TYPE : disp = "" + rs.getInt(col);
						break;
						case INTEGER_TYPE : disp = "" + rs.getInt(col);
						break;
						case DATE_TYPE : disp = rs.getDate(col) == null ? "" : sdf.format(rs.getDate(col));
						break;
						default : disp = rs.getString(col);
						}
					sb.append("<td>" + disp + "</td>");
					}
				}
				sb.append("</tr>");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	private String getWhrQuery(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper){
        String whrCl = null;
        StringBuilder whrStr = new StringBuilder(whr);
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		try {
            /*int projectId = (int) _session.getProjectId();
            Cache _cache = _session.getCache();*/
            if (searchBoxHelper == null)
               return null;
//            int maxCol = searchBoxHelper.m_maxCol;
            String topPageContext = searchBoxHelper.m_topPageContext;
        	boolean addWhr = false;
            for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
                ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
                int colSize = rowInfo.size();
                for (int j=0;j<colSize;j++) {
                    DimConfigInfo dimConfig = rowInfo.get(j);
                    if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
                    	continue;
                    boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
                    int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
                    String tempVarName = is123 ? "pv123" : topPageContext+paramId;
                    String tempVal = _session.getAttribute(tempVarName);
    				String end = "";
                    if(tempVal != null && !"".equals(tempVal) && !"-1000".equals(tempVal)){
	    				String encl ="";
	    				int type = dimConfig.m_dimCalc.m_dimInfo.m_type;
	    				String tName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.table;
	    				String cName = dimConfig.m_dimCalc.m_dimInfo.m_colMap.column;
	    				if(!"trick".equalsIgnoreCase(tName)){
	    					if(addWhr)
	    						whrStr.append(" and ");
	    					addWhr = true;
	    					if(type != DATE_TYPE)
	    						whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
	    					switch(type){
	    					case LOV_TYPE : whrStr.append(" in (");
	    					encl = "";
	    					end = ") ";
	    					break;
	    					case STRING_TYPE : whrStr.append(" like '%");
	    					encl = "%'";
	    					end = "";
	    					break;
	    					case NUMBER_TYPE : whrStr.append(" = ");
	    					encl = "";
	    					end = "";
	    					break;
	    					case LOV_NO_VAL_TYPE : whrStr.append(" in (");
	    					encl = "";
	    					end = ") ";
	    					break;
	    					case INTEGER_TYPE : whrStr.append(" = ");
	    					encl = "";
	    					end = " ";
	    					break;
	    					case DATE_TYPE :
	    						whrStr.append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table + "." + dimConfig.m_dimCalc.m_dimInfo.m_colMap.column);
	    						if(dimConfig.m_forDateApplyGreater)
	    							whrStr.append(" >= '");
	    						else
	    							whrStr.append(" <= '");
	    						encl = "'";
	    						end = "";
	    						break;
	    					default : whrStr.append(" like '%");
	    					encl = "%'";
	    					end = ") ";
	    					}
	    				
						if(type != DATE_TYPE)
							whrStr.append(tempVal + encl);
						else{
							try {
								whrStr.append(new java.sql.Date(sdf.parse(tempVal).getTime()) + encl);
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
                    }
                    }
					if(addWhr)
						whrCl = whrStr.append(end).toString();
                }
            }
		}catch (Exception e){
           e.printStackTrace();              
        }
		return whrCl;
    }
}
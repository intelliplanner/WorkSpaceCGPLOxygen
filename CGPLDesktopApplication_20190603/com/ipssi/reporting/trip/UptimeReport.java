package com.ipssi.reporting.trip;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.QueryParts;
import com.ipssi.tracker.colorcode.ColorCodeBean;
import com.ipssi.tracker.colorcode.ColorCodeDao;

public class UptimeReport {
	public static final int g_countDimId = 40000;
	public static final int g_simplifiedDetailedStatusDimId = 400001;
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {		
		ArrayList<DimConfigInfo> fpiList = (ArrayList<DimConfigInfo>) fpi.m_frontInfoList.clone(); //clone because we will be modifying based on data to show
		//look in search and set the data to show detail in the last cell
		int origListSz = fpiList.size();
	    processForDataToShow(fpiList, fpi.m_frontSearchCriteria, session, searchBoxHelper);
	    StringBuilder sb = new StringBuilder();
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpiList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
		//		printSearchBlock(fpiList, session);
		sb.append("<table ID='DATA_TABLE' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
//		ArrayList<Date> calList = new ArrayList<Date>();
//		String gran = null;
		
		QueryParts qp = qb.buildQueryParts(fpiList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session, searchBoxHelper, formatHelper, fpi.m_colIndexLookup, fpi.m_orderIds, null, Misc.getUndefInt(), fpi.m_doRollupAtJava ? 1 : 0, false,0);
		String query = qb.buildQuery(session, qp, null, false);
		System.out.println("#############"+query);
		ArrayList<ArrayList<String>> listOfRows = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> totValues = new ArrayList<ArrayList<String>>(); //index same as listOfRows
		HashMap<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>(); //key = rowIndex in listOfRows+dateString    	
		FastList<String> dateList = new FastList<String>();
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ResultInfo resultInfo = new ResultInfo(fpiList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,qp.m_needsRollup ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null , null, qp.m_isInclInGroupBy, null, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
		    readResultSet(resultInfo, fpiList, searchBoxHelper, session, listOfRows,  totValues,  values, dateList, origListSz);
			rs.close();
			stmt.close();
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		printTableHeader(sb, fpiList, fpi.m_frontSearchCriteria, searchBoxHelper, session, dateList, origListSz);
		printTableBody(sb, formatHelper, fpiList, searchBoxHelper, session, listOfRows,  totValues,  values, dateList , (ColorCodeBean)session.getAttributeObj("colorBean"), origListSz);
		sb.append("</table>");
		return sb.toString();
	}
	
	public DimConfigInfo processForDataToShow(ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
		DimConfigInfo countDimConfig = null;
		for (int i=fpiList.size()-1; i>=0;i--) {
			DimConfigInfo dataToShowInfo = fpiList.get(i);
			if (dataToShowInfo.m_dimCalc != null && dataToShowInfo.m_dimCalc.m_dimInfo != null && dataToShowInfo.m_dimCalc.m_dimInfo.m_subsetOf == UptimeReport.g_countDimId) {
				countDimConfig = dataToShowInfo;
				fpiList.remove(i);
			}
		}
    	return countDimConfig;
    }
    
public void readResultSet(ResultInfo resultInfo, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList, int origFpiListSize) throws Exception {
    	
		int cols = 0;
		String disp = null;
		if(fpList != null)
			cols = fpList.size();
		
		
		int rowIndex = -1; //will be incrmented to 0 when first row is read
		ArrayList<String> currRow = null;
		int numFixedCols = origFpiListSize-2;
		try {
			
		
			boolean newRow = true;
			while(resultInfo.next()){
				//get curr row
				
				//if (!GeneralizedQueryBuilder.g_doRollupAtJava && resultInfo.isCurrEqualToRelativeRow(1)) {//DEBUG13 ... why needed
				//	continue;					
				//}
				
				
				boolean doGroup = resultInfo.isRollupRow();
				int groupedItemIndex = doGroup ? resultInfo.getCurrColBeingRolledUp() : -1;
				DimConfigInfo groupedDCI = groupedItemIndex < 0 ? null : fpList.get(groupedItemIndex);
				if (groupedDCI != null && !groupedDCI.m_doRollupTotal) {
					continue;
				}
				if (doGroup) {
					newRow = true;
				}
				if (!resultInfo.isCurrEqualToRelativeRow(-1, numFixedCols-1)) {
					newRow = true;
				}
				if (newRow && !doGroup) {
					currRow = new ArrayList<String>();					
					listOfRows.add(currRow);
					rowIndex++;
				}
				StringBuilder dateStr = null;
				ArrayList<String> measureValues = new ArrayList<String>();
				for (int t1=0,t1s=cols-numFixedCols-1;t1<t1s;t1++) {
					measureValues.add(null);
				}
				
				for(int i= newRow && !doGroup ? 0 : numFixedCols; i<cols; i++){
					DimConfigInfo dci = fpList.get(i);
					if (dci.m_hidden) {
						currRow.add(null);
						continue;
					}
					
					//all the formatting & scaling being moved to resultInfo.getValStr();
					disp = resultInfo.getValStr(i);
					if (doGroup && i == groupedItemIndex)
					{
						disp = "ALL";
						
					}
					boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);					
					 
					String link = isNull ? null :GeneralizedQueryBuilder.generateLink(dci, fpList, resultInfo,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext, session);
					
					
					StringBuilder sb = new StringBuilder();
					if (doGroup)
					   sb.append("<b>");
					if (link != null) {
						sb.append("<a href=\"").append(link).append("\">");					
					}
					sb.append(disp);
					if (link != null) {
						sb.append("</a>");
					}
					if (doGroup)
						sb.append("</b>");
					if (i < numFixedCols)
						currRow.add(sb.toString());
					else if (i == numFixedCols)
						dateStr = sb;
					else
						measureValues.set(i-numFixedCols-1, sb.toString());
				}//end of for
				if (doGroup) {
					totValues.add(measureValues);
				}
				else {
					Pair<Integer,Boolean> pos = dateList.indexOf(dateStr.toString());
					if (!pos.second) {
						dateList.add(dateStr.toString());
					}
					dateStr.append(".").append(rowIndex);
					values.put(dateStr.toString(), measureValues);   	
				}
				if (newRow && !doGroup) {
					newRow = false;
				}				
			}//end of while
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
    }
	
	
	public void printTableHeader(StringBuilder sb, ArrayList<DimConfigInfo> fpList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper, SessionManager _session, FastList<String> dateList, int origListSz){
		int cols = 0;
  
		if(fpList != null)
			cols = fpList.size();
		boolean doingMultiMeasure = cols != origListSz;
		String rowSpan = doingMultiMeasure ? " rowspan='2' " : "";
		String dtColSpan = doingMultiMeasure ? " colspan='"+(cols-origListSz+1)+"' " : "";
		String css = doingMultiMeasure ? "tshb" : "tshc";
		sb.append("<thead>");
		sb.append("<tr>");
		int numFixedCols = origListSz-2;
		for (int i=0;i<numFixedCols; i++){
			DimConfigInfo dci = fpList.get(i);
			if(!dci.m_hidden){
				sb.append("<td  ").append(rowSpan).append("class='").append(css).append("'>" + dci.m_name + "</td>");
			}
		}
		for (int i=0,is=dateList.size();i<is;i++) {
			sb.append("<td ").append(dtColSpan).append("class='").append(css).append("'>").append(dateList.get(i)).append("</td>");
		}
		if (doingMultiMeasure) {
			sb.append("<tr>");
			for (int i=0,is=dateList.size();i<is;i++) {
				for (int j=origListSz-1;j<cols;j++) {
					DimConfigInfo dci = fpList.get(j);
					sb.append("<td  class='").append(css).append("'>" + dci.m_name + "</td>");
				}
			}
			
			sb.append("</tr>");
		}
		//LATER sb.append("<td class='tshc'>&nbsp;</td>");		
		sb.append("</tr></thead>");
	}
	
	public void printTableBody(StringBuilder sb, ResultInfo.FormatHelper formatHelper, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList , ColorCodeBean clrBean, int origListSz) throws Exception {
		boolean checkAll = true;
		
		
		sb.append("<tbody>");
        int numFixedCols = origListSz-2;
        int measureCols = fpList.size()-origListSz+1;
        
        for (int i=0,is=listOfRows.size();i<is;i++) {
        	sb.append("<tr>");
        	ArrayList<String> row = listOfRows.get(i);
        	
    		for (int j=0;j<numFixedCols; j++) {
    			DimConfigInfo dci = fpList.get(j);
    			if(dci.m_hidden)
    				continue;
    			String disp = row.get(j);
				
				int type = dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.m_type;
				boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;					
				
				String cellClass = doNumber ? "nn" : "cn";
				boolean colorForAll = checkAll;
				
				sb.append("<td class='").append(cellClass).append("'>");
				sb.append(disp);
				sb.append("</td>");
    		}    		
			//print vals for each dateCell
    		for (int j=0,js=dateList.size();j<js;j++) {
    			StringBuilder dateLookup = new StringBuilder();
    			dateLookup.append(dateList.get(j)).append(".").append(i);
    			ArrayList<String> dispValues = values.get(dateLookup.toString());
    			for (int k=0;k<measureCols;k++) {
    				int ks = dispValues == null ? 0 : dispValues.size();
    				DimConfigInfo dataDimConf = fpList.get(numFixedCols+k+1);
    				int typeDataDimConf = dataDimConf == null || dataDimConf.m_dimCalc == null || dataDimConf.m_dimCalc.m_dimInfo== null ? Cache.STRING_TYPE : dataDimConf.m_dimCalc.m_dimInfo.m_type;
    				boolean doNumberDataDimConf = typeDataDimConf == Cache.INTEGER_TYPE || typeDataDimConf == Cache.LOV_NO_VAL_TYPE || typeDataDimConf == Cache.NUMBER_TYPE;
    				String cellClassDataDimConf = doNumberDataDimConf ? "nn" : "cn";
    				String disp = k < ks ? dispValues.get(k) : null;
    				if (disp == null || disp.length() == 0)
        				disp = Misc.nbspString;
        			double v = Misc.getParamAsDouble(disp);
        			String colorClass = cellClassDataDimConf;
        			if (disp != null && !Misc.nbspString.equals(disp)) {
	        			if(clrBean != null && clrBean.isColorCode())
	        			{
	        				if(clrBean.isIncerasing())
	        				{
	        				colorClass = clrBean.getThresholdOne() < v && v < clrBean.getThresholdTwo()? "nnYellow": "nnGreen";
	        				colorClass = v >clrBean.getThresholdTwo()?"nnRed":colorClass;
	        				}else
	        				{
	        					colorClass =v <= clrBean.getThresholdTwo() && v <= clrBean.getThresholdOne()?"nnRed" :"nnYellow" ;
	            				colorClass = v >=clrBean.getThresholdTwo()?"nnGreen":colorClass;
	        				}
	        			}
	        			else {
	    					if (dataDimConf.m_color_code) {
	    						if (dataDimConf.m_param2_color == 0) {
	    							colorClass = v <=dataDimConf.m_param1?"nnGreen": v > dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ?"nnYellow":"nnRed";
	    						}
	    						 else {
	    							colorClass = v >= dataDimConf.m_param2 ?"nnGreen": v >dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ?"nnYellow":"nnRed";
	    						} //if color coding is applicable
	    					}// if has color coding
	        			}// multi measure color coding
        			} //if disp is not null
        			
        			sb.append("<td class='").append(colorClass).append("'>");
    				sb.append(disp);
    				sb.append("</td>");				
    			}
    		}
    		//print tot
    		if (false) {//later
    			ArrayList<String> dispValues = totValues.get(i);
    			for (int k=0,ks = dispValues.size();k<ks;k++) {
    				DimConfigInfo dataDimConf = fpList.get(numFixedCols+k+1);
    				int typeDataDimConf = dataDimConf == null || dataDimConf.m_dimCalc == null || dataDimConf.m_dimCalc.m_dimInfo== null ? Cache.STRING_TYPE : dataDimConf.m_dimCalc.m_dimInfo.m_type;
    				boolean doNumberDataDimConf = typeDataDimConf == Cache.INTEGER_TYPE || typeDataDimConf == Cache.LOV_NO_VAL_TYPE || typeDataDimConf == Cache.NUMBER_TYPE;
    				String cellClassDataDimConf = doNumberDataDimConf ? "nn" : "cn";
    				
    				String disp = dispValues.get(k);
    				if (disp == null || disp.length() == 0)
    					disp = Misc.nbspString;
				
    				sb.append("<td class='").append(cellClassDataDimConf).append("'><b>");
    				sb.append(disp);
    				sb.append("</b></td>");
    			}
    		}
        	sb.append("</tr>");
        }//for each row
        sb.append("</tbody>");
	}

}

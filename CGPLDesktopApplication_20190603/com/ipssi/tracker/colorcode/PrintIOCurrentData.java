package com.ipssi.tracker.colorcode;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.jsp.JspWriter;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.QueryParts;
import com.ipssi.reporting.trip.ResultInfo;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class PrintIOCurrentData {
	
	public static final boolean g_doRollupAtJava = false;
	public ArrayList<Pair<String, Integer>> updateIndex = new ArrayList<Pair<String,Integer>>();
	ArrayList<String> hiddenColumnList = new ArrayList<String>();
	ArrayList<DimConfigInfo> fpiList = new ArrayList<DimConfigInfo>();
	int hiddenIndex =0;
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper , JspWriter out) throws Exception {		
		
		ColorCodeDao.getColorCodeInfo(session.getConnection(),Misc.getParamAsInt(session.getParameter("pv123")),session,fpi.m_frontInfoList,searchBoxHelper);
		 fpiList = (ArrayList<DimConfigInfo>) fpi.m_frontInfoList.clone(); //clone because we will be modifying based on data to show
		//look in search and set the data to show detail in the last cell
		StringBuilder sb = new StringBuilder();
		ArrayList<String> headerName = getHeaderColumnName(session, fpi.m_frontSearchCriteria, searchBoxHelper);
		for(int j = 0 ; j <headerName.size() ; j++ )
		{
			System.out.println(headerName.get(j));
		}
		fpiList = removeUnusedColumn(fpiList ,headerName );
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpiList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
		//		printSearchBlock(fpiList, session);
		sb.append("<table ID='DATA_TABLE' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
		
		QueryParts qp = qb.buildQueryParts(fpiList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session, searchBoxHelper, formatHelper, fpi.m_colIndexLookup, fpi.m_orderIds, null, Misc.getUndefInt(), fpi.m_doRollupAtJava ? 1 : 0, false, fpi.m_orgTimingBased);
		String query = qb.buildQuery(session, qp, null, false);
		query = query + "group by vehicle.id";
		//qb.printTableHeader(sb, fpi, session, formatHelper);
		ArrayList<String> headerNameNew = printTableHeader(sb, fpiList, fpi.m_frontSearchCriteria, searchBoxHelper, session);
	/*	StringBuilder query2 = new StringBuilder();
		query2.append(query);
		query2.indexOf("from");
		ArrayList<String> headerName = printTableHeader(sb, fpiList, fpi.m_frontSearchCriteria, searchBoxHelper, session);
		  DimInfo attributeName = DimInfo.getDimInfo(20158);
		  for(int valcount = 0 ; valcount<attributeName.getValCount();valcount++)
		  {
			 int valId = attributeName.getValInfo(valcount).m_id ;
			 String name  = attributeName.getValInfo(valcount).m_name;
			 if(headerName.contains(name))
			 {
				 System.out.println("name of attribute****  " + name);
				 query2.insert(query2.indexOf("from")-1," , SUM(IF(current_data.attribute_id="+valId+",current_data.attribute_value,0)) as '"+name+"'");
				 
			 }
				
		  }
		  
	
		query2.append(" group by vehicle.id");
		System.out.println("#############"+query2);
		*/
		System.out.println("#############"+query);
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(Integer.MIN_VALUE);
		ResultSet rs = stmt.executeQuery(query);
		ResultSet rsClone = rs;
	/*	StringBuilder bfsub = new StringBuilder();
	
		while(rsClone.next())
		{
			 for(int valcount = 0 ; valcount<attributeName.getValCount();valcount++)
			  {
				 int valId = attributeName.getValInfo(valcount).m_id ;
				 String name  = attributeName.getValInfo(valcount).m_name;
				 if(headerName.contains(name))
				 {
					int aValue =  rs.getInt(name);
					 bfsub.append("<td>"+aValue+"</td>");
				 }
					
			  }
			 hiddenColumnList.add(bfsub.toString());
		}
		*/
		ResultInfo resultInfo = new ResultInfo(fpiList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,qp.m_needsRollup ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null , null, qp.m_isInclInGroupBy, null, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
		printTable(resultInfo, sb, fpiList, searchBoxHelper, session, out);

	
		return sb.toString();
	}
	
	
	public ArrayList<String> printTableHeader(StringBuilder sb, ArrayList<DimConfigInfo> fpList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper, SessionManager _session){
		sb.append("<thead>");
		sb.append("<tr>");
		int numFixedCols = fpList.size();
		for (int i=0;i<numFixedCols; i++){
			DimConfigInfo dci = fpList.get(i);
			if(!dci.m_hidden){
				sb.append("<td  class='tshc'>" + dci.m_name + "</td>");
			}
		}
		ArrayList<String> headerName = getHeaderColumnName(_session, searchBox, searchBoxHelper);
		/*
		for (int i=0,is=headerName.size();i<is;i++) {
			sb.append("<td class='tshc'>").append(headerName.get(i)).append("</td>");
		}
		//LATER sb.append("<td class='tshc'>&nbsp;</td>");	
		 * 	
		 */
		sb.append("</tr></thead>");
		return headerName;
	}
	
	public void printTable(ResultInfo resultInfo, StringBuilder sb, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session, JspWriter out) throws Exception{
	
		//ArrayList<DimConfigInfo> fpList = fpList; //fpi.m_frontInfoList;
		GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
		int cols = 0;
		int type = 0;
		String disp = null;
		String col = null;
		String subType = null;
		
		if(fpList != null)
			cols = fpList.size();
		Cache cache = session.getCache();
				
		try {
			while(resultInfo.next()){
				if (!g_doRollupAtJava && resultInfo.isCurrEqualToRelativeRow(1)) {
					continue;					
				}
				
				boolean checkAll = true;
				boolean doGroup = resultInfo.isRollupRow();
				int groupedItemIndex = doGroup ? resultInfo.getCurrColBeingRolledUp() : -1;
				DimConfigInfo groupedDCI = groupedItemIndex < 0 ? null : fpList.get(groupedItemIndex);
				if (groupedDCI != null && !groupedDCI.m_doRollupTotal) {
					continue;
				}
				if(groupedDCI != null)
				{
					if(groupedDCI.m_doRollupTotal)
					{
					checkAll = false;
					}
				}
				sb.append("<tr>");
				 
				for(int i=0; i<cols; i++){
					DimConfigInfo dci = fpList.get(i);
					if (dci.m_hidden)
						continue;
					//all the formatting & scaling being moved to resultInfo.getValStr();
					//<img src="buzzer.gif"> 
					if(dci.m_use_image)
						disp = "<img src=\""+Misc.G_IMAGES_BASE+dci.m_image_file+"\">";
					else
						disp = resultInfo.getValStr(i);
					
					if (doGroup && i == groupedItemIndex)
		                 disp = "ALL";
						//checkAll = true;
				
					boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);					
					type = dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.m_type;
					boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;					
					 
					String link = isNull ? null : qb.generateLink(dci, fpList, resultInfo,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext, session);
					
					String cellColor = null;
					
					String 	cellClass = doNumber ? "nn" : "cn";
//					if(doNumber && dci.m_color_code){
//						int dispNumber = Misc.getParamAsInt(disp);
//						if(!Misc.isUndef(dci.m_param1) && dispNumber > dci.m_param1){
//							cellClass = "nnYellow";
//						}else if(Misc.isUndef(dci.m_param2) || dispNumber > dci.m_param2){
//							cellClass = "nnGreen";
//						}else if(!Misc.isUndef(dci.m_param2)){
//							cellClass = "nnRed";
//						}
//					}
					
					
					boolean colorForAll = checkAll;
					if(doNumber && dci.m_color_code && disp != null && disp!=Misc.nbspString)
					{ 
						
					
						 if(!colorForAll)
						 {
							 colorForAll = dci.m_param1_color==1?true:colorForAll;
						 }
						 if(dci.m_param1_color==2)
						 {
							 colorForAll = colorForAll?false:true;
						 }	
						if(colorForAll)
						{	
						boolean tc = true;
						
						 if(dci.m_param2_color == 0)
					
						 {
							 cellClass = Misc.getParamAsInt(disp) <=dci.m_param1?"nnGreen":Misc.getParamAsInt(disp) > dci.m_param1 &&  Misc.getParamAsInt(disp) <= dci.m_param2 ?"nnYellow":"nnRed";
						/* if(Misc.getParamAsInt(disp) <= dci.m_param1)
						 {
							 cellClass = "nnGreen";
							 tc = false;
						 }
						 if(tc)
						 {
						cellClass =  Misc.getParamAsInt(disp) > dci.m_param1 &&  Misc.getParamAsInt(disp) <= dci.m_param2 ?"nnYellow":"nnRed";
					
						 }*/
						 }else
						
						 {
							cellClass = Misc.getParamAsInt(disp) >= dci.m_param2 ?"nnGreen":Misc.getParamAsInt(disp) >dci.m_param1 &&  Misc.getParamAsInt(disp) <= dci.m_param2 ?"nnYellow":"nnRed";
						/* if(Misc.getParamAsInt(disp) >= dci.m_param2)
						 {
							 cellClass = "nnGreen";
							 tc = false;
						 }
						 if(tc)
						 {
						cellClass =  Misc.getParamAsInt(disp) >dci.m_param1 &&  Misc.getParamAsInt(disp) <= dci.m_param2 ?"nnYellow":"nnRed";
					
						 }*/
						 }
						}
					}
					
					sb.append("<td ").append("class='").append(cellClass).append("'>");
					
					
					
					if (doGroup)
						sb.append("<b>");
					if(dci.m_show_modal_dialog){
						if (link != null) {
//							sb.append("<a href=\"javascript:popShowModalDialog('").append(link)
//							.append("', '").append(dci.m_dialog_width).append("', '").append(dci.m_dialog_height).append("')\">");
							sb.append("<a href=\"#\"").append(" onClick=\"popShowModalDialog('").append(link)
							.append("', event.srcElement, '").append(dci.m_dialog_width).append("', '").append(dci.m_dialog_height).append("');\">");
						}
						sb.append(disp == null ? "" : disp);
						if (link != null) {
							sb.append("</a>");
						}
					}else{
						if (link != null) {
							sb.append("<a href=\"").append(link).append("\">");					
						}
						sb.append(disp == null ? "" : disp);
						if (link != null) {
							sb.append("</a>");
						}
					}
					if (doGroup)
						sb.append("</b>");
					
				}//end of while
				//sb.append(hiddenColumnList.get(hiddenIndex));
				sb.append("</tr>");
				//hiddenIndex++;
				out.println(sb);
				sb.setLength(0);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}	
	}
	
	
	
	
	/*
	public static boolean doesAllPrevValNonNegative(ArrayList<String> prevSelectedVals){
		boolean retval = true;
		for (int j = 0; j < prevSelectedVals.size(); j++) {
			if(prevSelectedVals.get(j) == null || "".equals(prevSelectedVals.get(j)))
				return false;
		}
		return retval;
	}
	public static StringBuilder updatePrevRows(StringBuilder sb, ArrayList<String> prevSelectedVals, int columnCount){
		String str = sb.toString();
		String temp = null;
		StringBuilder retval = new StringBuilder();
		String[] rowsList = str.split("<tr>");
		String[] colList = null;
		int rowLen, colLen = 0;
		for (int i = 0; i < rowsList.length; i++) {
			retval.append("<tr>");
			rowLen = rowsList[i].length();
			temp = rowsList[i].substring(0, (rowsList[i].length() - "</tr>".length()));
			colList = temp.split("<td>");
			for (int j = 0; j < colList.length; j++) {
				if(j >=columnCount && columnCount < colList.length){
					temp = colList[j].substring(0, (colList[j].length() - "</td>".length()));
				}
				temp = colList[j].substring(0, (colList[j].length() - "</td>".length()));
				colList[j] = prevSelectedVals.get(j-columnCount);
			}
			retval.append("</tr>");
		}
		return retval;
	}
	public void upDatePrevValList(ResultInfo resultInfo, StringBuilder sb, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, ArrayList<String> selectedVal, ArrayList<String> prevSelectedVals) throws Exception {
		
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		String disp = null;
		if(fpList != null)
			cols = fpList.size();
		ArrayList<String> prevPrevSelectedVals = new ArrayList<String>();
		try {
			while(resultInfo.next()){
				boolean checkAll = true;
				boolean doGroup = resultInfo.isRollupRow();
				int groupedItemIndex = doGroup ? resultInfo.getCurrColBeingRolledUp() : -1;
				DimConfigInfo groupedDCI = groupedItemIndex < 0 ? null : fpList.get(groupedItemIndex);
				if (groupedDCI != null && !groupedDCI.m_doRollupTotal) {
					continue;
				}
				if(groupedDCI != null)
				{
					if(groupedDCI.m_doRollupTotal)
					{
					checkAll = false;
					}
				}
//				String batteryLevelDim = null;
				String dimId = null; 
				String dimVal = null; 
				boolean hackHidden = false;
				for(int i=0; i<cols; i++){
					DimConfigInfo dci = fpList.get(i);
					
					int paramid = Misc.getUndefInt();
					if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
		            {
		            	paramid = dci.m_dimCalc.m_dimInfo.m_id;
		            }
					if (dci.m_hidden){
						if(paramid == 20450){
							dimId = resultInfo.getValStr(i);
//							if(batteryLevelDim == null && "2".equals(dimId))
//								batteryLevelDim = dimId; 
							continue;
						}
						if(paramid == 20161){
							dimVal = resultInfo.getValStr(i);
							hackHidden = true;
						}
					}
					if(hackHidden){
						disp = dimVal;
//						for (int j = 0; j < selectedVal.size(); j++) {
//							if(selectedVal.get(j).equalsIgnoreCase(dimId)){
//								if(prevSelectedVals.get(j) == null || "".equals(prevSelectedVals.get(j))){
//									if("2".equals(dimId))
//										prevSelectedVals.set(j, "0".equals(disp) ? "1" : "0"); 
//									else
//										prevSelectedVals.set(j, disp); 
//								}else if(prevPrevSelectedVals.get(j) == null || "".equals(prevPrevSelectedVals.get(j))){
//									prevPrevSelectedVals.set(j, prevSelectedVals.get(j));
//									if("2".equals(dimId))
//										prevSelectedVals.set(j, "0".equals(disp) ? "1" : "0"); 
//									else
//										prevSelectedVals.set(j, disp); 
//								}
//							}
//						}
						for (int j = 0; j < selectedVal.size(); j++) {
							if(selectedVal.get(j).equalsIgnoreCase(dimId) && (prevSelectedVals.get(j) == null || "".equals(prevSelectedVals.get(j)))){
								prevSelectedVals.set(j, disp); 
							}
						}
						if(doesAllPrevValNonNegative(prevSelectedVals)){
//							for (int j = 0; j < prevSelectedVals.size(); j++) {
//								prevSelectedVals.set(j, "0".equals(prevSelectedVals.get(j)) ? "1" : "0");
//							}
							return;
						}
						continue;
					}
				}//end of while
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}	
	}
	
	
	
	
	
	
	
	
	
	*/
	
	//---------------------------------------------------------------------------------------------------
	
	
	
	
	//---------------------------------------------------------------------------------------------------
	
	public void printTableBody(ResultInfo resultInfo, StringBuilder sb, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, ArrayList<String> selectedVal, ArrayList<String> prevSelectedVals, String startDate) throws Exception {
	
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		int type = 0;
		String disp = null;
//		String oldGpsRecordTime = null;
	    java.util.Date prevGpsRecordTime = null;
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    java.util.Date startDateTS = sdf.parse(startDate);
//		String col = null;
//		String subType = null;
		
		if(fpList != null)
			cols = fpList.size();
//		Cache cache = session.getCache();
//		ArrayList<String> prevSelectedVals = new ArrayList<String>();
//		for (int j = 0; j < selectedVal.size(); j++) {
//			prevSelectedVals.add("");
//		}
//		upDatePrevValList(resultInfo, sb, fpi, searchBoxHelper, session, selectedVal,  prevSelectedVals);
//		resultInfo.m_rs.first();
		StringBuilder currTR = new StringBuilder();
		StringBuilder prevTR = new StringBuilder();
		try {
			while(resultInfo.next()){
//				boolean ckDuplicate = false;
				currTR = new StringBuilder();
				boolean checkAll = true;
				boolean doGroup = resultInfo.isRollupRow();
				int groupedItemIndex = doGroup ? resultInfo.getCurrColBeingRolledUp() : -1;
				DimConfigInfo groupedDCI = groupedItemIndex < 0 ? null : fpList.get(groupedItemIndex);
				if (groupedDCI != null && !groupedDCI.m_doRollupTotal) {
					continue;
				}
				if(groupedDCI != null)
				{
					if(groupedDCI.m_doRollupTotal)
					{
					checkAll = false;
					}
				}
				currTR.append("<tr>");
				String dimId = null; 
				String dimVal = null; 
				boolean hackHidden = false;
				boolean doesPrevValUpdated = false;
				int columnCount = -1;
				for(int i=0; i<cols; i++){
					DimConfigInfo dci = fpList.get(i);
					
					int paramid = Misc.getUndefInt();
					if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
		            {
		            	paramid = dci.m_dimCalc.m_dimInfo.m_id;
		            }
					if (dci.m_hidden){
						if(paramid == 20450){
							dimId = resultInfo.getValStr(i);
							continue;
						}
						if(paramid == 20161){
							dimVal = resultInfo.getValStr(i);
							hackHidden = true;
						}
					}
					boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);	
					
					type = dci.m_dimCalc.m_dimInfo.m_type;
					boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;	
//					String cellColor = null;
					
					String 	cellClass = doNumber ? "nn" : "cn";
					boolean colorForAll = checkAll;
					if(doNumber && dci.m_color_code && disp != null && disp!=Misc.nbspString)
					{ 
						 if(!colorForAll)
						 {
							 colorForAll = dci.m_param1_color==1?true:colorForAll;
						 }
						 if(dci.m_param1_color==2)
						 {
							 colorForAll = colorForAll?false:true;
						 }	
						if(colorForAll)
						{	
//						boolean tc = true;
						 if(dci.m_param2_color == 0)
					
						 {
							 cellClass = Misc.getParamAsInt(disp) <=dci.m_param1?"nnGreen":Misc.getParamAsInt(disp) > dci.m_param1 &&  Misc.getParamAsInt(disp) <= dci.m_param2 ?"nnYellow":"nnRed";
						 }else
						
						 {
							cellClass = Misc.getParamAsInt(disp) >= dci.m_param2 ?"nnGreen":Misc.getParamAsInt(disp) >dci.m_param1 &&  Misc.getParamAsInt(disp) <= dci.m_param2 ?"nnYellow":"nnRed";
						 }
						}
					}	
					//all the formatting & scaling being moved to resultInfo.getValStr();
					columnCount++;
					if(hackHidden){
						disp = dimVal;
						for (int j = 0; j < selectedVal.size(); j++) {
							if(selectedVal.get(j).equalsIgnoreCase(dimId)){
								currTR.append("<td ").append("class='").append(cellClass).append("'>");
								if (doGroup)
									currTR.append("<b>");
								currTR.append(disp == null ? "" : disp);
								if (doGroup)
									currTR.append("</b>");
								currTR.append("</td>");	
								prevSelectedVals.set(j, disp); 
//								if(prevSelectedVals.size() > (j))
//									prevSelectedVals.set(j, disp); 
//								else
//									prevSelectedVals.add(j, disp);
							}else{

								currTR.append("<td ").append("class='").append(cellClass).append("'>");
								if (doGroup)
									currTR.append("<b>");
//								currTR.append(disp == null ? "" : "");
								currTR.append(prevSelectedVals.size() <= j || prevSelectedVals.get(j) == null ? "" : prevSelectedVals.get(j));
								if (doGroup)
									currTR.append("</b>");
								currTR.append("</td>");	
							}
						}
//						if(!doesPrevValUpdated && doesAllPrevValNonNegative(prevSelectedVals)){
//							currTR = updatePrevRows(currTR, prevSelectedVals, columnCount);
//							doesPrevValUpdated = true;
//							
//						}
//						continue;
					}else{
						if(dci.m_use_image)
							disp = "<img src=\""+Misc.G_IMAGES_BASE+dci.m_image_file+"\">";
						else
							disp = resultInfo.getValStr(i);
					}
					if(!hackHidden){
					if (doGroup && i == groupedItemIndex)
		                 disp = "ALL";
						//checkAll = true;
				
									
					 
					String link = isNull ? null : GeneralizedQueryBuilder.generateLink(dci, fpList, resultInfo,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext, session);
					
					
					currTR.append("<td ").append("class='").append(cellClass).append("'>");
					if (doGroup)
						currTR.append("<b>");
					if(dci.m_show_modal_dialog){
						if (link != null) {
							currTR.append("<a href=\"#\"").append(" onClick=\"popShowModalDialog('").append(link)
							.append("', event.srcElement, '").append(dci.m_dialog_width).append("', '").append(dci.m_dialog_height).append("');\">");
						}
						currTR.append(disp == null ? "" : disp);
						if (link != null) {
							currTR.append("</a>");
						}
					}else{
						if (link != null) {
							currTR.append("<a href=\"").append(link).append("\">");					
						}
						currTR.append(disp == null ? "" : disp);
						if (link != null) {
							currTR.append("</a>");
						}
					}
					
					if (doGroup)
						currTR.append("</b>");
					currTR.append("</td>");	
					}
					if(paramid == 20134){
						if (resultInfo.getVal(i).getDateValLong() <= startDateTS.getTime()){
							continue;
						}
						else if((resultInfo.getVal(i).getDateVal()).equals(prevGpsRecordTime))
						{
//							prevTR = new StringBuilder(currTR);
							continue;
//							ckDuplicate = true;
//							continue;
						}else
						{
							sb.append(prevTR);
							prevTR = null;
							prevGpsRecordTime = resultInfo.getVal(i).getDateVal();
//							oldGpsRecordTime = disp;
						}
						
					}
				}//end of while
				
				currTR.append("</tr>");
				prevTR = new StringBuilder(currTR);
//				if(ckDuplicate)
//				{
//					
//					currTR.length();
//					//currTR.reverse();
//					currTR.lastIndexOf("<tr>");
//					currTR.delete(sb.lastIndexOf("<tr>"), sb.length());
//				}
//				out.println(sb);
//				sb.setLength(0);
			}
			sb.append(currTR);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}	
	}
	
	
	
	   public boolean hasNestedColHeader(ArrayList<DimConfigInfo> fpList) {
	    	for (int i=0,is=fpList == null ? 0 : fpList.size();i<is;i++) {
	    		DimConfigInfo dci = fpList.get(i);
	    		if (dci != null && dci.m_dataSpan > 1)
	    			return true;
	    	}
	    	return false;
	    }
	   
	   
	private ArrayList<String> getHeaderColumnName(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper){
		ArrayList<String> retval = new ArrayList<String>();
		try {
			if (searchBoxHelper == null)
				return null;
			String topPageContext = searchBoxHelper.m_topPageContext;
			boolean exit = false;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				if(exit)
					break;
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
					if(paramId == 20159){
					String tempVarName = topPageContext+paramId;
					String tempVal = _session.getAttribute(tempVarName);
				//	tempVal = tempVal.substring(tempVal.indexOf("0,"));
					String[] strValueArray =  tempVal.split(",");
					DimInfo dataTypeDim = DimInfo.getDimInfo(20159);
					ArrayList<Integer> selectedVal = new ArrayList<Integer>();
 				//	Misc.convertValToVector(tempVal, selectedVal);
 					for (int k = 0; k < strValueArray.length; k++) {
 						retval.add(dataTypeDim.getValInfo(Integer.parseInt(strValueArray[k])).m_name);
					}
					exit = true;
					break;
					}
					
				}
			}
		}catch (Exception e){
			e.printStackTrace();              
		}
		return retval;
	}
	public 	ArrayList<DimConfigInfo> removeUnusedColumn(ArrayList<DimConfigInfo> fpList , ArrayList<String> searchHeadders)
	{
		 DimInfo attributeName = DimInfo.getDimInfo(20158);
		 ArrayList<String> allAttributeNameList = new ArrayList<String>();
		  for(int valcount = 0 ; valcount<attributeName.getValCount();valcount++)
		  {
			 int valId = attributeName.getValInfo(valcount).m_id ;
			 String name  = attributeName.getValInfo(valcount).m_name;
			 
				 System.out.println("name of attribute****  " + name);
				 allAttributeNameList.add(name);
				 	
		  }
		  try{
			  ArrayList<DimConfigInfo> columnToremove = new ArrayList<DimConfigInfo>();
			  System.out.println("name of searchcolum****  " + searchHeadders.get(0) + "name of searchcolum****  " );
		  int numFixedCols = fpList. size();
			for (int i=0;i<numFixedCols; i++){
				DimConfigInfo dci = fpList.get(i);
				 System.out.println("name of dci attribute****  " + dci.m_name);
				if(!dci.m_hidden && allAttributeNameList.contains(dci.m_name)  ){
					//sb.append("<td  class='tshc'>" + dci.m_name + "</td>");
					if(!searchHeadders.contains(dci.m_name))
					{
						 System.out.println("name of ****Remove*** attribute****  " + dci.m_name);
						columnToremove.add(fpList.get(i));
						
					}
					
				}
			}
			
			for(int j = 0 ; j < columnToremove.size() ; j++ )
			{
				if(fpList.contains(columnToremove.get(j)))
				{
				System.out.println((columnToremove.get(j).m_name));
				fpList.remove(columnToremove.get(j));
				}
			}
		  }catch (Exception e) {
			// TODO: handle exception
			  return fpList;
		}
			return fpList;
			
	}
}

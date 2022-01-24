package com.ipssi.reporting.trip;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimConfigInfo.LinkHelper;
import com.ipssi.gen.utils.DimConfigInfo.ExprHelper.CalcFunctionEnum;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Value;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.QueryParts;
import com.ipssi.tracker.colorcode.ColorCodeBean;
import com.ipssi.tracker.colorcode.ColorCodeDao;
import com.sun.xml.internal.ws.util.StringUtils;

public class PrintSummaryTable {

	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception{
		return printPage(conn, fpi, session, searchBoxHelper, null, Misc.HTML, "",Misc.getUndefInt());
	}
	
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper,ByteArrayOutputStream stream,int reportType,String reportName,int reportId) throws Exception {		
		Table table = Table.createTable();
		StringBuilder rowLinkParam = new StringBuilder();
		//1st and last two column need to be hidden ... so that we dont screw up customization 
		ArrayList<DimConfigInfo> fpiList = (ArrayList<DimConfigInfo>) fpi.m_frontInfoList.clone(); //clone because we will be modifying based on data to show
		int origListSz = fpiList.size();
		HashMap<String, String> paramList = getSearchParamsForLink(session, fpi.m_frontSearchCriteria, searchBoxHelper);
		ArrayList<DimInfo> iterDims = new ArrayList<DimInfo>();
		ArrayList<ArrayList<Value>> iterValues = new ArrayList<ArrayList<Value>>();
		ArrayList<DimInfo> measureDims = new ArrayList<DimInfo>();
		processForDataToShow(fpiList, fpi.m_frontSearchCriteria, session, searchBoxHelper, rowLinkParam, iterDims, iterValues, measureDims);
		ColorCodeDao.getColorCodeInfo(session.getConnection(),Misc.getParamAsInt(session.getParameter("pv123")),session,fpiList,searchBoxHelper);
		StringBuilder sb = new StringBuilder();
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpiList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
		sb.append("<table ID='DATA_TABLE' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
		QueryParts qp = qb.buildQueryParts(fpiList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session, searchBoxHelper, formatHelper, fpi.m_colIndexLookup, fpi.m_orderIds, null, Misc.getUndefInt(), fpi.m_doRollupAtJava ? 1 : 0, false, fpi.m_orgTimingBased);
		String query = qb.buildQuery(session, qp, null, false);
		System.out.println("#############"+query);
		ArrayList<ArrayList<Value>> listOfRows = new ArrayList<ArrayList<Value>>();//second will have number of cols 
		HashMap<String, ArrayList<Value>> values = new HashMap<String, ArrayList<Value>>(); //key = rowIndex in listOfRows+"_"+firstIter+"_"+secondIter
		ArrayList<ArrayList<Value>>  itersFound = new ArrayList<ArrayList<Value>> ();
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ResultInfo resultInfo = new ResultInfo(fpiList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,qp.m_needsRollup ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null , null, qp.m_isInclInGroupBy, null, qp.groupByRollupAggIndicator, qp.m_doRollupAtJava);
			readResultSet(listOfRows, iterValues, values, resultInfo, fpiList, iterDims.size(), measureDims.size());
			rs.close();
			stmt.close();
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		printTableHeader(fpiList, fpi.m_objectIdParamLabel, session, table, iterDims, measureDims, iterValues);

		//printTableHeader(fpiList, fpi.m_frontSearchCriteria, searchBoxHelper, session, iterDims, iterValues, origListSz,table);
		printTableBody(formatHelper, fpiList, searchBoxHelper, session, listOfRows,  values, iterValues , iterDims, measureDims,  (ColorCodeBean)session.getAttributeObj("colorBean"), origListSz,table);
		if	(reportType == Misc.HTML){
			HtmlGenerator.printHtmlTable(table, sb, session);
		}
		else if	(stream != null && reportType == Misc.PDF)
		{
			PdfGenerator pdfGen = new PdfGenerator();
			pdfGen.printPdf(stream, reportName, table, session, reportId);	
		}
		else if	(stream != null && reportType == Misc.EXCEL)
		{
			ExcelGenerator excelGen = new ExcelGenerator();
			excelGen.printExcel(stream, reportName, table,session,reportId);
		}
		return sb.toString();
	}
	public void processForDataToShow(ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper,StringBuilder rowLinkParam, ArrayList<DimInfo> iterDims, ArrayList<ArrayList<Value>> iterValues, ArrayList<DimInfo> measureDims) throws Exception {
		DimConfigInfo dataToShowInfo = fpiList.get(fpiList.size()-1);
		DimConfigInfo iterDimConfigInfo = fpiList.get(fpiList.size()-2);
		DimConfigInfo zeroColumnInfo = fpiList.get(0);
		DimConfigInfo firstColumnInfo = fpiList.get(1);
		
		dataToShowInfo.m_hidden = false;
		iterDimConfigInfo.m_hidden = false;
		firstColumnInfo.m_hidden=false;
		zeroColumnInfo.m_hidden = true;
		if (searchBoxHelper != null) {
			//1. Setup dimensions for iteration
			MiscInner.PairIntStr v20059 = searchBoxHelper.getSearchValue(session, 20059, true); 
			ArrayList<Integer> tempIntList = new ArrayList<Integer>();
			Misc.convertValToVector(v20059 == null ? null : v20059.second, tempIntList);
			DimInfo iterControlDim = v20059 == null ? null : DimInfo.getDimInfo(v20059.first);
			//rearrange it so that we are in same order as spec in lov
			ArrayList<Integer> arrangedIterDims  = new ArrayList<Integer>();
			if (iterControlDim != null) {
				ArrayList<DimInfo.ValInfo> iterControlList = iterControlDim.getValList();
				for (DimInfo.ValInfo vinfo : iterControlList) {
					if (tempIntList.contains(vinfo.m_id))
						arrangedIterDims.add(vinfo.m_id);
				}
			}
			
			
			for (int art=0,arts = arrangedIterDims.size();art<arts;art++) {
				DimInfo iterDim = DimInfo.getDimInfo(arrangedIterDims.get(art));
				if (iterDim == null)
					continue;
				iterDims.add(iterDim);
				ArrayList<Value> valList = new ArrayList<Value>();
				iterValues.add(valList);
				
				String vals = searchBoxHelper.getSearchValue(session, iterDim.m_id).second;
				if (vals != null && vals.length() != 0) {
					ArrayList<Integer> valIdList = new ArrayList<Integer>();
					Misc.convertValToVector(vals, valIdList);
					if (valIdList != null && valIdList.size() > 0) {
						for (int j=0,js=valIdList.size();j<js;j++) {
							valList.add(new Value(valIdList.get(j)));
						}
					}
				}
				if (valList.size() == 0) {
					ArrayList<DimInfo.ValInfo> allVals = (ArrayList<DimInfo.ValInfo>) iterDim.getValList(session.getConnection(), session);
					for (int j=0, js= allVals == null ? 0 : allVals.size();j<js;j++) {
						valList.add(new Value(allVals.get(j).m_id));
					}
				}
				if (valList.size() != 0 && valList.get(0).m_iVal == Misc.G_HACKANYVAL) {
					iterValues.remove(iterValues.size()-1);
					iterDims.remove(iterDims.size()-1);
				}
			}
			if (iterDims.size() == 0)
				fpiList.remove(fpiList.size()-2);
			else {
				for (int j=0, js = iterDims.size();j<js;j++) {
					DimConfigInfo toAdd = j == 0 ? iterDimConfigInfo : (DimConfigInfo) iterDimConfigInfo.clone();
					DimInfo iterDim = iterDims.get(j);
					toAdd.m_dimCalc.m_dimInfo = iterDims.get(j);
					toAdd.m_columnName = "d"+iterDim.m_id;
					toAdd.m_internalName = "d"+iterDim.m_id;
					toAdd.m_name = iterDim.m_name;
					rowLinkParam.append("home"+iterDim.m_id);
					if (j != 0)
						fpiList.add(fpiList.size()-1, toAdd);
				}
			}
			//2 Set up which measure to show and first col value
			boolean done1 = false;
			boolean done2 = false;
			String topPageContext = searchBoxHelper.m_topPageContext;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
					if(dimInfo.m_subsetOf == 24103){
						int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
						String tempVarName =  topPageContext+paramId;
						String tempVal = session.getParameter(tempVarName);
						ValInfo vinfo = dimInfo.getValInfo(Misc.getParamAsInt(tempVal));
						done1 = true;
						if (vinfo != null) {
							firstColumnInfo.m_dimCalc.m_dimInfo = DimInfo.getDimInfo(vinfo.m_id);
							firstColumnInfo.m_columnName = "d"+vinfo.m_id;
							firstColumnInfo.m_internalName = "d"+vinfo.m_id;
							firstColumnInfo.m_name = vinfo.m_name;
							
							int refId = Misc.getParamAsInt(vinfo.getOtherProperty("ref_id"));
							String refName = vinfo.getOtherProperty("ref_name");
							zeroColumnInfo.m_hidden = true;
							zeroColumnInfo.m_dimCalc.m_dimInfo = DimInfo.getDimInfo(refId);
							zeroColumnInfo.m_columnName = refName == null ? "d"+refId : refName;
							zeroColumnInfo.m_internalName = "d"+refId;
							zeroColumnInfo.m_name = vinfo.m_name;
							zeroColumnInfo.m_hidden = true;
							//rowLinkParam.append("home"+vinfo.m_id);
						}
					}
					if (dimInfo.m_subsetOf == 20052) {
						done2 = true;
						int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
						String tempVarName =  topPageContext+paramId;
						String tempVal = session.getParameter(tempVarName);
						ArrayList<Integer> tempValInt = new ArrayList<Integer>();
						if (tempVal != null && tempVal.length() != 0) {
							Misc.convertValToVector(tempVal, tempValInt);
						}
						boolean usedDataToShowInfoOnce = false;
						for (int k=0,ks=tempValInt.size(); k<ks;k++) {
							int toshow = tempValInt.get(k);
							ValInfo vinfo = dimInfo.getValInfo(toshow);
							if (vinfo != null) {
								if (usedDataToShowInfoOnce) {
									dataToShowInfo = (DimConfigInfo) dataToShowInfo.clone();
									fpiList.add(dataToShowInfo);
								}
								else {
									usedDataToShowInfoOnce = true;
								}
								String useAgg = vinfo.getOtherProperty("use_aggregate");
								if (useAgg == null || useAgg.length() == 0) {
									dataToShowInfo.m_aggregate = false;
								}
								else {
									dataToShowInfo.m_aggregate = true;
									dataToShowInfo.m_default = useAgg;
								}
								dataToShowInfo.m_dimCalc.m_dimInfo = DimInfo.getDimInfo(vinfo.m_id);
								dataToShowInfo.m_columnName = "d"+vinfo.m_id;
								dataToShowInfo.m_internalName = "d"+vinfo.m_id;
								dataToShowInfo.m_name = vinfo.m_name;
								measureDims.add(dataToShowInfo.m_dimCalc.m_dimInfo);
							}
						}
					}
					if (done1 && done2)
						break;
				}//for each col
				if (done1 && done2)
					break;
				/*if (done)
					break;*/
			}
		}
		int desiredSizeIter= measureDims.size() > 1 ? 1 : 2;
		for (int j=iterDims.size()-1;j >=desiredSizeIter;j--) {
			iterDims.remove(j);
			iterValues.remove(j);
		}
	}

	public static String getStringOf(ArrayList<Value> list) {
		StringBuilder sb = new StringBuilder();
		for (int i=0,is=list.size(); i<is;i++) {
			if (i != 0)
				sb.append("_");
			sb.append(list.get(i));
		}
		return sb.toString();
	}
	public static String getStringOf(String prevKey, ArrayList<Value> list) {
		StringBuilder sb = new StringBuilder();
		if (prevKey != null)
			sb.append(prevKey);
		for (int i=0,is=list.size(); i<is;i++) {
			if (i != 0 || (prevKey != null && prevKey.length() > 0))
				sb.append("_");
			sb.append(list.get(i));
		}
		return sb.toString();
	}
	//HashMap<String, Integer> colIndexLookup, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList, int origFpiListSize,HashMap<String,String> paramMap, StringBuilder rowLinkParam
	public void readResultSet(ArrayList<ArrayList<Value>> listOfRows, ArrayList<ArrayList<Value>> itersFound, HashMap<String, ArrayList<Value>> values, ResultInfo resultInfo, ArrayList<DimConfigInfo> fpList, int numIters, int measureCount) throws Exception {
		int cols = 0;
		String disp = null;
		if(fpList != null)
			cols = fpList.size();
		int rowIndex = -1; 
		for (int i=itersFound.size();i<numIters;i++)
			itersFound.add(new ArrayList<Value>());
		int numFixedCols = cols-measureCount-numIters;
		ArrayList<Value> currRow = null;
		ArrayList<Value> iterValues = numIters == 0 ? null : new ArrayList<Value>();
		for (int t1=0; t1<numIters;t1++) {
			iterValues.add(null);
		}
		String currRowKey = null;
		try {
			boolean newRow = true;
			while(resultInfo.next()){
				//if (!GeneralizedQueryBuilder.g_doRollupAtJava && resultInfo.isCurrEqualToRelativeRow(1)) {
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
					currRow = new ArrayList<Value>();					
					listOfRows.add(currRow);
					currRowKey = null;
					rowIndex++;
				}
				StringBuilder dateStr = null;
				ArrayList<Value> measureValues = new ArrayList<Value>();
				for (int t1=0,t1s=measureCount;t1<t1s;t1++) {
					measureValues.add(null);
				}
				for (int t1=0; t1<numIters;t1++) {
					iterValues.set(t1,null);
				}
					
				for(int i= newRow && !doGroup ? 0 : numFixedCols; i<cols; i++){
					DimConfigInfo dci = fpList.get(i);
					//if (dci.m_hidden) { //dont otherwise we wont get the value in lookup
					//	currRow.add(null);
					//	continue;
					//}
					Value currVal = resultInfo.getVal(i);
					if (currVal  != null)         
						currVal = new Value(currVal); //resutInfo reuses the space allocated for Vaue
					if (doGroup && i == groupedItemIndex)
					{
						if (currVal == null)
							currVal = new Value();
						currVal.setGroup();
					}
					if (i < numFixedCols)
						currRow.add(currVal);
					else if (i < numFixedCols+numIters) {
						iterValues.set(i-numFixedCols, currVal);
					}
					else {
						measureValues.set(i-numFixedCols-numIters, currVal);
					}
				}//end of for
				for (int t1=0;t1<numIters;t1++) {
					Value v = iterValues.get(t1);
					if (v != null) {
						ArrayList<Value> list = itersFound.get(t1);
						boolean found =  false;
						for (int t2=0,t2s = list.size(); t2<t2s;t2++) {
							if (list.get(t2).equals(v)) {
								found = true;
								break;
							}
						}
						if (!found) {
							list.add(v);
						}
					}
				}
				if (currRowKey == null)
					currRowKey = getStringOf(currRow);
				String key = getStringOf(currRowKey, iterValues);
				values.put(key, measureValues);
				if (newRow && !doGroup) {
					newRow = false;
				}				
			}//end of while
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	public void readResultSetOrig(ResultInfo resultInfo,HashMap<String, Integer> colIndexLookup, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList, int origFpiListSize,HashMap<String,String> paramMap, StringBuilder rowLinkParam) throws Exception {
		int cols = 0;
		String disp = null;
		if(fpList != null)
			cols = fpList.size();
		int rowIndex = -1; 
		ArrayList<String> currRow = null;
		int numFixedCols = origFpiListSize-2;
		try {
			boolean newRow = true;
			while(resultInfo.next()){
				//if (!GeneralizedQueryBuilder.g_doRollupAtJava && resultInfo.isCurrEqualToRelativeRow(1)) {
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
					disp = resultInfo.getValStr(i);
					if (doGroup && i == groupedItemIndex)
					{
						disp = "ALL";

					}
					boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);					
					//String link = isNull ? null : generateLink(dci, colIndexLookup, fpList, resultInfo,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext, session,origFpiListSize, paramMap);
					String link = isNull ? null : GeneralizedQueryBuilder.generateLink(dci, fpList, resultInfo,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext, session);
					StringBuilder sb = new StringBuilder();
					if (doGroup)
						sb.append("<b>");
					if (link != null) {
						if(dci.m_show_modal_dialog){
							link = link.replaceAll("rowLinkParam", rowLinkParam.toString());
							link = getUrl(link,paramMap);
							sb.append(" <a href=\"#\" onClick=\"popShowModalDialog('"+link);
							sb.append(" ', event.srcElement, '"+dci.m_dialog_width+"', '"+dci.m_dialog_height+" ' )\"> ");
						}
						else
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
	private String generateLink(DimConfigInfo dimConfig, HashMap<String, Integer> colIndexLookup, ArrayList<DimConfigInfo> fpList, ResultInfo resultInfo, String topPageContext, SessionManager session,int origListSz,HashMap paramMap) {
		int numFixedCols = origListSz-2;
		StringBuilder retval = new StringBuilder();
		if (dimConfig == null || dimConfig.m_linkHelper == null)
			return null;
		DimConfigInfo.LinkHelper linkHelper = dimConfig.m_linkHelper;
		retval.append(linkHelper.m_pagePart);
		if (linkHelper.m_fixedParamPart != null || linkHelper.m_paramName.size() != 0)
			retval.append("?");
		if (linkHelper == null)
			return null;
		boolean firstParamAdded = false;
		if (linkHelper.m_fixedParamPart != null) {
			retval.append(linkHelper.m_fixedParamPart);
			firstParamAdded = true;
		}
		int indexInFrontInfoList = -1;
		for(int i=0; i<numFixedCols; i++){
			DimConfigInfo dimInfo = fpList.get(i);
			if(dimInfo == null || dimInfo.m_dimCalc == null || dimInfo.m_dimCalc.m_dimInfo == null)
				continue;
			String paramId =  topPageContext + dimInfo.m_dimCalc.m_dimInfo.m_id;
			String paramName = dimInfo.m_dimCalc.m_dimInfo.m_name;
			Integer indexInteger = colIndexLookup.get(paramName);
			if(indexInteger != null){
				indexInFrontInfoList = indexInteger.intValue();
			}
			String val = null;
			if(indexInFrontInfoList != -1) {
				Value rval = resultInfo == null ? null : resultInfo.getVal(indexInFrontInfoList);
				if (rval != null && rval.isNotNull())
					val = rval.toString();
			}
			if(val != null){
				if (firstParamAdded)
					retval.append("&");
				//if (paramName.second)
				//	retval.append(topPageContext);
				retval.append(paramId);
				retval.append("=");
				retval.append(val);
				firstParamAdded = true;
			}
		}
		return retval.toString();
	}

	public void printTableHeader(ArrayList<DimConfigInfo>fpList, String objectIdParamLabel, SessionManager session, Table table, ArrayList<DimInfo> iterDims, ArrayList<DimInfo> measureDims, ArrayList<ArrayList<Value>> iterValues) throws Exception{
		/*
		 *id-class 
		 *0 -tshb
		 *1 -tshc
		 *2 -cn
		 *3 -nn
		 *4 -nnGreen
		 *5 -nnYellow
		 *6 -nnRedf
		 */
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		TR tr = null; 
		TD td = null;
		String displayLink = null;
		int cols = 0;
		int index = 0;
		if(fpList != null)
			cols = fpList.size();
		tr = new TR();
		Cache cache = session.getCache();
		//requires nesting if measureDims > 1 or iterDims > 1
		boolean hasMultiple = measureDims.size() > 1 || iterDims.size() > 1; 
		int headerClassId = hasMultiple ? 0 : 1;
		tr.setClassId(headerClassId);
		String selectCheckBoxVarName=objectIdParamLabel;
		//doing top level
		tr.setId("scrollmenu");
		StringBuilder tempSB = new StringBuilder();
		int numFixedCols = cols - measureDims.size() - iterDims.size();
		//print fixed cols
		for(int i=0, numsToSkip=1; i<numFixedCols; i++){
			td = new TD();
			numsToSkip = 1; //if dimconfig dataspan > 1 then this becomes that value
			DimConfigInfo dci = fpList.get(i);
			if (dci == null )
				continue;
			if(dci.m_hidden)
				td.setHidden(true);
			DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
			int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
            boolean doDate = attribType == cache.DATE_TYPE;
            boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
			td.setContentType(doDate ? attribType : doInterval ? Misc.getParamAsInt(dimInfo.m_subtype,2) : attribType); 
			td.setColSpan(1);
			td.setRowSpan(hasMultiple ? 2:1);
			if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
				tempSB.setLength(0);
				FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
				if (tempSB.length() == 0) {
					//no priv
					td.setHidden(true);
				}
			}
			boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0);
			td.setDoIgnore(ignore);
			boolean doSortLink = dci.m_dataSpan <= 1;
			String name = dci.m_dataSpan > 1 ? dci.m_frontPageColSpanLabel : dci.m_name;
			if (doSortLink){
				displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>";
				displayLink	+= name != null && name.length() != 0 ? name : "&nbsp;";
			}
			else {
				displayLink = null;
			}

			td.setContent(name);
			if(dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
			{
				int paramid = dci.m_dimCalc.m_dimInfo.m_id;
				td.setId(paramid);
//				if(paramid == 20206 || paramid == 20205 || paramid == 20204 || paramid == 20203 || paramid == 20202)
//					updateIndex.add(new Pair<String, Integer>(name, index));
			}
			index++;
			if (dci.m_isSelect){
				displayLink = "<br/><input type='checkbox' name='select_"+selectCheckBoxVarName+"' class='tn' onclick='setSelectAll(this)'/>";
			}
			td.setDisplay(displayLink);
			tr.setRowData(td);  	
		}
		//print first iterDim cols ..if iter dims are not there then prints measure cols. 
		ArrayList<Value> iterList = iterValues.size() > 0 ? iterValues.get(0) : null;
		DimInfo iterDim = iterDims.size() > 0 ? iterDims.get(0) : null;
		int varColSpan = 1;
		boolean allColTot = false;
		if (iterValues.size() > 1) {
			varColSpan = iterValues.get(1).size()+1; //+1 for total
		}
		else if (measureDims.size() > 1) {
			varColSpan = measureDims.size();
		}
		int numIterDimCols = 0;
		
		if (iterList != null && iterList.size() > 0) {
			numIterDimCols = iterList.size() + 1; //+1 for total
			if (numIterDimCols == 2)
				numIterDimCols = 1; //no point doing total
			else
				allColTot = true;
		}
		else {//may have to iter by measureDims ... to make life simple create the topIter by looking at name from fpiList
			iterList = new ArrayList<Value>();
			for (int i=0,is=measureDims.size();i<is;i++) {
				iterList.add(new Value(fpList.get(numFixedCols+i).m_name));
			}
			numIterDimCols = iterList.size();
		}
		Connection conn = session.getConnection();
		for(int i=0; i<numIterDimCols; i++){
			Value colNameValue = i == iterList.size() ? new Value("Total") : iterList.get(i);
			String dispName = colNameValue.toString(i==iterList.size() ? null : iterDim, null, null, session, cache, conn, sdf);
			td = new TD();
			DimInfo dimInfo = hasMultiple ? measureDims.get(0) : i >= measureDims.size() ? measureDims.get(0) : measureDims.get(i);
			int attribType = dimInfo.getAttribType();
            boolean doDate = attribType == cache.DATE_TYPE;
            boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
			td.setContentType(doDate ? attribType : doInterval ? Misc.getParamAsInt(dimInfo.m_subtype,2) : attribType);
			int colSpan =  i == iterList.size() ? 1 : varColSpan;
			td.setColSpan(colSpan); //just one tot for all instead of second row for each second iter dims
			td.setRowSpan(i == iterList.size() ? 2 :1);//just one tot for all instead of second row for each
			boolean doSortLink = colSpan <= 1;
			String name = dispName;
			if (doSortLink){
				displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>";
				//displayLink	+= name != null && name.length() != 0 ? name : "&nbsp;";
			}
			else {
				displayLink = null;
			}

			td.setContent(name);
			if(dimInfo != null)
			{
				int paramid = dimInfo.m_id;
				td.setId(paramid);
//				if(paramid == 20206 || paramid == 20205 || paramid == 20204 || paramid == 20203 || paramid == 20202)
//					updateIndex.add(new Pair<String, Integer>(name, index));
			}
			index++;
			td.setLinkAPart(displayLink);
			tr.setRowData(td);  	
		}
		table.setHeader(tr);
		if (hasMultiple) {
			iterList = iterValues.size() > 1 ? iterValues.get(1) : null;
			iterDim = iterDims.size() > 1 ? iterDims.get(1) : null;
			if (iterList == null) { //get it from measureDims
				iterList = new ArrayList<Value>();
				for (int i=0,is=measureDims.size();i<is;i++) {
					iterList.add(new Value(fpList.get(numFixedCols+i).m_name));
				}
			}
			tr = new TR();
			tr.setClassId(headerClassId);
			
			for (int i=0,is = allColTot ? numIterDimCols-1 : numIterDimCols;i<is;i++) {
				for (int j=0;j<varColSpan;j++) {
					Value colNameValue = j == iterList.size() ? new Value("Total") : iterList.get(j);
					String dispName = colNameValue.toString(j == iterList.size() ? null : iterDim, null, null, session, cache, conn, sdf);
					td = new TD();
					DimInfo dimInfo =  j >= measureDims.size() ? measureDims.get(0) : measureDims.get(j);
					int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
		            boolean doDate = attribType == cache.DATE_TYPE;
		            boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
					td.setContentType(doDate ? attribType : doInterval ? Misc.getParamAsInt(dimInfo.m_subtype,2) : attribType); 
					if (dimInfo != null)
					{
						int paramid = dimInfo.m_id;
						td.setId(paramid);
					}
					boolean doSortLink = true;
					if (doSortLink){
						displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>";
						//displayLink += dispName != null && dispName.length() != 0 ? dispName : "&nbsp;";
						//displayLink += "</a>";
					}
					td.setContent(dispName);
					td.setLinkAPart(displayLink);
					tr.setRowData(td);	
	
				}
			}
			table.setHeader(tr);
		}

	}
	/*
	public void printTableHeaderOrig(ArrayList<DimConfigInfo> fpList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper, SessionManager _session, ArrayList<DimInfo> iterDims, ArrayList<Value> iterValues, int origListSz, Table table){
		int cols = 0;
		TR tr = null;
		TD td = null;
		if(fpList != null)
			cols = fpList.size();
		tr = new TR();
		boolean doingMultiMeasure = cols != origListSz;
		int rowSpan = doingMultiMeasure ? 2 : 1;
		int colSpan = doingMultiMeasure ? (cols-origListSz+1) : 1;
		int css = doingMultiMeasure ? 0 : 1;
		int numFixedCols = origListSz-2;
		for (int i=0;i<numFixedCols; i++){
			DimConfigInfo dci = fpList.get(i);
			if(!dci.m_hidden){
				td = new TD();
				td.setRowSpan(rowSpan);
				td.setClassId(css);
				td.setContent(dci.m_name);
				tr.setRowData(td);
			}
		}
		for (int i=0,is=dateList.size();i<is;i++) {
			td = new TD();
			td.setColSpan(colSpan);
			td.setClassId(css);
			td.setContent(dateList.get(i));
			tr.setRowData(td);
		}
		table.setHeader(tr);
		if (doingMultiMeasure) {
			tr = new TR();
			for (int i=0,is=dateList.size()+1;i<is;i++) {
				for (int j=origListSz-1;j<cols;j++) {
					DimConfigInfo dci = fpList.get(j);
					td = new TD();
					td.setClassId(css);
					td.setContent(dci.m_name);
					tr.setRowData(td);
				}
			}
			table.setHeader(tr);
		}
	}
	*/

	public void printTableBody(ResultInfo.FormatHelper formatHelper, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<Value>> listOfRows, HashMap<String, ArrayList<Value>> values, ArrayList<ArrayList<Value>> iterValues , ArrayList<DimInfo> iterDims, ArrayList<DimInfo> measureDims, ColorCodeBean clrBean, int origListSz, Table table) throws Exception {
		TR tr = null;
		TD td = null;
		boolean checkAll = true;
		
		int numFixedCols = fpList.size()-iterDims.size()-measureDims.size();
		int measureCols = measureDims.size();
		int numDimIter = iterDims.size();
		
		int num1stIterCols = 0; //number of blocks of num2ndIterCols including total if any
		boolean doAllColTotal = true;;
		boolean doSubColTotal = true;
		if (iterValues.size() == 0) {
			num1stIterCols = measureDims.size();
			doAllColTotal = false;
			doSubColTotal = false;
		}
		else {
			num1stIterCols = iterValues.get(0).size()+1;//for total
			if (num1stIterCols == 2) { //no point in doing total for 1
				num1stIterCols = 1;
				doAllColTotal = false;
			}
		}
		int num2ndIterCols = 1;
		if (iterValues.size() > 1) {
			num2ndIterCols = iterValues.get(1).size()+1; //for total
			if (num2ndIterCols == 2) {
				num2ndIterCols = 1;
				doSubColTotal = false;
			}
		}
		else if (iterValues.size() > 0) {
			num2ndIterCols = measureDims.size();
			doSubColTotal = false;
		}
		ArrayList<ArrayList<Value>> vdesc = new ArrayList<ArrayList<Value>>(num1stIterCols*num2ndIterCols);
		ArrayList<Value> currPrefix = new ArrayList<Value>(iterDims.size());
		computeValDescriptorHelper(currPrefix,  iterValues, vdesc);

		ArrayList<Value> subColTotal = doSubColTotal ? new ArrayList<Value>(measureDims.size()) : null;
		ArrayList<Value> allColTotal = doAllColTotal ? new ArrayList<Value>(measureDims.size()) : null ;
		ArrayList<ArrayList<Value>> allRowTotal = true || listOfRows.size() > 1 ? new ArrayList<ArrayList<Value>>(vdesc.size()) : null;//1st iindex is index of valDesc
		
	
		for (int i=0,is = subColTotal == null ? 0 : measureCols; i<is;i++) {
			subColTotal.add(new Value(0.0));
		}
		for (int i=0,is = allColTotal == null ? 0 : measureCols; i<is;i++) {
			allColTotal.add(new Value(0.0));
		}
		for (int j=0,js=allRowTotal == null ? 0 : vdesc.size();j<js;j++) {
			ArrayList<Value> r = new ArrayList<Value>();
			allRowTotal.add(r);
			for (int i=0,is = measureCols;i<is;i++) {
				r.add(new Value(0.0));
			}
		}
		Cache cache = session.getCache();
		Connection conn = session.getConnection();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		
		for (int i=0,is=listOfRows.size()+(allRowTotal == null ? 0 : 1);i<is;i++) {
			
			tr = new TR();
			boolean doingGrandTotal = i == listOfRows.size();
			String rowKey = null;
			//Print Fixed Cols ..
			ArrayList<Value> row = null;
			if (doingGrandTotal) {
				//dont why td style=display:none is screwing up stuff ..so hacking and printing the hidden stuff
				td = new TD();
				int cellClass = 2;
				td.setHidden(true);
				tr.setRowData(td);
				td = new TD();
				td.setColSpan(numFixedCols-1);
				td.setDisplay("Total");
				td.setClassId(cellClass);
				td.setDoGroup(false);
		
				tr.setRowData(td);
				row = new ArrayList<Value> ();
				for (int j=0;j<numFixedCols;j++) {
					Value v =new Value();
					v.setGroup();
					row.add(v);
				}
			}
			else {
				row = listOfRows.get(i);
				rowKey = getStringOf(row);
				
				for (int j=0;j<numFixedCols; j++) {
					td = new TD();
					DimConfigInfo dci = fpList.get(j);
					if (dci.m_hidden){
						td.setHidden(true);
					}
					//TIRED ... really need to have general print DCI and value and use it everywhere 
					Value currVal = row.get(j);
					DimInfo dimInfo = dci.m_dimCalc == null ? null : dci.m_dimCalc.m_dimInfo;
					String disp = currVal.toString(dimInfo, formatHelper.multScaleFactors.get(j), formatHelper.getFormatter(j), session, cache, conn, sdf);
					if(dimInfo != null){
						int paramid = dimInfo.m_id;
						td.setId(paramid);
						table.setIndexById(paramid,j);
					}
					if (dimInfo.m_id == 20356){//row number
						disp = Integer.toString(i+1);
						td.setContent(disp);
					}
					int type = dimInfo != null ? dimInfo.m_type : Cache.STRING_TYPE;
					td.setDisplay(disp);
					td.setContentType(type);
					boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;
					int cellClass = doNumber ? 3 : 2;
					td.setClassId(cellClass);
					td.setDoGroup(false);
					tr.setRowData(td);
				}
			}//printed fixed cols that were not grand total
			//now print values
			ArrayList<Value> prevValDesc = null;
			for (int j=0,vs=vdesc.size(), js=vdesc.size()+(allColTotal == null && subColTotal == null ? 0 : 1);j<js;j++) { //for printing col tot
				ArrayList<Value> currValDesc = j == vs ? null : vdesc.get(j);
				boolean doingSubColTotal = subColTotal != null && prevValDesc != null 
				   && 
						   (currValDesc != null && !currValDesc.get(0).equals(prevValDesc.get(0)) || currValDesc == null)
						   ;
				boolean doingAllColTotal = j == vs && allColTotal != null && !doingSubColTotal; //why no allColTotal
				
				//if doingSubColTotal ... will decrement j so that we get to redo the same but without total
				//add cols corresponding dimIter to row so that link could be generated
				if (j == vs && !doingAllColTotal && !doingSubColTotal) //subColTotal at end will force another iteration even if no doingAllColTotal
					break;
				int origRowSize = row.size();
				if (doingAllColTotal) {
					//nothin to add
				}
				else if (doingSubColTotal) {
					row.add(prevValDesc.get(0));
				}
				else {
					for (Value v:currValDesc)
						row.add(v);
				}
				ArrayList<Value> valuesToPrint = null;
				if (doingSubColTotal)
					valuesToPrint = subColTotal;
				else if (doingAllColTotal)
					valuesToPrint = allColTotal;
				else if (doingGrandTotal){//get values to print ... to handle rowTotal
					valuesToPrint = allRowTotal.get(j);
				}
				else {
					String key = PrintSummaryTable.getStringOf(rowKey, currValDesc);
					valuesToPrint = values.get(key);
					if (valuesToPrint == null) {
						valuesToPrint = new ArrayList<Value>(measureDims.size());
						for (int l1=0,l1s=measureDims.size();l1<l1s;l1++)
							valuesToPrint.add(new Value(0));
					}
				}
				//gotten values to print
				for (int l1=0,l1s=valuesToPrint.size();l1<l1s;l1++) {
					td = new TD();
					int fpIndex = numFixedCols+numDimIter+l1;
					DimConfigInfo dataDimConf = fpList.get(fpIndex);
					Value val = valuesToPrint.get(l1);
					String disp = val.toString(dataDimConf == null || dataDimConf.m_dimCalc == null ? null : dataDimConf.m_dimCalc.m_dimInfo, formatHelper.multScaleFactors.get(fpIndex), formatHelper.getFormatter(fpIndex), session, cache, conn, sdf);

					int typeDataDimConf = dataDimConf == null || dataDimConf.m_dimCalc == null || dataDimConf.m_dimCalc.m_dimInfo== null ? Cache.STRING_TYPE : dataDimConf.m_dimCalc.m_dimInfo.m_type;
					boolean doNumberDataDimConf = typeDataDimConf == Cache.INTEGER_TYPE || typeDataDimConf == Cache.LOV_NO_VAL_TYPE || typeDataDimConf == Cache.NUMBER_TYPE;
					int cellClassDataDimConf = doNumberDataDimConf ? 3 : 2;
					boolean isNull =  (disp == null || disp.length() == 0);
					//public static String generateLink(DimConfigInfo.LinkHelper linkHelper, ArrayList<DimConfigInfo> fpList, ResultInfo rs, String topPageContext,
					//		boolean onlyParamPart, SessionManager session, String objectIdParamLabel, int objectIdParamCol, ArrayList<Value> altRowVals) throws Exception { //will return null if there is no link to be printed
					
					String link = isNull ? null : GeneralizedQueryBuilder.generateLink(dataDimConf.m_linkHelper, fpList, null,searchBoxHelper == null ? "p" : searchBoxHelper.m_topPageContext, false, session, null, Misc.getUndefInt(), row);
					if (link != null) {
						link = appendAddnlParams(link, row, numFixedCols, numDimIter, fpList);
						StringBuilder sb = new StringBuilder();

						if(dataDimConf.m_show_modal_dialog){
							if (link.indexOf('?') >= 0)
								link += "&_do_popup=1";
							else
								link +="?_do_popup=1";
							sb.append(" <a href=\"#\" onClick=\"popShowModalDialog('"+link);
							sb.append(" ', event.srcElement, '"+dataDimConf.m_dialog_width+"', '"+dataDimConf.m_dialog_height+" ' )\"> ");
						}
						else
							sb.append("<a href=\"").append(link).append("\">");
						sb.append(disp);
						if (link != null) {
							sb.append("</a>");
						}
						disp = sb.toString();
					}
					
					if (disp == null || disp.length() == 0)
						disp = Misc.nbspString;
					
					td.setDisplay(disp);
					td.setContent(disp);
					td.setContentType(typeDataDimConf);
					//Now generate link
					double v = val.getDoubleVal();
					if (disp != null && !Misc.nbspString.equals(disp)) {
						if(clrBean != null && clrBean.isColorCode()) {
							if (clrBean.isIncerasing()) {
								cellClassDataDimConf = clrBean.getThresholdOne() < v && v < clrBean.getThresholdTwo()? 5 : 4;
								cellClassDataDimConf = v >clrBean.getThresholdTwo() ? 6 : cellClassDataDimConf;
							} 
							else {
								cellClassDataDimConf = v <= clrBean.getThresholdTwo() && v <= clrBean.getThresholdOne() ? 6 : 5;
								cellClassDataDimConf = v >=clrBean.getThresholdTwo() ? 4 : cellClassDataDimConf;
							}
						}
						else {
							if (dataDimConf.m_color_code) {
								if (dataDimConf.m_param2_color == 0) {
									cellClassDataDimConf = v <=dataDimConf.m_param1 ? 4 : v > dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ? 5 :6;
								}
								else {
									cellClassDataDimConf = v >= dataDimConf.m_param2 ? 4 : v >dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ? 5 : 6;
								} 
							}
						}
					}
					td.setClassId(cellClassDataDimConf);
					tr.setRowData(td);
				}//for each value to be print
				for (int l1=0,l1s = row.size()-origRowSize;l1 < l1s; l1++)
					row.remove(origRowSize);
				
				if (doingSubColTotal) {
					j--; //so that we come to same valDesc and print it for that
					for (int l1=0,l1s=subColTotal.size();l1<l1s;l1++) {
						subColTotal.get(l1).setValue(0.0);
					}
				}
				else if (subColTotal != null){
					for (int l1=0,l1s=subColTotal.size();l1<l1s;l1++) {
						subColTotal.get(l1).applyOp(CalcFunctionEnum.CUMM, valuesToPrint.get(l1));
					}
				}
				if (doingAllColTotal) {
					for (int l1=0,l1s=allColTotal.size();l1<l1s;l1++) {
						allColTotal.get(l1).setValue(0.0);
					}
				}
				else if (allColTotal != null && !doingSubColTotal){
					for (int l1=0,l1s=allColTotal.size();l1<l1s;l1++) {
						allColTotal.get(l1).applyOp(CalcFunctionEnum.CUMM, valuesToPrint.get(l1));
					}
				}
				if (!doingAllColTotal && !doingSubColTotal && allRowTotal != null) {
					ArrayList<Value> rowTot = allRowTotal.get(j);
					for (int l1=0,l1s=rowTot.size();l1<l1s;l1++) {
						rowTot.get(l1).applyOp(CalcFunctionEnum.CUMM, valuesToPrint.get(l1));
					}
				}
				prevValDesc = currValDesc;
			}//for each val desc
			table.setBody(tr);
			for (int t1=0,t1s=subColTotal == null ? 0 : subColTotal.size();t1<t1s;t1++){
				subColTotal.get(t1).setValue(0.0);
			}
			for (int t1=0,t1s=allColTotal == null ? 0 : allColTotal.size();t1<t1s;t1++){
				allColTotal.get(t1).setValue(0.0);
			}
			
				
		}//for each row
	}
	
	String appendAddnlParams(String link, ArrayList<Value>row, int numFixedCols,int  numDimIter, ArrayList<DimConfigInfo>fpList) {
		boolean hasQ = link.indexOf("?") >= 0;
		StringBuilder sb = new StringBuilder();
		
		for (int i=0,is=(numFixedCols+numDimIter) >= row.size() ? row.size() : numFixedCols+numDimIter; i<is;i++) {
			if (i >0 && i< (numFixedCols))
				continue;
			Value v = row.get(i);
			DimConfigInfo dci = fpList.get(i);
			DimInfo dmi = dci == null || dci.m_dimCalc == null ? null : dci.m_dimCalc.m_dimInfo;
			if (v == null || dmi == null || v.m_type == Cache.GROUP_TOTAL)
				continue;
			String paramName = "pv"+dmi.m_id;
			if (hasQ)
				sb.append("&");
			else
				sb.append("?");
			sb.append(paramName).append("=").append(v.toString());
			if (dci != null && dci.m_columnName != null && !dci.m_columnName.equals(paramName) && !dci.m_columnName.equals("d"+dmi.m_id)) {
				sb.append("&").append(dci.m_columnName).append("=").append(v.toString());
			}
				
			hasQ = true;
		}
		return link+sb.toString();
	}
	
	void computeValDescriptorHelper(ArrayList<Value> currPrefix, ArrayList<ArrayList<Value>> dimValList, ArrayList<ArrayList<Value>> vdesc) {
		int prefixLength = currPrefix.size();
		if (prefixLength == dimValList.size()) { // a valid tuple generated ... add it
			ArrayList<Value> val = new ArrayList<Value>(currPrefix);
			vdesc.add(val);
		}
		else {
			if (prefixLength < dimValList.size()) {
				ArrayList<Value> valIdList = dimValList.get(prefixLength);
				int i=0, sz = valIdList.size();
				for (;i<sz;i++) {
					currPrefix.add(valIdList.get(i));
					computeValDescriptorHelper(currPrefix, dimValList, vdesc);
					currPrefix.remove(currPrefix.size()-1);
				}
			}
		}
	}
	
	private HashMap<String, String> getSearchParamsForLink(SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper){
		HashMap<String, String> paramList = new HashMap<String, String>();
		try {
			if (searchBoxHelper == null)
				return null;
			String topPageContext = searchBoxHelper.m_topPageContext;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null || dimConfig.m_skip_link_params)
						continue;
					boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
					boolean isTime = "20506".equals(dimConfig.m_dimCalc.m_dimInfo.m_subtype);
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
					String tempVal = _session.getAttribute(is123 ? "pv123" : topPageContext+paramId);
					paramList.put(is123 ? "pv123" : "home"+paramId, tempVal);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return paramList;
	}
	public String getUrl(String url, HashMap<String, String> map) {
		if(url == null || !(url.length() > 0))
			return null;
		StringBuilder sb = new StringBuilder(url);
		List<String> listOfParams = new ArrayList<String>();
		for (String param : map.keySet()) {
			listOfParams.add(param + "=" + encodeString(map.get(param)));
		}
		if (!listOfParams.isEmpty()) {
			String query = "";
			int listSize = listOfParams.size();
			for(int i=0; i<listSize; i++){
				if(i == 0)
					query += listOfParams.get(i);
				else
					query += "&" + listOfParams.get(i);
			}
			if(!(url.indexOf("?") > 0))
				sb.append("?");
			if(!(url.indexOf("&") > 0))
				sb.append("&");
			sb.append(query);
		}

		return sb.toString();
	}
	public static String encodeString(String name) throws NullPointerException {
		String tmp = null;

		if (name == null)
			return null;

		try {
			tmp = java.net.URLEncoder.encode(name, "UTF-8");
		} catch (Exception e) {}

		if (tmp == null)
			throw new NullPointerException();

		return tmp;
	}
}
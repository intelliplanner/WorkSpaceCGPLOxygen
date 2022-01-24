package com.ipssi.reporting.trip;

import java.io.ByteArrayOutputStream;
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
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.QueryParts;
import com.ipssi.tracker.colorcode.ColorCodeBean;
import com.ipssi.tracker.colorcode.ColorCodeDao;

public class PrintDateTable {

    
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception{
		return printPage(conn, fpi, session, searchBoxHelper, null, Misc.HTML, "",Misc.getUndefInt());
	}
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper,ByteArrayOutputStream stream,int reportType,String reportName,int reportId) throws Exception {
		return printPage(conn, fpi, session, searchBoxHelper, stream, reportType, reportName, reportId, Table.createTable());
	}
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper,ByteArrayOutputStream stream,int reportType,String reportName,int reportId,Table table) throws Exception {		
		ArrayList<DimConfigInfo> fpiList = (ArrayList<DimConfigInfo>) fpi.m_frontInfoList.clone(); //clone because we will be modifying based on data to show
		//look in search and set the data to show detail in the last cell
		int origListSz = fpiList.size();
	    processForDataToShow(fpiList, fpi.m_frontSearchCriteria, session, searchBoxHelper);
	    
	    ColorCodeDao.getColorCodeInfo(session.getConnection(),Misc.getParamAsInt(session.getParameter("pv123")),session,fpiList,searchBoxHelper);
		StringBuilder sb = new StringBuilder();
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpiList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
		//		printSearchBlock(fpiList, session);
		sb.append("<table ID='DATA_TABLE' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
//		ArrayList<Date> calList = new ArrayList<Date>();
//		String gran = null;
		
		QueryParts qp = qb.buildQueryParts(fpiList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session, searchBoxHelper, formatHelper, fpi.m_colIndexLookup, fpi.m_orderIds, null, Misc.getUndefInt(), fpi.m_doRollupAtJava ? 1 : 0, false, fpi.m_orgTimingBased);
		String query = qb.buildQuery(session, qp, null, false);
		System.out.println("#############"+query);
		ArrayList<ArrayList<String>> listOfRows = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> totValues = new ArrayList<ArrayList<String>>(); //index same as listOfRows
		HashMap<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>(); //key = rowIndex in listOfRows+dateString    	
		FastList<String> dateList = new FastList<String>();
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ResultInfo resultInfo = new ResultInfo(fpiList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,qp.m_needsRollup ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, null , null, qp.m_isInclInGroupBy, null, qp.groupByRollupAggIndicator,qp.m_doRollupAtJava);
		    readResultSet(resultInfo, fpiList, searchBoxHelper, session, listOfRows,  totValues,  values, dateList, origListSz);
			rs.close();
			stmt.close();
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		printTableHeader(fpiList, fpi.m_frontSearchCriteria, searchBoxHelper, session, dateList, origListSz,table);
		printTableBody(formatHelper, fpiList, searchBoxHelper, session, listOfRows,  totValues,  values, dateList , (ColorCodeBean)session.getAttributeObj("colorBean"), origListSz,table);
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
	
	public void processForDataToShow(ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
    	DimConfigInfo dataToShowInfo = fpiList.get(fpiList.size()-1);
   
		dataToShowInfo.m_hidden = false;
		if (searchBoxHelper != null) {
			String topPageContext = searchBoxHelper.m_topPageContext;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				boolean done = false;
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
					if (dimInfo.m_subsetOf == 20052) {
						int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
						String tempVarName =  topPageContext+paramId;
						String tempVal = session.getAttribute(tempVarName);
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
							}
						}
									
						done = true;
						break;
					}
				}//for each col
				if (done)
					break;
			}//for each row
		}
    }
    
public void readResultSet(ResultInfo resultInfo, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList, int origFpiListSize) throws Exception {
	//table to be printed looks as follows:
	// col1 col2 col3    Date1,Date2,Date3
	//                                 M1,M2,M1,M2,M1,M2
	//--------------------------------------------------------
	//r1c1,r1c2,r1c3, r1d1m1,r1d1m2,r1d2m1,r1d2m2 ...
	//r2c1,r2c2,r2c3, r2d1m1,r2d1m2,r2d2m1,r2d2m2 ...
	
	//ArrayList<ArrayList<String>> listOfRows = inner arraylist's size = number of fixed cols and values are entries for each col
	//FastList<String> dateList = distinct dates seen in resultset in sorted manner
	//HashMap<String, ArrayList<String>> values = Key: date+"."+row number (the 1st index of listOfRows above)
	//                                                                                           value: ArrayList of values for each measure
	//
	//ArrayList<ArrayList<String>> totValues = for each row, the values for each measure
	
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
				else {//if(dateStr != null && dateList != null)
				//To-Do Temporary Fix, Need to validate againt other conditions and logic
					if(dateStr==null) dateStr=new StringBuilder();
                    Pair<Integer,Boolean> pos = dateList.indexOf(dateStr==null?"":dateStr.toString());
                    if (pos != null && !pos.second) {
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
	
	
	public void printTableHeader(ArrayList<DimConfigInfo> fpList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper, SessionManager _session, FastList<String> dateList, int origListSz, Table table) throws Exception{
		/*
		 *id-class 
		 *0 -tshb
		 *1 -tshc
		 *2 -cn
		 *3 -nn
		 *4 -nnGreen
		 *5 -nnYellow
		 *6 -nnRed
		 */
		int cols = 0;
		TR tr = null;
		TD td = null;
		if(fpList != null)
			cols = fpList.size();
		tr = new TR();
		boolean doingMultiMeasure = cols != origListSz;
		//String rowSpan = doingMultiMeasure ? " rowspan='2' " : "";
		int rowSpan = doingMultiMeasure ? 2 : 1;
		int colSpan = doingMultiMeasure ? (cols-origListSz+1) : 1;
		//String dtColSpan = doingMultiMeasure ? " colspan='"+(cols-origListSz+1)+"' " : "";
		int css = doingMultiMeasure ? 0 : 1;
		//String css = doingMultiMeasure ? "tshb" : "tshc";
//		sb.append("<thead>");
//		sb.append("<tr>");
		int numFixedCols = origListSz-2;
		for (int i=0;i<numFixedCols; i++){
			DimConfigInfo dci = fpList.get(i);
			if(!dci.m_hidden){
				td = new TD();
				td.setRowSpan(rowSpan);
				td.setClassId(css);
				td.setContent(dci.m_name);
				tr.setRowData(td);
				//sb.append("<td  ").append(rowSpan).append("class='").append(css).append("'>" + dci.m_name + "</td>");
			}
		}
		for (int i=0,is=dateList.size();i<is;i++) {
			td = new TD();
			td.setColSpan(colSpan);
			td.setClassId(css);
			td.setContent(dateList.get(i));
			tr.setRowData(td);
			//sb.append("<td ").append(dtColSpan).append("class='").append(css).append("'>").append(dateList.get(i)).append("</td>");
		}
		td = new TD();
		td.setColSpan(colSpan);
		td.setClassId(css);
		td.setContent("Total");
		tr.setRowData(td);
		table.setHeader(tr);
		//sb.append("<td ").append(dtColSpan).append("class='").append(css).append("'>").append("Total").append("</td>");
		if (doingMultiMeasure) {
			tr = new TR();
			for (int i=0,is=dateList.size()+1;i<is;i++) {
				for (int j=origListSz-1;j<cols;j++) {
					DimConfigInfo dci = fpList.get(j);
					td = new TD();
					td.setClassId(css);
					td.setContent(dci.m_name);
					tr.setRowData(td);
//					sb.append("<td  class='").append(css).append("'>" + dci.m_name + "</td>");
				}
			}
			table.setHeader(tr);
			//sb.append("</tr>");
		}
		//LATER sb.append("<td class='tshc'>&nbsp;</td>");		
		//sb.append("</tr></thead>");
		
	}
	
	public void printTableBody(ResultInfo.FormatHelper formatHelper, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList , ColorCodeBean clrBean, int origListSz, Table table) throws Exception {
		/*
		 *id-class 
		 *0 -tshb
		 *1 -tshc
		 *2 -cn
		 *3 -nn
		 *4 -nnGreen
		 *5 -nnYellow
		 *6 -nnRed
		 */
		TR tr = null;
		TD td = null;
		boolean checkAll = true;
		HashMap<Integer, Double> colTotal = new HashMap<Integer, Double>();
		HashMap<Integer, Double> rowTotal = new HashMap<Integer, Double>();
//		sb.append("<tbody>");
        int numFixedCols = origListSz-2;
        int measureCols = fpList.size()-origListSz+1;
        int mearureSubCols = (dateList.size()*measureCols);
    	for (int m=0;m<mearureSubCols+measureCols;m++){
	        	colTotal.put(m, 0.0);
        	}
         for (int i=0,is=listOfRows.size();i<is;i++) {
        	 for (int l=0;l<measureCols;l++){
  	        	rowTotal.put(l, 0.0);
  	        	}
        	tr = new TR();
        	//sb.append("<tr>");
        	ArrayList<String> row = listOfRows.get(i);
       		for (int j=0;j<numFixedCols; j++) {
    			DimConfigInfo dci = fpList.get(j);
    			if(dci.m_hidden)
    				continue;
    			td = new TD();
    			String disp = row.get(j);
    			td.setDisplay(disp);
    			td.setContent(disp);
				int type = dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.m_type;
				boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;					
				td.setContentType(type);
				String cellClass = doNumber ? "nn" : "cn";
				int css = doNumber ? 3 : 2;
				td.setClassId(css);
				boolean colorForAll = checkAll;
				//sb.append("<td class='").append(cellClass).append("'>");
				//sb.append(disp);
				//sb.append("</td>");
				tr.setRowData(td);
    		}    		
			//print vals for each dateCell
    		for (int j=0,js=dateList.size();j<js;j++) {
    			StringBuilder dateLookup = new StringBuilder();
    			dateLookup.append(dateList.get(j)).append(".").append(i);
    			ArrayList<String> dispValues = values.get(dateLookup.toString());
    	       
    			for (int k=0;k<measureCols;k++) {
    				td = new TD();
       				int ks = dispValues == null ? 0 : dispValues.size();
    				DimConfigInfo dataDimConf = fpList.get(numFixedCols+k+1);
    				int typeDataDimConf = dataDimConf == null || dataDimConf.m_dimCalc == null || dataDimConf.m_dimCalc.m_dimInfo== null ? Cache.STRING_TYPE : dataDimConf.m_dimCalc.m_dimInfo.m_type;
    				boolean doNumberDataDimConf = typeDataDimConf == Cache.INTEGER_TYPE || typeDataDimConf == Cache.LOV_NO_VAL_TYPE || typeDataDimConf == Cache.NUMBER_TYPE;
    				//String cellClassDataDimConf = doNumberDataDimConf ? "nn" : "cn";
    				int cellClassDataDimConf = doNumberDataDimConf ? 3 : 2;
    				String disp = k < ks ? dispValues.get(k) : null;
    				if (disp == null || disp.length() == 0)
        				disp = Misc.nbspString;
    				td.setDisplay(disp);
    				td.setContent(disp);
    				td.setContentType(typeDataDimConf);
        			double v = Misc.getParamAsDouble(disp);
        			//String colorClass = cellClassDataDimConf;
        			if(!Misc.isUndef(v)){
        				rowTotal.put(k, v+rowTotal.get(k));
        				colTotal.put(k+j*measureCols, v+colTotal.get(k+j*measureCols));
        			}
        			if (disp != null && !Misc.nbspString.equals(disp)) {
	        			if(clrBean != null && clrBean.isColorCode())
	        			{
	        				if(clrBean.isIncerasing())
	        				{
	        					cellClassDataDimConf = clrBean.getThresholdOne() < v && v < clrBean.getThresholdTwo()? 5 : 4;
	        					cellClassDataDimConf = v >clrBean.getThresholdTwo() ? 6 : cellClassDataDimConf;
	        					//colorClass = clrBean.getThresholdOne() < v && v < clrBean.getThresholdTwo()? "nnYellow": "nnGreen";
	        					//colorClass = v >clrBean.getThresholdTwo()?"nnRed":colorClass;
	        				}else
	        				{
	        					cellClassDataDimConf = v <= clrBean.getThresholdTwo() && v <= clrBean.getThresholdOne() ? 6 : 5;
	        					cellClassDataDimConf = v >=clrBean.getThresholdTwo() ? 4 : cellClassDataDimConf;
	        					//colorClass = v <= clrBean.getThresholdTwo() && v <= clrBean.getThresholdOne()?"nnRed" :"nnYellow" ;
	            				//colorClass = v >=clrBean.getThresholdTwo()?"nnGreen":colorClass;
	        				}
	        			}
	        			else {
	    					if (dataDimConf.m_color_code) {
	    						if (dataDimConf.m_param2_color == 0) {
	    							cellClassDataDimConf = v <=dataDimConf.m_param1 ? 4 : v > dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ? 5 :6;
	    							//colorClass = v <=dataDimConf.m_param1?"nnGreen": v > dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ?"nnYellow":"nnRed";
	    						}
	    						 else {
	    							 cellClassDataDimConf = v >= dataDimConf.m_param2 ? 4 : v >dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ? 5 : 6;
	    							//colorClass = v >= dataDimConf.m_param2 ?"nnGreen": v >dataDimConf.m_param1 &&  v <= dataDimConf.m_param2 ?"nnYellow":"nnRed";
	    						} //if color coding is applicable
	    					}// if has color coding
	        			}// multi measure color coding
        			} //if disp is not null
        			td.setClassId(cellClassDataDimConf);
        			tr.setRowData(td);
        			//sb.append("<td class='").append(colorClass).append("'>");
    				//sb.append(disp);
    				//sb.append("</td>");				
    			}
    		}
    		for (int n=0;n<measureCols;n++) {
    			td = new TD();
    			int cellClassDataDimConf = 3;
    			int disp = (int) Math.abs(rowTotal.get(n));
    			if (!Misc.isUndef(disp)) {
    				colTotal.put(mearureSubCols+n, disp+colTotal.get(mearureSubCols+n));
        			if(clrBean != null && clrBean.isColorCode())
        			{
        				if(clrBean.isIncerasing())
        				{
        					cellClassDataDimConf = clrBean.getThresholdOne() < disp && disp < clrBean.getThresholdTwo()? 5 : 4;
        					cellClassDataDimConf = disp >clrBean.getThresholdTwo() ? 6 : cellClassDataDimConf;
        				}else
        				{
        					cellClassDataDimConf = disp <= clrBean.getThresholdTwo() && disp <= clrBean.getThresholdOne() ? 6 : 5;
        					cellClassDataDimConf = disp >=clrBean.getThresholdTwo() ? 4 : cellClassDataDimConf;
        				}

        			}
    			}
    			td.setClassId(cellClassDataDimConf);
    			td.setDisplay((!Misc.isUndef(disp) && disp > 0.0) ? disp+"" :Misc.nbspString );
    			td.setContent((!Misc.isUndef(disp) && disp > 0.0) ? disp+"" :Misc.nbspString );
    			tr.setRowData(td);
    		}
    		table.setBody(tr);
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
				
//    				sb.append("<td class='").append(cellClassDataDimConf).append("'><b>");
//    				sb.append(disp);
//    				sb.append("</b></td>");
    			}
    		}
//        	sb.append("</tr>");
        }//for each row
         tr = new TR();
         td = new TD();
         td.setColSpan(numFixedCols);
         td.setDisplay("Grand Total");
         td.setContent("Grand Total");
         td.setClassId(2);
         tr.setRowData(td);
         for (int n=0;n<mearureSubCols+measureCols;n++) {
        	td = new TD();
        	int cellClassDataDimConf = 3;
 			int disp = (int) Math.abs(colTotal.get(n));
 			if (!Misc.isUndef(disp) && disp > 0.0) {
     			if(clrBean != null && clrBean.isColorCode())
     			{
    				if(clrBean.isIncerasing())
    				{
    					cellClassDataDimConf = clrBean.getThresholdOne() < disp && disp < clrBean.getThresholdTwo()? 5 : 4;
    					cellClassDataDimConf = disp >clrBean.getThresholdTwo() ? 6 : cellClassDataDimConf;
    				}else
    				{
    					cellClassDataDimConf = disp <= clrBean.getThresholdTwo() && disp <= clrBean.getThresholdOne() ? 6 : 5;
    					cellClassDataDimConf = disp >=clrBean.getThresholdTwo() ? 4 : cellClassDataDimConf;
    				}
     			}
 			}
			td.setClassId(cellClassDataDimConf);
			td.setDisplay((!Misc.isUndef(disp) && disp > 0.0) ? disp+"" :Misc.nbspString );
			td.setContent((!Misc.isUndef(disp) && disp > 0.0) ? disp+"" :Misc.nbspString );
			tr.setRowData(td);
 		}
        table.setBody(tr);
	}
	public double getRowWiseTotal(){
		double retval = 0.0;
		return retval;
	}
	public double getColumnWiseTotal(){
		double retval = 0.0;
		return retval;
	}
}
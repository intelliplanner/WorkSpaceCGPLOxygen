package com.ipssi.inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.TimePeriodHelper;

public class InventoryPrintData {
	private SessionManager session = null;
	public InventoryPrintData(SessionManager session){
		this.session = session;
		for (int i = 0; i < 3; i++) {
			String reportHeader = i == 0 ? "Added" : i == 1 ? "Released" :"Size";
			String columnName = i == 0 ? "sum(qty_to_added) qtyAdd" : i == 1 ? "sum(qty_to_release) qtyRelease" :"(max(cummulative_qty_added)-max(cummulative_qty_released)) qtyTotal";
			String columnAlias = i == 0 ? "qtyAdd" : i == 1 ? "qtyRelease" :"qtyTotal";
			String[] dataString = new String[3];
			dataString[0] = reportHeader;
			dataString[1] = columnName;
			dataString[2] = columnAlias;
			reportName.put(i, dataString);
		}
		
	}
private static	HashMap<Integer,String[]> reportName = new HashMap<Integer, String[]>();

	SimpleDateFormat dateFormat = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
	
	public static final boolean g_doRollupAtJava = false; //TODO - not implemented for true case yet
	private static HashMap<String, String> gTableList = new HashMap<String, String>(); //the entries here must match the one below
	public static final String sel = " select ";
	public static final String from = " from singleton, vehicle  ";
	public static final String grp = " group by ";
	public static final String whr = " where ";
	public static final String rollup = " with rollup ";
	public static final String having = " having ";
	private static ArrayList<Pair<String, String>> gTableWithDateOrdered = new ArrayList<Pair<String, String>>();
	static {
		gTableWithDateOrdered.add(new Pair<String, String>("@period","start_time"));
		for (Pair<String, String> entry : gTableWithDateOrdered) {
			gTableList.put(entry.first, entry.second);
		}
	}
    
    
    
	public String printPage(Connection conn, SessionManager session) throws Exception {		
	     StringBuilder sb = new StringBuilder();
		 int categoryId = Misc.getParamAsInt(session.getParameter("categoryCode"));
		 String itemCode = Misc.getParamAsString(session.getParameter("itemCode"));
		 String[] reportType = session.request.getParameterValues("reportTypes");
		 int granDesired = Misc.getParamAsInt(session.getParameter("granularityType"));
		 String startDate = Misc.getParamAsString(session.getParameter("startDate"));
		 String endDate = Misc.getParamAsString(session.getParameter("endDate"));
		 int portNodeId = Misc.getParamAsInt(session.getAttribute("pv123"));
		 long sDate = Misc.getUndefInt();
		 long eDate = Misc.getUndefInt();
		 long shiftSDate = Misc.getUndefInt();
		 long shiftEDate = Misc.getUndefInt();
		 
		 if (startDate != null && startDate.length() > 0) {
			sDate = dateFormat.parse(startDate).getTime();
		}else{
			sDate = System.currentTimeMillis();
		}
		 if (endDate != null && endDate.length() > 0) {
			 eDate = dateFormat.parse(endDate).getTime();
			}else{
				eDate = System.currentTimeMillis();
			}
		 
		 shiftSDate = TimePeriodHelper.getBegOfDate(sDate, granDesired);
		 shiftEDate = TimePeriodHelper.getBegOfDate(eDate, granDesired);

   
		 StringBuilder selectPart1 = new StringBuilder(" select v.categoryId,v.item_code,v.item_name,label ");
		 
		 
	     StringBuilder selectPart = new StringBuilder("select inventory_product.categoryId,inventory_product.item_code,inventory_product.item_name, inventory_product_history.id,@period.id  as tId,label ll");
	     if (reportType != null && reportType.length > 0) {
			for (int i = 0; i < reportType.length; i++) {
				int reportIdx = Misc.getParamAsInt(reportType[i]);
				String columnName = reportName.get(reportIdx)[1];
				String columnAlias = reportName.get(reportIdx)[2];
				selectPart.append(" , "+columnName);
				selectPart1.append(" , v."+columnAlias);
			}
	    	 
				
		}
	     selectPart1.append(" from @period left outer join ( ");
		 
		 selectPart.append(" from inventory_product_history cross join @period right outer join inventory_product on (prodcut_detail_id = inventory_product.id and inventory_product.port_node_id=").append(portNodeId).append(" and inventory_product_history.created_on between start_time and end_time ");
			 if (categoryId >= 0) {
				 selectPart.append(" and categoryId=").append(categoryId);
			}
			 if (itemCode != null && itemCode.length() > 0 && !itemCode.equalsIgnoreCase("-1")) {
				 selectPart.append("  and item_code='").append(itemCode).append("'");
			}
			 selectPart.append(") where inventory_product_history.created_on >='").append(new Timestamp(sDate)).append("' and inventory_product_history.created_on <='").append(new Timestamp(eDate)).append("'");
			 selectPart.append(" group by inventory_product.item_code,label) v on (v.tId = @period.id) where start_time >=' ").append(new Timestamp(shiftSDate)).append("' and start_time <='").append(new Timestamp(shiftEDate)).append("'");
			 

		 String period = granDesired == Misc.SCOPE_WEEK ? "week_table" : granDesired == Misc.SCOPE_MONTH ? "month_table" : granDesired == Misc.SCOPE_ANNUAL ? "year_table" : granDesired == Misc.SCOPE_SHIFT ? 
					"shift_table" : granDesired == Misc.SCOPE_HOUR ? "hour_table" : granDesired == Misc.SCOPE_HOUR_RELATIVE ? "(select date_add(now(), INTERVAL -60 MINUTE) start_time, now() end_time, 1 id, concat(date(now()),' ', hour(now()), 'hr') label, null port_node_id) " : "day_table";
		 String query = selectPart1.append(selectPart).toString();
		 query = query.replaceAll("@period", period);
		 System.out.print("$$$$$="+query);
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 try {
				ArrayList<ArrayList<String>> listOfRows = new ArrayList<ArrayList<String>>();
				//ArrayList<ArrayList<String>> totValues = new ArrayList<ArrayList<String>>(); //index same as listOfRows
				HashMap<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>(); //key = rowIndex in listOfRows+dateString    	
				FastList<String> dateList = new FastList<String>();

			 ps = conn.prepareStatement(query);
			 rs = ps.executeQuery();
			 int rowNo = -1;
             int categoryIdPrev = -1;
             String itemCodePrev = "-1";
             
             
			while (rs.next()) {
			int count = 5;//dynamic column start after 5th index...........
			int catId = rs.getInt(1);
			String itemCodeName = rs.getString(2);
			String itemName = rs.getString(3);
			String label = rs.getString(4);
			ArrayList<Integer> data = new ArrayList<Integer>();
		    for (int i = 0; i < reportType.length; i++) {
		    	int totalAdd = rs.getInt(i + count );
		    	data.add(totalAdd);
			}
			if (catId == 0  && itemCodeName == null && itemName == null) {
				dateList.add(label);
				continue;
			}
			
			dateList.add(label);//make smarter
			if (!itemCodePrev.equalsIgnoreCase(itemCodeName)) {
				ArrayList<String> listRowData = new ArrayList<String>();
				listRowData.add(catId+"");
				listRowData.add(itemCodeName);
				listRowData.add(itemName);
				listOfRows.add(listRowData);
				rowNo++;
				categoryIdPrev = catId;
				itemCodePrev = itemCodeName;
			}
			ArrayList<String> measureVal = new ArrayList<String>();
			 for (int i = 0; i < reportType.length; i++) {
					measureVal.add(data.get(i)+"");
				}
					values.put(label+"."+rowNo, measureVal);
			}
			sb.append("<table ID='DATA_TABLE' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");		
			 PrintTableHeaderNew(sb,dateList,reportType,reportType.length);
			 printTableBodyNew(sb, listOfRows,  values, dateList , reportType.length);	
			 sb.append("</table>");
		} catch (Exception e) {
			 e.printStackTrace();
		}
		
			return sb.toString();
	}

	public void PrintTableHeaderNew(StringBuilder sb,FastList<String> dateList,String[]reportType ,int origListSz){
		int cols = 1;
		boolean doingMultiMeasure = cols != origListSz;
		String rowSpan = doingMultiMeasure ? " rowspan='2' " : "";
		String dtColSpan = doingMultiMeasure ? " colspan='"+(origListSz)+"' " : "";
		String css = doingMultiMeasure ? "tshb" : "tshc";
		sb.append("<thead>");
		sb.append("<tr>");
		for (int i = 0; i < 3; i++) {
			String colName = i == 0 ? "Category Name" : i == 1 ? " Item Code" :"Item Name";
			sb.append("<td  ").append(rowSpan).append("class='").append(css).append("'>" + colName + "</td>");
		}
		for (int i=0,is=dateList.size();i<is;i++) {
			sb.append("<td ").append(dtColSpan).append("class='").append(css).append("'>").append(dateList.get(i)).append("</td>");
		}
		if (doingMultiMeasure) {
			sb.append("<tr>");
			for (int i=0,is=dateList.size();i<is;i++) {
				for (int j=0;j<origListSz;j++) {
					int reportIdx = Misc.getParamAsInt(reportType[j]);
					
					String colName = reportName.get(reportIdx)[0];
					sb.append("<td  class='").append(css).append("'>" + colName+ "</td>");
				}
			}
			
			sb.append("</tr>");
		}
		//LATER sb.append("<td class='tshc'>&nbsp;</td>");		
		sb.append("</tr></thead>");
	}
	public void printTableBodyNew(StringBuilder sb,ArrayList<ArrayList<String>> listOfRows,  HashMap<String, ArrayList<String>> values,FastList<String> dateList , int origListSz) throws Exception{
		sb.append("<tbody>");
        int numFixedCols = 3;
        int measureCols = origListSz;
        for (int i=0,is=listOfRows.size();i<is;i++) {
        	sb.append("<tr>");
        	ArrayList<String> row = listOfRows.get(i);
        	
    		for (int j=0;j<numFixedCols; j++) {
    			String disp = row.get(j);
    			if (j == 0) {
					disp = session.getCache().getAttribDisplayName("inventory_category", Misc.getParamAsInt(disp));
				}
				String cellClass = "cn";
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
    				
    				String cellClassDataDimConf =  "nn";
    				String disp = k < ks ? dispValues.get(k) : null;
    				if (disp == null || disp.length() == 0)
        				disp = Misc.nbspString;
        			sb.append("<td class='").append(cellClassDataDimConf).append("'>");
    				sb.append(disp);
    				sb.append("</td>");				
    			}
    		}
    		sb.append("</tr>");
        }//for each row
        sb.append("</tbody>");
	}

	/*public String createQuery(ArrayList<DimConfigInfo> dimConfigList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper,ResultInfo.FormatHelper formatHelper){
		
		StringBuilder selectClause = new StringBuilder(sel);
		StringBuilder groupByClause = new StringBuilder(grp);
		
		int granDesired = 10;//raw
		StringBuilder query = new StringBuilder();
		StringBuilder whereClauseFromSel = new StringBuilder();
		StringBuilder fromClauseTemp = new StringBuilder();
		StringBuilder joinClauseFromDim = new StringBuilder();
	//	StringBuilder selectPart = new StringBuilder(" select inventory_product.item_code,inventory_product.item_name ");
		HashMap<String, String> tList = new HashMap<String, String>();
		tList.put("inventory_product_history", "inventory_product_history");

		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);
			if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) {
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.length() == 0 || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("Dummy")) {
				continue;
			}

			if (dimConfig.m_granularity || dimConfig.m_dimCalc.m_dimInfo.m_id == 20113 || dimConfig.m_dimCalc.m_dimInfo.m_id == 20257)
				continue;
			ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
			String tab = colMap.table;
			if (tab != null) {
				
				if ("Singleton".equals(tab)) {
					tab = colMap.base_table;
					if (tab == null || tab.length() == 0 || tab.equals("Dummy"))
						tab = "trip_info";
				}
				else if (tab.startsWith("summary_period") || tab.equals("g_trip_start_end_summ") || tab.equals("summary_2_period_trip"))
					tab = "@period";


			}

			if (gTableList.containsKey(tab)) {
				tList.put(tab, tab);
			}
		}
		String firstTableUsedInTimeTableJoin = null;
		String colForFirstTableUsedInTimeTableJoin = null;

		
		 
			for (int i=gTableWithDateOrdered.size()-1; i>=0; i--) {
				if (tList.containsKey(gTableWithDateOrdered.get(i).first)) {
					firstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(i).first;
					colForFirstTableUsedInTimeTableJoin = gTableWithDateOrdered.get(i).second;
					break;
				}
			}
			
			
			 java.util.Date startDt = null;
			 java.util.Date endDt = null;

		for (int i=0,is=dimConfigList.size();i<is;i++){
			DimConfigInfo dimConfig = dimConfigList.get(i);

			if (!selectClause.toString().equals(sel))
				selectClause.append(", ");
			if (dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null) {
				selectClause.append("null");
				continue;
			}
			if (dimConfig.m_dimCalc.m_dimInfo.m_colMap.table == null || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.length() == 0 || dimConfig.m_dimCalc.m_dimInfo.m_colMap.table.equals("Dummy")) {
				selectClause.append("null");
				continue;
			}
		

			String aggregateOp = null;
			String adjColName = null;
			boolean isTimeDim = false;
			boolean isStartDateId = false;
			boolean isEndDateId = false;
			if (dimConfig.m_granularity || dimConfig.m_dimCalc.m_dimInfo.m_id == 20113 || dimConfig.m_dimCalc.m_dimInfo.m_id == 20257) {
				String granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
				granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
				//				if("tr_ana_trip_detail".equalsIgnoreCase(session.getParameter("page_context")) && ShiftPlanInfo.getShiftInfo(session.getParameter("pv123") , new Date(), session.getConnection()) != null)
				if("tr_ana_trip_detail".equalsIgnoreCase(session.getParameter("page_context")) && session.getParameter("pv123") != null && "22".equalsIgnoreCase(session.getParameter("pv123")))
					granDesired = 6;
				//some thing to get the granularity adjusted colName that will adjColName
				//needShiftTableJoin = granDesired == 6;
				FmtI.Date dateFormatter = (FmtI.Date) formatHelper.getFormatter(i);
				String sqlDatePattern = Misc.convertJavaDataFormatToMySQL(dateFormatter == null ? Misc.G_DEFAULT_DATE_FORMAT : dateFormatter.getPattern());
				adjColName = getGranBasedString(firstTableUsedInTimeTableJoin, colForFirstTableUsedInTimeTableJoin, granDesired, sqlDatePattern);
				isTimeDim = true;
				if(dimConfig.m_granularity){
					isStartDateId = PageHeader.isStartDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
					isEndDateId = PageHeader.isEndDateId(dimConfig.m_dimCalc.m_dimInfo.m_id);
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId;
					String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext;
					String tempVarName = topPageContext+paramId;
					String tempVal = session.getAttribute(tempVarName);
					if (isStartDateId) {
						startDt = new java.util.Date(tempVal);	
					}else if (isEndDateId) {
						endDt = new java.util.Date(tempVal);
					}
					
					
				}
			}
			if (dimConfig.m_aggregate) {
				String aggParamName = searchBoxHelper == null ? "p20053": searchBoxHelper.m_topPageContext+"20053";
				int aggDesired =Misc.getParamAsInt(session.getParameter(aggParamName));
				DimInfo aggDim = DimInfo.getDimInfo(20053);
				if (aggDim != null) {
					DimInfo.ValInfo valInfo = aggDim.getValInfo(aggDesired);
					if (valInfo != null) {
						aggregateOp = valInfo.getOtherProperty("op_text");
					}
				}
				if (dimConfig.m_default != null && !"".equals(dimConfig.m_default))
					aggregateOp = dimConfig.m_default;
				if (aggregateOp == null || aggregateOp.length() == 0)
					aggregateOp = "sum";
			}
			ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
			boolean addedInGroupBy = colMap.appendTableColName(selectClause,joinClauseFromDim, groupByClause, whereClauseFromSel, aggregateOp, adjColName, null, true, dimConfig.m_orderByInFrontPage);
			
		
			if (!tList.containsKey(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table)) {
				if("Singleton".equalsIgnoreCase(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table))
				{
					//	fromClauseTemp.append(" join ").append(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table).append(" ");
				}

				if (!isTimeDim)
					tList.put(dimConfig.m_dimCalc.m_dimInfo.m_colMap.table, dimConfig.m_dimCalc.m_dimInfo.m_colMap.table);
			}

		}
		fromClauseTemp.append(" inventory_product left join inventory_product_history on (inventory_product_history.prodcut_detail_id=inventory_product.id)"+ 
       " right join @period on (inventory_product_history.created_on between @period.start_time and @period.end_time)");
		whereClauseFromSel.append(" @period.start_time >= '").append(new Timestamp(startDt.getTime())+"").append("' and @period.start_time <= '").append(new Timestamp(endDt.getTime())+"").append("'");
		
		
		//select inventory_product.item_code,inventory_product.item_name,sum(qty_to_added) totalAdd,sum(qty_to_release) totalRelease,(max(cummulative_qty_added)-max(cummulative_qty_released)) inventorySize,label from 
		//inventory_product left join inventory_product_history on (inventory_product_history.prodcut_detail_id=inventory_product.id) 
		//right join day_table on (inventory_product_history.created_on between start_time and end_time)
		//where start_time >='2013-03-10' and end_time <='2013-03-16'
		//group by inventory_product.item_code,inventory_product.item_name,label;

		
		return query.toString();
	}
	
*/	private static String g_hackTripInfoDateFormat = "%Y-%m-%d";
	private static String getGranBasedString(String table, String column, int granDesired, String sqlDateFormat) {
		sqlDateFormat = g_hackTripInfoDateFormat;//TODO until we figure out how to sort properly
		String col = table+"."+column;
		String adjColName = "";
		if (granDesired == 4)
			adjColName = "DATE_FORMAT(cast("+col+" as date), '"+sqlDateFormat+"')";
		//	cast(cast("+col+" as date) as datetime)
		else if (granDesired == 6)
			adjColName = "concat("+"DATE_FORMAT(cast("+col+" as date), '"+sqlDateFormat+"')"+", ' ' ,shift.name)";
		else if (granDesired == Misc.SCOPE_HOUR)
			adjColName = " @period.label ";
		else if (granDesired == 3)
			adjColName = "DATE_FORMAT(adddate(cast("+col+" as date),1-dayofweek(cast("+col+" as date))), '"+sqlDateFormat+"')";
		else if (granDesired == 2)
			adjColName = "DATE_FORMAT(adddate(cast("+col+" as date),1-dayofmonth(cast("+col+" as date))), '"+sqlDateFormat+"')";
		else if (granDesired != 10)
			adjColName = "DATE_FORMAT(cast("+col+" as date), '"+sqlDateFormat+"')";
		//		else if (granDesired != 10)
		//			adjColName = "DATE_FORMAT(adddate(cast("+col+" as date),1-dayofmonth(cast("+col+" as date))), '"+sqlDateFormat+"')";
		else
			adjColName = "DATE_FORMAT("+col+", '"+sqlDateFormat+"')";;
			return adjColName;
	}
	/*public void processForDataToShow(ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
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
					if (dimInfo.m_subsetOf == 30093) {
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
    }*/
    /*
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
				
				if (!GeneralizedQueryBuilder.g_doRollupAtJava && resultInfo.isCurrEqualToRelativeRow(1)) {
					continue;					
				}
				
				
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
	*/
	
	/*public void printTableHeader(StringBuilder sb, ArrayList<DimConfigInfo> fpList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper, SessionManager _session, FastList<String> dateList, int origListSz){
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
	}*/
/*		public void printTableBody(StringBuilder sb, ResultInfo.FormatHelper formatHelper, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper, SessionManager session,ArrayList<ArrayList<String>> listOfRows, ArrayList<ArrayList<String>> totValues, HashMap<String, ArrayList<String>> values,FastList<String> dateList , ColorCodeBean clrBean, int origListSz) throws Exception {
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

*/

}

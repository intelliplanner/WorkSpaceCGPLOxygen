package com.ipssi.reporting.trip;

import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.servlet.jsp.JspWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ipssi.dispatchoptimization.PitStatsDTO;
import com.ipssi.dispatchoptimization.ShovelStatsDTO;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.ChartInfo;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.cache.CacheManager;

public class ChartJasonGenerator {
	private static final String dataKey="data";
	private static final String chartKey="chart";
	private static final String datasetKey="dataset";
	private static final String categoriesKey="categories";
	private static final String categoryKey="category";
	private static final String seriesnameKey="seriesname";
	private static final String trendlinesKey="trendlines";
	private static final String linesetKey="lineset";
	private static final String labelKey="label";
	private static final String valueKey="value";
	private static final String colorKey = "color";	
	private static final String totalKey = "Total";
	private static final String BLANK = "&nbsp;";
	private static final String numbersuffixKey = "numbersuffix";
	private static final String OPTIMIZATION="optimization_";
	private static final String LINE_CHART = "line";
	private static final String COLUMN2D_CHART="column2d";
	private static final String MSBAR2D_CHART="msbar2d";
	private static final String BAR2D_CHART="bar2d";
	private static final String MSLINE_CHART="msline";//Multi Series Line Chart
	private static final String MSLINE_COL_CHART="mslinecol";
	private static final String SCROLL_LINE_CHART="scrollline2d";//scroll in line Chart
	
	private static final String REAL_TIME_CATEGORY="realtime";
	private static final String REAL_TIME_LINE_CHART="realtimeline";
	private static final String REAL_TIME_DB_CHART="realtimeline_db";
	private static final String REAL_TIME_DUAL_Y_CHART="realtimelinedy";
	
	private static final String PIE_CATEGORY="pie";
	private static final String PIE_2D_CHART="pie2d";
	private static final String PIE_MULTILEVEL="multilevelpie";
	
	private static final String STACKED_CATEGORY="stacked";
	private static final String STACKED_COLUMN_CHART="stackedcolumn2d";
	private static final String MS_STACKED_COLUMN_CHART="msstackedcolumn2d";
	private static final String MS_STACKED_COLUMN_DUAL_Y_AXIS="msstackedcolumn2dlinedy";
	

	 public static DimConfigInfo getDimConfigInfoFromFrontList(ArrayList<DimConfigInfo> fpList, int id){
		   for (int i = 0; i < fpList.size(); i++) {
			   if(((DimConfigInfo)fpList.get(i)).m_dimCalc.m_dimInfo.m_id == id) 
				   return (DimConfigInfo)fpList.get(i);
		   }
		   return (DimConfigInfo)fpList.get(0);
	   }
	 @SuppressWarnings("unchecked")
	public static void getNestedJason(FrontPageInfo fpi, Table table, SessionManager session, Connection conn, JspWriter out,int portNodeId)	throws GenericException, ParseException, IOException {
		 FrontPageInfo nestedFP = null;
		String dataSrc ="";
		String frontPageName = "";
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		HashMap<String,Table> nestedTableFPMap=new HashMap();
		
		try {
			populateNestedTableMap(nestedTableFPMap,frontPageName,"",table);
		}catch (ChartException e1) {
			try {
				out.println(e1.getMessage());
				out.println("<BR>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int mapCounter=0;
		for (DimConfigInfo dci : fpList) {
			if (dci.m_refMasterBlockInPFM != null && dci.m_refMasterBlockInPFM.length() != 0) {
				frontPageName = dci.m_refMasterBlockInPFM;
				String pageContext = dci.m_lookHelp1;
				// return (DimConfigInfo)fpList.get(i);
				try {
				if (nestedFP == null) {
					try {
						//To-Do need to pass the proper table Structure to get the data 
						nestedFP = CacheManager.getFrontPageConfig(conn, session.getUser().getUserId(), Misc.getUserTrackControlOrg(session), pageContext,dci.m_refMasterBlockInPFM, 0, 0);
					} catch (Exception e) {
						throw new ChartException("Exception While Getting Nested FrontPageInfo.For "+nestedFP);  
					}
				}
				ChartInfo chartInfo = nestedFP.chartInfo;
				if (chartInfo == null)
					throw new ChartException("Chart Tag Not Found in Nested XML ("+frontPageName+").");
				
				nestedFP.chartInfo.setId("chart"+mapCounter);
				nestedFP.chartInfo.setRenderAt("chart_"+mapCounter);
				//EXTRACT ALL TABLES FROM NESTED REPORT
				dataSrc= getJason(nestedFP,nestedTableFPMap.get(mapCounter+""),session, null,null,portNodeId);
				nestedFP.chartInfo.setDataSource(dataSrc);
				FusionCharts fChart = new FusionCharts(nestedFP.chartInfo);
				
					out.print(fChart.render());
					mapCounter++;
				}catch (ChartException e1) {
					try {
						out.println("<BR>");
						out.println("Exception While Rendering Nested Chart. For "+frontPageName);
						out.println("<BR>");
						out.println(e1.getMessage());
						out.println("<BR>");
					} catch (IOException e) {
						e.printStackTrace();
					}
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			}
			
			nestedFP = null;
		}//End For
	}	
	private static void populateNestedTableMap(HashMap<String,Table> nestedTableFPMap, String frontPageName, String str,	Table table) throws ChartException {
int nestedCounter=0;
		ArrayList<TR> body = table.getBody();
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			for (int col = 0, size = tdArr == null ? 0 : tdArr.size(); col < size; col++) {
				try {
					TD td = tdArr.get(col);
					boolean hasNestedTable = (td != null && td.getNestedTable() != null) ? true: false;
					if (hasNestedTable) {
						Table nestedTab=td.getNestedTable();
						nestedTableFPMap.put(Misc.getParamAsString(nestedCounter+""), nestedTab);
						nestedCounter++;
					}
				}catch (Exception e) {
					throw new ChartException("Exception while getting Nested Table Data. For "+frontPageName);
				}
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static String getJason(FrontPageInfo fpi, Table table, SessionManager session, SearchBoxHelper searchBoxHelper,JspWriter out,int portNodeId) throws ChartException, IOException, ParseException {
		ChartInfo chartInfo=fpi.chartInfo;
		if(chartInfo == null)
			throw new ChartException("Chart Tag Not Found in XML.");
		if(table.getBody()==null)
			throw new ChartException("No Records found for selected search criteria.");
		
		Map<String, String> dataMap = new HashMap<String, String>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String chartCategory = chartInfo.getChartCategory();
		String chartType = chartInfo.getType();
	
		if ("shovel_stats_db_rt".contains(chartCategory)) {
			if ("shovel_stats_db_rt".contains(chartCategory)) {
				
				int _vehicle_rt = Misc.getParamAsInt(session.getParameter("c_vehicle_rt"));
				int _param_rt = Misc.getParamAsInt(session.getParameter("c_param_rt"));
				if (Misc.getParamAsInt(session.getParameter("real_time"),Misc.UNDEF_VALUE) == 1	&& _vehicle_rt > 0 && _param_rt >= 0) {
					// First Call will be normal, next onwards this block will
					// return the values.
					Map<String, Map<String, Map<String, String>>> resultSet = ChartUtil.getShovelDataFromTable(chartInfo,table, fpi, searchBoxHelper,session);
					return ChartUtil.getRealTimeData(resultSet.get(_vehicle_rt+ ""), _param_rt);
				} else {
					dataMap = ChartUtil.getShovelStats(gson, chartInfo, fpi,table, session, searchBoxHelper, out,portNodeId);
				}
				return gson.toJson(dataMap);
			}
		}
		
		if("shovel_stats".contains(chartCategory)){
			dataMap=ChartUtil.getShovelStats(gson,chartInfo,fpi,table,session,searchBoxHelper,out,portNodeId);
			return gson.toJson(dataMap);
		}else if (OPTIMIZATION.contains(chartCategory)) {
			String topPageContext = searchBoxHelper.m_topPageContext;
			ArrayList<Integer> params = new ArrayList();
			ArrayList<Integer> shovelIds = new ArrayList();
			ArrayList<Integer> pitIds = new ArrayList();
			String opt_var=chartCategory.split("_")[1];
			if ("column2d".equalsIgnoreCase(chartType)) {
				OptimizationChartStatsImpl stats = new OptimizationChartStatsImpl();
				ArrayList<Map<String, String>> dataSet =null;
				if("shovel".equalsIgnoreCase(opt_var)){
					ShovelStatsDTO dto = stats.getCurrentShovelStats(shovelIds);
				    dataSet = ChartUtil.getShovelMap(chartInfo, dto);
				}else if("pit".equalsIgnoreCase(opt_var)){
					PitStatsDTO dto = stats.getCurrentPitStats(pitIds);
					dataSet = ChartUtil.getPitMap(chartInfo, dto);
				}else{
					
				}
				if (dataSet != null	&& dataSet.size() > chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(dataSet.size(), chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				dataMap.put(dataKey, gson.toJson(dataSet));
				chartInfo.setId("chart0");
				chartInfo.setRenderAt("chart_0");
				// dataMap.put(trendlinesKey,
				// gson.toJson(getAvgLineJson(chartInfo, dataSet)));
			}

		}else if("exec_load_event".equalsIgnoreCase(chartCategory)) {
			
			
			if(fpi!=null){
//				"category": [
	ArrayList<Map<String, ArrayList<Map<String, String>> >> dataSetList = new ArrayList<Map<String, ArrayList<Map<String, String>> >>();
	
	Map<String, ArrayList<Map<String, String>> > categoriesMap = new HashMap<String, ArrayList<Map<String, String>> >() ;
				ArrayList<Map<String, Object>> dataSet= ChartUtil.getStepLineDetails(table,fpi,searchBoxHelper,session,categoriesMap,chartInfo.getAttributeByName("series_name"));
				System.out.println("StepLine Chart Json:::::");
				if (dataSet != null && dataSet.size() > chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(dataSet.size(), chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				dataMap.put(datasetKey, gson.toJson(dataSet));
				dataSetList.add(categoriesMap);
				dataMap.put(categoriesKey, gson.toJson(dataSetList));
			}
		/**	
			ArrayList<Map<String, String>> dataSet = ChartUtil.getExcavatorLoadEventsDataFromTable(table, fpi, searchBoxHelper,session);
			if (dataSet != null && dataSet.size() > chartInfo.getMaxXAxisPoints())
				ChartUtil.validateXAxisLength(dataSet.size(), chartInfo.getMaxXAxisPoints());
			dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
			dataMap.put(dataKey, gson.toJson(dataSet));
			*/
		}else if("acb_shovel_grouping".equalsIgnoreCase(chartCategory)) {
			Map<String, Map<String, String>> resultSet = ChartUtil.getACBAllDumperDataFromTable(table, fpi, searchBoxHelper,session);
			ArrayList<Map<String, String>> dataSet=ChartUtil.getBar2dJason(resultSet,fpi, searchBoxHelper,session,chartInfo);
			if(dataSet.size()<=0)
				throw new ChartException("Data not available for selected parameters.");
			
			dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
			dataMap.put(dataKey, gson.toJson(dataSet));
			
		}else if("acb_all_dumpers".equalsIgnoreCase(chartCategory)) {
			Map<String, Map<String, String>> resultSet = ChartUtil.getACBAllDumperDataFromTable(table, fpi, searchBoxHelper,session);
			ArrayList<Map<String, String>> dataSet=ChartUtil.getBar2dJason(resultSet,fpi, searchBoxHelper,session,chartInfo);
			if(dataSet.size()<=0)
				throw new ChartException("Data not available for selected parameters.");
			
			dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
			dataMap.put(dataKey, gson.toJson(dataSet));
			
		}else if(COLUMN2D_CHART.equalsIgnoreCase(chartCategory)) {
			ArrayList<Map<String, String>> dataSet=getColumn2DJson(chartInfo, table);
			if(dataSet!=null && dataSet.size()>chartInfo.getMaxXAxisPoints())
				ChartUtil.validateXAxisLength(dataSet.size(),chartInfo.getMaxXAxisPoints());
			dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
			dataMap.put(dataKey, gson.toJson(dataSet));
			dataMap.put(trendlinesKey, gson.toJson(getAvgLineJson(chartInfo, dataSet)));
			
		}else if (MSLINE_COL_CHART.equalsIgnoreCase(chartCategory)) {
			
			if (MSLINE_CHART.equalsIgnoreCase(chartType)) {
				String[] noOfLines=chartInfo.getAttributeByName("category_value_col_indx").split(",");
				ArrayList<DimConfigInfo> fpList =fpi.m_frontInfoList;
				
				ArrayList<Map<String, Object>> dataSetList = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < noOfLines.length; i++) {
					Map<String, Object> seriesMap = new HashMap<String, Object>();
					DimConfigInfo d=getDimConfigInfoFromFrontList(fpList,Misc.getParamAsInt(noOfLines[i]));
					String seriesName=d.m_name;
					boolean doCumulative=chartInfo.getDoCumulative()>0;
					ArrayList<Map<String, String>> columnData=ChartUtil.getMSLineDetailsColumnWise(table,valueKey,Misc.getParamAsInt(noOfLines[i]),doCumulative);
					if (columnData != null && columnData.size() > chartInfo.getMaxXAxisPoints())
						ChartUtil.validateXAxisLength(columnData.size(), chartInfo.getMaxXAxisPoints());
					seriesMap.put(seriesnameKey, seriesName);
					seriesMap.put(dataKey, columnData);
					dataSetList.add(seriesMap);
				}
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				dataMap.put(datasetKey, gson.toJson(dataSetList));
				//to get time block
				int timeBlockDim=Misc.getParamAsInt(chartInfo.getCategorySeries());
				ArrayList<Map<String, String>> categoryData=ChartUtil.getMSLineDetailsColumnWise(table,labelKey,timeBlockDim,false);
				Map<String, ArrayList<Map<String, String>>> categoriesMap=new HashMap<String, ArrayList<Map<String,String>>>();
				categoriesMap.put(categoryKey, categoryData);
				ArrayList<Map<String, ArrayList<Map<String, String>> >> categoriesList = new ArrayList<Map<String, ArrayList<Map<String, String>> >>();        
				categoriesList.add(categoriesMap);
				dataMap.put(categoriesKey, gson.toJson(categoriesList));
			}
		}
		else if (LINE_CHART.equalsIgnoreCase(chartCategory)) {
			
			if (LINE_CHART.equalsIgnoreCase(chartType)) {
				ArrayList<Map<String, String>> dataSet = getColumn2DJson(chartInfo, table);
				if (dataSet != null && dataSet.size() > chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(dataSet.size(), chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				dataMap.put(dataKey, gson.toJson(dataSet));
				dataMap.put(trendlinesKey, gson.toJson(getAvgLineJson(chartInfo, dataSet)));
			} else if (MSLINE_CHART.equalsIgnoreCase(chartType)) {
				// Axis Categories
				ArrayList<Map<String, ArrayList<Map<String, String>>>> axisCats = getCategoriesDetails(chartInfo, table,false);
				if(axisCats!=null && axisCats.size()>chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(axisCats.size(),chartInfo.getMaxXAxisPoints());
				dataMap.put(categoriesKey, gson.toJson(axisCats));
				boolean isMultiSeries=false;
				boolean createLineSet=false;
				ArrayList<Map<String, Object>> datasetOne=getStackedColumnJson(chartInfo,axisCats, table,isMultiSeries,createLineSet,null,false);
//				isMultiSeries=true;
//				ArrayList<Map<String, Object>> datasetTwo=getStackedColumnJson(chartInfo,axisCats, table,isMultiSeries,createLineSet,null);
					//ArrayList<Map<String,ArrayList<Map<String, Object>>>> combineList=new ArrayList<Map<String,ArrayList<Map<String,Object>>>>();
					//Map<String,ArrayList<Map<String, Object>>> combineMap=new HashMap<String, ArrayList<Map<String,Object>>>();
//				combineMap.put(datasetKey, datasetOne);
//				combineList.add(combineMap);
//				combineMap=new HashMap<String, ArrayList<Map<String,Object>>>();
//				combineMap.put(datasetKey, datasetTwo);
//				combineList.add(combineMap);
				dataMap.put(datasetKey, gson.toJson(datasetOne));
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				/*
				ArrayList<Map<String, String>> dataSet = getMSLineJson(chartInfo, table);
				if (dataSet != null && dataSet.size() > chartInfo.getMaxXAxisPoints())
					validateXAxisLength(dataSet.size(), chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(getBasicChartDetails(chartInfo)));
				dataMap.put(dataKey, gson.toJson(dataSet));
				dataMap.put(trendlinesKey, gson.toJson(getAvgLineJson(chartInfo, dataSet)));*/
			}
		} else if (STACKED_CATEGORY.equalsIgnoreCase(chartCategory)) {
			dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
			int hideTime=Misc.getParamAsInt(chartInfo.getAttributeByName("hide_time"));
			// Axis Categories
			ArrayList<Map<String, ArrayList<Map<String, String>>>> axisCats = getCategoriesDetails(chartInfo, table,hideTime==1);
			if(axisCats!=null && axisCats.size()>chartInfo.getMaxXAxisPoints())
				ChartUtil.validateXAxisLength(axisCats.size(),chartInfo.getMaxXAxisPoints());
			dataMap.put(categoriesKey, gson.toJson(axisCats));
			boolean isMultiSeries=false;
			boolean createLineSet=false;
			ArrayList<Map<String, Object>> datasetOne=getStackedColumnJson(chartInfo,axisCats, table,isMultiSeries,createLineSet,null,hideTime==1);
//			Collections.sort(datasetOne);
			if((MS_STACKED_COLUMN_CHART.equalsIgnoreCase(chartType)|| MS_STACKED_COLUMN_DUAL_Y_AXIS.equalsIgnoreCase(chartType)) && chartInfo.getCategoryCompareWithSeries()!=null && chartInfo.getCategoryCompareWithSeries().length()>3){
				isMultiSeries=true;
				ArrayList<Map<String, Object>> datasetTwo=getStackedColumnJson(chartInfo,axisCats, table,isMultiSeries,createLineSet,null,hideTime==1);
				ArrayList<Map<String,ArrayList<Map<String, Object>>>> combineList=new ArrayList<Map<String,ArrayList<Map<String,Object>>>>();
				Map<String,ArrayList<Map<String, Object>>> combineMap=new HashMap<String, ArrayList<Map<String,Object>>>();
				combineMap.put(datasetKey, datasetOne);
				combineList.add(combineMap);
				combineMap=new HashMap<String, ArrayList<Map<String,Object>>>();
				combineMap.put(datasetKey, datasetTwo);
				combineList.add(combineMap);
				dataMap.put(datasetKey, gson.toJson(combineList));
				if (MS_STACKED_COLUMN_DUAL_Y_AXIS.equalsIgnoreCase(chartType)) {
					if (MS_STACKED_COLUMN_DUAL_Y_AXIS.equalsIgnoreCase(chartType)) 
						createLineSet=true;
					ArrayList<Map<String, Object>> linesetObj =getStackedColumnJson(chartInfo,axisCats, table,isMultiSeries,createLineSet,null,hideTime==1);
					dataMap.put(linesetKey, gson.toJson(linesetObj));
				}
			}else if (STACKED_COLUMN_CHART.equalsIgnoreCase(chartType)){
				//Basic Stacked Column 2D chart
				dataMap.put(datasetKey, gson.toJson(datasetOne));
			}else{
				throw new ChartException("Unknown Chart Type for 'Stacked' Category. Please check and assign a valid chart type.");
			}
		}else if (PIE_CATEGORY.equalsIgnoreCase(chartCategory)) {
			if (PIE_2D_CHART.equalsIgnoreCase(chartType)) {
				ArrayList<Map<String, String>> dataArr = getColumn2DJson(chartInfo, table);
				if(dataArr!=null && dataArr.size()>chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(dataArr.size(),chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				dataMap.put(dataKey, gson.toJson(dataArr));
			}else if (PIE_MULTILEVEL.equalsIgnoreCase(chartType)){
				ArrayList<DimConfigInfo> fpList =fpi.m_frontInfoList;
				ArrayList<Map<String, Object>> dataArr = getMultilevelPieJson(chartInfo, table,fpList);
				if(dataArr!=null && dataArr.size()>chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(dataArr.size(),chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				dataMap.put(categoryKey, gson.toJson(dataArr));
			}else{
				throw new ChartException("Unknown Chart Type for 'PIE' Category. Please check and assign a valid chart type.");
			}
		} else if (REAL_TIME_CATEGORY.equalsIgnoreCase(chartCategory)) {
			
			if (REAL_TIME_LINE_CHART.equalsIgnoreCase(chartType)) {
				ArrayList<Map<String, String>> catArr = getRealTimeCategoryData(chartInfo, table);
				ArrayList<Map<String, String>> dataArr = getRealTimeData(chartInfo, table);
				if(dataArr!=null && dataArr.size()>chartInfo.getMaxXAxisPoints())
					ChartUtil.validateXAxisLength(dataArr.size(),chartInfo.getMaxXAxisPoints());
				dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
				
				Map<String,ArrayList<Map<String, String>>> catMap = new HashMap<String,ArrayList<Map<String, String>>>();
				Map<String,ArrayList<Map<String, String>>> valMap =  new HashMap<String,ArrayList<Map<String, String>>>();
				catMap.put(categoryKey,catArr);
				valMap.put(dataKey, dataArr);
				ArrayList a= new ArrayList();
				a.add(catMap);
				ArrayList b= new ArrayList();
				b.add(valMap);
				dataMap.put(categoriesKey,gson.toJson(a));
				dataMap.put(datasetKey, gson.toJson(b));
				
				//First Call will be normal, next onwards this block will return the values.
				if(Misc.getParamAsInt(session.getParameter("real_time"),Misc.UNDEF_VALUE) == 1){
					Map<String, String> value=dataArr.get(0);
					Map<String, String> label=catArr.get(0);
					return "&label="+label.get(labelKey)+"&value="+value.get(valueKey);
				}
				
			}else if (REAL_TIME_DUAL_Y_CHART.equalsIgnoreCase(chartType)){
				throw new ChartException("Not Supported REAL_TIME_DUAL_Y_CHART chart type.");
			}else{
				throw new ChartException("Unknown Chart Type for 'REAL_TIME' Category. Please check and assign a valid chart type.");
			}
		} else if ("msstackedcolumn2d".equalsIgnoreCase(chartType)) {
			//82452
//			return "{\"chart\":{\"caption\":\"Product-wisebreak-upofquarterlyrevenueinlastyear\",\"subcaption\":\"Harry'sSuperMart\",\"xaxisname\":\"Quarter\",\"yaxisname\":\"Sales(InUSD)\",\"paletteColors\":\"#0075c2,#45AFF5,#2C8A56,#1aaf5d,#50DE90\",\"numberPrefix\":\"$\",\"numbersuffix\":\"M\",\"bgColor\":\"#ffffff\",\"borderAlpha\":\"20\",\"showCanvasBorder\":\"0\",\"usePlotGradientColor\":\"0\",\"plotBorderAlpha\":\"10\",\"legendBorderAlpha\":\"0\",\"legendShadow\":\"0\",\"valueFontColor\":\"#ffffff\",\"showXAxisLine\":\"1\",\"xAxisLineColor\":\"#999999\",\"divlineColor\":\"#999999\",\"divLineDashed\":\"1\",\"showAlternateHGridColor\":\"0\",\"subcaptionFontBold\":\"0\",\"subcaptionFontSize\":\"14\"},\"categories\":[{\"category\":[{\"label\":\"Q1\"},{\"label\":\"Q2\"},{\"label\":\"Q3\"},{\"label\":\"Q4\"}]}],\"dataset\":[{\"dataset\":[{\"seriesname\":\"ProcessedFood\",\"data\":[{\"value\":\"30\"},{\"value\":\"26\"},{\"value\":\"29\"},{\"value\":\"31\"}]},{\"seriesname\":\"Un-ProcessedFood\",\"data\":[{\"value\":\"21\"},{\"value\":\"28\"},{\"value\":\"39\"},{\"value\":\"41\"}]}]},{\"dataset\":[{\"seriesname\":\"Electronics\",\"data\":[{\"value\":\"27\"},{\"value\":\"25\"},{\"value\":\"28\"},{\"value\":\"26\"}]},{\"seriesname\":\"Apparels\",\"data\":[{\"value\":\"17\"},{\"value\":\"15\"},{\"value\":\"18\"},{\"value\":\"16\"}]},{\"seriesname\":\"Others\",\"data\":[{\"value\":\"12\"},{\"value\":\"17\"},{\"value\":\"16\"},{\"value\":\"15\"}]}]}]}";
			return "{\"chart\":{\"caption\":\"Inventory Planning\",\"subcaption\":\"IntelliPlanner\",\"xaxisname\":\"Months\",\"yaxisname\":\"Quantity in Ton(s)\", \"paletteColors\":\"#0075c2,#45AFF5,#2C8A56,#1aaf5d,#50DE90\",\"numberPrefix\":\"\",\"numbersuffix\":\"\",\"bgColor\":\"#ffffff\",\"borderAlpha\":\"20\",\"showCanvasBorder\":\"0\",\"usePlotGradientColor\":\"0\",\"plotBorderAlpha\":\"10\",\"legendBorderAlpha\":\"0\",\"legendShadow\":\"0\",\"valueFontColor\":\"#ffffff\",\"showXAxisLine\":\"1\",\"xAxisLineColor\":\"#999999\",\"divlineColor\":\"#999999\",\"divLineDashed\":\"1\",\"showAlternateHGridColor\":\"0\",\"subcaptionFontBold\":\"0\",\"subcaptionFontSize\":\"14\"},\"categories\":[{\"category\":[{\"label\":\"Mar 2018\"}, {\"label\":\"Apr 2018\"}, {\"label\":\"May 2018\"} ]}],\"dataset\":[{\"dataset\":[{\"seriesname\":\"FSA(Plan)\",\"data\":[{\"value\":\"6000\"},{\"value\":\"7000\"},{\"value\":\"7000\"}]},{\"seriesname\":\"Evacuator(Plan)\",\"data\":[{\"value\":\"4000\"},{\"value\":\"4000\"},{\"value\":\"7000\"}]},{\"seriesname\":\"Import(Plan)\",\"data\":[{\"value\":\"10000\"},{\"value\":\"10000\"},{\"value\":\"10000\"}]},{\"seriesname\":\"Coal Block(Plan)\",\"data\":[{\"value\":\"4000\"},{\"value\":\"4000\"},{\"value\":\"4000\"}]}]},{\"dataset\":[{\"seriesname\":\"FSA(Procurement)\",\"data\":[{\"value\":\"5400\"},{\"value\":\"7800\"},{\"value\":\"1000\"}]},{\"seriesname\":\"Evacuator(Procurement)\",\"data\":[{\"value\":\"5000\"},{\"value\":\"3000\"},{\"value\":\"1000\"}]},{\"seriesname\":\"Import(Procurement)\",\"data\":[{\"value\":\"1000\"},{\"value\":\"0\"},{\"value\":\"0\"}]},{\"seriesname\":\"Coal Block(Procurement)\",\"data\":[{\"value\":\"4000\"},{\"value\":\"4000\"},{\"value\":\"4000\"}]}]},{\"dataset\":[{\"seriesname\":\"FSA(Logistics)\",\"data\":[{\"value\":\"5400\"},{\"value\":\"5600\"},{\"value\":\"0\"}]},{\"seriesname\":\"Evacuator(Logistics)\",\"data\":[{\"value\":\"5000\"},{\"value\":\"3200\"},{\"value\":\"1000\"}]},{\"seriesname\":\"Import(Logistics)\",\"data\":[{\"value\":\"10000\"},{\"value\":\"0\"},{\"value\":\"0\"}]},{\"seriesname\":\"Coal Block(Logistics)\",\"data\":[{\"value\":\"4000\"},{\"value\":\"4000\"},{\"value\":\"4000\"}]}]}]}";
		} 
		else if("msline".equalsIgnoreCase(chartType)) {
			
			ArrayList<ArrayList<DimConfigInfo>> fpList = fpi.m_frontSearchCriteria;
//			String topPageContext = searchBoxHelper.m_topPageContext;
//			for (ArrayList<DimConfigInfo> dciList : fpList) {
//			for (DimConfigInfo dci : dciList) {
				String tempVal = session.getAttribute("tr_chart_reports82452");
				if( Misc.getParamAsInt(tempVal)==1){
					return " {\"dataset\":\"[{\"seriesname\":\"Road Planned\",\"data\":[{\"value\":\"5000\"},{\"value\":\"7000\"},{\"value\":\"12000\"},{\"value\":\"18000\"},{\"value\":\"26000\"},{\"value\":\"32000\"},{\"value\":\"38000\"},{\"value\":\"44000\"},{\"value\":\"48000\"},{\"value\":\"52000\"},{\"value\":\"55000\"}]},{\"seriesname\":\"Road Target\",\"data\":[{\"value\":\"2000\"},{\"value\":\"4000\"},{\"value\":\"7000\"},{\"value\":\"9000\"},{\"value\":\"14000\"},{\"value\":\"16000\"},{\"value\":\"21000\"},{\"value\":\"28000\"},{\"value\":\"32000\"},{\"value\":\"38000\"},{\"value\":\"42000\"}]}]\",\"chart\":\"{\"numbersuffix\":\" (MT)\",\"subCaption\":\"Daywise\",\"toolTipBorderRadius\":\"2\",\"showLegend\":\"1\",\"enableSmartLabels\":\"0\",\"showBorder\":\"0\",\"showXAxisLine\":\"1\",\"useDataPlotColorForLabels\":\"1\",\"canvasBorderAlpha\":\"0\",\"bgColor\":\"#ffffff\",\"use3DLighting\":\"0\",\"legendBgColor\":\"#ffffff\",\"toolTipBgAlpha\":\"80\",\"paletteColors\":\"#1aaf5d,#f2c500,#f45b00,#0075c2,#8e0000,#5598c3,#2785c3,#31cc77,#1aaf5d,#f45b00\",\"usePlotGradientColor\":\"0\",\"toolTipBorderThickness\":\"0\",\"plotBorderAlpha\":\"10\",\"legendItemFontSize\":\"10\",\"numberPrefix\":\"\",\"toolTipBgColor\":\"#000000\",\"showHoverEffect\":\"1\",\"xAxisName\":\"Date\",\"legendShadow\":\"0\",\"captionFontSize\":\"14\",\"caption\":\"Commulative Inventory Plan\",\"showPercentValues\":\"1\",\"toolTipColor\":\"#ffffff\",\"exportEnabled\":\"1\",\"borderAlpha\":\"20\",\"startingAngle\":\"0\",\"decimals\":\"1\",\"subcaptionFontBold\":\"0\",\"legendBorderAlpha\":\"0\",\"showPercentInTooltip\":\"0\",\"legendItemFontColor\":\"#666666\",\"showShadow\":\"0\",\"rotatevalues\":\"0\",\"subcaptionFontSize\":\"14\",\"yAxisName\":\"Metric Ton(s)\",\"toolTipPadding\":\"5\"}\",\"categories\":\"[{\"category\":[{\"label\":\"2017-07-24\"},{\"label\":\"2017-08-09\"},{\"label\":\"2017-08-10\"},{\"label\":\"2017-08-11\"},{\"label\":\"2017-08-17\"},{\"label\":\"2017-08-18\"},{\"label\":\"2017-08-19\"},{\"label\":\"2017-08-20\"},{\"label\":\"2017-08-21\"},{\"label\":\"2017-08-22\"},{\"label\":\"2017-08-23\"}]}]\"}";
				}else
//			}
//			}
			return " {\"dataset\":\"[{\"seriesname\":\"Rail Planned\",\"data\":[{\"value\":\"9000\"},{\"value\":\"18000\"},{\"value\":\"28000\"},{\"value\":\"30000\"},{\"value\":\"34000\"},{\"value\":\"44000\"},{\"value\":\"46000\"},{\"value\":\"49000\"},{\"value\":\"50000\"},{\"value\":\"54000\"},{\"value\":\"58000\"}]},{\"seriesname\":\"Rail Target\",\"data\":[{\"value\":\"3000\"},{\"value\":\"6000\"},{\"value\":\"10000\"},{\"value\":\"13000\"},{\"value\":\"14000\"},{\"value\":\"21000\"},{\"value\":\"26000\"},{\"value\":\"35000\"},{\"value\":\"40000\"},{\"value\":\"44000\"},{\"value\":\"48000\"}]}]\",\"chart\":\"{\"numbersuffix\":\" (MT)\",\"subCaption\":\"Daywise\",\"toolTipBorderRadius\":\"2\",\"showLegend\":\"1\",\"enableSmartLabels\":\"0\",\"showBorder\":\"0\",\"showXAxisLine\":\"1\",\"useDataPlotColorForLabels\":\"1\",\"canvasBorderAlpha\":\"0\",\"bgColor\":\"#ffffff\",\"use3DLighting\":\"0\",\"legendBgColor\":\"#ffffff\",\"toolTipBgAlpha\":\"80\",\"paletteColors\":\"#1aaf5d,#f2c500,#f45b00,#0075c2,#8e0000,#5598c3,#2785c3,#31cc77,#1aaf5d,#f45b00\",\"usePlotGradientColor\":\"0\",\"toolTipBorderThickness\":\"0\",\"plotBorderAlpha\":\"10\",\"legendItemFontSize\":\"10\",\"numberPrefix\":\"\",\"toolTipBgColor\":\"#000000\",\"showHoverEffect\":\"1\",\"xAxisName\":\"Date\",\"legendShadow\":\"0\",\"captionFontSize\":\"14\",\"caption\":\"Commulative Inventory Plan\",\"showPercentValues\":\"1\",\"toolTipColor\":\"#ffffff\",\"exportEnabled\":\"1\",\"borderAlpha\":\"20\",\"startingAngle\":\"0\",\"decimals\":\"1\",\"subcaptionFontBold\":\"0\",\"legendBorderAlpha\":\"0\",\"showPercentInTooltip\":\"0\",\"legendItemFontColor\":\"#666666\",\"showShadow\":\"0\",\"rotatevalues\":\"0\",\"subcaptionFontSize\":\"14\",\"yAxisName\":\"Metric Ton(s)\",\"toolTipPadding\":\"5\"}\",\"categories\":\"[{\"category\":[{\"label\":\"2017-07-24\"},{\"label\":\"2017-08-09\"},{\"label\":\"2017-08-10\"},{\"label\":\"2017-08-11\"},{\"label\":\"2017-08-17\"},{\"label\":\"2017-08-18\"},{\"label\":\"2017-08-19\"},{\"label\":\"2017-08-20\"},{\"label\":\"2017-08-21\"},{\"label\":\"2017-08-22\"},{\"label\":\"2017-08-23\"}]}]\"}";
		}
			else {
			
			ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
			throw new ChartException("Unknown Chart Category. Please check and assign a valid chart type..");
		}
		System.out.println(gson.toJson(dataMap));
		return gson.toJson(dataMap);
	}
	
	private static ArrayList<Map<String, String>> getRealTimeData(ChartInfo chartInfo, Table table) throws ChartException {
		int valueIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryValue()));
		 if(valueIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'Value' DIM is not defined Properly. For "+chartInfo.getName());  
		}
		ArrayList<TR> body = table.getBody();
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();		
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> lv = new HashMap<String, String>();
			String val = tdArr.get(valueIndx).getContent();
//			if(totalKey.equalsIgnoreCase(val) || BLANK.equalsIgnoreCase(val) )
//				continue;
			lv.put(valueKey, (BLANK.equalsIgnoreCase(val)?"0":val));
			arrData.add(lv);
		}
		return arrData;
	}
	private static ArrayList<Map<String, String>> getRealTimeCategoryData(ChartInfo chartInfo, Table table) throws ChartException {
		int labelIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategorySeries()));
		int valueIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryValue()));
		if(labelIndx==Misc.UNDEF_VALUE && valueIndx==Misc.UNDEF_VALUE ){
			throw new ChartException("'Series and Value' DIM's are not defined Properly.For "+chartInfo.getName());   
		}else if(labelIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'Series' DIM is not defined Properly. For "+chartInfo.getName());  
		}else if(valueIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'Value' DIM is not defined Properly. For "+chartInfo.getName());  
		}
		ArrayList<TR> body = table.getBody();
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();		
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> lv = new HashMap<String, String>();
			String labelVal = tdArr.get(labelIndx).getContent();
			if(totalKey.equalsIgnoreCase(labelVal) || BLANK.equalsIgnoreCase(labelVal) )
				continue;
			lv.put(labelKey, labelVal);
			arrData.add(lv);
		}
		return arrData;
	}

	private static ArrayList<Map<String, String>> getMSLineJson(
			ChartInfo chartInfo, Table table) {
		// TODO Auto-generated method stub
		return null;
	}

	
private static ArrayList<Map<String, Object>> getMultilevelPieJson(ChartInfo chartInfo, Table table, ArrayList<DimConfigInfo> fpList) throws ChartException {
	int seriesIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategorySeries()));
	int valueIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryValue()));
	ChartUtil.checkDoRollupAttributeOnCategorySeries(fpList,Misc.getParamAsInt(chartInfo.getCategorySeries(),0));
	int subSeriesIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getSubCategorySeries()));
	StringTokenizer pletteColors=new StringTokenizer(chartInfo.getPaletteColors(),",");
	if(seriesIndx==Misc.UNDEF_VALUE && valueIndx==Misc.UNDEF_VALUE ){
		throw new ChartException("'Series and Value' DIM's are not defined Properly. For "+chartInfo.getName());  
	}else if(seriesIndx==Misc.UNDEF_VALUE){
		throw new ChartException("'Series' DIM is not defined Properly.For "+chartInfo.getName());  
	}else if(valueIndx==Misc.UNDEF_VALUE){
		throw new ChartException("'Value' DIM is not defined Properly.For "+chartInfo.getName());  
	}else if(subSeriesIndx==Misc.UNDEF_VALUE){
		throw new ChartException("'sub_category_series' DIM is not defined Properly.For "+chartInfo.getName());  
	}else if(pletteColors==null){
		throw new ChartException("'palettecolors' attribute has not defined in Chart Tag.For "+chartInfo.getName());  
	}
	ArrayList<TR> body = table.getBody();
	ArrayList<Map<String, Object>> arrData = new ArrayList<Map<String, Object>>();	
	Map<String, Object> topLv = new HashMap<String, Object>();
	topLv.put(labelKey, Misc.getParamAsString(chartInfo.getMlvlPIECoreCategoryLabel(),"Core Category"));
	topLv.put(colorKey, "#ffffff");
	topLv.put(numbersuffixKey, chartInfo.getNumbersuffix());
	ArrayList<Map<String, Object>> subCatList = new ArrayList<Map<String, Object>>();	
	Map<String, Object> subCatMap=null;
	ArrayList<Map<String, String>> rootCatList = null;
	int serCount=0;
	double topLvValue=0;
	String colorCode="";
	for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			String seriesVal = tdArr.get(seriesIndx).getContent();
			String subSeriesVal = tdArr.get(subSeriesIndx).getContent();
			String valueVal = tdArr.get(valueIndx).getContent();
			if (totalKey.equalsIgnoreCase(seriesVal)&& BLANK.equalsIgnoreCase(subSeriesVal)) {
				subCatMap.put(valueKey, valueVal);// total of subCategory
				subCatMap.put(categoryKey, rootCatList);
				topLv.put(numbersuffixKey, chartInfo.getNumbersuffix());
				subCatList.add(subCatMap);
				topLvValue = topLvValue + Misc.getParamAsDouble(valueVal, 0);
				serCount = 0;
			} else {
				if (subCatMap == null || serCount == 0) {
					subCatMap = new HashMap<String, Object>();
					colorCode = (String) pletteColors.nextElement();
					subCatMap.put(labelKey, tdArr.get(seriesIndx).getContent());
					subCatMap.put(colorKey, colorCode);
					topLv.put(numbersuffixKey, chartInfo.getNumbersuffix());
					rootCatList = new ArrayList<Map<String, String>>();
					serCount++;
				}
				Map<String, String> lv = new HashMap<String, String>();
				lv.put(labelKey, subSeriesVal);
				lv.put(valueKey, valueVal);
				lv.put(colorKey, colorCode);
				topLv.put(numbersuffixKey, chartInfo.getNumbersuffix());
				rootCatList.add(lv);
			}
		}
	topLv.put(valueKey, topLvValue);//total of all category
	topLv.put(categoryKey, subCatList);//list of subcategory
	arrData.add(topLv);
	return arrData;
}


/**
 * for creating Category/X Axis value for Stacked Chart
 * @param chartInfo
 * @param table
 * @return
 * @throws ChartException 
 */
	private static ArrayList<Map<String, ArrayList<Map<String, String>>>> getCategoriesDetails(ChartInfo chartInfo,Table table,boolean hideTime) throws ChartException {
		ArrayList<TR> body = table.getBody();
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, ArrayList<Map<String, String>>>> retVal=new ArrayList<Map<String, ArrayList<Map<String, String>>>>();
		int catIndex=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryAxis()));
		if(catIndex==Misc.UNDEF_VALUE)
			throw new ChartException("'category_axis' DIM has not defined Properly. For "+chartInfo.getName());  
		
		Map<String, String> chequeDuplicate = new HashMap<String, String>();
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> lv = new HashMap<String, String>();
			if(tdArr!=null){
				TD td = tdArr.get(catIndex);
				String colVal = td.getContent();
				if(totalKey.equalsIgnoreCase(colVal) || BLANK.equalsIgnoreCase(colVal) )
					throw new ChartException("remove 'do_rollup=1' from DIM's in XML. For "+chartInfo.getName());  
				
				//for removing time from date 
				if (hideTime) {
					try {
						Date date1 = null;
						if (colVal.contains("/"))
							date1 = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(colVal);
						else if (colVal.contains("-"))
							date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(colVal);
						colVal = ChartUtil.formatDate(date1, false, hideTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if(!chequeDuplicate.containsKey(colVal)){
					lv.put(labelKey,colVal);
					arrData.add(lv);
					chequeDuplicate.put(colVal,"");
				}
			}
		}
		if(arrData!=null && arrData.size()>chartInfo.getMaxXAxisPoints())
			ChartUtil.validateXAxisLength(arrData.size(),chartInfo.getMaxXAxisPoints());
		Map<String, ArrayList<Map<String, String>>> catMap=new HashMap<String, ArrayList<Map<String, String>>>();
		catMap.put(categoryKey, arrData);
		retVal.add(catMap);
		return retVal;
	}

	private static ArrayList<Map<String, ArrayList<Map<String, String>>>> getAvgLineJson(ChartInfo chartInfo, ArrayList<Map<String, String>> dataArr) {
		ArrayList<Map<String, ArrayList<Map<String, String>>>> arrData = new ArrayList<Map<String,ArrayList<Map<String, String>>>>();
		double avg = 0;
		double sum = 0;
		for (int i = 0; i < dataArr.size(); i++) {
			Map<String, String> m = (HashMap<String, String>) dataArr.get(i);
			sum += Misc.getParamAsDouble((String) m.get(valueKey), 0);
		}
		avg = sum / dataArr.size();
		Map<String, ArrayList<Map<String, String>>> topLineObj = new HashMap<String, ArrayList<Map<String, String>>>();
		Map<String, String> lineObj = new HashMap<String, String>();
		ArrayList<Map<String, String>>  lineArr = new ArrayList<Map<String, String>>();
		lineObj.put("startvalue", "" + avg);
		lineObj.put("color", "#1aaf5d");
		lineObj.put("displayvalue", Misc.getParamAsString(chartInfo.getAvgDisplayStr(), "Average"));
		lineObj.put("valueOnRight", "1");
		lineObj.put("thickness", "2");
		lineArr.add(lineObj);
		topLineObj.put("line", lineArr);
		arrData.add(topLineObj);
		return arrData;
	}
	public static ArrayList<Map<String, String>> getColumn2DJson(ChartInfo chartInfo, Table table) throws ChartException {
		int seriesIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategorySeries()));
		int valueIndx = table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryValue()));
		if(seriesIndx==Misc.UNDEF_VALUE && valueIndx==Misc.UNDEF_VALUE ){
			throw new ChartException("'Series and Value' DIM's are not defined Properly.For "+chartInfo.getName());  
		}else if(seriesIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'Series' DIM is not defined Properly. For "+chartInfo.getName());   
		}else if(valueIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'Value' DIM is not defined Properly. For "+chartInfo.getName());    
		}
		ArrayList<TR> body = table.getBody();
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();		
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> lv = new HashMap<String, String>();
			String seriesVal = tdArr.get(seriesIndx).getContent();
			String valueVal = tdArr.get(valueIndx).getContent();
			if(totalKey.equalsIgnoreCase(seriesVal) || BLANK.equalsIgnoreCase(seriesVal) )
				continue;
			lv.put(labelKey, seriesVal);
			lv.put(valueKey,Misc.getParamAsDouble(valueVal,0)+"");
			arrData.add(lv);
		}
		return arrData;
	}
	private static ArrayList<Map<String, Object>> getStackedColumnJson(ChartInfo chartInfo, 
			ArrayList<Map<String, ArrayList<Map<String, String>>>> axisCats, Table table, boolean isMultiSeries, boolean createLineSet, ArrayList<Map<String, Double>> lineuusetlist, boolean hideTime) throws ChartException {
		ArrayList<Map<String, Object>> retVal=new ArrayList<Map<String, Object>>();
		int axisIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryAxis()));
		int seriesIndx;
		int valueIndx;
		if(isMultiSeries && createLineSet){
			seriesIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getStackedDualYAxisSeries()));
			valueIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getStackedDualYAxisLine()));
		}else if(isMultiSeries){
			seriesIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryCompareWithSeries()));
			valueIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryCompareWithValue()));
		}else{
			seriesIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategorySeries()));
			valueIndx=table.getColumnIndexById(Misc.getParamAsInt(chartInfo.getCategoryValue()));
		}
		if(seriesIndx==Misc.UNDEF_VALUE && valueIndx==Misc.UNDEF_VALUE ){
			throw new ChartException("'category_series and category_value' DIM's are not defined Properly. For "+chartInfo.getName());  
		}else if(seriesIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'category_series' DIM is not defined Properly. For "+chartInfo.getName());  
		}else if(valueIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'category_value' DIM is not defined Properly. For "+chartInfo.getName());   
		}else if(axisIndx==Misc.UNDEF_VALUE){
			throw new ChartException("'category_axis' DIM is not defined Properly. For "+chartInfo.getName());   
		}
		
		ArrayList<String> seriesTempSeqList=getSeriesSeqList(table,seriesIndx,valueIndx);
		Map<String,Map<String,Double>> stackedMap=getStackedTempMap(axisCats,seriesTempSeqList);
		calculateSeriesValues(stackedMap,table,axisIndx,seriesIndx,valueIndx,hideTime);
		if(!(isMultiSeries && createLineSet)){
			
		//Now I have Series->Category->sum
		//set category values for every series in sequence
			
			boolean doCumulative=chartInfo.getDoCumulative()>0;
		for (int i = 0, listSize = seriesTempSeqList == null ? 0: seriesTempSeqList.size(); i < listSize; i++) {
			Map<String, Object> seriesMap = new HashMap<String, Object>();
			String serName = seriesTempSeqList.get(i);
			// add seriesname
			seriesMap.put(seriesnameKey, serName);
			double cumSum=0.0;
			ArrayList<Map<String, String>> dataArr = new ArrayList<Map<String, String>>();
			Map<String, Double> axisValueMap = stackedMap.get(serName);
			for (Map<String, ArrayList<Map<String, String>>> catMap : axisCats) {
				for (Entry<String, ArrayList<Map<String, String>>> entry : catMap.entrySet()) {
					if (entry.getKey() != null && entry.getKey().contains(categoryKey)) {
						ArrayList<Map<String, String>> cat = entry.getValue();
						for (Map<String, String> map : cat) {
							Map<String, String> valueMap = new HashMap<String, String>();
							
							String category = map.get(labelKey);
							String vl=axisValueMap.get(category).toString();
							
							if(totalKey.equalsIgnoreCase(vl) || BLANK.equalsIgnoreCase(vl) )
								throw new ChartException("remove 'do_rollup=1' from DIM's in XML. For "+chartInfo.getName());  
							if(doCumulative){
								cumSum=cumSum+Misc.getParamAsDouble(vl,0.0);
								valueMap.put(valueKey,cumSum+"");
							}else
							valueMap.put(valueKey, vl);
							// add value of category
							dataArr.add(valueMap);
						}
					}
				}
			}
			// add data to series
			seriesMap.put(dataKey, dataArr);
			retVal.add(seriesMap);
		}
		}else{
			Map<String, Object> seriesMap = new HashMap<String, Object>();
			// add seriesname
			seriesMap.put(seriesnameKey, chartInfo.getSYAxisName());
			seriesMap.put("showValues", "0");
			ArrayList<Map<String, Double>> linesetlist=new ArrayList<Map<String, Double>>();
			for (int i = 0, listSize = seriesTempSeqList == null ? 0: seriesTempSeqList.size(); i < listSize; i++) {
				String serName = seriesTempSeqList.get(i);
				ArrayList<Map<String, String>> dataArr = new ArrayList<Map<String, String>>();
				//cat->sum of vlue dim
				Map<String, Double> axisValueMap = stackedMap.get(serName);
				for (Map<String, ArrayList<Map<String, String>>> catMap : axisCats) {
					for (Entry<String, ArrayList<Map<String, String>>> entry : catMap.entrySet()) {
						if (entry.getKey() != null && entry.getKey().contains(categoryKey)) {
							ArrayList<Map<String, String>> cat = entry.getValue();
							int seq=0;
							for (Map<String, String> map : cat) {
								if (createLineSet) {
									if (cat.size() != linesetlist.size()) {
										Map<String, Double> valueMap = new HashMap<String, Double>();
										String category = map.get(labelKey);
										valueMap.put(valueKey, axisValueMap.get(category));
										// add value of category
										linesetlist.add(seq, valueMap);
										seq++;
									} else {
										Map<String, Double> vTemp = linesetlist.get(seq);
										String category = map.get(labelKey);
										Double sum = (vTemp.get(valueKey));
										vTemp.put(valueKey, (sum + Misc
												.getParamAsDouble(axisValueMap.get(category).toString(), 0)));
										linesetlist.set(seq, vTemp);
										// add value of category
										seq++;
										sum = 0.0;
									}
								} else {
									Map<String, String> valueMap = new HashMap<String, String>();
									String category = map.get(labelKey);
									valueMap.put(valueKey, axisValueMap.get(category).toString());
									// add value of category
									dataArr.add(valueMap);
								}
							}
						}
					}
				}
				// add data to series
				if (!createLineSet) {
					seriesMap.put(dataKey, dataArr);
					retVal.add(seriesMap);
				}
			}
			if (createLineSet){
				seriesMap.put(dataKey, linesetlist);
				retVal.add(seriesMap);
			}
		}
		return retVal;
	}
	private static void calculateSeriesValues(Map<String, Map<String, Double>> stackedMap, Table table,int axisIndx,int seriesIndx,int valueIndx, boolean hideTime) {
		ArrayList<TR> body = table.getBody();
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			if (tdArr != null) {
				String seriesVal = tdArr.get(seriesIndx).getContent();
				String axisVal = tdArr.get(axisIndx).getContent();
				String val = tdArr.get(valueIndx).getContent();
				//for removing time from date 
				if (hideTime) {
					try {
						Date date1 = null;
						if (axisVal.contains("/"))
							date1 = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(axisVal);
						else if (axisVal.contains("-"))
							date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(axisVal);
						axisVal = ChartUtil.formatDate(date1, false, hideTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
//					series->cat->sum
					Map<String, Double> catV = stackedMap.get(seriesVal);
					Double sum = (catV.get(axisVal) == null ? 0 : catV.get(axisVal));
					stackedMap.get(seriesVal).put(axisVal, (sum + Misc.getParamAsDouble(val, 0)));
					catV = null;
			}
		}
		System.out.println(stackedMap.toString());
	}
	
	private static Map<String, Map<String, Double>> getStackedTempMap(ArrayList<Map<String, ArrayList<Map<String, String>>>> axisCats,
			ArrayList<String> seriesTempSeqList) {
		Map<String, Map<String, Double>> catMapValue = new HashMap<String, Map<String, Double>>();
		for (int i = 0; i < seriesTempSeqList.size(); i++) {
			Map<String,Double> catSeriesMap = new HashMap<String,Double>();
			for (Map<String, ArrayList<Map<String, String>>> catMap : axisCats) {
				for (Entry<String, ArrayList<Map<String, String>>> entry : catMap.entrySet()) {
					if (entry.getKey() != null && entry.getKey().contains(categoryKey)) {
						ArrayList<Map<String, String>> cat = entry.getValue();
						for (Map<String, String> map : cat) {
							// assign Category with default value 0
							catSeriesMap.put(map.get(labelKey), 0.0);
						}
					}
				}
			}
			// assign categoryList to Every series
			//Series->Category->Value
			catMapValue.put(seriesTempSeqList.get(i), catSeriesMap);
		}
		return catMapValue;
	}

	private static ArrayList<String> getSeriesSeqList(Table table,int seriesIndx,int valueIndx) {
		ArrayList<TR> body = table.getBody();
		Map<String, Integer> serMap = new HashMap<String, Integer>();
		int seq=0;
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			if (tdArr != null) {
				String seriesVal = tdArr.get(seriesIndx).getContent();
				if (!serMap.containsKey(seriesVal)) {
					serMap.put(seriesVal, seq);
					seq++;
				}
			}
		}
		ArrayList<String> al=new ArrayList<String>((seq>10?seq:10));
		for (int i = 0; i < seq; i++) 
			al.add(i,"");
		for (Entry<String, Integer> entry : serMap.entrySet())
			al.set(entry.getValue(), entry.getKey());
		return al;
	}
	
	
}
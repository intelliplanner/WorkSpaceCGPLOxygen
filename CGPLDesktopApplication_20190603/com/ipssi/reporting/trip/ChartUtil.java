package com.ipssi.reporting.trip;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.jsp.JspWriter;

import com.google.gson.Gson;
import com.ipssi.dispatchoptimization.PitStatsDTO;
import com.ipssi.dispatchoptimization.ShovelStatsDTO;
import com.ipssi.gen.utils.ChartInfo;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.orient.jason.reader.OrientUtility;

public class ChartUtil {
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
	private static final String MSLINE_CHART="msline";//Multi Series Line Chart
	private static final String SCROLL_LINE_CHART="scrollline2d";//scroll in line Chart
	private static final String BAR2D_CHART="bar2d";
	private static final String KAGI_CHART="kagi";
	private static final String REAL_TIME_CATEGORY="realtime";
	private static final String REAL_TIME_LINE_CHART="realtimeline";
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
	public static ArrayList<Map<String, String>> getShovelMap(
			ChartInfo chartInfo, ShovelStatsDTO dto) {
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();	
		Map<String, String> m=new HashMap<String, String>();
		m.put(labelKey, "Tonnage Per Hour");
		m.put(valueKey,dto.getTonnagePerHour()+"");
		arrData.add(m);
		Map<String, String> m1=new HashMap<String, String>();
		m1.put(labelKey, "Number Of Cycle Per Trip");
		m1.put(valueKey,dto.getAvgNumOfCyclePerTrip()+"");
		arrData.add(m1);
		Map<String, String> m2=new HashMap<String, String>();
		m2.put(labelKey, "Avg Cycle Duration");
		m2.put(valueKey,dto.getAvgCycleTime()+"");
		arrData.add(m2);
		Map<String, String> m3=new HashMap<String, String>();
		m3.put(labelKey, "Idle Time %");
		m3.put(valueKey,dto.getAvgShovelIdlePercentage()+"");
		arrData.add(m3);
		Map<String, String> m4=new HashMap<String, String>();
		m4.put(labelKey, "Avg Dumper Wait time %");
		m4.put(valueKey,dto.getAvgDumperWaitTime()+"");
		arrData.add(m4);
		Map<String, String> m5=new HashMap<String, String>();
		m5.put(labelKey, "Avg Cleaning time %");
		m5.put(valueKey,dto.getAvgCleaningPercentage()+"");
		arrData.add(m5);
		return arrData;
	}
	 
	 public static ArrayList<Map<String, String>> getPitMap(
				ChartInfo chartInfo, PitStatsDTO dto) {
			ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();	
			Map<String, String> m=new HashMap<String, String>();
			m.put(labelKey, "Number Of Shovels");
			m.put(valueKey,dto.getNumOfShovels()+"");
			arrData.add(m);
			Map<String, String> m1=new HashMap<String, String>();
			m1.put(labelKey, "Number Of Dumpers");
			m1.put(valueKey,dto.getNumOfDumpers()+"");
			arrData.add(m1);
			Map<String, String> m2=new HashMap<String, String>();
			m2.put(labelKey, "Avg Tonnage Dispatched");
			m2.put(valueKey,dto.getAvgTonnageDispatched()+"");
			arrData.add(m2);
			Map<String, String> m3=new HashMap<String, String>();
			m3.put(labelKey, "Avg Wait Time For Dumpers");
			m3.put(valueKey,dto.getAvgWaitTimeForDumpers()+"");
			arrData.add(m3);
			Map<String, String> m4=new HashMap<String, String>();
			m4.put(labelKey, "Avg Idle Time Of Shovel");
			m4.put(valueKey,dto.getAvgIdleTimeOfShovel()+"");
			arrData.add(m4);
			Map<String, String> m5=new HashMap<String, String>();
			m5.put(labelKey, "Avg Cycle Time");
			m5.put(valueKey,dto.getAvgCycleTime()+"");
			arrData.add(m5);
			Map<String, String> m6=new HashMap<String, String>();
			m6.put(labelKey, "Avg Cycle Per Trip");
			m6.put(valueKey,dto.getAvgCyclePerTrip()+"");
			arrData.add(m6);
			Map<String, String> m7=new HashMap<String, String>();
			m7.put(labelKey, "Avg Lead");
			m7.put(valueKey,dto.getAvgLead()+"");
			arrData.add(m7);
			return arrData;
		}
	 
	public static ArrayList<Integer> getIntegerListFromStringArray(String[] split) {
		ArrayList<Integer> al=new ArrayList<Integer>();
		for (String str : split) {
			Integer i=new Integer(str);
			if(i!=-1000 && i!=-1111112 && i!=-1111113 && !Misc.isUndef(i))
			al.add(new Integer(str));
		}
		return al;
	}
	

	public static void validateXAxisLength(int dataSourceLength,int mentionedXMLLength) throws ChartException{
		if(mentionedXMLLength>0 && dataSourceLength>mentionedXMLLength)
			throw new ChartException("Kindly reduce your search criteria or customize 'Max X Axis Points' and 'Width' of the chart. Too many records ("+dataSourceLength+")");
	}

	public static void checkDoRollupAttributeOnCategorySeries(ArrayList<DimConfigInfo> fpList,int paramAsInt) throws ChartException {
	DimConfigInfo d=getDimConfigInfoFromFrontList(fpList,paramAsInt);
	if(!d.m_doRollupTotal)
		throw new ChartException("'category_series' DIM has not defined do_rollup=1 attribute.");  
}
	
	
	
	public static Map<String, String> getShovelStats(Gson gson,ChartInfo chartInfo,FrontPageInfo fpi, Table table, SessionManager session,SearchBoxHelper searchBoxHelper, JspWriter out,int portNodeId) throws ChartException, IOException {
		Map<String,Map<String,Map<String,String>>> resultSet=getShovelDataFromTable(chartInfo,table,fpi,searchBoxHelper,session);
		ArrayList<Integer> params = null;
		ArrayList<Integer> shovelIds = null;
		String topPageContext = searchBoxHelper.m_topPageContext;
		ArrayList al = searchBoxHelper.m_searchParams;
		for (int i = 0, is = al == null ? 0 : al.size(); i < is; i++) {
			ArrayList row = (ArrayList) al.get(i);
			for (int j = 0, js = row == null ? 0 : row.size(); j < js; j++) {
				DimConfigInfo dc = (DimConfigInfo) row.get(j);
				System.out.println(dc.m_dimCalc.m_dimInfo.m_id);
				int p_id = dc.m_dimCalc.m_dimInfo.m_id;
				String tempVarName = topPageContext + p_id;
				String tempVal = session.getAttribute(tempVarName);
				System.out.println(tempVarName + "=" + tempVal);
				switch (p_id) {
				case 82545: {//List Of Shovels
					if(tempVal!=null)
					shovelIds = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}
				case 82593: {//including vartual Shovels
					if(tempVal!=null)
					shovelIds = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}
				case 82548: {//List Of Dumpers
					if(tempVal!=null)
						shovelIds = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
				}
				case 82511: {//List of Pits 
					if(tempVal!=null)
						shovelIds = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}
				case 82510: {//Dumpers Current Params
					if(tempVal!=null)
					params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}
				case 82508: {//Shovel Current Params
					if(tempVal!=null)
						params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
						break; 
					}
				case 82509: {//Pits Current Parameters
					if(tempVal!=null)
						params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
						break; 
					}
				case 82516: {//Shovel Shift Params
					if(tempVal!=null)
					params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}
				case 82517: {//Pits Shift Params
					if(tempVal!=null)
						params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
						break; 
					}
				case 82518: {//Dumper Shift Parameters
					if(tempVal!=null)
						params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
						break; 
					}
				default:
				}
			}
		}
//Now I have Searh Criteria, resultSet from table
//create chartInfo and render from resultSet
			// ChartInfo chartInfo = chartInfo;
		if (chartInfo == null)
			throw new ChartException("Chart Tag Not Found in XML .");
		if (shovelIds == null)
			throw new ChartException("Please Select Vehicle/Shovel to show chart report.");

		
		ChartInfo ci=null;
		int dataPlotCounter=0;
		for (int i = 0; i < shovelIds.size(); i++) {
			int shovel_id=shovelIds.get(i);
			
			String dataSrc = null;
			if ("shovel_stats_db_rt".contains(chartInfo.getChartCategory())) {

				for (int j = 0; j < params.size(); j++) {
					int param_id = params.get(j);
					try {
						ci = (ChartInfo) chartInfo.clone();
					} catch (CloneNotSupportedException e) {
					}
					configureChartVariables(ci, params, i, j, shovel_id);
					dataSrc = getShovelDashboardJason(gson, ci, resultSet.get(shovel_id + ""), shovel_id, param_id,portNodeId);
				
					ci.setDataSource(dataSrc);
					FusionCharts fChart = new FusionCharts(ci);
					out.print(fChart.render());
					ci = null;
				}
				
			} else {
				
				try {
					ci = (ChartInfo) chartInfo.clone();
				} catch (CloneNotSupportedException e) {
				}
				ci.setCaption(ShoveNameMap.get(shovel_id+""));
				ci.setId("chart" + i+dataPlotCounter);
				ci.setRenderAt("chart_" + i+"_"+dataPlotCounter);
				dataSrc = getShovelJason(gson, ci, resultSet.get(shovel_id + ""), shovel_id, params);
				ci.setDataSource(dataSrc);
				FusionCharts fChart = new FusionCharts(ci);
				out.print(fChart.render());
				dataPlotCounter=(dataPlotCounter==3?0:dataPlotCounter);
			}
				
			
		}
		//returnig object to prevent Data not found message of null
		return null;
	}
	static void configureChartVariables(ChartInfo ci,ArrayList<Integer> params, int i, int j, int shovel_id) {
		ci.setYAxisMinValue(ci.getAttributeByName("min_" + params.get(j)));
		ci.setYAxisMaxValue(ci.getAttributeByName("max_" + params.get(j)));
		if (i == 0) {
			ci.setRotateYAxisName("1");
			ci.setShowYAxisValues("1");
			ci.setYAxisName(p_name.get(params.get(j)+""));
			ci.setWidth("325"); 
		} else {
			ci.setShowYAxisValues("0");
			ci.setYAxisName("");
		}
		if (j == 0) {
			ci.setCaption(ShoveNameMap.get(shovel_id + ""));
			ci.setShowLabels("0");
			ci.setShowLegend("0");
			
		} else {
			ci.setCaption("");
		}
		if (j == (params.size()-1)){
			ci.setShowXAxisLine("1");
			ci.setShowLabels("1");
			ci.setXAxisName("Date & Time");
			ci.setHeight("250"); 
		}else
			ci.setShowXAxisLine("0");

		ci.setId("chart" + i + j);
		ci.setRenderAt("chart_" + i + "_" + j);
	}
	private static String getShovelDashboardJason(Gson gson, ChartInfo ci,Map<String, Map<String, String>> timeMap, int shovel_id,Integer param_id,int portNodeId) {
		if (timeMap == null)
			return "";
		Map<String, String> dataMap = new HashMap<String, String>();
		ArrayList<Map<String, String>> timeData = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, ArrayList<Map<String, String>>>> categoriesList = new ArrayList<Map<String, ArrayList<Map<String, String>>>>();
		for (Entry<String, Map<String, String>> entry : timeMap.entrySet()) {
			String time = entry.getKey();
			Map<String, String> lv = new HashMap<String, String>();
			lv.put(labelKey, time);
			timeData.add(lv);
		}
		Map<String, ArrayList<Map<String, String>>> catMap = new HashMap<String, ArrayList<Map<String, String>>>();
		catMap.put(categoryKey, timeData);
		categoriesList.add(catMap);// All Categories Set
		// for Param NAme/series Name
		ArrayList<Map<String, Object>> dataSetList = getSingleParameterData(timeMap,timeData, shovel_id, param_id);
		dataMap.put(categoriesKey, gson.toJson(categoriesList));
		dataMap.put(datasetKey, gson.toJson(dataSetList));
		//add vehicle_id and param id for Ajax call
		
		Map<String, String> chartConfig=ChartUtil.getBasicChartDetails(ci);
		StringBuilder uri = new StringBuilder(chartConfig.get("datastreamurl"));
		chartConfig.remove("datastreamurl");
		uri=uri.append(shovel_id).append("&c_vehicle_rt=").append(shovel_id).append("&c_param_rt=").append(param_id).append("&pv123=").append(portNodeId);
		chartConfig.put("datastreamurl" ,uri.toString());  
		dataMap.put(chartKey, gson.toJson(chartConfig));
		//System.out.println("Shovel Json=> " + gson.toJson(dataMap));
		return gson.toJson(dataMap);
	}
	
	public static String getRealTimeData(Map<String, Map<String, String>> timeMap,Integer param_id){
		String retVal="";
		if(timeMap==null)
			return retVal;
		ArrayList<Map<String, String>> timeData = new ArrayList<Map<String, String>>();
		for (Entry<String, Map<String, String>> entry : timeMap.entrySet()) {
			String time = entry.getKey();
			Map<String, String> lv = new HashMap<String, String>();
			lv.put(labelKey, time);
			timeData.add(lv);
		}
		for (Iterator iterator = timeData.iterator(); iterator.hasNext();) {
			Map<String, String> timeM = (Map<String, String>) iterator.next();
			Map<String, String> p_data = timeMap.get(timeM.get(labelKey));
			retVal= "&label="+timeM.get(labelKey)+"&value="+Misc.getParamAsDouble(p_data.get(param_id + "")==null?"0.0":p_data.get(param_id + "").replaceAll(",", ""));
		}
		return retVal;
	}
	
	private static ArrayList<Map<String, Object>> getSingleParameterData(Map<String, Map<String, String>> timeMap,ArrayList<Map<String, String>> timeArr, int shovel_id,Integer param_id) {
		ArrayList<Map<String, Object>> retList =new ArrayList<Map<String,Object>>();
//		seriesMap
			Map<String, Object> seriesMap = new HashMap<String, Object>();
			seriesMap.put(seriesnameKey,p_name.get(param_id+""));
			ArrayList<Map<String, String>> timeParamArr=new ArrayList<Map<String,String>>();
			for (Iterator iterator = timeArr.iterator(); iterator.hasNext();) {
				Map<String, String> timeM = (Map<String, String>) iterator.next();
				Map<String, String> p_data = timeMap.get(timeM.get(labelKey));
				Map<String, String> pData =new HashMap<String, String>();
				pData.put(valueKey, p_data.get(param_id + ""));
				timeParamArr.add(pData);
			}
			seriesMap.put(dataKey,timeParamArr);
			retList.add(seriesMap);
		return retList;
	}
	static String getShovelJason(Gson gson,ChartInfo chartInfo,Map<String, Map<String, String>> timeMap,int shovelId, ArrayList<Integer> params) {
		if (timeMap == null)
			return "";
		Map<String, String> dataMap = new HashMap<String, String>();
		ArrayList<Map<String, String>> timeData = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, ArrayList<Map<String, String>>>> categoriesList = new ArrayList<Map<String, ArrayList<Map<String, String>>>>();
		for (Entry<String, Map<String, String>> entry : timeMap.entrySet()) {
			String time = entry.getKey();
			Map<String, String> lv = new HashMap<String, String>();
			lv.put(labelKey, time);
			timeData.add(lv);
		}
		Map<String, ArrayList<Map<String, String>>> catMap = new HashMap<String, ArrayList<Map<String, String>>>();
		catMap.put(categoryKey, timeData);
		categoriesList.add(catMap);// All Categories Set
		// for Param NAme/series Name
		ArrayList<Map<String, Object>> dataSetList = getParameterData(timeMap,timeData, shovelId, params);
		dataMap.put(categoriesKey, gson.toJson(categoriesList));
		dataMap.put(datasetKey, gson.toJson(dataSetList));
		dataMap.put(chartKey, gson.toJson(ChartUtil.getBasicChartDetails(chartInfo)));
		System.out.println("Shovel Json=> " + gson.toJson(dataMap));
		return gson.toJson(dataMap);
	}
	
	private static ArrayList<Map<String, Object>> getParameterData(Map<String, Map<String, String>> timeMap,ArrayList<Map<String, String>> timeArr, int shovelId,	ArrayList<Integer> params) {
		ArrayList<Map<String, Object>> retList =new ArrayList<Map<String,Object>>();
//		seriesMap
		for (int i = 0; i < params.size(); i++) {
			Map<String, Object> seriesMap = new HashMap<String, Object>();
			seriesMap.put(seriesnameKey,p_name.get(params.get(i)+""));
			ArrayList<Map<String, String>> timeParamArr=new ArrayList<Map<String,String>>();
			for (Iterator iterator = timeArr.iterator(); iterator.hasNext();) {
				Map<String, String> timeM = (Map<String, String>) iterator.next();
				Map<String, String> p_data = timeMap.get(timeM.get(labelKey));
				Map<String, String> pData =new HashMap<String, String>();
				pData.put(valueKey, p_data.get(params.get(i) + ""));
				timeParamArr.add(pData);
			}
			seriesMap.put(dataKey,timeParamArr);
			retList.add(seriesMap);
		}
		return retList;
	}
	static Map<String, Map<String, Map<String, String>>> getShovelDataFromTable(ChartInfo chartInfo, Table table, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper,SessionManager session) {
		ArrayList<TR> body = table.getBody();
		
		Map<String, Map<String, Map<String, String>>> shovelData=(Map<String, Map<String, Map<String, String>>>) session.getAttributeObj("_shovel_chart_data");
//		if (shovelData==null)
//		shovelData=(Map<String, Map<String, Map<String, String>>>) session.request.getSession().getAttribute("_shovel_chart_data");
	 if(shovelData==null)
		 shovelData=new HashMap<String, Map<String,Map<String,String>>>();
	 int showDateAsTimeOnly=Misc.getParamAsInt(chartInfo.getAttributeByName("show_date_as_time"));
		Map<String,Integer> totalParamsInFrontPage=getAllParamIdMap(fpi.m_frontInfoList,true);
		ArrayList<Integer> allParams=getAllParamId(fpi.m_frontInfoList,false);
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> lv = new HashMap<String, String>();
			Map<String, Map<String, String>> timeData=null;
			Map<String, String> paramData=new HashMap<String,String>();
			for (Integer pId : allParams) {
				int dimId=totalParamsInFrontPage.get(pId+"");
				if (tdArr != null) {
					String colVal = tdArr.get(table.getColumnIndexById(dimId)).getContent();
					paramData.put(pId+"", colVal==BLANK?"0.0":colVal.replaceAll(",", ""));
						
				}
			}
			//TO-DO need to remove below HardCode Time and shovelid DIM
			String shovel_id="";
			if(Misc.getUndefInt()!=table.getColumnIndexById(20274))
				shovel_id=tdArr.get(table.getColumnIndexById(20274)).getContent();//vehicle id
			else if(Misc.getUndefInt()!=table.getColumnIndexById(82547))
				shovel_id=tdArr.get(table.getColumnIndexById(82547)).getContent();//pit id
			String name="";//shovel/dumper/pit name
			if(Misc.getUndefInt()!=table.getColumnIndexById(9002))
				name=tdArr.get(table.getColumnIndexById(9002)).getContent();
			else
				name=tdArr.get(table.getColumnIndexById(82012)).getContent();
			//Hack for vitual shovel[-negative pile id]
			if(!Misc.isUndef(Misc.getParamAsInt(shovel_id)) && Misc.getParamAsInt(shovel_id)<=0 ){
				if(!ShoveNameMap.containsKey(shovel_id)){
					vitualShovelLoaded=false;
					loadVirtualShovels(session);
				}
			}else{
				ShoveNameMap.put(shovel_id,name);
			}
			String time=null;
			if(Misc.getUndefInt()!=table.getColumnIndexById(82530))//Shovel Curr Time
				time=tdArr.get(table.getColumnIndexById(82530)).getContent();
			else if (Misc.getUndefInt()!=table.getColumnIndexById(83110))//shovel Shift
				time=tdArr.get(table.getColumnIndexById(83110)).getContent();
			else if (Misc.getUndefInt()!=table.getColumnIndexById(82550))//dumper current Time
				time=tdArr.get(table.getColumnIndexById(82550)).getContent();
			else if (Misc.getUndefInt()!=table.getColumnIndexById(83120))//dumper shift
				time=tdArr.get(table.getColumnIndexById(83120)).getContent();
			else if(Misc.getUndefInt()!=table.getColumnIndexById(82537)) //Pit curr
				time=tdArr.get(table.getColumnIndexById(82537)).getContent();
			else if(Misc.getUndefInt()!=table.getColumnIndexById(83100)) //Pit Shift
				time=tdArr.get(table.getColumnIndexById(83100)).getContent();
			
			if(time==BLANK)
				time=formatDate(new Date(),showDateAsTimeOnly==1,false);
			else{
				try {
					Date date1=null;
					if(time.contains("/"))
					 date1=new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(time);
					else if(time.contains("-"))
						 date1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
					time=formatDate(date1,showDateAsTimeOnly==1,false);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
				
			if(shovelData.containsKey(shovel_id)){
				timeData=shovelData.get(shovel_id);
				if(!timeData.containsKey(time)){
					timeData.put(time, paramData);
					shovelData.put(shovel_id, timeData);
				}
			}else {
				timeData=new TreeMap<String,Map<String,String>>();
				timeData.put(time, paramData);
				shovelData.put(shovel_id, timeData);
			}
		}
		session.setAttributeObj("_shovel_chart_data",shovelData);
		return shovelData;
	}
	
	public static String formatDate(Date date ,boolean showTimeOnly,boolean hideTime){
		SimpleDateFormat outgoing = null;
		if(showTimeOnly)
			outgoing = new SimpleDateFormat("HH:mm:ss");
		else if(hideTime)
			outgoing = new SimpleDateFormat("yyyy-MM-dd");
		else 
			outgoing =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return outgoing.format(date);
	}
	
	public static Map<String, Integer> getAllParamIdMap(ArrayList fpList,	boolean b) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < fpList.size(); i++) {
			DimConfigInfo d=(DimConfigInfo) fpList.get(i);
			map.put(d.m_lookHelp1,d.m_dimCalc.m_dimInfo.m_id);
		}
		return map;
	}
	public static ArrayList<Integer> getAllParamId(ArrayList<DimConfigInfo> fpList,boolean onlyParam) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		if(onlyParam)
		for (int i = 0; i < 100; i++) {
			list.add(new Integer(0));
		}
		for (int i = 0; i < fpList.size(); i++) {
			DimConfigInfo d=(DimConfigInfo) fpList.get(i);
			if(onlyParam)
				list.add(Misc.getParamAsInt(d.m_lookHelp1),d.m_dimCalc.m_dimInfo.m_id);
			else
				list.add(Misc.getParamAsInt(d.m_lookHelp1));
		}
		return list;
	}
	public static Map<String, String> getBasicChartDetails(ChartInfo chartInfo) {
		Map<String, String> srcObj = new HashMap<String, String>();
		String chartType=chartInfo.getType();
		
        	
		srcObj.put("xAxisName", chartInfo.getXAxisName());
		srcObj.put("exportEnabled", chartInfo.getExportEnabled());
		//srcObj.put("showXAxisLine", "1");
		//caption setting
		srcObj.put("caption", chartInfo.getCaption());
		srcObj.put("subCaption", chartInfo.getSubCaption());
		srcObj.put("captionFontSize",  chartInfo.getCaptionFontSize());
		srcObj.put("subcaptionFontSize",  chartInfo.getAttributeByName("subcaptionfontsize"));
//		if ("msstepline".equalsIgnoreCase(chartType)) {
//			srcObj.put("theme", chartInfo.getAttributeByName("theme"));
//			srcObj.put("useforwardsteps","1");
//			return srcObj;
//		}
		srcObj.put("subcaptionFontBold", "0");
		srcObj.put("numberPrefix", chartInfo.getNumberPrefix());
		srcObj.put("numbersuffix", chartInfo.getNumbersuffix());
		srcObj.put("showXAxisLine", chartInfo.getShowXAxisLine()); 
		srcObj.put("showLabels", chartInfo.getShowLabels()); 
		srcObj.put("showLegend", chartInfo.getShowLegend()); 
	    srcObj.put("showYAxisValues", chartInfo.getShowYAxisValues()); 
		srcObj.put("yAxisMinValue", chartInfo.getYAxisMinValue()); 
		srcObj.put("yAxisMaxValue", chartInfo.getYAxisMaxValue()); 
		srcObj.put("rotateYAxisName", chartInfo.getRotateYAxisName()); 
		
		srcObj.put("paletteColors",chartInfo.getPaletteColors());
		srcObj.put("bgColor", "#ffffff");
		srcObj.put("showBorder", chartInfo.getShowBorder());
		srcObj.put("use3DLighting", "0");
		srcObj.put("showShadow", "0");
		srcObj.put("enableSmartLabels", "0");
		srcObj.put("startingAngle", "0");
		srcObj.put("showPercentValues",chartInfo.getAttributeByName("showPercentValues"));
		srcObj.put("showPercentInTooltip", chartInfo.getAttributeByName("showPercentInTooltip"));
		srcObj.put("decimals",chartInfo.getAttributeByName("decimals"));
		srcObj.put("showToolTip",chartInfo.getAttributeByName("showToolTip"));
		srcObj.put("showValues" ,chartInfo.getAttributeByName("showValues")); 
		srcObj.put("theme", chartInfo.getAttributeByName("theme"));
		srcObj.put("drawAnchors", chartInfo.getAttributeByName("drawAnchors"));
		
		//tool tips setting
		srcObj.put("toolTipColor", "#ffffff");
		srcObj.put("toolTipBorderThickness", "0");
		srcObj.put("toolTipBgColor", "#000000");
		srcObj.put("toolTipBgAlpha", "80");
		srcObj.put("toolTipBorderRadius", "2");
		srcObj.put("toolTipPadding", "5");
		srcObj.put("showHoverEffect", "1");
		//legend settings
		srcObj.put("legendBgColor", "#ffffff");
		srcObj.put("legendBorderAlpha", "0");
		srcObj.put("legendShadow", "0");
		srcObj.put("legendItemFontSize", "10");
		srcObj.put("legendItemFontColor", "#666666");
		srcObj.put("useDataPlotColorForLabels", "1");
		srcObj.put("rotatevalues", "0");
		srcObj.put("borderAlpha", "20");
		srcObj.put("canvasBorderAlpha", "0");
		srcObj.put("usePlotGradientColor", "0");
		srcObj.put("plotBorderAlpha", "10");
		srcObj.put("formatNumberScale" ,("".equalsIgnoreCase(chartInfo.getAttributeByName("formatNumberScale"))?"0":chartInfo.getAttributeByName("formatNumberScale")));  
		
		if (KAGI_CHART.equalsIgnoreCase(chartType)) {
			srcObj.put("reversalPercentage",chartInfo.getAttributeByName("reversalPercentage"));//5
			srcObj.put("rallythickness",chartInfo.getAttributeByName("rallythickness"));//3
			srcObj.put("declinethickness",chartInfo.getAttributeByName("declinethickness"));//3
		}else if (BAR2D_CHART.equalsIgnoreCase(chartType)) {
			srcObj.put("xAxisName", chartInfo.getXAxisName());
			srcObj.put("yAxisName", chartInfo.getYAxisName());
			srcObj.put("caption", chartInfo.getCaption());
			srcObj.put("subCaption", chartInfo.getSubCaption());
			//srcObj.put("theme", "fusion");
			srcObj.put("bgcolor" ,"000000");  
			srcObj.put("bgalpha" ,"100");  srcObj.put("canvasborderthickness" ,"1");  
			srcObj.put("canvasbordercolor" ,"008040");  srcObj.put("canvasbgcolor" ,"000000");  
			srcObj.put("decimals" ,chartInfo.getAttributeByName("decimals"));  
			srcObj.put("numvdivlines" ,chartInfo.getAttributeByName("numvdivlines"));  
			srcObj.put("divlinecolor" ,"008040");  
			srcObj.put("vdivlinecolor" ,"008040");  srcObj.put("divlinealpha" ,"100");  
			srcObj.put("chartleftmargin" ,"10");  srcObj.put("basefontcolor" ,"00dd00");  
			srcObj.put("showrealtimevalue" ,"0");  
			srcObj.put("showValues" ,chartInfo.getAttributeByName("showValues"));  
			srcObj.put("numbersuffix" ,chartInfo.getAttributeByName("numbersuffix"));  
			srcObj.put("labeldisplay" ,chartInfo.getAttributeByName("labeldisplay")); 
			srcObj.put("slantlabels" ,chartInfo.getAttributeByName("slantlabels"));  
			srcObj.put("tooltipbgcolor" ,"000000");  srcObj.put("tooltipbordercolor" ,"008040");  
			srcObj.put("basefontsize" ,"11");  srcObj.put("showalternatehgridcolor" ,"0");  
			srcObj.put("legendbgcolor" ,"000000");  srcObj.put("legendbordercolor" ,"008040");  
			srcObj.put("legendpadding" ,"35");  srcObj.put("showlabels" ,chartInfo.getShowLabels());  
			srcObj.put("showborder" ,chartInfo.getShowBorder());
			return srcObj;
		}else if (LINE_CHART.equalsIgnoreCase(chartType)) {
			srcObj.put("lineThickness", "2");
			srcObj.put("baseFontColor", "#333333");
			srcObj.put("baseFont", "Helvetica Neue,Arial");
			srcObj.put("canvasBgColor", "#ffffff");
			srcObj.put("canvasBorderAlpha", "0");
			srcObj.put("divlineAlpha", "100");
			srcObj.put("divlineColor", "#999999");
			srcObj.put("divlineThickness", "1");
			srcObj.put("divLineDashed", "1");
			srcObj.put("divLineDashLen", "1");
			srcObj.put("yAxisName", chartInfo.getYAxisName());
		}else if (MS_STACKED_COLUMN_DUAL_Y_AXIS.equalsIgnoreCase(chartType)) {
			srcObj.put("sNumberSuffix", chartInfo.getSNumberSuffix());
			srcObj.put("pyAxisName", chartInfo.getPYAxisName());
			srcObj.put("syAxisName", chartInfo.getSYAxisName());
		} else if (PIE_MULTILEVEL.equalsIgnoreCase(chartType)) {
			srcObj.put("pieFillAlpha", "60");
			srcObj.put("pieBorderThickness", "2");
			srcObj.put("hoverFillColor", "#cccccc");
			srcObj.put("pieBorderColor", "#ffffff");
			srcObj.put("useHoverColor", "1");
			srcObj.put("showValuesInTooltip", "1");
			srcObj.put("showPercentInTooltip", "0");
			srcObj.put("plotTooltext", Misc.getParamAsString(chartInfo.getPlotTooltext(),"$label, $value $numbersuffix, $percentValue"));
			
		} else if (REAL_TIME_LINE_CHART.equalsIgnoreCase(chartType)) {
			int overRideRefreshInterval=Misc.getParamAsInt(chartInfo.getAttributeByName("overRideRefreshInterval"),0);
			String defaultInterval=OrientUtility.getOtherProperty("chart.default.refresh.interval");
			String interval=overRideRefreshInterval==1?chartInfo.getAttributeByName("refreshinterval"):((defaultInterval==null || "".equals(defaultInterval))?"180":defaultInterval);
			
			srcObj.put("yAxisName", chartInfo.getYAxisName());
			srcObj.put("width",chartInfo.getWidth());
			srcObj.put("refreshinterval", interval);
			srcObj.put("numdisplaysets", chartInfo.getAttributeByName("numdisplaysets"));
			srcObj.put("manageresize" ,"1");  srcObj.put("bgcolor" ,"000000");  
			srcObj.put("bgalpha" ,"100");  srcObj.put("canvasborderthickness" ,"1");  
			srcObj.put("canvasbordercolor" ,"008040");  srcObj.put("canvasbgcolor" ,"000000");  
			srcObj.put("decimals" ,chartInfo.getAttributeByName("decimals"));  
			srcObj.put("numvdivlines" ,chartInfo.getAttributeByName("numvdivlines"));  
			srcObj.put("divlinecolor" ,"008040");  
			srcObj.put("vdivlinecolor" ,"008040");  srcObj.put("divlinealpha" ,"100");  
			srcObj.put("chartleftmargin" ,"10");  srcObj.put("basefontcolor" ,"00dd00");  
			srcObj.put("showrealtimevalue" ,"0");  
			srcObj.put("formatNumberScale" ,"0");  
			String uri=chartInfo.getAttributeByName("datastreamurl");
			//uri=uri.replaceAll("&amp;", "&");
			srcObj.put("showValues" ,chartInfo.getAttributeByName("showValues"));  
			srcObj.put("datastreamurl" ,uri);  
			srcObj.put("numbersuffix" ,chartInfo.getAttributeByName("numbersuffix"));  
			srcObj.put("labeldisplay" ,chartInfo.getAttributeByName("labeldisplay")); 
			srcObj.put("slantlabels" ,chartInfo.getAttributeByName("slantlabels"));  
			srcObj.put("tooltipbgcolor" ,"000000");  srcObj.put("tooltipbordercolor" ,"008040");  
			srcObj.put("basefontsize" ,"11");  srcObj.put("showalternatehgridcolor" ,"0");  
			srcObj.put("legendbgcolor" ,"000000");  srcObj.put("legendbordercolor" ,"008040");  
			srcObj.put("legendpadding" ,"35");  srcObj.put("showlabels" ,chartInfo.getShowLabels());  
			srcObj.put("showborder" ,chartInfo.getShowBorder());
			} else {
			srcObj.put("yAxisName", chartInfo.getYAxisName());
		}
		return srcObj;
	}
	public static Map<String, String> p_name=new HashMap<String, String>();
	public static Map<String, String> ShoveNameMap=new HashMap<String, String>();
	public static boolean vitualShovelLoaded=false;
	static{
//		loadVirtualShovels(null);
		p_name=new HashMap<String, String>();
		//Current Parameters for Shovels
		p_name.put("101", "Tonnage/Hour");
		p_name.put("106", "Avg Cleaning Time");
		p_name.put("105", "Avg Idle Time");
		p_name.put("104", "Avg Wait Time");
		p_name.put("102", "Avg Cycle Time");
		p_name.put("103", "Avg # Cycle/Trip");
		p_name.put("107", "Avg Loading Time");
		p_name.put("108", "Avg Unloading Time");
		p_name.put("109", "Avg Load Trip Time");
		p_name.put("110", "Avg Unload Trip Time");
		p_name.put("100", "# Trips/Hour");
		
		//Shift parameters for Shovels
		p_name.put("150", "Tonnage Dispatched");
		p_name.put("151", "Trips Per Hour");
		p_name.put("152", "Avg Cycles Per Trip");
		p_name.put("153", "Avg Wait Time");
		p_name.put("154", "Cleaning Percentage");
		p_name.put("155", "Idle Percentage");
		p_name.put("156", "Cycle Time");
		p_name.put("157", "Avg Loading Time");
		p_name.put("158", "Avg Unloading Time");
		p_name.put("159", "Avg Load Trip Time");
		p_name.put("160", "Avg Unload Trip Time");
		
		//Current parameters for Dumpers
		p_name.put("200", "Trips Per Hour" );
		p_name.put("201", "Tonnage Per Hour");
		p_name.put("202", "Avg Load Time");
		p_name.put("203", "Avg Wait Time");
		p_name.put("204", "Avg Load Lead Distance");
		p_name.put("205", "Avg Load Lead Time");
		p_name.put("206", "Avg UnLoad Lead Distance");
		p_name.put("207", "Avg UnLoad Lead Time");
		p_name.put("208", "p_19");
		p_name.put("209", "p_20");
		
		//Shift parameters for Dumpers
		
		p_name.put("250", "Trips Per Hour");
		p_name.put("251", "Tonnes Moved" );
		p_name.put("252", "Total Distance");
		p_name.put("253", "Avg Wait Time");
		p_name.put("254", "Avg Load Lead Distance");
		p_name.put("255", "Avg Load Lead Time");
		p_name.put("256", "Avg UnLoad Lead Distance");
		p_name.put("257", "Avg UnLoad Lead Time");
		p_name.put("258", "Total Waiting Time");
		p_name.put("259", "Total Idling Time");
		
		//Current parameters for Pits
		p_name.put("300", "Number Of Dumpers" );
		p_name.put("301", "Avg Tonnage Dispatched");
		p_name.put("302", "Avg Wait Time For Dumpers");
		p_name.put("303", "Avg Idle Time Of Shovel");
		p_name.put("304", "Avg Cycle Time");
		p_name.put("305", "Avg Cycle Per Trip");
		p_name.put("306", "Avg Lead");
		p_name.put("307", "p_28");
		p_name.put("308", "p_29");
		p_name.put("309", "p_30");
		//Shift parameters for Pits
		p_name.put("350", "Number Of Dumpers" );
		p_name.put("351", "Avg Tonnage Dispatched");
		p_name.put("352", "Avg Wait Time For Dumpers");
		p_name.put("353", "Avg Idle Time Of Shovel");
		p_name.put("354", "Avg Cycle Time");
		p_name.put("355", "Avg Cycle Per Trip");
		p_name.put("356", "Avg Lead");
		p_name.put("357", "p_28");
		p_name.put("358", "p_29");
		p_name.put("359", "p_30");
		//Generic Params
		p_name.put("90", "shovelid");
		p_name.put("91", "Shovel Name");
		p_name.put("92", "Time");
		p_name.put("92", "vehicle Id");
	}
	
	

	public static void loadVirtualShovels(SessionManager session) {
		if (vitualShovelLoaded)
			return;
		Connection conn = session.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("select id,short_code from dos_inventory_piles where pile_type=1 and status=1");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int pileId=Misc.getRsetInt(rs, "id");
				String name=Misc.getRsetString(rs, "short_code");
				ShoveNameMap.put(pileId+"",name);
			}
			ps = Misc.closePS(ps);
			

		} catch (Exception e) {
			vitualShovelLoaded = false;
			e.printStackTrace();
			//throw e;
		}
		vitualShovelLoaded = true;

	}
	public static ArrayList<Map<String, String>> getBar2dJason(Map<String, Map<String, String>> resultSet,
			FrontPageInfo fpi, SearchBoxHelper searchBoxHelper,	SessionManager session,ChartInfo chartInfo) throws ChartException {
		ArrayList<Integer> params = null;
		String topPageContext = searchBoxHelper.m_topPageContext;
		ArrayList al = searchBoxHelper.m_searchParams;
		for (int i = 0, is = al == null ? 0 : al.size(); i < is; i++) {
			ArrayList row = (ArrayList) al.get(i);
			for (int j = 0, js = row == null ? 0 : row.size(); j < js; j++) {
				DimConfigInfo dc = (DimConfigInfo) row.get(j);
				System.out.println(dc.m_dimCalc.m_dimInfo.m_id);
				int p_id = dc.m_dimCalc.m_dimInfo.m_id;
				String tempVarName = topPageContext + p_id;
				String tempVal = session.getAttribute(tempVarName);
				System.out.println(tempVarName + "=" + tempVal);
				switch (p_id) {
				case 82510: {//Dumpers Current Params
					if(tempVal!=null)
						//In future can cater multiple charts based on selected param
						//reason for taking it in List
					params = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}				
				default:
				}
			}
		}
		ArrayList<Map<String, String>> pair =new ArrayList<Map<String,String>>();
		if(params.size()<=0)
			throw new ChartException("Select valid Dumper parameter(s). Except [ANY,IS NOT NULL,IS NULL].");
		for (Entry<String, Map<String, String>> entry : resultSet.entrySet()) {
			String dumperName = entry.getKey();
			Map<String, String> lv = new HashMap<String, String>();
			lv.put(labelKey, dumperName);
			lv.put(valueKey, resultSet.get(dumperName).get(params!=null?params.get(0)+"":0));
			pair.add(lv);
		}
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		if(pair.size()>0)
		chartInfo.setCaption(p_name.get(params!=null && params.size()>0?params.get(0)+"":"0")+" @ "+(formatter.format(new Date())));
		return pair;
	}
	public static Map<String, Map<String, String>> getACBAllDumperDataFromTable(
			Table table, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper,SessionManager session) {
		ArrayList<TR> body = table.getBody();
		Map<String, Map<String, String>> shovelData = new HashMap<String, Map<String, String>>();

		Map<String, Integer> totalParamsInFrontPage = getAllParamIdMap(fpi.m_frontInfoList, true);
		ArrayList<Integer> allParams = getAllParamId(fpi.m_frontInfoList, false);

		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> paramData = new HashMap<String, String>();
			if (tdArr.get(table.getColumnIndexById(82549)).getContent()==null || tdArr.get(table.getColumnIndexById(82549)).getContent()=="&nbsp;")
				continue;
			for (Integer pId : allParams) {
				int dimId = totalParamsInFrontPage.get(pId + "");
				if (tdArr != null) {
					String colVal = tdArr.get(table.getColumnIndexById(dimId)).getContent();
					paramData.put(pId + "", colVal == BLANK ? "0.0" : colVal);
				}
			}
			String dumperId = "";
			if (Misc.getUndefInt() != table.getColumnIndexById(20274))
				dumperId = tdArr.get(table.getColumnIndexById(20274)).getContent();// vehicle id
			String name = "";// shovel/dumper/pit name
			if (Misc.getUndefInt() != table.getColumnIndexById(9002))
				name = tdArr.get(table.getColumnIndexById(9002)).getContent();
			ShoveNameMap.put(dumperId, name);
			shovelData.put(name, paramData);
		}
		return shovelData;
	}
	public static ArrayList<Map<String, String>> getExcavatorLoadEventsDataFromTable(Table table, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper,
			SessionManager session) {
	
		//ArrayList<Map<String, Object>> datasetOne=getStackedColumnJson(fpi.chartInfo,axisCats, table,isMultiSeries,createLineSet,null);
		
//		int seriesIndx = table.getColumnIndexById(Misc.getParamAsInt(fpi.chartInfo.getCategorySeries()));
//		int valueIndx = table.getColumnIndexById(Misc.getParamAsInt(fpi.chartInfo.getCategoryValue()));
//		if(seriesIndx==Misc.UNDEF_VALUE && valueIndx==Misc.UNDEF_VALUE ){
//			throw new ChartException("'Series and Value' DIM's are not defined Properly.For "+chartInfo.getName());  
//		}else if(seriesIndx==Misc.UNDEF_VALUE){
//			throw new ChartException("'Series' DIM is not defined Properly. For "+chartInfo.getName());   
//		}else if(valueIndx==Misc.UNDEF_VALUE){
//			throw new ChartException("'Value' DIM is not defined Properly. For "+chartInfo.getName());    
//		}
		int gpsTimeIndx = table.getColumnIndexById(82482);
		int gapRelIndx = table.getColumnIndexById(82701);
		int inactiveDurrIndx = table.getColumnIndexById(82702);
		int tripIdIndx = table.getColumnIndexById(82491);
		ArrayList<TR> body = table.getBody();
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();	
		//TreeMap<String,Triple<Integer,Integer,Integer>> dataMap=new TreeMap<String,Triple<Integer,Integer,Integer>>();
		//Triple<Integer, Integer, Integer> prev;
		int prvTripId=0;
		double dataVal=0;
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> lv = new HashMap<String, String>();
			String gpsTime = tdArr.get(gpsTimeIndx).getContent();
			int gapRel =Misc.getParamAsInt(tdArr.get(gapRelIndx).getContent());
			int inactiveDurr = Misc.getParamAsInt(tdArr.get(inactiveDurrIndx).getContent());
			int tripId =Misc.getParamAsInt(BLANK.equalsIgnoreCase(tdArr.get(tripIdIndx).getContent())?null:tdArr.get(tripIdIndx).getContent());
//			Triple<Integer, Integer, Integer> t=new Triple<Integer, Integer, Integer>(gapRel,inactiveDurr,tripId);
//			if(prev==null){
//				prev=t;
//			}
			
			if(Misc.isUndef(tripId)){
				if(inactiveDurr==0)
				dataVal=0;
				if(inactiveDurr>0){
					dataVal=1;
					for (int i = 0; i < (inactiveDurr/40); i++) {
						try {
							String time=manupulateDateTime(gpsTime,i*40,Calendar.SECOND);
							lv.put(labelKey, time);
							lv.put(valueKey,dataVal+"");
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						
					}
						
				}
				prvTripId=tripId;
			}else if(!Misc.isUndef(tripId) && prvTripId==tripId){
				//dataVal=1;
				prvTripId=tripId;
			}else if(!Misc.isUndef(tripId) && prvTripId!=tripId){
				dataVal=dataVal==2?2.5:2;
				prvTripId=tripId;
			}
			
			lv.put(labelKey, gpsTime);
			lv.put(valueKey,dataVal+"");
			arrData.add(lv);
		}

		
		return arrData;
	}
	
	static ArrayList<Map<String, Object>> getStepLineDetails(Table table, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper,
			SessionManager session, Map<String, ArrayList<Map<String, String>>> categoriesMap,String seriesName) {
		ArrayList<Map<String, Object>> dataSetList = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, String>> categoryList = new ArrayList<Map<String, String>>();
		Map<String, Object> seriesMap = new HashMap<String, Object>();
		seriesMap.put("seriesname",seriesName);
		seriesMap.put("linethickness", "3");
		seriesMap.put("anchorradius", "3");
		
		int gpsTimeIndx = table.getColumnIndexById(82482);
		int gapRelIndx = table.getColumnIndexById(82701);
		int inactiveDurrIndx = table.getColumnIndexById(82702);
		int tripIdIndx = table.getColumnIndexById(82491);
		ArrayList<TR> body = table.getBody();
		ArrayList<Map<String, String>> arrData = new ArrayList<Map<String, String>>();	
		//TreeMap<String,Triple<Integer,Integer,Integer>> dataMap=new TreeMap<String,Triple<Integer,Integer,Integer>>();
		//Triple<Integer, Integer, Integer> prev;
		int prvTripId=0;
		double dataVal=0;
		 int showDateAsTimeOnly=Misc.getParamAsInt(fpi.chartInfo.getAttributeByName("show_date_as_time"));
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> dlv = new HashMap<String, String>();
			Map<String, String> clv = new HashMap<String, String>();
			String gpsTime = tdArr.get(gpsTimeIndx).getContent();
			int gapRel =Misc.getParamAsInt(tdArr.get(gapRelIndx).getContent());
			int inactiveDurr = Misc.getParamAsInt(tdArr.get(inactiveDurrIndx).getContent());
			int tripId =Misc.getParamAsInt(BLANK.equalsIgnoreCase(tdArr.get(tripIdIndx).getContent())?null:tdArr.get(tripIdIndx).getContent());
//			Triple<Integer, Integer, Integer> t=new Triple<Integer, Integer, Integer>(gapRel,inactiveDurr,tripId);
//			if(prev==null){
//				prev=t;
//			}
			
			if(Misc.isUndef(tripId)){
				if(inactiveDurr==0)
				dataVal=0;
				if(inactiveDurr>0){
					dataVal=1;
					/*for (int i = 0; i < (inactiveDurr/40); i++) {
						try {
							String time=manupulateDateTime(gpsTime,i*40,Calendar.SECOND);
							clv.put(labelKey, time);
							dlv.put(valueKey,dataVal+"");
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						
					}*/
						
				}
				prvTripId=tripId;
			}else if(!Misc.isUndef(tripId) && prvTripId==tripId){
				//dataVal=1;
				prvTripId=tripId;
			}else if(!Misc.isUndef(tripId) && prvTripId!=tripId){
				dataVal=dataVal==2?2.25:2;
				prvTripId=tripId;
			}
			try {
				Date date1=null;
				if(gpsTime.contains("/"))
				 date1=new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(gpsTime);
				else if(gpsTime.contains("-"))
					 date1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(gpsTime);
				gpsTime=formatDate(date1,showDateAsTimeOnly==1,false);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			clv.put(labelKey, gpsTime);
			dlv.put(valueKey,dataVal+"");
			dataList.add(dlv);
			System.out.println("DataList Step size--"+dataList.size());
			categoryList.add(clv);
		}

		categoriesMap.put(categoryKey, categoryList);
		seriesMap.put("data",dataList);
		dataSetList.add(seriesMap);
		//dataSetList.add(1,categoriesMap);
		return dataSetList;
	}
	
	public static ArrayList<Map<String, String>> getMSLineDetailsColumnWise(Table table, String _labelKey,int dimId,boolean doCumulative) {
		int dataIndx = table.getColumnIndexById(dimId);
		ArrayList<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		if(Misc.isUndef(dataIndx))
			return dataList;
		ArrayList<TR> body = table.getBody();
		double cumSum=0.0;
		for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
			ArrayList<TD> tdArr = body.get(row).getRowData();
			Map<String, String> dlv = new HashMap<String, String>();
			String data =BLANK.equalsIgnoreCase(tdArr.get(dataIndx).getContent())?"0":tdArr.get(dataIndx).getContent();
			if(doCumulative){
				cumSum=cumSum+Misc.getParamAsDouble(data,0.0);
				data=cumSum+"";
			}
			dlv.put(_labelKey,data);
			dataList.add(dlv);
		}//end for
		return dataList;
	}
	
	public static String manupulateDateTime(String dateTime,int addVall,int calenderUnit) throws ParseException{
		Date date1=null;
		if(dateTime.contains("/"))
			 date1=new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(dateTime);
			else if(dateTime.contains("-"))
				 date1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
			//time=formatDate(date1,showDateAsTimeOnly);
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(date1);
		if(calenderUnit==Calendar.SECOND){
			cal.add(Calendar.SECOND, addVall);
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return dateFormat.format(cal.getTime());
//		
//		 cal.add(Calendar.DATE, addVall);
		 
	}
	
	public static void main(String[] args) {
		//System.out.println(formatDate(new Date(),1));
		try {
			Date date1=new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse("15/11/18 18:00:17");
			//System.out.println(formatDate(date1,1));
			System.out.println(manupulateDateTime("15/11/18 18:00:17",40,Calendar.SECOND));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		double a=23.5;
//		System.out.println(a/2);
//		System.out.println(((int)a/2.0)*2.0);
	}
	
	
}
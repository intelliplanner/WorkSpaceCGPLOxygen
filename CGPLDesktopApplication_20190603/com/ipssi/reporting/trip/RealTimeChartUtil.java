package com.ipssi.reporting.trip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

import com.google.gson.Gson;
import com.ipssi.gen.utils.ChartInfo;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;

public class RealTimeChartUtil {
	public static Map<String, String> getShovelStats(Gson gson,ChartInfo chartInfo,FrontPageInfo fpi, Table table, SessionManager session,SearchBoxHelper searchBoxHelper, JspWriter out) throws ChartException, IOException {
		Map<String,Map<String,Map<String,String>>> resultSet=ChartUtil.getShovelDataFromTable(chartInfo,table,fpi,searchBoxHelper,session);
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
				case 82545: {
					if(tempVal!=null)
					shovelIds = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
					break; 
				}
//				case 82400: {
//					if(tempVal!=null)
//					pitIds = ChartUtil.getIntegerListFromStringArray(tempVal.split(","));
//				}
				case 82508: {
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
			if ("shovel_stats_db".contains(chartInfo.getChartCategory())) {

				for (int j = 0; j < params.size(); j++) {
					int param_id = params.get(j);
					try {
						ci = (ChartInfo) chartInfo.clone();
					} catch (CloneNotSupportedException e) {
					}
					ChartUtil.configureChartVariables(ci, params, i, j, shovel_id);
					dataSrc = getShovelDashboardJason(gson, ci, resultSet.get(shovel_id + ""), shovel_id, param_id);
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
				dataSrc = ChartUtil.getShovelJason(gson, ci, resultSet.get(shovel_id + ""), shovel_id, params);
				ci.setDataSource(dataSrc);
				FusionCharts fChart = new FusionCharts(ci);
				out.print(fChart.render());
				dataPlotCounter=(dataPlotCounter==3?0:dataPlotCounter);
			}
				
			
		}
		return null;
	}

	private static String getShovelDashboardJason(Gson gson, ChartInfo ci,
			Map<String, Map<String, String>> map, int shovel_id, int param_id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static Map<String, String> p_name=new HashMap<String, String>();
	public static Map<String, String> ShoveNameMap=new HashMap<String, String>();
	static{
		p_name=new HashMap<String, String>();
		p_name.put("0", "Avg Tonnage Per Hour");
		p_name.put("1", "Avg cleaning percentage");
		p_name.put("2", "Avg Idle Percentage");
		p_name.put("3", "Avg Dumper Wait Time");
		p_name.put("4", "Avg Cycle Time");
		p_name.put("5", "Avg Num Of Cycle Per Trip");
		p_name.put("6", "p_6");
		p_name.put("7", "p_7");
		p_name.put("8", "p_8");
		p_name.put("9", "p_9");
		p_name.put("10", "p_10");
		p_name.put("90", "shovelid");
		p_name.put("91", "Shovel Name");
		p_name.put("92", "Time");
		p_name.put("92", "vehicle Id");
	}
}

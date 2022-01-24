package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.utils.ChartInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.miningOpt.SiteStats;


public class ChartGroupingUtil {
	
	private static Map<String,ArrayList<Integer>> groupLoadSites=new HashMap<String, ArrayList<Integer>>();
	private static Map<String,ArrayList<Integer>> subGroupLoadSites=new HashMap<String, ArrayList<Integer>>();
	private static Map<String,ArrayList<Integer>> pitLoadSites=new HashMap<String, ArrayList<Integer>>();
	static{
		
	}
	private static void loadSitesGrouping(SessionManager session) {
		Connection conn = session.getConnection();
		PreparedStatement ps = null;
		
		try {
			ps = conn.prepareStatement("SELECT DISTINCT (ip.id ) as load_site_id, ip.short_code as ls_name, p.name as pit_name,sb.sub_group_name , g.group_name from dos_inventory_piles ip join dos_pits p on  ip.pit_id=p.id join dos_sub_group_pits sgp on sgp.pit_id=p.id  join dos_sub_group sb on sb.id=sgp.sub_group_id join dos_group g on g.id=sb.group_id where ip.pile_type=1 and ip.status=1 group by g.id,sb.id,p.id");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String groupId=Misc.getRsetString(rs, "group_name");
				String subGroupId=Misc.getRsetString(rs, "sub_group_name");
				String pitId=Misc.getRsetString(rs, "pit_name");
				int loadSiteId=Misc.getRsetInt(rs, "load_site_id");
				
				if(groupLoadSites.containsKey(groupId)){
					ArrayList<Integer> ls =groupLoadSites.get(groupId)==null?new ArrayList<Integer>():groupLoadSites.get(groupId);
					ls.add(loadSiteId);
				}else{
					ArrayList<Integer> ls =new ArrayList<Integer>();
					ls.add(loadSiteId);
					groupLoadSites.put(groupId,ls);
				}
				if(subGroupLoadSites.containsKey(subGroupId)){
					ArrayList<Integer> ls =subGroupLoadSites.get(subGroupId)==null?new ArrayList<Integer>():subGroupLoadSites.get(subGroupId);
					ls.add(loadSiteId);
				}else{
					ArrayList<Integer> ls =new ArrayList<Integer>();
					ls.add(loadSiteId);
					groupLoadSites.put(subGroupId,ls);
				}
				if(pitLoadSites.containsKey(pitId)){
					ArrayList<Integer> ls =pitLoadSites.get(pitId)==null?new ArrayList<Integer>():pitLoadSites.get(pitId);
					ls.add(loadSiteId);
				}else{
					ArrayList<Integer> ls =new ArrayList<Integer>();
					ls.add(loadSiteId);
					pitLoadSites.put(pitId,ls);
				}
				System.out.println("Group="+groupId+", SubGroup="+subGroupId+", Pit="+pitId+", LS="+loadSiteId);
			}
			ps = Misc.closePS(ps);
			

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				TripInfoCacheHelper.initOpsBeanRelatedCache(conn,new ArrayList<Integer>());
				SiteStats.initStatic(conn, 1467, false);
			} catch (Exception e) {
				System.err.println("Exeption while initializing: TripInfoCacheHelper.initOpsBeanRelatedCache and SiteStats.initStatic");
			}
			
		}
	}
	public static ArrayList<Integer> getShovelList(String shovelGroupingOn) {
		return null;
		// TODO Auto-generated method stub
		
	}
	public static  Map<String,ArrayList<Integer>> getGroupingParameterList(String shovelGroupingOn,SessionManager session) {
		//Calling on every call to get latest data 
		loadSitesGrouping(session);
		if("0".equalsIgnoreCase(shovelGroupingOn)){
			return groupLoadSites;
		}else if("1".equalsIgnoreCase(shovelGroupingOn)){
			return subGroupLoadSites;
		}else if("1".equalsIgnoreCase(shovelGroupingOn)){
			return pitLoadSites;
		}else{
			return null;
		}
	}
	
	public static Map<String, Map<String, Map<String, String>>> getShovelDataFromTable(ChartInfo chartInfo,Table table, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper,SessionManager session, Map<String,ArrayList<Integer>> groupingList) {
		ArrayList<TR> body = table.getBody();
		
		Map<String, Map<String, Map<String, String>>> shovelData=new HashMap<String, Map<String,Map<String,String>>>();
		int showDateAsTimeOnly=Misc.getParamAsInt(chartInfo.getAttributeByName("show_date_as_time"));
		Map<String,Integer> totalParamsInFrontPage=ChartUtil.getAllParamIdMap(fpi.m_frontInfoList,true);
		ArrayList<Integer> allParams=ChartUtil.getAllParamId(fpi.m_frontInfoList,false);
//		for Numbers of column and group
		for (Entry<String, ArrayList<Integer>> entry : groupingList.entrySet()) {
			String groupName=entry.getKey();
			Map<String, Map<String, String>> timeData=null;
			Map<String, String> acuumulativeParamData=new HashMap<String,String>();
			for (int row = 0, rowSize = body == null ? 0 : body.size(); row < rowSize; row++) {
				ArrayList<TD> tdArr = body.get(row).getRowData();
				
				//TO-DO need to remove below HardCode Time and shovelid DIM
				String shovel_id="";
				if(Misc.getUndefInt()!=table.getColumnIndexById(20274))
					shovel_id=tdArr.get(table.getColumnIndexById(20274)).getContent();//vehicle id
				
				if(!needShovelData(shovel_id))//if not in list then next record
					continue;
				//Vehicel-.Time->Param value
				
				
				
				for (Integer pId : allParams) {
					int dimId=totalParamsInFrontPage.get(pId+"");
					if (tdArr != null) {
						double colVal = Misc.getParamAsDouble(tdArr.get(table.getColumnIndexById(dimId)).getContent(),0.0);
						if(null==acuumulativeParamData.get(pId+""))
							acuumulativeParamData.put(pId+"", colVal+"");
						else{
							acuumulativeParamData.put(pId+"",  (Misc.getParamAsDouble(acuumulativeParamData.get(pId+""),0.0)+colVal)+"");
						}
					}
				}
				
				
//				String time=null;
//				if(Misc.getUndefInt()!=table.getColumnIndexById(82530))//Shovel Curr Time
//					time=tdArr.get(table.getColumnIndexById(82530)).getContent();
//				else if (Misc.getUndefInt()!=table.getColumnIndexById(83110))//shovel Shift
//					time=tdArr.get(table.getColumnIndexById(83110)).getContent();
//				
				
				
			}//end table row
			
//			if(shovelData.containsKey(shovel_id)){
//				timeData=shovelData.get(shovel_id);
//				if(!timeData.containsKey(time)){
//					timeData.put(time, paramData);
//					shovelData.put(shovel_id, timeData);
//				}
//			}else {
				timeData=new TreeMap<String,Map<String,String>>();
				timeData.put(ChartUtil.formatDate(new Date(),showDateAsTimeOnly==1,false), acuumulativeParamData);
				shovelData.put(groupName, timeData);
//			}
			
		}//end grouping
		return shovelData;
	}
	private static boolean needShovelData(String shovel_id) {
		// TODO Auto-generated method stub
		return false;
	}
}
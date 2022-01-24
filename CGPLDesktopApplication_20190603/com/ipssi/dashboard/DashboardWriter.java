package com.ipssi.dashboard;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.naming.InitialContext;
import javax.servlet.jsp.JspWriter;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Common;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FmtI;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.DimInfo.DimValList;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.input.InputTemplate;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder;
import com.ipssi.tracker.common.util.PropertyManager;
import com.ipssi.tripprocessor.dashboard.bean.StrandedVehicleVO;
import com.ipssi.tripprocessor.dashboard.bean.TrackRegionInfoVO;
import com.ipssi.tripprocessor.dashboard.bean.VOInterface;
import com.ipssi.tripprocessor.dashboard.bean.VehicleOutsideGateVO;
import com.ipssi.tripprocessor.dashboard.bean.VehicleVO;
import com.ipssi.tripprocessor.ejb.session.CacheAccessorRemote;
import com.ipssi.tripcommon.LUInfoExtract;

public class DashboardWriter {
//	public static HashMap<String, List <Pair<Integer, LUInfoExtract>>> regionInfo = new HashMap<String, List <Pair<Integer, LUInfoExtract>>>();
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, String sessionMethodToInvoke) throws Exception {
		StringBuilder sb = new StringBuilder();
		//		printSearchBlock(fpiList, session);
		sb.append("<table ID='DATA_TABLE' width='100%' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
		printTableHeader(sb, fpi, session, searchBoxHelper);
		HashMap<String, String> filterCriteriaMap = new HashMap<String, String>();
		ArrayList<ArrayList<DimConfigInfo>> searchBox = fpi.m_frontSearchCriteria;
		setFilterCriteria(filterCriteriaMap, session, searchBox, searchBoxHelper);
		try{
			if("tripEventTextDashboard".equalsIgnoreCase(sessionMethodToInvoke))
				printTableForTextDashboard(conn,filterCriteriaMap, sb, fpi, searchBoxHelper, session, sessionMethodToInvoke);
			else if("tripCountPerformance".equalsIgnoreCase(sessionMethodToInvoke))
				printTableForAcc(conn,filterCriteriaMap, sb, fpi, searchBoxHelper, session, sessionMethodToInvoke);
			else
				printTable(conn, filterCriteriaMap, sb, fpi, searchBoxHelper, session, sessionMethodToInvoke);
			
			sb.append("</table>");
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		return sb.toString();
	}
	public String printPage(Connection conn, FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, String sessionMethodToInvoke, String home9029) throws Exception {
		StringBuilder sb = new StringBuilder();
		//		printSearchBlock(fpiList, session);
		sb.append("<table ID='DATA_TABLE' width='100%' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
		printTableHeader(sb, fpi, session, searchBoxHelper);
		HashMap<String, String> filterCriteriaMap = new HashMap<String, String>();
		ArrayList<ArrayList<DimConfigInfo>> searchBox = fpi.m_frontSearchCriteria;
		System.out.println("DashboardWriter.printPage() : " + "sessionMethodToInvoke => " + sessionMethodToInvoke);
		setFilterCriteria(filterCriteriaMap, session, searchBox, searchBoxHelper);
		filterCriteriaMap.put("home9029", home9029);
		try{
			
			if("tripCountPerformance".equalsIgnoreCase(sessionMethodToInvoke))
				printTableForAcc(conn,filterCriteriaMap, sb, fpi, searchBoxHelper, session, sessionMethodToInvoke);
			else
				printTable(conn,filterCriteriaMap, sb, fpi, searchBoxHelper, session, sessionMethodToInvoke);
			
			sb.append("</table>");
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		return sb.toString();
	}
	
	public void setFilterCriteria(HashMap<String, String> filterCriteriaMap, SessionManager _session, ArrayList<ArrayList<DimConfigInfo>> searchBox, SearchBoxHelper searchBoxHelper){
		try {
			if (searchBoxHelper == null)
				return ;
			String topPageContext = searchBoxHelper.m_topPageContext;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					boolean is123 = dimConfig.m_dimCalc.m_dimInfo.m_descDataDimId == 123;
//					boolean isDummy = false;
					int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
					String tempVarName = is123 ? "pv123" : topPageContext+paramId;
					//Misc.getUserTrackControlOrg(session)
					String tempVal =_session.getParameter(tempVarName);
					if (tempVal == null || tempVal.length() == 0)
						tempVal = _session.getAttribute(tempVarName);
					if(tempVal == null || tempVal.length() == 0 && paramId != 9003) //for veh type hack
						tempVal = dimConfig.m_dimCalc.m_dimInfo.m_default;
					if(tempVal != null){
						if(is123)
							filterCriteriaMap.put(tempVarName, String.valueOf(Misc.getUserTrackControlOrg(_session)));
						else
							filterCriteriaMap.put(""+paramId, tempVal);
					}
					// System.out.println("DashboardWriter.setFilterCriteria() : " + "paramId => "+paramId+" tempVal => "+tempVal);
				}
			}
		}catch (Exception e){
			e.printStackTrace();              
		}
		return ;
	}
	public void printTableHeader(StringBuilder sb, FrontPageInfo fpi, SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper) throws Exception {
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		if(fpList != null)
			cols = fpList.size();
		sb.append("<thead>");
		Cache cache = session.getCache();
		boolean hasMultiple = hasNestedColHeader(fpList);
		boolean hasNestedGroup = hasNestedGroupHeader(fpList);
		if (hasNestedGroup) {
			fpList = (ArrayList<DimConfigInfo>) fpList.clone();
			hasNestedGroup = populateGroupByAndConfirmNestedGroup(fpList, session.getConnection(), session, searchBoxHelper);
		}
		if (hasNestedGroup)
			hasMultiple = true;
		String headerClass = hasMultiple ? "tshb" : "tshc";
		
		//doing top level
		sb.append("<tr>");		
		for(int i=0, numsToSkip=1; i<cols; i += numsToSkip){
			numsToSkip = 1; //if dimconfig dataspan > 1 then this becomes that value
			DimConfigInfo dci = fpList.get(i);
			if (dci == null || dci.m_hidden)
				continue;
            DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
            
			int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
            boolean doDate = attribType == cache.DATE_TYPE;
            boolean doNumber = attribType == cache.NUMBER_TYPE;
                    
            int labelWidth = dci.m_labelWidth;
            String widthStr = dci.m_dataSpan <= 1 && labelWidth > 0 ? " width='"+labelWidth+"' " : " "; //do width only in spanning col
            String colSpan = hasMultiple && dci.m_dataSpan > 1 ? " colspan='"+dci.m_dataSpan +"' ": " ";
            String rowSpan = hasMultiple && dci.m_dataSpan <= 1 ? " rowspan='2' " : " ";
            numsToSkip = dci.m_dataSpan > 1 ? dci.m_dataSpan : 1;
            ArrayList<DimInfo.DimValList> groupList = dci.m_dimCalc != null && dci.m_dimCalc.m_groupBy != null && dci.m_dimCalc.m_groupBy.size() > 0 ?  (ArrayList<DimInfo.DimValList>)dci.m_dimCalc.m_groupBy : null;
			int ngroups = groupList != null && groupList.get(0).useThisForGps != null ? groupList.get(0).useThisForGps.size() : 0;
		    if (ngroups >= 1)
		    	numsToSkip = 1;
            sb.append("<td")
                .append(rowSpan)
                .append(colSpan)
                .append("dt_type='").append(doDate ? "date" : doNumber ? "num" : "text" ).append("' ")
                .append(widthStr)
                .append("class='").append(headerClass).append("'>")
                ;
            boolean doSortLink = dci.m_dataSpan <= 1;
            if (doSortLink)
                sb.append("<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>");
            String name = dci.m_dataSpan > 1 ? dci.m_frontPageColSpanLabel : dci.m_name;
            if (name == null || name.length() == 0)
            	name = dci.m_name;
            sb.append(name != null && name.length() != 0 ? name : "&nbsp;");
            if (doSortLink)
            	sb.append("</a>");            
            sb.append("</td>");			
		}
		sb.append("</tr>");
		if (hasMultiple) {
			sb.append("<tr>");		
			for(int i=0, numsToConsider=0; i<cols; i++,numsToConsider--){
				DimConfigInfo dci = fpList.get(i);
				if (dci == null || dci.m_hidden)
					continue;
				ArrayList<DimInfo.DimValList> groupList = dci.m_dimCalc != null && dci.m_dimCalc.m_groupBy != null && dci.m_dimCalc.m_groupBy.size() > 0 ?  (ArrayList<DimInfo.DimValList>)dci.m_dimCalc.m_groupBy : null;
				int ngroups = groupList != null && groupList.get(0).useThisForGps != null ? groupList.get(0).useThisForGps.size() : 0;
			    if (ngroups >= 1) {
			    	DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
		            
					int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
		            boolean doDate = attribType == cache.DATE_TYPE;
		            boolean doNumber = attribType == cache.NUMBER_TYPE;
		                    
		            int labelWidth = dci.m_labelWidth;
		            String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : " ";	
		            ArrayList<DimInfo.ValInfo> valList = groupList.get(0).useThisForGps;
		            for (int j=0;j<ngroups;j++) {
			            sb.append("<td")
			                .append(" dt_type='").append(doDate ? "date" : doNumber ? "num" : "text" ).append("' ")
			                .append(widthStr)
			                .append("class='").append(headerClass).append("'/>")
			                ;
			            boolean doSortLink = true;
			            if (doSortLink)
			                sb.append("<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>");
			            ValInfo vinfo = valList.get(j);
			            String labelName = vinfo == null ? "All" : vinfo.m_name;
			            sb.append(labelName != null && labelName.length() != 0 ? labelName : "&nbsp;");
			            if (doSortLink)
			            	sb.append("</a>");            
			            sb.append("</td>");
		            }
			    }
			    else {
					if (dci.m_dataSpan > 1)
						numsToConsider = dci.m_dataSpan;			
					
				
					if (numsToConsider <= 0)
						continue;
					
		            DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
		            
					int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
		            boolean doDate = attribType == cache.DATE_TYPE;
		            boolean doNumber = attribType == cache.NUMBER_TYPE;
		                    
		            int labelWidth = dci.m_labelWidth;
		            String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : " ";	            
		            
		            sb.append("<td")
		                .append(" dt_type='").append(doDate ? "date" : doNumber ? "num" : "text" ).append("' ")
		                .append(widthStr)
		                .append("class='").append(headerClass).append("'/>")
		                ;
		            boolean doSortLink = true;
		            if (doSortLink)
		                sb.append("<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>");            
		            sb.append(dci.m_name != null && dci.m_name.length() != 0 ? dci.m_name : "&nbsp;");
		            if (doSortLink)
		            	sb.append("</a>");            
		            sb.append("</td>");
			    }
			}
			sb.append("</tr>");
		}		
		sb.append("</thead>");
	}
	
	private static boolean hasAny(ArrayList<Integer>list) {
		for (int i=0,is = list == null ? 0 : list.size(); i<is;i++)
			if (list.get(i) == Misc.G_HACKANYVAL)
				return true;
		return false;
	}
	
	public boolean populateGroupByAndConfirmNestedGroup(ArrayList<DimConfigInfo> fpList, Connection conn, SessionManager session, SearchBoxHelper searchBox) throws Exception {//only does for 1
		boolean retval = false;
		for (int i=0,is=fpList == null ? 0 : fpList.size();i<is;i++) {
    		DimConfigInfo dci = fpList.get(i);
    		if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_groupBy != null && dci.m_dimCalc.m_groupBy.size() > 0) {
    			DimInfo.DimValList entry = (DimInfo.DimValList) dci.m_dimCalc.m_groupBy.get(0);
    			DimInfo groupDim = entry != null && entry.m_dimInfo != null ? entry.m_dimInfo: null;
    			if (groupDim == null)
    				continue;
    			ArrayList<DimInfo.ValInfo> finalList = new ArrayList<DimInfo.ValInfo>();
    			//will populate finalList of ValInfo to group by as follows:
    			//if there is a spec list, then the item in that list only be added if it is in search and is validVal
    			//if there is no spec list then we will treat the search as specList
    			//search list if non-existent or any then we will consider as all available
    			
    			//1. get valListInSearch and see there is any then consider it as as non-existent search
    			ArrayList<Integer> valListInSearch = new ArrayList<Integer> ();
    			String topContext = searchBox == null ? "p" : searchBox.m_topPageContext;
    			String searchVal = session.getParameter(topContext+groupDim.m_id);
    			if (searchVal == null || searchVal.length() == 0)
    				searchVal = session.getParameter("pv"+groupDim.m_id);
    			if (searchVal != null && searchVal.length() > 0)
    				Misc.convertValToVector(searchVal, valListInSearch);
    			boolean searchHasAny = hasAny(valListInSearch);
    			if (searchHasAny)
    				valListInSearch.clear();
    			
    			//2. valListSpec - check if any ... it means group by total needed ... if otherwise the list is empty then consider as same as valListInSearch.
    			
    			ArrayList<Integer> valListSpec = entry.getValList();
    			boolean valListSpecHasAny = hasAny(valListSpec);
    			
    			if (valListSpecHasAny) {//remove those entries
    				for (int j=valListSpec.size()-1;j>=0;j--)
    					valListSpec.remove(j);
    			}
    			boolean valListSpecSameAsSearchSpec = false;
    			if (valListSpec == null || valListSpec.size() == 0) {
    				valListSpec = valListInSearch;
    				valListSpecSameAsSearchSpec = true;
    			}
    			boolean hasValidSearch = valListInSearch != null && valListInSearch.size() > 0;
    			
    			
    			//3. now get the fullValList
    			ArrayList<DimInfo.ValInfo> fullValList = groupDim.getValList(conn, session);
    			for (int j=0,js=valListSpec == null ? 0 : valListSpec.size(); j<js;j++) {
    				int v = valListSpec.get(j);
    				if (hasValidSearch && !valListSpecSameAsSearchSpec) {//add if exists in searchList as well as valList
	    				for (int k=0,ks = valListInSearch == null ? 0 : valListInSearch.size(); k<ks; k++) {
	    					if (valListInSearch.get(k) == v) {
	    						for (int l=0,ls = fullValList == null ? 0 : fullValList.size(); l<ls;l++) {
	    							if (fullValList.get(l).m_id == v) {
	    								finalList.add(fullValList.get(l));
	    								break;
	    							}
	    						}
	    						break;
	    					}
	    				}
    				}
    				else {//add searchList was not proper or searchList same as spec
    					for (int l=0,ls = fullValList == null ? 0 : fullValList.size(); l<ls;l++) {
							if (fullValList.get(l).m_id == v) {
								finalList.add(fullValList.get(l));
								break;
							}
						}
    				} 
    			}//each valListspec
    			if (valListSpec == null || valListSpec.size() == 0) {
    				for (int l=0,ls = fullValList == null ? 0 : fullValList.size(); l<ls;l++) {
    					finalList.add(fullValList.get(l));
					}
        		}
    			if (finalList.size() == 0) {
    				entry.useThisForGps = null;
    				continue;
    			}
    			//HACK - Any is getting lost .. if (valListSpecHasAny)
    				finalList.add(null);
    			retval = true;
    			dci.m_dataSpan = finalList.size();
    			entry.useThisForGps = finalList;
    		}//if was a grouped dim 
    	}//for each dci
		return retval;
	}
	
	public boolean hasNestedGroupHeader(ArrayList<DimConfigInfo> fpList) {
		for (int i=0,is=fpList == null ? 0 : fpList.size();i<is;i++) {
    		DimConfigInfo dci = fpList.get(i);
    		if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_groupBy != null && dci.m_dimCalc.m_groupBy.size() > 0)
    			return true; 
    	}
    	return false;
	}
	 public boolean hasNestedColHeader(ArrayList<DimConfigInfo> fpList) {
	    	for (int i=0,is=fpList == null ? 0 : fpList.size();i<is;i++) {
	    		DimConfigInfo dci = fpList.get(i);
	    		if (dci != null && dci.m_dataSpan > 1)
	    			return true;
	    	}
	    	return false;
	    }
	public void printTable(Connection conn, HashMap<String, String> filterCriteriaMap, StringBuilder sb, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, String sessionMethodToInvoke) throws Exception{
		ArrayList <VOInterface> trackRegionInfoVOList = null;
		CacheAccessorRemote beanRemote = null;
		try	{ 
//			Properties env = new Properties();
//			env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
//			env.put("java.naming.provider.url", "jnp://localhost:1099");
//			env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
//			env.put("jnp.socket.Factory", "org.jnp.interfaces.TimedSocketFactory");
//			InitialContext ctx = new InitialContext(env);
//			CacheAccessorRemote beanRemote = (CacheAccessorRemote)ctx.lookup("CacheAccessor/remote");
			InitialContext context = new InitialContext(PropertyManager.getProperties());
			beanRemote = (CacheAccessorRemote)context.lookup(PropertyManager.getString("trip.remote.jndi"));
		} catch (Exception e)	{
			e.printStackTrace();
//			throw new RuntimeException(e);		
		}
		try{
			if("processTripSummary".equalsIgnoreCase(sessionMethodToInvoke)){
				GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
	            String outStr = qb.printDashBoard(session.getConnection() , fpi, session, searchBoxHelper, null, null);
	            sb.append(outStr);
	            return;
			}else if ("summaryReport".equalsIgnoreCase(sessionMethodToInvoke)){
				String valList = filterCriteriaMap.get("20210");
				if ( valList != null ){
				ArrayList<Integer> list = new ArrayList<Integer>();
				String[] valArray = valList.split(",");
				for ( int i = 0; i < valArray.length ; i++){
					int intValue = Misc.getParamAsInt(valArray[i]);
					if ( intValue != Misc.getUndefInt() ){
						list.add(intValue);
					}
				}
				 
				ArrayList<Integer> materialVector = new ArrayList<Integer>();
				String materialList = filterCriteriaMap.get("20451");
				if ( materialList != null && !"-1000".equalsIgnoreCase(materialList) ){
					Misc.convertValToVector(materialList, materialVector);
				}
				
				trackRegionInfoVOList = beanRemote.getTrackRegionDetails(list);
				trackRegionInfoVOList = filterSpecificMaterial(materialVector,trackRegionInfoVOList);
				
				// subraction of region sets;
				valList = filterCriteriaMap.get("120210");
				ArrayList<Integer> exclusiveRegionIds = new ArrayList<Integer>();
				Misc.convertValToVector(valList, exclusiveRegionIds);
				ArrayList<Pair<Integer,ArrayList<Integer>>> depList = DashboardDao.getContainingAreas(null,valList);
				ArrayList<Integer> distinctOpIdList = getDistinctOpIdList(depList);
				if ( distinctOpIdList.size() == 0){
					distinctOpIdList = exclusiveRegionIds;
				}
				ArrayList<VOInterface> tempVOList = beanRemote.getTrackRegionDetails(distinctOpIdList);;
				tempVOList = filterSpecificMaterial(materialVector, tempVOList);
				
				tempVOList = subtractOpStation(tempVOList,depList);
				trackRegionInfoVOList.addAll(tempVOList);
				} else {
					//sessionMethodToInvoke = "trackRegionPerformance";
					try{
					trackRegionInfoVOList = beanRemote.getTrackRegionPerformance(filterCriteriaMap);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// System.out.println("DashboardWriter.printTable() trackRegionInfoVOList + " + trackRegionInfoVOList);
				}
				
			} else 	if("processDetentionReport".equalsIgnoreCase(sessionMethodToInvoke)){
				try{
					trackRegionInfoVOList = beanRemote.getDetentionInfo(filterCriteriaMap); 	
					} catch (Exception e) {
						e.printStackTrace();
					}
			}else if("processDetentionDetails".equalsIgnoreCase(sessionMethodToInvoke)){
				int forId = Misc.getParamAsInt(session.getParameter("forId"), 9066);
				String opStationId = Misc.getParamAsString(session.getParameter("home9029"), "18");
				filterCriteriaMap.put("home9029", opStationId);
				trackRegionInfoVOList = beanRemote.getDetentionInfo(filterCriteriaMap);
				ArrayList<Pair<Integer, LUInfoExtract>> lu = null;
				if (trackRegionInfoVOList != null && trackRegionInfoVOList.size() != 0 && trackRegionInfoVOList.get(0) != null) {
					TrackRegionInfoVO trackRegionInfoVO = (TrackRegionInfoVO) trackRegionInfoVOList.get(0);
					Pair<Integer, ArrayList<Pair<Integer, LUInfoExtract>>> vehicleInDetention = getDetainedVehiclesInBound(forId, trackRegionInfoVO.getVehiclesAssociated(), session, searchBoxHelper, Misc.getUndefInt(), Misc.getUndefInt());
					if (vehicleInDetention != null)
						lu = vehicleInDetention.second;	
				}
				
				//List <Pair<Integer, LUInfoExtract>> lu = regionInfo.get(opStationId+"_"+forId);
				trackRegionInfoVOList = new ArrayList<VOInterface>();
				ArrayList<Integer> v9003Int = new ArrayList<Integer>(); //vehicle_type
				Misc.convertValToVector(filterCriteriaMap.get("9003"), v9003Int);
				boolean doFilterVehicle = v9003Int.contains(-1000);
				DetentionVO tempVo = null;
				if (lu != null) {
					for (Pair<Integer, LUInfoExtract> pair : lu) {
						tempVo = new DetentionVO();
						tempVo.setVehicleId(pair.first);
						CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(pair.first, session.getConnection());
						if ( !doFilterVehicle && !v9003Int.contains(vehSetup.m_type)){
							continue;
						}
						tempVo.setVehicleName(vehSetup.m_name);
						tempVo.setContractor(vehSetup.m_customer_name);
						VehicleDataInfo vehicleDataInfo = VehicleDataInfo.getVehicleDataInfo(conn, vehSetup.m_vehicleId, false, false);
						NewVehicleData vdp = vehicleDataInfo != null ? vehicleDataInfo.getDataList(conn, vehSetup.m_vehicleId, 0, false) : null;
						GpsData latestGpsData = vdp == null ? null : vdp.getLast(conn);
						long latestGpsRecordTime = latestGpsData == null ? Misc.getUndefInt() : latestGpsData.getGps_Record_Time();
						if(pair.second != null && !Misc.isUndef(pair.second.getWaitIn()) && !Misc.isUndef(latestGpsRecordTime)){
							long totTime = (latestGpsRecordTime - pair.second.getWaitIn())/(60*1000);
							long hr = totTime/60;
							int min = (int)totTime%60;
							tempVo.setDetentionTime((hr == 0 ? "00" : ""+hr) + ":" + (min == 0 ? "00" : min < 10 ? "0"+min : ""+min));
						}
						if(pair.second != null && latestGpsData != null) {
							String s = latestGpsData.getName(session.getConnection(),pair.first, vehSetup);
							if (s != null)
								tempVo.setLocation(s) ;
							//pair.second.latestGpsData.setName(null);
						}
						//tempVo.setVehicleTypeString(session.getCache().getAttribDisplayName(9097, vehSetup.m_type));
						tempVo.setVehicleTypeString(session.getCache().getAttribDisplayNameFull(session, session.getConnection(), DimInfo.getDimInfo(9003),vehSetup.m_type));
						tempVo.setVehicleType(vehSetup.m_type);
						tempVo.setGateIn(Misc.isUndef(pair.second.getWaitIn()) ? null : new Date(pair.second.getWaitIn()));
						tempVo.setGateOut(Misc.isUndef(pair.second.getWaitOut()) ? null : new Date(pair.second.getWaitOut()));
						tempVo.setGpsRecordTime(Misc.isUndef(latestGpsData.getGps_Record_Time()) ? null : new Date(latestGpsData.getGps_Record_Time()));
						trackRegionInfoVOList.add(tempVo);
					}
				}
//				String lower = Misc.getParamAsString(session.getParameter("lower"), "0");
//				String upper = Misc.getParamAsString(session.getParameter("upper"), "3");
//				String key = "";
//				trackRegionInfoVOList = beanRemote.getStrandedVehicle(filterCriteriaMap);
				
			}else 	if("trackRegionPerformance".equalsIgnoreCase(sessionMethodToInvoke)){
				trackRegionInfoVOList = beanRemote.getTrackRegionPerformance(filterCriteriaMap); 	
			}else if("strandedVehicle".equalsIgnoreCase(sessionMethodToInvoke)){
				trackRegionInfoVOList = beanRemote.getStrandedVehicle(filterCriteriaMap); 	
			}else if("vehicleOutsideGate".equalsIgnoreCase(sessionMethodToInvoke)){
				trackRegionInfoVOList = beanRemote.getVehicleOutsideGate(filterCriteriaMap); 	
			}else if("listQueuedVehicles".equalsIgnoreCase(sessionMethodToInvoke)){
				//HACK remove 9003 from this
				//filterCriteriaMap.remove("9003");
				try {
				trackRegionInfoVOList = beanRemote.getListQueuedVehicles(filterCriteriaMap);
				}
				catch (Exception e1) {
					e1.printStackTrace();
					throw e1;
				}
				StringBuilder vehStr = new StringBuilder("");
				ArrayList<Integer> v9003Int = new ArrayList<Integer>(); //vehicle_type
				Misc.convertValToVector(filterCriteriaMap.get("9003"), v9003Int);
				if (v9003Int.size() == 0)
				Misc.convertValToVector(filterCriteriaMap.get("home9003"), v9003Int);
				
				boolean doFilterVehicle = v9003Int.size() > 0 && !v9003Int.contains(-1000);
				if(doFilterVehicle){
					// System.out.println("DashboardWriter.printTable() : v9003Int : "+v9003Int);
				}
				for (int i11=trackRegionInfoVOList.size()-1;i11>=0;i11--) {
					VOInterface voInterface = trackRegionInfoVOList.get(i11);
					if(voInterface instanceof VehicleVO){
						VehicleVO vehicleVO = (VehicleVO)voInterface;
						if(vehicleVO != null && !Misc.isUndef(vehicleVO.getVehicleId())){
							if (doFilterVehicle) {
								try {
									CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleVO.getVehicleId(), session.getConnection());
									if ( doFilterVehicle && !v9003Int.contains(vehSetup.m_type)){
										trackRegionInfoVOList.remove(i11);
										continue;
									}	
								}
								catch (Exception e1) {
									System.out.println("DashboardWriter.printTable() : Error while removing vehicle from list"  );
									e1.printStackTrace();
								}								
							}
							if(vehStr.length() > 0)
								vehStr.append(",");
							vehStr.append(vehicleVO.getVehicleId());
						}
					}
				}
				// System.out.println("DashboardWriter.printTable()  :  vehStr =  "+vehStr);
				
				
				HashMap<Integer, Pair<Date, String>> vehCurrDataMap = DashboardDao.getVehicleCurrentData(session.getConnection(), vehStr.toString());
				for (VOInterface voInterface : trackRegionInfoVOList) {
					if(voInterface instanceof VehicleVO){
						VehicleVO vehicleVO = (VehicleVO)voInterface;
						if(vehicleVO != null && !Misc.isUndef(vehicleVO.getVehicleId())){
							CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleVO.getVehicleId(), session.getConnection());
							Pair<Date, String> pair = vehCurrDataMap.get(vehicleVO.getVehicleId());
							if(pair != null){
								VehicleDataInfo vehicleDataInfo = VehicleDataInfo.getVehicleDataInfo(conn, vehSetup.m_vehicleId, false, false);
								NewVehicleData vdp = vehicleDataInfo != null ? vehicleDataInfo.getDataList(conn, vehSetup.m_vehicleId, 0, false) : null;
								GpsData latestGpsData = vdp == null ? null : vdp.getLast(conn);
								long latestGpsRecordTime = latestGpsData == null ? Misc.getUndefInt() : latestGpsData.getGps_Record_Time();

								if(vehicleVO.getLuInfoExtract() != null && latestGpsData != null ){
									// System.out.println("DashboardWriter.printTable() : name :  " + vehicleVO.getLuInfoExtract().latestGpsData.getName(session.getConnection(), vehicleVO.getVehicleId()));
									// System.out.println("DashboardWriter.printTable() : time :  " + vehicleVO.getLuInfoExtract().latestGpsData.getGps_Record_Time());
								}
							// System.out.println("DashboardWriter.printTable()  vehCurrDataMap  : pair.first  :  "+pair.first+ " : pair.second : "+pair.second);
							vehicleVO.setCurrentGpsRecordTime(pair.first);
							vehicleVO.setCurrentLocation(pair.second);
							vehicleVO.setVehicleType(vehSetup.m_type);
							}
						}
					}
				}
				
//				HashMap<Integer, Pair<Date, String>> vehCurrDataMap = DashboardDao.getVehicleCurrentData(session.getConnection(), vehStr.toString());
//				for (VOInterface voInterface : trackRegionInfoVOList) {
//					if(voInterface instanceof VehicleVO){
//						VehicleVO vehicleVO = (VehicleVO)voInterface;
//						if(vehicleVO != null && !Misc.isUndef(vehicleVO.getVehicleId())){
//							CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleVO.getVehicleId(), session.getConnection());
//							Pair<Date, String> pair = vehCurrDataMap.get(vehicleVO.getVehicleId());
//							if(pair != null){
//							System.out.println("DashboardWriter.printTable()  vehCurrDataMap  : pair.first  :  "+pair.first+ " : pair.second : "+pair.second);
//							vehicleVO.setCurrentGpsRecordTime(pair.first);
//							vehicleVO.setCurrentLocation(pair.second);
//							vehicleVO.setVehicleType(vehSetup.m_type);
//							}
//						}
//					}
//				}
			}else if("TripInfoDetail".equalsIgnoreCase(sessionMethodToInvoke)){
				String v9021s = filterCriteriaMap.get("20022");
				// System.out.println("DashboardWriter.printTable()  TripInfoDetail  :  filterCriteriaMap.get(20022)   :  "+v9021s);
				String Id_20022 = Misc.getParamAsString(session.getParameter("home20022"),null);
				// System.out.println("DashboardWriter.printTable()   : Id_20022 :  "+ Id_20022);
				String Id_9064 = Misc.getParamAsString(session.getParameter("home9064"),null);
				// System.out.println("DashboardWriter.printTable()   : Id_9064 :  "+ Id_9064);
				TripInfoBean tripInfoBean = DashboardDao.getTripInfo(session.getConnection(), Misc.getParamAsInt(Id_20022), Misc.getParamAsString(Id_9064));	
				trackRegionInfoVOList = new ArrayList<VOInterface>();
				trackRegionInfoVOList.add(tripInfoBean);
			}
				
		} catch (Exception e)	{
			e.printStackTrace();
//			throw new RuntimeException(e);		
		}
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		boolean hasNestedGroup = this.hasNestedColHeader(fpList);
		if (hasNestedGroup) {
			fpList = (ArrayList<DimConfigInfo>) fpList.clone();
			hasNestedGroup = populateGroupByAndConfirmNestedGroup(fpList, session.getConnection(), session, searchBoxHelper);
		}
		int cols = 0;
		int type = 0;
		String disp = null;
		String dispID = null;
		String dispIDTemp = null;
		if(fpList != null)
			cols = fpList.size();
		FormatHelper formatHelper = getFormatHelper(fpList, session, searchBoxHelper);
				
		try {
			if(trackRegionInfoVOList != null && trackRegionInfoVOList.size() != 0){
				for (VOInterface voInterface : trackRegionInfoVOList) {
					sb.append("<tr>");
					for(int i=0; i<cols; i++){
						DimConfigInfo dci = fpList.get(i);
						if (dci.m_hidden){
							dispIDTemp = dispID;
							dispID = getDisp(voInterface, dci, formatHelper, i, session, searchBoxHelper, Misc.getUndefInt(), Misc.getUndefInt(), null);
							continue;
						}
						//all the formatting & scaling being moved to resultInfo.getValStr();
						ArrayList<DimInfo.ValInfo> groupingList = null;
						int groupingDim = Misc.getUndefInt();
						if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_groupBy != null && dci.m_dimCalc.m_groupBy.size() > 0) {
							ArrayList<DimInfo.DimValList> groupList = dci.m_dimCalc.m_groupBy;
							DimInfo.DimValList entry = groupList.get(0);
							if (entry.useThisForGps != null && entry.useThisForGps.size() > 0) {
								groupingList = entry.useThisForGps;
								groupingDim = entry.m_dimInfo.m_id;
							}
						}
						int groupingListSize = groupingList == null ? 0 : groupingList.size();
						for (int g=0,gs = groupingListSize == 0 ? 1 : groupingListSize; g<gs;g++) {
							DimInfo.ValInfo groupingValInfo = g<groupingListSize ? groupingList.get(g) : null;
							ArrayList<Integer> hackListOfVeh = new ArrayList<Integer>();//will get list of vehicles meant for vehicle display
							disp = getDisp(voInterface, dci, formatHelper, i, session, searchBoxHelper, groupingDim, groupingValInfo == null ? Misc.getUndefInt() : groupingValInfo.m_id, hackListOfVeh);
							
							boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);			
							type = dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.m_type;
							boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;					
	
							String link = isNull ? null : dci.m_linkHelper == null ? null : generateLink(dci, fpList, dispID, searchBoxHelper.m_topPageContext, session);
							if(link != null){
								// System.out.println("DashboardWriter.printTable()   :  link  :  "+link);
								// System.out.println("DashboardWriter.printTable()   :  dispIDTemp  :  "+dispIDTemp);
								String link2 = link+"&home9064="+dispIDTemp;
								// System.out.println("DashboardWriter.printTable()   :  link2  :  "+link2);
								if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo.m_id == 9035)
									link = link2;
								// System.out.println("DashboardWriter.printTable()   :  link (modified)  :  "+link);
							}
							if (link != null && hackListOfVeh.size() > 0) {
								StringBuilder s1 = new StringBuilder();
								s1.append("&vehicle_id=");
								Misc.convertInListToStr(hackListOfVeh, s1);
								link += s1;
							}
							if (!Misc.isUndef(groupingDim) && groupingValInfo != null)
								link += "&home"+groupingDim+"="+groupingValInfo.m_id;
							String cellClass = doNumber ? "nn" : "cn";
							sb.append("<td class='").append(cellClass).append("'>");
							if(dci.m_show_modal_dialog){
								if (link != null) {
	//								sb.append("<a href=\"javascript:popShowModalDialogDashborad('").append(link).append("')\">");
									sb.append("<a href=\"javascript:beginLoad('" 
	                                        //"javascript:popShowModalDialogDashborad('"
	                                        ).append(link).append("')\">");      
	
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
							sb.append("</td>");
						}
					}//end of while
					sb.append("</tr>");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}	
	}
	private ArrayList<VOInterface> filterSpecificMaterial(ArrayList<Integer> materialVector, ArrayList<VOInterface> trackRegionInfoVOList) {
		if ( materialVector == null || materialVector.size() == 0 ){
			return trackRegionInfoVOList;
		}
		for ( int i = 0 ; i < trackRegionInfoVOList.size(); i++){
			TrackRegionInfoVO tri = (TrackRegionInfoVO) trackRegionInfoVOList.get(i);
			List<Pair<Integer, LUInfoExtract>> vehiclesAssociated = tri.getVehiclesAssociated();
			ArrayList<Pair<Integer, LUInfoExtract>> newVehicleAssociatedList = new ArrayList<Pair<Integer,LUInfoExtract>>(); 
			for (int j = 0 ; j < vehiclesAssociated.size(); j++){
				Pair<Integer,LUInfoExtract> pair = vehiclesAssociated.get(j);
				if ( materialVector.contains(pair.second.getMaterialId()) ){
					newVehicleAssociatedList.add(pair);
				}
			}
			tri.setQueueLength(newVehicleAssociatedList.size());
		}
		return trackRegionInfoVOList;
	}
	private ArrayList<VOInterface> subtractOpStation(ArrayList<VOInterface> tempVOList, ArrayList<Pair<Integer, ArrayList<Integer>>> depList) {
		ArrayList<VOInterface> retval = new ArrayList<VOInterface>();
		for ( int i = 0 ; i < depList.size(); i++ ){
			Pair<Integer, ArrayList<Integer>> pair = depList.get(i);
			TrackRegionInfoVO baseTrackRegion = (TrackRegionInfoVO) getTrackRegion(tempVOList,pair.first);
			ArrayList<Integer> containedRegions = pair.second;
			for ( int j = 0; j < containedRegions.size();j++){
				TrackRegionInfoVO containedRegion = (TrackRegionInfoVO) getTrackRegion(tempVOList,containedRegions.get(j));//tempVOList.get(containedRegions.get(j));
				baseTrackRegion.setQueueLength(baseTrackRegion.getQueueLength() - containedRegion.getQueueLength());
			}
			retval.add(baseTrackRegion);
		}
		return retval;
	}
	private TrackRegionInfoVO getTrackRegion(ArrayList<VOInterface> tempVOList, int value) {
		for ( int i = 0; i < tempVOList.size();i++){
			TrackRegionInfoVO track = (TrackRegionInfoVO) tempVOList.get(i);
			if ( track.getOpStationId() == value){
				return track;
			}
		}
		return null;
	}
	private ArrayList<Integer> getDistinctOpIdList(ArrayList<Pair<Integer, ArrayList<Integer>>> depList) {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		for ( int i = 0; i < depList.size(); i++){
			ArrayList<Integer> child = depList.get(i).second;
			for( int j = 0; j < child.size() ; j++){
				if ( !retval.contains(child.get(j))){
					retval.add(child.get(j));
				}
			}
			retval.add(depList.get(i).first);
		}
		
		
		return retval;
	}
	public void printTableForAcc(Connection conn,HashMap<String, String> filterCriteriaMap, StringBuilder sb, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, String sessionMethodToInvoke) throws Exception{
		ArrayList <VOInterface> trackRegionInfoVOList = null;
		Date sysDate = new java.util.Date();
		long desGapForCurrMS = 60*60*1000;
		sysDate.setTime(sysDate.getTime()-desGapForCurrMS);
		
		//if((sysDate.getHours()-1) < 0){
		//	sysDate.setDate(sysDate.getDate()-1);
		//	sysDate.setHours(23);
		//}
		//else
		//	sysDate.setHours(sysDate.getHours()-1);
		
		int pv123 = Misc.getParamAsInt(filterCriteriaMap.get("pv123"), Misc.G_TOP_LEVEL_PORT);
		Triple<Integer, Integer, Date> t = Common.getShiftAndScheduleInfoIdReplWithTripGet(conn, sysDate, Misc.getUndefInt(), pv123);
		ArrayList<ShiftDashBoardBean> tripInfoForShiftList = DashboardDao.getTripInfoForShift(conn, t.third, pv123);
		ArrayList<ShiftDashBoardBean> tripInfoForCurrentList = DashboardDao.getTripInfoForShift(conn, sysDate, pv123);
		ArrayList<ShiftDashBoardBean> shiftInfoList = DashboardDao.getShiftInfo(conn, t.second);
		ShiftDashBoardBean sBean = null;
		double shiftDurHr =( double) (sysDate.getTime()-t.third.getTime()+desGapForCurrMS)/(3600000.0);
		double currDurHr =( double) (desGapForCurrMS)/(3600000.0);
		if (Misc.isEqual(shiftDurHr,0))
			shiftDurHr = 1.0;
		//get total planned, avg, cumm so that we can get the ratios
		int totTripsPlanned = 0;
		int totTripsCumm = 0;
		int totTripsCurr = 0;
		for (int i=0,is =tripInfoForShiftList.size();i<is;i++)
			totTripsCumm +=  tripInfoForShiftList.get(i).getNoOfTripsNow();
		if (totTripsCumm == 0)
			totTripsCumm = 1;
		
		for (int i=0,is =tripInfoForCurrentList.size();i<is;i++)
			totTripsCurr +=  tripInfoForCurrentList.get(i).getNoOfTripsNow();
		if (totTripsCurr == 0)
			totTripsCurr = 1;
		
		for (int i=0,is =shiftInfoList.size();i<is;i++)
			totTripsPlanned +=  shiftInfoList.get(i).getNoOfTripsNow();
		if (totTripsPlanned == 0)
			totTripsPlanned = 1;
		
		int len = tripInfoForShiftList.size();
		for (int i = 0,j = 0, k = 0; i < len; i++) {
			ShiftDashBoardBean shiftDashBoardBean = tripInfoForShiftList.get(i);
			shiftDashBoardBean.setAvgRatioCumm((double)shiftDashBoardBean.getNoOfTripsNow()/(double)totTripsCumm);
			
			if((j < tripInfoForCurrentList.size()) && (shiftDashBoardBean.getOpStationId() == tripInfoForCurrentList.get(j).getOpStationId())){
				sBean = tripInfoForCurrentList.get(j);
				shiftDashBoardBean.setTripsPerHourCurr(sBean.getNoOfTripsNow()); //will be replaced with calc later
				shiftDashBoardBean.setAvgRoundTripCurr(sBean.getAvgRoundTripCumm()); 
				shiftDashBoardBean.setAvgRatioCurr((double)sBean.getNoOfTripsNow()/(double)totTripsCurr);
				j++;
			}
			if((k < shiftInfoList.size()) && (shiftDashBoardBean.getOpStationId() == shiftInfoList.get(k).getOpStationId())){
				sBean = shiftInfoList.get(k);
				shiftDashBoardBean.setNoOfTripsTargeted(sBean.getNoOfTripsTargeted());
				shiftDashBoardBean.setNoOfVehiclesAssignedPlanned(sBean.getNoOfVehiclesAssignedPlanned());
				shiftDashBoardBean.setAvgRatioTargeted((double)sBean.getNoOfTripsNow()/(double)totTripsPlanned);
				k++;
			}
			shiftDashBoardBean.setTripsPerHourCumm((double)shiftDashBoardBean.getNoOfTripsNow()/shiftDurHr);
			shiftDashBoardBean.setTripsPerHourCurr((double)shiftDashBoardBean.getTripsPerHourCurr()/currDurHr);
            			
			//shiftDashBoardBean.setAvgRoundTripCumm(numer/shiftDashBoardBean.getNoOfTripsNow());
		}
		
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		int type = 0;
		String disp = null;
		String dispID = null;
		if(fpList != null)
			cols = fpList.size();
		FormatHelper formatHelper = getFormatHelper(fpList, session, searchBoxHelper);
				
		try {
			if(tripInfoForShiftList != null && tripInfoForShiftList.size() != 0){
				for (ShiftDashBoardBean shiftDashBoardBean : tripInfoForShiftList) {
					sb.append("<tr>");
					for(int i=0; i<cols; i++){
						DimConfigInfo dci = fpList.get(i);
						if (dci.m_hidden){
							dispID = getDispShift(shiftDashBoardBean, dci, formatHelper, i, session);
							continue;
						}
						//all the formatting & scaling being moved to resultInfo.getValStr();
						disp = getDispShift(shiftDashBoardBean, dci, formatHelper, i, session);

						boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);					
						type =  dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.m_type; 
						boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;					

						String link = isNull ? null : generateLink(dci, fpList, dispID, searchBoxHelper.m_topPageContext, session);
						

						String cellClass = doNumber ? "nn" : "cn";
						sb.append("<td class='").append(cellClass).append("'>");
						if(dci.m_show_modal_dialog){
							if (link != null) {
//								sb.append("<a href=\"javascript:popShowModalDialogDashborad(").append(link).append(")\">");
								sb.append("<a href=\"javascript:beginLoad('" 
                                        //"javascript:popShowModalDialogDashborad('"
                                        ).append(link).append("')\">");      

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
						sb.append("</td>");					
					}//end of while
					sb.append("</tr>");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}	
	}
	
	public void printTableForTextDashboard(Connection conn,HashMap<String, String> filterCriteriaMap, StringBuilder sb, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, String sessionMethodToInvoke) throws Exception{
		GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
        qb.printPage(conn , fpi, session, searchBoxHelper, null, null, null, null);
	}
	
	public  static class Value { 
		public int m_iVal = Misc.getUndefInt();
		public double m_dVal = Misc.getUndefDouble();
		public String m_strVal = null;
		public java.sql.Timestamp m_dateVal = null;
	}
	private String getDispShift(ShiftDashBoardBean shiftDashBoardBean, DimConfigInfo dci, FormatHelper formatHelper, int index, SessionManager session )throws Exception{
		   if (dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null)
			   return "";
			int key = dci.m_dimCalc.m_dimInfo.m_id;
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
			String retval = null;
			Value val = new Value();
//			if (dimInfo != null ) 
//				retval = toString(new Value(), dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
//			retval = retval == null ? ""+shiftDashBoardBean.getOpStationId() : retval;
//			toString(dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			switch (key) {
			case 9044:
				val.m_iVal = shiftDashBoardBean.getOpStationId();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getOpStationId() : retval;
				return retval;
			case 9045:
				return Misc.getParamAsString(shiftDashBoardBean.getRegionName());
			case 9046:
				val.m_iVal = shiftDashBoardBean.getNoOfVehiclesAssignedPlanned();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getNoOfVehiclesAssignedPlanned() : retval;
				return retval;
			case 9047:
				val.m_iVal = shiftDashBoardBean.getNoOfVehiclesAssignedNow();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getNoOfVehiclesAssignedNow() : retval;
				return retval;
			case 9048:
				val.m_iVal = shiftDashBoardBean.getNoOfTripsTargeted();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getNoOfTripsTargeted() : retval;
				return retval;
			case 9049:
				val.m_iVal = shiftDashBoardBean.getNoOfTripsNow();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getNoOfTripsNow() : retval;
				return retval;
			case 9050:
				val.m_dVal = shiftDashBoardBean.getTripsPerHourCumm();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getTripsPerHourCumm() : retval;
				return retval;
			case 9051:
				val.m_dVal = shiftDashBoardBean.getTripsPerHourCurr();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getTripsPerHourCurr() : retval;
				return retval;
			case 9052:
				val.m_dVal = shiftDashBoardBean.getAvgRoundTripCumm();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getAvgRoundTripCumm() : retval;
				return retval;
			case 9053:
				val.m_dVal = shiftDashBoardBean.getAvgRoundTripCurr();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getAvgRoundTripCurr() : retval;
				return retval;
			case 9054:
				val.m_dVal = shiftDashBoardBean.getAvgRatioTargeted();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getAvgRatioTargeted() : retval;
				return retval;
			case 9055:
				val.m_dVal = shiftDashBoardBean.getAvgRatioCumm();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getAvgRatioCumm() : retval;
				return retval;
			case 9056:
				val.m_dVal = shiftDashBoardBean.getAvgRatioCurr();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+shiftDashBoardBean.getAvgRatioCurr() : retval;
				return retval;
				
			default:
				return "";
			}
		
	}
	public String toString(Value val, DimInfo dimInfo, Pair<Double, Double> multScaleFactor, FmtI.AllFmt formatter, SessionManager session, Cache cache, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
	    String retval = null;
	    
		if (dimInfo != null ) {
			int attribType = dimInfo.getAttribType();
			double addFactor = 0;
			double mulFactor = 1;
			boolean uProfile = false;
			
			
			if (attribType == Cache.LOV_TYPE) {
				retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, val.m_iVal);
			}				
			else if (attribType == Cache.NUMBER_TYPE) {
				double dval = uProfile ? val.m_dVal*mulFactor+addFactor : val.m_dVal;
				if (formatter != null) {
					retval = ((FmtI.Number)formatter).format(dval);
				}
				else {						
					retval = Double.toString(dval);
				}
			}
			else if (attribType == Cache.DATE_TYPE) {
				if (formatter != null) {
					retval = ((FmtI.Date)formatter).format(val.m_dateVal);
				}
				else {
					retval = sdf.format(val.m_dateVal);
				}
			}				
			else if (attribType == Cache.INTEGER_TYPE || attribType == Cache.LOV_NO_VAL_TYPE) {
				int ival = uProfile ?(int) (val.m_iVal*mulFactor+addFactor) : val.m_iVal;
				retval = Integer.toString(ival);
			}				
			else if (attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE ) {
				retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, val.m_iVal);//TODO fully implement this function	
			}							
		}//if valid DimInfo				    
//	    if (retval == null)
//	    	retval = toString();
	    return retval;
   }//end of formatted toString()
	private String getDispNotUsed(VOInterface vOInterface, DimConfigInfo dci, FormatHelper formatHelper, int index, SessionManager session) throws Exception{
		return null;//getDisp(vOInterface, dci, formatHelper, index, session, null);
	}
	
	private String getDisp(VOInterface vOInterface, DimConfigInfo dci, FormatHelper formatHelper, int index, SessionManager session, SearchBoxHelper searchBoxHelper, int groupDim, int groupVal, ArrayList<Integer> hackListOfVeh) throws Exception{
		 if (dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null)
			   return "";
		int key = dci.m_dimCalc.m_dimInfo.m_id;
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
		String retval = null;
		Value val = new Value();
		List <Pair<Integer, LUInfoExtract>> luList = null;
		List <Pair<Integer, LUInfoExtract>> regionInfoLUList = null;
		int count = 0;
		if(vOInterface instanceof TrackRegionInfoVO){
			TrackRegionInfoVO trackRegionInfoVO = (TrackRegionInfoVO)vOInterface;
			switch (key) {
			case 20210:
				// System.out.println("DashboardWriter.getDisp()   case 20210:: "+Misc.getParamAsString(trackRegionInfoVO.getOpStationName()));
				return Misc.getParamAsString(trackRegionInfoVO.getOpStationName());
			case 9064:
				// System.out.println("DashboardWriter.getDisp()   case 9064:: "+Misc.getParamAsString(trackRegionInfoVO.getLatestEventDateTimeStr()));
				return Misc.getParamAsString(trackRegionInfoVO.getLatestEventDateTimeStr());
			case 20022:
				val.m_iVal = trackRegionInfoVO.getTripId();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+trackRegionInfoVO.getTripId() : retval;
				if("".equals(retval))
					retval = ""+trackRegionInfoVO.getTripId();
				// System.out.println("DashboardWriter.getDisp()   case 20022:: "+retval);
				return retval;
			case 9029:
				val.m_iVal = trackRegionInfoVO.getOpStationId();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+trackRegionInfoVO.getOpStationId() : retval;
				if("".equals(retval))
					retval = ""+trackRegionInfoVO.getOpStationId();
				// System.out.println("DashboardWriter.getDisp()   case 9029:: "+retval);
				return retval;
			
			case 9003:
				return session.getCache().getAttribDisplayNameFull(session, session.getConnection(), DimInfo.getDimInfo(9003),trackRegionInfoVO.getVehicleType());
				//return session.getCache().getAttribDisplayName(9003,trackRegionInfoVO.getVehicleType());
			
			case 9030:
				return Misc.getParamAsString(trackRegionInfoVO.getOpStationName());
			case 9031:
				val.m_iVal = trackRegionInfoVO.getOpStationStatus();
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+trackRegionInfoVO.getOpStationStatus() : retval;
				return retval;
//				return Misc.getParamAsString(trackRegionInfoVO.getOperationalStatus());
			case 9032:
				val.m_iVal =trackRegionInfoVO.getQueueLengthExt( groupDim, groupVal, session.getConnection());//getQueueLengthExt(trackRegionInfoVO , groupDim, groupVal, session.getConnection(), hackListOfVeh );
				
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+trackRegionInfoVO.getQueueLength() : retval;
				return retval;
			case 9033:
				val.m_dateVal = trackRegionInfoVO.getUnProcessedVehicleIn() == null ? null : new Timestamp (trackRegionInfoVO.getUnProcessedVehicleIn().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((trackRegionInfoVO.getUnProcessedVehicleIn() == null) ? "" : trackRegionInfoVO.getUnProcessedVehicleIn()) : retval;
				return retval;
//				return ""+ ((trackRegionInfoVO.getUnProcessedVehicleIn() == null) ? "" : trackRegionInfoVO.getUnProcessedVehicleIn());
			case 9034:
				Date fraudLastVehOutAt = null;
				if(trackRegionInfoVO.getLastVehicleOut() == null){
					fraudLastVehOutAt = new Date();
					fraudLastVehOutAt.setTime(fraudLastVehOutAt.getTime() - (7200000 + ((long)(Math.random() * 7200000))));
					trackRegionInfoVO.setLastVehicleOut(fraudLastVehOutAt);
				}
				val.m_dateVal = trackRegionInfoVO.getLastVehicleOut() == null ? null : new Timestamp (trackRegionInfoVO.getLastVehicleOut().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((trackRegionInfoVO.getLastVehicleOut() == null ) ? "" : trackRegionInfoVO.getLastVehicleOut()) : retval;
//				retval = retval == null ? ""+ ((trackRegionInfoVO.getLastVehicleOut() == null ) ? "" : trackRegionInfoVO.getLastVehicleOut()) : retval;
				return retval;
//				return ""+ ((trackRegionInfoVO.getLastVehicleOut() == null ) ? "" : trackRegionInfoVO.getLastVehicleOut());
			case 9035:
				val.m_iVal = trackRegionInfoVO.getLastOutProcessingTime()/60;
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+trackRegionInfoVO.getLastOutProcessingTime() : retval;
				return retval;
//				return ""+trackRegionInfoVO.getLastOutProcessingTime();
			case 9066:
			case 9067:
			case 9068:
			case 22028:
			case 22029:
				luList = trackRegionInfoVO.getVehiclesAssociated();
				Pair<Integer, ArrayList<Pair<Integer, LUInfoExtract>>> vehicleInDetention = getDetainedVehiclesInBound(key, luList, session, searchBoxHelper, groupDim, groupVal);
				regionInfoLUList = vehicleInDetention.second;
				count = vehicleInDetention.first;
				val.m_iVal = count;
				if (false && count != 0) {
					for (Pair<Integer, LUInfoExtract> entry :vehicleInDetention.second) {
						hackListOfVeh.add(entry.first);
					}
				}
				//regionInfo.put(trackRegionInfoVO.getOpStationId()+"_"+key, regionInfoLUList);
				 val.m_iVal = count;
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+count : retval;
				return retval;
			/* old approach
			case 9066:
				 luList = trackRegionInfoVO.getVehiclesAssociated();
				 regionInfoLUList = new ArrayList<Pair<Integer, LUInfoExtract>>();
				 count = 0;
				 for (Pair<Integer, LUInfoExtract> pair : luList) {
					if(pair.second != null && pair.second.getWaitIn() != null && pair.second.latestGpsRecordTime != null && (pair.second.latestGpsRecordTime.getTime() - pair.second.getWaitIn().getTime()) < (3*60*60*1000)){
						count++;
						regionInfoLUList.add(pair);
					}
				}
				 regionInfo.put(trackRegionInfoVO.getOpStationId()+"_"+"9066", regionInfoLUList);
				 val.m_iVal = count;
					if (dimInfo != null ) 
						retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
					retval = retval == null ? ""+count : retval;
					return retval;
			case 9067:
				 luList = trackRegionInfoVO.getVehiclesAssociated();
				 regionInfoLUList = new ArrayList<Pair<Integer, LUInfoExtract>>();
				 count = 0;
				 for (Pair<Integer, LUInfoExtract> pair : luList) {
					if(pair.second != null && pair.second.getWaitIn() != null && pair.second.latestGpsRecordTime != null && ((pair.second.latestGpsRecordTime.getTime() - pair.second.getWaitIn().getTime()) >= (3*60*60*1000) && (pair.second.latestGpsRecordTime.getTime() - pair.second.getWaitIn().getTime()) < (6*60*60*1000))){
						count++;
						regionInfoLUList.add(pair);
					}
				}
				 regionInfo.put(trackRegionInfoVO.getOpStationId()+"_"+"9067", regionInfoLUList);
				 val.m_iVal = count;
					if (dimInfo != null ) 
						retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
					retval = retval == null ? ""+count : retval;
					return retval;
			case 9068:
				 luList = trackRegionInfoVO.getVehiclesAssociated();
				 regionInfoLUList = new ArrayList<Pair<Integer, LUInfoExtract>>();
				 count = 0;
				 for (Pair<Integer, LUInfoExtract> pair : luList) {
					if(pair.second != null && pair.second.getWaitIn() != null && pair.second.latestGpsRecordTime != null && (pair.second.latestGpsRecordTime.getTime() - pair.second.getWaitIn().getTime()) >= (6*60*60*1000)){
						count++;
						regionInfoLUList.add(pair);
					}
				}
				 regionInfo.put(trackRegionInfoVO.getOpStationId()+"_"+"9068", regionInfoLUList);
				 val.m_iVal = count;
					if (dimInfo != null ) 
						retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
					retval = retval == null ? ""+count : retval;
					return retval;
					*/
			default:
				return "";
			}
		}else if(vOInterface instanceof StrandedVehicleVO){
			StrandedVehicleVO strandedVehicleVO = (StrandedVehicleVO)vOInterface;
			switch (key) {
			case 9036:
				return Misc.getParamAsString(strandedVehicleVO.getVehicleName());
			case 9030:
				return Misc.getParamAsString(strandedVehicleVO.getOpStationName());
			case 9037:
				val.m_dateVal = strandedVehicleVO.getAtTime() == null ? null : new Timestamp (strandedVehicleVO.getAtTime().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+((strandedVehicleVO.getAtTime() == null) ? "" : strandedVehicleVO.getAtTime()) : retval;
				return retval;
			case 9038:
				return Misc.getParamAsString(strandedVehicleVO.getPhone());
			
			default:
				return "";
			}
		}else if(vOInterface instanceof VehicleOutsideGateVO){
			VehicleOutsideGateVO vehicleOutsideGateVO = (VehicleOutsideGateVO)vOInterface;
			switch (key) {
			case 9036:
				return Misc.getParamAsString(vehicleOutsideGateVO.getVehicleName());
			case 9039:
//				val.m_iVal = trackRegionInfoVO.getType();
//				if (dimInfo != null ) 
//					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
//				retval = retval == null ? ""+trackRegionInfoVO.getType() : retval;
//				return retval;
				return Misc.getParamAsString(vehicleOutsideGateVO.getType());
			case 9038:
				return Misc.getParamAsString(vehicleOutsideGateVO.getPhone());
			case 9030:
				return Misc.getParamAsString(vehicleOutsideGateVO.getOpStationName());
			
			default:
				return "";
			}
		}else if(vOInterface instanceof VehicleVO){
			VehicleVO vehicleVO = (VehicleVO)vOInterface;
			switch (key) {
			case 9065:
				return Misc.getParamAsString(Integer.toString(vehicleVO.getLuInfoExtract().getMaterialId()));
			case 9036:
				return Misc.getParamAsString(vehicleVO.getVehicleName());
			case 9030:
				return Misc.getParamAsString(vehicleVO.getOpStationName());
			case 9003:
				return session.getCache().getAttribDisplayNameFull(session, session.getConnection(), DimInfo.getDimInfo(9003),vehicleVO.getVehicleType());
				//return session.getCache().getAttribDisplayName(9003,vehicleVO.getVehicleType());
			
			case 9033:
				val.m_dateVal = vehicleVO.getLastProcessedTime() == null ? null : new Timestamp (vehicleVO.getLastProcessedTime().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((vehicleVO.getLastProcessedTime() == null) ? "" : vehicleVO.getLastProcessedTime()) : retval;
				return retval;
				
			case 9057:
				val.m_dateVal = Misc.isUndef(vehicleVO.getLuInfoExtract().getWaitIn()) ? null : new Timestamp (vehicleVO.getLuInfoExtract().getWaitIn());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((Misc.isUndef(vehicleVO.getLuInfoExtract().getWaitIn())) ? "" : vehicleVO.getLuInfoExtract().getWaitIn()) : retval;
				return retval;
			
			case 9061:
				// System.out.println("DashboardWriter.getDisp() : 9061 : Current State  :  " + vehicleVO.getLuInfoExtract().getState());
				// System.out.println("DashboardWriter.getDisp() : 9061 : Current LuInfoExtract  :  " + vehicleVO.getLuInfoExtract().toString());
				return Misc.getParamAsString(vehicleVO.getLuInfoExtract().getState());
				
			case 9062:
				val.m_dateVal = vehicleVO.getCurrentGpsRecordTime() == null ? null : new Timestamp (vehicleVO.getCurrentGpsRecordTime().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				// System.out.println("DashboardWriter.getDisp() : 9062 : gps_record_time => " + vehicleVO.getCurrentGpsRecordTime());
				retval = retval == null ? ""+ ((vehicleVO.getCurrentGpsRecordTime() == null) ? "" : vehicleVO.getCurrentGpsRecordTime()) : retval;
				// System.out.println("DashboardWriter.getDisp() : 9062 : retval => " + retval);
				return retval;
				
			case 9063:
				return Misc.getParamAsString(vehicleVO.getCurrentLocation());
			case 9069:	
				CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleVO.getVehicleId(), session.getConnection());
				return Misc.getParamAsString(vehSetup.m_customer_name);
				
			default:
				return "";
			}
		}else if(vOInterface instanceof TripInfoBean){
			TripInfoBean tripInfoBean = (TripInfoBean)vOInterface;
			switch (key) {
			
			case 9002:
				return Misc.getParamAsString(tripInfoBean.getVehicleName());
				
			case 20023:
			val.m_dateVal = tripInfoBean.getShiftDate() == null ? null : new Timestamp (tripInfoBean.getShiftDate().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getShiftDate() == null) ? "" : tripInfoBean.getShiftDate()) : retval;
			return retval;
			
			case 20019:
			val.m_dateVal = tripInfoBean.getWaitInLoad() == null ? null : new Timestamp (tripInfoBean.getWaitInLoad().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getWaitInLoad() == null) ? "" : tripInfoBean.getWaitInLoad()) : retval;
			return retval;	
			
			case 20002:
				val.m_dateVal = tripInfoBean.getGateInLoad() == null ? null : new Timestamp (tripInfoBean.getGateInLoad().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((tripInfoBean.getGateInLoad() == null) ? "" : tripInfoBean.getGateInLoad()) : retval;
				return retval;
			
			case 20004:
			val.m_dateVal = tripInfoBean.getAreaInLoad() == null ? null : new Timestamp (tripInfoBean.getAreaInLoad().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getAreaInLoad() == null) ? "" : tripInfoBean.getAreaInLoad()) : retval;
			return retval;
			
			case 20005:
			val.m_dateVal = tripInfoBean.getAreaOutLoad() == null ? null : new Timestamp (tripInfoBean.getAreaOutLoad().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getAreaOutLoad() == null) ? "" : tripInfoBean.getAreaOutLoad()) : retval;
			return retval;
			
			case 20003:
			val.m_dateVal = tripInfoBean.getGateOutLoad() == null ? null : new Timestamp (tripInfoBean.getGateOutLoad().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getGateOutLoad() == null) ? "" : tripInfoBean.getGateOutLoad()) : retval;
			return retval;
			
			case 20020:
			val.m_dateVal = tripInfoBean.getWaitInUnload() == null ? null : new Timestamp (tripInfoBean.getWaitInUnload().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getWaitInUnload() == null) ? "" : tripInfoBean.getWaitInUnload()) : retval;
			return retval;
			
			case 20012:
			val.m_dateVal = tripInfoBean.getGateInUnload() == null ? null : new Timestamp (tripInfoBean.getGateInUnload().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getGateInUnload() == null) ? "" : tripInfoBean.getGateInUnload()) : retval;
			return retval;
			
			case 20014:
			val.m_dateVal = tripInfoBean.getAreaInUnload() == null ? null : new Timestamp (tripInfoBean.getAreaInUnload().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getAreaInUnload() == null) ? "" : tripInfoBean.getAreaInUnload()) : retval;
			return retval;
			
			case 20015:
			val.m_dateVal = tripInfoBean.getAreaOutUnload() == null ? null : new Timestamp (tripInfoBean.getAreaOutUnload().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getAreaOutUnload() == null) ? "" : tripInfoBean.getAreaOutUnload()) : retval;
			return retval;
			
			case 20013:
			val.m_dateVal = tripInfoBean.getGateOutUnload() == null ? null : new Timestamp (tripInfoBean.getGateOutUnload().getTime());
			if (dimInfo != null ) 
				retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
			retval = retval == null ? ""+ ((tripInfoBean.getGateOutUnload() == null) ? "" : tripInfoBean.getGateOutUnload()) : retval;
			return retval;
			
			
			default:
				return "";
			}
		}else if(vOInterface instanceof DetentionVO){
			DetentionVO vehicleVO = (DetentionVO)vOInterface;
			switch (key) {
			case 9029:
				val.m_iVal = vehicleVO.getOpStationId()/60;
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+vehicleVO.getOpStationId() : retval;
				return retval;
			case 9030:
				return Misc.getParamAsString(vehicleVO.getOpStationName());
			case 9003:
				return session.getCache().getAttribDisplayNameFull(session, session.getConnection(), DimInfo.getDimInfo(9003), vehicleVO.getVehicleType());
				//return session.getCache().getAttribDisplayName(9003,vehicleVO.getVehicleType());
			case 9063:
				return Misc.getParamAsString(vehicleVO.getLocation());
			case 9069:
				return Misc.getParamAsString(vehicleVO.getContractor());
			case 9036:
				return Misc.getParamAsString(vehicleVO.getVehicleName());
			case 9070:
				return Misc.getParamAsString(vehicleVO.getDetentionTime());
			case 9071:
				val.m_dateVal = vehicleVO.getGateIn() == null ? null : new Timestamp (vehicleVO.getGateIn().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((vehicleVO.getGateIn() == null) ? "" : vehicleVO.getGateIn()) : retval;
				return retval;
			case 9072:
				val.m_dateVal = vehicleVO.getGpsRecordTime() == null ? null : new Timestamp (vehicleVO.getGpsRecordTime().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((vehicleVO.getGpsRecordTime() == null) ? "" : vehicleVO.getGpsRecordTime()) : retval;
				return retval;
			case 9073:
				val.m_dateVal = vehicleVO.getGateOut() == null ? null : new Timestamp (vehicleVO.getGateOut().getTime());
				if (dimInfo != null ) 
					retval = toString(val, dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), session, session.getCache(), session.getConnection(), sdf);
				retval = retval == null ? ""+ ((vehicleVO.getGateOut() == null) ? "" : vehicleVO.getGateOut()) : retval;
				return retval;	
			
				
			default:
				return "";
			}
		}
		
		return null;
	}
	private static  int getQueueLengthExt(TrackRegionInfoVO data, int groupDim, int groupVal, Connection conn, ArrayList<Integer> hackListOfVehicles) throws Exception {
		List<Pair<Integer, LUInfoExtract>> vehiclesAssociated = data.getVehiclesAssociated();
			if (Misc.isUndef(groupVal) || groupVal == Misc.G_HACKANYVAL) {
				if (vehiclesAssociated != null && vehiclesAssociated.size() != 0) {
					for (Pair<Integer, LUInfoExtract> entry :vehiclesAssociated) {
						hackListOfVehicles.add(entry.first);
					}
				}
				return data.getQueueLength();
			}
			if (groupDim != 9097 && groupDim != 9003) {
				if (vehiclesAssociated != null && vehiclesAssociated.size() != 0) {
					for (Pair<Integer, LUInfoExtract> entry :vehiclesAssociated) {
						hackListOfVehicles.add(entry.first);
					}
				}
				return data.getQueueLength();
			}
			int retval = 0;
			if (vehiclesAssociated != null && vehiclesAssociated.size() != 0) {
				for (Pair<Integer, LUInfoExtract> entry :vehiclesAssociated) {
					try {
					CacheTrack.VehicleSetup vehInfo = CacheTrack.VehicleSetup.getSetup(entry.first, conn);
					if (vehInfo != null && vehInfo.m_type == groupVal)
						hackListOfVehicles.add(entry.first);
						retval++;
					}
					catch (Exception e1) {
						retval++;
						//eat it;
					}
				}
			}
			return retval;
	}
	
	private Pair<Integer, ArrayList<Pair<Integer, LUInfoExtract>>> getDetainedVehiclesInBound(int key, List<Pair<Integer, LUInfoExtract>>luList, SessionManager session, SearchBoxHelper searchBoxHelper, int groupDim, int groupVal) throws Exception {
		int loMin = Misc.getUndefInt();
		int hiMin = Misc.getUndefInt();
		String ctxt = searchBoxHelper == null ? "home" : searchBoxHelper.m_topPageContext;
		loMin = Misc.getParamAsInt(session.getParameter("lower"));
		hiMin = Misc.getParamAsInt(session.getParameter("upper"));
		if (Misc.isUndef(loMin) && Misc.isUndef(hiMin)) {
			int loDimId = 22030; //default to 9067's
			int hiDimId = 22031;
			switch (key) {
				case 9067:
					loDimId = 22032;
					hiDimId = 22033;
					break;
				case 9068:
					loDimId = 22034;
					hiDimId = 22035;
					break;
				case 22028:
					loDimId = 22036;
					hiDimId = 22037;
					break;
				case 22029:
					loDimId = 22038;
					hiDimId = 22039;
					break;
				default:
					break;
			}
			loMin = Misc.getParamAsInt(session.getParameter(ctxt+loDimId));
			hiMin = Misc.getParamAsInt(session.getParameter(ctxt+hiDimId));
		}
		if (Misc.isUndef(loMin))
			loMin = Integer.MIN_VALUE;
		if (Misc.isUndef(hiMin))
			hiMin = Integer.MAX_VALUE;
		return getDetainedVehiclesInBound(key, luList, loMin, hiMin, groupDim, groupVal, session.getConnection());
	}
	
	private Pair<Integer, ArrayList<Pair<Integer, LUInfoExtract>>> getDetainedVehiclesInBound(int key, List<Pair<Integer, LUInfoExtract>>luList, int loMin, int hiMinExcl, int groupDim, int groupVal, Connection conn) throws Exception {
		ArrayList<Pair<Integer, LUInfoExtract>> regionInfoLUList = new ArrayList<Pair<Integer, LUInfoExtract>>();
		Pair<Integer, ArrayList<Pair<Integer, LUInfoExtract>>> retval = new Pair<Integer, ArrayList<Pair<Integer, LUInfoExtract>>>(0, regionInfoLUList);
		 int count = 0;
		 for (Pair<Integer, LUInfoExtract> pair : luList) {
			CacheTrack.VehicleSetup vehInfo = CacheTrack.VehicleSetup.getSetup(pair.first, conn);
			VehicleDataInfo vehicleDataInfo = VehicleDataInfo.getVehicleDataInfo(conn, vehInfo.m_vehicleId, false, false);
			NewVehicleData vdp = vehicleDataInfo != null ? vehicleDataInfo.getDataList(conn, vehInfo.m_vehicleId, 0, false) : null;
			GpsData latestGpsData = vdp == null ? null : vdp.getLast(conn);
			long latestGpsRecordTime = latestGpsData == null ? Misc.getUndefInt() : latestGpsData.getGps_Record_Time();

			 int gapMin = pair.second != null && !Misc.isUndef(pair.second.getWaitIn()) && !Misc.isUndef(latestGpsRecordTime) ? (int)((latestGpsRecordTime - pair.second.getWaitIn())/(1000 * 60)) : Misc.getUndefInt();
			 if (!Misc.isUndef(gapMin) && gapMin >= loMin && gapMin < hiMinExcl) {
				 boolean toAdd = false;
				 if (groupDim != 9097 && groupDim != 9003) {
					toAdd = true;
				 }
				 else {
					try {
						if (vehInfo != null && vehInfo.m_type == groupVal)
							toAdd = true;
					}
					catch (Exception e1) {
						toAdd = true;;
					}
				}
			 	if (toAdd) {
					count++;
					regionInfoLUList.add(pair);
			 	}
			}
		}
		retval.first = count;
		return retval;
	}
	
	public String generateLink(DimConfigInfo dimConfig, ArrayList<DimConfigInfo> fpList, String rs, String topPageContext, SessionManager session) throws Exception { //will return null if there is no link to be printed
		//sort of duplicate of GeneralizedQueryBuilder.generateLink
       	if (dimConfig == null || dimConfig.m_linkHelper == null)
       		return null;
       	DimConfigInfo.LinkHelper linkHelper = dimConfig.m_linkHelper;   	
       	StringBuilder retval = new StringBuilder();
       	retval.append(linkHelper.m_pagePart);
       	if (linkHelper.m_fixedParamPart != null || linkHelper.m_paramName.size() != 0)
       		retval.append("?");
       	boolean firstParamAdded = false;
       	if (linkHelper.m_fixedParamPart != null) {
       		retval.append(linkHelper.m_fixedParamPart);
       		firstParamAdded = true;		
       	}
       	 
       	for (int i=0,is=linkHelper.m_paramName.size();i<is;i++) {
       		
       		MiscInner.PairStrBool paramName = linkHelper.m_paramName.get(i);
       		MiscInner.PairIntStr paramVal = linkHelper.m_paramValue.get(i);
       		String val = null;
       		if (paramName.second) {//get from search parameter
       			String pn = topPageContext+paramVal.second;
       			val = session.getParameter(pn);
       			if (val == null)
       				val = session.getParameter("pv"+paramVal.second);
       		}
       		else {
	       		if (paramVal.first >= 0) {
	       			val = rs;     			
	       		}
	       		else {
	       			val = paramVal.second;
	       		}
       		}
       		if (val == null)
       			continue;
       		if (firstParamAdded)
       			retval.append("&");
       		
       		retval.append(paramName.first);
       		retval.append("=");
       		retval.append(val);   		  
       	}
       	return retval.toString();   	   	
    }
	
	public static class FormatHelper {	
    	public ArrayList<DimConfigInfo> m_uProfileList;
    	public ArrayList<DimConfigInfo> m_sProfileList;
    	public ArrayList<DimConfigInfo> m_fProfileList; //currently not used	
    	public int m_fFormatSelected; //the formatting entry in d50560
    	public ArrayList<FmtI.AllFmt> formatters = null; // initialized and populated in getFormattersAndMultScale
    	public ArrayList<Pair<Double, Double>> multScaleFactors = null; // initialized and populated in getFormattersAndMultScale
    	public FmtI.AllFmt getFormatter(int index) {
    		return formatters == null || index < 0 || index >= formatters.size() ? null : formatters.get(index);
    	}
    }
    public FormatHelper formatHelper = null;
    
	public static FormatHelper getFormatHelper(ArrayList<DimConfigInfo> colList,  SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper) {
		FormatHelper retval = new FormatHelper();
		int uProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": "pv20501"), 0);
	    int sProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": "pv20530"), 0);
	    int fProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": "pv20560"), 0);
	    retval.m_fFormatSelected = fProfiler;
		Cache cache = session.getCache();
		retval.m_uProfileList = DimConfigInfo.getProfileList(cache.getUnitProfileDef(), uProfiler);
		retval.m_sProfileList = DimConfigInfo.getProfileList(cache.getScaleProfileDef(), sProfiler);
		retval.m_fProfileList = DimConfigInfo.getProfileList(cache.getFormatProfileDef(), fProfiler);
		retval.formatters = new ArrayList<FmtI.AllFmt>();
		retval.multScaleFactors = new ArrayList<Pair<Double,Double>>();
		String lang = "en";
		String country = "IN";
		DimInfo dLocalList = DimInfo.getDimInfo(20560);
		ValInfo dlocalValInfo = dLocalList == null ? null : dLocalList.getValInfo(retval.m_fFormatSelected);
		if (dlocalValInfo != null) {
			lang = dlocalValInfo.getOtherProperty("lang");
			country = dlocalValInfo.getOtherProperty("country");
		}
		
		if (lang == null || lang.length() == 0)
			lang = "en";
		if (country == null | country.length() == 0)
			country = "IN";
		
		Locale locale = new Locale(lang, country);
		for (int i=0,is = colList.size();i<is;i++) {
			DimConfigInfo dc = colList.get(i);
			FmtI.AllFmt toAdd = null;
			Pair<Double, Double> multScale = null;
			if (dc != null && dc.m_dimCalc != null && dc.m_dimCalc.m_dimInfo != null) {
				DimInfo dimInfo = dc.m_dimCalc.m_dimInfo;
				int ty = dimInfo.m_type;
				int subTy = Misc.getParamAsInt(dimInfo.m_subtype);
				DimInfo subTypeDim = DimInfo.getDimInfo(subTy);
				DimConfigInfo unitprofile = DimConfigInfo.getProfile(retval.m_uProfileList, subTy);
				DimConfigInfo scaleprofile = DimConfigInfo.getProfile(retval.m_sProfileList, subTy);
				
				
				if (ty == Cache.NUMBER_TYPE) {
					if (subTypeDim != null && unitprofile != null) {
						double addFactor = 0;
						double mulFactor = 1;
						DimInfo.ValInfo valInfo = subTypeDim.getValInfo(unitprofile.m_p_val);
						if (valInfo != null) {
							addFactor = Misc.getParamAsDouble(valInfo.getOtherProperty("add_factor"));
							mulFactor = Misc.getParamAsDouble(valInfo.getOtherProperty("multi_factor"));
							multScale = new Pair<Double,Double>(mulFactor, addFactor);
						}
					}
					if (subTypeDim != null && scaleprofile != null) {
						double unit = scaleprofile.m_scale;
						int numAfterDec = scaleprofile.m_decimalPrecision;
						FmtI.Number numfmt = new FmtI.Number(locale, unit, numAfterDec);
						toAdd = numfmt;
					}
				}
				else if (ty == Cache.DATE_TYPE) {
					FmtI.Date dtfmt = new FmtI.Date(locale, subTy == 20506);
					toAdd = dtfmt;
				}
				else {
					//do nothing - no formatting
				}				
			}
			retval.formatters.add(toAdd);
			retval.multScaleFactors.add(multScale);
		}
		return retval;
	}
//	public String toString() {//just value based
//		return this.m_type == Cache.INTEGER_TYPE ? Integer.toString(this.m_iVal) : 
//				 this.m_type == Cache.NUMBER_TYPE ? Double.toString(this.m_dVal) :
//				 this.m_type == Cache.STRING_TYPE ? m_strVal :
//			     Misc.m_indepFormatterFull.format(m_dateVal)
//				 ;
//	}
	//public String toString(DimInfo dimInfo, ArrayList uProfileList, Cache cache, SessionManager session, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
	
	public void printTableForAccOld(Connection conn,HashMap<String, String> filterCriteriaMap, StringBuilder sb, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, SessionManager session, String sessionMethodToInvoke) throws Exception{
		ArrayList <VOInterface> trackRegionInfoVOList = null;
		Date sysDate = new java.util.Date();
		if((sysDate.getHours()-1) < 0){
			sysDate.setDate(sysDate.getDate()-1);
			sysDate.setHours(23);
		}
		else
			sysDate.setHours(sysDate.getHours()-1);
		int pv123 = Misc.getParamAsInt(filterCriteriaMap.get("pv123"), Misc.G_TOP_LEVEL_PORT);
		Triple<Integer, Integer, Date> t = Common.getShiftAndScheduleInfoIdReplWithTripGet(conn, sysDate, Misc.getUndefInt(), pv123);
		ArrayList<ShiftDashBoardBean> tripInfoForShiftList = DashboardDao.getTripInfoForShift(conn, t.third, pv123);
		ArrayList<ShiftDashBoardBean> tripInfoForCurrentList = DashboardDao.getTripInfoForShift(conn, sysDate, pv123);
		ArrayList<ShiftDashBoardBean> shiftInfoList = DashboardDao.getShiftInfo(conn, t.second);
		ShiftDashBoardBean sBean = null;
		long denom = (sysDate.getTime() - t.third.getTime())/3600000;
		long numer = denom * 60;
		if(denom == 0)
			denom = 1;
		int len = tripInfoForShiftList.size();
		for (int i = 0,j = 0, k = 0; i < len; i++) {
			ShiftDashBoardBean shiftDashBoardBean = tripInfoForShiftList.get(i);
			if((j < tripInfoForCurrentList.size()) && (shiftDashBoardBean.getOpStationId() == tripInfoForCurrentList.get(j).getOpStationId())){
				sBean = tripInfoForCurrentList.get(j);
				shiftDashBoardBean.setTripsPerHourCurr(sBean.getNoOfTripsNow());
				//shiftDashBoardBean.setAvgRoundTripCurr(sBean.getNoOfVehiclesAssignedNow()); 
				shiftDashBoardBean.setAvgRoundTripCurr(sBean.getAvgRoundTripCumm());
				j++;
			}
			if((k < shiftInfoList.size()) && (shiftDashBoardBean.getOpStationId() == shiftInfoList.get(k).getOpStationId())){
				sBean = shiftInfoList.get(k);
				shiftDashBoardBean.setNoOfTripsTargeted(sBean.getNoOfTripsTargeted());
				shiftDashBoardBean.setNoOfVehiclesAssignedPlanned(sBean.getNoOfVehiclesAssignedPlanned());
				k++;
			}
			shiftDashBoardBean.setTripsPerHourCumm(shiftDashBoardBean.getNoOfTripsNow()/denom);
			shiftDashBoardBean.setAvgRoundTripCumm(numer/shiftDashBoardBean.getNoOfTripsNow());
		}
		
		ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		int cols = 0;
		int type = 0;
		String disp = null;
		if(fpList != null)
			cols = fpList.size();
		FormatHelper formatHelper = getFormatHelper(fpList, session, searchBoxHelper);
				
		try {
			if(tripInfoForShiftList != null && tripInfoForShiftList.size() != 0){
				for (ShiftDashBoardBean shiftDashBoardBean : tripInfoForShiftList) {
					sb.append("<tr>");
					for(int i=0; i<cols; i++){
						DimConfigInfo dci = fpList.get(i);
						if (dci.m_hidden)
							continue;
						//all the formatting & scaling being moved to resultInfo.getValStr();
						disp = getDispShift(shiftDashBoardBean, dci, formatHelper, i, session);

						boolean isNull = disp == null || disp.equals(Misc.emptyString) || disp.equals(Misc.nbspString);					
						type = dci.m_dimCalc.m_dimInfo.m_type;
						boolean doNumber = type == Cache.INTEGER_TYPE || type == Cache.LOV_NO_VAL_TYPE || type == Cache.NUMBER_TYPE;					

						String link = isNull ? null : generateLink(dci, fpList, disp, searchBoxHelper.m_topPageContext, session);


						String cellClass = doNumber ? "nn" : "cn";
						sb.append("<td class='").append(cellClass).append("'>");

						if (link != null) {
							sb.append("<a href=\"").append(link).append("\">");					
						}
						sb.append(disp == null ? "" : disp);
						if (link != null) {
							sb.append("</a>");
						}

						sb.append("</td>");					
					}//end of while
					sb.append("</tr>");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}	
	}
	
}

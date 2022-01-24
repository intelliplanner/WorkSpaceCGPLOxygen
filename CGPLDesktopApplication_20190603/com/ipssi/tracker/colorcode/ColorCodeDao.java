package com.ipssi.tracker.colorcode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.tracker.common.db.DBQueries;

public class ColorCodeDao {
	public static ConcurrentHashMap<String, Integer> g_colorCodeId = new ConcurrentHashMap<String, Integer>();
	public static ConcurrentHashMap<Integer, HashMap<String, ColorCodeBean>> g_colorCodeInfo = new ConcurrentHashMap<Integer, HashMap<String, ColorCodeBean>>();
	private static int REPORT_TYPE_PERFORMANCE=1;
	private static int REPORT_TYPE_DETAILED=2;
	private static int REPORT_TYPE_NORMAL=3;
	public static void saveColorCodeDetail(Connection conn,
			ColorCodeBean colorCodeBean , List colorCodeBeanList, int colorCodeId) {
		
	try {
		PreparedStatement psCheckForDelete = conn.prepareStatement(DBQueries.ColorCode.FETCH_COLORCODE_ID);
		psCheckForDelete.setInt(1, colorCodeBean.getReportType());
		psCheckForDelete.setInt(2, colorCodeBean.getPortNode());
		psCheckForDelete.setInt(3, colorCodeBean.getGranuality());
		psCheckForDelete.setInt(4, colorCodeBean.getAggrigation());
		ResultSet rsDeleteCheck = psCheckForDelete.executeQuery();
		int matchingColorCodeId = Misc.getUndefInt();
		if (rsDeleteCheck.next())
			matchingColorCodeId =rsDeleteCheck.getInt(1);
		rsDeleteCheck.close();
		psCheckForDelete.close();
		
		if (!Misc.isUndef(matchingColorCodeId) && matchingColorCodeId != colorCodeId) {
			PreparedStatement psDelete = conn.prepareStatement(DBQueries.ColorCode.DELETE_COLORCODE);
			PreparedStatement psDelDetail = conn.prepareStatement(DBQueries.ColorCode.DELETE_COLORCODE_DETAIL);
			psDelete.setInt(1,matchingColorCodeId);
			psDelDetail.setInt(1, matchingColorCodeId);
			psDelete.executeUpdate();
			psDelDetail.executeUpdate();
			psDelete.close();
			psDelDetail.close();
		}
		if (!Misc.isUndef(colorCodeId)) {
			PreparedStatement psDelDetail = conn.prepareStatement(DBQueries.ColorCode.DELETE_COLORCODE_DETAIL);
			psDelDetail.setInt(1, colorCodeId);
			psDelDetail.executeUpdate();
			psDelDetail.close();
		}
		PreparedStatement ps = conn.prepareStatement(Misc.isUndef(colorCodeId) ? DBQueries.ColorCode.INSERT_COLORCODE : DBQueries.ColorCode.UPDATE_COLORCODE);
		ps.setInt(1, colorCodeBean.getReportType());
		ps.setString(2,colorCodeBean.getName());
		ps.setString(3, colorCodeBean.getNotes());
		ps.setInt(4,colorCodeBean.getStatus());
		ps.setInt(5,colorCodeBean.getPortNode());
		ps.setInt(6,colorCodeBean.getGranuality());
		ps.setInt(7,colorCodeBean.getAggrigation());
		if (!Misc.isUndef(colorCodeId))
			ps.setInt(8, colorCodeId);
		ps.executeUpdate();
		if (Misc.isUndef(colorCodeId)) {
			ResultSet rs = ps.getGeneratedKeys();
			if(rs.next())
			{
				colorCodeId = rs.getInt(1);
			}
			rs.close();
		}
		ps.close();
		PreparedStatement pst = conn.prepareStatement(DBQueries.ColorCode.INSERT_COLORCODE_DETAIL);
		for(Iterator<ColorCodeBean> itrert = colorCodeBeanList.iterator();itrert.hasNext();)
		{
			ColorCodeBean colorBean = (ColorCodeBean) itrert.next();
			pst.setInt(1,colorCodeId);
			pst.setString(2, colorBean.getColumnName());
			pst.setInt(3,colorBean.getOrder());
			pst.setInt(4, colorBean.getThresholdOne());
			pst.setInt(5,colorBean.getThresholdTwo());
			pst.setInt(6,colorBean.getChkAll());
			pst.executeUpdate();
			
			
		}
		pst.close();
		ColorCodeDao.g_colorCodeId.clear();
		ColorCodeDao.g_colorCodeInfo.clear();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
		
	}
	
	public static ArrayList<ColorCodeBean> getColorCodeDetail(Connection conn , int portNode ,int status)
	{
		ArrayList<ColorCodeBean> colorInfoList = new ArrayList<ColorCodeBean>();
		try {
			PreparedStatement prs  = conn.prepareStatement(DBQueries.ColorCode.FETCH_COLORCODE);
			prs.setInt(1, portNode);
			prs.setInt(2, portNode);
			prs.setInt(3,status);
			ResultSet rs = prs.executeQuery();
			while(rs.next())
			{
				
				ColorCodeBean clrBean = new ColorCodeBean();
				clrBean.setColorCodeId(rs.getInt("id"));
				clrBean.setName(rs.getString("name"));
				clrBean.setReportType(rs.getInt("report_id"));
				clrBean.setNotes(rs.getString("notes"));
				clrBean.setGranuality(rs.getInt("granularity"));
				clrBean.setAggrigation(rs.getInt("aggregation"));
				clrBean.setStatus(rs.getInt("status"));
				colorInfoList.add(clrBean);
			}
			rs.close();
			prs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return colorInfoList;
	}
	public static void getColorCodeInfo (Connection conn , int orgId  , SessionManager session, ArrayList<DimConfigInfo> fpList, SearchBoxHelper searchBoxHelper)
	{
		
		HashMap<String ,ColorCodeBean> colorInfo = null;//new HashMap<String ,ColorCodeBean>();
		
		//------------------------------------------------------------------------------------------------------------------------------		

		
		int cols = 0;
		int type = 0;
		String disp = null;
		String col = null;
		String subType = null;
		String showVal = null;
		if(fpList != null)
			cols = fpList.size();
		Cache cache = session.getCache();
		ResultSet rst = null;
						
		
//------------------------------------------------------------------------------------------------------------------------------		
		
		String reportFile = session.getParameter("front_page");
		 DimInfo reportDim = DimInfo.getDimInfo(20425);
		 int reportId = -1 ;
		 int reportType = -1 ;
		 ArrayList valList = reportDim.getValList();
		    for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) 
		    {
		   	  DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
		    
		         if( valInfo.getOtherProperty("file").equalsIgnoreCase(reportFile))
		         {
		        	 reportId = valInfo.m_id;
		        	 reportType= Misc.getParamAsInt(valInfo.getOtherProperty("type"));
		         }
		         
		    }
		 DimInfo showTimeDim = DimInfo.getDimInfo(reportId == 7 ? 20609 : 20174);
		 String granParamName = searchBoxHelper == null ? "pv20051": searchBoxHelper.m_topPageContext+"20051";
		 String aggParamName = searchBoxHelper == null ? "pv20053": searchBoxHelper.m_topPageContext+"20053";
		 String showTime = searchBoxHelper == null ? "pv"+showTimeDim.m_id : searchBoxHelper.m_topPageContext+showTimeDim.m_id;
			int granDesired = Misc.getParamAsInt(session.getParameter(granParamName));
			int aggDesires = Misc.getParamAsInt(session.getParameter(aggParamName));
			int showTimeVal = Misc.getParamAsInt(session.getParameter(showTime));
			
			System.out.println(granDesired +"********"+ aggDesires+"***********"+showTimeVal );
			
		
		    
		    ArrayList showTimeList = showTimeDim.getValList();
		    for (int i=0,count=showTimeList == null ? 0 : showTimeList.size();i<count;i++) 
		    {
		   	  DimInfo.ValInfo valInfo = (DimInfo.ValInfo) showTimeList.get(i);
		    
		         if( valInfo.m_id == showTimeVal)
		         {
		        	showVal = valInfo.m_name;
		         }
		         
		    }
		    System.out.println("##############"+showVal);
		    StringBuilder key = new StringBuilder();
		    key.append(reportId).append("_").append(orgId);
		    if (REPORT_TYPE_DETAILED==reportType)//1,6,9,10
		    	key.append("_").append("_");
		    else if (REPORT_TYPE_PERFORMANCE==reportType)//0,2,7
		    	key.append("_").append(granDesired).append("_");
		    else if (REPORT_TYPE_NORMAL==reportType)//3,4,5,8
		    	key.append("_").append(granDesired).append("_").append(aggDesires);

		   Integer existingId = ColorCodeDao.g_colorCodeId.get(key.toString());
		   if (existingId != null && !Misc.isUndef(existingId.intValue()))
			   colorInfo = ColorCodeDao.g_colorCodeInfo.get(existingId);
		   if (existingId != null && colorInfo == null){
			   ColorCodeDao.g_colorCodeId.remove(key.toString()); 
		   }
		    if (colorInfo == null) {
				try {
					int colorCodeId = Misc.getUndefInt();
					
					
					PreparedStatement prs  = conn.prepareStatement(REPORT_TYPE_DETAILED==reportType?DBQueries.ColorCode.FETCH_COLORCODE_ID_DETAIL_REPORT: REPORT_TYPE_PERFORMANCE==reportType?DBQueries.ColorCode.FETCH_COLORCODE_ID_PERFORMANC_REPORT :DBQueries.ColorCode.FETCH_COLORCODE_ID_REPORT);
					prs.setInt(1, reportId);
					prs.setInt(2,orgId);
					if(REPORT_TYPE_DETAILED!=reportType) {
						Misc.setParamInt(prs, granDesired, 3);
					}
					//To-DO need to see conditions again and set it properly
					if(REPORT_TYPE_DETAILED!=reportType && REPORT_TYPE_PERFORMANCE!=reportType) {//3,4,5,8
						Misc.setParamInt(prs, granDesired, 4);
						Misc.setParamInt(prs, aggDesires, 5);
						Misc.setParamInt(prs, aggDesires, 6);			
					}
					
					ResultSet rs = prs.executeQuery();
					if (rs.next()) {
						colorCodeId = rs.getInt("id"); 
					}
					rs.close();
					prs.close();
					
					colorInfo = ColorCodeDao.g_colorCodeInfo.get(colorCodeId);
					if (colorInfo != null)
						ColorCodeDao.g_colorCodeId.put(key.toString(), colorCodeId);
					else {
			    		colorInfo = new HashMap<String, ColorCodeBean>(15, 0.75f);
			    		PreparedStatement ps = conn.prepareStatement(DBQueries.ColorCode.FETCH_COLORCODE_DETAIL);
			    		ps.setInt(1, colorCodeId);
			    		rst = ps.executeQuery();
			    		while(rst!=null && rst.next()) {
							ColorCodeBean clrBean = new ColorCodeBean();
							clrBean.setColumnName(rst.getString("column_id"));
							if((REPORT_TYPE_PERFORMANCE!=reportType) && showVal != null)
								clrBean.setColorCode(showVal.equalsIgnoreCase(rst.getString("column_id")));
							clrBean.setIncerasing(rst.getInt("oder") == 0);
							clrBean.setOrder(rst.getInt("oder"));
							clrBean.setThresholdOne(rst.getInt("thresholdone"));
							clrBean.setThresholdTwo(rst.getInt("thresholdtwo"));
							clrBean.setChkAll(rst.getInt("check_for_all"));
							
							colorInfo.put(rst.getString("column_id"), clrBean);
						}
			    		rst.close();
			    		ps.close();
			    	}
					ColorCodeDao.g_colorCodeInfo.put(colorCodeId, colorInfo);
			    	ColorCodeDao.g_colorCodeId.put(key.toString(), colorCodeId);
			    	
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			if(REPORT_TYPE_PERFORMANCE!=reportType)
				session.setAttributeObj("colorBean",colorInfo.get(showVal));
			for(int i=0; i<cols; i++){
				DimConfigInfo dci = fpList.get(i);
				if (dci.m_hidden || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null)
					continue;
				ColorCodeBean clrBean = colorInfo.get(dci.m_dimCalc.m_dimInfo.m_name);
				if (reportId == 2 && clrBean == null)
					clrBean = colorInfo.get(dci.m_name);
				if (clrBean != null) {
					dci.m_color_code = true;
			    dci.m_param1 =clrBean.getThresholdOne();
			    dci.m_param2 = clrBean.getThresholdTwo();
			    dci.m_param1_color = clrBean.getChkAll();
			    dci.m_param2_color = clrBean.getOrder() ;
			   
			//    dci.m_chk_all = clrBean.getChkAll() == 1 || clrBean.getChkAll()==2 ?true:false;
				}else if (dci.m_param3_color <= 0) //dont change params passed
				{
					dci.m_color_code = false;
					dci.m_param1 = -1;
				    dci.m_param2 = -1;
				    dci.m_param1_color = -1;
				    dci.m_param2_color = -1;
				 //   dci.m_chk_all = false;
				}
			
			}

		//return colorInfo;
	}
	
	public static ColorCodeBean getDetail(Connection conn , int colorCodeId)
	{
		ColorCodeBean colorDetailBean = new ColorCodeBean();
		ArrayList<ColorCodeBean> detailColorList = new ArrayList<ColorCodeBean>();
		try {
			PreparedStatement pr  = conn.prepareStatement(DBQueries.ColorCode.SELECT_COLORCODE);
			pr.setInt(1, colorCodeId);
			ResultSet rs = pr.executeQuery();
			if(rs.next())
			{
				colorDetailBean.setName(rs.getString("name"));
				colorDetailBean.setReportType(rs.getInt("report_id"));
				colorDetailBean.setNotes(rs.getString("notes"));
				colorDetailBean.setStatus(rs.getInt("status"));
				colorDetailBean.setPortNode(rs.getInt("port_node_id"));
				colorDetailBean.setGranuality(rs.getInt("granularity"));
				colorDetailBean.setAggrigation(rs.getInt("aggregation"));
				pr.close();
				rs.close();
				pr = conn.prepareStatement(DBQueries.ColorCode.FETCH_DETAIL_COLORCODE);
				pr.setInt(1, colorCodeId);
				rs = pr.executeQuery();
				while(rs.next())
				{
					ColorCodeBean clrBean = new ColorCodeBean();
					clrBean.setColumnName(rs.getString("column_id"));
					//clrBean.setGranuality(rs.getInt("granularity"));
					//clrBean.setAggrigation(rs.getInt("aggrigation"));
					clrBean.setOrder(rs.getInt("oder"));
					clrBean.setThresholdOne(rs.getInt("thresholdone"));
					clrBean.setThresholdTwo(rs.getInt("thresholdtwo"));
					clrBean.setChkAll(rs.getInt("check_for_all"));
					detailColorList.add(clrBean);
				}
				colorDetailBean.setDetailList(detailColorList);
				pr.close();
				rs.close();
			}
			else {
				rs.close();
				pr.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return colorDetailBean;
	}
	

}

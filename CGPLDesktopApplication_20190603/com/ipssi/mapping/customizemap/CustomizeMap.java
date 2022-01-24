package com.ipssi.mapping.customizemap;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.osgeo.mapguide.MgException;
import org.osgeo.mapguide.MgMap;
import org.osgeo.mapguide.MgResourceService;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.mapguideutils.Map;
import com.ipssi.reporting.cache.CacheManager;
import com.ipssi.tracker.landmarkdefinition.LandmarkBean;
import com.ipssi.tracker.landmarkdefinition.LandmarkDefinitionDao;

public class CustomizeMap {

	public static class ForMainFrame {
		public String m_taskPaneURL = null;
		public boolean m_showTaskPane = true;
	}

	public static class ForMapFrame {
		public double m_llExtentX = Misc.getUndefDouble();
		public double m_llExtentY = Misc.getUndefDouble();
		public double m_urExtentX = Misc.getUndefDouble();
		public double m_urExtentY = Misc.getUndefDouble();
		public ArrayList<String> m_addnlLayerNames = null;
		public int m_portNodeId = Misc.UNDEF_VALUE;
	}

	public static ForMainFrame getMainFrameCustomization(HttpServletRequest request, ServletContext application) throws Exception {
		//
		ForMainFrame retval = new ForMainFrame();
		try {
			InitHelper.init(request, application);
			SessionManager session = InitHelper.helpGetSession(request);
			Cache cache = session.getCache();
			User user = session.getUser();
			Connection conn = session.getConnection();
			String appParam = session.getParameter("_app_param");
			if ("".equals(appParam))
				appParam = null;
			if (appParam != null) {
				session.loadAdditionalSessionValuesFromNameValue(appParam);
			}

			String pgContext = Misc.getParamAsString(session.getParameter("page_context"), "tr_map_default");
			String pgTitle = Misc.getParamAsString(session.getParameter("page_name"), "My Map");
			user.loadParamsFromMenuSpec(session, pgContext);

			retval.m_taskPaneURL = Misc.getParamAsString(session.getParameter("task_pane_url"), null);
			retval.m_showTaskPane = Misc.getParamAsString(session.getParameter("_show_task_bar"), null) != null ? Boolean.parseBoolean(session.getParameter("_show_task_bar"))
					: true;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			try  {
				InitHelper.close(request, application);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		return retval;

	}

	public static void doMapCustomization(ForMainFrame instruction, MgMap map, MgResourceService resService) throws Exception {
		// nothing to be done yet
		return;
	}
	
	public static ForMapFrame getMapFrameCustomization(HttpServletRequest request, ServletContext application,String pageContext) throws Exception {
		return getMapFrameCustomization(request,application,1,1,pageContext);
	}
	public static ForMapFrame getMapFrameCustomization(HttpServletRequest request, ServletContext application, int row, int column,String pageContext) throws Exception {
		//
		ForMapFrame retval = new ForMapFrame();
		try {
			//InitHelper.init(request, application);// no need for initialize
			SessionManager session = InitHelper.helpGetSession(request);
			Cache cache = session.getCache();
			User user = session.getUser();
			Connection conn = session.getConnection();
			String appParam = session.getParameter("_app_param");
			if ("".equals(appParam))
				appParam = null;
			if (appParam != null) {
				session.loadAdditionalSessionValuesFromNameValue(appParam);
			}
			int userOrgControlId = Misc.getUserTrackControlOrg(session);
			retval.m_portNodeId = Misc.getParamAsInt(session.getParameter("pv123"),userOrgControlId);
			String pgContext = null;
			if (pageContext != null && pageContext.length() > 0 ) {
				 pgContext = pageContext;
			}else{
			 pgContext = "tr_vehicle_on_map_google";//Misc.getParamAsString(session.getParameter("page_context"), "tr_vehicle_on_map_realtime");
			}
			String pgTitle = Misc.getParamAsString(session.getParameter("page_name"), "My Map");
			user.loadParamsFromMenuSpec(session, pgContext);

			userOrgControlId = retval.m_portNodeId;
			MiscInner.PortInfo userOrgControlOrg = cache.getPortInfo(userOrgControlId, conn);

			// 1. get consts from Org
			// How - in OrgConst.java, set up the id for the field, in corresponding lov put an entry. then do a code something as follows
			// ArrayList<String> webLayoutNames = userOrgControlOrg.getStringParams(OrgConst.ID_WEBLAYOUT_NAME);
			// if (webLayoutNames != null && webLayoutNames.size() == 0) {
			// webLayout = webLayoutNames.get(0);
			// }
			retval.m_addnlLayerNames = userOrgControlOrg.getStringParams(OrgConst.ID_ADDNLLAYER_NAME);

			// 2. get parameters from user specific customization
			//int row = Misc.getParamAsInt(session.getParameter("row"), 1);
			//int col = Misc.getParamAsInt(session.getParameter("col"), 1);
			String configFile = Misc.getParamAsString(session.getParameter("front_page"), "tr_vehicle_on_map_realtime.xml");
			FrontPageInfo frontPageInfo = CacheManager.getFrontPageConfig(conn, user.getUserId(), userOrgControlId, pgContext, configFile, row, column);
			if (frontPageInfo != null) {
				retval.m_llExtentX = Misc.getParamAsDouble(frontPageInfo.getDefaultSearchCriteria(9017));
				retval.m_llExtentY = Misc.getParamAsDouble(frontPageInfo.getDefaultSearchCriteria(9018));
				retval.m_urExtentX = Misc.getParamAsDouble(frontPageInfo.getDefaultSearchCriteria(9019));
				retval.m_urExtentY = Misc.getParamAsDouble(frontPageInfo.getDefaultSearchCriteria(9020));
				// if you want other parameters - that may be comma spearated, see Misc.convertValToArray()
				System.out.println("Complete");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			try  {
				InitHelper.close(request, application);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		return retval;
	}

	public static ArrayList<LandmarkBean> getMapLandmarks(ForMapFrame instruction, MgMap map, MgResourceService resService, 
			HttpServletRequest request, ServletContext application, String sessionId, int prorityIndex)
	throws Exception {
		InitHelper.init(request, application);
		SessionManager session = InitHelper.helpGetSession(request);
		try{
			LandmarkDefinitionDao dao = new LandmarkDefinitionDao(session);
			ArrayList<LandmarkBean> list = dao.fetchLandmarkList(prorityIndex);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<LandmarkBean> getLandmarksMap(ForMapFrame instruction, MgMap map, MgResourceService resService, 
			HttpServletRequest request, ServletContext application, String sessionId, int prorityIndex,int landmark_type)
	throws Exception {
		InitHelper.init(request, application);
		SessionManager session = InitHelper.helpGetSession(request);
		try{
			LandmarkDefinitionDao dao = new LandmarkDefinitionDao(session);
			ArrayList<LandmarkBean> list = dao.fetchLandmarkListforSameOrg(prorityIndex, null, landmark_type);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	
	public static void doMapCustomization(ForMapFrame instruction, MgMap map, MgResourceService resService, HttpServletRequest request, ServletContext application, String sessionId)
			throws Exception {
		// create layers etc. as appropriate, as well as set up other parameters
		try {
			// Map.addLayerToMap(map, resService, ApplicationConstants.ROUTE_DEFINITION_LAYER,
			// true, true, ApplicationConstants.ROUTE_DEFINITION_LAYER,
			// Map.getLayerResourceStringForLibrary(ApplicationConstants.ROUTE_DEFINITION_LAYER, map.GetName() )
			// );
			InitHelper.init(request, application);
			SessionManager session = InitHelper.helpGetSession(request);
			/*
			 * for (int i=0,is=instruction != null && instruction.m_addnlLayerNames != null ? instruction.m_addnlLayerNames.size() : 0;i<is;i++) { String layerName =
			 * instruction.m_addnlLayerNames.get(i); MgResourceIdentifier layerResId = new MgResourceIdentifier(layerName); //Map.addLayerToMap(map, resService, layerName, true,
			 * true, layerResId.GetName(), layerName); if (com.ipssi.map.utils.ApplicationConstants.LANDMARK_LAYER.equalsIgnoreCase(layerResId.GetName())){ LandmarkDefinitionDao
			 * dao = new LandmarkDefinitionDao(session); ArrayList<LandmarkBean> list = dao.fetchLandmarkList(); StringBuffer buffer = new StringBuffer(); for ( int j = 0 ; j <
			 * list.size() ; j++){ if(j!=0){ buffer.append(","); } buffer.append(list.get(j).getId()); } Map.showTheseFeaturesOnMap("id", buffer.toString(), sessionId,
			 * map.GetName(), com.ipssi.map.utils.ApplicationConstants.LANDMARK_LAYER); } else {
			 * 
			 * } int dbg = 1; dbg++; }
			 */
			if (instruction != null && instruction.m_addnlLayerNames != null) {
				Map.addLayerToMap(map, resService, instruction.m_addnlLayerNames);
				
			} 
			try{
					LandmarkDefinitionDao dao = new LandmarkDefinitionDao(session);
					ArrayList<LandmarkBean> list = dao.fetchLandmarkList();
					StringBuilder buffer = new StringBuilder();
					for (int j = 0; j < list.size(); j++) {
						if (j != 0) {
							buffer.append(",");
						}
						buffer.append(list.get(j).getId());
					}
					//System.out.println("CustomizeMap.doMapCustomization()  ++++ == " + buffer.toString());
					Map.showTheseFeaturesOnMap("id", buffer.toString(), sessionId, map.GetName(), com.ipssi.map.utils.ApplicationConstants.LANDMARK_LAYER);
				
			} catch(MgException e1){
				e1.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			try  {
				InitHelper.close(request, application);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
}

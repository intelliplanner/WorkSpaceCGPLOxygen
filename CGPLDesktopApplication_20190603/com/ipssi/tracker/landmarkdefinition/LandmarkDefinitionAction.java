package com.ipssi.tracker.landmarkdefinition;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import com.ipssi.gen.utils.Common;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.geometry.Point;
import com.ipssi.jrm.JRMDeviceMap;
import com.ipssi.mapguideutils.LocalNameHelperRTree;
import com.ipssi.mapguideutils.Map;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.mapguideutils.ReadShapeFile;
import com.ipssi.mapping.common.util.ApplicationConstants;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.tracker.region.RegionDefinitionDao;

public class LandmarkDefinitionAction {
	public static final int DEFAULT_PROIRITY_INDEX = 0;
	private SessionManager m_session = null;

	public LandmarkDefinitionAction(SessionManager m_session) {
		super();
		this.m_session = m_session;
	}

	public ArrayList<LandmarkBean> getLandmarkList(SessionManager m_session) {

		ArrayList<LandmarkBean> landmarkBeanList = new ArrayList<LandmarkBean>();

		try {
			LandmarkDefinitionDao lmDao = new LandmarkDefinitionDao(m_session);
			landmarkBeanList = lmDao.fetchLandmarkList();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return landmarkBeanList;

	}

	public void showThisLandmarkOnMap(String id, String sessionId, String mapName) {
		try {
			//Map.showTheseFeaturesOnMap("id", id, sessionId, mapName, ApplicationConstants.LANDMARK_LAYER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void deleteLandmark(String id,int portNodeId) {
		try {
		LandmarkDefinitionDao lmDao = new LandmarkDefinitionDao(m_session);
		lmDao.deleteLandmarkFromDB(Common.getParamAsInt(id),portNodeId);
		if (!m_session.getConnection().getAutoCommit())
			m_session.getConnection().commit();
		if(true) {//!Utils.isNull(type) && Misc.getParamAsInt(type) == 2) {
			LocalNameHelperRTree.resetJRMRTreeLoaded();
			JRMDeviceMap.setAllRefreshRecalc(true, true);					
		}
		} catch (SQLException e) {
		e.printStackTrace();
		} catch (Exception e) {
		e.printStackTrace();
		}
		}

	/*public void deleteLandmark(String id) {
		try {
			LandmarkDefinitionDao lmDao = new LandmarkDefinitionDao(m_session);
			lmDao.deleteLandmarkFromDB(Common.getParamAsInt(id));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public String saveThisLanmark(String name, String userDescription, String geomText, String portNodeId, String sessionId, String mapName, boolean developGeometry,String destCode,String type,String riskLevel,String category,String startHour,String startMin,String endHour,String endMin,String category_type) {
		String result = "";
		int jrm_id = Misc.getUndefInt();
		try {
			LandmarkBean bean = new LandmarkBean();
			bean = developBean(name, geomText, portNodeId, "", userDescription, developGeometry,destCode,type);
			System.out.print("");
			LandmarkDefinitionDao landmarkDefinitonDao = new LandmarkDefinitionDao(m_session);
			RegionDefinitionDao regionDefinitonDao = new RegionDefinitionDao(m_session);
			int id = landmarkDefinitonDao.insertLandmark(bean);
			
			if(!Utils.isNull(type) && Integer.parseInt(type)==2)
				jrm_id =  regionDefinitonDao.saveJRM(id, type, riskLevel, category, startHour, startMin, endHour, endMin,category_type);
			result = id + "" + jrm_id;
			
			Point p = new Point(Misc.getParamAsDouble(geomText.substring(geomText.indexOf("(") + 1, geomText.indexOf(" "))), Misc.getParamAsDouble(geomText.substring(geomText
						.indexOf(" ") + 1, geomText.indexOf(")"))));
			result = result + "," + p.getX() + "," + p.getY(); 
			
			if (!m_session.getConnection().getAutoCommit())
				m_session.getConnection().commit();
			if(!Utils.isNull(type) && Misc.getParamAsInt(type) == 2) {
				LocalNameHelperRTree.resetJRMRTreeLoaded();
				JRMDeviceMap.setAllRefreshRecalc(true, true);					
			}
		//	landmarkDefinitonDao.updateSpatialLocationName(bean.getPortNodeId(), bean.getLowerPoint());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (result);
	}
	
	public String updateThisLandmark(String id, String name, String userDescription, String geomText, String portNodeId, String sessionId, String mapName, boolean developeGeometry,int oldPortNodeId,String destCode,String type, String riskLevel,String category,String startHour,String startMin, String endHour,String endMin, String category_type) {
		String result = "";
		try {
		System.out.println("In The Update Method $$$$$$$$$$$$$$$$$$$$$$$$$$");
		//ReadShapeFile.loadRTree();
		String description = "";
		LandmarkBean lBean = new LandmarkBean();
		
		lBean = developBean(name, geomText, portNodeId, description, userDescription, developeGeometry,destCode,type);
		lBean.setId(Common.getParamAsInt(id));
		
		LandmarkDefinitionDao landmarkDefinitonDao = new LandmarkDefinitionDao(m_session);
		landmarkDefinitonDao.updateLandmark(lBean,oldPortNodeId);
		RegionDefinitionDao regionDefinitonDao = new RegionDefinitionDao(m_session);
		regionDefinitonDao.updateJRMInDB(Common.getParamAsInt(id), type, riskLevel, category, startHour, startMin, endHour, endMin,category_type);
		// landmarkDefinitonDao.updateSpatialLocationName(lBean.getPortNodeId(), lBean.getLowerPoint());
		if (!m_session.getConnection().getAutoCommit())
			m_session.getConnection().commit();
		if(!Utils.isNull(type) && Misc.getParamAsInt(type) == 2) {
			LocalNameHelperRTree.resetJRMRTreeLoaded();
			JRMDeviceMap.setAllRefreshRecalc(true, true);					
		}
		result = id + "";
		if ( developeGeometry){
		//Map.showTheseFeaturesOnMap("id", id, sessionId, mapName, ApplicationConstants.LANDMARK_LAYER);
		} else {
		Point p = new Point(Misc.getParamAsDouble(geomText.substring(geomText.indexOf("(") + 1, geomText.indexOf(" "))), Misc.getParamAsDouble(geomText.substring(geomText
		.indexOf(" ") + 1, geomText.indexOf(")"))));
		result = result + "," + p.getX() + "," + p.getY();
		}
		} catch (Exception e) {
		e.printStackTrace();
		}
		return result;
		}

	/*public String updateThisLandmark(String id, String name, String userDescription, String geomText, String portNodeId, String sessionId, String mapName, boolean developeGeometry) {
		String result = "";
		try {

			String description = "";
			LandmarkBean lBean = new LandmarkBean();

			lBean = developBean(name, geomText, portNodeId, description, userDescription, developeGeometry);
			lBean.setId(Common.getParamAsInt(id));

			LandmarkDefinitionDao landmarkDefinitonDao = new LandmarkDefinitionDao(m_session);
			landmarkDefinitonDao.updateLandmark(lBean);

			
			landmarkDefinitonDao.updateSpatialLocationName(lBean.getPortNodeId(), lBean.getLowerPoint());
			
			result = id + "";
			if ( developeGeometry){
				Map.showTheseFeaturesOnMap("id", id, sessionId, mapName, ApplicationConstants.LANDMARK_LAYER);
			} else {
				Point p = new Point(Misc.getParamAsDouble(geomText.substring(geomText.indexOf("(") + 1, geomText.indexOf(" "))), Misc.getParamAsDouble(geomText.substring(geomText
						.indexOf(" ") + 1, geomText.indexOf(")"))));
				result = result + "," + p.getX() + "," + p.getY(); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}*/

	private LandmarkBean developBean(String name, String geomText, String portNodeId, String description, String userDescription, boolean developGeometry,String destCode,String landmarkType) throws Exception {
		String geometry = "";
		geometry = geomText;
		LandmarkBean ldBean = new LandmarkBean();
		ldBean.setName(Common.getParamAsString(name));
		ldBean.setPortNodeId(Common.getParamAsInt(portNodeId));
		ldBean.setDescription(Common.getParamAsString(description));
		ldBean.setUserDescription(Common.getParamAsString(userDescription));
		ldBean.setGeometery(Common.getParamAsString(geometry));
		ldBean.setDestCode(destCode);
		ldBean.setLandmarkType(Common.getParamAsInt(landmarkType));
		Point p = new Point(Misc.getParamAsDouble(geomText.substring(geomText.indexOf("(") + 1, geomText.indexOf(" "))), Misc.getParamAsDouble(geomText.substring(geomText
				.indexOf(" ") + 1, geomText.indexOf(")"))));
		ldBean.setLowerPoint(p);
		ldBean.setUpperPoint(p);
		return ldBean;
	}

	public void insertList(ArrayList<LandmarkBean> beanList) {
		LandmarkDefinitionDao ldDao = new LandmarkDefinitionDao(m_session);
		try {
			ldDao.insertList(beanList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveNewLandMarkNameOnly(String txt,int portNodeId){

		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(txt);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();
		List<LandmarkBean> beanList = new ArrayList<LandmarkBean>();
		for (int i = 0; i < size; i++) {
			LandmarkBean ldBean = new LandmarkBean();
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;
			ldBean.setId(Misc.getParamAsInt(e.getAttribute("LandMarkId"))) ;
			ldBean.setName( e.getAttribute("LandMarkName"));
			beanList.add(ldBean);
		}
		LandmarkDefinitionDao landmarkDefinitonDao = new LandmarkDefinitionDao(m_session);
		
		try {
			landmarkDefinitonDao.saveOnlyLandMarkName(beanList,portNodeId);
		} catch (Exception e) {
			 
			e.printStackTrace();
		}
		
	}

	public static void main(String a[]) {
		// LandmarkDefinitionAction abc = new LandmarkDefinitionAction(null);
		// abc.updateThisLandmark("1", "New Landmark", "newn ", "5, 0,0, 0,1, 1,1, 1,0, 0,0", "122", "", "");
		// abc.showThisLandmarkOnMap("1,6", "a9697bb0-ffff-ffff-8000-0024d2117742_en_7F0000010AFC0AFB0AFA", "Sheboygan");
		// abc.getLandmarkList();
	}
}

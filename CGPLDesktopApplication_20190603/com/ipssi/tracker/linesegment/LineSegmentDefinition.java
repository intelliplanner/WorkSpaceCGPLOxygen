package com.ipssi.tracker.linesegment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.osgeo.mapguide.MgEnvelope;


import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Polygon;
import com.ipssi.mapguideutils.Map;
import com.ipssi.mapping.common.util.ApplicationConstants;
import com.ipssi.mapping.ejb.gateway.GpsUtilGateway;
import com.ipssi.tracker.region.RegionBean;
import com.ipssi.tracker.region.RegionDefinitionDao;

public class LineSegmentDefinition {

	private SessionManager m_session = null;

	public LineSegmentDefinition(SessionManager m_session) {
		super();
		this.m_session = m_session;
	}
	public ArrayList<Point> points = new ArrayList<Point>();		

	
	public void formLineStringFromText(String s){
		s = s.substring(s.indexOf("(")+1);
		s = s.substring(0,s.indexOf(")"));
		String[] tempArray = s.split(","); 
		for(int i = 0; i< tempArray.length; i++){
			String temp = tempArray[i];
			Point p = new Point();
			String xy[] = temp.split(" ");
			p.setX(Double.parseDouble(xy[0]));
			p.setY(Double.parseDouble(xy[1]));
			this.addPoint(p);
		} 
	}
	
	public void addPoint(Point p){
		points.add(p);
	}

	public String processLineSegment(String name, String geomText, String descrption, String sessionId,boolean developGeom, int stateId,String districtName, String alignWith, int associatedWith) {
		int id = 0;
		String result = "";

	LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		try {
			boolean isMBR = false;
			String geometry = "";
			MgEnvelope e1 = null;
			if ( !developGeom ){
				geometry = geomText;
			} else {
				Map.createPolygonGeometryFromText(geomText);
				e1 = Map.getEnvelope(geomText);
				isMBR = Map.isEqualToMBR(geomText, e1);
			}
			
			double lowerX = 0.0;
			double lowerY = 0.0;
			double upperX = 0.0;
			double upperY = 0.0;
			
			if ( !developGeom){
				
				formLineStringFromText(geomText);
				for(int i = 0 ; i < points.size();i++){
					if ( i == 0 ){
						lowerX = upperX = points.get(i).getX();
						lowerY = upperY = points.get(i).getY();
					}
					if (lowerX > points.get(i).getX()){
						lowerX = points.get(i).getX();
					} 
					if ( upperX < points.get(i).getX()) {
						upperX = points.get(i).getX();
					}
					if ( lowerY > points.get(i).getY()){
						lowerY = points.get(i).getY();
					} 
					if (upperY < points.get(i).getY()){
						upperY = points.get(i).getY();
					}
				}
				result = lowerX + "," + lowerY + "," + upperX + ","+upperY;
			} else {
				
				lowerX = e1.GetLowerLeftCoordinate().GetX();
				lowerY = e1.GetLowerLeftCoordinate().GetY();
				upperX = e1.GetUpperRightCoordinate().GetX();
				upperY = e1.GetUpperRightCoordinate().GetY();
				result = lowerX + "," + lowerY + "," +upperX + "," + upperY;
				
			}
			
               id = lineSegmentDefinitonDao.saveLineSegment(name, geometry, descrption, lowerX,lowerY,upperX,upperY,stateId,districtName,alignWith,associatedWith);
			
			result = id + "," + result; 
			ArrayList <Integer> al = new ArrayList<Integer>();
			al.add(id);
			
		
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public ArrayList<LineSegmentBean> getLineSegmentList() {
		ArrayList<LineSegmentBean> lBeanList = new ArrayList<LineSegmentBean>();
		LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		try {
			lBeanList = lineSegmentDefinitonDao.getLineSegmentListMap();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lBeanList;
	}
	public ArrayList<RoadInfoBean> getRoadNameList() {
		ArrayList<RoadInfoBean> lBeanList = new ArrayList<RoadInfoBean>();
		LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		try {
			lBeanList = lineSegmentDefinitonDao.getRoadNameListMap();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lBeanList;
	}

	public  void getRoadNameList(String roadName,int portNodeId,ArrayList<RoadInfoBean> roadInfoList,HashMap<Integer,LineSegmentBean> lineSegmentMap) {

		LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		try {
			lineSegmentDefinitonDao.getRoadNameListMap(roadName,portNodeId,roadInfoList,lineSegmentMap);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void deleteRegion(int id) {
		LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		try {
			lineSegmentDefinitonDao.deleteRegionfromDB(id);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void saveRoadLineSegments(String segmentsId, int roadId){
		LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		int idx = segmentsId.indexOf("-1");
		if( idx >= 0){
		 segmentsId = segmentsId.substring(idx+3);
		}
		try {
			lineSegmentDefinitonDao.saveRoadLineSegments(segmentsId,roadId);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void saveRoadForOrg(String segmentsId, String roadName,int portNodeId){
		LineSegmentDefinitionDao lineSegmentDefinitonDao = new LineSegmentDefinitionDao(m_session);
		int idx = segmentsId.indexOf("-1");
		if( idx >= 0){
		 segmentsId = segmentsId.substring(idx+3);
		}
		try {
			lineSegmentDefinitonDao.saveRoadForOrg(segmentsId,roadName,portNodeId);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void deleteRoad(int roadId){
		LineSegmentDefinitionDao lineSegmentDefinitionDao = new LineSegmentDefinitionDao(m_session);
		try {
			lineSegmentDefinitionDao.deleteRoad(roadId);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	public void deleteRoadName(int roadId){
		LineSegmentDefinitionDao lineSegmentDefinitionDao = new LineSegmentDefinitionDao(m_session);
		try {
			lineSegmentDefinitionDao.deleteRoadName(roadId);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void saveNewLSNameOnly(String txt){
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(txt);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();
		ArrayList<LineSegmentBean> lsBeanList = new ArrayList<LineSegmentBean>();
		for (int i = 0; i < size; i++) {
			LineSegmentBean lsBean = new LineSegmentBean();
			org.w3c.dom.Node n = nList.item(i);

			org.w3c.dom.Element e = (org.w3c.dom.Element) n;

			lsBean.setId(Misc.getParamAsInt(e.getAttribute("lsId"))) ;
			lsBean.setName( e.getAttribute("lsName"));
		    lsBeanList.add(lsBean);
		}
		LineSegmentDefinitionDao lsDefinitonDao = new LineSegmentDefinitionDao(m_session);
		try {
			lsDefinitonDao.saveOnlyLSName(lsBeanList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	public ArrayList getLineSegmentNameList(String segmentsID){
		ArrayList dataList = new ArrayList();
		LineSegmentDefinitionDao lsDefinitonDao = new LineSegmentDefinitionDao(m_session);
		int idx = segmentsID.indexOf("-1");
		if( idx >= 0){
			segmentsID = segmentsID.substring(idx+3);
		}
		try {
			lsDefinitonDao.getLineSegmentNameList(segmentsID, dataList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataList;
	}
}

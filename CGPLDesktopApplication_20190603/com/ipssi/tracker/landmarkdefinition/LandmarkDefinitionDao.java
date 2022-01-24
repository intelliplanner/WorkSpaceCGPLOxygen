package com.ipssi.tracker.landmarkdefinition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.GeoToolsLayerLookup;
import com.ipssi.mapguideutils.GeoToolsUtils;
import com.ipssi.mapguideutils.LocalNameHelperRTree;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.mapguideutils.ReadShapeFile;
import com.ipssi.mapguideutils.ShapeFileBean;
import com.ipssi.mapguideutils.GeoToolsLayerLookup.LayerHolder;
import com.ipssi.mapping.common.db.DBQueries;
import com.ipssi.rfid.processor.Utils;

/**
 * @author jai
 * 
 */
public class LandmarkDefinitionDao {
	
	public LandmarkDefinitionDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	public SessionManager m_session = null;
    private static HashMap<String, String> stateName = null;// key stateName and value is abbreviation of stateName
     
   private static synchronized String getStateAbbrevName(String sname ){
	   if (stateName == null) {
	stateName = new HashMap<String, String>();
	stateName.put("ANDAMAN & NICOBAR", "AN");
	stateName.put("ANDHRA PRADESH", "AP");
	stateName.put("ARUNACHAL PRADES", "AR");
	stateName.put("ARUNACHAL PRADESH", "AR");
	stateName.put("ASSAM", "AS");
	stateName.put("BIHAR", "BR");
	stateName.put("CHANDIGARH", "CH");
	stateName.put("CHHATISGARH", "CG");
	stateName.put("CHHATTISGARH", "CG");
	stateName.put("DADRA & NAGAR HAVELI", "DN");
	stateName.put("DAMAN & DIU", "DU");
	stateName.put("DELHI", "DL");
	stateName.put("GOA", "GA");
	stateName.put("GUJARAT", "GJ");
	stateName.put("HARYANA", "HR");
	stateName.put("HIMACHAL PRADESH", "HP");
	stateName.put("JAMMU & KASHMIR", "JK");
	stateName.put("JHARKHAND", "JH");
	stateName.put("KARNATAKA", "KA");
	stateName.put("KERALA", "KL");
	stateName.put("LAKSHADWEEP", "LD");
	stateName.put("MADHYA PRADESH", "MP");
	stateName.put("MAHARASHTRA", "MH");
	stateName.put("MAHARASTRA", "MH");
	stateName.put("MANIPUR", "MN");
	stateName.put("MEGHALAYA", "ML");
	stateName.put("MIZORAM", "MZ");
	stateName.put("NAGALAND", "NL");
	stateName.put("ORISSA", "OR");
	stateName.put("ORRISA", "OR");
	stateName.put("PONDICHERRY", "PY");
	stateName.put("PUNJAB", "PB");
	stateName.put("RAJASTHAN", "RJ");
	stateName.put("SIKKIM", "SK");
	stateName.put("TAMIL NADU", "TN");
	stateName.put("TRIPURA", "TR");
	stateName.put("UTTAR PRADESH", "UP");
	stateName.put("UTTARAKHAND", "UA");
	stateName.put("UTTARANCHAL", "UA");
	stateName.put("WEST BENGAL", "WB");
	stateName.put("Unknown", "N/A");
	}
	   
	   return stateName.get(sname);
   }
	public ArrayList <LandmarkBean> fetchLandmarkList(int prorityIndex)
	throws SQLException, Exception {
		ArrayList<LandmarkBean> ldBeanList = new ArrayList<LandmarkBean>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = m_session.getConnection();
			if(conn == null)
				return ldBeanList;
			String sql = DBQueries.LandmarkDefinition.FETCH_LANDMARKS;
			ps = conn.prepareStatement(sql);

			int v123 = Misc.getParamAsInt(m_session.getAttribute("pv123"),
					Misc.G_TOP_LEVEL_PORT);
			ps.setInt(1, v123);
			// ps.setInt(2,
			// com.ipssi.mapping.common.util.ApplicationConstants.DELETED);
			ps.setInt(2, prorityIndex);
			rs = ps.executeQuery();
			while (rs.next()) {
				LandmarkBean lBean = new LandmarkBean();
				lBean.setId(rs.getInt("id"));
				lBean.setName(parseLandmarkName(rs.getString("name")));
				Point lowerPoint = new Point(rs.getDouble("lowerX"), rs
						.getDouble("lowerY"));
				lBean.setLowerPoint(lowerPoint);
				Point upperPoint = new Point(rs.getDouble("upperX"), rs
						.getDouble("upperY"));
				lBean.setUpperPoint(upperPoint);
				lBean.setPortNodeId(rs.getInt("port_node_id"));
				ldBeanList.add(lBean);
			}

			
		} catch (SQLException e) {
			System.out.println("Error In Fetch List");
			throw e;
		} catch (Exception e) {
			System.out.println("Error In Fetch List");
			throw e;
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}

		return ldBeanList;

	}
	public ArrayList <LandmarkBean> fetchLandmarkListforSameOrg(int prorityIndex,String landmarkName,int landmarkType)
	throws SQLException, Exception {
		ArrayList<LandmarkBean> ldBeanList = new ArrayList<LandmarkBean>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			String sql = DBQueries.LandmarkDefinition.FETCH_LANDMARKS3;
			if(landmarkType != Misc.getUndefInt()){
				sql += " and landmarks.landmark_type = ? ";;
			}
			if(!Utils.isNull(landmarkName)){
				sql += " and landmarks.name like '%"+landmarkName+"%'";
				
			}
			sql += " order by  landmarks.name ";
			System.out.println(sql);
			ps = conn.prepareStatement(sql);
			int v123 = Misc.getParamAsInt(m_session.getAttribute("pv123"),
					Misc.G_TOP_LEVEL_PORT);
			
			ps.setInt(1, v123);
			ps.setInt(2, prorityIndex);
			if(landmarkType != Misc.getUndefInt()){
				ps.setInt(3, landmarkType);
			}
//			if(!Utils.isNull(landmarkName)){
//				ps.setString(4, landmarkName);	
//			}
			System.out.println("LandmarkDefinitionDao :"+ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				LandmarkBean lBean = new LandmarkBean();

				lBean.setId(rs.getInt("id"));
				lBean.setName(parseLandmarkName(rs.getString("name")));

				Point lowerPoint = new Point(rs.getDouble("lowerX"), rs
						.getDouble("lowerY"));
				lBean.setLowerPoint(lowerPoint);

				Point upperPoint = new Point(rs.getDouble("upperX"), rs
						.getDouble("upperY"));
				lBean.setUpperPoint(upperPoint);
				lBean.setPortNodeId(rs.getInt("port_node_id"));
				lBean.setLandmarkType(rs.getInt("landmark_type"));
				ldBeanList.add(lBean);
			}

			
		} catch (SQLException e) {
			System.out.println("Error In Fetch List");
			throw e;
		} catch (Exception e) {
			System.out.println("Error In Fetch List");
			throw e;
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}

		return ldBeanList;

	}

	
	public ArrayList<LandmarkBean> fetchLandmarkList() throws SQLException,
	Exception {
		return fetchLandmarkList(LandmarkDefinitionAction.DEFAULT_PROIRITY_INDEX);
	}

	public ArrayList<LandmarkBean> fetchLandmarkListforSameOrg(String landmarkName,int landmarkType) throws SQLException,
	Exception {
		return fetchLandmarkListforSameOrg(LandmarkDefinitionAction.DEFAULT_PROIRITY_INDEX,landmarkName,landmarkType);
	}
	public void updateLandmark(LandmarkBean bean,int oldPortNodeId) throws SQLException,
	Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			String sql = DBQueries.LandmarkDefinition.UPDATE_LANDMARK;
			RTreesAndInformation. getWriteLock();
			/*
			 * " update landmarks set shape = ?, name = ?, port_node_id = ?,
			 * description=?, user_description = ?, lowerX = ?, lowerY = ?,
			 * upperX = ?," + " upperY = ? where id = ?";
			 */
			// String statNameFile =  "C:\\IPSSI\\mapdata\\Polygon\\state_boundary.shp";
			// String distNameFile =  "C:\\IPSSI\\mapdata\\Polygon\\district_boundary.shp";
			 //String statNameFile =  Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\state_boundary.shp";
			 //String distNameFile =  Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\district_boundary.shp";
			 String statNameFile = com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"state_boundary.shp";
			 String distNameFile = com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"district_boundary.shp";
			LayerHolder stateLayerHolder = new GeoToolsLayerLookup.LayerHolder(statNameFile);
			LayerHolder distLayerHolder = new GeoToolsLayerLookup.LayerHolder(distNameFile);
			com.vividsolutions.jts.geom.Point point = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());

			String stateName = GeoToolsUtils.getContainerRegionInLayer(stateLayerHolder,point);
			String distName = GeoToolsUtils.getContainerRegionInLayer(distLayerHolder, point);
			String landmarkName = parseLandmarkName(bean.getName());
			if(!LocalNameHelperRTree.isRTreeLoaded()){
				ReadShapeFile.loadRTree(conn);
				LocalNameHelperRTree.setRTreeLoaded();
			}


			ps = conn.prepareStatement(sql);

			ps.setString(1, bean.getGeometery());
			ps.setString(2, landmarkName);
			ps.setInt(3, bean.getPortNodeId());
			ps.setString(4, bean.getDescription());
			ps.setString(5, bean.getUserDescription());
			ps.setDouble(6, bean.getLowerPoint().getX());
			ps.setDouble(7, bean.getLowerPoint().getY());
			ps.setDouble(8, bean.getUpperPoint().getX());
			ps.setDouble(9, bean.getUpperPoint().getY());
			if (stateName == null) {
				stateName = "Unknown";
			}
			if (distName == null) {
				distName = "Unknown";
			}
			stateName = getStateAbbrevName(stateName);
			ps.setString(10, stateName);
			ps.setString(11, distName);
			ps.setTimestamp(12,new Timestamp(System.currentTimeMillis()));
			ps.setString(13, bean.getDestCode());
			ps.setInt(14, bean.getId());
			System.out.println("$$$$$$$$$$Query is: " + ps);
			ps.executeUpdate();
			
			//update Rtree corresponding to old and new portNode
			//case1. if old portnode and new port node are same
			if (oldPortNodeId == bean.getPortNodeId()) {
				Pair<RTree, Map<Integer, ShapeFileBean>> pair = RTreesAndInformation.addRTreeForLandMark(oldPortNodeId);
				Map<Integer,ShapeFileBean> shapefilebean = pair.second;
				ShapeFileBean bean2 = shapefilebean.get(bean.getId());
				com.vividsolutions.jts.geom.Point[] points = bean2.getPoints();
				//points[0] = GeoToolsUtils.getPointXIsLon(points[0].getX(),points[0].getY());

				Rectangle rectangle = new Rectangle();
				rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
				synchronized (pair.first) {
					pair.first.delete(rectangle, bean.getId());
				}


				points[0] = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());
				bean2.setPoints(points);
				rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
				synchronized (pair.first) {
					pair.first.add(rectangle, bean.getId());
				}
				//addTo.second.put(id, bean);
			}else{//case2. if old port node id is unequal to new portnode id
				if (oldPortNodeId != bean.getPortNodeId()) {
					Pair<RTree, Map<Integer, ShapeFileBean>> pair = RTreesAndInformation.addRTreeForLandMark(oldPortNodeId);
					Map<Integer, ShapeFileBean> shapefilebean = pair.second;
					ShapeFileBean bean2 = shapefilebean.get(bean.getId());
					com.vividsolutions.jts.geom.Point[] points = bean2.getPoints();
					Rectangle rectangle = new Rectangle();
					rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
					synchronized (pair.first) {
						pair.first.delete(rectangle, bean.getId());
					}
					shapefilebean.remove(bean.getId());

					pair =RTreesAndInformation.addRTreeForLandMark(bean.getPortNodeId());
					shapefilebean = pair.second;
					ShapeFileBean newBean = new ShapeFileBean();
					newBean.setName(landmarkName);
					newBean.setDistId(ShapeFileBean.StateDistInfo.getDistId(distName));
					newBean.setStateId(ShapeFileBean.StateDistInfo.getStateId(stateName));
					bean2 = shapefilebean.get(bean.getId());
					points[0] = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());
					newBean.setPoints(points);
					rectangle = new Rectangle();
					rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
					synchronized (pair.first) {
						pair.first.add(rectangle, bean.getId());
					}
					shapefilebean.put(bean.getId(), newBean);

				}
			}


		} catch (SQLException e) {
			System.out.println("Error In Update ");
			throw e;

		} catch (Exception e) {
			System.out.println("Error In Update ");
			throw e;
		}finally{
			RTreesAndInformation.releaseWriteLock();
			if (ps != null) {
				ps.close();
			}
		}
	}


	public int insertLandmark(LandmarkBean bean) throws SQLException, Exception {
		Connection conn = null;
		int id = -1;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		try {
			
			conn = m_session.getConnection();
			String sql = DBQueries.LandmarkDefinition.INSERT_LANDMARK;
			RTreesAndInformation. getWriteLock();
			ps = conn.prepareStatement(sql);
//'Point('+longitude+' ' +latitude + ')'
			/**
			 * " insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY)"
			 * + " values(GeomFromText(?),?,?,?,?,?,?,?,?)";" insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY,updated_on)"
			 * + " values(GeomFromText(?),?,?,?,?,?,?,?,?,?)"
			 * 
			 */
			 //String statNameFile =  "C:\\IPSSI\\mapdata\\Polygon\\state_boundary.shp";
			 //String distNameFile =  "C:\\IPSSI\\mapdata\\Polygon\\district_boundary.shp";
			// String statNameFile =  Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\state_boundary.shp";
			 //String distNameFile =  Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\district_boundary.shp";
			String statNameFile =  com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"state_boundary.shp";
			String distNameFile =  com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"district_boundary.shp";
			LayerHolder stateLayerHolder = null;
			LayerHolder distLayerHolder = null;
			try {
				stateLayerHolder  = new GeoToolsLayerLookup.LayerHolder(statNameFile);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			try {
				distLayerHolder  = new GeoToolsLayerLookup.LayerHolder(distNameFile);
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			com.vividsolutions.jts.geom.Point point = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());

			String stateName = stateLayerHolder == null ? null : GeoToolsUtils.getContainerRegionInLayer(stateLayerHolder,point);
			String distName = distLayerHolder == null ? null : GeoToolsUtils.getContainerRegionInLayer(distLayerHolder, point);
			
			String landmarkName = parseLandmarkName(bean.getName());
			String destCode = bean.getDestCode();
			java.sql.Timestamp ts = new Timestamp(new java.util.Date().getTime());
			ps.setString(1, bean.getGeometery());
			ps.setString(2, landmarkName);
			ps.setInt(3, bean.getPortNodeId());
			ps.setString(4, bean.getDescription());
			ps.setString(5, bean.getUserDescription());
			ps.setDouble(6, bean.getLowerPoint().getX());
			ps.setDouble(7, bean.getLowerPoint().getY());
			ps.setDouble(8, bean.getUpperPoint().getX());
			ps.setDouble(9, bean.getUpperPoint().getY());
			ps.setTimestamp(10, ts);
			ps.setInt(11, bean.getLandmarkType());

			if (stateName == null) {
				stateName = "Unknown";
			}
			if (distName == null) {
				distName = "Unknown";
			}
			stateName = getStateAbbrevName(stateName);
			ps.setString(12, stateName);
			ps.setString(13, distName);
			ps.setString(14, destCode);
			// ps.setInt(10,com.ipssi.mapping.common.util.ApplicationConstants.ACTIVE);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			
			if (rs.next())
				id = rs.getInt(1);
			
			if(!LocalNameHelperRTree.isRTreeLoaded()){
				ReadShapeFile.loadRTree(conn);
				LocalNameHelperRTree.setRTreeLoaded();
			}
			Pair<RTree, Map<Integer, ShapeFileBean>> pair = RTreesAndInformation.addRTreeForLandMark(bean.getPortNodeId());
			Map<Integer,ShapeFileBean> shapefilebean = pair.second;
			Rectangle rectangle = null;
			ShapeFileBean newBean = new ShapeFileBean();
			newBean.setName(landmarkName);
			newBean.setDistId(ShapeFileBean.StateDistInfo.getDistId(distName));
			newBean.setStateId(ShapeFileBean.StateDistInfo.getStateId(stateName));
			com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[1];
			points[0] = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());
			newBean.setPoints(points);
			rectangle = new Rectangle();
			rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
			synchronized (pair.first) {
				pair.first.add(rectangle, id);
			}
			shapefilebean.put(id, newBean);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error ="+ps);
			System.out.println("Error In Insert");
			throw e;

		} catch (Exception e) {
			System.out.println("Error In Insert");
			e.printStackTrace();
			throw e;
		}finally{
			RTreesAndInformation.releaseWriteLock();
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}
		return id;

	}

	public void deleteLandmarkFromDB(int id, int portNodeId) throws SQLException, Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
				conn = m_session.getConnection();
				String sql = DBQueries.LandmarkDefinition.DELETE_LANDMARK;

			ps = conn.prepareStatement(sql);
			// ps.setInt(1,
			// com.ipssi.mapping.common.util.ApplicationConstants.DELETED);
			ps.setInt(1, id);
			ps.executeUpdate();
			RTreesAndInformation. getWriteLock();
			if(!LocalNameHelperRTree.isRTreeLoaded()){
				ReadShapeFile.loadRTree(conn);
				LocalNameHelperRTree.setRTreeLoaded();
			}
	
			Pair<RTree, Map<Integer, ShapeFileBean>> pair = RTreesAndInformation.addRTreeForLandMark(portNodeId);
			Map<Integer, ShapeFileBean> shapefilebean = pair.second;
			ShapeFileBean bean2 = shapefilebean.get(id);
			com.vividsolutions.jts.geom.Point[] points = bean2.getPoints();
			Rectangle rectangle = new Rectangle();
			rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
			synchronized (pair.first) {
				pair.first.delete(rectangle, id);
			}
			shapefilebean.remove(id);
        
		} catch (SQLException e) {
			System.out.println("Error In Delete Landmark from DB");
			throw e;

		} catch (Exception e) {
			System.out.println("Error In Delete Landmark from DB");
			throw e;
		} finally {
		    RTreesAndInformation.releaseWriteLock();
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void insertList(ArrayList<LandmarkBean> beanList) throws Exception {
		Connection conn = null;
		int id = -1;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.LandmarkDefinition.INSERT_LANDMARK);

			/**
			 * " insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY,updated_on)"
			 * + " values(GeomFromText(?),?,?,?,?,?,?,?,?,?)";
			 * 
			 */
			// String statNameFile =  "C:\\IPSSI\\mapdata\\Polygon\\state_boundary.shp";
			 //String distNameFile =  "C:\\IPSSI\\mapdata\\Polygon\\district_boundary.shp";
			 //String statNameFile =  Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\state_boundary.shp";
			 //String distNameFile =  Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\district_boundary.shp";
			 String statNameFile =  com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"state_boundary.shp";
			 String distNameFile =  com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"district_boundary.shp";
			LayerHolder stateLayerHolder  = new GeoToolsLayerLookup.LayerHolder(statNameFile);
			LayerHolder distLayerHolder  = new GeoToolsLayerLookup.LayerHolder(distNameFile);


			java.sql.Timestamp ts = new Timestamp(new java.util.Date().getTime());
			Pair<RTree, Map<Integer, ShapeFileBean>> pair = null;
			Map<Integer,ShapeFileBean> shapefilebean = null;
			ShapeFileBean newBean = null;
			Rectangle rectangle = null;
			com.vividsolutions.jts.geom.Point[] points = new com.vividsolutions.jts.geom.Point[1];
			RTreesAndInformation.getWriteLock();
			for (int i = 0; i < beanList.size(); i++) {


				LandmarkBean bean = beanList.get(i);

				com.vividsolutions.jts.geom.Point point = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());
				
				String stateName = GeoToolsUtils.getContainerRegionInLayer(stateLayerHolder,point);
				String distName = GeoToolsUtils.getContainerRegionInLayer(distLayerHolder, point);
                String landmarkName = parseLandmarkName(bean.getName());
				ps.setString(1, bean.getGeometery());
				ps.setString(2, landmarkName);
				ps.setInt(3, bean.getPortNodeId());
				ps.setString(4, bean.getDescription());
				ps.setString(5, bean.getUserDescription());
				ps.setDouble(6, bean.getLowerPoint().getX());
				ps.setDouble(7, bean.getLowerPoint().getY());
				ps.setDouble(8, bean.getUpperPoint().getX());
				ps.setDouble(9, bean.getUpperPoint().getY());
				ps.setTimestamp(10, ts);
				ps.setInt(11,bean.getLandmarkType());
			//	shape,name,port_node_id,description,user_description,lowerX,lowerY,
		//		upperX,upperY,updated_on,landmark_type,state_name,district_name,dest_code)
				if (stateName == null) {
					stateName = "Unknown";
				}
				if (distName == null) {
					distName = "Unknown";
				}
				stateName = getStateAbbrevName(stateName);
				String destCode = null;
				ps.setString(12, stateName);
				ps.setString(13, distName);
				ps.setString(14, destCode);
				ps.executeUpdate();

				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					id = rs.getInt(1);

				if(!LocalNameHelperRTree.isRTreeLoaded()){
					ReadShapeFile.loadRTree(conn);
					LocalNameHelperRTree.setRTreeLoaded();
				}
//				updateSpatialLocationName(bean.getPortNodeId(), bean.getLowerPoint());
				// ps.setInt(10,com.ipssi.mapping.common.util.ApplicationConstants.ACTIVE);
//				ps.addBatch();
			    pair = RTreesAndInformation.addRTreeForLandMark(bean.getPortNodeId());
			    shapefilebean = pair.second;
				newBean = new ShapeFileBean();
				newBean.setName(landmarkName);
				newBean.setDistId(ShapeFileBean.StateDistInfo.getDistId(distName));
				newBean.setStateId(ShapeFileBean.StateDistInfo.getStateId(stateName));
				
				points[0] = GeoToolsUtils.getPointXIsLon(bean.getLowerPoint().getX(),bean.getLowerPoint().getY());
				newBean.setPoints(points);
				rectangle = new Rectangle();
				rectangle.set((float)points[0].getX(),(float)points[0].getY(),(float)points[0].getX(),(float)points[0].getY());
				synchronized (pair) {
					pair.first.add(rectangle, id);
				}
				shapefilebean.put(id, newBean);
			}
//			ps.executeBatch();
		} catch (SQLException e) {
			System.out.println("Error While Insert Landmark from CSV File");
			throw e;

		} catch (Exception e) {
			System.out.println("Error While Insert Landmark from CSV File");
			throw e;
		}finally{
			RTreesAndInformation.releaseWriteLock();
			if (ps != null) {
				ps.close();
			}
		}
	}

	public String parseLandmarkName(String name){
		String landmarkName = null;
		if (name != null) {
			landmarkName = name.trim().replaceAll("(\r\n|\r|\n)", "");
			name = landmarkName;
		}
		return name;
	}
	public void updateSpatialLocationName(int pv123, Point point)
	throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			String sql = "delete  from spatail_location_name where port_node_id = ?";
			// DBQueries.LandmarkDefinition.SPATIAL_DATA_DELETE;
			System.out
			.println("LandmarkDefinitionDao.updateSpatialLocationName() [DEBUG] + ALL Spatial Data Delete for Port Id = "
					+ pv123);

			ps = conn.prepareStatement(sql);

			ps.setInt(1, pv123);
			ps.executeUpdate();

			// CacheTrack.VehicleSetup.setForceLookLocation();

		} catch (SQLException e) {
			System.out.println("Error While Update SpatialLocation Name");
			throw e;

		} catch (Exception e) {
			System.out.println("Error While Update SpatialLocation Name");
			throw e;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void saveOnlyLandMarkName(List<LandmarkBean> bean, int portNodeId) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
		//	RTreesAndInformation.getWriteLock();
			conn = m_session.getConnection();
			String sql = "update landmarks set landmarks.name = ? where  landmarks.id = ?";
			ps = conn.prepareStatement(sql);
			if(!LocalNameHelperRTree.isRTreeLoaded()){
				ReadShapeFile.loadRTree(conn);
				LocalNameHelperRTree.setRTreeLoaded();
			}
			Pair<RTree, Map<Integer, ShapeFileBean>> pair =  RTreesAndInformation.addRTreeForLandMark(portNodeId);
			Map<Integer,ShapeFileBean> shapefilebean =  pair.second;
			ShapeFileBean shapeFileBean = null;
			for (int i = 0, k = bean.size(); i < k; i++) {
				LandmarkBean ldbean = bean.get(i);
				String landmarkName = parseLandmarkName(ldbean.getName());
				ps.setString(1, landmarkName);
				//ps.setInt(2, portNodeId);
				ps.setInt(2, ldbean.getId());
				ps.executeUpdate();
				shapeFileBean = shapefilebean.get(ldbean.getId());
				shapeFileBean.setName(landmarkName);
			//	ps.addBatch();
			}
//			updateSpatialLocationName(portNodeId, null);
			//int executeLength = ps.executeBatch().length;

			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			RTreesAndInformation.releaseWriteLock();
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args) {
		System.out.println("MySQL Connect Example.");
		  Connection conn = null;
		  String url = "jdbc:mysql://localhost:3306/";
		  String dbName = "ipssi";
		  String driver = "com.mysql.jdbc.Driver";
		  String userName = "root"; 
		  String password = "root";
//'Point('+longitude+' ' +latitude + ')'
			/**
			 * " insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY)"
			 * + " values(GeomFromText(?),?,?,?,?,?,?,?,?)";" insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY,updated_on)"
			 * + " values(GeomFromText(?),?,?,?,?,?,?,?,?,?)"
			 * 
			 */
		  String query1 = "select CHNC,longitude,latitude,landmarktype from fdhs_test where id >=1013";
		  String query = " insert into landmarks(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY,updated_on,landmark_type,state_name,district_name,old)"
				  + " values(GeomFromText(?),?,?,?,?,?,?,?,?,?,?,?,?,?)";
		  String statNameFile = null;// Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\state_boundary.shp";
			 String distNameFile = null;// Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"\\mapdata\\Polygon\\district_boundary.shp";
			 //String statNameFile = com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"state_boundary.shp";
			//String distNameFile = com.ipssi.mapping.common.util.ApplicationConstants.MAPDATAPATH+"district_boundary.shp";
			
		  try {
				LayerHolder stateLayerHolder = new GeoToolsLayerLookup.LayerHolder(statNameFile);
				LayerHolder distLayerHolder = new GeoToolsLayerLookup.LayerHolder(distNameFile);
			
		  Class.forName(driver).newInstance();
		  conn = DriverManager.getConnection(url+dbName,userName,password);
		  System.out.println("Connected to the database");
		  //String query = "select id,lowerX,lowerY from landmarks";
		  PreparedStatement ps = conn.prepareStatement(query1);
		  ResultSet rs  = ps.executeQuery();
		  PreparedStatement ps1 = conn.prepareStatement(query);
		  int count = 0;
		  while ( rs.next()) {
            String fdhs_vill = rs.getString(1);
            Double lowerx = Misc.getParamAsDouble(rs.getString(2));
            Double lowery = Misc.getParamAsDouble(rs.getString(3));
            int landmarktype = rs.getInt(4);
			  com.vividsolutions.jts.geom.Point point = GeoToolsUtils.getPointXIsLon(lowerx,lowery);

			String stateName = GeoToolsUtils.getContainerRegionInLayer(stateLayerHolder,point);
			String distName = GeoToolsUtils.getContainerRegionInLayer(distLayerHolder, point);
			if (stateName == null) {
				stateName = "Unknown";
			}
			if (distName == null) {
				distName = "Unknown";
			}
			//(shape,name,port_node_id,description,user_description,lowerX,lowerY,upperX,upperY,updated_on,landmark_type)"
	        ps1.setString(1, "Point("+lowerx+' ' +lowery + ")");
	        ps1.setString(2, fdhs_vill);
	        ps1.setInt(3, 213);
	        ps1.setString(4, "balwant");
	        ps1.setString(5, "");
	        ps1.setDouble(6, lowerx);
	        ps1.setDouble(7, lowery);
	        ps1.setDouble(8, lowerx);
	        ps1.setDouble(9, lowery);
	        ps1.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
	        ps1.setInt(11, landmarktype);
			ps1.setString(12, getStateAbbrevName(stateName));
			ps1.setString(13, distName);
			ps1.setInt(14, 1);
			ps1.execute();
		
			count++;
		}
		  System.out.println("Total Row Is ="+count);
		  conn.close();
		  System.out.println("Disconnected from database");
		  } catch (Exception e) {
		  e.printStackTrace();
		  }
		  }

}

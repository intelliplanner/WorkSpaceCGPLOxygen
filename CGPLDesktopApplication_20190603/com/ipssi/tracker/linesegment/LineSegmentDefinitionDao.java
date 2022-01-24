package com.ipssi.tracker.linesegment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.geometry.Point;
import com.ipssi.mapping.common.db.DBQueries;
import com.ipssi.tracker.region.RegionBean;

public class LineSegmentDefinitionDao {

SessionManager m_session = null;
	
	
	
	public LineSegmentDefinitionDao(SessionManager m_session) {
		super();
		this.m_session = m_session;
	}
	public String parseLineSegmentName(String name){
		String lineSegmentName = null;
		if (name != null) {
			lineSegmentName = name.trim().replaceAll("(\r\n|\r|\n)", "");
			name = lineSegmentName;
		}
		return name;
	}

	public int saveLineSegment(String name, String geomText,  String descrption, double lowerX, double lowerY, double upperX, double upperY, int statdId, String districtName, String alignWith, int associatedWith) throws Exception, SQLException{
		int id = -1;
		Connection conn = null;
		try{
			
				conn = m_session.getConnection();
			
			
			String query = DBQueries.LineSegmentDefiniton.INSERT_LINE_SEGMENT;
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, geomText);
			ps.setString(2, parseLineSegmentName(name));
			
			ps.setString(3,descrption);
			
			ps.setDouble(4, lowerX);
			ps.setDouble(5, lowerY);
			ps.setDouble(6, upperX);
			ps.setDouble(7, upperY);
			ps.setInt(8, statdId);
			ps.setString(9, districtName);
			ps.setString(10, alignWith);
			ps.setInt(11, associatedWith);
			ps.execute();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next())
				id = rs.getInt(1);
			rs.close();
			ps.close();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		}
		return id;
	}
	public ArrayList<RoadInfoBean> getRoadNameListMap() throws Exception, SQLException{
		return getRoadNameListMap(0);
	}
	public ArrayList<RoadInfoBean> getRoadNameListMap(int priority) throws Exception, SQLException{
		Connection conn = null;
		
		ArrayList<RoadInfoBean> lBeanList = new ArrayList<RoadInfoBean>();
		try{
			conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.FETCH_ROAD_LIST;
			
			PreparedStatement ps = conn.prepareStatement(query);
			//ps.setInt(1,priority);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				//regionList.put(rs.getInt("id"), rs.getString("short_code"));
				RoadInfoBean lBean = new RoadInfoBean();
				lBean.setRoadId(rs.getInt("id"));
				lBean.setRoadName(parseLineSegmentName(rs.getString("road_name")));
				lBeanList.add(lBean);
			}
			rs.close();
			ps.close();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		} 
		return lBeanList;
	}
	public ArrayList<LineSegmentBean> getLineSegmentListMap() throws Exception, SQLException{
		return getLineSegmentListMap(0);
	}
	
	public ArrayList<LineSegmentBean> getLineSegmentListMap(int priority) throws Exception, SQLException{
		Connection conn = null;
		
		ArrayList<LineSegmentBean> lBeanList = new ArrayList<LineSegmentBean>();
		try{
			conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.FETCH_LINE_SEGMENTS;
			
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1,priority);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				//regionList.put(rs.getInt("id"), rs.getString("short_code"));
				LineSegmentBean lBean = new LineSegmentBean();
				lBean.setId(rs.getInt("id"));
				lBean.setName(parseLineSegmentName(rs.getString("short_code")));
				lBean.setLowerPoint( new Point(rs.getDouble("lowerX"),rs.getDouble("lowerY") )  );
				lBean.setUpperPoint( new Point(rs.getDouble("upperX"), rs.getDouble("upperY") )   );
				lBean.setGeometery(rs.getString("shape"));
				lBean.setDescription(rs.getString("description"));
				lBeanList.add(lBean);
			}
			rs.close();
			ps.close();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		} 
		return lBeanList;
	}
	public void getRoadNameListMap(String roadName,int portNodeId,ArrayList<RoadInfoBean> roadInfoList,HashMap<Integer,LineSegmentBean> lineSegmentMap) throws Exception, SQLException{
		Connection conn = null;
		
		
		try{
			conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.FETCH_ROAD_INFO;
			if(roadName!= null && roadName.length() > 0){
				query = query +" and road_name like '%"+roadName+"%'";
			}
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1,portNodeId);
			ResultSet rs = ps.executeQuery();
			int prevRoadId = Misc.getUndefInt();
			ArrayList<RoadInfoBean> rBeanList = new ArrayList<RoadInfoBean>();
			ArrayList<Integer> segmentId = null;
			RoadInfoBean rBean = null;
			while(rs.next()){
				//regionList.put(rs.getInt("id"), rs.getString("short_code"));
				LineSegmentBean lBean = new LineSegmentBean();
				
				int newRoadId = rs.getInt("id");
				if(prevRoadId != newRoadId){
					segmentId = new ArrayList<Integer>();
					rBean = new RoadInfoBean();
					rBean.setLineSegmentId(segmentId);
					rBeanList.add(rBean);
					rBean.setRoadId(newRoadId);
					rBean.setRoadName(parseLineSegmentName(rs.getString("road_name")));
					roadInfoList.add(rBean);
					prevRoadId = newRoadId;
				}
				
				int lineSegmentId = rs.getInt("line_segment_id");
				    segmentId.add(lineSegmentId);
				if (!lineSegmentMap.containsKey(lineSegmentId)) {
					lBean.setId(lineSegmentId);
					lBean.setLineSegmentOrder(rs.getInt("line_segment_order"));
					lBean.setName(parseLineSegmentName(rs.getString("short_code")));
					lBean.setLowerPoint( new Point(rs.getDouble("lowerX"),rs.getDouble("lowerY") )  );
					lBean.setUpperPoint( new Point(rs.getDouble("upperX"), rs.getDouble("upperY") )   );
					lBean.setGeometery(rs.getString("shape"));
					lineSegmentMap.put(lineSegmentId, lBean);	
				}
			}
			rs.close();
			ps.close();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		} 
		
	}

	public void deleteRegionfromDB(int id) throws Exception, SQLException{
		Connection conn = null;
		try{
				conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.DELETE_LINESEGMENT;
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, id);
			ps.executeUpdate();
			ps.close();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		}  
		
	}
	public void deleteRoad(int roadId) throws Exception,SQLException{
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.DELETE_ROAD_SEGMENT;
			ps = conn.prepareStatement(query);
			ps.setInt(1, roadId);
			ps.execute();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		}finally{
			if (ps != null) {
				ps.close();	
			}
			
		}  
	}
	public void deleteRoadName(int roadId) throws Exception,SQLException{
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.DELETE_ROAD;
			ps = conn.prepareStatement(query);
			ps.setInt(1, roadId);
			ps.execute();
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		}finally{
			if (ps != null) {
				ps.close();	
			}
			
		}  
	}
	public void saveRoadLineSegments(String segmentIds,int roadId) throws Exception, SQLException{
		Connection conn = null;
		String[] segmentId = null;
		PreparedStatement ps = null;
		try {
			if(segmentIds != null && segmentIds.length()>0){
				segmentId = segmentIds.split(",");	
				conn = m_session.getConnection();
					
				if (!Misc.isUndef(roadId)) {
				  String  query = DBQueries.LineSegmentDefiniton.SAVE_ROAD_INFO;
					ps = conn.prepareStatement(query);
					for (int i = 0; i < segmentId.length; i++) {
						ps.setInt(1,new Long(roadId).intValue());
						ps.setInt(2, Misc.getParamAsInt(segmentId[i]));
						ps.setInt(3, i);
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public void saveRoadForOrg(String segmentIds,String roadName, int portNodeId) throws Exception, SQLException{
		Connection conn = null;
		String[] segmentId = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
			
			long newRoadId = Misc.getUndefInt();
			if(segmentIds != null && segmentIds.length()>0){
			segmentId = segmentIds.split(",");	
			conn = m_session.getConnection();
			String query = DBQueries.LineSegmentDefiniton.SAVE_ROAD_FOR_ORG;
			ps = conn.prepareStatement(query);
				ps.setString(1, roadName);
				ps.setInt(2, portNodeId);
		
			
			int affectedRow = ps.executeUpdate();
			if (affectedRow == 0) {
	            throw new SQLException("Creating user failed, no rows affected.");
	        }
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				newRoadId = rs.getLong(1);
			} else {
	            throw new SQLException("Creating user failed, no generated key obtained.");
	        }
			
			if (!Misc.isUndef(newRoadId)) {
			    query = DBQueries.LineSegmentDefiniton.SAVE_ROAD_INFO;
				ps = conn.prepareStatement(query);
				for (int i = 0; i < segmentId.length; i++) {
					ps.setInt(1,new Long(newRoadId).intValue());
					ps.setInt(2, Misc.getParamAsInt(segmentId[i]));
					ps.setInt(3, i);
					ps.addBatch();
				}
				ps.executeBatch();
			}
		}
		} catch(SQLException e){
			throw e;
		} catch(Exception e){
			throw e;
		}finally{
			if (ps != null) {
				ps.close();	
			}
			if (rs != null) {
				rs.close();
			}
			
		}  
		
	}
	public String parseLSName(String name){
		String regionName = null;
		if (name != null) {
			regionName = name.trim().replaceAll("(\r\n|\r|\n)", "");
			name = regionName;
		}
		return name;
	}
	public void saveOnlyLSName(List<LineSegmentBean> list) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = m_session.getConnection();
			String sql = "update line_segments_info set line_segments_info.short_code = ? where line_segments_info.id = ?";
			ps = conn.prepareStatement(sql);
			for (int i = 0, k = list.size(); i < k; i++) {
				LineSegmentBean lsdbean = list.get(i);
				ps.setString(1, parseLSName(lsdbean.getName()));
				ps.setInt(2, lsdbean.getId());
				ps.addBatch();
			}
			int executeLength = ps.executeBatch().length;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void getLineSegmentNameList(String segmentIds , ArrayList dataList) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if(segmentIds != null && segmentIds.length()>0){
			conn = m_session.getConnection();
			StringBuilder sql = new StringBuilder("select id,short_code from line_segments_info where id in (");
			// Misc.convertInListToStr(pIds, query);
			String[] sIds = segmentIds.split(",");  
			Misc.convertInListToStr(sIds, sql);
			sql.append(")");
			ps = conn.prepareStatement(sql.toString());
			rs = ps.executeQuery();
			Pair<Integer, String> dataPair = null;
			while (rs.next()) {
				dataPair = new Pair<Integer, String>(rs.getInt(1),parseLineSegmentName(rs.getString(2)));
			    dataList.add(dataPair);
			}
		 }
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}

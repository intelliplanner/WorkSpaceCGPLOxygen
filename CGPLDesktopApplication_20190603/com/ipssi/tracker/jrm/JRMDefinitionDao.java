package com.ipssi.tracker.jrm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.mapping.common.db.DBQueries;


/**
 * @author Vicky
 *
 */
/**
 * @author Vicky
 *
 */
public class JRMDefinitionDao {

	public SessionManager m_session = null;
	public JRMDefinitionDao(SessionManager m_session) {
		this.m_session = m_session;
	}
	public void insertList(JRMBean bean) throws Exception {
		Connection conn = null;
	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.JRMDefiniton.INSERT_JRM);
			//risk_level, category, landmark_type, start_hour, start_min, end_hour,
			//end_min, landmark_region_seg_id, status, created_on,category_type
				ps.setInt(1, bean.getRisk_level());
				ps.setInt(2, bean.getCategory());
				ps.setInt(3, bean.getLandmark_type());
				ps.setInt(4, bean.getStart_hour());
				ps.setInt(5, bean.getStart_min());
				ps.setInt(6, bean.getEnd_hour());
				ps.setInt(7, bean.getEnd_min());
				ps.setInt(8, bean.getLandmark_region_seg_id());
				ps.setInt(9, 1);
				ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
				ps.setString(11, bean.getCategory_type());
				ps.executeUpdate();
				 
				
		} catch (SQLException e) {
			System.out.println("Error While Insert Landmark from CSV File");
			throw e;
		} catch (Exception e) {
			System.out.println("Error While Insert Landmark from CSV File");
			throw e;
		}finally{
			//RTreesAndInformation.releaseWriteLock();
			if (ps != null) {
				ps.close();
			}		
			if (rs != null) {
				rs.close();
			}		
		}
		
	}
	
	public int saveRegion(JRMBean bean) {
		int id = Misc.getUndefInt();
		Connection conn = null;
		try{
			//shape,short_code,port_node_id,description,lowerX,lowerY,upperX,upperY,equal_to_MBR,region_type
			conn = m_session.getConnection();
			String query = DBQueries.RegionDefiniton.INSERT_REGION;
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, bean.getGeometery());
			ps.setString(2, parseRegionName(bean.getName()));
			ps.setInt(3, bean.getPortNodeId());
			ps.setString(4,bean.getDescription());
			ps.setDouble(5, bean.getLowerPoint().getX());
			ps.setDouble(6,  bean.getLowerPoint().getY());
			ps.setDouble(7,  bean.getUpperPoint().getX());
			ps.setDouble(8,  bean.getUpperPoint().getY());
			ps.setBoolean(9, false);
			ps.setInt(10, bean.getRegionType());
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()){
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch(SQLException e){
			try {
				throw e;
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch(Exception e){
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return id;
	}
	public String parseRegionName(String name){
		String regionName = null;
		if (name != null) {
			regionName = name.trim().replaceAll("(\r\n|\r|\n)", "");
		name = regionName;
		}
		return name;
		}

}

/**
 * 
 */
package com.ipssi.tracker.dimension;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.common.util.DataProcessorGateway;


/**
 * @author jai
 *
 */
public class DimensionDao {

	private SessionManager m_session;
	private static Logger logger = Logger.getLogger(SessionManager.class); 
	/**
	 * @param m_session
	 */
	public DimensionDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	/**
	 * @param id
	 * @return
	 */
	public DimensionDetailBean fetchDimension(DimensionDetailBean ddBean) throws GenericException{
		Connection conn = null;
		try{
			conn = m_session.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(DBQueries.Dimension.FETCH_ID_DETAILS);
			
			ps.setInt(1, ddBean.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				ddBean.addToTypeMap(rs.getDouble("reading"), rs.getDouble("value"));
			}
			rs.close();
			ps.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return ddBean;
	}

	/**
	 * @param bean
	 * @return 
	 * @throws GenericException 
	 */
	public boolean insert(DimensionDetailBean bean) throws GenericException {
		Connection conn = null;
		boolean retVal = false;
		try{
			Timestamp timestamp = new Timestamp((new Date()).getTime());
			
			conn = m_session.getConnection();
			PreparedStatement ps = conn.prepareStatement(DBQueries.Dimension.INSERT_INFO);
			// insert into dimension_values_map_info(name,description,port_node_id,updated_on) values(?,?,?,?)
			ps.setString(1, bean.getName() );
			ps.setString(2, bean.getDescription());
			ps.setInt(3, bean.getPortNodeId());
			ps.setInt(4, ApplicationConstants.ACTIVE);
			ps.setTimestamp(5, timestamp);
			
			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			rs.next();
			bean.setId(rs.getInt(1));
			
			insertMap(bean,conn,timestamp);
			
			rs.close();
			ps.close();
			
			retVal = true;
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
 
		return(retVal);
	}

	/**
	 * @param bean
	 * @param timestamp 
	 * @throws GenericException 
	 */
	private void insertMap(DimensionDetailBean bean,Connection conn, Timestamp timestamp) throws GenericException {
		try{
			int id = bean.getId();
			PreparedStatement ps = conn.prepareStatement(DBQueries.Dimension.DELETE_DETAILS);
			ps.setInt(1, id);
			ps.executeUpdate();
			
			ps = conn.prepareStatement(DBQueries.Dimension.INSERT_DETAILS);
			// "insert into dimension_value_map(dimension_values_map_info_id,reading,value,updated_on) values(?,?,?,?)";
			
			Set<Entry<Double, Double>> s = bean.getTypeMap().entrySet();
			Iterator<Entry<Double, Double>> itMap = s.iterator();
			while (itMap.hasNext()) {

				Map.Entry<Double, Double> meMap = (Map.Entry<Double, Double>) itMap.next();

				double reading = ((Double) meMap.getKey()).doubleValue();
				double value = ((Double) meMap.getValue()).doubleValue();
				
				ps.setInt(1, id);
				ps.setDouble(2, reading);
				ps.setDouble(3, value);
				ps.setTimestamp(4, timestamp);
				
				ps.addBatch();
			}
			
			ps.executeBatch();
			ps.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}

		
	}

	/**
	 * @param bean
	 * @throws GenericException 
	 */
	public boolean update(DimensionDetailBean bean) throws GenericException {
		Connection conn = null;
		boolean retVal = false;
		try{
			Timestamp ts = new Timestamp((new Date()).getTime());
			conn = m_session.getConnection();
			//update dimension_values_map_info set name= ?,
			//port_node_id=?,description=?,status=?,updated_on=? where id=?
			PreparedStatement ps = conn.prepareStatement(DBQueries.Dimension.UPDATE_INFO);
			
			ps.setString(1, bean.getName());
			ps.setString(3, bean.getDescription());
			ps.setInt(2, bean.getPortNodeId());
			ps.setInt(4, ApplicationConstants.ACTIVE);
			ps.setTimestamp(5, ts);
			ps.setInt(6, bean.getId());
			
			ps.executeUpdate();
			
			insertMap(bean,conn,ts);
			
			ps.close();
			
			retVal = true;
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return(retVal);
	}

	/**
	 * @param pv123 
	 * @return
	 * @throws GenericException 
	 */
	public ArrayList<DimensionDetailBean> fetchDimensionList(int pv123,int status) throws GenericException {
		ArrayList<DimensionDetailBean> ddList = new ArrayList<DimensionDetailBean>();
		Connection conn = null;
		try{
			conn = m_session.getConnection();
			PreparedStatement ps = conn.prepareStatement(DBQueries.Dimension.FETCH_LIST);
			ps.setInt(1, pv123);
			ps.setInt(2, status);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				DimensionDetailBean bean = new DimensionDetailBean();
				bean.setId(rs.getInt("id"));
				bean.setDescription(rs.getString("description"));
				bean.setName(rs.getString("name"));
				bean.setPortNodeId(rs.getInt("port_node_id"));
				
				ddList.add(bean);
			}
			rs.close();
			ps.close();
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}

		return ddList;
	}

	/**
	 * @param checkDelete
	 * @throws GenericException 
	 */
	public boolean deleteDimensionMap(String[] checkDelete) throws GenericException {
		boolean retVal = false;
		Connection conn = null;
		try{
			conn = m_session.getConnection();
			
			PreparedStatement psInfo = conn.prepareStatement(DBQueries.Dimension.DELETE_INFO);
			for ( int i = 0; i  < checkDelete.length ; i++){
				psInfo.setInt(1, ApplicationConstants.DELETED);
				psInfo.setInt(2, com.ipssi.gen.utils.Misc.getParamAsInt(checkDelete[i]));
				psInfo.addBatch();
			}
			
			
			psInfo.executeBatch();
			
			psInfo.close();
			
		} catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);

			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}

		return retVal;
		
	}
	public void handleUpdate(Connection conn, int dimReadingMapId) throws Exception {
	    ArrayList<Integer> temp = new ArrayList<Integer>();
	    temp.add(dimReadingMapId);
	    handleUpdate(conn, temp);
	}
	public void handleUpdate(Connection conn, ArrayList<Integer> dimReadingMapId) throws Exception {
		try {
			if (dimReadingMapId == null || dimReadingMapId.size() == 0)
				return;
			ArrayList<Integer> mapSetIdUsingThis = new ArrayList<Integer>();
			StringBuilder query = new StringBuilder(DBQueries.Dimension.GET_MAPSETS_USING);
			Misc.convertInListToStr(dimReadingMapId, query);
			query.append(")");
			PreparedStatement ps = conn.prepareStatement(query.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				mapSetIdUsingThis.add(id);
			}
			rs.close();
			ps.close();
			if (mapSetIdUsingThis.size() > 0) {
				try {
					conn.commit(); //sync ops happen on different connection
					DataProcessorGateway.refreshMapSets(mapSetIdUsingThis);
				}
				catch (Exception e2) {
					e2.printStackTrace();
					//eat it - though we need to give a warning and send an email or maybe just throw??
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}

package com.ipssi.tracker.iomap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import java.sql.PreparedStatement;

import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.common.util.DataProcessorGateway;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.gen.utils.*;
import static com.ipssi.tracker.common.util.ApplicationConstants.*;

import static com.ipssi.tracker.common.util.Common.*;


public class IoDao {
	private static Logger logger = Logger.getLogger(IoDao.class);

	/**
	 * Get Device Name Relation Map from Device_Model_info
	 * 
	 * @return
	 * @throws GenericException
	 */
	HashMap<Integer, String> getDeviceNameMap(Connection conn) throws GenericException {
		HashMap<Integer, String> deviceNameMap = new HashMap<Integer, String>();
		try {

			String fetchMap = DBQueries.IOMAP.FETCH_DEVICE_MODEL_DATA;

			PreparedStatement ps = conn.prepareStatement(fetchMap);
			ps.setInt(1, DELETED);
			ResultSet rs = null;

			rs = ps.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");

				deviceNameMap.put(id, name);
			}
			rs.close();
			ps.close();

		}

		catch (SQLException e) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, e);
			throw new GenericException(ExceptionMessages.DB_DATA_PROBLEM, e);
		}

		return deviceNameMap;
	}

	/**
	 * Get Device Pin Count, Id Relation Map from Device_Model_info
	 * 
	 */
	HashMap<Integer, Integer> getDevicePinCountMap(Connection conn) throws GenericException {
		HashMap<Integer, Integer> devicePinCountMap = new HashMap<Integer, Integer>();
		try {

			String fetchMap = DBQueries.IOMAP.FETCH_DEVICE_MODEL_PIN_DATA;

			PreparedStatement ps = conn.prepareStatement(fetchMap);
			ps.setInt(1, DELETED);
			ResultSet rs = null;

			rs = ps.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				int ioCount = Misc.getRsetInt(rs, "io_count", 0);

				devicePinCountMap.put(id, ioCount);
			}
			rs.close();
			ps.close();

		}

		catch (SQLException e) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, e);
			throw new GenericException(ExceptionMessages.DB_DATA_PROBLEM, e);
		}

		return devicePinCountMap;
	}

	/*
	 * Get Attribute Id and Description Relation Map from Dimension
	 */
	HashMap<Integer, String> getDimensionMap(Connection conn) throws GenericException {

		HashMap<Integer, String> dimension = new HashMap<Integer, String>();

		try {

			String fetchMap = DBQueries.APPLICATION.FETCH_DIMENSIONS;

			PreparedStatement ps = conn.prepareStatement(fetchMap);
			ResultSet rs = null;

			rs = ps.executeQuery();

			while (rs.next()) {
				int id = rs.getInt(1);
				String description = rs.getString(2);

				dimension.put(id, description);
			}
			rs.close();
			ps.close();

		}

		catch (SQLException e) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, e);
			throw new GenericException(ExceptionMessages.DB_DATA_PROBLEM, e);
		}

		return dimension;

	}

	/*
	 * insert data from IoBean to the database
	 */
	boolean insertData(Connection conn, IoBean ioBean) throws GenericException {

		boolean insertStatus = false;
		int updateIo = 0;

		try {

			int id = 0;
			String insertData = DBQueries.IOMAP.INSERT_DATA_TO_IO_MAP_INFO;

			Timestamp timestamp = new Timestamp((new Date()).getTime());

			String name = ioBean.getName();
			String description = ioBean.getDescription();
			int deviceModelInfoId = ioBean.getDeviceModelInfoId();
			HashMap<Integer, Integer> ioAttributeMap = ioBean.getIoAttributeMap();
			int portNodeId = ioBean.getOrganization();
			PreparedStatement ps = conn.prepareStatement(insertData);

			ps.setString(1, name);
			ps.setString(2, description);
			Misc.setParamInt(ps, deviceModelInfoId, 3);
			ps.setTimestamp(4, timestamp);
			Misc.setParamInt(ps, portNodeId, 5);
			updateIo = ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (updateIo > 0) {

				insertStatus = true;
			}
			if (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
			insertData = DBQueries.IOMAP.INSERT_DATA_TO_IO_MAP;

			if (ioAttributeMap.size() != 0) {

				PreparedStatement psMap = conn.prepareStatement(insertData);

				Set<Entry<Integer, Integer>> s = ioAttributeMap.entrySet();
				Iterator<Entry<Integer, Integer>> itMap = s.iterator();
				while (itMap.hasNext()) {

					Map.Entry<Integer, Integer> meMap = (Map.Entry<Integer, Integer>) itMap.next();

					int i = ((Integer) meMap.getKey()).intValue();

					psMap.setInt(1, id);
					psMap.setInt(2, i);
					if (ioAttributeMap.get(i) != null) {
						psMap.setInt(3, (Integer) ioAttributeMap.get(i));
					} else {
						
						continue;
					}
					
					psMap.setTimestamp(4, timestamp);
					Misc.setParamInt(psMap,  ioBean.getAttributeDimensionMap().get(i).first, 5);
					Misc.setParamDouble(psMap,  ioBean.getAttributeDimensionMap().get(i).second, 6);
					Misc.setParamDouble(psMap,  ioBean.getAttributeDimensionMap().get(i).third, 7);
					Misc.setParamDouble(psMap,  ioBean.getTransientandvalidonpowerattributeDimensionMap().get(i).first, 8);
					Misc.setParamDouble(psMap,  ioBean.getTransientandvalidonpowerattributeDimensionMap().get(i).second, 9);
					
					psMap.executeUpdate();
				}
				psMap.close();
			}
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

		return insertStatus;
	}

	/*
	 * Update a particular record in the Database
	 */
	boolean updateData(Connection conn, IoBean ioBean) throws GenericException {

		boolean insertStatus = false;

		Timestamp timestamp = new Timestamp((new Date()).getTime());
		int updateIo = 0;
		int id = ioBean.getId();
		String name = ioBean.getName();
		String description = ioBean.getDescription();
		int deviceModelInfoId = ioBean.getDeviceModelInfoId();
		HashMap<Integer, Integer> ioAttributeMap = ioBean.getIoAttributeMap();
		int portNodeId = ioBean.getOrganization();
		try {

			String updateData = DBQueries.IOMAP.UPDATE_IO_MAP_INFO;
			PreparedStatement ps = conn.prepareStatement(updateData);

			ps.setString(1, name);
			ps.setString(2, description);
			Misc.setParamInt(ps, deviceModelInfoId, 3);
			ps.setTimestamp(4, timestamp);
			Misc.setParamInt(ps, portNodeId, 5);
			ps.setInt(6, id);

			updateIo = ps.executeUpdate();
			ps.close();
			String removePreviousMap = DBQueries.IOMAP.DELETE_IO_MAP;

			PreparedStatement psDel = conn.prepareStatement(removePreviousMap);
			psDel.setInt(1, id);
			psDel.executeUpdate();
			psDel.close();
			updateData = DBQueries.IOMAP.INSERT_DATA_TO_IO_MAP;

			if (ioAttributeMap.size() != 0) {
				PreparedStatement psMap = conn.prepareStatement(updateData);
				Set<Entry<Integer, Integer>> s = ioAttributeMap.entrySet();
				Iterator<Entry<Integer, Integer>> itMap = s.iterator();
				while (itMap.hasNext()) {

					Map.Entry<Integer, Integer> meMap = (Map.Entry<Integer, Integer>) itMap.next();

					int i = ((Integer) meMap.getKey()).intValue();

					psMap.setInt(1, id);
					psMap.setInt(2, i);
					if (ioAttributeMap.get(i) != null) {
						psMap.setInt(3, (Integer) ioAttributeMap.get(i));
					} else {
						continue;
					}
					psMap.setTimestamp(4, timestamp);
					Misc.setParamInt(psMap,  ioBean.getAttributeDimensionMap().get(i).first, 5);
					Misc.setParamDouble(psMap,  ioBean.getAttributeDimensionMap().get(i).second, 6);
					Misc.setParamDouble(psMap,  ioBean.getAttributeDimensionMap().get(i).third, 7);
					Misc.setParamDouble(psMap,  ioBean.getTransientandvalidonpowerattributeDimensionMap().get(i).first, 8);
					Misc.setParamDouble(psMap,  ioBean.getTransientandvalidonpowerattributeDimensionMap().get(i).second, 9);
					psMap.executeUpdate();

				}
				psMap.close();
			}
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

		if (updateIo > 0) {
			insertStatus = true;
		}

		return insertStatus;
	}

	/*
	 * Fetch Details for particular record from the database
	 */
	IoBean fetchIo(Connection conn, int id) throws GenericException {

		IoBean ioBean = new IoBean();
		String fetchData = DBQueries.IOMAP.FETCH_IO_MAP_INFO;

		try {
			PreparedStatement ps = conn.prepareStatement(fetchData);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {

				ioBean.setName(rs.getString("name"));
				ioBean.setDescription(rs.getString("description"));
				ioBean.setId(id);
				ioBean.setStatus(Misc.getRsetInt(rs, "status", ACTIVE));
				ioBean.setDeviceModelInfoId(rs.getInt("device_model_info_id"));
				ioBean.setOrganization(Misc.getRsetInt(rs, "port_node_id", 1));
			}

			rs.close();
			ps.close();

			fetchData = DBQueries.IOMAP.FETCH_DEVICE_NAME;
			PreparedStatement psName = conn.prepareStatement(fetchData);
			psName.setInt(1, ioBean.getDeviceModelInfoId());
			rs = psName.executeQuery();
			if (!isNull(rs) && rs.next()) {
				// rs.beforeFirst();
				ioBean.setModelName(rs.getString("name"));
			}
			rs.close();
			psName.close();
			fetchData = DBQueries.IOMAP.FETCH_IO_MAP;
			PreparedStatement psMap = conn.prepareStatement(fetchData);
			psMap.setInt(1, id);

			rs = psMap.executeQuery();

			// rs.beforeFirst();
			while (rs.next()) {
				int ioId = rs.getInt("io_id");
				int attributeId = Misc.getRsetInt(rs, "attribute_id");
				ioBean.setIoAttributeValue(ioId, attributeId);
				ioBean.addToAttributeDimensionMap(ioId, new Triple<Integer, Double, Double>(rs.getInt("dimension_reading_id"), Misc.getRsetDouble(rs, "min_val"), Misc.getRsetDouble(rs, "max_val")));
			    ioBean.addToTransientandvalidonpowerDimensionMap(ioId, new Pair<Integer,Integer>(Misc.getRsetInt(rs, "transient"), Misc.getRsetInt(rs, "validOnPower")));
			}
			rs.close();
			psMap.close();
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
		return ioBean;
	}

	/*
	 * Fetch the List of all the records in the io_map_info table to display on defineDeviceMapping.jsp page
	 */
	ArrayList<IoBean> fetchIoData(Connection conn, SessionManager session) throws GenericException, Exception {
		ArrayList<IoBean> ioList = new ArrayList<IoBean>();
		try {
			String fetch = DBQueries.IOMAP.FETCH_IO_MAP_INFO_DATA;

			PreparedStatement ps = conn.prepareStatement(fetch);
			int v123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
			ps.setInt(1, v123);
			String pgContext = Misc.getParamAsString(session.getParameter("page_context"), "tr_iomapping_list");
			session.getUser().loadParamsFromMenuSpec(session, pgContext);
			com.ipssi.gen.utils.MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(session.request, pgContext);
			
			String topPageContext = searchBoxHelper == null ? "v" : searchBoxHelper.m_topPageContext + "9008";
			ps.setInt(2, Misc.getParamAsInt(session.getParameter(topPageContext),ApplicationConstants.ACTIVE));
			
			
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				IoBean ioBean = new IoBean();

				ioBean.setName(rs.getString("name"));
				ioBean.setDescription(rs.getString("description"));
				ioBean.setId(rs.getInt("id"));
				ioBean.setStatus(Misc.getRsetInt(rs, "status", ACTIVE));
				ioBean.setDeviceModelInfoId(Misc.getRsetInt(rs, "device_model_info_id"));
				ioBean.setOrganization(Misc.getRsetInt(rs, "port_node_id", 1));
				ioList.add(ioBean);
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

		return ioList;
	}

	/*
	 * Delete the IO MAP from the Database; The status of the record is set to "delete"
	 */
	boolean deleteIo(Connection conn, String[] id) throws GenericException {
		boolean result = false;

		int updateIo = 0;

		try {

			String deleteIo = DBQueries.IOMAP.DELETE_IO_MAP_INFO;

			PreparedStatement ps = conn.prepareStatement(deleteIo);
			ArrayList<Integer> ioMapSetIds = new ArrayList<Integer>();
			for (int i = 0; i < id.length; i++) {
				int iomapid = Misc.getParamAsInt(id[i]);
				ps.setInt(1, ApplicationConstants.DELETED);
				ps.setInt(2, iomapid);
				ioMapSetIds.add(iomapid);
				updateIo = ps.executeUpdate();
			}

			ps.close();
			try {
				conn.commit();// sync happens on different connection
				DataProcessorGateway.refreshMapSets(ioMapSetIds);
			} catch (Exception e2) {
				e2.printStackTrace();
				// eat it - though we need to give a warning and send an email or maybe just throw??
			}
			if (updateIo > 0) {
				result = true;
			}
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

		return result;
	}

	/**
	 * @param conn2
	 * @return
	 * @throws GenericException
	 */
	public HashMap<Integer, String> fetchDimensionReadingValueList(Connection conn, int pv123) throws GenericException {
		HashMap<Integer, String> list = new HashMap<Integer, String>();
		;
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.Dimension.FETCH_LIST);
			ps.setInt(1, pv123);
			ps.setInt(2, ApplicationConstants.ACTIVE);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.put(rs.getInt("id"), rs.getString("name"));
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

		return list;
	}

}

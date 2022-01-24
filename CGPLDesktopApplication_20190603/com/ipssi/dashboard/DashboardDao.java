package com.ipssi.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.tracker.common.db.DBQueries;

public class DashboardDao {

	public static ArrayList<Pair<Integer, ArrayList<Integer>>> getContainingAreas(ArrayList<Integer> idList, String csvList) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		ArrayList<Pair<Integer, ArrayList<Integer>>> retval = new ArrayList<Pair<Integer, ArrayList<Integer>>>();
		StringBuilder conatiningRegions = new StringBuilder("select op1.id,set2.op_station_id as contained_op_station "
				+ " from op_station op1 left join regions r1 on ( op1.wait_reg_id = r1.id and op1.status = 1) "
				+ " left join (  select op1.id op_station_id,op1.name op_station_name, r1.id region_id,r1.shape "
				+ " from op_station op1 left join regions r1 on ( op1.wait_reg_id = r1.id  and op1.status = 1) ) "
				+ " set2 on ( op1.id != set2.op_station_id ) where MBRContains( r1.shape , set2.shape ) = 1 and op1.id in ( ");
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (csvList == null) {
				for (int i = 0; i < idList.size(); i++) {
					if (i != 0) {
						conatiningRegions.append(",");
					}
					conatiningRegions.append(idList.get(i));
				}
			} else if (idList == null) {
				conatiningRegions.append(csvList);
			} else {
				return null;
			}
			conatiningRegions.append(")");
			ps = conn.prepareStatement(conatiningRegions.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				ArrayList<Integer> tempList = new ArrayList<Integer>();
				int lastId = rs.getInt("id");
				tempList.add(rs.getInt("contained_op_station"));
				while (rs.next()) {
					int currentId = rs.getInt("id");
					if (currentId != lastId) {
						retval.add(new Pair<Integer, ArrayList<Integer>>(lastId, tempList));
						lastId = currentId;
						tempList = new ArrayList<Integer>();
					} else {
						tempList.add(rs.getInt("contained_op_station"));
					}

				}
				retval.add(new Pair<Integer, ArrayList<Integer>>(lastId, tempList));
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retval;
	}

	public static ArrayList<ShiftDashBoardBean> getTripInfoForShift(Connection conn, Date startDate, int orgId) throws Exception {
		ResultSet rs = null;
		ArrayList<ShiftDashBoardBean> shiftBeanList = new ArrayList<ShiftDashBoardBean>();
		ShiftDashBoardBean shiftBean = null;
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.DASHBOARD.FETCH_SHIFT_TRIP_INFO);
			ps.setInt(1, orgId);
			ps.setDate(2, new java.sql.Date(startDate.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				shiftBean = new ShiftDashBoardBean();
				shiftBean.setOpStationId(rs.getInt(1));
				shiftBean.setRegionId(rs.getInt(2));
				shiftBean.setRegionName(rs.getString(3));
				shiftBean.setNoOfTripsNow(rs.getInt(4));
				shiftBean.setNoOfVehiclesAssignedNow(rs.getInt(5));
				shiftBean.setAvgRoundTripCumm(rs.getDouble(6));
				shiftBeanList.add(shiftBean);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return shiftBeanList;
	}

	public static ArrayList<ShiftDashBoardBean> getShiftInfo(Connection conn, int sId) throws Exception {
		ResultSet rs = null;
		ArrayList<ShiftDashBoardBean> shiftBeanList = new ArrayList<ShiftDashBoardBean>();
		ShiftDashBoardBean shiftBean = null;
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.DASHBOARD.FETCH_SHIFT_SCHEDULE);
			ps.setInt(1, sId);
			rs = ps.executeQuery();
			while (rs.next()) {
				shiftBean = new ShiftDashBoardBean();
				shiftBean.setOpStationId(rs.getInt("source"));
				shiftBean.setNoOfTripsTargeted(rs.getInt("number_of_trips"));
				shiftBean.setNoOfVehiclesAssignedPlanned(rs.getInt("number_of_vehicles"));
				shiftBeanList.add(shiftBean);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return shiftBeanList;
	}

	public static TripInfoBean getTripInfo(Connection conn, int vehicleId, String eventDate) throws Exception {
		System.out.println("DashboardDao.getTripInfo()  :  vehicleId :  " + vehicleId);
		System.out.println("DashboardDao.getTripInfo()  :  eventDate :  " + eventDate);
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date dt = null;
		try {
			dt = sdfTime.parse(eventDate);
		} catch (ParseException e2) {
			dt = sdf.parse(eventDate);
		}
		ResultSet rs = null;
		TripInfoBean tripInfoBean = null;
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.DASHBOARD.FETCH_TRIP_INFO_VEH);
			ps.setInt(1, vehicleId);
			ps.setString(2, indepDateFormat.format(dt));
			ps.setString(3, indepDateFormat.format(dt));
			System.out.println("DashboardDao.getTripInfo()  :  ps :  " + ps);
			rs = ps.executeQuery();
			while (rs.next()) {
				tripInfoBean = new TripInfoBean();
				tripInfoBean.setTripId(rs.getInt("id"));
				tripInfoBean.setVehicleId(rs.getInt("vehicle_id"));
				tripInfoBean.setWaitInLoad(rs.getTimestamp("load_area_wait_in"));
				tripInfoBean.setGateInLoad(rs.getTimestamp("load_gate_in"));
				tripInfoBean.setAreaInLoad(rs.getTimestamp("load_area_in"));
				tripInfoBean.setAreaOutLoad(rs.getTimestamp("load_area_out"));
				tripInfoBean.setGateOutLoad(rs.getTimestamp("load_gate_out"));
				tripInfoBean.setWaitInUnload(rs.getTimestamp("unload_area_wait_in"));
				tripInfoBean.setGateInUnload(rs.getTimestamp("unload_gate_in"));
				tripInfoBean.setAreaInUnload(rs.getTimestamp("unload_area_in"));
				tripInfoBean.setAreaOutUnload(rs.getTimestamp("unload_area_out"));
				tripInfoBean.setGateOutUnload(rs.getTimestamp("unload_gate_out"));
				tripInfoBean.setShiftDate(rs.getTimestamp("shift_date"));
			}
			System.out.println("DashboardDao.getTripInfo()  ::  " + tripInfoBean);
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return tripInfoBean;
	}

	public static TripInfoBean getTripInfo(Connection conn, int tripId) throws Exception {
		ResultSet rs = null;
		TripInfoBean tripInfoBean = null;
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.DASHBOARD.FETCH_TRIP_INFO);
			ps.setInt(1, tripId);
			rs = ps.executeQuery();
			while (rs.next()) {
				tripInfoBean = new TripInfoBean();
				tripInfoBean.setTripId(rs.getInt("id"));
				tripInfoBean.setVehicleId(rs.getInt("vehicle_id"));
				tripInfoBean.setWaitInLoad(rs.getDate("load_area_wait_in"));
				tripInfoBean.setGateInLoad(rs.getDate("load_gate_in"));
				tripInfoBean.setAreaInLoad(rs.getDate("load_area_in"));
				tripInfoBean.setAreaOutLoad(rs.getDate("load_area_out"));
				tripInfoBean.setGateOutLoad(rs.getDate("load_gate_out"));
				tripInfoBean.setWaitInUnload(rs.getDate("unload_area_wait_in"));
				tripInfoBean.setGateInUnload(rs.getDate("unload_gate_in"));
				tripInfoBean.setAreaInUnload(rs.getDate("unload_area_in"));
				tripInfoBean.setAreaOutUnload(rs.getDate("unload_area_out"));
				tripInfoBean.setGateOutUnload(rs.getDate("unload_gate_out"));
				tripInfoBean.setShiftDate(rs.getDate("shift_date"));
			}
			System.out.println("DashboardDao.getTripInfo()  ::  " + tripInfoBean);
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return tripInfoBean;
	}

	public static void main(String[] args) {
		try {
			HashMap<Integer, Pair<Date, String>> tr = getVehicleCurrentData(DBConnectionPool.getConnectionFromPoolNonWeb(), "15150,15151,15152");
			System.out.println("DashboardDao.main()");
		} catch (GenericException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashMap<Integer, Pair<Date, String>> getVehicleCurrentData(Connection conn, String vehStr) throws Exception {
		ResultSet rs = null;
		HashMap<Integer, Pair<Date, String>> vehCurrDataMap = new HashMap<Integer, Pair<Date, String>>();
		if ( vehStr.trim().length() == 0 )
			return null;
		try {
			StringBuilder query = new StringBuilder(DBQueries.DASHBOARD.FETCH_CURRENT_DATA);
			String queStr = query.replace(query.indexOf("#"), query.indexOf("#") + 1, vehStr).toString();
			queStr.replaceFirst("#", vehStr);
			PreparedStatement ps = conn.prepareStatement(queStr);
			// ps.setString(1, vehStr);
			System.out.println("DashboardDao.getVehicleCurrentData() :  DBQueries.DASHBOARD.FETCH_CURRENT_DATA  :  " + DBQueries.DASHBOARD.FETCH_CURRENT_DATA);
			System.out.println("DashboardDao.getVehicleCurrentData() :  vehStr   :   " + vehStr);
			System.out.println("DashboardDao.getVehicleCurrentData() :  ps    : " + ps);
			rs = ps.executeQuery();
			Pair<Date, String> pair = null;
			while (rs.next()) {
				pair = new Pair<Date, String>(rs.getTimestamp("gps_record_time"), Misc.getParamAsString(rs.getString("name")));
				vehCurrDataMap.put(rs.getInt("vehicle_id"), pair);
			}
			System.out.println("DashboardDao.getVehicleCurrentData() :  vehCurrDataMap.size()    : " + vehCurrDataMap.size());
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return vehCurrDataMap;
	}

	public static ArrayList<EngineEventBean> getShiftEngineEvents(Connection conn, Date startDate, int orgId) throws Exception {
		ResultSet rs = null;
		ArrayList<EngineEventBean> shiftBeanList = new ArrayList<EngineEventBean>();
		EngineEventBean shiftBean = null;
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.DASHBOARD.FETCH_SHIFT_ENGINE_EVENTS);
			ps.setInt(1, orgId);
			ps.setDate(2, new java.sql.Date(startDate.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				// shiftBean = new EngineEventBean();
				// shiftBean.setOpStationId(rs.getInt(1));
				// shiftBean.setRegionId(rs.getInt(2));
				// shiftBean.setRegionName(rs.getString(3));
				// shiftBean.setNoOfTripsNow(rs.getInt(4));
				// shiftBean.setNoOfVehiclesAssignedNow(rs.getInt(5));
				// shiftBean.setAvgRoundTripCumm(rs.getDouble(6));
				// shiftBeanList.add(shiftBean);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return shiftBeanList;
	}

	public ArrayList<OpstationOperationalStatus> getOpstationStatus(String valList, Connection connection) {
		ArrayList<OpstationOperationalStatus> list = new ArrayList<OpstationOperationalStatus>();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			StringBuilder buffer = new StringBuilder(
					"select Max(op_station_operational_status.id),op_station.name,op_station.id,op_station_operational_status.status,begin_time,end_time,reason,updated_on from op_station left join ( select * from op_station_operational_status order by id desc ) op_station_operational_status on ( op_station.id = op_station_operational_status.op_station_id) where op_station.id in  (");
			buffer.append(valList).append(") group by op_station.id");
			ps = connection.prepareStatement(buffer.toString());

			rs = ps.executeQuery();
			while (rs.next()) {
				OpstationOperationalStatus bean = new OpstationOperationalStatus();
				bean.setOpStationId(rs.getInt("id"));
				bean.setStatus(rs.getInt("status"));
				bean.setStartTime(rs.getTimestamp("begin_time"));
				bean.setEndTime(rs.getTimestamp("end_time"));
				bean.setReason(rs.getInt("reason"));
				bean.setOpName(rs.getString("name"));
				list.add(bean);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public void insertOperationalStatus(Connection connection, ArrayList<OpstationOperationalStatus> opList) {
		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(DBQueries.OperationStatus.INSERT_OP_STATUS);
			Timestamp updatedOn = new Timestamp(new Date().getTime());
			for (int i = 0; i < opList.size(); i++) {
				OpstationOperationalStatus bean = opList.get(i);
				// insert into op_station_operational_status(op_station_id,status,begin_time,end_time,updated_on,reason)
				ps.setInt(1, bean.getOpStationId());
				ps.setInt(2, bean.getStatus());
				ps.setTimestamp(3, bean.getStartTime());
				if (bean.getEndTime() == null) {
					ps.setNull(4, java.sql.Types.TIMESTAMP);
				} else {
					ps.setTimestamp(4, bean.getEndTime());
				}
				ps.setTimestamp(5, updatedOn);
				ps.setInt(6, bean.getReason());
				ps.addBatch();
			}

			ps.executeBatch();
			if ( !connection.getAutoCommit()){
				connection.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}

package com.ipssi.android;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;

public class AdaniSidingRakeDetails {

	// public static int onDeviceAttach(Connection conn, int deviceId,
	// String deviceNumber, int rakeNumber, String engineNumber,
	// String attachTime, int vehicleId, int userId) {
	// int insertId = 0;
	// try {
	// String query =
	// "insert into rake_details (device_id,device_number,rake_number,engine_number,device_attach_time,status,created_by,vehicle_id) values(?,?,?,?,?,?,?,?)";
	// System.out.println("[onDeviceAttach] " + query);
	// PreparedStatement st = conn.prepareStatement(query);
	// int index = 1;
	// st.setInt(index++, deviceId);
	// st.setString(index++, deviceNumber);
	// st.setInt(index++, rakeNumber);
	// st.setString(index++, engineNumber);
	// st.setString(index++, attachTime);
	// st.setInt(index++, 1);
	// st.setInt(index++, userId);
	// st.setInt(index++, vehicleId);
	// st.execute();
	// ResultSet generatedKeys = st.getGeneratedKeys();
	// if (generatedKeys.first()) {
	// insertId = generatedKeys.getInt(1);
	// }
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// System.out.println("[onDeviceAttach] insertId = " + insertId);
	// return insertId;
	// }

	public static String onDeviceAttach(Connection conn, int deviceId,
			String deviceNumber, int rakeNumber, String engineNumber,
			String attachTime, int userId) {
		int insertId = 0;
		if(getActiveVehicleList(conn,deviceId).length()<5){
		try {
			String query = "insert into challan_details(port_node_id,vehicle_id,challan_no,truck_no,from_location,to_location,"
					+ "challan_date,gr_no_,datefield1,updated_on,challan_rec_date,trip_status,status)"
					+ " values(1237,?,?,?,?,?,?,?,?,now(),now(),1,1)";
			System.out.println("[onDeviceAttach] " + query);
			PreparedStatement st = conn.prepareStatement(query);
			int index = 1;
			st.setInt(index++, deviceId);
			st.setInt(index++, Misc.getParamAsInt(engineNumber));
			st.setString(index++, deviceNumber);
			st.setString(index++, "Surajpur");
			st.setString(index++, "Parsa Kente");

			st.setString(index++, attachTime);
			st.setString(index++, String.valueOf(rakeNumber));
			st.setString(index++, attachTime);
			st.execute();
			ResultSet generatedKeys = st.getGeneratedKeys();
			if (generatedKeys.first()) {
				insertId = generatedKeys.getInt(1);
				// updateRakeNumber(conn, rakeNumber);
			}
		} catch (SQLIntegrityConstraintViolationException e) {
			insertId = -2;
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("[onDeviceAttach] insertId = " + insertId);
		
		return insertId>0?"Saved Successfully":"Something went wrong";
		}else{
			return "vehicle already allocated";
		}
	}

	public static String onDeviceDetach(Connection conn, int id,
			String deviceDetachTime) {
		int updateId = 0;
		try {
			String query = "update challan_details set datefield2=?,status=2 where id=?";
			System.out.println("[onDeviceDetach] " + query);
			PreparedStatement st = conn.prepareStatement(query);
			int index = 1;
			st.setString(index++, deviceDetachTime);
			st.setInt(index++, id);
			updateId = st.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("[onDeviceDetach] updateId " + updateId);
		return updateId>0?"Saved Successfully":"unable to save";
	}

	public static int updateSidingProgress(Connection conn, int id,
			String cleaningStart, String cleaningEnd, String engineDetach,
			String engineAttach, String loadedFrom, String updatedBy) {
		int updateId = 0;
		try {
			int index = 0;
			StringBuilder query = new StringBuilder();
			query.append("update challan_details set ");
			String[] params = new String[4];
			if (cleaningStart.length() > 0) {
				query.append("datefield3 = ?");
				params[index++] = cleaningStart;
			}
			// if (engineDetach.length() > 0) {
			// query.append(index > 0 ? "," : "");
			// query.append("engine_detach_time = ?");
			// params[index++] = engineDetach;
			// }
			// if (engineAttach.length() > 0) {
			// query.append(index > 0 ? "," : "");
			// query.append("engine_attach_time = ?");
			// params[index++] = engineAttach;
			// }
			if (cleaningEnd.length() > 0) {
				query.append(index > 0 ? "," : "");
				query.append("datefield4 = ?");
				params[index++] = cleaningEnd;
			}
			query.append(",description = ? where id=?");
			PreparedStatement st = conn.prepareStatement(query.toString());
			for (int i = 0; i < index; i++) {
				st.setString((i + 1), params[i]);
			}
			st.setString(++index, loadedFrom);
			st.setInt(++index, id);
			updateId = st.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("[update details] updateId " + updateId);
		return updateId;
	}

	public static String getAllDetails(Connection conn, int roleId) {
		JSONArray array = new JSONArray();
		try {
			// 116 for siding manager
			// 114 for device installer
			String query = "select * from rake_details where status = 1";
			System.out.println("[getAllDetails] " + query);
			PreparedStatement st = conn.prepareStatement(query);
			ResultSet resultSet = st.executeQuery();
			while (resultSet.next()) {
				JSONObject object = new JSONObject();
				int id = resultSet.getInt("id");
				int deviceId = resultSet.getInt("device_id");
				String deviceNumber = resultSet.getString("device_number");
				String rakeNumber = resultSet.getString("rake_number");
				String engineNumber = resultSet.getString("engine_number");
				String deviceAttachTime = resultSet
						.getString("device_attach_time");
				String cleaningStart = resultSet
						.getString("cleaning_start_time");
				String cleaningEnd = resultSet.getString("cleaning_end_time");
				String engineDetach = resultSet.getString("engine_detach_time");
				String engineAttach = resultSet.getString("engine_attach_time");
				String loadedFrom = resultSet.getString("loaded_from");
				object.put("id", id);
				object.put("device_id", deviceId);
				object.put("device_number", deviceNumber);
				object.put("rake_number", rakeNumber);
				object.put("engine_number", engineNumber);
				object.put("device_attach_time", deviceAttachTime);
				object.put("cleaning_start", cleaningStart);
				object.put("cleaning_end", cleaningEnd);
				object.put("engine_detach", engineDetach);
				object.put("engine_attach", engineAttach);
				object.put("loaded_from", loadedFrom);
				array.put(object);
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("[getAllDetails] " + array.toString());
		return (array.length() > 0) ? array.toString() : "-1";
	}

	public static String validateUser(Connection conn, String userName,
			String password) {
		JSONObject object = new JSONObject();
		String query = "select users.id,user_roles.role_id from users left outer "
				+ "join user_roles on users.id=user_roles.user_1_id where USERNAME like ? and password like ? and user_roles.role_id in (114,116,118) limit 1";
		System.out.println("[validateUser] " + query);
		try {
			PreparedStatement st = conn.prepareStatement(query);
			st.setString(1, userName);
			st.setString(2, password);
			ResultSet resultSet = st.executeQuery();
			if (resultSet.next()) {
				int id = resultSet.getInt(1);
				String roleId = resultSet.getString(2);
				object.put("id", id);
				object.put("role_id", roleId);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("[validateUser] " + object.toString());
		return object.toString();
	}

	// public static int updateDeviceOnVehicle(Connection conn,
	// int deviceInternalId, String engineNumber, int rakeNumber,
	// int userId, int portNodeId) {
	// int vehicleId = Misc.getUndefInt();
	// try {
	// String query =
	// "select id from vehicle where name = ? order by status asc  limit 1";
	// System.out.println("[updateDeviceOnVehicle] " + query);
	// PreparedStatement st = conn.prepareStatement(query);
	// st.setString(1, engineNumber);
	// ResultSet rs = st.executeQuery();
	// if (rs.next()) {
	// vehicleId = rs.getInt(1);
	// updateVehicleById(conn, deviceInternalId, rakeNumber, vehicleId);
	//
	// helperAddHistory(conn, vehicleId, deviceInternalId,
	// engineNumber, userId);
	// updateRakeNumber(conn, rakeNumber);
	// } else {
	// vehicleId = insertVehicle(conn, deviceInternalId, engineNumber,
	// rakeNumber, userId, portNodeId);
	// updateRakeNumber(conn, rakeNumber);
	// }
	// conn.commit();
	//
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	//
	// System.out.println("[updateDeviceOnVehicle] vehicleId " + vehicleId);
	// return vehicleId;
	// }

	// private static void updateRakeNumber(Connection conn, int rakeNumber) {
	// PreparedStatement ps = null;
	// try {
	// String insertQuery = "insert into rake_counter values(?)";
	// String query = "update rake_counter set max_rake=?";
	// System.out.println("[UpdateRakeNumber ] " + query);
	// ps = conn.prepareStatement(query);
	// int index = 1;
	// ps.setInt(index++, rakeNumber);
	// int executeUpdate = ps.executeUpdate();
	// if (executeUpdate == 0) {
	// ps.close();
	// ps = conn.prepareStatement(insertQuery);
	// ps.setInt(1, rakeNumber);
	// ps.executeQuery();
	// }
	// } catch (Exception e) {
	// System.out.println(e);
	// } finally {
	// if (ps != null) {
	// try {
	// ps.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	// public static String getDeviceList(DimInfo dimInfo) {
	// JSONArray dataList = new JSONArray();
	// if (dimInfo == null)
	// return null;
	// try {
	// ArrayList valList = dimInfo.getValList();
	// for (int i = 0, count = valList == null ? 0 : valList.size(); i < count;
	// i++) {
	// JSONObject object = new JSONObject();
	// DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
	// int id = valInfo.m_id;
	// String nameStr = valInfo.m_name;
	// object.put("id", id);
	// object.put("name", nameStr);
	// dataList.put(object);
	// }
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// System.out.println("[getDeviceList] " + dataList.toString());
	// return dataList.toString();
	// }

	// private static int insertVehicle(Connection conn, int deviceInternalId,
	// String engineNumber, int rakeNumber, int userId, int portNodeId) {
	// System.out.println(" ######## Insert New Vehicle ######");
	// PreparedStatement ps = null;
	//
	// int insertId = 0;
	// try {
	// String query =
	// "insert into vehicle (name, std_name, fieldeight, do_rule, do_trip, device_internal_id, status, updated_by, customer_id) values (?,?,?,?,?,?,?,?,?)";
	// ps = conn.prepareStatement(query);
	// System.out.println("[insertVehicle] " + query);
	// int index = 1;
	// ps.setString(index++, engineNumber);
	// ps.setString(index++, CacheTrack.standardizeName(engineNumber));
	// ps.setInt(index++, rakeNumber);
	// ps.setInt(index++, 1);
	// ps.setInt(index++, 1);
	// ps.setInt(index++, deviceInternalId);
	// ps.setInt(index++, 1);
	// ps.setInt(index++, userId);
	// ps.setInt(index++, portNodeId);
	// ps.execute();
	// ResultSet generatedKeys = ps.getGeneratedKeys();
	// if (generatedKeys.first()) {
	// insertId = generatedKeys.getInt(1);
	// System.out.println("[GeneratedKey] " + insertId);
	//
	// updateVehicleExtended(conn, insertId, userId);
	// }
	//
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// return insertId;
	// }
	//
	// private static boolean updateVehicleExtended(Connection conn,
	// int vehicleId, int userId) {
	// System.out.println(" ######## Insert New Vehicle ######");
	// PreparedStatement ps = null;
	// java.util.Date sysDate = new java.util.Date();
	// java.sql.Timestamp now = Misc.utilToSqlDate(sysDate);
	// try {
	// String query =
	// "insert into vehicle_extended (vehicle_id, extended_status,date_field1) values (?,?,?)";
	// System.out.println("[updateVehicleExtended] " + query);
	// ps = conn.prepareStatement(query);
	// int index = 1;
	// ps.setInt(index++, vehicleId);
	// ps.setInt(index++, 1);
	// ps.setTimestamp(index++, now);
	// ps.execute();
	//
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// return true;
	//
	// }
	//
	// private static void updateVehicleById(Connection conn,
	// int deviceInternalId, int maxRake, int vehicleId) {
	// PreparedStatement ps = null;
	// try {
	// String query =
	// "update vehicle set device_Internal_Id=?,field_eight=? status=1 where id=?";
	// System.out.println("[updateVehicleById] query=" + query);
	// ps = conn.prepareStatement(query);
	// int index = 1;
	// ps.setInt(index++, deviceInternalId);
	// ps.setInt(index++, maxRake);
	// ps.setInt(index++, vehicleId);
	// ps.executeUpdate();
	// } catch (Exception e) {
	// System.out.println(e);
	// } finally {
	// if (ps != null) {
	// try {
	// ps.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	//
	// private static void helperAddHistory(Connection conn, int vehicleId,
	// int deviceInternalId, String rakeNumber, int userId) {
	//
	// java.util.Date sysDate = new java.util.Date();
	// java.sql.Timestamp now = Misc.utilToSqlDate(sysDate);
	// try {
	// // insert into vehicle_history (vehicle_id, device_id, on_date,
	// // description, change_code, change_from, change_to, addnl_note,
	// // addnl_code, updated_by) values (?,?,?,?,?,?,?,?,?, ?)";
	// String query =
	// "insert into vehicle_history (vehicle_id, device_id,on_date, updated_by) values (?,?,?,?)";
	// PreparedStatement st = conn.prepareStatement(query);
	// int index = 1;
	// st.setInt(index++, vehicleId);
	// st.setInt(index++, deviceInternalId);
	// st.setTimestamp(index++, now);
	// st.setInt(index++, userId);
	// System.out.println("[helperAddHistory] query=" + query);
	// st.execute();
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// }
	//
	// public static int getRakeNumber(Connection conn) {
	// int rakeNumber = Misc.getUndefInt();
	// ResultSet rs = null;
	// PreparedStatement ps = null;
	// String query = "select max_rake from rake_counter limit 1";
	// System.out.println("[getRakeNumber] query=" + query);
	// try {
	// ps = conn.prepareStatement(query);
	// rs = ps.executeQuery();
	// if (rs.next())
	// rakeNumber = rs.getInt(1);
	//
	// } catch (Exception e) {
	// System.out.println(e);
	// } finally {
	// try {
	// if (ps != null)
	// ps.close();
	// if (rs != null)
	// rs.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// System.out.println("[getRakeNumber] rakeNumber = " + rakeNumber);
	// return rakeNumber;
	// }

	public static String getRakePlan(Connection conn, String onDate) {
		ResultSet rs = null;
		JSONObject retval = new JSONObject();
		PreparedStatement ps = null;

		String query = "select * from rake_plan where on_date = ?";
		try {

			System.out.println("[checkRakePlan] " + query);
			ps = conn.prepareStatement(query);
			ps.setString(1, onDate);
			rs = ps.executeQuery();

			if (rs.next()) {
				retval.put("onDate", rs.getString("on_date"));
				retval.put("total_rake", rs.getInt("total_rake"));
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[getRakePlane]retval=" + retval);
		return retval.toString();
	}

	public static int setRakePlan(Connection conn, String onDate,
			int totalRake, boolean isUpdate) {
		PreparedStatement ps = null;

		int retval = 0;
		try {
			String updateQuery = "update rake_plan set total_rake=? where on_date=?";
			if (isUpdate) {
				System.out.println("[checkRakePlan] " + updateQuery);
				ps = conn.prepareStatement(updateQuery);
				ps.setInt(1, totalRake);
				ps.setString(2, onDate);
				retval = ps.executeUpdate();
			} else {
				String query = "insert into rake_plan (on_date,total_rake) values (?,?)";
				System.out.println("[setRakePlan] " + query);
				ps = conn.prepareStatement(query);
				int index = 1;
				ps.setString(index++, onDate);
				ps.setInt(index++, totalRake);
				ps.execute();
				ResultSet generatedKeys = ps.getGeneratedKeys();
				if (generatedKeys.next()) {
					retval = generatedKeys.getInt(1);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("[setRakePlan] insertId=" + retval);
		return retval;
	}

	public static int getVehicleId(Connection conn, int rowId) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		int retval = Misc.getUndefInt();
		String query = "select vehicle_id from rake_details where id = ?";
		try {

			System.out.println("[getVehicleId] " + query);
			ps = conn.prepareStatement(query);
			ps.setInt(1, rowId);
			rs = ps.executeQuery();

			if (rs.next()) {
				retval = rs.getInt("vehicle_id");
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return retval;
	}

	public static void refreshVehicleCache(Connection conn, int vehicleId) {
		ArrayList<Integer> vehList = new ArrayList<Integer>();

		if (vehicleId > 0) {
			vehList.add(vehicleId);
			try {
				CacheTrack.VehicleSetup.loadSetup(conn, vehList, false, true);// force
				// full
				// initialization
				// CacheTrack.refreshVehicles(vehList, Misc.getServerName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getMobileNumbers(Connection conn) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		StringBuilder sb = new StringBuilder();
		String query = "select mobile from rake_mobiles";
		try {

			System.out.println("[getMobileNumbers] " + query);
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				String mobile = rs.getString("mobile");
				sb.append(sb.length() > 0 ? "," + mobile : mobile);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String getAvailableVehicleList(Connection conn) {
		ResultSet rs = null;
		JSONArray array = new JSONArray();
		PreparedStatement ps = null;
		ArrayList<String> vehiclsId = new ArrayList<String>();
		String query = "select id,name,device_internal_id from vehicle where vehicle.status = 1 and vehicle.customer_id = 1237";
		String challanQuery = "select vehicle_id from challan_details where status=1";
		try {

			System.out.println("[getMobileNumbers] " + challanQuery);
			ps = conn.prepareStatement(challanQuery);
			rs = ps.executeQuery();

			while (rs.next()) {
				String id = rs.getString("vehicle_id");
				vehiclsId.add(id);
			}

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				String id = rs.getString("id");
				if (!vehiclsId.contains(id)) {
					JSONObject object = new JSONObject();
					String name = rs.getString("name");
					String internalId = rs.getString("device_internal_id");
					object.put("id", id);
					object.put("name", name);
					object.put("internal_id", internalId);
					array.put(object);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return array.toString();
	}

	public static String getActiveVehicleList(Connection conn, int deviceId) {
		ResultSet rs = null;
		JSONArray array = new JSONArray();
		PreparedStatement ps = null;
		String query = "select challan_details.id,vehicle_id,challan_no,truck_no,gr_no_,datefield1,datefield3,datefield4,description,vehicle.device_internal_id from challan_details"
				+ " left outer join vehicle on vehicle.id= challan_details.vehicle_id where challan_details.status=1";
		if (deviceId > 0) {
			query = query + " and challan_details.vehicle_id=" + deviceId;
		}
		try {

			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				JSONObject object = new JSONObject();
				String id = rs.getString("id");
				String vehicleId = rs.getString("vehicle_id");
				String challanNo = rs.getString("challan_no");
				String truckNo = rs.getString("truck_no");
				String grNo = rs.getString("gr_no_");
				String dateField1 = rs.getString("datefield1");

				String dateField3 = rs.getString("datefield3");
				String dateField4 = rs.getString("datefield4");
				String description = rs.getString("description");
				String internalId = rs.getString("device_internal_id");

				object.put("id", id);
				object.put("device_id", vehicleId);
				object.put("rake_number", challanNo);
				object.put("device_number", truckNo);
				object.put("engine_number", grNo);
				object.put("device_attach_time", dateField1);

				object.put("cleaning_start", dateField3);
				object.put("cleaning_end", dateField4);
				object.put("loaded_from", description);
				object.put("internal_id", internalId);
				array.put(object);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return array.toString();
	}
}

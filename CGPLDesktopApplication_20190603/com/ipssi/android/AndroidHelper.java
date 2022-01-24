package com.ipssi.android;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

//import antlr.collections.List;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class AndroidHelper {

	public static double helpConvertKMInDegree(double dist) {
		return (dist * 0.00904);
	}

	public static Triple<Integer, String, String> getDriverDetailById(
			Connection conn, String driverId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "SELECT id, driver_name, driver_dl_number FROM driver_details WHERE id = ? ";
		Triple<Integer, String, String> retVal = null;
		try {
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, driverId, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				retVal = new Triple<Integer, String, String>(Misc.getRsetInt(
						rs, 1), rs.getString(2), rs.getString(3));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return retVal;
	}

	public static int insertDriverDetail(Connection conn, String driverName,
			int status, String driverDLNo) {
		PreparedStatement ps = null;
		int colPos = 1;
		int id = Misc.getUndefInt();
		ResultSet rs = null;
		String INSERT_DRIVER_DETAIL = "insert into driver_details "
				+ " (driver_name, driver_dl_number,status) " + " values "
				+ " (?,?,?)";
		try {

			ps = conn.prepareStatement(INSERT_DRIVER_DETAIL);
			// Misc.setParamInt(ps, type, colPos++);
			// ps.setTimestamp(colPos++, new Timestamp(driverDOB.getTime()));
			ps.setString(colPos++, driverName);
			ps.setString(colPos++, driverDLNo);
			ps.setInt(colPos++, status);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			id = -1;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}

	public static ArrayList<Pair<Integer, String>> getTransporterList(
			Connection conn, int portNodeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer, String>> transporterList = new ArrayList<Pair<Integer, String>>();
		String query = "SELECT transporter_details.id, transporter_details.name FROM transporter_details  where transporter_details.status=1 and port_node_id = ? ";

		try {
			ps = conn.prepareStatement(query);
			int paramIndex = 1;
			ps.setInt(1, portNodeId);
			rs = ps.executeQuery();
			while (rs.next()) {
				transporterList.add(new Pair<Integer, String>(Misc.getRsetInt(
						rs, 1), rs.getString(2)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return transporterList;
	}
	
	public static ArrayList<Pair<Integer,String>> getVehicleListWithShortName(Connection conn, String text, int portNodeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer, String>> vehList = new ArrayList<Pair<Integer, String>>();
		String query = " select vehicle.id," +
				" (case when vehicle.fieldfive is not null and length(vehicle.fieldfive) > 0 then vehicle.fieldfive else vehicle.std_name end) as vehicle_name" +
				" from vehicle join "
				+ " (select distinct(vehicle.id) vehicle_id from vehicle "
				+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
				+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
				+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
				+ " join port_nodes anc  on (anc.id in ("
				+ portNodeId
				+ ") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
				+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))) vi on vi.vehicle_id = vehicle.id "
				+ " where status in (1)";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				vehList.add(new Pair(Misc.getRsetInt(rs, 1), rs.getString(2)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return vehList;
	}

	public static ArrayList<Pair<Integer, String>> getVehicleList(
			Connection conn, String text, int portNodeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer, String>> vehList = new ArrayList<Pair<Integer, String>>();
		String query = " select vehicle.id,vehicle.std_name from vehicle join "
				+ " (select distinct(vehicle.id) vehicle_id from vehicle "
				+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
				+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
				+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
				+ " join port_nodes anc  on (anc.id in ("
				+ portNodeId
				+ ") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
				+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)))) vi on vi.vehicle_id = vehicle.id "
				+ " where status in (1) and vehicle.std_name like '%" + text
				+ "%'";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				vehList.add(new Pair(Misc.getRsetInt(rs, 1), rs.getString(2)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return vehList;
	}

	public static int insertVehicleDetail(Connection conn, String vehicleName,
			int status, int portNodeId) {
		PreparedStatement ps = null;
		int colPos = 1;
		int id = Misc.getUndefInt();
		ResultSet rs = null;
		String INSERT_VEHICLE_DETAIL = "insert into vehicle "
				+ " (name, customer_id, status) " + " values " + " (?,?,?)";
		try {

			ps = conn.prepareStatement(INSERT_VEHICLE_DETAIL);
			ps.setString(colPos++, vehicleName);
			ps.setInt(colPos++, portNodeId);
			ps.setInt(colPos++, status);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			id = -1;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}

	public static int insertInspectionVehicleDetails(Connection conn,
			int vehicle_id, int transporter_id, int driver_id, int portNodeId) {
		PreparedStatement ps = null;
		int colPos = 1;
		int id = Misc.getUndefInt();
		ResultSet rs = null;

		String INSERT_VEHICLE_DETAIL = "insert into inspection_vehicle_details "
				+ " (vehicle_id, transporter_id, driver_id,port_node_id) "
				+ " values " + " (?,?,?,?)";
		try {

			ps = conn.prepareStatement(INSERT_VEHICLE_DETAIL);
			ps.setInt(colPos++, vehicle_id);
			ps.setInt(colPos++, transporter_id);
			ps.setInt(colPos++, driver_id);
			ps.setInt(colPos++, portNodeId);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			id = -1;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}

	public static String getInspectionVehicleDetails(Connection conn, int id,
			int portNodeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder builder = new StringBuilder();
		// ArrayList<Pair<Integer, String>> transporterList = new
		// ArrayList<Pair<Integer, String>>();
		String query = "SELECT vehicle.name ,driver_details.driver_name,"
				+ "driver_details.ddt_training_expiry_date,"
				+ "driver_details.ddt_training_date,"
				+ "inspection_vehicle_details.vehicle_id,"
				+ "driver_details.status from inspection_vehicle_details "
				+ "left outer join vehicle on vehicle.id=inspection_vehicle_details.vehicle_id "
				+ "left outer join driver_details on inspection_vehicle_details.driver_id=driver_details.id "
				+ "where inspection_vehicle_details.id=? AND inspection_vehicle_details.port_node_id=?";

		try {
			ps = conn.prepareStatement(query);
			int paramIndex = 1;
			ps.setInt(paramIndex++, id);
			ps.setInt(paramIndex, portNodeId);
			rs = ps.executeQuery();
			String driverAction = "";
			while (rs.next()) {
				if (Misc.getRsetInt(rs, 6, -1) == 0) {
					driverAction = "Blocked";
				} else {
					Timestamp timestamp = rs.getTimestamp(3);
					System.out.println(timestamp + ">>>>>>"
							+ new Timestamp(System.currentTimeMillis()));
					if (timestamp != null
							&& timestamp.compareTo(new Timestamp(System
									.currentTimeMillis())) < 0) {
						driverAction = "DDT Needed";
					} else {
						driverAction = "OK";
					}
				}

				builder.append("{\"vehicle_name\":" + "\"" + rs.getString(1)
						+ "\"" + ",\"driver_name\":" + "\"" + rs.getString(2)
						+ "\"" + ",\"ddt_training_expiry_date\":" + "\""
						+ rs.getTimestamp(3) + "\"" + ",\"ddt_training_date\":"
						+ "\"" + rs.getTimestamp(4) + "\"" + ",\"vehicle_id\":"
						+ "\"" + rs.getString(5) + "\"" + ",\"status\":" + "\""
						+ driverAction + "\" }");
				// transporterList.add(new Pair<Integer,
				// String>(Misc.getRsetInt(rs, 1), rs.getString(2)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return builder.toString();
	}

	public static ArrayList<InspectionQuestion> getQuestionList(
			Connection conn, int type) {
		PreparedStatement ps = null;
		int colPos = 1;
		ResultSet rs = null;
		ArrayList<InspectionQuestion> questions = new ArrayList<InspectionQuestion>();
		String QUERY = "SELECT id,query_text,query_desc,is_mandatory,is_photo from inspection_query_details WHERE query_type=?";
		try {

			ps = conn.prepareStatement(QUERY);
			ps.setInt(colPos, type);
			// ps.setInt(colPos, portNodeId);
			rs = ps.executeQuery();

			while (rs.next()) {
				int id = rs.getInt(1);
				String query = rs.getString(2);
				String desc = rs.getString(3);
				boolean isMandatory = rs.getBoolean(4);
				boolean isPhoto = rs.getBoolean(5);
				questions.add(new InspectionQuestion(id, query, desc,
						isMandatory, isPhoto));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			questions = null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return questions;
	}

	public static int insertResultArray(Connection conn,
			ArrayList<InspectionQuestion> list, String observation) {
		PreparedStatement ps = null, statement = null;
		int colPos = 1;
		int id = Misc.getUndefInt();
		ResultSet rs = null;
		String QUERY_INSERT_OBSERVATION = "UPDATE inspection_vehicle_details SET observation=?,date_of_inspection=? where id=?";
		String INSERT_REPORT = "insert into inspection_report "
				+ " (inspection_vehicle_detail_id, query_id, result,photo,explanation,mandatory_to_complete) "
				+ " values " + " (?,?,?,?,?,?)";
		try {

			for (int i = 0; i < list.size(); i++) {
				colPos = 1;
				InspectionQuestion inspectionQuestion = list.get(i);
				ps = conn.prepareStatement(INSERT_REPORT);
				ps.setString(colPos++, inspectionQuestion
						.getInspectionVehicleDetailId());
				ps.setInt(colPos++, inspectionQuestion.getQueryId());
				ps.setString(colPos++, inspectionQuestion.getResult());
				ps.setBytes(colPos++, inspectionQuestion.getPhoto());
				ps.setString(colPos++, inspectionQuestion.getExplanation());
				ps.setString(colPos++, inspectionQuestion
						.getMandatoryToComplete());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					id = rs.getInt(1);
				}
			}
			statement = conn.prepareStatement(QUERY_INSERT_OBSERVATION);
			colPos = 1;
			statement.setString(colPos++, observation);
			statement.setTimestamp(colPos++, new Timestamp(System
					.currentTimeMillis()));
			statement.setString(colPos++, list.get(0)
					.getInspectionVehicleDetailId());
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			id = -1;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (statement != null)
					statement.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}

	public static int insertInspectionVehicleImages(Connection conn,
			int vehicle_store_id, byte[] leftImage, byte[] rightImage,
			byte[] frontImage, byte[] backImage, String damageText) {
		PreparedStatement ps = null;
		int colPos = 1;
		int id = Misc.getUndefInt();
		ResultSet rs = null;
		String UPDATE_VEHICLE_DETAIL = "UPDATE inspection_vehicle_details SET left_image=?,right_image=?,front_image=?,back_image=?,damage_detail=? WHERE "
				+ "id=?";
		// String INSERT_VEHICLE_DETAIL =
		// "insert into inspection_vehicle_details "
		// + " (left_image, right_image, front_image,back_image,damage_detail) "
		// + " values " + " (?,?,?,?,?) WHERE id="+vehicle_store_id;
		try {

			ps = conn.prepareStatement(UPDATE_VEHICLE_DETAIL);
			ps.setBytes(colPos++, leftImage);
			ps.setBytes(colPos++, rightImage);
			ps.setBytes(colPos++, frontImage);
			ps.setBytes(colPos++, backImage);
			ps.setString(colPos++, damageText);
			ps.setInt(colPos, vehicle_store_id);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			id = -1;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}

	public static String getQuestions(Connection conn, int result, int id) {
		PreparedStatement ps = null;
		int colPos = 1;
		ResultSet rs = null;
		StringBuilder builder = new StringBuilder();
		String QUERY = "SELECT inspection_query_details.query_text from inspection_query_details left outer join inspection_report on inspection_report.query_id=inspection_query_details.id "
				+ "WHERE inspection_report.result=? AND inspection_report.inspection_vehicle_detail_id=? AND inspection_query_details.is_mandatory=1";
		try {

			ps = conn.prepareStatement(QUERY);
			ps.setInt(colPos++, result);
			ps.setInt(colPos++, id);
			rs = ps.executeQuery();

			while (rs.next()) {
				builder.append(rs.getString(1) + "::");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (builder.length() == 0) {
			return (builder.append("Passed")).toString();
		}
		return builder.substring(0, builder.length() - 2);
	}

	public static String getLastInspectionDate(Connection conn, String id) {
		ResultSet rset = null;
		PreparedStatement st = null;
		int colPos = 1;
		String lastDate = "";
		String QUERY = "SELECT MAX(date_of_inspection) FROM inspection_vehicle_details WHERE vehicle_id=?";
		try {
			st = conn.prepareStatement(QUERY);
			st.setInt(colPos++, Misc.getParamAsInt(id));
			rset = st.executeQuery();
			while (rset.next()) {
				lastDate = rset.getTimestamp(1).toString();
			}
			if (lastDate == null) {
				lastDate = "N/A";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (rset != null)
					rset.close();
				if (st != null)
					st.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return lastDate;
	}

	public static String getQuestionAnswerList(Connection conn, int id) {
		ResultSet rs = null;
		PreparedStatement st = null;
		// JSONArray array=new JSONArray();
		StringBuilder builder = new StringBuilder();
		String QUERY = "SELECT inspection_query_details.query_text FROM inspection_query_details "
				+ "left outer join inspection_report on inspection_report.query_id=inspection_query_details.id "
				+ "WHERE inspection_report.result=2 AND inspection_report.inspection_vehicle_detail_id=(select max(id) from inspection_vehicle_details where vehicle_id=(Select vehicle_id from inspection_vehicle_details where id=?) AND id<>?)";
		try {
			st = conn.prepareStatement(QUERY);
			st.setInt(1, id);
			st.setInt(2, id);
			rs = st.executeQuery();

			while (rs.next()) {
				builder.append(rs.getString(1) + "::");
				// JSONObject object=new JSONObject();
				// try {
				// object.put("query", rs.getString(1));
				// object.put("sub_query", Misc.getRsetString(rs, 2, ""));
				// object.put("result", rs.getInt(3));
				// array.put(object);
				// } catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (builder.length() > 0) {
			return builder.substring(0, builder.length() - 2);
		}
		return builder.toString();
	}
}

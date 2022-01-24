package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;

public class ReportDao {
	
	Logger logger = Logger.getLogger(ReportDao.class);
	
	public boolean insertEngineEventsTrack(Connection conn, EngineEventsTrackBean engEvtTrackBean) throws GenericException {
		int iHit = 0;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		logger.info("Inserting EngineEventsTrack");
		try {
			String insertEngineEventsTrack = engEvtTrackBean.getEventType() == 2 ? DBQueries.ENGINEEVENTSTRACK.INSERT_REGION_EVENTS_TRACK : DBQueries.ENGINEEVENTSTRACK.INSERT_ENGINE_EVENTS_TRACK;
			PreparedStatement stmt = conn.prepareStatement(insertEngineEventsTrack);
			Misc.setParamLong(stmt, engEvtTrackBean.getAlarmCreatedById(), 1);
			stmt.setTimestamp(2, sysDate);
			Misc.setParamInt(stmt, engEvtTrackBean.getStatus(), 3);
			Misc.setParamInt(stmt, engEvtTrackBean.getPriority(), 4);
			Misc.setParamInt(stmt, engEvtTrackBean.getAssignedTo(), 5);
			stmt.setString(6, engEvtTrackBean.getComment1());
			Misc.setParamInt(stmt, engEvtTrackBean.getEventType(), 7);
			Misc.setParamInt(stmt, engEvtTrackBean.getReason1(), 8);
			Misc.setParamInt(stmt, engEvtTrackBean.getUnsafeZoneAction(), 9);
			Misc.setParamInt(stmt, engEvtTrackBean.getEngineEventId(), 10);
			iHit = stmt.executeUpdate();
			stmt.close();
		}  catch (SQLException sqlEx) {
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
		if (iHit > 0)
			return true;
		return false;
	}
	
	public boolean updateEngineEventsTrackSupervisor(Connection conn, EngineEventsTrackBean engEvtTrackBean) throws GenericException {
		int iHit = 0;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		logger.info("Updating EngineEventsTrack for supervisor");
		try {
			String insertEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.UPDATE_SUPERVISOR;
			PreparedStatement stmt = conn.prepareStatement(insertEngineEventsTrack);
			Misc.setParamInt(stmt, engEvtTrackBean.getReason1(), 1);
			stmt.setTimestamp(2, sysDate);
			stmt.setString(3, engEvtTrackBean.getComment1());
			Misc.setParamLong(stmt, engEvtTrackBean.getReason1UpdatedById(), 4);
			Misc.setParamInt(stmt, engEvtTrackBean.getStatus(), 5);
			Misc.setParamInt(stmt, engEvtTrackBean.getUnsafeZoneAction(), 6);
			Misc.setParamInt(stmt, engEvtTrackBean.getEngineEventId(), 7);
			Misc.setParamInt(stmt, engEvtTrackBean.getEventType(), 8);
			iHit = stmt.executeUpdate();
			stmt.close();	
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		}  catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		if (iHit > 0)
			return true;
		return false;
	}
	
	public boolean updateEngineEventsTrackManager(Connection conn, EngineEventsTrackBean engEvtTrackBean) throws GenericException {
		int iHit = 0;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		logger.info("Updating EngineEventsTrack for Manager");
		try {
			String insertEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.UPDATE_MANAGER;
			PreparedStatement stmt = conn.prepareStatement(insertEngineEventsTrack);
			Misc.setParamInt(stmt, engEvtTrackBean.getReason1(), 1);
			stmt.setTimestamp(2, sysDate);
			stmt.setString(3, engEvtTrackBean.getComment1());
			Misc.setParamLong(stmt, engEvtTrackBean.getReason2UpdatedById(), 4);
			Misc.setParamInt(stmt, engEvtTrackBean.getStatus(), 5);
			Misc.setParamInt(stmt, engEvtTrackBean.getEngineEventId(), 6);
			Misc.setParamInt(stmt, engEvtTrackBean.getEventType(), 7);
			iHit = stmt.executeUpdate();
			stmt.close();	
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		}  catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		if (iHit > 0)
			return true;
		return false;
	}
	
	public boolean updateEngineEventsTrack(Connection conn, EngineEventsTrackBean engEvtTrackBean) throws GenericException {
		int iHit = 0;
		Timestamp sysDate = new Timestamp((new Date()).getTime());
		logger.info("Updating EngineEventsTrack");
		try {
			String insertEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.UPDATE_ENGINE_EVENTS_TRACK;
			PreparedStatement stmt = conn.prepareStatement(insertEngineEventsTrack);
			Misc.setParamInt(stmt, engEvtTrackBean.getReason1(), 1);
			stmt.setTimestamp(2, sysDate);
			stmt.setString(3, engEvtTrackBean.getComment1());
			Misc.setParamInt(stmt, engEvtTrackBean.getReason2(), 4);
			stmt.setTimestamp(5, sysDate);
			stmt.setString(6, engEvtTrackBean.getComment2());
			Misc.setParamInt(stmt, engEvtTrackBean.getReason3(), 7);
			stmt.setTimestamp(8, sysDate);
			stmt.setString(9, engEvtTrackBean.getComment3());
			Misc.setParamInt(stmt, engEvtTrackBean.getEngineEventId(), 10);
			iHit = stmt.executeUpdate();
			stmt.close();	
		}  catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		}  catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		if (iHit > 0)
			return true;
		return false;
	}

	public EngineEventsTrackBean getEngineEventsTrackById(Connection conn, int engineEventsId, int eventType) throws GenericException {
		logger.info("Getting EngineEventsTrack");
		if(eventType == 1)
			return getEngineEventsTrackByEngineId(conn, engineEventsId, eventType);
		else if(eventType == 2)
			return getEngineEventsTrackByRegionId(conn, engineEventsId, eventType);
		else 
			return null;
	}
	
	// Table mapping from region_track_events -->> engine_events_track 
	// id -> engine_events_id
	// vehicle_id -> vehicle_id
	// event_type -> attribute_id  // => region rules
	// event_type_value -> attribute_value
	// event_start_time -> event_start_time
	// event_stop_time -> event_stop_time
	// op_station_id -> rule_id

	EngineEventsTrackBean getEngineEventsTrackByRegionId(Connection conn, int engineEventsId, int eventType) throws GenericException {
		System.out.println("ReportDao.getEngineEventsTrackByRegionId()");
		String fetchEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.FETCH_REGION_EVENTS_TRACK;
		ResultSet rs = null;
		EngineEventsTrackBean engEventsTrackBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchEngineEventsTrack);
			contSt.setInt(1, engineEventsId);
			contSt.setInt(2, eventType);
			rs = contSt.executeQuery();
			SimpleDateFormat sdf1 = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			boolean isEngEvtTrack = false;
				while (rs.next()) {
					engEventsTrackBean = new EngineEventsTrackBean();
					engEventsTrackBean.setEngineEventId(rs.getInt("engine_events_id"));
					engEventsTrackBean.setVehicleId(rs.getInt("vehicle_id"));
					engEventsTrackBean.setRuleId(rs.getInt("rule_id"));
					engEventsTrackBean.setRuleName(rs.getString("ruleName"));
					engEventsTrackBean.setEventBeginLongitude(rs.getDouble("event_begin_longitude"));
					engEventsTrackBean.setEventBeginLatitude(rs.getDouble("event_begin_latitude"));
					engEventsTrackBean.setEventEndLongitude(rs.getDouble("event_end_longitude"));
					engEventsTrackBean.setEventEndLatitude(rs.getDouble("event_end_latitude"));
					engEventsTrackBean.setEventStartTime(rs.getDate("event_start_time"));
					engEventsTrackBean.setEventStopTime(rs.getDate("event_stop_time"));
					engEventsTrackBean.setAttributeId(rs.getInt("attribute_id"));
					engEventsTrackBean.setAttributeValue(rs.getDouble("attribute_value"));
					engEventsTrackBean.setUpdatedOn(rs.getDate("updated_on"));
					engEventsTrackBean.setEventCreateRecTime(rs.getDate("event_create_recvtime"));
					engEventsTrackBean.setEventStartName(rs.getString("event_begin_name"));
					engEventsTrackBean.setEventEndName(rs.getString("event_end_name"));
					engEventsTrackBean.setReason1(rs.getInt("reason_1"));
					engEventsTrackBean.setComment1(rs.getString("comment_1"));
					engEventsTrackBean.setEventStartTimeStr(Misc.printDate(sdf1, rs.getTimestamp("event_start_time")));
					engEventsTrackBean.setVehicleName(rs.getString("vehiclename"));
					engEventsTrackBean.setStatus(rs.getInt("status"));
					engEventsTrackBean.setPriority(rs.getInt("priority"));
					engEventsTrackBean.setAssignedTo(rs.getInt("user_id"));
					engEventsTrackBean.setEventType(rs.getInt("event_type"));
					engEventsTrackBean.setEnggEventTrack(true);
					isEngEvtTrack = true;
				}
				if(!isEngEvtTrack){
					fetchEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.FETCH_REGION_EVENTS;
					contSt = conn.prepareStatement(fetchEngineEventsTrack);
					contSt.setInt(1, engineEventsId);
					rs = contSt.executeQuery();
					while(rs.next()){
						engEventsTrackBean = new EngineEventsTrackBean();
						engEventsTrackBean.setEngineEventId(rs.getInt("id"));
						engEventsTrackBean.setVehicleName(rs.getString("vehicleName"));
						engEventsTrackBean.setVehicleId(rs.getInt("vehicle_id"));
						engEventsTrackBean.setRuleId(rs.getInt("op_station_id"));
						engEventsTrackBean.setRuleName(rs.getString("opStationName"));
						engEventsTrackBean.setEventStartTime(rs.getDate("event_start_time"));
						engEventsTrackBean.setEventStopTime(rs.getDate("event_stop_time"));
						engEventsTrackBean.setAttributeId(rs.getInt("event_type"));
						engEventsTrackBean.setAttributeValue(rs.getDouble("event_type_value"));
						engEventsTrackBean.setEventStartTimeStr(Misc.printDate(sdf1, rs.getTimestamp("event_start_time")));
						engEventsTrackBean.setEventType(eventType);
						
					}
				}
			rs.close();
			contSt.close();
		} catch (SQLException sqlEx) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return engEventsTrackBean;
	}
	
	EngineEventsTrackBean getEngineEventsTrackByEngineId(Connection conn, int engineEventsId, int eventType) throws GenericException {
		System.out.println("ReportDao.getEngineEventsTrackByEngineId()");
		String fetchEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.FETCH_ENGINE_EVENTS_TRACK;
		ResultSet rs = null;
		EngineEventsTrackBean engEventsTrackBean = null;
		try {
			PreparedStatement contSt = conn.prepareStatement(fetchEngineEventsTrack);
			contSt.setInt(1, engineEventsId);
			contSt.setInt(2, eventType);
			rs = contSt.executeQuery();
			SimpleDateFormat sdf1 = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			boolean isEngEvtTrack = false;
				while (rs.next()) {
					engEventsTrackBean = new EngineEventsTrackBean();
					engEventsTrackBean.setEngineEventId(rs.getInt("engine_events_id"));
					engEventsTrackBean.setVehicleId(rs.getInt("vehicle_id"));
					engEventsTrackBean.setRuleId(rs.getInt("rule_id"));
					engEventsTrackBean.setRuleName(rs.getString("ruleName"));
					engEventsTrackBean.setEventBeginLongitude(rs.getDouble("event_begin_longitude"));
					engEventsTrackBean.setEventBeginLatitude(rs.getDouble("event_begin_latitude"));
					engEventsTrackBean.setEventEndLongitude(rs.getDouble("event_end_longitude"));
					engEventsTrackBean.setEventEndLatitude(rs.getDouble("event_end_latitude"));
					
//					engEventsTrackBean.setEventStartTime(Misc.printDate(sdf, rs.getDate("event_start_time")));
					engEventsTrackBean.setEventStartTime(rs.getDate("event_start_time"));
					engEventsTrackBean.setEventStopTime(rs.getDate("event_stop_time"));
					engEventsTrackBean.setAttributeId(rs.getInt("attribute_id"));
					engEventsTrackBean.setAttributeValue(rs.getDouble("attribute_value"));
					engEventsTrackBean.setUpdatedOn(rs.getDate("updated_on"));
					engEventsTrackBean.setEventCreateRecTime(rs.getDate("event_create_recvtime"));
					engEventsTrackBean.setEventStartName(rs.getString("event_begin_name"));
					engEventsTrackBean.setEventEndName(rs.getString("event_end_name"));
					engEventsTrackBean.setReason1(rs.getInt("reason_1"));
					engEventsTrackBean.setComment1(rs.getString("comment_1"));
					engEventsTrackBean.setEventStartTimeStr(Misc.printDate(sdf1, rs.getTimestamp("event_start_time")));
					engEventsTrackBean.setVehicleName(rs.getString("vehiclename"));
					engEventsTrackBean.setStatus(rs.getInt("status"));
					engEventsTrackBean.setPriority(rs.getInt("priority"));
					engEventsTrackBean.setAssignedTo(rs.getInt("user_id"));
					engEventsTrackBean.setEventType(rs.getInt("event_type"));
					engEventsTrackBean.setEnggEventTrack(true);
					isEngEvtTrack = true;
				}
				if(!isEngEvtTrack){
					fetchEngineEventsTrack = DBQueries.ENGINEEVENTSTRACK.FETCH_ENGINE_EVENTS;
					contSt = conn.prepareStatement(fetchEngineEventsTrack);
					contSt.setInt(1, engineEventsId);
					rs = contSt.executeQuery();
					while(rs.next()){
						engEventsTrackBean = new EngineEventsTrackBean();
						engEventsTrackBean.setEngineEventId(rs.getInt("id"));
						engEventsTrackBean.setVehicleId(rs.getInt("vehicle_id"));
						engEventsTrackBean.setRuleId(rs.getInt("rule_id"));
						engEventsTrackBean.setRuleName(rs.getString("ruleName"));
						engEventsTrackBean.setEventBeginLongitude(rs.getDouble("event_begin_longitude"));
						engEventsTrackBean.setEventBeginLatitude(rs.getDouble("event_begin_latitude"));
						engEventsTrackBean.setEventEndLongitude(rs.getDouble("event_end_longitude"));
						engEventsTrackBean.setEventEndLatitude(rs.getDouble("event_end_latitude"));
						engEventsTrackBean.setEventStartTime(rs.getDate("event_start_time"));
						engEventsTrackBean.setEventStopTime(rs.getDate("event_stop_time"));
						engEventsTrackBean.setAttributeId(rs.getInt("attribute_id"));
						engEventsTrackBean.setAttributeValue(rs.getDouble("attribute_value"));
						engEventsTrackBean.setUpdatedOn(rs.getDate("updated_on"));
						engEventsTrackBean.setEventCreateRecTime(rs.getDate("event_create_recvtime"));
						engEventsTrackBean.setEventStartName(rs.getString("event_begin_name"));
						engEventsTrackBean.setEventEndName(rs.getString("event_end_name"));
						engEventsTrackBean.setEventStartTimeStr(Misc.printDate(sdf1, rs.getTimestamp("event_start_time")));
						engEventsTrackBean.setVehicleName(rs.getString("vehiclename"));
						engEventsTrackBean.setEventType(eventType);
					}
				}
			rs.close();
			contSt.close();
		} catch (SQLException sqlEx) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return engEventsTrackBean;
	}
	
	public int getOrg(Connection conn, int eventId) throws GenericException {
		logger.info("Fetching org for region events  ............");
		int orgId = Misc.getUndefInt();
		try {
			String getOrg = DBQueries.ENGINEEVENTSTRACK.GET_ORG;
			PreparedStatement stmt = conn.prepareStatement(getOrg);
			ResultSet rs = null;
			stmt.setInt(1, eventId);
			rs = stmt.executeQuery();
				while (rs.next()) {
					orgId = rs.getInt("port_node_id");
				}
		}catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		}  catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		
		return orgId;
	}
	

	public ArrayList<Pair<Integer, String>> getUserList(Connection conn, int portNodeId) throws GenericException {
		logger.info("Fetching Parent User List  ............");
		int orgId = Misc.getUndefInt();
		String name = null;
		ArrayList<Pair<Integer, String>> userList = new ArrayList<Pair<Integer, String>>();
		try {
			String getOrg = DBQueries.ENGINEEVENTSTRACK.GET_USERS;
			PreparedStatement stmt = conn.prepareStatement(getOrg);
			ResultSet rs = null;
			stmt.setInt(1, portNodeId);
			rs = stmt.executeQuery();
				while (rs.next()) {
					
					orgId = rs.getInt("id");
					name = rs.getString("name");
					userList.add(new Pair<Integer, String>(orgId, name));
				}
		}catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		}  catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		
		return userList;
	}
	public boolean insertVehicleNotes(SessionManager session,VehicleInteractionBean vehInBean) throws GenericException {
		int iHit = 0;
		Connection conn = session.getConnection();
		logger.info("Inserting Vehicle Interaction Notes");
		PreparedStatement stmt = null;
		String query = null;
		try {
			query = "delete from last_vehicle_interaction_notes where vehicle_id=?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, vehInBean.getVehicleId());
			stmt.execute();
			stmt.close();
			query = "insert into vehicle_interaction_notes(vehicle_id,user_id,cause,notes,updated_on,location, next_follow_time)" +
							" (select ?,?,?,?,?,name,? from current_data where vehicle_id=? and attribute_id=0) ";
			stmt = conn.prepareStatement(query);
			Misc.setParamInt(stmt, vehInBean.getVehicleId(), 1);
			Misc.setParamInt(stmt, vehInBean.getUserId(), 2);
			Misc.setParamInt(stmt, vehInBean.getCauseId(), 3);
			stmt.setString(4, vehInBean.getNotes());
			stmt.setTimestamp(5, Misc.utilToSqlDate(vehInBean.getUpdatedOn()));
			stmt.setTimestamp(6, Misc.utilToSqlDate(vehInBean.getNextFollowTime()));
			Misc.setParamInt(stmt, vehInBean.getVehicleId(), 7);

			iHit = stmt.executeUpdate();
			stmt.close();
			
			query = "insert into last_vehicle_interaction_notes(vehicle_id,user_id,cause,notes,updated_on,location, next_follow_time)" +
					" (select ?,?,?,?,?,name,? from current_data where vehicle_id=? and attribute_id=0) ";
			
			stmt = conn.prepareStatement(query);
			Misc.setParamInt(stmt, vehInBean.getVehicleId(), 1);
			Misc.setParamInt(stmt, vehInBean.getUserId(), 2);
			Misc.setParamInt(stmt, vehInBean.getCauseId(), 3);
			stmt.setString(4, vehInBean.getNotes());
			stmt.setTimestamp(5, Misc.utilToSqlDate(vehInBean.getUpdatedOn()));
			stmt.setTimestamp(6, Misc.utilToSqlDate(vehInBean.getNextFollowTime()));
			Misc.setParamInt(stmt, vehInBean.getVehicleId(), 7);
			iHit = stmt.executeUpdate();
			stmt.close();
		}  catch (SQLException sqlEx) {
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
		//if (iHit > 0)
			return true;
		//return false;
	}

	public VehicleInteractionBean getLastCommentOnvehicle(Connection conn,int vehicleId) throws GenericException {
		VehicleInteractionBean retval = null; 
		try{
			String query = " select vehicle.name vehicle_name,vehicle_interaction_notes.notes, vehicle_interaction_notes.updated_on,users.name operator from vehicle left " +
						   " outer join vehicle_interaction_notes on (vehicle.id = vehicle_interaction_notes.vehicle_id) left outer join users on" +
						   " (vehicle_interaction_notes.user_id=users.id) where vehicle.id = ? order by vehicle_interaction_notes.updated_on desc limit 1";
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = null;
			stmt.setInt(1, vehicleId);
			rs = stmt.executeQuery();
				while (rs.next()) {
					retval = new VehicleInteractionBean();
					retval.setVehicleId(vehicleId);
					retval.setVehicleName(Misc.getRsetString(rs, "vehicle_name"));
					retval.setNotes(Misc.getRsetString(rs, "notes"));
					retval.setUpdatedOn(rs.getTimestamp("updated_on"));
					retval.setOperator(Misc.getRsetString(rs, "operator"));
				}
		}catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
				throw new GenericException(ex);
			}
			throw new GenericException(sqlEx);
		}  catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			throw new GenericException(ex);
		}
		return retval;
	}
}

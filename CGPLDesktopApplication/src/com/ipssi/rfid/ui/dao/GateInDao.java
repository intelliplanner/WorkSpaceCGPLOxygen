/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
//import javax.swing.plaf.synth.SynthOptionPaneUI;

import com.ipssi.SingleSession;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.DriverDetailBean;
//import com.ipssi.rfid.beans.Biometric;
import com.ipssi.rfid.beans.RegistrationStatus;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.TprReportData;
import com.ipssi.rfid.beans.Transporter;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.constant.Status.TPR;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataProcessor;
import com.ipssi.rfid.ui.controller.MainController;
import com.ipssi.rfid.ui.print.PrintData;

import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.stage.Stage;

public class GateInDao {

	public static int getQcMarkStatus(Connection conn, int tpr_id) {
		int status = Misc.getUndefInt();
		// Connection conn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// int tpr_id = Misc.getUndefInt() ;

		String query = "SELECT mark_for_qc from tp_step where tpr_id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, tpr_id);
			rs = ps.executeQuery();
			while (rs.next()) {
				status = rs.getInt(1);
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		}
		return status;
	}

	// public static int createTPRStep(Connection conn, TPStep mBean) throws
	// IOException {
	// int parameterIndex = 1;
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// boolean destroyIt = false;
	// int tpr_step_id = Misc.getUndefInt();
	// java.sql.Date now = new java.sql.Date((new java.util.Date())
	// .getTime());
	// Timestamp entime = new Timestamp(mBean.getEntryTime().getTime());
	// String query =
	// "INSERT INTO tp_step (in_time, out_time, tpr_id, user_by,vehicle_id,"
	// + " work_station_id, work_station_type )"
	// + " VALUES (?, ? , ? , ? ,?, ? , ? )";
	// try {
	// ps = conn.prepareStatement(query);
	// ps.setTimestamp(parameterIndex++, entime);
	// ps.setDate(parameterIndex++, now);
	// ps.setInt(parameterIndex++, mBean.getTprId());
	// ps.setInt(parameterIndex++, mBean.getUpdatedBy());
	// ps.setInt(parameterIndex++, mBean.getVehicleId());
	// ps.setInt(parameterIndex++, mBean.getWorkStationId());
	// ps.setInt(parameterIndex++, mBean.getWorkStationType());
	// ps.executeUpdate();
	// rs = ps.getGeneratedKeys();
	// if (rs.next()) {
	// tpr_step_id = rs.getInt(1);
	// }
	// LoggerNew.Write("tpr_StepId :" + tpr_step_id);
	//
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// LoggerNew.Write(ex);
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// }
	// return tpr_step_id;
	// }
	/*
	 * public static int createTPRStep(Connection conn, TPStep mBean) throws
	 * IOException, SQLException { int tpr_step_id = Misc.getUndefInt();
	 * RFIDMasterDao.insert(conn, mBean); tpr_step_id = mBean.getId(); return
	 * tpr_step_id; }
	 */

	// public static int getSetupByStdName(String stdName, Connection conn) {
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// int vehicle_id = Misc.getUndefInt();
	// boolean destroyIt = false;
	// try {
	// ps = conn.prepareStatement("select id from vehicle where name like " +
	// "'" + stdName + "'" + " and status = 1");
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// vehicle_id = rs.getInt(1);
	// }
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// LoggerNew.Write(ex);
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	//
	// }
	// return vehicle_id;
	// }
	public static int getOverrideStatus(int tprId) {
		return 1;
	}

	// public static boolean getVehicleExtendedDetailExist(int vehId) {
	// Connection conn = null;
	// boolean destroyIt = false;
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// boolean isExist = false;
	// //int tpr_id = Misc.getUndefInt() ;
	// String query =
	// " select vehicle_id from vehicle_extended where vehicle_id = ? and
	// extended_status in (1,100)";
	// VehicleExtended vehicleExtentedBean = new VehicleExtended();
	// try {
	// conn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// ps = conn.prepareStatement(query);
	// ps.setInt(1, vehId);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// isExist = true;
	// }
	// if (!isExist) {
	//
	// vehicleExtentedBean.setVehicleId(vehId);
	// vehicleExtentedBean.setExtendedStatus(1);
	// RFIDMasterDao.insert(conn, vehicleExtentedBean);
	// isExist = true;
	// }
	// conn.commit();
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// LoggerNew.Write(ex);
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// DBConnectionPool.returnConnectionToPoolNonWeb(conn,
	// destroyIt);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	// return isExist;
	// }
	public static VehicleExtended getDetails(Connection conn, int vehId) {
		// Connection conn = null;

		System.out.println(" ######## Start Get Vehicle Extended Detail  ######");
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// int tpr_id = Misc.getUndefInt() ;
		VehicleExtended vehicleExtentedBean = null;
		ArrayList<Object> list = null;
		try {
			vehicleExtentedBean = new VehicleExtended();
			vehicleExtentedBean.setVehicleId(vehId);
			list = RFIDMasterDao.select(conn, vehicleExtentedBean);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					vehicleExtentedBean = (VehicleExtended) list.get(i);
				}
			} else {
				vehicleExtentedBean.setExtendedStatus(1);
				RFIDMasterDao.insert(conn, vehicleExtentedBean, false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(" ######## End Get Vehicle Extended Detail  ######");
		return vehicleExtentedBean;
	}

	public static boolean InsertNewVehicle(Connection conn, String vehiclename, int userBy) throws Exception {
		System.out.println(" ######## Insert New Vehicle ######");
		Vehicle vehicleBean = null;
		VehicleExtended vehicleExtend = null;
		boolean isSuccess = false;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		String stdName = "";
		java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
		ResultSet rs = null;
		// int tpr_id = Misc.getUndefInt() ;
		try {
			vehicleBean = new Vehicle();
			vehicleBean.setVehicleName(vehiclename);
			stdName = CacheTrack.standardizeName(vehiclename);
			vehicleBean.setStdName(stdName);
			vehicleBean.setUpdatedBy(userBy);
			vehicleBean.setUpdatedOn(now);
			vehicleBean.setRfidTempStatus(100);
			vehicleBean.setStatus(1);
			vehicleBean.setCreatedOn(now);
			vehicleBean.setCustomerId(TokenManager.portNodeId);
			boolean isInserted = RFIDMasterDao.insert(conn, vehicleBean, false);
			conn.commit();
			if (isInserted) {
				vehicleExtend = new VehicleExtended();
				vehicleExtend.setVehicleId(vehicleBean.getId());
				vehicleExtend.setExtendedStatus(100);
				RFIDMasterDao.insert(conn, vehicleExtend, false);
				conn.commit();
				isSuccess = true;
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
			throw ex;
		}
		return isSuccess;

	}

	
//	vehName = CacheTrack.standardizeName(vehName);
//	vehPair = TPRInformation.getDriverDetails(conn, null, vehName);
	
	public static void InsertIntoTable(Connection conn, VehicleExtended vehicleExtentedBean) throws Exception {// Any
																												// Table
		PreparedStatement ps = null;
		ResultSet rs = null;
		java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
		boolean destroyIt = false;
		try {
			RFIDMasterDao.insert(conn, vehicleExtentedBean, false);
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public static TPRecord selectFromTable(Connection conn, int tprId) throws Exception {// Any Table
		boolean isValues = false;
		boolean destroyIt = false;
		TPRecord tprecord = new TPRecord();
		tprecord.setTprId(tprId);
		ArrayList<Object> list = null;

		try {
			list = RFIDMasterDao.select(conn, tprecord);
			for (int i = 0; i < list.size(); i++) {
				tprecord = (TPRecord) list.get(i);
			}
			isValues = true;
			// invConn.commit();
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
			throw ex;
		}
		return tprecord;

	}

	public static int getTprId(Connection conn, int vehicleId) throws Exception {
		String query = " SELECT tpr_id FROM tp_record WHERE vehicle_id  = ? ";
		PreparedStatement ps = null;
		ResultSet rs = null;

		boolean destroyIt = false;
		int id = Misc.getUndefInt();
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);

			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
			throw ex;
		}
		return id;
	}

	public static Vehicle selectDataFromVehicle(Connection conn, int vehId) throws Exception {

		System.out.println(" ######## Start Get Date From selectDataFromVehicle(Connection conn, int vehId) ######");

		Vehicle vehicleBean = null;
		ArrayList<Object> list = null;
		try {
			vehicleBean = new Vehicle();
			vehicleBean.setId(vehId);
			vehicleBean.setStatus(1);
			list = RFIDMasterDao.select(conn, vehicleBean);
			for (int i = 0; i < list.size(); i++) {
				vehicleBean = (Vehicle) list.get(i);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.out.println(" ######## End Get Date From selectDataFromVehicle(Connection conn, int vehId) ######");
		return vehicleBean;
	}

	public static Vehicle selectDataFromVehicle(Connection conn, String epcCode) throws Exception {

		System.out.println(" ######## Start Get Date From selectDataFromVehicle(Connection conn, int vehId) ######");

		Vehicle vehicleBean = null;
		ArrayList<Object> list = null;
		try {
			vehicleBean = new Vehicle();
			vehicleBean.setEpcId(epcCode);
			vehicleBean.setStatus(1);
			list = RFIDMasterDao.select(conn, vehicleBean);
			for (int i = 0; i < list.size(); i++) {
				vehicleBean = (Vehicle) list.get(i);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.out.println(" ######## End Get Date From selectDataFromVehicle(Connection conn, int vehId) ######");
		return vehicleBean;
	}
	
	public static boolean InsertIntoRegistrationStatus(Connection conn, RegistrationStatus regisBean) throws Exception {
		PreparedStatement ps = null;
		boolean isExist = false;
		boolean isInsert = false;
		ResultSet rs = null;
		java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
		String insertQuery = "INSERT INTO registration_status_rfid(tpr_id,tag_info,vehicle_info,driver_info,challan_record_info,multiple_tpr_info,created_on,driver_id,vehicle_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String updateQuery = "UPDATE registration_status_rfid SET tpr_id = ?,tag_info = ?,vehicle_info = ?,driver_info = ?,challan_record_info = ?,multiple_tpr_info = ?,updated_on = ?,driver_id = ? WHERE vehicle_id = ?";

		try {
			int colPos = 1;
			isExist = IsVehicleExistInRegisStatus(conn, regisBean.getVehicle_id());
			if (isExist) {
				ps = conn.prepareStatement(updateQuery);
			} else {
				ps = conn.prepareStatement(insertQuery);
			}
			ps.setInt(colPos++, regisBean.getTpr_id());
			ps.setInt(colPos++, regisBean.getTag_info());
			ps.setInt(colPos++, regisBean.getVehicle_info());
			ps.setInt(colPos++, regisBean.getDriver_info());
			ps.setInt(colPos++, regisBean.getChallan_record_info());
			ps.setInt(colPos++, regisBean.getMultiple_tpr_info());
			ps.setDate(colPos++, now);
			ps.setInt(colPos++, regisBean.getDriver_id());
			ps.setInt(colPos++, regisBean.getVehicle_id());
			ps.execute();
			ps.close();
			isInsert = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return isInsert;
	}

	public static boolean IsVehicleExistInRegisStatus(Connection invConn, int vehicle_id) throws Exception {
		String query = " SELECT vehicle_id FROM registration_status_rfid WHERE vehicle_id  = ? ";
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isExist = false;
		boolean destroyIt = false;
		int id = Misc.getUndefInt();
		try {
			ps = invConn.prepareStatement(query);
			ps.setInt(1, vehicle_id);
			rs = ps.executeQuery();
			while (rs.next()) {
				isExist = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
			throw ex;
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
		return isExist;
	}

	public static boolean getVehicleRegisStatusValues(Connection invConn, int vehicle_id) throws Exception {
		String query = " SELECT vehicle_id FROM registration_status_rfid WHERE vehicle_id  = ? ";
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isExist = false;
		boolean destroyIt = false;
		int id = Misc.getUndefInt();
		try {
			ps = invConn.prepareStatement(query);
			ps.setInt(1, vehicle_id);
			rs = ps.executeQuery();
			while (rs.next()) {
				isExist = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
			throw ex;
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
		return isExist;
	}

	// public static RegistrationStatus
	// FetchDataFromRegistrationStatus(Connection invConn, int vehId_glob) {
	//
	// PreparedStatement ps = null;
	// boolean isExist = false;
	// boolean isInsert = false;
	// ResultSet rs = null;
	// java.sql.Date now = new java.sql.Date((new java.util.Date())
	// .getTime());
	// boolean destroyIt = false;
	// String Query =
	// "Select tpr_id, tag_info, vehicle_info, driver_info, challan_record_info,
	// multiple_tpr_info, driver_id, vehicle_id From registration_status_rfid where
	// vehicle_id = ?";
	// // String updateQuery =
	// "UPDATE registration_status_rfid SET tpr_id = ?,tag_info = ?,vehicle_info =
	// ?,driver_info = ?,challan_record_info = ?,multiple_tpr_info = ?,updated_on =
	// ?,vehicle_id = ? WHERE vehicle_id = ?";
	// RegistrationStatus regisBean = null;
	// try {
	// int colPos = 1;
	// regisBean = new RegistrationStatus();
	// ps = invConn.prepareStatement(Query);
	// ps.setInt(colPos, vehId_glob);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// regisBean.setTpr_id(rs.getInt(1));
	// regisBean.setTag_info(rs.getInt(2));
	// regisBean.setVehicle_info(rs.getInt(3));
	// regisBean.setDriver_info(rs.getInt(4));
	// regisBean.setChallan_record_info(rs.getInt(5));
	// regisBean.setMultiple_tpr_info(rs.getInt(6));
	// regisBean.setDriver_id(rs.getInt(7));
	// regisBean.setVehicle_id(rs.getInt(8));
	// isInsert = true;
	// }
	//
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// LoggerNew.Write(ex);
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// }
	// return regisBean;
	//
	// }

	public static RegistrationStatus SelectFromRegistrationStatus(Connection invConn, int vehId_glob) throws Exception {
		System.out
				.println(" ######## Start Get SelectFromRegistrationStatus(Connection invConn, int vehId_glob) ######");
		PreparedStatement ps = null;
		boolean isExist = false;
		boolean isInsert = false;
		ResultSet rs = null;
		java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
		boolean destroyIt = false;
		String insertQuery = "INSERT INTO registration_status_rfid(tpr_id,tag_info,vehicle_info,driver_info,challan_record_info,multiple_tpr_info,created_on,driver_id,vehicle_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		RegistrationStatus regisBean = null;
		ArrayList<Object> list = null;
		isExist = IsVehicleExistInRegisStatus(invConn, vehId_glob);

		regisBean = new RegistrationStatus();
		regisBean.setVehicle_id(vehId_glob);
		if (!isExist) {
			try {
				ps = invConn.prepareStatement(insertQuery);
				int colPos = 1;
				ps.setInt(colPos++, regisBean.getTpr_id());
				ps.setInt(colPos++, regisBean.getTag_info());
				ps.setInt(colPos++, regisBean.getVehicle_info());
				ps.setInt(colPos++, regisBean.getDriver_info());
				ps.setInt(colPos++, regisBean.getChallan_record_info());
				ps.setInt(colPos++, regisBean.getMultiple_tpr_info());
				ps.setDate(colPos++, now);
				ps.setInt(colPos++, regisBean.getDriver_id());
				ps.setInt(colPos++, vehId_glob);
				ps.execute();
				ps.close();
				invConn.commit();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			}
			System.out.println(
					" ######## End Get SelectFromRegistrationStatus(Connection invConn, int vehId_glob) ######");
		}
		if (isExist) {
			try {
				regisBean.setVehicle_id(vehId_glob);
				list = RFIDMasterDao.select(invConn, regisBean);
				for (int i = 0; i < list.size(); i++) {
					regisBean = (RegistrationStatus) list.get(i);
				}
			} catch (Exception ex) {
				destroyIt = true;
				ex.printStackTrace();
			}
		}

		return regisBean;

	}

	public static Triple<Token, TPRecord, TPRBlockManager> getTPRecord(Connection conn, String vehicleName,
			int vehicleId, int workStationType, int workStationTypeId, int userId) {
		System.out
				.println(" ######## Start Get getTPRecord(Connection conn, String vehicleName, int vehicleId) ######");
		RFIDDataProcessor rfidProcessor = null;
		TPRecord tp_record = null;
		if (rfidProcessor == null) {
			rfidProcessor = new RFIDDataProcessor(0, workStationType, workStationTypeId, userId);
		}
		Triple<Token, TPRecord, TPRBlockManager> tpRecord = rfidProcessor.getTprecord(vehicleName, vehicleId, false,
				false);
		if (tpRecord != null) {
			tp_record = tpRecord.second;
		}
		System.out.println(" ######## End Get getTPRecord(Connection conn, String vehicleName, int vehicleId) ######");
		return tpRecord;

	}

	// public static boolean isDriverDetailComplete(int vehId_glob) {
	// Connection invConn = null;
	// boolean destroyIt = false;
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// //int tpr_id = Misc.getUndefInt() ;
	// boolean isFingerCaptured = false;
	//
	// String query =
	// " SELECT status FROM driver_details WHERE vehicle_id = ? and challan_date = ?
	// ";
	// try {
	// invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// ps = invConn.prepareStatement(query);
	// ps.setInt(1, vehId_glob);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// if (rs.getInt(1) == 1) {
	// isFingerCaptured = true;
	// }
	// }
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// LoggerNew.Write(ex);
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// DBConnectionPool.returnConnectionToPoolNonWeb(invConn,
	// destroyIt);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	// return isFingerCaptured;
	// }
	// public synchronized TPRecord syncTPRecord(String vehicleName, String
	// challanDate) throws IOException {
	// Connection invConn = null;
	// boolean destroyIt = false;
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// //int tpr_id = Misc.getUndefInt() ;
	// TPRecord mBean = null;
	// String query =
	// " SELECT tpr_id,transporter_id,challan_date FROM tp_record WHERE vehicle_Name
	// = ? and challan_date = ? ";
	// try {
	// invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// ps = invConn.prepareStatement(query);
	// ps.setString(1, vehicleName);
	// ps.setString(2, challanDate);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// mBean = new TPRecord();
	// mBean.setTprId(rs.getInt(1));
	// mBean.setTransporterId(rs.getInt(2));
	// mBean.setChallanDate(rs.getTimestamp(3));
	// }
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// DBConnectionPool.returnConnectionToPoolNonWeb(invConn,
	// destroyIt);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return mBean;
	// }
	// public int syncTPRBlockRecord(int tpr_id) throws IOException {
	// int status = Misc.getUndefInt();
	// Connection invConn = null;
	// boolean destroyIt = false;
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// //int tpr_id = Misc.getUndefInt() ;
	//
	// String query =
	// "SELECT status_result from tpr_block_detail where tpr_id = ? order by
	// created_on ";
	// try {
	// invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
	// ps = invConn.prepareStatement(query);
	// ps.setInt(1, tpr_id);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// status = rs.getInt(1);
	// }
	// } catch (Exception ex) {
	// destroyIt = true;
	// ex.printStackTrace();
	// } finally {
	// try {
	// if (ps != null) {
	// ps.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// if (rs != null) {
	// rs.close();
	// }
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// try {
	// DBConnectionPool.returnConnectionToPoolNonWeb(invConn,
	// destroyIt);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return status;
	// }
	public static String getValue(boolean boolVal) {
		String val = "No";
		if (boolVal) {
			val = "Yes";
		}
		return val;
	}

	public static boolean updateTPRQuestion(Connection conn, int tprId, int workstationTypeId, int questionId,
			int answerId, int userBy) throws Exception {
		TPSQuestionDetail tpsQuestionBean = null;
		boolean isInsert = false;
		try {
			tpsQuestionBean = new TPSQuestionDetail();
			tpsQuestionBean.setTprId(tprId);
			tpsQuestionBean.setTpsId(workstationTypeId);
			tpsQuestionBean.setQuestionId(questionId);
			tpsQuestionBean.setAnswerId(answerId);
			tpsQuestionBean.setUpdatedBy(userBy);
			System.out.println("End quesId :" + questionId + ", ansId :" + answerId);
			RFIDMasterDao.executeQuery(conn,
					"delete from tps_question_detail where tpr_id=" + tprId + " and question_id=" + questionId);
			isInsert = RFIDMasterDao.insert(conn, tpsQuestionBean, false);
			RFIDMasterDao.executeQuery(conn,
					"delete from tps_question_detail_apprvd where tpr_id=" + tprId + " and question_id=" + questionId);
			isInsert = RFIDMasterDao.insert(conn, tpsQuestionBean, true);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return isInsert;
	}

	public static boolean isTagExist(Connection conn, String epcId) throws Exception {
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select 1 from vehicle where rfid_epc=? and status=1");
			ps.setString(1, epcId);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = rs.getInt(1) == 1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return retval;
	}

	public static void insertReadings(Connection conn, int tprId, ArrayList<Pair<Long, Integer>> readings) {
		if (readings == null || readings.size() <= 0 || Misc.isUndef(tprId) || conn == null)
			return;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("insert into tpr_wb_readings (tpr_id,capture_time,reading) values (?,?,?) ");
			for (Pair<Long, Integer> reading : readings) {
				Misc.setParamInt(ps, tprId, 1);
				ps.setTimestamp(2, new Timestamp(reading.first));
				Misc.setParamInt(ps, reading.second, 3);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// public static void initManualEntry(Connection conn, boolean forceManual)
	// throws Exception {
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// String query = "SELECT id, is_manual_entry FROM work_station_details";
	// try {
	// ps = conn.prepareStatement(query);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// TokenManager.isManualEntry.put(rs.getInt(1),
	// TokenManager.forceManual ? 1 : rs.getInt(2));
	// }
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// throw ex;
	// }
	//
	// }

	public static boolean isLrExist(Connection conn, int tpr_id, String lrNo, int materialCode) {
		boolean isExist = false;
		// Connection conn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// int tpr_id = Misc.getUndefInt() ;

		String query = "SELECT tpr_id from tp_record where  consignee_ref_doc like ? and material_cat = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, lrNo);
			ps.setInt(2, materialCode);
			System.out.print("GateInDao  FlyAshIsLrNoExist() Query :" + ps.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				if (tpr_id != rs.getInt(1)) {
					isExist = true;
				}
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isExist;
	}

	public static boolean isStoneLrNoExist(Connection conn, int tpr_id, String lrNo, int materialCode) {
		boolean isExist = false;
		// Connection conn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// int tpr_id = Misc.getUndefInt() ;

		String query = "SELECT tpr_id from tp_record where  lr_no like ? and material_cat = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, lrNo);
			ps.setInt(2, materialCode);
			System.out.print("GateInDao StoneLrNoExist Query :" + ps.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				if (tpr_id != rs.getInt(1)) {
					isExist = true;
				}

			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}
		return isExist;
	}

	public static double calculateNetWt(double gross, double tare) {
		double Wb_Net_Wt = Misc.getUndefDouble();
		if (!Misc.isUndef(gross) && !Misc.isUndef(tare)) {
			Wb_Net_Wt = gross - tare;
		}
		return Wb_Net_Wt;
	}

	public static double calculateTotalShort(double Party_Net_Wt, double Wb_Net_Wt) {
		double totalShort = Misc.getUndefDouble();
		if (Party_Net_Wt != Misc.getUndefDouble() && Wb_Net_Wt != Misc.getUndefDouble()) {
			totalShort = Party_Net_Wt - (Party_Net_Wt < Wb_Net_Wt ? Party_Net_Wt : Wb_Net_Wt);
		}
		return totalShort;
	}

	public static double calculateAcceptedNetWt(double Party_Net_Wt, double Wb_Net_Wt) {
		double val = Misc.getUndefDouble();
		if (Party_Net_Wt != Misc.getUndefDouble() && Wb_Net_Wt != Misc.getUndefDouble()) {
			if (Party_Net_Wt < Wb_Net_Wt) {
				// String acceptedNetWt = new
				// DecimalFormat("#.##").format(Party_Net_Wt);
				// AcceptedNetWt.setText(Misc.getPrintableDouble(Party_Net_Wt));
				val = Party_Net_Wt;
			} else {
				// String acceptedNetWt = new
				// DecimalFormat("#.##").format(Wb_Net_Wt);
				// AcceptedNetWt.setText(Misc.getPrintableDouble(Wb_Net_Wt));
				val = Wb_Net_Wt;
			}
		}
		return val;
	}

	public static String getString(String str1, int defaultLength) {
		if (Utils.isNull(str1)) {
			str1 = "";
		}
		int strLen = str1.length();
		if (strLen > defaultLength) {
			str1 = str1.substring(0, defaultLength);
		}
		System.out.println(str1 + " new Length: " + str1.length());
		int diffLength = defaultLength - strLen;
		String str2 = " ";
		String str3 = "";
		for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
			str3 += str2;
		}

		str1 += str3;

		return str1;
	}

	public static String getLabelString(String str1, int defaultLength) {
		if (Utils.isNull(str1)) {
			str1 = "";
		}
		int strLen = str1.length();
		System.out.println(str1 + " Length: " + strLen);
		int diffLength = defaultLength - strLen;
		String str2 = " ";
		String str3 = "";
		for (int i = 0, is = str1 == null ? 0 : diffLength; i < is; i++) {
			str3 += str2;
		}
		str3 = str3 + str1;

		return str3;
	}

	public static String getLongToDatetime(long time) {

		if (time == 0 || Misc.isUndef(time)) {
			return "";
		} else {
			Date date = new Date(time);
			Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			return format.format(date);
		}

	}

	public static java.util.Date getExpectedDate(java.util.Date comboStart) {
		// Date dayAfter = new Date(someDate.getTime()+(24*60*60*1000));
		if (comboStart != null) {
			comboStart = new Date(comboStart.getTime() + 24 * 60 * 60 * 1000);
		}
		return comboStart;
	}

	public static void insertTemplate(Connection conn, int tprId, byte[] template) throws Exception {

		System.out.println("#### GateInDao: insertTemplate() #####");
		PreparedStatement ps = null;
		int colPos = 1;
		String query = "insert into tpr_driver_identify_debug " + " (tpr_id,template) values (?,?)";
		try {
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, tprId, colPos++);
			ps.setObject(colPos++, template);
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			Misc.closePS(ps);
		}
	}

	public static ArrayList<Pair<Integer, ArrayList<byte[]>>> getDriverDebugData(Connection conn) {
		boolean isExist = false;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer, ArrayList<byte[]>>> templateList = new ArrayList<Pair<Integer, ArrayList<byte[]>>>();
		String query = "SELECT template,tpr_id from tpr_driver_identify_debug";
		ArrayList<byte[]> userTemplate = null;
		try {
			ps = conn.prepareStatement(query);
			System.out.print("GateInDao getDriverDebugData Query :" + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				userTemplate = new ArrayList<byte[]>();
				userTemplate.add((byte[]) rs.getObject("template"));
				templateList.add(new Pair<Integer, ArrayList<byte[]>>(Misc.getRsetInt(rs, "tpr_id"), userTemplate));
				// userTemplate.add((byte[]) rs.getObject("template"));
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}

		return templateList;
	}

	private static void updateIdentifyDebugDriver(Connection conn, int tprId, int driverId) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update tpr_driver_identify_debug set driver_id=? where tpr_id=?");
			Misc.setParamInt(ps, driverId, 1);
			Misc.setParamInt(ps, tprId, 2);
			ps.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
		}

	}

	public static int getRandomNumber(int val) {
		Random t = new Random();
		return t.nextInt(val) + 1;
	}

	public static void initQCRandom(Connection conn) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		String query = "SELECT mines_details.id, transporter_details.id, grade_details.id, supplier_id FROM mines_details JOIN transporter_details JOIN grade_details join supplier_details";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				TokenManager.randomQC.put(rs.getInt(1) + "_" + rs.getInt(2) + "_" + rs.getInt(3) + "_" + rs.getInt(4),
						new Pair<Integer, Integer>(0, getRandomNumber(4)));

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	public static boolean isSelectedForQC(int mines, int transporter, int grade, int supplierId) {
		boolean isTrue = false;
		String keyStr = mines + "_" + transporter + "_" + grade + "_" + supplierId;
		if (TokenManager.randomQC.containsKey(keyStr)) {
			Pair<Integer, Integer> valPair = TokenManager.randomQC.get(keyStr);
			if (valPair != null && valPair.first != null && valPair.second != null
					&& (valPair.first.intValue() + 1) == valPair.second.intValue()) {

				System.out.println(valPair.first.intValue() + "   " + valPair.second.intValue());
				isTrue = true;
			}
			if (valPair.first.intValue() == TokenManager.randomCheckLotSize - 1) {
				valPair = new Pair<Integer, Integer>(0, GateInDao.getRandomNumber(TokenManager.randomCheckLotSize));
				TokenManager.randomQC.put(keyStr, valPair);
			} else {
				valPair = new Pair<Integer, Integer>(valPair.first.intValue() + 1, valPair.second);
				TokenManager.randomQC.put(keyStr, valPair);
			}
		}
		return isTrue;
	}

	public static void main(String s[]) {
		checkNumeric("1234");
		
		Connection conn = null;
		// int count = 0;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			// while (true) {
			// GateInDao.initQCRandom(conn);
			// boolean isTrue = isSelectedForQC(getRandomNumber(10),
			// getRandomNumber(10), getRandomNumber(20));
			updatePostLotData(conn, 1, "test");
			// System.out.println(isTrue);
			// }
		} catch (Exception ex) {
			// Logger.getLogger(DriverBean.class.getName()).log(Level.SEVERE,
			// null, ex);
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Connection conn = null;
		// int count = 0;
		// try {
		// conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		// ArrayList<Pair<Integer, ArrayList<byte[]>>> templateList =
		// getDriverDebugData(conn);
		// for(Pair<Integer,ArrayList<byte[]>> userTemplate : templateList){
		// System.out.println("Count: "+count++);
		// if(userTemplate == null || userTemplate.second == null ||
		// Misc.isUndef(userTemplate.first))
		// continue;
		// Pair<String, String> result = identifyUser(userTemplate.second);
		// int driverId = result == null ? Misc.getUndefInt() :
		// Misc.getParamAsInt(result.first);
		// if(!Misc.isUndef(driverId))
		// updateIdentifyDebugDriver(conn,userTemplate.first,driverId);
		// conn.commit();
		// //count++;2
		//
		// }
		//
		// } catch (Exception ex) {
		// Logger.getLogger(DriverBean.class.getName()).log(Level.SEVERE, null,
		// ex);
		// }
		// finally{
		// try {
		// DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		// } catch (GenericException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
	}

	public static void forceSignut(int userId, int srcType, String currWorkStationId) {
		// TODO Auto-generated method stub
		Connection conn = null;
		boolean destroyIt = false;
		// int count = 0;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			SingleSession.forceLogout(conn, userId, srcType, Integer.toString(TokenManager.systemId));
		} catch (Exception ex) {
			// Logger.getLogger(DriverBean.class.getName()).log(Level.SEVERE,
			// null, ex);
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String removeSpecialChar(String str) {
		if (str == null || str.length() == 0)
			return str;

		str = str.replaceAll("/", "").replaceAll("'", "");

		return str;
	}
	// public static void saveEpcDetail(Connection conn, String s,
	// int workStationType, int workStationId, int userId) {
	// // TODO Auto-generated method stub
	// PreparedStatement ps = null;
	// String query = "INSERT INTO epc_details () values (?,?,?,?)";
	// try{
	//
	// ps = conn.prepareStatement(query);
	// }catch(Exception e){
	//
	// }
	// }

	public static void stopClearScreenTimer(Timer timer) {
		try {
			if (timer != null) {
				timer.cancel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean getIsSelectedForQC(Connection conn, int tprId) {
		boolean status = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(tprId))
			return false;

		String query = "SELECT id from mpl_coal_sample_details where tpr_id = ? and rfid_epc is null ";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			while (rs.next()) {
				status = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return status;
	}

	public static void updateOldRecordForTag(Connection conn, String rfidEPC) throws Exception {
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		try {
			ps = conn.prepareStatement("update mpl_coal_sample_details set is_latest=0 where rfid_epc = ?");
			ps.setString(1, rfidEPC);
			ps.executeUpdate();
			ps1 = conn.prepareStatement("update mpl_coal_sample_details_apprvd set is_latest=0 where rfid_epc = ?");
			ps1.setString(1, rfidEPC);
			ps1.executeUpdate();
		} catch (Exception ex) {
			throw ex;
		} finally {
			Misc.closePS(ps);
			Misc.closePS(ps1);
		}

	}

	public static int getSupplier(Connection conn, int doId) {
		int supplierId = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select seller from do_rr_details where id= ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, doId);
			rs = ps.executeQuery();
			while (rs.next()) {
				supplierId = rs.getInt("seller");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return supplierId;
	}

	public static Triple<Integer, String, Integer> isLotExist(Connection conn, String epcId) {
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// Pair<Integer, String> pairVal = null;
		Triple<Integer, String, Integer> pairVal = null;
		String query = "Select mpl_post_lot_details.id, mpl_post_lot_details.name,mpl_lots_lab_details.lab_details_id  from mpl_post_lot_details join mpl_lots_lab_details on (mpl_post_lot_details.id = mpl_lots_lab_details.post_sample_lot_id) where mpl_lots_lab_details.status = 1 and mpl_lots_lab_details.rfid_epc=?";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, epcId);
			System.out.println(ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				pairVal = new Triple<Integer, String, Integer>(rs.getInt(1), rs.getString(2),
						rs.getInt("mpl_lots_lab_details.lab_details_id"));
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		}
		return pairVal;
	}

	public static void updateLotSampleAfterUpload(Connection conn, int id) {
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Integer, String> pairVal = null;

		String query = "update mpl_post_lot_details set status = 2 where  id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, id);
			System.out.println(ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void freeLotNumberAfterUpload(Connection conn, int latestAssignedId) {
		boolean destroyIt = false;
		PreparedStatement ps = null;
		String query = "update mpl_post_lot_number set is_free=0 where latest_assigned_id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, latestAssignedId);
			System.out.println(ps.toString());
			ps.executeUpdate();
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void updatePostLotData(Connection conn, int lotId, String rficEpc) {
		PreparedStatement ps = null;
		String query = "update mpl_lots_lab_details set sample_upload_tag_read=1 where post_sample_lot_id = ? and rfid_epc=?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, lotId);
			ps.setString(2, rficEpc);
			System.out.println(ps.toString());
			ps.executeUpdate();
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static Triple<Integer, String, Integer> searchPostLotData(Connection conn, String lotId, String lotName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select mpl_post_lot_details.id, mpl_post_lot_details.name,mpl_lots_lab_details.lab_details_id from mpl_post_lot_details join  mpl_lots_lab_details on (mpl_post_lot_details.id=mpl_lots_lab_details.post_sample_lot_id) where mpl_lots_lab_details.rfid_epc is not null and mpl_post_lot_details.status=1 and mpl_lots_lab_details.sample_upload_tag_read=1 ";
		// ;// new
		// StringBuilder("Select id from mpl_post_lot_details where epc_code is not null
		// and mpl_post_lot_details.sample_upload_tag_read=1 and
		// mpl_post_lot_details.port_node_id=? ")
		// ;
		// Pair<Integer, String> pairval = null;
		Triple<Integer, String, Integer> pairval = null;
		try {
			int colPos = 1;
			// if (!Misc.isUndef(lotId)) {
			if (lotId != null && lotId.length() > 0) {
				query += " and mpl_post_lot_details.id=? ";
			}
			if (lotName != null && lotName.length() > 0) {
				query += " and mpl_post_lot_details.name=? ";
			}

			ps = conn.prepareStatement(query);
			if (lotId != null && lotId.length() > 0) {
				ps.setString(colPos++, lotId);
			}
			if (lotName != null && lotName.length() > 0) {
				lotName = lotName.contains("lot_") ? lotName : "lot_" + lotName;
				ps.setString(colPos++, lotName);
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				if (pairval == null)
					pairval = new Triple<Integer, String, Integer>(rs.getInt("id"), rs.getString("name"),
							rs.getInt("mpl_lots_lab_details.lab_details_id"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return pairval;
	}

	public static String getTagExistance(Connection conn, String rfidEPC) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select tp_record.vehicle_name from mpl_coal_sample_details join tp_record on (tp_record.tpr_id=mpl_coal_sample_details.tpr_id) where mpl_coal_sample_details.status=1 and mpl_coal_sample_details.is_latest=1 and mpl_coal_sample_details.sample_read=0 and mpl_coal_sample_details.rfid_epc=?";
		String val = null;
		try {
			int colPos = 1;
			ps = conn.prepareStatement(query);
			ps.setString(colPos++, rfidEPC);
			rs = ps.executeQuery();
			while (rs.next()) {
				val = rs.getString(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return val;
	}
//	vehName = CacheTrack.standardizeName(vehName);
//	vehPair = TPRInformation.getVehicle(conn, null, vehName);
	public static Pair<String, String> getDriverDetails(Connection conn, String dlNumber) {

		boolean isExist = false;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<String, String> pairVal = null;
		String query = "SELECT driver_name,driver_dl_number from driver_details where driver_dl_number like '%" +dlNumber+ "%'";
		try {
			ps = conn.prepareStatement(query);
			System.out.print("GateInDao getDriverDebugData Query :" + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				pairVal = new Pair<String, String>(rs.getString("driver_name"),rs.getString("driver_dl_number"));
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {

			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}

		return pairVal;
	
	}
	public static boolean insertDriverDetails(Connection conn,String driverName,String driverDL) throws Exception {

		DriverDetailBean driverBean = null;
		boolean isInsert = false;
		try {
			driverBean = new DriverDetailBean();
			driverBean.setDriver_dl_number(driverDL);;
			driverBean.setDriver_name(driverName);
			driverBean.setDriver_std_name(CacheTrack.standardizeName(driverName));
			driverBean.setStatus(1);
			isInsert = RFIDMasterDao.insert(conn, driverBean, false);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return isInsert;
	}
	
	public static boolean insertTransporter(Connection conn,String name,String stdName,String sapCode) throws Exception {
		java.util.Date curr = new java.util.Date();
		Transporter bean = null;
		boolean isInsert = false;
		try {
			bean = new Transporter();
			bean.setName(name);
			bean.setCode(Misc.getParamAsString(stdName));
			bean.setSapCode(Misc.getParamAsString(sapCode));
			bean.setCreatedOn(curr);
			bean.setUpdatedBy(TokenManager.userId);
			bean.setPortNodeId(TokenManager.portNodeId);
			bean.setMaterialCat(TokenManager.materialCat);
			bean.setStatus(1);
			isInsert = RFIDMasterDao.insert(conn, bean, false);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return isInsert;
	}

	public static Pair<Boolean, Double> isSalesOrderQuantityExist(Connection conn, String sapSalesOrder,String sapLineItem ,double netWt) throws Exception {
		Pair<Boolean, Double> pairVal = null;
		int quantity = Misc.getUndefInt();
		Pair< Double, Double> lapseQuantity = getLapseQuantity(conn,sapSalesOrder,sapLineItem);
		if(lapseQuantity != null) {
			double remaining_quantiy = lapseQuantity.first - lapseQuantity.second;
			boolean isQuantityAvailable = remaining_quantiy < netWt ? false : true;
			pairVal = new Pair<Boolean, Double> (isQuantityAvailable, lapseQuantity.second);
		}
		return pairVal;
	}

	public static double getRemainingQuantity(Connection conn, String sapSalesOrder,String sapLineItem) {
		Pair< Double, Double> lapseQuantity = getLapseQuantity(conn,sapSalesOrder,sapLineItem);
		double remaining_quantiy = 0;
		if(lapseQuantity != null) {
			remaining_quantiy = lapseQuantity.first - lapseQuantity.second;
		}
		return remaining_quantiy;
	}
	
	public static Pair<Double, Double> getLapseQuantity(Connection conn, String sapSalesOrder,String sapLineItem) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Double, Double> pairVal = null;
		boolean connClose = false;
		String query = "SELECT sap_order_quantity, sap_order_lapse_quantity FROM cgpl_sales_order where cgpl_sales_order.status=1 and cgpl_sales_order.port_node_id= ? and sap_sales_order = ? and sap_line_item = ?";
		try {
			if(conn == null) {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				connClose=true;
			}
			ps = conn.prepareStatement(query);
			ps.setInt(1, TokenManager.portNodeId);
			ps.setString(2, sapSalesOrder);
			ps.setString(3, sapLineItem);
//			System.out.print("GateInDao getLapseQuantity Query :" + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (pairVal == null)
					pairVal = new Pair<Double, Double>(rs.getDouble(1), rs.getDouble(2));
			}
		} catch (Exception ex) {
			 ex.printStackTrace();;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
				if(connClose && conn!=null) {
					try {
						DBConnectionPool.returnConnectionToPoolNonWeb(conn);
					} catch (GenericException e) {
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return pairVal;
	}

	public static void updateCGPLSalesOrder(Connection conn, String sapSalesOrder, String sapLineItem, double quantity) throws Exception {
		PreparedStatement ps = null;
		String query = "update cgpl_sales_order set cgpl_sales_order.sap_order_lapse_quantity = ? where sap_sales_order = ? and sap_line_item = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setDouble(1,  quantity);
			ps.setString(2, sapSalesOrder);
			ps.setString(3, sapLineItem);
			System.out.println("[updateCGPLSalesOrder: "+ps.toString()+" ]");
			ps.executeUpdate();
			conn.commit();
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static ArrayList<Object> getTransactionData(Connection conn, TPRecord tpr){
		return getTransactionData( conn,  tpr,null)	;
	}
	public static ArrayList<Object> getTransactionData(Connection conn, TPRecord tpr,Criteria cr) {
		ArrayList<Object> list = null;
		
		try {
			if(tpr == null) {
				tpr = new TPRecord();
			}
			
			list = (ArrayList<Object>) RFIDMasterDao.select(conn, tpr,cr);
			
		} catch (Exception ex) {
			Logger.getLogger(GateInDao.class.getName()).log(Level.SEVERE, null, ex);
		} 
		return list;
	}
	
	public static boolean checkSapQuantityAvailability(TprReportData tprReportData) {
		if (tprReportData == null)
			return false;

		boolean isExist = false;
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			isExist = checkSapQuantityAvailability(conn,tprReportData); 
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return isExist;
	}

	public static boolean checkSapQuantityAvailability(Connection conn, TprReportData tprReportData) throws Exception {
		if (tprReportData == null)
			return false;

		boolean isExist = false;

		double netWtCalc = Misc.getParamAsDouble(tprReportData.getLoadGross())
				- Misc.getParamAsDouble(tprReportData.getLoadTare());
		Pair<Boolean, Double> pairVal = GateInDao.isSalesOrderQuantityExist(conn, tprReportData.getSalesOrder(),
				tprReportData.getLineItem(), netWtCalc);
		if (pairVal != null && pairVal.first) {
			return true;
		}
		return isExist;
	}
	
	
	
	public static LocalDate NOW_LOCAL_DATE(DateTimeFormatter dateFormatter) {
		String date = UIConstant.slipFormat.format(Calendar.getInstance().getTime());
		return LocalDate.parse(date, dateFormatter);
	}

	
	public static boolean checkNumeric(String value)    {
	    String number=value.replaceAll("\\s+","");
	    for(int j = 0 ; j<number.length();j++){ 
	            if(!(((int)number.charAt(j)>=47 && (int)number.charAt(j)<=57)))
	            {
	                return false;
	            }
	    }     
	    return true;
	}
	
	
	
	public static void pageSetup(Node node, Stage owner,MainController parent) 
    {
        // Create the PrinterJob
        PrinterJob job = PrinterJob.createPrinterJob();
         
        if (job == null) 
        {
            return;
        }
         
        // Show the page setup dialog
        boolean proceed = job.showPageSetupDialog(owner);
         
        if (proceed) 
        {
            print(job, node, parent);
        }
    }
     
    public static void print(PrinterJob job, Node node,MainController parent) 
	{
		// Define the Job Status Message
		try {
			if (parent != null) {
				parent.labelBlockingReason.textProperty().unbind();
				parent.labelBlockingReason.setText("Creating a printer job...");
			}
//			Printer printer = Printer.getDefaultPrinter();
//			PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT,
//					Printer.MarginType.DEFAULT);

			if (job != null) {
				// Show the printer job status
				if (parent != null) {
					parent.labelBlockingReason.textProperty().bind(job.jobStatusProperty().asString());
				}
				// Print the node
				boolean printed = job.printPage(node);
				if (printed) {
					job.endJob();
				} else {
					if (parent != null) {
						parent.labelBlockingReason.setText("Printing failed.");
					}
				}
			} else {
				if (parent != null) {
					parent.labelBlockingReason.setText("Could not create a printer job.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}   

	
}

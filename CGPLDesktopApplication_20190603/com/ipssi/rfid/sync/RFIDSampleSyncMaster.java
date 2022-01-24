package com.ipssi.rfid.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.Utils;

public class RFIDSampleSyncMaster {
	public static final int USERS = 40001;
	public static final int MINESDEVICES = 40002;
	public static final int TABLESTATUS = 40010;
	public static final int DATETIME = 10001;
	public static final int LOT_DETAILS = 40003;
	public static final int SAMPLE_LOT_DETAILS = 40004;
	public static final int SAMPLE_POST_LOT_DETAILS = 40005;
	public static final int SAMPLE_POST_LOT_BY_ID = 40006;
	public static final int LAB_LIST = 40007;
	public static boolean isAutoLotCreated = false;
	public static int isAutoLot = 1;
	public static int isManualLot = 2;
	
	public static StringBuilder getProperty(Connection conn, int propertyId,
			int deviceId, int userId, long lastSync, int syncStatus)
			throws SQLException {
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
		try {
			switch (propertyId) {
			case USERS:
				result = getUsers(conn, deviceId, userId, lastSync, syncStatus);
				break;
			case TABLESTATUS:
				result = getTableStatus(conn, deviceId, userId, lastSync,
						syncStatus);
				break;

			case LOT_DETAILS:
				result = getLotScanDetails(conn, Misc.UNDEF_VALUE,
						Misc.UNDEF_VALUE, Misc.UNDEF_VALUE);
				break;
			case SAMPLE_LOT_DETAILS:
				result = getLotSamplingData(conn, Misc.UNDEF_VALUE,
						Misc.UNDEF_VALUE);
				break;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		retval.append("<result>");
		if (result == null) {
			retval.append("0");
		} else {
			retval.append(result.toString());
		}
		retval.append("</result>");

		return retval;
	}

	public static StringBuilder getLabListDetails(Connection conn) {

		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select id,name from mpl_lab_details where status=1  ";
		try {
			ps = conn.prepareStatement(query);
			System.out.println("LabList: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(LAB_LIST).append(
							"\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id"))
						.append("\" N=\"").append(rs.getString("name")).append(
								"\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retval;

	}
	public static StringBuilder getRfidSampleData(Connection conn,
			int sampleRead, int isLatest, int mplLotSampleId, String action)
			throws SQLException {
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
		try {
			if (action.equalsIgnoreCase("GET_LOT_NAME_LIST")) {
				result = getLotNameList(conn, sampleRead, isLatest);
			} else if (action.equalsIgnoreCase("GET_LOT_LIST")) {
				result = getLotScanData(conn, sampleRead, isLatest,
						mplLotSampleId);
			} else {
				if (isLatest == 1 && sampleRead == 0)
					result = getLotSamplingData(conn, sampleRead, isLatest);
				else
					result = getLotScanData(conn, sampleRead, isLatest,
							Misc.UNDEF_VALUE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		retval.append("<result>");
		if (result == null) {
			retval.append("0");
		} else {
			retval.append(result.toString());
		}
		retval.append("</result>");

		return retval;

	}

	public static StringBuilder getLotScanData(Connection conn, int sampleRead,
			int isLatest, int mplLotSampleId) throws SQLException {
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
		try {
			result = getLotScanDetails(conn, sampleRead, isLatest,
					mplLotSampleId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		retval.append("<result>");
		if (result == null) {
			retval.append("0");
		} else {
			retval.append(result.toString());
		}
		retval.append("</result>");

		return retval;

	}

	public static StringBuilder getLotNameList(Connection conn, int sampleRead,
			int isLatest) throws SQLException {
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
		try {
			result = getLotNameDetails(conn, sampleRead, isLatest);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		retval.append("<result>");
		if (result == null) {
			retval.append("0");
		} else {
			retval.append(result.toString());
		}
		retval.append("</result>");

		return retval;

	}

	public static StringBuilder getUsers(Connection conn, int deviceId,
			int userId, long lastSync, int syncStatus) throws SQLException {
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Date last_sync = Utils.getDateTime(lastSync);
		String query1 = "select distinct users.id,USERNAME,PASSWORD,NAME from users where isactive=1  ";
		String query = "select distinct users.id,USERNAME,PASSWORD,NAME from users where isactive=1 ";
		try {// mines_details
			if (syncStatus == 2) {
				ps = conn.prepareStatement(query);
				System.out.println("Users: " + ps.toString());
			} else {
				ps = conn.prepareStatement(query1);
				System.out.println("Users: " + ps.toString());
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(USERS).append("\" >");
				}
				retval.append("<field I=\"").append(Misc.getRsetInt(rs, "id"))
						.append("\" U=\"").append(rs.getString("USERNAME"))
						.append("\" P=\"").append(rs.getString("PASSWORD"))
						.append("\" N=\"").append(rs.getString("NAME")).append(
								"\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	public static StringBuilder getTableStatus(Connection conn, int deviceId,
			int userId, long lastSync, int syncStatus) throws SQLException {
		StringBuilder retval = null;

		// String tables[] = { "users","mpl_handheld_devices" };
		String tables[] = { "users" };
		Date last_sync = Utils.getDateTime(lastSync);
		System.out.println("getTableStatus()  lastSync: " + lastSync);

		int status = 0;
		int index = 40001;

		for (String table : tables) {
			System.out.println("Table Name: " + table);
			String query = "select  count(*) as diff from " + table
					+ " where created_on >?";
			String query1 = "select  count(*) as diff from " + table
					+ " where updated_on >?";

			if (getCount(conn, query, table, last_sync) > 0) {
				status = 2;
			} else if (getCount(conn, query1, table, last_sync) > 0) {
				status = 1;
			} else {
				status = 0;
			}
			if (retval == null) {
				retval = new StringBuilder();
				retval.append("<object id=\"").append(TABLESTATUS).append(
						"\" >");
			}
			retval.append("<field I=\"").append(index).append("\" S=\"")
					.append(status).append("\" />");
			index++;
		}
		if (retval != null)
			retval.append("<field I=\"").append(TABLESTATUS).append("\" S=\"")
					.append(Utils.getDateTimeLong(new Date())).append("\" />");
		retval.append("</object>");

		System.out.println("retval: " + retval);
		return retval;
	}

	public static int getCount(Connection conn, String query, String table,
			Date lastSync) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;

		try {
			ps = conn.prepareStatement(query);
			ps.setTimestamp(1, new Timestamp(lastSync.getTime()) == null ? null
					: new Timestamp(lastSync.getTime()));
			rs = ps.executeQuery();
			System.out.println("Table Status " + ps.toString());
			while (rs.next()) {
				count = rs.getInt("diff");
			}

		} catch (Exception ex) {

			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return count;

	}

	public static StringBuilder getMinesDevices(Connection conn, int deviceId,
			int userId, long lastSync, int syncStatus) throws SQLException {
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Date last_sync = Utils.getDateTime(lastSync);
		String query1 = "select device_id,rfFlag, toAllowFlag from mpl_handheld_devices where status=1  and mpl_handheld_devices.updated_on > ?";

		String query = "select device_id,rfFlag, toAllowFlag from mpl_handheld_devices where status=1 ";
		try {// mines_details
			if (syncStatus == 2) {
				ps = conn.prepareStatement(query);
				System.out.println("mines_devices: " + ps.toString());
			} else {
				ps = conn.prepareStatement(query1);
				ps.setTimestamp(1, new Timestamp(last_sync.getTime()));
				System.out.println("mines_devices: " + ps.toString());
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(MINESDEVICES).append(
							"\" >");
				}
				retval.append("<field D=\"").append(
						Misc.getRsetInt(rs, "device_id")).append("\" R=\"")
						.append(rs.getInt("rfFlag")).append("\" T=\"").append(
								rs.getInt("toAllowFlag")).append("\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	public static StringBuilder getLotScanDetails(Connection conn,
			int sampleRead, int isLatest, int mplLotSampleId)
			throws SQLException {// for use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select mpl_coal_sample_details.id,mpl_coal_sample_details.tpr_id"
				+ ",mpl_coal_sample_details.rfid_epc,mpl_coal_sample_details.sample_read,"
				+ "mpl_coal_sample_details.is_latest,mpl_lot_number.lot_name,mpl_coal_sample_details.mpl_lot_sample_id from mpl_coal_sample_details join mpl_lot_details   on (mpl_coal_sample_details.mpl_lot_sample_id=mpl_lot_details.id) join mpl_lot_number on (mpl_lot_details.id= mpl_lot_number.latest_assigned_id) where mpl_coal_sample_details.is_latest=? and mpl_coal_sample_details.sample_read=? and mpl_lot_details.status=1 and mpl_coal_sample_details.status=1 and mpl_coal_sample_details.mpl_lot_sample_id is not null";

		if (!Misc.isUndef(mplLotSampleId)) {
			query = query + " and mpl_coal_sample_details.mpl_lot_sample_id="
					+ mplLotSampleId;
		}
		try {// mines_details
			ps = conn.prepareStatement(query);
			ps.setInt(1, isLatest);
			ps.setInt(2, sampleRead);
			System.out.println("getLotScanDetails: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(LOT_DETAILS).append(
							"\" >");
				}
				retval
						.append("<field I=\"")
						.append(
								Misc.getRsetInt(rs,
										"mpl_coal_sample_details.id"))
						.append("\" T=\"")
						.append(rs.getInt("mpl_coal_sample_details.tpr_id"))
						.append("\" R=\"")
						.append(
								rs
										.getString("mpl_coal_sample_details.rfid_epc"))
						.append("\" S=\"")
						.append(
								rs
										.getInt("mpl_coal_sample_details.sample_read"))
						.append("\" L=\"")
						.append(rs.getInt("mpl_coal_sample_details.is_latest"))
						.append("\" N=\"")
						.append(rs.getString("mpl_lot_number.lot_name"))
						.append("\" M=\"")
						.append(
								rs
										.getString("mpl_coal_sample_details.mpl_lot_sample_id"))
						.append("\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	public static StringBuilder getLotNameDetails(Connection conn,
			int sampleRead, int isLatest) throws SQLException {// for use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select distinct mpl_lot_number.lot_name,mpl_coal_sample_details.mpl_lot_sample_id from mpl_coal_sample_details join mpl_lot_details  on (mpl_coal_sample_details.mpl_lot_sample_id=mpl_lot_details.id) join mpl_lot_number on (mpl_lot_details.id= mpl_lot_number.latest_assigned_id) where mpl_coal_sample_details.is_latest=? and mpl_coal_sample_details.sample_read=? and mpl_lot_details.status=1 and mpl_coal_sample_details.mpl_lot_sample_id is not null and mpl_coal_sample_details.sampling_done_time >? and mpl_coal_sample_details.sampling_done_time<? group by mpl_coal_sample_details.mpl_lot_sample_id";

		try {// mines_details
			int colPos = 1;
			ps = conn.prepareStatement(query);
			ps.setInt(colPos++, isLatest);
			ps.setInt(colPos++, sampleRead);
			ps.setString(colPos++, getPreviousDateString(false));
			ps.setString(colPos++, getPreviousDateString(true));
			System.out.println("getLotNameDetails (Sampling Data Table) : "	+ ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(LOT_DETAILS).append(
							"\" >");
				}
				retval.append("<field M=\"").append(
						Misc.getRsetInt(rs,
								"mpl_coal_sample_details.mpl_lot_sample_id"))
						.append("\" N=\"").append(
								rs.getString("mpl_lot_number.lot_name"))
						.append("\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	public static StringBuilder getLotName(Connection conn, String epc)
			throws SQLException {// for use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// String query =
		// "Select distinct mpl_lot_number.lot_name,mpl_coal_sample_details.mpl_lot_sample_id from mpl_coal_sample_details join mpl_lot_details  on (mpl_coal_sample_details.mpl_lot_sample_id=mpl_lot_details.id) join mpl_lot_number on (mpl_lot_details.id= mpl_lot_number.latest_assigned_id) where mpl_coal_sample_details.is_latest=2 and mpl_coal_sample_details.sample_read=2 and mpl_lot_details.status=1 and mpl_coal_sample_details.mpl_lot_sample_id is not null group by mpl_coal_sample_details.mpl_lot_sample_id";
		String query = "Select mpl_lot_details.name from mpl_lot_details join mpl_coal_sample_details on (mpl_coal_sample_details.mpl_lot_sample_id=mpl_lot_details.id) where mpl_coal_sample_details.is_latest=2 and mpl_coal_sample_details.sample_read=2 and mpl_lot_details.status=1 and mpl_coal_sample_details.rfid_epc=? order by mpl_coal_sample_details.id desc limit 1";
		try {// mines_details
			ps = conn.prepareStatement(query);
			System.out.println("getLotName: " + ps.toString());
			ps.setString(1, epc);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null)
					retval = new StringBuilder();

				// retval.append(rs.getString("mpl_lot_number.lot_name"));
				retval.append(rs.getString(1));
			}
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	
	
	public static StringBuilder getLotSamplingData(Connection conn,
			int sampleRead, int isLatest) throws SQLException {// for use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// String query =
		// "Select id,tpr_id,rfid_epc,sample_read from mpl_coal_sample_details where sample_read=0 and is_latest=1";
		String query = "Select mpl_coal_sample_details.id,mpl_coal_sample_details.tpr_id,mpl_coal_sample_details.rfid_epc,mpl_coal_sample_details.sample_read,mpl_lot_details.name from mpl_coal_sample_details left join mpl_lot_details   on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id) where mpl_coal_sample_details.sample_read=? and mpl_coal_sample_details.is_latest=?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, sampleRead);
			ps.setInt(2, isLatest);
			System.out.println("getLotSamplingData: " + ps.toString());

			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(SAMPLE_LOT_DETAILS)
							.append("\" >");
				}
				retval
						.append("<field I=\"")
						.append(
								Misc.getRsetInt(rs,
										"mpl_coal_sample_details.id"))
						.append("\" T=\"")
						.append(
								Misc.getRsetInt(rs,
										"mpl_coal_sample_details.tpr_id"))
						.append("\" R=\"")
						.append(
								rs
										.getString("mpl_coal_sample_details.rfid_epc"))
						.append("\" S=\"").append(
								Misc.getRsetInt(rs,
										"mpl_coal_sample_details.sample_read"))
						.append("\" N=\"").append(
								rs.getString("mpl_lot_details.name")).append(
								"\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	public static StringBuilder checkSampleTag(Connection conn, String epc)
			throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder retval = null;
		// String query =
		// "Select id,tpr_id,rfid_epc,sample_read from mpl_coal_sample_details where sample_read=0 and is_latest=1";
		String query = "Select mpl_coal_sample_details.id,mpl_coal_sample_details.tpr_id,tp_record.vehicle_name from mpl_coal_sample_details join tp_record on (tp_record.tpr_id=mpl_coal_sample_details.tpr_id) where mpl_coal_sample_details.sample_read=0 and mpl_coal_sample_details.is_latest=1 and mpl_coal_sample_details.status=1 and mpl_coal_sample_details.rfid_epc=?";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, epc);
			System.out.println("checkSampleTag: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object>");
				}
				retval.append("<field I=\"").append(
						Misc.getRsetInt(rs, "mpl_coal_sample_details.id"))
						.append("\" T=\"").append(
								rs.getString("mpl_coal_sample_details.tpr_id"))
						.append("\" N=\"").append(
								rs.getString("tp_record.vehicle_name")).append(
								"\" />");
			}

			if (retval != null)
				retval.append("</object>");

		} catch (Exception ex) {
			ex.printStackTrace();
			retval = null;
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}
	static Date prevDate = null;
	public static void main(String args[]) {
		// TODO Auto-generated method stub
		Connection conn = null;
		boolean destroyIt = false;
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			// RFIDSampleSyncMaster.setSampleStatus(conn,
			// "E2001026780800600860BF02");
//			RFIDSampleSyncMaster.autoGeneratePostLots(conn);
//			String s = getPreviousDateString(false);
//			String ss =  getPreviousDateString(true);
//			System.out.println(s + " , "+ ss);
//			scheduleAutoGeneratePostLots(conn,true);//
//			RFIDSampleSyncMaster.changeSampleStatus(conn,1,2,null);
			Date currDate = null;
			
			//String d = "14-11-2018";
			//prevDate =  dateFormatter.parse(d);
//			int i=1;
//			while(i<5){
//				currDate = dateFormatter.parse(dateFormatter.format(new Date().getTime()));
//				if(prevDate==null || prevDate.before(currDate)){
					autoGenerateSubLots(conn,true);
//					prevDate = currDate;
//					i++;
//				}
//			}
//			autoGeneratePostLots(conn,true);//
//			autoGeneratePostLots(conn,false);
		//	getSamplingDoneCounts(conn,1,1,1,1);
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static StringBuilder changeSampleStatus(Connection conn,
			byte[] dataByte, int formDataLength, int totalBytesRead,
			int byteRead) throws Exception {
		StringBuilder retval = new StringBuilder();
		// PreparedStatement ps = null;
		// ResultSet rs = null;
		int recordCount = dataByte[0];
		int recordindex = 0;
		int byteindex = 1;

		while (++recordindex <= recordCount) {
			RFIDTagInfo tag = null;
			RFIDHolder holder = null;
			byte[] data = null;
			boolean status = false;
			PreparedStatement ps = null;
			// java.sql.Date now = new java.sql.Date((new java.util.Date())
			// .getTime());

			try {
				String vehicleName = null;
				String epcId = null;
				int sampleRead = Misc.UNDEF_VALUE;
				if (dataByte != null && dataByte.length > 0) {
					tag = new RFIDTagInfo();
					tag.epcId = Arrays.copyOfRange(dataByte, byteindex,
							byteindex + 12);
					byteindex = byteindex + 12;
					tag.userData = Arrays.copyOfRange(dataByte, byteindex,
							byteindex + 64);
					byteindex = byteindex + 64;
					holder = new RFIDHolder(null, tag);
					vehicleName = holder.getVehicleName();
					epcId = holder.getEpcId();
				}
				System.out.println("[Web RFID Tag Data]:" + tag);
				System.out.println("[Web RFID Vehicle]:" + vehicleName);
				System.out.println("[Web RFID EPC]:" + epcId);
				if (epcId != null || vehicleName != null) {
					String sql = "Update mpl_coal_sample_details set sample_read= ?  where rfid_epc=? And is_latest =1 and status=1";
					// String sql =
					// "update  mpl_coal_sample_details set sample_read=1 where rfid_epc=?";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, sampleRead);
					ps.setString(2, epcId);
					int rowUpdatedCount = ps.executeUpdate();
					status = true;
					conn.commit();
					retval.append(1);
					if (rowUpdatedCount > 0) {
						startLotAllotment(50);
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				retval.append("0");
			}
		}
		System.out.println("formDataLength=" + formDataLength);
		System.out.println("totalBytesRead=" + totalBytesRead);
		System.out.println("byteRead=" + byteRead);
		System.out.println("retvalSync=" + retval);
		return retval;
	}

	private static void startAllotment(int lotCount, String epc) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection conn = null;
		boolean destroyIt = false;
		String sql = "Select tp_record.supplier_id, tp_record.mines_id, tp_record.transporter_id, tp_record.material_grade_id, mpl_coal_sample_details.sampling_done_time from tp_record  join "
				+ "mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) "
				+ " where mpl_coal_sample_details.sample_read = 1 and mpl_coal_sample_details.status=1 and  mpl_coal_sample_details.rfid_epc = ? and mpl_coal_sample_details.is_latest=1";

		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(sql);
			ps.setString(1, epc);
			System.out.println("[RFIDSampleSyncMaster][startAllotment]: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				startLotAllotment(lotCount, rs.getInt("tp_record.supplier_id"), rs.getInt("tp_record.mines_id"),
						rs.getInt("tp_record.transporter_id"), rs.getInt("tp_record.material_grade_id"),rs.getString("mpl_coal_sample_details.sampling_done_time"));
			}
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private static void startLotAllotment(int lotCount, int supplierId,
			int minesId, int transporterId, int material_grade_id, String samplingDoneTime) {
		String lotNamePrefix = "sub_lot_";
		// String sql =
		// "Select supplier_id,transporter_id,mines_id,material_grade_id ,count(*) countVal from tp_record  join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) group by supplier_id,transporter_id,mines_id,material_grade_id and sample_read = 1";
		String sql = "Select count(*) countVal from tp_record  join mpl_coal_sample_details on"
				+ " (tp_record.tpr_id= mpl_coal_sample_details.tpr_id)"
				+ "where sample_read = 1 and supplier_id=? and transporter_id=? and mines_id=? and material_grade_id=? and sampling_done_time>? and sampling_done_time<?  group by supplier_id,transporter_id,mines_id,material_grade_id";
		
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection conn = null;
		boolean destroyIt = false;
		boolean lotCompleted = false;
		int totalLotCount = lotCount;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(sql);
			int colPos=1;
			ps.setInt(colPos++, supplierId);
			ps.setInt(colPos++, transporterId);
			ps.setInt(colPos++, minesId);
			ps.setInt(colPos++, material_grade_id);
			ps.setString(colPos++, getSamplingDoneTime(samplingDoneTime,0));
			ps.setString(colPos++, getSamplingDoneTime(samplingDoneTime,1));

			System.out.println("[RFIDSampleSyncMaster][startLotAllotment]: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				lotCompleted = rs.getInt(1) >= totalLotCount ? true : false;
				if (lotCompleted) {
					int generatedId = insertLotData(conn, lotNamePrefix,"mpl_lot_details");
					updateCoalSampleData(conn, supplierId, transporterId,
							minesId, material_grade_id, generatedId,samplingDoneTime);

					conn.commit();
				}
			}
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}



	private static void startLotAllotment(int lotCount) {
		String sql = "Select supplier_id,transporter_id,mines_id,material_grade_id ,count(*) countVal from tp_record  join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) group by supplier_id,transporter_id,mines_id,material_grade_id and sample_read=1";
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection conn = null;
		boolean destroyIt = false;
		boolean lotCompleted = false;
		int supplierId = Misc.getUndefInt();
		int minesId = Misc.getUndefInt();
		int transporterId = Misc.getUndefInt();
		int gradeId = Misc.getUndefInt();
		int totalLotCount = lotCount;
		String lotNamePrefix = "sub_lot_";

		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(sql);
			System.out.println("startLotAllotment: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				lotCompleted = rs.getInt(5) >= totalLotCount ? true : false;
				if (lotCompleted) {
					supplierId = rs.getInt(1);
					transporterId = rs.getInt(2);
					minesId = rs.getInt(3);
					gradeId = rs.getInt(4);
					int generatedId = insertLotData(conn, lotNamePrefix, "mpl_lot_number");
//					updateCoalSampleData(conn, supplierId, transporterId,
//							minesId, gradeId, generatedId);

					conn.commit();
				}
			}
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	private static void updateCoalSampleData(Connection conn, int supplierId,
			int transporterId, int minesId, int gradeId, int generatedId,String samplingDoneTime) {
		PreparedStatement ps = null;
		int sampleRead = 2;
		int isLatest = 2;
		String sql = "Update mpl_coal_sample_details join tp_record on (mpl_coal_sample_details.tpr_id= tp_record.tpr_id) set mpl_coal_sample_details.tag_issued=1, mpl_coal_sample_details.mpl_lot_sample_id=?, mpl_coal_sample_details.sample_read= ?,mpl_coal_sample_details.is_latest= ? where  mpl_coal_sample_details.status=1 and mpl_coal_sample_details.sample_read=1 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ?  and tp_record.supplier_id=? and tp_record.transporter_id=? and tp_record.mines_id=? and tp_record.material_grade_id=? ";
		try {
			int colPos = 1;
			
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, generatedId, colPos++);
			Misc.setParamInt(ps, sampleRead, colPos++);
			Misc.setParamInt(ps, isLatest, colPos++);
//			ps.setString(colPos++, getPreviousDateString(false));
//			ps.setString(colPos++, getPreviousDateString(true));
			ps.setString(colPos++, getSamplingDoneTime(samplingDoneTime,0));
			ps.setString(colPos++, getSamplingDoneTime(samplingDoneTime,1));
			Misc.setParamInt(ps, supplierId, colPos++);
			Misc.setParamInt(ps, transporterId, colPos++);
			Misc.setParamInt(ps, minesId, colPos++);
			Misc.setParamInt(ps, gradeId, colPos++);
			System.out.println("[RFIDSampleSyncMaster][updateCoalSampleData]: " + ps.toString());
			ps.executeUpdate();

			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static int insertLotData(Connection conn,String lotNamePrefix, String tableName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int status = 1;
		boolean isInserted = false;
		String sql = "Insert into " + tableName	+ " (name,created_on,status,port_node_id) values (?,?,?,?)";
		
		int generatedVal = Misc.getUndefInt();
		try {
			String lotNumber = getLotNumber(conn,lotNamePrefix ,"mpl_lot_number");
			ps = conn.prepareStatement(sql);
			ps.setString(1, lotNumber);
			ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			ps.setInt(3, status);
			ps.setInt(4, 463);
			ps.executeUpdate();

			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				generatedVal = rs.getInt(1);
				isInserted = true;
			}

			if (isInserted)
				updateLotNumber(conn, generatedVal, lotNumber, "mpl_lot_number");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return generatedVal;
	}

	private static String getLotNumber(Connection conn,String lotNamePrefix, String tableName) {
		String sql = "select lot_name from " + tableName
				+ " where is_free=0 and updated_on < ? order by id limit 1";
		ResultSet rs = null;
		PreparedStatement ps = null;
		String lotNumber = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getPreviousDateString(true));
			System.out.println("getLotNumber new: " + ps.toString());
			rs = ps.executeQuery();
			
			while (rs.next()) {
				lotNumber = rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		if (lotNumber == null)
			lotNumber = insertLotNumber(conn, lotNamePrefix, tableName);
		return lotNumber;
	}

	private static String insertLotNumber(Connection conn,String lotNamePrefix ,String tableName) {
		String lotNumber = null;
		//String lotName = "sub_lot_";
		tableName = Utils.isNull(tableName) ? "mpl_lot_number" : tableName;
		PreparedStatement ps = null;
		String sql = "Insert into "
				+ tableName
				+ "(lot_name,is_free,updated_on,created_on) values (?,0,now(),now())";
		try {

			lotNumber = getNewlotNumber(conn, lotNamePrefix, tableName);
			ps = conn.prepareStatement(sql);
			ps.setString(1, lotNumber);
			ps.executeUpdate();
			//conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return lotNumber;
	}

	private static String getNewlotNumber(Connection conn, String lotName,
			String tableName) {
		tableName = Utils.isNull(tableName) ? "mpl_lot_number" : tableName;
		String sql = "Select count(id) county from " + tableName;
		// String lotName ="sub_lot_";
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			System.out.println("getNewlotNumber(),using table: " + tableName
					+ " " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				lotName += rs.getInt(1) + 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (ps != null)
					ps.close();

				if (rs != null)
					rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return lotName;

	}

	private static String updateLotNumber(Connection conn, int lotSampleId,
			String lotName, String tableName) {
		String sql = "update " + tableName
				+ " set latest_assigned_id = ? , is_free=?, updated_on=now() where lot_name = ?";
		PreparedStatement ps = null;
		String lotNumber = null;
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, lotSampleId, colPos++);
			Misc.setParamInt(ps, 1, colPos++);
			
			ps.setString(colPos++, lotName);
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return lotNumber;
	}

	public static StringBuilder changeSampleStatus(Connection conn, int id,
			int lotCount, String epc) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		int sampleRead = 1;
		StringBuilder retval = checkSampleTag(conn, epc);

		if (retval == null)
			return retval;

		try {
			String sql = "update mpl_coal_sample_details set sample_read= ?, updated_on=?, sample_read_time=? where is_latest=1 and status=1 and rfid_epc=? order by id desc";
			ps = conn.prepareStatement(sql);
			int colPos = 1;
			ps.setInt(colPos++, sampleRead);
			ps.setTimestamp(colPos++, new Timestamp(System.currentTimeMillis()));
			ps.setTimestamp(colPos++, new Timestamp(System.currentTimeMillis()));
			ps.setString(colPos++, epc);
			ps.execute();
			System.out.println("[RFIDSampleSysncMaster][changeSampleStatus]: "	+ ps.toString());
			conn.commit();
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close(); // close result set
				}
				if (rs != null) {
					rs.close(); // close result set
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		if (retval != null) {
			startAllotment(lotCount, epc);
			// startLotAllotment(lotCount);
		}
		return retval;
	}

	private static void updateLinkSampleData(Connection conn,
			int mpl_lot_sample_id, int sample_read) {
		PreparedStatement ps = null;
		// java.sql.Date now = new java.sql.Date((new
		// java.util.Date()).getTime());
		// sample_read = 3
		// String sql =
		// "Update mpl_coal_sample_details set  mpl_lot_detail_id=?, sample_read= ?, sampling_done_time = ? , is_latest=? where  supplier_id=? and transporter_id=? and mines_id=? and material_grade_id=?";
		String sql = "Update mpl_coal_sample_details set mpl_coal_sample_details.sample_read= ? where  mpl_coal_sample_details.mpl_lot_sample_id=?";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, sample_read, colPos++);// sample_read = 3
			Misc.setParamInt(ps, mpl_lot_sample_id, colPos++);
			ps.executeUpdate();
			System.out.println("RFIDSampleSysncMaster : updateLinkSampleData "
					+ ps.toString());
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static StringBuilder updateNewLinkSampleData(Connection conn,
			int mpl_lot_sample_id, String epc, int sampleRead) {
		StringBuilder retval = new StringBuilder();
		PreparedStatement ps = null;
		String sql = "Update mpl_lot_details set epc_code=?,tag_issue_time= ? where id=?";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			ps.setString(colPos++, epc);
			ps.setTimestamp(colPos++, new Timestamp(System.currentTimeMillis()));
			Misc.setParamInt(ps, mpl_lot_sample_id, colPos++);
			ps.executeUpdate();
			System.out.println("RFIDSampleSysncMaster: updateNewLinkSampleData "+ ps.toString());
			RFIDSampleSyncMaster.updateLinkSampleData(conn, mpl_lot_sample_id,
					sampleRead);
			conn.commit();
			retval.append(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			retval.append("0");
		} finally {
			try {
				if (ps != null) {
					ps.close(); // close result set
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return retval;
	}

	public static StringBuilder getPostLotDetails(Connection conn,
			int port_node_id) {
		// int sampleRead=3;
		int portNodeId = Misc.isUndef(port_node_id) ? 463 : port_node_id;
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
		try {
			result = getPostLots(conn, portNodeId);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		retval.append("<result>");
		if (result == null) {
			retval.append("0");
		} else {
			retval.append(result.toString());
		}
		retval.append("</result>");
		return retval;
	}

	public static StringBuilder getLotPostsId(Connection conn, int postLotId) {// for
																				// use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select  mpl_lot_details.id,mpl_lot_details.epc_code,mpl_lot_details.mpl_post_lot_id  from mpl_lot_details left join mpl_coal_sample_details on (mpl_lot_details.id  = mpl_coal_sample_details.mpl_lot_sample_id)  where   sample_read=4  and mpl_lot_details.mpl_post_lot_id =  ? group by mpl_lot_details.epc_code";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, postLotId);
			System.out.println("getSubDetails: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"")
							.append(SAMPLE_POST_LOT_BY_ID).append("\" >");
				}
				retval.append("<field I=\"").append(
						Misc.getRsetInt(rs, "mpl_lot_details.id")).append(
						"\" R=\"").append(
						rs.getString("mpl_lot_details.epc_code")).append(
						"\" M=\"").append(
						rs.getInt("mpl_lot_details.mpl_post_lot_id")).append(
						"\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retval;
	}

	private static StringBuilder getPostLots(Connection conn, int port_node_id) {// for
																					// use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select mpl_post_lot_details.id, mpl_post_lot_details.name from mpl_lot_details join  mpl_post_lot_details on (mpl_lot_details.mpl_post_lot_id = mpl_post_lot_details.id) left join mpl_post_lot_number on (mpl_post_lot_details.id=mpl_post_lot_number.latest_assigned_id) where mpl_post_lot_details.tag_issue_time is null and mpl_post_lot_details.status=1 and mpl_post_lot_number.is_free=1 and mpl_lot_details.mpl_post_lot_id is not null and mpl_lot_details.created_on > ? group by mpl_post_lot_id";
		
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(true));
			System.out.println("getPostLots: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(
							SAMPLE_POST_LOT_DETAILS).append("\" >");
				}
				retval.append("<field I=\"").append(
						Misc.getRsetInt(rs, "mpl_post_lot_details.id")).append(
						"\" N=\"").append(
						rs.getString("mpl_post_lot_details.name")).append(
						"\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retval;
	}

	public static StringBuilder getPostLotById(Connection conn, int mplPostLotId)
			throws SQLException {
		StringBuilder retval = new StringBuilder();
		StringBuilder result = null;
		try {
			result = getPostLotScanDetails(conn, mplPostLotId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		retval.append("<result>");
		if (result == null) {
			retval.append("0");
		} else {
			retval.append(result.toString());
		}
		retval.append("</result>");

		return retval;

	}

	public static StringBuilder getPostLotScanDetails(Connection conn,
			int mplLotSampleId) throws SQLException {// for use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select mpl_coal_sample_details.id,mpl_coal_sample_details.tpr_id"
				+ ",mpl_coal_sample_details.rfid_epc,mpl_coal_sample_details.sample_read,"
				+ "mpl_coal_sample_details.is_latest,mpl_lot_number.lot_name,mpl_coal_sample_details.mpl_lot_sample_id from mpl_coal_sample_details join mpl_lot_details   on (mpl_coal_sample_details.mpl_lot_sample_id=mpl_lot_details.id) join mpl_lot_number on (mpl_lot_details.id= mpl_lot_number.latest_assigned_id) where mpl_coal_sample_details.is_latest=? and mpl_coal_sample_details.sample_read=? and mpl_lot_details.status=1 and mpl_coal_sample_details.mpl_lot_sample_id is not null";

		if (!Misc.isUndef(mplLotSampleId)) {
			query = query + " and mpl_coal_sample_details.mpl_lot_sample_id="
					+ mplLotSampleId;
		}
		try {// mines_details
			ps = conn.prepareStatement(query);

			System.out.println("getLotScanDetails: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null) {
					retval = new StringBuilder();
					retval.append("<object id=\"").append(LOT_DETAILS).append(
							"\" >");
				}
				retval
						.append("<field I=\"")
						.append(
								Misc.getRsetInt(rs,
										"mpl_coal_sample_details.id"))
						.append("\" T=\"")
						.append(rs.getInt("mpl_coal_sample_details.tpr_id"))
						.append("\" R=\"")
						.append(
								rs
										.getString("mpl_coal_sample_details.rfid_epc"))
						.append("\" S=\"")
						.append(
								rs
										.getInt("mpl_coal_sample_details.sample_read"))
						.append("\" L=\"")
						.append(rs.getInt("mpl_coal_sample_details.is_latest"))
						.append("\" N=\"")
						.append(rs.getString("mpl_lot_number.lot_name"))
						.append("\" M=\"")
						.append(
								rs
										.getString("mpl_coal_sample_details.mpl_lot_sample_id"))
						.append("\" />");
			}
			if (retval != null)
				retval.append("</object>");
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
	}

	public static StringBuilder updatePostLot(Connection conn, String epc,
			int mplPostLotId, int labId) {
		StringBuilder retval = new StringBuilder();
		PreparedStatement ps = null;
		java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
		boolean isLabExist = isTagWithSameLabExist(conn, mplPostLotId, labId);
		// updateLotLabDetailsForEpc(conn,epc, labId);
		boolean isOpen = isOpenTagForOtherLab(conn, epc);
		String sql = "";
		int status = 1;
		if (!isLabExist)
			sql = "insert into mpl_lots_lab_details (rfid_epc,created_on,status,lab_details_id,post_sample_lot_id) value (?,?,?,?,?)";
		else
			sql = "Update mpl_lots_lab_details set rfid_epc=?,status=? where lab_details_id=? and post_sample_lot_id = ?";
		
		if (isOpen) {
			retval.append("2");
		} else {
			try {
				int colPos = 1;

				ps = conn.prepareStatement(sql);
				ps.setString(colPos++, epc);
				if (!isLabExist)
					ps.setTimestamp(colPos++, new Timestamp(System
							.currentTimeMillis()));

				Misc.setParamInt(ps, status, colPos++);
				Misc.setParamInt(ps, labId, colPos++);
				Misc.setParamInt(ps, mplPostLotId, colPos++);
				ps.executeUpdate();
				System.out.println("RFIDSampleSysncMaster : updatePostLot() "
						+ ps.toString());
				RFIDSampleSyncMaster.updatePostLotDetailsById(conn,
						mplPostLotId);
				RFIDSampleSyncMaster
						.updateCoalSampleDetails(conn, mplPostLotId);
				conn.commit();
				retval.append(1);
			} catch (Exception ex) {
				ex.printStackTrace();
				retval.append("0");
			} finally {
				try {
					if (ps != null) {
						ps.close(); // close result set
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		return retval;
	}

	private static boolean isOpenTagForOtherLab(Connection conn, String epc) {
		// for use
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = " Select mpl_post_lot_details.id  from mpl_post_lot_details join mpl_lots_lab_details on (mpl_post_lot_details.id = mpl_lots_lab_details.post_sample_lot_id) where mpl_lots_lab_details.status = 1 and mpl_lots_lab_details.sample_upload_tag_read is null and mpl_lots_lab_details.rfid_epc=?";
		boolean isTrue = false;
		
		try {// mines_details
			ps = conn.prepareStatement(query);
			ps.setString(1, epc);
			System.out.println("[RFIDSampleSyncMaster][isOpenTagForOtherLab]: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				isTrue=true;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
		return isTrue;
	}

	private static void updateLotLabDetailsForEpc(Connection conn, String epc,int labId) {
		PreparedStatement ps = null;
		String sql = "Update mpl_lots_lab_details set status=2 where status=1 and rfid_epc = ? and post_sample_lot_id != ? ";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			ps.setString(colPos++, epc);
			ps.setInt(colPos++, labId);
			ps.executeUpdate();
			System.out.println("RFIDSampleSysncMaster: updateLotLabDetailsForEpc() "+ ps.toString());
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close(); // close result set
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	
	}

	private static void updatePostLotDetailsById(Connection conn,
			int mplPostLotId) {
		PreparedStatement ps = null;
		 int tagIssued = 1;
		 java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
		 String sql = "Update mpl_post_lot_details set tag_issued=?,tag_issue_time= ? where id=?";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, tagIssued, colPos++);
			ps.setTimestamp(colPos++, new Timestamp(System.currentTimeMillis()));
			Misc.setParamInt(ps, mplPostLotId, colPos++);
			ps.executeUpdate();
			System.out.println("RFIDSampleSysncMaster : updatePostLotDetailsById() "+ ps.toString());
			//conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static boolean isTagWithSameLabExist(Connection conn,int mplPostLotId, int labId) {// for use
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select id from mpl_lots_lab_details where status=1 and  post_sample_lot_id=? and lab_details_id=?";
		boolean isTrue = false;
		
		try {// mines_details
			ps = conn.prepareStatement(query);
			ps.setInt(1, mplPostLotId);
			ps.setInt(2, labId);
			System.out.println("isTagWithSameLabExist: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				isTrue=true;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
		return isTrue;
	}

	private static void updateCoalSampleDetails(Connection conn,
			int mplPostLotId) {
		PreparedStatement ps = null;
		// int sample_read = 5;
		String sql = "Update mpl_coal_sample_details set sample_read=5 where mpl_lot_sample_id in (Select mpl_lot_details.id from mpl_lot_details where mpl_lot_details.mpl_post_lot_id= ?)";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, mplPostLotId, colPos++);
			ps.executeUpdate();
			System.out.println("RFIDSampleSysncMaster : updateCoalSampleDetails() "+ ps.toString());
			//conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

//	public static void autoGenerateLots(Connection conn) {
//
//		java.sql.Date now = new java.sql.Date((new java.util.Date()).getTime());
//		Timestamp ts = new Timestamp(System.currentTimeMillis());
//
//		// String sql =
//		// "Select mpl_lot_details.id from  mpl_coal_sample_details join mpl_lot_details on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id) join mpl_lot_number on (mpl_lot_details.id = mpl_lot_number.latest_assigned_id)  where mpl_lot_details.epc_code is not null and  mpl_coal_sample_details.sample_read = 3 and mpl_lot_number.is_free=1 group by  mpl_lot_details.id";
//		String sql = "Select tp_record.supplier_id, tp_record.transporter_id, tp_record.mines_id, tp_record.material_grade_id, mpl_lot_details.id mpl_lot_id  from tp_record join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) join mpl_lot_details on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id) join mpl_post_lot_details on (mpl_lot_details.mpl_post_lot_id = mpl_post_lot_details.id) where mpl_coal_sample_details.created_on < ? and sample_read = 3 and mpl_post_lot_id is null and tp_record.transporter_id is not null and tp_record.supplier_id  is not null and  tp_record.mines_id is not null and  tp_record.material_grade_id is not null";
//		ArrayList<Integer> mplLotId = new ArrayList<Integer>();
//
//		// check tag issued for sublot tag_issued=1
//		// updateCoalSample(conn,mplLotId);
//		// updatePostLotNumber(mplLotId);
//		// -- update sampleRead=4
//		// -- create post lot number and
//		// -- free lot_number free=0
//	}


	public static void autoGenerateSubLots(Connection conn,boolean isAuto) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		String lotNamePrefix = "sub_lot_";
		String query = "Select  tp_record.supplier_id ,tp_record.transporter_id , tp_record.mines_id ,  tp_record.material_grade_id,count(id) from tp_record join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) where  sample_read=1 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ? and mpl_lot_sample_id is null and tp_record.transporter_id is not null and tp_record.supplier_id is not null and tp_record.mines_id is not null and  tp_record.material_grade_id is not null group by tp_record.transporter_id , tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			System.out.println("Manually genearate Sub lots: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if(!isAuto){
					int count = getPreviousDaySampleReadCount(conn, rs.getInt(1),rs
							.getInt(2), rs.getInt(3),
							rs.getInt(4));
					if(count == rs.getInt(5)){
						int generatedId = insertLotData(conn, lotNamePrefix,"mpl_lot_details");
						updateCoalSampleData(conn, rs.getInt(1),rs.getInt(2), rs.getInt(3),
								rs.getInt(4), generatedId,getPreviousDateString(false));
					}
				}else{
					int generatedId = insertLotData(conn, lotNamePrefix,"mpl_lot_details");
					updateCoalSampleData(conn, rs.getInt(1),rs.getInt(2), rs.getInt(3),
							rs.getInt(4), generatedId,getPreviousDateString(false));
				}
			}

			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
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
		
	}

	private static int getPreviousDaySampleReadCount(Connection conn, int supplierId,
			int transporterId, int minesId, int gradeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;		
		String query = "Select  count(id) from tp_record join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) where mpl_lot_sample_id is null and mpl_coal_sample_details.status=1 and rfid_epc is not null and sampling_done_time > ? and sampling_done_time < ?  and  tp_record.supplier_id =? and tp_record.transporter_id =? and tp_record.mines_id =? and  tp_record.material_grade_id =?";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			ps.setInt(3, supplierId);
			ps.setInt(4, transporterId);
			ps.setInt(5, minesId);
			ps.setInt(6, gradeId);
			System.out.println("getSampleReadCount(): " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				 count = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	return count;
}

	public static void autoGeneratePostLots(Connection conn, boolean isAuto) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Integer, String> pairStr = null;
		ArrayList<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();

		String query = "Select mpl_lot_details.id lot_details_id, mpl_lot_details.epc_code,tp_record.transporter_id , tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id,count(*) countSubLots from tp_record join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) join mpl_lot_details on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id)  where epc_code is not null and mpl_post_lot_id is null ";

		if(isAuto){
			//query += " and sample_read=3 and mpl_lot_details.tag_issue_time > ? and mpl_lot_details.tag_issue_time < ? ";
			query += " and sample_read=3 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ? ";
		}else{
			query += " and sample_read=3 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ? ";
		}

		query += " group by mpl_lot_details.id, tp_record.transporter_id , tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id ORDER BY  mpl_lot_details.id asc";

		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			System.out.println("Manually genearate post lots: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if(!isAuto){
					int _count = getSamplingDoneCounts(conn,rs.getInt("tp_record.transporter_id"), rs.getInt("tp_record.supplier_id"), rs.getInt("tp_record.mines_id") ,rs.getInt("tp_record.material_grade_id"));
					int _count2 = getSamplingDoneCountNew(conn,rs.getInt("tp_record.transporter_id"), rs.getInt("tp_record.supplier_id"), rs.getInt("tp_record.mines_id") ,rs.getInt("tp_record.material_grade_id"));
					if(_count == _count2){
						String str = rs.getInt("tp_record.transporter_id") + "_"
						+ rs.getInt("tp_record.supplier_id") + "_"
						+ rs.getInt("tp_record.mines_id") + "_"
						+ rs.getInt("tp_record.material_grade_id");
						pairStr = new Pair<Integer, String>(rs.getInt(1), str);
						list.add(pairStr);	
					}
				}else{
					String str = rs.getInt("tp_record.transporter_id") + "_"
					+ rs.getInt("tp_record.supplier_id") + "_"
					+ rs.getInt("tp_record.mines_id") + "_"
					+ rs.getInt("tp_record.material_grade_id");
					pairStr = new Pair<Integer, String>(rs.getInt(1), str);
					list.add(pairStr);
				}
			}

			if (list != null && list.size() > 0) {
				insertPostLotList(conn, list, "mpl_post_lot_details" , isAuto);
				conn.commit();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
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
	}

	public static void scheduleAutoGeneratePostLots(Connection conn, boolean isAuto) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Integer, String> pairStr = null;
		ArrayList<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();

		String query = "Select mpl_lot_details.id lot_details_id, mpl_lot_details.epc_code,tp_record.transporter_id , tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id,count(*) countSubLots from tp_record join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) join mpl_lot_details on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id)  where epc_code is not null and mpl_post_lot_id is null ";

//		if(isAuto){
//			query += " and sample_read=3 and mpl_lot_details.tag_issue_time > ? and mpl_lot_details.tag_issue_time < ? ";
//		}else{
			query += " and sample_read=3 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ? ";
//		}

		query += " group by mpl_lot_details.id, tp_record.transporter_id , tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id ORDER BY  mpl_lot_details.id asc";

		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			System.out.println("auto genearate post lots: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if(!isAuto){
					int _count = getSamplingDoneCounts(conn,rs.getInt("tp_record.transporter_id"), rs.getInt("tp_record.supplier_id"), rs.getInt("tp_record.mines_id") ,rs.getInt("tp_record.material_grade_id"));
					int _count2 = getSamplingDoneCountNew(conn,rs.getInt("tp_record.transporter_id"), rs.getInt("tp_record.supplier_id"), rs.getInt("tp_record.mines_id") ,rs.getInt("tp_record.material_grade_id"));
					if(_count == _count2){
						String str = rs.getInt("tp_record.transporter_id") + "_"
						+ rs.getInt("tp_record.supplier_id") + "_"
						+ rs.getInt("tp_record.mines_id") + "_"
						+ rs.getInt("tp_record.material_grade_id");
						pairStr = new Pair<Integer, String>(rs.getInt(1), str);
						list.add(pairStr);	
					}
				}else{
					String str = rs.getInt("tp_record.transporter_id") + "_"
					+ rs.getInt("tp_record.supplier_id") + "_"
					+ rs.getInt("tp_record.mines_id") + "_"
					+ rs.getInt("tp_record.material_grade_id");
					pairStr = new Pair<Integer, String>(rs.getInt(1), str);
					list.add(pairStr);
				}
			}

			if (list != null && list.size() > 0) {
				insertPostLotList(conn, list, "mpl_post_lot_details",isAuto);
				conn.commit();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
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
	}


	
	
	private static int getSamplingDoneCountNew(Connection conn, int transporterId, int supplierId, int minesId,int gradeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;		
		String query = "Select count(*) countSubLots from tp_record join mpl_coal_sample_details on " +
				"(tp_record.tpr_id= mpl_coal_sample_details.tpr_id) join mpl_lot_details on " +
				"(mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id)  " +
				"where sample_read=3 and mpl_coal_sample_details.sampling_done_time > ? and " +
				"mpl_coal_sample_details.sampling_done_time < ? and tp_record.transporter_id=? and" +
				" tp_record.supplier_id=?  and tp_record.mines_id=? and " +
				"tp_record.material_grade_id=? group by tp_record.transporter_id , " +
				"tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id ";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			ps.setInt(3, transporterId);
			ps.setInt(4, supplierId);
			ps.setInt(5, minesId);
			ps.setInt(6, gradeId);
			System.out.println("getSampleReadCount(): " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				 count = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	return count;

	
	}

	private static int getSamplingDoneCounts(Connection conn, int transporterId, int supplierId, int minesId,int gradeId) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;		
		String query = "Select count(*) countSubLots from tp_record join mpl_coal_sample_details on " +
				"(tp_record.tpr_id= mpl_coal_sample_details.tpr_id) join mpl_lot_details on " +
				"(mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id)  " +
				"where mpl_coal_sample_details.sampling_done_time > ? and " +
				"mpl_coal_sample_details.sampling_done_time < ? and tp_record.transporter_id=? and" +
				" tp_record.supplier_id=?  and tp_record.mines_id=? and " +
				"tp_record.material_grade_id=? group by tp_record.transporter_id , " +
				"tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id ";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			ps.setInt(3, transporterId);
			ps.setInt(4, supplierId);
			ps.setInt(5, minesId);
			ps.setInt(6, gradeId);
			System.out.println("getSampleReadCount(): " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				 count = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	return count;

	}

	private static void insertPostLotList(Connection conn,ArrayList<Pair<Integer, String>> list, String str, boolean isAuto) {
		HashMap<String, Integer> pairStrLotId = new HashMap<String, Integer>();

		if (list == null || list.size() == 0)
			return;

		for (int i = 0; i < list.size(); i++) {
			int postLotId = Misc.getUndefInt();
			Pair<Integer, String> pairVal = list.get(i);
			int lotId = pairVal.first;

			if (!pairStrLotId.containsKey(pairVal.second)) {
				postLotId = insertPostLotData(conn, "mpl_post_lot_details",isAuto);
				pairStrLotId.put(pairVal.second, postLotId);
			} else {
				postLotId = pairStrLotId.get(pairVal.second);
			}
			updateCoalSampleForPostLot(conn, lotId);
			updateLotDetails(conn, lotId, postLotId);
			updateLotNumber(conn, lotId, 0, "mpl_lot_number");
		}
	}

	private static void updateLotNumber(Connection conn, int mplLotId,int isFree, String tableName) {
		String sql = "update "+ tableName + " set latest_assigned_id = ? , is_free=?, updated_on=now() where latest_assigned_id = ?";
		PreparedStatement ps = null;
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, null, colPos++);
			Misc.setParamInt(ps, isFree, colPos++);
			Misc.setParamInt(ps, mplLotId, colPos++);
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void updateLotDetails(Connection conn, int mplLotId,
			int postLotId) {
		PreparedStatement ps = null;
		String sql = "Update mpl_lot_details set status=?, mpl_post_lot_id=?, updated_on=? where id=?";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, 2, colPos++);
			Misc.setParamInt(ps, postLotId, colPos++);
			ps.setTimestamp(colPos++, new Timestamp(System.currentTimeMillis()));
			Misc.setParamInt(ps, mplLotId, colPos++);
			System.out.println("RFIDSampleSysncMaster: updateLotDetails() "+ ps.toString());
			ps.executeUpdate();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close(); // close result set
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void updateCoalSampleForPostLot(Connection conn, int mplLotId) {
		PreparedStatement ps = null;
		String sql = "Update mpl_coal_sample_details set sample_read=4, updated_on=? where mpl_lot_sample_id = ?";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			ps.setTimestamp(colPos++, new Timestamp(System.currentTimeMillis()));
			Misc.setParamInt(ps, mplLotId, colPos++);
			ps.executeUpdate();
			System.out.println("RFIDSampleSysncMaster : updateCoalSampleForPostLot() "+ ps.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static int insertPostLotData(Connection conn, String tableName,boolean isAuto) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int status = 1;
		boolean isInserted = false;
		String sql = "Insert into " + tableName	+ " (name,created_on,status,port_node_id,is_auto) values (?, ?, ?, ?, ?)";
		
		
		int generatedVal = Misc.getUndefInt();
		try {
			String lotNumber = getLotNumber(conn, "lot_" ,"mpl_post_lot_number");
			ps = conn.prepareStatement(sql);
			ps.setString(1, lotNumber);
			ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			ps.setInt(3, status);
			ps.setInt(4, 463);
			ps.setInt(5, isAuto ? isAutoLot : isManualLot);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				generatedVal = rs.getInt(1);
				isInserted = true;
			}

			if (isInserted)
				updateLotNumber(conn, generatedVal, lotNumber,"mpl_post_lot_number");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return generatedVal;
	}

	private static String getCurrentDateString(boolean isManual) {
		SimpleDateFormat dateFormat = isManual ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") : new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String currentDate = dateFormat.format(new Date());
        return currentDate;
	}
	private static String getPreviousDateString(boolean isManual) {
		SimpleDateFormat dateFormat = isManual ? new SimpleDateFormat("yyyy-MM-dd 23:59:59") : new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Date dateBefore = new Date(new Date().getTime() - 1 * 24 * 3600 * 1000);
		String currentDate = dateFormat.format(dateBefore);
        return currentDate;
	}
	private static String getSamplingDoneTime(String samplingDoneTime, int startEndDate) {
		String newDate = "";
		String splitDate[] = (samplingDoneTime != null && samplingDoneTime.length()!=0) ?  samplingDoneTime.split(" ") : null;
		if(splitDate!=null){
			 newDate = startEndDate == 0 ? splitDate[0]+" 00:00:00" : splitDate[0]+" 23:59:59";
		}
		return newDate;
	}
	
	
	
	
	public static StringBuilder getPostLotName(Connection conn, String epc)
	throws SQLException {// for use
		StringBuilder retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// String query =
		// "Select distinct mpl_lot_number.lot_name,mpl_coal_sample_details.mpl_lot_sample_id from mpl_coal_sample_details join mpl_lot_details  on (mpl_coal_sample_details.mpl_lot_sample_id=mpl_lot_details.id) join mpl_lot_number on (mpl_lot_details.id= mpl_lot_number.latest_assigned_id) where mpl_coal_sample_details.is_latest=2 and mpl_coal_sample_details.sample_read=2 and mpl_lot_details.status=1 and mpl_coal_sample_details.mpl_lot_sample_id is not null group by mpl_coal_sample_details.mpl_lot_sample_id";
//		String query = "Select mpl_post_lot_details.name from mpl_coal_sample_details join mpl_lot_details on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id) join mpl_post_lot_details on (mpl_post_lot_details.id = mpl_lot_details.mpl_post_lot_id ) where mpl_post_lot_details.epc_code is not null  and  mpl_coal_sample_details.sample_read=5 and mpl_lot_details.status=2 and mpl_post_lot_details.epc_code=? limit 1";
		String query = "Select mpl_post_lot_details.name from mpl_coal_sample_details join mpl_lot_details on (mpl_coal_sample_details.mpl_lot_sample_id = mpl_lot_details.id) join mpl_post_lot_details on (mpl_post_lot_details.id = mpl_lot_details.mpl_post_lot_id ) where   mpl_post_lot_details.tag_issue_time is null and mpl_post_lot_details.status=1 and mpl_lot_details.mpl_post_lot_id is not null and mpl_lot_details.epc_code=? order by mpl_lot_details.id desc limit 1";
		try {// mines_details
			ps = conn.prepareStatement(query);
			ps.setString(1, epc);
			System.out.println("getLotName: " + ps.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null)
					retval = new StringBuilder();
					retval.append(rs.getString(1));
			}
		} catch (Exception ex) {
			retval = null;
			ex.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		}
		return retval;
}

	
}

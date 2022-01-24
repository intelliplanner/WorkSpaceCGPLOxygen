package com.ipssi.coalSampling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.processor.Utils;

public class RFIDSampleCache {
	public static int isAutoLot = 1;
	public static int isManualLot = 2;

	public static void main(String args[]) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			// RFIDSampleSyncMaster.setSampleStatus(conn,
			// "E2001026780800600860BF02");
//			RFIDSampleSyncMaster.autoGeneratePostLots(conn);
//			String s = getPreviousDateString(false);
//			String ss =  getPreviousDateString(true);
//			System.out.println(s + " , "+ ss);
//			scheduleAutoGeneratePostLots(conn,true);//
//			autoGeneratePostLots(conn,true);//
			autoGeneratePostLots(conn,false);
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


	private static void updateCoalSampleData(Connection conn, int supplierId,
			int transporterId, int minesId, int gradeId, int generatedId,String samplingDoneTime) {
		PreparedStatement ps = null;
		int sampleRead = 2;
		int isLatest = 2;
		String sql = "Update mpl_coal_sample_details join tp_record on (mpl_coal_sample_details.tpr_id= tp_record.tpr_id) set mpl_coal_sample_details.tag_issued=1, mpl_coal_sample_details.mpl_lot_sample_id=?, mpl_coal_sample_details.sample_read= ?,mpl_coal_sample_details.is_latest= ? where  mpl_coal_sample_details.sample_read=1 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ?  and tp_record.supplier_id=? and tp_record.transporter_id=? and tp_record.mines_id=? and tp_record.material_grade_id=? ";
		try {
			int colPos = 1;
			ps = conn.prepareStatement(sql);
			Misc.setParamInt(ps, generatedId, colPos++);
			Misc.setParamInt(ps, sampleRead, colPos++);
			Misc.setParamInt(ps, isLatest, colPos++);
			ps.setString(colPos++, getSamplingDoneTime(samplingDoneTime,0));
			ps.setString(colPos++, getSamplingDoneTime(samplingDoneTime,1));
			Misc.setParamInt(ps, supplierId, colPos++);
			Misc.setParamInt(ps, transporterId, colPos++);
			Misc.setParamInt(ps, minesId, colPos++);
			Misc.setParamInt(ps, gradeId, colPos++);
			System.out.println("UPDATE MPL_COAL_SAMPLE_DETAIL" + ps.toString());
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

	private static String getSamplingDoneTime(String samplingDoneTime, int startEndDate) {
		String newDate = "";
		String splitDate[] = (samplingDoneTime != null && samplingDoneTime.length()!=0) ?  samplingDoneTime.split(" ") : null;
		if(splitDate!=null){
			 newDate = startEndDate == 0 ? splitDate[0]+" 00:00:00" : splitDate[0]+" 23:59:59";
		}
		return newDate;
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
				e.printStackTrace();
			}
		}
		return lotName;
	}

	private static String updateLotNumber(Connection conn, int lotSampleId,
			String lotName, String tableName) {
		String sql = "update " + tableName	+ " set latest_assigned_id = ? , is_free=? where lot_name = ?";
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

	public static void autoGenerateSubLots(Connection conn,boolean isAuto) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		String lotNamePrefix = "sub_lot_";
		String query = "Select  tp_record.supplier_id ,tp_record.transporter_id , tp_record.mines_id ,  tp_record.material_grade_id,count(id) from tp_record join mpl_coal_sample_details on (tp_record.tpr_id= mpl_coal_sample_details.tpr_id) where  sample_read=1 and sampling_done_time > ? and sampling_done_time < ? and mpl_lot_sample_id is null and tp_record.transporter_id is not null and tp_record.supplier_id is not null and tp_record.mines_id is not null and  tp_record.material_grade_id is not null group by tp_record.transporter_id , tp_record.supplier_id  , tp_record.mines_id ,  tp_record.material_grade_id";
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, getPreviousDateString(false));
			ps.setString(2, getPreviousDateString(true));
			System.out.println("auto genearate Sub lots: " + ps.toString());
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
			query += " and sample_read=3 and mpl_lot_details.tag_issue_time > ? and mpl_lot_details.tag_issue_time < ? ";
		}else{
			query += " and sample_read=3 and mpl_coal_sample_details.sampling_done_time > ? and mpl_coal_sample_details.sampling_done_time < ? ";
		}

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

	
	private static String getPreviousDateString(boolean isManual) {
		SimpleDateFormat dateFormat = isManual ? new SimpleDateFormat("yyyy-MM-dd 23:59:59") : new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Date dateBefore = new Date(new Date().getTime() - 1 * 24 * 3600 * 1000);
		String currentDate = dateFormat.format(dateBefore);
        return currentDate;
	}
	
	
	
}

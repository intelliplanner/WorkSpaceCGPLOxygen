/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.database;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.ComboItemList;
//import com.ipssi.rfid.connection.Log_File;
import com.ipssi.rfid.processor.TPRUtils;
import com.ipssi.rfid.processor.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JComboBox;

/**
 *
 * @author Vi$ky
 */
public class DropDownValues {

	public static ArrayList<ComboItem> getGradeList(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> gradeList = new ArrayList<ComboItem>();
		String query = "SELECT id, name FROM grade_details where status=1";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				gradeList.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
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
		return gradeList;
	}

	public static String getGrade(int gradeId, Connection conn) {
		String grade_name = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = " SELECT name FROM grade_details where status=1 and id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, gradeId);
			rs = ps.executeQuery();
			while (rs.next()) {
				grade_name = rs.getString(1);
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
		return grade_name;
	}

	public static String getMines(int minesId, Connection conn) {
		String mines_name = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = " SELECT name FROM mines_details where status=1 and id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, minesId);
			rs = ps.executeQuery();
			while (rs.next()) {
				mines_name = rs.getString(1);
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
		return mines_name;
	}

	public static ArrayList<ComboItem> getMinesList(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> minesList = new ArrayList<ComboItem>();
		String query = "SELECT id,name FROM mines_details where status=1";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				minesList.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
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
		return minesList;
	}

	public static ArrayList<ComboItem> getTransporterList(Connection conn) {
		return getTransporterList(conn, Misc.getUndefInt(), Misc.getUndefInt());
	}

	public static ArrayList<ComboItem> getTransporterList(Connection conn, int materialCode) {
		return getTransporterList(conn, materialCode, Misc.getUndefInt());
	}

	public static ArrayList<ComboItem> getTransporterList(Connection conn, int materialCode, int doId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> transporter = new ArrayList<ComboItem>();
		String query = Misc.isUndef(doId)
				? " SELECT transporter_details.id, transporter_details.name FROM transporter_details "
				: " Select distinct(transporter_details.id) id, transporter_details.name  from transporter_details ";
		String where = " where transporter_details.status=1 ";
		if (!Misc.isUndef(materialCode))
			where += "and  material_cat = ? ";
		boolean useJoin = !Misc.isUndef(doId) && !isTempDo(conn, doId, false);
		if (useJoin) {
			query += " inner join do_grade_transporter on (transporter_details.id = do_grade_transporter.transporter_id) ";
			where += " and do_id = ?";
		}
		try {
			query += where;
			ps = conn.prepareStatement(query);
			int paramIndex = 1;
			if (!Misc.isUndef(materialCode))
				ps.setInt(paramIndex++, materialCode);
			if (useJoin)
				ps.setInt(paramIndex++, doId);
			rs = ps.executeQuery();
			while (rs.next()) {
				transporter.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
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
		return transporter;
	}

	public static String getTransporter(int transporterId, Connection conn) {
		String transporter_name = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "SELECT name FROM transporter_details where id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, transporterId);
			rs = ps.executeQuery();
			while (rs.next()) {
				transporter_name = Misc.getPrintableString(rs.getString(1));
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
		return transporter_name;
	}

	public static String getTransporterForSlip(int transporterId, Connection conn) {
		String transporter_name = "";
		String transporter_address = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "SELECT full_name, supervisor_address FROM transporter_details where id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, transporterId);
			rs = ps.executeQuery();
			while (rs.next()) {
				transporter_name = rs.getString(1) == null ? "" : rs.getString(1);
				if (!Utils.isNull(transporter_name))
					transporter_name += ", ";

				transporter_address = rs.getString(2) == null ? "" : rs.getString(2);
			}
			transporter_name += transporter_address;
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
		return transporter_name;
	}

	public static ArrayList<ComboItem> getDoRrList(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> doRr = new ArrayList<ComboItem>();
		String query = " SELECT id, do_rr_number FROM do_rr_details where status=1";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				doRr.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
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
		return doRr;
	}

	public static String getDoRr(int doId) {
		String doRrNo = "";
		Connection invConn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// ArrayList<String> transporter = new ArrayList<String>();
		String query = "SELECT do_rr_number FROM do_rr_details where status=1 and id=?";
		try {
			invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = invConn.prepareStatement(query);
			ps.setInt(1, doId);
			rs = ps.executeQuery();
			while (rs.next()) {
				doRrNo = rs.getString(1);
			}
		} catch (Exception ex) {
			destroyIt = true;
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
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(invConn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return doRrNo;
	}

	public static int getDoId(String doRr) {
		int doNo = Misc.getUndefInt();
		Connection invConn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// ArrayList<String> transporter = new ArrayList<String>();
		String query = "SELECT id FROM do_rr_details where status=1 and do_rr_number = ?";
		try {
			invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = invConn.prepareStatement(query);
			ps.setString(1, doRr);
			rs = ps.executeQuery();
			while (rs.next()) {
				doNo = rs.getInt(1);
			}
		} catch (Exception ex) {
			destroyIt = true;
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
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(invConn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return doNo;
	}

	public static String getQcStatus(int tprId) {
		String qcStat = "No";
		int qcVal = Misc.getUndefInt();
		String doRrNo = "";
		Connection invConn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		// ArrayList<String> transporter = new ArrayList<String>();
		String query = "SELECT qc_status FROM tpr_qc_detail WHERE status=1 and tpr_id = ?";
		try {
			invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = invConn.prepareStatement(query);
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			while (rs.next()) {
				qcVal = rs.getInt(1);
			}
		} catch (Exception ex) {
			destroyIt = true;
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
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(invConn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (qcVal == 0) {
			qcStat = "Yes";
		} else if (qcVal == 2) {
			qcStat = "Not Needed";
		} else {
			qcStat = "No";
		}
		return qcStat;
	}

	public static String getSupplierName(int supplierId) {
		String supplier_name = "";
		Connection invConn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = " SELECT  name FROM supplier_details WHERE status=1 and id = ? ";
		try {
			invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = invConn.prepareStatement(query);
			ps.setInt(1, supplierId);
			rs = ps.executeQuery();
			while (rs.next()) {
				supplier_name = rs.getString(1);
			}

		} catch (Exception ex) {
			destroyIt = true;
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
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(invConn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return supplier_name;
	}

	public static String getWorkStationName(int supplierId) {
		String workstation_name = "";
		Connection invConn = null;
		boolean destroyIt = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = " SELECT  workstation_name FROM workstation_details WHERE status=1 and workstation_id = ? ";
		try {
			invConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = invConn.prepareStatement(query);
			ps.setInt(1, supplierId);
			rs = ps.executeQuery();
			while (rs.next()) {
				workstation_name = rs.getString(1);
			}

		} catch (Exception ex) {
			destroyIt = true;
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
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(invConn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return workstation_name;
	}

	public static ArrayList<ComboItem> getBedList(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> bedList = new ArrayList<ComboItem>();
		String query = "SELECT bed_details.id, bed_details.name FROM status=1 and bed_details";
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				bedList.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
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
		return bedList;
	}

	public static boolean isTempDo(Connection conn, int doId, boolean isGrade) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select 1 from do_grade_transporter where do_id=? and "
					+ (isGrade ? "grade_id > 0" : "transporter_id > 0"));
			ps.setInt(1, doId);
			rs = ps.executeQuery();
			while (rs.next()) {
				return false;
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
		return true;
	}

	public static ArrayList<Pair> getGradeList(Connection conn, int selectedDo) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Integer, String> pairVal = null;
		ArrayList<Pair> gradeList = new ArrayList<Pair>();
		String query = "Select distinct(grade_details.id), grade_details.name  from grade_details";
		String where = " where grade_details.status=1 ";
		boolean isTempDo = isTempDo(conn, selectedDo, true);
		if (!Misc.isUndef(selectedDo) && !isTempDo) {
			query += " inner join do_grade_transporter on (grade_details.id = do_grade_transporter.grade_id)  ";
			query += "  and do_grade_transporter.do_id = ?";
		}
		try {
			query += where;
			ps = conn.prepareStatement(query);
			if (!Misc.isUndef(selectedDo) && !isTempDo) {
				ps.setInt(1, selectedDo);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				pairVal = new Pair<Integer, String>(rs.getInt(1), rs.getString(2));
				gradeList.add(pairVal);
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
		return gradeList;
	}

	public static ArrayList<Pair> getDoRrNumber(Connection conn, int minesId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Pair<Integer, String> pairVal = null;
		ArrayList<Pair> doRrList = new ArrayList<Pair>();
		String query = " SELECT id, (case when do_rr_number is not null and do_rr_number not like \"\" then do_rr_number else concat(\"MPL-\",id) end) do_number FROM do_rr_details where status=1 and mines_id = ?";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, minesId);
			rs = ps.executeQuery();
			System.out.println("DO RR: " + ps.toString());
			while (rs.next()) {
				pairVal = new Pair<Integer, String>(rs.getInt("id"), rs.getString("do_number"));
				doRrList.add(pairVal);
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
		return doRrList;
	}

	public static boolean isNull(javax.swing.JComboBox combo) {
		return (combo == null || combo.getSelectedItem() == null
				|| !combo.getSelectedItem().getClass().isAssignableFrom(ComboItem.class)
				|| combo.toString().equalsIgnoreCase("select"));
	}

	public static void setComboItem(javax.swing.JComboBox combo, int value) {
		if (combo == null || combo.getItemCount() == 0) {
			return;
		}
		if (Misc.isUndef(value)) {
			combo.setSelectedIndex(0);
			return;
		}
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (combo.getItemAt(i).toString().equalsIgnoreCase("select")) {
				continue;
			}
			ComboItem comboItem = (ComboItem) combo.getItemAt(i);
			if (comboItem != null && comboItem.getValue() == value) {
				combo.setSelectedIndex(i);
				break;
			}
		}
	}

	public static void setComboItemList(javax.swing.JComboBox combo, int value) {
		if (combo == null || combo.getItemCount() == 0) {
			return;
		}
		if (Misc.isUndef(value)) {
			combo.setSelectedIndex(0);
			return;
		}
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (combo.getItemAt(i).toString().equalsIgnoreCase("select")) {
				continue;
			}
			ComboItemList comboItem = (ComboItemList) combo.getItemAt(i);
			if (comboItem != null && comboItem.getValue() == value) {
				combo.setSelectedIndex(i);
				break;
			}
		}
	}

	public static int getComboSelectedVal(javax.swing.JComboBox combo) {
		return !isNull(combo) ? ((ComboItem) combo.getSelectedItem()).getValue() : Misc.getUndefInt();
	}

	public static String getComboSelectedText(javax.swing.JComboBox combo) {
		// TODO Auto-generated method stub
		return !isNull(combo) ? ((ComboItem) combo.getSelectedItem()).getLabel() : null;
	}

	public static int getComboListSelectedVal(javax.swing.JComboBox combo) {
		return !isNull(combo) ? ((ComboItemList) combo.getSelectedItem()).getValue() : Misc.getUndefInt();
	}

	public static String getComboListSelectedText(javax.swing.JComboBox combo) {
		return !isNull(combo) ? ((ComboItemList) combo.getSelectedItem()).getLabel() : null;
	}

	public static String getComboListSelectedAddress(javax.swing.JComboBox combo) {
		// TODO Auto-generated method stub
		return !isNull(combo) ? ((ComboItemList) combo.getSelectedItem()).getAddress() : null;
	}

	public static void setTransporterList(JComboBox combo, Connection conn, int materialCode) {
		combo.removeAllItems();
		combo.addItem(new ComboItem(Misc.getUndefInt(), "Select"));
		ArrayList<ComboItem> transporterList = DropDownValues.getTransporterList(conn, materialCode);
		for (int i = 0; i < transporterList.size(); i++) {
			combo.addItem(transporterList.get(i));
		}
		combo.setSelectedIndex(0);
	}

	public static void setConsignList(JComboBox combo, Connection conn, int consign) {
		combo.removeAllItems();
		combo.addItem(new ComboItemList(Misc.getUndefInt(), "Select", ""));
		combo.getItemCount();
		ArrayList<ComboItemList> consignList = DropDownValues.getConsignList(conn, consign);
		for (int i = 0; i < consignList.size(); i++) {
			combo.addItem(consignList.get(i));
			combo.setSelectedIndex(0);
		}
	}

	private static ArrayList<ComboItemList> getConsignList(Connection conn, int consign) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItemList> consignee = new ArrayList<ComboItemList>();
		String Where = "";
		String query = " SELECT DISTINCT vendor_details.id, vendor_details.name, vendor_details.address FROM port_nodes sel JOIN port_nodes anc ON \n "
				+ " (anc.lhs_number = sel.lhs_number AND anc.rhs_number = sel.rhs_number) JOIN port_nodes leaf ON \n"
				+ " (sel.lhs_number = leaf.lhs_number AND sel.rhs_number = leaf.rhs_number) JOIN vendor_details ON \n"
				+ " (vendor_details.port_node_id = anc.id OR vendor_details.port_node_id = leaf.id) ";
		if (consign == 0)// consignor
			Where = " WHERE sel.id = 463 AND vendor_details.status=1 AND vendor_details.consignor=1";
		else
			Where = " WHERE sel.id = 463 AND vendor_details.status=1 AND vendor_details.consignee=1";

		query = query + Where;

		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				consignee.add(new ComboItemList(Misc.getRsetInt(rs, 1), rs.getString(2), rs.getString(3)));
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
		return consignee;
	}

	public static Pair<Integer, Integer> getMaterialCode(int transporterId) {
		int material_Code = Misc.getUndefInt();
		int id = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "SELECT id, material_code FROM transporter_details where id = ?";
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			ps.setInt(1, transporterId);
			rs = ps.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
				material_Code = rs.getInt(2);
			}

		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
		return new Pair<Integer, Integer>(id, material_Code);
	}

	public static void setBedList(JComboBox combo, int transporterId) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ArrayList<Pair<Integer, String>> bedList = TPRUtils.getBedList(conn, transporterId, Misc.getUndefInt(),
					Misc.getUndefInt(), Misc.getUndefInt());// TPRUtils.getBedList(conn, tprRecord != null ?
															// tprRecord.getTransporterId() : Misc.getUndefInt(),
															// tprRecord != null ? tprRecord.getMinesId() :
															// Misc.getUndefInt(), tprRecord != null ?
															// tprRecord.getMaterialGradeId() : Misc.getUndefInt());
			for (int i = 0, is = bedList == null ? 0 : bedList.size(); i < is; i++) {
				combo.addItem(new ComboItem(bedList.get(i).first, bedList.get(i).second));
				combo.setSelectedIndex(0);
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// public static void setConsignorList(JComboBox combo, Connection conn) {
	// ArrayList<ComboItem> consignorList = DropDownValues.getConsignorList(conn);
	// for (int i = 0; i < consignorList.size(); i++) {
	// combo.addItem(consignorList.get(i));
	// combo.setSelectedIndex(0);
	// }
	// }

	// private static ArrayList<ComboItem> getConsignorList(Connection conn) {
	// PreparedStatement ps = null;
	// ResultSet rs = null;
	// ArrayList<ComboItem> consignor = new ArrayList<ComboItem>();
	// String query = " SELECT id, name FROM consignor_details";
	// try {
	// ps = conn.prepareStatement(query);
	// rs = ps.executeQuery();
	// while (rs.next()) {
	// consignor.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
	// }
	// } catch (Exception ex) {
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
	// }
	// return consignor;
	// }
	//
	public static String getUser(Connection conn, int tprId, int workStationType) {
		String user = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = " select users.name from tp_step left outer join users on (users.id = tp_step.user_by) where save_status=0 and tpr_id=? and work_station_type=? order by out_time limit 1";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, tprId);
			ps.setInt(2, workStationType);
			rs = ps.executeQuery();
			while (rs.next()) {
				user = rs.getString(1);
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
		return user;
	}

	public static void main(String[] arg) throws GenericException {
		boolean destroyIt = false;
		Connection conn = null;
		Pair<Integer, String> vehPair = null;
		int vehicleId = Misc.getUndefInt();
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			// ArrayList<ComboItem> t1 = getTransporterList(conn);
			//// ArrayList<ComboItem> t2 = getTransporterList(conn,0);
			// ArrayList<ComboItem> t3 = getTransporterList(conn,0,Misc.getUndefInt());
			// ArrayList<ComboItem> s2 = getMinesList(conn);
			// String s1 = getTransporter(1,conn);
			String s = getTransporterForSlip(1, conn);
			System.out.println(s);
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {

			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}

	public static void setMaterialSubCategory(JComboBox combo, Connection conn, int materialCat) {
		combo.removeAllItems();
		combo.addItem(new ComboItem(Misc.getUndefInt(), "Select"));
		ArrayList<ComboItem> materialSubCatList = DropDownValues.getMaterialCategoryList(conn, materialCat);
		for (int i = 0; i < materialSubCatList.size(); i++) {
			combo.addItem(materialSubCatList.get(i));
		}
		combo.setSelectedIndex(0);
	}

	private static ArrayList<ComboItem> getMaterialCategoryList(Connection conn, int materialCat) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> materialList = new ArrayList<ComboItem>();
		String query = "SELECT id, name FROM material_sub_cat WHERE status = 1 AND port_node_id = 463 AND material_cat = ? ";
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, materialCat);
			rs = ps.executeQuery();
			while (rs.next()) {
				materialList.add(new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2)));
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
		return materialList;
	}

	public static long getDifferenceBwDate(Date inTime) {
		System.out.print("Current Time: " + new Date().getTime());
		long diffDays = 100;
		if (inTime != null) {
			long diff = new Date().getTime() - inTime.getTime();
			diffDays = diff / (24 * 60 * 60 * 1000);
		}
		return diffDays;
	}

}

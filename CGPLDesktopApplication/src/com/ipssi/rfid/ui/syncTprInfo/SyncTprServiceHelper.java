/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.syncTprInfo;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.constant.UIConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author IPSSI
 */
public class SyncTprServiceHelper {

	public static void getData(Connection conn, SyncTprServiceHandler handler) {
		int runningTpr = getOpenTprStatus(conn);
		int completedTpr = getCompletedTprStatus(conn);
		if (handler != null) {
			handler.init(runningTpr, completedTpr);
		}
	}

	public static int getOpenTprStatus(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int retval = Misc.getUndefInt();
		
		try {
			String fromDate = UIConstant.dateFormat.format(new Date()) +" 00:00:00";
		    String toDate = UIConstant.dateFormat.format(new Date()) +" 23:59:59";
			String query = "select count(tpr_id) from tp_record where tpr_status in (0,1) and tpr_create_date between '"+ fromDate + "' and '" + toDate +"'";
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = Misc.getRsetInt(rs, 1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();

			} catch (SQLException ex) {
				Logger.getLogger(SyncTprServiceHelper.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return retval;
	}

	public static int getCompletedTprStatus(Connection conn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int retval = Misc.getUndefInt();
		try {
			String fromDate = UIConstant.dateFormat.format(new Date())+" 00:00:00";
		    String toDate = UIConstant.dateFormat.format(new Date())+" 23:59:59";
			String query = "select count(tpr_id) from tp_record where tpr_status in (2) and tpr_create_date between '"+ fromDate + "' and '" + toDate +"'";
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = Misc.getRsetInt(rs, 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();

			} catch (SQLException ex) {
				Logger.getLogger(SyncTprServiceHelper.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return retval;
	}
	
	
	public static void main(String s[]) {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();

		} catch (Exception ex) {
			 Logger.getLogger(SyncTprServiceHelper.class.getName()).log(Level.SEVERE,
			 null, ex);
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (GenericException e) {
				e.printStackTrace();
			}
		}

	}

}

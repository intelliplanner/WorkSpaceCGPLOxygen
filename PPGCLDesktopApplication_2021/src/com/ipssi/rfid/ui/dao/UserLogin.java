/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.User;

/**
 *
 * @author IPSSI
 */
public class UserLogin {
	public static User Login(Connection conn, String username, String password) throws GenericException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		User retval = null;
		try {
			ps = conn.prepareStatement("select id,name from users where username=? and password=? and isactive=1");
			ps.setString(1, username);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = new User(Misc.getRsetInt(rs, 1), username, rs.getString(2), false, true);
				retval.setSupperUser(isSuperUser(conn, retval.getId()));
				retval.setPrivList(getPrivList(conn, retval.getId()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}

	public static Pair<Integer, String> Login(Connection conn, String username1, char[] password1)
			throws GenericException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		int userId = Misc.getUndefInt();
		String name = null;
		try {
			ps = conn.prepareStatement("select id,name from users where username=? and password=? and isactive=1");
			ps.setString(1, username1);
			ps.setString(2, new String(password1));
			rs = ps.executeQuery();
			if (rs.next()) {
				userId = rs.getInt(1);
				name = rs.getString(2);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return new Pair<Integer, String>(userId, name);
	}

	public static ArrayList<Integer> getPrivList(Connection conn, int userId) {
		ArrayList<Integer> retval = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// check if it is super user
			ps = conn.prepareStatement(
					"select role_privs.priv_id from users join user_roles on (user_roles.user_1_id=users.id)  join role_privs on (user_roles.role_id = role_privs.role_id) where users.id=?");
			Misc.setParamInt(ps, userId, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (retval == null)
					retval = new ArrayList<Integer>();
				retval.add(Misc.getRsetInt(rs, 1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return retval;
	}

	public static boolean isSuperUser(Connection conn, int userId) {
		boolean retval = userId == 1 ? true : false;
		return retval;
	}

}

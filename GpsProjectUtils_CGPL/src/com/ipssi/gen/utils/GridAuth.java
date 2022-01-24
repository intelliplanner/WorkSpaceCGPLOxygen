package com.ipssi.gen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class GridAuth {
	private static String g_allowGridAuth = "grid_auth";
	private static String gridCode[] = {"51","01","51","53","15"
		                                                           ,"13","24","79","23","78"
		                                                           ,"22","77","20","71","94"
		                                                           ,"57","05","52","47","80"
		                                                           ,"57","40","48","69","03"
		                                                           ,"06","19","93","08","59"
		                                                           ,"74","94","48","59","52"
		                                                           ,"81","50","09","95","50"
		                                                           };
	private static Random randomGen = new Random();
	public static Triple<Character, Character, Character> gridAsk() {
		int a = 'A';
		char c1 = (char)((int)(randomGen.nextDouble()*26)+a);
		char c2 = (char)((int)(randomGen.nextDouble()*26)+a);
		char c3 = (char)((int)(randomGen.nextDouble()*26)+a);
		return new Triple<Character, Character, Character>(c1,c2,c3);
	}
	
	public static Triple<Boolean, String, String> validate(Connection conn, String authUser, int currentUser, char c1, char c2, char c3, String r1, String r2, String r3, boolean exclude0user) throws Exception {
		//check if authUser exists
		//if yes - does if the user have g_allowGridAuth
		//then check if the grid password matches
		//if succ then returns true, user, password for further processing by handlelogin
		// else returns false, reasonText and null
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean retval = false;
		String reasonText = null;
		String passwordOfAuthUser = null;
		try {
			Cache cache = Cache.getCacheInstance(conn);
			 PrivInfo.TagInfo tagInfo = g_allowGridAuth == null ? null : cache.getPrivId(g_allowGridAuth);
			 
			int privId =tagInfo == null ? Misc.getUndefInt() : tagInfo.m_read;
			ps = conn.prepareStatement("select users.id, users.password, user_roles.role_id, priv_id from users left outer join user_roles on (user_roles.user_1_id = users.id) left outer join role_privs on (role_privs.role_id = user_roles.role_id and priv_id = ?) where users.username=? and users.isactive=1 order by user_roles.role_id, priv_id ");
			ps.setInt(1, privId);
			ps.setString(2, authUser);
			rs = ps.executeQuery();
			int userId = Misc.getUndefInt();
			if (rs.next()) {
				userId = Misc.getRsetInt(rs, 1);
				passwordOfAuthUser = rs.getString(2);
				int roleId = Misc.getRsetInt(rs, 3);
				int privIdFound = Misc.getRsetInt(rs, 4);
				if (roleId == 1 || Misc.isUndef(privId) || privId == privIdFound) {
					retval = true;
				}
				else {
					reasonText = "User not allowed to do grid auth";
				}
			}
			else {
				reasonText = "User for grid auth does not exist";
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (retval) {
				//check if password matches
			//	int date = (new java.util.Date()).getDate();
				int date = 0;
				int c1idx = ((int)c1 - (int)'A' + date)%26;
				int c2idx = ((int)c2 - (int)'A' + date)%26;
				int c3idx = ((int)c3 - (int)'A' + date)%26;
				String c1pDesc = gridCode[c1idx];
				String c2pDesc = gridCode[c2idx];
				String c3pDesc = gridCode[c3idx];
				if (!c1pDesc.equals(r1) || !c2pDesc.equals(r2) || !c3pDesc.equals(r3)) {
					retval = false;
					reasonText = "Grid Code Does not match";
				}
			}
			//make a record ... and then
			//create table grid_auth_attemp(on_date timestamp null default null, grid_user varchar(64), grid_user_id int, logged_in_user int, result int ,reason_text varchar(64));
			ps = conn.prepareStatement("insert into grid_auth_attemp(on_date, grid_user, grid_user_id, logged_in_user, result, reason_text) values (now(), ?,?,?,?,?)");
			ps.setString(1, authUser);
			ps.setInt(2, userId);
			ps.setInt(3, currentUser);
			ps.setInt(4, retval ? 1 : 0);
			ps.setString(5, reasonText);
			ps.execute();
			ps = Misc.closePS(ps);
			if (retval)
				reasonText = authUser;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Triple<Boolean, String, String>(retval, reasonText, passwordOfAuthUser);
	}
}

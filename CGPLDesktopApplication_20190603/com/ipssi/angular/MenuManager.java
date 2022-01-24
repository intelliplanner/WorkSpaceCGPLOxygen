package com.ipssi.angular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.Gson;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.UserGen.Menu;

public class MenuManager {
	private static final Gson gson = new Gson();
	public static String getMenuJson(Menu menu){
		if(menu == null)
			return null;
		ArrayList<MenuAg> menuList = getMenuAg(menu);
		System.out.println(routes+",length="+routes.size());
		return gson.toJson(menuList);

	}
	public static ArrayList<MenuAg> getMenuAg(Menu menu){
		ArrayList<MenuAg> retval = null;
		for(int i=0,is=menu==null || menu.m_children == null ? 0 : menu.m_children.size();i<is;i++){
			Menu m1 = (Menu)menu.m_children.get(i);
			String route = getRoute(m1.m_menu.m_url);
			/*if(route == null || route.length() == 0)
				continue;*/
			routes.add(route);
			MenuAg m = new MenuAg();
			m.id = m1.m_menu.m_tag;
			m.title = m1.m_menu.m_sn;
			m.url = route;
			m.params = m1.m_menu.m_params;
//			m.icon = m1.m_menu.m_icon;
//			m.isChildTabbed = m1.m_menu.m_childTabbed;
			if(menu.m_children != null && menu.m_children.size() > 0){
				m.children = getMenuAg(m1);
				if(m.children != null && m.children.size() > 0){
					m.type="collapsable";
					if(m.icon == null || m.icon.length() ==0)
						m.icon = "apps";
				}else{
					if(m1.m_parent == null || m1.m_parent.m_menu == null || m1.m_parent.m_menu.m_tag == null || m1.m_parent.m_menu.m_tag.length() == 0 || m1.m_parent.m_menu.m_tag.equalsIgnoreCase("main"))
						m.icon = "layers";
				}
				if(retval == null)
					retval = new ArrayList<MenuManager.MenuAg>();
				retval.add(m);
			}


		}
		return retval;
	}
	static HashSet<String> routes = new HashSet<String>();
	private static String getRoute(String url) {
		if(url == null || url.length() == 0)
			return url;
		url = url.replaceAll(".jsp","");
		url = url.replaceAll(".do","");
		String[] urlPart = url.split("/");
		return urlPart == null || urlPart[urlPart.length -1] == null ? url : urlPart[urlPart.length -1];
	}
	public static class MenuAg{
		public String id;
		public String type="item";
		public String title;
		public String url;
		public String icon;
		public boolean isChildTabbed;
		public ArrayList params;
		public ArrayList<MenuAg> children; 
	}
	public static String GetUser(Connection conn, SessionManager _session, long userId , boolean isLoggedIn, boolean doMenu){
		JSONObject result = new JSONObject();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (!isLoggedIn) {
				result.putOpt("status", -1);
			} else {
				result.putOpt("status", 1);
				JSONObject userJson = new JSONObject();
				Menu menu = null;
				ps = conn.prepareStatement("select name,username,id,email from users where id=?");
				Misc.setParamLong(ps, userId, 1);
				rs = ps.executeQuery();
				while (rs.next()) {
					userJson.putOpt("name", rs.getString(1));
					userJson.putOpt("username", rs.getString(2));
					userJson.putOpt("id", Misc.getRsetInt(rs, 3));
					userJson.putOpt("email", rs.getString(4));
				}
				Misc.closeRS(rs);
				Misc.closePS(ps);
				ps = conn
						.prepareStatement("select user_preferences.name, user_preferences.value from user_preferences left join users on ( users.id = user_preferences.user_1_id ) where users.id=? and isactive=1");
				Misc.setParamLong(ps, userId, 1);
				rs = ps.executeQuery();
				while (rs.next()) {
					String key = rs.getString("name");
					String value = rs.getString("value");
					userJson.putOpt(key, value);
				}
				Misc.closeRS(rs);
				Misc.closePS(ps);
				ps = conn.prepareStatement("select role_privs.priv_id from users join user_roles on (user_roles.user_1_id=users.id)  join role_privs on (user_roles.role_id = role_privs.role_id) where users.id=?");
				Misc.setParamLong(ps, userId, 1);
				rs = ps.executeQuery();
				JSONArray privArray = null;
				while(rs.next()){
					privArray = new JSONArray();
					privArray.put(Misc.getRsetInt(rs, 1));
				}
				if(privArray != null)
					userJson.putOpt("privs", privArray);
				menu = _session.getMenu();
				if(doMenu)
					result.put("menu",MenuManager.getMenuJson(menu));
				result.put("user", userJson);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return result.toString();
	}
}

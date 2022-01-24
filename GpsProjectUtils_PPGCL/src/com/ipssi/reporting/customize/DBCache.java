package com.ipssi.reporting.customize;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.User;

public class DBCache {
	private static ConcurrentHashMap<String, DBConfig> g_byPortNode = new ConcurrentHashMap<String, DBConfig>(); //by String = id+tag
	private static ConcurrentHashMap<String, DBConfig> g_byUserId = new ConcurrentHashMap<String, DBConfig>(); //by String = id+tag
	private static ConcurrentHashMap<String, DBConfig> g_basic = new ConcurrentHashMap<String, DBConfig>(); //by String = id+tag
	
	public static DBConfig getExplicitConfig(Connection conn, int portNodeId, String tag) throws Exception {
		String key = portNodeId+"_"+tag;
		if (!g_byPortNode.containsKey(key)) {
			DBConfig retval = loadConfig(conn, portNodeId, Misc.getUndefInt(), tag);
			g_byPortNode.put(key, retval);
		}
		DBConfig temp = g_byPortNode.get(key);
		return temp;
	}
	
	public static DBConfig getConfig(Connection conn, int portNodeId, int userId, String tag, Cache cache, String file) throws Exception {
		try {
			DBConfig retval = null;
			//1st check for user ...if found to be explicitly null - then check portNodeId ... if found to be explicitly null - then check g_basic ... 
			//if not found to be explicitly null then read from DB
			if (!Misc.isUndef(userId)) {
				String key = userId+"_"+tag;
				if (!g_byUserId.containsKey(key)) {
					retval = loadConfig(conn, Misc.getUndefInt(), userId, tag);
					g_byUserId.put(key, retval);
				}
				retval = g_byUserId.get(key);
			}
			if (retval == null || retval.isEmpty()) {
				if (Misc.isUndef(portNodeId))
					portNodeId = Misc.G_TOP_LEVEL_PORT;
				for (MiscInner.PortInfo portInfo = cache.getPortInfo(portNodeId, conn); portInfo != null; portInfo = portInfo.m_parent) {
					String key = portInfo.m_id+"_"+tag;
					if (!g_byPortNode.containsKey(key)) {
						retval = loadConfig(conn, portInfo.m_id, Misc.getUndefInt(), tag);
						g_byPortNode.put(key, retval);
					}
					retval = g_byPortNode.get(key);
					if (retval != null && !retval.isEmpty())
						break;
				}//went up the org tree
			}//retval was null;
			if (retval == null || retval.isEmpty()) {
				//read it from file ..
				if (!g_basic.containsKey(tag)) {
					retval = DBCache.readConfig(file);
					DBCache.g_basic.put(tag, retval);
				}
				retval = g_basic.get(tag);
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static DBConfig loadConfig(Connection conn, int portNodeId, int userId, String tag) throws Exception {
		try {
			DBConfig retval = null;
			PreparedStatement ps = conn.prepareStatement("select db_layouts.id, row_number, col_number, row_span, col_span, title, component_id, item_tag, front_page, addnl_parameter from db_layouts join db_layout_items on (db_layouts.id = db_layout_items.db_layout_id) where (? is null or port_node_id=?) and (? is null or user_id=?) and tag = ? order by db_layouts.id, row_number, col_number");
			Misc.setParamInt(ps, portNodeId,1);
			Misc.setParamInt(ps, portNodeId,2);
			Misc.setParamInt(ps, userId,3);
			Misc.setParamInt(ps, userId,4);
			ps.setString(5, tag);
			ResultSet rs = ps.executeQuery();
			ArrayList<DBItem> addTo = null;
			while (rs.next()) {
				int layOutId = rs.getInt(1);
				
				DBItem item = new DBItem(Misc.getRsetInt(rs, "row_number"), Misc.getRsetInt(rs, "col_number"), Misc.getRsetInt(rs, "row_span"), Misc.getRsetInt(rs, "col_span"), rs.getString("title"), rs.getString("item_tag"), rs.getString("front_page"), rs.getInt("component_id"), rs.getString("addnl_parameter"));
				if (retval == null) {
					retval = new DBConfig();
					retval.setId(layOutId);
					retval.setForPortNodeId(portNodeId);
					retval.setForUserId(userId);
					addTo = new ArrayList<DBItem>();
					retval.setDbItems(addTo);
				}
				addTo.add(item);
			}
			rs.close();
			ps.close();
			if (retval == null)
				return new DBConfig();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private static DBConfig readConfig(String file) throws Exception {
        	Document configDoc = null;
        	FileInputStream inp = null;
        
          inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+file);
          MyXMLHelper test = new MyXMLHelper(inp, null);
          configDoc =  test.load();
          inp.close();
          int row=0;
          ArrayList<DBItem> addTo = null;
		  DBConfig retval = null;	
          for (Node rn = configDoc.getDocumentElement().getFirstChild(); rn != null; rn = rn.getNextSibling()) {
        	  if (rn.getNodeType() != 1)
        		  continue;
        	  Element re = (Element) rn;
        	  int col = 0;
        	  for (Node cn = re.getFirstChild();cn != null; cn = cn.getNextSibling()) {
        		  if (cn.getNodeType() != 1)
        			  continue;
        		  Element ce = (Element) cn;
        		  DBItem item = new DBItem(row, col, Misc.getParamAsInt(ce.getAttribute( "row_span")), Misc.getParamAsInt(ce.getAttribute( "col_span")), ce.getAttribute("title"), ce.getAttribute("item_tag"), ce.getAttribute("front_page"), Misc.getParamAsInt(ce.getAttribute("component_id")), ce.getAttribute("addnl_param"));
        		  if (retval == null) {
  					retval = new DBConfig();
  					retval.setId(Misc.getUndefInt());
  					retval.setForPortNodeId(Misc.getUndefInt());
  					retval.setForUserId(Misc.getUndefInt());
  					addTo = new ArrayList<DBItem>();
  					retval.setDbItems(addTo);
  				}
  				addTo.add(item);
  				col++;
        	  }
        	  row++;
          }
          if (retval == null)
        	  retval = new DBConfig();
          return retval;
	}
	
	private static void markDirty(int portNodeId, int userId, String tag) throws Exception {
		if (!Misc.isUndef(userId)) {
			String key = userId+"_"+tag;
			DBCache.g_byUserId.remove(key);
		}
		if (!Misc.isUndef(portNodeId)) {
			String key = portNodeId+"_"+tag;
			DBCache.g_byPortNode.remove(key);
		}
		return ;
	}
	
	public static void delete(Connection conn, int portNodeId, int userId, String tag) throws Exception {
		try {
			markDirty(portNodeId, userId, tag);
			PreparedStatement ps = conn.prepareStatement("select db_layouts.id from db_layouts join db_layout_items on (db_layouts.id = db_layout_items.db_layout_id) where (? is null or port_node_id=?) and (? is null or user_id=?) and tag = ? order by db_layouts.id, row_number, col_number");
			Misc.setParamInt(ps, portNodeId,1);
			Misc.setParamInt(ps, portNodeId,2);
			Misc.setParamInt(ps, userId,3);
			Misc.setParamInt(ps, userId,4);
			ps.setString(5, tag);
			ResultSet rs = ps.executeQuery();
			int id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs.close();
			ps.close();
			if (Misc.isUndef(id))
				return;
			ps = conn.prepareStatement("delete from db_layout_items where db_layout_id = ?");
			ps.setInt(1, id);
			ps.execute();
			ps.close();
			ps  = conn.prepareStatement("delete from db_layouts where id = ?");
			ps.setInt(1, id);
			ps.execute();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static DBConfig save(Connection conn, DBConfig config, int portNodeId, int userId, String tag) throws Exception {
		try {
			if (config == null)
				return config;
			if (config.getDbItems() == null || config.getDbItems().size() == 0) {
				delete(conn, portNodeId, userId, tag);
				return null;
			}
			PreparedStatement ps = conn.prepareStatement("select db_layouts.id from db_layouts join db_layout_items on (db_layouts.id = db_layout_items.db_layout_id) where (? is null or port_node_id=?) and (? is null or user_id=?) and tag = ? order by db_layouts.id, row_number, col_number");
			Misc.setParamInt(ps, portNodeId,1);
			Misc.setParamInt(ps, portNodeId,2);
			Misc.setParamInt(ps, userId,3);
			Misc.setParamInt(ps, userId,4);
			ps.setString(5, tag);
			ResultSet rs = ps.executeQuery();
			int id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs.close();
			ps.close();
			if (Misc.isUndef(id)) {
				ps = conn.prepareStatement("insert into db_layouts (port_node_id, user_id, tag) values (?,?,?)");
				Misc.setParamInt(ps, portNodeId, 1);
				Misc.setParamInt(ps, userId, 2);
				ps.setString(3, tag);
				ps.execute();
				rs = ps.getGeneratedKeys();
				id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
				rs.close();
				ps.close();
			}
			if (Misc.isUndef(id)) {
				markDirty(portNodeId, userId, tag);
				return null;
			}
			ps = conn.prepareStatement("delete from db_layout_items where db_layout_id = ?");
			ps.setInt(1, id);
			ps.execute();
			ps.close();
			ps = conn.prepareStatement("insert into db_layout_items (db_layout_id, row_number, col_number, row_span, col_span, title, component_id, item_tag, front_page, addnl_parameter) values (?,?,?,?,?,?,?,?,?,?)");
			for (DBItem item: config.getDbItems()) {
				ps.setInt(1, id);
				Misc.setParamInt(ps, item.getRow(), 2);
				Misc.setParamInt(ps, item.getCol(), 3);
				Misc.setParamInt(ps, item.getRowspan(), 4);
				Misc.setParamInt(ps, item.getColspan(), 5);
				ps.setString(6, item.getTitle());
				Misc.setParamInt(ps, item.getComponentId(), 7);
				ps.setString(8, item.getItemId());
				ps.setString(9, item.getConfigFile());
				ps.setString(10, item.getAddnlParam());
				ps.addBatch();
			}
			ps.executeBatch();
			ps.close();
			config.setId(id);
			markDirty(portNodeId, userId, tag);
			return config;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			Cache cache = Cache.getCacheInstance(conn);
			String tag = "test";
			
			DBConfig config = DBCache.getConfig(conn, 2, 1, "test", cache, "db_generic_operator.xml");
			DBCache.save(conn, config, 2, Misc.getUndefInt(), tag);
			if (!conn.getAutoCommit())
				conn.commit();
			config = DBCache.getConfig(conn, 2, 1, "test", cache, "db_generic_operator.xml");
			DBCache.delete(conn,2, Misc.getUndefInt(), tag);
			if (!conn.getAutoCommit())
				conn.commit();

			DBCache.save(conn, config, Misc.getUndefInt(),1, tag);
			if (!conn.getAutoCommit())
				conn.commit();
			config = DBCache.getConfig(conn, 2, 1, "test", cache, "db_generic_operator.xml");
			DBCache.delete(conn,2, Misc.getUndefInt(), tag);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		}
		finally {
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				conn = null;
			}	
			catch (Exception e2) {
				
			}
		}
			
	}
}

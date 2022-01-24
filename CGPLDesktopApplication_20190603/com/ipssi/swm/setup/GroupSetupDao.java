package com.ipssi.swm.setup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.swm.DBQueries;

public class GroupSetupDao {
	
   public static ArrayList<GroupBean> getGroups(SessionManager session) throws Exception {
	   Connection conn = session.getConnection();
	   HttpServletRequest request = session.request;
	   String pgContext = "tr_group_setup";
	   MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(request, pgContext);
		int v9008 = Misc.getParamAsInt(session.getParameter(searchBoxHelper.m_topPageContext+"9008"),1); //by default sel only active
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
		if (v9008 == Misc.G_HACKANYVAL)
			v9008 = Misc.getUndefInt();
		return getGroups(conn, v123, v9008);
   }
   public static void getRoadSegments(Connection conn, ArrayList<GroupBean> retval) throws Exception {
	   if (retval == null || retval.size() == 0)
		   return;
	   try {
		   StringBuilder sb = new StringBuilder();
		   sb.append("select group_opstation_id, road_segment_id, new_road_segments.name from group_opstation_roadsegments join new_road_segments on (new_road_segments.id = road_segment_id) where group_opstation_id in (");
		   for (int i=0,is=retval.size(); i<is; i++) {
			   GroupBean gb = retval.get(i);
			   if (i != 0)
				   sb.append(",");
			   sb.append(gb.getId());
		   }
		   sb.append(") order by group_opstation_id, name ");
		   PreparedStatement ps = conn.prepareStatement(sb.toString());
		   ResultSet rs = ps.executeQuery();
		   GroupBean prev = null;
		   while (rs.next()) {
			   int gid = Misc.getRsetInt(rs, 1);
			   int rsid = Misc.getRsetInt(rs,2);
			   String n = rs.getString(3);
			   if (gid <= 0 || rsid <= 0 || n == null)
				   continue;
			   if (prev != null && prev.getId() != gid)
				   prev = null;
			   if (prev == null) {
				   for (int i1=0,i1s=retval.size(); i1<i1s;i1++) {
					   if (retval.get(i1).getId() == gid) {
						   prev = retval.get(i1);
						   break;
					   }
				   }
			   }
			   if (prev == null)
				   continue;
			   prev.getRoadSegments().add(new GroupBean.RoadSegments(rsid, n));
		   }
		   rs = Misc.closeRS(rs);
		   ps = Misc.closePS(ps);
	   }
	   catch (Exception e) {
		   e.printStackTrace();
		   throw e;
	   }
   }
   
   public static void getRegions(Connection conn, ArrayList<GroupBean> retval) throws Exception {
	   if (retval == null || retval.size() == 0)
		   return;
	   try {
		   StringBuilder sb = new StringBuilder();
		   sb.append("select group_opstation_id, region_id, regions.short_code from group_opstation_regions join regions on (regions.id = group_opstation_regions.region_id) where group_opstation_id in (");
		   for (int i=0,is=retval.size(); i<is; i++) {
			   GroupBean gb = retval.get(i);
			   if (i != 0)
				   sb.append(",");
			   sb.append(gb.getId());
		   }
		   sb.append(") order by group_opstation_id, regions.short_code ");
		   PreparedStatement ps = conn.prepareStatement(sb.toString());
		   ResultSet rs = ps.executeQuery();
		   GroupBean prev = null;
		   while (rs.next()) {
			   int gid = Misc.getRsetInt(rs, 1);
			   int rsid = Misc.getRsetInt(rs,2);
			   String n = rs.getString(3);
			   if (gid <= 0 || rsid <= 0 || n == null)
				   continue;
			   if (prev != null && prev.getId() != gid)
				   prev = null;
			   if (prev == null) {
				   for (int i1=0,i1s=retval.size(); i1<i1s;i1++) {
					   if (retval.get(i1).getId() == gid) {
						   prev = retval.get(i1);
						   break;
					   }
				   }
			   }
			   if (prev == null)
				   continue;
			   prev.getRegions().add(new GroupBean.Regions(rsid, n));
		   }
		   rs = Misc.closeRS(rs);
		   ps = Misc.closePS(ps);
	   }
	   catch (Exception e) {
		   e.printStackTrace();
		   throw e;
	   }
   }
   public static ArrayList<GroupBean> getGroups(Connection conn, int v123, int v9008) throws Exception {
	   try {
		   if (Misc.isUndef(v9008)) {
			   v9008 = 1;
		   }
		   PreparedStatement ps = conn.prepareStatement(DBQueries.SETUP.GET_GROUP_ALL);
			ps.setInt(1, v123);
			Misc.setParamInt(ps, v9008, 2);
			Misc.setParamInt(ps, v9008, 3);
			ResultSet rs = ps.executeQuery();
			GroupBean prev = null;
			ArrayList<GroupBean> retval = new ArrayList<GroupBean>();
			while (rs.next()) {
				int id = rs.getInt("id");
				if (prev != null && prev.getId() != id)
					prev = null;
				if (prev == null) {
					prev = helperReadGroupHeader(rs);
					retval.add(prev);
				}
				helperReadOpList(rs, prev);
				
			}
			rs.close();
			ps.close();
			getRoadSegments(conn, retval);
			getRegions(conn, retval);
			return retval;
	   }
	   catch (Exception e) {
		   e.printStackTrace();
		   throw e;
	   }
	}
	
	public static GroupBean getGroup(Connection conn, int groupId) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.SETUP.GET_GROUP_INFO);
			ps.setInt(1, groupId);
			ResultSet rs = ps.executeQuery();
			GroupBean prev = null;
			while (rs.next()) {
				int id = rs.getInt("id");
				if (prev != null && prev.getId() != id)
					prev = null;
				if (prev == null) {
					prev = helperReadGroupHeader(rs);
				}
				helperReadOpList(rs, prev);
			}
			rs.close();
			ps.close();
			ArrayList<GroupBean> temp = new ArrayList<GroupBean>();
			temp.add(prev);
			getRoadSegments(conn, temp);;
			getRegions(conn, temp);
			return prev;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static GroupBean read(SessionManager session) {
		int id = Misc.getParamAsInt(session.getParameter("group_id"));
		int portNodeId = Misc.getParamAsInt(session.getParameter("applicableTo"));
		int status = Misc.getParamAsInt(session.getParameter("status"));
		String description = Misc.getParamAsString(session.getParameter("description"));
		String name = Misc.getParamAsString(session.getParameter("name"));
		int recommendedVehicle = Misc.getParamAsInt(session.getParameter("reco_vehicle_count"));
		GroupBean retval = new GroupBean(id, name, status, description, portNodeId, recommendedVehicle);
		helperReadOpsList(Misc.getParamAsString(session.getParameter("load_oplist_xml")), retval, 1);
		helperReadOpsList(Misc.getParamAsString(session.getParameter("unload_oplist_xml")), retval, 2);
		helperReadRoadSegments(Misc.getParamAsString(session.getParameter("roadsegments_xml")), retval);
		helperReadRegions(Misc.getParamAsString(session.getParameter("regions_xml")), retval);
		return retval;
		
	}
	
	public static void save(Connection conn, GroupBean group) throws Exception {
		try {
			//insert into group_opstations (name, status, description, port_node_id, recommended_vehicle values (?,?,?,?,?)"
			boolean isNew = Misc.isUndef(group.getId());
			PreparedStatement ps = conn.prepareStatement(isNew ? DBQueries.SETUP.INSERT_GROUP_HEADER : DBQueries.SETUP.UPDATE_GROUP_HEADER);
			int colIndex = 1;
			ps.setString(colIndex++, group.getName());
			ps.setInt(colIndex++, group.getStatus());
			ps.setString(colIndex++, group.getDescription());
			ps.setInt(colIndex++, group.getPortNodeId());
			ps.setInt(colIndex++, group.getRecoVehicleCount());
			if (!isNew) {
				ps.setInt(colIndex++,group.getId());
				ps.execute();
			}
			else {
				ps.executeUpdate();
				ResultSet rs = ps.getGeneratedKeys();
				int id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
				group.setId(id);
				rs.close();
			}
			ps.close();
			helperDeleteAndInsertOpsList(conn, group.getId(), group.getLoadStations(), 1);
			helperDeleteAndInsertOpsList(conn, group.getId(), group.getUnloadStations(), 2);
			helperDeleteAndInsertRoadSegments(conn, group.getId(), group.getRoadSegments());
			helperDeleteAndInsertRegions(conn, group.getId(), group.getRegions());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static void helperDeleteAndInsertOpsList(Connection conn, int groupId, ArrayList<GroupBean.Station> stationList, int listType) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(DBQueries.SETUP.DELETE_OPSLIST);
			ps.setInt(1, groupId);
			ps.setInt(2, listType);
			ps.execute();
			ps.close();
			ps = conn.prepareStatement(DBQueries.SETUP.INSERT_OPSLIST);
			for (GroupBean.Station station : stationList) {
				ps.setInt(1, groupId);
				ps.setInt(2, listType);
				ps.setInt(3, station.getSeq());
				ps.setInt(4, station.getId());
				ps.addBatch();
			}
			ps.executeBatch();
			ps.clearBatch();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static void helperDeleteAndInsertRoadSegments(Connection conn, int groupId, ArrayList<GroupBean.RoadSegments> stationList) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("delete from group_opstation_roadsegments where group_opstation_id=?");
			ps.setInt(1, groupId);
			ps.execute();
			ps.close();
			ps = conn.prepareStatement("insert into group_opstation_roadsegments(group_opstation_id, road_segment_id) values (?,?)");
			for (GroupBean.RoadSegments station : stationList) {
				ps.setInt(1, groupId);
				ps.setInt(2, station.getId());
				ps.addBatch();
			}
			ps.executeBatch();
			ps.clearBatch();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static void helperDeleteAndInsertRegions(Connection conn, int groupId, ArrayList<GroupBean.Regions> stationList) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("delete from group_opstation_regions where group_opstation_id=?");
			ps.setInt(1, groupId);
			ps.execute();
			ps.close();
			ps = conn.prepareStatement("insert into group_opstation_regions(group_opstation_id, region_id) values (?,?)");
			for (GroupBean.Regions station : stationList) {
				ps.setInt(1, groupId);
				ps.setInt(2, station.getId());
				ps.addBatch();
			}
			ps.executeBatch();
			ps.clearBatch();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static void helperReadOpsList(String xmlStr, GroupBean group, int listType) {
		Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
		int seq = 1;
		for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int opsId = Misc.getParamAsInt(e.getAttribute("op_id"));
			if (!Misc.isUndef(opsId)) {
				GroupBean.Station station = new GroupBean.Station(opsId, null, seq++);
				if (listType == 1) 
					group.addLoadStation(station);
				else
					group.addUnloadStation(station);
			}
		}
	}
	
	private static void helperReadRoadSegments(String xmlStr, GroupBean group) {
		Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
		int seq = 1;
		for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int opsId = Misc.getParamAsInt(e.getAttribute("op_id"));
			if (!Misc.isUndef(opsId)) {
				GroupBean.RoadSegments station = new GroupBean.RoadSegments(opsId, null);
					group.getRoadSegments().add(station);
			}
		}
	}
	
	private static void helperReadRegions(String xmlStr, GroupBean group) {
		Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
		int seq = 1;
		for (Node n = xmlDoc == null || xmlDoc.getDocumentElement() == null ? null : xmlDoc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int opsId = Misc.getParamAsInt(e.getAttribute("op_id"));
			if (!Misc.isUndef(opsId)) {
				GroupBean.Regions station = new GroupBean.Regions(opsId, null);
					group.getRegions().add(station);
			}
		}
	}
	
	private static GroupBean helperReadGroupHeader(ResultSet rs) throws Exception {
		//select group_opstations.id, group_opstations.name, group_opstations.status, group_opstations.description, group_opstations.port_node_id, group_opstations.recommended_vehicle, group_opstation_items.list_type, group_opstation_items.seq, group_opstation_items.opstation_id, op_station.name "
		int id = rs.getInt("id");
		String name = rs.getString("name");
		int status = rs.getInt("status");
		String description = rs.getString("description");
		int portNodeId = rs.getInt("port_node_id");
		int recommendedVehicle = rs.getInt("recommended_vehicle");
		GroupBean retval = new GroupBean(id, name, status, description, portNodeId, recommendedVehicle);
		return retval;		
	}
	
	private static void  helperReadOpList(ResultSet rs, GroupBean group) throws Exception {
		//select group_opstations.id, group_opstations.name, group_opstations.status, group_opstations.description, group_opstations.port_node_id, group_opstations.recommended_vehicle, group_opstation_items.list_type, group_opstation_items.seq, group_opstation_items.opstation_id, op_station.name "
		int listType = Misc.getRsetInt(rs, "list_type");
		int seq = Misc.getRsetInt(rs, "seq");
		int opId = Misc.getRsetInt(rs, "opstation_id");
		String opsName = rs.getString( "ops_name");
		if (Misc.isUndef(opId))
			return;
		GroupBean.Station station = new GroupBean.Station(opId, opsName, seq);
		if (listType == 1)
			group.addLoadStation(station);
		else
			group.addUnloadStation(station);
	}
	
}

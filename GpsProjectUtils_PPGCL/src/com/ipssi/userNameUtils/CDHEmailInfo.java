package com.ipssi.userNameUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.RTreesAndInformation;

public class CDHEmailInfo {
	private int id;
	private int refCdhId;
	private int portNodeId;
	private String custName;
	private String toLocation;
	private double authDistKM;
	private double authTimeKM;
	private int fromOpId;
	private int destType; // //1 => Landmark, 2 => Shapefile point, 3 => op_station
	private int destId;
	private double destLon;
	private double destLat;
	private String email;
	private String phone;
	private String refDestItemCode;
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("To[id,type]:").append(destId).append(",").append(destType)
		.append(" [Lon,Lat]:").append(destLon).append(",").append(destLat)
		.append(" EMAIL:").append(email).append(" Phone:").append(phone)
		.append(" refDestCode:").append(refDestItemCode).append(" From:").append(fromOpId);
		return sb.toString();
	}
	private static ConcurrentHashMap<Integer, CDHEmailInfo> g_cdhEmailPhones = new ConcurrentHashMap<Integer, CDHEmailInfo>();
	private static ConcurrentHashMap<Integer, ArrayList<Integer>> g_cdhEmailPhonesByDestOpStnId = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	private static ConcurrentHashMap<Integer, ArrayList<Integer>> g_cdhEmailPhonesByCDHId = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	private static void helperAddForDestOpId(int destOpId, int cdhId) {
		ArrayList<Integer> entry = g_cdhEmailPhonesByDestOpStnId.get(destOpId);
		boolean found = false;
		for (int i=0,is=entry == null ? 0 : entry.size(); i<is; i++) {
			if (entry.get(i) == cdhId) {
				found = true;
				break;
			}
		}
		if (!found) {
			entry = new ArrayList<Integer>();
			g_cdhEmailPhonesByDestOpStnId.put(destOpId, entry);
			entry.add(cdhId);
		}
	}
	private static void helperRemoveForDestOpId(int destOpId, int cdhId) {
		ArrayList<Integer> entry = g_cdhEmailPhonesByDestOpStnId.get(destOpId);
		boolean found = false;
		for (int i=0,is=entry == null ? 0 : entry.size(); i<is; i++) {
			if (entry.get(i) == cdhId) {
				entry.remove(i);
				break;
			}
		}		
	}
	private static void helperAddForDestCodeId(int challanDestHelperId, int cdhId) {
		ArrayList<Integer> entry = g_cdhEmailPhonesByCDHId.get(challanDestHelperId);
		boolean found = false;
		for (int i=0,is=entry == null ? 0 : entry.size(); i<is; i++) {
			if (entry.get(i) == cdhId) {
				found = true;
				break;
			}
		}
		if (!found) {
			entry = new ArrayList<Integer>();
			g_cdhEmailPhonesByCDHId.put(challanDestHelperId, entry);
			entry.add(cdhId);
		}
	}
	private static void helperRemoveForDestCodeId(int challanDestHelperId, int cdhId) {
		ArrayList<Integer> entry = g_cdhEmailPhonesByCDHId.get(challanDestHelperId);
		boolean found = false;
		for (int i=0,is=entry == null ? 0 : entry.size(); i<is; i++) {
			if (entry.get(i) == cdhId) {
				entry.remove(i);
				break;
			}
		}		
	}
	private static String g_getCDHEmail = " select c.ref_item_code c_ref_item_code, c.alert_mail_id c_alert_mail_id "+
		" , c.alert_phone c_alert_phone, c.port_node_id c_port_node_id, c.cust_name c_cust_name,c.to_location c_to_location "+
		" , c.auth_dist c_auth_dist, c.auth_time c_auth_time, c.from_op_id c_from_op_id "+
		" , c.id c_id, c.cdh_id c_cdh_id "+
		" , cl.id cl_id, cl.lowerX cl_lon, cl.upperX cl_lat, cs.id cs_id, cs.longitude cs_lon, cs.latitude cs_lat, co.id co_id, cr.id cr_id, (cr.lowerX+cr.upperX)/2 cr_lon, (cr.lowerY+cr.upperY)/2 cr_lat "+
		" , dl.id dl_id, dl.lowerX dl_lon, dl.upperX dl_lat, ds.id ds_id, ds.longitude ds_lon, ds.latitude ds_lat, dos.id dos_id, dr.id dr_id, (dr.lowerX+dr.upperX)/2 dr_lon, (dr.lowerY+dr.upperY)/2 dr_lat "+
		" from cdh_email_phone c "+ //this may get replaced with port stuff
		
		" left outer join landmarks cl on (c.landmark_id = cl.id) "+
		" left outer join shapefile_points cs on (c.shape_id = cs.id) "+
		" left outer join op_station co on (c.opstation_id = co.id) left outer join regions cr on (cr.id = co.gate_reg_id) "+
		" left outer join challan_dest_helper cdh on (c.cdh_id = cdh.id) "+
		" left outer join landmarks dl on (cdh.landmark_id = dl.id) "+
		" left outer join shapefile_points ds on (cdh.shape_point_id = ds.id) "+
		" left outer join op_station dos on (cdh.op_station_id = dos.id)  left outer join regions dr on (dr.id = dos.gate_reg_id) "
		;
	private static String g_updtateCDHId = "update cdh_email_phone set landmark_id = ?, shape_id=?, opstation_id = ? where id = ?"; 

	
	public static CDHEmailInfo getCDHInfo(Connection conn, int itemId) {
		loadCDH(conn, itemId, Misc.getUndefInt(), false);
		CDHEmailInfo cdhEmailInfo = g_cdhEmailPhones.get(itemId);
		return cdhEmailInfo;
	}
	public static CDHEmailInfo getCDHInfo(Connection conn, int fromOpStationId, int opStationId, int portNodeId, String destItemCode, int challanDestHelperId, double lon, double lat, double distThreshKM) {
		CDHEmailInfo retval = null;
		try {
			if (Misc.isUndef(portNodeId)) {
				if ("node_lafarge".equals(Misc.getServerName()))
					portNodeId = 481;
				else 
					portNodeId = 2;
			}
			if (Misc.isUndef(distThreshKM))
				distThreshKM = 15;
			
			loadCDH(conn, Misc.getUndefInt(), Misc.getUndefInt(), false);
			if (!Misc.isUndef(opStationId)) {
				ArrayList<Integer> entryList = CDHEmailInfo.g_cdhEmailPhonesByDestOpStnId.get(opStationId);
				for (int i1=0,i1s=entryList == null ? 0 : entryList.size();i1<i1s;i1++) {
					retval = g_cdhEmailPhones.get(entryList.get(i1));
					if (retval != null && (Misc.isUndef(fromOpStationId) || Misc.isUndef(retval.fromOpId) || (retval.fromOpId == fromOpStationId)))
						return retval;
				}
			}
			if (!Misc.isUndef(challanDestHelperId)) {
				ArrayList<Integer> entryList = CDHEmailInfo.g_cdhEmailPhonesByCDHId.get(challanDestHelperId);
				for (int i1=0,i1s=entryList == null ? 0 : entryList.size();i1<i1s;i1++) {
					retval = g_cdhEmailPhones.get(entryList.get(i1));
					if (retval != null && (Misc.isUndef(fromOpStationId) || Misc.isUndef(retval.fromOpId) || (retval.fromOpId == fromOpStationId)))
						return retval;
				}
			}
			Pair<CDHEmailInfo, Double> searchResult = RTreeSearch.getNearestCDHLM(conn, lon, lat, destItemCode, challanDestHelperId, fromOpStationId, portNodeId, distThreshKM);
			if (searchResult != null)
				retval = searchResult.first;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	public static void loadCDH(Connection conn, int itemId, int portNodeId, boolean must) {
		try {
			if (must) {
				if (!Misc.isUndef(itemId) || !Misc.isUndef(portNodeId)) {
					ArrayList<Integer> cdhList = new ArrayList<Integer> ();
					if (!Misc.isUndef(itemId)) {
						cdhList.add(itemId);
					}
					else {
						PreparedStatement ps = conn.prepareStatement("select distinct c.id from cdh_email_phone c join port_nodes leaf on (leaf.id = c.port_node_id) left outer join anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ");
						ps.setInt(1, portNodeId);
						ResultSet rs = ps.executeQuery();
						while (rs.next()) {
							cdhList.add(rs.getInt(1));
						}
						rs.close();
						ps.close();
					}
					ArrayList<Triple<Integer, Double, Double>> entries = new ArrayList<Triple<Integer, Double, Double>>();
					for (Integer iv : cdhList) {
						CDHEmailInfo cdh = g_cdhEmailPhones.get(iv);
						if (cdh != null) {
							if (!Misc.isUndef(cdh.destId) && cdh.destType == 3)
								CDHEmailInfo.helperRemoveForDestOpId(cdh.destId, cdh.id);
							if (cdh.refDestItemCode != null && cdh.refDestItemCode.length() > 0)
								CDHEmailInfo.helperRemoveForDestCodeId(cdh.refCdhId, cdh.id);
							g_cdhEmailPhones.remove(iv);
							
							entries.add(new Triple<Integer, Double, Double>(cdh.id, cdh.destLon, cdh.destLat));
						}
					}
					RTreesAndInformation.clearCDHLandmarksRTree(entries);
				}
				else { //need to remove all
					for (Map.Entry<Integer, CDHEmailInfo> entry : g_cdhEmailPhones.entrySet()) {
						g_cdhEmailPhones.remove(entry.getKey());
					}
					CDHEmailInfo.g_cdhEmailPhonesByCDHId.clear();
					CDHEmailInfo.g_cdhEmailPhonesByDestOpStnId.clear();
					RTreesAndInformation.clearCDHLandmarksRTree(null);
				}
			}
			if (!Misc.isUndef(itemId)) {
				CDHEmailInfo cdh = g_cdhEmailPhones.get(itemId);
				if (cdh != null)
					return;
			}
			
			String q = g_getCDHEmail;
			if (!Misc.isUndef(portNodeId)) {
				q.replace("from cdh_email_phone c", "from cdh_email_phone c join port_nodes leaf on (leaf.id = c.port_node_id) join port_nodes anc on (anc.id = "+portNodeId+" and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ");				
			}
			else if (!Misc.isUndef(itemId)) {
				q += " where c.id = "+itemId ;
			}
			Cache cache = Cache.getCacheInstance(conn);
			int prevPortNodeId = Misc.getUndefInt();
			StopDirControl stopDirControl = null;
			PreparedStatement ps = conn.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				CDHEmailInfo cdhEmailInfo = new CDHEmailInfo();
				cdhEmailInfo.id = rs.getInt("c_id");
				cdhEmailInfo.refCdhId = Misc.getRsetInt(rs, "c_cdh_id");
				cdhEmailInfo.portNodeId = Misc.getRsetInt(rs, "c_port_node_id");
				cdhEmailInfo.custName = rs.getString("c_cust_name");
				cdhEmailInfo.toLocation = rs.getString("c_to_location");
				cdhEmailInfo.authDistKM = Misc.getRsetDouble(rs, "c_auth_dist");
				cdhEmailInfo.authTimeKM = Misc.getRsetDouble(rs, "c_auth_time");
				cdhEmailInfo.fromOpId = Misc.getRsetInt(rs, "c_from_op_id");
				int destId = Misc.getRsetInt(rs, "cl_id");
				double destLon = Misc.getRsetDouble(rs, "cl_lon");
				double destLat = Misc.getRsetDouble(rs, "cl_lat");
				int destType = 0;
				
				if (Misc.isUndef(destId)) {
					destId = Misc.getRsetInt(rs, "dl_id");
					destLon = Misc.getRsetDouble(rs, "dl_lon");
					destLat = Misc.getRsetDouble(rs, "dl_lat");
						
				}
				if (!Misc.isUndef(destId))
					destType = 1;
				if (Misc.isUndef(destId)) {
					destId = Misc.getRsetInt(rs, "cs_id");
					destLon = Misc.getRsetDouble(rs, "cs_lon");
					destLat = Misc.getRsetDouble(rs, "cs_lat");
				}
				if (Misc.isUndef(destId)) {
					destId = Misc.getRsetInt(rs, "ds_id");
					destLon = Misc.getRsetDouble(rs, "ds_lon");
					destLat = Misc.getRsetDouble(rs, "ds_lat");
				}
				if (!Misc.isUndef(destId))
					destType = 2;
				if (Misc.isUndef(destId)) {
					destId = Misc.getRsetInt(rs, "co_id");
					destLon = Misc.getRsetDouble(rs, "cr_lon");
					destLat = Misc.getRsetDouble(rs, "cr_lat");
				}
				
				if (Misc.isUndef(destId)) {
					destId = Misc.getRsetInt(rs, "dos_id");
					destLon = Misc.getRsetDouble(rs, "dr_lon");
					destLat = Misc.getRsetDouble(rs, "dr_lat");
				}
				if (!Misc.isUndef(destId))
					destType = 3;
				cdhEmailInfo.destType = destType;
				cdhEmailInfo.destId = destId;
				cdhEmailInfo.destLon = destLon;
				cdhEmailInfo.destLat = destLat;
				cdhEmailInfo.email = rs.getString("c_alert_mail_id");
				cdhEmailInfo.phone = rs.getString("c_alert_phone");
				cdhEmailInfo.refDestItemCode = rs.getString("c_ref_item_code");
				if (Misc.isUndef(destId)) {
					//try to look up location and then update the record for future use
					TextInfo textInfo = new TextInfo();
					textInfo.setLine(cdhEmailInfo.toLocation,0);
					textInfo.setCustName(cdhEmailInfo.custName);
					if (stopDirControl == null || prevPortNodeId != cdhEmailInfo.portNodeId) {
						stopDirControl = StopDirControl.getControlFromOrg(cache.getPortInfo(cdhEmailInfo.portNodeId, conn));
						prevPortNodeId = cdhEmailInfo.portNodeId;
					}
					IdInfo idInfo = Utils.getIdInfo(textInfo, cdhEmailInfo.portNodeId, cdhEmailInfo.refCdhId, true, conn, stopDirControl);
					if (idInfo == null) {
						idInfo = Utils.getIdInfo(textInfo, cdhEmailInfo.portNodeId, cdhEmailInfo.refCdhId, true, conn, stopDirControl);
					}
					if (!Misc.isUndef(idInfo.getLongitude()) && !Misc.isEqual(idInfo.getLongitude(), 0)) {
						cdhEmailInfo.destId = idInfo.getDestId();
						cdhEmailInfo.destType = idInfo.getDestIdType();
						cdhEmailInfo.destLon = idInfo.getLongitude();
						cdhEmailInfo.destLat = idInfo.getLatitude();
						PreparedStatement ps2 = conn.prepareStatement(g_updtateCDHId);
						Misc.setParamInt(ps2, Misc.getUndefInt(), 1);
						Misc.setParamInt(ps2, Misc.getUndefInt(), 2);
						Misc.setParamInt(ps2, Misc.getUndefInt(), 3);
						ps2.setInt(cdhEmailInfo.destType, cdhEmailInfo.destId);//hackish the order is same as destType
						ps2.setInt(4, cdhEmailInfo.id);
						ps2.execute();
						ps2.close();
					}
				}
				if (cdhEmailInfo.refDestItemCode != null && cdhEmailInfo.refDestItemCode.length() > 0 && Misc.isUndef(cdhEmailInfo.refCdhId) && (!Misc.isUndef(cdhEmailInfo.destLon)) && !Misc.isEqual(cdhEmailInfo.destLon, 0)) { //
					//TODO update challan_dest_helper
				}
				g_cdhEmailPhones.put(cdhEmailInfo.id, cdhEmailInfo);
				if (cdhEmailInfo.destType == 3 && !Misc.isUndef(cdhEmailInfo.destId))
					CDHEmailInfo.helperAddForDestOpId(cdhEmailInfo.destId, cdhEmailInfo.id);
				if (cdhEmailInfo.refDestItemCode != null && cdhEmailInfo.refDestItemCode.length() > 0)
					CDHEmailInfo.helperAddForDestCodeId(cdhEmailInfo.refCdhId, cdhEmailInfo.id);
				//add to regions ..
				RTreesAndInformation.addCDHLandMark(cdhEmailInfo.id, cdhEmailInfo.destLon, cdhEmailInfo.destLat);
			}
			rs.close();
			ps.close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRefCdhId() {
		return refCdhId;
	}
	public void setRefCdhId(int refCdhId) {
		this.refCdhId = refCdhId;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getToLocation() {
		return toLocation;
	}
	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
	}
	public double getAuthDistKM() {
		return authDistKM;
	}
	public void setAuthDistKM(double authDistKM) {
		this.authDistKM = authDistKM;
	}
	public double getAuthTimeKM() {
		return authTimeKM;
	}
	public void setAuthTimeKM(double authTimeKM) {
		this.authTimeKM = authTimeKM;
	}
	public int getFromOpId() {
		return fromOpId;
	}
	public void setFromOpId(int fromOpId) {
		this.fromOpId = fromOpId;
	}
	public int getDestType() {
		return destType;
	}
	public void setDestType(int destType) {
		this.destType = destType;
	}
	public int getDestId() {
		return destId;
	}
	public void setDestId(int destId) {
		this.destId = destId;
	}
	public double getDestLon() {
		return destLon;
	}
	public void setDestLon(double destLon) {
		this.destLon = destLon;
	}
	public double getDestLat() {
		return destLat;
	}
	public void setDestLat(double destLat) {
		this.destLat = destLat;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getRefDestItemCode() {
		return refDestItemCode;
	}
	public void setRefDestItemCode(String refDestItemCode) {
		this.refDestItemCode = refDestItemCode;
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
		   conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		   if (!conn.getAutoCommit())
			   conn.setAutoCommit(true);
		   int portNodeId = 686;
			Cache cache = Cache.getCacheInstance(conn);
			StopDirControl stopDirControl = StopDirControl.getControlFromOrg(cache.getPortInfo(portNodeId, conn));
			TextInfo textInfo = null;
			IdInfo idInfo = null;
			ArrayList<Integer> portNodes = new ArrayList<Integer>();
			portNodes.add(686);
			
			textInfo = new TextInfo();
			textInfo.setLine("BENGALURU(CENTRAL)",0);
			textInfo.setCustName(null);
			
			idInfo =  Utils.getIdInfoByPartialMatch(conn, textInfo, portNodes, true, false);//DEBUG13
			idInfo = Utils.getIdInfo(textInfo, portNodeId, Misc.getUndefInt(), true, conn, stopDirControl);
			
			textInfo = new TextInfo();
			textInfo.setLine("BENGALURU",0);
			textInfo.setCustName(null);
			idInfo = Utils.getIdInfo(textInfo, portNodeId, Misc.getUndefInt(), true, conn, stopDirControl);
			
			
			textInfo = new TextInfo();
			textInfo.setLine("BANGALORE",0);
			textInfo.setCustName(null);
			idInfo = Utils.getIdInfo(textInfo, portNodeId, Misc.getUndefInt(), true, conn, stopDirControl);

			
			
			
		   loadCDH(conn, Misc.getUndefInt(), Misc.getUndefInt(), true);
	   }
	   catch (Exception e) {
		 e.printStackTrace();  
		 destroyIt = true;
	   }
	   finally {
		   if (conn != null) { 
			   try {
				   DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			   }
		   	  catch (Exception e) {
		   	  }
		  }
	   }
	}
	
}

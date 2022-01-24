package com.ipssi.eta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.LocalNameHelperRTree;
import com.ipssi.mapguideutils.ReadShapeFile;

public class SrcDestInfo implements Comparable {
	//following must start from 0 and must be continous
	public final static int G_TARGET_SENDER = 0;
	public final static int G_TARGET_RECEIVER = 1;
	public final static int G_TARGET_TRANSPORTER = 2;
	public final static int G_TARGET_UNCLASSIFIED = 3;
	public final static int G_ALERT_FOR_TY_SMS = 0;
	public final static int G_ALERT_FOR_TY_EMAIL = 1;
	public final static int G_ALERT_FOR_TY_NOTIFICATION = 2;

	public final static int ALERT_SRC = 0;
	public final static int ALERT_DEST = 1;
	public final static int ALERT_DELAY_INTERMEDIATE = 2;
	public final static int ALERT_DELAY_CONT = 3;
	public final static int ALERT_DELAYED_SRC_EXIT = 4;
	public final static int ALERT_DELAYED_DEST_EXIT = 5;
	public final static int ALERT_ONEXIT_DEST = 6;
	public final static int ALERT_NONREACH_DEST = 7;
	public final static int ALERT_SKIPPED_INTERMEDIATE = 8;
	public final static int ALERT_REACH_INTERMEDIATE = 9;
	public final static int ALERT_NEARING_SRC_BACK = 10;
	public final static int ALERT_STOPPAGE_FORW = 11;
	public final static int ALERT_STOPPAGE_BACKW = 12;
	public static String getNameForAlertTy(int ty) {
		switch (ty) {
		case ALERT_SRC: return "ALERT: Away Src";
		case ALERT_DEST: return "ALERT: Nearing Dest";
		case ALERT_DELAY_INTERMEDIATE: return "ALERT: Not reached wp";
		case ALERT_DELAY_CONT: return "ALERT: Running Late";
		case ALERT_DELAYED_SRC_EXIT: return "ALERT: Src Detention";
		case ALERT_DELAYED_DEST_EXIT: return "ALERT: Dest Detention";
		case ALERT_ONEXIT_DEST: return "ALERT: Dest Exit";
		case ALERT_NONREACH_DEST: return "ALERT: Not reached dest";
		case ALERT_SKIPPED_INTERMEDIATE: return "ALERT: Skipped Intermed";
		case ALERT_REACH_INTERMEDIATE: return "ALERT: Reached Intermed";
		case ALERT_NEARING_SRC_BACK: return "ALERT: Reaching Src";
		case ALERT_STOPPAGE_FORW: return "ALERT: Stopped Enroute";
		case ALERT_STOPPAGE_BACKW: return "ALERT: Stopped Back";

		default: return null;
		}
	}
	public static int getMinAlertTy() {
		return 0;
	}
	public static int getMaxAlertTy() {
		return 12;
	}
	public static boolean isDistBasedAlert(int ty) {
		return ty == ALERT_SRC || ty == ALERT_DEST || ty == ALERT_NEARING_SRC_BACK;
	}
	public static boolean isTimeBasedAlert(int ty) {
		return ty == ALERT_DELAYED_SRC_EXIT || ty == ALERT_DELAYED_DEST_EXIT || ty == ALERT_NONREACH_DEST || ty == ALERT_STOPPAGE_FORW || ty == ALERT_STOPPAGE_BACKW;
	}
	public static boolean isWPIndexBasedAlert(int ty) {
		return ty == SrcDestInfo.ALERT_DELAY_INTERMEDIATE || ty == SrcDestInfo.ALERT_NONREACH_DEST || ty == SrcDestInfo.ALERT_REACH_INTERMEDIATE || ty == SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE;
	}
	public final static int G_OP_STATION_TYPE=0; 
	public final static int G_REGION_TYPE = 1;
	//public final static int G_LANDMARK_TYPE = 2; //NOT USED
	public final static int G_MAP_TYPE = 3;
	public static ArrayList<ArrayList<MiscInner.PairIntStr>> g_defaultAlertFormat = null;
	static {
		g_defaultAlertFormat = new ArrayList<ArrayList<MiscInner.PairIntStr>>();
		g_defaultAlertFormat.add(null);
		g_defaultAlertFormat.add(null);
		g_defaultAlertFormat.add(null);
	}
	public static void addDefaultAlertFormat(int ty, int forTy, String s) {//forTy == 0 -> SMS, 1 == email 2 == notifcation
		boolean added = false;
		ArrayList<MiscInner.PairIntStr> addToFormatHolder = g_defaultAlertFormat.get(forTy);
		if (addToFormatHolder == null) {
			addToFormatHolder = new ArrayList<MiscInner.PairIntStr>();
			g_defaultAlertFormat.add(addToFormatHolder);
		}
		
		for (int i=0,is=addToFormatHolder == null ? 0 : addToFormatHolder.size(); i<is; i++) 
			if (addToFormatHolder.get(i).first == ty) {
				added = true;
				addToFormatHolder.get(i).second = s;
				break;
			}
		if (!added) {
			addToFormatHolder.add(new MiscInner.PairIntStr(ty, s));
		}
	}
	public static String getDefaultAlertFormat(int ty, int forTy) {
		ArrayList<MiscInner.PairIntStr> addToFormatHolder = g_defaultAlertFormat.get(forTy);
		for (int i=0,is=addToFormatHolder == null ? 0 : addToFormatHolder.size(); i<is; i++)
			if (addToFormatHolder.get(i).first == ty)
				return addToFormatHolder.get(i).second;
		if (forTy != 0)
			return getDefaultAlertFormat(ty, 0);
		return null;
	}
	static {
		addDefaultAlertFormat(SrcDestInfo.ALERT_SRC,0,"%vehicleId has left for %dest. ETA is %curr_eta. Is now %prop KM away from %src. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_DEST,0,"%vehicleId is now within %prop KM of %dest. Exp ETA is %est_eta. Currently at %location. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_DELAY_INTERMEDIATE,0,"%vehicleId for %dest has not reached %intermediate and may be running late. Exp ETA is %est_eta. Currently at %location. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_DELAY_CONT,0,"%vehicleId for %dest is running late. Exp ETA is %est_eta. Currently at %location. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_REACH_INTERMEDIATE,0,"%vehicleId for %dest has reached %intermediate.Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_DELAYED_DEST_EXIT,0,"%vehicleId is detained at Dest: %dest since %dest_intime. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_DELAYED_SRC_EXIT,0,"%vehicleId is detained at Src: %src since %intime. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_NONREACH_DEST,0,"%vehicleId has not reached %dest by ETA. Exp ETA is %est_eta. Currently at %location. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_SKIPPED_INTERMEDIATE,0,"%vehicleId for %dest has missed %intermediate. Currently at %location. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_ONEXIT_DEST,0,"%vehicleId has exitted %dest. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_NEARING_SRC_BACK,0,"%vehicleId is nearing %src. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_STOPPAGE_FORW,0,"%vehicleId has stopped while going to %dest. Sent at:%sentTime");
		addDefaultAlertFormat(SrcDestInfo.ALERT_STOPPAGE_BACKW,0,"%vehicleId has stopped after exitting %dest. Sent at:%sentTime");
	}
	private static ConcurrentHashMap<Integer, SrcDestInfo> g_srcDestInfoCache = null;//
	public static Collection<SrcDestInfo> dbgHelperGetAllSD() {
		return g_srcDestInfoCache == null ? null : g_srcDestInfoCache.values();
	}
	
	public static class WayPoint {
		private double longitude;
		private double latitude;
		private String name;
		private double buffer;
		private double transit;
		private int regionId = Misc.getUndefInt();
		public WayPoint(double longitude, double latitude, String name, double buffer, double transit, int regionId) {
			this.longitude = longitude;
			this.latitude = latitude;
			this.name = name;
			this.buffer = buffer;
			this.transit = transit;
			this.regionId = regionId;
		}
		public double getLongitude() {
			return longitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		public double getLatitude() {
			return latitude;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getBuffer() {
			return buffer;
		}
		public void setBuffer(double buffer) {
			this.buffer = buffer;
		}
		public double getTransit() {
			return transit;
		}
		public void setTransit(double transit) {
			this.transit = transit;
		}
		public int getRegionId() {
			return regionId;
		}
		public void setRegionId(int regionId) {
			this.regionId = regionId;
		}
	}
	
	public static class AlertSetting implements Cloneable {
		private double dist;
		private int alertType;
		private int contactId;
		private int alertRole;
		private String contactName;
		private String contactPhone;
		private String contactEmail;
		private int startMin;
		private int endMin;
		public boolean isValidForSend(long ts) {
			if (startMin == endMin || startMin < 0 || endMin <0)
				return true;
			java.util.Date dt = new java.util.Date(ts);
			if (dt == null)
				return true;
			int minAsked = dt.getHours()*60+dt.getMinutes();
			return startMin < endMin ? minAsked >= startMin && minAsked <= endMin : (minAsked >= startMin && minAsked <= 24*60) || (minAsked <= endMin);
		}
		public AlertSetting clone() {
			return new AlertSetting(dist, alertType, contactId, contactName, contactPhone, contactEmail,alertRole,startMin, endMin);
		}
		public AlertSetting(double dist, int alertType, int contactId, String contactName, String contactPhone, String contactEmail, int alertRole, int startMin, int endMin) {
			this.dist = dist;
			this.alertType = alertType;
			this.contactId = contactId;
			this.contactName = contactName;
			this.contactPhone = contactPhone;
			this.contactEmail = contactEmail;
			this.alertRole = alertRole;
			this.startMin = startMin;
			this.endMin = endMin;
		}
		public double getDist() {
			return dist;
		}
		public void setDist(double dist) {
			this.dist = dist;
		}
		public int getAlertType() {
			return alertType;
		}
		public void setAlertType(int alertType) {
			this.alertType = alertType;
		}
		public int getContactId() {
			return contactId;
		}
		public void setContactId(int contactId) {
			this.contactId = contactId;
		}
		public String getContactName() {
			return contactName;
		}
		public void setContactName(String contactName) {
			this.contactName = contactName;
		}
		public String getContactPhone() {
			return contactPhone;
		}
		public void setContactPhone(String contactPhone) {
			this.contactPhone = contactPhone;
		}
		public String getContactEmail() {
			return contactEmail;
		}
		public void setContactEmail(String contactEmail) {
			this.contactEmail = contactEmail;
		}
		public int getAlertRole() {
			return alertRole;
		}
		public void setAlertRole(int alertRole) {
			this.alertRole = alertRole;
		}
		public int getStartMin() {
			return startMin;
		}
		public void setStartMin(int startMin) {
			this.startMin = startMin;
		}
		public int getEndMin() {
			return endMin;
		}
		public void setEndMin(int endMin) {
			this.endMin = endMin;
		}
	}
	private int id;
	private int reverseId;
	private String name;
	private int portNodeId;
	private int status;
	private int srcId;
	private int srcType;
	private double srcLong;
	private double srcLat;
	private double srcBuffer;
	private double srcRadius = Misc.getUndefDouble();
	public String srcName;
	private int destId;
	private int destType;
	private double destLong;
	private double destLat;
	private double destBuffer;
	private double destRadius = Misc.getUndefDouble();
	private String destName;
	private double transitTime;
	private double transitDist;
	private double checkContDelayHrFreq = 1;
	private double notReachThresholdHr = 1;
	private int stoppageRuleId = 1;
	private String notes;
	private ArrayList<ArrayList<MiscInner.PairIntStr>> alertFormats;
	private ArrayList<Pair<Integer, ArrayList<AlertSetting>>> alertSettings;
	
	private int alertSrcDestId = Misc.getUndefInt();
	private ArrayList<WayPoint> waypoints = null;
	private int priority = 0;
	private ArrayList<Integer> areaOfOpRegions = null;
	private ArrayList<Integer> roadSegments = null;
	private boolean calcDone = false;
	private ArrayList<ArrayList<MiscInner.PairIntStr>> calcAlertFormats;
	private ArrayList<Pair<Integer, ArrayList<AlertSetting>>> calcAlertSettings;
	private double calcMaxNearSrcDist = Misc.getUndefDouble();
	public void addRoadSegment(int roadSegId) {
		if (roadSegments == null)
			roadSegments = new ArrayList<Integer>();
		roadSegments.add(roadSegId);
	}
	public void addAreaOfOp(int regionId) {
		if (areaOfOpRegions == null)
			areaOfOpRegions = new ArrayList<Integer>();
		areaOfOpRegions.add(regionId);
	}
	public boolean hasAlertOfType(Connection conn, int ty) {
		ArrayList<AlertSetting> settings = this.getAlertSettingCalc(conn, ty);
		return settings != null && settings.size() > 0;
	}
	
	private void putNameInfo(Connection conn, StringBuilder sb, int ty, int id, double lon, double lat, String name) {
		try {
			if (ty == G_OP_STATION_TYPE) {
				sb.append("Op:").append(id);
				OpStationBean bean = TripInfoCacheHelper.getOpStation(id);
				sb.append("-").append(bean == null ? "null" : bean.getOpStationName());
			}
			else if (ty == G_REGION_TYPE) {
				sb.append("Reg:").append(id);
				RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(id, conn);
				sb.append("-").append(rth == null || rth.region == null ? "null" : rth.region.m_name);
			}
			else if (!Misc.isUndef(lon)) {
				sb.append("Pt:").append(lon).append(",").append(lat).append("-").append(name);
			}
			else {
				sb.append("None");
			}
		}
		catch (Exception e) {
			
		}
	}
	public String toString() {
		Connection conn = null;
		String retval = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			retval = toString(conn);
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
			if (conn != null)
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			}
			catch (Exception e2) {
				
			}
			
		}
		return null;
	}
	public String toString(Connection conn) {
		StringBuilder sb = new StringBuilder();
		try {
			
			sb.append("[SrcDest:").append(this.getId()).append(" ").append(this.getName())
			.append("]").append("From:");
			this.putNameInfo(conn, sb, this.getSrcType(), this.getSrcId(), this.getSrcLong(), this.getSrcLat(), this.getSrcName());
			sb.append(" To:");
			this.putNameInfo(conn, sb, this.getDestType(), this.getDestId(), this.getDestLong(), this.getDestLat(), this.getDestName());
			sb.append("Transit:").append(this.getTransitDist()).append(" Time:").append(this.getTransitTime()).append(" Ref:").append(this.getAlertSrcDestId());
			for (int i=0,is=this.waypoints == null ? 0: this.waypoints.size();i<is;i++) {
				WayPoint waypoint = waypoints.get(i);
				sb.append("\nWapoint:").append(i).append(" : ");
				this.putNameInfo(conn, sb, waypoint.getRegionId() > 0 ? G_REGION_TYPE : Misc.getUndefInt(), waypoint.getRegionId(), waypoint.getLongitude(), waypoint.getLatitude(), waypoint.getName());
				sb.append(" Tansit:").append(waypoint.getTransit());
			}
			for (int i=SrcDestInfo.getMinAlertTy(),is=SrcDestInfo.getMaxAlertTy(); i<is; i++) {
				ArrayList<AlertSetting> adlist = this.getAlertSetting(i);
				String fmt = null;
				for (int j=0;j<3;j++) {
					String fmt0 = this.getAlertFormat(i, j,false, true, true);
					if (fmt0 != null) {
						fmt = (fmt == null ? "" : fmt)+"["+j+":"+fmt0+"]";	 
					}
				}
				
				if (fmt == null && (adlist == null || adlist.size() == 0))
					continue;
				sb.append("\n").append(SrcDestInfo.getNameForAlertTy(i)).append(":").append(fmt);
				for (int j=0,js=adlist == null ? 0 : adlist.size(); j<js;j++) {
					sb.append("[dist,ty,cid:").append(adlist.get(j).getDist()).append(",").append(adlist.get(j).getAlertType()).append(",").append(adlist.get(j).getAlertRole()).append(",").append(adlist.get(j).getContactId()).append("]");
				}
			}
		}
		catch (Exception e) {
			
		}
		return sb.toString();
	}
	private void calcFromAlertSrcDestId(Connection conn)  {
		try {
			if (calcDone)
				return;
			calcDone = true;
		//	if (Misc.isUndef(alertSrcDestId)) {
				
		//		return;
		//	}
			SrcDestInfo ref = SrcDestInfo.getSrcDestInfo(conn, alertSrcDestId);
			//if (ref == null)
			//	return;
			for (int i=SrcDestInfo.getMinAlertTy(),is=SrcDestInfo.getMaxAlertTy();i<=is;i++) {
				for (int j=0;j<3;j++) {
					String refFormat = ref == null ? null : ref.getAlertFormat(i,j,false, false, true);
					String meFormat = getAlertFormat(i,j,false, true, true);
					addAlertFormat(i, j, meFormat != null || refFormat == null ? meFormat : refFormat, true);
				}
				ArrayList<AlertSetting> refSetting = ref == null ? null : ref.getAlertSetting(i, false);
				ArrayList<AlertSetting> meSetting = getAlertSetting(i, false);
				for (int j=0,js=refSetting == null ? 0 : refSetting.size(); j<js;j++)
					addAlertSetting(i, refSetting.get(j), true);
	
				for (int j=0,js=meSetting == null ? 0 : meSetting.size(); j<js;j++)
					addAlertSetting(i, meSetting.get(j), true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public String getAlertFormatCalc(Connection conn, int ty, int forTy) {
		if (conn != null)
			calcFromAlertSrcDestId(conn);
		return getAlertFormat(ty, forTy, true, false, false);
	}
	public String getAlertFormat(int ty, int forTy) {
		return getAlertFormat(ty, forTy, false, false, false);
	}
	
	public String getAlertFormat(int ty, int forTy, boolean doinCalc, boolean dontGetDefault, boolean getStrictType) {
		
		ArrayList<ArrayList<MiscInner.PairIntStr>> usemeOuter = doinCalc ? calcAlertFormats : alertFormats;
		
		for (int art=0,arts = forTy == 0 || getStrictType ? 1 : 2; art < arts;art++) {//if forTy is not sms and if not found for forTy then get the one specified for sms
			ArrayList<MiscInner.PairIntStr> useme = usemeOuter.get(art == 0 ? forTy : 0);
			for (int i=0,is=useme == null ? 0 : useme.size(); i<is; i++)
				if (useme.get(i).first == ty)
					return useme.get(i).second;
		}
		 
		return dontGetDefault ? null : SrcDestInfo.getDefaultAlertFormat(ty, forTy);
	}
	
	public void addAlertFormat(int ty, int forTy, String s) {
		addAlertFormat(ty,forTy, s,false);
	}
	
	public void addAlertFormat(int ty, int forTy, String s, boolean doinCalc) {
		ArrayList<MiscInner.PairIntStr> useme = doinCalc ? calcAlertFormats.get(forTy) : alertFormats.get(forTy);
		boolean added = false;
		for (int i=0,is=useme == null ? 0 : useme.size(); i<is; i++)
			if (useme.get(i).first == ty) {
				useme.get(i).second = s;
				added = true;
				break;
			}
		if (!added) {
			if (useme == null) {
				useme = new ArrayList<MiscInner.PairIntStr>();
				if (doinCalc)
					this.calcAlertFormats.set(forTy, useme);
				else
					this.alertFormats.set(forTy, useme);
			}
			
			useme.add(new MiscInner.PairIntStr(ty, s));
		}
	}
	public ArrayList<AlertSetting> getAlertSetting(int ty) {
		return getAlertSetting(ty,false);
	}
	public ArrayList<AlertSetting> getAlertSettingCalc(Connection conn, int ty) {
		if (conn != null)
			this.calcFromAlertSrcDestId(conn);
		return getAlertSetting(ty,true);
	}
	public ArrayList<AlertSetting> getAlertSetting(int ty, boolean doinCalc) {
		ArrayList<Pair<Integer, ArrayList<AlertSetting>>> useme = doinCalc ? calcAlertSettings : alertSettings;
		for (int i=0,is=useme == null ? 0 : useme.size(); i<is; i++)
			if (useme.get(i).first == ty)
				return useme.get(i).second;
		return null;
	}
	
	public void addAlertSetting(int ty, AlertSetting alertSetting) {
		addAlertSetting(ty, alertSetting, false);
	}
	public void addAlertSetting(int ty, AlertSetting alertSetting, boolean doinCalc) {
		ArrayList<Pair<Integer, ArrayList<AlertSetting>>> useme = doinCalc ? calcAlertSettings : alertSettings;
		ArrayList<AlertSetting> theList = null;
		for (int i=0,is=useme == null ? 0 : useme.size(); i<is; i++)
			if (useme.get(i).first == ty) {
				theList = useme.get(i).second;
				if (theList == null) {
					theList = new ArrayList<AlertSetting>();
					useme.get(i).second = theList;
				}
				break;
			}
		if (theList == null) {
			if (useme == null)
				useme = new ArrayList<Pair<Integer, ArrayList<AlertSetting>>>();
			if (doinCalc)
				calcAlertSettings = useme;
			else
				alertSettings = useme;
			theList = new ArrayList<AlertSetting>();
			useme.add(new Pair<Integer, ArrayList<AlertSetting>>(ty, theList));
		}
		theList.add(alertSetting);
	}
	
	public static ArrayList<Integer> helpGetTimeMinForOtherAlert(ArrayList<AlertSetting> theList) {
		ArrayList<Integer> retval = null;
		if (theList != null && theList.size() > 0) {
			retval = new ArrayList<Integer>();
			for (AlertSetting setting: theList) {
				double d =setting.getDist();
				if (d < 0.005)
					d = 0;
				int dmtr = (int) Math.round(d*60);
				if (dmtr < 0)
					dmtr = 0;
				int insertAfter = -1;
				boolean match = false;
				for (int j=0,js=retval.size();j<js;j++) {
					int e = retval.get(j);
					if (e == dmtr) {//
						match = true;
						break;
					}
					if (e > dmtr) {
						break;
					}
					insertAfter = j;
				}
				if (!match) {
					if (insertAfter == retval.size()-1) {
						retval.add(dmtr);
					}
					else {
						retval.add(insertAfter+1, dmtr);
					}
				}
			}
		}
		return retval;
	}
	public static String getDefault = "select sd.id from src_dest_items sd join port_nodes anc on (anc.id = sd.port_node_id) join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	" where (sd.src_op_station_id is null and sd.src_region_id is null and sd.src_map_longitude is null and sd.src_map_latitude is null and sd.dest_op_station_id is null and sd.dest_region_id is null and sd.dest_map_longitude is null and sd.dest_map_latitude is null) "+
	" order by anc.lhs_number desc limit 1";
	public static int getDefaultSrcDest(Connection conn, int ownerOrgId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(getDefault);
			ps.setInt(1, ownerOrgId);
			rs = ps.executeQuery();
			int retval = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return Misc.getUndefInt();
	}
	public static ArrayList<Integer> helpGetDistForSrcDestAlertAsMtr(ArrayList<AlertSetting> theList) {
		ArrayList<Integer> retval = null;
		if (theList != null && theList.size() > 0) {
			retval = new ArrayList<Integer>();
			for (AlertSetting setting: theList) {
				double d =setting.getDist();
				if (d < 0.005)
					d = 0;
				int dmtr = (int) Math.round(d*1000);
				if (dmtr < 0)
					dmtr = 0;
				int insertAfter = -1;
				boolean match = false;
				for (int j=0,js=retval.size();j<js;j++) {
					int e = retval.get(j);
					if (e == dmtr) {//
						match = true;
						break;
					}
					if (e > dmtr) {
						break;
					}
					insertAfter = j;
				}
				if (!match) {
					if (insertAfter == retval.size()-1) {
						retval.add(dmtr);
					}
					else {
						retval.add(insertAfter+1, dmtr);
					}
				}
			}
		}
		return retval;
	}
	public SrcDestInfo(int id, int reverseId, String name, int portNodeId, int status, int srcId, int srcType, double srcLong, double srcLat, double srcBuffer, String srcName
			, int destId, int destType, double destLong, double destLat, double destBuffer, String destName
			,double transitTime, double transitDist, String notes, double checkContDelayHrFreq, int alertSrcDestId, double notReachThresholdHr, int stoppageRuleId
			) {
		alertFormats = new ArrayList<ArrayList<MiscInner.PairIntStr>>();
		alertFormats.add(null);
		alertFormats.add(null);
		alertFormats.add(null);
		calcAlertFormats = new ArrayList<ArrayList<MiscInner.PairIntStr>>();
		calcAlertFormats.add(null);
		calcAlertFormats.add(null);
		calcAlertFormats.add(null);
		
		this.id = id;
		this.reverseId = reverseId;
		this.name = name;
		this.portNodeId = portNodeId;
		this.status = status;
		this.srcId = srcId;
		this.srcType = srcType;
		this.srcLong = srcLong;
		this.srcLat = srcLat;
		this.srcBuffer = srcBuffer;
		this.srcName = srcName;
		this.destId = destId;
		this.destType = destType;
		this.destLong = destLong;
		this.destLat = destLat;
		this.destBuffer = destBuffer;
		this.destName = destName;
		this.transitDist = transitDist;
		this.transitTime = transitTime;
		this.notes = notes;
		this.checkContDelayHrFreq = checkContDelayHrFreq;
		this.alertSrcDestId = alertSrcDestId;
		this.notReachThresholdHr = notReachThresholdHr;
		this.stoppageRuleId = stoppageRuleId;
	}
	public SrcDestInfo reverse() {
		String newName = name.startsWith("Reverse-")? name.substring(8) : "Reverse-"+name;
		SrcDestInfo retval = new SrcDestInfo(reverseId, id, newName, portNodeId, status, destId, destType, destLong, destLat, destBuffer, destName
				, srcId, srcType, srcLong, srcLat, srcBuffer, srcName
				,transitTime, transitDist, notes,  checkContDelayHrFreq, this.alertSrcDestId, this.notReachThresholdHr,this.stoppageRuleId
				);
		for (int i=0,is=alertFormats.size(); i<is; i++) {
			ArrayList<MiscInner.PairIntStr> list = alertFormats.get(i);
			if (list == null)
				continue;
			ArrayList<MiscInner.PairIntStr> addToList = new ArrayList<MiscInner.PairIntStr>();
			retval.alertFormats.set(i, addToList);
			
			for (int j=0,js=list == null ? null : list.size(); j<js; j++) {
				addToList.add(new MiscInner.PairIntStr(list.get(j).first, list.get(j).second));
			}
		}
		if (this.alertSettings != null) 
			retval.alertSettings = new ArrayList<Pair<Integer, ArrayList<AlertSetting>>>();
		for (int i=0,is=this.alertSettings == null ? 0 : alertSettings.size(); i<is; i++) {
			retval.alertSettings.add(new Pair<Integer, ArrayList<AlertSetting>>(alertSettings.get(i).first, new ArrayList<AlertSetting>()));
			ArrayList<AlertSetting> toList = retval.alertSettings.get(retval.alertSettings.size()-1).second;
			ArrayList<AlertSetting> fromList = this.alertSettings.get(i).second;
			for (int j=0,js=fromList == null ? 0 : fromList.size(); j<js; j++) {
				toList.add(fromList.get(j).clone());
			}
		}
		if (waypoints != null)
			retval.waypoints = new ArrayList<WayPoint>(); 
		for (int i=0,is=waypoints == null ? 0 : waypoints.size();i<is;i++) {
			WayPoint waypoint = waypoints.get(i);
			WayPoint revWaypoint = new WayPoint(waypoint.longitude, waypoint.latitude, waypoint.name, waypoint.buffer, retval.transitTime - waypoint.transit, waypoint.regionId);
			retval.waypoints.add(revWaypoint);
		}
		return retval;
	}
	
	public void addIntermediate(double lon, double lat, String name, double buffer,double transit, int regionId ) {
		WayPoint wp = new WayPoint(lon, lat, name, buffer, transit, regionId);
		if (waypoints == null)
			waypoints = new ArrayList<WayPoint>();
		int pos = waypoints.size()-1;
		if (wp.getTransit() > 0.0005) {
			for (;pos>=0;pos--) {
				if (waypoints.get(pos).getTransit() <= wp.getTransit())
					break;
			}
		}
	    waypoints.add(null);
		for (int j=waypoints.size()-2;j>pos;j--) {
			waypoints.set(j+1, waypoints.get(j));
		}
		waypoints.set(pos+1, wp);
	}
	
	public void addAlert(int srcDestDelType, int alertType, double dist, int contactId, String contactName, String contactPhone, String contactEmail, int alertRole, int startMin, int endMin) {
		AlertSetting as = new AlertSetting(dist, alertType, contactId, contactName, contactPhone, contactEmail, alertRole, startMin, endMin);
		this.addAlertSetting(srcDestDelType, as);
	}
	
	public static ArrayList<SrcDestInfo> getSrcDestList(int srcDestId, int portNodeId, int getDirectOrDescOrAnc, int ofStatus, Connection conn) {//if srcDestId is not undef then portNodeId, ofStatus is ignored, getDirectOrDescOrAnc == 0 => direct, 1=> desc, 2=>anc
		PreparedStatement ps = null;
		PreparedStatement psIntermediate = null;
		PreparedStatement psAlert = null;
		PreparedStatement psFormat = null;
		PreparedStatement psAreaOfOps = null;
		PreparedStatement psRoadSegments = null;
		ResultSet rs = null;
		ArrayList<SrcDestInfo> retval = new ArrayList<SrcDestInfo>();
		try {
			StringBuilder sb = new StringBuilder();
			StringBuilder sbIntermediates = new StringBuilder();
			StringBuilder sbAlerts = new StringBuilder();
			StringBuilder sbFormats = new StringBuilder();
			StringBuilder sbAreaOfOps = new StringBuilder();
			StringBuilder sbRoadSegments = new StringBuilder();
			sb.append("select sd.id, sd.name, sd.port_node_id,sd.status, sd.src_region_id, sd.src_map_name,  sd.src_map_longitude, sd.src_map_latitude, sd.src_map_buffer ")
			    .append(", sd.dest_region_id, sd.dest_map_name,  sd.dest_map_longitude, sd.dest_map_latitude, sd.dest_map_buffer ")
			    .append(", sd.transit_time, sd.transit_dist, sd.notes, sd.reverse_id, sd.cont_check_freq_hr, sd.alert_src_dest_id, sd.src_op_station_id, sd.dest_op_station_id, sd.not_reach_threshold, sd.priority, sd.stoppage_rule_id ")
			;
			sbIntermediates.append("select src_dest_item_id, wp_longitude, wp_latitude, src_dest_intermediates.transit_time, src_dest_intermediates.buffer, src_dest_intermediates.name, src_dest_intermediates.region_id ");
			sbAlerts.append("select src_dest_item_id, src_or_dest_or_delay_type, src_dest_alerts.dist, alert_type, alert_to, contacts.contact_name name, contacts.phone, contacts.email, alert_to_role, start_min, end_min ");
			sbFormats.append("select src_dest_item_id, src_or_dest_or_delay_type, str, for_type ");
			sbAreaOfOps.append("select src_dest_item_id, region_id ");
			sbRoadSegments.append("select src_dest_item_id, new_road_segment_id ");
			if (!Misc.isUndef(srcDestId)) {
				sb.append(" from src_dest_items sd where id = ? ");
				sbIntermediates.append(" from src_dest_intermediates where src_dest_item_id = ? ");
				sbFormats.append(" from src_dest_alert_formats where src_dest_item_id = ? ");
				sbAlerts.append(" from src_dest_alerts left outer join customer_contacts contacts on (contacts.id = alert_to)  where src_dest_item_id = ? ");
				sbAreaOfOps.append(" from src_dest_areaofops   where src_dest_item_id = ? ");
				sbRoadSegments.append(" from src_dest_roadsegs   where src_dest_item_id = ? ");
				sbIntermediates.append(" order by src_dest_item_id, transit_time ");
				sbAlerts.append(" order by src_dest_item_id, src_or_dest_or_delay_type, dist, alert_type, contact_name ");
				sbFormats.append(" order by src_dest_item_id, src_or_dest_or_delay_type ");
				sbAreaOfOps.append(" order by src_dest_item_id, seq ");
				sbRoadSegments.append(" order by src_dest_item_id, seq ");
			}
			else {
				if (getDirectOrDescOrAnc == 0) {
					sb.append(" from src_dest_items sd where sd.port_node_id=?");
					sbIntermediates.append(" from src_dest_items sd join src_dest_intermediates on (sd.id = src_dest_item_id) where sd.port_node_id = ? ");
					sbFormats.append(" from src_dest_items sd join src_dest_alert_formats on (sd.id = src_dest_item_id) where sd.port_node_id = ? ");
					sbAlerts.append(" from src_dest_items sd join src_dest_alerts on (sd.id = src_dest_item_id) left outer join customer_contacts contacts on (contacts.id = alert_to) where sd.port_node_id = ? ");
					sbAreaOfOps.append(" from src_dest_items sd join src_dest_areaofops on (sd.id = src_dest_item_id) where sd.port_node_id = ? ");
					sbFormats.append(" from src_dest_items sd join src_dest_roadsegs on (sd.id = src_dest_item_id) where sd.port_node_id = ? ");
				}
				else if (getDirectOrDescOrAnc == 1) { //desc only
					sb.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = leaf.id) where anc.id = ? ");
					sbIntermediates.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = leaf.id) ")
					                               .append(" join src_dest_intermediates on (sd.id = src_dest_item_id) where anc.id = ?   ");
					sbFormats.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = leaf.id) ")
                    .append(" join src_dest_alert_formats on (sd.id = src_dest_item_id) where anc.id = ?   ");
					sbAlerts.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = leaf.id) ")
													.append(" join  src_dest_alerts on (sd.id = src_dest_item_id) left outer join customer_contacts contacts on (contacts.id = alert_to) where anc.id = ?  ");
					sbAreaOfOps.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = leaf.id) ")
                    .append(" join src_dest_areaofops on (sd.id = src_dest_item_id) where anc.id = ?   ");
					sbRoadSegments.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = leaf.id) ")
                    .append(" join src_dest_roadsegs on (sd.id = src_dest_item_id) where anc.id = ?   ");
				}
				else if (getDirectOrDescOrAnc == 2) { //anc only
					sb.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = anc.id) and leaf.id = ? ");
					sbIntermediates.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = anc.id) ")
													.append(" join src_dest_intermediates on (sd.id = src_dest_item_id) where leaf.id = ? ");
					sbFormats.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = anc.id) ")
					.append(" join src_dest_alert_formats on (sd.id = src_dest_item_id) where leaf.id = ? ");

					sbAlerts.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = anc.id) ")
					.append(" join  src_dest_alerts on (sd.id = src_dest_item_id) left outer join customer_contacts contacts on (contacts.id = alert_to) where leaf.id = ?  ");
					sbAreaOfOps.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = anc.id) ")
					.append(" join src_dest_areaofops on (sd.id = src_dest_item_id) where leaf.id = ? ");
					sbRoadSegments.append(" from port_nodes anc join port_nodes leaf on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join src_dest_items sd on (sd.port_node_id = anc.id) ")
					.append(" join src_dest_roadsegs on (sd.id = src_dest_item_id) where leaf.id = ? ");

					
				}
				if (!Misc.isUndef(ofStatus)) {
					sb.append(" and sd.status = ? ");
					sbIntermediates.append(" and sd.status = ? ");
					sbFormats.append(" and sd.status = ? ");
					sbAlerts.append(" and sd.status = ? ");
					sbAreaOfOps.append(" and sd.status = ? ");
					sbRoadSegments.append(" and sd.status = ? ");
				}
				sb.append(" order by sd.status desc, sd.id desc ");
				sbIntermediates.append(" order by src_dest_item_id, transit_time ");
				sbAlerts.append(" order by src_dest_item_id, src_or_dest_or_delay_type, dist, alert_type, contact_name ");
				sbFormats.append(" order by src_dest_item_id, src_or_dest_or_delay_type ");
				sbAreaOfOps.append(" order by src_dest_item_id, src_dest_areaofops.seq ");
				sbRoadSegments.append(" order by src_dest_item_id, src_dest_roadsegs.seq ");
	
			}
			ps = conn.prepareStatement(sb.toString());
			psIntermediate = conn.prepareStatement(sbIntermediates.toString());
			psAlert = conn.prepareStatement(sbAlerts.toString());
			psFormat = conn.prepareStatement(sbFormats.toString());
			psAreaOfOps = conn.prepareStatement(sbAreaOfOps.toString());
			psRoadSegments = conn.prepareStatement(sbRoadSegments.toString());
			if (!Misc.isUndef(srcDestId)) {
				ps.setInt(1, srcDestId);
				psIntermediate.setInt(1, srcDestId);
				psAlert.setInt(1, srcDestId);
				psFormat.setInt(1, srcDestId);
				psAreaOfOps.setInt(1, srcDestId);
				psRoadSegments.setInt(1, srcDestId);
			}
			else {
				ps.setInt(1, portNodeId);
				psIntermediate.setInt(1, portNodeId);
				psAlert.setInt(1, portNodeId);
				psFormat.setInt(1, portNodeId);
				psAreaOfOps.setInt(1, portNodeId);
				psRoadSegments.setInt(1, portNodeId);
				if (!Misc.isUndef(ofStatus)) {
					ps.setInt(2, ofStatus);
					psIntermediate.setInt(2, ofStatus);
					psAlert.setInt(2, ofStatus);
					psFormat.setInt(2, ofStatus);
					psAreaOfOps.setInt(2, ofStatus);
					psRoadSegments.setInt(2, ofStatus);
				}
			}
			rs = ps.executeQuery();
			HashMap<Integer, SrcDestInfo> lookup = new HashMap<Integer, SrcDestInfo>();
			while (rs.next()) {
				int id = rs.getInt("id");
				int reverseId = Misc.getRsetInt(rs, "reverse_id");
				String name = rs.getString("name");
				int portId = Misc.getRsetInt(rs, "port_node_id", Misc.G_TOP_LEVEL_PORT);
				int itemStatus = Misc.getRsetInt(rs, "status", 1);
				int srcId = Misc.getRsetInt(rs, "src_region_id");
				int srcOpId = Misc.getRsetInt(rs, "src_op_station_id");
				int srcType = G_REGION_TYPE;
				if (!Misc.isUndef(srcOpId)) {
					srcId = srcOpId;
					srcType = G_OP_STATION_TYPE;
				}
				double srcBuffer = Misc.getUndefDouble();
				double srcLong = Misc.getUndefDouble();
				double srcLat = Misc.getUndefDouble();
				String srcName = null;
				if (Misc.isUndef(srcId)) {
					srcType = G_MAP_TYPE;
					srcLong = Misc.getRsetDouble(rs, "src_map_longitude");
					srcLat = Misc.getRsetDouble(rs, "src_map_latitude");
					srcName = Misc.getRsetString(rs, "src_map_name");
					srcBuffer = Misc.getRsetDouble(rs, "src_map_buffer");
				}
				
				int destId = Misc.getRsetInt(rs, "dest_region_id");
				int destOpId = Misc.getRsetInt(rs, "dest_op_station_id");
				int destType = G_REGION_TYPE;
				if (!Misc.isUndef(destOpId)) {
					destId = destOpId;
					destType = G_OP_STATION_TYPE;
				}
				double destBuffer = Misc.getUndefDouble();
				double destLong = Misc.getUndefDouble();
				double destLat = Misc.getUndefDouble();
				String destName = null;
				if (Misc.isUndef(destId)) {
					destType = G_MAP_TYPE;
					destLong = Misc.getRsetDouble(rs, "dest_map_longitude");
					destLat = Misc.getRsetDouble(rs, "dest_map_latitude");
					destName = Misc.getRsetString(rs, "dest_map_name");
					destBuffer = Misc.getRsetDouble(rs, "dest_map_buffer");
				}
				double transitTime = Misc.getRsetDouble(rs, "transit_time");
				double transitDist  = Misc.getRsetDouble(rs, "transit_dist");
				String notes = Misc.getRsetString(rs, "notes");
				double contCheckFreq = Misc.getRsetDouble(rs, "cont_check_freq_hr");
				int alertSrcDestId = Misc.getRsetInt(rs, "alert_src_dest_id");
				double notReachThresholdHr = Misc.getRsetDouble(rs, "not_reach_threshold");
				int stoppageRuleId = Misc.getRsetInt(rs, "stoppage_rule_id", 1);
				SrcDestInfo srcDestInfo = new SrcDestInfo(id, reverseId, name, portId, itemStatus, srcId, srcType, srcLong, srcLat, srcBuffer, srcName
						, destId, destType, destLong, destLat, destBuffer, destName
						,transitTime, transitDist, notes, contCheckFreq, alertSrcDestId, notReachThresholdHr,stoppageRuleId
						);
				int priority = Misc.getRsetInt(rs, "priority",0);
				srcDestInfo.setPriority(priority);
				
				lookup.put(id, srcDestInfo);
				retval.add(srcDestInfo);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			int prevSrcDestId = Misc.getUndefInt();
			SrcDestInfo srcDestInfo = null;
			rs = psIntermediate.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("src_dest_item_id");
				if (id != prevSrcDestId) {
					srcDestInfo = lookup.get(id);
					prevSrcDestId = id;
				}
				if (srcDestInfo == null)
					continue;
				srcDestInfo.addIntermediate(Misc.getRsetDouble(rs, "wp_longitude"), Misc.getRsetDouble(rs, "wp_latitude"),rs.getString("name"),Misc.getRsetDouble(rs, "buffer"), Misc.getRsetDouble(rs, "transit_time"), Misc.getRsetInt(rs, "region_id"));
			}
			rs = Misc.closeRS(rs);
			psIntermediate = Misc.closePS(psIntermediate);
			
			prevSrcDestId = Misc.getUndefInt();
			srcDestInfo = null;
			rs = psFormat.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("src_dest_item_id");
				if (id != prevSrcDestId) {
					srcDestInfo = lookup.get(id);
					prevSrcDestId = id;
				}
				if (srcDestInfo == null)
					continue;
				srcDestInfo.addAlertFormat(rs.getInt(2), rs.getInt(4), rs.getString(3));
			}
			rs = Misc.closeRS(rs);
			psFormat = Misc.closePS(psFormat);
			
			prevSrcDestId = Misc.getUndefInt();
			srcDestInfo = null;
			rs = psAreaOfOps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("src_dest_item_id");
				if (id != prevSrcDestId) {
					srcDestInfo = lookup.get(id);
					prevSrcDestId = id;
				}
				if (srcDestInfo == null)
					continue;
				srcDestInfo.addAreaOfOp(rs.getInt(2));
			}
			rs = Misc.closeRS(rs);
			psAreaOfOps = Misc.closePS(psAreaOfOps);

			prevSrcDestId = Misc.getUndefInt();
			srcDestInfo = null;
			rs = psRoadSegments.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("src_dest_item_id");
				if (id != prevSrcDestId) {
					srcDestInfo = lookup.get(id);
					prevSrcDestId = id;
				}
				if (srcDestInfo == null)
					continue;
				srcDestInfo.addRoadSegment(rs.getInt(2));
			}
			rs = Misc.closeRS(rs);
			psRoadSegments = Misc.closePS(psRoadSegments);
			
			prevSrcDestId = Misc.getUndefInt();
			srcDestInfo = null;
			rs = psAlert.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("src_dest_item_id");
				if (id != prevSrcDestId) {
					srcDestInfo = lookup.get(id);
					prevSrcDestId = id;
				}
				if (srcDestInfo == null)
					continue;
				srcDestInfo.addAlert(Misc.getRsetInt(rs, "src_or_dest_or_delay_type"), Misc.getRsetInt(rs, "alert_type"), Misc.getRsetDouble(rs, "dist"), Misc.getRsetInt(rs, "alert_to"), rs.getString("name"), rs.getString("phone"), rs.getString("email"),Misc.getRsetInt(rs, "alert_to_role"), Misc.getRsetInt(rs, "start_min"), Misc.getRsetInt(rs,"end_min"));
			}
			rs = Misc.closeRS(rs);
			psAlert = Misc.closePS(psAlert);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			psIntermediate = Misc.closePS(psIntermediate);
			psAlert = Misc.closePS(psAlert);
			psFormat = Misc.closePS(psFormat);

		}
		return retval;
	}

	public static void save(SrcDestInfo srcDestInfo, Connection conn) throws Exception {
		try {
			//simplified ... no copy of older stuff ... instead we will change the details of NewVehicleData
			SrcDestInfo old = SrcDestInfo.getSrcDestInfo(conn, srcDestInfo.getId());
			
			boolean isNew = Misc.isUndef(srcDestInfo.getId());
			
			if (isNew) {//create placeholder so that we can do one code for update
				PreparedStatement ps = conn.prepareStatement("insert into src_dest_items(name) values(?)");
				ps.setString(1, srcDestInfo.getName());
				ps.execute();
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					srcDestInfo.setId(rs.getInt(1));
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}
		
			//update the base table // handles only regions and maps(means landmarks : user defined and general map)
			final String q = "update src_dest_items set name=?, port_node_id=?, status=?, src_region_id=?, src_map_name=?,  src_map_longitude = ?, src_map_latitude = ?, src_map_buffer =?"+
		    ", dest_region_id=?, dest_map_name=?,  dest_map_longitude=?, dest_map_latitude=?, dest_map_buffer=? "+
		    ",transit_time=?, transit_dist=?, notes=?, is_artificial=?, src_op_station_id=?, dest_op_station_id=?, cont_check_freq_hr=?, alert_src_dest_id=?,not_reach_threshold=?,priority=?, stoppage_rule_id=? where id = ?";
			PreparedStatement ps = conn.prepareStatement(q);
			int index = 1;
			ps.setString(index++, srcDestInfo.getName());
			Misc.setParamInt(ps, srcDestInfo.getPortNodeId(), index++);
			Misc.setParamInt(ps, srcDestInfo.getStatus(), index++);  
			if (srcDestInfo.getSrcType() == G_REGION_TYPE || srcDestInfo.getSrcType() == G_OP_STATION_TYPE) {
				Misc.setParamInt(ps, srcDestInfo.getSrcType() == G_REGION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt(), index++);
				ps.setString(index++, null);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), index++);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), index++);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), index++);
			}
			else {
				Misc.setParamInt(ps, Misc.getUndefInt(), index++);
				ps.setString(index++, srcDestInfo.getSrcName());
				Misc.setParamDouble(ps, srcDestInfo.getSrcLong(), index++);
				Misc.setParamDouble(ps, srcDestInfo.getSrcLat(), index++);
				Misc.setParamDouble(ps, srcDestInfo.getSrcBuffer(), index++);
			}
			
			if (srcDestInfo.getDestType() == G_REGION_TYPE || srcDestInfo.getDestType() == G_OP_STATION_TYPE) {
				Misc.setParamInt(ps, srcDestInfo.getDestType() == G_REGION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt(), index++);
				ps.setString(index++, null);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), index++);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), index++);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), index++);
			}
			else {
				Misc.setParamInt(ps, Misc.getUndefInt(), index++);
				ps.setString(index++, srcDestInfo.getDestName());
				Misc.setParamDouble(ps, srcDestInfo.getDestLong(), index++);
				Misc.setParamDouble(ps, srcDestInfo.getDestLat(), index++);
				Misc.setParamDouble(ps, srcDestInfo.getDestBuffer(), index++);
			}
			//",transit_time=?, transit_dist=?, freq_check=?, check_at=?, under_travel=?, notes=? where id = ?";
			Misc.setParamDouble(ps, srcDestInfo.getTransitTime(), index++);
			Misc.setParamDouble(ps, srcDestInfo.getTransitDist(), index++);
			ps.setString(index++, srcDestInfo.getNotes());
			ps.setInt(index++, (!Misc.isUndef(srcDestInfo.getDestId()) && srcDestInfo.getDestId() == 2) ? 1 : 0 );
			Misc.setParamInt(ps, srcDestInfo.getSrcType() == G_OP_STATION_TYPE ? srcDestInfo.getSrcId() : Misc.getUndefInt(), index++);
			Misc.setParamInt(ps, srcDestInfo.getDestType() == G_OP_STATION_TYPE ? srcDestInfo.getDestId() : Misc.getUndefInt(), index++);
			Misc.setParamDouble(ps, srcDestInfo.getCheckContDelayHrFreq(), index++);
			Misc.setParamInt(ps, srcDestInfo.alertSrcDestId, index++);
			Misc.setParamDouble(ps, srcDestInfo.notReachThresholdHr, index++);
			Misc.setParamDouble(ps, srcDestInfo.priority, index++);
			Misc.setParamInt(ps, srcDestInfo.getStoppageRuleId(), index++);
			ps.setInt(index++, srcDestInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
			
			//Now delete existing alert set up ... and reinsert
			ps = conn.prepareStatement("delete from src_dest_alerts where src_dest_item_id = ?");
			ps.setInt(1, srcDestInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("insert into src_dest_alerts (src_dest_item_id, src_or_dest_or_delay_type, dist, alert_type, alert_to, alert_to_role, start_min, end_min) values (?,?,?,?,?,?,?,?)");
			ps.setInt(1, srcDestInfo.getId());
			ArrayList<Pair<Integer, ArrayList<AlertSetting>>> outerList = srcDestInfo.alertSettings;
			for (int art=0,arts = outerList == null ? 0 : outerList.size();art<arts;art++) {
				int srcDestDelayType = outerList.get(art).first;
				ArrayList<AlertSetting> alertList = outerList.get(art).second;
				ps.setInt(2, srcDestDelayType);
				for (int i=0,is = alertList == null ? 0 : alertList.size();i<is;i++) {
					double dist = alertList.get (i).getDist();
					int alertTo = alertList.get(i).getContactId();
					int alertType = alertList.get(i).getAlertType();
					if (Misc.isUndef(dist))
						dist = 0;
					if (Misc.isUndef(alertType))
						continue;
					Misc.setParamDouble(ps, dist, 3);
					ps.setInt(4, alertType);
					Misc.setParamInt(ps, alertTo, 5);
					Misc.setParamInt(ps, alertList.get(i).getAlertRole(), 6);
					Misc.setParamInt(ps, alertList.get(i).getStartMin(), 7);
					Misc.setParamInt(ps, alertList.get(i).getEndMin(), 8);
					ps.addBatch();
				}
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);
			
			//Now delete existing intermediates ... and reinsert
			ps = conn.prepareStatement("delete from src_dest_intermediates where src_dest_item_id = ?");
			ps.setInt(1, srcDestInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("insert into src_dest_intermediates (src_dest_item_id, wp_longitude, wp_latitude, transit_time, buffer, name, region_id) values (?,?,?,?,?,?,?)");
			ps.setInt(1, srcDestInfo.getId());
			ArrayList<WayPoint> wpList = srcDestInfo.getWaypoints();
			for (int i=0,is = wpList == null ? 0 : wpList.size();i<is;i++) {
				double lon = wpList.get(i).getLongitude();
				double lat = wpList.get(i).getLatitude();
				double transitTime = wpList.get(i).getTransit();
				double buffer = wpList.get(i).getBuffer();
				String name = wpList.get(i).getName();
				int regionId = wpList.get(i).getRegionId();
				if (Misc.isUndef(regionId) && (Misc.isUndef(lon) || Misc.isUndef(lat)))
					continue;
				Misc.setParamDouble(ps, lon, 2);
				Misc.setParamDouble(ps, lat, 3);
				Misc.setParamDouble(ps, transitTime, 4);
				Misc.setParamDouble(ps, buffer, 5);
				ps.setString(6, name);
				Misc.setParamInt(ps, regionId, 7);
				ps.addBatch();
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);
			
			//Now delete existing area of ops
			ps = conn.prepareStatement("delete from src_dest_areaofops where src_dest_item_id = ?");
			ps.setInt(1, srcDestInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("insert into src_dest_areaofops (src_dest_item_id, region_id, seq) values (?,?,?)");
			ps.setInt(1, srcDestInfo.getId());
			ArrayList<Integer> areaOfOps = srcDestInfo.getAreaOfOpRegions();
			for (int i=0,is = areaOfOps == null ? 0 : areaOfOps.size();i<is;i++) {
				int regid = areaOfOps.get(i);
				if (Misc.isUndef(regid))
					continue;
					ps.setInt(2, regid);
					ps.setInt(3, i);
					ps.addBatch();
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);
			
			//Now delete existing roadSegments
			ps = conn.prepareStatement("delete from src_dest_roadsegs where src_dest_item_id = ?");
			ps.setInt(1, srcDestInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("insert into src_dest_roadsegs (src_dest_item_id, new_road_segment_id, seq) values (?,?,?)");
			ps.setInt(1, srcDestInfo.getId());
			ArrayList<Integer> roadSegments = srcDestInfo.getRoadSegments();
			for (int i=0,is = roadSegments == null ? 0 : roadSegments.size();i<is;i++) {
				int regid = roadSegments.get(i);
				if (Misc.isUndef(regid))
					continue;
					ps.setInt(2, regid);
					ps.setInt(3, i);
					ps.addBatch();
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);

			//Now delete existing alert formats ... and reinsert
			ps = conn.prepareStatement("delete from src_dest_alert_formats where src_dest_item_id = ?");
			ps.setInt(1, srcDestInfo.getId());
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("insert into src_dest_alert_formats (src_dest_item_id, src_or_dest_or_delay_type, str, for_type) values (?,?,?,?)");
			ps.setInt(1, srcDestInfo.getId());
			ArrayList<ArrayList<MiscInner.PairIntStr>> listOfAlerts = srcDestInfo.alertFormats;
			for (int j=0,js=listOfAlerts == null ? 0 : listOfAlerts.size();j<js; j++) {
				ArrayList<MiscInner.PairIntStr> theList = listOfAlerts.get(j);
				for (int i=0,is = theList == null ? 0 : theList.size();i<is;i++) {
					int ty = theList.get(i).first;
					String str = theList.get(i).second;
					if (str != null)
						str = str.trim();
					if (str != null && str.length() == 0)
						str = null;
					if (Misc.isUndef(ty) || str == null)
						continue;
					ps.setInt(2, ty);
					ps.setString(3, str);
					ps.setInt(4, j);
					ps.addBatch();
				}
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);
			if (srcDestInfo.getStatus() != 1) {
				SrcDestInfo.g_srcDestInfoCache.remove(srcDestInfo.getId());
				srcDestInfo = null;
			}
			else {
				SrcDestInfo.g_srcDestInfoCache.put(srcDestInfo.getId(), srcDestInfo);
			}
			SrcDestHelper.handleChange(conn, old, srcDestInfo, false);//update vehicleETA as needed
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPortNodeId() {
		return portNodeId;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSrcId() {
		return srcId;
	}

	public void setSrcId(int srcId) {
		this.srcId = srcId;
	}

	public int getSrcType() {
		return srcType;
	}

	public void setSrcType(int srcType) {
		this.srcType = srcType;
	}

	public double getSrcLong() {
		return srcLong;
	}

	public void setSrcLong(double srcLong) {
		this.srcLong = srcLong;
	}

	public double getSrcLat() {
		return srcLat;
	}

	public void setSrcLat(double srcLat) {
		this.srcLat = srcLat;
	}

	public double getSrcBuffer() {
		return srcBuffer;
	}

	public void setSrcBuffer(double srcBuffer) {
		this.srcBuffer = srcBuffer;
	}

	public int getDestId() {
		return destId;
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public int getDestType() {
		return destType;
	}

	public void setDestType(int destType) {
		this.destType = destType;
	}

	public double getDestLong() {
		return destLong;
	}

	public void setDestLong(double destLong) {
		this.destLong = destLong;
	}

	public double getDestLat() {
		return destLat;
	}

	public void setDestLat(double destLat) {
		this.destLat = destLat;
	}

	public double getDestBuffer() {
		return destBuffer;
	}

	public void setDestBuffer(double destBuffer) {
		this.destBuffer = destBuffer;
	}

	public double getTransitTime() {
		return transitTime;
	}

	public void setTransitTime(double transitTime) {
		this.transitTime = transitTime;
	}

	public double getTransitDist() {
		return transitDist;
	}

	public void setTransitDist(double transitDist) {
		this.transitDist = transitDist;
	}



	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public ArrayList<WayPoint> getWaypoints() {
		return waypoints;
	}

		public String getSrcName() {
		return srcName;
	}
	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}
	public String getDestName() {
		return destName;
	}
	public void setDestName(String destName) {
		this.destName = destName;
	}
	
	
	public int getReverseId() {
		return reverseId;
	}
	public void setReverseId(int reverseId) {
		this.reverseId = reverseId;
	}
	
	public synchronized static void loadSrcDestInfo(Connection conn, ArrayList<Integer> itemList)  {
		boolean localConn = conn == null;
		boolean destroyConn = false;
		//when loading in cache dont impact VehicleETA ... instead will be done in one shot for all vehicles ..
		try {
			if (localConn)
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			
			if (itemList == null || itemList.size() == 0) {
				ArrayList<SrcDestInfo> srcList = SrcDestInfo.getSrcDestList(Misc.getUndefInt(), Misc.G_TOP_LEVEL_PORT, 1, 1, conn);
				for (int i=0,is = srcList.size();i<is;i++) {
					SrcDestInfo item = srcList.get(i);
					SrcDestInfo old = g_srcDestInfoCache.get(item.getId());
					g_srcDestInfoCache.put(item.getId(), item);
					SrcDestHelper.handleChange(conn, old, item, true);
				}
			}
			else {
				for (int i=0,is = itemList.size();i<is;i++) {
					ArrayList<SrcDestInfo> srcList = SrcDestInfo.getSrcDestList(itemList.get(i), Misc.G_TOP_LEVEL_PORT, 1, 1, conn);
					SrcDestInfo item = srcList.get(i);
					SrcDestInfo old = g_srcDestInfoCache.get(item.getId());
					if ((srcList == null || srcList.size() == 0 || srcList.get(0).getStatus() != 1) && g_srcDestInfoCache.contains(item.getId())) {
						g_srcDestInfoCache.remove(item.getId());
					}
					else {
						g_srcDestInfoCache.put(item.getId(), item);
					}
					SrcDestHelper.handleChange(conn, old, item, true);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyConn = true;
		}
		finally {
			if (localConn) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyConn);
				}
				catch (Exception e) {
					
				}
			}
		}
	}
	public static SrcDestInfo getSrcDestInfo(Connection conn, int srcDestId)  {
		if (g_srcDestInfoCache == null) {
			try {
				if(!LocalNameHelperRTree.isRTreeLoaded()){
					ReadShapeFile.loadRTree(conn);
					LocalNameHelperRTree.setRTreeLoaded();
				}
			}
			catch (Exception e) {
				
			}
			g_srcDestInfoCache = new ConcurrentHashMap<Integer, SrcDestInfo>();
			loadSrcDestInfo(conn, null);
		}
		return g_srcDestInfoCache.get(srcDestId);
	}
	public double getSrcRadius(Connection conn) {
		try {
			if (Misc.isUndef(srcRadius)) {
				srcRadius = srcBuffer;
				int regId = this.srcId;
				if (this.srcType == G_OP_STATION_TYPE) {
					OpStationBean opbean = TripInfoCacheHelper.getOpStation(regId);
					if (opbean != null)
						regId = opbean.getGateAreaId();
				}
				if (this.srcType == G_OP_STATION_TYPE || srcType == G_REGION_TYPE) {
					RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regId, conn);
					if (rth != null) {
						double d1 = Point.fastGeoDistance(rth.region.m_llCoord.getX(), rth.region.m_llCoord.getY(), rth.region.m_urCoord.getX(), rth.region.m_urCoord.getY());
						srcRadius = d1/2;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return srcRadius;
	}
	public double getDestRadius(Connection conn) {
		try {
			if (Misc.isUndef(destRadius)) {
				destRadius = destBuffer;
				int regId = this.destId;
				if (this.destType == G_OP_STATION_TYPE) {
					OpStationBean opbean = TripInfoCacheHelper.getOpStation(regId);
					if (opbean != null)
						regId = opbean.getGateAreaId();
				}
				if (this.srcType == G_OP_STATION_TYPE || srcType == G_REGION_TYPE) {
					RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(regId, conn);
					if (rth != null) {
						double d1 = Point.fastGeoDistance(rth.region.m_llCoord.getX(), rth.region.m_llCoord.getY(), rth.region.m_urCoord.getX(), rth.region.m_urCoord.getY());
						destRadius = d1/2;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return destRadius;
	}
	public int compareTo(Object o) {
		SrcDestInfo rhs  = (SrcDestInfo) o;
		int rhsId = rhs.getId();
		return id < rhsId ? -1 : id == rhsId ? 0 : 1;
	}
	
	public void setWaypoints(ArrayList<WayPoint> waypoints) {
		this.waypoints = waypoints;
	}
	
	public double getCheckContDelayHrFreq() {
		return checkContDelayHrFreq;
	}

	public void setCheckContDelayHrFreq(double checkContDelayHrFreq) {
		this.checkContDelayHrFreq = checkContDelayHrFreq;
	}
	public double getNotReachThresholdHr() {
		return notReachThresholdHr;
	}
	public void setNotReachThresholdHr(double notReachThresholdHr) {
		this.notReachThresholdHr = notReachThresholdHr;
	}
	public int getAlertSrcDestId() {
		return alertSrcDestId;
	}
	public void setAlertSrcDestId(int alertSrcDestId) {
		this.alertSrcDestId = alertSrcDestId;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public ArrayList<Integer> getAreaOfOpRegions() {
		return areaOfOpRegions;
	}
	public void setAreaOfOpRegions(ArrayList<Integer> areaOfOpRegions) {
		this.areaOfOpRegions = areaOfOpRegions;
	}
	public ArrayList<Integer> getRoadSegments() {
		return roadSegments;
	}
	public void setRoadSegments(ArrayList<Integer> roadSegments) {
		this.roadSegments = roadSegments;
	}
	public int getStoppageRuleId() {
		return stoppageRuleId;
	}
	public void setStoppageRuleId(int stoppageRuleId) {
		this.stoppageRuleId = stoppageRuleId;
	}
	public double getCalcMaxNearSrcDist() {
		return calcMaxNearSrcDist;
	}
	public void setCalcMaxNearSrcDist(double calcMaxNearSrcDist) {
		this.calcMaxNearSrcDist = calcMaxNearSrcDist;
	}
}

package com.ipssi.manualTrip.sand;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class Requirements implements Comparable {
	public ArrayList<SimpleTripData> options = new ArrayList<SimpleTripData>();
	public int sameDateEndIndexExcl = -1;
	public int sameDateTotCount = 0;
	private int id = Misc.getUndefInt();
	private int portNodeId = Misc.getUndefInt();
	private String vehicleName;
	private String dateStr;
	private String fromStr;
	private String toStr;
	private int count;
	private String stdName;
	private int vehicleId = Misc.getUndefInt();
	private long ts = Misc.getUndefInt();
	private int fromId = Misc.getUndefInt();
	private int toId = Misc.getUndefInt();
	private int fromId2 = Misc.getUndefInt();
	private int toId2 = Misc.getUndefInt();
	private boolean loadChangeAllowed = true;
	private boolean unloadChangeAllowed = true;
	private boolean materialChangeAllowed = false;
	private int materialId = Misc.getUndefInt();
	private ArrayList<Integer> tripId = new ArrayList<Integer>();
	public boolean hasFullDate() {
		java.util.Date dt = new java.util.Date(ts);
		return dt.getHours() != 0 || dt.getMinutes() != 0 || dt.getSeconds() != 0;
	}
	private ArrayList<TripCreateStrategy> tripPlan = new ArrayList<TripCreateStrategy>();
	public static class TripCreateStrategy {
		public static int G_TO_MAP = 0;
		public static int G_CHANGE_LOAD = 1;
		public static int G_CHANGE_UNLOAD = 2;
		public static int G_CREATE_FULL = 3;
		public static int G_EXTEND_TO_U = 4;
		public static int G_EXTEND_TO_L = 5;
		public static int G_DELETE_THIS = 6;
		public static int G_UNKNOWN = 7;
		public static int G_BREAK_TRIP = 8;
		public static int G_CHANGE_L_EXTEND = 9;
		public static int G_CHANGE_U_EXTEND = 10;
		public static int G_ALREADY_MAPPED = 11;
		private SimpleTripData base = null;
		private int strategy = G_UNKNOWN; //
		private long targetDate = Misc.getUndefInt();
		private ReferenceData referenceData = null;
		private ArrayList<SimpleTripData> boundingTripsPlus = null;
		private String addnlParam = null;
		public String getRelevantBoundingTripInfo() {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			
			if (this.strategy == Requirements.TripCreateStrategy.G_TO_MAP || this.strategy == Requirements.TripCreateStrategy.G_CHANGE_LOAD || this.strategy == Requirements.TripCreateStrategy.G_CHANGE_UNLOAD) {
			}//do nothing
			else if (this.strategy == Requirements.TripCreateStrategy.G_EXTEND_TO_U && referenceData != null) {
				this.addBoundTripInfo(sb, sdfFull);
			}
			else if (this.strategy == Requirements.TripCreateStrategy.G_EXTEND_TO_L && referenceData != null) {
				this.addBoundTripInfo(sb, sdfFull);
			}
			else if (this.strategy == Requirements.TripCreateStrategy.G_CREATE_FULL && referenceData != null) {
				this.addBoundTripInfo(sb, sdfFull);
			}
			else if (this.strategy == Requirements.TripCreateStrategy.G_UNKNOWN) {
				this.addBoundTripInfo(sb, sdfFull);			
			}
			return sb.toString();
		}
		public Pair<Pair<Long, Long>, Pair<Long, Long>> getEstTimings() {
			long lgin = Misc.getUndefInt();
			long lgout = Misc.getUndefInt();
			long ugin = Misc.getUndefInt();
			long ugout = Misc.getUndefInt();
			if (this != null && this.getReferenceData() != null) {
				ReferenceData referenceData = this.getReferenceData();
				SimpleTripData trip = this.getBase();
				if (this.strategy == Requirements.TripCreateStrategy.G_TO_MAP || this.strategy == Requirements.TripCreateStrategy.G_CHANGE_LOAD || this.strategy == Requirements.TripCreateStrategy.G_CHANGE_UNLOAD) {
					lgin = trip.getLgin();
					lgout = trip.getLgout();
					ugin = trip.getUgin();
					ugout = trip.getUgout();
				}
				else if (this.strategy == Requirements.TripCreateStrategy.G_EXTEND_TO_U && referenceData != null) {
					lgin = trip.getLgin();
					lgout = trip.getLgout();
					ugin = lgout+(referenceData.getUgin()-referenceData.getLgout());
					ugout = ugin + (referenceData.getUgout()-referenceData.getUgin());
				}
				else if (this.strategy == Requirements.TripCreateStrategy.G_EXTEND_TO_L && referenceData != null) {
					ugin = trip.getUgin();
					ugout = trip.getUgout();
					lgout = ugin-(referenceData.getUgin()-referenceData.getLgout());
					lgin = lgout - (referenceData.getLgout()-referenceData.getLgin());
				}
				else if (this.strategy == Requirements.TripCreateStrategy.G_CREATE_FULL && referenceData != null) {
					ugin = this.getTargetDate();
					ugout = ugin + (referenceData.getUgout()-referenceData.getUgin());
					lgout = ugin-(referenceData.getUgin()-referenceData.getLgout());
					lgin = lgout - (referenceData.getLgout()-referenceData.getLgin());
				}
			}
			return new Pair<Pair<Long, Long>, Pair<Long, Long>>(new Pair<Long, Long>(lgin, lgout), new Pair<Long,Long> (ugin, ugout));
		}
		private void addBoundTripInfo(StringBuilder sb, SimpleDateFormat sdfFull) {
			for (int i=0,is= this.boundingTripsPlus == null ? 0 : this.boundingTripsPlus.size(); i<is; i++) {
				SimpleTripData tr = this.boundingTripsPlus.get(i);
				if (i != 0)
					sb.append("<br/>");
				if (tr.getEarliest() <= 0)
					continue;
				sb.append(tr.getTripId()).append(tr.getLgin() > 0 || tr.getLgout() > 0? " L:" : " U:").append(sdfFull.format(Misc.longToUtilDate(tr.getEarliest())));
				sb.append("--");
				sb.append(sdfFull.format(Misc.longToUtilDate(tr.getLatest())));
				sb.append(tr.getUgout() > 0 || tr.getUgin() > 0 ? ":U":":L");
				sb.append(";").append(tr.getAssignedToReq());
			}
		}
		
		public SimpleTripData getBase() {
			return base;
		}
		public void setBase(SimpleTripData base) {
			this.base = base;
		}
		public int getStrategy() {
			return strategy;
		}
		public void setStrategy(int strategy) {
			this.strategy = strategy;
		}
		public long getTargetDate() {
			return targetDate;
		}
		public void setTargetDate(long targetDate) {
			this.targetDate = targetDate;
		}
		public TripCreateStrategy(SimpleTripData base, int strategy, long targetDate, ReferenceData referenceData, ArrayList<SimpleTripData> boundingTripsPlus, String addnlParam) {
			super();
			this.base = base;
			this.strategy = strategy;
			this.targetDate = targetDate;
			this.referenceData = referenceData;
			this.boundingTripsPlus = boundingTripsPlus;
			this.addnlParam = addnlParam;
		}
		public ReferenceData getReferenceData() {
			return referenceData;
		}
		public void setReferenceData(ReferenceData referenceData) {
			this.referenceData = referenceData;
		}
		public String getAddnlParam() {
			return addnlParam;
		}
		public void setAddnlParam(String addnlParam) {
			this.addnlParam = addnlParam;
		}
		
	}//end of class
	public void resetStrategy() {
		this.tripPlan.clear();
	}
	public void addStrategy(SimpleTripData base, int strategy, long targetDate, ReferenceData referenceData, ArrayList<SimpleTripData> boundingTripsPlus, String addnlParam) {
		TripCreateStrategy item = new TripCreateStrategy(base, strategy, targetDate, referenceData, boundingTripsPlus, addnlParam);
		this.tripPlan.add(item);
	}
	
	public String toString() {
		return getUserDataString()+ " Processed:"+getProcessedDataString(null);
	}
	public String getUserDataString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.vehicleName).append(",").append(dateStr).append(",").append(fromStr).append(",").append(toStr).append(",").append(count)
		;
		return sb.toString();
	}
	public String getProcessedDataString(Connection conn) {
		StringBuilder sb = new StringBuilder();
		String dbVeh = null;
		String dbFromId = null;
		String dbToId = null;
		if (conn != null) {
			try {
				CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(this.vehicleId, conn);
				if (vehSetup != null)
					dbVeh = vehSetup.m_name;
				TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());
				OpStationBean fromOp = TripInfoCacheHelper.getOpStation(this.fromId);
				if (fromOp != null)
					dbFromId = fromOp.getOpStationName();
				OpStationBean toOp = TripInfoCacheHelper.getOpStation(this.toId);
				if (toOp != null)
					dbToId = toOp.getOpStationName();
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
		}
		sb.append(this.vehicleId);
		if (dbVeh != null)
			sb.append("(").append(dbVeh).append(")");
		SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sb.append(",").append(ts <= 0 ? "No Date" : sdfFull.format(Misc.longToUtilDate(ts)));
		sb.append(",").append(this.fromId);
		if (dbFromId != null)
			sb.append("(").append(dbFromId).append(")");
		sb.append(",").append(this.toId);
		if (dbToId != null)
			sb.append("(").append(dbToId).append(")");
		sb.append(",Trips:");
		boolean isFirst = false;
		for (Integer iv : this.tripId) {
			if (!isFirst)
				sb.append(",");
			sb.append(iv);
		}
		return sb.toString();
	}
	public String helperGetDataIssues() {
		StringBuilder sb = new StringBuilder();
		if (Misc.isUndef(vehicleId)) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append("No vehicle");
		}
		if (ts <= 0) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append("No Date");
		}
		if (Misc.isUndef(fromId)) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append("No From");
		}
		if (Misc.isUndef(toId)) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append("No To");
		}
		return sb.toString();
	}
	public Requirements(int id, int portNodeId, String vehicleName, String dateStr,
			String fromStr, String toStr, int count, String stdName,
			int vehicleId, long ts, int fromId, int toId, int fromId2, int toId2
			,boolean loadChangeAllowed, boolean unloadChangeAllowed, boolean materialChangeAllowed, int materialId
			) {
		super();
		this.id = id;
		this.portNodeId = portNodeId;
		this.vehicleName = vehicleName;
		this.dateStr = dateStr;
		this.fromStr = fromStr;
		this.toStr = toStr;
		this.count = count;
		this.stdName = stdName;
		this.vehicleId = vehicleId;
		this.ts = ts;
		this.fromId = fromId;
		this.toId = toId;
		this.fromId2 = fromId2;
		this.toId2 = toId2;
		this.loadChangeAllowed = loadChangeAllowed;
		this.unloadChangeAllowed = unloadChangeAllowed;
		this.materialChangeAllowed = materialChangeAllowed;
		this.materialId = materialId;
	}
	public boolean fixVehicleId(Connection conn) throws Exception {
		boolean retval = false;
		
		if (stdName == null && vehicleName != null) {
			stdName = CacheTrack.standardizeName(vehicleName);
			retval = true;
		}
		if (stdName == null)
			return retval;
		boolean vehicleIdUndef = Misc.isUndef(vehicleId);
		if (Misc.isUndef(vehicleId)) 
			vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(stdName, conn);
		if (Misc.isUndef(vehicleId))
			vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(stdName+"IP", conn);
		if (Misc.isUndef(vehicleId))
			vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(stdName+"IPSSI", conn);
		//if (Misc.isUndef(vehicleId))
		//	vehicleId = CacheTrack.VehicleSetup.getSetupBy4Digit(vehicleName.substring(vehicleName.length()-4, vehicleName.length()), conn);
		if (vehicleIdUndef && !Misc.isUndef(vehicleId))
			retval = true;
		return retval;
	}
	public boolean fixDateStr(Connection conn) {
		boolean retval = false;
		if (this.ts <= 0) {
			String s = this.dateStr;
			if (s == null)
				return retval;
			s = s.toUpperCase();
			s = s.replaceAll("\\.", "-");
			s = s.replaceAll("/", "-");
			
			String parts[] = s.split("-");
			for (int i=0;i<parts.length;i++) {
				if (parts[i] != null)
					parts[i] = parts[i].trim();
				if (parts[i].length() == 0)
					return retval; //dont know format
				if (parts[i].startsWith("0"))
					parts[i] = parts[i].substring(1);
			}
			if (parts.length < 3)
				return retval; //cant be fixed
			String timePart = null;
			if (parts[2].length() > 4) {//has time part
				String endPart = parts[2];
				int idx = endPart.indexOf(" ");
				if (idx >= 0) {
					timePart = endPart.substring(idx+1);
					endPart = endPart.substring(0,idx);
					parts[2] = endPart;
				}
				else {
					return retval; //not sure what is in there
				}
			}
			int mon = -1;
			int day = -1;
			int year = -1;
			int hr = 0;
			int min = 0;
			int sec = 0;
			if (parts[0].length() > 2) {//yyyy-mm-dd format
				year = Misc.getParamAsInt(parts[0]);
				day = Misc.getParamAsInt(parts[2]);
			}
			else {
				year = Misc.getParamAsInt(parts[2]);
				day = Misc.getParamAsInt(parts[0]);
			}
			if (parts[1].length() > 2) {//in text
				String mn = parts[1];
				if (mn.startsWith("JAN"))
					mon = 1;
				else if (mn.startsWith("FEB"))
					mon = 2;
				else if (mn.startsWith("MAR"))
					mon = 3;
				else if (mn.startsWith("APR"))
					mon = 4;
				else if (mn.startsWith("MAY"))
					mon = 5;
				else if (mn.startsWith("JUN"))
					mon = 6;
				else if (mn.startsWith("JUL"))
					mon = 7;
				else if (mn.startsWith("AUG"))
					mon = 8;
				else if (mn.startsWith("SEP"))
					mon = 9;
				else if (mn.startsWith("OCT"))
					mon = 10;
				else if (mn.startsWith("NOV"))
					mon = 11;
				else if (mn.startsWith("DEC"))
					mon = 12;
			}
			else {
				mon = Misc.getParamAsInt(parts[1]);
			}
			if (timePart != null) {
				timePart = timePart.trim();
				if (timePart.length() == 0)
					timePart = null;
			}
			if (parts.length > 3) {
				for (int i=3;i<parts.length;i++) {
					if (timePart != null) 
						timePart += ":";
					else timePart = "";
					timePart += parts[i];
				}
			}
			if (timePart != null) {
				timePart.replaceAll(" ", ":");
				timePart.replaceAll(",", ":");
				String tps[] = timePart.split(":");
				boolean hasPM = false;
				for (int i=0; i<tps.length;i++) {
					if (tps[i] == null)
						continue;
					tps[i] = tps[i].trim();
					if (tps[i].startsWith("0"))
						tps[i] = tps[i].substring(1);
					if (tps[i].length() == 0)
						tps[i] = null;
					if (tps[i] == null)
						continue;
					if (tps[i].indexOf("PM") >= 0) {
						hasPM = true;
						tps[i] = tps[i].substring(0, tps[i].indexOf("PM"));
						if (tps[i].length() == 0)
							tps[i] = null;
					}
					if (tps[i].indexOf("AM") >= 0) {
						tps[i] = tps[i].substring(0, tps[i].indexOf("AM"));
						if (tps[i].length() == 0)
							tps[i] = null;
					}
				}
				boolean doneHr = false;
				boolean doneMin = false;
				boolean doneSec = false;
				for (int i=0;i<tps.length;i++) {
					if (tps[i] != null) {
						int temp = Misc.getParamAsInt(tps[0],0);
						if (!doneHr) {
							hr = temp;
							if (hasPM)
								hr += 12;
							doneHr = true;
						}
						else if (!doneMin) {
							min = temp;
							doneMin = true;
						}
						else if (!doneSec) {
							sec = temp;
							doneSec = true;
						}
					}//if valid tps[i]
				}//for each timepart componet
			}//processed timepart
			if (year >= 0 && year <= 70)
				year += 2000;
			else if (year > 70 && year <99)
				year += 1900;
			if (!Misc.isUndef(year))
					year-= 1900;
			if (year <= 0 || mon <= 0 || day == 0)
				return retval;
			mon--; //to get java based
			java.util.Date dt = new java.util.Date(year,mon,day,hr,min,sec);
			this.ts = dt.getTime();
			retval = true;
		}
		return retval;
	}
	public boolean fixFromToId(Connection conn, Map<String, Integer> nameToIdLookup) {
		boolean retval = false;
		for (int art=0;art<2;art++) {
			String n = art == 0 ? this.fromStr : this.toStr;
			int id = art == 0 ? this.fromId : this.toId;
			if (n == null)
				continue;
			if (n != null)
				n = n.trim();
			if (n.length() == 0)
				continue;
			if (!Misc.isUndef(id))
				continue;
			Integer iv = nameToIdLookup.get(n.toUpperCase());
			if (iv != null) {
				id = iv.intValue();
				if (art == 0)
					this.fromId = id;
				else
					this.toId = id;
				retval = true;
			}
		}
		return retval;
	}

	public void addTripId(int tripId) {
		if (!Misc.isUndef(tripId))
			this.tripId.add(tripId);
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getDateStr() {
		return dateStr;
	}
	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	public String getFromStr() {
		return fromStr;
	}
	public void setFromStr(String fromStr) {
		this.fromStr = fromStr;
	}
	public String getToStr() {
		return toStr;
	}
	public void setToStr(String toStr) {
		this.toStr = toStr;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getStdName() {
		return stdName;
	}
	public void setStdName(String stdName) {
		this.stdName = stdName;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public long getTs() {
		return ts;
	}
	public boolean isValid() {
		return !Misc.isUndef(vehicleId) && ts > 0 && !Misc.isUndef(fromId) && !Misc.isUndef(toId);
	}
	public long getDate() {
		return getDate(ts);
	}
	public static long getDate(long ts) {
		java.util.Date dt = new java.util.Date(ts);
		dt.setHours(0);
		dt.setMinutes(0);
		dt.setSeconds(0);
		return dt.getTime()/1000*1000;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
	public int getFromId() {
		return fromId;
	}
	public int getFromId2() {
		return fromId2;
	}
	public void setFromId(int fromId) {
		this.fromId = fromId;
	}
	public int getToId() {
		return toId;
	}
	public void setToId(int toId) {
		this.toId = toId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<Integer> getTripId() {
		return tripId;
	}
	public void setTripId(ArrayList<Integer> tripId) {
		this.tripId = tripId;
	}
	@Override
	public int compareTo(Object arg0) {
		Requirements rhs = (Requirements) arg0;
		if (this.stdName != null && rhs.stdName != null) {
			int cmp = stdName.compareTo(rhs.stdName);
			if (cmp != 0)
				return cmp;
		}
		else if (!Misc.isUndef(vehicleId) && !Misc.isUndef(rhs.vehicleId)) {
			int cmp = vehicleId - rhs.vehicleId;
			if (cmp != 0)
				return cmp;
		}
		else if (this.vehicleName != null && rhs.vehicleName != null) {
			int cmp = stdName.compareTo(rhs.stdName);
			if (cmp != 0)
				return cmp;
		}
		if (this.ts > 0 && rhs.ts > 0) {
			long cmp = ts-rhs.ts;
			if (cmp != 0)
				return cmp < 0 ? -1 : 1;
		}
		else if (this.dateStr != null && rhs.dateStr != null) {
			int cmp = dateStr.compareTo(rhs.dateStr);
			if (cmp != 0)
				return cmp;
		}
		if (!Misc.isUndef(fromId) && !Misc.isUndef(rhs.fromId)) {
			int cmp = fromId - rhs.fromId;
			if (cmp != 0)
				return cmp;
		}
		else if (fromStr != null && rhs.fromStr != null) {
			int cmp = fromStr.compareTo(rhs.fromStr);
			if (cmp != 0)
				return cmp;
		}
		if (!Misc.isUndef(toId) && !Misc.isUndef(rhs.toId)) {
			int cmp = toId - rhs.toId;
			if (cmp != 0)
				return cmp;
		}
		else if (toStr != null && rhs.toStr != null) {
			int cmp = toStr.compareTo(rhs.toStr);
			if (cmp != 0)
				return cmp;
		}
		return id-rhs.id;
	}
	public static void sort(ArrayList<Requirements> dataList) {
		Collections.sort(dataList);
	}

	public ArrayList<TripCreateStrategy> getTripPlan() {
		return tripPlan;
	}

	public void setTripPlan(ArrayList<TripCreateStrategy> tripPlan) {
		this.tripPlan = tripPlan;
	}
	public int getToId2() {
		return toId2;
	}
	public void setToId2(int toId2) {
		this.toId2 = toId2;
	}
	public boolean isLoadChangeAllowed() {
		return loadChangeAllowed;
	}
	public void setLoadChangeAllowed(boolean loadChangeAllowed) {
		this.loadChangeAllowed = loadChangeAllowed;
	}
	public boolean isUnloadChangeAllowed() {
		return unloadChangeAllowed;
	}
	public void setUnloadChangeAllowed(boolean unloadChangeAllowed) {
		this.unloadChangeAllowed = unloadChangeAllowed;
	}
	public boolean isMaterialChangeAllowed() {
		return materialChangeAllowed;
	}
	public void setMaterialChangeAllowed(boolean materialChangeAllowed) {
		this.materialChangeAllowed = materialChangeAllowed;
	}
	public void setFromId2(int fromId2) {
		this.fromId2 = fromId2;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	
}

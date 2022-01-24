package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Value;
import com.vividsolutions.jts.geom.Point;

public class MiningCurrentViewTemp {
	public static class OpInfo {
		private int id;
		private String name;
		private String loc;
		private double lastInOutSec;
		private double lastWaitSec;
		private double avgInOutSec;
		private double avgWaitSec;
		private double avgProcTimeSec = 180;
		private double avgLUTimeSec = 180;
		private double avgULTimeSec = 180;
		private double avgUnloadProcTime = 180;
		private long lastVehicleOut;
		private int plus5WaitSec;
		private int plus5Count;
		private int bestVehicleToDivert;
		private String bestOpToDivertTo;
		
		private ArrayList<Pair<Integer, Long>> insideVehicle = new ArrayList<Pair<Integer,Long>>();//first = vehicleId, second = seconds
		private ArrayList<Pair<Integer, Long>> goingAwayVehicle = new ArrayList<Pair<Integer,Long>>();
		private ArrayList<Pair<Integer, Long>> atUnloadVehicle = new ArrayList<Pair<Integer,Long>>();
		private ArrayList<Pair<Integer, Long>> comingInVehicle = new ArrayList<Pair<Integer,Long>>();
		public final static int g_opName = 35052;
		public final static int g_latestOutVehicleTime = 35053;
		public final static int g_waitingVehCount = 35054;
		public final static int g_comingInVehCount = 35055;
		public final static int g_waitAtUnloadVehCount = 35056;
		public final static int g_goingAwayVehCount = 35057;
		public final static int g_latestProcTimeSec = 35058;
		public final static int g_5minVehWaitCount = 35059;
		public final static int g_5minMaxWaitingTimeToClear = 35060;
		public final static int g_bestVehicleToDivert = 35061;
		public final static int g_bestOpToDivertTo = 35062;
		
		public boolean isNear(Connection conn, OpStationBean me, int otherOpId) {
			try {
				if (me == null)
					me = TripInfoCacheHelper.getOpStation(this.id);
				OpStationBean other = TripInfoCacheHelper.getOpStation(otherOpId);
				if (me == null || other == null)
					return false;
				RegionTestHelper meRTH = RegionTest.getRegionInfo(me.getGateAreaId(), conn);
				RegionTestHelper otherRTH = RegionTest.getRegionInfo(other.getGateAreaId(), conn);
				if (meRTH == null || otherRTH == null)
					return false;
				com.ipssi.geometry.Point p1 = meRTH.region.getCenter();
				com.ipssi.geometry.Point p2 = otherRTH.region.getCenter();
				double d1 = p1.distance(p2);
				return d1 < 0.200;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		public Pair<Value,String> getValue(Connection conn, int dimId) throws Exception { //first is value, 2nd is addnl value to show as click detail
			Value val = null;
			String link = null;
			switch (dimId) {
				case g_opName: {
					val = new Value(this.name);
					break;
				}
				case g_latestOutVehicleTime: {
					val = new Value(lastVehicleOut > 0 ? new java.util.Date(lastVehicleOut) : (java.util.Date)null);
					break;
				}
				case g_waitingVehCount: {
					val = new Value(this.insideVehicle.size());
					link = getVehicleList(conn, this.insideVehicle);
					break;
				}
				case g_comingInVehCount: {
					val = new Value(this.comingInVehicle.size());
					link = getVehicleList(conn, this.comingInVehicle);
					break;
				}
				case g_waitAtUnloadVehCount: {
					val = new Value(this.atUnloadVehicle.size());
					link = getVehicleList(conn, this.atUnloadVehicle);
					break;
				}
				case g_goingAwayVehCount: {
					val = new Value(this.goingAwayVehicle.size());
					link = getVehicleList(conn, this.goingAwayVehicle);
					break;
				}
				case g_latestProcTimeSec: {
					val = new Value(this.lastInOutSec);
					break;
				}
				case g_5minVehWaitCount: {
					val = new Value(this.plus5Count);
					break;
				}
				case g_5minMaxWaitingTimeToClear: {
					val = new Value(this.plus5WaitSec);
					break;
				}
				case g_bestVehicleToDivert: {
					CacheTrack.VehicleSetup vehsetup = CacheTrack.VehicleSetup.getSetup(this.bestVehicleToDivert, conn);
					val = vehsetup == null ? null : new Value(vehsetup.m_name);
					break;
				}
				case g_bestOpToDivertTo: {
					val = new Value(bestOpToDivertTo);
					break;
				}
				default: {
					break;
				}
			}
			return new Pair<Value, String>(val, link);
		}
		
		public static String getVehicleList(Connection conn, ArrayList<Pair<Integer, Long>> arrayList) throws Exception {
			StringBuilder sb = new StringBuilder();
			for (int i=0,is=arrayList.size();i<is;i++) {
				CacheTrack.VehicleSetup vehsetup = CacheTrack.VehicleSetup.getSetup(arrayList.get(i).first, conn);
				if (vehsetup == null)
					continue;
				String name = vehsetup.m_name;
				if (sb.length() != 0)
					sb.append(",");
				sb.append(name);
			}
			return sb.toString();
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
		public String getLoc() {
			return loc;
		}
		public void setLoc(String loc) {
			this.loc = loc;
		}
		public ArrayList<Pair<Integer, Long>> getInsideVehicle() {
			return insideVehicle;
		}
		public ArrayList<Pair<Integer, Long>> getGoingAwayVehicle() {
			return goingAwayVehicle;
		}
		public ArrayList<Pair<Integer, Long>> getAtUnloadVehicle() {
			return atUnloadVehicle;
		}
		public ArrayList<Pair<Integer, Long>> getComingInVehicle() {
			return comingInVehicle;
		}
		public int getPlus5WaitSec() {
			return plus5WaitSec;
		}
		public void setPlus5WaitSec(int plus5WaitSec) {
			this.plus5WaitSec = plus5WaitSec;
		}
		public int getPlus5Count() {
			return plus5Count;
		}
		public void setPlus5Count(int plus5Count) {
			this.plus5Count = plus5Count;
		}
	}
	private ArrayList<OpInfo> opInfos = new ArrayList<OpInfo>();
	private long maxCurrTime;
	public OpInfo getNearest(int otherOpId, Connection conn) {
		try{
			for (int i=0,is=opInfos.size();i<is;i++) {
				OpInfo curr = opInfos.get(i);
				if (curr.isNear(conn, null, otherOpId))
					return curr;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	public long getMaxCurrTime() {
		return maxCurrTime;
	}
	public void setMaxCurrTime(long maxCurrTime) {
		this.maxCurrTime = maxCurrTime;
	}
	public ArrayList<OpInfo> getOpInfos() {
		return opInfos;
	}
	public void calcPlus5SimpleHack(int num) {
		//		private ArrayList<Pair<Integer, Long>> insideVehicle = new ArrayList<Pair<Integer,Long>>();//first = vehicleId, second = seconds
		//private ArrayList<Pair<Integer, Long>> goingAwayVehicle = new ArrayList<Pair<Integer,Long>>();
		//private ArrayList<Pair<Integer, Long>> atUnloadVehicle = new ArrayList<Pair<Integer,Long>>();
		//private ArrayList<Pair<Integer, Long>> comingInVehicle = new ArrayList<Pair<Integer,Long>>();
		 int thresholdSec = num*60;
		 long thresholdMilli = thresholdSec*1000;
		 int minOpInfoPlus5Min = 0;
		 int minOpInfoPlus5MinIndex = -1;
		for (int oi=0,ois = opInfos.size();oi<ois;oi++) {
			OpInfo opInfo = opInfos.get(oi);
			ArrayList<Integer> procWaitInside = new ArrayList<Integer>();
			int  totProcTime = 0;
			for (int i=0, is=opInfo.insideVehicle.size();i<is;i++) {
				Pair<Integer, Long> entry = opInfo.insideVehicle.get(i);
				if (i == 0) {
					totProcTime = Math.max((int) 0, (int)( (opInfo.avgProcTimeSec*1000+entry.second - maxCurrTime)/1000));
					procWaitInside.add(totProcTime);
				}
				else {
					totProcTime += opInfo.avgProcTimeSec;
					procWaitInside.add(totProcTime);
				}
			}
			//now get vehicles that will reach
			for (int i=0,is=opInfo.comingInVehicle.size();i<is;i++) {
				Pair<Integer, Long> entry = opInfo.comingInVehicle.get(i);
				int timeRemainingForTravel = Math.max(0, (int)( (entry.second+opInfo.avgULTimeSec*1000 - maxCurrTime)/1000));
				if (timeRemainingForTravel > thresholdSec)
					break;
			   if (timeRemainingForTravel < totProcTime)
				   totProcTime += opInfo.avgProcTimeSec;
			   else
				   totProcTime = timeRemainingForTravel + (int) opInfo.avgProcTimeSec;
				procWaitInside.add(totProcTime);				
			}
			for (int i=0,is=opInfo.atUnloadVehicle.size();i<is;i++) {
				Pair<Integer, Long> entry = opInfo.atUnloadVehicle.get(i);
				int timeRemainingForTravel = Math.max(0, (int)( (entry.second+(opInfo.avgULTimeSec+opInfo.avgUnloadProcTime)*1000 - maxCurrTime)/1000));
				if (timeRemainingForTravel > thresholdSec)
					break;
				 if (timeRemainingForTravel < totProcTime)
					   totProcTime += opInfo.avgProcTimeSec;
				 else
					   totProcTime = timeRemainingForTravel + (int) opInfo.avgProcTimeSec;
				procWaitInside.add(totProcTime);				
			}
			int cnt = 0;
			for (int i=0,is=procWaitInside.size();i<is;i++) {
				if (procWaitInside.get(i) > 0) {
					cnt++;
				}
			}
			opInfo.plus5Count = cnt;
			opInfo.plus5WaitSec = procWaitInside.size() > 0 ? procWaitInside.get(procWaitInside.size()-1) : 0;
			if (opInfo.atUnloadVehicle.size() > 0)
				opInfo.bestVehicleToDivert = opInfo.atUnloadVehicle.get(0).first;
			else if (opInfo.comingInVehicle.size() > 0)
				opInfo.bestVehicleToDivert = opInfo.comingInVehicle.get(0).first;
			else if (opInfo.insideVehicle.size() > 0)
				opInfo.bestVehicleToDivert = opInfo.insideVehicle.get(opInfo.insideVehicle.size()-1).first;
			else if (opInfo.goingAwayVehicle.size() > 0)
				opInfo.bestVehicleToDivert = opInfo.goingAwayVehicle.get(opInfo.goingAwayVehicle.size()-1).first;

			
			if (minOpInfoPlus5MinIndex < 0 || minOpInfoPlus5Min > opInfo.plus5WaitSec) {
				minOpInfoPlus5Min = opInfo.plus5WaitSec - thresholdSec;
				minOpInfoPlus5MinIndex = oi;
			}
		}
		//now get the best OpInfoDivertTo
		if (minOpInfoPlus5MinIndex >= 0) {
			String n = opInfos.get(minOpInfoPlus5MinIndex).name;
			for (int oi=0,ois = opInfos.size();oi<ois;oi++) {
				opInfos.get(oi).bestOpToDivertTo = n;;
			}
		}
	}
	private static String qString = "select t.vehicle_id, t.load_gate_op, t.load_area_wait_in, t.load_gate_in, t.load_gate_out, t.load_area_wait_out "
	+ " , t.unload_gate_op, t.unload_area_wait_in, t.unload_gate_in, t.unload_gate_out, t.unload_area_wait_out, current_data.gps_record_time "
	+ " ,t.confirm_time, lo.name from   "
	+ " (select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) "
	+ ") vi  join (select vehicle_id, max(combo_start) cst from trip_info where load_gate_op is not null group by vehicle_id) ltp  "
	+ " on (ltp.vehicle_id = vi.vehicle_id) join trip_info t "
	+ " on (ltp.vehicle_id = t.vehicle_id and  ltp.cst = t.combo_start) "
	+" left outer join op_station lo on (lo.id = t.load_gate_op) "
	+" left outer join current_data on (current_data.vehicle_id = t.vehicle_id and current_data.attribute_id=0) "
	+" where timestampdiff(minute,t.load_gate_out,now()) < 180"
	+" order by t.load_gate_op, t.combo_start asc "
	;	
	public void loadOpInfo(Connection conn, int pv123) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement(qString);
			ps.setInt(1, pv123);
			ResultSet rs = ps.executeQuery();
			OpInfo curr = null;
			OpStationBean currOpInfo = null;
			long now = System.currentTimeMillis();
			int prevOpId = Misc.getUndefInt();
			while (rs.next()) {
				int colIndex = 1;
				int vehicleId = rs.getInt(colIndex++);
				int loadGateOp = Misc.getRsetInt(rs, colIndex++);
				long lwin = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long lgin = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long lgout = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long lwout = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				
				int unloadGateOp = Misc.getRsetInt(rs, colIndex++);
				long uwin = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long ugin = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long ugout = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long uwout = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long cdt = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				long conft  = Misc.sqlToLong(rs.getTimestamp(colIndex++));
				String lopName = rs.getString(colIndex++);
				long tripmx = 0;
				if (prevOpId == loadGateOp) {
				
				}
				else if (curr != null && curr.isNear(conn, currOpInfo, loadGateOp)) {
					
				}
				else {
					curr = getNearest(loadGateOp, conn);
				}
				
				if (curr == null){
					curr = new OpInfo();
					opInfos.add(curr);
					curr.id = loadGateOp;
					curr.name = lopName;
					currOpInfo = TripInfoCacheHelper.getOpStation(loadGateOp);
				}
				prevOpId = loadGateOp;
				if (lwout > 0) {
					if (curr.lastVehicleOut <= 0 || curr.lastVehicleOut <= lwout) {
						curr.lastVehicleOut = lwout;
						int tsInOut  = (int) ((lwout-lwin)/1000);
						curr.lastInOutSec = tsInOut;
						int tsWait =  (int) ((lgin-lwin)/1000);
						curr.lastWaitSec =tsWait;
						int cnt = (curr.goingAwayVehicle.size()+curr.atUnloadVehicle.size()+curr.comingInVehicle.size());
						curr.avgInOutSec = (curr.avgInOutSec * cnt+lwout)/(cnt+1);
						curr.avgWaitSec = (curr.avgWaitSec * cnt+lwout)/(cnt+1);
					}
				}
				if (lwout <= 0) {
					curr.insideVehicle.add(new Pair<Integer, Long>(vehicleId, lwin));
					if (tripmx < lwin)
						tripmx = lwin;
				}				
				else if (uwin <= 0) {
					curr.goingAwayVehicle.add(new Pair<Integer, Long>(vehicleId, lwout));
					if (tripmx < lwout)
						tripmx = lwout;

				}
				else if (uwout <= 0) {
					curr.atUnloadVehicle.add(new Pair<Integer, Long>(vehicleId, uwin));
					if (tripmx < uwin)
						tripmx = uwin;

				}
				else {
					curr.comingInVehicle.add(new Pair<Integer, Long>(vehicleId, uwout));
					if (tripmx < uwout)
						tripmx = uwout;
				}
				if (cdt < 0)
					cdt = tripmx;
				if (maxCurrTime < cdt)
					maxCurrTime = cdt;

			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		
		}
	}
}

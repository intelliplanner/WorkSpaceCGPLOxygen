package com.ipssi.simulHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

//import bsh.This;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.miningOpt.SiteStats;
import com.ipssi.routemonitor.RouteDef;
import com.ipssi.simulHelper.ShovelInfo.*;
import com.ipssi.simulHelper.SimParams.*;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

public class DumperInfo {
	volatile boolean canGenerateInsutruction = false;
	volatile int instructionGenAt = 2;
	//above two for testing of random dyn instruction
	volatile long lastUnloadTime = -1;
	volatile int dumperId;
	volatile long dataGenOpCompleteTime=-1;//for load to be set by shovel
	volatile long dataGenOpStartTime=-1;//for load, the arrival time
	volatile int dataGenOpType=-1;// tells tillWhatOpstation has data been generated
	volatile int dataGenOpId=Misc.getUndefInt();//tells tillWhatOpStationId has data been generated
	volatile SimInstruction instruction = null;
	volatile Route routeUsedForLastGen = null;
	volatile long moveStarted=-1;
	volatile boolean lastOpCompleteOfLoad = false;
	volatile int shovelId = Misc.getUndefInt();//the current load/unload to use in case not coming from Instruction
	volatile int uopId;
	volatile int firstShovelId = Misc.getUndefInt();
	volatile int secondShovelId = Misc.getUndefInt();
	volatile int lastPicked = Misc.getUndefInt();
	
	volatile private CacheTrack.VehicleSetup vehsetup = null;
	volatile private double prevNameAtLon = Misc.getUndefDouble();
	volatile private double prevNameAtLat = Misc.getUndefDouble();
	
	public String getGpsName(Connection conn, boolean force) throws Exception {
		if (force || Math.abs(lon-prevNameAtLon) > 0.0005 || Math.abs(lat-prevNameAtLat) > 0.0005) {
			if (vehsetup == null) {
				vehsetup = CacheTrack.VehicleSetup.getSetup(this.dumperId, conn);
			}
			gpsName = SimGenerateData.calcGpsName(conn, this.dumperId, vehsetup, lon, lat, this.latestGpsAt);
			prevNameAtLon = lon;
			prevNameAtLat = lat;
		}
		return gpsName;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(dumperId).append(",").append(shovelId).append(",").append(uopId);
		return sb.toString();
	}

	public  synchronized long getMoveStarted() {
		return  this.moveStarted;
	}
	private double lon;
	private double lat;
	private double cummDist;
	private long latestGpsAt;
	private String gpsName;
	
	public static class PrepForInstructionResult {
		boolean alreadyApplied = false;
		boolean notReadyForApply = false;
		boolean isLoadedOnMovement = false;
		int toId = Misc.getUndefInt();
		int toType = Misc.getUndefInt();
		boolean mayNeedToInterpolateRoute = false;
		boolean dontDoMoveDelayAtBegOfRoute = false;
		int returnId = Misc.getUndefInt();
		NearestRouteInfo nearestRouteInfo = null;
	}
	public static class NearestRouteInfo {
		Route route;
		double destLonIfRest = Misc.getUndefDouble();
		double destLatIfRest = Misc.getUndefDouble();;
		Pair<Integer, Double> entryPoint;
		Pair<Integer, Double> cutOffPoint;
	}
	private NearestRouteInfo getNearestRoute(PrepForInstructionResult prepForInstruction) {
		//We may have instruction coming in before unload started (trip is guiding that), after unloading
		NearestRouteInfo retval = new NearestRouteInfo();
		if (this.routeUsedForLastGen != null) {
			if (prepForInstruction.toType == SimInstruction.TYPE_REST) {
				UOpParam uopParams = SimParams.getUopParam(prepForInstruction.toId);
				retval.entryPoint = this.routeUsedForLastGen.getNearestInfo(this.lon, this.lat);
				retval.cutOffPoint = this.routeUsedForLastGen.getNearestInfo(uopParams.getLon(), uopParams.getLat());
				retval.destLonIfRest = uopParams.getLon();
				retval.destLatIfRest = uopParams.getLat();
				retval.route = this.routeUsedForLastGen;
			}
			else if (prepForInstruction.toType == SimInstruction.TYPE_SHOVEL) {
				Route route = SimParams.getRouteForShovelUop(prepForInstruction.toId, this.routeUsedForLastGen.getUOpId());
				retval.entryPoint = route.getNearestInfo(this.lon, this.lat);
				retval.cutOffPoint = new Pair<Integer, Double>(route.getBackRoute().size()-1,0.0);
				retval.route = route;
			}
			else if (prepForInstruction.toType == SimInstruction.TYPE_UOP) {
				Route route = SimParams.getRouteForShovelUop(this.routeUsedForLastGen.getShovelId(), prepForInstruction.toId);
				retval.entryPoint = route.getNearestInfo(this.lon, this.lat);
				retval.cutOffPoint = new Pair<Integer, Double>(0,0.0);
				retval.route = route;
			}
		}
		else {
			Route bestRoute = null;
			Pair<Integer, Double> bestInfo = null;
			if (prepForInstruction.toType != SimInstruction.TYPE_REST) {
				ArrayList<Route> routeList = prepForInstruction.toType == SimInstruction.TYPE_SHOVEL ? SimParams.getRouteListForShovel(prepForInstruction.toId)
						: SimParams.getRouteListForUOp(prepForInstruction.toId)
						;
				for (int i=0,is=routeList.size();i<is;i++) {
					Pair<Integer,Double> nearestInfo = routeList.get(i).getNearestInfo(this.lon,this.lat);
					if (bestRoute == null || nearestInfo.second < bestInfo.second) {
						bestRoute = routeList.get(i);
						bestInfo = nearestInfo;
					}
				}
				retval.route = bestRoute;
				retval.entryPoint = bestInfo;
				retval.cutOffPoint = new Pair<Integer, Double>(prepForInstruction.toType == SimInstruction.TYPE_SHOVEL ? bestRoute.getBackRoute().size()-1 : 0, 0.0);
			}
			else {
				//rest to rest ..
				Collection<ArrayList<Route>> allRoutes = SimParams.getAllRoutes();
				for (Iterator<ArrayList<Route>> iter = allRoutes.iterator(); iter.hasNext(); ) {
					ArrayList<Route> routeList = iter.next();
					for (int i=0,is=routeList.size();i<is;i++) {
						Pair<Integer,Double> nearestInfo = routeList.get(i).getNearestInfo(this.lon,this.lat);
						if (bestRoute == null || nearestInfo.second < bestInfo.second) {
							bestRoute = routeList.get(i);
							bestInfo = nearestInfo;
						}
					}
				}
				retval.route = bestRoute;
				retval.entryPoint = bestInfo;
				UOpParam uopParams = SimParams.getUopParam(prepForInstruction.toId);
				retval.cutOffPoint = this.routeUsedForLastGen.getNearestInfo(uopParams.getLon(), uopParams.getLat());
				retval.destLonIfRest = uopParams.getLon();
				retval.destLatIfRest = uopParams.getLat();
			}
		}
		return retval;
	}
	private static class CurrInfo {
		public double lon;
		public double lat;
		public long ts;
		public String name;
		public double cummDist = 0;
		public CurrInfo(double lon, double lat, long ts, String name, double cummDist) {
			this.lon = lon;
			this.lat = lat;
			this.ts = ts;
			this.name = name;
			this.cummDist = cummDist;
		}
	}
	private CurrInfo setThingsToCurrentTime(Connection conn, long currTS) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		CurrInfo retval = null;
		try {
			ps = conn.prepareStatement("select lgd.longitude, lgd.latitude, lgd.gps_record_time, lgd.name, lgd.attribute_value from logged_data_pb lgd join "+
					" (select max(gps_record_time) grt from logged_data_pb where vehicle_id=? and attribute_id=0 and gps_record_time <= ?) mx "+
					" on (mx.grt = lgd.gps_record_time and lgd.attribute_id=0 and lgd.vehicle_id=?) "
					);
			ps.setInt(1, this.dumperId);
			ps.setTimestamp(2, Misc.longToSqlDate(currTS));
			ps.setInt(3, this.dumperId);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = new CurrInfo(rs.getDouble(1), rs.getDouble(2), Misc.sqlToLong(rs.getTimestamp(3)), rs.getString(4), rs.getDouble(5));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("delete from logged_data_pb where vehicle_id=? and gps_record_time > ?");
			ps.setInt(1, this.dumperId);
			ps.setTimestamp(2, Misc.longToSqlDate(currTS));
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	private PrepForInstructionResult prepForInstruction(Connection conn, long currTS) throws Exception {
//Works on for UOP to LOP properly. Assumes 
		
		PrepForInstructionResult retval = new PrepForInstructionResult();
		
		boolean done = false;
		if (this.dataGenOpType == this.instruction.getToType() && this.dataGenOpId == this.instruction.getToId()) {
			retval.alreadyApplied = true;
			DumperInfo.logDisposition(currTS, 0, this.getDumperId(), this.instruction.getToId(), this.instruction.getReturnId(), this.instruction.getToType());
			return retval;
		}
		
		if (this.instruction.getApplyMode() == SimInstruction.APPLY_AFT_LU_COMPLETE) {
			if (instruction.getToType() == SimInstruction.TYPE_SHOVEL) {
				if (this.dataGenOpType == SimInstruction.TYPE_UOP) {
					if (this.dataGenOpCompleteTime <= 0 || this.dataGenOpCompleteTime > currTS) { //hasnt yet completed UOP .. so defer it
						retval.notReadyForApply = true;
						return retval;
					}
				}
				else if (this.dataGenOpType == SimInstruction.TYPE_SHOVEL) {
					if (this.dataGenOpStartTime <= currTS) { //meaning instruction somehow arrived later ... or later ... so ignore it
						retval.alreadyApplied = true;
						DumperInfo.logDisposition(currTS, 0, this.getDumperId(), this.instruction.getToId(), this.instruction.getReturnId(), this.instruction.getToType());
						return retval;
					}
				}
			}
			else if (instruction.getToType() == SimInstruction.TYPE_UOP) {
				if (this.dataGenOpType == SimInstruction.TYPE_SHOVEL) {
					if (this.dataGenOpCompleteTime <= 0 || this.dataGenOpCompleteTime > currTS) { //hasnt yet completed LOAD .. so defer it
						retval.notReadyForApply = true;
						return retval;
					}
				}
				else if (this.dataGenOpType == SimInstruction.TYPE_UOP) {
					if (this.dataGenOpStartTime <= currTS) { //meaning instruction somehow arrived earliier ... or later ... so ignore it
						DumperInfo.logDisposition(currTS, 0, this.getDumperId(), this.instruction.getToId(), this.instruction.getReturnId(), this.instruction.getToType());
						retval.alreadyApplied = true;
						return retval;
					}
				}
			}
			else {
				DumperInfo.logDisposition(currTS, 0, this.getDumperId(), this.instruction.getToId(), this.instruction.getReturnId(), this.instruction.getToType());
				retval.alreadyApplied = true;
				return retval;
			}
		}
		retval.nearestRouteInfo = new NearestRouteInfo();
		int shovelId = this.instruction.getToType() == SimInstruction.TYPE_SHOVEL ? this.instruction.getToId() : this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? this.dataGenOpId : this.shovelId;
		int uopId = this.instruction.getToType() == SimInstruction.TYPE_UOP ? this.instruction.getToId() : this.dataGenOpType == SimInstruction.TYPE_UOP ? this.dataGenOpId : this.uopId;
		if (Misc.isUndef(shovelId))
			shovelId = this.shovelId;
		if (Misc.isUndef(uopId))
			uopId = this.uopId;
		
		retval.nearestRouteInfo.route = SimParams.getRouteForShovelUop(shovelId, uopId);
		retval.nearestRouteInfo.entryPoint = new Pair<Integer, Double>(this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? retval.nearestRouteInfo.route.getBackRoute().size()-1 : 0, 0.0);
		retval.nearestRouteInfo.cutOffPoint = new Pair<Integer, Double>(this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? 0 : retval.nearestRouteInfo.route.getBackRoute().size()-1, 0.0);
		retval.toId = this.instruction.getToId();
		retval.returnId = this.instruction.getReturnId();
		retval.toType = this.instruction.getToType();
		retval.isLoadedOnMovement = this.instruction.getToType() == SimInstruction.TYPE_UOP;//(this.dataGenOpType == SimInstruction.TYPE_SHOVEL && this.dataGenOpCompleteTime > 0);
		if (retval.toType == this.dataGenOpType && retval.toId != this.dataGenOpId) {
			// some data of future may have been generated .. so we need to delete and iterporlate
			CurrInfo curr = this.setThingsToCurrentTime(conn, currTS < this.moveStarted ? this.moveStarted : currTS);
			this.lon = curr.lon;
			this.lat = curr.lat;
			this.latestGpsAt = curr.ts;
			this.gpsName = curr.name;
			this.cummDist = curr.cummDist;
			if (currTS > this.moveStarted) {
				retval.mayNeedToInterpolateRoute = true;
				retval.dontDoMoveDelayAtBegOfRoute = true;
			}
		}
		
		/* OLD CODE
		if ((this.instruction.getApplyMode() ==SimInstruction.APPLY_AFT_LU_COMPLETE) &&
		      ((this.instruction.getToType() == SimInstruction.TYPE_SHOVEL && this.dataGenOpType != SimInstruction.TYPE_UOP) ||
		    		  (this.instruction.getToType() == SimInstruction.TYPE_UOP && this.dataGenOpType != SimInstruction.TYPE_SHOVEL)
		    		  ))
			this.instruction.setApplyMode(SimInstruction.APPLY_NOW);
		if (this.instruction.getApplyMode() ==SimInstruction.APPLY_AFT_LU_COMPLETE) {
			if (this.dataGenOpCompleteTime <= 0 || this.dataGenOpCompleteTime > currTS) {
				retval.notReadyForApply = true;
				return retval;
			}
			retval.nearestRouteInfo = new NearestRouteInfo();
			retval.nearestRouteInfo.route = SimParams.getRouteForShovelUop(this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? this.dataGenOpId : this.instruction.getToId(), this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? this.instruction.getToId() : this.dataGenOpId);
			retval.nearestRouteInfo.entryPoint = new Pair<Integer, Double>(this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? retval.nearestRouteInfo.route.getBackRoute().size()-1 : 0, 0.0);
			retval.nearestRouteInfo.cutOffPoint = new Pair<Integer, Double>(this.dataGenOpType == SimInstruction.TYPE_SHOVEL ? 0 : retval.nearestRouteInfo.route.getBackRoute().size()-1, 0.0);
			retval.toId = this.instruction.getToId();
			retval.toType = this.instruction.getToType();
			retval.isLoadedOnMovement = (this.dataGenOpType == SimInstruction.TYPE_SHOVEL && this.dataGenOpCompleteTime > 0); 
			return retval;
		}
	
		if (this.instruction.getApplyMode() ==SimInstruction.APPLY_NOW) {
			//will need to get rid of existing data ..
			retval.mayNeedToInterpolateRoute = true;
			retval.toId = this.instruction.getToId();
			retval.toType = this.instruction.getToType();
			if (this.lastOpCompleteOfLoad)
				retval.isLoadedOnMovement = true;
			long toDelDataFromIncl = currTS;
			long mvStarted = this.getMoveStarted();
			if (mvStarted> currTS) {
				retval.dontDoMoveDelayAtBegOfRoute = true;
				toDelDataFromIncl = mvStarted+1000;
			}
			retval.nearestRouteInfo = this.getNearestRoute(retval);
			
			PreparedStatement ps = conn.prepareStatement("delete from logged_data_pb where vehicle_id=? and gps_record_time >= ?");
			ps.setInt(1, this.dumperId);
			ps.setTimestamp(2, Misc.utilToSqlDate(toDelDataFromIncl));
			ps.execute();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("select gps_record_time, longitude,latitude,attribute_value,speed,name from logged_data where vehicle_id = ? and attribute_id=0 and gps_record_time < ? order by gps_record_time desc limit 1");
			ps.setInt(1, this.dumperId);
			ps.setTimestamp(2, Misc.utilToSqlDate(toDelDataFromIncl));
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				this.latestGpsAt = Misc.sqlToLong(rs.getTimestamp(1));
				this.lon = rs.getDouble(2);
				this.lat = rs.getDouble(3);
				this.cummDist = rs.getDouble(4);
				this.gpsName = rs.getString(6);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		*/
		return retval;
	}
	private static ConcurrentHashMap<Integer, Integer> newUopByShovel = new ConcurrentHashMap<Integer, Integer>();
	
	public static void setUopForShovel(int shovelId, int uopId) {
		newUopByShovel.put(shovelId, uopId);
	}
	private static int getIndexInList(ArrayList<Integer> list, int val) {
		for (int i=0,is=list.size(); i<is; i++) {
			if (val == list.get(i))
				return i;
		}
		return -1;
	}
	private void setNewUOpByShovel() {
		ArrayList<Integer> uopList = SimParams.getUopList();
		Set<Entry<Integer, Integer>> shovelUop = newUopByShovel.entrySet();
		for (Iterator<Entry<Integer, Integer>> iter = shovelUop.iterator();iter.hasNext();) {
			Entry<Integer, Integer> entry = iter.next();
			int currUop = entry.getValue();
			int currUopIndex = getIndexInList(uopList, currUop);
			int nextIndex = (currUopIndex+1)%uopList.size();
			entry.setValue(uopList.get(nextIndex));
		}
		
	}
	public  synchronized long generateDataFromToMovement(Connection conn, PreparedStatement psInsertLgd, long currTS) throws Exception {
		PrepForInstructionResult prepForInstruction = null;
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		boolean fromManualInstruction = false;
		if (this.instruction == null) {
			if (this.dataGenOpCompleteTime > 0 && this.dataGenOpCompleteTime <= currTS) {
				prepForInstruction = new PrepForInstructionResult();
				
				int shovelId = this.shovelId;
				ShovelInfo shovelInfo = SimGenerateData.getShovelInfo(shovelId);
				if (true && currTS - SimGenerateData.g_lastUopCheckTime > 20*1000) {
					Integer uopFromNew = newUopByShovel.get(shovelId);
					if (uopFromNew == null) {
						uopFromNew = new Integer(this.uopId);
						newUopByShovel.put(shovelId, uopFromNew);
					}
					setNewUOpByShovel();
					SimGenerateData.g_lastUopCheckTime = currTS;
				}
				int uopId = this.uopId;
				if (true) {
					Integer uopFromNew = newUopByShovel.get(shovelId);
					if (uopFromNew == null) {
						uopFromNew = new Integer(this.uopId);
						newUopByShovel.put(shovelId, uopFromNew);
					}
					this.uopId = uopId = uopFromNew.intValue();
				}
				boolean goingToShovel = this.dataGenOpType == SimInstruction.TYPE_UOP;
				
				if (goingToShovel && !Misc.isUndef(this.secondShovelId)) {
					//check which is the one that has least Q
					if (false) {
						if (this.shovelId == this.secondShovelId) {
							shovelId = this.firstShovelId;
						}
						else {
							shovelId = this.secondShovelId;
						}
					}
					else  {
						shovelInfo = SimGenerateData.getShovelInfo(firstShovelId);
						ShovelInfo shovelInfo2 = SimGenerateData.getShovelInfo(secondShovelId);
						int sz1 = shovelInfo.getPendingDumpers().size();
						int sz2 = shovelInfo2.getPendingDumpers().size();
						if (sz2 < sz1) {//note shovelInfo changed
							shovelId = shovelInfo2.getShovelId();
						//	System.out.println("[SIMULATOR] Picking shovel "+shovelInfo2.getShovelId()+" for dumper "+this.getDumperId());
							shovelInfo = shovelInfo2;
						}
						else {
							shovelId = shovelInfo.getShovelId();
						//	System.out.println("[SIMULATOR] Picking shovel "+shovelInfo.getShovelId()+" for dumper "+this.getDumperId());
						}
					}
				}
				if (goingToShovel) {
					
					shovelInfo = SimGenerateData.getShovelInfo(shovelId);
					//ShovelInfo.ShovelQItem dumperEntry = new ShovelInfo.ShovelQItem(dumperId, this.latestGpsAt);
					//shovelInfo.getPendingDumpers().offer(dumperEntry);
				}
				else {
				}
			//	System.out.println("[SIM_EXT]"+goingToShovel+ " S1:"+this.firstShovelId+" S2:"+this.secondShovelId+" Pick:"+shovelId+" Uop:"+uopId+ " Pend:"+shovelInfo.getPendingDumpers().size());
				//@#@#@#@#
				Route route = SimParams.getRouteForShovelUop(shovelId, uopId);
				NearestRouteInfo rinfo = new NearestRouteInfo();
				rinfo.route = route;
				prepForInstruction.nearestRouteInfo = rinfo;
				prepForInstruction.mayNeedToInterpolateRoute = false;
				prepForInstruction.isLoadedOnMovement = (this.dataGenOpType == SimInstruction.TYPE_SHOVEL && this.dataGenOpCompleteTime > 0);
				if (this.dataGenOpType == SimInstruction.TYPE_SHOVEL) {
					prepForInstruction.toId = uopId;
					prepForInstruction.toType = SimInstruction.TYPE_UOP;
					if (route != null && route.getBackRoute() != null) {
						rinfo.entryPoint = new Pair<Integer, Double>(route.getBackRoute().size()-1,0.0);
					}
					rinfo.cutOffPoint = new Pair<Integer, Double>(0,0.0);			
				}
				else if (this.dataGenOpType == SimInstruction.TYPE_UOP){
					prepForInstruction.toId = shovelId;
					prepForInstruction.toType = SimInstruction.TYPE_SHOVEL;
					rinfo.entryPoint = new Pair<Integer, Double>(0,0.0);
					rinfo.cutOffPoint = new Pair<Integer, Double>(route.getBackRoute().size()-1,0.0);
			
				}
				else {
					return -1;
				}
				//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" AutoGenerating forward movement for dumper:"+this.getDumperId()+" Target:"+(prepForInstruction.toType ==  SimInstruction.TYPE_SHOVEL ? "S,":"D,")+prepForInstruction.toId+" load status:"+prepForInstruction.isLoadedOnMovement);

				//if (this.lastOpCompleteOfLoad) 
				//	prepForInstruction.isLoadedOnMovement = true;
			}
			else
				return -1;
		}
		else {
			prepForInstruction = this.prepForInstruction(conn, currTS);
			fromManualInstruction = true;
			//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" InstructionGenerating forward movement for dumper:"+this.getDumperId()+" Target:"+(prepForInstruction.toType ==  SimInstruction.TYPE_SHOVEL ? "S,":"D,")+prepForInstruction.toId+" load status:"+prepForInstruction.isLoadedOnMovement);
		}
		if (this.instruction != null && !prepForInstruction.notReadyForApply && !prepForInstruction.alreadyApplied) {
		//	System.out.println("[#####APPLYING_MANUAL:"+this.dumperId);
		}
		if (this.instruction != null &&  prepForInstruction.alreadyApplied) {
		//	System.out.println("[#####ALREADY APPLIED MANUAL:"+this.dumperId+" To:"+this.instruction.getToId());
		}
		if (!prepForInstruction.notReadyForApply)
			this.instruction = null;
		if (prepForInstruction.alreadyApplied || prepForInstruction.notReadyForApply)
			return -1;
		if (prepForInstruction.toType != SimInstruction.TYPE_UOP && this.dataGenOpType == SimInstruction.TYPE_SHOVEL) {
			SimGenerateData.removeEntryForDumper(this.dataGenOpId, dumperId);
		}
		
		//1. generate a random delay from movement start
		//2. get route and get random stop delay .. and generate data till unload
		//3. generate data during unload (including dala up/down
		if (prepForInstruction.toType == SimInstruction.TYPE_UOP) {
			this.uopId = prepForInstruction.toId;
			if (!Misc.isUndef(prepForInstruction.returnId))
				this.shovelId = prepForInstruction.returnId;
			
		}
		else if (prepForInstruction.toType == SimInstruction.TYPE_SHOVEL) {
			this.shovelId = prepForInstruction.toId;
			if (!Misc.isUndef(prepForInstruction.returnId))
				this.uopId = prepForInstruction.returnId;
		}
		
		long minTS = -1;
		long maxTS = -1;
		if (!prepForInstruction.dontDoMoveDelayAtBegOfRoute) {
			int moveDelay = prepForInstruction.isLoadedOnMovement ? SimParams.getVal(SimParams.MOVE_DELAY, SimParams.MOVE_DELAY_LO, SimParams.MOVE_DELAY_HI)
					: SimParams.getVal(SimParams.UNLOAD_AFTER_DELAY, SimParams.UNLOAD_AFTER_DELAY_LO, SimParams.UNLOAD_AFTER_DELAY_HI);
			moveDelay +=  (int)(prepForInstruction.isLoadedOnMovement ? SimParams.getExtraAfterLoadDelay() : SimParams.getExtraAfterUnloadDelay());
			long moveStartTS = currTS+1000*(moveDelay > 0 ? moveDelay-1 : 0);
			long ts = this.latestGpsAt + 10*1000;
			if (moveDelay > 0) {
				while (ts < moveStartTS && moveDelay > 0) {
					if (ts >= currTS) {
						if (minTS <= 0 || minTS > ts)
							minTS = ts;
						if (maxTS <= 0 || maxTS < ts)
							maxTS = ts;
						psInsertLgd.setInt(1, this.dumperId);
						psInsertLgd.setInt(2, 0);
						psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
						psInsertLgd.setDouble(4, lon);
						psInsertLgd.setDouble(5, lat);
						psInsertLgd.setDouble(6, cummDist);
						psInsertLgd.setDouble(7, 0);
						psInsertLgd.setInt(8, 1);
						psInsertLgd.setString(9, gpsName);
						psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
						psInsertLgd.addBatch();
						this.latestGpsAt = ts;
						this.moveStarted = ts;
					}
					ts += 10*1000;
				}
			}
			else {
				this.moveStarted = this.latestGpsAt;
			}
				
		}
		
		
		Route route = prepForInstruction.nearestRouteInfo.route;
		this.routeUsedForLastGen = route;
		this.dataGenOpType = prepForInstruction.toType;
		this.dataGenOpCompleteTime = -1;
		this.dataGenOpStartTime = -1;
		this.dataGenOpId = prepForInstruction.toId;
		
		Pair<Integer, Double> entry = prepForInstruction.nearestRouteInfo.entryPoint;
		Pair<Integer, Double> exit = prepForInstruction.nearestRouteInfo.cutOffPoint;
		ArrayList<GpsPlus> routePts;
		int transitTime;
		int totDelay;
		if (prepForInstruction.isLoadedOnMovement) {
			routePts = route.getForwRoute();
			//int temp = exit.first;
			Pair<Integer, Double>temp = exit;
			exit=entry;
			entry=temp;
			exit.first = routePts.size()-1;
			transitTime = route.getForwSec();
			totDelay = SimParams.getVal(route.getForwSec(), route.getLoForwSec(), route.getHiForwSec())-transitTime;
		}
		else {
			routePts = route.getBackRoute();
			transitTime = route.getBackSec();
			totDelay = SimParams.getVal(route.getBackSec(), route.getLoBackSec(), route.getHiBackSec())-transitTime;	
		}
		totDelay += SimParams.getExtraAfterTransit();
		if (!SimGenerateData.g_doStraightLineRoutes) {//NOT USED
			boolean skipEntry = false;
			if (prepForInstruction.mayNeedToInterpolateRoute && (entry.first != 0 && entry.first != routePts.size()-1)) {
				long newMinTS = interpolateData(conn, psInsertLgd, entry.second, this.lon, this.lat, routePts.get(entry.first).getLon(), routePts.get(entry.first).getLat(), transitTime+totDelay, route.getForwDist());
				if ((newMinTS > 0) && (newMinTS < minTS || minTS <= 0))
					minTS = newMinTS;
				skipEntry = true;
			}
			//generate pt of route
			
			long newMinTS = putDataForRouteOnly(skipEntry, conn, psInsertLgd,  transitTime, totDelay, routePts, entry.first, exit.first);
			if ((newMinTS > 0) && (newMinTS < minTS || minTS <= 0))
				minTS = newMinTS;
			if (prepForInstruction.mayNeedToInterpolateRoute && (exit.first != 0 && exit.first != routePts.size()-1)) {
				newMinTS = interpolateData(conn, psInsertLgd, exit.second, this.lon, this.lat, prepForInstruction.nearestRouteInfo.destLonIfRest, prepForInstruction.nearestRouteInfo.destLatIfRest, transitTime+totDelay, route.getForwDist());
				if ((newMinTS > 0) && (newMinTS < minTS || minTS <= 0))
					minTS = newMinTS;
	
			}
		}
		else {
			
			int routeTransitTime = transitTime+totDelay;
			double routeTransitDist = route.getForwDist();
			ShovelInfo shovelInfo = SimParams.getShovelInfo(route.getShovelId());
			UOpParam uopInfo = SimParams.getUopParam(route.getUOpId());
			boolean destToShovel = prepForInstruction.toId == route.getShovelId() && prepForInstruction.toType == SimInstruction.TYPE_SHOVEL; 
			double rtStLon = destToShovel ? uopInfo.getLon() : shovelInfo.getLon();
			double rtStLat  = destToShovel ? uopInfo.getLat() : shovelInfo.getLat();;
			double rtEnLon  = destToShovel ? shovelInfo.getLon() : uopInfo.getLon();
			double rtEnLat = destToShovel ? shovelInfo.getLat() : uopInfo.getLat();
			double stLon = lon;
			double stLat = lat;
			Pair<Double, Double> alphaDist = RouteDef.checkWhereInSegment(lon, lat, rtStLon, rtStLat, rtEnLon, rtEnLat);
			double alpha = alphaDist.first;
			
			routeTransitDist = routeTransitDist * ((alpha >= -0.001 && alpha <= 1.00001) ? (1-alpha) : (1+Math.abs(alpha))); 
			routeTransitTime = (int) (routeTransitTime * ((alpha >= -0.001 && alpha <= 1.00001) ? (1-alpha) : (1+Math.abs(alpha))));
			long dbgLatestTS = this.latestGpsAt;
			double initLon = this.lon;
			double initLat = this.lat;
			
			
			long newMinTS = interpolateDataForStlineRoute(conn, psInsertLgd,  this.lon, this.lat, rtEnLon, rtEnLat, routeTransitTime, routeTransitDist);
			SimpleDateFormat tdf = new SimpleDateFormat("HH:mm:ss");
			DecimalFormat df = new DecimalFormat("#.00000");
			DecimalFormat df2 = new DecimalFormat("#.00");
			dbgLog(currTS, destToShovel, fromManualInstruction, routeTransitTime, routeTransitDist, dbgLatestTS, initLon, initLat, shovelInfo, uopInfo);
			System.out.println("[SIM##] Thread:"+Thread.currentThread().getId()+" D:,"+this.getDumperId()+" ,Now:,"+tdf.format(Misc.longToUtilDate(currTS))+" ,ToU?:,"+!destToShovel+" ,M?:,"+fromManualInstruction+" ,S:,"+shovelInfo.getShovelId()+" ,U:,"+route.getUOpId()+" ,TT:,"+routeTransitTime+ " ,TD:,"+df2.format(routeTransitDist)
					+ " ,Move:,"+tdf.format(Misc.longToUtilDate(moveStarted))+ " ,Bef:,"+tdf.format(Misc.longToUtilDate(dbgLatestTS))+" ,Fin:,"+tdf.format(Misc.longToUtilDate(this.latestGpsAt))
					+" ,Init:(,"+df.format(initLon)+","+df.format(initLat)+",) Dest:(,"+df.format(this.lon)+","+df.format(this.lat)+",)"
					+" SPos:(,"+df.format(shovelInfo.getLon())+","+","+df.format(shovelInfo.getLat())+",)"+" UPos:(,"+df.format(uopInfo.getLon())+","+df.format(uopInfo.getLat())+",)"
					);
			
			if ((newMinTS > 0) && (newMinTS < minTS || minTS <= 0))
				minTS = newMinTS;
		}
		this.dataGenOpStartTime = this.latestGpsAt;
		if (prepForInstruction.isLoadedOnMovement && prepForInstruction.toType ==  SimInstruction.TYPE_UOP) {
			//generate unload pts
			UOpParam uopInfo = SimParams.getUopParam(prepForInstruction.toId);
			int unloadSec = SimParams.getVal(uopInfo.getUnloadTime(), uopInfo.getLoUnloadTime(), uopInfo.getHiUnloadTime());
			//generate data for dala up/down
			long ts = this.latestGpsAt;
			this.dataGenOpStartTime = ts;
			if (minTS <= 0 || minTS > ts)
				minTS = ts;
			if (maxTS <= 0 || maxTS < ts)
				maxTS = ts;
			psInsertLgd.setInt(1, this.dumperId);
			psInsertLgd.setInt(2, Misc.DALAUP_DIM_ID);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
			psInsertLgd.setDouble(4, lon);
			psInsertLgd.setDouble(5, lat);
			psInsertLgd.setDouble(6, 0);
			psInsertLgd.setDouble(7, 0);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, gpsName);
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
			psInsertLgd.addBatch();
			ts += unloadSec*1000;
			if (minTS <= 0 || minTS > ts)
				minTS = ts;
			if (maxTS <= 0 || maxTS < ts)
				maxTS = ts;
			psInsertLgd.setInt(1, this.dumperId);
			psInsertLgd.setInt(2, Misc.DALAUP_DIM_ID);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
			psInsertLgd.setDouble(4, lon);
			psInsertLgd.setDouble(5, lat);
			psInsertLgd.setDouble(6, 0);
			psInsertLgd.setDouble(7, 0);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, gpsName);
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
			psInsertLgd.addBatch();
			
			//now do up thingy
			ts += (1)*1000;
			if (minTS <= 0 || minTS > ts)
				minTS = ts;
			if (maxTS <= 0 || maxTS < ts)
				maxTS = ts;
			psInsertLgd.setInt(1, this.dumperId);
			psInsertLgd.setInt(2, Misc.DALAUP_DIM_ID);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
			psInsertLgd.setDouble(4, lon);
			psInsertLgd.setDouble(5, lat);
			psInsertLgd.setDouble(6, 1);
			psInsertLgd.setDouble(7, 0);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, gpsName);
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
			psInsertLgd.addBatch();
			long actEnd = ts;//-1000;
			long actPlus10 = actEnd + 1000*10;
			ts = this.latestGpsAt+10*1000;
			while (ts <actPlus10) {
				if (ts > actEnd)
					ts = actEnd;
				psInsertLgd.setInt(1, this.dumperId);
				psInsertLgd.setInt(2, 0);
				psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
				psInsertLgd.setDouble(4, lon);
				psInsertLgd.setDouble(5, lat);
				psInsertLgd.setDouble(6,  cummDist);
				psInsertLgd.setDouble(7, 0);
				psInsertLgd.setInt(8, 1);
				psInsertLgd.setString(9, gpsName);
				psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
				psInsertLgd.addBatch();
				this.latestGpsAt = ts;
				ts += 10*1000;
			}
			
			this.dataGenOpCompleteTime = this.latestGpsAt;
			this.lastUnloadTime = this.latestGpsAt;
			//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" Generated unload forward movement for dumper:"+this.getDumperId()+" Target:"+(prepForInstruction.toType ==  SimInstruction.TYPE_SHOVEL ? "S,":"D,")+prepForInstruction.toId+" load status:"+prepForInstruction.isLoadedOnMovement+" Start at:"+sdf.format(Misc.longToUtilDate(this.dataGenOpStartTime))+" Complete at:"+sdf.format(Misc.longToUtilDate(this.dataGenOpCompleteTime)));
		}
		else if (prepForInstruction.toType == SimInstruction.TYPE_SHOVEL){
			
			ShovelInfo shovelInfo = SimGenerateData.getShovelInfo(prepForInstruction.toId);
			ShovelInfo.ShovelQItem dumperEntry = new ShovelInfo.ShovelQItem(dumperId, this.latestGpsAt);
			shovelInfo.getPendingDumpers().offer(dumperEntry);
			this.dataGenOpStartTime = this.latestGpsAt;
			//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" Generated load forward movement for dumper:"+this.getDumperId()+" Target:"+(prepForInstruction.toType ==  SimInstruction.TYPE_SHOVEL ? "S,":"D,")+prepForInstruction.toId+" load status:"+prepForInstruction.isLoadedOnMovement+" Arrive at:"+sdf.format(Misc.longToUtilDate(this.dataGenOpStartTime)));
			//System.out.println("Shovel:"+shovelInfo.getShovelId()+" Adding dumper:"+this.getDumperId()+" LoadAt:"+sdf.format(Misc.longToUtilDate(this.latestGpsAt)));
		}
		return minTS;
	}
	public  synchronized long generateDataDuringLoad(Connection conn, PreparedStatement psInsertLgd, ShovelInfo fromShovel, ShovelQItem dumperEntry, ArrayList<MiscInner.Pair> cycleMarkers, int numShovelCycles, long currTS, int processCount) throws Exception {
		//if positionCompleted > currTS, then strik at positionCompleted
		//strike before cycle start and then 2s at 2nd/3rd cycle and the cycle just before last
		//regular gps data till load Complete ..
		//genera
		long minTS = -1;
		long maxTS = -1;
		if (dumperEntry.getPositionCompleted() > currTS && this.latestGpsAt < dumperEntry.getPositionCompleted()) {
			maxTS = minTS = dumperEntry.getPositionCompleted();
			psInsertLgd.setInt(1, this.dumperId);
			psInsertLgd.setInt(2, Misc.STRIKE_DIM_ID);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(dumperEntry.getPositionCompleted()));
			psInsertLgd.setDouble(4, lon);
			psInsertLgd.setDouble(5, lat);
			psInsertLgd.setDouble(6, 2);
			psInsertLgd.setDouble(7, 3);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, gpsName);
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(dumperEntry.getPositionCompleted()));
			psInsertLgd.addBatch();
			processCount++;
		//	System.out.println("GENERATE DATA DURING LOAD:: ProcessCount1::"+processCount);
			if (processCount > 1000) {
				psInsertLgd.executeBatch();
				processCount = 0;
			}
		}
		for (int i=0,is=numShovelCycles;i<is;i++) {
			boolean toGenStrike = i == 0 || i == 1 || (i == (numShovelCycles-2));
			if (toGenStrike) {
				long ts = dumperEntry.getLoadStart()+(cycleMarkers.get(i).first+2)*1000;
				if (ts < currTS)
					continue;
				if (minTS <= 0 || minTS > ts)
					minTS = ts;
				if (maxTS <= 0 || maxTS < ts)
					maxTS = ts;
				psInsertLgd.setInt(1, this.dumperId);
				psInsertLgd.setInt(2, Misc.STRIKE_DIM_ID);
				psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
				psInsertLgd.setDouble(4, lon);
				psInsertLgd.setDouble(5, lat);
				psInsertLgd.setDouble(6, 2);
				psInsertLgd.setDouble(7, 3);
				psInsertLgd.setInt(8, 1);
				psInsertLgd.setString(9, gpsName);
				psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
				psInsertLgd.addBatch();
				processCount++;
			//	System.out.println("GENERATE DATA DURING LOAD:: ProcessCount2::"+processCount);
				if (processCount > 1000) {
					psInsertLgd.executeBatch();
					processCount = 0;
				}
			}
		}
		//now generate positional data ..
		long ts = this.latestGpsAt + 10*1000;
		while (ts <= dumperEntry.getLoadComplete()) {
			if (ts >= currTS) {
				if (minTS <= 0 || minTS > ts)
					minTS = ts;
				if (maxTS <= 0 || maxTS < ts)
					maxTS = ts;
				psInsertLgd.setInt(1, this.dumperId);
				psInsertLgd.setInt(2, 0);
				psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
				psInsertLgd.setDouble(4, lon);
				psInsertLgd.setDouble(5, lat);
				psInsertLgd.setDouble(6, cummDist);
				psInsertLgd.setDouble(7, 0);
				psInsertLgd.setInt(8, 1);
				psInsertLgd.setString(9, gpsName);
				psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
				psInsertLgd.addBatch();
				processCount++;
			//	System.out.println("GENERATE DATA DURING LOAD:: ProcessCount3::"+processCount);
				if (processCount > 1000) {
					psInsertLgd.executeBatch();
					processCount = 0;
				}
				ts += 10*1000;
			}
		}
		if (maxTS > 0)
			this.latestGpsAt = maxTS;
		return minTS;
	}
	private long interpolateData(Connection conn, PreparedStatement psInsertLgd,  double dist, double stLon, double stLat, double enLon, double enLat, int routeTransitTime, double routeTransitDist) throws Exception {
		int secNeeded = (int)(dist/routeTransitDist*routeTransitTime);
		if (secNeeded <= 0)
			return -1;
		long ts = this.latestGpsAt+10*1000;
		long miTS = -1;
		long endAfter = this.latestGpsAt+secNeeded*1000;
		long endAfterEnsure = endAfter+10*1000;
		double initDist = this.cummDist;
		long initTS = this.latestGpsAt;
		while (ts < endAfterEnsure) {
			if (ts >= endAfter)
				ts = endAfter;
			if (miTS <= 0 || ts < miTS)
				miTS = ts;
				
			double frac = (double)(ts - initTS)/(double)(secNeeded*1000);
			double lon = (enLon-stLon)*frac+stLon;
			double lat = (enLat-stLat)*frac+stLat;
			double cummDist = dist*frac+initDist;
			this.lon = lon;
			this.lat = lat;
			this.cummDist = cummDist;
			this.latestGpsAt = ts;
			psInsertLgd.setInt(1, dumperId);
			psInsertLgd.setInt(2, 0);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
			psInsertLgd.setDouble(4, lon);
			psInsertLgd.setDouble(5, lat);
			psInsertLgd.setDouble(6, cummDist);
			psInsertLgd.setDouble(7, 20);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, gpsName);
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
			psInsertLgd.addBatch();
			ts += 10*1000;
		}
		return miTS;
	}
	
	private long interpolateDataForStlineRoute(Connection conn, PreparedStatement psInsertLgd,  double stLon, double stLat, double enLon, double enLat, int routeTransitTime, double routeTransitDist) throws Exception {
		double deltaDist = 10.0/(double)routeTransitTime*routeTransitDist;
		long ts = this.latestGpsAt;
		long tsEnd = this.latestGpsAt+1000*routeTransitTime;
		long miTS = this.latestGpsAt;
		for (int i=0,is=routeTransitTime%10 == 0 ? routeTransitTime/10 : routeTransitTime/10+1;i<is;i++) {
			double lon = stLon+(enLon-stLon)*((double)(i+1))/(double) is;
			double lat = stLat+(enLat-stLat)*((double)(i+1))/(double) is;
			ts += 10*1000;
			if (ts > tsEnd)
				ts = tsEnd;
			double cummDist = this.cummDist+deltaDist;
			this.lon = lon;
			this.lat = lat;
			this.cummDist = cummDist;
			this.latestGpsAt = ts;
			
			psInsertLgd.setInt(1, dumperId);
			psInsertLgd.setInt(2, 0);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
			psInsertLgd.setDouble(4, lon);
			psInsertLgd.setDouble(5, lat);
			psInsertLgd.setDouble(6, cummDist);
			psInsertLgd.setDouble(7, 20);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, this.getGpsName(conn, false));
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
			psInsertLgd.addBatch();
		}
		return miTS;
	}
	
	private long putDataForRouteOnly(boolean skipInit, Connection conn, PreparedStatement psInsertLgd,  int transitSec, int totDelay, ArrayList<GpsPlus> route, int startIndex, int endIndexIncl) throws Exception {
		long minTS = -1;
		long maxTS = -1;
		long ts = this.latestGpsAt;
		double delayAt = 0.3+0.4*Math.random();
		boolean iterateBack = startIndex > endIndexIncl;
		if (iterateBack && (startIndex == (route.size()-1))) //we already are at end point ..so can skip it
			skipInit = true;
		int numPoints = (iterateBack ? (startIndex-endIndexIncl) : (endIndexIncl-startIndex))+1;
		int posForDelay = (int)(delayAt * numPoints);
		if (posForDelay <= 0)
			posForDelay = 1;
		if (posForDelay >= numPoints)
			posForDelay = numPoints-1;
		
		for (int i=0; i<numPoints; i++) {
			int ptIndex = iterateBack ? startIndex-i : startIndex+i;
			int prevPtIndex = iterateBack ? ptIndex+1 : ptIndex-1;
			GpsPlus prevData = prevPtIndex < 0 || prevPtIndex >= route.size() ? null : route.get(prevPtIndex);
			GpsPlus data = route.get(ptIndex);
			
			if (i == posForDelay) {
				long nextTS = ts + (totDelay*1000);
				long toEnsureEnd = nextTS + 10*1000;
				while (ts < toEnsureEnd) {
					if (ts > nextTS)
						ts = nextTS;
					if (minTS <= 0 || minTS > ts)
						minTS = ts;
					if (maxTS <= 0 || maxTS < ts)
						maxTS = ts;
					psInsertLgd.setInt(1, this.dumperId);
					psInsertLgd.setInt(2, 0);
					psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
					psInsertLgd.setDouble(4, lon);
					psInsertLgd.setDouble(5, lat);
					psInsertLgd.setDouble(6,this. cummDist);
					psInsertLgd.setDouble(7, 0);
					psInsertLgd.setInt(8, 1);
					psInsertLgd.setString(9, gpsName);
					psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
					psInsertLgd.addBatch();
					ts += 10*1000;
				}
			}
			if (skipInit && i==0)
				continue;
			ts += (iterateBack ? -1 : 1) * (data.getGpsRecTime() - (prevData == null ? data.getGpsRecTime() : prevData.getGpsRecTime()));
			
			this.cummDist += (iterateBack ? -1 : 1)*(data.getAv()-(prevData == null ? 0 : prevData.getAv()));
			this.gpsName = data.getName();
			this.latestGpsAt = ts;
			this.lon = data.getLon();
			this.lat = data.getLat();
			this.gpsName = data.name;
			
			if (minTS <= 0 || minTS > ts)
				minTS = ts;
			if (maxTS <= 0 || maxTS < ts)
				maxTS = ts;
			psInsertLgd.setInt(1, this.dumperId);
			psInsertLgd.setInt(2, 0);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
			psInsertLgd.setDouble(4, data.getLon());
			psInsertLgd.setDouble(5, data.getLat());
			psInsertLgd.setDouble(6, this.cummDist);
			psInsertLgd.setDouble(7, data.getSpeed());
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, gpsName);
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
			psInsertLgd.addBatch();
			
		}
		
		return minTS;
	}
	
	public int getDumperId() {
		return dumperId;
	}
	public void setDumperId(int dumperId) {
		this.dumperId = dumperId;
	}
	
	public  synchronized double getLon() {
		return lon;
	}
	public  synchronized void setLon(double lon) {
		this.lon = lon;
	}
	public  synchronized double getLat() {
		return lat;
	}
	public  synchronized void setLat(double lat) {
		this.lat = lat;
	}
	
	public  synchronized SimInstruction getInstruction() {
		return instruction;
	}
	public  synchronized void setInstruction(SimInstruction instruction) {
		this.instruction = instruction;
	}
	public DumperInfo(int dumperId) {
		super();
		this.dumperId = dumperId;
	}
	public  synchronized int getShovelId() {
		return shovelId;
	}
	public  synchronized void setShovelId(int shovelId) {
		this.shovelId = shovelId;
	}
	public  synchronized int getUopId() {
		return uopId;
	}
	public  synchronized void setUopId(int uopId) {
		this.uopId = uopId;
	}
	public  synchronized double getCummDist() {
		return cummDist;
	}
	public  synchronized void setCummDist(double cummDist) {
		this.cummDist = cummDist;
	}
	public  synchronized long getLatestGpsAt() {
		return latestGpsAt;
	}
	public  synchronized void setLatestGpsAt(long latestGpsAt) {
		this.latestGpsAt = latestGpsAt;
	}
	public  synchronized String getGpsName() {
		return gpsName;
	}
	public  synchronized void setGpsName(String gpsName) {
		this.gpsName = gpsName;
	}
	public static void main(String a[]) throws Exception {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		long ts = System.currentTimeMillis()/1000*1000;
		SimGenerateData simgen = new SimGenerateData();
		for (int i=0;i<120*60;i++) {
			simgen.generateAndGetTS(conn, ts+i*1000);
		}
		
	}
	public static void logDisposition(long currTS, int status, int dumperId, int fromId, int returnId, int destType) {
		Connection conn = null;
		PreparedStatement ps = null;
		boolean destroyIt = false;
		try {
			int shovelId = fromId;
			int uopId = returnId;
			if (destType == SimInstruction.TYPE_UOP) {
				shovelId = returnId;
				uopId = fromId;
			}
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement("insert into dbg_sim(thread_id, dumper_id, curr_ts, to_shovel, manual, rshovel, ruop, status) values (?,?,?,?,?,?,?,?)");
			int colIndex = 1;
			ps.setInt(colIndex++, (int) Thread.currentThread().getId());
			ps.setInt(colIndex++, (int) dumperId);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(currTS));
			ps.setInt(colIndex++, destType == SimInstruction.TYPE_UOP ? 0 : 1);
			ps.setInt(colIndex++, 1);
			ps.setInt(colIndex++, shovelId);
			ps.setInt(colIndex++, uopId);
			ps.setInt(colIndex++, status);
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e2) {
					
				}
			}
		}
	}
	public  void dbgLog(long currTS, boolean destToShovel, boolean fromManualInstruction, int routineTransitTime, double routineTransitDist, long dbgLatestTS, double initLon, double initLat,  ShovelInfo shovelInfo, UOpParam uopInfo) {
		Connection conn = null;
		PreparedStatement ps = null;
		boolean destroyIt = false;
		try {
			
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement("insert into dbg_sim(thread_id, dumper_id, curr_ts, to_shovel, manual "
					+", rshovel, ruop, tt, td, move_ts "
					+", pb_start, pb_end, init_lon, init_lat, dest_lon"
					+", dest_lat, shovel_lon, shovel_lat, uop_lon, uop_lat"
					+", status "
					+") values "+
					"(?,?,?,?,?"
					+",?,?,?,?,?"
					+",?,?,?,?,?"
					+",?,?,?,?,?"
					+",?"
					+") ");
			
			int colIndex = 1;
			ps.setInt(colIndex++, (int) Thread.currentThread().getId());
			ps.setInt(colIndex++, (int) this.getDumperId());
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(currTS));
			ps.setInt(colIndex++, destToShovel ? 1 : 0);
			ps.setInt(colIndex++, fromManualInstruction ? 1 : 0);
			ps.setInt(colIndex++, shovelInfo.getShovelId());
			ps.setInt(colIndex++, uopId);
			ps.setInt(colIndex++, routineTransitTime);
			ps.setDouble(colIndex++, routineTransitDist);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(this.moveStarted));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(dbgLatestTS));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(this.latestGpsAt));
			ps.setDouble(colIndex++, initLon);
			ps.setDouble(colIndex++, initLat);
			ps.setDouble(colIndex++, this.lon);
			ps.setDouble(colIndex++, this.lat);
			ps.setDouble(colIndex++, shovelInfo.getLon());
			ps.setDouble(colIndex++, shovelInfo.getLat());
			ps.setDouble(colIndex++, uopInfo.getLon());
			ps.setDouble(colIndex++, uopInfo.getLat());
			ps.setInt(colIndex++, 3);
			ps.execute();
			if (!conn.getAutoCommit())
				conn.commit();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e2) {
					
				}
			}
		}
	}
	
}

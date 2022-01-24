package com.ipssi.simulHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.simulHelper.SimParams.*;
import com.ipssi.simulHelper.ShovelInfo.*;
import com.ipssi.simulHelper.DumperInfo.*;

public class SimGenerateData extends GenerateData {
	private static LinkedList<SimInstruction> g_instrList = new LinkedList<SimInstruction>();
	public static final boolean g_doStraightLineRoutes = true;
	public static final boolean g_doLoadFromAssignment = true;
	public static  boolean g_doAutoGenerateInstruction = false;//MAKE IT FALSE
	public static final int g_extraordinaryDelayRegime = 1;//could 0 or more (1,2,3,4) are reasonable 
	public static long g_lastUopCheckTime = 0;
	private static class MasterShovelInfo {
		int shovelId;
		double lon;
		double lat;
		String posName;
		public MasterShovelInfo(int shovelId, double lon,
				double lat, String posName) {
			super();
			this.shovelId = shovelId;
			this.lon = lon;
			this.lat = lat;
			this.posName = posName;
		}
	}
	public static void init(Connection conn) {
		//sim_shovel_params will have inv_pile_id as parameter ... we will shift around shovel_id so that the number of inv piles etc matches
		//sim dumper params will have start_shovel and start_uop ... we will 
		try {
			PreparedStatement ps = conn.prepareStatement("select shovel_id, inv_pile_id, status, master_shovel_id,master_inv_pile_id,master_lon,master_lat,master_pos_name from sim_shovel_params order by master_inv_pile_id");
			ResultSet rs = ps.executeQuery();
			ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>> requirement = new ArrayList<Pair<Integer, ArrayList<MiscInner.Pair>>>();
			ArrayList<Pair<Integer, ArrayList<MasterShovelInfo>>> shovelInfo = new ArrayList<Pair<Integer, ArrayList<MasterShovelInfo>>>();
			ArrayList<MiscInner.Pair> mappingToMaster = new ArrayList<MiscInner.Pair>();
			for (int art=1;art<=3;art++) {//3 inv pile id
				requirement.add(new Pair<Integer, ArrayList<MiscInner.Pair>>(art, new ArrayList<MiscInner.Pair>()));
				shovelInfo.add(new Pair<Integer, ArrayList<MasterShovelInfo>>(art, new ArrayList<MasterShovelInfo>()));
			}
			while (rs.next()) {
				int reqShovelId = rs.getInt(1);
				int reqInvPileId = rs.getInt(2);
				int status = rs.getInt(3);
				int masterInvPileId = rs.getInt(5);
				MasterShovelInfo shovel = new MasterShovelInfo(rs.getInt(4), rs.getDouble(6), rs.getDouble(7), rs.getString(8));
				for (int i=0,is=requirement.size(); i<is; i++) {
					if (requirement.get(i).first == reqInvPileId) {
						requirement.get(i).second.add(new MiscInner.Pair(reqShovelId, status));
						break;
					}
				}
				for (int i=0,is=shovelInfo.size(); i<is; i++) {
					if (shovelInfo.get(i).first == masterInvPileId) {
						shovelInfo.get(i).second.add(shovel);
						break;
					}
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("update sim_shovel_params set status=0");
			ps.executeUpdate();
			ps = Misc.closePS(ps);
			for (int art=0;art<3;art++) {
				ArrayList<MiscInner.Pair> reqList = requirement.get(art).second;
				ArrayList<MasterShovelInfo> avList = shovelInfo.get(art).second;
				int avIndex = 0;
				for (int i=0,is=reqList.size();i<is;i++) {
					
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static volatile long g_currTS = 0;
	public static SimInstruction addInstruction(int forDumper, long instrTime, int applyMode, int toId, int toType, int returnId) {
		DumperInfo.logDisposition(g_currTS, 0, forDumper, toId, returnId, toType);
		synchronized (g_instrList) {
			SimInstruction add = new SimInstruction(forDumper, instrTime, applyMode,  toId, toType, returnId);
			
			g_instrList.addFirst(add);
			return add;
		}
	}
	public  GenerateData generateAndGetTS(Connection conn, long currTS) throws Exception {
		long minLoadTS = -1;
		long minShovelGpsTS = -1;
		long minDumperGpsTS = -1;
		int processCount = 0;
		
		synchronized (g_instrList) {
			for (SimInstruction item = g_instrList.poll();item != null; item = g_instrList.poll()) {
				DumperInfo dumper = getDumperInfo(item.getForDumperId());
				if (dumper != null)
					dumper.setInstruction(item);
			}
		}
		PreparedStatement psInsertLoad = conn.prepareStatement("insert into exc_load_event_pb(vehicle_id, gps_record_time, quality, dig_prior_sec, stick_in_sec, swing_sec, boom_up, close_dur, updated_on) "+
				" values (?,?,?,?,?,?,?,?,?)"
			)
			;		
		PreparedStatement psInsertLgd = conn.prepareStatement("insert ignore into logged_data_pb(vehicle_id, attribute_id, gps_record_time, longitude, latitude, attribute_value, speed, source, name, updated_on) "+
		" values (?,?,?,?,?,?,?,?,?,?)");
		
		if (currTS <= 0) {
			currTS = System.currentTimeMillis();
			currTS = currTS/1000 * 1000;
		}
		if (g_lastUopCheckTime <= 0)
			g_lastUopCheckTime = currTS;
		
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		//System.out.println("Doing sim for:"+sdf.format(Misc.longToUtilDate(currTS)));
		if (!SimParams.inited()) {
			SimParams.init(conn);
			generateInitData(conn, psInsertLgd, currTS, processCount);
			minShovelGpsTS = currTS;
			minDumperGpsTS = currTS;
			
			if (!conn.getAutoCommit())
				conn.commit();
		}
		g_currTS = currTS;
		GenerateData retval = new GenerateData();
		//go thru shovels and figure out what event has completed and generate info
		Collection<ShovelInfo> shovelInfos = shovelQ.values();
		for(Iterator<ShovelInfo> iter = shovelInfos.iterator();iter.hasNext();) {
			ShovelInfo shovelInfo = iter.next();
			Queue<ShovelQItem> dumpers = shovelInfo.getPendingDumpers();
			ShovelQItem dumper = dumpers.peek();
			if (dumper != null && dumper.getArrivedAt() <=  currTS && dumper.getLoadStart() <= 0 && shovelInfo.canGenerateCycle(currTS)) {
				
				ShovelInfo.GenInfo genInfo = shovelInfo.generateCycles(conn,dumper,currTS, psInsertLoad, psInsertLgd, false);
				if (genInfo != null) {
					//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" Genertated cycles for shovel:"+shovelInfo.getShovelId()+" to load dumper:"+dumper.getDumperId()+" arrived at:"+sdf.format(Misc.longToUtilDate(dumper.getArrivedAt()))+" positioned at:"+sdf.format(Misc.longToUtilDate(dumper.getPositionCompleted()))+ " loadStart at:"+sdf.format(Misc.longToUtilDate(dumper.getLoadStart()))+" to complete at:"+sdf.format(Misc.longToUtilDate(dumper.getLoadComplete()))+" numcycle:"+genInfo.numShovelCycles);
					if (genInfo.tsStartLoadCycle > 0 && (minLoadTS <= 0 || minLoadTS > genInfo.tsStartLoadCycle))
						minLoadTS = genInfo.tsStartLoadCycle;
					DumperInfo dumperInfo = getDumperInfo(dumper.getDumperId());
					long ts = dumperInfo.generateDataDuringLoad(conn, psInsertLgd, shovelInfo, dumper, genInfo.cycleMarkers, genInfo.numShovelCycles, genInfo.numShovelCycles, processCount);
					if (ts > 0 && (ts < minDumperGpsTS || minDumperGpsTS < 0))
						minDumperGpsTS = ts;
				}
			}
			else if (dumper != null && dumper.getLoadComplete() > 0 && dumper.getLoadComplete() <= currTS) {
				DumperInfo dumperInfo = getDumperInfo(dumper.getDumperId());
				dumperInfo.dataGenOpCompleteTime = dumper.getLoadComplete();
				dumperInfo.lastOpCompleteOfLoad = true;
				//long ts = dumperInfo.generateDataFromToMovement(conn, psInsertLgd, currTS);
				//if (ts > 0 && ts < minDumperGpsTS)
				//	minDumperGpsTS = ts;
				dumpers.poll();

				//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" Load completed ... will generate movement later:"+shovelInfo.getShovelId()+" to load dumper:"+dumper.getDumperId()+" arrived at:"+sdf.format(Misc.longToUtilDate(dumper.getArrivedAt()))+" positioned at:"+sdf.format(Misc.longToUtilDate(dumper.getPositionCompleted()))+ " loadStart at:"+sdf.format(Misc.longToUtilDate(dumper.getLoadStart()))+" to complete at:"+sdf.format(Misc.longToUtilDate(dumper.getLoadComplete())));
			}
			else if (true && shovelInfo.canGenerateCycle(currTS) && (dumper == null || dumper.getArrivedAt() > currTS || dumper.getPositionCompleted() < 0 || dumper.getPositionCompleted() > currTS)) {
				//no dumper  ... generate clean cycle
				ShovelInfo.GenInfo genInfo = shovelInfo.generateCycles(conn,dumper,currTS, psInsertLoad, psInsertLgd, true);
				if (genInfo != null) {
					//System.out.println("[SIM]"+sdf.format(Misc.longToUtilDate(currTS))+" Genertated cycles for shovel:"+shovelInfo.getShovelId()+" to load dumper:"+dumper.getDumperId()+" arrived at:"+sdf.format(Misc.longToUtilDate(dumper.getArrivedAt()))+" positioned at:"+sdf.format(Misc.longToUtilDate(dumper.getPositionCompleted()))+ " loadStart at:"+sdf.format(Misc.longToUtilDate(dumper.getLoadStart()))+" to complete at:"+sdf.format(Misc.longToUtilDate(dumper.getLoadComplete()))+" numcycle:"+genInfo.numShovelCycles);
					if (genInfo.tsStartLoadCycle > 0 && (minLoadTS <= 0 || minLoadTS > genInfo.tsStartLoadCycle))
						minLoadTS = genInfo.tsStartLoadCycle;
					
				}
			}
			
			long nextGpsPosDueAt = shovelInfo.getLastLgdAt() + 60*1000;
			if (nextGpsPosDueAt < currTS)
				nextGpsPosDueAt =  currTS;
//			PreparedStatement psInsertLgd = conn.prepareStatement("insert ignore into logged_data(vehicle_id, attribute_id, gps_record_time, longitude, latitude, attribute_value, speed, source, name, updated_on) "+
//			" values (?,?,?,?,?,?,?,?,?,?)");

			while (nextGpsPosDueAt <= currTS ) {
				if (minShovelGpsTS <= 0 || minShovelGpsTS > nextGpsPosDueAt)
					minShovelGpsTS = nextGpsPosDueAt;

				psInsertLgd.setInt(1, shovelInfo.getShovelId());
				psInsertLgd.setInt(2, 0);
				psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(nextGpsPosDueAt));
				psInsertLgd.setDouble(4, shovelInfo.getLon());
				psInsertLgd.setDouble(5, shovelInfo.getLat());
				psInsertLgd.setDouble(6, 0);
				psInsertLgd.setDouble(7, 0);
				psInsertLgd.setInt(8, 1);
				psInsertLgd.setString(9, shovelInfo.getGpsName());
				psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(nextGpsPosDueAt));
				shovelInfo.setLastLgdAt(nextGpsPosDueAt);
				nextGpsPosDueAt += 60*1000;
				psInsertLgd.addBatch();
				processCount++;
				//System.out.println("Main:: ProcessCount1::"+processCount);
				if (processCount > 1000) {
					psInsertLgd.executeBatch();
					processCount = 0;
				}
			}
		}
		Collection<DumperInfo> dumperInfos = SimGenerateData.dumperInfos.values();
		for(Iterator<DumperInfo> iter = dumperInfos.iterator();iter.hasNext();) {
			DumperInfo dumperInfo = iter.next();
			
			 
			if (g_doAutoGenerateInstruction) {
				if (!dumperInfo.canGenerateInsutruction && dumperInfo.dataGenOpCompleteTime <= currTS && dumperInfo.dataGenOpCompleteTime > 0 && dumperInfo.dataGenOpType == SimInstruction.TYPE_SHOVEL) {
					dumperInfo.canGenerateInsutruction = true;
					dumperInfo.instructionGenAt = (int)(3*Math.random());
				}
				if (dumperInfo.dataGenOpType == SimInstruction.TYPE_SHOVEL && dumperInfo.dataGenOpStartTime <= currTS)
					dumperInfo.lastUnloadTime = -1;	
				boolean toAdd = false;
				if (dumperInfo.instruction == null && dumperInfo.canGenerateInsutruction && !toAdd 
						&& dumperInfo.instructionGenAt <= 0) {
					if (dumperInfo.dataGenOpType == SimInstruction.TYPE_UOP && dumperInfo.dataGenOpStartTime > currTS && dumperInfo.dataGenOpStartTime- currTS < 120*1000) {
						toAdd = true;
					}
				}
				if (dumperInfo.instruction == null && dumperInfo.canGenerateInsutruction && !toAdd 
						&& dumperInfo.instructionGenAt <= 1) {
					if (dumperInfo.dataGenOpType == SimInstruction.TYPE_UOP && dumperInfo.dataGenOpStartTime <= currTS && dumperInfo.dataGenOpCompleteTime >= currTS) {
						toAdd = true;
					}
				}
				if (dumperInfo.instruction == null && dumperInfo.canGenerateInsutruction && !toAdd 
						&& dumperInfo.instructionGenAt <= 2) {
					if (dumperInfo.dataGenOpType == SimInstruction.TYPE_SHOVEL
							&& dumperInfo.lastUnloadTime > 0 && dumperInfo.lastUnloadTime+30*1000 <= currTS
							&&  dumperInfo.dataGenOpStartTime > currTS) {
						toAdd = true;
					}
				}
				if (toAdd) {
					int whichShovel = (int)( Math.random()*shovelQ.size());
					int toId = dumperInfo.shovelId;
					if (whichShovel < 0)
						whichShovel = 0;
					else if (whichShovel > dbgShovelArrayList.size()-1)
						whichShovel = dbgShovelArrayList.size()-1;
					toId = dbgShovelArrayList.get(whichShovel);
					int assignedUopForNewShovel = SimParams.getAssignedUopForShovel(toId);
					addInstruction(dumperInfo.getDumperId(), -1, SimInstruction.APPLY_AFT_LU_COMPLETE, toId, SimInstruction.TYPE_SHOVEL, assignedUopForNewShovel).isManual = true;
					dumperInfo.canGenerateInsutruction = false;
				}
			}
			if (dumperInfo.dataGenOpCompleteTime >= currTS && dumperInfo.dataGenOpType != SimInstruction.TYPE_SHOVEL)
				dumperInfo.lastOpCompleteOfLoad = false;
			if (true || (dumperInfo.dataGenOpType == SimInstruction.TYPE_UOP && dumperInfo.dataGenOpCompleteTime <= currTS)) {
				long ts = dumperInfo.generateDataFromToMovement(conn, psInsertLgd, currTS);
				if (ts > 0 && (ts < minDumperGpsTS || minDumperGpsTS < 0))
					minDumperGpsTS = ts;
			}
			if (dumperInfo.getLatestGpsAt()+10*1000 <= currTS) {
				//generate data standing at same pt
				long ts = dumperInfo.getLatestGpsAt()+10*1000;
				if (ts < currTS)
					ts = currTS;
				while (ts <= currTS) {
					if (minDumperGpsTS <= 0 || minDumperGpsTS > ts)
						minDumperGpsTS = ts;
					psInsertLgd.setInt(1, dumperInfo.dumperId);
					psInsertLgd.setInt(2, 0);
					psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(ts));
					psInsertLgd.setDouble(4, dumperInfo.getLon());
					psInsertLgd.setDouble(5, dumperInfo.getLat());
					psInsertLgd.setDouble(6, dumperInfo.getCummDist());
					psInsertLgd.setDouble(7, 0);
					psInsertLgd.setInt(8, 1);
					psInsertLgd.setString(9, dumperInfo.getGpsName());
					psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(ts));
					psInsertLgd.addBatch();
					processCount++;
					//System.out.println("Main:: ProcessCount2::"+processCount);
					if (processCount > 1000) {
						psInsertLgd.executeBatch();
						processCount = 0;
					}
					dumperInfo.setLatestGpsAt(ts);
					ts += 10*1000;
				}
			}
		}
		psInsertLoad.executeBatch();
		psInsertLgd.executeBatch();
		psInsertLoad = Misc.closePS(psInsertLoad);
		psInsertLgd = Misc.closePS(psInsertLgd);
		if (!conn.getAutoCommit())
			conn.commit();
		retval.tsLoadEvent = minLoadTS;
		retval.tsShovel = minShovelGpsTS;
		retval.tsTipper = minDumperGpsTS;
		return retval;
	}
	public static String calcGpsName(Connection conn, int vehicleId, CacheTrack.VehicleSetup vehSetup, double lon, double lat, long currTS) {
		GpsData gpsData = new GpsData(currTS);
		gpsData.setLongitude(lon);
		gpsData.setLatitude(lat);
		String name = gpsData.getName(conn, vehicleId, vehSetup);
		return name;
	}
	private static void generateInitData(Connection conn, PreparedStatement psInsertLgd, long currTS, int processCount) throws Exception {
		Collection<ShovelInfo> shovelInfos = shovelQ.values();
		for(Iterator<ShovelInfo> iter = shovelInfos.iterator();iter.hasNext();) {
			ShovelInfo shovelInfo = iter.next();
			psInsertLgd.setInt(1, shovelInfo.shovelId);
			psInsertLgd.setInt(2, 0);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(currTS));
			psInsertLgd.setDouble(4, shovelInfo.lon);
			psInsertLgd.setDouble(5, shovelInfo.lat);
			shovelInfo.lastLgdAt = currTS;
			psInsertLgd.setDouble(6, 0);
			psInsertLgd.setDouble(7, 0);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, shovelInfo.getGpsName(conn, true));
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(currTS));
			psInsertLgd.addBatch();
			processCount++;
		//	System.out.println("GENERATE INIT DATA:: ProcessCount 1::"+processCount);
			if (processCount > 1000) {
				psInsertLgd.executeBatch();
				processCount = 0;
			}
			
		}
		Collection<DumperInfo> dumperInfos = SimGenerateData.dumperInfos.values();
		for(Iterator<DumperInfo> iter = dumperInfos.iterator();iter.hasNext();) {
			DumperInfo dumperInfo = iter.next();
			psInsertLgd.setInt(1, dumperInfo.dumperId);
			psInsertLgd.setInt(2, 0);
			psInsertLgd.setTimestamp(3, Misc.utilToSqlDate(currTS));
			psInsertLgd.setDouble(4, dumperInfo.getLon());
			psInsertLgd.setDouble(5, dumperInfo.getLat());
			dumperInfo.setLatestGpsAt(currTS);
			psInsertLgd.setDouble(6, 0);
			psInsertLgd.setDouble(7, 0);
			psInsertLgd.setInt(8, 1);
			psInsertLgd.setString(9, dumperInfo.getGpsName(conn, true));
			psInsertLgd.setTimestamp(10, Misc.utilToSqlDate(currTS));
			psInsertLgd.addBatch();
			processCount++;
			//System.out.println("GENERATE INIT DATA:: ProcessCount::"+processCount);
			if (processCount > 1000) {
				psInsertLgd.executeBatch();
				processCount = 0;
			}
			
		}
		psInsertLgd.executeBatch();
		
		
	}
	private static HashMap<Integer, ShovelInfo> shovelQ = new HashMap<Integer, ShovelInfo>();
	private static ArrayList<Integer> dbgShovelArrayList = new ArrayList<Integer>(); 
	public static ShovelInfo getShovelInfo(int shovelId) {
		return shovelQ.get(shovelId);
	}
	public static ShovelInfo putShovelInfo(int shovelId, ShovelParam param) {
		ShovelInfo retval = shovelQ.get(shovelId);
		if (retval != null)
			return retval;
		retval = new ShovelInfo(shovelId, param);
		shovelQ.put(shovelId, retval);
		dbgShovelArrayList.add(shovelId);
		return retval;
	}
	private static HashMap<Integer, DumperInfo> dumperInfos = new HashMap<Integer, DumperInfo>();
	public static DumperInfo getDumperInfo(int dumperId) {
		return dumperInfos.get(dumperId);
	}
	public static void putDumperInfo(int dumperId, DumperInfo dumperInfo) {
		dumperInfos.put(dumperId, dumperInfo);
	}
	public static void removeEntryForDumper(int shovelId, int dumperId) {
		ShovelInfo shovelInfo = getShovelInfo(shovelId);
		for (Iterator<ShovelQItem>iter = shovelInfo.getPendingDumpers().iterator();iter.hasNext();) {
			ShovelQItem item = iter.next();
			if (item.getDumperId() == dumperId) {
				iter.remove();
				break;
			}
		}
	}
	public static void main(String a[]) throws Exception {
		DumperInfo.logDisposition(System.currentTimeMillis(), 0, 1, 27350, 121, SimInstruction.TYPE_SHOVEL);
		if (true)
			return;
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		long ts = System.currentTimeMillis()/1000*1000;
		SimGenerateData simgen = new SimGenerateData();
		
		for (int i=0;i<120*60;i++) {
			simgen.generateAndGetTS(conn, ts+i*1000);
		}
	}
	//CONFIG parameters

}

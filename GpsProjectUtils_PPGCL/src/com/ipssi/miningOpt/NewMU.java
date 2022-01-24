package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.w3c.dom.*;



import com.ipssi.RegionTest.RegionTest;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.LatestTripInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Region;
import com.ipssi.mining.ManagementUnit;
import com.ipssi.processor.utils.GpsData;

public class NewMU {
	public static int DUMPER_TYPE = 0;
	public static int SHOVEL_TYPE = 1;
	public static int LOAD_SITE_PILE_TYPE = 1;
	public static int UNLOAD_SITE_PILE_TYPE = 2;
	public static int FUEL_DIM_ID = 3;
	public static int SHIFT_TIME_SEC = 8*60*60;
	public static int SHIFT_TIME_HR = 8;
	public static int TOTAL_QUEUE_TIME_HR = 2;
	public static int TOTAL_TRANSITION_TIME_SEC = 2*60*60;
	public static int TRANSITION_TIME_SEC = 60;
	public static int LOADING_UNLOADING_TIME_MIN = 2;
	public static int AVG_DUMPER_SPEED_KM_HR = 20;
	ExecutorService processor = Executors.newFixedThreadPool(1);
	public void submitTask(int dumperId, GpsData gpsData, LatestTripInfo latestTrip, boolean isInRest, boolean doOptimizer, VehicleDataInfo vdf) {
		processor.submit(new Task(dumperId, gpsData, this, latestTrip, isInRest, doOptimizer, vdf));
	}
	public static class Task implements Runnable {
		int dumperId;
		GpsData gpsData;
		NewMU ownerMU;
		LatestTripInfo latestTrip;
		boolean isInRest;
		boolean doOptimizer;
		VehicleDataInfo vdf;
		
		public void run() {
			// TODO Auto-generated method stub
			Connection conn = null;
			boolean destroyIt = false;
			try {
				DumperInfo dumperInfo = (DumperInfo) ownerMU.getVehicleInfo(dumperId);
				if (dumperInfo == null)
					return;
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				if (latestTrip != null) {
					dumperInfo.onUpdateTripChange(conn, ownerMU, latestTrip, this.isInRest, this.doOptimizer, vdf);
				}
				else {
					dumperInfo.onUpdateNewData(conn, ownerMU, gpsData);
				}
			}
			catch (Exception e) {
				destroyIt = true;
				e.printStackTrace();
				//eat it
			}
			finally {
				if (conn != null) {
					try {
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
					}
					catch (Exception e) {
						
					}
					conn = null;
				}
			}
		}

		public int getDumperId() {
			return dumperId;
		}

		public void setDumperId(int dumperId) {
			this.dumperId = dumperId;
		}

		public GpsData getGpsData() {
			return gpsData;
		}

		public void setGpsData(GpsData gpsData) {
			this.gpsData = gpsData;
		}

		public NewMU getOwnerMU() {
			return ownerMU;
		}

		public void setOwnerMU(NewMU ownerMU) {
			this.ownerMU = ownerMU;
		}

		public LatestTripInfo getLatestTrip() {
			return latestTrip;
		}

		public void setLatestTrip(LatestTripInfo latestTrip) {
			this.latestTrip = latestTrip;
		}

		public boolean isInRest() {
			return isInRest;
		}

		public void setInRest(boolean isInRest) {
			this.isInRest = isInRest;
		}

		public boolean isDoOptimizer() {
			return doOptimizer;
		}

		public void setDoOptimizer(boolean doOptimizer) {
			this.doOptimizer = doOptimizer;
		}

		public VehicleDataInfo getVdf() {
			return vdf;
		}

		public void setVdf(VehicleDataInfo vdf) {
			this.vdf = vdf;
		}

		public Task(int dumperId, GpsData gpsData, NewMU ownerMU,
				LatestTripInfo latestTrip, boolean isInRest,
				boolean doOptimizer, VehicleDataInfo vdf) {
			super();
			this.dumperId = dumperId;
			this.gpsData = gpsData;
			this.ownerMU = ownerMU;
			this.latestTrip = latestTrip;
			this.isInRest = isInRest;
			this.doOptimizer = doOptimizer;
			this.vdf = vdf;
		}
		
	}
	private static String GET_ALL_VEHICLES = "	select "+
	" vehicle.id vehicle_id, vehicle.name vehicle_name, vehicle.type vehicle_lov_type, vehicle_types.id vehicle_type_id, vehicle_types.name vehicle_type_name, vehicle.status vehicle_status, vehicle.detailed_status vehicle_detailed_status "+
	" ,capacity_wt, capacity_vol, uassigned_rate_hourly, assigned_rate_idling_hourly, assigned_rate_use_hourly, assigned_rate_use_perkm "+
	" ,cycle_time_second "+
	" ,fuel_consumption_rate_hourly ,fuel_consumption_rate_km,fuel_tank_capacity "+ 
	" ,vehicle_types.str_field1,vehicle_types.str_field2,vehicle_types.str_field3,vehicle_types.str_field4 "+
	" ,vehicle_types.int_field1,vehicle_types.int_field2,vehicle_types.int_field3,vehicle_types.int_field4 "+
	" ,vehicle_types.double_field1,vehicle_types.double_field2,vehicle_types.double_field3,vehicle_types.double_field4 "+
	" ,vehicle_types.date_field1,vehicle_types.date_field2,vehicle_types.date_field3,vehicle_types.date_field4" +
	" ,icon_use, icon_idling, icon_resting, icon_breakdown, icon_alarm, icon_nontracking "+

	"	from vehicle join port_nodes leafp on (leafp.id = vehicle.customer_id) join port_nodes ancp on (ancp.id = ? and ancp.lhs_number <= leafp.lhs_number and ancp.rhs_number >= leafp.rhs_number) join vehicle_types on (vehicle.type = vehicle_types.vehicle_type_lov) join port_nodes anc on (anc.id = vehicle_types.port_node_id) "+
	"	join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	"	where vehicle_types.vehicle_cat = ? and vehicle.status in (1,2) "
	;
	private static String GET_ALL_SITE = "select ip.id, ip.short_code, iplm.lowerX, iplm.lowerY, ip.length, ip.region_id, ip.status, ip.pit_id, ip.op_difficulty, ip.create_date, ip.close_date "+
		 " from inventory_piles ip join port_nodes leaf on (leaf.id = ip.port_node_id) "+ 
	" join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	" left outer join landmarks iplm on (iplm.id = ip.landmark_id) "+
	" where ip.pile_type = ? order by pit_id, ip.short_code ";
	private static String GET_ALL_SITE_MATERIAL = "select mm.inventory_pile_id, mm.material_id, mm.mix from  "+
	"  inventory_piles ip join port_nodes leaf on (leaf.id = ip.port_node_id) "+ 
	" join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	" join inv_material_mix mm on (mm.inventory_pile_id = ip.id) order by mm.inventory_pile_id, mm.mix asc, mm.inventory_pile_id desc "
	;
	private static String GET_ALL_SITE_NOT_ALLOWED = "select mm.inventory_pile_id, vehicle_type_id, vehicle_cat from  "+
	"  inventory_piles ip join port_nodes leaf on (leaf.id = ip.port_node_id) "+ 
	" join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	" join inv_not_allowed_veh_types mm on (mm.inventory_pile_id = ip.id) left outer join vehicle_types on (vehicle_types.id = vehicle_type_id) order by mm.inventory_pile_id, vehicle_type_id, vehicle_cat "
	;
	private ArrayList<ShiftDef> shifts = new ArrayList<ShiftDef>();
	private ConcurrentHashMap<Integer, LoadSite> loadSites = new ConcurrentHashMap<Integer, LoadSite>();
	private ConcurrentHashMap<Integer, UnloadSite> unloadSites = new ConcurrentHashMap<Integer, UnloadSite>();
	private ConcurrentHashMap<Integer, Route> routes = new ConcurrentHashMap<Integer, Route>();
	private ConcurrentHashMap<Integer, ShovelInfo> shovels = new ConcurrentHashMap<Integer, ShovelInfo>();
	private ConcurrentHashMap<Integer, DumperInfo> dumpers = new ConcurrentHashMap<Integer, DumperInfo>();
	private ConcurrentHashMap<Integer, Pits> pits = new ConcurrentHashMap<Integer, Pits>();
	private ConcurrentHashMap<Integer, Material> materialList = new ConcurrentHashMap<Integer, Material>();
	private static ConcurrentHashMap<Integer, NewMU> g_allManagementUnits = new ConcurrentHashMap<Integer, NewMU>();
	
	
	private int portNodeId = Misc.G_TOP_LEVEL_PORT;
	private boolean isLoaded = false;
	private long simulationNow = -1;
	
	
	private static int SHOVEL_IDLING_EVENT_ID = 56;
	private static int DUMPER_IDLING_EVENT_ID = 56; //stoppage
	
	public volatile Parameters parameters = new Parameters();
	
	public CoreVehicleInfo getVehicleInfo(int vehicleId) {
		CoreVehicleInfo retval = this.dumpers.get(vehicleId);
		if (retval == null)
			retval = this.shovels.get(vehicleId);
		return retval;
	}
	public Site getSiteInfo(int siteId) {
		Site retval = loadSites.get(siteId);
		if (retval == null)
			retval = unloadSites.get(siteId);
		return retval;
	}
	
	public boolean isValidAssignment(Connection conn, CoreVehicleInfo vehicleInfo, int vehicleCat, int siteId, int destId) throws Exception {
		return  isValidAssignmentBySite(conn, vehicleInfo, vehicleCat, siteId, destId) 
		   &&isValidForOtherVehicles(conn, vehicleInfo, vehicleCat, siteId)
		   && isValidAssignmentByPit(conn, siteId, destId);
		
	}
	public boolean setAssignment(Connection conn, int vehicleId, int siteId, int destId, long ts, int extAssignmentStatus, boolean doOnlyIfAssignmentValid, int srcOfAssignment) throws Exception {
		//1. check if assighment is possible
		//2. save assignment to db
		//3. remove current assignment from data structure
		//4. and set new assignment
		Pair<CoreVehicleInfo, Integer> vehicleInfo =  getVehicleInfo(conn, vehicleId);
		
		boolean isValid = isValidAssignment(conn, vehicleInfo.first, vehicleInfo.second, siteId, destId);
		if (!isValid)
			return false;
		//Step 2 - insert in DB
		PreparedStatement ps = conn.prepareStatement("delete from vehicle_assignment where vehicle_id = ? and at_time = ?");
		if (ts <= 0)
			ts = (new java.util.Date()).getTime();
		
		ps.setInt(1, vehicleId);
		ps.setTimestamp(2, Misc.longToSqlDate(ts));
		ps.executeUpdate();
		ps = Misc.closePS(ps);
		//create table vehicle_assignment(id int not null auto_increment, vehicle_id int, site_id int, dest_id int, make_unassigned int, at_time timestamp null default null);
		ps = conn.prepareStatement("insert into vehicle_assignment(vehicle_id, site_id, dest_id, make_unassigned, at_time, src_of_creation) values (?,?,?,?,?,?)");
		ps.setInt(1, vehicleId);
		Misc.setParamInt(ps, siteId, 2);
		Misc.setParamInt(ps, destId, 3);
		Misc.setParamInt(ps, extAssignmentStatus, 4);
		ps.setTimestamp(5, Misc.longToSqlDate(ts));
		ps.setInt(6, srcOfAssignment < 0 ? 0 : srcOfAssignment);
		ps.execute();
		ps = Misc.closePS(ps);
		
		//Step 3 - remove from current data structure
		
		helperRemoveAssignment(conn, vehicleInfo.first, vehicleInfo.second, ts);
		this.helperSetAssignment(conn, vehicleInfo.first, vehicleInfo.second, siteId, destId, extAssignmentStatus, ts, srcOfAssignment);
		
		//Step 4 - remove from current data structure
		if (!conn.getAutoCommit())
			conn.commit();
		return true;
		
	}
	
	
	private void helperRemoveAssignment(Connection conn, CoreVehicleInfo vehicleInfo, int vehicleCat, long ts) {
		if (vehicleCat == DUMPER_TYPE) {
			DumperInfo dumperInfo = (DumperInfo) vehicleInfo;
			Route route = this.routes.get(dumperInfo.getAssignedRoute());
			if (route != null) {
				route.removeVal(dumperInfo.getId());
				LoadSite site = this.getLoadSite(conn, route.getLoadSite());
				if (site != null) {
					site.removeDumperVal(dumperInfo.getId());
				}
				UnloadSite usite = this.getUnloadSite(conn, route.getUnloadSite());
				if (usite != null) {
					usite.removeDumperVal(dumperInfo.getId());
				}
			}
			dumperInfo.setAssignedRoute(Misc.getUndefInt());
			
		}
		else {
			ShovelInfo shovelInfo = (ShovelInfo) vehicleInfo;
			LoadSite site = this.getLoadSite(conn, shovelInfo.getAssignedLoadSite());
			if (site != null) {
				site.removeVal(vehicleInfo.getId());
			}
			shovelInfo.setAssignedLoadSite(Misc.getUndefInt());
		}
		vehicleInfo.setAssignmentStatus(CoreVehicleInfo.UNASSIGNED_BUT_AV);
		vehicleInfo.setLastAssignmentTime(ts);
	}
	
	private void helperSetAssignment(Connection conn, CoreVehicleInfo vehicleInfo, int vehicleCat, int siteId, int destId, int extAssignmentStatus, long ts, int srcOfAssignment) throws Exception {
		if (extAssignmentStatus == CoreVehicleInfo.ASSIGNED)
			extAssignmentStatus = CoreVehicleInfo.UNASSIGNED_BUT_AV;
		if (vehicleCat == DUMPER_TYPE) {
			DumperInfo dumperInfo = (DumperInfo) vehicleInfo;
			Route route = getRoute(conn, siteId, destId, true);
			if (route != null) {
				dumperInfo.setAssignedRoute(route.getId());
				
				route.addIfNotExist(dumperInfo.getId());
				extAssignmentStatus = CoreVehicleInfo.ASSIGNED;
			}
			LoadSite site = this.getLoadSite(conn, siteId);
			UnloadSite usite = this.getUnloadSite(conn, destId);
			 if (site != null) {
				 site.addDumperIfNotExist(dumperInfo.getId());
			 }
			 if (usite != null) {
				 usite.addDumperIfNotExist(dumperInfo.getId());
			 }
		}
		else {
			ShovelInfo shovelInfo = (ShovelInfo) vehicleInfo;
			LoadSite site = this.getLoadSite(conn, siteId);
			
			
			if (site != null) {
				shovelInfo.setAssignedLoadSite(siteId);
				site.addIfNotExist(shovelInfo.getId());
				extAssignmentStatus = CoreVehicleInfo.ASSIGNED;
			}
		}
		vehicleInfo.setSrcOfAssignment(srcOfAssignment);
		vehicleInfo.setAssignmentStatus(extAssignmentStatus);
		vehicleInfo.setLastAssignmentTime(ts);
	}
	
	public synchronized static NewMU getManagementUnit(Connection conn, int portNodeId) {
		try {
			if (Misc.isUndef(portNodeId))
				return null;
			Cache cache = Cache.getCacheInstance(conn);
			int miningPortNodeId = Misc.getUndefInt();
			for (MiscInner.PortInfo port = cache.getPortInfo(portNodeId, conn); port != null; port = port.m_parent) {
				ArrayList templ = port.m_orgFlexParams == null ? null :  port.m_orgFlexParams.getIntParams(OrgConst.ID_NEW_MANAGEMENT_UNIT_MARKER);
				if (templ != null && templ.size() > 0) {
					if (((Integer) templ.get(0)).intValue() == 1) {
						miningPortNodeId = port.m_id;
					}
					break;
				}
			}
			if (Misc.isUndef(miningPortNodeId)) {
				return null;//miningPortNodeId = Misc.G_TOP_LEVEL_PORT;
			}
			NewMU retval = g_allManagementUnits.get(miningPortNodeId);
			if (retval == null) {
				retval = new NewMU(miningPortNodeId);//dont ensure all here now ... we will do in prepForRedo
				retval.load(conn, true);
				g_allManagementUnits.put(miningPortNodeId, retval);
				Parameters.loadParameters(conn, miningPortNodeId);
			}
			
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Route getRouteInfo(int routeId) {
		return this.routes.get(routeId);
	}
	public Route getRoute(Connection conn, int siteId, int destId, boolean createIfNotExist) throws Exception {
		Collection<Route> allRoutes = this.routes.values();
		for (java.util.Iterator<Route> iter = allRoutes.iterator(); iter != null && iter.hasNext(); ) {
			Route retval = iter.next();
			if (retval.getLoadSite() == siteId && retval.getUnloadSite() == destId)
				return retval;
		}
		if (createIfNotExist) {
			Route retval = Route.createRoute(conn, siteId, destId, Misc.getUndefDouble(), Route.SWAG, this.portNodeId, this);
			this.routes.put(retval.getId(), retval);
			return retval;
		}
		else {
			return null;
		}
		
	}
	
	public ArrayList<Route> getAllRouteFromSrc(Connection conn, int siteId) throws Exception {
		ArrayList<Route> retval = new ArrayList<Route>();
		Collection<Route> allRoutes = this.routes.values();
		for (java.util.Iterator<Route> iter = allRoutes.iterator(); iter != null && iter.hasNext(); ) {
			Route item = iter.next();
			if (item.getLoadSite() == siteId)
				retval.add(item);
		}
		return retval;
	}
	
	public ArrayList<Route> getAllRouteToDest(Connection conn, int destId) throws Exception {
		ArrayList<Route> retval = new ArrayList<Route>();
		Collection<Route> allRoutes = this.routes.values();
		for (java.util.Iterator<Route> iter = allRoutes.iterator(); iter != null && iter.hasNext(); ) {
			Route item = iter.next();
			if (item.getUnloadSite() == destId)
				retval.add(item);
		}
		return retval;
	}
	public ShiftDef getShiftDef(long ts) {
		java.util.Date dt = new java.util.Date(ts);
		int hr = dt.getHours();
		int min = dt.getMinutes();
		for (int i=0,is=shifts.size();i<is;i++) {
			ShiftDef sd = shifts.get(i);
			boolean stOK = false;
			boolean enOK = false;
			if (sd.getEndHr() < sd.getStartHr()) {
				stOK = hr <= 24 ? (hr == sd.getStartHr() && min >= sd.getStartMin()) || (hr > sd.getStartHr())
						: true;
				enOK = hr <= 24 ? true : (hr == sd.getEndHr() && min < sd.getEndMin()) || (hr < sd.getEndHr());
			}
			else {
				stOK = (hr == sd.getStartHr() && min >= sd.getStartMin()) || (hr > sd.getStartHr());
				enOK = (hr == sd.getEndHr() && min < sd.getEndMin()) || (hr < sd.getEndHr());
			}
			if (stOK && enOK)
				return sd;
		}
		return shifts.get(0);
	}
	
	public synchronized void load(Connection conn, boolean must) throws Exception {
		if (this.isLoaded && !must)
			return;
		isLoaded = false;
		clear();
		loadMaterial(conn);
		loadShift(conn);
		loadPits(conn);
		loadSites(conn, LOAD_SITE_PILE_TYPE);
		loadSites(conn, UNLOAD_SITE_PILE_TYPE);
		loadRoute(conn);
		loadVehicles(conn, SHOVEL_TYPE);
		loadVehicles(conn,DUMPER_TYPE);
		loadAssignments(conn);
		loadShiftStartData(conn, System.currentTimeMillis());		
		this.isLoaded = true;
	}
	
	public void loadShiftStartData(Connection conn, long ts) throws Exception {
		PreparedStatement psBase = conn.prepareStatement("select logged_data.vehicle_id, attribute_value from logged_data join "+
				" ( "+
				" select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where  attribute_id=0 and gps_record_time <= ? and  vehicle_id = ? group by vehicle_id, attribute_id  "+
				" ) mx on(logged_data.vehicle_id = mx.vehicle_id and mx.attribute_id = logged_data.attribute_id and mx.grt = logged_data.gps_record_time) "
				);
		PreparedStatement psTrip = conn.prepareStatement(
				" select trip_info.vehicle_id, mu_site_id, mu_dest_id, sum(ldy.attribute_value-ldx.attribute_value)*1.05, count(*) from trip_info "+
				" join logged_data ldx on (ldx.vehicle_id = trip_info.vehicle_id and ldx.attribute_id=0 and ldx.gps_record_time = load_gate_in) "+
				" join logged_data ldy on (ldy.vehicle_id = trip_info.vehicle_id and ldy.attribute_id=0 and ldy.gps_record_time = unload_gate_in) "+
				 " where unload_gate_in >= ? and trip_info.vehicle_id = ? "
				);
		
		ShiftDef sd = this.getShiftDef(ts);
		java.util.Date shiftBefin = new java.util.Date(ts);
		int hr = sd == null ? 0 : sd.getStartHr();
		int min = sd == null ? 0 : sd.getStartMin();
		shiftBefin.setHours(hr);
		shiftBefin.setMinutes(min);
		if (shiftBefin.getTime() > ts)
			Misc.addDays(shiftBefin, -1);
		java.sql.Timestamp shiftTS = Misc.utilToSqlDate(shiftBefin);
		//initialze stuff
		for (Iterator<LoadSite> iter = this.loadSites.values().iterator(); iter.hasNext();) {
			Site site = iter.next();
			site.setTotTonnesInShift(0);
			site.setTotTripsInShift(0);
			//TODO reset time
		}
		for (Iterator<UnloadSite> iter = this.unloadSites.values().iterator(); iter.hasNext();) {
			Site site = iter.next();
			site.setTotTonnesInShift(0);
			site.setTotTripsInShift(0);
			//TODO reset time
		}
		psBase.setTimestamp(1, shiftTS);
		psTrip.setTimestamp(1, shiftTS);
		for (java.util.Iterator<DumperInfo> iter =  this.getAllDumpers().iterator(); iter.hasNext(); ) {
			DumperInfo dumper = iter.next();
			dumper.setLoadKMSinceReset(0);
			dumper.setDistMarkerAtReset(0);
			
			int vehicleId = dumper.getId();
			psBase.setInt(2, vehicleId);
			psTrip.setInt(2, vehicleId);
			ResultSet rs =psBase.executeQuery();
			if (rs.next()) {
				double marker = rs.getDouble(2);
				dumper.setDistMarkerAtReset(marker);
			}
			rs = Misc.closeRS(rs);
			rs = psTrip.executeQuery();
			while (rs.next()) {
				int siteId = rs.getInt(1);
				int destId = rs.getInt(2);
				double lkm = rs.getDouble(3);
				int cnt = rs.getInt(4);
				Site site = this.getLoadSite(conn, siteId);
				Site usite = this.getUnloadSite(conn, destId);
				double cap = dumper.getCapacityWt();
				if (site != null) {
					site.setTotTonnesInShift(cap*cnt+site.getTotTonnesInShift());
					site.setTotTripsInShift(cnt+site.getTotTripsInShift());
				}
				if (usite != null) {
					usite.setTotTonnesInShift(cap*cnt+usite.getTotTonnesInShift());
					usite.setTotTripsInShift(cnt+usite.getTotTripsInShift());
				}
				dumper.setLoadKMSinceReset(dumper.getLoadKMSinceReset()+lkm);
				dumper.setNumberTripsSinceReset(dumper.getNumberTripsSinceReset()+cnt);
			}
			rs = Misc.closeRS(rs);
		}
		psBase = Misc.closePS(psBase);
		psTrip = Misc.closePS(psTrip);
	}
	
	private void loadShift(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select distinct shift.id, shift.name,start_hour, start_min, stop_hour, stop_min from shift join shift_timings on (shift.id = shift_timings.shift_id) join port_nodes anc on (anc.id = shift.port_node_id) join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by start_hour");
		ps.setInt(1, this.portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			ShiftDef sd = ShiftDef.read(rs);
			shifts.add(sd);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		if (shifts.size() == 0)
			shifts.add(new ShiftDef(-1,"All",0,0,23,59));
	}
	private void loadRoute(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement(
				" select mrd.id, mrd.site_id, mrd.dest_id, mrd.dist, mrd.src_of_dist "+
				" from  "+
				" ( "+
				" select max(mrd.id) mxid, mrd.site_id, mrd.dest_id from "+
				"  ( "+
				"    select mrd.site_id, mrd.dest_id, max(case when updated_on > created_on then updated_on else created_on end) mxdt "+
				"    from mining_route_def mrd join port_nodes leaf on (mrd.port_node_id = leaf.id)  "+
				"    join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
				"    where mrd.status in (1,2) "+
				"    group by mrd.site_id, mrd.dest_id "+
				"  ) ml join mining_route_def mrd on (mrd.site_id = ml.site_id and mrd.dest_id = ml.dest_id "+ 
				"      and (case when mrd.updated_on > mrd.created_on then mrd.updated_on else mrd.created_on end) = ml.mxdt) "+ 
				" group by mrd.site_id, mrd.dest_id "+
				" ) ml join mining_route_def mrd on (mrd.id = ml.mxid) "
				
		);
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			int siteId = Misc.getRsetInt(rs, 2);
			int destId = Misc.getRsetInt(rs, 3);
			double dist = Misc.getRsetDouble(rs, 4);
			int src = Misc.getRsetInt(rs, 5);
			if (Misc.isUndef(siteId) || Misc.isUndef(destId))
				continue;
			if (dist < 0.00005)
				dist = Misc.getUndefDouble();
			Route route = new Route(id, this);
			route.setLoadSite(siteId);
			route.setUnloadSite(destId);
			route.setDistance(dist);
			route.setDistSrc(src);
			this.routes.put(id, route);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		Collection<Route> allRoutes = this.getAllRoutes();
		for (java.util.Iterator<Route> iter = allRoutes.iterator(); iter.hasNext(); ) {
			Route route = iter.next();
			if (Misc.isUndef(route.getDistance())) {
				Pair<Double, Integer> distEst = route.estDist(conn, route.getLoadSite(), route.getUnloadSite());
				route.updateDist(conn, distEst.first, distEst.second);
				route.setDistance(distEst.first);
				route.setDistSrc(distEst.second);
				if (!conn.getAutoCommit())
					conn.commit();
			}
		}
	}
	private void loadNotAllowedDumperTyAtShovel(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select distinct vehicle_type_not_allowed.shovel_type_id, vehicle_type_not_allowed.dumper_type_id from vehicle_type_not_allowed join vehicle_types on (vehicle_type_not_allowed.shovel_type_id = vehicle_types.id) join port_nodes leaf on (leaf.id=vehicle_types.port_node_id) left outer join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) order by shovel_type_id");
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		int prevShovelTypeId = -1;
		ArrayList<Integer> prevList = null;
		while (rs.next()) {
			if (prevShovelTypeId != rs.getInt(1)) {
				if (prevList != null && prevList.size() != 0) {
					ArrayList<ShovelInfo> forTheseShovels = getShovelOfType(prevShovelTypeId);
					for (int t1=0,t1s=forTheseShovels.size(); t1<t1s;t1++) {
						forTheseShovels.get(t1).setNotAllowedOtherCatTypes((ArrayList<Integer>)prevList.clone()); 
					}
				}
				prevList = null;
				prevShovelTypeId = rs.getInt(1);
			}
			if (prevList == null)
				prevList = new ArrayList<Integer>();
			prevList.add(rs.getInt(2));
		}
		if (prevList != null &&prevList.size() != 0 && prevShovelTypeId != -1) {
			ArrayList<ShovelInfo> forTheseShovels = getShovelOfType(prevShovelTypeId);
			for (int t1=0,t1s=forTheseShovels.size(); t1<t1s;t1++) {
				forTheseShovels.get(t1).setNotAllowedOtherCatTypes((ArrayList<Integer>)prevList.clone()); 
			}
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	private ArrayList<ShovelInfo> getShovelOfType(int ty) {
		ArrayList<ShovelInfo> retval = new ArrayList<ShovelInfo>();
		Collection<ShovelInfo> shovels = this.shovels.values();
		for (java.util.Iterator<ShovelInfo> iter = shovels.iterator(); iter.hasNext();) {
			ShovelInfo shovel = iter.next();
			if (shovel.getVehicleTypeId() == ty) {
				retval.add(shovel);
			}
		}
		return retval;
	}
	private void loadMaterial(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select gp.id, gp.name, flex_field1, flex_field2, flex_field3 from generic_params gp join port_nodes anc on (anc.id=gp.port_node_id) join port_nodes leaf on (leaf.id=? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where gp.status=1 and gp.param_id=20451 order by name");
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Material mat = new Material(rs.getInt(1), rs.getString(2), rs.getString(3), Misc.getParamAsDouble(rs.getString(4)), Misc.getParamAsDouble(rs.getString(5), 2.7));
			this.materialList.put(mat.getId(), mat);
		}
		
	}
	private void loadAssignments(Connection conn) throws Exception {
		//create table vehicle_assignment(id int not null auto_increment, vehicle_id int, site_id int, dest_id int, make_unassigned int, at_time timestamp null default null);
		PreparedStatement ps = conn.prepareStatement("select vehicle_assignment.vehicle_id, site_id, dest_id, make_unassigned, at_time,src_of_creation from vehicle_assignment join vehicle on (vehicle.id = vehicle_assignment.vehicle_id) join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join (select vehicle_id, max(at_time) mx from vehicle_assignment group by vehicle_id) mxl on (mxl.mx = at_time and mxl.vehicle_id = vehicle_assignment.vehicle_id) ");
		ps.setInt(1, portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int vehicleId = rs.getInt(1);
			Pair<CoreVehicleInfo, Integer> vehInfo = this.getVehicleInfo(conn, vehicleId);
			int siteId = Misc.getRsetInt(rs, "site_id");
			int destId = Misc.getRsetInt(rs, "dest_id");
			long atTs = Misc.sqlToLong(rs.getTimestamp("at_time"));
			int srcOfAssignment = rs.getInt("src_of_creation");
			this.helperRemoveAssignment(conn, vehInfo.first, vehInfo.second, atTs);
			this.helperSetAssignment(conn, vehInfo.first, vehInfo.second,siteId , destId, Misc.getRsetInt(rs, "make_unassigned"), atTs, srcOfAssignment);
		}
		rs = Misc.closeRS(rs);
		ps=Misc.closePS(ps);
	}
	private void loadPits(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select mining_pits.id, mining_pits.name from mining_pits join port_nodes leaf on (leaf.id = mining_pits.port_node_id) join port_nodes anc on (anc.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where mining_pits.status in (1) order by mining_pits.name");
		ps.setInt(1, this.portNodeId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			String name = rs.getString(2);
			Pits pit = new Pits(id, name, this);
			this.pits.put(pit.getId(), pit);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	}
	
	public synchronized void clear() {
		this.pits.clear();
		this.shovels.clear();
		this.dumpers.clear();
		this.routes.clear();
		this.loadSites.clear();
		this.unloadSites.clear();
	}
	public Pair<CoreVehicleInfo, Integer> getVehicleInfo(Connection conn, int vehicleId) {
		CoreVehicleInfo info = this.shovels.get(vehicleId);
		int vehicleCat = SHOVEL_TYPE;
		if (info == null) {
			vehicleCat = DUMPER_TYPE;
			info = this.dumpers.get(vehicleId);
		}
		return new Pair<CoreVehicleInfo, Integer>(info, vehicleCat);
	}
	private boolean isValidAssignmentByPit(Connection conn, int siteId, int destId) {
		Site loadSite = this.loadSites.get(siteId);
		Site unloadSite = this.unloadSites.get(destId);
		boolean retval = true;
		if (loadSite != null && unloadSite != null) {
			boolean pitSame = (Misc.isUndef(loadSite.getPitId())) || (Misc.isUndef(unloadSite.getPitId())) || loadSite.getPitId() == unloadSite.getPitId();
			if (!pitSame) {
				System.out.println("Assignment for dumper between site:"+siteId+" dest:"+destId+" not allwoed because diff pit");
				return false;
			}
			//get by Material
			int lm = loadSite.getMaterialId();
			int um = unloadSite.getMaterialId();
			Material lmat = this.materialList.get(lm);
			Material umat = this.materialList.get(um);
			if (lmat != null && umat != null && lmat.getMaterialCat() != null && umat.getMaterialCat() != null &&! lmat.getMaterialCat().equals(umat.getMaterialCat())) {
				System.out.println("Assignment for dumper between site:"+siteId+" dest:"+destId+" not allwoed because diff material cat:"+lmat.getMaterialCat()+" umat:"+umat.getMaterialCat());
				return false;
			}
		}
		return true;
	}
	private boolean isValidAssignmentBySite(Connection conn, CoreVehicleInfo vehicleInfo, int vehicleCat, int siteId, int destId) {
		boolean retval = true;
		LoadSite loadSite = this.loadSites.get(siteId);
		if (loadSite != null) {
			retval =  !Helper.isInList(vehicleCat == SHOVEL_TYPE ? loadSite.getNotAllowedLoaderTypes() : loadSite.getNotAllowedDumperTypes(), vehicleInfo.getVehicleTypeId());
		}
		if (!retval) {
			System.out.println("Invalid assignment of vehicle:"+vehicleInfo.getId()+" at site:"+siteId+" dest:"+destId+" becasue of cat Not allowed at Load");
			return retval;
		}
		UnloadSite unloadSite = this.unloadSites.get(destId);
		if (unloadSite != null) {
			
			retval =  !Helper.isInList(vehicleCat == SHOVEL_TYPE ? unloadSite.getNotAllowedLoaderTypes() : unloadSite.getNotAllowedDumperTypes(), vehicleInfo.getVehicleTypeId());
			if (!retval) {
				System.out.println("Invalid assignment of vehicle:"+vehicleInfo.getId()+" at site:"+siteId+" dest:"+destId+" becasue of cat Not allowed at Unload");
			}
		}
		return retval;
	}
	private boolean isValidForOtherVehicles(Connection conn, CoreVehicleInfo vehicleInfo, int vehicleCat, int siteId) throws  Exception {
		boolean retval = true;
	
		if (vehicleCat == DUMPER_TYPE) {
			LoadSite loadSite = this.loadSites.get(siteId);
			if (loadSite == null)
				return true;
			int dumperType = vehicleInfo.getVehicleTypeId();
			try {
				loadSite.getReadLock();
				ArrayList<Integer> shovelsAssigned = loadSite.getAssignedShovels();
				
				for (int i=0,is=shovelsAssigned == null ? 0 : shovelsAssigned.size(); i<is; i++) {
					ShovelInfo shovelInfo = this.shovels.get(shovelsAssigned.get(i));
					if (shovelInfo != null && shovelInfo.isNotAllowedDumperType(dumperType)) {
						System.out.println("Invalid assignment of vehicle:"+vehicleInfo.getId()+" at site:"+siteId+" becasue of cat Not allowed for shovel:"+shovelInfo.getId()+" ty:"+shovelInfo.getVehicleTypeId());
						return false;
					}
				}
			}
			catch (Exception e2) {
				
			}
			finally {
				loadSite.releaseReadLock();
			}
			return true;
		}
		else {//shovel should be compatible with any of the dumper coming to it on any of the routes
			ArrayList<Route> routesAssigned = this.getAllRouteFromSrc(conn, siteId);
			ShovelInfo shovelInfo = (ShovelInfo) vehicleInfo;
			for (int i=0,is=routesAssigned == null ? 0 : routesAssigned.size(); i<is; i++) {
				Route route = routesAssigned.get(i);
				try {
					route.getReadLock();
					for (int j=0,js=route.getAssignedDumpers().size(); j<js; j++) {
						int dumperId = route.getAssignedDumpers().get(j);
						DumperInfo dumperInfo = dumpers.get(dumperId);
						if (shovelInfo.isNotAllowedDumperType(dumperInfo.getVehicleTypeId())) {
							System.out.println("Invalid assignment of vehicle:"+vehicleInfo.getId()+" at site:"+siteId+" becasue of cat Not allowed for dumper:"+dumperInfo.getId()+" ty:"+dumperInfo.getVehicleTypeId());
							return false;
						}
					}
				}
				catch (Exception e) {
					
				}
				finally {
					route.releaseReadLock();
				}
			}
		}
		return true;		
	}
	
	private void loadSites(Connection conn, int pileType) throws Exception {
		PreparedStatement ps = conn.prepareStatement(GET_ALL_SITE);
		ps.setInt(1, portNodeId);
		ps.setInt(2, pileType);
		ResultSet rs = ps.executeQuery();
		Pits oldPit = null;
		Site oldSite = null;
		while (rs.next()) {
			int id = rs.getInt(1);
			String name = rs.getString(2);
			double lon = Misc.getRsetDouble(rs, 3);
			double lat = Misc.getRsetDouble(rs, 4);
			double len = Misc.getRsetDouble(rs, 5);
			int regionId = Misc.getRsetInt(rs, 6);
			int status = Misc.getRsetInt(rs, 7);
			int pitId = Misc.getRsetInt(rs, 8);
			double opDiff = Misc.getRsetDouble(rs,9,1);
			long startDate = Misc.sqlToLong(rs.getTimestamp(10));
			long endDate = Misc.sqlToLong(rs.getTimestamp(11));
			
			if (oldPit != null && oldPit.getId() != pitId) {
				oldPit = null;
			}
			if (oldPit == null) {
				oldPit = this.getPit(conn, pitId);
				oldSite = null;
			}
			if (oldSite != null && oldSite.getId() != id) 
				oldSite = null;
			
			if (oldSite == null) 
				oldSite = pileType == LOAD_SITE_PILE_TYPE ? this.getLoadSite(conn, id) : this.getUnloadSite(conn, id);
			if (oldSite == null) {
				oldSite = pileType == LOAD_SITE_PILE_TYPE ? (Site) new LoadSite(id, this) : (Site) new UnloadSite(id, this);
				if (pileType == LOAD_SITE_PILE_TYPE)
					this.loadSites.put(id, (LoadSite) oldSite);
				else
					this.unloadSites.put(id, (UnloadSite) oldSite);
			}
			
			if (oldPit != null)
				Helper.addIfNotExist( pileType == NewMU.LOAD_SITE_PILE_TYPE ? oldPit.getLoadSites() : oldPit.getUnloadSites(), id);
			oldSite.setName(name);
			oldSite.setLon(lon);
			oldSite.setLat(lat);
			oldSite.setLowRadius(len);
			oldSite.setRegion(RegionTest.getRegionInfo(regionId, conn));
			oldSite.setStatus(status);
			oldSite.setPitId(pitId);
			oldSite.setDifficulty(opDiff);
			oldSite.setStartFrom(startDate);
			oldSite.setEndTill(endDate);
			//Add to RTree
			Region r = oldSite.getRegion() == null ? null : oldSite.getRegion().region;
			if (r == null && !Misc.isUndef(oldSite.getLon()) && !Misc.isEqual(oldSite.getLon(), 0)) {
				r = com.ipssi.geometry.Region.getLongLatBoxAround(new Point(oldSite.getLon(), oldSite.getLat()), oldSite.getLowRadius());
			}
			if (r != null)
				InvRTree.addInventoryRegion(r, oldSite.getId());
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		
		ps = conn.prepareStatement(GET_ALL_SITE_MATERIAL);
		ps .setInt(1, portNodeId);
		rs = ps.executeQuery();
		oldSite = null;
		
		while (rs.next()) {
			int siteId = rs.getInt(1);
			int matId = rs.getInt(2);
			double mixPerc = Misc.getRsetDouble(rs, 3);
			if (oldSite != null && oldSite.getId() != siteId)
				oldSite = null;
			if (oldSite == null) {
				oldSite = pileType == LOAD_SITE_PILE_TYPE ? this.getLoadSite(conn, siteId) : this.getUnloadSite(conn, siteId);
			}
			if (oldSite == null)
				continue;
			oldSite.setMaterialId(matId);
		}
		rs=  Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		
		ps = conn.prepareStatement(GET_ALL_SITE_NOT_ALLOWED);
		ps .setInt(1, portNodeId);
		rs = ps.executeQuery();
		oldSite = null;
		
		while (rs.next()) {
			int siteId = rs.getInt(1);
			int vehTypeId = rs.getInt(2);
			int vehCat = rs.getInt(3);
			if (oldSite != null && oldSite.getId() != siteId)
				oldSite = null;
			if (oldSite == null) {
				oldSite = pileType == LOAD_SITE_PILE_TYPE ? this.getLoadSite(conn, siteId) : this.getUnloadSite(conn, siteId);
			}
			if (oldSite == null)
				continue;
			Helper.addIfNotExist(vehCat == DUMPER_TYPE ? oldSite.getNotAllowedDumperTypes() : oldSite.getNotAllowedLoaderTypes(), vehTypeId);
		}
		rs=  Misc.closeRS(rs);
		ps = Misc.closePS(ps);
	
	}

	public void loadVehicles(Connection conn, int vehCatType) throws Exception {
		PreparedStatement ps = conn.prepareStatement(GET_ALL_VEHICLES);
		ps.setInt(1,portNodeId);
		ps.setInt(2, portNodeId);
		ps.setInt(3, vehCatType);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
        	int vehId = rs.getInt(1);
        	CoreVehicleInfo info = null;
        	if (vehCatType == SHOVEL_TYPE) {
        		info = this.shovels.get(vehId);
        		if (info == null) {
        			info = new ShovelInfo(vehId, this);
        			this.shovels.put(vehId, (ShovelInfo) info);
        		}
        	}
        	else  {
        		info = this.dumpers.get(vehId);
        		if (info == null) {
        			info = new DumperInfo(vehId, this);
        			this.dumpers.put(vehId, (DumperInfo) info);
        		}
        	}
        	info.populateInfo(rs);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		if (vehCatType == SHOVEL_TYPE)
			this.loadNotAllowedDumperTyAtShovel(conn);
	}
	
	public Collection<LoadSite> getAllLoadSites() {
		return loadSites.values();
	}
	public Collection<UnloadSite> getAllUnloadSites() {
		return unloadSites.values();
	}
	public Collection<Route> getAllRoutes() {
		return routes.values();
	}
	public Collection<ShovelInfo> getAllShovels() {
		return shovels.values();
	}
	public Collection<DumperInfo> getAllDumpers() {
		return dumpers.values();
	}
	public Collection<Pits> getAllPits() {
		return pits.values();
	}
	public Pits getPit(Connection conn, int id) {
		Pits retval = pits.get(id);
		return retval;
	}
	public LoadSite getLoadSite(Connection conn, int id) {
		LoadSite retval = loadSites.get(id);
		return retval;
	}
	public UnloadSite getUnloadSite(Connection conn, int id) {
		UnloadSite retval = unloadSites.get(id);
		return retval;
	}

	public Pair<Integer, Integer> getBestRouteDest(Connection conn, int forVehicleId, int siteId, int destIdAsPerAssignment, int destIdAsPerPrev, int routeAsPerPrev) throws Exception {
		ArrayList<Route> routes = getAllRouteFromSrc(conn, siteId);
		Route currBestRoute = null;
		Site site = this.getSiteInfo(siteId);
		int bestScore = -1; //matches per assignment = 1000, matches destIdAsPerPrev 900, matches routeAsPerPrev 800
		//, if diff pit, bit undef then not valid
		//, if same pit: matches as per material same pit 700/ matches as perMaterialCat 650		
		//, if diff pit but one is undef then 600/550
		//, else if same pit and both undef then 500
		//, else if same pit and one is under then 400
		//else 300
		boolean multiFound = false;
		Pair<CoreVehicleInfo, Integer> vehicleInfo = this.getVehicleInfo(conn, forVehicleId);
		for (int i=0,is=routes == null ? 0 : routes.size(); i<is; i++) {
			Route route = routes.get(i);
			if (route.getUnloadSite() == destIdAsPerAssignment && !Misc.isUndef(destIdAsPerAssignment)) {
				currBestRoute = route;
				bestScore = 1000;
				break;
			}
			int myScore = -1;
			if (route.getUnloadSite() == destIdAsPerAssignment && !Misc.isUndef(destIdAsPerAssignment)) {
				myScore = 900;
			}
			else if (route.getId() == routeAsPerPrev) {
				myScore = 800;
			}
			else {
				Site usite = getSiteInfo(route.getUnloadSite());
				if (usite == null)
					continue;
				
				boolean exactPit = site.getPitId() == usite.getPitId() && !Misc.isUndef(site.getPitId());
				boolean approxPit = (Misc.isUndef(site.getPitId()) || Misc.isUndef(usite.getPitId()));
				if (!exactPit && !approxPit)
					continue;
				if (!isValidAssignmentBySite(conn, vehicleInfo.first, vehicleInfo.second, siteId, route.getUnloadSite()))
					continue;
				Material lmat = this.materialList.get(site.getMaterialId());
				Material umat = this.materialList.get(usite.getMaterialId());
				
				if (!Misc.isUndef(site.getMaterialId()) && usite.getMaterialId() == site.getMaterialId()) {
					myScore = exactPit ? 700 : 600;
				}
				if (lmat == null || umat == null) {
					myScore = exactPit ? 650 : 550;
				}
				else if (lmat.getMaterialCat() == null || umat.getMaterialCat() == null) {
					myScore = exactPit ? 650 : 550;
				}
				else if (lmat.getMaterialCat().equals(umat.getMaterialCat())) {
					myScore = exactPit ? 650 : 550;
				}
				else if (exactPit)
					myScore = 500;
				else 
					myScore = 400;
			}
			if (myScore > 0 && myScore > bestScore) {
				currBestRoute = route;
				bestScore = myScore;
			}
			else if (myScore > 0 && myScore == bestScore) {
				multiFound = true;
			}
		}//for each possible dest
		int retRouteId = currBestRoute == null ? Misc.getUndefInt() : currBestRoute.getId();
		int retDestId = currBestRoute == null ? Misc.getUndefInt() : currBestRoute.getUnloadSite();
		if (Misc.isUndef(retRouteId)) {
			retRouteId = routeAsPerPrev; 
		}
		if (Misc.isUndef(retDestId)) {
			retDestId = Misc.isUndef(destIdAsPerAssignment) ? destIdAsPerPrev : destIdAsPerAssignment;
		}
		return new Pair<Integer, Integer>(retRouteId, retDestId);
	}
	
	
	public int[]  getSitesWithLatLon(boolean doLoad, long ts) {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		if (doLoad) {
			Collection<LoadSite> list = this.getAllLoadSites();
			for (java.util.Iterator<LoadSite> iter = list.iterator(); iter.hasNext();) {
				LoadSite site = iter.next();
				if ((site.getRegion() != null || !Misc.isUndef(site.getLon())) && site.isDateValid(ts)) {
					retval.add(site.getId());
				}
			}
		}
		else {
			Collection<UnloadSite> list = this.getAllUnloadSites();
			for (java.util.Iterator<UnloadSite> iter = list.iterator(); iter.hasNext();) {
				UnloadSite site = iter.next();
				if ((site.getRegion() != null || !Misc.isUndef(site.getLon())) && site.isDateValid(ts))
					retval.add(site.getId());
			}
		}
		if (retval.size() == 0)
			return null;
		int[] retvalAsArr = new int[retval.size()];
		for (int i=0,is=retval.size(); i<is; i++)
			retvalAsArr[i] = retval.get(i);
		return retvalAsArr;
	}
	
	public NewMU(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	
	public void removeAllAssignments(Connection conn, long ts, int srcOfAssignment) throws Exception {
		Collection<ShovelInfo> shovels = this.shovels.values();
		for (java.util.Iterator<ShovelInfo> iter=shovels.iterator(); iter.hasNext(); ) {
			ShovelInfo shovel = iter.next();
			this.setAssignment(conn, shovel.getId(), Misc.getUndefInt(), Misc.getUndefInt(), ts, CoreVehicleInfo.UNASSIGNED_BUT_AV, false, srcOfAssignment);
		}
		Collection<DumperInfo> dumpers = this.dumpers.values();
		for (java.util.Iterator<DumperInfo> iter=dumpers.iterator(); iter.hasNext(); ) {
			DumperInfo shovel = iter.next();
			this.setAssignment(conn, shovel.getId(), Misc.getUndefInt(), Misc.getUndefInt(), ts, CoreVehicleInfo.UNASSIGNED_BUT_AV, false, srcOfAssignment);
		}
	}
	public ArrayList<Route> getRoutesInOrder() {
		ArrayList<Route> routeBySite = new ArrayList<Route>();
	    Collection<Route> allRoutes = this.routes.values();
	    for (java.util.Iterator<Route> iter = allRoutes.iterator(); iter.hasNext();) {
	    	Route route = iter.next();
	    	try {
	    		route.getReadLock();
		    	if (route.getAssignedDumpers().size() == 0)
		    		continue;
	    	}
	    	catch (Exception e) {
	    		
	    	}
	    	finally {
	    		route.releaseReadLock();
	    	}
	    	int insertBefore = 0;
	    	int is = routeBySite.size();
	    	Site meSite = this.loadSites.get(route.getLoadSite());
	    	if (meSite == null)
	    		continue;
	    	for (;insertBefore < is;insertBefore++) {
	    		if (routeBySite.get(insertBefore).getLoadSite() == meSite.getId())
	    			continue;
	    		
	    		Site otherSite = this.loadSites.get(routeBySite.get(insertBefore).getLoadSite());
	    		if (otherSite.getPitId() > meSite.getPitId())
	    			break;
	    		if (otherSite.getName().compareTo(meSite.getName()) > 0)
	    			break;
	    	}
	    	if (insertBefore == is)
	    		routeBySite.add(route);
	    	else
	    		routeBySite.add(insertBefore, route);
	    }
	    return routeBySite;
	}
	
	
	public String toString(Connection conn, boolean doAll) throws Exception {
	    ArrayList<Route> routeBySite = this.getRoutesInOrder();
	    StringBuilder retval = new StringBuilder();
	    retval.append("<data>\n");
	    LoadSite site = null;
	    for (int i=0,is=routeBySite.size();i<is;i++) {
	    	Route route = routeBySite.get(i);
	    	if (site != null && route.getLoadSite() != site.getId()) {
	    		retval.append("</site>");
	    		site = null;
	    	}
	    	if (site == null) {
	    		site = this.loadSites.get(route.getLoadSite());
		    	retval.append("<site ");
		    	site.toString(retval, doAll);
		    	retval.append(">\n");
		    	try {
		    		site.getReadLock();
			    	for (int j=0,js=site.getAssignedShovels().size(); j<js; j++) {
			    		int shovelId = site.getAssignedShovels().get(j);
			    		ShovelInfo shovel = this.shovels.get(shovelId);
			    		if (shovel == null)
			    			continue;
			    		retval.append("\t<shovel ");
			    		shovel.toString(retval, doAll);
			    		retval.append("/>\n");
			    	}
		    	}
		    	catch (Exception e) {
		    		
		    	}
		    	finally {
		    		site.releaseReadLock();
		    	}
	    	}
	    	UnloadSite unloadSite = this.unloadSites.get(route.getUnloadSite());
	    	if (unloadSite != null) {
	    		retval.append("\t<dest ");
	    		unloadSite.toString(retval, doAll);
	    		
	    		Helper.putDBGProp(retval, "dist", route.getDistance());
	    		if (doAll)
	    			Helper.putDBGProp(retval, "dist_src", route.getDistSrc());
	    		retval.append(">\n");
	    	}
	    	else {
	    		continue; //else will get dangling dumpers
	    	}
	    	try {
	    		route.getReadLock();
		    	for (int j=0,js=route.getAssignedDumpers().size(); j<js; j++) {
		    		DumperInfo dumperInfo = this.dumpers.get(route.getAssignedDumpers().get(j));
		    		if (dumperInfo == null)
		    			continue;
		    		retval.append("\t\t<dumper ");
		    		dumperInfo.toString(retval, doAll);
		    		retval.append("/>\n");
		    	}
	    	}
	    	catch (Exception e) {
	    		
	    	}
	    	finally {
	    		route.releaseReadLock();
	    	}
	    	retval.append("\t</dest>\n");
	    	
	    }
	    if (site != null)
	    	retval.append("</site>\n");
	    retval.append("</data>");
	    return retval.toString();
	}
	
	public void saveFromXML(Connection conn, String xml, long ts) throws Exception {
		String xmlDataString = xml;
		System.out.println("xmlDataString :" +xmlDataString);
		Document xmlDoc = xmlDataString != null && xmlDataString.length() != 0 ? MyXMLHelper.loadFromString(xmlDataString) : null;
		Element top = xmlDoc.getDocumentElement();
		this.removeAllAssignments(conn, ts, 0);
		for (Node sn = top.getFirstChild(); sn != null; sn = sn.getNextSibling()) {
			if (sn.getNodeType() != 1)
				continue;
			Element se = (Element) sn;
			int siteId = Misc.getParamAsInt(se.getAttribute("id"));
			
			for (Node cn = se.getFirstChild(); cn != null; cn = cn.getNextSibling()) {
				if (cn.getNodeType() != 1)
					continue;
				Element ce = (Element) cn;
				String tagName = ce.getTagName();
				if ("dest".equals(tagName)) {
					int destId = Misc.getParamAsInt(ce.getAttribute("id"));
					double dist = Misc.getParamAsDouble(ce.getAttribute("dist"));
					int distSrc = Route.DIST_FROM_USER;
					Route rt = this.getRoute(conn, siteId, destId, true);
					if (!Misc.isEqual(dist, rt.getDistance())) {
						rt.updateDist(conn, dist, distSrc);
					}
					for (Node ccn = ce.getFirstChild(); ccn != null; ccn = ccn.getNextSibling()) {
						if (ccn.getNodeType() != 1)
							continue;
						Element cce = (Element) ccn;
						this.setAssignment(conn, Misc.getParamAsInt(cce.getAttribute("id")), siteId,destId, ts, CoreVehicleInfo.ASSIGNED, true, 0);
					}
				}
				else if ("shovel".equals(tagName)) {
					this.setAssignment(conn, Misc.getParamAsInt(ce.getAttribute("id")), siteId, Misc.getUndefInt(), ts, CoreVehicleInfo.ASSIGNED, true, 0);
				}
			}
		}
		this.loadAssignments(conn);
	}
	
	public boolean optimize(Connection conn, long ts, ArrayList<Integer> dumperList) throws Exception {		
		if (dumperList != null && dumperList.size() > 0) {
			Iterator<Integer> itr = dumperList.iterator();
			while (itr.hasNext()) {
				DumperInfo dumperInfo = dumpers.get(itr.next());
				int routeId = dumperInfo.getAssignedRoute();
				Route route = routes.get(routeId);
				
				LoadSite loadSite = (route.getLoadSite() == Misc.getUndefInt())?null:loadSites.get(route.getLoadSite());
				UnloadSite unloadSite = (route.getUnloadSite() == Misc.getUndefInt())?null:unloadSites.get(route.getAltUnloadSite());
				
				if (loadSite ==  null || unloadSite == null) {
					System.out.println("Not Assigned");
				}
			}			
		}else {
			/* SORT DUMPERS BASED ON VOL*/
		    List<Integer> sortedDumperList = new ArrayList<Integer>(dumpers.keySet());
			Collections.sort(sortedDumperList, new Comparator<Integer>() {
			  public int compare(Integer a, Integer b) {
			    return (int) (dumpers.get(b).getCapacityVol() - dumpers.get(a).getCapacityVol());
			  }
			});		
			/* SORT SHOVELS BASED ON VOL*/		
			List<Integer> sortedShovelList = new ArrayList<Integer>(shovels.keySet());
			Collections.sort(sortedShovelList, new Comparator<Integer>() {
			  public int compare(Integer a, Integer b) {
			    return (int) (shovels.get(b).getCapacityVol() - shovels.get(a).getCapacityVol());
			  }
			});
	
			Iterator<Integer> itr = sortedShovelList.iterator();
			double productionCapacity [] = new double [sortedShovelList.size()];
			double consumerTotalCapacity [] = new double [sortedShovelList.size()];
			int sCounter = 0;
			while (itr.hasNext()) {
				ShovelInfo lshovel = shovels.get(itr.next());  
				System.out.println("Shovel["+sCounter+"], ID ["+lshovel.getId()+"]"+lshovel.getCapacityVol());
				double cycleTime = (lshovel.getCycleTimeSec()== Misc.getUndefDouble())?10:lshovel.getCycleTimeSec();
				double numCycles = (SHIFT_TIME_SEC-TOTAL_TRANSITION_TIME_SEC)/cycleTime;
				System.out.println("[OPTIMIZE][NUMCYCLES] "+numCycles);
				productionCapacity [sCounter++] = numCycles*((lshovel.getCapacityVol()==Misc.getUndefDouble())?10:lshovel.getCapacityVol());
			}		
		 
			Iterator<Integer> ditr = sortedDumperList.iterator();
		    while (ditr.hasNext()) {
				DumperInfo ldumper = dumpers.get(ditr.next());
				for (int shovelCounter=0; shovelCounter < sortedShovelList.size(); shovelCounter++) {
					int skey = sortedShovelList.get(shovelCounter);
					ShovelInfo lshovel = shovels.get(skey);							
					int loadSiteId =  lshovel.getAssignedLoadSite();
					LoadSite lsite = loadSites.get(loadSiteId);
					if (lsite == null) {
						System.out.println("LoadSite IS NULL");
						return false;
					}
					ArrayList<Route> routeList = getAllRouteFromSrc(conn,loadSiteId);
					for(int index=0; index<routeList.size(); index++) {
						if(index > 0) {
							if (ditr.hasNext())
								ldumper = dumpers.get(ditr.next());
							else
								break;
						}					
						Route r = routeList.get(index);							        	
						double distance = r.getDistance();
						double tripTime = (distance*2)/ldumper.getAvgOpSpeedPerKM();
						//double nTrips = (SHIFT_TIME_HR - TOTAL_TRANSITION_TIME_HR)/tripTime;
						double approxTrips = (SHIFT_TIME_HR - TOTAL_QUEUE_TIME_HR)/tripTime;
						double nTrips = (SHIFT_TIME_HR - TOTAL_QUEUE_TIME_HR - (approxTrips*(LOADING_UNLOADING_TIME_MIN/60)))/tripTime;
						nTrips = Math.ceil(nTrips);
						double contribution = nTrips*ldumper.getCapacityVol();
						if (consumerTotalCapacity[shovelCounter] < productionCapacity[shovelCounter]) {
							consumerTotalCapacity[shovelCounter] += contribution;					
							int unloadSiteId = r.getUnloadSite();
							UnloadSite lUnloadSite = unloadSites.get(unloadSiteId);
							if (lUnloadSite == null) {
								System.out.println("UnloadSite IS NULL");
								return false;
							}
							System.out.println("Dumper ["+ldumper.getId()+"], ID ["+lsite.getId()+"]"+lUnloadSite.getId());
							boolean succ = setAssignment(conn, ldumper.getId(), lsite.getId(), lUnloadSite.getId(), ts, CoreVehicleInfo.ASSIGNED, true,0);
						} else {
							System.out.println("["+lshovel.getId()+"] Shovel Assignment Full, ["+productionCapacity[shovelCounter]+"]["+consumerTotalCapacity[shovelCounter]+"]");
						}
					}
					shovelCounter++;
				}
			}
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		NewMU mu = NewMU.getManagementUnit(conn, 816);
		mu.load(conn, false);
		ArrayList<Integer> arrList = new ArrayList<Integer>();
		long now = System.currentTimeMillis();
		mu.optimize(conn, now, null);
		/*CoreVehicleInfo vehInfo = mu.shovels.get(41356);//exc
		System.out.println(vehInfo);
		vehInfo = mu.shovels.get(41500);//loader
		System.out.println(vehInfo);
		vehInfo = mu.dumpers.get(27645);//tipper
		System.out.println(vehInfo);
		vehInfo = mu.dumpers.get(41371);//dumper
		System.out.println(vehInfo);
		if (true) return;
		
		vehInfo = mu.shovels.get(26011);
		vehInfo = mu.shovels.get(26090);
		vehInfo = mu.shovels.get(26181);
		vehInfo = mu.dumpers.get(25990);
		vehInfo = mu.dumpers.get(25994);
		vehInfo = mu.dumpers.get(26026);
		vehInfo = mu.dumpers.get(26034);
		vehInfo = mu.dumpers.get(26150);
		Site site = mu.loadSites.get(1);
		site = mu.loadSites.get(2);
		site = mu.unloadSites.get(3);
		site = mu.unloadSites.get(4);
		long now = System.currentTimeMillis();
		boolean succ = mu.setAssignment(conn, 26011, 1, Misc.getUndefInt(), now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of shovel 26011 to 1:"+succ);
		succ = mu.setAssignment(conn, 26090, 1, Misc.getUndefInt(), now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of shovel 26090 to 1:"+succ);
		succ = mu.setAssignment(conn, 26181, 1, Misc.getUndefInt(), now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of shovel 26181 to 1:"+succ);
		succ = mu.setAssignment(conn, 25990, 1, 3, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 25990 from 1 to 3:"+succ);
		succ = mu.setAssignment(conn, 25990, 1, 4, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 25990 from 1 to 4:"+succ);
		succ = mu.setAssignment(conn, 25990, 1, 5, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 25990 from 1 to 5:"+succ);
		succ = mu.setAssignment(conn, 25994, 1, 3, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 25994 from 1 to 3:"+succ);
		succ = mu.setAssignment(conn, 26026, 1, 3, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 26026 from 1 to 3:"+succ);
		succ = mu.setAssignment(conn, 26034, 1, 3, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 26034 from 1 to 3:"+succ);
		succ = mu.setAssignment(conn, 26150, 1, 3, now, CoreVehicleInfo.ASSIGNED, true,0);
		System.out.println("Assignment of dumper 26150 from 1 to 3:"+succ);
		conn.commit();
		String str = mu.toString(conn,true);		
		System.out.println(str);
		mu.saveFromXML(conn, str, System.currentTimeMillis());
		mu.load(conn,true);
		str = mu.toString(conn, true);
		System.out.println(str);
		*/
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	synchronized public long getSimulationNow() {
		return simulationNow;
	}
	synchronized public void setSimulationNow(long simulationNow) {
		if (simulationNow > this.simulationNow)
			this.simulationNow= simulationNow;
	}
	public static int getDUMPER_TYPE() {
		return DUMPER_TYPE;
	}
	public static void setDUMPER_TYPE(int dumper_type) {
		DUMPER_TYPE = dumper_type;
	}
	public static int getFUEL_DIM_ID() {
		return FUEL_DIM_ID;
	}
	public static void setFUEL_DIM_ID(int fuel_dim_id) {
		FUEL_DIM_ID = fuel_dim_id;
	}
	
	public static int getDUMPER_IDLING_EVENT_ID() {
		return DUMPER_IDLING_EVENT_ID;
	}
	public static void setDUMPER_IDLING_EVENT_ID(int dumper_idling_event_id) {
		DUMPER_IDLING_EVENT_ID = dumper_idling_event_id;
	}
	
	public static int getSHOVEL_IDLING_EVENT_ID() {
		return SHOVEL_IDLING_EVENT_ID;
	}
	public static void setSHOVEL_IDLING_EVENT_ID(int shovel_idling_event_id) {
		SHOVEL_IDLING_EVENT_ID = shovel_idling_event_id;
	}
}

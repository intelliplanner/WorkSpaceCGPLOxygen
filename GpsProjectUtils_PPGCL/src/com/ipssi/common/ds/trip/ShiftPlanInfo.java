package com.ipssi.common.ds.trip;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;
import java.util.HashMap;

import com.ipssi.gen.utils.*;
import com.ipssi.processor.utils.GpsData;

public class ShiftPlanInfo {
	public static String GET_ORG_WITH_VALID_PLAN = "select distinct port_nodes.id , (case when shift_schedule_info.id is null then 0 else 1 end) from port_nodes left outer join shift_schedule_info on (shift_schedule_info.port_node_id = port_nodes.id) ";
	public static String GET_SHIFT_PLAN_SUMM_CACHE = "select id, port_node_id, day, shift_id, start_hour, start_min, duration from shift_schedule_info ";// where datediff(?,day) between 0 and 1 ";//order by port_node_id, day, shift_schedule_info.id;
	public static String GET_SHIFT_PLAN_SCHED_INFO_CACHE = "select shift_schedule_info_id,  port_node_id, shift_schedule.source, shift_schedule.source_loc_id, shift_schedule.destination, shift_schedule.dest_loc_id, shift_schedule.vehicle_type, shift_schedule.number_of_trips, shift_schedule.number_of_vehicles, shift_schedule.start_hour, shift_schedule.start_min, shift_schedule.stop_hour, shift_schedule.stop_min from shift_schedule_info join shift_schedule on (shift_schedule_info.id = shift_schedule_info_id)" ;// where datediff(?, day) between 0 and 1 ";//order by port_node_id, day, shift_schedule_info.id;
	public static String GET_SHIFT_PLAN_DRIVER_ASSIGN_CACHE = "select shift_schedule_info_id,  port_node_id, driver_assignments.vehicle_id, driver_assignments.driver_id from shift_schedule_info join driver_assignments on (shift_schedule_info.id = shift_schedule_info_id) ";//where datediff(?, day) between 0 and 1 ";//order by port_node_id, day, shift_schedule_info.id;
	public static String GET_SHIFT_PLAN_SUMM = "select id, port_node_id, day, shift_id from shift_schedule_info where id = ?";
		
    public static class ShiftLookupInfo { //this is the final result of applicable shiftdef or shift plan for the org
    	public ShiftDef shiftDef;
    	
    	public ShiftPlanInfo shiftPlan;
    	public Date loDateValidityForNullShiftPlan = null; //latest endDate before atTime if such a shiftplan exists or the 1st date for which data has been loaded 
    	public Date hiDateValidityForNullShiftPlan = null;//latest startDate before atTime if such a shiftplan exists or the 1day after the last date for which data has been loaded
    	 
    	public ShiftLookupInfo(ShiftDef shiftDef, ShiftPlanInfo shiftPlan, Date loDateValidityForNullShiftPlan, Date hiDateValidityForNullShiftPlan) {
    		this.shiftDef = shiftDef;
    		this.shiftPlan = shiftPlan;
    		this.loDateValidityForNullShiftPlan = loDateValidityForNullShiftPlan;
    		this.hiDateValidityForNullShiftPlan = hiDateValidityForNullShiftPlan;
    	}
    	
    	public int getShiftId() {
    		return shiftPlan != null ?shiftPlan.getShiftId() : shiftDef != null ? shiftDef.shiftId : Misc.getUndefInt();     			
    	}
    	
    	public Date getShiftDate(Date atTime) {
    		if (shiftPlan != null) {
    			return shiftPlan.getStartDate();
    		}
    		else if (shiftDef != null) {
    			Date shiftDate = new Date(atTime.getYear(),atTime.getMonth(),atTime.getDate());
    			shiftDate.setHours(shiftDef.startMin/60);
    			shiftDate.setMinutes(shiftDef.startMin%60);
    			
    			if (atTime.getHours()*60+atTime.getMinutes() < shiftDef.startMin) {
    				Misc.addDays(shiftDate,-1);
    			}
    			return shiftDate;
    		}
    		else {
    			return new Date(atTime.getYear(),atTime.getMonth(),atTime.getDate());
    		}
    	}
    	
    	public boolean isApplicable(Date atTime) {
    		if (shiftPlan != null) {
    		    return shiftPlan.isApplicable(atTime);	
    		}
    		else {
    			if (atTime.after(loDateValidityForNullShiftPlan) && atTime.before(hiDateValidityForNullShiftPlan)) {	
    				if (shiftDef != null)    			    	
    					return shiftDef.isApplicable(atTime);
    				else
    					return true;
    			}
    			else {
    				return false;
    			}
    		}
    	}
    }
    
	volatile private static HashMap<Integer, ShiftLookupInfo> g_fastLookupSched = null;//new HashMap<Integer, ShiftLookupInfo>();
	//Given a GpsData for a vehicle, we first will look up the org for the vehicle and then lookup in g_fastLookupSched using the orgId. If the shiftScheduleInfo applies 
	//for the time asked then we return this shift scheduleInfo.
	//Else we will lookup g_schedInfos and get data from there
	//When shift info is updated (either in shift definition Id or in shift plan for an org, not only we update g_schedInfos but also clear g_fastLookupSched
	
	
	volatile private static HashMap<Integer, ArrayList<Pair<Date,ShiftPlanInfo>>> g_schedInfos = null;//new HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>>();
	//Key: OrgId, Value: Pair of scheduleInfo and the shift Date. For a given shift Date there might be multiple ScheduleInfo.
	//   shiftDate cannot be null and must have 0 hr, 0 min, 0 sec
	//   if null shift info, means no info for the org for that particular date
	//   when checking for time we need to look at that shift date and the one before that
	//   when loading stuff we need to make entry even if no data is found for a particular date
	
	volatile private static HashMap<Integer, Integer> g_hasValidPlans = null;//new HashMap<Integer, Integer>();
	//key: orgId value: 0 - no plan on its own or anc, 1 from ancestor, 2 on its own
	volatile private static boolean g_hasValidPlanInitialized = false;
	public static void reset() {
		g_hasValidPlans = null;
		g_hasValidPlanInitialized = false;
		g_fastLookupSched = null;
		g_schedInfos = null;
		
	}
	 private static void init(Connection conn) throws Exception {
		try {
			if (g_fastLookupSched != null)
				return;
			synchronized (ShiftPlanInfo.class){
				HashMap<Integer, ShiftLookupInfo> temp_fastLookupSched = new HashMap<Integer, ShiftLookupInfo>();
				HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> temp_schedInfos = new HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>>();
				HashMap<Integer, Integer> temp_hasValidPlans = new HashMap<Integer, Integer>();
				PreparedStatement ps = conn.prepareStatement(ShiftDef.GET_ORG_LIST);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					Integer idInt = new Integer(id);
					temp_fastLookupSched.put(idInt, null);
					temp_schedInfos.put(idInt, null);
					temp_hasValidPlans.put(idInt, null);
				}
				rs.close();
				ps.close();
				g_hasValidPlans = temp_hasValidPlans;
				g_schedInfos = temp_schedInfos;
				g_fastLookupSched = temp_fastLookupSched;
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private static int g_numRetainSchedInfos = 200; //TODO property based
	
	
    
    private int orgId;
    private int shiftId;
    private int shiftSchedId;
    private Date startDate;
    private Date endDate;
    
       
    public class SchedLine {
    	private  int sourceId = Misc.getUndefInt();
    	private int sourceLocId = Misc.getUndefInt();
    	private int destId = Misc.getUndefInt();
    	private int destLocId = Misc.getUndefInt();
    	private Date startTime = null;
    	private Date endTime = null;
    	private int vehCount =  Misc.getUndefInt();
    	private int qty  = Misc.getUndefInt();
    	public SchedLine(int sourceId, int sourceLocId, int destId, int destLocId, Date startTime, Date endTime, int vehCount, int qty) {
    		this.sourceId = sourceId;
    		this.sourceLocId = sourceLocId;
    		this.destId = destId;
    		this.destLocId = destLocId;
    		this.startTime = startTime;
    		this.endTime = endTime;
    		this.vehCount = vehCount;
    		this.qty = qty;    		
    	}
    	public SchedLine() {
    		
    	}
    	public int getSourceId() {
    		return sourceId;
    	}
    	public int getSourceLocId() {
    		return sourceLocId;
    	}
    	public int getDestId() {
    		return destId;
    	}
    	public int getDestLocId() {
    		return destLocId;
    	}
    	public Date getStartTime() {
    		return startTime;
    	}
    	public Date getEndTime() {
    		return endTime;
    	}
    	public int getVehCount() {
    		return vehCount;
    	}
    	public int getQty() {
    		return qty;
    	}
    }
    
    public class DriverAssignment implements Comparable {
    	private int driverId;
    	private int vehicleId;
    	public DriverAssignment(int vehicleId, int driverId) {
    		this.vehicleId = vehicleId;
    		this.driverId = driverId;
    	}
    	public int getDriverId() {
    		return driverId;
    	}
    	public int getVehicleId() {
    		return vehicleId;
    	}
    	public int compareTo(Object obj) {		
    		return vehicleId-(((DriverAssignment)obj).vehicleId);
    	}
    }
    
    private ArrayList<SchedLine> schedLines;
    private FastList<DriverAssignment> driverAssignments; //kept sorted by vehicleId
    
    private ShiftPlanInfo(int orgId, int shiftId, int shiftSchedId, Date startDate, Date endDate) {
    	this.orgId = orgId;
    	this.shiftId = shiftId;
    	this.shiftSchedId = shiftSchedId;
    	this.startDate = startDate;
    	this.endDate = endDate;
    }
    
    public ArrayList<SchedLine> getSchedLines() {
    	return schedLines;
    }
    
    public FastList<DriverAssignment> getDriverAssignments() {
    	return driverAssignments;
    }
    
    public boolean isOpStationOperational(int opId, int vehicleId, long atTime) {//TODO - currently ignores the start/endTime
    	if (schedLines == null || schedLines.size() == 0)
    		return true;
    	for (int i=0,is=schedLines.size();i<is;i++) {
    		SchedLine schedLine = schedLines.get(i);
    		if ((schedLine.sourceId == opId) || (schedLine.destId == opId))
    			return true;
    	}
    	return false;
    }
    
    public int getDriver(int vehicleId) {
    	DriverAssignment compVal = new DriverAssignment(vehicleId, Misc.getUndefInt());
    	DriverAssignment assignment = driverAssignments == null ? null : driverAssignments.get(compVal);
    	if (assignment != null && assignment.getVehicleId() == vehicleId)
    		return assignment.getDriverId();
    	return Misc.getUndefInt();
    	
    }
    public void addSchedLine(SchedLine schedLine) {
    	if (schedLines == null) {
    		schedLines = new ArrayList<SchedLine>();    		
    	}
    	schedLines.add(schedLine);
    }
    
    public void addDriverAssignment(DriverAssignment assignment) {
    	if (assignment == null)
    		return;
    	if (driverAssignments == null)
    		driverAssignments = new FastList<DriverAssignment>();
    	driverAssignments.add(assignment);
    }
    
    public int getOrgId() {
    	return orgId;
    }
    
    public int getShiftId() {
    	return shiftId;
    }
    
    public int getShiftSchedId() {
    	return shiftSchedId;
    }
    
    public Date getStartDate() {
    	return startDate;
    }
    
    public Date getEndDate() {
    	return endDate;
    }
    
       
    private static ShiftPlanInfo readShiftPlan(ResultSet summInfo, ResultSet lines, ResultSet drivers, boolean[] lineDriverHasNext) throws Exception {
    	try {
    		int shiftSchedId = summInfo.getInt(1);
    		int orgId = summInfo.getInt(2);
    		Date shiftDate = summInfo.getDate(3);
    		int shiftId = summInfo.getInt(4);
    		int startHour = summInfo.getInt(5);
    		int startMin = summInfo.getInt(6);
    		int duration = summInfo.getInt(7);
    		
    		Date startDate = new Date(shiftDate.getTime());
    		startDate.setHours(startHour);
    		startDate.setMinutes(startMin);
    		Date endDate = new Date(startDate.getTime()+duration*60*1000);
    		    	       	
    		
    		ShiftPlanInfo retval = new ShiftPlanInfo(orgId, shiftId, shiftSchedId, startDate, endDate);
    		if (lines != null && lineDriverHasNext[0]) {
    			do  {
    				int lineSchedId = lines.getInt(1);
    				int linePortId = lines.getInt(2); 
    				if (linePortId < orgId) {
    					lineDriverHasNext[0] = lines.next();
    					continue;
    				}
    				if (linePortId > orgId) {
    					break;
    				}
    				if (lineSchedId < shiftSchedId) {
    					lineDriverHasNext[0] = lines.next();
    					continue;
    				}
    				if (lineSchedId > shiftSchedId)
    					break;
    				//GET_SHIFT_PLAN_SCHED_INFO_CACHE = "select shift_schedule_info_id,  shift_schedule.source, shift_schedule.source_loc_id, shift_schedule.destination, shift_schedule.dest_loc_id, shift_schedule.vehicle_type, shift_schedule.number_of_trips, shift_schedule.number_of_vehicles, shift_schedule.start_hour, shift_schedule.start_min, shift_schedule.stop_hour, shift_schedule.stop_min from shift_schedule_info join shift_schedule on (shift_schedule_info.id = shift_schedule_info_id)" ;// where datediff(?, day) between 0 and 1 ";//order by port_node_id, day, shift_schedule_info.id;
    				int sourceId = Misc.getRsetInt(lines,3);
    				int sourceLocId = Misc.getRsetInt(lines,4);
    				int destId = Misc.getRsetInt(lines,5);
    				int destLocId = Misc.getRsetInt(lines,6);
    				int vehCount = Misc.getRsetInt(lines,8);
    				int qty = Misc.getRsetInt(lines, 9);
    				int startH = Misc.getRsetInt(lines, 10);
    				int startM = Misc.getRsetInt(lines, 11);
    				int endH = Misc.getRsetInt(lines, 12);
    				int endM = Misc.getRsetInt(lines, 13);
    				Date schedStartDate = new Date(startDate.getTime());
    				Date schedEndDate = new Date(endDate.getTime());
    				if (!Misc.isUndef(startH) && !Misc.isUndef(startM)) {
    					schedStartDate.setHours(startH);
    					schedStartDate.setMinutes(startM);    					
    				}
    				if (!Misc.isUndef(endH) && !Misc.isUndef(endM)) {
    					schedEndDate.setHours(endH);
    					schedEndDate.setMinutes(endM);    					
    				}
    				if (schedStartDate.equals(schedEndDate) || schedStartDate.after(schedEndDate)) {
    					Misc.addDays(schedEndDate, 1);
    				}
    			   SchedLine schedLine  = retval.new SchedLine(sourceId, sourceLocId, destId, destLocId, schedStartDate, schedEndDate, vehCount, qty);
    			   retval.addSchedLine(schedLine);
    			   lineDriverHasNext[0] = lines.next();
    			}
    			while (lineDriverHasNext[0]);
    		}
    			
    		if (drivers != null && lineDriverHasNext[1]) {
    			do  {
    				int lineSchedId = drivers.getInt(1);
    				int linePortId = drivers.getInt(2);
    				if (linePortId < orgId) {
    					lineDriverHasNext[1] = drivers.next();
    					continue;
    				}
    				if (linePortId > orgId)
    					break;
    				if (lineSchedId < shiftSchedId) {
    					lineDriverHasNext[1] = drivers.next();
    					continue;
    				}
    				if (lineSchedId > shiftSchedId)
    					break;
    				// "shift_schedule_info_id,  driver_assignments.vehicle_id, driver_assignments.driver_id
    				int vehicleId = Misc.getRsetInt(drivers,3);
    				int driverId = Misc.getRsetInt(drivers,4);
    				DriverAssignment assignment  = retval.new DriverAssignment(vehicleId, driverId);
    			    retval.addDriverAssignment(assignment);
    			    lineDriverHasNext[1] = drivers.next();
    			}
    			while (lineDriverHasNext[1]);
    		}
    	    return retval;	
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    private static HashMap<Integer, Integer> updateOrgsValidPlan(HashMap<Integer, Integer> hasValidPlanLookup, MiscInner.PortInfo port, int oldVal) {
    	if (port == null)
    		return hasValidPlanLookup;
    	Integer portId = new Integer(port.m_id);
    	Integer currVal = hasValidPlanLookup.get(port.m_id);
    	int currValInt = currVal == null ? 0 : currVal.intValue();
    	if (currValInt == 0 && oldVal != 0) {
    		hasValidPlanLookup.put(port.m_id, new Integer(1));
    		currValInt = 1;
    	} 
    	for (int i=0,is=port.m_children == null ? 0 : port.m_children.size(); i<is;i++) {
    		MiscInner.PortInfo chInfo = (MiscInner.PortInfo) port.m_children.get(i);
    		hasValidPlanLookup = updateOrgsValidPlan(hasValidPlanLookup, chInfo, currValInt);
    	}
    	return hasValidPlanLookup;
    }
    
    
    private static int hasValidPlan(int orgId, Connection conn) throws Exception {
    	try {
    		init(conn);
    		HashMap<Integer,Integer> hasValidPlans = g_hasValidPlans;
    		if (!g_hasValidPlanInitialized) {
    			 synchronized (ShiftPlanInfo.class) {
		       	     PreparedStatement ps = conn.prepareStatement(GET_ORG_WITH_VALID_PLAN);
		       	     ResultSet rs = ps.executeQuery();
		       	     Integer two = new Integer(2);
		       	     Integer zero = new Integer(0);
		       	     
		       	     while (rs.next()) {
		       	    	int portid = rs.getInt(1);
		       	    	int exist = rs.getInt(2);
		       	    	Integer portIdInt = new Integer(portid);
		       	    	hasValidPlans.put(portIdInt, exist == 1 ? two : zero);
		       	     }
		       	     rs.close();
		       	     ps.close();
    			 
		    	    Cache cache = Cache.getCacheInstance(conn);
		    	    MiscInner.PortInfo orgPortInfo = cache.getPortInfo(Misc.G_TOP_LEVEL_PORT, conn);
		    	    hasValidPlans = updateOrgsValidPlan(hasValidPlans, orgPortInfo, 0);
		    	    g_hasValidPlans = hasValidPlans;
		    	    g_hasValidPlanInitialized = true;
    			 }//end of sync block
    		}
    	    
	        if (!Misc.isUndef(orgId)) {
	           	Integer hasSomePlan = hasValidPlans.get(orgId);
	           	return hasSomePlan != null ? hasSomePlan.intValue() : 0;
	         }
	        return 0;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    private  static HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> loadShiftPlan(HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> schedInfos, Connection conn, int orgId, Date atTime) throws Exception {//will load for at's date as well as prev's date
    	
    	java.sql.Date atTimeSql = null;
    	
    	
    	try {
           if (hasValidPlan(orgId, conn) != 2)
        	   return schedInfos;
            if (atTime == null) {
            	atTimeSql = new java.sql.Date(System.currentTimeMillis());
            }
            else {
            	atTimeSql = new java.sql.Date(atTime.getTime());
            }
            
            
            //1. load shift overview
            //2. load shift Schedule Lines
            //3. load driver assignments
            StringBuilder summ = new StringBuilder(GET_SHIFT_PLAN_SUMM_CACHE);
            StringBuilder info = new StringBuilder(GET_SHIFT_PLAN_SCHED_INFO_CACHE);
            StringBuilder driver = new StringBuilder(GET_SHIFT_PLAN_DRIVER_ASSIGN_CACHE);
            //// where datediff(?, day) between 0 and 1 ";//order by port_node_id, day, shift_schedule_info.id;
            if (!Misc.isUndef(orgId)) { //from summ we will get the lowest org for which shift plan has been defined, while for otherswill get the lowest most org's sched info if defined for summ but for detailed info and driver it will get from orgId
            	//the idea is that summ will get the org for which we want to get the 
            	summ.append(" join (select anc.id pnid from port_nodes leaf join port_nodes anc on  (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ")
					.append(" where exists(select 1 from shift_schedule_info where shift_schedule_info.port_node_id = anc.id) order by anc.lhs_number desc limit 1) portWithShift  ")
					.append(" on (portWithShift.pnid = shift_schedule_info.port_node_id)  ");
            	summ.append(" where datediff(?, day) between 0 and 1 and shift_schedule_info.status = 1");
            	info.append(" where port_node_id = ? and datediff(?, day) between 0 and 1  and shift_schedule_info.status = 1");
            	driver.append(" where port_node_id = ? and datediff(?,day) between 0  and 1  and shift_schedule_info.status = 1");
            }
            else {
            	summ.append(" where datediff(?, day) between 0 and 1  and shift_schedule_info.status = 1 ");
            	info.append(" where datediff(?, day) between 0 and 1  and shift_schedule_info.status = 1 ");
            	driver.append(" where datediff(?, day) between 0 and 1  and shift_schedule_info.status = 1 ");
            }
            summ.append(" order by port_node_id, shift_schedule_info.id ");
            info.append(" order by port_node_id, shift_schedule_info.id ");
            driver.append(" order by port_node_id, shift_schedule_info.id, driver_assignments.driver_id ");
            
            PreparedStatement psSumm = conn.prepareStatement(summ.toString());
            PreparedStatement psInfo = conn.prepareStatement(info.toString());
            PreparedStatement psDriver = conn.prepareStatement(driver.toString());
            int colIndexForDate = 1;
            if (!Misc.isUndef(orgId)) {
            	psSumm.setInt(1, orgId);
            	colIndexForDate = 2;
            }
            psSumm.setDate(colIndexForDate, atTimeSql);
            psInfo.setDate(colIndexForDate, atTimeSql);
            psDriver.setDate(colIndexForDate, atTimeSql);
            ResultSet rsSumm = psSumm.executeQuery();
            ResultSet rsInfo = null;
            ResultSet rsDriver = null;
            int prevPort = Misc.getUndefInt();
            Date shiftDate1 = new Date(atTime.getYear(), atTime.getMonth(), atTime.getDate());
            Date shiftDate2 = new Date(shiftDate1.getTime());
            Misc.addDays(shiftDate2,-1);
            boolean gotPlanForShiftDate1 = false;
            boolean gotPlanForShiftDate2 = false;
            ArrayList<Pair<Date, ShiftPlanInfo>> addThis = new ArrayList<Pair<Date, ShiftPlanInfo>>();
            boolean[] lineDriverHasNext = {false,false}; 
            while (rsSumm.next()) {
                   int schedId = rsSumm.getInt(1);
                   int ofPort = rsSumm.getInt(2);
                   
                   if (!Misc.isUndef(orgId)) {
                	   psInfo.setInt(1, ofPort);
                	   psDriver.setInt(1, ofPort);
                	   if (rsInfo != null) {
                		   rsInfo.close();
                		   rsInfo = null;
                	   }
                	   if (rsDriver != null) {
                		   rsDriver.close();
                		   rsDriver = null;
                	   }
                	   rsInfo = psInfo.executeQuery();
                	   rsDriver = psDriver.executeQuery();
                	   lineDriverHasNext[0] = rsInfo.next();
                	   lineDriverHasNext[1] = rsDriver.next();                	   
                   }
                   else {
                	   if (rsInfo != null) {
                		   rsInfo.close();
                		   rsInfo = null;
                	   }
                	   if (rsDriver != null) {
                		   rsDriver.close();
                		   rsDriver = null;
                	   }
                	   rsInfo = psInfo.executeQuery();
                	   rsDriver = psDriver.executeQuery();
                	   lineDriverHasNext[0] = rsInfo.next();
                	   lineDriverHasNext[1] = rsDriver.next();
                   }
                   if (ofPort != orgId) {
                	   //set the info to null .. so that we dont load
                       Cache cache = Cache.getCacheInstance(conn);
                       for (MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn); portInfo != null; portInfo = portInfo.m_parent) {
                    	   if (portInfo.m_id == ofPort)
                    		   break;
                    	   addThis.clear();
                    	   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate1, null));
                    	   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate2, null));
                    	   schedInfos = ShiftPlanInfo.addScheduleInfo(schedInfos, portInfo.m_id, addThis);                    	   
                       }
                       addThis.clear();
                   }
                   
                   if (prevPort != ofPort && !Misc.isUndef(prevPort)) {
                	   //make sure we got some value for 
                	   if (!gotPlanForShiftDate1) {
                		   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate1, null));
                	   }
                	   if (!gotPlanForShiftDate2) {
                		   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate2, null));
                	   }
                	   gotPlanForShiftDate1 = false;
                	   gotPlanForShiftDate2 = false;
                	   schedInfos = ShiftPlanInfo.addScheduleInfo(schedInfos, prevPort, addThis);
                	   addThis.clear();
                   }
                   //now read
                   ShiftPlanInfo shiftPlanInfo = ShiftPlanInfo.readShiftPlan(rsSumm, rsInfo, rsDriver, lineDriverHasNext);
                   Date shiftDate = new Date(shiftPlanInfo.getStartDate().getYear(), shiftPlanInfo.getStartDate().getMonth(), shiftPlanInfo.getStartDate().getDate());
                   if (shiftDate.equals(shiftDate1))
                	   gotPlanForShiftDate1 = true;
                   else  if (shiftDate.equals(shiftDate2))
                	   gotPlanForShiftDate2 = true;
                   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate, shiftPlanInfo));
                   
                   prevPort = ofPort;
            }
            if (!Misc.isUndef(prevPort)) {
         	   //make sure we got some value for 
            	if (!gotPlanForShiftDate1) {
         		   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate1, null));
         	   }
         	   if (!gotPlanForShiftDate2) {
         		   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate2, null));
         	   }
         	   gotPlanForShiftDate1 = false;
         	   gotPlanForShiftDate2 = false;
         	   schedInfos = ShiftPlanInfo.addScheduleInfo(schedInfos, prevPort, addThis);
         	   addThis.clear();
            }
            else {
            	Cache cache = Cache.getCacheInstance(conn);
                for (MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn); portInfo != null; portInfo = portInfo.m_parent) {
             	   addThis.clear();
             	   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate1, null));
             	   addThis.add(new Pair<Date, ShiftPlanInfo>(shiftDate2, null));
             	   schedInfos = ShiftPlanInfo.addScheduleInfo(schedInfos, portInfo.m_id, addThis);                    	   
                }
                addThis.clear();
            }
            if (rsSumm != null)
            	rsSumm.close();
            if (rsInfo != null)
            	rsInfo.close();
            if (rsDriver != null)
            	rsDriver.close();
            psSumm.close();
            psInfo.close();
            psDriver.close();
            return schedInfos;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    public static ShiftLookupInfo getShiftInfo(Integer orgId, long atTime, Connection conn) throws Exception { //will get the appropriate shift covering for this period    	
    	try {
    		init(conn);
    		Date atTimeDate = new Date(atTime);
    		HashMap<Integer, ShiftLookupInfo> fastLookupSched = g_fastLookupSched;
    		ShiftLookupInfo retval = fastLookupSched.get(orgId);
    		
    		if (retval == null || !retval.isApplicable(atTimeDate)) {
    			Triple<ShiftPlanInfo, Date, Date> shiftPlanInfo = null;
    			ShiftDef shiftDef = ShiftDef.getShiftDef(orgId.intValue(), conn, atTimeDate);
    			shiftPlanInfo = calcShiftPlan(orgId, atTimeDate, conn);
    			retval = new ShiftLookupInfo(shiftDef, shiftPlanInfo.first, shiftPlanInfo.second, shiftPlanInfo.third);
    			if (!fastLookupSched.containsKey(orgId)) {
    			    fastLookupSched.put(orgId, retval);
    			    g_fastLookupSched = fastLookupSched;
    			}
    			else
    				fastLookupSched.put(orgId, retval);
    			
    		}
            
    		return retval;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    private static Triple<ShiftPlanInfo, Date, Date> calcShiftPlan(Integer orgId, Date atTime, Connection conn) throws Exception {
    	//1st: shiftPlanInfo if covering atTime
    	//2nd latest endDate before atTime if such a shiftplan exists or the 1st date for which data has been loaded 
    	//3rd latest startDate before atTime if such a shiftplan exists or the 1day after the last date for which data has been loaded
    	
    	//kind of tricky ... basically we move up to see the shiftPlan that covers the atTime - but if there is no entry for shiftDate = atTime or 1 day prior we also load the data
    	//second we need to know the latest date for which shiftPlan = null means that we need not check for shiftPlan - basically
    	if (hasValidPlan(orgId, conn) == 0) {
    		return new Triple<ShiftPlanInfo, Date, Date> (null, new Date(0,0,1), new Date(124,0,1));
    	}
    		
    	Date shiftDate1 = new Date(atTime.getYear(), atTime.getMonth(), atTime.getDate());
    	Date shiftDate2 = new Date(shiftDate1.getTime());
    	Misc.addDays(shiftDate2, -1);
    	Cache cache = Cache.getCacheInstance(conn);
    	ShiftPlanInfo foundPlan = null;
    	Date loDateBeforeAtTime = null;
    	Date hiDateAfterAtTime = null;
    	HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> schedInfos = g_schedInfos;
    	for (MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn); portInfo != null; portInfo = portInfo.m_parent)  {
    		 ArrayList<Pair<Date, ShiftPlanInfo>> currList = schedInfos.get(portInfo.m_id);
    		 boolean foundShiftDate1 = false;
    		 boolean foundShiftDate2 = false;
    		 int indexToLookBefore = ShiftPlanInfo.getAddAfterIndex(currList, shiftDate1);
    		 for (int i=indexToLookBefore-1;i>=0;i--) {//check if data has been loaded
    			Pair<Date, ShiftPlanInfo> item = currList.get(i);
    			if (item.first.equals(shiftDate1)) {
    				foundShiftDate1 = true;
    			}
    			else if (item.first.equals(shiftDate2)) {
    				foundShiftDate2 = true;
    				break;
    			}
    		 }
    		 if (!foundShiftDate1 || !foundShiftDate2) {//load if necessary    			 
    			 schedInfos = ShiftPlanInfo.loadShiftPlan(schedInfos, conn, portInfo.m_id, atTime);
    			 currList = schedInfos.get(portInfo.m_id);
    			 indexToLookBefore = ShiftPlanInfo.getAddAfterIndex(currList, shiftDate1);
    		 }
    		 
    		 for (int i=indexToLookBefore-1;i>=0;i--) {
     			Pair<Date, ShiftPlanInfo> item = currList.get(i);
     		    
     			
     			if (item.second != null && item.second.isApplicable(atTime)) {
     				foundPlan = item.second;
     				break;
     			}
     			if (item.second != null) {
     		    	if (item.second.getStartDate().after(atTime) && (hiDateAfterAtTime == null || hiDateAfterAtTime.after(item.second.getStartDate())))
     		    		hiDateAfterAtTime = item.second.getStartDate();
     		    	if (item.second.getEndDate().before(atTime) && (loDateBeforeAtTime == null || loDateBeforeAtTime.before(item.second.getEndDate())))
     		    		loDateBeforeAtTime = item.second.getEndDate();
     		    }
     			if (item.first.before(shiftDate2)) {
     				break;
     			}
     		 }//look back
    		 
    		 if (foundPlan != null)
    			 break;
    		 
    	}//for each org in anc tree
    	
    	if (foundPlan == null) {
    	    	if (loDateBeforeAtTime == null)
    	    		loDateBeforeAtTime = shiftDate2;
    	    	if (hiDateAfterAtTime == null) {
    	    		hiDateAfterAtTime = new Date(shiftDate1.getTime());
    	    		Misc.addDays(hiDateAfterAtTime,1);
    	    	}
    	}
    	return new Triple<ShiftPlanInfo, Date, Date>(foundPlan, foundPlan == null ? loDateBeforeAtTime : null, foundPlan == null ? hiDateAfterAtTime : null);
    }//end of func
    
    private static int getAddAfterIndex(ArrayList<Pair<Date,ShiftPlanInfo>> addTo, Date shiftDate) {
    	if (addTo == null)
    		return 0;
    	int i=0,is=addTo.size();
    	for (;i<is;i++) {
    		Pair<Date, ShiftPlanInfo> item = addTo.get(i);
    		
    		if (item.first == null)
    			continue;
    		//item.first will be non null now
    		if (shiftDate == null || item.first.after(shiftDate))
    			break;
    	}
    	return i;
    }
    
    private static HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> addScheduleInfo(HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> schedInfos, Integer orgId, ArrayList<Pair<Date,ShiftPlanInfo>> addThis) {
    	//will remove also
    	ArrayList<Pair<Date,ShiftPlanInfo>> addTo = schedInfos.get(orgId);
    	if (addTo == null) {
    		addTo = new ArrayList<Pair<Date, ShiftPlanInfo>>();    		    	
    	}
    	else {
    	}
    	//first remove existing entries for shiftDates
    	Date prevShiftDate = null;
    	
    	for (int i=addThis.size()-1;i>=0;i--) {
    		Date currShiftDate = addThis.get(i).first;
    	    if (currShiftDate == prevShiftDate) {
    	    	continue;
    	    }
    	    prevShiftDate = currShiftDate;
    	    int indexOfAddAfter=getAddAfterIndex(addTo, currShiftDate);
    	    for (int j=indexOfAddAfter-1;j>=0;j--) {
    	    	if (!currShiftDate.equals(addTo.get(j).first))
    	    		break;
    	    	addTo.remove(j);
    	    }
    	}
    	int minAddAfter = addTo.size() + addThis.size()+1;
    	int maxAddAfter = -2;
    	
    	for (int i=0,is=addThis.size();i<is;i++) {
    	   Date shiftDate = addThis.get(i).first;
    	   ShiftPlanInfo schedInfo = addThis.get(i).second;
	    	int indexOfAddAfter=getAddAfterIndex(addTo, shiftDate);
	    	if (indexOfAddAfter < minAddAfter)
	    		minAddAfter = indexOfAddAfter;
	    	else if (indexOfAddAfter > maxAddAfter)
	    		maxAddAfter = indexOfAddAfter;
	    	int sz=addTo.size();
	    	Pair<Date, ShiftPlanInfo> addItem = new Pair<Date,ShiftPlanInfo>(shiftDate, schedInfo);
	    	if (indexOfAddAfter < sz)
	    		addTo.add(indexOfAddAfter, addItem);
	    	else
	    		addTo.add(indexOfAddAfter, addItem);
    	}
    	int itemsToRemove = addTo.size()-g_numRetainSchedInfos;
    	if (itemsToRemove > 0) {
    		
    		for (int i=0,is = minAddAfter-1;i<is;i++) {
    	       addTo.remove(0);
    	       itemsToRemove--;
    	       if (itemsToRemove >= 0)
    	    	   break;
    		}
    	}
    	if (itemsToRemove > 0) {    	
    		for (int i=addTo.size()-1,is = maxAddAfter+1;i>is;i--) {
    	       addTo.remove(i);
    	       itemsToRemove--;
    	       if (itemsToRemove >= 0)
    	    	   break;
    		}
    	}
    	    	
    	if (!schedInfos.containsKey(orgId)) {
    		schedInfos.put(orgId, addTo);
    		g_schedInfos = schedInfos;
    	}
    	else {
    		schedInfos.put(orgId, addTo);
    	}
    	return schedInfos;
    }
    
    private static boolean removeScheduleInfo(Integer orgId, Date shiftDate, int schedInfoId) {
    	boolean retval = false;
    	HashMap<Integer, ArrayList<Pair<Date, ShiftPlanInfo>>> schedInfos = g_schedInfos;
    	ArrayList<Pair<Date, ShiftPlanInfo>> addTo = schedInfos.get(orgId);
    	if (addTo == null)
    		return retval;
    	
    	int indexOfAddAfter = getAddAfterIndex(addTo, shiftDate);
    	boolean schedInfoIdMatches = false;
    	for (int i=indexOfAddAfter-1;i>=0;i--) {
    		Pair<Date,ShiftPlanInfo> item = addTo.get(i);
    		if (shiftDate != null && item.first == null)
    			break; //done .. all items before will be lesser date
    		else if (shiftDate != null && item.first != null && item.first.before(shiftDate))
    			break;//done .. all items before will be lesser date
    		if ((shiftDate == null && item.first == null) || shiftDate.equals(item.first)) { //item
				retval = true;
				schedInfoIdMatches =  (item.second != null && item.second.getShiftSchedId() == schedInfoId);
			   	addTo.remove(i);
    		}    		
    	}
    	if (!schedInfoIdMatches && !Misc.isUndef(schedInfoId)) {
    		for (int i=addTo.size()-1;i>=0;i--) {
        		Pair<Date,ShiftPlanInfo> item = addTo.get(i);
    			if (item.second != null && item.second.getShiftSchedId() == schedInfoId) {
    				retval = true;
    			   	addTo.remove(i);
    			   	break;
    			}    				
        	}	
    	}
    	if (retval) {
    		if (!schedInfos.containsKey(orgId)) {
    			schedInfos.put(orgId, addTo);
        		g_schedInfos = schedInfos;
    		}
    		else {
    			schedInfos.put(orgId, addTo);
    		}
    	}
        return retval;
    }
    
    public boolean isApplicable(Date atTime) {
    	return (atTime.equals(this.startDate) || (atTime.after(startDate) && atTime.before(this.endDate)));
    }
    
    public static void clearFastScheduleLookup(Connection conn, MiscInner.PortInfo portInfo) throws Exception {		
		try {
			
			if (portInfo == null)
				return;
			init(conn);
			for (int i=0,is = portInfo.m_children == null ? 0 : portInfo.m_children.size(); i<is;i++) {
				clearFastScheduleLookup(conn, (MiscInner.PortInfo) portInfo.m_children.get(i));
			}
			if (g_fastLookupSched.containsKey(portInfo.m_id))
				g_fastLookupSched.put(portInfo.m_id, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	public static void reloadShiftDetail(int shiftPlanId) throws Exception { //will loadShiftDef again here and will also clear up ShiftPlanInfo.g_fastSchedLookup
		Connection conn = null;
		boolean destroyIt = false; 
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			//get the org of the shift and the shiftDateId ..
			init(conn);
			int orgId = Misc.getUndefInt();
			Date shiftDate = null;
			int shiftId = Misc.getUndefInt();
			PreparedStatement ps = conn.prepareStatement(GET_SHIFT_PLAN_SUMM);
			ps.setInt(1, shiftPlanId);
			ResultSet rs = ps.executeQuery();
			HashMap<Integer, Integer> hasValidPlans = g_hasValidPlans;
			if (rs.next()) {
			    orgId = rs.getInt(2);
			    shiftDate = rs.getDate(3);
			    shiftId = rs.getInt(4);
			    
			     PreparedStatement ps2 = conn.prepareStatement(GET_ORG_WITH_VALID_PLAN+" where port_nodes.id = ?");
			     ps2.setInt(1, orgId);
	       	     ResultSet rs2 = ps2.executeQuery();
	       	     Integer two = new Integer(2);
	       	     Integer zero = new Integer(0);
	       	     
	       	     while (rs2.next()) {
	       	    	int portid = rs2.getInt(1);
	       	    	int exist = rs2.getInt(2);
	       	    	Integer portIdInt = new Integer(portid);
	       	    	if (!hasValidPlans.containsKey(portIdInt)) {
	       	    		hasValidPlans.put(new Integer(portid), exist == 1 ? two : zero);
	       	    		g_hasValidPlans = hasValidPlans;
	       	    	}
	       	    	else {
	       	    		hasValidPlans.put(new Integer(portid), exist == 1 ? two : zero);	
	       	    	}
	       	    	
	       	     }
	       	     rs2.close();
	       	     ps2.close();
	       	
	       	     Cache cache = Cache.getCacheInstance(conn);
	       	     MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn);
	       	     hasValidPlans = updateOrgsValidPlan(hasValidPlans, portInfo, 0);
	       	     
			    
			    boolean exists = ShiftPlanInfo.removeScheduleInfo(orgId, shiftDate, shiftPlanId);
			    if (exists) { //remove from fastLookUp too
					ShiftPlanInfo.clearFastScheduleLookup(conn, portInfo);
			    	//remove from
			    }
			}
			rs.next();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
			throw e;
		}
		finally {
			if (conn != null) {
				try {
				    DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e2) {
					
				}
			}
		}
	}
	
	public static void main(String[] args) {
		int orgId1 = 26; //mcd
		int orgId2 = 2; //Bulker
		
		Date t1 = new Date(112,3,1);
		Date t2 = new Date(t1.getTime());
		Date t3 = new Date(t1.getTime());
		Date t4 = new Date(t1.getTime());
		t2.setHours(11);
		t2.setMinutes(59);
		
		//t3.setDate(7);
		//t3.setHours(1);
		
		//t4.setDate(8);
		//t4.setMinutes(48);
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ShiftDef def = null;
//		    ShiftPlanInfo.ShiftLookupInfo shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t1, conn);
//		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t2, conn);
//		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t3, conn);
//		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t4, conn);
//		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    ShiftPlanInfo.reloadShiftDetail(86);//
//		    shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t3, conn);
//		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t4, conn);
//		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    shiftPlan.shiftDef = def;
//		    ShiftPlanInfo.reloadShiftDetail(86);//
//		    shiftPlan = ShiftPlanInfo.getShiftInfo(orgId1, t1, conn);
		    conn.commit();
//		    def = shiftPlan.shiftDef;
//		    shiftPlan = null;
		    
		}
		catch (Exception e) {
			destroyIt = true;
		}
		finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}
			catch (Exception e2) {
				
			}
			
		}
	
	}
        
}

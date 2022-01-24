package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ipssi.gen.utils.Misc;

public class CoreVehicleInfo {
	 private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	 
	public static int ASSIGNED = 0;
	public static int UNASSIGNED_BUT_AV = 10;
	public static int UNASSIGNED_BD = 20;
	
	public static int M_WAIT_FOR_LOAD = 0;
	public static int M_BEING_LOADED = 1;
	public static int M_LOADED_WAIT = 2;
	public static int M_TOUNLOAD = 3;
	public static int M_TO_OTHER_LOAD = 4;
	public static int M_WAIT_FOR_UNLOAD = 5;
	public static int M_BEING_UNLOADED = 6;
	public static int M_UNLOADED_WAIT = 7;
	public static int M_TOLOAD = 8;
	public static int M_TO_OTHER_UNLOAD = 9;
	private NewMU ownerMU;
	private int id;
	private String name;
	private int vehicleTypeId;
	private int typeLOV;
	private int detailedStatus;
	private int assignmentStatus;
	private int srcOfAssignment;
	private long lastAssignmentTime;
	private double capacityWt = Misc.getUndefDouble();
	private double capacityVol = Misc.getUndefDouble();
	private double unassignedRatePerHour = Misc.getUndefDouble();
	private double assignedRatePerHour = Misc.getUndefDouble();
	private double fuelTankCapacity = Misc.getUndefDouble();
	private double idlingRatePerHour = Misc.getUndefDouble();
	
	private long lastFuellingTime;
	private double estFuelRemaining = Misc.getUndefDouble();
	private boolean stopped;
	private boolean inRest;
	private long waitingOrStoppedSince;
	private long lastStatResetTime;
	private int currentOperator;

	private int[] flexInt = new int[4];
	private String[] flexString = new String[4];
	private double[] flexDouble = new double[4];
	private long[] flexDate = new long[4];

	NewAssignmentMgmt assignmentList = null;
	NewEventDismissMgmt eventDismissMgmt = null;
	// related to UI presentation
	private String styleClass;
	private String iconName; // image name
	private String iconHoverText;
	private String iconPopHoverText;
	
	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	public String getIconHoverText() {
		return iconHoverText;
	}

	public void setIconHoverText(String iconHoverText) {
		this.iconHoverText = iconHoverText;
	}

	public String getIconPopHoverText() {
		return iconPopHoverText;
	}

	public void setIconPopHoverText(String iconPopHoverText) {
		this.iconPopHoverText = iconPopHoverText;
	}

	public NewAssignmentMgmt getAssignmentList(Connection conn) throws Exception {
		return this.assignmentList == null ? NewAssignmentMgmt.create(conn, this.getId()) : assignmentList;
	}
 	
	public void toString(StringBuilder sb, boolean doAll) {
		Helper.putDBGProp(sb, "id", id);
		Helper.putDBGProp(sb, "name", name);
		Helper.putDBGProp(sb, "assign_status",assignmentStatus);
		Helper.putDBGProp(sb, "assign_time",lastAssignmentTime);
		Helper.putDBGProp(sb, "is_stopped",stopped);
		Helper.putDBGProp(sb, "in_rest",inRest);
		Helper.putDBGProp(sb, "wait_since_sec",waitingOrStoppedSince);
		Helper.putDBGProp(sb, "last_stat_reset",lastStatResetTime);
		Helper.putDBGProp(sb, "fuel_remain",estFuelRemaining);
		Helper.putDBGProp(sb, "curr_operator",currentOperator);
		Helper.putDBGProp(sb, "vehicle_type_id",vehicleTypeId);
		Helper.putDBGProp(sb, "vehicle_lov_type", typeLOV);
		Helper.putDBGProp(sb, "detailed_status",detailedStatus);
		Helper.putDBGProp(sb, "cap_wt",capacityWt);
		Helper.putDBGProp(sb, "cap_vol",capacityVol);
		Helper.putDBGProp(sb, "unassigned_rate",unassignedRatePerHour);
		Helper.putDBGProp(sb, "assigned_rate",assignedRatePerHour);
		Helper.putDBGProp(sb, "tank_cap",fuelTankCapacity);
		Helper.putDBGProp(sb, "idling_rate",idlingRatePerHour);
		Helper.putDBGProp(sb, "last_fuel_time",lastFuellingTime);
		for (int i=0,is=flexInt.length; i<is; i++) {
			Helper.putDBGProp(sb, "flex_int"+(i+1),flexInt[i]);
		}
		for (int i=0,is=flexDouble.length; i<is; i++) {
			Helper.putDBGProp(sb, "flex_dbl"+(i+1),flexDouble[i]);
		}
		for (int i=0,is=flexString.length; i<is; i++) {
			Helper.putDBGProp(sb, "flex_str"+(i+1),flexString[i]);
		}
		for (int i=0,is=flexDate.length; i<is; i++) {
			Helper.putDBGProp(sb, "flex_date"+(i+1),flexDate[i]);
		}			
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, true);
		return sb.toString();
	}
	public CoreVehicleInfo(int id, NewMU ownerMU) {
		this.id = id;
		this.ownerMU = ownerMU;
	}
	public void populateInfo(ResultSet rs) throws Exception {
		this.id = rs.getInt("vehicle_id");
		this.name= rs.getString("vehicle_name");
		this.vehicleTypeId = rs.getInt("vehicle_type_id");
		this.typeLOV = Misc.getRsetInt(rs, "vehicle_lov_type");
		this.detailedStatus = Misc.getRsetInt(rs, "vehicle_detailed_status", Misc.getRsetInt(rs, "vehicle_status", 0));
		this.capacityWt = Misc.getRsetDouble(rs, "capacity_wt");
		this.capacityVol = Misc.getRsetDouble(rs, "capacity_vol");
		this.unassignedRatePerHour = Misc.getRsetDouble(rs, "uassigned_rate_hourly");
		this.assignedRatePerHour = Misc.getRsetDouble(rs, "assigned_rate_use_hourly");
		this.idlingRatePerHour = Misc.getRsetDouble(rs, "assigned_rate_idling_hourly");
		
		this.fuelTankCapacity = Misc.getRsetDouble(rs, "fuel_tank_capacity");
		for (int i=0,is=flexInt.length; i<is;i++) {
			this.flexInt[i] = Misc.getRsetInt(rs, "int_field"+(i+1));
 		}
		for (int i=0,is=flexString.length; i<is;i++) {
			this.flexString[i] = rs.getString("str_field"+(i+1));
 		}
		for (int i=0,is=flexDouble.length; i<is;i++) {
			this.flexDouble[i] = Misc.getRsetDouble(rs, "double_field"+(i+1));
 		}
		for (int i=0,is=flexDate.length; i<is;i++) {
			this.flexDate[i] = Misc.sqlToLong(rs.getTimestamp("date_field"+(i+1)));
 		}
		
			
	}
	public int getFlexInt(int i) {
		return i>= 0 && i<flexInt.length ?  flexInt[i] : Misc.getUndefInt();
	}
	public double getFlexDouble(int i) {
		return i>= 0 && i<flexDouble.length ?  flexDouble[i] : Misc.getUndefDouble();
	}
	public String getFlexString(int i) {
		return i>= 0 && i<flexString.length ?  flexString[i] : null;
	}
	public long getFlexLong(int i) {
		return i>= 0 && i<flexDate.length ?  flexDate[i] : -1;
	}
	
	public void setFlexInt(int i, int v) {
		if (i>=0 && i<flexInt.length)
			flexInt[i] = v;
	}
	public void setFlexDouble(int i, double v) {
		if (i>=0 && i<flexDouble.length)
			flexDouble[i] = v;
	}
	public void setFlexString(int i, String v) {
		if (i>=0 && i<flexInt.length)
			flexString[i] = v;
	}
	public void setFlexLong(int i, long v) {
		if (i>=0 && i<flexInt.length)
			flexDate[i] = v;
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
	public int getVehicleTypeId() {
		return vehicleTypeId;
	}
	public void setVehicleTypeId(int type) {
		this.vehicleTypeId = type;
	}
	public double getCapacityWt() {
		return capacityWt;
	}
	public void setCapacityWt(double capacityWt) {
		this.capacityWt = capacityWt;
	}
	public double getCapacityVol() {
		return capacityVol;
	}
	public void setCapacityVol(double capacityVol) {
		this.capacityVol = capacityVol;
	}
	public double getUnassignedRatePerHour() {
		return unassignedRatePerHour;
	}
	public void setUnassignedRatePerHour(double unassignedRatePerHour) {
		this.unassignedRatePerHour = unassignedRatePerHour;
	}
	public double getAssignedRatePerHour() {
		return assignedRatePerHour;
	}
	public void setAssignedRatePerHour(double assignedRatePerHour) {
		this.assignedRatePerHour = assignedRatePerHour;
	}
	
	
	public double getFuelTankCapacity() {
		return fuelTankCapacity;
	}
	public void setFuelTankCapacity(double fuelTankCapacity) {
		this.fuelTankCapacity = fuelTankCapacity;
	}
	public long getLastFuellingTime() {
		return lastFuellingTime;
	}
	public void setLastFuellingTime(long lastFuellingTime) {
		this.lastFuellingTime = lastFuellingTime;
	}
	public double getEstFuelRemaining() {
		return estFuelRemaining;
	}
	public void setEstFuelRemaining(double estFuelRemaining) {
		this.estFuelRemaining = estFuelRemaining;
	}
	public boolean isStopped() {
		return stopped;
	}
	public void setStopped(boolean isStopped) {
		this.stopped = isStopped;
	}
	public long getWaitingOrStoppedSince() {
		return waitingOrStoppedSince;
	}
	public void setWaitingOrStoppedSinceSec(long waitingOrStoppedSince) {
		this.waitingOrStoppedSince = waitingOrStoppedSince;
	}
	public long getLastStatResetTime() {
		return lastStatResetTime;
	}
	public void setLastStatResetTime(long lastStatResetTime) {
		this.lastStatResetTime = lastStatResetTime;
	}
	public int getCurrentOperator() {
		return currentOperator;
	}
	public void setCurrentOperator(int currentOperator) {
		this.currentOperator = currentOperator;
	}
	public int getDetailedStatus() {
		return detailedStatus;
	}
	public void setDetailedStatus(int detailedStatus) {
		this.detailedStatus = detailedStatus;
	}
	public int getAssignmentStatus() {
		return assignmentStatus;
	}
	public void setAssignmentStatus(int assignmentStatus) {
		this.assignmentStatus = assignmentStatus;
	}
	public long getLastAssignmentTime() {
		return lastAssignmentTime;
	}
	public void setLastAssignmentTime(long lastAssignmentTime) {
		this.lastAssignmentTime = lastAssignmentTime;
	}
	public double getIdlingRatePerHour() {
		return idlingRatePerHour;
	}
	public void setIdlingRatePerHour(double idlingRatePerHour) {
		this.idlingRatePerHour = idlingRatePerHour;
	}
	public int[] getFlexInt() {
		return flexInt;
	}
	public void setFlexInt(int[] flexInt) {
		this.flexInt = flexInt;
	}
	public String[] getFlexString() {
		return flexString;
	}
	public void setFlexString(String[] flexString) {
		this.flexString = flexString;
	}
	public double[] getFlexDouble() {
		return flexDouble;
	}
	public void setFlexDouble(double[] flexDouble) {
		this.flexDouble = flexDouble;
	}
	public long[] getFlexDate() {
		return flexDate;
	}
	public void setFlexDate(long[] flexDate) {
		this.flexDate = flexDate;
	}

	public boolean isInRest() {
		return inRest;
	}

	public void setInRest(boolean isInRest) {
		this.inRest = isInRest;
	}

	public int getSrcOfAssignment() {
		return srcOfAssignment;
	}

	public void setSrcOfAssignment(int srcOfAssignment) {
		this.srcOfAssignment = srcOfAssignment;
	}

	public void getReadLock() {
		lock.readLock().lock();
	}
	public void releaseReadLock() {
		lock.readLock().unlock();
	}
	public void getWriteLock() {
		lock.writeLock().lock();
	}
	public void releaseWriteLock() {
		lock.writeLock().unlock();
	}

	public NewEventDismissMgmt getEventDismissMgmt(Connection conn) {
		if (eventDismissMgmt == null)
			this.eventDismissMgmt = NewEventDismissMgmt.create(conn, 0, id);
		return eventDismissMgmt;
	}

	public NewMU getOwnerMU() {
		return ownerMU;
	}

	public void setOwnerMU(NewMU ownerMU) {
		this.ownerMU = ownerMU;
	}

}

package com.ipssi.swm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.swm.setup.GroupBean;
import com.ipssi.tracker.drivers.DriverCoreBean;
import com.ipssi.tracker.drivers.DriverSkillsBean;

public class AssignmentHelper {
	public static class ExtGroupBean {
	   public GroupBean coreGroup;
	   public double vehNeed;
	   public int indexInAv = -1;
	   public ArrayList<Triple<VehicleInfo, DriverCoreBean, AssignmentHelper.Assignment>> assignments = new ArrayList<Triple<VehicleInfo, DriverCoreBean, AssignmentHelper.Assignment>>();
	   public String toString() {
		   return Integer.toString(coreGroup.getId());
	   }
	   public void addAssignment(VehicleInfo vehicleInfo, DriverCoreBean driverInfo, AssignmentHelper.Assignment assignment) {
		 
		   assignments.add(new Triple<VehicleInfo, DriverCoreBean, AssignmentHelper.Assignment>(vehicleInfo, driverInfo, assignment));
	   }
	   
	   public void removeAssignment(VehicleInfo vehicleInfo, DriverCoreBean driverInfo, AssignmentHelper.Assignment assignment) {
		   for (int i=assignments.size()-1; i>=0; i--) {
			   Triple<VehicleInfo, DriverCoreBean, AssignmentHelper.Assignment> entry = assignments.get(i);
			   if ((entry.first == null || entry.first.getVehicleId() == vehicleInfo.getVehicleId()) && (entry.second == null || entry.second.getId() == driverInfo.getId()) && ((entry.first != null || entry.second != null) && entry.third.overlaps(assignment)))
				   assignments.remove(i);
		   }
	   }
	   
	   public double getAssignFrac(boolean forLoader) {
		   //check if the vehicles assigned meet the need
		   double retval = 1;
		   double vehLoaderAssigned = 0;
		   double vehDumperPressurAssigned = 0;
		   double vehTipperAssigned = 0;
		   for (Triple<VehicleInfo, DriverCoreBean, AssignmentHelper.Assignment> entry:assignments) {
			   if (entry.first != null) {
				   if (entry.first.isLoader())
					   vehLoaderAssigned += entry.third.frac();
				   else if (entry.first.isTipper())
					   vehTipperAssigned += entry.third.frac();
				   else if (entry.first.isDumperPressure()) 
					   vehDumperPressurAssigned += entry.third.frac();
			   }
		   }
		   double vehNeed = this.vehNeed;
		   if (forLoader) {
			   //if no assignment of vehDumperPressure and vehTipper then gues from vehNeed
			   if (Misc.isEqual(vehTipperAssigned,0) && Misc.isEqual(vehDumperPressurAssigned, 0))
				   vehNeed = this.vehNeed;
			   else
				   vehNeed = vehTipperAssigned;
			   vehNeed = vehNeed*3/14;
			   retval = Misc.isEqual(vehNeed, 0) ? 0 : vehLoaderAssigned/vehNeed;
		   }
		   else {
			  retval = Misc.isEqual(vehNeed, 0) ? 0 : (vehTipperAssigned+vehDumperPressurAssigned)/vehNeed;
		   }
		   
		   if (retval > 1)
			   retval = 1;
		   return retval;
	   }
	   
	   public boolean isAv() {
		   return indexInAv != -1;
	   }
	}
	public static class Vehicle {
		public VehicleInfo vehicle;
		public ArrayList<Pair<DriverCoreBean, ArrayList<Assignment>>> assignments = new ArrayList<Pair<DriverCoreBean, ArrayList<Assignment>>>();
		public DriverCoreBean tentativeDriver;
		public int indexInAv = -1;
		public double getAssignFrac() {
			   //check if the vehicles assigned meet the need
			   double vehAssigned = 0;
			   for (Pair<DriverCoreBean, ArrayList<Assignment>> entry1:assignments) {
				   for (Assignment assign:entry1.second) {
					  vehAssigned += assign.frac();
				   }
			   }
			   if (vehAssigned > 1)
				   vehAssigned = 1;
			   return vehAssigned;
		   }
		   
		   public boolean isAv() {
			   return indexInAv != -1;
		   }
	}
	
	public static class Driver {
		public DriverCoreBean driver;
		public ArrayList<Pair<VehicleInfo, ArrayList<Assignment>>> assignments = new ArrayList<Pair<VehicleInfo, ArrayList<Assignment>>>() ;
		public VehicleInfo tentativeVehicle;
		public int indexInAv = -1;
		private int minSkillLevel = 10;
		private int minSkillLevelLoader = 10;
		public int getSomeVehicleAssigned(boolean forLoader) {
			for (Pair<VehicleInfo, ArrayList<Assignment>> assignment : assignments) {
				if (assignment.first != null &&((forLoader && assignment.first.isLoader()) || (!forLoader && ! assignment.first.isLoader()))) 
					return assignment.first.getVehicleId();
			}
			return Misc.getUndefInt();
		}
		
		public int getMinSkillLevel() {
			if (minSkillLevel < 10)
				return minSkillLevel;
			int minskill = 10;
			DimInfo d9097 = DimInfo.getDimInfo(9097);
			
			for (DriverSkillsBean skill: driver.getDriverSkillsList()) {
				int v = skill.getId();
				boolean isLoader =  (v == 15 || v == 16 || 1 == d9097.getAParentVal(9022, v));
				if (isLoader)
					continue;
				if (skill.getValue() < minskill)
					minskill = skill.getValue();
			}
			minSkillLevel = minskill;
			return minSkillLevel;
		}
		
		public int getMinSkillLevelLoader() {
			if (minSkillLevelLoader < 10)
				return minSkillLevelLoader;
			int minskill = 10;
			DimInfo d9097 = DimInfo.getDimInfo(9097);
			
			for (DriverSkillsBean skill: driver.getDriverSkillsList()) {
				int v = skill.getId();
				boolean isLoader =  (v == 15 || v == 16 || 1 == d9097.getAParentVal(9022, v));
				if (!isLoader)
					continue;
				if (skill.getValue() < minskill)
					minskill = skill.getValue();
			}
			minSkillLevelLoader = minskill;
			return minSkillLevelLoader;
		}
		
		public double getAssignFrac() {
			   //check if the vehicles assigned meet the need
			   double vehAssigned = 0;
			   for (Pair<VehicleInfo, ArrayList<Assignment>> entry1:assignments) {
				   for (Assignment assign:entry1.second) {
					  vehAssigned += assign.frac();
				   }
			   }
			   if (vehAssigned > 1)
				   vehAssigned = 1;
			   return vehAssigned;
		   }
		   
		   public boolean isAv() {
			   return indexInAv != -1;
		   }

	}

	public static class Assignment {
		private ExtGroupBean group;
		private int startHr = Misc.getUndefInt();
		private int startMin = Misc.getUndefInt();
		private int endHr = Misc.getUndefInt();
		private int endMin = Misc.getUndefInt();

		public Assignment(ExtGroupBean group, int startHr, int startMin,
				int endHr, int endMin) {
			super();
			this.group = group;
			this.startHr = startHr;
			this.startMin = startMin;
			this.endHr = endHr;
			this.endMin = endMin;
		}
		public double frac() {
			if (endHr == startHr)
				return 1; //hack ...
		   double hr = endHr - startHr;
		   if (hr > 8)
			   return 1;
		   hr *= 60;
		   hr -= startMin;
		   hr += endMin;
		   return hr/8.0;
		}
		
		public boolean overlaps(Assignment other) {
			if (other.endHr < startHr || (other.endHr == startHr && other.endMin <= startMin))
				return false;
			if (endHr < other.startHr || (endHr == other.startHr && endMin <= other.startMin))
				return false;
			return true;
		}
		public ExtGroupBean getGroup() {
			return group;
		}
		public void setGroup(ExtGroupBean group) {
			this.group = group;
		}
		public int getStartHr() {
			return startHr;
		}
		public void setStartHr(int startHr) {
			this.startHr = startHr;
		}
		public int getStartMin() {
			return startMin;
		}
		public void setStartMin(int startMin) {
			if (Misc.isUndef(startMin))
				startMin = 0;
			this.startMin = startMin;
		}
		public int getEndHr() {
			return endHr;
		}
		public void setEndHr(int endHr) {
			if (Misc.isUndef(endHr)) {
				endHr = 25;
			}
			this.endHr = endHr;
		}
		public int getEndMin() {
			return endMin;
		}
		public void setEndMin(int endMin) {
			if (Misc.isUndef(endMin))
				endMin = 60;
			this.endMin = endMin;
		}
	}
	
	
	public HashMap<Integer, Driver> driverLookup = new HashMap<Integer, Driver>();
	public HashMap<Integer, Vehicle> vehicleLookup = new HashMap<Integer, Vehicle>();
	public HashMap<Integer, ExtGroupBean> groupLookup = new HashMap<Integer, ExtGroupBean>();
	public ArrayList<Driver> availableDriver = new ArrayList<Driver>(); //asc order of skill level (remember high is 0 - so this really is desc) & then by desc of alloc
	public ArrayList<Vehicle> availableVehicle = new ArrayList<Vehicle>();//desc order of alloc
	public ArrayList<ExtGroupBean> availableGroup = new ArrayList<ExtGroupBean>(); //desc order of already assigned
	
	public boolean setTentativeVehicle(int driverId, int vehicleId) {
		//check if the desired vehicle has a driver different from the driver ... if not ... then set the assignment
		Driver driver = driverLookup.get(driverId);
		if (driver == null)
			return false;
		Vehicle vehicle = vehicleLookup.get(vehicleId);
		for (Pair<DriverCoreBean, ArrayList<Assignment>> vehsDriver : vehicle.assignments) {
			DriverCoreBean oth = vehsDriver.first;
			if (oth == null)
					continue;
			if (oth.getId() != driverId) {
				driver.tentativeVehicle = null;
				return false;
			}
		}
		driver.tentativeVehicle = vehicle.vehicle;
		vehicle.tentativeDriver = driver.driver;
	   return true;
	}	

	public int insertAvDriver(Driver driver, boolean forLoader) {//asc order of skill level (remember high is 0 - so this really is desc) & then by desc of alloc
		//find pos to insert and then insert ...
		int skillLevel = forLoader ? driver.getMinSkillLevelLoader() : driver.getMinSkillLevel();
		double use = driver.getAssignFrac();
		for (int i=availableDriver.size()-1;i>=0;i--) {
			Driver temp = availableDriver.get(i);
			if ((forLoader && (temp.getMinSkillLevelLoader() > skillLevel || (temp.getMinSkillLevelLoader() == skillLevel &&  temp.getAssignFrac() >= use))) ||
			(!forLoader && (temp.getMinSkillLevel() > skillLevel || (temp.getMinSkillLevel() == skillLevel &&  temp.getAssignFrac() >= use)))
					) {
				//insert here
				if (i == availableDriver.size()-1)
					availableDriver.add(driver);
				else
					availableDriver.add(i+1, driver);
				return i+1;
			}
		}
		if (availableDriver.size() == 0)
			availableDriver.add(driver);
		else
			availableDriver.add(0, driver);
		return 0;
	}
	
	public int insertAvVehicle(Vehicle vehicle, boolean forLoader) {//desc of alloc
		//find pos to insert and then insert ...s
		if ((forLoader && !vehicle.vehicle.isLoader() || (!forLoader && vehicle.vehicle.isLoader())))
				return -1;
		double use = vehicle.getAssignFrac();
		for (int i=availableVehicle.size()-1;i>=0;i--) {
			Vehicle temp = availableVehicle.get(i);
			if (temp.getAssignFrac() >= use) {
				//insert here
				if (i == availableVehicle.size()-1)
					availableVehicle.add(vehicle);
				else
					availableVehicle.add(i+1, vehicle);
				return i+1;
			}
		}
		if (availableVehicle.size() == 0)
			availableVehicle.add(vehicle);
		else
			availableVehicle.add(0, vehicle);
		return 0;
	}
	
	public int insertAvGroup(ExtGroupBean group, boolean forLoader) { //desc order of already assigned
		//find pos to insert and then insert ...
		double use = group.getAssignFrac(forLoader);
		for (int i=availableGroup.size()-1;i>=0;i--) {
			ExtGroupBean temp = availableGroup.get(i);
			double tempFrac = temp.getAssignFrac(forLoader);
			if (tempFrac > use || (Misc.isEqual(tempFrac, use) && temp.coreGroup.getLoadStations().size() < group.coreGroup.getLoadStations().size())) {
				//insert here
				if (i == availableGroup.size()-1)
					availableGroup.add(group);
				else
					availableGroup.add(i+1, group);
				return i+1;
			}
		}
		if (availableGroup.size() == 0)
			availableGroup.add(group);
		else
			availableGroup.add(0, group);
		return 0;
	}
	
	public void populateAvailable(boolean forLoader) {
		availableDriver.clear();
		availableVehicle.clear();
		availableGroup.clear();
		for (Driver driver:driverLookup.values()) {
			double use = driver.getAssignFrac();
			if (use < 0.95) {
				this.insertAvDriver(driver, forLoader);
			}
		}
		for (Vehicle vehicle:vehicleLookup.values()) {
			
			double use = vehicle.getAssignFrac();
			if (use < 0.95) {
				this.insertAvVehicle(vehicle, forLoader);
			}
		}
		for (ExtGroupBean group:groupLookup.values()) {
			double use = group.getAssignFrac(forLoader);
			if (use < 0.95) {
				this.insertAvGroup(group, forLoader);
			}
		}
		fillAvIndex();
	}
	
	public void fillAvIndex() {
		for (int i=0,is=availableDriver.size();i<is;i++) {
			availableDriver.get(i).indexInAv = i;
		}
		for (int i=0,is=availableVehicle.size();i<is;i++) {
			availableVehicle.get(i).indexInAv = i;
		}
		for (int i=0,is=availableGroup.size();i<is;i++) {
			availableGroup.get(i).indexInAv = i;
		}

	}
	
	
	public boolean addAssignment(ExtGroupBean group, VehicleInfo vehicleInfo, DriverCoreBean driverInfo, int startHr, int startMin, int endHr, int endMin) {
		Driver driver = driverInfo == null ? null : driverLookup.get(driverInfo.getId());
		Vehicle vehicle = vehicleInfo == null ? null : vehicleLookup.get(vehicleInfo.getVehicleId());
		if (driver == null && vehicle == null)
			return false;
	
		Assignment toAdd = new Assignment(group, startHr, startMin, endHr, endMin);
		
		if (vehicle != null && vehicle.assignments == null)
			vehicle.assignments = new ArrayList<Pair<DriverCoreBean, ArrayList<Assignment>>>();
		if (driver != null && driver.assignments == null)
			driver.assignments = new ArrayList<Pair<VehicleInfo, ArrayList<Assignment>>>();
		//1st check if the assignment will not clash time wise in entries for the vehicle and for the driver
		if (vehicle != null) {
			for (Pair<DriverCoreBean, ArrayList<Assignment>> entry: vehicle.assignments) {
				for (Assignment assignment:entry.second) {
					if (assignment.overlaps(toAdd))
						return false;
				}
			}
		}
		if (driver != null) {
			for (Pair<VehicleInfo, ArrayList<Assignment>> entry: driver.assignments) {
				for (Assignment assignment:entry.second) {
					if (assignment.overlaps(toAdd))
						return false;
				}
			}
		}
		//now add
		boolean added = false;
		if (vehicle != null) { 
			for (Pair<DriverCoreBean, ArrayList<Assignment>> entry: vehicle.assignments) {
				if (entry.first != null && driverInfo != null && entry.first.getId() == driverInfo.getId()) {
					entry.second.add(toAdd);
					added = true;
				}
			}
			if (!added) {
				ArrayList<Assignment> ar = new ArrayList<Assignment>();
				ar.add(toAdd);
				vehicle.assignments.add(new Pair<DriverCoreBean, ArrayList<Assignment>> (driverInfo, ar));
			}
		}
		added = false;
		if (driver != null) {
			for (Pair<VehicleInfo, ArrayList<Assignment>> entry: driver.assignments) {
				if (entry.first != null && vehicleInfo != null && entry.first.getVehicleId() == vehicleInfo.getVehicleId()) {
					entry.second.add(toAdd);
					added = true;
				}
			}
			if (!added) {
				ArrayList<Assignment> ar = new ArrayList<Assignment>();
				ar.add(toAdd);
				driver.assignments.add(new Pair<VehicleInfo, ArrayList<Assignment>> (vehicleInfo, ar));
			}
		}
		group.addAssignment(vehicleInfo, driverInfo, toAdd);
		
		return true;
	}
	
	public void todoAdjForAvStructRemoveAssignment(ExtGroupBean group, VehicleInfo vehicleInfo, DriverCoreBean driverInfo, int startHr, int startMin, int endHr, int endMin) {
		Driver driver = driverLookup.get(driverInfo.getId());
		Vehicle vehicle = vehicleLookup.get(vehicleInfo.getVehicleId());
		if (driver == null || vehicle == null)
			return ;
		
		Assignment toAdd = new Assignment(group, startHr, startMin, endHr, endMin);
		if (vehicle.assignments != null) {
			for (Pair<DriverCoreBean, ArrayList<Assignment>> entry: vehicle.assignments) {
				if (entry.first.getId() == driverInfo.getId()) {
					for (int i=entry.second.size()-1;i>=0;i--) {
						if (toAdd.overlaps(entry.second.get(i))) {
							entry.second.remove(i);
						}
					}
				}
			}
		}
		if (driver.assignments != null) {
			for (Pair<VehicleInfo, ArrayList<Assignment>> entry: driver.assignments) {
				if (entry.first.getVehicleId() == vehicleInfo.getVehicleId()) {
					for (int i=entry.second.size()-1;i>=0;i--) {
						if (toAdd.overlaps(entry.second.get(i))) {
							entry.second.remove(i);
						}
					}
				}
			}
		}
		group.removeAssignment(vehicleInfo, driverInfo,toAdd);
	}
	
	public int getDriverIndexInAvNotUsed(Driver driver) {
		for (int i=0,is=availableDriver.size();i<is;i++) {
			if (availableDriver.get(i).driver.getId() == driver.driver.getId())
				return i;
		}
		return -1;
	}
	
	public int getVehicleIndexInAvNotUsed(Vehicle vehicle) {
		for (int i=0,is=availableVehicle.size();i<is;i++) {
			if (availableVehicle.get(i).vehicle.getVehicleId() == vehicle.vehicle.getVehicleId())
				return i;
		}
		return -1;
	}
	
	public int getGroupIndexInAvNotUsed(ExtGroupBean group) {
		for (int i=0,is=availableGroup.size();i<is;i++) {
			if (availableGroup.get(i).coreGroup.getId() == group.coreGroup.getId())
				return i;
		}
		return -1;
	}
	
	
}

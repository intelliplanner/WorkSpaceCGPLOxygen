package com.ipssi.swm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
import com.ipssi.swm.AssignmentHelper.Driver;
import com.ipssi.swm.setup.GroupBean;
import com.ipssi.swm.setup.GroupSetupDao;
import com.ipssi.tracker.drivers.DriverCoreBean;
import com.ipssi.tracker.drivers.DriverDetailsDao;
import com.ipssi.tracker.drivers.DriverSkillsBean;
import com.ipssi.tracker.shiftsetup.DriverAttendanceBean;
import com.ipssi.tracker.shiftsetup.DriverShiftRosterBean;
import com.ipssi.tracker.shiftsetup.ShiftScheduleBean;
import com.ipssi.tracker.shiftsetup.ShiftScheduleDao;
import com.ipssi.tracker.shiftsetup.ShiftScheduleBean.DriverAssignment;
import com.ipssi.tracker.shiftsetup.ShiftScheduleBean.ScheduleDetails;
import com.ipssi.tracker.vehicleMaintenance.VehicleMaintenanceBean;
import com.ipssi.tracker.vehicleMaintenance.VehicleMaintenanceDao;

public class GeneratePlan {
	public static void generatePlan(Connection conn, int orgId, int shiftScheduleId, PlanControl planControl) throws Exception {
		//Plan => route, vehicle, driver
		// a driver has preferred vehicles and preferred routes
		//we get the list of drivers and assigning it to vehicle's that dont yet have driver
		//    ... then we get list of new vehicles ... and assign it to plan
		//    ... we get previous plan
		try {
			ShiftScheduleBean currPlan = ShiftScheduleDao.fetchScheduleDetails(conn, shiftScheduleId);
			Date date = currPlan.getDate();
			int shiftId = currPlan.getShiftId();
			int compScheduleId = getComparablePlanId(conn, shiftScheduleId);
			
			ShiftScheduleBean compPlan = ShiftScheduleDao.fetchScheduleDetails(conn, compScheduleId);			
			ArrayList<VehicleInfo> availableVehicle = VehicleInfo.getVehicleInfo(conn, orgId);
			ArrayList<DriverCoreBean> availableDriver = DriverDetailsDao.getDriverDataByOrg(conn, orgId);
			adjustForPlanControl(conn, availableVehicle, availableDriver, planControl, currPlan, orgId);
			boolean doMining = true;
			if (doMining) {
				ArrayList<Double> shovelQtyMet = new ArrayList<Double>();
				ArrayList<Double> truckQty = new ArrayList<Double>();
				HashMap<Integer, ArrayList<DriverAssignment>> byVehicleAssignment = new HashMap<Integer, ArrayList<DriverAssignment>>();
				HashMap<Integer, ArrayList<DriverAssignment>> byDriverAssignment = new HashMap<Integer, ArrayList<DriverAssignment>>();
				ArrayList<Double> timeRemainingForVeh = new ArrayList<Double>();
				ArrayList<Double> timeRemainingForDriver = new ArrayList<Double>();
				miningInitBase(conn, currPlan, availableVehicle, availableDriver, shovelQtyMet,  truckQty, byVehicleAssignment,  byDriverAssignment,  timeRemainingForVeh,  timeRemainingForDriver); 
				assignShovel(conn, currPlan, availableVehicle, availableDriver, shovelQtyMet,  truckQty, byVehicleAssignment,  byDriverAssignment,  timeRemainingForVeh,  timeRemainingForDriver);
				assignTruck(conn, currPlan, availableVehicle, availableDriver, shovelQtyMet,  truckQty, byVehicleAssignment,  byDriverAssignment,  timeRemainingForVeh,  timeRemainingForDriver);
				assignDriver(conn, currPlan, availableVehicle, availableDriver, shovelQtyMet,  truckQty, byVehicleAssignment,  byDriverAssignment,  timeRemainingForVeh,  timeRemainingForDriver);
			}
			else {
				ArrayList<GroupBean> coreGroups = GroupSetupDao.getGroups(conn, orgId, Misc.getUndefInt());
				ArrayList<AssignmentHelper.ExtGroupBean> groups =  fillExtGroupInfo(conn, coreGroups, orgId, new AverageParams());
				AssignmentHelper assignmentHelper = rearrangeDataStructure(groups, availableDriver, availableVehicle, currPlan, planControl.getPlanGuideline() == 1);
				
				ArrayList<AssignmentHelper.ExtGroupBean> groupsForRef =  fillExtGroupInfo(conn, coreGroups, orgId, new AverageParams());
				
				AssignmentHelper refHelper = rearrangeDataStructure(groupsForRef, availableDriver, availableVehicle, compPlan, planControl.getPlanGuideline() == 1);
				
				doAssignment(assignmentHelper, refHelper, true);
				doAssignment(assignmentHelper, refHelper, false);
				convertBackToShiftSchedule(assignmentHelper, currPlan);
			}
			ShiftScheduleDao.update(conn, currPlan);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static int getVehicleIndex(ArrayList<VehicleInfo> vehicleList, int vehicleId) {
		for (int i=0,is=vehicleList == null ? 0 : vehicleList.size(); i<is;i++)
			if (vehicleList.get(i).getVehicleId() == vehicleId)
				return i;
		return -1;
	}
	
	private static VehicleInfo getVehicleInfo(ArrayList<VehicleInfo> vehicleList, int vehicleId) {
		int idx = getVehicleIndex(vehicleList, vehicleId);
		return idx >= 0 ? vehicleList.get(idx) : null;
	}
	
	private static int getDriverIndex(ArrayList<DriverCoreBean> driverList, int driverId) {
		for (int i=0,is=driverList == null ? 0 : driverList.size(); i<is;i++)
			if (driverList.get(i).getId() == driverId)
				return i;
		return -1;
	}
	
	private static DriverCoreBean getDriverInfo(ArrayList<DriverCoreBean> vehicleList, int vehicleId) {
		int idx = getDriverIndex(vehicleList, vehicleId);
		return idx >= 0 ? vehicleList.get(idx) : null;
	}
	public static void assignShovel(Connection conn, ShiftScheduleBean currPlan, ArrayList<VehicleInfo> vehicleList, ArrayList<DriverCoreBean> driverList, ArrayList<Double> shovelQtyMet,  ArrayList<Double> truckQty, HashMap<Integer, ArrayList<DriverAssignment>> byVehicleAssignment, HashMap<Integer, ArrayList<DriverAssignment>> byDriverAssignment, ArrayList<Double> timeRemainingForVeh, ArrayList<Double> timeRemainingForDrive) {
		boolean allocationRemaining = false;
		boolean allShovelAllocated = false;
		boolean changesMade = true;
		while (changesMade) {
			changesMade = false;
			for (int i=0,is = currPlan.getDetails() == null ? 0 : currPlan.getDetails().size(); i<is; i++) {
				allocationRemaining = false;
				allShovelAllocated = true;
				changesMade = false;
				ScheduleDetails det = currPlan.getDetails().get(i);
				double leadTime = det.getLead();
				double shovelQtyAllocated = shovelQtyMet.get(i);
				if (shovelQtyAllocated >= det.getNumberOfTrips())
					continue;
				allocationRemaining = true;
				
				for (int j=0,js=vehicleList == null ? 0 : vehicleList.size();j<js;j++) {
					VehicleInfo vehicleInfo = vehicleList.get(j);
					if (!vehicleInfo.isShovel())
						continue;
					double timeRem = timeRemainingForVeh.get(j);
					if (timeRem < leadTime)
						continue;
					allShovelAllocated = false;
					int vehicleId = vehicleInfo.getVehicleId();
					double assignmentDurMin = timeRem;
					double loadTime = vehicleInfo.getLoadTimeMin();
					int maxVeh =(int) Math.round((double)leadTime/(double)loadTime);
					int numTrips = (int) Math.round((double)(maxVeh * timeRem)/(double)leadTime);
					double capTon = vehicleInfo != null ? vehicleInfo.getCapacityTonne() : 35;
					double qtyMetByAssignment = capTon * numTrips;
					double qtyNeeded = det.getNumberOfTrips()-shovelQtyMet.get(i);
					if (qtyNeeded < qtyMetByAssignment) {
						assignmentDurMin = (int)(assignmentDurMin*qtyNeeded/qtyMetByAssignment);
						qtyMetByAssignment = qtyNeeded;
					}
					else {
						
					}
					int driverId = Misc.getUndefInt();
					int driverIndex = -1;
					for (int k=0,ks=driverList == null ? 0 : driverList.size(); k<ks;k++) {
						double driverQtyRem = timeRemainingForDrive.get(k);
						if (driverQtyRem >= assignmentDurMin-0.01) {
							driverId = driverList.get(k).getId();
							driverIndex = k;
							break;
						}
					}
					DriverAssignment assignment = new DriverAssignment(vehicleInfo.getVehicleId(), driverId, det.getSourceLocId(), det.getDestination());
					shovelQtyMet.set(i, shovelQtyMet.get(i)+qtyMetByAssignment);
					det.shovelAssignmentCalc.add(assignment);
					timeRemainingForVeh.set(j, timeRemainingForVeh.get(j)-assignmentDurMin);
					ArrayList<DriverAssignment> aslist = byVehicleAssignment.get(vehicleId);
					changesMade = true;
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byVehicleAssignment.put(vehicleId, aslist);
					}
					aslist.add(assignment);
					timeRemainingForDrive.set(driverIndex, timeRemainingForDrive.get(driverIndex)-assignmentDurMin);
					aslist = byDriverAssignment.get(driverId);
					changesMade = true;
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byDriverAssignment.put(driverId, aslist);
					}
					aslist.add(assignment);
					break;
				}
			}
			
		}
	}
	
	public static void assignTruck(Connection conn, ShiftScheduleBean currPlan, ArrayList<VehicleInfo> vehicleList, ArrayList<DriverCoreBean> driverList, ArrayList<Double> shovelQtyMet,  ArrayList<Double> truckQty, HashMap<Integer, ArrayList<DriverAssignment>> byVehicleAssignment, HashMap<Integer, ArrayList<DriverAssignment>> byDriverAssignment, ArrayList<Double> timeRemainingForVeh, ArrayList<Double> timeRemainingForDrive) {
		boolean allocationRemaining = false;
		boolean allTruckAllocated = false;
		boolean changesMade = true;
		while (changesMade) {
			changesMade = false;
			for (int i=0,is = currPlan.getDetails() == null ? 0 : currPlan.getDetails().size(); i<is; i++) {
				allocationRemaining = false;
				allTruckAllocated = true;
				changesMade = false;
				ScheduleDetails det = currPlan.getDetails().get(i);
				double leadTime = det.getLead();
				double truckQtyAllocated = truckQty.get(i);
				if (truckQtyAllocated >= det.getNumberOfTrips())
					continue;
				allocationRemaining = true;
				
				for (int j=0,js=vehicleList == null ? 0 : vehicleList.size();j<js;j++) {
					VehicleInfo vehicleInfo = vehicleList.get(j);
					if (vehicleInfo.isShovel())
						continue;
					double timeRem = timeRemainingForVeh.get(j);
					if (timeRem < leadTime)
						continue;
					allTruckAllocated = false;
					int vehicleId = vehicleInfo.getVehicleId();
					double assignmentDurMin = timeRem;
					double loadTime = vehicleInfo.getLoadTimeMin();
					int maxVeh =1;
					int numTrips = (int) Math.round((double)(maxVeh * timeRem)/(double)leadTime);
					double capTon = vehicleInfo != null ? vehicleInfo.getCapacityTonne() : 35;
					double qtyMetByAssignment = capTon * numTrips;
					double qtyNeeded = det.getNumberOfTrips()-truckQty.get(i);
					if (qtyNeeded < qtyMetByAssignment) {
						assignmentDurMin = (int)(assignmentDurMin*qtyNeeded/qtyMetByAssignment);
						qtyMetByAssignment = qtyNeeded;
					}
					else {
						
					}
					int driverId = Misc.getUndefInt();
					int driverIndex = -1;
					for (int k=0,ks=driverList == null ? 0 : driverList.size(); k<ks;k++) {
						double driverQtyRem = timeRemainingForDrive.get(k);
						if (driverQtyRem >= assignmentDurMin-0.01) {
							driverId = driverList.get(k).getId();
							driverIndex = k;
							break;
						}
					}
					DriverAssignment assignment = new DriverAssignment(vehicleInfo.getVehicleId(), driverId, det.getSourceLocId(), det.getDestination());
					currPlan.addDriverAssignment(assignment);
					truckQty.set(i, truckQty.get(i)+qtyMetByAssignment);
					det.truckAssignmentCalc.add(assignment);
					timeRemainingForVeh.set(j, timeRemainingForVeh.get(j)-assignmentDurMin);
					ArrayList<DriverAssignment> aslist = byVehicleAssignment.get(vehicleId);
					changesMade = true;
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byVehicleAssignment.put(vehicleId, aslist);
					}
					aslist.add(assignment);
					timeRemainingForDrive.set(driverIndex, timeRemainingForDrive.get(driverIndex)-assignmentDurMin);
					aslist = byDriverAssignment.get(driverId);
					changesMade = true;
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byDriverAssignment.put(driverId, aslist);
					}
					aslist.add(assignment);
					break;
				}
			}
		}
	}
	
	public static void assignDriver(Connection conn, ShiftScheduleBean currPlan, ArrayList<VehicleInfo> vehicleList, ArrayList<DriverCoreBean> driverList, ArrayList<Double> shovelQtyMet,  ArrayList<Double> truckQty, HashMap<Integer, ArrayList<DriverAssignment>> byVehicleAssignment, HashMap<Integer, ArrayList<DriverAssignment>> byDriverAssignment, ArrayList<Double> timeRemainingForVeh, ArrayList<Double> timeRemainingForDrive) {
		boolean changesMade = true;
		while (changesMade) {
			changesMade = false;
			for (int i=0,is = currPlan.getDriverAssignment() == null ? 0 : currPlan.getDriverAssignment().size(); i<is; i++) {
				DriverAssignment assignment = currPlan.getDriverAssignment().get(i);
				if (assignment.getDriverId() > 0)
					continue;
				double assignmentDurMin = assignment.minutesAvailable(currPlan, conn);
				int driverId = Misc.getUndefInt();
				int driverIndex = -1;
				for (int k=0,ks=driverList == null ? 0 : driverList.size(); k<ks;k++) {
					double driverQtyRem = timeRemainingForDrive.get(k);
					if (driverQtyRem >= assignmentDurMin-0.01) {
						driverId = driverList.get(k).getId();
						driverIndex = k;
						break;
					}
				}
				if (driverId > 0) {
					changesMade = true;
					assignment.setDriverId(driverId);
					timeRemainingForDrive.set(driverIndex, timeRemainingForDrive.get(driverIndex)-assignmentDurMin);
					ArrayList<DriverAssignment> aslist = byDriverAssignment.get(driverId);
					changesMade = true;
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byDriverAssignment.put(driverId, aslist);
					}
					aslist.add(assignment);
					break;
				}
			}
		}
	}

	public static void miningInitBase(Connection conn, ShiftScheduleBean currPlan, ArrayList<VehicleInfo> vehicleList, ArrayList<DriverCoreBean> driverList, ArrayList<Double> shovelQtyMet,  ArrayList<Double> truckQty, HashMap<Integer, ArrayList<DriverAssignment>> byVehicleAssignment, HashMap<Integer, ArrayList<DriverAssignment>> byDriverAssignment, ArrayList<Double> timeRemainingForVeh, ArrayList<Double> timeRemainingForDriver) {
		int durMin = currPlan.getShiftDurMin(conn);
		for (int i=0,is= vehicleList == null ? 0 : vehicleList.size(); i<is;i++) {
			timeRemainingForVeh.add(new Double(durMin));
		}
		for (int i=0,is= driverList == null ? 0 : driverList.size(); i<is;i++) {
			timeRemainingForDriver.add(new Double(durMin));
		}
		for (ScheduleDetails det: currPlan.getDetails()) {
			shovelQtyMet.add(0.0);
			truckQty.add(0.0);
		}
		for (DriverAssignment assignment: currPlan.getDriverAssignment()) {
			int idx = currPlan.getAssignmentInTargetIndex(assignment);
			if (idx >= 0) {
				ScheduleDetails sched = currPlan.getDetails().get(idx);
				int vehicleId = assignment.getVehicleId();
				int driverId = assignment.getDriverId();
				int vehIndex = getVehicleIndex(vehicleList, vehicleId);
				int driverIndex = getDriverIndex(driverList, driverId);
				VehicleInfo vehicleInfo = vehIndex >= 0 ? vehicleList.get(vehIndex) : null;
				DriverCoreBean driverInfo = driverIndex >= 0 ? driverList.get(driverIndex) : null;
				boolean isShovel = vehicleInfo != null && vehicleInfo.isShovel();
				double capTon = vehicleInfo != null ? vehicleInfo.getCapacityTonne() : 35;
				double capVol = vehicleInfo != null ? vehicleInfo.getCapacityVolume() : 15;
				int leadTimeMin = (int) (sched.getLead()*10);
				int assignmentDurMin = assignment.minutesAvailable(currPlan, conn);
				if (vehicleInfo != null)
					timeRemainingForVeh.set(vehIndex, timeRemainingForVeh.get(vehIndex)-assignmentDurMin);
				if (isShovel) {
					double loadTime = vehicleInfo.getLoadTimeMin();
					int maxVeh =(int) Math.round((double)leadTimeMin/(double)loadTime);
					int numTrips = (int) Math.round((double)(maxVeh * assignmentDurMin)/(double)leadTimeMin);
					double qtyMetByAssignment = capTon * numTrips;
					double qtyNeeded = sched.getNumberOfTrips()-shovelQtyMet.get(idx);
					if (qtyNeeded < qtyMetByAssignment) {
						assignmentDurMin = (int)(assignmentDurMin*qtyNeeded/qtyMetByAssignment);
						qtyMetByAssignment = qtyNeeded;
					}
					else {
						
					}
					shovelQtyMet.set(idx, shovelQtyMet.get(idx)+qtyMetByAssignment);
					sched.shovelAssignmentCalc.add(assignment);
				}
				else {
					int maxVeh =1;
					int numTrips = (int) Math.round((double)(maxVeh * assignmentDurMin)/(double)leadTimeMin);
					double qtyMetByAssignment = capTon * numTrips;
					double qtyNeeded = sched.getNumberOfTrips()-truckQty.get(idx);
					if (qtyNeeded < qtyMetByAssignment) {
						assignmentDurMin = (int)(assignmentDurMin*qtyNeeded/qtyMetByAssignment);
						qtyMetByAssignment = qtyNeeded;
					}
					else {
						
					}

					truckQty.set(idx,truckQty.get(idx)+qtyMetByAssignment);
					sched.truckAssignmentCalc.add(assignment);
						
				}
				if (vehicleInfo != null) {
					timeRemainingForVeh.set(vehIndex, timeRemainingForVeh.get(vehIndex)-assignmentDurMin);
					ArrayList<DriverAssignment> aslist = byVehicleAssignment.get(vehicleId);
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byVehicleAssignment.put(vehicleId, aslist);
					}
					aslist.add(assignment);
				}
				if (driverInfo != null) {
					timeRemainingForVeh.set(driverIndex, timeRemainingForDriver.get(driverIndex)-assignmentDurMin);

					ArrayList<DriverAssignment> aslist = byDriverAssignment.get(driverId);
					if (aslist == null) {
						aslist = new ArrayList<DriverAssignment>();
						byDriverAssignment.put(driverId, aslist);
					}
					aslist.add(assignment);
				}
			}
		}
	}
	public static void convertBackToShiftSchedule(AssignmentHelper assignmentHelper, ShiftScheduleBean currPlan) {
		currPlan.getDriverAssignment().clear();
		for (AssignmentHelper.ExtGroupBean group:assignmentHelper.groupLookup.values()) {
			for(Triple<VehicleInfo, DriverCoreBean, AssignmentHelper.Assignment> entry:group.assignments) {
				int vehicleId = entry.first == null ? Misc.getUndefInt() : entry.first.getVehicleId();
				int driverId = entry.second == null ? Misc.getUndefInt() : entry.second.getId();
				int startHour = entry.third == null ? Misc.getUndefInt() : entry.third.getStartHr();
				int stopHour = entry.third == null ? Misc.getUndefInt() : entry.third.getEndHr();
				int startMin = entry.third == null ? Misc.getUndefInt() : entry.third.getStartMin();
				int stopMin = entry.third == null ? Misc.getUndefInt() : entry.third.getEndMin();
				int srcId = Misc.getUndefInt();
				int srcType = Misc.getUndefInt();
				int destId = Misc.getUndefInt();
				int destType = Misc.getUndefInt();
				ShiftScheduleBean.DriverAssignment assign = new ShiftScheduleBean.DriverAssignment(vehicleId, driverId, startHour, startMin, stopHour, stopMin, null, group.coreGroup.getId(), Misc.getUndefInt(), null, null, srcId, srcType, destId, destType);
				currPlan.addDriverAssignment(assign);
			}
		}
	}
	
	public static void doAssignment(AssignmentHelper assignmentHelper, AssignmentHelper refHelper, boolean forLoader) {
		doDriverVehicle(assignmentHelper, refHelper, forLoader);
		assignmentHelper.populateAvailable(forLoader);
	
		for (;assignmentHelper.availableGroup.size() > 0;) {
			int pos = assignmentHelper.availableGroup.size()-1;
			AssignmentHelper.ExtGroupBean group = assignmentHelper.availableGroup.get(pos);
			assignmentHelper.availableGroup.remove(pos);
			//get the best available driver ... for the group
			int index = -1;
			Driver matchDriver = null;
			for (index=assignmentHelper.availableDriver.size()-1;index >= 0; index--) {
				Driver driver = assignmentHelper.availableDriver.get(index);
				if (driver.getMinSkillLevelLoader() > 9)
					continue;
				//check if driver has skill for the group of matching load skill set ... if so pick and assign ... todo
				boolean hasMatching = false;
				
				for (DriverSkillsBean skill : driver.driver.getDriverSkillsList()) {
					if (skill.getFactor1() == group.coreGroup.getId()) {
						hasMatching = true;
						break;
					}
				}
				if (hasMatching) {
					matchDriver = driver;
					break;
				}
			}
			if (index >= 0 && matchDriver != null) {
				assignmentHelper.availableDriver.remove(index);
			}
			AssignmentHelper.Vehicle vehicle = matchDriver == null || matchDriver.tentativeVehicle == null ? null : assignmentHelper.vehicleLookup.get(matchDriver.tentativeVehicle.getVehicleId());
			if (vehicle == null && assignmentHelper.availableVehicle.size() > 0) {
				vehicle = assignmentHelper.availableVehicle.get(assignmentHelper.availableVehicle.size()-1);
			}
			if (vehicle != null) {
			
				int indexInAv = vehicle.indexInAv;
				if (indexInAv >= 0)
					assignmentHelper.availableVehicle.remove(indexInAv);
				for (int i=indexInAv,is=assignmentHelper.availableVehicle.size();i<is;i++)
					assignmentHelper.availableVehicle.get(i).indexInAv = i;
			}
			if (vehicle == null)
				continue;//nothing can be done for this group ...
			
			assignmentHelper.addAssignment(group, vehicle == null ? null : vehicle.vehicle, matchDriver==null ? null : matchDriver.driver, Misc.getUndefInt(), Misc.getUndefInt(),Misc.getUndefInt(),Misc.getUndefInt());
			int temp1 = 0;
			if (group.getAssignFrac(forLoader) < 0.95) {
				temp1 = assignmentHelper.insertAvGroup(group, forLoader);
				for (int i=temp1, is = assignmentHelper.availableGroup.size();i<is;i++)
					assignmentHelper.availableGroup.get(i).indexInAv = i;
			}
			if (vehicle != null && vehicle.getAssignFrac() < 0.95) {
				temp1 = assignmentHelper.insertAvVehicle(vehicle, forLoader);
				for (int i=temp1, is = assignmentHelper.availableVehicle.size();i<is;i++)
					assignmentHelper.availableVehicle.get(i).indexInAv = i;
			}
			if (matchDriver != null && matchDriver.getAssignFrac() < 0.95) {
				temp1 = assignmentHelper.insertAvDriver(matchDriver, forLoader);
				for (int i=temp1, is = assignmentHelper.availableDriver.size();i<is;i++)
					assignmentHelper.availableDriver.get(i).indexInAv = i;
			}
		}
	
	}
	
	public static void doDriverVehicle(AssignmentHelper assignmentHelper, AssignmentHelper refHelper, boolean forLoader) {
		for (AssignmentHelper.Driver driver:assignmentHelper.driverLookup.values()) {
			DriverCoreBean core = driver.driver;
			if (forLoader && driver.getMinSkillLevelLoader() > 9)
				continue; //is not a loader operator
			boolean assigned = false;
			if (core.getVehicleId1() > 0) {
				AssignmentHelper.Vehicle veh1 = assignmentHelper.vehicleLookup.get(core.getVehicleId1());
				if (veh1 != null)
					assigned = assignmentHelper.setTentativeVehicle(core.getId(), core.getVehicleId1());
			}
			if (!assigned) {
				AssignmentHelper.Vehicle veh1 = assignmentHelper.vehicleLookup.get(core.getVehicleId2());
				if (veh1 != null)
					assigned = assignmentHelper.setTentativeVehicle(core.getId(), core.getVehicleId2());
			}
			if (!assigned) {
				AssignmentHelper.Vehicle veh1 = assignmentHelper.vehicleLookup.get(driver.getSomeVehicleAssigned(forLoader));
				if (veh1 != null)
					assigned = assignmentHelper.setTentativeVehicle(core.getId(), veh1.vehicle.getVehicleId());
			}
			if (!assigned && refHelper != null) {
				Driver refDriver = refHelper.driverLookup.get(core.getId());
				if (refDriver != null) {
					AssignmentHelper.Vehicle veh1 = assignmentHelper.vehicleLookup.get(refDriver.getSomeVehicleAssigned(forLoader));
					if (veh1 != null)
							assigned = assignmentHelper.setTentativeVehicle(core.getId(), veh1.vehicle.getVehicleId());
				}
			}
		}
	}
	
	private static AssignmentHelper rearrangeDataStructure(ArrayList<AssignmentHelper.ExtGroupBean> groups, ArrayList<DriverCoreBean> availableDriver, ArrayList<VehicleInfo> availableVehicle, ShiftScheduleBean currPlan, boolean fillWithCurrPlan) {
		AssignmentHelper retval = new AssignmentHelper();
		for (int i=0,is = groups.size();i<is;i++) {
			retval.groupLookup.put(groups.get(i).coreGroup.getId(), groups.get(i));
		}
		for (int i=0,is = availableDriver.size();i<is;i++) {
			AssignmentHelper.Driver entry = new AssignmentHelper.Driver();
			entry.driver = availableDriver.get(i);
			retval.driverLookup.put(entry.driver.getId(), entry);
		}
		for (int i=0,is = availableVehicle.size();i<is;i++) {
			AssignmentHelper.Vehicle entry = new AssignmentHelper.Vehicle();
			entry.vehicle = availableVehicle.get(i);
			retval.vehicleLookup.put(entry.vehicle.getVehicleId(), entry);
		}
		if (fillWithCurrPlan) {
			for (ShiftScheduleBean.DriverAssignment assignment : currPlan.getDriverAssignment()) {
				AssignmentHelper.ExtGroupBean group = retval.groupLookup.get(assignment.getFactor1());
				if (group == null)
					continue;
				AssignmentHelper.Driver driver = retval.driverLookup.get(assignment.getDriverId());
				if (driver == null && assignment.getDriverId() > 0)
					continue;
				AssignmentHelper.Vehicle vehicle = retval.vehicleLookup.get(assignment.getVehicleId());
				if (vehicle == null && assignment.getVehicleId() > 0)
					continue;
				if (group == null || (driver == null && vehicle == null))
					continue;
				retval.addAssignment(group, vehicle == null ? null : vehicle.vehicle, driver == null ? null : driver.driver, assignment.getDriverShiftStartHour(), assignment.getDriverShiftStartMin(), assignment.getDriverShiftStopHour(), assignment.getDriverShiftStopMin());
			}
		}
		return retval;
	}
	private static void addMinute(Date dt, int min) {
		if (dt == null)
			return;
		dt.setTime(dt.getTime()+min*60*1000);
	}
	private static void adjustForPlanControl(Connection conn, ArrayList<VehicleInfo> availableVehicle, ArrayList<DriverCoreBean> availableDriver, PlanControl planControl, ShiftScheduleBean schedule, int orgId) throws Exception {
		if (!planControl.isIgnoreDriverAv()) {//remove drivers from list that are not available
			//get attendance roster
			//get shift roster
			Date dt = new Date(schedule.getDate().getTime());
			ArrayList<DriverAttendanceBean> attendanceRoster = ShiftScheduleDao.getDriverAttendanceRoster(conn, dt, orgId);
			ArrayList<DriverShiftRosterBean> shiftRoster = ShiftScheduleDao.getDriverShiftRoster(conn, dt, orgId);
			HashMap<Integer, Integer> driverAttendance = new HashMap<Integer, Integer>();
			if (attendanceRoster.size() > 0) {
				for (DriverAttendanceBean item:attendanceRoster) {
					Integer id = new Integer(item.getDriverId());
					driverAttendance.put(id,id);
				}
			}
			else if (shiftRoster.size() > 0){
				for (DriverShiftRosterBean item:shiftRoster) {
					Integer id = new Integer(item.getDriverId());
					driverAttendance.put(id,id);
				}
			}
			if (!driverAttendance.isEmpty()) {
				for (int i=availableDriver.size()-1;i>=0;i--) {
					if (!driverAttendance.containsKey(availableDriver.get(i))) {
						availableDriver.remove(i);
					}
				}
			}
		}
		
		if (!planControl.isIgnoreVehicleAv()) {//remove vehicle that are not available
			HashMap<Integer, Integer> unavVeh = new HashMap<Integer, Integer>();
			 List<VehicleMaintenanceBean> maintVeh = VehicleMaintenanceDao.getAllVehicleUnderMaint(conn, orgId);
			 for (VehicleMaintenanceBean maint : maintVeh) {
				 if (maint.getActualOutDate()== null ) {
					 Integer id = new Integer(maint.getVehicleId());
					 unavVeh.put(id, id);
				 }
			 }
			 for (int i=availableVehicle.size()-1;i>=0;i--) {
				 VehicleInfo vehicleInfo = availableVehicle.get(i);
				 int opstat = vehicleInfo.getOpStatus();
				 boolean inbkdStat = opstat == 3 || opstat == 4 || opstat == 8;
				 boolean isDelayedData  = vehicleInfo.getLastTrackTime() == null || (System.currentTimeMillis() - vehicleInfo.getLastTrackTime().getTime() > 24*3600*60);
				 if (inbkdStat || isDelayedData || unavVeh.containsKey(availableVehicle.get(i).getVehicleId())) {
					 availableVehicle.remove(i);
				 }
			 }
		}
		
		for (int i=availableDriver == null ? -1 : availableDriver.size()-1; i>=0;i--) {
			DriverCoreBean driver = availableDriver.get(i);
			//check if exists in not avail list
			boolean found = false;
			for (int j=0,js = planControl.getIgnoreDriverList() == null ? 0 : planControl.getIgnoreDriverList().size();j<js;j++) {
				if (planControl.getIgnoreDriverList().get(j).first == driver.getId()) {
					found = true;
					break;
				}
			}
			if (found)
				availableDriver.remove(i);
		}
		for (int i=availableVehicle == null ? -1 : availableVehicle.size()-1; i>=0;i--) {
			VehicleInfo vehicle = availableVehicle.get(i);
			//check if exists in not avail list
			boolean found = false;
			for (int j=0,js = planControl.getIgnoreVehicleList() == null ? 0 : planControl.getIgnoreVehicleList().size();j<js;j++) {
				if (planControl.getIgnoreVehicleList().get(j).first == vehicle.getVehicleId()) {
					found = true;
					break;
				}
			}
			if (found)
				availableVehicle.remove(i);
		}
	}
	
	private static ArrayList<AssignmentHelper.ExtGroupBean> fillExtGroupInfo(Connection conn, ArrayList<GroupBean> coreGroups, int orgId, AverageParams params) throws Exception {
		try {
			ArrayList<AssignmentHelper.ExtGroupBean> retval = new ArrayList<AssignmentHelper.ExtGroupBean>();
			for (GroupBean core:coreGroups) {
				AssignmentHelper.ExtGroupBean extGroup = new AssignmentHelper.ExtGroupBean();
				extGroup.coreGroup = core;
				retval.add(extGroup);
				int binCount = core.getLoadStations().size();
				int vehNeed = (int) Math.ceil((double)binCount/(params.hrsOfOps/params.hrsPerTrip+0.0001));
				extGroup.vehNeed = vehNeed;
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static int getComparablePlanId(Connection conn, int refScheduleId) throws Exception {
		try {
			int retval = Misc.getUndefInt();
			PreparedStatement ps = conn.prepareStatement("select ref.id, (case when ref.shift_id = orig.shift_id then 1 else 0 end)  from shift_schedule_info orig  cross join shift_schedule_info ref where orig.id = ? and ref.day < orig.day order by  (case when ref.shift_id = orig.shift_id then 1 else 0 end) desc, ref.day desc  ");
			ps.setInt(1, refScheduleId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				retval = rs.getInt(1);
			}
			rs.close();
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
		
}

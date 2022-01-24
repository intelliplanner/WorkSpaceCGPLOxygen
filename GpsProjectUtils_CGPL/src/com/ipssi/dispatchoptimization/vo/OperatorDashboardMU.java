package com.ipssi.dispatchoptimization.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class OperatorDashboardMU {
	public static int DUMPER_TYPE = 0;
	public static int SHOVEL_TYPE = 1;
	public static int LOAD_SITE_PILE_TYPE = 1;
	public static int UNLOAD_SITE_PILE_TYPE = 2;
	public static int FUEL_DIM_ID = 3;
	public static int SHIFT_TIME_SEC = 8 * 60 * 60;
	public static int SHIFT_TIME_HR = 8;
	public static int TOTAL_QUEUE_TIME_HR = 2;
	public static int TOTAL_TRANSITION_TIME_SEC = 2 * 60 * 60;
	public static int TRANSITION_TIME_SEC = 60;
	public static int LOADING_UNLOADING_TIME_MIN = 2;
	public static int AVG_DUMPER_SPEED_KM_HR = 20;
	
	private static ConcurrentHashMap<Integer, Map<Integer,LiveAssignmentDTO>> liveAssignmentsOnPit = new ConcurrentHashMap<Integer, Map<Integer,LiveAssignmentDTO>>();
	private static ConcurrentHashMap<Integer, RouteVo> routes = new ConcurrentHashMap<Integer, RouteVo>();
	private static ConcurrentHashMap<Integer, LoadSiteVO> loadSites = new ConcurrentHashMap<Integer, LoadSiteVO>();
	private static ConcurrentHashMap<Integer, UnloadSiteVo> unLoadSites = new ConcurrentHashMap<Integer, UnloadSiteVo>();
	private static ConcurrentHashMap<Integer, ShovelInfoVo> shovels = new ConcurrentHashMap<Integer, ShovelInfoVo>();
	private static ConcurrentHashMap<Integer, DumperInfoVo> dumpers = new ConcurrentHashMap<Integer, DumperInfoVo>();
	private static ConcurrentHashMap<Integer, ArrayList<Integer>> shovelAssignedDumpers = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	//vehicle_id and alert_def_id
	private static ConcurrentHashMap<Pair<Integer,Integer>, DOSAlertDTO> alerts = new ConcurrentHashMap<Pair<Integer,Integer>, DOSAlertDTO>();
	private static ConcurrentHashMap<Integer, DOSAlertDTO> latestVehicleAlert= new ConcurrentHashMap<Integer, DOSAlertDTO>();
	private static ConcurrentHashMap<Pair<Integer,Integer>,Long> alertsDismiss = new ConcurrentHashMap<Pair<Integer,Integer>, Long>();
	
	
	public static Long getAlertsDismissCache(Pair<Integer,Integer> vehId) {
		return alertsDismiss.get(vehId);
	}
	

	public static boolean getAlertDismissFlagOnVehicle(int vehicleId, int dimId) {
		Pair<Integer, Integer> vehDim = new Pair<Integer, Integer>(vehicleId,dimId);// for normal
		Pair<Integer, Integer> unDefDIM = new Pair<Integer, Integer>(Misc.UNDEF_VALUE, dimId);// for all vehicle, alert stop for 1 param
		Pair<Integer, Integer> vehUndef = new Pair<Integer, Integer>(vehicleId,Misc.UNDEF_VALUE);// for vehicle, all alert stop
		Pair<Integer, Integer> allVehAllAlerts = new Pair<Integer, Integer>(Misc.UNDEF_VALUE,Misc.UNDEF_VALUE);// for all vehicle, all alert stop
		if (alertsDismiss.contains(unDefDIM)) {
			// for all vehicle, alert stop for 1 param
			long dismissdurr = alertsDismiss.get(unDefDIM);
			if (dismissdurr < System.currentTimeMillis()) {
				alertsDismiss.remove(unDefDIM);
				return false;
			}else
				return true;
		} else if (alertsDismiss.contains(vehUndef)) {
			// for vehicle, all alert stop
			long dismissdurr = alertsDismiss.get(vehUndef);
			if (dismissdurr < System.currentTimeMillis()) {
				alertsDismiss.remove(vehUndef);
				return false;
			}else
				return true;
		} else if (alertsDismiss.contains(vehDim)) {
			// for vehicle, for alert stop
			long dismissdurr = alertsDismiss.get(vehDim);
			if (dismissdurr < System.currentTimeMillis()) {
				alertsDismiss.remove(vehDim);
				return false;
			}else
				return true;
		} else if (alertsDismiss.contains(allVehAllAlerts)) {
			// for all vehicle, all alert stop
			long dismissdurr = alertsDismiss.get(allVehAllAlerts);
			if (dismissdurr < System.currentTimeMillis()) {
				alertsDismiss.remove(allVehAllAlerts);
				return false;
			}else
				return true;
		} 
		return false;
	}



	
	public static void setAlertsDismissCache(Pair<Integer,Integer> pair, Long dur) {
		if(alertsDismiss.contains(pair)){
			alertsDismiss.remove(pair);
		}
		alertsDismiss.put(pair, dur);
	}
	public static boolean containsAlerts(Pair<Integer,Integer> pair) {
		return alerts.containsKey(pair);
	}
	public static  DOSAlertDTO getAlerts(Pair<Integer,Integer> pair) {
		return alerts.get(pair);
	}
	public static  DOSAlertDTO getLatestVehicleAlert(Integer vehicleId) {
		return latestVehicleAlert.get(vehicleId);
	}
	public static void updateAlerts(Pair<Integer,Integer> pair, DOSAlertDTO alert, boolean notify){
	 System.out.println("Received Alert(Notify="+notify+"), Vehicle="+pair.first+",AlertDef="+pair.second+" from DOS ="+alert.toString());
		if(alerts.contains(pair)){
			alerts.remove(pair);
		}
		//if(alert.isOn()){
			alerts.put(pair, alert);
			//latestVehicleAlert for OPratorDashBoard
			latestVehicleAlert.put(pair.first, alert);
		//}
		if(notify){
			DOSNotificationHelper.sendNotification(alert);
		}
	}

	
	public static ConcurrentHashMap<Pair<Integer, Integer>, DOSAlertDTO> getAlerts() {
		return alerts;
	}
	public static void setAlerts(
			ConcurrentHashMap<Pair<Integer, Integer>, DOSAlertDTO> alerts) {
		OperatorDashboardMU.alerts = alerts;
	}
	public static ConcurrentHashMap<Integer, DOSAlertDTO> getLatestVehicleAlert() {
		return latestVehicleAlert;
	}
	public static void setLatestVehicleAlert(
			ConcurrentHashMap<Integer, DOSAlertDTO> latestVehicleAlert) {
		OperatorDashboardMU.latestVehicleAlert = latestVehicleAlert;
	}
	public static ConcurrentHashMap<Pair<Integer, Integer>, Long> getAlertsDismiss() {
		return alertsDismiss;
	}
	public static void setAlertsDismiss(ConcurrentHashMap<Pair<Integer, Integer>, Long> alertsDismiss) {
		OperatorDashboardMU.alertsDismiss = alertsDismiss;
	}
	public static void main(String[] args) {
		DOSAlertDTO alert=new DOSAlertDTO();
		alert.setVehicle_id(27281);
		alert.setAlertId(1);
		alert.setAlertDefId(1);
		alert.setStatus(1);
		alert.setParam_value(8);
		Pair<Integer,Integer> pair = new Pair<Integer, Integer>(27281,1);
		updateAlerts(pair, alert, true);
	}


	
	public static ConcurrentHashMap<Integer, ArrayList<Integer>> getShovelAssignedDumpers() {
		return shovelAssignedDumpers;
	}
	public static ArrayList<Integer> getAssignedDumpersForShovel(int shovelId) {
		return shovelAssignedDumpers.get(shovelId)==null?new ArrayList<Integer>():shovelAssignedDumpers.get(shovelId);
	}
	public static void setShovelAssignedDumpers(
			ConcurrentHashMap<Integer, ArrayList<Integer>> shovelAssignedDumpers) {
		OperatorDashboardMU.shovelAssignedDumpers = shovelAssignedDumpers;
	}



	public static CoreVehicleInfoVo getVehicleInfo(int vehicleId) {
		CoreVehicleInfoVo retval = dumpers.get(vehicleId);
		if (retval == null)
			retval = shovels.get(vehicleId);
		return retval;
	}

	

	public static RouteVo getRoute(int routeId) {
		return routes.get(routeId);
	}

	public static ShovelInfoVo getShovel(int shovelId) {
		return shovels.get(shovelId);
	}

	public static DumperInfoVo getDumper(int dumperId) {
		return dumpers.get(dumperId);
	}

	
	public static void setRoute(RouteVo obj) {
		if (routes.contains(obj.getRouteId())) {
			routes.remove(obj.getRouteId());
			routes.put(obj.getRouteId(), obj);
			
		} else
			routes.put(obj.getRouteId(), obj);
	}

	public static void setShovel(ShovelInfoVo obj) {
		if (shovels.contains(obj.getId())) {
			shovels.remove(obj.getId());
			shovels.put(obj.getId(), obj);
		} else
			shovels.put(obj.getId(), obj);
	}

	public static void setDumper(DumperInfoVo obj) {
		if (dumpers.contains(obj.getId())) {
			dumpers.remove(obj.getId());
			dumpers.put(obj.getId(), obj);
		} else
			dumpers.put(obj.getId(), obj);
	}

	private OperatorDashboardMU() {

	}

	public static void setUnloadSite(UnloadSiteVo obj) {
		if (unLoadSites.contains(obj.getId())) {
			unLoadSites.remove(obj.getId());
			unLoadSites.put(obj.getId(), obj);
		} else
			unLoadSites.put(obj.getId(), obj);
	}

	public static void setLoadSite(LoadSiteVO obj) {
		if (loadSites.contains(obj.getId())) {
			loadSites.remove(obj.getId());
			loadSites.put(obj.getId(), obj);
		} else
			loadSites.put(obj.getId(), obj);
	}

	public static LoadSiteVO getLoadSite(int loadSiteId) {
		return loadSites.get(loadSiteId);
	}

	public static UnloadSiteVo getUnLoadSite(int unLoadSiteId) {
		return unLoadSites.get(unLoadSiteId);
	}
	
	public static ConcurrentHashMap<Integer, RouteVo> getRoutes() {
		return routes;
	}

	public static void setRoutes(ConcurrentHashMap<Integer, RouteVo> routes) {
		OperatorDashboardMU.routes = routes;
	}

	public static ConcurrentHashMap<Integer, LoadSiteVO> getLoadSites() {
		return loadSites;
	}

	public static void setLoadSites(ConcurrentHashMap<Integer, LoadSiteVO> loadSites) {
		OperatorDashboardMU.loadSites = loadSites;
	}

	public static ConcurrentHashMap<Integer, UnloadSiteVo> getUnLoadSites() {
		return unLoadSites;
	}

	public static void setUnLoadSites(
			ConcurrentHashMap<Integer, UnloadSiteVo> unLoadSites) {
		OperatorDashboardMU.unLoadSites = unLoadSites;
	}

	public static ConcurrentHashMap<Integer, ShovelInfoVo> getShovels() {
		return shovels;
	}

	public static void setShovels(ConcurrentHashMap<Integer, ShovelInfoVo> shovels) {
		OperatorDashboardMU.shovels = shovels;
	}

	public static ConcurrentHashMap<Integer, DumperInfoVo> getDumpers() {
		return dumpers;
	}

	public static void setDumpers(ConcurrentHashMap<Integer, DumperInfoVo> dumpers) {
		OperatorDashboardMU.dumpers = dumpers;
	}

	public static ConcurrentHashMap<Integer, Map<Integer,LiveAssignmentDTO>> getLiveAssignmentsOnPit() {
		return liveAssignmentsOnPit;
	}

	public static void setLiveAssignmentsOnPit(ConcurrentHashMap<Integer, Map<Integer,LiveAssignmentDTO>> liveAssignmentsOnPit) {
		OperatorDashboardMU.liveAssignmentsOnPit = liveAssignmentsOnPit;
	}
	public static void setliveAssignmentListForPit(int pitId,Map<Integer,LiveAssignmentDTO> assignmentList) {
		 liveAssignmentsOnPit.remove(pitId);
		 liveAssignmentsOnPit.put(pitId,assignmentList);
	}
	public static Map<Integer,LiveAssignmentDTO> getLiveAssignmentForPit(int portNodeId, int pitId) {
		return liveAssignmentsOnPit.get(pitId)==null? new HashMap<Integer,LiveAssignmentDTO>():liveAssignmentsOnPit.get(pitId);
	}
	public static void addAssignedDumperListToShovel( int shovelId,ArrayList<Integer> assignedDumperList) {
		shovelAssignedDumpers.remove(shovelId);
		shovelAssignedDumpers.put(shovelId,assignedDumperList); ;
	}
	

	public static void addLiveAssignmentsOnPit(int pitId, LiveAssignmentDTO liveAssignmentDTO) {
		
		Map<Integer,LiveAssignmentDTO> liveAssignmentsOnPitList = getLiveAssignmentForPit (0, pitId);
		liveAssignmentsOnPitList.remove(liveAssignmentDTO.getShovel().getId());
		liveAssignmentsOnPitList.put(liveAssignmentDTO.getShovel().getId(),liveAssignmentDTO);
		//No need to put again 
		liveAssignmentsOnPit.put(pitId,liveAssignmentsOnPitList);
	}
	
	public static void addLiveAssignmentsOnPitList(int pitId, Map<Integer,LiveAssignmentDTO> liveAssignmentsOnPitList) {
		if (liveAssignmentsOnPitList != null) {
			if(liveAssignmentsOnPit.contains(pitId))
				liveAssignmentsOnPit.remove(pitId);
			liveAssignmentsOnPit.put(pitId, liveAssignmentsOnPitList);;			
		}
	}
	
	public static void addShovelAssignedDumpers (int shovelId, int dumperId) {
		ArrayList<Integer> list = getAssignedDumpersForShovel (shovelId);
		list.add(dumperId);
		shovelAssignedDumpers.put(shovelId, list);
	}
	public static void removeShovelAssignedDumper (int shovelId, int dumperId) {
		ArrayList<Integer> list=getAssignedDumpersForShovel (shovelId);
		if(list!=null)
			list.remove(dumperId);
		shovelAssignedDumpers.put(shovelId,list);
	}
	public static void reAssignShovelDumper (int shovelId, int dumperId,int prevShovelId) {
		removeShovelAssignedDumper (prevShovelId, dumperId);
		addShovelAssignedDumpers (shovelId, dumperId);
	}
}

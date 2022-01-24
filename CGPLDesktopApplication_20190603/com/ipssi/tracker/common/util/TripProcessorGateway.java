package com.ipssi.tracker.common.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.ipssi.dataprocessor.ejb.session.DataCacheAccessorRemote;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tripprocessor.ejb.session.CacheAccessorRemote;

public class TripProcessorGateway {

	private final static Logger logger = Logger.getLogger(TripProcessorGateway.class);
	private static CacheAccessorRemote syncObject = null;
	private static boolean nonEjbCall = true; // for message base refresh entities.

	static {
		try {
			InitialContext context = new InitialContext(PropertyManager.getProperties());
			syncObject = (CacheAccessorRemote) context.lookup(PropertyManager.getString("trip.remote.jndi"));
		} catch (Exception e) {
			logger.error("Problem in looking up ejb ", e);
		}

	}

	/**
	 * 
	 * @param vehicle_id
	 */
	public static void refreshVehicles(ArrayList<Integer> vehicleIDs, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("TripProcessorGateway.refreshVehicles() is called From New EJB Method And Instance "+serverName+" #####");
				for (Integer itId : vehicleIDs) {
					TPQueueSender.send(new Triple<Integer, Integer,String> (itId,com.ipssi.reporting.common.util.ApplicationConstants.VEHICLE,serverName));
				}
				return;
			}else{
				System.out.println("TripProcessorGateway.refreshVehicles() is called From OLD EJB Method");
			    syncObject.refreshVehicles(vehicleIDs);
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the vehicle ", e);
		}
	}

	/**
	 * 
	 * @param opStation id
	 */
	public static void refreshMapSets(ArrayList<Integer> opstationIDs, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("TripProcessorGateway.refreshMapSets() is called From New EJB Method And Instance "+serverName+" #####");
				for (Integer itId : opstationIDs) {
					TPQueueSender.send(new Triple<Integer, Integer, String> (itId,com.ipssi.reporting.common.util.ApplicationConstants.OPSTATION,serverName));
				}
				return;
			}else{
				System.out.println("TripProcessorGateway.refreshMapSets() is called From OLD EJB Method");
			    syncObject.refreshOpStations(opstationIDs);
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the opStation ", e);
		}
	}

	/**
	 * 
	 * @param not operating opStation
	 */
	public static void getNotOperatingRegions() {
		try {
			syncObject.getNotOperatingRegions();
		} catch (Exception e) {
			logger.error("Problem while getting not operating opStation ", e);
		}
	}

	/**
	 * 
	 * @param Stranded Vehicle
	 */
	public static void getStrandedVehicle(HashMap<String, String> filterCriteriaMap) {
		try {
			syncObject.getStrandedVehicle(filterCriteriaMap);
		} catch (Exception e) {
			logger.error("Problem while getting Stranded Vehicle ", e);
		}
	}

	/**
	 * 
	 * @param Track Region Performance
	 */
	public static void getTrackRegionPerformance(HashMap<String, String> filterCriteriaMap) {
		try {
			syncObject.getTrackRegionPerformance(filterCriteriaMap);
		} catch (Exception e) {
			logger.error("Problem while getting Track Region Performance ", e);
		}
	}
	
	/**
	 * 
	 * @param Vehicle Outside Gate
	 */
	public static void getVehicleOutsideGate(HashMap<String, String> filterCriteriaMap) {
		try {
			syncObject.getVehicleOutsideGate(filterCriteriaMap);
		} catch (Exception e) {
			logger.error("Problem while getting  Vehicle Outside Gate ", e);
		}
	}
	
	/**
	 * 
	 * @param regionAlert ids
	 */
	public static void refreshRegionAlerts(ArrayList<Integer> alertIds, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("TripProcessorGateway.refreshRegionAlerts() is called From New EJB Method And Instance "+serverName+" #####");
				for (Integer itId : alertIds) {
					TPQueueSender.send(new Triple<Integer, Integer,String> (itId,com.ipssi.reporting.common.util.ApplicationConstants.REGIONALERT,serverName));
				}
				return;
			}else{
			System.out.println("TripProcessorGateway.refreshRegionAlerts() is called From OLD EJB Method");	
			syncObject.refreshAlertCache(alertIds);
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the Region Alert ", e);
		}
	}
	/**
	 * 
	 * @param orgId
	 */
	public static void reloadShiftDef(int orgId, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("TripProcessorGateway.reloadShiftDef() is called From New EJB Method And Instance "+serverName+" #####");
				TPQueueSender.send(new Triple<Integer, Integer, String> (orgId,com.ipssi.reporting.common.util.ApplicationConstants.SHIFT,serverName));
				return;
			}else{
				System.out.println("TripProcessorGateway.reloadShiftDef() is called From OLD EJB Method");
			    syncObject.reloadShiftDef(orgId);
			}
		} catch (Exception e) {
			logger.error("Problem while reload Shift Def ", e);
		}
	}
	/**
	 * 
	 * @param shiftPlanId
	 */
	public static void reloadShiftDetail(int shiftPlanId, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("TripProcessorGateway.reloadShiftDetail() is called From New EJB Method And Instance "+serverName+" #####");
				TPQueueSender.send(new Triple<Integer, Integer, String> (shiftPlanId,com.ipssi.reporting.common.util.ApplicationConstants.SHIFTDETAIL,serverName));
				return;
			}else{
				System.out.println("TripProcessorGateway.reloadShiftDetail() is called From OLD EJB Method");
			    syncObject.reloadShiftDetail(shiftPlanId);
			}
		} catch (Exception e) {
			logger.error("Problem while reload Shift Detail ", e);
		}
	}
	public static void reloadOpStationProfileDetails(ArrayList<Integer> ids, String serverName)  {//for change command = 21 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERreloadOpStationProfileDetails called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,21,serverName));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void reloadOpStationProfileVehicleLinks(ArrayList<Integer> ids, String serverName)  {//for change command = 23 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERreloadOpStationProfileVehicleLinks called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,23,serverName));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void reloadStopParamProfileDetails(ArrayList<Integer> ids, String serverName) {//for change command = 24 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERreloadStopParamProfileDetails called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,24,serverName));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void reloadTripParamVehicleLinks(ArrayList<Integer> ids, String serverName)  {//for change command = 25 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERreloadTripParamVehicleLinks called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,25,serverName));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void tripDBGDump(ArrayList<Integer> ids, String serverName)  {//for change command = 26 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERtripDBGDump called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,26,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void tripDBGAddTrace(ArrayList<Integer> ids, String serverName)  {//for change command = 27 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERtripDBGAddTrace called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,27,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void refreshCurrentCache(ArrayList<Integer> ids, String serverName)  {//for change command = 27 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERTripUpdateCurrentCache called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,31,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void refreshNewMUCache(ArrayList<Integer> ids, String serverName)  {//for change command = 27 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERTripUpdateMUCache called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,34,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void tripDBGRemoveTrace(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERtripDBGRemoveTrace called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,28,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void refreshCache(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERrefreshCache called:");
			TPQueueSender.send(new Triple<Integer, Integer,String> (ids.get(0),29,serverName));			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void redo(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERredo called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,30,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void updateChallan(ArrayList<Integer> ids, String serverName, boolean doDBOnly) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERUpdateChallan called:");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,doDBOnly ? 33 : 32,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void redoWithCache(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERredoWithCache called:");
			System.out.println("[LOCTRACKERredoWithCache called:] first doing cacheBuild");
			TPQueueSender.send(new Triple<Integer, Integer,String> (ids.get(0),29,serverName));
			System.out.println("[LOCTRACKERredoWithCache called:] now doing redo");
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,30,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void reloadGeneral(ArrayList<Integer> ids, int commandCode, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERreloadGeneral called:"+commandCode);
			for (Integer itId : ids) {
				TPQueueSender.send(new Triple<Integer, Integer,String> (itId,commandCode,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
//		refreshVehicles(0);
	}
}

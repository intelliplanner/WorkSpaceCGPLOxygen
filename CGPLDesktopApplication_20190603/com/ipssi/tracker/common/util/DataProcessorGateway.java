package com.ipssi.tracker.common.util;

import java.util.ArrayList;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.ipssi.dataprocessor.ejb.session.DataCacheAccessorRemote;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class DataProcessorGateway {

	private final static Logger logger = Logger.getLogger(DataProcessorGateway.class);
	private static DataCacheAccessorRemote syncObject = null;
	private static boolean nonEjbCall = true; // for message base refresh entities.

	static {
		try {
			InitialContext context = new InitialContext(PropertyManager.getProperties());
			syncObject = (DataCacheAccessorRemote) context.lookup(PropertyManager.getString("data.remote.jndi"));
		} catch (Exception e) {
			logger.error("Problem in looking up ejb ", e);
		}

	}

	
	public static void sendMessage(ArrayList<Integer> vehicleIDs, int cmd) {
		try {
			if(nonEjbCall){
				String serverName = Misc.getServerName();
				System.out.println("DataProcessorGateway.sendMesg is called #####");
				for (Integer itId : vehicleIDs) {
					DPQueueSender.send(new Triple<Integer, Integer, String> (itId,cmd, serverName));
				}
				return;
			}
			
		} catch (Exception e) {
			logger.error("Problem while sendig mesg ", e);
		}
	}

	/**
	 * 
	 * @param vehicle_id
	 */
	public static void refreshVehicles(ArrayList<Integer> vehicleIDs) {
		try {
			if(nonEjbCall){
				System.out.println("DataProcessorGateway.refreshVehicles() is called #####");
				for (Integer itId : vehicleIDs) {
					DPQueueSender.send(new Pair<Integer, Integer> (itId,com.ipssi.reporting.common.util.ApplicationConstants.VEHICLE));
				}
				return;
			}
			System.out.println("DataProcessorGateway.refreshVehicles() is being called %%%%%%");
			syncObject.refreshVehicles(vehicleIDs);
			System.out.println("DataProcessorGateway.refreshVehicles() is called $$$$$$");
		} catch (Exception e) {
			logger.error("Problem while refreshing the vehicle ", e);
		}
	}

	/**
	 * 
	 * @param mapSet id
	 */
	public static void refreshMapSets(ArrayList<Integer> mapSetIDs) {
		try {
			if(nonEjbCall){
				System.out.println("DataProcessorGateway.refreshMapSets() is called #####");
				for (Integer itId : mapSetIDs) {
					DPQueueSender.send(new Pair<Integer, Integer> (itId,com.ipssi.reporting.common.util.ApplicationConstants.IO));
				}
				return;
			}
			syncObject.refreshMapSets(mapSetIDs);
		} catch (Exception e) {
			logger.error("Problem while refreshing the mapSet ", e);
		}
	}
	public static void main(String[] args) {
//		refreshVehicles(0);
	}
}

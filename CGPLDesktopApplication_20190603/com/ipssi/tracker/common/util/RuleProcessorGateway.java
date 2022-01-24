package com.ipssi.tracker.common.util;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.ruleprocessor.ejb.session.RuleCacheAccessorRemote;

public class RuleProcessorGateway {

	private final static Logger logger = Logger.getLogger(RuleProcessorGateway.class);
	private static RuleCacheAccessorRemote syncObject = null;
	private static boolean nonEjbCall = true; // for message base refresh entities.

	static {
		try {
			InitialContext context = new InitialContext(PropertyManager.getProperties());
			syncObject = (RuleCacheAccessorRemote) context.lookup(PropertyManager.getString("rule.remote.jndi"));
		} catch (Exception e) {
			logger.error("Problem in looking up ejb ", e);
		}

	}
	/*	To refresh entity and their type
     * Vehicle 		: 5
     * Rule			: 6
     * Alert		: 7
     * IO			: 8
     * Op station 	: 9
     * Shift		: 10   // need to check
     * 
    */

	/**
	 * 
	 * @param vehicle_id
	 */
	public static void refreshVehicle(List<Integer> vehicle_id_list,String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("RuleProcessorGateway.refreshVehicles() is called From New EJB Method And Instance "+serverName+"#####");
				for (Integer vehicleId : vehicle_id_list) {
					//RPQueueSender.send(new Pair<Integer, Integer> (vehicleId, com.ipssi.reporting.common.util.ApplicationConstants.VEHICLE));
					RPQueueSender.send(new Triple<Integer, Integer,String> (vehicleId, com.ipssi.reporting.common.util.ApplicationConstants.VEHICLE,serverName));
				}
				return;
			}else{
			System.out.println("RuleProcessorGateway.refreshVehicles() is being called From OLD EJB Method %%%%%%");
			syncObject.refreshVehicle(vehicle_id_list);
			System.out.println("RuleProcessorGateway.refreshVehicles() is called $$$$$$");
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the vehicle ", e);
		}
	}

	/**
	 * 
	 * @param vehicle_id
	 */
	public static void refreshRule(int rule_id, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("RuleProcessorGateway.refreshRule()  is called From New EJB Method And Instance "+serverName+"#####");
				//RPQueueSender.send(new Pair<Integer, Integer> (rule_id,com.ipssi.reporting.common.util.ApplicationConstants.RULE));
				RPQueueSender.send(new Triple<Integer, Integer,String> (rule_id,com.ipssi.reporting.common.util.ApplicationConstants.RULE,serverName));
				return;
			}else{
			System.out.println("RuleProcessorGateway.refreshRule() is being called From OLD EJB Method %%%%%%");
			syncObject.refreshRule(rule_id);
			System.out.println("RuleProcessorGateway.refreshRule() is called $$$$$$");
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the rule ", e);
		}
	}

	/**
	 * 
	 * @param rule_set_id
	 */
	public static void refreshRuleSet(int rule_set_id, String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("RuleProcessorGateway.refreshRuleSet()  is called From New EJB Method And Instance "+serverName+"#####");
				//RPQueueSender.send(new Pair<Integer, Integer> (rule_set_id,com.ipssi.reporting.common.util.ApplicationConstants.RULESETS));
				RPQueueSender.send(new Triple<Integer, Integer, String> (rule_set_id,com.ipssi.reporting.common.util.ApplicationConstants.RULESETS,serverName));
				return;
			}else{
			System.out.println("RuleProcessorGateway.refreshRuleSet() is being called From OLD EJB Method %%%%%%");
			syncObject.refreshRuleSet(rule_set_id);
			System.out.println("RuleProcessorGateway.refreshRuleSet() is called $$$$$$");
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the rule set ", e);
		}

	}

	/**
	 * 
	 * @param rule_set_id
	 */
	public static void refreshNotificationSet(int notification_set_id ,String serverName) {
		try {
			if(nonEjbCall){
				System.out.println("RuleProcessorGateway.refreshNotificationSet() is called From New EJB Method And Instance "+serverName+"#####");
		//		RPQueueSender.send(new Pair<Integer, Integer> (notification_set_id,com.ipssi.reporting.common.util.ApplicationConstants.ALERTS));
				RPQueueSender.send(new Triple<Integer, Integer,String> (notification_set_id,com.ipssi.reporting.common.util.ApplicationConstants.ALERTS,serverName));
				return;
			}else{
			System.out.println("RuleProcessorGateway.refreshNotificationSet() is being called From OLD EJB Method %%%%%%");
			syncObject.refreshNotificationSet(notification_set_id);
			System.out.println("RuleProcessorGateway.refreshNotificationSet() is called $$$$$$");
			}
		} catch (Exception e) {
			logger.error("Problem while refreshing the notification set ", e);
		}

	}


	public static void ruleDBGDump(ArrayList<Integer> ids, String serverName)  {//for change command = 26 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERruleDBGDump called:");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,26,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void ruleDBGAddTrace(ArrayList<Integer> ids, String serverName)  {//for change command = 27 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERruleDBGAddTrace called:");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,27,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void ruleDBGRemoveTrace(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERruleDBGRemoveTrace called:");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,28,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void refreshCache(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERruleRefreshCache called:");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,29,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void refreshCurrentCache(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERrefreshCurrentCache called:");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,31,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void redo(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERruleRedo called:");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,30,serverName));
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void redoWithCache(ArrayList<Integer> ids, String serverName) {//for change command = 28 .. change opprofile details
		try {
			System.out.println("[LOCTRACKERruleRedoWithCache called:");
			System.out.println("[LOCTRACKERruleRedoWithCache called:] first doing cacheBuild");
			RPQueueSender.send(new Triple<Integer, Integer,String> (ids.get(0),29,serverName));
			System.out.println("[LOCTRACKERruleRedoWithCache called:] now doing redo");
			for (Integer itId : ids) {
				RPQueueSender.send(new Triple<Integer, Integer,String> (itId,30,serverName));
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	public static void main(String[] args) {
		refreshRule(0,"default");
	}
}

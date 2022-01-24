package com.ipssi.mapping.ejb.gateway;

import java.util.ArrayList;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.ipssi.ejb.session.GpsCacheAccessorRemote;
import com.ipssi.gen.utils.Pair;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.mapguideutils.ReadShapeFile;


public class GpsUtilGateway {

	private final static Logger logger = Logger.getLogger(GpsUtilGateway.class);
	private static GpsCacheAccessorRemote syncObject = null;

	static {
		try {
			Properties env = new Properties();

			env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
			//env.put("java.naming.provider.url", "jnp://localhost:1099");
			env.put("java.naming.provider.url", "jnp://10.1.1.1:1099");
			env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
			env.put("jnp.socket.Factory", "org.jnp.interfaces.TimedSocketFactory");
			InitialContext ctx = new InitialContext(env);
			//TestSessionRemote beanRemote = (TestSessionRemote)ctx.lookup("TestSession/remote");
			GpsCacheAccessorRemote gpsTeanRemote = (GpsCacheAccessorRemote)ctx.lookup("GpsCacheAccessor/remote");
//			gpsTeanRemote.TestDisplay();
			syncObject = (GpsCacheAccessorRemote) ctx.lookup("GpsCacheAccessor/remote");
			
		} catch (Exception e) {
			logger.error("GpsUtilGateway  :  Problem in looking up ejb ", e);
			e.printStackTrace();
		}

	}

	/**
	 * @param vehicle_id
	 */
	public static void refreshVehicles(ArrayList<Integer> vehicleIDs) {
		try {
			System.out.println("GpsUtilGateway.refreshVehicles() is being called %%%%%%");
			syncObject.refreshVehicles(vehicleIDs);
			System.out.println("GpsUtilGateway.refreshVehicles() is called $$$$$$");
		} catch (Exception e) {
			logger.error("GpsUtilGateway  :  Problem while refreshing the vehicle ", e);
			e.printStackTrace();
		}
	}
	/**
	 * @param vehicle_id
	 */
    public static void makePortTreeDirty() throws Exception{
    	try {
    		System.out.println("GpsUtilGateway.makePortTreeDirty() is called. ");
			syncObject.makePortTreeDirty();
		} catch (Exception e) {
			logger.error("GpsUtilGateway  :  Problem while makePortTreeDirty ", e);
			e.printStackTrace();
		}
    }
	/**
	 * @param vehicle_id
	 */
    public static void makeDistCalcControlDirtyForAll(){
    	try {
    		System.out.println("GpsUtilGateway.makeDistCalcControlDirtyForAll() is called. ");
			syncObject.makeDistCalcControlDirtyForAll();
		} catch (Exception e) {
			logger.error("GpsUtilGateway  :  Problem while makeDistCalcControlDirtyForAll ", e);
			e.printStackTrace();
		}
    }
	/**
	 * @param vehicle_id
	 */
    public static void refreshRegionIds(ArrayList<Integer> regionIDs) throws Exception{
    	try {
    		System.out.println("GpsUtilGateway.refreshRegionIds() is called. ");
			syncObject.refreshRegionIds(regionIDs);
		} catch (Exception e) {
			logger.error("GpsUtilGateway  :  Problem while refreshRegionIds ", e);
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * @param vehicle_id
	 */
    public static Pair<ArrayList<Object>, String> calcGetNameHelper(int vehicleId, double latitude, double longitude) throws Exception{
    	Pair<ArrayList<Object>, String> name = null;
    	try {
    		System.out.println("GpsUtilGateway.calcGetNameHelper() is called. ");
    		name = syncObject.calcGetNameHelper(vehicleId, latitude, longitude);
		} catch (Exception e) {
			logger.error("GpsUtilGateway  :  Problem while calcGetNameHelper ", e);
			e.printStackTrace();
		}
		return name;
	}
	public static void main(String[] args) {
//		refreshVehicles(0);
	}
}

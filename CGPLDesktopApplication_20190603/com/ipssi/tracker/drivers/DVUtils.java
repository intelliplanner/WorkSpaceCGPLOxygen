package com.ipssi.tracker.drivers;
import java.util.*;
import java.sql.*;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.SessionManager;

public class DVUtils {
	
    public static StringBuilder getDriverAutoCompleteObj(Connection conn, SessionManager _session, Cache _cache, int pv123, String varName) throws Exception {
    	StringBuilder retval = new StringBuilder();
    	ArrayList<DriverCoreBean> driverList = DriverDetailsDao.getDriverDataByOrg(conn, pv123);
    	int index = 0;
    	StringBuilder temp = new StringBuilder();
    	DimInfo d9003 = DimInfo.getDimInfo(9003);
    	for (DriverCoreBean driver:driverList) {
    		String n = driver.getName();
    		int val = driver.getId();
    		temp.setLength(0);
    		//temp.append(driver.inShift() ? "Y" : "N").append(",");
    		//temp.append(driver.hasSignedIn() ? "Y" : "N").append(",");
    		int orgId = driver.getOrgId();
			DimInfo factor1Dim = null;
			DimInfo factor2Dim = null;
			MiscInner.PortInfo portInfo =  _cache.getPortInfo(orgId, conn);
			ArrayList<Integer> factorDimList =portInfo == null ? null : portInfo.getIntParams(OrgConst.ID_SKILL_FACTOR);
			factor1Dim = factorDimList == null || factorDimList.size() == 0 ? null : DimInfo.getDimInfo(factorDimList.get(0));
			factor2Dim = factorDimList == null || factorDimList.size() == 1 ? null : DimInfo.getDimInfo(factorDimList.get(1)); 
			
    		for (int j=0,js = driver.getDriverSkillsList() == null ? 0 : driver.getDriverSkillsList().size(); j<js;j++) {
    			if (j != 0)
    				temp.append(",");
    			driver.getDriverSkillsList().get(j).addToString(conn, _session, temp, _cache, d9003, factor1Dim, factor2Dim);
    		}
    		helperAddToJscriptVar(retval, varName, index++, n, temp.toString(), val);
    	}
    	return retval;	
    }
    
    public static StringBuilder getVehicleAutoCompleteObj(Connection conn, Cache _cache, int pv123, String varName) throws Exception {
    	StringBuilder retval = new StringBuilder();
    	ArrayList<VehicleInfo> vehicleList = VehicleInfo.getVehicles(conn, pv123);
    	int index = 0;
    	DimInfo detailedStatus = DimInfo.getDimInfo(9083);
    	for (VehicleInfo vehicle:vehicleList) {
    		String n = vehicle.getName();
    		int val = vehicle.getId();
    		helperAddToJscriptVar(retval, varName, index++, n, _cache.getAttribDisplayName(detailedStatus, vehicle.getDetailedStatus()), val);
    	}
    	return retval;
    }
    public static int guessAppropPV123(Cache cache, Connection conn, int pv123, int driverId, int vehicleId) throws Exception {
    	int driverPV123 = Misc.getUndefInt();
    	int vehiclePV123 = Misc.getUndefInt();
    	if (!Misc.isUndef(driverId)) {
    		PreparedStatement ps = conn.prepareStatement("select org_id from driver_details where id = ? and status != 0");
    		ps.setInt(1, driverId);
    		ResultSet rs = ps.executeQuery();
    		if (rs.next())
    			driverPV123 = rs.getInt(1);
    		rs.close();
    		ps.close();
    	}
    	if (!Misc.isUndef(vehicleId)) {
    		PreparedStatement ps = conn.prepareStatement("select customer_id from vehicle where id = ? and status != 0");
    		ps.setInt(1, vehicleId);
    		ResultSet rs = ps.executeQuery();
    		if (rs.next())
    			vehiclePV123 = rs.getInt(1);
    		rs.close();
    		ps.close();
    	}
    	int guessedPV123 = Misc.getUndefInt();
    	if (!Misc.isUndef(vehiclePV123) || !Misc.isUndef(driverPV123)) {
    		if (Misc.isUndef(vehiclePV123))
    			guessedPV123 = driverPV123;
    		else if (Misc.isUndef(driverPV123))
    			guessedPV123 = vehiclePV123;
    		else if (vehiclePV123 == driverPV123)
    				guessedPV123 = vehiclePV123;
    		else {
    			if (cache.isAncestor(conn, vehiclePV123, driverPV123)) 
    				guessedPV123 = vehiclePV123;
    			else if (cache.isAncestor(conn, driverPV123, vehiclePV123))
    				guessedPV123 = driverPV123;
    		}
    	}
    	if (Misc.isUndef(guessedPV123))
    		guessedPV123 = pv123;
    	else if (cache.isAncestor(conn, guessedPV123, pv123)) {
    	//do nothing	guessedPV123 = pv123;
    	}
    	else
    		guessedPV123 = pv123;
    	return guessedPV123;
    }

    public static StringBuilder getVehicleAutoCompleteObjExt(Connection conn, Cache _cache, int pv123, String varName) throws Exception {
    	StringBuilder retval = new StringBuilder();
    	PreparedStatement ps = conn.prepareStatement("select v.id, v.name from vehicle v join port_nodes leaf on (leaf.id = v.customer_id) join port_nodes anc on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number)) where anc.id =? and v.status in (1,2) order by v.name ");
    	ps.setInt(1, pv123);
    	ResultSet rs = ps.executeQuery();
    	int index = 0;
    	while (rs.next()) {
    		String n = rs.getString(2);
    		int val = rs.getInt(1);
    		helperAddToJscriptVar(retval, varName, index++, n, n, val);
    	}
    	rs.close();
    	ps.close();
    	return retval;
    }
    
    public static StringBuilder getDriverAutoCompleteObjExt(Connection conn, Cache _cache, int pv123, String varName) throws Exception {
    	StringBuilder retval = new StringBuilder();
    	PreparedStatement ps = conn.prepareStatement("select v.id, v.driver_name, v.provided_uid, v.driver_uid, v.driver_mobile_one from driver_details v join port_nodes leaf on (leaf.id = v.org_id) join port_nodes anc on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number >= leaf.lhs_number and anc.rhs_number <= leaf.rhs_number)) where anc.id =? and v.status in (1,2) order by v.driver_name ");
    	ps.setInt(1, pv123);
    	ResultSet rs = ps.executeQuery();
    	int index = 0;
    	while (rs.next()) {
    		String n = rs.getString(2);
    		int val = rs.getInt(1);
    		String puid = rs.getString(3);
    		String uid = rs.getString(4);
    		String pm = rs.getString(5);
    		String comboDesc = "["+(pm == null ? "" : pm)+"_"+(puid == null ? "" : puid)+"_"+(uid == null ? "" : uid)+"]";
    		helperAddToJscriptVar(retval, varName, index++, n, comboDesc, val);
    	}
    	rs.close();
    	ps.close();
    	return retval;
    }
    
    
    private static void helperAddToJscriptVar(StringBuilder retval, String varName, int index, String n, String desc, int val) {
    	retval.append(varName).append("[").append(index).append("]").append("=").append("{'n':'").append(n).append("','d':'").append(desc).append("','v':").append(val).append("};");
    }
}

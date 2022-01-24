package com.ipssi.swm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import java.sql.ResultSet;

public class PlanControl {
	private int planGuideline = 1;
	private boolean ignoreVehicleAv = false;
	private boolean ignoreDriverAv = false;
	private ArrayList<Pair<Integer, String>> ignoreDriverList = null;
	private ArrayList<Pair<Integer, String>> ignoreVehicleList = null;
	public PlanControl() {
		
	}
	public PlanControl(int planGuideline, boolean ignoreVehicleAv,
			boolean ignoreDriverAv,
			ArrayList<Pair<Integer, String>> ignoreDriverList,
			ArrayList<Pair<Integer, String>> ignoreVehicleList) {
		super();
		this.planGuideline = planGuideline;
		this.ignoreVehicleAv = ignoreVehicleAv;
		this.ignoreDriverAv = ignoreDriverAv;
		this.ignoreDriverList = ignoreDriverList;
		this.ignoreVehicleList = ignoreVehicleList;
	}
	public void addIgnoreDriver(int id, String name) {
		if (ignoreDriverList == null)
			ignoreDriverList = new ArrayList<Pair<Integer, String>>();
		ignoreDriverList.add(new Pair<Integer, String>(id, name));
	}
	public void addIgnoreVehicle(int id, String name) {
		if (ignoreVehicleList == null)
			ignoreVehicleList = new ArrayList<Pair<Integer, String>>();
		ignoreVehicleList.add(new Pair<Integer, String>(id, name));
	}
	public static PlanControl getPlanControl(Connection conn, int orgId) throws Exception {
		try {
			PlanControl retval = new PlanControl();
			PreparedStatement ps = conn.prepareStatement("select port_node_id, plan_guideline, ignore_vehicle_av, ignore_driver_av from swm_plan_control where port_node_id=?");
			ps.setInt(1, orgId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				retval.setPlanGuideline(Misc.getRsetInt(rs, 2, retval.getPlanGuideline()));
				retval.setIgnoreVehicleAv(Misc.getRsetInt(rs, 3, retval.isIgnoreVehicleAv() ? 1 : 0) != 0);
				retval.setIgnoreDriverAv(Misc.getRsetInt(rs, 4, retval.isIgnoreDriverAv() ? 1 : 0) != 0);
			}
			rs.close();
			ps.close();
			retval.setIgnoreVehicleList(null);
			retval.setIgnoreDriverList(null);
			
			ps = conn.prepareStatement("select l.vehicle_id, vehicle.name from swm_plan_control_veh_ignore l join vehicle on (l.vehicle_id = vehicle.id) where l.port_node_id = ? order by vehicle.name ");
			ps.setInt(1, orgId);
			rs = ps.executeQuery();
			while (rs.next()) {
				int vehId = rs.getInt(1);
				String n = rs.getString(2);
				retval.addIgnoreVehicle(vehId, n);
			}
			rs.close();
			ps.close();
			
			ps = conn.prepareStatement("select l.driver_id, driver_details.driver_name from swm_plan_control_driver_ignore l join driver_details on (l.driver_id = driver_details.id) where l.port_node_id = ? order by driver_details.driver_name ");
			ps.setInt(1, orgId);
			rs = ps.executeQuery();
			while (rs.next()) {
				int vehId = rs.getInt(1);
				String n = rs.getString(2);
				retval.addIgnoreDriver(vehId, n);
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
	public static void savePlanControl(Connection conn, int orgId, PlanControl retval) throws Exception {
		try {
			PreparedStatement ps = null;
			//check if exists ... if it does ... update else insert
			ps = conn.prepareStatement("select port_node_id from swm_plan_control where port_node_id = ?");
			ps.setInt(1, orgId);
			ResultSet rs = ps.executeQuery();
			boolean exists = false;
			if (rs.next())
				exists = true;
			rs.close();
			ps.close();
			
			if (exists) {
				ps = conn.prepareStatement("update swm_plan_control set plan_guideline=?, ignore_vehicle_av=?, ignore_driver_av=? where port_node_id=?");
			}
			else {
				ps = conn.prepareStatement("insert into swm_plan_control (plan_guideline, ignore_vehicle_av, ignore_driver_av, port_node_id) values (?,?,?,?)");
			}
			ps.setInt(1, retval.getPlanGuideline());
			ps.setInt(2, retval.isIgnoreVehicleAv() ? 1 : 0);
			ps.setInt(3, retval.isIgnoreDriverAv() ? 1 : 0);
			ps.setInt(4, orgId);
			ps.execute();
			ps.close();
			
			ps = conn.prepareStatement("delete from swm_plan_control_veh_ignore where port_node_id=?");
			ps.setInt(1, orgId);
			ps.execute();
			ps.close();
			
			ps = conn.prepareStatement("delete from swm_plan_control_driver_ignore where port_node_id=?");
			ps.setInt(1, orgId);
			ps.execute();
			ps.close();
			if (retval.getIgnoreVehicleList() != null) {
				ps = conn.prepareStatement("insert into swm_plan_control_veh_ignore (vehicle_id, port_node_id) values (?,?)");
				for (Pair<Integer, String> entry: retval.getIgnoreVehicleList()) {
					if (entry.first != null && entry.first >= 0) {
						ps.setInt(1, entry.first);
						ps.setInt(2, orgId);
						ps.addBatch();
					}
				}
				ps.executeBatch();
				ps.close();
			}
			
			if (retval.getIgnoreDriverList() != null) {
				ps = conn.prepareStatement("insert into swm_plan_control_driver_ignore (driver_id, port_node_id) values (?,?)");
				for (Pair<Integer, String> entry: retval.getIgnoreDriverList()) {
					if (entry.first != null && entry.first >= 0) {
						ps.setInt(1, entry.first);
						ps.setInt(2, orgId);
						ps.addBatch();
					}
				}
				ps.executeBatch();
				ps.close();
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public int getPlanGuideline() {
		return planGuideline;
	}
	public void setPlanGuideline(int planGuideline) {
		this.planGuideline = planGuideline;
	}
	public boolean isIgnoreVehicleAv() {
		return ignoreVehicleAv;
	}
	public void setIgnoreVehicleAv(boolean ignoreVehicleAv) {
		this.ignoreVehicleAv = ignoreVehicleAv;
	}
	public boolean isIgnoreDriverAv() {
		return ignoreDriverAv;
	}
	public void setIgnoreDriverAv(boolean ignoreDriverAv) {
		this.ignoreDriverAv = ignoreDriverAv;
	}
	public ArrayList<Pair<Integer, String>> getIgnoreDriverList() {
		return ignoreDriverList;
	}
	public void setIgnoreDriverList(
			ArrayList<Pair<Integer, String>> ignoreDriverList) {
		this.ignoreDriverList = ignoreDriverList;
	}
	public ArrayList<Pair<Integer, String>> getIgnoreVehicleList() {
		return ignoreVehicleList;
	}
	public void setIgnoreVehicleList(
			ArrayList<Pair<Integer, String>> ignoreVehicleList) {
		this.ignoreVehicleList = ignoreVehicleList;
	}
}

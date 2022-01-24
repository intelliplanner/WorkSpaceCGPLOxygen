package com.ipssi.tracker.drivers;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.tracker.common.db.DBQueries;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VehicleInfo {
	private int id;
	private String name;
	private int status;
	private int detailedStatus;
	private int portNodeId;
	private int type;
	public VehicleInfo(int id, String name,  int status, int detailedStatus, int portNodeId, int type) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.detailedStatus = detailedStatus;
		this.portNodeId = portNodeId;
		this.type = type;
	}
	public static ArrayList<VehicleInfo> getVehicles(Connection conn, int pv123) throws Exception {
		try {
			ArrayList<VehicleInfo> retval = new ArrayList<VehicleInfo>();
			PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.FETCH_VEHICLE_DETAILS);
			ps.setInt(1, pv123);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				VehicleInfo item = new VehicleInfo(rs.getInt("id"), rs.getString("name"), rs.getInt("status"), rs.getInt("detailed_status"), rs.getInt("customer_id"), rs.getInt("type"));
				retval.add(item);
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getDetailedStatus() {
		return detailedStatus;
	}
	public void setDetailedStatus(int detailedStatus) {
		this.detailedStatus = detailedStatus;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}

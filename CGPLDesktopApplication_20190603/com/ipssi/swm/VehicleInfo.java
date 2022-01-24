package com.ipssi.swm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;

public class VehicleInfo {
    private int vehicleId;
    private int vehicleType;
    private Date lastTrackTime;
    private int installStatus;
    private int opStatus;
    private String vehicleName;
    private boolean isLoader = false;
    private boolean isTipper = false;
    private boolean isDumperPressure = false;
    public String toString() {
    	return vehicleId + "-" + vehicleName;
    }
    public boolean isLoader() {
    	return isLoader;
    }
    public boolean isShovel() {
    	return vehicleType == 1 || vehicleType == 15 || vehicleType == 16;
    }
    public double getCapacityTonne() {
    	return 35;
    }
    public double getCapacityVolume() {
    	return 15;
    }
    public double getLoadTimeMin() {
    	return 3;
    }
    public boolean isTipper() {
    	return isTipper;
    }
    public boolean isDumperPressure() {
    	return isDumperPressure;
    }
    
	public VehicleInfo(int vehicleId, int vehicleType, Date lastTrackTime, int installStatus, int opStatus, String vehicleName) {
		super();
		this.vehicleId = vehicleId;
		this.vehicleType = vehicleType;
		this.lastTrackTime = lastTrackTime;
		this.installStatus = installStatus;
		this.opStatus = opStatus;
		this.setVehicleName(vehicleName);
		DimInfo d9097 = DimInfo.getDimInfo(9097);
		int v = d9097.getAParentVal(9022, vehicleType);
		if (v == 0 || vehicleType == 14 || vehicleType == 15)
			isLoader = true;
		else if (v == 1 && vehicleType != 14)
			isTipper = true;
		else
			isDumperPressure = true;
	}
	
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}
	public Date getLastTrackTime() {
		return lastTrackTime;
	}
	public void setLastTrackTime(Date lastTrackTime) {
		this.lastTrackTime = lastTrackTime;
	}
	public int getInstallStatus() {
		return installStatus;
	}
	public void setInstallStatus(int installStatus) {
		this.installStatus = installStatus;
	}
	public int getOpStatus() {
		return opStatus;
	}
	public void setOpStatus(int opStatus) {
		this.opStatus = opStatus;
	}
    
	public static ArrayList<VehicleInfo> getVehicleInfo(Connection conn, int orgId) throws Exception {
		try {
			ArrayList<VehicleInfo> retval = new ArrayList<VehicleInfo>();
			try {
				PreparedStatement ps = conn.prepareStatement("select vehicle.id, vehicle.type, current_data.gps_record_time, vehicle.status, vehicle.detailed_status, vehicle.name from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join "+
						" port_nodes anc on  (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number and anc.id = ? ) join current_data on (current_data.attribute_id in (0) and current_data.vehicle_id = vehicle.id) where vehicle.status in (1) ");
				ps.setInt(1, orgId);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					VehicleInfo veh = new VehicleInfo(rs.getInt(1), rs.getInt(2), Misc.sqlToUtilDate(rs.getTimestamp(3)), rs.getInt(4), rs.getInt(5), rs.getString(6));
					retval.add(veh);
				}
				rs.close();
				ps.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	
	public String getVehicleName() {
		return vehicleName;
	}
	
}

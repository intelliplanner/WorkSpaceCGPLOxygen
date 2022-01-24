package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;

public class DeviceModel {
	
	private int id;
	private int cummDistProvided; // 0 not there, 1 cumm, 2 delta, 3 canbus
	private int cummDistCalcApproach;// 0 dont. 1 complex, 2 simple
	private int manualAdjTZMin;
	private double cummDataToMeter;
	private double distAdjFactor = 1;
	private static ConcurrentHashMap<Integer, DeviceModel>  g_deviceModels = new ConcurrentHashMap<Integer, DeviceModel>();
	
	public static DeviceModel getDeviceModel(Connection conn, int modelId) {
		DeviceModel retval = null;
		try {
			if (g_deviceModels.size() == 0) {
				loadDeviceModel(conn, Misc.getUndefInt());
			}
			return g_deviceModels.get(modelId);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return retval;
	}
	private static String GET_MODEL_DETAIL = "select id, cumm_dist, use_cumm_dist, manual_adj_tz_min, cumm_data_to_meter, dist_adj_factor from device_model_info";
	public static void loadDeviceModel(Connection conn, int modelId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(modelId < 0 ? GET_MODEL_DETAIL : GET_MODEL_DETAIL+" where id = ?");
			if (modelId >= 0)
				ps.setInt(1, modelId);
			rs = ps.executeQuery();
			while (rs.next()) {
				DeviceModel entry = new DeviceModel();
				entry.id = rs.getInt(1);
				entry.cummDistProvided = Misc.getRsetInt(rs, 2, 0);
				entry.cummDistCalcApproach = Misc.getRsetInt(rs, 3, 0);
				entry.manualAdjTZMin = Misc.getRsetInt(rs, 4, 0);
				entry.cummDataToMeter = Misc.getRsetDouble(rs, 5, 1000);
				entry.distAdjFactor = Misc.getRsetDouble(rs, 6, 1);
				g_deviceModels.put(entry.id, entry);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e2) {
				
			}
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCummDistProvided() {
		return cummDistProvided;
	}
	public void setCummDistProvided(int cummDistProvided) {
		this.cummDistProvided = cummDistProvided;
	}
	public int getCummDistCalcApproach() {
		return cummDistCalcApproach;
	}
	public void setCummDistCalcApproach(int cummDistCalcApproach) {
		this.cummDistCalcApproach = cummDistCalcApproach;
	}
	public int getManualAdjTZMin() {
		return manualAdjTZMin;
	}
	public void setManualAdjTZMin(int manualAdjTZMin) {
		this.manualAdjTZMin = manualAdjTZMin;
	}
	public double getCummDataToMeter() {
		return cummDataToMeter;
	}
	public void setCummDataToMeter(double cummDataToMeter) {
		this.cummDataToMeter = cummDataToMeter;
	}
	public double getDistAdjFactor() {
		return distAdjFactor;
	}
	public void setDistAdjFactor(double distAdjFactor) {
		this.distAdjFactor = distAdjFactor;
	}
}

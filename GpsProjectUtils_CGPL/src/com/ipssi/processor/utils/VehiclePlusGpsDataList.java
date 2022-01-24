package com.ipssi.processor.utils;

import java.io.Serializable;
import java.util.ArrayList;

import com.ipssi.cache.OtherData;
import com.ipssi.gen.utils.Misc;

public class VehiclePlusGpsDataList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int type = Misc.getUndefInt();
	private int vehicleId;
	private ArrayList<GpsData> dataList;
	private ArrayList<CanData> canDataList;
	private ArrayList<OtherData> otherDataList;
	public VehiclePlusGpsDataList() {
		vehicleId = Misc.getUndefInt();
		dataList = null;
		canDataList = null;
		otherDataList = null;
	}
	public VehiclePlusGpsDataList(int vehicleId, ArrayList<GpsData> dataList) {
		this.vehicleId = vehicleId;
		this.dataList = dataList;
	}
	public VehiclePlusGpsDataList(int type, int vehicleId, ArrayList<GpsData> dataList) {
		this.type = type;
		this.vehicleId = vehicleId;
		this.dataList = dataList;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
		public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setDataList(ArrayList<GpsData> dataList) {
		this.dataList = dataList;
	}
	public ArrayList<GpsData> getDataList() {
		return dataList;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(vehicleId);
		for (int i=0,is=dataList == null ? 0 : dataList.size(); i<is; i++) {
			if (i == 0)
				sb.append(dataList.get(0).toString());
			else {
				sb.append("{d").append(dataList.get(i).getDimId()).append(",").append(dataList.get(i).getValue()).append("}");
			}
		}
		return sb.toString();
	}
	public ArrayList<CanData> getCanDataList() {
		return canDataList;
	}
	public void setCanDataList(ArrayList<CanData> canDataList) {
		this.canDataList = canDataList;
	}
	public ArrayList<OtherData> getOtherDataList() {
		return otherDataList;
	}
	public void setOtherDataList(ArrayList<OtherData> otherDataList) {
		this.otherDataList = otherDataList;
	}
	
	

}

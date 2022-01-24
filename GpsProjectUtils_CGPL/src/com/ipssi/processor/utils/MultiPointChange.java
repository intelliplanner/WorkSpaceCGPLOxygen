package com.ipssi.processor.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Pair;

public class MultiPointChange  implements Serializable {
	private static final long serialVersionUID = 1L;
	private int vehicleId;
    private GpsData pointJustAdded;
    private ArrayList<Pair<Date, Double>> valList;
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public GpsData getPointJustAdded() {
		return pointJustAdded;
	}
	public void setPointJustAdded(GpsData pointJustAdded) {
		this.pointJustAdded = pointJustAdded;
	}
	public ArrayList<Pair<Date, Double>> getValList() {
		return valList;
	}
	public void setValList(ArrayList<Pair<Date, Double>> valList) {
		this.valList = valList;
	}
    
    
}

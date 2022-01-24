package com.ipssi.dashboard;

import java.util.Date;

import com.ipssi.tripprocessor.dashboard.bean.VOInterface;

public class TripInfoBean extends VOInterface {
	private static final long serialVersionUID = 1L;
	private int tripId;
	private int vehicleId;
	private String vehicleName;
	private Date waitInLoad;
	private Date gateInLoad;
	private Date areaInLoad;
	private Date areaOutLoad;
	private Date gateOutLoad;
	private Date waitOutLoad;
	private Date waitInUnload;
	private Date gateInUnload;
	private Date areaInUnload;
	private Date areaOutUnload;
	private Date gateOutUnload;
	private Date waitOutUnload;
	private Date shiftDate;
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public Date getWaitInLoad() {
		return waitInLoad;
	}
	public void setWaitInLoad(Date waitInLoad) {
		this.waitInLoad = waitInLoad;
	}
	public Date getGateInLoad() {
		return gateInLoad;
	}
	public void setGateInLoad(Date gateInLoad) {
		this.gateInLoad = gateInLoad;
	}
	public Date getAreaInLoad() {
		return areaInLoad;
	}
	public void setAreaInLoad(Date areaInLoad) {
		this.areaInLoad = areaInLoad;
	}
	public Date getAreaOutLoad() {
		return areaOutLoad;
	}
	public void setAreaOutLoad(Date areaOutLoad) {
		this.areaOutLoad = areaOutLoad;
	}
	public Date getGateOutLoad() {
		return gateOutLoad;
	}
	public void setGateOutLoad(Date gateOutLoad) {
		this.gateOutLoad = gateOutLoad;
	}
	public Date getWaitOutLoad() {
		return waitOutLoad;
	}
	public void setWaitOutLoad(Date waitOutLoad) {
		this.waitOutLoad = waitOutLoad;
	}
	public Date getWaitInUnload() {
		return waitInUnload;
	}
	public void setWaitInUnload(Date waitInUnload) {
		this.waitInUnload = waitInUnload;
	}
	public Date getGateInUnload() {
		return gateInUnload;
	}
	public void setGateInUnload(Date gateInUnload) {
		this.gateInUnload = gateInUnload;
	}
	public Date getAreaInUnload() {
		return areaInUnload;
	}
	public void setAreaInUnload(Date areaInUnload) {
		this.areaInUnload = areaInUnload;
	}
	public Date getAreaOutUnload() {
		return areaOutUnload;
	}
	public void setAreaOutUnload(Date areaOutUnload) {
		this.areaOutUnload = areaOutUnload;
	}
	public Date getGateOutUnload() {
		return gateOutUnload;
	}
	public void setGateOutUnload(Date gateOutUnload) {
		this.gateOutUnload = gateOutUnload;
	}
	public Date getWaitOutUnload() {
		return waitOutUnload;
	}
	public void setWaitOutUnload(Date waitOutUnload) {
		this.waitOutUnload = waitOutUnload;
	}
	public Date getShiftDate() {
		return shiftDate;
	}
	public void setShiftDate(Date shiftDate) {
		this.shiftDate = shiftDate;
	}
	
}

package com.ipssi.cache;

import java.io.Serializable;

import com.ipssi.gen.utils.Misc;

public class OtherData implements Serializable {
	private static final long serialVersionUID = 1L;
	protected int id = Misc.getUndefInt();
	protected int vehicleId = Misc.getUndefInt();
	protected long gpsRecordTime = Misc.getUndefInt();
	protected long gpsRecvTime = Misc.getUndefInt();
	public void setId(int id) {
		this.id = id;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public long getGpsRecordTime() {
		return gpsRecordTime;
	}
	public void setGpsRecordTime(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	public long getGpsRecvTime() {
		return gpsRecvTime;
	}
	public void setGpsRecvTime(long updatedOnTime) {
		this.gpsRecvTime = updatedOnTime;
	}

}

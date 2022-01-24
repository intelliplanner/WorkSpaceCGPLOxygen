package com.ipssi.processor.utils;

import java.io.Serializable;
import java.util.Date;


import com.ipssi.gen.utils.Misc;

public class CanData implements Serializable, Comparable<CanData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int vehId = Misc.getUndefInt();
	private long gpsRecordTime;
	private long canRecvTime;
	private long canRecordTime;
	private int dimId = 0;
	private double value;
	
	public CanData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof CanData) {
			CanData gpsData = (CanData) obj;
			if (gpsData != null) {// && Misc.isEqual(y, gpsData.y) && Misc.isEqual(x, gpsData.x)) {
				return this.gpsRecordTime == gpsData.getGpsRecordTime();
//				if (!isNull(this.gps_Record_Time)) {
//					return this.gps_Record_Time.equals(gpsData.getGps_Record_Time());
//				} else {
//					return this.gps_Record_Time == gpsData.getGps_Record_Time();
//				}
			}
		}
		return false;
	}
	
	public int compareTo(CanData p) {		
		//CanData p = (CanData)obj;
		return this.gpsRecordTime < p.gpsRecordTime ? -1 : this.gpsRecordTime > p.gpsRecordTime ? 1 : 0;		
	}
	
	public String toString() {
		return " [CAN DATA]: "+" VehId: "+ vehId+","+" GpsRecTime: ("+ new Date(this.gpsRecordTime).toString() +  " canRecordTime: " + new Date(this.canRecordTime).toString() +  " recv: " + new Date(this.canRecvTime).toString()
				 +" Dim:("+ dimId+","+value+")";
	}

	public long getGpsRecordTime() {
		return gpsRecordTime;
	}

	public void setGpsRecordTime(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}

	public long getGpsRecvTime() {
		return canRecvTime;
	}

	public void setGpsRecvTime(long canRecvTime) {
		this.canRecvTime = canRecvTime;
	}

	public long getCanRecordTime() {
		return canRecordTime;
	}

	public void setCanRecordTime(long canRecordTime) {
		this.canRecordTime = canRecordTime;
	}

	public int getDimId() {
		return dimId;
	}

	public void setDimId(int dimId) {
		this.dimId = dimId;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getVehId() {
		return vehId;
	}

	public void setVehId(int vehId) {
		this.vehId = vehId;
	}

	public long getCanRecvTime() {
		return canRecvTime;
	}

	public void setCanRecvTime(long canRecvTime) {
		this.canRecvTime = canRecvTime;
	}
	

}

package com.ipssi.cache;

import java.io.Serializable;

import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;

public class CanDataPoint  implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	private double dval;
	private String sval;
	private long gpsRecordTime;
	public int compareTo(Object obj) {		
		CanDataPoint p = (CanDataPoint)obj;
		return this.gpsRecordTime < p.gpsRecordTime ? -1 : this.gpsRecordTime > p.gpsRecordTime ? 1 : 0;		
	}
	public int valCompare(Object obj) {
		CanDataPoint p = (CanDataPoint)obj;
		if (p.sval != null)
			return -1*p.sval.compareTo(this.sval);
		else if (this.sval != null)
			return sval.compareTo(p.sval);
		else if (Misc.isEqual(this.dval, p.dval))
			return 0;
		else
			return dval-p.dval < 0 ? -1 : 1;
	}
	public double getDval() {
		return dval;
	}
	public void setDval(double dval) {
		this.dval = dval;
	}
	public String getSval() {
		return sval;
	}
	public void setSval(String sval) {
		this.sval = sval;
	}
	public long getGpsRecordTime() {
		return gpsRecordTime;
	}
	public void setGpsRecordTime(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}

}

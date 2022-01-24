package com.ipssi.modeler;

import java.io.Serializable;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;

public  class ModelState implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	private double x1 = Misc.getUndefDouble();
	public transient boolean  hasReset = false;
	public ModelState() {
	}
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	public String toString() {
		return Double.toString(x1);
	}
	
	public void setX1(double x1) {
		if (x1 < 0 || x1 > 60) {
			int dbg = 1;
			dbg++;
		}
		this.x1 = x1;
	}
	public double getX1() {
		return x1;
	}
}

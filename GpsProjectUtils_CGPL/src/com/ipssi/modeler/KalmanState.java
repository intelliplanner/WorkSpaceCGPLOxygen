package com.ipssi.modeler;

import java.io.Serializable;
import java.util.Date;

import com.ipssi.gen.utils.Misc;

public class KalmanState extends ModelState  implements Serializable {
	private static final long serialVersionUID = 1L;
	private double x2 = Misc.getUndefDouble();
	private double p11 = Misc.getUndefDouble();
	private double p12 = Misc.getUndefDouble();
	private double p22 = Misc.getUndefDouble();
	private double smoothX2 = Misc.getUndefDouble();
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public double getX2() {
		return x2;
	}
	public void setX2(double x2) {
		this.x2 = x2;
	}
	public double getP11() {
		return p11;
	}
	public void setP11(double p11) {
		this.p11 = p11;
	}
	public double getP12() {
		return p12;
	}
	public void setP12(double p12) {
		this.p12 = p12;
	}
	public double getP22() {
		return p22;
	}
	public void setP22(double p22) {
		this.p22 = p22;
	}
	public void setSmoothX2(double smoothX2) {
		this.smoothX2 = smoothX2;
	}
	public double getSmoothX2() {
		return smoothX2;
	}
	
	public String toString() {
		StringBuilder retval = new StringBuilder();
		retval.append(getX1()).append(",").append(x2).append(",").append(smoothX2).append(",").append(p11).append(",").append(p12).append(",").append(p22);
		return retval.toString();
	}
}

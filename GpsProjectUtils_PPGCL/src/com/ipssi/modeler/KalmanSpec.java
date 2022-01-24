package com.ipssi.modeler;

import java.util.Date;
import java.util.Map;

import org.w3c.dom.Element;

import com.ipssi.gen.utils.Misc;
import com.ipssi.modeler.ModelSpec;
import com.ipssi.processor.utils.GpsData;

public class KalmanSpec extends ModelSpec {
    private double qf = 0.0001;
    private double r = 20;
    private double p11 = 1000;
    private double p12 = 0;
    private double p22 = 1000;
    private double speedExp = 0.1;
    public static double defaultX2 = -0.2;
    
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    
    public KalmanSpec() {
    	super();
    	this.modelType = ModelSpec.KALMAN_FLOW;
    }
    public double getQf() {
		return qf;
	}
	public void setQf(double qf) {
		this.qf = qf;
	}
	public double getR() {
		return r;
	}
	public void setR(double r) {
		this.r = r;
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
	public double getSpeedExp() {
		return speedExp;
	}
	public void setSpeedExp(double speedExp) {
		this.speedExp = speedExp;
	}
	
	public void readModelSpecific(Element elem) { //must be similar to updateWithDynParamModelSpecific
		qf = Misc.getParamAsDouble(elem.getAttribute("qf"), qf);
		r = Misc.getParamAsDouble(elem.getAttribute("r"), r);
		p11 = Misc.getParamAsDouble(elem.getAttribute("p11"), p11);
		p12 = Misc.getParamAsDouble(elem.getAttribute("p12"), p12);
		p22 = Misc.getParamAsDouble(elem.getAttribute("p22"), p22);
		speedExp = Misc.getParamAsDouble(elem.getAttribute("speed_exp"), speedExp);
	}
	
	public void updateWithDynParamModelSpecific(Map<String, Double> params) {
		qf = getDoubleDynParam(params, "qf", qf);
		r = getDoubleDynParam(params, "r", r);
		p11 = getDoubleDynParam(params, "p11", p11);
		p12 = getDoubleDynParam(params, "p12", p12);
		p22 = getDoubleDynParam(params, "p22", p22);
		speedExp = getDoubleDynParam(params, "speed_exp", speedExp);
	}
	
	 public void copyFromSpecific(ModelSpec rhs) {
	    	this.qf = ((KalmanSpec) rhs).qf;
	    	this.r = ((KalmanSpec) rhs).r;
	    	this.p11 = ((KalmanSpec) rhs).p11;
	    	this.p12 = ((KalmanSpec) rhs).p12;
	    	this.p22 = ((KalmanSpec) rhs).p22;
	    	this.speedExp = ((KalmanSpec) rhs).speedExp;
	    }
	
	public void reinit(double v, ModelState retvalGeneric, VehicleSpecific vehicleParam) {
		v = getAppropValAdjForIgnore(v, vehicleParam);
		KalmanState retval = (KalmanState) retvalGeneric;
		retval.setX1(v);
		retval.setP11(p11);
		retval.setP12(p12);
		retval.setP22(p22);
		if (Misc.isUndef(retval.getX2())) {
			retval.setX2(defaultX2);		
		    retval.setSmoothX2(retval.getX2());
		}
		retval.hasReset = true;
	}
	public KalmanState init( double v, VehicleSpecific vehicleParam) {
		KalmanState retval = new KalmanState();
		reinit(v, retval, vehicleParam);
		return retval;
	}
	
	 public  KalmanState next(ModelState curr, double v, double delta, VehicleSpecific vehicleParam) {
		 KalmanState retval = null;
		 double scale = vehicleParam == null ? 1 : vehicleParam.getScale();
		 if (curr == null)
			 return init(v, vehicleParam);
		 try {
			 retval = (KalmanState) ((KalmanState) curr).clone();
		 }
		 catch (Exception e) {
			 retval = new KalmanState();
		 }
		 retval.hasReset = false;
		 double y_t = v;
		//c => xt|t, c_1 => xt|t-1
		 double x1_t_c = retval.getX1();
		 double x2_t_c = retval.getX2();
		 double p11_t_c = retval.getP11();
		 double p12_t_c = retval.getP12();
		 double p22_t_c = retval.getP22();
		 
         if (this.toIgnore(y_t, vehicleParam))
        	 return retval;
         double absdelta = Math.abs(y_t - x1_t_c);
         if (!Misc.isEqual(x1_t_c,  0.0)) {
        	 absdelta = Math.abs(absdelta/x1_t_c);
         }
         if (absdelta > this.resetIfValChangeRel) {
        	 //check if prev is also above ... then we reset
        	 reinit(v, retval, vehicleParam);
        	 return retval;
         }
        	 
         
		 
		 
		 double deltaSqr = delta*delta;
		 
		 double x1_t_c_1 = x1_t_c+delta*x2_t_c;
		 double x2_t_c_1 = x2_t_c;
		 double p11_t_c_1 = p11_t_c + 2*p12_t_c*delta + p22_t_c * 	deltaSqr + deltaSqr*delta*qf/3.0;
		 double p12_t_c_1 = p12_t_c + p22_t_c * delta + deltaSqr * qf/2.0;
		 double p22_t_c_1 = p22_t_c + delta * qf;
		 double k1 = p11_t_c_1/(p11_t_c_1+r*scale);
		 double k2 = p12_t_c_1/(p11_t_c_1+r*scale);
		 
		 x1_t_c = x1_t_c_1 + k1*(y_t-x1_t_c_1);
		 x2_t_c = x2_t_c_1 + k2*(y_t-x1_t_c_1);
		 p11_t_c = p11_t_c_1*(1-k1);
		 p12_t_c = p12_t_c_1*(1-k1);
		 p22_t_c = p22_t_c_1-k2*p12_t_c_1;
		 System.out.println(y_t +"," + delta+ ","+x1_t_c_1+ ","+x2_t_c_1+","+p11_t_c_1+","+p12_t_c_1+","+p22_t_c_1+","+k1+","+k2+","+x1_t_c+ ","+x2_t_c+","+p11_t_c+","+p12_t_c+","+p22_t_c);
		 
		 double smoothX2 = x2_t_c*speedExp+(1-speedExp)*retval.getSmoothX2();
		 x1_t_c = super.getAppropValAdjForIgnore(x1_t_c, vehicleParam);
		 retval.setX1(x1_t_c);
		 retval.setX2(x2_t_c);
		 retval.setP11(p11_t_c);
		 retval.setP12(p12_t_c);
		 retval.setP22(p22_t_c);
		 retval.setSmoothX2(smoothX2);		 
		 return retval;
	 }
	 
	 public double predict(double prevVal, ModelState refstate, double delta) {
		 KalmanState ksp = (KalmanState) refstate;
		 double rate = ksp != null ? ksp.getSmoothX2() : defaultX2;
		 double retval = prevVal + rate*delta;
		 return retval;
	 }
}

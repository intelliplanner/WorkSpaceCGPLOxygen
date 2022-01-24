package com.ipssi.modeler;

import java.util.Date;
import java.util.Map;

import org.w3c.dom.Element;

import com.ipssi.gen.utils.Misc;
import com.ipssi.processor.utils.GpsData;

public class ExpSpec extends ModelSpec {
	public static double defaultExp = 0.2;
    public double exp = defaultExp;
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    public ExpSpec() {
    	super();
    	this.modelType = 0;
    }
    
    public void readModelSpecific(Element elem) {//must be similar to updateWithDynParamModelSpecific
    	exp = Misc.getParamAsDouble(elem.getAttribute("exp"), exp);
    }
    
    public void updateWithDynParamModelSpecific(Map<String, Double> params) { //must be similar to readModelSpecific
    	exp = getDoubleDynParam(params, "exp", exp);
    }
    
    public void copyFromSpecific(ModelSpec rhs) {
    	this.exp = ((ExpSpec) rhs).exp;
    }
    
	public double getExp() {
		return exp;
	}

	public void setExp(double exp) {
		this.exp = exp;
	}
    
	public void reinit(double v, ModelState retvalGeneric, VehicleSpecific vehicleParam) {
		v = this.getAppropValAdjForIgnore(v, vehicleParam);
		ExpState retval = (ExpState) retvalGeneric;
		retval.setX1(v);
		retval.hasReset = true;
		return;
	}
	public ExpState init(double v, VehicleSpecific vehicleParam) {
		ExpState retval = new ExpState();
		reinit(v, retval, vehicleParam);
		return retval;
	}
	
	 public  ExpState next(ModelState curr, double v, double delta, VehicleSpecific vehicleParam)  {
		 ExpState retval = null;
		 if (curr == null) {
			 return init(v, vehicleParam);
		 }
		 try {
			 retval = (ExpState) ((ExpState) curr).clone();
		 }
		 catch (Exception e) {
			 retval = new ExpState();
		 }
		 retval.hasReset = false;
		  if (this.toIgnore(v, vehicleParam))
	        	 return retval;
		 retval.setX1(exp*v+(1-exp)*retval.getX1());
		 return retval;
	 }
	 
	 public double predict(double prevVal, ModelState refstate, double delta) {
		 ExpState ksp = (ExpState) refstate;
		 double rate = refstate != null ? exp : defaultExp;
		 double retval =rate*delta+(1-rate)*delta;
		 return retval;
	 }
}

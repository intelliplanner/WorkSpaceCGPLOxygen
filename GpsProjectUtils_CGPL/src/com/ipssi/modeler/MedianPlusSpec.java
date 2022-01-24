package com.ipssi.modeler;

import java.util.Map;

import org.w3c.dom.Element;

import com.ipssi.gen.utils.Misc;

public class MedianPlusSpec  extends ModelSpec {
	 private int medianWindow = Misc.getUndefInt();
	 private int maxMedianWindow = Misc.getUndefInt();
	 private double maxMedianDistKM = Misc.getUndefDouble();
	 private int pastLookForPredict = 21;   
	 public Object clone() throws CloneNotSupportedException {
	    return super.clone();
	 }
	    
	 public MedianPlusSpec() {
	    	super();
	    	this.modelType = ModelSpec.MEDIAN_PLUS;
	 }
		
	public void readModelSpecific(Element elem) { //must be similar to updateWithDynParamModelSpecific
		medianWindow = Misc.getParamAsInt(elem.getAttribute("median_window"), medianWindow);
		maxMedianWindow = Misc.getParamAsInt(elem.getAttribute("max_median_window"),maxMedianWindow);
		maxMedianDistKM = Misc.getParamAsDouble(elem.getAttribute("max_median_dist_km"), maxMedianDistKM);
		if (!Misc.isUndef(medianWindow))
			VehicleModelInfo.g_medianWindow = medianWindow;
		if (!Misc.isUndef(maxMedianWindow))
			VehicleModelInfo.g_maxMedianWindow = maxMedianWindow;
		if (!Misc.isUndef(maxMedianDistKM))
			VehicleModelInfo.g_maxMedianDistanceKM = maxMedianDistKM;
		
		VehicleModelInfo.g_changeJumpValueIfAllChanges = true;//medianWindow < 15;
		pastLookForPredict = Misc.getParamAsInt(elem.getAttribute("past_look_predict"), pastLookForPredict);
	}
		
	public void updateWithDynParamModelSpecific(Map<String, Double> params) {
		medianWindow = getIntDynParam(params, "median_window", medianWindow);
		maxMedianWindow = getIntDynParam(params, "max_median_window",maxMedianWindow);
		maxMedianDistKM = getDoubleDynParam(params, "max_median_dist_km", maxMedianDistKM);
		
		pastLookForPredict = getIntDynParam(params, "past_look_predict", pastLookForPredict);
	}
		
	public void copyFromSpecific(ModelSpec rhs) {
		 this.medianWindow = ((MedianPlusSpec) rhs).medianWindow;
		 this.maxMedianWindow = ((MedianPlusSpec) rhs).maxMedianWindow;
		 this.maxMedianDistKM = ((MedianPlusSpec) rhs).maxMedianDistKM;
		 
		 
		 this.pastLookForPredict = ((MedianPlusSpec) rhs).pastLookForPredict;
	}
		
	public void reinit(double v, ModelState retvalGeneric, VehicleSpecific vehicleParam) {
			v = getAppropValAdjForIgnore(v, vehicleParam);
			MedianState retval = (MedianState) retvalGeneric;
			retval.setX1(v);
			retval.hasReset = true;
	}
	public MedianState init( double v, VehicleSpecific vehicleParam) {
		MedianState retval = new MedianState();
		reinit(v, retval, vehicleParam);
		return retval;
	}
		
	public  MedianState next(ModelState curr, double v, double delta, VehicleSpecific vehicleParam) {
		MedianState retval = null;
		double scale = vehicleParam == null ? 1 : vehicleParam.getScale();
		if (curr == null)
			 return init(v, vehicleParam);
		 try {
			 retval = (MedianState) ((MedianState) curr).clone();
		}
		catch (Exception e) {
			 retval = new MedianState();
		}
		retval.setX1(v);
		retval.hasReset = false;
		return retval;
	}
		 
	public double predict(double prevVal, ModelState refstate, double delta) {
		MedianState ksp = (MedianState) refstate;
		double retval = prevVal;
		return retval;
	}

	public void setMedianWindow(int medianWindow) {
		this.medianWindow = medianWindow;
	}

	public int getMedianWindow() {
		return medianWindow;
	}
	
	public int getPastLookForPredict() {
		return pastLookForPredict;
	}

	public int getMaxMedianWindow() {
		return maxMedianWindow;
	}

	public void setMaxMedianWindow(int maxMedianWindow) {
		this.maxMedianWindow = maxMedianWindow;
	}

	public double getMaxMedianDistKM() {
		return maxMedianDistKM;
	}

	public void setMaxMedianDistKM(double maxMedianDistKM) {
		this.maxMedianDistKM = maxMedianDistKM;
	}

}

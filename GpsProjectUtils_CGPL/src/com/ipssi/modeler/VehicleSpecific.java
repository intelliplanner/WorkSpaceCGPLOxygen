package com.ipssi.modeler;

import com.ipssi.gen.utils.Misc;

public class VehicleSpecific {
    private double min = 0;
    private double max = 100;
    private double scale = 1;
    private boolean valid = true;
    public VehicleSpecific() {
    	
    }
	public void setMin(double min) {
		this.min = min;
		scale = (max-min)/100;
		valid = (!Misc.isUndef(min) && !Misc.isUndef(max) && !Misc.isEqual(min, 0) && !Misc.isEqual(max, 0));
	}
	public double getMin() {
		return min;
	}
	public void setMax(double max) {
		this.max = max;
		scale = (max-min)/100;
		valid = (!Misc.isUndef(min) && !Misc.isUndef(max) && !Misc.isEqual(min, 0) && !Misc.isEqual(max, 0));
	}
	public double getMax() {
		return max;
	}
	public double getScale() {
		return scale;
	}
	
	public boolean isValid() {
		return valid;
	}
}

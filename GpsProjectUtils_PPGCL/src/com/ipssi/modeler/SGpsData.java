package com.ipssi.modeler;

import java.util.ArrayList;
import java.util.Date;

import com.ipssi.processor.utils.GpsData;

public class SGpsData extends GpsData {
	public boolean isTrue = false;
	public boolean m_isArtificial = false;
	public SGpsData(GpsData data) {
		super(data);
	}
	
	public SGpsData(GpsData data, boolean isTrue) {
		super(data);
		this.isTrue = isTrue;
	}
	
	public boolean isArtificial() {
		return m_isArtificial;
	}
	
	public boolean setArtificial() { //true if becomes dirty
		if (!m_isArtificial) {
//	        Date newGpsRecTime = getGps_Record_Time();
//	        newGpsRecTime.setTime(newGpsRecTime.getTime()+1);
//	        this.setGps_Record_Time(newGpsRecTime);
	        this.setGps_Record_Time(getGps_Record_Time()+1);
	        m_isArtificial = true;
	        return true;
		}
		return false;
	}
	
	public boolean merge(SGpsData other, boolean mergingRightToLeft) { //return true if dirty
    	boolean retval = false;
    	if (other != null) {
    		if (isTrue() != other.isTrue())
    			retval = true;
    		this.isTrue = other.isTrue();
    	}
    	return retval;
    }
	
	public boolean isTrue() {
		return isTrue;
	}

}

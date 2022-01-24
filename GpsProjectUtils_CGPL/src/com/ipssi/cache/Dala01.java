package com.ipssi.cache;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import com.ipssi.common.ds.trip.LUSequence;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.processor.utils.GpsData;

public class Dala01 implements Serializable, Comparable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public long tstart;
    public double distMarkerAtStart;
    public int secEnd;
    public double distKM;
    private boolean meDistInvalid = false;
    private boolean meDistInvalidBecausePastInvalid = false;
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
    	sb.append("St:").append(tstart <= 0 ? "null": sdf.format(new java.util.Date(tstart))).append(" SecEnd:").append(secEnd).append(" Dist:").append(distKM).append(" V:").append(meDistInvalid).append(",").append(this.meDistInvalidBecausePastInvalid);
    	return sb.toString();
    }
    public Dala01(long ts) {
    	this.tstart = ts;
    }
    public int compareTo(Object obj) {		
		Dala01 p = (Dala01) obj;
		return this.tstart < p.tstart ? -1 : this.tstart > p.tstart ? 1 : 0;		
	}
    public static boolean isValid(long st, long en, double d, double thresh) {
    	int secGap = (int)((en-st)/1000);
    	return secGap >= LUSequence.g_minDalaUpSec 
    	//&& secGap <= LUSequence.g_maxDalaUpSec 
    	&& d < (thresh+0.0001)
    	//&& !this.meDistInvalidBecausePastInvalid
    	;
    }
    public boolean isValid() {
    	return this.secEnd >= LUSequence.g_minDalaUpSec
    	//&& this.secEnd <= LUSequence.g_maxDalaUpSec 
    	&& !meDistInvalid
    //	&& !this.meDistInvalidBecausePastInvalid
    	;
    }
    public boolean isRestLike() {
    	return this.secEnd >= LUSequence.g_maxDalaUpSec && !meDistInvalid && !this.meDistInvalidBecausePastInvalid;
    }
	public boolean isMeDistInvalidBecausePastInvalid() {
		return meDistInvalidBecausePastInvalid;
	}
	public void setMeDistInvalidBecausePastInvalid(
			boolean meDistInvalidBecausePastInvalid) {
		this.meDistInvalidBecausePastInvalid = meDistInvalidBecausePastInvalid;
	}
	public boolean isMeDistInvalid() {
		return meDistInvalid;
	}
	public void setMeDistInvalid(double thresh) {
		this.meDistInvalid = this.distKM >= thresh;
	}
	
}

package com.ipssi.modeler;

import org.w3c.dom.Element;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;
import java.util.*;
import org.w3c.dom.Node;

public abstract class ModelSpec implements Cloneable {

	public static final int EXP_SMOOTH = 0;
	public static final int KALMAN_FLOW = 1;
	public static final int MEDIAN_PLUS = 2;
	public int modelType = KALMAN_FLOW;
	public int forAttribId = Misc.getUndefInt();
	public boolean deltaByTime = false;
	public int deltaDimId  = -1;
	public boolean doAdjustByBattLevel = true; //not used by Modelere but by core Data Processor read logic
    public double ignoreBelowAbs = Misc.getUndefDouble();
    public double ignoreAboveAbs = Misc.getUndefDouble();
    public double ignoreBelowRel  = Misc.getUndefDouble();
    public double ignoreAboveRel = Misc.getUndefDouble();
    public double resetIfValChangeRel = Misc.getUndefDouble();
    public double resetIfValChangeAbs = Misc.getUndefDouble();
    public double resetIfGapExceedsSecs = 12*60*60;
    public double refueIfChangeExceeds = 20;
	public double tolForparamIsSame = 1;
	public int gapBeforeSyncAdvisable = 30;
	public int ptsToLookBackForReset = 10;
	public int ptsToLookBackForLevelChange = 150;
	public int stopDurExceedsSec =90;//was 3*60
	public double checkIfResidueExceeds = 5;
	public int posNegForward = 10;// 20;
	public double posNegThresh = 6.5;//5.0;
	public double posNegPropExceedingThresh = 0.3;//to avoid outliers ..
	public double kmpl  = 15;
	public double lowVarianceThreshold = 2.1;

	//public ArrayList<ArrayList<Double>> levelThreshold = new ArrayList<ArrayList<Double>>();
	//public ArrayList<Integer> levelNBuckets = new ArrayList<Integer>();
	//public ArrayList<Double> levelResBuckets = new ArrayList<Double>();
	/*
	public void readLevelRules(Element levelRule) {//assumes in order of "n", "res" and no "res" missing!!
		int prevNum = Misc.getUndefInt();
		ArrayList<Double> nentry = null;
		for (Node n = levelRule == null ? null : levelRule.getFirstChild(); n != null ; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int num = Misc.getParamAsInt(e.getAttribute("n"));
			double res = Misc.getParamAsDouble(e.getAttribute("res"));
			double propPlus = Misc.getParamAsDouble(e.getAttribute("prop_plus"));
			double propNeg = Misc.getParamAsDouble(e.getAttribute("prop_neg"));
			if (prevNum != num) {
				nentry = new ArrayList<Double>();
				levelNBuckets.add(num);
				levelThreshold.add(nentry);
			}
			int resIndex = 0;
			for (int is=levelResBuckets.size();resIndex<is;resIndex++) {
				if (Misc.isEqual(levelResBuckets.get(resIndex), res)) {
					break;
				}
			}
			if (resIndex == levelResBuckets.size()) {
				levelResBuckets.add(res);
				levelResBuckets.add(-1*res);
			}
			nentry.add(propPlus);
			nentry.add(propNeg);
			prevNum = num;
		}
	}
	
	public ArrayList<ArrayList<Pair<Integer, Integer>>> initLevelThresholdResultHolder(ArrayList<ArrayList<Pair<Integer, Integer>>> retval) {
		if (retval == null) {
			int sz1 = levelResBuckets.size();
			int sz2 = levelResBuckets.size();
			retval = new ArrayList<ArrayList<Pair<Integer, Integer>>>(sz1);
			
			for (int i=0;i<sz1;i++) {
				ArrayList<Pair<Integer, Integer>> temp = new ArrayList<Pair<Integer, Integer>>(sz2);
				for (int j=0;j<sz2;j++) {
					temp.add(new Pair<Integer, Integer>(0,0));
				}
			}
		}
		else {
			for (int i=0, sz1 = retval.size();i<sz1;i++) {
				ArrayList<Pair<Integer, Integer>> temp = retval.get(i);
				for (int j=0, sz2 = temp.size();j<sz2;j++) {
					Pair<Integer, Integer> c= temp.get(j);
					c.first = 0;
					c.second = 0;
				}
			}
		}
		return retval;
	}
	*/
    public abstract void readModelSpecific(Element elem);
    public abstract void updateWithDynParamModelSpecific(Map<String, Double> params);
    public abstract void copyFromSpecific(ModelSpec rhs);
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    
	public static ArrayList<ModelSpec> readModels(Element el) { //whenever new param added here .... need to be updated to updateWithDynParam
		ArrayList<ModelSpec> retvalList = new ArrayList<ModelSpec>();
		for (Node n =  el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element elem = (Element) n;
			if ("1".equals(elem.getAttribute("is_inactive")))
				continue;
			String forAttrib = elem.getAttribute("for_attrib");
			if (forAttrib == null || forAttrib.length() == 0)
				return retvalList;
			ArrayList<Integer> idList = new ArrayList<Integer> ();
			Misc.convertValToVector(forAttrib, idList);
			for (int i=0,is=idList.size(); i<is; i++) {
				int modelType = Misc.getParamAsInt(elem.getAttribute("type"), KALMAN_FLOW);
				ModelSpec retval = null;
				if (modelType == KALMAN_FLOW)
					retval = new KalmanSpec();
				else if (modelType == EXP_SMOOTH)
					retval = new ExpSpec();
				else if (modelType == MEDIAN_PLUS)
					retval = new MedianPlusSpec();
				retvalList.add(retval);
				retval.forAttribId = idList.get(i);
				retval.deltaByTime = "1".equals(elem.getAttribute("delta_by_time"));
				retval.deltaDimId = Misc.getParamAsInt(elem.getAttribute("delta_by_id"));
				retval.doAdjustByBattLevel = "1".equals(elem.getAttribute("adjust_by_battery_level"));
				retval.ignoreAboveAbs = Misc.getParamAsDouble(elem.getAttribute("ignore_above_abs"), Misc.LARGE_NUMBER);
				retval.ignoreBelowAbs = Misc.getParamAsDouble(elem.getAttribute("ignore_below_abs"), -1*Misc.LARGE_NUMBER);
				retval.ignoreAboveRel = Misc.getParamAsDouble(elem.getAttribute("ignore_above_rel"), Misc.LARGE_NUMBER);
				retval.ignoreBelowRel = Misc.getParamAsDouble(elem.getAttribute("ignore_below_rel"), -1*Misc.LARGE_NUMBER);
				retval.resetIfValChangeRel = Misc.getParamAsDouble(elem.getAttribute("reset_if_change_by_rel"), Misc.LARGE_NUMBER);
				retval.resetIfValChangeAbs = Misc.getParamAsDouble(elem.getAttribute("reset_if_change_by_abs"), Misc.LARGE_NUMBER);
				retval.tolForparamIsSame = Misc.getParamAsDouble(elem.getAttribute("tol_param_same"), retval.tolForparamIsSame);
				retval.gapBeforeSyncAdvisable = Misc.getParamAsInt(elem.getAttribute("gap_before_sync"), retval.gapBeforeSyncAdvisable);
				retval.ptsToLookBackForReset = Misc.getParamAsInt(elem.getAttribute("pts_reset"), retval.ptsToLookBackForReset);
				retval.ptsToLookBackForLevelChange = Misc.getParamAsInt(elem.getAttribute("pts_level_change"), retval.ptsToLookBackForLevelChange);
				retval.resetIfGapExceedsSecs = Misc.getParamAsDouble(elem.getAttribute("reset_gap_exceeds_sec"), retval.resetIfGapExceedsSecs);
				retval.refueIfChangeExceeds = Misc.getParamAsDouble(elem.getAttribute("is_refuel_if_change_exceeds"), retval.refueIfChangeExceeds);
				retval.stopDurExceedsSec = Misc.getParamAsInt(elem.getAttribute("stop_dur_exceeds_sec"), retval.stopDurExceedsSec);
				retval.checkIfResidueExceeds = Misc.getParamAsDouble(elem.getAttribute("residue_exceeds"), retval.checkIfResidueExceeds);
				retval.posNegForward = Misc.getParamAsInt(elem.getAttribute("pos_neg_forward"), retval.posNegForward);
				retval.posNegThresh = Misc.getParamAsDouble(elem.getAttribute("pos_neg_thresh"), retval.posNegThresh);
				retval.posNegPropExceedingThresh = Misc.getParamAsDouble(elem.getAttribute("pos_neg_prop_exceed_thresh"), retval.posNegPropExceedingThresh);
				retval.lowVarianceThreshold = Misc.getParamAsDouble(elem.getAttribute("low_variance_thresh"), retval.lowVarianceThreshold);
				retval.kmpl = Misc.getParamAsDouble(elem.getAttribute("kmpl"), retval.kmpl);
				
				
				retval.readModelSpecific(elem);
//				Element levelRules = MyXMLHelper.getChildElementByTagName(elem, "level_rules");
//				retval.readLevelRules(levelRules);
			}
		}
		return retvalList;
	}
	
	public void updateWithDynParams(Map<String, Double> params) {//must be similar to readModel
		deltaByTime = getBooleanDynParam(params, "delta_by_time", deltaByTime);
		deltaDimId = getIntDynParam(params, "delta_by_id", deltaDimId);
		doAdjustByBattLevel = getBooleanDynParam(params, "adjust_by_battery_level", doAdjustByBattLevel);
		ignoreAboveAbs = getDoubleDynParam(params, "ignore_above_abs", ignoreAboveAbs);
		ignoreBelowAbs = getDoubleDynParam(params, "ignore_below_abs", ignoreBelowAbs);
		ignoreAboveRel = getDoubleDynParam(params, "ignore_above_rel", ignoreAboveRel);
		ignoreBelowRel = getDoubleDynParam(params, "ignore_below_rel", ignoreBelowRel);
		resetIfValChangeRel = getDoubleDynParam(params, "reset_if_change_by_rel", resetIfValChangeRel);
		resetIfValChangeAbs = getDoubleDynParam(params, "reset_if_change_by_abs", resetIfValChangeAbs);
		this.tolForparamIsSame = getDoubleDynParam(params, "tol_param_same", this.tolForparamIsSame);
		this.gapBeforeSyncAdvisable = getIntDynParam(params, "gap_before_sync", this.gapBeforeSyncAdvisable);
		this.ptsToLookBackForReset = getIntDynParam(params, "pts_reset", this.ptsToLookBackForReset);
		this.ptsToLookBackForLevelChange = getIntDynParam(params, "pts_level_change", this.ptsToLookBackForLevelChange);
		this.resetIfGapExceedsSecs = getDoubleDynParam(params, "reset_gap_exceeds_sec", this.resetIfGapExceedsSecs);
		this.refueIfChangeExceeds= getDoubleDynParam(params,"is_refuel_if_change_exceeds", this.refueIfChangeExceeds);
		this.stopDurExceedsSec = getIntDynParam(params, "stop_dur_exceeds_sec", this.stopDurExceedsSec);
		this.checkIfResidueExceeds = getDoubleDynParam(params,"residue_exceeds", this.checkIfResidueExceeds);
		this.posNegForward = getIntDynParam(params,"pos_neg_forward", this.posNegForward);
		this.posNegThresh = getDoubleDynParam(params, "pos_neg_thresh", this.posNegThresh);
		this.posNegPropExceedingThresh = getDoubleDynParam(params,"pos_neg_prop_exceed_thresh", this.posNegPropExceedingThresh);
		this.lowVarianceThreshold = getDoubleDynParam(params,"low_variance_thresh", this.lowVarianceThreshold);
		this.kmpl = getDoubleDynParam(params,"kmpl", this.kmpl);
		updateWithDynParamModelSpecific(params);
	}
	
	public void copyFrom(ModelSpec rhs) {
		deltaByTime = rhs.deltaByTime;
		deltaDimId = rhs.deltaDimId;
		doAdjustByBattLevel = rhs.doAdjustByBattLevel;
		ignoreAboveAbs = rhs.ignoreAboveAbs;
		ignoreBelowAbs = rhs.ignoreBelowAbs;
		ignoreAboveRel = rhs.ignoreAboveRel;
		ignoreBelowRel = rhs.ignoreBelowRel;
		resetIfValChangeRel = rhs.resetIfValChangeRel;
		resetIfValChangeAbs = rhs.resetIfValChangeAbs;
		tolForparamIsSame = rhs.tolForparamIsSame;
		gapBeforeSyncAdvisable = rhs.gapBeforeSyncAdvisable;
		ptsToLookBackForReset = rhs.ptsToLookBackForReset;
		ptsToLookBackForLevelChange = rhs.ptsToLookBackForLevelChange;
		resetIfGapExceedsSecs = rhs.resetIfGapExceedsSecs;
		stopDurExceedsSec = rhs.stopDurExceedsSec;
		checkIfResidueExceeds = rhs.checkIfResidueExceeds;
		posNegForward = rhs.posNegForward;
		posNegThresh = rhs.posNegThresh;
		this.posNegPropExceedingThresh = rhs.posNegPropExceedingThresh;
		this.lowVarianceThreshold = rhs.lowVarianceThreshold;
		this.kmpl  = rhs.kmpl;
		copyFromSpecific(rhs);
	}
	
	protected boolean getBooleanDynParam(Map<String, Double> params, String paramName, boolean def) {
		Double vo = params.get(paramName);
		if (vo == null)
			return def;
		double v = vo.doubleValue();
		if (Misc.isUndef(v))
			return def;
		return v > 0.5;
	}
	
	protected double getDoubleDynParam(Map<String, Double> params, String paramName, double def) {
		Double vo = params.get(paramName);
		if (vo == null)
			return def;
		double v = vo.doubleValue();
		if (Misc.isUndef(v))
			return def;
		return v;
	}

	protected int getIntDynParam(Map<String, Double> params, String paramName, int def) {
		Double vo = params.get(paramName);
		if (vo == null)
			return def;
		double v = vo.doubleValue();
		if (Misc.isUndef(v))
			return def;
		return (int) Math.round(v);
	
	}
	
	 public  abstract ModelState init(double v, VehicleSpecific vehicleParam);
	 public abstract void reinit(double v, ModelState retvalGeneric, VehicleSpecific vehicleParam);
     public  abstract ModelState next(ModelState curr, double v, double delta, VehicleSpecific vehicleParam);
     public abstract double predict(double prevVal, ModelState refstate, double delta);
     public boolean isDurExceedForCheck(long gapSec, VehicleSpecific vehicleParam) {
    	 return gapSec > stopDurExceedsSec;
     }
     public boolean isValExccedForCheck(double valAtBeg, double valAtEnd, VehicleSpecific vehicleSpecificParam) {
    	 double scale = vehicleSpecificParam== null ? 1 : vehicleSpecificParam.getScale();
    	 return Math.abs(valAtEnd - valAtBeg) >= this.checkIfResidueExceeds*scale;
     }
     
     public boolean mayConsiderAsRefuelling(double v, VehicleSpecific vehicleSpecificParam) {
    	 double scale = vehicleSpecificParam== null ? 1 : vehicleSpecificParam.getScale();
    	 return v >= this.refueIfChangeExceeds*scale;
     }
     public boolean toIgnore(double v, VehicleSpecific vehicleParam) {
    	 double ignoreBelowAbs = this.ignoreBelowAbs;
    	 double ignoreAboveAbs = this.ignoreAboveAbs;
    	 
    	 if (vehicleParam != null) {
    		 ignoreBelowAbs = vehicleParam.getMin();
    		 ignoreAboveAbs = vehicleParam.getMax();
    	 }
    	  return (v < ignoreBelowAbs || v  > ignoreAboveAbs);
     }
     
     public double getAppropValAdjForIgnore(double v, VehicleSpecific vehicleParam) {
    	 double ignoreBelowAbs = this.ignoreBelowAbs;
    	 double ignoreAboveAbs = this.ignoreAboveAbs;
    	 
    	 if (vehicleParam != null) {
    		 ignoreBelowAbs = vehicleParam.getMin();
    		 ignoreAboveAbs = vehicleParam.getMax();
    	 }
    	 return v < ignoreBelowAbs ? ignoreBelowAbs : v > ignoreAboveAbs ? ignoreAboveAbs : v;
     }
     public boolean isInLowvariance(double v1, double v2, VehicleSpecific vehicleSpecific) {
    	 double diff = Math.abs(v1-v2);
    	 return diff < (vehicleSpecific == null ? 1 : vehicleSpecific.getScale())*this.lowVarianceThreshold;
 	}
     
     public boolean mayNeedReset(double smoothVal, double rawVal, VehicleSpecific vehicleParam) {
    	 double diff =Math.abs(smoothVal-rawVal);
    	 boolean retval = false; 
    	 double resetIfValChangeAbs = this.resetIfValChangeAbs;
    	 if (vehicleParam != null)
    		 resetIfValChangeAbs = resetIfValChangeAbs * (vehicleParam.getMax() - vehicleParam.getMin())/100;
    	 retval = diff > resetIfValChangeAbs;
    	 if (!retval && !Misc.isEqual(diff,0) && !Misc.isEqual(smoothVal, 0) && !Misc.isEqual(rawVal, 0)) {
			 double perc = Math.abs(diff/smoothVal);
			 retval = perc > resetIfValChangeRel;
    	 }
    	 return retval;
     }
     
     public boolean resetBecauseOfGap(Date prev, Date curr) {
    	 return prev != null && curr != null && (curr.getTime() - prev.getTime())/1000 > this.resetIfGapExceedsSecs;
     }
     public boolean resetBecauseOfGap(long prev, long curr) {
    	 return !Misc.isUndef(prev) && !Misc.isUndef(curr) && (curr - prev)/1000 > this.resetIfGapExceedsSecs;
     }
     
     public double getDelta(GpsData prevData, GpsData currData, GpsData prevDeltaData, GpsData currDeltaData, boolean deltaDimCumm) {
    	double delta = Misc.getUndefDouble();
		if (deltaByTime) {
			delta = prevData == null ? 0 : (currData.getGps_Record_Time() - prevData.getGps_Record_Time())/1000;
		}
		else if (this.deltaDimId >= 0) {
			if (prevDeltaData != null && currDeltaData != null) {
				double v1 = currDeltaData.getValue();
				double v2 = prevDeltaData.getValue();
				delta = deltaDimCumm ? v2-v1 : v1;
			}
			if (Misc.isUndef(delta)) {
				if (deltaDimId == 0 && prevData != null && currData != null) {//hack ... for testing as well as fall back
					delta = currData.fastGeoDistance(prevData.getLongitude(), prevData.getLatitude());;
				}
			}
		}
		if (Misc.isUndef(delta))
			delta = 0;
		return delta;
	}
     
     public double getResetAbs() {
    	 return this.resetIfValChangeAbs;
     }
    
}

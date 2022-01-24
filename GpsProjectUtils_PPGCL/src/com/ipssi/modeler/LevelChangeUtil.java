package com.ipssi.modeler;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

//DEBUG13 import com.ipssi.cache.NewVehicleData;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.TrackMisc;
import com.ipssi.geometry.Point;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.Dimension;
import com.ipssi.processor.utils.DimensionInfo;
import com.ipssi.processor.utils.GpsData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class LevelChangeUtil {
	public static boolean g_mergeStop = true;
	private volatile static ArrayList<Pair<Integer, Integer>> dimToRuleMap = null;
	private volatile static int lastDimForRuleId = Misc.getUndefInt(); //if asked dim == lastDimForRuleId, then return lastRuleforDimId .. and vice versa when asked for rule
	private volatile static int lastRuleForDimId = Misc.getUndefInt();
	public static int getLevelRuleIdForDim(int dimId) {
		if (lastDimForRuleId == dimId)
			return lastRuleForDimId;
		for (Pair<Integer, Integer> entry:dimToRuleMap) {
			if (entry.first == dimId) {
				lastDimForRuleId = entry.first;
				lastRuleForDimId = entry.second;
				return lastRuleForDimId;
			}
		}
		return Misc.getUndefInt();
	}
	public static int getDimIdForLevelRule(int ruleId) {
		if (lastRuleForDimId == ruleId)
			return lastDimForRuleId;
		for (Pair<Integer, Integer> entry:dimToRuleMap) {
			if (entry.second == ruleId) {
				lastDimForRuleId = entry.first;
				lastRuleForDimId = entry.second;
				return lastDimForRuleId;
			}
		}
		return Misc.getUndefInt();
	}
	
	public static ArrayList<Pair<Integer, Integer>> getLevelRuleForDim(Connection conn) throws Exception {
		try {
			if (dimToRuleMap != null)
				return dimToRuleMap;
			if (conn == null)
				return null;
			ArrayList<Pair<Integer, Integer>> retval = new ArrayList<Pair<Integer, Integer>>();
			StringBuilder q = new StringBuilder("select rules.id, param_0 from rules join conditions_clauses on (rules.id = conditions_clauses.rule_id and rules.status in (1) and conditions_clauses.rule_type_id in (11) and param_0_type=2) order by rules.id desc  ");
			PreparedStatement ps = conn.prepareStatement(q.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int rid = rs.getInt(1);
				int did = rs.getInt(2);
				//add if it does not exist already
				boolean exists = false;
				for (int i=0,is = retval.size(); i<is; i++) {
					if (retval.get(i).first == did) {
						exists = true;
						break;
					}
				}
				if (!exists)
					retval.add(new Pair<Integer, Integer>(did, rid));
			}
			rs.close();
			ps.close();
			//populate it in temp table so that queries can be done properly
			ps = conn.prepareStatement("delete from temp_model_level_rule_dimid");
			ps.execute();
			ps.close();
			ps = conn.prepareStatement("insert into temp_model_level_rule_dimid (rule_id, dim_id) values (?,?)");
			for (int i=0, is=retval.size(); i<is ;i++) {
				ps.setInt(1, retval.get(i).second);
				ps.setInt(2, retval.get(i).first);
				ps.addBatch();
			}
		
			ps.executeBatch();
			ps.close();
			dimToRuleMap = retval;
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	/*CENT_CACHE
	public static double getSlope(Connection conn, NewVehicleData dataList, int fromPos, MedianPlusSpec modelSpec,NewVehicleData deltaDimDataList, boolean deltaDimCumm) {
		int lookBack = modelSpec.getPastLookForPredict();
		double a = 0;
		//double b = dataList.get(fromPos).getValue();
		//1st look back until reset is seen and there are enough points
		int start = fromPos;
		int endExcl = fromPos;
		int count = 0;
		boolean toreset = false;
		for (int i=fromPos; i>=0;i--) {
			GpsData data = dataList.get(i);
			if (data == null)
				break;
			if (toreset) {
				count = 0;
				start = i;
				endExcl = i;
			}
			if (data.getModelState(modelSpec) != null && data.getModelState(modelSpec).hasReset) {
				toreset = true;
			}
			else {
				endExcl--;
				count++;
			}
			if (count > lookBack) {
				break;
			}
		}
		if (start != endExcl) {
			double sy = 0;
			double sx = 0;
			double sxy = 0;
			double sx2 = 0;
			count = 0;
			GpsData pd = dataList.get(endExcl);
			GpsData pDelta = deltaDimDataList.get(pd);
			double cummDelta = 0;
			
			for (int i=endExcl+1;i<=start;i++) {
				count++;
				GpsData d = dataList.get(i);
				GpsData deltaGD = deltaDimDataList.get(d);
				double delta = modelSpec.getDelta(pd, d, pDelta, deltaGD, deltaDimCumm); 
				double y = d.getValue();
				cummDelta += delta;
				sy += y;
				sx += cummDelta;
				sxy += y*cummDelta;
				sx2 += cummDelta*cummDelta;
				
			}
			a = (sy*sx2 - sx*sxy)/(count*sx2 - sx*sx);
			//b = (count * sxy - sx*sy)/(count * sx2 - sx*sx);
		}
		return a;
		
	}
	*/
	/*CENT_CACHE_TODO_LATER
	public static void nonMedianDetectLevelChange(Connection conn, VehicleModelInfo vehicleInfo, int dimId,  int startPoint, int endPointIncl, boolean force) throws Exception {
		int medianWindow = vehicleInfo.m_medianWindow;
		Dimension dim = Dimension.getDimInfo(dimId);
		if (dim == null || dim.getModelSpec() == null)
			return;
		int ruleForDim = getLevelRuleIdForDim(dimId);
		if (Misc.isUndef(ruleForDim))
			return;
		NewVehicleData dataList = vehicleInfo.getDataList(conn, dimId);
		ModelSpec modelSpec = vehicleInfo.getModelSpec(conn, dimId);
		
		if (dataList == null || modelSpec == null)
			return;
		//if (modelSpec.levelThreshold == null || modelSpec.levelThreshold.size() == 0)//UNCOMMENT_FOR_PREV_APPROACH
		//	continue;
		GpsData data = dataList.get(conn, startPoint);
		LevelChangeList levelChangeList = vehicleInfo.getLevelChangeList(dimId);
		if (levelChangeList == null) {
			levelChangeList = vehicleInfo.addLevelChangeList(dimId);
		}
		if (!force && levelChangeList.ptsSeenSinceLastCheck < levelChangeList.g_checkAfterNPoints) {
			levelChangeList.ptsSeenSinceLastCheck++;
			return;
		}
		
		NewVehicleData deltaDataList = modelSpec.deltaByTime || modelSpec.deltaDimId < 0 ? null : vehicleInfo.getDataList(conn, modelSpec.deltaDimId);
		VehicleSpecific vehicleSpecificParam = vehicleInfo.getVehicleParam(conn, dimId);
		double threshold = 0.050; 
		CacheTrack.VehicleSetup vehSetupInfo = CacheTrack.VehicleSetup.getSetup(vehicleInfo.getVehicleId(), conn);
		CacheTrack.VehicleSetup.DistCalcControl distCalcControl = vehSetupInfo == null ? null : vehSetupInfo.getDistCalcControl(conn);
		if (distCalcControl != null && !Misc.isUndef(distCalcControl.m_distThresholdForStopped))
				threshold = distCalcControl.m_distThresholdForStopped;
		Dimension deltaDim = Dimension.getDimInfo(modelSpec.deltaDimId);
		boolean isCumm = deltaDim != null && deltaDim.isCummulative();
		
		double scale = vehicleSpecificParam == null ? 1 : vehicleSpecificParam.getScale();
		levelChangeList.ptsSeenSinceLastCheck++;
		int start = startPoint -  modelSpec.posNegForward;//modelSpec.ptsToLookBackForLevelChange;
		if (dataList.prevDCPos != -1 && start > dataList.prevDCPos) {
			start = dataList.prevDCPos;
		}
		//first go back ... until a stop beg is seen
		GpsData begD1 = dataList.get(start);
		GpsData begDeltaD1 = deltaDataList == null ? null : deltaDataList.get(begD1);
		for(int i=start-1;i >= 0; i--) {
			GpsData currD1 = dataList.get(i);
			GpsData currDeltaD1 = deltaDataList == null ? null : deltaDataList.get(currD1);
			
			double delta = modelSpec.getDelta(begD1, currD1, begDeltaD1, currDeltaD1, isCumm);
			if (delta > threshold) {
				break;
			}
			else {
				start = i;
			}
		}
		//Now move start to last event so that we don;t create dupli events
		if (start >= 0) {
			GpsData td1 = dataList.get(start);
			LevelChangeEvent lindex = levelChangeList.get(new LevelChangeEvent(td1, null));
			if (lindex != null) {
				start = dataList.indexOf(lindex).first;
			}
		}
		if (start < 0)
			start = 0;
		
		//int start = index.first - modelSpec.ptsToLookBackForLevelChange;
		//if (start < 0)
		//	start = 0;
		int end = endPointIncl;
		
		//local vars for loop
		begD1 = dataList.get(start);//presumed beg of start or the prev pt
		begDeltaD1 = deltaDataList == null ? null : deltaDataList.get(begD1);
		int indexOfStart = start;
		boolean seenStop = false;
		Pair<Integer, Integer> dirtyIndicator = new Pair<Integer, Integer>(-1,-1);
		boolean seenViolation = false;
		GpsData prevViolationPt = null;
		double prevViolationAmtAbs = 0;
		double prevViolationAmt = 0;
		boolean doingMedian = modelSpec.modelType == ModelSpec.MEDIAN_PLUS;
		for (int i=start+1;i<=end;i++) {
			GpsData currData = dataList.get(i);
			

			
			GpsData currDeltaData = deltaDataList == null ? null : deltaDataList.get(currData);
			double delta = modelSpec.getDelta(begD1, currData, begDeltaD1, currDeltaData, isCumm);
			//hardcoded for dist dim ...
			if (delta < threshold) {
				if (!seenStop) {
					seenStop = true; 
				}
			}//vehicle is stopped
			else {
				
				if (seenStop) {
					seenStop = false;
					long durGap = (currData.getGps_Record_Time() - begD1.getGps_Record_Time())/1000;
					if (modelSpec.isDurExceedForCheck(durGap, vehicleSpecificParam)) {
						dbgStartStopMarker(vehicleInfo.getVehicleId(), begD1, currData, conn);
						boolean gapExceeded = true;
						if (doingMedian) {
							GpsData currPlusWindow = dataList.get(i+medianWindow-1);
							if (currPlusWindow != null) {
								modelSpec.isValExccedForCheck(begD1.getValue(), currPlusWindow.getValue(), vehicleSpecificParam);
							}
							else {
								gapExceeded = false;
							}
						}
						else {
							//gapExceeded = modelSpec.isValExccedForCheck(begD1.getDimensionInfo.getValue(), curr.getSpeed(), vehicleSpecificParam)
						}
						if  (gapExceeded) {//modelSpec.isValExccedForCheck(valAtStopBeg, curr.getSpeed(), vehicleSpecificParam)) {
							int sz = i+modelSpec.posNegForward+(doingMedian ? medianWindow-1 : 0);
							if (sz > dataList.size()) {
								break; 
							}
							double pos = 0;
							double neg = 0;
							int countExceedThresh = 0;
							int countBelowThresh = 0;
							GpsData tprev = begD1;
							GpsData tprevDelta = begDeltaD1;
							GpsData ptAtEnd = null;
							if (doingMedian) {
								//1. get slope 
								//2. use slope/dist to get predicted value
								double slope = getSlope(dataList, indexOfStart, (MedianPlusSpec) modelSpec, deltaDataList, isCumm);
								double prevVal = begD1.getValue();
								for (int j=i+medianWindow;j<sz;j++) {
									GpsData tcurr = dataList.get(j);
									GpsData tcurrDelta = deltaDataList == null ? null : deltaDataList.get(tcurr);
									double tdelta = modelSpec.getDelta(tprev, tcurr, tprevDelta, tcurrDelta, isCumm);
									
									double predicted = prevVal + slope * tdelta;
									double residue = tcurr.getSpeed() - predicted;
									residue /= scale;
									if (residue > 0) {
										pos += residue;
										if (residue > modelSpec.posNegThresh)
											countExceedThresh++;
									}
									else {
										neg -= residue; //effectively adding the sum of the abs to neg
										if (residue < -1*modelSpec.posNegThresh)
											countBelowThresh++;
									}
									tprevDelta = tcurrDelta;
									tprev = tcurr;
									prevVal = predicted;
								}
							}
							else {
								
								ModelState refstate = begD1.getModelState();
								double predicted = tprev.getValue();
								double predictedFirst = predicted;
							    
								for (int j=i;j<sz;j++) {
									GpsData tcurr = dataList.get(j);
									if (tcurr != null)
										ptAtEnd = tcurr;
									GpsData tcurrDelta = deltaDataList == null ? null : deltaDataList.get(tcurr);
									double tdelta = modelSpec.getDelta(tprev, tcurr, tprevDelta, tcurrDelta, isCumm);
									tprevDelta = tcurrDelta;
									tprev = tcurr;
									predicted = modelSpec.predict(predicted, refstate, tdelta);
									if (j == i)
										predictedFirst = predicted;
									double residue = tcurr.getSpeed() - predicted;
									residue /= scale;
									if (residue > 0) {
										pos += residue;
										if (residue > modelSpec.posNegThresh)
											countExceedThresh++;
									}
									else {
										neg -= residue; //effectively adding the sum of the abs to neg
										if (residue < -1*modelSpec.posNegThresh)
											countBelowThresh++;
									}
								}// looked forwar to see residue and do a statistical rethink
							}
							double gapRaw = ( (pos-neg)/((double)modelSpec.posNegForward));
							double gap = Math.abs(gapRaw);
							boolean thresholdViolated = gap > modelSpec.posNegThresh;
							if (thresholdViolated) {
								if (gapRaw > 0) {
									if (((double)countExceedThresh/(double)modelSpec.posNegForward) < modelSpec.posNegPropExceedingThresh)
										thresholdViolated = false;
								}
								else {
									if (((double)countBelowThresh/(double)modelSpec.posNegForward) < modelSpec.posNegPropExceedingThresh)
										thresholdViolated = false;
								}
							}
							//to prevent duplicate events ... we continue ignoring consecutive violations
							if (thresholdViolated) {
								if (prevViolationPt != null) {
									boolean toremovePrev = gap >= prevViolationAmtAbs;
									if (toremovePrev) {
										helperMarkEventForRemove(levelChangeList, prevViolationPt, dirtyIndicator);
										helperAddToEventList(vehicleInfo, begD1, gapRaw*scale, dimId, dirtyIndicator, ptAtEnd, begD1);
										prevViolationPt = begD1;
										prevViolationAmtAbs = gap;
										prevViolationAmt = gapRaw;
									}
									else {
										helperMarkEventForRemove(levelChangeList, begD1, dirtyIndicator);
									}
								}//there existed prev pt
								else {
									helperAddToEventList(vehicleInfo, begD1, gapRaw*scale, dimId, dirtyIndicator, ptAtEnd, begD1);//gap instead of  currData.getSpeed() - predictedFirst
									prevViolationPt = begD1;
									prevViolationAmtAbs = gap;
								}//no prev pt exist
							}//if there was a violation
							else {
								if (prevViolationPt == null || !prevViolationPt.equals(begD1)) {
									helperMarkEventForRemove(levelChangeList, begD1, dirtyIndicator);
								}
								prevViolationPt = null;
								prevViolationAmtAbs = 0;
							}//if there was no violation
						}//the values differ enough to cause check for level change
					}//stoppage dur exceeds threshold
				}//resumption from stop
				begD1 = currData;
				indexOfStart = i;
				begDeltaD1 = currDeltaData;
			}//moving
		}//for each point (start to end) examined
		if (dirtyIndicator.first != -1) {
			vehicleInfo.getLevelChangeList(dimId).save(conn, dirtyIndicator,  vehicleInfo.getVehicleId(), dimId, ruleForDim, dataList, vehicleInfo);
		}
	}
	CENT_CACHE */
	public static int getStopEnd(int startIndex, SSpList specialList, int gapThresh) { //see getStartBef and getNextStopStart
		SGpsData refStart = specialList.get(startIndex);
		if (refStart == null)
			return startIndex;
		for (int i=startIndex, is = specialList.size(); i<is;i += 2) {
			SGpsData next = specialList.get(i+1);
			SGpsData nextNext = specialList.get(i+2);
			if (g_mergeStop && next != null && nextNext != null && (nextNext.getGps_Record_Time()-next.getGps_Record_Time()) < 10000 ) { //MAKE 10000 parameterizable
				continue;
			}
			return i+1;
		}
		return specialList.size();
	}
	public static int getNextStopStart(int startIndex, SSpList specialList, int gapThresh) { //see getStartBef and getNextStopStart
		SGpsData refStart = specialList.get(startIndex);
		if (refStart == null)
			return specialList.size();
		refStart = null;
		for (int i=startIndex, is = specialList.size(); i<is;i += 2) {
			SGpsData curr = specialList.get(i);
			if (!curr.isTrue()) {
				i++;
				continue;
			}
			//now positioned at true
			if (refStart == null)
				refStart = curr;
			SGpsData next = specialList.get(i+1);
			if (next == null)
				break;
			SGpsData nextNext = specialList.get(i+2);
			if (g_mergeStop && next != null && nextNext != null && (nextNext.getGps_Record_Time()-next.getGps_Record_Time()) < 10000 ) { //Make it parameterizable
				continue;
			}
			if ((next.getGps_Record_Time()-refStart.getGps_Record_Time())/1000 > gapThresh)
				return i;
			refStart = null;
		}
		return specialList.size();
	}

	public static int getStartBefIndex(GpsData refData, SSpList specialList, int gapThresh) {//overlapped with getStartBef ... see getStopEnd, getNextStopStart
		SGpsData lookup = new SGpsData(refData);
		int retvalIndex = (int) specialList.indexOf(lookup).first;
		SGpsData retval = specialList.get(retvalIndex);
		int indexRelLookup = 0;
		if (retval != null) {
			if (specialList.isAtEnd(lookup)) {
				//check if TT or FF sequence
				SGpsData prev = specialList.get(lookup, -1);
				if (prev != null) {
					if (prev.isTrue()) {
						retval = prev;
						indexRelLookup = -1;
					}
					else { //either FT or FF sequence prev is the F
						SGpsData prevPrev = specialList.get(lookup, -2);
						indexRelLookup = -2;
						retval = prevPrev; 
					}
				}
				else {
					retval = null;
				}
			}
			else if (!retval.isTrue()){
				retval = specialList.get(lookup, -1);
				indexRelLookup = -1;
			}
		}
		
        GpsData refStopEnd = null;
        GpsData prevStopStart = null;
       
		for (;retval != null; indexRelLookup -= 2, retval = specialList.get(lookup, indexRelLookup)) {
			SGpsData next = specialList.get(lookup, indexRelLookup+1);
			
			//retval is beg of stop, next is stop end
			if (refStopEnd == null) {
				refStopEnd = next;
			}
			if (g_mergeStop) {//peek previous and see if the end is same time as this ... in that case take the start of previous
				SGpsData prev = specialList.get(lookup,indexRelLookup-1);
				if (prev != null && retval.getGps_Record_Time()-prev.getGps_Record_Time() < 10000) { //Make it parameterizable
					continue;
				}
			}
			int gap = refStopEnd != null ? (int)(refStopEnd.getGps_Record_Time() - retval.getGps_Record_Time())/1000 : 0;
			if (gap >= gapThresh)
				return retvalIndex+indexRelLookup; //new Pair<GpsData, GpsData>(retval, refStopEnd);
			refStopEnd = null;
		}
		return -1;
	}
	
	public static Pair<GpsData, GpsData> getStartBef(GpsData refData, SSpList specialList, int gapThresh) {//overlapped with getStartBefIndex
		SGpsData lookup = new SGpsData(refData);
		SGpsData retval = specialList.get(lookup);
		int indexRelLookup = 0;
		if (retval != null) {
			if (specialList.isAtEnd(lookup)) {
				//check if TT or FF sequence
				SGpsData prev = specialList.get(lookup, -1);
				if (prev != null) {
					if (prev.isTrue()) {
						retval = prev;
						indexRelLookup = -1;
					}
					else { //either FT or FF sequence prev is the F
						SGpsData prevPrev = specialList.get(lookup, -2);
						indexRelLookup = -2;
						retval = prevPrev; 
					}
				}
				else {
					retval = null;
				}
			}
			else if (!retval.isTrue()){
				retval = specialList.get(lookup, -1);
				indexRelLookup = -1;
			}
		}
		
        GpsData refStopEnd = null;
        GpsData prevStopStart = null;
       
		for (;retval != null; indexRelLookup -= 2, retval = specialList.get(lookup, indexRelLookup)) {
			SGpsData next = specialList.get(lookup, indexRelLookup+1);
			
			//retval is beg of stop, next is stop end
			if (refStopEnd == null) {
				refStopEnd = next;
			}
			if (g_mergeStop) {//peek previous and see if the end is same time as this ... in that case take the start of previous
				SGpsData prev = specialList.get(lookup,indexRelLookup-1);
				if (prev != null && retval.getGps_Record_Time()-prev.getGps_Record_Time() < 10000) { //make it parameterizable
					continue;
				}
			}
			int gap = refStopEnd != null ? (int)(refStopEnd.getGps_Record_Time() - retval.getGps_Record_Time())/1000 : 0;
			if (gap >= gapThresh)
				return new Pair<GpsData, GpsData>(retval, refStopEnd);
			refStopEnd = null;
		}
		return null;
	}
	
	
	
	public static long dgbdt1 = (new java.util.Date(113,2,22,7,28,0)).getTime();
	public static long dgbdt2 = (new java.util.Date(113,2,22,7,29,0)).getTime();
	/*DEBUG13
	public static Pair<GpsData, GpsData>getPointAtDist(Connection conn, GpsData refFgps, GpsData refDgps, boolean doLHS, double distKM, int minAllowed, int maxAllowed, NewVehicleData fuelData, NewVehicleData distData) {
		
		//returns the fuel.list, dist.list index st point is at least distKM away for refDistIndex... ret val is st is of fueldata, 2nd is of dist
		int index = 0;
		 if (refDgps == null)
			 return new Pair<GpsData, GpsData>(refFgps, refDgps);
		 int dir = doLHS ? -1 : 1;
		 GpsData distGps = refDgps;
		while (true) {		
			GpsData dgps = distData.get(conn, refDgps, (index+1)*dir);
			if (dgps == null)
				break;
			distGps = dgps;
			double d = Math.abs(dgps.getValue()-refDgps.getValue());
			if (d > distKM)
				break;
			index++;
			if (index >= maxAllowed)
					break;
		}
		if (index < minAllowed) {
			GpsData dgps = distData.get(conn, refDgps, minAllowed*dir);
			if (dgps != null)
				distGps = dgps;
		}
		GpsData fuelGps = fuelData.get(conn, distGps);
		if (fuelGps == null) {
			distGps = refDgps;
			fuelGps = refFgps;
		}
		return new Pair<GpsData, GpsData>(fuelGps, distGps);
	}
	
	public static void detectLevelChange(Connection conn, VehicleModelInfo vehicleInfo, int dimId,  GpsData startPoint, GpsData endPointIncl, boolean force) throws Exception {
		int medianWindow = vehicleInfo.m_medianWindow;
		Dimension dim = Dimension.getDimInfo(dimId);
		if (dim == null || dim.getModelSpec() == null)
			return;
		int ruleForDim = getLevelRuleIdForDim(dimId);
		if (Misc.isUndef(ruleForDim))
			return;
		NewVehicleData dataList = vehicleInfo.getDataList(conn, dimId);
		NewVehicleData distList = vehicleInfo.getDataList(conn, 0);
		
		ModelSpec modelSpec = vehicleInfo.getModelSpec(conn, dimId);
		VehicleSpecific vehicleSpecificParam = vehicleInfo.getVehicleParam(conn, dimId);
		
		if (dataList == null || modelSpec == null)
			return;
		
		//if (modelSpec.levelThreshold == null || modelSpec.levelThreshold.size() == 0)//UNCOMMENT_FOR_PREV_APPROACH
		//	continue;
		
		LevelChangeList levelChangeList = vehicleInfo.getLevelChangeList(conn, dimId);
		if (levelChangeList == null) {
			levelChangeList = vehicleInfo.addLevelChangeList(dimId);
		}
		if (!force && levelChangeList.ptsSeenSinceLastCheck < levelChangeList.g_checkAfterNPoints) {
			levelChangeList.ptsSeenSinceLastCheck++;
			return;
		}
		int medianWindowMult = VehicleModelInfo.g_changeJumpValueIfAllChanges ? 2 : 1;
		double distKM = vehicleInfo.m_maxMedianDistanceKM;
		int maxMedianWindow = vehicleInfo.m_maxMedianWindow;
		GpsData distGps = distList.get(conn, startPoint);
	    Pair<GpsData, GpsData> adjstart=  getPointAtDist(conn, startPoint, distGps, true, distKM, medianWindow, maxMedianWindow, dataList, distList);	
		startPoint = adjstart.first;
		distGps = adjstart.second;
		//Now go thru stop list ... starting from start ... until we get to a start exceeding the endIncl ..
		//for each 
		SSpList startStopList = vehicleInfo.startStopList;
		int stopIndex = LevelChangeUtil.getStartBefIndex(startPoint, startStopList, modelSpec.stopDurExceedsSec);
		if (stopIndex < 0)
			return;
		GpsData fuelEnd = endPointIncl;
		double scale = vehicleSpecificParam == null ? 1 : vehicleSpecificParam.getScale();
		levelChangeList.ptsSeenSinceLastCheck = 0;
		Pair<Integer, Integer> dirtyIndicator = new Pair<Integer, Integer>(-1,-1);
		
		for (int i=stopIndex,is=startStopList.size();i<is;) {//i will be incremented inside
			SGpsData stopStart = startStopList.get(i);
			if (stopStart == null)
				break;
			if (fuelEnd.getGps_Record_Time() < stopStart.getGps_Record_Time())
				break;
			boolean dbgStop = false;
			if (stopStart.getGps_Record_Time() >= dgbdt1 && stopStart.getGps_Record_Time() <= dgbdt2) {
				int dbg = 1;
				dbgStop = true;
				dbg++;
			}
			int stopEndIndex = LevelChangeUtil.getStopEnd(i, startStopList, modelSpec.stopDurExceedsSec);
			SGpsData stopEnd = startStopList.get(stopEndIndex);
			if (stopEnd == null)
				break;
			GpsData dataAtStart = dataList.get(conn, stopStart);
			
			if (dataAtStart == null) {
				i = LevelChangeUtil.getNextStopStart(stopEndIndex, startStopList, modelSpec.stopDurExceedsSec);
				continue;//dataAtStart = dataList.get(0);
			}
			GpsData dataAtEnd = dataList.get(conn, stopEnd);
			if (dataAtEnd == null)
				dataAtEnd = dataAtStart;
			double hackKMPerLitre = modelSpec.kmpl;
			if (hackKMPerLitre < 0)
				hackKMPerLitre  = 15;
			double hackDist = stopStart.fastGeoDistance(dataAtEnd.getLongitude(), dataAtEnd.getLatitude());
			GpsData tprevEnd = dataAtEnd;
			GpsData origDataEnd = dataAtEnd;
			
			GpsData bestDataAtEnd = null;
			double bestGapRaw = 0;
			double bestGap = 0;
			boolean bestIsFinallyValid = false;
			
			for (int k1 = 0, k1s = medianWindowMult* medianWindow;k1 <k1s;k1++) {
				GpsData temp = dataList.get(conn, origDataEnd, k1);
				if (temp == null)
					break;
				dataAtEnd = temp;
				hackDist += tprevEnd.fastGeoDistance(dataAtEnd.getLongitude(), dataAtEnd.getLatitude());
				tprevEnd = dataAtEnd;
				//int indexOfEnd = dataList.indexOf(dataAtEnd).first;
				boolean gapExceeded = modelSpec.isValExccedForCheck(dataAtStart.getValue(), dataAtEnd.getValue(), vehicleSpecificParam);
				
				GpsData innerPrevEnd = dataAtEnd;
				boolean checkingUpside = dataAtEnd.getValue() - dataAtStart.getValue() > 0.0001;
				double innerHackDist = 0;
				if (gapExceeded) {//does not work for dist dim
					
					double prevVal = dataAtStart.getValue();
					
					Pair<GpsData, GpsData> lookTill=  getPointAtDist(conn, dataAtEnd, distList.get(conn,temp), false, distKM, modelSpec.posNegForward, maxMedianWindow, dataList, distList);	
					
//					int sz = lookTill.first;
					double pos = 0;
					double neg = 0;
					int countExceedThresh = 0;
					int countBelowThresh = 0;
					GpsData origLastCheckTill = null;
					int ptsSeen = 0;
					for (int j=0;true;j++) {
						GpsData tcurrData = dataList.get(conn, dataAtEnd, j);
						if (tcurrData == null || (lookTill.first != null && lookTill.first.getGps_Record_Time() <= tcurrData.getGps_Record_Time()))
							break;
						ptsSeen++;
						double predicted = prevVal;// + slope * tdelta;
						double residue = tcurrData.getValue() - predicted;
						innerHackDist += innerPrevEnd.fastGeoDistance(tcurrData);
						innerPrevEnd = tcurrData;
						if (!checkingUpside) {
							double hackTotDist = hackDist+innerHackDist;
							double hacklitreadjust = hackTotDist/hackKMPerLitre;
							residue += hacklitreadjust;
						}
						residue /= scale;
						if (residue > 0) {
							pos += residue;
							if (residue > modelSpec.posNegThresh)
								countExceedThresh++;
						}
						else {
							neg -= residue; //effectively adding the sum of the abs to neg
							if (residue < -1*modelSpec.posNegThresh)
								countBelowThresh++;
						}
					}//end for got number of points exceeding/being less than resiude;
					if (ptsSeen <= 0)
						continue;
					double gapRaw =  ( (pos-neg)/((double)ptsSeen));// ( (pos-neg)/((double)modelSpec.posNegForward));
					double gap = Math.abs(gapRaw);
					
					
					if (bestDataAtEnd == null || (Math.abs(gapRaw)-Math.abs(bestGapRaw)) > 0.001) {
						bestDataAtEnd = dataAtEnd;
						bestGapRaw = gapRaw;
						bestGap = gap;
					}
					boolean thresholdViolated = gap > modelSpec.posNegThresh;
					if (thresholdViolated) {
						if (gapRaw > 0) {
							//if (((double)countExceedThresh/(double)modelSpec.posNegForward) < modelSpec.posNegPropExceedingThresh)
							if (((double)countExceedThresh/(double)ptsSeen) < modelSpec.posNegPropExceedingThresh)
								
								thresholdViolated = false;
						}
						else {
							//if (((double)countBelowThresh/(double)modelSpec.posNegForward) < modelSpec.posNegPropExceedingThresh)
							if (((double)countBelowThresh/(double)ptsSeen) < modelSpec.posNegPropExceedingThresh)
								thresholdViolated = false;
						}
					}
					//to prevent duplicate events ... we continue ignoring consecutive violations
					if (thresholdViolated) {
						//helperAddToEventList(vehicleInfo, stop.first, gapRaw*scale, dimId, dirtyIndicator, dataAtEnd);
						bestIsFinallyValid = true;
					}
					else {
						bestIsFinallyValid = false;
						//helperMarkEventForRemove(levelChangeList, stop.first, dirtyIndicator);
					}
				}//gap exceeded
				else {
					bestIsFinallyValid = false;
				}
			} //finished examining if constraint is violated
			if (bestDataAtEnd != null) {
				if (bestIsFinallyValid) {
					//check if on the LHS there is no delta ... else remove it
					boolean toAdd = true;
					boolean toLookLeft = true;
					if (toLookLeft) {
						toAdd = false;
						double prevVal = dataAtStart.getValue();
//						int indexOfStart = dataList.indexOf(dataAtStart).first;
						Pair<GpsData, GpsData> lookTill=  getPointAtDist(conn, dataAtStart, distList.get(conn, dataAtStart), true, distKM, modelSpec.posNegForward, maxMedianWindow, dataList, distList);	
					
//						int sz = lookTill.first;
						double pos = 0;
						double neg = 0;
						int countExceedThresh = 0;
						int countBelowThresh = 0;
						GpsData origLastCheckTill = null;
						int ptsSeen = 0;
						GpsData innerPrevEnd = dataAtStart;
						boolean checkingUpside = !(dataAtEnd.getValue() - dataAtStart.getValue() > 0.0001);
						double innerHackDist = 0;
						double rhsVal  = bestDataAtEnd.getValue();
						int countOfConsequentLHSValSimilarToRHSVAL  = 0;
						int maxCountOfConsequentLHSValSimilarToRHSVAL  = 0;
						
						for (int j=0;true;j--) {
							GpsData tcurrData = dataList.get(conn, dataAtStart, j);
							if (tcurrData == null || (lookTill.first != null && lookTill.first.getGps_Record_Time() >= tcurrData.getGps_Record_Time()))
								break;
							ptsSeen++;
							double predicted = prevVal;// + slope * tdelta;
							double residue = tcurrData.getValue() - predicted;
							double resRelRHS  = tcurrData.getValue()  - rhsVal;
							innerHackDist += innerPrevEnd.fastGeoDistance(tcurrData);
							innerPrevEnd = tcurrData;
							if (!checkingUpside) {
								double hackTotDist = hackDist+innerHackDist;
								double hacklitreadjust = hackTotDist/hackKMPerLitre;
								residue += hacklitreadjust;
								resRelRHS += hacklitreadjust;
							}
							
							resRelRHS /= scale;
							if (Math.abs(resRelRHS) < modelSpec.posNegThresh) {
								countOfConsequentLHSValSimilarToRHSVAL++;
							}
							else {
								if (countOfConsequentLHSValSimilarToRHSVAL > maxCountOfConsequentLHSValSimilarToRHSVAL) {
									maxCountOfConsequentLHSValSimilarToRHSVAL  = countOfConsequentLHSValSimilarToRHSVAL;
								}
								countOfConsequentLHSValSimilarToRHSVAL = 0;
							}
							residue /= scale;
							if (residue > 0) {
								pos += residue;
								if (residue > modelSpec.posNegThresh)
									countExceedThresh++;
							}
							else {
								neg -= residue; //effectively adding the sum of the abs to neg
								if (residue < -1*modelSpec.posNegThresh)
									countBelowThresh++;
							}
						}//end for got number of points exceeding/being less than resiude;
						if (countOfConsequentLHSValSimilarToRHSVAL > maxCountOfConsequentLHSValSimilarToRHSVAL) {
							maxCountOfConsequentLHSValSimilarToRHSVAL  = countOfConsequentLHSValSimilarToRHSVAL;
						}
						if (ptsSeen < 0)
							toAdd = true;
						if (ptsSeen >= 0) {
							double gapRaw =  ( (pos-neg)/((double)ptsSeen));// ( (pos-neg)/((double)modelSpec.posNegForward));
							double gap = Math.abs(gapRaw);
							boolean thresholdViolated = Math.abs(gapRaw-bestGapRaw) > modelSpec.posNegThresh;
							if (thresholdViolated)
								toAdd = true;
							else
								toAdd = false;
//						if (thresholdViolated) {
//							if (gapRaw > 0) {
//								if (((double)countExceedThresh/(double)ptsSeen) < modelSpec.posNegPropExceedingThresh)
//									thresholdViolated = false;
//							}
//							else {
//								if (((double)countBelowThresh/(double)ptsSeen) < modelSpec.posNegPropExceedingThresh)
//									thresholdViolated = false;
//							}
//						}
						}//ptseen > 0
						if (toAdd && maxCountOfConsequentLHSValSimilarToRHSVAL > 1*medianWindow) //make it parametrizable
							toAdd = false;
					}//end of if validating against LHS
					if (toAdd)
						helperAddToEventList(conn, vehicleInfo, stopStart, bestGapRaw*scale, dimId, dirtyIndicator, bestDataAtEnd, dataAtStart);
					else 
						helperMarkEventForRemove(levelChangeList, stopStart, dirtyIndicator);		
				}
				else {
					helperMarkEventForRemove(levelChangeList, stopStart, dirtyIndicator);
				}
				if (dirtyIndicator.first != -1) {
					vehicleInfo.getLevelChangeList(conn, dimId).save(conn, dirtyIndicator,  vehicleInfo.getVehicleId(), dimId, ruleForDim, dataList, vehicleInfo);
					dirtyIndicator.first = -1;
					dirtyIndicator.second = -1;
				}
			}//if valid/not valid from rhs ... so now t
			else {
				helperMarkEventForRemove(levelChangeList, stopStart, dirtyIndicator);
				if (dirtyIndicator.first != -1) {
					vehicleInfo.getLevelChangeList(conn, dimId).save(conn, dirtyIndicator,  vehicleInfo.getVehicleId(), dimId, ruleForDim, dataList, vehicleInfo);
					dirtyIndicator.first = -1;
					dirtyIndicator.second = -1;
				}
			}
			i = LevelChangeUtil.getNextStopStart(stopEndIndex, startStopList, modelSpec.stopDurExceedsSec);
		}
		
			
	}
	
	DEBUG13 */
	private static boolean helperMarkEventForRemove(LevelChangeList changeList, GpsData data, Pair<Integer, Integer> dirtyIndicator) {
		boolean madeDirty = false;
		
		Pair<Integer, Boolean> index = changeList.indexOf(new LevelChangeEvent(data, null));
		if (index.second) {
			LevelChangeEvent evt = changeList.get(index.first);
			if (evt.getEventId() > 0) {
				evt.setEventId(-1*evt.getEventId());
				evt.setIsDirty(true);
				if (dirtyIndicator.first == -1 || dirtyIndicator.first > index.first)
					dirtyIndicator.first = index.first;
				if (dirtyIndicator.first == -1 || dirtyIndicator.second < index.first)
					dirtyIndicator.second = index.first;
				madeDirty = true;
			}
			else {
				//changeList.remove(index.first);
				if (!Misc.isUndef(evt.getEventId())) {
					evt.setIsDirty(true);
				}
				else {
					changeList.remove(index.first);
					if (dirtyIndicator.first > -1 && dirtyIndicator.first >= index.first)
						dirtyIndicator.first = dirtyIndicator.first - 1;
					if (dirtyIndicator.second > -1 && dirtyIndicator.second >= index.first)
						dirtyIndicator.second = dirtyIndicator.second - 1;
				}
				madeDirty = true;
			}
		}
		else {
			//do nothing
		}
		return madeDirty;
	}
	/*DEBUG13 
	private static boolean helperAddToEventList(Connection conn, VehicleModelInfo vehicleInfo, GpsData ptAtStopBeg, double delta, int dimId, Pair<Integer, Integer> dirtyIndicator, GpsData ptAtEnd, GpsData dataAtStart) throws Exception {
		boolean madeDirty = false;
		
		LevelChangeList eventList = vehicleInfo.getLevelChangeList(conn, dimId);
		NewVehicleData dataList = vehicleInfo.getDataList(conn, dimId);
		GpsData dataAtEnd = null;
		synchronized (dataList) {
			Pair<Integer,Boolean> idxEnd = dataList.indexOf(conn, ptAtEnd);
			int idxEndIntege = idxEnd.first;
			if (!idxEnd.second)
				idxEndIntege++;
			dataAtEnd = dataList.simpleGet(idxEndIntege);
		}
		if (dataAtEnd != null) {
			delta = dataAtEnd.getValue() - dataAtStart.getValue();
		}
		LevelChangeEvent dummy = new LevelChangeEvent(Misc.getUndefInt(), ptAtStopBeg, delta, ptAtEnd);
		Pair<Integer, Boolean> index = eventList.indexOf(dummy);
		
		//10 08:56:45
		LevelChangeEvent entry = dummy;
		dummy.setValue(dataAtStart.getValue());
		dummy.setIsDirty(true);
		boolean toAdd = true;
		if (index.second) {
			entry = eventList.get(index.first);
			//check if it could potentially previous one
			LevelChangeEvent overlapping = eventList.get(index.first-1);
			if (overlapping != null && (overlapping.getEventId() > 0 || Misc.isUndef(overlapping.getEventId())) && overlapping.getEndPt().getGps_Record_Time() > dummy.getGps_Record_Time()) {
				double overlapAmt = Math.abs(overlapping.getAmtChange());
				double meAmt = Math.abs(dummy.getAmtChange());
				
				if (overlapAmt > meAmt || Misc.isEqual(overlapAmt, meAmt)) {
					//dont add and mark myself for removal
					toAdd = false;
					helperMarkEventForRemove(eventList, entry, dirtyIndicator);
				}
				else { //delete previous and add this
					helperMarkEventForRemove(eventList, overlapping, dirtyIndicator);
				}
			}
			//check if it could potentially overlap next one
			if (toAdd) {
				overlapping = eventList.get(index.first+1);
				if (overlapping != null && (overlapping.getEventId() > 0 || Misc.isUndef(overlapping.getEventId())) && dummy.getEndPt().getGps_Record_Time() > overlapping.getGps_Record_Time()) {
					double overlapAmt = Math.abs(overlapping.getAmtChange());
					double meAmt = Math.abs(dummy.getAmtChange());
					
					if (overlapAmt > meAmt && !Misc.isEqual(overlapAmt, meAmt)) {
						//dont add and mark myself for removal
						toAdd = false;
						helperMarkEventForRemove(eventList, entry, dirtyIndicator);
					}
					else { //delete previous and add this
						helperMarkEventForRemove(eventList, overlapping, dirtyIndicator);
					}
				}
			}
			if (toAdd) {
				if (!Misc.isEqual(entry.getAmtChange(), delta))
					madeDirty = true;
			
				entry.setAmtChange(delta);
				entry.setValue(dataAtStart.getValue());
				entry.setIsDirty(true);
				entry.setEndPt(ptAtEnd);
				if (entry.getEventId() < 0 && !Misc.isUndef(entry.getEventId())) {
					entry.setEventId(-1*entry.getEventId());
					entry.setIsDirty(true);
					madeDirty = true;
				}
				toAdd = false;
			}
			
		}
		if (toAdd) {
			//check if this event being added overlaps a pervious event - if so pick the one that give the maximum in amt(dur) ...
			LevelChangeEvent overlapping = eventList.get(index.first);
			if (overlapping != null && (overlapping.getEventId() > 0 || Misc.isUndef(overlapping.getEventId())) && overlapping.getEndPt().getGps_Record_Time() > dummy.getGps_Record_Time()) {
				double overlapAmt = Math.abs(overlapping.getAmtChange());
				double meAmt = Math.abs(dummy.getAmtChange());
				
				if (overlapAmt > meAmt && !Misc.isEqual(overlapAmt, meAmt)) {
					//do nothing
					toAdd = false;
				}
				else { //delete previous and add this
					helperMarkEventForRemove(eventList, overlapping, dirtyIndicator);
				}
			}
		}
		if (toAdd) { //check with next one for overlapping
			LevelChangeEvent overlapping = eventList.get(index.first+1);
			if (overlapping != null && (overlapping.getEventId() > 0 || Misc.isUndef(overlapping.getEventId())) && dummy.getEndPt().getGps_Record_Time() > overlapping.getGps_Record_Time()) {
				double overlapAmt = Math.abs(overlapping.getAmtChange());
				double meAmt = Math.abs(dummy.getAmtChange());
				
				if (overlapAmt > meAmt && !Misc.isEqual(overlapAmt, meAmt)) {
					//do nothing
					toAdd = false;
				}
				else { //delete previous and add this
					helperMarkEventForRemove(eventList, overlapping, dirtyIndicator);
				}
			}
		}
		
		if (toAdd) {
				eventList.add(dummy);
				dummy.setValue(dataAtStart.getValue());
				dummy.setIsDirty(true);
				
				madeDirty = true;
				if (dirtyIndicator.first != -1) {
					if (dirtyIndicator.first > index.first)
						dirtyIndicator.first = dirtyIndicator.first+1;
					if (dirtyIndicator.second > index.first)
						dirtyIndicator.second = dirtyIndicator.second+1;
				}
		}
		if (madeDirty) {
			int indexInt = index.first;
			if (!index.second)
				indexInt++;
			if (dirtyIndicator.first == -1) {
				dirtyIndicator.first = indexInt;
				dirtyIndicator.second = indexInt;
			}
			else {
				if (dirtyIndicator.first > indexInt)
					dirtyIndicator.first = indexInt;
				if (dirtyIndicator.second < indexInt)
					dirtyIndicator.second = indexInt;
			}
		}
		return madeDirty;
	}
	DEBUG13 */
	private static int helperGetThresholdViolated(ArrayList<ArrayList<Pair<Integer, Integer>>> residueCount, ModelSpec modelSpec) { //NOT USED
		int retval = 0;
		for (int i=0,is = residueCount.size(); i<is;i++) {
			ArrayList<Pair<Integer, Integer>> row = residueCount.get(i);
			ArrayList<Double> threshCheckRow = null;//modelSpec.levelThreshold.get(i); //UNCOMMENT_FOR_PREV_APPROACH
			for (int j=0,js = row.size();j<js;j++) {
				Pair<Integer, Integer> item = row.get(j);
				if (item.first == 0)
					continue;
				double prop = ((double)item.first)/((double)item.second);
				if (prop > threshCheckRow.get(j)) {
					return i;
				}
			}
		}
		return retval = -1;
	}
	 
    private static void helperUpdateCountInfo(ArrayList<ArrayList<Pair<Integer, Integer>>> addTo,  double residue, int seqFromBase, ModelSpec spec, VehicleSpecific vehicleSpecificParam) { //NOT USED
    	//NOT USED
    	residue = residue/vehicleSpecificParam.getScale();
    	ArrayList<Integer> numPointBucket = null;// spec.levelNBuckets;//UNCOMMENT_FOR_PREV_APPROACH
    	ArrayList<Double> residueBucket = null;//spec.levelResBuckets;UNCOMMENT_FOR_PREV_APPROACH
    	int szNumPoints = numPointBucket.size();
    	int szNumBucket = residueBucket.size();
    	
    	//get the index in seqFromBase which is the first Item that is greater 
    	int index1 = 0;
    	for (index1 = 0; index1 <szNumPoints && numPointBucket.get(index1) < seqFromBase; index1++ ) {
    		//do nothing
    	}
    	if (index1 == szNumPoints)
    		index1++;
    	//get the index in resiude bucket for which this items is greater ... note we are looking at positive and therfore skipping odds
    	boolean residueIsNegative = residue < 0;
    	double absResidue = residueIsNegative ? -1* residue : residue;
    	int index2 = 0;
    	for (index2 = 0; index2 <szNumBucket && residueBucket.get(index2) < absResidue; index2 += 2 ) {
    		//do nothing
    	}
    	index2 -= 2;
    	//now update temp entries ... we incr pts count 
    	for (int i=index1;i<szNumPoints;i++) {
    		for (int j=0;j<szNumBucket;j++) {
    			Pair<Integer, Integer> item = addTo.get(i).get(j);
    			item.first = item.first+1;
    			if (j <= index2+1) {
    				if (residueIsNegative && j%2 == 1)
    					item.second = item.second+1;
    				else if (!residueIsNegative && j%2 == 0)
    					item.second = item.second+1;
    			}
    		}
    	}
    }
	
	private static void dbgStartStopMarker(int vehicleId, GpsData atBeg, GpsData atEnd, Connection conn) {
		try {
			PreparedStatement ps = conn.prepareStatement("update debug_fuel set at_beg_val = ?,  at_beg_raw = ?, at_end_val = ?, at_end_raw = ? where vehicle_id = ? and at_beg_time = ? and at_end_time = ?");
			ps.setDouble(1, atBeg.getValue());
			ps.setDouble(2, atBeg.getSpeed());
			ps.setDouble(3, atEnd.getValue());
			ps.setDouble(4, atEnd.getSpeed());
			ps.setInt(5, vehicleId);
			ps.setTimestamp(6, Misc.longToSqlDate(atBeg.getGps_Record_Time()));
			ps.setTimestamp(7, Misc.longToSqlDate(atEnd.getGps_Record_Time()));
			int count = ps.executeUpdate();
			ps.close();
			if (count == 0) {
				ps = conn.prepareStatement("insert into debug_fuel (at_beg_val,  at_beg_raw, at_end_val, at_end_raw, vehicle_id, at_beg_time, at_end_time) values (?,?,?,?,?,?,?) ");
				ps.setDouble(1, atBeg.getValue());
				ps.setDouble(2, atBeg.getSpeed());
				ps.setDouble(3, atEnd.getValue());
				ps.setDouble(4, atEnd.getSpeed());
				ps.setInt(5, vehicleId);
				ps.setTimestamp(6, Misc.longToSqlDate(atBeg.getGps_Record_Time()));
				ps.setTimestamp(7, Misc.longToSqlDate(atEnd.getGps_Record_Time()));
				ps.execute();
				ps.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
    public static void loadChangeList(Connection conn, ArrayList<Integer> vehicleIds) throws Exception {
    	try {
    		getLevelRuleForDim(conn); // just be sure that it has been called and temp etc properly set up
    		StringBuilder q = new StringBuilder();
    		if (true) {
    			q.append(" delete from engine_events using ")
                  .append(" model_state join temp_model_level_rule_dimid on (temp_model_level_rule_dimid.dim_id = model_state.attribute_id) join engine_events on (model_state.vehicle_id = engine_events.vehicle_id and engine_events.rule_id = temp_model_level_rule_dimid.rule_id) ") 
                  .append(" where engine_events.event_start_time >=model_state.saved_at ") 
                ;
                if (vehicleIds != null && vehicleIds.size() != 0) {
        			q.append(" and engine_events.vehicle_id in (");
        			Misc.convertInListToStr(vehicleIds, q);
        			q.append(")");
        		}
                PreparedStatement ps = conn.prepareStatement(q.toString());
                ps.execute();
                ps.close();
    			return;
    		}
    		q.append(" select engine_events.vehicle_id, engine_events.rule_id,temp_model_level_rule_dimid.dim_id,  engine_events.id, event_start_time, addnl_value1 ")
    		  .append(" ,event_begin_longitude, event_begin_latitude, event_create_recvtime, engine_events.attribute_value, event_begin_name, engine_events.event_stop_time ")
    		  .append(" from model_state join temp_model_level_rule_dimid on (temp_model_level_rule_dimid.dim_id = model_state.attribute_id) join engine_events on (model_state.vehicle_id = engine_events.vehicle_id and engine_events.rule_id = temp_model_level_rule_dimid.rule_id) ") 
    		  .append(" where ");
 //   		q.append(" engine_events.rule_id <> ").append(LevelChangeUtil.hackGetRefuelingRuleIdForDim(3)).append(" and ");//HACK
    		q.append(" engine_events.event_start_time >= model_state.saved_at ");
    		
    		if (vehicleIds != null && vehicleIds.size() != 0) {
    			q.append(" and engine_events.vehicle_id in (");
    			Misc.convertInListToStr(vehicleIds, q);
    			q.append(")");
    		}
    		q.append(" order by engine_events.vehicle_id, engine_events.rule_id" );
    		PreparedStatement ps = conn.prepareStatement(q.toString());
    		ResultSet rs = ps.executeQuery();
    		VehicleModelInfo vehicleInfo = null;
    		FastList<LevelChangeEvent> addToThis = null;
    		int prevDimId = Misc.getUndefInt();
    		while (rs.next()) {
    			int vehicleId = rs.getInt(1);
    			int ruleId = rs.getInt(2);
    			int dimId = rs.getInt(3);
    			int eventId = rs.getInt(4);
    			Date ts = Misc.sqlToUtilDate(rs.getTimestamp(5));
    			Date evtstopTs = Misc.sqlToUtilDate(rs.getTimestamp("event_stop_time"));
    			double delta = rs.getDouble(6);
    			double lon = rs.getDouble(7);
    			double lat = rs.getDouble(8);
    			Date rscv = Misc.sqlToUtilDate(rs.getTimestamp(9));
    			double val = rs.getDouble(10);
    			String name = rs.getString(11);
    			if (vehicleInfo == null || vehicleInfo.getVehicleId() != vehicleId) {
    				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(null, vehicleId);
    				addToThis = null;
    			}
    			if (vehicleInfo == null)
    				continue;
    			
    			GpsData evtData = new GpsData(ts);
    			GpsData evtStop = new GpsData(evtstopTs);
    			evtData.setGpsRecvTime(rscv);
    			//ON_THE_FLY_NAME evtData.setName(name);
//    			evtData.setPoint(new Point(lon, lat));
    			evtData.setLongitude(lon);
    			evtData.setLatitude(lat);
    			evtData.setSourceChannel(ChannelTypeEnum.CURRENT);
    			evtData.setDimensionInfo(dimId, val);
    			evtData.setSpeed(val);
    			LevelChangeEvent event = new LevelChangeEvent(eventId, evtData, delta, evtStop);
    			if (addToThis == null || prevDimId != dimId) {
    				//DEBUG13 addToThis = vehicleInfo.getLevelChangeList(conn, dimId);
    				if (addToThis == null)
    					addToThis = vehicleInfo.addLevelChangeList(dimId);
    			}
    			addToThis.add(event);
    			prevDimId = dimId;
    		}
    		rs.close();
    		ps.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    
    
}

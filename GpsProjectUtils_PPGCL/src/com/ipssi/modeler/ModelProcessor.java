package com.ipssi.modeler;

import static com.ipssi.gen.utils.Common.isNull;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//DEBUG13 import com.ipssi.cache.NewVehicleData;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.Dimension;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.GpsDataResultSetReader;
import com.ipssi.processor.utils.Vehicle;

public class ModelProcessor {
	public static final boolean g_doLazyCacheBuild = true;//for debug false, in prod true

	public static boolean handleMultiplePoints(Connection conn, ArrayList<GpsData> allData, int vehicleId, PreparedStatement psUpdateData) throws Exception {
		//hack .. 
		/* DEBUG13
		boolean hasFuel = false;
		for (GpsData d:allData) {
			if (d.getDimId() == 3) {
				hasFuel = true;
				break;
			}
		}
		if (!hasFuel) {
			return false;
		}
		return handleMultiplePoints(conn, allData,  VehicleModelInfo.getVehicleModelInfo(conn, vehicleId), psUpdateData, false, false, false, false, false);
		*/
		return false;
	}
	/*DEBUG13	
	private static Pair<Boolean, GpsData> helpEvaluateStopOrDataGap(Connection conn, GpsData refGpsData, FastList<SGpsData> specialList, NewVehicleData dataList, double distThreshold, int gapSecThreshold, int lossSecThreshold) {
		if (refGpsData.getDimId() == ApplicationConstants.IGN_ONOFF) {
				return new Pair<Boolean, GpsData>(refGpsData.getValue() < 0.5, refGpsData);
		}
		int dataDimId = 0;
		GpsData prevDataPoint = null; 
		
		//check against previous stopped point (to avoid creeping movement
		SGpsData lastLocalGpsData = new SGpsData(refGpsData);
		SGpsData stopCheckLocal = specialList.get(lastLocalGpsData, true);
		boolean prevSpecialListIsTrue = false;
		if (stopCheckLocal != null && stopCheckLocal.isTrue()) {
		     	prevDataPoint = stopCheckLocal;
		     	prevSpecialListIsTrue = true;
		}
		GpsData ptBefore = dataList.get(conn,refGpsData, -1);
		
		if (prevDataPoint == null) {
			prevDataPoint = ptBefore;
		}
		
		if (prevDataPoint != null) {
			//get distance, get threshold for the vehicle org parameter
		   double dist = prevDataPoint.fastGeoDistance(lastLocalGpsData.getLongitude(), lastLocalGpsData.getLatitude());
		   boolean retval = dist < distThreshold && !Misc.isEqual(dist,distThreshold);
		   if (!retval) {
			   //check if there is excess gap
			   if (!prevSpecialListIsTrue) { //if comparing against prevspecialListIsTrue ... regardless of prev being true/false, if this is moving then we need to insert false
				   int gapSec = (int)(refGpsData.getGps_Record_Time() - prevDataPoint.getGps_Record_Time())/1000;
				   if (gapSec > lossSecThreshold) {
					   retval = true;
				   }   
			   }
		   }
		   if (retval) {
			   GpsData startMarker = refGpsData;
			   if (!prevSpecialListIsTrue) {
				   startMarker = prevDataPoint;
			   }
			   return new Pair<Boolean, GpsData>(true, startMarker);
		   }
		   else {
			   
			   return new Pair<Boolean, GpsData>(false, refGpsData);
		   }
		}
		return new Pair<Boolean, GpsData>(false, refGpsData);
	}
	
	private static void handleIntermediateUpdate(Connection conn, SSpList specialList, NewVehicleData dataList, GpsData gpsData, Pair<Integer, Integer> specialListImpactBegInclEndExcl, FastList<SGpsData> tempSpecialList, double distThreshold, int gapThreshold, int lossSecThreshold) throws Exception {
		//DataList dataList = vehicleCachedInfo.getDataList();

		SGpsData dummy = tempSpecialList.get(new SGpsData(gpsData));
		if (dummy != null) {
			Pair<Integer, Boolean> pair = tempSpecialList.indexOf(dummy);
			if (pair != null && pair.first != null) {
				dummy = tempSpecialList.get(pair.first + 1);
			}
		}

		boolean ancientSpecialListGpsDataIsSame = false;
		int index = 0;
		for (GpsData nextGpsData = dataList.get(conn, gpsData, ++index); nextGpsData != null; nextGpsData = dataList.get(conn, gpsData, ++index)) {
			
			Pair<Boolean, GpsData> evalResult = ModelProcessor.helpEvaluateStopOrDataGap(conn, nextGpsData, specialList, dataList, distThreshold, gapThreshold,lossSecThreshold);
			SGpsData localGpsData = new SGpsData(evalResult.second, evalResult.first);
			boolean doingArtificial = !localGpsData.equals(nextGpsData);
			if (doingArtificial) {
				// check if the point specialList is same as this one
				SGpsData spListRefPt = specialList.get(localGpsData);
				if (!(spListRefPt != null && spListRefPt.equals(localGpsData))) {
					doingArtificial = false;
				}
			}
			boolean done = false;
			if (doingArtificial) { // insert both the begin and endmarker
				localGpsData.setArtificial();
				// check if something needs to be done ..
				SGpsData dataInSpecialList = specialList.get(localGpsData);
				boolean localDataIsTrue = localGpsData.isTrue();
				boolean dataInSpecialListIsTrue = dataInSpecialList == null ? !localDataIsTrue : dataInSpecialList.isTrue();
				if (dataInSpecialListIsTrue != localDataIsTrue) { // we need to do something
					specialList.update(localGpsData, specialListImpactBegInclEndExcl);
				} else {
					done = true;
					done = false;
				}
				SGpsData endMarker = new SGpsData(nextGpsData, evalResult.first);
				dataInSpecialList = specialList.get(endMarker);
				boolean endMarkerIsTrue = endMarker.isTrue();
				dataInSpecialListIsTrue = dataInSpecialList == null ? !endMarkerIsTrue : dataInSpecialList.isTrue();
				if (dataInSpecialListIsTrue != endMarkerIsTrue) { // we need to do something
					specialList.update(endMarker, specialListImpactBegInclEndExcl);
				} else {
					done = true;
					done = false;
				}
			} else {
				// check if something needs to be done ..
				SGpsData dataInSpecialList = specialList.get(localGpsData);
				boolean localDataIsTrue = localGpsData.isTrue();
				boolean dataInSpecialListIsTrue = dataInSpecialList == null ? !localDataIsTrue : dataInSpecialList.isTrue();
				if (dataInSpecialListIsTrue != localDataIsTrue) { // we need to do something
					specialList.update(localGpsData, specialListImpactBegInclEndExcl);
				} else {
					done = true;
					done = false;
				}
			}
			if (done) {
				break;
			}
			
			SGpsData modifiedSpecialListMarker = specialList.get(new SGpsData(nextGpsData),1);
			
			SGpsData oldSpecialListMarker = tempSpecialList.get(new SGpsData(nextGpsData),1);
			
			if ( modifiedSpecialListMarker != null && oldSpecialListMarker != null && 
					modifiedSpecialListMarker.equals(oldSpecialListMarker) && 
					modifiedSpecialListMarker.isTrue() == oldSpecialListMarker.isTrue()){
				if ( ancientSpecialListGpsDataIsSame && !dummy.equals(modifiedSpecialListMarker) && dummy.isTrue() != modifiedSpecialListMarker.isTrue()){
					break;
				} 
				else {
					dummy = modifiedSpecialListMarker;
					ancientSpecialListGpsDataIsSame = true;
				}
			} 
			else {
				ancientSpecialListGpsDataIsSame = false;
			}
		}
	}

	private static void helpAddToStopStart(GpsData data, VehicleModelInfo vehicleModelInfo, int gapThreshold, int lossSecThreshold, Connection conn) throws Exception {
		int dimId = data.getDimId();
		if (dimId != 0 && dimId != ApplicationConstants.IGN_ONOFF) {
			return;
		}
		NewVehicleData dataList = vehicleModelInfo.getDataList(conn, dimId);
		SSpList specialList = dimId == 0 ? vehicleModelInfo.startStopList : vehicleModelInfo.ignOffStartStopList;
		FastList<SGpsData> tempSpecialList = specialList.createTempSpecialList();
		
		Pair<Integer, Integer> specialListImpactBegInclEndExcl = new Pair<Integer,Integer>(-1,-1);
		VehicleSpecific vehicleSpecificParam = vehicleModelInfo.getVehicleParam(conn, dimId);
		double threshold = 0.050; 
		CacheTrack.VehicleSetup vehSetupInfo = CacheTrack.VehicleSetup.getSetup(vehicleModelInfo.getVehicleId(), conn);
		CacheTrack.VehicleSetup.DistCalcControl distCalcControl = vehSetupInfo == null ? null : vehSetupInfo.getDistCalcControl(conn);
		if (distCalcControl != null && !Misc.isUndef(distCalcControl.m_distThresholdForStopped))
				threshold = distCalcControl.m_distThresholdForStopped;
		//threshold = 0.1;
		Dimension deltaDim = Dimension.getDimInfo(dimId);
		boolean isCumm = deltaDim != null && deltaDim.isCummulative();
		Pair<Boolean, GpsData> evalResult = helpEvaluateStopOrDataGap(conn, data, specialList, dataList, threshold, gapThreshold, lossSecThreshold); 
		SGpsData localGpsData = new SGpsData(evalResult.second, evalResult.first);
				 
		boolean doingArtificial = !localGpsData.equals(data);
		if (doingArtificial) {
			// check if the point specialList is same as this one
			SGpsData spListRefPt = specialList.get(localGpsData);
			if (!(spListRefPt != null && spListRefPt.equals(localGpsData))) {
				doingArtificial = false;
			}
		}
		if (doingArtificial) {
			localGpsData.setArtificial();
			specialList.update(localGpsData, specialListImpactBegInclEndExcl);
			SGpsData endMarker = new SGpsData(data, evalResult.first);
			specialList.update(endMarker, specialListImpactBegInclEndExcl);
		} 
		else {
			specialList.update(localGpsData, specialListImpactBegInclEndExcl);
		}
		handleIntermediateUpdate(conn, specialList, dataList, data, specialListImpactBegInclEndExcl, tempSpecialList, threshold, gapThreshold, lossSecThreshold);
	}
	*/
	public static boolean handleMultiplePoints(Connection conn, ArrayList<GpsData> allData, VehicleModelInfo vehicleModelInfo, PreparedStatement psUpdateData, boolean justAdd, boolean addNewPointsInUpdate, boolean doingRecovery, boolean generateStats, boolean inCacheBuildMode) throws Exception { //returns true if there was an addBatch for prior data
		/*DEBUG13
		//psUpdateData = DataProcessor.DBQueries.NEW_UPDATE_DATA_INFO
		try {
			//1. Add simple points ... so that ref are all set
			//2. Add points with modelSpec
			//    for each point find the change indices and save it back to the database
			//3. clean up .... 
			addNewPointsInUpdate = true; //dont know but in DP there is an issue
			ArrayList<Pair<Integer,Boolean>> addPos = new ArrayList<Pair<Integer, Boolean>>(allData.size());
			boolean hasUsefulModel = false;
			boolean retval = false;
			boolean hasDC = false;
			Dimension fuelDim = Dimension.getDimInfo(3);
			int gapThreshold = fuelDim == null || fuelDim.getModelSpec() == null ? 180 : fuelDim.getModelSpec().stopDurExceedsSec;
			int lossSecThreshold = fuelDim == null || fuelDim.getModelSpec() == null ? 1200 : (int) fuelDim.getModelSpec().resetIfGapExceedsSecs;
			int maxPointsCount = 0;
			for (GpsData data:allData) {
				if (!hasDC)
					hasDC = ChannelTypeEnum.isDataChannel(data.getSourceChannel());
				Dimension dim = Dimension.getDimInfo(data.getDimId());
				NewVehicleData dataList = vehicleModelInfo.getDataList(conn, dim.getId());
				
				if ((dim.isUsedInModel() || dim.isPowerOnIndicator()) && dim.getModelSpec() == null) {
					dataList.add(conn, data);
					helpAddToStopStart(data, vehicleModelInfo, gapThreshold, lossSecThreshold, conn);
				}
				else if (justAdd) { //TBD should it be here ... although handle also seems to work ok
					//CENT_CACHE_DOWN_BUT_CHECK Pair<GpsData, Boolean> addRes = dataList.add(conn, data);
					dataList.add(conn, data);
					helpAddToStopStart(data, vehicleModelInfo, gapThreshold, lossSecThreshold, conn);
					//CENT_CACHE_DOWN_BUT_CHECK if (!addRes.second)
						dataList.setMpMarker(data);
				}
				else if (dim.getModelSpec() != null){
					hasUsefulModel = true;
				}
			}
			
			if (hasUsefulModel) {
				ArrayList<GpsData> toRemove = new ArrayList<GpsData>();
				for (int i=0,is=allData.size();i<is;i++) {
					GpsData data = allData.get(i);
					Dimension dim = Dimension.getDimInfo(data.getDimId());
					if (dim.isUsedInModel() && dim.getModelSpec() != null) {
						NewVehicleData dataList = vehicleModelInfo.getDataList(conn, dim.getId());
						toRemove.clear();
						ArrayList<GpsData> changes =  handleNewPointSimple(conn, data, vehicleModelInfo, addNewPointsInUpdate, toRemove, justAdd);
						if (changes.size() > 0)
							LevelChangeUtil.detectLevelChange(conn, vehicleModelInfo, dim.getId(), changes.get(0), changes.get(changes.size()-1), false);
						for (int j=0,js = changes.size();j<js;j++) {
							GpsData gpsData = changes.get(i);
							if (gpsData == null)
								continue;
							int colIndex = 1;
							Misc.setParamDouble(psUpdateData, gpsData.getValue(), colIndex++);
							Misc.setParamDouble(psUpdateData, gpsData.getSpeed(), colIndex++);
							//psUpdateData.setInt(colIndex++, (int)(gpsData.getSpeed()*1000));
							psUpdateData.setInt(colIndex++, vehicleModelInfo.getVehicleId());
							java.sql.Timestamp grt = Misc.longToSqlDate(gpsData.getGps_Record_Time());
							psUpdateData.setTimestamp(colIndex++, grt);
							psUpdateData.setInt(colIndex++, dim.getId());
							
							psUpdateData.addBatch();
							//try {
							//psUpdateData.execute();
							//}
							//catch (Exception e) {
							//	e.printStackTrace();
							//}
							retval = true;
						}//for each change
						for (int j=0,js=toRemove.size();j<js;j++) {
							dataList.remove(toRemove.get(i));
						}
					}//is modelled data
				}//for each data
				
				//if (hasDC || maxPointsCount > 6000) {
				if (true) {
					//CENT_CACHE vehicleModelInfo.clearUnneeded(conn, false, generateStats, false);
					if (!doingRecovery) {
						vehicleModelInfo.saveState(conn, false);
					}
				}
			}//if there was a useful modelled data
			return retval;
		}//end of try
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		*/
		return false;
	}

	public static boolean isValidFromEnoughIgnOn(GpsData data, VehicleModelInfo vehicleModelInfo, long msGapThresh) {
		return true;
	
		/*DEBUG13
		SSpList ignOffStopList = vehicleModelInfo.ignOffStartStopList;
		SGpsData dummy = null;
		int prevIndex = vehicleModelInfo.tempLookupIndexInIgnOffStartStopList;
		if (!Misc.isUndef(prevIndex)) {
			SGpsData temp = ignOffStopList.get(prevIndex);
			if (temp != null) {
				if (temp.getGps_Record_Time() > data.getGps_Record_Time()) {
					temp = ignOffStopList.get(--prevIndex);
					if (temp == null || temp.getGps_Record_Time() > data.getGps_Record_Time()) {
						prevIndex = Misc.getUndefInt();
					}
				}
			}
		}
		if (Misc.isUndef(prevIndex)) {
			dummy = new SGpsData(data);
			Pair<Integer, Boolean> idx = ignOffStopList.indexOf(dummy);
			prevIndex = idx.first;
		}
		vehicleModelInfo.tempLookupIndexInIgnOffStartStopList = prevIndex;
		dummy = ignOffStopList.get(prevIndex);
		if (dummy == null) {
			return true;
		}
		else if (dummy.isTrue) {
			return false;
		}
		else {
			SGpsData dummyPrev = ignOffStopList.get(prevIndex-1);
			if (dummyPrev != null && !dummyPrev.isTrue)
				dummy = dummyPrev;
			long gap = data.getGps_Record_Time() - dummy.getGps_Record_Time();
			return gap >= msGapThresh;
		}
		*/
	}
	/*
	private static Pair<Boolean,Double> helpSetMedian(Connection conn, GpsData newPoint, VehicleModelInfo vehicleInfo, NewVehicleData dataList, ModelSpec spec, VehicleSpecific vehicleParam, boolean lookForwardToo) throws Exception {
		//CHANGE IN BEHAVIOUR ... addAt is already added
		//1st if there was reset ...
		vehicleInfo.tempLookupIndexInIgnOffStartStopList = Misc.getUndefInt();
		long msGapThresh = 110*1000;
		NewVehicleData distDataList = vehicleInfo.getDataList(conn, 0);
			
		boolean hasReset = false;
		double [] workList = vehicleInfo.medianWork1;
		GpsData[] medianPoints = vehicleInfo.medianPoints;
		int medianWindow = vehicleInfo.m_medianWindow;
		int maxMedianWindow = vehicleInfo.m_maxMedianWindow;
		double maxMedianDistKM = vehicleInfo.m_maxMedianDistanceKM;
		//maxMedianDistKM = 3;
		if (maxMedianDistKM <= 0)
			maxMedianWindow = medianWindow;
		boolean toBreakAtLowVar = true;
		int numContLowVarianceLimit = (int)(2*medianWindow); //Make it parametrizable
		boolean toBreakAtSignificantBreak = true;
		int index = 0;
		int winsz = maxMedianWindow;
		if (isValidFromEnoughIgnOn(newPoint, vehicleInfo, msGapThresh)) {
			medianPoints[index] = newPoint;
			workList[index++] = newPoint.getSpeed();
		}
		else {
			GpsData prev = dataList.get(conn, newPoint, -1);
			return new Pair<Boolean, Double>(false, prev == null ? newPoint.getSpeed() : prev.getValue());
		}
		int medianWindowSz = index;
		
		GpsData prevPoint = null; //misnomer ... it is the point at the beg of the window
		GpsData firstPoint = null;//misnomer ... it is the point just before the point being added
		lookForwardToo = false;
		
		double cummDist = 0;
		GpsData prevDistGpsPoint = null;
		int currLowVarianceCount = 0;
		
		for (int i=-1;index < winsz;i--) {
			GpsData temp = dataList.get(conn, newPoint, i);
			if (temp == null)
				break;
			if (!isValidFromEnoughIgnOn(temp, vehicleInfo, msGapThresh)) {
				continue;
			}
			
			if (firstPoint == null)
				firstPoint = temp;
			//if (temp.getModelState() == null || temp.getModelState().hasReset) .... TBD
			//	break;
			if (prevPoint != null && spec.isInLowvariance(prevPoint.getSpeed(), temp.getSpeed(), vehicleParam)) {
				currLowVarianceCount++;
			}
			else {
				currLowVarianceCount = 0;
			}
			boolean toAdd = true;
			if (currLowVarianceCount > numContLowVarianceLimit) {
				toAdd = false;
				if (toBreakAtLowVar)
					break;
			}
			else {
				
			}
			
			if (maxMedianDistKM > 0) {
				GpsData tempDistPoint = distDataList != null ? distDataList.get(conn, temp) : null;
				if (tempDistPoint != null && prevDistGpsPoint != null) {
					double delta = prevDistGpsPoint.getValue() - tempDistPoint.getValue();
					if (delta > 0.050)
						toAdd = true;
					cummDist += delta;
				}
				if (toAdd) {
					medianPoints[index] = temp;
					workList[index++] = temp.getSpeed();		
				}
				prevDistGpsPoint = tempDistPoint;
				if (cummDist >= maxMedianDistKM && index >= medianWindow)
					break;
			}
			else {
				if (toAdd) {
					medianPoints[index] = temp;
					workList[index++] = temp.getSpeed();
				}
			}
			prevPoint = temp;
		}
		if (firstPoint == null)
			firstPoint = prevPoint;
		if (firstPoint == null)
			firstPoint = newPoint;
		
		medianWindowSz = index;
		if ( toBreakAtSignificantBreak) {
			int howManyConsecutiveBreak = 1*medianWindow; //MAKE IT PARAMETERIZABLE
			for (int i=howManyConsecutiveBreak;i<medianWindowSz;i++) {
				double cv = workList[i];
				if (spec.mayNeedReset(cv, workList[i-1], vehicleParam)) {//check we see numPoints of resetvalue and if so we stop at
					boolean allSigDiff = true;
					for (int j=0,js=howManyConsecutiveBreak;j<js;j++) {
						int idx = i-j-1;
						double v = workList[idx];
						if (!spec.mayNeedReset(cv, v, vehicleParam)) {
							allSigDiff = false;
							break;
						}
					}//checked enough pts
					if (allSigDiff) {
						medianWindowSz = i;
						break;
					}
				}//
			}
		}
		//replace points that are in excess with median if all except few points dont exceed the prevVal
		if (true && medianWindowSz >= medianWindow) {
			double prevVal = prevPoint == null ? Misc.getUndefDouble() : prevPoint.getValue();
			if (!Misc.isUndef(prevVal) && spec != null) {
				int cntExceedingRange = 0;
				for (int i=0,is=medianWindow;i<is;i++) {
					double v = workList[i];
					if (spec.mayNeedReset(v, prevVal, vehicleParam)) {
						cntExceedingRange++;
					}
				}
				if (cntExceedingRange > 0) {
					int thCount = (int)(0.864*medianWindow)+1; //00.136 = 1-..864 =1- 2 sigma
					if (cntExceedingRange <= thCount && cntExceedingRange != medianWindow) { //repop with avg
						
						for (int i=0, is= medianWindow;i<is;i++) {//check for excess only for medianWindow
							GpsData temp = medianPoints[i];
							workList[i] = spec.mayNeedReset(temp.getSpeed(), prevVal, vehicleParam) ? 
									(temp.equals(newPoint) ? firstPoint.getValue() : temp.getValue()) : temp.getSpeed();
						}
					}//excess points within thresh .. replace with avg 
					else {
						hasReset = true;
					}
				}//if cnt exceeds 0
			}//valid prevval
		}//new approach for removing outlier
		double currMedian = vehicleInfo.getMedianFromWork(workList, medianWindowSz);
		
		return new Pair<Boolean, Double>(hasReset, currMedian);
	}
	*/
	public static boolean isOutOfRange(GpsData data, ModelSpec spec, VehicleSpecific vehicleParam) {
		return spec != null ? spec.toIgnore(data.getSpeed(), vehicleParam) : false;
	}
	/* ... being woked on
	public static  ArrayList<Integer> handleNewPointUseMe(GpsData data, VehicleModelInfo vehicleInfo, Pair<Integer, Boolean> addAt, boolean addNewPointsInUpdate, ArrayList<Integer> toremove, boolean justAdd) {// returns the index atWhich added if new, else -1. Assumes check done that we are not going back in time & that modelSpec is non-null (as verified thru addSimple
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			ArrayList<GpsData> ptsToBeChanged = new ArrayList<GpsData>(); //these values will change and are removed otherwise
			int dimId = data.getDimId();
			ModelSpec spec = vehicleInfo.getModelSpec(dimId);
			VehicleSpecific vehicleParam = vehicleInfo.getVehicleParam(dimId);
			VehicleModelInfo.Data dataList = vehicleInfo.getDataList(dimId);
			if (dataList == null)
				dataList = vehicleInfo.addDataList(dimId);
			ArrayList<Integer> retval = new ArrayList();
			//if out of range .. remove
			if (ModelProcessor.isOutOfRange(data, spec, vehicleParam)) {
				GpsData tempData = dataList.get(addAt.first);
				ptsToBeChanged.add(tempData);
				dataList.remove(addAt.first);
				return retval;
			}
			boolean madeChangesForOutlier = false;
			{//remove for outliers ..
				boolean toCheckForOutlier = false;
				int startIndexForOutlierCheck = addAt.first;
				int endIndexExclForOutlierCheck = addAt.first+1;
				int suppWinSize = 10;
				ArrayList<Integer> toRemoveIndices = new ArrayList<Integer>();
				ModelProcessor.identifyOutliers(data, vehicleInfo, startIndexForOutlierCheck, endIndexExclForOutlierCheck, suppWinSize, toRemoveIndices);
				boolean doingMedian = spec.modelType ==ModelSpec.MEDIAN_PLUS;
				for (int i=toRemoveIndices.size()-1;i>=0;i--) {
					GpsData tempData = dataList.get(toRemoveIndices.get(i));
					ptsToBeChanged.add(tempData);
					dataList.remove(toRemoveIndices.get(i));
					madeChangesForOutlier = true;
				}
			}		
			{//now get the median filter ...
				
			}
		
		Date gpsTime = data.getGps_Record_Time();
		Date recvTime = data.getGpsRecvTime();
		
		
		
		VehicleModelInfo.Data deltaDataList = null;
		Dimension deltaDim = Dimension.getDimInfo(spec.deltaDimId);
		if (deltaDim != null) {
			deltaDataList = vehicleInfo.getDataList(spec.deltaDimId);
		}
		
		double rawVal = data.getSpeed();
		
		
		
		if (addAt == null)
			addAt = dataList.indexOf(data);
		int addAtInt = addAt.first;
		boolean exists = addAt.second;
		boolean isAddAtEnd = dataList.isAtEnd(addAtInt);
		int currSzIncl = dataList.size()-1;
		int dcPos = dataList.getDcPos();
		boolean isOutlier = spec.toIgnore(rawVal, vehicleParam);
		dataList.prevDCPos = -1;
		
		if (isAddAtEnd && dcPos != currSzIncl) {//sync to end ..
			dataList.prevDCPos = dcPos;
			int start = dcPos;
			int end = currSzIncl;
			GpsData prev = dataList.get(start);
			GpsData prevDeltaData = null;
			if (prev != null && !spec.deltaByTime && deltaDataList != null)
				prevDeltaData =  deltaDataList.get(prev);
			for (int i=start+1;i<=end;i++) {
				GpsData curr = dataList.get(i);
				if (curr == null)
					continue;
				ModelState currState = curr.getModelState();
				double oldv = currState.getX1();
				double newv = oldv;
				if (doingMedian) {
					double median = ModelProcessor.helpSetMedian(curr, i-1, vehicleInfo, dataList);
					currState = spec.next(prev == null ? null : prev.getModelState(), median, Misc.getUndefDouble(),vehicleParam);
				}
				else {
					GpsData currDeltaData = deltaDataList == null ? null : deltaDataList.get(curr);
					double delta = spec.getDelta(prev, curr, prevDeltaData, currDeltaData, deltaDim.isCummulative());
					prevDeltaData = currDeltaData;
					double v = curr.getSpeed();
					currState = spec.next(prev == null ? null : prev.getModelState(), v, delta,vehicleParam);
				}
				newv = currState.getX1();
				if (!Misc.isEqual(newv, oldv, spec.tolForparamIsSame, 0.00001)) {//TBD - should we mark it as not even go forward re syncing currently just marking it as not requiring change in val
					retval.add(i);
					//System.out.println("[MP] - MS:Resync "+vehicleInfo.getVehicleId()+" at:"+addAtInt+1+" Time:"+curr.getGps_Record_Time()+" state:"+currState.toString());
				}
				curr.setValue(newv);
				curr.setModelState(currState);
				prev = curr;
			}	
			dcPos = currSzIncl;
		}
		
		if (!exists) {
			ModelState currState = data.getModelState();
			if (!justAdd) {	
				GpsData prev = dataList.get(addAtInt);
				if (doingMedian) {
					
					double median = Misc.getUndefDouble();
					if (isOutlier) {
						if (prev != null && prev.getModelState() != null)
							median = prev.getModelState().getX1();
					}
					if (Misc.isUndef(median))
						median = ModelProcessor.helpSetMedian(data, addAtInt, vehicleInfo, dataList);
					currState = spec.next(prev == null ? null : prev.getModelState(), median, Misc.getUndefDouble(),vehicleParam);
				}
				else {
					GpsData prevDeltaData = null;
					if (prev != null && !spec.deltaByTime && deltaDataList != null)
						prevDeltaData =  deltaDataList.get(prev);
					GpsData currDeltaData = deltaDataList == null ? null : deltaDataList.get(data);
					double delta = spec.getDelta(prev, data, prevDeltaData, currDeltaData, deltaDim.isCummulative());
					prevDeltaData = currDeltaData;
					
					currState = prev == null && data.getModelState() != null ? data.getModelState() : spec.next(prev == null ? null : prev.getModelState(), rawVal, delta, vehicleParam);
				}
				data.setModelState(currState);
			}
			double oldv = data.getValue();
			double newv = currState.getX1();
			boolean isequal = Misc.isEqual(oldv, newv, spec.tolForparamIsSame, 0.00001);
			data.setValue(currState.getX1());
			dataList.add(data);
			if (addNewPointsInUpdate) {
				if (!isequal)
					retval.add(addAtInt+1); //we will only send changes in existing data
				if (isOutlier) {
					toremove.add(addAtInt+1);
				}
			}
			//System.out.println("[MP] - MS: "+vehicleInfo.getVehicleId()+" at:"+addAtInt+1+" Time:"+data.getGps_Record_Time()+" state:"+currState.toString());
			dcPos = addAtInt+1;
			dataList.setDcPos(dcPos);
			//now check for modelReset
			if (!justAdd && !isOutlier) {//although doesn't really matter ...
				handleModelReset(addAtInt+1, retval, spec, dataList, deltaDataList, deltaDim, addNewPointsInUpdate, vehicleInfo.getVehicleId(), vehicleParam, vehicleInfo);
			}
		}
		else {
			//GpsData prev = dataList.get(addAtInt);
			//prev.setSourceChannel(ChannelTypeEnum.BOTH);
		}
		return retval;
	}
	*/	
	/*CENT_CACHE_NOT_USED
	public static void identifyOutliers(Connection conn, GpsData data, VehicleModelInfo vehicleInfo, int startIndex, int endIndexExcl, int suppWinSize, ArrayList<Integer> toRemoveIndices) {
		int dataDimId = data.getDimId();
		ModelSpec spec = vehicleInfo.getModelSpec(conn, dataDimId);
		VehicleSpecific vehicleParam = vehicleInfo.getVehicleParam(conn, dataDimId);
		ArrayList<Double> valsList = new ArrayList<Double>();
		NewVehicleData dataList = vehicleInfo.getDataList(conn, dataDimId);
		int valsStart = Integer.MAX_VALUE;
		
		for (int i=startIndex-suppWinSize,is = endIndexExcl+suppWinSize; i<is; i++) {
			GpsData pt = dataList.get(i);
			if (pt != null) {
				valsList.add(pt.getSpeed());
				if (valsStart > i)
					valsStart = i;
			}
		}
		helperGetOutliers(valsList, spec.getResetAbs()*(vehicleParam == null ? 1 : vehicleParam.getScale()), suppWinSize, toRemoveIndices, valsStart);
	}
	
	public static void helperGetOutliers(ArrayList<Double> valsList, double gapThresh, int suppWinSize, ArrayList<Integer> toRemoveIndices, int baseIndex) {
		int sz = valsList.size();
		if (sz < (2*suppWinSize+1))
			return;
		double leftSumm = 0;
		double rtSumm = 0;
		for (int i=0;i<suppWinSize;i++) {
			leftSumm += valsList.get(i);
			rtSumm += valsList.get(suppWinSize+1+i);
		}
		
		for (int i=suppWinSize, is=sz-suppWinSize-1;i<is;i++) {
			double v = valsList.get(i);
			double lavg = leftSumm/(double)suppWinSize;
			double ravg = leftSumm/(double)suppWinSize;
			double lgap = v-lavg;
			double rgap = v-ravg;
			if ( ((lgap >= 0 && rgap >= 0) || (lgap <= 0 && rgap <= 0)) && Math.abs(lgap) > gapThresh && Math.abs(rgap) > gapThresh) {
				toRemoveIndices.add(i+baseIndex);
			}
			if (i == is-1)
				break;
			leftSumm = leftSumm - valsList.get(i-suppWinSize)+valsList.get(i+1);
			rtSumm = rtSumm - valsList.get(i+1)+valsList.get(i+1+suppWinSize+1);
		}
	}
	
	*/
	
	public static  ArrayList<GpsData> handleNewPointSimple(Connection conn, GpsData data, VehicleModelInfo vehicleInfo, boolean addNewPointsInUpdate, ArrayList<GpsData> toRemove, boolean justAdd) throws Exception {// returns the index atWhich added if new, else -1. Assumes check done that we are not going back in time & that modelSpec is non-null (as verified thru addSimple
		return new ArrayList<GpsData>();
		/*DEBUG13
		//return val: 1st = arraylist of indices in datalist that have changed or impacted
		//                   2nd = add this in changeIndices to detectLevelChange
		//why: basically if currV same as estimated value we dont add in retval.first ... but we still need to check for levelchange
		
		int medianWindow = vehicleInfo.m_medianWindow;
		ArrayList<GpsData> retval = new ArrayList();
		int retvalSecond = -1;
//		Date gpsTime = data.getGps_Record_Time();
//		Date recvTime = data.getGpsRecvTime();
		int dimId = data.getDimId();
		
		ModelSpec spec = vehicleInfo.getModelSpec(conn, dimId);
		boolean doingMedian = spec.modelType ==ModelSpec.MEDIAN_PLUS;
		
		NewVehicleData dataList = vehicleInfo.getDataList(conn, dimId);
		if (dataList == null)
			return null;
		VehicleSpecific vehicleParam = vehicleInfo.getVehicleParam(conn, dimId);
		NewVehicleData deltaDataList = null;
		Dimension deltaDim = Dimension.getDimInfo(spec.deltaDimId);
		if (deltaDim != null) {
			deltaDataList = vehicleInfo.getDataList(conn, spec.deltaDimId);
		}
		NewVehicleData ignDataList = null;
		ignDataList = vehicleInfo.getDataList(conn, ApplicationConstants.IGN_ONOFF);
		double rawVal = data.getSpeed();
		
		
		////CENT_CACHE_DOWN_BUT_CHECK Pair<GpsData, Boolean> added = dataList.add(conn, data);
		dataList.add(conn, data);
		//CENT_CACHE_DOWN_BUT_CHECK boolean exists = added.second;
		boolean exists = false;
		boolean isAddAtEnd = dataList.isAtEnd(data);
		GpsData mpMarker = dataList.getMpMarker();
		
		boolean isOutlier = spec.toIgnore(rawVal, vehicleParam);
		
		if (isAddAtEnd && mpMarker != null) {//sync to end ..
			int relStart = 0;
			if (VehicleModelInfo.g_tryToDoForwardMedian)
				relStart -= medianWindow/2;
			GpsData prev = null;
			for (prev = dataList.get(conn, mpMarker, relStart++); prev == null && relStart <= 0;prev = dataList.get(conn, mpMarker, relStart++)) {
				//yes do nothing
			}
			GpsData prevDeltaData = null;
			if (prev != null && !spec.deltaByTime && deltaDataList != null)
				prevDeltaData =  deltaDataList.get(conn, prev);
			for (GpsData curr = dataList.get(conn, mpMarker, relStart++); curr != null; curr = dataList.get(conn, mpMarker, relStart++)) {
				ModelState currState = curr.getModelState(spec);
				double oldv = currState.getX1();
				double newv = oldv;
				if (doingMedian) {

					Pair<Boolean, Double> resetMedian = ModelProcessor.helpSetMedian(conn, curr, vehicleInfo, dataList, spec, vehicleParam, VehicleModelInfo.g_tryToDoForwardMedian);
					double median = resetMedian.second;
					currState = spec.next(prev == null ? null : prev.getModelState(spec), median, Misc.getUndefDouble(),vehicleParam);
				}
				else {
					GpsData currDeltaData = deltaDataList == null ? null : deltaDataList.get(conn, curr);
					double delta = spec.getDelta(prev, curr, prevDeltaData, currDeltaData, deltaDim.isCummulative());
					prevDeltaData = currDeltaData;
					double v = curr.getSpeed();
					currState = spec.next(prev == null ? null : prev.getModelState(spec), v, delta,vehicleParam);
				}
				newv = currState.getX1();
				if (!Misc.isEqual(newv, oldv, spec.tolForparamIsSame, 0.00001)) {//TBD - should we mark it as not even go forward re syncing currently just marking it as not requiring change in val
					retval.add(curr);
					//System.out.println("[MP] - MS:Resync "+vehicleInfo.getVehicleId()+" at:"+addAtInt+1+" Time:"+curr.getGps_Record_Time()+" state:"+currState.toString());
				}
				curr.setValue(newv);
				curr.setModelState(currState);
				prev = curr;
			}	
			dataList.setMpMarker(null);
		}
		
		if (!exists) {
			ModelState currState = data.getModelState(spec);
			if (!justAdd) {	
				GpsData prev = dataList.get(conn, data, -1);
				if (doingMedian) {
					
					double median = Misc.getUndefDouble();
					if (isOutlier) {
						if (prev != null && prev.getModelState(spec) != null)
							median = prev.getModelState(spec).getX1();
					}
					if (Misc.isUndef(median)) {
						Pair<Boolean, Double> resetMedian = ModelProcessor.helpSetMedian(conn, data, vehicleInfo, dataList, spec, vehicleParam, false);
						median = resetMedian.second;
					}
					currState = spec.next(prev == null ? null : prev.getModelState(spec), median, Misc.getUndefDouble(),vehicleParam);
				}
				else {
					GpsData prevDeltaData = null;
					if (prev != null && !spec.deltaByTime && deltaDataList != null)
						prevDeltaData =  deltaDataList.get(conn, prev);
					GpsData currDeltaData = deltaDataList == null ? null : deltaDataList.get(conn, data);
					double delta = spec.getDelta(prev, data, prevDeltaData, currDeltaData, deltaDim.isCummulative());
					prevDeltaData = currDeltaData;
					
					currState = prev == null && data.getModelState(spec) != null ? data.getModelState(spec) : spec.next(prev == null ? null : prev.getModelState(spec), rawVal, delta, vehicleParam);
				}
				data.setModelState(currState);
			}
			double oldv = data.getValue();
			double newv = currState.getX1();
			boolean isequal = Misc.isEqual(oldv, newv, spec.tolForparamIsSame, 0.00001);
			data.setValue(currState.getX1());
			if (addNewPointsInUpdate) {
				if (!isequal) {
					retval.add(data); //we will only send changes in existing data
				}
				if (isOutlier) {
					toRemove.add(data);
				}
			}
			//System.out.println("[MP] - MS: "+vehicleInfo.getVehicleId()+" at:"+addAtInt+1+" Time:"+data.getGps_Record_Time()+" state:"+currState.toString());
			if (!isAddAtEnd)
				dataList.setMpMarker(data);
			//now check for modelReset
			if (!justAdd && !isOutlier && !doingMedian) {//although doesn't really matter ...
				//CENT_CACHE_TODO_LATER handleModelReset(addAtInt+1, retval, spec, dataList, deltaDataList, deltaDim, addNewPointsInUpdate, vehicleInfo.getVehicleId(), vehicleParam, vehicleInfo);
			}
		}
		else {
			//GpsData prev = dataList.get(addAtInt);
			//prev.setSourceChannel(ChannelTypeEnum.BOTH);
		}
		return retval;
		DEBUG13 */
	}
	
	/* CENT_CACHE_TODO_MAKE_SUITABKE_FOR_RELATIVE
	
	public static void handleModelReset(int indexOfPointAdded, ArrayList<Integer> changeIndices, ModelSpec spec, NewVehicleData dataList, VehicleModelInfo.Data deltaDataList, Dimension deltaDim, boolean addNewPointsInUpdate, int vehicleId, VehicleSpecific vehicleParam, VehicleModelInfo vehicleInfo) {
		int medianWindow = vehicleInfo.m_medianWindow;
		GpsData pt = dataList.get(indexOfPointAdded);
		GpsData prevPt = dataList.get(indexOfPointAdded-1);
		boolean doingMedian = spec.modelType == ModelSpec.MEDIAN_PLUS;
		boolean resetBecauseOfTimeGap = pt != null && prevPt != null && spec.resetBecauseOfGap(prevPt.getGps_Record_Time(), pt.getGps_Record_Time());
		if (resetBecauseOfTimeGap || spec.mayNeedReset(pt.getValue(), pt.getSpeed(), vehicleParam)) {//pt changed inside
			int checkTill = indexOfPointAdded-spec.ptsToLookBackForReset+1;
			if (resetBecauseOfTimeGap) {//we dont need to go back
				checkTill = indexOfPointAdded;
			}
			else {
				boolean dir = pt.getValue() - pt.getSpeed() > 0;
				
				if (checkTill < 0)
					return; //not enough point to check for model reset
				for (int i=indexOfPointAdded-1;i>=checkTill;i--) {
					pt = dataList.get(i);
					boolean tempDir = pt.getValue() - pt.getSpeed() > 0;
					if (tempDir != dir || !spec.mayNeedReset(pt.getValue(), pt.getSpeed(), vehicleParam)) {
						return;
					}
				}
			}
			if (doingMedian) {
				//check if there are no reset in the past window ... else we
				for (int k=1,ks = medianWindow;k<ks;k++) {
					GpsData td = dataList.get(checkTill-k);
					if (td.getModelState() != null && td.getModelState().hasReset)
						return; //no need to reset
				}
			}
			pt = dataList.get(checkTill);
			GpsData prevDeltaData = null;
			if (!spec.deltaByTime && deltaDataList != null)
				prevDeltaData =  deltaDataList.get(pt);
			if (resetBecauseOfTimeGap) {
				System.out.println("[MP] Resetting because time gap for vehicle : "+vehicleId+ " Data:"+pt.toString()+ " Model est:"+pt.getModelState().getX1() +" Prev Pt:"+prevPt.toString());
			}
			else {
				System.out.println("[MP] Resetting because excess residue for vehicle: "+vehicleId+ " Data:"+pt.toString()+ " Model est:"+pt.getModelState().getX1());
			}
			if (doingMedian) {//TBD ... needed to get slope 
				pt.getModelState().hasReset = true;
				return;
			}
			double v = pt.getSpeed();
			spec.reinit(v, pt.getModelState(), vehicleParam);
			int insertPosInChangeIndices = 0;
			for (int i=checkTill;i<=indexOfPointAdded;i++){ //starting from checkTill instead of one forward so as to include the effect of reinit
				GpsData curr = dataList.get(i);
				ModelState currState = curr.getModelState();
				if (curr == null)
					continue;
				if (i > checkTill) { //checkTill already done
					if (doingMedian) {
						
							Pair<Boolean, Double> resetMedian = ModelProcessor.helpSetMedian(curr, i, vehicleInfo, dataList, spec, vehicleParam, true);
							double median = resetMedian.second;
							currState = spec.next(pt == null ? null : pt.getModelState(), median, Misc.getUndefDouble(),vehicleParam);
					}
					else {
						double tempv = curr.getSpeed();
						GpsData currDeltaData = deltaDataList == null ? null : deltaDataList.get(curr);
						double delta = spec.getDelta(pt, curr, prevDeltaData, currDeltaData, deltaDim.isCummulative());
						prevDeltaData = currDeltaData;
						currState = spec.next(pt == null ? null : pt.getModelState(), tempv, delta, vehicleParam);
					}
				}
				double newv = currState.getX1();
				double oldv = curr.getValue();
				//System.out.println("[MP] - MS:Reset "+vehicleId+" at:"+i+" Time:"+curr.getGps_Record_Time()+" state:"+currState.toString());
				if (!Misc.isEqual(newv, oldv, spec.tolForparamIsSame, 0.00001)) {//TBD - should we mark it as not even go forward re syncing currently just marking it as not requiring change in val
					//i != indexOfPointAdded becase not interested in freshly added points
					if (i != indexOfPointAdded || addNewPointsInUpdate) {
						//add the changes in  changeIndices in ordered manner
						for (int js = changeIndices.size();insertPosInChangeIndices<js;insertPosInChangeIndices++) {
							if (changeIndices.get(insertPosInChangeIndices) >= i)
								break;
						}
						if (insertPosInChangeIndices  == changeIndices.size() || changeIndices.get(insertPosInChangeIndices) != i) {
							if (insertPosInChangeIndices == changeIndices.size()) {
								changeIndices.add(i);
							}
							else {
								changeIndices.add(null);
								for (int j=changeIndices.size()-2;j >= insertPosInChangeIndices;j--) {//-2 ... because added null at end to create space
									changeIndices.set(j+1, changeIndices.get(j));
								}
								changeIndices.set(insertPosInChangeIndices, i);
							}
						}//if change indices does not already exist
					}//if to add to change indixes
				}//vals are different
				curr.setValue(newv);
				curr.setModelState(currState);
				pt = curr;
			}
		}
		
	}
	*/
	public static  class RecoveryConfig {
	    public Date start = null;
	    public Date end = null;
	    public ArrayList<Integer> org = null;
	    public boolean todoRedo = false;
	    public boolean generateStats = false;
	    public ArrayList<Integer> vehicleIDs = null;
	    public ArrayList<Integer> traceVehicleIDs = null;
	    public ArrayList<Integer> attributeIDs = null;
	    private Document configXML = null;
		private static String g_configFileName = "model_redo.xml";
		private boolean lazyCache = true;
		
		public boolean noInitStateInInitRecovery() {
			return Misc.isInList(org, Misc.G_TOP_LEVEL_PORT) && end == null && todoRedo;
		}
		
		public boolean ignoreInitStateInRedo() {
			return end == null && todoRedo;
		}

		public static RecoveryConfig read() {
	    	RecoveryConfig retval = new RecoveryConfig();
	    	FileInputStream inp = null;
	    	try {

	    		inp = new FileInputStream(Misc.getServerConfigPath()+System.getProperty("file.separator")+g_configFileName);
	            MyXMLHelper test = new MyXMLHelper(inp, null);
	            retval.configXML = test.load();
	            Element elem = retval.configXML.getDocumentElement();
	            inp.close();
	            inp = null;
	            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            if (elem != null) {
	                retval.todoRedo =  "1".equals(elem.getAttribute("redo")); 
	                retval.generateStats = "1".equals(elem.getAttribute("stats"));
	                retval.lazyCache = !"0".equals(elem.getAttribute("lazy_cache"));
	                retval.start = Misc.getParamAsDate(elem.getAttribute("start"), null, formatter);
	                retval.end = Misc.getParamAsDate(elem.getAttribute("end"), null, formatter);
	                //retval.org = Misc.getParamAsInt(elem.getAttribute("org"));
	                String orgs = elem.getAttribute("org");
	                if (orgs != null && orgs.length() > 0)  {
	                	retval.org = new ArrayList<Integer>();
	                	Misc.convertValToVector(orgs, retval.org);
	                }
	                if (retval.org == null || retval.org.size() == 0) {
	                	String ids = elem.getAttribute("id");
	                	if (ids != null && ids.length() > 0) {
	                		retval.vehicleIDs = new ArrayList<Integer>();
	                		Misc.convertValToVector(ids, retval.vehicleIDs);
	                	}
	                }
	                String ids = elem.getAttribute("attrib_id");
	            	if (ids != null && ids.length() > 0) {
	            		retval.attributeIDs = new ArrayList<Integer>();
	            		Misc.convertValToVector(ids, retval.attributeIDs);
	            		if (retval.attributeIDs.size() == 0)
	            			retval.attributeIDs = null;
	            	}
	            	String traceVehicleIds = elem.getAttribute("trace_vehicle_id");
	            	if (traceVehicleIds != null && traceVehicleIds.length() != 0) {
	            		retval.traceVehicleIDs = new ArrayList<Integer>();
	            		Misc.convertValToVector(traceVehicleIds, retval.traceVehicleIDs);
	            	}
	                if (retval.vehicleIDs != null && retval.vehicleIDs.size() == 0)
	                	retval.vehicleIDs = null;
	                if (retval.org != null && retval.org.size() == 0)
	                	retval.org = null;
	                //if (retval.todoRedo && retval.vehicleIDs == null && Misc.isUndef(retval.org))
	                //	retval.org = Misc.G_TOP_LEVEL_PORT;
	            }
	            inp = null;
	            test = null;
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    		//eat it
	    	}
	    	finally {
	    		if (inp != null) {
	    			try {
	    				inp.close();
	    			}
	    			catch (Exception e1) {
	    				e1.printStackTrace();
	    				//eat it
	    			}
	    		}
	    	}
	    	return retval;
	    }
	    
		public static void  readAndReset() {
			RecoveryConfig retval = read();
			retval.reset();
		}
		
	    public void reset() {
	    	//if (true) return;
	    	FileWriter fout = null;
	        PrintWriter outw = null;
	        try {
		    	if (configXML != null && todoRedo) {
		            Element elem = configXML.getDocumentElement();
		            elem.setAttribute("redo","0");
		            fout = new FileWriter(Misc.getServerConfigPath()+System.getProperty("file.separator")+g_configFileName);
		            outw = new PrintWriter(fout, true);
		            com.ipssi.gen.utils.MyXMLHelper helper = new com.ipssi.gen.utils.MyXMLHelper(null,outw);
		            helper.save(configXML);
		            outw.close();
		            outw = null;
		            fout.close();
		            fout = null;
		    	}
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	        	//eat it
	        }
	        finally {
	        	try {
	        		if (outw != null)
	        			outw.close();
	        		if (fout != null)
	        			fout.close();
	        	}
	        	catch (Exception e1) {
	        		e1.printStackTrace();
	        		//eat it
	        	}
	        }
	    }
	}
	
	public static boolean prepForLoad(Connection conn, RecoveryConfig recoveryConfig, ArrayList<Integer> vehicleIDs) throws Exception {//returns true if there are any dimension with model
		boolean hasDimWithModel = false;
		//1. get list of attributes that have models specified in temp_ .... will be used to do join etc later
		PreparedStatement ps = conn.prepareStatement(DBQueries.CLEAN_TEMP_ATTRIB_WITH_MODEL);
		ps.execute();
		ps.close();
		
	    Collection<Dimension> dimList = Dimension.getDimList();
	    //TODO later ... get the order in which attributes must be obtained ... currently same as dimid
	    StringBuilder dimListStr = new StringBuilder();
	    ps = conn.prepareStatement(DBQueries.INSERT_IN_TEMP_ATTRIB_WITH_MODEL);
	    for (Dimension dim:dimList) {
	    	if (dim.modelSpec == null || dim.getId() == 0)
	    		continue;
	        ps.setInt(1, dim.getId());
	        ps.setInt(2, dim.getId());
	        ps.setInt(3, dim.getId());
	        if (dimListStr.length() != 0)
	        	dimListStr.append(",");
	        dimListStr.append(dim.getId());
	        ps.addBatch();
	        if (dim.modelSpec.deltaDimId >= 0 && !dim.modelSpec.deltaByTime) {
	        	ps.setInt(1, dim.modelSpec.deltaDimId);
		        ps.setInt(2, dim.getId());
		        ps.setInt(3, dim.modelSpec.deltaDimId);
//		        if (dimListStr.length() != 0)
//		        	dimListStr.append(",");
//		        dimListStr.append(dim.modelSpec.deltaDimId);
		        ps.addBatch();
		        
		        ps.setInt(1, ApplicationConstants.IGN_ONOFF); //ign
		        ps.setInt(2, dim.getId());
		        ps.setInt(3, ApplicationConstants.IGN_ONOFF);
		        ps.addBatch();
	        }
	        hasDimWithModel = true;
	    }
	    ps.executeBatch();
	    ps.close();
	    if (!hasDimWithModel)
	    	return false;
	    
	    //2.  Now remove entries from modelstate which are no longer modeled
	    ps = conn.prepareStatement("delete from model_state where attribute_id not in ("+dimListStr+")");
	    ps.execute();
	    ps.close();
	    
	    //3. Now ensure that there is an entry in model_state for each vehicle and attrib of interest
	    ps = conn.prepareStatement(DBQueries.INSERT_MISSING_VEHICLE_MODEL);
	    ps.execute();
	    ps.close();
	    
	    //4. Now for vehicles requiring redo, set object and saved_at to null;
	    if (recoveryConfig.todoRedo) {
	    	StringBuilder query = new StringBuilder();
	    	query.append("update model_state join vehicle on (model_state.vehicle_id = vehicle.id and vehicle.status = 1 ");
	    	if (recoveryConfig.attributeIDs != null && recoveryConfig.attributeIDs.size() != 0) {
	    		query.append(" and model_state.attribute_id in (");
	    		Misc.convertInListToStr(recoveryConfig.attributeIDs, query);
	    		query.append(")");
	    	}
	    	if (recoveryConfig.vehicleIDs != null && recoveryConfig.vehicleIDs.size() != 0) {
	    		query.append(" and vehicle.id in (");
	    		Misc.convertInListToStr(recoveryConfig.vehicleIDs, query);
	    		query.append(")");
	    	}
	    	if (vehicleIDs != null && vehicleIDs.size() != 0) {
	    		query.append(" and vehicle.id in (");
	    		Misc.convertInListToStr(vehicleIDs, query);
	    		query.append(")");
	    	}
	    	query.append(") ");
	    	if (recoveryConfig.org != null && recoveryConfig.org.size() != 0) {
	    		query.append(" join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.id in (");
	    		Misc.convertInListToStr(recoveryConfig.org, query);
	    		query.append(")");
	    		query.append(" and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ");
	    	}
	    	query.append(" set object=null, saved_at = ?, firstpoint = ? ");
	    	ps = conn.prepareStatement(query.toString());
	    	Timestamp startTS = Misc.utilToSqlDate(recoveryConfig.start == null ? new Date(90,0,1) : recoveryConfig.start);
	    	ps.setTimestamp(1, startTS);
	    	ps.setTimestamp(2, startTS);
	    	ps.execute();
	    	ps.close();
	    }
	    return true;
	}
	
	private static class ModelStateLoadInfo {
		public Date savedAt = null;
		public Date firstPoint = null;
		public ModelState modelState = null;
		public ModelStateLoadInfo(Date savedAt, Date firstPoint, ModelState modelState) {
			this.savedAt = savedAt;
			this.firstPoint = firstPoint;
			this.modelState = modelState;
		}
	}
	private static HashMap<Pair<Integer, Integer>, ModelStateLoadInfo> loadInitModelState(Connection conn, ArrayList<Integer> vehicleIds) throws Exception {
		HashMap<Pair<Integer, Integer>, ModelStateLoadInfo> retval = new HashMap<Pair<Integer, Integer>, ModelStateLoadInfo>();
		StringBuilder query = new StringBuilder();
		query.append(DBQueries.GET_MODEL_STATE);
		if (vehicleIds != null && vehicleIds.size() != 0) {
			query.append(" where model_state.vehicle_id in (");
			Misc.convertInListToStr(vehicleIds, query);
			query.append(") ");
		}
		PreparedStatement ps = conn.prepareStatement(query.toString());
		ResultSet rs = ps.executeQuery();
		while (rs.next())	{
			int vehicleId = rs.getInt("vehicle_id");
			int attribute_id = rs.getInt("attribute_id");
			Timestamp firstPoint = rs.getTimestamp("firstpoint");
			Timestamp savedAt = rs.getTimestamp("saved_at");
			byte[] obj = (byte[]) rs.getObject("object");
			ModelState data = null;
			if (obj != null && obj.length != 0) {
				ByteArrayInputStream bais = new ByteArrayInputStream(obj);
				ObjectInputStream in = new ObjectInputStream(bais);
				data = (ModelState)in.readObject();
				in.close();
			}
			else {
				data = null;				   
			}
			retval.put(new Pair<Integer, Integer>(attribute_id, vehicleId), new ModelStateLoadInfo(Misc.sqlToUtilDate(savedAt), Misc.sqlToUtilDate(firstPoint), data));
		}
		rs.close();
		ps.close();
		return retval;
	}
	
	public static void starterBuildCache(Connection conn) throws Exception {
		/*DEBUG13
		RecoveryConfig recoveryConfig = RecoveryConfig.read();
		VehicleModelInfo.loadVehicleSpecificModelSpec(conn, null);
		prepForLoad(conn, recoveryConfig, null);
		if (!g_doLazyCacheBuild && !recoveryConfig.lazyCache)
			buildCache(conn, null);
		recoveryConfig.reset(); //TODO
		*/
	}
	public static void buildCache(Connection conn, ArrayList<Integer> vehicleIds) throws Exception {
		/*DEBUG13
		boolean generateStats = false;
		HashMap<Pair<Integer, Integer>, ModelStateLoadInfo> initStates =  loadInitModelState(conn, vehicleIds);
		LevelChangeUtil.loadChangeList(conn, vehicleIds);
		
		StringBuilder query = new StringBuilder();
		query.setLength(0);
		query.append(DBQueries.GET_GPS_DATA_REDO_1);
		if (vehicleIds != null && vehicleIds.size() != 0) {
			query.append(" where model_state.vehicle_id in (");
			Misc.convertInListToStr(vehicleIds, query);
			query.append(") ");
		}
		query.append(" group by  model_state.vehicle_id, temp_attrib_with_model.askdim_id order by model_state.vehicle_id, temp_attrib_with_model.askdim_id ");
		query.append(DBQueries.GET_GPS_DATA_REDO_2);
		PreparedStatement ps = conn.prepareStatement(query.toString());
		ResultSet rs = ps.executeQuery();
		GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
		int cnt = 0;
		Vehicle vehicle = null;
		
		ArrayList<GpsData> dataHolder = new ArrayList<GpsData>(1);
		dataHolder.add(null);
		
		PreparedStatement psUpdateData = conn.prepareStatement(DBQueries.NEW_UPDATE_DATA_INFO);
		int prevVehicleId = Misc.getUndefInt();
		int prevDimId = Misc.getUndefInt();
		Dimension dimInfo = null;
		VehicleModelInfo vehicleModelInfo = null;
		ModelStateLoadInfo loadInfo = null;
		boolean justAdd = true;
		int ptsAdded = 0;
		boolean toSetState = true;
		int batchSize = 1000;
		long prevTime = Misc.getUndefInt();
		while ((vehicle = reader.readGpsData()) !=null) {
			try {			
				int vehicleId = vehicle.getId();
				GpsData data = vehicle.getGpsData();
				//data.setName(null);
				int dimId = data.getDimId();
				if (vehicleId != prevVehicleId) {
					if (!Misc.isUndef(prevVehicleId)) {
						//CENT_CACHE vehicleModelInfo.clearUnneeded(conn, true, generateStats, true);
						vehicleModelInfo.saveState(conn,true);
						//CENT_CACHE_DOWNGRADE if (generateStats) {
						//CENT_CACHE_DOWNGRADE 	vehicleModelInfo.saveStats(conn);
						//CENT_CACHE_DOWNGRADE }
						System.out.println("[MP] Recovery of vehicle End "+ prevVehicleId+" Count:"+cnt);
					}
					System.out.println("[MP] Recovery of vehicle Start "+vehicleId);
					if (false) {//for dbg
						PreparedStatement ps1 = conn.prepareStatement("delete from debug_fuel where vehicle_id = ? and (at_beg_time >= ? or at_beg_time is null)");
						ps1.setInt(1, vehicleId);
						ps1.setTimestamp(2, Misc.longToSqlDate(data.getGps_Record_Time()));
						ps1.execute();
						ps1.close();
					}
					cnt = 0;
					vehicleModelInfo = VehicleModelInfo.getVehicleModelInfo(null, vehicleId);
				}
				if (cnt > 0 && cnt % 10000 == 0) {
					System.out.println("[MP] Recovery of vehicle Ongoing "+vehicleId + " Count:"+cnt);
				}
				if (vehicleId != prevVehicleId)
					prevTime = Misc.getUndefInt();
				if (vehicleId != prevVehicleId || dimId != prevDimId) {
					dimInfo = Dimension.getDimInfo(dimId);
					loadInfo = initStates.get(new Pair<Integer, Integer>(dimId, vehicleId));
					toSetState = loadInfo != null;;
				}
				if ((prevVehicleId != vehicleId && ptsAdded > 0) || ptsAdded > batchSize) {
					System.out.println("[MP] Recovery of vehicle - updating data "+ (ptsAdded <= batchSize ? prevVehicleId : vehicleId) + " Count:"+ptsAdded);
					try {
					psUpdateData.executeBatch();
					psUpdateData.clearBatch();
					}
					catch (Exception e) {
						e.printStackTrace();
						
					}
					System.out.println("[MP] Recovery of vehicle - updating data done "+ (ptsAdded <= batchSize ? prevVehicleId : vehicleId) + " Count:"+ptsAdded);
					ptsAdded = 0;
				}
				justAdd = false; //loadInfo == null || loadInfo.savedAt == null || !data.getGps_Record_Time().after(loadInfo.savedAt);
				if (toSetState && loadInfo != null && loadInfo.modelState != null && loadInfo.savedAt != null && !(loadInfo.savedAt.getTime() > (data.getGps_Record_Time()))) {
					toSetState = false;
					data.setModelState(loadInfo.modelState);
					initStates.remove(new Pair<Integer, Integer>(dimId, vehicleId));
					justAdd = true;
				}
				//dataHolder.add(data);
				//if (prevTime != data.getGps_Record_Time()) {
				//	boolean added = handleMultiplePoints(conn, dataHolder, vehicleModelInfo, psUpdateData, justAdd, false, false, generateStats, true);//conn is null so as to enable build of cache
				//	if (added)
				//		ptsAdded++;
				//	dataHolder.clear();
				//}
				dataHolder.set(0, data);
				boolean added = handleMultiplePoints(conn, dataHolder, vehicleModelInfo, psUpdateData, justAdd, false, false, generateStats, false);//conn is null so as to enable build of cache
				if (added)
					ptsAdded++;
				cnt++;
				prevVehicleId = vehicleId;
				prevDimId = dimId;
				prevTime = data.getGps_Record_Time();
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
		}
		if (ptsAdded > 0) {
			System.out.println("[MP] Recovery of vehicle - updating data "+ prevVehicleId + " Count:"+ptsAdded);
			psUpdateData.executeBatch();
			psUpdateData.clearBatch();
		}
		rs.close();
		ps.close();
		psUpdateData.close();
		if (!Misc.isUndef(prevVehicleId)) {
			//CENT_CACHE vehicleModelInfo.clearUnneeded(conn, true, generateStats, true);
			vehicleModelInfo.saveState(conn,true);
		//CENT_CACHE_DOWNGRADE	if (generateStats) {
		//		vehicleModelInfo.saveStats(conn);
		//	}
			System.out.println("[MP] Recovery of vehicle End "+prevVehicleId+" Count:"+cnt);
			cnt = 0;
		}
		*/
	 }
	 /*DEBUG13
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn.setAutoCommit(true); // for debug
			ModelProcessor.starterBuildCache(conn);
			VehicleModelInfo vehicleInfo = null;
			int vehicleId = 17145;//15390; 16707 = 5801 bhushan, 17266 = 1831390 and 17754 = 1811743, 17409 = vectra	
			//17092=9836
			boolean doAll = false;
			if (!doAll) {
				if (true) {
					//vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17148);
					//vehicleInfo.saveState(conn, true);
					//vehicleInfo.remove();
					//vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17148);
					//vehicleInfo.saveState(conn, true);
					//vehicleInfo.remove();
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 18587);
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
			}
			else {
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 18587);
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 18586);
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (true) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 16707);//5801
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (true) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17144);//5802
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17145);//1500
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17147);//0817
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17148);//5803
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17149);//5780
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17290);//6454
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17358);//8063
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
				if (false) {
					vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 17360);//7294
					vehicleInfo.saveState(conn, true);
					vehicleInfo.remove();
				}
			
			}
			
			if (true) {
				return;
			}
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15987);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15988);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15989);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15990);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15991);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15992);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15993);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15994);
			vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15995);
			
			if (true)
				return;
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15987);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15988);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15989);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15990);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15991);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15992);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15993);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15994);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
			try {
				vehicleInfo = VehicleModelInfo.getVehicleModelInfo(conn, 15995);
			}
			catch (Exception e1) {
				e1.printStackTrace();
				//eat it
			}
		} catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
			;
			// eat it
		} finally {
			try {
				if (conn.getAutoCommit()) { // mysql issue cant commit/rollback if autocommint
					conn.setAutoCommit(false);
				}
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
				// eat it
			}
		}
	}// end of main
	DEBUG13 */
}

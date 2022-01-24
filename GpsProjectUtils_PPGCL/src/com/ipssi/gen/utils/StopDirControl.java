package com.ipssi.gen.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.geometry.Point;
import com.ipssi.mapguideutils.ShapeFileBean;

public class StopDirControl {
	// FOR AMBUJA ... this must go in prod
	/*
	private double stopThreshSpeedKMPH = 5;
	private double mergeStopsSeqIfDistKM = 2.0;
	private double dirChangeDetectThreshKM = 0.3;
	private double dirChangeDetectThreshKMHi = 10;
	private double dirChangeDetectThreshKMSuper = 30;
	private boolean stopOpStationAllowedAfterLoad = true;
	private boolean stopOpStationAllowedAfterUnload = false;
	private double sameDirThresholdDegree = 30;
	private double stopThreshTobeOpstationIfDirMin = 25;
	private double stopThreshTobeOpstationMin = 60;
	private double mergeStopOpstationIfInKMRange = 1;
	private boolean doStopProcessing = false;
	private boolean doDirChangeProcessing = false;
	private boolean doInsideOpStation = false;
	private int dataGapThreshSec = 180;
	private double furthestRefLongitude = Misc.getUndefDouble();
	private double furthestRefLatitude = Misc.getUndefDouble();

	private boolean mergeStopIfEndIsAlsoClose = true; //for ambuja it should be true;
	private double minDistAfterWhichLoadUnloadPossibleKM = 0.1; //for mining it should be of the order of 4KM
	private boolean forProperLoDirMust = false;
	private boolean forProperHiDirMust = false;
	private boolean forProperBothDirMust = false;
	private double lookBackDistMaxThreshForWaitKM =0.180;// 0.02; //for mining
	private double lookBackDistBetweenStopKM = 0.080;//0.1; //for mining
	*/
	//30m/60m/60 degree reasonable
	//30m/60m/30 mostly lo dir missed
	private double dalaUpMaxDistKM = 0.09;
	private double stopThreshDistKM = 0.1; //0.023 for mines/coal theft
	private double stopThreshSpeedKMPH = 5;//2.4 for mines/coal theft; 
	private double mergeStopsSeqIfDistKM = 0.020;
	private double dirChangeDetectThreshKM = 0.15; //0.030 mining
	private double dirChangeDetectThreshKMHi = 0.3; //0.060 mining
	private double dirChangeDetectThreshKMSuper = 6; //0.2 mining
	//CHANGE15 private boolean stopOpStationAllowedAfterLoad = false;
	//CHANGE15 private boolean stopOpStationAllowedAfterUnload = false;
	private double sameDirThresholdDegree = 60;
	private double stopThreshTobeOpstationIfDirMin = 30; //0 mining
	private double stopThreshTobeOpstationMin = 60; //0 mining
	private double mergeStopOpstationIfInKMRange = 1; 
	private int doStopProcessing = 0;//0 => do nothing, 1 => do stop and use it only for recording, 2 => do stop and use it for LU Internally, 3=> All
	private int stopStopPreferredOpType = 2; // same as TripInfoConstants - 1 is load, 2 is unload
	private double minDistAfterWhichLoadUnloadPossibleKM = Misc.getUndefDouble(); //0.1 mining; //for mining it should be of the order of 4KM
	private double minDistAfterWhichLUPossibleIfDala = Misc.getUndefDouble(); //0.07 for mining
	private double superMinDistAfterWhichLoadUnloadPossibleKM = Misc.getUndefDouble();
	private boolean forProperLoDirMust = false;
	private boolean forProperHiDirMust = false;
	private boolean forProperBothDirMust = false;
	private double lookBackDistMaxThreshForWaitKM =15; //aca 0.180;// 0.02; //for mining 15 for laf, 3 otherwise
	private double lookBackDistBetweenStopKM = 10;//aca 0.080;//0.1; //for mining //10 for laf , 2 otherwise
	private double lookForwDistMaxThreshForWaitKM =15; //15 for laf, undef otherwise
	private double lookForwDistBetweenStopKM = 10;//15 for laf, undef otherwise
	private boolean doForwBackForRegularOp = false;
	private boolean doTravelDistanceInsteadOfGeo = false; //FOR laf false, true otherwise
	private double distSkipBeforeDirChangeCheck = 0.2;
	private double treatStopAsSameAsFixedIfDistLessThanKM = 3;
	private int prefToFixedLoad = 1; //0 => doesnt matter, 1 => to fixed, 2= fixed only if in range
	private int prefToFixedUnload = 0; //0 => doesnt matter, 1 => to fixed, 2= fixed only if in range

	//additional params from org ... to make it one place
	public double m_linkedGateWaitBoxMtr = 60;//ID_LINKED_VEHICLE_GATEWAIT_AREA_BOX_MTR = 10 
	public double m_linkedOpAreaBoxMtr = 15;//ID_LINKED_VEHICLE_OPAREA_BOX_MTR = 9
	public double m_linkedShiftDistExceedsMtr = 6;//ID_LINKED_VEHICLE_SHIFT_DIST_EXCEEDS_MTR = 11
	public boolean toSendMessage = false;//ID_TO_SEND_TRIP_MESSAGE_INT = 4
	public boolean m_luMustBeProper = false;//ID_DO_LU_MUST_BE_PROPER = 13 
	// Challan
	public int m_lookForChallan = 0;//ID_LOOK_FOR_CHALLAN = 26
	public boolean m_pickFurthestUnloadOpstation = false;//ID_INT_PICK_FURTHEST_UNLOAD_OPSTATION = 39
	public boolean getBestAmongstSameOpIdMultiLU = false; //ID_INT_GETBEST_AMONGS_SAME_OPID_MULTILU =40
	public boolean doMultiLoadMultiLU = false; //ID_INT_DO_MULTILOAD_MULTILU = 41
	public boolean doMultiUnloadMultiLU = false; //ID_INT_DO_MULTIUNLOAD_MULTILU = 42
	public int m_afterUnloadAuto = 0; //ID_INT_AFTER_UNLOAD_GEN_INSTR = 151
	public boolean m_doETA = false;//ID_INT_DOETA = 152
	public boolean m_doNewApproachForTrip = true;//ID_INT_TRIP_CALC_NEW_APPROACH = 155
	public int m_getNearestOpForStop = -1;//if -1 then dont get (default), if 0 then get of any type, else get nearest Opstation of sub type
	                                                                          //ID_INT_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE = 156
	public int m_getNearstSpecialLM = -1;//if 0 then dont get (default), 0 get of any type, ese get of nearest sub type
	                                                                        //ID_INT_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE = 157
	public double m_threshKMForNearestOp = 50;//ID_DOUBLE_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE_MAXDIST = 30 
	public double m_threshKMForNearestLM = 50;//ID_DOUBLE_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE_MAXDIST = 31
	public boolean m_useChallanInPreferredLU = true;//ID_STOPDIR_INT_USECHALLAN_FOR_LUBREAK_ID = 10029
	public boolean m_usePrevLUInPreferredLU = true;//ID_STOPDIR_INT_USEPREVLU_FOR_LUBREAK_ID = 10039
	public int newPickFirstLastLOrUMask = -1;//ID_INT_PICKFIRSTLASTLORU_MASK_NEW == 10032
	public int newShiftDateBy = 0; //0 = unload, 1 = load //ID_INT_SHIFTDATE_BY_NEW
	public int m_materialLookUpApproach = 0;//ID_MATERIAL_LOOKUP_APPROACH = 33; //val = 0 => none, val = 1 => APMDC style exhaustive
	public int doSpecialAlgoForBestSeq = 0; //1 for mines
	public int dataGapSec = 0; //<= 0 ... means igonore
	public boolean bestLUisValidExtremum = false;//ID_DO_EXTREMUM_VALID_LU == 7	
	public boolean setGinToWinInForwBack = true;
	public boolean removeLLU = true;
	public double dontRemoveLLIfDistExceeds = Misc.getUndefDouble();
	public boolean doTurnDirOnLOnly = true;
	public int skipTimeGapLessThanSec = 0; //0 or negative means dont skip
	public boolean lookupChallanDestAddress = true;
	public boolean lookupChallanSrcAddress = true;
	public boolean insertChallanDestAddress = true;
	public boolean insertChallanSrcAddress = true;
	public boolean saveTurnEngineEventId = true;
	public int challanByDeliveryDate = 0;
	public int doStrictChallanTimingMatch = 0;
	public int leftChallanLeewayMin = 60;
	public int rightChallanLeewayMax = 60;
	public boolean splitULIfNextIsUL = true;
	
	public int ignoreOpNearLandmarkTypes = Misc.getUndefInt();
	public double ignoreLandmarkDistThresh = 1.0;
	public int extStopDurSecComplete = Misc.getUndefInt();//STOP_EXT_DUR_SEC_COMPLETE
	public boolean doAllMultiLU = true;
	public double multiLULeadDistLb = Misc.getUndefDouble();
	public double multiLULeadDistUb = Misc.getUndefDouble();
	public int flatLUPickStrategy = 0;//0-chronological,1- NearestFirstIntermediate, 2-FarthestFirstIntermediates
	public boolean doMultiLUDeltaDist = false;
	public int challanIgnDistProvided = 0; //0 dont ign, 1 always ignore and use cdh_email_info
	public int monitorStopLoThreshSec = Misc.getUndefInt();
	public int monitorStopHiThreshSec = Misc.getUndefInt();
	public int monitorNoDataLoThreshSec = Misc.getUndefInt();
	public int monitorNoDataHiThreshSec = Misc.getUndefInt();
	public double monitorExcessLeadLoThreshKm = Misc.getUndefInt();
	public double monitorExcessLeadHiThreshKm = Misc.getUndefInt();
	public static StopDirControl getControl(TripParams tripParams, MiscInner.PortInfo portInfo) {
		StopDirControl retval = new StopDirControl();
		getControl(retval, tripParams, portInfo);
		return retval;
	}
	public static void getControl(StopDirControl retval, TripParams tripParams, MiscInner.PortInfo portInfo) {
		retval.dalaUpMaxDistKM = getDouble(OrgConst.STOPDIR_DALA_UP_MAX_DIST, tripParams, portInfo, retval.dalaUpMaxDistKM);
		retval.treatStopAsSameAsFixedIfDistLessThanKM = getDouble(OrgConst.ID_STOPDIR_THRESH_STOP_SAME_AS_FIXED_KM, tripParams, portInfo, retval.treatStopAsSameAsFixedIfDistLessThanKM);
		retval.stopThreshSpeedKMPH = getDouble(OrgConst.ID_STOPDIR_THRESH_SPEEDKMPH_DOUBLE, tripParams, portInfo, retval.stopThreshSpeedKMPH);
		retval.stopThreshDistKM = getDouble(OrgConst.ID_STOPDIR_THRESH_DISTKM_DOUBLE, tripParams, portInfo, retval.stopThreshDistKM);
		retval.mergeStopsSeqIfDistKM = getDouble(OrgConst.ID_STOPDIR_MERGE_STOP_KM_DOUBLE, tripParams, portInfo, retval.mergeStopsSeqIfDistKM);
		retval.dirChangeDetectThreshKM = getDouble(OrgConst.ID_STOPDIR_DIRCHANGE_LO_KM_DOUBLE, tripParams, portInfo, retval.dirChangeDetectThreshKM);
		retval.dirChangeDetectThreshKMHi = getDouble(OrgConst.ID_STOPDIR_DIRCHANGE_HI_KM_DOUBLE, tripParams, portInfo, retval.dirChangeDetectThreshKMHi);
		retval.dirChangeDetectThreshKMSuper = getDouble(OrgConst.ID_STOPDIR_DIRCHANGE_SUPER_KM_DOUBLE, tripParams, portInfo, retval.dirChangeDetectThreshKMSuper);
		retval.sameDirThresholdDegree = getDouble(OrgConst.ID_STOPDIR_SAMEDIR_DEGREE_DOUBLE, tripParams, portInfo, retval.sameDirThresholdDegree);
		retval.stopThreshTobeOpstationMin = getDouble(OrgConst.ID_STOPDIR_DURTHRESH_IFNODIR_MIN_DOUBLE, tripParams, portInfo, retval.stopThreshTobeOpstationMin);
		retval.stopThreshTobeOpstationIfDirMin = getDouble(OrgConst.ID_STOPDIR_DURTHRESH_IFDIR_MIN_DOUBLE, tripParams, portInfo, retval.stopThreshTobeOpstationIfDirMin);
		retval.mergeStopOpstationIfInKMRange = getDouble(OrgConst.ID_STOPDIR_MERGE_OPSTATION_KM_DOUBLE, tripParams, portInfo, retval.mergeStopOpstationIfInKMRange);
		retval.extStopDurSecComplete = getInt(OrgConst.STOP_EXT_DUR_SEC_COMPLETE, tripParams, portInfo, retval.extStopDurSecComplete);
		retval.prefToFixedLoad = getInt(OrgConst.ID_STOPDIR_ID_STOPDIR_PREF_TO_FIXED_LOAD_INT, tripParams, portInfo, retval.prefToFixedLoad);
		retval.prefToFixedUnload = getInt(OrgConst.ID_STOPDIR_ID_STOPDIR_PREF_TO_FIXED_UNLOAD_INT, tripParams, portInfo, retval.prefToFixedUnload);
		retval.doStopProcessing = getInt(OrgConst.ID_STOPDIR_DOSTOP_PROC_INT, tripParams, portInfo, retval.doStopProcessing);
		retval.doTravelDistanceInsteadOfGeo = getBoolean(OrgConst.ID_STOPDIR_FORWBACK_BYTRAVEL, tripParams, portInfo, retval.doTravelDistanceInsteadOfGeo);
		retval.stopStopPreferredOpType = getInt(OrgConst.ID_STOPDIR_PREFERRED_LUTYPE, tripParams, portInfo, retval.stopStopPreferredOpType);
		retval.doForwBackForRegularOp = getBoolean(OrgConst.ID_STOPDIR_FORWBACK_FOR_REG, tripParams, portInfo, retval.doForwBackForRegularOp);
		retval.forProperLoDirMust = getBoolean(OrgConst.ID_INT_STOP_VALID_ONLYIF_LOW_DIR, tripParams, portInfo, retval.forProperLoDirMust);
		retval.forProperHiDirMust = getBoolean(OrgConst.ID_INT_STOP_VALID_ONLYIF_HIGH_DIR, tripParams, portInfo, retval.forProperHiDirMust);
		retval.forProperBothDirMust = getBoolean(OrgConst.ID_INT_STOP_VALID_ONLYIF_BOTH_DIR, tripParams, portInfo, retval.forProperBothDirMust);
		retval.minDistAfterWhichLoadUnloadPossibleKM = getDouble(OrgConst.ID_STOP_MINDIST_LOADUNLOAD, tripParams, portInfo, retval.minDistAfterWhichLoadUnloadPossibleKM);
		retval.minDistAfterWhichLUPossibleIfDala = getDouble(OrgConst.ID_STOP_MINDIST_DALA_LOADUNLOAD, tripParams, portInfo, retval.minDistAfterWhichLUPossibleIfDala);
		retval.superMinDistAfterWhichLoadUnloadPossibleKM = getDouble(OrgConst.ID_STOP_SUPER_MINDIST_LOADUNLOAD, tripParams, portInfo, retval.superMinDistAfterWhichLoadUnloadPossibleKM);
		retval.lookBackDistMaxThreshForWaitKM = getDouble(OrgConst.ID_WAIT_LOOKBACK_MAXTHRESH_KM, tripParams, portInfo, retval.lookBackDistMaxThreshForWaitKM);
		retval.lookBackDistBetweenStopKM = getDouble(OrgConst.ID_WAIT_LOOKBACK_BETWEENSTOP_KM, tripParams, portInfo, retval.lookBackDistBetweenStopKM);
		retval.lookForwDistMaxThreshForWaitKM = getDouble(OrgConst.ID_WAIT_LOOKFORW_MAXTHRESH_KM, tripParams, portInfo, retval.lookForwDistMaxThreshForWaitKM);
		retval.lookForwDistBetweenStopKM = getDouble(OrgConst.ID_WAIT_LOOKFORW_BETWEENSTOP_KM, tripParams, portInfo, retval.lookForwDistBetweenStopKM);
		retval.distSkipBeforeDirChangeCheck = getDouble(OrgConst.ID_DIRCHANGE_SKIP_KM, tripParams, portInfo, retval.distSkipBeforeDirChangeCheck);
		retval.m_linkedGateWaitBoxMtr = getDouble(OrgConst.ID_LINKED_VEHICLE_GATEWAIT_AREA_BOX_MTR, tripParams, portInfo, retval.m_linkedGateWaitBoxMtr);
		retval.m_linkedOpAreaBoxMtr = getDouble(OrgConst.ID_LINKED_VEHICLE_OPAREA_BOX_MTR, tripParams, portInfo, retval.m_linkedOpAreaBoxMtr);
		retval.m_linkedShiftDistExceedsMtr = getDouble(OrgConst.ID_LINKED_VEHICLE_SHIFT_DIST_EXCEEDS_MTR, tripParams, portInfo, retval.m_linkedShiftDistExceedsMtr);
		retval.toSendMessage = getBoolean(OrgConst.ID_TO_SEND_TRIP_MESSAGE_INT, tripParams, portInfo, retval.toSendMessage);
		retval.m_luMustBeProper = getBoolean(OrgConst.ID_DO_LU_MUST_BE_PROPER, tripParams, portInfo, retval.m_luMustBeProper);
		retval.m_lookForChallan = getInt(OrgConst.ID_LOOK_FOR_CHALLAN, tripParams, portInfo, retval.m_lookForChallan);
		retval.m_pickFurthestUnloadOpstation = getBoolean(OrgConst.ID_INT_PICK_FURTHEST_UNLOAD_OPSTATION, tripParams, portInfo, retval.m_pickFurthestUnloadOpstation);
		retval.getBestAmongstSameOpIdMultiLU = getBoolean(OrgConst.ID_INT_GETBEST_AMONGS_SAME_OPID_MULTILU, tripParams, portInfo, retval.getBestAmongstSameOpIdMultiLU);
		retval.doMultiLoadMultiLU = getBoolean(OrgConst.ID_INT_DO_MULTILOAD_MULTILU, tripParams, portInfo, retval.doMultiLoadMultiLU);
		retval.doMultiUnloadMultiLU = getBoolean(OrgConst.ID_INT_DO_MULTIUNLOAD_MULTILU, tripParams, portInfo, retval.doMultiUnloadMultiLU);
		retval.m_afterUnloadAuto = getInt(OrgConst.ID_INT_AFTER_UNLOAD_GEN_INSTR, tripParams, portInfo, retval.m_afterUnloadAuto);
		retval.m_doETA = getBoolean(OrgConst.ID_INT_DOETA, tripParams, portInfo, retval.m_doETA);
		retval.m_doNewApproachForTrip = getBoolean(OrgConst.ID_INT_TRIP_CALC_NEW_APPROACH, tripParams, portInfo, retval.m_doNewApproachForTrip);
		retval.m_getNearestOpForStop = getInt(OrgConst.ID_INT_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE, tripParams, portInfo, retval.m_getNearestOpForStop);
		retval.m_getNearstSpecialLM = getInt(OrgConst.ID_INT_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE, tripParams, portInfo, retval.m_getNearstSpecialLM);
		retval.m_getNearstSpecialLM = getInt(OrgConst.ID_INT_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE, tripParams, portInfo, retval.m_getNearstSpecialLM);
		retval.m_threshKMForNearestOp = getDouble(OrgConst.ID_DOUBLE_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE_MAXDIST, tripParams, portInfo, retval.m_threshKMForNearestOp);
		retval.m_threshKMForNearestLM = getDouble(OrgConst.ID_DOUBLE_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE_MAXDIST, tripParams, portInfo, retval.m_threshKMForNearestLM);
		retval.m_useChallanInPreferredLU = getBoolean(OrgConst.ID_STOPDIR_INT_USECHALLAN_FOR_LUBREAK_ID, tripParams, portInfo, retval.m_useChallanInPreferredLU);
		retval.m_usePrevLUInPreferredLU = getBoolean(OrgConst.ID_STOPDIR_INT_USEPREVLU_FOR_LUBREAK_ID, tripParams, portInfo, retval.m_usePrevLUInPreferredLU);
		retval.newPickFirstLastLOrUMask = getInt(OrgConst.ID_INT_PICKFIRSTLASTLORU_MASK_NEW, tripParams, portInfo, retval.newPickFirstLastLOrUMask);
		retval.newShiftDateBy = getInt(OrgConst.ID_INT_SHIFTDATE_BY_NEW, tripParams, portInfo, retval.newShiftDateBy);
		retval.m_materialLookUpApproach = getInt(OrgConst.ID_MATERIAL_LOOKUP_APPROACH, tripParams, portInfo, retval.m_materialLookUpApproach);
		retval.bestLUisValidExtremum = getBoolean(OrgConst.ID_DO_EXTREMUM_VALID_LU, tripParams, portInfo, retval.bestLUisValidExtremum);
		retval.doSpecialAlgoForBestSeq = getInt(OrgConst.ID_INT_SPECIAL_ALGO_BEST_SEQ, tripParams, portInfo, retval.doSpecialAlgoForBestSeq);
		retval.setGinToWinInForwBack = getBoolean(OrgConst.ID_INT_SET_GIN_TO_WIN_FOR_FORWBACK, tripParams, portInfo, retval.setGinToWinInForwBack);
		retval.dataGapSec = getInt(OrgConst.ID_INT_DATA_GAP_CONSIDER_AS_START, tripParams, portInfo, retval.dataGapSec);
		
		retval.removeLLU = getBoolean(OrgConst.ID_INT_DOLLU_REMOVAL, tripParams, portInfo, retval.removeLLU);
		retval.dontRemoveLLIfDistExceeds = getDouble(OrgConst.ID_DOUBLE_DIST_NOREMOVE_LL, tripParams, portInfo, retval.dontRemoveLLIfDistExceeds);
		retval.doTurnDirOnLOnly = getBoolean(OrgConst.ID_INT_STOPDIR_RECORD_DIR_ON_LONLY, tripParams, portInfo, retval.doTurnDirOnLOnly);
		retval.skipTimeGapLessThanSec = getInt(OrgConst.ID_INT_STOPDIR_SKIP_TIID_INT_STOPDIR_SKIP_TIME_LESS_THANME_LESS_THAN, tripParams, portInfo, retval.skipTimeGapLessThanSec);

		retval.lookupChallanDestAddress = getBoolean(OrgConst.ID_INT_TRIPPARAM_DO_CDH_DEST_LOOKUP, tripParams, portInfo, retval.lookupChallanDestAddress);
		retval.lookupChallanSrcAddress = getBoolean(OrgConst.ID_INT_TRIPPARAM_DO_CDH_SRC_LOOKUP, tripParams, portInfo, retval.lookupChallanSrcAddress);
		retval.insertChallanDestAddress = getBoolean(OrgConst.ID_INT_TRIPPARAM_DO_CDH_DEST_INSERT, tripParams, portInfo, retval.insertChallanDestAddress);
		retval.insertChallanSrcAddress = getBoolean(OrgConst.ID_INT_TRIPPARAM_DO_CDH_SRC_INSERT, tripParams, portInfo, retval.insertChallanSrcAddress);
		retval.saveTurnEngineEventId = getBoolean(OrgConst.ID_SAVE_ENGINE_EVENTID, tripParams, portInfo, retval.saveTurnEngineEventId);
		retval.challanByDeliveryDate = getInt(OrgConst.ID_CHALLAN_BY_DELIVERY_DATE, tripParams, portInfo, retval.challanByDeliveryDate);
		retval.doStrictChallanTimingMatch = getInt(OrgConst.ID_INT_CHALLAN_TIMING_MERGE_STRICT, tripParams, portInfo, retval.doStrictChallanTimingMatch);
		retval.leftChallanLeewayMin = getInt(OrgConst.ID_INT_CHALLAN_TIMING_LEFT_LEEWAY, tripParams, portInfo, retval.leftChallanLeewayMin);
		retval.rightChallanLeewayMax = getInt(OrgConst.ID_INT_CHALLAN_TIMING_RIGHT_LEEWAY, tripParams, portInfo, retval.rightChallanLeewayMax);
		retval.ignoreOpNearLandmarkTypes = getInt(OrgConst.ID_IGNORE_OP_NEAR_LANDMARK_TYPE, tripParams, portInfo, retval.ignoreOpNearLandmarkTypes);
		retval.ignoreLandmarkDistThresh = getDouble(OrgConst.ID_IGNORE_LANDMARK_DIST_THRESH, tripParams, portInfo, retval.ignoreLandmarkDistThresh);
		retval.doAllMultiLU  = getBoolean(OrgConst.ID_DOALL_MULTI_LU, tripParams, portInfo, retval.doAllMultiLU);
		retval.flatLUPickStrategy = getInt(OrgConst.ID_FLAT_LU_PICK_STRATEGY, tripParams, portInfo, retval.flatLUPickStrategy);
		retval.multiLULeadDistLb = getDouble(OrgConst.ID_MULTI_LU_LEAD_LB, tripParams, portInfo, retval.multiLULeadDistLb);
		retval.multiLULeadDistUb = getDouble(OrgConst.ID_MULTI_LU_LEAD_UB, tripParams, portInfo, retval.multiLULeadDistUb);
		retval.doMultiLUDeltaDist  = getBoolean(OrgConst.ID_DO_MULTI_LU_DELTA_DIST, tripParams, portInfo, retval.doMultiLUDeltaDist);
		retval.challanIgnDistProvided = getInt(OrgConst.ID_INT_CHALLAN_IGN_DISTPROVIDED, tripParams, portInfo, retval.challanIgnDistProvided);

		retval.monitorStopLoThreshSec = getInt(OrgConst.ID_INT_LOW_CRIT_STOP_THRESH_SEC, tripParams, portInfo, retval.monitorStopLoThreshSec);
		retval.monitorStopHiThreshSec = getInt(OrgConst.ID_INT_HIGH_CRIT_STOP_THRESH_SEC, tripParams, portInfo, retval.monitorStopHiThreshSec);
		retval.monitorNoDataLoThreshSec = getInt(OrgConst.ID_INT_LOW_CRIT_NODATA_THRESH_SEC, tripParams, portInfo, retval.monitorNoDataLoThreshSec);
		retval.monitorNoDataHiThreshSec = getInt(OrgConst.ID_INT_HIGH_CRIT_NODATA_THRESH_SEC, tripParams, portInfo, retval.monitorNoDataHiThreshSec);
		retval.monitorExcessLeadLoThreshKm = getDouble(OrgConst.ID_DOUBLE_LOW_CRIT_EXCESSLEAD_THRESH_KM, tripParams, portInfo, retval.monitorExcessLeadLoThreshKm);
		retval.monitorExcessLeadHiThreshKm = getDouble(OrgConst.ID_DOUBLE_HIGH_CRIT_EXCESSLEAD_THRESH_KM, tripParams, portInfo, retval.monitorExcessLeadHiThreshKm);

		//special for newPickFirstLastLorUMask
		if (retval.newPickFirstLastLOrUMask == -1) {
			boolean bestLoadFirst = getBoolean(OrgConst.ID_DO_LOAD_FIRST,tripParams, portInfo, false);
			boolean bestLoadLast = getBoolean(OrgConst.ID_DO_LOAD_LAST,tripParams, portInfo, false);
			boolean bestUnloadFirst = getBoolean(OrgConst.ID_DO_UNLOAD_FIRST,tripParams, portInfo, false);
			boolean bestUnloadLast = getBoolean(OrgConst.ID_DO_UNLOAD_LAST,tripParams, portInfo, false);
			int newVal = 0;
			//(bit 3: first Load, bit 2 last load, bit 1: first unload, bit 0: last unload) (default: 0)
			if (bestLoadFirst)
				newVal |= 0x8;
			if (bestLoadLast)
				newVal |= 0x4;
			if (bestUnloadFirst)
				newVal |= 0x2;
			if (bestUnloadLast)
				newVal |= 0x1;
			retval.newPickFirstLastLOrUMask = newVal;
		}
	}
	
	public static boolean getBoolean(int id, TripParams tripParams, MiscInner.PortInfo portInfo, boolean currVal) {
		int retval = currVal ? 1 : 0;
		Integer dv = tripParams == null ? null : tripParams.getIntParam(id);
		if (dv != null)
			retval = dv.intValue();
		else if (portInfo != null) {
			ArrayList valList = portInfo.getIntParams(id);
			if (valList != null && valList.size() > 0)
				retval = ((Integer)(valList.get(0))).intValue();
		}
		return retval != 0;
	}
	
	
	
	public static int getInt(int id, TripParams tripParams, MiscInner.PortInfo portInfo, int currVal) {
		int retval = currVal;
		Integer dv = tripParams == null ? null : tripParams.getIntParam(id);
		if (dv != null)
			retval = dv.intValue();
		else if (portInfo != null) {
			ArrayList valList = portInfo.getIntParams(id);
			if (valList != null && valList.size() > 0)
				retval = ((Integer)(valList.get(0))).intValue();
		}
		return retval;
	}
	
	public static double getDouble(int id, TripParams tripParams, MiscInner.PortInfo portInfo, double currVal) {
		double retval = currVal;
		Double dv = tripParams == null ? null : tripParams.getDoubleParam(id);
		if (dv != null)
			retval = dv.doubleValue();
		else if (portInfo != null) {
			ArrayList valList = portInfo.getDoubleParams(id);
			if (valList != null && valList.size() > 0)
				retval = ((Double)(valList.get(0))).doubleValue();
		}
		return retval;
	}
	public static StopDirControl getControlFromOrg(MiscInner.PortInfo portInfo) {
		return StopDirControl.getControl(null, portInfo);
	}
	
	//CHANGE15 public int allowedLoadTypes() { // -1 if can be any, 0 if can be only load, 1 if can be only unload
	//CHANGE15 	int retval = -1;
	//CHANGE15 	if (stopOpStationAllowedAfterLoad && !stopOpStationAllowedAfterUnload)
	//CHANGE15 		retval = 1;
	//CHANGE15 	if (!stopOpStationAllowedAfterLoad && stopOpStationAllowedAfterUnload)
	//CHANGE15 		retval = 0;
	//CHANGE15 	return retval;
	//CHANGE15 }
	
	public double getStopThreshSpeedKMPH() {
		return stopThreshSpeedKMPH;
	}
	public void setStopThreshSpeedKMPH(double stopThreshSpeedKMPH) {
		this.stopThreshSpeedKMPH = stopThreshSpeedKMPH;
	}

	
	public double getMergeStopsSeqIfDistKM() {
		return mergeStopsSeqIfDistKM;
	}
	public void setMergeStopsSeqIfDistKM(double mergeStopsSeqIfDistKM) {
		this.mergeStopsSeqIfDistKM = mergeStopsSeqIfDistKM;
	}
	public double getDirChangeDetectThreshKM(Point st, Point en) {
		return st == null || en == null ? dirChangeDetectThreshKM : dirChangeDetectThreshKM+TrackMisc.getSimpleDistance(st, en);
	}
	public void setDirChangeDetectThreshKM(double dirChangeDetectThreshKM) {
		this.dirChangeDetectThreshKM = dirChangeDetectThreshKM;
	}
	//CHANGE15 public boolean isStopOpStationAllowedAfterLoad() {
	//CHANGE15 	return stopOpStationAllowedAfterLoad;
	//CHANGE15 }
	//CHANGE15 public void setStopOpStationAllowedAfterLoad(
	//CHANGE15 		boolean stopOpStationAllowedAfterLoad) {
	//CHANGE15 	this.stopOpStationAllowedAfterLoad = stopOpStationAllowedAfterLoad;
	//CHANGE15 }
	//CHANGE15 public boolean isStopOpStationAllowedAfterUnload() {
	//CHANGE15 	return stopOpStationAllowedAfterUnload;
	//CHANGE15 }
	//CHANGE15 public void setStopOpStationAllowedAfterUnload(
	//CHANGE15 		boolean stopOpStationAllowedAfterUnload) {
	//CHANGE15 	this.stopOpStationAllowedAfterUnload = stopOpStationAllowedAfterUnload;
	//CHANGE15 }
	public double getSameDirThresholdDegree() {
		return sameDirThresholdDegree;
	}
	public void setSameDirThresholdDegree(double sameDirThresholdDegree) {
		this.sameDirThresholdDegree = sameDirThresholdDegree;
	}
	public double getStopThreshTobeOpstationIfDirMin() {
		return stopThreshTobeOpstationIfDirMin;
	}
	public void setStopThreshTobeOpstationIfDirMin(
			double stopThreshTobeOpstationIfDirMin) {
		this.stopThreshTobeOpstationIfDirMin = stopThreshTobeOpstationIfDirMin;
	}
	public double getStopThreshTobeOpstationMin() {
		return stopThreshTobeOpstationMin;
	}
	public void setStopThreshTobeOpstationMin(double stopThreshTobeOpstationMin) {
		this.stopThreshTobeOpstationMin = stopThreshTobeOpstationMin;
	}
	public double getMergeStopOpstationIfInKMRange() {
		return mergeStopOpstationIfInKMRange;
	}
	public void setMergeStopOpstationIfInKMRange(
			double mergeStopOpstationIfInKMRange) {
		this.mergeStopOpstationIfInKMRange = mergeStopOpstationIfInKMRange;
	}
	public void setDoStopProcessing(int doStopProcessing) {
		this.doStopProcessing = doStopProcessing;
	}
	public int getDoStopProcessing() {
		return doStopProcessing;
	}
	//CHANGE15 public void setDoDirChangeProcessing(boolean doDirChangeProcessing) {
	//CHANGE15 	this.doDirChangeProcessing = doDirChangeProcessing;
	//CHANGE15 }
	//CHANGE15 public boolean isDoDirChangeProcessing() {
	//CHANGE15 	return doDirChangeProcessing;
	//CHANGE15 }
	public void setDirChangeDetectThreshKMHi(double dirChangeDetectThreshKMHi) {
		this.dirChangeDetectThreshKMHi = dirChangeDetectThreshKMHi;
	}
	public double getDirChangeDetectThreshKMHi(Point st, Point en) {
			return st == null || en == null ? dirChangeDetectThreshKMHi : dirChangeDetectThreshKMHi+TrackMisc.getSimpleDistance(st, en);
	}

	//CHANGE15 public boolean isDoInsideOpStation() {
	//CHANGE15 	return doInsideOpStation;
	//CHANGE15 }

	//CHANGE15 public void setDoInsideOpStation(boolean doInsideOpStation) {
	//CHANGE15 	this.doInsideOpStation = doInsideOpStation;
	//CHANGE15 }

	//CHANGE15 public void setDataGapThreshSec(int dataGapThreshSec) {
	//CHANGE15 	this.dataGapThreshSec = dataGapThreshSec;
	//CHANGE15 }

	//CHANGE15 public int getDataGapThreshSec() {
	//CHANGE15 	return dataGapThreshSec;
	//CHANGE15 }

	//CHANGE15 public void setFurthestRefLongitude(double furthestRefLongitude) {
	//CHANGE15 	this.furthestRefLongitude = furthestRefLongitude;
	//CHANGE15 }

	//CHANGE15 public double getFurthestRefLongitude() {
	//CHANGE15 	return furthestRefLongitude;
	//CHANGE15 }

	//CHANGE15 public void setFurthestRefLatitude(double furthestRefLatitude) {
	//CHANGE15 	this.furthestRefLatitude = furthestRefLatitude;
	//CHANGE15 }

	//CHANGE15 public double getFurthestRefLatitude() {
	//CHANGE15 	return furthestRefLatitude;
	//CHANGE15 }

	//CHANGE15 public boolean isMergeStopIfEndIsAlsoClose() {
	//CHANGE15 	return mergeStopIfEndIsAlsoClose;
	//CHANGE15 }

	//CHANGE15 public void setMergeStopIfEndIsAlsoClose(boolean mergeStopIfEndIsAlsoClose) {
	//CHANGE15 	this.mergeStopIfEndIsAlsoClose = mergeStopIfEndIsAlsoClose;
	//CHANGE15 }

	public double getMinDistAfterWhichLoadUnloadPossibleKM() {
		double retval =  minDistAfterWhichLoadUnloadPossibleKM;
		return retval;
	}

	public void setMinDistAfterWhichLoadUnloadPossibleKM(
			double minDistAfterWhichLoadUnloadPossibleKM) {
		this.minDistAfterWhichLoadUnloadPossibleKM = minDistAfterWhichLoadUnloadPossibleKM;
	}

	public boolean isForProperLoDirMust() {
		return forProperLoDirMust;
	}

	public void setForProperLoDirMust(boolean forProperLoDirMust) {
		this.forProperLoDirMust = forProperLoDirMust;
	}

	public boolean isForProperHiDirMust() {
		return forProperHiDirMust;
	}

	public void setForProperHiDirMust(boolean forProperHiDirMust) {
		this.forProperHiDirMust = forProperHiDirMust;
	}

	public boolean isForProperBothDirMust() {
		return forProperBothDirMust;
	}

	public void setForProperBothDirMust(boolean forProperBothDirMust) {
		this.forProperBothDirMust = forProperBothDirMust;
	}

	public double getLookBackDistMaxThreshForWaitKM() {
		return lookBackDistMaxThreshForWaitKM;
	}

	public void setLookBackDistMaxThreshForWaitKM(
			double lookBackDistMaxThreshForWaitKM) {
		this.lookBackDistMaxThreshForWaitKM = lookBackDistMaxThreshForWaitKM;
	}

	public double getLookBackDistBetweenStopKM() {
		return lookBackDistBetweenStopKM;
	}

	public void setLookBackDistBetweenStopKM(double lookBackDistBetweenStopKM) {
		this.lookBackDistBetweenStopKM = lookBackDistBetweenStopKM;
	}

	public double getDirChangeDetectThreshKMSuper() {
		return dirChangeDetectThreshKMSuper;
	}

	public void setDirChangeDetectThreshKMSuper(double dirChangeDetectThreshKMSuper) {
		this.dirChangeDetectThreshKMSuper = dirChangeDetectThreshKMSuper;
	}

	public double getDistSkipBeforeDirChangeCheck() {
		return distSkipBeforeDirChangeCheck;
	}

	public void setDistSkipBeforeDirChangeCheck(double distSkipBeforeDirChangeCheck) {
		this.distSkipBeforeDirChangeCheck = distSkipBeforeDirChangeCheck;
	}

	public double getStopThreshDistKM() {
		return stopThreshDistKM;
	}

	public void setStopThreshDistKM(double stopThreshDistKM) {
		this.stopThreshDistKM = stopThreshDistKM;
	}

	public int getStopStopPreferredOpType() {
		return stopStopPreferredOpType;
	}

	public void setStopStopPreferredOpType(int stopStopPreferredOpType) {
		this.stopStopPreferredOpType = stopStopPreferredOpType;
	}

	public double getLookForwDistMaxThreshForWaitKM() {
		return lookForwDistMaxThreshForWaitKM;
	}

	public void setLookForwDistMaxThreshForWaitKM(
			double lookForwDistMaxThreshForWaitKM) {
		this.lookForwDistMaxThreshForWaitKM = lookForwDistMaxThreshForWaitKM;
	}

	public double getLookForwDistBetweenStopKM() {
		return lookForwDistBetweenStopKM;
	}

	public void setLookForwDistBetweenStopKM(double lookForwDistBetweenStopKM) {
		this.lookForwDistBetweenStopKM = lookForwDistBetweenStopKM;
	}

	public boolean isDoForwBackForRegularOp() {
		return doForwBackForRegularOp;
	}

	public void setDoForwBackForRegularOp(boolean doForwBackForRegularOp) {
		this.doForwBackForRegularOp = doForwBackForRegularOp;
	}

	public double getDirChangeDetectThreshKM() {
		return dirChangeDetectThreshKM;
	}

	public double getDirChangeDetectThreshKMHi() {
		return dirChangeDetectThreshKMHi;
	}

	public boolean isDoTravelDistanceInsteadOfGeo() {
		return doTravelDistanceInsteadOfGeo;
	}

	public void setDoTravelDistanceInsteadOfGeo(boolean doTravelDistanceInsteadOfGeo) {
		this.doTravelDistanceInsteadOfGeo = doTravelDistanceInsteadOfGeo;
	}

	public double getTreatStopAsSameAsFixedIfDistLessThanKM() {
		return treatStopAsSameAsFixedIfDistLessThanKM;
	}

	public void setTreatStopAsSameAsFixedIfDistLessThanKM(
			double treatStopAsSameAsFixedIfDistLessThanKM) {
		this.treatStopAsSameAsFixedIfDistLessThanKM = treatStopAsSameAsFixedIfDistLessThanKM;
	}

	public int getPrefToFixedLoad() {
		return prefToFixedLoad;
	}

	public void setPrefToFixedLoad(int prefToFixedLoad) {
		this.prefToFixedLoad = prefToFixedLoad;
	}
	
	public int getPrefToFixedUnload() {
		return prefToFixedUnload;
	}

	public void setPrefToFixedUnload(int prefToFixedUnload) {
		this.prefToFixedUnload = prefToFixedUnload;
	}

	public double getM_linkedGateWaitBoxMtr() {
		return m_linkedGateWaitBoxMtr;
	}

	public void setM_linkedGateWaitBoxMtr(double gateWaitBoxMtr) {
		m_linkedGateWaitBoxMtr = gateWaitBoxMtr;
	}

	public double getM_linkedOpAreaBoxMtr() {
		return m_linkedOpAreaBoxMtr;
	}

	public void setM_linkedOpAreaBoxMtr(double opAreaBoxMtr) {
		m_linkedOpAreaBoxMtr = opAreaBoxMtr;
	}

	public double getM_linkedShiftDistExceedsMtr() {
		return m_linkedShiftDistExceedsMtr;
	}

	public void setM_linkedShiftDistExceedsMtr(double shiftDistExceedsMtr) {
		m_linkedShiftDistExceedsMtr = shiftDistExceedsMtr;
	}

	public boolean isToSendMessage() {
		return toSendMessage;
	}

	public void setToSendMessage(boolean toSendMessage) {
		this.toSendMessage = toSendMessage;
	}

	public boolean isM_luMustBeProper() {
		return m_luMustBeProper;
	}

	public void setM_luMustBeProper(boolean mustBeProper) {
		m_luMustBeProper = mustBeProper;
	}

	public int getM_lookForChallan() {
		return m_lookForChallan;
	}

	public void setM_lookForChallan(int forChallan) {
		m_lookForChallan = forChallan;
	}

	public boolean isM_pickFurthestUnloadOpstation() {
		return m_pickFurthestUnloadOpstation;
	}

	public void setM_pickFurthestUnloadOpstation(boolean furthestUnloadOpstation) {
		m_pickFurthestUnloadOpstation = furthestUnloadOpstation;
	}

	public boolean isGetBestAmongstSameOpIdMultiLU() {
		return getBestAmongstSameOpIdMultiLU;
	}

	public void setGetBestAmongstSameOpIdMultiLU(
			boolean getBestAmongstSameOpIdMultiLU) {
		this.getBestAmongstSameOpIdMultiLU = getBestAmongstSameOpIdMultiLU;
	}

	public boolean isDoMultiLoadMultiLU() {
		return doMultiLoadMultiLU;
	}

	public void setDoMultiLoadMultiLU(boolean doMultiLoadMultiLU) {
		this.doMultiLoadMultiLU = doMultiLoadMultiLU;
	}

	public boolean isDoMultiUnloadMultiLU() {
		return doMultiUnloadMultiLU;
	}

	public void setDoMultiUnloadMultiLU(boolean doMultiUnloadMultiLU) {
		this.doMultiUnloadMultiLU = doMultiUnloadMultiLU;
	}

	public int getM_afterUnloadAuto() {
		return m_afterUnloadAuto;
	}

	public void setM_afterUnloadAuto(int unloadAuto) {
		m_afterUnloadAuto = unloadAuto;
	}

	public boolean isM_doETA() {
		return m_doETA;
	}

	public void setM_doETA(boolean m_doeta) {
		m_doETA = m_doeta;
	}

	public boolean isM_doNewApproachForTrip() {
		return m_doNewApproachForTrip;
	}

	public void setM_doNewApproachForTrip(boolean newApproachForTrip) {
		m_doNewApproachForTrip = newApproachForTrip;
	}

	public int getM_getNearestOpForStop() {
		return m_getNearestOpForStop;
	}

	public void setM_getNearestOpForStop(int nearestOpForStop) {
		m_getNearestOpForStop = nearestOpForStop;
	}

	public int getM_getNearstSpecialLM() {
		return m_getNearstSpecialLM;
	}

	public void setM_getNearstSpecialLM(int nearstSpecialLM) {
		m_getNearstSpecialLM = nearstSpecialLM;
	}

	public double getM_threshKMForNearestOp() {
		return m_threshKMForNearestOp;
	}

	public void setM_threshKMForNearestOp(double forNearestOp) {
		m_threshKMForNearestOp = forNearestOp;
	}

	public double getM_threshKMForNearestLM() {
		return m_threshKMForNearestLM;
	}

	public void setM_threshKMForNearestLM(double forNearestLM) {
		m_threshKMForNearestLM = forNearestLM;
	}

	public boolean isM_useChallanInPreferredLU() {
		return m_useChallanInPreferredLU;
	}

	public void setM_useChallanInPreferredLU(boolean challanInPreferredLU) {
		m_useChallanInPreferredLU = challanInPreferredLU;
	}

	public boolean isM_usePrevLUInPreferredLU() {
		return m_usePrevLUInPreferredLU;
	}

	public void setM_usePrevLUInPreferredLU(boolean prevLUInPreferredLU) {
		m_usePrevLUInPreferredLU = prevLUInPreferredLU;
	}

	public boolean isBestLoadFirst() {
		return (newPickFirstLastLOrUMask & 0x8) != 0;
	}
	public boolean isBestLoadLast() {
		return (newPickFirstLastLOrUMask & 0x4) != 0;
	}
	public boolean isBestUnloadFirst() {
		return (newPickFirstLastLOrUMask & 0x2) != 0;
	}
	public boolean isBestUnloadLast() {
		return (newPickFirstLastLOrUMask & 0x1) != 0;
	}
	public void setBestLoadFirst(boolean val) {
		if (val)
			newPickFirstLastLOrUMask |= 0x8;
		else
			newPickFirstLastLOrUMask &= ~0x00000008;
	}
	public void setBestLoadLast(boolean val) {
		if (val)
			newPickFirstLastLOrUMask |= 0x4;
		else
			newPickFirstLastLOrUMask &= ~0x00000004;
	}
	public void setBestUnloadFirst(boolean val) {
		if (val)
			newPickFirstLastLOrUMask |= 0x2;
		else
			newPickFirstLastLOrUMask &= ~0x00000002;
	}
	public void setBestUnloadLast(boolean val) {
		if (val)
			newPickFirstLastLOrUMask |= 0x1;
		else
			newPickFirstLastLOrUMask &= ~0x00000001;
	}
	public int getNewShiftDateBy() {
		return newShiftDateBy;
	}

	public void setNewShiftDateBy(int newShiftDateBy) {
		this.newShiftDateBy = newShiftDateBy;
	}

	public int getM_materialLookUpApproach() {
		return m_materialLookUpApproach;
	}

	public void setM_materialLookUpApproach(int lookUpApproach) {
		m_materialLookUpApproach = lookUpApproach;
	}

	public boolean isBestLUisValidExtremum() {
		return bestLUisValidExtremum;
	}

	public void setBestLUisValidExtremum(boolean bestLUisValidExtremum) {
		this.bestLUisValidExtremum = bestLUisValidExtremum;
	}
	public int getDoSpecialAlgoForBestSeq() {
		return doSpecialAlgoForBestSeq;
	}
	public void setDoSpecialAlgoForBestSeq(int doSpecialAlgoForBestSeq) {
		this.doSpecialAlgoForBestSeq = doSpecialAlgoForBestSeq;
	}
	public boolean isSetGinToWinInForwBack() {
		return setGinToWinInForwBack;
	}
	public void setSetGinToWinInForwBack(boolean setGinToWinInForwBack) {
		this.setGinToWinInForwBack = setGinToWinInForwBack;
	}
	public int getDataGapSec() {
		return dataGapSec;
	}
	public void setDataGapSec(int dataGapSec) {
		this.dataGapSec = dataGapSec;
	}
	public boolean isRemoveLLU() {
		return removeLLU;
	}
	public void setRemoveLLU(boolean removeLLU) {
		this.removeLLU = removeLLU;
	}
	public double getDontRemoveLLIfDistExceeds() {
		return dontRemoveLLIfDistExceeds;
	}
	public void setDontRemoveLLIfDistExceeds(double dontRemoveLLIfDistExceeds) {
		this.dontRemoveLLIfDistExceeds = dontRemoveLLIfDistExceeds;
	}
	public boolean isDoTurnDirOnLOnly() {
		return doTurnDirOnLOnly;
	}
	public void setDoTurnDirOnLOnly(boolean doTurnDirOnLOnly) {
		this.doTurnDirOnLOnly = doTurnDirOnLOnly;
	}
	public int getSkipTimeGapLessThanSec() {
		return skipTimeGapLessThanSec;
	}
	public void setSkipTimeGapLessThanSec(int skipTimeGapLessThanSec) {
		this.skipTimeGapLessThanSec = skipTimeGapLessThanSec;
	}
	public boolean isLookupChallanDestAddress() {
		return lookupChallanDestAddress;
	}
	public void setLookupChallanDestAddress(boolean lookupChallanDestAddress) {
		this.lookupChallanDestAddress = lookupChallanDestAddress;
	}
	public boolean isLookupChallanSrcAddress() {
		return lookupChallanSrcAddress;
	}
	public void setLookupChallanSrcAddress(boolean lookupChallanSrcAddress) {
		this.lookupChallanSrcAddress = lookupChallanSrcAddress;
	}
	public boolean isInsertChallanDestAddress() {
		return insertChallanDestAddress;
	}
	public void setInsertChallanDestAddress(boolean insertChallanDestAddress) {
		this.insertChallanDestAddress = insertChallanDestAddress;
	}
	public boolean isInsertChallanSrcAddress() {
		return insertChallanSrcAddress;
	}
	public void setInsertChallanSrcAddress(boolean insertChallanSrcAddress) {
		this.insertChallanSrcAddress = insertChallanSrcAddress;
	}
	public double getSuperMinDistAfterWhichLoadUnloadPossibleKM() {
		return superMinDistAfterWhichLoadUnloadPossibleKM;
	}
	public void setSuperMinDistAfterWhichLoadUnloadPossibleKM(
			double superMinDistAfterWhichLoadUnloadPossibleKM) {
		this.superMinDistAfterWhichLoadUnloadPossibleKM = superMinDistAfterWhichLoadUnloadPossibleKM;
	}
	public boolean isSplitULIfNextIsUL() {
		return splitULIfNextIsUL;
	}
	public void setSplitULIfNextIsUL(boolean splitULIfNextIsUL) {
		this.splitULIfNextIsUL = splitULIfNextIsUL;
	}
	public double getMinDistAfterWhichLUPossibleIfDala() {
		return minDistAfterWhichLUPossibleIfDala;
	}
	public void setMinDistAfterWhichLUPossibleIfDala(
			double minDistAfterWhichLUPossibleIfDala) {
		this.minDistAfterWhichLUPossibleIfDala = minDistAfterWhichLUPossibleIfDala;
	}
	public int getMonitorStopLoThreshSec() {
		return monitorStopLoThreshSec;
	}
	public void setMonitorStopLoThreshSec(int monitorStopLoThreshSec) {
		this.monitorStopLoThreshSec = monitorStopLoThreshSec;
	}
	public int getMonitorStopHiThreshSec() {
		return monitorStopHiThreshSec;
	}
	public void setMonitorStopHiThreshSec(int monitorStopHiThreshSec) {
		this.monitorStopHiThreshSec = monitorStopHiThreshSec;
	}
	public int getMonitorNoDataLoThreshSec() {
		return monitorNoDataLoThreshSec;
	}
	public void setMonitorNoDataLoThreshSec(int monitorNoDataLoThreshSec) {
		this.monitorNoDataLoThreshSec = monitorNoDataLoThreshSec;
	}
	public int getMonitorNoDataHiThreshSec() {
		return monitorNoDataHiThreshSec;
	}
	public void setMonitorNoDataHiThreshSec(int monitorNoDataHiThreshSec) {
		this.monitorNoDataHiThreshSec = monitorNoDataHiThreshSec;
	}
	public double getMonitorExcessLeadLoThreshKm() {
		return monitorExcessLeadLoThreshKm;
	}
	public void setMonitorExcessLeadLoThreshKm(double monitorExcessLeadLoThreshKm) {
		this.monitorExcessLeadLoThreshKm = monitorExcessLeadLoThreshKm;
	}
	public double getMonitorExcessLeadHiThreshKm() {
		return monitorExcessLeadHiThreshKm;
	}
	public void setMonitorExcessLeadHiThreshKm(double monitorExcessLeadHiThreshKm) {
		this.monitorExcessLeadHiThreshKm = monitorExcessLeadHiThreshKm;
	}
	public int getExtStopDurSecComplete() {
		return extStopDurSecComplete;
	}
	public void setExtStopDurSecComplete(int extStopDurSecComplete) {
		this.extStopDurSecComplete = extStopDurSecComplete;
	}
	public double getDalaUpMaxDistKM() {
		return dalaUpMaxDistKM;
	}
	public void setDalaUpMaxDistKM(double dalaUpMaxDistKM) {
		this.dalaUpMaxDistKM = dalaUpMaxDistKM;
	}

}

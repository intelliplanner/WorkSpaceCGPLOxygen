package com.ipssi.miningOpt;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TripParams;

public class Parameters {
	public  ArrayList<Integer> CRIT_EVENTS_TO_TRACK_SHOVEL = new ArrayList<Integer>();
	public  ArrayList<Integer> NORM_EVENTS_TO_TRACK_SHOVEL = new ArrayList<Integer>();
	public  ArrayList<Integer> CRIT_EVENTS_TO_TRACK_DUMPER = new ArrayList<Integer>();
    public  ArrayList<Integer> NORM_EVENTS_TO_TRACK_DUMPER = new ArrayList<Integer>();
	public  boolean g_usePredictedToShowCurrStuff = true;//will use predicted for frac and for wait params in display 
	public boolean autoUpdateAssignmentOnLoadChange = true;//TODO make it dyn
	public boolean autoUpdateAssignmentOnUnloadChange = true;//TODO make it dyn
	public   boolean g_doPredictedQLenAtLExit = true;
	public   boolean g_doOptimizationAtLExit = true;
	public    int OPTIMIZE_Q_THRESHOLD = 2;
	public  int OPTIMIZE_APPROACH = DynOptimizer.OPTIMIZE_STICKY;
	public static void loadParameters(Connection conn, int portNodeId) throws Exception {//so that it could be called on save of Org Parameters
		NewMU newmu = NewMU.getManagementUnit(conn, portNodeId);
		if (newmu == null)
			return ;
		Cache cache = Cache.getCacheInstance(conn);
		MiscInner.PortInfo portInfo = cache.getPortInfo(portNodeId, conn);
		Parameters parameters = newmu.parameters;
		parameters.CRIT_EVENTS_TO_TRACK_SHOVEL = getMultiInt(OrgConst.OPT_ID_INT_CRIT_EVENTS_TO_TRACK_SHOVEL,portInfo, parameters.CRIT_EVENTS_TO_TRACK_SHOVEL);
		parameters.NORM_EVENTS_TO_TRACK_SHOVEL = getMultiInt(OrgConst.OPT_ID_INT_NORM_EVENTS_TO_TRACK_SHOVEL,portInfo, parameters.NORM_EVENTS_TO_TRACK_SHOVEL);
		parameters.CRIT_EVENTS_TO_TRACK_DUMPER = getMultiInt(OrgConst.OPT_ID_INT_CRIT_EVENTS_TO_TRACK_DUMPER,portInfo, parameters.CRIT_EVENTS_TO_TRACK_DUMPER);
		parameters.NORM_EVENTS_TO_TRACK_DUMPER = getMultiInt(OrgConst.OPT_ID_INT_NORM_EVENTS_TO_TRACK_DUMPER,portInfo, parameters.NORM_EVENTS_TO_TRACK_DUMPER);
		parameters.g_usePredictedToShowCurrStuff = StopDirControl.getBoolean(OrgConst.OPT_ID_INT_USE_PREDICTED_INSHOW, null, portInfo, parameters.g_usePredictedToShowCurrStuff); 
		parameters.autoUpdateAssignmentOnLoadChange = StopDirControl.getBoolean(OrgConst.ID_MINING_UPDATE_ASSIGNMENT_IF_SRC_DIFF, null, portInfo, parameters.autoUpdateAssignmentOnLoadChange);
		parameters.autoUpdateAssignmentOnUnloadChange = StopDirControl.getBoolean(OrgConst.ID_MINING_UPDATE_ASSIGNMENT_IF_DEST_DIFF, null, portInfo, parameters.autoUpdateAssignmentOnUnloadChange);
		parameters.g_doPredictedQLenAtLExit = StopDirControl.getBoolean(OrgConst.OPT_DO_PREDICTED_LOAD_EXIT, null, portInfo, parameters.g_doPredictedQLenAtLExit);
		parameters.g_doOptimizationAtLExit =StopDirControl.getBoolean(OrgConst.OPT_DO_OPTIMIZATION_AT_LEXIT, null, portInfo, parameters.g_doOptimizationAtLExit);
		parameters.OPTIMIZE_Q_THRESHOLD = StopDirControl.getInt(OrgConst.OPT_DO_OPTIMIZE_Q_THRESHOLD, null, portInfo, parameters.OPTIMIZE_Q_THRESHOLD);
		parameters.OPTIMIZE_APPROACH = StopDirControl.getInt(OrgConst.OPT_OPTIMIZE_APPROACH, null, portInfo, parameters.OPTIMIZE_APPROACH);
	}
	
	public static ArrayList<Integer> getMultiInt(int id, MiscInner.PortInfo portInfo, ArrayList<Integer> currVal) {
		ArrayList<Integer> retval = null;
		if (portInfo != null) {
			retval = portInfo.getIntParams(id);
		}
		if (retval == null || retval.size() == 0)
			retval = currVal;
		return retval;
	}
}

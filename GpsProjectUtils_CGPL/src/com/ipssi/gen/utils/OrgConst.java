package com.ipssi.gen.utils;

import java.util.ArrayList;

import com.ipssi.miningOpt.DynOptimizer;

public class OrgConst {
	public static int G_NUMBER_VEHICLES = 5400;
	// naming convention ID_ is the lov id, at the end suffix by int, dbl, str
	// INT LOV
	public static int ID_CHECK_USER_DEFINED_LANDMARK_INT = 0;
	public static int ID_CHECK_USER_DEFINED_REGION_INT = 1;
	public static int ID_TILING_SIZE = 2;
	public static int ID_ORG_SUPERVISION_LEVEL = 3;
	public static int ID_TO_SEND_TRIP_MESSAGE_INT = 4;
	public static int ID_ASSIGNMENT_FACTOR=5;
	public static int ID_SKILL_FACTOR=6;
	public static int ID_DO_EXTREMUM_VALID_LU = 7;
	public static int ID_DO_TRACK_MULTI_LU = 8;
	public static int ID_DO_LOAD_FIRST = 9;
	public static int ID_DO_LOAD_LAST = 10;
	public static int ID_DO_UNLOAD_FIRST = 11;
	public static int ID_DO_UNLOAD_LAST = 12;
	public static int ID_DO_LU_MUST_BE_PROPER = 13;
	public static int ID_MARK_LOAD_WAIT_IN_SHIFT_DATE = 14;
	public static int ID_MARK_LOAD_GATE_IN_SHIFT_DATE = 15;
	public static int ID_MARK_LOAD_AREA_IN_SHIFT_DATE = 16;
	public static int ID_MARK_LOAD_AREA_OUT_SHIFT_DATE = 17;
	public static int ID_MARK_LOAD_GATE_OUT_SHIFT_DATE = 18;
	public static int ID_MARK_LOAD_WAIT_OUT_SHIFT_DATE = 19;
	
	public static int ID_MARK_UNLOAD_WAIT_IN_SHIFT_DATE = 20;
	public static int ID_MARK_UNLOAD_GATE_IN_SHIFT_DATE = 21;
	public static int ID_MARK_UNLOAD_AREA_IN_SHIFT_DATE = 22;
	public static int ID_MARK_UNLOAD_AREA_OUT_SHIFT_DATE = 23;
	public static int ID_MARK_UNLOAD_GATE_OUT_SHIFT_DATE = 24;
	public static int ID_MARK_UNLOAD_WAIT_OUT_SHIFT_DATE = 25;
	
	public static int ID_LOOK_FOR_CHALLAN = 26;
 
		public static int ID_FIRST_ACTION_THRESHOLD = 27;
	
	
 
	public static int ID_SHOW_BASE_MAP_DETAILS = 28;
    public static int ID_SHOW_MINING_IN_DEPLOYMENT = 29;
	public static int ID_MAP_LOC_LOOKUP_BOUND_MTR = 30;
	public static int ID_LANDMARK_LOC_LOOKUP_BOUND_MTR = 31;
	public static int ID_INVPILE_LOC_LOOKUP_BOUND_MTR = 32;
	public static int ID_MATERIAL_LOOKUP_APPROACH = 33; //val = 0 => none, val = 1 => APMDC style exhaustive
	
	public static int ID_DEVICE_MESSAGE_REMOVE_APPROACH = 34; // val =0 => remove only sent, val = 1 (for MICT) send all messages before the latest ack ..

	public static int ID_INT_DO_MERGE_CONSECUTIVE_LLUU = 35;//default 1
	public static int ID_INT_DO_SHORT_TRIP_POOR_NW=36;//default 0
	public static int ID_INT_DO_STOP_MARKER = 37;//default = 0;
	public static int ID_INT_DO_PREV_STATE_FROM_LUSEQ = 38; //default = 0
	public static int ID_INT_PICK_FURTHEST_UNLOAD_OPSTATION = 39;
	
	public static int ID_INT_GETBEST_AMONGS_SAME_OPID_MULTILU = 40;
	public static int ID_INT_DO_MULTILOAD_MULTILU = 41;
	public static int ID_INT_DO_MULTIUNLOAD_MULTILU = 42;
	public static int ID_INT_INPRELOAD_PROCESSING_PREISINSIDE = 43; //eventually get rid of it and assess from gate area info
	public static int ID_TOGOUP_ORG_FOR_OPSTATION=44;
	public static int ID_INT_FUEL_WINDOW = 45;
	public static int ID_INT_MAX_FUEL_WINDOW=46;
	public static int ID_INT_FUEL_POS_NEG_WINDOW=47;
	public static int ID_INT_TO_STOP_GOINGUP_FOR_OPSTATION = 48;
	public static int ID_INT_AFTER_UNLOAD_GEN_INSTR = 151; //0 go back to from opstation, 1 wait as is 2 wait at local branch office
	public static int ID_INT_DOETA = 152;
	public static int ID_INT_MIN_GPS_SPEED_CALC_SPEED = 153;
	public static int ID_INT_FOR_LOAD_ONLY = 154;
	public static int ID_INT_TRIP_CALC_NEW_APPROACH = 155;
	public static int ID_INT_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE = 156; //if -1 then dont get (default), if 0 then get of any type, else get nearest Opstation of sub type
	public static int ID_INT_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE = 157; //if 0 then dont get (default), if 1 get of type
	public static int ID_INT_DO_TRIP_DATA_EXCHANGE = 158; //if 0 then dont get (default), if 1 get of type
	
	
 	// DOUBLE LOV
	public static int ID_THRESHOLD_SMALL_BOX = 0;
	public static int ID_THRESHOLD_DIST_TO_AVOID_LOOKUP_DBL = 1;
	public static int ID_USER_DEFINED_DIST_RANGE_DBL = 2;
	public static int ID_BASEMAP_DIST_RANGE_DBL = 3;
	public static int ID_BASEMAP_ROAD_RANGE_DBL = 4;
	public static int ID_BASEMAP_INREGIONSEQ_THRESHOLD = 5;
	public static int ID_SHOW_DIST_IF_GREATER = 6;
	public static int ID_RETAIN_DIST_MARGIN = 7;
	public static int ID_STOP_DIST_MARGIN = 8;
	public static int ID_LINKED_VEHICLE_OPAREA_BOX_MTR = 9;
	public static int ID_LINKED_VEHICLE_GATEWAIT_AREA_BOX_MTR = 10;
	public static int ID_LINKED_VEHICLE_SHIFT_DIST_EXCEEDS_MTR = 11;
	public static int ID_RECALC_ORIENTATION_IF_DIST_EXCEEDS_MTR = 12;
	public static int ID_BASEMAP_ROAD_RANGE_GETFIRST_DBL = 13;
	public static int ID_USER_MAX_SPEED = 14;
	public static int ID_TRIP_PROCESS_IF_DIST_EXCEEDS_DBL = 15;
	public static int ID_TRIP_MOVING_IF_SPEED_EXCEEDS_DBL = 16;
	public static int ID_TRIP_STOPPED_IF_SPEED_LESS_DBL = 17;
	public static int ID_RED_THRESHOLD = 18;
	public static int ID_YELLOW_THRESHOLD = 19;
	public static int ID_SHOW_DISTRICT_STATE_NAMES_IF_DIST_GRT_THAN = 20;
	public static int ID_USER_LEAST_SPEED = 21;
	public static int ID_FUEL_MAX_DISTKM = 22;
	public static int ID_FUEL_KMPL=23;
	public static int ID_FUEL_RESET_VAL_CHANGE=24;
	public static int ID_FUEL_RESIDUE_EXCEEDS=25;
	public static int ID_FUEL_POS_NEG_THRESH=26;
	public static int ID_FUEL_POS_NEG_LOOKAHEAD__PROP_EXCEEDS=27;
	public static int ID_WAIT_LOOKBACK_MAXTHRESH_KM=28;
	public static int ID_WAIT_LOOKBACK_BETWEENSTOP_KM=29;
	public static int ID_DOUBLE_FOR_STOP_GET_NEAREST_OP_OF_SUB_TYPE_MAXDIST = 30; //default = 50
	public static int ID_DOUBLE_FOR_STOP_GET_NEAREST_LANDMARK_OF_SUB_TYPE_MAXDIST = 31; //default = 50
	public static int ID_DIRCHANGE_SKIP_KM = 32;
	public static int ID_WAIT_LOOKFORW_MAXTHRESH_KM=33;
	public static int ID_WAIT_LOOKFORW_BETWEENSTOP_KM=34;
	
	
	// STRING PARAM
	public static int ID_BASEMAP_LANDMARK_LAYERS_STR = 0;
	public static int ID_BASEMAP_ROAD_LAYERS_STR = 1;
	public static int ID_BASEMAP_REGIONSEQUENCE_LAYERS_STR = 2;
	public static int ID_DIST_FORMAT_STR = 3;
	public static int ID_WEBLAYOUT_NAME=4;
	public static int ID_ADDNLLAYER_NAME = 5;
	public static int ID_MAP_NAME = 6;
	public static int ID_BASEMAP_ROAD_LAYERS_GETFIRST_STR = 7;
	public static int ID_NAMES_TO_BE_CROPPED_FROM_LOCATION_NAME = 8;
	public static int ID_STRING_DEFAULT_ADDNL_ALLOWED_STRING = 1000;
	public static int ID_STRING_AEM_ADDNL_ALLOWED_STRING = 1001;
	public static int ID_STRING_ATRACK_ADDNL_ALLOWED_STRING = 1002;
	public static int ID_STRING_TELTONIKA_ADDNL_ALLOWED_STRING = 1003;
	public static int ID_STRING_FASTTRACK_ADDNL_ALLOWED_STRING = 1004;
	public static int ID_STRING_GEOVAS_ADDNL_ALLOWED_STRING = 1005;
	public static int ID_STRING_HELIOS_ADDNL_ALLOWED_STRING = 1006;
	public static int ID_STRING_ITRACGOLD_ADDNL_ALLOWED_STRING = 1007;
	public static int ID_STRING_LOOKET_ADDNL_ALLOWED_STRING = 1008;
	public static int ID_STRING_MEITRACK310_ADDNL_ALLOWED_STRING = 1009;
	public static int ID_STRING_MEITRACK_ADDNL_ALLOWED_STRING = 1010;
	public static int ID_STRING_NOWIRE_ADDNL_ALLOWED_STRING = 1011;
	public static int ID_STRING_ROBOGM_ADDNL_ALLOWED_STRING = 1012;
	public static int ID_STRING_SUNTECH_ADDNL_ALLOWED_STRING = 1013;
	public static int ID_STRING_VISIONTEK_ADDNL_ALLOWED_STRING = 1014;
	public static int ID_STRING_DONGLE_ADDNL_ALLOWED_STRING=1015;

	public static int ID_STOPDIR_THRESH_SPEEDKMPH_DOUBLE = 10001;
	public static int ID_STOPDIR_MERGE_STOP_KM_DOUBLE = 10002;
	public static int ID_STOPDIR_DIRCHANGE_LO_KM_DOUBLE = 10003;
	public static int ID_STOPDIR_DIRCHANGE_HI_KM_DOUBLE = 10004;
	public static int ID_STOPDIR_SAMEDIR_DEGREE_DOUBLE = 10005;
	public static int ID_STOPDIR_DURTHRESH_IFNODIR_MIN_DOUBLE = 10006;
	public static int ID_STOPDIR_DURTHRESH_IFDIR_MIN_DOUBLE = 10007;
	public static int ID_STOPDIR_MERGE_OPSTATION_KM_DOUBLE = 10008;

	public static int ID_STOPDIR_DOSTOP_AFTER_LOAD_INT = 10009;
	public static int ID_STOPDIR_DOSTOP_AFTER_UNLOAD_INT = 10010;
	public static int ID_STOPDIR_DOSTOP_PROC_INT = 10011;
	public static int ID_STOPDIR_DODIR_PROC_INT = 10012;
	public static int ID_INT_STOP_TO_MERGE_IF_BOTH_END_IN_RANGE = 10013;
	public static int ID_INT_STOP_VALID_ONLYIF_LOW_DIR = 10014;
	public static int ID_INT_STOP_VALID_ONLYIF_HIGH_DIR = 10015;
	public static int ID_INT_STOP_VALID_ONLYIF_BOTH_DIR = 10016;
	public static int ID_STOP_MINDIST_LOADUNLOAD = 10017;
	public static int ID_STOPDIR_DIRCHANGE_SUPER_KM_DOUBLE = 10018;
	public static int ID_STOPDIR_THRESH_DISTKM_DOUBLE = 10019;
	public static int ID_DOUBLE_DIST_NOREMOVE_LL = 10020;
	
	public static int ID_STOPDIR_PREFERRED_LUTYPE = 10020;
	public static int ID_STOPDIR_FORWBACK_FOR_REG = 10021;
	public static int ID_STOPDIR_FORWBACK_BYTRAVEL = 10022;
	public static int ID_STOPDIR_THRESH_STOP_SAME_AS_FIXED_KM = 10023;
	public static int ID_STOPDIR_ID_STOPDIR_PREF_TO_FIXED_LOAD_INT = 10024;
	public static int ID_STOPDIR_ID_STOPDIR_PREF_TO_FIXED_UNLOAD_INT = 10025;
	public static int ID_INT_CHALLANSET_FIXEDLOAD = 10026;
	public static int ID_INT_CHALLANSET_FIXEDUNLOAD = 10027;
	public static int ID_INT_CHALLANSET_ADDNL_PARAM_BIN_QtyMatDistEmail = 10028;
	public static int ID_STOPDIR_INT_USECHALLAN_FOR_LUBREAK_ID = 10029;
	public static int ID_STOPDIR_INT_USEPREVLU_FOR_LUBREAK_ID = 10030;
	public static int ID_INT_MRS_STYLE_HYBRID_ALWAY = 10031;//NOTUSED
	public static int ID_INT_PICKFIRSTLASTLORU_MASK_NEW = 10032;//firs L last L first U last U
	public static int ID_INT_SHIFTDATE_BY_NEW = 10033;
	public static int ID_INT_SPECIAL_ALGO_BEST_SEQ = 10034;
	public static int ID_INT_SET_GIN_TO_WIN_FOR_FORWBACK = 10035;
	public static int ID_INT_DATA_GAP_CONSIDER_AS_START = 10036;
	public static int ID_INT_DO_MAP_ICON_BY_LOAD = 10037;
	public static int ID_INT_DO_MAP_FILTER_DIMID = 10038;
	public static int ID_INT_TRACK_LATEST_EVENT = 10039;
	public static int ID_INT_DOLLU_REMOVAL = 10040;
	public static int ID_INT_STOPDIR_RECORD_DIR_ON_LONLY = 10041;
	public static int ID_INT_STOPDIR_SKIP_TIID_INT_STOPDIR_SKIP_TIME_LESS_THANME_LESS_THAN = 10042;
	public static int ID_INT_TRIPPARAM_DO_CDH_DEST_LOOKUP = 10043;
	public static int ID_INT_TRIPPARAM_DO_CDH_SRC_LOOKUP = 10044;
	public static int ID_INT_TRIPPARAM_DO_CDH_DEST_INSERT = 10045;
	public static int ID_INT_TRIPPARAM_DO_CDH_SRC_INSERT = 10046;
	
	public static int ID_INT_SCAN_MIN_TIME = 10047;
	public static int ID_INT_SCAN_MAX_TIME = 10048;
	public static int ID_INT_SCAN_OFFICE_REGION_ID = 10049;
	public static int ID_INT_DO_SCAN = 10050;
	
	public static int ID_DOUBLE_DIST_FROM_STRAIGHT_LINE = 10051;
	public static int ID_DOUBLE_DIST_PER_DAY = 10052;
	public static int ID_MAX_TIME_PER_DELIVERY = 10053;
	public static int ID_STRING_DB_OPSUMM_PAGE = 10054;
	
	public static int ID_DO_ETA_PROC_NEW = 10055;
	public static int ID_DO_ETA_SRCDEST_BACK = 10056;
	public static int ID_DO_MININIG_UNIT_PROCESSING = 10057;
	public static int ID_SAVE_ENGINE_EVENTID = 10058;
	public static int ID_CHALLAN_BY_DELIVERY_DATE = 10059;
	public static int ID_STOP_SUPER_MINDIST_LOADUNLOAD = 10060;
	public static int ID_INT_TELTONIKA_GAP = 10061;
	public static int ID_INT_CHALLAN_TIMING_MERGE_STRICT = 10062;
	public static int ID_INT_CHALLAN_TIMING_LEFT_LEEWAY = 10063;
	public static int ID_INT_CHALLAN_TIMING_RIGHT_LEEWAY = 10064;
	public static int ID_STR_STORE_CODE = 10065;
	public static int ID_SPLIT_UL_IF_NEXT_IS_UL = 10066;
	
	public static int ID_IGNORE_OP_NEAR_LANDMARK_TYPE = 10067;
	public static int ID_IGNORE_LANDMARK_DIST_THRESH = 10068;
	public static int ID_DOALL_MULTI_LU = 10069;
	public static int ID_FLAT_LU_PICK_STRATEGY = 10070;
	public static int ID_MULTI_LU_LEAD_LB = 10071;
	public static int ID_MULTI_LU_LEAD_UB = 10072;
	public static int ID_DO_MULTI_LU_DELTA_DIST = 10073;
	public static int ID_INT_CHALLAN_IGN_DISTPROVIDED = 10074; //0 dont ignore, 1 always ignore and use cdh_email_info,
	public static int ID_STOP_MINDIST_DALA_LOADUNLOAD = 10075;
	public static int ID_STRING_BASE_IMAGE_TRIP = 10076;
	public static int ID_DOUBLE_BASE_IMAGE_LX = 10077;
	public static int ID_DOUBLE_BASE_IMAGE_UX = 10078;
	public static int ID_DOUBLE_BASE_IMAGE_LY = 10079;
	public static int ID_DOUBLE_BASE_IMAGE_UY = 10080;
	
	public static int ID_INT_LOW_CRIT_STOP_THRESH_SEC = 10081;
	public static int ID_INT_HIGH_CRIT_STOP_THRESH_SEC = 10082;
	public static int ID_INT_LOW_CRIT_NODATA_THRESH_SEC = 10083;
	public static int ID_INT_HIGH_CRIT_NODATA_THRESH_SEC = 10084;
	public static int ID_DOUBLE_LOW_CRIT_EXCESSLEAD_THRESH_KM = 10085;
	public static int ID_DOUBLE_HIGH_CRIT_EXCESSLEAD_THRESH_KM = 10086;
	public static int ID_INT_SHOVEL_TYPES = 10087;
	
//MINING related	
	public static int ID_NEW_MANAGEMENT_UNIT_MARKER = 10088;
	public static int ID_MINING_UPDATE_ASSIGNMENT_IF_SRC_DIFF = 10089;
	public static int ID_MINING_UPDATE_ASSIGNMENT_IF_DEST_DIFF = 10090;
	public static int OPT_ID_INT_CRIT_EVENTS_TO_TRACK_SHOVEL = 10091;
	public static int OPT_ID_INT_NORM_EVENTS_TO_TRACK_SHOVEL = 10092;
	public static int OPT_ID_INT_CRIT_EVENTS_TO_TRACK_DUMPER = 10093;
	public static int OPT_ID_INT_NORM_EVENTS_TO_TRACK_DUMPER = 10094;
	public static int OPT_ID_INT_USE_PREDICTED_INSHOW = 10095;
	public static int OPT_DO_PREDICTED_LOAD_EXIT = 10096;
	public static int OPT_DO_OPTIMIZATION_AT_LEXIT = 10097;
	public static int OPT_DO_OPTIMIZE_Q_THRESHOLD = 10098;
	public static int OPT_OPTIMIZE_APPROACH = 10099;

//MININING RELATED
	public static int GEN_IGNORE_POINT_AFTER_INVALID = 10100;
	
	public static int SENDER_L1_PHONE = 10101;
	public static int SENDER_L2_PHONE = 10102;
	public static int TRANSPORTER_L1_PHONE = 10103;
	public static int TRANSPORTER_L2_PHONE = 10104;
	public static int CONSIGNEE_L1_PHONE = 10105;
	public static int CONSIGNEE_L2_PHONE = 10106;
	
	public static int SENDER_L1_EMAIL = 10107;
	public static int SENDER_L2_EMAIL = 10108;
	public static int TRANSPORTER_L1_EMAIL = 10109;
	public static int TRANSPORTER_L2_EMAIL = 10110;
	public static int CONSIGNEE_L1_EMAIL = 10111;
	public static int CONSIGNEE_L2_EMAIL = 10112;
	
	public static int SENDER_L1_USER = 10113;
	public static int SENDER_L2_USER = 10114;
	public static int TRANSPORTER_L1_USER = 10115;
	public static int TRANSPORTER_L2_USER = 10116;
	public static int CONSIGNEE_L1_USER = 10117;
	public static int CONSIGNEE_L2_USER = 10118;
	
	public static int STOP_EXT_DUR_SEC_COMPLETE = 10119;
	public static int PLAYBACK_DEFAULT_EVENT_STRING = 10120; //ruleid_durthreshsec_maskevent
	public static int PLAYBACK_INT_SHOW_VEHICLE_WATERMARK = 10121;
	public static int PLAYBACK_INT_SHOW_LU_OP_NAME = 10122;
	public static int PLAYBACK_INT_SHOW_LU_PTS = 10123;
	public static int PLAYBACK_INT_SHOW_ALT_LU_PTS = 10124;
	public static int PLAYBACK_INT_SHOW_LABELS_TRIP_POINTS = 10125;
	public static int PLAYBACK_INT_SHOW_LABELS_EVENT_POINTS = 10126;
	public static int PLAYBACK_INT_TRIPDATA_MASK = 10127;
	public static int PLAYBACK_INT_SHOW_CHALLAN = 10128;
	public static int STOPDIR_DALA_UP_MAX_DIST = 10129;
	
	public static int INT_SITESTAT_LEAD_BY_ADJ = 10140; //default = true
	public static int DOUBLE_SITESTAT_IGN_LO_TRAVELSECFRAC = 10141; //default = true
	public static int DOUBLE_SITESTAT_IGN_HI_TRAVELSECFRAC = 10142;
	public static int DOUBLE_SITESTAT_IGN_STDDEV = 10143;
	
}
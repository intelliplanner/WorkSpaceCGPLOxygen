package com.ipssi.common.ds.trip;

public class TripInfoConstants {
	public static final int IN = 1;
	public static final int OUT = 2;
	public static final int WAIT = 1;
	public static final int GATE = 2;
	public static final int OPERATION = 3;
	public static final int NO_REGION = 4;
	public static final int LOAD = 1;
	public static final int UNLOAD = 2;
	public static final int HYBRID_UL = 11;
    public static final int PRE_LOAD_IM = 12;
    public static final int POST_IM = 13;
    public static final int PRE_UNLOAD_IM = 14;
	public static final int HYBRID_LU = 15;
	public static final int HYBRID_ALL = 16;
	public static final int HYBRID_NONE = 17;
	public static final int STOP_BASED_OPSTATION_TEMPLATE = 19;
	public static final int STOP_BASED_OPSTATION = 18;
	public static final int STOP_BASED_OPSTATION_TEMPLATE_LOAD = 19; //dupli
	
	public static final int STOP_IGNORE = 21;
	public static boolean g_doDataLossAsNewType = false;//DEBUG13 ...leads to issue with 41142 data loss .. check that then turn to true
	public static boolean g_recordImpactOfArt = false;//DEBUG13 .. must be false is prod
	public static boolean isStopType(int type) {
		return STOP_BASED_OPSTATION_TEMPLATE == type || STOP_BASED_OPSTATION == type || STOP_IGNORE == type;  
	}
	public static final int PREFERRED_LOAD_LOWPRIORITY = 22;
	public static final int PREFERRED_UNLOAD_LOWPRIORITY = 23;
	public static final int HYBRID_UL_ALWAY = 24;
	public static final int PREFERRED_LOAD_HIPRIORITY = 25;
	public static final int PREFERRED_UNLOAD_HIPRIORITY = 26;
	
	public static final int REST_NORMAL_SUBTYPE = 10001;
	public static final int REST_TRANSIT_SUBTYPE = 10002;
	
	public static final int OFFICE = 30;	
	public static final int LOAD_TRACK_REGION = 3;
	public static final int UNLOAD_TRACK_REGION = 4;
	public static final int LOAD_OUTSIDE_VEHICLE_REGION = 5;
	public static final int UNLOAD_OUTSIDE_VEHICLE_REGION = 6;
	public static final int REST_AREA_REGION = 7;
	public static final int LOAD_OUTER_TRACK_REGION = 9;
	public static final int UNLOAD_OUTER_TRACK_REGION = 10;
	public static final int WEIGH_BRIDGE_TRACK_REGION = 11;
	public static final int TRIP_EVENT = 0;
	public static final int ROAD_DEVIATION = 1;
	public static final int LOAD_WAIT_IN = 0;
	public static final int LOAD_GATE_IN = 1;
	public static final int LOAD_AREA_IN = 2;
	public static final int LOAD_AREA_OUT = 3;
	public static final int LOAD_GATE_OUT = 4;
	public static final int LOAD_WAIT_OUT = 5;
	public static final int UNLOAD_WAIT_IN = 6;
	public static final int UNLOAD_GATE_IN = 7;
	public static final int UNLOAD_AREA_IN = 8;
	public static final int UNLOAD_AREA_OUT = 9;
	public static final int UNLOAD_GATE_OUT = 10;
	public static final int UNLOAD_WAIT_OUT = 11;
	public static final int TRIP_CONFIRM_TIME = 12;
	public static final int COMPLETE_LOAD_DATA = 13;
	public static final int COMPLETE_UNLOAD_DATA = 14;
	public static final int AREA_OF_WORK_EXIT = 15;
	public static final int AREA_OF_CONFIRM_EXIT = 16;
	//MUST BE SEQUENTIAL - USED in  isNonRegionEvent routine
	public static final int STOP_START_EVENT = 17;
	public static final int STOP_END_EVENT = 18;
	//MUST BE SEQUENTIAL- used in isDirChange
	public static final int DIR_CHANGE_EVENT_LO = 19;
	public static final int DIR_CHANGE_EVENT_HI = 20;
	public static final int DIR_CHANGE_EVENT_BOTH = 21;
	public static final int DIR_CHANGE_EVENT_SUPER = 22;
	public static final int DIR_CHANGE_EVENT_SUPER_BOTH = 23;
	
	public static final int DIR_CHANGE_EVENT_LO_SUPP = 24;
	public static final int DIR_CHANGE_EVENT_HI_SUPP = 25;
	public static final int DIR_CHANGE_EVENT_BOTH_SUPP = 26;
	public static final int DIR_CHANGE_EVENT_SUPER_SUPP = 27;
	public static final int DIR_CHANGE_EVENT_SUPER_BOTH_SUPP = 28;
	
	public static final int DIR_CHANGE_EVENT_NONE = 29;
	public static final int STOP_START_EVENT_DATALOSS = 30;
	
	public static final int EXT_DALA_UP = 41;
	public static final int EXT_DALA_DOWN = 42;
	public static final int EXT_STRIKE = 43;
	public static final int EXT_SHOVEL_LOAD = 44;
	public static final int EXT_SHOVEL_READ = 45;
	public static final int EXT_SHOVEL_UNREAD = 46;
	public static final int EXT_POSITIONING_STOP = 47;
	
	public static final int LOAD_WB1_IN = 101;
	public static final int LOAD_WB2_IN = 103;
	public static final int LOAD_WB3_IN = 105;
	public static final int UNLOAD_WB1_IN = 107;
	public static final int UNLOAD_WB2_IN = 109;
	public static final int UNLOAD_WB3_IN = 111;	
	
	public static final int WAIT_IN = 0;
	public static final int GATE_IN = 1;
	public static final int AREA_IN = 2;
	public static final int AREA_OUT = 3;
	public static final int GATE_OUT = 4;
	public static final int WAIT_OUT = 5;
	public static final int CURRENT = 1;
	public static final int DATA = 2;
	public static final int RADIUS = 6400;
	public static final int QUEUE_LENGTH = 9001;
	public static final int PROCESSING_TIME = 9002;
	public static final int NOT_OPERATING = 9003;
	public static final int STRANDED_VEHICLES = 9004;
	public static final int EMAIL = 2;
	public static final int SMS = 1;
	public static final int DELETED = 0;
	public static final int ACTIVE = 1;
	public static final int INACTIVE = 2;
	
	public static final int SIMPLE_OPTYPE_LOAD = 0;
	public static final int SIMPLE_OPTYPE_UNLOAD = 1;
	public static final int SIMPLE_OPTYPE_HYBRID = 2;
	public static final int SIMPLE_OPTYPE_STOP = 3;
	public static final int SIMPLE_OPTYPE_IGNORE = 4;
	public static final int SIMPLE_OPTYPE_GUESS_LOAD = 5;
	public static final int SIMPLE_OPTYPE_GUESS_UNLOAD = 6;
	public static int getSimplifiedOpType(int optype) {
		int retval  = SIMPLE_OPTYPE_IGNORE;
		switch (optype) {
		    case PREFERRED_LOAD_LOWPRIORITY:
		    	retval = SIMPLE_OPTYPE_GUESS_LOAD;
		    	break;
		    case PREFERRED_UNLOAD_LOWPRIORITY:
		    	retval = SIMPLE_OPTYPE_GUESS_UNLOAD;
		    	break;
		    case PREFERRED_LOAD_HIPRIORITY:
		    	retval = SIMPLE_OPTYPE_LOAD;
		    	break;
		    case PREFERRED_UNLOAD_HIPRIORITY:
		    	retval = SIMPLE_OPTYPE_UNLOAD;
		    	break;
			case LOAD :
				retval = SIMPLE_OPTYPE_UNLOAD;
				break;
			case UNLOAD :
				retval = SIMPLE_OPTYPE_UNLOAD;
			    break;
			case HYBRID_UL:
			case HYBRID_LU:
			case HYBRID_ALL:
			case HYBRID_NONE:
			case HYBRID_UL_ALWAY:
			case STOP_BASED_OPSTATION_TEMPLATE:
			case STOP_BASED_OPSTATION:
				retval = SIMPLE_OPTYPE_HYBRID;
			    break;
			case STOP_IGNORE:
				retval = SIMPLE_OPTYPE_IGNORE;
			    break;
			 default :
				 retval = SIMPLE_OPTYPE_IGNORE;
		}
		
		return retval;
	}
	
	public static boolean isGuessOpType(int simpleType) {
		return simpleType == SIMPLE_OPTYPE_HYBRID || simpleType == SIMPLE_OPTYPE_GUESS_LOAD || simpleType == SIMPLE_OPTYPE_GUESS_UNLOAD; 
	}
	
	public static long getArtificialIgnoreTime(long tm) {
		long ms = tm % 1000L;
		long sec = tm / 1000L;
		if (ms > 500)
			sec++;
		return sec*1000L;
	}
	
	
}

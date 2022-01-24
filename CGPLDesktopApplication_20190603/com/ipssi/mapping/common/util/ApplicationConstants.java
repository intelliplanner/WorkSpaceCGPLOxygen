package com.ipssi.mapping.common.util;


public class ApplicationConstants {

	public static final String realpath = "C:\\Program Files\\MapGuideOpenSource2.0\\WebServerExtensions\\www\\";
	public static final String wgs84 = "GEOGCS[\"WGS84 Lat/Long's, Degrees, -180 ==> +180\",DATUM[\"D_WGS_1984\",SPHEROID[\"World_Geodetic_System_of_1984\",6378137,298.257222932867]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
	public static final double RADIUS = 6378.137;
	public static final String LANDMARK_LAYER = "LandmarkLayer";
	public static final String Filter = "Filter";
	public static final String PLAYBACK_DATE_FORMAT = "MM/dd/yyyy";
	public static String Status = "ACTIVE";
	
	public final static String LINESTRING = "LineString";
	public final static String COMMA = ",";
	public final static String OPEN_BRACES = "(";
	public final static String CLOSE_BRACES = ")";
	public final static String POINT = "Point";
	public final static String SPACE = " ";
	
	public static final String PLAY = "play";
	public static final String PAUSE = "pause";
	public static final String STOP = "stop";
	public static final String FORWARD = "forward";
	public static final String REWIND = "rewind";
	
	public static final String VEHICLE_PLAYBACK_LAYER = "Vehicle_Playback_Layer";
	public static final String EVENT_PLAYBACK_LAYER = "Event_Playback_Layer";
	
	public static final String DATETIME = "datetime";
	public static final String EVENT_START_TIME = "event_start_time";
	public static final String GREATER_THAN = ">";
	public static final String LESS_THAN = "<";
	public static final String EQUAL = "=";
	public static final String SESSION_ID = "session_id";
	public static final String AND = "AND";
	public static final String QUOTES = "'";
	public static final String POLYGON_TEXT = "POLYGON(())";
	public static final String REGION_DEFINITION_LAYER = "RegionDefinitionLayer";
	public static final String ROUTE_DEFINITION_LAYER = "RouteDefinitionLayer";
	public static final String OR = "OR";
	public static final String ROUTE_LAYER = "Roads";

	public static final int DELETED = 0;
	public static final int ACTIVE = 1;
	public static final int INACTIVE = 2;
	
	public static final int LINE = 1;
	public static final String LINESTRING_TEXT = "LINESTRING()";
	

	public static final String CREATE = "create";
	public static final String DELETE = "delete";
	public static final String EDIT = "edit";
	public static final String SAVE = "save";
	public static final String SEARCH = "search";
	public static final String VIEW = "view";
	public static final String ACTION = "action";
	
	public static final int STOP_ORDINAL = 3;
	public static final int MOVING_ORDINAL = 5;
	public static final int REGION_ORDINAL = 1;
	public static final int STRANDED_ORDINAL = 2;
	public static final int DATA_ORDINAL = 4;
	
	public static final int PLAYBACK_ANALYSIS_MODE = 1;
	public static final int PLAYBACK_DATA_MODE = 0;
	//public static final String MAPDATAPATH = "C:\\working\\EclipseWorkspace\\LocTracker\\config_server\\mapdata\\Polygon\\";
	public static final String MAPDATAPATH = "/home/mapdata/Polygon/";
}

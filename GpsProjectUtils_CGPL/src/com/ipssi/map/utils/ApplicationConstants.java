package com.ipssi.map.utils;

import com.ipssi.gen.utils.Misc;

public class ApplicationConstants {

	public static final String realpath = "C:\\Program Files\\MapGuideOpenSource2.0\\WebServerExtensions\\www\\";
	public static final String wgs84 = "GEOGCS[\"WGS84 Lat/Long's, Degrees, -180 ==> +180\",DATUM[\"D_WGS_1984\",SPHEROID[\"World_Geodetic_System_of_1984\",6378137,298.257222932867]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
	public static final double EARTH_RADIUS = 6378.137;
	public static final double RADIUS = 6378.137;
	public static final String LANDMARK_LAYER = "LandmarkLayer";
	public static final String Filter = "Filter";
	public static final String PLAYBACK_DATE_FORMAT = Misc.G_DEFAULT_DATE_FORMAT;
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

//	public static final String DELETED = "DELETED";
	public static final int LINE = 1;
	public static final String LINESTRING_TEXT = "LINESTRING()";
	public static final int RECTANGLE = 2;;
	public static final String BASE_MAP_NAME = "Sheboygan";
	public static final int DIST_DIM = 0;
	public static final int IGN_ONOFF = 2; //rajeev changed 2013-02-10 ... was 1 ... but in internal it is 2
	public static final int BATTERY_ONSTATUS = 1;//rajeev changed 2013-02-10 ... was 2
	public static final int FUEL_LEVEL = 3;
	public static final int RPM = 21;
	
	public static final int LOGGED_DATA_MAX_NAME_LEN = 80;
}

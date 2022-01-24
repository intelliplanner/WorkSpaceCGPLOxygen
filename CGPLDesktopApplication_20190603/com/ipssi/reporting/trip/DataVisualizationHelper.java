package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.tracker.vehicle.VehicleDao;
/*
 * Notes:
 * Users specifies the dim to be shown on Y1 axis, Y2 axis. In addition user may select binary data(s) to be shown and events to be shown. On x-axixs currently
 * timestamp or cumm dist can be shown
 * Let us first look at how to specify:
 * Y1, Y2, Bin, X - list of id in internal to show. All of these can only be fetched from logged_data table and the column will consist of 
 * attribute_value or speed followed by _ and attribute_id followed by _ and cumm func (cumm func can be moving average, median or cumm
 * Thus to show cumm dist on x-axis, use attribute_value_0_cumm as the column name
 * 
 * Now let us look at how things will be shown
 * Y1, Y2 against X is simple - we will either ensure color across different vehicles to be different or draw style to be different
 * events and bin data are plotted similarly - the begin or 1 will be indicated by full and other by empty of same shape. Y val will be the same as the value of 1st Y1 measure
 * as was at the time.
 * 
 * Now how data is fetched - the core function is:
 *getDataForCharting(TotalResults retval, Date start, Date end, Connection conn, int getForFirstNVehicles)
 *TotResults contains the list of vehicles for which to find, list of measures etc ...
 *We generate query order by vehicle and by attribute id (but taking care that attribute asked for x is at the beginning)
 *after that look at the function
 *
 *To make x values' consistent, we convert time to ms, double to *1000 and then convert to long
 */
public class DataVisualizationHelper {
	 public static int g_y1DimId = 20624;
	 public static int g_y2DimId = 20625;
	 public static int g_binDimId = 20626;
	 public static int g_xDimId = 20627;
	 public static int g_simpleSpeed = 20628;
	 public static int g_gpsRecTime = 20134;
	 public static int g_averagingWinow = 20629;
	 public static int g_eventThresholdMin = 20641;
	 private static class HelperGpsTimeXVal implements Comparable {
		 public Date gpsRecTime = null;
		 public long xVal = 0;
		 public double speed =Misc.LARGE_NUMBER;
		 public HelperGpsTimeXVal(Date gpsRecTime, double speed) {
			 this.gpsRecTime = gpsRecTime;
			 if (gpsRecTime != null)
				 xVal =  gpsRecTime.getTime();
			 this.speed = speed;
		 }
		 public HelperGpsTimeXVal(Date gpsRecTime, long xVal, double speed) {
			 this.gpsRecTime = gpsRecTime;
			 if (Misc.isUndef(xVal)) {
				 xVal = gpsRecTime == null ? 0 : gpsRecTime.getTime();
			 }
			 this.xVal = xVal;
			 this.speed = speed;
		 }
		 public int compareTo(Object obj) {		
			 HelperGpsTimeXVal p = (HelperGpsTimeXVal)obj;
				return this.gpsRecTime.compareTo(p.gpsRecTime);		
		 }
	 }
	 
	 private static class HelperGetY1Val implements Comparable {
		 public long xVal = Misc.getUndefInt();
		 public double yVal = 0;
		 public HelperGetY1Val(long xVal) {
			 this.xVal = xVal;
		 }
		 public HelperGetY1Val(long xVal, double yVal) {
			this.yVal = yVal;
			 this.xVal = xVal;
		 }
		 public int compareTo(Object obj) {		
			  HelperGetY1Val p = (HelperGetY1Val)obj;
				return xVal < p.xVal ?-1 : xVal == p.xVal ? 0 : 1;		
		 }
	 }
	 
	 public static class VehicleSpecificResults {
		 public ArrayList<ArrayList<Pair<Long, Double>>> y1DataList = new ArrayList<ArrayList<Pair<Long, Double>>>(); //indexed by measures asked for
		 public ArrayList<ArrayList<Pair<Long, Double>>> y2DataList = new ArrayList<ArrayList<Pair<Long, Double>>>(); //indexed by measures asked for
		 public ArrayList<ArrayList<Pair<Long, Long>>> binDataList = new ArrayList<ArrayList<Pair<Long, Long>>>();//indexed by bin measures asked 1st of date is when 1, 2nd when 0
		 public ArrayList<ArrayList<Pair<Long, Long>>> eventData = new ArrayList<ArrayList<Pair<Long, Long>>>(); //indexed by Id's asked for
		 public ArrayList<Pair<Long, String>> locationData = new ArrayList<Pair<Long, String>>();
		 private FastList<HelperGetY1Val> y1valLookupHelperForBin = null;
		 public boolean binOnY1() {
			 for (int art=0;art<2;art++) {
				 ArrayList<ArrayList<Pair<Long,Double>>> list = art == 0 ? y1DataList : y2DataList;
				 for (int i=0,is=list.size();i<is;i++) {
					 if (list.get(i).size() > 0)
						 return art == 0;
				 }
			 }
			 return true;
		 }
		 public void resetY1ValLookupHelperForBin() {
			 y1valLookupHelperForBin = null;
		 }
		 public double getY1valForBin(long v1) {
			 //populate y1valLookupHelperForBin if necessary
			 if (y1valLookupHelperForBin == null) {
				 y1valLookupHelperForBin = new FastList<HelperGetY1Val>();
				 ArrayList<Pair<Long, Double>> useThisToPopulate = null;
				 for (int art=0;art<2;art++) {
					 ArrayList<ArrayList<Pair<Long,Double>>> list = art == 0 ? y1DataList : y2DataList;
					 for (int i=0,is=list.size();i<is;i++) {
						 if (list.get(i).size() > 0) {
							 useThisToPopulate = list.get(i);
							 break;
						 }
					 }
				 }
				 if (useThisToPopulate != null) {
					 for (Pair<Long, Double> entry : useThisToPopulate ) {
						 y1valLookupHelperForBin.add(new HelperGetY1Val(entry.first, entry.second) );
					 }
				 }
			 }
			 //populated ... now get less than or equal entry
			 HelperGetY1Val lookup = new HelperGetY1Val(v1);
			 HelperGetY1Val res = y1valLookupHelperForBin.get(lookup);
			 if (res == null)
				 if (y1valLookupHelperForBin.size() == 0)
					 return 1;
				 else
					 return y1valLookupHelperForBin.get(0).yVal;
			 else if (y1valLookupHelperForBin.isAtEnd(lookup)) {
				 return res.yVal;
			 }
			 else {
				 if (res.xVal == lookup.xVal)
					 return res.yVal;
				 HelperGetY1Val next = y1valLookupHelperForBin.get(lookup, 1);
				 if (Misc.isEqual(next.yVal, res.yVal))
					 return res.yVal;
				 return (next.yVal-res.yVal)*(double)(lookup.xVal - res.xVal)/(double)(next.xVal - res.xVal)+res.yVal;
			 }
		 }
	 }
	 
	 public static class TotalResults {
		 public ArrayList<Pair<Integer, String>> y1MeasureList = new ArrayList<Pair<Integer, String>>();
		 public ArrayList<Pair<Integer, String>> y2MeasureList = new ArrayList<Pair<Integer, String>>();
		 public ArrayList<Pair<Integer, String>> binMeasureList = new ArrayList<Pair<Integer, String>>();
		 public ArrayList<Pair<Integer, String>> eventList = new ArrayList<Pair<Integer, String>>();
		 public ArrayList<Pair<Integer, String>> vehicleList = new ArrayList<Pair<Integer, String>>();
		 public int xaxis = 20134;
		 public int averagingWindow = 5;
		 public int eventThreshold  = 3;
		 public double filterDistLessThan = -1;
		 public ArrayList<VehicleSpecificResults> data = new ArrayList<VehicleSpecificResults>(); //indep
		 public int xDataType = Cache.DATE_TYPE; //could be integer, double. ... if date then ms, if int then int, if double then val = val/1000
		 public int nVehicles = 1;
		 public Date start = null;
		 public Date end = null;
		 public int colorIndexStart = 0;
	 }
	 public static void main(String[] args) {
			Connection conn = null;
			boolean destroyIt = false;
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				Cache.getCacheInstance(conn);
				TotalResults results = new TotalResults();
				results.vehicleList.add(new Pair<Integer, String>(15989, "15989-MICT"));
				results.y1MeasureList.add(new Pair<Integer, String>(20630, "Fuel (Raw)"));
				results.y1MeasureList.add(new Pair<Integer, String>(20632, "Fuel (Moving Avg)"));
				results.y1MeasureList.add(new Pair<Integer, String>(20633, "Fuel (Median Filtered)"));
				results.y2MeasureList.add(new Pair<Integer, String>(20628, "Speed"));
				results.binMeasureList.add(new Pair<Integer, String>(20266, "Ign On/Off"));
				results.eventList.add(new Pair<Integer, String>(1, "Stoppage"));
				results.averagingWindow = 5;
				results.eventThreshold = 3;
				results.filterDistLessThan = -1;
				results.xaxis = 20640;
				results.xDataType = Cache.DATE_TYPE;
				Date start = new Date(112,5,4);
				Date end = new Date(112,5,5);
				results.start = start;
				results.end = end;
				DataVisualizationHelper.getDataForCharting(results, start, end, conn, 1);
				StringBuilder sb = DataVisualizationHelper.generateFlotData(results, false,"graph_jscript");
			   // System.out.println(sb);	
			}
			catch (Exception e) {
				destroyIt = true;
			}
			finally {
				
			}
	 }
	 
     public static TotalResults getDataForChartingBySearch(SessionManager session, Connection conn, int maxVehiclesToGet, String pageContext, boolean doJSON) throws Exception {
    	 TotalResults retval = new TotalResults();
		 //1. read search box to find the data measures to show on Y1- axis and Y2-axis and measure axis
		 //2. read search box to find the eventId's to show
    	 //3. get list of vehicles to show
    	 //4. get the data ... by calling the detailedfunction
    	 
    	 //1 reading measures to show
    	 MiscInner.SearchBoxHelper searchBoxHelper = com.ipssi.gen.utils.Misc.preprocessForSearchBeforeView(session.request, pageContext);
    	 String topContext = searchBoxHelper == null ? null : searchBoxHelper.m_topPageContext;
    	 if (topContext == null)
    		 topContext = "p";
    	 readParamList(topContext, g_y1DimId, session, retval.y1MeasureList);
    	 readParamList(topContext, g_y2DimId, session, retval.y2MeasureList);
    	 readParamList(topContext, g_binDimId, session, retval.binMeasureList);
    	 //get events to show ...
    	 ArrayList<Integer> tempList = new ArrayList<Integer>();
    	 Misc.convertValToVector(session.getParameter(topContext+20141), tempList);
    	 if (tempList.size() > 0) {
    		 DimInfo ruleDim = DimInfo.getDimInfo(20141);
    		 ArrayList<DimInfo.ValInfo> valList = ruleDim.getValList(conn, session);
    		 if (valList != null && valList.size() > 0) { 
		    	 for (Integer i1:tempList) {
		    		 for (DimInfo.ValInfo v : valList) {
		    			 if (v.m_id == i1) {
		    				 retval.eventList.add(new Pair<Integer, String>(v.m_id, v.m_name));
		    				 break;
		    			 }
		    		 }
		    	 }
    		 }
    	 }
    	 retval.xaxis = Misc.getParamAsInt(session.getParameter(topContext+g_xDimId), retval.xaxis); 
    	 retval.filterDistLessThan = Misc.getParamAsDouble(session.getParameter(searchBoxHelper.m_topPageContext+g_simpleSpeed), retval.filterDistLessThan);
    	 
    	 SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
 		 SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
 		 Date stDate = readDate(session.getParameter(topContext+20023), sdfTime, sdf);
 		 Date enDate = readDate(session.getParameter(topContext+20034), sdfTime, sdf);
 		 if (stDate == null && enDate == null) {
 			 stDate = new Date();
 			 Misc.addDays(stDate, -1);
 			 enDate = new Date();
 		 }
 		 if (stDate == null && enDate != null) {
 			 stDate = new Date(enDate.getTime());
 			 Misc.addDays(stDate, -1);
 		 }
 		 else if (stDate != null && enDate == null) {
 			 enDate = new Date(stDate.getTime());
 			 Misc.addDays(enDate, 1);
 		 }
 		 retval.start = stDate;
 		 retval.end = enDate;
 		 retval.averagingWindow = Misc.getParamAsInt(session.getParameter(topContext+g_averagingWinow), retval.averagingWindow);
 		 retval.eventThreshold = Misc.getParamAsInt(session.getParameter(topContext+g_eventThresholdMin), retval.eventThreshold);
    	 //get list of vehicles ... the query will be generated using generalized query
 		 PreparedStatement ps = VehicleDao.getVehicleDataQuery(session, pageContext, true);
 		 ResultSet rs = ps.executeQuery();
 		 while (rs.next()) {
 			retval.vehicleList.add(new Pair<Integer, String>(rs.getInt(1), rs.getString(2)));
 		 }
 		 rs.close();
 		 ps.close();
 		 retval.colorIndexStart = Misc.getParamAsInt(session.getParameter("last_color"), 0);
 		 
 		 getDataForCharting(retval, stDate, enDate, conn, maxVehiclesToGet);
    	 return retval;
     }
     
     
     private static class AvgMedianHolder {
		 double medianSpeed = 0;
		 double medianVal = 0;
		 double avgSpeed = 0;
		 double avgVal = 0;
		 double cummSpeed = 0;
		 double cummVal = 0;
		 double cummValMinutes = 0;
		 double cummSpeedMinutes = 0;
		 public void reset() {
			 medianSpeed = 0;
			 medianVal = 0;
			 avgSpeed = 0;
			 avgVal = 0;
			 cummSpeed = 0;
			 cummVal = 0;
			 cummSpeedMinutes = 0;
			 cummValMinutes = 0;
		 }
	 }  
     
	 public static void getDataForCharting(TotalResults retval, Date start, Date end, Connection conn, int getForFirstNVehicles) throws Exception {
    	 try {
    		 //Generate the query for getting data from logged_data .. we will look at diminfo for y1, y2, binmeasure use column val to interpret which col/attrib ...
    		 //the format is attribute_value|speed_<attribId>_ma|median
    		 //data for 0 is always obtained .. to populate other stuff .. (like names etc)
    		 //and then for each attribute_id asked for the measure ... we  keep an array of relevant measure and calc spec for that measure ...
    		 //relevant measure is pair of two ints - 1st is  (y1=0 index, y2=1 index, bin=3 indes, x-axis=4 index) 2nd is index in the resp measure list
    		 //
    		 
    		 StringBuilder sb = new StringBuilder();
    		 int nvehicles = 0;
    		 int xaxisAttrib = -1;
    		 int xaxisValOrSpeed = 0;
    		 int xaxisAveraging = 0;
    		 sb.append("select vehicle_id, attribute_id, gps_record_time, attribute_value, speed, name from logged_data where vehicle_id in (");
    		 for (int i=0, is = getForFirstNVehicles < retval.vehicleList.size() ? getForFirstNVehicles : retval.vehicleList.size(); i<is ;i++) {
    			 if (i != 0)
    				 sb.append(",");
    			 nvehicles++;
    			 sb.append(retval.vehicleList.get(i).first);
    		 }
    		 retval.nVehicles = nvehicles;
    		 if (nvehicles <= 0)
    			 return;
    		 sb.append(") and gps_record_time >= ? and gps_record_time <= ? and attribute_id in ( ");
    		 StringBuilder sb1 = new StringBuilder();
    		 //below is for looking up measure list and how to calculate for that measure given one attribute id
    		 HashMap<Integer, ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>>> askLookup = new HashMap<Integer, ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>>>();
    		 //key = attrib id, value arraylist of things of data being asked. 1st = art, 2=index in array. Pair.1st = value or speed, Pair.2nd - value or Moving Avg or Median

    		 //add for 0 ... the dist dim
    	     ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>> lookupVal = new ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>>();
    	     askLookup.put(0, lookupVal);
    	     sb.append("0");
		     lookupVal.add(new Triple<Integer, Integer, Pair<Integer, Integer>>(-1, 0, new Pair<Integer, Integer>(0, 0)));
    		 boolean dataNeedsToBeFound = false;
    		 SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
    		 for (int art=0;art<4;art++) {
    			 ArrayList<Pair<Integer, String>> list = art == 0 ? retval.y1MeasureList : art == 1 ? retval.y2MeasureList : art == 2 ? retval.binMeasureList : null;
    			 int sz = art < 3 ? list == null ? 0 : list.size() : 1;
    			 for (int i=0;i<sz;i++) {
    				 int dim = art < 3? list.get(i).first : retval.xaxis;
    				 DimInfo dimInfo = DimInfo.getDimInfo(dim);
    				 if (dimInfo == null)
    					 continue;
    				 dataNeedsToBeFound =true;
    				 String column = dimInfo.m_colMap.column;
    				 if (column.equals("gps_record_time")) {
    					 if (art == 3) {
	    					 xaxisAttrib = -1;
	    		    		 xaxisValOrSpeed = 0;
	    		    		 xaxisAveraging = 0;
	    		    		 retval.xDataType = Cache.DATE_TYPE;
	    		    		 lookupVal = new ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>>();
	    		    		 lookupVal.add(new Triple<Integer, Integer, Pair<Integer, Integer>>(3,0,new Pair<Integer, Integer>(0,0)));
	    		    		 askLookup.put(-1, lookupVal);
    					 }
    				 }
    				 else {
	    				 StringTokenizer strtok = new StringTokenizer(column,"_",false);
	    				 int attrib = 0;
	    				 int valOrSpeed = 0;
	    				 int averaging = 0;
	    				 boolean skipNext = false;
	    			     while(strtok.hasMoreTokens()) {
					           String s = strtok.nextToken();
					           if (skipNext) {
					        	   skipNext = false;
					        	   continue;
					           }
					           if (s.equals("attribute")) {
					        	   skipNext = true;
					        	   valOrSpeed = 0;
					           }
					           else if (s.equals("speed")) {
					        	   valOrSpeed = 1;
					           }
					           else {
					        	   int temp = Misc.getParamAsInt(s);
					        	   if (!Misc.isUndef(temp)) {
					        		   attrib = temp;
					        	   }
					        	   else {
					        		   if ("ma".equals(s))
					        			   averaging = 1;
					        		   else if ("median".equals(s))
					        			   averaging = 2;
					        		   else if ("cumm".equals(s)) {
					        			   averaging = 3;
					        			   if (attrib == 0) //hack ...dist is already cumm
					        				   averaging = 0;
					        		   }
					        		   else if ("cummdur".equals(s)) {
					        			   averaging = 4;
					        		   }
					        	   }
					           }
	    			     }//end if interpreting the column val
    				 
	    			     if (art == 3) {//for xaxis
	    			    	 xaxisAttrib = attrib;
	    		    		 xaxisValOrSpeed = valOrSpeed;
	    		    		 xaxisAveraging = averaging;
	    		    		 retval.xDataType = Cache.NUMBER_TYPE;
	    			     }
	    			     lookupVal = askLookup.get(attrib);
	    			     if (lookupVal == null) {
	    			    	 lookupVal = new ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>>();
	    			    	 askLookup.put(attrib, lookupVal);
	    			    	  sb.append(",").append(attrib);
	    			     }
	    			     if (art >= 3) {//make sure that xaxis is processed 1st
	    			    	 lookupVal.add(0, new Triple<Integer, Integer, Pair<Integer, Integer>>(art, i, new Pair<Integer, Integer>(valOrSpeed, averaging)));
	    			     }
	    			     else {
	    			    	 lookupVal.add(new Triple<Integer, Integer, Pair<Integer, Integer>>(art, i, new Pair<Integer, Integer>(valOrSpeed, averaging)));
	    			     }
    				 }//regular pattern
    			 }  //for each measure being asked
    		 }//for art
    		 
    		 sb.append(") order by vehicle_id, ");
    		 if (xaxisAttrib > 0) { //to ensure that xaxis calcl value is read first
    			 sb.append(" (case when attribute_id = ").append(xaxisAttrib).append(" then -1 else attribute_id end), ");
    		 }
    		 else {
    			 sb.append(" attribute_id, ");
    		 }
    		 sb.append(" gps_record_time ");
    		 createEmpty(retval, nvehicles);
    		 VehicleSpecificResults dataHolder = null;
    		 ArrayList<Triple<Integer, Integer, Pair<Integer, Integer>>> relevanceInfo = null;
    		 ArrayList<FastList<HelperGpsTimeXVal>> xvalLookupList = new ArrayList<FastList<HelperGpsTimeXVal>>();
    		 for (int i=0,is=retval.vehicleList.size();i<is;i++) {
    			 xvalLookupList.add(new FastList<HelperGpsTimeXVal>());
    		 }
    		 FastList<HelperGpsTimeXVal> xvalLookup = null;;

    		 if (dataNeedsToBeFound) {
	    		 PreparedStatement ps = conn.prepareStatement(sb.toString());
	    		 ps.setTimestamp(1, Misc.utilToSqlDate(start));
	    		 ps.setTimestamp(2, Misc.utilToSqlDate(end));
	    		 ResultSet rs = ps.executeQuery();
	    		 int prevVehicle = Misc.getUndefInt();
	    		 int prevAttrib = Misc.getUndefInt();
	    		 ArrayList<Double> valWindow = new ArrayList<Double>();
	    		 ArrayList<Double> speedWindow = new ArrayList<Double>();
	    		 boolean needsValAvg = false;
	    		 boolean needsSpeedAvg = false;
	    		 boolean needsValMedian = false;
	    		 boolean needsSpeedMedian = false;
	    		 AvgMedianHolder avgInfo = new AvgMedianHolder();
	    		 Date prevTime = null;
	    		 double prevVal = Misc.getUndefDouble();
	    		 double prevSpeed = Misc.getUndefDouble();
	    		 Date oneStartTime = null;
	    		 while (rs.next()) {
	    			 int vehicleId = rs.getInt(1);
	    			 int attribId = rs.getInt(2);
	    			 Date gpsRecTime = Misc.sqlToUtilDate(rs.getTimestamp(3));
	    			 double val = rs.getDouble(4);
	    			 double speed = rs.getDouble(5);
	    			//for negative temp if (attribId == 3 && (speed <= 0 || val <= 0)) { //hack .. we also need to figure out the max
	    			//	 continue;
	    			// }
	    			 double speedValForFilter = attribId == 0 ? speed : Misc.LARGE_NUMBER;
	    			 String name = rs.getString(6);
	    			 name = name.replaceAll("[^A-Za-z0-9_]", "_");
	    			 if (prevVehicle != vehicleId)
	    				 prevAttrib = Misc.getUndefInt();
	    			 if (prevAttrib != attribId || vehicleId != prevVehicle) {
	    				 valWindow.clear();
	    				 speedWindow.clear();
	    				 dataHolder = null;
	    				 relevanceInfo = null;
	    				 avgInfo.reset();
	    				 prevTime = null;
	    				 prevVal = Misc.getUndefDouble();
	    				 prevSpeed = Misc.getUndefDouble();
	    	    		 needsValAvg = false;
	    	    		 needsSpeedAvg = false;
	    	    		 needsValMedian = false;
	    	    		 needsSpeedMedian = false;
	    			 }
	    			 if (dataHolder == null) {
	    				 for (int i=0,is=retval.vehicleList.size();i<is;i++) {
	    					 if (retval.vehicleList.get(i).first == vehicleId) {
	    						 dataHolder = retval.data.get(i);
	    						 xvalLookup = xvalLookupList.get(i);
	    						 break;
	    					 }
	    				 }
	    			 }
	    			 if (relevanceInfo == null) {
	    				 relevanceInfo = askLookup.get(attribId);
	        			 for (int i=0,is=relevanceInfo == null ? 0 : relevanceInfo.size(); i<is;i++) {
	        				 Pair<Integer, Integer> howto = relevanceInfo.get(i).third;
	        				 if (howto.second == 1) {
	        					 if (howto.first == 0)
	        						 needsValAvg = true;
	        					 else
	        						 needsSpeedAvg = true;
	        				 }
	        				 else if (howto.second == 2) {
	        					 if (howto.first == 0)
	        						 needsValMedian = true;
	        					 else 
	        						 needsSpeedMedian = true;
	        				 }
	        			}
	    			 }
	    			 if (dataHolder == null || relevanceInfo == null)
	    				 continue;
	    			 helpSetAvgEtc(val, speed, valWindow, speedWindow, needsValAvg, needsSpeedAvg, needsValMedian, needsSpeedMedian, avgInfo, retval.averagingWindow, prevTime, prevVal, prevSpeed, gpsRecTime);
	    			 prevVal = val;
	    			 prevSpeed = speed;
	    			 prevTime = gpsRecTime;
	    			 long xval = Misc.getUndefInt();
	    			 double speedAtXVal = Misc.LARGE_NUMBER;
	    			 for (int j=0, js=relevanceInfo.size();j<js;j++) {
	    				 Triple<Integer, Integer, Pair<Integer, Integer>> forWhat = relevanceInfo.get(j);
	    				 int iIndex = forWhat.first;
	    				 int jIndex = forWhat.second;
	    				 int valOrSpeed = forWhat.third.first;
	    				 int averaging = forWhat.third.second;
	    				 double valToUse = val;
	    				 if (averaging == 0) {
	    					 valToUse = valOrSpeed == 0 ? val : speed;
	    				 }
	    				 else if (averaging == 1) {
	    					 valToUse = valOrSpeed == 0 ? avgInfo.avgVal : avgInfo.avgSpeed;
	    				 }
	    				 else if (averaging == 2) {
	    					 valToUse = valOrSpeed == 0 ? avgInfo.medianVal : avgInfo.medianSpeed;
	    				 }
	    				 else if (averaging == 3) {
	    					 valToUse = valOrSpeed == 0 ? avgInfo.cummVal : avgInfo.cummSpeed;
	    				 }
	    				 else if (averaging == 4) {
	    					 valToUse = valOrSpeed == 0 ? avgInfo.cummValMinutes : avgInfo.cummSpeedMinutes;
	    				 }
	    				 if (Misc.isUndef(xval)) {
	    					 DataVisualizationHelper.HelperGpsTimeXVal t1 = new HelperGpsTimeXVal(gpsRecTime,0);
	    					 HelperGpsTimeXVal t2 = xvalLookup.get(t1);
	    					 if (t2 != null) {
	    						 if (xaxisAttrib > 0 && attribId == 0) {
	    							 t2.speed = speed;
	    						 }
	    						 xval = t2.xVal;
	    						 speedAtXVal = t2.speed;
	    					 }
	    					 else if (xaxisAttrib < 0) {
	    						 xval = gpsRecTime.getTime();
	    						 speedAtXVal = speedValForFilter;
	    					 }
	    				 }
	    				 if (iIndex == 3) {//doing stuff for ... calculation of x-axis
	    					 xvalLookup.add(new HelperGpsTimeXVal(gpsRecTime, retval.xaxis <= 0 ? gpsRecTime.getTime() : (long) (valToUse*1000),speedValForFilter));
	    					 xval = retval.xaxis <= 0 ? gpsRecTime.getTime() : (long) (valToUse*1000);
	    					 speedAtXVal = speedValForFilter;
	    				 }
	    				 else if (iIndex == -1) {//for loc stuff
	    					 if (xaxisAttrib < 0) {
	    						 xvalLookup.add(new HelperGpsTimeXVal(gpsRecTime, speedValForFilter));
	    						 xval = gpsRecTime.getTime();
	    						 speedAtXVal = speedValForFilter;
	    					 }

	    					 if (speedAtXVal >= retval.filterDistLessThan && name != null && name.length() != 0) {
		    					 String adjName = name; //replacement already done ..//name.replaceAll("[^A-Za-z0-9_]", "_");//name.replace('\n', '_');
		    					 
		    					 if (retval.xDataType != Cache.DATE_TYPE) {
		    						 adjName = name+"<br/>Time:"+sdf.format(gpsRecTime);
		    					 }
		    					 if (dataHolder.locationData.size() == 0 || !dataHolder.locationData.get(dataHolder.locationData.size()-1).second.equals(adjName)) {
		    						 dataHolder.locationData.add(new Pair<Long, String>(xval, adjName));
		    					 }
	    					 }
	    				 }
	    				 else if (iIndex == 0 || iIndex == 1)  {//regular stuff
	    					 ArrayList<Pair<Long,Double>> addToList = iIndex == 0 ? dataHolder.y1DataList.get(jIndex) : dataHolder.y2DataList.get(jIndex);
	    					 Pair<Long, Double> prevEntry = addToList.size() > 0 ? addToList.get(addToList.size()-1) : null;
	    					 Pair<Long, Double> prevPrevEntry = addToList.size() > 1 ? addToList.get(addToList.size()-2) : null;
	    					 if (prevEntry != null && prevPrevEntry != null && Misc.isEqual(prevEntry.second.doubleValue(), prevPrevEntry.second.doubleValue()) && Misc.isEqual(prevEntry.second.doubleValue(), valToUse)) {
	    						 prevEntry.first = xval;
	    						 prevEntry.second = valToUse;
	    					 }
	    					 else {
		    					 if (speedAtXVal >= retval.filterDistLessThan) {
		    						 addToList.add(new Pair<Long, Double>(xval, valToUse));
		    					 }
	    					 }
	    				 }
	    				 else {//doing binary data ...
	    					 ArrayList<Pair<Long,Long>> dataList = dataHolder.binDataList.get(jIndex);
	    					 boolean isOne = valToUse > 0.5;
	    					 Pair<Long,Long> lastEntry = dataList.size() != 0 ? dataList.get(dataList.size()-1) : null;
	    					 
	    					 if (lastEntry == null) {
	    						if (isOne) {
	    							dataList.add(new Pair<Long,Long>(xval, null));
	    							oneStartTime = gpsRecTime;
	    						}
	    						else {
	    							dataList.add(new Pair<Long, Long>(null, xval));
	    							oneStartTime = null;
	    						}
	    					 }
	    					 else {
	    						 if (isOne) {//if prev was 1, meaning lastEntry seond is null then
	    							 if (lastEntry.second != null) {
	    								 dataList.add(new Pair<Long, Long>(xval, null));
	    								 oneStartTime = gpsRecTime;
	    							 }
	    						 }
	    						 else {
	    							 if (lastEntry.second == null) {
	    								 lastEntry.second = xval;
	    								 if (oneStartTime != null && ((double)(gpsRecTime.getTime()-oneStartTime.getTime())/(1000.0*60.0)) < retval.eventThreshold && !gpsRecTime.equals(oneStartTime))
	    									 dataList.remove(dataList.size()-1);
	    							 }
	    						 }
	    					 }//
	    				 }//special processing of bin data list
	    			 }//for each impact point
	    			 prevVehicle = vehicleId;
	    			 prevAttrib = attribId;
	    		 }
	    		 rs.close();
	    		 ps.close();
    		 }
    		 
    		 //Now read the eventList .
    		 if (retval.eventList.size() > 0) {
	    		 sb.setLength(0);
	    		 sb.append("select vehicle_id, rule_id, event_start_time, event_stop_time from engine_events where vehicle_id in (");
	    		 for (int i=0, is = getForFirstNVehicles < retval.vehicleList.size() ? getForFirstNVehicles : retval.vehicleList.size(); i<is ;i++) {
	    			 if (i != 0)
	    				 sb.append(",");
	    			 nvehicles++;
	    			 sb.append(retval.vehicleList.get(i).first);
	    		 }
	    		 sb.append(") and rule_id in (");
	    		 for (int i=0, is = retval.eventList.size(); i<is ;i++) {
	    			 if (i != 0)
	    				 sb.append(",");
	    			 sb.append(retval.eventList.get(i).first);
	    		 }
	    		 sb.append(") and event_start_time >= ? and event_start_time <= ? ");
	    		 PreparedStatement ps = conn.prepareStatement(sb.toString());
	    		 ps.setTimestamp(1, Misc.utilToSqlDate(start));
	    		 ps.setTimestamp(2, Misc.utilToSqlDate(end));
	    		 ResultSet rs = ps.executeQuery();
	    		 int prevVehicleId = Misc.getUndefInt();
	    		 int prevRuleId = Misc.getUndefInt();
	    		 int prevVehicle = Misc.getUndefInt();
	    		 
	    		 dataHolder = null;
	    		 xvalLookup = null;
	    		 int ruleIndex = -1;
	    		 while (rs.next()) {
	    			 int vehicleId = rs.getInt(1);
	    			 int ruleId = rs.getInt(2);
	    			 Date st = Misc.sqlToUtilDate(rs.getTimestamp(3));
	    			 Date en = Misc.sqlToUtilDate(rs.getTimestamp(4));
	    			 if (prevVehicleId != vehicleId) {
	    				 dataHolder = null;
	    			 }
	    			 if (ruleId != prevRuleId) {
	    				 for (int i=0,is=retval.eventList.size(); i<is;i++) {
	    					 if (retval.eventList.get(i).first == ruleId) {
	    						 ruleIndex = i;
	    						 break;
	    					 }
	    				 }
	    			 }
	    			 if (dataHolder == null) {
	    				 for (int i=0,is=retval.vehicleList.size();i<is;i++) {
	    					 if (retval.vehicleList.get(i).first == vehicleId) {
	    						 dataHolder = retval.data.get(i);
	    						 xvalLookup = xvalLookupList.get(i);
	    						 break;
	    					 }
	    				 }
	    			 }
	    			 if (dataHolder == null || ruleIndex < 0)
	    				 continue;
	    			 if (en != null && st != null && ((double)(en.getTime()-st.getTime())/(1000.0*60.0)) < retval.eventThreshold  && !en.equals(st) ) {
	    				 continue;
	    			 }
	    			 Long xval1 = null;
	    			 Long xval2 = null;
	    			 
	    			 if (st != null) {
	    				 HelperGpsTimeXVal lookup = new HelperGpsTimeXVal(st,0);
	        			 HelperGpsTimeXVal lookupres = xvalLookup.get(lookup);
	        			 if (lookupres != null)
	        				 xval1 = new Long(lookupres.xVal);
	    			 }
	    			 if (en != null) {
	    				 HelperGpsTimeXVal lookup = new HelperGpsTimeXVal(en,0);
	        			 HelperGpsTimeXVal lookupres = xvalLookup.get(lookup);
	        			 if (lookupres != null)
	        				 xval2 = new Long(lookupres.xVal);
	    			 }
	    			 dataHolder.eventData.get(ruleIndex).add(new Pair<Long, Long>(xval1, xval2));
	    			 prevVehicleId = vehicleId;
	    			 prevRuleId = ruleId;
	    		 }
	    		 rs.close();
	    		 ps.close();
    		 }
    		 return;
    	 }
    	 catch (Exception e) {
    		 e.printStackTrace();
    		 throw e;
    	 }
     }
	 
	 public static StringBuilder generateFlotData(TotalResults results, boolean doJSON, String pageContext) {
		 StringBuilder sb = new StringBuilder();
		 if (!doJSON) {
			 sb.append("var lastColor = 0;");
		 }
		 if (!doJSON && results.xDataType == Cache.DATE_TYPE) {
			 
			 sb.append("options.xaxes[0].mode = \"time\"\n");
		 }
		 if (!doJSON) {
			 sb.append("var alldata=");
		 }
		sb.append("{\n");
		sb.append("\"datasets\": {\n");
		 boolean firstDataSetPrinted = false;
		 ArrayList<Integer> colorIndex = new ArrayList<Integer>();
		 int currColorIndex = 0;
		 for (int i=0,is=results.data.size();i<is;i++) {
			 colorIndex.add(currColorIndex);
			 VehicleSpecificResults data = results.data.get(i);
			 String vehName = results.vehicleList.get(i).second;
			 //do y1/y2
			
			 for (int art=0;art<2;art++) {
				ArrayList<ArrayList<Pair<Long, Double>>> allDataList = art == 0 ? data.y1DataList : data.y2DataList; 
				for (int j=0,js=allDataList.size();j<js;j++, currColorIndex++) {
					ArrayList<Pair<Long, Double>> seriesDataList = allDataList.get(j);
					String measureName = art == 0 ? results.y1MeasureList.get(j).second : results.y2MeasureList.get(j).second;
					if (seriesDataList.size() == 0)
						continue;
					String label = measureName+"-"+vehName;
					if (firstDataSetPrinted)
						sb.append("\n,");
					else
						firstDataSetPrinted = true;
					sb.append("\"").append(label).append("\": {");
					sb.append("\"label\":\"").append(label).append("\"\n");
					sb.append(",\"lines\":{\"show\":true, \"fill\":false}\n")
					    .append(",\"points\":{\"show\":false}\n")
					    .append(",\"color\":").append(currColorIndex+results.colorIndexStart).append("\n")
					    .append(",\"yaxis\":").append(art+1).append("\n")
					    .append(",\"xaxis\":").append(1).append("\n")
					    .append(",\"hoverable\":true\n")
					    //other options come here
					    ;
					sb.append(",\"data\":[\n");
					for (int k=0,ks=seriesDataList.size();k<ks;k++) {
						if (k != 0)
							sb.append(",");
						if (k != 0 && k%10 == 0)
							sb.append("\n");
						Long xl = seriesDataList.get(k).first;
						double y = seriesDataList.get(k).second;
						if (results.xDataType == Cache.NUMBER_TYPE)
							sb.append("[").append((double)xl.longValue()/1000.0).append(",").append(y).append("]");
						else {
							if (results.xDataType == Cache.DATE_TYPE)
								xl = new Long(xl.longValue()+5500*3600);
							sb.append("[").append(xl.longValue()).append(",").append(y).append("]");
						}
					}
					sb.append("\n]\n"); //end of data array
					sb.append("}\n");//end of vehicle series info
				}//for each data list
			 }//art
			 //do bin/event
			 int seriesIndex = 0;
			 int binOnYaxis = data.binOnY1() ? 1 : 2;
			 String symbols[] = {"circle", "square", "diamond", "triangle"};
			 for (int art=0;art<2;art++) {
				ArrayList<ArrayList<Pair<Long, Long>>> allDataList = art == 0 ? data.binDataList : data.eventData; 
				for (int j=0,js=allDataList.size();j<js;j++,seriesIndex += 2, currColorIndex += 1) {
					ArrayList<Pair<Long, Long>> seriesDataList = allDataList.get(j);
					String measureName = art == 0 ? results.binMeasureList.get(j).second : results.eventList.get(j).second;
					if (seriesDataList.size() == 0)
						continue;
					int colorToUse = currColorIndex;//colorIndex.get(i);
					for (int up=0;up<2;up++) {
						String dataLabel = measureName+(up == 0 ? "-Beg-":"-End-")+vehName;
						String seriesLabel = measureName+"-"+vehName;
						if (firstDataSetPrinted)
							sb.append("\n,");
						else
							firstDataSetPrinted = true;
						sb.append("\"").append(dataLabel).append("\": {");
						if (up == 0)
						sb.append("\"label\":\"").append(seriesLabel).append("\"\n,");
						sb.append("\"lines\":{\"show\":false,\"fill\":true}\n")
					        .append(",\"points\":{\"radius\":4,\"show\":true,\"fillColor\":null").append(",\"symbol\":")
					        //.append("\"").append(symbols[(seriesIndex/2)%4]).append("\"")
					        .append("\"").append(up == 0 ? symbols[0] : "cross").append("\"")
					        .append("}\n")
					        .append(",\"color\":").append(colorToUse+results.colorIndexStart).append("\n")
					        .append(",\"yaxis\":").append(binOnYaxis).append("\n")
						    ;
						sb.append(",\"data\":[\n");
						boolean ptAdded = false;
						for (int k=0,ks=seriesDataList.size();k<ks;k++) {
							Long xl = up == 0 ? seriesDataList.get(k).first : seriesDataList.get(k).second;
							if (xl == null)
								continue;
								
							if (ptAdded)
								sb.append(",");
							else
								ptAdded = true;
							if (k !=0 && k%10 == 0)
								sb.append("\n");
							double y = data.getY1valForBin(xl);
							if (results.xDataType == Cache.NUMBER_TYPE)
								sb.append("[").append((double)xl.longValue()/1000.0).append(",").append(y).append("]");
							else {
								if (results.xDataType == Cache.DATE_TYPE)
									xl = new Long(xl.longValue()+5500*3600);
								sb.append("[").append(xl.longValue()).append(",").append(y).append("]");
							}
						}
						sb.append("\n]\n");//end of data array
						sb.append("}\n");//end of series info
					}
				}//for each data list
			 }//art
		 }//for each vehicle
		 sb.append("},\n");
		 sb.append("\"lastColor\":").append(currColorIndex+results.colorIndexStart).append(",\n");
		 //do location ...
		 sb.append("\"locations\": {\n");
		 firstDataSetPrinted = false;
		 for (int i=0,is=results.data.size();i<is;i++) {
			 VehicleSpecificResults data = results.data.get(i);
			 String vehName = results.vehicleList.get(i).second;
			 if (i != 0)
				 sb.append("\n,");
			 sb.append("\"").append(vehName).append("\":[");
			 ArrayList<Pair<Long, String>> loc = data.locationData;
			 for (int j=0,js=loc.size();j<js;j++) {
				 if (j != 0)
					 sb.append(",");
				 if (j!=0 && j%10 == 0)
					 sb.append("\n");
				 Long xl = loc.get(j).first;
				String str = loc.get(j).second;
				if (results.xDataType == Cache.NUMBER_TYPE)
					sb.append("[").append((double)xl.longValue()/1000.0).append(",\"").append(str).append("\"]");
				else
					sb.append("[").append(xl.longValue()).append(",\"").append(str).append("\"]");
			 }
			 sb.append("\n]");
		 }
		 sb.append("\n},\n");
		 //do array of vehicle found ... so that we can incrementally add remove data
		 sb.append("\"alreadyLoaded\": {");
		 firstDataSetPrinted = false;
		 for (int i=0,is=results.data.size();i<is;i++) {
			 VehicleSpecificResults data = results.data.get(i);
			 String vehName = results.vehicleList.get(i).second;
			 if (i != 0)
				 sb.append(",");
			 if (i != 0 && i%10 == 0)
				 sb.append("\n");
			 sb.append("\"").append(vehName).append("\":true");
		 }
	 	 sb.append("}\n");
	 	 sb.append("}\n");
	 	
	 	 if (!doJSON) {
	 		 sb.append("var datasets = alldata.datasets;\nvar locations = alldata.locations;\nvar alreadyLoaded=alldata.alreadyLoaded;\n");
	 		 sb.append("lastColor = ").append(currColorIndex+results.colorIndexStart).append(";\n");
	 		sb.append("var vehicleList = ["); 
	 		printMeasureListAsStrIntArr(sb, results.vehicleList, false); 
	 		sb.append("];\n");
	 		
	 		sb.append("var dataList = ["); 
	 		boolean printedFirst  = printMeasureListAsStrIntArr(sb, results.y1MeasureList, false);
	 		printMeasureListAsStrIntArr(sb, results.y2MeasureList, printedFirst);
	 		sb.append("];\n");
	 		
	 		sb.append("var eventList = ["); 
	 		printedFirst  = printMeasureListAsStrIntArr(sb, results.binMeasureList, false);
	 		printMeasureListAsStrIntArr(sb, results.eventList, printedFirst);
	 		sb.append("];\n");
	 		
	 		sb.append("var fetchURL='graph_jscript.jsp?json=1&last_color='+lastColor+'&_ts=").append(System.currentTimeMillis()).append("&page_context=").append(pageContext);
	 		//sb.append("&y1=");
	 		//printMeasureListAsIdCSV(sb, results.y1MeasureList, false);
	 		//sb.append("&y2=");
	 		//printMeasureListAsIdCSV(sb, results.y2MeasureList, false);
	 		//sb.append("&b=");
	 		//printMeasureListAsIdCSV(sb, results.binMeasureList, false);
	 		//sb.append("&e=");
	 		//printMeasureListAsIdCSV(sb, results.eventList, false);
	 		//sb.append("&x=").append(results.xaxis);
	 		//SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
	 		//sb.append("&st=").append(sdf.format(results.start));
	 		//sb.append("&en=").append(sdf.format(results.end));
	 		//sb.append("&avg=").append(results.averagingWindow);
	 		//sb.append("&thresh=").append(results.eventThreshold);
	 		//sb.append("&filter=").append(results.filterDistLessThan);
	 		sb.append("';\n");
	 	 }
		
		 return sb;
	 }
	 
	 private static boolean printMeasureListAsIdCSV(StringBuilder sb, ArrayList<Pair<Integer, String>> theList, boolean printedFirst) {
		 for (int i=0,is = theList.size();i<is;i++) {
			 Pair<Integer, String> entry = theList.get(i);
			 if (printedFirst)
				 sb.append(",");
			 else
				 printedFirst = true;
			 sb.append(entry.first);
		 }
		 return printedFirst;		 
	 }
	 private static boolean printMeasureListAsStrIntArr(StringBuilder sb, ArrayList<Pair<Integer, String>> theList, boolean printedFirst) {
		 for (int i=0,is = theList.size();i<is;i++) {
			 Pair<Integer, String> entry = theList.get(i);
			 if (printedFirst)
				 sb.append(",");
			 else
				 printedFirst = true;
			 sb.append("[\"").append(entry.second).append("\",").append(entry.first).append("]");
		 }
		 return printedFirst;
	 }
	
	 private static void helpSetAvgEtc(double val, double speed, ArrayList<Double>valWindow, ArrayList<Double>speedWindow, boolean needsValAvg, boolean needsSpeedAvg, boolean needsValMedian, boolean needsSpeedMedian, AvgMedianHolder avgInfo, int avgWindow, Date prevTime, double prevVal, double prevSpeed, Date currTime) {
		 double oldVal = 0;
		 double oldSpeed = 0;
		 if (needsValAvg || needsValMedian) {
			 if (valWindow.size() == 0) {
				 for (int i=0;i<avgWindow;i++) {
					 valWindow.add(val);
				 }
				 avgInfo.avgVal = val;
				 oldVal = val;
			 }
			 else {
				 oldVal = valWindow.get(0);
				 for (int i=1,is=valWindow.size();i<is;i++) {
					 valWindow.set(i-1, valWindow.get(i));
				 }
				 valWindow.set(valWindow.size()-1, val);
			 }
		 }
		 if (needsSpeedAvg || needsSpeedMedian) {
			 if (speedWindow.size() == 0) {
				 for (int i=0;i<avgWindow;i++) {
					 speedWindow.add(speed);
				 }
				 avgInfo.avgSpeed = speed;
				 oldSpeed = speed;
			 }
			 else {
				 oldSpeed = speedWindow.get(0);
				 for (int i=1,is=speedWindow.size();i<is;i++) {
					 speedWindow.set(i-1, speedWindow.get(i));
				 }
				 speedWindow.set(speedWindow.size()-1, speed);
			 }
		 }
		 if (needsValAvg) {
			 avgInfo.avgVal = (avgInfo.avgVal*valWindow.size() - oldVal + val)/(double)valWindow.size();
		 }
		 if (needsSpeedAvg) {
			 avgInfo.avgSpeed =  (avgInfo.avgSpeed*speedWindow.size() - oldSpeed + speed)/(double)speedWindow.size();
		 }
		 if (needsValMedian) {
			 avgInfo.medianVal = helpGetMedian(valWindow);
		 }
		 if (needsSpeedMedian) {
			 avgInfo.medianSpeed = helpGetMedian(speedWindow);
		 }
		 avgInfo.cummSpeed += speed;
		 avgInfo.cummVal += val;
		 double min = prevTime == null || currTime == null ? 0 : (double)( currTime.getTime() - prevTime.getTime())/(1000.0*60.0);
		// double avgVal = Misc.isUndef(prevVal) ? val : (val+prevVal)/2.0;
		// double avgSpeed = Misc.isUndef(prevSpeed) ? speed : (speed+prevSpeed)/2.0;
		 double avgVal = Misc.isUndef(prevVal) ? 0 : val < 0.5 ? 0 : 1;
		 double avgSpeed = Misc.isUndef(prevSpeed) ? 0 : speed < 0.5 ? 0 : 1;
			
		 avgInfo.cummSpeedMinutes += avgSpeed*min;
		 avgInfo.cummValMinutes += avgVal*min;
	 }
	 
	 private static double helpGetMedian(ArrayList<Double> dataWindow) {
		 ArrayList<Double> copy = (ArrayList<Double>) dataWindow.clone();
		 Collections.sort(copy);
		 int n = dataWindow.size();
		 if (n % 2 == 0) {
			 double v1 = copy.get(n/2);
			 double v2 = copy.get(n/2-1);
			 return (v1+v2)/2.0;
		 }
		 else 
			 return copy.get(n/2);
	 }
	 
	 private double helpDoMedian(double newVal, double currAvg, ArrayList<Double> window) {
		 double retval = 0;
		 double remVal = window.get(0);
		 for (int i=1,is=window.size();i<is;i++) {
			 window.set(i-1, window.get(i));
		 }
		 retval = (currAvg * window.size() - remVal + newVal)/(double) window.size();
		 window.set(window.size()-1, newVal);
		 return retval;
	 }
	 private static void createEmpty(TotalResults retval, int nvehicles) {
		 for (int i=0;i<nvehicles;i++) {
			 VehicleSpecificResults entry = new VehicleSpecificResults();
			 for (int art=0;art<2;art++) {
				 int sz = art == 0 ? retval.y1MeasureList.size() : retval.y2MeasureList.size();
				 ArrayList<ArrayList<Pair<Long, Double>>> dataVal = art == 0 ? entry.y1DataList : entry.y2DataList;
				 for (int j=0;j<sz;j++)
					 dataVal.add(new ArrayList<Pair<Long, Double>>());
			 }
			 for (int art=0;art<2;art++) {
				 int sz = art == 0 ? retval.binMeasureList.size() : retval.eventList.size();
				 ArrayList<ArrayList<Pair<Long, Long>>> dataVal = art == 0 ? entry.binDataList : entry.eventData;
				 for (int j=0;j<sz;j++)
					 dataVal.add(new ArrayList<Pair<Long, Long>>());
			 }
			 retval.data.add(entry);
		 }
	 }

	 private static Date readDate(String dateString, SimpleDateFormat sdfTime, SimpleDateFormat sdf) {
    	 Date dt = null;
		 try {
			dt = sdfTime.parse(dateString);
		 }
		 catch (ParseException e2) {
			try {
				dt = sdf.parse(dateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
			}
		 }
		 return dt;
     }
     private static void readParamList(String topContext, int dimId, SessionManager session, ArrayList<Pair<Integer, String>> retval)  {
    	 ArrayList<Integer> tempList = new ArrayList<Integer>();
    	 tempList.clear();
    	 Misc.convertValToVector(session.getParameter(topContext+dimId), tempList);
    	 for (Integer i:tempList) {
    		 DimInfo d = DimInfo.getDimInfo(i);
    		 if (d != null)
    			 retval.add(new Pair<Integer, String>(d.m_id, d.m_name));
    	 }
    	 return;   	 
     }
     
    
}

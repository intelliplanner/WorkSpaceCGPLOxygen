package com.ipssi.analysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;

public class DrivingAnalysis {
	public static class Parameters{		
		public int contDrive = 4*60;
		public int considerAsContDriveIfBreakLessThan = 15;
		public int minBreakAfterContDriving = 30;
		public int mealBreaks = 60;
		public int countMealBreaks = 3;
		public int extendedBreakDuration = 4*60;
		public int minTotalBreakDuration = 10*60;
		public int typicalNoEntryOpen = 21;
		public int typicalNoEntryEnd = 6;
		public double mergeStopIfImpliedSpeedLessThan = 4;
		public int loadunloadDelay = 24*60;
		public int getContDrive() {
			return contDrive;
		}
		public void setContDrive(int contDrive) {
			this.contDrive = contDrive;
		}
		public int getConsiderAsContDriveIfBreakLessThan() {
			return considerAsContDriveIfBreakLessThan;
		}
		public void setConsiderAsContDriveIfBreakLessThan(
				int considerAsContDriveIfBreakLessThan) {
			this.considerAsContDriveIfBreakLessThan = considerAsContDriveIfBreakLessThan;
		}
		public int getMinBreakAfterContDriving() {
			return minBreakAfterContDriving;
		}
		public void setMinBreakAfterContDriving(int minBreakAfterContDriving) {
			this.minBreakAfterContDriving = minBreakAfterContDriving;
		}
		public int getMealBreaks() {
			return mealBreaks;
		}
		public void setMealBreaks(int mealBreaks) {
			this.mealBreaks = mealBreaks;
		}
		public int getCountMealBreaks() {
			return countMealBreaks;
		}
		public void setCountMealBreaks(int countMealBreaks) {
			this.countMealBreaks = countMealBreaks;
		}
		public int getExtendedBreakDuration() {
			return extendedBreakDuration;
		}
		public void setExtendedBreakDuration(int extendedBreakDuration) {
			this.extendedBreakDuration = extendedBreakDuration;
		}
		public int getMinTotalBreakDuration() {
			return minTotalBreakDuration;
		}
		public void setMinTotalBreakDuration(int minTotalBreakDuration) {
			this.minTotalBreakDuration = minTotalBreakDuration;
		}
		public int getTypicalNoEntryOpen() {
			return typicalNoEntryOpen;
		}
		public void setTypicalNoEntryOpen(int typicalNoEntryOpen) {
			this.typicalNoEntryOpen = typicalNoEntryOpen;
		}
		public int getTypicalNoEntryEnd() {
			return typicalNoEntryEnd;
		}
		public void setTypicalNoEntryEnd(int typicalNoEntryEnd) {
			this.typicalNoEntryEnd = typicalNoEntryEnd;
		}
		public double getMergeStopIfImpliedSpeedLessThan() {
			return mergeStopIfImpliedSpeedLessThan;
		}
		public void setMergeStopIfImpliedSpeedLessThan(
				double mergeStopIfImpliedSpeedLessThan) {
			this.mergeStopIfImpliedSpeedLessThan = mergeStopIfImpliedSpeedLessThan;
		}
	}
	
	public static class StoppageInfo implements Comparable{
		public int id;
		public ArrayList<Integer> idsOfMerge = null;
		public Date start;
		public Date end;
		public double startOdo;
		public double endOdo;
		public String startLoc;
		public String endLoc;
		public double startLon;
		public double startLat;
		public double endLon;
		public double endLat;
		public int assessedCauseFactor;
		public double dur() {
			return DrivingAnalysis.dur(start, end);
		}
		public int compareTo(Object p) {
			StoppageInfo rhs = (StoppageInfo) p;
			return this.start.compareTo(rhs.start);
		}
		public StoppageInfo(int id, Date start, Date end, double startOdo,
				double endOdo, String startLoc, String endLoc, double startLon,
				double startLat, double endLon, double endLat) {
			super();
			this.id = id;
			this.start = start;
			this.end = end;
			this.startOdo = startOdo;
			this.endOdo = endOdo;
			this.startLoc = startLoc;
			this.endLoc = endLoc;
			this.startLon = startLon;
			this.startLat = startLat;
			this.endLon = endLon;
			this.endLat = endLat;
		}
		public StoppageInfo(Date start) {
			this.start = start;
		}
		
	}
	public static class TripInfo implements Comparable {
		
		public int id;
		public Date in;
		public Date out;
		public int type;
		public double inOdo;
		public double outOdo;
		public double inLon;
		public double inLat;
		public double outLon;
		public double outLat;
		public double dur() {
			return DrivingAnalysis.dur(in, out);
		}
		public TripInfo(int id, Date in, Date out, int type, double inOdo,
				double outOdo, double inLon, double inLat, double outLon,
				double outLat) {
			super();
			this.id = id;
			this.in = in;
			this.out = out;
			this.type = type;
			this.inOdo = inOdo;
			this.outOdo = outOdo;
			this.inLon = inLon;
			this.inLat = inLat;
			this.outLon = outLon;
			this.outLat = outLat;
		}
		public TripInfo(Date start) {
			this.in = start;
		}

		public int compareTo(Object p) {
			TripInfo rhs = (TripInfo) p;
			return this.in.compareTo(rhs.in);
		}
	}

	public static class DriveAnalysisException {
		public static int G_EXCESSIVE_LOAD_UNLOAD_DELAY = 477;
		public static int G_NO_EXTENDED_REST = 476;
		public static int G_NOT_ENOUGH_MEAL_BREAK = 475;
		public static int G_INSUFFICIENT_BREAK_AFTER_CONT_DRIVING= 474;
		public static int G_CONTINOUS_DRIVING = 473;
		
		public static int G_UNKNOWN = 0;
		public static int G_MANDBREAK_AFTER_CONT = 1;
		public static int G_MEAL_BREAK = 2;
		public static int G_REST_DUR_BREAK = 3;
		public static int G_NO_ENTRY = 4;
		public static int G_LOAD_UNLOAD_DELAY = 5;
		public static int G_UNEXPLAINED_BREAK = 6;
		public static int G_SHORT_BREAK = 7;
		public static int G_LOAD_UNLOAD_EXCESSIVE_DELAY = 8;
		public int type;
		public Date start;
		public Date end;
		public String startLoc;
		public String endLoc;
		public double startLon;
		public double startLat;
		public double endLon;
		public double endLat;
		
		public DriveAnalysisException(int type, Date start, Date end,
				String startLoc, String endLoc, double startLon,
				double startLat, double endLon, double endLat) {
			super();
			this.type = type;
			this.start = start;
			this.end = end;
			this.startLoc = startLoc;
			this.endLoc = endLoc;
			this.startLon = startLon;
			this.startLat = startLat;
			this.endLon = endLon;
			this.endLat = endLat;
		}
		
	}
	public static void runAnalysis(Connection conn, int vehicleId, Date startPer, Date endPer,Parameters parameters) throws Exception {
		if (startPer == null)
			startPer = new Date(113,6,1);
		if (endPer == null)
			endPer = new Date(113,6,20);
		Date atDate = new Date(startPer.getTime());
		FastList<StoppageInfo> stopList = loadStoppage(conn, vehicleId, startPer, endPer, parameters);
		FastList<TripInfo> tripList = loadTrip(conn, vehicleId, startPer, endPer, parameters);
		PreparedStatement ps = conn.prepareStatement("update engine_events set assessed_factor=? where id=?");
		if (parameters == null)
			parameters = new Parameters();
		while (atDate.before(endPer)) {
			ArrayList<DriveAnalysisException> exceptionsList = new ArrayList<DriveAnalysisException> ();
			doAnalysis(conn, atDate, stopList, tripList,parameters, exceptionsList, ps);
			ps.executeBatch();
			DrivingAnalysis.save(exceptionsList, vehicleId, conn);
			if (!conn.getAutoCommit())
				conn.commit();
		}
		ps.close();
				
	}
	public static double dur(Date start, Date end) {
		return (double) ((end == null ? System.currentTimeMillis()+10*60000 : end.getTime()) - start.getTime())/60000.0;
	}
	public static Pair<Boolean, Boolean> isNearLoadUnload(FastList<TripInfo> tripList, StoppageInfo stoppage) {
		TripInfo ch = new TripInfo(stoppage.start);
		int idx = tripList.indexOf(ch).first;
		TripInfo prior = tripList.get(idx);
		TripInfo next = tripList.get(idx+1);
		Point refPt = new Point(stoppage.startLon, stoppage.startLat);
		double distPrior = prior == null ? Misc.LARGE_NUMBER : refPt.distance(new Point (prior.inLon, prior.inLat));
		if (distPrior > 10)
			prior = null;
		double distNext = next == null ? Misc.LARGE_NUMBER : refPt.distance(new Point (next.inLon, next.inLat));
		if (distNext > 10)
			next = null;
		boolean type = prior == null && next == null ? false : prior != null && next == null ? prior.type ==  0 : next != null && prior == null ? next.type == 0 : distNext < distPrior ? next.type == 0 : prior.type == 0;
		return new Pair<Boolean, Boolean> (type, prior != null || next != null);
	}
	public static void doAnalysis(Connection conn, Date atDate, FastList<StoppageInfo> stopList, FastList<TripInfo> tripList,Parameters parameters, ArrayList<DriveAnalysisException> exceptionsList, PreparedStatement ps) throws Exception {
		Date begDate = new Date(atDate.getTime());
		Misc.addDays(begDate, -1);
		
		StoppageInfo stCmp = new StoppageInfo(atDate);
		int stopIndex = stopList.indexOf(stCmp).first;
		TripInfo trCmp = new TripInfo(atDate);
		
		int tripIndex= tripList.indexOf(trCmp).first;
		stCmp = new StoppageInfo(begDate);
		trCmp = new TripInfo(begDate);
		int startStopIndex = stopList.indexOf(stCmp).first;
		int startTripIndex = tripList.indexOf(trCmp).first;
		
		//public int contDrive = 4*60;
		//public int considerAsContDriveIfBreakLessThan = 15;
		//public int minBreakAfterContDriving = 30;
		//public int mealBreaks = 60;
		//public int countMealBreaks = 3;
		//public int extendedBreakDuration = 4*60;
		//public int minTotalBreakDuration = 10*60;
		//public int typicalNoEntryOpen = 21;
		//public int typicalNoEntryEnd = 6;
		//public double mergeStopIfImpliedSpeedLessThan = 4;
		
		double contDriveMin = 0;
		int countMealBreaks = 0;
		double extendedBreakDuration = 0;
		boolean seenContDriveException = false;
		double loadDelay = 0;
		double unloadDelay = 0;
		boolean seenLUdelay = false;
		boolean prevSeenLUdelay = false;
		StoppageInfo atStop = null;
		for (int i=startStopIndex;i<=stopIndex;i++) {
			StoppageInfo curr = stopList.get(i);
			if (curr == null)
				continue;
			atStop = curr;
			StoppageInfo prev = stopList.get(i-1);
			StoppageInfo next = stopList.get(i+1);
			contDriveMin += prev == null ? 0 : dur(prev.end, curr.start);
			boolean prevSeenContDriveException = seenContDriveException;
			double dur = curr.dur();
			
			if (contDriveMin > parameters.contDrive) {
				//continous driving exception
				if (!prevSeenContDriveException)
				exceptionsList.add(new DriveAnalysisException(DriveAnalysisException.G_CONTINOUS_DRIVING, curr.start, curr.end,
					curr.startLoc, curr.startLoc, curr.startLon,
					curr.startLat, curr.startLon, curr.startLat));
				seenContDriveException = true;
			}
			
			if (seenContDriveException && dur < parameters.minBreakAfterContDriving && dur > parameters.considerAsContDriveIfBreakLessThan) {
				//not enough break after continous driving
				exceptionsList.add(new DriveAnalysisException(DriveAnalysisException.G_INSUFFICIENT_BREAK_AFTER_CONT_DRIVING, curr.start, curr.end,
						curr.startLoc, curr.startLoc, curr.startLon,
						curr.startLat, curr.startLon, curr.startLat));
				
			}
			if (seenContDriveException &&  dur >= parameters.considerAsContDriveIfBreakLessThan && dur <= parameters.mealBreaks) {
				//not enough break after continous driving
				curr.assessedCauseFactor = DriveAnalysisException.G_MANDBREAK_AFTER_CONT;
			}
			if (dur < parameters.considerAsContDriveIfBreakLessThan) {
				contDriveMin += dur;
			}
			else {
				contDriveMin = 0;
				seenContDriveException = false;
			}
			Pair<Boolean, Boolean> lunear = isNearLoadUnload(tripList, curr);
			if (lunear.second) {
				curr.assessedCauseFactor = DriveAnalysisException.G_LOAD_UNLOAD_DELAY;
				if (lunear.first) {
					loadDelay += dur;
					unloadDelay = 0;
				}
				else {
					unloadDelay += dur;
					loadDelay = 0;
				}
			}
			else {
				loadDelay = 0;
				unloadDelay = 0;				
			}
			prevSeenLUdelay = seenLUdelay;
			if (loadDelay >= parameters.loadunloadDelay || unloadDelay >= parameters.loadunloadDelay) {
				seenLUdelay = true;
				if (!prevSeenLUdelay && seenLUdelay) {
					exceptionsList.add(new DriveAnalysisException(DriveAnalysisException.G_EXCESSIVE_LOAD_UNLOAD_DELAY, curr.start, curr.end,
							curr.startLoc, curr.startLoc, curr.startLon,
							curr.startLat, curr.startLon, curr.startLat));
				}
				
			}
			else {
				seenLUdelay = false;
			}
			if (dur >= parameters.extendedBreakDuration) {
				extendedBreakDuration+= dur;
				if (curr.assessedCauseFactor <= 0 && extendedBreakDuration < parameters.minTotalBreakDuration)
					curr.assessedCauseFactor = DriveAnalysisException.G_REST_DUR_BREAK;
			}
			if (dur >= parameters.mealBreaks) {
				countMealBreaks++;
				if (curr.assessedCauseFactor <= 0 && countMealBreaks <= parameters.countMealBreaks  && dur < parameters.extendedBreakDuration)
					curr.assessedCauseFactor = DriveAnalysisException.G_MEAL_BREAK;
			}
			if (curr.assessedCauseFactor <= 0 && dur < parameters.considerAsContDriveIfBreakLessThan)
				curr.assessedCauseFactor = DriveAnalysisException.G_SHORT_BREAK;
			if (curr.assessedCauseFactor <= 0) {
				Date dt = curr.end == null ? curr.start : curr.end;
				double hr = dt.getHours();
				if (curr.assessedCauseFactor <= 0 && hr >= parameters.typicalNoEntryOpen-1.5 && hr <= parameters.typicalNoEntryEnd+1.5)
					curr.assessedCauseFactor = DriveAnalysisException.G_NO_ENTRY;
				dt = curr.start;
				hr = dt.getHours();
				if (curr.assessedCauseFactor <= 0 && hr >= parameters.typicalNoEntryEnd - 0.5 && hr <= parameters.typicalNoEntryEnd+0.5)
					curr.assessedCauseFactor = DriveAnalysisException.G_NO_ENTRY;
			}
			if (curr.assessedCauseFactor > 0) {
				ps.setInt(1, curr.assessedCauseFactor);
				ps.setInt(2, curr.id);
				ps.addBatch();
				for (int t1=0, t1s = curr.idsOfMerge == null ? 0 : curr.idsOfMerge.size();t1<t1s;t1++) {
					ps.setInt(1, curr.assessedCauseFactor);
					ps.setInt(2, curr.idsOfMerge.get(t1));
					ps.addBatch();
				}
			}
				
		}
		if (atStop != null) {
		if (countMealBreaks < parameters.mealBreaks)
			exceptionsList.add(new DriveAnalysisException(DriveAnalysisException.G_NOT_ENOUGH_MEAL_BREAK, atStop.start, atStop.end,
				atStop.startLoc, atStop.startLoc, atStop.startLon,
				atStop.startLat, atStop.startLon, atStop.startLat));
		if (extendedBreakDuration < parameters.minTotalBreakDuration)
			exceptionsList.add(new DriveAnalysisException(DriveAnalysisException.G_NO_EXTENDED_REST, atStop.start, atStop.end,
					atStop.startLoc, atStop.startLoc, atStop.startLon,
					atStop.startLat, atStop.startLon, atStop.startLat));
		}
		Misc.addDays(atDate, 1.0/24.0);
	}
	
	public static void save(ArrayList<DriveAnalysisException> exceptionList, int vehicleId, Connection conn) throws Exception {
		PreparedStatement ps =conn.prepareStatement("insert into engine_events(vehicle_id, rule_id, event_start_time, event_stop_time, event_begin_longitude, event_begin_latitude, event_begin_name, event_end_longitude, event_end_latitude, event_end_name)" +
				" value (?,?,?,?,?,?,?,?,?,?) ");
		for (int i=0,is=exceptionList.size();i<is;i++) {
			DriveAnalysisException e5 = exceptionList.get(i);
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setInt(colIndex++, e5.type);
			
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(e5.start));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(e5.end));
			Misc.setParamDouble(ps, e5.startLon, colIndex++);
			Misc.setParamDouble(ps, e5.startLat, colIndex++);
			ps.setString(colIndex++, e5.startLoc);
			Misc.setParamDouble(ps, e5.endLon, colIndex++);
			Misc.setParamDouble(ps, e5.endLat, colIndex++);
			ps.setString(colIndex++, e5.endLoc);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	public static FastList<StoppageInfo> loadStoppage(Connection conn, int vehicleId, Date startPer, Date endPer,Parameters parameters) throws Exception {
		try {
			FastList<StoppageInfo> retval = new FastList<StoppageInfo>();
			final String q = " select engine_events.id, event_start_time, event_stop_time, event_begin_longitude, event_begin_latitude, event_begin_name, event_end_longitude, event_end_latitude, event_end_name, "+
			" lgd_st.attribute_value, lgd_en.attribute_value "+
			" from engine_events join logged_data lgd_st on (engine_events.vehicle_id = lgd_st.vehicle_id and lgd_st.attribute_id = 0 and engine_events.event_start_time = lgd_st.gps_record_time) "+
			" join logged_data lgd_en on (engine_events.vehicle_id = lgd_en.vehicle_id and lgd_en.attribute_id = 0 and engine_events.event_stop_time = lgd_en.gps_record_time) "+
			" where engine_events.vehicle_id = ? and engine_events.rule_id=1 "+
			" and engine_events.event_stop_time >= ? and engine_events.event_start_time <= ? "+
			" order by event_start_time, event_stop_time "
			;
			PreparedStatement ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.utilToSqlDate(startPer));
			ps.setTimestamp(3, Misc.utilToSqlDate(endPer));
			ResultSet rs = ps.executeQuery();
			StoppageInfo prevStoppage = null;
			while (rs.next()) {
				int colIndex = 1;
				int id = rs.getInt(colIndex++);
				Date start = Misc.sqlToUtilDate(rs.getTimestamp(colIndex++));
				Date end = Misc.sqlToUtilDate(rs.getTimestamp(colIndex++));
				double startLon =  Misc.getRsetDouble(rs, colIndex++);
				double startLat =  Misc.getRsetDouble(rs, colIndex++);
				String startLoc = rs.getString(colIndex++);
				
				double endLon =  Misc.getRsetDouble(rs, colIndex++);
				double endLat =  Misc.getRsetDouble(rs, colIndex++);
				String endLoc = rs.getString(colIndex++);
				
				double startOdo = Misc.getRsetDouble(rs, colIndex++);
				double endOdo = Misc.getRsetDouble(rs, colIndex++);
				boolean toMerge = false;
				if (prevStoppage != null) {
					double gapMin = ((double) (start.getTime() - prevStoppage.end.getTime()))/(1000.0*60.0);
					double gapDistKM = startOdo - prevStoppage.startOdo;
					toMerge = gapMin < 0.5;
					if (!toMerge) {
						double speed = gapDistKM/gapMin*60;
						if (speed < parameters.mergeStopIfImpliedSpeedLessThan)
							toMerge = true;
					}
					if (gapDistKM > 1 || gapMin > 15)
						toMerge = false;
				}
				if (toMerge) {
					prevStoppage.end = end;
					prevStoppage.endLon = endLon;
					prevStoppage.endLat = endLat;
					prevStoppage.endLoc = endLoc;
					prevStoppage.endOdo = endOdo;
					if (prevStoppage.idsOfMerge == null)
						prevStoppage.idsOfMerge = new ArrayList<Integer>();
					prevStoppage.idsOfMerge.add(id);
				}
				else {
					StoppageInfo stoppageInfo = new StoppageInfo(id, start, end, startOdo,
							endOdo, startLoc, endLoc, startLon,
							startLat, endLon, endLat);
					retval.add(stoppageInfo);
				}
			}
			rs.close();
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static FastList<TripInfo> loadTrip(Connection conn, int vehicleId, Date startPer, Date endPer,Parameters parameters) throws Exception {
		try {
			FastList<TripInfo> retval = new FastList<TripInfo>();
			final String q = 
				" select trip_info.id,load_gate_in, gi.longitude,gi.latitude,gi.attribute_value, load_gate_out, oi.longitude,oi.latitude,oi.attribute_value, "+
				" unload_gate_in, ui.longitude,ui.latitude,ui.attribute_value, unload_gate_out, vi.longitude,vi.latitude,vi.attribute_value "+
				" from trip_info left outer join logged_data gi on (gi.vehicle_id =trip_info.vehicle_id and gi.attribute_value=0 and gi.gps_record_time = load_gate_in) "+
				" left outer join logged_data oi on (oi.vehicle_id =trip_info.vehicle_id and oi.attribute_value=0 and oi.gps_record_time = load_gate_out) "+
				" left outer join logged_data ui on (ui.vehicle_id =trip_info.vehicle_id and ui.attribute_value=0 and ui.gps_record_time = unload_gate_in) "+
				" left outer join logged_data vi on (vi.vehicle_id =trip_info.vehicle_id and vi.attribute_value=0 and vi.gps_record_time = unload_gate_out) "+
				" where trip_info.vehicle_id = ? "+
				" and combo_end >= ? and combo_start <= ? "+
				" order by combo_start "
				;

			PreparedStatement ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.utilToSqlDate(startPer));
			ps.setTimestamp(3, Misc.utilToSqlDate(endPer));
			ResultSet rs = ps.executeQuery();
			StoppageInfo prevStoppage = null;
			while (rs.next()) {
				int colIndex = 1;
				int id = rs.getInt(colIndex++);
				Date in= Misc.sqlToUtilDate(rs.getTimestamp(colIndex++));
				double inLon = Misc.getRsetDouble(rs, colIndex++);
				double inLat = Misc.getRsetDouble(rs, colIndex++);
				double inOdo = Misc.getRsetDouble(rs, colIndex++);
				Date out= Misc.sqlToUtilDate(rs.getTimestamp(colIndex++));
				double outLon = Misc.getRsetDouble(rs, colIndex++);
				double outLat = Misc.getRsetDouble(rs, colIndex++);
				double outOdo = Misc.getRsetDouble(rs, colIndex++);
				if (in != null) {
					TripInfo item = new TripInfo(id, in, out, 0, inOdo,
						 outOdo, inLon, inLat, outLon,
						 outLat);
					retval.add(item);
				}
				in= Misc.sqlToUtilDate(rs.getTimestamp(colIndex++));
				inLon = Misc.getRsetDouble(rs, colIndex++);
				inLat = Misc.getRsetDouble(rs, colIndex++);
				 inOdo = Misc.getRsetDouble(rs, colIndex++);
				 out= Misc.sqlToUtilDate(rs.getTimestamp(colIndex++));
				 outLon = Misc.getRsetDouble(rs, colIndex++);
				 outLat = Misc.getRsetDouble(rs, colIndex++);
				 outOdo = Misc.getRsetDouble(rs, colIndex++);
				if (in != null) {
					TripInfo item = new TripInfo(id, in, out, 1, inOdo,
						 outOdo, inLon, inLat, outLon,
						 outLat);
					retval.add(item);
				}
			}
			rs.close();
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static void main(String[] args) {
		Connection conn = null;
		try {
			int vehicleId = 1;
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			//DrivingAnalysis.runAnalysis(conn, 19563, null, null, null);
			DrivingAnalysis.runAnalysis(conn, 19564, null, null, null);
			if (!conn.getAutoCommit())
				conn.commit();
			DrivingAnalysis.runAnalysis(conn, 19565, null, null, null);
			if (!conn.getAutoCommit())
				conn.commit();
			DrivingAnalysis.runAnalysis(conn, 19566, null, null, null);
			if (!conn.getAutoCommit())
				conn.commit();
			DrivingAnalysis.runAnalysis(conn, 19664, null, null, null);
			if (!conn.getAutoCommit())
				conn.commit();
			
			if (!conn.getAutoCommit())
				conn.commit();
//			DrivingAnalysis.runAnalysis(conn, 19739, null, null, null);
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			if (conn != null) {
				try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
				}
				catch (Exception e5) {
					
				}
			}
		}
	}
		
}

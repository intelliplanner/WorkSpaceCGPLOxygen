package com.ipssi.common.ds.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.geometry.Point;
//usage: getGpsPlusViolations to get violations object and call relevant methods
public class GpsPlusViolations {
	public static int CHANGE_CODE_FOR_MARK_FORQC = 100;
	public static int CHANGE_CODE_FOR_MARK_FOR_REPAIR = 101;
	public static double g_defaultTripDurHr = 6;//TODO make it DB configurable
	public static long g_defaultAllowedTrackDelay = 20*60*1000;//TODO make it DB configurable
	public static double g_distPlusMinusPerc = 0.1;//TODO make it DB configurable
	public static double g_stdLoadUnloadHr = 1;//TODO make it DB configurable
	public static double g_workHours = 16;//TODO make it DB configurable
	public static int G_TARPOLINE_Q = 2;
	public static int G_SEAL_Q = 1;
	//Yes 1, 2 No , NC 3
	private int vehicleId = Misc.getUndefInt();
	private long currTime = Misc.getUndefInt();
	private int tprId = Misc.getUndefInt();
	private CriticalRuleInfo criticalRuleInfo = null;
	private boolean criticalRuleInfoCalculated = false;
	private int srcMinesId = Misc.getUndefInt();
	private long srcTime = Misc.getUndefInt();
	private boolean srcCalculated = false;
	private double tatSpecDist = Misc.getUndefDouble();
	private double tatSpecTime = Misc.getUndefDouble();
	private boolean tatSpecCalculated = false;
	private Pair<Integer, String> manualQC = null;
	private Pair<Integer, String> manualGpsRepair = null;
	private ArrayList<String> calcDetailedGPSViolations = null;
	private Pair<Boolean, String> calcSafetyViolations = null;
	private Pair<Boolean, String> calcSummaryGpsViolations = null;
	private Triple<Long, Pair<Double, Double>, String> calcLastTracked = null;
	private boolean distTravelledCalc = false;
	private double distTravelled = Misc.getUndefDouble();
	private double partyGrossWt = Misc.getUndefDouble();
	private boolean partyGrossWtCalculated = false;
	private ArrayList<Pair<Integer, Integer>> qAnswered = null;
	private int portNodeId = Misc.getUndefInt();
	private Pair<Pair<Long, String>, Pair<Long, String>> nonTrackWindow = null;
	
	public static GpsPlusViolations getGpsPlusViolatins(Connection conn, int vehicleId, int tprId, long currTime, int srcMineId, long challanDateTime) throws Exception {
		long currentTimeMinus1Days = System.currentTimeMillis() - 24*60*60*1000;
		challanDateTime = currentTimeMinus1Days < challanDateTime ? challanDateTime : currentTimeMinus1Days;
		if (currTime <= 0)
			currTime = System.currentTimeMillis();
		GpsPlusViolations retval = new GpsPlusViolations(vehicleId, tprId, currTime);
		if (Misc.isUndef(vehicleId))
			return retval;
		try {
			retval.getCriticalRuleInfo(conn, vehicleId);
			retval.setUpSrcInfo(conn, vehicleId, tprId, srcMineId, challanDateTime);
			retval.setTATSpecInfo(conn, retval.srcMinesId);
			if (Misc.isUndef(retval.srcTime) && !Misc.isUndef(retval.srcMinesId) ) {
				retval.srcTime = currTime-(long)((!Misc.isUndef(retval.tatSpecTime) ? retval.tatSpecTime : GpsPlusViolations.g_defaultTripDurHr)*1000*3600);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//throw e;//TBD
		}
		return retval;
	}
	public void updateManualQCFlag(Connection conn, int tprId) {
		try {
			if (this.manualQC == null) {
				Pair<Pair<Integer, String>, Pair<Integer, String>> temp = GpsPlusViolations.getManualMarkForQCAndGpsRepair(conn, vehicleId);
				this.manualQC = temp.first;
				this.manualGpsRepair = temp.second;
			}
			if (this.manualQC.first > 0) {
				this.setMarkForQCOrGpsRepair(conn, vehicleId, tprId, true, false, null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	public void updateManualRepairFlag(Connection conn, int tprId)  {
		try {
			if (this.manualGpsRepair == null) {
				Pair<Pair<Integer, String>, Pair<Integer, String>> temp = GpsPlusViolations.getManualMarkForQCAndGpsRepair(conn, vehicleId);
				this.manualGpsRepair = temp.second;
				this.manualQC = temp.first;
			}
			if (this.manualGpsRepair.first > 0) {
				this.setMarkForQCOrGpsRepair(conn, vehicleId, tprId, false, false, null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public long getGateInTime(Connection conn) {
		long retval = currTime;
		PreparedStatement ps = null;
		
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select latest_unload_gate_in_out from tp_record where tpr_id=?");
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = Misc.sqlToLong(rs.getTimestamp(1));
//				currTime = Misc.sqlToLong(rs.getTimestamp(1));
			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	public Pair<ResultEnum, String> getMarkForQC(Connection conn, double grossUnloadWt, double grossPartyWt) {
		try {
			if (this.manualQC == null) {
				Pair<Pair<Integer, String>, Pair<Integer, String>> temp = GpsPlusViolations.getManualMarkForQCAndGpsRepair(conn, vehicleId);
				this.manualQC = temp.first;
				this.manualGpsRepair = temp.second;
			}
			long gateInTime = getGateInTime(conn);
			
			if (Misc.isUndef(grossPartyWt) && !Misc.isUndef(grossUnloadWt) && this.criticalRuleInfo.getGrossDiff() != null && !Misc.isUndef(this.criticalRuleInfo.getGrossDiff().getVal())) {
				if (this.partyGrossWtCalculated)
					grossPartyWt = this.partyGrossWt;
				else {
					this.partyGrossWt = grossPartyWt = GpsPlusViolations.getPartyGross(conn, tprId);
					this.partyGrossWtCalculated = true;
				}
			}
			
			if (this.qAnswered == null) {
				ArrayList qId = new ArrayList<Integer>();
				if (this.criticalRuleInfo.getTarpoline() != null && !Misc.isUndef(this.criticalRuleInfo.getTarpoline().getVal())) {
					qId.add(GpsPlusViolations.G_TARPOLINE_Q);
				}
				if (this.criticalRuleInfo.getTarpoline() != null && !Misc.isUndef(this.criticalRuleInfo.getTarpoline().getVal())) {
					qId.add(GpsPlusViolations.G_SEAL_Q);
				}
				this.qAnswered = GpsPlusViolations.getQAnswer(conn, tprId, qId);
			}
			
			if (this.nonTrackWindow == null && this.criticalRuleInfo.getGpsNonTrackDur() != null && !Misc.isUndef(criticalRuleInfo.getGpsNonTrackDur().getVal())) {
				this.nonTrackWindow = GpsPlusViolations.getNonTrackWindow(conn, vehicleId, this.srcTime, gateInTime, (long)(criticalRuleInfo.getGpsNonTrackDur().getVal()*1000*60),criticalRuleInfo.getNonTrackAllowedDistKM(), criticalRuleInfo.getNonTrackAllowedDistKM(), criticalRuleInfo.getNonTrackAllowedJumpSpeed(), g_defaultAllowedTrackDelay);//Misc.getUndefInt());//no non track allowance since till gate in only
			}
			
			int lowestPriority = Integer.MAX_VALUE;
			String qcReason = null;
			boolean doQC = false;
			if (manualQC != null && manualQC.first > 0) {
				lowestPriority = -1;
				qcReason = manualQC.second == null ? "Manually Asked" : manualQC.second;
				doQC = true;
			}
			else {//evaluate from stuff
				if (this.criticalRuleInfo.getTatDur() != null && !Misc.isUndef(this.criticalRuleInfo.getTatDur().getVal())) {
					double timeTaken = Misc.isUndef(this.srcTime) ? Misc.getUndefDouble() : (double)(gateInTime - this.srcTime)/(3600*1000);
					if (!Misc.isUndef(timeTaken) && !Misc.isUndef(this.tatSpecTime)) {
						double diff = timeTaken - this.tatSpecTime;
						double v = this.criticalRuleInfo.getTatDur().getVal();
						int priority = this.criticalRuleInfo.getTatDur().getReportingPriority();
						if (diff > v) {
							doQC = true;
							if (priority < lowestPriority) {
								qcReason = "Excess TAT";
								lowestPriority = priority;
							}
						}
					}
				}//for tat
				if (this.criticalRuleInfo.getGpsNonTrackDur() != null && !Misc.isUndef(this.criticalRuleInfo.getGpsNonTrackDur().getVal()) && this.nonTrackWindow != null) {
					/*long st = this.nonTrackWindow.first == null || this.nonTrackWindow.first.first <= 0 ? Misc.getUndefInt() : this.nonTrackWindow.first.first;
					long en = this.nonTrackWindow.second == null || this.nonTrackWindow.second.first <= 0 ? Misc.getUndefInt() : this.nonTrackWindow.second.first;
					double gapMin = Misc.isUndef(st) ? Double.MAX_VALUE : (double)(en-st)/(1000*60);
					if (gapMin > this.criticalRuleInfo.getGpsNonTrackDur().getVal()) {
						int priority = this.criticalRuleInfo.getGpsNonTrackDur().getReportingPriority();
						doQC = true;
						if (priority < lowestPriority) {
							qcReason = "Excess Data Loss/Non Track";
							lowestPriority = priority;
						}
					}*/
					if (this.nonTrackWindow != null && this.nonTrackWindow.second.first > 0) {
						int priority = this.criticalRuleInfo.getGpsNonTrackDur().getReportingPriority();
						doQC = true;
						if (priority < lowestPriority) {
							qcReason = "Excess Data Loss/Non Track";
							lowestPriority = priority;
						}
					}
				}//for non-track
				if (!Misc.isUndef(grossPartyWt) && !Misc.isUndef(grossUnloadWt) && this.criticalRuleInfo.getGrossDiff() != null && !Misc.isUndef(this.criticalRuleInfo.getGrossDiff().getVal())) {
					double gap = Math.abs(grossPartyWt - grossUnloadWt);
					if (gap > this.criticalRuleInfo.getGrossDiff().getVal()) {
						int priority = this.criticalRuleInfo.getGrossDiff().getReportingPriority();
						doQC = true;
						if (priority < lowestPriority) {
							qcReason = "Excess Gross Wt Difference";
							lowestPriority = priority;
						}
					}
				}//for excess gross wt
				
				if (this.qAnswered != null && this.criticalRuleInfo.getTarpoline() != null && !Misc.isUndef(this.criticalRuleInfo.getTarpoline().getVal())) {
					int askedVal = (int)Math.round(this.criticalRuleInfo.getTarpoline().getVal());
					int v = GpsPlusViolations.getAnswerForQ(this.qAnswered, this.G_TARPOLINE_Q);
					if (Misc.isUndef(v))
						v = 3;//Not checked;
					if ((askedVal == 3 && v != 1) || (askedVal == 2 && v == 2)) {
						int priority = this.criticalRuleInfo.getTarpoline().getReportingPriority();
						doQC = true;
						if (priority < lowestPriority) {
							qcReason = "Trapoline missing";
							lowestPriority = priority;
						}
					}
				}//for tarpoline
				if (this.qAnswered != null && this.criticalRuleInfo.getSeal() != null && !Misc.isUndef(this.criticalRuleInfo.getSeal().getVal())) {
					int askedVal = (int)Math.round(this.criticalRuleInfo.getSeal().getVal());
					int v = GpsPlusViolations.getAnswerForQ(this.qAnswered, this.G_SEAL_Q);
					if (Misc.isUndef(v))
						v = 3;//Not checked;
					if ((askedVal == 3 && v != 1) || (askedVal == 2 && v == 2)) {
						int priority = this.criticalRuleInfo.getSeal().getReportingPriority();
						doQC = true;
						if (priority < lowestPriority) {
							qcReason = "Seal missing";
							lowestPriority = priority;
						}
					}
				}//for seal
				
				if (this.criticalRuleInfo.getEvent() != null && !Misc.isUndef(this.criticalRuleInfo.getEvent().getVal())) {
					if (this.calcSummaryGpsViolations == null)
						getGpsQCViolationsSummary(conn);//init
					if (this.calcSummaryGpsViolations.first) {
						int priority = this.criticalRuleInfo.getEvent().getReportingPriority();
						doQC = true;
						if (priority < lowestPriority) {
							qcReason = "Gps Violations:"+this.calcSummaryGpsViolations.second;
							lowestPriority = priority;
						}
					}
				}//for GPS violations
			}
			ResultEnum first = !doQC ? ResultEnum.GREEN : ResultEnum.RED;
			String text= !doQC ? "No" : "Yes - "+qcReason;
			return new Pair<ResultEnum, String>(first, text);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
		
	}
	public Pair<ResultEnum, String> getDriverHours(Connection conn, int driverId)  {
		String q = " select sum(case when tat_station_details.min_time is null then tat_station_details.max_time "+
				   " else tat_station_details.max_time end) totTransit, count(*), max(tpr.latest_unload_gate_in_out) "+
				   " from tp_record tpr left outer join mines_details on (tpr.mines_id = mines_details.id) "+
	               " left outer join tat_station_details on (tat_station_details.from_opstation = mines_details.opstation_id) "+
				   " where tpr.driver_id=? and tpr.latest_unload_gate_in_out > ? and  tpr.latest_unload_gate_in_out < ?"
	               ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		double totTransit = Misc.getUndefDouble();
		int cnt = Misc.getUndefInt();
		long prevTripTIme = Misc.getUndefInt();
		try {
			ps = conn.prepareStatement(q);
			ps.setInt(1, driverId);
			ps.setTimestamp(2, Misc.longToSqlDate(this.currTime-24*3600*1000));
			ps.setTimestamp(3, Misc.longToSqlDate(this.currTime));
			rs = ps.executeQuery();
			if (rs.next()) {
				totTransit = Misc.getRsetDouble(rs,1);
				cnt = Misc.getRsetInt(rs, 2);
				prevTripTIme = Misc.sqlToLong(rs.getTimestamp(3));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		try {
			if (Misc.isUndef(cnt))
				cnt = 0;
			if (Misc.isUndef(totTransit))
				totTransit = 0;
			ResultEnum first = ResultEnum.GREEN;
			StringBuilder sb = new StringBuilder();
			sb.append(cnt);
			if (prevTripTIme > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
				sb.append("(Last:").append(sdf.format(new java.util.Date(prevTripTIme))).append(")");
			}
			
			double totDrivHr = cnt*2*GpsPlusViolations.g_stdLoadUnloadHr+2*totTransit;
			if (!Misc.isUndef(this.tatSpecTime))
				totDrivHr += this.tatSpecTime;
			totDrivHr += GpsPlusViolations.g_stdLoadUnloadHr;
			cnt++; //for curr
			if (cnt > 3 || totDrivHr > GpsPlusViolations.g_workHours)
				first = ResultEnum.RED;
			return new Pair<ResultEnum, String>(first, sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	
	public Pair<ResultEnum, String> getSafetyViolations(Connection conn)  {
		try {
		Pair<Boolean, String> result = this.calcSafetyViolations;
		if (result == null)
			calcSafetyViolations = result = GpsPlusViolations.getSummaryGPSSafetyViolation(conn, this.vehicleId, this.srcTime);
		ResultEnum first = result == null || !result.first ? ResultEnum.GREEN : ResultEnum.RED;
		String text = first == ResultEnum.GREEN ? "No" : "Yes - "+result.second;//result will be non null if not green
		return new Pair<ResultEnum, String>(first, text);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	public Pair<ResultEnum, String> getGpsQCViolationsSummary(Connection conn, double grossWt) {
		return getGpsQCViolationsSummary(conn);
	}
	public Pair<ResultEnum, String> getGpsQCViolationsSummary(Connection conn) {
		try {
			Pair<Boolean, String> result = calcSummaryGpsViolations;
			if (result == null) {
				ArrayList<Integer> ruleIds = this.criticalRuleInfo.getRulesUsed();
				int critThreshold = this.criticalRuleInfo.getEvent() == null || Misc.isUndef(this.criticalRuleInfo.getEvent().getVal()) ? Misc.getUndefInt()
						: (int)Math.round(this.criticalRuleInfo.getEvent().getVal());
				calcSummaryGpsViolations = result = GpsPlusViolations.getSummaryGPSQCViolation(conn, vehicleId, ruleIds, critThreshold, this.srcTime);
			}
			ResultEnum first = result == null || !result.first ? ResultEnum.GREEN : ResultEnum.RED;
			String text = first == ResultEnum.GREEN ? "No" : "Yes - "+result.second;//result will be non null if not green
			return new Pair<ResultEnum, String>(first, text);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	
	public Pair<ResultEnum, ArrayList<String>> getGpsQCViolationsDetailed(Connection conn, double grossWt) {
		return getGpsQCViolationsDetailed(conn);
	}
	public Pair<ResultEnum, ArrayList<String>> getGpsQCViolationsDetailed(Connection conn) {
		try {
			if (this.calcDetailedGPSViolations == null) {
				ArrayList<Integer> ruleIds = this.criticalRuleInfo.getRulesUsed();
				int critThreshold = this.criticalRuleInfo.getEvent() == null || Misc.isUndef(this.criticalRuleInfo.getEvent().getVal()) ? Misc.getUndefInt()
						: (int)Math.round(this.criticalRuleInfo.getEvent().getVal());
				calcDetailedGPSViolations = GpsPlusViolations.getDetailedGPSQCViolation(conn, vehicleId, ruleIds, critThreshold, this.srcTime);
				if (this.nonTrackWindow == null && this.criticalRuleInfo.getGpsNonTrackDur() != null && !Misc.isUndef(criticalRuleInfo.getGpsNonTrackDur().getVal())) {
					long gateInTime = getGateInTime(conn);
					this.nonTrackWindow = GpsPlusViolations.getNonTrackWindow(conn, vehicleId, this.srcTime, gateInTime, (long)(criticalRuleInfo.getGpsNonTrackDur().getVal()*1000*60),criticalRuleInfo.getNonTrackAllowedDistKM(), criticalRuleInfo.getNonTrackAllowedDistKM(), criticalRuleInfo.getNonTrackAllowedJumpSpeed(), Misc.getUndefInt());//no non track allowance since till gate in only
				}
				String nonTrackWindowText = this.getNonTrackWindowText(this.criticalRuleInfo, this.nonTrackWindow);
				if (nonTrackWindowText != null && calcDetailedGPSViolations.size() > 0) {
					boolean doNonTrackLast = true;
					if (this.criticalRuleInfo != null && this.criticalRuleInfo.getGpsNonTrackDur() != null && this.criticalRuleInfo.getEvent() != null && this.criticalRuleInfo.getGpsNonTrackDur().getReportingPriority() < this.criticalRuleInfo.getEvent().getReportingPriority()) {
						doNonTrackLast = false;
					}
					if (doNonTrackLast )
						calcDetailedGPSViolations.add(nonTrackWindowText);
					else 
						calcDetailedGPSViolations.add(0, nonTrackWindowText);
				}
			}
			ResultEnum first = calcDetailedGPSViolations == null ||calcDetailedGPSViolations.size() == 0 ? ResultEnum.GREEN : ResultEnum.RED;
			return new Pair<ResultEnum, ArrayList<String>>(first, calcDetailedGPSViolations);
		}
		catch (Exception e) {
			e.printStackTrace();
			ArrayList<String> temp = new ArrayList<String>();
			temp.add("ERROR");
			return new Pair<ResultEnum, ArrayList<String>>(ResultEnum.RED, temp);
		}
	}
	
	public Pair<ResultEnum, String> getGpsRepairNeeded(Connection conn) {
		try {
			if (this.manualGpsRepair == null) {
				Pair<Pair<Integer, String>, Pair<Integer, String>> temp = GpsPlusViolations.getManualMarkForQCAndGpsRepair(conn, vehicleId);
				this.manualGpsRepair = temp.second;
				this.manualQC = temp.first;
			}
			if (this.calcLastTracked == null)
				this.calcLastTracked = GpsPlusViolations.getLastTracked(conn, vehicleId);
			ResultEnum first = ResultEnum.GREEN;
			String text = "No";
			if (manualGpsRepair != null && manualGpsRepair.first > 0) {
				first = ResultEnum.RED;
				text = "Yes - "+(manualGpsRepair.second == null ? "Manually asked" : manualGpsRepair.second);
			}
			else {
				if (this.calcLastTracked != null) {
					long gapMill = this.currTime - this.calcLastTracked.first;
					if (gapMill > GpsPlusViolations.g_defaultAllowedTrackDelay || this.calcLastTracked.first <= 0) {
						first = ResultEnum.RED;
						SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
						text = "Yes - "+(this.calcLastTracked.first > 0 ? sdf.format(new java.util.Date(this.calcLastTracked.first)) : "Never Tracked");
					}
					else {
						//TODO that the vehicle is within Plant
					}
				}
			}
			return new Pair<ResultEnum, String>(first, text);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	
	public Pair<ResultEnum, String> getGpsIsTracking(Connection conn)  {
		try {
			if (this.calcLastTracked == null)
				this.calcLastTracked = GpsPlusViolations.getLastTracked(conn, vehicleId);
			ResultEnum first = ResultEnum.GREEN;
			String text = "Yes";
			if (this.calcLastTracked != null) {
				long gapMill = this.currTime - this.calcLastTracked.first;
				if (gapMill > GpsPlusViolations.g_defaultAllowedTrackDelay || this.calcLastTracked.first <= 0) {
					first = ResultEnum.RED;
					SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
					text = "No - "+(this.calcLastTracked.first > 0 ? sdf.format(new java.util.Date(this.calcLastTracked.first)) : "Never Tracked");
				}
				else {
					//TODO that the vehicle is within Plant
				}
			}
			return new Pair<ResultEnum, String>(first, text);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	
	public Pair<ResultEnum, String> getTATDistance(Connection conn) {
		try {
			double dist = this.distTravelled;
			if (!this.distTravelledCalc)
				dist = GpsPlusViolations.getDistTravelled(conn, vehicleId, this.srcTime);
			StringBuilder sb = new StringBuilder();
			java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,### KM");
			sb.append("[").append(Misc.isUndef(dist) ? "N/A" : formatter.format(dist));
			sb.append("/").append(Misc.isUndef(this.tatSpecDist) ? "N/A" : formatter.format(this.tatSpecDist));
			sb.append("]");
			ResultEnum first = ResultEnum.GREEN;
			if (!Misc.isUndef(dist) && !Misc.isUndef(this.tatSpecDist) && !Misc.isEqual(dist, this.tatSpecDist, 0.1, GpsPlusViolations.g_distPlusMinusPerc)) {
				first = ResultEnum.RED;
			}
			return new Pair<ResultEnum, String>(first, sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	public Pair<ResultEnum, String> getTATTiming(Connection conn)  {
		try {
			double timeTaken = Misc.isUndef(this.srcTime) ? Misc.getUndefDouble() : (double)(this.currTime - this.srcTime)/(3600*1000);
			
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(Misc.isUndef(timeTaken) ? "N/A" : getStringForInterval(timeTaken));
			sb.append("/").append(Misc.isUndef(this.tatSpecTime) ? "N/A" : getStringForInterval(this.tatSpecTime));
			sb.append("]");
			ResultEnum first = ResultEnum.GREEN;
			if (!Misc.isUndef(timeTaken) && !Misc.isUndef(this.tatSpecTime) && !Misc.isEqual(timeTaken, this.tatSpecTime) && timeTaken > this.tatSpecTime) {
				first = ResultEnum.RED;
			}
			return new Pair<ResultEnum, String>(first, sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			return new Pair<ResultEnum, String>(ResultEnum.RED, "ERROR");
		}
	}
	
	public Pair<ResultEnum, String> getViolationBlocked(Connection conn)  {
		return new Pair<ResultEnum, String>(ResultEnum.GREEN, "No");
	}
   //////// Other helper/useful methods for desktop
	//dont want to use Workflow methods ... else extendedTables can be obtained from there itself
	/*
	public void copyToApproved(Connection conn, String baseTable, String primCol, String fkName, ArrayList<String> extendedTables, ArrayList<String> nestedTables, int objectId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (primCol == null)
				primCol = "id";
			if (fkName == null)
				fkName = "object_id";

			String toSuffix = "_apprvd";
			for (int art=0,arts = (extendedTables == null ? 0 : extendedTables.size())+1; art<arts;art++) {
				//1st make sure we have rows in baseTable and extendedTables
				String fromTab = art == 0 ? baseTable : extendedTables.get(art-1);
				String toTab = fromTab+toSuffix;
				String objectColName = art == 0 ? primCol : fkName;
				ps = conn.prepareStatement("select "+objectColName)
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	*/
	////////   PRIVATE methods follow not to be used externally
	private static String getStringForInterval(double hrs) {
		int hr = (int) Math.floor(hrs);
		double frac = hrs-hr;
		int min = (int) Math.floor(frac*60);
		return hr +"h "+min+"m";
	}
	private CriticalRuleInfo getCriticalRuleInfo(Connection conn, int vehicleId) {
		if (this.criticalRuleInfoCalculated)
			return this.criticalRuleInfo;
		this.criticalRuleInfo = CriticalRuleInfo.getCriticalRuleInfo(conn, vehicleId, true);
		this.criticalRuleInfoCalculated = true;
		return this.criticalRuleInfo;
	}
	private void setTATSpecInfo(Connection conn, int minesId) throws Exception {
		if (this.tatSpecCalculated)
			return;
		this.tatSpecCalculated = true;
		Pair<Double, Double> tatDistTime = GpsPlusViolations.getTatSpecInfo(conn, minesId);
		if (tatDistTime != null) {
			this.tatSpecDist = tatDistTime.first;
			this.tatSpecTime = tatDistTime.second;
		}
	}
	private void setUpSrcInfo(Connection conn, int vehicleId, int tprId, int srcMineId, long srcTiming) throws Exception {
		if (this.srcCalculated)
			return;
		this.srcCalculated = true;
		if (!Misc.isUndef(srcMineId)) {
			this.srcMinesId = srcMineId;
			this.srcTime = srcTiming;
		}
		if (Misc.isUndef(srcMineId) || Misc.isUndef(srcTime)) {
			Pair<Integer, Long> retval = GpsPlusViolations.getSrcFromTpr(conn, tprId);
			if(retval != null){
				System.out.println("TPR Loading mine/time:"+retval.first+"/"+retval.second);
			}
			if (retval != null && !Misc.isUndef(retval.first))
				this.srcMinesId = retval.first;
			if (retval != null && !Misc.isUndef(retval.second))
				this.srcTime = retval.second;
		}
		if (Misc.isUndef(srcMineId) || Misc.isUndef(srcTime)) {
			Pair<Integer, Long> retval = GpsPlusViolations.getSrcFromTripInfo(conn, vehicleId);
			if(retval != null){
				System.out.println("GPS Loading mine/time:"+retval.first+"/"+retval.second);
			}
			if (retval != null && !Misc.isUndef(retval.first))
				this.srcMinesId = retval.first;
			if (retval != null && !Misc.isUndef(retval.second))
				this.srcTime = retval.second;
		}
		this.setTATSpecInfo(conn, srcMinesId);
		if (!Misc.isUndef(this.tatSpecTime) && Misc.isUndef(srcTime)){
			srcTime = this.currTime - (long)(this.tatSpecTime*60*60*1000);
			System.out.println("TAT Time:"+this.tatSpecTime);
		}
	}
	
	
	private GpsPlusViolations(int vehicleId, int tprId, long currTime) {
		this.vehicleId = vehicleId;
		this.tprId = tprId;
		this.currTime = currTime;
	}
	
	private static Pair<Integer, Long> getSrcFromTpr(Connection conn, int tprId) throws Exception {
		int srcMineId = Misc.getUndefInt();
		long srcTiming = Misc.getUndefInt();
		if (Misc.isUndef(tprId)) {
			return new Pair<Integer, Long>(srcMineId, srcTiming);
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			String q = "select mines_id, challan_date from tp_record where tpr_id = ?";
			ps = conn.prepareStatement(q);
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			if (rs.next()) {
				srcMineId = Misc.getRsetInt(rs, 1);
				srcTiming = Misc.sqlToLong(rs.getTimestamp(2));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Integer, Long>(srcMineId, srcTiming);
	}
	
	private static Pair<Integer, Long> getSrcFromTripInfo(Connection conn, int vehicleId) throws Exception {
		int srcMineId = Misc.getUndefInt();
		long srcTiming = Misc.getUndefInt();
		if (Misc.isUndef(vehicleId)) {
			return new Pair<Integer, Long>(srcMineId, srcTiming);
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			String q = "select mines_details.id, trip_info.load_gate_out from trip_info left outer join mines_details on (mines_details.opstation_id = trip_info.load_gate_op) where vehicle_id = ? and confirm_time is not null order by combo_start desc limit 1";
			ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			if (rs.next()) {
				srcMineId = Misc.getRsetInt(rs, 1);
				srcTiming = Misc.sqlToLong(rs.getTimestamp(2));
				
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Integer, Long>(srcMineId, srcTiming);
	}
	
	public static Pair<Double, Double> getTatSpecInfo(Connection conn, int minesId) throws Exception {
		double tatDist = Misc.getUndefDouble();
		double tatTime = Misc.getUndefDouble();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			String q = "select tat_station_details.typical_distance, tat_station_details.max_time from  tat_station_details where  tat_station_details.from_opstation = ? ";
			ps = conn.prepareStatement(q);
			ps.setInt(1, minesId);
			rs = ps.executeQuery();
			if (rs.next()) {
				tatDist = Misc.getRsetDouble(rs, 1);
				tatTime = Misc.getRsetDouble(rs, 2);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Double, Double>(tatDist, tatTime);
	}
	
	private static void setMarkForQCOrGpsRepair(Connection conn, int vehicleId, int tprId, boolean doQC, boolean toSetTo1, String reason) throws Exception {
		//update flags and make entry in vehicle_history
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder q = new StringBuilder();
			String flagColName = doQC ? "mark_for_qc" : "mark_for_gps_repair";
			String reasonColName = doQC ? "mark_for_qc_reason" : "mark_for_gps_repair_reason";
			q.append("update vehicle_extended set ").append(flagColName).append("=?,").append(reasonColName).append("=? where vehicle_id = ?");
			ps = conn.prepareStatement("select 1 from vehicle_extended where vehicle_id = ?");//check if exists
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			boolean exists  = rs.next();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (!exists) {
				ps = conn.prepareStatement("insert into vehicle_extended(vehicle_id) (?)");
				ps.setInt(1, vehicleId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
			ps = conn.prepareStatement(q.toString());
			ps.setInt(1, toSetTo1 ? 1 : 0);
			if (!toSetTo1) {
				reason = "Reset: applied in TPR:"+tprId;
			}
			ps.setString(2, reason);
			ps.setInt(3, vehicleId);
			ps.execute();
			ps.close();
			q.setLength(0);
			q.append("insert into vehicle_history (vehicle_id, device_id, on_date, description, change_code, change_to,updated_by) ")
			.append(" (select vehicle.id, vehicle.device_internal_id, now(), ?,?,?,? from vehicle where id = ?)");
			ps = conn.prepareStatement(q.toString());
			String desc = (doQC ? "QC set to " : "Repair set to ")+(toSetTo1 ? "1 " : "0 ")+reason;
			ps.setString(1, desc);
			ps.setInt(2, doQC ? GpsPlusViolations.CHANGE_CODE_FOR_MARK_FORQC : GpsPlusViolations.CHANGE_CODE_FOR_MARK_FOR_REPAIR);
			ps.setInt(3, toSetTo1 ? 1 : 0);
			ps.setInt(4,1);
			ps.setInt(5, vehicleId);
			ps.setInt(1, vehicleId);
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	private static Pair<Pair<Long, String>, Pair<Long, String>> getNonTrackWindow(Connection conn, int vehicleId, long since, long till, long minGapMilli, double allowedDist, double allowedImpliedSpeedLow, double jumpImpliedSpeed, long allowedTrackDelay) throws Exception {
		long st = Misc.getUndefInt();
		String stName = null;
		long en = Misc.getUndefInt();
		String enName = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId))
			return new Pair<Pair<Long, String>, Pair<Long, String>>(new Pair<Long, String>(st, stName), new Pair<Long, String>(en, enName));
		try {
			if (till <= 0)
				till = System.currentTimeMillis();
			if (since <= 0)
				since = till-(long)(GpsPlusViolations.g_defaultTripDurHr*1000*3600);
			ps = conn.prepareStatement("select logged_data.longitude, logged_data.latitude, logged_data.gps_record_time, logged_data.speed, logged_data.attribute_value, logged_data.name from logged_data where vehicle_id=? and attribute_id=0 and gps_record_time between ? and ? order by gps_record_time ");
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.utilToSqlDate(since));
			ps.setTimestamp(3, Misc.utilToSqlDate(till));
			rs = ps.executeQuery();
			double prevLon = Misc.getUndefDouble();
			double prevLat = Misc.getUndefDouble();
			long prevGrt = since;
			double prevSpeed = Misc.getUndefDouble();
			double prevCummD = Misc.getUndefDouble();
			String prevName = null;
			boolean dataFound = false;
			while (rs.next()) {
				dataFound = true;
				double lon = Misc.getRsetDouble(rs, 1);
				double lat = Misc.getRsetDouble(rs, 2);
				long grt = Misc.sqlToLong(rs.getTimestamp(3));
				double speed = Misc.getRsetDouble(rs,4);
				double cummD = Misc.getRsetDouble(rs, 5);
				String name = rs.getString(6);
				if (grt <= 0 || Misc.isUndef(lon) || Misc.isUndef(lat))
					continue;
				long gap = grt - prevGrt;
				
				double distTravelledByPt = Misc.getUndefDouble();
				double distDiff = Misc.isUndef(prevCummD) || Misc.isUndef(cummD) ? Misc.getUndefDouble() : cummD - prevCummD;
				if (!Misc.isUndef(prevLon) && !Misc.isUndef(lon)) {
					distTravelledByPt = Point.fastGeoDistance(lon, lat, prevLon, prevLat);
				}
				double dist = distTravelledByPt > -0.005 && distTravelledByPt > distDiff ? distTravelledByPt : distDiff;
				double impliedSpeed = dist < -0.005 ? Misc.getUndefDouble() : dist*3600*1000/gap;
				boolean suspect = false;
				if (jumpImpliedSpeed > 0 && impliedSpeed > jumpImpliedSpeed) {
					suspect = true;
				}
				else if (gap > minGapMilli) {
					if (allowedDist > 0 && dist > allowedDist) {//check if the impliedSpeed > lowLimit ... if so we consider non suspext
						if (allowedImpliedSpeedLow > 0 && impliedSpeed > allowedImpliedSpeedLow) {
							
						}
						else {
							suspect = true;
						}
					}
					else {
						suspect = true;
					}//dist exceeds 
					if (suspect) {
						//check if in region ... we found non-compliant and therefore done ..
						if (!isInOpRegion(conn, prevLon, prevLat, lon, lat)) {
							suspect = true;
							
						}
					}//if suspect
				}//gap > min
				if (suspect) {
					st = prevGrt;
					stName = prevName == null ? name : prevName;
					en = grt;
					enName = name == null ? prevName : name;
					break;
				}
				
				prevLon = lon;
				prevLat = lat;
				prevGrt = grt;
				prevSpeed = speed;
				prevCummD = cummD;
				prevName = name;
				
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (dataFound) {
				if (st <= 0 && allowedTrackDelay > 0) {//check if non-tracking at end
					long gap = till - prevGrt;
					if (gap > minGapMilli) {
						st = prevGrt;
						stName = prevName;
						en = till;
						enName = prevName;
					}
				}
			}
			else {
				st = Misc.getUndefInt();
				en = till;
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Pair<Long, String>, Pair<Long, String>>(new Pair<Long, String>(st, stName), new Pair<Long, String>(en, enName));
	}
	private static boolean isInOpRegion(Connection conn, double lon1, double lat1, double lon2, double lat2) {
		boolean retval = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			retval = false; //TODO
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	private static Pair<Pair<Integer, String>, Pair<Integer, String>> getManualMarkForQCAndGpsRepair(Connection conn, int vehicleId) throws Exception {
		int manualMarkForQC = 0;
		int manualMarkForRepair = 0;
		String qcReason = null;
		String repairReason = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId))
			return new Pair<Pair<Integer, String>, Pair<Integer, String>>(new Pair<Integer, String>(manualMarkForQC, qcReason), new Pair<Integer, String>(manualMarkForRepair, repairReason));
		try {
			String q = "select ve.mark_for_qc, ve.mark_for_qc_reason, ve.mark_for_gps_repair, ve.mark_for_gps_repair_reason from vehicle join vehicle_extended ve on (vehicle.id = ve.vehicle_id) where vehicle.id = ? ";
			ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			if (rs.next()) {
				manualMarkForQC = rs.getInt(1);
				qcReason = rs.getString(2);
				manualMarkForRepair = rs.getInt(3);
				repairReason = rs.getString(4);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Pair<Integer, String>, Pair<Integer, String>>(new Pair<Integer, String>(manualMarkForQC, qcReason), new Pair<Integer, String>(manualMarkForRepair, repairReason));
	}
	
	private static Triple<Long, Pair<Double, Double>, String> getLastTracked(Connection conn, int vehicleId) throws Exception {
		long trackTime = Misc.getUndefInt();
		double lon = Misc.getUndefDouble();
		double lat = Misc.getUndefDouble();
		String name = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId))
			return new Triple<Long, Pair<Double, Double>, String>(trackTime, new Pair<Double, Double>(lon, lat), null);
		try {
			String q = "select gps_record_time, longitude, latitude, name from logged_data where vehicle_id=? and attribute_id=0 order by gps_record_time desc limit 1";
			ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			if (rs.next()) {
				trackTime = Misc.sqlToLong(rs.getTimestamp(1));
				lon = Misc.getRsetDouble(rs, 2);
				lat = Misc.getRsetDouble(rs, 3);
				name = rs.getString(4);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Triple<Long, Pair<Double, Double>, String>(trackTime, new Pair<Double, Double>(lon, lat), null);
	}
	
	private static Pair<Boolean, String> getSummaryGPSSafetyViolation(Connection conn, int vehicleId, long srcTiming) throws Exception {
		boolean retval = false;
		String name = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId) || srcTiming <= 0)
			return new Pair<Boolean, String>(retval, name);
		try {
			StringBuilder q = new StringBuilder();
			q.append("select rules.name from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ")
			.append(" join safety_gps_rules on (safety_gps_rules.port_node_id = anc.id) join engine_events on (engine_events.vehicle_id = vehicle.id and engine_events.rule_id = safety_gps_rules.rule_id) ")
			.append(" join rules on (rules.id = engine_events.rule_id) where vehicle.id = ? and engine_events.event_start_time >= ? order by event_start_time desc limit 1 ");
			ps = conn.prepareStatement(q.toString());
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.longToSqlDate(srcTiming));
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = true;
				name = rs.getString(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Boolean, String>(retval, name);
	}


	private static Pair<Boolean, String> getSummaryGPSQCViolation(Connection conn, int vehicleId, ArrayList<Integer> ruleIds, int critThreshold, long srcTiming) throws Exception {
		boolean retval = false;
		String name = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId) || srcTiming <= 0 || Misc.isUndef(critThreshold) || ruleIds == null || ruleIds.size() == 0)
			return new Pair<Boolean, String>(retval, name);
		try {
			StringBuilder q = new StringBuilder();
			q.append("select rules.name from engine_events ");
			q.append(" join rules on (rules.id = engine_events.rule_id) ");
			q.append(" where vehicle_id = ? and rule_id in (");
			Misc.convertInListToStr(ruleIds, q);
			q.append(") ");
			
			q.append(" and event_start_time >= ? and criticality >= ? order by criticality desc, engine_events.event_start_time desc limit 1");
			ps = conn.prepareStatement(q.toString());
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.longToSqlDate(srcTiming));
			ps.setInt(3, critThreshold);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = true;
				name = rs.getString(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<Boolean, String>(retval, name);
	}
	
	 
	private static ArrayList<String> getDetailedGPSQCViolation(Connection conn, int vehicleId, ArrayList<Integer> ruleIds, int critThreshold, long srcTiming) throws Exception {
		ArrayList<String> retval = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId) || srcTiming <= 0 || Misc.isUndef(critThreshold) || ruleIds == null || ruleIds.size() == 0)
			return retval;
		try {
			StringBuilder q = new StringBuilder();
			q.append("select ee.criticality, rules.name, ee.event_start_time, ee.event_begin_name, ee.event_stop_time, ee.event_end_name ")
			.append(" from engine_events ee ");
			q.append(" join rules on (rules.id = ee.rule_id) ");
			q.append(" where ee.vehicle_id = ? and ee.rule_id in (");
			Misc.convertInListToStr(ruleIds, q);
			q.append(")");
			
			q.append(" and ee.event_start_time >= ? and ee.criticality >= ? order by ee.criticality desc, ee.event_start_time desc ");
			ps = conn.prepareStatement(q.toString());
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.longToSqlDate(srcTiming));
			ps.setInt(3, critThreshold);
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				int crit = Misc.getRsetInt(rs, 1);
				String name = Misc.getRsetString(rs, 2);
				long tsStart = Misc.sqlToLong(rs.getTimestamp(3));
				String locName = Misc.getRsetString(rs, 4);
				long tsEnd = Misc.sqlToLong(rs.getTimestamp(5));
				String endName = Misc.getRsetString(rs, 6);
				StringBuilder entry = new StringBuilder();
				entry.append(crit)
				.append(" - ")
				.append(name)
				.append(" - ")
				.append(locName)
				.append(" at ")
				.append(sdf.format(new java.util.Date(tsStart)));
				;
				retval.add(entry.toString());
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	private static double getDistTravelled(Connection conn, int vehicleId, long srcTiming) throws Exception {
		double dist = Misc.getUndefDouble();
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(vehicleId) || srcTiming <= 0)
			return dist;
		try {
			String q = "select (dest.attribute_value - src.attribute_value) dist from " +
					"(select attribute_value from logged_data where vehicle_id = ? and attribute_id=0 and gps_record_time >= ? order by gps_record_time limit 1) src" +
					" cross join "+
					"(select attribute_value from logged_data where vehicle_id = ? and attribute_id=0 order by gps_record_time desc limit 1) dest "
					;
			ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			ps.setTimestamp(2, Misc.longToSqlDate(srcTiming));
			ps.setInt(3, vehicleId);
			rs = ps.executeQuery();
			if (rs.next()) {
				dist = Misc.getRsetDouble(rs, 1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return dist;
	}
	
	private static double getPartyGross(Connection conn, int tprId) throws Exception {
		double wt = Misc.getUndefDouble();
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(tprId))
			return wt;
		try {
			String q = "select load_gross from tp_record where tpr_id = ?";
			ps = conn.prepareStatement(q);
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			if (rs.next()) {
				wt = Misc.getRsetDouble(rs, 1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return wt;
	}
	private static int getAnswerForQ(ArrayList<Pair<Integer, Integer>> theList, int q) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is; i++) {
			if (theList.get(i).first == q)
				return theList.get(i).second;
		}
		return Misc.getUndefInt();
	}
	private static ArrayList<Pair<Integer, Integer>> getQAnswer(Connection conn, int tprId, ArrayList<Integer> qid) throws Exception {
		ArrayList<Pair<Integer, Integer>> retval = new ArrayList<Pair<Integer, Integer>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (Misc.isUndef(tprId) || qid == null || qid.size() == 0)
			return retval;
		try {
			StringBuilder q = new StringBuilder();
			q.append("select tps_question_detail.question_id, tps_question_detail.answer_id from tps_question_detail where tpr_id=? and question_id in (");
			Misc.convertInListToStr(qid, q);
			q.append(") ");
			ps = conn.prepareStatement(q.toString());
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			while (rs.next()) {
				retval.add(new Pair<Integer, Integer>(Misc.getRsetInt(rs,1), Misc.getRsetInt(rs,2)));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	private static String getNonTrackWindowText(CriticalRuleInfo critRule, Pair<Pair<Long, String>, Pair<Long, String>> nonTrackWindow) {
		StringBuilder retval = new StringBuilder();
		
		if (nonTrackWindow != null && nonTrackWindow.second.first > 0) {
			int priority = critRule == null || critRule.getGpsNonTrackDur() == null ? Misc.getUndefInt() : critRule.getGpsNonTrackDur().getReportingPriority();
			if (priority > 0)
				retval.append(priority).append("-");
			retval.append("NonTrack: ");
			if (nonTrackWindow.first.first <= 0)
				retval.append(" Never Tracked");
			else {
				SimpleDateFormat fmt = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
				int durMin = (int) Math.round((double)(nonTrackWindow.second.first - nonTrackWindow.first.first)/60000.0);
				retval.append(durMin).append(" min near ").append(nonTrackWindow.first.second).append(" at ").append(fmt.format(Misc.longToUtilDate(nonTrackWindow.first.first)));
			}
		}
		return retval.toString();
	}
	public static void main(String[] args) {
		Connection conn = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//TripCountMDB tripCountMDB = new TripCountMDB();

			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn.setAutoCommit(true);
			GpsPlusViolations violations = GpsPlusViolations.getGpsPlusViolatins(conn, 23580, 55665, new java.util.Date(116,0,17,18,32,10).getTime(),5, new java.util.Date(116,0,17,17,52,10).getTime());
			Pair<ResultEnum, String> getMarkQC = violations.getMarkForQC(conn, 24840, 25000);
			violations.getGpsQCViolationsDetailed(conn);
			if (true)
				return;
			Pair<Pair<Long, String>, Pair<Long, String>> nonTrack = violations.getNonTrackWindow(conn, violations.vehicleId, new java.util.Date(115,10,29).getTime(), new java.util.Date(115,11,4,6,0,0).getTime(), 120000, 0.07, 40, 150,30*60000);
			System.out.println(getNonTrackWindowText(violations.criticalRuleInfo,nonTrack));

			nonTrack = nonTrack = violations.getNonTrackWindow(conn, violations.vehicleId, new java.util.Date(115,10,29).getTime(), new java.util.Date(115,11,5,6,0,0).getTime(), 3600000, 0.07, 40, 150,30*60000);
			System.out.println(getNonTrackWindowText(violations.criticalRuleInfo,nonTrack));
			
			nonTrack = violations.getNonTrackWindow(conn, 1, new java.util.Date(115,10,29).getTime(), new java.util.Date(115,11,4).getTime(), 120000, 0.07, 40, 150, Misc.getUndefInt());
			System.out.println(getNonTrackWindowText(violations.criticalRuleInfo,nonTrack));
			
					   //1, new java.util.Date(115,11,16,22,0,0).getTime());
			System.out.println(violations.getDriverHours(conn, 1));
			System.out.println(violations.getGpsQCViolationsDetailed(conn));
			System.out.println(violations.getGpsQCViolationsSummary(conn));
			System.out.println(violations.getTATDistance(conn));
			System.out.println(violations.getTATTiming(conn));
			System.out.println(violations.getSafetyViolations(conn));
			System.out.println(violations.getGpsRepairNeeded(conn));
			System.out.println(violations.getGpsIsTracking(conn));
			violations.updateManualRepairFlag(conn, 10000);
			System.out.println(violations.getGpsIsTracking(conn));
			System.out.println(violations.getMarkForQC(conn, 3000, Misc.getUndefDouble()));
			violations.updateManualQCFlag(conn, 10000);
		
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public int getPortNodeId(Connection conn, int vehicleId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int retval = Misc.G_TOP_LEVEL_PORT;
		try {
			ps = conn.prepareStatement("select customer_id from vehicle where id = ?");
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			retval = rs.next() ? Misc.getRsetInt(rs, 1, retval) : retval;
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	public long getSrcTime() {
		return srcTime;
	}
	public static Pair<Long, String> getLatestLocation(Connection conn, int vehicleId) throws Exception {
        long trackTime = Misc.getUndefInt();
        String name = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        if (Misc.isUndef(vehicleId))
              return new Pair<Long, String>(trackTime,  null);
        try {
              String q = "select gps_record_time, name from current_data where vehicle_id=? and attribute_id=0";
              ps = conn.prepareStatement(q);
              ps.setInt(1, vehicleId);
              rs = ps.executeQuery();
              if (rs.next()) {
                    trackTime = Misc.sqlToLong(rs.getTimestamp(1));
                    name = rs.getString(2);
              }
              rs = Misc.closeRS(rs);
              ps = Misc.closePS(ps);
        }
        catch (Exception e) {
              e.printStackTrace();
              throw e;
        }
        finally {
              rs = Misc.closeRS(rs);
              ps = Misc.closePS(ps);
        }
        return new Pair<Long, String>(trackTime, name);
  }

	/*private static long dayMillis = 24*60*60*1000;
	public long getSuggestedChallanDate(Connection conn,int vehicleId, long challanDate){
		if(Misc.isUndef(challanDate))
			return Misc.getUndefInt();
		Pair<Integer, Long> gpsTimingPair = GpsPlusViolations.getSrcFromTripInfo(conn, vehicleId);
		long tatTiming = Misc.isUndef(tatSpecTime) ? Misc.getUndefInt() : currTime-(long)(tatSpecTime*3600*1000);
		long gpsTiming = Misc.isUndef(g_defaultTripDurHr) ? Misc.getUndefInt() : currTime-(long)(g_defaultTripDurHr*3600*1000);
		long challanDatePart = challanDate/dayMillis;
		if(gpsTiming.) 
	}*/
}

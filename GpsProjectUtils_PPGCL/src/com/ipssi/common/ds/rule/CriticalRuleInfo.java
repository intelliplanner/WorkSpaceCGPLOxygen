package com.ipssi.common.ds.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.common.ds.trip.ThreadContextCache;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.ShapeFileBean;
import com.ipssi.processor.utils.GpsData;

public class CriticalRuleInfo {
	public static class CritSpec {
		private double val;
		private int reportingPriority;
		public double getVal() {
			return val;
		}
		public void setVal(double val) {
			this.val = val;
		}
		public int getReportingPriority() {
			return reportingPriority;
		}
		public void setReportingPriority(int reportingPriority) {
			this.reportingPriority = reportingPriority;
		}
		public CritSpec(double val, int reportingPriority) {
			super();
			this.val = val;
			this.reportingPriority = reportingPriority;
		}
	}
	public static class CritEval {
		private int specId = Misc.getUndefInt();
		private int ruleId = Misc.getUndefInt();
		private boolean locOrRegion = false;
		private int itemId = Misc.getUndefInt();
		private int itemDistMtr = Misc.getUndefInt();
		private int itemType = Misc.getUndefInt();
		private String itemPattern = null;
		private int condition  = Misc.getUndefInt();
		private double v1 = Misc.getUndefDouble();
		private double v2 = Misc.getUndefDouble();
		private int criticality = Misc.getUndefInt();
		private int priority = Misc.getUndefInt();
		public boolean hasLocOrRegion() {
			return !Misc.isUndef(this.itemId) || !Misc.isUndef(this.itemType) || this.itemPattern != null;
		}
		public boolean conditionMet(double v) {
			if (condition == 1) {
				return v > v1 && !Misc.isEqual(v, v1);
			}
			else if (condition == 2) {
				return v < v1 && !Misc.isEqual(v, v1);
			}
			else if (condition == 3) {
				return Misc.isEqual(v, v1);
			}
			else if (condition == 4) {
				return Misc.isEqual(v, v1) || v >= v1;
			}
			else if (condition == 5) {
				return Misc.isEqual(v, v1) || v <= v1;
			}
			else if (condition == 6) {
				return !Misc.isEqual(v, v1);
			}
			else {
				return Misc.isEqual(v, v1) || Misc.isEqual(v, v2) || (v >= v1 && v <= v2);
			}
		}
		public int getRuleId() {
			return ruleId;
		}
		public void setRuleId(int ruleId) {
			this.ruleId = ruleId;
		}
		public boolean isLocOrRegion() {
			return locOrRegion;
		}
		public void setLocOrRegion(boolean locOrRegion) {
			this.locOrRegion = locOrRegion;
		}
		public int getItemId() {
			return itemId;
		}
		public void setItemId(int itemId) {
			this.itemId = itemId;
		}
		public int getItemDistMtr() {
			return itemDistMtr;
		}
		public void setItemDistMtr(int itemDistMtr) {
			this.itemDistMtr = itemDistMtr;
		}
		public int getItemType() {
			return itemType;
		}
		public void setItemType(int itemType) {
			this.itemType = itemType;
		}
		public String getItemPattern() {
			return itemPattern;
		}
		public void setItemPattern(String itemPattern) {
			if (itemPattern != null)
				itemPattern = itemPattern.toUpperCase();
			this.itemPattern = itemPattern;
		}
		public int getSpecId() {
			return specId;
		}
		public void setSpecId(int specId) {
			this.specId = specId;
		}
		public CritEval(int specId, int ruleId, boolean locOrRegion,
				int itemId, int itemDistMtr, int itemType, String itemPattern, int condition, double v1, double v2, int criticality, int priority) {
			super();
			this.specId = specId;
			this.ruleId = ruleId;
			this.locOrRegion = locOrRegion;
			this.itemId = itemId;
			this.itemDistMtr = itemDistMtr;
			this.itemType = itemType;
			if (itemPattern != null)
				itemPattern = itemPattern.toUpperCase();
			this.itemPattern = itemPattern;
			
			this.condition = condition;
			this.v1 = v1;
			this.v2 = v2;
			this.criticality = criticality;
			this.priority = priority;
		}
		public int getCondition() {
			return condition;
		}
		public void setCondition(int condition) {
			this.condition = condition;
		}
		public double getV1() {
			return v1;
		}
		public void setV1(double v1) {
			this.v1 = v1;
		}
		public double getV2() {
			return v2;
		}
		public void setV2(double v2) {
			this.v2 = v2;
		}
		public int getCriticality() {
			return criticality;
		}
		public void setCriticality(int criticality) {
			this.criticality = criticality;
		}
		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}
	}
	private int id = Misc.getUndefInt();
	private int portNodeId = Misc.getUndefInt();
	private CritSpec tatDur;
	private CritSpec gpsNonTrackDur;
	private double nonTrackAllowedDistKM;
	private double nonTrackAllowedAvgSpeed;
	private double nonTrackAllowedJumpSpeed;
	private CritSpec grossDiff;
	private CritSpec event;
	private CritSpec tarpoline;
	private CritSpec seal;
	//Approach: given an event with start/end point, we first see which critEval it falls in ignoring the duration parameter 
	//(ie based on matches to loc/region) - thereafter that is kept fixed and we evaluate crit rating based on the set of
	//critEval matching that loc or those that dont have any loc
	//So we keep a dataStructure of list of each rule Id that have  same locId/pattern ... and a hashmap of id to block (and index) and hashmap if rule id block 
	private ArrayList<ArrayList<CritEval>> ruleCritEvalsValidLocEval = new ArrayList<ArrayList<CritEval>>();
	private ArrayList<ArrayList<CritEval>> ruleCritEvalsNoLocEval = new ArrayList<ArrayList<CritEval>>();
	private HashMap<Integer, MiscInner.Pair> byCritEvalIdValidLoc = new HashMap<Integer, MiscInner.Pair>();//first = entry in ruleCritEvalsValidLocEval, second index in the array 
	private HashMap<Integer, MiscInner.Pair> byCritRuleIdNoLoc = new HashMap<Integer, MiscInner.Pair>();
	private HashMap<Integer, Pair<ArrayList<Integer>, Integer>> byCritRuleBlockListPlusMaxDist = new HashMap<Integer, Pair<ArrayList<Integer>, Integer>>(); 
	private ArrayList<Integer> rulesUsed = null;
	private void calcRulesUsed(Connection conn) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(CriticalRuleInfo.G_GET_RULEID);
			ps.setInt(1, this.getId());
			rs = ps.executeQuery();
			rulesUsed = new ArrayList<Integer>();
			while (rs.next()) {
				rulesUsed.add(rs.getInt(1));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public ArrayList<Integer> getRulesUsed() {
		if (rulesUsed != null)
			return rulesUsed;
		ArrayList<Integer> retval = new ArrayList<Integer>();
		for (int art=0;art<2;art++) {
			ArrayList<ArrayList<CritEval>> theList = art == 0 ? this.ruleCritEvalsValidLocEval : this.ruleCritEvalsNoLocEval;
			for (int i=0,is = theList.size(); i<is; i++) {
				ArrayList<CritEval> celist = theList.get(i);
				int ruleId = celist == null || celist.size() == 0 ? Misc.getUndefInt() : celist.get(0).getRuleId();
				if (Misc.isUndef(ruleId))
					continue;
				boolean found = false;
				for (Integer iv: retval) {
					if (iv == ruleId) {
						found = true;
						break;
					}
				}
				if (!found)
					retval.add(ruleId);
			}//for each critEval
		}//art loop
		return retval;
	}
	public Pair<Integer, CritEval> getCritEvalForEE(Connection conn,  int ruleId, double lon, double lat, long startGpsTime, long endGpsTime, int prevCritEvalId, int portNodeId, CacheTrack.VehicleSetup.DistCalcControl distCalcControl, int vehicleId) throws Exception {
		//if prevCritEvalid == 0 then, previously was in opstation and therefore will be returned as is
		//if prevCritiEvalId is negative, it may be ruleId or it may be undef
		//retval.first = the new critEvalId - if negative then negative of RuleId, unless it is undef ... small possibility of bug if rule Id becomes -undef
		//retval.second = null => nothing matches the duration (or other condition in future)
		//retval == null => no possible critEval for the ruleId ... so no point asking for
		
		//lock must be taken outside
		try {
			if (prevCritEvalId == 0)
				return new Pair<Integer, CritEval>(0, null);
			int matchingBlockIndex = -1;
			if (prevCritEvalId < 0 && prevCritEvalId == -1*ruleId) {
				//doing by rule
			}
			else {
				MiscInner.Pair entryFor = this.byCritEvalIdValidLoc.get(prevCritEvalId);
				if (entryFor != null && entryFor.first >= 0 && entryFor.first < this.ruleCritEvalsValidLocEval.size() && this.ruleCritEvalsValidLocEval.get(entryFor.first).size() > 0) {
					matchingBlockIndex = entryFor.first;
				}
				if (matchingBlockIndex < 0) {
					Pair<Boolean, ArrayList<RegionTestHelper>> opstnInAndRegionTest = ThreadContextCache.isInOpStationAndAllRegIn(conn, lon, lat, portNodeId, null, vehicleId, startGpsTime);
					if (opstnInAndRegionTest.first)
						return new Pair<Integer, CritEval>(0, null);
					
					matchingBlockIndex = getMatchingLocOrRegion(conn,  ruleId, lon, lat, startGpsTime, endGpsTime, prevCritEvalId, portNodeId, distCalcControl, opstnInAndRegionTest.second);
				}//if we needed to find out matching
			}
			if (matchingBlockIndex >= 0) {
				//check if the conditions are matched
				double gapDurMin = (double)(endGpsTime <0 ? System.currentTimeMillis() : endGpsTime - startGpsTime)/(1000.0*60.0);
				ArrayList<CritEval> ceList = this.ruleCritEvalsValidLocEval.get(matchingBlockIndex);
				CritEval actMatching = null;
				int critValIdFirst = ceList.get(0).getSpecId();
				for (int i=0,is=ceList == null ? 0 : ceList.size(); i<is; i++) {
					CritEval ce = ceList.get(i);
					if (ce.conditionMet(gapDurMin)) {
						actMatching = ce;
						break;
					}
				}
				return new Pair<Integer, CritEval>(critValIdFirst, actMatching);
			}
			else {//look in rule without any loc info
				MiscInner.Pair entryFor = this.byCritRuleIdNoLoc.get(ruleId);
				matchingBlockIndex = entryFor == null ? -1 : entryFor.first;
				if (matchingBlockIndex < 0 || matchingBlockIndex >= this.ruleCritEvalsNoLocEval.size())
					matchingBlockIndex = -1;
				if (matchingBlockIndex >= 0 && this.ruleCritEvalsNoLocEval.get(matchingBlockIndex).size() == 0)
					matchingBlockIndex = -1;
				if (matchingBlockIndex >= 0) {
					double gapDurMin = (double)(endGpsTime <= 0 ? System.currentTimeMillis() : endGpsTime - startGpsTime)/(1000.0*60.0);
					ArrayList<CritEval> ceList = this.ruleCritEvalsNoLocEval.get(matchingBlockIndex);
					CritEval actMatching = null;
					int critValIdFirst = -1*ruleId;
					for (int i=0,is=ceList == null ? 0 : ceList.size(); i<is; i++) {
						CritEval ce = ceList.get(i);
						if (ce.conditionMet(gapDurMin)) {
							actMatching = ce;
							break;
						}
					}
					return new Pair<Integer, CritEval>(critValIdFirst, actMatching);
				}
				return new Pair<Integer, CritEval>(0, null);//no non loc based criteval for rule
			}
		}
		catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		finally {
		}
	}
	
	private int getMatchingLocOrRegion(Connection conn,  int ruleId, double lon, double lat, long startGpsTime, long endGpsTime, int prevCritEvalId, int portNodeId, CacheTrack.VehicleSetup.DistCalcControl distCalcControl, ArrayList<RegionTestHelper> prevCalcResult) throws Exception {
		int matchingBlockIndex = -1;
		Pair<ArrayList<Integer>, Integer> blockPlusMaxDistEntry = byCritRuleBlockListPlusMaxDist.get(ruleId);
		if (blockPlusMaxDistEntry != null && blockPlusMaxDistEntry.first != null && blockPlusMaxDistEntry.first.size() > 0) {
			double distKM = blockPlusMaxDistEntry.second.doubleValue()/1000.0;
			//get neighbouring LM and containing regions ... then for each block, check the first one that has an entry in one of the list and if so that becomes the critEvalId to use
			ArrayList<Triple<ShapeFileBean, Double, Double>> nearLandMarks = RTreeSearch.getNearestNLMOrMap(conn, lon, lat, portNodeId, distKM, distCalcControl);
			ArrayList<RegionTestHelper> regionList =prevCalcResult != null ? prevCalcResult :  RTreeSearch.getContainingRegions(new com.ipssi.geometry.Point(lon,lat));
			
			for (int i=0,is=blockPlusMaxDistEntry.first.size();i<is;i++) {
				int idx = blockPlusMaxDistEntry.first.get(i);
				ArrayList<CritEval> critEvalList = ruleCritEvalsValidLocEval.get(idx);
				CritEval ce = critEvalList == null || critEvalList.size() == 0 ? null : critEvalList.get(0);
				if (ce == null)
					continue;
				if (ce.locOrRegion) {
					for (int j=0,js=nearLandMarks == null ? 0 : nearLandMarks.size(); j<js;j++) {
						Triple<ShapeFileBean, Double, Double> lm = nearLandMarks.get(j);
						double dist = lm.second;
						int distMtr = (int) (Math.round(dist*1000));
						if (distMtr > ce.itemDistMtr)
							continue;
						if (!Misc.isUndef(ce.itemType)) {
							if (lm.first.getSubType() == ce.itemType) {
								matchingBlockIndex = idx;
								break;
							}
						}
						else if (ce.itemPattern != null) {
							String n= lm.first.getName().toUpperCase();
							if (n.startsWith(ce.itemPattern)) {
								matchingBlockIndex = idx;
								break;
							}
						}
						else if (!Misc.isUndef(ce.itemId)) {
							if (lm.first.getId() == ce.itemId) {
								matchingBlockIndex = idx;
								break;
							}
						}
					}//for each matching lm found
				}//if doing lm
				else {
					for (int j=0,js=regionList == null ? 0 : regionList.size(); j<js;j++) {
						RegionTestHelper reg = regionList.get(j);
						if (!Misc.isUndef(ce.itemType)) {
							if (reg.region.getRegionType() == ce.itemType) {
								matchingBlockIndex = idx;
								break;
							}
						}
						else if (ce.itemPattern != null) {
							String n= reg.region.m_name.toUpperCase();
							if (n.startsWith(ce.itemPattern)) {
								matchingBlockIndex = idx;
								break;
							}
						}
						else if (!Misc.isUndef(ce.itemId)) {
							if (reg.region.id == ce.itemId) {
								matchingBlockIndex = idx;
								break;
							}
						}
					}//for each matching region found
				}//if doing region
				if (matchingBlockIndex >= 0)
					break;
			}//for each block
		}//if there were blocks
		return matchingBlockIndex;
	}
	
	
	private int helperGetCritEvalBlockByLocFor(Connection conn, GpsData startData, String startPosName) {
		int retval = Misc.getUndefInt();
		return retval;
	}
	private void addRule(int id, int ruleId, boolean locOrRegion, int locId, int locDistMtr, int locType, String locNamePattern, int condition, double v1, double v2, int criticality, int priority) {
		if (locOrRegion && Misc.isUndef(locDistMtr))
			locDistMtr = 50;
		CritEval eval = new CritEval(id, ruleId, locOrRegion, locId, locDistMtr, locType, locNamePattern, condition, v1, v2, criticality, priority);
		Pair<ArrayList<Integer>, Integer> byRuleIdBlockListMaxDistEntry = byCritRuleBlockListPlusMaxDist.get(ruleId);
		if (byRuleIdBlockListMaxDistEntry == null) {
			byRuleIdBlockListMaxDistEntry = new Pair<ArrayList<Integer>, Integer>(new ArrayList<Integer>(), new Integer(Integer.MIN_VALUE));
			byCritRuleBlockListPlusMaxDist.put(ruleId, byRuleIdBlockListMaxDistEntry);
		}
		if (eval.hasLocOrRegion()) {
			int matchPos = -1;
			if (locOrRegion && byRuleIdBlockListMaxDistEntry.second < locDistMtr)
				byRuleIdBlockListMaxDistEntry.second = locDistMtr;
			for (int i=0,is=ruleCritEvalsValidLocEval.size(); i<is; i++) {
				ArrayList<CritEval> celist = ruleCritEvalsValidLocEval.get(i);
				CritEval ce = celist == null ||celist.size() == 0 ? null : celist.get(0);
				if (ce == null)
					continue;
				if (ce.isLocOrRegion() == eval.isLocOrRegion() && ce.getItemId() == eval.getItemId() && ce.getItemType() == eval.getItemType() && ((ce.getItemPattern() == null && eval.getItemPattern() == null) || (ce.getItemPattern() != null && ce.getItemPattern().equals(eval.getItemPattern())))) {
					matchPos = i;
					break;
				}
			}
			ArrayList<CritEval> addTo = null;
			if (matchPos < 0) {
				addTo = new ArrayList<CritEval>();
				ruleCritEvalsValidLocEval.add(addTo);
				matchPos = ruleCritEvalsValidLocEval.size()-1;
				byRuleIdBlockListMaxDistEntry.first.add(matchPos);
			}
			else {
				addTo = ruleCritEvalsValidLocEval.get(matchPos);
			}
			addTo.add(eval);
			this.byCritEvalIdValidLoc.put(eval.getSpecId(), new MiscInner.Pair(matchPos, addTo.size()-1));
		}//if had valid tagInfo
		else {
			int matchPos = -1;
			for (int i=0,is=this.ruleCritEvalsNoLocEval.size(); i<is; i++) {
				ArrayList<CritEval> celist = ruleCritEvalsNoLocEval.get(i);
				CritEval ce = celist == null ||celist.size() == 0 ? null : celist.get(0);
				if (ce == null)
					continue;
				if (ce.getRuleId() == eval.getRuleId()) {
					matchPos = i;
					break;
				}
			}
			ArrayList<CritEval> addTo = null;
			if (matchPos < 0) {
				addTo = new ArrayList<CritEval>();
				ruleCritEvalsNoLocEval.add(addTo);
				matchPos = ruleCritEvalsNoLocEval.size()-1;
			}
			else {
				addTo = ruleCritEvalsNoLocEval.get(matchPos);
			}
			addTo.add(eval);
			this.byCritRuleIdNoLoc.put(eval.getRuleId(), new MiscInner.Pair(matchPos, addTo.size()-1));
		}
	}
	
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public CritSpec getTatDur() {
		return tatDur;
	}
	public void setTatDur(double val, int priority) {
		this.tatDur = new CritSpec(val, priority);
	}
	public void setTatDur(CritSpec tatDur) {
		this.tatDur = tatDur;
	}
	public CritSpec getGpsNonTrackDur() {
		return gpsNonTrackDur;
	}
	public void setGpsNonTrackDur(double val, int priority) {
		this.gpsNonTrackDur = new CritSpec(val, priority);
	}
	public void setGpsNonTrackDur(CritSpec gpsNonTrackDur) {
		this.gpsNonTrackDur = gpsNonTrackDur;
	}
	public CritSpec getGrossDiff() {
		return grossDiff;
	}
	public void setGrossDiff(double val, int priority) {
		this.grossDiff = new CritSpec(val, priority);
	}
	public void setGrossDiff(CritSpec grossDiff) {
		this.grossDiff = grossDiff;
	}
	public CritSpec getEvent() {
		return event;
	}
	public void setEvent(double val, int priority) {
		this.event = new CritSpec(val, priority);
	}
	public void setEvent(CritSpec event) {
		this.event = event;
	}
	public CritSpec getTarpoline() {
		return tarpoline;
	}
	public void setTarpoline(double val, int priority) {
		this.tarpoline = new CritSpec(val, priority);
	}
	public void setTarpoline(CritSpec tarpoline) {
		this.tarpoline = tarpoline;
	}
	public CritSpec getSeal() {
		return seal;
	}
	public void setSeal(double val, int priority) {
		this.seal = new CritSpec(val, priority);
	}
	public void setSeal(CritSpec seal) {
		this.seal = seal;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public ArrayList<ArrayList<CritEval>> getRuleCritEvalsValidLocEval() {
		return ruleCritEvalsValidLocEval;
	}


	public void setRuleCritEvalsValidLocEval(
			ArrayList<ArrayList<CritEval>> ruleCritEvalsValidLocEval) {
		this.ruleCritEvalsValidLocEval = ruleCritEvalsValidLocEval;
	}


	public ArrayList<ArrayList<CritEval>> getRuleCritEvalsNoLocEval() {
		return ruleCritEvalsNoLocEval;
	}

	public void setRuleCritEvalsNoLocEval(
			ArrayList<ArrayList<CritEval>> ruleCritEvalsNoLocEval) {
		this.ruleCritEvalsNoLocEval = ruleCritEvalsNoLocEval;
	}
	
	public static CriticalRuleInfo getCriticalRuleInfo(Connection conn, int vehicleId, boolean topLevelOnly) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		CriticalRuleInfo retval = new CriticalRuleInfo();
		try {
			String q = "select qc_rules_details.id from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) join qc_rules_details on (qc_rules_details.port_node_id = anc.id and qc_rules_details.status in (1)) where vehicle.id = ? order by anc.lhs_number desc limit 1";
			ps = conn.prepareStatement(q);
			ps.setInt(1, vehicleId);
			rs = ps.executeQuery();
			int qcid = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			StringBuilder sb = new StringBuilder();
			sb.append("select ").append(CriticalRuleInfo.G_QCR_HEADER_SEL);
			if (!topLevelOnly)
				sb.append(",").append(CriticalRuleInfo.G_QCR_DETAILED_SEL);
			 sb.append(" from qc_rules_details qcr ");
			 if (!topLevelOnly)
				 sb.append(" left outer join qc_rule_specific det on (qcr.id = det.qc_rule_id) ");
			sb.append(" where qcr.status in (1) ");
			sb.append("and qcr.id = ?");
			if (!topLevelOnly)
				sb.append(" order by qcr.id, det.reporting_priority, det.id ");
			ps = conn.prepareStatement(sb.toString());
			ps.setInt(1, qcid);
			rs = ps.executeQuery();
			boolean rsExhausted = !rs.next();
			rsExhausted = retval.readRuleFromRSAndGetRsExhausted(rs, rsExhausted, !topLevelOnly);
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			if (topLevelOnly) {
				retval.calcRulesUsed(conn);
			}
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
	
	public boolean readRuleFromRSAndGetRsExhausted(ResultSet rs, boolean rsExhausted, boolean readDetailed) throws Exception {
		if (rsExhausted)
			return rsExhausted;
		rsExhausted = false;
		do  {
			int qcrId = Misc.getRsetInt(rs, "qcr_id"); 
			if (Misc.isUndef(id)) {
				int portNodeId = Misc.getRsetInt(rs, "port_node_id");
				this.id = qcrId;
				this.portNodeId = portNodeId;
				this.setTatDur(Misc.getRsetDouble(rs, "tat_time_val"), Misc.getRsetInt(rs, "tat_time_priority"));
				this.setGpsNonTrackDur(Misc.getRsetDouble(rs, "gps_non_track_val"), Misc.getRsetInt(rs, "gps_non_track_priority"));
				this.setGrossDiff(Misc.getRsetDouble(rs, "gross_difference_val"), Misc.getRsetInt(rs, "gross_difference_priority"));
				this.setEvent(Misc.getRsetDouble(rs, "rating_exceeds_val"), Misc.getRsetInt(rs, "rating_exceeds_priority"));
				this.setTatDur(Misc.getRsetDouble(rs, "tat_time_val"), Misc.getRsetInt(rs, "tat_time_priority"));
				this.setTarpoline(Misc.getRsetDouble(rs, "tarpoline_val"), Misc.getRsetInt(rs, "tarpoline_priority"));
				this.setSeal(Misc.getRsetDouble(rs, "seal_val"), Misc.getRsetInt(rs, "seal_priority"));
				this.setNonTrackAllowedDistKM(Misc.getRsetDouble(rs, "non_track_okay_till_distkm"));
				this.setNonTrackAllowedAvgSpeed(Misc.getRsetDouble(rs, "non_track_okay_above_avgspeed"));
				this.setNonTrackAllowedJumpSpeed(Misc.getRsetDouble(rs, "non_track_jumpspeed"));
			}
			else if (this.getId() != qcrId) {//going to read next
				return rsExhausted;
			}
			if (readDetailed) {
				int detId = Misc.getRsetInt(rs, "det_id");
				int ruleId = Misc.getRsetInt(rs, "ee_rule_id");
				int locId = Misc.getRsetInt(rs, "location_id");
				int locMtr = Misc.getRsetInt(rs, "location_distmtr");
				int locType = Misc.getRsetInt(rs, "location_type");
				String locPattern = Misc.getRsetString(rs, "location_name_pattern", null);
				if (locPattern != null) {
					locPattern = locPattern.trim();
					if (locPattern.length() == 0)
						locPattern = null;
				}
				boolean doingLoc = !Misc.isUndef(locId) || !Misc.isUndef(locType) || locPattern != null;
				if (!doingLoc) {
					locId = Misc.getRsetInt(rs, "region_id");
					locType = Misc.getRsetInt(rs, "region_type");
					locPattern = Misc.getRsetString(rs, "region_name_pattern", null);
					if (locPattern != null) {
						locPattern = locPattern.trim();
						if (locPattern.length() == 0)
							locPattern = null;
					}
				}
				int cond = Misc.getRsetInt(rs, "duration_condition");
				double v1 = Misc.getRsetDouble(rs, "v1");
				double v2 = Misc.getRsetDouble(rs, "v2");
				int crit = Misc.getRsetInt(rs,"criticality");
				int priority = Misc.getRsetInt(rs, "reporting_priority");
				this.addRule(detId, ruleId, doingLoc, locId, locMtr, locType, locPattern, cond, v1, v2, crit, priority);
			}
		}	
		while (rs.next()); //in normal course if exiting then rs is exhausted
		return true;
	}
	public static String G_QCR_HEADER_SEL = "qcr.id qcr_id, qcr.port_node_id, qcr.tat_time_val, qcr.tat_time_priority, qcr.gps_non_track_val, qcr.gps_non_track_priority "+
    ",qcr.gross_difference_val, qcr.gross_difference_priority, qcr.rating_exceeds_val, qcr.rating_exceeds_priority,qcr.tarpoline_val,qcr.tarpoline_priority,qcr.seal_val,qcr.seal_priority "+
    ",qcr.non_track_okay_till_distkm,qcr.non_track_okay_above_avgspeed,qcr.non_track_jumpspeed "
	;
	public static String G_QCR_DETAILED_SEL = "det.id det_id, det.ee_rule_id, det.location_id, det.location_distmtr, det.location_type, det.location_name_pattern "+
    ", det.region_id, det.region_type, det.region_name_pattern "+
    ",det.duration_condition, det.v1, det.v2, det.criticality,det.reporting_priority "
    ;
	public static String G_GET_RULEID = "select distinct ee_rule_id from qc_rule_specific where qc_rule_id=?";
	public double getNonTrackAllowedDistKM() {
		return nonTrackAllowedDistKM;
	}
	public void setNonTrackAllowedDistKM(double nonTrackAllowedDistKM) {
		this.nonTrackAllowedDistKM = nonTrackAllowedDistKM;
	}
	public double getNonTrackAllowedAvgSpeed() {
		return nonTrackAllowedAvgSpeed;
	}
	public void setNonTrackAllowedAvgSpeed(double nonTrackAllowedAvgSpeed) {
		this.nonTrackAllowedAvgSpeed = nonTrackAllowedAvgSpeed;
	}
	public double getNonTrackAllowedJumpSpeed() {
		return nonTrackAllowedJumpSpeed;
	}
	public void setNonTrackAllowedJumpSpeed(double nonTrackAllowedJumpSpeed) {
		this.nonTrackAllowedJumpSpeed = nonTrackAllowedJumpSpeed;
	}
}

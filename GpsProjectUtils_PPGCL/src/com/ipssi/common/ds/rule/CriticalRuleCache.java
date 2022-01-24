package com.ipssi.common.ds.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.ipssi.common.ds.rule.CriticalRuleInfo.CritSpec;
import com.ipssi.common.ds.rule.CriticalRuleInfo.CritEval;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.GpsData;

public class CriticalRuleCache {
	private static ReentrantReadWriteLock gLockForUpd = new ReentrantReadWriteLock();
	private static HashMap<Integer, CriticalRuleInfo> gAllCriticalRules = null;
	private static HashMap<Integer, ArrayList<Integer>> gAllCriticalRulesByPort = null;
	public static Pair<Integer, CritEval> getCritEvalForEE(Connection conn,  int ruleId, double lon, double lat, long startTime, long endTime, int prevCritEvalId, int portNodeId, CacheTrack.VehicleSetup.DistCalcControl distCalcControl, Cache cache, int vehicleId) {
		try {
			CriticalRuleInfo ruleCache = getCritRuleCacheToApply(conn, portNodeId, cache);
			if (ruleCache != null) {
				return ruleCache.getCritEvalForEE(conn, ruleId, lon, lat,startTime, endTime, prevCritEvalId, portNodeId, distCalcControl, vehicleId);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new Pair<Integer, CritEval>(Misc.getUndefInt(), null); 
	}
	public static CriticalRuleInfo getCritRuleCacheToApply(Connection conn, int ownerOrg, Cache cache) throws Exception {
		CriticalRuleInfo retval = null;
		try {
			loadCritRule(conn, null, false);
			gLockForUpd.readLock().lock();
			for (MiscInner.PortInfo curr = cache.getPortInfo(ownerOrg, conn); curr != null; curr = curr.m_parent) {
				ArrayList<Integer> byPort = gAllCriticalRulesByPort.get(curr.m_id);
				for (int t1=0,t1s=byPort == null ? 0 : byPort.size(); t1<t1s; t1++) {
					retval = gAllCriticalRules.get(byPort.get(t1));
					if (retval != null)
						return retval;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			gLockForUpd.readLock().unlock();
		}
		return null;
	}
	
	public static void loadCritRule(Connection conn, ArrayList<Integer> critRuleIds, boolean clean) {
		try {
			gLockForUpd.readLock().lock();
			if (gAllCriticalRules != null && !clean && (critRuleIds == null || critRuleIds.size() == 0))
				return;	
		}
		catch (Exception e) {
			
		}
		finally {
			gLockForUpd.readLock().unlock();
		}
		ResultSet rs = null;
		PreparedStatement ps  =null;

		try {
			gLockForUpd.writeLock().lock();
			if (clean) {
				gAllCriticalRules = null;
				gAllCriticalRulesByPort = null;
				critRuleIds = null;
			}
			if (gAllCriticalRules == null) {
				gAllCriticalRules = new HashMap<Integer, CriticalRuleInfo>();
				gAllCriticalRulesByPort = new HashMap<Integer, ArrayList<Integer>>();
			}
			if (critRuleIds != null && critRuleIds.size() != 0) {//cleanup old entries
				for (Integer iv:critRuleIds) {
					CriticalRuleInfo cr = gAllCriticalRules.get(iv);
					if (cr != null) {
						gAllCriticalRules.remove(iv);
						ArrayList<Integer> byPortList = gAllCriticalRulesByPort.get(cr.getPortNodeId());
						for (int t1=0,t1s=byPortList == null ? 0 : byPortList.size(); t1<t1s;t1++) {//clean up old entries in by port
							if (byPortList.get(t1).equals(iv)) {
								byPortList.remove(t1);
								break;
							}
						}
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("select ").append(CriticalRuleInfo.G_QCR_HEADER_SEL).append(",").append(CriticalRuleInfo.G_QCR_DETAILED_SEL);
			 sb.append(" from qc_rules_details qcr left outer join qc_rule_specific det on (qcr.id = det.qc_rule_id) ");
			sb.append(" where qcr.status in (1) ");
			if (critRuleIds != null && critRuleIds.size() > 0) {
				sb.append(" and qcr.id in (");
				Misc.convertInListToStr(critRuleIds, sb);
				sb.append(") ");
			}
			sb.append(" order by qcr.id, det.reporting_priority, det.id ");
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			boolean rsExhausted = !rs.next();
			while (!rsExhausted) {
				CriticalRuleInfo currQCRule = new CriticalRuleInfo();
				rsExhausted = currQCRule.readRuleFromRSAndGetRsExhausted(rs, rsExhausted, true);
				gAllCriticalRules.put(currQCRule.getId(), currQCRule);
				ArrayList<Integer> byPort = gAllCriticalRulesByPort.get(currQCRule.getPortNodeId());
				if (byPort == null) {
					byPort = new ArrayList<Integer>();
					gAllCriticalRulesByPort.put(currQCRule.getPortNodeId(), byPort);
				}
				byPort.add(currQCRule.getId());
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			
		}
		finally {
			gLockForUpd.writeLock().unlock();
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);

		}
	}

}

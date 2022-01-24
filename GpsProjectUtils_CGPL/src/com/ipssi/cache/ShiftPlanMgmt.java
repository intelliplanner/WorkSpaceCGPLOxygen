package com.ipssi.cache;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;

public class ShiftPlanMgmt {
	
	private static HashMap<Integer, ShiftPlanCurrent> currentPlanRoutes;
	private static ArrayList<ShiftPlanHistory> historyList;
	private static ReentrantReadWriteLock currentlock;	
	private static ReentrantReadWriteLock historylock;	
	private static HashMap<Integer, MiscInner.PairIntLong> currentAssignment;
	
	static {
		currentPlanRoutes = new HashMap<Integer, ShiftPlanCurrent> ();
		historyList = new ArrayList<ShiftPlanHistory> ();
		currentlock = new ReentrantReadWriteLock(true);
		historylock = new ReentrantReadWriteLock(true);
		currentAssignment = new HashMap<Integer, MiscInner.PairIntLong> ();
	}
	
	private static void readLock(ReentrantReadWriteLock lock, String type) {
		//Logger.log("[DUMPER]["+getId()+"]["+type+"][READ][LOCK]", LEVEL.DEBUG);
		lock.readLock().lock();
	}
	
	private static void readUnlock(ReentrantReadWriteLock lock, String type) {
		//Logger.log("[DUMPER]["+getId()+"][READ][UNLOCK]", LEVEL.DEBUG);
		lock.readLock().unlock();
	}
	
	private static void writeLock(ReentrantReadWriteLock lock, String type) {
		//Logger.log("[DUMPER]["+getId()+"]["+type+"][WRITE][LOCK]", LEVEL.DEBUG);
		lock.writeLock().lock();
	}
	
	private static void writeUnlock(ReentrantReadWriteLock lock, String type) {
		//Logger.log("[DUMPER]["+getId()+"]["+type+"][WRITE][UNLOCK]", LEVEL.DEBUG);
		lock.writeLock().unlock();
	}
	
	public static void setCurrentAssignment (int dumperId, int shovelId, long ts) {
		try {
			MiscInner.PairIntLong pair = null;
			writeLock(currentlock, "CURRENT");
			if (currentAssignment.containsKey(dumperId)) {
				pair = currentAssignment.get(dumperId);
				pair.first = shovelId;
				pair.second = ts;
			} else {
				pair = new MiscInner.PairIntLong(shovelId, ts);
				currentAssignment.put(dumperId, pair);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeUnlock(currentlock, "CURRENT");
		}
	}
	
	public static MiscInner.PairIntLong getLatestAssignment(Connection conn, int dumperId, long ts) {
		//return new MiscInner.PairIntLong(Misc.getUndefInt(), -1);
		MiscInner.PairIntLong pair = null;
		try {			
			readLock(currentlock, "CURRENT");
			if (currentAssignment.containsKey(dumperId)) {
				pair = currentAssignment.get(dumperId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readUnlock(currentlock, "CURRENT");
		}
		return pair;
	}
	public static ArrayList<ShiftPlanCurrent> getCurrentPlan() {
		ArrayList<ShiftPlanCurrent> localList = null;
		try {
			localList = new ArrayList<ShiftPlanCurrent> ();
			readLock(currentlock, "CURRENT");
			Iterator<ShiftPlanCurrent> itr = currentPlanRoutes.values().iterator();
			while (itr.hasNext()) {
				ShiftPlanCurrent currentPlan = itr.next();
				localList.add(currentPlan);
			}						
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readUnlock(currentlock, "CURRENT");
		}
		return localList;
	}

	public static ShiftPlanCurrent setCurrentPlan(int shiftTargetId, int routeId, long timestamp, int loadSiteId, int unloadSiteId) {
		try {
			ShiftPlanCurrent currentPlan = null;
			if (currentPlanRoutes.containsKey(routeId)) {
				writeLock(currentlock, "CURRENT");
				currentPlan = currentPlanRoutes.get(routeId);
			}  else {
				currentPlan = new ShiftPlanCurrent();  
				writeLock(currentlock, "CURRENT");
				currentPlan.setShiftTargetId(shiftTargetId);
				currentPlan.setRouteId(routeId);		
				currentPlan.setTimestamp(timestamp);
				currentPlan.setLoadSiteId(loadSiteId);
				currentPlan.setUnloadSiteId(unloadSiteId);
				currentPlanRoutes.put(routeId, currentPlan);
			}
			return currentPlan;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeUnlock(currentlock, "CURRENT");
		}
		return null;
	}
	
	public static void addShovelToCurrentPlan(ShiftPlanCurrent currentPlan, int shovelId) {
		try {
			writeLock(currentlock, "CURRENT");
			currentPlan.addToShovelIdList(shovelId);			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeUnlock(currentlock, "CURRENT");
		}
	}
	
	/*public static void addToCurrentList (ShiftPlanCurrent shiftPlanCurrent) {
		try {
			writeLock(currentlock, "HISTORY");
			currentPlanRoutes.add(shiftPlanCurrent);			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeUnlock(currentlock, "HISTORY");
		}
	}*/
		
	public static void addToHistoryList (ShiftPlanHistory shiftPlan) {
		try {
			writeLock(historylock, "HISTORY");
			historyList.add(shiftPlan);			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeUnlock(historylock, "HISTORY");
		}
	}
	
	private static Comparator<ShiftPlanHistory> historyComparator = new Comparator<ShiftPlanHistory> () {
		public int compare(ShiftPlanHistory arg0, ShiftPlanHistory arg1) {
			int returnVal = 0;			
			if (arg0.getTimestamp() == arg1.getTimestamp()) {
				returnVal = 0;
			} else if (arg0.getTimestamp() < arg1.getTimestamp()) {		
				returnVal = -1; //ascending
				//returnVal = 1;//descending
			} else if (arg1.getTimestamp() < arg0.getTimestamp()) {		
				returnVal = 1;//ascending
				//returnVal = -1; //descending
			} 			
			return returnVal;
		}		
	};
	
	public static ShiftPlanHistory getShiftPlan(long ts) {
		try {
			if (historyList.size() <= 0) {
				loadHistory();
			}
			ShiftPlanHistory compareTo = new ShiftPlanHistory();
			compareTo.setTimestamp(ts);
			int matchPos = Collections.binarySearch(historyList, compareTo, historyComparator);
			if (matchPos < 0) {
				matchPos = Math.abs(matchPos)-2;
				if (matchPos >= historyList.size()){
					matchPos = historyList.size() - 1;
				}
			}
			
			return matchPos < 0 || matchPos >= historyList.size() ? null : historyList.get(matchPos);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void loadHistory () {
		loadHistoryList ();
		loadHistoryLoadSiteList();
	}
	
	private static void loadHistoryList () {
		Connection conn = null;
		boolean destroyIt = false;		
		try {
			String query = "select shift_target_id, live_on from dos_shift_plan_assignments_live_history";
			//Logger.log("[SHIFTPLANMGMT][LOADHISTORY]["+query+"]", LEVE);
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (conn != null) {				
				PreparedStatement ps = conn.prepareStatement(query);					
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {									
					int shiftTargetId = rs.getInt("shift_target_id");		
					long liveon = Misc.sqlToLong(rs.getTimestamp("live_on"));					
					ShiftPlanHistory shiftPlan = new ShiftPlanHistory ();
					shiftPlan.setShiftTargetId(shiftTargetId);
					shiftPlan.setTimestamp(liveon);					
					addToHistoryList(shiftPlan);					
				}					
				rs.close();
				ps.close();
			} else {
				//Logger.log("[DATABASE][GETINTERACTIVEASSIGNMENTTONNAGE]["+shiftTargetId+"]["+route.getId()+"][CONNECTION][NULL]", LEVEL.ERROR);
			}
		}catch (Exception e){
			//Logger.log("[DATABASE][GETINTERACTIVEASSIGNMENTTONNAGE]["+shiftTargetId+"]["+route.getId()+"][CONNECTION][EXCEPTION]", LEVEL.ERROR);
			destroyIt = true;
			e.printStackTrace();
		} finally {
			try {
				if(conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				//Logger.log("[DATABASE][GETINTERACTIVEASSIGNMENTTONNAGE]["+shiftTargetId+"]["+route.getId()+"][CONNECTION][FINALLY][EXCEPTION]", LEVEL.ERROR);
				e.printStackTrace();
			}
		}
	}
	
	private static void loadHistoryLoadSiteList () {
		Connection conn = null;
		boolean destroyIt = false;		
		try {
			writeLock(historylock, "HISTORY");
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			Iterator<ShiftPlanHistory> itr = historyList.iterator();
			while (itr.hasNext()) {
				ShiftPlanHistory history = itr.next();
				String query = "select distinct(inv_pile) from dos_shift_plan_assignments where shift_target_id=? and creation_time=?";
			//	Logger.log("[SHIFTPLANMGMT][LOADHISTORY]["+query+"]", LEVE);
				if (conn != null) {				
					PreparedStatement ps = conn.prepareStatement(query);
					ps.setInt(1, history.getShiftTargetId());
					ps.setTimestamp(2, Misc.longToSqlDate(history.getTimestamp()));
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {			
						int loadSiteId = rs.getInt("inv_pile");								
						history.addToLoadSiteIdList(loadSiteId);
					}					
					rs.close();
					ps.close();
				} else {
					//Logger.log("[DATABASE][GETINTERACTIVEASSIGNMENTTONNAGE]["+shiftTargetId+"]["+route.getId()+"][CONNECTION][NULL]", LEVEL.ERROR);
				}
			}
		}catch (Exception e){
			//Logger.log("[DATABASE][GETINTERACTIVEASSIGNMENTTONNAGE]["+shiftTargetId+"]["+route.getId()+"][CONNECTION][EXCEPTION]", LEVEL.ERROR);
			destroyIt = true;
			e.printStackTrace();
		} finally {
			try {
				writeUnlock(historylock, "HISTORY");
				if(conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				//Logger.log("[DATABASE][GETINTERACTIVEASSIGNMENTTONNAGE]["+shiftTargetId+"]["+route.getId()+"][CONNECTION][FINALLY][EXCEPTION]", LEVEL.ERROR);
				e.printStackTrace();
			}
		}
	}
}

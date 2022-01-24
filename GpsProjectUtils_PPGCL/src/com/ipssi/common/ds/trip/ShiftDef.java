package com.ipssi.common.ds.trip;
//the main func of interest here is getShiftDef() && reloadShiftDef
        
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;


import java.sql.*;

public class ShiftDef implements Comparable {
	public static String GET_ORG_LIST = "select id from port_nodes";
	private static String GET_SHIFT_DEF = "select shift.id, shift.name, shift.port_node_id, shift_timings.valid_start, shift_timings.valid_end, shift_timings.start_hour, shift_timings.start_min, shift_timings.stop_hour, shift_timings.stop_min from shift join shift_timings on (shift_timings.shift_id = shift.id) ";//order by in query
	
	volatile static private HashMap<Integer, ArrayList<ShiftDef>> g_shiftInfos = null;
	public static void reset() {
		g_shiftInfos = null;
	}
	private static void init(Connection conn) throws Exception {
		if (g_shiftInfos != null) 
			return;
		g_shiftInfos = new HashMap<Integer, ArrayList<ShiftDef>>();
		synchronized (ShiftDef.class) {
			HashMap<Integer, ArrayList<ShiftDef>> shiftInfos = g_shiftInfos;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = conn.prepareStatement(GET_ORG_LIST);
				rs = ps.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					Integer idInt = new Integer(id);
					shiftInfos.put(idInt, null);
				}
				rs.close();
				rs = null;
				ps.close();
				ps = null;
				shiftInfos = load(shiftInfos, Misc.getUndefInt(), conn);
				g_shiftInfos = shiftInfos;
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			finally {
				try {
					if (rs != null)
						rs.close();
				}
				catch (Exception e1) {
					
				}
				try {
					if (ps != null)
						ps.close();
				}
				catch (Exception e1) {
					
				}
			}
		}
	}
	
	static private Date g_refDate = new Date (0,0,1); 
	//Key: OrgId
	//ArrayList sorted by validstartDate and startTime,
	public boolean isApplicable(Date atTime) { 
	   int mon = atTime.getMonth();
       int day = atTime.getDate();
       int hr = atTime.getHours();
       int min = atTime.getMinutes();
       if (mon == 1 && day == 29)
    	   day = 28;
       Date atDateRe1900 = new Date(0,mon,day);
       int dateNum = Misc.getDaysDiff(atDateRe1900, g_refDate);
       int altDateNum = dateNum+365;
       int minSince0 = hr*60+min;
       int altMinSince0 = minSince0+1440;
       return (
    		   ((dateNum >= validStartDayNum && dateNum < validEndDayNum) || (altDateNum >= validStartDayNum && altDateNum < validEndDayNum))
    		   &&
    		   ((minSince0 >= startMin && minSince0 < endMin) || (altMinSince0 >= startMin && altMinSince0 < endMin))
    		   )
    		   ;
	}
	public static ShiftDef getShiftDef(int orgId, Connection conn, Date atTime) throws Exception {
		Cache cache = Cache.getCacheInstance(conn);
		return getShiftDef(cache.getPortInfo(orgId, conn), conn, atTime);
	}
	
	public static ShiftDef getShiftDef(MiscInner.PortInfo org, Connection conn, Date atTime) throws Exception {
		//first = prevShiftDef, second = currShiftDef, third = nextShiftDef
    	//if shiftDef is found that has valdity over atTime both from validStart/validEnd and startDate/endDate then that is returned
    	//else will return the one that has validity and closest to atTime
    	//else will return the one that is closest in validity and then closest in atTime.
       ArrayList<ShiftDef> shiftDefList = getShiftDefList(org, conn);
       ShiftDef currBest = null;
       int currBestValidityGap = 1000;
       int currBestTimeGap = 3000; //24*60 roughly ..
       int mon = atTime.getMonth();
       int day = atTime.getDate();
       int hr = atTime.getHours();
       int min = atTime.getMinutes();
       if (mon == 1 && day == 29)
    	   day = 28;
       Date atDateRe1900 = new Date(0,mon,day);
       int dateNum = Misc.getDaysDiff(atDateRe1900, g_refDate);
       int altDateNum = dateNum+365;
       int minSince0 = hr*60+min;
       int altMinSince0 = minSince0+1440;
       int indexOfBest = -1;
       for (int i=0,is=shiftDefList == null ? 0 : shiftDefList.size(); i<is;i++) {
    	   ShiftDef shift = shiftDefList.get(i);
    	   int validGap = 0;
           if ((dateNum >= shift.validStartDayNum && dateNum < shift.validEndDayNum) || (altDateNum >= shift.validStartDayNum && altDateNum < shift.validEndDayNum)) {
        	    validGap = 0;   
           }
           else {
        	   validGap = Math.abs(dateNum-shift.validMidDayNum);
           }
           int minGap = 0;
           if ((minSince0 >= shift.startMin && minSince0 < shift.endMin) || (altMinSince0 >= shift.startMin && altMinSince0 < shift.endMin))
        	   minGap = 0;
           else
        	   minGap = Math.abs(minSince0 - shift.midMin);
           if (validGap == 0 && minGap == 0) {
        	   indexOfBest = i;
        	   currBest = shift;
        	   break;
           }
           else {
        	   if (validGap == currBestValidityGap) {
        		   if (minGap < currBestTimeGap) {
        			   indexOfBest = i;
        			   currBest  = shift;
        			   currBestValidityGap = validGap;
        			   currBestTimeGap = minGap;
        		   }        		           			   
        	   }
        	   else if (validGap < currBestValidityGap) {
        		   indexOfBest = i;
        		   currBest  = shift;
    			   currBestValidityGap = validGap;
    			   currBestTimeGap = minGap;
        	   }
           }
       }
       //ShiftDef prev = indexOfBest >= 1 && currBest != null ? shiftDefList.get(indexOfBest-1) : null;
       //ShiftDef next = indexOfBest < shiftDefList.size()-1 && currBest != null ? shiftDefList.get(indexOfBest+1) : null;
       //return new Triple<ShiftDef, ShiftDef, ShiftDef>(prev, currBest, next);
       return currBest;
    }
	
	public static void reloadShiftDef(int orgId) throws Exception { //will loadShiftDef again here and will also clear up ShiftPlanInfo.g_fastSchedLookup
		Connection conn = null;
		boolean destroyIt = false; 
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			init(conn);
			ShiftDef.load(g_shiftInfos, orgId, conn);
			Cache cache = Cache.getCacheInstance(conn);
			MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, conn);
			ShiftPlanInfo.clearFastScheduleLookup(conn, portInfo);			
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
			throw e;
		}
		finally {
			if (conn != null) {
				try {
				    DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e2) {
					
				}
			}
		}
		
	}
    
    public int orgId;
    public int shiftId;
    public String shiftName;    
    public int startMin = 0;
    public int endMin = 1441;
    public int midMin = 720;
    public int validStartDayNum = 0;
    public int validEndDayNum = 366;
    public int validMidDayNum = 173;
        
    
    
    private ShiftDef(int orgId, int shiftId, String shiftName, int startH, int startM, int endH, int endM, Date validStartDate, Date validEndDate) { //why private - never need to construct outside
    	this.orgId = orgId;
    	this.shiftId = shiftId;
    	this.shiftName = shiftName;
    	 
    	if (validStartDate != null && validEndDate != null) {
    		validStartDate.setYear(0);
    		validEndDate.setYear(0);
    		if (validStartDate.after(validEndDate) || (validStartDate.equals(validEndDate)))
    			validEndDate.setYear(1);
    		validStartDayNum = Misc.getDaysDiff(validStartDate, g_refDate);
    		validEndDayNum = Misc.getDaysDiff(validEndDate, g_refDate);
    		validMidDayNum = (validStartDayNum+validEndDayNum)/2;    		
    	}
    	
	     if (endH < startH || (endH == startH && endM <= startM)) {
	    	 endH += 24;
	     }
	     startMin = startH * 60 + startM;
	     endMin = endH * 60 + endM;
	     midMin = (startMin+endMin)/2;
    }
    
    
    public int compareTo(Object rhsObj) {
        ShiftDef rhs = (ShiftDef) rhsObj;
        if (rhs == null) {
        	return +1;
        }
        int compVal = validStartDayNum - rhs.validStartDayNum;
        if (compVal == 0) {
        	compVal = startMin - rhs.startMin;
        }
        
        return compVal;           
    }
    
    private static ArrayList<ShiftDef> getShiftDefList(MiscInner.PortInfo org, Connection conn) throws Exception {
		ArrayList<ShiftDef> retval = null;
		init(conn);
		HashMap<Integer, ArrayList<ShiftDef>> shiftInfos = g_shiftInfos;
		for(;org != null; org = org.m_parent) {
			retval = shiftInfos.get(org.m_id);
			if (retval != null)
				return retval;
		}
		return retval;
    }
    
    private static HashMap<Integer, ArrayList<ShiftDef>> load(HashMap<Integer, ArrayList<ShiftDef>> shiftInfos, int orgId, Connection conn) throws Exception {
       try {
    	  StringBuilder buf = new StringBuilder(GET_SHIFT_DEF);
    	  if (!Misc.isUndef(orgId))
    		  buf.append(" and port_node_id=? ");
    	  buf.append(" order by port_node_id, valid_start, start_hour, start_min ");
    	  PreparedStatement ps = conn.prepareStatement(buf.toString());
    	  if (!Misc.isUndef(orgId))
    		  ps.setInt(1, orgId);
    	  ResultSet rs = ps.executeQuery();
    	  int lastPortNode = Misc.getUndefInt();
    	  ArrayList<ShiftDef> lastEntry = null;
    	   
    	  while (rs.next()) {
    		  int id = rs.getInt(1);
    		  String name = rs.getString(2);
    		  int ofOrgId = rs.getInt(3);
    		  Date validStart = rs.getDate(4);
    		  Date validEnd = rs.getDate(5);
    		  int startHr = rs.getInt(6);
    		  int startMin = rs.getInt(7);
    		  int endHr = rs.getInt(8);
    		  int endMin = rs.getInt(9);
    		  Date startDate = new Date(0,0,1,startHr,startMin,0);
    		  Date endDate = new Date(0,0,1,endHr,endMin,0);
    		  ShiftDef def = new ShiftDef(ofOrgId, id, name, startHr, startMin, endHr, endMin, validStart, validEnd);
    		  if (ofOrgId != lastPortNode) {
    			  
    			  if (lastEntry != null) {
    				  Integer lastPortNodeInt = new Integer(lastPortNode);
    				  if (!shiftInfos.containsKey(lastPortNodeInt)) {
    					  shiftInfos.put(lastPortNodeInt, lastEntry);
    					  g_shiftInfos = shiftInfos;
    				  }
    				  else
    					  shiftInfos.put(new Integer(lastPortNode), lastEntry);
    			  }
    			  lastEntry = new ArrayList<ShiftDef>();
    		  }
    		  helpAdd(lastEntry, def);
    		  lastPortNode = ofOrgId;
    	  }
    	  if (lastEntry != null) {
    		  Integer lastPortNodeInt = new Integer(lastPortNode);
    		  if (!shiftInfos.containsKey(lastPortNodeInt)) {
				  shiftInfos.put(lastPortNodeInt, lastEntry);
				  g_shiftInfos = shiftInfos;
			  }
			  else
				  shiftInfos.put(new Integer(lastPortNode), lastEntry);
		  }
    	  rs.close();
    	  ps.close();
    	  return shiftInfos;
       }
       catch (Exception e) {
    	   e.printStackTrace();
    	   throw e;
       }    
    }
    
    private static void helpAdd(ArrayList<ShiftDef> addTo, ShiftDef def) { 
    	if (def != null) {
	    	int i=0,is=addTo.size();
	    	for (;i<is;i++) {
	    		ShiftDef item = addTo.get(i);
	    		int compval = item.compareTo(def);
	    		if (compval == 0) {
	    			addTo.set(i, def);
	    			break;
	    		}
	    		else if (compval > 0) {
	    			addTo.add(i, def);
	    			break;
	    		}
	    	}
	    	if (i == is)
	    		addTo.add(def);
    	}
    }    
    
}

package com.ipssi.shift;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.TimePeriodHelper;

public class ShiftInformation {

	private static HashMap<Integer, ArrayList<ShiftBean>> shiftMap = new HashMap<Integer, ArrayList<ShiftBean>>();
	public static void reset() {
		shiftMap.clear();
	}
	public static ArrayList<ShiftBean> getShiftForPort(int portNodeId, Connection conn) {
		ArrayList<ShiftBean> retval = new ArrayList<ShiftBean>();
		try {
			if (shiftMap.isEmpty()) {

				ShiftDao.fetchShift(conn);

			}
			Cache cache = Cache.getCacheInstance(conn);
			for (MiscInner.PortInfo portInfo = cache.getPortInfo(portNodeId, conn); portInfo != null; portInfo = portInfo.m_parent) {
				int pid = portInfo.m_id;
				retval = shiftMap.get(pid);
				if (retval != null) {
					//find shifts that has validity around getValidFor if getValidFor is not null .. else get all
					break;//found the nearest
				}
			}
		} catch (GenericException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
	public static void makeDirty() {
		shiftMap.clear();
	}

	public static ArrayList<ShiftBean> addShiftForPort(int portNodeId) throws Exception {
		ArrayList<ShiftBean> retval = shiftMap.get(portNodeId);
		if (retval == null) {
			retval = new ArrayList<ShiftBean>();
			shiftMap.put(portNodeId, retval);
		}
		return retval;
	}

	public static ShiftBean getShiftById(int portNodeId ,int shiftId, Connection conn) throws Exception {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		ShiftBean retval = null;
		ShiftBean temp = null;
		Iterator<ShiftBean> it =null;
		if (lookInThis != null) {
			for(it=lookInThis.iterator();it.hasNext();)
			{
				temp = it.next();
				if(temp.getId() == shiftId){
					retval = temp;
					break;
				}
			}

		}
		return retval;
	}

	/*public static int getFirstShiftId(int portNodeId , Connection conn, int granParam) throws Exception {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		int retval = Misc.getUndefInt();
		if (lookInThis != null) {
			if (granParam != Misc.SCOPE_SHIFT)
				retval = lookInThis.get(0).getId();
			else {//find shift that encompasses the current time
				java.util.Date dt = new java.util.Date();
				int hr = dt.getHours();
				int min = dt.getMinutes();
				for (ShiftBean shift:lookInThis) {
					if ((shift.getStartHour() < hr || (shift.getStartHour() == hr && shift.getStartMin() <= min)) &&
						(shift.getStopHour() > hr || (shift.getStopHour() == hr && shift.getStopMin() > min))) {
						retval = shift.getId();
						break;
					}
				}
				if (Misc.isUndef(retval) && lookInThis.size() > 0) {
					retval = lookInThis.get(lookInThis.size()-1).getId();
				}
			}
		}
		return retval;
	}*/
	public static int getFirstShiftId(int portNodeId , Connection conn, int granParam) throws Exception {
		return getFirstShiftId(portNodeId, conn, granParam,new Date());
	}
	public static int getFirstShiftIdInDay(int portNodeId , Connection conn) throws Exception {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		int retval = Misc.getUndefInt();
		if (lookInThis != null) {
			ShiftBean first = null;
			for (ShiftBean shift:lookInThis) {
				if (first == null || first.getStartHour() > shift.getStartHour()) 
					first = shift;
			}
			if (first != null)
				retval = first.getId();
			if (Misc.isUndef(retval) && lookInThis.size() > 0) {
				retval = lookInThis.get(lookInThis.size()-1).getId();
			}
		}
		return retval;
	}
	
	public static int getFirstShiftId(int portNodeId , Connection conn, int granParam,Date dt) throws Exception {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		int retval = Misc.getUndefInt();
		if (lookInThis != null) {
			if(dt == null)
				dt = new java.util.Date();
				double ts = dt.getHours() + ((dt.getMinutes()*1.0)/60);			
			for (ShiftBean shift:lookInThis) {
				double start = shift.getStartHour() + ((shift.getStartMin()*1.0)/60);	
				double end = shift.getStopHour() + ((shift.getStopMin()*1.0)/60);
				boolean startEQts = Misc.isEqual(start, ts);
				boolean endEQts = Misc.isEqual(end, ts);
				boolean startLTts = start < ts &&  !startEQts;
				boolean endLTts =  end < ts && !endEQts;
				boolean matching = false;
				if (start > end) {
					matching = ((startLTts || startEQts) && (ts <= 24 || Misc.isEqual(ts, 24)))
					
					|| 
					((0 <= ts || Misc.isEqual(0, ts)) && (!endLTts && !endEQts))
							;
				}
				else {
					matching = (startLTts || startEQts) && (!endLTts && !endEQts);
				}
				if (matching) {
					retval = shift.getId();
					break;
				}
			}
			if (Misc.isUndef(retval) && lookInThis.size() > 0) {
				retval = lookInThis.get(lookInThis.size()-1).getId();
			}
		}
		return retval;
	}
	public static ShiftBean getShift(Connection conn,int portNodeId,Date dt) {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		int index = Misc.getUndefInt();
		ShiftBean retval = null;
		try{
		if (lookInThis != null) {
			int size = lookInThis.size();
			if(dt == null)
				dt = new java.util.Date();
			double ts = dt.getHours() + ((dt.getMinutes()*1.0)/60);			
			for (int i=0;i<size;i++) {
				ShiftBean shift = lookInThis.get(i);
				double start = shift.getStartHour() + ((shift.getStartMin()*1.0)/60);	
				double end = shift.getStopHour() + ((shift.getStopMin()*1.0)/60);
				if ((start <= ts && ts <= end) || ((start <= ts && ts >= end) && (start >= end ))|| ((start > ts && ts <= end) && (start >= end ))) {
					return shift;
				}
			}
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static ShiftBean getRelativeShiftId(int portNodeId , Connection conn, int granParam,Date dt,int relativeShift) throws Exception {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		int index = Misc.getUndefInt();
		ShiftBean retval = null;
		if (lookInThis != null) {
			int size = lookInThis.size();
			if(dt == null)
				dt = new java.util.Date();
				double ts = dt.getHours() + ((dt.getMinutes()*1.0)/60);			
			for (int i=0;i<size;i++) {
				ShiftBean shift = lookInThis.get(i);
				double start = shift.getStartHour() + ((shift.getStartMin()*1.0)/60);	
				double end = shift.getStopHour() + ((shift.getStopMin()*1.0)/60);
				if ((start <= ts && ts <= end) || ((start <= ts && ts >= end) && (start >= end ))|| ((start > ts && ts <= end) && (start >= end ))) {
					index = i;
					break;
				}
			}
			if(!Misc.isUndef(index)){
				index = ((relativeShift%size) + index + size)%size;
			}
			retval = lookInThis.get(index);
		}
		return retval;
	}
	/*public static int getRelativeShift(int portNodeId , Connection conn,java.util.Date dt,int relativeShift) throws Exception {
		ArrayList<ShiftBean> lookInThis = getShiftForPort(portNodeId, conn);
		int retval = Misc.getUndefInt();
		if (lookInThis != null) {
			if(dt == null)
				dt = new java.util.Date();
				double ts = dt.getHours() + ((dt.getMinutes()*1.0)/60);			
			for (ShiftBean shift:lookInThis) {
				double start = shift.getStartHour() + ((shift.getStartMin()*1.0)/60);	
				double end = shift.getStopHour() + ((shift.getStopMin()*1.0)/60);
				if ((start <= ts && ts <= end) || ((start <= ts && ts >= end) && (start >= end ))|| ((start > ts && ts <= end) && (start >= end ))) {
					retval = shift.getId();
					break;
				}
			}
			if (Misc.isUndef(retval) && lookInThis.size() > 0) {
				retval = lookInThis.get(lookInThis.size()-1).getId();
			}
		}
		return retval;
	}*/
	public static void main(String arg[]){
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM HH:mm");
			java.util.Date dt = sdf.parse("2013-12-12 00:59");
			for(int i=0;i<24;i++){
				TimePeriodHelper.addScopedDur(dt, Misc.SCOPE_HOUR, 60);
				ShiftBean shift = ShiftInformation.getRelativeShiftId(57, conn, 6,dt,2);
				if(shift != null)
				System.out.println(dt+":"+shift.getShiftName()+":"+shift.getDur());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}

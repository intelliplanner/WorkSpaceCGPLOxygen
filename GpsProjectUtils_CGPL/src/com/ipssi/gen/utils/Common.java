package com.ipssi.gen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

import com.ipssi.reporting.common.db.DBQueries;

public class Common {

	public static final int G_DEFAULT_LOCALEID = MiscInner.PortInfo.G_DEFAULT_LOCALEID;
	public static String G_DEFAULT_DATE_FORMAT = Misc.G_DEFAULT_DATE_FORMAT;
	public static java.text.SimpleDateFormat m_dateFormatter = new java.text.SimpleDateFormat(Common.G_DEFAULT_DATE_FORMAT);
	// New
	//
	private Common() {
	}

	public static final int UNDEF_VALUE = -1111111;
	public static final double UNDEF_FLOAT_VALUE = -1e12f;
	public static final double UNDEF_FLOAT_VALUE_CMP = -1e11f;// UNDEF_VALUE *

	// 100000;

	public static boolean isUndef(long val) {
		return val == UNDEF_VALUE;
	}

	public static boolean isUndef(double val) {
		return val <= UNDEF_FLOAT_VALUE_CMP;
	}

	public static boolean isExplicitAny(int val) {
		return val == (UNDEF_VALUE + 1);
	}

	public static int getUndefInt() {
		return (int) UNDEF_VALUE;
	}

	public static int getExplicitAny() {
		return (int) UNDEF_VALUE + 1;
	}

	public static double getUndefDouble() {
		return (double) UNDEF_FLOAT_VALUE;
	}

	public static boolean isNull(Object object) {
		return (object == null);
	}

	public static double getParamAsDouble(String str, double undefVal) {
		if (str == null)
			return (double) undefVal;
		try {
			str = str.trim();
			double f = Double.parseDouble(str);
			if (Common.isUndef(f))
				return undefVal;
			return f;
		} catch (Exception e) {
			return (double) undefVal;
		}
	}

	public static double getParamAsDouble(String str) {
		return getParamAsDouble(str, getUndefDouble());
	}

	public static long getParamAsLong(String str, long undefVal) {
		if (str == null)
			return (long) undefVal;
		try {
			str = str.trim();
			long l = Long.parseLong(str);
			if (Common.isUndef(l))
				return undefVal;
			return l;
		} catch (Exception e) {
			return (long) undefVal;
		}
	}

	public static long getParamAsLong(String str) {
		return getParamAsLong(str, getUndefInt());
	}

	public static int getParamAsInt(String str, int undefVal) {
		if (str == null)
			return undefVal;
		try {
			str = str.trim();
			int i = Integer.parseInt(str);
			if (Common.isUndef(i))
				return undefVal;
			return i;
		} catch (Exception e) {
			return undefVal;
		}
	}

	public static String getParamAsString(String str, String undefVal) {
		if (str == null || str.length() == 0)
			return undefVal;
		return str;
	}

	public static String getParamAsString(String str) {
		return getParamAsString(str, "");
	}

	public static int getParamAsInt(String str) {
		return getParamAsInt(str, getUndefInt());
	}

	public static java.sql.Date getParamAsDate(Element dateElement, String prefix, java.util.Date defaultDate) {
		return getParamAsDate(dateElement.getAttribute(prefix + "day"), dateElement.getAttribute(prefix + "month"), dateElement.getAttribute(prefix + "year"), defaultDate);
	}

	public static java.sql.Date getParamAsDate(String dateStr, java.util.Date defaultDate) {
		return getParamAsDate(dateStr, defaultDate, null);
	}

	public static java.sql.Date getParamAsDate(String dateStr, java.util.Date defaultDate, FmtI.Date formatter) {
		java.sql.Date retval = null;
		if (formatter != null)
			retval = formatter.getDate(dateStr, defaultDate);
		else {
			try {
				java.util.Date dt1 = m_dateFormatter.parse(dateStr);
				if (dt1 != null)
					retval = new java.sql.Date(dt1.getTime());
			}

			catch (Exception e) {
			}
		}
		if (retval == null && defaultDate != null)
			retval = new java.sql.Date(defaultDate.getTime());
		return retval;
	}

	public static java.sql.Date getParamAsDate(String day, String month, String year, java.util.Date defaultDate) {
		java.util.Date date = defaultDate;
		if ((day != null) && (month != null) && (year != null)) {
			try {
				date = new java.util.Date(Integer.parseInt(year) - 1900, Integer.parseInt(month), Integer.parseInt(day));
			} catch (Exception e) {
			}
		}
		if (date != null)
			return new java.sql.Date(date.getTime());
		else
			return null;
	}

	public static Timestamp getParamAsDateFromDatePicker(String text) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		Date date = sdf.parse(text);
		return (new Timestamp(date.getTime()));
	}
	/**
	 * 
	 * @param utilDate
	 * @return
	 */
	public static java.sql.Date utilToSqlDate(java.util.Date utilDate) {

		if (utilDate instanceof java.util.Date) {
			return new java.sql.Date(utilDate.getTime());
		}
		return null;
	}
	public static Triple<Integer, Integer, Date> getShiftAndScheduleInfoIdReplWithTripGet(Connection conn, Date inDate, int vehicleId, int orgId) throws Exception {
	    	int shiftId = Misc.getUndefInt();
	    	int shiftScheduleInfoId = Misc.getUndefInt();
	    	java.sql.Date shiftDate = null;
	    	try {
	    		if (inDate != null) {
	    			shiftDate = new java.sql.Date(inDate.getTime());
	    			
	    			
		    		int hr = inDate.getHours();
		    		int min = inDate.getMinutes();
		    		
		    		PreparedStatement ps = conn.prepareStatement(DBQueries.SHIFT.GET_DEFINED_SHIFT);
		    		ps.setInt(1, orgId);
		    		ps.setDate(2, shiftDate);
		    		ps.setInt(3, hr);
		    		ps.setInt(4, hr);
		    		ps.setInt(5, min);
		    		ps.setInt(6, hr);
		    		ps.setInt(7, min);
		    		ps.setDate(8, shiftDate);
		    		ps.setInt(9, hr);
		    		ps.setInt(10, hr);
		    		ps.setInt(11, min);
		    		ps.setInt(12, hr);
		    		ps.setInt(13, min);	    		
		    		ResultSet rs = ps.executeQuery();
		    		boolean found = false;
		    		if (rs.next()) {
		    			shiftScheduleInfoId = rs.getInt(1);
		    			shiftId = rs.getInt(2);
		    			shiftDate = rs.getDate(5);
		    			found = true;
		    		}
		    		rs.close();
		    		ps.close();
		    		if (!found) {
		    			ps = conn.prepareStatement(DBQueries.SHIFT.GET_REG_SHIFT);
		    			
		    			
			    		ps.setInt(1, orgId);
			    		java.sql.Date inDateRel1900 = new java.sql.Date(inDate.getYear(), inDate.getMonth(), inDate.getDate());
		    			if (inDateRel1900.getMonth() == 1 && inDateRel1900.getDate() == 29)
		    				inDateRel1900.setDate(28);
		    			inDateRel1900.setYear(0);
			    		ps.setDate(2,inDateRel1900);
			    		ps.setDate(3,inDateRel1900);
			    		java.sql.Date inDateRel1900Plus1 = new java.sql.Date(inDateRel1900.getTime());
			    		inDateRel1900Plus1.setYear(1);
			    		ps.setDate(4,inDateRel1900Plus1);
			    		ps.setDate(5,inDateRel1900Plus1);
			    		ps.setInt(6, hr);
			    		ps.setInt(7, hr);
			    		ps.setInt(8, min);
			    		ps.setInt(9, hr);
			    		ps.setInt(10, min);
			    		ps.setInt(11, hr);
			    		ps.setInt(12, hr);
			    		ps.setInt(13, min);
			    		ps.setInt(14, hr);
			    		ps.setInt(15, min);
			    		rs = ps.executeQuery();
			    		if (rs.next()) {
			    			shiftId = rs.getInt(1);
			    			int suggStart = rs.getInt(2);
			    			if (suggStart > hr)
			    				Misc.addDays(shiftDate, -1);
			    		}
			    		rs.close();
			    		ps.close();
		    		}
	    		}
	    		return new Triple<Integer, Integer, Date>(shiftId, shiftScheduleInfoId, shiftDate);
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    		throw e;
	    	}
	    }
	
}

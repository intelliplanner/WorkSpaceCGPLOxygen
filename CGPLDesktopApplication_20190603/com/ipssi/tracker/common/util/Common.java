package com.ipssi.tracker.common.util;

import org.w3c.dom.Element;

import com.ipssi.gen.utils.FmtI;
import com.ipssi.gen.utils.Misc;

public class Common {

	public static final int G_DEFAULT_LOCALEID = 0;
	public static String G_DEFAULT_DATE_FORMAT = Misc.G_DEFAULT_DATE_FORMAT;
    private static java.text.SimpleDateFormat m_dateFormatter = new java.text.SimpleDateFormat(Common.G_DEFAULT_DATE_FORMAT);

	/**
	 * 
	 */
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
				java.util.Date dt1 = null;
				synchronized (m_dateFormatter) {
				   dt1 = m_dateFormatter.parse(dateStr);
				}
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

}

package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.*;

public class FmtI {
    public static class AllFmt {
       public java.text.Format m_formatter = null; //overridden by specific       
    }              
    
    public static class Date extends AllFmt {
    	public Date(Locale locale, boolean doTime) {
            m_formatter = doTime ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale) : DateFormat.getDateInstance(DateFormat.SHORT, locale);
            if (doTime) {
            	String p1 = getPattern();
            	//get the space and replace whatever pattern by HH:mm
            	int sp = p1.indexOf(' ');
            	if (sp != -1) {
            		p1 = p1.substring(0, sp)+" HH:mm:ss";
            		((SimpleDateFormat)m_formatter).applyPattern(p1);
            	}
            }
         }
    	public Date(Locale locale, boolean doTime, boolean doSec) {
            m_formatter = doTime ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale) : DateFormat.getDateInstance(DateFormat.SHORT, locale);
            if (doTime) {
            	String p1 = getPattern();
            	//get the space and replace whatever pattern by HH:mm
            	int sp = p1.indexOf(' ');
            	if (sp != -1) {
            		p1 = p1.substring(0, sp)+" HH:mm"+(doSec ? ":ss":"");
            		((SimpleDateFormat)m_formatter).applyPattern(p1);
            	}
            }
         }
        public Date(Locale locale) {
          this(locale, false);
        }
        public String getPattern() {
           try {
              return ((SimpleDateFormat)m_formatter).toPattern();
           }
           catch (Exception e) {
              return Misc.G_DEFAULT_DATE_FORMAT;
           }
        }
        public String format(java.util.Date dt) {           
           return dt == null ? Misc.emptyString : ((DateFormat) m_formatter).format(dt);
        }
        public java.sql.Date getDate(String dateStr, java.util.Date defaultDate) {           
           java.util.Date dt = null;
           try {
              dt = ((DateFormat)m_formatter).parse(dateStr);
           }
           catch (Exception e) {
//              e.printStackTrace();
//              int dbg = 1;
           }
           
           if (dt == null)
              dt = defaultDate;
           return dt != null ? new java.sql.Date(dt.getTime()) : null;
           
        }        
    }    
    
    public static class Number extends AllFmt  {
        public MiscInner.UnitInfo m_unitInfo = MiscInner.UnitInfo.g_defaultUnit;
        public double unadjustedParseDouble(String str, double undef) { //gets Raw info
           if (str == null || str.length() == 0)
              return undef;
           double retval = undef;
           try {
              java.lang.Number num = ((DecimalFormat)m_formatter).parse(str);
              retval = num.doubleValue();
           }
           catch (Exception e) {
           }
           if (Misc.isUndef(retval))
              retval = undef;
           return retval;
        }
        public int unadjustedParseInt(String str, int undef) {
           if (str == null || str.length() == 0)
              return undef;
           int retval = undef;
           try {
              java.lang.Number num = ((DecimalFormat)m_formatter).parse(str);
              retval = num.intValue();
           }
           catch (Exception e) {
           }
           if (Misc.isUndef(retval))
              retval = undef;           
           return retval;
        }
        public double getDouble(String str, double undef) { //will get unit adjusted
           double retval = unadjustedParseDouble(str,undef);
           if (!Misc.isUndef(retval)) {
              retval = retval*m_unitInfo.m_unit;
           }
           return (double)retval;
        }
        public double getInt(String str, int undef) { //will get unit adjusted
           int retval = unadjustedParseInt(str,undef);
           if (!Misc.isUndef(retval))
              retval = (int)(retval*m_unitInfo.m_unit);
           return retval;
        }
        public Number(Locale locale, MiscInner.UnitInfo unitInfo) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
			if (dfs.getGroupingSeparator() == (char)160)
				dfs.setGroupingSeparator((char)32);
           m_formatter = (DecimalFormat)DecimalFormat.getNumberInstance(locale);
		   ((DecimalFormat)m_formatter).setDecimalFormatSymbols(dfs);
			//DecimalFormatSymbols dfs = ((DecimalFormat)m_formatter).getDecimalFormatSymbols();
			//if (dfs.getGroupingSeparator() == (char)160)
			//	dfs.setGroupingSeparator((char)32);
           m_unitInfo = unitInfo;           
           ((DecimalFormat)m_formatter).applyPattern(m_unitInfo.m_helperPattern);
        }
        public Number(Locale locale/*, int numDecimal=1*/) {//will create a formatter that formats to 1 decimal 
           int numDecimal = 1;
           m_unitInfo = new MiscInner.UnitInfo(0, 0, 1, numDecimal, null, null, null,0);
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
			if (dfs.getGroupingSeparator() == (char)160)
				dfs.setGroupingSeparator((char)32);
			m_formatter = (DecimalFormat)DecimalFormat.getNumberInstance(locale);
			((DecimalFormat)m_formatter).setDecimalFormatSymbols(dfs);
			//DecimalFormatSymbols dfs = ((DecimalFormat)m_formatter).getDecimalFormatSymbols();
			//if (dfs.getGroupingSeparator() == (char)160)
			//	dfs.setGroupingSeparator((char)32);
           ((DecimalFormat)m_formatter).applyPattern(m_unitInfo.m_helperPattern);
        }
        public Number(Locale locale, double unit, int numDecimal) {//will create a formatter that formats to 1 decimal 
            m_unitInfo = new MiscInner.UnitInfo(0, 0, unit, numDecimal, null, null, null,0);
 			DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
 			if (dfs.getGroupingSeparator() == (char)160)
 				dfs.setGroupingSeparator((char)32);
 			m_formatter = (DecimalFormat)DecimalFormat.getNumberInstance(locale);
 			((DecimalFormat)m_formatter).setDecimalFormatSymbols(dfs);
 		   //DecimalFormatSymbols dfs = ((DecimalFormat)m_formatter).getDecimalFormatSymbols();
 		   //if (dfs.getGroupingSeparator() == (char)160)
 		   //    dfs.setGroupingSeparator((char)32);
            ((DecimalFormat)m_formatter).applyPattern(m_unitInfo.m_helperPattern);
        }
        public Number(Locale locale, double unit, int numDecimal, int minDecimal) {//will create a formatter that formats to 1 decimal 
            m_unitInfo = new MiscInner.UnitInfo(0, 0, unit, numDecimal, null, null, null,minDecimal);
 			DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
 			if (dfs.getGroupingSeparator() == (char)160)
 				dfs.setGroupingSeparator((char)32);
 			m_formatter = (DecimalFormat)DecimalFormat.getNumberInstance(locale);
 			((DecimalFormat)m_formatter).setDecimalFormatSymbols(dfs);
 		   //DecimalFormatSymbols dfs = ((DecimalFormat)m_formatter).getDecimalFormatSymbols();
 		   //if (dfs.getGroupingSeparator() == (char)160)
 		   //    dfs.setGroupingSeparator((char)32);
            ((DecimalFormat)m_formatter).applyPattern(m_unitInfo.m_helperPattern);
        }
        public Number(Locale locale, int numDecimal) {//will create a formatter that formats to 1 decimal 
           m_unitInfo = new MiscInner.UnitInfo(0, 0, 1, numDecimal, null, null, null,0);
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
			if (dfs.getGroupingSeparator() == (char)160)
				dfs.setGroupingSeparator((char)32);
			m_formatter = (DecimalFormat)DecimalFormat.getNumberInstance(locale);
			((DecimalFormat)m_formatter).setDecimalFormatSymbols(dfs);
		   //DecimalFormatSymbols dfs = ((DecimalFormat)m_formatter).getDecimalFormatSymbols();
		   //if (dfs.getGroupingSeparator() == (char)160)
		   //    dfs.setGroupingSeparator((char)32);
           ((DecimalFormat)m_formatter).applyPattern(m_unitInfo.m_helperPattern);
        }
        public String format(double f) {
           return format(f, false, true);
        }
        public String format(int f) {
           return format(f, false, true);
        }
        public String format(double f, boolean adorned, boolean showZero) {
           String part1 = Misc.emptyString;
           if (!Misc.isUndef(f) && (!Misc.isEqual(f,0) || showZero)) {
              f = f/m_unitInfo.m_unit;
              part1 = ((DecimalFormat)m_formatter).format(f);
           }
           String part2 = Misc.emptyString;
           if (adorned) {
              part2 = m_unitInfo.m_suffix;
           }
           if (part1 == null && part1.length() == 0)
              return Misc.emptyString;
           if (part2 != null && part2.length() > 0) {
              return part1 + " "+part2;
           }
           return part1;              
        }                
    }
        
    public static class Currency  extends Number {    
        public DecimalFormat m_currFormatter = null;        
        public MiscInner.CurrencyInfo m_currencyInfo = null;
        public Currency(Locale locale, MiscInner.CurrencyInfo currencyInfo, MiscInner.UnitInfo unitInfo) {
           super(locale, unitInfo);           
           m_currencyInfo = currencyInfo;        
           m_currFormatter = (DecimalFormat)DecimalFormat.getCurrencyInstance(locale);
           m_currFormatter.setCurrency(m_currencyInfo.m_currency);           
           m_currFormatter.applyPattern(m_unitInfo.m_helperPattern);
        }
        
        public void setCurrency(MiscInner.CurrencyInfo currencyInfo) {
           m_currencyInfo = currencyInfo;
           m_currFormatter.setCurrency(m_currencyInfo.m_currency);           
        }
        
        public MiscInner.CurrencyInfo getCurrency(MiscInner.CurrencyInfo currencyInfo) {
           return m_currencyInfo;           
        }
        
        public String format(double f) { //overrides of parent
           return format(f, false, false,true);
        }
        public String format(int f) {//overrides of parent
           return format(f, false, false,true);
        }
        public String format(double f, boolean adorned, boolean showZero) {//overrides of parent
           return format(f, adorned, adorned, showZero);
        }
        public String format(double f, boolean adornWithUnit, boolean adornWithCurrency, boolean showZero) {//overrides of parent
           String part1 = Misc.emptyString;
           if (!Misc.isEqual(f,0) || showZero) {
              f = f/m_unitInfo.m_unit;
              if (adornWithCurrency)
                 part1 = m_currFormatter.format(f);
              else
                 part1 = ((DecimalFormat)m_formatter).format(f);              
           }
           String part2 = Misc.emptyString;
           if (adornWithUnit) {              
              part2 = m_unitInfo.m_suffix;
           }
           
           if (part1 == null && part1.length() == 0)
              return Misc.emptyString;
           if (part2 != null && part2.length() > 0) {
              return part1 + " "+part2;
           }
           return part1;              
        }        
    }
    
}
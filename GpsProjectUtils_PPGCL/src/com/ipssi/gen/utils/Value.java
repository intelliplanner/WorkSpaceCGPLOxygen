package com.ipssi.gen.utils;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;


public class Value implements Comparable {
	 
	//represents the value read from RS (in one of 4 types: int,double,string,date) Has constructors and setValue.
	//Most importantly toString(...) converts val to formatted/lov disp name field and a simple tostring() converts val to unformatted/unadjusted string
	public int m_iVal = Misc.getUndefInt();
	public int m_invlFormatter = 2;//default for seconds
	public double m_dVal = Misc.getUndefDouble();
	public String m_strVal = null;
	public long m_dateVal = 0;
	public int m_type = Cache.STRING_TYPE; //data type
	public boolean isNull() {
		if (m_type == Cache.NUMBER_TYPE)
			return Misc.isUndef(m_dVal);
		else if (m_type == Cache.STRING_TYPE)
			return m_strVal == null;
		else if (m_type == Cache.DATE_TYPE)
			return m_dateVal <= 0;
		else
			return Misc.isUndef(m_iVal);
	}
	public int getIntVal() {
		if (m_type == Cache.NUMBER_TYPE)
			return Misc.isUndef(m_dVal) ? Misc.getUndefInt() : (int) Math.round(m_dVal);
		else if (m_type == Cache.STRING_TYPE || m_type == Cache.GROUP_TOTAL)
			return Misc.getParamAsInt(m_strVal);
		else if (m_type == Cache.DATE_TYPE)
			return Misc.getUndefInt();
		else
			return m_iVal;
	}
	public double getDoubleVal() {
		if (m_type == Cache.NUMBER_TYPE)
			return (m_dVal);
		else if (m_type == Cache.STRING_TYPE || m_type == Cache.GROUP_TOTAL)
			return Misc.getParamAsDouble(m_strVal);
		else if (m_type == Cache.DATE_TYPE)
			return Misc.getUndefDouble();
		else
			return Misc.isUndef(m_iVal) ? Misc.getUndefDouble() : m_iVal;
	}
	public String getStringVal() {
		return toString();
	}
	public Date getDateVal() {
		return m_type == Cache.DATE_TYPE ? m_dateVal <= 0 ? null : new java.util.Date(m_dateVal): null;
	}
	public long getDateValLong() {
		return m_type == Cache.DATE_TYPE ? m_dateVal <= 0 ? Misc.getUndefInt() : m_dateVal : Misc.getUndefInt();
	}

	public void setGroup() {
		m_type = Cache.GROUP_TOTAL;
	}
	public Value(int val) {
		m_type = Cache.INTEGER_TYPE;
		m_iVal = val;
		m_dVal = val;
	}
	public Value(double val) {
		m_type = Cache.NUMBER_TYPE;
		m_dVal = val;
		m_iVal = Misc.isUndef(m_dVal) ? Misc.getUndefInt() : (int) Math.round(m_dVal);//for lov values - i found
	}
	public Value(String val) {
		m_type = Cache.STRING_TYPE;
		m_strVal = val;
	}
	public Value(java.util.Date val) {
		m_type = Cache.DATE_TYPE;
		m_dateVal = val == null ? 0 :val.getTime();
	}
	public Value(long dateVal) {
		m_type = Cache.DATE_TYPE;
		m_dateVal = dateVal;
	}
	public Value(Value rhsVal) {
		m_type = rhsVal.m_type;
		m_iVal = rhsVal.m_iVal;
		m_dVal = rhsVal.m_dVal;
		m_strVal = rhsVal.m_strVal;
		m_dateVal = rhsVal.m_dateVal;			
	}

	public Value() {
		
	}
	public void setValue(int val) {
		m_type = Cache.INTEGER_TYPE;
		m_iVal = val;
	}
	public void setValue(double val) {
		m_type = Cache.NUMBER_TYPE;
		m_dVal = val;
	}
	public void setValue(String val) {
		m_type = Cache.STRING_TYPE;
		m_strVal = val;
	}
	public void setValue(java.util.Date val) {
		m_type = Cache.DATE_TYPE;
		m_dateVal = val == null ? 0 : val.getTime();
	}
	public void setValue(long val) {
		m_type = Cache.DATE_TYPE;
		m_dateVal = val;
	}
	public void setValue(Value rhsVal) {
		m_type = rhsVal.m_type;
		m_iVal = rhsVal.m_iVal;
		m_dVal = rhsVal.m_dVal;
		m_strVal = rhsVal.m_strVal;
		m_dateVal = rhsVal.m_dateVal;			
	}
	public void applyOp(DimConfigInfo.ExprHelper.CalcFunctionEnum op, Value rhsVal) {//TODO elaborate for other types, mix and match
	   
		if (rhsVal != null && rhsVal.isNotNull()) {							
			if (!isNotNull())
				this.setValue(rhsVal);				
			else {										
				if (m_type == Cache.NUMBER_TYPE) {
					if (rhsVal.m_type == Cache.INTEGER_TYPE)
						m_dVal += rhsVal.m_iVal;
					else if (rhsVal.m_type == Cache.NUMBER_TYPE)
						m_dVal += rhsVal.m_dVal;
					else if (rhsVal.m_type == Cache.STRING_TYPE)
						m_dVal += Misc.getParamAsDouble(rhsVal.m_strVal,0);
					else {//
						
					}
				}
				else if (m_type == Cache.INTEGER_TYPE) {
					if (rhsVal.m_type == Cache.INTEGER_TYPE) 
						m_iVal += rhsVal.m_iVal;
					else if (rhsVal.m_type == Cache.NUMBER_TYPE) {
						m_type = Cache.NUMBER_TYPE;
						m_dVal = m_iVal + rhsVal.m_dVal;
						m_iVal = Misc.getUndefInt();							
					}
					else if (rhsVal.m_type == Cache.STRING_TYPE) {
						m_iVal += Misc.getParamAsInt(rhsVal.m_strVal, 0);
					}
				}
				else if (m_type == Cache.STRING_TYPE) {
					
				}
			}//if lhs is not null
		}//if rhs is not null
	}//end
	
	public boolean equals(Value rhsVal) {
		return m_type == rhsVal.m_type && (this.m_type == Cache.GROUP_TOTAL || (this.m_type == Cache.INTEGER_TYPE && m_iVal == rhsVal.m_iVal) ||
				 (this.m_type == Cache.NUMBER_TYPE  && Misc.isEqual(m_dVal,rhsVal.m_dVal)) ||
				 (this.m_type == Cache.NUMBER_TYPE  && Misc.isUndef(m_dVal) && Misc.isUndef(rhsVal.m_dVal)) ||
				 (this.m_type == Cache.STRING_TYPE  && ((m_strVal == null && m_strVal == rhsVal.m_strVal) || (m_strVal != null && m_strVal.equals(rhsVal.m_strVal))))  ||
				 (this.m_type == Cache.DATE_TYPE   && ((m_dateVal <= 0  && rhsVal.m_dateVal <= 0) || (m_dateVal > 0 && m_dateVal == rhsVal.m_dateVal))) 
				 )
				 ;
		
	}
	
	public boolean isNotNull() {
		return ((this.m_type == Cache.INTEGER_TYPE && !Misc.isUndef(this.m_iVal)) ||
				 (this.m_type == Cache.NUMBER_TYPE && !Misc.isUndef(this.m_dVal)) ||
				 (this.m_type == Cache.STRING_TYPE && this.m_strVal != null) ||
				 (this.m_type == Cache.DATE_TYPE && this.m_dateVal > 0) ||
				 this.m_type == Cache.GROUP_TOTAL
				 );
	}
	public String toString(SimpleDateFormat sdf) {
		return this.m_type == Cache.INTEGER_TYPE && !Misc.isUndef(this.m_iVal) ? Integer.toString(this.m_iVal) : 
			 this.m_type == Cache.NUMBER_TYPE && !Misc.isUndef(this.m_dVal)? Double.toString(this.m_dVal) :
			 this.m_type == Cache.STRING_TYPE ? m_strVal :
			 this.m_type == Cache.GROUP_TOTAL ? "Total" :
			 this.m_type == Cache.DATE_TYPE && this.m_dateVal > 0 ? sdf == null ? Misc.indepDateFormat(new java.util.Date(m_dateVal)) : sdf.format(new java.util.Date(m_dateVal))
					: null; 
		     
	}
	public String toString() {//just value based
		return this.m_type == Cache.INTEGER_TYPE ? Integer.toString(this.m_iVal) : 
				 this.m_type == Cache.NUMBER_TYPE ? Double.toString(this.m_dVal) :
				 this.m_type == Cache.STRING_TYPE ? m_strVal :
				this.m_type == Cache.GROUP_TOTAL ? "Total" :
			     m_dateVal <= 0 ? "" : Misc.indepDateFormat(new java.util.Date(m_dateVal))
				 ;
	}
	//public String toString(DimInfo dimInfo, ArrayList uProfileList, Cache cache, SessionManager session, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
//	public String formatTimeInterval(double num) {
//		return formatTimeInterval((int)num);
//	}
	public String formatTimeInterval(double num) {
		if(num<0)
			return "";
		int hr = (int) num / (60 * 60);
		int min = (int) (num - (hr * (60 * 60))) / 60;
		int sec = (int) num - ((min * 60) + (hr * 60 * 60));
		int days = hr / 24;
		hr = hr - days * 24;

		if (m_invlFormatter == 2) {// seconds
			if (hr == 0 && days == 0)
				return Integer.toString(min) + "m:" + Integer.toString(sec)+ "s";
			if (days == 0)
				return Integer.toString(hr) + "h:" + Integer.toString(min)+ "m:" + Integer.toString(sec) + "s";
			else
				return Integer.toString(days) + "d:" + Integer.toString(hr)+ "h:" + Integer.toString(min) + "m:"+ Integer.toString(sec) + "s";
		} else if (m_invlFormatter == 1) {// Hours
			if (days == 0)
				return Integer.toString(hr) + "h";
			else
				return Integer.toString(days) + "d:" + Integer.toString(hr)+ "h";
		} else {// Minutes
			if (hr == 0 && days == 0)
				return Integer.toString(min) + "m";
			if (days == 0)
				return Integer.toString(hr) + "h:" + Integer.toString(min)+ "m";
			else
				return Integer.toString(days) + "d:" + Integer.toString(hr)+ "h:" + Integer.toString(min) + "m";
		}
	}
	
	public String toString(DimInfo dimInfo, com.ipssi.gen.utils.Pair<Double, Double> multScaleFactor, FmtI.AllFmt formatter, SessionManager session, Cache cache, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
	    String retval = null;
	    if (multScaleFactor == null) {
	    	multScaleFactor = new Pair<Double, Double>(1.0,1.0);
	    }
	    
	    if (this.m_type == Cache.GROUP_TOTAL)
	    	return "Total";
	    if (dimInfo == null)
	    	return toString(); //convert as is
		if (dimInfo != null ) {
			int attribType = dimInfo.getAttribType();
			int subType = Misc.getParamAsInt(dimInfo.m_subtype);
			double addFactor = 0;
			double mulFactor = 1;
			boolean uProfile = false;
			if (dimInfo.m_descDataDimId == 123) { //a better approach will be to change getAttribDisplayNameFull below - but requires changes in LOV
				MiscInner.PortInfo av = cache.getParentPortNode(conn, m_iVal, dimInfo);     
			    retval = av == null ? "N/A" : av.m_name;
			}
			else if (attribType == Cache.LOV_TYPE) {
				retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, m_iVal);
			}				
			else if (attribType == Cache.NUMBER_TYPE) {
				double internalVal = getDoubleVal();
				
				double dval = uProfile ? internalVal*mulFactor+addFactor : internalVal;
				if (subType == 20510){
					//multScaleFactor.first/multi_factor should be >0
					//multScaleFactor.second/add_factor 0=Convert Minute to Hour and 1=Convert Minute to Seconds
					dval=dval*60;
					if(multScaleFactor.first==60){
					//	dval=((int)dval/60)*multScaleFactor.first;
						m_invlFormatter=0;//minute
					}else if(multScaleFactor.first==3600){
						//dval=((int)dval/3600)*multScaleFactor.first;
						m_invlFormatter=1;	//hour
					}else{
						//dval=((int)dval)*multScaleFactor.first;
						m_invlFormatter=2;	//second
					}
//					dval=(multScaleFactor.first>0 && multScaleFactor.second==0)?dval/multScaleFactor.first:(multScaleFactor.first>0 && multScaleFactor.second==1)?dval*multScaleFactor.first:dval;
					retval = formatTimeInterval(dval);
				
					
				}else if ( subType == 20502)
					retval = Misc.m_currency_formatter.format(dval);
				else if (formatter != null) {
					retval = formatter == null ? Double.toString(dval) : ((FmtI.Number)formatter).format(dval);
				}
				else {						
					retval = Double.toString(dval);
				}
			}
			else if (attribType == Cache.DATE_TYPE) {
				Date internalVal = this.getDateVal();
				if (formatter != null) {
					retval = ((FmtI.Date)formatter).format(internalVal);
				}
				else {
					retval = sdf.format(internalVal);
				}
			}				
			else if (attribType == Cache.INTEGER_TYPE || attribType == Cache.LOV_NO_VAL_TYPE) {
				int internalVal = this.getIntVal();
				int ival = uProfile ?(int) (internalVal*mulFactor+addFactor) : internalVal;
				if (subType == 20510)
					retval = formatTimeInterval(ival);
				else
					retval = Integer.toString(ival);
			}				
			else if (attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE ) {
				retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, m_iVal);//TODO fully implement this function	
			}							
		}//if valid DimInfo				    
	    if (retval == null)
	    	retval = toString();
	    return retval;
   }//end of formatted toString()
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Value rhs = (Value) arg0;
		int result = 0;
		boolean meNull = this.isNull();
		boolean rhsNull = rhs == null || rhs.isNull();
		if (meNull && rhsNull) {
			result = 0;
		}
		else if (meNull && !rhsNull) {
			result = -1;
		}
		else if (!meNull && rhsNull) {
			result = 1;
		}
		if (!Misc.isUndef(this.m_dVal) || !Misc.isUndef(rhs.m_dVal)) {
			double ld = this.getDoubleVal();
			double rd = rhs.getDoubleVal();
			result = Misc.isEqual(ld, rd) ? 0 : ld-rd < 0 ? -1 : 1;
		}
		else if (!Misc.isUndef(this.m_iVal) || !Misc.isUndef(rhs.m_iVal)) {
			int ld = this.getIntVal();
			int rd = rhs.getIntVal();
			result = ld == rd ? 0 : ld < rd ? -1 : 1;
		}
		else if (this.m_dateVal > 0 || rhs.m_dateVal > 0) {
			long ld = this.getDateValLong();
			long rd = rhs.getDateValLong();
			result = ld == rd ? 0 : ld < rd ? -1 : 1;
		}
		else {
			String ld = this.getStringVal();
			String rd = rhs.getStringVal();
			result = ld == null ? 1 : ld.compareTo(rd);
		}
		return result;
	}
	public int hashCode() {
		if (this.isNull())
			return Misc.getUndefInt();
		if (!Misc.isUndef(this.m_iVal)) {
			return this.m_iVal;
		}
		else if (!Misc.isUndef(this.m_dVal)) {
			return (new Double(this.m_dVal)).hashCode();
		}
		else if (!Misc.isUndef(this.m_dateVal)) {
			return (new Double(this.m_dVal)).hashCode();
		}
		else if (this.m_strVal != null)
			return m_strVal.hashCode();
		else
			return Misc.getUndefInt();
	}
	public static class ArrayOfValComparator implements Comparator<ArrayList<Value>> {
		public int compare(ArrayList<Value> o1, ArrayList<Value> o2) {
			// TODO Auto-generated method stub
			for (int i=0,is=o1==null ? 0 : o1.size(); i<is; i++) {
				Value l = o1 == null || o1.size() <= i ? null : o1.get(i);
				Value r = o2 == null || o2.size() <= i ? null : o2.get(i);
				boolean lnull = l == null || l.isNull();
				boolean rnull = r == null || r.isNull();
				
				int cmp = lnull && rnull ? 0 : lnull && !rnull ? 1 : !lnull && rnull ? -1 :  l.compareTo(r);
				if (cmp != 0)
					return cmp;
			}
			return 0;
		}

		
	}

}

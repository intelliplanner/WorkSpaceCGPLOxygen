package com.ipssi.reporting.trip;

//move to GpsProjectUtils
public class ResultInfoBak {/*
	public ResultSet m_rs;
	public ArrayList<DimConfigInfo> m_fpList;
	public HashMap<String, Integer> m_colIndexLookup;
	public SessionManager m_session;
	public ArrayList<Integer> m_colsInGroupBy = null; //Int = index in the fpList, bool = true => asc, else desc
	public SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
	public ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult = null;
	public ArrayList<VirtualHelper> virtualHelper = null;
	
    
	public static class FormatHelper {	
    	public ArrayList<DimConfigInfo> m_uProfileList;
    	public ArrayList<DimConfigInfo> m_sProfileList;
    	public ArrayList<DimConfigInfo> m_fProfileList; //currently not used	
    	public int m_fFormatSelected; //the formatting entry in d50560
    	public ArrayList<FmtI.AllFmt> formatters = null; // initialized and populated in getFormattersAndMultScale
    	public ArrayList<Pair<Double, Double>> multScaleFactors = null; // initialized and populated in getFormattersAndMultScale
    	public FmtI.AllFmt getFormatter(int index) {
    		return formatters == null || index < 0 || index >= formatters.size() ? null : formatters.get(index);
    	}
    }
    public FormatHelper formatHelper = null;
    
	public  static class Value { 
		//represents the value read from RS (in one of 4 types: int,double,string,date) Has constructors and setValue.
		//Most importantly toString(...) converts val to formatted/lov disp name field and a simple tostring() converts val to unformatted/unadjusted string
		public int m_iVal = Misc.getUndefInt();
		public double m_dVal = Misc.getUndefDouble();
		public String m_strVal = null;
		public java.util.Date m_dateVal = null;
		public int m_type = Cache.STRING_TYPE; //data type
		public int getIntVal() {
			if (m_type == Cache.NUMBER_TYPE)
				return (int) Math.round(m_dVal);
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
				return m_iVal;
		}
		public String getStringVal() {
			return toString();
		}
		public Date getDateVal() {
			return m_type == Cache.DATE_TYPE ? m_dateVal : null;
		}

		public void setGroup() {
			m_type = Cache.GROUP_TOTAL;
		}
		public Value(int val) {
			m_type = Cache.INTEGER_TYPE;
			m_iVal = val;
		}
		public Value(double val) {
			m_type = Cache.NUMBER_TYPE;
			m_dVal = val;
		}
		public Value(String val) {
			m_type = Cache.STRING_TYPE;
			m_strVal = val;
		}
		public Value(java.util.Date val) {
			m_type = Cache.DATE_TYPE;
			m_dateVal = val;
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
		   
			if (rhsVal.isNotNull()) {							
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
					 (this.m_type == Cache.NUMBER_TYPE  && m_dVal == rhsVal.m_dVal) ||
					 (this.m_type == Cache.STRING_TYPE  && ((m_strVal == null && m_strVal == rhsVal.m_strVal) || (m_strVal != null && m_strVal.equals(rhsVal.m_strVal))))  ||
					 (this.m_type == Cache.DATE_TYPE   && ((m_dateVal == null && m_dateVal == rhsVal.m_dateVal) || (m_dateVal != null && m_dateVal.equals(rhsVal.m_dateVal)))) 
					 )
					 ;
			
		}
		
		public boolean isNotNull() {
			return ((this.m_type == Cache.INTEGER_TYPE && !Misc.isUndef(this.m_iVal)) ||
					 (this.m_type == Cache.NUMBER_TYPE && !Misc.isUndef(this.m_dVal)) ||
					 (this.m_type == Cache.STRING_TYPE && this.m_strVal != null) ||
					 (this.m_type == Cache.DATE_TYPE && this.m_dateVal != null) ||
					 this.m_type == Cache.GROUP_TOTAL
					 );
		}
		public String toString() {//just value based
			return this.m_type == Cache.INTEGER_TYPE ? Integer.toString(this.m_iVal) : 
					 this.m_type == Cache.NUMBER_TYPE ? Double.toString(this.m_dVal) :
					 this.m_type == Cache.STRING_TYPE ? m_strVal :
					this.m_type == Cache.GROUP_TOTAL ? "Total" :
				     Misc.indepDateFormat(m_dateVal)
					 ;
		}
		//public String toString(DimInfo dimInfo, ArrayList uProfileList, Cache cache, SessionManager session, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
		public String formatTimeInterval(double num) {
			return formatTimeInterval((int)num);
		}
		public String formatTimeInterval(int num) {
			int hr = num/60;
			int min = num - hr*60;
			int days = hr/24;
			hr = hr - days*24;
			if (hr == 0 && days == 0)
				return Integer.toString(num)+"m";
			else if (days == 0) {
				return Integer.toString(hr)+"h:"+Integer.toString(min)+"m";
			}
			else {
				return Integer.toString(days)+"d:"+Integer.toString(hr)+"h:"+Integer.toString(min)+"m";
			}			
		}
		public String toString(DimInfo dimInfo, Pair<Double, Double> multScaleFactor, FmtI.AllFmt formatter, SessionManager session, Cache cache, Connection conn, SimpleDateFormat sdf) throws Exception { //generalized formatting
		    String retval = null;
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
				
				if (attribType == Cache.LOV_TYPE) {
					retval = cache.getAttribDisplayNameFull(session, conn, dimInfo, m_iVal);
				}				
				else if (attribType == Cache.NUMBER_TYPE) {
					double internalVal = getDoubleVal();
					
					double dval = uProfile ? internalVal*mulFactor+addFactor : internalVal;
					if (subType == 20510)
						retval = formatTimeInterval(dval);
					else if ( subType == 20502)
						retval = Misc.m_currency_formatter.format(dval);
					else if (formatter != null) {
						retval = ((FmtI.Number)formatter).format(dval);
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
	}//end of class
	private static final int g_prevRowsCount = 1;
	private static final int g_nextRowsCount = 1;	
	private ArrayList<ArrayList<Value> >m_rows = new ArrayList<ArrayList<Value>> ();	
	private ArrayList<Boolean> m_rowsValidity = new ArrayList<Boolean>();
	private boolean m_rsetIsClosed = false;//if last row has been read
	boolean m_firstRowRead = false;
	private ArrayList<Integer> m_colIndexUsingExpr = null;
	private ArrayList<ArrayList<Value> > m_prevValOfRequiringExpr = null; //initialized in ResultInfo
	private ArrayList<ArrayList<Value> > m_currValOfRequiringExpr = null; //initialized in ResultInfo
	
	 
	
	private static void clearRow(ArrayList<Value> row) {
		for (int i=0,is=row.size();i<is;i++) {
			Value val = row.get(i);
			if (val.m_type == Cache.INTEGER_TYPE)
				val.m_iVal = Misc.getUndefInt();
			else if (val.m_type == Cache.NUMBER_TYPE)
				val.m_dVal = Misc.getUndefDouble();
			else if (val.m_type == Cache.DATE_TYPE)
				val.m_dateVal = null;
			else
				val.m_strVal = null;
		}
	}
	
	private static void copyRow(ArrayList<Value> fromRow, ArrayList<Value> toRow) {
		for (int i=0,is=fromRow.size();i<is;i++) {
			Value fromVal = fromRow.get(i);
			Value toVal = fromRow.get(i);
			if (fromVal.m_type == Cache.INTEGER_TYPE)
				toVal.m_iVal = fromVal.m_iVal;
			else if (fromVal.m_type == Cache.NUMBER_TYPE)
				toVal.m_dVal = fromVal.m_dVal;
			else if (fromVal.m_type == Cache.DATE_TYPE)
				toVal.m_dateVal = fromVal.m_dateVal;
			else
				fromVal.m_strVal = fromVal.m_strVal;
		}
	}
	
	private ArrayList<Value> createEmptyRow() {
		ArrayList<Value> retval = new ArrayList<Value>();
		for (int i=0,is=m_fpList.size();i<is;i++) {
			DimConfigInfo dci = m_fpList.get(i);
			int attribType = dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.getAttribType();
			if (attribType == Cache.STRING_TYPE) {
				retval.add(new Value((String) null));
			}
			else if (attribType == Cache.NUMBER_TYPE) {
				retval.add(new Value(Misc.getUndefDouble()));
			}
			else if (attribType == Cache.DATE_TYPE) {
				retval.add(new Value((Date) null));
			}
			else {
				retval.add(new Value(Misc.getUndefInt()));
			}
		}
		return retval;
	}
	public void setResultSet(ResultSet rs) {
		m_rs = rs;
	}
	
	public ResultInfo(ArrayList<DimConfigInfo> fpList, HashMap<String, Integer> colIndexLookup, ResultSet rs, SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper, ArrayList<Integer> colsInGroupBy, ArrayList<Integer> colIndexUsingExpr, FormatHelper formatHelper, ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult, ArrayList<VirtualHelper> virtualHelper) {
		m_rs = rs;
		m_fpList = fpList;
		m_colIndexLookup = colIndexLookup;
		m_session = session;			
		m_colsInGroupBy = colsInGroupBy;
		this.virtualResult = virtualResult;
		this.virtualHelper = virtualHelper;
		
		for (int i=0,is=g_prevRowsCount+g_nextRowsCount+1;i<is;i++) {
			m_rows.add(createEmptyRow());
			m_rowsValidity.add(new Boolean(false));
		}
		m_colsInGroupBy = colsInGroupBy;
		int colsInGroupBySize =colsInGroupBy == null ? 0 :  colsInGroupBy.size();
		m_currentlyShowingRollupFor = colsInGroupBySize;
		
		if (colsInGroupBySize > 0) {				
			m_currGroupedRow = new ArrayList<Value>(colsInGroupBySize);
			m_nextGroupedRow = new ArrayList<Value>(colsInGroupBySize);
			for (int i=0,is=colsInGroupBySize;i<is;i++) {
				m_currGroupedRow.add(null);
				m_nextGroupedRow.add(null);
			}
		}
		m_colIndexUsingExpr = colIndexUsingExpr;
		if (colIndexUsingExpr != null && colIndexUsingExpr.size() > 0) {
			m_prevValOfRequiringExpr = new ArrayList<ArrayList<Value>>();
			m_currValOfRequiringExpr = new ArrayList<ArrayList<Value>>();
			for (int i=0,is=colIndexUsingExpr.size();i<is;i++) {
				ArrayList<Value> pval = new ArrayList<Value>();
				m_prevValOfRequiringExpr.add(pval);
				ArrayList<Value> cval = new ArrayList<Value>();
				m_currValOfRequiringExpr.add(cval);
				DimConfigInfo dci = fpList.get(colIndexUsingExpr.get(i));				
				for (int j=0,js=dci.m_expr.m_resetColIndex.size();j<js;j++) {
					pval.add(null);
					cval.add(null);
				}
			}
		}
	    if (formatHelper == null) {
	    	formatHelper = getFormatHelper(m_fpList, session, searchBoxHelper);
	    }
	    this.formatHelper = formatHelper;		
	}
	
	public void readRow(int index) throws Exception { //do related calculations in this. calc values will not be set ... instead these will be set in processForCalc, which needs to be called after processForGrouping
		ArrayList<Value> row = m_rows.get(index);		
		Connection conn = m_session.getConnection();
		Cache cache = m_session.getCache();
		//do copy etc if any. in this also do any calculations if desired
		
		for (int i=0,is=m_fpList.size();i<is;i++) {//must have all the calculations etc
			DimConfigInfo dci = m_fpList.get(i);
			int attribType = dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.getAttribType();
			VirtualHelper vhelper = virtualHelper == null ? null : virtualHelper.get(i);
			if (vhelper == null || !vhelper.isVirtual) {
				if (attribType == Cache.STRING_TYPE) {
					String val = m_rs.getString(i+1);
					row.get(i).setValue(val);
				}
				else if (attribType == Cache.INTEGER_TYPE) {
					int val = Misc.getRsetInt(m_rs,i+1);
					row.get(i).setValue(val);
				}
				else if (attribType == Cache.NUMBER_TYPE) {
					double val = Misc.getRsetDouble(m_rs,i+1);
					row.get(i).setValue(val);
				}
				else if (attribType == Cache.DATE_TYPE) {					
					java.sql.Timestamp val = m_rs.getTimestamp(i+1);
					row.get(i).setValue(Misc.sqlToUtilDate(val));
				}
				else {
					int val = Misc.getRsetInt(m_rs,i+1);
					if (attribType == Cache.LOV_TYPE) {
						val = cache.getParentDimValId(conn, dci.m_dimCalc.m_dimInfo, val);
					}
					row.get(i).setValue(val);
				}
			}
			else {
				int vehicleId = vhelper.look1 >= 0 ? Misc.getRsetInt(m_rs, vhelper.look1+1, index) : Misc.getUndefInt();
			    int tabIndex = vhelper.tabIndex;
			    java.util.Date gpsRecordTime = vhelper.look2 >= 0 ? Misc.sqlToUtilDate(m_rs.getTimestamp(vhelper.look2+1)) : null;
			    if (i >= 15 && gpsRecordTime.after(new Date(111,7,25,5,0,0))) {
			    	int dbg = 1;
			    	dbg++;
			    }
			    GeneralizedQueryBuilder.VirtualVal lookupVal = new GeneralizedQueryBuilder.VirtualVal(gpsRecordTime);
			    MiscInner.Pair lookupKey = new MiscInner.Pair(vehicleId, i);
			    FastList<GeneralizedQueryBuilder.VirtualVal> list = virtualResult == null || virtualResult.get(tabIndex) == null ? null : virtualResult.get(tabIndex).get(lookupKey);
			    GeneralizedQueryBuilder.VirtualVal entry = list == null  ? null : gpsRecordTime == null ? list.get(0) : list.get(lookupVal);
			    Object oval = entry == null ? null : entry.getVal(vhelper.col, gpsRecordTime);
				if (attribType == Cache.STRING_TYPE) {
					String val = oval == null ? null : oval.toString();
					row.get(i).setValue(val);
				}
				else if (attribType == Cache.INTEGER_TYPE) {
					if (oval != null) {
						int dbg = 1;
						dbg++;
					}
					int val = oval == null ? Misc.getUndefInt() : oval instanceof Integer ? ((Integer)oval).intValue() : (int) Math.round(((Double)oval).doubleValue());
					row.get(i).setValue(val);
				}
				else if (attribType == Cache.NUMBER_TYPE) {
					double val = oval == null ? Misc.getUndefDouble() : ((Double)oval).doubleValue();
					row.get(i).setValue(val);
				}
				else if (attribType == Cache.DATE_TYPE) {
					row.get(i).setValue((Date)oval);
				}
				else {
					int val = oval == null ? Misc.getUndefInt() : (int) Math.round(((Double)oval).doubleValue());
					if (attribType == Cache.LOV_TYPE) {
						val = cache.getParentDimValId(conn, dci.m_dimCalc.m_dimInfo, val);
					}
					row.get(i).setValue(val);
				}
			}
		}					
	}
	
	public void processForCalcValues() {
		int index = g_prevRowsCount;
		ArrayList<Value> row = m_rows.get(index);
		ArrayList<Value> prevRow = m_rows.get(index-1);
		if (m_firstRowRead) {
			ArrayList<ArrayList<Value>> temp = m_prevValOfRequiringExpr;
			m_prevValOfRequiringExpr = m_currValOfRequiringExpr;
			m_currValOfRequiringExpr = temp;
		}
		for (int i=0,is=m_colIndexUsingExpr == null ? 0 : m_colIndexUsingExpr.size();i<is;i++) {
			int colIndex = m_colIndexUsingExpr.get(i);
			DimConfigInfo dci = m_fpList.get(colIndex);						
			extractGroupingElem(row, m_currValOfRequiringExpr.get(i), dci.m_expr.m_resetColIndex);
			Value valToAdd = row.get(dci.m_expr.m_cummColIndex);
			if (isRollupRow() || !isEqualIncl(m_currValOfRequiringExpr.get(i), m_prevValOfRequiringExpr.get(i))) {
				row.get(colIndex).setValue(valToAdd);
			}
			else {
				row.get(colIndex).setValue(prevRow.get(colIndex));
				row.get(colIndex).applyOp(dci.m_expr.m_calcFunction, valToAdd);
			}			
		}
	}
	
	public ArrayList<Value> peek(int indexRelCurr) throws Exception {//indexRel can be 1 or upto -g_prevRowsCount. If it is 1 then will attempt read ahead, else will return if there is a row
		if (indexRelCurr > g_nextRowsCount || indexRelCurr < -1*g_prevRowsCount)
			return null;
		boolean isValid = m_rowsValidity.get(g_prevRowsCount+indexRelCurr);
		if (isValid) {
		   return	m_rows.get(indexRelCurr+g_prevRowsCount);
		}
		if (indexRelCurr < 0 && !isValid) {
			return null; //not enough prev row
		}
		if (m_rsetIsClosed) {
			return null;
		}
		m_rsetIsClosed = !m_rs.next();
		if (m_rsetIsClosed) {
			return null;
		}
		clearRow(m_rows.get(g_prevRowsCount+indexRelCurr));
		readRow(g_prevRowsCount+indexRelCurr);
		m_rowsValidity.set(g_prevRowsCount+indexRelCurr, true);
		return m_rows.get(indexRelCurr+g_prevRowsCount);
	}
	
	public boolean next() throws Exception {	//THE VALUES RETURNED MIGHT CHANGE BETWEEN ONE INVOCATION TO ANOTHER ... so to remember values, make a copy
		//do shifting to left
		ArrayList<Value> firstRow = m_rows.get(0);
		for (int i=1,is=m_rows.size();i<is;i++) {
			m_rows.set(i-1, m_rows.get(i));
			m_rowsValidity.set(i-1, m_rowsValidity.get(i));
		}
		m_rows.set(m_rows.size()-1,firstRow);
		m_rowsValidity.set(m_rowsValidity.size()-1, false);
		boolean retval = false;
		if (m_rowsValidity.get(g_prevRowsCount)) {
			processForGrouping();
			processForCalcValues();
			m_firstRowRead = true;
			retval = true;
		   return retval;
		}		
		if (m_rsetIsClosed)
			return false;
		
		retval = m_rs.next();

		if (!retval) {
			m_rsetIsClosed = true;
			return retval;
		}
		readRow((g_prevRowsCount));
		m_rowsValidity.set(g_prevRowsCount, true);
	    processForGrouping();
	    processForCalcValues();
	    m_firstRowRead = true;
		return retval;
	}
	
    public boolean isCurrEqualToRelativeRow(int currRelIndex) throws Exception {
    	return isEqualIncl(m_rows.get(g_prevRowsCount), peek(currRelIndex));
    }
    public boolean isCurrEqualToRelativeRow(int currRelIndex, int szIncl) throws Exception {
    	return isEqualIncl(m_rows.get(g_prevRowsCount), peek(currRelIndex), szIncl);
    }
    
    private boolean isEqualIncl(ArrayList<Value> row1, ArrayList<Value> row2) {//returns the first index at which mismatch occurs. If no mismatch then sz is returned
    	int sz = row1 == null ? (row2 == null ? -1 : row2.size()-1) : row1.size()-1;
    	return isEqualIncl(row1,row2,sz);
    }
    
	 private boolean isEqualIncl(ArrayList<Value> row1, ArrayList<Value> row2, int sz) {//returns the first index at which mismatch occurs. If no mismatch then sz is returned
		 if ((row1 == null && row2 != null) || (row1 != null && row2 == null))
				 return false;
	    
	     for (int i=0; i<=sz;i++) {
	         Value elem1 = row1.get(i);
	         Value elem2 = row2.get(i);
	         if (elem1 == null && elem2 == null) {
	            //do nothing
	         }
	         else if ((elem1 != null && elem2 == null) || (elem1 == null && elem2 != null) || !elem1.equals(elem2)) {
	            return false;
	         }	         
	     }
	     return true;
	 }
	 
	 // start of rollup row identification related code
	 //Approach ... 1
	 //Definition: data row - the data row
	 //                   group row/rollup row/row - the row that has values in order for group index
	 //                  
	 //Observation ... rollup rows occur sequentially. 
	 //                          If the previous row indicated rollup at group col 'x' then, in the current group row, x-1 and later must be null
	 //                          else the rollup show sequence becomes reset
	 
	 // Now the question comes how do we know we are in start of rollup mode
	 
	 // Focus on the last item in curr group row. If this item is not null then this cant be rollup start
	 // if this item is null it could come because this corresponds to data or corresponds to rollup. 
	 // if we have already seen null without marking rollup then we can safely say that this rollup
	 //  else check if this row differs from the next row till last pos
	 // 
	 	
	private ArrayList<Value> m_currGroupedRow = null;
	private ArrayList<Value> m_nextGroupedRow = null;
	private boolean m_seenNullForLastItemInGroup = false;
	private boolean m_inRollupMode = false;
	private int m_currentlyShowingRollupFor;// = m_colsInGroupBy.size(); initialized in ResultInfo
	
	
	 private void extractGroupingElem(ArrayList<Value> row, ArrayList<Value> groupVal, ArrayList<Integer> groupIndex) {
		 if (row == null) {
			 for (int i=0,is=groupIndex.size();i<is;i++) {
		          groupVal.set(i,null);
		     } 
		 }
		 else {
		     for (int i=0,is=groupIndex.size();i<is;i++) {
		          groupVal.set(i,row.get(groupIndex.get(i)));
		     }
		 }
	 }
	 
	 public boolean isRollupRow() {
		 return m_inRollupMode;
	 }
	 
	 public int getCurrColBeingRolledUp() {
		 return m_colsInGroupBy == null || m_currentlyShowingRollupFor >= m_colsInGroupBy.size() ? -1 : m_colsInGroupBy.get(m_currentlyShowingRollupFor);
	 }
	 
	 private void processForGrouping() throws Exception {
		 int numColsInGroupBy = m_colsInGroupBy == null ? 0 : m_colsInGroupBy.size();
		 if (numColsInGroupBy == 0) 
			 return;
		 //first get the prev, curr, next rows
    	 ArrayList<Value> temp = m_currGroupedRow;
    	 m_currGroupedRow = m_nextGroupedRow;
    	 m_nextGroupedRow = temp;    	     	 
    	 ArrayList<Value> nextRow = peek(1);    	 
    	 extractGroupingElem(nextRow, m_nextGroupedRow, m_colsInGroupBy);
    	 if (m_currGroupedRow.get(0) == null)
    		 extractGroupingElem(m_rows.get(g_prevRowsCount), m_currGroupedRow, m_colsInGroupBy);
    	 
    	 if (m_inRollupMode) {
    	      //now check if the grouping construct is reset
    		 for (int i=m_currentlyShowingRollupFor-1;i<numColsInGroupBy;i++) {
    			 Value v = m_currGroupedRow.get(i);
    			 if (v.isNotNull()) {
    				 m_inRollupMode = false;
    				 m_seenNullForLastItemInGroup = false;
    				 m_currentlyShowingRollupFor = numColsInGroupBy;
    			 }
    		 }
    	 }
    	 if (m_inRollupMode) {
    		 m_currentlyShowingRollupFor--;
    	 }
    	 else { //check if we have to start
    		 Value v = m_currGroupedRow.get(numColsInGroupBy-1);
    		  if (!v.isNotNull()) {
    			 if (m_seenNullForLastItemInGroup) {
    			    m_inRollupMode = true;
    			    m_currentlyShowingRollupFor = numColsInGroupBy-1;
    			 }
    			 else {
    				 //check if curr is different from next till incl numColsInGroupBy-2
    				if (!isEqualIncl(m_currGroupedRow, m_nextGroupedRow, numColsInGroupBy-2)) {
    				    m_inRollupMode = true;
    				    m_currentlyShowingRollupFor = numColsInGroupBy-1;
    				}
    				else {
    					m_seenNullForLastItemInGroup = true;
    				}
    			 }//first time seeing NullForLastItemInGroup
    		  }//v is null    		  
    	 }//end of checking if need to start inRollupMode
	 }//end of func

	
	public Value getVal(int index) {
		return m_rows.get(g_prevRowsCount).get(index);
	}
	
	public Value getVal(String str) {
		Integer index = this.m_colIndexLookup.get(str);
		return index == null ? null : getVal(index.intValue());
	}
	
	public Value getValDefaultAdjusted(int index) {
		Value retval = m_rows.get(g_prevRowsCount).get(index);
		if (retval.isNotNull())
			return retval;
		//else adjust it for default
		DimConfigInfo dci = m_fpList.get(index);
		DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
		if (dimInfo != null ) {
			String str = dimInfo.getDefaultString();
			if (str != null && str.length() != 0) {
				if (retval.m_type == Cache.INTEGER_TYPE) {
					int val = Misc.getParamAsInt(str);
					if (!Misc.isUndef(val))
						retval = new Value(val);
				}
				else if (retval.m_type == Cache.NUMBER_TYPE) {
					double val = Misc.getParamAsDouble(str);
					if (!Misc.isUndef(val))
						retval = new Value(val);
				}
				else if (retval.m_type == Cache.DATE_TYPE) {
					java.sql.Timestamp val = null;
					synchronized (Misc.m_indepFormatterFull) {
						val = Misc.getParamAsTimestamp(str, null, Misc.m_indepFormatterFull);
					}
					if (val != null)
					    retval = new Value(Misc.sqlToUtilDate(val));					
				}
				else {
					retval = new Value(str);
				}
			}//if good str
		}//if good dimInfo
		return retval;
	}
	
	
	public String getValStr(int index) throws Exception {
		return getValStr(index, false, true, Misc.nbspString);
	}
   		
	public String getValStr(int index, boolean doDefaultAdjusted, boolean doProfileAdjusted, String nullValString) throws Exception { //TODO adjust for profile/format adjusted
		Cache cache = m_session.getCache();
		Connection conn = m_session.getConnection();
	    String retval = null;
	    Value val = doDefaultAdjusted ? getValDefaultAdjusted(index) : getVal(index);
	    if (!val.isNotNull()) {
	    	retval = nullValString;
	    }
	    else {
		    DimConfigInfo dci = m_fpList.get(index);
			DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
			if (dimInfo != null ) {
				retval = val.toString(dimInfo, formatHelper.multScaleFactors.get(index), formatHelper.formatters.get(index), m_session, m_session.getCache(), conn, sdf);
			}			
	    }
	    if (retval == null)
	    	retval = val.toString();
	    return retval;
	}
	
	public static FormatHelper getFormatHelper(ArrayList<DimConfigInfo> colList,  SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper) {
		FormatHelper retval = new FormatHelper();
		// TODO pv20501
//		int uProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": searchBoxHelper.m_topPageContext+"20501"), 0);
//	    int sProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": searchBoxHelper.m_topPageContext+"20530"), 0);
//	    int fProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": searchBoxHelper.m_topPageContext+"20560"), 0);
		int uProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": "pv20501"), 0);
	    int sProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": "pv20530"), 0);
	    int fProfiler = Misc.getParamAsInt(session.getParameter(searchBoxHelper == null ? "": "pv20560"), 0);
	    retval.m_fFormatSelected = fProfiler;
		Cache cache = session.getCache();
		retval.m_uProfileList = DimConfigInfo.getProfileList(cache.getUnitProfileDef(), uProfiler);
		retval.m_sProfileList = DimConfigInfo.getProfileList(cache.getScaleProfileDef(), sProfiler);
		retval.m_fProfileList = DimConfigInfo.getProfileList(cache.getFormatProfileDef(), fProfiler);
		retval.formatters = new ArrayList<FmtI.AllFmt>();
		retval.multScaleFactors = new ArrayList<Pair<Double,Double>>();
		String lang = "en";
		String country = "IN";
		DimInfo dLocalList = DimInfo.getDimInfo(20560);
		ValInfo dlocalValInfo = dLocalList == null ? null : dLocalList.getValInfo(retval.m_fFormatSelected);
		if (dlocalValInfo != null) {
			lang = dlocalValInfo.getOtherProperty("lang");
			country = dlocalValInfo.getOtherProperty("country");
		}
		
		if (lang == null || lang.length() == 0)
			lang = "en";
		if (country == null | country.length() == 0)
			country = "IN";
		
		Locale locale = new Locale(lang, country);
		for (int i=0,is = colList.size();i<is;i++) {
			DimConfigInfo dc = colList.get(i);
			FmtI.AllFmt toAdd = null;
			Pair<Double, Double> multScale = null;
			if (dc != null && dc.m_dimCalc != null && dc.m_dimCalc.m_dimInfo != null) {
				DimInfo dimInfo = dc.m_dimCalc.m_dimInfo;
				int ty = dimInfo.m_type;
				int subTy = Misc.getParamAsInt(dimInfo.m_subtype);
				DimInfo subTypeDim = DimInfo.getDimInfo(subTy);
				DimConfigInfo unitprofile = DimConfigInfo.getProfile(retval.m_uProfileList, subTy);
				DimConfigInfo scaleprofile = DimConfigInfo.getProfile(retval.m_sProfileList, subTy);
				
				
				if (ty == Cache.NUMBER_TYPE) {
					if (subTypeDim != null && unitprofile != null) {
						double addFactor = 0;
						double mulFactor = 1;
						DimInfo.ValInfo valInfo = subTypeDim.getValInfo(unitprofile.m_p_val);
						if (valInfo != null) {
							addFactor = Misc.getParamAsDouble(valInfo.getOtherProperty("add_factor"));
							mulFactor = Misc.getParamAsDouble(valInfo.getOtherProperty("multi_factor"));
							multScale = new Pair<Double,Double>(mulFactor, addFactor);
						}
					}
					if (subTypeDim != null && scaleprofile != null) {
						double unit = scaleprofile.m_scale;
						int numAfterDec = scaleprofile.m_decimalPrecision;
						FmtI.Number numfmt = new FmtI.Number(locale, unit, numAfterDec);
						toAdd = numfmt;
					}
				}
				else if (ty == Cache.DATE_TYPE) {
					FmtI.Date dtfmt = new FmtI.Date(locale, subTy == 20506);
					toAdd = dtfmt;
				}
				else {
					//do nothing - no formatting
				}				
			}
			retval.formatters.add(toAdd);
			retval.multScaleFactors.add(multScale);
		}
		return retval;
	}

	
*/}

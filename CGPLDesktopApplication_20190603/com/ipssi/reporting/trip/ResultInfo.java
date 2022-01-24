package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.OpsTPR.OpsToTPRMines;
import com.ipssi.common.ds.trip.ChallanInfo;
import com.ipssi.common.ds.trip.LatestTripInfo;
import com.ipssi.common.ds.trip.NewProfileCache;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.OpToOpETA;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.eta.NewETAforSrcDestItem;
import com.ipssi.eta.NewVehicleETA;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.ColumnMappingHelper;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.DriverExtendedInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.FmtI;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.gen.utils.DimConfigInfo.ExprHelper;
import com.ipssi.gen.utils.DimConfigInfo.ExprHelper.CalcFunctionEnum;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.FmtI.AllFmt;
import com.ipssi.gen.utils.FmtI.Number;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.gen.utils.Value;
import com.ipssi.geometry.Region;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.tprCache.HHLatestCache;
import com.ipssi.tprCache.Loader;
import com.ipssi.tprCache.TPRLatestCache;
import com.ipssi.tripcommon.LUInfoExtract;


public class ResultInfo {
	public ResultSet m_rs;
	public ArrayList<DimConfigInfo> m_fpList;
	public HashMap<String, Integer> m_colIndexLookup;
	public SessionManager m_session;
	public ArrayList<Integer> m_colsInGroupBy = null; //Int = index in the fpList, bool = true => asc, else desc
	public ArrayList<Integer> m_allColsInGroupBy = null;//if !g_rollupAtJava (as is the case) then will be same as m_colsInGroupBy
	public ProcessShowResult m_processShowResult = null;
	
	 //effectively these are the columns across which we do group by in Query
	public SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
	public ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult = null;
	public ArrayList<VirtualHelper> virtualHelper = null;
	private boolean m_useCache = false;
	public ArrayList<Pair<Integer,Long>> m_vehicleList = null;
	public CurrCacheGrouper m_grouper = null;
    private int m_vehicleIndex = -1;
    private int m_vehicleCount = 0;
	public static class FormatHelper {	
    	public ArrayList<DimConfigInfo> m_uProfileList;
    	public ArrayList<DimConfigInfo> m_sProfileList;
    	public ArrayList<DimConfigInfo> m_fProfileList; //currently not used	
    	public int m_fFormatSelected; //the formatting entry in d50560
    	public ArrayList<FmtI.AllFmt> formatters = null; // initialized and populated in getFormattersAndMultScale
    	public ArrayList<com.ipssi.gen.utils.Pair<Double, Double>> multScaleFactors = null; // initialized and populated in getFormattersAndMultScale
    	public FmtI.AllFmt getFormatter(int index) {
    		return formatters == null || index < 0 || index >= formatters.size() ? null : formatters.get(index);
    	}
    }
    public FormatHelper formatHelper = null;
    
	private static final int g_prevRowsCount = 1;
	private static final int g_nextRowsCount = 1;	
	private ArrayList<ArrayList<Value> >m_rows = new ArrayList<ArrayList<Value>> ();	
	private ArrayList<Boolean> m_rowsValidity = new ArrayList<Boolean>();
	private boolean m_rsetIsClosed = false;//if last row has been read
	boolean m_firstRowRead = false;
	private ArrayList<Integer> m_colIndexUsingExpr = null;
	private ArrayList<ArrayList<Value> > m_prevValOfRequiringExpr = null; //initialized in ResultInfo
	private ArrayList<ArrayList<Value> > m_currValOfRequiringExpr = null; //initialized in ResultInfo

	//for rollupInJava
	private boolean doRollupInJava = false;
	private ArrayList<Integer> groupByRollupAggIndicator = null; //0 => agg, 1 => group by, 2 => rollup
	private ArrayList<ArrayList<Value>> rollupTot = new ArrayList<ArrayList<Value>>();
	private ArrayList<Integer> rollupIndices = null;//calculated ... but set up in init
	private int earliestRollupColDiff =0;// groupByRollupAggIndicator.size();
	private int currentIndexInRollupIndices = 0;;//rollupIndices.size();
	int lastSubGroupIndexPrinted = 0;//rollupIndices.size();
	private void subTotalInJavaInitRollupHolder() {
		if (!doRollupInJava)
			return;
		rollupIndices = new ArrayList<Integer>();
		for (int i=0,is=groupByRollupAggIndicator == null ? 0 : groupByRollupAggIndicator.size(); i<is;i++)
			if (groupByRollupAggIndicator.get(i) == 2)
				rollupIndices.add(i);
		int numRollup = rollupIndices.size();
		int numCols = m_fpList.size();
		rollupTot = new ArrayList<ArrayList<Value>>();
		for (int i=0;i<numRollup;i++) {
			ArrayList<Value> valList = new ArrayList<Value>();
			for (int j=0;j<numCols;j++) {
				valList.add(null);
			}
			rollupTot.add(valList);
		}
		earliestRollupColDiff = groupByRollupAggIndicator.size();
		currentIndexInRollupIndices = rollupIndices.size();
		lastSubGroupIndexPrinted = rollupIndices.size();
		if (rollupIndices.size() == 0)
			currentIndexInRollupIndices = -1;
	}
	public boolean nextRowRollupInJava() throws Exception {
		//By design: the row that caller sees after next will be  be in m_rows.get(g_prevRowsCount) (so long as that is valid - and if it is not valid, then that means
		// there are no more rows (equivalent of .next() is false)

		//curr row is the data row from resultSet that has been sent to the caller
		//nextRow is the next Data Row from resultset
		//Now when next is called - based on next Data row's difference with the curr row, we will have to first pump out the totaled rows
		// and once done with totaled rows, we then move the next Data row to curr - as noted above for the caller to see the 'row' that is
		//to be seen after next, that row has to be in m_rows.get(g_prevRowsCount) and must be valid

		//So how does nextInJava work:
		//we keep two variables (actually three - but the last is for book keeping/totaling purpose)
		//earliestRollupColDiff - when we get nextRow from resultset we compare against currRow and see the earliestRollupCol where it differes
		//   then when totalling we will have to print total for each of rollups that are at this or later
		//currentIndexInRollupIndices - once we know the earliestRollupColDiff, we iterate 
		// backwars in rollupIndices and see if we get dimConfigIndex that is greater or equal to earliest - 
		// if so the current total for that rollupindex is the row to be set for next
		if (m_rsetIsClosed && currentIndexInRollupIndices < 0)
			return false;
		boolean retval = false;
		if (rollupIndices.size() > 0) {
			if (currentIndexInRollupIndices >= rollupIndices.size()) {//get the next Row and get the latestNonAggColDiff
				ArrayList<Value> currDataRow = peek(0);
				ArrayList<Value> nextDataRow = peek(1);
				if (currDataRow == null) {//not to check for totalling
					earliestRollupColDiff = groupByRollupAggIndicator.size(); 
				}
				else {
					earliestRollupColDiff = nextDataRow == null ? 0 : subTotalInJavaGetDiff(currDataRow, nextDataRow,  rollupIndices.get(rollupIndices.size()-1));
				}
				lastSubGroupIndexPrinted = rollupIndices.size();
			}
			
			for (currentIndexInRollupIndices--; currentIndexInRollupIndices>=0; currentIndexInRollupIndices--) {
				int currRollupDimConfigIndex = rollupIndices.get(currentIndexInRollupIndices);
				if (currRollupDimConfigIndex >= earliestRollupColDiff) {//need to print Row ..
					lastSubGroupIndexPrinted = currentIndexInRollupIndices;
					subTotalInJavaPrepForPrintOfIndex(currentIndexInRollupIndices, rollupIndices.get(currentIndexInRollupIndices));
					ArrayList<Value> temp = m_rows.get(g_prevRowsCount);
					m_rows.set(g_prevRowsCount, m_rows.get(g_prevRowsCount-1));
					m_rows.set(g_prevRowsCount-1, temp);
					copyRow(rollupTot.get(currentIndexInRollupIndices), m_rows.get(g_prevRowsCount));
					return m_rowsValidity.get(g_prevRowsCount);
					//add row before currRow & print with value
					//GeneralizedQueryBuilder.subTotalInJavaPrintRow(table, currRollupDimConfigIndex, prevRowIndex, addAfter, rollupGroupTot.get(i), qp.groupByRollupAggIndicator, dimConfigList, formatHelper, session, sdf, conn);
				}	
				else {
					currentIndexInRollupIndices = -1;
					break;
				}
			}
			
			if (lastSubGroupIndexPrinted < rollupIndices.size()) 
				subTotalInJavaResetRollupHolder( lastSubGroupIndexPrinted);
		}
		else {
			peek(1);
		}
		//make peeked row as curr
		ArrayList<Value> firstRow = m_rows.get(0);
		for (int i=1,is=m_rows.size();i<is;i++) {
			m_rows.set(i-1, m_rows.get(i));
			m_rowsValidity.set(i-1, m_rowsValidity.get(i));
		}
		m_rows.set(m_rows.size()-1,firstRow);
		m_rowsValidity.set(m_rowsValidity.size()-1, false);
		retval = m_rowsValidity.get(g_prevRowsCount);
		if (retval) {
			processForCalcValues();
			subTotalInJavaDoCumm(m_rows.get(g_prevRowsCount));
			m_firstRowRead = true;
			currentIndexInRollupIndices = rollupIndices.size() == 0 ? -1 : rollupIndices.size();
		}
		return retval;
	}

	private void subTotalInJavaResetRollupHolder(int initFromIncl) {
		for (int i=initFromIncl, is = rollupTot == null ? 0 : rollupTot.size();i<is;i++) {
			ArrayList<Value> valList = rollupTot.get(i);
			for (int j=0, js = valList.size();j<js;j++) {
				valList.set(j, null);
			}
		}
	}
	private void subTotalInJavaDoCumm( ArrayList<Value> row) {
		for (int i=0, is = rollupTot == null ? 0 : rollupTot.size();i<is;i++) {
			ArrayList<Value> valList = rollupTot.get(i);
			for (int j=0, js = valList.size();j<js;j++) {
				Value rhs = row.get(j);
				if (rhs == null || rhs.isNull())
					continue;
				if (groupByRollupAggIndicator.get(j) != 0) {
					ResultInfo.setValInRow(valList, j, new Value(rhs));
					continue;
				}
				Value lhs = valList.get(j);
				if (lhs == null || lhs.isNull())
					ResultInfo.setValInRow(valList, j, new Value(rhs));
				else {
					lhs.applyOp(DimConfigInfo.ExprHelper.CalcFunctionEnum.CUMM, rhs);
				}
			}
		}
	}

	private int subTotalInJavaGetDiff(ArrayList<Value> prevDataRow, ArrayList<Value> currDataRow, int lookFrom) {
		 for (int i=0;i<=lookFrom;i++) {
			 if (groupByRollupAggIndicator.get(i) == 2) {
				 Value prev = prevDataRow.get(i);
				 Value curr = currDataRow.get(i);
				 boolean prevNull = prev == null || prev.isNull();
				 boolean currNull = curr == null || curr.isNull();
				 if (prevNull && currNull)
					 continue;
				 else if (((prevNull && !currNull) || (!prevNull && currNull) || !prev.equals(curr))) {
					 return i;
				 }
			 }
		 }
		 return groupByRollupAggIndicator.size();
	}
	private void subTotalInJavaPrepForPrintOfIndex(int ofGroupIndex, int colIndexIncl) {
		ArrayList<Value> valList = rollupTot.get(ofGroupIndex);
		for (int i=colIndexIncl,is=valList.size();i<is;i++) {
			if (groupByRollupAggIndicator.get(i) != 0)
				valList.set(i, null);
		}
	}
	//end rollup in java
	private static void clearRow(ArrayList<Value> row) {
		for (int i=0,is=row.size();i<is;i++) {
			Value val = row.get(i);
			if (val == null) {
			}
			else if (val.m_type == Cache.INTEGER_TYPE)
				val.m_iVal = Misc.getUndefInt();
			else if (val.m_type == Cache.NUMBER_TYPE)
				val.m_dVal = Misc.getUndefDouble();
			else if (val.m_type == Cache.DATE_TYPE)
				val.m_dateVal = 0;
			else
				val.m_strVal = null;
		}
	}
	
	private static void copyRow(ArrayList<Value> fromRow, ArrayList<Value> toRow) {
		for (int i=0,is=fromRow.size();i<is;i++) {
			Value fromVal = fromRow.get(i);
			Value toVal = toRow.get(i);
			if (toVal == null) {
				toVal = fromVal == null ? null : new Value(fromVal);
				toRow.set(i, toVal);
			}
			else if (fromVal == null) {
				toRow.set(i, null);
			}
			else if (fromVal.m_type == Cache.INTEGER_TYPE)
				toVal.m_iVal = fromVal.m_iVal;
			else if (fromVal.m_type == Cache.NUMBER_TYPE)
				toVal.m_dVal = fromVal.m_dVal;
			else if (fromVal.m_type == Cache.DATE_TYPE)
				toVal.m_dateVal = fromVal.m_dateVal;
			else
				toVal.m_strVal = fromVal.m_strVal;
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
	
	public ResultInfo(ArrayList<DimConfigInfo> fpList, HashMap<String, Integer> colIndexLookup, ResultSet rs, SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper, ArrayList<Integer> colsInGroupBy, ArrayList<Integer> colIndexUsingExpr, FormatHelper formatHelper, ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult, ArrayList<VirtualHelper> virtualHelper, ArrayList<Integer> allColsInGroupBy, ProcessShowResult processShowResult, ArrayList<Integer> groupByRollupAggIndicator, boolean doRollupInJava) {
		init(fpList, colIndexLookup, rs, session, searchBoxHelper, colsInGroupBy, colIndexUsingExpr, formatHelper, virtualResult, virtualHelper,null, null, allColsInGroupBy, processShowResult, groupByRollupAggIndicator, doRollupInJava);
	}
	public ResultInfo(ArrayList<DimConfigInfo> fpList, HashMap<String, Integer> colIndexLookup, ResultSet rs, SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper, ArrayList<Integer> colsInGroupBy, ArrayList<Integer> colIndexUsingExpr, FormatHelper formatHelper, ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult, ArrayList<VirtualHelper> virtualHelper,CurrCacheGrouper grouper, ArrayList<Integer> allColsInGroupBy, ProcessShowResult processShowResult, ArrayList<Integer> groupByRollupAggIndicator, boolean doRollupInJava) {
		init(fpList, colIndexLookup, rs, session, searchBoxHelper, colsInGroupBy, colIndexUsingExpr, formatHelper, virtualResult, virtualHelper, null, grouper, allColsInGroupBy, processShowResult, groupByRollupAggIndicator, doRollupInJava);
	}
	public ResultInfo(ArrayList<DimConfigInfo> fpList, HashMap<String, Integer> colIndexLookup, ResultSet rs, SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper, ArrayList<Integer> colsInGroupBy, ArrayList<Integer> colIndexUsingExpr, FormatHelper formatHelper, ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult, ArrayList<VirtualHelper> virtualHelper,ArrayList<Pair<Integer,Long>> vehicleList, ArrayList<Integer> allColsInGroupBy, ProcessShowResult processShowResult, ArrayList<Integer> groupByRollupAggIndicator, boolean doRollupInJava) {
		init(fpList, colIndexLookup, rs, session, searchBoxHelper, colsInGroupBy, colIndexUsingExpr, formatHelper, virtualResult, virtualHelper,vehicleList, null, allColsInGroupBy, processShowResult, groupByRollupAggIndicator, doRollupInJava);
	}
	public void init(ArrayList<DimConfigInfo> fpList, HashMap<String, Integer> colIndexLookup, ResultSet rs, SessionManager session, MiscInner.SearchBoxHelper searchBoxHelper, ArrayList<Integer> colsInGroupBy, ArrayList<Integer> colIndexUsingExpr, FormatHelper formatHelper, ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult, ArrayList<VirtualHelper> virtualHelper,ArrayList<Pair<Integer,Long>> vehicleList, CurrCacheGrouper grouper, ArrayList<Integer> allColsInGroupBy, ProcessShowResult processShowResult, ArrayList<Integer> groupByRollupAggIndicator, boolean doRollupInJava) {
		this.doRollupInJava = doRollupInJava;
		m_rs = rs;
		m_fpList = fpList;
		m_processShowResult = processShowResult;
		m_colIndexLookup = colIndexLookup;
		m_session = session;			
		m_colsInGroupBy = colsInGroupBy;
		this.virtualResult = virtualResult;
		this.virtualHelper = virtualHelper;
		this.m_allColsInGroupBy = allColsInGroupBy;
		m_useCache = grouper != null || (vehicleList != null && vehicleList.size() > 0);
		if(m_useCache){
			m_grouper = grouper;
			m_vehicleList = vehicleList;
			m_vehicleCount = vehicleList == null ? grouper == null ? 0 : grouper.getSize() : vehicleList.size();
		}
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
	    this.groupByRollupAggIndicator = groupByRollupAggIndicator;
	    if (this.groupByRollupAggIndicator == null || this.groupByRollupAggIndicator.size() == 0) {
	    	this.groupByRollupAggIndicator = new ArrayList<Integer>();
	    	for (int i=0,is=m_fpList.size();i<is;i++)
	    		this.groupByRollupAggIndicator.add(0);
	    }
	    this.subTotalInJavaInitRollupHolder();
	}
	public static void setValInRow(ArrayList<Value> row, int index, Value v) {
		row.set(index, v);
	}
	public void readRow(int index) throws Exception { //do related calculations in this. calc values will not be set ... instead these will be set in processForCalc, which needs to be called after processForGrouping
		ArrayList<Value> row = m_rows.get(index);		
		Connection conn = m_session.getConnection();
		Cache cache = m_session.getCache();
		//do copy etc if any. in this also do any calculations if desired
		
		if (m_useCache) {
			if (m_grouper != null) {
				ArrayList<Value> gr = m_grouper.getRow(m_vehicleIndex);
				if (gr == null) {
					for (int i=0,is=m_fpList.size();i<is;i++) {//must have all the calculations etc
						row.set(i, null);
					}
				}
				else {
					for (int i=0,is=gr.size(); i<is;i++)
						row.set(i, gr.get(i));
				}
			}
			else {
				int vId = Misc.getUndefInt();
				Pair<Integer, Long> vehEntry = m_vehicleList == null || m_vehicleIndex < 0 || m_vehicleIndex >= m_vehicleList.size() ? null :
					m_vehicleList.get(m_vehicleIndex);
				vId = vehEntry != null ? vehEntry.first : Misc.getUndefInt();
				CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vId, conn);
				VehicleExtendedInfo vehicleExt = VehicleExtendedInfo.getVehicleExtended(conn, vId);
				DriverExtendedInfo driverExt = null;//DEBUG13 maby OOM on MPL DriverExtendedInfo.getDriverExtendedByVehicleId(conn, vId);
				TPRLatestCache latestTPR = TPRLatestCache.getLatest(vId);
				VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vId, false, false);
				if (vehSetup == null || vdf == null) {
					for (int i=0,is=m_fpList.size();i<is;i++) {//must have all the calculations etc
						row.set(i, null);
					}
				}
				
				VehicleControlling vehicleControlling = NewProfileCache.getOrCreateControlling( vId);
				StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
				synchronized (vdf) {
					for (int i=0,is=m_fpList.size();i<is;i++) {//must have all the calculations etc
						int dimId = Misc.getUndefInt();
						DimConfigInfo dci = m_fpList.get(i);
						int attribType = dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.getAttribType();
						if(dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
							dimId = dci.m_dimCalc.m_dimInfo.m_id;
						Value cvalItem = getCachedValueWithHack(conn,m_session, vId, dimId, vehSetup, vdf, vehicleExt, driverExt, m_grouper == null ? null : m_grouper.getLoadFromDBVals(),latestTPR, stopDirControl); 
						if (attribType == Cache.STRING_TYPE) {
							if(cvalItem != null) 
								setValInRow(row, i, new Value(cvalItem.getStringVal()));
							else
								setValInRow(row, i, new Value((String) null));
						}
						else if (attribType == Cache.INTEGER_TYPE) {
							if(cvalItem != null)
								setValInRow(row, i, new Value(cvalItem.getIntVal()));
							else
								setValInRow(row, i, new Value(Misc.getUndefInt()));
						}
						else if (attribType == Cache.NUMBER_TYPE) {
							if(cvalItem != null)
								setValInRow(row, i, new Value(cvalItem.getDoubleVal()));
							else
								setValInRow(row, i, new Value(Misc.getUndefDouble()));
						}
						else if (attribType == Cache.DATE_TYPE) {
							if(cvalItem != null)
								setValInRow(row, i, new Value(cvalItem.getDateVal()));
							else
								setValInRow(row, i, new Value((Date)null));

						}
						else {
							int val = cvalItem != null ? cvalItem.getIntVal() : Misc.getUndefInt();
							if (attribType == Cache.LOV_TYPE) {
								val = cache.getParentDimValId(conn, dci.m_dimCalc.m_dimInfo, cvalItem != null ? cvalItem.getIntVal() : Misc.getUndefInt());
							}
							setValInRow(row, i, new Value(val));
						}
					}//for each dim
				} //sunc
			}
		}//if doing cache
		else {
			for (int i=0,is=m_fpList.size();i<is;i++) {//must have all the calculations etc
				int dimId = Misc.getUndefInt();
				DimConfigInfo dci = m_fpList.get(i);
				int attribType = dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null ? Cache.STRING_TYPE : dci.m_dimCalc.m_dimInfo.getAttribType();
				VirtualHelper vhelper = virtualHelper == null ? null : virtualHelper.get(i);
				if(dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null)
					dimId = dci.m_dimCalc.m_dimInfo.m_id;

				if (vhelper == null || !vhelper.isVirtual) {
					if (attribType == Cache.STRING_TYPE) {
						String val = m_rs.getString(i+1);
						setValInRow(row,i,new Value(val));
					}
					else if (attribType == Cache.INTEGER_TYPE) {
						int val = Misc.getRsetInt(m_rs,i+1);
						setValInRow(row,i,new Value(val));
					}
					else if (attribType == Cache.NUMBER_TYPE) {
						double val = Misc.getRsetDouble(m_rs,i+1);
						setValInRow(row,i,new Value(val));
					}
					else if (attribType == Cache.DATE_TYPE) {					
						java.sql.Timestamp val = m_rs.getTimestamp(i+1);
						setValInRow(row,i,new Value(Misc.sqlToUtilDate(val)));
					}
					else {
						int val = Misc.getRsetInt(m_rs,i+1);
						if (attribType == Cache.LOV_TYPE) {
							val = cache.getParentDimValId(conn, dci.m_dimCalc.m_dimInfo, val);
						}
						setValInRow(row,i,new Value(val));
					}
				}
				else {
					int vehicleId = vhelper.look1 >= 0 ? Misc.getRsetInt(m_rs, vhelper.look1+1, index) : Misc.getUndefInt();
				    int tabIndex = vhelper.tabIndex;
				    java.util.Date gpsRecordTime = vhelper.look2 >= 0 ? Misc.sqlToUtilDate(m_rs.getTimestamp(vhelper.look2+1)) : null;
				   
				    VirtualVal lookupVal = new VirtualVal(gpsRecordTime);
				    MiscInner.Pair lookupKey = new MiscInner.Pair(vehicleId, i);
				    FastList<VirtualVal> list = virtualResult == null || virtualResult.get(tabIndex) == null ? null : virtualResult.get(tabIndex).get(lookupKey);
				    VirtualVal entry = list == null  ? null : gpsRecordTime == null ? list.get(0) : list.get(lookupVal);
				    Object oval = entry == null ? null : entry.getVal(vhelper.col, gpsRecordTime);
					if (attribType == Cache.STRING_TYPE) {
						String val = oval == null ? null : oval.toString();
						setValInRow(row,i,new Value(val));
					}
					else if (attribType == Cache.INTEGER_TYPE) {
						if (oval != null) {
							int dbg = 1;
							dbg++;
						}
						int val = oval == null ? Misc.getUndefInt() : oval instanceof Integer ? ((Integer)oval).intValue() : (int) Math.round(((Double)oval).doubleValue());
						setValInRow(row,i,new Value(val));
					}
					else if (attribType == Cache.NUMBER_TYPE) {
						double val = oval == null ? Misc.getUndefDouble() : ((Double)oval).doubleValue();
						setValInRow(row,i,new Value(val));
					}
					else if (attribType == Cache.DATE_TYPE) {
						row.get(i).setValue((Date)oval);
						setValInRow(row,i,new Value((Date)oval));
					}
					else {
						int val = oval == null ? Misc.getUndefInt() : (int) Math.round(((Double)oval).doubleValue());
						if (attribType == Cache.LOV_TYPE) {
							val = cache.getParentDimValId(conn, dci.m_dimCalc.m_dimInfo, val);
						}
						setValInRow(row,i,new Value(val));
					}
				}
			}			
		}
	}
	private ArrayList<Value> getCurrRow() {
		return m_rows.get(g_prevRowsCount);
	}
	private ArrayList<Value> getPrevRow() {
		return m_rows.get(g_prevRowsCount-1);
	}
	public boolean isCurrDifferentForSelDimIndex(ArrayList<Integer> exclThese) {
		return isDifferentForSelDimIndex(this.getPrevRow(), this.getCurrRow(), this.m_firstRowRead, this.m_allColsInGroupBy, exclThese);	
	}
	public static boolean isDifferentForSelDimIndex(ArrayList<Value> prevRow, ArrayList<Value> row,  boolean firstRowRead, ArrayList<Integer> allColsInGroupBy, ArrayList<Integer> dimIndexOfcummDontResetOver) {
		boolean retval = true;
		if (firstRowRead) {
			for (int i=0,is = allColsInGroupBy == null ? 0 : allColsInGroupBy.size(); i<is; i++) {
				//check if to excl based on dimIndexOfcummDontResetOver
				retval = false;
				int idx = allColsInGroupBy.get(i);
				boolean exists = false;
				for (int j=0,js = dimIndexOfcummDontResetOver == null ? 0 : dimIndexOfcummDontResetOver.size(); j<js; j++) {
					if (dimIndexOfcummDontResetOver.get(j) == idx) {
						exists  = true;
						break;
					}
				}
				if (exists)
					continue;
				Value prev = prevRow.get(idx);
				Value curr = row.get(idx);
				boolean prevIsNull = prev == null || prev.isNull();
				boolean currIsNull = curr == null || curr.isNull();
				if (prevIsNull && currIsNull)
					continue;
				if ((prevIsNull && !currIsNull) || (!prevIsNull && currIsNull) || !prev.equals(curr)) {
					retval = true;
					break;
				}
			}
		}
		return retval;
	}
	
	public void processForCalcValues() {
		int index = g_prevRowsCount;
		ArrayList<Value> row = m_rows.get(index);
		ArrayList<Value> prevRow = m_rows.get(index-1);
		if (this.m_processShowResult != null) {
			ArrayList<Pair<Integer, DimConfigInfo.ExprHelper.CalcFunctionEnum>> dimIndexWithCummAtJava = this.m_processShowResult.dimIndexWithDoJavaCumm;
			if (dimIndexWithCummAtJava != null && dimIndexWithCummAtJava.size() != 0 && !isDifferentForSelDimIndex(prevRow, row, m_firstRowRead, this.m_allColsInGroupBy, this.m_processShowResult.dimIndexOfcummDontResetOver)) {
				for (int i=0,is=dimIndexWithCummAtJava == null ? 0 : dimIndexWithCummAtJava.size(); i<is; i++) {
						//add prev to curr
					int idx = dimIndexWithCummAtJava.get(i).first;
					Value newCurr = row.get(idx);
					
					Value prevCurr = prevRow.get(idx);
					if (newCurr == null || newCurr.isNull())
						newCurr = prevCurr == null || prevCurr.isNull() ? null : new Value(prevCurr);
					else if (prevCurr != null && prevCurr.isNotNull()) {
						newCurr = new Value(newCurr);
						newCurr.applyOp(dimIndexWithCummAtJava.get(i).second, prevCurr);
					}
					row.set(idx, newCurr);
				}
			}
		}
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
				setValInRow(row,colIndex, new Value(valToAdd));
			}
			else {
				setValInRow(row,colIndex,new Value(prevRow.get(colIndex)));
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
		if (indexRelCurr <= 0 && !isValid) {
			return null; //not enough prev/curr row
		}
		if (m_rsetIsClosed) {
			return null;
		}
		if(m_useCache)
			m_rsetIsClosed = m_vehicleCount <= ++m_vehicleIndex;
		if(m_rs != null)
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
		if (this.doRollupInJava)
			return this.nextRowRollupInJava();
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
		if(m_useCache)
			retval = m_vehicleCount > ++m_vehicleIndex;
		if(m_rs != null)
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
	   // m_vehicleIndex++;
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
	 //below is for !doRollupInJava (query has with rollup)
	 
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
	private int m_currentlyShowingRollupFor;// = if (m_doRollup) then m_colsInGroupBy.size() else 0; initialized in ResultInfo
	
	
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
		 return doRollupInJava ? currentIndexInRollupIndices >= 0 && currentIndexInRollupIndices < rollupIndices.size() 
		 : m_inRollupMode;
	 }
	 
	 public int getCurrColBeingRolledUp() {
		 return doRollupInJava 
		 ? currentIndexInRollupIndices >= 0 && currentIndexInRollupIndices < rollupIndices.size() ? rollupIndices.get(currentIndexInRollupIndices) : -1 
		 :
			 m_colsInGroupBy == null || m_currentlyShowingRollupFor >= m_colsInGroupBy.size() ? -1 : m_colsInGroupBy.get(m_currentlyShowingRollupFor);
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
    			 if (v != null && v.isNotNull()) {
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
    		  if (v == null || !v.isNotNull()) {
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
		return index < 0 || index >= m_rows.get(g_prevRowsCount).size() ? null : m_rows.get(g_prevRowsCount).get(index);
	}
	
	public Value getVal(String str) {
		Integer index = this.m_colIndexLookup.get(str);
		return index == null ? null : getVal(index.intValue());
	}
	
	public Value getValDefaultAdjusted(int index) {
		Value retval = m_rows.get(g_prevRowsCount).get(index);
		if (retval != null && retval.isNotNull())
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
   	public String getValString(Value val, int index,  String nullValString) throws Exception {
   		Cache cache = m_session.getCache();
		Connection conn = m_session.getConnection();
	    String retval = null;
	    if (val == null || !val.isNotNull()) {
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
	    	retval = val == null ? nullValString : val.toString();
	    return retval;
   	}
   	
	public String getValStr(int index, boolean doDefaultAdjusted, boolean doProfileAdjusted, String nullValString) throws Exception { //TODO adjust for profile/format adjusted
		Cache cache = m_session.getCache();
		Connection conn = m_session.getConnection();
	    String retval = null;
	    Value val = doDefaultAdjusted ? getValDefaultAdjusted(index) : getVal(index);
	    if (val == null || !val.isNotNull()) {
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
	    	retval = val == null ? nullValString : val.toString();
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
						FmtI.Number numfmt = new FmtI.Number(locale, unit, numAfterDec, scaleprofile.m_minDecimal);
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
	public static class VirtualVal implements Comparable<VirtualVal> {

		public Date time = null;
		public VirtualVal(Date time) {
			this.time = time;
		}

		public int compareTo(VirtualVal rhs) {
			return time.compareTo(rhs.time);
		}	

		public Object getVal(String name, Date ts) {
			return null;
		}

		public  Object getVal(String name) {
			return getVal(name, null);
		}
	}
	public static class VirtualGpsData extends VirtualVal {		
		public double v = Misc.getUndefDouble();
		public double rawV = Misc.getUndefDouble();
		public double odoDay = Misc.getUndefDouble();
		public double odoWeek = Misc.getUndefDouble();
		public double odoMonth = Misc.getUndefDouble();
		public VirtualGpsData(Date recTime, double val, double rawV, double odoDay, double odoWeek, double odoMonth) {
			super(recTime);		
			v = val;
			this.rawV = rawV;
			this.odoDay = odoDay;
			this.odoWeek = odoWeek;
			this.odoMonth = odoMonth;
		}

		public Object getVal(String name, Date ts) {
			if (name.equals("bin_val")) {
				return new Integer( v >= 0.5 ? 1 : 0); 
			}
			else if (name.equals("attribute_value")){
				return new Double(v);
			}
			else if (name.equals("speed")) {
				return new Double(rawV);
			}
			else if (name.equals("odometer_day")) {
				Date dt = new Date();
				TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_DAY);
				return new Double(this.time != null && this.time.before(dt) ? Misc.getUndefDouble() : odoDay);
			}
			else if (name.equals("odometer_week")) {
				Date dt = new Date();
				TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_WEEK);
				return new Double(this.time != null && this.time.before(dt) ? Misc.getUndefDouble() : odoWeek);
			}
			else if (name.equals("odometer_month")) {
				Date dt = new Date();
				TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_MONTH);
				return new Double(this.time != null && this.time.before(dt) ? Misc.getUndefDouble() : odoMonth);
			}
			else if (name.startsWith("gap_")) {
				double cv = odoWeek;
				double val = v;
				Date dt = new Date();
				
				if (name.endsWith("speed")) {
					val = rawV;
				}
				if (name.startsWith("gap_day_")) {
					cv = odoDay;
			        TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_DAY);
				}
				else if (name.startsWith("gap_month_")) {
					cv = odoMonth;
			        TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_MONTH);
				}
				else if (name.startsWith("gap_inverted_day_")) {
					TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_DAY);
				}
				else {
			        TimePeriodHelper.setBegOfDate(dt, Misc.SCOPE_WEEK);
				}
				if (this.time != null && this.time.before(dt))
					cv = Misc.getUndefDouble();
				if (Misc.isUndef(cv) || Misc.isUndef(val))
					return new Double(Misc.getUndefDouble());
				else if (cv > val)
					return new Double(0);
				else if (name.startsWith("gap_inverted_day_")) { // for engine off dur 
					cv = odoDay;
					double currDayMinutes = (double) ((System.currentTimeMillis() - dt.getTime())/(1000*60*60*1.0));
					return new Double(currDayMinutes-val+cv);
				}
				else
					return new Double(val-cv);
			}
			return null;
		}		

		public Object getVal(String name) {
			return getVal(name, null);
		}

		public String toString() {
			return "("+time.toString()+","+v+")";
		}

	}

	public static class VirtualEventData extends VirtualVal {
		public static HashMap<String, Integer> colLookup = new HashMap<String, Integer>();
		static {
			//vehicle_id, time stamp, on_off_<RULE_ID>,start_<RULE_ID>. end_<RULE_ID>, id_<RULE_ID>, begin_name_<RULE_ID>, end_name_<RULE_ID> ....
			colLookup.put("on_off", -1);
			colLookup.put("since_or_ended", -2); //if ended then end else start
			colLookup.put("start",0);
			colLookup.put("end", 1);
			colLookup.put("id", 2);
			colLookup.put("begin_name", 3);
			colLookup.put("end_name", 4);	
			colLookup.put("addnl_value1", 5);
		}
		ArrayList<Object> vals = new ArrayList();
		public String toString() {
			return "("+vals.get(0)+","+vals.get(1)+","+vals.get(2)+","+vals.get(3)+","+vals.get(4)+","+vals.get(5)+")";
		}
		public VirtualEventData(Date start, Date end, int id, String beginName, String endName, double addnlVal) {
			super(start);
			vals.add(start);
			vals.add(end);
			vals.add(new Integer(id));
			vals.add(beginName);
			vals.add(endName);	
			vals.add(new Double(addnlVal));
		}
		public Object getVal(String name, Date ts) {
			Integer index = colLookup.get(name);
			if (index == null)
				return null;
			int indexInt = index.intValue();
			if (indexInt == -1) {
				Date st = (Date) vals.get(0);
				Date en = (Date) vals.get(1);
				if ((ts == null && en == null) || 
						( (ts.after(st) || ts.equals(st)) && (en == null || ts.before(en)) )
						)
					return new Integer(1);
				else
					return new Integer(0);				
			}
			else if (indexInt == -2) {
				Date st = (Date) vals.get(0);
				Date en = (Date) vals.get(1);
				return en == null ? st : en;
			}
			else return vals.get(indexInt);			
		}
		public Object getVal(String name) {
			return getVal(name, null);
		}	
	}
	public static class VirtualHelper { //keeps info to help with virtual table calc
		public boolean isVirtual = false;
		public int tabIndex = 0; //0=loggged_data, 1=current_data, 2=engine_events, 3=latest_engine_events
		public String col = null;	
		public int look1 = -1;
		public int look2 = -1;
	}
	public static Value getETARelatedHack(Connection conn, int vehicleId, int dimId, CacheTrack.VehicleSetup vehSetup, VehicleDataInfo vdf) {//DEBUG13 move to CacheValue ... to accomodate errors/flex
		//will return null to indicate that it has nothing to do with ETA ... so get it elsewhere
		Value retval = null;
		switch (dimId) {//first check which dims are handled .. for others return null
		case 396://exp eta
		case 397://curr eta
		case 398://act eta
		case 399://Exp/Act
		case 20348://curr/Act eta

		case 21417: //jde location
		case 21444: //MRS to loc
		case 21443://from loc
		case 21162: //transit delays
		case 21164://on way
		case 21165://delayed
		case 21448://on way back
		case 21163://MPL dist to dest
		case 21445: //dist to dest
		case 21446: //dist travelled
		case 21447://dist remaining
		case 21161://MPL dist remaining
		case 22077:// Orient Customer name
		case 95043://Transit Time
			break;
		default:
			return null;
		}
		//sigh ... need to put try catch
		int ty = Cache.INTEGER_TYPE;
		NewVehicleETA vehicleETA = null;
		NewETAforSrcDestItem specificETA = null;
		try {
			vehicleETA = Misc.getSERVER_MODE() != 1 ? NewVehicleETA.getETAObj(conn, vehicleId) : null;
			specificETA = vehicleETA == null ? null : vehicleETA.getSpecificETA();
			String n = null;
			
			int iv = Misc.getUndefInt();
			long dt = Misc.getUndefInt();
			double dv = Misc.getUndefDouble();
			switch (dimId) {
				case 396://exp eta
					dt = specificETA == null || vehicleETA == null ? Misc.getUndefInt() : vehicleETA.getEstETA(specificETA);
					retval = new Value(dt);
					ty = Cache.DATE_TYPE;
					break;
				case 95043://auth transit time
					iv =specificETA != null && specificETA.getTransitTime() > 0 ? (int)( specificETA.getTransitTime() * 24*60) : Misc.getUndefInt();
					if (!Misc.isUndef(iv)) {
						LatestTripInfo tempLatest = LatestTripInfo.getLatestTripInfo(vehicleId);
						int fromOpId = tempLatest == null || tempLatest.getLoad() == null ? Misc.getUndefInt() : tempLatest.getLoad().getOfOpStationId(); 
						OpToOpETA optoOp = OpToOpETA.get(conn, fromOpId, Misc.getUndefInt());
						iv = optoOp == null ? 4*60 : (int) optoOp.getLoadLeadTime();
					}
					ty = Cache.INTEGER_TYPE;
					break;
				case 397://curr eta
					dt = specificETA == null || vehicleETA == null ? Misc.getUndefInt() : vehicleETA.getCurrETA(specificETA);
					if (dt <= 0) {
						LatestTripInfo tempLatest = LatestTripInfo.getLatestTripInfo(vehicleId);
						
						LUInfoExtract loadExt = tempLatest == null ? null : tempLatest.getLoad();
						LUInfoExtract unloadExt = tempLatest == null ? null : tempLatest.getUnload();
						long relTA = loadExt != null ? loadExt.getLatestEventDateTime() : Misc.getUndefInt();
						
						if (relTA <= 0)
							relTA = unloadExt != null ? unloadExt.getLatestEventDateTime() : Misc.getUndefInt();
						if (relTA > 0) {
							int fromOpId = tempLatest == null || tempLatest.getLoad() == null ? Misc.getUndefInt() : tempLatest.getLoad().getOfOpStationId(); 
							OpToOpETA optoOp = OpToOpETA.get(conn, fromOpId, Misc.getUndefInt());
							int transitTime = optoOp != null && optoOp.getLoadLeadTime() > 0 ? (int) optoOp.getLoadLeadTime() : 4*60;
							if (transitTime > 0) {
								dt = relTA + transitTime*60*1000;
							}
						}
					}

					retval = new Value(dt);
					ty = Cache.DATE_TYPE;
					break;
				case 398://act eta
					dt = vehicleETA == null ? Misc.getUndefInt() : vehicleETA.getCurrToInTime();
					retval = new Value(dt);
					ty = Cache.DATE_TYPE;
					break;
				case 399://Exp/Act diff
					if (vehicleETA != null && specificETA != null) {
						long dt2 = vehicleETA.getCurrETA(specificETA);
						long dt1 = vehicleETA.getCurrToInTime();
						if (dt1 <= 0) {
							dt1 = vehicleETA.getEstETA(specificETA);
						}
						long diffMilli = dt1-dt2;
						dv = diffMilli/(1000*60);
					}
					retval = new Value(dv);
					ty = Cache.NUMBER_TYPE;
					break;

				case 20348://curr/Act eta
					dt = vehicleETA == null ? Misc.getUndefInt() : vehicleETA.getCurrToInTime() > 0 ? vehicleETA.getCurrToInTime() : vehicleETA.getCurrETA(specificETA);
					retval = new Value(dt);
					ty = Cache.DATE_TYPE;
					break;

				case 21417: //jde location
				case 21444: //t
					n = vehicleETA == null ? null : vehicleETA.getTo(conn, (byte)(dimId == 21417 ? 1 : 0));
					retval = new Value(n);
					ty = Cache.STRING_TYPE;
					break;
				case 21443://from loc
					n = vehicleETA == null ? null : vehicleETA.getFrom(conn, (byte)0);
					retval = new Value(n);
					ty = Cache.STRING_TYPE;
					break;
				case 21162:
				case 21164://on way
				case 21165:
				case 21448://on way back
					iv = vehicleETA == null ? 0 : vehicleETA.isDelayed(conn, specificETA);
					retval = new Value(iv);
					ty = Cache.INTEGER_TYPE;
					break;
				case 21163:
				case 21445: //dist to dest
					dv = specificETA == null ? Misc.getUndefDouble() : specificETA.getTransitDist();
					retval = new Value(dv);
					ty = Cache.NUMBER_TYPE;
					break;
				case 21446: //dist travelled
					NewVehicleData vdp = vdf.getDataList(conn, vehicleId, 0, true);
					dv = vehicleETA == null ? Misc.getUndefDouble() : vehicleETA.calcDistTravelled(conn, vdp, null);
					retval = new Value(dv);
					ty = Cache.NUMBER_TYPE;
					break;
				case 21447://dist remaining
				case 21161:
					vdp = vdf.getDataList(conn, vehicleId, 0, true);
					dv = vehicleETA == null ? Misc.getUndefDouble() : vehicleETA.calcDistRemaining(specificETA, conn, vehSetup, vdp, null, Misc.getUndefDouble());
					retval = new Value(dv);
					ty = Cache.NUMBER_TYPE;
					break;
				case 22077://Customer name as per Orient
					n = vehicleETA == null ? null : vehicleETA.getCustomer(conn, vehicleId);
					retval = new Value(n);
					ty = Cache.STRING_TYPE;
					break;
			}
		}
		catch (Exception e) {
			System.out.print("CCACHE ERR ETA"+vehicleId);
			e.printStackTrace();
		}
		finally {
			if (retval == null) {
				retval= ty == Cache.INTEGER_TYPE ? new Value(Misc.getUndefInt())
				: ty == Cache.STRING_TYPE ? new Value((String) null) 
				: new Value(Misc.getUndefDouble())
				;
				try {
					//debugging messages
						StringBuilder sb = new StringBuilder();
						sb.append("[ETA DBG:").append(vehicleId).append("] vehicleETA:").append(vehicleETA == null ? "null" : Integer.toString(vehicleETA.getCurrPossibleSrcDestList() == null || vehicleETA.getCurrPossibleSrcDestList().size() == 0? Misc.getUndefInt() : vehicleETA.getCurrPossibleSrcDestList().get(0).first)).append(" specificETA:").append(specificETA == null ? "null" : Integer.toString(specificETA.getSrcDestId()));
						System.out.println(sb);
						sb.setLength(0);
						if (vehicleETA != null) {
						sb.append("[ETA DBG:").append(vehicleId).append("] From:").append(vehicleETA.getCurrFromOpStationId()).append("/").append(vehicleETA.getCurrFromOpStationInTime() > 0 ? (new java.util.Date(vehicleETA.getCurrFromOpStationInTime())).toString() : "NoIn").append(" ChTo:");
						ChallanInfo ch = NewVehicleETA.getChallanInfo(conn, vehicleId, vehicleETA.getCurrSrcDestChallanDate());
						if (ch != null)
							sb.append(new java.util.Date(ch.getChallanDate())).append(",").append(ch.getFromLoc()).append(",").append(ch.getToLoc());
						System.out.println(sb);
						sb.setLength(0);
						}
						sb.append(vehicleETA);
						System.out.println(vehicleETA);
					}
					catch (Exception e) {
						
					}
			}
			
		}
		return retval;
	}
	public static long getLUInfoExtEventTime(LUInfoExtract liExt, String seq){
		if(liExt == null)
			return Misc.getUndefInt();
		
		if("0".equalsIgnoreCase(seq))
			return liExt.getWaitIn();
		else if("1".equalsIgnoreCase(seq))
			return liExt.getGateIn();
		else if("2".equalsIgnoreCase(seq))
			return liExt.getAreaIn();
		else if("3".equalsIgnoreCase(seq))
			return liExt.getAreaOut();
		else if("4".equalsIgnoreCase(seq))
			return liExt.getGateOut();
		else if("5".equalsIgnoreCase(seq))
			return liExt.getWaitOut();
		return Misc.getUndefInt();
	}
	
	public static int getMaxLUInfoExtEventTime(LUInfoExtract liExt){
		if(liExt == null)
			return Misc.UNDEF_VALUE;
		
		if(liExt.getWaitOut() > 0)
			return 5;
		else if(liExt.getGateOut() > 0)
			return 4;
		else if(liExt.getAreaOut() > 0)
			return 3;
		else if(liExt.getAreaIn() > 0)
			return 2;
		else if(liExt.getGateIn() > 0)
			return 1;
		else if(liExt.getWaitIn() > 0)
			return 0;
		return Misc.UNDEF_VALUE;
	}
	
	public static Value getCachedValueWithHack(Connection conn,SessionManager session, int vId, int dimId, CacheTrack.VehicleSetup vehSetup, VehicleDataInfo vdf, VehicleExtendedInfo vehicleExt, DriverExtendedInfo driverExt, HashMap<String, Value> loadFromDBVals, StopDirControl stopDirControl) throws Exception { //slowly move the special cases to CacheValue .. but needed for flexibility
		return getCachedValueWithHack(conn,session, vId, dimId, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,null, stopDirControl);
	}
	public static int SECL_G_STOPRULEID = 1;
	public static int SECL_G_RD = 603;
	public static Value getHrsCategory(double gapHr, ArrayList<DimInfo.ValInfo> hrsCategory) {
		for (Iterator iterator = hrsCategory.iterator(); iterator.hasNext();) {
				DimInfo.ValInfo valInfo = (DimInfo.ValInfo) iterator.next();
				String stSeq = valInfo.m_str_field1;
				String edSeq = valInfo.m_str_field2;
				double stPt = Misc.getParamAsDouble(stSeq, -100000.0);
				double edPt = Misc.getParamAsDouble(edSeq, 100000.0);
				if ((gapHr >= stPt || Misc.isEqual(gapHr, stPt)) && (gapHr < edPt && !Misc.isEqual(gapHr, edPt))) {
					return new Value(valInfo.m_id);
				}
		}
		return new Value(Misc.getUndefInt());
	}
	
	public static Value getCachedValueWithHack(Connection conn,SessionManager session, int vId, int dimId, CacheTrack.VehicleSetup vehSetup, VehicleDataInfo vdf, VehicleExtendedInfo vehicleExt, DriverExtendedInfo driverExt, HashMap<String, Value> loadFromDBVals,TPRLatestCache latestTPR, StopDirControl stopDirControl) throws Exception { //slowly move the special cases to CacheValue .. but needed for flexibility
		
		Value etaVal = getETARelatedHack(conn, vId, dimId, vehSetup, vdf);
		if (etaVal != null)
			return etaVal;
		DimInfo dimInfo = DimInfo.getDimInfo(dimId);
		if (dimInfo != null && dimInfo.m_loadFromDB == 1) {
			Value v = loadFromDBVals == null ? null : loadFromDBVals.get(vId+"_"+dimId);
			if (v == null)
				v = new Value();
			return v;
		}
		
		if (dimId >= 45001 && dimId < 50000) {
			if (dimInfo != null) {
				Value retval = null;
				int attribDimId = dimId-40000;
				ColumnMappingHelper colmap = dimInfo.m_colMap;
				if (colmap != null && colmap.column != null ) {
					int temp = Misc.getParamAsInt(colmap.column.substring(colmap.column.lastIndexOf('_')+1));
					if (temp != attribDimId) {
						System.out.println("{LOCERROR] Incorrect dim for:"+dimId+" $ col:"+colmap.column);
					}
				}
				
				NewVehicleData vdp = vdf.getDataList(conn, vId, attribDimId, false);
				if (vdp != null) {
					GpsData last = vdp.getLast(conn);
					if (last != null) {
						double dv = last.getValue();
						if (dimInfo.m_type != Cache.NUMBER_TYPE) {
							retval = new Value(Math.round(dv));
						}
						else 
							retval = new Value(dv);
					}
				}
				return retval;
			}
			
		}
		if (dimInfo != null && (dimInfo.m_id >= 30190 && dimInfo.m_id <= 30193)) {
			//get latest challan
			LatestTripInfo tempLatest = LatestTripInfo.getLatestTripInfo(vId);
			String val = null;
			if (tempLatest != null) {
				ChallanInfo ch = tempLatest.getChallanInfo();
				Value tripLOPIdValue = getCachedValueWithHack(conn,session, vId, 35238, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				int tripLOPId = tripLOPIdValue == null ? Misc.getUndefInt() : tripLOPIdValue.getIntVal();
				OpStationBean lop = TripInfoCacheHelper.getOpStation(tripLOPId);
				if (ch != null) {
					val = dimInfo.m_id == 30190 ? ch.getStr4() : dimInfo.m_id == 30191 ? ch.getAlertPhoneL1Sender() : dimInfo.m_id == 30192 ? ch.getAlertPhoneL1Transporter() : ch.getAlertPhoneL1Customer();
				}
				if (val == null && lop != null) {
					val = dimInfo.m_id == 30190 ? null : dimInfo.m_id == 30191 ? lop.getAlertPhoneL1Sender() : dimInfo.m_id == 30192 ? lop.getAlertPhoneL1Transporter() : lop.getAlertPhoneL1Customer();
				}
			}
			return new Value(val);
		}
		{
			if (dimInfo != null && dimInfo.m_subsetOf == 123) {
				Cache cache = Cache.getCacheInstance(conn);
				MiscInner.PortInfo portInfo = cache.getParentPortNode(conn, vehSetup.m_ownerOrgId, dimInfo);
				return new Value(portInfo == null ? vehSetup.m_ownerOrgId : portInfo.m_id);
			}
		}
		//SECL related stuff
		LatestTripInfo tempLatest = LatestTripInfo.getLatestTripInfo(vId);
		Value trackingStatus = CacheValue.getValueInternal(conn, vId, 35219, vehSetup, vdf);
		int trackingStatusInt = trackingStatus == null || trackingStatus.isNotNull() ? 0 : trackingStatus.getIntVal();
		if (dimId == 75001) {//latest unload/load opid
			int relevantOpId = tempLatest != null ? (tempLatest.getUnload() !=null && tempLatest.getUnload().getEarliestEventDateTime() > 0 
					   ? tempLatest.getUnload().getOfOpStationId()
					   : tempLatest.getLoad() != null && tempLatest.getLoad().getEarliestEventDateTime() > 0 ? tempLatest.getLoad().getOfOpStationId() : Misc.getUndefInt())
					 : Misc.getUndefInt()
					 ;
			OpStationBean bean = TripInfoCacheHelper.getOpStation(relevantOpId);
			String val = bean == null ? null : bean.getOpStationName();
			return new Value(val);
		}
		else if (dimId == 75002) {//transit/inside/delayed
			Value tripLoadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			int tripLoadStatusInt = tripLoadStatusVal == null || tripLoadStatusVal.isNull() ? 1 : tripLoadStatusVal.getIntVal();
			
			int iv = trackingStatusInt <= 0 ? (tripLoadStatusInt == 1 || tripLoadStatusInt == 3 ? 0 : 1) : 2;
			return new Value(iv);
		}
		else if (dimId == 75003) {//exception rule name
			CacheValue.LatestEventInfo latestStop = CacheValue.getLatestEvent(vId, ResultInfo.SECL_G_STOPRULEID);
			CacheValue.LatestEventInfo latestRD = CacheValue.getLatestEvent(vId, ResultInfo.SECL_G_RD);
			boolean isStopped = latestStop != null && (latestStop.getEndTime() <= 0 && (System.currentTimeMillis() - latestStop.getStartTime()) > 15*60*1000);
			boolean isRD = latestRD != null && latestRD.getEndTime() <= 0;
			int val = 0;
			if (trackingStatusInt > 0)
				val =  100;
			else if (isStopped && isRD)
				val = 3;
			else if (isStopped)
				val = 1;
			else if (isRD)
				val = 2;
			else {
				Value etaTime = getCachedValueWithHack(conn,session, vId, 397, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if (etaTime != null && etaTime.isNotNull() && etaTime.getDateValLong() < System.currentTimeMillis())
					val = 4;
			}
			return new Value(val);
		}
		else if (dimId == 75004) {//exception rule name
			int val = 0;
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			if (latestCache == null)
				return getCachedValueWithHack(conn,session, vId, 75003, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			Value transitMinVal = getCachedValueWithHack(conn,session, vId, 95043, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			int transitMin = transitMinVal == null || transitMinVal.isNull()? 4*60 : transitMinVal.getIntVal();
			long outTime = latestCache.getUnloadTareTime() > 0 ? latestCache.getUnloadTareTime() : latestCache.getUnloadGrossTime() > 0 ? latestCache.getUnloadGrossTime() : latestCache.getLoadGrossTime() > 0 ? latestCache.getLoadGrossTime() : latestCache.getLoadTareTime();
			if (outTime > 0 && System.currentTimeMillis() > (outTime+transitMin*60*1000))
				val = 4;
			else {
				CacheValue.LatestEventInfo latestStop = CacheValue.getLatestEvent(vId, ResultInfo.SECL_G_STOPRULEID);
				CacheValue.LatestEventInfo latestRD = CacheValue.getLatestEvent(vId, ResultInfo.SECL_G_RD);
				boolean isStopped = latestStop != null && latestStop.getEndTime() <= 0;
				boolean isRD = latestRD != null && latestRD.getEndTime() <= 0;
			
				if (trackingStatusInt > 0)
					val =  100;
				else if (isStopped && isRD)
					val = 3;
				else if (isStopped)
					val = 1;
				else if (isRD)
					val = 2;
			}
			return new Value(val);
		}
		else if (dimId == 75005) {//isStopped
			CacheValue.LatestEventInfo latestStop = CacheValue.getLatestEvent(vId, ResultInfo.SECL_G_STOPRULEID);
			return new Value(latestStop != null && latestStop.getEndTime() <= 0 ? 1 : 0);
		}
		else if (dimId == 75006) {//isStopped
			CacheValue.LatestEventInfo latestRD = CacheValue.getLatestEvent(vId, ResultInfo.SECL_G_RD);
			return new Value(latestRD != null && latestRD.getEndTime() <= 0 ? 1 : 0);
		}
		else if (dimId == 75007) {
			Value etaTime = getCachedValueWithHack(conn,session, vId, 397, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			return new Value((etaTime != null && etaTime.isNotNull() && etaTime.getDateValLong() < System.currentTimeMillis()) ? 1 :0 );
		}
		
		
		//end SECl related stuff
			
		
		if (dimId == 95000) {// Plant Detention Definition
			// In Parking/WaitOut/GateIn etc
			ArrayList vListDef = DimInfo.getDimInfo(95000).getValList(conn,session);
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			if (latest != null && latest.getLoad() != null && latest.getUnload() == null) {
				LUInfoExtract lext = latest.getLoad();
				int mxEvt = getMaxLUInfoExtEventTime(lext);
				if (mxEvt < 5) {
					for (Iterator iterator = vListDef.iterator(); iterator.hasNext();) {
						DimInfo.ValInfo object = (DimInfo.ValInfo) iterator.next();
						String stSeq = object.m_str_field1;
						String edSeq = object.m_str_field2;
						int stPt = Misc.getParamAsInt(stSeq, 0);
						int edPt = Misc.getParamAsInt(edSeq,6);
						if (mxEvt <= edPt
								&& mxEvt >= stPt)
							return new Value(object.m_id);
					}
				}
			} 
			return new Value(Misc.getUndefInt());
		}
		
		
		if (dimId == 94996) {// Plant Detention Category HRS DIM 94996
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			if (latest != null && latest.getLoad() != null && latest.getUnload() == null) {
				LUInfoExtract lext = latest.getLoad();
				ArrayList hrsCategory = DimInfo.getDimInfo(94996).getValList(conn, session);
				long earliest = lext.getEarliestEventDateTime();
				long gap = earliest > 0 ? System.currentTimeMillis() - earliest : 0;
				double gapHr = (double)(gap)/(double)(60*60*1000);
				return getHrsCategory(gapHr, hrsCategory);
			}
			return new Value(Misc.getUndefInt());
		}
		//count95001 for PLANT
		if (dimId == 95001 || dimId == 95004 || dimId == 95003) {// Plant Detention Category HRS DIM 95001
			return new Value(1);			
		}
		
		
		//for Customer Detention
		if (dimId == 94999) {//Customer Name DIM 94999
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			if (latest != null) {
				LUInfoExtract load = latest.getLoad();
				LUInfoExtract unLoad = latest.getUnload();
				if(load!=null && unLoad!=null){//unload wait in true
					int maxSeqNo = getMaxLUInfoExtEventTime(unLoad);
					//0 = waitIn
					//5= waitOut
					if(maxSeqNo>=0 && maxSeqNo<5){
						ChallanInfo challan=latest.getChallanInfo();
						String custName=null;
						if(challan!=null){
							custName=challan.getTextInfo().getCustName();
							if(custName!=null)
								return new Value(custName);
						}
					}
				}
			}
		}//Customer Name End
		
		//for Customer Detention
		if (dimId == 94998) {//HRS Category DIM 94998
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			ArrayList hrsCategory = DimInfo.getDimInfo(94998).getValList(conn, session);;
			
			if (latest != null && latest.getUnload() == null) {
				return getHrsCategory(-75000, hrsCategory); //intransit
			}
			if (latest != null && latest.getUnload() != null) {
				LUInfoExtract unLoad = latest.getUnload();
				int maxSeqNo = getMaxLUInfoExtEventTime(unLoad);
					//0 = waitIn
					//5= waitOut
				if(maxSeqNo>=0 && maxSeqNo<5){
					long earliest = unLoad.getEarliestEventDateTime();
					long gap = earliest > 0 ? System.currentTimeMillis() - earliest : 0;
					double gapHr = (double)(gap)/(double)(60*60*1000);
					
					return getHrsCategory(gapHr, hrsCategory);
				}
			}
			return new Value(Misc.getUndefInt());
		}
		
/**
 * For Transit detention 
 */
		//count95002 for Transit Detention
		if (dimId == 94997) {// Transit Detention OnGoing Trips count
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			if (latest != null && latest.getLoad() != null && latest.getLoad().getGateOut() > 0) {
				Value currETA=getETARelatedHack(conn, vId, 397, vehSetup, vdf);
				Value expETA=getETARelatedHack(conn, vId, 396, vehSetup, vdf);
				Value actETA = getETARelatedHack(conn, vId, 398, vehSetup, vdf);
				
				long diffInETA=currETA == null || currETA.isNull() || expETA == null || expETA.isNull() ? 0 : expETA.getDateValLong() - currETA.getDateValLong();
				double gapHr = (double)diffInETA/(double)(60*60*1000);
				if ((actETA != null && actETA.isNotNull()) || (latest.getUnload() != null))
					gapHr = 75000; //Reached
				
				ArrayList hrsCategory = DimInfo.getDimInfo(94997).getValList(conn, session);
				return getHrsCategory(gapHr, hrsCategory);
			}
			return new Value(Misc.getUndefInt());
		}
		
		if (dimId == 95002) {// Transit Detention OnGoing Trips count
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			if (latest != null && latest.getLoad() != null && latest.getLoad().getGateOut() > 0 && latest.getUnload() == null) {
				return new Value(1);
			}
			return new Value(0);
		}
		
		

		
		
		Value cvalItem = CacheValue.getValueInternal(conn, vId, dimId, vehSetup, vdf, vehicleExt, driverExt);
		long now = System.currentTimeMillis();
		java.util.Date dt = new java.util.Date(now);
		dt.setDate(1);
		dt.setHours(0);
		dt.setMinutes(0);
		dt.setSeconds(0);
		long today = dt.getTime();
		switch (dimId) {
		
		// Related to Datefield1
		case 30170: {
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			long val = Misc.getUndefInt();
			if (latest != null && latest.getChallanInfo() != null && !Misc.isUndef(latest.getChallanInfo().getDateField1())) {
				val = latest.getChallanInfo().getDateField1();
			}
			cvalItem = new Value(val);
			break;
		}
		case 30171: {
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			long val = Misc.getUndefInt();
			if (latest != null && latest.getChallanInfo() != null && !Misc.isUndef(latest.getChallanInfo().getDateField2())) {
				val = latest.getChallanInfo().getDateField2();
			}
			cvalItem = new Value(val);
			break;
		}
		case 30172: {
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			long val = Misc.getUndefInt();
			if (latest != null && latest.getChallanInfo() != null && !Misc.isUndef(latest.getChallanInfo().getDateField3())) {
				val = latest.getChallanInfo().getDateField3();
			}
			cvalItem = new Value(val);
			break;
		}
		case 30173: {
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			long val = Misc.getUndefInt();
			if (latest != null && latest.getChallanInfo() != null && !Misc.isUndef(latest.getChallanInfo().getDateField4())) {
				val = latest.getChallanInfo().getDateField4();
			}
			cvalItem = new Value(val);
			break;
		}
		case 30189: {	//name="Challan Status"
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			int val = Misc.getUndefInt();
			if (latest != null && latest.getChallanInfo() != null &&  !Misc.isUndef(latest.getChallanInfo().getStatus())) {
				val = latest.getChallanInfo().getStatus();
			}
			cvalItem = new Value(val);
			break;
		}
		case 30194: {    // Adani Rake Load status
			LatestTripInfo latest = LatestTripInfo.getLatestTripInfo(vId);
			LUInfoExtract loadExtract = latest.getLoad();
			LUInfoExtract unloadExtract = latest.getUnload();
			int val = Misc.getUndefInt();
			val = loadExtract != null && loadExtract.getGateOut() > 0 && (unloadExtract == null || (unloadExtract.getGateIn() <= 0)) ? 0
					: unloadExtract != null && unloadExtract.getGateIn() > 0 && (unloadExtract.getAreaOut() <= 0) ? 1
					: unloadExtract != null && unloadExtract.getAreaOut() > 0 ? 2
					: 3
					;
					/*val = loadExtract != null && loadExtract.getGateIn() > 0 && loadExtract.getGateOut() <= 0 ? 0
							: loadExtract != null && loadExtract.getGateOut() >0 && (unloadExtract == null || unloadExtract.getGateIn() <= 0) ? 1
							: unloadExtract != null && unloadExtract.getGateIn() > 0 && unloadExtract.getGateOut() <= 0 ? 2
							: 3
							;*/
			cvalItem = new Value(val);
			break;
		}
		
		case 90174: {//RFID Trip Status
			//(case when g_currentTPR.combo_start is null then 2 when g_currentTPR.tpr_status != 2 then 3 else 5 end)
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			if (Misc.g_doMPL) {
			int val = 5;
			if (latestCache != null) {
				if (latestCache.getMaterialId() != 0) {
					val = 6;
				}
				else {
					if (latestCache.getComboStart() <= 0)
						val = 2;
					else if (latestCache.getTprStatus() != 2)
						val = 3;
				}
			}
			cvalItem = new Value(val);
		}else {
			  int rfidLoadStatus = 0;
			  if (latestCache != null) {
				 if (latestCache.getUnloadTareTime() > 0) {
				   rfidLoadStatus = 3;
				}
				else if (latestCache.getUnloadGrossTime() > 0) {
				  rfidLoadStatus = 2;
				}
				else if (latestCache.getLoadGrossTime() > 0) {
				  rfidLoadStatus = 1;
				}
				else {
				  rfidLoadStatus = 0;
				}
				 cvalItem = new Value(rfidLoadStatus);
			  }
			  else {
				  cvalItem = ResultInfo.getCachedValueWithHack(conn, session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			  }
			
			}

			break;
		}
		/*case 90174: {//RFID Trip Status
			//(case when g_currentTPR.combo_start is null then 2 when g_currentTPR.tpr_status != 2 then 3 else 5 end)
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			int val = 5;
			if (latestCache != null) {
				if (latestCache.getMaterialId() != 0) {
					val = 6;
				}
				else {
					if (latestCache.getComboStart() <= 0)
						val = 2;
					else if (latestCache.getTprStatus() != 2)
						val = 3;
				}
			}
			cvalItem = new Value(val);
			break;
		}*/
		case 90175: {//RFID+Trip Status
			//<val id="0" name="Wait for Load" />
			//<val id="1" name="Loaded (Maybe)" />
			//<val id="2" name="Loaded (MPL)" />
			//<val id="3" name="Wait for Unload" />
			//<val id="4" name="Unloaded" />
			//<val id="5" name="Non MPL" />
			//<val id="6" name="Non Coal (MPL)" />
			//<val id="7" name="Loaded (Non MPL)" />			
			int val = 5;
			Value tripLoadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			int tripLoadStatus = tripLoadStatusVal == null ? 3 : tripLoadStatusVal.getIntVal();
			Value tripLOPIdValue = getCachedValueWithHack(conn,session, vId, 35238, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			int tripLOPId = tripLOPIdValue == null ? Misc.getUndefInt() : tripLOPIdValue.getIntVal();
			//Value tripUOPIdValue = getCachedValueWithHack(conn,session, vId, 35238, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals);
			//int tripUOPId = tripLOPIdValue == null ? Misc.getUndefInt() : tripLOPIdValue.getIntVal();
		
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long latestMinesHHLogTime = -1;
			int minesId = Misc.getUndefInt();
			System.out.println("[CCACHE][RFIDGPSSTATUS] Thread:"+Thread.currentThread().getId()+" Vehicle:"+vId+" LatestCache:"+latestCache+" TripStatus:"+tripLoadStatus+" TripLop:"+tripLOPId);
			if (latestCache == null || (latestCache.getComboStart() > 0 && latestCache.getTprStatus() == 2)) {
				//either no TPR or latest TPR is closed
				//check if loaded from someplace and which is known to be mines and is operating for HH data is null or prior to
				if (tripLoadStatus == 1 || tripLoadStatus == 0) {//ie loaded or waiting for load
					ArrayList<Integer> operativeMinesList = OpsToTPRMines.getMinesListForOps(conn, tripLOPId, true);
					
					if (operativeMinesList != null && operativeMinesList.size() != 0) {
						System.out.println("[CCACHE][RFIDGPSSTATUS] Thread:"+Thread.currentThread().getId()+" TPR closed .. trip loaded checking operatinve:"+operativeMinesList+" Vehicle:"+vId+" LatestCache:"+latestCache+" TripStatus:"+tripLoadStatus+" TripLop:"+tripLOPId);
						if (tripLoadStatus == 0) {
							val = 0;//waiting for load
						}
						else {//check if HH data for any of the mines to mapped to ops has data that is behind load gate out .. if so then
							//that may be presumed ... else loaded for others
							Value lastLoadOutValue = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
							long lastLoadOut = lastLoadOutValue == null ? Misc.getUndefInt() : lastLoadOutValue.getDateValLong();
							boolean mayBeLoadedForMPL = false;
							if (lastLoadOut > 0) {
								
								for (Integer mine: operativeMinesList) {
									long hhLogForMine = HHLatestCache.getLatest(mine);
									if (lastLoadOut > hhLogForMine) {
										mayBeLoadedForMPL = true;
										System.out.println("[CCACHE][RFIDGPSSTATUS] Thread:"+Thread.currentThread().getId()+" TPR closed .. may be loaded:"+mine+" HHLog:"+Misc.longToUtilDate(hhLogForMine)+" LoadOut:"+Misc.longToUtilDate(lastLoadOut)+" Vehicle:"+vId+" LatestCache:"+latestCache+" TripStatus:"+tripLoadStatus+" TripLop:"+tripLOPId);
										break;
									}
									System.out.println("[CCACHE][RFIDGPSSTATUS] Thread:"+Thread.currentThread().getId()+" TPR closed .. not considered loaded for:"+mine+" HHLog:"+Misc.longToUtilDate(hhLogForMine)+" LoadOut:"+Misc.longToUtilDate(lastLoadOut)+" Vehicle:"+vId+" LatestCache:"+latestCache+" TripStatus:"+tripLoadStatus+" TripLop:"+tripLOPId);
								}
							}
							val = mayBeLoadedForMPL ? 1 : 7; //loaded for someone else 
						}
					
					}
					else {
						System.out.println("[CCACHE][RFIDGPSSTATUS] Thread:"+Thread.currentThread().getId()+" TPR closed .. trip for other:"+operativeMinesList+" Vehicle:"+vId+" LatestCache:"+latestCache+" TripStatus:"+tripLoadStatus+" TripLop:"+tripLOPId);
						val = tripLoadStatus == 1 ? 7 : 5; //loaded for some one else
					}
				}
				else {//either wait for unload or unloaded but no entry in TPR therefore operating for elsewhere
					val = 5;
				}
			}
			else {//we have an open entry in TPR
				if (latestCache.getMaterialId() != 0) {//latest TPR is not of coal ... so
					val = 6; //non coal but MPL
				}
				else {
					if (latestCache.getComboStart() < 0) {//trip started not yet reached MPL
						val = 2;//loaded for MPL						
					}
					else if (latestCache.getTprStatus() != 2) {//is Open
						val = 3; //being unloaded
					}
					else {//trip closed ... should not happen
						val = 5;//operating elsewhere
					}
				}
			}
			System.out.println("[CCACHE][RFIDGPSSTATUS] Thread:"+Thread.currentThread().getId()+" TPR result:"+val+" Vehicle:"+vId+" LatestCache:"+latestCache+" TripStatus:"+tripLoadStatus+" TripLop:"+tripLOPId);

			cvalItem = new Value(val);
			break;
		}
		case 90176: {//RFID Mines name  ... and if may be  then lop name
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				if (latestCache.getMaterialId() != 0) {
					val = "MPL Plant (Non Coal)";
				}
				else {
					int minesId = latestCache.getFromMinesId();
					val = OpsToTPRMines.getMinesName(conn, minesId);
				}
			}
			if (val == null) {
				Value value = getCachedValueWithHack(conn,session, vId, 22043, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				val = value == null ? null : value.toString();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90177: {//RFID MPL gate in
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getComboStart();
			}
			cvalItem = new Value(val);
			break;
		}
	
		case 90225: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getGradeCode();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90226: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getFromCode();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90227: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getToCode();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90228: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadWbInName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90229: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			double val = Misc.getUndefDouble();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getNetLoad();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90230: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			double val = Misc.getUndefDouble();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getNetUnload();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90234: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadWbOutName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90235: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadGateInName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90236: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadGateOutName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90237: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnloadGateInName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90238: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnloadGateOutName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90239: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnLoadWbInName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90240: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			String val = null;
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnLoadWbOutName();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90241: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadWbInTime();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90242: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadWbOutTime();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90243: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnLoadWbInTime();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90244: {
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnLoadWbOutTime();
			}
			cvalItem = new Value(val);
			break;
		}
		
		case 90210: {//RFID load tare
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			double val = Misc.getUndefDouble();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadTare();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90211: {//RFID load gross
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			double val = Misc.getUndefDouble();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getLoadGross();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90212: {//RFID unload tare
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			double val = Misc.getUndefDouble();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnloadTare();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90213: {//RFID unload gross
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			double val = Misc.getUndefDouble();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getUnloadGross();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90178: {//RFID MPL gate out
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {//TODO approp for non coal
				val = latestCache.getUgout();
			}
			cvalItem = new Value(val);
			break;
		}
		case 90188: {//RFID challan date or load gate out
			TPRLatestCache latestCache = TPRLatestCache.getLatest(vId);
			long val = Misc.getUndefInt();
			if (latestCache != null && latestCache.getTprStatus() != 2) {
				val = latestCache.getRfChallanDate();
				if (val <= 0)
					val = latestCache.getChallanDate();
			}
			if (val <= 0) {
				Value value = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				val = value == null ? -1 : value.getDateValLong();
			}
			cvalItem = new Value(val);
			break;
		}
		case 55013: {//APTS - deployment status
				Value uptimeClassify = getCachedValueWithHack(conn,session, vId, 35219, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				int val = 0;
				int uptimeClassifyInt = uptimeClassify == null ? 2 : uptimeClassify.getIntVal();
				if (uptimeClassifyInt == 2) {
					val = 2;
				}
				else {
					Value loadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					int loadStatus = loadStatusVal == null ? 3 : loadStatusVal.getIntVal();
					if (loadStatus == 3)
						val = 1;
					else 
						val = 0;
				}
				cvalItem = new Value(val);
				break;
			}
		case 55014: {//APTS - running/transit status
			String val = null;
			Value uptimeClassify = getCachedValueWithHack(conn,session, vId, 35219, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
			
			int uptimeClassifyInt = uptimeClassify == null ? 2 : uptimeClassify.getIntVal();
			if (uptimeClassifyInt == 2) {
				val = "Not Tracking";
			}
			else {
				Value loadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				int loadStatus = loadStatusVal == null ? 3 : loadStatusVal.getIntVal();
				DimInfo subTypeDim = DimInfo.getDimInfo(20655);
				Value fromSubTypeVal = getCachedValueWithHack(conn,session, vId, 55001, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value toSubTypeVal = getCachedValueWithHack(conn,session, vId, 55002, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				int fromSubType = fromSubTypeVal == null ? Misc.getUndefInt() : fromSubTypeVal.getIntVal();
				int toSubType = toSubTypeVal == null ? Misc.getUndefInt() :toSubTypeVal.getIntVal();
				if (loadStatus == 0) {
					ValInfo vif = subTypeDim.getValInfo(fromSubType);
					if (vif != null)
						val = "At Load:"+vif.m_name;
				}
				else if (loadStatus == 2) {
					ValInfo vif = subTypeDim.getValInfo(toSubType);
					if (vif != null)
						val = "At Unload:"+vif.m_name;
				}
				else if (loadStatus == 1) {
					ValInfo vif = subTypeDim.getValInfo(fromSubType);
					ValInfo vif2 = subTypeDim.getValInfo(toSubType);
					val = "Transit:"+ (vif == null ? "Unknown" : vif.m_name)+( " To ")+(vif2 == null ? "Unknown" : vif2.m_name);
				}
				else {
					val = "Returning Back";
				}
			}
			cvalItem = new Value(val);
			break;
		}
		    case 20940:
			case 20941: { //start of op
				if (cvalItem != null) {
					long dtp = cvalItem.getDateValLong();
					if (dtp > 0 && dtp < today)
						cvalItem = new Value (0L);
					else
						cvalItem = new Value(cvalItem);
				}
				else {
					cvalItem = new Value (0L);
				}
				break;
			}
			case 20619://bin cnt
			case 21193: { //count of unload
				Value latestUnload = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				if (latestUnload == null || latestUnload.getDateValLong() <= 0)
					latestUnload = CacheValue.getValueInternal(conn, vId, 21191, vehSetup, vdf, vehicleExt, driverExt);
				if (latestUnload != null && latestUnload.getDateValLong() > 0 && latestUnload.getDateValLong() >= today) {
					cvalItem = new Value(cvalItem);
				}
				else {
					cvalItem = new Value(0);
				}
				break;
			}
			
			case 21436: {
				Value startItem = ResultInfo.getCachedValueWithHack(conn,session, vId, 21431, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				int val = 0;
				if (startItem != null) {
					long dtl = startItem.getDateValLong();
					if (dtl > 0) {
						val = (now-dtl) > 30*60*1000 ? 0 : 1;
					}
				}
				cvalItem = new Value(val);
				break;
			}
			//20497: uin
			//21192: uuout
			//21191: lin
			//22044: lout
			case 35230: {//load_gate_out if unload_gate_out is not null else currentTime - 24hr
				Value uout = ResultInfo.getCachedValueWithHack(conn,session, vId, 21192, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value lout = ResultInfo.getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if  ((lout == null || lout.getDateValLong() <= 0) || (uout != null && uout.getDateValLong() > 0)) {
					long ts = now - 24*60*60*1000L;
					cvalItem = new Value(ts);
				}
				else {
					cvalItem = lout;
				}
				break;
			}
			case 35231: {//unload_gate_in else currentTime
				Value uout = ResultInfo.getCachedValueWithHack(conn,session, vId, 21192, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value uin = ResultInfo.getCachedValueWithHack(conn,session, vId, 20497, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if  ((uin == null || uin.getDateValLong() <= 0) || (uout != null && uout.getDateValLong() > 0)) {
					long ts = now;
					cvalItem = new Value(ts);
				}
				else {
					cvalItem = uin;
				}
				break;
			}
			case 35232: {//load op if unload_gate_out is not null else null
				Value uout = ResultInfo.getCachedValueWithHack(conn,session, vId, 21192, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value lout = ResultInfo.getCachedValueWithHack(conn,session, vId, 22043, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if  ( (uout != null && uout.getDateValLong() > 0)) {
					cvalItem = new Value((String)null);
				}
				else {
					cvalItem = lout;
				}
				break;
			}
			case 35233: {//load gate out time if unload_gate_out is not null else null
				Value uout = ResultInfo.getCachedValueWithHack(conn,session, vId, 21192, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value lout = ResultInfo.getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if  ( (uout != null && uout.getDateValLong() > 0)) {
					cvalItem = new Value(0L);
				}
				else {
					cvalItem = lout;
				}
				break;
			}
			case 35218:
				cvalItem = new Value(1);
				break;
			case 35219: {
				Value grt = ResultInfo.getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				
				long gapMin = grt == null || grt.getDateValLong() <= 0 ? 10000 : (now - grt.getDateValLong())/60000;
				
				int propVal = ResultInfo.helperGetLevelFromLov(DimInfo.getDimInfo(35219), (int) gapMin);
					//propVal = gapMin <= 60 ? 0 : gapMin <= 720 ? 1 : 2;
				cvalItem = new Value(propVal);
				break;
			}
			case 35236: {
				DimInfo dim35237 = DimInfo.getDimInfo(35237);
				
				Value uptimeClassify = getCachedValueWithHack(conn,session, vId, 35219, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				String val = null;
				int uptimeClassifyInt = uptimeClassify == null ? 2 : uptimeClassify.getIntVal();
				if (uptimeClassifyInt == 2) {
					val = dim35237.getValInfo(0).m_name;
				}
				else {
					Value loadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					int loadStatus = loadStatusVal == null ? 3 : loadStatusVal.getIntVal();
					Value loadAtTS = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					if (loadAtTS == null || (loadAtTS.getDateValLong()-now) > 72*3600000L) {
						loadStatus = 3; //operating elsewhere
					}
					Value lopVal = getCachedValueWithHack(conn,session, vId, 22043, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					String lop = lopVal == null ? null : lopVal.getStringVal();
					
					Value uopVal = getCachedValueWithHack(conn,session, vId, 20497, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					String uop = uopVal == null ? null : uopVal.getStringVal();
					if ((lop != null && lop.startsWith("SM - ")) || (uop != null && uop.startsWith("SM - ")))
						loadStatus = 3;
					if (uop != null && uop.endsWith("Stop"))
						loadStatus = 3; 
					
					if (loadStatus == 0) {//in load
						val = lop;
					}
					else if (loadStatus == 1) {//loaded
						//get dist from MPL ... we really should have otherProperty on Dim ... but will lead to GpsProjectUtilsChange
						val = dim35237.getValInfo(4).m_name;
						DimInfo dim35236 = DimInfo.getDimInfo(35236);
						ValInfo vmin1 = dim35237 == null ? null : dim35237.getValInfo(-1);
						String lonStr = vmin1 == null ? null : vmin1.getOtherProperty("ref_lon");
						String latStr = vmin1 == null ? null : vmin1.getOtherProperty("ref_lat");
						String refDistStr = vmin1 == null ? null : vmin1.getOtherProperty("ref_dist");
						double lon = Misc.getParamAsDouble(lonStr);
						double lat = Misc.getParamAsDouble(latStr);
						double refDist = Misc.getParamAsDouble(refDistStr);
						if (!Misc.isUndef(lon) && !Misc.isUndef(lat) && !Misc.isUndef(refDist)) {
							NewVehicleData vdt = vdf.getDataList(conn, vId, 0, false);
							GpsData curr =  vdt == null ? null : vdt.getLast(conn);
							if (curr != null) {
								double d= curr.distance(lon, lat);
								if (d <= refDist)
									val = dim35237.getValInfo(2).m_name;
								else
									val = dim35237.getValInfo(3).m_name;
							}						//if curr foud	
						}//if ref known
					}//if loaded
					else if (loadStatus == 2) {//in unload
						val = uop;
					}
					else if (loadStatus == 3) {//unloaded
						val = dim35237.getValInfo(1).m_name;
					}
				}
				cvalItem = new Value(val);
				break;
			}
			case 35261: {
				DimInfo dim35237 = DimInfo.getDimInfo(35237);
				
				Value uptimeClassify = getCachedValueWithHack(conn,session, vId, 35219, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				String val = null;
				int uptimeClassifyInt = uptimeClassify == null ? 2 : uptimeClassify.getIntVal();
				if (uptimeClassifyInt == 2) {
					val = dim35237.getValInfo(0).m_name;
				}
				else {
					Value loadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					int loadStatus = loadStatusVal == null ? 3 : loadStatusVal.getIntVal();
					Value loadAtTS = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					if (loadAtTS == null || (loadAtTS.getDateValLong()-now) > 72*3600000L) {
						loadStatus = 3; //operating elsewhere
					}
					Value lopVal = getCachedValueWithHack(conn,session, vId, 22043, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					String lop = lopVal == null ? null : lopVal.getStringVal();
					
					Value uopVal = getCachedValueWithHack(conn,session, vId, 20497, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					String uop = uopVal == null ? null : uopVal.getStringVal();
					if (lop != null && lop.indexOf("PKCL") < 0)
						loadStatus = 3;
					if (uop != null && uop.indexOf("Kamalpur") < 0  && uop.indexOf("Kamalpur") < 0  && uop.indexOf("Ramanujnagar") < 0   && uop.indexOf("Karonji") < 0 )
						loadStatus = 3; 
					if (loadStatus != 3) {
						NewVehicleData vdt = vdf.getDataList(conn, vId, 0, false);
						GpsData curr =  vdt == null ? null : vdt.getLast(conn);
						ArrayList<RegionTestHelper> regionInList = RTreeSearch.getContainingRegions(curr.getPoint());
						if (loadStatus == 0) {//in load
							//checi if in Adani Parsa Kante Mines - id = 3227291
							if (isRegionIn(regionInList, 3227291))
								val = "Inside - "+lop;
							else
								val = "Waiting - "+lop;
						}
						else if (loadStatus == 1) {//loaded
							int toRamanuj = 3238727;
							int nearRamanuj = 3238730;
							int toKamalpur = 3238728;
							int nearKamalpur = 3238731;
							int toKaronji = 3238729;
							int nearKaronji = 3238732;
							if (isRegionIn(regionInList, nearRamanuj)) {								
								val = RegionTest.getRegionInfo(nearRamanuj, conn).region.m_name;
							}
							else if (isRegionIn(regionInList, nearKamalpur)) {								
								val = RegionTest.getRegionInfo(nearKamalpur, conn).region.m_name;
							}
							else if (isRegionIn(regionInList, nearKaronji)) {								
								val = RegionTest.getRegionInfo(nearKaronji, conn).region.m_name;
							}
							else if (isRegionIn(regionInList, toRamanuj)) {								
								val = RegionTest.getRegionInfo(toRamanuj, conn).region.m_name;
							}
							else if (isRegionIn(regionInList, toKamalpur)) {								
								val = RegionTest.getRegionInfo(toKamalpur, conn).region.m_name;
							}
							else if (isRegionIn(regionInList, toKaronji)) {								
								val = RegionTest.getRegionInfo(toKaronji, conn).region.m_name;
							}
							else {
								val = dim35237.getValInfo(4).m_name;
							}
						}//if loaded
						else if (loadStatus == 2) {//in unload
							val = uop;
						}
						else if (loadStatus == 3) {//unloaded
							val = dim35237.getValInfo(1).m_name;
						}
					}
					else {
						val = dim35237.getValInfo(1).m_name;
					}
				}
				cvalItem = new Value(val);
				break;
			}
			case 35244: {
				Value stopDur = getCachedValueWithHack(conn,session, vId, 20255, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				DimInfo d35244 = DimInfo.getDimInfo(35244);
				int stopDurInt = stopDur == null ? 0 : stopDur.getIntVal();
				int val = helperGetLevelFromLov(d35244, stopDurInt);
				cvalItem = new Value(val);
				break;
			}
			case 35245: {//TAT
				
				Value loadStatusVal = getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				int loadStatus = loadStatusVal == null ? 3 : loadStatusVal.getIntVal();
				int val = 0;
				if (true || loadStatus == 1) {
					Value timAllowed = getCachedValueWithHack(conn,session, vId, 21190, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					Value dbgTimeAllowed = timAllowed;
					String lop = null;
					if (timAllowed == null || timAllowed.isNull()) {
						Value lopVal = getCachedValueWithHack(conn,session, vId, 22043, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
						lop = lopVal == null ? null : lopVal.getStringVal();
						if (lop != null && lop.indexOf("PKCL") >= 0 && (timAllowed == null || timAllowed.isNull())) {
							timAllowed = new Value(180);
						}
					}
					Value timStarted = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					Value timEnd = getCachedValueWithHack(conn,session, vId, 20496, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
					long timEndLong = timEnd == null ? 0 : timEnd.getDateValLong();
					if (timEndLong <= 0) 
						timEndLong = now;
					int timAllowedInt = timAllowed == null ? 0 : timAllowed.getIntVal();
					long timStartedLong = timStarted == null ? 0 : timStarted.getDateValLong();
					int gap = 1;
					if (timAllowedInt > 0 && timStartedLong > 1) {
						gap = (int)((timEndLong-timStartedLong)/(60*1000)) - timAllowedInt;
						if (gap <= 1)
							gap = 1;
						
					}
					else {
						gap = Integer.MAX_VALUE;
					}
					val = helperGetLevelFromLov(DimInfo.getDimInfo(35245), gap);
					if (true) {//dbg
						StringBuilder dbgStr = new StringBuilder();
						dbgStr.append("[CCACHE_TAT]").append(vId).append(" LoadStatus:").append(loadStatus).append(" Allowed (calc):").append(dbgTimeAllowed).append(" Allowed (adj):").append(timAllowed);
						SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
						dbgStr.append(" St:").append(timStarted == null || timStarted.isNull() ? "null" : sdf.format(timStarted.getDateVal()));
						dbgStr.append(" En:").append(timEnd == null || timEnd.isNull() ? "null" : sdf.format(timEnd.getDateVal()));
						dbgStr.append(" Gap:").append(gap).append(" val:").append(val);
						System.out.println(dbgStr);
					}
				}
				cvalItem = new Value(val);
				break;
			}
			case 35234:
			case 35235:
				int prop = vehSetup.m_flag;
				cvalItem = new  Value(prop);
				break;
			case 35250: //General Last Critical Start
			{
				
				CacheValue.LatestEventInfo latest = CacheValue.getLatestEvent(vId, Misc.getUndefInt());
				Value loadStatus = ResultInfo.getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if (loadStatus != null && loadStatus.getIntVal() != 1)
					latest = null;
				cvalItem = new Value(latest == null ? 0L : latest.getStartTime());
				break;
			}
			case 35251: //General Last Critical End
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestEvent(vId, Misc.getUndefInt());
				Value loadStatus = ResultInfo.getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if (loadStatus != null && loadStatus.getIntVal() != 1)
					latest = null;
				cvalItem = new Value(latest == null ? 0L : latest.getEndTime());
				break;
			}
			case 35252://General Last Critical Location
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestEvent(vId, Misc.getUndefInt());
				Value loadStatus = ResultInfo.getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if (loadStatus != null && loadStatus.getIntVal() != 1)
					latest = null;
				cvalItem = new Value(latest == null ? (String) null : latest.getStartName());
				break;
			}
			case 35253://General Last Critical Rule
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestEvent(vId, Misc.getUndefInt());
				Value loadStatus = ResultInfo.getCachedValueWithHack(conn,session, vId, 20355, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if (loadStatus != null && loadStatus.getIntVal() != 1)
					latest = null;
				cvalItem = new Value(latest == null ? (String) null : latest.getRuleName());
				break;
			}
			case 35254://General Recent/Ongoing Critical Event
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestEvent(vId, Misc.getUndefInt());
				cvalItem = new Value(latest == null ? 0 : latest.getEndTime() <= 0 ? 1 : (latest.getStartTime()-now) > 30*60*1000 ? 0 : 1);
				break;
			}
			//case 21431: //Crit Start be careful .. innerCal must be CacheValue.getValueInternal
			//case 21432://Crit End be careful .. innerCal must be CacheValue.getValueInternal
			//case 21433://Crit Loc be careful .. innerCal must be CacheValue.getValueInternal
			//case 21434://Crit Rule Name be careful .. innerCal must be CacheValue.getValueInternal
			//{
			//	CacheValue.LatestEventInfo currLatest = null;
			///	int ruleList[] = {1,564,502,503,504};
			//	int currLatestRuleId = -1;
			//	for (int t1=0; t1<ruleList.length; t1++) {
			//		CacheValue.LatestEventInfo temp = CacheValue.getLatestEvent(vId, ruleList[t1]);
			//		if (temp == null)
			//			continue;
			//		if (ruleList[t1] ==1 && (temp.getStartName() == null || !temp.getStartName().startsWith("[UZ]")))
			//			continue;
			//		if (temp != null && (currLatest == null || currLatest.getStartTime() <= temp.getStartTime())) {
			//			currLatest = temp;
			//			currLatestRuleId = ruleList[t1];
			//		}
			//	}
			//	if (currLatest != null) {
			//		
			//	}
			//	break;
			//}
			case 37020://General Last Critical Rule
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestOpenEvent(vId, 1);
				cvalItem = new Value(latest == null ?  0 : 1);
				break;
			}
			
			case 37021://General Last Critical Rule
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestOpenEvent(vId, 56);
				cvalItem = new Value(latest == null ? 0 : 1);
				
				break;
			}
			case 37022://General Last Critical Rule
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestOpenEvent(vId, 561);
				cvalItem = new Value(latest == null ? 0 : 1);
				
				break;
			}
			case 37023://General Last Critical Rule
			{
				CacheValue.LatestEventInfo latest = CacheValue.getLatestOpenEvent(vId, 577);
				cvalItem = new Value(latest == null ? 0 : 1);
				
				break;
			}
			case 20458 : {
				Value ls = CacheValue.getValueInternal(conn, vId, 20355, vehSetup, vdf);
				Value ss = CacheValue.getValueInternal(conn, vId, 20255, vehSetup, vdf);
				int loadStatus = ls == null ? Misc.getUndefInt() : ls.getIntVal(); 
				double stopSince =  ss == null ? Misc.getUndefDouble() : ss.getDoubleVal();
				int ccrVal = loadStatus;
				if (!Misc.isUndef(stopSince) &&  !Misc.isUndef(loadStatus)) {
					if(loadStatus == 1 && stopSince <= 0.0)
						ccrVal = 4;
					else if(loadStatus == 1 && stopSince > 0.0)
						ccrVal = 5;
					else if(loadStatus == 3 && stopSince <= 0.0)
						ccrVal = 6;
					else if(loadStatus == 3 && stopSince > 0.0)
						ccrVal = 7;
					else
						ccrVal = loadStatus;
						
				}
				cvalItem = new Value(ccrVal);
				break;
			}
			//latest TPR related dims
			case 92013://latest tpr challanDate
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getChallanDate());
				break; 
			case 92014://latest tpr rfChallanDate
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getRfChallanDate());
				break; 
			case 92015://latest tpr challanNo
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getChallanNo());
				break; 
			case 92016://latest tpr rfChallanNo
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getRfChallanNo());
				break; 
			case 92017://latest tpr lrNo
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getLrNo());
				break; 
			case 92018://latest tpr rfLRNo
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getRfLrNo());
				break; 
			case 92019://latest tpr loadGateIn
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getLgin());
				break; 
			case 92020://latest tpr loadGateOut
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getLgout());
				break; 
			case 92021://latest tpr unloadGateIn
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getUgin());
				break; 
			case 92022://latest tpr unloadGateOut
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getUgout());
				break; 
			case 92023://latest tpr tprStatus
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getTprStatus());
				break; 
			case 92024://latest tpr from(mines name)
				cvalItem = latestTPR == null ? null : new Value(OpsToTPRMines.getMinesName(conn, latestTPR.getFromMinesId()));
				break;
			case 92025://latest tpr to
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getToDestId());
				break; 
			case 92026://latest tpr material
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getMaterialId());
				break;
			case 92027://latest tpr tprId
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getTprId());
				break;
			case 92028://latest tpr doNumber
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getDoNumber());
				break;
			case 92029://latest tpr driver
				DriverExtendedInfo driver = latestTPR == null ? null : DriverExtendedInfo.getDriverExtended(conn, latestTPR.getDriverId());
				String driverName = driverExt == null ? null : driver.getDriverName();
				cvalItem = latestTPR == null ? null : new Value(driverName);
				break;
			case 92030://latest tpr transporter
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getTransporter());
				break;
			case 92031://latest tpr rfTransporter
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getRfTransporter());
				break;
			case 92032://latest tpr rfMinesId
				cvalItem = latestTPR == null ? null : new Value(latestTPR.getRfMinesId());
				break;
			case 20075: {
				System.out.println( " start20075");
				int lowThresh = 30;
				int hiThresh = 60;
				if (stopDirControl != null) {
					lowThresh = stopDirControl.getMonitorStopLoThreshSec();
					hiThresh = stopDirControl.getMonitorStopHiThreshSec();
				}
				boolean isShovel = vehSetup.isShovelType();
				int val=0;
				CacheValue.LatestEventInfo latestEngineOn=null;
				CacheValue.LatestEventInfo latestStoppage=null;
				Value ss = CacheValue.getValueInternal(conn, vId, 20255, vehSetup, vdf);;
				if(isShovel){
					latestEngineOn = CacheValue.getLatestOpenEvent(vId, 56);
					if (latestEngineOn!=null)
						val=now-latestEngineOn.getEndTime()>hiThresh*1000?2:(now-latestEngineOn.getEndTime()>lowThresh*1000)?1:0;
						else if(ss!=null)
						
					val= ss.getIntVal()>hiThresh/60?2:(ss.getIntVal()>lowThresh/60)?1:0;
						
				}
					else  if(ss!=null)
				{
					val= ss.getIntVal()>hiThresh/60?2:(ss.getIntVal()>lowThresh/60)?1:0;
					System.out.println("now"+ now +"ss.getIntVal()"+ss.getIntVal()+"val" +val+" vId"+vId);
					
				}
				cvalItem = new Value(val);
				break;
			}
			case 20076: {
				
				int lowThresh = 30;
				int hiThresh = 60;
				if (stopDirControl != null) {
					lowThresh = stopDirControl.getMonitorNoDataLoThreshSec();
					hiThresh = stopDirControl.getMonitorNoDataHiThreshSec();
				}
				int val=0;
				NewVehicleData _vdp = vdf.getDataList(conn, vId, 0, false);
				GpsData data =new GpsData();
				Value latestGpsReordTime = getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				if(latestGpsReordTime!=null){
				val= now-latestGpsReordTime.m_dateVal>hiThresh*1000?2:(now-latestGpsReordTime.m_dateVal>lowThresh*1000)?1:0;
				}
				cvalItem = new Value(val);
				break;
			}
			case 20077: {
				
				double lowThresh = 6;
				double hiThresh = 10;
				if (stopDirControl != null) {
					lowThresh = stopDirControl.getMonitorExcessLeadLoThreshKm();
					hiThresh = stopDirControl.getMonitorExcessLeadHiThreshKm();
				}
				
				int val=0;
				Value unLoadgateIn = getCachedValueWithHack(conn,session, vId, 21192, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				Value loadGateOut = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				NewVehicleData _vdp = vdf.getDataList(conn, vId, 0, false); 
				if(loadGateOut!=null){
					GpsData ldata =new GpsData();
					ldata.setGps_Record_Time(loadGateOut.getDateValLong());
					ldata = _vdp.get(conn, ldata);
					GpsData udata =new GpsData();
					if(unLoadgateIn != null){
						udata.setGps_Record_Time(unLoadgateIn.getDateValLong());
						udata = _vdp.get(conn, udata);
					}else{
						udata = _vdp.getLast(conn);
					}
					if(ldata != null && udata != null){
						double dist=udata.getValue()-ldata.getValue();
						val= dist>hiThresh?2:dist>lowThresh?1:0;
						}
				}
				cvalItem = new Value(val);
				break;
			}
			case 20078: {
				int val=0;
				Value lout = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value lin = CacheValue.getValueInternal(conn, vId, 21191, vehSetup, vdf, vehicleExt, driverExt);
				if(lin!=null&&lout==null)
					{
						lout=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				if(lout!=null)
					val=(lout.getDateValLong()-lin.getDateValLong())>3*60*1000?1:0;
				}cvalItem = new Value(val);
				break;
			}
			case 20079: {
				int val=0;
				Value lareawaitin = CacheValue.getValueInternal(conn, vId, 22072, vehSetup, vdf, vehicleExt, driverExt);
				Value lin = CacheValue.getValueInternal(conn, vId, 21191, vehSetup, vdf, vehicleExt, driverExt);
				if(lareawaitin!=null&&lin==null)
				{
					
						lin=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(lin!=null)
					val=(lin.getDateValLong()-lareawaitin.getDateValLong())>10*60*1000?1:0;
				}
				cvalItem = new Value(val);
				break;
			}
			
			case 20080: {
				int val=0;
				Value uin = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				Value lout = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if(lout!=null&&uin==null)
				{
					
						uin=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(uin!=null)
					val=uin.getDateValLong()-lout.getDateValLong()>10*60*1000?1:0;
				
				}cvalItem = new Value(val);
				break;
			}
			case 20081: {
				int val=0;
				Value uin = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				Value uout = getCachedValueWithHack(conn,session, vId, 20496, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if(uin!=null&&uout==null)
				{
						uout=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(uout!=null)
				val=(uout.getDateValLong()-uin.getDateValLong())>3*60*1000?1:0;
				}
				cvalItem = new Value(val);
				break;
			}
			case 20082: {
				int val=0;
				Value uareawaitin = CacheValue.getValueInternal(conn, vId, 22074, vehSetup, vdf, vehicleExt, driverExt);
				Value uin = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				if(uareawaitin!=null&&uin==null)
				{
					
						uin=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(uin!=null)
				val=(uin.getDateValLong()-uareawaitin.getDateValLong())>10*60*1000?1:0;
				}
				cvalItem = new Value(val);
				break;
			}
			
			case 20083: {
				int val=0;
				Value confirmtime = CacheValue.getValueInternal(conn, vId, 22076, vehSetup, vdf, vehicleExt, driverExt);
				Value uout = getCachedValueWithHack(conn,session, vId, 20496, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if(uout!=null&&confirmtime==null)
				{
					
					confirmtime=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				if(confirmtime!=null)	
				val=confirmtime.getDateValLong()-uout.getDateValLong()>10*60*1000?1:0;
				}cvalItem = new Value(val);
				break;
			}
			case 20084: {
				double val=0;
				Value lout = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				Value lin = CacheValue.getValueInternal(conn, vId, 21191, vehSetup, vdf, vehicleExt, driverExt);
				if(lin!=null&&lout==null)
					{
					
						lout=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				if(lout!=null)
					val=((lout.getDateValLong()-lin.getDateValLong())/(60*1000));
				}
				cvalItem = new Value(val);
				break;
			}
			
			case 20085: {
				double val=0;
				Value lareawaitin = CacheValue.getValueInternal(conn, vId, 22072, vehSetup, vdf, vehicleExt, driverExt);
				Value lin = CacheValue.getValueInternal(conn, vId, 21191, vehSetup, vdf, vehicleExt, driverExt);
				if(lareawaitin!=null)
				{
					if(lin==null)
						lin=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(lin!=null)
					val=(lin.getDateValLong()-lareawaitin.getDateValLong())/(60*1000);
				}
				cvalItem = new Value(val);
				break;
			}
			
			case 20086: {
				double val=0;
				Value uin = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				Value lout = getCachedValueWithHack(conn,session, vId, 22044, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if(lout!=null)
				{
					if(uin==null)
						uin=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(uin!=null)
					val=(uin.getDateValLong()-lout.getDateValLong())/(60*1000);
				
				}
				cvalItem = new Value(val);
				break;
			}
			case 20087: {
				double val=0;
				Value uin = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				Value uout = getCachedValueWithHack(conn,session, vId, 20496, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if(uin!=null)
				{
					if(uout==null)
						uout=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(uout!=null)
				val=(uout.getDateValLong()-uin.getDateValLong())/(60*1000);
				}
				cvalItem = new Value(val);
				break;
			}
			case 20088: {
				double val=0;
				Value uareawaitin = CacheValue.getValueInternal(conn, vId, 22074, vehSetup, vdf, vehicleExt, driverExt);
				Value uin = CacheValue.getValueInternal(conn, vId, 21192, vehSetup, vdf, vehicleExt, driverExt);
				if(uareawaitin!=null)
				{
					if(uin==null)
						uin=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
					if(uin!=null)
				val=(uin.getDateValLong()-uareawaitin.getDateValLong())/(60*1000);
				}
				cvalItem = new Value(val);
				break;
			}
			
			case 20089: {
				double val=0;
				Value confirmtime = CacheValue.getValueInternal(conn, vId, 22076, vehSetup, vdf, vehicleExt, driverExt);
				if(confirmtime==null)
				confirmtime=getCachedValueWithHack(conn,session, vId, 20173, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,stopDirControl);
				Value uout = getCachedValueWithHack(conn,session, vId, 20496, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
				if(uout!=null&&confirmtime!=null)
				{
				val=(confirmtime.getDateValLong()-uout.getDateValLong())/(60*1000);
				}
				cvalItem = new Value(val);
				break;
			}
			case 82616 : {
				//Alert Alarm On/Off 
				boolean isAlert=false;
				if( CacheValue.getValueInternal(conn, vId, 82617, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82617, vehSetup, vdf).m_iVal ==1 )
					isAlert=true;
				if(CacheValue.getValueInternal(conn, vId, 82619, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82619, vehSetup, vdf).m_iVal==1 )
					isAlert=true;
				if(	CacheValue.getValueInternal(conn, vId, 82620, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82620, vehSetup, vdf).m_iVal==1 )
					isAlert=true;
				if(	CacheValue.getValueInternal(conn, vId, 82621, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82621, vehSetup, vdf).m_iVal==1 )
					isAlert=true;
				if(CacheValue.getValueInternal(conn, vId, 82623, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82623, vehSetup, vdf).m_iVal==1 )
					isAlert=true;
				if(	CacheValue.getValueInternal(conn, vId, 82625, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82625, vehSetup, vdf).m_iVal==1 )
					isAlert=true;
				if(	CacheValue.getValueInternal(conn, vId, 82627, vehSetup, vdf)==null?false:CacheValue.getValueInternal(conn, vId, 82627, vehSetup, vdf).m_iVal==1)
					isAlert=true;
				if(isAlert){
					cvalItem = new Value(1);
				}else{
					cvalItem = new Value(0);
				}
			}
			break;
			default:
				cvalItem = cvalItem == null ? null : new Value(cvalItem);
				break;
		}

		return cvalItem;
	}
	
	public static int helperGetLevelFromLov(DimInfo dimInfo, int dur) {
		int val = Misc.getUndefInt();
		ArrayList<DimInfo.ValInfo> valList = dimInfo == null ? null : dimInfo.getValList();
		for (int i=0,is = valList == null ? 0 : valList.size(); i<is;i++) {
			ValInfo vi = valList.get(i);
			int prop = Misc.getParamAsInt(vi == null ? null : vi.getOtherProperty("thresh_min"));
			if (prop >= dur) {
				val = valList.get(i).m_id;
				break;
			}
		}
		if (Misc.isUndef(val))
			val = valList != null && valList.size() > 0 ? valList.get(valList.size()-1).m_id : 0;
		return val;
	}
	public static boolean isRegionIn(ArrayList<RegionTestHelper> regList, int id) {
		for (int i=0,is= regList == null ? 0 : regList.size(); i < is; i++) {
			RegionTestHelper rt = regList.get(i);
			if (rt.region.id == id)
				return true;
		}
		return false;
	}
	public boolean isUseCache() {
		return m_useCache;
	}
public static void main(String[] args) throws Exception {
		Loader.executeLoad();
		Connection conn = null;
		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		int vehicleId = 43656;//43875
		int dimId = 90174;
		System.out.println(OpsToTPRMines.getMinesName(conn, 1));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 358, true));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 358, false));
		System.out.println(OpsToTPRMines.getMinesName(conn, 4));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 360, true));
		System.out.println(OpsToTPRMines.getMinesListForOps(conn, 360, false));
		System.out.println(TPRLatestCache.getLatest(43656));
		System.out.println(TPRLatestCache.getLatest(43875));
		System.out.println(Misc.longToUtilDate(HHLatestCache.getLatest(1)));
		System.out.println(Misc.longToSqlDate(HHLatestCache.getLatest(5)));
		Value v = null;
		dimId = 90174;
		VehicleControlling vehicleControlling = NewProfileCache.getControlling(vehicleId);
		StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn,CacheTrack.VehicleSetup.getSetup(vehicleId, conn));
		/*v = ResultInfo.getCachedValueWithHack(conn, _session, vehicleId, dimId, CacheTrack.VehicleSetup.getSetup(vehicleId, conn), VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false), VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null, stopDirControl);
		System.out.println("d90174:"+v);
		dimId = 90175;
		v = ResultInfo.getCachedValueWithHack(conn, vehicleId, dimId, CacheTrack.VehicleSetup.getSetup(vehicleId, conn), VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false), VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null, stopDirControl);
		System.out.println("d90174:"+v);
		dimId = 90176;
		v = ResultInfo.getCachedValueWithHack(conn, vehicleId, dimId, CacheTrack.VehicleSetup.getSetup(vehicleId, conn), VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false), VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null, stopDirControl);
		System.out.println("d90174:"+v);
		dimId = 90177;
		v = ResultInfo.getCachedValueWithHack(conn, vehicleId, dimId, CacheTrack.VehicleSetup.getSetup(vehicleId, conn), VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false), VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null, stopDirControl);
		System.out.println("d90174:"+v);
		dimId = 90188;
		v = ResultInfo.getCachedValueWithHack(conn, vehicleId, dimId, CacheTrack.VehicleSetup.getSetup(vehicleId, conn), VehicleDataInfo.getVehicleDataInfo(conn, vehicleId, true, false), VehicleExtendedInfo.getVehicleExtended(conn, vehicleId), null, null, stopDirControl);
		System.out.println("d90174:"+v);*/
		
		DBConnectionPool.returnConnectionToPoolNonWeb(conn);
	}
}

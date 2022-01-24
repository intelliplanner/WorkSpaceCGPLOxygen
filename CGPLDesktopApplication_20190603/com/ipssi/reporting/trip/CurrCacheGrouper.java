package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.NewProfileCache;
import com.ipssi.common.ds.trip.VehicleControlling;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.DriverExtendedInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.CachedWhereItem;
import com.ipssi.tprCache.TPRLatestCache;
import com.ipssi.workflow.WorkflowHelper;

public class CurrCacheGrouper {
	private ArrayList<Integer> groupingIndex = null;
	private ArrayList<Triple<Integer, ArrayList<Value>, ArrayList<Value>>> rows = null; //pair first  = count, second = result, third vals for sort
	private ArrayList<DimConfigInfo> fpiList = null;
	private ArrayList<ArrayList<DimConfigInfo>> searchBox = null;
	private ArrayList<Pair<Integer, Boolean>> sortingCriteria = null; //second = false => asc
	private HashMap<String, Value> loadFromDBVals = null;//key = vehId+_+dimId
	public HashMap<String, Value> getLoadFromDBVals() {
		return loadFromDBVals;
	}
	public int getSize() {
		return rows == null ? 0 :rows.size();
	}
	public ArrayList<Value> getRow(int idx) {
		Triple<Integer, ArrayList<Value>, ArrayList<Value>> entry = rows == null || idx < 0 || idx >= rows.size() ? null : rows.get(idx);
		return entry == null ? null : entry.second;
	}
	public ArrayList<Integer> getGroupingIndex() {
		return groupingIndex;
	}
	public boolean hasGrouping() {
		return groupingIndex != null && groupingIndex.size() > 0;
	}
	public static int compareTo(Value v1, Value v2) { //move this to Value
		int retval = 0;
		if (v1 == v2) {
			
		}
		else if (v1 == null && v2 != null) {
			retval = -1;
		}
		else if (v1 != null && v2 == null) {
			retval = 1;
		}
		else {
			if (v1.m_type == Cache.STRING_TYPE) {
				String v1i = v1.getStringVal();
				String v2i = v2.getStringVal();
				retval = v1i == v2i ? 0 : v1i == null ? -1 : v2i == null ? 1 : v1i.compareTo(v2i);
			}
			else if (v1.m_type == Cache.NUMBER_TYPE) {
				double v1i = v1.getDoubleVal();
				double v2i = v2.getDoubleVal();
				retval = Misc.isEqual(v1i, v2i) ? 0 : v1i-v2i < 0 ? -1 : 1; 
			}
			else if (v1.m_type == Cache.DATE_TYPE) {
				long v1i = v1.getDateValLong();
				long v2i = v2.getDateValLong();
				retval = v1i < v2i ? -1 : v1i == v2i ? 0 : 1;
			}
			else {
				int v1i = v1.getIntVal();
				int v2i = v2.getIntVal();
				retval = v1i < v2i ? -1 : v1i == v2i ? 0 : 1;
			}
		}	
		return retval;
	}
	public int compareTo(ArrayList<Value> lhs, ArrayList<Value> rhs) {
		int retval = 0;
		for (int i=0,is = groupingIndex == null ? 0 : groupingIndex.size(); i<is;i++) {
			int idx = groupingIndex.get(i);
			Value v1 = lhs == null || lhs.size() <= idx ? null : lhs.get(idx);
			Value v2 = rhs == null || rhs.size() <= idx ? null : rhs.get(idx);
			retval = compareTo(v1, v2); 
			if (retval != 0)
				break;
		}		
		return retval;
	}
	public void addEmptyRow() {
		ArrayList<Value> row = new ArrayList<Value> ();
		for (int i=0,is=fpiList == null ? 0 : this.fpiList.size(); i<is; i++) {
			row.add(null);
		}
		rows.add(new Triple<Integer, ArrayList<Value>, ArrayList<Value>>(1, row, null));
	}
	
	public void addTotalRowAt(int idx, Triple<Integer, ArrayList<Value>, ArrayList<Value>> row) {
		if (row == null || row.second == null)
			return;
		if (idx < 0)
			idx = rows.size();
		if (idx == rows.size()) {
			rows.add(row);
		}
		else {
			rows.add(idx, row);
		}
	}
	
	public CurrCacheGrouper(ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, ArrayList<Pair<Integer, Boolean>> sortingCriteria, HashMap<String, Value> loadFromDBVals) {
		//sortingCriteria second = false => asc
		this.searchBox = searchBox;
		this.sortingCriteria = sortingCriteria;
		this.loadFromDBVals = loadFromDBVals;
		for (int i=0,is=fpiList == null ? 0 : fpiList.size(); i<is;i++) {
			DimConfigInfo dci = fpiList.get(i);
			if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_refBlockInPFM == null) {
				if (dci.m_aggregate) {
					if (groupingIndex == null) {
						groupingIndex = new ArrayList<Integer>();
						for (int j=0,js=i;j<js;j++) {
							DimConfigInfo dci2 = fpiList.get(j);				
							if (dci2 != null && dci2.m_dimCalc != null && dci2.m_dimCalc.m_dimInfo != null && dci2.m_refBlockInPFM == null) {
								groupingIndex.add(j);
							}
						}
					}
					else {
						//do nothing
					}
				}
				else if (groupingIndex != null) {
					groupingIndex.add(i);
				}
			}
		}
		rows = new ArrayList<Triple<Integer,ArrayList<Value>, ArrayList<Value>>>();
		this.fpiList = fpiList;
	}
	public void doGetDataAndGrouping(Connection conn, ArrayList<Pair<Integer, Long>> vehicleList, SessionManager _session, SearchBoxHelper searchBoxHelper) throws Exception {
		StringBuilder dbgSB = null;//new StringBuilder();
		
		long ts1 = System.currentTimeMillis();
		long ts2 = ts1;
		
		ArrayList<ArrayList<CachedWhereItem>> clauses = searchBox == null ? null : GeneralizedQueryBuilder.getWhrQueryCached(_session, searchBox, searchBoxHelper);
		ts2 = System.currentTimeMillis();
		if (dbgSB != null) {
			dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" AftGetWhr (ms)").append((ts2-ts1)/1000).append("\n");
			System.out.println(dbgSB);
			dbgSB.setLength(0);
		}
		
		
		ts1 = ts2;
		boolean needsSorting = this.groupingIndex == null && vehicleList != null && vehicleList.size() > 0 && vehicleList.get(0).second == null && (sortingCriteria == null || sortingCriteria.size() > 0);
		ArrayList<Integer> vehicleIdListParamPassed = new ArrayList<Integer>();
		GeneralizedQueryBuilder.addObjectIdPassedInWhr(_session, WorkflowHelper.getTableInfo(WorkflowHelper.G_OBJ_VEHICLES), null, vehicleIdListParamPassed,false);
		ts2 = System.currentTimeMillis();
		if (dbgSB != null) {
			dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" AftObjPassed:").append(vehicleIdListParamPassed.size()).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
		}
		ts1 = ts2;
		long now = System.currentTimeMillis();
		if (needsSorting) {
			DimInfo d35219 = DimInfo.getDimInfo(35219);
			if (sortingCriteria == null) {
				sortingCriteria = new ArrayList<Pair<Integer, Boolean>> ();
				if (d35219 != null)
					sortingCriteria.add(new Pair<Integer, Boolean>(35219, false));//sortingCriteria second = false => asc
				sortingCriteria.add(new Pair<Integer, Boolean>(9002, false));
			}
		}
		ts2 = System.currentTimeMillis();
		if (dbgSB != null) {
			dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" AftObjPassed:(vehicleList, vehicleIdListParam, groupingIndex, sortingCriteria) (").append(vehicleList.size()).append(",").append(vehicleIdListParamPassed.size()).append(",").append(groupingIndex == null ? 0 : groupingIndex.size()).append(",").append(sortingCriteria == null ? 0 : sortingCriteria.size()).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
		}
		ts1 = ts2;

		for (int i=0,is = vehicleList == null ? 0 : vehicleList.size(); i<is;i++) {
			int vId = vehicleList.get(i).first;
			
			if (vehicleIdListParamPassed.size() > 0) {
				boolean found = false;
				for (int t1=0,t1s=vehicleIdListParamPassed.size(); t1<t1s;t1++) {
					if (vehicleIdListParamPassed.get(t1) == vId) {
						found = true;
						break;
					}
				}
				if (!found) {
					continue;
				}
			}
			ts2 = System.currentTimeMillis();
			if (dbgSB != null) {
				dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" check matching in passed ").append(vId).append(",").append(i).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
			}
			ts1 = ts2;
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vId, conn);
			ts2 = System.currentTimeMillis();
			if (dbgSB != null) {
				dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" Got VehicleCacheSetup ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
			}
			ts1 = ts2;

			VehicleExtendedInfo vehicleExt = VehicleExtendedInfo.getVehicleExtended(conn, vId);
			ts2 = System.currentTimeMillis();
			if (dbgSB != null) {
				dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" Got VehicleExt ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
			}
			ts1 = ts2;

			DriverExtendedInfo driverExt = null;//DEBUG13 .. may be OOM on MPL  DriverExtendedInfo.getDriverExtendedByVehicleId(conn, vId);
			//ts2 = System.currentTimeMillis();
			//System.out.println("[CCACH]Grouping Thread:"+Thread.currentThread().getId()+" Got driverExt "+vId+" (ms)+"+(ts2-ts1)/1000);
			//ts1 = ts2;
			TPRLatestCache tprLatestCache = TPRLatestCache.getLatest(vId);
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vId, false, false);
			
			if (vehSetup != null && vdf != null) {
				if (clauses != null) {
					boolean matches = true;
					matches = matches && GeneralizedQueryBuilder.evaluateCachedWhere(conn,_session, vId, clauses, loadFromDBVals);
					ts2 = System.currentTimeMillis();
					if (dbgSB != null) {
						dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" Got matches ").append(vId).append(",").append(matches).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
					}
					ts1 = ts2;

					if (!matches)
						continue;
				}
				ArrayList<Value> row = new ArrayList<Value>();
				ArrayList<Value> sortData = null;
				VehicleControlling vehicleControlling = NewProfileCache.getOrCreateControlling( vId);
				StopDirControl stopDirControl = vehicleControlling.getStopDirControl(conn, vehSetup);
				

				synchronized (vdf) {
					ts2 = System.currentTimeMillis();
					if (dbgSB != null) {
						dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" starting get data: ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
					}
					ts1 = ts2;
					int loadFromDBIndex = -1;
					for (int j=0,js = fpiList.size(); j<js;j++) {
						DimConfigInfo dci = fpiList.get(j);
						
						int dimId = dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_refBlockInPFM == null ? dci.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
						
						Value v = null;
						if (!Misc.isUndef(dimId)) {
							v = ResultInfo.getCachedValueWithHack(conn,_session, vId, dimId, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals,tprLatestCache, stopDirControl);
						}
						else if (dci.m_name != null && dci.m_name.length() > 0) {
							v = new Value(dci.m_name);
						}
						else if (dci.m_disp != null && dci.m_disp.length() > 0) {
							v = new Value(dci.m_disp);
						}
						row.add(v);
					}
					ts2 = System.currentTimeMillis();
					if (dbgSB != null) {
						dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" got data: ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
					}
					ts1 = ts2;
					if (needsSorting) {
                        sortData = new ArrayList<Value> ();
                    	for (int j=0,js = sortingCriteria == null ? 0 :sortingCriteria.size(); j<js;j++) {
    						int dimId = sortingCriteria.get(j).first;
    						Value v = null;
    						if (!Misc.isUndef(dimId)) {
    							v = ResultInfo.getCachedValueWithHack(conn,_session, vId, dimId, vehSetup, vdf, vehicleExt, driverExt, loadFromDBVals, stopDirControl);
    						}
    						sortData.add(v);
    					}  	
					}
					ts2 = System.currentTimeMillis();
					if (dbgSB != null)
						dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" got sort crit data: ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
					ts1 = ts2;
				}//end synch block ... getting of row
				if (groupingIndex == null) {
					rows.add(new Triple<Integer, ArrayList<Value>, ArrayList<Value>>(1, row, sortData));
					continue;
				}
				int addBefore = rows.size();
				int comp = 1;
				for (int j=0,js = rows.size();j<js;j++) {
					ArrayList<Value> existingRow = rows.get(j).second;
				    int cmpVal = compareTo(existingRow, row);
				    if (cmpVal >= 0) {
				    	comp = cmpVal;
				    	addBefore = j;
				    	break;
				     }
				}//checed where to add
				ts2 = System.currentTimeMillis();
				if (dbgSB != null)
					dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" found add before row: ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
				ts1 = ts2;
				if (addBefore == rows.size()) {
					rows.add(new Triple<Integer, ArrayList<Value>, ArrayList<Value>>(1,row, null));
				}
				else if (comp != 0) {
					rows.add(addBefore, new Triple<Integer, ArrayList<Value>, ArrayList<Value>>(1,row, null));
				}
				else {
					//need to merge
					ArrayList<Value> existingRow = rows.get(addBefore).second;
					int existingCnt = rows.get(addBefore).first;
					for (int j=0,js=fpiList.size();j<js;j++) {
						DimConfigInfo dci = fpiList.get(j);
						if (dci.m_aggregate && dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_refBlockInPFM == null) {
							String aggregateOp = dci.m_default;
							if (aggregateOp == null || aggregateOp.length() == 0)
								aggregateOp = "sum";
							Value cv = existingRow.get(j);
							Value nv = row.get(j);
							if (nv != null) {
								if (cv == null) {
									existingRow.set(j, new Value(nv));
								}
								else {
									doAggregate(aggregateOp, cv, nv, existingCnt,1);
								}//ifcurrent val is not null
							}//if newVal is not null
							
						}//for each aggregateable dci
					} //for each dimConfig
					rows.get(addBefore).first = existingCnt+1;
					ts2 = System.currentTimeMillis();
					if (dbgSB != null) {
						dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" merged row: ").append(vId).append(" (ms)+").append((ts2-ts1)/1000).append("\n");
					}
					ts1 = ts2;
					
				}//need to merge
				
			}//if vaild vehsetupvdf
		}//for each vehicle
		if (needsSorting && rows.size() > 1) {
			SortHelper sorter = new SortHelper(sortingCriteria);
			Collections.sort(rows, sorter);
		}
		ts2 = System.currentTimeMillis();
		if (dbgSB != null)
			dbgSB.append("[CCACH]Grouping Thread:").append(Thread.currentThread().getId()).append(" done:  (ms)+").append((ts2-ts1)/1000).append("\n");
		ts1 = ts2;
		if (dbgSB != null)
			System.out.println(dbgSB);
	}//end of func
	
	public void addZeroRow() throws Exception {
		ArrayList<Value> row = new ArrayList<Value>();
		for (int j=0,js = fpiList.size(); j<js;j++) {
			DimConfigInfo dci = fpiList.get(j);
			DimInfo dimInfo = dci != null && dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo :null;
			Value v = null;
			if (dimInfo != null) {
				if (dimInfo.m_type == Cache.INTEGER_TYPE)
					v = new Value(0);
				else if (dimInfo.m_type == Cache.NUMBER_TYPE)
					v = new Value(0.0);
			}
			else if (dci.m_name != null && dci.m_name.length() > 0)
				v = new Value(dci.m_name);
			else if (dci.m_disp != null && dci.m_disp.length() > 0) 
				v = new Value(dci.m_disp);
			row.add(v);
		}
		rows.add(new Triple<Integer, ArrayList<Value>, ArrayList<Value>>(1, row, null));
	}//end of func
	
	public Triple<Integer, ArrayList<Value>, ArrayList<Value>> doTotal(int start, int endExcl, ArrayList<Integer> totalIndexToRetain) {
		if (start == endExcl) {
			return null;
		}
		if (start < 0)
			start = 0;
		if (endExcl < 0 || endExcl > rows.size())
			endExcl = rows.size();
		if (rows.size() == 0)
			return null;
		ArrayList<Value> firstRow = rows.get(start).second;
		ArrayList<Value> firstRowSort = rows.get(start).third;
		ArrayList<Value> existingRow = new ArrayList<Value>();
		for (int i=0,is = firstRow == null ? 0 : firstRow.size(); i<is ;i++) {
			existingRow.add(null);
		}
		int existingCnt = 0;
		for (int i=start;i<endExcl;i++) {
			ArrayList<Value> row = rows.get(i).second;
			int rowCnt = rows.get(i).first;
			for (int j=0,js=fpiList.size();j<js;j++) {
				DimConfigInfo dci = fpiList.get(j);
				if (dci.m_aggregate && dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_refBlockInPFM == null) {
					String aggregateOp = dci.m_default;
					if (aggregateOp == null || aggregateOp.length() == 0)
						aggregateOp = "sum";
					Value cv = existingRow.get(j);
					Value nv = row.get(j);
					if (nv != null) {
						if (cv == null) {
							existingRow.set(j, new Value(nv));
						}
						else {
							doAggregate(aggregateOp, cv, nv, existingCnt, rowCnt);
						}//ifcurrent val is not null
					}//if newVal is not null
				}//for each aggregateable dci
				
			} //for each dimConfig
			existingCnt += rowCnt;
		}//for each row
		for (int j=0,js=fpiList.size();j<js;j++) {
			DimConfigInfo dci = fpiList.get(j);
			boolean toRetain = !(dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_refBlockInPFM == null);
			
			if (!toRetain) { //check in index
				if (dci.m_aggregate)
					continue; //dont make changes ... it has already been calculated
				for (int k=0,ks = totalIndexToRetain == null ? 0 : totalIndexToRetain.size(); k<ks; k++) {
					if (totalIndexToRetain.get(k) == j) {
						toRetain = true;
						break;
					}
				}	
			}
			if (toRetain) {
				existingRow.set(j, firstRow.get(j) == null ? null : new Value(firstRow.get(j)));
			}
			else {
				existingRow.set(j, new Value("Total"));//DEBUG13 - figure out how to use grouping of ResultInfo
			}
		}
		return new Triple<Integer, ArrayList<Value>, ArrayList<Value>>(existingCnt, existingRow, firstRowSort);
	}
	
	public static void doAggregate(String aggregateOp, Value cv, Value nv, int existingCnt, int addnlCnt) {
		if (nv.m_type == Cache.NUMBER_TYPE) {
			double cvi = cv.getDoubleVal();
			double nvi = nv.getDoubleVal();
			if (Misc.isUndef(cvi)) {
				cvi = nvi;
			}
			else if (!Misc.isUndef(nvi)) {
				if ("sum".equals(aggregateOp)) {
					cvi += nvi;
				}
				else if ("min".equals(aggregateOp)) {
					cvi = Math.min(cvi, nvi);
				}
				else if ("max".equals(aggregateOp)) {
					cvi = Math.max(cvi, nvi);
				}
				else if ("avg".equals(aggregateOp)) {
					cvi = (cvi*existingCnt + nvi*addnlCnt)/(existingCnt+addnlCnt);
				}
			}
			cv.setValue(cvi);
		}//doing double type
		else if (nv.m_type == Cache.STRING_TYPE) {
			String cvi = cv.getStringVal();
			String nvi = nv.getStringVal();
			if (cvi == null || cvi.length() == 0) {
				cvi = nvi;
			}
			else if (nvi != null && nvi.length() != 0) {
				if ("sum".equals(aggregateOp)) {
					cvi += ","+nvi ;
				}
				else if ("min".equals(aggregateOp)) {
					cvi = cvi.compareTo(nvi) <= 0 ? cvi : nvi;
				}
				else if ("max".equals(aggregateOp)) {
					cvi = cvi.compareTo(nvi) <= 0 ? nvi : cvi;
				}
				else if ("avg".equals(aggregateOp)) {
				}
			}
			cv.setValue(cvi);
		}//doing string type
		else if (nv.m_type == Cache.DATE_TYPE) {
			long cvi = cv.getDateValLong();
			long nvi = nv.getDateValLong();
			if (Misc.isUndef(cvi)) {
				cvi = nvi;
			}
			else if (!Misc.isUndef(nvi)) {
				if ("sum".equals(aggregateOp)) {
					cvi += nvi;
				}
				else if ("min".equals(aggregateOp)) {
					cvi = Math.min(cvi, nvi);
				}
				else if ("max".equals(aggregateOp)) {
					cvi = Math.max(cvi, nvi);
				}
				else if ("avg".equals(aggregateOp)) {
					cvi = (long) ((double) (cvi*existingCnt + nvi*addnlCnt)/(double) (existingCnt+addnlCnt));
				}
			}
			cv.setValue(cvi);
		}//doing date type
		else {
			int cvi = cv.getIntVal();
			int nvi = nv.getIntVal();
			if (Misc.isUndef(cvi)) {
				cvi = nvi;
			}
			else if (!Misc.isUndef(nvi)) {
				if ("sum".equals(aggregateOp)) {
					cvi += nvi;
				}
				else if ("min".equals(aggregateOp)) {
					cvi = Math.min(cvi, nvi);
				}
				else if ("max".equals(aggregateOp)) {
					cvi = Math.max(cvi, nvi);
				}
				else if ("avg".equals(aggregateOp)) {
					cvi = (int) ((double) (cvi*existingCnt + nvi*addnlCnt)/(double) (existingCnt+addnlCnt));
				}
			}
			cv.setValue(cvi);
		}//doing int type
	}
	
	public static class SortHelper implements Comparator<Triple<Integer, ArrayList<Value>, ArrayList<Value>>> {
		private ArrayList<Pair<Integer, Boolean>> sortCriteria = null;
		public SortHelper(ArrayList<Pair<Integer, Boolean>> sortCriteria) {
			this.sortCriteria = sortCriteria;
		}
		public int compare(Triple<Integer, ArrayList<Value>, ArrayList<Value>> lhs, Triple<Integer, ArrayList<Value>, ArrayList<Value>> rhs) {
			int retval = 0;
			for (int i=0,is = sortCriteria == null ? 0 : sortCriteria.size(); i<is; i++) {
				Value v1 = lhs == null || lhs.third == null || lhs.third.size() <= i ? null : lhs.third.get(i);
				Value v2 = rhs == null || rhs.third == null || rhs.third.size() <= i ? null : rhs.third.get(i);
				retval = CurrCacheGrouper.compareTo(v1, v2);
				if (sortCriteria.get(i).second) {
					retval = -1*retval;
				}
				if (retval != 0)
					return retval;
			}
			return retval;
		}
	}
}

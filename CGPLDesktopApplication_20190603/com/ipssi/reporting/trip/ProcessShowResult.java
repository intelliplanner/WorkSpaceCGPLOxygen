package com.ipssi.reporting.trip;

import java.util.ArrayList;

import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;

public class ProcessShowResult {
	public Pair<String, Integer> rowLinkHelperOld = null;
	public ArrayList<Integer> dimIndexToPivot = null;
	public ArrayList<Integer> dimIndexOfPivotMeasure = null;
	public ArrayList<Integer> dimIndexOfcummDontResetOver = null;
	public ArrayList<Pair<Integer, DimConfigInfo.ExprHelper.CalcFunctionEnum>> dimIndexWithDoJavaCumm = null;
	public boolean doTotalAcrossColInPivot = false;
	public ArrayList<Integer> dimIndexOfMeasuresTotAcrossRow = null;
	public int maxYTDScopeAsked = -1;//TODO @#@#@#@ to incorporate
	public ArrayList<Integer> colsInRow = new ArrayList<Integer>();
	public boolean hasColWithRowId = false;
	public boolean hasPivoting() {
		return dimIndexToPivot != null && dimIndexOfPivotMeasure != null && dimIndexToPivot.size() > 0 && dimIndexOfPivotMeasure.size() > 0; 
	}
	//public ArrayList<ProcessShowResult> addnlMultiPivot = new ArrayList<ProcessShowResult>();
	//mpl_stone_extraneous - we want to show extraneous by
	
	boolean needsManipulation() {
		return (dimIndexToPivot != null && dimIndexToPivot.size() > 0)
	|| (dimIndexOfMeasuresTotAcrossRow != null && dimIndexOfMeasuresTotAcrossRow.size() > 0)
		//|| (dimIndexOfPivotMeasure != null && dimIndexOfPivotMeasure.size() > 0)
		//|| (dimIndexWithYTDLike != null && dimIndexWithYTDLike.size() > 0)
		//|| (dimIndexWithCummLike != null && dimIndexWithCummLike.size() > 0)
		;
	}
	public static ProcessShowResult processForDataToShow(FrontPageInfo fpi, ArrayList<DimConfigInfo> fpiList, ArrayList<ArrayList<DimConfigInfo>> searchBox, SessionManager session, SearchBoxHelper searchBoxHelper) throws Exception {
		ProcessShowResult retval = new ProcessShowResult();
		boolean doingBackMode =Misc.getSERVER_MODE() == 1; 
		for (int i=0,is=fpiList == null ? 0 : fpiList.size(); i<is;i++) {
			DimConfigInfo dc = fpiList.get(i);
			if (dc != null && dc.m_dimCalc != null && dc.m_dimCalc.m_dimInfo != null) {
				boolean putColInRow = true;
				if (dc.m_dimCalc.m_dimInfo.m_id == 20356) {
					retval.hasColWithRowId = true;
				}
				if (doingBackMode && dc.m_dimCalc.m_dimInfo.m_id == 20255) {
					dc.m_dimCalc.m_dimInfo = DimInfo.getDimInfo(20765);
					Integer iInt = new Integer(i);
					fpi.m_colIndexLookup.put("d20255", iInt);
					fpi.m_colIndexLookup.put("d20765", iInt);
				}
				if (dc.getDoPivot() != 0) {
					putColInRow = false;
					if (retval.dimIndexToPivot == null) {
						retval.dimIndexToPivot = new ArrayList<Integer>();
					}
					retval.dimIndexToPivot.add(i);
					if (dc.isDoTotalAcrossColInPivot())
						retval.doTotalAcrossColInPivot = true;
				}
				if (dc.isDoTotalAcrossRow()) {
					if (retval.dimIndexOfMeasuresTotAcrossRow == null) {
						retval.dimIndexOfMeasuresTotAcrossRow = new ArrayList<Integer>();
					}
					retval.dimIndexOfMeasuresTotAcrossRow.add(i);
				}
				if (dc.isDoPivotMeasure()) {
					putColInRow = false;
					if (retval.dimIndexOfPivotMeasure == null) {
						retval.dimIndexOfPivotMeasure = new ArrayList<Integer>();
					}
					retval.dimIndexOfPivotMeasure.add(i);
				}
				if (dc.isDoJavaCumm()) {
					if (retval.dimIndexWithDoJavaCumm == null) {
						retval.dimIndexWithDoJavaCumm = new ArrayList<Pair<Integer, DimConfigInfo.ExprHelper.CalcFunctionEnum>>();
					}
					String aggregateOp = GeneralizedQueryBuilder.helperGetAggregateOp(dc, searchBoxHelper, session);
					DimConfigInfo.ExprHelper.CalcFunctionEnum opEnum = DimConfigInfo.ExprHelper.CalcFunctionEnum.getFuncCode(aggregateOp); 
					retval.dimIndexWithDoJavaCumm.add(new Pair<Integer, DimConfigInfo.ExprHelper.CalcFunctionEnum>(i, opEnum));
				}
				if (dc.isCummDontResetOver()) {
					if (retval.dimIndexOfcummDontResetOver == null) {
						retval.dimIndexOfcummDontResetOver = new ArrayList<Integer>();
					}
					retval.dimIndexOfcummDontResetOver.add(i);
				}
				
				if (dc.getYtdScope() >= 0) {
					int scp = dc.getYtdScope();
					if (Misc.isLHSHigherScope(scp, retval.maxYTDScopeAsked))
						retval.maxYTDScopeAsked = scp;
				}
				if (putColInRow) {
					retval.colsInRow.add(i);
				}
			}
		}
		DimConfigInfo firstColumnInfo = fpiList.get(0);
		DimConfigInfo secondColumnInfo = fpiList.get(1);
		Pair<String, Integer> rowLinkHelper = null;
		boolean done = false;
		String topPageContext = searchBoxHelper.m_topPageContext;
			for (int i=0, is=searchBox == null ? 0 : searchBox.size();i<is;i++) {
				ArrayList<DimConfigInfo> rowInfo = searchBox.get(i);
				int colSize = rowInfo.size();
				for (int j=0;j<colSize;j++) {
					DimConfigInfo dimConfig = rowInfo.get(j);
					if(dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null)
						continue;
					DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
					if(dimInfo.m_subsetOf == 24103){
						int paramId = dimConfig.m_dimCalc.m_dimInfo.m_id;
						String tempVarName =  topPageContext+paramId;
						String tempVal = session.getParameter(tempVarName);
						ValInfo vinfo = dimInfo.getValInfo(Misc.getParamAsInt(tempVal));
						if (vinfo != null) {
							done = true;
							secondColumnInfo.m_dimCalc.m_dimInfo = DimInfo.getDimInfo(vinfo.m_id);
							secondColumnInfo.m_columnName = "d"+vinfo.m_id;
							secondColumnInfo.m_internalName = "d"+vinfo.m_id;
							secondColumnInfo.m_name = vinfo.m_name;
							int refId = Misc.getParamAsInt(vinfo.getOtherProperty("ref_id"));
							String refName = vinfo.getOtherProperty("ref_name");
							rowLinkHelper = new Pair<String, Integer>(refName, 0);
							firstColumnInfo.m_hidden = true;
							firstColumnInfo.m_dimCalc.m_dimInfo = DimInfo.getDimInfo(refId);
							firstColumnInfo.m_columnName = refName == null ? "d"+refId : refName;
							firstColumnInfo.m_internalName = "d"+refId;
							firstColumnInfo.m_name = vinfo.m_name;
							firstColumnInfo.m_hidden = true;
						}
						break;
					}
					if(done)
						break;
				}
			}
			retval.rowLinkHelperOld = rowLinkHelper;
			if (!retval.needsManipulation())
				retval.colsInRow = null;
			return retval;
		}
}

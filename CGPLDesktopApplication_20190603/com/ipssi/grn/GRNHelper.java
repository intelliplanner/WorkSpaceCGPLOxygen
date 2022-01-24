package com.ipssi.grn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.jsp.JspWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimCalc;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.FmtI;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.input.InputTemplate;
import com.ipssi.reporting.trip.CssClassDefinition;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder;
import com.ipssi.reporting.trip.HtmlGenerator;
import com.ipssi.reporting.trip.ProcessShowResult;
import com.ipssi.reporting.trip.ResultInfo;
import com.ipssi.reporting.trip.ResultInfo.FormatHelper;
import com.ipssi.reporting.trip.ResultInfo.VirtualVal;
import com.ipssi.workflow.WorkflowDef;
import com.ipssi.workflow.WorkflowHelper;

public class GRNHelper {
	public static class UserGivenQtyParams {
		ArrayList<Integer> groupingIndex = null;
		FrontPageInfo fpi = null;
		int polineIndex = -1;
		int potargetQtyIndex = -1;
		HashMap<String, ArrayList<Pair<Integer, Double>>> valsProvided = new HashMap<String, ArrayList<Pair<Integer, Double>>>();
		UserGivenQtyParams(ArrayList<Integer> groupingIndex, FrontPageInfo fpi) {
			this.groupingIndex = groupingIndex;
			this.fpi = fpi;
			if (fpi != null) { 
				this.polineIndex = fpi.getColIndexByName("d90537");
				this.potargetQtyIndex = fpi.getColIndexByName("d90509");
			}
		}
		
		//key = seller+mines+grade+transporter+date vals = sequential list of PolineItemId if specified + targetQty
		public static String getKey(ArrayList<Value> valList, SimpleDateFormat sdf) {
			StringBuilder sb = new StringBuilder();			
			
			for (int i=0,is=valList == null ? 0 : valList.size(); i<is;i++) {
				if (sb.length() != 0)
					sb.append("$");
				Value v = valList.get(i);
				sb.append(v == null || v.isNull() ? "null" : v.m_type == Cache.DATE_TYPE ? sdf.format(v.getDateVal()) : v.toString());
			}
			return sb.toString();
		}
		
		public void readParams(SessionManager session) {
			String xmlDataString = session == null ? null : session.getParameter("XML_DATA");
			Document xmlDoc = xmlDataString != null && xmlDataString.length() != 0 ? MyXMLHelper.loadFromString(xmlDataString) : null;
			Element topElem = xmlDoc == null ? null : xmlDoc.getDocumentElement();
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
			for (Node n=topElem == null ? null : topElem.getFirstChild(); n!=null; n=n.getNextSibling()) {
				if (n.getNodeType() != 1)
					continue;
				Element e = (Element) n;
				ArrayList<Value> valList = new ArrayList<Value>();
				for (int i=0,is=this.groupingIndex == null ? 0 : groupingIndex.size(); i<is;i++) {
					int gi = groupingIndex.get(i);
					if (gi < 0)
						continue;
					DimConfigInfo dci = (DimConfigInfo) fpi.m_frontInfoList.get(gi);
					if (dci == null || dci.m_dimCalc == null || dci.m_dimCalc.m_dimInfo == null)
						continue;
					String varName = "v"+dci.m_dimCalc.m_dimInfo.m_id;
					int attribType = dci.m_dimCalc.m_dimInfo.m_type;
					Value v = null;
					if (attribType == Cache.STRING_TYPE) {
						v = new Value(e.getAttribute(varName));
					}	
					else if (attribType == Cache.NUMBER_TYPE) {
						v = new Value(Misc.getParamAsDouble(e.getAttribute(varName)));
					}
					else if (attribType == Cache.DATE_TYPE) {
						v = new Value(Misc.getParamAsDate(e.getAttribute(varName), null, null, sdf, null));
					}
					else {
						v = new Value(Misc.getParamAsInt(e.getAttribute(varName)));
					}
					valList.add(v);
				}
				String key = getKey(valList, sdf);
				
				int polineItemId = Misc.getParamAsInt(e.getAttribute("v90537"));
				double targetQty = Misc.getParamAsDouble(e.getAttribute("v90509"));
				ArrayList<Pair<Integer, Double>> resList = valsProvided.get(key);
				if (resList == null) {
					resList = new ArrayList<Pair<Integer, Double>>();
					valsProvided.put(key, resList);
				}
				resList.add(new Pair<Integer, Double>(polineItemId, targetQty));
			}
		}
		
		ArrayList<Pair<Integer, Double>> getParams(ResultInfo resultInfo) {
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
			ArrayList<Value> valList = new ArrayList<Value>();
			for (int i=0,is=this.groupingIndex == null ? 0 : groupingIndex.size(); i<is;i++) {
				int gi = groupingIndex.get(i);
				if (gi < 0)
					continue;
				valList.add(resultInfo.getVal(gi));
			}
			String key = this.getKey(valList, sdf);
			ArrayList<Pair<Integer, Double>> res = this.valsProvided.get(key);
			return res;
		}
	}

	public static void printPage(Connection conn, SessionManager session, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, JspWriter out) throws Exception {
		int eventBasedDateIndex = fpi.getColIndexByName("event_based_date");
		
		ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpi.m_frontInfoList, session, searchBoxHelper); //TODO - get rid of dependency on searchBoxHelper so that formatHelper is called before processSearchBox
		ArrayList<ArrayList<Value>> dataRows = getReadyTPR(conn, session, fpi, searchBoxHelper, formatHelper);
		GRNHelper.printHeader(fpi.m_frontInfoList, out, session);
		GRNHelper.printRows(conn, session, dataRows, fpi, formatHelper, out);
	}
	public static ArrayList<Integer> getGroupingIndex(FrontPageInfo fpi) {
		ArrayList<Integer> retval = new ArrayList<Integer>();
		for (int i=0,is=fpi.m_frontInfoList.size();i<is;i++) {
			DimConfigInfo dci = (DimConfigInfo) fpi.m_frontInfoList.get(i);
			if (dci != null && "1".equals(dci.m_customField3))
				retval.add(i);
		}
		return retval;
	}
	public static String GRN_INSERT_Q = "insert into grns(seller_id, material_id, mines_id, transporter_id, for_start_date, for_end_date, posting_date, document_date, bill_of_lading_date, bill_of_lading" +
			" , header_text, delivery_note, store_sap_code, post_status,qty_to_post, supplier_qty,po_line_item_id "+
			", status, port_node_id, created_on, updated_on, created_by, updated_by) "+
	" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static String UPDATE_GRN_ID_REPORT_STATUS_OF_TPR = "update tp_record set grn_id = ?, reporting_status=? where tpr_id = ? ";
	public static String UPDATE_GRN_ID_REPORT_STATUS_OF_TPR_APPRVD = "update tp_record_apprvd set grn_id = ?, reporting_status=? where tpr_id = ? ";
	public static String UPDATE_HEADER = "update grns set header_text=(case when header_text is null then concat('RFID',id) else header_text end) " +
			", delivery_note=(case when delivery_note is null then concat('RF/',id) else delivery_note end) where id = ?";  
	
public static void saveData(Connection conn, SessionManager session) throws Exception {
		String xmlDataString = session == null ? null : session.getParameter("XML_DATA");
		Document xmlDoc = xmlDataString != null && xmlDataString.length() != 0 ? MyXMLHelper.loadFromString(xmlDataString) : null;
		Element topElem = xmlDoc == null ? null : xmlDoc.getDocumentElement();
		SimpleDateFormat withMinOnly = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		SimpleDateFormat withDateOnly = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		SimpleDateFormat withSec = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		SimpleDateFormat stdFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		PreparedStatement ps = conn.prepareStatement(GRN_INSERT_Q);
		PreparedStatement psUpdGRNIdOfTPR = conn.prepareStatement(UPDATE_GRN_ID_REPORT_STATUS_OF_TPR);
		PreparedStatement psUpdGRNIdOfTPR_apprvd = conn.prepareStatement(UPDATE_GRN_ID_REPORT_STATUS_OF_TPR_APPRVD);
		PreparedStatement psUpdHeader = conn.prepareStatement(UPDATE_HEADER);
		int newReportingStatus = 11;//GRN mapped
		int tprsSeen = 0;
		ArrayList<Integer> tprsUpd = new ArrayList<Integer>();
		ArrayList<Integer> grnIds = new ArrayList<Integer>();
		int portNodeId = Misc.getParamAsInt(session.getParameter("pv123"));
		for (Node n=topElem == null ? null : topElem.getFirstChild(); n!=null; n=n.getNextSibling()) {
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element) n;
			int colIndex = 1;
			String idParam = e.getAttribute("v90500");
			if (idParam == null || idParam.length() == 0)
				continue;//not checked
			
			Misc.setParamInt(ps, Misc.getParamAsInt(e.getAttribute("v60244")), colIndex++);//supplier
			Misc.setParamInt(ps, Misc.getParamAsInt(e.getAttribute("v60229")), colIndex++);//grade
			Misc.setParamInt(ps, Misc.getParamAsInt(e.getAttribute("v60230")), colIndex++);//mines
			Misc.setParamInt(ps, Misc.getParamAsInt(e.getAttribute("v60231")), colIndex++);//transporter
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(Misc.getParamAsDate(e.getAttribute("v90525") //for start date
					, withSec, withMinOnly, withDateOnly, stdFmt)));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(Misc.getParamAsDate(e.getAttribute("v90526") //for end date
					, withSec, withMinOnly, withDateOnly, stdFmt)));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(Misc.getParamAsDate(e.getAttribute("v90527")//posting date
					, withSec, withMinOnly, withDateOnly, stdFmt)));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(Misc.getParamAsDate(e.getAttribute("v90528")//document date
					, withSec, withMinOnly, withDateOnly, stdFmt)));
			ps.setTimestamp(colIndex++, Misc.utilToSqlDate(Misc.getParamAsDate(e.getAttribute("v90529")//bill of lading date
					, withSec, withMinOnly, withDateOnly, stdFmt)));
			ps.setString(colIndex++, InputTemplate.getCleanedString(e.getAttribute("v90542")));//bill of lading
			ps.setString(colIndex++, InputTemplate.getCleanedString(e.getAttribute("v90530")));//header text
			ps.setString(colIndex++, InputTemplate.getCleanedString(e.getAttribute("v90531")));//delivery note
			ps.setString(colIndex++, InputTemplate.getCleanedString(e.getAttribute("v90532")));//store code
			ps.setInt(colIndex++, 0);//post status // made 0 being mapped on 27-06-2016
			Misc.setParamDouble(ps, Misc.getParamAsDouble(e.getAttribute("v90535")), colIndex++);//qty to post
			Misc.setParamDouble(ps, Misc.getParamAsDouble(e.getAttribute("v90536")), colIndex++);//qty in delivery note
			Misc.setParamInt(ps, Misc.getParamAsInt(e.getAttribute("v90537")), colIndex++);//po line item id
			ps.setInt(colIndex++, 1);//status
			int pv123 = Misc.getParamAsInt(e.getAttribute("pv123"), portNodeId);
			Misc.setParamInt(ps, pv123, colIndex++);
			java.sql.Timestamp now = Misc.longToSqlDate(System.currentTimeMillis());
			int userId = (int) session.getUserId();
			ps.setTimestamp(colIndex++, now);
			ps.setTimestamp(colIndex++, now);
			Misc.setParamInt(ps, userId, colIndex++);
			Misc.setParamInt(ps, userId, colIndex++);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int grnId = rs.next() ? Misc.getRsetInt(rs, 1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
			grnIds.add(grnId);
			psUpdHeader.setInt(1, grnId);
			psUpdHeader.addBatch();
			String tprIdRef = InputTemplate.getCleanedString(e.getAttribute("v90502"));
			if (tprIdRef != null && tprIdRef.length() != 0) { 
				ArrayList<Integer> tprIds = new ArrayList<Integer>();
				Misc.convertValToVector(tprIdRef, tprIds);
				for (int j=0,js=tprIds.size();j<js;j++) {
					psUpdGRNIdOfTPR.setInt(1, grnId);
					psUpdGRNIdOfTPR.setInt(2, newReportingStatus);
					psUpdGRNIdOfTPR.setInt(3, tprIds.get(j));
					psUpdGRNIdOfTPR.addBatch();
					
					psUpdGRNIdOfTPR_apprvd.setInt(1, grnId);
					psUpdGRNIdOfTPR_apprvd.setInt(2, newReportingStatus);
					psUpdGRNIdOfTPR_apprvd.setInt(3, tprIds.get(j));
					psUpdGRNIdOfTPR_apprvd.addBatch();
					tprsUpd.add(tprIds.get(j));
					tprsSeen++;
					if (tprsSeen > 3000) {
						psUpdGRNIdOfTPR.executeBatch();
						psUpdGRNIdOfTPR_apprvd.executeBatch();
						tprsSeen = 0;
					}
				}//for each tpr
			}//if tprdRef
		}//for each grn
		if (tprsSeen > 0) {
			psUpdGRNIdOfTPR.executeBatch();
			psUpdGRNIdOfTPR_apprvd.executeBatch();
			tprsSeen = 0;
		}
		psUpdHeader.executeBatch();
		psUpdHeader = Misc.closePS(psUpdHeader);
		psUpdGRNIdOfTPR = Misc.closePS(psUpdGRNIdOfTPR);
		psUpdGRNIdOfTPR_apprvd = Misc.closePS(psUpdGRNIdOfTPR_apprvd);
		ps = Misc.closePS(ps);
		
		//Now set up workflow for grn 
		if (grnIds != null && grnIds.size() > 0) {
			InputTemplate grnCreateTemplate = InputTemplate.getTemplate(session.getCache(), conn, "tr_grn_template", portNodeId, "tr_grn_inp.xml", session);
			if (grnCreateTemplate != null)
				WorkflowHelper.doWorkflowCreateUpdateEtc(conn, session, grnIds, WorkflowHelper.G_GRNS, grnCreateTemplate.getRows(), true, null, null, null, (int)session.getUserId(), null,WorkflowDef.WORKFLOW_TYPE_REG);
		}
		//And workflows for tprIds
		if (tprsUpd != null && tprsUpd.size() > 0) {
			ArrayList<DimConfigInfo> row = new ArrayList<DimConfigInfo>();
			DimConfigInfo dciRepStatus = new DimConfigInfo();
			dciRepStatus.m_dimCalc = new DimCalc(DimInfo.getDimInfo(60283), null ,null);
			DimConfigInfo dciGrnId = new DimConfigInfo();
			dciGrnId.m_dimCalc = new DimCalc(DimInfo.getDimInfo(60286), null, null); 
			row.add(dciRepStatus);
			row.add(dciGrnId);
			ArrayList<ArrayList<DimConfigInfo>> rows = new ArrayList<ArrayList<DimConfigInfo>>();
			rows.add(row);
			WorkflowHelper.doWorkflowCreateUpdateEtc(conn, session, grnIds, WorkflowHelper.G_OBJ_TPRECORD, rows, true, null, null, null, (int)session.getUserId(), null,WorkflowDef.WORKFLOW_TYPE_REG);
		}
	}

	public static ArrayList<ArrayList<Value>> getReadyTPR(Connection conn, SessionManager session, FrontPageInfo fpi, SearchBoxHelper searchBoxHelper, FormatHelper formatHelper) throws Exception {
		Connection poQtyLookupConn = null;
		ResultSet rs = null;
		Statement stmt = null;
		try {
			poQtyLookupConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ProcessShowResult processDataToShowResult = null;
			ArrayList<Integer> groupingIndex = getGroupingIndex(fpi);
			UserGivenQtyParams userGivenQtyParams = new UserGivenQtyParams(groupingIndex, fpi);
			userGivenQtyParams.readParams(session);
			
			GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
			int grnIndexAlt = fpi.getColIndexByName("select");
//			int tripQtyIndex = fpi.getColIndexByName("d60262");
			int tripQtyIndex = fpi.getColIndexByName("d60627");
			//int doBasedPOLineIndex = fpi.getColIndexByName("d70219");
			//int doBasedQtyIndex = fpi.getColIndexByName("d70220");
			int qtyToPostIndex = fpi.getColIndexByName("d90535");
			int challanDateIndex = fpi.getColIndexByName("d60234");
			int eventBasedDateIndex = fpi.getColIndexByName("d90153");
			int mplQtyIndex = fpi.getColIndexByName("d60261");
			int modeIndex = fpi.getColIndexByName("d70208");
			int grnStartDateIndex = fpi.getColIndexByName("d90525");
			int grnEndDateIndex = fpi.getColIndexByName("d90526");
			int grnSuppQtyIndex = fpi.getColIndexByName("d90536");
			int grnPostingDateIndex = fpi.getColIndexByName("d90527");
			int grnDocumentDateIndex = fpi.getColIndexByName("d90528");
			int grnWayBillIndex = fpi.getColIndexByName("d90529");
			int storeCodeIndex = fpi.getColIndexByName("d90532");
			int grnTPRRefIndex = fpi.getColIndexByName("d90502");
			int tprIdIndex = fpi.getColIndexByName("d60201");
			int balanceQtyIndex = fpi.getColIndexByName("d90505");
			
			
			GeneralizedQueryBuilder.QueryParts qp = qb.buildQueryParts(fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
					searchBoxHelper, formatHelper, fpi.m_colIndexLookup, GeneralizedQueryBuilder.getDriverObjectFromName(fpi.m_driverObjectLocTracker), fpi.m_orderIds, null, false, Misc.getUndefInt(), 1, false,0);
			String query = qb.buildQuery(session, qp, null ,false);
			ArrayList<HashMap<MiscInner.Pair, FastList<VirtualVal>>> virtualResult = null;
			ArrayList<Pair<Integer,Long>> vehicleList = null;
			System.out.println("#############"+query);
			
			java.util.Date userStartDt = qp.startDt;
			java.util.Date userEndDt = qp.endDt;		
			String storeCode = null;
			Cache cache = session.getCache();
			int portNodeId = Misc.getParamAsInt(session.getParameter("pv123"));
			MiscInner.PortInfo portInfo = cache.getPortInfo(portNodeId, conn);
			if (portInfo != null) {
				ArrayList<String> vl = portInfo.getStringParams(OrgConst.ID_STR_STORE_CODE);
				if (vl != null && vl.size() > 0)
					storeCode = vl.get(0);
			}
			
			stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE); //.. leads to issues
			rs = stmt.executeQuery(query);
			
			ResultInfo resultInfo = new ResultInfo(fpi.m_frontInfoList, fpi.m_colIndexLookup, rs, session, searchBoxHelper,
					qp.m_needsRollup && qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, fpi.m_colIndexUsingExpr, formatHelper, virtualResult, qp.m_virtualCol,vehicleList, processDataToShowResult != null && processDataToShowResult.colsInRow != null && processDataToShowResult.colsInRow.size() > 0 ? processDataToShowResult.colsInRow : qp.m_hasColWithAgg ? qp.m_isInclInGroupBy : null, processDataToShowResult, null, false);
	
			boolean ignoreNegQtyIfPOGiven = "1".equals(session.getParameter(searchBoxHelper.m_topPageContext+90507));
			boolean dontMergeTPRsWithDifferentDOPO = !"1".equals(session.getParameter(searchBoxHelper.m_topPageContext+90508));
			boolean forRailUseMpl = !"0".equals(session.getParameter(searchBoxHelper.m_topPageContext+90503));
			ArrayList<Value> prevDataRow = null;
			ArrayList<ArrayList<Value>> rows = new ArrayList<ArrayList<Value>>();
			int currGRNIndex = 0;
			double currAvQty = 0;
			double qtyBeingPosted = 0;
			HashMap<Integer, Double> poToAvQtyIndex = new HashMap<Integer, Double>();//if same po line item is used then to lookup
			ArrayList<Pair<Integer, Double>> currGroupingUserSpecValues = null;
			int currGroupingUserSpecValuesIndex = 0;
			int prevPOLineItemFromDO = Misc.getUndefInt();
			int prevPOLineItemForGRN = Misc.getUndefInt();
			ArrayList<Value> currTotRow = null;
			long mxChallanDate = -1;
			long mxEventDate = -1;
			double qtyOfSupplierForGRN = 0;
			StringBuilder referredTPR = new StringBuilder();
			boolean assumingGivenPO = false;
			while (resultInfo.next()) {
				ArrayList<Value> currDataRow = new ArrayList<Value>();
				for (int i=0,is=fpi.m_frontInfoList.size();i<is;i++) {
					Value v = resultInfo.getVal(i);
					currDataRow.add(v);//NOTE just ref being added ... when we needto add grouperRow we will create copy in grouperRow
				}
				Value tv1 = null; // resultInfo.getVal(doBasedPOLineIndex);
				int currPOLineItemFromDO = tv1 == null || tv1.isNull() ? Misc.getUndefInt() : tv1.getIntVal(); 
				boolean diff = ResultInfo.isDifferentForSelDimIndex(prevDataRow, currDataRow, prevDataRow != null, groupingIndex, null);
				if (diff) {
					currGroupingUserSpecValues = userGivenQtyParams.getParams(resultInfo);
					currGroupingUserSpecValuesIndex = 0;
				}
				if (!diff) {
					if (!dontMergeTPRsWithDifferentDOPO && (prevPOLineItemFromDO != currPOLineItemFromDO)) {
						diff = true;
					}
				}					
				Value tripQty = tripQtyIndex >= 0 ? currDataRow.get(tripQtyIndex) : null;;
				double tripQtyDouble = tripQty == null || tripQty.isNull() ? 0 : tripQty.getDoubleVal();
				Value suppQtyV = mplQtyIndex >= 0 ? currDataRow.get(mplQtyIndex) : null;
				double suppQtyDouble = suppQtyV == null || suppQtyV.isNull() ? 0 : suppQtyV.getDoubleVal();
				if (forRailUseMpl) {
					Value modeV = modeIndex >= 0 ? currDataRow.get(modeIndex) : null;
					if (modeV != null && modeV.getIntVal() == 2) {
						tripQtyDouble = suppQtyDouble;
					}
				}
				
				if (!diff && prevDataRow != null) {
					//check if diff because it will be exhausted				
					double bal = currAvQty - qtyBeingPosted - tripQtyDouble;
					boolean toIgnoreNegative = ignoreNegQtyIfPOGiven || !assumingGivenPO;
					if (!toIgnoreNegative && bal < -0.001 && currAvQty > 0.001) {
						//cant be posted to same
						diff = true;
					}
				}
				if (diff && currTotRow != null) {//add a new row for total ... and set up various markers
					rows.add(currTotRow);
					++currGRNIndex;
					if (grnIndexAlt >= 0) currTotRow.set(grnIndexAlt, new Value(currGRNIndex));
					if (qtyToPostIndex >= 0) currTotRow.set(qtyToPostIndex, new Value(qtyBeingPosted));
					if (mplQtyIndex >= 0) currTotRow.set(mplQtyIndex, new Value(qtyOfSupplierForGRN));
					if (grnPostingDateIndex >= 0) currTotRow.set(grnPostingDateIndex, new Value(mxEventDate));
					if (grnDocumentDateIndex >= 0) currTotRow.set(grnDocumentDateIndex, new Value(mxEventDate));
					if (grnWayBillIndex >= 0) currTotRow.set(grnWayBillIndex, new Value(mxChallanDate));
					if (storeCodeIndex >= 0)currTotRow.set(storeCodeIndex, new Value(storeCode));
					if (grnStartDateIndex >= 0) currTotRow.set(grnStartDateIndex, new Value(userStartDt));
					if (grnEndDateIndex >= 0) currTotRow.set(grnEndDateIndex, new Value(userEndDt));
					if (grnTPRRefIndex >= 0) currTotRow.set(grnTPRRefIndex, new Value(referredTPR.toString()));
					if (!Misc.isUndef(prevPOLineItemForGRN)) {
						double poqty = poToAvQtyIndex.containsKey(prevPOLineItemForGRN) ? poToAvQtyIndex.get(prevPOLineItemForGRN) : 0; 
						poToAvQtyIndex.put(prevPOLineItemForGRN, new Double(poqty - qtyBeingPosted));
						if (balanceQtyIndex >= 0)
							currTotRow.set(balanceQtyIndex, new Value(poqty - qtyBeingPosted));
					}
					
				}
				if (diff) {
					int poFromUserSpec = Misc.getUndefInt();
					double targetFromUserSpec = Misc.getUndefDouble();
					mxChallanDate = -1;
					mxEventDate = -1;
					referredTPR.setLength(0);
					
					if (currGroupingUserSpecValues != null && currGroupingUserSpecValues.size() > currGroupingUserSpecValuesIndex) {
						poFromUserSpec = currGroupingUserSpecValues.get(currGroupingUserSpecValuesIndex).first;
						targetFromUserSpec = currGroupingUserSpecValues.get(currGroupingUserSpecValuesIndex).second;
						if (targetFromUserSpec < 0.001)
							targetFromUserSpec = Misc.getUndefDouble();
						if (!Misc.isUndef(currPOLineItemFromDO) && dontMergeTPRsWithDifferentDOPO && poFromUserSpec != currPOLineItemFromDO) {
							//see if we have matching in user params
							poFromUserSpec = Misc.getUndefInt();
							targetFromUserSpec = Misc.getUndefDouble();
							for (int t1=currGroupingUserSpecValuesIndex,t1s=currGroupingUserSpecValues.size();t1<t1s;t1++) {
								if (currGroupingUserSpecValues.get(t1).first.intValue() == currPOLineItemFromDO) {
									poFromUserSpec = currGroupingUserSpecValues.get(t1).first;
									targetFromUserSpec = currGroupingUserSpecValues.get(t1).second;
									currGroupingUserSpecValues.remove(t1);
									break;
								}
							}
						}
						else {	
							currGroupingUserSpecValuesIndex++;
						}
					}
					Value qty = null; //resultInfo.getVal(doBasedQtyIndex);
					if (Misc.isUndef(poFromUserSpec)) {
						poFromUserSpec = currPOLineItemFromDO;
					}
					if (targetFromUserSpec < 0.001 && qty != null && qty.isNotNull())
						targetFromUserSpec = qty.getDoubleVal();
					assumingGivenPO = false;
					if (targetFromUserSpec < 0.001) 
						targetFromUserSpec = 0;
					if (targetFromUserSpec > 0.001){
						assumingGivenPO = true;
					}
					if (!Misc.isUndef(poFromUserSpec))
						assumingGivenPO = true;
					if (!Misc.isUndef(poFromUserSpec)) {
						if (poToAvQtyIndex.containsKey(poFromUserSpec)) { 
							double poqty = poToAvQtyIndex.get(poFromUserSpec);
							if (targetFromUserSpec < 0.001)
								targetFromUserSpec = poqty;
						}
						else  {
							double poqty = GRNHelper.getAvQty(poQtyLookupConn, poFromUserSpec);
							poToAvQtyIndex.put(poFromUserSpec, poqty);
						}
					}
					
					prevPOLineItemForGRN = poFromUserSpec;
					
					currTotRow = new ArrayList<Value>();
					for (int i=0,is=fpi.m_frontInfoList.size();i<is;i++) {
						currTotRow.add(new Value(currDataRow.get(i)));
					}
					if (userGivenQtyParams.polineIndex >= 0) {
						currTotRow.set(userGivenQtyParams.polineIndex, new Value(poFromUserSpec));
					}
					if (userGivenQtyParams.potargetQtyIndex >= 0) {
						currTotRow.set(userGivenQtyParams.potargetQtyIndex, new Value(targetFromUserSpec));
					}
					currAvQty = targetFromUserSpec;
					qtyBeingPosted = 0;
					prevPOLineItemFromDO = currPOLineItemFromDO;
					qtyOfSupplierForGRN = 0;
					referredTPR.setLength(0);
				}//if diff
				//now add the currRow
				Value currChallanDateV = resultInfo.getVal(challanDateIndex);
				Value evenDateV = resultInfo.getVal(eventBasedDateIndex);
				if (currChallanDateV != null && currChallanDateV.isNotNull()) {
					if (mxChallanDate < 0 || mxChallanDate < currChallanDateV.getDateValLong())
						mxChallanDate = currChallanDateV.getDateValLong();
				}
				if (evenDateV != null && evenDateV.isNotNull()) {
					if (mxEventDate < 0 || mxEventDate < evenDateV.getDateValLong())
						mxEventDate = evenDateV.getDateValLong();
				}
				qtyBeingPosted += tripQtyDouble;
				prevDataRow = currDataRow;
				qtyOfSupplierForGRN += suppQtyDouble;
				int tprId = tprIdIndex >= 0 ? currDataRow.get(tprIdIndex).getIntVal() : Misc.getUndefInt();
				if (!Misc.isUndef(tprId)) {
					if (referredTPR.length()>0)
						referredTPR.append(",");
					referredTPR.append(tprId);
				}
				
			}
			
			rs = Misc.closeRS(rs);
			stmt.close();
			stmt = null;
			if (prevDataRow != null) {
				rows.add(currTotRow);
				++currGRNIndex;
				if (grnIndexAlt >= 0) currTotRow.set(grnIndexAlt, new Value(currGRNIndex));
				if (qtyToPostIndex >= 0) currTotRow.set(qtyToPostIndex, new Value(qtyBeingPosted));
				if (mplQtyIndex >= 0) currTotRow.set(mplQtyIndex, new Value(qtyOfSupplierForGRN));
				if (grnPostingDateIndex >= 0) currTotRow.set(grnPostingDateIndex, new Value(mxEventDate));
				if (grnDocumentDateIndex >= 0) currTotRow.set(grnDocumentDateIndex, new Value(mxEventDate));
				if (grnWayBillIndex >= 0) currTotRow.set(grnWayBillIndex, new Value(mxChallanDate));
				if (storeCodeIndex >= 0)currTotRow.set(storeCodeIndex, new Value(storeCode));
				if (grnStartDateIndex >= 0) currTotRow.set(grnStartDateIndex, new Value(userStartDt));
				if (grnEndDateIndex >= 0) currTotRow.set(grnEndDateIndex, new Value(userEndDt));
				if (grnTPRRefIndex >= 0) currTotRow.set(grnTPRRefIndex, new Value(referredTPR.toString()));
				
				if (!Misc.isUndef(prevPOLineItemForGRN)) {
					double poqty = poToAvQtyIndex.containsKey(prevPOLineItemForGRN) ? poToAvQtyIndex.get(prevPOLineItemForGRN) : 0; 
					poToAvQtyIndex.put(prevPOLineItemForGRN, new Double(poqty - qtyBeingPosted));
					if (balanceQtyIndex >= 0)
						currTotRow.set(balanceQtyIndex, new Value(poqty - qtyBeingPosted));
				}
				
				
			}//if prevData exists
			return rows;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			try {
				while (rs != null && rs.next()); //just go thru to exhaust rs
				rs = Misc.closeRS(rs);
				stmt.close();
			}
			catch (Exception e2) {
				
			}
			try {
				if (poQtyLookupConn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(poQtyLookupConn);
			}
			catch (Exception e2) {
				
			}
		}
	}
	
	public static double getAvQty(Connection conn, int poLineItemId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select (case when po_line_item.qty_av is null then 0 else po_line_item.qty_av end)- (case when grntot90641.qty is null then 0 else grntot90641.qty end) from po_line_item left outer join (select po_line_item_id pol, sum(qty_to_post) qty from grns where post_status in (0,1,2) and status=1 and po_line_item_id = ? group by po_line_item_id) grntot90641 on (grntot90641.pol = po_line_item.id) where po_line_item.id = ? ");
		ps.setInt(1, poLineItemId);
		ps.setInt(2, poLineItemId);
		ResultSet rs = ps.executeQuery();
		double qty = rs.next() ? rs.getDouble(1) : 0;
		rs.close();
		ps.close();
		return qty;
	}
	
	public static void printHeader(ArrayList<DimConfigInfo> dciList, JspWriter out, SessionManager session) throws Exception {
		StringBuilder sb = new StringBuilder();
		String displayLink = null;
		//ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		Cache cache = session.getCache();
		boolean hasMultiple = false;//hasNestedColHeader(rows); ... nested printed in its own header ... at the most in case of all read only does it make sense to print in col ... too complex
		HtmlGenerator.printTableStart(sb, null, HtmlGenerator.styleWithAdornment, "DATA_TABLE", false, false, false);
		sb.append("<thead>");
		sb.append("<tr class='").append(CssClassDefinition.getHtmlCssClass(hasMultiple ? 0 : 1)).append("'>");
		StringBuilder tempSB = new StringBuilder();
		ArrayList<DimConfigInfo> row = dciList;
		for (int j=0,js = row.size(); j<js; j++) {
			DimConfigInfo dci = row.get(j); 
			if (dci == null)
				continue;
			boolean hasColSpanLabel = InputTemplate.getCleanedString(dci.m_frontPageColSpanLabel) != null;
			if (hasColSpanLabel)
				continue;
			sb.append("<td ").append(dci.m_hidden ? " style='display:none' " : "");
		
			boolean isMultiRow = dci.m_nestedCols != null && dci.m_nestedCols.get(0).size() > 1;
			if (hasMultiple && !isMultiRow)
				sb.append(" rowspan='2' ");
//				if(dci.m_hidden) {
//					sb.append("style='display:none'");
//				}
			DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
			int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
			boolean doDate = attribType == cache.DATE_TYPE;
			boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
			int numColspan = 1;//dci.m_nestedCols != null ? dci.m_nestedCols.get(0).size() : 1;
			if (numColspan > 1)
				sb.append(" colspan = '").append(numColspan).append("' ");
			String contentType =  doDate ? "date" : doInterval ? "interval" : attribType == Cache.NUMBER_TYPE || attribType == Cache.INTEGER_TYPE  ? "num" : "text";
			sb.append(" dt_type='").append(contentType).append("'");
			if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
				tempSB.setLength(0);
				FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
				if (tempSB.length() == 0) {
				//no priv
					sb.append("style='display:none'");
				}
			}
			boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0);
			boolean doSortLink = numColspan <= 1 && !ignore;
			String name =  dci.m_name;
			boolean mandatory = dci.m_isMandatory;
			String mandPrefix = mandatory ? "<span style='font-weight:bold'> * </span>" : "";
			if (doSortLink){
				displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>" +(name != null && name.length() != 0 ? name : "&nbsp;")+"</a>";
			}
			else {
				displayLink = name;
			}
			
			if (dci.m_isSelect){
				String actSelectCheckBoxName = "grn_id";
				if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null) {
					actSelectCheckBoxName = "v"+dci.m_dimCalc.m_dimInfo;
				}
				displayLink = (dci.m_name != null && dci.m_name.length() > 0 ? dci.m_name+"<br/>" : "")+"<input type='checkbox' name='select_"+actSelectCheckBoxName+"' class='tn' onclick='setSelectAll(this)'/>";
			}
			sb.append(">").append(displayLink).append(mandPrefix).append("</td>");
		}//template col
		sb.append("<td>&nbsp;</td>");
		sb.append("</tr>");
		sb.append("</thead>");
		out.println(sb);
	}

	public static void printRows(Connection conn, SessionManager session, ArrayList<ArrayList<Value>> dataRows, FrontPageInfo fpi, FormatHelper formatHelper, JspWriter out) throws Exception {
		ArrayList<DimConfigInfo> dciList = fpi.m_frontInfoList;

		int startIndex = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<TBODY>");
		out.println(sb);
		sb.setLength(0);
		Cache cache = session.getCache();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		int endIndexIncl = startIndex;
		for (int i=0,is=dataRows.size();i<is;i++) {
			ArrayList<Value> dataRow = dataRows.get(i);
			sb.append("<tr>");
			for (int j=0,js=dciList.size();j<js;j++) {
				DimConfigInfo dci = dciList.get(j); 
				if (dci == null)
					continue;
				boolean hasColSpanLabel = InputTemplate.getCleanedString(dci.m_frontPageColSpanLabel) != null;
				if (hasColSpanLabel)
					continue;
				DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
				if (dci.m_isSelect) {
					String varName = "grn_id";
					if (dimInfo != null)
						varName = "v"+dimInfo.m_id;
					Value v = dataRow.get(j);
					sb.append("<td  ").append(dci.m_hidden ? " style='display:none' " : "").append("class='cn'><input type='checkbox' name='").append(varName).append("' value='").append(v == null ? Misc.getUndefInt() : v.m_iVal).append("'/></td>");
				}
				else {
					String varName = "v"+dimInfo.m_id;
					int attribType = dimInfo.m_type;
					boolean doNumber = attribType == Cache.NUMBER_TYPE;
					boolean doDate = attribType == Cache.DATE_TYPE;
					boolean read = dci.m_readOnly;
					String css = doNumber ? "nn" : "cn"; 
					sb.append("<td ").append(dci.m_hidden ? " style='display:none' " : "").append("class='").append(css).append("'>");
					Value v = dataRow.get(j);
					if (dci.m_hidden) {
						sb.append("<input type='hidden' name='").append(varName).append("' value='").append(v == null || v.isNull() ? "" : v.toString()).append("'/>");
					}
					else {
						String s = null;
						if (attribType != Cache.LOV_TYPE && attribType != Cache.LOV_NO_VAL_TYPE) {
							s = v == null || v.isNull() ? "&nbsp;" : v.toString(dimInfo, formatHelper.multScaleFactors.get(j), formatHelper.formatters.get(j), session, cache, conn, sdf);
						}
						cache.printDimVals(session, conn, session.getUser(), dimInfo, v == null || v.isNull() ? Misc.getUndefInt() : v.m_iVal, null, sb, varName, true,  "select", false, Misc.getUndefInt(), dci.m_height < 1 ? 1 :dci.m_height, dci.m_width < 0 ? 20 : dci.m_width
				                  , false, null, false, true, read, dci.m_onChange_handler, null,  Misc.getUndefInt(), Misc.getUndefInt()
				                  , null, s,dci.m_isRadio,1, dci);
					}
					sb.append("</td>");
				}//for each col
			}//for each col
			sb.append("<td class='cn'>").append("<img  title= \"Add row\" src=\"").append(com.ipssi.gen.utils.Misc.G_IMAGES_BASE).append("green_check.gif\" onclick='addRowDataSpecial(event)'  />");
			sb.append("</td>");
			sb.append("</tr>");
			out.println(sb);
			sb.setLength(0);
		}//for each row
		sb.append("</tbody></table>");
		out.println(sb);
		sb.setLength(0);
	}
	
	public static void printRowsOld(Connection conn, SessionManager session, ArrayList<Pair<Boolean, ArrayList<Value>>> dataRows, FrontPageInfo fpi, FormatHelper formatHelper, JspWriter out) throws Exception {
		ArrayList<DimConfigInfo> dciList = fpi.m_frontInfoList;

		int startIndex = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<TBODY>");
		out.println(sb);
		sb.setLength(0);
		Cache cache = session.getCache();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		int endIndexIncl = startIndex;
		for (int i=0,is=dataRows.size();i<is;i++) {
			ArrayList<Value> dataRow = dataRows.get(i).second;
			boolean doing1stRow = i == startIndex;
			if (doing1stRow)
			for (int j=startIndex;j<is;j++) {
				endIndexIncl = j;
				if (dataRows.get(j).first) {
					break;
				}
			}
			if (i == endIndexIncl) {
				startIndex = i+1;
				continue;
			}
			if (doing1stRow) {
				ArrayList<Value> grouperRow = dataRows.get(endIndexIncl).second;
				String rowspan = " rowspan='"+ (endIndexIncl-startIndex)+("' ");
				sb.append("<tr>");
				boolean hackSingleColForNonTripSeen = false; 
				for (int j=0,js=dciList.size();j<js;j++) {
					DimConfigInfo dci = dciList.get(j); 
					if (dci == null || dci.m_hidden)
						continue;
					boolean hasColSpanLabel = InputTemplate.getCleanedString(dci.m_frontPageColSpanLabel) != null;
					if (hasColSpanLabel)
						continue;
					DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
					if (dci.m_isSelect) {
						
						String varName = "grn_id";
						if (dimInfo != null)
							varName = "v"+dimInfo.m_id;
						Value v = grouperRow.get(j);
						sb.append("<td ").append(rowspan).append(" class='cn'><input type='checkbox' name='").append(varName).append("' value='").append(v == null ? Misc.getUndefInt() : v.m_iVal).append("'/></td>");
					}
					else {
						if (!hackSingleColForNonTripSeen)
							sb.append("<td ").append(rowspan).append("class='cn'><table border='0' cellpadding='0' cellspacing='0'>");
						hackSingleColForNonTripSeen = true;
						sb.append("<tr><td class='tn'><b>").append(dci.m_name).append(":</b></td><td class='tn'>&nbsp;&nbsp;</td>");
						int attribType = dimInfo.m_type;
						boolean doNumber = attribType == Cache.NUMBER_TYPE;
						sb.append("<td class='tn' align='").append(doNumber ? "right" : "left").append("'>");
						Value v = grouperRow.get(j);
						String s = v == null || v.isNull() ? "&nbsp;" : v.toString(dimInfo, formatHelper.multScaleFactors.get(j), formatHelper.formatters.get(j), session, cache, conn, sdf);
						sb.append(s);
						sb.append("</td></tr>");
					}
				}
				sb.append("</table></td>");
			}
			else {
				sb.append("<tr>");
			}
			for (int j=0,js=dciList.size();j<js;j++) {
				DimConfigInfo dci = dciList.get(j); 
				if (dci == null || dci.m_hidden)
					continue;
				boolean hasColSpanLabel = InputTemplate.getCleanedString(dci.m_frontPageColSpanLabel) != null;
				if (!hasColSpanLabel)
					continue;
				DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
				if (dci.m_isSelect) {
					
					String varName = "grn_id";
					if (dimInfo != null)
						varName = "v"+dimInfo.m_id;
					Value v = dataRow.get(j);
					sb.append("<td class='cn'><input type='checkbox' name='").append(varName).append("' value='").append(v == null ? Misc.getUndefInt() : v.m_iVal).append("'/></td>");
				}
				else {
					int attribType = dimInfo.m_type;
					boolean doNumber = attribType == Cache.NUMBER_TYPE;
					sb.append("<td class='").append(doNumber ? "nn" : "cn").append("'>");
					Value v = dataRow.get(j);
					String s = v == null || v.isNull() ? "&nbsp;" : v.toString(dimInfo, formatHelper.multScaleFactors.get(j), formatHelper.formatters.get(j), session, cache, conn, sdf);
					sb.append(s);
					sb.append("</td>");
				}
			}
			sb.append("</tr>");
			out.println(sb);
			sb.setLength(0);
		}
		sb.append("</TBODY>");
		out.println(sb);
		sb.setLength(0);
	}
	
	
	public static void printHeaderOld(ArrayList<DimConfigInfo> dciList, JspWriter out, SessionManager session) throws Exception {
		StringBuilder sb = new StringBuilder();
		String displayLink = null;
		//ArrayList<DimConfigInfo> fpList = fpi.m_frontInfoList;
		Cache cache = session.getCache();
		boolean hasMultiple = false;//hasNestedColHeader(rows); ... nested printed in its own header ... at the most in case of all read only does it make sense to print in col ... too complex
		HtmlGenerator.printTableStart(sb, null, HtmlGenerator.styleWithAdornment, "DATA_TABLE", true, true, false);
		sb.append("<thead>");
		sb.append("<tr class='").append(CssClassDefinition.getHtmlCssClass(hasMultiple ? 0 : 1)).append("'>");
		StringBuilder tempSB = new StringBuilder();
		ArrayList<DimConfigInfo> row = dciList;
		boolean hackSingleColForNonTripSeen = false;
		for (int j=0,js = row.size(); j<js; j++) {
			DimConfigInfo dci = row.get(j); 
			if (dci == null || dci.m_hidden)
				continue;
			boolean hasColSpanLabel = InputTemplate.getCleanedString(dci.m_frontPageColSpanLabel) != null; 
			if (hackSingleColForNonTripSeen && !hasColSpanLabel && !dci.m_isSelect)
				continue;
			if (!hasColSpanLabel && !dci.m_isSelect)
				hackSingleColForNonTripSeen = true;
			sb.append("<td ");
		
			boolean isMultiRow = dci.m_nestedCols != null && dci.m_nestedCols.get(0).size() > 1;
			if (hasMultiple && !isMultiRow)
				sb.append(" rowspan='2' ");
//				if(dci.m_hidden) {
//					sb.append("style='display:none'");
//				}
			DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
			int attribType = dimInfo != null ? dimInfo.getAttribType() : cache.STRING_TYPE;
			boolean doDate = attribType == cache.DATE_TYPE;
			boolean doInterval = dimInfo != null && "20510".equals(dimInfo.m_subtype);
			int numColspan = 1;//dci.m_nestedCols != null ? dci.m_nestedCols.get(0).size() : 1;
			if (numColspan > 1)
				sb.append(" colspan = '").append(numColspan).append("' ");
			String contentType =  doDate ? "date" : doInterval ? "interval" : attribType == Cache.NUMBER_TYPE || attribType == Cache.INTEGER_TYPE  ? "num" : "text";
			sb.append(" dt_type='").append(contentType).append("'");
			if (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0) {
				tempSB.setLength(0);
				FrontPageInfo.getAllListMenuDiv(session, dci, tempSB, true, null);
				if (tempSB.length() == 0) {
				//no priv
					sb.append("style='display:none'");
				}
			}
			boolean ignore = dci.m_isSelect || (dci.m_innerMenuList != null && dci.m_innerMenuList.size() > 0);
			boolean doSortLink = numColspan <= 1 && !ignore;
			String name =  dci.m_name;
			boolean mandatory = dci.m_isMandatory;
			String mandPrefix = mandatory ? "<span style='font-weight:bold'> * </span>" : "";
			if (doSortLink){
				displayLink = "<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>" +(name != null && name.length() != 0 ? name : "&nbsp;")+"</a>";
			}
			else {
				displayLink = name;
			}
			
			if (dci.m_isSelect){
				String actSelectCheckBoxName = "grn_id";
				if (dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null) {
					actSelectCheckBoxName = "v"+dci.m_dimCalc.m_dimInfo;
				}
				displayLink = (dci.m_name != null && dci.m_name.length() > 0 ? dci.m_name+"<br/>" : "")+"<input type='checkbox' name='select_"+actSelectCheckBoxName+"' class='tn' onclick='setSelectAll(this)'/>";
			}
			sb.append(">").append(displayLink).append(mandPrefix).append("</td>");
		}//template col
		sb.append("</tr>");
		sb.append("</thead>");
		out.println(sb);
	}
}

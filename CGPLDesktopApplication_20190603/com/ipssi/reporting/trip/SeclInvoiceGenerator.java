package com.ipssi.reporting.trip;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FastList;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.HelperPeriodItemList;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.QueryParts;
import com.ipssi.reporting.trip.GeneralizedQueryBuilder.SubQueryPreprocess;
import com.ipssi.reporting.trip.ResultInfo.VirtualVal;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.SeclTprInvoice;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.tracker.colorcode.ColorCodeDao;

public class SeclInvoiceGenerator {
	public static class TPRKeyInfo {
		int tprId;
		String doNumber;
		String minesCode;
		String loadWBOutName;
		public int getTprId() {
			return tprId;
		}
		public void setTprId(int tprId) {
			this.tprId = tprId;
		}
		public String getDoNumber() {
			return doNumber;
		}
		public void setDoNumber(String doNumber) {
			this.doNumber = doNumber;
		}
		public String getMinesCode() {
			return minesCode;
		}
		public void setMinesCode(String minesCode) {
			this.minesCode = minesCode;
		}
		public String getLoadWBOutName() {
			return loadWBOutName;
		}
		public void setLoadWBOutName(String loadWBOutName) {
			this.loadWBOutName = loadWBOutName;
		}
		public TPRKeyInfo(int tprId, String doNumber,
				String minesCode, String loadWBOutName) {
			super();
			this.tprId = tprId;
			this.doNumber = doNumber;
			this.minesCode = minesCode;
			this.loadWBOutName = loadWBOutName;
		}		
	}
	private static DoDetails getDO(Connection conn, String doNumber, int tprId) throws Exception {
		return DoDetails.getDODetails(conn, doNumber, Misc.getUndefInt(), true, tprId);
	}
	private static Mines getRelevantMines(Connection conn, String minesCode) throws Exception {
		
		/*PreparedStatement ps = conn.prepareStatement("select m.id, pm.id, sa.id, a.id,m.dmf_rate, pm.dmf_rate, sa.dmf_rate, a.dmf_rate from mines_details m left outer join mines_details pm on (pm.sn = m.parent_mines_code) left outer join mines_details sa on (m.parent_sub_area_code = sa.sn) left outer join mines_details a on (a.sn = m.parent_area_code) where m.sn=?");
		ps.setString(1, minesCode);
		ResultSet rs = ps.executeQuery();
		int minesId = Misc.getUndefInt();
		if (rs.next()) {
			int mId = Misc.getRsetInt(rs, 1);
			int pmId = Misc.getRsetInt(rs, 2);
			int saId = Misc.getRsetInt(rs, 3);
			int aId = Misc.getRsetInt(rs, 4);
			double mdmf = Misc.getRsetDouble(rs,5);
			double pmdmf = Misc.getRsetDouble(rs,6);
			double sadmf = Misc.getRsetDouble(rs,7);
			double admf = Misc.getRsetDouble(rs,8);
			if (!Misc.isUndef(mdmf))
				minesId = mId;
			else if (!Misc.isUndef(pmdmf))
				minesId = pmId;
			else if (!Misc.isUndef(sadmf))
				minesId = saId;
			else
				minesId = aId;
		}
		
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		Mines retval = (Mines) RFIDMasterDao.get(conn, Mines.class,minesId);*/
		return Mines.getMines(conn, minesCode, Misc.getUndefInt());
	}
	private static Mines getRelevantMinesForWB(Connection conn, String wbCode, HashMap<String, Mines> minesList) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select mines_code from secl_workstation_mines_group where workstation_code = ?");
		ps.setString(1, wbCode);
		ResultSet rs = ps.executeQuery();
		Mines retval = null;
		while (rs.next()) {
			String m = rs.getString(1);
			retval = minesList.get(m);
			if (retval == null) {
				retval = getRelevantMines(conn,m);
				if (retval != null)
					minesList.put(m, retval);
			}
			if (retval != null)
				break;
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return retval;
	}
	private static void calcInvoice(Connection conn, ArrayList<TPRKeyInfo> tprList) throws Exception {
		HashMap<String, DoDetails> doLists = new HashMap<String, DoDetails>();
		HashMap<String, Mines> relevantMinesFromSourceMines = new HashMap<String, Mines>();
		HashMap<String, Mines> relevantMinesFromWB = new HashMap<String, Mines>();
		for (int i=0,is = tprList.size(); i<is; i++) {
			TPRKeyInfo tprInfo = tprList.get(i);
			DoDetails doDetail = null;//doLists.get(tprInfo.doNumber);
			Mines mines = null;
			//if (doDetail == null) {
			doDetail = SeclInvoiceGenerator.getDO(conn, tprInfo.doNumber,tprInfo.getTprId());
				//if (doDetail != null)
				//	doLists.put(tprInfo.doNumber, doDetail);
			//}
			if (doDetail == null)
				continue;
			if (tprInfo.minesCode != null && tprInfo.minesCode.length() != 0) {
				mines = relevantMinesFromSourceMines.get(tprInfo.minesCode);
				if (mines == null) {
					mines = SeclInvoiceGenerator.getRelevantMines(conn, tprInfo.minesCode);
					if (mines != null)
						relevantMinesFromSourceMines.put(tprInfo.minesCode, mines);
				}
			}
			if (mines == null && tprInfo.loadWBOutName != null && tprInfo.loadWBOutName.length() != 0) {
				mines = relevantMinesFromWB.get(tprInfo.loadWBOutName);
				if (mines == null) {
					mines = SeclInvoiceGenerator.getRelevantMinesForWB(conn, tprInfo.loadWBOutName, relevantMinesFromSourceMines);
					if (mines != null) {
						relevantMinesFromWB.put(tprInfo.loadWBOutName, mines);
					}
				}
			}
			if (mines == null)
				continue;
			TPRecord tpRecord = (TPRecord) RFIDMasterDao.get(conn, TPRecord.class, tprInfo.tprId);
			SeclTprInvoice invoice = SeclTprInvoice.getSeclTprInvoice(conn, tpRecord, doDetail, mines);
			invoice.save(conn);
			if (!conn.getAutoCommit())
				conn.commit();
		}
	}
	
	public static void handleAndRedirToInvoiceGen(FrontPageInfo fpi, SessionManager session, SearchBoxHelper searchBoxHelper, HttpServletResponse response,String[] tprIds, int action) throws Exception {
		//action = 0 - only generate for tprIds passed
		//action = 1 - only show
		//action = 2 - generate if missing and show
		//1. first get tpr id
		//2. generate invoice regardless of it being generaeted
		ArrayList<TPRKeyInfo> tprList = new ArrayList<TPRKeyInfo>();
		
		
		StringBuilder tprs = null;
		if(tprIds != null && tprIds.length > 0){
			for (int i = 0; i < tprIds.length; i++) {
				if(tprs == null)
					tprs = new StringBuilder();
				else
					tprs.append(", ");
				tprs.append(tprIds[i]);
			}
		}
		if (action == 0) {
			if (tprIds == null || tprIds.length == 0)
				return;
			String sel = "select tp_record.tpr_id,tp_record.do_number, tp_record.mines_code, tp_record.load_wb_out_name from tp_record left outer join secl_tpr_invoice on (tp_record.tpr_id = secl_tpr_invoice.tpr_id) where tp_record.tpr_id in ("+tprs.toString()+") and (secl_tpr_invoice.invoice_locked is null or secl_tpr_invoice.invoice_locked = 0) and tp_record.load_gross is not null and tp_record.load_tare is not null and (tp_record.load_gross - tp_record.load_tare) > 0 ";
			Connection conn = session.getConnection();
			PreparedStatement ps = conn.prepareStatement(sel);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TPRKeyInfo tpInfo = new TPRKeyInfo(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
				tprList.add(tpInfo);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);	
			calcInvoice(conn, tprList);
		}
		else  {
			GeneralizedQueryBuilder gqb = new GeneralizedQueryBuilder();
			StringBuilder sb = new StringBuilder();
			ResultSet rs = null;
			ResultInfo resultInfo = null;
			QueryParts qp = null;
			String query = null;
			long startTime = System.currentTimeMillis();
			DimInfo d20765 = DimInfo.getDimInfo(20765);
			String pgContext = session.getParameter("page_context");
			ResultInfo.FormatHelper formatHelper = ResultInfo.getFormatHelper(fpi.m_frontInfoList, session, searchBoxHelper);
			int portNodeId = gqb.getPortNodeId(session, fpi.m_frontSearchCriteria, searchBoxHelper);
			ArrayList<Pair<Integer,Long>> vehicleList = null;
			boolean hasStartEndPERIOD = false;
			int rollupAtJava = fpi.m_doRollupAtJava ? 1 : 0;
			if (!Misc.g_doRollupAtJava) //20160420 ... now rollup at java preferred
				rollupAtJava = 0;
			qp = gqb.buildQueryParts(fpi.m_frontInfoList, fpi.m_frontSearchCriteria, fpi.m_hackTrackDriveTimeTableJoinLoggedData, session,
					searchBoxHelper, formatHelper, fpi.m_colIndexLookup, gqb.getDriverObjectFromName(fpi.m_driverObjectLocTracker), fpi.m_orderIds, null, false, Misc.getUndefInt(), rollupAtJava, false,0);
			hasStartEndPERIOD = qp.hasAtPeriodCol;
			sb = new StringBuilder();
			sb.append("select tp_record.tpr_id,tp_record.do_number, tp_record.mines_code, tp_record.load_wb_out_name ")
			.append(qp.m_fromClause)
			.append(qp.m_whereClause).append(" and (secl_tpr_invoice.tpr_id is null or secl_tpr_invoice.load_gross < 0 or secl_tpr_invoice.load_tare < 0 or secl_tpr_invoice.load_gross is null or secl_tpr_invoice.load_tare is null) ");
			
			System.out.println("#############"+sb.toString());
			Connection conn = session.getConnection();
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				TPRKeyInfo tpInfo = new TPRKeyInfo(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
				tprList.add(tpInfo);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			calcInvoice(conn, tprList);
			query = gqb.buildQuery(session, qp, vehicleList,false);
			if (fpi.m_customField1 != null && fpi.m_customField1.length() > 0)
				query += " limit "+fpi.m_customField1;
			System.out.println("#############"+query);
			//qb.printPage(_dbConnection , fPageInfo, _session , searchBoxHelper,null,buffer, reportType, reportName,Misc.getUndefInt(),null, null,null, null, null);
			ByteArrayOutputStream buffer = null;
			buffer = new ByteArrayOutputStream();
			gqb.printPage(conn , fpi, session , searchBoxHelper,null,buffer, Misc.EXCEL, "Invoice Data",Misc.getUndefInt(),null, null,null, null, null);
			if(buffer != null && buffer.size() > 0){
				ServletOutputStream stream = response.getOutputStream();
				response.setContentType("application/xls");
				response.addHeader("Content-Disposition", "attachment; filename="+"InvoiceData.xls");
				response.setContentLength(buffer.size());		     
				stream.write(buffer.toByteArray());
			}
			System.out.println("[REPORT GENRATION TIME:"+(System.currentTimeMillis() - startTime)+"]");
		}
	}
}

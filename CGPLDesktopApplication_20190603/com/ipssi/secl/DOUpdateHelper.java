package com.ipssi.secl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.*;
import com.ipssi.gen.utils.DimCalc;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.input.InputTemplate;
import com.ipssi.input.WorkflowInfoAskedByUser;
import com.ipssi.rfid.beans.DOUpdInfo;
import com.ipssi.workflow.WorkflowDef;
import com.ipssi.workflow.WorkflowHelper;

public class DOUpdateHelper {
	public static void saveCommunicationResult(ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> doupdList, Connection conn, String sendToWBCode)  {
		PreparedStatement psGetWbCode = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("insert into secl_remote_comm_result(do_id, do_number, wb_id, wb_code, lifted_qty, alloc_qty, lock_status, result_code, sent_to_wb_id, sent_to_wb_code, created_on) "+
					"values (?,?,?,?,?,?,?,?,?,?,now())"
					);
			psGetWbCode = conn.prepareStatement("select code from secl_workstation_details where id = ? ");
			for (int i=0,is=doupdList == null ? 0 : doupdList.size(); i<is; i++) {
				Pair<Integer, ArrayList<DOUpdInfo>> updTo = doupdList.get(i);
				psGetWbCode.setInt(1, updTo.first);
				String toWbCode = null;
				rs = psGetWbCode.executeQuery();
				if (rs.next()) {
					toWbCode = rs.getString(1);
				}
				rs = Misc.closeRS(rs);
				for (int j=0,js=updTo.second == null ? 0 : updTo.second.size(); j<js; j++) {
					DOUpdInfo info = updTo.second.get(j);
					Misc.setParamInt(ps, info.getDoId(), 1);
					ps.setString(2, info.getDoNumber());
					Misc.setParamInt(ps, info.getWbId(), 3);
					ps.setString(4, info.getWbCode());
					Misc.setParamDouble(ps, info.getLastQtyLifted(), 5);
					Misc.setParamDouble(ps, info.getCurrentAllocation(), 6);
					Misc.setParamInt(ps, info.getLockStatus(), 7);
					Misc.setParamInt(ps, info.getResultStatus(), 8);
					Misc.setParamInt(ps, updTo.first, 9);
					ps.setString(10, sendToWBCode);
					ps.addBatch();
				}
				
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);
			if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	public static StringBuilder printDoUpdError(SessionManager session) {
		ArrayList<DOUpdInfo> errList = (ArrayList<DOUpdInfo>)session.getAttributeObj("_doUpdErr");
		if (errList != null && errList.size() > 0) {
			String header = "Updates to remote WB failed. Please goto WB and update it manually otherwise operations maybecome inconsistent";
			return printDoUpdError(errList,header);
		}
		return new StringBuilder();
	}
	public static StringBuilder printDoUpdError(ArrayList<DOUpdInfo> errList, String header) {
		StringBuilder sb = new StringBuilder();
		if (errList != null && errList.size() > 0) {
			if (header != null  && header.length() > 0) {
				sb.append("<div class='shl'>").append(header).append("</div>");
			}
			for (DOUpdInfo err : errList) {
				sb.append("<div class='tn'> WB:").append(err.getWbCode()).append("&nbsp;&nbsp;DO:").append(err.getDoNumber());
				if (!Misc.isUndef(err.getCurrentAllocation())) {
					sb.append("&nbsp;&nbsp;Allocation To:").append(err.getCurrentAllocation());
				}
				if (!Misc.isUndef(err.getLastQtyLifted())) {
					sb.append("&nbsp;&nbsp;Lifted Qty To:").append(err.getLastQtyLifted());
				}
				if (!Misc.isUndef(err.getLockStatus())) {
					sb.append("&nbsp;&nbsp;Lock Status To:").append(err.getLockStatus());
				}
				sb.append("</div>");
			}
			sb.append("<div width='100%' class='tn'><hr noshade size='1'></div>");
		}
		return sb;
	}
	
	public static void handleWBMigration(SessionManager session) throws Exception {
		String xmlDataString = session == null ? null : session.getParameter("XML_DATA");
		System.out.println("xmlDataString :" +xmlDataString);
		Document xmlDoc = xmlDataString != null && xmlDataString.length() != 0 ? MyXMLHelper.loadFromString(xmlDataString) : null;
		Element topElem = xmlDoc == null ? null : xmlDoc.getDocumentElement();
		int fromWBId = Misc.getParamAsInt(session.getParameter("from_wb_id"), Misc.getParamAsInt(session.getParameter("pv93408")));
		int toWBId = Misc.getParamAsInt(session.getParameter("to_wb_id"));
		ArrayList<Triple<Integer, Double, Double>> toMigrate = new ArrayList<Triple<Integer, Double, Double>>();
		for (Node n = topElem.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() != 1) {
				continue;
			}
			Element e = (Element) n;
			int doId = Misc.getParamAsInt(e.getAttribute("do_id"));
			if (Misc.isUndef(doId))
				continue;
			double propAlloc = Misc.getParamAsDouble(e.getAttribute("current_allocation"));
			double proplift = Misc.getParamAsDouble(e.getAttribute("lifted_qty"));
			//		//toMigrateDO: first = doId, second = allocQty specified by user, third = liftedQty specified by user
			toMigrate.add(new Triple<Integer, Double, Double>(doId, propAlloc, proplift));
			
		}
		if (toMigrate.size() > 0)
			DOUpdateHelper.doMigration(session.getConnection(), session, fromWBId, toWBId, toMigrate);
	}
	public static class MigrationInfo {
		private int doId;
		private int targetWBId;
		private double totalTransferAllocation;
		private double fromLiftedQty;
		private double fromAllocQty;
		private boolean existsInToWBId = false;
		
		private int fromResultStatus;
		private String fromResultMessage;
		private int toResultStatus;
		private String toResultMessage;
		public int getDoId() {
			return doId;
		}
		public void setDoId(int doId) {
			this.doId = doId;
		}
		public int getTargetWBId() {
			return targetWBId;
		}
		public void setTargetWBId(int targetWBId) {
			this.targetWBId = targetWBId;
		}
		public double getTotalTransferAllocation() {
			return totalTransferAllocation;
		}
		public void setTotalTransferAllocation(double totalTransferAllocation) {
			this.totalTransferAllocation = totalTransferAllocation;
		}
		public int getFromResultStatus() {
			return fromResultStatus;
		}
		public void setFromResultStatus(int fromResultStatus) {
			this.fromResultStatus = fromResultStatus;
		}
		public String getFromResultMessage() {
			return fromResultMessage;
		}
		public void setFromResultMessage(String fromResultMessage) {
			this.fromResultMessage = fromResultMessage;
		}
		public int getToResultStatus() {
			return toResultStatus;
		}
		public void setToResultStatus(int toResultStatus) {
			this.toResultStatus = toResultStatus;
		}
		public String getToResultMessage() {
			return toResultMessage;
		}
		public void setToResultMessage(String toResultMessage) {
			this.toResultMessage = toResultMessage;
		}
		public MigrationInfo(int doId, int targetWBId, double fromAllocQty, double fromLiftedQty, double totalTransferAllocation) {
			super();
			this.doId = doId;
			this.targetWBId = targetWBId;
			this.totalTransferAllocation = totalTransferAllocation;
			this.fromLiftedQty = fromLiftedQty;
			this.fromAllocQty = fromAllocQty;
		}
		public double getFromLiftedQty() {
			return fromLiftedQty;
		}
		public void setFromLiftedQty(double fromLiftedQty) {
			this.fromLiftedQty = fromLiftedQty;
		}
		public double getFromAllocQty() {
			return fromAllocQty;
		}
		public void setFromAllocQty(double fromAllocQty) {
			this.fromAllocQty = fromAllocQty;
		}
		public boolean isExistsInToWBId() {
			return existsInToWBId;
		}
		public void setExistsInToWBId(boolean existsInToWBId) {
			this.existsInToWBId = existsInToWBId;
		}
	}
	public static void doMigration(Connection conn,SessionManager session,  int fromWBId, int toWBId, ArrayList<Triple<Integer, Double, Double>> toMigrateDO) throws Exception {
		//toMigrateDO: first = doId, second = allocQty specified by user, third = liftedQty specified by user
		//MigrationInfo will get us current alloc and qty lifted of from and targetAllocation on to (i.e liftedQty to not change
		String fromWBCode = null;
		String toWBCode = null;
		PreparedStatement ps = conn.prepareStatement("select id, code from secl_workstation_details where id = ? or id = ?");
		ps.setInt(1, fromWBId);
		ps.setInt(2, toWBId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			String str = rs.getString(2);
			if (id == fromWBId)
				fromWBCode = str;
			if (id == toWBId)
				toWBCode = str;
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ArrayList<MigrationInfo> dosMigrationInfo = calcMigrationInfo(conn, fromWBId, toWBId,  toMigrateDO);
		ArrayList<Integer> doIds = new ArrayList<Integer>();
		PreparedStatement psIns = conn.prepareStatement("insert into current_do_status (lifted_qty, current_allocation, do_id, wb_id) values (?,?,?,?) ");
		PreparedStatement psUpd = conn.prepareStatement("update current_do_status set lifted_qty=?, current_allocation=? where do_id=? and wb_id=?");
		String userComment = "Migrating WB from "+fromWBCode+" to "+toWBCode;
		InputTemplate inputTemplate = InputTemplate.getTemplate(session.getCache(), session.getConnection(), "secl_dorr_template", 2, "secl_dorr_allocation_inp.xml", session); 
		for (int i=0,is=dosMigrationInfo == null ? 0 : dosMigrationInfo.size(); i<is; i++) {
			MigrationInfo migrationInfo =dosMigrationInfo.get(i);
			int doId = migrationInfo.getDoId();
			doIds.add(doId);
			double fromAlloc = migrationInfo.getFromAllocQty();
			double fromQty = migrationInfo.getFromLiftedQty();
			double toAlloc = migrationInfo.getTotalTransferAllocation();
			Misc.setParamDouble(psUpd, Misc.getUndefDouble(), 1);
			Misc.setParamDouble(psUpd, fromQty, 2);
			psUpd.setInt(3, doId);
			psUpd.setInt(4, fromWBId);
			psUpd.addBatch();
			
			PreparedStatement psToUse = migrationInfo.existsInToWBId ? psUpd : psIns;
			Misc.setParamDouble(psToUse, Misc.getUndefDouble(), 1);
			Misc.setParamDouble(psToUse, migrationInfo.getTotalTransferAllocation(), 2);
			psToUse.setInt(3, doId);
			psToUse.setInt(4, toWBId);

			psToUse.addBatch();			
		}
		psUpd.executeBatch();
		psIns.executeBatch();
		psUpd = Misc.closePS(psUpd);
		psIns = Misc.closePS(psIns);
		WorkflowHelper.doWorkflowCreateUpdateEtc(conn, session, doIds, 62, inputTemplate.getRows(), true, null, null, null, session.getUser().getUserId(), userComment,WorkflowDef.WORKFLOW_TYPE_REG);
		//this.workflowId = workflowId;
		//this.doApprove = doingApproval;
		//this.comments = comments;
		ArrayList<Integer> wkfTypeIds = new ArrayList<Integer>();
		wkfTypeIds.add(52);
		ArrayList<ArrayList<Integer>> wkfList = WorkflowHelper.getActiveWorkflowsFor(conn, doIds, wkfTypeIds);
		ArrayList<WorkflowInfoAskedByUser> workflowToProcess = new ArrayList<WorkflowInfoAskedByUser>();
		for (int i=0,is=wkfList.size(); i<is; i++) {
			workflowToProcess.add(new WorkflowInfoAskedByUser(wkfList.get(i).get(0), true, userComment, true));
		}
		WorkflowInfoAskedByUser.updateWorkflowInfoAskedFromWorkflowId(conn, workflowToProcess);
		inputTemplate.handleApproveRejectForInpTemplateGeneral(conn, workflowToProcess, session);
		ArrayList<Pair<Boolean, Integer>> tprMigrateResult = DOUpdInfo.migrateOpenTPR(conn, fromWBCode, toWBCode, doIds);
		
		//1. set the new values in fromWBId's proposed allocation
		//2. set the new values in toWBIds proposed allocation
		//3. getDoUpdateInfo for affected DO's
		//4. for approve
		//2. getDoUpdateInfo
		//3.
		//4. force approve
		
		//2. updateDOInfo remotely
		//2.  set the values 
		//2. calculate updateInfo 
	}
	
	
	private static ArrayList<MigrationInfo> calcMigrationInfo(Connection conn, int fromWBId, int toWBId, ArrayList<Triple<Integer, Double, Double>> toMigrateDO) throws Exception {
		//toMigrateDO: first = doId, second = allocQty specified by user, third = liftedQty specified by user
		//1st get the fromWB's allocation and lifted qty
		//then get toWB's any existing allocation for the target DO
		HashMap<Integer, Triple<Integer, Double, Double>> forDOLookupOfUser = new HashMap<Integer, Triple<Integer, Double, Double>>();
		ArrayList<MigrationInfo> retval = new ArrayList<MigrationInfo> ();
		StringBuilder sb = new StringBuilder();
		PreparedStatement ps = null;
		ResultSet rs = null;
		sb.append("select dorr.id, dorr.do_number, alloc_apprvd.wb_id, alloc_apprvd.wb_code, alloc_apprvd.current_allocation, alloc_apprvd.lifted_qty from ")
		.append(" mines_do_details dorr ")
		.append(" left outer join current_do_status_apprvd alloc_apprvd on (alloc_apprvd.do_id = dorr.id) ")
		.append(" where alloc_apprvd.wb_id = ? ");
		int len = sb.length();
		sb.append(" and dorr.id in (");
		int tempLen = sb.length();
		int afterWhr = sb.length();
		for (int i=0,is=toMigrateDO == null ? 0 : toMigrateDO.size(); i<is; i++) {
			if (i != 0)
				sb.append(",");
			forDOLookupOfUser.put(toMigrateDO.get(i).first, toMigrateDO.get(i));
			sb.append(toMigrateDO.get(i).first);
		}
		sb.append(") ");
		if (sb.length() == tempLen)
			sb.setLength(len);
		sb.append(" order by dorr.id ");
		HashMap<Integer, MigrationInfo> forDOLookup = new HashMap<Integer, MigrationInfo>();
		try {
			ps = conn.prepareStatement(sb.toString());
			ps.setInt(1, fromWBId);
			rs = ps.executeQuery();
			while (rs.next()) {
				int doId = rs.getInt(1);
				double alloc = rs.getDouble(5);
				double lifted = rs.getDouble(6);
				
				Triple<Integer, Double, Double> userInfo = forDOLookupOfUser.get(doId);
				if (userInfo != null && userInfo.second != null && !Misc.isUndef(userInfo.second)) {
					alloc = userInfo.second;
				}
				if (userInfo != null && userInfo.third != null && !Misc.isUndef(userInfo.third)) {
					lifted = userInfo.third;
				}
				double balance = alloc - lifted;
				if (!Misc.isEqual(balance, 0) && balance > 0) {
					MigrationInfo info = new MigrationInfo(rs.getInt(1), toWBId, alloc, lifted, balance);
					forDOLookup.put(info.getDoId(), info);
					retval.add(info);
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		
		//now look at right and see if there are any existing allocaiton
		try {
			ps = conn.prepareStatement(sb.toString());
			ps.setInt(1, toWBId);
			rs = ps.executeQuery();
			while (rs.next()) {
				int doId = rs.getInt(1);
				MigrationInfo info = forDOLookup.get(doId);
				if (info != null) {
					info.setTotalTransferAllocation(info.getTotalTransferAllocation()+rs.getDouble(5));
					info.setExistsInToWBId(true);
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	public static ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> getDOUpdateInfoOnUpdOfLock(Connection conn, ArrayList<Integer> doIds) {
		ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> retval = new ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select dorr.id, dorr.do_number, alloc.wb_id, alloc.wb_code, dorr.lock_status from ")
			.append(" mines_do_details dorr join current_do_status alloc on (dorr.id = alloc.do_id)")
			;			
			int len = sb.length();
			sb.append(" where dorr.id in (");
			int afterWhr = sb.length();
			if (doIds != null)
				Misc.convertInListToStr(doIds, sb);
			if (afterWhr != sb.length()) {
				sb.append(") ");
			}
			else {
				sb.setLength(len);
			}
			sb.append(" order by alloc.wb_id, dorr.id ");
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			int prevWBId = Misc.getUndefInt();
			ArrayList<DOUpdInfo> prevList = null;
			while (rs.next()) {
				int doId = Misc.getRsetInt(rs, 1);
				String doNumber = rs.getString(2);
				int wbId = Misc.getRsetInt(rs, 3);
				if (Misc.isUndef(doId) || Misc.isUndef(wbId))
					continue;
				if (wbId != prevWBId) {
					prevList = null;
				}
				if (prevList == null) {
					prevList = new ArrayList<DOUpdInfo>();
					retval.add(new Pair<Integer, ArrayList<DOUpdInfo>>(wbId, prevList));
				}
				String wbCode = rs.getString(4);
				int lockStatus = rs.getInt(5);
					
				DOUpdInfo info = new DOUpdInfo();
				info.setDoNumber(doNumber);
				info.setDoId(doId);
				info.setWbCode(wbCode);
				info.setWbId(wbId);
				info.setLockStatus(lockStatus);
				prevList.add(info);
				prevWBId = wbId;
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	
	public  static ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> getDOUpdateInfoOnUpdOfDO(Connection conn, int doId) {
		ArrayList<Integer> doIds = new ArrayList<Integer>();
		doIds.add(doId);
		return getDOUpdateInfoOnUpdOfDO(conn, doIds);
	}
	
	public  static ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> getDOUpdateInfoOnUpdOfDO(Connection conn, ArrayList<Integer> doIds) {
		ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> retval = new ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select dorr.id, dorr.do_number, alloc.wb_id, alloc.wb_code, alloc.current_allocation, alloc.lifted_qty, alloc_apprvd.current_allocation, alloc_apprvd.lifted_qty from ")
			.append(" mines_do_details dorr join current_do_status alloc on (dorr.id = alloc.do_id)")
			.append(" left outer join current_do_status_apprvd alloc_apprvd on (alloc_apprvd.do_id = alloc.do_id and alloc_apprvd.wb_id = alloc.wb_id) ")
			.append(" where (alloc_apprvd.do_id is null or (alloc.lifted_qty is not null and alloc.lifted_qty <> alloc_apprvd.lifted_qty) ")
			.append(" or (alloc_apprvd.current_allocation is null and alloc.current_allocation is not null) or (alloc_apprvd.current_allocation is not null and alloc.current_allocation is null) ")
			.append(" or (alloc_apprvd.current_allocation <> alloc.current_allocation) )")
			;
			
			int len = sb.length();
			sb.append(" and dorr.id in (");
			int afterWhr = sb.length();
			if (doIds != null)
				Misc.convertInListToStr(doIds, sb);
			if (afterWhr != sb.length()) {
				sb.append(") ");
			}
			else {
				sb.setLength(len);
			}
			sb.append(" order by alloc.wb_id, dorr.id ");
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			int prevWBId = Misc.getUndefInt();
			ArrayList<DOUpdInfo> prevList = null;
			while (rs.next()) {
				int doId = Misc.getRsetInt(rs, 1);
				String doNumber = rs.getString(2);
				int wbId = Misc.getRsetInt(rs, 3);
				if (Misc.isUndef(doId) || Misc.isUndef(wbId))
					continue;
				if (wbId != prevWBId) {
					prevList = null;
				}
				if (prevList == null) {
					prevList = new ArrayList<DOUpdInfo>();
					retval.add(new Pair<Integer, ArrayList<DOUpdInfo>>(wbId, prevList));
				}
				String wbCode = rs.getString(4);
				double allocQty = Misc.getRsetDouble(rs, 5);
				double liftQty = Misc.getRsetDouble(rs, 6);
				double allocQtyApprvd = Misc.getRsetDouble(rs, 7);
				double liftQtyApprvd = Misc.getRsetDouble(rs, 8);
				if (!Misc.isUndef(liftQty) && Misc.isEqual(liftQty, liftQtyApprvd))
					liftQty = Misc.getUndefDouble();
				if (!Misc.isUndef(allocQty) && Misc.isEqual(allocQty, allocQtyApprvd))
					allocQty = Misc.getUndefDouble();
					
				DOUpdInfo info = new DOUpdInfo();
				info.setDoNumber(doNumber);
				info.setDoId(doId);
				info.setWbCode(wbCode);
				info.setWbId(wbId);
				info.setLastQtyLifted(liftQty);
				info.setCurrentAllocation(allocQty);
				
				prevList.add(info);
				prevWBId = wbId;
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return retval;
	}
	public static ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> prepDORRAllocationForFinApproval(Connection conn, int doId, int userId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("update current_do_status set lifted_qty=proposed_lifted where do_id=?");
		ps.setInt(1, doId);
		ps.execute();
		ps = Misc.closePS(ps);
		ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> doUpdInfo = DOUpdateHelper.getDOUpdateInfoOnUpdOfDO(conn, doId);
		saveAllocChangeHistory(conn, doId, userId);
		ps = conn.prepareStatement("update current_do_status set proposed_lifted=null where do_id=?");
		ps.setInt(1, doId);
		ps.execute();
		ps = Misc.closePS(ps);
		//PreparedStatement updRegInfoLiftedQty = conn.prepareStatement("update current_do_status alloc join current_do_status_apprvd alloc_apprvd on (alloc.do_id = alloc_apprvd.do_id and alloc.wb_id = alloc_apprvd.wb_id) "+
		//	" set alloc.lifted_qty = alloc_apprvd.lifted_qty where alloc.lifted_qty is null and alloc_apprvd.lifted_qty is not null and alloc.do_id = ?");
		ps = conn.prepareStatement("update current_do_status alloc join current_do_status_apprvd alloc_apprvd on (alloc.do_id = alloc_apprvd.do_id and alloc.wb_id = alloc_apprvd.wb_id) "+
		" set alloc.lifted_qty = (case when alloc.lifted_qty is null then alloc_apprvd.lifted_qty else alloc.lifted_qty end) where alloc.do_id = ?");
		ps.setInt(1, doId);
		ps.execute();
		ps = Misc.closePS(ps);
		//TODO update history information re changes ... and then approve
		return doUpdInfo;
	}
	
	public static ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> mergeDoUpdInfo(ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> fromList, ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> toList) {
		for (int i=0,is=fromList == null ? 0 : fromList.size(); i<is; i++) {
			int wbId = fromList.get(i).first;
			ArrayList<DOUpdInfo> doList = fromList.get(i).second;
			int idx = -1;
			for (int j=0,js=toList == null ? 0 : toList.size(); j<js; j++) {
				if (toList.get(j).first == wbId) {
					idx = j;
					break;
				}
			}
			ArrayList<DOUpdInfo> addTo = null;
			if (idx < 0) {
				if (toList == null)
					toList = new ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>>();
				addTo = new ArrayList<DOUpdInfo>();
				toList.add(new Pair<Integer, ArrayList<DOUpdInfo>>(wbId, addTo) );
			}
			else {
				addTo = toList.get(idx).second;
			}
			for (int j=0,js=fromList.get(i).second.size(); j<js; j++) {
				addTo.add(fromList.get(i).second.get(j));
			}
		}
		return toList;
	}

	public static void rememberErroredDoUpdInfo(ArrayList<Pair<Integer, ArrayList<DOUpdInfo>>> doUpdInfo, ArrayList<DOUpdInfo> updsWithError) {
		for (int j=0,js = doUpdInfo == null ? 0 : doUpdInfo.size(); j<js; j++) {
			ArrayList<DOUpdInfo> updList = doUpdInfo.get(j).second;
			for (int k=0,ks = updList == null ? 0 : updList.size(); k<ks; k++) {
				if (updList.get(k).getResultStatus() != 1)
					updsWithError.add(updList.get(k));
			}
		}
	}
	
	public static void saveAllocChangeHistory(Connection conn, int doId, int userId) {
		PreparedStatement ps = null;
		try {
			// do_id int,
			StringBuilder sb = new StringBuilder("insert into secl_wb_alloc_change_history(do_id, do_number, approved_on, approved_by, initial_qty_alloc, initial_lifted "+
					", lock_status, wb_id, wb_code, proposed_lifted_qty, prior_lifted_qty, proposed_current_allocation, prior_current_allocation)  ( "+
					" select dorr.id, dorr.do_number, now(), ?, dorr.qty_alloc, dorr.qty_already_lifted "+
					" ,dorr.lock_status, alloc.wb_id, alloc.wb_code, (case when alloc.lifted_qty is not null and alloc.lifted_qty <> alloc_apprvd.lifted_qty then alloc.lifted_qty else null end), alloc_apprvd.lifted_qty, alloc.current_allocation, alloc_apprvd.current_allocation "+
					" from mines_do_details dorr left outer join current_do_status alloc on (alloc.do_id = dorr.id) left outer join current_do_status_apprvd alloc_apprvd "+
					" on (alloc.do_id = alloc_apprvd.do_id and alloc.wb_id = alloc_apprvd.wb_id) "
					).append(" where dorr.id = ?)  ");
			ps = conn.prepareStatement(sb.toString());
			ps.setInt(1, userId);
			ps.setInt(2, doId);
			ps.execute();
			ps = Misc.closePS(ps);
			if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	
}

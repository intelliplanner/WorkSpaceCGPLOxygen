package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SeclRemoteConnManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.SeclRemoteConnManager.Station;
import com.ipssi.gen.utils.SeclRemoteConnManager.StationTree;
import com.ipssi.rfid.beans.RemoteTPRHelper.*;

public class RemoteTPRSync {
	
	public static LocalTPRChangeResult onApproveApplyTPRLocally(Connection conn, int workflowId, int tprId, HashMap<String, Value> newTPR, HashMap<String, Value> oldTPR, boolean doingWtWf) throws Exception {
		try {
			ArrayList<String> wbCodeOfInterest = RemoteTPRHelper.getWBOfInterest(newTPR, oldTPR);
			String meCode = SeclRemoteConnManager.getMyCode(conn);
			Station meStation = null;
			ArrayList<Station> filteredAncestorListExclMe = SeclRemoteConnManager.getStationAndParentInfo(conn, meCode);
			if (filteredAncestorListExclMe.size() > 0) {
				meStation = filteredAncestorListExclMe.get(0);
				filteredAncestorListExclMe.remove(0);
			}
			StationTree meTree = SeclRemoteConnManager.getRelevantPartOfTree(conn, meStation, wbCodeOfInterest);
			if (meStation == null)
				meStation = meTree.me;
			ArrayList<Pair<String, Integer>> colsChanged = RemoteTPRHelper.identifyFieldChanges(newTPR, oldTPR, null, doingWtWf);
			
			boolean doCurrDoStatus = true;
			boolean createEntryForTarget = 1 != RemoteTPRHelper.getTPRInt(newTPR,"propagate_changes_in_real_time");
			String actionId = RemoteTPRSync.getIdForAction(conn, workflowId, tprId);
			Pair<Boolean, ChangeRecord> changeResult = RemoteTPRSync.applyTPRChangeOnCurrNode(conn, actionId, newTPR, oldTPR, tprId, colsChanged
					, meTree, meStation
					, doCurrDoStatus, doingWtWf, false, createEntryForTarget, createEntryForTarget);
			Value challanNoVal = newTPR.get("challan_no");
			String challanNo = challanNoVal == null ? null : challanNoVal.getStringVal();
			//changeResult.second.save(conn, actionId, challanNo, meStation.getCode(), 2);
			LocalTPRChangeResult retval = new LocalTPRChangeResult(newTPR,oldTPR, meStation,filteredAncestorListExclMe,meTree, doCurrDoStatus, createEntryForTarget,changeResult.first,  actionId, doingWtWf, colsChanged, tprId, challanNo); 
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static String onApproveApplyTPRRemotely(Connection conn, int tprId, LocalTPRChangeResult changeNeed)  {
		//returns the action taken results
		String retval = null;
		Value challanNoVal = changeNeed.newTPR.get("challan_no") ;
		String challanNo = challanNoVal == null ? null : challanNoVal.getStringVal();
		applyChangesDownOnTree(null,changeNeed.actionId, Misc.getUndefInt(), conn, tprId, challanNo
				, changeNeed.meTree, false, changeNeed.newTPR
				, changeNeed.colsChanged
				, changeNeed.doCurrDoStatus, changeNeed.createEntryForTarget, changeNeed.doingWtWf, false, conn, 0);
		applyChangesUpOnTree(conn, changeNeed.actionId, tprId, challanNo, changeNeed.meStation, changeNeed.filteredAncestorListExclMe, changeNeed.newTPR, changeNeed.colsChanged, changeNeed.doCurrDoStatus, changeNeed.createEntryForTarget, changeNeed.doingWtWf, false);
		retval = RemoteTPRSync.getDisplayableActionTaken(conn, changeNeed.actionId);
		return retval;
	}
	
	public static void fetchFromTopAndApply(Connection desktopConn, Connection remoteConn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		if (desktopConn == null || remoteConn == null)
			return;
		try {
			//from to current and leave it at that - the end points should eventually pick and bring it to their level
			//create table remote_tprchange_action(action_id varchar(64), wb_code varchar(24), tpr_id int, challan_no varchar(24), created_on timestamp null default null, action int, change_cols varchar(1024), dir int, doing_wt_wf int, primary key(action_id,wb_code));
			String meCode = SeclRemoteConnManager.getMyCode(desktopConn);
			ArrayList<Station> ancList = SeclRemoteConnManager.getStationAndParentInfo(desktopConn, meCode);
			Station me = ancList != null && ancList.size() > 0 ? ancList.get(0) : null;
			
			StationTree meTreeUnfiltered = SeclRemoteConnManager.getRelevantPartOfTree(desktopConn, me, null);
			if (meTreeUnfiltered.wbReachable == null || meTreeUnfiltered.wbReachable.size() == 0)
				return;
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct action_id, tpr_id, challan_no, change_cols, doing_wt_wf from remote_tprchange_action where action=0 and dir=0 and wb_code in ( ");
			for (int i=0,is=meTreeUnfiltered.wbReachable == null ? 0 : meTreeUnfiltered.wbReachable.size(); i<is; i++) {
				if (i != 0)
					sb.append(",");
				sb.append("'").append(meTreeUnfiltered.wbReachable.get(i)).append("'");
			}
			sb.append(") order by created_on, doing_wt_wf ");
			ps = remoteConn.prepareStatement(sb.toString());
			ArrayList<LocalTPRChangeResult> todoList = new ArrayList<LocalTPRChangeResult>();
			rs = ps.executeQuery();
			while (rs.next()) {
				LocalTPRChangeResult entry = new LocalTPRChangeResult();
				entry.actionId = rs.getString(1);
				entry.tprId = rs.getInt(2);
				entry.challanNo = rs.getString(3);
				String changedColsStr = rs.getString(4);
				ArrayList<Pair<String, Integer>> colsChanged = RemoteTPRHelper.getColsChangedAsArray(changedColsStr);
				entry.colsChanged = colsChanged;
				entry.doingWtWf = rs.getInt(5) == 1;
				entry.doCurrDoStatus = true;
				entry.createEntryForTarget = true;
				entry.meTree = meTreeUnfiltered;
				entry.meStation = me;
				todoList.add(entry);
			}
			rs=  Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			//currently todoList is incomplete - now for each entry we will applyLocally
			ArrayList<String> meFullWBReachable = meTreeUnfiltered.wbReachable;
			
			for (int i=0,is=todoList.size(); i<is;i++) {
				try {
					LocalTPRChangeResult entry = todoList.get(i);
					HashMap<String, Value> newTPR = RemoteTPRHelper.readData(remoteConn, entry.tprId, false);
					int locRegTPRId = RemoteTPRHelper.getTPRIdFromChallan(entry.challanNo, desktopConn, true);
					HashMap<String, Value> oldTPR = RemoteTPRHelper.readData(desktopConn,locRegTPRId , false);
					entry.newTPR = newTPR;
					entry.oldTPR = oldTPR;
					ArrayList<String> wbsOfInterest = RemoteTPRHelper.getWBOfInterest(newTPR, oldTPR);
					ArrayList<String> reachableWbsOfInterest = RemoteTPRHelper.filterWBReachable(meFullWBReachable, wbsOfInterest);
					if (reachableWbsOfInterest == null || reachableWbsOfInterest.size() == 0)
						continue;
					meTreeUnfiltered.wbReachable = reachableWbsOfInterest;
					applyChangesDownOnTree(remoteConn,entry.actionId, entry.tprId, desktopConn, locRegTPRId, entry.challanNo
							, meTreeUnfiltered, true, newTPR
							, entry.colsChanged
							, entry.doCurrDoStatus, entry.createEntryForTarget, entry.doingWtWf, true, desktopConn, 0);
					//commit already done inside func for work being done at top
				}
				catch (Exception e2) {
					e2.printStackTrace();
					//eat it
					try {
						if (!desktopConn.getAutoCommit()) {
							desktopConn.rollback();
						}
					}
					catch (Exception e3) {
						break;
					}
					try {
						if (!remoteConn.getAutoCommit()) {
							remoteConn.rollback();
						}
					}
					catch (Exception e3) {
						break;
					}
				}//exception in processing tpr
			}//for each tpr
			meTreeUnfiltered.wbReachable = meFullWBReachable; //just to be safe for reusability

		}
		catch (Exception e) {
			try {
				if (remoteConn != null && !remoteConn.isClosed()) {
					remoteConn.rollback();
				}
			}
			catch (Exception e2) {
				
			}
			try {
				if (desktopConn != null && !desktopConn.isClosed()) {
					desktopConn.rollback();
				}
			}
			catch (Exception e2) {
				
			}
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	public static void fetchFromMeAndAppyUp(Connection desktopConn) {//
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//from to current and leave it at that - the end points should eventually pick and bring it to their level
			//create table remote_tprchange_action(action_id varchar(64), wb_code varchar(24), tpr_id int, challan_no varchar(24), created_on timestamp null default null, action int, change_cols varchar(1024), dir int, doing_wt_wf int, primary key(action_id,wb_code));
			String meCode = SeclRemoteConnManager.getMyCode(desktopConn);
			ArrayList<Station> ancList = SeclRemoteConnManager.getStationAndParentInfo(desktopConn, meCode);
			Station me = ancList != null && ancList.size() > 0 ? ancList.get(0) : null;
			if (ancList != null && ancList.size() > 1) {
				ancList.remove(0);
			}
			if (ancList == null || ancList.size() == 0)
				return;
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct action_id, tpr_id, challan_no, change_cols, doing_wt_wf from remote_tprchange_action where action=0 and dir=1 "); 
			sb.append(" order by created_on, doing_wt_wf ");
			ps = desktopConn.prepareStatement(sb.toString());
			ArrayList<LocalTPRChangeResult> todoList = new ArrayList<LocalTPRChangeResult>();
			rs = ps.executeQuery();
			while (rs.next()) {
				LocalTPRChangeResult entry = new LocalTPRChangeResult();
				entry.actionId = rs.getString(1);
				entry.tprId = rs.getInt(2);
				entry.challanNo = rs.getString(3);
				String changedColsStr = rs.getString(4);
				ArrayList<Pair<String, Integer>> colsChanged = RemoteTPRHelper.getColsChangedAsArray(changedColsStr);
				entry.colsChanged = colsChanged;
				entry.doingWtWf = rs.getInt(5) == 1;
				entry.doCurrDoStatus = false;
				entry.createEntryForTarget = true;
				todoList.add(entry);
			}
			rs=  Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			//currently todoList is incomplete - now for each entry we will applyLocally
			
			for (int i=0,is=todoList.size(); i<is;i++) {
				try {
					LocalTPRChangeResult entry = todoList.get(i);
					HashMap<String, Value> newTPR = RemoteTPRHelper.readData(desktopConn, entry.tprId, false);
					applyChangesUpOnTree(desktopConn, entry.actionId, entry.tprId, entry.challanNo, me, ancList, newTPR, entry.colsChanged, entry.doCurrDoStatus, entry.createEntryForTarget, entry.doingWtWf, true);
				}
				catch (Exception e2) {
					e2.printStackTrace();
					//eat it
					try {
						if (!desktopConn.getAutoCommit()) {
							desktopConn.rollback();
						}
					}
					catch (Exception e3) {
						break;
					}
				}//exception in processing tpr
			}//for each tpr
		}
		catch (Exception e) {
			try {
				if (desktopConn != null && !desktopConn.isClosed()) {
					desktopConn.rollback();
				}
			}
			catch (Exception e2) {
				
			}
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	private static boolean  applyChangesDownOnTree(Connection parentConn,String actionId,  int parentTPRId, Connection rootConn, int rootTPRId, String challanNo
			, StationTree root, boolean applyOnRoot, HashMap<String, Value> newTPR
			, ArrayList<Pair<String, Integer>> colsChanged
			, boolean doCurrDoStatus, boolean createEntryForTarget, boolean doingWtWf, boolean dontDoDesc, Connection recordActionAlsoHere, int level) { //returns true if successfully applied on root if neeed to apply
		boolean retval = false;
		try {
			if (Misc.isUndef(parentTPRId) && parentConn != null) {
				parentTPRId = RemoteTPRHelper.getTPRIdFromChallan(challanNo, parentConn, true);
			}
			if (Misc.isUndef(rootTPRId) && rootConn != null) {
				rootTPRId = RemoteTPRHelper.getTPRIdFromChallan(challanNo, rootConn, true);
			}
			if (applyOnRoot && rootConn != null) {
				HashMap<String, Value> oldTPR = RemoteTPRHelper.readData(rootConn, rootTPRId, false);
				
				Pair<Boolean, ChangeRecord> changeResult = RemoteTPRSync.applyTPRChangeOnCurrNode(rootConn, actionId, newTPR, oldTPR, rootTPRId, colsChanged
						, root, root.me
						, doCurrDoStatus, doingWtWf, true, createEntryForTarget, false);
				if (!rootConn.getAutoCommit())
					rootConn.commit();
				if (parentConn != null) {
					changeResult.second.save(parentConn, actionId, challanNo, root.me.getCode(), 2);
					RemoteTPRSync.updateTPRWorkedOn(parentConn, actionId, 0, null, root.wbReachable, 1);
					if (!parentConn.getAutoCommit())
						parentConn.commit();
				}
				if (recordActionAlsoHere != null && level != 1) {
					changeResult.second.save(recordActionAlsoHere, actionId, challanNo, root.me.getCode(), 2);
					RemoteTPRSync.updateTPRWorkedOn(recordActionAlsoHere, actionId, 0, null, root.wbReachable, 1);
					if (!recordActionAlsoHere.getAutoCommit())
						recordActionAlsoHere.commit();
				}
			}
		}
		catch (Exception e2) {
			e2.printStackTrace();
			//eat it
			return false;
		}
		retval = true;
		if (!dontDoDesc) {
			for (int i=0,is=root.children == null ? 0 : root.children.size(); i<is;i++) {
				Connection remoteConn = null;
				try {
					StationTree child = root.children.get(i);
					Station childStation = child.me;
					remoteConn = childStation.getConnection();
					if (remoteConn == null) {
						continue;
					}
					applyChangesDownOnTree(rootConn, actionId, rootTPRId, remoteConn, Misc.getUndefInt(), challanNo
							, child, true, newTPR
							, colsChanged
							, doCurrDoStatus, createEntryForTarget, doingWtWf,dontDoDesc, recordActionAlsoHere, level+1)
							;
					if (!remoteConn.getAutoCommit()) {
						remoteConn.commit();
					}
					remoteConn.close();
					remoteConn = null;
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it;
				}
				finally {
					try {
						if (remoteConn != null) 
							remoteConn.close();
					}
					catch (Exception e) {
						
					}
				}//end of finally
			}//for each child in DFS manner
		}
		return true;
	}
	
	private static void  applyChangesUpOnTree(Connection meConn,String actionId,  int meTPRId, String challanNo
			, Station me, ArrayList<Station> ancListExclMe, HashMap<String, Value> newTPR
			, ArrayList<Pair<String, Integer>> colsChanged
			, boolean doCurrDoStatus, boolean createEntryForTarget, boolean doingWtWf, boolean dontDoUpperAnc) { //returns true if successfully applied on root if neeed to apply
		//apply on parent - and make entry there to apply upward commit and then update local. repeat
		Connection botConn = meConn;
		int botTPRId = meTPRId;
		boolean toCloseBotConn = false;
		
		ArrayList<Connection> connsToClose = new ArrayList<Connection>();
		for (int i=0,is=ancListExclMe == null ? 0 : dontDoUpperAnc && ancListExclMe.size() > 1 ? 1 : ancListExclMe.size(); i<is; i++) {
			Connection parConn = null;
			try {
				Station par = ancListExclMe.get(i);
				parConn = par.getConnection();
				if (parConn == null)
					break;
				connsToClose.add(parConn);
				int parTPRId = RemoteTPRHelper.getTPRIdFromChallan(challanNo, parConn, true);
				HashMap<String, Value> oldTPR = RemoteTPRHelper.readData(parConn, parTPRId, false);
			
				Pair<Boolean, ChangeRecord> changeResult = RemoteTPRSync.applyTPRChangeOnCurrNode(parConn, actionId, newTPR, oldTPR, parTPRId, colsChanged, null, par, doCurrDoStatus, doingWtWf, true, false, createEntryForTarget);
				if (!parConn.getAutoCommit())
					parConn.commit();
				RemoteTPRSync.updateTPRWorkedOn(botConn, actionId, 1, me.getCode(),null, 1);
				changeResult.second.save(meConn, actionId, challanNo, par.getCode(), 2); //always know at the initiator what happened
				if (!meConn.getAutoCommit())
					meConn.commit();
				if (toCloseBotConn) { //to avoid doube work saving record of changes 
					changeResult.second.save(botConn, actionId, challanNo, par.getCode(), 2);
					if (!botConn.getAutoCommit())
						botConn.commit();
				}
				
				
				botConn = parConn;
				botTPRId = parTPRId;
				toCloseBotConn = true;
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			finally {
				
			}//end finally
		}//for each ancestor
		for (int i=0,is=connsToClose.size();i<is;i++) {
			try {
				connsToClose.get(i).close();
			}
			catch (Exception e) {
				//eat it
			}
		}
	}
	
	
	
	public static Pair<Boolean, ChangeRecord> applyTPRChangeOnCurrNode(Connection conn, String actionId, HashMap<String, Value> newTPR, HashMap<String, Value> oldTPR, int locRegTPRId, ArrayList<Pair<String, Integer>> colsChanged
			, StationTree meTree, Station meStation
			, boolean doCurrDoStatus, boolean doingWtWf, boolean copyOver, boolean createDownEntryForTarget, boolean createUpTarget) throws Exception {
		
		//returns true if there were changes. 1first= if changes, Second = changes on DO, Third Changes in TPR
		ArrayList<Pair<String, Integer>> diffCols = RemoteTPRHelper.identifyFieldChanges(newTPR, oldTPR, colsChanged, doingWtWf);
		String challanNo = RemoteTPRHelper.getTPRString(newTPR,"challan_no");
		ChangeRecord changeRecord = new ChangeRecord();
		if (diffCols.size() == 0) 
			return new Pair<Boolean,ChangeRecord>(false, changeRecord);
		if (doCurrDoStatus)
			applyDOChanges (conn, newTPR, oldTPR, doingWtWf, changeRecord);
		if (copyOver) {
			//copy + if new is isLatest and inPlant, then make others status=2.
			changeRecord.popTripActChange(conn, challanNo, true);
			changeRecord.tprChanges = RemoteTPRHelper.getDiffTPR(newTPR, oldTPR, diffCols);
			locRegTPRId = RemoteTPRHelper.copyDataTo(conn, newTPR, Misc.getUndefInt(), true, diffCols);
			locRegTPRId = RemoteTPRHelper.copyDataTo(conn, newTPR, locRegTPRId, false, diffCols);
			changeRecord.popTripActChange(conn, challanNo, false);
			if (!createDownEntryForTarget && !createUpTarget) {//doing in realtime
				RemoteTPRHelper.doIsLatestRelated(conn, oldTPR, newTPR, locRegTPRId);// .. being done on down part	
			}
//			
		}
		changeRecord.save(conn, actionId, challanNo, meStation.getCode(), 2);
		
		if (createDownEntryForTarget && meTree != null) {
			createEntryForDescTarget(conn, actionId, locRegTPRId, challanNo, meTree,  doingWtWf,  colsChanged);
		}
		if (createUpTarget) {
			createEntryForUpTarget(conn, actionId, locRegTPRId,  challanNo, doingWtWf,  colsChanged);
		}
		return new Pair<Boolean, ChangeRecord>(true, changeRecord);
	}
	
	private static void createEntryForUpTarget(Connection conn, String actionId, int ofTPRId, String challanNo, boolean doingWtWf, ArrayList<Pair<String, Integer>> changedCols) throws Exception {
		String changedColsString = RemoteTPRHelper.getColsChangedAsString(changedCols);
		PreparedStatement ps = conn.prepareStatement("insert into remote_tprchange_action(action_id,wb_code, tpr_id, created_on, action, change_cols, dir, doing_wt_wf, challan_no) values (?,'__srv',?,now(), 0, ?, 1,?,?)");
		ps.setString(1, actionId);
		ps.setInt(2, ofTPRId);
		ps.setString(3, changedColsString);
		ps.setInt(4, doingWtWf ? 1 : 0);
		ps.setString(5, challanNo);
		ps.executeUpdate();
		ps = Misc.closePS(ps);
	}

	private static void createEntryForDescTarget(Connection conn, String actionId, int ofTPRId, String challanNo, StationTree meTree, boolean doingWtWf, ArrayList<Pair<String, Integer>> changedCols) throws Exception {
		String changedColsString = RemoteTPRHelper.getColsChangedAsString(changedCols);
		PreparedStatement ps = conn.prepareStatement("insert into remote_tprchange_action(action_id,wb_code, tpr_id, created_on, action, change_cols, dir, doing_wt_wf, challan_no) values (?,?,?,now(), 0, ?, 0,?,?)");
		for (int i=0,is=meTree.wbReachable == null ? 0 : meTree.wbReachable.size();i<is;i++) {
			ps.setString(1, actionId);
			ps.setString(2,meTree.wbReachable.get(i));
			ps.setInt(3, ofTPRId);
			ps.setString(4, changedColsString);
			ps.setInt(5, doingWtWf ? 1 : 0);
			ps.setString(6, challanNo);
			ps.addBatch();
		}
		ps.executeBatch();
		ps = Misc.closePS(ps);
	}
	
	private static ChangeRecord applyDOChanges (Connection conn, HashMap<String, Value> newTPR, HashMap<String, Value> oldTPR, boolean doingWtWf, ChangeRecord retval) throws Exception { //returns changes in prog for DO
		if (retval == null) {
			retval = new ChangeRecord();
		}
		double newNet = RemoteTPRHelper.getLoadNet(newTPR);
		double oldNet = RemoteTPRHelper.getLoadNet(oldTPR);
		if (!doingWtWf)
			newNet = oldNet;
		if (RemoteTPRHelper.getTPRInt(newTPR, "status") == 0)
			newNet = 0;
		if (RemoteTPRHelper.getTPRInt(oldTPR, "status") == 0)
			oldNet = 0;
		if (oldNet <= 0)
			oldNet = 0;
		if (newNet <= 0)
			newNet = 0;
		boolean isDiff = !Misc.isEqual(oldNet, newNet);
		Value newDO = newTPR.get("do_number");
		Value oldDO = oldTPR.get("do_number");
		String oldDOStr = oldDO == null ? null : oldDO.getStringVal();
		String newDOStr = newDO == null ? null : newDO.getStringVal();

		if ((newDOStr != null && !newDOStr.equals(oldDOStr)) || (oldDOStr != null && !oldDOStr.equals(newDOStr)))
			isDiff = true;
		if (newDOStr == null && oldDOStr == null)
			isDiff = false;
		if (isDiff) {
			Value newWB = newTPR.get("load_wb_out_name");
			Value oldWB = oldTPR.get("load_wb_out_name");
			String newWBStr = newWB == null ? null : newWB.getStringVal();
			String oldWBStr = oldWB == null ? null : oldWB.getStringVal();
			retval.popDOActChange(conn, oldDOStr, newDOStr, oldWBStr, newWBStr, true);
			for (int art=0;art<2;art++) {
				String tab = art == 0 ? "current_do_status" : "current_do_status_apprvd";
				//1 insert in target and then update
				PreparedStatement ps = conn.prepareStatement("insert into "+tab+" (do_number, wb_code, current_allocation, lifted_qty) (select ?,?,0,0 from dual where not exists (select 1 from "+tab+"  where do_number=? and wb_code=?))");
				for (int art2=0;art2<2;art2++) {
					String wb = art2 == 0 ? oldWBStr : newWBStr;
					String doN = art2 == 0 ? oldDOStr : newDOStr;
					if (wb == null || doN == null)
						continue;
					ps.setString(1, doN);
					ps.setString(2, wb);
					ps.setString(3, doN);
					ps.setString(4, wb);
					ps.addBatch();
				}
				ps.executeBatch();
				ps = Misc.closePS(ps);
				if (oldDOStr != null && oldWBStr != null) {
					ps = conn.prepareStatement("update "+tab+" set lifted_qty=lifted_qty-? where do_number=? and wb_code=?");
					ps.setDouble(1, oldNet);
					ps.setString(2, oldDOStr);
					ps.setString(3, oldWBStr);
					ps.executeUpdate();
					ps = Misc.closePS(ps);
					if (art == 0)
						retval.doChanges = "Minus on DO:"+oldDOStr+" WB:"+oldWBStr+" by "+oldNet;
				}
				
				if (newDOStr != null && newWBStr != null) {
					ps = conn.prepareStatement("update "+tab+" set lifted_qty=lifted_qty+? where do_number=? and wb_code=?");
					ps.setDouble(1, newNet);
					ps.setString(2, newDOStr);
					ps.setString(3, newWBStr);
					ps.executeUpdate();
					ps = Misc.closePS(ps);
					if (art == 0) {
						String tretval = "Plus on DO:"+newDOStr+" WB:"+newWBStr+" by "+newNet;
						if (retval.doChanges != null)
							retval.doChanges += "; "+tretval;
						else
							retval.doChanges = tretval;
					}
				}
			}//for reg/apprvd
			retval.popDOActChange(conn, oldDOStr, newDOStr, oldWBStr, newWBStr, false);
		}//if diff
		return retval;
	}
	
	public static String getCodeFromRecordSrc(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select code from secl_workstation_details where id = ?");
		ps.setInt(1, Misc.getRecordSrcId(conn));
		ResultSet rs = ps.executeQuery();
		String retval = "__srv";
		if (rs.next()) {
			retval = rs.getString(1);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return retval;
	}
	
	public static String getIdForAction(Connection conn, int workflowId, int tprId) throws Exception {
		String code = getCodeFromRecordSrc(conn);
		return code+workflowId+"_"+tprId;
	}

	private static void updateTPRWorkedOn(Connection connToWork, String actionId,  int dir, String code,ArrayList<String> allCodes,  int status) throws Exception {
		//need to get for all WB till and Inclu myPos
		
		PreparedStatement ps = connToWork.prepareStatement(dir == 0 ? "update remote_tprchange_action set action=? where action_id=? and wb_code=? and dir=0" :  "update remote_tprchange_action set action=? where action_id=? and dir=1");
		if (allCodes == null || allCodes.size() == 0 || dir != 0) {
			ps.setInt(1, status);
			ps.setString(2, actionId);
			if (dir == 0)
				ps.setString(3, code);
			ps.executeUpdate();	
		}
		else {
			for (int i=0,is=allCodes.size(); i<is;i++) {
				ps.setInt(1, status);
				ps.setString(2, actionId);
				ps.setString(3, allCodes.get(i));
				ps.addBatch();	
			}
			ps.executeBatch();
		}
		
		ps = Misc.closePS(ps);
	}

	
	public static String mergeDisplayableAction(ArrayList<Triple<Integer, ArrayList<String>, String>> results) throws Exception {//first = tpr, 2nd WB of interest, 3rd action
		StringBuilder sb  =  new StringBuilder();
		sb.append("<table cellpadding='1' border='0'>");
		for (int i=0,is=results.size(); i<is; i++) {
			sb.append("<tr><td class='sh'>TPR Id:").append(results.get(i).first).append(" WB:").append(results.get(i).second).append("</td></tr>");
			sb.append("<tr><td class='tn'>").append(results.get(i).third).append("</td></tr>");
		}
		return sb.toString();
	}
	
	
	public static String getDisplayableActionTaken(Connection conn, String actionId)  {
		StringBuilder sb = new StringBuilder();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select table_type, action_taken_on_code, val_changes from remote_tprchange_action_detail where action_id = ? order by action_taken_on_code");
			ps.setString(1, actionId);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (sb.length() > 0) {
					sb.append("<br/>");
				}
				String actionCode = rs.getString(2);
				String action  = rs.getString(3);
				int tableType = rs.getInt(1);
				sb.append(actionCode).append(" - ").append(tableType == 0 ? "TPRec":"Prog.").append(":").append(action);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return sb.toString();
	}
	
	public static class LocalTPRChangeResult {
		public HashMap<String, Value> newTPR;
		public HashMap<String, Value> oldTPR;
		public Station meStation;
		public ArrayList<Station> filteredAncestorListExclMe;
		public StationTree meTree;
		public boolean doCurrDoStatus;
		public boolean createEntryForTarget;
		public boolean didChange;
		public String actionId;
		public boolean doingWtWf;
		public ArrayList<Pair<String, Integer>> colsChanged;
		public int tprId;
		public String challanNo;
		public LocalTPRChangeResult() {
			
		}
		public LocalTPRChangeResult(HashMap<String, Value> newTPR,
				HashMap<String, Value> oldTPR, Station meStation,
				ArrayList<Station> filteredAncestorListExclMe,
				StationTree meTree,
				boolean doCurrDoStatus, boolean createEntryForTarget,
				boolean didChange, String actionId, boolean doingWtWf, ArrayList<Pair<String, Integer>> colsChanged, int tprId, String challanNo) {
			super();
			this.newTPR = newTPR;
			this.oldTPR = oldTPR;
			this.meStation = meStation;
			this.filteredAncestorListExclMe = filteredAncestorListExclMe;
			this.meTree = meTree;
			this.doCurrDoStatus = doCurrDoStatus;
			this.createEntryForTarget = createEntryForTarget;
			this.didChange = didChange;
			this.actionId = actionId;
			this.colsChanged = colsChanged;
			this.doingWtWf = doingWtWf;
			this.tprId = tprId;
			this.challanNo = challanNo;
		}
	}
	
	public static class ChangeRecord {
		public String tprChanges;
		public String doChanges;
		public double beforeNet;
		public double afterNet;
		public String beforeDONumber;
		public String beforeWBCode;
		public double beforeLifted;
		public double beforeDOAftLifted;
		public String afterDONumber;
		public String afterWBCode;
		public double afterDOAftLifted;
		public double afterLifted;
		public void save(Connection conn, String actionId, String challanNo, String wbCode, int saveOf) throws Exception {
			//saveOf = 0 => only TPR, 1 => only DO, 2 => all
			PreparedStatement ps = conn.prepareStatement("insert into remote_tprchange_action_detail(action_id, table_type, action_taken_on_code, val_changes, on_date, challan_no "+
					", before_net, after_net"+
					", before_do_number, before_wb_code,  before_lifted, before_do_aft_lifted"+
					", after_do_number, after_wb_code, after_lifted, after_do_aft_lifted) "+
					" values (?,?,?,?,now(),?"+
					",?,?"+
					",?,?,?,?"+
					",?,?,?,?"+
					")");
			
			if (this.tprChanges != null && this.tprChanges.length() > 0 && (saveOf == 0 || saveOf == 2)) {
				int tableType = 0;
				ps.setString(1, actionId);
				ps.setInt(2, tableType);
				ps.setString(3, wbCode);
				ps.setString(4, this.tprChanges);
				ps.setString(5, challanNo);
				Misc.setParamDouble(ps, this.beforeNet, 6);
				Misc.setParamDouble(ps, this.afterNet, 7);
				ps.setString(8, null);
				ps.setString(9, null);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), 10);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), 11);
				ps.setString(12, null);
				ps.setString(13, null);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), 14);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), 15);

				ps.executeUpdate();
			}
			if (this.doChanges != null && this.doChanges.length() > 0 && (saveOf == 0 || saveOf == 2)) {
				int tableType = 1;
				ps.setString(1, actionId);
				ps.setInt(2, tableType);
				ps.setString(3, wbCode);
				ps.setString(4, this.doChanges);
				ps.setString(5, challanNo);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), 6);
				Misc.setParamDouble(ps, Misc.getUndefDouble(), 7);
				ps.setString(8, this.beforeDONumber);
				ps.setString(9, this.beforeWBCode);
				Misc.setParamDouble(ps, this.beforeLifted, 10);
				Misc.setParamDouble(ps, this.beforeDOAftLifted, 11);
				ps.setString(12, this.afterDONumber);
				ps.setString(13, this.afterWBCode);
				Misc.setParamDouble(ps, this.afterLifted, 14);
				Misc.setParamDouble(ps, this.afterDOAftLifted, 15);
				ps.executeUpdate();
			}
			ps = Misc.closePS(ps);
		}
		public void popTripActChange(Connection conn,String challanNo, boolean doingBef) throws Exception {
			PreparedStatement ps = conn.prepareStatement("select load_gross - load_tare from tp_record where challan_no=? and status in (1,2) order by tpr_id desc ");
			ps.setString(1, challanNo);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				double net = rs.getDouble(1);
				if (doingBef) {
					this.beforeNet = net;
				}
				else
					this.afterNet = net;
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		
		public void popDOActChange(Connection conn, String befDO, String aftDO, String befWB, String aftWB, boolean doingBef) throws Exception {
			this.beforeDONumber = befDO;
			this.beforeWBCode = befWB;
			this.afterDONumber = aftDO;
			this.afterDONumber = aftWB;
			
			PreparedStatement ps = conn.prepareStatement("select do_number, wb_code, lifted_qty from current_do_status_apprvd where do_number in (?,?) and wb_code in (?,?)");
			ps.setString(1, befDO);
			ps.setString(2, aftDO);
			ps.setString(3, befWB);
			ps.setString(4, aftWB);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				String doN = rs.getString(1);
				String wbC = rs.getString(2);
				double lifted = rs.getDouble(3);
				if (doN == null || wbC == null)
					continue;
				if (doN.equals(befDO) && wbC.equals(befWB)) {
					if (doingBef)
						this.beforeLifted = lifted;
					else
						this.beforeDOAftLifted = lifted;
				}
				if (doN.equals(aftDO) && wbC.equals(aftWB)) {
					if (doingBef)
						this.afterLifted = lifted;
					else
						this.afterDOAftLifted = lifted;
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		ArrayList<Station> allstn = SeclRemoteConnManager.getStationAndParentInfo(conn,"R_WB1");
		Station wb1 = allstn.get(0);
		RemoteTPRSync.fetchFromTopAndApply(wb1.getConnection(), conn);
		RemoteTPRSync.fetchFromMeAndAppyUp(conn);
		
		int dbg = 1;
		
		
	}	
}

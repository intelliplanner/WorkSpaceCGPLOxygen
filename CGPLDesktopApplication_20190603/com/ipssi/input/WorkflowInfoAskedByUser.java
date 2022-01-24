package com.ipssi.input;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WorkflowInfoAskedByUser {
	public WorkflowInfoAskedByUser(int workflowId, boolean doingApproval, String comments) {
		this.workflowId = workflowId;
		this.doApprove = doingApproval;
		this.comments = comments;
	}
	public WorkflowInfoAskedByUser(int workflowId, boolean doingApproval, String comments, boolean doForce) {
		this.workflowId = workflowId;
		this.doApprove = doingApproval;
		this.comments = comments;
		this.setForce(doForce);
	}
	public static void updateWorkflowInfoAskedFromWorkflowId(Connection conn, ArrayList<WorkflowInfoAskedByUser> itemList) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select workflow_type_id,status,pending_approval_of,object_id from workflows where id=?");
		for (int i=0,is=itemList == null ? 0: itemList.size(); i<is; i++) {
			WorkflowInfoAskedByUser item = itemList.get(i);
			ps.setInt(1, item.getWorkflowId());
			ResultSet rs = ps.executeQuery();
			int wkfType = Misc.getUndefInt();
			int apprvUserConf = Misc.getUndefInt();
			int stillPending = Misc.getUndefInt();
			int objectId = Misc.getUndefInt();
			if (rs.next()) {
				wkfType = Misc.getRsetInt(rs, 1);
				stillPending = Misc.getRsetInt(rs, 2);
				apprvUserConf = Misc.getRsetInt(rs,3);
				objectId = Misc.getRsetInt(rs, 4);
			}
			rs = Misc.closeRS(rs);
			item.setWkfType(wkfType);
			item.setStillPending(stillPending);
			item.setApprvUserConf(apprvUserConf);
			item.setObjectId(objectId);
		}
		ps = Misc.closePS(ps);
	}
	
	int workflowId = Misc.getUndefInt();
	boolean doApprove = false;;
	String comments = null;
	int wkfType = Misc.getUndefInt();
	int apprvUserConf = Misc.getUndefInt(); //whose approal required
	int stillPending = Misc.getUndefInt(); //is it still pending
	int objectId = Misc.getUndefInt();
	boolean force = false;
	boolean forFinalAtDOApproval = false;
	public int getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}
	public boolean isDoApprove() {
		return doApprove;
	}
	public void setDoApprove(boolean doApprove) {
		this.doApprove = doApprove;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public int getWkfType() {
		return wkfType;
	}
	public void setWkfType(int wkfType) {
		this.wkfType = wkfType;
	}
	public int getApprvUserConf() {
		return apprvUserConf;
	}
	public void setApprvUserConf(int apprvUserConf) {
		this.apprvUserConf = apprvUserConf;
	}
	public int getStillPending() {
		return stillPending;
	}
	public void setStillPending(int stillPending) {
		this.stillPending = stillPending;
	}
	public int getObjectId() {
		return objectId;
	}
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}
	public boolean isForce() {
		return force;
	}
	public void setForce(boolean force) {
		this.force = force;
	}
	public boolean isForFinalAtDOApproval() {
		return forFinalAtDOApproval;
	}
	public void setForFinalAtDOApproval(boolean forFinalAtDOApproval) {
		this.forFinalAtDOApproval = forFinalAtDOApproval;
	}
	
}

package com.ipssi.common.ds.trip;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import com.ipssi.gen.utils.Misc;
import com.ipssi.miningOpt.*;
import com.ipssi.miningOpt.ShovelInfo;
public class HelperAssignment {
	private NewMU newmu = null;
	private DumperInfo dumperInfo = null;
	private ArrayList<Integer> shovelInfo = null; //1st temp marker to keep track of previous list is 
	private int loadSite = Misc.getUndefInt();
	private int unloadSite = Misc.getUndefInt();
	private long lastCalculatedFor = 0;
	private long assignmentValidTill = 0;
	public HelperAssignment(NewMU newmu, DumperInfo dumperInfo) {
		this.newmu = newmu;
		this.dumperInfo = dumperInfo;
	}
	public void clear() {
		this.loadSite = Misc.getUndefInt();
		this.unloadSite = Misc.getUndefInt();
		this.shovelInfo = null;
		lastCalculatedFor = -1;
		assignmentValidTill = -1;
	}
	public void update(Connection conn, long ts) throws Exception {
		if (ts >= lastCalculatedFor && ts < assignmentValidTill)
			return;
		this.clear();
		if (dumperInfo == null) {
			return;
		}
		Assignment dummy = new Assignment(ts);
		long minValidTill = Long.MAX_VALUE;
		NewAssignmentMgmt tempMgmt = null;
		tempMgmt = dumperInfo.getAssignmentList(conn);
		Assignment dumperAssignment = null;
		Assignment nextAssignment = null;
		if (tempMgmt != null) {
			synchronized (tempMgmt) {
				dumperAssignment = tempMgmt.get(conn, dummy);
				nextAssignment = tempMgmt.get(conn, dummy, 1);
			}
		}
		if (dumperAssignment != null) {
			Site site =  newmu.getSiteInfo(dumperAssignment.getSiteId());
			if (site != null && site instanceof LoadSite)
				this.loadSite = site.getId();
			site =  newmu.getSiteInfo(dumperAssignment.getDestId());
			if (site != null && site instanceof UnloadSite)
				this.unloadSite = site.getId();
			long validTill = nextAssignment == null ? Long.MAX_VALUE : nextAssignment.getAtTime();
			if (minValidTill > validTill)
				minValidTill = validTill;
		}
		Collection<ShovelInfo> shovels = newmu.getAllShovels();
		for (java.util.Iterator<ShovelInfo> iter = shovels.iterator(); iter.hasNext(); ) {
			ShovelInfo shovel = iter.next();
			tempMgmt = shovel.getAssignmentList(conn);
			Assignment temp = null;
			Assignment next = null;
			if (tempMgmt != null) {
				synchronized (tempMgmt) {
					temp = tempMgmt.get(conn,dummy);
					next = tempMgmt.get(conn, dummy, 1);
				}
			}
			
			if (temp != null && temp.getSiteId() == this.loadSite && !Misc.isUndef(temp.getSiteId())) {
				if (shovelInfo == null)
					shovelInfo = new ArrayList<Integer>();
				shovelInfo.add(shovel.getId());
				long validTill = next == null ? Long.MAX_VALUE : next.getAtTime();
				if (minValidTill > validTill)
					minValidTill = validTill;
			}
		}
		if (minValidTill < ts)
			minValidTill = ts+1000;
		this.lastCalculatedFor = ts;
		this.assignmentValidTill = minValidTill;
		
	}
	public NewMU getNewmu() {
		return newmu;
	}
	public void setNewmu(NewMU newmu) {
		this.newmu = newmu;
	}
	public DumperInfo getDumperInfo() {
		return dumperInfo;
	}
	public void setDumperInfo(DumperInfo dumperInfo) {
		this.dumperInfo = dumperInfo;
	}
	public ArrayList<Integer> getShovelInfo() {
		return shovelInfo;
	}
	public void setShovelInfo(ArrayList<Integer> shovelInfo) {
		this.shovelInfo = shovelInfo;
	}
	public int getLoadSite() {
		return loadSite;
	}
	public void setLoadSite(int loadSite) {
		this.loadSite = loadSite;
	}
	public int getUnloadSite() {
		return unloadSite;
	}
	public void setUnloadSite(int unloadSite) {
		this.unloadSite = unloadSite;
	}
	public long getLastCalculatedFor() {
		return lastCalculatedFor;
	}
	public void setLastCalculatedFor(long lastCalculatedFor) {
		this.lastCalculatedFor = lastCalculatedFor;
	}
	public long getAssignmentValidTill() {
		return assignmentValidTill;
	}
	public void setAssignmentValidTill(long assignmentValidTill) {
		this.assignmentValidTill = assignmentValidTill;
	}
}

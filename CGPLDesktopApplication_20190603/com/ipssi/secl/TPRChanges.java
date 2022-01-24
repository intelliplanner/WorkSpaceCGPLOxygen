package com.ipssi.secl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.ColumnMappingHelper;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SeclRemoteConnManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.rfid.beans.DOUpdInfo;
import com.ipssi.workflow.WorkflowHelper;
import com.ipssi.workflow.WorkflowHelper.TableObjectInfo;

public class TPRChanges {
	private static String CRIT_TPR_CHANGE = "status, load_tare, load_gross, unload_tare, unload_gross, do_number, load_wb_in_name, load_wb_out_name, unload_wb_in_name, unload_wb_out_name, challan_no , tpr_status, is_latest ";
	private static String GET_CRIT_TPR_CHANGE = "select "+CRIT_TPR_CHANGE+" from tp_record where tpr_id = ? ";
	private static String GET_CRIT_TPR_APPRVD_CHANGE = "select "+CRIT_TPR_CHANGE+" from tp_record_apprvd where tpr_id = ? ";
	private int tprId = Misc.getUndefInt();
	public static class SpecificTPChange {
		private int status = Misc.getUndefInt();
		private double loadTare = Misc.getUndefDouble();
		private double loadGross = Misc.getUndefDouble();
		private double unloadTare = Misc.getUndefDouble();
		private double unloadGross = Misc.getUndefDouble();
		private String doNumber = null;
		private String loadTareWb = null;
		private String loadGrossWb = null;
		private String unloadTareWb = null;
		private String unloadGrossWb = null;
		private String challanNo = null;
		private int tprStatus = Misc.getUndefInt();
		private int isLatest = Misc.getUndefInt();
		public double getNet() {
			return ((status ==1 || status ==2) && !Misc.isUndef(loadTare)  && !Misc.isUndef(loadGross)) ? loadGross - loadTare : 0;
		}
		
		public static SpecificTPChange getFromTPRecord(ResultSet rs) throws Exception {
				return new SpecificTPChange(Misc.getRsetInt(rs, 1), Misc.getRsetDouble(rs, 2), Misc.getRsetDouble(rs, 3), Misc.getRsetDouble(rs, 4), Misc.getRsetDouble(rs, 5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10), rs.getString(11), Misc.getRsetInt(rs, 12), Misc.getRsetInt(rs, 13));
		}
		public boolean isImpactingProg(SpecificTPChange rhs) {
			double meNet = getNet();
			double rhsNet = rhs.getNet();
			return !(Misc.isEqual(meNet, rhsNet)
					&& ((doNumber == null && rhs.doNumber == null) || (doNumber != null && rhs.doNumber != null && doNumber.equals(rhs.doNumber)))
					&& ((loadGrossWb == null && rhs.loadGrossWb == null) || (loadGrossWb != null && rhs.loadGrossWb != null && loadGrossWb.equals(rhs.loadGrossWb)))
					) && (doNumber != null || rhs.doNumber != null);
		}
		public boolean equals(SpecificTPChange rhs) { //prob not used
			boolean retval = this.status == rhs.status
			&& ((Misc.isEqual(this.loadTare, rhs.loadTare)) || (Misc.isUndef(this.loadTare) && Misc.isUndef(rhs.loadTare)))
			&& ((Misc.isEqual(this.loadGross, rhs.loadGross)) || (Misc.isUndef(this.loadGross) && Misc.isUndef(rhs.loadGross)))
			&& ((Misc.isEqual(this.unloadTare, rhs.unloadTare)) || (Misc.isUndef(this.unloadTare) && Misc.isUndef(rhs.unloadTare)))
			&& ((Misc.isEqual(this.unloadGross, rhs.unloadGross)) || (Misc.isUndef(this.unloadGross) && Misc.isUndef(rhs.unloadGross)))
			&& ((doNumber == null && rhs.doNumber == null) || (doNumber != null && rhs.doNumber != null && doNumber.equals(rhs.doNumber)))
			&& ((loadTareWb == null && rhs.loadTareWb == null) || (loadTareWb != null && rhs.loadTareWb != null && loadTareWb.equals(rhs.loadTareWb)))
			&& ((loadGrossWb == null && rhs.loadGrossWb == null) || (loadGrossWb != null && rhs.loadGrossWb != null && loadGrossWb.equals(rhs.loadGrossWb)))			
			&& ((unloadTareWb == null && rhs.unloadTareWb == null) || (unloadTareWb != null && rhs.unloadTareWb != null && unloadTareWb.equals(rhs.unloadTareWb)))
			&& ((unloadGrossWb == null && rhs.unloadGrossWb == null) || (unloadGrossWb != null && rhs.unloadGrossWb != null && unloadGrossWb.equals(rhs.unloadGrossWb)))
			&& tprStatus == rhs.tprStatus
			&& isLatest == rhs.isLatest
			;
			return retval;
		}
		public SpecificTPChange() {
			
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public double getLoadTare() {
			return loadTare;
		}
		public void setLoadTare(double loadTare) {
			this.loadTare = loadTare;
		}
		public double getLoadGross() {
			return loadGross;
		}
		public void setLoadGross(double loadGross) {
			this.loadGross = loadGross;
		}
		public double getUnloadTare() {
			return unloadTare;
		}
		public void setUnloadTare(double unloadTare) {
			this.unloadTare = unloadTare;
		}
		public double getUnloadGross() {
			return unloadGross;
		}
		public void setUnloadGross(double unloadGross) {
			this.unloadGross = unloadGross;
		}
		public String getDoNumber() {
			return doNumber;
		}
		public void setDoNumber(String doNumber) {
			this.doNumber = doNumber;
		}
		public String getLoadTareWb() {
			return loadTareWb;
		}
		public void setLoadTareWb(String loadTareWb) {
			this.loadTareWb = loadTareWb;
		}
		public String getLoadGrossWb() {
			return loadGrossWb;
		}
		public void setLoadGrossWb(String loadGrossWb) {
			this.loadGrossWb = loadGrossWb;
		}
		public String getUnloadTareWb() {
			return unloadTareWb;
		}
		public void setUnloadTareWb(String unloadTareWb) {
			this.unloadTareWb = unloadTareWb;
		}
		public String getUnloadGrossWb() {
			return unloadGrossWb;
		}
		public void setUnloadGrossWb(String unloadGrossWb) {
			this.unloadGrossWb = unloadGrossWb;
		}
		public SpecificTPChange(int status, double loadTare, double loadGross,
				double unloadTare, double unloadGross, String doNumber,
				String loadTareWb, String loadGrossWb, String unloadTareWb,
				String unloadGrossWb, String challanNo, int tprStatus, int isLatest) {
			super();
			this.status = status;
			this.loadTare = loadTare;
			this.loadGross = loadGross;
			this.unloadTare = unloadTare;
			this.unloadGross = unloadGross;
			this.doNumber = doNumber;
			this.loadTareWb = loadTareWb;
			this.loadGrossWb = loadGrossWb;
			this.unloadTareWb = unloadTareWb;
			this.unloadGrossWb = unloadGrossWb;
			this.challanNo = challanNo;
			this.tprStatus = tprStatus;
			this.isLatest = isLatest;
		}
		public String getChallanNo() {
			return challanNo;
		}
		public void setChallanNo(String challanNo) {
			this.challanNo = challanNo;
		}
		public int getTprStatus() {
			return tprStatus;
		}
		public void setTprStatus(int tprStatus) {
			this.tprStatus = tprStatus;
		}
		public int getIsLatest() {
			return isLatest;
		}
		public void setIsLatest(int isLatest) {
			this.isLatest = isLatest;
		}
	}
	
	ArrayList<String> tpQueriesOnRemote = null;
	ArrayList<String> doQueriesOnRemote = null;
	ArrayList<String> wbToSend = null;
	public TPRChanges(int tprId, ArrayList<String> tpQueriesOnRemote, ArrayList<String> doQueriesOnRemote, ArrayList<String> wbToSend) {
		this.tpQueriesOnRemote = tpQueriesOnRemote;
		this.doQueriesOnRemote = doQueriesOnRemote;
		this.wbToSend = wbToSend;
		this.tprId = tprId;
	}
	private static ArrayList<String> g_colsToIgnore = new ArrayList<String>();
	static {
		g_colsToIgnore.add("tpr_id");
		g_colsToIgnore.add("vehicle_id");
		g_colsToIgnore.add("transporter_id");
		g_colsToIgnore.add("do_id");
		g_colsToIgnore.add("material_grade_id");
		g_colsToIgnore.add("plant_id");
		g_colsToIgnore.add("washery_id");
		g_colsToIgnore.add("rfid_info_id");
		g_colsToIgnore.add("product_id");
		g_colsToIgnore.add("remote_tpr_id");
	}
	public static Triple<ArrayList<String>, String, String> getColsOfTPR(Connection conn) throws Exception {//first = arraylist of col names of tprecord, 2nd select cols of tp_record, 3 select from apprvd
		ArrayList<String> cols = new ArrayList<String>();
		StringBuilder sbReg = new StringBuilder();
		StringBuilder sbApp = new StringBuilder();
		sbReg.append("select ");
		sbApp.append("select ");
		DatabaseMetaData dbmt = conn.getMetaData();
		HashMap<String, String> apprvdTPCol = DOUpdInfo.getColsInDB(conn, "tp_record_apprvd");
	
		ResultSet rs = dbmt.getColumns(null, null, "tp_record", null);
		boolean isFirst = true;
		while (rs.next()) {
			String col = rs.getString("COLUMN_NAME");
			if (g_colsToIgnore.contains(col) || !apprvdTPCol.containsKey(col))
				continue;
			if (!isFirst) {
				sbReg.append(",");
				sbApp.append(",");
			}
			else {
				isFirst=false;
			}
			sbReg.append(col);
			sbApp.append(col);
			cols.add(col);
		}
		rs = Misc.closeRS(rs);
		sbReg.append(" from tp_record where tpr_id = ? ");
		sbApp.append(" from tp_record_apprvd where tpr_id = ? ");
		return new Triple<ArrayList<String>, String, String>(cols, sbReg.toString(), sbApp.toString());
	}
	public static TPRChanges getTPRChanges(Connection conn, int tprId, Triple<ArrayList<String>, String, String> colsAndSel, boolean wtChangePossible) throws Exception {
		PreparedStatement ps = null;
		PreparedStatement psApp  =null;
		ResultSet rsReg = null;
		ResultSet rsApp = null;
		ArrayList<String> retval = new ArrayList<String>();
		ArrayList<String> doChanges = new ArrayList<String>();
		try {
			ps = conn.prepareStatement(GET_CRIT_TPR_CHANGE);
			ps.setInt(1, tprId);
			rsReg = ps.executeQuery();
			SpecificTPChange newVal = null;
			SpecificTPChange oldVal = null;
			if (rsReg.next()) {
				newVal = SpecificTPChange.getFromTPRecord(rsReg);
			}
			rsReg = Misc.closeRS(rsReg);
			ps = Misc.closePS(ps);
			
			ps = conn.prepareStatement(GET_CRIT_TPR_APPRVD_CHANGE);
			ps.setInt(1, tprId);
			rsApp = ps.executeQuery();
			if (rsApp.next()) {
				oldVal = SpecificTPChange.getFromTPRecord(rsApp);
			}
			else
				oldVal = new SpecificTPChange();
			rsApp = Misc.closeRS(rsApp);
			ps = Misc.closePS(ps);
			if (!wtChangePossible) {
				newVal.loadGross = oldVal.loadGross;
				newVal.loadTare = oldVal.loadTare;
			}
			if (colsAndSel == null)
				colsAndSel = TPRChanges.getColsOfTPR(conn);
			ArrayList<String> cols = colsAndSel.first;
			ps = conn.prepareStatement(colsAndSel.second);
			ps.setInt(1, tprId);
			rsReg = ps.executeQuery();
			psApp = conn.prepareStatement(colsAndSel.third);
			psApp.setInt(1, tprId);
			rsApp = psApp.executeQuery();
			StringBuilder sb = new StringBuilder();
			sb.append("insert into tp_record(challan_no) (select '").append(newVal.getChallanNo()).append("' from dual where not exists (select 1 from tp_record where challan_no='").append(newVal.getChallanNo()).append("' ))");
			retval.add(sb.toString());
			sb.setLength(0);
			sb.append("insert into tp_record_apprvd(tpr_id, challan_no) (select tp_record.tpr_id, tp_record.challan_no from tp_record where tp_record.challan_no= '").append(newVal.getChallanNo()).append("' and not exists (select 1 from tp_record_apprvd where tp_record_apprvd.challan_no = '").append(newVal.getChallanNo()).append("' ))");
			retval.add(sb.toString());
			sb.setLength(0);
//now create the update statement for tp_record
			boolean isFirst = true;
			if (!rsApp.next())
				rsApp = null;
			if (!rsReg.next())
				rsReg = null;
			boolean foundDiff = false;
			for (int i=0,is=cols.size(); i<is; i++) {
				Object vreg = rsReg == null ? null : rsReg.getObject(i+1);
				Object vapp = rsApp == null ? null : rsApp.getObject(i+1);
				if ((vreg == null && vapp == null) || (vreg != null && vreg.equals(vapp)))
					continue;
				foundDiff = true;
				if (!isFirst)
					sb.append(",");
				sb.append(cols.get(i)).append("=");
				if (vreg == null)
					sb.append("null");
				else 
					sb.append("'").append(vreg.toString()).append("'");
				isFirst = false;
			}
			retval.add("update tp_record set "+sb+" where challan_no='"+newVal.getChallanNo()+"' ");
			retval.add("update tp_record_apprvd set "+sb+" where challan_no='"+newVal.getChallanNo()+"' ");
			if (!foundDiff)
				retval.clear();
			//now let us look at changes in isLatest and make corresponding changes
			sb.setLength(0);
			if (newVal.isLatest != oldVal.isLatest) {
				if (newVal.isLatest == 1 && newVal.tprStatus == 1) {
					sb.append("update tp_record t0 cross join (select vehicle_id vid, tpr_id tpid from tp_record where challan_no='").append(newVal.getChallanNo()).append("' ) tpl set is_latest=0 where vehicle_id = vid and is_latest=1 and tpr_status=1 and tpr_id <> tpid ");
					retval.add(sb.toString());
					sb.setLength(0);
					sb.append("update tp_record_apprvd t0 cross join (select vehicle_id vid, tpr_id tpid from tp_record where challan_no='").append(newVal.getChallanNo()).append("' ) tpl set is_latest=0 where vehicle_id = vid and is_latest=1 and tpr_status=1 and tpr_id <> tpid ");
					retval.add(sb.toString());
					sb.setLength(0);
				}
			}
			//now let us look at the changes in current_do_status
			sb.setLength(0);
			if (newVal.isImpactingProg(oldVal)) {
				//1. get oldNet, 2. get newNet
				double oldNet = oldVal.getNet();
				double newNet = newVal.getNet();
				//reduce in old by this much
				//increase by newNet ... but be careful the current_do_status entry may not exist
				if (!Misc.isEqual(oldNet, 0) && oldNet > 0) {
					sb.append("update current_do_status set lifted_qty=lifted_qty-").append(oldNet).append(" where do_number='").append(oldVal.getDoNumber()).append("' and wb_code='").append(oldVal.loadGrossWb).append("' ");
					doChanges.add(sb.toString());
					sb.setLength(0);
					sb.append("update current_do_status_apprvd set lifted_qty=lifted_qty-").append(oldNet).append(" where do_number='").append(oldVal.getDoNumber()).append("' and wb_code='").append(oldVal.loadGrossWb).append("' ");
					doChanges.add(sb.toString());
					sb.setLength(0);
				}
				if (!Misc.isEqual(newNet, 0) && newNet > 0) {
					if (newVal.getDoNumber() != null && newVal.getLoadGrossWb() != null) {
						sb.append("insert into current_do_status(do_number, wb_code, current_allocation, lifted_qty) (select '").append(newVal.getDoNumber()).append("', '").append(newVal.loadGrossWb).append("' , 0,0 from dual where not exists (select 1 from current_do_status where do_number='").append(newVal.getDoNumber()).append("' and wb_code=  '").append(newVal.loadGrossWb).append("'))");
						doChanges.add(sb.toString());
						sb.setLength(0);
						sb.append("insert into current_do_status_apprvd(do_number, wb_code, current_allocation, lifted_qty) (select '").append(newVal.getDoNumber()).append("', '").append(newVal.loadGrossWb).append("' , 0,0 from dual where not exists (select 1 from current_do_status_apprvd where do_number='").append(newVal.getDoNumber()).append("' and wb_code=  '").append(newVal.loadGrossWb).append("'))");
						doChanges.add(sb.toString());
						sb.setLength(0);
					}
					sb.append("update current_do_status set lifted_qty=lifted_qty+").append(newNet).append(" where do_number='").append(newVal.getDoNumber()).append("' and wb_code='").append(newVal.loadGrossWb).append("' ");
					doChanges.add(sb.toString());
					sb.setLength(0);
					sb.append("update current_do_status_apprvd set lifted_qty=lifted_qty+").append(newNet).append(" where do_number='").append(newVal.getDoNumber()).append("' and wb_code='").append(newVal.loadGrossWb).append("' ");
					doChanges.add(sb.toString());
					sb.setLength(0);
				}
			}
			 
			ArrayList<String> wbToSend = new ArrayList<String>();
			for (int art=0;art<2;art++) {
				SpecificTPChange change = art == 0 ? newVal : oldVal;
				for (int art2=0;art2<2;art2++) {
					String wb = art2 == 0 ? change.loadTareWb : change.loadGrossWb;
					if (wb != null) {
						boolean found = false;
						for (int i=0,is=wbToSend.size();i<is;i++) {
							if (wb.equals(wbToSend.get(i))) {
								found = true;
								break;
							}
						}
						if (!found)
							wbToSend.add(wb);
					}
				}
			}
			return new TPRChanges(tprId, retval, doChanges, wbToSend);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rsReg = Misc.closeRS(rsReg);
			rsApp = Misc.closeRS(rsApp);
			ps = Misc.closePS(ps);
			psApp = Misc.closePS(psApp);
		}
	}
	public void save(Connection conn, SeclRemoteConnManager.Station station, int applied) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("insert into remote_tpr_changes(wb_code, on_date, applied, query) values (?,?,?,?)");
			ps.setString(1, station.getCode());
			ps.setTimestamp(2, Misc.longToSqlDate(System.currentTimeMillis()));
			for (int art=0;art<2;art++) {
				ArrayList<String> qlist = art == 0 ? this.tpQueriesOnRemote : this.doQueriesOnRemote;
				for (int i=0,is=qlist == null ? 0 : qlist.size(); i<is; i++) {
					ps.setInt(3, applied);
					ps.setString(4, qlist.get(i));
					ps.execute();
				}
			}
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
			
		}
	}
	public void applyDOLocal(Connection conn) throws Exception {
		try {
			for (int i=0,is=doQueriesOnRemote == null ? 0 : this.doQueriesOnRemote.size(); i<is; i++) {
				String q = this.doQueriesOnRemote.get(i);
				PreparedStatement ps = conn.prepareStatement(q);
				ps.execute();
				ps = Misc.closePS(ps);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public String  applyRemote(Connection conn, boolean doingServer) throws Exception {
		//TP changes on local will have been done on approval ..
		//DO changes on local will be done by applyDOLocal ...
		StringBuilder sb = new StringBuilder();
		if (true) //DEBUG13 to make it entrant 
			return sb.toString();
		if (this.wbToSend == null || this.wbToSend.size() == 0)
			return sb.toString();
		ArrayList<SeclRemoteConnManager.Station> stationList = SeclRemoteConnManager.getStationAndParentInfo(conn, this.wbToSend);
		if (stationList == null || stationList.size() == 0)
			return sb.toString();
		for (int i=0,is=stationList == null ? 0 : stationList.size(); i<is; i++) {
			SeclRemoteConnManager.Station station = stationList.get(i);
			if (station.isSameAsMeMachine(conn))
				continue; //already done
			Connection remoteConn = null;
			boolean succ = true;
			try {
				remoteConn = station.getConnection();
				for (int art=0;art<2;art++) {
					ArrayList<String> qlist = art == 0 ? this.tpQueriesOnRemote : this.doQueriesOnRemote;
					for (int j=0,js=qlist == null ? 0 : qlist.size(); j<js; j++) {
						PreparedStatement ps = remoteConn.prepareStatement(qlist.get(j));
						ps.execute();
						ps = Misc.closePS(ps);
					}
				}
				if (!remoteConn.getAutoCommit())
					remoteConn.commit();
			}
			catch (Exception e2) {
				e2.printStackTrace();
				succ = false;
			}
			finally {
				if (remoteConn != null) {
					try {
						remoteConn.close();
						remoteConn = null;
					}
					catch (Exception e3) {
						
					}
				}//close remoteConn
				if (!succ) {//save it
					//sb.append("Updates to:").append(station.getCode()).append(" failed. ")
					if (sb.length() != 0)
						sb.append(",");
					else {
						sb.append("TPR Id[").append(this.tprId).append("] on WB [");
					}
					sb.append(station.getCode() == null ? " Server" : station.getCode());
					this.save(conn, station,0);
					if (!conn.getAutoCommit())
						conn.commit();
				}
				else {
					this.save(conn, station,1);
					if (!conn.getAutoCommit())
						conn.commit();
				}
				
			}//finally within loop for each station
		}//each station
		if (sb.length() > 0)
			sb.append("]<br/>");
		return sb.toString();
	}//end of func
	public int getTprId() {
		return tprId;
	}
	public void setTprId(int tprId) {
		this.tprId = tprId;
	}

}

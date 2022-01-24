package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Value;

public class RemoteTPRHelper {
	
	private static ArrayList<Pair<String, Integer>> g_colsOfInterest = new ArrayList<Pair<String, Integer>>();
	static { 
		g_colsOfInterest.add(new Pair<String, Integer>("tpr_id", Cache.INTEGER_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("vehicle_name", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("material_cat", Cache.INTEGER_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("challan_no", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("lr_no", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("do_number", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("invoice_number", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("is_latest", Cache.INTEGER_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("tpr_status", Cache.INTEGER_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("allow_gross_tare_diff_wb", Cache.INTEGER_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("mines_code", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("transporter_code", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("product_code", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("customer_code", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("destination_code", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("destination_state_code", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("washery_code", Cache.STRING_TYPE));
		
		g_colsOfInterest.add(new Pair<String, Integer>("latest_load_wb_in_out", Cache.DATE_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("load_wb_in_name", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("load_tare", Cache.NUMBER_TYPE));

		g_colsOfInterest.add(new Pair<String, Integer>("latest_load_wb_out_out", Cache.DATE_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("load_wb_out_name", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("load_gross", Cache.NUMBER_TYPE));

		g_colsOfInterest.add(new Pair<String, Integer>("latest_unload_wb_in_out", Cache.DATE_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("unload_wb_in_name", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("unload_tare", Cache.NUMBER_TYPE));

		g_colsOfInterest.add(new Pair<String, Integer>("latest_unload_wb_out_out", Cache.DATE_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("unload_wb_out_name", Cache.STRING_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("unload_gross", Cache.NUMBER_TYPE));
		
		g_colsOfInterest.add(new Pair<String, Integer>("status", Cache.INTEGER_TYPE));
		g_colsOfInterest.add(new Pair<String, Integer>("propagate_changes_in_real_time", Cache.INTEGER_TYPE));
	}
	private static ArrayList<Pair<String, Integer>> g_colsOfInterestForWt = new ArrayList<Pair<String, Integer>>();
	static { 
		g_colsOfInterestForWt.add(new Pair<String, Integer>("invoice_number", Cache.STRING_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("latest_load_wb_in_out", Cache.DATE_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("load_wb_in_name", Cache.STRING_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("load_tare", Cache.NUMBER_TYPE));

		g_colsOfInterestForWt.add(new Pair<String, Integer>("latest_load_wb_out_out", Cache.DATE_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("load_wb_out_name", Cache.STRING_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("load_gross", Cache.NUMBER_TYPE));

		g_colsOfInterestForWt.add(new Pair<String, Integer>("latest_unload_wb_in_out", Cache.DATE_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("unload_wb_in_name", Cache.STRING_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("unload_tare", Cache.NUMBER_TYPE));

		g_colsOfInterestForWt.add(new Pair<String, Integer>("latest_unload_wb_out_out", Cache.DATE_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("unload_wb_out_name", Cache.STRING_TYPE));
		g_colsOfInterestForWt.add(new Pair<String, Integer>("unload_gross", Cache.NUMBER_TYPE));

	}
	public static boolean ofInterestToWtWf(String col) {
		for (int i=0,is=g_colsOfInterestForWt.size(); i<is; i++) {
			if (g_colsOfInterestForWt.get(i).first.equals(col))
				return true;
		}
		return false;
	}
	public static ArrayList<String> filterWBReachable(ArrayList<String> wbReachable, ArrayList<String> wbOfInterest) {
		ArrayList<String> retval = new ArrayList<String>();
		for (int i=0,is=wbReachable == null ? 0 : wbReachable.size(); i<is;i++) {
			String wb = wbReachable.get(i);
			boolean found = false;
			for (int j=0,js=wbOfInterest == null ? 0 : wbOfInterest.size(); j<js; j++) {
				if (wbOfInterest.get(j).equals(wb)) {
					found = true;
				}
			}
			if (found)
				retval.add(wb);
		}
		return retval;
	}
	public static ArrayList<String> getWBOfInterest(HashMap<String, Value> newTPR, HashMap<String, Value> oldTPR)  {
		ArrayList<String> retval = new ArrayList<String>();
		for (int art=0;art<2;art++) {
			HashMap<String, Value> tpr = art == 0 ? newTPR : oldTPR;
			for (int art2=0;art2<4;art2++) {
				Value wb = art2 == 0 ? tpr.get("load_wb_in_name") : art2 == 1 ? tpr.get("load_wb_out_name") : art2 == 2 ? tpr.get("unload_wb_in_name") : tpr.get("unload_wb_out_name");
				if (wb == null || wb.isNull())
					continue;
				boolean alreadyAdded = false;
				for (int t1=0,t1s=retval.size();t1<t1s; t1++) {
					if (retval.get(t1).equals(wb.getStringVal())) {
						alreadyAdded = true;
						break;
					}
				}
				if (alreadyAdded)
					continue;
				retval.add(wb.getStringVal());
			}
		}
		return retval;
	}
	public static double getLoadNet(HashMap<String, Value> tprInfo) {
		Value gross = tprInfo.get("load_gross");
		Value tare = tprInfo.get("load_tare");
		double grossd = gross == null || gross.isNull() ? 0 : gross.getDoubleVal();
		double tared = tare == null || tare.isNull() ? 0 : tare.getDoubleVal();
		return grossd-tared;
	}
	public static void adjustColsChangedForDataUpdate(HashMap<String, Value> newTPR, HashMap<String, Value> oldTPR, ArrayList<Pair<String, Integer>> colsChanged, boolean doingWtWf) {
		if (doingWtWf) {
			
		}
	}
	public static int copyDataTo(Connection conn, HashMap<String, Value> tprInfo, int toTprId, boolean isReg, ArrayList<Pair<String, Integer>> colsChanged) throws Exception {
		String tab = isReg ? "tp_record" : "tp_record_apprvd";
		StringBuilder sb=new StringBuilder();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (Misc.isUndef(toTprId)) {
				String challanNo = tprInfo.get("challan_no").getStringVal();
				toTprId = RemoteTPRHelper.getTPRIdFromChallan(challanNo, conn, true);
			}
			boolean isNew = Misc.isUndef(toTprId);
			if (!isReg) {
				String challanNo = RemoteTPRHelper.getChallanFromTPRId(toTprId, conn, isReg);//checking if there is an entry in apprvd
				if (challanNo == null)
					isNew = true;
			}
			
			if (isNew) {
				sb.append("insert into ").append(isReg ? "tp_record" : "tp_record_apprvd").append(" (");
			}
			else {
				sb.append("update  ").append(isReg ? "tp_record" : "tp_record_apprvd").append(" set ");
			}
			boolean isFirst = true;
			for (int i=0,is=colsChanged.size();i<is; i++) {
				if ("tpr_id".equals(colsChanged.get(i).first) || "propagate_changes_in_real_time".equals(colsChanged.get(i).first))
						continue;
				if (!isFirst)
					sb.append(",");
				sb.append(colsChanged.get(i).first);
				if (!isNew)
					sb.append("=?");
				isFirst = false;
			}
			
			if (isNew) {
				if (!isReg)
					sb.append(",tpr_id");
				sb.append(") values (");
				isFirst = true;
				for (int i=0,is=colsChanged.size();i<is; i++) {
					if ("tpr_id".equals(colsChanged.get(i).first) || "propagate_changes_in_real_time".equals(colsChanged.get(i).first))
							continue;
					if (!isFirst)
						sb.append(",");
					sb.append("?");
					isFirst = false;
				}
				if (!isReg)
					sb.append(",?");
				sb.append(")");
			}
			else {
				sb.append(" where tpr_id=? ");
			}
			ps = conn.prepareStatement(sb.toString());
			int idx = 1;
			for (int i=0,is=colsChanged.size();i<is; i++) {
				if ("tpr_id".equals(colsChanged.get(i).first) || "propagate_changes_in_real_time".equals(colsChanged.get(i).first))
						continue;
				
				Value val = tprInfo.get(colsChanged.get(i).first);
				if (colsChanged.get(i).second == Cache.STRING_TYPE) {
					ps.setString(idx,val == null ? null : val.getStringVal());
				}
				else if (colsChanged.get(i).second == Cache.DATE_TYPE) {
					ps.setTimestamp(idx, val == null ? null : Misc.longToSqlDate(val.getDateValLong()));
				}
				else if (colsChanged.get(i).second == Cache.NUMBER_TYPE) {
					Misc.setParamDouble(ps, val == null ? Misc.getUndefDouble() : val.getDoubleVal(), idx);
				}
				else {
					Misc.setParamInt(ps, val == null ? Misc.getUndefInt() : val.getIntVal(), idx);
				}
				idx++;
			}
			if (!isNew || !isReg) {
				ps.setInt(idx, toTprId);
			}
			ps.executeUpdate();
			if (isNew && isReg) {
				rs = ps.getGeneratedKeys();
				toTprId = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
				rs = Misc.closeRS(rs);
			}
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit())
				conn.rollback();
		
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
		}
		return toTprId;
	}
	
	private static String getChallanFromTPRId(int tprId, Connection conn, boolean isReg) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select challan_no from tp_record"+(isReg ? "" : "_apprvd")+" where tpr_id=?");
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			String challanNo = null;
			if (rs.next()) {
				challanNo = rs.getString(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return challanNo;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	static int getTPRIdFromChallan(String challanNo, Connection conn, boolean isReg) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select tpr_id from tp_record"+(isReg ? "" : "_apprvd")+" where challan_no=? order by (case when status = 1 then 10 else status end) desc , tpr_id desc");
			ps.setString(1, challanNo);
			rs = ps.executeQuery();
			int tprId = Misc.getUndefInt();
			if (rs.next()) {
				tprId = rs.getInt(1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return tprId;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
	}
	
	public static String getDiffTPR(HashMap<String, Value> newTPR, HashMap<String, Value> oldTPR, ArrayList<Pair<String, Integer>> changedCols) {
		StringBuilder sb = new StringBuilder();
		for (int i=0,is=changedCols.size(); i<is;i++) {
			if ("tpr_id".equals(changedCols.get(i)) || "propagate_changes_in_real_time".equals(changedCols.get(i)))
				continue;
			sb.append("[").append(changedCols.get(i).first).append(" O:").append(oldTPR.get(changedCols.get(i).first)).append(" N:").append(newTPR.get(changedCols.get(i).first)).append("] ");
		}
		return sb.toString();
	}
	
	public static HashMap<String, Value> readData(Connection conn, int tprId, boolean isReg) throws Exception {
		PreparedStatement ps =  null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select ");
			for (int i=0,is=g_colsOfInterest.size(); i<is; i++) {
				if (i != 0)
					sb.append(",");
				sb.append(g_colsOfInterest.get(i).first);
			}
			sb.append(" from tp_record").append(isReg ? "" : "_apprvd").append(" where tpr_id=?" );
			ps = conn.prepareStatement(sb.toString());
			HashMap<String, Value> retval = new HashMap<String, Value>();
			ps.setInt(1, tprId);
			rs = ps.executeQuery();
			if (rs.next()) {
				for (int i=0,is=g_colsOfInterest.size(); i<is; i++) {
					String colName = g_colsOfInterest.get(i).first;
					int ty = g_colsOfInterest.get(i).second;
					Value val = null;
					if (ty == Cache.STRING_TYPE)
						val = new Value(rs.getString(i+1));
					else if (ty == Cache.DATE_TYPE)
						val = new Value(Misc.sqlToLong(rs.getTimestamp(i+1)));
					else if (ty == Cache.NUMBER_TYPE) 
						val = new Value(Misc.getRsetDouble(rs, i+1));
					else 
						val = new Value(Misc.getRsetInt(rs, i+1));
					retval.put(colName, val);
				}	
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
		}
	}
	public static String getColsChangedAsString(ArrayList<Pair<String, Integer>> result) {
		StringBuilder sb = new StringBuilder();
		for (int i=0,is=result.size();i<is;i++) {
			if (i != 0)
				sb.append(",");
			sb.append(result.get(i).first);
		}
		return sb.toString();
	}
	public static ArrayList<Pair<String, Integer>> getColsChangedAsArray(String changedCols)  {
		ArrayList<Pair<String, Integer>> retval = new ArrayList<Pair<String, Integer>>();
		String[] cols = changedCols.split(",");
		int prevIndex = -1;
		for (int i=0,is=cols == null ? 0 : cols.length; i<is; i++) {
			String str = cols[i];
			for (int j=prevIndex+1,js = g_colsOfInterest.size(); j<js;j++) {
				if (g_colsOfInterest.get(j).first.equals(str)) {
					retval.add(g_colsOfInterest.get(j));
					break;
				}
			}
		}
		return retval;
	}
	public static int getTPRInt(HashMap<String, Value> tpr, String col) {
		Value v = tpr == null ? null : tpr.get(col);
		return v == null ? Misc.getUndefInt() : v.getIntVal();
	}
	public static double getTPRDouble(HashMap<String, Value> tpr, String col) {
		Value v = tpr == null ? null : tpr.get(col);
		return v == null ? Misc.getUndefDouble() : v.getDoubleVal();
	}
	public static long getTPRDate(HashMap<String, Value> tpr, String col) {
		Value v = tpr == null ? null : tpr.get(col);
		return v == null ? Misc.getUndefInt() : v.getDateValLong();
	}
	public static String getTPRString(HashMap<String, Value> tpr, String col) {
		Value v = tpr == null ? null : tpr.get(col);
		return v == null ? null : v.getStringVal();
	}
	public static ArrayList<Pair<String, Integer>> identifyFieldChanges(HashMap<String, Value> reg, HashMap<String, Value> apprvd, ArrayList<Pair<String, Integer>> forTheseCols, boolean doingWtWf) {
		ArrayList<Pair<String, Integer>> retval = new ArrayList<Pair<String, Integer>>();
		HashMap<String, String> colsAskedFor = new HashMap<String, String>();
		if (forTheseCols == null)
			forTheseCols = g_colsOfInterest;
		for (int i=0,is=forTheseCols == null ? 0 : forTheseCols.size(); i<is; i++) {
			colsAskedFor.put(forTheseCols.get(i).first, forTheseCols.get(i).first);
		}
		//if loadGross is not null in New then need to copy over all cols+load if necessary
		Value newLoadGross = reg.get("load_gross");
		boolean newLoadGrossIsValid = (newLoadGross != null && newLoadGross.isNotNull()) || (colsAskedFor.containsKey("load_gross") || colsAskedFor.containsKey("latest_load_wb_out_out") || colsAskedFor.containsKey("load_wb_out_name"));
		Value newUnloadTare = reg.get("unload_tare");
		boolean newUnloadTareIsValid = (newUnloadTare != null && newUnloadTare.isNotNull())  || (colsAskedFor.containsKey("unload_tare") || colsAskedFor.containsKey("latest_unload_wb_out_out") || colsAskedFor.containsKey("unload_wb_out_name"));;
		boolean needsStatusLike = false;
		Value oldStatus = apprvd.get("status");
		Value oldTPRStatus = apprvd.get("tpr_status");
		Value oldLatest = apprvd.get("is_latest");
		Value oldVehicle = apprvd.get("vehicle_name");
		Value oldChallan = apprvd.get("challan_no");
		for (int i=0,is=g_colsOfInterest.size(); i<is; i++) {
			
			String colName = g_colsOfInterest.get(i).first;
			if ("tpr_id".equals(colName) || "propagate_changes_in_real_time".equals(colName))
				continue;
			
			boolean wtRelatedCol =ofInterestToWtWf(colName);
			if (wtRelatedCol && !doingWtWf)
				continue;
			boolean doOnlyIfOldIsNull = !wtRelatedCol && doingWtWf;
			Value regVal = reg.get(colName);
			Value apprvdVal = apprvd.get(colName);
			boolean isDiff = (regVal != null && apprvdVal != null && !regVal.equals(apprvdVal)) || (regVal == null && apprvdVal != null && apprvdVal.isNotNull()) || (apprvdVal == null && regVal != null && regVal.isNotNull());
			if (isDiff) { 
				boolean toAdd = colsAskedFor.containsKey(colName);
				if (!toAdd && newLoadGrossIsValid) {
					//add all stuff that are relevant prior to Load
					if (!(colName.equals("unload_tare") || colName.equals("latest_unload_wb_in_out") || colName.equals("unload_wb_in")
							|| colName.equals("unload_gross") || colName.equals("latest_unload_wb_out_out") || colName.equals("unload_wb_out") 
							))
						toAdd=true;
				}
				if (!toAdd && newUnloadTareIsValid)
					toAdd = true;
				if (toAdd)
					retval.add(g_colsOfInterest.get(i));
			}
		}
		
		return retval;
	}

	public static void doIsLatestRelated(Connection conn, HashMap<String, Value> oldTPR, HashMap<String, Value> newTPR, int tprId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Value newLatestVal = newTPR.get("is_latest");
			int newLatest = newLatestVal == null || newLatestVal.isNull() ? 0 : newLatestVal.getIntVal();
			Value newTprStatusVal = newTPR.get("tpr_status");
			int newTprStatus = newTprStatusVal == null || newTprStatusVal.isNull() ? 0 : newTprStatusVal.getIntVal();
			Value oldTprStatusVal = oldTPR.get("tpr_status");
			int oldTprStatus = oldTprStatusVal == null || oldTprStatusVal.isNull() ? 0 : oldTprStatusVal.getIntVal();
			
			Value oldLatestVal = oldTPR.get("is_latest");
			int oldLatest = oldLatestVal == null || oldLatestVal.isNull() ? 0 : oldLatestVal.getIntVal();
			if ((newLatest != oldLatest || newTprStatus != oldTprStatus) && newLatest == 1 && newTprStatus == 0) {
				String vehicleName = getTPRString(newTPR,"vehicle_name");
				vehicleName = CacheTrack.standardizeName(vehicleName);
				ps = conn.prepareStatement("select id from vehicle where std_name=? order by (case when status=1 then 10 else status end) desc");
				ps.setString(1, vehicleName);
				rs = ps.executeQuery();
				int vehicleId = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
				
				ps = conn.prepareStatement("update tp_record set tpr_status=2, is_latest=0 where vehicle_id = ? and tpr_id <> ?");
				ps.setInt(1, vehicleId);
				ps.setInt(2, tprId);
				ps.executeUpdate();
				ps = Misc.closePS(ps);
				
				ps = conn.prepareStatement("update tp_record_apprvd set tpr_status=2, is_latest=0 where vehicle_id = ? and tpr_id <> ?");
				ps.setInt(1, vehicleId);
				ps.setInt(2, tprId);
				ps.executeUpdate();
				ps = Misc.closePS(ps);
					
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		
	}
	
}

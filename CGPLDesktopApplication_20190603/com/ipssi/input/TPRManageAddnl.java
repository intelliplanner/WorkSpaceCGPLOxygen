package com.ipssi.input;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MiscInner.Pair;
import com.ipssi.gen.utils.MiscInner.PairIntBool;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Value;
import com.ipssi.rfid.constant.Type;


public class TPRManageAddnl {
	public static String CHECK_OTHER_OPEN_LATEST = "select 1 from tp_record tp1 where exists (select 1 from tp_record tp2 where tp2.vehicle_id = tp1.vehicle_id and tp2.status in (1) and tp1.tpr_id <> tp2.tpr_id and tp2.is_latest = 1 and tp2.tpr_status <> 2) where tp1.tpr_id = ?";
	static String GET_WT_INFO = "select latest_load_wb_in_out, latest_load_wb_out_out, latest_unload_wb_in_out, latest_unload_wb_out_out from tp_record where tpr_id = ?";
	static String UPDATE_UNLOAD_OUT = "update tp_record set tpr_status=0, is_latest=1, propagate_changes_in_real_time=1, latest_unload_wb_out_out = null, unload_tare=null, earliest_unload_wb_out_in = null, unload_wb_out_name=null, earliest_unload_gate_out_in=null, latest_unload_gate_out_out=null, unload_gate_out_name=null where tpr_id = ?";
	static String UPDATE_UNLOAD_IN = "update tp_record set tpr_status=0, is_latest=1, propagate_changes_in_real_time=1, latest_unload_wb_in_out = null, unload_gross=null, earliest_unload_wb_in_in = null, unload_wb_in_name=null, latest_unload_wb_out_out = null, unload_tare=null, earliest_unload_wb_out_in = null, unload_wb_out_name=null, earliest_unload_gate_out_in=null, latest_unload_gate_out_out=null, unload_gate_out_name=null where tpr_id = ?"; 
	static String UPDATE_LOAD_OUT = "update tp_record set tpr_status=0, is_latest=1,propagate_changes_in_real_time=1,  latest_load_wb_out_out = null, load_gross=null, earliest_load_wb_out_in = null, load_wb_out_name=null, earliest_load_gate_out_in=null, latest_load_gate_out_out=null, load_gate_out_name=null, earliest_unload_gate_in_in=null, latest_unload_gate_in_out=null, unload_gate_in_name=null, latest_unload_wb_in_out = null, unload_gross=null, earliest_unload_wb_in_in = null, unload_wb_in_name=null, latest_unload_wb_out_out = null, unload_tare=null, earliest_unload_wb_out_in = null, unload_wb_out_name=null, earliest_unload_gate_out_in=null, latest_unload_gate_out_out=null, unload_gate_out_name=null where tpr_id = ?";
	static String UPDATE_LOAD_IN = "update tp_record set tpr_status=0, is_latest=1,propagate_changes_in_real_time=1,  latest_load_wb_in_out = null, load_tare=null, earliest_load_wb_in_in = null, load_wb_in_name=null, latest_load_wb_out_out = null, load_gross=null, earliest_load_wb_out_in = null, load_wb_out_name=null, earliest_load_gate_out_in=null, latest_load_gate_out_out=null, load_gate_out_name=null, earliest_unload_gate_in_in=null, latest_unload_gate_in_out=null, unload_gate_in_name=null, latest_unload_wb_in_out = null, unload_gross=null, earliest_unload_wb_in_in = null, unload_wb_in_name=null, latest_unload_wb_out_out = null, unload_tare=null, earliest_unload_wb_out_in = null, unload_wb_out_name=null, earliest_unload_gate_out_in=null, latest_unload_gate_out_out=null, unload_gate_out_name=null where tpr_id = ?";
	static String COMPLETE_OTHERS = "update tp_record set tpr_status=2 where tpr_status=0 and tpr_id <> ? and vehicle_id=?";
	static String COMPLETE_OTHERS_APPRVD = "update tp_record_apprvd set tpr_status=2 where tpr_status=0 and tpr_id <> ? and vehicle_id=?";
	static String GET_VEHICLE_ID = "select vehicle_id from tp_record where tpr_id=?";
	static String MARK_LATEST = "update tp_record set tpr_status=0, is_latest=1,propagate_changes_in_real_time=1 where tpr_id = ?";

	private static void helperSimpleMakeLatest(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = conn.prepareStatement(GET_VEHICLE_ID);
		ps.setInt(1, tprId);
		ResultSet rs = ps.executeQuery();
		int vehicleId = Misc.getUndefInt();
		
		while (rs.next()) {
			vehicleId = rs.getInt(1);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(COMPLETE_OTHERS);
		ps.setInt(1, tprId);
		ps.setInt(2, vehicleId);
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(COMPLETE_OTHERS_APPRVD);
		ps.setInt(1, tprId);
		ps.setInt(2, vehicleId);
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(MARK_LATEST);
		ps.setInt(1, tprId);
		ps.execute();
		ps = Misc.closePS(ps);
	}
	
	private static void helperClearWt(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = conn.prepareStatement(GET_WT_INFO);
		ps.setInt(1, tprId);
		ResultSet rs = ps.executeQuery();
		java.sql.Timestamp dt1 = null;
		java.sql.Timestamp dt2 = null;
		java.sql.Timestamp dt3 = null;
		java.sql.Timestamp dt4 = null;
		
		while (rs.next()) {
			dt1 = rs.getTimestamp(1);
			dt2 = rs.getTimestamp(2);
			dt3 = rs.getTimestamp(3);
			dt4 = rs.getTimestamp(4);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(dt4 != null ? UPDATE_UNLOAD_OUT : dt3 != null ? UPDATE_UNLOAD_IN : dt2 != null ? UPDATE_LOAD_OUT : UPDATE_LOAD_IN);
		ps.setInt(1, tprId);
		ps.execute();
		ps = Misc.closePS(ps);
	}
	
	public static void handleEnableForNextWt(Connection conn, ArrayList<Integer> tprIds) throws Exception {
		for (int i=0,is=tprIds == null ? 0 : tprIds.size(); i<is;i++) {
			helperClearWt(conn, tprIds.get(i));
			helperSimpleMakeLatest(conn, tprIds.get(i));
			//set up workflow etc
		}
	}
	
	public static void handleMakeMeLatest(Connection conn, ArrayList<Integer> tprIds) throws Exception {
		for (int i=0,is=tprIds == null ? 0 : tprIds.size(); i<is;i++) {
			helperSimpleMakeLatest(conn, tprIds.get(i));
			//set up workflow etc
		}
	}
	
	public static boolean checkIfAnyOtherOpenAndLatest(Connection conn, int tprId)  throws Exception {
		boolean retval = false;
		PreparedStatement ps = conn.prepareStatement(CHECK_OTHER_OPEN_LATEST);
		ps.setInt(1, tprId);
		ResultSet rs = ps.executeQuery();
		retval = rs.next();
		rs = Misc.closeRS(rs);
	    ps = Misc.closePS(ps);
	    return retval;
	}
	
	public static boolean isDataChangeAllowed(Connection conn, SessionManager session, int tprId, Value newReportingStatus, Value newLatest, InputTemplate.TPRStatusInfo tprStatusInfo, StringBuilder errMsg) throws Exception {
		boolean retval = true;
		if (newReportingStatus != null && newReportingStatus.isNotNull() && session != null && !session.getUser().isSuperUser() && tprStatusInfo != null) {
			if (!tprStatusInfo.isReportingStatusChangeAllowed(newReportingStatus.getIntVal(), Misc.getUndefInt())) {
				retval = false;
				if (errMsg != null) {
					if (errMsg.length() != 0)
						errMsg.append("<br/>");
					errMsg.append("Reporting status change in ").append(tprId).append(" is not allowed");
				}
			}
		}
		if (retval) {
			if (newLatest != null && newLatest.isNotNull() && session != null && !session.getUser().isSuperUser() && tprStatusInfo != null && newLatest.getIntVal() == 1 && tprStatusInfo.isLatest != 1) {
				if (checkIfAnyOtherOpenAndLatest(conn, tprId)) {
					retval = false;
					if (errMsg != null) {
						if (errMsg.length() != 0)
							errMsg.append("<br/>");
						errMsg.append("Is Latest cannot be changed in ").append(tprId).append(" because another open latest trip exists");
					}
				}
			}	
		}
		return retval;
	}
	//OVERRIDE_RELATED
	public static int getVehicleId(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select vehicle_id from tp_record where tpr_id=?");
		ps.setInt(1, tprId);
		ResultSet rs = ps.executeQuery();
		int vehicleId = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
		rs  = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return vehicleId;
	}
	
	public static int getLatestTPRId(Connection conn, int vehicleId) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select tp_record.tpr_id from tp_record where vehicle_id =? and is_latest=1 and tpr_status in (0,1)");
		ps.setInt(1, vehicleId);
		ResultSet rs = ps.executeQuery();
		int tprId = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
		rs  = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return tprId;
	}
	
	public static int G_NO_OVERRIDE_ASKED = 0;
	public static int G_CAN_OVERRIDE = 1;
	public static int G_CANT_OVERRIDE_BECAUSE_MULTIPLE_TPR = 2;
	public static int G_CANT_OVERRIDE_BECAUSE_WBIN_SKIPPED = 3;
	public static int G_CANT_OVERRIDE_BECAUSE_WBOUT_SKIPPED = 4;
	public static int G_CANT_OVERRIDE_BECAUSE_RECORD_ALREADY_CAPTURED = 5;
	public static int G_CANT_OVERRIDE_BECAUSE_MULTI_OPEN_PARTIAL = 6;
	public static int G_CANT_OVERRIDE_BECAUSE_MULTI_OPEN_NO_PARTIAL = 7;
	public static class TPRandBlockInfo {
		int tprId = Misc.getUndefInt();
		int vehicleId = Misc.getUndefInt();
		int materialCat = 0;
		int nextStep = 1;
		int blockedStep = 2;
		boolean has2002 = false;
		boolean hasAlreadyRecorded = false;
		long comboStart = -1;
		long comboEnd = -1;
		int stepCompleted = -1;
		int firstStepCompleted = 0;
		boolean hasMultipleOpen = false;
		boolean hasPartial = false;
		ArrayList<MiscInner.Pair> currBlocks = null;
		
		public int getMaterialCat() {
			return materialCat;
		}
		public void setMaterialCat(int materialCat) {
			this.materialCat = materialCat;
		}
		public int getNextStep() {
			return nextStep;
		}
		public void setNextStep(int nextStep) {
			this.nextStep = nextStep;
		}
		public int getBlockedStep() {
			return blockedStep;
		}
		public void setBlockedStep(int blockedStep) {
			this.blockedStep = blockedStep;
		}
		public boolean isHas2002() {
			return has2002;
		}
		public void setHas2002(boolean has2002) {
			this.has2002 = has2002;
		}
		public ArrayList<MiscInner.Pair> getCurrBlocks() {
			return currBlocks;
		}
		public void setCurrBlocks(ArrayList<MiscInner.Pair> currBlocks) {
			this.currBlocks = currBlocks;
		}
		public TPRandBlockInfo(int tprId, int vehicleId, int materialCat, int nextStep, int blockedStep,
				boolean has2002, ArrayList<Pair> currBlocks, long comboStart, long comboEnd, int stepCompleted, int firstStepCompleted
				,boolean hasMultipleOpen,boolean hasPartial) {
			super();
			this.tprId = tprId;
			this.vehicleId = vehicleId;
			this.materialCat = materialCat;
			this.nextStep = nextStep;
			this.blockedStep = blockedStep;
			this.has2002 = has2002;
			this.currBlocks = currBlocks;
			this.comboStart = comboStart;
			this.comboEnd = comboEnd;
			this.stepCompleted = stepCompleted;
			this.firstStepCompleted = firstStepCompleted;
			this.hasMultipleOpen = hasMultipleOpen;
			this.hasPartial = hasPartial;
		}
		public int getTprId() {
			return tprId;
		}
		public void setTprId(int tprId) {
			this.tprId = tprId;
		}
		public int getVehicleId() {
			return vehicleId;
		}
		public void setVehicleId(int vehicleId) {
			this.vehicleId = vehicleId;
		}
		public long getComboStart() {
			return comboStart;
		}
		public void setComboStart(long comboStart) {
			this.comboStart = comboStart;
		}
		public long getComboEnd() {
			return comboEnd;
		}
		public void setComboEnd(long comboEnd) {
			this.comboEnd = comboEnd;
		}
		public int getStepCompleted() {
			return stepCompleted;
		}
		public void setStepCompleted(int stepCompleted) {
			this.stepCompleted = stepCompleted;
		}
		public int getFirstStepCompleted() {
			return firstStepCompleted;
		}
		public void setFirstStepCompleted(int firstStepCompleted) {
			this.firstStepCompleted = firstStepCompleted;
		}
		public boolean isHasAlreadyRecorded() {
			return hasAlreadyRecorded;
		}
		public void setHasAlreadyRecorded(boolean hasAlreadyRecorded) {
			this.hasAlreadyRecorded = hasAlreadyRecorded;
		}
		public boolean isHasMultipleOpen() {
			return hasMultipleOpen;
		}
		public boolean isHasPartial() {
			return hasPartial;
		}
		public void setHasMultipleOpen(boolean hasMultipleOpen) {
			this.hasMultipleOpen = hasMultipleOpen;
		}
		public void setHasPartial(boolean hasPartial) {
			this.hasPartial = hasPartial;
		}
		
	}
	public static TPRandBlockInfo getTPRandBlockInfo(Connection conn, int tprId) throws Exception {
		return getTPRandBlockInfo(conn, tprId, Misc.getUndefInt());
	}
	public static TPRandBlockInfo getTPRandBlockInfo(Connection conn, int tprId,int vehicleId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = conn.prepareStatement("select tpr_id, vehicle_id, material_cat, next_step_type, combo_start, combo_end " +
				",(case when latest_unload_gate_out_out is not null then 7 when latest_unload_wb_out_out is not null then 6 "+
				" when latest_unload_yard_out_out is not null then 5 when latest_unload_yard_in_out is not null then 4"+
				" when latest_unload_wb_in_out is not null then 3 when latest_reg_out is not null and next_step_type > 2 then 2 "+
				" when latest_unload_gate_in_out is not null then 1 else 0 end) "+
				",(case when latest_unload_gate_in_out is not null then 1 when latest_unload_wb_in_out is not null then 3 "+
				" when latest_reg_out is not null and next_step_type > 2 then 2 "+
				" when latest_unload_yard_in_out is not null then 4 when latest_unload_yard_out_out is not null then 5"+
				" when latest_unload_wb_out_out is not null then 6  "+
				" when latest_unload_gate_out_out is not null then 7 else 0 end) "+
				"from tp_record where tpr_id=?");
		ps.setInt(1, tprId);
		rs = ps.executeQuery();
		int materialCat = 0;
		int nextStepType = 1;
		long comboStart = -1;
		long comboEnd = -1;
		int stepCompleted = 0;
		int firstStepStarted = 0;
		if (rs.next()) {
			tprId = Misc.getRsetInt(rs, 1);
			vehicleId = Misc.getRsetInt(rs, 2);
			materialCat = rs.getInt(3);
			nextStepType = Misc.getRsetInt(rs, 4, 1);
			comboStart = Misc.sqlToLong(rs.getTimestamp(5));
			comboEnd = Misc.sqlToLong(rs.getTimestamp(6));
			stepCompleted = rs.getInt(7);
			firstStepStarted = rs.getInt(8);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement("select bins.type, workstation_type_id from tpr_block_status left outer join block_instruction bins on (bins.id=tpr_block_status.instruction_id)  where tpr_id = ? and tpr_block_status.status in (1,2) and (override_workstation_type_id is null or override_workstation_type_id < workstation_type_id) order by tpr_block_status.created_on");
		//ps = conn.prepareStatement("select instruction_id, workstation_type_id,ins.type from tpr_block_status where tpr_id = ? and status in (1,2) and (override_workstation_type_id is null or override_workstation_type_id < workstation_type_id) order by created_on ");
		ps.setInt(1, tprId);
		ArrayList<MiscInner.Pair> currBlocks = new ArrayList<MiscInner.Pair>();
		rs = ps.executeQuery();
		int maxStep = -1;
		boolean has2002 = false;
		boolean has2001 = false;
		while (rs.next()) {
			int instr = Misc.getRsetInt(rs, 1);
			int askedStep = Misc.getRsetInt(rs, 2);
			currBlocks.add(new MiscInner.Pair(instr, askedStep));
			if (askedStep > maxStep)
				maxStep = askedStep;
			if (instr == 2002)
				has2002 = true;
			if(instr == 2001)
				has2001 = true;
		}
		/*ps.clearParameters();
		ps = conn.prepareStatement("");*/
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement("select count(*), " +
				" sum(case when material_cat=0 then " +
				" (case when ((unload_tare is null or unload_tare=0.0) and (unload_gross is null or unload_gross=0.0)) " +
				"  or ((unload_tare is not null or unload_tare>0.0) and (unload_gross is not null or unload_gross>0.0)) then 0 else 1 end  )" +
				" else " +
				" (case when ((load_tare is null or load_tare=0.0) and (load_gross is null or load_gross=0.0)) " +
				"  or ((load_tare is not null and load_tare>0.0) and (load_gross is not null and load_gross>0.0)) then 0 else 1 end  )" +
				" end )"+
				" from tp_record where vehicle_id=? and tpr_status=0 and status=1 group by vehicle_id ");
		ps.setInt(1, vehicleId);
		rs = ps.executeQuery();
		int openTPRCount = Misc.getUndefInt();
		boolean hasMultipleOpen = false;
		boolean hasPartial = false;
		if(rs.next()){
			openTPRCount = Misc.getRsetInt(rs, 1);
			hasPartial = Misc.getRsetInt(rs, 2) > 0;
		}
		has2002 = has2002 && (openTPRCount > 1);
		hasMultipleOpen = (openTPRCount > 1);
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		return new TPRandBlockInfo(tprId, vehicleId, materialCat, nextStepType, maxStep, has2002, currBlocks, comboStart, comboEnd, stepCompleted, firstStepStarted,hasMultipleOpen,hasPartial);
	}
	/*public static TPRandBlockInfo getTPRandBlockInfoNew(Connection conn, int tprId) throws Exception {

		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = conn.prepareStatement("select tpr_id, vehicle_id, material_cat, next_step_type, combo_start, combo_end " +
				",(case when latest_unload_gate_out_out is not null then 7 when latest_unload_wb_out_out is not null then 6 "+
				" when latest_unload_yard_out_out is not null then 5 when latest_unload_yard_in_out is not null then 4"+
				" when latest_unload_wb_in_out is not null then 3 when latest_reg_out is not null and next_step_type > 2 then 2 "+
				" when latest_unload_gate_in_out is not null then 1 else 0 end) "+
				",(case when latest_unload_gate_in_out is not null then 1 when latest_unload_wb_in_out is not null then 3 "+
				" when latest_reg_out is not null and next_step_type > 2 then 2 "+
				" when latest_unload_yard_in_out is not null then 4 when latest_unload_yard_out_out is not null then 5"+
				" when latest_unload_wb_out_out is not null then 6  "+
				" when latest_unload_gate_out_out is not null then 7 else 0 end) "+
				"from tp_record where tpr_id=?");
		ps.setInt(1, tprId);
		rs = ps.executeQuery();
		int materialCat = 0;
		int nextStepType = 1;
		int vehicleId = Misc.getUndefInt();
		long comboStart = -1;
		long comboEnd = -1;
		int stepCompleted = 0;
		int firstStepStarted = 0;
		if (rs.next()) {
			tprId = Misc.getRsetInt(rs, 1);
			vehicleId = Misc.getRsetInt(rs, 2);
			materialCat = rs.getInt(3);
			nextStepType = Misc.getRsetInt(rs, 4, 1);
			comboStart = Misc.sqlToLong(rs.getTimestamp(5));
			comboEnd = Misc.sqlToLong(rs.getTimestamp(6));
			stepCompleted = rs.getInt(7);
			firstStepStarted = rs.getInt(8);
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement("select bins.type, workstation_type_id from tpr_block_status left outer join block_instruction bins on (bins.id=tpr_block_status.instruction_id)  where tpr_id = ? and tpr_block_status.status in (1,2) and (override_workstation_type_id is null or override_workstation_type_id < workstation_type_id) order by tpr_block_status.created_on");
		//ps = conn.prepareStatement("select instruction_id, workstation_type_id,ins.type from tpr_block_status where tpr_id = ? and status in (1,2) and (override_workstation_type_id is null or override_workstation_type_id < workstation_type_id) order by created_on ");
		ps.setInt(1, tprId);
		ArrayList<MiscInner.Pair> currBlocks = new ArrayList<MiscInner.Pair>();
		rs = ps.executeQuery();
		int maxStep = -1;
		boolean has2002 = false;
		boolean has2001 = false;
		while (rs.next()) {
			int instr = Misc.getRsetInt(rs, 1);
			int askedStep = Misc.getRsetInt(rs, 2);
			currBlocks.add(new MiscInner.Pair(instr, askedStep));
			if (askedStep > maxStep)
				maxStep = askedStep;
			if (instr == 2002)
				has2002 = true;
			if(instr == 2001)
				has2001 = true;
		}
		ps.clearParameters();
		ps = conn.prepareStatement("");
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		
		ps = conn.prepareStatement("select tpr_id, material_cat, next_step_type" +
				",(case when latest_unload_gate_out_out is not null then 7 when latest_unload_wb_out_out is not null then 6 "+
				" when latest_unload_yard_out_out is not null then 5 when latest_unload_yard_in_out is not null then 4"+
				" when latest_unload_wb_in_out is not null then 3 when latest_reg_out is not null and next_step_type > 2 then 2 "+
				" when latest_unload_gate_in_out is not null then 1 else 0 end) "+
				"from tp_record where vehicle_id=? and tpr_status=0 and status=1 and tpr_id not in (?) order by combo_start desc");
		ps.setInt(1, vehicleId);
		ps.setInt(2, tprId);
		rs = ps.executeQuery();
		int openTPRCount = 0;
		int currMaterialCat = Misc.getUndefInt();
		int prevLatest = 
		while(rs.next()){
			openTPRCount++;
			int _tprId = Misc.getRsetInt(rs, 1);
			int _materialCat = rs.getInt(2);
			int _nextStepType = Misc.getRsetInt(rs, 3, 1);
			int _stepCompleted = rs.getInt(4);
			if(maxStep != 1 && ( Misc.isUndef(currMaterialCat) || currMaterialCat == _materialCat) && maxStep == _stepCompleted){
				
			}
		}
		rs = Misc.closeRS(rs);
		ps = Misc.closePS(ps);
		if(has2001 && openTPRCount == 2){
			
		}else if(openTPRCount > 1){
			has2002 = true;
		}
		return new TPRandBlockInfo(tprId, vehicleId, materialCat, nextStepType, maxStep, has2002, currBlocks, comboStart, comboEnd, stepCompleted, firstStepStarted);
	
	}*/
	public static int canOverrideNew(Connection conn, TPRandBlockInfo tprAndBlockInfo) throws Exception {

		ArrayList<MiscInner.Pair> currBlocks = tprAndBlockInfo.getCurrBlocks(); 
		if(tprAndBlockInfo.isHasMultipleOpen()){
			if(tprAndBlockInfo.isHasPartial()){
				return G_CANT_OVERRIDE_BECAUSE_MULTI_OPEN_PARTIAL;
			}else{
				return G_CANT_OVERRIDE_BECAUSE_MULTI_OPEN_NO_PARTIAL;
			}
		}
		if (currBlocks.size() == 0) {
			return G_NO_OVERRIDE_ASKED;
		}else if (tprAndBlockInfo.getMaterialCat() == Type.TPRMATERIAL.COAL) {
			int nextStep = tprAndBlockInfo.getNextStep();
			int blockedStep = tprAndBlockInfo.getBlockedStep();
			if (nextStep <= Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE && blockedStep > Type.WorkStationType.WEIGH_BRIDGE_IN_TYPE) {
				return G_CANT_OVERRIDE_BECAUSE_WBIN_SKIPPED;
			}
			else if (nextStep <= Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE && blockedStep > Type.WorkStationType.WEIGH_BRIDGE_OUT_TYPE) {
				return G_CANT_OVERRIDE_BECAUSE_WBOUT_SKIPPED;
			}
			else {
				return G_CAN_OVERRIDE;
			}
		}
		else if (tprAndBlockInfo.getMaterialCat() == Type.TPRMATERIAL.FLYASH) {
			int nextStep = tprAndBlockInfo.getNextStep();
			int blockedStep = tprAndBlockInfo.getBlockedStep();
			if (nextStep <= Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE && blockedStep > Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE) {
				return G_CANT_OVERRIDE_BECAUSE_WBIN_SKIPPED;
			}else {
				return G_CAN_OVERRIDE;
			}
		}else {
			return G_CAN_OVERRIDE;
		}
	}
	public static int canOverride(Connection conn, TPRandBlockInfo tprAndBlockInfo) throws Exception {
		int retval = G_NO_OVERRIDE_ASKED;
		
		//2 get max step at which the override step is less than
		ArrayList<MiscInner.Pair> currBlocks = tprAndBlockInfo.getCurrBlocks(); 
		PairIntBool isRejectableTPR = isRejectableOverride(conn, tprAndBlockInfo, Misc.getUndefInt());
		if (currBlocks.size() == 0) {
			retval = G_NO_OVERRIDE_ASKED;
		}else if(isRejectableTPR.second){
			retval = G_CANT_OVERRIDE_BECAUSE_RECORD_ALREADY_CAPTURED;
		}else if (tprAndBlockInfo.isHas2002()) {
			retval = G_CANT_OVERRIDE_BECAUSE_MULTIPLE_TPR;
		}
		else if (tprAndBlockInfo.getMaterialCat() == Type.TPRMATERIAL.COAL) {
			int nextStep = tprAndBlockInfo.getNextStep();
			int blockedStep = tprAndBlockInfo.getBlockedStep();
			if (nextStep <= 3 && blockedStep > 3) {
				retval = G_CANT_OVERRIDE_BECAUSE_WBIN_SKIPPED;
			}
			else if (nextStep <= 6 && blockedStep > 6) {
				retval = G_CANT_OVERRIDE_BECAUSE_WBOUT_SKIPPED;
			}
			else {
				retval = G_CAN_OVERRIDE;
			}
		}
		else if (tprAndBlockInfo.getMaterialCat() == Type.TPRMATERIAL.FLYASH) {
			int nextStep = tprAndBlockInfo.getNextStep();
			int blockedStep = tprAndBlockInfo.getBlockedStep();
			if (nextStep <= Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE && blockedStep > Type.WorkStationType.FLY_ASH_GROSS_WT_TYPE) {
				retval = G_CANT_OVERRIDE_BECAUSE_WBIN_SKIPPED;
			}else {
				retval = G_CAN_OVERRIDE;
			}
		}else {
			retval = G_CAN_OVERRIDE;
		}
		return retval;
	}
	public static MiscInner.PairIntBool isRejectableOverride(Connection conn, TPRandBlockInfo tprAndBlockInfo, int maxComboEndComboStartAllowed) throws Exception {
		//first = the id to reject to, second = if prev combo_end is close enough to this ..
		//1. if nextStep is <= 1 //ie gate in not done ..
		//2. AND the immediate trip that is prior to this is open, of same material code and combo_end of prev <= combo_start of this
		boolean rejectAble = false;
		if (Misc.isUndef(maxComboEndComboStartAllowed))
			maxComboEndComboStartAllowed = 90*60*1000;
		int rejectToId = Misc.getUndefInt();
		if (tprAndBlockInfo.getNextStep() <= 1) {
			String q = "select tpo.tpr_id, tpo.material_cat, tpo.tpr_status, tpo.next_step_type, tpo.combo_end "+
			  " from tp_record tpn join tp_record tpo on (tpn.tpr_id = ? and tpn.vehicle_id = tpo.vehicle_id and tpo.status in (1) and tpo.combo_end <= tpn.combo_start and tpo.tpr_status=0 and tpn.tpr_id <> tpo.tpr_id ) "+
			    " order by tpo.combo_start desc";
			PreparedStatement ps = conn.prepareStatement(q);
			ps.setInt(1, tprAndBlockInfo.getTprId());
			ResultSet rs = ps.executeQuery();
			int tprCount = 0;
			while (rs.next()) {
				int tprId = Misc.getRsetInt(rs, 1);
				int matCode = Misc.getRsetInt(rs, 2);
				int tprStatus = Misc.getRsetInt(rs, 3);
				int nextStepType = Misc.getRsetInt(rs, 4);
				long comboEnd = Misc.sqlToLong(rs.getTimestamp(5));
				if (matCode == tprAndBlockInfo.getMaterialCat() && nextStepType == tprAndBlockInfo.getBlockedStep()+1) { //&& nextStepType == tprAndBlockInfo.getBlockedStep()+1) {
					rejectToId = tprId;
					if ((tprAndBlockInfo.getComboStart() - comboEnd) <=maxComboEndComboStartAllowed)
						rejectAble = true;
				}
				tprCount++;
			}
			if(tprCount > 1){
				rejectAble = false;
				rejectToId = Misc.getUndefInt();
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new MiscInner.PairIntBool(rejectToId, rejectAble);
	}
	
	public static void rejectTPROverrideSimple(Connection conn, int oldTprId, int rejectToId) throws Exception {
		//doing nothing .. we should somewhere keep record of timins and set status = 0
	}
	public static void rejectTPROverride(Connection conn, int oldTprId, int rejectToId) throws Exception {
		//Do in both reg and apprvd
		//1. of Old - make status 0, reporting status 9, cancellation_reason = user error, reference tpr id to reject, is_latest=0, tpr_status=2
		//2. of New - make is_latest=1
		if (!Misc.isUndef(rejectToId)) {
			String qold = "update tp_record set status=0, is_latest=0, reporting_status=9, cancellation_reason=3, status_reason='Override being rejected', ref_tpr_id_if_cancelled=? where tpr_id = ?";
			String qnew = "update tp_record set is_latest=1 where tpr_id = ?";
			for (int art=0;art<2;art++) {
				String q1 = art == 0 ? qold : qold.replaceAll(" tp_record ", " tp_record_apprvd ");
				PreparedStatement ps = conn.prepareStatement(q1);
				ps.setInt(1, rejectToId);
				ps.setInt(2, oldTprId);
				ps.execute();
				ps = Misc.closePS(ps);
				String q2 = art == 0 ? qnew : qnew.replaceAll(" tp_record ", " tp_record_apprvd ");
				ps = conn.prepareStatement(q2);
				ps.setInt(1, rejectToId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
		}
	}
	
	
	public static void closeTPROverride(Connection conn, int tprId) throws Exception {
		//Do in both reg and apprvd
		//1. of Old - make tpr_status=2 
		if (!Misc.isUndef(tprId)) {
			String qnew = "update tp_record set tpr_status=2 where tpr_id = ?";
			for (int art=0;art<2;art++) {
				String q2 = art == 0 ? qnew : qnew.replaceAll(" tp_record ", " tp_record_apprvd ");
				PreparedStatement ps = conn.prepareStatement(q2);
				ps.setInt(1, tprId);
				ps.execute();
				ps = Misc.closePS(ps);
			}
		}
	}
	public static boolean isMergeAbleTPR(Connection conn, int fromTPRId, int toTPRId) throws Exception {
		//tpr is mergeable if from's comboEnd <= to's comboStart and materialCode is the same and start Step of to is 0 or after from
		TPRandBlockInfo from = getTPRandBlockInfo(conn, fromTPRId);
		TPRandBlockInfo to = getTPRandBlockInfo(conn, toTPRId);
		return to != null && from != null && from.getComboEnd() <= to.getComboStart() && from.getMaterialCat() == to.getMaterialCat()
		 && (to.getNextStep() <= 0 ||
				 from.getStepCompleted() < to.getFirstStepCompleted() 
				 )
				 ;				 
	}
	public static void mergeTPROverride(Connection conn, int fromTPRId, int toTPRId) throws Exception {
		//whew ..
		TPRandBlockInfo from = getTPRandBlockInfo(conn, fromTPRId);
		TPRandBlockInfo to = getTPRandBlockInfo(conn, toTPRId);
		TPRManageAddnl.helpeMergePreGateIn(conn, fromTPRId, toTPRId);
		TPRManageAddnl.helperMergeRFPart(conn, fromTPRId, toTPRId);
		if (from.getFirstStepCompleted() > 0) {
			TPRManageAddnl.helpeMergeTillStep(conn, fromTPRId, toTPRId, from.getFirstStepCompleted());
		}
	}
	
	public static void  helperMergeRFPart(Connection conn, int fromTPRId, int toTPRId) throws Exception {
		StringBuilder sb = new StringBuilder();
		boolean hasAdded = false;
		sb.append("update tp_record tp2 join tp_record tp1 on (tp1.tpr_id = ? and tp2.tpr_id = ? and tp2.rf_record_id is null and tp1.rf_record_id is not null) set ");
		hasAdded = addUpdClause(sb, "is_merged_with_hh_tpr", hasAdded);
		hasAdded = addUpdClause(sb, "rf_lr_date", hasAdded);
		hasAdded = addUpdClause(sb, "rf_transporter_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_mines_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_grade", hasAdded);
		hasAdded = addUpdClause(sb, "rf_challan_date", hasAdded);
		hasAdded = addUpdClause(sb, "rf_challan_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_lr_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_load_tare", hasAdded);
		hasAdded = addUpdClause(sb, "rf_load_gross", hasAdded);
		hasAdded = addUpdClause(sb, "rf_device_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_do_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_record_id", hasAdded);
		hasAdded = addUpdClause(sb, "rf_record_key", hasAdded);
		hasAdded = addUpdClause(sb, "rf_vehicle_id", hasAdded);
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		ps.setInt(1, fromTPRId);
		ps.setInt(2, toTPRId);
		int rowsUpd = ps.executeUpdate();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(sb.toString().replaceAll(" tp_record ", " tp_record_apprvd "));
		ps.setInt(1, fromTPRId);
		ps.setInt(2, toTPRId);
		ps.executeUpdate();
		ps = Misc.closePS(ps);
		
		if (rowsUpd > 0) {
			ps = conn.prepareStatement("update rfid_handheld_log set ref_tpr_id = ? where ref_tpr_id = ?");
			ps.setInt(1, toTPRId);
			ps.setInt(2, fromTPRId);
			ps.execute();
			ps = Misc.closePS(ps);
		}
		else {
			ps = conn.prepareStatement("update rfid_handheld_log set ref_tpr_id = null where ref_tpr_id = ?");
			ps.setInt(1, fromTPRId);
			ps.execute();
			ps = Misc.closePS(ps);
		}
 		
		
	}
	public static boolean overideAndContinueWithCurr(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = null;
		int vehicleId = Misc.getUndefInt();
		try{
			vehicleId = getVehicleId(conn, tprId);
			if(Misc.isUndef(vehicleId))
				return false;
			ps = conn.prepareStatement("update tp_record set is_latest=0, tpr_status=2 where tpr_status=0 and vehicle_id=? and tpr_id != ?");
			Misc.setParamInt(ps, vehicleId, 1);
			Misc.setParamInt(ps, tprId, 2);
			System.out.println("[Query]:"+ps.toString());
			ps.executeUpdate();
			
			ps.clearParameters();
			ps = conn.prepareStatement("update tp_record_apprvd set is_latest=0, tpr_status=2  where tpr_status=0 and vehicle_id=? and tpr_id != ?");
			Misc.setParamInt(ps, vehicleId, 1);
			Misc.setParamInt(ps, tprId, 2);
			ps.executeUpdate();
			ps.close();
			ps=null;
			overideTPR(conn, tprId);
		    
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return true;
	}
	public static boolean overideTPR(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = null;
		int vehicleId = Misc.getUndefInt();
		try{
			vehicleId = getVehicleId(conn, tprId);
			if(Misc.isUndef(vehicleId))
				return false;
			ps = conn.prepareStatement("update block_instruction bi join tpr_block_status tbs on (bi.id=tbs.instruction_id) set bi.status=0 where tbs.create_type=1 and tbs.type=0 and (tbs.override_workstation_type_id is null or  tbs.override_workstation_type_id < workstation_type_id) and tpr_id=?");
			Misc.setParamInt(ps, tprId, 1);
			System.out.println("[Query]:"+ps.toString());
			ps.executeUpdate();
			
			ps.clearParameters();
			ps = conn.prepareStatement("update tpr_block_status tbs set tbs.override_workstation_type_id=(case when tbs.type=0 then tbs.workstation_type_id else 1000 end),tbs.override_date=now(),tbs.override_status=1 where (tbs.override_workstation_type_id is null or  tbs.override_workstation_type_id < workstation_type_id) and tpr_id=?");
			Misc.setParamInt(ps, tprId, 1);
			ps.executeUpdate();
			
		    
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return true;
	}
	public static boolean helperMakeLatest(Connection conn, int tprId) throws Exception {
		PreparedStatement ps = null;
		int vehicleId = Misc.getUndefInt();
		try{
			vehicleId = getVehicleId(conn, tprId);
			if(Misc.isUndef(vehicleId))
				return false;
			ps = conn.prepareStatement("update tp_record set is_latest=0, tpr_status=2 where tpr_status=0 and vehicle_id=?");
			Misc.setParamInt(ps, vehicleId, 1);
			System.out.println("[Query]:"+ps.toString());
			ps.executeUpdate();
			
			ps.clearParameters();
			ps = conn.prepareStatement("update tp_record set is_latest=1 ,tpr_status=0 where tpr_id=?");
			Misc.setParamInt(ps, tprId, 1);
			ps.executeUpdate();
			
			ps.clearParameters();
			ps = conn.prepareStatement("update tp_record_apprvd set is_latest=0, tpr_status=2  where tpr_status=0 and vehicle_id=?");
			Misc.setParamInt(ps, vehicleId, 1);
			ps.executeUpdate();
			
			ps.clearParameters();
			ps = conn.prepareStatement("update tp_record_apprvd set is_latest=1 ,tpr_status=0 where tpr_id=?");
			Misc.setParamInt(ps, tprId, 1);
			ps.executeUpdate();
		    
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return true;
	}
	
	public static void helpeMergeTillStep(Connection conn, int fromTPRId, int toTPRId, int tillInclStep) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("update tp_record tp2 join tp_record tp1 on (tp1.tpr_id = ? and tp2.tpr_id = ?) set ");
		boolean hasAdded = false;
		if (tillInclStep >= 1) {
			hasAdded = addUpdClause(sb, "earliest_unload_gate_in_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_unload_gate_in_out", hasAdded);
			hasAdded = addUpdClause(sb, "driver_id", hasAdded);
			hasAdded = addUpdClause(sb, "dl_no", hasAdded);
			hasAdded = addUpdClause(sb, "driver_name", hasAdded);
			hasAdded = addUpdClause(sb, "driver_src", hasAdded);
			hasAdded = addUpdClause(sb, "vehicle_src", hasAdded);
			hasAdded = addUpdClause(sb, "mark_for_gps", hasAdded);
			hasAdded = addUpdClause(sb, "rf_also_on_card", hasAdded);
			hasAdded = addUpdClause(sb, "is_new_vehicle", hasAdded);
			
		}
		if (tillInclStep >= 2) {
			hasAdded = addUpdClause(sb, "earliest_reg_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_reg_out", hasAdded);
			hasAdded = addUpdClause(sb, "challan_data_edit_at_reg", hasAdded);
			
		}
		if (tillInclStep >= 3) {
			hasAdded = addUpdClause(sb, "earliest_unload_wb_in_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_unload_wb_in_out", hasAdded);
			hasAdded = addUpdClause(sb, "mark_for_qc", hasAdded);
			hasAdded = addUpdClause(sb, "permit_no", hasAdded);
			hasAdded = addUpdClause(sb, "material_description", hasAdded);
			hasAdded = addUpdClause(sb, "mark_for_gps", hasAdded);
			hasAdded = addUpdClause(sb, "mark_for_qc_reason", hasAdded);
			hasAdded = addUpdClause(sb, "rf_also_on_card", hasAdded);
			hasAdded = addUpdClause(sb, "challan_data_edit_at_wb", hasAdded);
			hasAdded = addUpdClause(sb, "wbin_station_id", hasAdded);
		}
		if (tillInclStep >= 4) {
			hasAdded = addUpdClause(sb, "earliest_unload_yard_in_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_unload_yard_in_out", hasAdded);
			hasAdded = addUpdClause(sb, "bed_assigned", hasAdded);
			hasAdded = addUpdClause(sb, "mark_for_gps", hasAdded);
		}
		if (tillInclStep >= 5) {
			hasAdded = addUpdClause(sb, "earliest_unload_yard_out_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_unload_yard_out_out", hasAdded);
		}
		if (tillInclStep >= 6) {
			hasAdded = addUpdClause(sb, "earliest_unload_wb_out_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_unload_wb_out_out", hasAdded);
			hasAdded = addUpdClause(sb, "wbout_station_id", hasAdded);
		}
		if (tillInclStep >= 7) {
			hasAdded = addUpdClause(sb, "earliest_unload_gate_out_in", hasAdded);
			hasAdded = addUpdClause(sb, "latest_unload_gate_out_out", hasAdded);
		}
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		ps.setInt(1, fromTPRId);
		ps.setInt(2, toTPRId);
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(sb.toString().replaceAll(" tp_record ", " tp_record_apprvd "));
		ps.setInt(1, fromTPRId);
		ps.setInt(2, toTPRId);
		ps.execute();
		ps = Misc.closePS(ps);
		
		String q = null;
		q = "delete from tp_step where tpr_id=? and work_station_type <= ?";
		ps = conn.prepareStatement(q);
		ps.setInt(1, toTPRId);
		ps.setInt(2, tillInclStep);
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(q.replaceAll(" tp_step ", " tp_step_apprvd "));
		ps.setInt(1, toTPRId);
		ps.setInt(2, tillInclStep);
		ps.execute();
		ps = Misc.closePS(ps);

		q = "insert into tp_step (tpr_id, vehicle_id, work_station_type, work_station_id, has_valid_rf, in_time, out_time, updated_on, user_by, result_code, for_block) (select ? vehicle_id, work_station_type, work_station_id, has_valid_rf, in_time, out_time, updated_on, user_by, result_code, for_block where tpr_id = ? and work_station_type <= ?)";
		ps = conn.prepareStatement(q);
		ps.setInt(1, toTPRId);
		ps.setInt(2, fromTPRId);
		ps.setInt(3, tillInclStep);
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(q.replaceAll(" tp_step ", " tp_step_apprvd "));
		ps.setInt(1, toTPRId);
		ps.setInt(2, fromTPRId);
		ps.setInt(3, tillInclStep);
		ps = Misc.closePS(ps);
		
		q ="insert into tps_question_detail (tpr_id, question_id, answer_id, updated_on, user_by) (select ?, t1.question_id, t1.answer_id, t1.updated_on, t1.user_by from tps_question_detail t1 where t1.tpr_id = ? and not exists(select 1 from tps_question_detail t2 where t2.tpr_id = ? and t2.question_id = t1.question_id))"; 
		ps = conn.prepareStatement(q);
		ps.setInt(1, toTPRId);
		ps.setInt(2, fromTPRId);
		ps.setInt(3, toTPRId);
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(q.replaceAll(" tps_question_detail ", " tps_question_detail_apprvd "));
		ps.setInt(1, toTPRId);
		ps.setInt(2, fromTPRId);
		ps.setInt(3, toTPRId);
		ps.execute();
		ps = Misc.closePS(ps);
		
	}
	public static void helpeMergePreGateIn(Connection conn, int fromTPRId, int toTPRId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("update tp_record tp2 join tp_record tp1 on (tp1.tpr_id = ? and tp2.tpr_id = ?) set ");
		boolean hasAdded = false;
		hasAdded = addUpdClause(sb, "transporter_id", hasAdded);
		hasAdded = addUpdClause(sb, "do_id", hasAdded);
		hasAdded = addUpdClause(sb, "material_grade_id", hasAdded);
		hasAdded = addUpdClause(sb, "mines_id", hasAdded);
		hasAdded = addUpdClause(sb, "challan_no", hasAdded);
		hasAdded = addUpdClause(sb, "challan_date", hasAdded);
		hasAdded = addUpdClause(sb, "tpr_create_date", hasAdded);
		hasAdded = addUpdClause(sb, "lr_no", hasAdded);
		hasAdded = addUpdClause(sb, "lr_date", hasAdded);
		hasAdded = addUpdClause(sb, "load_tare", hasAdded);
		hasAdded = addUpdClause(sb, "load_gross", hasAdded);
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		ps.setInt(1, fromTPRId);
		ps.setInt(2, toTPRId);
		ps.execute();
		ps = Misc.closePS(ps);
		ps = conn.prepareStatement(sb.toString().replaceAll(" tp_record ", " tp_record_apprvd "));
		ps.setInt(1, fromTPRId);
		ps.setInt(2, toTPRId);
		ps.execute();
		ps = Misc.closePS(ps);
	}
	
	public static boolean addUpdClause(StringBuilder sb, String col, boolean hasAdded) {
		if (hasAdded)
			sb.append(",");
		hasAdded = true;
		sb.append(" tp2.").append(col).append(" = (case when tp2.").append(col).append(" is null then tp1.").append(col).append(" else tp2.").append(col).append(" end) ");
		return hasAdded;
	}
	
}

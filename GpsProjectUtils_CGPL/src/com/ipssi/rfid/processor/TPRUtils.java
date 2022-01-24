package com.ipssi.rfid.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.StepResults;
import com.ipssi.rfid.beans.TATResults;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.beans.ViolationsResults;
import com.ipssi.rfid.constant.Results;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.db.RFIDMasterDao;

public class TPRUtils {
	public static int CHECK_GPS_WORKING = 1;
	public static int CHECK_TAT_VIOLATION = 2;
	public static int CHECK_ALL_VIOLATION = 3;
	
	public static boolean getPaperValidity(Connection conn, int vehicleId){
		return true;
	}
	public static TATResults getTATResults(Connection conn, int tprId){
		return new TATResults();
	}
	public static ViolationsResults getViolations(Connection conn, int vehicleId, int tprId, int violationType){
		return new ViolationsResults();
	}
	public static Pair <Date, Boolean> getDriverHours(Connection conn, int driverId){
		return new Pair <Date, Boolean>(new Date(), true);
	}
	public static Pair<String, Integer> getUnloadAssignment(Connection conn, int tprId){
		return new Pair <String, Integer>("", 0);
	}
	public static Pair<String, Integer> getWBAssignment(Connection conn, int tprId){
		return new Pair <String, Integer>("", 0);
	}
	public static StepResults getNextStepDetail(Connection conn, int tprId){
		return new StepResults();
	}
	public static void printResult(Connection conn, int tprId, StepResults stepResults ){
		
	}
	public static boolean getGPSStatus(Connection conn, int vehicleId){
		return true;
	}
	public static boolean getMarkQCResult(Connection conn, int vehicleId, int tprId, double grossRecvWt){
		return true;
	}
	public static boolean scanDoc(Connection conn, int tprId, int docType){
		return true;
	}
	public static boolean getFinalProcessStatus(Connection conn, int tprId){
		return true;
	}
	public static String getPropertyValue(Connection conn, String key){
		return "";
	}
	public static void setProperty(Connection conn, String path, String propertyName){
		
	}
	/*public static ArrayList<Pair<Integer, String>> getBedListJava(Connection conn, int transporterId,int mines, int grade) {
		ArrayList<Pair<Integer, String>> retval = null;
		retval = getBedList(conn, transporterId, mines, grade);
		if(retval != null && retval.size() > 0)
			return retval;
		retval = getBedList(conn, transporterId, mines, Misc.getUndefInt());
		if(retval != null && retval.size() > 0)
			return retval;
		retval = getBedList(conn, transporterId, Misc.getUndefInt(),grade);
		if(retval != null && retval.size() > 0)
			return retval;
		retval = getBedList(conn, Misc.getUndefInt(), mines, grade);
		if(retval != null && retval.size() > 0)
			return retval;
		retval = getBedList(conn, transporterId, Misc.getUndefInt(), Misc.getUndefInt());
		if(retval != null && retval.size() > 0)
			return retval;
		retval = getBedList(conn, Misc.getUndefInt(), mines, Misc.getUndefInt());
		if(retval != null && retval.size() > 0)
			return retval;
		retval = getBedList(conn, Misc.getUndefInt(), Misc.getUndefInt(), grade);
		if(retval != null && retval.size() > 0)
			return retval;
		return retval;
	}*/
	public static ArrayList<Pair<Integer, String>> getBedList(Connection conn, int transporterId,int mines, int grade, int doId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Pair<Integer, String>> retval = null;
		String query = " SELECT bed.id, bed_details.name, " +
				" concat" +
				"( " +
				" case when bed_details.name is null then \"\" else bed_details.name end , " +
				" case when lhd.name is null then \"\" else \" \" end, " +
				" case when lhd.name is null then \"\" else lhd.name end , " +
				" case when rhd.name is null then \"\" else \"-\" end, " +
				" case when rhd.name is null then \"\" else rhd.name end, " +
				" case when lhd2.name is null then \"\" else \" or \" end, " +
				" case when lhd2.name is null then \"\" else lhd2.name end , " +
				" case when rhd2.name is null then \"\" else \"-\" end, " +
				" case when rhd2.name is null then \"\" else rhd2.name end " +
				" ) bed_name, " +
				" (case " +
				/*" when (bed.transporter_id=? and bed.mines_id=? and bed.grade_id=?) then 0"+
				" when (bed.transporter_id=? and bed.mines_id=? and bed.grade_id is null) then 1"+
				" when (bed.transporter_id=? and bed.mines_id is null and bed.grade_id=?) then 2"+
				" when (bed.transporter_id=? and bed.mines_id is null and bed.grade_id is null) then 3"+
				" when (bed.transporter_id is null and bed.mines_id=? and bed.grade_id=?) then 4"+
				" when (bed.transporter_id is null and bed.mines_id=? and bed.grade_id is null) then 5"+
				" when (bed.transporter_id is  null and bed.mines_id is null and bed.grade_id=?) then 6 " +
				" else 7 " +*/
                " when (bed.transporter_id=? and bed.mode=? and bed.mines_id=? and bed.grade_id=?) then 0"+
                " when (bed.transporter_id=? and bed.mode=? and bed.mines_id=? and bed.grade_id is null ) then 1"+
                " when (bed.transporter_id=? and bed.mode=? and bed.mines_id is null and bed.grade_id =? ) then 2"+
                " when (bed.transporter_id=? and bed.mode=? and bed.mines_id is null and bed.grade_id is null ) then 3"+
                " when (bed.transporter_id=? and bed.mode is null and bed.mines_id =? and bed.grade_id =? ) then 4"+
                " when (bed.transporter_id=? and bed.mode is null and bed.mines_id =? and bed.grade_id is null ) then 5"+
                " when (bed.transporter_id=? and bed.mode is null and bed.mines_id is null and bed.grade_id = ? ) then 6"+
                " when (bed.transporter_id=? and bed.mode is null and bed.mines_id is null and bed.grade_id is null ) then 7"+
                
                " when (bed.transporter_id is null and bed.mode=? and bed.mines_id=? and bed.grade_id=?) then 8"+
                " when (bed.transporter_id is null and bed.mode=? and bed.mines_id=? and bed.grade_id is null ) then 9"+
                " when (bed.transporter_id is null and bed.mode=? and bed.mines_id is null and bed.grade_id =? ) then 10"+
                " when (bed.transporter_id is null and bed.mode=? and bed.mines_id is null and bed.grade_id is null ) then 11"+
                " when (bed.transporter_id is null and bed.mode is null and bed.mines_id =? and bed.grade_id =? ) then 12"+
                " when (bed.transporter_id is null and bed.mode is null and bed.mines_id =? and bed.grade_id is null ) then 13"+
                " when (bed.transporter_id is null and bed.mode is null and bed.mines_id is null and bed.grade_id = ? ) then 14"+
                " else 15 "+
				" end) weight"+
				" from bed_assignment_details bed join bed_details on (bed.curr_bed_module = bed_details.id) " +
				" left outer join hopper_details lhd on (lhd.id = bed.curr_start_hopper_no) " +
				" left outer join hopper_details rhd on (rhd.id = bed.curr_end_hopper_no)  "+
				" left outer join hopper_details lhd2 on (lhd2.id = bed.hopper_2_start) " +
				" left outer join hopper_details rhd2 on (rhd2.id = bed.hopper_2_end)  "+
				" where " +
				//" (((? is null and bed.transporter_id is null) or bed.transporter_id = ?) and ((? is null and bed.mines_id is null) or bed.mines_id=?) and ((? is null and bed.grade_id is null) or bed.grade_id=?) ) " +
				//" and " +
				" bed.status=1 "+
				//" and curr_start_date <= now() and (curr_end_date is null or curr_end_date >= now())"+
				" order by weight  ";
		try {
			int mode = getDoMode(conn,doId);
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, transporterId, 1);
			Misc.setParamInt(ps, mode, 2);
			Misc.setParamInt(ps, mines, 3);
			Misc.setParamInt(ps, grade, 4);
			Misc.setParamInt(ps, transporterId, 5);
			Misc.setParamInt(ps, mode, 6);
			Misc.setParamInt(ps, mines, 7);
			Misc.setParamInt(ps, transporterId, 8);
			Misc.setParamInt(ps, mode, 9);
			Misc.setParamInt(ps, grade, 10);
			Misc.setParamInt(ps, transporterId, 11);
			Misc.setParamInt(ps, mode, 12);
			
			Misc.setParamInt(ps, transporterId, 13);
			Misc.setParamInt(ps, mines, 14);
			Misc.setParamInt(ps, grade, 15);
			Misc.setParamInt(ps, transporterId, 16);
			Misc.setParamInt(ps, mines, 17);
			Misc.setParamInt(ps, transporterId, 18);
			Misc.setParamInt(ps, grade, 19);
			Misc.setParamInt(ps, transporterId, 20);
			
			Misc.setParamInt(ps, mode, 21);
			Misc.setParamInt(ps, mines, 22);
			Misc.setParamInt(ps, grade, 23);
			Misc.setParamInt(ps, mode, 24);
			Misc.setParamInt(ps, mines, 25);
			Misc.setParamInt(ps, mode, 26);
			Misc.setParamInt(ps, grade, 27);
			Misc.setParamInt(ps, mode, 28);
			
			Misc.setParamInt(ps, mines, 29);
			Misc.setParamInt(ps, grade, 30);
			Misc.setParamInt(ps, mines, 31);
			Misc.setParamInt(ps, grade, 32);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				if(retval == null)
					retval = new ArrayList<Pair<Integer,String>>();
				retval.add(new Pair<Integer, String>(rs.getInt(1), rs.getString(3)));
				/*bed_Id = rs.getInt(1);
	                bed_name = rs.getString(2);*/
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return retval;
	}
	private static int getDoMode(Connection conn, int doId) {
		if(Misc.isUndef(doId) )
			return Misc.getUndefInt();
		PreparedStatement ps = null;
        ResultSet rs = null;
        try{
        	 ps = conn.prepareStatement("select type from do_rr_details where id=?");
        	 ps.setInt(1, doId);
        	 rs = ps.executeQuery();
             while (rs.next()) {
            	 return Misc.getRsetInt(rs, "type");
             }
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
		return Misc.getUndefInt();
	}
	private static ArrayList<Pair<Integer, String>> getBedListLast(Connection conn, int transporterId,int mines, int grade) {
		String bed_name = "";
		int bed_Id = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String where = null;
		ArrayList<Pair<Integer, String>> retval = null;
		String query = " SELECT bed.id, bed_details.name, " +
				" concat" +
				"( " +
				" case when bed_details.name is null then \"\" else bed_details.name end , " +
				" case when lhd.name is null then \"\" else \" \" end, " +
				" case when lhd.name is null then \"\" else lhd.name end , " +
				" case when rhd.name is null then \"\" else \"-\" end, " +
				" case when rhd.name is null then \"\" else rhd.name end, " +
				" case when lhd2.name is null then \"\" else \" or \" end, " +
				" case when lhd2.name is null then \"\" else lhd2.name end , " +
				" case when rhd2.name is null then \"\" else \"-\" end, " +
				" case when rhd2.name is null then \"\" else rhd2.name end " +
				" ) bed_name, " +
				" (case " +
				" when (bed.transporter_id is not null and bed.mines_id is not null and bed.grade_id is not null) then 0"+
				" when (bed.transporter_id is not null and bed.mines_id is not null and bed.grade_id is null) then 1"+
				" when (bed.transporter_id is not null and bed.mines_id is null and bed.grade_id is not null) then 2"+
				" when (bed.transporter_id is null and bed.mines_id is not null and bed.grade_id is not null) then 3"+
				" when (bed.transporter_id is not null and bed.mines_id is null and bed.grade_id is null) then 4"+
				" when (bed.transporter_id is null and bed.mines_id is not null and bed.grade_id is null) then 5"+
				" when (bed.transporter_id is  null and bed.mines_id is null and bed.grade_id is not null) then 6 " +
				" else 7 end) weight"+
				" from bed_assignment_details bed join bed_details on (bed.curr_bed_module = bed_details.id) " +
				" left outer join hopper_details lhd on (lhd.id = bed.curr_start_hopper_no) " +
				" left outer join hopper_details rhd on (rhd.id = bed.curr_end_hopper_no)  "+
				" left outer join hopper_details lhd2 on (lhd2.id = bed.hopper_2_start) " +
				" left outer join hopper_details rhd2 on (rhd2.id = bed.hopper_2_end)  "+
				" where (((? is null and bed.transporter_id is null) or bed.transporter_id = ?) and ((? is null and bed.mines_id is null) or bed.mines_id=?) and ((? is null and bed.grade_id is null) or bed.grade_id=?) ) " +
				" and bed.status=1 "+
				//" and curr_start_date <= now() and (curr_end_date is null or curr_end_date >= now())"+
				" order by weight  ";
		try {
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, transporterId, 1);
			Misc.setParamInt(ps, transporterId, 2);
			Misc.setParamInt(ps, mines, 3);
			Misc.setParamInt(ps, mines, 4);
			Misc.setParamInt(ps, grade, 5);
			Misc.setParamInt(ps, grade, 6);
			rs = ps.executeQuery();
			while (rs.next()) {
				if(retval == null)
					retval = new ArrayList<Pair<Integer,String>>();
				retval.add(new Pair<Integer, String>(rs.getInt(1), rs.getString(3)));
				/*bed_Id = rs.getInt(1);
	                bed_name = rs.getString(2);*/
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return retval;
	}
	public static Pair<Integer, String> getBedAllignment(Connection conn, int transporterId,int mines, int grade,int doId) {
		if(Misc.isUndef(transporterId) && Misc.isUndef(mines) && Misc.isUndef(grade))
			return null;
		ArrayList<Pair<Integer, String>> bedList = getBedList(conn, transporterId, mines, grade,doId);
        return bedList != null && bedList.size() > 0 ? bedList.get(0) : new Pair<Integer, String>(Misc.getUndefInt(), "");
    }
	public static int getQuestionResult(Connection conn,int tprId,int questionId){
		int retval = Misc.getUndefInt();
		ArrayList<Object> list = null;
		if(Misc.isUndef(tprId))
			return retval;
		try{
			TPSQuestionDetail question = new TPSQuestionDetail();
			question.setTprId(tprId);
			question.setQuestionId(questionId);
			list = RFIDMasterDao.select(conn, question);
			if(list != null && list.size() > 0){
				question = (TPSQuestionDetail)list.get(0);
				retval = Misc.isUndef(question.getAnswerId()) ? Results.Questions.NC : question.getAnswerId();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static TPStep getCurrentStep(Connection conn,int tprId){
		TPStep retval = null;
		ArrayList<Object> list = null;
		if(Misc.isUndef(tprId))
			return null;
		try{
			retval = new TPStep();
			retval.setTprId(tprId);
			Criteria cr = new Criteria(retval.getClass());
			cr.setDesc(true);
			cr.setOrderByClause("tp_step.in_time");
			list = RFIDMasterDao.select(conn, retval,cr);
			for(int i=0,is=list == null? 0 : list.size();i<is;i++){
				retval = (TPStep) list.get(i);
				if(retval != null && retval.getEntryTime() != null && !Misc.isUndef(retval.getWorkStationType()))
					break;
				else
					retval = null;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static int isVehicleDocumentComplete(Connection conn,int vehicleId, int documentId,long threshold){
		int retval = Results.Questions.NO;
		ArrayList<Object> list = null;
		if(Misc.isUndef(vehicleId))
			return retval;
		try{
			VehicleExtended vehExtended = new VehicleExtended();
			vehExtended.setVehicleId(vehicleId);
			list = RFIDMasterDao.select(conn, vehExtended);
			if(list != null && list.size() > 0){
				vehExtended = (VehicleExtended) list.get(0);
				switch (documentId) {
				case Status.TPRQuestion.isFitnessOk:
					retval = vehExtended.getPermit1_number_expiry() == null ||  (System.currentTimeMillis() - vehExtended.getPermit1_number_expiry().getTime()) > threshold ? Results.Questions.NO : Results.Questions.YES;
					break;
				case Status.TPRQuestion.isRoadPermitOk:
					retval = vehExtended.getPermit2_number_expiry() == null ||  (System.currentTimeMillis() - vehExtended.getPermit2_number_expiry().getTime()) > threshold ? Results.Questions.NO : Results.Questions.YES;
					break;
				case Status.TPRQuestion.isInsuranceOk:
					retval = vehExtended.getInsurance_number_expiry() == null ||  (System.currentTimeMillis() - vehExtended.getInsurance_number_expiry().getTime()) > threshold ? Results.Questions.NO : Results.Questions.YES;
					break;
				case Status.TPRQuestion.isPolutionOk:
					retval = vehExtended.getDate_field1() == null ||  (System.currentTimeMillis() - vehExtended.getDate_field1().getTime()) > threshold ? Results.Questions.NO : Results.Questions.YES;
					break;
				default:
					retval = Results.Questions.NO;
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static Pair<Integer, String> getSupplierFromDo(Connection conn,int dorrId) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		int id = Misc.getUndefInt();
		String name = "";
		try{
			ps = conn.prepareStatement("select supplier_details.id,supplier_details.name from do_rr_details left outer join supplier_details on (supplier_details.id=do_rr_details.seller) where do_rr_details.id=?");
			Misc.setParamInt(ps, dorrId, 1);
			rs = ps.executeQuery();
			if(rs.next()){
				id = Misc.getRsetInt(rs, 1);
				name = rs.getString(2);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(rs != null)
				rs.close();
			if(ps != null)
				ps.close();
		}
		return new Pair<Integer, String>(id, name);
	}
	public static long getChallanDate(Connection conn, int vehicleId, int tprId){
		long retval = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		if(Misc.isUndef(vehicleId) || conn == null)
			return retval;
		try {
			ps = conn.prepareStatement("select challan_date ");//to do
			rs = ps.executeQuery();
			while(rs.next()){
				//to do
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return retval;
	}
	public static Triple<Integer,Long,Long> getDriverBlockStatus(Connection conn, int driverId){
		int status = Misc.getUndefInt();
		long start = Misc.getUndefInt();
		long end = Misc.getUndefInt();
		PreparedStatement ps = null;
		ResultSet rs = null;
		if(Misc.isUndef(driverId) || conn == null)
			return null;
		try {
			ps = conn.prepareStatement("select block_status, block_from, block_to from driver_details where id=?");//to do
			Misc.setParamInt(ps, driverId, 1);
			rs = ps.executeQuery();
			while(rs.next()){
				status = Misc.getRsetInt(rs, "block_status");
				start = Misc.getDateInLong(rs, "block_from");
				end = Misc.getDateInLong(rs, "block_to");
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Triple<Integer, Long, Long>(status, start, end);
	}
	
	
	
}

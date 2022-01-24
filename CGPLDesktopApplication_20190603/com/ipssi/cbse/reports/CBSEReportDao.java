package com.ipssi.cbse.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;

public class CBSEReportDao {
static	SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Logger logger = Logger.getLogger(CBSEReportDao.class);
	public  Triple<String, String, String>  getReportDetail(ArrayList<CBSEStudentBean> stuDataList, CBSEReportBean searchParms,SessionManager session){
		//first = matching
		//second = student w/o sheet
		//third = student with sheet
		PreparedStatement ps = null;
		ResultSet rs = null;
		int matchingStdAns = 0;
		int studentWithoutSheet = 0;
		int sheetWithoutStu = 0;
		int totalStd = 0;
		Connection conn = null;
		  Triple<String, String, String> counts = null;
		try{
			conn = session.getConnection();
			String query = CBSEDBQueries.CBSEDB.GET_STD_DETAIL;

			if ("10".equalsIgnoreCase(searchParms.getClassCode())) {
				query = query.replace("@STD_TABLE", "student_10");
			}else if("12".equalsIgnoreCase(searchParms.getClassCode())){
				query = query.replace("@STD_TABLE", "student_12");
			}else{
				query = query.replace("@STD_TABLE", "student_10");
			}
			
			ps = conn.prepareStatement(query);
			ps.setString(1, searchParms.getCenterCode());
			//ps.setTimestamp(2, new Timestamp(searchParms.getStartDate().getTime()));
			ps.setString(2, searchParms.getExamCode());
			ps.setInt(3, Integer.parseInt(searchParms.getClassCode()));
			rs = ps.executeQuery();
			CBSEStudentBean prev = null;
			while (rs.next()) {
				//GET_STD_DETAIL = "select cenno,cen_name, paper_code, paper_name, Date(exam_date) examDate, data_1, rollno,schno,cname,fname,cs_schname,sch_no, cdi.st_as "+
				//" from cent cross join cbse_exam_schedule ces left outer join cbse_data_info cdi on (cdi.centre_code = cent.cenno and cdi.exam = ces.paper_code and cdi.class=class) left outer join cbse_data cd on (cdi.id = cd.cbse_data_info_id) " +
				//" left outer join @STD_TABLE  on(rollno=cast(data_1 as unsigned) and cenno=cdi.centre_code)  " +
	            //" where cenno= ? and ces.paper_code=? and class=? order by cenno, exam_date, data_1, rollno ";
				
				String rollNo = rs.getInt("data_1")+"";
				if (rollNo == null || rollNo.length() == 0)
					continue;
				if (prev == null || !prev.getRollNo().equals(rollNo))
					prev = null;
				String paperCode = Misc.getParamAsString(rs.getString("paper_code"));

				if (prev != null && !prev.getPaperCode().equals(paperCode))
					prev = null;
				if (prev == null) {
					prev = new CBSEStudentBean();
					stuDataList.add(prev);
					prev.setRollNo(rollNo);
					prev.setPaperCode(paperCode);
					prev.setCenterCode(rs.getString("cenno"));
					prev.setPaperName(rs.getString("paper_name"));
					prev.setCenterName(rs.getString("cen_name"));
					prev.setStudentName(rs.getString("cname"));
					prev.setFatherName(rs.getString("fname"));
					prev.setSchool(rs.getString("cs_schname"));
				}
				int stAs = Misc.getRsetInt(rs, "st_as", 1);
				if (stAs == 0) {
					prev.setHasCopy(true);
				}
				if (stAs == 1) {
					prev.setHasStudent(true);
				}
			}
			
			ps.close();
			rs.close();
		  
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		int match = 0;
		int onlystu = 0;
		int onlyanswer = 0;
		for (CBSEStudentBean bean:stuDataList) {
			 if (bean.isHasCopy() && bean.isHasStudent())
				 match++;
			 if (bean.isHasCopy() && !bean.isHasStudent())
				 onlyanswer++;
			 if (!bean.isHasCopy() && bean.isHasStudent())
				 onlystu++;
		}
		return new Triple<String, String, String>(Integer.toString(match), Integer.toString(onlyanswer), Integer.toString(onlystu));
	}
	public ArrayList<Pair<String,String>> getExamScheduleList(SessionManager session) throws GenericException{
		ArrayList<Pair<String,String>> dataList = new ArrayList<Pair<String,String>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = session.getConnection();
			String query = CBSEDBQueries.CBSEDB.GET_EXAM_SCH;
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
			String paperCode = rs.getString(1).trim();
			String paperName = rs.getString(2).trim();
			Pair<String, String> pair = new Pair<String, String>(paperCode,paperName);
			dataList.add(pair);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dataList;
	}
	public ArrayList<String> getStateList(SessionManager session) throws GenericException{
		ArrayList< String> dataList = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try{
			conn = session.getConnection();
			String query = CBSEDBQueries.CBSEDB.GET_STATE_LIST;
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			 while (rs.next()) {
				 dataList.add(rs.getString(1));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if (ps != null) {
					ps.close();
				}		
				if (rs != null) {
					rs.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dataList;
	}
	public ArrayList<Pair<String, String>> getCentreList(SessionManager session)throws GenericException{
		 ArrayList<Pair<String, String>> dataList = new ArrayList<Pair<String,String>>();
		 PreparedStatement ps = null;
		 ResultSet rs = null;
		 Connection conn = null;
		 try{
			 String query = CBSEDBQueries.CBSEDB.GET_CENTRE_LIST;
			 conn = session.getConnection();
			 ps = conn.prepareStatement(query);
			 rs = ps.executeQuery();
			 while (rs.next()) {
				Pair<String, String> pair = new Pair<String, String>(rs.getString(1).trim(),rs.getString(2).trim());
				dataList.add(pair);
				
			}
			 
		 }catch (Exception e) {
	       e.printStackTrace();
		}finally{
	       try {
				if (ps != null) {

					ps.close();

				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		 return dataList;
	}
	public ArrayList<CBSEReportBean> getCBSEReport(SessionManager session,CBSEReportBean searchParms) throws GenericException {
		
		String query = CBSEDBQueries.CBSEDB.GET_REPORT_DATA;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		String whereClause = "";
		ArrayList<CBSEReportBean> cbseReport = null;
		try {
			if(searchParms != null)
			{
				if(searchParms.getClassCode() != null){
					if(whereClause !=  null && whereClause.length() > 0)
					whereClause += " and ";
				whereClause += "ces.class = '"+searchParms.getClassCode()+"' ";
				}
				if(searchParms.getStartDate() != null){
					if(whereClause !=  null && whereClause.length() > 0)
					whereClause += " and ";
				whereClause += "DATE(ces.exam_date) >= '"+indepDateFormat.format(searchParms.getStartDate())+"' ";
				}
				if(searchParms.getEndDate() != null){
					if(whereClause !=  null && whereClause.length() > 0)
						whereClause += " and ";
					whereClause += "DATE(ces.exam_date) <= '"+indepDateFormat.format(searchParms.getEndDate())+"' ";
				}
				if(searchParms.getCenterCode() != null && !"-1".equalsIgnoreCase(searchParms.getCenterCode()) && searchParms.getCenterCode().length() > 0){
					if(whereClause !=  null && whereClause.length() > 0)
						whereClause += " and ";
					whereClause += "cent.cenno like '%"+searchParms.getCenterCode()+"%' ";
				}
				if(searchParms.getExamCode() != null && !"-1".equalsIgnoreCase(searchParms.getExamCode()) && searchParms.getExamCode().length() > 0){
					if(whereClause !=  null && whereClause.length() > 0)
						whereClause += " and ";
					whereClause += "ces.paper_code like '%"+searchParms.getExamCode()+"%' ";
				}
				if(searchParms.getState() != null && !"-1".equalsIgnoreCase(searchParms.getState()) && searchParms.getState().length() > 0){
					if(whereClause !=  null && whereClause.length() > 0)
						whereClause += " and ";
					whereClause += "cent.calpha like '%"+searchParms.getState()+"%' ";
				}
			}
			if(whereClause != null && whereClause.length() > 0)
				query += " where "+whereClause;
			
			if ("10".equalsIgnoreCase(searchParms.getClassCode())) {
				query = query.replace("@STD_TABLE", "student_10");
			}else if("12".equalsIgnoreCase(searchParms.getClassCode())){
				query = query.replace("@STD_TABLE", "student_12");
			}else{
				query = query.replace("@STD_TABLE", "student_10");
			}
			//"select cent.cenno, cent.cen_name, cent.calpha, ces.class, ces.paper_code, ces.paper_name, date(ces.exam_date) date_dd, vi.registered_student, sum(case when st_as=0 and length(data_1) >0  then 1 else 0 end) ansScan,sum(case when st_as =1 and length(data_1) >0  then 1 else 0 end) studentScan "+
			String orderby = "  group by cent.cenno, cent.cen_name, cent.calpha, ces.class, ces.paper_code, ces.paper_name, date(ces.exam_date), vi.registered_student order by cent.cenno, ces.exam_date, ces.class desc, ces.paper_name ";
			//where centre_code='3736' and class=12 and calpha like '%nl%'  group by DATE(exam_date),centre_code order by calpha,centre_code,DATE(exam_date);
			conn = session.getConnection();
			ps = conn.prepareStatement(query+orderby);
			System.out.println("CBSEReportDao.getCBSEReport() $$Query="+ps);
			rs = ps.executeQuery();
			if (!Common.isNull(rs)) {
				cbseReport = new ArrayList<CBSEReportBean>();
				CBSEReportBean reportBean = null;
				while (rs.next()) {
					reportBean = new CBSEReportBean();
					reportBean.setStartDate(Misc.getDate(rs, "date_dd"));
					reportBean.setCenterCode(rs.getString("cenno"));
					reportBean.setCentre(rs.getString( "cen_name"));
					reportBean.setExamCode(rs.getString("paper_code"));
					reportBean.setExamName(rs.getString("paper_name"));
					reportBean.setState(rs.getString("calpha"));
					reportBean.setClassCode(rs.getString("class"));
					reportBean.setRegisteredStudent(Misc.getRsetInt(rs,"registered_student"));
					reportBean.setScanStudent(Misc.getRsetInt(rs,"studentScan"));
					reportBean.setScanAnswersheet(Misc.getRsetInt(rs,"ansScan"));
					
					cbseReport.add(reportBean);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException sqlEx) {
			try {
				sqlEx.printStackTrace();
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new GenericException(ex);
		}
		return cbseReport;

	}
}

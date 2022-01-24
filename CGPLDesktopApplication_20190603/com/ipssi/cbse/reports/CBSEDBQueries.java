package com.ipssi.cbse.reports;

public class CBSEDBQueries {
	public static class CBSEDB {

		public static final String GET_REPORT_DATA;
		public static final String GET_CENTRE_LIST;
		public static final String GET_STATE_LIST;
		public static final String GET_EXAM_SCH;
		public static final String GET_STD_DETAIL;
		public static final String GET_ANS_DETAIL;
		static {
			//GET_REPORT_DATA = "select DATE(exam_date) date_dd,cen_name,exam,calpha,(select count(cenno) from @STD_TABLE where cenno=centre_code) registered_student, sum(case when st_as=0 and length(data_1) >0  then 1 else 0 end) ansScan,sum(case when st_as =1 and length(data_2) >0  then 1 else 0 end) studentScan,centre_code from cbse_data_info left outer join cbse_data on (cbse_data_info_id=cbse_data_info.id) left outer join cent on(centre_code=cenno)";
			//GET_REPORT_DATA = "select DATE(cbse_exam_schedule.exam_date) date_dd,cen_name,cbse_exam_schedule.paper_name,calpha,vi.registered_student, sum(case when st_as=0 and length(data_1) >0  then 1 else 0 end) ansScan,sum(case when st_as =1 and length(data_1) >0  then 1 else 0 end) studentScan, cdi.centre_code,  from  cbse_data_info cdi join cbse_data cd on (cdi.id = cd.cbse_data_info_id) left outer join cent on(cdi.centre_code=cenno) left outer join cbse_exam_schedule on (cbse_exam_schedule.class= cdi.class and cbse_exam_schedule.paper_code = cdi.exam) left outer join  (select count(cenno) registered_student,cenno from @STD_TABLE group by cenno) vi on (vi.cenno=cdi.centre_code)";
//			GET_REPORT_DATA = "select cent.cenno, cent.cen_name, cent.calpha, ces.class, ces.paper_code, ces.paper_name, date(ces.exam_date) date_dd, vi.registered_student, sum(case when cdi.st_as=0 and length(data_1) >0  then 1 else 0 end) ansScan,sum(case when cdi.st_as <> 0 and length(data_1) >0  then 1 else 0 end) studentScan "+  
//					"  from  cent cross join cbse_exam_schedule ces left outer join cbse_data_info cdi on (cdi.centre_code = cent.cenno and cdi.exam = ces.paper_code and cdi.class=ces.class) left outer join cbse_data cd on (cdi.id = cd.cbse_data_info_id)  left outer join  (select count(cenno) registered_student,cenno from @STD_TABLE group by cenno) vi on (vi.cenno=cent.cenno) ";
			GET_REPORT_DATA = "select cent.cenno, cent.cen_name, cent.calpha, ces.class, ces.paper_code, ces.paper_name, date(ces.exam_date) date_dd, vi.registered_student, sum(case when cdi.st_as=0 and length(data_1) >0  then 1 else 0 end) ansScan,sum(case when cdi.st_as <> 0 and length(data_1) >0  then 1 else 0 end) studentScan "+  
			" from  cbse_data_info cdi join cent on (cent.cenno = cdi.centre_code) "+
			" join cbse_exam_schedule ces on (cdi.exam = ces.paper_code and cdi.class = ces.class) "+
			" left outer join cbse_data cd on (cdi.id = cd.cbse_data_info_id)	"+
			" left outer join  (select count(*) registered_student,centre_code, exam_code, class from student_subj group by centre_code, exam_code, class) vi on (vi.centre_code=cent.cenno and vi.class=ces.class and vi.exam_code=ces.paper_code) ";

			  

			GET_CENTRE_LIST = "select  cenno,cen_name from cent order by cen_name";
			GET_STATE_LIST = "select distinct calpha from cent order by calpha";
			GET_EXAM_SCH = "select paper_code,paper_name from cbse_exam_schedule group by paper_code,paper_name order by paper_name";
			GET_STD_DETAIL = "select cent.cenno,cen_name, paper_code, paper_name, Date(ces.exam_date) examDate, data_1, rollno,schno,cname,fname,cs_schname,sch_no, cdi.st_as "+
			" from  cbse_data_info cdi join cent on (cent.cenno = cdi.centre_code) "+
			" join cbse_exam_schedule ces on (cdi.exam = ces.paper_code and cdi.class = ces.class) "+
			" left outer join cbse_data cd on (cdi.id = cd.cbse_data_info_id)	"+

//			" from cent cross join cbse_exam_schedule ces left outer join cbse_data_info cdi on (cdi.centre_code = cent.cenno and cdi.exam = ces.paper_code and cdi.class=ces.class) left outer join cbse_data cd on (cdi.id = cd.cbse_data_info_id) " +
			" left outer join @STD_TABLE  on(rollno=cast(data_1 as unsigned))  " +
            " where cent.cenno= ? and ces.paper_code=? and ces.class=? order by cent.cenno, ces.exam_date, data_1, rollno ";

//			GET_STD_DETAIL = "select centre_code,exam,Date(exam_date) examDate,data_1,data_2,rollno,schno,cname,fname,cs_schname,sch_no from  cbse_data_info cdi join cbse_data "+ 
//            " left outer join @STD_TABLE  on(rollno=cast(data_2 as unsigned) and cenno=centre_code) left outer join cent on (cent.sch_no=schno)"+
//            " where centre_code= ? and st_as=1 and Date(exam_date)=? and class=? order by Date(exam_date),rollno";
            /*GET_STD_DETAIL = "select centre_code,exam,Date(exam_date) examDate,data_1,data_2,rollno,schno,cname,fname,cs_schname,sch_no from cbse_data_info left outer join cbse_data on (cbse_data_info_id=cbse_data_info.id)"+ 
                                 " left outer join @STD_TABLE  on(rollno=cast(data_2 as unsigned) and cenno=centre_code) left outer join cent on (cent.sch_no=schno)"+
                                 " where centre_code= ? and st_as=1 and Date(exam_date)=? and class=? order by Date(exam_date),rollno";*/
           /* GET_ANS_DETAIL = "select data_1,data_2 from cbse_data_info left outer join cbse_data on (cbse_data_info_id=cbse_data_info.id) "+ 
                              " where centre_code = ? and st_as=0 and Date(exam_date)=? and class=? order by Date(exam_date),data_2"; */
            /*GET_ANS_DETAIL ="select centre_code,exam,Date(exam_date) examDate,data_1,data_2,rollno,schno,cname,fname,cs_schname,sch_no from cbse_data_info left outer join cbse_data on (cbse_data_info_id=cbse_data_info.id) "+ 
                            " left outer join @STD_TABLE on(rollno=cast(data_2 as unsigned) and cenno=centre_code) left outer join cent on (cent.sch_no=schno) "+
                            " where centre_code=? and st_as=0 and Date(exam_date)=? and class=? order by Date(exam_date),rollno";*/
			GET_ANS_DETAIL ="select centre_code,exam,Date(exam_date) examDate,data_1,data_2,rollno,schno,cname,fname,cs_schname,sch_no from  cbse_data  "+ 
            " left outer join @STD_TABLE on(rollno=cast(data_2 as unsigned) and cenno=centre_code) left outer join cent on (cent.sch_no=schno) "+
            " where centre_code=? and st_as=0 and Date(exam_date)=? and class=? order by Date(exam_date),rollno";
		}
	}
}

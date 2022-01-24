package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;

public class MplGCVLCCDao {
	public SessionManager m_session = null;

	public MplGCVLCCDao(SessionManager m_session) {
		this.m_session = m_session;
	}
	public MplGCVLCCDao() {
		// TODO Auto-generated constructor stub
	}
	
	Logger logger = Logger.getLogger(MplGCVLCCDao.class);

	public String calculateAvgGCVAndRsMKCal() throws GenericException {
		    int count = 0;
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			
			try {
				conn = m_session.getConnection();
				ps = conn.prepareStatement("truncate mpl_mines_ranking");
				ps.executeUpdate();
				ps = Misc.closePS(ps);
				
				
				conn = m_session.getConnection();
				String sql = "insert into mpl_mines_ranking(supplier_id ,mines_id ,material_grade_id ,transporter_id ,billed_gcv,lcc_value ,gcv_update_date,gcv_cumm_avg ,rsMkcal,status,port_node_id,calculation_time ) select  dlt.supplier_id,dlt.mines_id,dlt.material_grade_id,dlt.transporter_id,dlt.gcv_value, dlt.lcc_value, max(dlt.end_datetime) as gcv_end_datetime , sum(( dlt.gcv_value*dlt.tonnage)/dlt.tonnage ) as CumulativeAvgGCV, ((dlt.lcc_value*1000)/sum(( dlt.gcv_value*dlt.tonnage)/dlt.tonnage )) as RsMkCal,1,2, now() from ( SELECT tp.supplier_id, tp.mines_id,tp.material_grade_id,tp.transporter_id ,gcv.gcv_value,lcc.lcc_value,gcv.end_datetime,dt.label,sum(tp.unload_gross),sum(tp.unload_tare), sum(tp.unload_gross-tp.unload_tare) as tonnage from tp_record tp join mpl_lcc lcc on ( tp.supplier_id=lcc.seller_id and  tp.mines_id=lcc.mines_id and tp.material_grade_id=lcc.grade_id and tp.transporter_id=lcc.transporter_id and  ((tp.latest_unload_wb_out_out between lcc.start_datetime  and lcc.end_datetime) or (tp.latest_unload_wb_out_out >= lcc.start_datetime  and lcc.end_datetime is null)))  join mpl_gcv gcv on ( gcv.seller_id=lcc.seller_id and  gcv.mines_id=lcc.mines_id and gcv.grade_id=lcc.grade_id and gcv.transporter_id=lcc.transporter_id  and (tp.latest_unload_wb_out_out between gcv.start_datetime  and gcv.end_datetime)) join day_table dt on (tp.latest_unload_wb_out_out between dt.start_time and dt.end_time) group by tp.supplier_id, tp.mines_id,tp.material_grade_id,tp.transporter_id,dt.label ) as dlt group by dlt.supplier_id,dlt.mines_id,dlt.material_grade_id,dlt.transporter_id  order by rsMkcal";
				System.out.println(sql);
				ps = conn.prepareStatement(sql);
				ps.executeUpdate();
			} catch (SQLException e) {
				 e.printStackTrace();
				 return "Fail";	
			} catch (Exception e) {
				e.printStackTrace();
				 return "Fail";	
			}finally{
				if (ps != null) {
					Misc.closePS(ps);
				}
				if (rs != null) {
					Misc.closeRS(rs);
				}
			}
		return "Success";	
}
}

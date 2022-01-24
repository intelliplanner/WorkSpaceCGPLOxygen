package com.ipssi.shift;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ipssi.gen.exception.ExceptionMessages;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.reporting.common.db.DBQueries;
 
public class ShiftDao {

	private static Logger logger = Logger.getLogger(ShiftDao.class);

	public static void fetchShift(Connection conn) throws GenericException, SQLException {
		ShiftBean bean = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int index = Misc.getUndefInt(); 
		ArrayList<ShiftBean> addTo = null;
		boolean destroyIt = false;
		boolean toReturnConn = false;
		int pos = 0;
		try {
			if (conn==null) {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				toReturnConn = true;
			}
			int prevPortNode = Misc.getUndefInt();
			ps = conn.prepareStatement(DBQueries.SHIFT.FETCH_SHIFT_SCHEDULE);
			rs = ps.executeQuery();
			while (rs.next()) {
				int portNodeId = rs.getInt("port_node_id");
				if (prevPortNode != portNodeId) {
					addTo = null;
				}
				if (addTo == null) {
					prevPortNode = portNodeId;
					pos = 0;
					addTo =  ShiftInformation.addShiftForPort(portNodeId);			
				}
				bean = new ShiftBean();
				bean.setShiftName(rs.getString("name"));
				bean.setStartHour(rs.getInt("start_hour"));
				bean.setStartMin(rs.getInt("start_min"));
				bean.setStopHour(rs.getInt("stop_hour"));
				bean.setStopMin(rs.getInt("stop_min"));
				bean.setValidity(rs.getDate("valid_start"),rs.getDate("valid_end"));
				bean.setId(rs.getInt("id"));
				double sHours = bean.getStartHour() + (bean.getStartMin()/60.0);
				double eHours = bean.getStopHour() + (bean.getStopMin()/60.0);
				double dur = eHours - sHours;
				if(dur <= 0.0)
					dur += 24;
				bean.setDur(dur);
				addTo.add(pos++,bean);
			}
		}
		catch (SQLException sqlEx) {
			try {
				logger.error(ExceptionMessages.DB_DATA_PROBLEM, sqlEx);
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				logger.error(ExceptionMessages.DB_RET_CONN_PROBLEM, ex);
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			logger.error(ExceptionMessages.DB_DATA_PROBLEM, ex);
			destroyIt = true;
			throw new GenericException(ex);
		}
		finally{
			if(rs!=null)
				rs.close();
			if(ps!=null)
				ps.close();
			if (toReturnConn && conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}

package com.ipssi.grn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.ipssi.gen.utils.Misc;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.customer.CustomerBean;

public class GRNDao {
	
	public int getGRNPostStatus(Connection conn,int grnId) throws GenericException, SQLException {
		String fecthGrnStatus = "select post_status from grns where id=? ";
		PreparedStatement contSt=null;
		int postStatus=Misc.getUndefInt();
		ResultSet rs = null;
		try {
			contSt = conn.prepareStatement(fecthGrnStatus);
			Misc.setParamInt(contSt,grnId,1);
			rs = contSt.executeQuery();
			if (!Common.isNull(rs)) {
				
				while (rs.next()) {
					postStatus=Misc.getRsetInt(rs, "post_status");
				}
			}
			if(rs!=null)
			rs.close();
			if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			throw new GenericException(ex);
		}
		finally{
			if(rs!=null)
				rs.close();
				if(contSt!=null)
				contSt.close();
		}
		return postStatus;

	}


	public boolean updateGRNPostStatus(Connection conn,int grnId,int updateStatus,String notes) throws GenericException, SQLException {
		String fecthGrnStatus = "update grns set post_status=?, notes=? where id=? ";
		PreparedStatement contSt=null;
		 boolean retval= true;
		
		//ResultSet rs = null;
		try {
			contSt = conn.prepareStatement(fecthGrnStatus);
			contSt.setInt(1, updateStatus);
			contSt.setString(2, notes);
			contSt.setInt(3, grnId);
			contSt.execute();
						if(contSt!=null)
			contSt.close();
		} catch (SQLException sqlEx) {
			try {
				retval= false;
				throw new GenericException(sqlEx);
			} catch (Exception ex) {
				throw new GenericException(sqlEx);
			}
		} catch (Exception ex) {
			throw new GenericException(ex);
		}
		finally{
				if(contSt!=null)
				contSt.close();
		}
		return retval;

	}


	
}

package com.ipssi.scan.upload;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.trip.challan.ChallanDefinitionBean;
import com.ipssi.trip.challan.ChallanParamBean;

public class ScanDocumentDao {
	Logger logger = Logger.getLogger(ScanDocumentDao.class);
	public int insertData(Connection conn, String id, FileItem item) throws Exception {
		int tripId = 0;
		int flag = 0;
		boolean destroyit = false;
		PreparedStatement ps=null;
		PreparedStatement psInsOrUpd = null;
		ResultSet rs = null;
		InputStream inps = null;
		try {
			//String fetchScanFields = "select scan1,scan2,scan3,scan4,scan5 from tp_record_ext where tpr_id="+id;
			String fetchScanFields = "select scan1_flag,scan2_flag,scan3_flag,scan4_flag,scan5_flag from tp_record_ext where tpr_id="+id;
			System.out.println(fetchScanFields);
			String sql = "INSERT INTO tp_record_ext (tpr_id, mode, scan1, scan1_flag)" +  "VALUES ( ?, ?, ?, ?)";
			ps = conn.prepareStatement(fetchScanFields);
			rs = ps.executeQuery(fetchScanFields);
			
			if (rs.next()) {
				flag =1;
				System.out.println("Checking Existing Data");
				int s1 = rs.getInt("scan1_flag");
				int s2 = rs.getInt("scan2_flag");
				int s3 = rs.getInt("scan3_flag");
				int s4 = rs.getInt("scan4_flag");
				int s5 = rs.getInt("scan5_flag");
				if (s1 == 0)
				{
					sql = "update tp_record_ext set scan1 = ?, scan1_flag = 1 where tpr_id= ?";
					System.out.println("Set Image: "+sql);
					psInsOrUpd=conn.prepareStatement(sql);
					inps = item.getInputStream();
					psInsOrUpd.setBinaryStream(1, inps, (int) item.getSize());
					psInsOrUpd.setString(2,id);
				}
				else if (s2 == 0)
				{
					sql = "update tp_record_ext set scan2 = ?, scan2_flag = 1 where tpr_id= ?";
					System.out.println("Set Image: "+sql);
					psInsOrUpd=conn.prepareStatement(sql);
					inps = item.getInputStream();
					psInsOrUpd.setBinaryStream(1, inps, (int) item.getSize());	
					psInsOrUpd.setString(2,id);
		        }
				else if (s3 == 0)
				{
					sql = "update tp_record_ext set scan3 = ?, scan3_flag = 1 where tpr_id= ?";
					System.out.println("Set Image: "+sql);
					psInsOrUpd=conn.prepareStatement(sql);
					inps = item.getInputStream();
					psInsOrUpd.setBinaryStream(1, inps, (int) item.getSize());
					psInsOrUpd.setString(2,id);

		        }
				else if (s4 == 0)
				{
					sql = "update tp_record_ext set scan4 = ?, scan4_flag = 1 where tpr_id= ?";
					System.out.println("Set Image: "+sql);
					psInsOrUpd=conn.prepareStatement(sql);
					inps = item.getInputStream();
					psInsOrUpd.setBinaryStream(1, inps, (int) item.getSize());				
					psInsOrUpd.setString(2,id);
				}
				else if (s5 == 0)
				{
					sql = "update tp_record_ext set scan5 = ?, scan5_flag = 1 where tpr_id= ?";
					System.out.println("Set Image: "+sql);
					psInsOrUpd=conn.prepareStatement(sql);
					inps = item.getInputStream();
					psInsOrUpd.setBinaryStream(1, inps, (int) item.getSize());				
					psInsOrUpd.setString(2,id);
				}
				else
				{
					sql = "update tp_record_ext set scan1 = ?, scan1_flag = 1, scan2_flag=0, scan3_flag=0, scan4_flag=0, scan5_flag=0 where tpr_id= ?";
					System.out.println("Set Image: "+sql);
					psInsOrUpd=conn.prepareStatement(sql);
					inps = item.getInputStream();
					psInsOrUpd.setBinaryStream(1, inps, (int) item.getSize());				
					psInsOrUpd.setString(2,id);
				}
			}
			rs.close();
			System.out.println("Existing Data Checked");
			if (flag == 0)
			{
				System.out.println("In Prepare Statement");
				if (psInsOrUpd == null)
					psInsOrUpd = Misc.closePS(psInsOrUpd);
				psInsOrUpd=conn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
				//String sql = "INSERT INTO tp_record_ext (tpr_id, mode, scan1, scan1_flag)" +  "VALUES ( ?, ?, ?, ?)";
				Misc.setParamInt(psInsOrUpd, id, 1);
				Misc.setParamInt(psInsOrUpd, 1, 2);
				inps = item.getInputStream();
				psInsOrUpd.setBinaryStream(3, inps, (int) item.getSize());
				Misc.setParamInt(psInsOrUpd, 1, 4);
			}
			System.out.println (psInsOrUpd.toString());
            int reslt = psInsOrUpd.executeUpdate();
			System.out.println ("result::"+reslt);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		finally{
			ps = Misc.closePS(ps);
			psInsOrUpd = Misc.closePS(psInsOrUpd);
			if (inps != null) {
				try {
					inps.close();
				}
				catch (Exception e4) {
					e4.printStackTrace();
				}
			}
				//DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyit); ... will be closed elsewhere
		}
		return tripId;
	}

}

package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


import com.ipssi.gen.utils.SessionManager;


public class LatLongDisplayDao {
	public SessionManager m_session = null;

	public LatLongDisplayDao(SessionManager m_session) {
		this.m_session = m_session;
	}
	public ArrayList<VehicleInteractionBean> getLatLongList(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<VehicleInteractionBean> dataList = new ArrayList<VehicleInteractionBean>();
		int counter=0;
		try {
			conn = m_session.getConnection();
			StringBuilder query = new StringBuilder("select  vehicle_id,longitude,latitude from temporary_display_latlng");
			ps = conn.prepareStatement(query.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				VehicleInteractionBean bean = new VehicleInteractionBean();
				bean.setVehicleId(rs.getInt(1));
				bean.setLatitude(rs.getDouble(3));
				bean.setLongitude(rs.getDouble(2));
				dataList.add(bean);
				counter++;
				/*if(counter == 100)
					break;*/
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dataList;

	}
	
}

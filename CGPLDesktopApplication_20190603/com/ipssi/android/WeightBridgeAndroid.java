package com.ipssi.android;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextpdf.text.pdf.codec.Base64;

import com.ipssi.gen.utils.Misc;

public class WeightBridgeAndroid {
	public static int insertWeightBridgeData(Connection conn, String vehicle,
			String ref, String time, String gross, String tare) {
		PreparedStatement ps = null;
		int colPos = 1;
		int id = Misc.getUndefInt();
		ResultSet rs = null;
		String INSERT_CHALLAN_DETAIL = "insert into challan_details "
				+ " (vehicle_id, gr_no_ ,challan_date,consignor,consignee) "
				+ " values " + " (?,?,?,?,?)";
		try {

			ps = conn.prepareStatement(INSERT_CHALLAN_DETAIL);
			ps.setInt(colPos++, Misc.getParamAsInt(vehicle));
			ps.setString(colPos++, ref);
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm");
			Date date = dateFormat.parse(time);
			ps.setTimestamp(colPos++, new Timestamp(date.getTime()));
			ps.setString(colPos++, gross);
			ps.setString(colPos, tare);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			id = -111;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return id;
	}

	public static int insertSolidWasteData(Connection conn, String image, String type,
			String lat, String lng) {
		int id = -111;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		int index = 1;
		byte[] imageBytes;
		imageBytes = Base64.decode(image);
		String QUERY = "INSERT INTO solid_waste_management "
				+ "(image,image_type,lat,lng)" + " values (?,?,?,?) ";
		try {
			ps = conn.prepareStatement(QUERY);
			ps.setBytes(index++, imageBytes);
			ps.setInt(index++, Misc.getParamAsInt(type));
			ps.setDouble(index++, Misc.getParamAsDouble(lat));
			ps.setDouble(index++, Misc.getParamAsDouble(lng));
			ps.executeUpdate();
			resultSet = ps.getGeneratedKeys();
			if (resultSet.next()) {
				id = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -111;
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}
	
	public static int insertCitizenRequestData(Connection conn,String img,String name,double lat,double lng,String mobile,String location,String comment,String nature){
		int id = -111;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		int index = 1;
		byte[] imageBytes;
		imageBytes = Base64.decode(img);
		String QUERY = "INSERT INTO citizen_complaints "
				+ "(lat,lng,nature,image,user_name,mobile,location,comment)" + " values (?,?,?,?,?,?,?,?) ";
		try {
			ps = conn.prepareStatement(QUERY);
			ps.setDouble(index++, lat);
			ps.setDouble(index++, lng);
			ps.setString(index++, nature);
			ps.setBytes(index++, imageBytes);
			ps.setString(index++, name);
			ps.setString(index++, mobile);
			ps.setString(index++, location);
			ps.setString(index++, comment);
			ps.executeUpdate();
			resultSet = ps.getGeneratedKeys();
			if (resultSet.next()) {
				id = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -111;
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
				if (ps != null)
					ps.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return id;
	}

}

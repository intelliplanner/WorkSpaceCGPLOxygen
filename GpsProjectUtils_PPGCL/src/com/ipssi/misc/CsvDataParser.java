package com.ipssi.misc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import com.csvreader.CsvReader;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class CsvDataParser {
	
	public static Connection getConn() throws SQLException{
		String connectString = "jdbc:mysql://";
		String user = "root";
		String password = "ebwebw";
		String port = "3306";
		String server = "192.168.1.20";
		String dbName = "ipssi";
		connectString = connectString + server + ":" + port + "/"+ dbName;
		System.out.println(connectString);
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		Connection conn = ((DriverManager.getConnection(connectString, user, password)));
		return conn;
	}
	
	public static void retConn(Connection conn){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String a[]) {
		String filePath = "C:\\Users\\jai\\Desktop\\GpsData\\IPSSI_15thDEC2009\\15-dec-vehicle.csv";
		String query = 
			"insert into vehicledata_current (strvehicleno,dblLon,dblLat,datetime,dblDistance,strRoad_Name,strArea_1,strArea_2,Town_Name,Speed,ID,PlateNo,cust_id,vehicle_id,ServDtTime) "
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement psVehicle = null;  
		try {
			 conn = getConn();
			 conn.setAutoCommit(false);
			ps = conn.prepareStatement(query);
			
			CsvReader csv = new CsvReader(filePath);
			HashMap<String, Integer> vehicle = new HashMap<String, Integer>();
			int start = 88;
			int counter = 0;
			while (csv.readRecord()) {

				if (csv.getColumnCount() < 14) {
					continue;
				} else {
					// insert into vehicledata_current
					// (strvehicleno,dblLon,dblLat,datetime,dblDistance,strRoad_Name,strArea_1,strArea_2,Town_Name,Speed,
					//  ID,PlateNo,cust_id,vehicle_id,ServDtTime)
					// values(?,?,?,?,?,?,?,?,?,?,?,?,?,?);
					String vehidx = csv.get(1);
					if ( vehicle.get(vehidx) == null ){
						vehicle.put(vehidx, start++);

				/*		psVehicle = conn1.prepareStatement("insert into vehicle(customer_id,name) value(?,?)");
						psVehicle.setInt(1, 1);
						psVehicle.setString(2, vehidx);
						psVehicle.executeUpdate();
						ResultSet rs = psVehicle.getGeneratedKeys();
						rs.next();
						vehicle.put(vehidx, rs.getInt(1));
						rs.close();
						psVehicle.close();*/
						
					}
					
					ps.setString(1, vehidx);
					ps.setDouble(2, Misc.getParamAsDouble(csv.get(4)));
					ps.setDouble(3, Misc.getParamAsDouble(csv.get(5)));
					ps.setTimestamp(4, Timestamp.valueOf(csv.get(6))   ); 
					ps.setDouble(5,Misc.getParamAsDouble(csv.get(8)));
					ps.setString(6, csv.get(12));
					ps.setString(7, csv.get(9));
					ps.setString(8, csv.get(10));
					ps.setString(9, csv.get(11));
					ps.setDouble(10, Misc.getParamAsDouble(csv.get(7)) ); //speed
					ps.setInt(11,Misc.getParamAsInt(csv.get(0)));
					ps.setString(12, csv.get(2));
					ps.setInt(13, 1);
					ps.setInt(14,vehicle.get(vehidx));
					ps.setTimestamp(15, Timestamp.valueOf(csv.get(15)));
					
					ps.addBatch();
					System.out.println(csv.getRawRecord());
					
					counter++;
					if ( counter > 5000){
						ps.executeBatch();
						conn.commit();
						ps.clearBatch();
						counter = 0;
						System.out.println("_______________________________________________________________________________________");
						System.out.println("Committed Records");
						
					}
				}
			}
		} catch (Exception e) {
			System.out.println("OHH!! my goodness. we have an exception");
			e.printStackTrace();
		} finally{
			try {
				psVehicle.close();
				ps.close();
				retConn(conn);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

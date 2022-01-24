package com.ipssi.tracker.common.util;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.ipssi.gen.utils.DBConnectionPool;

public class polygon {
	private static final int RADIUS = 6400;
	private static final int MAX_POINT_COUNT = 5;

	private static point convertCartesian(double latitude, double longitude) {
		point p1 = new point(); // p1[][] = new double[1][2];
		p1.setX(RADIUS * (Math.sin(Math.toRadians(latitude))) * (Math.cos(Math.toRadians(longitude))));
		p1.setY(RADIUS * (Math.sin(Math.toRadians(latitude))) * (Math.sin(Math.toRadians(longitude))));
		return p1;
	}

	/**
	 * 
	 * @param xy
	 * @param r
	 * @param len
	 * @return
	 */
	private static double[][] shift(double xy[][], int r, int len) {

		for (int i = r; i < len - 1; i++) {
			xy[i][0] = xy[i + 1][0];
			xy[i][1] = xy[i + 1][1];
		}
		return xy;
	}

	/**
	 * 
	 * @param xy
	 * @return
	 * @throws Exception 
	 */
	private static void maxAreaPolygon(Connection conn, double xy[][]) throws Exception {
		int len = xy.length;
		double maxPolygon[][] = new double[len][2];
		double temp[][] = new double[len][2];
		double area = 0, a1 = 0;
		
		for (int z = 0; z < 20; z++) {
			Random random = new Random();
			len = xy.length;
			for (int i = 0; i < len; i++) {
				temp[i][0] = xy[i][0];
				temp[i][1] = xy[i][1];
			}
			for (int i = 0; len > 0; i++) {
				int r = random.nextInt(len);
				maxPolygon[i][0] = temp[r][0];
				maxPolygon[i][1] = temp[r][1];
				temp = shift(temp, r, len);
				len--;
			}
			try {
				String query = "SELECT Area(GeomFromText(\"Polygon((";
				for (int i = 0; i < maxPolygon.length; i++) {
					query = query + maxPolygon[i][0] + " " + maxPolygon[i][1] + ",";
				}
				query = query + maxPolygon[0][0] + " " + maxPolygon[0][1] + ")";
				query = query + ",(0 0,0 0,0 0,0 0,0 0))\")) a";

				PreparedStatement ps = conn.prepareStatement(query);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
				    a1 = rs.getDouble("a");
				}
				rs.close();
				ps.close();

			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			if (area < a1) {
				area = a1;
				for (int i = 0; i < len; i++) {
					xy[i][0] = maxPolygon[i][0];
					xy[i][1] = maxPolygon[i][1];
				}
			}

		}

	}

	/**
	 * 
	 * @param vehicle_id
	 * @throws Exception 
	 */
	private static void insertRegion(Connection conn, int vehicle_id) throws Exception {

		List<Point> pointList = getPoints(conn, vehicle_id);

		double xyCordinates[][] = new double[MAX_POINT_COUNT][2];

		int rowIndex = 0;
		for (Point point : pointList) {

			if (rowIndex == MAX_POINT_COUNT) {
				rowIndex = 0;
				processPoints(conn, xyCordinates);
			}
			point pointObj = convertCartesian(point.longitude, point.latitude);
			xyCordinates[rowIndex][0] = pointObj.getX();
			xyCordinates[rowIndex][1] = pointObj.getY();
			rowIndex++;

		}
		processPoints(conn, xyCordinates);

	}

	/**
	 * 
	 * @param xyCordinates
	 * @throws Exception 
	 */
	private static void processPoints(Connection conn, double xyCordinates[][]) throws Exception {
		String pointString = "";
		
		maxAreaPolygon(conn, xyCordinates);

		for (int i = 0; i < xyCordinates.length; i++) {
			pointString = pointString + xyCordinates[i][0] + " " + xyCordinates[i][1] + ",";

		}
		if (pointString.endsWith(",")) {
			pointString = pointString.substring(0, pointString.lastIndexOf(","));
			pointString = pointString.substring(0, pointString.lastIndexOf(",")) + "," + xyCordinates[0][0] + " " + xyCordinates[0][1];
		}

		int gKey = insertRegionInDB(conn, pointString);

	}

	/**
	 * 
	 * @param pointString
	 */
	private static int insertRegionInDB(Connection conn, String pointString) throws Exception {
		int key = 0;
		try {
			String query = "insert into regions(short_code, shape) values ((select now()),geomfromtext('polygon((?))'));";
			query = query.replaceFirst("[?]", pointString);
			PreparedStatement ps = conn.prepareStatement(query);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			
			if (rs.next()){
				key = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} 
		return key;
	}

	/**
	 * 
	 * @param vehicle_id
	 * @return
	 * @throws Exception 
	 */
	private static List<Point> getPoints(Connection conn, int vehicle_id) throws Exception {

		List<Point> pointList = new ArrayList<Point>();
		try {

			String query = "select distinct dbllon, dbllat from vehicledata where vehicle_id = ?";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, vehicle_id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Point point = new polygon().new Point();
				point.longitude = rs.getDouble("dbllon");
				point.latitude = rs.getDouble("dbllat");
				pointList.add(point);
			}

			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return pointList;
	}

	private class Point {
		public double longitude;
		public double latitude;;
	}
	static boolean inRegion(Connection conn, int regionId, Point pt) throws Exception {
		boolean result = false;
		try {

			String query = "select MBRContains(shape, GeomFromText(?)) result from regions where id = ?";

			PreparedStatement ps = conn.prepareStatement(query);
			String point = "Point(" + pt.longitude + " " + pt.latitude + ")";
			ps.setString(1, point);
			ps.setInt(2, regionId);
			ResultSet rs = ps.executeQuery();
			rs.next();

			if (rs.getInt("result") > 0) {
				result = true;
			} else {
				result = false;
			}
			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return result;
	}
	/**
	 * 
	 * @param regionId
	 * @param x
	 * @param y
	 * @return
	 * @throws Exception 
	 */
	static boolean inRegion(Connection conn, int regionId, double x, double y) throws Exception {
		boolean result = false;
		try {

			String query = "select MBRContains(shape, GeomFromText(?)) result from regions where id = ?";

			PreparedStatement ps = conn.prepareStatement(query);
			String point = "Point(" + x + " " + y + ")";
			ps.setString(1, point);
			ps.setInt(2, regionId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
	
				if (rs.getInt("result") > 0) {
					result = true;
				} else {
					result = false;
				}
			}
			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return result;
	}

	public static void main(String[] args) {
		// convertCartesian(76.84978, 31.400522);
		try {
			insertRegion(null, 3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param a
	 */
	// public static void main(String a[]) {
	// polygon p = new polygon();
	// double xy[][] = new double[17][2];
	//
	// /*
	// * xy[0][0] = 0; xy[0][1] = 1; xy[1][0] = 0; xy[1][1] = 0; xy[2][0] = 1; xy[2][1] = 0; xy[3][0] = 1; xy[3][1] = 1;
	// */
	// xy[0][0] = 76.849968;
	// xy[0][1] = 31.400403;
	// xy[1][0] = 76.850003;
	// xy[1][1] = 31.400368;
	// xy[2][0] = 76.850013;
	// xy[2][1] = 31.400437;
	// xy[3][0] = 76.850048;
	// xy[3][1] = 31.400437;
	// xy[4][0] = 76.85005;
	// xy[4][1] = 31.400437;
	// xy[5][0] = 76.850068;
	// xy[5][1] = 31.400423;
	// xy[6][0] = 76.850093;
	// xy[6][1] = 31.400448;
	//
	// xy[7][0] = 76.849867;
	// xy[7][1] = 31.400848;
	// xy[8][0] = 76.850748;
	// xy[8][1] = 31.399658;
	// xy[9][0] = 76.84829;
	// xy[9][1] = 31.397668;
	// xy[10][0] = 76.846883;
	// xy[10][1] = 31.395135;
	// xy[11][0] = 76.846632;
	// xy[11][1] = 31.394677;
	// xy[12][0] = 76.846635;
	// xy[12][1] = 31.394657;
	// xy[13][0] = 76.846642;
	// xy[13][1] = 31.394655;
	// xy[14][0] = 76.8466;
	// xy[14][1] = 31.39466;
	// xy[15][0] = 76.847053;
	// xy[15][1] = 31.39558;
	// xy[16][0] = 76.847227;
	// xy[16][1] = 31.395665;
	//
	// /*
	// * 76.849968 31.400403 76.850003 31.400368 76.850013 31.400437 76.850048 31.400437 76.85005 31.400437 76.850068 31.400423 76.850093 31.400448
	// */
	// point q = new point();
	//
	// System.out.println("----In Region " + p.inRegion(3, 1, 1));
	// System.out.println("----In Region " + p.inRegion(3, 6, 6));
	// q = p.convertCartesian(76.849968, 31.400403);
	//
	// System.out.println("----Coordinates " + q.getX() + " " + q.getY());
	//
	// p.maxAreaPolygon(xy);
	// System.out.println("------Region ");
	// for (int i = 0; i < xy.length; i++) {
	// System.out.println(xy[i][0] + " " + xy[i][1]);
	// }
	// }
}

package com.ipssi.segAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.mapguideutils.PrintIdAll;
import com.ipssi.mapguideutils.RTreeUtils;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.vividsolutions.jts.triangulate.*;
import com.vividsolutions.jts.geom.*;
//import gnu.trove.TIntProcedure;

public class Voronoi {
	public static void doVoronoiForBins(Connection conn, int portNodeId) {
		PreparedStatement ps = null;
		try {
			//1. first set geom for all bins to null;
			//2. calculate voronoi
			//3. save
			ps = conn.prepareStatement("update swm_bins  join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (?) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) set voronoi_geom=null");  
			ps.setInt(1, portNodeId);
			ps = Misc.closePS(ps);
			Triple<ArrayList<Integer>, ArrayList<Coordinate>, Envelope> binPointInfo = getBinPoints(conn, portNodeId);
			Geometry voronoiResult = getVoronoi(binPointInfo.second, binPointInfo.third);
			ArrayList<com.ipssi.geometry.Polygon> polygonList = getAlignedVoronoi(binPointInfo.second, voronoiResult);
			ps = conn.prepareStatement("update swm_bins set voronoi_geom = geomfromtext(?) where id=?");
			for (int i=0,is=polygonList.size();i<is;i++) {
				com.ipssi.geometry.Polygon plygn = polygonList.get(i);
				if (plygn == null)
					continue;
				int id = binPointInfo.first.get(i);
				ps.setInt(2, id);
				ps.setString(1, plygn.toWKT());
				ps.executeUpdate();
			}
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
		finally {
			ps = Misc.closePS(ps);
		}
	}
	public static Geometry getVoronoi(ArrayList<Coordinate> ptList, Envelope envp) {
		VoronoiDiagramBuilder vdb = new VoronoiDiagramBuilder();
		vdb.setClipEnvelope(envp);
		vdb.setSites(ptList);
		 GeometryFactory factory = new GeometryFactory();
		return vdb.getDiagram(factory);
	}
	
	public static ArrayList<com.ipssi.geometry.Polygon> getAlignedVoronoi(ArrayList<Coordinate> ptList, Geometry voronoiResult) {
		ArrayList<com.ipssi.geometry.Polygon> retval = new ArrayList<com.ipssi.geometry.Polygon>();
		Properties properties = new Properties();
		properties.put("MinNodeEntries", 5);
		properties.put("MaxNodeEntries", 10);
		RTree rtree = new RTree();
		rtree.init(properties);
		
		
		for (int i=0,is=ptList.size();i<is;i++) {
			retval.add(null);
			Rectangle rectangle = new Rectangle();
			float x = (float) ptList.get(i).x;
			float y = (float) ptList.get(i).y;
			rectangle.set(x,y,x,y); 
			rtree.add(rectangle, i);
		}
		double dist = RTreeUtils.helpConvertKMInDegree(0.1);
		for (int i=0,is = voronoiResult.getNumGeometries(); i<is;i++) {
			Geometry geom = voronoiResult.getGeometryN(i);
			Coordinate coord = (Coordinate) geom.getUserData();
			//in RTree get the n
			List<Integer> arrayOfIds= null;
			PrintIdAll pid=new PrintIdAll();
		//TODO DEBUG13 	rtree.nearest((new com.infomatiq.jsi.Point((float)coord.x,(float)coord.y)), pid,(float)dist);
			arrayOfIds=pid.getArrayOfIds();
			int idx = -1;
			if (arrayOfIds != null) {
				idx = arrayOfIds.get(0);
			}
			if (idx >= 0) {
				com.ipssi.geometry.Polygon plygn = new com.ipssi.geometry.Polygon();
				Coordinate coordinates[] = geom.getCoordinates();
				for (int j=0,js=coordinates.length;j<js;j++) {
					plygn.addPoint(new com.ipssi.geometry.Point(coordinates[j].x, coordinates[j].y));
				}
				retval.set(idx, plygn);
			}
		}
		return retval;
	}
	public static Triple<ArrayList<Integer>, ArrayList<Coordinate>, Envelope> getBinPoints(Connection conn, int portNodeId) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> idList = new ArrayList<Integer>();
		ArrayList<Coordinate> ptList = new ArrayList<Coordinate>();
		Envelope envp = null;
		try {
			ps = conn.prepareStatement(" select swm_bins.id,  landmarks.lowerX, landmarks.lowerY "+
					" from "+
					" swm_bins "+ 
					" join port_nodes leaf on (leaf.id = swm_bins.port_node_id) join port_nodes anc on (anc.id in (?) and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+ 
					" left outer join landmarks on (landmarks.id = swm_bins.landmark_id) "+
					" where swm_bins.status in (1) "
					);
			ps.setInt(1, portNodeId);
			rs = ps.executeQuery();
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			
			while (rs.next()) {
				int id = rs.getInt(1);
				double lon = Misc.getRsetDouble(rs, 2);
				double lat = Misc.getRsetDouble(rs, 3);
				Coordinate pt = new Coordinate();
				pt.x = lon;
				pt.y = lat;
				
				ptList.add(pt);
				idList.add(id);
				if (lon > maxX)
					maxX = lon;
				if (lat > maxY)
					maxY = lat;
				if (lon < minX)
					minX = lon;
				if (lat < minY)
					minY = lat;
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			double delta = RTreeUtils.helpConvertKMInDegree(1);
			Coordinate pt1 = new Coordinate();
			pt1.x = minX-delta;
			pt1.y = minY-delta;
			Coordinate pt2 = new Coordinate();
			pt2.x = maxX+delta;
			pt2.y = maxY+delta;
			envp = new Envelope(pt1,pt2);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			
		}
		return new Triple<ArrayList<Integer>, ArrayList<Coordinate>, Envelope>(idList, ptList, envp);
	}
	
	public static void main(String[] args) {
		//handleClusterCall();
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int portNodeId = 750;	
			Voronoi.doVoronoiForBins(conn, 750);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			}
			catch (Exception e) {
				
			}
			}
		}
		
	
}

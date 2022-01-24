package com.ipssi.miningOpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ipssi.RegionTest.RegionTest;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.itextpdf.awt.geom.Point;

public class Route {
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private NewMU ownerMU;
	private int id = Misc.getUndefInt();
     private int loadSite = Misc.getUndefInt();
     private int unloadSite = Misc.getUndefInt();
     private double distance = Misc.getUndefDouble();     
     private int distSrc = SWAG;
     
     public static int DIST_FROM_USER = 100;
     public static int DIST_FROM_GPS_EXACT = 90;
     public static int DIST_FROM_GPS_REG_MATCH = 80;
     public static int SWAG = 20;
     private ArrayList<Pair<Integer, Double>> altUnloadSite = new ArrayList<Pair<Integer, Double>>();
     private ArrayList<Integer> assignedDumpers = new ArrayList<Integer>();
     public boolean noDumpersAssigned() {
    	 try {
    		 this.getReadLock();
    		 return assignedDumpers.isEmpty();
    	 }
    	 catch (Exception e) {
    		 
    	 }
    	 finally {
    		this.releaseReadLock(); 
    	 }
    	 return false;
     }
     public String toString() {
    	 return "Src:"+loadSite+" Dest:"+unloadSite+" dist:"+distance;
     }
     public  void updateDist(Connection conn, double dist, int distSrc) throws Exception {
    	 String q = "update mining_route_def set dist = ?, src_of_dist = ? where id = ?";
    	 String qapp = "update mining_route_def_apprvd set dist = ?, src_of_dist = ? where id = ?";
    	 for (int art=0; art<2; art++) {
    		 PreparedStatement ps = conn.prepareStatement(art == 0? q : qapp);
	    	 Misc.setParamDouble(ps, dist, 1);
	    	 Misc.setParamInt(ps, Misc.isUndef(distSrc) ? SWAG : distSrc, 2);
	    	 ps.setInt(3, this.getId());
	    	 ps.execute();
	    	 ps = Misc.closePS(ps);
    	 }
     }
     public  static Route createRoute(Connection conn, int siteId, int destId, double dist, int distSrc, int portNodeId, NewMU ownerMU) throws Exception {
    	 //create table mining_route_def(id int not null auto_increment, site_id int, dest_id int, dist double, src_of_dist int, port_node_id int, status int, created_by int, created_on timestamp null default null, updated_by int, updated_on timestamp null default null, primary key(id));
	    String q = "insert into mining_route_def(site_id, dest_id, dist, src_of_dist, port_node_id, status, created_on) values(?,?,?,?,?,1,now())";
	    String qapp = "insert into mining_route_def_apprvd(id,site_id, dest_id, dist, src_of_dist, port_node_id, status, created_on) values(?,?,?,?,?,?,1,now())";
	    if (Misc.isUndef(dist)) {
	    	Pair<Double, Integer> distEst = estDist(conn, siteId, destId);
	    	dist = distEst.first;
	    	distSrc = distEst.second;
	    }
	 	PreparedStatement ps = conn.prepareStatement(q);
	 	int colIndex = 1;
	 	ps.setInt(colIndex++, siteId);
	 	ps.setInt(colIndex++, destId);
	 	ps.setDouble(colIndex++, dist);
	 	ps.setInt(colIndex++, distSrc);
	 	ps.setInt(colIndex++, portNodeId);
	 	ps.executeUpdate();
	 	ResultSet rs = ps.getGeneratedKeys();
	 	int id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
	 	rs = Misc.closeRS(rs);
	 	ps = Misc.closePS(ps);
	 	ps = conn.prepareStatement(qapp);
	 	colIndex = 1;
	 	ps.setInt(colIndex++, id);
	 	ps.setInt(colIndex++, siteId);
	 	ps.setInt(colIndex++, destId);
	 	ps.setDouble(colIndex++, dist);
	 	ps.setInt(colIndex++, distSrc);
	 	ps.setInt(colIndex++, portNodeId);
	 	ps.execute();
	 	ps = Misc.closePS(ps);
 		Route retval = new Route(id, ownerMU);
 		retval.setLoadSite(siteId);
 		retval.setUnloadSite(destId);
 		retval.setDistance(dist);
 		retval.setDistSrc(distSrc);
 		return retval;
     }
     public static Pair<Double, Integer> estDist(Connection conn, int siteId, int destId) {
    	 PreparedStatement ps = null;
    	 ResultSet rs = null;
    	 double retval = Misc.getUndefDouble();
    	 int distSrc = Misc.getUndefInt();
    	 try {
    		 //get lat/long
    		 ps = conn.prepareStatement("select dist, src_of_dist  from mining_route_def where site_id=? and dest_id=?  order by id desc");
    		 ps.setInt(1, siteId);
    		 ps.setInt(2, destId);
    		 rs = ps.executeQuery();
    		 if (rs.next()) {
    			 retval = Misc.getRsetDouble(rs, 1);
    			 
    			 distSrc = Misc.getRsetInt(rs, 2);
    			 if (retval < 0.0005) {
    				 retval = Misc.getUndefDouble();
    				 distSrc = SWAG;
    			 }
    		 }
    		 rs = Misc.closeRS(rs);
    		 ps = Misc.closePS(ps);
    		 
    		 if (!Misc.isUndef(retval) && distSrc != SWAG)
    			 return new Pair<Double, Integer>(retval, distSrc);
    		 //check if trips existing ...
    		 int siteRegId = Misc.getUndefInt();
    		 int destRegId = Misc.getUndefInt();
    		 double siteLon = Misc.getUndefDouble();
    		 double siteLat = Misc.getUndefDouble();
    		 double destLon = Misc.getUndefDouble();
    		 double destLat = Misc.getUndefDouble();
    		 ps = conn.prepareStatement("select ip.id, iplm.lowerX, iplm.lowerY, ip.region_id from inventory_piles ip left outer join landmarks iplm on (iplm.id = ip.landmark_id) where ip.id in (?,?)");
    		 ps.setInt(1, siteId);
    		 ps.setInt(2, destId);
    		 rs = ps.executeQuery();
    		 while (rs.next()) {
    			int ipId = rs.getInt(1);
    			double lon = Misc.getRsetDouble(rs, 2);
    			double lat = Misc.getRsetDouble(rs, 3);
    			int regId = Misc.getRsetInt(rs, 4);
    			if (ipId == siteId) {
    				siteRegId = regId;
    				siteLon = lon;
    				siteLat = lat;
    			}
    			else {
    				destRegId = regId;
    				destLon = lon;
    				destLat = lat;
    			}
    		 }
    		 rs = Misc.closeRS(rs);
    		 ps = Misc.closePS(ps);
    		 //1st get from dir match
    		 ps = conn.prepareStatement("select avg(ldy.attribute_value-ldx.attribute_value)*1.05 from trip_info join logged_data ldy on (ldy.vehicle_id = trip_info.vehicle_id and ldy.attribute_id=0 and ldy.gps_record_time = trip_info.unload_gate_in) join logged_data ldx on (ldx.vehicle_id = trip_info.vehicle_id and ldx.attribute_id=0 and ldx.gps_record_time = trip_info.load_gate_in) left outer join op_station lop on (lop.id = load_gate_op) left outer join op_station uop on (uop.id = unload_gate_op) where (trip_info.mu_site_id = ? or lop.gate_reg_id = ?) and (trip_info.mu_dest_id = ? or uop.gate_reg_id = ?) order by combo_start desc limit 10");
    		 ps.setInt(1, siteId);
    		 ps.setInt(2, siteRegId);
    		 ps.setInt(3, destId);
    		 ps.setInt(4, destRegId);
    		 rs = ps.executeQuery();
    		 if (rs.next()) {
    			 retval = Misc.getRsetDouble(rs, 1);
    			 distSrc = Route.DIST_FROM_GPS_EXACT;
    		 }
    		 rs = Misc.closeRS(rs);
    		 ps = Misc.closePS(ps);
    		 if (!Misc.isUndef(retval) && retval > 0.0005 && distSrc != SWAG)
    			 return new Pair<Double, Integer>(retval, distSrc);
    		 
    		 //try to get from nearness match
    		 if (!Misc.isUndef(siteRegId)) {
    			 RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(siteRegId, conn);
    			 if (rth != null) {
    				 siteLon = rth.region.getCenter().getX();
    				 siteLat = rth.region.getCenter().getY();
    			 }
    		 }
    		 
    		 if (!Misc.isUndef(destRegId)) {
    			 RegionTest.RegionTestHelper rth = RegionTest.getRegionInfo(siteRegId, conn);
    			 if (rth != null) {
    				 destLon = rth.region.getCenter().getX();
    				 destLat = rth.region.getCenter().getY();
    			 }
    		 }
    		 ps = conn.prepareStatement("select avg(ldy.attribute_value-ldx.attribute_value)*1.05 from trip_info join logged_data ldy on (ldy.vehicle_id = trip_info.vehicle_id and ldy.attribute_id=0 and ldy.gps_record_time = trip_info.unload_gate_in) join logged_data ldx on (ldx.vehicle_id = trip_info.vehicle_id and ldx.attribute_id=0 and ldx.gps_record_time = trip_info.load_gate_in) "+
    				 " left outer join op_station lop on (lop.id = load_gate_op) left outer join regions lreg on (lreg.id = lop.gate_reg_id) "+
    				 " left outer join op_station uop on (uop.id = unload_gate_op) left outer join regions ureg on (ureg.id = uop.gate_reg_id) "+
    				 " where (case when lop.gate_reg_id = ? then 0 else get_dist(?,?,(lreg.lowerX+lreg.upperX)/2 , (lreg.lowerY+lreg.upperY)/2)  end) < ? "+
    				 " and (case when uop.gate_reg_id = ? then 0 else get_dist(?,?,(ureg.lowerX+ureg.upperX)/2, (ureg.lowerY+ureg.upperY)/2) end) < ? "+
    				 " order by ((case when lop.gate_reg_id = ? then 0 else get_dist(?,?,(lreg.lowerX+lreg.upperX)/2 , (lreg.lowerY+lreg.upperY)/2)  end)  +  (case when uop.gate_reg_id = ? then 0 else get_dist(?,?,(ureg.lowerX+ureg.upperX)/2, (ureg.lowerY+ureg.upperY)/2) end)/5) asc, combo_start desc limit 30 "
    				 );
    		Misc.setParamInt(ps, siteRegId, 1);
    		Misc.setParamDouble(ps, siteLon, 2);
    		Misc.setParamDouble(ps, siteLat, 3);
    		Misc.setParamDouble(ps, 0.3, 4);
    		Misc.setParamInt(ps, destRegId, 5);
    		Misc.setParamDouble(ps, destLon, 6);
    		Misc.setParamDouble(ps, destLat, 7);
    		Misc.setParamDouble(ps, 2, 8);
    		Misc.setParamInt(ps, siteRegId, 9);
    		Misc.setParamDouble(ps, siteLon, 10);
    		Misc.setParamDouble(ps, siteLat, 11);
    		Misc.setParamInt(ps, destRegId, 12);
    		Misc.setParamDouble(ps, destLon, 13);
    		Misc.setParamDouble(ps, destLat, 14);

    		rs = ps.executeQuery();
    		if (rs.next()) {
    			retval = Misc.getRsetDouble(rs, 1);
   			 	distSrc = Route.DIST_FROM_GPS_REG_MATCH;
   		 	}
   		 	rs = Misc.closeRS(rs);
   		 	ps = Misc.closePS(ps);
   		 
		 	double altretval = Misc.isUndef(siteLon) || Misc.isUndef(siteLat) || Misc.isUndef(destLon) || Misc.isUndef(destLat) ? retval : Point.distance(siteLon, siteLat, destLon, destLat)*1.3;
		 	if (Math.abs(altretval-retval) < 0.2) {
		 		
		 	}
		 	else {
		 		retval = altretval;
		 		distSrc = SWAG;
		 	}
		 	if (retval < 0.0005) {
		 		retval = 5.5;
		 		distSrc = SWAG;
		 	}
   		 	return new Pair<Double, Integer>(retval, distSrc);
   		 	
    	 }
    	 catch (Exception e) {
    		 e.printStackTrace();
    		 //eat it
    	 }
    	 finally {
    		 rs = Misc.closeRS(rs);
    		 ps = Misc.closePS(ps);
    	 }
    	 return new Pair<Double, Integer>(4.0, SWAG);
     }
     
     public Route(int id, NewMU ownerMU) {
    	 this.id = id;
    	 this.ownerMU = ownerMU;
     }
	public int getLoadSite() {
		return loadSite;
	}
	public void setLoadSite(int loadSite) {
		this.loadSite = loadSite;
	}
	public int getUnloadSite() {
		return unloadSite;
	}
	public void setUnloadSite(int unloadSite) {
		this.unloadSite = unloadSite;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public ArrayList<Pair<Integer, Double>> getAltUnloadSite() {
		return altUnloadSite;
	}
	public void setAltUnloadSite(ArrayList<Pair<Integer, Double>> altUnloadSite) {
		this.altUnloadSite = altUnloadSite;
	}
	public ArrayList<Integer> getAssignedDumpers() {
		return assignedDumpers;
	}
	public void setAssignedDumpers(ArrayList<Integer> assignedDumpers) {
		this.assignedDumpers = assignedDumpers;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getDistSrc() {
		return distSrc;
	}
	public void setDistSrc(int distSrc) {
		this.distSrc = distSrc;
	}
	public void getReadLock() {
		lock.readLock().lock();
	}
	public void releaseReadLock() {
		lock.readLock().unlock();
	}
	public void getWriteLock() {
		lock.writeLock().lock();
	}
	public void releaseWriteLock() {
		lock.writeLock().unlock();
	}
	public void addIfNotExist(int val) {
		try {
			getWriteLock();
			for (int i=0,is=this.assignedDumpers == null ? 0 : assignedDumpers.size(); i<is; i++)
				if (val == assignedDumpers.get(i).intValue())
					return;
			assignedDumpers.add(val);
		}
		catch (Exception e2) {
			
		}
		finally {
			releaseWriteLock();
		}
	}
	
	public boolean removeVal(int val) {
		try {
			getWriteLock();
		
			for (int i=0,is=this.assignedDumpers == null ? 0 : assignedDumpers.size(); i<is; i++)
				if (val == assignedDumpers.get(i).intValue()) {
					assignedDumpers.remove(i);
					return true;
				}
			return false;
		}
		catch (Exception e) {
			
		}
		finally {
			releaseWriteLock();
		}
		return false;
	}
	
	public String getName(Connection conn, int fromperspectiveOf) {
		Site src =  ownerMU.getLoadSite(conn, this.getLoadSite());
		Site dest =  ownerMU.getUnloadSite(conn, this.getUnloadSite());
		String srcName = src == null ? "N/A" : src.getName();
		String destName = dest == null ? "N/A" : dest.getName();
		if (fromperspectiveOf == 0)
			return destName;
		else if (fromperspectiveOf == 1)
			return srcName;
		else
			return srcName + " TO " + destName;
		
	}
}

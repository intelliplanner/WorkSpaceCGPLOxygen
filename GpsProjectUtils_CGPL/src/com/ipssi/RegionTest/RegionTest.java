package com.ipssi.RegionTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.geometry.*;
import com.ipssi.mapguideutils.RTreeSearch;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.mapguideutils.ReadShapeFile;

import static com.ipssi.gen.utils.DBConnectionPool.*;

public class RegionTest {
	public static class RegionTestHelper {
		public Region region;
		//public Point lastPoint; //not used ... needs sync
		//public boolean lastResult; //not used .. needs sync
		public RegionTestHelper(Region r) {
			this.region = r;
		}
	}
	
//ORIG	public static volatile ConcurrentHashMap<Integer, RegionTestHelper> g_regionInfos = new ConcurrentHashMap<Integer, RegionTestHelper>(5000, 0.75f); //getRegionInfo
	public static Pair<Integer, Region> saveAndGetBoxRegion(Connection conn, Point pt,String name,  int portNodeId, double boxSz, boolean addToCache) throws Exception { //DUPLICATED from LocTracker tripSetup
	    try {
	    	if (pt == null)
	    		pt = new Point(24,60);
	    	com.ipssi.geometry.Region r = com.ipssi.geometry.Region.getLongLatBoxAround(pt, boxSz/1000.0);
	    	 String q = "insert into regions (short_code, shape, port_node_id, lowerX, lowerY, upperX, upperY, equal_to_MBR, status, is_artificial) "+
	            " values (?,geomfromtext(?),?,?,?,?,?,?,?,1)";
	    	 String areaName = name;
	    	 if (areaName == null)
	    		 areaName = "ARTIFICIAL_DYN";
	    	PreparedStatement ps = conn.prepareStatement(q);
	    	//		INSERT_ART_REGION = "insert into regions (short_code, shape, port_node_id, description, lowerX, lowerY, upperX, upperY, equal_to_MBR, status, is_artificial) "+
	    	ps.setString(1, areaName);
	    	ps.setString(2, r.toWKT());
	    	ps.setInt(3, portNodeId);
	    	ps.setDouble(4, r.m_llCoord.getX());
	    	ps.setDouble(5, r.m_llCoord.getY());
	    	ps.setDouble(6, r.m_urCoord.getX());
	    	ps.setDouble(7, r.m_urCoord.getY());
	    	ps.setInt(8, r.m_isMBRSameAsRegion ? 1 : 0);
	    	ps.setInt(9, 1);
	    	ps.execute();
	    	ResultSet rs = ps.getGeneratedKeys();
	    	int retval = Misc.getUndefInt();
	    	if (rs.next()) {
	    		retval = rs.getInt(1);
	    	}
	    	rs.close();
	    	ps.close();
	    	if (addToCache) {
	    		addRegionToCache(retval, r);
	    	}
	    	return new Pair<Integer, Region>(retval, r);
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	throw e;
	    }
	}
	private static void addRegionToCache(int id, Region r) throws Exception {
		try {
			RTreesAndInformation.getWriteLock();
			RTreesAndInformation.addRegion(r, id);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			RTreesAndInformation.releaseWriteLock();	
		}
	}
	public static RegionTestHelper getRegionInfo(int regionId, Connection dbConn) 
    {
		try {
			RTreesAndInformation.getReadLock();
			RegionTestHelper retval = RTreesAndInformation.getRregionDetail(regionId);
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			RTreesAndInformation.releaseReadLock();	
		}
		return null;
    }
	public static Map<Integer, RegionTestHelper> loadRegions(Connection dbConn, ArrayList<Integer> refreshTheseRegions) throws Exception {//refreshTheseRegions == null => refresh ALL!!, otherwise will selectively refresh the regions
		try {
			RTreesAndInformation.getWriteLock();
			ReadShapeFile.loadRegions(dbConn, refreshTheseRegions);
			return RTreesAndInformation.getRegionMap();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			RTreesAndInformation.releaseWriteLock();	
		}
	}
	
	public static boolean PointIn(Connection conn, Point point, Region region) throws Exception{
		boolean result = false;		
		
		try{
//			RegionTestHelper regionTestHelper = getRegionInfo(regionId, conn);
			if (region == null || point == null)
				return false;
			//if (point.equals(regionTestHelper.lastPoint))
			//	return regionTestHelper.lastResult;
			double x = point.getX();
			double y = point.getY();
			double lx = region.m_llCoord.getX();
			double ly = region.m_llCoord.getY();
			double ux = region.m_urCoord.getX();
			double uy = region.m_urCoord.getY();
			
			boolean isInMBR = 
			    ((x >= lx && x <= ux) || Misc.isEqual(x, lx) || Misc.isEqual(x, ux)) 
			    		&& 
			    ((y >= ly && y <= uy) || Misc.isEqual(y, ly) || Misc.isEqual(y, uy))
			    ;
			if (isInMBR) {
				 if (!region.m_isMBRSameAsRegion) {
                     result = GeometryTest.pointInRegion(point, region);			 
				 }
				 else {
					 result = true;
				 }
			}
			//regionTestHelper.lastPoint = point;
			//regionTestHelper.lastResult = result;
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		} 
	}
	
	public static boolean PointIn(Connection conn, Point point, int regionId) throws Exception{
		boolean result = false;		
		
		try{
			RegionTestHelper regionTestHelper = getRegionInfo(regionId, conn);
			if (regionTestHelper == null || point == null)
				return false;
			return PointIn(conn, point, regionTestHelper.region);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void refreshRegionIds(ArrayList<Integer> regionIDs) throws Exception {
		Connection conn = null;
	  	  boolean destroyIt = false;
	  	  try {
	  		  conn = DBConnectionPool.getConnectionFromPoolNonWeb();
	  		  
	  		  loadRegions(conn, regionIDs);
	  	  }
	  	  catch (Exception e){
	  		  destroyIt = true;
	  		  throw e;
	  	  }
	  	  finally {
	  		  try {
	  			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
	  		  }
	  		  catch (Exception e1) {
	  			  e1.printStackTrace();
	  			  //eat it
	  		  }
	  	  }	  	
    }
	public static boolean overlapRegionsCheck(Connection conn, int regId1, int regId2) throws Exception{
		try{
			RegionTestHelper regionTestHelper1 = getRegionInfo(regId1, conn);
			RegionTestHelper regionTestHelper2 = getRegionInfo(regId2, conn);
			if (regionTestHelper1 == null || regionTestHelper2 == null)
				return false;
			ArrayList<Point> pointList = regionTestHelper1.region.points;
			for (int i = 0, is = pointList.size(); i < is; i++) {
				if(PointIn(conn, pointList.get(i), regionTestHelper2.region))
					return true;
			}
			pointList = regionTestHelper2.region.points;
			for (int i = 0, is = pointList.size(); i < is; i++) {
				if(PointIn(conn, pointList.get(i), regionTestHelper1.region))
					return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		} 
		return false;
	}
	/* ORIG
	private static void addRegionToCache(int id, Region r) throws Exception {
		try {
			g_regionInfos.put(id, new RegionTestHelper(r));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static RegionTestHelper getRegionInfo(int regionId, Connection dbConn) throws Exception
    {
        return getRegionInfo(new Integer(regionId), dbConn);
    }
    public static RegionTestHelper getRegionInfo(Integer regionId, Connection dbConn) throws Exception
    {            
        try {
        	RegionTestHelper retval = null;
            Map<Integer, RegionTestHelper> regionInfo = g_regionInfos;
            if (!regionInfo.isEmpty()) 
            {
                retval = regionInfo.get(regionId);
                if (retval != null)
                   return retval;
            }
            //either no data for the vehicle found or the data has not been loaded
            
            ArrayList<Integer> temp = null;
            if (!regionInfo.isEmpty()) 
            {
               temp = new ArrayList<Integer>();
               temp.add(regionId);
            }
            regionInfo = loadRegions(dbConn,temp);
            return regionInfo.get(regionId);          
        }
        catch (Exception e) 
        {
          e.printStackTrace();
          throw e;
        }
    }
    
    synchronized public static Map<Integer, RegionTestHelper> loadRegions(Connection dbConn, ArrayList<Integer> refreshTheseRegions) throws Exception {//refreshTheseRegions == null => refresh ALL!!, otherwise will selectively refresh the regions
       try 
       {
          if (refreshTheseRegions == null)
            g_regionInfos.clear();
          Map<Integer, RegionTestHelper> putInThis = g_regionInfos;            
          //check against java mem model - above can be optimized .. i believe it can't be
          
          for (int i=0,is = refreshTheseRegions == null ? 0 : refreshTheseRegions.size();i<is;i++) 
          {
              putInThis.remove((Integer)refreshTheseRegions.get(i));
          }
          StringBuilder q = new StringBuilder("select id, short_code, astext(shape), " +
          		" cast(lowerx as DECIMAL(9,6)) lowerx, cast(lowery as DECIMAL(9,6)) lowery, cast(upperx as DECIMAL(9,6)) upperx, " +
          		"cast(uppery as DECIMAL(9,6)) uppery, equal_to_mbr, port_node_id from regions where status = 1");
          if (refreshTheseRegions != null && refreshTheseRegions.size() > 0) 
          {
             q.append(" and id in (");
             Misc.convertInListToStr(refreshTheseRegions, q);
             q.append(")");
          }
          PreparedStatement ps = dbConn.prepareStatement(q.toString());
          ResultSet rs = ps.executeQuery();
          
          while (rs.next()) 
          {
        	  int regId = rs.getInt(1);
        	  String name = rs.getString(2);
        	  String shape = rs.getString(3);
        	  double lx = rs.getDouble(4);
        	  double ly = rs.getDouble(5);
        	  double ux = rs.getDouble(6);
        	  double uy = rs.getDouble(7);
        	  boolean eqToMBR = rs.getInt(8) == 1;
        	  int portNodeId = Misc.getRsetInt(rs,9, Misc.G_TOP_LEVEL_PORT);
        	  if (shape == null || shape.length() == 0)
        		  continue;
        	 Region r = new Region(regId, name, shape, new Point (lx, ly), new Point (ux, uy), eqToMBR, portNodeId);
        	 putInThis.put(regId, new RegionTestHelper(r));
          }
          rs.close();
          ps.close();
          
          return g_regionInfos;
       }
       catch (Exception e) 
       {
         e.printStackTrace();
         throw e;
       }         
    }
    
	public static boolean PointIn(Connection conn, Point point, int regionId) throws Exception{
		boolean result = false;		
		
		try{
			RegionTestHelper regionTestHelper = getRegionInfo(regionId, conn);
			if (regionTestHelper == null || point == null)
				return false;
			//if (point.equals(regionTestHelper.lastPoint))
			//	return regionTestHelper.lastResult;
			double x = point.getX();
			double y = point.getY();
			double lx = regionTestHelper.region.m_llCoord.getX();
			double ly = regionTestHelper.region.m_llCoord.getY();
			double ux = regionTestHelper.region.m_urCoord.getX();
			double uy = regionTestHelper.region.m_urCoord.getY();
			
			boolean isInMBR = 
			    ((x >= lx && x <= ux) || Misc.isEqual(x, lx) || Misc.isEqual(x, ux)) 
			    		&& 
			    ((y >= ly && y <= uy) || Misc.isEqual(y, ly) || Misc.isEqual(y, uy))
			    ;
			if (isInMBR) {
				 if (!regionTestHelper.region.m_isMBRSameAsRegion) {
                     result = GeometryTest.pointInRegion(point, regionTestHelper.region);			 
				 }
				 else {
					 result = true;
				 }
			}
			//regionTestHelper.lastPoint = point;
			//regionTestHelper.lastResult = result;
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		} 
	}
	
	// To test point in a region
	
    //made private to prevent use until fixed for moving regions
     */	
	public static HashMap<Integer, Boolean> PointIn(Connection conn, Point point, ArrayList<Integer> regionList) throws SQLException,Exception { //TODO convert to use above approach, otherwise will not work for moving regions
		HashMap<Integer, Boolean> result = new HashMap<Integer, Boolean>();
		try {
		
			String query = "select id,astext(shape) g,MBRContains(shape,GeomFromText(?)) as region, "
					+ " equal_to_MBR from regions where id in (";

			int i = 0;
			for (i = 0; i < regionList.size(); i++) {
				query = query + regionList.get(i) + ",";
			}
			query = query.substring(0, query.length() - 1) + ")";

			PreparedStatement ps = conn.prepareStatement(query);

			ps.setString(1, point.toSqlString());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getInt("region") == 0) {

					result.put(rs.getInt("id"), false);
				} else {

					boolean eqaulToMBR = rs.getBoolean("equal_to_MBR");
					

					if (eqaulToMBR) {
						result.put(rs.getInt("id"), true);
					} else {
						boolean temp = GeometryTest.pointInRegion(point, rs.getString("g"));
						result.put(rs.getInt("id"), temp);

					}
				}
			}
			rs.close();
			ps.close();

		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
	
	//making private to prevent use until the function is fixed for moving regions. Also does not close rs,ps because prematurely returns
	private static long PointIn(Connection conn, Point point, ArrayList<Integer> regionList,boolean firstRegion) throws SQLException,Exception { //TODO convert to use above approache ... otherwise will not work for moving regions
		long retVal = Misc.getUndefInt();
		
		try {
		
			String query = "select id,astext(shape) g,MBRContains(shape,GeomFromText(?)) as region, "
					+ " equal_to_MBR from regions where id in (";

			int i = 0;
			for (i = 0; i < regionList.size(); i++) {
				query = query + regionList.get(i) + ",";
			}
			query = query.substring(0, query.length() - 1) + ")";

			PreparedStatement ps = conn.prepareStatement(query);

			ps.setString(1, point.toSqlString());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getInt("region") == 0) {
					
				} else {
					boolean eqaulToMBR = rs.getBoolean("equal_to_MBR");
					if (eqaulToMBR) {
						//result.put(rs.getInt("id"), true);
						return(rs.getInt("id"));
					} else {
						boolean temp = GeometryTest.pointInRegion(point, rs.getString("g"));
						if ( temp )
							return(rs.getInt("id"));
						//result.put(rs.getInt("id"), temp);

					}
				}

			}
			rs.close();
			ps.close();

		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		return retVal;
	}
	public static ArrayList<RegionTestHelper> getRegionsContaining(Point pt)  {
		//do search
		ArrayList<RegionTestHelper> searchResult = null;
		try {
			searchResult = RTreeSearch.getContainingRegions(pt);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return searchResult;
	}
	
	public static ArrayList<RegionTestHelper> getRegionsContaining(Connection conn, Point pt, int portNodeId, int regionType)  {
			//do search
		ArrayList<RegionTestHelper> searchResult = null;
		try {
			searchResult = RTreeSearch.getContainingRegions(pt);
			boolean doPort = !Misc.isUndef(portNodeId) && (portNodeId != Misc.G_TOP_LEVEL_PORT);
			boolean doType = !Misc.isUndef(regionType);
			if (searchResult != null && searchResult.size() != 0 &&  (doPort || doType )) {
				Cache cache = Cache.getCacheInstance(conn);
				for (int i=0;i<searchResult.size(); i++) {
					RegionTestHelper rt = searchResult.get(i);
					if (doPort && !cache.isAncestorOrg(conn, rt.region.getPortNodeId(), portNodeId)) {
						searchResult.remove(i);
						i--;
						continue;
					}
					if (doType && rt.region.getRegionType() != regionType) {
						searchResult.remove(i);
						i--;
						continue;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return searchResult;
	}
		/*
	public static void refreshRegionIds(ArrayList<Integer> regionIDs) throws Exception {
		Connection conn = null;
	  	  boolean destroyIt = false;
	  	  try {
	  		  conn = DBConnectionPool.getConnectionFromPoolNonWeb();
	  		  loadRegions(conn, regionIDs);
	  	  }
	  	  catch (Exception e){
	  		  destroyIt = true;
	  		  throw e;
	  	  }
	  	  finally {
	  		  try {
	  			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
	  		  }
	  		  catch (Exception e1) {
	  			  e1.printStackTrace();
	  			  //eat it
	  		  }
	  	  }	  	
    }
 	
	
	
	public static void main(String a[]) {
		Point p = new Point(1, 1);
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(89);
		list.add(90);
		boolean result = false ;
		HashMap<Integer, Boolean> hm = new HashMap<Integer, Boolean>();
		try {
			//hm = RegionTest.PointIn(null, p, list);
			result = RegionTest.PointIn(null,  p, 89);
			//System.out.println(" 89 = "  + result);
			result = RegionTest.PointIn(null, p, 90);
			//System.out.println(" 90 = "  +result);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(hm);
		
	}
	*/
}

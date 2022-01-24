package com.ipssi.miningOpt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.ipssi.RegionTest.RegionTest;
import com.ipssi.RegionTest.RegionTest.RegionTestHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.geometry.Point;
import com.ipssi.geometry.Region;
import com.ipssi.mapguideutils.PrintIdAll;

public class InvRTree {
///// Rtree for inventory piles
	//Regions currently are being kept flat
	private static RTree inventoryRTree = null;
	public static ArrayList<Pair<Integer, Boolean>> getAllSitesContaining(Connection conn, Point point, long dt, NewMU newmu, int doOfLoadUnload) throws Exception {//2nd = true if load site
		//doOfLoadUnload == 0 => of load only, 1 =of unload only, else any
		 ArrayList<Pair<Integer, Boolean>> retval = new ArrayList<Pair<Integer, Boolean>>();
		List<Integer> arrayOfIds= null;
		PrintIdAll pid=new PrintIdAll(); 
		RTree rtree=getInventoryRTree();
		float flon = (float)point.getX();
		float flat = (float)point.getY();
		//float delta = 0.000001f;
		//RectRangle rect = new Rectangle(flon-delta, flat-delta, flon+delta, flat+delta);
		Rectangle rect = new Rectangle(flon, flat, flon, flat);
		synchronized (rtree) {
			rtree.intersects(rect, pid);
			arrayOfIds = pid.getArrayOfIds();
		}
		if (arrayOfIds != null) {
			
			for (Integer i:arrayOfIds) {
				Site site = newmu.getLoadSite(conn, i);
				boolean isLoad = true;
				if (site == null) {
					site = newmu.getUnloadSite(conn, i);
					isLoad = false;
				}
				if (site != null) {
					if (!site.isDateValid(dt))
						continue;
					if ((doOfLoadUnload == 0 && !isLoad) || (doOfLoadUnload == 1 && isLoad))
						continue;
					RegionTestHelper rt = site.getRegion();
					if (site.getRegion() == null ||RegionTest.PointIn(conn, point, rt.region)) {
						retval.add(new Pair<Integer, Boolean>(i, isLoad));
					}
					
				}
			}
			return retval;
		}
		//else {
		//	com.infomatiq.jsi.Point p1 = new com.infomatiq.jsi.Point((float)flon,(float)flat);
		//	rtree.nearest(p1, pid, (float) 0.001);
		//	arrayOfIds = pid.getArrayOfIds();
		//	int dbg = 1;
		//	dbg++;
		//}
		pid.cleanArrayOfIds();
		return retval;
	}
	public static RTree getInventoryRTree() {
		if (inventoryRTree == null) {
			Properties properties = new Properties();
			properties.put("MinNodeEntries", 5);
			properties.put("MaxNodeEntries", 10);
			RTree rtree = new RTree();
			rtree.init(properties);
			inventoryRTree = rtree;
		}
		return inventoryRTree;
	}
	public static void clearInventoryRTree(ArrayList<Pair<Region, Integer>> refreshThese) {//assumes sync is done outside
		if (refreshThese == null || inventoryRTree == null) {
			System.out.println("[TRACE] Clearing all of regionTree and regionInfos");
			inventoryRTree = null;
			getInventoryRTree();
		}
		else {
			for (Pair<Region, Integer> rt :refreshThese) {
				Rectangle rectangle = new Rectangle();
				rectangle.set((float)rt.first.m_llCoord.getX(), (float)rt.first.m_llCoord.getY(), (float)rt.first.m_urCoord.getX(), (float)rt.first.m_urCoord.getY());
				synchronized (inventoryRTree) {
					inventoryRTree.delete(rectangle, rt.second);
				}
			}
		}
	}
	
	public static void addInventoryRegion(Region r, int id) {//sync done outside
		Rectangle rectangle = new Rectangle();
		rectangle.set((float)r.m_llCoord.getX(), (float)r.m_llCoord.getY(), (float)r.m_urCoord.getX(), (float)r.m_urCoord.getY()); 
		RTree rtree = getInventoryRTree();
		synchronized (rtree) {
			rtree.add(rectangle, id);
		}
	}
	//end of fixed Region related Rtree

}

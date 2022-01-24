package com.ipssi.mining;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.miningOpt.*;

public class MUHelper {
	
	
	public static JSONObject getMenuJSON(NewMU newMU, CoreVehicleInfo vehInfo) throws JSONException{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt(vehInfo.getName()+",0", getPlayJSON(newMU, vehInfo));
		jsonObject.putOpt(vehInfo.getName()+",1", getEditJSON(newMU, vehInfo));
		jsonObject.putOpt(vehInfo.getName()+",2", getRefreshJSON(newMU, vehInfo));
				//jsonObject.putOpt("delete", getDeleteJSON(newMU, vehInfo));
		
		// System.out.println("MUHelper.getRouteJSON() [" + "MENU" + "] : " + jsonObject.toString());
		
		return jsonObject;
	}
	/*public static JSONObject getShovelIconInfoJSON(NewMU newMU, int shovelId) throws JSONException{
		JSONObject jsonObject = new JSONObject();
		ShovelInfo shovel = getShovel(newMU, shovelId);
		// set Icon presentation related stuff
		jsonObject.putOpt("icon", shovel.getName());
		jsonObject.putOpt("iconText", shovel.getName());
		jsonObject.putOpt("iconHoverText", shovel.getName());
		jsonObject.putOpt("pophover", shovel.getName());
		jsonObject.putOpt("pane", shovel.getName());
		jsonObject.putOpt("menu", getMenuJSON(newMU, shovel.getId()));
		
		// System.out.println("MUHelper.getShovelIconInfoJSON() [" + shovel.getId() != null ? shovel.getId() : "ShovelInfo" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	*/
	public static JSONObject getDumperIconInfoJSON(Connection conn, NewMU newMU, int dumperId, long now, boolean isLoad) throws Exception{
		
		JSONObject jsonObject = null;  
		DumperInfo dumper = getDumper(newMU, dumperId);
		//boolean loadStatus  = dumper.isTripStatusLikeLoad(dumper.getCurrentLoadStatus());
		int loadStatus = dumper.getCurrentLoadStatus();
//		if(isLoad && dumper.getPercentLegCompleted() > 0.50){
//			return null;
//		}
//		if(!isLoad && dumper.getPercentLegCompleted() <= 0.50){
//			return null;
//		}
		if(!isLoad){
			System.out.print("isLoaded: "+isLoad +"  ,loadStatus: "+dumper.getCurrentLoadStatus());
			if(loadStatus <= DumperInfo.L_ENROUTE && dumper.getPercentLegCompleted() <= 0.50){
				//add Dumper to left Canvas
				jsonObject =  getDumperJson(conn,newMU, now,dumper);
			}else if(loadStatus >= DumperInfo.U_WAIT && dumper.getPercentLegCompleted() > 0.50 ){
				jsonObject = getDumperJson(conn,newMU, now,dumper);
			}
		}else{
			System.out.print("isLoaded"+isLoad +"  ,loadStatus: "+dumper.getCurrentLoadStatus());
			if(loadStatus <= DumperInfo.L_ENROUTE && dumper.getPercentLegCompleted() > 0.50){
				//add Dumper to right Canvas
				jsonObject =  getDumperJson(conn,newMU, now,dumper);
			}else if(loadStatus >= DumperInfo.U_WAIT && dumper.getPercentLegCompleted() <= 0.50){
				jsonObject = getDumperJson(conn,newMU, now,dumper);
			}
		}
		
	return jsonObject;
	
 }
	
	public static JSONObject getDumperJson(Connection conn, NewMU newMU, long now,DumperInfo dumper){
		JSONObject jsonObject = null;
		try{
			DumperDisplay dumperDisplay = new DumperDisplay(dumper, conn, now);

			dumper.getReadLock();
			jsonObject = new JSONObject();
			jsonObject.putOpt("icon", dumperDisplay.getIconHoverText());
			jsonObject.putOpt("imageName", dumperDisplay.getIconName());
			jsonObject.putOpt("iconText", dumperDisplay.getIconName());
			jsonObject.putOpt("iconHoverText", dumperDisplay.getIconHoverText());
			jsonObject.putOpt("pophover", dumperDisplay.getIconPopHoverText());
			jsonObject.putOpt("pane", "RIGHT"); // set orientation
			// virendra:  percentage need to set.
			jsonObject.putOpt("percentLegCompleted", "" + (!Misc.isUndef(dumper.getPercentLegCompleted()) ? dumper.getPercentLegCompleted() * 100 : 0));
			jsonObject.putOpt("blinkSpeed", (dumperDisplay.getIconName() == null ? "" : dumperDisplay.getIconName()).toLowerCase().contains("blink")  == true ? "100" : "0");
			// virendra:  menu need to set
			jsonObject.putOpt("loadStatus",  dumperDisplay.getCurrentLoadStatus()); 
			
			jsonObject.putOpt("menu", getMenuJSON(newMU, dumper));
		}catch (Exception e2) {

			}
			finally {
				dumper.releaseReadLock();
			}
			// System.out.println("MUHelper.getDumperIconInfoJSON() [" + dumper.getId() != null ? dumper.getId() : "dumperInfo" + "] : " + jsonObject.toString());
			return jsonObject;
	}
	
	
	public static JSONObject getIconInfoJSON(NewMU newMU, CoreVehicleInfo vehInfo, boolean isLeft) throws JSONException{
		JSONObject jsonObject = new JSONObject();
		//ShovelInfo shovel = getShovel(newMU, shovelId);
		// set Icon presentation related stuff
		jsonObject.putOpt("icon", vehInfo.getIconName());// may need to swap with below one
		jsonObject.putOpt("iconText", vehInfo.getName());
		jsonObject.putOpt("iconHoverText", vehInfo.getIconHoverText());
		jsonObject.putOpt("pophover", vehInfo.getIconPopHoverText());
		jsonObject.putOpt("pane", isLeft ? "left" : "right");
		jsonObject.putOpt("menu", getMenuJSON(newMU, vehInfo));
		
		// System.out.println("MUHelper.getIconInfoJSON() [" + vehInfo.getId() != null ? vehInfo.getId() : "CoreVehicleInfo" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	public static JSONObject getIconInfoShovelJSON(NewMU newMU, LoadSite loadSite, LoadSiteDisplay loadSiteDisplay, boolean isLeft) throws JSONException{
		JSONObject jsonObject = new JSONObject();
		//ShovelInfo shovel = getShovel(newMU, shovelId);
		// set Icon presentation related stuff
		jsonObject.putOpt("icon", (loadSiteDisplay.getIconName() == null)?"":loadSiteDisplay.getIconName());// may need to swap with below one
		//jsonObject.putOpt("iconText",(loadSiteDisplay.getShovelName() == null)?"":loadSiteDisplay.getShovelName());
		jsonObject.putOpt("iconHoverText", (loadSiteDisplay.getIconHoverText() == null)?"":loadSiteDisplay.getIconHoverText());
		//jsonObject.putOpt("iconRightHoverText", loadSiteDisplay.getRightHoverText());
		//jsonObject.putOpt("popHover", (loadSiteDisplay.getRightHoverText() == null)?"":loadSiteDisplay.getRightHoverText());
		jsonObject.putOpt("pane", "RIGHT"); //jsonObject.putOpt("pane", isLeft ? "left" : "right");
		
		JSONObject jsonObjectmenu = new JSONObject();		
		ArrayList<ShovelDisplay> shovelList= loadSiteDisplay.getDetailedShovelInfo();
		int i=1;
		for (Iterator iterator = shovelList.iterator(); iterator.hasNext();) {
			ShovelDisplay shovelDisplay = (ShovelDisplay) iterator.next();
			int shovelId = shovelDisplay.getShovel().getId();
			String shovelName = shovelDisplay.getShovel().getName();
									
			JSONObject jsonObjectSubMenuItemsList1 = new JSONObject();	
			jsonObjectSubMenuItemsList1.put("name", "Re-Assign");
			jsonObjectSubMenuItemsList1.put("icon", "play");
			jsonObjectSubMenuItemsList1.put("id", ""+shovelId);
			jsonObjectSubMenuItemsList1.put("value",shovelName);
			jsonObjectSubMenuItemsList1.put("url", "");
		
			JSONObject jsonObjectSubMenuItemsList2 = new JSONObject();	
			jsonObjectSubMenuItemsList2.put("name", "Reminder in 10 Min");
			jsonObjectSubMenuItemsList2.put("icon", "cut");
			jsonObjectSubMenuItemsList2.put("id", ""+shovelId);
			jsonObjectSubMenuItemsList2.put("value", shovelName);
			jsonObjectSubMenuItemsList2.put("url", "");
			
			JSONObject jsonObjectSubMenuItemsList3 = new JSONObject();	
			jsonObjectSubMenuItemsList3.put("name", "Dismiss");
			jsonObjectSubMenuItemsList3.put("icon", "edit");
			jsonObjectSubMenuItemsList3.put("id", ""+shovelId);
			jsonObjectSubMenuItemsList3.put("value", shovelName);
			jsonObjectSubMenuItemsList3.put("url", "");						
			
			JSONObject jsonObjectSubMenuItems = new JSONObject();	
			jsonObjectSubMenuItems.put(shovelId+",0", jsonObjectSubMenuItemsList1);
			jsonObjectSubMenuItems.put(shovelId+",1", jsonObjectSubMenuItemsList2);
			jsonObjectSubMenuItems.put(shovelId+",2", jsonObjectSubMenuItemsList3);
						
			JSONObject jsonObjectSubMenu = new JSONObject();
			jsonObjectSubMenu.putOpt("name", shovelDisplay.getShovel().getName());
			jsonObjectSubMenu.putOpt("items", jsonObjectSubMenuItems);
			
			jsonObjectmenu.putOpt("Menu"+i, jsonObjectSubMenu);	
			i++;
		}
		jsonObject.putOpt("menu", jsonObjectmenu);
		jsonObject.putOpt("showelWaiting", ""+(i-1));

		// virendra:  need to change as per ur new action impl
		//jsonObject.putOpt("menu", getMenuJSON(newMU, shovelInfo));
		
		
		// System.out.println("MUHelper.getIconInfoJSON() [" + loadSiteDisplay.getShovelName() != null ? loadSiteDisplay.getShovelName() : "CoreVehicleInfo" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	public static JSONObject getIconInfoArrJSON(Connection conn, NewMU newMU, LoadSite loadSite, boolean isLeft, long now) throws Exception{
		
//		JSONArray retval = new JSONArray();
		JSONObject jsonObject = null;
		LoadSiteDisplay loadDisplay = new LoadSiteDisplay(loadSite, conn, now);
		ArrayList<Integer> assignedShovels = loadSite.getAssignedShovels();
		jsonObject = getIconInfoShovelJSON(newMU, loadSite, loadDisplay, isLeft);
		/*for (Iterator iterator = assignedShovels.iterator(); iterator.hasNext();) {
			Integer shovelId = (Integer) iterator.next();
			CoreVehicleInfo shovel = getShovel(newMU, shovelId);
			jsonObject = getIconInfoShovelJSON(newMU, loadSite, loadDisplay, isLeft);
			if(jsonObject != null) {
				//retval.put(jsonObject);
				break;
			}
		}*/
		
		// System.out.println("MUHelper.getIconInfoArrJSON() [" + loadSite.getId() != null ? loadSite.getId() : "IconInfo" + "] : " + jsonObject.toString());
		//return retval;
		return jsonObject;
	}
	public static JSONArray getDumpersList(Connection conn, NewMU newMU, Route route, long now) throws Exception{
		JSONArray retval = new JSONArray();
		ArrayList<Integer>  dumpersList = route.getAssignedDumpers();
		for (Iterator iterator = dumpersList.iterator(); iterator.hasNext();) {
			Integer dumperId = (Integer) iterator.next();
			JSONObject jsonObject = null;
			jsonObject = getDumperIconInfoJSON(conn, newMU, dumperId, now, false);
			retval.put(jsonObject);
		}
		return retval;	
	}
	public static JSONObject getLoadMinSrcInfoJSON(Connection conn, NewMU newMU, LoadSite site, Route route, long now) throws Exception{
		JSONObject jsonObject = new JSONObject();
		boolean isLoad = true;	
		LoadSiteDisplay loadDisplay = new LoadSiteDisplay(site, conn, now);
				
		jsonObject.putOpt("name", loadDisplay.getShovelName());// need to check what data to be feed like queued vehicles/processed vehicles/avg loading time/material lifted etc.
		jsonObject.putOpt("hoverText", loadDisplay.getIconHoverText());
		jsonObject.putOpt("textStyle", "divRightSource-content");
		jsonObject.putOpt("dumpers", getDumpers(conn, newMU, route, now, isLoad));
		return jsonObject;
	}
	public static JSONObject getMinSrcInfoJSON(Connection conn, NewMU newMU, UnloadSite site, Route route, long now) throws Exception{
		JSONObject jsonObject = new JSONObject();
		boolean isLoad = false;	
		UnloadSiteDisplay unloadDisplay = new UnloadSiteDisplay(site, conn, now);
				
		jsonObject.putOpt("name", site.getName());// need to check what data to be feed like queued vehicles/processed vehicles/avg loading time/material lifted etc.
		jsonObject.putOpt("hoverText", unloadDisplay.getLeftHoverText()); //unloadDisplay.getIconHoverText());
		jsonObject.putOpt("textStyle", "divLeftDestinationNew-content");
		jsonObject.putOpt("dumpers", getDumpers(conn, newMU, route, now, isLoad));
		return jsonObject;
	}
	public static String getLoadDisplayAsLaneId(Connection conn, NewMU newMU, Route route, long now) throws Exception{
		int loadSiteId = route.getLoadSite();
		LoadSite loadSite = getLoadSite(newMU, loadSiteId);
		LoadSiteDisplay loadDisplay = new LoadSiteDisplay(loadSite, conn, now);
		
		return loadDisplay.getShovelName();
	}
	public static String getLoadDisplayLaneId(Connection conn, NewMU newMU, int loadSiteId, long now) throws Exception{
		LoadSite loadSite = getLoadSite(newMU, loadSiteId);
		LoadSiteDisplay loadDisplay = new LoadSiteDisplay(loadSite, conn, now);
		
		return loadDisplay.getShovelName();
	}
	public static JSONArray getSrcInfoJSON(Connection conn, NewMU newMU, LoadSite loadSite, boolean isLeft, long now) throws Exception{
		
		LoadSiteDisplay loadDisplay = new LoadSiteDisplay(loadSite, conn, now);
		
		JSONArray retval = new JSONArray();	
		JSONObject jsonObject = new JSONObject();
		jsonObject.putOpt("name", null == loadDisplay.getFirstLineStatistics() ? "" : loadDisplay.getFirstLineStatistics());// need to check what data to be feed like queued vehicles/processed vehicles/avg loading time/material lifted etc.
		jsonObject.putOpt("hoverText", loadDisplay.getLeftHoverText());
		//jsonObject.putOpt("textStyle", site.getStyleClass());
		jsonObject.putOpt("textStyle", "divLeftSource-content");
		retval.put(jsonObject);

		JSONObject jsonObject1 = new JSONObject();
		jsonObject1.putOpt("name", null == loadDisplay.getSecondLineStatistics() ? "" : loadDisplay.getSecondLineStatistics());// need to check what data to be feed like processed vehicles/loading tqueued vehicles/ime
		jsonObject1.putOpt("hoverText", "");
		jsonObject1.putOpt("textStyle", "divLeftSource-content");
		retval.put(jsonObject1);

		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.putOpt("name", null == loadDisplay.getThirdLineStatistics() ? "" : loadDisplay.getThirdLineStatistics());// need to check what data to be feed like queued vehicles/processed vehicles/loading time
    	jsonObject2.putOpt("hoverText", "");
		jsonObject2.putOpt("textStyle", "divLeftSource-content");
		retval.put(jsonObject2);
		
		//JSONObject jsonObject3 = new JSONObject();
		//jsonObject3.putOpt("name", loadDisplay.getThirdLineStatistics());// need to check what data to be feed like queued vehicles/processed vehicles/loading time
		//jsonObject3.putOpt("hoverText", loadDisplay.getIconHoverText());
		//jsonObject3.putOpt("textStyle", "divLeftSource-content");
		//retval.put(jsonObject3);

		// System.out.println("MUHelper.getSrcInfoJSON() [" + loadDisplay.getShovelName() != null ? loadDisplay.getShovelName() : "SrcInfo" + "] : " + retval.toString());
		return retval;
	}
	public static JSONObject getLoadSiteJSON(Connection conn, NewMU newMU, LoadSite loadSite, boolean isLeft, long now) throws Exception{
		JSONObject jsonObject = new JSONObject();
		
		
		jsonObject.putOpt("loadSiteId", Integer.toString(loadSite.getId()));
		jsonObject.putOpt("iconInformation", getIconInfoArrJSON(conn, newMU, loadSite, isLeft, now));
		jsonObject.putOpt("sourceInformation", getSrcInfoJSON(conn, newMU, loadSite, isLeft, now));
		
		// System.out.println("MUHelper.getLoadSiteJSON() [" + loadSite.getId() != null ? loadSite.getId() : "loadSite" + "] : " + jsonObject.toString());
		
		return jsonObject;
	}
public static JSONArray getUnloadSrcInfoJSON(Connection conn, NewMU newMU, UnloadSite unloadSite, boolean isLeft, long now) throws Exception{
		
		UnloadSiteDisplay unloadDisplay = new UnloadSiteDisplay(unloadSite, conn, now);
		JSONArray retval = new JSONArray();	
		

		JSONObject jsonObject1 = new JSONObject();
		jsonObject1.putOpt("name", (unloadDisplay.getFirstLineStatistics() == null)?"":unloadDisplay.getFirstLineStatistics());// need to check what data to be feed like processed vehicles/loading tqueued vehicles/ime
		jsonObject1.putOpt("hoverText", unloadDisplay.getIconHoverText());
		jsonObject1.putOpt("textStyle","divRightDestinationNew-content");
		retval.put(jsonObject1);

		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.putOpt("name", (unloadDisplay.getSecondLineStatistics() == null)?"":unloadDisplay.getSecondLineStatistics());// need to check what data to be feed like queued vehicles/processed vehicles/loading time
		jsonObject2.putOpt("hoverText", unloadDisplay.getRightHoverText());
		jsonObject2.putOpt("textStyle", "divRightDestinationNew-content");
		retval.put(jsonObject2);
		
		JSONObject jsonObject3 = new JSONObject();
		jsonObject3.putOpt("name", (unloadDisplay.getThirdLineStatistics() == null)?"":unloadDisplay.getThirdLineStatistics());// need to check what data to be feed like queued vehicles/processed vehicles/loading time
		jsonObject3.putOpt("hoverText", unloadDisplay.getRightHoverText());
		jsonObject3.putOpt("textStyle", "divRightDestinationNew-content");
		retval.put(jsonObject3);

		// System.out.println("MUHelper.getSrcInfoJSON() [" + unloadDisplay.getShovelName() != null ? unloadDisplay.getShovelName() : "SrcInfo" + "] : " + retval.toString());
		return retval;
	}
	public static JSONObject getUnloadSiteJSONObject(Connection conn, NewMU newMU, UnloadSite unloadSite, boolean isLeft, long now) throws Exception{
		UnloadSiteDisplay unloadDisplay = new UnloadSiteDisplay(unloadSite, conn, now);
		JSONObject jsonObject = new JSONObject();
		jsonObject.putOpt("name", (unloadSite.getName() == null) ? unloadDisplay.getShovelName():unloadSite.getName());// need to check what data to be feed like queued vehicles/processed vehicles/avg loading time/material lifted etc.
		jsonObject.putOpt("hoverText", unloadDisplay.getLeftHoverText());
		jsonObject.putOpt("textStyle", "divRightDestinationNew-content");
		return jsonObject;
	}
	public static JSONObject getUnloadSiteJSON(Connection conn, NewMU newMU, UnloadSite unloadSite, boolean isLeft, long now) throws Exception{
		JSONObject jsonObject = new JSONObject();
		jsonObject.putOpt("unloadSiteId", getUnloadSiteJSONObject(conn, newMU, unloadSite, isLeft, now));
//		jsonObject.putOpt("iconInformation", getIconInfoArrJSON(newMU, unloadSite, isLeft)); // dont need
		jsonObject.putOpt("destinationInformation", getUnloadSrcInfoJSON(conn, newMU, unloadSite, isLeft, now));
		
// System.out.println("MUHelper.getRouteJSON() [" + unloadSite.getId() != null ? unloadSite.getId() : "unloadSite" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	public static JSONObject getUnloadToLoadJSON(Connection conn, NewMU newMU, Route route, long now) throws Exception{
		JSONObject jsonObject = new JSONObject();
		int loadSiteId = route.getLoadSite();
		int unloadSiteId = route.getUnloadSite();
		//ArrayList<Integer> loadIdList = getLoadList(newMU, unloadSiteId);
		ArrayList<Route> rList = newMU.getAllRouteToDest(conn, unloadSiteId);
		UnloadSite unLoadSite = getUnloadSite(newMU, unloadSiteId);
		JSONArray retval = new JSONArray();	
		for (Iterator iterator = rList.iterator(); iterator.hasNext();) {
			Route rout = (Route) iterator.next();
			LoadSite loadSite = getLoadSite(newMU, rout.getLoadSite());
			if(loadSite != null){
				try{
					loadSite.getReadLock();
					retval.put(getLoadMinSrcInfoJSON(conn, newMU, loadSite, rout, now));
				}catch (Exception e2) {

				}
				finally {
					loadSite.releaseReadLock();
				}

				}
		}
		jsonObject.putOpt("sourceRight", retval);
		try{
			unLoadSite.getReadLock();
			jsonObject.putOpt("destinationRight", getUnloadSiteJSON(conn, newMU, unLoadSite, false, now));
		}catch (Exception e2) {

		}
		finally {
			unLoadSite.releaseReadLock();
		}	
		//System.out.println("MUHelper.getLoadToUnloadJSON() [" + route.getId() != null ? route.getId() : "ROUTE" + "] : " + jsonObject.toString());
		
		return jsonObject;
	}
	public static JSONObject getLoadToUnloadJSON(Connection conn, NewMU newMU, Route route, long now) throws Exception{
		JSONObject jsonObject = new JSONObject();
		int loadSiteId = route.getLoadSite();
		LoadSite loadSite = getLoadSite(newMU, loadSiteId);
		try{
			loadSite.getReadLock();
			jsonObject.putOpt("sourceLeft", getLoadSiteJSON(conn, newMU, loadSite, true, now));
		}catch (Exception e2) {

		}
		finally {
			loadSite.releaseReadLock();
		}
		//ArrayList<Integer> unloadIdList = getUnloadList(newMU, loadSiteId);
		ArrayList<Route> rList = newMU.getAllRouteFromSrc(conn, loadSiteId);
		JSONArray retval = new JSONArray();	
		for (Iterator iterator = rList.iterator(); iterator.hasNext();) {
			Route rout = (Route) iterator.next();
			UnloadSite unLoadSite = getUnloadSite(newMU, rout.getUnloadSite());
			if(unLoadSite != null){
				try{
					unLoadSite.getReadLock();
					retval.put(getMinSrcInfoJSON(conn, newMU, unLoadSite, rout, now));
				}catch (Exception e2) {
					
				}
				finally {
					unLoadSite.releaseReadLock();
				}
			}
		}
		jsonObject.putOpt("destinationLeft", retval);
				
	//	System.out.println("MUHelper.getLoadToUnloadJSON() [" + route.getId() != null ? route.getId() : "ROUTE" + "] : " + jsonObject.toString());
		
		return jsonObject;
	}
	public static JSONArray getDumpers(Connection conn, NewMU newMU, Route route, long now, boolean isLoad) throws Exception{
		JSONArray retval = new JSONArray();
		ArrayList<Integer>  dumpersList = route.getAssignedDumpers();
		for (Iterator iterator = dumpersList.iterator(); iterator.hasNext();) {
			Integer dumperId = (Integer) iterator.next();
			JSONObject jsonObject = null;
			jsonObject = getDumperIconInfoJSON(conn, newMU, dumperId, now, isLoad);
			retval.put(jsonObject);
		}
		return retval;	
	}
	public static JSONObject getRouteJSONOld(Connection conn, NewMU newMU, Route route, long now, int count) throws Exception{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt("id", ""+count);
		jsonObject.putOpt("laneId", getLoadDisplayAsLaneId(conn, newMU, route, now));
		jsonObject.putOpt("loadToUnload", getLoadToUnloadJSON(conn, newMU, route, now));
//		jsonObject.putOpt("vehicle", getDumpers(newMU, route));
		jsonObject.putOpt("unloadToLoad", getUnloadToLoadJSON(conn, newMU, route, now));
		
		// System.out.println("MUHelper.getRouteJSON() [" + route.getId() != null ? route.getId() : "ROUTE" + "] : " + jsonObject.toString());
		
		return jsonObject;
	}
	public static boolean hasSite(ArrayList <Pair<Integer, JSONObject>> jsonObjArr, int id){
		boolean retval = false;
			for (Iterator iterator = jsonObjArr.iterator(); iterator.hasNext();) {
				Pair<Integer, JSONObject> pair = (Pair<Integer, JSONObject>) iterator.next();
				if(pair != null && pair.first != null && pair.first == id){
					retval = true;
					break;
				}
			}
			
		return retval;
	}
	public static void updateRouteLoadUnloadJSONObjects(Connection conn, NewMU newMU, Route route, long now, int count,ArrayList <Pair<Integer, JSONObject>> loadJsonObjArr, ArrayList <Pair<Integer, JSONObject>> unloadJsonObjArr) throws Exception{
		JSONObject jsonObject = new JSONObject();
		int loadSiteId = route.getLoadSite();
		if(!hasSite(loadJsonObjArr, loadSiteId)){
			JSONObject loadJson = getLoadToUnloadJSON(conn, newMU, route, now);
			loadJsonObjArr.add(new Pair<Integer, JSONObject>(loadSiteId,loadJson));
		}
		int unloadSiteId = route.getUnloadSite();
		if(!hasSite(unloadJsonObjArr, unloadSiteId)){
			JSONObject unloadJson = getUnloadToLoadJSON(conn, newMU, route, now);
			unloadJsonObjArr.add(new Pair<Integer, JSONObject>(unloadSiteId,unloadJson));
		}
				
		/*jsonObject.putOpt("laneId", getLoadDisplayAsLaneId(conn, newMU, route, now));
		jsonObject.putOpt("loadToUnload", getLoadToUnloadJSON(conn, newMU, route, now));
//		jsonObject.putOpt("vehicle", getDumpers(newMU, route));
		jsonObject.putOpt("unloadToLoad", getUnloadToLoadJSON(conn, newMU, route, now));
		
		// System.out.println("MUHelper.getRouteJSON() [" + route.getId() != null ? route.getId() : "ROUTE" + "] : " + jsonObject.toString());*/
		
	}
	public static JSONObject getRouteJSON(Connection conn, NewMU newMU, Route route, long now, int count,ArrayList <Pair<Integer, JSONObject>> loadJsonObjArr, ArrayList <Pair<Integer, JSONObject>> unloadJsonObjArr) throws Exception{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt("id", ""+count);
		jsonObject.putOpt("laneId", getLoadDisplayAsLaneId(conn, newMU, route, now));
		jsonObject.putOpt("loadToUnload", getLoadToUnloadJSON(conn, newMU, route, now));
//		jsonObject.putOpt("vehicle", getDumpers(newMU, route));
		jsonObject.putOpt("unloadToLoad", getUnloadToLoadJSON(conn, newMU, route, now));
		
	//	System.out.println("MUHelper.getRouteJSON() [" + route.getId() != null ? route.getId() : "ROUTE" + "] : " + jsonObject.toString());
		
		return jsonObject;
	}
	public static JSONArray getManagementUnit(Connection conn, int portNodeId) throws Exception{
		NewMU newMU = null;
		newMU = NewMU.getManagementUnit(conn, portNodeId);
		System.out.println("MUHelper.getManagementUnit() newMU: " + newMU != null ? newMU.toString(conn, true) : "No Data");
		Collection<Route> routes = newMU.getAllRoutes();
		long now = (new java.util.Date(117,9,10)).getTime();
//		Collection<DumperInfo> dumpers = newMU.getAllDumpers();
		ArrayList <Pair<Integer, JSONObject>> loadJsonObjArr = new ArrayList <Pair<Integer, JSONObject>>();
		ArrayList <Pair<Integer, JSONObject>> unloadJsonObjArr = new ArrayList <Pair<Integer, JSONObject>>();
		if(routes != null && routes.size() > 0){
			int count = 1;
			for (Iterator iterator = routes.iterator(); iterator.hasNext();) {
				Route route = (Route) iterator.next();
				updateRouteLoadUnloadJSONObjects(conn, newMU, route, now, count,loadJsonObjArr,unloadJsonObjArr);
				count++;
			}
		}
		JSONArray retval = new JSONArray();
		if(loadJsonObjArr != null && loadJsonObjArr.size() > 0 && unloadJsonObjArr != null && unloadJsonObjArr.size() > 0){
			int loadSize = loadJsonObjArr.size();
			int unloadSize = unloadJsonObjArr.size();
			int size = loadSize > unloadSize ? loadSize : unloadSize;
			for (int i = 0; i < size; i++) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.putOpt("id", ""+i);
				if(i < loadSize){
					Pair<Integer, JSONObject> loadJsonObj = loadJsonObjArr.get(i);
					jsonObject.putOpt("laneId", getLoadDisplayLaneId(conn, newMU, loadJsonObj.first, now));
					jsonObject.putOpt("loadToUnload", loadJsonObj.second);
				}else{
					jsonObject.putOpt("laneId", "No Load");
					jsonObject.putOpt("loadToUnload", "");
				}
				if(i < unloadSize){
					Pair<Integer, JSONObject> unloadJsonObj = unloadJsonObjArr.get(i);
					jsonObject.putOpt("unloadToLoad", unloadJsonObj.second);
				}else{
					jsonObject.putOpt("unloadToLoad", "");
				}
				
				retval.put(jsonObject);
			}
		}
		return retval;
	}
	
	public static String getManagementUnit(HttpServletRequest request, HttpServletResponse response){
		SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
		int defaultPortNodeId =  Misc.getParamAsInt(session.getParameter("pv123"),816);// 816;//
		System.out.println("new MUHelper.getManagementUnit() defaultPortNodeId: " + defaultPortNodeId);
		JSONArray retval = new JSONArray() ;
		try {
			if(conn == null)
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			retval = getManagementUnit(conn, defaultPortNodeId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("MUHelper.getManagementUnit() JSON: " + retval);
		return retval.toString();
	}
	
	
	public static ArrayList<Integer> getUnloadList(NewMU newMU, int loadId){
		
		ArrayList<Integer> retval = new ArrayList<Integer>();
		Collection<Route> allRoute = newMU.getAllRoutes();
		if(allRoute != null && allRoute.size() > 0){
			for (Iterator iterator = allRoute.iterator(); iterator.hasNext();) {
				Route route = (Route) iterator.next();
				if(loadId == route.getLoadSite() && !Misc.isUndef(route.getUnloadSite()))
					retval.add(route.getUnloadSite());	
			}
		}
		return retval;
	}
	public static ArrayList<Integer> getLoadList(NewMU newMU, int unLoadId){
		
		ArrayList<Integer> retval = new ArrayList<Integer>();
		Collection<Route> allRoute = newMU.getAllRoutes();
		if(allRoute != null && allRoute.size() > 0){
			for (Iterator iterator = allRoute.iterator(); iterator.hasNext();) {
				Route route = (Route) iterator.next();
				if(unLoadId == route.getUnloadSite() && !Misc.isUndef(route.getLoadSite()))
					retval.add(route.getLoadSite());	
			}
		}
		return retval;
	}
	
	public static ShovelInfo getShovel(NewMU newMU, int shovelId){
		Collection<ShovelInfo> allShovels = newMU.getAllShovels();
		for (Iterator iterator = allShovels.iterator(); iterator.hasNext();) {
			ShovelInfo shovelInfo = (ShovelInfo) iterator.next();
			if(shovelInfo != null && shovelId == shovelInfo.getId())
				return shovelInfo;
		}
		return null;
	}
	public static DumperInfo getDumper(NewMU newMU, int dumperId){
		Collection<DumperInfo> allDumperInfos = newMU.getAllDumpers();
		for (Iterator iterator = allDumperInfos.iterator(); iterator.hasNext();) {
			DumperInfo dumperInfo = (DumperInfo) iterator.next();
			if(dumperInfo != null && dumperId == dumperInfo.getId())
				return dumperInfo;
		}
		return null;
	}
	public static LoadSite getLoadSite(NewMU newMU, int loadId){
		Collection<LoadSite> allloadSites = newMU.getAllLoadSites();
		for (Iterator iterator = allloadSites.iterator(); iterator.hasNext();) {
			LoadSite loadSite = (LoadSite) iterator.next();
			if(loadSite != null && loadId == loadSite.getId())
				return loadSite;
		}
		return null;
	}
	public static UnloadSite getUnloadSite(NewMU newMU, int unloadId){
		Collection<UnloadSite> allUnloadSites = newMU.getAllUnloadSites();
		for (Iterator iterator = allUnloadSites.iterator(); iterator.hasNext();) {
			UnloadSite unloadSite = (UnloadSite) iterator.next();
			if(unloadSite != null && unloadId == unloadSite.getId())
				return unloadSite;
		}
		return null;
	}
	public static Pits getPit(NewMU newMU, int pitId){
		Collection<Pits> allPits = newMU.getAllPits();
		for (Iterator iterator = allPits.iterator(); iterator.hasNext();) {
			Pits pits = (Pits) iterator.next();
			if(pits != null && pitId == pits.getId())
				return pits;
		}
		return null;
	}
	public static JSONObject getPlayJSON(NewMU newMU, CoreVehicleInfo vehInfo) throws JSONException{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt("id", "0");
		jsonObject.putOpt("name", "Re-Assign");
		jsonObject.putOpt("icon", "play");
		jsonObject.putOpt("value", vehInfo.getName());
		jsonObject.putOpt("url", "");
		
	//	System.out.println("MUHelper.getPlayJSON() [" +"Play" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	public static JSONObject getEditJSON(NewMU newMU, CoreVehicleInfo vehInfo) throws JSONException{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt("id", "1");
		jsonObject.putOpt("name", "Reminder in 10 Min");
		jsonObject.putOpt("icon", "edit");
		jsonObject.putOpt("value", vehInfo.getName());
		jsonObject.putOpt("url", "");
		
	//	System.out.println("MUHelper.getEditJSON() [" + "Edit" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	public static JSONObject getRefreshJSON(NewMU newMU, CoreVehicleInfo vehInfo) throws JSONException{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt("id", "2");
		jsonObject.putOpt("name", "Dismiss");
		jsonObject.putOpt("icon", "refresh");
		jsonObject.putOpt("value", vehInfo.getName());
		jsonObject.putOpt("url", "");
		
	//	System.out.println("MUHelper.getRefreshJSON() ["+ "Refresh" + "] : " + jsonObject.toString());
		return jsonObject;
	}
	public static JSONObject getDeleteJSON(NewMU newMU, CoreVehicleInfo vehInfo) throws JSONException{
		JSONObject jsonObject = new JSONObject();

		jsonObject.putOpt("id", "4");
		jsonObject.putOpt("name", "Remove Lane");
		jsonObject.putOpt("icon", "delete");
		jsonObject.putOpt("value", "2");
		jsonObject.putOpt("url", "");
		
	//	System.out.println("MUHelper.getDeleteJSON() [" + "Remove Lane" + "] : " + jsonObject.toString());
		return jsonObject;
	}

	public static void dbgMiningCache() {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			NewMU newmu = NewMU.getManagementUnit(conn, 816);
			Collection<LoadSite> allLoads = newmu.getAllLoadSites();
			long now = (new java.util.Date(117,9,10)).getTime();
		//	System.out.println("-------------------------------------------------     Loading Sites       ------------------------------------------------------------");
			for (Iterator<LoadSite> iter = allLoads.iterator(); iter.hasNext();) {
				LoadSite site = iter.next();
				LoadSiteDisplay loadDisplay = new LoadSiteDisplay(site, conn, now);
//				System.out.println("ShovelName:-----\n"+loadDisplay.getShovelName()+"\n---------------");
//				System.out.println("IconName:--------\n"+loadDisplay.getIconName()+"\n---------------");
//				System.out.println("BlinkRate:---------\n"+loadDisplay.getBlinkRate()+"\n---------------");
//				System.out.println("SiteName:---------\n"+loadDisplay.getFirstLineStatistics()+"\n---------------");
//				System.out.println("1st Line:------------\n"+loadDisplay.getSecondLineStatistics()+"\n---------------");
//				System.out.println("2nd Line:------------\n"+loadDisplay.getThirdLineStatistics()+"\n---------------");
//				System.out.println("Mid Hover (On Image or Name):----\n"+loadDisplay.getIconHoverText()+"\n---------------");
//				System.out.println("Left Hover:(On stat text)---------\n"+loadDisplay.getLeftHoverText()+"\n---------------");
//				System.out.println("Right Hover:(On or in any vehicle in Q---------\n"+loadDisplay.getRightHoverText()+"\n---------------");
	//			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
			}
		//	System.out.println("-----------------------------------------------     Unloading Sites       ------------------------------------------------------------");
			Collection<UnloadSite> allUnloadSites = newmu.getAllUnloadSites();
			for (Iterator<UnloadSite> iter = allUnloadSites.iterator(); iter.hasNext();) {
				UnloadSite site = iter.next();
				UnloadSiteDisplay loadDisplay = new UnloadSiteDisplay(site, conn, now);
//				System.out.println("Name:-----\n"+loadDisplay.getShovelName()+"\n---------------");
//				System.out.println("IconName:--------\n"+loadDisplay.getIconName()+"\n---------------");
//				System.out.println("BlinkRate:---------\n"+loadDisplay.getBlinkRate()+"\n---------------");
//				System.out.println("1st Line:---------\n"+loadDisplay.getFirstLineStatistics()+"\n---------------");
//				System.out.println("2nd Line:------------\n"+loadDisplay.getSecondLineStatistics()+"\n---------------");
//				System.out.println("3rd Line: Not used------------\n"+loadDisplay.getThirdLineStatistics()+"\n---------------");
//				System.out.println("Mid Hover: on icon or name ---------\n"+loadDisplay.getIconHoverText()+"\n---------------");
//				System.out.println("Left Hover: on stat box---------\n"+loadDisplay.getLeftHoverText()+"\n---------------");
//				System.out.println("Right Hover: on vehicles or Q box---------\n"+loadDisplay.getRightHoverText()+"\n---------------");
//				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
			}
	//		System.out.println("-------------------------------------------------     Dumpers       ------------------------------------------------------------");
			Collection<DumperInfo> allDumpers = newmu.getAllDumpers();
			for (Iterator<DumperInfo> iter = allDumpers.iterator(); iter.hasNext();) {
				DumperInfo site = iter.next();
				DumperDisplay loadDisplay = new DumperDisplay(site, conn, now);
//				System.out.println("Name:-----\n"+loadDisplay.getIconHoverText()+"\n---------------");
//				System.out.println("IconName:--------\n"+loadDisplay.getIconName()+"\n---------------");
//				System.out.println("BlinkRate:---------\n"+loadDisplay.getBlinkRate()+"\n---------------");
//				System.out.println("Hover Text:---------\n"+loadDisplay.getIconPopHoverText()+"\n---------------");
//				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
				}
				catch (Exception e) {
					
				}
			}
		}
	}
	public static String getJsonDataList(HttpServletRequest request, HttpServletResponse response){
		SessionManager session = InitHelper.helpGetSession(request);
		Connection conn = InitHelper.helpGetDBConn(request);
		int defaultPortNodeId =  Misc.getParamAsInt(session.getParameter("pv123"),816);// 816;//
		System.out.println("new MUHelper.getShovelList() defaultPortNodeId: " + defaultPortNodeId);
		JSONArray retval = null;
		
		JSONObject jsonData = new JSONObject() ;
		try {
			if(conn == null)
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			retval = getShowelList();				
			jsonData.put("ShowelList", retval);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("MUHelper.getShovelList() JSON: " + retval);
		return jsonData.toString();
	}
	public static JSONArray getShowelList() {
		JSONArray retval = new JSONArray();
		JSONObject jsonObj = null;
		int i = 1;
		try {
			while (i < 10) {
				jsonObj = new JSONObject();
				jsonObj.put("id", i + "");
				jsonObj.put("name", "showel" + i);
				retval.put(jsonObj);
				i++;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
	public static void main(String[] args) {
		
//		System.out.println(getShovelList(null,null));
	
	}	
}

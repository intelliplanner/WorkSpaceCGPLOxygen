package com.ipssi.reporting.cache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimCalc;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.reporting.customize.CustomizeDao;
import com.ipssi.reporting.customize.MenuBean;
import com.ipssi.reporting.customize.ReportDetailVO;
import com.ipssi.reporting.customize.UIColumnBean;
import com.ipssi.reporting.customize.UIParameterBean;

public class CacheManager {
	
	private static Map<String, Map<String, FrontPageInfo>> portConfigCache = null;
	private static Map<String, Map<String, FrontPageInfo>> userConfigCache = null;
	private static Map<String, FrontPageInfo> configCache = null;
	static{
		portConfigCache = new ConcurrentHashMap<String, Map<String, FrontPageInfo>>();
		userConfigCache = new ConcurrentHashMap<String, Map<String, FrontPageInfo>>();
		configCache = new ConcurrentHashMap<String, FrontPageInfo>();
	}
	public static void makeAllDirty() {
		configCache.clear();
		portConfigCache.clear();
		userConfigCache.clear();
	}
	public static void makeUserConfigDirty(int userId, String menuTag, String configFile, int row, int column) {
		StringBuilder uKey = new StringBuilder();
		uKey.append(userId).append("_").append(menuTag).append("_").append(configFile).append("_").append(row).append("_").append(column);
		if (userConfigCache != null)
			userConfigCache.remove(uKey.toString());
	}
	
	public static void makePortConfigDirty(int portNodeId, String menuTag, String configFile, int row, int column) {
		StringBuilder uKey = new StringBuilder();
		uKey.append(portNodeId).append("_").append(menuTag).append("_").append(configFile).append("_").append(row).append("_").append(column);
		if(portConfigCache != null){
			portConfigCache.remove(uKey.toString());
		}
	}
	
	private static FrontPageInfo getUserConfig(Connection conn, int userId, String menuTag, String configFile, int row, int column) throws Exception{
		StringBuilder uKey = new StringBuilder();
		uKey.append(userId).append("_").append(menuTag).append("_").append(configFile).append("_").append(row).append("_").append(column);
		FrontPageInfo frontPageInfo = null;
		Map<String, FrontPageInfo> userConfigMap = null;
		if(userConfigCache != null){
			userConfigMap = userConfigCache.get(uKey.toString());
			if(userConfigMap != null){
				frontPageInfo = userConfigMap.get(configFile);
				if(frontPageInfo != null)
					return frontPageInfo;
				else{
					// TODO get data from DB and load specific to config - user
					frontPageInfo = getUserFrontPageInfo(conn, userId, menuTag,configFile,row,column);
					if(frontPageInfo != null)
						userConfigMap.put(configFile, frontPageInfo);
					return frontPageInfo;
				}
					
			}else{
				// TODO create new instance of userConfigMap and get data from DB and load specific to user
				
				userConfigMap = new ConcurrentHashMap<String, FrontPageInfo>(); //needed because users can share
				frontPageInfo = getUserFrontPageInfo(conn, userId, menuTag,configFile,row,column);
				if(frontPageInfo != null){
					userConfigMap.put(configFile, frontPageInfo);
					userConfigCache.put(uKey.toString(), userConfigMap);
				}
				return frontPageInfo;
			}
		}
//		else{
//			// TODO create new instance of userConfigCache and load user specific data from DB
//			userConfigCache = new HashMap<String, Map<String, FrontPageInfo>>();
//			userConfigMap = new HashMap<String, FrontPageInfo>();
//			frontPageInfo = getUserFrontPageInfo(userId, portNodeId,menuTag,configFile,row,column);
//			if(frontPageInfo != null)
//				userConfigMap.put(configFile, frontPageInfo);
//			userConfigCache.put(new Integer(uKey.toString()), userConfigMap);
//			return frontPageInfo;
//		}
		return frontPageInfo;
	}
	
	private static FrontPageInfo getPortNodeConfig(Connection conn, int portNodeId, String menuTag, String configFile, int row, int column) throws Exception{
		StringBuilder uKey = new StringBuilder();
		uKey.append(portNodeId).append("_").append(menuTag).append("_").append(configFile).append("_").append(row).append("_").append(column);
		FrontPageInfo frontPageInfo = null;
		Map<String, FrontPageInfo> portNodeConfigMap = null;
		if(portConfigCache != null){
			portNodeConfigMap = portConfigCache.get(uKey.toString());
			if(portNodeConfigMap != null){
				frontPageInfo = portNodeConfigMap.get(configFile);
				if(frontPageInfo != null)
					return frontPageInfo;
				else{
					// TODO get data from DB and load specific to config - port node
					frontPageInfo = getPortNodeFrontPageInfo(conn, portNodeId,menuTag,configFile,row,column);
					if(frontPageInfo != null)
							portNodeConfigMap.put(configFile, frontPageInfo);
					return frontPageInfo;
				}
					
			}else{
				// TODO create new instance of portNodeConfigMap and get data from DB and load specific to port node
				portNodeConfigMap = new ConcurrentHashMap<String, FrontPageInfo>(); //needed because sharing can happen
				frontPageInfo = getPortNodeFrontPageInfo(conn, portNodeId,menuTag,configFile,row,column);
				if(frontPageInfo != null){
					portNodeConfigMap.put(configFile, frontPageInfo);
					portConfigCache.put(uKey.toString(), portNodeConfigMap);
				}
				return frontPageInfo;
			}
		}
//		else{
//			// TODO create new instance of userConfigCache and load port node specific data from DB
//			portConfigCache = new HashMap<String, Map<String, FrontPageInfo>>();
//			portNodeConfigMap = new HashMap<String, FrontPageInfo>();
//			frontPageInfo = getUserFrontPageInfo(userId, portNodeId,menuTag,configFile,row,column);
//			if(frontPageInfo != null)
//				portNodeConfigMap.put(configFile, frontPageInfo);
//			portConfigCache.put(new Integer(uKey.toString()), portNodeConfigMap);
//			return frontPageInfo;
//		}
		return frontPageInfo;
	}
	
	public static String getParamValue(String paramName){
		return null;
	}
	
	public static FrontPageInfo getFrontPageConfig(Connection conn, int userId, int portNodeId, String menuTag, String configFile, int row, int column, ReportDetailVO reportDetailVO) throws Exception{
		FrontPageInfo frontPageInfo = null;
		if(userId != 0 && !Misc.isUndef(userId))
			frontPageInfo = getUserConfig(conn, userId, menuTag, configFile, row, column);
		if(frontPageInfo == null && portNodeId != 0 && !Misc.isUndef(portNodeId))
			frontPageInfo = getPortNodeConfig(conn, portNodeId, menuTag, configFile, row, column);
		if(frontPageInfo == null){
			frontPageInfo = checkAndLoadFrontPageInfoFromDB(conn, reportDetailVO);
		}
		return frontPageInfo;
	}
	
	public static FrontPageInfo getFrontPageConfig(Connection conn, int userId, int portNodeId, String menuTag, String configFile, int row, int column) throws Exception{
		FrontPageInfo frontPageInfo = null;
		if(userId != 0 && !Misc.isUndef(userId))
			frontPageInfo = getUserConfig(conn, userId, menuTag, configFile, row, column);
		if(frontPageInfo == null && portNodeId != 0 && !Misc.isUndef(portNodeId))
			frontPageInfo = getPortNodeConfig(conn, portNodeId, menuTag, configFile, row, column);
		if(frontPageInfo == null){
			frontPageInfo = checkAndLoadFrontPageInfo(conn, configFile);
		}
		return frontPageInfo;
	}
	
	public static FrontPageInfo getBaseFrontPageInfo(Connection conn, String configFile) throws Exception{
		return checkAndLoadFrontPageInfo(conn, configFile);
	}
	
	private static FrontPageInfo checkAndLoadFrontPageInfo(Connection conn, String configFile) throws Exception{
		FrontPageInfo fPageInfo = configCache.get(configFile);
		if(fPageInfo != null)
			return fPageInfo;
		else{
			fPageInfo = FrontPageInfo.getFrontPage(configFile, true, conn, Cache.getCacheInstance(conn));
			configCache.put(configFile, fPageInfo);
		}
		return fPageInfo;
	}
	
	private static FrontPageInfo checkAndLoadFrontPageInfoFromDB(Connection conn, ReportDetailVO reportDetailVO) throws Exception{
		FrontPageInfo fPageInfo = configCache.get(reportDetailVO.getFileName());
		if(fPageInfo != null)
			return fPageInfo;
		else{
			fPageInfo = FrontPageInfo.getFrontPage(reportDetailVO, true, conn, Cache.getCacheInstance(conn));
			configCache.put(reportDetailVO.getFileName(), fPageInfo);
		}
		return fPageInfo;
	}
	
	private static FrontPageInfo getUserFrontPageInfo(Connection conn, int userId, String menuTag, String configFile, int row, int column) throws Exception{
		CustomizeDao customizeDao = new CustomizeDao();
		MenuBean menuBean = customizeDao.getMenuByUserId(conn, userId, menuTag, configFile, row, column);
		if (menuBean == null)
			return null;
		FrontPageInfo fPageInfo = checkAndLoadFrontPageInfo(conn, configFile); 
		cleanupUiColumnList(fPageInfo, menuBean.getUiColumnBean());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(fPageInfo);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object deepCopy = ois.readObject();
		
		fPageInfo = (FrontPageInfo)deepCopy; //this will copy over the properties!!
		
		
		
		setFrontInfo(fPageInfo, menuBean);
		setFrontSearchCriteria(fPageInfo, menuBean);
		
		
		return fPageInfo;
	}
	
	private static FrontPageInfo getPortNodeFrontPageInfo(Connection conn, int portNodeId, String menuTag, String configFile, int row, int column) throws Exception{
		CustomizeDao customizeDao = new CustomizeDao();
		MenuBean menuBean = customizeDao.getMenuByPortId(conn, portNodeId, menuTag, configFile, row, column);
		if (menuBean == null)
			return null;
		FrontPageInfo fPageInfo = checkAndLoadFrontPageInfo(conn, configFile);
		cleanupUiColumnList(fPageInfo, menuBean.getUiColumnBean());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(fPageInfo);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object deepCopy = ois.readObject();
		fPageInfo = (FrontPageInfo)deepCopy;
		setFrontInfo(fPageInfo, menuBean);
		setFrontSearchCriteria(fPageInfo, menuBean);
		
		return fPageInfo;
	}
	private static void cleanupUiColumnList(FrontPageInfo fPageInfo, List uiColumnList) {
		for (int l = uiColumnList == null ? -1 : uiColumnList.size()-1; l >= 0 ; l--) {
			UIColumnBean uIColumnBean = (UIColumnBean) uiColumnList.get(l);
			String uiColumnName = uIColumnBean.getColumnName();
			Integer origDimConfigIndex = fPageInfo.m_colIndexLookup.get(uiColumnName);
			if (origDimConfigIndex == null) {
				//due to a naming bug somewhere uiColumnName may be dimId
				int tempId = Misc.getParamAsInt(uiColumnName);
				if (!Misc.isUndef(tempId)) {
					origDimConfigIndex = fPageInfo.m_colIndexLookup.get("d"+tempId);
				}
			}
			if (origDimConfigIndex == null) {
				uiColumnList.remove(l);
			}
		}
	}
	private static void setFrontInfo(FrontPageInfo fPageInfo, MenuBean menuBean)
	{

		if(fPageInfo != null){
			
			List uiColumnList = menuBean.getUiColumnBean();
			ArrayList dimConfigInfoList_ = new ArrayList();
			int[] posnOfDimConfigAdded = new int[fPageInfo.m_frontInfoList.size()];
			for (int i=0,is=posnOfDimConfigAdded.length;i<is;i++)
				posnOfDimConfigAdded[i] = -1;
			ArrayList<Integer> addnlDimForColorIndexIndicesOrig = new ArrayList<Integer>();
			for (int l = 0; l < uiColumnList.size(); l++) {
				UIColumnBean uIColumnBean = (UIColumnBean) uiColumnList.get(l);
				
				String uiColumnName = uIColumnBean.getColumnName();
				Integer origDimConfigIndex = fPageInfo.m_colIndexLookup.get(uiColumnName);
				if (origDimConfigIndex == null) {
					//due to a naming bug somewhere uiColumnName may be dimId
					int tempId = Misc.getParamAsInt(uiColumnName);
					if (!Misc.isUndef(tempId)) {
						origDimConfigIndex = fPageInfo.m_colIndexLookup.get("d"+tempId);
					}
				}
				if (origDimConfigIndex == null)
					continue;
				int origDimConfigIndexInt = origDimConfigIndex.intValue();
				//add things that are hidden that are before it 
				posnOfDimConfigAdded[origDimConfigIndexInt] = l;
				DimConfigInfo dimConfigInfo = (DimConfigInfo) fPageInfo.m_frontInfoList.get(origDimConfigIndexInt);
				dimConfigInfo.m_name = uIColumnBean.getAttrValue();
				dimConfigInfo.m_doRollupTotal = uIColumnBean.getRollup() == 1;
				dimConfigInfoList_.add(dimConfigInfo);
				if (dimConfigInfo.m_color_code_by != null) {
					String s = dimConfigInfo.m_color_code_by; 
					Integer origColorIndex  = fPageInfo.m_colIndexLookup.get(s);
					if (origColorIndex == null) {
						//due to a naming bug somewhere uiColumnName may be dimId
						int tempId = Misc.getParamAsInt(s);
						if (!Misc.isUndef(tempId)) {
							origColorIndex = fPageInfo.m_colIndexLookup.get("d"+tempId);
						}
					}
					if (origColorIndex != null) {
						addnlDimForColorIndexIndicesOrig.add(origColorIndex);
					}
				}
				
			}
			//now add dims needed for color by
			for (int i1=0,i1s = addnlDimForColorIndexIndicesOrig == null ? 0 : addnlDimForColorIndexIndicesOrig.size();i1<i1s;i1++) {
				int idx = addnlDimForColorIndexIndicesOrig.get(i1);
				if (posnOfDimConfigAdded[idx] < 0) {
					//we need add
					posnOfDimConfigAdded[idx] = dimConfigInfoList_.size();
					DimConfigInfo dimConfigInfo = (DimConfigInfo) fPageInfo.m_frontInfoList.get(idx);
					dimConfigInfo.m_hidden = true;
					dimConfigInfoList_.add(dimConfigInfo); 
				}
			}
			//add innerMandatory
			int lastAddedPos = dimConfigInfoList_.size();
			for (int i=fPageInfo.m_frontInfoList.size()-1;i>=0;i--) {
				DimConfigInfo dc = (DimConfigInfo)fPageInfo.m_frontInfoList.get(i);
				if (dc.innerMandatory && posnOfDimConfigAdded[i] == -1) {
					dc.m_hidden = true;
					if (lastAddedPos == dimConfigInfoList_.size())
						dimConfigInfoList_.add(dc);
					else {
						dimConfigInfoList_.add(lastAddedPos, dc);
					}
					posnOfDimConfigAdded[i] = lastAddedPos;
				}
				else {
					int posAddedAt = posnOfDimConfigAdded[i];
					if (posAddedAt != -1)
						lastAddedPos = posAddedAt;
				}
			}
			
			//add Mandatory
			lastAddedPos = dimConfigInfoList_.size();
			for (int i=fPageInfo.m_frontInfoList.size()-1;i>=0;i--) {
				DimConfigInfo dc = (DimConfigInfo)fPageInfo.m_frontInfoList.get(i);
				if (dc.m_isMandatory && posnOfDimConfigAdded[i] == -1) {
					dc.m_hidden = true;
					if (lastAddedPos == dimConfigInfoList_.size())
						dimConfigInfoList_.add(dc);
					else {
						dimConfigInfoList_.add(lastAddedPos, dc);
					}
					posnOfDimConfigAdded[i] = lastAddedPos;
				}
				else {
					int posAddedAt = posnOfDimConfigAdded[i];
					if (posAddedAt != -1)
						lastAddedPos = posAddedAt;
				}
			}
			//now add hidden - add it just before the next non-hidden item is added
			lastAddedPos = dimConfigInfoList_.size();
			
			for (int i=fPageInfo.m_frontInfoList.size()-1;i>=0;i--) {
				DimConfigInfo dc = (DimConfigInfo)fPageInfo.m_frontInfoList.get(i);
				if ((dc.m_hidden & posnOfDimConfigAdded[i] == -1)|| (posnOfDimConfigAdded[i] == -1 && (dc.m_isSelect || (dc.m_innerMenuList != null && dc.m_innerMenuList.size() != 0)))) {
					if (lastAddedPos == dimConfigInfoList_.size())
						dimConfigInfoList_.add(dc);
					else {
						dimConfigInfoList_.add(lastAddedPos, dc);
					}
				}
				else {
					int posAddedAt = posnOfDimConfigAdded[i];
					if (posAddedAt != -1)
						lastAddedPos = posAddedAt;
				}
			}
			
			for (int i=0,is=posnOfDimConfigAdded.length;i<is;i++)
				posnOfDimConfigAdded[i] = -1;
			fPageInfo.m_frontInfoList = dimConfigInfoList_;
			fPageInfo.postProcess(null, true);
		}
	}
	private static boolean setParamValFromMenuAndGetIfFound(DimConfigInfo dimConfigInfo, List uiParamList) {
		DimInfo dimInfo = dimConfigInfo != null && dimConfigInfo.m_dimCalc != null ? dimConfigInfo.m_dimCalc.m_dimInfo : null;
		boolean retval = false;
		String hackAnyValStr = Integer.toString(Misc.G_HACKANYVAL);
		String undefValStr = Integer.toString(Misc.getUndefInt());
		
		for (int l = 0; l < uiParamList.size(); l++) {
			UIParameterBean uipParameterBean = (UIParameterBean) uiParamList.get(l);
			
			String uiColumnName = uipParameterBean.getParamName();
			boolean beginsWithD = uiColumnName != null && uiColumnName.startsWith("d");
			int uiColumnDimId = beginsWithD ? Misc.getParamAsInt(uiColumnName.substring(1)) : Misc.getParamAsInt(uiColumnName);
			if (
					(
					(dimConfigInfo.m_columnName != null && dimConfigInfo.m_columnName.equals(uiColumnName)) ||
					(dimInfo != null && dimInfo.m_id ==uiColumnDimId )
					)
					
				&& (	!hackAnyValStr.equals(uipParameterBean.getParamValue()) && !"".equals(uipParameterBean.getParamValue()) && !undefValStr.equals(uipParameterBean.getParamValue()))
						
				)
				
			{
				dimConfigInfo.m_default = uipParameterBean.getParamValue();
				dimConfigInfo.m_rightOperand = uipParameterBean.getRightOperand();
				dimConfigInfo.m_defaultOperator = uipParameterBean.getOperator();
				retval = true;
				break;
			}
		}
		return retval;
	}
	private static void setFrontSearchCriteria(FrontPageInfo fPageInfo, MenuBean menuBean)
	{
		if(fPageInfo != null){
			List searchConfigInfoList = fPageInfo.m_frontSearchCriteria;
			List dimConfigInfoList_ = fPageInfo.m_frontInfoList;
			List uiParamList = menuBean.getUiParameterBean();
			

			DimConfigInfo dimConfigInfo = null;
			DimCalc dimCalc = null;
			DimInfo dimInfo = null;
			//Will add to searchConfigInfoList if
			//is hidden and there is parameter specified in the menu perferences, is mandatory or the search field is also in the view column
			//How it works
			//Below: dimConfigInfoList - the list of rows in the cloned FrontPageInfo derived from the base file
			// We go thru each row and if there is any col that has found to be worth adding then we add that col 
			// in a new array corresponding to new row (dimConfigInfoList_1) and at the end set the row entry to this
			// else if nothing is added then we remove.
			for (int k = 0; k < searchConfigInfoList.size(); k++) { //k could be changed inside because of removal - so k<searchConfigInfoList is needed
				List dimConfigInfoList= (List)searchConfigInfoList.get(k);
				ArrayList dimConfigInfoList_1 = new ArrayList(); //will add col's of search for this row in this
				for (int i = 0; i < dimConfigInfoList.size(); i++) { 
					dimConfigInfo= (DimConfigInfo)dimConfigInfoList.get(i);
					dimCalc = dimConfigInfo.m_dimCalc;
					dimInfo = dimCalc != null ? dimCalc.m_dimInfo : null;
					if(dimConfigInfo != null && (dimConfigInfo.m_hidden || dimConfigInfo.m_forceGetValueDefaultValueFromDB)){
						boolean added = setParamValFromMenuAndGetIfFound(dimConfigInfo, uiParamList);
						if (added) {//not all hidden are added - only those that are mandatory or explicitly chosen by the user are added
							dimConfigInfoList_1.add(dimConfigInfo);
						}
						if (!added && dimConfigInfo.m_isMandatory)
							dimConfigInfoList_1.add(dimConfigInfo);
					}
					else {
						if (dimConfigInfo.m_isMandatory) {
							setParamValFromMenuAndGetIfFound(dimConfigInfo, uiParamList);
							dimConfigInfoList_1.add(dimConfigInfo);
						}
						else {
							for (int j = 0; j < dimConfigInfoList_.size(); j++) {
								DimConfigInfo dimConfigInfo_ = (DimConfigInfo) dimConfigInfoList_.get(j);
								DimInfo dimInfo_ = dimConfigInfo_.m_dimCalc != null ? dimConfigInfo_.m_dimCalc.m_dimInfo : null;
								
								if ((dimConfigInfo_.m_columnName != null) && dimConfigInfo_.m_columnName.equalsIgnoreCase(dimConfigInfo.m_columnName) ||
										(dimInfo_ != null && dimInfo != null && dimInfo_.m_descDataDimId == dimInfo.m_descDataDimId)
									){
									setParamValFromMenuAndGetIfFound(dimConfigInfo, uiParamList);
									dimConfigInfoList_1.add(dimConfigInfo);
									break;
								}								
							}
						}
					}
//					dimConfigInfoList.remove(i);
				}//each col of search
				
//				if(dimConfigInfoList.size() == 0)
//					searchConfigInfoList.remove(k);
				if (dimConfigInfoList_1.size() == 0) {
					searchConfigInfoList.remove(k);
					k--;
				}
				else {
					searchConfigInfoList.set(k, dimConfigInfoList_1);
				}
			}//each row of search
		}
	}
	
	public static void main(String[] args) {
		
		FrontPageInfo fPageInfo = null;
		
		try {
			// fPageInfo = getFrontPageConfig(Connection conn, int userId, int portNodeId, String menuTag, String configFile, int row, int column) 
			fPageInfo = getFrontPageConfig(null, 2, 3, "20", "test_front_page_vehicle.xml", 0, 0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(fPageInfo.toString());
//		try {
//			fPageInfo = FrontPageInfo.getFrontPage("test_front_page_vehicle.xml", true, DBConnectionPool.getConnectionFromPool(), Cache.getCacheInstance(DBConnectionPool.getConnectionFromPool()));
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(fPageInfo);
//			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//			ObjectInputStream ois = new ObjectInputStream(bais);
//			Object deepCopy = ois.readObject();
//			System.out.println(deepCopy.toString());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	public static FrontPageInfo getFrontPageInfoByMenuId(Connection conn, int menuId, String configFile) throws Exception{
		CustomizeDao customizeDao = new CustomizeDao();
		MenuBean menuBean = customizeDao.getMenuById(conn, menuId);
		if (menuBean == null)
			return null;
		FrontPageInfo fPageInfo = checkAndLoadFrontPageInfo(conn, configFile);
		cleanupUiColumnList(fPageInfo, menuBean.getUiColumnBean());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(fPageInfo);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object deepCopy = ois.readObject();
		fPageInfo = (FrontPageInfo)deepCopy;
		setFrontInfo(fPageInfo, menuBean);
		setFrontSearchCriteria(fPageInfo, menuBean);
		return fPageInfo;
	}
	
}









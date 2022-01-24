package com.ipssi.reporting.customize;

import java.util.ArrayList;

import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;


public class MenuBean {
	private int id = Misc.getUndefInt();
	private long userId = Misc.getUndefInt();
	private int portNodeId = Misc.getUndefInt();
	private int rowId = 0;
	private int colId = 0;
	private String menuTag;
	private String componentFile;
	private ArrayList<UIColumnBean> uiColumnBean = new ArrayList<UIColumnBean>();
	private ArrayList<UIParameterBean> uiParameterBean = new ArrayList<UIParameterBean>();
	
	public int getRowId() {
		return rowId;
	}
	
	public int getColId() {
		return colId;
	}
	
	public void setRowId(int id) {
		this.rowId = id;
	}
	
	public void setColId(int id) {
		this.colId = id;
	}
	
	public int getId() {
		return id;
	}
	public ArrayList<UIColumnBean> getUiColumnBean() {
		return uiColumnBean;
	}
	public void setUiColumnBean(ArrayList<UIColumnBean> uiColumnBean) {
		this.uiColumnBean = uiColumnBean;
	}
	public ArrayList<UIParameterBean> getUiParameterBean() {
		return uiParameterBean;
	}
	public void setUiParameterBean(ArrayList<UIParameterBean> uiParameterBean) {
		this.uiParameterBean = uiParameterBean;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public String getMenuTag() {
		return menuTag;
	}
	public void setMenuTag(String menuTag) {
		this.menuTag = menuTag;
	}
	public String getComponentFile() {
		return componentFile;
	}
	public void setComponentFile(String componentFile) {
		this.componentFile = componentFile;
	}
	public static MenuBean getMenuBeanForFrontPage(FrontPageInfo frontPage, String menuTag, String componentFile, int row, int col, int userId, int portNodeId) {
		if (frontPage == null)
			return null;
		MenuBean menuBean = new MenuBean();
		menuBean.setMenuTag(menuTag);
		menuBean.setComponentFile(componentFile);
		menuBean.setRowId(row);
		menuBean.setColId(col);
		menuBean.setUserId(userId);
		menuBean.setPortNodeId(portNodeId);
		ArrayList<UIColumnBean> uiColList = new ArrayList<UIColumnBean>();
		for (int i=0,is=frontPage.m_frontInfoList.size();i<is;i++) {
           DimConfigInfo dc = (DimConfigInfo) frontPage.m_frontInfoList.get(i)	;
           if (!dc.m_hidden && dc.m_columnName != null && dc.m_columnName.length() != 0)
        	   uiColList.add(UIColumnBean.getColFromDimConfigInfo(dc));        	   
		}
		menuBean.setUiColumnBean(uiColList);
		ArrayList<UIParameterBean> searchColList = new ArrayList<UIParameterBean>();
		for (int i=0,is=frontPage.m_frontSearchCriteria.size();i<is;i++) {
			ArrayList<DimConfigInfo> r = (ArrayList<DimConfigInfo>) frontPage.m_frontSearchCriteria.get(i);
			for (int j=0,js=r.size();j<js;j++) {
				DimConfigInfo dc = r.get(j);
				if (dc != null && dc.m_hidden) {
					searchColList.add(UIParameterBean.getColFromDimConfigInfo(dc));
				}
			}
		}
		menuBean.setUiParameterBean(searchColList);
		return menuBean;
	}
}

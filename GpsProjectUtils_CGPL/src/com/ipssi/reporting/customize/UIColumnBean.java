package com.ipssi.reporting.customize;

import com.ipssi.gen.utils.DimConfigInfo;

public class UIColumnBean {
	private int menuId;
	private String columnName;
	private String attrName;
	private String attrValue;
	private int rollup;
	public int getRollup() {
		return rollup;
	}
	public void setRollup(int rollup) {
		this.rollup = rollup;
	}
	public int getMenuId() {
		return menuId;
	}
	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public String getAttrValue() {
		return attrValue;
	}
	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}
	public static UIColumnBean getColFromDimConfigInfo(DimConfigInfo dc) {
		if (dc == null)
			return null;
		UIColumnBean uiColBean = new UIColumnBean();
		uiColBean.setColumnName(dc.m_columnName);
		uiColBean.setAttrName(dc.m_name);
		uiColBean.setRollup(dc.m_doRollupTotal ? 1 : 0);
		return uiColBean;
	}
	
}

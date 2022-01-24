package com.ipssi.reporting.customize;

import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;

public class UIParameterBean {
	private int menuId = Misc.getUndefInt();
	private String paramName;
	private String paramValue;
	private String label;
	private DimInfo dimInfo;
	private String operator = null;
	private String rightOperand = null;
	//public UIParameterBean(String paramName, String paramValue){
	//	this.paramName = paramName;
	//	this.paramValue = paramValue;
	//}
	public UIParameterBean(String paramName, String paramValue, String operator, String rightOperand){
		this.paramName = paramName;
		this.paramValue = paramValue;
		this.operator = operator;
		this.rightOperand = rightOperand;
	}
	//public UIParameterBean(){}
	public DimInfo getDimInfo() {
		return dimInfo;
	}
	public void setDimInfo(DimInfo dimInfo) {
		this.dimInfo = dimInfo;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getMenuId() {
		return menuId;
	}
	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public String getParamValue() {
		return paramValue;
	}
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	public static UIParameterBean getColFromDimConfigInfo(DimConfigInfo dc) {
		if (dc == null)
			return null;
		UIParameterBean uiColBean = new UIParameterBean(dc.m_columnName, dc.m_default, dc.m_defaultOperator, dc.m_rightOperand);
		uiColBean.setDimInfo(dc.m_dimCalc == null ? null : dc.m_dimCalc.m_dimInfo);
		return uiColBean;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getRightOperand() {
		return rightOperand;
	}
	public void setRightOperand(String rightOperand) {
		this.rightOperand = rightOperand;
	}
	
}

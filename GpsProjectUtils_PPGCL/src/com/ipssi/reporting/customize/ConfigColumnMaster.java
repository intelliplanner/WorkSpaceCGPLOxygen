package com.ipssi.reporting.customize;

import com.ipssi.gen.utils.DimConfigInfo;

public class ConfigColumnMaster {
	private String columnId;
	private String internalName;
	private String label;
	private DimConfigInfo dimConfigInfo;
	private String propertyValues;
	private String operator;
	private String rightOperand;
//	private ArrayList<Integer> selectedValues = null;
	
	public String getPropertyValues() {
		return propertyValues;
	}
	public void setPropertyValues(String propertyValues) {
		this.propertyValues = propertyValues;
	}

	public DimConfigInfo getDimConfigInfo() {
		return dimConfigInfo;
	}
	public void setDimConfigInfo(DimConfigInfo dimConfigInfo) {
		this.dimConfigInfo = dimConfigInfo;
	}
	public String getColumnId() {
		return columnId;
	}
	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}
	public String getInternalName() {
		return internalName;
	}
	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
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

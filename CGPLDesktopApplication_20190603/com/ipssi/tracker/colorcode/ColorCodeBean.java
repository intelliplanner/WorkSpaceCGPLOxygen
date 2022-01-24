package com.ipssi.tracker.colorcode;

import java.util.ArrayList;
import java.util.HashMap;

public class ColorCodeBean {
	
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getApplicableTo() {
		return applicableTo;
	}
	public void setApplicableTo(int applicableTo) {
		this.applicableTo = applicableTo;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getReportType() {
		return reportType;
	}
	public void setReportType(int reportType) {
		this.reportType = reportType;
	}
	public int getColumnId() {
		return columnId;
	}
	public void setColumnId(int columnId) {
		this.columnId = columnId;
	}
	public int getGranuality() {
		return granuality;
	}
	public void setGranuality(int granuality) {
		this.granuality = granuality;
	}
	public int getAggrigation() {
		return aggrigation;
	}
	public void setAggrigation(int aggrigation) {
		this.aggrigation = aggrigation;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public int getThresholdOne() {
		return thresholdOne;
	}
	public void setThresholdOne(int thresholdOne) {
		this.thresholdOne = thresholdOne;
	}
	public int getThresholdTwo() {
		return thresholdTwo;
	}
	public void setThresholdTwo(int thresholdTwo) {
		this.thresholdTwo = thresholdTwo;
	}
	HashMap<Integer , ArrayList<ColorCodeBean>> colorInfo = new HashMap<Integer , ArrayList<ColorCodeBean>>();
	public HashMap<Integer, ArrayList<ColorCodeBean>> getColorInfo() {
		return colorInfo;
	}
	public void setColorInfo(HashMap<Integer, ArrayList<ColorCodeBean>> colorInfo) {
		this.colorInfo = colorInfo;
	}
	private String columnName;
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	private String name;
	private int applicableTo ;
	private String notes;
	private int reportType;
	private int columnId;
	private int granuality;
	private int aggrigation;
	private int order;
	private int thresholdOne;
	private int thresholdTwo;
	private int portNode ;
	private int status;
	private int colorCodeId;
	private boolean hasGranularity ;
	private boolean hasAggrigation ;
	private boolean colorCode = false;
	private boolean incerasing;
	private int chkAll; 
	
	public int getChkAll() {
		return chkAll;
	}
	public void setChkAll(int chkAll) {
		this.chkAll = chkAll;
	}
	public boolean isIncerasing() {
		return incerasing;
	}
	public void setIncerasing(boolean incerasing) {
		this.incerasing = incerasing;
	}
	public boolean isColorCode() {
		return colorCode;
	}
	public void setColorCode(boolean colorCode) {
		this.colorCode = colorCode;
	}
	public boolean isHasGranularity() {
		return hasGranularity;
	}
	public void setHasGranularity(boolean hasGranularity) {
		this.hasGranularity = hasGranularity;
	}
	public boolean isHasAggrigation() {
		return hasAggrigation;
	}
	public void setHasAggrigation(boolean hasAggrigation) {
		this.hasAggrigation = hasAggrigation;
	}
	ArrayList<ColorCodeBean> detailList = new ArrayList<ColorCodeBean>();
	
	public ArrayList<ColorCodeBean> getDetailList() {
		return detailList;
	}
	public void setDetailList(ArrayList<ColorCodeBean> detailList) {
		this.detailList = detailList;
	}
	public int getColorCodeId() {
		return colorCodeId;
	}
	public void setColorCodeId(int colorCodeId) {
		this.colorCodeId = colorCodeId;
	}
	public int getPortNode() {
		return portNode;
	}
	public void setPortNode(int portNode) {
		this.portNode = portNode;
	}
	public int getStatus() {
		// TODO Auto-generated method stub
	return	status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
	

}

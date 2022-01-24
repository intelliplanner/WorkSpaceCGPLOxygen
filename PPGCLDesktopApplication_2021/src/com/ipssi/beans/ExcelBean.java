package com.ipssi.beans;

import com.ipssi.custom_annotation.ExcelCol;

public class ExcelBean {
	@ExcelCol(value="TPR_ID")
	private String tprId;
	@ExcelCol(value="INVOICE STATUS")
	private String invoiceStatus;
	@ExcelCol(value="INVOICE_NO")
	private String invoiceNo;
	@ExcelCol(value="CUSTOMER")
	private String customer;
	@ExcelCol(value="LINE ITEM")
	private String lineItem;
	@ExcelCol(value="SALES_ORDER")
	private String salesOrder;
	@ExcelCol(value="TRANSPORTER")
	private String transporterName;
	@ExcelCol(value="TPR STATUS")
	private String tprStatus;
	@ExcelCol(value="VEHICLE_NAME")
	private String vehicleName;
	@ExcelCol(value="LOAD_TARE")
	private String loadTare;
	@ExcelCol(value="LOAD_GROSS")
	private String loadGross;
	
	@ExcelCol(value="Net Weight")
	private String netWt;
	
	@ExcelCol(value="GateInTime")
	private String gateInTime;
	@ExcelCol(value="TareTime")
	private String tareTime;
	@ExcelCol(value="GrossTime")
	private String grosstime;
	@ExcelCol(value="GateOut")
	private String gateOut;
	
	
	
	
	public ExcelBean(String tprId, String invoiceStatus, String invoiceNo, String customer, String lineItem, String salesOrder,
			String transporterName, String tprStatus, String vehicleName, String loadTare, String loadGross,String netWt,String gateInTime
			,String tareTime,String grosstime,String gateOut) {
		this.tprId=tprId;
		this.invoiceStatus = invoiceStatus;
		this.invoiceNo = invoiceNo;
		this.customer = customer;
		this.lineItem = lineItem;
		this.salesOrder = salesOrder;
		this.transporterName = transporterName;
		this.tprStatus = tprStatus;
		this.vehicleName = vehicleName;
		this.loadTare = loadTare;
		this.loadGross = loadGross;
		this.netWt=netWt;
		this.gateInTime=gateInTime;
		this.tareTime=tareTime;
		this.grosstime=grosstime;
		this.gateOut=gateOut;
	}

	public String getTprId() {
		return tprId;
	}

	public void setTprId(String tprId) {
		this.tprId = tprId;
	}

	public String getInvoiceStatus() {
		return invoiceStatus;
	}

	public void setInvoiceStatus(String invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getLineItem() {
		return lineItem;
	}

	public void setLineItem(String lineItem) {
		this.lineItem = lineItem;
	}

	public String getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(String salesOrder) {
		this.salesOrder = salesOrder;
	}

	public String getTransporterName() {
		return transporterName;
	}

	public void setTransporterName(String transporterName) {
		this.transporterName = transporterName;
	}

	public String getTprStatus() {
		return tprStatus;
	}

	public void setTprStatus(String tprStatus) {
		this.tprStatus = tprStatus;
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	public String getLoadTare() {
		return loadTare;
	}

	public void setLoadTare(String loadTare) {
		this.loadTare = loadTare;
	}

	public String getLoadGross() {
		return loadGross;
	}

	public void setLoadGross(String loadGross) {
		this.loadGross = loadGross;
	}

	public String getGateInTime() {
		return gateInTime;
	}

	public void setGateInTime(String gateInTime) {
		this.gateInTime = gateInTime;
	}

	public String getTareTime() {
		return tareTime;
	}

	public void setTareTime(String tareTime) {
		this.tareTime = tareTime;
	}

	public String getGrosstime() {
		return grosstime;
	}

	public void setGrosstime(String grosstime) {
		this.grosstime = grosstime;
	}

	public String getGateOut() {
		return gateOut;
	}

	public void setGateOut(String gateOut) {
		this.gateOut = gateOut;
	}

	public String getNetWt() {
		return netWt;
	}

	public void setNetWt(String netWt) {
		this.netWt = netWt;
	}

}

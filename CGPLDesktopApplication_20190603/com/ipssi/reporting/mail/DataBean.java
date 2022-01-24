package com.ipssi.reporting.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataBean {
	public static final String ALERT_FORMAT = "ALert: Reminder: @service_name is due on @vehicle_name on @service_date.";
	private double odometer;
	private double engine_hr;
	private Date service_date;
	private double metric_one;
	private double metric_two;
	private double months;
	private int vehicle_id;
	private int service_item_id;
	private int id;
	private int status;
	private int seq;
	private String vehicleName;
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public String getServiceItemName() {
		return serviceItemName;
	}
	public void setServiceItemName(String serviceItemName) {
		this.serviceItemName = serviceItemName;
	}
	public ArrayList<CustomerContactInfo> getCustomerContactList() {
		return customerContactList;
	}
	public void setCustomerContactList(ArrayList<CustomerContactInfo> customerContactList) {
		this.customerContactList = customerContactList;
	}
	private String serviceItemName;
	private ArrayList<CustomerContactInfo> customerContactList;
	
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getService_item_id() {
		return service_item_id;
	}
	public void setService_item_id(int service_item_id) {
		this.service_item_id = service_item_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVehicle_id() {
		return vehicle_id;
	}
	public void setVehicle_id(int vehicle_id) {
		this.vehicle_id = vehicle_id;
	}
	public double getOdometer() {
		return odometer;
	}
	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}
	public double getEngine_hr() {
		return engine_hr;
	}
	public void setEngine_hr(double engine_hr) {
		this.engine_hr = engine_hr;
	}
	public Date getService_date() {
		return service_date;
	}
	public void setService_date(Date service_date) {
		this.service_date = service_date;
	}
	public double getMetric_one() {
		return metric_one;
	}
	public void setMetric_one(double metric_one) {
		this.metric_one = metric_one;
	}
	public double getMetric_two() {
		return metric_two;
	}
	public void setMetric_two(double metric_two) {
		this.metric_two = metric_two;
	}
	public double getMonths() {
		return months;
	}
	public void setMonths(double months) {
		this.months = months;
	}
	public String getAlertFormatedString(){
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String retval = ALERT_FORMAT;
		retval = retval.replaceAll("@vehicle_name", vehicleName)
				       .replaceAll("@service_date", sdf.format(service_date == null ? new Date() : service_date))
				       .replaceAll("@service_name", serviceItemName);
		return retval;
	}
}

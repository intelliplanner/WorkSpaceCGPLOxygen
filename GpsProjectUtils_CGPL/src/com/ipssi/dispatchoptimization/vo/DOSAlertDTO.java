package com.ipssi.dispatchoptimization.vo;

import java.util.Date;

public class DOSAlertDTO {
	private int alertId;
	private int vehicle_id;
	private int alertDefId;
	private Date window_start;
	private Date window_end;
	private int num_trips_matched;
	private int num_trips_out_of_range;
	private int status;
	private double param_value;
	private Date eventStartAt;
	private Date eventStopAt;
	private Date updated_on;
	private String debugString;
	private boolean isOn;
	private boolean sendNotification;
	
	
	public String getDebugString() {
		return debugString;
	}
	public void setDebugString(String debugString) {
		this.debugString = debugString;
	}
	public boolean isSendNotification() {
		return sendNotification;
	}
	public void setSendNotification(boolean sendNotification) {
		this.sendNotification = sendNotification;
	}
	public int getAlertId() {
		return alertId;
	}
	public void setAlertId(int alertId) {
		this.alertId = alertId;
	}
	public int getVehicle_id() {
		return vehicle_id;
	}
	public void setVehicle_id(int vehicle_id) {
		this.vehicle_id = vehicle_id;
	}
	public int getAlertDefId() {
		return alertDefId;
	}
	public void setAlertDefId(int alertDefId) {
		this.alertDefId = alertDefId;
	}
	public Date getWindow_start() {
		return window_start;
	}
	public void setWindow_start(Date window_start) {
		this.window_start = window_start;
	}
	public Date getWindow_end() {
		return window_end;
	}
	public void setWindow_end(Date window_end) {
		this.window_end = window_end;
	}
	public int getNum_trips_matched() {
		return num_trips_matched;
	}
	public void setNum_trips_matched(int num_trips_matched) {
		this.num_trips_matched = num_trips_matched;
	}
	public int getNum_trips_out_of_range() {
		return num_trips_out_of_range;
	}
	public void setNum_trips_out_of_range(int num_trips_out_of_range) {
		this.num_trips_out_of_range = num_trips_out_of_range;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public double getParam_value() {
		return param_value;
	}
	public void setParam_value(double param_value) {
		this.param_value = param_value;
	}
	public Date getEventStartAt() {
		return eventStartAt;
	}
	public void setEventStartAt(Date eventStartAt) {
		this.eventStartAt = eventStartAt;
	}
	public Date getEventStopAt() {
		return eventStopAt;
	}
	public void setEventStopAt(Date eventStopAt) {
		this.eventStopAt = eventStopAt;
	}
	public Date getUpdated_on() {
		return updated_on;
	}
	public void setUpdated_on(Date updated_on) {
		this.updated_on = updated_on;
	}
	public boolean isOn() {
		return isOn;
	}
	public void setOn(boolean isOn) {
		this.isOn = isOn;
	}
	
	
	@Override
	public String toString() {
		StringBuffer sb =new StringBuffer();
		
		sb.append("alertId=").append(alertId)
		.append(",vehicle_id=").append(vehicle_id)
		.append(",alertDefId=").append(alertDefId)
		.append(",window_start=").append(window_start)
		.append(",window_end=").append(window_end)
		.append(",num_trips_matched=").append(num_trips_matched)
		.append(",num_trips_out_of_range=").append(num_trips_out_of_range)
		.append(",status=").append(status)
		.append(",param_value=").append(param_value)
		.append(",eventStartAt=").append(eventStartAt)
		.append(",eventStopAt=").append(eventStopAt)
		.append(",updated_on=").append(updated_on)
		.append(",isOn=").append(isOn)
		.append(",sendNotification=").append(sendNotification);
		return sb.toString();
	}
	
	public String toMsgString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
}

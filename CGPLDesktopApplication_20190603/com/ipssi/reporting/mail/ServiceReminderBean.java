package com.ipssi.reporting.mail;

import java.util.Date;

public class ServiceReminderBean {
	public int id;
	public int vehicle_id;
	public int service_item_id;
	public double odometer;
	public double odometer_threshold;
	public double engine_hr;
	public double engine_hr_threshold;
	public Date prev_service_date;
	public double months_threshold;
	public double metric_one;
	public double metric_one_threshold;
	public double metric_two;
	public double metric_two_threshold;
	public int completion_threshold;
	public int frequency_threshold;
	public Date updated_on;
	public int seq;
	public double odometer_reading;
	public double engine_hr_reading;
	public double metric_one_reading;
	public double metric_two_reading;
	public double days_reading;
	public Date reminder_date;
	public Date next_service_date;
	public int status;
	public double gps_start_reading;
	public Date gps_start_date;
	public double opt_odo_reading;
	public double opt_engine_reading;
	public int veh_recurring_service_id;
	public double log_odometer_reading;
	public double log_engine_hr_reading;
	public double log_metric_one_reading;
	public double log_metric_two_reading;
	public double log_days_reading;
	
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
	public int getService_item_id() {
		return service_item_id;
	}
	public void setService_item_id(int service_item_id) {
		this.service_item_id = service_item_id;
	}
	public double getOdometer() {
		return odometer;
	}
	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}
	public double getOdometer_threshold() {
		return odometer_threshold;
	}
	public void setOdometer_threshold(double odometer_threshold) {
		this.odometer_threshold = odometer_threshold;
	}
	public double getEngine_hr() {
		return engine_hr;
	}
	public void setEngine_hr(double engine_hr) {
		this.engine_hr = engine_hr;
	}
	public double getEngine_hr_threshold() {
		return engine_hr_threshold;
	}
	public void setEngine_hr_threshold(double engine_hr_threshold) {
		this.engine_hr_threshold = engine_hr_threshold;
	}
	public Date getPrev_service_date() {
		return prev_service_date;
	}
	public void setPrev_service_date(Date prev_service_date) {
		this.prev_service_date = prev_service_date;
	}
	public double getMonths_threshold() {
		return months_threshold;
	}
	public void setMonths_threshold(double months_threshold) {
		this.months_threshold = months_threshold;
	}
	public double getMetric_one() {
		return metric_one;
	}
	public void setMetric_one(double metric_one) {
		this.metric_one = metric_one;
	}
	public double getMetric_one_threshold() {
		return metric_one_threshold;
	}
	public void setMetric_one_threshold(double metric_one_threshold) {
		this.metric_one_threshold = metric_one_threshold;
	}
	public double getMetric_two() {
		return metric_two;
	}
	public void setMetric_two(double metric_two) {
		this.metric_two = metric_two;
	}
	public double getMetric_two_threshold() {
		return metric_two_threshold;
	}
	public void setMetric_two_threshold(double metric_two_threshold) {
		this.metric_two_threshold = metric_two_threshold;
	}
	public int getCompletion_threshold() {
		return completion_threshold;
	}
	public void setCompletion_threshold(int completion_threshold) {
		this.completion_threshold = completion_threshold;
	}
	public int getFrequency_threshold() {
		return frequency_threshold;
	}
	public void setFrequency_threshold(int frequency_threshold) {
		this.frequency_threshold = frequency_threshold;
	}
	public Date getUpdated_on() {
		return updated_on;
	}
	public void setUpdated_on(Date updated_on) {
		this.updated_on = updated_on;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public double getOdometer_reading() {
		return odometer_reading;
	}
	public void setOdometer_reading(double odometer_reading) {
		this.odometer_reading = odometer_reading;
	}
	public double getEngine_hr_reading() {
		return engine_hr_reading;
	}
	public void setEngine_hr_reading(double engine_hr_reading) {
		this.engine_hr_reading = engine_hr_reading;
	}
	public double getMetric_one_reading() {
		return metric_one_reading;
	}
	public void setMetric_one_reading(double metric_one_reading) {
		this.metric_one_reading = metric_one_reading;
	}
	public double getMetric_two_reading() {
		return metric_two_reading;
	}
	public void setMetric_two_reading(double metric_two_reading) {
		this.metric_two_reading = metric_two_reading;
	}
	public double getDays_reading() {
		return days_reading;
	}
	public void setDays_reading(double days_reading) {
		this.days_reading = days_reading;
	}
	public Date getReminder_date() {
		return reminder_date;
	}
	public void setReminder_date(Date reminder_date) {
		this.reminder_date = reminder_date;
	}
	public Date getNext_service_date() {
		return next_service_date;
	}
	public void setNext_service_date(Date next_service_date) {
		this.next_service_date = next_service_date;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public double getGps_start_reading() {
		return gps_start_reading;
	}
	public void setGps_start_reading(double gps_start_reading) {
		this.gps_start_reading = gps_start_reading;
	}
	public Date getGps_start_date() {
		return gps_start_date;
	}
	public void setGps_start_date(Date gps_start_date) {
		this.gps_start_date = gps_start_date;
	}
	public double getOpt_odo_reading() {
		return opt_odo_reading;
	}
	public void setOpt_odo_reading(double opt_odo_reading) {
		this.opt_odo_reading = opt_odo_reading;
	}
	public double getOpt_engine_reading() {
		return opt_engine_reading;
	}
	public void setOpt_engine_reading(double opt_engine_reading) {
		this.opt_engine_reading = opt_engine_reading;
	}
	public int getVeh_recurring_service_id() {
		return veh_recurring_service_id;
	}
	public void setVeh_recurring_service_id(int veh_recurring_service_id) {
		this.veh_recurring_service_id = veh_recurring_service_id;
	}
	public double getLog_odometer_reading() {
		return log_odometer_reading;
	}
	public void setLog_odometer_reading(double log_odometer_reading) {
		this.log_odometer_reading = log_odometer_reading;
	}
	public double getLog_engine_hr_reading() {
		return log_engine_hr_reading;
	}
	public void setLog_engine_hr_reading(double log_engine_hr_reading) {
		this.log_engine_hr_reading = log_engine_hr_reading;
	}
	public double getLog_metric_one_reading() {
		return log_metric_one_reading;
	}
	public void setLog_metric_one_reading(double log_metric_one_reading) {
		this.log_metric_one_reading = log_metric_one_reading;
	}
	public double getLog_metric_two_reading() {
		return log_metric_two_reading;
	}
	public void setLog_metric_two_reading(double log_metric_two_reading) {
		this.log_metric_two_reading = log_metric_two_reading;
	}
	public double getLog_days_reading() {
		return log_days_reading;
	}
	public void setLog_days_reading(double log_days_reading) {
		this.log_days_reading = log_days_reading;
	}
}

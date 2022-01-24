package com.ipssi.orient.jason.reader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ipssi.gen.utils.CacheTrack;

public class OrientVehicleDataDTO {

	private String vehicle_no;
	private String date_time;
	private String latitude;
	private String longitude;
	private String location;
	private String speed;
	private String device_id;
	private String ignition_status;
	private String temperature_status;
	
	public OrientVehicleDataDTO(String vehicle_no, String date_time, String latitude, String longitude, String location ,
			String speed, String device_id, String ignition_status, String temperature_status) {
		 this.vehicle_no=vehicle_no;
		 this.date_time=date_time;
		 this.latitude=latitude;
		 this.longitude=longitude;
		 this.location=location;
		 this.speed=speed;
		 this.device_id=device_id;
		 this.ignition_status=ignition_status;
		 this.temperature_status=temperature_status;
	}
	public String getVehicle_no() {
		return vehicle_no;
	}
	public void setVehicle_no(String vehicle_no) {
		this.vehicle_no = vehicle_no;
	}
	public String getDate_time() {
		return date_time;
	}
	public void setDate_time(String date_time) {
		this.date_time = date_time;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getIgnition_status() {
		return ignition_status;
	}
	public void setIgnition_status(String ignition_status) {
		this.ignition_status = ignition_status;
	}
	public String getTemperature_status() {
		return temperature_status;
	}
	public void setTemperature_status(String temperature_status) {
		this.temperature_status = temperature_status;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "vehicle_no=" +this.vehicle_no+",date_time=" +this.date_time+",latitude=" +this.latitude+",longitude=" +this.longitude+",location=" +this.location+",speed=" +this.speed+",device_id=" +this.device_id+",ignition_status=" +this.ignition_status+",temperature_status =" +this.temperature_status;
	}
	public String toJson() {
//		String data = "&NVT,JH05AH6815,22.93,86.0642,2013-08-13 18:16:40,2013-08-13 18:18:03,018,45.0036,75432.94,A";
//		incoming 08-06-2018 17:07:00
//		outgoing yyyy-MM-dd HH:mm:ss
		return "&NVT," +this.vehicle_no+","+this.latitude+","+this.longitude+"," +formatDate(this.date_time)+","+new SimpleDateFormat("yyMMddHHmmss").format(new Date())+"," +this.speed+",,,A#";
	}
	
	private static String formatDate(String input){
		DateFormat incoming = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
		Date date;
		try {
			date = (Date)incoming.parse(input);
			//SimpleDateFormat outgoing = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat outgoing = new SimpleDateFormat("yyMMddHHmmss");
			return outgoing.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(formatDate("16-08-2018 10:15:20"));
		
		
	}
	
}

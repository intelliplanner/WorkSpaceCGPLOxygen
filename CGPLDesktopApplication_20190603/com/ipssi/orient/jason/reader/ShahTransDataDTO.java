package com.ipssi.orient.jason.reader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ipssi.gen.utils.CacheTrack;

public class ShahTransDataDTO {
	private String deviceimei;
	private String vehicleno;
	private String NLangitude;
	private String ELangitude;
	private String Speed;
	private String Direction;
	private String Devicetime;
	private String AlertMsg;
	
	public ShahTransDataDTO(String deviceimei, String vehicleno, String NLangitude, String ELangitude,
String Speed, String Direction, String Devicetime, String AlertMsg) {
		 this.vehicleno=vehicleno;
		 this.deviceimei=deviceimei;
		 this.NLangitude=NLangitude;
		 this.ELangitude=ELangitude;
		 this.Speed=Speed;
		 this.Devicetime=Devicetime;
		 this.AlertMsg=AlertMsg;
	}
	
	

	public String getDeviceimei() {
		return deviceimei;
	}



	public void setDeviceimei(String deviceimei) {
		this.deviceimei = deviceimei;
	}



	public String getVehicleno() {
		return vehicleno;
	}



	public void setVehicleno(String vehicleno) {
		this.vehicleno = vehicleno;
	}



	public String getNLangitude() {
		return NLangitude;
	}



	public void setNLangitude(String langitude) {
		NLangitude = langitude;
	}



	public String getELangitude() {
		return ELangitude;
	}



	public void setELangitude(String langitude) {
		ELangitude = langitude;
	}



	public String getSpeed() {
		return Speed;
	}



	public void setSpeed(String speed) {
		Speed = speed;
	}



	public String getDirection() {
		return Direction;
	}



	public void setDirection(String direction) {
		Direction = direction;
	}



	public String getDevicetime() {
		return Devicetime;
	}



	public void setDevicetime(String devicetime) {
		Devicetime = devicetime;
	}



	public String getAlertMsg() {
		return AlertMsg;
	}



	public void setAlertMsg(String alertMsg) {
		AlertMsg = alertMsg;
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "vehicle_no=" +this.vehicleno+",date_time=" +this.Devicetime+",latitude=" +this.NLangitude+",longitude=" +this.ELangitude+",location=,speed=" +this.Speed+",device_id=" +this.deviceimei+",ignition_status=,temperature_status =" ;
	}
	public String toJson() {
//		String data = "&NVT,JH05AH6815,22.93,86.0642,2013-08-13 18:16:40,2013-08-13 18:18:03,018,45.0036,75432.94,A";
//		incoming 08-06-2018 17:07:00
//		outgoing yyyy-MM-dd HH:mm:ss
		return "&NVT," +this.vehicleno+","+this.NLangitude+","+this.ELangitude+"," +formatDate(this.Devicetime)+","+new SimpleDateFormat("yyMMddHHmmss").format(new Date())+"," +this.Speed+",,,A#";
	}
	
	private static String formatDate(String input){
		DateFormat incoming = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
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

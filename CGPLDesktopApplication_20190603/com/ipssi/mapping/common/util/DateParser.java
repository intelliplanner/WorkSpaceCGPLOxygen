/**
 * 
 */
package com.ipssi.mapping.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author jai
 * 
 */
public class DateParser {
	@SuppressWarnings("deprecation")
	public static Timestamp parseDate(String date, String hour, String min) throws ParseException {
		Timestamp t = new Timestamp(1);

		DateFormat formatter = new SimpleDateFormat(ApplicationConstants.PLAYBACK_DATE_FORMAT);
		try{
//		t = (Timestamp) formatter.parse(date);
			Date d = (Date) formatter.parse(date);
			t.setTime(d.getTime());
			
		
		} catch (Exception r){
			r.printStackTrace();
		}
		
		t.setHours(Integer.parseInt(hour));
		t.setMinutes(Integer.parseInt(min));
		System.out.println("DateParser.parseDate() ::::: " + t.toString());
		return t;
		
		
	}
	
	public static void main(String a[]){
		try {
			System.out.println(parseDate("02/14/2010","08","09"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

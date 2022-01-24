/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.connection;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.text.ParseException;

/**
 *
 * @author Vi$ky
 */
public class dateTest {
	public static void main(String[] args) {
		String string = "Jan 07, 2014 9:15:12 PM";
		DateFormat inFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");

		String string2 = "2014-12-27 19:10:22";
		DateFormat inFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		String string3 = "12-09-2014 01:10 PM";
		DateFormat inFormat3 = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
		DateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = null;
		Date date2 = null;
		Date date3 = null;
		try {
			date = inFormat.parse(string);
			date2 = inFormat2.parse(string2);
			date3 = inFormat3.parse(string3);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date != null) {
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");// dd/MM/yyyy
			Date now = new Date();
			String strDate = sdfDate.format(now);
			System.out.println("my Date" + strDate);

			// String myDate = outFormat.format(date);
			// String myDate2 = outFormat.format(date2);
			// String myDate3 = outFormat.format(date3);
			// LoggerNew.Write(myDate);
			// LoggerNew.Write(myDate2);
			// LoggerNew.Write(myDate3);
		}
	}
}

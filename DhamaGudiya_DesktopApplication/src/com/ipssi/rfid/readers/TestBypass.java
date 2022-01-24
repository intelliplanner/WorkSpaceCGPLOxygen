package com.ipssi.rfid.readers;

import java.io.DataInputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class TestBypass {
	public static void main(String[] arg) {
		try {
			DataInputStream di = null;
			// FileOutputStream fo = null;
			byte[] b = new byte[1];

			// PROXY
			System.setProperty("http.proxyHost", "172.16.108.174");
			System.setProperty("http.proxyPort", "8080");

			Authenticator.setDefault(new Authenticator() {
                                @Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("tecnet\\sams'basuki", "modi@2015".toCharArray());
				}
			});

			URL u = new URL("Http://203.197.197.17:8980/LocTracker/home.jsp");
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			di = new DataInputStream(con.getInputStream());
			while (-1 != di.read(b, 0, 1)) {
				System.out.print(new String(b));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

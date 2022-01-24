/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.data;

/**
 *
 * @author IPSSI
 */
public class LovType {
	public static class DataEntry {
		// (0=coal, 1=stone, 2=flyash, 3=other)
		public static final int AUTO = 0;
		public static final int MANUAL = 1;

		public static String getStr(int id) {
			switch (id) {
			case AUTO:
				return "AUTO";
			case MANUAL:
				return "MANUAL";
			default:
				return "NA";
			}
		}
		public static String getStrAutoComplete(int id) {
			switch (id) {
			case AUTO:
				return "ON";
			case MANUAL:
				return "OFF";
			default:
				return "NA";
			}
		}
	}
}

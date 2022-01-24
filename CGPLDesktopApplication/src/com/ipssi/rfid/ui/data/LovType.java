/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.data;

import com.ipssi.gen.utils.Misc;

/**
 *
 * @author IPSSI
 */
public class LovType {
	public static final int ANY = -1;
	public static class Status {
		public static final int ANY = -1;
		public static final int REPORTED = 1;
		public static final int DISPATCHED = 2;
		public static final int IN_PLANT = 3;
		public static final int UNDER_VERIFICATION = 4;
		public static final int CANCELLED = 9;
		public static final int CLOSED = 10;

		public static String getStr(int id) {
			switch (id) {
			case ANY:
				return "Any";
			case REPORTED:
				return "Reported";
			case DISPATCHED:
				return "Dispatched";
			case IN_PLANT:
				return "In Plant";
			case UNDER_VERIFICATION:
				return "Under Verification";
			case CANCELLED:
				return "Cancelled";
			case CLOSED:
				return "Closed";
			default:
				return "NA";
			}
		}
	}

	public static class TprStatus {
		public static final int ANY = -1;
		public static final int IN_PLANT = 0;
		public static final int DISPATCHED = 1;
		public static final int COMPLETED = 2;

		public static String getStr(int id) {
			switch (id) {
			case ANY:
				return "Any";
			case IN_PLANT:
				return "In Plant";
			case DISPATCHED:
				return "Dispatched";
			case COMPLETED:
				return "Completed";
			default:
				return "NA";
			}
		}
	}

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
	
	public static class Reason_60284 {
		public static final int TRANSPORTER_CHANGE=1;
		public static final int TARE_WEIGHT_CHANGE=2;
		public static final int GROSS_WEIGHT_CHANGE=3;
		public static final int DUPLICATE=4;
		public static final int MERGED=5;
		public static final int USER_ERROR=6;
		public static final int TESTING_TPR=7;
		public static final int SYSTEM_ERROR=8;
		
		public static String getStr(int id) {
			switch (id) {
			case TRANSPORTER_CHANGE:
				return "Transporter_Change";
			case TARE_WEIGHT_CHANGE:
				return "Tare_Weight_Change";
			case GROSS_WEIGHT_CHANGE:
				return "Gross_Weight_Change";
			case DUPLICATE:
				return "Duplicate";
			case MERGED:
				return "Merged";
			case USER_ERROR:
				return "User_Error";
			case TESTING_TPR:
				return "Testing_TPR";
			case SYSTEM_ERROR:
				return "System_Error";

			default:
				return "NA";
			}
		}
	}

	public static class DeviceState {
		public static final int DISABLE = 0;
		public static final int ENABLE = 1;

		public static String getStr(int id) {
			switch (id) {
			case DISABLE:
				return "Disable";
			case ENABLE:
				return "Enable";
			default:
				return "NA";
			}
		}
	}
	
}

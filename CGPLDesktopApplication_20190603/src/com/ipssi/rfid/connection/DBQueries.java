/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.connection;

/**
 *
 * @author Vi$ky
 */
public class DBQueries {

	private static final boolean isMySql = true;

	public static class IPSSI {

		public final static String INSERT_ENTRY_DATA;
		public final static String INSERT_OUT_DATA;
		public final static String UPDATE_OUT_DATA;
		static {
			if (isMySql) {
				INSERT_ENTRY_DATA = " INSERT INTO entryrecord (vehicleNo, gateIn) VALUES (?, ?) ";
				INSERT_OUT_DATA = " INSERT INTO entryrecord (vehicleNo, gateOut) VALUES (?, ?) ";
				UPDATE_OUT_DATA = " UPDATE entryrecord SET gateOut = ? WHERE vehicleNo = ? ";

			}
		}

	}

}

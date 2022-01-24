package com.ipssi.rfid.readers;

import java.sql.Connection;

import com.ipssi.rfid.beans.TPRecord;


public interface UIHandler {
	public void updateVehicleBlockStatus(Connection conn, int vehicleId, TPRecord tpr);
}

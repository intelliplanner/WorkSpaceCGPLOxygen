package com.ipssi.rfid.readers;

import java.sql.Connection;

import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.processor.TPRBlockManager;

public interface TAGListener {
	// void manageTag(ArrayList<String> tags);
	// void manageTag(Token token,TPRecord tpr,TPStep tps,int blockStatus, String
	// blockingReason);
	void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager tprBlockManager);

	void showMessage(String message);

	void setVehicleName(String vehicleName);

	int promptMessage(String message, Object[] options);

	// void setBlockingStatus(int blockStatus);
	void clearVehicleName();

	void clear(boolean clearToken, Connection conn);

	// void setBlockingReason(String blockingReason);
	int mergeData(long sessionId, String epc, RFIDHolder rfidHolder);
}

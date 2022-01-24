package com.ipssi.rfid.readers;

import java.sql.Connection;

import com.ipssi.rfid.beans.ProcessStepProfile;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.processor.TPRBlockManager;

public interface TAGListener {
	//void manageTag(ArrayList<String> tags);
	//void manageTag(Token token,TPRecord tpr,TPStep tps,int blockStatus, String blockingReason);
	void manageTag(int readerId, Connection conn,Token token,TPRecord tpr,TPStep tps, TPRBlockManager tprBlockManager, boolean isCurrentThread, boolean allowSave);
	boolean showMessage(int readerId, String message);
	void setVehicleName(int readerId, String vehicleName);
	int promptMessage(int readerId,String message, Object[] options);
	void varfiyVehicle(int readerId,Vehicle veh);
	//void setBlockingStatus(int blockStatus);
	void clearVehicleName(int readerId);
	void clear(int readerId,boolean clearToken, Connection conn);
	//void setBlockingReason(String blockingReason);
	int mergeData(int readerId, long sessionId, String epc, RFIDHolder rfidHolder);
    TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh);
    boolean isSystemReady();
    ProcessStepProfile getProcessStepProfile(int materialCat);
//	void processVehicleAndNotify(Connection conn, Token token, String epcId, int vehicleId, RFIDHolder data, String vehicleName);
}

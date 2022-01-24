package com.ipssi.rfid.readers;

import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralType;

public interface ConnectionStatusI {
	void setConnectionStatus(int id,PeripheralType type, PeripheralStatus status);
}

package com.ipssi.rfid.integration;

public interface WeighBridgeListener {
	void changeValue(String str);

	void showDisconnection();

	void removeDisconnection();
}

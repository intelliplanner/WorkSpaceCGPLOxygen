package com.ipssi.rfid.integration;

public interface WeighBridgeListener {
	void changeValue(String str);
	void isWBEmpty(boolean isEmpty);
	void showDisconnection();
	void removeDisconnection();
}

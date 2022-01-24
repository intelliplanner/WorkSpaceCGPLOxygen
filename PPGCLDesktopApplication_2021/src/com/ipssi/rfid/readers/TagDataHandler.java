package com.ipssi.rfid.readers;

import com.ipssi.rfid.beans.RFIDHolder;

public interface TagDataHandler {
	void read(String epcId, RFIDHolder holder);
}

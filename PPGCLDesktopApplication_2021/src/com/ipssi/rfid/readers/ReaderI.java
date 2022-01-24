package com.ipssi.rfid.readers;

import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.rfid.beans.RFIDTagInfo;

public interface ReaderI {

	boolean open();

	public void close();

	byte[] executeCommand(byte[] command);

	ArrayList<String> getRFIDTagList();

	HashMap<String, RFIDTagInfo> getRFIDTagInfoList();

	boolean writeCardG2(RFIDTagInfo tag, int attempt);

	// boolean writeCardG2(RFIDTagInfo tag);

	boolean blockEraseCardG2(byte[] epc, int attempt);

	// boolean blockEraseCardG2(byte[] epc);

	byte[] getCommand(CommandData command);

	RFIDTagInfo getData(byte[] epc);

	boolean clearData(byte[] epc, int attempt);
}

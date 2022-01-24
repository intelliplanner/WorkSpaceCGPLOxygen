package com.ipssi.rfid.readers;

import java.util.ArrayList;
import java.util.HashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.processor.Utils;

import UHF.Reader18;

public class RFIDReaderUHFClient implements ReaderI {

	private String portAddr;
	private int readerId;
	private int readerConnectionType = RFIDConfig.READER_TYPE_TCPIP;
	private int port;
	private String server;
	UHF.Reader18 reader = null;
	private int handle = Misc.getUndefInt();
	private int readerAddr = Misc.getUndefInt();
	/**
	 * @param args
	 */
	static {
		try {
			System.loadLibrary("UHF_Reader18");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public RFIDReaderUHFClient(String portAddr, int readerId) {
		this.portAddr = portAddr;
		this.readerId = readerId;
		this.readerConnectionType = RFIDConfig.READER_TYPE_SERIAL;
	}

	public RFIDReaderUHFClient(String server, int port, int readerId) {
		this.server = server;
		this.port = port;
		this.readerId = readerId;
		this.readerConnectionType = RFIDConfig.READER_TYPE_TCPIP;
	}

	public static void main(String[] args) {
		// RFIDReaderUHFClient rf = new RFIDReaderUHFClient("com4", 0);
		RFIDReaderSerialClient rf = new RFIDReaderSerialClient("com4", 0);
		rf.open();
		ArrayList<String> tags = rf.getRFIDTagList();
		for (int i = 0, is = tags == null ? 0 : tags.size(); i < is; i++) {
			RFIDTagInfo tag = rf.getData(Utils.HexStringToByteArray(tags.get(i)));
			System.out.println(tags.get(i));
			if (tag != null && tag.userData != null && tag.userData.length > 0)
				System.out.println(Utils.ByteArrayToHexString(tag.userData));
		}
		rf.close();
	}

	@Override
	public boolean open() {
		boolean retval = false;
		try {
			if (reader == null)
				reader = new Reader18();
			if (readerConnectionType == RFIDConfig.READER_TYPE_SERIAL) {
				int arr[] = new int[2];
				arr[0] = (byte) 0xFF;// serial port
				arr[1] = (byte) 0x05;
				int result[] = reader.AutoOpenComPort(arr);
				if (result != null && result.length > 3) {
					if (result[0] == 0) {
						readerAddr = result[2];
						handle = result[3];
						retval = true;
					}
				}
			} else {
				int result[] = reader.OpenNetPort((byte) 0xFF, port, server);
				if (result != null && result.length > 2) {
					if (result[0] == 0) {
						readerAddr = result[1];
						handle = result[2];
						retval = true;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (reader == null)
			return;
		try {
			if (readerConnectionType == RFIDConfig.READER_TYPE_SERIAL) {
				reader.CloseComPort();
			} else {
				reader.CloseNetPort(handle);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			reader = null;
			handle = Misc.getUndefInt();
			readerAddr = Misc.getUndefInt();
		}
	}

	@Override
	public byte[] executeCommand(byte[] command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getRFIDTagList() {
		if (reader == null || Misc.isUndef(handle))
			return null;
		ArrayList<String> rfidTagList = new ArrayList<String>();
		int[] arr = new int[2];
		arr[0] = 0xFF;
		arr[1] = handle;
		int[] dataStream = reader.Inventory_G2(arr);
		if (dataStream != null && dataStream.length > 2) {
			if (dataStream[0] == 0x01) {
				int inventoryCount = dataStream[1];
				int tagLen = 0;
				if (inventoryCount > 0) {
					tagLen = dataStream[2] - 1;
				}
				if (((tagLen + 1) * inventoryCount + 1) <= dataStream.length) {
					for (int i = 0; i < inventoryCount; i++) {
						byte[] EPC = new byte[tagLen];
						// int index = 0;
						int startIndex = 4 + (tagLen + 1) * i;
						for (int j = 0; j < tagLen; j++) {
							EPC[j] = (byte) dataStream[startIndex + j];
						}
						String EPCId = Utils.ByteArrayToHexString(EPC);
						rfidTagList.add(EPCId);
						// System.out.println(EPCId);
					}
				}
			}
		}
		return rfidTagList;
	}

	@Override
	public HashMap<String, RFIDTagInfo> getRFIDTagInfoList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean writeCardG2(RFIDTagInfo tag, int attempt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean blockEraseCardG2(byte[] epc, int attempt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getCommand(CommandData command) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * ReadCard_G2 (): Function description: The function is used to read part or
	 * all of a Tag�s Password, EPC, TID, or User memory. To the word as a unit,
	 * start to read data from the designated address. Usage: int[]
	 * ReadCard_G2(int[]arr); Parameter: arr: Input array. First byte is the address
	 * of the reader Second is the byte length of EPC. Third to EPC-length+2 is EPC
	 * number. EPC-length+3th is select the memory area to read. 0x00: Password
	 * area; 0x01: EPC memory area; 0x02: TID memory area; 0x03: User�s memory
	 * area; Other value when error occurred. EPC-length+4th is the address of tag
	 * data to read (Word/Hex). Such as, 0x00 stand in start to read data from first
	 * word, 0x01 stand in start to read data from second word, and so on.
	 * EPC-length+5th is the number of word to read. Can not set 0 or 120,
	 * otherwise, return the parameter error information. Num <= 120 EPC-length+6th
	 * to EPC-length+9th is Password. EPC-length+10th is EPC masking starting
	 * address of byte. EPC-length+11th is Masking bytes. EPC-length+12th is EPC
	 * masking Flag. 0x00:disabled; 0x01:enabled; EPC-length+13th is the handle of
	 * port. Returns: Return an array ,first is non-zero value when error
	 * occurred.Then second byte is error code. Zero value when successfully,then
	 * other byte is the Read data
	 * 
	 */
	@Override
	public RFIDTagInfo getData(byte[] epc) {

		// System.out.println("start getData");
		RFIDTagInfo tagInfo = null;
		if (reader == null || Misc.isUndef(handle) || epc == null || epc.length < 12)
			return null;
		int[] data = new int[25];
		int i = 0;
		data[i++] = 0xFF;
		byte wordCount = Utils.intToByte(32)[0];
		data[i++] = Utils.intToByte(epc.length)[0]; // EPC Len
		for (int k = 0; k < (epc.length); k++) {
			data[i++] = epc[k]; // EPC
		}
		data[i++] = Utils.MEM_USER; // MEM Type
		data[i++] = 0x00; // Word Ptr
		data[i++] = wordCount; // Num words in memory
		for (int j = 0; j < 4; j++) {
			data[i++] = 0x00; // password
		}
		data[i++] = 0x00; // mask addr
		data[i++] = 0x00; // mask len
		data[i++] = 0x00;
		data[i++] = handle;
		int[] dataStream = reader.ReadCard_G2(data);
		if (dataStream != null && dataStream.length > 2) {
			// System.out.println("data len" + (wordCount * 2 + 4));
			if (dataStream[0] == 0x00) {
				if (dataStream.length > 64) {
					tagInfo = new RFIDTagInfo();
					tagInfo.epcId = epc;
					tagInfo.userData = new byte[wordCount * 2];
					for (int j = 0; j < wordCount * 2; j++) {
						tagInfo.userData[j] = (byte) dataStream[j + 2];
					}
				}
			} else {
				System.out.println(Utils.GetReturnCodeDesc(dataStream[0]));
			}
			// System.out.println(Constant.GetReturnCodeDesc(dataStream[3]));
		}
		// System.out.println("end getData");
		return tagInfo;// TODO Auto-generated method stub
	}

	@Override
	public boolean clearData(byte[] epc, int attempt) {
		// TODO Auto-generated method stub
		return false;
	}

}

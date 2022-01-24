package com.ipssi.rfid.readers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import jssc.SerialPort;
import jssc.SerialPortException;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.connection.ConnectionManager;
import com.ipssi.rfid.processor.Utils;

public class RFIDReaderSerialClient implements ReaderI {

	private String portAddr;
	private boolean isInventoryScan = false;
	public int MAX_RETRY_COUNT = 3;
	private SerialPort comport = null;
	private Object lock1 = new Object();
	private int readerId = 0;

	public RFIDReaderSerialClient(String portAddr, int readerId) {
		this.portAddr = portAddr;
		this.readerId = readerId;
	}

	public boolean checkConnection() {
		boolean connTrue = false;
		synchronized (lock1) {
			try {
				if (comport != null && !comport.isOpened()) {
					try {
						comport.closePort();
						comport = null;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (comport == null) {
					comport = new SerialPort(portAddr);
					comport.openPort();
					comport.setParams(SerialPort.BAUDRATE_57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					setConnected();
				}
				connTrue = true;
			} catch (Exception ex) {
				setDisconnected();
				comport = null;
				ex.printStackTrace();
			}
		}
		return connTrue;
	}

	public void setConnected() {
		if (readerId == 0)
			ConnectionManager.setRfidReaderOneConnected(true);
		else if (readerId == 1)
			ConnectionManager.setRfidReaderTwoConnected(true);
		else
			ConnectionManager.setRfidDesktopConnectedd(true);
	}

	public void setDisconnected() {
		if (readerId == 0)
			ConnectionManager.setRfidReaderOneConnected(false);
		else if (readerId == 1)
			ConnectionManager.setRfidReaderTwoConnected(false);
		else
			ConnectionManager.setRfidDesktopConnectedd(false);
	}

	public void close() {
		try {
			if (comport != null) {
				comport.closePort();
			}
			comport = null;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("RFIDReaderTCPClient.close");
		}
	}

	public boolean open() {
		return checkConnection();
	}
	/*
	 * private byte[] getDataFromTCP(byte[] command){ byte[] retval = null; try{ if
	 * (socket != null && socket.isConnected()){
	 * socket.getOutputStream().write(command); Thread.sleep(50); } int readCount =
	 * 0; byte[] temp = null; int bytesRead = 0; int totalBytes = 5; //retval = new
	 * byte[512]; boolean readStart = false; if (socket != null &&
	 * socket.isConnected()) { while (readCount != totalBytes) { temp =
	 * socket.getInputStream().readBytes(); if(temp != null){ readCount =
	 * temp.length; if (!readStart && temp !=null) { totalBytes =
	 * Utils.byteToInt(temp[0]) + 1; readStart = true; } if(retval == null){ retval
	 * = new byte[totalBytes]; }
	 * 
	 * for(int i=0;i<temp.length;i++){ retval[i+bytesRead] = temp[i]; }
	 * 
	 * bytesRead += readCount; }else break; }
	 * 
	 * } }catch(Exception ex){ ex.printStackTrace(); } return retval; }
	 */

	private byte[] getDataFromSerial(byte[] command) {
		byte[] retval = null;
		try {
			if (comport != null && comport.isOpened()) {
				comport.writeBytes(command);
				Thread.sleep(50);
			}
			int readCount = 0;
			byte[] temp = null;
			int bytesRead = 0;
			int totalBytes = 5;
			// retval = new byte[512];
			boolean readStart = false;
			if (comport != null && comport.isOpened()) {
				while (readCount != totalBytes) {
					temp = comport.readBytes();
					if (temp != null) {
						readCount = temp.length;
						if (!readStart && temp != null) {
							totalBytes = Utils.byteToInt(temp[0]) + 1;
							readStart = true;
						}
						if (retval == null) {
							retval = new byte[totalBytes];
						}

						for (int i = 0; i < temp.length; i++) {
							retval[i + bytesRead] = temp[i];
						}

						bytesRead += readCount;
					} else {
						break;
					}
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public byte[] executeCommand(byte[] command) {
		boolean isConnection = false;
		byte[] retval = null;
		int retryCount = 0;
		if (isInventoryScan) {
			/*
			 * try { wait(1000); } catch (InterruptedException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 */
			return null;
		}
		isInventoryScan = true;
		synchronized (lock1) {
			while (retryCount < MAX_RETRY_COUNT) {
				isConnection = checkConnection();
				if (isConnection) {
					try {
						if (comport.isOpened()) {
							comport.writeBytes(command);
							Thread.sleep(50);
						}
						int readCount = 0;
						byte[] temp = null;
						int bytesRead = 0;
						int totalBytes = 5;
						// retval = new byte[512];
						boolean readStart = false;
						long st = System.currentTimeMillis();
						while (bytesRead != totalBytes) {
							if (comport.isOpened()) {
								temp = comport.readBytes();
								if (temp == null) {
									if ((System.currentTimeMillis() - st) > 2 * 1000)
										throw new RFIDException("Serial port disconnected");
									continue;
								}
								readCount = temp.length;
								if (!readStart && temp != null) {
									totalBytes = Utils.byteToInt(temp[0]) + 1;
									readStart = true;
								}
								if (retval == null) {
									retval = new byte[totalBytes];
								}

								for (int i = 0; i < temp.length; i++) {
									retval[i + bytesRead] = temp[i];
								}

								bytesRead += readCount;
							}
							if (retval != null && totalBytes > 5) {
								byte[] crc = Utils.longToByte(Utils.uiCrc16Cal(retval, (totalBytes - 2)));// BitConverter.GetBytes();
								if (Utils.byteToInt(crc[0]) != Utils.byteToInt(retval[totalBytes - 2])
										|| Utils.byteToInt(crc[1]) != Utils.byteToInt(retval[totalBytes - 1])) {
									retval = null;
								} else {
									break;
								}
							}
						}
					} catch (SerialPortException ex) {
						setDisconnected();
						try {
							if (comport != null)
								comport.closePort();
							comport = null;
						} catch (Exception ex1) {
							ex1.printStackTrace();
						}
						ex.printStackTrace();
					} catch (RFIDException ex) {
						setDisconnected();
						try {
							if (comport != null)
								comport.closePort();
							comport = null;
						} catch (Exception ex1) {
							ex1.printStackTrace();
						}
						ex.printStackTrace();
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						/*
						 * try{ notifyAll(); }catch(Exception ex){ ex.printStackTrace(); }
						 */
					}
					if (retval != null) {
						break;
					}
					retryCount++;
				}
			}
		}
		isInventoryScan = false;
		return retval;
	}

	public ArrayList getRFIDTagList() {
		ArrayList<String> rfidTagList = null;
		byte[] dataStream = null;
		int dataCount = 0;
		CommandData commandInventory = new CommandData();
		commandInventory.cmd = Utils.COMMAND_INVENTORY;
		dataStream = executeCommand(getCommand(commandInventory));
		rfidTagList = new ArrayList<String>();
		if (dataStream != null && dataStream.length > 5 && dataStream[0] > 0x06
				&& dataStream[2] == Utils.COMMAND_INVENTORY) {
			if (dataStream[3] == 0x01) {
				int inventoryCount = Utils.byteToInt(dataStream[4]);
				int tagLen = 0;
				if (inventoryCount > 0) {
					tagLen = Utils.byteToInt(dataStream[5]);
				}
				if (((tagLen + 1) * inventoryCount + 1) <= dataStream.length) {
					for (int i = 0; i < inventoryCount; i++) {
						byte[] EPC = new byte[tagLen];
						// int index = 0;
						int startIndex = 6 + (tagLen + 1) * i;
						for (int j = 0; j < tagLen; j++) {
							EPC[j] = dataStream[startIndex + j];
						}
						String EPCId = Utils.ByteArrayToHexString(EPC);
						rfidTagList.add(EPCId);
						System.out.println(EPCId);
					}
				}
			}
		}
		return rfidTagList;
	}

	public HashMap<String, RFIDTagInfo> getRFIDTagInfoList() {
		HashMap<String, RFIDTagInfo> rfidTagInfoList = null;
		byte[] dataStream = null;
		int dataCount = Utils.UNDEF_INT;
		CommandData commandInventory = new CommandData();
		commandInventory.cmd = Utils.COMMAND_INVENTORY;
		dataStream = executeCommand(getCommand(commandInventory));
		rfidTagInfoList = new HashMap<String, RFIDTagInfo>();
		if (dataStream != null && dataStream.length > 5 && dataStream[0] > 0x06
				&& dataStream[2] == Utils.COMMAND_INVENTORY) {
			if (dataStream[3] == 0x01) {
				// System.out.println("Success");
				int inventoryCount = Utils.byteToInt(dataStream[4]);
				int tagLen = 0;
				if (inventoryCount > 0) {
					tagLen = Utils.byteToInt(dataStream[5]);
				}
				if (((tagLen + 1) * inventoryCount + 1) <= dataStream.length) {
					for (int i = 0; i < inventoryCount; i++) {
						byte[] EPC = new byte[tagLen];
						// int index = 0;
						int startIndex = 6 + (tagLen + 1) * i;
						for (int j = 0; j < tagLen; j++) {
							EPC[j] = dataStream[startIndex + j];
						}
						System.out.println((i + 1) + "," + Utils.ByteArrayToHexString(EPC));
						RFIDTagInfo tag = getData(EPC);
						if (tag != null && tag.userData != null) {
							rfidTagInfoList.put(Utils.ByteArrayToHexString(EPC), tag);
						}
					}
				}
				// System.out.println(tagList);
			} else {
				// System.out.println(Constant.GetReturnCodeDesc(dataStream[3]));
			}
		} else {
			// System.out.println("No tag operated");
		}
		return rfidTagInfoList;
	}

	public boolean writeCardG2(RFIDTagInfo tag, int attempt) {
		boolean retval = false;
		for (int i = 0; i < attempt; i++) {
			retval = writeCardG2(tag);
			if (retval) {
				break;
			}
			// Thread.Sleep(50);
		}
		return retval;
	}

	private boolean writeCardG2(RFIDTagInfo tag) {
		boolean retval = false;
		if (tag == null || tag.epcId == null || tag.epcId.length <= 0) {
			System.out.println("No Tag Oprated for Write");
			return retval;
		}
		CommandData commandData = new CommandData();
		commandData.cmd = Utils.COMMAND_WRITE_DATA;
		byte wordCount = Utils.intToByte(tag.userData.length / 2)[0];
		int dataLen = 10 + tag.epcId.length + (wordCount * 2);
		int i = 0;
		byte[] data = new byte[dataLen];
		data[i++] = wordCount; // word count
		data[i++] = Utils.intToByte((tag.epcId.length / 2))[0]; // EPC Len
		for (int k = 0; k < tag.epcId.length; k++) {
			data[i++] = tag.epcId[k]; // EPC
		}
		data[i++] = Utils.MEM_USER; // MEM Type
		data[i++] = 0x00; // Word Ptr
		for (int j = 0; j < (wordCount * 2); j++) {
			data[i++] = tag.userData[j]; // userdata
		}
		for (int k = 0; k < 4; k++) {
			data[i++] = 0x00; // password
		}
		data[i++] = 0x00; // mask addr
		data[i++] = Utils.intToByte(tag.epcId.length / 2)[0]; // EPC Len
		commandData.data = data;
		byte[] dataStream = executeCommand(getCommand(commandData));
		if (dataStream != null && dataStream.length > 3) {
			if (dataStream[0] > 0x03 && dataStream[2] == Utils.COMMAND_WRITE_DATA) {
				if (dataStream[3] == 0x00) {
					retval = true;
				}
			} else {
				System.out.println(Utils.GetReturnCodeDesc(dataStream[3]));
			}
		}
		return retval;
	}

	public boolean blockEraseCardG2(byte[] epc, int attempt) {
		boolean retval = false;
		RFIDTagInfo tag = new RFIDTagInfo();
		tag.epcId = epc;
		tag.userData = "0000".getBytes(Charset.forName("UTF-8"));
		;
		for (int i = 0; i < attempt; i++) {
			retval = writeCardG2(tag);
			if (retval) {
				break;
			}
			// Thread.Sleep(50);
		}
		return retval;
	}

	private boolean blockEraseCardG2(byte[] epc) {
		boolean retval = false;
		if (epc == null || epc.length <= 0) {
			System.out.println("No Tag Oprated for Erase");
			return retval;
		}
		CommandData commandData = new CommandData();
		commandData.cmd = Utils.COMMAND_BLOCK_ERASE;
		int dataLen = 10 + epc.length;
		int i = 0;
		byte wordCount = Utils.intToByte(32)[0];
		byte[] data = new byte[dataLen];
		// ENum EPC Mem WordPtr Num Pwd MaskAdr MaskLen
		data[i++] = Utils.intToByte(epc.length / 2)[0]; // EPC Len
		for (int k = 0; k < (epc.length); k++) {
			data[i++] = epc[k]; // EPC
		}
		data[i++] = Utils.MEM_USER; // MEM Type
		data[i++] = 0x00; // Word Ptr
		data[i++] = wordCount; // Word count
		for (int j = 0; j < 4; j++) {
			data[i++] = 0x00; // password
		}
		data[i++] = 0x00; // mask addr
		data[i++] = 0x00; // mask len
		commandData.data = data;
		byte[] dataStream = executeCommand(getCommand(commandData));
		if (dataStream != null && dataStream.length > 3) {
			if (dataStream[0] > 0x03 && dataStream[2] == Utils.COMMAND_WRITE_DATA) {
				if (dataStream[3] == 0x00) {
					retval = true;
				}
			} else {
				System.out.println(Utils.GetReturnCodeDesc(dataStream[3]));
			}
		}
		return retval;
	}

	public byte[] getCommand(CommandData command) {
		// System.out.println("start getCommand");
		byte[] retval = null;
		if (command != null) {
			int len = command.data != null ? (4 + command.data.length) : 4;
			command.length = Utils.intToByte(len)[0];
			retval = new byte[len + 1];
			int i = 0;
			retval[i++] = command.length;
			retval[i++] = command.addr;
			retval[i++] = command.cmd;
			if (command.data != null && command.data.length > 0) {
				for (int j = 0; j < command.data.length; j++) {
					retval[i++] = command.data[j];
				}
			}

			/*
			 * CRC16 crc16 = new CRC16(); crc16.update(retval, 0, retval.length-1);
			 */
			retval[i++] = Utils.longToByte(Utils.uiCrc16Cal(retval, (len - 1)))[0];
			retval[i++] = Utils.longToByte(Utils.uiCrc16Cal(retval, (len - 1)))[1];
		}
		// System.out.println("end getCommand");
		return retval;
	}

	public RFIDTagInfo getData(byte[] epc) {
		// System.out.println("start getData");
		RFIDTagInfo tagInfo = null;
		if (epc != null && epc.length == 12) {
			CommandData commandData = new CommandData();
			commandData.cmd = Utils.COMMAND_READ_DATA;
			int dataLen = 10 + epc.length;
			int i = 0;
			byte wordCount = Utils.intToByte(32)[0];
			byte[] data = new byte[dataLen];
			data[i++] = Utils.intToByte(epc.length / 2)[0]; // EPC Len
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
			data[i++] = Utils.intToByte(epc.length)[0]; // EPC Len
			// data[i++] = 0x00; //mask enabled
			commandData.data = data;
			byte[] dataStream = executeCommand(getCommand(commandData));
			if (dataStream != null && dataStream.length > 2 && (Utils.byteToInt(dataStream[0]) > (wordCount * 2 + 4)
					&& dataStream.length >= (wordCount * 2 + 4)) && dataStream[2] == Utils.COMMAND_READ_DATA) {
				// System.out.println("data len" + (wordCount * 2 + 4));
				if (dataStream[3] == 0x00) {
					tagInfo = new RFIDTagInfo();
					tagInfo.epcId = epc;
					tagInfo.userData = new byte[wordCount * 2];
					for (int j = 0; j < wordCount * 2; j++) {
						tagInfo.userData[j] = dataStream[j + 4];
					}

				}
				// System.out.println(Constant.GetReturnCodeDesc(dataStream[3]));
			}
		}
		// System.out.println("end getData");
		return tagInfo;
	}

	@Override
	public boolean clearData(byte[] epc, int attempt) {

		boolean retval = false;
		if (epc == null || epc.length <= 0) {
			System.out.println("No Tag Oprated for Write");
			return retval;
		}
		CommandData commandData = new CommandData();
		commandData.cmd = Utils.COMMAND_WRITE_DATA;
		byte[] userData = new byte[2];
		userData[0] = RFIDHolder.CARD_CLEAR;
		userData[1] = RFIDHolder.CARD_CLEAR;
		byte wordCount = Utils.intToByte(userData.length / 2)[0];
		int dataLen = 10 + epc.length + (wordCount * 2);
		int i = 0;
		byte[] data = new byte[dataLen];
		data[i++] = wordCount; // word count
		data[i++] = Utils.intToByte((epc.length / 2))[0]; // EPC Len
		for (int k = 0; k < epc.length; k++) {
			data[i++] = epc[k]; // EPC
		}
		data[i++] = Utils.MEM_USER; // MEM Type
		data[i++] = 0x1f; // Word Ptr
		for (int j = 0; j < (wordCount * 2); j++) {
			data[i++] = userData[j]; // userdata
		}
		for (int k = 0; k < 4; k++) {
			data[i++] = 0x00; // password
		}
		data[i++] = 0x00; // mask addr
		data[i++] = 0x00; // mask len
		commandData.data = data;
		byte[] dataStream = executeCommand(getCommand(commandData));
		if (dataStream != null && dataStream.length > 3) {
			if (dataStream[0] > 0x03 && dataStream[2] == Utils.COMMAND_WRITE_DATA) {
				if (dataStream[3] == 0x00) {
					retval = true;
				}
			} else {
				System.out.println(Utils.GetReturnCodeDesc(dataStream[3]));
			}
		}
		return retval;

	}
}

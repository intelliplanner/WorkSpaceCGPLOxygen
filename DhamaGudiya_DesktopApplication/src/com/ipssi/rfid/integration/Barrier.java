package com.ipssi.rfid.integration;

import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.connection.ConfigUtility;
import com.ipssi.rfid.connection.ConnectionManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDConfig;
import com.ipssi.rfid.readers.RFIDException;
import com.ipssi.rfid.readers.RFIDMaster;


import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

//import org.bridj.cpp.com.OLEAutomationLibrary;

public class Barrier {
	private Barrier barrier = null;
	static SerialPort comport = null;
	boolean _continue = false;
	Thread readThread = null;
	static String comAddr;
	static int baudrate;
	static int dataBits;
	static int stopBits;
	static int parity;
	static byte changeSignalCommand = "G".getBytes()[0];
	static byte entryCommand = "R".getBytes()[0];
	static byte exitCommand = "X".getBytes()[0];
	static Object lock1 = new Object();
	static {
		try {
			ConfigUtility configUtility = new ConfigUtility();
			Properties prop = configUtility.getBarrierConfiguration();
			comAddr = Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT"), "COM10");
			baudrate = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_BAUDRATE"), 9600);
			dataBits = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_DATABITS"), 8);
			parity = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_PARITY"), 0);
			stopBits = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_STOPBITS"), 1);
			changeSignalCommand = Misc.getParamAsString(prop.getProperty("CHANGE_SIGNAL_COMMAND"), "G").getBytes()[0];
			entryCommand = Misc.getParamAsString(prop.getProperty("BARRIER_ENTRY_COMMAND"), "R").getBytes()[0];
			exitCommand = Misc.getParamAsString(prop.getProperty("BARRIER_EXIT_COMMAND"), "E").getBytes()[0];
			System.out.println(
					"---------------------------------------Barrier initProperties-------------------------------------------");
			System.out.println("changeSignalCommand : " + prop.getProperty("CHANGE_SIGNAL_COMMAND"));
			System.out.println("changeSignalCommand into byte Length : " + prop.getProperty("CHANGE_SIGNAL_COMMAND"));
			System.out.println("EntryCommand : " + prop.getProperty("BARRIER_ENTRY_COMMAND"));
			System.out.println("EntryCommand into byte Length : " + prop.getProperty("BARRIER_ENTRY_COMMAND"));
			System.out.println(
					"--------------------------------------------------------------------------------------------------------");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void setConnected() {
		ConnectionManager.setBarrierConnected(true);
	}

	public static void setDisconnected() {
		ConnectionManager.setBarrierConnected(false);
	}

	public static void openEntryGate() {
		try {
			if (isConnected()) {
				comport.writeByte(entryCommand);
				System.out.println("Barrier request : " + entryCommand);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void openExitGate() {
		try {
			if (isConnected()) {
				comport.writeByte(exitCommand);
				System.out.println("BARRIER EXIT OPEN COMMAND : " + exitCommand);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void ChangeSignal() {
		try {
			if (isConnected()) {
				comport.writeByte(changeSignalCommand);
				System.out.println("Change Signal COMMAND : " + changeSignalCommand);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Barrier.openEntryGate();
		try {
			Thread.sleep(15 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Barrier.openExitGate();
	}

	public static boolean isConnected() {
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
					comport = new SerialPort(comAddr);
					comport.openPort();
					comport.addEventListener(new SerialPortEventListener() {

						@Override
						public void serialEvent(SerialPortEvent event) {

							int dataLength = event.getEventValue();
							if (event.isRXCHAR()) {// If data is available

								if (dataLength > 0) {// Check bytes count in the input buffer
									// Read data, if 10 bytes available
									try {
										byte buffer[] = comport.readBytes(dataLength);
										String message = new String(buffer);
										if (!Utils.isNull(message)) {
											System.out.println("Barrier response : " + message);
										}
									} catch (SerialPortException ex) {
										setDisconnected();
										ex.printStackTrace();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							} else if (event.isCTS()) {// If CTS line has changed state
								if (event.getEventValue() == 1) {// If line is ON
									System.out.println("CTS - ON");
								} else {
									System.out.println("CTS - OFF");
								}
							} else if (event.isDSR()) {/// If DSR line has changed state
								if (event.getEventValue() == 1) {// If line is ON
									System.out.println("DSR - ON");
								} else {
									System.out.println("DSR - OFF");
								}
							}
						}
					});
					comport.setParams(baudrate, dataBits, stopBits, parity);
					setConnected();
				}
				connTrue = true;
			} catch (Exception ex) {
				comport = null;
				setDisconnected();
				ex.printStackTrace();
			}
		}
		return connTrue;
	}
}

package com.ipssi.rfid.integration;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.connection.ConfigUtility;
import com.ipssi.rfid.connection.ConnectionManager;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDException;
//import com.sun.management.ThreadMXBean;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class WeighBridge implements SerialPortEventListener, InterruptListener {
	
	SerialPort comport = null;
	boolean _continue = false;
	Thread readThread = null;
	char stx = (char) 2;
	char etx = (char) 3;
	char cr = (char) 13;
	char nl = (char) 10;
	char space = (char) 32;
	String comAddr;
	int baudrate;
	int dataBits;
	int parity;
	int stopBits;
	WeighBridgeListener listener;
	Object lock1 = new Object();

	public static void main(String[] args) {
		WeighBridge wb = new WeighBridge();
		wb.startWeighBridge();
	}

	public WeighBridge() {
		try {
			ConfigUtility configUtility = new ConfigUtility();
			Properties prop = configUtility.getWeighBridgeConfiguration();
			comAddr = Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT"), "COM9");
			baudrate = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_BAUDRATE"), 2400);
			dataBits = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_DATABITS"), 7);
			parity = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_PARITY"), 0);
			stopBits = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_STOPBITS"), 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private static ConcurrentHashMap<String, WeighBridge> weighBridgeMap = new ConcurrentHashMap<>();
	public static WeighBridge getWeighBridge(String comAddress, WeighBridgeListener listener) {
		WeighBridge wb = null;
		if(weighBridgeMap.containsKey(comAddress)) {
			wb = weighBridgeMap.get(comAddress);
		}else {
			wb = new WeighBridge(comAddress);
			weighBridgeMap.put(comAddress, wb);
			wb.startWeighBridge();
		}
		if(wb != null)
			wb.setListener(listener);
		return wb;
	}
	
	private WeighBridge(String comAddress) {
		try {
			ConfigUtility configUtility = new ConfigUtility();
			Properties prop = configUtility.getWeighBridgeConfiguration();
			comAddr = Misc.getParamAsString(prop.getProperty(comAddress), "COM9");
			baudrate = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_BAUDRATE"), 2400);
			dataBits = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_DATABITS"), 7);
			parity = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_PARITY"), 0);
			stopBits = Misc.getParamAsInt(prop.getProperty("BARRIER_COM_STOPBITS"), 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setConnected() {
		ConnectionManager.setWeighBridgeConnected(true);
	}

	public void setDisconnected() {
		ConnectionManager.setWeighBridgeConnected(false);
	}

	private void resetComm() {
		try {
			if (comport != null) {
				System.out.println("[WB]:reset");
				comport.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isConnected() {
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
					if (!TokenManager.isDebug)
						comport.setParams(baudrate, dataBits, stopBits, parity);
					else
						comport.setParams(9600, 8, 1, 0, true, true);
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

	Thread monitor = null;
	Thread t = null;

	public void stopWBThread() {
		try {
			if (t != null)
				t.interrupt();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startWeighBridge() {
		if (TokenManager.isDebug)
			startWeighBridgeThread();
		else
			startWeighBridgeEventBased();
	}

	public void startWeighBridgeThread() {
		stopWBThread();
		try {
			t = new Thread(new Runnable() {
				@Override
				public void run() {
					int dataLength = 32;
					while (isConnected()) {
						try {
							System.out.println("[COMM isRING]:" + comport.isRING());
							System.out.println("[COMM isOpened]:" + comport.isOpened());
							System.out.println("[COMM isCTS]:" + comport.isCTS());
							System.out.println("[COMM isDSR]:" + comport.isDSR());
							System.out.println("[COMM data]:" + comport.readBytes());
							byte buffer[] = comport.readBytes(dataLength);
							if (buffer == null)
								throw new RFIDException("Weigh Bridge disconnected");
							ThreadMonitor.stop(monitor);
							if (listener != null)
								listener.removeDisconnection();
							monitor = ThreadMonitor.start(TokenManager.weighBridgeTimeout, new InterruptListener() {
								@Override
								public void interrupt() {
									// TODO Auto-generated method stub
									if (listener != null)
										listener.showDisconnection();
								}
							});
							String message = new String(buffer);
							System.out.println("WB_DATA_STR" + message);
							if (!Utils.isNull(message)) {
								String result = null;
								for (int i = 0; i < message.length(); i++) {
									char c = message.charAt(i);
									result = getReadingFromString(c);
									if (!Utils.isNull(result) && result.length() > 0) {
										if (listener != null)
											listener.changeValue(comAddr, result);
										// LoggerNew.Write(result);
										// Logger.Write("[Weigh Bridge Serial]" + result);
										// onChange(result);
									}
								}
							}
						} catch (Exception ex) {
							setDisconnected();
							resetComm();
							if (listener != null) {
								listener.showDisconnection();
								System.out.println("[WB]:disconnected");
							}
						} finally {
							try {
								Thread.sleep(200);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			});
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startWeighBridgeEventBased() {
		try {
			if (isConnected()) {
				comport.addEventListener(this);
				ThreadMonitor.stop(monitor);
				if (listener != null)
					listener.removeDisconnection();
				monitor = ThreadMonitor.start(TokenManager.weighBridgeTimeout * 10, new InterruptListener() {

					@Override
					public void interrupt() {
						// TODO Auto-generated method stub
						if (listener != null)
							listener.showDisconnection();
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("No Data Found");
		}
	}

	public void stopWeighBridge() {
		if (comport != null && comport.isOpened())
			try {
				comport.closePort();
				if (TokenManager.isDebug) {
					stopWBThread();
				}
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		comport = null;
	}

	boolean isStart = false;
	boolean isEnd = false;
	boolean isFirst = true;
	String valStr = "";

	private String getReadingFromString(char c) {
		if (!isStart && Character.isDigit(c) && isFirst)
			return null;
		else {
			if (!isStart) {
				isStart = true;
				isEnd = false;
				valStr = "";
			}
			if (Character.isDigit(c)) {
				if (!isEnd) {
					valStr += c + "";
					// LoggerNew.Write("[Weigh Bridge Serial]-" + valStr);
				}
			} else {
				if (valStr.length() > 0) {

					isEnd = true;
					isStart = false;
					isFirst = false;
					return valStr;
				}
			}
		}
		return null;
	}

	public void setListener(WeighBridgeListener listener) {
		this.listener = listener;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		int dataLength = event.getEventValue();
		if (event.isRXCHAR()) {// If data is available

			if (dataLength > 0) {// Check bytes count in the input buffer
				// Read data, if 10 bytes available
				try {
					byte buffer[] = comport.readBytes(dataLength);
					if (buffer == null)
						return;
					ThreadMonitor.stop(monitor);
					if (listener != null)
						listener.removeDisconnection();
					monitor = ThreadMonitor.start(TokenManager.weighBridgeTimeout, new InterruptListener() {
						@Override
						public void interrupt() {
							// TODO Auto-generated method stub
							if (listener != null) {
								listener.showDisconnection();
								System.out.println("[WB]:disconnected");
							}
						}
					});
					String message = new String(buffer);
					System.out.println("WB_DATA_STR" + message);
					if (!Utils.isNull(message)) {
						String result = null;
						for (int i = 0; i < message.length(); i++) {
							char c = message.charAt(i);
							result = getReadingFromString(c);
							if (!Utils.isNull(result) && result.length() > 1) {
								if (listener != null)
									listener.changeValue(comAddr, result);
								// LoggerNew.Write(result);
								// Logger.Write("[Weigh Bridge Serial]" + result);
								// onChange(result);
							}
						}
					}
				}
				/*
				 * catch (SerialPortException ex) { setDisconnected(); ex.printStackTrace(); }
				 * catch (RFIDException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */catch (Exception ex) {
					setDisconnected();
					resetComm();
					listener.showDisconnection();
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

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub

	}
}

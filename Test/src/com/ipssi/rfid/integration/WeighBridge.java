package com.ipssi.rfid.integration;

import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.ConnectionStatusI;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralStatus;
import com.ipssi.rfid.readers.PeripheralConnectionStatus.PeripheralType;
import com.ipssi.rfid.readers.RFIDException;
import com.ipssi.rfid.ui.secl.controller.PropertyManager;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


public class WeighBridge implements SerialPortEventListener, InterruptListener {
	SerialPort comport = null;
	boolean _continue = false;
	Thread readThread = null;
	char stx = (char)2;
	char etx = (char)3;
	char cr = (char)13;
	char nl = (char)10;
	char space = (char)32;
	String comAddr; 
	int baudrate;
	int dataBits; 
	int parity;
	int stopBits;
	boolean isBusy = false;
	private boolean isEmpty = false;
	WeighBridgeListener listener;
	Object lock1 = new Object();
	Object lockEmptyWB = new Object();
	private ConnectionStatusI connectionStatusHandler = null;
	private int disconnetionMillis;
	private double dizitizerZero = 0.0;
	

	public static void main(String[] args){
		WeighBridge wb = new WeighBridge();
		wb.startWeighBridge();
	}
	boolean readEmpty = false;
	public boolean isEmpty() {
		if(TokenManager.isSimulateWB)
			return true;
		boolean retval = false;
		synchronized (lockEmptyWB) {
			try {
				while(readEmpty)
					lockEmptyWB.wait();
				retval = readEmpty;
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				lockEmptyWB.notifyAll();
			}
		}
		return retval;
	}

	public void setEmpty(boolean isEmpty) {
		synchronized (lockEmptyWB) {
			try {
				while(readEmpty)
					lockEmptyWB.wait();
				if(this.isEmpty != isEmpty){
					listener.isWBEmpty(isEmpty);;
				}
				this.isEmpty = isEmpty;
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				lockEmptyWB.notifyAll();
			}
		}
	}

	public WeighBridge(){
		try{
			//			ConfigUtility configUtility = new ConfigUtility();
			//			Properties prop = configUtility.getWeighBridgeConfiguration();
			Properties prop = PropertyManager.getProperty(PropertyType.WeighBridge);
			comAddr = Misc.getParamAsString(prop.getProperty("COM_PORT"), "COM9");
			baudrate = Misc.getParamAsInt(prop.getProperty("COM_BAUDRATE"), 2400);
			dataBits = Misc.getParamAsInt(prop.getProperty("COM_DATABITS"), 7);
			parity = Misc.getParamAsInt(prop.getProperty("COM_PARITY"), 0);
			stopBits = Misc.getParamAsInt(prop.getProperty("COM_STOPBITS"), 1);
			disconnetionMillis = Misc.getParamAsInt(prop.getProperty("DISCONNETION_MILLIS"), 1000);
			dizitizerZero  = Misc.getParamAsDouble(PropertyManager.getPropertyVal(PropertyType.System, ""),0.01);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void resetComm(){
		try{
			if(comport != null ){
				System.out.println("[WB]:reset");
				comport.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private Thread connMonitor = null;
	public void startMonitor(long millis){
		if(millis < 1000)
			millis = 30*1000;
		try{
			final long sleepTime = millis;
			stopMonitor();
			connMonitor = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true){
						try{
							if(connectionStatusHandler != null){
								connectionStatusHandler.setConnectionStatus(Misc.getUndefInt(), PeripheralType.WEIGHBRIDGE, isConnected() ? PeripheralStatus.CONNECTED : PeripheralStatus.DISCONNECTED);
							}
						}catch(Exception ex){
							ex.printStackTrace();
						}finally {
							notifyAll();
							try{
								Thread.sleep(sleepTime);
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
					}
				}
			});
			connMonitor.setName("Monitor Weigh Bridge");
			connMonitor.start();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void stopMonitor(){
		try{
			if(connMonitor != null)
				connMonitor.interrupt();
			connMonitor = null;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public boolean checkPort(){
		if(comport != null){
			try {
				return comport.writeByte((byte)1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public boolean isConnected()
	{
		boolean connTrue = false;
		synchronized (lock1) {
			synchronized (lock1) {
				try{

					while(isBusy)
						lock1.wait();
					isBusy = true;
					if(comport != null && !(comport.isOpened() && checkPort()) ){
						try{
							comport.removeEventListener();
							comport.closePort();
							comport = null;
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
					if(comport == null){
						comport = new SerialPort(comAddr);
						comport.openPort();
						if(!TokenManager.isDebug)
							comport.setParams(baudrate, dataBits, stopBits, parity);
						else
							comport.setParams(9600, 8, 1, 0, true, true);
						comport.addEventListener(this);
					}
					connTrue = true;
				}
				catch (Exception ex)
				{
					comport = null;
					ex.printStackTrace();
				}finally{
					isBusy = false;
					lock1.notifyAll();
				}
			}
			/*if(comport == null || !comport.isOpened()){
				if(connectionStatusHandler != null){
					connectionStatusHandler.setConnectionStatus(Misc.getUndefInt(), PeripheralType.WEIGHBRIDGE, PeripheralStatus.DISCONNECTED);
				}
			}*/
		}
		return connTrue;
	}
	Thread monitor = null;
	Thread t = null;
	public void stopWBThread(){
		try{
			if(t != null)
				t.interrupt();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void startWeighBridge(){
		if(TokenManager.isDebug)
			startWeighBridgeThread();
		else
			startWeighBridgeEventBased();
	}
	public void startWeighBridgeThread(){
		stopWBThread();
		try{
			t = new Thread(new Runnable() {
				@Override
				public void run() {
					int dataLength = 32;
					while(isConnected()){
						try {
							System.out.println("[COMM isRING]:"+comport.isRING());
							System.out.println("[COMM isOpened]:"+comport.isOpened());
							System.out.println("[COMM isCTS]:"+comport.isCTS());
							System.out.println("[COMM isDSR]:"+comport.isDSR());
							System.out.println("[COMM data]:"+comport.readBytes());
							byte buffer[] = comport.readBytes(dataLength);
							if(buffer == null)
								throw new RFIDException("Weigh Bridge disconnected");
							ThreadMonitor.stop(monitor);
							if(listener != null)
								listener.removeDisconnection();
							monitor = ThreadMonitor.start(TokenManager.weighBridgeTimeout,new InterruptListener() {
								@Override
								public void interrupt() {
									// TODO Auto-generated method stub
									if(listener != null)
										listener.showDisconnection();
								}
							});
							String message = new String(buffer);
							System.out.println("WB_DATA_STR"+message);
							if (!Utils.isNull(message))
							{
								String result = null;
								for (int i=0;i<message.length();i++)
								{
									char c = message.charAt(i);
									result = getReadingFromString(c);
									if (!Utils.isNull(result) && result.length() > 1)
									{
										if(listener != null)
											listener.changeValue(result);
										//LoggerNew.Write(result);
										//Logger.Write("[Weigh Bridge Serial]" + result);
										//onChange(result);
									}
								}
							}
						}catch(Exception ex){
							resetComm();
							if(listener != null){
								listener.showDisconnection();
								System.out.println("[WB]:disconnected");
							}
						}finally{
							try{
								Thread.sleep(200);
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
					}
				}
			});
			t.start();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void startWeighBridgeEventBased(){
		try
		{
			isConnected();
			/*if(isConnected()){
				ThreadMonitor.stop(monitor);
				if(listener != null)
					listener.removeDisconnection();
				monitor = ThreadMonitor.start(TokenManager.weighBridgeTimeout*10, new InterruptListener() {

					@Override
					public void interrupt() {
						// TODO Auto-generated method stub
						if(listener != null)
							listener.showDisconnection();
					}
				});
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("No Data Found");
		}
	}
	public  void stopWeighBridge(){
		if (comport != null && comport.isOpened())
			try {
				comport.closePort();
				if(TokenManager.isDebug){
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
	private String getReadingFromString(char c)
	{
		if (!isStart && Character.isDigit(c) && isFirst)
			return null;
		else
		{
			if (!isStart)
			{
				isStart = true;
				isEnd = false;
				valStr = "";
			}
			if (Character.isDigit(c))
			{
				if (!isEnd)
				{
					valStr += c + "";
					//LoggerNew.Write("[Weigh Bridge Serial]-" + valStr);
				}
			}
			else
			{
				if (valStr.length() > 0)
				{

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
		if(event.isRXCHAR()){//If data is available
			if(dataLength > 0){//Check bytes count in the input buffer
				//Read data, if 10 bytes available 
				try {
					byte buffer[] = comport.readBytes(dataLength);
					if(buffer == null)
						throw new RFIDException("Weigh Bridge disconnected");
					connectionStatusHandler.setConnectionStatus(Misc.getUndefInt(), PeripheralType.WEIGHBRIDGE, PeripheralStatus.CONNECTED);
					ThreadMonitor.stop(monitor);
					monitor = ThreadMonitor.start(disconnetionMillis, ()->{
						connectionStatusHandler.setConnectionStatus(Misc.getUndefInt(), PeripheralType.WEIGHBRIDGE, PeripheralStatus.DISCONNECTED);
					});
					String message = new String(buffer);
					System.out.println("WB_DATA_STR"+message);
					if (!Utils.isNull(message))
					{
						String result = null;
						for (int i=0;i<message.length();i++)
						{
							char c = message.charAt(i);
							result = getReadingFromString(c);
							if (!Utils.isNull(result) && result.length() > 1)
							{
								if(listener != null){
									listener.changeValue(result);
									double val = Misc.getParamAsDouble(result);
									setEmpty(!Misc.isUndef(val) && (val/1000) <= dizitizerZero);
								}
								//LoggerNew.Write(result);
								//Logger.Write("[Weigh Bridge Serial]" + result);
								//onChange(result);
							}
						}
					}
				}
				/*catch (SerialPortException ex) {
						setDisconnected();
						ex.printStackTrace();
					} catch (RFIDException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/catch(Exception ex){
						resetComm();
						if(listener != null){
							listener.showDisconnection();
							System.out.println("[WB]:disconnected");
						}
					}
			}

		}
		else if(event.isCTS()){//If CTS line has changed state
			if(event.getEventValue() == 1){//If line is ON
				System.out.println("CTS - ON");
			}
			else {
				System.out.println("CTS - OFF");
			}
		}
		else if(event.isDSR()){///If DSR line has changed state
			if(event.getEventValue() == 1){//If line is ON
				System.out.println("DSR - ON");
			}
			else {
				System.out.println("DSR - OFF");
			}
		}

	}
	@Override
	public void interrupt() {
		// TODO Auto-generated method stub

	}

	public void setConnectionStatusHandler(ConnectionStatusI mConnectionStatusHandler) {
		// TODO Auto-generated method stub
		connectionStatusHandler = mConnectionStatusHandler;
	}
}

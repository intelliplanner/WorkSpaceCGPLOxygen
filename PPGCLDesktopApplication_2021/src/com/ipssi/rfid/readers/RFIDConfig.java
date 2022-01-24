package com.ipssi.rfid.readers;

public class RFIDConfig {
	public static final int READER_TYPE_SERIAL = 0;
	public static final int READER_TYPE_TCPIP = 1;
	private String readerOneServer = "192.168.1.190";
	private int readerOnePort = 6000;
	private String readerTwoServer = "192.168.1.191";
	private int readerTwoPort = 6001;
	private String readerOneComm = "COM1";
	private String readerTwoComm = "COM2";
	private int readerOneConnectionType = READER_TYPE_TCPIP;
	private int readerTwoConnectionType = READER_TYPE_TCPIP;
	private String readerDesktopComm = "COM5";
	private boolean isReaderOneValid = true;
	private boolean isReaderTwoValid = false;
	private boolean isReaderDesktopValid = false;

	public boolean isReaderOneValid() {
		return isReaderOneValid;
	}

	public void setReaderOneValid(boolean isReaderOneValid) {
		this.isReaderOneValid = isReaderOneValid;
	}

	public boolean isReaderTwoValid() {
		return isReaderTwoValid;
	}

	public void setReaderTwoValid(boolean isReaderTwoValid) {
		this.isReaderTwoValid = isReaderTwoValid;
	}

	public boolean isReaderDesktopValid() {
		return isReaderDesktopValid;
	}

	public void setReaderDesktopValid(boolean isReaderDesktopValid) {
		this.isReaderDesktopValid = isReaderDesktopValid;
	}

	public static int getReaderTypeTcpip() {
		return READER_TYPE_TCPIP;
	}

	public String getReaderOneServer() {
		return readerOneServer;
	}

	public int getReaderOnePort() {
		return readerOnePort;
	}

	public String getReaderTwoServer() {
		return readerTwoServer;
	}

	public int getReaderTwoPort() {
		return readerTwoPort;
	}

	public String getReaderOneComm() {
		return readerOneComm;
	}

	public String getReaderTwoComm() {
		return readerTwoComm;
	}

	public int getReaderOneConnectionType() {
		return readerOneConnectionType;
	}

	public int getReaderTwoConnectionType() {
		return readerTwoConnectionType;
	}

	public String getReaderDesktopComm() {
		return readerDesktopComm;
	}

	public void setReaderOneServer(String readerOneServer) {
		this.readerOneServer = readerOneServer;
	}

	public void setReaderOnePort(int readerOnePort) {
		this.readerOnePort = readerOnePort;
	}

	public void setReaderTwoServer(String readerTwoServer) {
		this.readerTwoServer = readerTwoServer;
	}

	public void setReaderTwoPort(int readerTwoPort) {
		this.readerTwoPort = readerTwoPort;
	}

	public void setReaderOneComm(String readerOneComm) {
		this.readerOneComm = readerOneComm;
	}

	public void setReaderTwoComm(String readerTwoComm) {
		this.readerTwoComm = readerTwoComm;
	}

	public void setReaderOneConnectionType(int readerOneConnectionType) {
		this.readerOneConnectionType = readerOneConnectionType;
	}

	public void setReaderTwoConnectionType(int readerTwoConnectionType) {
		this.readerTwoConnectionType = readerTwoConnectionType;
	}

	public void setReaderDesktopComm(String readerDesktopComm) {
		this.readerDesktopComm = readerDesktopComm;
	}

}

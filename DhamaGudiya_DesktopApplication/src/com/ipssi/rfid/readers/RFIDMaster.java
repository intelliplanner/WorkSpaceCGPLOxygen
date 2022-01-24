package com.ipssi.rfid.readers;

import java.net.InetAddress;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.RFIDHolder;

public class RFIDMaster {

	public static RFIDReaderTCPClient[] tcpReaderlist;
	public static boolean isRunning = false;
	private static RFIDReaderSerialClient desktopReader;
	private static RFIDReaderSerialClient[] serialReaderlist;
	public static int READER_ONE_COMPORT = 1;
	public static int READER_TWO_COMPORT = 2;
	private static RFIDConfig cfg = null;

	public static RFIDConfig getConfig() {
		return cfg;
	}

	public static void init(RFIDConfig mConfig) {
		cfg = mConfig;
	}

	private static void load() throws RFIDException {
		if (cfg == null) {
			throw new RFIDException("Readers not initialize");
		} else {
			RFIDReaderTCPClient reader1, reader2;
			RFIDReaderSerialClient serialReader1, serialReader2;
			serialReader1 = new RFIDReaderSerialClient(cfg.getReaderOneComm(), 0);
			serialReader2 = new RFIDReaderSerialClient(cfg.getReaderTwoComm(), 1);
			reader1 = new RFIDReaderTCPClient(cfg.getReaderOneServer(), cfg.getReaderOnePort(), 0);
			reader2 = new RFIDReaderTCPClient(cfg.getReaderTwoServer(), cfg.getReaderTwoPort(), 1);
			tcpReaderlist = new RFIDReaderTCPClient[2];
			serialReaderlist = new RFIDReaderSerialClient[2];
			tcpReaderlist[0] = reader1;
			tcpReaderlist[1] = reader2;
			serialReaderlist[0] = serialReader1;
			serialReaderlist[1] = serialReader2;
		}
	}

	private static void loadDesktopReader() throws RFIDException {
		if (cfg == null) {
			throw new RFIDException("Readers not initialize.");
		} else if (desktopReader == null && cfg.isReaderDesktopValid()) {
			desktopReader = new RFIDReaderSerialClient(cfg.getReaderDesktopComm(), Misc.getUndefInt());
			if (desktopReader != null && !desktopReader.checkConnection()) {
				desktopReader = null;
			}
		}

	}

	public static RFIDReaderSerialClient getDesktopReader() throws RFIDException {
		if (desktopReader == null) {
			loadDesktopReader();
		}
		return desktopReader;
	}

	public static ReaderI getReader(int id) throws RFIDException {
		if (tcpReaderlist == null || serialReaderlist == null) {
			load();
		}
		int connectionType = id == 0 ? cfg.getReaderOneConnectionType() : cfg.getReaderTwoConnectionType();
		if (connectionType == RFIDConfig.READER_TYPE_SERIAL) {
			return serialReaderlist[id];
		} else {
			return tcpReaderlist[id];
		}
	}

	public static boolean StartRFIDReaders() throws RFIDException {
		boolean retval = false;
		if (tcpReaderlist == null || serialReaderlist == null) {
			load();
		}
		try {
			if (cfg.isReaderOneValid()) {
				if (cfg.getReaderOneConnectionType() == RFIDConfig.READER_TYPE_TCPIP) {
					serialReaderlist[0] = null;
					if (tcpReaderlist[0] != null) {
						/*
						 * if (IsConnectedToReader(cfg.getReaderOneServer())) { if
						 * (!tcpReaderlist[0].open()) tcpReaderlist[0] = null; }
						 */
						if (!tcpReaderlist[0].open()) {
							tcpReaderlist[0] = null;
						}
					}
				} else {
					tcpReaderlist[0] = null;
					if (serialReaderlist[0] != null) {
						if (!serialReaderlist[0].open()) {
							serialReaderlist[0] = null;
						}
					}
				}
			}
			if (cfg.isReaderTwoValid()) {
				if (cfg.getReaderTwoConnectionType() == RFIDConfig.READER_TYPE_TCPIP) {
					serialReaderlist[1] = null;
					if (tcpReaderlist[1] != null) {
						/*
						 * if (IsConnectedToReader(cfg.getReaderTwoServer())) { if
						 * (!tcpReaderlist[1].open()) tcpReaderlist[1] = null; } else tcpReaderlist[1] =
						 * null;
						 */
						if (!tcpReaderlist[1].open()) {
							tcpReaderlist[1] = null;
						}

					}
				} else {
					tcpReaderlist[1] = null;
					if (serialReaderlist[1] != null) {
						if (!serialReaderlist[1].open()) {
							serialReaderlist[1] = null;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public static void StopRFIDReaders() {
		if (tcpReaderlist != null) {
			if (tcpReaderlist[0] != null) {
				tcpReaderlist[0].close();
			}
			if (tcpReaderlist[1] != null) {
				tcpReaderlist[1].close();
			}
		}
		if (desktopReader != null) {
			desktopReader.close();
		}
	}

	public static boolean IsConnectedToReader(String readerIp) {
		boolean pingable = false;
		try {
			InetAddress inet = InetAddress.getByName(readerIp);
			pingable = inet.isReachable(1000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pingable;
	}

	public static void main(String[] arg) {
		try {
			RFIDMaster.init(new RFIDConfig());
			RFIDHolder hh1 = new RFIDHolder(1, "Vehicle1", 1, "2016-03-18 08:30", "ch2201", "lr2201", 1, 1, true);
			// RFIDHolder hh2 = new RFIDHolder(23453,"wb397751", 1, "2016-03-18 15:30",
			// "ch2202", "lr2202", 1, 1,true);
			// RFIDHolder hh3 = new RFIDHolder(23498,"jh10ag2098", 1, "2016-03-18 18:30",
			// "ch2203", "lr2203", 1, 1,true);
			// RFIDMaster.getReader(0).writeCardG2(hh1.createTag(0), 10);
			// RFIDMaster.getReader(0).writeCardG2(hh2.createTag(0), 10);
			// RFIDMaster.getReader(0).writeCardG2(hh1.createTag(0), 10);

			ArrayList<String> tags = RFIDMaster.getReader(0).getRFIDTagList();
			if (tags != null && tags.size() > 0) {
				hh1.setEpcId(tags.get(0));
				RFIDMaster.getReader(0).writeCardG2(hh1.createTag(0), 10);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

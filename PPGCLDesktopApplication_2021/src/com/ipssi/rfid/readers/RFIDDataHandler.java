package com.ipssi.rfid.readers;

import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.processor.TPRBlockManager;

public class RFIDDataHandler implements Runnable {

	Thread mThread = null;
	int refrehTime;
	private Object obj = new Object();
	RFIDDataProcessor rfidProcessor = null;
	private int readerId = 0;
	private boolean isRunning = false;

	public RFIDDataHandler(int refrehTime, int readerId, int workStationType, int workStationTypeId, int userId) {
		this.refrehTime = refrehTime;
		this.readerId = readerId;
		this.rfidProcessor = new RFIDDataProcessor(readerId, workStationType, workStationTypeId, userId);
	}

	public void setTagListener(TAGListener tagListener) {
		if (rfidProcessor != null)
			rfidProcessor.setTagListener(tagListener);
	}

	public void stopReadTagData() {
		if (rfidProcessor != null)
			rfidProcessor.stopReadTagData();
	}

	public void start() {
		RFIDConfig cfg = RFIDMaster.getConfig();
		if (cfg == null || (readerId == 0 ? !cfg.isReaderOneValid() : !cfg.isReaderDesktopValid()))
			return;
		stop();
		mThread = new Thread(this);
		mThread.start();
	}

	public void pause() {
		synchronized (obj) {
			try {
				if (rfidProcessor != null)
					rfidProcessor.wait();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void resume() {
		synchronized (obj) {
			try {
				if (rfidProcessor != null)
					rfidProcessor.notifyAll();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			isRunning = true;
		}
	}

	public void stop() {
		synchronized (obj) {
			try {
				if (mThread != null) {
					mThread.stop();
					mThread = null;
				}
				isRunning = false;

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		try {
			while (rfidProcessor != null && RFIDMaster.getReader(readerId) != null) {
				try {
					rfidProcessor.processTag();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					try {
						Thread.sleep(refrehTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Triple<Token, TPRecord, TPRBlockManager> getTprecord(String vehicleName) {
		return (rfidProcessor != null ? rfidProcessor.getTprecord(vehicleName) : null);
	}

	public void clearData(byte[] epc, int attempt) {
		if (rfidProcessor != null)
			rfidProcessor.clearData(epc, attempt);
	}
}

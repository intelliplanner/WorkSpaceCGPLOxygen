package com.ipssi.rfid.integration;

import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.readers.WeighBridgeTcp;

public class WeighmentReader implements Runnable {
	WeighmentReaderHandler weighmentReaderHandler = null;
	private int port=0;
	private String host = null;
	private boolean isRunning = false;
	Thread mThread = null;
	private long refreshRate = TokenManager.tprSyncFreq;// 10*60*1000;
	private Object lock = new Object();
	public WeighmentReader(int port,String host,WeighmentReaderHandler weighmentReaderHandler){
		this.weighmentReaderHandler=weighmentReaderHandler;
		this.port=port;
		this.host=host;
	}
	@Override
	public void run() {
		int retryCount = 0;
		int MAX_RETRY_COUNT = 3;
		while (isRunning) {
			WeighBridgeTcp weighBridgeTcp = new WeighBridgeTcp(host, port);
			// WeighBridgeTcp weighBridgeTcp = new WeighBridgeTcp("127.0.0.1",5000);
			boolean isConnected = weighBridgeTcp.getConnection();
			if (isConnected) {
				byte[] _weight = weighBridgeTcp.executeCommand("05");
				String wght = new String(_weight);
				System.out.println("Weightment: " + wght);
				weighmentReaderHandler.initWeight(wght);
			}else {
				weighmentReaderHandler.initWeight(null);
			}
		}
	}
	public void start() {
		stop();
		if (mThread == null) {
			mThread = new Thread(this);
			isRunning = true;
			mThread.start();
		} else {
			isRunning = true;
		}

	}

	public void stop() {
		synchronized (lock) {
			try {
				if (mThread != null) {
					// mThread.stop();
					mThread = null;
				}

				isRunning = false;

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}

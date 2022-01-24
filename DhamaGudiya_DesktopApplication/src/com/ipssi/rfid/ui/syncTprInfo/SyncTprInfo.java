/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.syncTprInfo;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.rfid.processor.TokenManager;
import java.sql.Connection;

/**
 *
 * @author IPSSI
 */
public class SyncTprInfo implements Runnable {

	private SyncTprServiceHandler handler = null;
	private boolean isRunning = false;
	Thread mThread = null;
	private long refreshRate = TokenManager.tprSyncFreq;// 10*60*1000;
	private Object lock = new Object();

	public SyncTprInfo(long refreshRate, SyncTprServiceHandler handler) {
		this.refreshRate = refreshRate;
		this.handler = handler;
	}

	public SyncTprInfo() {

	}

	public void setRefressRate(long refreshRate) {
		this.refreshRate = refreshRate;
	}

	public void setHandler(SyncTprServiceHandler handler) {
		this.handler = handler;
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

	@Override
	public void run() {
		try {
			while (isRunning) {
				Connection conn = null;
				boolean destroyIt = false;
				try {
					System.out.println("############# Sync Process Start ##############");
					conn = DBConnectionPool.getConnectionFromPoolNonWeb();
					SyncTprServiceHelper.getData(conn, handler);
					System.out.println("############# Sync Process End ##############");
				} catch (Exception ex) {
					ex.printStackTrace();
					destroyIt = true;
				} finally {
					try {
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
						Thread.sleep(refreshRate);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}

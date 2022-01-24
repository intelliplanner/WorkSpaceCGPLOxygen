package com.ipssi.rfid.integration;
public class ThreadMonitor implements Runnable {
	private final long timeout;
	private final InterruptListener listener;

	public static Thread start(final long timeout,final InterruptListener _listener) throws InterruptedException {
		System.out.println("[ThreadMonitor]:"+timeout);
		return start(Thread.currentThread(), timeout, _listener);	 	
	}

	public static Thread start(final Thread thread, final long timeout,final InterruptListener _listener) throws InterruptedException{
		Thread monitor = null;
		if (timeout > 0) {
			final ThreadMonitor timout = new ThreadMonitor(timeout,_listener);
			monitor = new Thread(timout, ThreadMonitor.class.getSimpleName());
			monitor.setDaemon(true);
			monitor.start();
		}
		return monitor;	 	
	}

	public static void stop(final Thread thread) {
		try{
			if (thread != null) {
				thread.interrupt();
			}	
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private ThreadMonitor(final long timeout,final InterruptListener _listener) {
		this.timeout = timeout;
		this.listener = _listener;
	}

	public void run() {
		try {
			Thread.sleep(timeout);
			listener.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}




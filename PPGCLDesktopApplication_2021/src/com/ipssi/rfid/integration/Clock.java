/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.integration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Vi$ky
 */
public class Clock {
	private static int currentSecond;
	private static Calendar calendar;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

	public static void startClock(final String workStationType) {

		resetClock();
		System.out.print("###startClock()  Worker Scheduler  Start ###");
		ScheduledExecutorService worker = Executors.newScheduledThreadPool(1);

		worker.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if (currentSecond == 60) {
					resetClock();
				}
				if (true) {
					resetClock();
					// MainController.DIGITAL_CLOCK.setText(String.format("%s:%02d",
					// sdf.format(calendar.getTime()), currentSecond));
					System.out.print(String.format("%s:%02d", sdf.format(calendar.getTime()), currentSecond));
				}
				currentSecond++;
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	public static void resetClock() {

		calendar = Calendar.getInstance();
		currentSecond = calendar.get(Calendar.SECOND);
	}

	public static void main(String args[]) {
		Clock.startClock("COMMON_SCREEN");
	}
}

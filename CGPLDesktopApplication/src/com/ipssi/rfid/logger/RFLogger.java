package com.ipssi.rfid.logger;

import com.ipssi.rfid.constant.PropertyManagerNew;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author nstephen
 */
public class RFLogger {

	// private static final String logDir = "C:" + File.separator + "ipssi" +
	// File.separator+ "properties" + File.separator + "log"+File.separator;
	private static final String logDir = PropertyManagerNew.BASE + "log" + File.separator;
	public static Logger logger;
	static {
		try {
			new File(logDir).mkdirs();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void init() {
		System.out.println("RFLogger.init()");
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();

		// log file max size 1MB, 3 rolling files, append-on-open
		Handler fileHandler = null;
		try {
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH").format(new Date());
			fileHandler = new FileHandler(logDir + timeStamp + ".log", 30 * 1024 * 1024, 1, true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileHandler.setFormatter(new SingleLineFormatter());
		Logger.getLogger("").addHandler(fileHandler);

	}

	public static void RouteStdOutErrToFile() {
		// now rebind stdout/stderr to logger
		System.out.println("RFLogger.RouteStdOutErr()");
		LoggingOutputStream los;

		logger = Logger.getLogger("stdout");
		los = new LoggingOutputStream(logger, StdOutErrLevel.STDOUT);
		System.setOut(new PrintStream(los, true));
		System.out.println("RFLogger.main()   called");
		logger = Logger.getLogger("stderr");
		los = new LoggingOutputStream(logger, StdOutErrLevel.STDERR);
		System.setErr(new PrintStream(los, true));
	}

	public static void main1(String[] args) throws Exception {

		// initialize logging to go to rolling log file
		/*
		 * LogManager logManager = LogManager.getLogManager(); logManager.reset();
		 * 
		 * // log file max size 1MB, 3 rolling files, append-on-open String filePath =
		 * "C:" + File.separator + "ipssi" + File.separator + "properties"+
		 * File.separator+ "log"+File.separator; Handler fileHandler = new
		 * FileHandler(filePath+File.separator+"log", 1000000, 3, true);
		 * fileHandler.setFormatter(new SimpleFormatter());
		 * Logger.getLogger("").addHandler(fileHandler);
		 */
		init();
		// preserve old stdout/stderr streams in case they might be useful
		PrintStream stdout = System.out;
		// PrintStream stderr = System.err;

		// now rebind stdout/stderr to logger
		/*
		 * Logger logger; LoggingOutputStream los;
		 * 
		 * logger = Logger.getLogger("stdout"); los = new LoggingOutputStream(logger,
		 * StdOutErrLevel.STDOUT); System.setOut(new PrintStream(los, true));
		 * System.out.println("Main.main()   called"); logger =
		 * Logger.getLogger("stderr"); los= new LoggingOutputStream(logger,
		 * StdOutErrLevel.STDERR); System.setErr(new PrintStream(los, true));
		 */
		RouteStdOutErrToFile();
		// show stdout going to logger
		System.out.println("Hello world!");

		// now log a message using a normal logger
		logger = Logger.getLogger("test");
		logger.info("This is a test log message");

		// now show stderr stack trace going to logger
		try {
			throw new RuntimeException("Test");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Main.main() Error happened");
		}

		// and output on the original stdout
		stdout.println("Hello on old stdout");
		try {
			throw new RuntimeException("Test 1 K files ");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(
					"Main.main() Error happened Main.main() Error happened Main.main() Error happenedMain.main() Error happened Main.main() Error happened Main.main() Error happened Main.main() Error happened Main.main() Error happened Main.main() Error happened ");
		}
		for (int i = 0; i < 100; i++) {
			String string = args[i];

		}
		logger = Logger.getLogger("test Logger");
		try {
			throw new RuntimeException("Test  1 K files second time");
		} catch (Exception e) {
			e.printStackTrace();
		}
		stdout.println("Hello again on old stdout");

	}

}

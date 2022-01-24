/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.constant.Type;

/**
 *
 * @author Vi$ky
 */
public class ConfigUtility {
	public static final String BASE = PropertyManagerNew.path;
	// String path = "C:" + File.separator + "ipssi" + File.separator + "properties"
	// + File.separator + "new_conn.property";
	// String screenListPath = "C:" + File.separator + "ipssi" + File.separator +
	// "properties" + File.separator + "screen_list.property";
	// String rfidConfigPath = "C:" + File.separator + "ipssi" + File.separator +
	// "properties" + File.separator + "RFIDConfig.property";
	// String barrierConfigPath = "C:" + File.separator + "ipssi" + File.separator +
	// "properties" + File.separator + "barrier.property";
	// String weighBridgeConfigPath = "C:" + File.separator + "ipssi" +
	// File.separator + "properties" + File.separator + "weighBridge.property";
	// private static String WorkStationpath = "C:" + File.separator + "ipssi" +
	// File.separator + "properties" + File.separator;

	String path = BASE + "new_conn.property";
	String screenListPath = BASE + "screen_list.property";
	String rfidConfigPath = BASE + "RFIDConfig.property";
	String barrierConfigPath = BASE + "barrier.property";
	String weighBridgeConfigPath = BASE + "weighBridge.property";
	private static String WorkStationpath = BASE;
	private File configFile = new File(path);
	private File logFile = new File(path);
	private File screenListFile = new File(screenListPath);
	private File rfidConfigFile = new File(rfidConfigPath);
	private File barrierConfigFile = new File(barrierConfigPath);
	private File weighBridgeConfigFile = new File(weighBridgeConfigPath);
	private static File workStationConfigFile = new File(WorkStationpath);
	// private Properties configProps;

	public void loadProperlies() throws FileNotFoundException, IOException {
		Properties defaultProps = new Properties();
		// sets default properties
		defaultProps.setProperty("desktop.DBConn.userName", "root");
		defaultProps.setProperty("desktop.DBConn.password", "root");
		defaultProps.setProperty("desktop.DBConn.host", "172.16.189.220");
		defaultProps.setProperty("desktop.DBConn.port", "3306");
		defaultProps.setProperty("desktop.DBConn.Database", "ipssi_cgpl");
		defaultProps.setProperty("desktop.DBConn.maxConnection", "5");

		if (!configFile.exists()) {
			File f = new File(path);
			f.getParentFile().mkdirs();
			f.createNewFile();
			FileOutputStream cfos = new FileOutputStream(path);
			defaultProps.store(cfos, "Desktop Application");
			cfos.close();
		}

	}

	public boolean setScreenList(Properties prop) throws IOException {
		boolean Success = false;
		if (!screenListFile.exists()) {
			File f = new File(screenListPath);
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		FileOutputStream cfos = new FileOutputStream(screenListPath);
		prop.store(cfos, "Selected Screen List For this System");
		Success = true;
		cfos.close();
		return Success;
	}

	public Properties loadScreenList() throws FileNotFoundException, IOException {
		Properties screenList = null;
		if (screenListFile.exists()) {
			screenList = new Properties();
			InputStream inputStream = new FileInputStream(screenListPath);
			screenList.load(inputStream);
			inputStream.close();
		}
		return screenList;
	}

	public Properties getWeighBridgeConfiguration() {
		Properties config = null;
		if (barrierConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(weighBridgeConfigPath);
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return config;
	}

	public Properties getBarrierConfiguration() {
		Properties config = null;
		if (barrierConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(barrierConfigPath);
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return config;
	}

	public static void loadWorkStationProperties(String suffix, int type) {
		try {
			Properties defaultProps = new Properties();
			// sets default properties
			int prev = Misc.getUndefInt();
			int next = Misc.getUndefInt();
			if (type > Type.WorkStationType.GATE_IN_TYPE) {
				prev = type - 1;
			}
			if (type < Type.WorkStationType.GATE_OUT_TYPE) {
				next = type + 1;
			}
			defaultProps.setProperty("WORK_STATION_TYPE", type + "");
			defaultProps.setProperty("WORK_STATION_ID", "1");
			defaultProps.setProperty("NEXT_WORK_STATION_TYPE", Misc.isUndef(next) ? "" : next + "");
			defaultProps.setProperty("PREV_WORK_STATION_TYPE", Misc.isUndef(prev) ? "" : prev + "");
			defaultProps.setProperty("MIN_TOKEN_GAP", "1800");
			defaultProps.setProperty("PRINTER_CONNECTED", "0");
			defaultProps.setProperty("WEIGHMENT_PRINTER_CONNECTED", "1");
			defaultProps.setProperty("REFRESH_INTERVAL", "10");
			defaultProps.setProperty("CREATE_NEW_TRIP", Type.WorkStationType.GATE_IN_TYPE == type ? "1" : "0");
			defaultProps.setProperty("CLOSE_TRIP", Type.WorkStationType.GATE_OUT_TYPE == type ? "1" : "0");
			defaultProps.setProperty("PORT_NODE_ID", "463");
			defaultProps.setProperty("MAXIMUM_TARE_DAYS", "0");
			defaultProps.setProperty("WEIGHT", "");
			workStationConfigFile = new File(WorkStationpath + suffix + "_configuration.property");
			// loads properties from file
			if (!workStationConfigFile.exists()) {
				File f = new File(WorkStationpath + suffix + "_configuration.property");
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream cfos = new FileOutputStream(WorkStationpath + suffix + "_configuration.property");
				defaultProps.store(cfos, Type.WorkStationType.getString(type));
				cfos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Properties getWorkStationConfiguration(String suffix, int type)
			throws FileNotFoundException, IOException {
		Properties config = null;
		workStationConfigFile = new File(WorkStationpath + suffix + "_configuration.property");
		if (workStationConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(WorkStationpath + suffix + "_configuration.property");
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			loadWorkStationProperties(suffix, type);
		}
		return config;
	}

	public static Properties getSystemConfiguration() throws FileNotFoundException, IOException {
		Properties config = null;
		workStationConfigFile = new File(WorkStationpath + "system_configuration.property");
		if (workStationConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(WorkStationpath + "system_configuration.property");
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			loadSystemProperties();
		}
		return config;
	}
	
	public static Properties getDBConfiguration() throws FileNotFoundException, IOException {
		Properties config = null;
		workStationConfigFile = new File(WorkStationpath + "new_conn.property");
		if (workStationConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(WorkStationpath + "new_conn.property");
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			loadDBProperties();
		}
		return config;
	}


	private static void loadDBProperties() {
		try {
			Properties defaultProps = new Properties();
			defaultProps.setProperty("desktop.DBConn.Database", "ipssi_ppgcl");
			defaultProps.setProperty("desktop.DBConn.host", "localhost");
			defaultProps.setProperty("desktop.DBConn.port", "3306");
			defaultProps.setProperty("desktop.DBConn.userName", "root");
			defaultProps.setProperty("desktop.DBConn.password", "root");
			defaultProps.setProperty("desktop.DBConn.maxConnection", "5");
			workStationConfigFile = new File(WorkStationpath + "new_conn.property");
			// loads properties from file
			if (!workStationConfigFile.exists()) {
				File f = new File(WorkStationpath + "new_conn.property");
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream cfos = new FileOutputStream(WorkStationpath + "new_conn.property");
				defaultProps.store(cfos, "new_conn");
				cfos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void loadSystemProperties() {
		try {
			Properties defaultProps = new Properties();
			defaultProps.setProperty("SYNC_CLOCK", "1");
			defaultProps.setProperty("SYSTEM_DATE_FORMAT", "");
			defaultProps.setProperty("DEBUG", "0");
			defaultProps.setProperty("CLOCK_SYNC_FREQ", "");

			workStationConfigFile = new File(WorkStationpath + "system_configuration.property");
			// loads properties from file
			if (!workStationConfigFile.exists()) {
				File f = new File(WorkStationpath + "system_configuration.property");
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream cfos = new FileOutputStream(WorkStationpath + "system_configuration.property");
				defaultProps.store(cfos, "system_configuration");
				cfos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Properties getReaderConfiguration() {
		Properties config = null;
		if (rfidConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(rfidConfigPath);
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			loadReaderProperties();
		}
		return config;
	}

	public static void loadReaderProperties() {
		try {
			Properties defaultProps = new Properties();
			defaultProps.setProperty("READER_ONE_TCP_IP", "192.168.1.190");
			defaultProps.setProperty("READER_ONE_TCP_PORT", "6000");
			defaultProps.setProperty("READER_TWO_TCP_IP", "192.168.1.191");
			defaultProps.setProperty("READER_TWO_TCP_PORT", "6001");
			defaultProps.setProperty("READER_ONE_COM", "COM3");
			defaultProps.setProperty("READER_TWO_COM", "COM2");
			defaultProps.setProperty("READER_ONE_CONN_TYPE", "0");
			defaultProps.setProperty("READER_TWO_CONN_TYPE", "0");
			defaultProps.setProperty("READER_DESKTOP_COM", "COM3");
			defaultProps.setProperty("READER_ONE_PRESENT", "1");
			defaultProps.setProperty("READER_TWO_PRESENT", "0");
			defaultProps.setProperty("READER_DESKTOP_PRESENT", "1");

			workStationConfigFile = new File(WorkStationpath + "RFIDConfig.property");
			// loads properties from file
			if (!workStationConfigFile.exists()) {
				File f = new File(WorkStationpath + "RFIDConfig.property");
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream cfos = new FileOutputStream(WorkStationpath + "RFIDConfig.property");
				defaultProps.store(cfos, "RFIDConfig");
				cfos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static Properties getWeighBridgeConfigurationNew()
			throws FileNotFoundException, IOException {
		Properties config = null;
		workStationConfigFile = new File(WorkStationpath + "weighBridge.property");
		if (workStationConfigFile.exists()) {
			InputStream inputStream = null;
			try {
				config = new Properties();
				inputStream = new FileInputStream(WorkStationpath + "weighBridge.property");
				config.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			loadWeighbridgeProperties();
		}
		return config;
	}
	private static void loadWeighbridgeProperties() {
		try {
			Properties defaultProps = new Properties();
			defaultProps.setProperty("WEIGHBRIDGE_ONE_PORT", "1");
			defaultProps.setProperty("WEIGHBRIDGE_TWO_PORT", "1");
			defaultProps.setProperty("WEIGHBRIDGE_THREE_PORT", "1");
			defaultProps.setProperty("WEIGHBRIDGE_FOUR_PORT", "1");
			defaultProps.setProperty("WEIGHBRIDGE_ONE_HOST", "192.168.1.190");
			defaultProps.setProperty("WEIGHBRIDGE_TWO_HOST", "192.168.1.191");
			defaultProps.setProperty("WEIGHBRIDGE_THREE_HOST", "192.168.1.190");
			defaultProps.setProperty("WEIGHBRIDGE_FOUR_HOST", "192.168.1.190");
			workStationConfigFile = new File(WorkStationpath + "weighBridge.property");
			// loads properties from file
			if (!workStationConfigFile.exists()) {
				File f = new File(WorkStationpath + "weighBridge.property");
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream cfos = new FileOutputStream(WorkStationpath + "weighBridge.property");
				defaultProps.store(cfos, "weighBridge");
				cfos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

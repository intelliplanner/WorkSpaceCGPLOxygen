package com.ipssi.rfid.constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Properties;

public class PropertyManagerNew {
	
	public static String path = "C:" + File.separator + "ipssi" + File.separator + "properties_dhamagudiya" + File.separator;
	
//	public static String path = "C:" + File.separator + "ipssi" + File.separator + "properties_cgpl" + File.separator;
	
	public static final String BASE = path;
	public static String RESOURCE_BASE = "/";

	static {
		try {
			new File(BASE).mkdirs();
			// copyDBUpdates();
			// System.out.println(new File("/new_conn.property").getAbsoluteFile());
			/*
			 * RESOURCE_BASE =
			 * MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI().
			 * toString().replace("file:/",""); if(RESOURCE_BASE != null &&
			 * RESOURCE_BASE.endsWith(".jar")){ RESOURCE_BASE = "jar:file://" +
			 * RESOURCE_BASE + "!//"; }
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static enum PropertyType {
		Systems, Database, RfidReader, WeighBridge, Barrier, Centric, UIConfig, DBUpdateSQL, WorkingHr, GateIn, GateOut, WbGross, WbTare; 
	}

	private static InputStream getResourceFileStream(PropertyType type) throws URISyntaxException {
		switch (type) {
		case Systems:
			// return RESOURCE_BASE + "system_configuration.property";
			return PropertyManagerNew.class.getResourceAsStream("/system_configuration.property");// .toURI().toString().replace("file:/","");
		case Database:
			// return RESOURCE_BASE + "new_conn.property";
			return PropertyManagerNew.class.getResourceAsStream("/new_conn.property");// .toURI().toString().replace("file:/","");
		case RfidReader:
			// return RESOURCE_BASE + "RFIDConfig.property";
			return PropertyManagerNew.class.getResourceAsStream("/RFIDConfig.property");// .toURI().toString().replace("file:/","");
		case WeighBridge:
			// return RESOURCE_BASE + "weighBridge.property";
			return PropertyManagerNew.class.getResourceAsStream("/weighBridge.property");// .toURI().toString().replace("file:/","");
		case Barrier:
			// return RESOURCE_BASE + "barrier.property";
			return PropertyManagerNew.class.getResourceAsStream("/barrier.property");// .toURI().toString().replace("file:/","");
		case Centric:
			// return RESOURCE_BASE + "centric.property";
			return PropertyManagerNew.class.getResourceAsStream("/centric.property");// .toURI().toString().replace("file:/","");
		case UIConfig:
			// return RESOURCE_BASE + "centric.property";
			return PropertyManagerNew.class.getResourceAsStream("/UIConfig.property");// .toURI().toString().replace("file:/","");
		case DBUpdateSQL:
			// return RESOURCE_BASE + "centric.property";
			return PropertyManagerNew.class.getResourceAsStream("/dbUpdates.sql");// .toURI().toString().replace("file:/","");
		case WorkingHr:
			// return RESOURCE_BASE + "centric.property";
			return PropertyManagerNew.class.getResourceAsStream("/workingHr.property");// .toURI().toString().replace("file:/","");
		case GateIn:
			// return RESOURCE_BASE + "new_conn.property";
			return PropertyManagerNew.class.getResourceAsStream("/GATE_IN_TYPE_configuration.property");// .toURI().toString().replace("file:/","");
		case GateOut:
			// return RESOURCE_BASE + "new_conn.property";
			return PropertyManagerNew.class.getResourceAsStream("/GATE_OUT_TYPE_configuration.property");// .toURI().toString().replace("file:/","");
		case WbTare:
			// return RESOURCE_BASE + "new_conn.property";
			return PropertyManagerNew.class.getResourceAsStream("/WEIGH_BRIDGE_OUT_TYPE_configuration.property");// .toURI().toString().replace("file:/","");
		case WbGross:
			// return RESOURCE_BASE + "new_conn.property";
			return PropertyManagerNew.class.getResourceAsStream("/WEIGH_BRIDGE_IN_TYPE_configuration.property");// .toURI().toString().replace("file:/","");
//		case ReadTag:
//			// return RESOURCE_BASE + "new_conn.property";
//			return PropertyManagerNew.class.getResourceAsStream("/READ_TAG_configuration.property");// .toURI().toString().replace("file:/","");

		default:
			return null;
		}
	}

	private static String getFileURL(PropertyType type) throws URISyntaxException {
		switch (type) {
		case Systems:
			return BASE + "system_configuration.property";
		// return
		// PropertyManagerNew.class.getResource("system_configuration.property").toURI().toString().replace("file:/","");
		case Database:
			return BASE + "new_conn.property";
		// return
		// PropertyManagerNew.class.getResource("/new_conn.property").toURI().toString().replace("file:/","");
		case RfidReader:
			return BASE + "RFIDConfig.property";
		// return
		// PropertyManagerNew.class.getResource("/RFIDConfig.property").toURI().toString().replace("file:/","");
		case WeighBridge:
			return BASE + "weighBridge.property";
		// return
		// PropertyManagerNew.class.getResource("/weighBridge.property").toURI().toString().replace("file:/","");
		case Barrier:
			return BASE + "barrier.property";
		// return
		// PropertyManagerNew.class.getResource("/barrier.property").toURI().toString().replace("file:/","");
		case Centric:
			return BASE + "centric.property";
		// return
		// PropertyManagerNew.class.getResource("/centric.property").toURI().toString().replace("file:/","");
		case UIConfig:
			return BASE + "UIConfig.property";
		case DBUpdateSQL:
			return BASE + "dbUpdates.sql";
		case WorkingHr:
			return BASE + "workingHr.property";
		case GateIn:
			return BASE + "GATE_IN_TYPE_configuration.property";
		case GateOut:
			return BASE + "GATE_OUT_TYPE_configuration.property";
		case WbTare:
			return BASE + "WEIGH_BRIDGE_OUT_TYPE_configuration.property";
		case WbGross:
			return BASE + "WEIGH_BRIDGE_IN_TYPE_configuration.property";
//		case ReadTag:
//			return BASE + "READ_TAG_configuration.property";
		default:
			return null;
		}
	}

	public static void setProperty(PropertyType type, String key, String val) {
		try {
			Properties props = getProperty(type);
			props.setProperty(key, val);
			File file = new File(getFileURL(type));
			if (file.exists()) {
				FileOutputStream cfos = new FileOutputStream(file);
				props.store(cfos, "");
				cfos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void copyDBUpdates() {
		InputStream inputStream = null;
		FileOutputStream out = null;
		try {
			File file = new File(BASE + "dbUpdates.sql");
			if (!file.exists() || true) {
				inputStream = getResourceFileStream(PropertyType.DBUpdateSQL);
				out = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = inputStream.read(buffer);
				Files.write(file.toPath(), "test".getBytes());
				while (len != -1) {
					out.write(buffer, 0, len);
					len = inputStream.read(buffer);
				}
				out.flush();
				out.close();
				inputStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void setUpdator() {
		InputStream inputStream = null;
		FileOutputStream out = null;
		try {
			File file = new File(BASE + "updator.jar");
			if (!file.exists() || true) {
				inputStream = PropertyManagerNew.class.getResourceAsStream("/updator.jar");
				out = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = inputStream.read(buffer);
				Files.write(file.toPath(), "test".getBytes());
				while (len != -1) {
					out.write(buffer, 0, len);
					len = inputStream.read(buffer);
				}
				out.flush();
				out.close();
				inputStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	synchronized public static Properties getProperty(PropertyType type) {
		Properties props = null;
		InputStream inputStream = null;
		FileOutputStream out = null;
		try {
			File file = new File(getFileURL(type));
			if (file.exists()) {
				props = new Properties();
				inputStream = new FileInputStream(file);
				props.load(inputStream);
			} else {
				props = new Properties();
				inputStream = getResourceFileStream(type);// new FileInputStream(getResourceFileURL(type));
				out = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = inputStream.read(buffer);
				Files.write(file.toPath(), "test".getBytes());
				while (len != -1) {
					out.write(buffer, 0, len);
					len = inputStream.read(buffer);
				}
				out.flush();
				out.close();
				inputStream.close();
				inputStream = getResourceFileStream(type);// new FileInputStream(getResourceFileURL(type));
				props.load(inputStream);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return props;
	}

	public static String getPropertyVal(PropertyType type, String key) {
		if (type == null || key == null) {
			return null;
		}
		Properties props = getProperty(type);
		if (!props.containsKey(key)) {
			setProperty(type, key, "");
		}
		return props.getProperty(key);
	}

	public static void main(String[] arg) {
		System.out.println(DatabaseProperty.desktop_DBConn_Database.toString());
		// System.out.println(DatabaseProperty.remote_DBConn_Database.toString());
	}

	public enum SystemProperty {
		PRINTER_ADDR, DEBUG, PRINT_ON_SAVE, DIZITIZER_ZERO_FOR_SAVE, CENTERED_FOR_SAVE, DIGITIZER_ZERO, SAME_STATION_MINUTES, SAP_USERNAME,SAP_PASSWORD,AUTO_COMPLETE_ON_OFF,TAG_READ_TYPE,PROJECT_AREA;
	}

	public enum DatabaseProperty {
		desktop_DBConn_host, desktop_DBConn_port, desktop_DBConn_Database, desktop_DBConn_userName, desktop_DBConn_password, desktop_DBConn_maxConnection;
		// remote_DBConn_host,
		// remote_DBConn_port,
		// remote_DBConn_Database,
		// remote_DBConn_userName,
		// remote_DBConn_password,
		// remote_DBConn_maxConnection;

		@Override
		public String toString() {
			return this.name().replaceAll("_", ".");
		}
	}

	public enum RfidReaderProperty {
		READER_ONE_PRESENT, READER_TWO_PRESENT, READER_DESKTOP_PRESENT, READER_ONE_CONN_TYPE, READER_ONE_COM, READER_ONE_TCP_IP, READER_ONE_TCP_PORT, READER_TWO_CONN_TYPE, READER_TWO_COM, READER_TWO_TCP_IP, READER_TWO_TCP_PORT, READER_DESKTOP_CONN_TYPE, READER_DESKTOP_COM, READER_DESKTOP_TCP_IP, READER_DESKTOP_TCP_PORT;
	}

	public static class WeighBridgeProperty {

		// private String PRESENT;
		private String BARRIER_COM_PORT;
		private String BARRIER_COM_BAUDRATE;
		private String BARRIER_COM_PARITY;
		private String BARRIER_COM_DATABITS;
		private String BARRIER_COM_STOPBITS;
		private String SIMULATE_WB;
		private String DISCONNETION_MILLIS;
		private String PING_INTERVAL;
	}

	public static class BarrierProperty {

		// private String PRESENT;
		private String BARRIER_COM_PORT;
		private String BARRIER_COM_BAUDRATE;
		private String BARRIER_COM_PARITY;
		private String BARRIER_COM_DATABITS;
		private String BARRIER_COM_STOPBITS;
		private String BARRIER_ENTRY_COMMAND;
		private String BARRIER_EXIT_COMMAND;
		// private String COMMAND_GAP;
	}

	public static class CentricProperty {

		private String PRESENT;
		private String COM_PORT;
		private String COM_BAUDRATE;
		private String COM_PARITY;
		private String COM_DATABITS;
		private String COM_STOPBITS;
		private String COMMAND;
	}

	public static class UIConfigProperty {

		private String MIN_TOKEN_GAP;
		private String CLOSE_TRIP;
		private String CREATE_NEW_TRIP;
		private String REFRESH_INTERVAL;
		private String WORK_STATION_ID;
		private String PREV_WORK_STATION_TYPE;
		private String NEXT_WORK_STATION_TYPE;
		private String WORK_STATION_TYPE;
		private String SAME_STATION_TPR_THRESHOLD;
	}

	public static class GateInProperty {

		private String MIN_TOKEN_GAP;
		private String CLOSE_TRIP;
		private String CREATE_NEW_TRIP;
		private String REFRESH_INTERVAL;
		private String WORK_STATION_ID;
		private String PREV_WORK_STATION_TYPE;
		private String NEXT_WORK_STATION_TYPE;
		private String WORK_STATION_TYPE;
		private String SAME_STATION_TPR_THRESHOLD;
		private String FORCE_MANUAL;
		private String GATE_PASS_PRINTER_CONNECTED;
	}

	public static class GateOutProperty {

		private String MIN_TOKEN_GAP;
		private String CLOSE_TRIP;
		private String CREATE_NEW_TRIP;
		private String REFRESH_INTERVAL;
		private String WORK_STATION_ID;
		private String PREV_WORK_STATION_TYPE;
		private String NEXT_WORK_STATION_TYPE;
		private String WORK_STATION_TYPE;
		private String SAME_STATION_TPR_THRESHOLD;
		private String FORCE_MANUAL;
	}

	public static class WBTareProperty {

		private String MIN_TOKEN_GAP;
		private String CLOSE_TRIP;
		private String CREATE_NEW_TRIP;
		private String REFRESH_INTERVAL;
		private String WORK_STATION_ID;
		private String PREV_WORK_STATION_TYPE;
		private String NEXT_WORK_STATION_TYPE;
		private String WORK_STATION_TYPE;
		// private String SAME_STATION_TPR_THRESHOLD;
		private String WEIGHT;
		private String MIN_WEIGHT;
		private String MAX_WEIGHT;
		private String FORCE_MANUAL;

	}

	public static class WBGrossProperty {

		private String MIN_TOKEN_GAP;
		private String CLOSE_TRIP;
		private String CREATE_NEW_TRIP;
		private String REFRESH_INTERVAL;
		private String WORK_STATION_ID;
		private String PREV_WORK_STATION_TYPE;
		private String NEXT_WORK_STATION_TYPE;
		private String WORK_STATION_TYPE;
		private String SAME_STATION_TPR_THRESHOLD;
		private String WEIGHT;
		private String MIN_WEIGHT;
		private String MAX_WEIGHT;
		private String FORCE_MANUAL;
	}
	

}

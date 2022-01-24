package com.ipssi.rfid.constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.WorkStationDetails;
import com.ipssi.rfid.beans.WorkStationProperties;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TokenManager;

/**
 * This utility class is responsible for loading the resources.properties file.
 * All the methods which is accessing the properties are also defined here.
 */
public class PropertyManager1 {

	private static final long serialVersionUID = 1L;
	/**
	 * Properties set for accessing the resources.properties file.
	 */
	public static String path = "C:" + File.separator + "ipssi" + File.separator + "properties_cgpl" + File.separator;
	private static Properties props = new Properties();

	public static int getWorkstationId(Connection conn, int workstationType) {
		Properties defaultProps = null;
		int retval = Misc.getUndefInt();
		try {
			File configFile = new File(path + "RFIDMaster.property");
			if (!configFile.exists()) {
				defaultProps = new Properties();
				retval = Misc.getUndefInt();
				WorkStationDetails workStation = new WorkStationDetails();
				workStation.setComments("system generated");
				workStation.setCreatedBy(TokenManager.userId);
				workStation.setName(Type.WorkStationType.getString(workstationType));
				workStation.setPortNodeId(TokenManager.portNodeId);
				RFIDMasterDao.insert(conn, workStation, false);
				retval = workStation.getId();
				defaultProps.setProperty("WORKSTATION_ID", retval + "");
				File f = new File(path + "RFIDMaster.property");
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream cfos = new FileOutputStream(path + "RFIDMaster.property");
				defaultProps.store(cfos, "main config file");
				cfos.close();
			} else {
				defaultProps = new Properties();
				InputStream inputStream = new FileInputStream(path + "RFIDMaster.property");
				defaultProps.load(inputStream);
				inputStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (defaultProps != null)
			retval = Misc.getParamAsInt(defaultProps.getProperty("WORKSTATION_ID"));
		return retval;
	}

	public static void init(Connection conn, int workstationType) {
		ArrayList<Object> list = null;
		try {
			if (props == null)
				props = new Properties();
			int workstationId = getWorkstationId(conn, workstationType);
			WorkStationProperties wsProps = new WorkStationProperties();
			wsProps.setWorkstationId(workstationId);
			wsProps.setWorkstationType(workstationType);
			list = RFIDMasterDao.select(conn, wsProps);
			props.setProperty("WORK_STATION_ID", workstationId + "");
			props.setProperty("WORK_STATION_TYPE", workstationType + "");
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					wsProps = (WorkStationProperties) list.get(i);
					props.setProperty(wsProps.getName(), wsProps.getValue());
				}
			} else {
				int next = Misc.getUndefInt();
				if (workstationType < Type.WorkStationType.GATE_OUT_TYPE) {
					next = workstationType + 1;
				}
				wsProps.setName("MIN_TOKEN_GAP");
				wsProps.setValue("1800");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);

				wsProps.setName("MORPHO_DEVICE_EXIST");
				wsProps.setValue(workstationType == Type.WorkStationType.GATE_IN_TYPE ? "1" : "0");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);

				wsProps.setName("PRINTER_CONNECTED");
				wsProps.setValue(workstationType == Type.WorkStationType.GATE_IN_TYPE ? "1" : "0");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);

				wsProps.setName("REFRESH_INTERVAL");
				wsProps.setValue("10");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);

				wsProps.setName("CREATE_NEW_TRIP");
				wsProps.setValue("1");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);

				wsProps.setName("NEXT_WORK_STATION_TYPE");
				wsProps.setValue(Misc.isUndef(next) ? "" : next + "");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);

				wsProps.setName("CLOSE_TRIP");
				wsProps.setValue(Type.WorkStationType.GATE_OUT_TYPE == workstationType ? "1" : "0");
				props.setProperty(wsProps.getName(), wsProps.getValue());
				RFIDMasterDao.insert(conn, wsProps, false);
			}
		} catch (Exception excp) {
			excp.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public static Properties getProperties() {
		return props;
	}

	/**
	 * This method reads the property & parses the obtained value to its
	 * <code>int</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose integer equivalent is required from property file.
	 * @return int Property value.
	 * @throws NumberFormatException
	 *             .
	 */
	public static int getInteger(String propertyName) throws NumberFormatException {
		return getInteger(propertyName, 0);
	}

	public static int getInteger(String propertyName, int defaultVal) throws NumberFormatException {
		return Misc.getParamAsInt(props.getProperty(propertyName), defaultVal);
	}

	/**
	 * This method reads the property & parses the obtained value to its
	 * <code>int</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose integer equivalent is required from property file.
	 * @return double Property value.
	 * @throws NumberFormatException
	 *             .
	 */
	public static double getDouble(String propertyName) throws NumberFormatException {
		return Double.parseDouble(props.getProperty(propertyName));
	}

	/**
	 * This method reads the property & parses the obtained value to its
	 * <code>long</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose <code>long</code> equivalent is required from
	 *            property file.
	 * @return long Property value.
	 * @throws NumberFormatException
	 *             .
	 */
	public static long getLong(String propertyName) throws NumberFormatException {
		return Long.parseLong(props.getProperty(propertyName));
	}

	/**
	 * This method reads the property & returns the value <code>String</code>.
	 * 
	 * @param propertyName
	 *            Property whose <code>String</code> equivalent is required from
	 *            property file.
	 * @return String Property value.
	 */
	public static String getString(String propertyName) {
		return props.getProperty(propertyName);
	}

	public static void main(String[] arg) {
		String t = getString("rfid.max_threshold");
	}

}

package com.ipssi.tracker.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;

/**
 * This utility class is responsible for loading the resources.properties file.
 * All the methods which is accessing the properties are also defined here.
 */
public class PropertyManager {

	/**
	 * Properties set for accessing the resources.properties file.
	 */
	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(PropertyManager.class);

	static {
		try {
			InputStream inputStream = PropertyManager.class.getClassLoader().getResourceAsStream("LocTracker.properties");
			//java.io.File fin = new java.io.File("C:\\IPSSI\\LocTracker\\resources\\LocTracker.properties");
			//java.io.FileInputStream inputStream = new java.io.FileInputStream(fin);
			props.load(inputStream);
			String serverName = System.getProperty("jboss.server.name");
			Misc.loadCFGConfigServerProp();
			props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));

			inputStream.close();			
		} catch (IOException ioExcp) {
			logger.error(ioExcp);
		} catch (Exception excp) {
			logger.error(excp);
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
	 *            Property whose integer equivalent is required from property
	 *            file.
	 * @return int Property value.
	 * @throws NumberFormatException .
	 */
	public static int getInteger(String propertyName) throws NumberFormatException {
		return com.ipssi.gen.utils.Misc.getParamAsInt(props.getProperty(propertyName));
	}

	/**
	 * This method reads the property & parses the obtained value to its
	 * <code>long</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose <code>long</code> equivalent is required from
	 *            property file.
	 * @return long Property value.
	 * @throws NumberFormatException .
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

	/**
	 * 
	 * @param propertyName
	 * @return
	 */
	public static boolean getBoolean(String propertyName) {
		return Boolean.parseBoolean(props.getProperty(propertyName));
	}

	/**
	 * 
	 * @return current date if property is not set or invalide
	 */
	public static Date getDate(String propertyName, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		try {
			return dateFormat.parse(props.getProperty(propertyName));
		} catch (Exception e) {
			// logger.error(null, e);
		}

		return new Date();
	}
}

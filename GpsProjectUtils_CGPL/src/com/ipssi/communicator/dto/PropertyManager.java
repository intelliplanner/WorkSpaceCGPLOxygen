package com.ipssi.communicator.dto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;

public class PropertyManager {

	private static final long serialVersionUID = 1L;
	/**
	 * Properties set for accessing the resources.properties file.
	 */
	private static Properties props = new Properties();

	static {
		try {
			InputStream inputStream = null;
			try {
			 inputStream = PropertyManager.class.getClassLoader().getResourceAsStream("RuleProcessor.properties");
			//java.io.File fin = new java.io.File("C:\\IPSSI\\RuleProcessor\\resources\\RuleProcessor.properties");
			//java.io.FileInputStream inputStream = new java.io.FileInputStream(fin);
			}
			catch (Exception e) {
				//eat it
			}
			try {
				if (inputStream == null) {
					//java.io.File fin = new java.io.File(Misc.CFG_CONFIG_SERVER+System.getProperty("file.separator")+"RuleProcessor.properties");
					java.io.File fin = new java.io.File(Misc.getServerConfigPath()+System.getProperty("file.separator")+"RuleProcessor.properties");
					inputStream = new java.io.FileInputStream(fin);				
				}
			}
			catch (Exception e) {
			//eat it
			}
			if (inputStream != null) 
				props.load(inputStream);
			Misc.loadCFGConfigServerProp();
			props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));
			inputStream.close();
		} 
		catch (Exception excp) {
			excp.printStackTrace();
		}
		finally {
			String serverName = Misc.getServerName();
			Misc.loadCFGConfigServerProp();
			props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));
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
	 * This method reads the property & parses the obtained value to its <code>int</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose integer equivalent is required from property file.
	 * @return int Property value.
	 * @throws NumberFormatException .
	 */
	public static int getInteger(String propertyName) throws NumberFormatException {
		return getInteger(propertyName, 0);
	}
	public static int getInteger(String propertyName, int defaultVal) throws NumberFormatException {
		return Misc.getParamAsInt(props.getProperty(propertyName), defaultVal);
	}

	/**
	 * This method reads the property & parses the obtained value to its <code>int</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose integer equivalent is required from property file.
	 * @return double Property value.
	 * @throws NumberFormatException .
	 */
	public static double getDouble(String propertyName) throws NumberFormatException {
		return Double.parseDouble(props.getProperty(propertyName));
	}

	/**
	 * This method reads the property & parses the obtained value to its <code>long</code> equivalent.
	 * 
	 * @param propertyName
	 *            Property whose <code>long</code> equivalent is required from property file.
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
	 *            Property whose <code>String</code> equivalent is required from property file.
	 * @return String Property value.
	 */
	public static String getString(String propertyName) {
		return props.getProperty(propertyName);
	}
}

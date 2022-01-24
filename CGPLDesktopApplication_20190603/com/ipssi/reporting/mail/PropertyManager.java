package com.ipssi.reporting.mail;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.ipssi.gen.utils.Misc;
public class PropertyManager {

	private static final long serialVersionUID = 1L;
	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(PropertyManager.class);
	static {
		try {
			java.io.File fin = new java.io.File(Misc.getServerConfigPath()+System.getProperty("file.separator")+"/AutoMailSender.properties");
			java.io.FileInputStream inputStream = new java.io.FileInputStream(fin);
			props.load(inputStream);
			inputStream.close();
		} catch (IOException ioExcp) {
			logger.error(ioExcp);
		} catch (Exception excp) {
			logger.error(excp);
		}
	}
	public static Properties getProperties() {
		return props;
	}
	public static int getInteger(String propertyName) throws NumberFormatException {
		return getInteger(propertyName, 0);
	}
	public static int getInteger(String propertyName, int defaultVal) throws NumberFormatException {
		return Misc.getParamAsInt(props.getProperty(propertyName), defaultVal);
	}
	public static double getDouble(String propertyName) throws NumberFormatException {
		return Double.parseDouble(props.getProperty(propertyName));
	}

	public static long getLong(String propertyName) throws NumberFormatException {
		return Long.parseLong(props.getProperty(propertyName));
	}
	public static String getString(String propertyName) {
		return props.getProperty(propertyName);
	}
}

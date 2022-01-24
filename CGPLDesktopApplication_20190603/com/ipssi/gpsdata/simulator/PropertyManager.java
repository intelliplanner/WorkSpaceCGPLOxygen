package com.ipssi.gpsdata.simulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;

/**
 * This utility class is responsible for loading the resources.properties file. All the methods which is accessing the properties are also defined here.
 */
public class PropertyManager {

	private static final long serialVersionUID = 1L;
	/**
	 * Properties set for accessing the resources.properties file.
	 */
	private static Properties props = new Properties();
	private static Logger logger = Logger.getLogger(PropertyManager.class);

	static {
		boolean toretry = false;
		String serverName = null;
		boolean oldVersion = false;
		//for old version
		if (oldVersion) {
			serverName = System.getProperty("jboss.server.name");		
		}
		//for new version
		if (!oldVersion) {
			serverName = Misc.getServerName();	
		}
		
	
		try {
			InputStream inputStream = PropertyManager.class.getClassLoader().getResourceAsStream("TcpServer.properties");
			//java.io.File fin = new java.io.File("C:\\IPSSI\\RuleProcessor\\resources\\RuleProcessor.properties");
			//java.io.FileInputStream inputStream = new java.io.FileInputStream(fin);
			props.load(inputStream);
			Misc.loadCFGConfigServerProp();
			props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));
			inputStream.close();
		} catch (IOException ioExcp) {
			toretry = true;
			logger.error(ioExcp);
		} catch (Exception excp) {
			toretry = true;
			logger.error(excp);
		}
		
		if (props.size() != 0 && !toretry) {
			//just check if the the thing is consistent
			String dirPath = props.getProperty("directory.path");
			if (dirPath == null || (System.getProperty("file.separator").equals("/") && !dirPath.contains("home"))) {
				toretry = true;
			}
		}
		if (props.size() == 0 || toretry) {
			toretry = false;
			props.clear();
			try {
				java.io.File fin = new java.io.File("/home/jboss/protocol/TcpServer.properties");
				java.io.FileInputStream inputStream = new java.io.FileInputStream(fin);
				
				props.load(inputStream);
				Misc.loadCFGConfigServerProp();			
				props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));

				inputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				toretry = true;
			}
		}
		if (props.size() == 0 || toretry) {
			toretry = false;
			props.clear();
			try {
				java.io.File fin = new java.io.File("G:\\Working\\EclipseWorkspace\\NewTcpServer\\protocol\\TcpServer.properties");
				//java.io.File fin = new java.io.File("C:\\Working\\EclipseWorkspace\\NewTcpServer\\protocol\\TcpServer.properties");
				java.io.FileInputStream inputStream = new java.io.FileInputStream(fin);
				props.load(inputStream);
				Misc.loadCFGConfigServerProp();			
				props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));

				inputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
				props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming");
				Misc.loadCFGConfigServerProp();
				props.setProperty("java.naming.provider.url", "jnp://localhost:"+Integer.toString(Misc.g_jniPort));
				props.setProperty("remote.jndi", "ConnectionFactory");
				props.setProperty("dataprocessor.queue", "queue/gpsQueue");
				props.setProperty("date.format", "yyyy-MM-dd HH:mm:ss");
				props.setProperty("directory.path", "C:\\Working\\EclipseWorkspace\\NewTcpServer\\protocol\\");
				props.setProperty("debug.mode", "1");
				
			}
		}
		
		//load any instance specific properties ... old
		
		
		boolean isdefault = "default".equalsIgnoreCase(serverName) || serverName == null;
		try {
    		String	fullFileName = Misc.getServerConfigPath()+System.getProperty("file.separator")+serverName+System.getProperty("file.separator")+"TcpServer.properties";
			java.io.FileInputStream inputStream = new java.io.FileInputStream(fullFileName);
			if (inputStream != null) {
				props.load(inputStream);
				inputStream.close();
			}
			else {
				throw new Exception("TcpPropertyManagerAddnlProps read");
			}
		}
		catch (Exception e) {
			e.printStackTrace();//eat it
			if (isdefault) {
				try {
					String	fullFileName = Misc.getServerConfigPath()+System.getProperty("file.separator")+"TcpServer.properties";
					java.io.FileInputStream inputStream = new java.io.FileInputStream(fullFileName);
					if (inputStream != null) {
						props.load(inputStream);
						inputStream.close();
					}
				}
				catch (Exception e2) {
					e2.printStackTrace();//eat it
				}
			}
		}
		
		//for backward compatability ... though should be moved
		Properties addnlProps = new Properties();
		try {
    		String	fullFileName = Misc.getServerConfigPath()+System.getProperty("file.separator")+serverName+System.getProperty("file.separator")+"tcp_addnl.properties";
			java.io.FileInputStream inputStream = new java.io.FileInputStream(fullFileName);
			if (inputStream != null) {
				addnlProps.load(inputStream);
				inputStream.close();
			}
			else {
				throw new Exception("TcpPropertyManagerAddnlProps read");
			}
		}
		catch (Exception e) {
			e.printStackTrace();//eat it
			if (isdefault) {
				try {
					String	fullFileName = Misc.getServerConfigPath()+System.getProperty("file.separator")+"tcp_addnl.properties";
					java.io.FileInputStream inputStream = new java.io.FileInputStream(fullFileName);
					if (inputStream != null) {
						addnlProps.load(inputStream);
						inputStream.close();
					}
				}
				catch (Exception e2) {
					e2.printStackTrace();//eat it
				}
			}
		}
		if (addnlProps != null && addnlProps.size() != 0) {}
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

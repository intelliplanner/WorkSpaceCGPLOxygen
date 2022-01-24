package com.ipssi.rfid.ui.secl.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Properties;

public class PropertyManager {
	public static final String BASE = "D:\\ipssi\\properties\\";
	public static String RESOURCE_BASE = "/";
	static{
		try{
			new File(BASE).mkdirs();
			
//			System.out.println(new File("/new_conn.property").getAbsoluteFile());
			/*RESOURCE_BASE = MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString().replace("file:/","");
			if(RESOURCE_BASE != null && RESOURCE_BASE.endsWith(".jar")){
				RESOURCE_BASE = "jar:file://" + RESOURCE_BASE + "!//";
			}*/
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static enum PropertyType{
		System,
		Database,
		RfidReader,
		WeighBridge,
		Barrier,
		Centric,
	}
	private static InputStream getResourceFileStream(PropertyType type) throws URISyntaxException{
		switch (type) {
		case System:
//			return RESOURCE_BASE + "system_configuration.property";
			return PropertyManager.class.getResourceAsStream("/system_configuration.property");//.toURI().toString().replace("file:/","");
		case Database:
//			return RESOURCE_BASE + "new_conn.property";
			return PropertyManager.class.getResourceAsStream("/new_conn.property");//.toURI().toString().replace("file:/","");
		case RfidReader:
//			return RESOURCE_BASE + "RFIDConfig.property";
			return PropertyManager.class.getResourceAsStream("/RFIDConfig.property");//.toURI().toString().replace("file:/","");
		case WeighBridge:
//			return RESOURCE_BASE + "weighBridge.property";
			return PropertyManager.class.getResourceAsStream("/weighBridge.property");//.toURI().toString().replace("file:/","");
		case Barrier:
//			return RESOURCE_BASE + "barrier.property";
			return PropertyManager.class.getResourceAsStream("/barrier.property");//.toURI().toString().replace("file:/","");
		case Centric:
//			return RESOURCE_BASE + "centric.property";
			return PropertyManager.class.getResourceAsStream("/centric.property");//.toURI().toString().replace("file:/","");
		default:
			return null;
		}
	}
	
	private static String getFileURL(PropertyType type) throws URISyntaxException{
		switch (type) {
		case System:
			return BASE+"system_configuration.property";
//			return PropertyManager.class.getResource("system_configuration.property").toURI().toString().replace("file:/","");
		case Database:
			return BASE+"new_conn.property";
//			return PropertyManager.class.getResource("/new_conn.property").toURI().toString().replace("file:/","");
		case RfidReader:
			return BASE+"RFIDConfig.property";
//			return PropertyManager.class.getResource("/RFIDConfig.property").toURI().toString().replace("file:/","");
		case WeighBridge:
			return BASE+"weighBridge.property";
//			return PropertyManager.class.getResource("/weighBridge.property").toURI().toString().replace("file:/","");
		case Barrier:
			return BASE+"barrier.property";
//			return PropertyManager.class.getResource("/barrier.property").toURI().toString().replace("file:/","");
		case Centric:
			return BASE+"centric.property";
//			return PropertyManager.class.getResource("/centric.property").toURI().toString().replace("file:/","");
		default:
			return null;
		}
	}
	public static void setProperty(PropertyType type,String key,String val) {
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
	public static void setUpdator(){
		InputStream inputStream = null;
		FileOutputStream out = null;
		try{
			File file = new File(BASE+"updator.jar");
			if (!file.exists()|| true) {
				inputStream = PropertyManager.class.getResourceAsStream("/updator.jar");
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
		} catch (Exception  ex) {
			ex.printStackTrace();
		} finally {
			try {
				if(inputStream != null)
					inputStream.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	public static Properties getProperty(PropertyType type){
		Properties props = null;
		InputStream inputStream = null;
		FileOutputStream out = null;
		try{
			File file = new File(getFileURL(type));
			if (file.exists()) {
				props = new Properties();
				inputStream = new FileInputStream(file);
				props.load(inputStream);
			}else{
				props = new Properties();
				inputStream = getResourceFileStream(type);//new FileInputStream(getResourceFileURL(type));
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
				inputStream = getResourceFileStream(type);//new FileInputStream(getResourceFileURL(type));
				props.load(inputStream);
				
			}
		} catch (Exception  ex) {
			ex.printStackTrace();
		} finally {
			try {
				if(inputStream != null)
					inputStream.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return props;
	}
	public static String getPropertyVal(PropertyType type,String key){
		Properties props = getProperty(type);
		return props == null ? null : props.getProperty(key);
	}
	public static void main(String[] arg){
		// prints Java Runtime Version before property set
		   System.out.print("Previous : ");
		   System.out.println(System.getProperty("SERVER" ));
		   System.setProperty("SERVER", "remote");
		     
		   // prints Java Runtime Version after property set
		   System.out.print("New : ");
		   System.out.println(System.getProperty("SERVER" ));
//		setProperty(PropertyType.System, "DEBUG", "0");
//		System.out.println(getPropertyVal(PropertyType.System, "DEBUG"));
	}
}

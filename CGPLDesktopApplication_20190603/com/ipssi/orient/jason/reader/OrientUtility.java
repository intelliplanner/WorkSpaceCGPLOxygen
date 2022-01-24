package com.ipssi.orient.jason.reader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;

public class OrientUtility {
	public static String CFG_ORIENT_VEHICLE_DATA_URL = "";
	public static String CFG_ORIENT_URL_KEY = ".orient.vehicle.data.url";
	public static String CFG_ORIENT_GRISH_URL_KEY = ".orient.vehicle.data.girish.url";
	public static String jsonDataURL = null;
	public static String jsonDataURLGrish = null;
	public static String tcpIP = null;
	public static int tcpPort ;
	public static int urlInterval;
	
	private static Properties prop = new Properties();
	public static List<OrientVehicleDataDTO> cachedDataList=new ArrayList<OrientVehicleDataDTO>();
	public static List<ShahTransDataDTO> cachedTGDataList=new ArrayList<ShahTransDataDTO>();
	public static Date g_now = null;
	
	static {
		prop = new Properties();
		String instanceName = Misc.getServerName();
		try {
			prop.load(new BufferedInputStream(new FileInputStream(Misc.CONN_PROPERTY)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		jsonDataURL = prop.getProperty(instanceName	+ CFG_ORIENT_URL_KEY);
		jsonDataURLGrish = prop.getProperty(instanceName	+ CFG_ORIENT_GRISH_URL_KEY);
		tcpIP = prop.getProperty(instanceName	+ ".orient.tcp.ip");
		tcpPort = Misc.getParamAsInt(prop.getProperty(instanceName	+ ".orient.tcp.port"),5405);
		urlInterval = Misc.getParamAsInt(prop.getProperty(instanceName	+ ".orient.interval"),30);
	}
	
	
	public static void sendToTcp() {
		String json;
		String modifiedJson;
		Socket clientSocket = null;
		g_now = new Date();
		List<OrientVehicleDataDTO> list=null;
		try {
//			clientSocket = new Socket("localhost", 5405);
			clientSocket = new Socket(OrientUtility.tcpIP,OrientUtility.tcpPort);
			
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			list=OrientUtility.cachedDataList;
			System.out.println("Start Sending data to TCP [count-"+ list.size() + "] at " + g_now);
			for (int i = 0; i < list.size(); i++) {
				OrientVehicleDataDTO obj = list.get(i);
				json =obj.toJson();
				outToServer.writeBytes(json + '\n');
//				System.out.println(json);
				Thread.sleep(100);
				modifiedJson = inFromServer.readLine();
				System.out.println("Recieved Ack from TCP="+modifiedJson);
//				if("ACK".equalsIgnoreCase(modifiedJson)){
					list.remove(i);
//				}else{
//					System.err.println("ACK Error ="+modifiedJson);
//				}
			}
			
			outToServer.close();
			
			g_now = new Date();
			System.out.println("End Sending Data At ["+g_now+"] Data Left in cache["+list.size()+"]");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				if(clientSocket!=null)
					clientSocket.close();
				list=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static String getOtherProperty(String name){
		return prop.getProperty(name);
	}
	
	public static List<OrientVehicleDataDTO> getVehicleJsonData() {
		g_now = new Date();
		System.out.println("Fetching Data from Orient, Previous not sent [count-"+ cachedDataList.size() + "] ");
		InputStream in = null;
		try{
			URL url = new URL(jsonDataURL);
			in = url.openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<Map<String,List<OrientVehicleDataDTO>>>() {}.getType();
			Map<String,List<OrientVehicleDataDTO>> json=gson.fromJson(reader,type);
			List<OrientVehicleDataDTO> dataList=json.get("detail_data");
			for (OrientVehicleDataDTO data : dataList) {
				cachedDataList.add(data);
				data.setVehicle_no(CacheTrack.standardizeNameNew(data.getVehicle_no()));
			}
			System.out.println("Got Koyal {"+dataList.size()+"}Vehicle(s) data ");
			in.close();
		}catch(Exception e){
			System.err.println("Exeption while fetching Koyal Data:-"+e.getMessage());
		}
		try{
			// second URL to get orient data
			URL url = new URL(jsonDataURLGrish);
			in = url.openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<Map<String,List<OrientVehicleDataDTO>>>() {}.getType();
			Map<String,List<OrientVehicleDataDTO>> json=gson.fromJson(reader,type);
			List<OrientVehicleDataDTO> dataList=json.get("detail_data");
			for (OrientVehicleDataDTO data : dataList) {
				cachedDataList.add(data);
				data.setVehicle_no(CacheTrack.standardizeName(data.getVehicle_no()));
			}
			System.out.println("Got Girish {"+dataList.size()+"}Vehicle(s) data ");
			in.close();
		}catch(Exception e){
			System.err.println("Exeption while fetching Girish Data:-"+e.getMessage());
		}
		return null;
	}


public static void sendTrackinggenieToTcp() throws IOException {
	String json;
	String modifiedJson;
	Socket clientSocket = null;
	g_now = new Date();
	List<ShahTransDataDTO> list=null;
	try {
		clientSocket = new Socket(OrientUtility.tcpIP,OrientUtility.tcpPort);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		list=OrientUtility.cachedTGDataList;
		System.out.println("Start Sending TG data to TCP [count-"+ list.size() + "] at " + g_now);
		for (int i = 0; i < list.size(); i++) {
			ShahTransDataDTO obj = list.get(i);
			json =obj.toJson();
			outToServer.writeBytes(json + '\n');
			Thread.sleep(100);
			modifiedJson = inFromServer.readLine();
			list.remove(i);
		}
		outToServer.close();
		g_now = new Date();
		System.out.println("End Sending TG Data At ["+g_now+"] Data Left in cache["+list.size()+"]");
	} catch (UnknownHostException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	} finally {
		try {
			if(clientSocket!=null)
				clientSocket.close();
			list=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public static void getTrackinggenieData(){
	System.out.println("Fetching Data from Trackinggenie, Previous not sent [count-"+ cachedTGDataList.size() + "] "); 
	try{
	URL url = new URL("http://locate.trackinggenie.com/trackingapi/vstatus.php");
	    Map<String,String> params = new LinkedHashMap<String,String>();
	    params.put("username", "STRANS");
	    params.put("password", "Naminath@21");
	    params.put("apitoken", "C5naTAYtm3v*B8+F");
	    StringBuilder postData = new StringBuilder();
	    for (Map.Entry<String,String> param : params.entrySet()) {
	        if (postData.length() != 0) postData.append('&');
	        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	        postData.append('=');
	        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	    }
	    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	    conn.setDoOutput(true);
	    conn.getOutputStream().write(postDataBytes);
	    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	    StringBuilder sb = new StringBuilder();
	    for (int c; (c = in.read()) >= 0;)
	        sb.append((char)c);
	    String response = sb.toString();
	    //System.out.println(response);
	    Gson gson = new GsonBuilder().create();
		Type type = new TypeToken<ShahTransWrapperDTO>() {}.getType();
		ShahTransWrapperDTO json=gson.fromJson(response,type);//List<ShahTransDataDTO>
		ArrayList<ShahTransDataDTO> dataList=json.getData();
		for(ShahTransDataDTO data : dataList) {
		cachedTGDataList.add(data);
	} 
		System.out.println("Got Trackinggenie {"+dataList.size()+"}Vehicle(s) data ");
		
	}catch(Exception e){
		System.err.println("Exeption while fetching Trackinggenie Data:-"+e.getMessage());
	}
}

public static void main(String[] args) {
	
	String modifiedJson;
	Socket clientSocket = null;
	g_now = new Date();
	List<ShahTransDataDTO> list=null;
	//String json="&ITP,MH18AA0846,22.93,86.0642,2018-11-30 18:16:40,2013-08-13 18:18:03,,,,,A#";
	String json="&ITP,TN01AX3281,15.7612916,76.3733083,2018-12-14 11:55:18,2018-12-14 11:55:21,43.0,,,A#";
//	String json="&ITP,KA32D0931,28.621797,77.215469,2018-12-08 05:19:46,2018-12-08 05:17:14,3.390186,0,,A&ITP,KA32D0931,28.621854,77.215401,2018-12-08 05:19:49,2018-12-08 05:17:14,3.17,0,,A&ITP,KA32D0931,28.621911,77.215333,2018-12-08 05:19:52,2018-12-08 05:17:14,3.66,0,,A&ITP,KA32D0931,28.621979,77.215227,2018-12-08 05:19:55,2018-12-08 05:17:14,3.55,0,,A&ITP,KA32D0931,28.62204,77.215136,2018-12-08 05:19:58,2018-12-08 05:17:14,3.95,0,,A&ITP,KA32D0931,28.622085,77.215068,2018-12-08 05:20:01,2018-12-08 05:17:14,2.9,0,,A&ITP,KA32D0931,28.622136,77.215,2018-12-08 05:20:04,2018-12-08 05:17:14,2.64,0,,A&ITP,KA32D0931,28.622168,77.214955,2018-12-08 05:20:07,2018-12-08 05:17:14,0.82,0,,A&ITP,KA32D0931,28.622168,77.21494,2018-12-08 05:20:10,2018-12-08 05:17:14,0,0,,A&ITP,KA32D0931,28.622185,77.214879,2018-12-08 05:20:13,2018-12-08 05:17:14,0.15,0,,A&ITP,KA32D0931,28.622213,77.214834,2018-12-08 05:20:16,2018-12-08 05:17:14,1.9,0,,A&ITP,KA32D0931,28.622241,77.214812,2018-12-08 05:20:19,2018-12-08 05:17:14,0.94,0,,A&ITP,KA32D0931,28.622248,77.214812,2018-12-08 05:20:22,2018-12-08 05:17:14,0,0,,A&ITP,KA32D0931,28.622268,77.214767,2018-12-08 05:20:25,2018-12-08 05:17:14,2.08,0,,A&ITP,KA32D0931,28.62229,77.214729,2018-12-08 05:20:28,2018-12-08 05:17:14,0.91,0,,A&ITP,KA32D0931,28.622328,77.214676,2018-12-08 05:20:31,2018-12-08 05:17:14,2.24,0,,A&ITP,KA32D0931,28.62235,77.214623,2018-12-08 05:20:34,2018-12-08 05:17:14,1.56,0,,A&ITP,KA32D0931,28.62237,77.214593,2018-12-08 05:20:37,2018-12-08 05:17:14,0.86,0,,A&ITP,KA32D0931,28.622398,77.214555,2018-12-08 05:20:40,2018-12-08 05:17:14,2.37,0,,A&ITP,KA32D0931,28.622434,77.214487,2018-12-08 05:20:43,2018-12-08 05:17:14,2.3,0,,A#";
	try {
		clientSocket = new Socket("203.197.197.18",5405);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		System.out.println("Start Sending Test data to TCP at " + g_now);
			outToServer.writeBytes(json + '\n');
			Thread.sleep(100);
			modifiedJson = inFromServer.readLine();
			System.out.println("Ack="+modifiedJson);
		outToServer.close();
		g_now = new Date();
		System.out.println("End Sending Test Data At ["+g_now+"]");
	} catch (UnknownHostException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	} finally {
		try {
			if(clientSocket!=null)
				clientSocket.close();
			list=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
//	 1=tr_dos_reports20054=-1.0|tr_dos_reports20055=0.0, 2=tr_dos_reports20056=0.0|tr_dos_reports20057=1.0
//	try {
		//jsonDataURL="http://202.143.97.118/jsp/Service_vehicle.jsp?user=koyal&pass=123456";
//		OrientUtility.getVehicleJsonData();
//		OrientUtility.startOrientTCPClient();
//		
//		System.out.println(CacheTrack.standardizeNameNew("9306MH18AA"));
//		System.out.println(CacheTrack.standardizeName("9306MH18AA"));
//		System.out.println(CacheTrack.standardizeNameNew("9306 MH 18 AA"));
//		System.out.println(CacheTrack.standardizeName("9306 MH 18 AA"));
//		
		/**
	Runnable runnable = new Runnable() {
		public void run() {
			try {
			OrientUtility.getVehicleJsonData();
			g_now = new Date();
			System.out.println("Start Sending data to TCP [count-"+ cachedDataList.size() + "] at " + g_now);
			OrientUtility.sentToTcp();			
			g_now = new Date();
			System.out.println("End Sending data to TCP [count-"+ cachedDataList.size() + "] at " + g_now);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	};
	ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	service.scheduleAtFixedRate(runnable, 0, urlInterval, TimeUnit.SECONDS);
	*/
}

//	private static void startOrientTCPClient() {
//		Runnable runnable = new Runnable() {
//			public void run() {
//				now = new Date();
//				System.out.println("Start Sending data to TCP [count-"+ cachedDataList.size() + "] at " + now);
//								
//				now = new Date();
//				System.out.println("End Sending data to TCP [count-"+ cachedDataList.size() + "] at " + now);
//			}
//		};
//		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
//		service.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.SECONDS);
//
//	}
	

}

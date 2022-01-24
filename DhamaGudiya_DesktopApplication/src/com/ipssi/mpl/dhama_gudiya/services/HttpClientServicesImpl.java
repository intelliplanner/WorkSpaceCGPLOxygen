/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.mpl.dhama_gudiya.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.ipssi.rfid.processor.TokenManager;

/**
 *
 * @author IPSSI
 */
public class HttpClientServicesImpl {

	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String STATUS_OK = "HTTP/1.1 200 OK";
	private static final String BASE_URL = "/LocTracker/insertRFIDMaithon.jsp";
	public static final int MINES = 40001;
	public static final int DONUMBER = 40002;
	public static final int TRANSPORTER = 40003;
	public static final int GRADES = 40004;
	public static final int PRIVILEGE_LIST = 40010;
	public static final int USER_LOGIN = 40011;

	public static String loginRequest(String username, String password) throws Exception {
		String URI = "http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL
				+ "?action=USER_LOGIN&object_id=" + USER_LOGIN + "&username=" + username + "&password=" + password;
		System.out.println("[ loginRequest URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e + ":Connection Error");
		}
		return response;
	}

	public static String getTransporter(int userId) {
		String URI = "http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=GET_CONSTANT&object_id=" + TRANSPORTER + "&user_id=" + userId;
		System.out.println("[ getTransporter URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return response;

	}

	public static String getDONumbers(int userId) {
		String URI ="http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=GET_CONSTANT&object_id=" + DONUMBER + "&user_id=" + userId
				+ "&device_id=-1111111";
		System.out.println("[ getDONumbers URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return response;
	}

	public static String getMinesData(int userId) {
		String URI ="http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=GET_CONSTANT&object_id=" + MINES + "&user_id=" + userId + "&device_id=1";
		System.out.println("[ getMinesData URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return response;
	}

	public static String getPrivlegeList(int userId) throws MalformedURLException, IOException {
		String URI = "http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=GET_CONSTANT&object_id=" + PRIVILEGE_LIST + "&user_id=" + userId
				+ "&device_id=1";
		// http://localhost:8080/LocTracker/insertRFIDMaithon.jsp?action=USER_LOGIN&username=3&password=3&object_id=40011
		System.out.println("[ getPrivlegeList URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return response;

	}

	public static String getGrades(int userId) {
		String URI ="http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=GET_CONSTANT&object_id=" + GRADES + "&user_id=" + userId
				+ "&device_id=-1111111";
		System.out.println("[ getGrades URI: " + URI +" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return response;

	}

	public static boolean sendDataToServer(String data, String epcId, String vehicleName, int deviceId, int userId)
			throws MalformedURLException, IOException {
		String URI = "http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=SET_RFID&data=" + data + "&epc_id=" + epcId + "&vehicle_name=" + vehicleName
				+ "device_id=" + deviceId + "user_id=" + userId;
		System.out.println("[ sendDataToServer URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return Integer.valueOf("1") == 1;

	}

	public static String getVehicleInformation(String vehicleName, String epcId) {
		String URI ="http://" + TokenManager.serverIp + ":" + TokenManager.serverPort + BASE_URL + "?action=GET_TPR_INFORMATION&vehicle_name=" + vehicleName + "&epc_id=" + epcId;// String
		System.out.println("[ getVehicleInformation URI: " + URI+" ]");
		String response = null;
		try {
			response = sendData(URI);
		} catch (Exception e) {
			System.out.println(e);
		}
		return response;
	}

	public static String sendData(String url) throws Exception {
		String URI = url;
		System.out.print("sendData() ");
		URL obj = null;
		StringBuffer response = null;
		String inputLine;
		int count = 1;

		HttpURLConnection client = null;
		while (count <= TokenManager.retryCount) {
			try {
				obj = new URL(URI);
				HttpURLConnection.setFollowRedirects(false);
				// HttpURLConnection.setConnectionTimeout(params, 10000);
				// HttpURLConnection.setSoTimeout(params, 10000);
				client = (HttpURLConnection) obj.openConnection();
				client.setRequestMethod("POST");
				client.setRequestProperty("User-Agent", USER_AGENT);
				client.setConnectTimeout(10000);
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				break;
			} catch (Exception e) {
				count++;
				if (count > TokenManager.retryCount) {
					throw new Exception();
				}
			}
		}
		System.out.println("Response: "+ response.toString());
		System.out.println("-----------------------------------");
		return response.toString();
	}
}

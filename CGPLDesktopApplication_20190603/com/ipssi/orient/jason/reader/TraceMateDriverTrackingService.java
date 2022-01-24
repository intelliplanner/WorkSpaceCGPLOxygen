package com.ipssi.orient.jason.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TraceMateDriverTrackingService {

	static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private static String userId = "intelli";
	private static String password = "123456a";
	private static String accName = "intelliPlanner";
	private static String concentToken = "YmQ4MmJjYTVlZWRmMTljMGFiOWY5NGE2ZWZhN2I1MGMxOTI5YmJlZTozMTYzMjg0NGJmYmEwMTI4MmRjMTFmNjQzNDRmZmE3ZDA0NjdiMTk0";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String concentToken = "YmQ4MmJjYTVlZWRmMTljMGFiOWY5NGE2ZWZhN2I1MGMxOTI5YmJlZTozMTYzMjg0NGJmYmEwMTI4MmRjMTFmNjQzNDRmZmE3ZDA0NjdiMTk0";
		 String token = getToken();

		String msisdn = "918130933688";
/*
		 addUser(msisdn, token);

		
		  String concentAuthToken=getUserConsentToken();
		  String concentStatus=getUserConsentStatus(msisdn,concentAuthToken);
		  if("concentAuthStatus".equalsIgnoreCase(concentStatus)){
		  System.out.println("Consent Status="+concentStatus); }else{
		  System.out.println("Consent Failed="+concentStatus); }
		 */
		locateUser(msisdn);
		// http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/trail-rest/locateme?acc=intelliPlanner&device=91**********&userid=intelli&password=123456a
	}

	public static double roundDown5(double d) {
		return ((long) (d * 1e2)) / 1e2;
	}

	public static Map<String, String> locateUser(String msisdn) {
		System.out.println("locateUserService..");
		Map<String, String> retVal=null;
		try {
			StringBuilder urlString = new StringBuilder();
			urlString.append("http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/trail-rest/locateme?acc=")
					.append(accName).append("&device=");
			urlString.append(msisdn).append("&userid=").append(userId)
			.append("&password=").append(password);
			System.out.println("LocateMe URL=" + urlString.toString());
			URL url = new URL(urlString.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", " */*");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			int responseCode = conn.getResponseCode();
			String responseMess = conn.getResponseMessage();
			System.out.println("headerFields= " + headerFields.toString());
			System.out.println("ResponseCode= " + String.valueOf(responseCode));
			System.out.println("ResponseMessage= " + responseMess);
			StringBuilder outputStringBuilder = new StringBuilder();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
					outputStringBuilder.append(line);
				bufferedReader.close();
//				"result":{"device"
				Gson gson = new GsonBuilder().create();
				Type type = new TypeToken<LocateDTO>() {}.getType();
				LocateDTO json = gson.fromJson(outputStringBuilder.toString(), type);
				retVal=json.getResult().get("device");
			} else {
				String response = "";
				String line;
				InputStream errorStream = conn.getErrorStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
				while ((line = br.readLine()) != null)
					response += line;
				System.out.println("Error Stream=" + response);
				outputStringBuilder.append(new String("false : " + responseCode	+ " " + responseMess));
			}
		//	System.out.println("Got Response= "	+ outputStringBuilder.toString());
			// return json.get("Consent").get("status");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	public static String getUserConsentStatus(String msisdn, String token) {
		try {
			URL url = new URL("http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/consentstatus?msisdn="+ msisdn);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			int responseCode = conn.getResponseCode();
			String responseMess = conn.getResponseMessage();
			InputStream errorStream = conn.getErrorStream();
//			System.out.println("headerFields= " + headerFields.toString());
//			System.out.println("ResponseCode= " + String.valueOf(responseCode));
//			System.out.println("ResponseMessage= " + responseMess);
			StringBuilder outputStringBuilder = new StringBuilder();
			Map<String, Map<String, String>> json=null;
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader bufferedReader = new BufferedReader(	new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
					outputStringBuilder.append(line);
				bufferedReader.close();
			} else {
				outputStringBuilder.append(new String("false : " + responseCode	+ " " + responseMess + " " + errorStream.toString()));
			}
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
			json = gson.fromJson(outputStringBuilder.toString(), type);
			System.out.println("Got Response= "	+ outputStringBuilder.toString());
			return json.get("Consent").get("status")==null?json.get("Failure").get("Desc"):json.get("Consent").get("status");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getUserConsentToken() {
		try {
			URL url = new URL("http://tracemateplus.airtel.in/oauth/token?grant_type=client_credentials");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",	"application/x-www-form-urlencoded");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Authorization", "Basic " + concentToken);
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			int responseCode = conn.getResponseCode();
			String responseMess = conn.getResponseMessage();
			InputStream errorStream = conn.getErrorStream();
//			System.out.println("headerFields= " + headerFields.toString());
//			System.out.println("ResponseCode= " + String.valueOf(responseCode));
//			System.out.println("ResponseMessage= " + responseMess);
			StringBuilder outputStringBuilder = new StringBuilder();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader bufferedReader = new BufferedReader(	new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
					outputStringBuilder.append(line);
				bufferedReader.close();
			} else {
				outputStringBuilder.append(new String("false : " + responseCode	+ " " + responseMess + " " + errorStream.toString()));
			}
			// {"access_token":"8b7e3ec4c6856849ce69a149b171b5b0","token_type":"BEARER","expires_in":3600}
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> json = gson.fromJson(outputStringBuilder.toString(), type);
//			System.out.println("Got Response= "	+ outputStringBuilder.toString());
			return json.get("access_token");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getToken() {
		String response = "";
		InputStream in = null;
		try {
			URL url = new URL("http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/inteltoken?userid=intelli&password=123456a");
			in = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> json = gson.fromJson(reader, type);
			String token = json.get("token");
			response = token;
//			System.out.println("Got Token=" + token);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public static ADDUserDTO addUser(String token,Map<String, String> driverDetail) {
		ADDUserDTO output =null;
		try {
			URL url = new URL("http://tracemateplus.airtel.in/trail-rest/entities/import");
			Map<String, String> params = new LinkedHashMap<String, String>();
			Map<String, ArrayList<Map<String, String>>> parent = new HashMap<String, ArrayList<Map<String, String>>>();
			ArrayList<Map<String, String>> driverList = new ArrayList<Map<String, String>>();
			driverList.add(driverDetail);
			parent.put("entityImportList", driverList);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
//			String list = "{'entityImportList': [{'firstName': 'Rajesh','lastName': 'Singh','msisdn': '918130933688'}]}";
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Length", String.valueOf(gson.toJson(parent).length()));
			conn.setRequestProperty("Token", token);
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
//			System.out.println("postJsonDataFinal =" + gson.toJson(parent));
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(gson.toJson(parent));
			wr.flush();
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			int responseCode = conn.getResponseCode();
			String responseMess = conn.getResponseMessage();
			InputStream errorStream = conn.getErrorStream();
//			System.out.println("postJsonData =" + list);
//			System.out.println("headerFields= " + headerFields.toString());
//			System.out.println("ResponseCode= " + String.valueOf(responseCode));
//			System.out.println("ResponseMessage= " + responseMess);
			StringBuilder outputStringBuilder = new StringBuilder();
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<ADDUserDTO>() {}.getType();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
					outputStringBuilder.append(line);
				bufferedReader.close();
				output = gson.fromJson(outputStringBuilder.toString(),	type);
			} else {
				outputStringBuilder.append(new String("false : " + responseCode	+ " " + responseMess + " " + errorStream.toString()));
				output = gson.fromJson(outputStringBuilder.toString(),	type);
			}
			System.out.println("addUser Response= "+ outputStringBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public class LocateDTO {
		//{"result":{"device":{"positiontime":"2018-12-08T11:34:27.654+05:30","msisdn":"918130933688","resourceName":"Rajesh Singh","latitude":"28.594145","longitude":"77.31583","address":"Near Puri Software Solutions Private Limited, Block B, Sector 7, Noida, Uttar Pradesh"}},
//		"error_code":0,"error_message":""}
		Map<String,Map<String,String>> result;
		int error_code;
		String error_message;
		public Map<String, Map<String, String>> getResult() {
			return result;
		}
		public void setResult(Map<String, Map<String, String>> result) {
			this.result = result;
		}
		public int getError_code() {
			return error_code;
		}
		public void setError_code(int error_code) {
			this.error_code = error_code;
		}
		public String getError_message() {
			return error_message;
		}
		public void setError_message(String error_message) {
			this.error_message = error_message;
		}
	}

	
	public class ADDUserDTO {
		UserDTO failureList[];
		UserDTO successList[];
		public UserDTO[] getFailureList() {
			return failureList;
		}
		public void setFailureList(UserDTO[] failureList) {
			this.failureList = failureList;
		}
		public UserDTO[] getSuccessList() {
			return successList;
		}
		public void setSuccessList(UserDTO[] successList) {
			this.successList = successList;
		}
	}

	public class UserDTO {
		String firstName;
		String lastName;
		String msisdn;
		String errorDesc;
		String errorSsmDetails;
		String status;
		String date;
		String id;
		String updateDate;
		public String getUpdateDate() {
			return updateDate;
		}
		public void setUpdateDate(String updateDate) {
			this.updateDate = updateDate;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		public String getMsisdn() {
			return msisdn;
		}
		public void setMsisdn(String msisdn) {
			this.msisdn = msisdn;
		}
		public String getErrorDesc() {
			return errorDesc;
		}
		public void setErrorDesc(String errorDesc) {
			this.errorDesc = errorDesc;
		}
		public String getErrorSsmDetails() {
			return errorSsmDetails;
		}
		public void setErrorSsmDetails(String errorSsmDetails) {
			this.errorSsmDetails = errorSsmDetails;
		}
	}
}

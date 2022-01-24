package com.ipssi.tracker.alert;

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


public class Test {
	static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private static String userId="intelli";
	private static String password="123456a";
	private static String accName="intelliPlanner";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String concentToken = "YmQ4MmJjYTVlZWRmMTljMGFiOWY5NGE2ZWZhN2I1MGMxOTI5YmJlZTozMTYzMjg0NGJmYmEwMTI4MmRjMTFmNjQzNDRmZmE3ZDA0NjdiMTk0";
		//String token = getToken();

		String msisdn = "918130933688";

		//addUser(msisdn, token);

		/*String concentAuthToken=getUserConsentToken(msisdn, concentToken);
		String concentStatus=getUserConsentStatus(msisdn,concentAuthToken);
		if("concentAuthStatus".equalsIgnoreCase(concentStatus)){
			System.out.println("Consent Status="+concentStatus);
		}else{
			System.out.println("Consent Failed="+concentStatus);
		}*/
		 locateUser(msisdn,  "");
		//http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/trail-rest/locateme?acc=intelliPlanner&device=91**********&userid=intelli&password=123456a
	}
	public static double roundDown5(double d) {
	    return ((long)(d * 1e2)) / 1e2;
	    //Long typecast will remove the decimals
	}

	public static String locateUser(String msisdn, String token){
		System.out.println("locateUser.."); 
		try{
			StringBuilder urlString=new StringBuilder();
			urlString.append("http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/trail-rest/locateme?acc=").append(accName).append("&device=");
			urlString.append(msisdn).append("&userid=").append(userId).append("&password=").append(password);
			System.out.println("LocateMe URL="+urlString.toString());
		URL url = new URL(urlString.toString());
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestMethod("GET");
		    conn.setRequestProperty("Accept", " */*");
		    conn.setUseCaches (false);
		    conn.setDoInput(true);
		    conn.setDoOutput(true);
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            int responseCode = conn.getResponseCode();
            String responseMess = conn.getResponseMessage();
            System.out.println("headerFields= "+ headerFields.toString());
            System.out.println("ResponseCode= "+ String.valueOf(responseCode));
            System.out.println("ResponseMessage= "+ responseMess);
            StringBuilder outputStringBuilder=new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    outputStringBuilder.append(line);
                }
                bufferedReader.close();
            }else {
            	String response = "";
            	String line;
            	 InputStream errorStream = conn.getErrorStream();
            	BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
            	while ((line = br.readLine()) != null)
            	    response += line;
            	System.out.println("Error Stream="+response);
                outputStringBuilder.append(new String("false : "+responseCode +" " + responseMess ));
            }
			System.out.println("Got Response= "+outputStringBuilder.toString());
//			return json.get("Consent").get("status");
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	
	public static String getUserConsentStatus(String msisdn, String token){
		System.out.println("getUserConsentStatus  msisdn.."); 
		try{
		URL url = new URL("http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/consentstatus?msisdn="+msisdn);
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestMethod("GET");
		    conn.setRequestProperty("Accept", "application/json");
		    conn.setRequestProperty  ("Authorization", "Bearer " + token);
		    conn.setUseCaches (false);
		    conn.setDoInput(true);
		    conn.setDoOutput(true);
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            int responseCode = conn.getResponseCode();
            String responseMess = conn.getResponseMessage();
            InputStream errorStream = conn.getErrorStream();
            System.out.println("headerFields= "+ headerFields.toString());
            System.out.println("ResponseCode= "+ String.valueOf(responseCode));
            System.out.println("ResponseMessage= "+ responseMess);
            StringBuilder outputStringBuilder=new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = bufferedReader.readLine()) != null)
                    outputStringBuilder.append(line);
                bufferedReader.close();
            }else {
                outputStringBuilder.append(new String("false : "+responseCode +" " + responseMess + " " + errorStream.toString()));
            }
            Gson gson = new GsonBuilder().create();
    		Type type = new TypeToken<Map<String,Map<String,String>>>() {}.getType();
    		Map<String,Map<String,String>> json=gson.fromJson(outputStringBuilder.toString(),type);
			System.out.println("Got Response= "+outputStringBuilder.toString());
			return json.get("Consent").get("status");
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getUserConsentToken(String msisdn, String token){
		System.out.println("getUserConsentToken  msisdn.."); 
		try{
		URL url = new URL("http://tracemateplus.airtel.in/oauth/token?grant_type=client_credentials");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestMethod("POST");
		    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		    conn.setRequestProperty("Accept", "application/json");
		    conn.setRequestProperty  ("Authorization", "Basic " + token);
		    conn.setUseCaches (false);
		    conn.setDoInput(true);
		    conn.setDoOutput(true);
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            int responseCode = conn.getResponseCode();
            String responseMess = conn.getResponseMessage();
            InputStream errorStream = conn.getErrorStream();
            System.out.println("headerFields= "+ headerFields.toString());
            System.out.println("ResponseCode= "+ String.valueOf(responseCode));
            System.out.println("ResponseMessage= "+ responseMess);
            StringBuilder outputStringBuilder=new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = bufferedReader.readLine()) != null)
                    outputStringBuilder.append(line);
                bufferedReader.close();
            }else {
                outputStringBuilder.append(new String("false : "+responseCode +" " + responseMess + " " + errorStream.toString()));
            }
            //{"access_token":"8b7e3ec4c6856849ce69a149b171b5b0","token_type":"BEARER","expires_in":3600}
    	    Gson gson = new GsonBuilder().create();
    		Type type = new TypeToken<Map<String,String>>() {}.getType();
    		Map<String,String> json=gson.fromJson(outputStringBuilder.toString(),type);//List<ShahTransDataDTO>
			System.out.println("Got Response= "+outputStringBuilder.toString());
			return json.get("access_token");
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getToken(){
		System.out.println("getting Token.. "); 
		String response ="";
		InputStream in = null;
		try{
			URL url = new URL("http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/inteltoken?userid=intelli&password=123456a");
			in = url.openStream(); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<Map<String,String>>() {}.getType();
			Map<String,String> json=gson.fromJson(reader,type);
			String token=json.get("token");
			response=token;
			System.out.println("Got Token="+token);
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return response ;
	}	
	
	public static void addUser(String msisdn, String token){
		System.out.println("addUser  msisdn.."); 
		try{
			URL url = new URL("http://tracemateplus.airtel.in/trail-rest/entities/import");
		    Map<String,String> params = new LinkedHashMap<String,String>();
		    Map<String, String> v=new HashMap<String, String>();
		    Map<String, ArrayList<Map<String, String>>> parent = new HashMap<String, ArrayList<Map<String, String>>>();
		    v.put("firstName","Rajesh");
		    v.put("lastName", "Singh");
		    v.put("msisdn", "918130933688");
		    ArrayList<Map<String, String>> l=new ArrayList<Map<String,String>>();
		    l.add( v);
		    parent.put("entityImportList", l);
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestMethod("POST");
		    String list="{'entityImportList': [{'firstName': 'Rajesh','lastName': 'Singh','msisdn': '918130933688'}]}";
		    conn.setRequestProperty("Content-Type", "application/json");
		    conn.setRequestProperty("Accept", "application/json");
		    conn.setRequestProperty("Content-Length", String.valueOf(gson.toJson(parent).length()));
		    conn.setRequestProperty("Token", token);
		    conn.setUseCaches (false);
		    conn.setDoInput(true);
		    conn.setDoOutput(true);
		    System.out.println("postJsonDataFinal ="+  gson.toJson(parent));
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(gson.toJson(parent));
		    wr.flush();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            int responseCode = conn.getResponseCode();
            String responseMess = conn.getResponseMessage();
            InputStream errorStream = conn.getErrorStream();
            System.out.println("postJsonData ="+ list);
            System.out.println("headerFields= "+ headerFields.toString());
            System.out.println("ResponseCode= "+ String.valueOf(responseCode));
            System.out.println("ResponseMessage= "+ responseMess);
            StringBuilder outputStringBuilder=new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) 
                    outputStringBuilder.append(line);
                bufferedReader.close();
            }else {
                outputStringBuilder.append(new String("false : "+responseCode +" " + responseMess + " " + errorStream.toString()));
            }
    	    Gson gson = new GsonBuilder().create();
    		Type type = new TypeToken<ADDUserDTO>() {}.getType();
    		ADDUserDTO json=gson.fromJson(outputStringBuilder.toString(),type);//List<ShahTransDataDTO>
			System.out.println("Got Response= "+outputStringBuilder.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	class ADDUserDTO{
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
//		String successToString(){
//			return "";
//		}
	}
	
	class UserDTO{
		String firstName;
		String lastName;
		String msisdn;
		String errorDesc;
		String errorSsmDetails;
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
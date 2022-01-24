package com.ipssi.rfid.sap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.processor.TokenManager;

/**
 *
 * @author joe666
 */
public class HttpPostRequest {
	private String redirectUrl = "http://localhost:8090/LocTracker/SingereniDashBoardData.jsp";
	TPRecord tprecord;
	HttpPostRequest(TPRecord tprecord) {
		this.tprecord = tprecord;
	}

	public static void main(String[] args) {
		TPRecord tpr = null;
		HttpPostRequest s = new HttpPostRequest(tpr);
		String str = s.httpClientPost();
		System.out.println(str);
	}

	private String httpClientPost() {
		String outputString = "";
		try {
			String URL = redirectUrl;
			String responseString = "";
			String wsURL = URL;
			URL url = new URL(wsURL);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			// ByteArrayOutputStream bout = new ByteArrayOutputStream();
			// JSONObject jsonObj = new JSONObject();

			JSONArray jsonArray = getFormatData();
			Map<String, Object> params = new LinkedHashMap<String, Object>();
			params.put("jsonData", jsonArray);
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.getOutputStream().write(postDataBytes);

			OutputStream out = httpConn.getOutputStream();
			// Write the content of the request to the outputstream of
			// the HTTP Connection.
			// out.write(b);
			InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			while ((responseString = in.readLine()) != null) {
				outputString = outputString + responseString;
			}
			out.close();
			in.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outputString;
	}

	private JSONArray getFormatData() {
			if(tprecord == null)
				return null;
			
			JSONObject jsonObj = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			
			try {
				jsonObj.put("TPR_ID", tprecord.getTprId());
				jsonObj.put("VBELN", tprecord.getTprId());
				jsonObj.put("FKIMG", tprecord.getAllowGrossTareDiffWB());
				jsonObj.put("TRANSPORTER", tprecord.getTransporterId());
				jsonObj.put("VNUMBER", tprecord.getVehicleName());
				jsonObj.put("SHIPTO", tprecord.getConsignee());
				jsonObj.put("HSN", TokenManager.HSN_NO);
				jsonObj.put("EMPTY_WT", tprecord.getUnloadTare());
				jsonObj.put("GROSS_WT", tprecord.getUnloadGross());
				jsonObj.put("INTIME", tprecord.getEarliestUnloadWbInEntry());
				jsonObj.put("OUTTIME", tprecord.getLatestUnloadWbInExit());

			} catch (JSONException e) {
				e.printStackTrace();
				System.out.println(e); 
			}

			return jsonArray;
		}

}
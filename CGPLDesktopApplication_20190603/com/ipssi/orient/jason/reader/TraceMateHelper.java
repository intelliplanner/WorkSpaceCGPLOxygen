package com.ipssi.orient.jason.reader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.orient.jason.reader.TraceMateDriverTrackingService.ADDUserDTO;
import com.ipssi.orient.jason.reader.TraceMateDriverTrackingService.UserDTO;

public class TraceMateHelper {

	public static void main(String[] args) {
/*		String time="2018-12-08T15:34:26.708+05:30";
		// yyyy-mm-ddTHH:mm:ss.mLocale
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d=null;
		try {
			d = sdf.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String formattedTime = output.format(d);
		System.out.println("formattedTime="+formattedTime);*/
		// String token = getToken();
		//String msisdn = "917835004444";//ipssi cust care
		String msisdn = "918826033688";//aditya
//		String msisdn = "917762902730";//sushant
		// addUser(msisdn, token);
		/*
		 * String concentAuthToken=getUserConsentToken(msisdn, concentToken);
		 * String concentStatus=getUserConsentStatus(msisdn,concentAuthToken);
		 * if("concentAuthStatus".equalsIgnoreCase(concentStatus)){
		 * System.out.println("Consent Status="+concentStatus); }else{
		 * System.out.println("Consent Failed="+concentStatus); }
		 */
		//locateUser(msisdn, "");
		// http://tracemateplus.airtel.in/apigw/airtel/smtrail/v1/trail-rest/locateme?acc=intelliPlanner&device=91**********&userid=intelli&password=123456a
		
		//registerDriver(msisdn,null,null);
		//getDriverConsent(msisdn,null,null);
		
		List<String> userList=new ArrayList();
		
		for (String val: userList) {
			Map<String,String> map=TraceMateHelper.getUserMap(val,null);
			String msgsim=TraceMateHelper.registerDriver(map,null,null);
//			confirmMessage.append("\n").append(msgsim);
		}
		
		
	}

	public static double roundDown5(double d) {
		return ((long) (d * 1e2)) / 1e2;
	}

	public static Map<String, String> getUserMap(String val,ArrayList<DimInfo.ValInfo> simUsersList){
		Map<String, String> driverDetail = new HashMap<String, String>();
		for (DimInfo.ValInfo valI: simUsersList) {
			if(val.equalsIgnoreCase(valI.m_id+"")){
				driverDetail.put("firstName",valI.m_name.split(" ")[0]);
				driverDetail.put("lastName", valI.m_name.split(" ")[1]);
				driverDetail.put("msisdn", valI.m_sn);
				break;
			}
		}
		return driverDetail;
	}
	public static String registerDriver(Map<String, String> driverDetail ,SessionManager session, HttpServletRequest request3) {
		String token = TraceMateDriverTrackingService.getToken();
		ADDUserDTO addResponse=TraceMateDriverTrackingService.addUser(token,driverDetail);
		StringBuilder response=new StringBuilder();
		if(addResponse!=null && addResponse.getSuccessList().length>0){
			UserDTO users[]=addResponse.getSuccessList();
			for (int i = 0; i < users.length; i++) {
				UserDTO user=users[i];
			addToSimUsersTable(user,session.getConnection());
			response.append("User Registered successfully. \n Name: ").append(user.getFirstName()).append(" "+user.getLastName()).append(", Mobile: "+user.getMsisdn()).append("\n");
			}
		}
		if(addResponse!=null && addResponse.getFailureList().length>0){
			response.append( "User Registration failed for following user(s) with reason. Try Again Later.\n");
			UserDTO users[]=addResponse.getFailureList();
			for (int i = 0; i < users.length; i++) {
				UserDTO userf=users[i];
				response.append("Name: ").append(userf.getFirstName()).append(" "+userf.getLastName()).append(", Mobile: "+userf.getMsisdn()).append(", Error: ").append(userf.getErrorDesc()).append(", Description: ").append(userf.errorSsmDetails).append("\n");
			}
		} 
			return response.toString();
	}

	private static void addToSimUsersTable(UserDTO user, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("insert into orient_sim_tracking_users(updated_on,first_name, last_name, " +
					"msisdn,consent_status,created_on) values (now(),?,?,?,0,now())");
			ps.setString(1, user.getFirstName());
			ps.setString(2, user.getLastName());
			ps.setString(3, user.getMsisdn());
			ps.executeUpdate();
			ps = Misc.closePS(ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getDriverConsent(String msisdn,SessionManager session, HttpServletRequest request3) {
		System.out.println("getDriverConsent MSISDN="+msisdn);
		String concentAuthToken = TraceMateDriverTrackingService.getUserConsentToken();
		String concentStatus=TraceMateDriverTrackingService.getUserConsentStatus(msisdn,concentAuthToken);
		 System.out.println("Consent Status="+concentStatus); 
		if ("ALLOWED".equalsIgnoreCase(concentStatus)|| "PENDING".equalsIgnoreCase(concentStatus)) {
			updateUserConsent(msisdn,concentStatus,session.getConnection());
			return "Sent Consent SMS to User successfully. Waiting for user's reply SMS with 'Y'";
		} else if ("FAILURE DUE TO NETWORK ISSUE".equalsIgnoreCase(concentStatus)) {
			return "Consent SMS to User failed[Service not available]. Try Again Later.";
		} else {
			return "Consent SMS to User failed["+concentStatus+"]. Try Again Later.";
		}
	}
	
	private static boolean updateUserConsent(String msisdn, String concentStatus,Connection conn) {
		PreparedStatement ps = null;
		int status="ALLOWED".equalsIgnoreCase(concentStatus)?1:"PENDING".equalsIgnoreCase(concentStatus)?2:0;
		try {
			ps = conn.prepareStatement("update orient_sim_tracking_users set consent_status=?,updated_on=now() where msisdn=? ");
			ps.setInt(1, status);
			ps.setString(2, msisdn);
			ps.executeUpdate();
			ps = Misc.closePS(ps);
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String locateUser(String msisdn,SessionManager session, HttpServletRequest request) {
		 Map<String, String> msg = TraceMateDriverTrackingService.locateUser(msisdn);
		return "";
	}
	
	private static int executeService=0;
	static{
		executeService=Misc.getParamAsInt(OrientUtility.getOtherProperty("node_lafarge.orient.sim.tracking.active"));
	}
	public static void locateUser(Connection conn) {
		System.out.println("locateUser..");
		if(executeService==0 || Misc.isUndef(executeService))
			return;
		ArrayList<Map<String, String>> locationList=new ArrayList<Map<String,String>>();
		ArrayList<UserDTO> simList=getSimTrackingUsers(conn,"1") ;//status consent allowed
		for (UserDTO userDTO : simList) {
			 Map<String, String> location = TraceMateDriverTrackingService.locateUser(userDTO.getMsisdn());
			 locationList.add(location);
		}
		sendSimTrackingDataToTcp(locationList,conn) ;
	}

	public static boolean sendSimTrackingDataToTcp(ArrayList<Map<String, String>> locationList,Connection conn) {
		//ArrayList<UserDTO> simList=new ArrayList<UserDTO>();
		String json;
		String modifiedJson;
		Socket clientSocket = null;
		try {
//			clientSocket = new Socket("203.197.197.18", 5405);
//			clientSocket = new Socket("localhost", 5205);
			clientSocket = new Socket(OrientUtility.tcpIP,OrientUtility.tcpPort);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//			System.out.println("Start Sending SimTracking data to TCP [count-"+ locationList.size() + "] at "+new Date().toString());
			for (int i = 0; i < locationList.size(); i++) {
				Map<String, String> obj = locationList.get(i);
				if(obj==null)
					continue;
				json =toJson(obj);
				outToServer.writeBytes(json + '\n');
				Thread.sleep(100);
				modifiedJson = inFromServer.readLine();
				System.out.println("Recieved Ack from TCP="+modifiedJson);
				
			}
			outToServer.close();
//			System.out.println("End Sending Data At ["+new Date()+"]");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if(clientSocket!=null)
					clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	
	public static String toJson(Map<String, String> obj) {
		String positiontime=obj.get("positiontime");
		return "&ITP," +obj.get("msisdn")+","+obj.get("latitude")+","+obj.get("longitude")+"," +formatDate(positiontime)+","+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+",,,,A#";
	}
	
	private static String formatDate(String input){
		DateFormat incoming = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
		Date date;
		try {
			date = (Date)incoming.parse(input);
			//2018-11-30 18:16:40
			SimpleDateFormat outgoing = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return outgoing.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static ArrayList<UserDTO> getSimTrackingUsers(Connection conn,String status) {
		ArrayList<UserDTO> simList=new ArrayList<UserDTO>();
		PreparedStatement ps =null;
		try {
			ps = conn.prepareStatement("select st.id,st.updated_on,st.first_name, st.last_name, st.msisdn,st.consent_status,st.created_on from orient_sim_tracking_users st  where  st.consent_status in ("+status+") order by  st.id ");
			ResultSet rs = ps.executeQuery();
	 	while( rs.next()){
	 		TraceMateDriverTrackingService.UserDTO user=new TraceMateDriverTrackingService().new UserDTO();
	 		user.setFirstName(Misc.getRsetString(rs, "first_name"));
	 		user.setLastName(Misc.getRsetString(rs, "last_name"));
	 		user.setMsisdn(Misc.getRsetString(rs, "msisdn"));
	 		user.setStatus(Misc.getRsetString(rs, "consent_status"));
	 		user.setDate(Misc.getRsetString(rs, "created_on"));
	 		user.setId(Misc.getRsetString(rs, "id"));
	 		user.setUpdateDate(Misc.getRsetString(rs, "updated_on"));
	 		simList.add(user);
	 	}
		}catch (Exception e) {
			e.printStackTrace();
			//throw e;
		}finally{
			Misc.closePS(ps);
		}
		return simList;
	}
}

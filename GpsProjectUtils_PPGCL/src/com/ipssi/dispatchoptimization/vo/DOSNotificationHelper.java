package com.ipssi.dispatchoptimization.vo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.communicator.dto.CommunicatorDTO;
import com.ipssi.communicator.dto.CommunicatorQueueSender;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Value;
import com.ipssi.report.cache.CacheValue;

public class DOSNotificationHelper {
	private static final int SMS = 1;
	private static final int EMAIL = 2;
	private static Map<Integer,AlertDefinition> defList=new HashMap<Integer,AlertDefinition>();
	
	private static Map<String,String> params=new HashMap<String,String>();
	//alert_def_id and port node id-> list of actions
	private static Map<Pair<Integer, Integer>,StringBuffer> actionList=new HashMap<Pair<Integer, Integer>,StringBuffer>();
	static{
		 updateAlertDefination();
		// updateUserDetailsAndAlertActions();
		 
		 params.put("82616","No Of Cycles(Shovel)");
			 params.put("82533","Idle Time(Shovel)" );
			 params.put("82554","Avg Load Lead(Dumper)" );
			 params.put("82556","Avg UnLoad Lead (Dumper)");
			 params.put("82622","Stoppage Duration with Window(Dumper)" );
			 params.put("82624","Stoppage at Load Site(Dumper)" );
			 params.put("82626","Stoppage at Unload Site(Dumper)" );
			 params.put("82628","Frwd Avg Speed" );
			 params.put("82680","Bkwd Avg Speed(Dumper)" );
			 params.put("82682","Wait After Unload Gate Out(Dumper)" );
			 params.put("82684","Shovel Queue waiting" );
			 params.put("82686","LoadGateOut-Adjusted LoadGateOut Duration(Dumper)" );
			 params.put("82688","Stoppage without Window(Dumper)" );
			
			
	}
	public static  AlertDefinition getAlertDefinition(Integer defId) {
		if(defList==null || !defList.containsKey(defId)){
			updateAlertDefination();
		}
		return defList.get(defId);
	}
	public static  ArrayList<AlertDefinition> getAlertDefinitionList() {
		if(defList==null )
			updateAlertDefination();
		return new ArrayList<AlertDefinition>(defList.values());
	}
	public static  String getAlertParamString(Integer dimId) {
		return params.get(dimId);
	}
	
	public static  String getAlertDefinitionString(DOSAlertDTO alert) {
		return getMessageFromDefinition(getAlertDefinition(alert.getAlertDefId()),alert);
	}
	public static void sendSMSNotification(String contactList, String msz,
			String vehicleName,int vehicleId, DOSAlertDTO alertDto) {
		CommunicatorDTO commDTO = new CommunicatorDTO();
		 System.out.println("sendSMSNotification.msg="+msz+",contactList="+contactList+",vehicleName="+vehicleName);
		try {
			String[] mobileNumberList = contactList.split(",");
			for (int i = 0; i < mobileNumberList.length; i++) {
				commDTO.setBody(msz);
				commDTO.setNotificationType(SMS);
				commDTO.setTo(mobileNumberList[i]);
				commDTO.setForceSend(true);
				commDTO.setVehicleId(vehicleId);// "ENTER VEHICLE ID"
				commDTO.setAlertIndex(0);
				commDTO.setRuleId((20000+alertDto.getAlertDefId()));//AlertDef + 20000
				commDTO.setEngineEventId(alertDto.getAlertId());//AlertID
				try {
					CommunicatorQueueSender.send(commDTO);
				} catch (Exception e) {
					System.out.println("Error in DOSNotificationHelper.SMSNotification while send message to queue "+ e);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			
		}	
	}

	public static void sendEMailNotification(String emailList, String msz,
			String vehicleName,int vehicleId, DOSAlertDTO alertDto, AlertDefinition def) {
		 System.out.println("sendSMSNotification.msg="+msz+",contactList="+emailList+",vehicleName="+vehicleName);
		try {
			String[] emailAddressList = emailList.split(",");
			for (int i = 0; i < emailAddressList.length; i++) {
				CommunicatorDTO commDTO = new CommunicatorDTO();
				commDTO.setNotificationType(EMAIL);
				commDTO.setTo(emailAddressList[i]);
				commDTO.setBody(msz);
				commDTO.setForceSend(true);
				commDTO.setVehicleId(vehicleId);// vehicle ID
				commDTO.setAlertIndex(0);
				commDTO.setRuleId((20000+alertDto.getAlertDefId()));
				commDTO.setSubject("Alert: "+vehicleName+" on "+params.get(def.getDim_id()));
				commDTO.setEngineEventId(alertDto.getAlertId());
				System.out.println("DOSNotificationHelper.sendEMailNotification() :: Sending out Notification :: "+ commDTO.getBody());
				try {
					CommunicatorQueueSender.send(commDTO);
				} catch (Exception e) {
					System.out.println("Error in DOSNotificationHelper.sendEMailNotification while send message to queue "+ e);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			
		}	
	}

	

	public static void sendNotification(DOSAlertDTO alert) {
		if(defList==null || !defList.containsKey(alert.getAlertDefId())){
			updateAlertDefination();
		}
		DOSNotificationHelper.AlertDefinition def=defList.get(alert.getAlertDefId());
		ArrayList<DOSNotificationHelper.UserDetails> list= updateUserDetailsAndAlertActions(alert.getAlertDefId());
		String msg=getMessageFromDefinition(def,alert);
		String vName=getDimValue(alert.getVehicle_id(), 9003).getStringVal();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			UserDetails userDetails = (UserDetails) iterator.next();
			int action=userDetails.getAction();
			/*<d82669>
			 <val id="0" name="Perform All Actions");
			 <val id="1" name="SMS" );
			<val id="2" name="E-Mail" );
			<val id="3" name="App Notification");
			<val id="4" name="GUI Notification" );
			<val id="5" name="SMS and E-Mail Only" );
			<val id="6" name="App and GUI Notification Only" );
		</d82669>*/
			if(action==1 || action==5 || action==0){//SMS
				sendSMSNotification(userDetails.getMobile(), msg, vName,alert.getVehicle_id(),alert);
			}
			if (action==2 || action==5 || action==0){//E-Mail
				sendEMailNotification(userDetails.getEmail(), msg, vName,alert.getVehicle_id(),alert,def);
			}
			if(action==3 || action==6 || action==0){//App Notification
				//App Notification
			}
			if (action==4 || action==6 || action==0){//GUI Notification
				//GUI Notification
			}
			
		}
		
	}
	
	
	private static String getMessageFromDefinition(AlertDefinition def, DOSAlertDTO alert) {
	 StringBuffer sb=new StringBuffer();
	 String vName=getDimValue(alert.getVehicle_id(), 9002).getStringVal();
	 String paramName=params.get(def.getDim_id()+"");
	 String vType=(def.getVehicle_type()==3||def.getVehicle_type()==4)?" Dumper ":def.getVehicle_type()==65?" Shovel ":" Unknown Vehicle ";
	 sb.append(def.getAlert_name()).append(" Alert :").append(vType).append(vName).append(" has breached ").append(paramName).append(" value, Expected ")
	 .append(def.getExpected_value()).append(" and current value(s) :( ").append((alert.getDebugString()==null || "".equalsIgnoreCase(alert.getDebugString()))?alert.getParam_value():alert.getDebugString())
	 .append(" )");
	 return sb.toString();
	}
		
	private static Value getDimValue(int vId, int dimId){
		Connection conn = null;
		boolean destroyIt = false;
		Value val = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vId, conn);
			VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(conn, vId,	false, false);
			val = CacheValue.getValueInternal(conn, vId, dimId, vehSetup, vdf);
		} catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}finally {
			try {
				if(conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		return val==null?new Value("0"):val;
	}

	private static void updateAlertDefination() {
		Connection conn = null;
		boolean destroyIt = false;
		PreparedStatement ps =null;
		ResultSet rs = null;
		defList=new HashMap<Integer, AlertDefinition>();
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement("select * from dos_alerts_settings where status = 1");
			rs = ps.executeQuery();
	 	while( rs.next()){
	 		DOSNotificationHelper.AlertDefinition def=new DOSNotificationHelper().new AlertDefinition();
	 		def.setId(Misc.getRsetInt(rs, "id"));
	 		def.setAlert_name(Misc.getRsetString(rs, "alert_name"));
	 		def.setDim_id(Misc.getRsetInt(rs, "dim_id"));
	 		def.setExpected_value(Misc.getRsetInt(rs, "expected_value"));
	 		def.setMeasure_type(Misc.getRsetInt(rs, "measure_type"));
	 		def.setMeasure_value(Misc.getRsetInt(rs, "measure_value"));
	 		def.setMeasure_direction(Misc.getRsetInt(rs, "measure_direction"));
	 		def.setWindow_size(Misc.getRsetInt(rs, "window_size"));
	 		def.setRepeat_duration(Misc.getRsetInt(rs, "repeat_duration"));
	 		def.setVehicle_type(Misc.getRsetInt(rs, "vehicle_type"));
	 		def.setVehicle_id(Misc.getRsetInt(rs, "vehicle_id"));
	 		def.setPort_node_id(Misc.getRsetInt(rs, "port_node_id"));
	 		def.setRule_id(Misc.getRsetInt(rs, "rule_id"));
	 		defList.put(def.getId(),def);
	 	}
		}catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				if(conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		
	}
	
	
	private static ArrayList<DOSNotificationHelper.UserDetails>  updateUserDetailsAndAlertActions(int alert_setting_id) {
		Connection conn = null;
		PreparedStatement ps =null;
		ArrayList<DOSNotificationHelper.UserDetails> simList=new ArrayList<DOSNotificationHelper.UserDetails>();
		boolean destroyIt = false;
		ResultSet rs = null;	 
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement("select a.user_id,a.alert_setting_id,u.phone,u.email,u.username,a.action,a.port_node_id from dos_alerts_actions a left outer join users u on (a.user_id=u.id)" +
					" where status = 1 and alert_setting_id="+alert_setting_id);
			rs = ps.executeQuery();
			
	 	while( rs.next()){
	 		DOSNotificationHelper.UserDetails def=new DOSNotificationHelper().new UserDetails();
	 		def.setId(Misc.getRsetInt(rs, "user_id"));
	 		def.setEmail(Misc.getRsetString(rs, "email"));
	 		def.setMobile(Misc.getRsetString(rs, "phone"));
	 		def.setUsername(Misc.getRsetString(rs, "username"));
	 		def.setPort_node_id(Misc.getRsetInt(rs, "port_node_id"));
	 		def.setAction(Misc.getRsetInt(rs, "action"));
	 		def.setAlertDefId(Misc.getRsetInt(rs, "alert_setting_id"));
	 		simList.add(def);
//	 		if(!actionList.containsKey(pair)){
//	 			StringBuffer sb=new StringBuffer();
//	 			sb.append(def.getAction()).append("|").append(def.)
//	 			actionList.put(pair, def);
//			}
	 	}
		}catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				if(conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
			
		return simList;
	}
	class UserDetails{
		int id;
		String mobile;
		String email ;
		String username;
		int action;
		int alertDefId;
		int port_node_id;
		
		public int getAlertDefId() {
			return alertDefId;
		}
		public void setAlertDefId(int alertDefId) {
			this.alertDefId = alertDefId;
		}
		public int getAction() {
			return action;
		}
		public void setAction(int action) {
			this.action = action;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getMobile() {
			return mobile;
		}
		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public int getPort_node_id() {
			return port_node_id;
		}
		public void setPort_node_id(int port_node_id) {
			this.port_node_id = port_node_id;
		}
		
		
	}
	public class AlertDefinition{
		int id ;
		String alert_name ;
		int dim_id ;
		int expected_value;
		int measure_type;
		int measure_value;
		int measure_direction;
		int window_size ;
		int repeat_duration ;
		int vehicle_type ;
		int vehicle_id;
		int port_node_id ;
		int rule_id;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getAlert_name() {
			return alert_name;
		}
		public void setAlert_name(String alert_name) {
			this.alert_name = alert_name;
		}
		public int getDim_id() {
			return dim_id;
		}
		public void setDim_id(int dim_id) {
			this.dim_id = dim_id;
		}
		public int getExpected_value() {
			return expected_value;
		}
		public void setExpected_value(int expected_value) {
			this.expected_value = expected_value;
		}
		public int getMeasure_type() {
			return measure_type;
		}
		public void setMeasure_type(int measure_type) {
			this.measure_type = measure_type;
		}
		public int getMeasure_value() {
			return measure_value;
		}
		public void setMeasure_value(int measure_value) {
			this.measure_value = measure_value;
		}
		public int getMeasure_direction() {
			return measure_direction;
		}
		public void setMeasure_direction(int measure_direction) {
			this.measure_direction = measure_direction;
		}
		public int getWindow_size() {
			return window_size;
		}
		public void setWindow_size(int window_size) {
			this.window_size = window_size;
		}
		public int getRepeat_duration() {
			return repeat_duration;
		}
		public void setRepeat_duration(int repeat_duration) {
			this.repeat_duration = repeat_duration;
		}
		public int getVehicle_type() {
			return vehicle_type;
		}
		public void setVehicle_type(int vehicle_type) {
			this.vehicle_type = vehicle_type;
		}
		public int getVehicle_id() {
			return vehicle_id;
		}
		public void setVehicle_id(int vehicle_id) {
			this.vehicle_id = vehicle_id;
		}
		public int getPort_node_id() {
			return port_node_id;
		}
		public void setPort_node_id(int port_node_id) {
			this.port_node_id = port_node_id;
		}
		public int getRule_id() {
			return rule_id;
		}
		public void setRule_id(int rule_id) {
			this.rule_id = rule_id;
		}
		
//		updated_on  ;         
//		created_on  ;         
//		status  ;         
//		created_by;         
//		updated_by;         
	
	}
}

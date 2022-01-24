package com.ipssi.reporting.mail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class VehicleServiceReminderHelper {
	
	private static final int PERDAY_KM = 200;
	private static final int PERDAY_ENGINE_HR = 8;
	private static final String INSERT_VEH_RECURRING_ALERT_LOG = "insert into veh_recurring_service_alert_log" +
			" (vehicle_id,veh_recurring_service_id,odometer_reading,engine_hr_reading,metric_one_reading,metric_two_reading," +
			" next_service_date,status,created_date,updated_on)" +
			" values(?,?,?,?,?,?,?,?,?,?)";
	
	private static final String FETCH_SERVICE_REMINDER_CANDIDATE = "select vrs.id, vrs.vehicle_id, vrs.service_item_id, vrs.odometer, " +
			" vrs.odometer_threshold,vrs.engine_hr,vrs.engine_hr_threshold,vrs.prev_service_date,vrs.months_threshold,vrs.metric_one," +
			" vrs.metric_one_threshold,vrs.metric_two,vrs.metric_two_threshold,vrs.prev_service_date,vrs.next_service_date,vrs.completion_threshold," +
			" vrs.frequency_threshold, vrs.updated_on, vrf.veh_recurring_service_id, vrf.seq, vrf.reminder_date, vrf.odometer_reading, " +
			" vrf.engine_hr_reading, vrf.days_reading, vrf.metric_one_reading, vrf.metric_two_reading, val.id, val.next_service_date, " +
			" val.status, val.odometer_reading vor,val.engine_hr_reading vehr,val.days_reading vdd,val.metric_one_reading vmor,val.metric_two_reading vmtr" +
//			" , vdr.gps_start_reading, vdr.gps_start_date " +
//			" ,(case when vdr.attribute_id = 0 then (case when vdr.gps_start_reading is null then (case when vdr.user1_reading is null then null " +
//			" else vdr.user1_reading end) else vdr.gps_start_reading end) else null end) odo_reading" +
//			" ,(case when vdr.attribute_id = 2 then (case when vdr.gps_start_reading is null then " +
//			" (case when vdr.user1_reading is null then null else vdr.user1_reading end) else vdr.gps_start_reading end) else null end) engine_reading" +
			" from veh_recurring_service vrs " +
//			" left outer join vehicle_daily_readings vdr on (vrs.vehicle_id = vdr.vehicle_id and vdr.date = ? ) " +
			" left outer join veh_rec_frequency vrf on (vrs.id = vrf.veh_recurring_service_id) " +
			" left outer join veh_recurring_service_alert_log val on (vrs.id = val.veh_recurring_service_id) ";
	private static final String FETCH_ALERT_LIST="select rec_service.id recurring_id,alert_log.id,alert_log.status,service_item.id service_id,service_item.name service_name,vehicle.id vehicle_id,vehicle.name vehicle_name,alert_log.next_service_date from veh_recurring_service_alert_log alert_log join veh_recurring_service rec_service on (rec_service.id=alert_log.veh_recurring_service_id) join service_item on (rec_service.service_item_id=service_item.id) join vehicle on (rec_service.vehicle_id=vehicle.id) where alert_log.status in (1,3)";
	private static final String FETCH_CUSTOMER_INFO="select customer_contacts.id, customer_contacts.contact_name, customer_contacts.mobile, customer_contacts.email from customer_contacts join veh_recurring_users on (veh_recurring_users.pre_reminder_user_id=customer_contacts.id) where veh_recurring_users.veh_recurring_service_id=?";
	private static final String UPDATE_ALERT_SEND_STATUS="update veh_recurring_service_alert_log set status=? where id=?";
	
	public static void processServiceReminder(Connection conn){
		try {
			HashMap<Pair<Integer,Integer>, Pair<Double,Date>> latestVehDailyReadingMap =  getLatestVehDailyReading(conn);
			ArrayList<ServiceReminderBean> serviceReminderList =  getServiceReminderList(conn, 0);
			for (Iterator iterator = serviceReminderList.iterator(); iterator.hasNext();) {
				ServiceReminderBean serviceReminderBean = (ServiceReminderBean) iterator.next();
				int vehicleId = serviceReminderBean.getVehicle_id();
				DataBean dataBean = new DataBean();
				dataBean.setVehicle_id(vehicleId);
				dataBean.setId(serviceReminderBean.getId());
				dataBean.setService_item_id(serviceReminderBean.getService_item_id());
				double nextReading = serviceReminderBean.getOdometer_reading();
				int attrId = 0;
				Date alertDate = null;
				Date currDate = new Date();
				double calcReading = Misc.getUndefDouble();
				if (serviceReminderBean.getVeh_recurring_service_id() < 1) {
					DataBean tktDataBean = getLatestVehTicketReading(conn, vehicleId);
					updateVehNextFrequencyDetail(serviceReminderBean, tktDataBean, conn);					
				}else {
					Pair<Double,Date> latestReadingPair = latestVehDailyReadingMap.get(new Pair<Integer, Integer>(vehicleId, attrId) );
					int daysDiff = Misc.getUndefInt();
					if (latestReadingPair != null && latestReadingPair.second != null && latestReadingPair.first != Misc.getUndefDouble()) {
						daysDiff = Misc.getDaysDiff(currDate, latestReadingPair.second);
						if (daysDiff != Misc.getUndefInt() && daysDiff > 0) {
							calcReading = latestReadingPair.first + daysDiff*PERDAY_KM;
						}
					}
					if (calcReading != Misc.getUndefDouble()) {
						dataBean.setOdometer(calcReading);
					}
					if (nextReading != Misc.getUndefDouble() && calcReading != Misc.getUndefDouble() && nextReading < calcReading) {
						alertDate = currDate;
					}
					nextReading = serviceReminderBean.getEngine_hr_reading();
					attrId = 2;
					latestReadingPair = latestVehDailyReadingMap.get(new Pair<Integer, Integer>(vehicleId, attrId) );
					daysDiff = Misc.getUndefInt();
					if (latestReadingPair != null && latestReadingPair.second != null && latestReadingPair.first != Misc.getUndefDouble()) {
						daysDiff = Misc.getDaysDiff(currDate, latestReadingPair.second);
						if (daysDiff != Misc.getUndefInt() && daysDiff > 0) {
							calcReading = latestReadingPair.first + daysDiff*PERDAY_ENGINE_HR;
						}
					}
					if (calcReading != Misc.getUndefDouble()) {
						dataBean.setEngine_hr(calcReading);
					}
					if (alertDate== null && nextReading != Misc.getUndefDouble() && calcReading != Misc.getUndefDouble() && nextReading < calcReading) {
						alertDate = currDate;
					}
					dataBean.setService_date(currDate);
					if (alertDate == null && serviceReminderBean.getReminder_date().getTime() < currDate.getTime()) {
						alertDate = currDate;
					}
//					nextReading = serviceReminderBean.getMetric_one_reading();
//					attrId = 2;
//					latestReading = latestVehDailyReadingMap.get(new Pair<Integer, Integer>(vehicleId, attrId) );
//					if (nextReading != Misc.getUndefDouble() && latestReading != Misc.getUndefDouble() && nextReading < latestReading) {
//						alertDate = new Date();
//					}
//					nextReading = serviceReminderBean.getMetric_two_reading();
//					attrId = 2;
//					latestReading = latestVehDailyReadingMap.get(new Pair<Integer, Integer>(vehicleId, attrId) );
//					if (nextReading != Misc.getUndefDouble() && latestReading != Misc.getUndefDouble() && nextReading < latestReading) {
//						alertDate = new Date();
//					}
									
					if (alertDate != null) {
						insertVehRecurringAlertLog(dataBean, conn);
						updateVehNextFrequencyDetail(serviceReminderBean, dataBean, conn);
					}
				}
				
			}
			// To insert/update frequency for latest closed tickets
			serviceReminderList =  getServiceReminderList(conn, 4);
			for (Iterator iterator = serviceReminderList.iterator(); iterator.hasNext();) {
				ServiceReminderBean serviceReminderBean = (ServiceReminderBean) iterator.next();
				DataBean tktDataBean = getLatestVehTicketReading(conn, serviceReminderBean.getVehicle_id());
				updateVehNextFrequencyDetail(serviceReminderBean, tktDataBean, conn);
			}
			// To send mails
			MailSender.sendAllReminders(conn);
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static boolean updateVehRecFrequency(DataBean dataBean, Connection conn) throws SQLException{		
		int[] iHit = null;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		try {
			if (dataBean != null){
				String INSERT_VEH_RECFREQUENCY = "update veh_rec_frequency set " +
				" seq = ? ,reminder_date = ? ,odometer_reading = ? ,engine_hr_reading = ? ,days_reading = ? ,metric_one_reading = ? ,metric_two_reading = ? , " +
				" next_service_date = ? ,updated_on = ?  " +
				" where veh_recurring_service_id = ?";
				
				stmt = conn.prepareStatement(INSERT_VEH_RECFREQUENCY);
				Misc.setParamInt(stmt, dataBean.getSeq(),1);
				stmt.setTimestamp(2, Misc.utilToSqlDate(dataBean.getService_date()));
				Misc.setParamDouble(stmt, dataBean.getOdometer(),3);
				Misc.setParamDouble(stmt, dataBean.getEngine_hr(),4);
				Misc.setParamDouble(stmt, dataBean.getMonths(),5);
				Misc.setParamDouble(stmt, dataBean.getMetric_one(),6);
				Misc.setParamDouble(stmt, dataBean.getMetric_two(),7);
				stmt.setTimestamp(8, Misc.utilToSqlDate(dataBean.getService_date()));
				stmt.setTimestamp(9, Misc.utilToSqlDate(new Date()));
				Misc.setParamInt(stmt, dataBean.getId(),10);
//				stmt.addBatch();		 
			}
			stmt.execute();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(stmt!=null)
				stmt.close();	
		}

		return insertStatus;
	}
	
	
	public static boolean insertVehRecFrequency(DataBean dataBean, Connection conn) throws SQLException{		
		int[] iHit = null;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		try {
			if (dataBean != null){
				String INSERT_VEH_RECFREQUENCY = "insert into veh_rec_frequency" +
				" (veh_recurring_service_id,seq,reminder_date,odometer_reading,engine_hr_reading,days_reading,metric_one_reading,metric_two_reading," +
				" next_service_date,updated_on)" +
				" values(?,?,?,?,?,?,?,?,?,?)";
				
				stmt = conn.prepareStatement(INSERT_VEH_RECFREQUENCY);
				Misc.setParamInt(stmt, dataBean.getId(),1);
				Misc.setParamInt(stmt, dataBean.getSeq(),2);
				stmt.setTimestamp(3, Misc.utilToSqlDate(dataBean.getService_date()));
				Misc.setParamDouble(stmt, dataBean.getOdometer(),4);
				Misc.setParamDouble(stmt, dataBean.getEngine_hr(),5);
				Misc.setParamDouble(stmt, dataBean.getMonths(),6);
				Misc.setParamDouble(stmt, dataBean.getMetric_one(),7);
				Misc.setParamDouble(stmt, dataBean.getMetric_two(),8);
				stmt.setTimestamp(9, Misc.utilToSqlDate(dataBean.getService_date()));
				stmt.setTimestamp(10, Misc.utilToSqlDate(new Date()));
//				stmt.addBatch();		 
			}
			stmt.execute();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(stmt!=null)
				stmt.close();	
		}

		return insertStatus;
	}

	public static boolean updateVehNextFrequencyDetail(ServiceReminderBean serviceReminderBean, DataBean dataBean, Connection conn) throws SQLException{		
		
		if (dataBean == null) {
			return false;
		}
		int compThrehold = serviceReminderBean.getCompletion_threshold();
		int overdueThrehold = serviceReminderBean.getFrequency_threshold();
		double tempThres = Misc.getUndefDouble();
		double percentThres = Misc.getUndefDouble();
		DataBean reminderBean = new DataBean();
		reminderBean.setVehicle_id(serviceReminderBean.getVehicle_id());
		reminderBean.setId(serviceReminderBean.getId());
		reminderBean.setSeq(serviceReminderBean.getSeq() == Misc.getUndefInt() ? 0 : serviceReminderBean.getSeq() +1);
		tempThres = dataBean.getOdometer();
		
		if (tempThres != Misc.getUndefDouble() && tempThres > 0.0) {
			percentThres = tempThres*overdueThrehold/100;
			reminderBean.setOdometer(tempThres+percentThres);
		}
		tempThres = dataBean.getEngine_hr();
		if (tempThres != Misc.getUndefDouble() && tempThres > 0.0) {
			percentThres = tempThres*overdueThrehold/100;
			reminderBean.setEngine_hr(tempThres+percentThres);
		}
		Date serviceDate = dataBean.getService_date();// TODO need to keep base value in freqency table and then calc next schedule
		if (serviceDate != null) {
			Misc.addDays(serviceDate, 10);// hard coded need to change freqency table structure
			reminderBean.setService_date(serviceDate);
		}
		tempThres = dataBean.getMetric_one();
		if (tempThres != Misc.getUndefDouble() && tempThres > 0.0) {
			percentThres = tempThres*overdueThrehold/100;
			reminderBean.setMetric_one(tempThres+percentThres);
		}
		tempThres = dataBean.getMetric_two();
		if (tempThres != Misc.getUndefDouble() && tempThres > 0.0) {
			percentThres = tempThres*overdueThrehold/100;
			reminderBean.setMetric_two(tempThres+percentThres);
		}
		
		int recId = dataBean.getId();
		if (recId != Misc.getUndefInt() && recId > 0) {
			reminderBean.setId(recId);
			updateVehRecFrequency(reminderBean, conn);
		}else{
			insertVehRecFrequency(reminderBean, conn);
		}
		
		
		return true;
	}	
	public static boolean insertVehRecurringAlertLog(DataBean dataBean, Connection conn) throws SQLException{		
		int[] iHit = null;
		boolean insertStatus = false;
		PreparedStatement stmt=null;
		try {
			if (dataBean != null){
				
				stmt = conn.prepareStatement(INSERT_VEH_RECURRING_ALERT_LOG);
				Misc.setParamInt(stmt, dataBean.getVehicle_id(),1);
				Misc.setParamInt(stmt, dataBean.getService_item_id(),2);
				Misc.setParamDouble(stmt, dataBean.getOdometer(),3);
				Misc.setParamDouble(stmt, dataBean.getEngine_hr(),4);
//				Misc.setParamDouble(stmt, serviceReminderBean.getDays_reading(),6);
				Misc.setParamDouble(stmt, dataBean.getMetric_one(),5);
				Misc.setParamDouble(stmt, dataBean.getMetric_two(),6);
				stmt.setTimestamp(7, Misc.utilToSqlDate(dataBean.getService_date()));
				Misc.setParamInt(stmt, 1,8);
				stmt.setTimestamp(9, Misc.utilToSqlDate(new Date()));
				stmt.setTimestamp(10, Misc.utilToSqlDate(new Date()));
//				stmt.addBatch();		 
			}
			stmt.execute();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally
		{
			if(stmt!=null)
				stmt.close();	
		}

		return insertStatus;
	}	
	
	public static ArrayList<ServiceReminderBean> getServiceReminderList(Connection conn, int statusCluase) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		ArrayList<ServiceReminderBean> serviceReminderBeanList = new ArrayList<ServiceReminderBean>();
		String where = " where vrs.status = 1 and (val.id is null or val.status in (1,2,3) ) "; // 1:active, 2:pending, 3:send
		if (statusCluase == 4) {
			where = " where vrs.status = 1 and (val.status in (4) ) "; // 4:closed
		}
		String query = FETCH_SERVICE_REMINDER_CANDIDATE;
		query = query + where;
		try{
			ps = conn.prepareStatement(query);
//			ps.setTimestamp(1, Misc.longToSqlDate(current.getTime()));
//			Misc.setParamInt(ps, granularity, 2);
			rs = ps.executeQuery();
			while(rs.next())
			{
				ServiceReminderBean serviceReminderBean = new ServiceReminderBean();
				serviceReminderBean.setId(Misc.getRsetInt(rs, "id"));
				serviceReminderBean.setVehicle_id(Misc.getRsetInt(rs, "vehicle_id"));
				serviceReminderBean.setService_item_id(Misc.getRsetInt(rs, "service_item_id"));
				serviceReminderBean.setOdometer(Misc.getRsetDouble(rs, "odometer"));
				serviceReminderBean.setOdometer_threshold(Misc.getRsetDouble(rs, "odometer_threshold"));
				serviceReminderBean.setEngine_hr(Misc.getRsetDouble(rs, "engine_hr"));
				serviceReminderBean.setEngine_hr_threshold(Misc.getRsetDouble(rs, "engine_hr_threshold"));
				serviceReminderBean.setPrev_service_date(rs.getTimestamp("prev_service_date"));
				serviceReminderBean.setMonths_threshold(Misc.getRsetDouble(rs, "months_threshold"));
				serviceReminderBean.setMetric_one(Misc.getRsetDouble(rs, "metric_one"));
				serviceReminderBean.setMetric_one_threshold(Misc.getRsetDouble(rs, "metric_one_threshold"));
				serviceReminderBean.setMetric_two(Misc.getRsetDouble(rs, "metric_two"));
				serviceReminderBean.setMetric_two_threshold(Misc.getRsetDouble(rs, "metric_two_threshold"));
				serviceReminderBean.setCompletion_threshold(Misc.getRsetInt(rs, "completion_threshold"));
				serviceReminderBean.setFrequency_threshold(Misc.getRsetInt(rs, "frequency_threshold"));
				serviceReminderBean.setUpdated_on(rs.getTimestamp("updated_on"));
				serviceReminderBean.setVeh_recurring_service_id(Misc.getRsetInt(rs, "veh_recurring_service_id"));
				serviceReminderBean.setSeq(Misc.getRsetInt(rs, "seq"));
				serviceReminderBean.setOdometer_reading(Misc.getRsetDouble(rs, "odometer_reading"));
				serviceReminderBean.setEngine_hr_reading(Misc.getRsetDouble(rs, "engine_hr_reading"));
				serviceReminderBean.setMetric_one_reading(Misc.getRsetDouble(rs, "metric_one_reading"));
				serviceReminderBean.setMetric_two_reading(Misc.getRsetDouble(rs, "metric_two_reading"));
				serviceReminderBean.setDays_reading(Misc.getRsetDouble(rs, "days_reading"));
				serviceReminderBean.setReminder_date(rs.getTimestamp("reminder_date"));
				serviceReminderBean.setNext_service_date(rs.getTimestamp("next_service_date"));
				serviceReminderBean.setStatus(Misc.getRsetInt(rs, "status"));
				serviceReminderBean.setLog_odometer_reading(Misc.getRsetDouble(rs, "vor"));
				serviceReminderBean.setLog_engine_hr_reading(Misc.getRsetDouble(rs, "vehr"));
				serviceReminderBean.setLog_days_reading(Misc.getRsetDouble(rs, "vdd"));
				serviceReminderBean.setLog_metric_one_reading(Misc.getRsetDouble(rs, "vmor"));
				serviceReminderBean.setLog_metric_two_reading(Misc.getRsetDouble(rs, "vmtr"));
//				serviceReminderBean.setGps_start_date(rs.getTimestamp("gps_start_date"));
//				serviceReminderBean.setGps_start_reading(Misc.getRsetInt(rs, "gps_start_reading"));
//				serviceReminderBean.setOpt_odo_reading(Misc.getRsetInt(rs, "odo_reading"));
//				serviceReminderBean.setOpt_engine_reading(Misc.getRsetInt(rs, "engine_reading"));
				serviceReminderBeanList.add(serviceReminderBean);
//				String key = (serviceReminderBean.getGroupId())+("#"+serviceReminderBean.getReportId())+("#"+serviceReminderBean.getFrequencyId());;
//				lookup.put(key, serviceReminderBean);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
		}
		return serviceReminderBeanList;
	}
	
	public static HashMap<Pair<Integer,Integer>, Pair<Double,Date>> getLatestVehDailyReading(Connection conn) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		HashMap<Pair<Integer,Integer>, Pair<Double,Date>> vehicleAttrValMap = new HashMap<Pair<Integer,Integer>, Pair<Double,Date>>();
		String query = "select val.vehicle_id, val.attribute_id, val.date, val.gps_start_reading, val.user1_reading, val.gps_start_date" +
				" from " +
				"(select * from " +
				"(select * from " +
				"(select vehicle_id left_vehicle_id,attribute_id left_attribute_id, max(date) left_dt " +
				"from vehicle_daily_readings where gps_start_reading is not null group by vehicle_id,attribute_id) mx_gps " +
				"left outer join " +
				" (select vehicle_id,attribute_id, max(date) dt from vehicle_daily_readings where user1_reading is not null" +
				" group by vehicle_id,attribute_id) mx on (mx_gps.left_vehicle_id = mx.vehicle_id and mx_gps.left_attribute_id = mx.attribute_id ) " +
				"union " +
				"select * from " +
				"(select vehicle_id,attribute_id, max(date) dt from vehicle_daily_readings " +
				"where gps_start_reading is not null group by vehicle_id,attribute_id) mx_gps " +
				"right outer join " +
				" (select vehicle_id,attribute_id, max(date) dt from vehicle_daily_readings where user1_reading is not null" +
				" group by vehicle_id,attribute_id) mx " +
				"on (mx_gps.vehicle_id = mx.vehicle_id and mx_gps.attribute_id = mx.attribute_id ))max_all_data) max_all " +
				"join vehicle_daily_readings val on ((val.vehicle_id = max_all.left_vehicle_id or val.vehicle_id = max_all.vehicle_id)" +
				" and (val.attribute_id = max_all.attribute_id or val.attribute_id = max_all.left_attribute_id)" +
				" and (val.date = max_all.dt or val.date = max_all.left_dt))  order by val.vehicle_id, val.attribute_id, val.date";
		try{
			
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			int vId = Misc.getUndefInt();
			int aId = Misc.getUndefInt();
			int prevVId = Misc.getUndefInt();
			int prevAId = Misc.getUndefInt();
			double gpsReading = Misc.getUndefDouble();
			double user1Reading = Misc.getUndefDouble();
			double prevGpsReading = Misc.getUndefDouble();
			double prevUser1Reading = Misc.getUndefDouble();
			Date readDate = null;
			while(rs.next())
			{
				vId =  Misc.getRsetInt(rs, "vehicle_id");
				aId = Misc.getRsetInt(rs, "attribute_id");
				readDate = rs.getTimestamp("date");
				gpsReading = Misc.getRsetDouble(rs, "gps_start_reading");
				user1Reading = Misc.getRsetDouble(rs, "user1_reading");
				double tempReading = Misc.getUndefDouble();
				if (prevVId != Misc.getUndefInt()) {
					if (prevVId == vId && prevAId == aId) {
						if (user1Reading != Misc.getUndefDouble()) {
							tempReading = user1Reading;
						}else if(prevUser1Reading != Misc.getUndefDouble() && gpsReading != Misc.getUndefDouble() && prevGpsReading != Misc.getUndefDouble()){
							tempReading = user1Reading + (gpsReading - prevGpsReading);
						}else if(prevUser1Reading != Misc.getUndefDouble()){
							tempReading = prevUser1Reading;
						}
					}
				}
				if (tempReading == Misc.getUndefDouble())
					tempReading = user1Reading  != Misc.getUndefDouble() ? user1Reading : gpsReading;
				vehicleAttrValMap.put(new Pair<Integer, Integer>(vId, aId), new Pair<Double,Date>(tempReading,readDate));
				prevVId = vId;
				prevAId = aId;
				prevGpsReading = gpsReading;
				prevUser1Reading = user1Reading;
//				String key = (serviceReminderBean.getGroupId())+("#"+serviceReminderBean.getReportId())+("#"+serviceReminderBean.getFrequencyId());;
//				lookup.put(key, serviceReminderBean);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
		}
		return vehicleAttrValMap;
	}
	
	public static DataBean getLatestVehTicketReading(Connection conn, int vehicleId) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		HashMap<Pair<Integer,Integer>, Double> vehicleAttrValMap = new HashMap<Pair<Integer,Integer>, Double>();
		String query = "select vehicle_maint.vehicle_id, metric_one,metric_two,engine_hr,odometer, updated_on" +
				" from (select vehicle_id, max(updated_on) dt from vehicle_maint where status in (2,5) and vehicle_id = ? group by vehicle_id) mx " +
				" join vehicle_maint on (vehicle_maint.vehicle_id = mx.vehicle_id and vehicle_maint.updated_on = mx.dt)";
		DataBean readDate = null;
		try{
			
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, vehicleId,1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				readDate = new DataBean();
				readDate.setVehicle_id(Misc.getRsetInt(rs, "vehicle_id"));
				readDate.setMetric_one(Misc.getRsetDouble(rs, "metric_one"));
				readDate.setMetric_two(Misc.getRsetDouble(rs, "metric_two"));
				readDate.setEngine_hr(Misc.getRsetDouble(rs, "engine_hr"));
				readDate.setOdometer(Misc.getRsetDouble(rs, "odometer"));
				readDate.setService_date(rs.getTimestamp("updated_on"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
		}
		return readDate;
	}
	public static ArrayList<DataBean> getServiceAlertList(Connection conn) throws SQLException{
		PreparedStatement ps =null;
		ResultSet rs=null;
		ArrayList<DataBean> alertList = new ArrayList<DataBean>();
		DataBean alertBean = null;
		String query = FETCH_ALERT_LIST;
		try{
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next())
			{
				alertBean = new DataBean();
				alertBean.setId(Misc.getRsetInt(rs, "id"));
				alertBean.setVehicle_id(Misc.getRsetInt(rs, "vehicle_id"));
				alertBean.setStatus(Misc.getRsetInt(rs, "status"));
				alertBean.setVehicleName(Misc.getRsetString(rs, "vehicle_name"));
				alertBean.setService_item_id(Misc.getRsetInt(rs, "service_id"));
				alertBean.setServiceItemName(Misc.getRsetString(rs, "service_name"));
				alertBean.setService_date(rs.getTimestamp("next_service_date"));
				alertBean.setCustomerContactList(getCustomerContactList(Misc.getRsetInt(rs, "recurring_id"),conn));
				alertList.add(alertBean);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
		}
		return alertList;
	}
	private static ArrayList<CustomerContactInfo> getCustomerContactList(int id,Connection conn) throws SQLException {
		PreparedStatement ps =null;
		ResultSet rs=null;
		ArrayList<CustomerContactInfo> custList = new ArrayList<CustomerContactInfo>();
		CustomerContactInfo custInfo = null;
		String query = FETCH_CUSTOMER_INFO;
		try{
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, id, 1);
			rs = ps.executeQuery();
			while(rs.next())
			{
				custInfo = new CustomerContactInfo();
				custInfo.setName(Misc.getRsetString(rs, "contact_name"));
				custInfo.setEmail(Misc.getRsetString(rs, "mobile"));
				custInfo.setMobile(Misc.getRsetString(rs, "email"));
				custList.add(custInfo);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
		}
		return custList;
	}
	public static void setAlertLogSendStatus(Connection conn,int status,int alertLogId) throws SQLException{
		PreparedStatement ps =null;
		String query = UPDATE_ALERT_SEND_STATUS;
		try{
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, status, 1);
			Misc.setParamInt(ps, alertLogId, 2);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
				if(ps != null)
					ps.close();
		}
	}
}

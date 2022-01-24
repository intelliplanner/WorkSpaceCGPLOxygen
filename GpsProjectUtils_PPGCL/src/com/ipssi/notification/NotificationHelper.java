package com.ipssi.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.eta.NewETAAlertHelper;
import com.ipssi.eta.SrcDestInfo;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Value;
import com.ipssi.notification.Notification.AnswerEnum;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.tprCache.TPRLatestCache;


public class NotificationHelper {
	
public static void saveNotificationInfo(Connection conn, int notificationId, int consigneeId, int consignorId, int transporterId, int vehicleId) throws Exception {
		
		Notification notification =  Notification.getNotification(conn, notificationId);
		NotificationInfo nInfo = new NotificationInfo();
		nInfo.setNotificationId(notification.getId());
		nInfo.setVehicleId(vehicleId);
		nInfo.setConsigneeId(consigneeId);
		nInfo.setConsignorId(consignorId);
		nInfo.setTransporterId(transporterId);
		
		StringBuilder nText = getText(notification);
		nInfo.setText(nText != null ? nText.toString() : null);
		nInfo.setQuestionsId_1(notification.getQuestionsId_1());
		nInfo.setQuestionsId_2(notification.getQuestionsId_2());
		
		NotificationInfo.saveNotificationInfo(conn, nInfo);
	}

public static void updateNotificationInfo(Connection conn, int notificationId, int consigneeId, int consignorId, int transporterId, int vehicleId) throws Exception {
	
	Notification notification =  Notification.getNotification(conn, notificationId);
	NotificationInfo nInfo = new NotificationInfo();
	nInfo.setVehicleId(vehicleId);
	nInfo.setConsigneeId(consigneeId);
	nInfo.setConsignorId(consignorId);
	nInfo.setTransporterId(transporterId);
	
	StringBuilder nText = getText(notification);
	nInfo.setText(nText != null ? nText.toString() : null);
	nInfo.setQuestionsId_1(notification.getQuestionsId_1());
	nInfo.setQuestionsId_2(notification.getQuestionsId_2());
	
	NotificationInfo.saveNotificationInfo(conn, nInfo);
}

public static void updateNotificationInfoWithResponse(Connection conn, int nInfoId, int answer1, int answer2, String answerText1, String answerText2) throws Exception {
	
	NotificationInfo nInfo = new NotificationInfo();//NotificationInfo.getNotificationInfo(conn, nInfoId);
	nInfo.setId(nInfoId);
	nInfo.setAnswersId_1(answer1);
	nInfo.setAnswersId_2(answer2);
	nInfo.setNotes(answerText1 != null ? answerText1 : answerText2 != null ? answerText2 : null);
	
	NotificationInfo.updateNotificationInfoText(conn, nInfo);	
	
	nInfo = NotificationInfo.getNotificationInfo(conn, nInfoId);
	Notification notification =  Notification.getNotification(conn, nInfo.getNotificationId());
	if(notification.getAnswerId_1_1() == answer1 && !Misc.isUndef(notification.getNotificationId_1_1())){
		Notification newNotification =  Notification.getNotification(conn, notification.getNotificationId_1_1());
//		NotificationInfo newNotificationInfo = updateNotificationInfo(conn, newNotification.getId(), nInfo.getConsigneeId(), nInfo.getConsignorId()
//				, nInfo.getTransporterId(), nInfo.getVehicleId());
	}
	
	
}


	public static StringBuilder getText(Notification notification){
		StringBuilder retval = null;
		String pattern = null;
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		String format = notification.getText();
		if(notification != null)
			retval = new StringBuilder();
		pattern = "%vehicleName";
		if (format.indexOf(pattern) >= 0) {
			String replacement = null;
			replacement = NewETAAlertHelper.helperGetCleanString(replacement);
			format = format.replaceAll(pattern, replacement);
		}
		
		
		pattern = "%location";
		if (format.indexOf(pattern) >= 0) {
			Value  val = null;//CacheValue.getValueInternal(conn, vehicleId, 20167, vehSetup, vdf);
			String replacement = val == null ? "N/A" : val.toString();
			replacement = NewETAAlertHelper.helperGetCleanString(replacement);
			format = format.replaceAll(pattern, replacement);
		}
		
		pattern = "%sentAt";
		if (format.indexOf(pattern) >= 0) {
			String replacement = "";//helperGetCleanDate(sdf,vehicleETA.getCurrFromOpStationInTime());
			format = format.replaceAll(pattern, replacement);
		}
		
		return retval;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
}

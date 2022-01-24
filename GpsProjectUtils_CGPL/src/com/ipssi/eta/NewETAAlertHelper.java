package com.ipssi.eta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.sql.ResultSet;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.VehicleExtendedInfo;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.report.cache.CacheValue.LatestEventInfo;

import com.ipssi.userNameUtils.CDHEmailInfo;
import com.ipssi.userNameUtils.IdInfo;
import com.ipssi.userNameUtils.TextInfo;
import com.ipssi.communicator.dto.CommunicatorQueueSender;
import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.trip.ChallanInfo;
import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.communicator.dto.CommunicatorDTO;

public class NewETAAlertHelper {
	public static boolean g_alertSimulateMode = false;
	public static String helperGetCleanDate(SimpleDateFormat sdf, long dt) {
		return dt <= 0 ? "N/A" : sdf.format(Misc.longToUtilDate(dt));
	}
	
	public static String helperGetCleanString(String s) {
		if (s != null) {
			s = s.trim();
			if (s.length() == 0)
				s = null;
		}
		return s == null ? "N/A" : s;
	}
	
	public static String helperGetCleanDouble(double d) {
		return d < 0.0 ? "N/A" : ""+d;
	}
	
	public static Pair<Boolean, ArrayList<MiscInner.Triple>> handleSendAlertAndEvent(Connection conn, int vehicleId, NewVehicleETA vehicleETA, NewETAforSrcDestItem specificETA, ChallanInfo challanInfo
	,boolean doChallanOnly, int eventTy, int flexParam, long nowTime, GpsData latestPosn
	,StopDirControl stopDirControl, VehicleDataInfo vdf
	,boolean sendAlert, CacheTrack.VehicleSetup vehSetup
	)  {
		//returns: first = true if success
		//  2nd = arraylist of notification sent for different target type - 1st = targetType, 2 = notificationTypeId, 3 = notificationId
		boolean retval = true;
		try {
			vehicleETA.setAlertSentThisTime(true);
			StringBuilder traceStr = NewVehicleETA.toTrace(vehicleId) ? new StringBuilder() : null;
			SrcDestInfo srcDestInfo = SrcDestInfo.getSrcDestInfo(conn, specificETA == null ? Misc.getUndefInt() : specificETA.getSrcDestId());
			if (srcDestInfo == null)
				return new Pair<Boolean, ArrayList<MiscInner.Triple>>(true, null);
			//createAlertDB
			NewVehicleData vdp = vdf.getDataList(conn, vehicleId, 0, false);
			vehicleETA.calcEstETA(specificETA, conn, vehSetup, vdp, latestPosn, nowTime);
			if (traceStr != null) {
				traceStr.append("[ETA:").append(vehicleId).append(" Send Alert now DB:")
				.append("Ty:").append(SrcDestInfo.getNameForAlertTy(eventTy)).append(",Flex:").append(flexParam);
				System.out.println(traceStr);
				traceStr.setLength(0);
			}
			retval = createAlertDB(conn, vehicleId, vehicleETA, specificETA, challanInfo, eventTy, flexParam, nowTime, latestPosn) && retval;
			//get String to send
			if (!conn.getAutoCommit())
				conn.commit();
			if (srcDestInfo == null || !sendAlert)
				return new Pair<Boolean, ArrayList<MiscInner.Triple>>(retval, null);
			ResultOfWhoToSend msgTargets = getWhoToSend(conn,  vehicleId, vehicleETA
					, srcDestInfo, eventTy, flexParam, doChallanOnly
					, challanInfo, stopDirControl, nowTime);
			if (msgTargets == null || msgTargets.isEmpty())
				return new Pair<Boolean, ArrayList<MiscInner.Triple>>(retval, null);
			
			String formatSMS = srcDestInfo.getAlertFormat(eventTy,0);
			String formatEmail = srcDestInfo.getAlertFormat(eventTy,1);
			String formatNotification = srcDestInfo.getAlertFormat(eventTy,2);
			
			if (formatSMS == null) //formatEmail and Notifcation default from sms
				return new Pair<Boolean, ArrayList<MiscInner.Triple>>(retval, null);
			String pattern = null;
			SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
			double flexKM = (double)flexParam/1000.0;
			double flexHr = (double)flexParam/(60.0);
			if (flexKM < 0)
				flexKM = 0;
			if (flexHr < 0)
				flexHr = 0;
			
			//... now figure out text and then send
			if (formatSMS.equals(formatEmail))
				formatEmail = null;
			if (formatSMS.equals(formatNotification))
				formatNotification = null;
			//to avoid duplication of replacement .. will revert back
			for (int art=0;art<3;art++) {
				String format = art == 0 ? formatSMS : art == 1 ? formatEmail : formatNotification;
				if (format == null)
					continue;
				//“Inv. 2720003672, Qty. 17 MT, 	Grade - PPC, 	Trk No: MH18BG2371, Dispatched from: OCL, Jalgaon/Depot name for 	Customer: Garg Agencies & Ship to party name: Shree ganesh agencies, Aurangabad, ETA : 07/12/17, 12:00; 	Transporter contact. 9010878001. 	Sent at  07/12/17, 10:00 AM”
			//Inv. XXXX , 	  Qty. XXXX MT, Grade - XXXX , 	Trk No: XXXX , 		Dispatched from: XXXX , XXXX 					Customer: XXXX 			& Ship to party name: XXXX , 							 ETA : XXXX ;			 	Transporter contact. XXXX . 		Sent at XXXX 
			//“Inv. %invNo , Qty. %qty MT, 	Grade - %grade , 	Trk No: %trkNo , Dispatched from: OCL, Jalgaon Customer: %customer & Ship to party name: %shipToPartyName , ETA : 07/12/17, 12:00; 	Transporter contact. %trnptCont . 	Sent at  07/12/17, 10:00 AM”
			pattern = "%shipToPartyName";
			if (format.indexOf(pattern) >= 0) {
				String replacement = challanInfo.getDestAddr1();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%customer";
			if (format.indexOf(pattern) >= 0) {
				String replacement = challanInfo.getCustomer();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%trnptCont";
			if (format.indexOf(pattern) >= 0) {
				String replacement = challanInfo.getAlertPhone_l1_Transporter();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%trkNo";
			if (format.indexOf(pattern) >= 0) {
				String replacement = challanInfo.getTruckNo();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%grade"; // material
			if (format.indexOf(pattern) >= 0) {
				String replacement = challanInfo.getStr2();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%qty";
			if (format.indexOf(pattern) >= 0) {
				String replacement = helperGetCleanDouble(challanInfo.getQty());
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%invNo";
			if (format.indexOf(pattern) >= 0) {
				String replacement = challanInfo.getGrNo();
				replacement = helperGetCleanString(replacement);
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%stoppedSince";
			if (format.indexOf(pattern) >= 0) {
				LatestEventInfo latestEvent = CacheValue.getLatestEvent(vehicleId, srcDestInfo.getStoppageRuleId());
				String replacement =  helperGetCleanDate(sdf,latestEvent == null ? null : latestEvent.getStartTime());
				format = format.replaceAll(pattern, replacement);
			}
			pattern = "%stoppedAt";
			if (format.indexOf(pattern) >= 0) {
				LatestEventInfo latestEvent = CacheValue.getLatestEvent(vehicleId, srcDestInfo.getStoppageRuleId());
				String replacement = helperGetCleanString(latestEvent == null ? null : latestEvent.getStartName());
				format = format.replaceAll(pattern, replacement);
			}
				pattern = "%vehicleId";				
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehSetup.m_name;
					
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%sentTime";
				if (format.indexOf(pattern) >= 0) {
					String replacement = helperGetCleanDate(sdf,nowTime);
					format = format.replaceAll(pattern, replacement);
				}
				
				pattern = "%consignor";
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehicleETA.getFrom(conn,(byte)1);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				
				pattern = "%dest_intime"; //other wise dest replaces!!
				if (format.indexOf(pattern) >= 0) {
					String replacement = helperGetCleanDate(sdf,vehicleETA.getCurrToInTime());
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%dest_city"; //other wise dest replaces!!
				if (format.indexOf(pattern) >= 0) {
					TextInfo destInfo = challanInfo == null ? null : challanInfo.getTextInfo();
					String replacement = destInfo == null ? null : destInfo.getCity();
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%dest";
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehicleETA.getTo(conn, (byte)0);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%from";
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehicleETA.getFrom(conn,(byte)0);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%src";
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehicleETA.getFrom(conn,(byte)0);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%to";
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehicleETA.getTo(conn, (byte)0);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%consignee";
				if (format.indexOf(pattern) >= 0) {
					String replacement = vehicleETA.getTo(conn, (byte)1);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%curr_eta";
				if (format.indexOf(pattern) >= 0) {
					String replacement = helperGetCleanDate(sdf,vehicleETA.getCurrETA(specificETA));
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%est_eta";
				if (format.indexOf(pattern) >= 0) {
					String replacement = helperGetCleanDate(sdf,vehicleETA.getEstETA(specificETA));
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%intermediate";
				if (format.indexOf(pattern) >= 0) {
					ArrayList<SrcDestInfo.WayPoint> wplist = srcDestInfo.getWaypoints();
					String replacement = wplist == null || flexParam < 0 || flexParam >= wplist.size() ? "N/A" : wplist.get(flexParam).getName();
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				
				
				pattern = "%location";
				if (format.indexOf(pattern) >= 0) {
					Value  val = CacheValue.getValueInternal(conn, vehicleId, 20167, vehSetup, vdf);
					String replacement = val == null ? "N/A" : val.toString();
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				
				pattern = "%intime";
				if (format.indexOf(pattern) >= 0) {
					String replacement = helperGetCleanDate(sdf,vehicleETA.getCurrFromOpStationInTime());
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%time";
				if (format.indexOf(pattern) >= 0) {
					Value  val = CacheValue.getValueInternal(conn, vehicleId, 20173, vehSetup, vdf);
					String replacement = val == null ? "N/A" : sdf.format(val.getDateVal());
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%prop";
				if (format.indexOf(pattern) >= 0) {
					String replacement = "N/A";
					if (flexParam >= 0 && SrcDestInfo.isDistBasedAlert(eventTy))
						replacement = Double.toString(flexKM);
					else if (SrcDestInfo.isTimeBasedAlert(eventTy))
						replacement = Double.toString(flexHr);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				pattern = "%route";
				if (format.indexOf(pattern) >= 0) {
					String replacement = srcDestInfo == null ? null : srcDestInfo.getName();
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				if (format.indexOf("%field") >= 0 || format.indexOf("%misc") >= 0) {
					VehicleExtendedInfo vehicleExt = VehicleExtendedInfo.getVehicleExtended(conn, vehSetup.m_vehicleId);
					if (vehicleExt != null) {
						pattern = "%fieldone";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldone();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldtwo";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldtwo();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldthree";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldthree();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldfour";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldfour();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldfive";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldfive();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldsix";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldsix();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldseven";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldseven();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%fieldeight";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getFieldeight();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
						pattern = "%misc";
						if (format.indexOf(pattern) >= 0) {
							String replacement = vehicleExt.getMiscellaneous();
							replacement = helperGetCleanString(replacement);
							format = format.replaceAll(pattern, replacement);
						}
					}
				}
				pattern = "%misc";
				if (format.indexOf(pattern) >= 0) {
					Cache cache = Cache.getCacheInstance(conn);
					String replacement = cache.getPortName(conn, vehSetup.m_ownerOrgId);
					replacement = helperGetCleanString(replacement);
					format = format.replaceAll(pattern, replacement);
				}
				if (art == 0)
					formatSMS = format;
				else if (art == 1)
					formatEmail = format;
				else
					formatNotification = format;
			}
			if (formatEmail == null)
				formatEmail = formatSMS;
			if (formatNotification == null)
				formatNotification = formatSMS;
			//whew ... now send it
			if (traceStr != null) {
				traceStr.append("[ETA:").append(vehicleId).append(" Sending Alert:")
				.append("Ty:").append(SrcDestInfo.getNameForAlertTy(eventTy)).append(",Flex:").append(flexParam)
				.append(" MsgTarget:").append(msgTargets.toString());
				;
				System.out.println(traceStr);
				traceStr.setLength(0);
			}
			ArrayList<MiscInner.Triple> notificationIds = null;
			if (msgTargets.smsList != null && msgTargets.smsList.size() > 0) {
				logMessageToSend(conn, msgTargets, 0, formatSMS, vehSetup, vehicleId, flexParam, eventTy, vehicleETA, specificETA, latestPosn, challanInfo) ;
				sendSMS(conn, msgTargets.smsList, formatSMS, vehicleId, flexParam, eventTy);
			}
			if (msgTargets.emailList != null && msgTargets.emailList.size() > 0) {
				logMessageToSend(conn, msgTargets, 1, formatEmail, vehSetup, vehicleId, flexParam, eventTy, vehicleETA, specificETA, latestPosn, challanInfo) ;
				sendEmail(conn, msgTargets.emailList, formatEmail, vehicleId, flexParam, eventTy);
			}
			if (msgTargets.notificationIdTargetLevel != null && msgTargets.notificationIdTargetLevel.size() > 0) {
				logMessageToSend(conn, msgTargets, 2, formatNotification, vehSetup, vehicleId, flexParam, eventTy, vehicleETA, specificETA, latestPosn, challanInfo) ;
				 notificationIds = sendNotifications(conn, msgTargets, formatNotification, vehicleId, flexParam, eventTy);
			}
			return new Pair<Boolean, ArrayList<MiscInner.Triple>>(true, notificationIds);
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		return new Pair<Boolean, ArrayList<MiscInner.Triple>>(retval, null);
	}
	public static void logMessageToSend(Connection conn, ResultOfWhoToSend msgTargets, int what, String format, CacheTrack.VehicleSetup vehSetup, int vehicleId, int flexParam, int eventTy, NewVehicleETA vehicleETA, NewETAforSrcDestItem specificETA, GpsData latestPosn, ChallanInfo challanInfo) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("[ETA_SEND] Thread:").append(Thread.currentThread().getId()).append((what == 0 ? " SMS" : what == 1 ? " EMAIL" : " APP"))
			.append(" ").append(vehSetup.m_name).append(" Event:").append(SrcDestInfo.getNameForAlertTy(eventTy))
			.append(" Est ETA:").append(new java.util.Date(vehicleETA.getEstETA(specificETA)))
			.append(" Gps posn:").append(latestPosn.calcName(conn, vehicleId, vehSetup)).append(" Gps Time:").append( new java.util.Date(latestPosn.getGps_Record_Time()))
			.append(" Load Op:");
			OpStationBean bean = TripInfoCacheHelper.getOpStation(vehicleETA.getCurrFromOpStationId());
		    sb.append(bean == null ? "null": bean.getOpStationName());
		    sb.append(" Load Out:").append(new java.util.Date(vehicleETA.getCurrFroOpStationOutTime()));
		    sb.append("\n");
		    sb.append("[ETA_SEND] Thread:").append(Thread.currentThread().getId());
		    sb.append(" Uin:").append(new java.util.Date(vehicleETA.getCurrToInTime()));
		    sb.append(" UOut:").append(new java.util.Date(vehicleETA.getCurrToOutTime()));
		    sb.append(" UOp:");
		    bean = TripInfoCacheHelper.getOpStation(vehicleETA.getCurrToOpStationId());
		    sb.append(bean == null ? "null": bean.getOpStationName());
		    sb.append("\n");
		    if (challanInfo != null) {
		    	sb.append("[ETA_SEND] Thread:").append(Thread.currentThread().getId());
		    	sb.append(" Challan Date:").append(new java.util.Date(challanInfo.getChallanDate())).append(" Challan To:").append(challanInfo.getToLoc());
		    }
		    sb.append("\n");
		    sb.append("[ETA_SEND] Thread:").append(Thread.currentThread().getId()).append(" SentTo:");
		    if (what == 0) {
		    	sb.append(msgTargets == null ? null : msgTargets.smsList);
		    }
		    else if (what == 1) {
		    	sb.append(msgTargets == null ? null : msgTargets.emailList);
		    }
		    else if (what == 2) {
		    	sb.append(msgTargets == null ? null : msgTargets.notificationIdTargetLevel).append(" L1:").append(msgTargets.notificationL1).append(" L2:").append(msgTargets.notificationL2);	
		    }
		    sb.append("\n");
		    sb.append("[ETA_SEND] Thread:").append(Thread.currentThread().getId()).append(" Message:").append(format);

		    System.out.println(sb);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static ArrayList<MiscInner.Triple> sendNotifications(Connection conn, ResultOfWhoToSend msgTargets, String formatNotification, int vehicleId, int flexParam, int eventTy) {
		ArrayList<MiscInner.Triple> retval = new ArrayList<MiscInner.Triple>();
		//  result :1st = targetType, 2 = notificationTypeId, 3 = notificationId
		//ResultOfWhoToSendfirst = notification type id, second = target type, 3rd.first = if L1, 3rd.second = if L2
		WhoToSend l1l2 = null;
		for (int i=0,is=msgTargets.notificationIdTargetLevel == null ? 0 : msgTargets.notificationIdTargetLevel.size(); i<is; i++) {
			int notificationTypeId = msgTargets.notificationIdTargetLevel.get(i).first;
			int forTarget = msgTargets.notificationIdTargetLevel.get(i).second;
			boolean doingl1 = msgTargets.notificationIdTargetLevel.get(i).third.first;
			boolean doingl2 = msgTargets.notificationIdTargetLevel.get(i).third.second;
			if (doingl1 && doingl2 && l1l2 == null) {
				l1l2 = msgTargets.notificationL1;
				if (l1l2 == null)
					l1l2 = msgTargets.notificationL2;
				else {
					l1l2 = l1l2.copy();
					if (msgTargets.notificationL2 != null)
						l1l2.merge(msgTargets.notificationL2);
				}
			}
			int notificationId = NewETAAlertHelper.createNotification(conn, vehicleId, notificationTypeId, forTarget, doingl1 && doingl2 ? l1l2 : doingl1 ? msgTargets.notificationL1 : msgTargets.notificationL2);
			retval.add(new MiscInner.Triple(forTarget, notificationTypeId, notificationId));		//  result :1st = targetType, 2 = notificationTypeId, 3 = notificationId
		}
		return retval;
	}
	public static void sendSMS(Connection conn, ArrayList<String> theList, String formattedStr, int vehicleId, int flexParam, int eventTy) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is;i++) {
			CommunicatorDTO commDTO = new CommunicatorDTO();
			
			commDTO.setNotificationType(1);
			commDTO.setTo(theList.get(i));
			commDTO.setBody(formattedStr);
			
			commDTO.setForceSend(true);
			commDTO.setVehicleId(vehicleId);
			commDTO.setAlertIndex(flexParam);
			commDTO.setRuleId(eventTy);
			commDTO.setSubject("ETA Alerts");
			commDTO.setEngineEventId(Misc.getUndefInt());
			
			try {
				if (!g_alertSimulateMode)
					CommunicatorQueueSender.send(commDTO);
				else
					System.out.println("[ALERT SIM]: for: vehicle:"+vehicleId+" Event:"+SrcDestInfo.getNameForAlertTy(eventTy)+" To:"+commDTO.getTo()+" Body:"+commDTO.getBody());
			} 
			catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
	
	public static void sendEmail(Connection conn, ArrayList<String> theList, String formattedStr, int vehicleId, int flexParam, int eventTy) {
		for (int i=0,is=theList == null ? 0 : theList.size(); i<is;i++) {
			CommunicatorDTO commDTO = new CommunicatorDTO();
			
			commDTO.setNotificationType(2);
			commDTO.setTo(theList.get(i));
			commDTO.setBody(formattedStr);
			commDTO.setForceSend(true);
			commDTO.setVehicleId(vehicleId);
			commDTO.setAlertIndex(flexParam);
			commDTO.setRuleId(eventTy);
			commDTO.setSubject("ETA Alerts");
			commDTO.setEngineEventId(Misc.getUndefInt());
			
			try {
				if (!g_alertSimulateMode)
					CommunicatorQueueSender.send(commDTO);
				else
					System.out.println("[ALERT SIM]: vehicle:"+vehicleId+" Event:"+SrcDestInfo.getNameForAlertTy(eventTy)+" To:"+commDTO.getTo()+" Body:"+commDTO.getBody());
			} 
			catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
	
	public static boolean createAlertDB(Connection conn, int vehicleId, NewVehicleETA vehicleETA, NewETAforSrcDestItem specificETA, ChallanInfo challanInfo
			,int eventTy, int flexParam, long nowTime, GpsData latestPosn
			)  {//returns true if succ else false
		boolean retval = true;
		PreparedStatement ps = null;
		try {
			final String insertQ = "insert into eta_alerts_new(vehicle_id, created_on, trip_from_station_id, trip_from_in, trip_from_out "+
			" ,trip_to_station_id, trip_to_station_in, trip_to_station_out, challan_from_loc, challan_to_loc, challan_date, challan_id "+
			", src_dest_id, est_to_location_lon, est_to_location_lat, transit_dist, transit_time "+
			", event_type_id, event_flex_param, curr_eta, est_eta, latest_grt) "+
			" values ("+
			"?,?,?,?,?,"+
			"?,?,?,?,?,?,?,"+
			"?,?,?,?,?,"+
			"?,?,?,?,?"+
			")"
			;
			ps = conn.prepareStatement(insertQ);
			int colIndex = 1;
			//"insert into eta_alerts_new(vehicle_id, created_on, trip_from_station_id, trip_from_in, trip_from_out "+
			Misc.setParamInt(ps, vehicleId, colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(nowTime));
			Misc.setParamInt(ps, vehicleETA.getCurrFromOpStationId(), colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrFromOpStationInTime()));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrFroOpStationOutTime()));
			//" ,trip_to_station_id, trip_to_station_in, trip_to_station_out, challan_from_loc, challan_to_loc, challan_date, challan_id "+
			Misc.setParamInt(ps, vehicleETA.getCurrToOpStationId(), colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrToInTime()));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(vehicleETA.getCurrToOutTime()));
			ps.setString(colIndex++, challanInfo == null ? null : challanInfo.getFromLoc());
			ps.setString(colIndex++, challanInfo == null ? null : challanInfo.getToLoc());
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(challanInfo == null ? 0 : challanInfo.getChallanDate()));
			Misc.setParamInt(ps, challanInfo == null ? Misc.getUndefInt() : challanInfo.getId(), colIndex++);
			//", src_dest_id, est_to_location_lon, est_to_location_lat, transit_dist, transit_time, base_transit_dist, base_transit_time "+
			Misc.setParamInt(ps, vehicleETA.getCurrPossibleSrcDestList() == null || vehicleETA.getCurrPossibleSrcDestList().size() == 0 ? Misc.getUndefInt() : vehicleETA.getCurrPossibleSrcDestList().get(0).first, colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getDestLon(), colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getDestLat(), colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getTransitDist(), colIndex++);
			Misc.setParamDouble(ps, specificETA == null ? Misc.getUndefDouble() : specificETA.getTransitTime(), colIndex++);
			//", event_type_id, event_flex_param, curr_eta, est_eta) "+
			Misc.setParamInt(ps, eventTy, colIndex++);
			Misc.setParamInt(ps, flexParam, colIndex++);
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(specificETA == null ? 0 : vehicleETA.getCurrETA(specificETA)));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(specificETA == null ? 0 : vehicleETA.getEstETA(specificETA)));
			ps.setTimestamp(colIndex++, Misc.longToSqlDate(latestPosn == null ? 0 : latestPosn.getGps_Record_Time()));
			ps.execute();
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			retval = false;
			e.printStackTrace();
			//eat it
		}
		finally {
			ps = Misc.closePS(ps);
		}
		return retval;

	}
	
	
	public static class WhoToSend {
		public ArrayList<String> forSender = new ArrayList<String>();
		public ArrayList<String> forCustomer = new ArrayList<String>();
		public ArrayList<String> forTransporter = new ArrayList<String>();
		public ArrayList<String> unclassified = new ArrayList<String>();
		public WhoToSend copy() {
			WhoToSend retval = new WhoToSend();
			for (int art=0;art<4;art++) {
				ArrayList<String> toList= art == 0 ? retval.forSender : art == 1 ? retval.forTransporter : art == 2 ? retval.forCustomer : retval.unclassified;
				ArrayList<String> fromList= art == 0 ? this.forSender : art == 1 ? this.forTransporter : art == 2 ? this.forCustomer : this.unclassified;
				for (int j=0,js=fromList.size(); j<js; j++)
					toList.add(fromList.get(j));
			}
			return retval;
		}
		public WhoToSend merge(WhoToSend rhs) {
			for (int art=0;art<4;art++) {
				ArrayList<String> toList= art == 0 ? this.forSender : art == 1 ? this.forTransporter : art == 2 ? this.forCustomer : this.unclassified;
				ArrayList<String> fromList= art == 0 ? rhs.forSender : art == 1 ? rhs.forTransporter : art == 2 ? rhs.forCustomer : rhs.unclassified;
				for (int j=0,js=fromList.size(); j<js; j++) {
					boolean found = false;
					for (int i=0,is=toList.size();i<is;i++)
						if (toList.get(i).equals(fromList.get(j))) {
							found = true;
							break;
						}
					if (!found)
						toList.add(fromList.get(j));
				}
			}
			return this;
		}
		public boolean isEmpty() {
			return forSender.size() == 0 && forCustomer.size() == 0 && forTransporter.size() == 0 && unclassified.size() == 0;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int art=0;art<4;art++) {
				sb.append("[").append(art == 0 ? "Sender:" : art == 1 ? "Transporter:" : art == 2 ? "Customer:" :"Unclassified:");
				ArrayList<String> theList = art == 0 ? this.forSender : art == 1 ? this.forTransporter : art == 2 ? this.forCustomer : this.unclassified;
				Misc.convertInListToStr(theList, sb);
				sb.append("]");
			}
			return sb.toString();
		}
	}
	
	private static class ResultOfWhoToSend {
		public ArrayList<String> smsList;
		public ArrayList<String> emailList;
		public WhoToSend notificationL1;
		public WhoToSend notificationL2;
		public ArrayList<Triple<Integer, Integer, Pair<Boolean,Boolean>>> notificationIdTargetLevel;
		//first = notification type id, second = target type, 3rd.first = if L1, 3rd.second = if L2
		public boolean isEmpty() {
			return (smsList == null || smsList.size() == 0) && (emailList == null || emailList.size() == 0) && (notificationIdTargetLevel == null || notificationIdTargetLevel.size() == 0);
		}
	}

	public static ResultOfWhoToSend getWhoToSend(Connection conn,  int vehicleId, NewVehicleETA vehicleETA
			, SrcDestInfo srcDestInfo, int eventTy, int flexParam, boolean doChallanOnly
			, ChallanInfo challanInfo, StopDirControl stopDirControl, long nowTime) throws Exception {
		//return value: first = alertType, second list of WhoToSend
		
		ArrayList<SrcDestInfo.AlertSetting> alertList = srcDestInfo.getAlertSettingCalc(conn, eventTy);
		double flexKM = (double)flexParam/1000.0;
		double flexHr = (double)flexParam/(60.0);
		if (flexKM < 0)
			flexKM = 0;
		if (flexHr < 0)
			flexHr = 0;
		ArrayList<String> emailList = new ArrayList<String>();
		ArrayList<String> smsList = new ArrayList<String>();
		WhoToSend notificationL1 = null;
		WhoToSend notificationL2 = null;
		ArrayList<Triple<Integer, Integer, Pair<Boolean,Boolean>>> notificationIdsTargetLevel = new ArrayList<Triple<Integer, Integer, Pair<Boolean,Boolean>>>();
		//first = notification type id, second = target type, 3rd.first = if L1, 3rd.second = if L2
		for (int i=0,is=alertList == null ? 0 : alertList.size(); i<is; i++) {
			SrcDestInfo.AlertSetting alertSetting = alertList.get(i);
			
			boolean ofInterest =  alertSetting.isValidForSend(nowTime);
			if (!ofInterest)
				continue;
			if (SrcDestInfo.isDistBasedAlert(eventTy))
				ofInterest = Misc.isEqual(flexKM, alertSetting.getDist(),0.1,0.05);
			else if (SrcDestInfo.isTimeBasedAlert(eventTy))
				ofInterest = Misc.isEqual(flexHr, alertSetting.getDist(),0.01,0.01);
			else 
				ofInterest = true;
			
			int notificationTypeId = Misc.getUndefInt();
			int typeMsg = alertSetting.getAlertType();//0 = SMS, 1 = EMAIL, 2 = Notification
			int targetRole = 4; //1 = sender, 2 = transporter, 3 = consignee, 4 = unclassified
			int targetLevel = Misc.getUndefInt();
			if (typeMsg < 0) {
				typeMsg = 2;
				notificationTypeId = -1*typeMsg;
			}
			if (typeMsg < 0 || typeMsg > 2)
				ofInterest = false;
			if (!ofInterest)
				continue;
			int tempRoleFromUI = alertSetting.getAlertRole();
			 //<val id="0" name="Sender L1"/>
			 //  <val id="1" name="Sender L2"/>
			 //  <val id="2" name="Transporter L1"/>
			 //  <val id="3" name="Transporter L2"/>
			 //  <val id="4" name="Customer L1"/>
			 //  <val id="5" name="Customer L2"/>
			if (tempRoleFromUI == 0) {
				targetRole = SrcDestInfo.G_TARGET_SENDER;
				targetLevel = 1;
			}
			else if (tempRoleFromUI == 1) {
				targetRole = SrcDestInfo.G_TARGET_SENDER;
				targetLevel = 2;
			}
			else if (tempRoleFromUI == 2) {
				targetRole = SrcDestInfo.G_TARGET_TRANSPORTER;
				targetLevel = 1;
			}
			else if (tempRoleFromUI == 3) {
				targetRole = SrcDestInfo.G_TARGET_TRANSPORTER;
				targetLevel = 2;
			}
			else if (tempRoleFromUI == 4) {
				targetRole = SrcDestInfo.G_TARGET_RECEIVER;
				targetLevel = 1;
			}
			else if (tempRoleFromUI == 5) {
				targetRole = SrcDestInfo.G_TARGET_RECEIVER;
				targetLevel = 2;
			}
			else {
				targetRole = SrcDestInfo.G_TARGET_UNCLASSIFIED;
				targetLevel = Misc.getUndefInt();
			}
			
			if (alertSetting.getContactId() > 0 && !doChallanOnly) {
				if (typeMsg == SrcDestInfo.G_ALERT_FOR_TY_SMS)
					NewETAAlertHelper.addWhoToSendContact(smsList, conn, alertSetting.getContactId(), typeMsg);
				else if (typeMsg == SrcDestInfo.G_ALERT_FOR_TY_EMAIL)
					NewETAAlertHelper.addWhoToSendContact(emailList, conn, alertSetting.getContactId(), typeMsg);
				else if (false) {//currently not implemented for user name from target
					if (notificationL1 == null)
						notificationL1 = getWhoToSendFromNonSrcDestForNotification(conn, vehicleId, vehicleETA, challanInfo, 1, stopDirControl, doChallanOnly);
					NewETAAlertHelper.addWhoToSendContact(notificationL1.unclassified, conn, alertSetting.getContactId(), typeMsg);
					addToNotificationIdsTargetLevel(notificationIdsTargetLevel, notificationTypeId, SrcDestInfo.G_TARGET_UNCLASSIFIED, 1);
				}
			}
			if (typeMsg == SrcDestInfo.G_ALERT_FOR_TY_SMS)
				NewETAAlertHelper.getWhoToSendFromNonSrcDest(conn, smsList, vehicleId, vehicleETA, challanInfo, typeMsg, targetLevel, stopDirControl, doChallanOnly, targetRole);
			else if (typeMsg == SrcDestInfo.G_ALERT_FOR_TY_EMAIL)
				NewETAAlertHelper.getWhoToSendFromNonSrcDest(conn, emailList, vehicleId, vehicleETA, challanInfo, typeMsg, targetLevel, stopDirControl, doChallanOnly, targetRole);
			else {//currently not implemented for user name from target
				if (targetLevel == 1 && notificationL1 == null)
					notificationL1 = getWhoToSendFromNonSrcDestForNotification(conn, vehicleId, vehicleETA, challanInfo, 1, stopDirControl, doChallanOnly);
				if (targetLevel == 2 && notificationL2 == null)
					notificationL2 = getWhoToSendFromNonSrcDestForNotification(conn, vehicleId, vehicleETA, challanInfo, 2, stopDirControl, doChallanOnly);
				addToNotificationIdsTargetLevel(notificationIdsTargetLevel, notificationTypeId, targetRole, targetLevel);
			}
		}
		ResultOfWhoToSend retval = new ResultOfWhoToSend();
		retval.smsList = smsList;
		retval.emailList = emailList;
		retval.notificationL1 = notificationL1;
		retval.notificationL2 = notificationL2;
		retval.notificationIdTargetLevel = notificationIdsTargetLevel;
		return retval;
	}
	
	private static void addToNotificationIdsTargetLevel(ArrayList<Triple<Integer, Integer, Pair<Boolean,Boolean>>> theList, int notificationId, int target, int level) {
		boolean found = false;
		for (int t1=0,t1s=theList.size();t1<t1s;t1++) {
			Triple<Integer, Integer, Pair<Boolean,Boolean>> entry = theList.get(t1);
			if (entry.first == notificationId && entry.second == target) {
				found = true;
				if (level == 1)
					entry.third.first =  true;
				else if (level == 2)
					entry.third.second = true;
				break;
			}
		}
		if (!found) {
			theList.add(new Triple<Integer, Integer, Pair<Boolean,Boolean>>(notificationId, target, new Pair<Boolean, Boolean>(level == 1, level == 2)));
		}
	}
	
	private static WhoToSend getWhoToSendFromNonSrcDestForNotification(Connection conn, int vehicleId, NewVehicleETA vehicleETA, ChallanInfo challanInfo, int level, StopDirControl stopDirControl, boolean doChallanOnly) throws Exception {
		WhoToSend retval = new WhoToSend();
		getWhoToSendFromNonSrcDest(conn, retval.forSender, vehicleId, vehicleETA, challanInfo, SrcDestInfo.G_ALERT_FOR_TY_NOTIFICATION, level, stopDirControl, doChallanOnly, SrcDestInfo.G_TARGET_SENDER);
		getWhoToSendFromNonSrcDest(conn, retval.forTransporter, vehicleId, vehicleETA, challanInfo, SrcDestInfo.G_ALERT_FOR_TY_NOTIFICATION, level, stopDirControl, doChallanOnly, SrcDestInfo.G_TARGET_TRANSPORTER);
		getWhoToSendFromNonSrcDest(conn, retval.forCustomer, vehicleId, vehicleETA, challanInfo, SrcDestInfo.G_ALERT_FOR_TY_NOTIFICATION, level, stopDirControl, doChallanOnly, SrcDestInfo.G_TARGET_RECEIVER);
		return retval;
	}
	
	private static void getWhoToSendFromNonSrcDest(Connection conn, ArrayList<String> addToThisList, int vehicleId, NewVehicleETA vehicleETA, ChallanInfo challanInfo, int getWhat, int level, StopDirControl stopDirControl, boolean doChallanOnly, int targetType) throws Exception {
		//for what = 1 => email, 2 => sms, 3 => notification
		
		NewETAAlertHelper.addWhoToSendChallan(addToThisList, conn, challanInfo, getWhat, level, targetType, stopDirControl);
		IdInfo dest = challanInfo == null ? null : challanInfo.getIdInfoWithCalc(conn, false, false, stopDirControl);
		if (dest != null && dest.getDestIdType() == 3) {
			int toOp = dest.getDestId();
			NewETAAlertHelper.addWhoToSendOpStation(addToThisList, conn, toOp, getWhat, level, targetType);
		}
		if (doChallanOnly)
			return;
		CacheTrack.VehicleSetup vehsetup = CacheTrack.VehicleSetup.getSetup(vehicleId,conn);
		NewETAAlertHelper.addWhoToSendPort(addToThisList, conn, vehsetup == null ? Misc.getUndefInt() : vehsetup.m_ownerOrgId, getWhat, level, targetType);
		
		int fromOp = vehicleETA.getCurrFromOpStationId();
		NewETAAlertHelper.addWhoToSendOpStation(addToThisList, conn, fromOp, getWhat, level, targetType);
		
	}
	private static void addWhoToSendPort(ArrayList<String> addToThisList, Connection conn, int portNodeId, int getWhat, int level, int targetType) throws Exception {
		//getWhat = 0=sms, 1=email 2=notification 
		Cache cache = Cache.getCacheInstance(conn);
		MiscInner.PortInfo port = cache.getPortInfo(portNodeId, conn);
		if (port != null) {
			//helperAddWhoTo(WhoToSend result, String items, int inList) {
			//inList = 1 => sender, 2=>transporter, 3=>consignee, 4=>unclassified
			
			if (level == 1 || Misc.isUndef(level)) {
				int paramId = Misc.getUndefInt();
				if (targetType == SrcDestInfo.G_TARGET_SENDER)
					paramId = getWhat == 1 ? OrgConst.SENDER_L1_EMAIL : getWhat == 0 ? OrgConst.SENDER_L1_PHONE : OrgConst.SENDER_L1_USER;
				else if (targetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					paramId = getWhat == 1 ? OrgConst.TRANSPORTER_L1_EMAIL : getWhat == 0 ? OrgConst.TRANSPORTER_L1_PHONE : OrgConst.TRANSPORTER_L1_USER;
				else
					paramId = getWhat == 1 ? OrgConst.CONSIGNEE_L1_EMAIL : getWhat == 0 ? OrgConst.CONSIGNEE_L1_PHONE : OrgConst.CONSIGNEE_L1_USER;
				ArrayList<String> params = port.getStringParams(paramId);
				for (int j=0,js=params == null ? 0 : params.size();j<js;j++) {
					NewETAAlertHelper.helperAddWhoTo(addToThisList, params.get(j), getWhat == 0);
				}
			}
			if (level == 2 || Misc.isUndef(level)) {
				int paramId = Misc.getUndefInt();
				if (targetType == SrcDestInfo.G_TARGET_SENDER)
					paramId = getWhat == 1 ? OrgConst.SENDER_L2_EMAIL : getWhat == 0 ? OrgConst.SENDER_L2_PHONE : OrgConst.SENDER_L2_USER;
				else if (targetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					paramId = getWhat == 1 ? OrgConst.TRANSPORTER_L2_EMAIL : getWhat == 0 ? OrgConst.TRANSPORTER_L2_PHONE : OrgConst.TRANSPORTER_L2_USER;
				else
					paramId = getWhat == 1 ? OrgConst.CONSIGNEE_L2_EMAIL : getWhat == 0 ? OrgConst.CONSIGNEE_L2_PHONE : OrgConst.CONSIGNEE_L2_USER;
				ArrayList<String> params = port.getStringParams(paramId);
				for (int j=0,js=params == null ? 0 : params.size();j<js;j++) {
					NewETAAlertHelper.helperAddWhoTo(addToThisList, params.get(j), getWhat == 0);
				}
			}
		}
	}
	
	private static void addWhoToSendOpStation(ArrayList<String> addToThisList, Connection conn, int opstationId, int getWhat, int level, int forTargetType) {
		OpStationBean opbean = TripInfoCacheHelper.getOpStation(opstationId);
		if (level == 1 || Misc.isUndef(level)) {
			if (getWhat == 1) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertEmailL1Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertEmailL1Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertEmailL1Customer(), getWhat == 0);
			}
			else if (getWhat == 0) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertPhoneL1Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertPhoneL1Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertPhoneL1Customer(), getWhat == 0);
			}
			else {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertUserL1Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertUserL1Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertUserL1Customer(), getWhat == 0);
			}
		}
		if (level == 2 || Misc.isUndef(level)) {
			if (getWhat == 1) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertEmailL2Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertEmailL2Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertEmailL2Customer(), getWhat == 0);
			}
			else if (getWhat == 0) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertPhoneL2Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertPhoneL2Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertPhoneL2Customer(), getWhat == 0);
			}
			else {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertUserL2Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertUserL2Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, opbean.getAlertUserL2Customer(), getWhat == 0);
			}
		}
	}
	
	private static void addWhoToSendChallan(ArrayList<String> addToThisList, Connection conn, ChallanInfo challanInfo, int getWhat, int level, int forTargetType, StopDirControl stopDirControl) {
		//for what = 1 => email, 2 => sms, 3 => notification
		if (challanInfo == null)
			return;
		if (level == 1 || Misc.isUndef(level)) {
			if (getWhat == 1) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertEmailL1Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertEmailL1Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertEmailL1Customer(), getWhat == 0);
				else
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertMailId(conn, stopDirControl), getWhat == 0);
			}
			else if (getWhat == 0) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhoneL1Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhoneL1Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhoneL1Customer(), getWhat == 0);
				else
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhone(conn, stopDirControl), getWhat == 0);
			}
			else {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertUserL1Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertUserL1Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertUserL1Customer(), getWhat == 0);
			}
		}
		if (level == 2 || Misc.isUndef(level)) {
			if (getWhat == 1) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertEmailL2Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertEmailL2Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertEmailL2Customer(), getWhat == 0);
				else
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertMailId(conn, stopDirControl), getWhat == 0);
			}
			else if (getWhat == 0) {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhoneL2Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhoneL2Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhoneL2Customer(), getWhat == 0);
				else
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertPhone(conn, stopDirControl), getWhat == 0);
			}
			else {
				if (forTargetType == SrcDestInfo.G_TARGET_SENDER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertUserL2Sender(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_TRANSPORTER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertUserL2Transporter(), getWhat == 0);
				else if (forTargetType == SrcDestInfo.G_TARGET_RECEIVER)
					NewETAAlertHelper.helperAddWhoTo(addToThisList, challanInfo.getAlertUserL2Customer(), getWhat == 0);
			}
		}
	}
	
	private static void helperAddWhoTo(ArrayList<String> addToThisList, String items,  boolean standardizeAsPhone) {
		if (items == null)
			return;
		String itemList[] = items.split("[;,| ]");
		for (int i=0,is=itemList.length;i<is;i++) {
			String entry = itemList[i];
			if (entry == null)
				continue;
			entry = entry.trim();
			entry = entry.toUpperCase();
			if (standardizeAsPhone) {
				if (entry.startsWith("+91"))
					entry = entry.substring(3);
				if (entry.startsWith("0"))
					entry = entry.substring(1);
				entry = entry.replaceAll("\\(", "");
				entry = entry.replaceAll("\\)", "");
				entry = entry.replaceAll("-", "");
				entry = entry.replaceAll(" ", "");
				entry = entry.trim();
			}
			if (entry.length() == 0)
				continue;
			boolean found = false;
			for (int j=0,js = addToThisList == null ? 0 : addToThisList.size();j<js;j++ ) {
				if (addToThisList.get(j).equals(entry)) {
					found = true;
					break;
				}
			}
			if (!found) {
				addToThisList.add(entry);
			}
		}
	}
	
	private static void addWhoToSendContact(ArrayList<String> addToThisList, Connection conn, int contactId, int forWhat)  throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("select id, email, mobile,phone from customer_contacts where id = ?");
			ps.setInt(1, contactId);
			rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String em = rs.getString(2);
				String ph = rs.getString(3);
				String mo = rs.getString(4);
				if (forWhat == 0) {
					NewETAAlertHelper.helperAddWhoTo(addToThisList, ph, true);
					NewETAAlertHelper.helperAddWhoTo(addToThisList, mo, true);
				}
				if (forWhat == 1) {
					NewETAAlertHelper.helperAddWhoTo(addToThisList, em, false);
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		
	}

	
	private static Pair<ArrayList<String>, ArrayList<String>> getEmailPhoneForAlertNotUsed(Connection conn, ArrayList<Integer> emailList, ArrayList<Integer> phoneList, String otherPhone, String otherEmail) {
		ArrayList<String> email = new ArrayList<String> ();
		ArrayList<String> phone = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (otherEmail != null)
				otherEmail = otherEmail.trim();
			if (otherPhone != null)
				otherPhone = otherPhone.trim();
			if (otherEmail != null && otherEmail.length() == 0)
				otherEmail = null;
			if (otherPhone != null && otherPhone.length() == 0)
				otherPhone = null;
			if (otherEmail != null) {
				String[] list = otherEmail.split("[;,| ]");
				for (int i=0,is = list==null ? 0 : list.length;i<is;i++) {
					String s = list[i];
					if (s == null)
						continue;
					s = s.trim();
					if (s.length() <= 1)
						continue;
					if (email.indexOf(s) >= 0)
						continue;
					email.add(s);
				}
			}
			if (otherPhone != null) {
				String[] list = otherPhone.split("[;,| ]");
				for (int i=0,is = list==null ? 0 : list.length;i<is;i++) {
					String s = list[i];
					if (s == null)
						continue;
					s = s.trim();
					if (s.startsWith("+91"))
						s = s.substring(3);
					if (s.startsWith("0"))
						s = s.substring(1);
					s = s.trim();
					if (s.length() <= 1)
						continue;
					if (phone.indexOf(s) >= 0)
						continue;
					phone.add(s);
				}
			}
			if (emailList.size() > 0 || phoneList.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("select id, email, mobile,phone from customer_contacts where id in (");
				if (emailList.size() > 0) {
					Misc.convertInListToStr(emailList, sb);
					if (phoneList.size() > 0)
						sb.append(",");
				}
				if (phoneList.size() > 0) {
					Misc.convertInListToStr(phoneList, sb);
				}
				sb.append(") ");
				ps = conn.prepareStatement(sb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					int id = rs.getInt(1);
					String em = rs.getString(2);
					String ph = rs.getString(3);
					String mo = rs.getString(4);
					if (em != null)
						em = em.trim();
					if (ph != null)
						ph = ph.trim();
					if (mo !=null)
						mo = mo.trim();
					if (em != null && em.length() == 0)
						em = null;
					if (ph != null && ph.length() == 0)
						ph = null;
					if (mo != null && mo.length() == 0)
						mo = null;
					if (ph != null && ph.startsWith("+91"))
						ph = ph.substring(3);
					if (mo != null && mo.startsWith("+91"))
						mo = mo.substring(3);
					if (ph != null && ph.startsWith("0"))
						ph = ph.substring(1);
					if (mo != null && mo.startsWith("0"))
						mo = mo.substring(1);
					if (mo != null)
						mo = mo.trim();
					if (ph != null)
						ph = ph.trim();
					if ("".equals(mo))
						mo = null;
					if ("".equals(ph))
						ph = null;
					if (mo == null)
						mo = ph;
					if (phoneList != null && phoneList.indexOf(id) >= 0 && phone.indexOf(mo) < 0) {
						phone.add(mo);
					}
					if (emailList != null && emailList.indexOf(id) >= 0 && email.indexOf(em) < 0) {
						email.add(em);
					}
				}
				rs = Misc.closeRS(rs);
				ps = Misc.closePS(ps);
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		return new Pair<ArrayList<String>, ArrayList<String>>(email, phone);
	}
	
	private static ArrayList<Integer> helperAddToListNotUsed(ArrayList<Integer> theList, int item) {
		boolean found = false;
		for (int i=0,is = theList == null ? 0 : theList.size(); i<is; i++) {
			if (theList.get(i) == item) {
				found = true;
				break;
			}
		}
		if (!found) {
			if (theList == null)
				theList = new ArrayList<Integer>();
			theList.add(item);
		}
		return theList;
	}
	// ****** FOR external Notification interface
	public static void closeNotification(Connection conn, int notificationId) {
		//TODO DEBUG
	}
	public static boolean isOpenNotification(Connection conn, int notificationId) {
		return notificationId > 0;//TODO
	}
	public static int createNotification(Connection conn, int vehicleId, int notificationTypeId, int forTarget, WhoToSend whoToSend) {
		
		if (NewETAAlertHelper.g_alertSimulateMode) {
			System.out.println("[ETA_APP_NOTIFY] NotificationTypeId:"+notificationTypeId+", forTarget:"+forTarget+" WhoToSend:"+whoToSend);
		}
		return Misc.getUndefInt();
	}
	// ****** 
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CDHEmailInfo test1 = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), 3363147, 481, null, Misc.getUndefInt(), Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble());
			System.out.println(test1);
			test1 = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), 123, 481, null, Misc.getUndefInt(),  Misc.getUndefDouble(), Misc.getUndefDouble(), Misc.getUndefDouble());
			System.out.println(test1);
			test1 = CDHEmailInfo.getCDHInfo(conn, Misc.getUndefInt(), Misc.getUndefInt(), 481, null, Misc.getUndefInt(),  76.1177632212639, 28.7795553304385, Misc.getUndefDouble());
			System.out.println(test1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null)
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}
//		Tester.callMain(args);
	}
}

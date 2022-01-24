package com.ipssi.gen.deviceMessaging;

import java.sql.Connection;
import java.util.ArrayList;

import com.ipssi.common.ds.trip.OpStationBean;
import com.ipssi.common.ds.trip.TripInfoCacheHelper;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.CacheTrack.VehicleSetup;
import com.ipssi.miningOpt.SiteStats;

public class MiningCmd {
	public static boolean sendMessage(Connection conn, int vehicleId, int destId, DestTypeEnum destType, int siteIdOfShovel, MessageTypeEnum message, String genericMessage) {
		boolean retval = false;
		try {
			CacheTrack.VehicleSetup shovelSetup = null;
			CacheTrack.VehicleSetup vehicleSetup = null;
			int shovelId = Misc.getUndefInt();
			SiteStats.InvPile siteInfo = null;
			OpStationBean opb = null;
			vehicleSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			if (vehicleSetup == null)
				return false;
			SiteStats.loadInvPileOnly(conn, false);
			if (destType == DestTypeEnum.SHOVEL) {
				shovelSetup = CacheTrack.VehicleSetup.getSetup(destId, conn);
				if (!Misc.isUndef(siteIdOfShovel)) {
					siteInfo = SiteStats.getInvPile(siteIdOfShovel);	
				}
				 
			}
			else if (destType == DestTypeEnum.UNLOAD) {
				siteInfo = SiteStats.getInvPile(destId);
			}
			else if (destType == DestTypeEnum.REST) {
				opb = TripInfoCacheHelper.getOpStation(destId);
			}
			String formattedMessage = null;
			if (vehicleSetup.deviceModelInfoId == 35) {
				formattedMessage = getGalileoFmtMessage(message, shovelSetup, siteInfo, opb, 6, genericMessage);
			}
			if (formattedMessage != null) {
				retval = MessageCache.addMessage(conn, vehicleId, formattedMessage, 0, true, MessageCache.REMOVE_ALL_NON_LATEST, MessageCache.REMOVE_ALL_NON_LATEST_OR_SENT) != null; 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			
		}
		return retval;
	}
	
	public static String getGalileoFmtMessage(MessageTypeEnum messageType, CacheTrack.VehicleSetup shovelSetup, SiteStats.InvPile siteInfo, OpStationBean opb, int messageLen, String otherMessage) {
		StringBuilder sb = new StringBuilder();
		sb.append("RS232 ");
		
		if (messageType == MessageTypeEnum.GOTO_SHOVEL) {
			String shovelCode = shovelSetup.getShortCode();
			if (shovelCode == null) {
				shovelCode = shovelSetup.m_name;
			}
			shovelCode = shovelCode.trim();
			if (shovelCode.length() > 3) {
				shovelCode = shovelCode.substring(shovelCode.length()-3, shovelCode.length());
			}
			shovelCode = shovelCode.trim();
			int charsRemainingForSiteCode = 6-shovelCode.length(); 
			String siteCode = null;
			if (siteInfo != null) {
				int benchId = siteInfo.getBenchId();
				int directionId = siteInfo.getDirectionId();
				DimInfo benchDimInfo = DimInfo.getDimInfo(83141);
				DimInfo directionDimInfo = DimInfo.getDimInfo(83142);
				if (!Misc.isUndef(benchId) && benchDimInfo != null) {
					DimInfo.ValInfo val = benchDimInfo.getValInfo(benchId);
					siteCode = val != null ? val.m_name : Integer.toString(benchId);
				}
				if (siteCode != null && siteCode.length() > 3)
					siteCode = siteCode.substring(3);
				charsRemainingForSiteCode -= siteCode == null ? 0 : siteCode.length();
				if (!Misc.isUndef(directionId) && directionDimInfo != null) {
					DimInfo.ValInfo val = directionDimInfo.getValInfo(directionId);
					String dirName = val.m_name;
					if (dirName.length() > charsRemainingForSiteCode)
						dirName = dirName.substring(charsRemainingForSiteCode);
					if (siteCode == null) {
						siteCode = val != null ? dirName : null;
					}
					else {
						siteCode +=( val != null ? dirName : "");
					}
				}
				if (siteCode == null)
					siteCode = siteInfo.getName();
				if (siteCode != null && siteCode.length() > 3)
					siteCode = siteCode.substring(siteCode.length()-3, siteCode.length());
			}
			if (siteCode != null) {
				siteCode = siteCode.trim();
				sb.append(siteCode);
				if (shovelCode.length() >= 3 && siteCode.length() == 3) {//remove a char from siteCode if it is ending in numeric
					if (Character.isDigit(siteCode.charAt(2)))
						siteCode = siteCode.substring(3);
				}
			}
			if (((siteCode == null ? 0 : siteCode.length())+shovelCode.length()) < 6)
				sb.append(" ");
			sb.append(shovelCode);
		}
		else if (messageType == MessageTypeEnum.GOTO_UNLOAD) {
			String name = siteInfo.getName();
			if (name.length() > 6)
				name = name.substring(6);
			sb.append(name);
		}
		else {
			DimInfo messageDim = DimInfo.getDimInfo(75022);
			String msg = null;
			if (messageDim != null) {
				DimInfo.ValInfo val = messageDim.getValInfo(messageType.getOrdinal());
				if (val != null)
					msg = val.m_name;
			}
			if (msg == null) {
				if (messageType == MessageTypeEnum.OTHER) {
					sb.append(otherMessage.length() > 6 ? otherMessage.substring(6) : otherMessage);
				}
				if (messageType == MessageTypeEnum.MAINTENANCE_BREAK) {
					sb.append("MBREAK");
				}
				else if (messageType == MessageTypeEnum.OFF_DUTY) {
					sb.append("OFF DU");
				}
				else if (messageType == MessageTypeEnum.STOP_NOW) {
					sb.append("STOP");
				}
				else if (messageType == MessageTypeEnum.TAKE_BREAK) {
					sb.append("CHAI");
				}
				else if (messageType == MessageTypeEnum.TAKE_FUEL) {
					sb.append("FUEL");
				}
			}//if no msg from dim
			if (msg != null && msg.length() > 6)
				msg = msg.substring(6);
			if (msg != null)
				sb.append(msg);
		}//if fixed message
		return sb.length() == "RS232 ".length() ? null : sb.toString();
	}
	
	public enum MessageTypeEnum {
		GOTO_SHOVEL(0), GOTO_UNLOAD(1), TAKE_BREAK(2), OFF_DUTY(3), TAKE_FUEL(4), STOP_NOW(5), MAINTENANCE_BREAK(6), OTHER(7);
		int ordinal;
		private MessageTypeEnum(int ordinal) {
			this.ordinal = ordinal;
		}
		public int getOrdinal() {
			return this.ordinal;
		}
		public static String toString(MessageTypeEnum v) {
			switch (v) {
			//GOTO_SHOVEL(0), GOTO_UNLOAD(1), TAKE_BREAK(2), OFF_DUTY(3), TAKE_FULE(4), STOP_NOW(5), MAINTENANCE_BREAK(6), OTHER(7);
				case GOTO_SHOVEL: return "SHOVEL";
				case GOTO_UNLOAD: return "UNLOAD";
				case TAKE_BREAK: return "Break";
				case OFF_DUTY: return "Off";
				case TAKE_FUEL: return "Fuel";
				case STOP_NOW: return "Stop";
				case MAINTENANCE_BREAK: return "Maintenance";
				default: return "Unk";
			}
		}
	}

	public enum DestTypeEnum {
		SHOVEL(0), UNLOAD(1), REST(2);
		int ordinal;
		private DestTypeEnum(int ordinal) {
			this.ordinal = ordinal;
		}
		public int getOrdinal() {
			return this.ordinal;
		}
		public static String toString(DestTypeEnum v) {
			switch (v) {
				case SHOVEL: return "SHOVEL";
				case UNLOAD: return "UNLOAD";
				case REST: return "Rest";
				default: return "Unk";
			}
		}
	}
	public static void main(String[] args) {
		Connection conn = null;
		int vehicleId = 27290;
		int shovelId = 27350;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			boolean succ = MiningCmd.sendMessage(conn, vehicleId, shovelId, DestTypeEnum.SHOVEL, 179, MessageTypeEnum.GOTO_SHOVEL, null);
			ArrayList<Message> tosend = MessageCache.getMessagesToSend(vehicleId, conn);
			shovelId = 27351;
			succ = MiningCmd.sendMessage(conn, vehicleId, shovelId, DestTypeEnum.SHOVEL, 179, MessageTypeEnum.GOTO_SHOVEL, null);
			shovelId = 27352;
			succ = MiningCmd.sendMessage(conn, vehicleId, shovelId, DestTypeEnum.SHOVEL, 179, MessageTypeEnum.GOTO_SHOVEL, null);
			tosend = MessageCache.getMessagesToSend(vehicleId, conn);
			int dbg = 1;
			dbg++;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, false);
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it
				}
			}
		}
	}
}

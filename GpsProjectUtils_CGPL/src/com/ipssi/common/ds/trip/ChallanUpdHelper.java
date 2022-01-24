package com.ipssi.common.ds.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.ipssi.communicator.dto.TPQueueSender;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.StopDirControl;
import com.ipssi.gen.utils.Triple;
import com.ipssi.userNameUtils.IdInfo;

public class ChallanUpdHelper {
	public static void sendMessageOnUpdate(Connection conn, ArrayList<Integer> challanIdList) throws Exception {
		if (!conn.getAutoCommit())
			conn.commit();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (challanIdList == null || challanIdList.size() == 0)
				return;
			StringBuilder sb = new StringBuilder();
			sb.append(" select distinct vheicle_id from challan_details where id in (");
			Misc.convertInListToStr(challanIdList, sb);
			sb.append(")");
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			ArrayList<Integer> tosend = new ArrayList<Integer>();
			while (rs.next()) {
				int vehId = rs.getInt(1);
				tosend.add(vehId);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			if (!conn.getAutoCommit())
				conn.commit();
			if (tosend != null && tosend.size() > 0) {
				System.out.println("[ChallanUpdHelper UpdChallan called:");
				String serverName = Misc.getServerName();
				for (Integer itId : tosend) {
					TPQueueSender.send(new Triple<Integer, Integer,String> (itId,31,serverName));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		finally {
			try {
				if (rs != null)
					rs.close();
			}
			catch (Exception e1) {
				
			}
			try {
				if (ps != null)
					ps.close();
			}
			catch (Exception e1) {
				
			}
		}
			
	}
	public static void postProcessChallanUpdate(Connection conn, ArrayList<Integer> challanIdList) throws Exception {
		if (challanIdList == null || challanIdList.size() == 0)
			return;
		StringBuilder sb = new StringBuilder();
		sb.append(" update challan_details set trip_status = (case when at_cust_arrival is not null then 3 when delivery_date is not null then 2 else trip_status end), updated_on=now(), challan_rec_date=now() where id in (");
		Misc.convertInListToStr(challanIdList, sb);
		sb.append(")");
		PreparedStatement ps = conn.prepareStatement(sb.toString());
		ps.executeUpdate();
		ps.close();
		sb.setLength(0);
		sb.append("update challan_details join challan_dispatch_item on (challan_details.id = challan_dispatch_item.challan_id and received_qty is not null) set trip_status=2, delivery_date=now(), updated_on=now(), challan_rec_date=now() ");
		sb.append(" where challan_details.trip_status in (1,3) and challan_details.id in (");
		Misc.convertInListToStr(challanIdList, sb);
		sb.append(")");
		ps = conn.prepareStatement(sb.toString());
		ps.executeUpdate();
		ps.close();
	}

	public static void postProcessChallanCreate(Connection conn, ArrayList<Integer> challanIdList) throws Exception {
		ArrayList<Pair<Integer, ChallanInfo>> challanInfoList = ChallanInfo.read(conn, challanIdList);
		TripInfoCacheHelper.initOpsBeanRelatedCache(conn, new ArrayList<Integer>());//to initialize
		//1. pop from/to location if fromStationId/toStationId
		//2. get Invoice DistKM and calculate if possible and 
		//3. populate eta ..
		
		int prevVehicleId = Misc.getUndefInt();
		ChallanInfo prevChallanInfo = null;
		double travelDistFactor = 1.3;
		double kmPerDay = 250;
		double maxDaysPerDelivery = 0.5; //in case multi challan per pt for diff delivery, then days added for each delivery and dist acc calc
		CacheTrack.VehicleSetup vehSetup = null;
		StopDirControl stopDirControl = null;
		IdInfo prevIdInfo = null;
		Cache cache = Cache.getCacheInstance(conn);
		
		double cummDur = 0;
		for (int i=0,is=challanInfoList == null ? 0 : challanInfoList.size(); i<is; i++) {
			int vehicleId = challanInfoList.get(i).first;
			if (prevVehicleId != vehicleId) {
				prevChallanInfo = null;
				vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
				stopDirControl = vehSetup.getStopDirControl(conn);
				Triple<Double, Double, Double> param = getDistFactorKmPerDayDeliveryTime(conn, vehSetup);
				travelDistFactor = param.first;
				kmPerDay = param.second;
				maxDaysPerDelivery = param.third; 
				prevIdInfo = null;
				cummDur = 0;
			}
			
			ChallanInfo ch = challanInfoList.get(i).second;
			
			String fromLocationToSet = null;
			String toLocationToSet = null;
			if (!Misc.isUndef(ch.getFromStationId())) {
				OpStationBean op = TripInfoCacheHelper.getOpStation(ch.getFromStationId());
				if (ch.getFromLoc() == null && op != null)
					fromLocationToSet = op.getOpStationName();
			}
			if (!Misc.isUndef(ch.getToStationId())) {
				OpStationBean op = TripInfoCacheHelper.getOpStation(ch.getToStationId());
				if (ch.getToLoc() == null && op != null)
					toLocationToSet = op.getOpStationName();
			}
			double invoiceDistKM = ch.getSimpleInvoiceDistKM();
			if (invoiceDistKM < 0.0005)
				invoiceDistKM = Misc.getUndefDouble();
			double authTimeDays = Misc.getUndefDouble();
			long origETA = ch.getOrigETA();
			if (origETA > 0) {
				authTimeDays = (origETA - ch.getChallanDate())/(24*3600*1000);
			}
			if (Misc.isUndef(invoiceDistKM) || Misc.isUndef(authTimeDays)) {			
				IdInfo dest = ch.getIdInfoWithCalc(conn, false, stopDirControl);
				IdInfo src = null;
				if (prevIdInfo != null && prevChallanInfo != null && (ch.getChallanDate() - prevChallanInfo.getChallanDate()) < 60000) {
					src = prevIdInfo;
					cummDur += maxDaysPerDelivery;
				}
				else {
					src = ch.getSrcIdInfoWithCalc(conn, false, stopDirControl);
					cummDur = 0;
				}
				prevIdInfo = dest;
				Pair<Double, Double> authDistTime = getAuthDistMinForSrcToDes(conn, src, dest, vehicleId, travelDistFactor, kmPerDay);
				if (Misc.isUndef(invoiceDistKM)) 
					invoiceDistKM = authDistTime.first;
				if (Misc.isUndef(authTimeDays) && !Misc.isUndef(authDistTime.second))
					authTimeDays = authDistTime.second/(24*60) + cummDur;
			}
			//now update ..
			StringBuilder sb = new StringBuilder();
			boolean added = false;
			sb.append("update challan_details set trip_status=1 ");
			added = true;
			ArrayList<Object> params = new ArrayList<Object>();
			if (fromLocationToSet != null) {
				sb.append(added ? "," : "").append(" from_location = ?");
				params.add(fromLocationToSet);
				added = true;
			}
			if (toLocationToSet != null) {
				sb.append(added ? "," : "").append(" to_location = ?");
				params.add(toLocationToSet);
				added = true;
			}
			if (!Misc.isUndef(invoiceDistKM)) {
				sb.append(added ? "," : "").append("invoice_distkm = ?");
				params.add(new Double(invoiceDistKM));
				added = true;
			}
			if (origETA <= 0) {
				long tsToSet = ch.getChallanDate() + (long) (authTimeDays*24*3600*1000);
				Timestamp ts = Misc.longToSqlDate(tsToSet);
				sb.append(added ? "," : "").append("orig_eta = ?, curr_eta = ?");
				params.add(ts);
				params.add(ts);
				added = true;
			}
			if (added) {
				sb.append(" where id = ? ");
				params.add(new Integer(ch.getId()));
				PreparedStatement ps = conn.prepareStatement(sb.toString());
				for (int i1=0,i1s=params.size();i1<i1s;i1++) {
					Object pr = params.get(i1);
					if (pr == null) {
						ps.setString(i1+1, null);
					}
					else if (pr instanceof String) {
						ps.setString(i1+1, (String)pr);
					}
					else if (pr instanceof Double) {
						ps.setDouble(i1+1, ((Double) pr).doubleValue());
					}
					else if (pr instanceof java.sql.Timestamp) {
						ps.setTimestamp(i1+1, (Timestamp) pr);
					}
					else if (pr instanceof Integer) {
						ps.setInt(i1+1, ((Integer) pr).intValue());
					}
				}
				ps.executeUpdate();
				ps.close();
			}
			prevVehicleId = vehicleId;
			prevChallanInfo = ch;
			cummDur = authTimeDays;
		}
		
		postProcessChallanUpdate(conn, challanIdList);
	}
	 
	public static Triple<Double, Double, Double> getDistFactorKmPerDayDeliveryTime(Connection conn, CacheTrack.VehicleSetup vehSetup) {
		double travelDistFactor = 1.3;
		double kmPerDay = 250;
		double maxDaysPerDelivery = 0.5; //in case multi challan per pt for diff delivery, then days added for each delivery and dist acc calc
		
		try {
			Cache cache = Cache.getCacheInstance(conn);
			double deliveryDur = 0;
			MiscInner.PortInfo portInfo = cache.getPortInfo(vehSetup.m_ownerOrgId, conn);
			 ArrayList<Double> templ = portInfo.getDoubleParams(OrgConst.ID_DOUBLE_DIST_FROM_STRAIGHT_LINE);
			 if (templ != null && templ.size() > 0) 
				travelDistFactor = templ.get(0);
			 templ = portInfo.getDoubleParams(OrgConst.ID_DOUBLE_DIST_PER_DAY);
			 if (templ != null && templ.size() > 0)
				kmPerDay = templ.get(0);
			 templ = portInfo.getDoubleParams(OrgConst.ID_MAX_TIME_PER_DELIVERY);
			 if (templ != null && templ.size() > 0)
				 maxDaysPerDelivery = templ.get(0);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		
		 return new Triple<Double, Double, Double>(travelDistFactor, kmPerDay, maxDaysPerDelivery);
	}
	
	public static Pair<Double, Double> getAuthDistMinForSrcToDes(Connection conn, IdInfo src, IdInfo dest, int vehicleId, double travelDistFactor, double kmPerDay) throws Exception {
		double authDist = Misc.getUndefDouble();
		double authTimeMinute = Misc.getUndefDouble();
		if (src != null && !Misc.isUndef(src.getLongitude()) && dest != null && !Misc.isUndef(dest.getLongitude())) {
			if (src.getDestIdType() == 3 && dest.getDestIdType() == 3) {
				Pair<Double, Double> pr = getAuthDistMinForOpToOp(conn, src.getDestId(), dest.getDestId());
				authDist = pr.first;
				authTimeMinute = pr.second;
			}
			if (Misc.isUndef(authDist)) {
				double d = com.ipssi.geometry.Point.fastGeoDistance(src.getLongitude(), src.getLatitude(), dest.getLongitude(), dest.getLatitude());
				d *= travelDistFactor;
				authDist = d;
			}
			if (Misc.isUndef(authTimeMinute)) {
				authTimeMinute = authDist/kmPerDay*(24*60);
			}
		}
		return new Pair<Double, Double>(authDist, authTimeMinute);		
	}
	
	public static Pair<Double, Double> getAuthDistMinForOpToOp(Connection conn, int fromOp, int toOp) throws Exception {
		PreparedStatement ps = conn.prepareStatement("select load_lead_dist, load_lead_minute from eta_setup_op_to_op where lopid=? and uopid=?");
		ps.setInt(1, fromOp);
		ps.setInt(2, toOp);
		double dist = Misc.getUndefDouble();
		double leadMinutes = Misc.getUndefDouble();
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			dist = Misc.getRsetDouble(rs, 1);
			leadMinutes = Misc.getRsetDouble(rs, 2);
		}
		rs.close();
		ps.close();
		return new Pair<Double, Double>(dist, leadMinutes);
	}
}

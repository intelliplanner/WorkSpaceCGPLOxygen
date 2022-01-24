package com.ipssi.tracker.common.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import com.csvreader.CsvReader;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.trip.challan.ChallanUtils;

public class RFIDHelper {
	
	public static void insertUpdateRFTrip(PreparedStatement ps,	PreparedStatement psRFTrip,	PreparedStatement psSelectRFTrip,
	PreparedStatement psSelectTrip,	PreparedStatement psUpdateRFTripLoad,PreparedStatement psUpdateRFTripLoadTare,
	PreparedStatement psUpdateRFTripLoadGross,	PreparedStatement psUpdateRFTripUnLoad,	PreparedStatement psUpdateRFTripUnLoadTare,
	PreparedStatement psUpdateRFTripUnLoadGross,ResultSet rsSelectRFTrip,ResultSet rsSelectTrip,CsvReader c, Connection _dbConnection,
	String systemCode,int systemType) throws Exception{
		Date weighmentDate = null;
		String dateStr = c.get(0);
		String vehicleName = c.get(1);
		double grossWt = Misc.getParamAsDouble(c.get(2));
		double tareWt = Misc.getParamAsDouble(c.get(3));
		long dt = ChallanUtils.getLongDateForYYYYMMDDHHmmss(dateStr); 
		int vehId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, _dbConnection);
		ps.setString(1, systemCode);
		ps.setString(2, vehicleName);
		ps.setString(3, dateStr);
		if(systemType == 1){
			Misc.setParamDouble(ps, grossWt, 4);
			Misc.setParamDouble(ps, tareWt, 5);
			Misc.setParamDouble(ps, 0.0, 6);
			Misc.setParamDouble(ps, 0.0, 7);
		}else{
			Misc.setParamDouble(ps, 0.0, 4);
			Misc.setParamDouble(ps, 0.0, 5);
			Misc.setParamDouble(ps, grossWt, 6);
			Misc.setParamDouble(ps, tareWt, 7);
		}
		Misc.setParamInt(ps, vehId, 8);
		ps.setTimestamp(9, Misc.utilToSqlDate(dt));
		ps.execute();
		
		// trip identification and updation
		boolean isTareProper = false;
		boolean isGrossProper = false;
		if( tareWt > 8000 && tareWt < 18000){
			isTareProper = true;
		}
		if( grossWt > 18000 && grossWt < 45000){
			isGrossProper = true;
		}
		
		Misc.setParamInt(psSelectRFTrip, vehId, 1);
		psSelectRFTrip.setTimestamp(2, Misc.utilToSqlDate(dt));
		rsSelectRFTrip = psSelectRFTrip.executeQuery();
		int tripId = Misc.getUndefInt();
		if(rsSelectRFTrip.next()){
			tripId = rsSelectRFTrip.getInt(1);
		}
		rsSelectRFTrip.close();
		if(!Misc.isUndef(tripId)){
			if(isTareProper && isGrossProper){
				if(systemType == 1){
					Misc.setParamDouble(psUpdateRFTripLoad, grossWt, 1);
					Misc.setParamDouble(psUpdateRFTripLoad, tareWt, 2);
					Misc.setParamInt(psUpdateRFTripLoad, tripId, 3);
					psUpdateRFTripLoad.execute();
				}else{
					Misc.setParamDouble(psUpdateRFTripUnLoad, grossWt, 1);
					Misc.setParamDouble(psUpdateRFTripUnLoad, tareWt, 2);
					Misc.setParamInt(psUpdateRFTripUnLoad, tripId, 3);
					psUpdateRFTripUnLoad.execute();
				}
			}else if(isTareProper){
				if(systemType == 1){
					Misc.setParamDouble(psUpdateRFTripLoadTare, tareWt, 1);
					Misc.setParamInt(psUpdateRFTripLoadTare, tripId, 2);
					psUpdateRFTripLoadTare.execute();
				}else{
					Misc.setParamDouble(psUpdateRFTripUnLoadTare, tareWt, 1);
					Misc.setParamInt(psUpdateRFTripUnLoadTare, tripId, 2);
					psUpdateRFTripUnLoadTare.execute();
				}
			}else if(isGrossProper){
				if(systemType == 1){
					Misc.setParamDouble(psUpdateRFTripLoadGross, grossWt, 1);
					Misc.setParamInt(psUpdateRFTripLoadGross, tripId, 2);
					psUpdateRFTripLoadGross.execute();
				}else{
					Misc.setParamDouble(psUpdateRFTripUnLoadGross, grossWt, 1);
					Misc.setParamInt(psUpdateRFTripUnLoadGross, tripId, 2);
					psUpdateRFTripUnLoadGross.execute();
				}
			}
		}
		
		if(Misc.isUndef(tripId)){
			
			Misc.setParamInt(psSelectTrip, vehId, 1);
			psSelectTrip.setTimestamp(2, Misc.utilToSqlDate(dt));
			rsSelectTrip = psSelectTrip.executeQuery();
			tripId = Misc.getUndefInt();
			Timestamp comboStartTs = null;
			Timestamp comboEndTs = null;
			if(rsSelectTrip.next()){
				tripId = rsSelectTrip.getInt(1);
				comboStartTs = rsSelectTrip.getTimestamp(2);
				comboEndTs = rsSelectTrip.getTimestamp(3);
			}
			rsSelectTrip.close();
			
			Misc.setParamInt(psRFTrip, vehId, 1);
			psRFTrip.setString(2, systemCode);
			psRFTrip.setString(3, vehicleName);
			psRFTrip.setString(4, dateStr);
			psRFTrip.setTimestamp(5, Misc.utilToSqlDate(dt));
			if(systemType == 1){
				Misc.setParamDouble(psRFTrip, isGrossProper ? grossWt : 0.0, 6);
				Misc.setParamDouble(psRFTrip, isTareProper ? tareWt : 0.0, 7);
				Misc.setParamDouble(psRFTrip, 0.0, 8);
				Misc.setParamDouble(psRFTrip, 0.0, 9);
			}else{
				Misc.setParamDouble(psRFTrip, 0.0, 6);
				Misc.setParamDouble(psRFTrip, 0.0, 7);
				Misc.setParamDouble(psRFTrip, isGrossProper ? grossWt : 0.0, 8);
				Misc.setParamDouble(psRFTrip, isTareProper ? tareWt : 0.0, 9);
			}
			Misc.setParamInt(psRFTrip, tripId, 10);
			psRFTrip.setTimestamp(11, comboStartTs);
			psRFTrip.setTimestamp(12, comboEndTs);
			psRFTrip.execute();
		}
		System.out.println("RFIDHelper.insertUpdateRFTrip() vehicle_id:"+vehId+" vehicleName:"+vehicleName+" dateStr:"+dateStr);
	}
	
}

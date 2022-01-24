package com.ipssi.processor.utils;

import java.sql.ResultSet;

import com.ipssi.gen.utils.Misc;
import com.ipssi.geometry.Point;
import com.ipssi.map.utils.ApplicationConstants;

public class GpsDataResultSetReader {
	ResultSet rs = null;
//	private static int g_considerBothAsCurrIfGapSecondExceeds = 3600; //TODO make it property driven ... see also DataCache.java
//    private static int g_considerBothAsBothIfTheseManyIgnoreableSeen = 3;
//    private Date lastDCTime = null;
//    private int ignoreableCurrSeen = 0;
    private int lastVehId = Misc.getUndefInt();
    private boolean getGpsId = false;
    public GpsDataResultSetReader(ResultSet rs) {
        this.rs = rs;
    }
    public GpsDataResultSetReader(ResultSet rs, boolean getGpsId) {
    	this.rs = rs;
    	this.getGpsId = getGpsId;
    }
    
    private GpsData readCore() throws Exception {
    	double longitude = Misc.getRsetDouble(rs, "longitude");
    	double latitude = Misc.getRsetDouble(rs, "latitude");
    	int dimId = Misc.getRsetInt(rs, "attribute_id", ApplicationConstants.DIST_DIM);
    	double dimVal = Misc.getRsetDouble(rs, "attribute_value");
    	if (dimId == 0 && dimVal < 0)
    		dimVal = 0;
    	long gpsTime = Misc.sqlToLong(rs.getTimestamp("gps_record_time"));
    	long gpsRecv = Misc.sqlToLong(rs.getTimestamp("updated_on"));
    	String name = rs.getString("name");
    	int channel = Misc.getRsetInt(rs, "source", 1);
    	double speed = rs.getDouble("speed");
    	
    	ChannelTypeEnum chEnum = ChannelTypeEnum.getChannelType(channel);
    	GpsData gpsData = new GpsData();
    	gpsData.setLongitude(longitude);
    	gpsData.setLatitude(latitude);
    	gpsData.setGps_Record_Time(gpsTime);
        gpsData.setSourceChannel(chEnum);
        gpsData.setDimensionInfo(dimId, dimVal);
        gpsData.updateWithNameHack(name);
        gpsData.setGpsRecvTime(gpsRecv);
        gpsData.setSpeed(speed);
        if (getGpsId)
        	gpsData.setGpsRecordingId(Misc.getRsetInt(rs, "gps_id"));
        return gpsData;
    }
    
    public  Vehicle readGpsData() throws Exception {//will do a next()!! .... duplication with readGpsDataExt ... that gets Name and retains sourceChannel
		try {
			Vehicle retval = null;
		    if (rs.next()) {
		    	int vehicleId = rs.getInt("vehicle_id");
		    	if (vehicleId != lastVehId) {
//		    		lastDCTime = null;
//		    		ignoreableCurrSeen = 0;
		    		lastVehId = vehicleId;
		    	}
		    	GpsData gpsData = readCore();
		        if (gpsData.getSourceChannel() == ChannelTypeEnum.BOTH) {//HACK sanity check ... we are missing something in EIL and got a both channel for a much earlier time. it is quite possible that updated_on reflects 1st creation and second one really happened at the right time
		        	gpsData.setSourceChannel(ChannelTypeEnum.CURRENT);
				}
		        retval = new Vehicle(vehicleId);
		        retval.setGpsData(gpsData);
		    }
		    return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
    
    public  VehicleWithName readGpsDataPlain() throws Exception {//will do a next()!! .... duplication with readGpsDataExt ... that gets Name and retains sourceChannel
		try {
			VehicleWithName retval = null;
		    if (rs.next()) {
		    	int vehicleId = rs.getInt("vehicle_id");
		    	if (vehicleId != lastVehId) {
//		    		lastDCTime = null;
//		    		ignoreableCurrSeen = 0;
		    		lastVehId = vehicleId;
		    	}
		    	GpsData gpsData = readCore();
		        retval = new VehicleWithName(vehicleId);
		        retval.setGpsData(gpsData);
		        retval.setName(rs.getString("name"));
		    }
		    return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}

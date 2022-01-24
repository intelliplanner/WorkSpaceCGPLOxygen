package com.ipssi.tripprocessor.dashboard.bean;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.tripcommon.LUInfoExtract;

public class TrackRegionInfoVO extends VOInterface{
	private static final long serialVersionUID = 1L;
	private int opStationId;
	private String opStationName;
	private int queueLength;
	private String operationalStatus;
	private Date unProcessedVehicleIn;
	private Date lastVehicleOut;
	private int lastOutProcessingTime;
	private int opStationStatus;
	private int tripId;
	private Date latestEventDateTime;
	private String latestEventDateTimeStr;
	private int vehicleType;
	private int fullQueueLength;
	
	private List<Pair<Integer, LUInfoExtract>> vehiclesAssociated = new ArrayList<Pair<Integer, LUInfoExtract>> ();
	
	public String getLatestEventDateTimeStr() {
		return latestEventDateTimeStr;
	}
	public void setLatestEventDateTimeStr(String latestEventDateTimeStr) {
		this.latestEventDateTimeStr = latestEventDateTimeStr;
	}
	public Date getLatestEventDateTime() {
		return latestEventDateTime;
	}
	public void setLatestEventDateTime(Date latestEventDateTime) {
		this.latestEventDateTime = latestEventDateTime;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public int getOpStationStatus() {
		return opStationStatus;
	}
	public void setOpStationStatus(int opStationStatus) {
		this.opStationStatus = opStationStatus;
	}
	public int getOpStationId() {
		return opStationId;
	}
	public void setOpStationId(int opStationId) {
		this.opStationId = opStationId;
	}
	public String getOpStationName() {
		return opStationName;
	}
	public void setOpStationName(String opStationName) {
		this.opStationName = opStationName;
	}
	public int getQueueLength() {
		return queueLength;
	}
	public int getQueueLengthExt(int groupDim, int groupVal, Connection conn) throws Exception {
		if (Misc.isUndef(groupVal) || groupVal == Misc.G_HACKANYVAL) {
			return queueLength;
		}
		if (groupDim != 9097 && groupDim != 9003)
			return queueLength;
		int retval = 0;
		if (vehiclesAssociated != null && vehiclesAssociated.size() != 0) {
			for (Pair<Integer, LUInfoExtract> entry :vehiclesAssociated) {
				try {
				CacheTrack.VehicleSetup vehInfo = CacheTrack.VehicleSetup.getSetup(entry.first, conn);
				if (vehInfo != null && vehInfo.m_type == groupVal)
					retval++;
				}
				catch (Exception e1) {
					retval++;
					//eat it;
				}
			}
		}
		return retval;
	}
	public void setQueueLength(int queueLength) {
		this.queueLength = queueLength;
	}
	public String getOperationalStatus() {
		return operationalStatus;
	}
	public void setOperationalStatus(String operationalStatus) {
		this.operationalStatus = operationalStatus;
	}
	public Date getUnProcessedVehicleIn() {
		return unProcessedVehicleIn;
	}
	public void setUnProcessedVehicleIn(Date unProcessedVehicleIn) {
		this.unProcessedVehicleIn = unProcessedVehicleIn;
	}
	public Date getLastVehicleOut() {
		return lastVehicleOut;
	}
	public void setLastVehicleOut(Date lastVehicleOut) {
		this.lastVehicleOut = lastVehicleOut;
	}
	public int getLastOutProcessingTime() {
		return lastOutProcessingTime;
	}
	public void setLastOutProcessingTime(int lastOutProcessingTime) {
		this.lastOutProcessingTime = lastOutProcessingTime;
	}
	public void clearVehiclesAssodicated() {
		if (vehiclesAssociated != null)
			vehiclesAssociated.clear();
	}
	public void addVehiclesAssociated(Pair<Integer, LUInfoExtract> pair) {
		
		if (vehiclesAssociated == null)
			vehiclesAssociated = new ArrayList<Pair<Integer, LUInfoExtract>>();
		vehiclesAssociated.add(pair);
	}
	
	public void setVehiclesAssociated(List<Pair<Integer, LUInfoExtract>> vehiclesAssociated) {
		if (vehiclesAssociated == null)
			this.vehiclesAssociated = null;
		else {
			if (this.vehiclesAssociated == null)
				this.vehiclesAssociated = new ArrayList<Pair<Integer, LUInfoExtract>>();
			else
				this.vehiclesAssociated.clear();
		
			for (Pair<Integer, LUInfoExtract> entry: vehiclesAssociated) {
				this.vehiclesAssociated.add(entry);
			}
		}
	}
	public List<Pair<Integer, LUInfoExtract>> getVehiclesAssociated() {
		return vehiclesAssociated;
	}
	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}
	public int getVehicleType() {
		return vehicleType;
	}
	public void setFullQueueLength(int fullQueueLength) {
		this.fullQueueLength = fullQueueLength;
	}
	public int getFullQueueLength() {
		return fullQueueLength;
	}
	
}

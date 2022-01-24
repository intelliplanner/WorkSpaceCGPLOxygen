package com.ipssi.rfid.beans;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Misc;

public class Token {
	Logger logger = Logger.getLogger(Token.class);
	public static final int RETURN_BY_SERVICE = 0;
	public static final int RETURN_BY_USER = 1;
	private int vehicleId;
	private String epcId;
	private int status;
	private int action;
	private long lastSeen = Misc.getUndefInt();
	private long lastProcessed = Misc.getUndefInt();
	private String vehicleName;
	private boolean readFromTag = true;

	public Token(int vehicleId, String epcId) {
		// throw new UnsupportedOperationException("Not supported yet."); //To change
		// body of generated methods, choose Tools | Templates.
		this.vehicleId = vehicleId;
		this.epcId = epcId;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public String getEpcId() {
		return epcId;
	}

	public int getStatus() {
		return status;
	}

	public int getAction() {
		return action;
	}

	public long getLastSeen() {
		return lastSeen;
	}

	public long getLastProcessed() {
		return lastProcessed;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public void setEpcId(String epcId) {
		this.epcId = epcId;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}

	public void setLastProcessed(long lastProcessed) {
		this.lastProcessed = lastProcessed;
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	public boolean isReadFromTag() {
		return readFromTag;
	}

	public void setReadFromTag(boolean readFromTag) {
		this.readFromTag = readFromTag;
	}
}

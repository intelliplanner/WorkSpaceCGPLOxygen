package com.ipssi.userNameUtils;
import com.ipssi.gen.utils.Misc;
public class IdInfo {
	private double longitude = Misc.getUndefDouble();
	private double latitude = Misc.getUndefDouble();
	private int id = Misc.getUndefInt();
	private int destId = Misc.getUndefInt();
	private byte destIdType; //1 => Landmark, 2 => Shapefile point, 3 => op_station
	private byte matchQuality; //1=>District HQ, 2=>City, 3=>locality, 4=>exact
	// Trip Alert related mail and phone
	private String alertMailId = null;
	private String alertPhone = null;
	
	public String toString() {
			return id+","+destId+","+destIdType+","+matchQuality+","+longitude+","+latitude;
	}
	public double getLongitude() {
		return longitude <= 0 ? Misc.getUndefDouble() : longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude <= 0 ? Misc.getUndefDouble() : latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getDestId() {
		return destId;
	}
	public void setDestId(int destId) {
		this.destId = destId;
	}
	public byte getDestIdType() {
		return destIdType;
	}
	public void setDestIdType(byte destIdType) {
		this.destIdType = destIdType;
	}
	public byte getMatchQuality() {
		return matchQuality;
	}
	public void setMatchQuality(byte matchQuality) {
		this.matchQuality = matchQuality;
	}
	public String getAlertMailId() {
		return alertMailId;
	}
	public void setAlertMailId(String alertMailId) {
		this.alertMailId = alertMailId;
	}
	public void addAlertMailId(String alertMailId) {
		if (alertMailId != null) {
			alertMailId = alertMailId.trim();
			if (alertMailId.length() != 0) {
					if (this.alertMailId == null || this.alertMailId.length() == 0)
						this.alertMailId = alertMailId;
					else
						this.alertMailId += ";"+alertMailId;
			}
		}
	}
	public String getAlertPhone() {
		return alertPhone;
	}
	public void setAlertPhone(String alertPhone) {
		this.alertPhone = alertPhone;
	}
	public void addAlertPhone(String alertPhone) {
		if (alertPhone != null) {
			alertPhone = alertPhone.trim();
			if (alertPhone.length() != 0) {
					if (this.alertPhone == null || this.alertPhone.length() == 0)
						this.alertPhone = alertPhone;
					else
						this.alertPhone += ";"+alertPhone;
			}
		}
	}

}

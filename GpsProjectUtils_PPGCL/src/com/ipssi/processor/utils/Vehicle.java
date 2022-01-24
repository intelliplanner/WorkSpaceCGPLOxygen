package com.ipssi.processor.utils;

import java.sql.ResultSet;

import com.ipssi.map.utils.ApplicationConstants;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.geometry.Point;

/**
 * This class structure will be decided on the basis of gps data coming. This structure below is the place holder only.
 * 
 * @author Kapil
 * 
 */
public class Vehicle implements Cloneable {

	private int id;
	private GpsData gpsData;
	
    public Vehicle(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the gpsData
	 */
	public GpsData getGpsData() {
		// Retured new gpsData object so that each executor can maintain its own copy
		return gpsData;
	}

	/**
	 * @param gpsData
	 *            the gpsData to set
	 */
	public void setGpsData(GpsData gpsData) {
		this.gpsData = gpsData;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vehicle) {
			return ((Vehicle) obj).id == this.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "[Vehicle id = " + id +"]" + (gpsData == null ? "null" : gpsData.toString());
	}
	
	

}

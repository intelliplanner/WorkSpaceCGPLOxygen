package com.ipssi.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;

public class BLEInfo extends OtherData implements Comparable {
	public final static String GET_BLE_SEL = "select ble_read_event.id, ble_read_event.vehicle_id, ble_read_event.gps_record_time, ble_read_event.updated_on "+
	", ble_read_event.ble_tag, ble_read_event.is_present, ble_read_event.rssi, ble_read_event.is_present, ble_read_event.truck_vehicle_id "
		;
	private String tag;
	private int rssi;
	private boolean present;
	private int truckVehicleId = Misc.getUndefInt();
	
	public static BLEInfo read(Connection conn, ResultSet rs) throws Exception {
//		public final static String GET_BLE_SEL = "select ble_read_event.id, ble_read_event.vehicle_id, ble_read_event.gps_record_time, ble_read_event.updated_on "+
//		", ble_read_event.ble_tag, ble_read_event.is_present, ble_read_event.rssi, ble_read_event.is_present, ble_read_event.truck_vehicle_id "
//		public BLEInfo(int id, int vehicleId, long gpsRecordTime,
//				long updatedOnTime, String tag, int rssi, boolean present, int truckVehicleId) {

		BLEInfo retval = new BLEInfo(Misc.getRsetInt(rs, "id"), Misc.getRsetInt(rs, "vehicle_id"), Misc.sqlToLong(rs.getTimestamp("gps_record_time")),
				Misc.sqlToLong(rs.getTimestamp("updated_on")), rs.getString("ble_tag"),Misc.getRsetInt(rs, "rssi"), Misc.getRsetInt(rs, "is_present") == 1, Misc.getRsetInt(rs, "truck_vehicle_id")
				)
		;
		return retval;
	}
	public void saveToDB(Connection conn) throws Exception {
//		public final static String GET_BLE_SEL = "select ble_read_event.id, ble_read_event.vehicle_id, ble_read_event.gps_record_time, ble_read_event.updated_on "+
//		", ble_read_event.ble_tag, ble_read_event.is_present, ble_read_event.rssi, ble_read_event.is_present, ble_read_event.truck_vehicle_id "

		if (Misc.isUndef(truckVehicleId)) {
			truckVehicleId = CacheTrack.VehicleSetup.getSetupByBLE(tag, conn);
		}
		PreparedStatement ps = null;
		if (!Misc.isUndef(id)) {
			ps = conn.prepareStatement("update ble_read_event set vehicle_id=?, gps_record_time=?,updated_on=?, ble_tag=?,is_present=?,rssi=?,truck_vehicle_id=? where id=?");
		}
		else {
			ps = conn.prepareStatement("insert into ble_read_event(vehicle_id, gps_record_time, updated_on, ble_tag,is_present, rssi, truck_vehicle_id) values (?,?,?,?,?,?,?)");
		}
		int colPos = 1;
		Misc.setParamInt(ps, vehicleId, colPos++);
		ps.setTimestamp(colPos++, Misc.longToSqlDate(gpsRecordTime));
		ps.setTimestamp(colPos++, Misc.longToSqlDate(gpsRecvTime));
		ps.setString(colPos++, tag);
		Misc.setParamInt(ps, this.isPresent() ? 1 : 0, colPos++);
		Misc.setParamInt(ps, this.rssi, colPos++);
		Misc.setParamInt(ps, this.truckVehicleId, colPos++);
		if (!Misc.isUndef(id)) {
			ps.setInt(colPos++, this.id);
			ps.executeUpdate();
		}
		else {
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			this.id = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
		}
		ps = Misc.closePS(ps);
	}

	public int compareTo(Object obj) {		
		BLEInfo p = (BLEInfo)obj;
		int retval = 0;
		retval = this.gpsRecordTime < p.gpsRecordTime ? -1 : this.gpsRecordTime == p.gpsRecordTime ? 0 : 1;
		if (retval == 0) {
			retval = this.truckVehicleId < p.truckVehicleId ? -1 : this.truckVehicleId == p.truckVehicleId ? 0 : 1;
		}
		if (retval == 0) {
			retval = this.tag != null  && p.tag == null ? 0 : this.tag != null && p.tag == null ? 1 : this.tag == null && p.tag != null ? -1 :  tag.compareTo(p.tag) ;
		}
		return retval;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdfTime = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM+":ss");
		sb.append("[")
		.append("id:").append(id)
		.append(",id:").append(vehicleId)
		.append(",rt:").append(this.gpsRecordTime <= 0 ? "null" : sdfTime.format(new java.util.Date(gpsRecordTime)))
		.append(",upd:").append(this.gpsRecvTime <= 0 ? "null" : sdfTime.format(new java.util.Date(gpsRecvTime)))
		.append(",T:").append(tag)
		.append(",P:").append(present ? 1 : 0)
		.append(",Truck:").append(truckVehicleId)
		.append("]");
		return sb.toString();
	}


	public BLEInfo(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public boolean isPresent() {
		return present;
	}
	public void setPresent(boolean isPresent) {
		this.present = present;
	}
	public int getTruckVehicleId() {
		return truckVehicleId;
	}
	public void setTruckVehicleId(int truckVehicleId) {
		this.truckVehicleId = truckVehicleId;
	}
	public BLEInfo(int id, int vehicleId, long gpsRecordTime,
			long updatedOnTime, String tag, int rssi, boolean present, int truckVehicleId) {
		super();
		this.id = id;
		this.vehicleId = vehicleId;
		this.gpsRecordTime = gpsRecordTime;
		this.gpsRecvTime = updatedOnTime;
		this.tag = tag;
		this.rssi = rssi;
		this.present = present;
		this.truckVehicleId = truckVehicleId;
	}

}

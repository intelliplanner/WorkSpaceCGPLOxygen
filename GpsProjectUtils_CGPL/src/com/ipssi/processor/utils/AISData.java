package com.ipssi.processor.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.cache.OtherData;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class AISData extends OtherData{
	public final static int PACKET_LOGIN = 1;
	public final static int PACKET_NORMAL = 2;
	public final static int PACKET_EMERGENCY = 3;
	public final static int PACKET_HEALTH = 4;
	public final static int PACKET_UNKNOWN = 0;
	public  static boolean HAS_SOMETHING_BEFORE_LIVE = true;
	public  static boolean HAS_EMERGENCY = true;
	public  static boolean HAS_GEO = true;
	public  static boolean HAS_DATE_TIME_SPLIT = true;
	public static boolean HAS_ODOMETER = true;
	public static int HAS_NUM_ANALOG = 2;
    private String vehicleNumber = null;
    private String imei = null;
    private String firmwareVersion = null;
    private String protocolVersion = null;
    private String vendorId = null;
    private int packetType = PACKET_UNKNOWN;
    private String subPacketType = null;
    private boolean packetStatus = false;
    private boolean gpsValid = false;
    private double lat = Misc.getUndefDouble();
    private double lon = Misc.getUndefDouble();
    private double speed = Misc.getUndefDouble();
    private double heading = Misc.getUndefDouble();
    private int numSat = 0;
    private double altitude = Misc.getUndefDouble();
    private double pdop = Misc.getUndefDouble();
    private double hdop = Misc.getUndefDouble();
    private String nwOperatorName = null;
    private boolean ignStatus = false;
    private boolean mainPowerStatus = false;
    private double mainVoltage = Misc.getUndefDouble();
    private double internalVoltage = Misc.getUndefDouble();
    private boolean emergencyStatus = false;
    private String geoInOut = null;
    private String geoInOutName = null;
    private int gsmStrength = Misc.getUndefInt();
    private String mccCountryCode = null;
    private String mncConde = null;
    private String lacCode = null;
    private String cellId = null;
    private String neighbourLacCode1 = null;
    private String neighbourCellId1 = null;
    private int neighbourStrength1  = Misc.getUndefInt();
    private String neighbourLacCode2 = null;
    private String neighbourCellId2 = null;
    private int neighbourStrength2  = Misc.getUndefInt();
    private String neighbourLacCode3 = null;
    private String neighbourCellId3 = null;
    private int neighbourStrength3  = Misc.getUndefInt();
    private String neighbourLacCode4 = null;
    private String neighbourCellId4 = null;
    private int neighbourStrength4 = Misc.getUndefInt();
    private boolean digitalInputStatus[] = new boolean[6];
    private boolean digitalOutputStatus[] = new boolean[6];
    private double analogInput[] = new double[6];
    private int frameNumber = Misc.getUndefInt();;
    private String otaParamChanged = null;
    private String otaParamChangeSource = null;
    private String checkSumSent = null;
    private int  internalBatteryPerc = Misc.getUndefInt();
    private int lowBatteryThresh = Misc.getUndefInt();
    private int memoryPerc = Misc.getUndefInt();
    private int ignOnDataRate = Misc.getUndefInt();
    private int ignOffDataRate = Misc.getUndefInt();
    private double odoMeter = Misc.getUndefDouble();
    private int loctionSource =  Misc.getUndefInt();
    private String mobileNumber = null;
    private String messageString = null;
    private String field1 = null;
    private String field2 = null;
    private String field3 = null;
    public static boolean g_newVehByDevice = true;
    public static int getCreateVehicleId(Connection conn, String vehicleName, String mobileNumber, String imei) throws Exception {
    	int vehicleId = Misc.getUndefInt();
    	
    	if (g_newVehByDevice) {
    		if (imei != null)
    			vehicleId = CacheTrack.VehicleSetup.getSetup(imei, conn);
    	}
    	else {
    		if (vehicleName != null)
    			vehicleId = CacheTrack.VehicleSetup.getSetupByStdName(vehicleName, conn);
    		else if (imei != null)
    			vehicleId = CacheTrack.VehicleSetup.getSetup(imei, conn);
    	}
    	
    	if (Misc.isUndef(vehicleId) && imei != null && vehicleName != null) {
    		Misc.loadCFGConfigServerProp();
    		boolean todoOtherConn = Misc.getSERVER_MODE() == 1;
    		if (todoOtherConn) {
    			boolean succ = true;
    			Connection otherConn = null;
    			try {
    				otherConn = Misc.getBackupFreshConn();
    				if (!otherConn.getAutoCommit())
    					otherConn.setAutoCommit(false);
    				vehicleId = helperInsertVehicle(otherConn, vehicleName, mobileNumber, imei, vehicleId);
    				if (!otherConn.getAutoCommit())
    					otherConn.commit();
    			}
    			catch (Exception e) {
    				e.printStackTrace();
    				succ = false;
    				throw e;
    			}
    			finally {
    				if (otherConn != null) {
    					try {
    						if (!otherConn.getAutoCommit()) {
    							if (!succ)
    								otherConn.rollback();
    						}
    					}
    					catch (Exception e2) {
    						//eait
    					}
    					try {
    						otherConn.close();
    					}
    					catch (Exception e3) {
    						//eat it
    					}
    				}//if otherConn != null
    			}//finally block
    		}//if todoOtherConn
    		vehicleId = helperInsertVehicle(conn, vehicleName, mobileNumber, imei, vehicleId);
    		if (!conn.getAutoCommit())
    			conn.commit();
    		ArrayList<Integer> vidlist = new ArrayList<Integer>();
    		vidlist.add(vehicleId);
    		CacheTrack.VehicleSetup.loadSetup(conn, vidlist,false);
    	}
    	return vehicleId;
    }
    
    public void save(Connection conn) throws Exception {
    	this.vehicleId = getCreateVehicleId(conn, this.getVehicleNumber(), this.getMobileNumber(), this.getImei());
    	if (this.vehicleId <= 0)
    		return;
    	if (this.getPacketType() == PACKET_NORMAL)
    		this.saveNormal(conn);
    	else if (this.getPacketType() == PACKET_LOGIN)
    		this.saveLogin(conn);
    	else if (this.getPacketType() == PACKET_EMERGENCY)
    		this.saveEmergency(conn);
    	else if (this.getPacketType() == PACKET_HEALTH)
    		this.saveHealth(conn);
    	if (!conn.getAutoCommit())
    		conn.commit();
    }
    public static int getPacketType(String str) {
    	return getPacketType(str,0);
    }
	public static int getPacketType(String str, int idx) {
		if (str == null)
			return PACKET_UNKNOWN;
		
		if (str.startsWith("$LGN,",idx))
			return PACKET_LOGIN;
		else if (str.startsWith("$EPB,EMR",idx)) 
			return PACKET_EMERGENCY;
		else if (str.startsWith("$EPB,",idx))
			return PACKET_NORMAL;
		else if (str.startsWith("$HLT",idx))
			return PACKET_HEALTH;
		else
			return PACKET_UNKNOWN;
	}
	
	public static int getPacketStart(String str) {
		int idx = str == null ? -1 : str.indexOf("$");
		return idx;
	}
	public static int getPacketEndExcl(String str, int packetStart) {
		int idx = str == null ? -1 : str.indexOf("*");
		if (idx >= 0) {
			int packetType = getPacketType(str, packetStart);
			return (packetType == PACKET_EMERGENCY) ? (str.length() > (idx+8) ? (idx+8+1) : -1) : idx+1;
		}
		return idx;
	}
	

	public static AISData get(String str) {
		AISData retval = new AISData();
		retval.parse(str);
		return retval;
	}
	private static String getCsv(String csv[], int index) {
		return index >= 0 && index < csv.length ? csv[index] : null;
	}
	public void parse(String str) {
		int packetType = AISData.getPacketType(str);
		String csv[] = str.split(",");
		this.packetType = packetType;
		this.messageString = str;
		String last = csv[csv.length-1];
		int posStar = last.lastIndexOf('*');
		if (posStar >= 0) {
			if (posStar != last.length()-1) {
				//thing following * is checksum
				this.checkSumSent = last.substring(posStar+1, last.length());
			}
			last = last.substring(0,posStar);
			csv[csv.length-1] = last;
		}
		long updatedOn = System.currentTimeMillis();
		this.setGpsRecvTime(updatedOn);
		this.gpsRecordTime = updatedOn;
		switch (packetType) {
			case PACKET_LOGIN: {
					int index = 1;
					this.vehicleNumber = getCsv(csv,index++);
					this.imei = getCsv(csv,index++);
					this.firmwareVersion = getCsv(csv,index++);
					this.protocolVersion = getCsv(csv,index++);
					this.lat = Misc.getParamAsDouble(getCsv(csv,index))*("S".equals(getCsv(csv,index+1)) ? -1 : 1);
					index += 2;
					this.lon = Misc.getParamAsDouble(getCsv(csv,index))*("W".equals(getCsv(csv,index+1)) ? -1 : 1);
					index += 2;
				break;
			}
			case PACKET_NORMAL: {
				int index = 1;
				this.vendorId = getCsv(csv,index++);
				this.firmwareVersion = getCsv(csv,index++);
				this.subPacketType = getCsv(csv,index++);
				if (HAS_SOMETHING_BEFORE_LIVE) {
					String s1 = getCsv(csv,index++);
				}
				
				this.packetStatus = "L".equals(getCsv(csv,index++));
				this.imei = getCsv(csv,index++);
				this.vehicleNumber = getCsv(csv,index++);
				this.gpsValid = "1".equals(getCsv(csv,index++));
				String dtStr = getCsv(csv,index++);
				if (HAS_DATE_TIME_SPLIT) {//date now ddmmyy,hhmmss
					String timStr = getCsv(csv,index++);
					dtStr = dtStr.substring(0,4)+"20"+dtStr.substring(4,6)+timStr;
				}
				int date = Misc.getParamAsInt(dtStr.substring(0,2));
				if (date <= 0)
					date = 1;
				int mon = Misc.getParamAsInt(dtStr.substring(2,4));
				if (mon <= 1)
					mon = 1;
				else if (mon >= 12)
					mon = 12;
				mon--;
				int year = Misc.getParamAsInt(dtStr.substring(4,8));
				if (year >= 1900)
					year -= 1900;
				else
					year = 1900;
				int hr = Misc.getParamAsInt(dtStr.substring(8,10));
				if (hr <= 0)
					hr = 0;
				else if (hr >= 23)
					hr = 23;
				int min = Misc.getParamAsInt(dtStr.substring(10,12));
				if (min <= 0)
					min = 0;
				else if (min >= 59)
					min = 59;
				int sec = Misc.getParamAsInt(dtStr.substring(12,14));
				if (sec <= 0)
					sec = 0;
				else if (sec >= 59)
					sec = 59;
				java.util.Date dt = new java.util.Date(year,mon,date,hr,min,sec);
				Misc.addSeconds(dt,330*60);
				this.gpsRecordTime = dt.getTime();
				this.lat = Misc.getParamAsDouble(getCsv(csv,index))*("S".equals(getCsv(csv,index+1)) ? -1 : 1);
				index += 2;
				this.lon = Misc.getParamAsDouble(getCsv(csv,index))*("W".equals(getCsv(csv,index+1)) ? -1 : 1);
				index += 2;
				this.speed = Misc.getParamAsDouble(getCsv(csv,index++));
				this.heading = Misc.getParamAsDouble(getCsv(csv,index++));
				this.numSat = Misc.getParamAsInt(getCsv(csv,index++));
				this.altitude = Misc.getParamAsDouble(getCsv(csv,index++));
				this.pdop = Misc.getParamAsDouble(getCsv(csv,index++));
				this.hdop = Misc.getParamAsDouble(getCsv(csv,index++));
				if (HAS_ODOMETER)
					this.odoMeter = Misc.getParamAsDouble(getCsv(csv,index++));
				this.nwOperatorName = getCsv(csv,index++);
				this.ignStatus = "1".equals(getCsv(csv,index++));
				this.mainPowerStatus = "1".equals(getCsv(csv,index++));
				this.mainVoltage = Misc.getParamAsDouble(getCsv(csv,index++));
				this.internalVoltage = Misc.getParamAsDouble(getCsv(csv,index++));
				if (HAS_EMERGENCY) { 
					this.emergencyStatus = "1".equals(getCsv(csv,index++));
				}
				if (HAS_GEO) {
					String inout = getCsv(csv,index++);
					String geoname = getCsv(csv,index++);
					this.geoInOut = inout;
					this.geoInOutName = geoname;
				}
				this.gsmStrength = Misc.getParamAsInt(getCsv(csv,index++));
				this.mccCountryCode = getCsv(csv,index++);
				this.mncConde = getCsv(csv,index++);
				this.lacCode = getCsv(csv,index++);
				this.cellId = getCsv(csv,index++);
				this.neighbourLacCode1 = getCsv(csv,index++);
				this.neighbourCellId1 = getCsv(csv,index++);
				this.neighbourStrength1 = Misc.getParamAsInt(getCsv(csv,index++));
				this.neighbourLacCode2 = getCsv(csv,index++);
				this.neighbourCellId2 = getCsv(csv,index++);
				this.neighbourStrength2 = Misc.getParamAsInt(getCsv(csv,index++));
				this.neighbourLacCode3 = getCsv(csv,index++);
				this.neighbourCellId3 = getCsv(csv,index++);
				this.neighbourStrength3 = Misc.getParamAsInt(getCsv(csv,index++));
				this.neighbourLacCode4 = getCsv(csv,index++);
				this.neighbourCellId4 = getCsv(csv,index++);
				this.neighbourStrength4 = Misc.getParamAsInt(getCsv(csv,index++));
				String din = getCsv(csv,index++);
				for (int i=0,is = Math.min(din.length(), this.digitalInputStatus.length);i<is;i++)
					this.digitalInputStatus[i] = "1".equals(din.charAt(i));
				din = getCsv(csv,index++);
				for (int i=0,is = Math.min(din.length(), this.digitalOutputStatus.length);i<is;i++)
					this.digitalOutputStatus[i] = "1".equals(din.charAt(i));
				this.frameNumber = Misc.getParamAsInt(getCsv(csv,index++));
				if ("OT".equals(this.subPacketType) && index < (csv.length-3)) {
					this.otaParamChanged = getCsv(csv,index++);
					this.otaParamChangeSource = getCsv(csv,index++);
				}
				for (int i=0,is=HAS_NUM_ANALOG,csvnmin1 = csv.length-1;i <is && index < csvnmin1;i++) {
					this.setAnalogInput(i, Misc.getParamAsDouble(getCsv(csv,index++)));
				}
				this.checkSumSent = csv[csv.length-1];
				break;
			}
			case PACKET_EMERGENCY: {
				int index = 2;
				this.imei = getCsv(csv,index++);
				this.packetStatus = "NM".equals(getCsv(csv,index++));
				String dtStr = getCsv(csv,index++);
				if (HAS_DATE_TIME_SPLIT) {//date now ddmmyy,hhmmss
					String timStr =getCsv(csv,index++);
					dtStr = dtStr.substring(0,4)+"20"+dtStr.substring(4,6)+timStr;
				}
				int date = Misc.getParamAsInt(dtStr.substring(0,2));
				if (date <= 0)
					date = 1;
				int mon = Misc.getParamAsInt(dtStr.substring(2,4));
				if (mon <= 1)
					mon = 1;
				else if (mon >= 12)
					mon = 12;
				mon--;
				int year = Misc.getParamAsInt(dtStr.substring(4,8));
				if (year >= 1900)
					year -= 1900;
				else
					year = 1900;
				int hr = Misc.getParamAsInt(dtStr.substring(8,10));
				if (hr <= 0)
					hr = 0;
				else if (hr >= 23)
					hr = 23;
				int min = Misc.getParamAsInt(dtStr.substring(10,12));
				if (min <= 0)
					min = 0;
				else if (min >= 59)
					min = 59;
				int sec = Misc.getParamAsInt(dtStr.substring(12,14));
				if (sec <= 0)
					sec = 0;
				else if (sec >= 59)
					sec = 59;
				java.util.Date dt = new java.util.Date(year,mon,date,hr,min,sec);
				Misc.addSeconds(dt,330*60);
				this.gpsRecordTime = dt.getTime();
				
				this.gpsValid = "1".equals(getCsv(csv,index++));
				
				
				this.lat = Misc.getParamAsDouble(getCsv(csv,index))*("S".equals(getCsv(csv,index+1)) ? -1 : 1);
				index += 2;
				this.lon = Misc.getParamAsDouble(getCsv(csv,index))*("W".equals(getCsv(csv,index+1)) ? -1 : 1);
				index += 2;
				
				this.altitude = Misc.getParamAsDouble(getCsv(csv,index++));
				this.speed = Misc.getParamAsDouble(getCsv(csv,index++));
				this.odoMeter = Misc.getParamAsDouble(getCsv(csv,index++));
				this.loctionSource = "G".equals(getCsv(csv,index++)) ? 1 : 0;
				
				this.vehicleNumber = getCsv(csv,index++);
				this.vehicleNumber=CacheTrack.standardizeName(this.vehicleNumber);
				//get first non-zero .. because left padded with 0
				int pos = 0;
				for(int is=this.vehicleNumber.length();pos<is && this.vehicleNumber.charAt(pos) == '0';pos++) {
					
				}
				if (pos != 0)
					this.vehicleNumber = this.vehicleNumber.substring(pos, this.vehicleNumber.length());
				break;
			}
			case PACKET_HEALTH: {
				int index = 1;
				this.vendorId = getCsv(csv,index++);
				this.firmwareVersion = getCsv(csv,index++);
				this.imei = getCsv(csv,index++);
				this.internalBatteryPerc = Misc.getParamAsInt(getCsv(csv,index++));
				this.lowBatteryThresh = Misc.getParamAsInt(getCsv(csv,index++));
				this.memoryPerc = Misc.getParamAsInt(getCsv(csv,index++));
				this.ignOnDataRate = Misc.getParamAsInt(getCsv(csv,index++));
				
				String din = getCsv(csv,index++);
				for (int i=0,is = Math.min(din.length(), this.digitalInputStatus.length);i<is;i++)
					this.digitalInputStatus[i] = "1".equals(din.charAt(i));
				din = getCsv(csv,index++);
				for (int i=0,is = Math.min(din.length(), this.digitalOutputStatus.length);i<is;i++)
					this.digitalOutputStatus[i] = "1".equals(din.charAt(i));
				for (int i=0,is = Math.min(this.analogInput.length, csv.length-index);i<is;i++)
					this.analogInput[i] = Misc.getParamAsDouble(getCsv(csv,index++));
				break;
			}
			default:
				break;
		}
		if (this.vehicleNumber != null)
			this.vehicleNumber = CacheTrack.standardizeName(this.vehicleNumber);
	}
	
	private static String g_insNormal = "insert ignore into ais_logged_data( " +
			"vehicle_id, gps_record_time, vendor_code, firmware_version, subpacket_type " +
			", packet_status, imei, vehicle_number, gps_valid,longitude "+
			", latitude, speed, heading, odometer,numstat "+
			", altitude, pdop, hdop, nw_oper_name, ign_status "+
			", main_power_status, main_voltage, internal_voltage, emergency_status, gsm_strength "+
			", mcc, mnc, lac, cell_id, n1_lac "+
			", n1_cell_id, n1_strength, n2_lac, n2_cell_id, n2_strength "+
			", n3_lac, n3_cell_id, n3_strength, n4_lac, n4_cell_id "+
			", n4_strength, din0, din1, din2, din3 "+
			", din4, din5, dop0, dop1, dop2 "+
			", dop3, dop4, dop5, ain0, ain1 "+
			", ain2, ain3, ain4, ain5, frame_number "+
			", ota_param_changed, ota_param_source, check_sum, updated_on,message_string "+
			", geo_inout, geo_name, field1, field2, field3 "+
			") values ("+
			"?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			",?,?,?,?,?"+
			")";
	public static String trim(String str, int sz) {
		if (str != null) {
			str = str.trim();
			if (str.length() > sz)
				str = str.substring(sz);
		}
		return str;
	}
	private void saveNormal(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement(g_insNormal);
		int index = 1;
	//	"vehicle_id, gps_record_time, vendor_code, firmware_version, subpacket_type " +
		Misc.setParamInt(ps, this.getVehicleId(), index++);
		ps.setTimestamp(index++, Misc.longToSqlDate(this.getGpsRecordTime()));
		ps.setString(index++, trim(this.getVendorId(), 16));
		ps.setString(index++, trim(this.getFirmwareVersion(), 16));
		ps.setString(index++, trim(this.getSubPacketType(), 6));
		//", packet_status, imei, vehicle_number, gps_valid,longitude "+
		ps.setInt(index++, this.getPacketStatus() ? 1 : 0);
		ps.setString(index++, trim(this.getImei(), 16));
		ps.setString(index++, trim(this.getVehicleNumber(), 24));
		ps.setInt(index++, this.isGpsValid() ? 1 : 0);
		Misc.setParamDouble(ps, this.getLon(), index++);
		//", latitude, speed, heading, odometer,numstat "+
		Misc.setParamDouble(ps, this.getLat(), index++);
		Misc.setParamDouble(ps, this.getSpeed(), index++);
		Misc.setParamDouble(ps, this.getHeading(), index++);
		Misc.setParamDouble(ps, this.getOdoMeter(), index++);
		Misc.setParamInt(ps, (int) this.getNumSat(), index++);
		//", altitude, pdop, hdop, nw_oper_name, ign_status "+
		Misc.setParamDouble(ps, this.getAltitude(), index++);
		Misc.setParamDouble(ps, this.getPdop(), index++);
		Misc.setParamDouble(ps, this.getHdop(), index++);
		ps.setString(index++, trim(this.getNwOperatorName(), 24));
		ps.setInt(index++, this.isIgnStatus() ? 1 : 0);
		//", main_power_status, main_voltage, internal_voltage, emergency_status, gsm_strength "+
		ps.setInt(index++, this.isMainPowerStatus() ? 1 : 0);
		Misc.setParamDouble(ps, this.getMainVoltage(), index++);
		Misc.setParamDouble(ps, this.getInternalVoltage(), index++);
		ps.setInt(index++, this.isEmergencyStatus() ? 1 : 0);
		Misc.setParamInt(ps, (int) this.getGsmStrength(), index++);
		//", mcc, mno, lac, cell_id, n1_lac "+
		ps.setString(index++, trim(this.getMccCountryCode(), 12));
		ps.setString(index++, trim(this.getMncConde(), 12));
		ps.setString(index++, trim(this.getLacCode(), 12));
		ps.setString(index++, trim(this.getCellId(), 12));
		ps.setString(index++, trim(this.getNeighbourLacCode1(), 12));
		//", n1_cell_id, n1_strength, n2_lac, n2_cell_id, n2_strength "+
		ps.setString(index++, trim(this.getNeighbourCellId1(), 12));
		Misc.setParamInt(ps, (int) this.getNeighbourStrength1(), index++);
		ps.setString(index++, trim(this.getNeighbourLacCode2(), 12));
		ps.setString(index++, trim(this.getNeighbourCellId2(), 12));
		Misc.setParamInt(ps, (int) this.getNeighbourStrength2(), index++);
		//", n3_lac, n3_cell_id, n3_strength, n4_lac, n4_cell_id "+
		ps.setString(index++, trim(this.getNeighbourLacCode3(), 12));
		ps.setString(index++, trim(this.getNeighbourCellId3(), 12));
		Misc.setParamInt(ps, (int) this.getNeighbourStrength3(), index++);
		ps.setString(index++, trim(this.getNeighbourLacCode4(), 12));
		ps.setString(index++, trim(this.getNeighbourCellId4(), 12));
		//", n4_strength, din0, din1, din2, din3 "+
		Misc.setParamInt(ps, (int) this.getNeighbourStrength4(), index++);
		ps.setInt(index++, this.getDigitalInputStatus(0)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(1)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(2)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(3)?1:0);
		//", din4, din5, dop0, dop1, dop2 "+
		ps.setInt(index++, this.getDigitalInputStatus(4)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(5)?1:0);
		ps.setInt(index++, this.getDigitalOutputStatus(0)?1:0);
		ps.setInt(index++, this.getDigitalOutputStatus(1)?1:0);
		ps.setInt(index++, this.getDigitalOutputStatus(2)?1:0);
		//", dop3, dop4, dop5, ain0, ain1 "+
		ps.setInt(index++, this.getDigitalOutputStatus(3)?1:0);
		ps.setInt(index++, this.getDigitalOutputStatus(4)?1:0);
		ps.setInt(index++, this.getDigitalOutputStatus(5)?1:0);
		Misc.setParamDouble(ps, this.getAnalogInput(0), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(1), index++);
		////", ain2, ain3, ain4, ain5, frame_number "+
		Misc.setParamDouble(ps, this.getAnalogInput(2), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(3), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(4), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(5), index++);
		Misc.setParamInt(ps, this.getFrameNumber(), index++);
		//", ota_param_changed, ota_param_source, check_sum, updated_on "+
		ps.setString(index++, trim(this.getOtaParamChanged(), 12));
		ps.setString(index++, trim(this.getOtaParamChangeSource(), 24));
		ps.setString(index++, trim(this.getCheckSumSent(), 8));
		ps.setTimestamp(index++, Misc.longToSqlDate(this.gpsRecvTime));
		ps.setString(index++, trim(this.getMessageString(),500));
		ps.setString(index++, trim(this.geoInOut,4));
		ps.setString(index++, trim(this.geoInOutName,24));
		ps.setString(index++, trim(this.field1,24));
		ps.setString(index++, trim(this.field2,24));
		ps.setString(index++, trim(this.field3,24));
		ps.executeUpdate();
		ps = Misc.closePS(ps);
	}
	private static String g_insLogin = "insert ignore into ais_login_data( " +
	"vehicle_id, gps_record_time,  firmware_version, protocol_version , imei "+
	", vehicle_number, longitude, latitude,message_string"+
	") values ("+
	"?,?,?,?,?"+
	",?,?,?,?"+
	")";
	private void saveLogin(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement(g_insLogin);
		int index = 1;
		
		
	//	"vehicle_id, gps_record_time,  firmware_version, protocol_version , imei "+
		Misc.setParamInt(ps, this.getVehicleId(), index++);
		ps.setTimestamp(index++, Misc.longToSqlDate(this.getGpsRecordTime()));
		ps.setString(index++, trim(this.getFirmwareVersion(), 16));
		ps.setString(index++, trim(this.getProtocolVersion(), 16));
		ps.setString(index++, trim(this.getImei(), 16));
		//", vehicle_number, longitude, latitude"+
		ps.setString(index++, trim(this.getVehicleNumber(), 24));
		Misc.setParamDouble(ps, this.getLon(), index++);
		Misc.setParamDouble(ps, this.getLat(), index++);
		ps.setString(index++, this.getMessageString());
		ps.executeUpdate();
		ps = Misc.closePS(ps);
	}
	private static String g_insEmeregency = "insert ignore into ais_emergency_data( " +
	"vehicle_id, gps_record_time,  packet_status, imei, vehicle_number "+
	", gps_valid, longitude, latitude, speed, heading "+
	", odometer,updated_on, message_string "+
	") values ( "+
	"?,?,?,?,? "+
	",?,?,?,?,? "+
	",?,?,? "+
	")";
	private void saveEmergency(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement(g_insEmeregency);
		int index = 1;
	//		"vehicle_id, gps_record_time,  packet_status, imei, vehicle_number "+
		Misc.setParamInt(ps, this.getVehicleId(), index++);
		ps.setTimestamp(index++, Misc.longToSqlDate(this.getGpsRecordTime()));
		ps.setInt(index++, this.getPacketStatus() ? 1 : 0);
		ps.setString(index++, trim(this.getImei(), 16));
		ps.setString(index++, trim(this.getVehicleNumber(), 24));
		//", gps_valid, longitude, latitude, speed, heading "+
		ps.setInt(index++, this.isGpsValid() ? 1 : 0);
		Misc.setParamDouble(ps, this.getLon(), index++);
		Misc.setParamDouble(ps, this.getLat(), index++);
		Misc.setParamDouble(ps, this.getSpeed(), index++);
		Misc.setParamDouble(ps, this.getHeading(), index++);
		//", odometer "+
		Misc.setParamDouble(ps, this.getOdoMeter(), index++);
		ps.setTimestamp(index++, Misc.longToSqlDate(this.gpsRecvTime));
		ps.setString(index++, this.getMessageString());
		ps.executeUpdate();
		ps = Misc.closePS(ps);
	}
	private static String g_insHealth = "insert ignore into ais_health_data( " +
	"vehicle_id, gps_record_time,  vendor_code, firmware_version, imei "+
	", batt_perc, low_batt_thresh, mem_perc, ign_on_rate, ign_off_rate "+
	", din0, din1, din2, din3, din4 "+
	", din5, ain0, ain1, ain2, ain3 "+
	", ain4, ain5,message_string "+
	") values ( "+
	"?,?,?,?,? "+
	",?,?,?,?,? "+
	",?,?,?,?,? "+
	",?,?,?,?,? "+
	",?,?,? "+
	")";
	private void saveHealth(Connection conn) throws Exception {
		PreparedStatement ps = conn.prepareStatement(g_insHealth);
		int index = 1;
		
		

	//	"vehicle_id, gps_record_time,  vendor_code, firmware_version, imei "+
		Misc.setParamInt(ps, this.getVehicleId(), index++);
		ps.setTimestamp(index++, Misc.longToSqlDate(this.getGpsRecordTime()));
		ps.setString(index++, trim(this.getVendorId(), 16));
		ps.setString(index++, trim(this.getFirmwareVersion(), 16));
		ps.setString(index++, trim(this.getImei(), 16));
//		", batt_perc, low_batt_thresh, mem_perc, ign_on_rate, ign_off_rate "+
		Misc.setParamInt(ps, this.getInternalBatteryPerc(), index++);
		Misc.setParamInt(ps, this.getLowBatteryThresh(), index++);
		Misc.setParamInt(ps, this.getMemoryPerc(), index++);
		Misc.setParamInt(ps, this.getIgnOnDataRate(),index++);
		Misc.setParamInt(ps, this.getIgnOffDataRate(), index++);
		//", din0, din1, din2, din3, din4 "+
		ps.setInt(index++, this.getDigitalInputStatus(0)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(1)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(2)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(3)?1:0);
		ps.setInt(index++, this.getDigitalInputStatus(4)?1:0);
		//", din5, ain0, ain1, ain2, ain3 "+
		ps.setInt(index++, this.getDigitalInputStatus(5)?1:0);
		Misc.setParamDouble(ps, this.getAnalogInput(0), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(1), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(2), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(3), index++);
		//", ain4, ain5 "+
		Misc.setParamDouble(ps, this.getAnalogInput(4), index++);
		Misc.setParamDouble(ps, this.getAnalogInput(5), index++);
		ps.setString(index++, this.getMessageString());
		ps.executeUpdate();
		ps = Misc.closePS(ps);
	}
	public static String g_insVehWoId = "insert into vehicle("+
	"customer_id,name,std_name,type,device_internal_id"+
	",device_model_info_id,status,install_date,updated_on,io_set_id"+
	",detailed_status,do_rule,do_trip,manual_adj_tz_min,sim_number"+
	",sub_type"+
	")  values ("+
	"?,?,?,?,?"+
	",?,?,?,?,?"+
	",?,?,?,?,?"+
	",?"+
	")";
	public static String g_insVehWithId = "insert into vehicle("+
	"customer_id,name,std_name,type,device_internal_id"+
	",device_model_info_id,status,install_date,updated_on,io_set_id"+
	",detailed_status,std_name,do_rule,do_trip,manual_adj_tz_min,sim_number"+
	",sub_type,id"+
	")  values ("+
	"?,?,?,?,?"+
	",?,?,?,?,?"+
	",?,?,?,?,?"+
	",?,?"+
	")";
	public static int G_DEFAULT_CUSTOMER_ID = 1;//unassigned
	public static int G_DEFAULT_VEHICLE_TYPE = 0;
	public static int G_DEFAULT_MODEL_ID = 1;
	public static int G_DEFAULT_IOSETID = 1;
	public static int G_DEFAULT_SUBTYPE = 0;
	private static int helperInsertVehicle(Connection conn, String vehicleName, String mobileNumber,String imei, int useVehicleId) throws Exception {
		PreparedStatement ps = conn.prepareStatement(Misc.isUndef(useVehicleId) ? g_insVehWoId : g_insVehWithId);
		int index = 1;

		//"customer_id,name,std_name,type,device_internal_id"+
		ps.setInt(index++, G_DEFAULT_CUSTOMER_ID);
		ps.setString(index++, g_newVehByDevice ? imei : vehicleName);
		ps.setString(index++, g_newVehByDevice ? imei : vehicleName);
		ps.setInt(index++, G_DEFAULT_VEHICLE_TYPE);
		ps.setString(index++, imei);
		//",device_model_info_id,status,install_date,updated_on,io_set_id"+
		ps.setInt(index++, G_DEFAULT_MODEL_ID);
		ps.setInt(index++,1);
		java.sql.Timestamp ts = Misc.longToSqlDate(System.currentTimeMillis());
		ps.setTimestamp(index++, ts);
		ps.setTimestamp(index++, ts);
		ps.setInt(index++, G_DEFAULT_IOSETID);
		//",detailed_status,do_rule,do_trip,manual_adj_tz_min,sim_number"+
		ps.setInt(index++, 1);
		ps.setInt(index++, 0);
		ps.setInt(index++, 0);
		Misc.setParamInt(ps, Misc.getUndefInt(), index++);//ps.setInt(index++, 0);
		ps.setString(index++,mobileNumber);
		//",sub_type,id"+
		ps.setInt(index++, G_DEFAULT_SUBTYPE);
		if (!Misc.isUndef(useVehicleId))
			ps.setInt(index++, useVehicleId);
		ps.executeUpdate();
		if (Misc.isUndef(useVehicleId)) {
			ResultSet rs = ps.getGeneratedKeys();
			useVehicleId = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
			rs = Misc.closeRS(rs);
		}
		ps = Misc.closePS(ps);
		return useVehicleId;
	}
	
	
	public String getVehicleNumber() {
		return vehicleNumber;
	}
	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getFirmwareVersion() {
		return firmwareVersion;
	}
	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}
	public String getProtocolVersion() {
		return protocolVersion;
	}
	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public int getPacketType() {
		return packetType;
	}
	public void setPacketType(int packetType) {
		this.packetType = packetType;
	}
	public boolean getPacketStatus() {
		return packetStatus;
	}
	public void setPacketStatus(boolean packetStatus) {
		this.packetStatus = packetStatus;
	}
	public boolean isGpsValid() {
		return gpsValid;
	}
	public void setGpsValid(boolean gpsValid) {
		this.gpsValid = gpsValid;
	}
	public long getGpsRecordTime() {
		return gpsRecordTime;
	}
	public void setGpsRecordTime(long gpsRecordTime) {
		this.gpsRecordTime = gpsRecordTime;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getHeading() {
		return heading;
	}
	public void setHeading(double heading) {
		this.heading = heading;
	}
	public int getNumSat() {
		return numSat;
	}
	public void setNumSat(int numSat) {
		this.numSat = numSat;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getPdop() {
		return pdop;
	}
	public void setPdop(double pdop) {
		this.pdop = pdop;
	}
	public double getHdop() {
		return hdop;
	}
	public void setHdop(double hdop) {
		this.hdop = hdop;
	}
	public String getNwOperatorName() {
		return nwOperatorName;
	}
	public void setNwOperatorName(String nwOperatorName) {
		this.nwOperatorName = nwOperatorName;
	}
	public boolean isIgnStatus() {
		return ignStatus;
	}
	public void setIgnStatus(boolean ignStatus) {
		this.ignStatus = ignStatus;
	}
	public boolean isMainPowerStatus() {
		return mainPowerStatus;
	}
	public void setMainPowerStatus(boolean mainPowerStatus) {
		this.mainPowerStatus = mainPowerStatus;
	}
	public double getMainVoltage() {
		return mainVoltage;
	}
	public void setMainVoltage(double mainVoltage) {
		this.mainVoltage = mainVoltage;
	}
	public double getInternalVoltage() {
		return internalVoltage;
	}
	public void setInternalVoltage(double internalVoltage) {
		this.internalVoltage = internalVoltage;
	}
	public boolean isEmergencyStatus() {
		return emergencyStatus;
	}
	public void setEmergencyStatus(boolean emergencyStatus) {
		this.emergencyStatus = emergencyStatus;
	}
	public int getGsmStrength() {
		return gsmStrength;
	}
	public void setGsmStrength(int gsmStrength) {
		this.gsmStrength = gsmStrength;
	}
	public String getMccCountryCode() {
		return mccCountryCode;
	}
	public void setMccCountryCode(String mccCountryCode) {
		this.mccCountryCode = mccCountryCode;
	}
	public String getMncConde() {
		return mncConde;
	}
	public void setMncConde(String mncConde) {
		this.mncConde = mncConde;
	}
	public String getLacCode() {
		return lacCode;
	}
	public void setLacCode(String lacCode) {
		this.lacCode = lacCode;
	}
	public String getCellId() {
		return cellId;
	}
	public void setCellId(String cellId) {
		this.cellId = cellId;
	}
	public String getNeighbourLacCode1() {
		return neighbourLacCode1;
	}
	public void setNeighbourLacCode1(String neighbourLacCode1) {
		this.neighbourLacCode1 = neighbourLacCode1;
	}
	public String getNeighbourCellId1() {
		return neighbourCellId1;
	}
	public void setNeighbourCellId1(String neighbourCellId1) {
		this.neighbourCellId1 = neighbourCellId1;
	}
	public int getNeighbourStrength1() {
		return neighbourStrength1;
	}
	public void setNeighbourStrength1(int neighbourStrength1) {
		this.neighbourStrength1 = neighbourStrength1;
	}
	public String getNeighbourLacCode2() {
		return neighbourLacCode2;
	}
	public void setNeighbourLacCode2(String neighbourLacCode2) {
		this.neighbourLacCode2 = neighbourLacCode2;
	}
	public String getNeighbourCellId2() {
		return neighbourCellId2;
	}
	public void setNeighbourCellId2(String neighbourCellId2) {
		this.neighbourCellId2 = neighbourCellId2;
	}
	public int getNeighbourStrength2() {
		return neighbourStrength2;
	}
	public void setNeighbourStrength2(int neighbourStrength2) {
		this.neighbourStrength2 = neighbourStrength2;
	}
	public String getNeighbourLacCode3() {
		return neighbourLacCode3;
	}
	public void setNeighbourLacCode3(String neighbourLacCode3) {
		this.neighbourLacCode3 = neighbourLacCode3;
	}
	public String getNeighbourCellId3() {
		return neighbourCellId3;
	}
	public void setNeighbourCellId3(String neighbourCellId3) {
		this.neighbourCellId3 = neighbourCellId3;
	}
	public int getNeighbourStrength3() {
		return neighbourStrength3;
	}
	public void setNeighbourStrength3(int neighbourStrength3) {
		this.neighbourStrength3 = neighbourStrength3;
	}
	public String getNeighbourLacCode4() {
		return neighbourLacCode4;
	}
	public void setNeighbourLacCode4(String neighbourLacCode4) {
		this.neighbourLacCode4 = neighbourLacCode4;
	}
	public String getNeighbourCellId4() {
		return neighbourCellId4;
	}
	public void setNeighbourCellId4(String neighbourCellId4) {
		this.neighbourCellId4 = neighbourCellId4;
	}
	public int getNeighbourStrength4() {
		return neighbourStrength4;
	}
	public void setNeighbourStrength4(int neighbourStrength4) {
		this.neighbourStrength4 = neighbourStrength4;
	}
	public boolean[] getDigitalInputStatus() {
		return digitalInputStatus;
	}
	public void setDigitalInputStatus(int i, boolean digitalInputStatus) {
		if (i >= 0 && i < this.digitalInputStatus.length)
		this.digitalInputStatus[i] = digitalInputStatus;
	}
	public boolean getDigitalInputStatus(int i) {
		return i < 0 || i >= digitalInputStatus.length ? false : digitalInputStatus[i];
	}
	public void setDigitalInputStatus(boolean[] digitalInputStatus) {
		this.digitalInputStatus = digitalInputStatus;
	}
	public boolean[] getDigitalOutputStatus() {
		return digitalOutputStatus;
	}
	public void setDigitalOutputStatus(boolean[] digitalOutputStatus) {
		this.digitalOutputStatus = digitalOutputStatus;
	}
	public boolean getDigitalOutputStatus(int i) {
		return i < 0 || i >= digitalOutputStatus.length ? false : digitalOutputStatus[i];
	}
	public void setDigitalOutputStatus(int i, boolean digitalOutputStatus) {
		if (i >= 0 && i < this.digitalOutputStatus.length)
		this.digitalOutputStatus[i] = digitalOutputStatus;
	}
	public double[] getAnalogInput() {
		return analogInput;
	}
	public void setAnalogInput(double[] analogInput) {
		this.analogInput = analogInput;
	}
	public double getAnalogInput(int i) {
		return i >=0 && i < this.analogInput.length ? analogInput[i] : Misc.getUndefDouble();
	}
	public void setAnalogInput(int i, double analogInput) {
		if (i>=0 && i<this.analogInput.length)
			this.analogInput[i] = analogInput;
	}
	public int getFrameNumber() {
		return frameNumber;
	}
	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}
	public String getOtaParamChanged() {
		return otaParamChanged;
	}
	public void setOtaParamChanged(String otaParamChanged) {
		this.otaParamChanged = otaParamChanged;
	}
	public String getOtaParamChangeSource() {
		return otaParamChangeSource;
	}
	public void setOtaParamChangeSource(String otaParamChangeSource) {
		this.otaParamChangeSource = otaParamChangeSource;
	}
	public String getCheckSumSent() {
		return checkSumSent;
	}
	public void setCheckSumSent(String checkSumSent) {
		this.checkSumSent = checkSumSent;
	}
	public int getInternalBatteryPerc() {
		return internalBatteryPerc;
	}
	public void setInternalBatteryPerc(int internalBatteryPerc) {
		this.internalBatteryPerc = internalBatteryPerc;
	}
	public int getLowBatteryThresh() {
		return lowBatteryThresh;
	}
	public void setLowBatteryThresh(int lowBatteryThresh) {
		this.lowBatteryThresh = lowBatteryThresh;
	}
	public int getMemoryPerc() {
		return memoryPerc;
	}
	public void setMemoryPerc(int memoryPerc) {
		this.memoryPerc = memoryPerc;
	}
	public int getIgnOnDataRate() {
		return ignOnDataRate;
	}
	public void setIgnOnDataRate(int ignOnDataRate) {
		this.ignOnDataRate = ignOnDataRate;
	}
	public int getIgnOffDataRate() {
		return ignOffDataRate;
	}
	public void setIgnOffDataRate(int ignOffDataRate) {
		this.ignOffDataRate = ignOffDataRate;
	}
	public double getOdoMeter() {
		return odoMeter;
	}
	public void setOdoMeter(double odoMeter) {
		this.odoMeter = odoMeter;
	}
	public int getLoctionSource() {
		return loctionSource;
	}
	public void setLoctionSource(int loctionSource) {
		this.loctionSource = loctionSource;
	}
	public String getSubPacketType() {
		return subPacketType;
	}
	public void setSubPacketType(String subPacketType) {
		this.subPacketType = subPacketType;
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}
	
	public static void main(String[] args) {
		//2013-06-07 09:09:04
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			String login = "$LGN,DDNNAC1234,891362031104100,0031AI14,1.0,28.451448,N,77.071343,E*";
			String normal = "$EPB,TNT,0050AI14,NR,L,861359031085689,DDNNAC1234,1,23072018092824,28.451317,N,77.071683,E,0.0,8.57,9,265.4,0.00,0.00,Idea Cellular L,1,1,12.1,5.6,0,18,404,4,74,dfc1,74,dfc2,0,74,5e43,0,74,dfc3,0,0,0,0,1001,00,20,4C9E*";
			String normal2 = "$EPB,TNT,0050AI14,OT,L,861359030984502,DDNNAC1234,1,21072018073245,28.451420,N,77.071712,E,0.0,353.54,6,239.5,0.00,0.00,Idea Cellular L,1,1,12.1,5.6,0,23,404,4,74,dfc2,74,dfc3,0,74,5e43,0,74,dfc1,0,74,5e45,0,1100,00,16,APN,SMS,9991123456,E862*";
			String normal3 = "$EPB,TNT,0060AI14,NR,01,L,861359030984742,DDNNAC1234,1,010918,112653,28.451358,N,77.071587,E,0.0,302.18,13,247.2,0.00,0.00,airtel,1,1,11.7,4.4,0,O,GEO-NA,27,404,10,96,3652,96,3654,24,96,3653,24,96,35dc,17,96,40df,11,1110,00,039824,9906*";
			String normal4 =  "$EPB,ITP,0005ITP1,NR,01,L,861359030984940,DDNNAC1234,1,060918,130926,28.451220,N,77.071700,E,0.0,138.21,12,203.2,1.15,0.80,46,Vodafone - Delh,1,1,11.4,4.3,0,O,GEO-IN,20,404,11,9b,390,96,3652,0,96,3654,0,96,35dc,0,96,40df,0,1110,00,020234,E6D8*";
			String normal5 =  "$EPB,ITP,0005ITP1,NR,01,L,861359030984940,DDNNAC1234,1,060918,130926,28.451220,N,77.071700,E,0.0,138.21,12,203.2,1.15,0.80,46,Vodafone - Delh,1,1,11.4,4.3,0,O,GEO-IN,20,404,11,9b,390,96,3652,0,96,3654,0,96,35dc,0,96,40df,0,1110,00,020234,11.1,12.3,E6D8*";
			String emergency = "$EPB,EMR,891359032344502,NM,20072018102750,A,00028.451448,N,00077.071343,E,0000000229.2,002.68,0000.0,G,000000DDNNAC1234*00005daa";
			String health = "$HLT,TNT,0050AI14,891362031104100,100,10,50,10,60,192,1187,505,0,0,1,0*"; 
			String health2 = "$HLT,TNT,0050AI14,861459031104100,100,10,50,10,60,192,1187,505,0,0,1,0*";
			HAS_SOMETHING_BEFORE_LIVE = false;
			HAS_GEO = false;
			HAS_DATE_TIME_SPLIT = false;
			HAS_ODOMETER = false;
			AISData loginRes = get(login);
		//	loginRes.save(conn);
			
			AISData normalRes = get(normal);
			//normalRes.save(conn);
			normalRes = get(normal2);
			//normalRes.save(conn);
			HAS_SOMETHING_BEFORE_LIVE = true;
			HAS_GEO = true;
			HAS_DATE_TIME_SPLIT = true;
			normalRes = get(normal3);
			HAS_ODOMETER = true;
			normalRes = get(normal4);
			normalRes = get(normal5);

			HAS_SOMETHING_BEFORE_LIVE = false;
			HAS_GEO = false;
			HAS_DATE_TIME_SPLIT = false;
			HAS_ODOMETER = false;
			AISData emergencyRes = get(emergency);
			//emergencyRes.save(conn);

			AISData healthRes = get(health);
			//healthRes.save(conn);
			healthRes = get(health2);
			//healthRes.save(conn);
			
		}
		catch (Exception e) {
			destroyIt = true;
			e.printStackTrace();
		}
		finally {
			if (conn != null) {
				try {
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e) {
					
				}
				
			}
		}
	}

	public String getGeoInOut() {
		return geoInOut;
	}

	public void setGeoInOut(String geoInOut) {
		this.geoInOut = geoInOut;
	}

	public String getGeoInOutName() {
		return geoInOutName;
	}

	public void setGeoInOutName(String geoInOutName) {
		this.geoInOutName = geoInOutName;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	public String getField3() {
		return field3;
	}

	public void setField3(String field3) {
		this.field3 = field3;
	}
}

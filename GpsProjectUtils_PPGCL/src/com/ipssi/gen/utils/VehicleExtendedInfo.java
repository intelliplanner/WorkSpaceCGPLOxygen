package com.ipssi.gen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.exception.GenericException;

public class VehicleExtendedInfo{
	private int vehicleId;
	private int capacitySec;
	private String registerationNumber;
	private long registerationNumberExpiry;
	private String insuranceNumber;
	private long insuranceNumberExpiry;
	private String permit1Number;
	private long permit1NumberExpiry;
	private String permit1Desc;
	private String permit2Number;
	private long permit2NumberExpiry;
	private String permit2Desc;
	private double workingHrs;
	private long hiredFrom;
	private double rentalRateUsage;
	private double rentalRateRetainer;
	private long acquisitionDate;
	private long releaseDate;
	private String notes;
	private int lovField1;
	private int lovField2;
	private int lovField3;
	private int lovField4;
	private int lovField5;
	private int lovField6;
	private int lovField7;
	private int lovField8;
	private int lovField9;
	private int lovField10;
	private double doubleField1;
	private double doubleField2;
	private double doubleField3;
	private double doubleField4;
	private double doubleField5;
	private double doubleField6;
	private double doubleField7;
	private double doubleField8;
	private double doubleField9;
	private double doubleField10;
	private String strField1;
	private String strField2;
	private String strField3;
	private String strField4;
	private String strField5;
	private String strField6;
	private String strField7;
	private String strField8;
	private String strField9;
	private String strField10;
	private long dateField1;
	private long dateField2;
	private long dateField3;
	private long dateField4;
	private long dateField5;
	private long dateField6;
	private long dateField7;
	private long dateField8;
	private long dateField9;
	private long dateField10;
	private int plant;
	private int purpose;
	private int transporterId;
	private int extendedStatus;
	private int otherVehicleId;
	
	private String miscellaneous;
	private String driver;
	private String fieldone;
	private String fieldtwo;
	private String fieldthree;
	private String fieldfour;
	private String fieldfive;
	private String fieldsix;
	private String fieldseven;
	private String fieldeight;
	private String fieldnine;
	private String fieldten;
	private String fieldeleven;
	private String fieldtwelve;
	private String fieldthirteen;
	private String fieldfourteen;
	private int make;
	private int model;
	private int capacity;
	
	private int cause;
	private String lastComment;
	private String lastCommentlocation;
	private long lastCommentTime;
	private long nextFollowTime;
	private String commentUser;
	private int driverDetailId;

	
	private static Object syscDataVariable = new Object();
	private static ConcurrentHashMap<Integer, VehicleExtendedInfo> vehicleExtMap = null;
	public static void clear(){
		synchronized(syscDataVariable) {
			if(vehicleExtMap != null)
				vehicleExtMap.clear();
			vehicleExtMap = null;
			
		}
	}
	public static VehicleExtendedInfo getVehicleExtended(Connection conn, int vehicleId){
		if(vehicleExtMap == null)
			loadVehicleExtended(conn, Misc.getUndefInt());
		return vehicleExtMap != null ? vehicleExtMap.get(vehicleId) : null;
	}
	private static void loadVehicleExtended(Connection conn,int vehicleId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		VehicleExtendedInfo vehicle = null;
		try{
			String query = " " +
					" select vehicle.id, capacity_sec, registeration_number, registeration_number_expiry," +
					" insurance_number,insurance_number_expiry,permit1_number,permit1_number_expiry,permit1_desc," +
					" permit2_number,permit2_number_expiry,permit2_desc,working_hrs,hired_from,rental_rate_usage," +
					" rental_rate_retainer,acquisition_date,release_date,vehicle_extended.notes,lov_field1,lov_field2,lov_field3," +
					" lov_field4,lov_field5,lov_field6,lov_field7,lov_field8,lov_field9,lov_field10,double_field1," +
					" double_field2,double_field3,double_field4,double_field5,double_field6,double_field7," +
					" double_field8,double_field9,double_field10,str_field1,str_field2,str_field3,str_field4," +
					" str_field5,str_field6,str_field7,str_field8,str_field9,str_field10,date_field1,date_field2," +
					" date_field3,date_field4,date_field5,date_field6,date_field7,date_field8,date_field9," +
					" date_field10,vehicle_extended.plant,purpose,transporter_id,extended_status,other_vehicle_id, " +
					" miscellaneous, driver, fieldone, fieldtwo, fieldthree, fieldfour, fieldfive, fieldsix, fieldseven, " +
					" fieldeight, fieldnine, fieldten, fieldeleven, fieldtwelve, fieldthirteen, fieldfourteen, make, model, capacity, " +
					" cause,interaction_notes.notes last_comment,location,next_follow_time,users.name user_name,interaction_notes.updated_on last_comment_time " +
					" from vehicle left outer join vehicle_extended on (vehicle.id=vehicle_extended.vehicle_id) " +
					" left outer join last_vehicle_interaction_notes interaction_notes  on (interaction_notes.vehicle_id = vehicle.id) left outer join users on (interaction_notes.user_id = users.id) " +
					" where ((capacity_sec > -1)  or (registeration_number is not null and TRIM(registeration_number) != '') or  (registeration_number_expiry is not null) or (insurance_number is not null and TRIM(insurance_number) != '') or (insurance_number_expiry is not null) or (permit1_number is not null and TRIM(permit1_number) != '') or (permit1_number_expiry is not null) or (permit1_desc is not null and TRIM(permit1_desc) != '') or (permit2_number is not null and TRIM(permit2_number) != '') or (permit2_number_expiry is not null) or (permit2_desc is not null and TRIM(permit2_desc) != '') or (working_hrs > -1) or (hired_from > -1) or (rental_rate_usage > -1) or (rental_rate_retainer > -1) or (acquisition_date is not null) or (release_date is not null) or (vehicle_extended.notes is not null and TRIM(vehicle_extended.notes) != '') or (lov_field1 > -1) or (lov_field2 > -1) or (lov_field3 > -1) or (lov_field4 > -1) or (lov_field5 > -1) or (lov_field6 > -1) or (lov_field7 > -1) or (lov_field8 > -1) or (lov_field9 > -1) or (lov_field10 > -1) or (double_field1 > -1) or (double_field2 > -1) or (double_field3 > -1) or (double_field4 > -1) or (double_field5 > -1) or (double_field6 > -1) or (double_field7 > -1) or (double_field8 > -1) or (double_field9 > -1) or (double_field10 > -1) or (str_field1 is not null and TRIM(str_field1) != '') or (str_field2 is not null and TRIM(str_field2) != '') or (str_field3 is not null and TRIM(str_field3) != '') or (str_field4 is not null and TRIM(str_field4) != '') or (str_field5 is not null and TRIM(str_field5) != '') or (str_field6 is not null and TRIM(str_field6) != '') or (str_field7 is not null and TRIM(str_field7) != '') or (str_field8 is not null and TRIM(str_field8) != '') or (str_field9 is not null and TRIM(str_field9) != '') or (str_field10 is not null and TRIM(str_field10) != '') or (date_field1 is not null) or (date_field2 is not null) or (date_field3 is not null) or (date_field4 is not null) or (date_field5 is not null) or (date_field6 is not null) or (date_field7 is not null) or (date_field8 is not null) or (date_field9 is not null) or (date_field10 is not null) or (vehicle_extended.plant > -1) or (purpose > -1) or (transporter_id > -1) or ( miscellaneous is not null and TRIM(miscellaneous) != '') or ( driver is not null and TRIM(driver) != '') or ( fieldone is not null and TRIM(fieldone) != '') or ( fieldtwo is not null and TRIM(fieldtwo) != '') or ( fieldthree is not null and TRIM(fieldthree) != '') or ( fieldfour is not null and TRIM(fieldfour) != '') or ( fieldfive is not null and TRIM(fieldfive) != '') or ( fieldsix is not null and TRIM(fieldsix) != '') or ( fieldseven is not null and TRIM(fieldseven) != '') or ( fieldeight is not null and TRIM(fieldeight) != '') or ( fieldnine is not null and TRIM(fieldnine) != '') or ( fieldten is not null and TRIM(fieldten) != '') or ( fieldeleven is not null and TRIM(fieldeleven) != '') or ( fieldtwelve is not null and TRIM(fieldtwelve) != '') or ( fieldthirteen is not null and TRIM(fieldthirteen) != '') or ( fieldfourteen is not null and TRIM(fieldfourteen) != '') or ( make > -1) or ( model > -1) or ( capacity > -1) or (cause > -1) or (interaction_notes.notes is not null and TRIM(interaction_notes.notes) != '') or (location is not null and TRIM(location) != '') or (next_follow_time is not null) or (users.name is not null and TRIM(users.name) != '') or (interaction_notes.updated_on is not null))  and vehicle.status=1";
			if(!Misc.isUndef(vehicleId))
				query += " and vehicle.id =" + vehicleId;
			ps = conn.prepareStatement(query);
			synchronized(syscDataVariable) {
				rs = ps.executeQuery();
				while(rs.next()){
					vehicle = new VehicleExtendedInfo();
					vehicle.setVehicleId(Misc.getRsetInt(rs, "id"));
					vehicle.setCapacitySec(Misc.getRsetInt(rs, "capacity_sec"));
					vehicle.setRegisterationNumber(rs.getString("registeration_number"));
					vehicle.setRegisterationNumberExpiry(Misc.getDateInLong(rs, "registeration_number_expiry"));
					vehicle.setInsuranceNumber(rs.getString("insurance_number"));
					vehicle.setInsuranceNumberExpiry(Misc.getDateInLong(rs, "insurance_number_expiry"));
					vehicle.setPermit1Number(rs.getString("permit1_number"));
					vehicle.setPermit1NumberExpiry(Misc.getDateInLong(rs, "permit1_number_expiry"));
					vehicle.setPermit1Desc(rs.getString("permit1_desc"));
					vehicle.setPermit2Number(rs.getString("permit2_number"));
					vehicle.setPermit2NumberExpiry(Misc.getDateInLong(rs, "permit2_number_expiry"));
					vehicle.setPermit2Desc(rs.getString("permit2_desc"));
					vehicle.setWorkingHrs(Misc.getRsetDouble(rs, "working_hrs"));
					vehicle.setHiredFrom(Misc.getDateInLong(rs, "hired_from"));
					vehicle.setRentalRateUsage(Misc.getRsetDouble(rs, "rental_rate_usage"));
					vehicle.setRentalRateRetainer(Misc.getRsetDouble(rs, "rental_rate_retainer"));
					vehicle.setAcquisitionDate(Misc.getDateInLong(rs, "acquisition_date"));
					vehicle.setReleaseDate(Misc.getDateInLong(rs, "release_date"));
					vehicle.setNotes(rs.getString("notes"));
					
					vehicle.setLovField1(Misc.getRsetInt(rs, "lov_field1"));
					vehicle.setLovField2(Misc.getRsetInt(rs, "lov_field2"));
					vehicle.setLovField3(Misc.getRsetInt(rs, "lov_field3"));
					vehicle.setLovField4(Misc.getRsetInt(rs, "lov_field4"));
					vehicle.setLovField5(Misc.getRsetInt(rs, "lov_field5"));
					vehicle.setLovField6(Misc.getRsetInt(rs, "lov_field6"));
					vehicle.setLovField7(Misc.getRsetInt(rs, "lov_field7"));
					vehicle.setLovField8(Misc.getRsetInt(rs, "lov_field8"));
					vehicle.setLovField9(Misc.getRsetInt(rs, "lov_field9"));
					vehicle.setLovField10(Misc.getRsetInt(rs, "lov_field10"));
					
					vehicle.setDoubleField1(Misc.getRsetDouble(rs, "double_field1"));
					vehicle.setDoubleField2(Misc.getRsetDouble(rs, "double_field2"));
					vehicle.setDoubleField3(Misc.getRsetDouble(rs, "double_field3"));
					vehicle.setDoubleField4(Misc.getRsetDouble(rs, "double_field4"));
					vehicle.setDoubleField5(Misc.getRsetDouble(rs, "double_field5"));
					vehicle.setDoubleField6(Misc.getRsetDouble(rs, "double_field6"));
					vehicle.setDoubleField7(Misc.getRsetDouble(rs, "double_field7"));
					vehicle.setDoubleField8(Misc.getRsetDouble(rs, "double_field8"));
					vehicle.setDoubleField9(Misc.getRsetDouble(rs, "double_field9"));
					vehicle.setDoubleField10(Misc.getRsetDouble(rs, "double_field10"));
					
					vehicle.setStrField1(rs.getString("str_field1"));
					vehicle.setStrField2(rs.getString("str_field2"));
					vehicle.setStrField3(rs.getString("str_field3"));
					vehicle.setStrField4(rs.getString("str_field4"));
					vehicle.setStrField5(rs.getString("str_field5"));
					vehicle.setStrField6(rs.getString("str_field6"));
					vehicle.setStrField7(rs.getString("str_field7"));
					vehicle.setStrField8(rs.getString("str_field8"));
					vehicle.setStrField9(rs.getString("str_field9"));
					vehicle.setStrField10(rs.getString("str_field10"));
					
					vehicle.setDateField1(Misc.getDateInLong(rs, "date_field1"));
					vehicle.setDateField2(Misc.getDateInLong(rs, "date_field2"));
					vehicle.setDateField3(Misc.getDateInLong(rs, "date_field3"));
					vehicle.setDateField4(Misc.getDateInLong(rs, "date_field4"));
					vehicle.setDateField5(Misc.getDateInLong(rs, "date_field5"));
					vehicle.setDateField6(Misc.getDateInLong(rs, "date_field6"));
					vehicle.setDateField7(Misc.getDateInLong(rs, "date_field7"));
					vehicle.setDateField8(Misc.getDateInLong(rs, "date_field8"));
					vehicle.setDateField9(Misc.getDateInLong(rs, "date_field9"));
					vehicle.setDateField10(Misc.getDateInLong(rs, "date_field10"));
					
					vehicle.setPlant(Misc.getRsetInt(rs, "plant"));
					vehicle.setPurpose(Misc.getRsetInt(rs, "purpose"));
					vehicle.setTransporterId(Misc.getRsetInt(rs, "transporter_id"));
					vehicle.setExtendedStatus(Misc.getRsetInt(rs, "extended_status"));
					vehicle.setOtherVehicleId(Misc.getRsetInt(rs, "other_vehicle_id"));
					
					vehicle.setMiscellaneous(rs.getString("miscellaneous"));
					vehicle.setDriver(rs.getString("driver"));
					vehicle.setFieldone(rs.getString("fieldone"));
					vehicle.setFieldtwo(rs.getString("fieldtwo"));
					vehicle.setFieldthree(rs.getString("fieldthree"));
					vehicle.setFieldfour(rs.getString("fieldfour"));
					vehicle.setFieldfive(rs.getString("fieldfive"));
					vehicle.setFieldsix(rs.getString("fieldsix"));
					vehicle.setFieldseven(rs.getString("fieldseven"));
					vehicle.setFieldeight(rs.getString("fieldeight"));
					vehicle.setFieldnine(rs.getString("fieldnine"));
					vehicle.setFieldten(rs.getString("fieldten"));
					vehicle.setFieldeleven(rs.getString("fieldeleven"));
					vehicle.setFieldtwelve(rs.getString("fieldtwelve"));
					vehicle.setFieldthirteen(rs.getString("fieldthirteen"));
					vehicle.setFieldfourteen(rs.getString("fieldfourteen"));
					vehicle.setMake(Misc.getRsetInt(rs, "make"));
					vehicle.setModel(Misc.getRsetInt(rs, "model"));
					vehicle.setCapacity(Misc.getRsetInt(rs, "capacity"));
					
					vehicle.setCause(Misc.getRsetInt(rs, "cause"));
					vehicle.setLastComment(rs.getString("last_comment"));
					vehicle.setLastCommentlocation(rs.getString("location"));
					vehicle.setNextFollowTime(Misc.getDateInLong(rs, "next_follow_time"));
					vehicle.setLastCommentTime(Misc.getDateInLong(rs, "last_comment_time"));
					vehicle.setCommentUser(rs.getString("user_name"));
					
					if (vehicleExtMap == null)
						vehicleExtMap = new ConcurrentHashMap<Integer, VehicleExtendedInfo>();
					vehicleExtMap.put(vehicle.getVehicleId(), vehicle);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	public static void markDirty(Connection conn, int vehicleId){
		loadVehicleExtended(conn, vehicleId);
	}
	public int getVehicleId() {
		return vehicleId;
	}
	public int getCapacitySec() {
		return capacitySec;
	}
	public String getRegisterationNumber() {
		return registerationNumber;
	}
	public long getRegisterationNumberExpiry() {
		return registerationNumberExpiry;
	}
	public String getInsuranceNumber() {
		return insuranceNumber;
	}
	public long getInsuranceNumberExpiry() {
		return insuranceNumberExpiry;
	}
	public String getPermit1Number() {
		return permit1Number;
	}
	public long getPermit1NumberExpiry() {
		return permit1NumberExpiry;
	}
	public String getPermit1Desc() {
		return permit1Desc;
	}
	public String getPermit2Number() {
		return permit2Number;
	}
	public long getPermit2NumberExpiry() {
		return permit2NumberExpiry;
	}
	public String getPermit2Desc() {
		return permit2Desc;
	}
	public double getWorkingHrs() {
		return workingHrs;
	}
	public long getHiredFrom() {
		return hiredFrom;
	}
	public double getRentalRateUsage() {
		return rentalRateUsage;
	}
	public double getRentalRateRetainer() {
		return rentalRateRetainer;
	}
	public long getAcquisitionDate() {
		return acquisitionDate;
	}
	public long getReleaseDate() {
		return releaseDate;
	}
	public String getNotes() {
		return notes;
	}
	public int getLovField1() {
		return lovField1;
	}
	public int getLovField2() {
		return lovField2;
	}
	public int getLovField3() {
		return lovField3;
	}
	public int getLovField4() {
		return lovField4;
	}
	public int getLovField5() {
		return lovField5;
	}
	public int getLovField6() {
		return lovField6;
	}
	public int getLovField7() {
		return lovField7;
	}
	public int getLovField8() {
		return lovField8;
	}
	public int getLovField9() {
		return lovField9;
	}
	public int getLovField10() {
		return lovField10;
	}
	public double getDoubleField1() {
		return doubleField1;
	}
	public double getDoubleField2() {
		return doubleField2;
	}
	public double getDoubleField3() {
		return doubleField3;
	}
	public double getDoubleField4() {
		return doubleField4;
	}
	public double getDoubleField5() {
		return doubleField5;
	}
	public double getDoubleField6() {
		return doubleField6;
	}
	public double getDoubleField7() {
		return doubleField7;
	}
	public double getDoubleField8() {
		return doubleField8;
	}
	public double getDoubleField9() {
		return doubleField9;
	}
	public double getDoubleField10() {
		return doubleField10;
	}
	public String getStrField1() {
		return strField1;
	}
	public String getStrField2() {
		return strField2;
	}
	public String getStrField3() {
		return strField3;
	}
	public String getStrField4() {
		return strField4;
	}
	public String getStrField5() {
		return strField5;
	}
	public String getStrField6() {
		return strField6;
	}
	public String getStrField7() {
		return strField7;
	}
	public String getStrField8() {
		return strField8;
	}
	public String getStrField9() {
		return strField9;
	}
	public String getStrField10() {
		return strField10;
	}
	public long getDateField1() {
		return dateField1;
	}
	public long getDateField2() {
		return dateField2;
	}
	public long getDateField3() {
		return dateField3;
	}
	public long getDateField4() {
		return dateField4;
	}
	public long getDateField5() {
		return dateField5;
	}
	public long getDateField6() {
		return dateField6;
	}
	public long getDateField7() {
		return dateField7;
	}
	public long getDateField8() {
		return dateField8;
	}
	public long getDateField9() {
		return dateField9;
	}
	public long getDateField10() {
		return dateField10;
	}
	public int getPlant() {
		return plant;
	}
	public int getPurpose() {
		return purpose;
	}
	public int getTransporterId() {
		return transporterId;
	}
	public int getExtendedStatus() {
		return extendedStatus;
	}
	public int getOtherVehicleId() {
		return otherVehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public void setCapacitySec(int capacitySec) {
		this.capacitySec = capacitySec;
	}
	public void setRegisterationNumber(String registerationNumber) {
		this.registerationNumber = registerationNumber;
	}
	public void setRegisterationNumberExpiry(long registerationNumberExpiry) {
		this.registerationNumberExpiry = registerationNumberExpiry;
	}
	public void setInsuranceNumber(String insuranceNumber) {
		this.insuranceNumber = insuranceNumber;
	}
	public void setInsuranceNumberExpiry(long insuranceNumberExpiry) {
		this.insuranceNumberExpiry = insuranceNumberExpiry;
	}
	public void setPermit1Number(String permit1Number) {
		this.permit1Number = permit1Number;
	}
	public void setPermit1NumberExpiry(long permit1NumberExpiry) {
		this.permit1NumberExpiry = permit1NumberExpiry;
	}
	public void setPermit1Desc(String permit1Desc) {
		this.permit1Desc = permit1Desc;
	}
	public void setPermit2Number(String permit2Number) {
		this.permit2Number = permit2Number;
	}
	public void setPermit2NumberExpiry(long permit2NumberExpiry) {
		this.permit2NumberExpiry = permit2NumberExpiry;
	}
	public void setPermit2Desc(String permit2Desc) {
		this.permit2Desc = permit2Desc;
	}
	public void setWorkingHrs(double workingHrs) {
		this.workingHrs = workingHrs;
	}
	public void setHiredFrom(long hiredFrom) {
		this.hiredFrom = hiredFrom;
	}
	public void setRentalRateUsage(double rentalRateUsage) {
		this.rentalRateUsage = rentalRateUsage;
	}
	public void setRentalRateRetainer(double rentalRateRetainer) {
		this.rentalRateRetainer = rentalRateRetainer;
	}
	public void setAcquisitionDate(long acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}
	public void setReleaseDate(long releaseDate) {
		this.releaseDate = releaseDate;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public void setLovField1(int lovField1) {
		this.lovField1 = lovField1;
	}
	public void setLovField2(int lovField2) {
		this.lovField2 = lovField2;
	}
	public void setLovField3(int lovField3) {
		this.lovField3 = lovField3;
	}
	public void setLovField4(int lovField4) {
		this.lovField4 = lovField4;
	}
	public void setLovField5(int lovField5) {
		this.lovField5 = lovField5;
	}
	public void setLovField6(int lovField6) {
		this.lovField6 = lovField6;
	}
	public void setLovField7(int lovField7) {
		this.lovField7 = lovField7;
	}
	public void setLovField8(int lovField8) {
		this.lovField8 = lovField8;
	}
	public void setLovField9(int lovField9) {
		this.lovField9 = lovField9;
	}
	public void setLovField10(int lovField10) {
		this.lovField10 = lovField10;
	}
	public void setDoubleField1(double doubleField1) {
		this.doubleField1 = doubleField1;
	}
	public void setDoubleField2(double doubleField2) {
		this.doubleField2 = doubleField2;
	}
	public void setDoubleField3(double doubleField3) {
		this.doubleField3 = doubleField3;
	}
	public void setDoubleField4(double doubleField4) {
		this.doubleField4 = doubleField4;
	}
	public void setDoubleField5(double doubleField5) {
		this.doubleField5 = doubleField5;
	}
	public void setDoubleField6(double doubleField6) {
		this.doubleField6 = doubleField6;
	}
	public void setDoubleField7(double doubleField7) {
		this.doubleField7 = doubleField7;
	}
	public void setDoubleField8(double doubleField8) {
		this.doubleField8 = doubleField8;
	}
	public void setDoubleField9(double doubleField9) {
		this.doubleField9 = doubleField9;
	}
	public void setDoubleField10(double doubleField10) {
		this.doubleField10 = doubleField10;
	}
	public void setStrField1(String strField1) {
		this.strField1 = strField1;
	}
	public void setStrField2(String strField2) {
		this.strField2 = strField2;
	}
	public void setStrField3(String strField3) {
		this.strField3 = strField3;
	}
	public void setStrField4(String strField4) {
		this.strField4 = strField4;
	}
	public void setStrField5(String strField5) {
		this.strField5 = strField5;
	}
	public void setStrField6(String strField6) {
		this.strField6 = strField6;
	}
	public void setStrField7(String strField7) {
		this.strField7 = strField7;
	}
	public void setStrField8(String strField8) {
		this.strField8 = strField8;
	}
	public void setStrField9(String strField9) {
		this.strField9 = strField9;
	}
	public void setStrField10(String strField10) {
		this.strField10 = strField10;
	}
	public void setDateField1(long dateField1) {
		this.dateField1 = dateField1;
	}
	public void setDateField2(long dateField2) {
		this.dateField2 = dateField2;
	}
	public void setDateField3(long dateField3) {
		this.dateField3 = dateField3;
	}
	public void setDateField4(long dateField4) {
		this.dateField4 = dateField4;
	}
	public void setDateField5(long dateField5) {
		this.dateField5 = dateField5;
	}
	public void setDateField6(long dateField6) {
		this.dateField6 = dateField6;
	}
	public void setDateField7(long dateField7) {
		this.dateField7 = dateField7;
	}
	public void setDateField8(long dateField8) {
		this.dateField8 = dateField8;
	}
	public void setDateField9(long dateField9) {
		this.dateField9 = dateField9;
	}
	public void setDateField10(long dateField10) {
		this.dateField10 = dateField10;
	}
	public void setPlant(int plant) {
		this.plant = plant;
	}
	public void setPurpose(int purpose) {
		this.purpose = purpose;
	}
	public void setTransporterId(int transporterId) {
		this.transporterId = transporterId;
	}
	public void setExtendedStatus(int extendedStatus) {
		this.extendedStatus = extendedStatus;
	}
	public void setOtherVehicleId(int otherVehicleId) {
		this.otherVehicleId = otherVehicleId;
	}
	public String getMiscellaneous() {
		return miscellaneous;
	}
	public String getDriver() {
		return driver;
	}
	public String getFieldone() {
		return fieldone;
	}
	public String getFieldtwo() {
		return fieldtwo;
	}
	public String getFieldthree() {
		return fieldthree;
	}
	public String getFieldfour() {
		return fieldfour;
	}
	public String getFieldfive() {
		return fieldfive;
	}
	public String getFieldsix() {
		return fieldsix;
	}
	public String getFieldseven() {
		return fieldseven;
	}
	public String getFieldeight() {
		return fieldeight;
	}
	public String getFieldnine() {
		return fieldnine;
	}
	public String getFieldten() {
		return fieldten;
	}
	public String getFieldeleven() {
		return fieldeleven;
	}
	public String getFieldtwelve() {
		return fieldtwelve;
	}
	public String getFieldthirteen() {
		return fieldthirteen;
	}
	public String getFieldfourteen() {
		return fieldfourteen;
	}
	public int getMake() {
		return make;
	}
	public int getModel() {
		return model;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setMiscellaneous(String miscellaneous) {
		this.miscellaneous = miscellaneous;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public void setFieldone(String fieldone) {
		this.fieldone = fieldone;
	}
	public void setFieldtwo(String fieldtwo) {
		this.fieldtwo = fieldtwo;
	}
	public void setFieldthree(String fieldthree) {
		this.fieldthree = fieldthree;
	}
	public void setFieldfour(String fieldfour) {
		this.fieldfour = fieldfour;
	}
	public void setFieldfive(String fieldfive) {
		this.fieldfive = fieldfive;
	}
	public void setFieldsix(String fieldsix) {
		this.fieldsix = fieldsix;
	}
	public void setFieldseven(String fieldseven) {
		this.fieldseven = fieldseven;
	}
	public void setFieldeight(String fieldeight) {
		this.fieldeight = fieldeight;
	}
	public void setFieldnine(String fieldnine) {
		this.fieldnine = fieldnine;
	}
	public void setFieldten(String fieldten) {
		this.fieldten = fieldten;
	}
	public void setFieldeleven(String fieldeleven) {
		this.fieldeleven = fieldeleven;
	}
	public void setFieldtwelve(String fieldtwelve) {
		this.fieldtwelve = fieldtwelve;
	}
	public void setFieldthirteen(String fieldthirteen) {
		this.fieldthirteen = fieldthirteen;
	}
	public void setFieldfourteen(String fieldfourteen) {
		this.fieldfourteen = fieldfourteen;
	}
	public void setMake(int make) {
		this.make = make;
	}
	public void setModel(int model) {
		this.model = model;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getCause() {
		return cause;
	}
	public String getLastComment() {
		return lastComment;
	}
	public String getLastCommentlocation() {
		return lastCommentlocation;
	}
	public long getLastCommentTime() {
		return lastCommentTime;
	}
	public long getNextFollowTime() {
		return nextFollowTime;
	}
	public String getCommentUser() {
		return commentUser;
	}
	public void setCause(int cause) {
		this.cause = cause;
	}
	public void setLastComment(String lastComment) {
		this.lastComment = lastComment;
	}
	public void setLastCommentlocation(String lastCommentlocation) {
		this.lastCommentlocation = lastCommentlocation;
	}
	public void setLastCommentTime(long lastCommentTime) {
		this.lastCommentTime = lastCommentTime;
	}
	public void setNextFollowTime(long nextFollowTime) {
		this.nextFollowTime = nextFollowTime;
	}
	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}
	public static void main(String[] a) throws GenericException{
		boolean destroyIt = false;
		Connection conn = null;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(22586, conn);
			VehicleExtendedInfo vehicle = VehicleExtendedInfo.getVehicleExtended(conn, 22586);
			System.out.println("Data :"+vehicle.getLastCommentlocation());
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}
	}
	public int getDriverDetailId() {
		return driverDetailId;
	}
	public void setDriverDetailId(int driverDetailId) {
		this.driverDetailId = driverDetailId;
	}
}

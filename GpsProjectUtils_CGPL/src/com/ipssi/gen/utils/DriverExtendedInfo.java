package com.ipssi.gen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.exception.GenericException;


public class DriverExtendedInfo{
	int id;
	int portNodeId;
	String driverUID;
	String driverName;
	int status;
	String driverDLNumber;
	String driverMobileOne;
	String driverMobileTwo;
	String driverAddressOne;
	String driverAddressTwo;
	String driverInsuranceOne;
	String driverInsuranceTwo;
	long insuranceOneDate;
	long insuranceTwoDate;
	long updatedOn;
	long dlDate;
	String info1;
	String info2;
	String info3;
	String info4;
	int vehicleIdOne;
	int vehicleIdTwo;
	String providedUID;
	long driverDob;
	long dlExpiryDate;
	int ddtTraining;
	long ddtTrainingDate;
	long ddtTrainingExpiryDate;
	int madical;
	long medicalDate;
	String driverStdName;
	
    private static Object syscDataVariable = new Object();
	private static ConcurrentHashMap<Integer, DriverExtendedInfo> driverExtMap = null;
	private static ConcurrentHashMap<Integer, Integer> vehicleDriverMap = null;
	public static void clear(){
		synchronized(syscDataVariable) {
			if(driverExtMap != null)
				driverExtMap.clear();
			driverExtMap = null;
			if(vehicleDriverMap != null)
				vehicleDriverMap.clear();
			vehicleDriverMap = null;
		}
	}
	public static DriverExtendedInfo getDriverExtendedByVehicleId(Connection conn, int vehicleId){
		int driverId = Misc.getUndefInt();
		if(driverExtMap == null)
			loadDriverExtended(conn,Misc.getUndefInt());
		if(vehicleDriverMap != null)
			driverId = vehicleDriverMap.get(vehicleId) != null ? vehicleDriverMap.get(vehicleId) : Misc.getUndefInt();
		return driverExtMap != null ? driverExtMap.get(driverId) : null;
	}
	public static DriverExtendedInfo getDriverExtended(Connection conn, int driverId){
		if(driverExtMap == null)
			loadDriverExtended(conn,Misc.getUndefInt());
		return driverExtMap != null ? driverExtMap.get(driverId) : null;
	}
	private static void loadDriverExtended(Connection conn,int driverId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		DriverExtendedInfo driver = null;
		try{
			String query = "" +
					" select id, org_id, driver_uid, driver_name, status, driver_dl_number," +
					" driver_mobile_one,driver_mobile_two,driver_address_one,driver_address_two," +
					" driver_insurance_one,driver_insurance_two,insurance_one_date,insurance_two_date," +
					" updated_on,dl_date,info1,info2,info3,info4,vehicle_id_1,vehicle_id_2,provided_uid," +
					" driver_dob,dl_expiry_date,ddt_training,ddt_training_date,ddt_training_expiry_date," +
					" madical,medical_date,driver_std_name from  driver_details" +
					" ";
			if(!Misc.isUndef(driverId))
				query += " where id=" + driverId;
			ps = conn.prepareStatement(query);
			synchronized(syscDataVariable) {
				rs = ps.executeQuery();
				while(rs.next()){
					driver = new DriverExtendedInfo();
					driver.setId(Misc.getRsetInt(rs, "id"));
					driver.setPortNodeId(Misc.getRsetInt(rs, "org_id"));
					driver.setDriverUID(rs.getString("driver_uid"));
					driver.setDriverName(rs.getString("driver_name"));
					driver.setStatus(Misc.getRsetInt(rs, "status"));
					driver.setDriverDLNumber(rs.getString("driver_dl_number"));
					driver.setDriverMobileOne(rs.getString("driver_mobile_one"));
					driver.setDriverMobileTwo(rs.getString("driver_mobile_two"));
					driver.setDriverAddressOne(rs.getString("driver_address_one"));
					driver.setDriverAddressTwo(rs.getString("driver_address_two"));
					driver.setDriverInsuranceOne(rs.getString("driver_insurance_one"));
					driver.setDriverInsuranceTwo(rs.getString("driver_insurance_two"));
					driver.setInsuranceOneDate(Misc.getDateInLong(rs, "insurance_one_date"));
					driver.setInsuranceTwoDate(Misc.getDateInLong(rs, "insurance_two_date"));
					driver.setUpdatedOn(Misc.getDateInLong(rs, "updated_on"));
					driver.setDlDate(Misc.getDateInLong(rs, "dl_date"));
					driver.setInfo1(rs.getString("info1"));
					driver.setInfo2(rs.getString("info2"));
					driver.setInfo3(rs.getString("info3"));
					driver.setInfo4(rs.getString("info4"));
					int vehicleIdOne = Misc.getRsetInt(rs, "vehicle_id_1");
					int vehicleIdTwo = Misc.getRsetInt(rs, "vehicle_id_2");
					driver.setVehicleIdOne(vehicleIdOne);
					driver.setVehicleIdTwo(vehicleIdTwo);
					
					driver.setDriverDob(Misc.getRsetInt(rs, "driver_dob"));
					driver.setProvidedUID(rs.getString("provided_uid"));
					driver.setDlExpiryDate(Misc.getDateInLong(rs, "dl_expiry_date"));
					driver.setDdtTraining(Misc.getRsetInt(rs, "ddt_training"));
					driver.setDdtTrainingDate(Misc.getDateInLong(rs, "ddt_training_date"));
					driver.setDdtTrainingExpiryDate(Misc.getDateInLong(rs, "ddt_training_expiry_date"));
					driver.setMadical(Misc.getRsetInt(rs, "madical"));
					driver.setMedicalDate(Misc.getDateInLong(rs, "medical_date"));
					driver.setDriverStdName(rs.getString("driver_std_name"));
					if (driverExtMap == null)
						driverExtMap = new ConcurrentHashMap<Integer, DriverExtendedInfo>();
					driverExtMap.put(driver.getId(), driver);
					if(!Misc.isUndef(vehicleIdOne) || !Misc.isUndef(vehicleIdOne)){
						if(vehicleDriverMap == null)
							vehicleDriverMap = new ConcurrentHashMap<Integer, Integer>();
						vehicleDriverMap.put(!Misc.isUndef(vehicleIdOne) ? vehicleIdOne : vehicleIdTwo, driver.getId());
					}
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
	public static void markDirty(Connection conn, int driverId){
		loadDriverExtended(conn, driverId);
	}
	public int getId() {
		return id;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public String getDriverUID() {
		return driverUID;
	}
	public String getDriverName() {
		return driverName;
	}
	public int getStatus() {
		return status;
	}
	public String getDriverDLNumber() {
		return driverDLNumber;
	}
	public String getDriverMobileOne() {
		return driverMobileOne;
	}
	public String getDriverMobileTwo() {
		return driverMobileTwo;
	}
	public String getDriverAddressOne() {
		return driverAddressOne;
	}
	public String getDriverAddressTwo() {
		return driverAddressTwo;
	}
	public String getDriverInsuranceOne() {
		return driverInsuranceOne;
	}
	public String getDriverInsuranceTwo() {
		return driverInsuranceTwo;
	}
	public long getInsuranceOneDate() {
		return insuranceOneDate;
	}
	public long getInsuranceTwoDate() {
		return insuranceTwoDate;
	}
	public long getUpdatedOn() {
		return updatedOn;
	}
	public long getDlDate() {
		return dlDate;
	}
	public String getInfo1() {
		return info1;
	}
	public String getInfo2() {
		return info2;
	}
	public String getInfo3() {
		return info3;
	}
	public String getInfo4() {
		return info4;
	}
	public int getVehicleIdOne() {
		return vehicleIdOne;
	}
	public int getVehicleIdTwo() {
		return vehicleIdTwo;
	}
	public String getProvidedUID() {
		return providedUID;
	}
	public long getDriverDob() {
		return driverDob;
	}
	public long getDlExpiryDate() {
		return dlExpiryDate;
	}
	public int getDdtTraining() {
		return ddtTraining;
	}
	public long getDdtTrainingDate() {
		return ddtTrainingDate;
	}
	public long getDdtTrainingExpiryDate() {
		return ddtTrainingExpiryDate;
	}
	public int getMadical() {
		return madical;
	}
	public long getMedicalDate() {
		return medicalDate;
	}
	public String getDriverStdName() {
		return driverStdName;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public void setDriverUID(String string) {
		this.driverUID = string;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setDriverDLNumber(String driverDLNumber) {
		this.driverDLNumber = driverDLNumber;
	}
	public void setDriverMobileOne(String driverMobileOne) {
		this.driverMobileOne = driverMobileOne;
	}
	public void setDriverMobileTwo(String driverMobileTwo) {
		this.driverMobileTwo = driverMobileTwo;
	}
	public void setDriverAddressOne(String driverAddressOne) {
		this.driverAddressOne = driverAddressOne;
	}
	public void setDriverAddressTwo(String driverAddressTwo) {
		this.driverAddressTwo = driverAddressTwo;
	}
	public void setDriverInsuranceOne(String driverInsuranceOne) {
		this.driverInsuranceOne = driverInsuranceOne;
	}
	public void setDriverInsuranceTwo(String driverInsuranceTwo) {
		this.driverInsuranceTwo = driverInsuranceTwo;
	}
	public void setInsuranceOneDate(long insuranceOneDate) {
		this.insuranceOneDate = insuranceOneDate;
	}
	public void setInsuranceTwoDate(long insuranceTwoDate) {
		this.insuranceTwoDate = insuranceTwoDate;
	}
	public void setUpdatedOn(long updatedOn) {
		this.updatedOn = updatedOn;
	}
	public void setDlDate(long dlDate) {
		this.dlDate = dlDate;
	}
	public void setInfo1(String info1) {
		this.info1 = info1;
	}
	public void setInfo2(String info2) {
		this.info2 = info2;
	}
	public void setInfo3(String info3) {
		this.info3 = info3;
	}
	public void setInfo4(String info4) {
		this.info4 = info4;
	}
	public void setVehicleIdOne(int i) {
		this.vehicleIdOne = i;
	}
	public void setVehicleIdTwo(int i) {
		this.vehicleIdTwo = i;
	}
	public void setProvidedUID(String providedUID) {
		this.providedUID = providedUID;
	}
	public void setDriverDob(long driverDob) {
		this.driverDob = driverDob;
	}
	public void setDlExpiryDate(long dlExpiryDate) {
		this.dlExpiryDate = dlExpiryDate;
	}
	public void setDdtTraining(int ddtTraining) {
		this.ddtTraining = ddtTraining;
	}
	public void setDdtTrainingDate(long ddtTrainingDate) {
		this.ddtTrainingDate = ddtTrainingDate;
	}
	public void setDdtTrainingExpiryDate(long ddtTrainingExpiryDate) {
		this.ddtTrainingExpiryDate = ddtTrainingExpiryDate;
	}
	public void setMadical(int madical) {
		this.madical = madical;
	}
	public void setMedicalDate(long medicalDate) {
		this.medicalDate = medicalDate;
	}
	public void setDriverStdName(String driverStdName) {
		this.driverStdName = driverStdName;
	}
	public static void main(String[] a) throws GenericException{
		Connection conn = null;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			DriverExtendedInfo driver = DriverExtendedInfo.getDriverExtendedByVehicleId(conn, 19938);
			System.out.println(driver.getDriverName());
			DriverExtendedInfo.clear();
			driver = DriverExtendedInfo.getDriverExtendedByVehicleId(conn, 19938);
			System.out.println(driver.getDriverName());
		}catch(Exception ex){
			ex.printStackTrace();
			
		}finally{
			DBConnectionPool.returnConnectionToPoolNonWeb(conn);
		}
	}
}
package com.ipssi.tracker.drivers;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.ColumnMappingHelper;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.exception.ExceptionMessages;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.ApplicationConstants;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.drivers.DriverCoreBean;
import com.ipssi.tracker.drivers.DriverSkillsBean;


public class DriverDetailsDao {

	public boolean insertDriverDetails(Connection conn, DriverCoreBean driverDetailsBean) throws GenericException{
		try{
			boolean insertsuccess = false;
			int iHit = 0;
			Timestamp sysDate = new Timestamp((new Date()).getTime());
			PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.INSERT_DRIVER_DETAILS);
			
			Misc.setParamInt(ps,driverDetailsBean.getOrgId(),1);
			ps.setString(2,driverDetailsBean.getDriverUID());
			ps.setString(3, driverDetailsBean.getName());
			ps.setString(4,driverDetailsBean.getDLNumber());
			ps.setString(5,driverDetailsBean.getDriverMobileOne());
			ps.setString(6,driverDetailsBean.getDriverMobileTwo());
			ps.setString(7, driverDetailsBean.getDriverAddressOne());
			ps.setString(8, driverDetailsBean.getDriverAddressTwo());
			ps.setString(9, driverDetailsBean.getDriverInsuranceOne());
			ps.setString(10, driverDetailsBean.getDriverInsuranceTwo());
			ps.setTimestamp(11, Misc.utilToSqlDate(driverDetailsBean.getInsuranceOneDate()));
			ps.setTimestamp(12, Misc.utilToSqlDate(driverDetailsBean.getInsuranceTwoDate()));
			ps.setTimestamp(13, sysDate);
			ps.setTimestamp(14, Misc.utilToSqlDate(driverDetailsBean.getDlDate()));
			ps.setString(15, driverDetailsBean.getInfo1());
			ps.setString(16, driverDetailsBean.getInfo2());
			ps.setString(17, driverDetailsBean.getInfo3());
			ps.setString(18, driverDetailsBean.getInfo4());
			ps.setInt(19, driverDetailsBean.getVehicleId1());
			ps.setInt(20, driverDetailsBean.getVehicleId2());
			iHit = ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				driverDetailsBean.setId(rs.getInt(1));
			}
			rs.close();
			ps.close();
			insertDriverSkills(conn,driverDetailsBean, sysDate);
			if (iHit > 0)
				insertsuccess = true;
			return insertsuccess;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}
	}
	
	public void insertDriverSkills(Connection conn, DriverCoreBean driverDetailsBean, Timestamp sysDate) throws GenericException {
		try{
			PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.DELETE_DRIVER_SKILLS);
			
			ps.setInt(1, driverDetailsBean.getId());
			ps.execute();
			ps.close();
			ArrayList<DriverSkillsBean> driverSkillsList = driverDetailsBean.getDriverSkillsList();
			ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.INSERT_DRIVER_SKILLS);
			System.out.println("DriverDetailDao.inserDriverSkills() :: driverSkillsList.size() :: "	+ driverSkillsList.size());
			
				for (Iterator<DriverSkillsBean> iterator = driverSkillsList.iterator(); iterator.hasNext();) {
					DriverSkillsBean driverSkillsListBean = (DriverSkillsBean) iterator.next();
					Misc.setParamInt(ps, driverDetailsBean.getId(), 1);
					Misc.setParamInt(ps, driverSkillsListBean.getKey(), 2);
					Misc.setParamInt(ps, driverSkillsListBean.getValue(), 3);
					ps.setString(4,driverSkillsListBean.getValueName());
					Misc.setParamInt(ps, driverSkillsListBean.getFactor1(), 5);
					Misc.setParamInt(ps, driverSkillsListBean.getFactor1(), 6);
					
					ps.addBatch();				
				}
				if (ps.executeBatch().length != driverSkillsList.size()) {
					ps.close();
					throw new GenericException(
							"No of records inserted is not equals to maintTicketDetailList size.");
				}
				ps.close();			
		}
		catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}
		
	}
	
	public void updateDriverDetails(Connection conn, DriverCoreBean driverDetailsBean) throws GenericException{
		try{
			PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.UPDATE_DRIVER_DETAILS);
			Timestamp sysDate = new Timestamp((new Date()).getTime());
			Misc.setParamInt(ps,driverDetailsBean.getOrgId(),1);
			ps.setString(2,driverDetailsBean.getDriverUID());
			ps.setString(3, driverDetailsBean.getName());
			ps.setString(4,driverDetailsBean.getDLNumber());
			ps.setString(5,driverDetailsBean.getDriverMobileOne());
			ps.setString(6,driverDetailsBean.getDriverMobileTwo());
			ps.setString(7, driverDetailsBean.getDriverAddressOne());
			ps.setString(8, driverDetailsBean.getDriverAddressTwo());
			ps.setString(9, driverDetailsBean.getDriverInsuranceOne());
			ps.setString(10, driverDetailsBean.getDriverInsuranceTwo());
			ps.setTimestamp(11, Misc.utilToSqlDate(driverDetailsBean.getInsuranceOneDate()));
			ps.setTimestamp(12, Misc.utilToSqlDate(driverDetailsBean.getInsuranceTwoDate()));
			ps.setTimestamp(13, sysDate);
			ps.setTimestamp(14, Misc.utilToSqlDate(driverDetailsBean.getDlDate()));
			ps.setString(15, driverDetailsBean.getInfo1());
			ps.setString(16, driverDetailsBean.getInfo2());
			ps.setString(17, driverDetailsBean.getInfo3());
			ps.setString(18, driverDetailsBean.getInfo4());
			ps.setInt(19, driverDetailsBean.getVehicleId1());
			ps.setInt(20, driverDetailsBean.getVehicleId2());
			Misc.setParamInt(ps,driverDetailsBean.getId(),21);
			ps.executeUpdate();
			ps.close();
			insertDriverSkills(conn, driverDetailsBean, sysDate);	
		}
		catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}
	}
	
	public DriverCoreBean getDriverDetailsById(Connection conn, int paramAsInt) throws GenericException{
		
		try{
			PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.FETCH_DRIVER_DETAILS_BYID);
	
			ResultSet rs =null;
			DriverCoreBean driverDetailsBean = null;
			Misc.setParamInt(ps,ApplicationConstants.ACTIVE,1);
			Misc.setParamInt(ps,paramAsInt,2);
			rs = ps.executeQuery();
			while(rs.next()){
				driverDetailsBean = new DriverCoreBean();
				driverDetailsBean.setId(paramAsInt);
				driverDetailsBean.setName(rs.getString("driver_name"));
				driverDetailsBean.setOrgId(Misc.getRsetInt(rs,"org_id"));
				driverDetailsBean.setDriverUID(rs.getString("driver_uid"));
				driverDetailsBean.setDLNumber(rs.getString("driver_dl_number"));
				driverDetailsBean.setDriverMobileOne(rs.getString("driver_mobile_one"));
				driverDetailsBean.setDriverMobileTwo(rs.getString("driver_mobile_two"));
				driverDetailsBean.setInsuranceOneDate(rs.getTimestamp("insurance_one_date"));
				driverDetailsBean.setInsuranceTwoDate(rs.getTimestamp("insurance_two_date"));
				driverDetailsBean.setDriverAddressOne(rs.getString("driver_address_one"));
				driverDetailsBean.setDriverAddressTwo(rs.getString("driver_address_two"));	
				driverDetailsBean.setDriverInsuranceOne(rs.getString("driver_insurance_one"));
				driverDetailsBean.setDriverInsuranceTwo(rs.getString("driver_insurance_two"));
				driverDetailsBean.setDlDate(Misc.sqlToUtilDate(rs.getTimestamp("dl_date")));
				driverDetailsBean.setInfo1(rs.getString("info1"));
				driverDetailsBean.setInfo2(rs.getString("info2"));
				driverDetailsBean.setInfo3(rs.getString("info3"));
				driverDetailsBean.setInfo4(rs.getString("info4"));
				driverDetailsBean.setVehicleId1(Misc.getRsetInt(rs, "vehicle_id_1"));
				driverDetailsBean.setVehicleId2(Misc.getRsetInt(rs, "vehicle_id_2"));
				driverDetailsBean.setVehicleId1Name(Misc.getRsetString(rs, "veh1_name"));
				driverDetailsBean.setVehicleId2Name(Misc.getRsetString(rs, "veh2_name"));

				driverDetailsBean.setDriverSkillsList(getDriverSkills(conn, driverDetailsBean));
			}
			rs.close();
			ps.close();
			return driverDetailsBean;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}
	}

	private static ArrayList<DriverSkillsBean> getDriverSkills(Connection conn, DriverCoreBean driverDetailsBean) throws GenericException{
		try{
		PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.FETCH_DRIVER_SKILLS);
		
		ResultSet rs =null;
		ArrayList<DriverSkillsBean> driverSkillsBeanList = null;
		ps.setInt(1,driverDetailsBean.getId());
		rs = ps.executeQuery();
		if(!Common.isNull(rs)){
			driverSkillsBeanList = new ArrayList<DriverSkillsBean>();
			DriverSkillsBean driverSkillsBean = null; 
			while(rs.next()){
				driverSkillsBean = new DriverSkillsBean();
				driverSkillsBean.setKey(Misc.getRsetInt(rs, "skill_type"));
				driverSkillsBean.setValue(Misc.getRsetInt(rs, "skill_level"));
				driverSkillsBean.setValueName(rs.getString("skill_name"));
				driverSkillsBean.setFactor1(Misc.getRsetInt(rs, "factor1"));
				driverSkillsBean.setFactor2(Misc.getRsetInt(rs, "factor2"));
				driverSkillsBeanList.add(driverSkillsBean);
			}
		}
		rs.close();
		ps.close();
		
		return driverSkillsBeanList;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}
	}

	public static ArrayList<DriverCoreBean> getDriverDataByOrg(SessionManager session) throws GenericException{
		Connection conn = session.getConnection();
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		return getDriverDataByOrg(conn, v123);
	}
	public static ArrayList<DriverCoreBean> getDriverDataByOrg(Connection conn, int v123) throws GenericException{
		String fetchDrivers = DBQueries.DRIVERDETAILS.FETCH_DRIVER_DETAILS_BYORG;
		//String fetchAllDrivers = DBQueries.DRIVERDETAILS.FETCH_ALL_DRIVERS;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ArrayList<DriverCoreBean> driverDetailsBeanList = null;
		try {
			ps = conn.prepareStatement(fetchDrivers);
			Misc.setParamInt(ps,ApplicationConstants.ACTIVE,2);
			Misc.setParamInt(ps,v123,1);
			/*
			if(v123==2){			
				ps = conn.prepareStatement(fetchAllDrivers);
				Misc.setParamInt(ps,ApplicationConstants.ACTIVE,1);
			}
			else{
				ps = conn.prepareStatement(fetchDrivers);
				Misc.setParamInt(ps,ApplicationConstants.ACTIVE,1);
				Misc.setParamInt(ps,v123,2);
			}*/
			rs = ps.executeQuery();
			if(!Common.isNull(rs)){
				driverDetailsBeanList = new ArrayList<DriverCoreBean>();
				DriverCoreBean driverDetailsBean = null; 
				while(rs.next()){
					driverDetailsBean = new DriverCoreBean();
					driverDetailsBean.setId(Misc.getRsetInt(rs,"id"));
					driverDetailsBean.setOrgId(Misc.getRsetInt(rs, "org_id"));
					driverDetailsBean.setName(rs.getString("driver_name"));
					driverDetailsBean.setDriverUID(rs.getString("driver_uid"));
					driverDetailsBean.setDLNumber(rs.getString("driver_dl_number"));
					driverDetailsBean.setDriverMobileOne(rs.getString("driver_mobile_one"));
					driverDetailsBean.setDriverMobileTwo(rs.getString("driver_mobile_two"));
					driverDetailsBean.setInsuranceOneDate(rs.getTimestamp("insurance_one_date"));
					driverDetailsBean.setInsuranceTwoDate(rs.getTimestamp("insurance_two_date"));
					driverDetailsBean.setDriverAddressOne(rs.getString("driver_address_one"));
					driverDetailsBean.setDriverAddressTwo(rs.getString("driver_address_two"));	
					driverDetailsBean.setDriverInsuranceOne(rs.getString("driver_insurance_one"));
					driverDetailsBean.setDriverInsuranceTwo(rs.getString("driver_insurance_two"));
					driverDetailsBean.setDriverSkillsList(getDriverSkills(conn, driverDetailsBean));
					driverDetailsBean.setDlDate(Misc.sqlToUtilDate(rs.getTimestamp("dl_date")));
					driverDetailsBean.setInfo1(rs.getString("info1"));
					driverDetailsBean.setInfo2(rs.getString("info2"));
					driverDetailsBean.setInfo3(rs.getString("info3"));
					driverDetailsBean.setInfo4(rs.getString("info4"));
					driverDetailsBean.setVehicleId1(Misc.getRsetInt(rs, "vehicle_id_1"));
					driverDetailsBean.setVehicleId2(Misc.getRsetInt(rs, "vehicle_id_2"));
					driverDetailsBean.setVehicleId1Name(Misc.getRsetString(rs, "veh1_name"));
					driverDetailsBean.setVehicleId2Name(Misc.getRsetString(rs, "veh2_name"));

					driverDetailsBeanList.add(driverDetailsBean);
				}
			}
			rs.close();
			ps.close();
			return driverDetailsBeanList;		
		}catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}		
	}

	public void deleteDriverDetails(Connection conn, int[] checkDelete) throws GenericException{
		String deleteDriver = DBQueries.DRIVERDETAILS.DELETE_DRIVER_DETAILS;
		PreparedStatement ps = null;
		
		try{
			if(checkDelete == null || checkDelete.length==0) ;
			else {
				for(int i=0;i<checkDelete.length;i++){
					ps = conn.prepareStatement(deleteDriver);
					ps.setInt(1, ApplicationConstants.DELETED);
					ps.setInt(2, checkDelete[i]);
					ps.execute();
				}
				ps.close();
			}
		}catch(Exception e){
			throw new GenericException(e);
		}
		
		
	}

}

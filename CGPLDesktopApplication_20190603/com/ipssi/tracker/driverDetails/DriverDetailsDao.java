package com.ipssi.tracker.driverDetails;
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
import com.ipssi.tracker.driverDetails.DriverDetailsBean;
import com.ipssi.tracker.driverDetails.DriverSkillsBean;


public class DriverDetailsDao {

	public boolean insertDriverDetails(Connection conn, DriverDetailsBean driverDetailsBean) throws GenericException{
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
	
	public void insertDriverSkills(Connection conn, DriverDetailsBean driverDetailsBean, Timestamp sysDate) throws GenericException {
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
	
	public void updateDriverDetails(Connection conn, DriverDetailsBean driverDetailsBean) throws GenericException{
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
			Misc.setParamInt(ps,driverDetailsBean.getId(),14);
			ps.executeUpdate();
			ps.close();
			insertDriverSkills(conn, driverDetailsBean, sysDate);	
		}
		catch(Exception e){
			e.printStackTrace();
			throw new GenericException(e);
		}
	}
	
	public DriverDetailsBean getDriverDetailsById(Connection conn, int paramAsInt) throws GenericException{
		
		try{
			PreparedStatement ps = conn.prepareStatement(DBQueries.DRIVERDETAILS.FETCH_DRIVER_DETAILS_BYID);
	
			ResultSet rs =null;
			DriverDetailsBean driverDetailsBean = null;
			Misc.setParamInt(ps,ApplicationConstants.ACTIVE,1);
			Misc.setParamInt(ps,paramAsInt,2);
			rs = ps.executeQuery();
			while(rs.next()){
				driverDetailsBean = new DriverDetailsBean();
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

	private static ArrayList<DriverSkillsBean> getDriverSkills(Connection conn, DriverDetailsBean driverDetailsBean) throws GenericException{
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

	public ArrayList<DriverDetailsBean> getDriverDataByOrg(SessionManager session) throws GenericException{
		Connection conn = session.getConnection();
		int v123 = Misc.getParamAsInt(session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
		return getDriverDataByOrg(conn, v123);
	}
	
	public static ArrayList<DriverDetailsBean> getDriverDataByOrg(Connection conn, int v123) throws GenericException{
		
		String fetchDrivers = DBQueries.DRIVERDETAILS.FETCH_DRIVER_DETAILS_BYORG;
		//String fetchAllDrivers = DBQueries.DRIVERDETAILS.FETCH_ALL_DRIVERS;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ArrayList<DriverDetailsBean> driverDetailsBeanList = null;
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
				driverDetailsBeanList = new ArrayList<DriverDetailsBean>();
				DriverDetailsBean driverDetailsBean = null; 
				while(rs.next()){
					driverDetailsBean = new DriverDetailsBean();
					driverDetailsBean.setId(Misc.getRsetInt(rs,"id"));
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

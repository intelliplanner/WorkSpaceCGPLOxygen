package com.ipssi.mobile.challan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.ipssi.common.ds.trip.ChallanUpdHelper;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.mobile.challan.ChallanInfoBean.DeliveryInfo;
import com.ipssi.mobile.challan.ChallanInfoBean.MaterialInfo;
import com.ipssi.multi.MultiChallanAction;

public class ChallanInfoDao {
	private static final String INSERT_CHALLAN = "insert into challan_details (vehicle_id,truck_no,from_location,to_location,challan_date,to_station_id,from_station_id) values (?,?,?,?,?,?,?)";
	private static final String UPDATE_CHALLAN = "update challan_details set vehicle_id=?,truck_no=?,from_location=?,to_location=?,challan_date=?,from_station_id=?,to_station_id=? where id=?";
	private static final String INSERT_CHALLAN_ITEM = "insert into challan_dispatch_item (challan_id,id,code,dispatch_qty,received_qty,dispatch_notes,recieved_notes) values (?,?,?,?,?,?,?)";
	private static final String UPDATE_CHALLAN_ITEM = "update challan_dispatch_item set challan_id=?,id=?,code=?,dispatch_qty=?,received_qty=?,dispatch_notes=?,recieved_notes=? where row_id=?";
	private static final String SELECT_CHALLAN = "select challan_details.id,vehicle_id,truck_no,from_location,to_location,challan_date,to_station_id,from_station_id from challan_details left outer join vehicle on (vehicle.id=challan_details.vehicle_id) where vehicle.customer_id=? ";
	private static final String SELECT_CHALLAN_ITEM = "select row_id,id,code,dispatch_qty,received_qty,dispatch_notes,recieved_notes from challan_dispatch_item where challan_id=? ";
	private static final String SELECT_VEHICLE = "select id,vehicle.name,cast(latitude as decimal(10,6)) lat,cast(longitude as decimal(10,6)) lon from vehicle join current_data on (current_data.vehicle_id=vehicle.id) where attribute_id=0 and vehicle.customer_id=?";
	private static final String SELECT_OPSTATION = "select id,name from opstation_mapping join op_station on (opstation_mapping.op_station_id=op_station.id) where type=? and port_node_id=? ";
	
	private static final int TYPE_LOAD = 1;
	private static final int TYPE_UNLOAD = 2;
	
	public static void insertChallan(Connection conn,ChallanInfoBean challan){
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		ArrayList <Integer> idsList = new ArrayList<Integer>();
		java.util.Date challanDate = null;
		java.util.Date prevDate = null;
		int secIncr = 0;
		try{
			if(challan != null && challan.getDeliveries() != null && challan.getDeliveries().size() > 0 
					&& challan.getVehicle() != null && !Misc.isUndef(challan.getVehicle().getId()) && challan.getSource() != null && !Misc.isUndef(challan.getSource().getId())){
				for(DeliveryInfo delivery : challan.getDeliveries()){
					if(delivery == null || delivery.getDestination() == null || Misc.isUndef(delivery.getDestination().getId()))
						continue;
					if(ps != null){
						ps.clearParameters();
						ps.clearBatch();
						ps.close();
						ps = null;
					}
					challanDate = challan.getDispatchDate();
					if (challanDate == null)
						challanDate = new java.util.Date();
					if (prevDate != null && prevDate.equals(challanDate)) {
						challanDate.setSeconds(challanDate.getSeconds()+(++secIncr));
					}
					else {
						prevDate = challanDate;
					}
				    ps = conn.prepareStatement(INSERT_CHALLAN);
					Misc.setParamInt(ps, challan.getVehicle().getId(), 1);
					ps.setString(2,challan.getVehicle().getName());
					ps.setString(3,challan.getSource().getName());
					ps.setString(4,delivery.getDestination().getName());
					ps.setTimestamp(5, Misc.utilToSqlDate(challanDate.getTime()));
					
					Misc.setParamInt(ps, delivery.getDestination().getId(), 6);
					Misc.setParamInt(ps, challan.getSource().getId(), 7);
					ps.executeUpdate();
					rs = ps.getGeneratedKeys();
					if (rs.next()){
						int id = rs.getInt(1);
						idsList.add(id);
						delivery.setChallanId(rs.getInt(1));
					}
					if(rs != null)
						rs.close();
					updateChallanDeliveryForGRNo(conn, idsList);
					insertChallanDisptachItem(conn, delivery);
					ChallanUpdHelper.postProcessChallanCreate(conn, idsList);
				}
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void updateChallanDeliveryForGRNo(Connection conn,ArrayList <Integer> idsList){
		PreparedStatement ps = null;
		ResultSet rs = null;
		int firstId = 0;
		try{
			if(idsList != null && idsList.size() > 0 ){
				for(Integer ids : idsList){
					if(ids == null || Misc.isUndef(ids.intValue()))
						continue;
					if(firstId == 0)
						firstId = ids;
				    ps = conn.prepareStatement("update challan_details set gr_no_=? where id=?" );
				    ps.setString(1,"GP_"+firstId);
				    Misc.setParamInt(ps, ids, 2);
					ps.executeUpdate();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void insertChallanDisptachItem(Connection conn,DeliveryInfo deliveryInfo){
		PreparedStatement ps = null;
		try{
			if(deliveryInfo != null && !Misc.isUndef(deliveryInfo.getChallanId())){
				ps = conn.prepareStatement("delete from challan_dispatch_item where challan_id=?");
				Misc.setParamInt(ps, deliveryInfo.getChallanId(), 1);
				ps.executeUpdate();
				for(MaterialInfo material : deliveryInfo.getItems()){
					if(material == null || Misc.isUndef(material.getId()))
						continue;
					if(ps != null){
						ps.clearParameters();
						ps.clearBatch();
						ps = null;
					}
				    ps = conn.prepareStatement(INSERT_CHALLAN_ITEM);
					Misc.setParamInt(ps, deliveryInfo.getChallanId(), 1);
					Misc.setParamInt(ps, material.getId(), 2);
					ps.setString(3,material.getCode());
					Misc.setParamDouble(ps, material.getQty(), 4);
					Misc.setParamDouble(ps, material.getRecivedQty(), 5);
					ps.setString(6,material.getDispatchNotes());
					ps.setString(7,material.getRecivedNotes());
					ps.executeUpdate();
					
				}
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void updateChallanDisptachItem(Connection conn,DeliveryInfo deliveryInfo){
		PreparedStatement ps = null;
		try{
			if(deliveryInfo != null && !Misc.isUndef(deliveryInfo.getChallanId())){
				
				for(MaterialInfo material : deliveryInfo.getItems()){
					if(material == null || Misc.isUndef(material.getId()))
						continue;
					if(ps != null){
						ps.clearParameters();
						ps.clearBatch();
						ps = null;
					}
				    ps = conn.prepareStatement(UPDATE_CHALLAN_ITEM);
					Misc.setParamInt(ps, deliveryInfo.getChallanId(), 1);
					Misc.setParamInt(ps, material.getId(), 2);
					ps.setString(3,material.getCode());
					Misc.setParamDouble(ps, material.getQty(), 4);
					Misc.setParamDouble(ps, material.getRecivedQty(), 5);
					ps.setString(6,material.getDispatchNotes());
					ps.setString(7,material.getRecivedNotes());
					Misc.setParamInt(ps, material.getRowId(), 8);
					ps.executeUpdate();
					
				}
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void updateChallan(Connection conn,ChallanInfoBean challan){
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		ArrayList <Integer> idsList = new ArrayList<Integer>();
		try{
			if(challan != null && challan.getDeliveries() != null && challan.getDeliveries().size() > 0 
					&& challan.getVehicle() != null && !Misc.isUndef(challan.getVehicle().getId()) && challan.getSource() != null && !Misc.isUndef(challan.getSource().getId())){
				for(DeliveryInfo delivery : challan.getDeliveries()){
					if(delivery == null || delivery.getDestination() == null || Misc.isUndef(delivery.getDestination().getId()))
						continue;
					if(ps != null){
						ps.clearParameters();
						ps.clearBatch();
						ps.close();
						ps = null;
					}
				    ps = conn.prepareStatement(UPDATE_CHALLAN);
					Misc.setParamInt(ps, challan.getVehicle().getId(), 1);
					ps.setString(2,challan.getVehicle().getName());
					ps.setString(3,challan.getSource().getName());
					ps.setString(4,delivery.getDestination().getName());
					ps.setTimestamp(5, new Timestamp(((challan.getDispatchDate() == null ? System.currentTimeMillis() : challan.getDispatchDate().getTime())+(900*count++))));
					Misc.setParamInt(ps, challan.getSource().getId(), 6);
					Misc.setParamInt(ps, delivery.getDestination().getId(), 7);
					Misc.setParamInt(ps, delivery.getChallanId(), 8);
					ps.executeUpdate();
					//rs = ps.getGeneratedKeys();
					/*if (rs.next()){
						delivery.setChallanId(rs.getInt(1));
					}*/
					/*if(rs != null)
						rs.close();*/
					updateChallanDisptachItem(conn, delivery);
					idsList.add(delivery.getChallanId());
				}
				ChallanUpdHelper.postProcessChallanUpdate(conn, idsList);
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static ArrayList<ChallanInfoBean> getChallan(Connection conn,int portNodeId,int challanId, int vehicleId,int destinationId){

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ChallanInfoBean> retval = null;
		DeliveryInfo delivery= null;
		ChallanInfoBean challanBean = null;
		int count = 1;
		try{
			ps = conn.prepareStatement(SELECT_CHALLAN+(Misc.isUndef(challanId) ? "" : " and challan_details.id=?" )+(Misc.isUndef(vehicleId) ? "" : " and challan_details.vehicle_id=?" )+(Misc.isUndef(destinationId) ? "" : " and challan_details.to_station_id=?" ));
			Misc.setParamInt(ps, portNodeId, count++);
			if(!Misc.isUndef(challanId))
				Misc.setParamInt(ps, challanId, count++);
			if(!Misc.isUndef(vehicleId))
				Misc.setParamInt(ps, vehicleId, count++);
			if(!Misc.isUndef(destinationId))
				Misc.setParamInt(ps, destinationId, count++);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<ChallanInfoBean>();
				challanBean = new ChallanInfoBean();
				challanBean.setDeliveries(new ArrayList<ChallanInfoBean.DeliveryInfo>());
				challanBean.setVehicle(new LocationInfo(Misc.getRsetInt(rs, "vehicle_id"),rs.getString("truck_no")));
				ArrayList<MaterialInfo> materialList = getChallanMaterialInfo(conn,challanId);
				challanBean.setSource(new LocationInfo(Misc.getRsetInt(rs, "from_station_id"),rs.getString("from_location")));
				delivery = new DeliveryInfo(new LocationInfo(Misc.getRsetInt(rs, "to_station_id"),rs.getString("to_location")));
				delivery.setChallanId(Misc.getRsetInt(rs, "id"));
				challanBean.getDeliveries().add(delivery);
				delivery.setItems(materialList);
				challanBean.setDispatchDate(Misc.getDate(rs,"challan_date"));
				retval.add(challanBean);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return retval;
	
	}
	/*public static ArrayList<ChallanInfoBean> getChallan(Connection conn,int portNodeId,int challanId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ChallanInfoBean> retval = null;
		ChallanInfoBean challanBean = null;
		try{
			ps = conn.prepareStatement(SELECT_CHALLAN+(Misc.isUndef(challanId) ? "" : " and challan_details.id=?" ));
			Misc.setParamInt(ps, portNodeId, 1);
			if(!Misc.isUndef(challanId))
				Misc.setParamInt(ps, challanId, 2);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<ChallanInfoBean>();
				challanBean = new ChallanInfoBean();
				challanBean.setVehicle(new LocationInfo(Misc.getRsetInt(rs, "vehicle_id"),rs.getString("truck_no")));
				
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}*/
	
	public static ArrayList<MaterialInfo> getChallanMaterialInfo(Connection conn,int challanId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<MaterialInfo> retval = null;
		try{
			ps = conn.prepareStatement(SELECT_CHALLAN_ITEM);
			Misc.setParamInt(ps, challanId, 1);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<MaterialInfo>();
				MaterialInfo materialInfo = new MaterialInfo(Misc.getRsetInt(rs, "id"),rs.getString("code"),rs.getDouble("dispatch_qty"),rs.getDouble("received_qty"),rs.getString("dispatch_notes"),rs.getString("recieved_notes"));
				materialInfo.setRowId(Misc.getRsetInt(rs, "row_id"));
				retval.add(materialInfo);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return retval;
	}
	
	public static ArrayList<LocationInfo> getVehicle(Connection conn,int portNodeId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<LocationInfo> retval = null;
		try{
			ps = conn.prepareStatement(SELECT_VEHICLE);
			Misc.setParamInt(ps, portNodeId, 1);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<LocationInfo>();
				LocationInfo locationInfo = new LocationInfo(Misc.getRsetInt(rs, "id"),rs.getString("name"),rs.getDouble("lat"),rs.getDouble("lon"));
				retval.add(locationInfo);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return retval;
	}
	
	public static ArrayList<LocationInfo> getMaterial(Connection conn,int portNodeId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ArrayList<LocationInfo> retval = null;
		try{
			DimInfo dim = DimInfo.getDimInfo(20451);
			ps = conn.prepareStatement(dim.m_getQuery);
			Misc.setParamInt(ps, portNodeId, 1);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<LocationInfo>();
				LocationInfo locationInfo = new LocationInfo(Misc.getRsetInt(rs, 1),rs.getString(2),0.0,0.0);
				retval.add(locationInfo);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return retval;
	}
	
	public static ArrayList<LocationInfo> getLoadUnloadLov(Connection conn,int portNodeId,boolean isLoad){
		ArrayList<LocationInfo> retval = null;
		try{
			ArrayList<MiscInner.PairIntStr> lovList = MultiChallanAction.getLoadUnloadLov(conn, portNodeId, isLoad);
			if(lovList != null && lovList.size() > 0){
				for(MiscInner.PairIntStr valPair : lovList ){
					LocationInfo locationInfo = new LocationInfo(valPair.first,valPair.second);
					if(retval == null)
						retval = new ArrayList<LocationInfo>();
					retval.add(locationInfo);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	public static ArrayList<LocationInfo> getSourceLov(Connection conn,int portNodeId){
		return getLoadUnloadLov(conn, portNodeId, true);
	}
	public static ArrayList<LocationInfo> getDestinationLov(Connection conn,int portNodeId){
		return getLoadUnloadLov(conn, portNodeId, false);
	}
	public static ArrayList<LocationInfo> getSource(Connection conn,int portNodeId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<LocationInfo> retval = null;
		try{
			ps = conn.prepareStatement(SELECT_OPSTATION);
			Misc.setParamInt(ps, TYPE_LOAD, 1);
			Misc.setParamInt(ps, portNodeId, 2);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<LocationInfo>();
				LocationInfo locationInfo = new LocationInfo(Misc.getRsetInt(rs, "id"),rs.getString("name"));
				retval.add(locationInfo);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return retval;
	}
	public static ArrayList<LocationInfo> getDestination(Connection conn,int portNodeId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<LocationInfo> retval = null;
		try{
			ps = conn.prepareStatement(SELECT_OPSTATION);
			Misc.setParamInt(ps, TYPE_UNLOAD, 1);
			Misc.setParamInt(ps, portNodeId, 2);
			rs = ps.executeQuery();
			while(rs.next()){
				if(retval == null)
					retval = new ArrayList<LocationInfo>();
				LocationInfo locationInfo = new LocationInfo(Misc.getRsetInt(rs, "id"),rs.getString("name"));
				retval.add(locationInfo);
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return retval;
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

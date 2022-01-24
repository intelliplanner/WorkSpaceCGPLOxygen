package com.ipssi.gen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class TripParams {
	private int id;
	private String name;
	private String description;
	private int portNodeId;
	private int status;
	private HashMap<Integer, Integer> intParams = new HashMap<Integer, Integer>();
	private HashMap<Integer, Double> doubleParams = new HashMap<Integer, Double>();
	private HashMap<Integer, String> stringParams = new HashMap<Integer, String>();

	public TripParams(int id, String name, String description, int portNodeId,int status) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.portNodeId = portNodeId;
		this.status = status;
	}

	public static ArrayList<TripParams> getList(Connection conn, int portNodeId, int status) throws Exception {
		try {
			ArrayList<TripParams> retval = new ArrayList<TripParams>();
			PreparedStatement ps = conn.prepareStatement("select tp.id, tp.name, tp.description, tp.port_node_id, tp.status from tripparam_profiles tp join port_nodes leaf on (leaf.id = tp.port_node_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where anc.id = ? and tp.status in (?) order by tp.id desc");
			ps.setInt(1, portNodeId);
			ps.setInt(2, status);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				TripParams params = new TripParams(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5));
				retval.add(params);
			}
			rs.close();
			ps.close();
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static TripParams getDetails(Connection conn, int id) throws Exception {
		try {
			TripParams retval = null;
			PreparedStatement ps = conn.prepareStatement("select id, name, description, port_node_id, status from tripparam_profiles where id = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				retval = new TripParams(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5));
			}
			rs.close();
			ps.close();
			if (retval == null)
				return retval;
			for (int art=0;art<3;art++) {
				ps = conn.prepareStatement("select param_id, param_val from tripparam_profile_"+(art == 0 ? "int" : art == 1 ? "double" : "string") + " where tripparam_profile_id=?");
				ps.setInt(1,id);
				rs = ps.executeQuery();
				while (rs.next()) {
					if (art == 0)
						retval.addIntParam(rs.getInt(1), Misc.getRsetInt(rs, 2));
					else if (art == 1)
						retval.addDoubleParam(rs.getInt(1),Misc.getRsetDouble(rs, 2));
					else
						retval.addStringParam(rs.getInt(1), rs.getString(2));
				}
				rs.close();
				ps.close();
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static void updateOrCreate(Connection conn, TripParams param) throws Exception {
		try {
			boolean newlyCreated = false;
			if (Misc.isUndef(param.getId())) {
				newlyCreated = true;
				PreparedStatement ps = conn.prepareStatement("insert into tripparam_profiles(name, description, port_node_id, status) values (?,?,?,?)");
				ps.setString(1, param.getName());
				ps.setString(2, param.getDescription());
				ps.setInt(3, param.getPortNodeId());
				ps.setInt(4, param.getStatus());
				ps.execute();
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					param.setId(rs.getInt(1));
				}
				rs.close();
				ps.close();
			}
			else {
				PreparedStatement ps = conn.prepareStatement("update tripparam_profiles set name=?, description=?, port_node_id=?, status=? where id=?");
				ps.setString(1, param.getName());
				ps.setString(2, param.getDescription());
				ps.setInt(3, param.getPortNodeId());
				ps.setInt(4, param.getStatus());
				ps.setInt(5, param.getId());
				ps.execute();
				ps.close();
			}
			if (!newlyCreated) {
				for (int art=0;art<3;art++) {
					PreparedStatement ps = conn.prepareStatement("delete from tripparam_profile_"+(art == 0 ? "int" : art == 1 ? "double" : "string") + " where tripparam_profile_id=?");
					ps.setInt(1,param.getId());
					ps.execute();
					ps.close();
				}
			}
			for (int art=0;art<3;art++) {
				PreparedStatement ps = conn.prepareStatement("insert into tripparam_profile_"+(art == 0 ? "int" : art == 1 ? "double" : "string") + " (tripparam_profile_id, param_id, param_val) values (?,?,?)");
				HashMap hm = art == 0 ? param.intParams : art == 1 ? param.doubleParams : param.stringParams;
				Set s = hm.entrySet();
				for (Object o : s) {
					ps.setInt(1, param.getId());
					if (art == 0) {
						Map.Entry<Integer, Integer> v= (Map.Entry<Integer, Integer>)o;
						ps.setInt(2, v.getKey());
						ps.setInt(3, v.getValue());
					}
					else if (art == 1) {
						Map.Entry<Integer, Double> v= (Map.Entry<Integer, Double>)o;
						ps.setInt(2, v.getKey());
						ps.setDouble(3, v.getValue());
						
					}
					else {
						Map.Entry<Integer, String> v= (Map.Entry<Integer, String>)o;
						ps.setInt(2, v.getKey());
						ps.setString(3, v.getValue());
					}
					ps.addBatch();
				}
				ps.executeBatch();
				ps.close();				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void addIntParam(int id, int val) {
		intParams.put(id, val);
	}
	public void addDoubleParam(int id, double val) {
		doubleParams.put(id, val);
	}
	public void addStringParam(int id, String val) {
		stringParams.put(id, val);
	}
	public Integer getIntParam(int id) {
		return intParams.get(id);
	}
	public Double getDoubleParam(int id) {
		return doubleParams.get(id);
	}
	public String getStringParam(int id) {
		return stringParams.get(id);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	

}

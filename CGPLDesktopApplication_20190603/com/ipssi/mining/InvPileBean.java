package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class InvPileBean {
	private int id;
	private int portNodeId;
	private String shortCode;
	private String name;
	private String otherDesc;
	private double longitude;
	private double latitude;
	private double width;
	private double length;
	private int status;
	private Date createDate;
	private Date closeDate;
	private Date updateDate;
	private ArrayList<Pair<Integer, Double>> materialMix;
	private int type;
	private int regionId;
	private int landmarkId;
	private int priority;
	private boolean dirty = false;
	private String landmarkName = null;
	private String regionName = null;
	private int pitId = Misc.getUndefInt();
	private double difficultyFactor = 1;
	private ArrayList<Integer> notAllowedDumperTypes = null;
	private ArrayList<Integer> notAllowedShovelTypes = null;
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public InvPileBean(int id, int portNodeId, String shortCode, String name, String otherDesc, double longitude, double latitude, double width,
			double length, int status, Date createDate, Date closeDate, Date updateDate, int type, int regionId, int landmarkId, int priority, String landmarkName, String regionName, int pitId, double difficultyFactor) {
		this.id = id;
		this.portNodeId = portNodeId;
		this.shortCode = shortCode;
		this.name = name;
		this.otherDesc = otherDesc;
		this.longitude = longitude;
		this.latitude = latitude;
		this.width = width;
		this.length = length;
		this.status = status;
		this.createDate = createDate;
		this.closeDate = closeDate;
		this.updateDate = updateDate;
		this.type = type;
		this.regionId = regionId;
		this.landmarkId = landmarkId;
		this.priority = priority;
		this.landmarkName = landmarkName;
		this.regionName = regionName;
		this.pitId = pitId;
		this.difficultyFactor = difficultyFactor;
		
	}
	
	public void addMaterial(int materialId, double mix) {
		if (materialMix == null)
			materialMix = new ArrayList<Pair<Integer, Double>>();
		boolean found = false;
		for (int i=0,is=materialMix.size(); i<is;i++) {
			if (materialMix.get(i).first == materialId) {
				found = true;
				break;
			}
		}
		if (!found)
			materialMix.add(new Pair<Integer, Double>(materialId, mix));
	}

	public void addNotAllowedVehicleType(int vehicleTypeId, int vehicleCat) {
		ArrayList<Integer> useThis = null;
		if (vehicleCat == 0) {
			if (notAllowedDumperTypes == null)
				notAllowedDumperTypes = new ArrayList<Integer>();
			useThis = notAllowedDumperTypes;
		}
		else {
			if (notAllowedShovelTypes == null)
				notAllowedShovelTypes = new ArrayList<Integer>();
			useThis = notAllowedShovelTypes;
		}
		boolean found = false;
		for (int i=0,is=useThis.size(); i<is;i++) {
			if (useThis.get(i) == vehicleTypeId) {
				found = true;
				break;
			}
		}
		if (!found)
			useThis.add(vehicleTypeId);
	}

	

	public static ArrayList<InvPileBean> getInvPiles(Connection conn, int portNodeId, ArrayList<Integer> status, int type) throws Exception {
		   try {
			   ArrayList<InvPileBean> retval = new ArrayList<InvPileBean>();
			   StringBuilder sb = new StringBuilder();

			   sb.append("select inventory_piles.port_node_id, inventory_piles.id, inventory_piles.short_code, inventory_piles.name, inventory_piles.longitude, inventory_piles.latitude, length, width, inventory_piles.status, material_id, mix, inventory_piles.create_date, inventory_piles.close_date, inventory_piles.priority, inventory_piles.updated_on, inventory_piles.other_notes, pile_type, inventory_piles.region_id, inventory_piles.landmark_id, lm.name lm_name, reg.short_code reg_name, inventory_piles.pit_id, inventory_piles.op_difficulty, inv_not_allowed_veh_types.vehicle_type_id, vehicle_types.vehicle_cat from inventory_piles left outer join landmarks lm on (lm.id = landmark_id) left outer join regions reg on (reg.id = region_id) left outer join inv_material_mix on (inv_material_mix.inventory_pile_id = inventory_piles.id) left outer join inv_not_allowed_veh_types on (inv_not_allowed_veh_types.inventory_pile_id = inventory_piles.id) left outer join vehicle_types on (vehicle_types.id = vehicle_type_id) where inventory_piles.port_node_id=? and pile_type=? ");
			   
			   if (status != null && status.size() != 0 && !status.contains(Misc.G_HACKANYVAL)) {
				   sb.append(" and inventory_piles.status in (");
				   Misc.convertInListToStr(status, sb);
				   sb.append(")");
			   }
			   sb.append(" order by create_date ");
			   PreparedStatement ps = conn.prepareStatement(sb.toString());
			   ps.setInt(1, portNodeId);
			   ps.setInt(2, type);
			   ResultSet rs = ps.executeQuery();
			   InvPileBean prev = null;
			   while (rs.next()) {
				   if (prev == null || prev.getId() != rs.getInt("id")) {
					   prev = new InvPileBean(rs.getInt("id"),rs.getInt("port_node_id"), rs.getString("short_code"), rs.getString("name"), rs.getString("other_notes"),Misc.getRsetDouble(rs, "longitude"), Misc.getRsetDouble(rs, "latitude"), Misc.getRsetDouble(rs, "width"), Misc.getRsetDouble(rs, "length"),Misc.getRsetInt(rs, "status"), Misc.sqlToUtilDate(rs.getTimestamp("create_date")), Misc.sqlToUtilDate(rs.getTimestamp("close_date")), Misc.sqlToUtilDate(rs.getTimestamp("updated_on")),  Misc.getRsetInt(rs, "pile_type"), Misc.getRsetInt(rs, "region_id"), Misc.getRsetInt(rs, "landmark_id"),rs.getInt("priority"), rs.getString("lm_name"), rs.getString("reg_name"), Misc.getRsetInt(rs, "pit_id"), Misc.getRsetDouble(rs, "op_difficulty"));
					   retval.add(prev);
				   }
				   int materialId = Misc.getRsetInt(rs, "material_id");
				   if (!Misc.isUndef(materialId))
					   prev.addMaterial(materialId, Misc.getRsetDouble(rs, "mix"));
				   int vehicleCat = Misc.getRsetInt(rs, "vehicle_cat", 0);
				   int vehicleTypeId = Misc.getRsetInt(rs, "vehicle_type_id");
				   if (!Misc.isUndef(vehicleTypeId))
					   prev.addNotAllowedVehicleType(vehicleTypeId, vehicleCat);
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
	
	public static void saveInvPiles(Connection conn, ArrayList<InvPileBean> beanList) throws Exception {
		try {
			
			PreparedStatement psUpd = conn.prepareStatement("update inventory_piles set short_code=?, name=?, longitude=?, latitude=?, length=?, width=?, " +
					"status=?,  updated_on=now(), other_notes=?, pile_type=?, region_id=?, landmark_id=?, create_date=?, close_date=?, priority=? where id=?");
			PreparedStatement psIns = conn.prepareStatement("insert into inventory_piles (short_code, name, longitude, latitude, length, width," +
					" status,  other_notes, pile_type, region_id, landmark_id, create_date, close_date, priority, port_node_id, updated_on)" +
					" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, now())");
			PreparedStatement delMix = conn.prepareStatement("delete from inv_material_mix where inventory_pile_id = ?");
			PreparedStatement insMix = conn.prepareStatement("insert into inv_material_mix (inventory_pile_id, material_id, mix) values(?,?,?)");
			PreparedStatement delType = conn.prepareStatement("delete from inv_not_allowed_veh_types where inventory_pile_id = ?");
			PreparedStatement insType = conn.prepareStatement("insert into inv_not_allowed_veh_types (inventory_pile_id, vehicle_type_id) values(?,?)");
			for (InvPileBean bean:beanList) {
				int id = bean.getId();
				if (!Misc.isUndef(id) && !bean.isDirty())
					continue;
				boolean hasLat = !Misc.isUndef(bean.getLatitude());
				boolean hasLon = !Misc.isUndef(bean.getLongitude());
				boolean hasRegion = !Misc.isUndef(bean.getRegionId());
				boolean hasLandmark = !Misc.isUndef(bean.getLandmarkId());
				
				if ((hasLat && !hasLon) || (!hasLat && hasLon) || (!hasLat && !hasLon && !hasRegion && !hasLandmark))
					continue;
				PreparedStatement ps = Misc.isUndef(id) ? psIns : psUpd;
				int colIndex = 1;
				
				ps.setString(colIndex++, bean.getShortCode());
				ps.setString(colIndex++, bean.getName());
				Misc.setParamDouble(ps, bean.getLongitude(), colIndex++);
				Misc.setParamDouble(ps, bean.getLatitude(), colIndex++);
				Misc.setParamDouble(ps, bean.getLength(), colIndex++);
				Misc.setParamDouble(ps, bean.getWidth(), colIndex++);
				Misc.setParamInt(ps, bean.getStatus(), colIndex++);
				ps.setString(colIndex++, bean.getOtherDesc());
				Misc.setParamInt(ps, bean.getType(), colIndex++);
				Misc.setParamInt(ps, bean.getRegionId(), colIndex++);
				Misc.setParamInt(ps, bean.getLandmarkId(), colIndex++);
				if(bean.getCreateDate() != null)
					Misc.setParamDate(ps, colIndex++, new java.sql.Date( bean.getCreateDate().getTime()));
				else
					ps.setNull(colIndex++, java.sql.Types.DATE);
				if(bean.getCloseDate() != null)
					Misc.setParamDate(ps, colIndex++, new java.sql.Date( bean.getCloseDate().getTime()));
				else
					ps.setNull(colIndex++, java.sql.Types.DATE);
				Misc.setParamInt(ps, bean.getPriority(), colIndex++);
				
				if (!Misc.isUndef(id)) {
					ps.setInt(colIndex++, bean.getId());
					ps.addBatch();
					delMix.setInt(1, bean.getId());
					delMix.addBatch();
					
				}
				else {
					Misc.setParamInt(ps, bean.getPortNodeId(), colIndex++);
					ps.executeUpdate();
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next()) {
						bean.setId(rs.getInt(1));
					}
					rs.close();
				}
				for (int i=0,is = bean.getMaterialMix() == null ? 0 : bean.getMaterialMix().size();i<is;i++) {
					Pair<Integer, Double> mix = bean.getMaterialMix().get(i);
					if (mix.first == null || Misc.isUndef(mix.first))
						continue;
					insMix.setInt(1, bean.getId());
					
					insMix.setInt(2, mix.first);
					Misc.setParamDouble(insMix, mix.second, 3);
					insMix.addBatch();
				}
				for (int art=0;art < 2; art++) {
					ArrayList<Integer> useThis = art == 0 ? bean.getNotAllowedDumperTypes() : bean.getNotAllowedShovelTypes();
					for (int i=0,is = useThis== null ? 0 : useThis.size();i<is;i++) {
						int vehicleTypeId = useThis.get(i);
						if (Misc.isUndef(vehicleTypeId))
							continue;
						insType.setInt(1, bean.getId());
						insType.setInt(2, vehicleTypeId);
						insMix.addBatch();
					}
				}
			}
			psUpd.executeBatch();
			psUpd.close();
			delMix.executeBatch();
			delMix.close();
			insMix.executeBatch();
			insMix.close();
			psIns.close();
			delType.executeBatch();
			insType.executeBatch();
			delType.close();
			insType.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOtherDesc() {
		return otherDesc;
	}
	public void setOtherDesc(String otherDesc) {
		this.otherDesc = otherDesc;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	public ArrayList<Pair<Integer, Double>> getMaterialMix() {
		return materialMix;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public int getLandmarkId() {
		return landmarkId;
	}

	public void setLandmarkId(int landmarkId) {
		this.landmarkId = landmarkId;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public String getLandmarkName() {
		return landmarkName;
	}

	public void setLandmarkName(String landmarkName) {
		this.landmarkName = landmarkName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public void setMaterialMix(ArrayList<Pair<Integer, Double>> materialMix) {
		this.materialMix = materialMix;
	}

	public int getPitId() {
		return pitId;
	}

	public void setPitId(int pitId) {
		this.pitId = pitId;
	}

	public double getDifficultyFactor() {
		return difficultyFactor;
	}

	public void setDifficultyFactor(double difficultyFactor) {
		this.difficultyFactor = difficultyFactor;
	}

	public ArrayList<Integer> getNotAllowedDumperTypes() {
		return notAllowedDumperTypes;
	}

	public void setNotAllowedDumperTypes(ArrayList<Integer> notAllowedDumperTypes) {
		this.notAllowedDumperTypes = notAllowedDumperTypes;
	}

	public ArrayList<Integer> getNotAllowedShovelTypes() {
		return notAllowedShovelTypes;
	}

	public void setNotAllowedShovelTypes(ArrayList<Integer> notAllowedShovelTypes) {
		this.notAllowedShovelTypes = notAllowedShovelTypes;
	}
	
}

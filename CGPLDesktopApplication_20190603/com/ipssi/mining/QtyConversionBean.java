package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;

public class QtyConversionBean {
	private int materialId;
	private int vehicleType;
	private double qty;
	
	public QtyConversionBean(int materialId, int vehicleType, double qty) {
		this.materialId = materialId;
		this.vehicleType = vehicleType;
		this.qty = qty;
	}	
	
	public static QtyConversionBean getInfor(ArrayList<QtyConversionBean> list, int materialId) {
		for (QtyConversionBean bean:list) {
			if (bean.getMaterialId() == materialId)
				return bean;
		}
		return null;
	}
	
	
	public static ArrayList<QtyConversionBean> getConversionFactor(Connection conn, int portNodeId) throws Exception {
		   try {
			   
			   ArrayList<QtyConversionBean> retval = new ArrayList<QtyConversionBean>();
			   StringBuilder sb = new StringBuilder();

			   sb.append("select material_id, vehicle_type, qty from trip_to_qty_conversion where port_node_id = ?");
			   PreparedStatement ps = conn.prepareStatement(sb.toString());
			   ps.setInt(1, portNodeId);
			   ResultSet rs = ps.executeQuery();
			   while (rs.next()) {
				   int matId = rs.getInt(1);
				   int vehType = Misc.getRsetInt(rs,2);
				   double qty = Misc.getRsetDouble(rs, 3);
				   if (Misc.isUndef(matId) || Misc.isUndef(vehType) || Misc.isUndef(qty))
					   continue;
				   retval.add(new QtyConversionBean(matId, vehType, qty));
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
	
	public static void saveConversionFactor(Connection conn, int portNodeId, ArrayList<QtyConversionBean> beanList) throws Exception {
		try {
			PreparedStatement psDel = conn.prepareStatement("delete from trip_to_qty_conversion where port_node_id = ?");
			psDel.setInt(1, portNodeId);
			psDel.execute();
			psDel.close();
			PreparedStatement psIns = conn.prepareStatement("insert into trip_to_qty_conversion (port_node_id, material_id, vehicle_type,  qty) values (?,?,?,?)");
			for (QtyConversionBean bean:beanList) {
					psIns.setInt(1, portNodeId);
					psIns.setInt(2, bean.getMaterialId());
					psIns.setInt(3, bean.getVehicleType());
					psIns.setDouble(4, bean.getQty());
					psIns.addBatch();
			}
			psIns.executeBatch();
			psIns.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}

	public int getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}
	
}

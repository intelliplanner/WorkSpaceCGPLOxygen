package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;

public class AnnProdBean {
	private int materialId;
	private ArrayList<Double> target = new ArrayList<Double>();
	public AnnProdBean(int materialId, ArrayList<Double> target) {
		this.materialId = materialId;
		this.target = target;
	}
	
	public AnnProdBean(int materialId) {
		this.materialId = materialId;
		for (int i=0,is=12; i<is; i++)
			target.add(null);
	}
	
	public void setTargetFor(int monthIndex, double amt) {
		target.set(monthIndex, amt);
	}
	public static AnnProdBean getInfor(ArrayList<AnnProdBean> list, int materialId) {
		for (AnnProdBean bean:list) {
			if (bean.getMaterialId() == materialId)
				return bean;
		}
		return null;
	}
	
	public static ArrayList<AnnProdBean> getEmptyGoal(Connection conn, int portNodeId) throws Exception {
		ArrayList<MiscInner.PairIntStr> materialList = InvPileAction.getMaterialList(conn, portNodeId);
		ArrayList<AnnProdBean> retval = new ArrayList<AnnProdBean>();
		for (MiscInner.PairIntStr entry:materialList) {
			retval.add(new AnnProdBean(entry.first));
		}
		return retval;
	}
	
	public static ArrayList<AnnProdBean> getProdGoal(Connection conn, int portNodeId, int year, boolean getNearestPrior) throws Exception {
		   try {
			   if (getNearestPrior) {
				   PreparedStatement ps = conn.prepareStatement("select max(year) from production_plan where port_node_id = ? and year <= ?");
				   ps.setInt(1, portNodeId);
				   ps.setInt(2, year);
				   ResultSet rs = ps.executeQuery();
				   int yrSeen = rs.next() ? rs.getInt(1) : Misc.getUndefInt();
				   rs.close();
				   ps.close();
				   return getProdGoal(conn, portNodeId, year, false);
			   }
			   ArrayList<AnnProdBean> retval = getEmptyGoal(conn, portNodeId);
			   StringBuilder sb = new StringBuilder();

			   sb.append("select material_id, month, qty from production_plan where port_node_id = ? and year = ?");
			   PreparedStatement ps = conn.prepareStatement(sb.toString());
			   ps.setInt(1, portNodeId);
			   ps.setInt(2, year);
			   ResultSet rs = ps.executeQuery();
			   AnnProdBean prev = null;
			   while (rs.next()) {
				   int matId = rs.getInt(1);
				   if (prev == null || prev.getMaterialId() != matId) {
					   prev = getInfor(retval, matId);
				   }
				   int mon = Misc.getRsetInt(rs, 2);
				   double qty = Misc.getRsetDouble(rs, 3);
				   if (Misc.isUndef(mon) || Misc.isUndef(qty) | prev == null)
					   continue;
				   prev.setTargetFor(mon, qty);
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
	
	public static void saveProdGoal(Connection conn, int portNodeId, int year, ArrayList<AnnProdBean> beanList) throws Exception {
		try {
			PreparedStatement psDel = conn.prepareStatement("delete from production_plan where port_node_id = ? and year = ?");
			psDel.setInt(1, portNodeId);
			psDel.setInt(2, year);
			psDel.execute();
			psDel.close();
			PreparedStatement psIns = conn.prepareStatement("insert into production_plan (port_node_id, year, material_id, month, qty) values (?,?,?,?,?)");
			for (AnnProdBean bean:beanList) {
				ArrayList<Double> qty = bean.getTarget();
				for (int j=0,js = qty.size();j<js;j++) {
					if (qty.get(j) == null || Misc.isUndef(qty.get(j)))
						continue;
					psIns.setInt(1, portNodeId);
					psIns.setInt(2, year);
					psIns.setInt(3, bean.getMaterialId());
					psIns.setInt(4, j);
					psIns.setDouble(5, qty.get(j));
					psIns.addBatch();
				}
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
	public ArrayList<Double> getTarget() {
		return target;
	}
	public void setTarget(ArrayList<Double> target) {
		this.target = target;
	}
}

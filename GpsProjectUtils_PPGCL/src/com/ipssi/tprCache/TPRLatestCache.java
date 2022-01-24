package com.ipssi.tprCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;

public class TPRLatestCache {
	private static ConcurrentHashMap<Integer, TPRLatestCache> g_latestTPRCache = new ConcurrentHashMap<Integer, TPRLatestCache>();
	private static volatile boolean g_latestInited = false;
	public static TPRLatestCache getLatest(int vehicleId) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			if (!g_latestInited) {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				load(conn);
			}
			return g_latestTPRCache.get(vehicleId);
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
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
		return null;
	}
	private int vehicleId;
	private int tprId;
	private int fromMinesId;
	private int toDestId;
	private int materialId;
	private int tprStatus;
	private long lgin;
	private long lgout;
	private long ugin;
	private long ugout;
	private long challanDate;
	private long rfChallanDate;
	private long comboStart;
	private long comboEnd;
	private String challanNo;
	private String rfChallanNo;
	private String lrNo;
	private String rfLrNo;
	private String doNumber;
	private int driverId;
	private String transporter;
	private String rfTransporter;
	private int rfMinesId;
	private double loadTare;
	private double loadGross;
	private double unloadTare;
	private double unloadGross;
	private String areaCode;
	private long loadTareTime;
	private long loadGrossTime;
	private String loadTareWb;
	private String loadGrossWb;
	private long unloadGrossTime;
	private long unloadTareTime;
	private String unloadTareWb;
	private String unloadGrossWb;
	private String fromCode;
	private String toCode;
	private String gradeCode;
	private double netUnload;
	private double netLoad;
	private String loadWbInName;
	private String loadWbOutName;
	private String loadGateInName;
	private String loadGateOutName;
	private String unloadGateInName;
	private String unloadGateOutName;
	private String unLoadWbInName;
	private String unLoadWbOutName;
	private long loadWbInTime;
	private long loadWbOutTime;
	private long unLoadWbInTime;
	private long unLoadWbOutTime;
	
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Veh:").append(vehicleId).append(" TPRId:").append(tprId).append(" areaCode:").append(areaCode)
		.append(" loadTare:").append(loadTare).append(" loadGross:").append(loadGross).append(" unloadTare:").append(unloadTare).append(" unloadGross:").append(unloadGross)
		.append(" Mat:").append(materialId).append(" TPR Status:").append(tprStatus)
		.append(" Mines:").append(fromMinesId).append(" Combo:").append(Misc.longToUtilDate(comboStart))
		.append(", Grade:").append(gradeCode).append(", FromCode:").append(fromCode)
		.append(", toCode:").append(toCode).append(", NetLoad:").append(netLoad)
		.append(", NetUnload:").append(netUnload);
		return sb.toString();
	}
	
	public static void load(Connection conn) throws Exception {
		try {
			String q = "select vehicle.id vehicle_id, tp_record.tpr_id, tp_record.tpr_status," +
				" tp_record.load_tare, tp_record.load_gross, tp_record.unload_tare, tp_record.unload_gross, "+(Misc.g_doAREACODE ? "tp_record.area_code ":"'SECL' as area_code") +
				" ,tp_record.mines_id, tp_record.mines_code mines_name, tp_record.combo_start, tp_record.challan_date, tp_record.combo_end, tp_record.material_cat, tp_record.earliest_load_gate_in_in, tp_record.latest_load_gate_out_out, tp_record.earliest_unload_gate_in_in, tp_record.latest_unload_gate_out_out, tp_record.challan_date, tp_record.rf_challan_date, tp_record.plant_id, tp_record.challan_no " +
				" ,tp_record.rf_challan_id,tp_record.lr_no,tp_record.rf_lr_id,tp_record.driver_id,tp_record.rf_mines_id,tp_record.transporter_code transporter, tp_record.latest_load_wb_in_out, tp_record.latest_load_wb_out_out, tp_record.load_wb_in_name, tp_record.load_wb_out_name, tp_record.latest_unload_wb_in_out, tp_record.latest_unload_wb_out_out, tp_record.unload_wb_in_name, tp_record.unload_wb_out_name," +
				" tp_record.mines_code, tp_record.destination_code,tp_record.grade_code,tp_record.load_gate_in_name ,tp_record.load_gate_out_name,tp_record.unload_gate_in_name ,tp_record.unload_gate_out_name    " +
				" from current_vehicle_tpr ctp left outer join vehicle on (ctp.vehicle_name = vehicle.std_name) left outer join tp_record on (ctp.tpr_id = tp_record.tpr_id "+(Misc.g_doAREACODE ? " and ctp.area_code = tp_record.area_code" : "")+") " ;
			System.out.println("[TPRBUILDCACHE] "+Thread.currentThread().getId()+" Building TPR Cache : "+ q);
			PreparedStatement ps = conn.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			int count = 0;
			while (rs.next()) {
				TPRLatestCache item = new TPRLatestCache(rs); 
				g_latestTPRCache.put(item.getVehicleId(), item);
//				System.out.println(item.toString());
				count++;
			}
			System.out.println("[TPRBUILDCACHE] "+Thread.currentThread().getId()+" Done Building TPR Cache : count= "+count);
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			g_latestInited = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		g_latestInited = true;
	}
	
	public TPRLatestCache(ResultSet rs) throws SQLException {
		super();
		this.tprStatus = Misc.getRsetInt(rs, "tpr_status");
		this.vehicleId = Misc.getRsetInt(rs, "vehicle_id");
		this.tprId = Misc.getRsetInt(rs, "tpr_id");
		this.loadTare = Misc.getRsetDouble(rs, "load_tare");
		this.loadGross = Misc.getRsetDouble(rs, "load_gross");
		this.unloadTare = Misc.getRsetDouble(rs, "unload_tare");
		this.unloadGross = Misc.getRsetDouble(rs, "unload_gross");
		this.areaCode=rs.getString("area_code");
		this.fromMinesId = Misc.getRsetInt(rs, "mines_id");
		this.toDestId = Misc.getRsetInt(rs, "plant_id");
		this.materialId = Misc.getRsetInt(rs, "material_cat");
		this.lgin = Misc.sqlToLong(rs.getTimestamp("earliest_load_gate_in_in"));
		this.lgout = Misc.sqlToLong(rs.getTimestamp("latest_load_gate_out_out"));
		this.ugin = Misc.sqlToLong(rs.getTimestamp("earliest_unload_gate_in_in"));
		this.ugout = Misc.sqlToLong(rs.getTimestamp("latest_unload_gate_out_out"));
		this.comboStart = Misc.sqlToLong(rs.getTimestamp("combo_start"));
		this.challanNo = rs.getString("challan_no");
		this.rfChallanNo = rs.getString("rf_challan_id");
		this.lrNo = rs.getString("lr_no");
		this.rfLrNo = rs.getString("rf_lr_id");
		this.driverId = Misc.getRsetInt(rs, "driver_id");
//		this.doNumber = doNumber;
		this.transporter = rs.getString("transporter");
//		this.rfTransporter = rfTransporter;
		this.rfMinesId=Misc.getRsetInt(rs, "rf_mines_id");
		this.comboEnd=Misc.sqlToLong(rs.getTimestamp("combo_end"));

		this.gradeCode=rs.getString("grade_code");
		this.fromCode=rs.getString("mines_code");
		this.toCode=rs.getString("destination_code");
		
		this.netLoad=loadGross-loadTare;
		this.netUnload=unloadGross-unloadTare;
		this.loadWbInName=rs.getString("load_wb_in_name");//load_wb_in_name
		this.loadWbOutName=rs.getString("load_wb_out_name");//load_wb_out_name
		this.unLoadWbInName=rs.getString("unload_wb_in_name");//load_wb_in_name
		this.unLoadWbOutName=rs.getString("unload_wb_out_name");//load_wb_out_name
		
		this.loadWbInTime=Misc.sqlToLong(rs.getTimestamp("latest_load_wb_in_out"));//load_wb_in_name
		this.loadWbOutTime=Misc.sqlToLong(rs.getTimestamp("latest_load_wb_out_out"));//load_wb_out_name
		this.unLoadWbInTime=Misc.sqlToLong(rs.getTimestamp("latest_unload_wb_in_out"));//load_wb_in_name
		this.unLoadWbOutTime=Misc.sqlToLong(rs.getTimestamp("latest_unload_wb_out_out"));//load_wb_out_name
		
		this.loadGateInName=rs.getString("load_gate_in_name");//load_gate_in_name
		this.loadGateOutName=rs.getString("load_gate_out_name");//load_gate_out_name
		this.unloadGateInName=rs.getString("unload_gate_in_name");//unload_gate_in_name
		this.unloadGateOutName=rs.getString("unload_gate_out_name");//unload_gate_out_name
	}
	
	public static void main(String[] args) {
		getLatest(1604);
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public int getTprId() {
		return tprId;
	}

	public void setTprId(int tprId) {
		this.tprId = tprId;
	}

	public int getFromMinesId() {
		return fromMinesId;
	}

	public void setFromMinesId(int fromMinesId) {
		this.fromMinesId = fromMinesId;
	}

	public int getToDestId() {
		return toDestId;
	}

	public void setToDestId(int toDestId) {
		this.toDestId = toDestId;
	}

	public int getMaterialId() {
		return materialId;
	}

	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}

	public int getTprStatus() {
		return tprStatus;
	}

	public void setTprStatus(int tprStatus) {
		this.tprStatus = tprStatus;
	}

	public long getLgin() {
		return lgin;
	}

	public void setLgin(long lgin) {
		this.lgin = lgin;
	}

	public long getLgout() {
		return lgout;
	}

	public void setLgout(long lgout) {
		this.lgout = lgout;
	}

	public long getUgin() {
		return ugin;
	}

	public void setUgin(long ugin) {
		this.ugin = ugin;
	}

	public long getUgout() {
		return ugout;
	}

	public void setUgout(long ugout) {
		this.ugout = ugout;
	}

	public long getChallanDate() {
		return challanDate;
	}

	public void setChallanDate(long challanDate) {
		this.challanDate = challanDate;
	}

	public long getRfChallanDate() {
		return rfChallanDate;
	}

	public void setRfChallanDate(long rfChallanDate) {
		this.rfChallanDate = rfChallanDate;
	}

	public long getComboStart() {
		return comboStart;
	}

	public void setComboStart(long comboStart) {
		this.comboStart = comboStart;
	}

	public long getComboEnd() {
		return comboEnd;
	}

	public void setComboEnd(long comboEnd) {
		this.comboEnd = comboEnd;
	}

	public String getChallanNo() {
		return challanNo;
	}

	public void setChallanNo(String challanNo) {
		this.challanNo = challanNo;
	}

	public String getRfChallanNo() {
		return rfChallanNo;
	}

	public void setRfChallanNo(String rfChallanNo) {
		this.rfChallanNo = rfChallanNo;
	}

	public String getLrNo() {
		return lrNo;
	}

	public void setLrNo(String lrNo) {
		this.lrNo = lrNo;
	}

	public String getRfLrNo() {
		return rfLrNo;
	}

	public void setRfLrNo(String rfLrNo) {
		this.rfLrNo = rfLrNo;
	}

	public String getDoNumber() {
		return doNumber;
	}

	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public String getTransporter() {
		return transporter;
	}

	public void setTransporter(String transporter) {
		this.transporter = transporter;
	}

	public String getRfTransporter() {
		return rfTransporter;
	}

	public void setRfTransporter(String rfTransporter) {
		this.rfTransporter = rfTransporter;
	}

	public int getRfMinesId() {
		return rfMinesId;
	}

	public void setRfMinesId(int rfMinesId) {
		this.rfMinesId = rfMinesId;
	}

	public double getLoadTare() {
		return loadTare;
	}

	public void setLoadTare(double loadTare) {
		this.loadTare = loadTare;
	}

	public double getLoadGross() {
		return loadGross;
	}

	public void setLoadGross(double loadGross) {
		this.loadGross = loadGross;
	}

	public double getUnloadTare() {
		return unloadTare;
	}

	public void setUnloadTare(double unloadTare) {
		this.unloadTare = unloadTare;
	}

	public double getUnloadGross() {
		return unloadGross;
	}

	public void setUnloadGross(double unloadGross) {
		this.unloadGross = unloadGross;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public long getLoadTareTime() {
		return loadTareTime;
	}

	public void setLoadTareTime(long loadTareTime) {
		this.loadTareTime = loadTareTime;
	}

	public long getLoadGrossTime() {
		return loadGrossTime;
	}

	public void setLoadGrossTime(long loadGrossTime) {
		this.loadGrossTime = loadGrossTime;
	}

	public String getLoadTareWb() {
		return loadTareWb;
	}

	public void setLoadTareWb(String loadTareWb) {
		this.loadTareWb = loadTareWb;
	}

	public String getLoadGrossWb() {
		return loadGrossWb;
	}

	public void setLoadGrossWb(String loadGrossWb) {
		this.loadGrossWb = loadGrossWb;
	}

	public long getUnloadGrossTime() {
		return unloadGrossTime;
	}

	public void setUnloadGrossTime(long unloadGrossTime) {
		this.unloadGrossTime = unloadGrossTime;
	}

	public long getUnloadTareTime() {
		return unloadTareTime;
	}

	public void setUnloadTareTime(long unloadTareTime) {
		this.unloadTareTime = unloadTareTime;
	}

	public String getUnloadTareWb() {
		return unloadTareWb;
	}

	public void setUnloadTareWb(String unloadTareWb) {
		this.unloadTareWb = unloadTareWb;
	}

	public String getUnloadGrossWb() {
		return unloadGrossWb;
	}

	public void setUnloadGrossWb(String unloadGrossWb) {
		this.unloadGrossWb = unloadGrossWb;
	}

	public String getFromCode() {
		return fromCode;
	}

	public void setFromCode(String fromCode) {
		this.fromCode = fromCode;
	}

	public String getToCode() {
		return toCode;
	}

	public void setToCode(String toCode) {
		this.toCode = toCode;
	}

	public String getGradeCode() {
		return gradeCode;
	}

	public void setGradeCode(String gradeCode) {
		this.gradeCode = gradeCode;
	}

	public double getNetUnload() {
		return netUnload;
	}

	public void setNetUnload(double netUnload) {
		this.netUnload = netUnload;
	}

	public double getNetLoad() {
		return netLoad;
	}

	public void setNetLoad(double netLoad) {
		this.netLoad = netLoad;
	}

	public String getLoadWbInName() {
		return loadWbInName;
	}

	public void setLoadWbInName(String loadWbInName) {
		this.loadWbInName = loadWbInName;
	}

	public String getLoadWbOutName() {
		return loadWbOutName;
	}

	public void setLoadWbOutName(String loadWbOutName) {
		this.loadWbOutName = loadWbOutName;
	}

	public String getLoadGateInName() {
		return loadGateInName;
	}

	public void setLoadGateInName(String loadGateInName) {
		this.loadGateInName = loadGateInName;
	}

	public String getLoadGateOutName() {
		return loadGateOutName;
	}

	public void setLoadGateOutName(String loadGateOutName) {
		this.loadGateOutName = loadGateOutName;
	}

	public String getUnloadGateInName() {
		return unloadGateInName;
	}

	public void setUnloadGateInName(String unloadGateInName) {
		this.unloadGateInName = unloadGateInName;
	}

	public String getUnloadGateOutName() {
		return unloadGateOutName;
	}

	public void setUnloadGateOutName(String unloadGateOutName) {
		this.unloadGateOutName = unloadGateOutName;
	}

	public String getUnLoadWbInName() {
		return unLoadWbInName;
	}

	public void setUnLoadWbInName(String unLoadWbInName) {
		this.unLoadWbInName = unLoadWbInName;
	}

	public String getUnLoadWbOutName() {
		return unLoadWbOutName;
	}

	public void setUnLoadWbOutName(String unLoadWbOutName) {
		this.unLoadWbOutName = unLoadWbOutName;
	}

	public long getLoadWbInTime() {
		return loadWbInTime;
	}

	public void setLoadWbInTime(long loadWbInTime) {
		this.loadWbInTime = loadWbInTime;
	}

	public long getLoadWbOutTime() {
		return loadWbOutTime;
	}

	public void setLoadWbOutTime(long loadWbOutTime) {
		this.loadWbOutTime = loadWbOutTime;
	}

	public long getUnLoadWbInTime() {
		return unLoadWbInTime;
	}

	public void setUnLoadWbInTime(long unLoadWbInTime) {
		this.unLoadWbInTime = unLoadWbInTime;
	}

	public long getUnLoadWbOutTime() {
		return unLoadWbOutTime;
	}

	public void setUnLoadWbOutTime(long unLoadWbOutTime) {
		this.unLoadWbOutTime = unLoadWbOutTime;
	}
	
	
}

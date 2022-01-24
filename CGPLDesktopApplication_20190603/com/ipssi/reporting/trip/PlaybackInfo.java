package com.ipssi.reporting.trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.OrgConst;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;

public class PlaybackInfo {
	public static int G_MASK_L = 0x1;
	public static int G_MASK_L2U = 0x2;
	public static int G_MASK_U = 0x4;
	public static int G_MASK_U2L = 0x8;

	public static boolean getBooleanValFromOrgConst(MiscInner.PortInfo portInfo, int param, boolean defval) {
		ArrayList<Integer> ilist = portInfo.getIntParams(param);
		if (ilist != null && ilist.size() > 0)
			return ilist.get(0) != 0;
		return defval;
	}
	public static class PlaybackImgInfo {
		public double lx = Misc.getUndefDouble();
		public double ly = Misc.getUndefDouble();
		public double ux = Misc.getUndefDouble();
		public double uy = Misc.getUndefDouble();
		public double imgHeight = Misc.getUndefDouble();
		public double imgWidth = Misc.getUndefDouble();
		public String fileName = null;
		public boolean isValid() {
			return !Misc.isUndef(lx) && !Misc.isUndef(ly) && !Misc.isUndef(ux) && !Misc.isUndef(uy) && fileName != null && ((ux-lx) > 0.0001) && ((uy-ly) > 0.0001);
		}
		public PlaybackImgInfo(double lx, double ly, double ux, double uy,
				double imgHeight, double imgWidth, String fileName) {
			super();
			this.lx = lx;
			this.ly = ly;
			this.ux = ux;
			this.uy = uy;
			this.imgHeight = imgHeight;
			this.imgWidth = imgWidth;
			this.fileName = fileName;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Name:").append(fileName).append("(lx,ly,ux,uy)").append(lx).append(",").append(ly).append(",").append(ux).append(",").append(uy).append(" (h,w)").append(imgHeight).append(",").append(imgWidth);
			return sb.toString();
		}
		public String getFullFileName() {
			return fileName != null && fileName.length() > 0 ? Misc.ABSOLUTE_IMAGE_PATH+System.getProperty("file.separator")+"playback_images"+System.getProperty("file.separator")+fileName : null;
		}
		public static PlaybackImgInfo get(SessionManager session, int vehicleId) throws Exception {
	    	Connection conn = session.getConnection();
	    	CacheTrack.VehicleSetup vehsetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
	    	String baseFile = session.getParameter("base_file");
			double lx = Misc.getParamAsDouble(session.getParameter("lx"));
			double ux = Misc.getParamAsDouble(session.getParameter("ux"));
			double ly = Misc.getParamAsDouble(session.getParameter("ly"));
			double uy = Misc.getParamAsDouble(session.getParameter("uy"));
			
			int pv123 = vehsetup == null ? 463 : vehsetup.m_ownerOrgId;//DEBUG13 Misc.getParamAsInt(session.getParameter("pv123"));
			MiscInner.PortInfo portInfo = session.getCache().getPortInfo(pv123, conn);
			if (baseFile != null)
				baseFile = baseFile.trim();
			
			if (baseFile == null || baseFile.length() == 0) {
				ArrayList<String> bml = portInfo.getStringParams(10076);
				if (bml != null && bml.size() > 0)
					baseFile = bml.get(0);
			}
			if (Misc.isUndef(lx)) {
				ArrayList<Double> dbl = portInfo.getDoubleParams(10077);
				if (dbl != null && dbl.size() > 0)
					lx= dbl.get(0);
			}
			if (Misc.isUndef(ux)) {
				ArrayList<Double> dbl = portInfo.getDoubleParams(10078);
				if (dbl != null && dbl.size() > 0)
					ux= dbl.get(0);
			}
			if (Misc.isUndef(ly)) {
				ArrayList<Double> dbl = portInfo.getDoubleParams(10079);
				if (dbl != null && dbl.size() > 0)
					ly= dbl.get(0);
			}
			if (Misc.isUndef(uy)) {
				ArrayList<Double> dbl = portInfo.getDoubleParams(10080);
				if (dbl != null && dbl.size() > 0)
					uy= dbl.get(0);
			}
			if (baseFile != null)
				baseFile = baseFile.trim();
			
			//String baseImageFullName = baseFile != null && baseFile.length() > 0 ? Misc.getServerConfigPath()+System.getProperty("file.separator")+baseFile : null;
			return new PlaybackImgInfo(lx, ly,ux,uy,Misc.getUndefDouble(), Misc.getUndefDouble(), baseFile); 
			
	    }
	    public static PlaybackImgInfo get(Connection conn, int vehicleId, long stasLong, long enasLong)  throws Exception {
	    	java.sql.Timestamp st = Misc.utilToSqlDate(stasLong);
	    	java.sql.Timestamp en = Misc.utilToSqlDate(enasLong);
	    	String fileName = null;
	    	int imgHeight = 1400;
	    	int imgWidth = 1140;
	    	PreparedStatement ps = conn.prepareStatement("select min(longitude), min(latitude), max(longitude), max(latitude) from logged_data where vehicle_id = ? and attribute_id=0 and gps_record_time between ? and ? ");
	    	ps.setInt(1, vehicleId);
	    	ps.setTimestamp(2, st);
	    	ps.setTimestamp(3, en);
	    	ResultSet rs = ps.executeQuery();
	    	double lx = Misc.getUndefDouble();
	    	double ly = Misc.getUndefDouble();
	    	double ux = Misc.getUndefDouble();
	    	double uy = Misc.getUndefDouble();
	    	PlaybackImgInfo retval = null;
	    	if (rs.next()) {
	    		lx = Misc.getRsetDouble(rs, 1);
	    		ly = Misc.getRsetDouble(rs, 2);
	    		ux = Misc.getRsetDouble(rs, 3);
	    		uy = Misc.getRsetDouble(rs, 4);
	    	}
	    	rs = Misc.closeRS(rs);
	    	ps = Misc.closePS(ps);
	    	CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
	    	int vehiclePortNodeId = vehSetup == null ? Misc.G_TOP_LEVEL_PORT : vehSetup.m_ownerOrgId; 
	    	ps = conn.prepareStatement("select pb.id,pb.name, img_height, img_width,lx,ly,ux,uy from playback_images pb join port_nodes anc on (anc.id = pb.port_node_id) "+ 
	    	" join port_nodes leaf on (leaf.id = ? and anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "+
	    			" order by "+
	    			" (case when ?/lx between 1.1 and 1.2 and ?/ly between 1.1 and 1.2 and ux/? between 1.1 and 1.2 and uy/? between 1.1 and 1.2 then 100000 "+
	    			"      when lx <= ? and ly <= ? and ux >= ? and uy >= ? then 100000-(?/lx+?/ly+ux/?+uy/?) "+
	    			" else getPropOverlap(?,?,lx,ux)*getPropOverlap(?,?,ly,uy) "+
	    			" end) desc, ((uy-ux)*(ly-lx)) desc, anc.lhs_number desc "
	    			)
	    			;
	    	    int colIndex = 1;
	    		ps.setInt(colIndex++,vehiclePortNodeId);
	    		Misc.setParamDouble(ps, lx, colIndex++);
	    		Misc.setParamDouble(ps, ly, colIndex++);
	    		Misc.setParamDouble(ps, ux, colIndex++);
	    		Misc.setParamDouble(ps, uy, colIndex++);
	    		Misc.setParamDouble(ps, lx, colIndex++);
	    		Misc.setParamDouble(ps, ly, colIndex++);
	    		Misc.setParamDouble(ps, ux, colIndex++);
	    		Misc.setParamDouble(ps, uy, colIndex++);
	    		Misc.setParamDouble(ps, lx, colIndex++);
	    		Misc.setParamDouble(ps, ly, colIndex++);
	    		Misc.setParamDouble(ps, ux, colIndex++);
	    		Misc.setParamDouble(ps, uy, colIndex++);
	    		
	    		Misc.setParamDouble(ps, lx, colIndex++);
	    		Misc.setParamDouble(ps, ux, colIndex++);
	    		Misc.setParamDouble(ps, ly, colIndex++);
	    		Misc.setParamDouble(ps, uy, colIndex++);
	    		rs = ps.executeQuery();
	    		
	    		if (rs.next()) {
	    			fileName = rs.getString(2);
	    			imgHeight = Misc.getRsetInt(rs, 3, imgHeight);
	    			imgWidth = Misc.getRsetInt(rs, 3, imgWidth);
	    			retval = new PlaybackImgInfo(Misc.getRsetDouble(rs, "lx"), Misc.getRsetDouble(rs, "ly"), Misc.getRsetDouble(rs, "ux"), Misc.getRsetDouble(rs, "uy"),
	    					Misc.getRsetDouble(rs, "img_height"), Misc.getRsetDouble(rs, "img_width"), rs.getString("name"));
	    		}
	    		rs = Misc.closeRS(rs);
	    		ps = Misc.closePS(ps);
	    		if (retval != null && (retval.fileName == null || Misc.isUndef(retval.lx) || Misc.isUndef(retval.ly) || Misc.isUndef(retval.ux) || Misc.isUndef(retval.uy)))
	    			retval = null;
	    		return retval;
	    }
	}
	public static class TripTiming {
		public int vehicleId;
		public long lgin;
		public long lgout;
		public long ugin;
		public long ugout;
		public long cnfTime;
		
		public TripTiming(int vehicleId, long lgin, long lgout, long ugin, long ugout, long cnfTime) {
			super();
			this.vehicleId = vehicleId;
			this.lgin = lgin;
			this.lgout = lgout;
			this.ugin = ugin;
			this.ugout = ugout;
			this.cnfTime = cnfTime;
		}
		public static TripTiming get(Connection conn, int tripId) throws SQLException {
			PreparedStatement ps = conn.prepareStatement("select load_gate_in, load_gate_out, unload_gate_in, unload_gate_out, confirm_time, vehicle_id from trip_info where id=?");
	    	ps.setInt(1, tripId);
	    	ResultSet rs = ps.executeQuery();
	    	java.sql.Timestamp st = null;
	    	java.sql.Timestamp en = null;
	    	int vehicleId = Misc.getUndefInt();
	    	TripTiming retval = null;
	    	if (rs.next()) {
	    		retval = new TripTiming(rs.getInt(6), Misc.sqlToLong(rs.getTimestamp(1)), Misc.sqlToLong(rs.getTimestamp(2)), Misc.sqlToLong(rs.getTimestamp(3)), Misc.sqlToLong(rs.getTimestamp(4)), Misc.sqlToLong(rs.getTimestamp(5)));
	    	}
	    	rs = Misc.closeRS(rs);
	    	ps = Misc.closePS(ps);
	    	return retval;
		}
	}	
    
	public static class TripPointInfo {
		public long ts;
		public double lon;
		public double lat;
		public String name;
		public int durSec;
		public int avSinceLastLIn;
		public TripPointInfo(long ts, double lon, double lat, String name, int durSec, int avSinceLastLin) {
			this.ts = ts;
			this.lon = lon;
			this.lat = lat;
			this.name = name;
			this.durSec = durSec;
			this.avSinceLastLIn = avSinceLastLin;
		}
	}
	public static class ChallanPointInfo {
		public long challanDate;
		public double lon;
		public double lat;
		public String grNo;
		public String destCity;
		public String toLoc;
		public int invoiceDistKM;
		public ChallanPointInfo(long challanDate, double lon, double lat,
				String grNo, String destCity, String toLoc, int invoiceDistKM) {
			super();
			this.challanDate = challanDate;
			this.lon = lon;
			this.lat = lat;
			this.grNo = grNo;
			this.destCity = destCity;
			this.toLoc = toLoc;
			this.invoiceDistKM = invoiceDistKM;
		}
		
	}
	public static class ExtTripInfo {
		public ArrayList<MiscInner.PairIntStr> lopid = new ArrayList<MiscInner.PairIntStr>();
		public ArrayList<MiscInner.PairIntStr> uopid = new ArrayList<MiscInner.PairIntStr>();
		public ArrayList<TripPointInfo> lopLoc = new ArrayList<TripPointInfo>();
		public ArrayList<TripPointInfo> uopLoc = new ArrayList<TripPointInfo>();
		public ArrayList<TripPointInfo> otherLoadLoc = new ArrayList<TripPointInfo>();
		public ArrayList<TripPointInfo> otherUnloadLoc = new ArrayList<TripPointInfo>();
		public ArrayList<ChallanPointInfo> challanInfo = new ArrayList<ChallanPointInfo>();
		public ArrayList<Triple<Integer, String, ArrayList<TripPointInfo>>> eventList = new ArrayList<Triple<Integer, String, ArrayList<TripPointInfo>>>();//1st ruleId, 2nd name of rule
		public static ExtTripInfo getExtTripInfo(Connection conn, int vehicleId, long st, long en, ArrayList<Triple<Integer, Integer, Integer>> whichAndWhatEvent) throws Exception {
			//whichEventsAndWhat = first - ruleId, 2nd - durSec exceeding, 3rd whichPart of trip
			CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehicleId, conn);
			int pv123 = vehSetup == null ? 463 : vehSetup.m_ownerOrgId;
			MiscInner.PortInfo portInfo = Cache.getCacheInstance(conn).getPortInfo(pv123, conn);
			boolean showChallan = PlaybackInfo.getBooleanValFromOrgConst(portInfo,OrgConst.PLAYBACK_INT_SHOW_CHALLAN,false);
			boolean showLUOpName = PlaybackInfo.getBooleanValFromOrgConst(portInfo,OrgConst.PLAYBACK_INT_SHOW_LU_OP_NAME,false);
			boolean showLUPts = PlaybackInfo.getBooleanValFromOrgConst(portInfo,OrgConst.PLAYBACK_INT_SHOW_LU_PTS,!showLUOpName);
			boolean showAltLUPts = PlaybackInfo.getBooleanValFromOrgConst(portInfo,OrgConst.PLAYBACK_INT_SHOW_ALT_LU_PTS,true);
			
			ExtTripInfo retval = new ExtTripInfo();
			if(showLUPts || showAltLUPts)
				ExtTripInfo.loadTripPoints(retval, conn, vehicleId, st, en);
			if(showChallan)
				ExtTripInfo.loadLUOpIdAndChallan(retval, conn, vehicleId, st, en);
			ExtTripInfo.loadEEStuff(retval, conn, vehicleId, st, en, whichAndWhatEvent);
			
			return retval;
		}
		
		private static void loadTripPoints(ExtTripInfo retval, Connection conn, int vehicleId, long st, long en) throws Exception {
			String q = " select logged_data.longitude, logged_data.latitude, logged_data.gps_record_time " +
			",(case when logged_data.gps_record_time = (case when load_area_in is not null then load_area_in else load_gate_in end) then lop.name "+
			" when logged_data.gps_record_time = (case when unload_area_in is not null then unload_area_in else unload_gate_in end) then uop.name "+
			" else logged_data.name end) "+
					", logged_data.attribute_value, trip_info.id "+
            "  "+
			",(case when logged_data.gps_record_time = (case when load_area_in is not null then load_area_in else load_gate_in end) then 0 "+
			" when logged_data.gps_record_time = (case when unload_area_in is not null then unload_area_in else unload_gate_in end) then 1 "+
			" when logged_data.gps_record_time = flat_multi_lu.multi_gin_1 then 2 "+
			" when logged_data.gps_record_time = flat_multi_lu.multi_gin_2 then 3 "+
			" when logged_data.gps_record_time = flat_multi_lu.multi_gin_3 then 4 "+
			" when logged_data.gps_record_time = flat_multi_lu.load_multi_gin_1 then 5 "+
			" when logged_data.gps_record_time = flat_multi_lu.load_multi_gin_2 then 6 "+
			" when logged_data.gps_record_time = flat_multi_lu.load_multi_gin_3 then 7 "+
			" else 8 end) ty "+
			",(case when logged_data.gps_record_time = (case when load_area_in is not null then load_area_in else load_gate_in end) then timestampdiff(second, load_gate_in, load_gate_out)  "+
			" when logged_data.gps_record_time = (case when unload_area_in is not null then unload_area_in else unload_gate_in end) then  timestampdiff(second, unload_gate_in, unload_gate_out)  "+
			" when logged_data.gps_record_time = flat_multi_lu.multi_gin_1 then  timestampdiff(second, flat_multi_lu.multi_gin_1, flat_multi_lu.multi_gout_1)  "+
			" when logged_data.gps_record_time = flat_multi_lu.multi_gin_2 then  timestampdiff(second, flat_multi_lu.multi_gin_2, flat_multi_lu.multi_gout_2)  "+
			" when logged_data.gps_record_time = flat_multi_lu.multi_gin_3 then  timestampdiff(second, flat_multi_lu.multi_gin_3, flat_multi_lu.multi_gout_3)  "+
			" when logged_data.gps_record_time = flat_multi_lu.load_multi_gin_1 then  timestampdiff(second, flat_multi_lu.load_multi_gin_1, flat_multi_lu.load_multi_gout_1)  "+
			" when logged_data.gps_record_time = flat_multi_lu.load_multi_gin_2 then  timestampdiff(second, flat_multi_lu.load_multi_gin_2, flat_multi_lu.load_multi_gout_2)  "+
			" when logged_data.gps_record_time = flat_multi_lu.load_multi_gin_3 then  timestampdiff(second, flat_multi_lu.load_multi_gin_3, flat_multi_lu.load_multi_gout_3)  "+
			" else null end) dur "+
			
			" from "+
			" trip_info  left outer join op_station lop on (lop.id = trip_info.load_gate_op) left outer join op_station uop on (uop.id=unload_gate_op) left outer join flat_multi_lu on (trip_info.id = flat_multi_lu.trip_id) "+
			" left outer join logged_data on (logged_data.vehicle_id = trip_info.vehicle_id and logged_data.attribute_id=0 "+
			" and ( "+
			"      logged_data.gps_record_time = (case when load_area_in is not null then load_area_in else load_gate_in end) "+
			" or logged_data.gps_record_time = (case when unload_area_in is not null then unload_area_in else unload_gate_in end) "+
			" or logged_data.gps_record_time = flat_multi_lu.multi_gin_1 "+
			" or logged_data.gps_record_time = flat_multi_lu.multi_gin_2 "+
			" or logged_data.gps_record_time = flat_multi_lu.multi_gin_3 "+
			" or logged_data.gps_record_time = flat_multi_lu.load_multi_gin_1 "+
			" or logged_data.gps_record_time = flat_multi_lu.load_multi_gin_2 "+
			" or logged_data.gps_record_time = flat_multi_lu.load_multi_gin_3 "+
			") )"+//end of lgd join clause
			//combo_start <= dur_end and combo_end >= dur_start
			" where trip_info.vehicle_id = ? and (trip_info.combo_start <= ? and trip_info.combo_end >= ?) and ( "+
			"      trip_info.load_gate_in between ? and ? "+
			" or     trip_info.load_gate_out between ? and ? "+
		    " or trip_info.unload_gate_in between ? and ? "+
		    " or trip_info.unload_gate_out between ? and ? "+
		    " or (? >= coalesce(load_gate_in, load_gate_out, unload_gate_in, unload_gate_out) and ? <= coalesce(unload_gate_out, now())) " +
			" ) "+//end of and clause
		    " order by trip_info.combo_start, trip_info.id, logged_data.gps_record_time "
            		;
			java.sql.Timestamp tmFrom = Misc.utilToSqlDate(st);
			java.sql.Timestamp tmTo = Misc.utilToSqlDate(en);
			PreparedStatement ps = conn.prepareStatement(q);
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);

			System.out.println("QUERY: "+ps.toString());
			ResultSet rs = ps.executeQuery();
			double startAV = Misc.getUndefDouble();
			
			int prevTripId = Misc.getUndefInt();
			while (rs.next()) {
				int tripId = rs.getInt(6);
				if (tripId != prevTripId) {
					startAV = Misc.getRsetDouble(rs, 5,0);
					prevTripId = tripId;
				}
				
				
				double lon = Misc.getRsetDouble(rs, 1);
				double lat = Misc.getRsetDouble(rs, 2);
				long ts = Misc.sqlToLong(rs.getTimestamp(3));
				String name = rs.getString(4);
				double av = Misc.getRsetDouble(rs,5,0);
				int ty = Misc.getRsetInt(rs, 7, 8);
				int dur = rs.getInt(8);
				TripPointInfo pt = new TripPointInfo(ts, lon, lat, name, dur, (int)((av-startAV)*1.05));
				if (ty ==0) {//load
					retval.lopLoc.add(pt);
				}
				else if (ty == 1) {
					retval.uopLoc.add(pt);
				}
				else if (ty == 2 || ty == 3 || ty == 4) {
					retval.otherLoadLoc.add(pt);
				}
				else if (ty == 5 || ty == 6 || ty == 7) {
					retval.otherUnloadLoc.add(pt);
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return;
		}
		private static void loadLUOpIdAndChallan(ExtTripInfo retval, Connection conn, int vehicleId, long st, long en) throws Exception {
			String q = " select trip_info.id, trip_info.load_gate_op, lop.name, trip_info.unload_gate_op, uop.name "+
			" ,cd.gr_no_, cd.challan_date, cd.dest_city, cd.to_location, trip_info.dest_lon, trip_info.dest_lat, cd.invoice_distkm "+
            "  "+
			" from "+
			" trip_info  left outer join op_station lop on (lop.id = trip_info.load_gate_op) left outer join op_station uop on (uop.id = trip_info.unload_gate_op) "+
			" left outer join challan_details cd on (cd.trip_info_id = trip_info.id) "+
			" where trip_info.vehicle_id = ? and (trip_info.combo_start <= ? and trip_info.combo_end >= ?) and ( "+
			"      trip_info.load_gate_in between ? and ? "+
			" or     trip_info.load_gate_out between ? and ? "+
		    " or trip_info.unload_gate_in between ? and ? "+
		    " or trip_info.unload_gate_out between ? and ? "+
		    " or (? >= coalesce(load_gate_in, load_gate_out, unload_gate_in, unload_gate_out) and ? <= coalesce(unload_gate_out, now())) " +
			" ) "+//end of and clause
		    " order by trip_info.combo_start "
            		;
			java.sql.Timestamp tmFrom = Misc.utilToSqlDate(st);
			java.sql.Timestamp tmTo = Misc.utilToSqlDate(en);
			PreparedStatement ps = conn.prepareStatement(q);
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);

			System.out.println("QUERY: "+ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (!Misc.isUndef(Misc.getRsetInt(rs, 2)))
						retval.lopid.add(new MiscInner.PairIntStr(rs.getInt(2), rs.getString(3)));
				if (!Misc.isUndef(Misc.getRsetInt(rs, 4)))
					retval.uopid.add(new MiscInner.PairIntStr(rs.getInt(4), rs.getString(5)));
				//" cd.gr_no_, cd.challan_date, cd.dest_city, cd.to_location, trip_info.dest_lon, trip_info.dest_lat, cd.invoice_distkm "+
				if (rs.getTimestamp(7) != null && !Misc.isUndef(Misc.getRsetDouble(rs,10))) {
					retval.challanInfo.add(new  ChallanPointInfo(Misc.sqlToLong(rs.getTimestamp(7)), Misc.getRsetDouble(rs,10), Misc.getRsetDouble(rs, 11), rs.getString(6), rs.getString(8), rs.getString(9), (int) Misc.getRsetDouble(rs, 12)));
				}
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return;
		}
		
        private static String getRuleName(Connection conn, int ruleId) throws Exception {
        	PreparedStatement ps = conn.prepareStatement("select name from rules where id=?");
        	ps.setInt(1, ruleId);
        	ResultSet rs = ps.executeQuery();
        	String name = rs.next() ? rs.getString(1) : null;
        	Misc.closeRS(rs);
        	Misc.closePS(ps);
        	return name;
        }
		private static void loadEEStuff(ExtTripInfo retval, Connection conn, int vehicleId, long st, long en, ArrayList<Triple<Integer, Integer, Integer>> whichAndWhatEvent) throws Exception {
			//whichAndWhatEvent first = ruleId, second = exceeding Dur, third sel mask
//			public static int G_MASK_L = 0x1;
//			public static int G_MASK_L2U = 0x2;
//			public static int G_MASK_U = 0x4;
//			public static int G_MASK_U2L = 0x8;
			StringBuilder ruleIdStr = new StringBuilder();
			if (whichAndWhatEvent == null || whichAndWhatEvent.size() == 0)
				return;
			for (int i=0,is=whichAndWhatEvent.size(); i<is; i++) {
				if (i != 0)
					ruleIdStr.append(",");
				ruleIdStr.append(whichAndWhatEvent.get(i).first);
			}
			String q = " select ee.rule_id, ee.event_start_time, ee.event_begin_name, ee.event_begin_longitude, ee.event_begin_latitude, lgd.attribute_value, timestampdiff(second, event_start_time, (case when event_stop_time is null then now() else event_stop_time end) dur, trip_info.id "+
			" (case when ee.event_start_time >= load_gate_in and ee.event_start_time < load_gate_out then 0 "+
			" when ee.event_start_time >= load_gate_out and ee.event_start_time < unload_gate_in then 1 "+
			" when ee.event_start_time >= unload_gate_in and ee.event_start_time < unload_gate_out then 2 "+
			" else 3 end) ty "+
            "  "+
			" from "+
			" trip_info  "+
			" join engine_events ee on (ee.vehicle_id = trip_info.vehicle_id and ee.rule_id in ("+
			ruleIdStr+") "+
			" left outer join logged_data lgd on (lgd.vehicle_id = ee.vehicle_id and lgd.attribute_id=0 and lgd.gps_record_time = ee.event_start_time) "+
			" where trip_info.vehicle_id = ? and (trip_info.combo_start <= ? and trip_info.combo_end >= ?) and ( "+
			"      trip_info.load_gate_in between ? and ? "+
			" or     trip_info.load_gate_out between ? and ? "+
		    " or trip_info.unload_gate_in between ? and ? "+
		    " or trip_info.unload_gate_out between ? and ? "+
		    " or (? >= coalesce(load_gate_in, load_gate_out, unload_gate_in, unload_gate_out) and ? <= coalesce(unload_gate_out, now())) " +
			" ) "+//end of and clause
		    " order by ee.rule_id, ee.event_start_time "
            		;
			java.sql.Timestamp tmFrom = Misc.utilToSqlDate(st);
			java.sql.Timestamp tmTo = Misc.utilToSqlDate(en);
			PreparedStatement ps = conn.prepareStatement(q);
			int colIndex = 1;
			ps.setInt(colIndex++, vehicleId);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);
			ps.setTimestamp(colIndex++,tmFrom);
			ps.setTimestamp(colIndex++,tmTo);

			System.out.println("QUERY: "+ps.toString());
			ResultSet rs = ps.executeQuery();
			int prevRuleId = Misc.getUndefInt();
			int durThreshSec = 0;
			int mask = 0;
			Triple<Integer, String, ArrayList<TripPointInfo>> addInThis = null;
			while (rs.next()) {
				int ruleId = rs.getInt(1);
				if (ruleId != prevRuleId) {
					String ruleName = getRuleName(conn, ruleId);
					addInThis = new Triple<Integer, String, ArrayList<TripPointInfo>>(ruleId, ruleName, new ArrayList<TripPointInfo>());
					retval.eventList.add(addInThis);
					durThreshSec = 0;
					mask = PlaybackInfo.G_MASK_L2U;
					for (int i=0,is=whichAndWhatEvent.size();i<is;i++) {
						if (whichAndWhatEvent.get(i).first == ruleId) {
							durThreshSec = whichAndWhatEvent.get(i).second;
							mask = whichAndWhatEvent.get(i).third;
						}
					}
					prevRuleId = ruleId;
				}
				
			//" select ee.rule_id, ee.event_start_time, ee.event_begin_name, ee.event_begin_longitude, ee.event_begin_latitude, lgd.attribute_value, timestampdiff(second, event_start_time, (case when event_stop_time is null then now() else event_stop_time end) dur, trip_info.id "+
				long ts = Misc.sqlToLong(rs.getTimestamp(2));
				String name = rs.getString(3);
				double lon = rs.getDouble(4);
				double lat = rs.getDouble(5);
				double av = rs.getDouble(6);
				int dur = rs.getInt(7);
				int ty = rs.getInt(8);
				if (dur < durThreshSec)
					continue;
				if (!((ty == 0 && (mask & G_MASK_L) > 0)
					|| (ty == 2 && (mask & G_MASK_L2U) > 0)
					|| (ty == 2 && (mask & G_MASK_U) > 0)
					|| (ty == 3 && (mask & G_MASK_U2L) > 0)
					))
					continue;
				addInThis.third.add(new TripPointInfo(ts, lon,lat,name,dur, (int) (av*1.05)));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return;
		}
	}

}

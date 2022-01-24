package com.ipssi.mining;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.ipssi.dispatchoptimization.vo.DumperInfoVo;
import com.ipssi.dispatchoptimization.vo.LoadSiteVO;
import com.ipssi.dispatchoptimization.vo.OperatorDashboardMU;
import com.ipssi.dispatchoptimization.vo.RouteVo;
import com.ipssi.dispatchoptimization.vo.ShovelInfoVo;
import com.ipssi.dispatchoptimization.vo.UnloadSiteVo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.miningOpt.SiteStats;

public class DOSInitialAssignmentDao {
	
	public static void updateRelatedDims(int refDynDimId[]) {
	//	int refDynDimId[]={82421,82420};//Load and Unload Site
		for (int i = 0; i < refDynDimId.length; i++) {
				DimInfo refDim = DimInfo.getDimInfo((Integer) refDynDimId[i]);
				if (refDim != null)
					refDim.makeDirty();
		}
	}
	public static double getAvgDumperTypeCapacity(SessionManager session){
		
		try {
			Connection conn=session.getConnection();
			PreparedStatement ps = conn.prepareStatement("select avg(capacity_vol) as capacity_vol from vehicle_types where vehicle_cat=0");
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return Misc.getRsetInt(rs, "capacity_vol");
			}
			ps = Misc.closePS(ps);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 19;
	}
	public static boolean updateShiftTargetAssigmentStatus(int shiftTargetId,SessionManager session,int status){
		try {
			Connection conn=session.getConnection();
			PreparedStatement ps = conn.prepareStatement("update dos_shift_target set is_live=? where id=? ");
			ps.setInt(1, status);
			ps.setInt(2, shiftTargetId);
			ps.executeUpdate();
			ps = Misc.closePS(ps);
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void loadOPDashBasicStructure(SessionManager session) throws Exception {
		PreparedStatement ps =null;
		try {
			Connection conn=session.getConnection();
			ps = conn.prepareStatement("select * from dos_inventory_piles where pile_type=1 and status=1");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LoadSiteVO lv = new LoadSiteVO();
				lv.setId(Misc.getRsetInt(rs, "id"));
				lv.setName(Misc.getRsetString(rs, "short_code"));
				lv.setPitId(Misc.getRsetInt(rs, "pit_id"));
				lv.setDifficulty(Misc.getRsetDouble(rs, "op_difficulty"));
				lv.setAvgCycleTime(Misc.getRsetDouble(rs, "avg_cycle_time"));
				lv.setClearingCycleTime(Misc.getRsetDouble(rs, "clearing_cycle_time"));
				//lv.setClearingTime(Misc.getRsetDouble(rs, "vehicle_id"));
				lv.setPositioningTime(Misc.getRsetDouble(rs, "positioning_time"));
				OperatorDashboardMU.setLoadSite(lv);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
			ps = conn.prepareStatement("select * from dos_inventory_piles where pile_type=2 and status=1");
			rs = ps.executeQuery();
			while (rs.next()) {
				UnloadSiteVo lv = new UnloadSiteVo();
				lv.setId(Misc.getRsetInt(rs, "id"));
				lv.setName(Misc.getRsetString(rs, "short_code"));
				lv.setPitId(Misc.getRsetInt(rs, "pit_id"));
				lv.setDifficulty(Misc.getRsetDouble(rs, "op_difficulty"));
				lv.setAvgCycleTime(Misc.getRsetDouble(rs, "avg_cycle_time"));
				lv.setClearingCycleTime(Misc.getRsetDouble(rs, "clearing_cycle_time"));
				//lv.setClearingTime(Misc.getRsetDouble(rs, "vehicle_id"));
				lv.setPositioningTime(Misc.getRsetDouble(rs, "positioning_time"));
				OperatorDashboardMU.setUnloadSite(lv);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
			ps = conn.prepareStatement("select * from dos_route_def where status=1");
			rs = ps.executeQuery();
			while (rs.next()) {
				RouteVo rv = new RouteVo();
				rv.setRouteId(Misc.getRsetInt(rs, "id"));
				rv.setDifficulty(Misc.getRsetInt(rs, "difficulty"));
				rv.setDistance(Misc.getRsetInt(rs, "dist"));
				rv.setDistSrc(Misc.getRsetInt(rs, "src_of_dist"));
				rv.setLoadSite(OperatorDashboardMU.getLoadSite(Misc.getRsetInt(rs, "site_id")));
				rv.setUnloadSite(OperatorDashboardMU.getUnLoadSite(Misc.getRsetInt(rs, "dest_id")));
				OperatorDashboardMU.setRoute(rv);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
			ps = conn.prepareStatement("select id,name from vehicle where status=1 and type in(65)");
			rs = ps.executeQuery();
			while (rs.next()) {
				ShovelInfoVo rv = new ShovelInfoVo();
				rv.setId(Misc.getRsetInt(rs, "id"));
				rv.setName(Misc.getRsetString(rs, "name"));
				OperatorDashboardMU.setShovel(rv);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
			ps = conn.prepareStatement("select id,name from vehicle where status=1 and type in(3)");
			rs = ps.executeQuery();
			while (rs.next()) {
				DumperInfoVo rv = new DumperInfoVo();
				rv.setId(Misc.getRsetInt(rs, "id"));
				rv.setName(Misc.getRsetString(rs, "name"));
				OperatorDashboardMU.setDumper(rv);
			}
			Misc.closePS(ps);
			Misc.closeRS(rs);
			ps = conn.prepareStatement("select dumper_id,shovel_id from dos_shift_plan_assignments spa where plan_status=1 order by shovel_id");
			rs = ps.executeQuery();
			int prvShovelId=0;
			ArrayList<Integer> l=new ArrayList<Integer>();
			while (rs.next()) {
				int shovelId = Misc.getRsetInt(rs, "shovel_id");
				if (prvShovelId != 0 && shovelId != prvShovelId) {
					OperatorDashboardMU.addAssignedDumperListToShovel(prvShovelId, l);
					l = new ArrayList<Integer>();
				}
				l.add(Misc.getRsetInt(rs, "dumper_id"));
				prvShovelId = shovelId;
			}
			ArrayList<Integer> chk=OperatorDashboardMU.getAssignedDumpersForShovel(prvShovelId);
			if(chk==null || chk.size()<=0)
				OperatorDashboardMU.addAssignedDumperListToShovel(prvShovelId, l);
			Misc.closePS(ps);
			Misc.closeRS(rs);
			
			
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			Misc.closePS(ps);
		}
		
	}
	
	public static boolean updateShiftTargetLiveStatus(int shiftTargetId,SessionManager session){
		try {
			Connection conn=session.getConnection();
			PreparedStatement ps = conn.prepareStatement("update dos_shift_target set is_live=3 ,assignment_end_time=now() where is_live=1");
			ps.executeUpdate();
			ps = Misc.closePS(ps);
			ps = conn.prepareStatement("update dos_shift_target set is_live=1,assignment_live_time=now() where id="+shiftTargetId);
			ps.executeUpdate();
			ps = Misc.closePS(ps);
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static ResultSet getShiftTarget(int shiftTargetId,SessionManager session) throws Exception{
		try {
			Connection conn=session.getConnection();
			
			PreparedStatement ps = conn.prepareStatement("select * from dos_shift_target where id=? ");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
   		
	 	return rs;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	public static ResultSet getRouteTarget(int shiftTargetId,SessionManager session) throws Exception{
		try {
			Connection conn=session.getConnection();
			
			PreparedStatement ps = conn.prepareStatement("select * from dos_route_target st join dos_route_def def on (st.route_id=def.id) where st.shift_target_id=? ");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
   		
	 	return rs;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	public static ResultSet getWhatIfDataSet(int shiftTargetId,SessionManager session) throws Exception{
		try {
			Connection conn=session.getConnection();
			
			PreparedStatement ps = conn.prepareStatement("select  st.prod_hours ,st.addnl_delay,st.unload_time,st.clean_time , spa.inv_pile,spa.unload_site,fwd_difficulty,bkwrd_difficulty,lead_load, count(distinct spa.shovel_id) as cnt_shvl,scat.name as scat_name,scat.capacity_vol as scat_vol,count(distinct spa.dumper_id) as  cnt_dum,dcat.name as dcat_name,dcat.capacity_vol as dcat_vol,dcat.cycle_time_second as dumper_speed, stv.avg_cycle_time,stv.avg_fill_factor,sum(dumper_usage_percentage)as per,dcat.id as dcat,scat.id as scat from dos_shift_plan_assignments spa left outer join vehicle sh on (spa.shovel_id=sh.id) left outer join vehicle dum on (spa.dumper_id=dum.id) left outer join vehicle_types scat on (scat.vehicle_type_lov=sh.type) left outer join vehicle_types dcat on (dcat.vehicle_type_lov=dum.type) left outer join dos_route_target rt on (rt.route_id=spa.route_id and spa.shift_target_id=rt.shift_target_id) left outer join dos_shift_target_vehicle stv on (stv.vehicle_id=spa.shovel_id and spa.shift_target_id=stv.shift_target_id) left outer join dos_shift_target st on (spa.shift_target_id=st.id) where spa.shift_target_id=? group by inv_pile,unload_site,scat.id,dcat.id");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
	 	return rs;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}	
	
	public static ResultSet getWhatIfDataSetSave(int shiftTargetId,SessionManager session) throws Exception{
		try {
			Connection conn=session.getConnection();
			PreparedStatement ps = conn.prepareStatement("select init_shovel_count,init_dumper_count,spst.cycle_time,sp.fill_factor,spa.route_id,spa.shift_target_id,st.prod_hours ,st.addnl_delay,st.unload_time,st.clean_time , spa.inv_pile,spa.route_id,spa.unload_site,shovel_cat as scat, dumper_cat as dcat, shovel_count as cnt_shvl,dumper_count as  cnt_dum,dcat.name as dcat_name,dcat.capacity_vol as dcat_vol,dcat.cycle_time_second as dumper_speed, scat.name as scat_name,scat.capacity_vol as scat_vol,rt.fwd_difficulty,rt.bkwrd_difficulty,rt.lead_load,avg_cycle_time,avg_fill_factor from  dos_interactive_assignment spa left outer join dos_route_target rt on (rt.route_id=spa.route_id and spa.shift_target_id=rt.shift_target_id)  left outer join dos_shift_target st on (spa.shift_target_id=st.id) left outer join vehicle_types scat on (scat.id=spa.shovel_cat)  left outer join vehicle_types dcat on (dcat.id=spa.dumper_cat) left outer join dos_r_target_site_params sp on (sp.shift_target_id=spa.shift_target_id and sp.load_site=spa.inv_pile) left outer join dos_load_site_shoveltypes_params spst on (spst.target_site_param_id=sp.id) where spa.shift_target_id=? and spa.status=1 order by spa.route_id");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
	 	return rs;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}	
	public static boolean saveWhatIfAssignment(int portNodeId, SessionManager session, HttpServletRequest request)throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			int shiftTargetId = Misc.getParamAsInt(session.getParameter("shift_target_id"));
			conn = session.getConnection();
			ps = conn.prepareStatement("update dos_interactive_assignment set shovel_count=?,dumper_count=? where shift_target_id=? and inv_pile=? and unload_site=? and route_id=?");
			int stCnt=Misc.getParamAsInt(session.getParameter("rowCount"));stCnt--;
		    while (stCnt > 0) {
				ps.setDouble(1, Misc.getParamAsDouble(session.getParameter("p_num_svl_" + stCnt)));
				ps.setDouble(2, Misc.getParamAsDouble(session.getParameter("p_num_dum_" + stCnt)));
				ps.setInt(3, shiftTargetId);
				ps.setInt(4, Misc.getParamAsInt(session.getParameter("ls_"+ stCnt)));
				ps.setInt(5, Misc.getParamAsInt(session.getParameter("uls_"+ stCnt)));
				ps.setInt(6, Misc.getParamAsInt(session.getParameter("route_id_" + stCnt)));
				ps.executeUpdate();
				stCnt--;
			}
		    ps = Misc.closePS(ps);
		    return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static ArrayList<Integer> getDumperList(int shiftTargetId,SessionManager session) throws Exception{
		ArrayList<Integer> dumperList=new ArrayList<Integer>();
		PreparedStatement ps =null;
		try {
			Connection conn=session.getConnection();
			
			ps = conn.prepareStatement("select * from dos_shift_target_vehicle where shift_target_id=? and type in (3,4)");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
   		
	 	while( rs.next()){
	 		dumperList.add(Misc.getRsetInt(rs, "vehicle_id"));
	 	}
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			Misc.closePS(ps);
		}
		
		return dumperList;
	}
	
	public static ResultSet getDOSAssignment(int shiftTargetId,SessionManager session) throws Exception{
		try {
			Connection conn=session.getConnection();
			PreparedStatement ps = conn.prepareStatement("select st.nick_name , pa.* from dos_shift_plan_assignments pa left join dos_shift_target st on (pa.shift_target_id=st.id) where shift_target_id=? ");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
   		
	 	return rs;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	public static ResultSet getSiteParamData(int shiftTargetId,SessionManager session,int siteType) throws Exception{
		try {
			Connection conn=session.getConnection();
			
			PreparedStatement ps = conn.prepareStatement("select * from dos_r_target_site_params where shift_target_id=? and site_type=?");
			ps.setInt(1, shiftTargetId);
			ps.setInt(2, siteType);
			ResultSet rs = ps.executeQuery();
   		
	 	return rs;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	public static ArrayList<RouteVo> getRouteList(ArrayList<DimInfo.ValInfo> routeIdList ,SessionManager session,int pit,String [] loadSites) throws Exception{
//		ArrayList<RouteVo> retVal=new ArrayList<RouteVo>(routeIdList!=null?routeIdList.size():10);
		Set<String> lsSET = new HashSet<String>(Arrays.asList( loadSites!=null?loadSites: new String[] {""}));
		StringBuilder rInlist=new StringBuilder();
		for (DimInfo.ValInfo val: routeIdList) 
			rInlist.append(val.m_id).append(",");
		StringBuilder lsInlist=new StringBuilder();
		for (int i = 0; i < (loadSites!=null?loadSites.length:0); i++){
			if(!"-- Select Multiple Load Sites --".equalsIgnoreCase(loadSites[i]))
			lsInlist.append(loadSites[i]).append(",");
		}
//		rInlist.substring(0, rInlist.length()-1);
//		lsInlist.substring(0, lsInlist.length()-1);
//		ArrayList<RouteVo> dbRouteIdList=new ArrayList<RouteVo>();
		return getRoutes(rInlist.substring(0, rInlist.length()-1) , session,pit,lsInlist.length()>0?lsInlist.substring(0, lsInlist.length()-1):"");
//		for (DimInfo.ValInfo valInf: routeIdList) {
//				RouteVo vo=getRoutestRoute(valInf.m_id , session);
//				if(vo.getPitId()==pit && lsSET.contains(vo.getLoadSite().getId()+""))
//					retVal.add(vo);
//			}
//		return retVal;
		
	}
	private static ArrayList<RouteVo> getRoutes(String inlist,SessionManager session, int pit,String lsInlist) throws Exception {
		ArrayList<RouteVo> retVal=new ArrayList<RouteVo>();
		try {
			Connection conn=session.getConnection();
			StringBuilder qry=new StringBuilder();
			qry.append("select id,pit_id,site_id,dest_id from dos_route_def where id in ("+inlist+")  and  status=1 " );
			if(pit>0)
				qry.append(" and pit_id="+pit);
			if(lsInlist!=null && lsInlist.length()>0)
				qry.append(" and site_id in ("+lsInlist+")");
			
			
			PreparedStatement ps = conn.prepareStatement(qry.toString());
//			ps.setInt(1, routeId);
			ResultSet rs = ps.executeQuery();
//			RouteVo dr=null;
   		 while (rs.next()) {
   			RouteVo dr = new RouteVo();
   			dr.setRouteId(Misc.getRsetInt(rs, 1));
   			dr.setPitId(Misc.getRsetInt(rs, 2));
   			LoadSiteVO lsVO = new LoadSiteVO();
   			lsVO.setId(Misc.getRsetInt(rs, 3));
   			dr.setLoadSite(lsVO);
   			UnloadSiteVo ulsVO = new UnloadSiteVo();
   			ulsVO.setId(Misc.getRsetInt(rs, 4));
   			dr.setUnloadSite(ulsVO);
   			retVal.add(dr);
   		 }
   		 rs = Misc.closeRS(rs);
   		 ps = Misc.closePS(ps);
//   		session.setAttributeObj("routeObj",dr);
	 	return retVal;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static ArrayList<ValInfo> removeExtraLSites(ArrayList<DimInfo.ValInfo> loadSiteList,String [] loadSites) throws Exception{
		if(loadSites==null)
			return loadSiteList;
//		String [] newLoadSites=new String[]();
//		for (int i = 0; i < (loadSites!=null?loadSites.length:0); i++){
//		if(!"-- Select Multiple Load Sites --".equalsIgnoreCase(loadSites[i]))
//			lsInlist.append(loadSites[i]).append(",");
		ArrayList<DimInfo.ValInfo> retVal=new ArrayList<DimInfo.ValInfo>();
		Set<String> lsSET = new HashSet<String>(Arrays.asList(loadSites));
		lsSET.remove("-- Select Multiple Load Sites --");
		//Arrays.asList( loadSites!=null?loadSites: new String[] {""}));
		for (DimInfo.ValInfo valInf: loadSiteList) {
			int ls_id = valInf.m_id;
				if(lsSET.contains(ls_id+""))
					retVal.add(valInf);
			}
		return retVal;
		
	}
	public static double roundDown2(double d) {
	    return ((long)(d * 1e2)) / 1e2;
	}
	public static double roundDown3(double d) {
	    return ((long)(d * 1e3)) / 1e3;
	}
	public static double roundDown1(double d) {
	    return ((long)(d * 1e1)) / 1e1;
	}
	
	public static ArrayList<ValInfo> removeExtraULSites(ArrayList<DimInfo.ValInfo> uloadSiteList,ArrayList<RouteVo> routes) throws Exception{
		ArrayList<DimInfo.ValInfo> retVal=new ArrayList<DimInfo.ValInfo>();
		for (DimInfo.ValInfo valInf: uloadSiteList) {
			int uls_id = valInf.m_id;
				if(checkUnloadSite(uls_id,routes))
					retVal.add(valInf);
			}
		return retVal;
		
	}
	private static boolean checkUnloadSite(int uls_id, ArrayList<RouteVo> routes) {
		for (RouteVo routeVo : routes) {
			 if(routeVo.getUnloadSite().getId()==uls_id)
				 return true;
		}
		return false;
	}
	public static int getPitId(int shiftTargetId, SessionManager session)
			throws Exception {
		int pit_id = Misc.UNDEF_VALUE;
		try {
			Connection conn = session.getConnection();
			PreparedStatement ps = conn.prepareStatement("select pit_id from dos_shift_target where id=?");
			ps.setInt(1, shiftTargetId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				pit_id = Misc.getRsetInt(rs, 1);
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
			return pit_id;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
	
	public static RouteVo getRoute(int routeId ,SessionManager session) throws Exception{
		try {
			Connection conn=session.getConnection();
			PreparedStatement ps = conn.prepareStatement("select id,pit_id,site_id,dest_id from dos_route_def where id=? and  status=1");
			ps.setInt(1, routeId);
			ResultSet rs = ps.executeQuery();
			RouteVo dr=null;
   		 if (rs.next()) {
   			 dr = new RouteVo();
   			dr.setRouteId(Misc.getRsetInt(rs, 1));
   			dr.setPitId(Misc.getRsetInt(rs, 2));
   			LoadSiteVO lsVO = new LoadSiteVO();
   			lsVO.setId(Misc.getRsetInt(rs, 3));
   			dr.setLoadSite(lsVO);
   			UnloadSiteVo ulsVO = new UnloadSiteVo();
   			ulsVO.setId(Misc.getRsetInt(rs, 4));
   			dr.setUnloadSite(ulsVO);
   		 }
   		 rs = Misc.closeRS(rs);
   		 ps = Misc.closePS(ps);
   		session.setAttributeObj("routeObj",dr);
	 	return dr;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	
	public static String saveTargets(int portNodeId,SessionManager session,HttpServletRequest request) throws Exception{
		Connection conn=null;
		PreparedStatement ps =null;
		SiteStats working =null;
		try{
			try {
				working = new SiteStats(conn,-1,false,true, true);
				if(working!=null)working.loadUopEtc(conn, portNodeId);
			} catch (Exception e) {
				System.err.println("Getting exception While initializing Stats..working.loadUopEtc(conn, portNodeId)");
			}
			
			
//			int routeId= Misc.getParamAsInt(session.getParameter("sel_route_id"));
			int shiftId=Misc.getParamAsInt(session.getParameter("shift_id"));
			List<String> dumperList=(List<String>)Arrays.asList((request.getParameterValues("dumper_List")==null?new String[0]:request.getParameterValues("dumper_List")));
			List<String> shovelList=(List<String>)Arrays.asList((request.getParameterValues("shovel_List")==null?new String[0]:request.getParameterValues("shovel_List")));
			List<String> routeList=(List<String>)Arrays.asList((request.getParameterValues("route_List")==null?new String[0]:request.getParameterValues("route_List")));
			List<String> loadSiteList=(List<String>)Arrays.asList((request.getParameterValues("lsite_id")==null?new String[0]:request.getParameterValues("lsite_id")));
			List<String> unLoadSiteList=(List<String>)Arrays.asList((request.getParameterValues("ulsite_id")==null?new String[0]:request.getParameterValues("ulsite_id")));
			
		conn=session.getConnection();
		
		
		ps = conn.prepareStatement("insert into dos_shift_target(shift_id,min_qty,max_qty,shift_date,status,port_node_id,total_dumpers," +
				"total_shovels,prod_hours,addnl_delay,unload_time,clean_time,nick_name,pit_id,is_live,update_date,created_on) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,now(),now())");
		ps.setInt(1,shiftId );
		ps.setInt(2, Misc.getParamAsInt(session.getParameter("min_qty"),0));
		ps.setInt(3, Misc.getParamAsInt(session.getParameter("max_qty"),0));
		String dateStr=session.getParameter("shift_date")==null?new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT).format(new Date()):session.getParameter("shift_date");
		Misc.setParamDate(ps,4,new java.sql.Date(Misc.getParamAsDate(dateStr, null, new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT)).getTime()));
		ps.setInt(5, Misc.getParamAsInt(session.getParameter("status_id")));
		ps.setInt(6, portNodeId);
		ps.setInt(7, dumperList.size());//tot_d
		ps.setInt(8, shovelList.size());//tot_s
		ps.setDouble(9, Misc.getParamAsDouble(session.getParameter("prod_hours"),0));
		ps.setDouble(10, Misc.getParamAsDouble(session.getParameter("addnl_delay"),0));
		ps.setDouble(11, Misc.getParamAsDouble(session.getParameter("unload_time"),0));
		ps.setDouble(12, Misc.getParamAsDouble(session.getParameter("clean_time"),0));
		ps.setString(13, session.getParameter("nick_name"));
		ps.setInt(14, Misc.getParamAsInt(session.getParameter("sel_pit_id")));
		ps.executeUpdate();
		int shift_target_id=0;
		ResultSet rs = ps.getGeneratedKeys();
        if(rs != null && rs.next()){
        	shift_target_id=rs.getInt(1);
        }
        rs.close();
        ps = Misc.closePS(ps);
        
        System.out.println("created dos_shift_target id="+shift_target_id);
        
        
		int minQty=0;
		int maxQty=0;
		ps = conn.prepareStatement("insert into dos_route_target(shift_target_id,shift_id,route_id,min_qty,max_qty,fwd_difficulty,bkwrd_difficulty,lead_load,update_date) values (?,?,?,?,?,?,?,?,now())");
		for (String count : routeList) {
			int routeId = Misc.getParamAsInt(session.getParameter("sel_route_id_"+count));
			ps.setInt(1,shift_target_id);
			ps.setInt(2,shiftId);
			ps.setInt(3, routeId);
			/*Now getting it on jsp page
			 * 
			 * double fwdDif= Misc.getUndefDouble();
			double bkwrdDif=Misc.getUndefDouble();	
			try{
				SiteStats working = new SiteStats(true, true);
				RouteVo r = getRoute(routeId , session);//OperatorDashboardMU.getRoute( Misc.getParamAsInt(session.getParameter("sel_route_id_"+count)));
				SiteStats.Stats stats = working.getStatsForInvPile( r!=null?r.getLoadSite().getId():0, conn, portNodeId, System.currentTimeMillis());
				fwdDif=stats.getForwRouteDifficulty(r != null && r.getLoadSite() != null ? r.getLoadSite().getId() : Misc.getUndefInt());
				bkwrdDif=stats.getBackRouteDifficulty(r != null && r.getLoadSite() != null ? r.getLoadSite().getId() : Misc.getUndefInt());
			}catch(Exception e){
				e.printStackTrace();
			}
			*/
			ps.setInt(4, Misc.getParamAsInt(session.getParameter("r_min_qty_"+count),0));
			ps.setInt(5, Misc.getParamAsInt(session.getParameter("r_max_qty_"+count),0));
			ps.setDouble(6,  Misc.getParamAsDouble(session.getParameter("fwd_diff_"+count),0));
			ps.setDouble(7,  Misc.getParamAsDouble(session.getParameter("bkwrd_diff_"+count),0));
			ps.setDouble(8,  Misc.getParamAsDouble(session.getParameter("lead_load_"+count),0));
			
			ps.addBatch();
		}
		ps.executeBatch();
		ps = Misc.closePS(ps);
		
		 System.out.println("created dos_route_target total count="+routeList.size());
		 
		
		ps = conn.prepareStatement("delete from dos_shift_target_vehicle where shift_target_id=?");
		ps.setInt(1, shift_target_id);
		ps.executeUpdate();
		ps = Misc.closePS(ps);
		
		//Insert load site parameters
		PreparedStatement psLS = conn.prepareStatement("insert into dos_r_target_site_params(shift_target_id,load_site ,site_type,fill_factor,extra_1 ,extra_2,min_qty,max_qty,update_date) values (?,?,?,?,?,?,?,?,now())");
		PreparedStatement psST = conn.prepareStatement("insert into dos_load_site_shoveltypes_params(target_site_param_id,shovel_type  ,cycle_time ,extra_1 ,extra_2,extra_3,update_date) values (?,?,?,?,?,?,now())");
		for (String siteId : loadSiteList) {
			psLS.setInt(1,shift_target_id);
			psLS.setInt(2,Misc.getParamAsInt(siteId));
			psLS.setInt(3,1);//PILE TYPE == 1 LoadSite, 2- Unload Site
			psLS.setDouble(4, Misc.getParamAsDouble(session.getParameter("l_fill_factor_"+siteId),0));
			psLS.setInt(5, Misc.getParamAsInt(session.getParameter("extra_1_"+siteId),0));
			psLS.setDouble(6, Misc.getParamAsDouble(session.getParameter("extra_2_"+siteId),0));
			psLS.setInt(7, Misc.getParamAsInt(session.getParameter("l_min_qty_"+siteId),0));
			psLS.setInt(8, Misc.getParamAsInt(session.getParameter("l_max_qty_"+siteId),0));
			minQty+=Misc.getParamAsInt(session.getParameter("l_min_qty_"+siteId),0);
			maxQty+=Misc.getParamAsInt(session.getParameter("l_max_qty_"+siteId),0);
			psLS.executeUpdate();
			int target_site_param_id=0;
			ResultSet rsLS = psLS.getGeneratedKeys();
//	        if(rs != null && rsLS.next()){
	        if(rsLS.next()){
	        	target_site_param_id=rsLS.getInt(1);
	        }
	        //rs.close();
	        //psLS = Misc.closePS(psLS);
	      //Insert load site Shovel type parameters
	        int stCnt=Misc.getParamAsInt(session.getParameter("lsiteST_Count_"+siteId))-1;
	        while(stCnt>=0) {
	        	psST.setInt(1,target_site_param_id);
	        	psST.setInt(2,Misc.getParamAsInt(session.getParameter("shovel_type_"+siteId+"_"+stCnt)));
	        	psST.setDouble(3, Misc.getParamAsDouble(session.getParameter("l_st_cyc_time_"+siteId+"_"+stCnt),0));
	        	psST.setInt(4, Misc.getParamAsInt(session.getParameter("extra_1_"+siteId+"_"+stCnt)));
	        	psST.setInt(5, Misc.getParamAsInt(session.getParameter("extra_2_"+siteId+"_"+stCnt)));
	        	psST.setDouble(6, Misc.getParamAsDouble(session.getParameter("extra_3_"+siteId+"_"+stCnt)));
	        	psST.addBatch();
				stCnt--;
			}
	        
		}
		psST.executeBatch();
        psST = Misc.closePS(psST);
		
        
        ps = conn.prepareStatement("update dos_shift_target set min_qty=? ,max_qty=? where id=?");
		ps.setInt(1,minQty);
		ps.setInt(2,maxQty);
		ps.setInt(3,shift_target_id);
		ps.executeUpdate();
		ps = Misc.closePS(ps);
		
		
        //for Unload sites
        ps= conn.prepareStatement("insert into dos_r_target_site_params(shift_target_id,load_site, site_type,min_qty,max_qty,update_date) values (?,?,?,?,?,now())");
		for (String siteId : unLoadSiteList) {
			ps.setInt(1,shift_target_id);
			ps.setInt(2,Misc.getParamAsInt(siteId));
			ps.setInt(3,2);//PILE TYPE == 1 LoadSite, 2- Unload Site
			ps.setInt(4, Misc.getParamAsInt(session.getParameter("ul_min_qty_"+siteId),0));
			ps.setInt(5, Misc.getParamAsInt(session.getParameter("ul_max_qty_"+siteId),0));
			ps.addBatch();
		}
		ps.executeBatch();
        ps = Misc.closePS(ps);
        
        
        
		ps = conn.prepareStatement("insert into dos_shift_target_vehicle(shift_target_id,shift_id,vehicle_id,type,update_date) values (?,?,?,?,now())");
		for (String dumperId : dumperList) {
			ps.setInt(1,shift_target_id);
			ps.setInt(2, shiftId);
			ps.setInt(3,Misc.getParamAsInt(dumperId));
			ps.setInt(4,4);//type-4 is dumper and 65 is shovel
			ps.addBatch();
		}
		ps.executeBatch();
		ps = Misc.closePS(ps);
		
		ps = conn.prepareStatement("insert into dos_shift_target_vehicle(shift_target_id,shift_id,vehicle_id,type,curr_pos,is_on_dig_site,allow_redeploy,avg_cycle_time,avg_fill_factor,update_date) values (?,?,?,?,?,?,?,?,?,now())");
		//PreparedStatement psShovelParam = conn.prepareStatement("insert into dos_target_shovel_params(shift_target_id,shovel_id,dest_id,fwd_difficulty,bkwrd_difficulty,lead_load,update_date) values (?,?,?,?,?,?,now())");
		for (String shovelId : shovelList) {
			ps.setInt(1,shift_target_id);
			ps.setInt(2, shiftId);
			ps.setInt(3,Misc.getParamAsInt(shovelId));
			ps.setInt(4,65);//type-4 is dumper and 65 is shovel
			ps.setInt(5, Misc.getParamAsInt(session.getParameter("s_pit_id_"+shovelId)));
			ps.setInt(6,session.getParameter("s_dig_site_"+shovelId)==null?0:1);
			ps.setInt(7,session.getParameter("s_allow_redeploy_"+shovelId)==null?0:1);
			ps.setDouble(8,Misc.getParamAsDouble(session.getParameter("s_cycle_time_"+shovelId),0));
			ps.setDouble(9,Misc.getParamAsDouble(session.getParameter("s_avg_fillfactor_"+shovelId),0));
			
			ps.addBatch();
			/** As we are saving shovel type details in dos_load_site_shoveltypes_params
			double fwdDif= Misc.getUndefDouble();
			double bkwrdDif=Misc.getUndefDouble();	
			double loadLead=Misc.getUndefDouble();	
			try{
				
				ArrayList<Integer> destIdList = working!=null?working.getDestIdList():new ArrayList<Integer>(); 
				SiteStats.Stats stats = working.getStatsForShovel(Misc.getParamAsInt(shovelId), conn, portNodeId);
				//Shovel Parameters
				for(Integer destId: destIdList){
					fwdDif=stats.getForwRouteDifficulty(destId);
					bkwrdDif=stats.getBackRouteDifficulty(destId);
					loadLead=stats.getDestLeadDist(destId);
					psShovelParam.setInt(1,shift_target_id);
					psShovelParam.setInt(2, Misc.getParamAsInt(shovelId));
					psShovelParam.setDouble(3,destId);
					psShovelParam.setDouble(4,fwdDif);
					psShovelParam.setDouble(5, bkwrdDif);
					psShovelParam.setDouble(6,loadLead);
					psShovelParam.addBatch();
				}
				psShovelParam.executeBatch();
				Misc.closePS(psShovelParam);
			}catch(Exception e){
				e.printStackTrace();
			}
			**/
		}
		ps.executeBatch();
		ps = Misc.closePS(ps);
		 System.out.println("created  dos_shift_target_vehicle total Dumpers=["+dumperList.size()+"] Shovels["+shovelList.size()+"]");
		if (!conn.getAutoCommit())
			conn.commit();
		
	}catch (Exception e) {
		e.printStackTrace();
		return "Shift Target not saved. Try Again Later."+"["+e.getMessage()+"]";
		//throw e;
	}
	return "Shift Target Saved Successfully.";
	}
	
	
	
	public static String updateTargets(int portNodeId,SessionManager session,HttpServletRequest request,int shift_target_id) throws Exception{
		Connection conn=null;
		PreparedStatement ps =null;
		SiteStats working =null;
		try{
//			try {
//				working = new SiteStats(conn,-1,false,true, true);
//				if(working!=null)working.loadUopEtc(conn, portNodeId);
//			} catch (Exception e) {
//				System.err.println("Getting exception While initializing Stats..working.loadUopEtc(conn, portNodeId)");
//			}
			
			
//			int routeId= Misc.getParamAsInt(session.getParameter("sel_route_id"));
//			int shiftId=Misc.getParamAsInt(session.getParameter("shift_id"));
//			List<String> dumperList=(List<String>)Arrays.asList((request.getParameterValues("dumper_List")==null?new String[0]:request.getParameterValues("dumper_List")));
//			List<String> shovelList=(List<String>)Arrays.asList((request.getParameterValues("shovel_List")==null?new String[0]:request.getParameterValues("shovel_List")));
			List<String> routeList=(List<String>)Arrays.asList((request.getParameterValues("route_List")==null?new String[0]:request.getParameterValues("route_List")));
			List<String> loadSiteList=(List<String>)Arrays.asList((request.getParameterValues("lsite_id")==null?new String[0]:request.getParameterValues("lsite_id")));
			List<String> unLoadSiteList=(List<String>)Arrays.asList((request.getParameterValues("ulsite_id")==null?new String[0]:request.getParameterValues("ulsite_id")));
			
		conn=session.getConnection();
		
		
		ps = conn.prepareStatement("update dos_shift_target set shift_id=?,shift_date=?," +
				"prod_hours=?,addnl_delay=?,unload_time=?,clean_time=?,is_live=6,update_date=now()" +
				" where id=?");
		ps.setInt(1,Misc.getParamAsInt(session.getParameter("shift_id")) );
		String dateStr=session.getParameter("shift_date")==null?new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT).format(new Date()):session.getParameter("shift_date");
		Misc.setParamDate(ps,2,new java.sql.Date(Misc.getParamAsDate(dateStr, null, new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT)).getTime()));
		ps.setDouble(3, Misc.getParamAsDouble(session.getParameter("prod_hours"),0));
		ps.setDouble(4, Misc.getParamAsDouble(session.getParameter("addnl_delay"),0));
		ps.setDouble(5, Misc.getParamAsDouble(session.getParameter("unload_time"),0));
		ps.setDouble(6, Misc.getParamAsDouble(session.getParameter("clean_time"),0));
		ps.setInt(7, shift_target_id);
		ps.executeUpdate();

        System.out.println("updated dos_shift_target id="+shift_target_id);
        
		int minQty=0;
		int maxQty=0;
		//shift_target_id,shift_id,route_id,
		ps = conn.prepareStatement("update dos_route_target set fwd_difficulty=?,bkwrd_difficulty=?,lead_load=?,update_date=now()" +
				" where shift_target_id=? and route_id=?");
		for (String count : routeList) {
			int routeId = Misc.getParamAsInt(session.getParameter("sel_route_id_"+count));
			ps.setDouble(1,  Misc.getParamAsDouble(session.getParameter("fwd_difficulty_"+count),0));
			ps.setDouble(2,  Misc.getParamAsDouble(session.getParameter("bkwrd_difficulty_"+count),0));
			ps.setDouble(3,  Misc.getParamAsDouble(session.getParameter("lead_load_"+count),0));
			ps.setInt(4,shift_target_id);
			ps.setInt(5, routeId);
			ps.addBatch();
		}
		ps.executeBatch();
		ps = Misc.closePS(ps);
		
		 System.out.println("Updated dos_route_target total count="+routeList.size());
		 
		//Update load site parameters
		PreparedStatement psLS = conn.prepareStatement("update dos_r_target_site_params set fill_factor=? ,min_qty=?,max_qty=?,update_date=now()" +
				" where shift_target_id=? and load_site=?");
		for (String siteId : loadSiteList) {
			psLS.setDouble(1, Misc.getParamAsDouble(session.getParameter("l_fill_factor_"+siteId),0));
			psLS.setInt(2, Misc.getParamAsInt(session.getParameter("l_min_qty_"+siteId),0));
			psLS.setInt(3, Misc.getParamAsInt(session.getParameter("l_max_qty_"+siteId),0));
			psLS.setInt(4,shift_target_id);
			psLS.setInt(5,Misc.getParamAsInt(siteId));
			psLS.executeUpdate();
			minQty+=Misc.getParamAsInt(session.getParameter("l_min_qty_"+siteId),0);
			maxQty+=Misc.getParamAsInt(session.getParameter("l_max_qty_"+siteId),0);
		}
//		psLS.executeBatch();
		psLS = Misc.closePS(psLS);
        
        ps = conn.prepareStatement("update dos_shift_target set min_qty=? ,max_qty=? where id=?");
		ps.setInt(1,minQty);
		ps.setInt(2,maxQty);
		ps.setInt(3,shift_target_id);
		ps.executeUpdate();
		ps = Misc.closePS(ps);
		
        //for Unload sites
        ps= conn.prepareStatement("update dos_r_target_site_params set min_qty=?,max_qty=?,update_date=now()" +
		" where shift_target_id=? and load_site=?");
		for (String siteId : unLoadSiteList) {
			ps.setInt(1, Misc.getParamAsInt(session.getParameter("ul_min_qty_"+siteId),0));
			ps.setInt(2, Misc.getParamAsInt(session.getParameter("ul_max_qty_"+siteId),0));
			ps.setInt(3,shift_target_id);
			ps.setInt(4,Misc.getParamAsInt(siteId));
			ps.executeUpdate();
		}
		ps.executeBatch();
        ps = Misc.closePS(ps);
		if (!conn.getAutoCommit())
			conn.commit();
	}catch (Exception e) {
		e.printStackTrace();
		return "Shift Target not saved. Try Again Later."+"["+e.getMessage()+"]";
		//throw e;
	}
	return "Shift Target Updated Successfully.";
	}
	
	public static List<Integer> getIntegerListFromStringList(List<String> split) {
		ArrayList<Integer> al=new ArrayList<Integer>();
		for (String str : split)
			al.add(new Integer(str));
		return al;
	}
	
}

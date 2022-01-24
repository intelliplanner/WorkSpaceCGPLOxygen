package com.ipssi.dispatchoptimization;

import java.util.ArrayList;

import com.ipssi.dispatchoptimization.vo.DumperInfoVo;
import com.ipssi.dispatchoptimization.vo.LoadSiteVO;
import com.ipssi.dispatchoptimization.vo.PitsVo;
import com.ipssi.dispatchoptimization.vo.RouteVo;
import com.ipssi.dispatchoptimization.vo.ShovelInfoVo;
import com.ipssi.dispatchoptimization.vo.UnloadSiteVo;

public interface OperatorDashInterface {
	
	
	public ArrayList<PitsVo> getAllPits(int portNodeId) ;

	public PitsVo getPitById(int portNodeId, int pitId) ;

	public ArrayList<RouteVo> getAllRoutesOnPit (int portNodeId, int pitId);

	public RouteVo getRouteById(int portNodeId, int pitId, int routId);

	public LoadSiteVO getLoadSiteByRouteId(int portNodeId, int pitId, int routeId);

	public UnloadSiteVo getUnloadSiteByRouteId(int portNodeId, int pitId, int routeId);

	public ArrayList<ShovelInfoVo> getAllShovelsOnRoute (int portNodeId, int pitId, int routeId);

	public ShovelInfoVo getShovelById (int portNodeId, int pitId, int routeId, int shovelId);

	public ArrayList<DumperInfoVo> getAllDumpersOnShovel (int portNodeId, int pitId, int routeId, int shovelId);

	public DumperInfoVo getDumperById (int portNodeId, int pitId, int routeId, int shovelId, int dumperId);

	
	
//	public ArrayList<PitsVo> getAllPits(int portNodeId);
//	public PitsVo getPitById(int pitId);
//	
//	public ArrayList<RouteVo> getAllRoutes(int portNodeId);
//	public ArrayList<RouteVo> getAllRoutesOnPit(int pitId);
//	public RouteVo getRouteById(int routeId);
//	
//	public ShovelInfoVo getShovel(int shovelId);
//	public ArrayList<ShovelInfoVo> getShovelsByRouteId(int routeId);
//	public ArrayList<ShovelInfoVo> getShovelsByPitId(int pit);
//	
//	public DumperInfoVo getDumperById(int dumperId);
//	public ArrayList<DumperInfoVo> getDumpersByShovelId(int shovelId);
//	public ArrayList<DumperInfoVo> getDumpersByRouteId(int routeId);
//	
	
}

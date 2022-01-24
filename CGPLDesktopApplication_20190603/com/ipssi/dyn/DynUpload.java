package com.ipssi.dyn;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.common.ds.OpsTPR.OpsToTPRMines;
import com.ipssi.eta.NewSrcDestProfileCache;
import com.ipssi.eta.NewVehicleETA;
import com.ipssi.eta.RedoHelper;
import com.ipssi.eta.SrcDestHelper;
import com.ipssi.eta.SrcDestInfo;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.ColumnMappingHelper;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.Value;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.input.InputTemplate;
import com.ipssi.jrm.JRMDeviceMap;
import com.ipssi.mapguideutils.LocalNameHelperRTree;
import com.ipssi.mapguideutils.RTreesAndInformation;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.report.cache.CacheValue;
import com.ipssi.report.cache.ScanSupport;
import com.ipssi.routemonitor.RouteDef;
import com.ipssi.segAnalysis.SegmentCluster;
import com.ipssi.tprCache.HHLatestCache;
import com.ipssi.tprCache.Loader;
import com.ipssi.tprCache.TPRLatestCache;
import com.ipssi.tracker.common.util.DataProcessorGateway;
import com.ipssi.tracker.common.util.RuleProcessorGateway;
import com.ipssi.tracker.common.util.TripProcessorGateway;
import com.ipssi.workflow.WorkflowDef;

public class DynUpload {
	public static Document internalXML = null;
	public static Document lovXML = null;
	static private final int LOV_STANDARD = 1;
	static private final int LOV_CUSTOM = 2;
	public static ArrayList<Integer> getVehicleId(HttpServletRequest request, Connection dbConn) {
		ArrayList<Integer> arrList = new ArrayList<Integer>();		
		try {
			String[] org = request.getParameterValues("org_id");
			if (org != null && org.length > 0) {
				StringBuilder sb = new StringBuilder("select distinct(vehicle.id) vehicle_id from vehicle left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) join port_nodes anc  on (anc.id in (");
				Misc.convertInListToStr(org, sb);
				sb.append(") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) and vehicle.status=1");
				
				//		"select vehicle.id from vehicle join port_nodes leaf on (leaf.id = vehicle.customer_id) join port_nodes anc on (anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) where anc.id in (");
				//sb.append(") and vehicle.status in (1) ");
				PreparedStatement ps = dbConn.prepareStatement(sb.toString());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					arrList.add(rs.getInt(1));
				}
				rs.close();
				ps.close();
			}
			if (arrList.size() == 0) {
				String vehicle[] = request.getParameterValues("vehicle_id");
				for (int i=0,is=vehicle.length;i<is;i++) {
					arrList.add(Misc.getParamAsInt(vehicle[i]));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
		return arrList;
		
	}
	public static void dpActionController(HttpServletRequest request, Connection dbConn) {
		try {
			boolean doTrip = "1".equals(request.getParameter("dp_control"));
			if (doTrip) {
				ArrayList<Integer> arrList =getVehicleId(request, dbConn); 
				
				if (arrList.size() > 0) {
					String action=request.getParameter("action");
					if ("refresh_cache".equals(action)) {
						DataProcessorGateway.sendMessage(arrList, 1);
					}
					else if ("dump".equals(action)) {
						DataProcessorGateway.sendMessage(arrList, 2);
					}
					else if ("trace".equals(action)) {
						DataProcessorGateway.sendMessage(arrList, 3);
					}
					else if ("stop_trace".equals(action)) {
						DataProcessorGateway.sendMessage(arrList, 4);
					}
				}//if proper
			}//if dpControl
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void jrmAnalysisActionController(HttpServletRequest request, Connection dbConn) {
		try {
			boolean doJRMAnalysis = "1".equals(request.getParameter("jrm_analysis"));
			if (!doJRMAnalysis)
				return;
			boolean doClustering = "1".equals(request.getParameter("cluster"));
			boolean doSegPrep = "1".equals(request.getParameter("seg_line"));
			boolean doVehPopEstimator = "1".equals(request.getParameter("vehicle_estimator"));
			boolean doOtherDBRec = "1".equals(request.getParameter("other_db"));
			int ruleId=Misc.getParamAsInt(request.getParameter("rule_id"));
			boolean partialMode = !"0".equals(request.getParameter("partial"));
			double clusterSegmenterThresh = Misc.getParamAsDouble(request.getParameter("segmenter_thresh"), 2);
			SegmentCluster.controller(dbConn, doClustering, doSegPrep, doVehPopEstimator, doOtherDBRec, ruleId, partialMode, clusterSegmenterThresh);			

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void miscActionController(HttpServletRequest request, Connection dbConn) {
		
		try {
			boolean doMisc = "1".equals(request.getParameter("misc_control"));
			if (doMisc) {
				String action = request.getParameter("action");
				if ("load_mines_to_ops".equals(action)) {
					OpsToTPRMines.loadMinesOpsMappingFromDB(dbConn);
				}
				else if ("load_latest_do_for_mines".equals(action)) {
					OpsToTPRMines.loadLatestDOForMinesFromDB(dbConn);
				}
				else if ("load_latest_tpr".equals(action)) {
					TPRLatestCache.load(dbConn);
				}
				else if ("load_latest_hh_log".equals(action)) {
					HHLatestCache.load(dbConn);
				}
				else if ("stop_tpr_cache_loader".equals(action)) {
					Loader.stop();
				}
				else if ("start_tpr_cache_loader".equals(action)) {
					Loader.start();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	public static void tripActionController(HttpServletRequest request, Connection dbConn) {
		try {
			boolean doTrip = "1".equals(request.getParameter("trip_control"));
			if (doTrip) {
				ArrayList<Integer> arrList =getVehicleId(request, dbConn); 
				
				if (arrList.size() > 0) {
					String action=request.getParameter("action");
					if ("update_current_challan_reg".equals(action)) {
						TripProcessorGateway.updateChallan(arrList, Misc.getServerName(), false);
					}
					else if ("update_current_challan_db".equals(action)) {
						TripProcessorGateway.updateChallan(arrList, Misc.getServerName(), true);
					}
					else if ("update_current_cache".equals(action)) {
						TripProcessorGateway.refreshCurrentCache(arrList, Misc.getServerName());
					}
					else if ("update_newmu_cache".equals(action)) {
						TripProcessorGateway.refreshNewMUCache(arrList, Misc.getServerName());
					}
					else if ("trace".equals(action)) {
						TripProcessorGateway.tripDBGAddTrace(arrList, Misc.getServerName());
					}
					else if ("stop_trace".equals(action)) {
						TripProcessorGateway.tripDBGRemoveTrace(arrList, Misc.getServerName());
					}
					else if ("refresh_cache".equals(action)) {
						TripProcessorGateway.refreshCache(arrList, Misc.getServerName());
					}
					else if ("redo".equals(action)) {
						Thread th = new RedoSender(arrList, false);
						th.start();
						//TripProcessorGateway.redo(arrList, Misc.getServerName());
					}
					//else if ("redo_with_cache".equals(action)) {
					//	TripProcessorGateway.redoWithCache(arrList, Misc.getServerName());
					//}
					else {
						TripProcessorGateway.tripDBGDump(arrList, Misc.getServerName());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	
	public static void etaActionController(HttpServletRequest request, Connection dbConn, JspWriter out) throws Exception{
		try {
			boolean doTrip = "1".equals(request.getParameter("eta_control"));
			if (doTrip) {
				ArrayList<Integer> arrList =getVehicleId(request, dbConn); 
				
				if (arrList.size() > 0) {
					String action=request.getParameter("action");
					if ("refresh_sd".equals(action)) {//vehicleId refers to src dest id
						Connection conn = null;
						boolean destroyit = false;
						try {
							System.out.println("[ETA LOC] RefreshSD:"+arrList);
							conn = DBConnectionPool.getConnectionFromPoolNonWeb();
							if (arrList == null || arrList.size() == 0)
								SrcDestHelper.reinitAllRtree();
							SrcDestInfo.loadSrcDestInfo(conn, arrList);
							if (conn.getAutoCommit())
								conn.commit();
							RedoHelper.execAction(null, RedoHelper.G_UPDATE_SRC_DEST, Misc.getUndefInt(), false);
						}
						catch (Exception e) {
							destroyit = true;
							e.printStackTrace();
						}
						finally {
							if (conn != null)
								DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyit);
						}
					}
					else if ("refresh_cache".equals(action)) {
						Connection conn = null;
						boolean destroyit = false;
						try {
							System.out.println("[ETA LOC] Refresh ETa profile cache:");
							conn = DBConnectionPool.getConnectionFromPoolNonWeb();
							NewSrcDestProfileCache.load(conn, null, true);
						}
						catch (Exception e) {
							destroyit = true;
							e.printStackTrace();
						}
						finally {
							if (conn != null)
								DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyit);
						}
					}
					else if ("redo".equals(action)) {//todo in thread
						System.out.println("[ETA LOC] Redo:"+arrList);
						RedoHelper.execAction(arrList, RedoHelper.G_REDO_VEHICLE, Misc.getUndefInt(), false);
					}
					else if ("update_state".equals(action)) {//todo in thread
						System.out.println("[ETA LOC] Update state for:"+arrList);
						RedoHelper.execAction(arrList, RedoHelper.G_UPDATE_SRC_DEST, Misc.getUndefInt(), false);
					}
					else if ("dump_src".equals(action)) {
						Connection conn = null;
						boolean destroyit = false;
						try {
							conn = DBConnectionPool.getConnectionFromPoolNonWeb();
							for (int i=0,is=arrList == null ? 0 : arrList.size(); i<is; i++) {
								System.out.println("[ETA LOC] Dumping SD for:"+arrList.get(i));
								SrcDestInfo sd = SrcDestInfo.getSrcDestInfo(conn, arrList.get(i));
								if (sd != null)
									System.out.println(sd.toString());
							}
						}
						catch (Exception e) {
							destroyit = true;
							e.printStackTrace();
						}
						finally {
							if (conn != null)
								DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyit);
						}
					}
					else if ("dump_cache".equals(action)) {//todo
					}
					else if ("dump_state".equals(action)) {
						Connection conn = null;
						boolean destroyit = false;
						try {
							conn = DBConnectionPool.getConnectionFromPoolNonWeb();
							for (int i=0,is=arrList == null ? 0 : arrList.size(); i<is; i++) {
								System.out.println("[ETA LOC] Dumping state for:"+arrList.get(i));
								NewVehicleETA vehicleETA = NewVehicleETA.getETAObj(conn, arrList.get(i));
								
								if (vehicleETA != null) {
									out.println(vehicleETA.toString());
									System.out.println(vehicleETA.toString());
								}
							}
						}
						catch (Exception e) {
							destroyit = true;
							e.printStackTrace();
							
						}
						finally {
							if (conn != null)
								DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyit);
						}
						
					}
					else if ("trace".equals(action)) {
						for (int i=0,is=arrList == null ? 0 : arrList.size(); i<is; i++) {
							NewVehicleETA.addToTrace(arrList.get(i));
						}
					}
					else if ("stop_trace".equals(action)) {
						for (int i=0,is=arrList == null ? 0 : arrList.size(); i<is; i++) {
							NewVehicleETA.removeFromTrace(arrList.get(i));
						}//for each vehicle
					}//action elif
				}//if arrList
			}//if doin eta_control
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static void dataActionController(HttpServletRequest request, Connection dbConn) {
		try {
			boolean doTrip = "1".equals(request.getParameter("data_control"));
			String action=request.getParameter("action");
			
			if (doTrip) {
				if ("jrm_refresh_cache".equals(action)) {
					LocalNameHelperRTree.resetJRMRTreeLoaded();
					JRMDeviceMap.setAllRefreshRecalc(false, true);
				}
				if ("jrm_recalc".equals(action)) {
					JRMDeviceMap.setAllRefreshRecalc(false, true);
				}
				if ("jrm_send_refresh".equals(action)) {
					JRMDeviceMap.setAllRefreshRecalc(true, false);
				}
				if ("jrm_refresh_all".equals(action)) {
					LocalNameHelperRTree.resetJRMRTreeLoaded();
					JRMDeviceMap.setAllRefreshRecalc(true, true);
				}
				if ("set_intermediate".equals(action)) {
					System.out.println("[DP Upd]"+Misc.g_intermediateDistUpdMode);
					Misc.g_intermediateDistUpdMode = Misc.getParamAsInt(request.getParameter("mode"),0);
					System.out.println("[DP Upd]"+Misc.g_intermediateDistUpdMode);
				}
				else if ("redo_scan".equals(action)) {
					ArrayList<Integer> arrList =getVehicleId(request, dbConn);
					for (int i1=0, i1s = arrList == null ? 0 : arrList.size();i1<i1s; i1++) {
						ScanSupport.redo(dbConn, arrList.get(i1));
					}
				}
				else if ("refresh_routes".equals(action)) {
					System.out.println("[LOCTR] Refreshing route cache ..");
					RouteDef.resetCache(dbConn);
					System.out.println("[LOCTR] ... Refreshed route cache");
				}
				else if ("refresh_rtree_core".equals(action)) {//excludes specialRegionRTree ... coming from op and SrcDestInfo
					System.out.println("[LOCTR] Refreshing landmarks, regions ...");
					RTreesAndInformation.refreshAll(dbConn);
					System.out.println("[LOCTR] ... Refreshed landmarks, regions cache");					
				}
				
				else {
					ArrayList<Integer> arrList =getVehicleId(request, dbConn);
					
					if (arrList.size() > 0) {
						
						if ("dump".equals(action)) {
							System.out.println("[LOCTR] dumping Cache for:"+arrList);
							StringBuilder sb = new StringBuilder();
							for (int i=0,is=arrList.size();i<is;i++) {
								int vehId = arrList.get(i);
								VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(dbConn, vehId, false, false);
								if (vdf != null) {
									synchronized (vdf) {
										sb.append(vdf);
									}
								}
							}
							System.out.println(sb);
							System.out.println("[LOCTR] dumpped Cache for:"+arrList);
							
						}else if ("dump_cached_vehicle_data".equals(action)) {
							System.out.println("[LOCTR] Dump Vehicle data cache ...");
							StringBuilder sb = new StringBuilder();
							for (int i=0,is=arrList.size();i<is;i++) {
								try {
									int vehId = arrList.get(i);
									CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehId, dbConn);
									VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(dbConn, vehId, false, false);
									if (vdf != null) {
										synchronized (vdf) {
											NewVehicleData vdt = vdf.getDataList(dbConn, vehId, 0, false);
											if (vdt != null) {
												sb.append("vehId :"+vehId + " : ");
												sb.append(new java.util.Date(vdt.getOdometerDayRecTime())).append("\n");
												GpsData odoGpsPoint = vdt.getOdoDayGps(dbConn);
												if (odoGpsPoint != null) {
													sb.append(odoGpsPoint.toString()).append("\n");
													
												}
												sb.append(new java.util.Date(vdt.getOdometerWeekRecTime())).append("\n");
												GpsData odoWeekGpsPoint = vdt.getOdoWeekGps(dbConn);
												if (odoWeekGpsPoint != null) {
													sb.append(odoWeekGpsPoint.toString()).append("\n");
													
												}
											}
										}
									}
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}//for
							System.out.println(sb);
							System.out.println("[LOCTR] ... Dump Vehicle data cache done. ");					
						}
						if ("curr_name".equals(action)) {
							System.out.println("[LOCTR] updating current name for:"+arrList);
							PreparedStatement ps = dbConn.prepareStatement("update current_data set name=? where vehicle_id=?");
							PreparedStatement psLog = dbConn.prepareStatement("update logged_data set name=? where vehicle_id = ? and attribute_id=? and gps_record_time = ?" );
							for (int i=0,is=arrList.size();i<is;i++) {
								try {
									int vehId = arrList.get(i);
									CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehId, dbConn);
									VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(dbConn, vehId, false, false);
									if (vdf != null) {
										synchronized (vdf) {
											NewVehicleData vdt = vdf.getDataList(dbConn, vehId, 0, false);
											if (vdt != null) {
												GpsData lastPoint = vdt.getLast(dbConn);
												if (lastPoint != null) {
													String name = lastPoint.getName(dbConn, vehId, vehSetup);
													vdt.clearRecordName();
													ps.setString(1, name);
													ps.setInt(2, vehId);
													ps.addBatch();
													psLog.setString(1, name);
													psLog.setInt(2, vehId);
													psLog.setInt(3, 0);
													psLog.setTimestamp(4, Misc.utilToSqlDate(lastPoint.getGps_Record_Time()));
													psLog.addBatch();
												}//lastPoint != null
											}//vdt != null
										}//syn vdf
									}//vdf != null
								}//try
								catch (Exception e) {
									e.printStackTrace();
								}
							}//for
							try {
								ps.executeBatch();
								psLog.executeBatch();
								ps.close();
								psLog.close();
								if (!dbConn.getAutoCommit())
									dbConn.commit();
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							System.out.println("[LOCTR] updated current name for:"+arrList);
						}
						else if ("update_current_cache".equals(action)) {
							//TEMP ... need to change stuff in CacheValue
							System.out.println("[LOCTR] current cache for:"+arrList);
							for (int i=0,is=arrList.size();i<is;i++) {
								try {
									int vehId = arrList.get(i);
						
									CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehId, dbConn);
									VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(dbConn, vehId, false, false);
									if (vdf != null) {
										synchronized (vdf) {
											CacheValue.add(vehId, 20173, Misc.getUndefInt(), Misc.getUndefDouble(), null, Misc.getUndefInt(), Cache.DATE_TYPE);
											CacheValue.add(vehId, 20165, Misc.getUndefInt(), Misc.getUndefDouble(), null, Misc.getUndefInt(), Cache.NUMBER_TYPE);
											CacheValue.add(vehId, 20166, Misc.getUndefInt(), Misc.getUndefDouble(), null, Misc.getUndefInt(), Cache.NUMBER_TYPE);
											CacheValue.add(vehId, 20167, Misc.getUndefInt(), Misc.getUndefDouble(), null, Misc.getUndefInt(), Cache.STRING_TYPE);
										}//syn vdf
									}//vdf != null
								}//try
								catch (Exception e) {
									e.printStackTrace();
								}
								System.out.println("[LOCTR] current cache for:"+arrList);
							}//for
						}
						else if ("refresh_cache".equals(action)) {
							System.out.println("[LOCTR] refreshing central cache for:"+arrList);
							for (int i=0,is=arrList.size();i<is;i++) {
								try {
									int vehId = arrList.get(i);
									CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(vehId, dbConn);
									VehicleDataInfo vdf = VehicleDataInfo.getVehicleDataInfo(dbConn, vehId, false, false);
									if (vdf != null) {
										synchronized (vdf) {
											vdf.reinit(dbConn, vehSetup);
										}//syn vdf
									}//vdf != null
								}//try
								catch (Exception e) {
									e.printStackTrace();
								}
							}//for
							System.out.println("[LOCTR] central cache refreshred for:"+arrList);
						}
						else if ("recalc_dist".equals(action)) {
							System.out.println("[LOCTR] refreshing recalc dist for:"+arrList);
							Thread th = new RedoNonQ(arrList, 1);
							th.start();
						}

					}//if arrList exists
				}//vehicle dependent stuff
			}//do data_control
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	
	public static void ruleActionController(HttpServletRequest request, Connection dbConn) {
		try {
			boolean doTrip = "1".equals(request.getParameter("rule_control"));
			if (doTrip) {
				ArrayList<Integer> arrList = getVehicleId(request, dbConn);
				if (arrList.size() > 0) {
					String action=request.getParameter("action");
					if ("update_current_cache".equals(action)) {
						RuleProcessorGateway.refreshCurrentCache(arrList, Misc.getServerName());
					}
					else if ("trace".equals(action)) {
						RuleProcessorGateway.ruleDBGAddTrace(arrList, Misc.getServerName());
					}
					else if ("stop_trace".equals(action)) {
						RuleProcessorGateway.ruleDBGRemoveTrace(arrList, Misc.getServerName());
					}
					else if ("refresh_cache".equals(action)) {
						RuleProcessorGateway.refreshCache(arrList, Misc.getServerName());
					}
					else if ("redo".equals(action)) {
						Thread th = new RedoSender(arrList, true);
						th.start();
					}
					else if ("refresh_vehicle".equals(action)) {
						RuleProcessorGateway.refreshVehicle(arrList, Misc.getServerName());
					}
					else if ("refresh_rule".equals(action)) {
						for (int i=0,is=arrList.size();i<is;i++)
							RuleProcessorGateway.refreshRule(arrList.get(i), Misc.getServerName());
					}
					else if ("refresh_ruleset".equals(action)) {
						for (int i=0,is=arrList.size();i<is;i++)
							RuleProcessorGateway.refreshRuleSet(arrList.get(i), Misc.getServerName());
					}
					else if ("refresh_all_notification".equals(action)) {
						RuleProcessorGateway.refreshNotificationSet(Misc.getUndefInt(), Misc.getServerName());
					}
					else if ("refresh_notification".equals(action)) {
						for (int i=0,is=arrList.size();i<is;i++)
							RuleProcessorGateway.refreshNotificationSet(arrList.get(i), Misc.getServerName());
					}
					//else if ("redo_with_cache".equals(action)) {
					//	RuleProcessorGateway.redoWithCache(arrList, Misc.getServerName());
					//}
					else {
						RuleProcessorGateway.ruleDBGDump(arrList, Misc.getServerName());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	public static void controller(HttpServletRequest request, Connection dbConn) {
		try {
			boolean doMenu = "1".equals(request.getParameter("do_menu"));
			boolean doXmlCache = "1".equals(request.getParameter("do_xml_cache"));
			boolean doCoreCache = "1".equals(request.getParameter("do_core_cache"));
			
			String cfgFile[] = request.getParameterValues("cfg");
//			if (doXmlCache) {
//				XmlCache.makeXmlCacheDirty();
//			}
			if (doMenu) {
				Cache cache = Cache.getCacheInstance(dbConn);
				cache.loadMenuetc(dbConn, null);
			}
			if (doCoreCache)
				doCoreCache(dbConn);
			boolean updated = doUpdCFG(cfgFile);
			if (updated || doCoreCache) {
				com.ipssi.reporting.cache.CacheManager.makeAllDirty();
				InputTemplate.makeDirty(null, null, null, Misc.getUndefInt(), null);
			}
			if (doCoreCache) {
				WorkflowDef.clearWorkflowDef();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//eat it
		}
	}
	
	public static boolean doUpdCFG(String cfgFile[]) {
		boolean doAll = false;
		if (cfgFile == null || cfgFile.length == 0)
			return false;
		for (int i=0,is=cfgFile == null ? 0 : cfgFile.length;i<is;i++) {
			if ("all".equals(cfgFile[i])) {
				doAll = true;
				break;
			}
		}
		if (doAll) {
			try {
				FrontPageInfo.g_frontPageList.clear();
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
			try {
				PageHeader.g_pageHeaderList.clear();
			}
			catch (Exception e) {
				e.printStackTrace();
				//eat it
			}
		}
		else {
			for (int i=0,is=cfgFile == null ? 0 : cfgFile.length;i<is;i++) {
				String n = cfgFile[i];
				try {
					FrontPageInfo.g_frontPageList.remove(n);
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it
				}
				try {
					for (int j=0,js = PageHeader.g_pageHeaderList.size();j<js;j++) {
						 PageHeader pageHeader = (PageHeader) PageHeader.g_pageHeaderList.get(i);
			                if (pageHeader.m_name.equals(n)) {
			                  PageHeader.g_pageHeaderList.remove(i);
			                  break;
			                }
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it
				}
			}	
		}
		return true;
	}
	
    public static void doCoreCache(Connection dbConn) {
    	try {
    		System.out.println("REPORT CACHE UPDATE STARTED");
    		//String serverConfigPath = Misc.CFG_CONFIG_SERVER;
    		String serverConfigPath = Misc.getServerConfigPath();//.getBASE_CFG_CONFIG_SERVER();
    		String internalName = "internal.xml";
    		String lovName = "lov.xml";
    		FileInputStream inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+"internal.xml");
            MyXMLHelper test = new MyXMLHelper(inp, null);
            internalXML = test.load();
            Element elem = internalXML.getDocumentElement();
            inp.close();
            inp = null;
            test = null;
            inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+lovName);
            test = new MyXMLHelper(inp,null);
            lovXML = test.load();
            inp.close();
            test=null;
            inp=null;
            createPartialMetaAndUpdateId(dbConn);
            WorkflowDef.loadWorkflowDef(true);
                        //load the menuDocument
            //            prjDimList = new DimMapList(lovXML, internalXML, partialMetaXML, DimMapInfo.FOR_PROJECT);
//            altDimList = new DimMapList(lovXML, internalXML, partialMetaXML, DimMapInfo.FOR_ALTERNATIVE);
//            System.out.println("Ended Cache");
            internalXML = null;
            lovXML = null;
            System.out.println("REPORT CACHE UPDATE ENDED");
    	}
    	catch (Exception e) {
    		System.out.println("REPORT CACHE UPDATE CONTAINS ERROR");
    		e.printStackTrace();
    		//eat it  .. dont screw up things
    	}
    }
    
    private  static void createPartialMetaAndUpdateId(Connection dbConn) throws Exception {
    	  //name is misnomer coming ... from CapEx
    	  
    	       //Add in from the dimension list
    	       Element dims = MyXMLHelper.getChildElementByTagName(internalXML.getDocumentElement(),"dimensions");
    	       NodeList childNodes = dims.getChildNodes();
    	       int chIndex, chCount;
    	       for (chIndex=0,chCount = childNodes.getLength();chIndex < chCount;chIndex++) {
    	           Node tempNode = childNodes.item(chIndex);
    	           if (tempNode.getNodeType() != Node.ELEMENT_NODE)
    	              continue;
    	           Element chElem = (Element) tempNode;
//    	           if (chElem.getAttribute("dimlist").equals("1")) {
    	           {

    	               //create the id - note that dimlist only contains standard dimensions
    	               //custom dimensions id's will be incremented (with custom_dim_start) later on.
    	               
    	               MiscInner.PairElemInt attribInfo = getAttribCatNodeNotFromDimList(chElem.getTagName(), chElem);
    	               if (attribInfo != null){
    	                   Element attribNode = attribInfo.first;
    	                   String attribNodeIdStr = attribNode.getAttribute("id");
    	                   String chElemNodeIdStr = chElem.getAttribute("id");
    	                   if (attribNodeIdStr == null || attribNodeIdStr.length() == 0) {                   
    	                       attribNode.setAttribute("id", chElemNodeIdStr);                      
    	                       attribNode.setAttribute("sn", chElem.getAttribute("sn"));
    	                   }
    	               }
    	            
    	               String dimsn = chElem.getAttribute("sn");
    	               String dimname = chElem.getAttribute("name");
    	               
    	               String chElemType = chElem.getAttribute("type");
    	               // sameer 04112006
    	               //int valType = chElemType.equals("number")?NUMBER_TYPE :
    	               //                             chElemType.equals("string")?STRING_TYPE:chElemType.equals("date") ? DATE_TYPE: LOV_TYPE;
    	               //String typeStringForMeta = valType == NUMBER_TYPE?"n":valType == STRING_TYPE?"s": valType == DATE_TYPE ? "d" : "i";
    	               int valType = chElemType.equals("number")? Cache.NUMBER_TYPE :
    				                 chElemType.equals("string")? Cache.STRING_TYPE :
    				                 chElemType.equals("date") ? Cache.DATE_TYPE :
    				                 chElemType.equals("file") ? Cache.FILE_TYPE : chElemType.equals("img") ? Cache.IMAGE_TYPE : Cache.LOV_TYPE;
    	               String typeStringForMeta = valType == Cache.NUMBER_TYPE?"n":valType == Cache.STRING_TYPE?"s": valType == Cache.DATE_TYPE ? "d" : valType == Cache.FILE_TYPE ? "f" : valType == Cache.IMAGE_TYPE ? "p"  : "i";
    	               // end sameer 04112006
    	            
    	            
    	               String subsetOf = chElem.getAttribute("subset_of");
    	               if (subsetOf != null && subsetOf.length() != 0) {          
    	                   Element attribNode = attribInfo.first;
    	                   attribNode.setAttribute("subset_of", subsetOf);
    	               }
    	               String notLoad = chElem.getAttribute("no_load");               
    	               String precalc = chElem.getAttribute("pre_calc");
    	               String propOf = chElem.getAttribute("property_of");               
    	               String showInUI = chElem.getAttribute("show_in_ui");
    	               String exprString = chElem.getAttribute("expr_string");               
    	               //Add is_exp and ref_exp_raw="123" info ... 080605
    	               String isExp = chElem.getAttribute("is_exp");
    	               String refExpRaw =  chElem.getAttribute("ref_exp_raw");
    	               String preDef = chElem.getAttribute("pre_def");
    	               String desc = chElem.getAttribute("desc");
    	               String colClause = chElem.getAttribute(Misc.G_DO_ORACLE ? "column_orcl" : "column");
    	               if (Misc.G_DO_ORACLE && (colClause == null || colClause.length() == 0))
    	                  colClause = chElem.getAttribute("column");
    	               String joinClause = chElem.getAttribute(Misc.G_DO_ORACLE ? "join_clause_orcl" : "join_clause");
    	               if (Misc.G_DO_ORACLE && (joinClause == null || joinClause.length() == 0))
    	                  joinClause = chElem.getAttribute("join_clause");
    	               String andClause = chElem.getAttribute(Misc.G_DO_ORACLE ? "and_clause_orcl" : "and_clause");
    	               if (Misc.G_DO_ORACLE && (andClause == null || andClause.length() == 0))
    	                  andClause = chElem.getAttribute("and_clause");
    	               ColumnMappingHelper colMap = new ColumnMappingHelper(chElem.getAttribute("table"),
    	                               colClause,
    	                               valType,
    	                               andClause,
    	                               Misc.getParamAsString(chElem.getAttribute("base_table"), ""),
    	                               joinClause,
    	                               "1".equals(chElem.getAttribute("use_col_for_name")),
    	                               "1".equals(chElem.getAttribute("do_having_filter")),
    	                               chElem.getAttribute("pur_link_hint"), ////order_to_ccbs, order_to_prj, supp_to_ccbs_awarded, supp_to_ccbs_assoc, supp_to_order_assoc, supp_to_order_awarded
    	                               "1".equals(chElem.getAttribute("colname_has_agg")),
    	                               Misc.getParamAsString(chElem.getAttribute("id_field"), ""),
    	                               Misc.getParamAsString(chElem.getAttribute("name_field"), "")
    	                               );
    	               int qtyType = Misc.getParamAsInt(chElem.getAttribute("qty_type"), 0);
    	               int dimId = Misc.getParamAsInt(chElem.getAttribute("id"));
    	               int refDescDimId = Misc.getParamAsInt(chElem.getAttribute("ref_desc_dim_id"));
    	               
    	               String refOrgLevel = chElem.getAttribute("ref_org_level_cat");
    	               if ("".equals(refOrgLevel))
    	                   refOrgLevel = null;
    	               
    	               String refOrgDescTill =  chElem.getAttribute("ref_org_desc_till"); //if empty then all
    	               if ("".equals(refOrgDescTill))
    	                   refOrgDescTill = null;
    	               
    	               String refOrgAncTill = chElem.getAttribute("ref_org_anc_till"); //if empty then all
    	               if ("".equals(refOrgAncTill))
    	                   refOrgAncTill = null;
    	                   
    	               boolean useRepCurrencyByDefault = !"0".equals(chElem.getAttribute("use_rep_currency"));
    	               

    	               int refMeasureId = Misc.getParamAsInt(chElem.getAttribute("ref_measure_id"));
    	               int ignorePastFuture = Misc.getParamAsInt(chElem.getAttribute("ref_ignore_past_or_future"), 0);
    	               int editPastMon = Misc.getParamAsInt(chElem.getAttribute("edit_past"),0);
    	               int editFutureMon = Misc.getParamAsInt(chElem.getAttribute("edit_future"),24);
    	               int lookWhereBaseline = Misc.getParamAsInt(chElem.getAttribute("look_where_baseline"), 0);
    	               boolean notime = "1".equals(chElem.getAttribute("notime"));
    	               if (Misc.isUndef(refDescDimId))
    	                  refDescDimId = dimId;
    	               if (Misc.isUndef(refMeasureId))
    	                  refMeasureId = dimId;
    	               double scale = Misc.getParamAsDouble(chElem.getAttribute("ref_scale"), 1);
    	               String unitString = chElem.getAttribute("ref_unit");
    	               int currencyRateList = Misc.getParamAsInt(chElem.getAttribute("currency_rate_list"),1);
    	               String isGlobalProp = chElem.getAttribute("is_global");
    	               DimInfo dimInfo = addDimInfo(dbConn, chElem.getTagName(), dimId, colMap, attribInfo.first, attribInfo.second, chElem.getAttribute("default"), qtyType, refDescDimId,refOrgLevel, refMeasureId, ignorePastFuture, editPastMon, editFutureMon, scale, unitString, useRepCurrencyByDefault, notime, dimname, dimsn, Misc.getParamAsInt(subsetOf), chElem, refOrgDescTill, refOrgAncTill, chElem.getAttribute("sub_type"));
    	               dimInfo.m_lookInThisBaseline = lookWhereBaseline;
    	               

    	           } //if dimlist == 1
    	       } //for loop ends

    	       //add dims to DimInfo that can only be found by CatNode
    	       for (int art = 0;art<3;art++) {
    	           Element lovNode = null;
    	           if (art == 0) {
    	              lovNode = MyXMLHelper.getChildElementByTagName((Element)lovXML.getDocumentElement().getElementsByTagName("standard").item(0), "lov");              
    	           }
    	           else if (art == 1){
    	              lovNode = MyXMLHelper.getChildElementByTagName(internalXML.getDocumentElement(), "lov");              
    	           }
    	           else {
    	              lovNode = MyXMLHelper.getChildElementByTagName(internalXML.getDocumentElement(), "constants");              
    	           }
    	           for (Node n=lovNode.getFirstChild();n!=null;n=n.getNextSibling()) {
    	              if (n.getNodeType() != 1)
    	                 continue;
    	              Element e = (Element) n;
    	              String idStr = e.getAttribute("id");
    	              if (idStr == null || idStr.length() == 0) {
    	                  addDimInfo(dbConn, e.getTagName(), Misc.getUndefInt(),null, e, Cache.LOV_TYPE, null, 4, Misc.getUndefInt(), null, Misc.getUndefInt(), 0, 0, 0, 1.0f, null, true, true, null, null, Misc.getUndefInt(), null, null,null,null);
    	              }
    	           }           
    	       }
    	       

    	       // ValidVal
    	       
    	       NodeList nl = lovXML.getElementsByTagName("validval");
    	       childNodes = nl.item(0).getChildNodes(); // the pair nodes
    	       for (chIndex=0,chCount = childNodes.getLength(); chIndex < chCount; chIndex++) {
    	          Node tempNode = childNodes.item(chIndex);
    	          if (tempNode.getNodeType() != Node.ELEMENT_NODE)
    	             continue;
    	        
    	          Element chNode = (Element) tempNode;
    	         //$$DIFF_FROM_CACHE DimInfo.loadValidVal(chNode);          
    	       }// end of for for pairs             
    	  }
    public static DimInfo addDimInfo(Connection dbConn, String catName, int id, ColumnMappingHelper colMap, Element xmlInfo, int attribType, String defaultVal, int qtyType, int descDataDimId, String refOrgLevel, int refMeasureId, int ignorePastFuture, int editPastMon, int editFutureMon, double scale, String unitString, boolean useRepCurrencyByDefault, boolean noTime, String name, String sn, int subsetOf, Element refInInternal, String refOrgDescTill, String refOrgAncTill, String subType) throws Exception {
        
        if (ignorePastFuture == 1) { //ignore future
           if (editPastMon == 0)
              editPastMon = 1;
           if (editFutureMon > 0)
              editFutureMon = 0;
        }
        else if (ignorePastFuture == -1) {
           if (editFutureMon == 0)
              editFutureMon = 1;
           if (editPastMon > 0)
              editPastMon = 0;
        }
        DimInfo dimInfo = DimInfo.getDimInfo(id);
        boolean isNew = dimInfo == null;
        if (isNew)
           dimInfo = new DimInfo();
        else {
           //$$DIFF_FROM_CACHE ... dimInfo.initDimInfo();
        }
        dimInfo.m_id = id;
        dimInfo.m_name = name;
        dimInfo.m_sn = sn;
        if (dimInfo.m_name == null)
           dimInfo.m_name = dimInfo.m_catName;
        if (dimInfo.m_sn == null)
           dimInfo.m_sn = dimInfo.m_name;
        dimInfo.m_subsetOf = subsetOf;
        if (Misc.isUndef(dimInfo.m_subsetOf))
           dimInfo.m_subsetOf = dimInfo.m_id;
        
        dimInfo.m_colMap = colMap;
        dimInfo.m_type = attribType;
        Element attribNode = xmlInfo;
        if (attribNode != null) {
        	readLovNode(dimInfo, attribNode);
           //dimInfo.m_valList = ValInfo.readLovNode(attribNode);            
           dimInfo.m_subsetOf = Misc.getParamAsInt(attribNode.getAttribute("subset_of"), dimInfo.m_id);
        }
        if (subType == null || subType.length() == 0)
            subType = "20508"; //normal data
        dimInfo.m_subtype = subType;
        dimInfo.m_catName = catName;
        if (!Misc.isUndef(id))
           DimInfo.g_dimList.put(new Integer(id), dimInfo);
        if (catName != null && catName.length() != 0)
           DimInfo.g_dimListByCatName.put(catName, dimInfo);
        if (defaultVal == null || defaultVal.length() == 0) {
        	ArrayList valList = dimInfo.getValList();
           defaultVal = valList == null || valList.size() == 0 ? null : Integer.toString(((ValInfo)valList.get(0)).m_id);
        }
        
        dimInfo.m_default = defaultVal;
        dimInfo.m_qtyType = qtyType;
        dimInfo.m_descDataDimId = descDataDimId;
        if (refOrgLevel != null && refOrgLevel.length() != 0) {
           dimInfo.m_refOrgLevel = new ArrayList();
           Misc.convertValToVector(refOrgLevel, dimInfo.m_refOrgLevel);
        }
        if (refOrgDescTill != null) {
           dimInfo.m_refOrgDescTill = new ArrayList();
           Misc.convertValToVector(refOrgDescTill, dimInfo.m_refOrgDescTill);
        }
        if (refOrgAncTill != null) {
           dimInfo.m_refOrgAncTill = new ArrayList();
           Misc.convertValToVector(refOrgAncTill, dimInfo.m_refOrgAncTill);
        }
        if (Misc.isUndef(refMeasureId))
           refMeasureId = id;
        dimInfo.m_refMeasureId = refMeasureId;
        dimInfo.m_ignorePastFuture = ignorePastFuture;
        dimInfo.m_editPastEditMon = editPastMon;
        dimInfo.m_editFutureEditMon = editFutureMon;
        if (Misc.isUndef(scale) || scale < 0)
           scale = 1;
        dimInfo.m_scale = scale;
        if (unitString == null)
           unitString = "";
        dimInfo.m_unitString = unitString;
        dimInfo.m_useRepCurrencyByDefault = useRepCurrencyByDefault;
        if (dimInfo.m_colMap != null && "npv".equals(dimInfo.m_colMap.table) || noTime)
           dimInfo.m_noTime = true;
        if (refInInternal != null) {
           dimInfo.m_currencyList = Misc.getParamAsInt(refInInternal.getAttribute("currency_rate_list"),1);
        }
        if (xmlInfo != null) {
           //$$DIFF_FROM_CACHE String tag = xmlInfo.getAttribute("acc_check");
        	//$$DIFF_FROM_CACHE if (tag != null && tag.length() != 0) {
        	//$$DIFF_FROM_CACHE g_tempDimInfoListRequiringAccessCheck.add(dimInfo);
        	//$$DIFF_FROM_CACHE dimInfo.m_tempAccTag = tag;
        	//$$DIFF_FROM_CACHE   dimInfo.m_accCheckonObj = Misc.getParamAsInt(xmlInfo.getAttribute("acc_check_obj_type"), Misc.G_FOR_ORDER);
        	//$$DIFF_FROM_CACHE  }
           dimInfo.m_getQuery = Misc.getParamAsString(xmlInfo.getAttribute("dyn_query"),null);
           dimInfo.m_lovListRequiresDynamicOrg = "1".equals(xmlInfo.getAttribute("dyn_query_org_dependent"));
           
        }
                 
   	 if (dimInfo.m_getQuery != null && !dimInfo.m_lovListRequiresDynamicOrg)
   		 dimInfo.checkAndPopulateLovListFromDB(dbConn, null);
        
        return dimInfo;

    }

    private static void readLovNode(DimInfo dimInfo, Element lovNode) {
        for (Node n = lovNode.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != 1)
               continue;
            Element e = (Element) n;
            ValInfo valInfo = new ValInfo(e);
            ValInfo orig = dimInfo.getValInfo(valInfo.m_id);
            if (orig == null) {
            	dimInfo.addValInfo(valInfo);
            }
            else {
                 if (orig.m_otherProp != null)
                    orig.m_otherProp = (ArrayList) valInfo.m_otherProp.clone();
                 orig.m_id = valInfo.m_id;
                 orig.m_name = valInfo.m_name;
                 orig.m_sn = valInfo.m_sn;
                 orig.m_isSp = valInfo.m_isSp;
            }
        }
     }//end of func
    private static MiscInner.PairElemInt getAttribCatNodeNotFromDimList(String attribCat, Element refDimensionsNodeInInternal) { //IS Fine only used in creation
        int retType = Cache.LOV_NO_VAL_TYPE; //the initialization is important basically if non lov in dimensions that overrides type found elsewhere
                                         //but if lov then just take from whereever it was found .. but being lazy so not checking if is
                                         //found only in dimensions and there  for LOV_NO_TYPE

        Element retNode = null;
        NodeList nl;
        Element node;
        NodeList attribNl;
        int definedWhere = 0;
        boolean found = false;
        if  (refDimensionsNodeInInternal  == null) {// this really needs to be in the last so that variables that are not
             nl = internalXML.getElementsByTagName("dimensions");
             node = (Element) nl.item(0);
             attribNl = node.getElementsByTagName(attribCat);
             if (attribNl.getLength() > 0) { // found it
                 refDimensionsNodeInInternal = (Element) attribNl.item(0);
             }
          }

          if (refDimensionsNodeInInternal != null) {
              String typeDefined = refDimensionsNodeInInternal.getAttribute("type");
              if (!"lov".equals(typeDefined)) {
            	  // dev 09 Aug 2010
            	  return new MiscInner.PairElemInt(refDimensionsNodeInInternal, "string".equals(typeDefined) ? Cache.STRING_TYPE : "date".equals(typeDefined) ? Cache.DATE_TYPE: "file".equals(typeDefined) ? Cache.FILE_TYPE : "img".equals(typeDefined) ? Cache.IMAGE_TYPE : "integer".equals(typeDefined) ? Cache.INTEGER_TYPE : Cache.NUMBER_TYPE);
                 
            	  // sameer 04112006
//                  return new MiscInner.PairElemInt(refDimensionsNodeInInternal, "string".equals(typeDefined) ? STRING_TYPE : "date".equals(typeDefined) ? DATE_TYPE: "file".equals(typeDefined) ? FILE_TYPE : "img".equals(typeDefined) ? IMAGE_TYPE : NUMBER_TYPE);              
                  // end sameer 04112006
              }
          }

          nl = lovXML.getElementsByTagName("standard");
          node = (Element)nl.item(0);
          attribNl = node.getElementsByTagName(attribCat);
          if (attribNl.getLength() > 0) { // found it
              retNode = (Element) attribNl.item(0);
              retType = Cache.LOV_TYPE;
              definedWhere = LOV_STANDARD;
              found = true;
          }
          if (retNode == null) {
             nl = lovXML.getElementsByTagName("custom");
             node = (Element) nl.item(0);
             nl = node.getElementsByTagName("lov");
             node = (Element) nl.item(0);
             attribNl = node.getElementsByTagName(attribCat);
             if (attribNl.getLength() > 0) { // found it
                 retNode = (Element) attribNl.item(0);
                 retType = Cache.LOV_TYPE;
                 definedWhere = LOV_CUSTOM;
                 found = true;
             }
          }
          if (retNode == null) {
             nl = lovXML.getElementsByTagName("custom");
             node = (Element) nl.item(0);
             nl = node.getElementsByTagName("other_custom_fields");
             node = (Element) nl.item(0);
             attribNl = node.getElementsByTagName(attribCat);
             if (attribNl.getLength() > 0) { // found it
                  retNode = (Element) attribNl.item(0);
                  String nodeTypeString = retNode.getAttribute("type");
                  if (nodeTypeString.equalsIgnoreCase("number"))
                      retType = Cache.NUMBER_TYPE;
                  else if (nodeTypeString.equalsIgnoreCase("string"))
                      retType = Cache.STRING_TYPE;
                  else if (nodeTypeString.equalsIgnoreCase("date"))
                      retType = Cache.DATE_TYPE;
                  // sameer 04112006
                  else if (nodeTypeString.equalsIgnoreCase("file"))
                      retType = Cache.FILE_TYPE;
                  else if (nodeTypeString.equalsIgnoreCase("img"))
                      retType = Cache.IMAGE_TYPE;
                  // end sameer 04112006
                  else
                      retType = Cache.LOV_NO_VAL_TYPE;
                  definedWhere = LOV_CUSTOM;
                  found = true;
             }
          }
          if (retNode == null) {
             nl = internalXML.getElementsByTagName("constants");
             node = (Element) nl.item(0);
             attribNl = node.getElementsByTagName(attribCat);
             if (attribNl.getLength() > 0) { // found it
                  retNode = (Element) attribNl.item(0);
                  retType = Cache.LOV_TYPE;
                  definedWhere = Cache.INTERNAL_CONST;
                  found = true;
             }
          }
          if (retNode == null) {
             nl = internalXML.getElementsByTagName("lov");
             node = (Element) nl.item(0);
             attribNl = node.getElementsByTagName(attribCat);
             if (attribNl.getLength() > 0) { // found it
                 retNode = (Element) attribNl.item(0);
                 retType = Cache.LOV_TYPE;
                 definedWhere = Cache.INTERNAL_LOV;
                 found = true;
             }
          }

          if (refDimensionsNodeInInternal != null) {
             String typeAttrib = refDimensionsNodeInInternal.getAttribute("type");
             boolean isNumber = typeAttrib.equals("number");
             if (isNumber)
                retType =  Cache.NUMBER_TYPE;
             else {
                boolean isString = typeAttrib.equals("string");
                if (isString)
                  retType = Cache.STRING_TYPE;
                if ("date".equals(typeAttrib))
                  retType = Cache.DATE_TYPE;
    			// sameer 04112006
    			      if ("file".equals(typeAttrib))
                  retType = Cache.FILE_TYPE;
                if ("img".equals(typeAttrib))
                  retType = Cache.IMAGE_TYPE;
                // sameer 04112006
             }
             if (typeAttrib.equals("lov") && !found) { //check if  is a subset_of
                String subSetOf = refDimensionsNodeInInternal.getAttribute("subset_of");
                if (subSetOf != null && subSetOf.length() != 0) {
                   retType = Cache.LOV_TYPE;
                   retNode = refDimensionsNodeInInternal;
                   definedWhere = LOV_STANDARD;
                }
                // to populate lov from data base - dyn_query
                String dynQuery = refDimensionsNodeInInternal.getAttribute("dyn_query");
                if (dynQuery != null && dynQuery.length() != 0) {
                   retType = Cache.LOV_TYPE;
                   retNode = refDimensionsNodeInInternal;
                   definedWhere = LOV_STANDARD;
                }
             }


          }
          if (retNode == null) {
             retNode = refDimensionsNodeInInternal;// this really needs to be in the last so that variables that are not
             definedWhere = Cache.INTERNAL_DIMLIST;
             //the initialization is important basically if non lov in dimensions that overrides type found elsewhere
             //but if lov then just take from whereever it was found .. but being lazy so not checking if is
             //found only in dimensions and there  for LOV_NO_TYPE
          }

          if (retNode != null)
             return new MiscInner.PairElemInt(retNode, retType);         
          else
             return null;
      }

}

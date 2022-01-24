package com.ipssi.gen.utils;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ipssi.cache.NewVehicleData;
import com.ipssi.cache.VehicleDataInfo;
import com.ipssi.processor.utils.ChannelTypeEnum;
import com.ipssi.processor.utils.GpsData;
import com.ipssi.processor.utils.GpsDataResultSetReader;
import com.ipssi.processor.utils.Vehicle;
import com.ipssi.processor.utils.VehicleWithName;
import com.ipssi.reporting.common.util.Common;

public class ProcessConfig {
	private static volatile boolean g_copyDone = false;
	private static String fullFileName = null;
	private  int doCopy = Misc.getUndefInt();
    public  Date start = null;
    public  Date end = null;
    public  ArrayList<Integer> org = null;
    private  int gapSec = Misc.getUndefInt();
    private  double speedThreshold = Misc.getUndefInt();
    private  double distThreshold = Misc.getUndefInt();
    
    private static Document configXML = null;
	private static String g_configFileName = "config.xml";
	private String fromServer = null;
	private String fromDB = null;
	private String fromPort = null;
	private String fromUser = null;
	private String fromPassword = null;
	private String toServer1 = null;
	private String toServer2 = null;
	private String toServer3 = null;
	private boolean copyInJava = true;
	private double addnlMultForGpsId = 1.0;
	private boolean justCopy = false;
	private int dosmartSetup = 2;//-1 do as specified from toConn, else 0 => do only for specified in from conn, 1=> for all vehicles regardless of redirection, 2=> for only redirected .. generally should be 2
	private boolean doUpdDistOnly = false;
	private boolean doUpdName = false;
	private double minDistMovedForNameCalc = 0.0;
	private boolean doPrevToZero = true;
	public Date updDistStart = null;
	public String toString(){
		return " [DoCopy : " + doCopy + ", Start : " + start + ", End : " + end + ", Org : " + org + ", GapSecond : " + gapSec ;
	}
//	public static int getDoCopyVal(){
//		ProcessConfig retval = read();
//		return retval.doCopy;
//	}
	public static synchronized void read() {
		processConfigFile();
	}
	public static synchronized void processConfigFile() {
		if(g_copyDone)
			return ;
//	public static synchronized ProcessConfig read(){
		Misc.loadCFGConfigServerProp();
		ProcessConfig retval = new ProcessConfig();
    	FileInputStream inp = null;
    	try {
    		String serverName = Misc.getServerName();
    		boolean isdefault = "default".equalsIgnoreCase(serverName) || serverName == null;
    		try {
    			fullFileName = Misc.getServerConfigPath()+System.getProperty("file.separator")+serverName+System.getProperty("file.separator")+g_configFileName;
    			inp = new FileInputStream(fullFileName);
			}
			catch (Exception e2) {
				
			}
    		
    		if(isdefault && inp == null){
    			try {
    				fullFileName =Misc.getServerConfigPath()+System.getProperty("file.separator")+g_configFileName;
    				inp = new FileInputStream(fullFileName);
    			}
    			catch (Exception e) {
    				
    			}
    		}						
    		if (inp == null)
    			return;

            MyXMLHelper test = new MyXMLHelper(inp, null);
            retval.configXML = test.load();
            Element elem = retval.configXML.getDocumentElement();
            inp.close();
            inp = null;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (elem != null) {
                retval.doCopy = Misc.getParamAsInt(elem.getAttribute("do_copy"), retval.doCopy);
                retval.start = Misc.getParamAsDate(elem.getAttribute("start"), null, formatter);
                retval.end = Misc.getParamAsDate(elem.getAttribute("end"), null, formatter);
                retval.copyInJava = !"0".equals(elem.getAttribute("copy_in_java"));
                retval.fromServer = Misc.getParamAsString(elem.getAttribute("from_server"), "203.197.197.17");
                retval.fromDB = Misc.getParamAsString(elem.getAttribute("from_db"), "ipssi");
                retval.fromPort = Misc.getParamAsString(elem.getAttribute("from_port"), "3306");
                retval.fromUser = Misc.getParamAsString(elem.getAttribute("from_user"), "jboss");
                retval.fromPassword = Misc.getParamAsString(elem.getAttribute("from_password"), "redhat_1234");
                retval.toServer1 = Misc.getParamAsString(elem.getAttribute("to_server1"));
                retval.toServer2 = Misc.getParamAsString(elem.getAttribute("to_server2"));
                retval.toServer3 = Misc.getParamAsString(elem.getAttribute("to_server3"));
                retval.dosmartSetup = Misc.getParamAsInt(elem.getAttribute("smart_setup"), retval.dosmartSetup);
                retval.justCopy = "1".equals(elem.getAttribute("just_copy"));
                retval.addnlMultForGpsId = Misc.getParamAsDouble(elem.getAttribute("gps_id_mult"),retval.addnlMultForGpsId);
            	retval.doUpdDistOnly = "1".equals(elem.getAttribute("do_upd_dist_only"));
            	retval.doUpdName = "1".equals(elem.getAttribute("do_upd_name"));
            	retval.minDistMovedForNameCalc = Misc.getParamAsDouble(elem.getAttribute("min_dist_name_calc"),retval.minDistMovedForNameCalc);
            	retval.doPrevToZero = !"0".equals(elem.getAttribute("do_prev_to_zero"));
            	retval.updDistStart = Misc.getParamAsDate(elem.getAttribute("upd_dist_start"), null, formatter);
                String orgs = elem.getAttribute("org");
                if (orgs != null && orgs.length() > 0)  {
                	retval.org = new ArrayList<Integer>();
                	Misc.convertValToVector(orgs, retval.org);
                }
                retval.gapSec = Misc.getParamAsInt(elem.getAttribute("gap_sec"), 30);// default 30 seconds
                retval.speedThreshold = Misc.getParamAsDouble(elem.getAttribute("speed_threshold"), 2.4);// default 2.4 
                retval.distThreshold = Misc.getParamAsDouble(elem.getAttribute("dist_threshold"), 0.05);// default 0.05 
            }
            inp = null;
            test = null;
            if (retval.doCopy == 1 && retval.copyInJava) {
            	System.out.println("ProcessConfig.processConfigFile() doCopy and copyInJava");
            	doProcessConfigInJava(retval);
            	g_copyDone = true;
            	return;
            }if (retval.doUpdDistOnly && retval.doUpdName) {
            	System.out.println("ProcessConfig.processConfigFile() doUpdDistOnly and doUpdName");
            	doUpdDistAndName(retval);
            	g_copyDone = true;
            	return;
            }
            if (retval.doUpdDistOnly) {
            	System.out.println("ProcessConfig.processConfigFile() doUpdDistOnly");
            	doUpdDistOnly(retval);
            	g_copyDone = true;
            	return;
            }
            if(retval.doCopy == 1){
            	System.out.println("ProcessConfig.processConfigFile() doCopy");
    			// do procedure call
            	Connection conn = null;
        		CallableStatement cs = null;
        		PreparedStatement ps = null;
        		boolean destroyWhenReturning = false;
        		try {
        			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        			boolean auto = conn.getAutoCommit();
        			if (conn.getAutoCommit())
        				conn.setAutoCommit(false);
        			//conn.setAutoCommit(true);
        			StringBuilder query = new StringBuilder();
        			System.out.println("ProcessConfig.callProcedure() started");
        			ps = conn.prepareStatement("truncate "+retval.fromDB+".config_calc");
        			ps.execute();
        			ps.close();
        			if (!conn.getAutoCommit())
        				conn.commit();
        			query.append("insert into "+retval.fromDB+".config_calc (select distinct(vehicle.id) vehicle_id , 0, 0, 0, vehicle_id ")
    					.append("from vehicle  ")
    					.append("left outer join "+retval.fromDB+".port_nodes custleaf on (custleaf.id = vehicle.customer_id and vehicle.status =1) ") 
    					.append("left outer join "+retval.fromDB+".vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id)  ")
    					.append("left outer join "+retval.fromDB+".port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) ")
    					.append("join "+retval.fromDB+".port_nodes anc  on (anc.id in ( ");
    				Misc.convertInListToStr(retval.org, query);
    				query.append(") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) ")
    					.append("or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) )");
        			ps = conn.prepareStatement(query.toString());
        			ps.execute();
        			ps.close();
        			if (!conn.getAutoCommit())
        				conn.commit();
        			System.out.println("ProcessConfig.callProcedure() populate config_calc completed");
        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        			String startDate = null;
        			String endDate = null;
        			if(retval.start != null){
        				startDate = sdf.format(retval.start);
        				startDate = "'"+startDate+"'";
        			}
        			if(retval.end != null){
        				endDate = sdf.format(retval.end);
        				endDate = "'"+endDate+"'";
        			}
        			cs = conn.prepareCall("{call "+retval.fromDB+".ILCopyLoggedData("+startDate+","+endDate+")}");            
        			cs.execute();            
        			cs.close();
        			if (!conn.getAutoCommit())
        				conn.commit();
        			
        			System.out.println("ProcessConfig.callProcedure() ILCopyLoggedData completed");
        			
        			if(retval.start != null){
        				retval.start = new Date(retval.start.getTime() - 24*60*60*1000);
        				startDate = sdf.format(retval.start);
        				startDate = "'"+startDate+"'";
        			}
        			cs = conn.prepareCall("{call "+retval.fromDB+".ILCleanupLoggedData("+startDate+","+endDate+","+retval.gapSec+")}");            
        			cs.execute();            
        			cs.close();
        			if (!conn.getAutoCommit())
        				conn.commit();
        			
        			System.out.println("ProcessConfig.callProcedure() ILCleanupLoggedData completed");
        			
        			cs = conn.prepareCall("{call "+retval.fromDB+".ILUpdCummDistWithSpeed("+startDate+","+retval.speedThreshold+","+retval.distThreshold+")}");            
        			cs.execute();            
        			cs.close();
        			if (!conn.getAutoCommit())
        				conn.commit();
        			
        			System.out.println("ProcessConfig.callProcedure() ILUpdCummDistWithSpeed completed");
        			
        			System.out.println("ProcessConfig.callProcedure() reset completed ");
        			if (!conn.getAutoCommit() != auto)
        				conn.setAutoCommit(auto);
        		}catch(Exception e){
        			System.out.println("ProcessConfig.read() " + e.getMessage());
        			e.printStackTrace();
        			destroyWhenReturning = true;
        		}
        		finally {
        			try {
        				if (conn != null && conn.getAutoCommit()){
        					conn.setAutoCommit(false);
        				}
        				if (conn != null)
        					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyWhenReturning);
        			}
        			catch (Exception e2) {
        			    e2.printStackTrace();
        			    //eat it
        			}
   
        		}
        		System.out.println("ProcessConfig.callProcedure() ended");
    		}
            
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		//eat it
    	}
    	finally {

    		if (inp != null) {
    			try {
    				inp.close();
    			}
    			catch (Exception e1) {
    				e1.printStackTrace();
    				//eat it
    			}
    			

    		}
    		ProcessConfig.reset();
 			g_copyDone = true;

    	}
    	return;
    }
	public static ArrayList<MiscInner.Pair> smartSetupConfigCalc(Connection fromConn, Connection toConn, ProcessConfig retval) throws Exception {
		//get all vehicles which are being redirected to current server
		ArrayList<MiscInner.Pair> thelist = new ArrayList<MiscInner.Pair>();
		if (retval.dosmartSetup == -1 || retval.dosmartSetup == 0) {
			PreparedStatement ps = toConn.prepareStatement("select from_vehicle_id, vehicle_id from config_calc where copy_status is null or copy_status=0");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int fromVehicleId = Misc.getRsetInt(rs,1);
				int toVehicleId = Misc.getRsetInt(rs, 2);
				if (Misc.isUndef(fromVehicleId))
					fromVehicleId = toVehicleId;
				if (Misc.isUndef(toVehicleId))
					toVehicleId = fromVehicleId;
				if (Misc.isUndef(fromVehicleId))
					continue;
				thelist.add(new MiscInner.Pair(fromVehicleId, toVehicleId));
			}
			rs = Misc.closeRS(rs);
			ps = Misc.closePS(ps);
		}
		else {
			StringBuilder sb = new StringBuilder("select v.id, ve.other_vehicle_id, v.server_ip_1, v.server_ip_2 "+
					" from vehicle v left outer join vehicle_extended ve on (v.id = ve.vehicle_id) "+
					" left outer join port_nodes custleaf on (custleaf.id = v.customer_id) "+
					" left outer join vehicle_access_groups vag on (vag.vehicle_id = v.id) "+
					" left outer join port_nodes leaf on (leaf.id = vag.port_node_id) "+
					" left outer join port_nodes anc on ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) or (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number)) "+
					" where (v.status in (1)) "+
					" and anc.id in (");
			Misc.convertInListToStr(retval.org, sb);
			sb.append(")");
			PreparedStatement ps = fromConn.prepareStatement(sb.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int fromVehicleId = Misc.getRsetInt(rs,1);
				int toVehicleId = Misc.getRsetInt(rs, 2);
				if (Misc.isUndef(fromVehicleId))
					fromVehicleId = toVehicleId;
				if (Misc.isUndef(toVehicleId))
					toVehicleId = fromVehicleId;
				if (Misc.isUndef(fromVehicleId))
					continue;
				String toIp1 = rs.getString(3);
				String toIp2 = rs.getString(4);
				boolean toAdd = retval.dosmartSetup == 1 || 
				   (toIp1 != null && (toIp1.equals(retval.toServer1) || toIp1.equals(retval.toServer2) || toIp1.equals(retval.toServer3)))
						 || (toIp2 != null && (toIp2.equals(retval.toServer1) || toIp2.equals(retval.toServer2) || toIp2.equals(retval.toServer3)))
								 ;
				if (!toAdd)
					continue;
				thelist.add(new MiscInner.Pair(fromVehicleId, toVehicleId)); 
			}
			rs.close();
			ps.close();
		}
		
		//set the list in toConn, so that update status could be done
		if (retval.dosmartSetup != -1) {
			PreparedStatement ps = toConn.prepareStatement("truncate table config_calc");
			ps.execute();
			ps = Misc.closePS(ps);
			ps = toConn.prepareStatement("insert into config_calc(from_vehicle_id, vehicle_id, copy_status, cleanup_status, cumm_status) values (?,?,0,0,0)");
			for (MiscInner.Pair pr:thelist) {
				ps.setInt(1, pr.first);
				ps.setInt(2, pr.second);
				ps.addBatch();
			}
			ps.executeBatch();
			ps = Misc.closePS(ps);
			if (!toConn.getAutoCommit())
				toConn.commit();
		}
		return thelist;
	}
	public static void doProcessConfigInJava(ProcessConfig retval) {
		Connection fromConn = null;
		Connection toConn = null;
		boolean destroyIt = false;
		try{
			Misc.loadCFGConfigServerProp();
			toConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			String connectString = "jdbc:mysql://"+ retval.fromServer + ":" + retval.fromPort +"/"+retval.fromDB+"?zeroDateTimeBehavior=convertToNull&"+"user="+retval.fromUser+"&password="+retval.fromPassword;
			
			//String connectString = Misc.G_DO_ORACLE ? "jdbc:oracle:thin:@" : "jdbc:mysql://";// "jdbc:sqlserver://";		connectString = connectString + connProps.getProperty("DBConn.host", "localhost") + ":" + connProps.getProperty("DBConn.port", "1521") + ":"
			//connectString = connectString + retval.fromServer + ":" + retval.fromPort + "/"
				//+retval.fromDB +"?zeroDateTimeBehavior=convertToNull&";			
			fromConn = 	DriverManager.getConnection(connectString);
			
			ArrayList<MiscInner.Pair> vehList = smartSetupConfigCalc(fromConn, toConn, retval);
			PreparedStatement copy = toConn.prepareStatement("insert ignore into logged_data (vehicle_id, attribute_id, gps_record_time, longitude, latitude, attribute_value, source, name, speed, updated_on, gps_id) values (?,?,?,?,?,?,?,?,?,?,?)");
			int vehCount = 0;
			PreparedStatement updCfgCalc = toConn.prepareStatement("update config_calc set copy_status=1 where vehicle_id=?");
			PreparedStatement updCfgCalcExc = toConn.prepareStatement("update config_calc set copy_status=2 where vehicle_id=?");
			
			for (MiscInner.Pair pr: vehList) {
				int toVehicleId = pr.second;
				int fromVehicleId = pr.first;
				vehCount++;
				try{
					System.out.println("Copying VehCount:"+vehCount+" from:"+fromVehicleId+ " to:"+toVehicleId);
					CacheTrack.VehicleSetup vehSetup = CacheTrack.VehicleSetup.getSetup(toVehicleId, toConn);
					double orgMaxSpeed = -1 ;
					double orgThresholdSpeed = -1;
					try {
						Cache _cache = Cache.getCacheInstance(toConn);
						int userOrgControlId = vehSetup.m_ownerOrgId;
						MiscInner.PortInfo userOrgControlOrg = _cache.getPortInfo(userOrgControlId, toConn);
						ArrayList orgMaxSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_MAX_SPEED);
						ArrayList orgThresholdSpeedList = (ArrayList) userOrgControlOrg.getDoubleParams(OrgConst.ID_USER_LEAST_SPEED);
						orgMaxSpeed = orgMaxSpeedList == null || orgMaxSpeedList.size() == 0 || Common.isNull(orgMaxSpeedList. get(0)) ? 100 : ((Double) orgMaxSpeedList
								.get(0));
						orgThresholdSpeed = orgThresholdSpeedList == null || orgThresholdSpeedList.size() == 0 || Common.isNull(orgThresholdSpeedList. get(0)) ? 4.8 : ((Double) orgThresholdSpeedList
								.get(0));
					} catch (Exception e) {
						e.printStackTrace();
					}

					String getQuerySel = "select logged_data.vehicle_id,  cast(logged_data.longitude as DECIMAL(9,6)) longitude, cast(logged_data.latitude as DECIMAL(9,6))  latitude, logged_data.attribute_id, cast(attribute_value as decimal(14,3)) attribute_value, gps_record_time, source, name,updated_on,cast(speed as decimal(10,3)) speed, gps_id from logged_data ";
					HashMap<Integer, GpsData> justBefore = new HashMap<Integer, GpsData>();
					HashMap<Integer, GpsData> justBeforeInTo = new HashMap<Integer, GpsData>();
					if (retval.start != null) {
						String q = getQuerySel +"  join (select vehicle_id, attribute_id, max(gps_record_time) grt from logged_data where vehicle_id = ? and gps_record_time < ? group by vehicle_id,attribute_id) mi on (mi.vehicle_id = logged_data.vehicle_id and mi.attribute_id=logged_data.attribute_id and mi.grt = logged_data.gps_record_time) ";
						PreparedStatement ps = fromConn.prepareStatement(q);
						java.sql.Timestamp ts = Misc.utilToSqlDate(retval.start);
						ps.setInt(1, fromVehicleId);
						ps.setTimestamp(2, ts);
						ResultSet rs = ps.executeQuery();
						Vehicle v = null;
						GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true); 
						while ((v = reader.readGpsData()) != null) {
							justBefore.put(v.getGpsData().getDimId(), v.getGpsData());
						}
						rs = Misc.closeRS(rs);
						ps = Misc.closePS(ps);

						ps = toConn.prepareStatement(q);
						ts = Misc.utilToSqlDate(retval.start);
						ps.setInt(1, fromVehicleId);
						ps.setTimestamp(2, ts);
						rs = ps.executeQuery();
						v = null;
						reader = new GpsDataResultSetReader(rs, true); 
						while ((v = reader.readGpsData()) != null) {
							justBeforeInTo.put(v.getGpsData().getDimId(), v.getGpsData());
						}
						rs = Misc.closeRS(rs);
						ps = Misc.closePS(ps);

						if (!retval.justCopy) {
							ps = toConn.prepareStatement("delete from logged_data where vehicle_id=? and gps_record_time >= ?");
							ps.setInt(1, toVehicleId);
							ps.setTimestamp(2, ts);
							ps.execute();
							ps = Misc.closePS(ps);
						}
					}
					else {
						if (!retval.justCopy) {
							PreparedStatement ps = toConn.prepareStatement("delete from logged_data where vehicle_id=?");
							ps.setInt(1, toVehicleId);
							ps.execute();
							ps = Misc.closePS(ps);
						}
					}
					int countPts = 0;
					int ptsLimit = 10000;
					PreparedStatement ps = fromConn.prepareStatement(getQuerySel+" where vehicle_id=? "+(retval.start != null ? " and gps_record_time >= ?" : "")+" order by attribute_id, gps_record_time ",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
					ps.setFetchSize(Integer.MIN_VALUE);
					ps.setInt(1, fromVehicleId);
					if (retval.start != null) {
						java.sql.Timestamp ts = Misc.utilToSqlDate(retval.start);
						ps.setTimestamp(2, ts);
					}

					ResultSet rs = ps.executeQuery();
					GpsDataResultSetReader reader = new GpsDataResultSetReader(rs, true);
					VehicleWithName vehicle = null;
					int prevAttributeId = Misc.getUndefInt();
					GpsData prev = null;
					while ((vehicle = reader.readGpsDataPlain()) != null) {
						GpsData gpsData = vehicle.getGpsData();
						if (gpsData.getDimId() != prevAttributeId) {
							prev = justBeforeInTo.get(gpsData.getDimId());
							if (prev == null && !retval.doPrevToZero)
								prev = justBefore.get(gpsData.getDimId());
							prevAttributeId = gpsData.getDimId();
						}
						if (!retval.justCopy && gpsData.getDimId() == 0) {
							long gapTS = (gpsData.getGps_Record_Time() - (prev == null ? 0 : prev.getGps_Record_Time()))/1000;
							if (gapTS < retval.gapSec)
								continue;
						}
						if (!retval.justCopy) {
							double distRelPrev = 0;
							if (gpsData.getDimId() == 0 && prev != null && prev != null && prev.isValidPoint() && gpsData.isValidPoint()) {
								distRelPrev = prev.fastGeoDistance(gpsData);
								if (gpsData.getGpsRecordingId() < 0)
									gpsData.setGpsRecordingId(Misc.getUndefInt());
								if (!Misc.isEqual(retval.addnlMultForGpsId,1) && gpsData.getGpsRecordingId() > 0) {
									double v = gpsData.getGpsRecordingId();
									v *= retval.addnlMultForGpsId;
									int iv = (int) Math.round(v);
									gpsData.setGpsRecordingId(iv);
								}
							}
							NewVehicleData.newUpdateDist(fromConn, vehSetup, prev, gpsData, distRelPrev, orgMaxSpeed, orgThresholdSpeed);
						}
						prev = gpsData;
						//now save
						copy.setInt(1, toVehicleId);
						copy.setInt(2, gpsData.getDimId());
						copy.setTimestamp(3, Misc.utilToSqlDate(gpsData.getGps_Record_Time()));
						copy.setDouble(4, gpsData.getLongitude());
						copy.setDouble(5, gpsData.getLatitude());
						copy.setDouble(6, gpsData.getValue());
						ChannelTypeEnum src = gpsData.getSourceChannel();
						copy.setInt(7, src == ChannelTypeEnum.CURRENT ? 0 : src == ChannelTypeEnum.DATA ? 1 : 2);
						copy.setString(8, vehicle.getName());
						copy.setDouble(9, gpsData.getSpeed());
						copy.setTimestamp(10, Misc.utilToSqlDate(gpsData.getGpsRecvTime()));
						copy.setInt(11, gpsData.getGpsRecordingId());
						copy.addBatch();
						countPts++;
						if (countPts%ptsLimit == 0) {
							System.out.println("Inserting data VehCount:"+vehCount+" from:"+fromVehicleId+ " to:"+toVehicleId+ " countPts:"+countPts);
							copy.executeBatch();
							if (!toConn.getAutoCommit())
								toConn.commit();
							System.out.println("Inserted data VehCount:"+vehCount+" from:"+fromVehicleId+ " to:"+toVehicleId+ " countPts:"+countPts);

						}
					}//for all pts
					if (countPts%ptsLimit > 0) {
						System.out.println("Inserting data VehCount:"+vehCount+" from:"+fromVehicleId+ " to:"+toVehicleId+ " countPts:"+countPts);
						copy.executeBatch();
						if (!toConn.getAutoCommit())
							toConn.commit();
						System.out.println("Inserted data VehCount:"+vehCount+" from:"+fromVehicleId+ " to:"+toVehicleId+ " countPts:"+countPts);
					}
					rs.close();
					ps.close();
					updCfgCalc.setInt(1, toVehicleId);
					updCfgCalc.execute();
					if (!toConn.getAutoCommit())
						toConn.commit();
				}catch(Exception ex ){
					System.out.println("[ProcessConfig] Error from:"+fromVehicleId+ " to:"+toVehicleId);
					ex.printStackTrace();
					updCfgCalcExc.setInt(1, toVehicleId);
					updCfgCalcExc.execute();
					if (!toConn.getAutoCommit())
						toConn.commit();
				}
			}//for all vehicle
			updCfgCalc.close();
			Misc.closeStatement(updCfgCalcExc);
			System.out.println("Copying done");
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		}
		finally {
			try {
				if (fromConn != null)
					fromConn.close();
			}
			catch (Exception e) {
			
			}
			try {
				if (toConn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(toConn, destroyIt);
			}
			catch (Exception e) {
			
			}
		}
	}
	
	public static void doUpdDistAndName(ProcessConfig retval) {
		Connection toConn = null;
		boolean destroyIt = false;
		try{
			Misc.loadCFGConfigServerProp();
			toConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int doSmartSetupOrig = retval.dosmartSetup;
			retval.dosmartSetup = -1;//force to get list of vehicle whose dist to be updated from config_calc
			ArrayList<MiscInner.Pair> vehList = smartSetupConfigCalc(null, toConn, retval);
			retval.dosmartSetup = doSmartSetupOrig;
			Date distStart = retval.updDistStart;
			int vehCount = 0;
			PreparedStatement updCfgCalc = toConn.prepareStatement("update config_calc set copy_status=1 where vehicle_id=?");
			for (MiscInner.Pair pr: vehList) {
				try {
					int fromVehicleId = pr.first;
					vehCount++;
					System.out.println("Updating dist and name of VehCount:"+vehCount+" vehicle:"+fromVehicleId);
					NewVehicleData.updateAllDistCalcAndLocation(fromVehicleId, distStart == null ? 1 : distStart.getTime(), 0,retval.minDistMovedForNameCalc);
					updCfgCalc.setInt(1, fromVehicleId);
					updCfgCalc.execute();
					if (!toConn.getAutoCommit())
						toConn.commit();
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			}//for all vehicle
			updCfgCalc.close();
			System.out.println("Updating Dist and name done");
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		}
		finally {
			
			try {
				if (toConn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(toConn, destroyIt);
			}
			catch (Exception e) {
			
			}
		}
	}

	public static void doUpdDistOnly(ProcessConfig retval) {
		Connection toConn = null;
		boolean destroyIt = false;
		try{
			Misc.loadCFGConfigServerProp();
			toConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int doSmartSetupOrig = retval.dosmartSetup;
			retval.dosmartSetup = -1;//force to get list of vehicle whose dist to be updated from config_calc
			ArrayList<MiscInner.Pair> vehList = smartSetupConfigCalc(null, toConn, retval);
			retval.dosmartSetup = doSmartSetupOrig;
			int vehCount = 0;
			PreparedStatement updCfgCalc = toConn.prepareStatement("update config_calc set copy_status=1 where vehicle_id=?");
			for (MiscInner.Pair pr: vehList) {
				try {
					int fromVehicleId = pr.first;
					vehCount++;
					System.out.println("Updating dist of VehCount:"+vehCount+" vehicle:"+fromVehicleId);
					NewVehicleData.updateAllDistCalc(fromVehicleId, -1, 0);
					updCfgCalc.setInt(1, fromVehicleId);
					updCfgCalc.execute();
					if (!toConn.getAutoCommit())
						toConn.commit();
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			}//for all vehicle
			updCfgCalc.close();
			System.out.println("Updating Dist done");
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
		}
		finally {
			
			try {
				if (toConn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(toConn, destroyIt);
			}
			catch (Exception e) {
			
			}
		}
	}

	public static void main(String[] args) {
		read();
	}
	
	
	/*public static void callProcedure (){
		Connection conn = null;
		CallableStatement cs = null;
		boolean destroyWhenReturning = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			conn.setAutoCommit(true);
			cs = conn.prepareCall("{call copyLoggedData('2013-09-11 00:00:28',now())}");            
//			cs.registerOutParameter(1, Types.VARCHAR);            
//			cs.setString(2, office);            
			cs.execute();            
//			String str = cs.getString(1);call cleanupLoggedData('2013-09-11 00:00:28',now(),45);
			cs.close();
			cs = conn.prepareCall("{call cleanupLoggedData('2013-09-11 00:00:28',now(),45)}");            
			cs.execute();            
			cs.close();
			cs = conn.prepareCall("{call updCummDistWithSpeed(481,null,'2013-10-21 00:00',null,null)}");            
			cs.execute();            
			cs.close();
			       
		}catch(Exception e){
			System.out.println("ProcessConfig.callProcedure() " + e.getMessage());
			e.printStackTrace();
			destroyWhenReturning = true;
		}
		finally {
			try {
				if (conn != null && conn.getAutoCommit()){
					conn.setAutoCommit(false);
				}
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyWhenReturning);
			}
			catch (Exception e2) {
			    e2.printStackTrace();
			    //eat it
			}
		}
	}*/
    
	/*public static void  readAndReset() {
		ProcessConfig retval = read();
		retval.reset();
	}*/
	
    public static void reset() {
    	FileWriter fout = null;
        PrintWriter outw = null;
        try {
	    	if (configXML != null) {
	            Element elem = configXML.getDocumentElement();
	            elem.setAttribute("do_copy","0");
	            elem.setAttribute("do_upd_dist_only", "0");
	            fout = new FileWriter(fullFileName);
	            outw = new PrintWriter(fout, true);
	            com.ipssi.gen.utils.MyXMLHelper helper = new com.ipssi.gen.utils.MyXMLHelper(null,outw);
	            helper.save(configXML);
	            outw.close();
	            outw = null;
	            fout.close();
	            fout = null;
	    	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        	//eat it
        }
        finally {
        	try {
        		if (outw != null)
        			outw.close();
        		if (fout != null)
        			fout.close();
        	}
        	catch (Exception e1) {
        		e1.printStackTrace();
        		//eat it
        	}
        }
    }
    
}

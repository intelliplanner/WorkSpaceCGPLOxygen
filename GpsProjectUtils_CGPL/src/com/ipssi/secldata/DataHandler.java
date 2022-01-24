package com.ipssi.secldata;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Triple;
	//CURRENTLY DUPLICATION OF CODE WITH com.ipssi.secldata.GetData ... eventually move to common package though maxSize may need to be changed

public class DataHandler {
	private static ConcurrentHashMap<String,Triple<ArrayList<String>,Date,Integer>> dataSenderCache = new ConcurrentHashMap<String, Triple<ArrayList<String>,Date,Integer>>();
	private static ConcurrentHashMap<String , Integer> systemInfoMap = null;
	public static String processDeviceRequest(Connection conn, String gpsString, boolean overWeb)  {
		String responseStr = null;
		try{
			System.out.println("@@secl-communication start:command:"+gpsString);
			if (gpsString !=null && gpsString.contains("@@secl")) {
				System.out.println("@@secl-communication start");
				responseStr = TxExecutor.processTxCmd(conn, gpsString);								
				if (responseStr == null) {
					if (gpsString.contains("@@secl,GET") || gpsString.contains("@@secl,RESUME") || gpsString.contains("@@secl,CLEAR")){
						//@@secl,sender,27
						String tokens[] = gpsString.split(",");
						if (tokens != null && tokens.length == 6 )
							responseStr = getQueueData(conn, tokens[1], tokens[2], tokens[3],tokens[4],Misc.getParamAsInt(tokens[5]));
					}
					else if (gpsString.contains("@@secl,SET")){
						//@@secl,requester,datacommaseparated(tableId,action_id,colVal1,colVal2,...
						String dataString = gpsString.substring(10);
						String query = QueryBuilder.getQuery(dataString);
						String logQuery = QueryBuilder.getLogQuery(dataString);
						if(logQuery != null && logQuery.length() > 0)
							responseStr = QueryBuilder.executeQuery(logQuery, conn);
						else
							responseStr = QueryBuilder.executeQuery(query, conn);
					}
					
				}
				//System.out.println("DeviceInteractionModule: Device Request [" + gpsString + " == " + response + "]");
				if (!overWeb)
					responseStr += "#";
				System.out.println("@@secl-communication stop");
				return responseStr == null ? null : (responseStr).replaceAll("\r\n","");
			}
		}
		catch(Exception ex){

		}finally{
  
		}
		return null;
	}
	private static int g_maxPacketSize = 1475; //about 1480 seems reasonable ... though
	private static String g_rowSeparator = "!!";
	
    synchronized private static String multiGetResponse(String systemCode, String lastIndexStr, boolean dateUpfront, SimpleDateFormat sdf) {
    	StringBuilder sb = new StringBuilder();
    	Triple<ArrayList<String>,Date,Integer> dataSegment = null;
    	dataSegment = dataSenderCache.get(systemCode);
    	boolean putRowSeparator = false;
    	if (dateUpfront) {    		
    		java.util.Date now = new java.util.Date();
			sb.append(sdf.format(now)).append(",").append(dataSegment.first.size());
			putRowSeparator = true;
    	}
    	int lastIndexSend = Misc.getParamAsInt(lastIndexStr, -1);
    	int dataAddedIndex = lastIndexSend;
    	boolean hasMoreData = false;
    	for (int i=lastIndexSend+1, is = dataSegment == null || dataSegment.first == null ? 0 : dataSegment.first.size()-1;i<is;i++) {
    		hasMoreData = true; // will be set to false after addingData
    	     String itemToAdd = dataSegment.first.get(i);
    	     boolean toAdd = sb.length() == 0 || (sb.length()+itemToAdd.length()) < g_maxPacketSize;
    	     if (toAdd) {
    	    	 hasMoreData = false;
    	    	 if (putRowSeparator) {
    	    		 sb.append(putRowSeparator);
    	    	 }
    	    	 putRowSeparator = true;
    	    	 sb.append(itemToAdd);
    	    	 dataAddedIndex = i;
    	     }
    	}
    	if (!hasMoreData) {
    		sb.append("###");
    	}
    	if (dataSegment != null)
    		dataSegment.third = dataAddedIndex;
    	return sb.toString();
    }
	    
	synchronized private static String getQueueDataWIP(Connection conn, String command,String systemCode,String lastSyncDateStr,String lastIndexStr,int criticality){
		//for get: yyyyMMddHHmmss,no of records followed !! and then next
		//      resume: trip record!!trip record ... ending with ### if no more records without maxSz break
		//      clear: @@@
		String retval = null;
		boolean destroyIt = false;
		int portNodeId = Misc.getUndefInt();
		Triple<ArrayList<String>,Date,Integer> dataSegment = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date lastSyncDate = null;
		int lastIndexSend = Misc.getParamAsInt(lastIndexStr,-1);
		try{
			if(systemInfoMap == null)
				populateSystemInfo(conn);
			if(systemCode != null && systemInfoMap != null)
				portNodeId = systemInfoMap.get(systemCode);
			if(Misc.isUndef(portNodeId))
				return retval;
			dataSegment = dataSenderCache.get(systemCode);
			lastSyncDate = lastSyncDateStr != null && lastSyncDateStr.length() == 14  ? sdf.parse(lastSyncDateStr) : null;
			if(command != null && command.toUpperCase().contains("RESUME")){
				System.out.println("@@secl-RESUME");
				retval = multiGetResponse(systemCode, lastIndexStr, false, sdf); 
			}
			else if(command != null && command.toUpperCase().contains("CLEAR")){
				System.out.println("@@secl-CLEAR");
				if(dataSegment != null  && dataSegment.first != null){
					dataSegment.first.clear();
					dataSegment.third = -1;
					//dataSegment.second = null;
					System.out.println("@@secl-communication :"+systemCode+"-clear");
				}
				retval = "@@@";
			}
			else{
				System.out.println("@@secl-GET");
				if(dataSegment == null){
					dataSegment = new Triple<ArrayList<String>, Date, Integer>(new ArrayList<String>(), lastSyncDate, -1);
					dataSenderCache.put(systemCode, dataSegment);
				}
				java.util.Date now = new java.util.Date();
				DataRequester.populateTripDataForPort(conn, portNodeId, dataSegment.second, dataSegment.first, criticality);//criticallity
				java.util.Date newSyncDate = now;
				if((newSyncDate != null) && (dataSegment.second == null || (newSyncDate.getTime() > dataSegment.second.getTime()))){
					dataSegment.second = new Date(newSyncDate.getTime());
				    dataSegment.third = -1;
				}
				retval = multiGetResponse(systemCode, lastIndexStr, true, sdf);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
		}
		return retval;
	}

	private static String getQueueData(Connection conn, String command,String systemCode,String lastSyncDateStr,String lastIndexStr,int criticality) throws Exception {
		String retval = null;
		boolean destroyIt = false;

		int portNodeId = Misc.getUndefInt();
		Triple<ArrayList<String>,Date,Integer> dataSegment = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date lastSyncDate = null;
		int lastIndexSend = Misc.getParamAsInt(lastIndexStr,-1);
		try{
			if(systemInfoMap == null)
				populateSystemInfo(conn);
			if(systemCode != null && systemInfoMap != null)
				portNodeId = systemInfoMap.get(systemCode);
			if(Misc.isUndef(portNodeId))
				return retval;
			dataSegment = dataSenderCache.get(systemCode);
			lastSyncDate = lastSyncDateStr != null && lastSyncDateStr.length() == 14  ? sdf.parse(lastSyncDateStr) : null;
			Date now = new Date(System.currentTimeMillis());
			if(command != null && command.toUpperCase().contains("RESUME")){
				System.out.println("@@secl-RESUME");
				if(dataSegment != null  && dataSegment.first != null)
					dataSegment.third =  lastIndexSend;
				if(dataSegment.third < (dataSegment.first.size() -1)){
					retval = dataSegment.first.get(++dataSegment.third);
					/*if(dataSegment.third >= 0){
						for(int i=dataSegment.third;i>=0;i--){
							dataSegment.first.remove(i);
						}
					}*/
				}
				else 
					retval = "###";
			}else if(command != null && command.toUpperCase().contains("CLEAR")){
				System.out.println("@@secl-CLEAR");
				if(dataSegment != null  && dataSegment.first != null){
					dataSegment.first.clear();
					dataSegment.third = -1;
					//dataSegment.second = null;
					System.out.println("@@secl-communication :"+systemCode+"-clear");
				}
				retval = "@@@";
			}
			else{
				System.out.println("@@secl-GET");
				if(dataSegment == null){
					dataSegment = new Triple<ArrayList<String>, Date, Integer>(new ArrayList<String>(), lastSyncDate, -1);
					dataSenderCache.put(systemCode, dataSegment);
				}
				Date newSyncDate = DataRequester.populateTripDataForPort(conn, portNodeId, dataSegment.second, dataSegment.first, criticality);//criticallity
				if((newSyncDate != null) && (dataSegment.second == null || (newSyncDate.getTime() > dataSegment.second.getTime()))){
					dataSegment.second = new Date(newSyncDate.getTime());
				    dataSegment.third = -1;
				}
				return sdf.format(dataSegment.second == null ? now : dataSegment.second)+","+dataSegment.first.size();
			}
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
			throw ex;
		}
		return retval;
	}
	synchronized private static void populateSystemInfo(Connection conn){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select code,port_node_id from rfid_sys_code");
			rs = ps.executeQuery();
			while(rs.next()){
				if(systemInfoMap == null)
					systemInfoMap = new ConcurrentHashMap<String , Integer>();
				systemInfoMap.put(rs.getString("code"), Misc.getRsetInt(rs, "port_node_id"));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	public static void callMain(String[] args) {
		try{
			Connection conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			DataRequester dataRequester = new DataRequester();
			ArrayList<String> dataQueue = new ArrayList<String>();
			dataRequester.populateTripDataForPort(conn, 652, null, dataQueue, 1);
			System.out.println();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyIt = true;
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
		System.out.println("end main");
	}
}



package com.ipssi.rfid.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.Unique;
import com.ipssi.rfid.processor.Utils;

@Table("secl_workstation_details")
public class SECLWorkstationDetails {
	public static final int GATE=0;
	public static final int WB=1;
	public static final int BOTH=2;
	
	
	@KEY
	@GENRATED
	@PRIMARY_KEY
	@Column("id")
	private int id = Misc.getUndefInt();
	@Unique
	@Column("uid")
	private String uid;
	@Column("name")
	private String name;
	@Column("code")
	private String code;
	@Column("workstation_profile_id")
	private int workStationProfileId=Misc.getUndefInt();
	@Column("mines_id")
	private int minesId=Misc.getUndefInt();
	@Column("mines_code")
	private String minesCode;
	@Column("challan_series")
	private String challanSeries;
	@Column("server_code")
	private String serverCode;
	@Column("port_node_id")
	private int portNodeId=Misc.getUndefInt();
	@Column("status")
	private int status=Misc.getUndefInt();
	@Column("created_by")
	private int createdBy=Misc.getUndefInt();
	@Column("created_on")
	private Date createdOn;
	@Column("updated_by")
	private int updatedBy=Misc.getUndefInt();
	@Column("updated_on")
	private Date updatedOn;
	@Column("notes")
	private String Notes;
	@Column("gate_reader_one_type")
	private int gateReaderOneType = Misc.getUndefInt();
	@Column("gate_reader_two_type")
	private int gateReaderTwoType = Misc.getUndefInt();
	@Column("idle_threshold_sec")
	private int idleThresholdSeconds = Misc.getUndefInt();
	@Column("server_conn_threshold_sec")
	private int serverConnThresholdSec = Misc.getUndefInt();
	
	@Column("lr_prefix_first")
	private String lrPrefixFirst; 
	@Column("lr_prefix_second")
	private String lrPrefixSecond;
	
	@Column("road_lr_prefix_first")
	private String roadLrPrefixFirst; 
	@Column("road_lr_prefix_second")
	private String roadLrPrefixSecond;
	
	@Column("washery_lr_prefix_first")
	private String washeryLrPrefixFirst; 
	@Column("washery_lr_prefix_second")
	private String washeryLrPrefixSecond;
	
	@Column("other_lr_prefix_first")
	private String otherLrPrefixFirst; 
	@Column("other_lr_prefix_second")
	private String otherLrPrefixSecond;
	
	@Column("tare_change_threshold")
	private double tareChangeThresHold = Misc.getUndefDouble();
	@Column("gross_change_threshold")
	private double grossChangeThresHold = Misc.getUndefDouble();
	
	private String coorporateIdentityNumber = null;
	private String sidingCode;
	private ArrayList<Triple<Integer,String,String>> operatingMines=null;
	private ArrayList<Triple<Integer,String,String>> operatingSiding=null;
	private WorkStationProfile workStationProfile;
	private WorkstationIpDetails workStationIpDetails;
	
	@Column("prefered_product")
	private String preferedProduct;
	@Column("prefered_grade")
	private String preferedGrade;
	@Column("no_remote")
	private int noRemote = Misc.getUndefInt();
	@Column("type")
	private int type=Misc.getUndefInt();
	@Column("str_field1")
	private String strField1;
	@Column("str_field2")
	private String strField2;
	@Column("str_field3")
	private String strField3;
	@Column("sap_code")
	private String sapCode;
	
	public int getType() {
		return type;
	}
	public SECLWorkstationDetails(){
		super();
	}
	public SECLWorkstationDetails(String uid){
		super();
		this.uid = uid;
	}
	public SECLWorkstationDetails(int id){
		super();
		this.id = id;
	}
	public int getNoRemote() {
		return noRemote;
	}
	public static SECLWorkstationDetails getWorkStation(Connection conn) throws Exception{
		return getWorkStation(conn, null);
	}
	public static SECLWorkstationDetails getWorkStation(Connection conn, String prefix) throws Exception{
		String uid = getUID(prefix);
		if(uid == null || uid.length() <= 0)
			return null;
		SECLWorkstationDetails retval = null;
		ArrayList<SECLWorkstationDetails> resultList = (ArrayList<SECLWorkstationDetails>) RFIDMasterDao.getList(conn, new SECLWorkstationDetails(uid),null);
		if(resultList != null && resultList.size() > 0){
			retval = resultList.get(0);
			retval.setOperatingMines(conn);
			retval.setOperatingSiding(conn);
			retval.setOrganisation(conn);
			if(retval != null){
				retval.workStationProfile = WorkStationProfile.getWorkStationProfile(conn, retval.workStationProfileId);
			}
			
		}
		if(retval == null){
			retval = new SECLWorkstationDetails(uid);
			retval.setStatus(1);
			retval.setPortNodeId(2);
			RFIDMasterDao.insert(conn, retval);
		}
		retval.workStationIpDetails = WorkstationIpDetails.getWorkstationIpDetails(conn, uid);
		return retval;
	}
	private void setOrganisation(Connection conn) throws SQLException{
		if(Misc.isUndef(this.portNodeId))
			return;
		PreparedStatement ps = conn.prepareStatement("select STR_FIELD3 from port_nodes where id=?");
		Misc.setParamInt(ps, this.portNodeId, 1);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			this.coorporateIdentityNumber = rs.getString(1);
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
	
	}
	private void setOperatingMines(Connection conn) throws SQLException{
		if(this.code == null || this.code.length() <= 0)
			return;
		PreparedStatement ps = conn.prepareStatement("select mines_details.id,mines_details.sn,mines_details.name from secl_workstation_mines_group left outer join mines_details on (mines_details.sn=secl_workstation_mines_group.mines_code) where workstation_code like ? and mines_details.status=1");
		ps.setString(1, this.code);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			if(this.operatingMines == null)
				this.operatingMines = new ArrayList<Triple<Integer,String,String>>();
			String code = rs.getString(2);
			if(this.minesCode == null || this.minesCode.length() <= 0)
				this.minesCode = code;
			this.operatingMines.add(new Triple<Integer, String, String>(Misc.getRsetInt(rs, 1), code , rs.getString(3)) );
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
	}
	private void setOperatingSiding(Connection conn) throws SQLException{
		if(this.code == null || this.code.length() <= 0)
			return;
		PreparedStatement ps = conn.prepareStatement("select mines_details.id,mines_details.sn,mines_details.name from secl_workstation_destination_group left outer join mines_details on (mines_details.sn=secl_workstation_destination_group.destination_code) where workstation_code like ? and mines_details.status=1");
		ps.setString(1, this.code);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			if(this.operatingSiding == null)
				this.operatingSiding = new ArrayList<Triple<Integer,String,String>>();
			String code = rs.getString(2);
			if(this.sidingCode == null || this.sidingCode.length() <= 0)
				this.sidingCode = code;
			this.operatingSiding.add(new Triple<Integer, String, String>(Misc.getRsetInt(rs, 1), code , rs.getString(3)) );
		}
		Misc.closeRS(rs);
		Misc.closePS(ps);
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getWorkStationProfileId() {
		return workStationProfileId;
	}

	public void setWorkStationProfileId(int workStationProfileId) {
		this.workStationProfileId = workStationProfileId;
	}

	public int getMinesId() {
		return minesId;
	}

	public void setMinesId(int minesId) {
		this.minesId = minesId;
	}

	public String getChallanSeries() {
		return challanSeries;
	}

	public void setChallanSeries(String challanSeries) {
		this.challanSeries = challanSeries;
	}

	public int getPortNodeId() {
		return portNodeId;
	}

	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getNotes() {
		return Notes;
	}

	public void setNotes(String notes) {
		Notes = notes;
	}

	public WorkStationProfile getWorkStationProfile() {
		return workStationProfile;
	}

	public void setWorkStationProfile(WorkStationProfile workStationProfile) {
		this.workStationProfile = workStationProfile;
	}

	/*	create table  secl_workstation_profile(id int(11) primary key auto_increment,name varchar(64),port_node_id int(11),status int(11),
	 *  created_on datetime default null,created_by int(11),updated_on timestamp default current_timestamp,updated_by int(11), 
	 *  notes varchar(255));
	 */
	@Table("secl_workstation_profile")
	public static class WorkStationProfile{
		@KEY
		@GENRATED
		@PRIMARY_KEY
		@Column("id")
		private int id = Misc.getUndefInt();
		@Column("name")
		private String name;
		@Column("port_node_id")
		private int portNodeId=Misc.getUndefInt();
		@Column("status")
		private int status=Misc.getUndefInt();
		@Column("created_by")
		private int createdBy=Misc.getUndefInt();
		@Column("created_on")
		private Date createdOn;
		@Column("updated_by")
		private int updatedBy=Misc.getUndefInt();
		@Column("updated_on")
		private Date updatedOn;
		@Column("notes")
		private String Notes;
		ArrayList<WorkStationProfileScreens> workStationProfileScreens;
		
		public ProcessStepProfile getProcessStepProfile(int materialCat){
			if(Misc.isUndef(materialCat) || workStationProfileScreens == null || workStationProfileScreens.size() == 0){
				return ProcessStepProfile.getStandardProcessStepByMaterialCat(materialCat);
			}
			ArrayList<Integer> processSteps = null;
			Collections.sort(workStationProfileScreens,new Comparator<WorkStationProfileScreens>() {
				public int compare(WorkStationProfileScreens o1, WorkStationProfileScreens o2) {
					// TODO Auto-generated method stub
					return o1.seqNo - o2.seqNo;
				}
			});
			for(int i=0;i<workStationProfileScreens.size();i++){
				if(workStationProfileScreens.get(i).materialCat != materialCat)
					continue;
				if(processSteps == null)
					processSteps = new ArrayList<Integer>();
				processSteps.add(workStationProfileScreens.get(i).type);
			}
			return processSteps == null ? ProcessStepProfile.getStandardProcessStepByMaterialCat(materialCat) :  new ProcessStepProfile(processSteps);
		}
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getPortNodeId() {
			return portNodeId;
		}
		public void setPortNodeId(int portNodeId) {
			this.portNodeId = portNodeId;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public int getCreatedBy() {
			return createdBy;
		}
		public void setCreatedBy(int createdBy) {
			this.createdBy = createdBy;
		}
		public Date getCreatedOn() {
			return createdOn;
		}
		public void setCreatedOn(Date createdOn) {
			this.createdOn = createdOn;
		}
		public int getUpdatedBy() {
			return updatedBy;
		}
		public void setUpdatedBy(int updatedBy) {
			this.updatedBy = updatedBy;
		}
		public Date getUpdatedOn() {
			return updatedOn;
		}
		public void setUpdatedOn(Date updatedOn) {
			this.updatedOn = updatedOn;
		}
		public String getNotes() {
			return Notes;
		}
		public void setNotes(String notes) {
			Notes = notes;
		}
		public ArrayList<WorkStationProfileScreens> getWorkStationProfileScreens() {
			return workStationProfileScreens;
		}
		public void setWorkStationProfileScreens(ArrayList<WorkStationProfileScreens> workStationProfileScreens) {
			this.workStationProfileScreens = workStationProfileScreens;
		}
		public static WorkStationProfile getWorkStationProfile(Connection conn,int id) throws Exception{
			WorkStationProfile workStationProfile = (WorkStationProfile) RFIDMasterDao.get(conn, WorkStationProfile.class, id);
			if(workStationProfile != null){
				WorkStationProfileScreens temp = new WorkStationProfileScreens();
				temp.setWorkstationProfileId(id);
				workStationProfile.setWorkStationProfileScreens((ArrayList<WorkStationProfileScreens>) RFIDMasterDao.getList(conn, temp, null));
			}
			return workStationProfile;
		}

	}

	/*  create table secl_workstation_screens(workstation_profile_id int(11),type int(11),manual_entry int(2) default 0, 
	 *  can_print int(2) default 0, can_create_vehicle int(2) default 0,can_start int(2) default 0,can_close int(2) default 0,
	 *  seq_no int(4) , prefered_next_station int(4));
	 */
	@Table("secl_workstation_screens")
	public static class WorkStationProfileScreens{
		@KEY
		@PRIMARY_KEY
		@Column("workstation_profile_id")
		private int workstationProfileId = Misc.getUndefInt();
		@Column("type")
		private int type=Misc.getUndefInt();
		@Column("manual_entry")
		private int manualEntry=Misc.getUndefInt();
		@Column("can_print")
		private int canPrint=Misc.getUndefInt();
		@Column("can_create_vehicle")
		private int canCreateVehicle=Misc.getUndefInt();
		@Column("can_start")
		private int canStart=Misc.getUndefInt();
		@Column("can_close")
		private int canClose=Misc.getUndefInt();
		@Column("seq_no")
		private int seqNo=Misc.getUndefInt();
		@Column("material_cat")
		private int materialCat=Misc.getUndefInt();
		@Column("prefered_next_station")
		private int preferedNextStation=Misc.getUndefInt();
		
		
		public int getMaterialCat() {
			return materialCat;
		}
		public void setMaterialCat(int materialCat) {
			this.materialCat = materialCat;
		}
		public int getWorkstationProfileId() {
			return workstationProfileId;
		}
		public void setWorkstationProfileId(int workstationProfileId) {
			this.workstationProfileId = workstationProfileId;
		}
		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}
		public int getManualEntry() {
			return manualEntry;
		}
		public void setManualEntry(int manualEntry) {
			this.manualEntry = manualEntry;
		}
		public int getCanPrint() {
			return canPrint;
		}
		public void setCanPrint(int canPrint) {
			this.canPrint = canPrint;
		}
		public int getCanCreateVehicle() {
			return canCreateVehicle;
		}
		public void setCanCreateVehicle(int canCreateVehicle) {
			this.canCreateVehicle = canCreateVehicle;
		}
		public int getCanStart() {
			return canStart;
		}
		public void setCanStart(int canStart) {
			this.canStart = canStart;
		}
		public int getCanClose() {
			return canClose;
		}
		public void setCanClose(int canClose) {
			this.canClose = canClose;
		}
		public int getSeqNo() {
			return seqNo;
		}
		public void setSeqNo(int seqNo) {
			this.seqNo = seqNo;
		}
		public int getPreferedNextStation() {
			return preferedNextStation;
		}
		public void setPreferedNextStation(int preferedNextStation) {
			this.preferedNextStation = preferedNextStation;
		}
		
	}
	public static void main(String[] arg ) throws GenericException{
		boolean destroyIt = false;
		Connection conn = null;
		try {
			getMacId();
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			String systemName = getComputerName();
			getSytemHardwareSerial();
			getUID(null);
			//SECLWorkstationDetails workstation = SECLWorkstationDetails.getWorkStation(conn);
			System.out.println(systemName);
			System.out.println(getCurrentIP());
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
		}
	}
	public static String getComputerName()
	{
	    Map<String, String> env = System.getenv();
	    if (env.containsKey("COMPUTERNAME"))
	        return env.get("COMPUTERNAME");
	    else if (env.containsKey("HOSTNAME"))
	        return env.get("HOSTNAME");
	    else
	        return "Unknown Computer";
	}
	

	public static String getSytemHardwareSerial() throws IOException {
		 // wmic command for diskdrive id: wmic DISKDRIVE GET SerialNumber
        // wmic command for cpu id : wmic cpu get ProcessorId
        Process process = Runtime.getRuntime().exec(new String[] { "wmic", "bios", "get", "serialnumber" });
        process.getOutputStream().close();
        Scanner sc = new Scanner(process.getInputStream());
        String prop = null;
        while (sc.hasNext()){
        	if(prop == null)
        		prop = sc.next();
        	else
        		prop += sc.next();
        }
        System.out.println(prop);
        return prop;
	}
	public static void getMacId(){
		InetAddress ip;
		try {
			System.out.println(InetAddress.getLocalHost().getHostName());
			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
			Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();
			while(network.hasMoreElements()){
				byte[] mac = network.nextElement().getHardwareAddress();
			System.out.print("Current MAC address : ");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			System.out.println(sb.toString());
			}

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (SocketException e){

			e.printStackTrace();

		}
	}
	public static String getUID() {
		return getUID(null);
	}
	public static String getUID(String prefix) {
		String result = "";
		try {
			File file = File.createTempFile("realhowto",".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);

			String vbs =
					"Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
							+ "Set colItems = objWMIService.ExecQuery _ \n"
							+ "   (\"Select * from Win32_BaseBoard\") \n"
							+ "For Each objItem in colItems \n"
							+ "    Wscript.Echo objItem.SerialNumber \n"
							+ "    exit for  ' do the first cpu only! \n"
							+ "Next \n";

			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
			BufferedReader input =
					new BufferedReader
					(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result += line;
			}
			input.close();
			result = (prefix != null && prefix.length() > 0 ? prefix : "") + InetAddress.getLocalHost().getHostName() + "_" + result;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Motherboard:"+result.trim());
		if(result != null)
			result = CacheTrack.standardizeNameNew(result);
		return result.trim();
	}
	public static String getCurrentIP(Connection conn){
		String retval = null;
		int matchRate = -1;
		try {
			Pair<String, String> connParams = DBConnectionPool.getConnectionParams(conn);
			String remoteConnIp = connParams == null || connParams.first == null ? null : connParams.first;   
			String currIP = InetAddress.getLocalHost().getHostAddress();
			int currMatch = getIPMatch(remoteConnIp, currIP);
			if(currMatch > matchRate){
				retval = currIP;
				matchRate = currMatch; 
			}
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();)
			{
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();)
				{
					InetAddress addr = a.nextElement();
					currIP = addr.getHostAddress();
					currMatch = getIPMatch(remoteConnIp, currIP);
					if(currMatch > matchRate){
						retval = currIP;
						matchRate = currMatch; 
					}
				}
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return retval; 
	}
	public static String getCurrentIP() throws Exception{
		String retval = InetAddress.getLocalHost().getHostAddress();
		if(retval != null && !retval.startsWith("127") && !retval.contains(":"))
			return retval;
		Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
	    for (; n.hasMoreElements();)
	    {
	        NetworkInterface e = n.nextElement();
	        Enumeration<InetAddress> a = e.getInetAddresses();
	        for (; a.hasMoreElements();)
	        {
	            InetAddress addr = a.nextElement();
	            retval = addr.getHostAddress();
	            if(retval != null && !retval.startsWith("127") && !retval.contains(":"))
	    			return retval;
	        }
	    }
	    //System.out.println("Your Host addr: " + retval);  // often returns "127.0.0.1"
	    return null; 
	} 
	public String getMinesCode() {
		return minesCode;
	}
	public void setMinesCode(String minesCode) {
		this.minesCode = minesCode;
	}
	public String getSidingCode() {
		return sidingCode;
	}
	public ArrayList<Triple<Integer, String, String>> getOperatingMines() {
		return operatingMines;
	}
	public ArrayList<Triple<Integer, String, String>> getOperatingSiding() {
		return operatingSiding;
	}
	public int getGateReaderOneType() {
		return gateReaderOneType;
	}
	public void setGateReaderOneType(int gateReaderOneType) {
		this.gateReaderOneType = gateReaderOneType;
	}
	public int getGateReaderTwoType() {
		return gateReaderTwoType;
	}
	public void setGateReaderTwoType(int gateReaderTwoType) {
		this.gateReaderTwoType = gateReaderTwoType;
	}
	public int getIdleThresholdSeconds() {
		return idleThresholdSeconds;
	}
	public void setIdleThresholdSeconds(int idleThresholdSeconds) {
		this.idleThresholdSeconds = idleThresholdSeconds;
	}
	public int getServerConnThresholdSec() {
		return serverConnThresholdSec;
	}
	public void setServerConnThresholdSec(int serverConnThresholdSec) {
		this.serverConnThresholdSec = serverConnThresholdSec;
	}
	public WorkstationIpDetails getWorkStationIpDetails() {
		return workStationIpDetails;
	}
	public String getCoorporateIdentityNumber() {
		return coorporateIdentityNumber;
	}
	public String getLrPrefixFirst() {
		return lrPrefixFirst;
	}
	public String getLrPrefixSecond() {
		return lrPrefixSecond;
	}
	public double getTareChangeThresHold() {
		return tareChangeThresHold;
	}
	public double getGrossChangeThresHold() {
		return grossChangeThresHold;
	}
	public String getPreferedProduct() {
		return preferedProduct;
	}
	public String getPreferedGrade() {
		return preferedGrade;
	}
	public static Pair<Integer, String> getWorkstationId(Connection conn){
		return getWorkstationId(conn, null);
	}
	public static Pair<Integer, String> getWorkstationId(Connection conn,String prefix){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select id,code from secl_workstation_details where uid=?");
			ps.setString(1, getUID(prefix));
			rs = ps.executeQuery();
			if(rs.next()){
				return new Pair<Integer, String>(Misc.getRsetInt(rs, 1),rs.getString(2));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return null;
	}
	public static Pair<Integer, String> getWorkstationByUID(Connection conn,String uid){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("select id,code from secl_workstation_details where uid=?");
			ps.setString(1, uid);
			rs = ps.executeQuery();
			if(rs.next()){
				return new Pair<Integer, String>(Misc.getRsetInt(rs, 1),rs.getString(2));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return null;
	}
	public boolean isRegistered(){
		return !Utils.isNull(getCode()) && getPortNodeId() > 2;
	}
	public String getRoadLrPrefixFirst() {
		return roadLrPrefixFirst;
	}
	public String getRoadLrPrefixSecond() {
		return roadLrPrefixSecond;
	}
	public String getWasheryLrPrefixFirst() {
		return washeryLrPrefixFirst;
	}
	public String getWasheryLrPrefixSecond() {
		return washeryLrPrefixSecond;
	}
	public String getOtherLrPrefixFirst() {
		return otherLrPrefixFirst;
	}
	public String getOtherLrPrefixSecond() {
		return otherLrPrefixSecond;
	}
	public String getStrField1() {
		return strField1;
	}
	public String getStrField2() {
		return strField2;
	}
	public String getStrField3() {
		return strField3;
	}
	public String getSapCode() {
		return sapCode;
	}
	public String getServerCode() {
		return serverCode;
	}
	
	public static int getIPMatch(String ip1,String ip2){
		int retval = 0;
		if(ip1 == null || ip1.length() <= 0 || ip2 == null || ip2.length() <= 0)
			return retval;
		String[] ipAdd1 = ip1.split(".");
		String[] ipAdd2 = ip2.split(".");
		if(ipAdd1 == null || ipAdd1.length < 4 || ipAdd2 == null || ipAdd2.length < 4)
			return retval;
		for(int i=0;i<4;i++){
			if(ipAdd1[i].equalsIgnoreCase(ipAdd2[i]))
				retval++;
		}
		return retval;
	}
}

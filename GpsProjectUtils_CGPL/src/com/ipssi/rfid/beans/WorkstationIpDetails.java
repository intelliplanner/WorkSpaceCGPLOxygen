package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("secl_ip_details")
public class WorkstationIpDetails {
    @KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
	private int id = Misc.getUndefInt();
    @Column("ip")          
	private String ip;
    @Column("port")             
    private String port;
    @Column("db")               
    private String db;
    @Column("mac_id")
    private String macId;
    @Column("user_id")
    private String user;
    @Column("password")
    private String password;
    @Column("status")
    private int status = Misc.getUndefInt();
    @Column("notes")
    private String notes;
    @Column("created_on")
    private Date createdOn;
    @Column("created_by")
    private int createdBy = Misc.getUndefInt();
    @Column("updated_by")      
    private int updatedBy = Misc.getUndefInt();
    @Column("server_ip")
	private String serverIp;
    @Column("server_port")             
    private String serverPort;
    @Column("server_db")               
    private String serverDb;
    @Column("server_user_id")
    private String serverUserId;
    @Column("server_password")
    private String serverPassword;

    public WorkstationIpDetails() {
		super();
	}
    public WorkstationIpDetails(String uid) {
		super();
		this.macId = uid;
	}
    public WorkstationIpDetails(int id) {
		super();
		this.id=id;
	}
    public static WorkstationIpDetails getWorkstationIpDetails(Connection conn,String uid) {
    	WorkstationIpDetails retval = null;
    	try{
    		ArrayList<WorkstationIpDetails> resultList = (ArrayList<WorkstationIpDetails>) RFIDMasterDao.getList(conn, new WorkstationIpDetails(uid),null);
    		if(resultList != null && resultList.size() > 0){
    			retval = resultList.get(0);
    		}else{
    			retval = new WorkstationIpDetails(uid);
    			retval.setStatus(1);
    			RFIDMasterDao.insert(conn, retval);
    		}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return retval;
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
	public String getMacId() {
		return macId;
	}
	public void setMacId(String macId) {
		this.macId = macId;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public String getServerPort() {
		return serverPort;
	}
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	public String getServerDb() {
		return serverDb;
	}
	public void setServerDb(String serverDb) {
		this.serverDb = serverDb;
	}
	public String getServerUserId() {
		return serverUserId;
	}
	public void setServerUserId(String serverUserId) {
		this.serverUserId = serverUserId;
	}
	public String getServerPassword() {
		return serverPassword;
	}
	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}
	@Override
	public String toString() {
		return "WorkstationIpDetails [id=" + id + ", ip=" + ip + ", port=" + port + ", db=" + db + ", macId=" + macId
				+ ", user=" + user + ", password=" + password + ", status=" + status + ", notes=" + notes
				+ ", createdOn=" + createdOn + ", createdBy=" + createdBy + ", updatedBy=" + updatedBy + ", serverIp="
				+ serverIp + ", serverPort=" + serverPort + ", serverDb=" + serverDb 
				+ ", serverUserId=" + serverUserId + ", serverPassword=" + serverPassword + "]";
	}
	
}

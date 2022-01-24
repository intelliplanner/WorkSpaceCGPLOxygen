package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;
import com.ipssi.rfid.db.Table.ReadOnly;

@Table("vehicle_rfid_info")
public class VehicleRFIDInfo {
    @KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
	private int id = Misc.getUndefInt();
    
    @Column("vehicle_id")
    private int vehicleId = Misc.getUndefInt();
    
    @Column("driver_id")
    private int driverId = Misc.getUndefInt();
    
    @Column("driver_name")
    private String driverName;
    
    @Column("driver_dl_no")
    private String driverDLNo;
    
    @Column("driver_mobile")
    private String driverMobileNo;
    
    @Column("card_type")
    private int cardType = Misc.getUndefInt();
    
    @Column("card_issued_for")
    private int cardIssuedFor = Misc.getUndefInt();
    
    @Column("purpose")
    private String purpose;
    
    @Column("allowed_mines")
    private int allowedMines = Misc.getUndefInt();
    
    @Column("issue_date")
    private Date issueDate;
    
    @Column("valid_upto")
    private Date validUpto;
    
    @Column("status")
    private int status=Misc.getUndefInt();
    
    @ReadOnly
    @Column("created_on")
    private Date cretedOn;
    
    @Column("created_by")
    private int createdBy=Misc.getUndefInt();
    
    @Column("do_assigned")
    private String doAssigned;

    @Column("return_date")
    private Date returnDate;
    
    @Column("allowed_mines_code")
    private String allowedMinesCode;

    @Column("epc_id")
    private String epcId;
    
    @Column("issued_tpr_id")
    private int issuedTprId = Misc.getUndefInt();

	public Date getReturnDate() {
		return returnDate;
	}



	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}



	public void setCretedOn(Date cretedOn) {
		this.cretedOn = cretedOn;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverDLNo() {
		return driverDLNo;
	}

	public void setDriverDLNo(String driverDLNo) {
		this.driverDLNo = driverDLNo;
	}

	public String getDriverMobileNo() {
		return driverMobileNo;
	}

	public void setDriverMobileNo(String driverMobileNo) {
		this.driverMobileNo = driverMobileNo;
	}

	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}

	public int getCardIssuedFor() {
		return cardIssuedFor;
	}

	public void setCardIssuedFor(int cardIssuedFor) {
		this.cardIssuedFor = cardIssuedFor;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public int getAllowedMines() {
		return allowedMines;
	}

	public void setAllowedMines(int allowedMines) {
		this.allowedMines = allowedMines;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public Date getValidUpto() {
		return validUpto;
	}

	public void setValidUpto(Date validUpto) {
		this.validUpto = validUpto;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCretedOn() {
		return cretedOn;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public static void main(String[] args) {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			VehicleRFIDInfo v1 = (VehicleRFIDInfo) RFIDMasterDao.get(conn, VehicleRFIDInfo.class, Misc.getUndefInt());
//			System.out.println();
		} catch (Exception e) {
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	public String getAllowedMinesCode() {
		return allowedMinesCode;
	}

	public void setAllowedMinesCode(String allowedMinesCode) {
		this.allowedMinesCode = allowedMinesCode;
	}



	public String getDoAssigned() {
		return doAssigned;
	}



	public void setDoAssigned(String doAssigned) {
		this.doAssigned = doAssigned;
	}
	public String getEpcId() {
		return epcId;
	}
	public void setEpcId(String epcId) {
		this.epcId = epcId;
	}
	public int getIssuedTprId() {
		return issuedTprId;
	}
	public void setIssuedTprId(int issuedTprId) {
		this.issuedTprId = issuedTprId;
	}
	
}

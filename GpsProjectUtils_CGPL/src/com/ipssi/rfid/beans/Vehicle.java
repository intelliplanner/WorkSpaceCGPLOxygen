package com.ipssi.rfid.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("vehicle")
public class Vehicle {

    @KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("id")
    private int id = Misc.getUndefInt();
    @Column("name")
    private String vehicleName;
    @Column("rfid_epc")
    private String epcId;
    @Column("std_name")
    private String stdName;
    @Column("tare")
    private double avgTare = Misc.getUndefDouble();
    @Column("gross")
    private double avgGross = Misc.getUndefDouble();
    @Column("status")
    private int status = Misc.getUndefInt();
    @Column("rfid_temp_status")
    private int rfidTempStatus = Misc.getUndefInt();
    @Column("rfid_issue_date")
    private Date rfid_issue_date;
    @Column("last_epc")
    private String lastEPC;
    @Column("load_tare_freq")
    private double loadTareFreq = Misc.getUndefDouble();
    @Column("unload_tare_freq")
    private double unloadTareFreq = Misc.getUndefDouble();
    @Column("flyash_tare_time")
    private Date loadTareTime;
    @Column("unload_tare_time")
    private Date unloadTareTime;
    @Column("flyash_tare")
    private double loadTare = Misc.getUndefDouble();
    //@Column("unload_tare")
    private double unloadTare = Misc.getUndefDouble();
    
    @Column("card_type")
    private int cardType = Misc.getUndefInt();
    @Column("card_purpose")
    private int cardPurpose = Misc.getUndefInt();
    @Column("do_assigned")
    private String doAssigned;
    @Column("prefered_mines")
    private int preferedMines = Misc.getUndefInt();
    @Column("prefered_mines_code")
    private String preferedMinesCode;
    @Column("prefered_driver")
    private int preferedDriver = Misc.getUndefInt();
    @Column("rfid_info_id")
    private int rfidInfoId = Misc.getUndefInt();
    @Column("card_validity_type")
    private int cardValidityType = Misc.getUndefInt();
    @Column("card_init_date")
    private Date cardInitDate;
    @Column("card_expiary_date")
    private Date cardExpiaryDate;
    @Column("card_init")
    private int cardInit = Misc.getUndefInt();
	@Column("customer_id")
    private int customerId = Misc.getUndefInt();
	@Column("is_vehicle_on_gate")
    private int vehicleOnGate = Misc.getUndefInt();
	@Column("last_tare_tpr")
    private int lastTareTPR = Misc.getUndefInt();
	@Column("min_tare")
    private double minTare = Misc.getUndefDouble();
	@Column("min_gross")
    private double minGross = Misc.getUndefDouble();
	@Column("gate_pass_number")
    private String gatePassNumber;
	
	private String nameOnCard=null;
	
	@Column("src_record_time")
	private Date srcRecordTime;
	
    public Date getCardInitDate() {
		return cardInitDate;
	}


	public void setCardInitDate(Date cardInitDate) {
		this.cardInitDate = cardInitDate;
	}


	public int getVehicleOnGate() {
		return vehicleOnGate;
	}


	public void setVehicleOnGate(int vehicleOnGate) {
		this.vehicleOnGate = vehicleOnGate;
	}


	public int getRfidInfoId() {
		return rfidInfoId;
	}


	public void setRfidInfoId(int rfidInfoId) {
		this.rfidInfoId = rfidInfoId;
	}


	public int getPreferedMines() {
		return preferedMines;
	}


	public int getPreferedDriver() {
		return preferedDriver;
	}


	public void setPreferedDriver(int preferedDriver) {
		this.preferedDriver = preferedDriver;
	}


	public void setPreferedMines(int preferedMines) {
		this.preferedMines = preferedMines;
	}


	public int getCardType() {
		return cardType;
	}


	public void setCardType(int cardType) {
		this.cardType = cardType;
	}


	public int getCardPurpose() {
		return cardPurpose;
	}


	public void setCardPurpose(int cardPurpose) {
		this.cardPurpose = cardPurpose;
	}

	public int getCardValidityType() {
		return cardValidityType;
	}


	public void setCardValidityType(int cardValidityType) {
		this.cardValidityType = cardValidityType;
	}


	public Date getCardExpiaryDate() {
		return cardExpiaryDate;
	}


	public void setCardExpiaryDate(Date cardExpiaryDate) {
		this.cardExpiaryDate = cardExpiaryDate;
	}


	public int getCardInit() {
		return cardInit;
	}


	public void setCardInit(int cardInit) {
		this.cardInit = cardInit;
	}
	
    public Date getRfid_issue_date() {
        return rfid_issue_date;
    }

    
    public void setRfid_issue_date(Date rfid_issue_date) {
        this.rfid_issue_date = rfid_issue_date;
    }
    public int getRfidTempStatus() {
        return rfidTempStatus;
    }

    public void setRfidTempStatus(int rfidTempStatus) {
        this.rfidTempStatus = rfidTempStatus;
    }
    private int transporterId;
    
    private int minesId;
    
    private Date createdOn;
    private Date updatedOn;
    
    private int updatedBy;
    @Column("record_src")
    private int recordSrc = Misc.getUndefInt();
    
    @Column("rf_updated_on")
    private Date rfUpdatedOn;
   // @Column("stone_tare_time")
    private Date stoneTareTime;
    
//    @Column("stone_tare")
    private double stoneTare = Misc.getUndefDouble();
    
    @Column("tag_init_challan")
    private String tagInitChallan;
    public int getId() {
        return id;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getEpcId() {
        return epcId;
    }

    public double getAvgTare() {
        return avgTare;
    }

    public double getAvgGross() {
        return avgGross;
    }

    public int getStatus() {
        return status;
    }

    public int getTransporterId() {
        return transporterId;
    }

    public int getMinesId() {
        return minesId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setEpcId(String epcId) {
        this.epcId = epcId;
    }

    public void setAvgTare(double avgTare) {
        this.avgTare = avgTare;
    }

    public void setAvgGross(double avgGross) {
        this.avgGross = avgGross;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTransporterId(int transporterId) {
        this.transporterId = transporterId;
    }

    public void setMinesId(int minesId) {
        this.minesId = minesId;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getStdName() {
        return stdName;
    }

    public void setStdName(String stdName) {
        this.stdName = stdName;
    }

	public String getLastEPC() {
		return lastEPC;
	}

	public void setLastEPC(String lastEPC) {
		this.lastEPC = lastEPC;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public VehicleExtended getVehicleExt() {
		return vehicleExt;
	}


	public void setVehicleExt(VehicleExtended vehicleExt) {
		this.vehicleExt = vehicleExt;
	}

	private VehicleExtended vehicleExt;
	private VehicleRFIDInfo vehicleRFIDInfo;
	public Vehicle(){
		super();
	}
	
	public Vehicle(int id, String vehicleName, String epcId, int status) {
		super();
		this.id = id;
		this.vehicleName = vehicleName;
		this.epcId = epcId;
		this.status = status;
	}
	public static Vehicle getVehicleByEpc(Connection conn, String epcId) throws Exception {
		ArrayList<Vehicle> list = null;
		try {
			Vehicle veh = new Vehicle();
			veh.setStatus(1);
			if(epcId != null && epcId.length() > 0 && !epcId.equalsIgnoreCase("E000000000000000000000E0")){
				veh.setEpcId(epcId);
				list = (ArrayList<Vehicle>) RFIDMasterDao.getList(conn, new Vehicle(Misc.getUndefInt(), null, epcId,Status.ACTIVE),null);
				if (list != null && list.size() > 0) {
					return list.get(0);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	
    }
	public static Vehicle getVehicleByEpcNew(Connection conn, String epcId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int vehicleId = Misc.getUndefInt();
		ps = conn.prepareStatement("select vehicle.id from vehicle_rfid_info left outer join vehicle on (vehicle.id=vehicle_rfid_info.vehicle_id) where vehicle_rfid_info.epc_id like ? and vehicle_rfid_info.status=1 and vehicle.status=1");
		rs = ps.executeQuery();
		if(rs.next()){
			vehicleId = Misc.getRsetInt(rs, 1);
		}
		return getVehicle(conn, vehicleId);
	}
    public static Vehicle getVehicle(Connection conn, int vehicleId) throws Exception {
    	Vehicle vehicle = null;
    	if(Misc.isUndef(vehicleId))
    		return vehicle;
    	vehicle = (Vehicle) RFIDMasterDao.get(conn, Vehicle.class, vehicleId);
    	if(vehicle != null){
    		vehicle.setVehicleExt((VehicleExtended) RFIDMasterDao.get(conn, VehicleExtended.class, vehicleId));
    		vehicle.setVehicleRFIDInfo((VehicleRFIDInfo) RFIDMasterDao.get(conn, VehicleRFIDInfo.class,vehicle.getRfidInfoId()));
    	}
    	return vehicle;
    }
	public double getLoadTareFreq() {
		return loadTareFreq;
	}
	public void setLoadTareFreq(double loadTareFreq) {
		this.loadTareFreq = loadTareFreq;
	}
	public double getUnloadTareFreq() {
		return unloadTareFreq;
	}
	public void setUnloadTareFreq(double unloadTareFreq) {
		this.unloadTareFreq = unloadTareFreq;
	}
	public Date getLoadTareTime() {
		return loadTareTime;
	}
	public void setLoadTareTime(Date loadTareTime) {
		this.loadTareTime = loadTareTime;
	}
	public Date getUnloadTareTime() {
		return unloadTareTime;
	}
	public void setUnloadTareTime(Date unloadTareTime) {
		this.unloadTareTime = unloadTareTime;
	}
	

	public double getLoadTare() {
		return loadTare;
	}


	public void setLoadTare(double loadTare) {
		this.loadTare = loadTare;
	}


	public double getUnloadTare() {
		return unloadTare;
	}


	public void setUnloadTare(double unloadTare) {
		this.unloadTare = unloadTare;
	}


	public String getPreferedMinesCode() {
		return preferedMinesCode;
	}


	public void setPreferedMinesCode(String preferedMinesCode) {
		this.preferedMinesCode = preferedMinesCode;
	}


	public String getDoAssigned() {
		return doAssigned;
	}


	public void setDoAssigned(String doAssigned) {
		this.doAssigned = doAssigned;
	}


	public VehicleRFIDInfo getVehicleRFIDInfo() {
		return vehicleRFIDInfo;
	}


	public void setVehicleRFIDInfo(VehicleRFIDInfo vehicleRFIDInfo) {
		this.vehicleRFIDInfo = vehicleRFIDInfo;
	}
	
	public Date getStoneTareTime() {
		return stoneTareTime;
	}


	public void setStoneTareTime(Date stoneTareTime) {
		this.stoneTareTime = stoneTareTime;
	}


	public Date getFlyashTareTime() {
		return loadTareTime;
	}


	public void setFlyashTareTime(Date flyashTareTime) {
		this.loadTareTime = flyashTareTime;
	}

	public double getStoneTare() {
		return stoneTare;
	}

	public void setStoneTare(double stoneTare) {
		this.stoneTare = stoneTare;
	}

	public double getFlyashTare() {
		return loadTare;
	}

	public void setFlyashTare(double flyashTare) {
		this.loadTare = flyashTare;
	}


	public int getLastTareTPR() {
		return lastTareTPR;
	}


	public void setLastTareTPR(int lastTareTPR) {
		this.lastTareTPR = lastTareTPR;
	}


	public double getMinTare() {
		return minTare;
	}


	public void setMinTare(double minTare) {
		this.minTare = minTare;
	}


	public double getMinGross() {
		return minGross;
	}


	public void setMinGross(double minGross) {
		this.minGross = minGross;
	}
	
	public boolean isRFEquals(Vehicle other){
		return 
		((this.epcId == null && other.epcId == null) || (this.epcId != null && this.epcId.equalsIgnoreCase(other.epcId)))
		&& this.minTare == other.minTare
		&& this.minGross == other.minGross
		&& this.cardType == other.cardType
		&& this.cardPurpose == other.cardPurpose
		&& ((this.cardExpiaryDate == null || other.cardExpiaryDate == null) || (this.cardExpiaryDate != null && other.cardExpiaryDate != null && this.cardExpiaryDate.getTime() == other.cardExpiaryDate.getTime()))
		&& ((this.cardInitDate == null && other.cardInitDate == null) || (this.cardInitDate != null && other.cardInitDate != null && this.cardInitDate.getTime() == other.cardInitDate.getTime()))
		&& ((this.doAssigned == null && other.doAssigned == null) || (this.doAssigned != null && this.doAssigned.equalsIgnoreCase(other.doAssigned)))
		&& ((this.preferedMinesCode == null && other.preferedMinesCode == null) || (this.preferedMinesCode != null && this.preferedMinesCode.equalsIgnoreCase(other.preferedMinesCode)));
	}
	public int getRecordSrc() {
		return recordSrc;
	}
	public void setRecordSrc(int recordSrc) {
		this.recordSrc = recordSrc;
	}
	public Date getRfUpdatedOn() {
		return rfUpdatedOn;
	}
	public void setRfUpdatedOn(Date rfUpdatedOn) {
		this.rfUpdatedOn = rfUpdatedOn;
	}
	public String getTagInitChallan() {
		return tagInitChallan;
	}
	public void setTagInitChallan(String tagInitChallan) {
		this.tagInitChallan = tagInitChallan;
	}
	public String getGatePassNumber() {
		return gatePassNumber;
	}
	public void setGatePassNumber(String gatePassNumber) {
		this.gatePassNumber = gatePassNumber;
	}
	public String getNameOnCard() {
		return nameOnCard;
	}
	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}
	public Date getSrcRecordTime() {
		return srcRecordTime;
	}
	public void setSrcRecordTime(Date srcRecordTime) {
		this.srcRecordTime = srcRecordTime;
	}
}

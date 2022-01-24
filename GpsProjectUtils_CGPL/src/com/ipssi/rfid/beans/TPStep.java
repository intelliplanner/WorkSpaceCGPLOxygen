package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("tp_step")
public class TPStep {
	public static final int SAVE_AND_CONTINUE = 0;
	public static final int REQUEST_OVERRIDE = 1;
    @KEY
    @GENRATED
    @PRIMARY_KEY
    @Column("tps_id")
    private int id = Misc.getUndefInt();
    @Column("tpr_id")
    private int tprId = Misc.getUndefInt();
    @Column("vehicle_id")
    private int vehicleId = Misc.getUndefInt();
    @Column("work_station_type")
    private int workStationType = Misc.getUndefInt();
    @Column("work_station_id")
    private int workStationId = Misc.getUndefInt();
    @Column("has_valid_rf")
    private int hasValidRf = Misc.getUndefInt();
    @Column("material_cat")
    private int materialCat = Misc.getUndefInt();
    @Column("in_time")
    private Date entryTime;
    @Column("is_centered")
    private int isCentered = Misc.getUndefInt();

    public Date getExitTime() {
        return exitTime;
    }

    public void setExitTime(Date exitTime) {
        this.exitTime = exitTime;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }
    @Column("notes")
    private String notes;
    
    
            
    
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
   
//    public String getEntryTime() {
//        return entryTime;
//    }
//
//    public void setEntryTime(String entryTime) {
//        this.entryTime = entryTime;
//    }
    @Column("out_time")
    private Date exitTime;

//    public String getExitTime() {
//        return exitTime;
//    }
//
//    public void setExitTime(String exitTime) {
//        this.exitTime = exitTime;
//    }
    @Column("tare_wt")
    private double tareWt = Misc.getUndefDouble();
    @Column("gross_wt")
    private double grossWt = Misc.getUndefDouble();
    @Column("short_wt")
    private double shortWt = Misc.getUndefDouble();
    @Column("mark_for_qc")
    private int markForQc = Misc.getUndefInt();
    @Column("qc_id")
    private int qcId = Misc.getUndefInt();
    @Column("dispatch_permit_no")
    private String dispatchPermitNo;
    @Column("mineral_challan_no")
    private String mineralChallanNo;
    @Column("iia_receipt_no")
    private String iiaReceiptNo;
    @Column("has_gps_violations")
    private int hasGpsViolations = Misc.getUndefInt();
    @Column("updated_on")
    private Date updatedOn;
    @Column("user_by")
    private int updatedBy = Misc.getUndefInt();
    @Column("save_status")
    private int saveStatus = Misc.getUndefInt();
    public int getId() {
        return id;
    }

    public int getTprId() {
        return tprId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public int getWorkStationType() {
        return workStationType;
    }

    public int getWorkStationId() {
        return workStationId;
    }

    public int getHasValidRf() {
        return hasValidRf;
    }

   

    public double getTareWt() {
        return tareWt;
    }

    public double getGrossWt() {
        return grossWt;
    }

    public double getShortWt() {
        return shortWt;
    }

    public int getMarkForQc() {
        return markForQc;
    }

    public int getQcId() {
        return qcId;
    }

    public String getDispatchPermitNo() {
        return dispatchPermitNo;
    }

    public String getMineralChallanNo() {
        return mineralChallanNo;
    }

    public String getIiaReceiptNo() {
        return iiaReceiptNo;
    }

    public int getHasGpsViolations() {
        return hasGpsViolations;
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

    public void setTprId(int tprId) {
        this.tprId = tprId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setWorkStationType(int workStationType) {
        this.workStationType = workStationType;
    }

    public void setWorkStationId(int workStationId) {
        this.workStationId = workStationId;
    }

    public void setHasValidRf(int hasValidRf) {
        this.hasValidRf = hasValidRf;
    }


    public void setTareWt(double tareWt) {
        this.tareWt = tareWt;
    }

    public void setGrossWt(double grossWt) {
        this.grossWt = grossWt;
    }

    public void setShortWt(double shortWt) {
        this.shortWt = shortWt;
    }

    public void setMarkForQc(int markForQc) {
        this.markForQc = markForQc;
    }

    public void setQcId(int qcId) {
        this.qcId = qcId;
    }

    public void setDispatchPermitNo(String dispatchPermitNo) {
        this.dispatchPermitNo = dispatchPermitNo;
    }

    public void setMineralChallanNo(String mineralChallanNo) {
        this.mineralChallanNo = mineralChallanNo;
    }

    public void setIiaReceiptNo(String iiaReceiptNo) {
        this.iiaReceiptNo = iiaReceiptNo;
    }

    public void setHasGpsViolations(int hasGpsViolations) {
        this.hasGpsViolations = hasGpsViolations;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

	public int getSaveStatus() {
		return saveStatus;
	}

	public void setSaveStatus(int saveStatus) {
		this.saveStatus = saveStatus;
	}

	public int getMaterialCat() {
		return materialCat;
	}

	public void setMaterialCat(int materialCat) {
		this.materialCat = materialCat;
	}

	public int getIsCentered() {
		return isCentered;
	}

	public void setIsCentered(int isCentered) {
		this.isCentered = isCentered;
	}

	
}

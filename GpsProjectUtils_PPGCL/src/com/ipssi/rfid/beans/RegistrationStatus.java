package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("registration_status_rfid")
public class RegistrationStatus {

    @Table.KEY
    @PRIMARY_KEY
    @Table.Column("vehicle_id")
    int vehicle_id;
    @Table.Column("tpr_id")
    int tpr_id = Misc.getUndefInt();
    @Table.Column("tag_info")
    int tag_info = Misc.getUndefInt();
    @Table.Column("vehicle_info")
    int vehicle_info = Misc.getUndefInt();
    @Table.Column("driver_info")
    int driver_info = Misc.getUndefInt();
    @Table.Column("challan_record_info")
    int challan_record_info = Misc.getUndefInt();
    @Table.Column("multiple_tpr_info")
    int multiple_tpr_info = Misc.getUndefInt();
    @Table.Column("tag_info_created_on")
    Date tag_info_created_on ;
    @Table.Column("vehicle_info_created_on")
    Date vehicle_info_created_on;
    @Table.Column("driver_info_created_on")
    Date driver_info_created_on;
    @Table.Column("challan_record_info_created_on")
    Date challan_record_info_created_on;
    @Table.Column("multiple_tpr_info_created_on")
    Date multiple_tpr_info_created_on;
    @Table.Column("created_on")
    Date created_on;
    @Table.Column("updated_on")
    Date updated_on;
    @Table.Column("driver_id")
    int driver_id = Misc.getUndefInt();

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(int vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public int getTpr_id() {
        return tpr_id;
    }

    public void setTpr_id(int tpr_id) {
        this.tpr_id = tpr_id;
    }

    public int getTag_info() {
        return tag_info;
    }

    public void setTag_info(int tag_info) {
        this.tag_info = tag_info;
    }

    public int getVehicle_info() {
        return vehicle_info;
    }

    public void setVehicle_info(int vehicle_info) {
        this.vehicle_info = vehicle_info;
    }

    public int getDriver_info() {
        return driver_info;
    }

    public void setDriver_info(int driver_info) {
        this.driver_info = driver_info;
    }

    public int getChallan_record_info() {
        return challan_record_info;
    }

    public void setChallan_record_info(int challan_record_info) {
        this.challan_record_info = challan_record_info;
    }

    public int getMultiple_tpr_info() {
        return multiple_tpr_info;
    }

    public void setMultiple_tpr_info(int multiple_tpr_info) {
        this.multiple_tpr_info = multiple_tpr_info;
    }

    public Date getTag_info_created_on() {
        return tag_info_created_on;
    }

    public void setTag_info_created_on(Date tag_info_created_on) {
        this.tag_info_created_on = tag_info_created_on;
    }

    public Date getVehicle_info_created_on() {
        return vehicle_info_created_on;
    }

    public void setVehicle_info_created_on(Date vehicle_info_created_on) {
        this.vehicle_info_created_on = vehicle_info_created_on;
    }

    public Date getDriver_info_created_on() {
        return driver_info_created_on;
    }

    public void setDriver_info_created_on(Date driver_info_created_on) {
        this.driver_info_created_on = driver_info_created_on;
    }

    public Date getChallan_record_info_created_on() {
        return challan_record_info_created_on;
    }

    public void setChallan_record_info_created_on(Date challan_record_info_created_on) {
        this.challan_record_info_created_on = challan_record_info_created_on;
    }

    public Date getMultiple_tpr_info_created_on() {
        return multiple_tpr_info_created_on;
    }

    public void setMultiple_tpr_info_created_on(Date multiple_tpr_info_created_on) {
        this.multiple_tpr_info_created_on = multiple_tpr_info_created_on;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Date created_on) {
        this.created_on = created_on;
    }

    public Date getUpdated_on() {
        return updated_on;
    }

    public void setUpdated_on(Date updated_on) {
        this.updated_on = updated_on;
    }
}

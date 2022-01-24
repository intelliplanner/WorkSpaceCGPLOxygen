/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("vehicle_extended")
public class VehicleExtended {

    @Table.KEY
    @PRIMARY_KEY
    @Table.Column("vehicle_id")
    private int vehicleId= Misc.getUndefInt();;
    @Table.Column("transporter_id")
    private int transporter_id= Misc.getUndefInt();
    @Table.Column("transporter_code")
    private String transporterCode;
    @Table.Column("date_field1")
    private Date date_field1;
    @Table.Column("registeration_number")
    private String registeration_number;
    @Table.Column("registeration_number_expiry")
    private Date registeration_number_expiry;
    @Table.Column("double_field1")
    private double doubleField1= Misc.getUndefDouble();;
    @Table.Column("str_field4")
    private String strField4;
    @Table.Column("str_field8")
    private String strField8;
    @Table.Column("str_field9")
    private String strField9;
    @Table.Column("str_field5")
    private String strField5;
    @Table.Column("insurance_number")
    private String insurance_number;
    @Table.Column("insurance_number_expiry")
    private Date insurance_number_expiry;
    @Table.Column("permit1_number")
    private String permit1_number;
    @Table.Column("permit1_number_expiry")
    private Date permit1_number_expiry;
    @Table.Column("permit2_number")
    private String permit2_number;
    @Table.Column("permit2_number_expiry")
    private Date permit2_number_expiry;
    @Table.Column("str_field10")
    private String strField10;
    @Table.Column("str_field11")
    private String strField11;

    public String getStrField11() {
        return strField11;
    }

    public void setStrField11(String strField11) {
        this.strField11 = strField11;
    }

    public String getStrField10() {
        return strField10;
    }

    public void setStrField10(String strField10) {
        this.strField10 = strField10;
    }

    public String getPermit2_number() {
        return permit2_number;
    }

    public void setPermit2_number(String permit2_number) {
        this.permit2_number = permit2_number;
    }

    public Date getPermit2_number_expiry() {
        return permit2_number_expiry;
    }

    public void setPermit2_number_expiry(Date permit2_number_expiry) {
        this.permit2_number_expiry = permit2_number_expiry;
    }
    @Table.Column("str_field1")
    private String str_field1;
    @Table.Column("str_field2")
    private String str_field2;
    @Table.Column("str_field3")
    private String str_field3;
    @Table.Column("extended_status")
    private int extendedStatus=Misc.getUndefInt();
    @Table.Column("str_field6")
    private String strField6;
    @Table.Column("str_field7")
    private String strField7;

    public double getDoubleField1() {
        return doubleField1;
    }

    public void setDoubleField1(double doubleField1) {
        this.doubleField1 = doubleField1;
    }

    public String getStrField4() {
        return strField4;
    }

    public void setStrField4(String strField4) {
        this.strField4 = strField4;
    }

    public String getStrField5() {
        return strField5;
    }

    public void setStrField5(String strField5) {
        this.strField5 = strField5;
    }

    public String getStrField6() {
        return strField6;
    }

    public void setStrField6(String strField6) {
        this.strField6 = strField6;
    }

    public String getStrField7() {
        return strField7;
    }

    public void setStrField7(String strField7) {
        this.strField7 = strField7;
    }

    public String getStrField8() {
        return strField8;
    }

    public void setStrField8(String strField8) {
        this.strField8 = strField8;
    }

    public String getStrField9() {
        return strField9;
    }

    public void setStrField9(String strField9) {
        this.strField9 = strField9;
    }

    public Date getDate_field1() {
        return date_field1;
    }

    public void setDate_field1(Date date_field1) {
        this.date_field1 = date_field1;
    }

    public Date getRegisteration_number_expiry() {
        return registeration_number_expiry;
    }

    public void setRegisteration_number_expiry(Date registeration_number_expiry) {
        this.registeration_number_expiry = registeration_number_expiry;
    }

    public Date getInsurance_number_expiry() {
        return insurance_number_expiry;
    }

    public void setInsurance_number_expiry(Date insurance_number_expiry) {
        this.insurance_number_expiry = insurance_number_expiry;
    }

    public Date getPermit1_number_expiry() {
        return permit1_number_expiry;
    }

    public void setPermit1_number_expiry(Date permit1_number_expiry) {
        this.permit1_number_expiry = permit1_number_expiry;
    }

    public String getStr_field1() {
        return str_field1;
    }

    public void setStr_field1(String str_field1) {
        this.str_field1 = str_field1;
    }

    public String getStr_field2() {
        return str_field2;
    }

    public void setStr_field2(String str_field2) {
        this.str_field2 = str_field2;
    }

    public String getStr_field3() {
        return str_field3;
    }

    public void setStr_field3(String str_field3) {
        this.str_field3 = str_field3;
    }

    public int getExtendedStatus() {
        return extendedStatus;
    }

    public void setExtendedStatus(int extendedStatus) {
        this.extendedStatus = extendedStatus;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getTransporter_id() {
        return transporter_id;
    }

    public void setTransporter_id(int transporter_id) {
        this.transporter_id = transporter_id;
    }

    public String getRegisteration_number() {
        return registeration_number;
    }

    public void setRegisteration_number(String registeration_number) {
        this.registeration_number = registeration_number;
    }

    public String getInsurance_number() {
        return insurance_number;
    }

    public void setInsurance_number(String insurance_number) {
        this.insurance_number = insurance_number;
    }

    public String getPermit1_number() {
        return permit1_number;
    }

    public void setPermit1_number(String permit1_number) {
        this.permit1_number = permit1_number;
    }

	public String getTransporterCode() {
		return transporterCode;
	}

	public void setTransporterCode(String transporterCode) {
		this.transporterCode = transporterCode;
	}

	public VehicleExtended() {
		super();
	}

	public VehicleExtended(int vehicleId) {
		super();
		this.vehicleId = vehicleId;
	}
    
}

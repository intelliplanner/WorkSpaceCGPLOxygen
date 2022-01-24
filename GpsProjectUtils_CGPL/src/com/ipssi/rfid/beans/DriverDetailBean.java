package com.ipssi.rfid.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("driver_details")
public class DriverDetailBean {

    @Table.KEY
    @PRIMARY_KEY
    @Table.GENRATED
    @Table.Column("id")
    int id = Misc.getUndefInt();
    @Table.Column("org_id")
    int org_id = Misc.getUndefInt();
    @Table.Column("driver_name")
    String driver_name;
    @Table.Column("status")
    int status = Misc.getUndefInt();
    @Table.Column("driver_dl_number")
    String driver_dl_number;
    @Table.Column("driver_mobile_one")
    String driver_mobile_one;
    @Table.Column("driver_mobile_two")
    String driver_mobile_two;
    @Table.Column("driver_address_one")
    String driver_address_one;
    @Table.Column("driver_address_two")
    String driver_address_two;
    @Table.Column("driver_insurance_one")
    String driver_insurance_one;
    @Table.Column("driver_insurance_two")
    String driver_insurance_two;
    @Table.Column("insurance_one_date")
    String insurance_one_date;
    @Table.Column("insurance_two_date")
    String insurance_two_date;
    @Table.Column("updated_on")
    Date updated_on;
    @Table.Column("dl_date")
    Date dl_date;
    @Table.Column("info1")
    String info1;
    @Table.Column("info2")
    String info2;
    @Table.Column("info3")
    String info3;
    @Table.Column("info4")
    String info4;
    @Table.Column("vehicle_id_1")
    int vehicle_id_1 = Misc.getUndefInt();
    @Table.Column("provided_uid")
    String provided_uid;
    @Table.Column("driver_dob")
    Date driver_dob;
    @Table.Column("dl_expiry_date")
    Date dl_expiry_date;
    @Table.Column("ddt_training")
    int ddt_training = Misc.getUndefInt();
    @Table.Column("ddt_training_date")
    Date ddt_training_date;
    @Table.Column("ddt_training_expiry_date")
    Date ddt_training_expiry_date;
    @Table.Column("madical")
    int madical = Misc.getUndefInt();
    @Table.Column("medical_date")
    Date medical_date;
    @Table.Column("driver_std_name")
    String driver_std_name;
    @Table.Column("type")
    int type = Misc.getUndefInt();
    @Table.Column("guid_type")
    int guid_type = Misc.getUndefInt();
    byte[] capture_template_first;
    byte[] capture_template_second;
    byte[] capture_template_third;
    byte[] capture_template_fourth;
    byte[] capture_template_fifth;
    byte[] capture_template_sixth;
    byte[] capture_template_seventh;
    byte[] capture_template_eighth;
    byte[] capture_template_ninth;
    byte[] capture_template_tenth;
    byte[] first_finger_template;
    byte[] second_finger_template;
    byte[] third_finger_template;
    byte[] fourth_finger_template;
    byte[] fifth_finger_template;
    byte[] sixth_finger_template;
    byte[] seventh_finger_template;
    byte[] eight_finger_template;
    byte[] ninth_finger_template;
    byte[] tenth_finger_template;
    byte[] driver_photo;
    @Table.Column("created_on")
    Date created_on;
//                  `lov_field1` int(11) default NULL,                       
//                  `lov_field2` int(11) default NULL,                       
//                  `lov_field3` int(11) default NULL,                       
//                  `lov_field4` int(11) default NULL,                       
    
    int is_finger_captured;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrg_id() {
        return org_id;
    }

    public void setOrg_id(int org_id) {
        this.org_id = org_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDriver_dl_number() {
        return driver_dl_number;
    }

    public void setDriver_dl_number(String driver_dl_number) {
        this.driver_dl_number = driver_dl_number;
    }

    public String getDriver_mobile_one() {
        return driver_mobile_one;
    }

    public void setDriver_mobile_one(String driver_mobile_one) {
        this.driver_mobile_one = driver_mobile_one;
    }

    public String getDriver_mobile_two() {
        return driver_mobile_two;
    }

    public void setDriver_mobile_two(String driver_mobile_two) {
        this.driver_mobile_two = driver_mobile_two;
    }

    public String getDriver_address_one() {
        return driver_address_one;
    }

    public void setDriver_address_one(String driver_address_one) {
        this.driver_address_one = driver_address_one;
    }

    public String getDriver_address_two() {
        return driver_address_two;
    }

    public void setDriver_address_two(String driver_address_two) {
        this.driver_address_two = driver_address_two;
    }

    public String getDriver_insurance_one() {
        return driver_insurance_one;
    }

    public void setDriver_insurance_one(String driver_insurance_one) {
        this.driver_insurance_one = driver_insurance_one;
    }

    public String getDriver_insurance_two() {
        return driver_insurance_two;
    }

    public void setDriver_insurance_two(String driver_insurance_two) {
        this.driver_insurance_two = driver_insurance_two;
    }

    public String getInsurance_one_date() {
        return insurance_one_date;
    }

    public void setInsurance_one_date(String insurance_one_date) {
        this.insurance_one_date = insurance_one_date;
    }

    public String getInsurance_two_date() {
        return insurance_two_date;
    }

    public void setInsurance_two_date(String insurance_two_date) {
        this.insurance_two_date = insurance_two_date;
    }

    public Date getUpdated_on() {
        return updated_on;
    }

    public void setUpdated_on(Date updated_on) {
        this.updated_on = updated_on;
    }

    public Date getDl_date() {
        return dl_date;
    }

    public void setDl_date(Date dl_date) {
        this.dl_date = dl_date;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
    }

    public String getInfo3() {
        return info3;
    }

    public void setInfo3(String info3) {
        this.info3 = info3;
    }

    public String getInfo4() {
        return info4;
    }

    public void setInfo4(String info4) {
        this.info4 = info4;
    }

    public int getVehicle_id_1() {
        return vehicle_id_1;
    }

    public void setVehicle_id_1(int vehicle_id_1) {
        this.vehicle_id_1 = vehicle_id_1;
    }

    public String getProvided_uid() {
        return provided_uid;
    }

    public void setProvided_uid(String provided_uid) {
        this.provided_uid = provided_uid;
    }

    public Date getDriver_dob() {
        return driver_dob;
    }

    public void setDriver_dob(Date driver_dob) {
        this.driver_dob = driver_dob;
    }

    public Date getDl_expiry_date() {
        return dl_expiry_date;
    }

    public void setDl_expiry_date(Date dl_expiry_date) {
        this.dl_expiry_date = dl_expiry_date;
    }

    public int getDdt_training() {
        return ddt_training;
    }

    public void setDdt_training(int ddt_training) {
        this.ddt_training = ddt_training;
    }

    public Date getDdt_training_date() {
        return ddt_training_date;
    }

    public void setDdt_training_date(Date ddt_training_date) {
        this.ddt_training_date = ddt_training_date;
    }

    public Date getDdt_training_expiry_date() {
        return ddt_training_expiry_date;
    }

    public void setDdt_training_expiry_date(Date ddt_training_expiry_date) {
        this.ddt_training_expiry_date = ddt_training_expiry_date;
    }

    public int getMadical() {
        return madical;
    }

    public void setMadical(int madical) {
        this.madical = madical;
    }

    public Date getMedical_date() {
        return medical_date;
    }

    public void setMedical_date(Date medical_date) {
        this.medical_date = medical_date;
    }

    public String getDriver_std_name() {
        return driver_std_name;
    }

    public void setDriver_std_name(String driver_std_name) {
        this.driver_std_name = driver_std_name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getCapture_template_first() {
        return capture_template_first;
    }

    public void setCapture_template_first(byte[] capture_template_first) {
        this.capture_template_first = capture_template_first;
    }

    public int getGuid_type() {
        return guid_type;
    }

    public void setGuid_type(int guid_type) {
        this.guid_type = guid_type;
    }

    public byte[] getCapture_template_second() {
        return capture_template_second;
    }

    public void setCapture_template_second(byte[] capture_template_second) {
        this.capture_template_second = capture_template_second;
    }

    public byte[] getCapture_template_third() {
        return capture_template_third;
    }

    public void setCapture_template_third(byte[] capture_template_third) {
        this.capture_template_third = capture_template_third;
    }

    public byte[] getCapture_template_fourth() {
        return capture_template_fourth;
    }

    public void setCapture_template_fourth(byte[] capture_template_fourth) {
        this.capture_template_fourth = capture_template_fourth;
    }

    public byte[] getCapture_template_fifth() {
        return capture_template_fifth;
    }

    public void setCapture_template_fifth(byte[] capture_template_fifth) {
        this.capture_template_fifth = capture_template_fifth;
    }

    public byte[] getCapture_template_sixth() {
        return capture_template_sixth;
    }

    public void setCapture_template_sixth(byte[] capture_template_sixth) {
        this.capture_template_sixth = capture_template_sixth;
    }

    public byte[] getCapture_template_seventh() {
        return capture_template_seventh;
    }

    public void setCapture_template_seventh(byte[] capture_template_seventh) {
        this.capture_template_seventh = capture_template_seventh;
    }

    public byte[] getCapture_template_eighth() {
        return capture_template_eighth;
    }

    public void setCapture_template_eighth(byte[] capture_template_eighth) {
        this.capture_template_eighth = capture_template_eighth;
    }

    public byte[] getCapture_template_ninth() {
        return capture_template_ninth;
    }

    public void setCapture_template_ninth(byte[] capture_template_ninth) {
        this.capture_template_ninth = capture_template_ninth;
    }

    public byte[] getCapture_template_tenth() {
        return capture_template_tenth;
    }

    public void setCapture_template_tenth(byte[] capture_template_tenth) {
        this.capture_template_tenth = capture_template_tenth;
    }

    public byte[] getFirst_finger_template() {
        return first_finger_template;
    }

    public void setFirst_finger_template(byte[] first_finger_template) {
        this.first_finger_template = first_finger_template;
    }

    public byte[] getSecond_finger_template() {
        return second_finger_template;
    }

    public void setSecond_finger_template(byte[] second_finger_template) {
        this.second_finger_template = second_finger_template;
    }

    public byte[] getThird_finger_template() {
        return third_finger_template;
    }

    public void setThird_finger_template(byte[] third_finger_template) {
        this.third_finger_template = third_finger_template;
    }

    public byte[] getFourth_finger_template() {
        return fourth_finger_template;
    }

    public void setFourth_finger_template(byte[] fourth_finger_template) {
        this.fourth_finger_template = fourth_finger_template;
    }

    public byte[] getFifth_finger_template() {
        return fifth_finger_template;
    }

    public void setFifth_finger_template(byte[] fifth_finger_template) {
        this.fifth_finger_template = fifth_finger_template;
    }

    public byte[] getSixth_finger_template() {
        return sixth_finger_template;
    }

    public void setSixth_finger_template(byte[] sixth_finger_template) {
        this.sixth_finger_template = sixth_finger_template;
    }

    public byte[] getSeventh_finger_template() {
        return seventh_finger_template;
    }

    public void setSeventh_finger_template(byte[] seventh_finger_template) {
        this.seventh_finger_template = seventh_finger_template;
    }

    public byte[] getEight_finger_template() {
        return eight_finger_template;
    }

    public void setEight_finger_template(byte[] eight_finger_template) {
        this.eight_finger_template = eight_finger_template;
    }

    public byte[] getNinth_finger_template() {
        return ninth_finger_template;
    }

    public void setNinth_finger_template(byte[] ninth_finger_template) {
        this.ninth_finger_template = ninth_finger_template;
    }

    public byte[] getTenth_finger_template() {
        return tenth_finger_template;
    }

    public void setTenth_finger_template(byte[] tenth_finger_template) {
        this.tenth_finger_template = tenth_finger_template;
    }

    public byte[] getDriver_photo() {
        return driver_photo;
    }

    public void setDriver_photo(byte[] driver_photo) {
        this.driver_photo = driver_photo;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Date created_on) {
        this.created_on = created_on;
    }

    public int getIs_finger_captured() {
        return is_finger_captured;
    }

    public void setIs_finger_captured(int is_finger_captured) {
        this.is_finger_captured = is_finger_captured;
    }
}

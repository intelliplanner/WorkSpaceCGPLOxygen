/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package register_relative_information;

import java.util.Date;

/**
 *
 * @author rajeev
 */
public class HoldingRegistrationData {
    String Fname ;
    String Lname ;
    String Father_Name ;

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    
    byte[] image;
    String Email_ID ;
    Long Phone_Num ;
    Long Mobile_Num ;
    Date DOB;
      Long Pin;
       String Address;
    String City;
    String State;
  
    String Notes;

    public int getR_id() {
        return R_id;
    }

    public void setR_id(int R_id) {
        this.R_id = R_id;
    }
    int R_id;
    public Long getPhone_Num() {
        return Phone_Num;
    }

    public void setPhone_Num(Long Phone_Num) {
        this.Phone_Num = Phone_Num;
    }

    public Long getMobile_Num() {
        return Mobile_Num;
    }

    public void setMobile_Num(Long Mobile_Num) {
        this.Mobile_Num = Mobile_Num;
    }

    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public Long getPin() {
        return Pin;
    }

    public void setPin(Long Pin) {
        this.Pin = Pin;
    }
   

    public String getFname() {
        return Fname;
    }

    public void setFname(String Fname) {
        this.Fname = Fname;
    }

    public String getLname() {
        return Lname;
    }

    public void setLname(String Lname) {
        this.Lname = Lname;
    }

    public String getFather_Name() {
        return Father_Name;
    }

    public void setFather_Name(String Father_Name) {
        this.Father_Name = Father_Name;
    }

    

    public String getEmail_ID() {
        return Email_ID;
    }

    public void setEmail_ID(String Email_ID) {
        this.Email_ID = Email_ID;
    }

   

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public String getState() {
        return State;
    }

    public void setState(String State) {
        this.State = State;
    }

    

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String Notes) {
        this.Notes = Notes;
    }
}

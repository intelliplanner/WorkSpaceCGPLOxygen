package com.ipssi.tracker.driverDetails;

import java.util.ArrayList;
import java.util.Date;

public class DriverDetailsBean {
	private int id;
	private int orgId;
	private String name;
	private int  status;
	private String driverUID;
	private String driverMobileOne;
	private String driverMobileTwo;
	private String DLNumber;
	private String driverAddressOne;
	private String driverAddressTwo;
	private String driverInsuranceOne;
	private String driverInsuranceTwo;
	private Date insuranceOneDate;
	private Date insuranceTwoDate;
	private int vehicleId1;
	private int vehicleId2;
	private String vehicleId1Name;
	private String vehicleId2Name;
	
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getOrgId(){
		return this.orgId;
	}
	public void setOrgId(int orgId){
		this.orgId = orgId;
	}
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getStatus(){
		return this.status;
	}
	public void setStatus(int status){
		this.status = status;
	}
	public String getDriverUID(){
		return this.driverUID;
	}
	public void setDriverUID(String driverUID){
		this.driverUID = driverUID;
	}
	public String getDriverMobileOne(){
		return this.driverMobileOne;
	}
	public void setDriverMobileOne(String driverMobileOne){
		this.driverMobileOne = driverMobileOne;
	}
	public String getDriverMobileTwo(){
		return this.driverMobileTwo;
	}
	public void setDriverMobileTwo(String driverMobileTwo){
		this.driverMobileTwo = driverMobileTwo;
	}
	public String getDLNumber(){
		return this.DLNumber;
	}
	public void setDLNumber(String DLNumber){
		this.DLNumber = DLNumber;
	}
	public String getDriverAddressOne(){
		return this.driverAddressOne;
	}
	public void setDriverAddressOne(String driverAddressOne){
		this.driverAddressOne = driverAddressOne;
	}
	public String getDriverAddressTwo(){
		return this.driverAddressTwo;
	}
	public void setDriverAddressTwo(String driverAddressTwo){
		this.driverAddressTwo = driverAddressTwo;
	}
	public String getDriverInsuranceOne(){
		return this.driverInsuranceOne;
	}
	public void setDriverInsuranceOne(String driverInsuranceOne){
		this.driverInsuranceOne = driverInsuranceOne;
	}
	public String getDriverInsuranceTwo(){
		return this.driverInsuranceTwo;
	}
	public void setDriverInsuranceTwo(String driverInsuranceTwo){
		this.driverInsuranceTwo = driverInsuranceTwo;
	}
	public Date getInsuranceOneDate() {
		return insuranceOneDate;
	}
	public void setInsuranceOneDate(Date insuranceOneDate) {
		this.insuranceOneDate = insuranceOneDate;
	}
	public Date getInsuranceTwoDate() {
		return insuranceTwoDate;
	}
	public void setInsuranceTwoDate(Date insuranceTwoDate) {
		this.insuranceTwoDate = insuranceTwoDate;
	}
	private ArrayList<DriverSkillsBean> driverSkillsList = new ArrayList<DriverSkillsBean>();
	public ArrayList<DriverSkillsBean> getDriverSkillsList(){
		return this.driverSkillsList;
	}
	public void setDriverSkillsList(ArrayList<DriverSkillsBean> driverSkillsList){
		this.driverSkillsList = driverSkillsList;
	}
	public int getVehicleId1() {
		return vehicleId1;
	}
	public void setVehicleId1(int vehicleId1) {
		this.vehicleId1 = vehicleId1;
	}
	public int getVehicleId2() {
		return vehicleId2;
	}
	public void setVehicleId2(int vehicleId2) {
		this.vehicleId2 = vehicleId2;
	}
	public String getVehicleId1Name() {
		return vehicleId1Name;
	}
	public void setVehicleId1Name(String vehicleId1Name) {
		this.vehicleId1Name = vehicleId1Name;
	}
	public String getVehicleId2Name() {
		return vehicleId2Name;
	}
	public void setVehicleId2Name(String vehicleId2Name) {
		this.vehicleId2Name = vehicleId2Name;
	}
	
}

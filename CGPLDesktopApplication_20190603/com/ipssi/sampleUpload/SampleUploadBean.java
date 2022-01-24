package com.ipssi.sampleUpload;

import java.util.Date;

/**
 * @author IPSSI
 * 
 */

public class SampleUploadBean {

	int id;
	String nameOfSiding;
	String rrNo;
	String transporter;
	int noOfTrucksLoaded;
	int noOfTrucksUnloaded;
	double wetQtyMt;
	double tmArb;
	double imAdb;
	double ashAdb;
	double vmAdb;
	double fcAdb;
	int gcvAdb;
	int gcvArb;
	double imEq;
	double ashEq;
	double vmEq;
	double fcEq;
	double gcvKcalKg;
	String gcvOrGrade;
	String dateOfUnloading;
	Date dateOfSamplePreparation;
	public Date getDateOfSamplePreparation() {
		return dateOfSamplePreparation;
	}

	public void setDateOfSamplePreparation(Date dateOfSamplePreparation) {
		this.dateOfSamplePreparation = dateOfSamplePreparation;
	}

	String analyzedGrade;
	int status;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNameOfSiding() {
		return nameOfSiding;
	}

	public void setNameOfSiding(String nameOfSiding) {
		this.nameOfSiding = nameOfSiding;
	}

	public String getRrNo() {
		return rrNo;
	}

	public void setRrNo(String rrNo) {
		this.rrNo = rrNo;
	}

	public String getTransporter() {
		return transporter;
	}

	public void setTransporter(String transporter) {
		this.transporter = transporter;
	}

	public int getNoOfTrucksLoaded() {
		return noOfTrucksLoaded;
	}

	public void setNoOfTrucksLoaded(int noOfTrucksLoaded) {
		this.noOfTrucksLoaded = noOfTrucksLoaded;
	}

	public int getNoOfTrucksUnloaded() {
		return noOfTrucksUnloaded;
	}

	public void setNoOfTrucksUnloaded(int noOfTrucksUnloaded) {
		this.noOfTrucksUnloaded = noOfTrucksUnloaded;
	}

	public double getWetQtyMt() {
		return wetQtyMt;
	}

	public void setWetQtyMt(double wetQtyMt) {
		this.wetQtyMt = wetQtyMt;
	}

	public double getTmArb() {
		return tmArb;
	}

	public void setTmArb(double tmArb) {
		this.tmArb = tmArb;
	}

	public double getImAdb() {
		return imAdb;
	}

	public void setImAdb(double imAdb) {
		this.imAdb = imAdb;
	}

	public double getAshAdb() {
		return ashAdb;
	}

	public void setAshAdb(double ashAdb) {
		this.ashAdb = ashAdb;
	}

	public double getVmAdb() {
		return vmAdb;
	}

	public void setVmAdb(double vmAdb) {
		this.vmAdb = vmAdb;
	}

	public double getFcAdb() {
		return fcAdb;
	}

	public void setFcAdb(double fcAdb) {
		this.fcAdb = fcAdb;
	}

	public int getGcvAdb() {
		return gcvAdb;
	}

	public void setGcvAdb(int gcvAdb) {
		this.gcvAdb = gcvAdb;
	}

	public int getGcvArb() {
		return gcvArb;
	}

	public void setGcvArb(int gcvArb) {
		this.gcvArb = gcvArb;
	}

	public double getImEq() {
		return imEq;
	}

	public void setImEq(double imEq) {
		this.imEq = imEq;
	}

	public double getAshEq() {
		return ashEq;
	}

	public void setAshEq(double ashEq) {
		this.ashEq = ashEq;
	}

	public double getVmEq() {
		return vmEq;
	}

	public void setVmEq(double vmEq) {
		this.vmEq = vmEq;
	}

	public double getFcEq() {
		return fcEq;
	}

	public void setFcEq(double fcEq) {
		this.fcEq = fcEq;
	}

	public double getGcvKcalKg() {
		return gcvKcalKg;
	}

	public void setGcvKcalKg(double gcvKcalKg) {
		this.gcvKcalKg = gcvKcalKg;
	}

	public String getGcvOrGrade() {
		return gcvOrGrade;
	}

	public void setGcvOrGrade(String gcvOrGrade) {
		this.gcvOrGrade = gcvOrGrade;
	}

	public String getDateOfUnloading() {
		return dateOfUnloading;
	}

	public void setDateOfUnloading(String dateOfUnloading) {
		this.dateOfUnloading = dateOfUnloading;
	}


	public String getAnalyzedGrade() {
		return analyzedGrade;
	}

	public void setAnalyzedGrade(String analyzedGrade) {
		this.analyzedGrade = analyzedGrade;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}

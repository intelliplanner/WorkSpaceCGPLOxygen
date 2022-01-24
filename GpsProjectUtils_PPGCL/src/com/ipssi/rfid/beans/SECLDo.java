package com.ipssi.rfid.beans;

public class SECLDo {
	
private int id;
private String doNumber;
private String preferredWB1;
private String preferredWB2;
private String preferredWB3;
private String preferredWB4;
private double preferredQty1;

private boolean flagWB1 = true;
private boolean flagWB2 = true;
private boolean flagWB3 = true;
private boolean flagWB4 = true;

private boolean flagWB1Old = true;
private boolean flagWB2Old = true;
private boolean flagWB3Old = true;
private boolean flagWB4Old = true;

public boolean getFlagWB1(){
	return flagWB1;
}

public boolean getFlagWB2(){
	return flagWB2;
}

public boolean getFlagWB3(){
	return flagWB3;
}

public boolean getFlagWB4(){
	return flagWB4;
}

public void setFlagWB1(boolean flag){
	flagWB1 = flag;
}

public void setFlagWB2(boolean flag){
	flagWB2 = flag;
}

public void setFlagWB3(boolean flag){
	flagWB3 = flag;
}

public void setFlagWB4(boolean flag){
	flagWB4 = flag;
}
public String getPreferredWB2old() {
	return preferredWB2old;
}
public void setPreferredWB2old(String preferredWB2old) {
	this.preferredWB2old = preferredWB2old;
}
public String getPreferredWB3old() {
	return preferredWB3old;
}
public void setPreferredWB3old(String preferredWB3old) {
	this.preferredWB3old = preferredWB3old;
}
public String getPreferredWB4old() {
	return preferredWB4old;
}
public void setPreferredWB4old(String preferredWB4old) {
	this.preferredWB4old = preferredWB4old;
}
public double getPreferredQty1old() {
	return preferredQty1old;
}
public void setPreferredQty1old(double preferredQty1old) {
	this.preferredQty1old = preferredQty1old;
}
public double getPreferredQty2old() {
	return preferredQty2old;
}
public void setPreferredQty2old(double preferredQty2old) {
	this.preferredQty2old = preferredQty2old;
}
public double getPreferredQty3old() {
	return preferredQty3old;
}
public void setPreferredQty3old(double preferredQty3old) {
	this.preferredQty3old = preferredQty3old;
}
public double getPreferredQty4old() {
	return preferredQty4old;
}
public void setPreferredQty4old(double preferredQty4old) {
	this.preferredQty4old = preferredQty4old;
}
public void setRemainingQty(double remainingQty) {
	this.remainingQty = remainingQty;
}
private double preferredQty2;
private double preferredQty3;
private double preferredQty4;
private String preferredWB1old;
private String preferredWB2old;
private String preferredWB3old;
private String preferredWB4old;
private double preferredQty1old;
private double preferredQty2old;
private double preferredQty3old;
private double preferredQty4old;

private double remainingQty;
private double allocatedQty;
private double qtyAlreadyLifted;

public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getPreferredWB1() {
	return preferredWB1;
}
public void setPreferredWB1(String preferredWB1) {
	this.preferredWB1 = preferredWB1;
}
public String getPreferredWB2() {
	return preferredWB2;
}
public void setPreferredWB2(String preferredWB2) {
	this.preferredWB2 = preferredWB2;
}
public String getPreferredWB3() {
	return preferredWB3;
}
public void setPreferredWB3(String preferredWB3) {
	this.preferredWB3 = preferredWB3;
}
public String getPreferredWB4() {
	return preferredWB4;
}
public void setPreferredWB4(String preferredWB4) {
	this.preferredWB4 = preferredWB4;
}
public double getPreferredQty1() {
	return preferredQty1;
}
public void setPreferredQty1(double preferredQty1) {
	this.preferredQty1 = preferredQty1;
}
public double getPreferredQty2() {
	return preferredQty2;
}
public void setPreferredQty2(double preferredQty2) {
	this.preferredQty2 = preferredQty2;
}
public double getPreferredQty3() {
	return preferredQty3;
}
public void setPreferredQty3(double preferredQty3) {
	this.preferredQty3 = preferredQty3;
}
public double getPreferredQty4() {
	return preferredQty4;
}
public void setPreferredQty4(double preferredQty4) {
	this.preferredQty4 = preferredQty4;
}
public void setDoNumber(String doNumber) {
	this.doNumber = doNumber;
}
public String getDoNumber() {
	return doNumber;
}
public void setRemainingQty() {
	
	if((preferredQty1+preferredQty2+preferredQty3+preferredQty4)>0)
	this.remainingQty = preferredQty1+preferredQty2+preferredQty3+preferredQty4;
	else 
		remainingQty=allocatedQty-qtyAlreadyLifted;
}
public double getRemainingQty() {
	return remainingQty;
}
public void setQtyAlreadyLifted(double qtyAlreadyLifted) {
	this.qtyAlreadyLifted = qtyAlreadyLifted;
}
public double getQtyAlreadyLifted() {
	return qtyAlreadyLifted;
}
public void setAllocatedQty(double alocatedQty) {
	this.allocatedQty = alocatedQty;
}
public double getAllocatedQty() {
	return allocatedQty;
}
public void setPreferredWB1old(String preferredWB1old) {
	this.preferredWB1old = preferredWB1old;
}
public String getPreferredWB1old() {
	return preferredWB1old;
}

public void setFlagWB1Old(boolean flagWB1Old) {
	this.flagWB1Old = flagWB1Old;
}

public boolean getFlagWB1Old() {
	return flagWB1Old;
}

public void setFlagWB2Old(boolean flagWB2Old) {
	this.flagWB2Old = flagWB2Old;
}

public boolean getFlagWB2Old() {
	return flagWB2Old;
}

public void setFlagWB3Old(boolean flagWB3Old) {
	this.flagWB3Old = flagWB3Old;
}

public boolean getFlagWB3Old() {
	return flagWB3Old;
}

public void setFlagWB4Old(boolean flagWB4Old) {
	this.flagWB4Old = flagWB4Old;
}

public boolean getFlagWB4Old() {
	return flagWB4Old;
}

}

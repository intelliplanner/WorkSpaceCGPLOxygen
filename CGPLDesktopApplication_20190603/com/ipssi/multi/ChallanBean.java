package com.ipssi.multi;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;

public class ChallanBean {
	private int vehicleId = Misc.getUndefInt();
	private String vehicleName;
	private int fromStationId = Misc.getUndefInt();
	private String fromStationName;
	private String consignor;
	private String fromAddress;
	private int toStationId = Misc.getUndefInt();
	private String toStationName;
	
	private String consignee;
	private String toAddress;
	private String challanNumber;
	private java.util.Date challanDate;
	private double grossLoad = Misc.getUndefDouble();
	private double tareLoad = Misc.getUndefDouble();
	private double grossUnload = Misc.getUndefDouble();
	private double tareUnload = Misc.getUndefDouble();
	private int materialId = Misc.getUndefInt();
	private int portNodeId = Misc.G_TOP_LEVEL_PORT;
	private long deliveryDate;
	private double deliveryQty;
	private String GRN;
	private String deliveryNotes;
	private String billEmail;
	private double invoiceDistKM = Misc.getUndefDouble();
	private ArrayList<MiscInner.PairIntStr> materialList = null;
	private ArrayList<MiscInner.PairIntStr> loadOp = null;//shared
	private ArrayList<MiscInner.PairIntStr> unloadOp = null;//shared
	private int fixedLoad = Misc.getUndefInt();//if > 0 then fixed Load Op Station .. if 0 then only one from op station list else take by address too
	private int fixeUnLoad = Misc.getUndefInt();
	private int challanId = Misc.getUndefInt();
	private int tripId = Misc.getUndefInt();
	private int tripStatus = 1;
	private int challanType = 1;
	private int loadStatus = 1;
	private long currETA = Misc.getUndefInt();
	private long origETA = Misc.getUndefInt();
	private long atCustArrivalTS = Misc.getUndefInt();
	private String description = null;
	private String notes = null;
	private ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> multi_material = null;
	public int getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}
	public String getVehicleName() {
		return vehicleName;
	}
	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	public int getFromStationId() {
		return fromStationId;
	}
	public void setFromStationId(int fromStationId) {
		this.fromStationId = fromStationId;
	}
	public String getConsignor() {
		return consignor;
	}
	public void setConsignor(String consignor) {
		this.consignor = consignor;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public int getToStationId() {
		return toStationId;
	}
	public void setToStationId(int toStationId) {
		this.toStationId = toStationId;
	}
	public String getConsignee() {
		return consignee;
	}
	public void setConsignee(String consignee) {
		this.consignee = consignee;
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public String getChallanNumber() {
		return challanNumber;
	}
	public void setChallanNumber(String challanNumber) {
		this.challanNumber = challanNumber;
	}
	public java.util.Date getChallanDate() {
		return challanDate;
	}
	public void setChallanDate(java.util.Date challanDate) {
		this.challanDate = challanDate;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public ArrayList<MiscInner.PairIntStr> getMaterialList() {
		return materialList;
	}
	public void setMaterialList(ArrayList<MiscInner.PairIntStr> materialList) {
		this.materialList = materialList;
	}
	public ArrayList<MiscInner.PairIntStr> getLoadOp() {
		return loadOp;
	}
	public void setLoadOp(ArrayList<MiscInner.PairIntStr> loadOp) {
		this.loadOp = loadOp;
	}
	public ArrayList<MiscInner.PairIntStr> getUnloadOp() {
		return unloadOp;
	}
	public void setUnloadOp(ArrayList<MiscInner.PairIntStr> unloadOp) {
		this.unloadOp = unloadOp;
	}
	public int getFixedLoad() {
		return fixedLoad;
	}
	public void setFixedLoad(int fixedLoad) {
		this.fixedLoad = fixedLoad;
	}
	public int getFixeUnLoad() {
		return fixeUnLoad;
	}
	public void setFixeUnLoad(int fixeUnLoad) {
		this.fixeUnLoad = fixeUnLoad;
	}
	public int getChallanId() {
		return challanId;
	}
	public void setChallanId(int challanId) {
		this.challanId = challanId;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	public long getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(long l) {
		this.deliveryDate = l;
	}
	public double getDeliveryQty() {
		return deliveryQty;
	}
	public void setDeliveryQty(double deliveryQty) {
		this.deliveryQty = deliveryQty;
	}
	public String getGRN() {
		return GRN;
	}
	public void setGRN(String grn) {
		GRN = grn;
	}
	public String getDeliveryNotes() {
		return deliveryNotes;
	}
	public void setDeliveryNotes(String deliveryNotes) {
		this.deliveryNotes = deliveryNotes;
	}
	public String getBillEmail() {
		return billEmail;
	}
	public void setBillEmail(String billEmail) {
		this.billEmail = billEmail;
	}
	public double getInvoiceDistKM() {
		return invoiceDistKM;
	}
	public void setInvoiceDistKM(double invoiceDistKM) {
		this.invoiceDistKM = invoiceDistKM;
	}
	public int getTripStatus() {
		return tripStatus;
	}
	public void setTripStatus(int tripStatus) {
		this.tripStatus = tripStatus;
	}
	public double getGrossLoad() {
		return grossLoad;
	}
	public void setGrossLoad(double grossLoad) {
		this.grossLoad = grossLoad;
	}
	public double getTareLoad() {
		return tareLoad;
	}
	public void setTareLoad(double tareLoad) {
		this.tareLoad = tareLoad;
	}
	public double getGrossUnload() {
		return grossUnload;
	}
	public void setGrossUnload(double grossUnload) {
		this.grossUnload = grossUnload;
	}
	public double getTareUnload() {
		return tareUnload;
	}
	public void setTareUnload(double tareUnload) {
		this.tareUnload = tareUnload;
	}
	public ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> getMulti_material() {
		return multi_material;
	}
	public void setMulti_material(
			ArrayList<Triple<Integer, Triple<Integer, Double,String>, Pair<Double, String>>> multi_material) {
		this.multi_material = multi_material;
	}
	public String getFromStationName() {
		return fromStationName;
	}
	public void setFromStationName(String fromStationName) {
		this.fromStationName = fromStationName;
	}
	public String getToStationName() {
		return toStationName;
	}
	public void setToStationName(String toStationName) {
		this.toStationName = toStationName;
	}
	public int getChallanType() {
		return challanType;
	}
	public void setChallanType(int challanType) {
		this.challanType = challanType;
	}
	public int getLoadStatus() {
		return loadStatus;
	}
	public void setLoadStatus(int loadStatus) {
		this.loadStatus = loadStatus;
	}
	public long getCurrETA() {
		return currETA;
	}
	public void setCurrETA(long currETA) {
		this.currETA = currETA;
	}
	public long getOrigETA() {
		return origETA;
	}
	public void setOrigETA(long origETA) {
		this.origETA = origETA;
	}
	public long getAtCustArrivalTS() {
		return atCustArrivalTS;
	}
	public void setAtCustArrivalTS(long atCustArrivalTS) {
		this.atCustArrivalTS = atCustArrivalTS;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
}

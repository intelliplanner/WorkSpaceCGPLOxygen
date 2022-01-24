package com.ipssi.inventory;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * 
 * @author balwant
 *
 */

public class InventoryMngBean {
    
	private int id;
	private int productId;
	private int categoryId;
	private String itemCode;
	private String itemName;
	private String manufacturer;
	private String manufacturerCode;
	private String notes;
	private String lotNumber;
	private int quantaty;
	private int initialQty;
	private int remainQty;
	private double price;
	private int age;
	private String supplier;
	private String purchaseReceiver;
	private String deliveryReport;
	private long warrantyTill;
	private long mfgDate;
	private long acquisitionDate;
	private long createdOn;
	private boolean yetReleased;
	private int stockDifference;
	private int productLife;
	private int productLifeUnit;
	private int portNodeId;
	private String ticketId;
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public String getTicketId() {
		return ticketId;
	}
	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getManufacturerCode() {
		return manufacturerCode;
	}
	public void setManufacturerCode(String manufacturerCode) {
		this.manufacturerCode = manufacturerCode;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getLotNumber() {
		return lotNumber;
	}
	public void setLotNumber(String lotNumber) {
		this.lotNumber = lotNumber;
	}
	public int getQuantaty() {
		return quantaty;
	}
	public void setQuantaty(int quantaty) {
		this.quantaty = quantaty;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getSupplier() {
		return supplier;
	}
	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}
	public String getPurchaseReceiver() {
		return purchaseReceiver;
	}
	public void setPurchaseReceiver(String purchaseReceiver) {
		this.purchaseReceiver = purchaseReceiver;
	}
	public String getDeliveryReport() {
		return deliveryReport;
	}
	public void setDeliveryReport(String deliveryReport) {
		this.deliveryReport = deliveryReport;
	}
	public long getWarrantyTill() {
		return warrantyTill;
	}
	public void setWarrantyTill(long warrantyTill) {
		this.warrantyTill = warrantyTill;
	}
	public long getMfgDate() {
		return mfgDate;
	}
	public void setMfgDate(long mfgDate) {
		this.mfgDate = mfgDate;
	}
	public long getAcquisitionDate() {
		return acquisitionDate;
	}
	public void setAcquisitionDate(long acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}
	public long getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
	public int getInitialQty() {
		return initialQty;
	}
	public void setInitialQty(int initialQty) {
		this.initialQty = initialQty;
	}
	public boolean isYetReleased() {
		return yetReleased;
	}
	public void setYetReleased(boolean yetReleased) {
		this.yetReleased = yetReleased;
	}
	public int getStockDifference() {
		return stockDifference;
	}
	public void setStockDifference(int stockDifference) {
		this.stockDifference = stockDifference;
	}
	public int getRemainQty() {
		return remainQty;
	}
	public void setRemainQty(int remainQty) {
		this.remainQty = remainQty;
	}
	public int getProductLife() {
		return productLife;
	}
	public void setProductLife(int productLife) {
		this.productLife = productLife;
	}
	public int getProductLifeUnit() {
		return productLifeUnit;
	}
	public void setProductLifeUnit(int productLifeUnit) {
		this.productLifeUnit = productLifeUnit;
	}
	


	
}

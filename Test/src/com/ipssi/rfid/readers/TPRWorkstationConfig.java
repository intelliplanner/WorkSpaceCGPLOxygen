package com.ipssi.rfid.readers;

public class TPRWorkstationConfig {
	private int workstationType;
	private int materialCat;
	private boolean initCard = false;
	private boolean returnCard = false;
	
	public TPRWorkstationConfig(int workstationType, int materialCat) {
		super();
		this.workstationType = workstationType;
		this.materialCat = materialCat;
	}
	public int getWorkstationType() {
		return workstationType;
	}
	public void setWorkstationType(int workstationType) {
		this.workstationType = workstationType;
	}
	public int getMaterialCat() {
		return materialCat;
	}
	public void setMaterialCat(int materialCat) {
		this.materialCat = materialCat;
	}
	public boolean isInitCard() {
		return initCard;
	}
	public void setInitCard(boolean initCard) {
		this.initCard = initCard;
	}
	public boolean isReturnCard() {
		return returnCard;
	}
	public void setReturnCard(boolean returnCard) {
		this.returnCard = returnCard;
	}
	
	
}

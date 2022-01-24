package com.ipssi.rfid.ui.controller;

import javafx.scene.control.Button;

public interface ControllerI {

	void clearInputs();

	void stopRfid();

	boolean save();

	public void init(MainController parent);

	void setVehicleName(String vehicleName);

	void clearVehicleName();

	void enableController(Button controllerId, boolean enable);

	public void enableManualEntry(boolean enable);

	public void stopSyncTprService();

	public void initController(SettingController settingParent);

	public void requestFocusNextField();

	public void setTitle(String title);
	
	public void vehicleNameAction();

	public void dlNoAction();

}

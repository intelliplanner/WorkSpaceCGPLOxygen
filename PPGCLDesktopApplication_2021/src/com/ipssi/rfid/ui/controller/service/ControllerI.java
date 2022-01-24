package com.ipssi.rfid.ui.controller.service;

import java.sql.Connection;

import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.ui.controller.MainController;
import com.ipssi.rfid.ui.controller.SettingController;

import javafx.scene.control.Button;

public interface ControllerI {

	void clearInputs();

	void stopRfid();

	boolean save();

	void init(MainController parent);

	void setVehicleName(String vehicleName);

	void clearVehicleName();

	void enableController(Button controllerId, boolean enable);

	void enableManualEntry(boolean enable);

	void stopSyncTprService();

	void initController(SettingController settingParent);

	void requestFocusNextField();

	void setTitle(String title);

	void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager);

}

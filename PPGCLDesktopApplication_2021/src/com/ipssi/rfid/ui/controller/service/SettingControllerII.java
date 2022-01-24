package com.ipssi.rfid.ui.controller.service;

import com.ipssi.rfid.ui.controller.MainController;
import com.ipssi.rfid.ui.controller.SettingController;

public interface SettingControllerII {

	void clearInputs();

	void defaultSetting();

	boolean saveData();

	void init(MainController parent);

	void initController(SettingController parent);

	void requestFocusNextField();

	void setTitle(String title);

}

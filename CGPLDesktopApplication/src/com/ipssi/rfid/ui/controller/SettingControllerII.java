package com.ipssi.rfid.ui.controller;

public interface SettingControllerII {

	void clearInputs();

	void defaultSetting();

	boolean saveData();

	public void init(MainController parent);

	public void initController(SettingController parent);

	public void requestFocusNextField();

	public void setTitle(String title);

}

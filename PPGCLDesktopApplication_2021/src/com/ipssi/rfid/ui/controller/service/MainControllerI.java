package com.ipssi.rfid.ui.controller.service;

import com.ipssi.rfid.ui.controller.StartApplicationPPGCL;

import javafx.event.Event;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public interface MainControllerI {
	void screenComponentVisible(boolean screenTitleVisible, boolean mainMenuContainerVisible,
			boolean blockingReasonLabelVisible, boolean signOutControlerVisible, boolean mainActionContainerVisible);

	int promptNewTest(String title, String message, String[] options);

	void showAlert(AlertType alertType, String title, String message);

	void showAlert(String title, String message);

	int prompt(String title, String message, String[] options, double x, double y);

	int prompt(String title, String message, String[] options);

	void registerIdleMonitor();

	void setScreenTitle(String screenTitle);

	void initializeScreen();

	void initMenuCache();

	void loadScreen(String fxmlUrl, String menuId);

	void init(StartApplicationPPGCL startApplicationPPGCL);

	String getSourceId(Event event);

	void initializeMenuControlls();

	void signOutAction();

	void changeContainerColor();

	void saveAction();

	void handleActionControl(Button control);

	void stopRFID();

	void clearInputsAction();

	void setMenuVisible(boolean _gateInVisible, boolean _tareVisible, boolean _grossVisible, boolean _gateOutVisible,
			boolean _settingMenuVisible, boolean _tagReadMenuVisible, boolean _tprDetailsMenuVisible,
			boolean _issueTagVisible, boolean _salesOrderVisible);

	void setMenuDisable(boolean gateInVisible, boolean tareVisible, boolean grossVisible, boolean gateOutVisible,
			boolean settingVisible, boolean transporterMenuVisible, boolean tagReadMenuVisible,
			boolean tprDetailsMenuDisable, boolean tagIssueMenuDisable, boolean salesOrderDisable);
}

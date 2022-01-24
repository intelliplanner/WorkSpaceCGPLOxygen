package com.ipssi.rfid.ui.controller.service;

import javafx.event.ActionEvent;

public interface ActionControllerI {
	void vehicleAction();
	void handleActionControl(String controllId);
	void onControlAction(ActionEvent event);
}

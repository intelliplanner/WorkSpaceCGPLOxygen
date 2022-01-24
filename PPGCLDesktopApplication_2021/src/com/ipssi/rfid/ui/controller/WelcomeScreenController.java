/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class WelcomeScreenController implements Initializable, ControllerI {

	private static final Logger log = Logger.getLogger(WelcomeScreenController.class.getName());

	@FXML
	private JFXButton SIGN_OUT_CONTROL;

	@FXML
	private AnchorPane WELCOME_ANCHORPANE;
	
	@FXML
	public VBox  WELCOME_VBOX;
	
	private MainController parent = null;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// parent.initializeScreen();
	}

	@FXML
	private void signOutMouseClicked(MouseEvent event) {
		parent.signOutAction(); // signOut();
	}

	@FXML
	private void signOutKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			parent.signOutAction();
		}
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	@Override
	public void clearInputs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopRfid() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVehicleName(String vehicleName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearVehicleName() {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableController(Button controllerId, boolean enable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableManualEntry(boolean enable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSyncTprService() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initController(SettingController settingParent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestFocusNextField() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub
		
	}

}

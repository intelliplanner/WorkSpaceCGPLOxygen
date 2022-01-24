/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class WelcomeScreenController implements Initializable, ControllerI {

	@FXML
	private JFXButton SIGN_OUT_CONTROL;
	private MainController parent = null;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}

	@FXML
	private void signOutMouseClicked(MouseEvent event) {
		signOut();
	}

	@FXML
	private void signOutKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			signOut();
		}
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	private void signOut() {
		parent.signOutAction();
	}

	@Override
	public void stopRfid() {
	}

	@Override
	public void setVehicleName(String vehicleName) {
	}

	@Override
	public void clearVehicleName() {
	}

	@Override
	public void enableController(Button controllerId, boolean enable) {
	}

	@Override
	public void enableManualEntry(boolean enable) {
	}

	@Override
	public void stopSyncTprService() {
	}

	@Override
	public void initController(SettingController parent) {
	}

	@Override
	public void requestFocusNextField() {
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public void vehicleNameAction() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void dlNoAction() {
		// TODO Auto-generated method stub
		
	}


}

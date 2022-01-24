/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.constant.PropertyManagerNew.PropertyType;
import com.ipssi.rfid.processor.TokenManager;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class DatabaseSettingController implements Initializable, ControllerI {

	@FXML
	private JFXTextField textBoxLocalHost;
	@FXML
	private JFXTextField textBoxLocalPort;
	@FXML
	private JFXTextField textBoxLocalDB;
	@FXML
	private JFXTextField textBoxLocalUsername;
	@FXML
	private JFXPasswordField textBoxLocalPassword;
	@FXML
	private JFXTextField textBoxLocalMaxConn;

	@FXML
	private Label screenTitle;
	private SettingController parentSettingController = null;

	@FXML
	private void onControlKeyPress(KeyEvent event) {
	}

	/**
	 * Initializes the controller class.
	 */

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		loadDataBase(null);
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public boolean save() {
		PropertyManagerNew.setProperty(PropertyType.Database, "desktop.DBConn.host", textBoxLocalHost.getText());
		PropertyManagerNew.setProperty(PropertyType.Database, "desktop.DBConn.Database", textBoxLocalDB.getText());
		PropertyManagerNew.setProperty(PropertyType.Database, "desktop.DBConn.port", textBoxLocalPort.getText());
		PropertyManagerNew.setProperty(PropertyType.Database, "desktop.DBConn.userName", textBoxLocalUsername.getText());
		PropertyManagerNew.setProperty(PropertyType.Database, "desktop.DBConn.password", textBoxLocalPassword.getText());
		PropertyManagerNew.setProperty(PropertyType.Database, "desktop.DBConn.maxConnection", textBoxLocalMaxConn.getText());
		loadDataBase(null);
		return true;
	}

	private void loadDataBase(ActionEvent event) {
		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyType.Database);
			if (prop != null) {
				textBoxLocalHost.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.host"), ""));
				textBoxLocalPort.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.port"), ""));
				textBoxLocalDB.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.Database"), ""));
				textBoxLocalUsername.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.userName"), ""));
				textBoxLocalPassword.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.password"), ""));
				textBoxLocalMaxConn.setText(Misc.getParamAsString(prop.getProperty("desktop.DBConn.maxConnection"), ""));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void requestFocusNextField() {
		textBoxLocalHost.requestFocus();
	}

	@Override
	public void initController(SettingController parentSettingController) {
		this.parentSettingController = parentSettingController;
	}

	@Override
	public void init(MainController parent) {
	}

	@Override
	public void setTitle(String title) {
		screenTitle.setText(title);
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
	public void vehicleNameAction() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void dlNoAction() {
		// TODO Auto-generated method stub
		
	}


}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.constant.PropertyManagerNew.PropertyType;
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
public class BarrierWindowSettingController implements Initializable, ControllerI {

	@FXML
	private JFXTextField textBoxBarrierOneComm;
	@FXML
	private JFXTextField textBoxBarrierOneBaudRate;
	@FXML
	private JFXTextField textBoxBarrierOneParity;
	@FXML
	private JFXTextField textBoxBarrierOneDataBits;
	@FXML
	private JFXTextField textBoxBarrierOneStopBits;
	@FXML
	private JFXTextField textBoxBarrierOneEntryCommand;
	@FXML
	private JFXTextField textBoxBarrierOneExitCommand;

	@FXML
	private Label screenTitle;

	private MainController parent = null;
	private SettingController settingParent = null;

	/**
	 * Initializes the controller class.
	 *
	 * @param url
	 * @param rb
	 */
	@FXML
	private void onControlKeyPress(KeyEvent event) {
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		loadBarrierConfig();
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public boolean save() {
		return true;
	}

	@Override
	public void initController(SettingController settingParent) {
		this.settingParent = settingParent;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	@Override
	public void requestFocusNextField() {
		textBoxBarrierOneComm.requestFocus();
	}

	private void loadBarrierConfig() {
		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyType.Barrier);
			if (prop != null) {
				textBoxBarrierOneComm.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT"), "COM1"));// com1
				textBoxBarrierOneBaudRate
						.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_BAUDRATE"), "9600"));
				textBoxBarrierOneParity.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PARITY"), "0"));
				textBoxBarrierOneDataBits.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_DATABITS"), "8"));
				textBoxBarrierOneStopBits.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_STOPBITS"), "1"));
				textBoxBarrierOneEntryCommand
						.setText(Misc.getParamAsString(prop.getProperty("BARRIER_ENTRY_COMMAND"), "R"));
				textBoxBarrierOneExitCommand
						.setText(Misc.getParamAsString(prop.getProperty("BARRIER_EXIT_COMMAND"), "E"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	@Override
	public void stopSyncTprService() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
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

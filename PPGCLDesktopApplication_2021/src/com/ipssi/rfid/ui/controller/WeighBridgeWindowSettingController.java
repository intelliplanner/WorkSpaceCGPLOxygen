/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.constant.PropertyManagerNew.PropertyType;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI3
 */
public class WeighBridgeWindowSettingController implements Initializable, ControllerI {
	private static final Logger log = Logger.getLogger(WeighBridgeWindowSettingController.class.getName());
	private MainController parent = null;
	private SettingController settingParent = null;

	// @FXML
	// private JFXTextField textBoxWeighBridgePingInterval;
	// @FXML
	// private JFXTextField textBoxWeighBridgeDisconnectionMillis;
	@FXML
	private JFXTextField textBoxWeighBridgeStopBits;
	@FXML
	private JFXTextField textBoxWeighBridgeComm1;
	@FXML
	private JFXTextField textBoxWeighBridgeComm2;
	@FXML
	private JFXTextField textBoxWeighBridgeComm3;
	@FXML
	private JFXTextField textBoxWeighBridgeComm4;
	@FXML
	private JFXTextField textBoxWeighBridgeBaudRate;
	@FXML
	private JFXTextField textBoxWeighBridgeParity;
	@FXML
	private JFXTextField textBoxWeighBridgeDataBits;
	@FXML
	private JFXCheckBox checkBoxSimulateWB;

	@FXML
	private JFXTextField textBoxWeighBridgePingInterval;

	@FXML
	private Label screenTitle;

	@FXML
	private void onControlKeyPress(KeyEvent event) {
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		loadWeighBridge();
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
		textBoxWeighBridgeComm1.requestFocus();
	}

	private void saveWeighBridge() {

	}

	void loadWeighBridge() {
		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyType.WeighBridge);
			if (prop != null) {
				textBoxWeighBridgeComm1.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT"), ""));
				textBoxWeighBridgeComm2.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT_TWO"), ""));
				textBoxWeighBridgeComm3.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT_THREE"), ""));
				textBoxWeighBridgeComm4.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PORT_FOUR"), ""));
				textBoxWeighBridgeBaudRate.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_BAUDRATE"), ""));
				textBoxWeighBridgeParity.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_PARITY")));
				textBoxWeighBridgeDataBits.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_DATABITS"), ""));
				textBoxWeighBridgeStopBits.setText(Misc.getParamAsString(prop.getProperty("BARRIER_COM_STOPBITS"), ""));
				textBoxWeighBridgePingInterval.setText(Misc.getParamAsString(prop.getProperty("PING_INTERVAL"), ""));
				checkBoxSimulateWB.setSelected("1".equalsIgnoreCase(prop.getProperty("SIMULATE_WB")));
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
	public void clearInputs() {
	}

	@Override
	public void stopRfid() {
	}

	@Override
	public boolean save() {
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_PORT", textBoxWeighBridgeComm1.getText());// com1
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_PORT_TWO",
				textBoxWeighBridgeComm2.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_PORT_THREE",
				textBoxWeighBridgeComm3.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_PORT_FOUR",
				textBoxWeighBridgeComm4.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_BAUDRATE",
				textBoxWeighBridgeBaudRate.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_PARITY",
				textBoxWeighBridgeParity.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_DATABITS",
				textBoxWeighBridgeDataBits.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "BARRIER_COM_STOPBITS",
				textBoxWeighBridgeStopBits.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "SIMULATE_WB",
				checkBoxSimulateWB.isSelected() ? "1" : "0");
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "PING_INTERVAL",
				textBoxWeighBridgePingInterval.getText());
		loadWeighBridge();
		return true;
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
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub

	}

	// @Override
	// public void vehicleNameAction() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void dlNoAction() {
	// // TODO Auto-generated method stub
	//
	// }

}

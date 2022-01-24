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
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class GateInSettingController implements Initializable, ControllerI {
	@FXML
	private JFXComboBox<?> COMBO_MANAUL_ENTRY;
	@FXML
	private JFXComboBox<?> COMBO_GATE_PRINTER;
	@FXML
	private JFXCheckBox checkBoxCreateNewTripYes;
	@FXML
	private JFXTextField textBoxMinTokenGap;
	@FXML
	private JFXCheckBox checkBoxCloseTripYes;
	@FXML
	private JFXTextField textBoxPrevWorkStationType;
	@FXML
	private JFXTextField textBoxrefreshInterval;
	@FXML
	private JFXTextField textBoxSameStationTprThreshHold;
	@FXML
	private JFXTextField textBoxWorkStationType;
	@FXML
	private JFXTextField textBoxNextWorkStationType;

	@FXML
	private JFXTextField textBoxWorkStationId;

	@FXML
	private Label screenTitle;

	private SettingController settingControllerParent = null;
	private MainController parent = null;
	private static final Logger log = Logger.getLogger(GateInSettingController.class.getName());
	
	/**
	 * Initializes the controller class.
	 */
	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			String controlId = parent.getSourceId(event);
			handleActionControl(controlId);
		}
	}

	private void handleActionControl(String controlId) {
		if (controlId == null) {
			return;
		}

		controlId = controlId.toUpperCase();

		switch (controlId) {
		// case "checkBoxCreateNewTripYes":
		// textBoxMinTokenGap.requestFocus();
		// case "textBoxMinTokenGap":
		// checkBoxCloseTripYes.requestFocus();
		// case "checkBoxCloseTripYes":
		// textBoxPrevWorkStationType.requestFocus();
		// case "textBoxPrevWorkStationType":
		// textBoxrefreshInterval.requestFocus();
		// case "textBoxrefreshInterval":
		// textBoxSameStationTprThreshHold.requestFocus();
		// case "textBoxSameStationTprThreshHold":
		// textBoxWorkStationType.requestFocus();
		// case "textBoxWorkStationType":
		// textBoxNextWorkStationType.requestFocus();
		// case "textBoxNextWorkStationType":
		// textBoxWorkStationId.requestFocus();
		case "textBoxWorkStationId":
			settingControllerParent.CONTROL_SAVE.requestFocus();
		default:
			break;
		}

	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_MANAUL_ENTRY, LovDao.LovItemType.DATA_ENTRY_TYPE,
				Misc.getUndefInt(), Misc.getUndefInt());
		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_GATE_PRINTER, LovDao.LovItemType.PRINTER,
				Misc.getUndefInt(), Misc.getUndefInt());
		loadGateInProperties(null);

	}



	@Override
	public boolean save() {
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "CREATE_NEW_TRIP",
				checkBoxCreateNewTripYes.isSelected() ? "1" : "0");// 1
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "CLOSE_TRIP",
				checkBoxCloseTripYes.isSelected() ? "1" : "0");// 1
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "MIN_TOKEN_GAP",
				textBoxMinTokenGap.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "PREV_WORK_STATION_TYPE",
				textBoxPrevWorkStationType.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "REFRESH_INTERVAL",
				textBoxrefreshInterval.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "SAME_STATION_TPR_THRESHOLD",
				textBoxSameStationTprThreshHold.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "WORK_STATION_TYPE",
				textBoxWorkStationType.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "NEXT_WORK_STATION_TYPE",
				textBoxNextWorkStationType.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "WORK_STATION_ID",
				textBoxWorkStationId.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "FORCE_MANUAL",
				Integer.toString(LovUtils.getIntValue(COMBO_MANAUL_ENTRY)));
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.GateIn, "GATE_PASS_PRINTER_CONNECTED",
				Integer.toString(LovUtils.getIntValue(COMBO_GATE_PRINTER)));
		
		loadGateInProperties(null);
		return true;
	}

	private void loadGateInProperties(ActionEvent event) {
		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyManagerNew.PropertyType.GateIn);
			if (prop != null) {
				checkBoxCreateNewTripYes.setSelected("1".equalsIgnoreCase(prop.getProperty("CREATE_NEW_TRIP")));
				textBoxMinTokenGap.setText(Misc.getParamAsString(prop.getProperty("MIN_TOKEN_GAP"), ""));
				checkBoxCloseTripYes.setSelected("1".equalsIgnoreCase(prop.getProperty("CLOSE_TRIP")));
				textBoxPrevWorkStationType
						.setText(Misc.getParamAsString(prop.getProperty("PREV_WORK_STATION_TYPE"), ""));
				textBoxrefreshInterval.setText(Misc.getParamAsString(prop.getProperty("REFRESH_INTERVAL"), ""));
				textBoxSameStationTprThreshHold
						.setText(Misc.getParamAsString(prop.getProperty("SAME_STATION_TPR_THRESHOLD"), ""));
				textBoxWorkStationType.setText(Misc.getParamAsString(prop.getProperty("WORK_STATION_TYPE"), ""));
				textBoxNextWorkStationType
						.setText(Misc.getParamAsString(prop.getProperty("NEXT_WORK_STATION_TYPE"), ""));
				textBoxWorkStationId.setText(Misc.getParamAsString(prop.getProperty("WORK_STATION_ID"), ""));
				COMBO_MANAUL_ENTRY.getSelectionModel()
						.clearAndSelect(Misc.getParamAsInt(prop.getProperty("FORCE_MANUAL"), 0));
				COMBO_GATE_PRINTER.getSelectionModel().clearAndSelect(Misc.getParamAsInt(prop.getProperty("GATE_PASS_PRINTER_CONNECTED"), 0));
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
	}

	@Override
	public void stopSyncTprService() {
	}

//	@Override
//	public void vehicleNameAction() {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void dlNoAction() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void clearInputs() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void init(MainController parent) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void initController(SettingController settingParent) {
//		// TODO Auto-generated method stub
//		
//	}


	@Override
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public void requestFocusNextField() {
		checkBoxCreateNewTripYes.requestFocus();
	}

	@Override
	public void initController(SettingController settingControllerParent) {
		this.settingControllerParent = settingControllerParent;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

}

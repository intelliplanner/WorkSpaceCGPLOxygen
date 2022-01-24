/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
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
public class GrossWbSettingController implements Initializable, ControllerI {

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
	private JFXTextField textBoxMinWeight;
	@FXML
	private JFXTextField textBoxMaxWeight;
	@FXML
	private JFXTextField textBoxDigitizerZero;

	@FXML
	private JFXComboBox<?> COMBO_MANAUL_ENTRY;

	private MainController parent = null;
	private SettingController settingControllerParent = null;

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
		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_MANAUL_ENTRY, LovDao.LovItemType.DATA_ENTRY_TYPE,
				Misc.getUndefInt(), Misc.getUndefInt());
		loadWbGrossProperties(null);
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public void initController(SettingController settingControllerParent) {
		this.settingControllerParent = settingControllerParent;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	@Override
	public void requestFocusNextField() {
		// textBoxLocalHost.requestFocus();
		checkBoxCreateNewTripYes.requestFocus();
	}

	@Override
	public boolean save() {
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "CREATE_NEW_TRIP",
				checkBoxCreateNewTripYes.isSelected() ? "1" : "0");// 1
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "CLOSE_TRIP",
				checkBoxCloseTripYes.isSelected() ? "1" : "0");// 1
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "MIN_TOKEN_GAP",
				textBoxMinTokenGap.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "PREV_WORK_STATION_TYPE",
				textBoxPrevWorkStationType.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "REFRESH_INTERVAL",
				textBoxrefreshInterval.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "SAME_STATION_TPR_THRESHOLD",
				textBoxSameStationTprThreshHold.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "WORK_STATION_TYPE",
				textBoxWorkStationType.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "NEXT_WORK_STATION_TYPE",
				textBoxNextWorkStationType.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "WORK_STATION_ID",
				textBoxWorkStationId.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "MAX_WEIGHT",
				textBoxMaxWeight.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "MIN_WEIGHT",
				textBoxMinWeight.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "WEIGHT",
				textBoxDigitizerZero.getText());
		PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross, "FORCE_MANUAL",
				Integer.toString(LovUtils.getIntValue(COMBO_MANAUL_ENTRY)));
		// PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.WbGross,
		// "FORCE_MANUAL", textBoxManualEntry.getText());
		loadWbGrossProperties(null);
		return true;
	}

	private void loadWbGrossProperties(ActionEvent event) {
		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyManagerNew.PropertyType.WbGross);
			if (prop != null) {
				checkBoxCreateNewTripYes.setSelected("1".equalsIgnoreCase(prop.getProperty("CREATE_NEW_TRIP")));
				textBoxMinTokenGap.setText(Misc.getParamAsString(prop.getProperty("MIN_TOKEN_GAP"), ""));
				checkBoxCloseTripYes.setSelected("1".equalsIgnoreCase(prop.getProperty("CLOSE_TRIP")));
				textBoxrefreshInterval.setText(Misc.getParamAsString(prop.getProperty("REFRESH_INTERVAL"), ""));
				textBoxSameStationTprThreshHold
						.setText(Misc.getParamAsString(prop.getProperty("SAME_STATION_TPR_THRESHOLD"), ""));
				textBoxWorkStationId.setText(Misc.getParamAsString(prop.getProperty("WORK_STATION_ID"), ""));
				textBoxWorkStationType.setText(Misc.getParamAsString(prop.getProperty("WORK_STATION_TYPE"), ""));
				textBoxPrevWorkStationType
						.setText(Misc.getParamAsString(prop.getProperty("PREV_WORK_STATION_TYPE"), ""));
				textBoxNextWorkStationType
						.setText(Misc.getParamAsString(prop.getProperty("NEXT_WORK_STATION_TYPE"), ""));
				textBoxMaxWeight.setText(Misc.getParamAsString(prop.getProperty("MAX_WEIGHT"), ""));
				textBoxMinWeight.setText(Misc.getParamAsString(prop.getProperty("MIN_WEIGHT"), ""));
				textBoxDigitizerZero.setText(Misc.getParamAsString(prop.getProperty("WEIGHT"), ""));
				COMBO_MANAUL_ENTRY.getSelectionModel()
						.clearAndSelect(Misc.getParamAsInt(prop.getProperty("FORCE_MANUAL"), 0));
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

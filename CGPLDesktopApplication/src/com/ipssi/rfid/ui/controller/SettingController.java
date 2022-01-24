/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.constant.ScreenConstant;
import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class SettingController implements Initializable, ControllerI {

	@FXML
	private AnchorPane SETTING_ANCHOR_PANE;
	@FXML
	public BorderPane settingScreenMainForm;
	@FXML
	public HBox actionControlContainer;

	@FXML
	private JFXButton CONTROL_GATE_IN_SETTING;
	@FXML
	private JFXButton CONTROL_GATE_OUT_SETTING;
	@FXML
	private JFXButton CONTROL_WB_TARE_SETTING;
	@FXML
	private JFXButton CONTROL_WB_GROSS_SETTING;
	@FXML
	private JFXButton CONTROL_SYSTEM_CONFIG_SETTING;
	@FXML
	private JFXButton CONTROL_DATABASE_SETTING;
	@FXML
	private JFXButton CONTROL_RFID_CONFIG_SETTING;
	@FXML
	private JFXButton CONTROL_BARRIER_CONFIG_SETTING;
	@FXML
	private JFXButton CONTROL_WEIGHBRIDGE_CONFIG_SETTING;
//	@FXML
//	private JFXButton CONTROL_TAG_READ_CONFIG_SETTING;

	@FXML
	public JFXButton CONTROL_SAVE;
	@FXML
	public JFXButton CONTROL_RESET;

	private MainController parent = null;
	// private SettingController settingParent=null;
	private AnchorPane settingCenterView = null;
	// public static SettingControllerI currentSettingViewController = null;
	public static ControllerI currentViewController = null;
	public static ControllerI currentSettingViewController = null;
	private Button _selectedSettingControl = null;

	private static final Logger log = Logger.getLogger(SettingController.class.getName());
	/**
	 * Initializes the controller class.
	 */
	@FXML
	private void controlItemClicked(MouseEvent event) {
		Button control = ((Button) event.getSource());
		_selectedSettingControl = control;
		handleActionControl(control);

	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			Button control = ((Button) event.getSource());
			_selectedSettingControl = control;
			handleActionControl(control);
		}
	}

	@FXML
	private void onMouseClickedEvent(MouseEvent event) {
		Button control = ((Button) event.getSource());
		Stage stage = (Stage) control.getScene().getWindow();
		stage.close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		setScreenComponentVisible(false);
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public void stopRfid() {
	}

	@Override
	public boolean save() {
		boolean isTrue = this.currentSettingViewController.save();
		if (isTrue) {
			parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Detail Saved, Please Restart The Application. ");
		}
		return true;

	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
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

	public void loadScreen(String fxmlUrl, String menuId) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlUrl));
			this.settingCenterView = fxmlLoader.load();
			this.currentSettingViewController = fxmlLoader.getController();
			this.currentSettingViewController.initController(this);
			this.currentSettingViewController.init(parent);
			this.currentSettingViewController.setTitle(parent.menuScreenMap.get(menuId).getScreenTitle());
			settingScreenMainForm.setCenter(settingCenterView);
			this.currentSettingViewController.requestFocusNextField();
		} catch (IOException ex) {
			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void handleActionControl(Button control) {

		if (control == null || control.isDisable() || !control.isVisible()) {
			return;
		}
		String controlId = control.getId().toUpperCase();

		switch (controlId) {
		case "CONTROL_DATABASE_SETTING":
			databaseConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_GATE_IN_SETTING":
			gateInConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_GATE_OUT_SETTING":
			gateOutConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_WB_TARE_SETTING":
			wbTareConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_WB_GROSS_SETTING":
			wbGrossConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_SYSTEM_CONFIG_SETTING":
			systemConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_RFID_CONFIG_SETTING":
			RfidReaderConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_WEIGHBRIDGE_CONFIG_SETTING":
			weighBridgeConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
		case "CONTROL_BARRIER_CONFIG_SETTING":
			barrierConfigAction();
			setSettingControllsColor(control, "#ff4000");
			break;
//		case "CONTROL_TAG_READ_CONFIG_SETTING":
//			readTagConfigAction();
//			setSettingControllsColor(control, "#ff4000");
//			break;
		case "CONTROL_SAVE":
			save();
			break;
		case "CONTROL_RESET":
			CONTROL_GATE_IN_SETTING.requestFocus();
			break;
		default:
			break;
		}

	}

	private void systemConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.SYSTEM_CONFIG_SETTING_WINDOW,	ScreenConstant.MenuItemId.SYSTEM_CONFIG_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void wbGrossConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.WB_GROSS_SETTING_WINDOW, ScreenConstant.MenuItemId.WB_GROSS_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void wbTareConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.WB_TARE_SETTING_WINDOW, ScreenConstant.MenuItemId.WB_TARE_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void gateOutConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.GATE_OUT_SETTING_WINDOW,
				ScreenConstant.MenuItemId.GATE_OUT_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void databaseConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.DATABASE_SETTING_WINDOW,
				ScreenConstant.MenuItemId.DATABASE_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void gateInConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.GATE_IN_SETTING_WINDOW, ScreenConstant.MenuItemId.GATE_IN_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void RfidReaderConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.RFID_READER_SETTING_WINDOW,
				ScreenConstant.MenuItemId.RFID_READER_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void weighBridgeConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.WEIGHBRIDGE_SETTING_WINDOW,
				ScreenConstant.MenuItemId.WEIGHBRIDGE_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void barrierConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.BARRIER_SETTING_WINDOW, ScreenConstant.MenuItemId.BARRIER_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}
	private void readTagConfigAction() {
		loadScreen(ScreenConstant.ScreenLinks.TAG_READ_SETTING_WINDOW, ScreenConstant.MenuItemId.TAG_READ_SETTING_WINDOW);
		setScreenComponentVisible(true);
	}

	private void setScreenComponentVisible(boolean actionControlVisible) {
		actionControlContainer.setVisible(actionControlVisible);
	}

	private void setSettingControllsColor(Button control, String fontcolor) {
		CONTROL_GATE_OUT_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_WB_TARE_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_WB_GROSS_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_SYSTEM_CONFIG_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_DATABASE_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_GATE_IN_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_WEIGHBRIDGE_CONFIG_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_BARRIER_CONFIG_SETTING.setStyle("-fx-text-fill:#ffffff;");
		CONTROL_RFID_CONFIG_SETTING.setStyle("-fx-text-fill:#ffffff;");
		control.setStyle("-fx-text-fill:#0000CD;");
	}

	@Override
	public void initController(SettingController currentSettingViewController) {
		this.currentSettingViewController = currentSettingViewController;
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

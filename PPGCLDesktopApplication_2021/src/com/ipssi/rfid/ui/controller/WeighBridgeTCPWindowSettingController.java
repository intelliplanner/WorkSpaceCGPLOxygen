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
public class WeighBridgeTCPWindowSettingController implements Initializable, ControllerI {
	private static final Logger log = Logger.getLogger(WeighBridgeWindowSettingController.class.getName());
	private MainController parent = null;
	private SettingController settingParent = null;

	@FXML
	private JFXTextField textBoxWBOneHost;
	@FXML
	private JFXTextField textBoxWBTwoHost;
	@FXML
	private JFXTextField textBoxWBThreeHost;
	@FXML
	private JFXTextField textBoxWBFourHost;
	@FXML
	private JFXTextField textBoxWBOnePort;
	@FXML
	private JFXTextField textBoxWBTwoPort;
	@FXML
	private JFXTextField textBoxWBThreePort;
	@FXML
	private JFXTextField textBoxWBFourPort;
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
		textBoxWBOneHost.requestFocus();
	}

	private void saveWeighBridge() {

	}

	void loadWeighBridge() {
		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyType.WeighBridge);
			if (prop != null) {
				textBoxWBOnePort.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_ONE_PORT"), ""));
				textBoxWBTwoPort.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_TWO_PORT"), ""));
				textBoxWBThreePort.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_THREE_PORT"), ""));
				textBoxWBFourPort.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_FOUR_PORT"), ""));
				textBoxWBOneHost.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_ONE_HOST"), ""));
				textBoxWBTwoHost.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_TWO_HOST")));
				textBoxWBThreeHost.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_THREE_HOST"), ""));
				textBoxWBFourHost.setText(Misc.getParamAsString(prop.getProperty("WEIGHBRIDGE_FOUR_HOST"), ""));
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
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_ONE_PORT", textBoxWBOnePort.getText());// com1
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_TWO_PORT", textBoxWBTwoPort.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_THREE_PORT", textBoxWBThreePort.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_FOUR_PORT", textBoxWBFourPort.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_ONE_HOST", textBoxWBOneHost.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_TWO_HOST", textBoxWBTwoHost.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_THREE_HOST", textBoxWBThreeHost.getText());
		PropertyManagerNew.setProperty(PropertyType.WeighBridge, "WEIGHBRIDGE_FOUR_HOST", textBoxWBFourHost.getText());
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
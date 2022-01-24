/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.constant.PropertyManagerNew.PropertyType;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
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
public class RfidSettingController implements Initializable, ControllerI {

	@FXML
	private JFXCheckBox checkBoxReaderOnePresent;
	@FXML
	private JFXCheckBox checkBoxReaderTwoPresent;
	@FXML
	private JFXCheckBox checkBoxReaderThreePresent;
	@FXML
	private JFXCheckBox checkBoxReaderOneTcpIp;
	@FXML
	private JFXTextField textBoxReaderOneHost;
	@FXML
	private JFXTextField textBoxReaderOnePort;
	@FXML
	private JFXTextField textBoxReaderOneCom;
	@FXML
	private JFXCheckBox checkBoxReaderTwoTcpIp;
	@FXML
	private JFXTextField textBoxReaderTwoHost;
	@FXML
	private JFXTextField textBoxReaderTwoPort;
	@FXML
	private JFXTextField textBoxReaderTwoCom;
	@FXML
	private JFXCheckBox checkBoxReaderThreeTcpIp;
	@FXML
	private JFXTextField textBoxReaderThreeHost;
	@FXML
	private JFXTextField textBoxReaderThreePort;
	@FXML
	private JFXTextField textBoxReaderThreeCom;

	@FXML
	private Label screenTitleOne;
	@FXML
	private Label screenTitleTwo;
	@FXML
	private Label screenTitleThree;

	private SettingController settingParent = null;
	private MainController parent = null;

	@FXML
	private void onControlKeyPress(KeyEvent event) {
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
		loadRfidConfig();
	}

	@Override
	public void clearInputs() {

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
		checkBoxReaderOnePresent.requestFocus();
	}

	private void loadRfidConfig() {

		try {
			Properties prop = PropertyManagerNew.getProperty(PropertyManagerNew.PropertyType.RfidReader);
			if (prop != null) {

				textBoxReaderOneHost.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_IP"), ""));
				textBoxReaderOnePort.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_PORT"), ""));
				
				textBoxReaderTwoHost.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_IP"), ""));
				textBoxReaderTwoPort.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_PORT"), ""));
				
				textBoxReaderOneCom.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_COM"), ""));
				textBoxReaderTwoCom.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_COM"), ""));
				
				
				
				checkBoxReaderOneTcpIp.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_ONE_CONN_TYPE")));
				checkBoxReaderTwoTcpIp.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_TWO_CONN_TYPE")));
				
				textBoxReaderThreeCom.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_COM"), ""));
				

				checkBoxReaderOnePresent.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_ONE_PRESENT")));
				checkBoxReaderTwoPresent.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_TWO_PRESENT")));
				checkBoxReaderThreePresent.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_DESKTOP_PRESENT")));
				
				
//				checkBoxReaderThreeTcpIp.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_DESKTOP_CONN_TYPE")));
//				textBoxReaderThreeHost.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_IP"), ""));
//				textBoxReaderThreePort.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_PORT"), ""));


			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void setTitle(String title) {
		screenTitleOne.setText("READER ONE");
		screenTitleTwo.setText("READER TWO");
		screenTitleThree.setText("DESKTOP READER");
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
	public boolean save() {
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_ONE_TCP_IP", textBoxReaderOneHost.getText());
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_ONE_TCP_PORT", textBoxReaderOnePort.getText());
		
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_TWO_TCP_IP", textBoxReaderTwoHost.getText());
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_TWO_TCP_PORT", textBoxReaderTwoPort.getText());
		
		
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_ONE_COM", textBoxReaderOneCom.getText());
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_TWO_COM", textBoxReaderTwoCom.getText());
		
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_ONE_CONN_TYPE", checkBoxReaderOneTcpIp.isSelected() ? "1" : "0");
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_TWO_CONN_TYPE", checkBoxReaderTwoTcpIp.isSelected() ? "1" : "0");
		
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_DESKTOP_COM", textBoxReaderThreeCom.getText());
		
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_ONE_PRESENT", checkBoxReaderOnePresent.isSelected() ? "1" : "0");
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_TWO_PRESENT", checkBoxReaderTwoPresent.isSelected() ? "1" : "0");
		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_DESKTOP_PRESENT", checkBoxReaderThreePresent.isSelected() ? "1" : "0");
		
		
		
//		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_DESKTOP_CONN_TYPE", checkBoxReaderThreeTcpIp.isSelected() ? "1" : "0");
//		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_DESKTOP_TCP_IP", textBoxReaderThreeHost.getText());
//		PropertyManagerNew.setProperty(PropertyType.RfidReader, "READER_DESKTOP_TCP_PORT", textBoxReaderThreePort.getText());
		
		
		
//		checkBoxReaderOneTcpIp.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_ONE_CONN_TYPE")));
//		textBoxReaderOneHost.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_IP"), ""));
//		textBoxReaderOnePort.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_TCP_PORT"), ""));
//		textBoxReaderOneCom.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_COM"), ""));
//
//		checkBoxReaderTwoTcpIp.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_TWO_CONN_TYPE")));
//		textBoxReaderTwoHost.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_IP"), ""));
//		textBoxReaderTwoPort.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_TCP_PORT"), ""));
//		textBoxReaderTwoCom.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_COM"), ""));
//
//		checkBoxReaderThreeTcpIp.setSelected("1".equalsIgnoreCase(prop.getProperty("READER_DESKTOP_CONN_TYPE")));
//		textBoxReaderThreeHost.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_IP"), ""));
//		textBoxReaderThreePort.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_TCP_PORT"), ""));
//		textBoxReaderThreeCom.setText(Misc.getParamAsString(prop.getProperty("READER_DESKTOP_COM"), ""));
		
		loadRfidConfig();
		return true;
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

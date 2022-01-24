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
import java.util.logging.Logger;

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
public class SystemConfigurationSettingController implements Initializable, ControllerI {

    @FXML
    private JFXCheckBox CHECKBOX_BARRIER;
    @FXML
    private JFXTextField TEXTBOX_SERVER_IP;
    @FXML
    private JFXCheckBox CHECKBOX_DEBUG_MODE;
    @FXML
    private JFXTextField TEXTBOX_SERVER_PORT;
    @FXML
    private JFXTextField TEXTBOX_SOCKET_PORT;
    @FXML
    private JFXTextField TEXTBOX_RELAY_ONE;
    @FXML
    private JFXTextField TEXTBOX_RELAY_TWO;
    @FXML
    private JFXTextField TEXTBOX_LISTEN_ON;
    @FXML
    private JFXTextField TEXTBOX_REFRESH_INTERVAL;
    @FXML
    private JFXTextField TEXTBOX_SYSTEM_ID;
    @FXML
    private JFXTextField TEXTBOX_PULSE_TIME;
    @FXML
    private JFXTextField TEXTBOX_SAME_STATION_TPR_THRESHOLD;
    @FXML
    private JFXTextField TEXTBOX_APPLICATION_MODE;
    
    @FXML
    private JFXTextField TEXTBOX_PORT_NODE_ID;
    @FXML
    private Label screenTitle;
    
    @FXML
    private JFXTextField TEXTBOX_SAP_USERNAME;
    @FXML
    private JFXTextField TEXTBOX_SAP_PASSWORD;

    @FXML
    private JFXTextField TEXTBOX_SAP_HTTP_PORT;
    
    @FXML
    private JFXTextField TEXTBOX_SAP_HTTPS_PORT;
    
	@FXML
	private JFXComboBox<?> COMBO_AUTO_COMPLETE;

//	@FXML
//	private JFXComboBox<?> COMBO_PROJECT_AREA;
    
	@FXML
	private JFXComboBox<?> COMBO_TAG_READ_TYPE;
	
    private SettingController settingControllerParent = null;
    private MainController parent = null;

    private static final Logger log = Logger.getLogger(SystemConfigurationSettingController.class.getName());
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	  LovUtils.setLov(null, TokenManager.portNodeId, COMBO_AUTO_COMPLETE, LovDao.LovItemType.AUTO_COMPLETE_VEHICLE,
    			  Misc.getUndefInt(), Misc.getUndefInt());
      	LovUtils.setLov(null, TokenManager.portNodeId, COMBO_TAG_READ_TYPE, LovDao.LovItemType.DATA_ENTRY_TYPE,
      			 Misc.getUndefInt(), Misc.getUndefInt());
      	
        loadSystemConfigurationProperties(null);
    }

    @FXML
    private void onControlKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
            String controlId = parent.getSourceId(event);
            handleActionControl(controlId);
        }
    }

    @Override
    public void clearInputs() {

    }

    @Override
    public void stopRfid() {

    }

    private void loadSystemConfigurationProperties(Object object) {

        try {
            Properties prop = PropertyManagerNew.getProperty(PropertyManagerNew.PropertyType.Systems);
            if (prop != null) {
                CHECKBOX_BARRIER.setSelected("1".equalsIgnoreCase(prop.getProperty("IS_BARRIER")));
                CHECKBOX_DEBUG_MODE.setSelected("1".equalsIgnoreCase(prop.getProperty("DEBUG")));
                TEXTBOX_SOCKET_PORT.setText(Misc.getParamAsString(prop.getProperty("SOCKET_PORT"), ""));
                TEXTBOX_RELAY_ONE.setText(Misc.getParamAsString(prop.getProperty("READER_ONE_RELAY_ID"), ""));
                TEXTBOX_RELAY_TWO.setText(Misc.getParamAsString(prop.getProperty("READER_TWO_RELAY_ID"), ""));
                TEXTBOX_SERVER_IP.setText(Misc.getParamAsString(prop.getProperty("SERVER_IP"), ""));
                TEXTBOX_SERVER_PORT.setText(Misc.getParamAsString(prop.getProperty("SERVER_PORT"), ""));
                TEXTBOX_LISTEN_ON.setText(Misc.getParamAsString(prop.getProperty("LISTEN_ON"), ""));
                TEXTBOX_REFRESH_INTERVAL.setText(Misc.getParamAsString(prop.getProperty("REFRESH_INTERVAL"), ""));
                TEXTBOX_APPLICATION_MODE.setText(Misc.getParamAsString(prop.getProperty("APPLICATION_MODE"), ""));
                TEXTBOX_PULSE_TIME.setText(Misc.getParamAsString(prop.getProperty("PULSE_TIME"), ""));
                TEXTBOX_SYSTEM_ID.setText(Misc.getParamAsString(prop.getProperty("SYSTEM_ID"), ""));
                TEXTBOX_PORT_NODE_ID.setText(Misc.getParamAsString(prop.getProperty("PORT_NODE_ID"), ""));
                TEXTBOX_SAME_STATION_TPR_THRESHOLD.setText(Misc.getParamAsString(prop.getProperty("SAME_STATION_TPR_THRESHOLD"), ""));
                TEXTBOX_SAP_USERNAME.setText(Misc.getParamAsString(prop.getProperty("SAP_USERNAME"), "RFIDUSER"));
                TEXTBOX_SAP_PASSWORD.setText(Misc.getParamAsString(prop.getProperty("SAP_PASSWORD"), "Tata@123"));
                COMBO_AUTO_COMPLETE.getSelectionModel().clearAndSelect(Misc.getParamAsInt(prop.getProperty("AUTO_COMPLETE_ON_OFF"), 0));
                TEXTBOX_SAP_HTTP_PORT.setText(Misc.getParamAsString(prop.getProperty("SAP_HTTP_PORT"), "50000"));
                TEXTBOX_SAP_HTTPS_PORT.setText(Misc.getParamAsString(prop.getProperty("SAP_HTTPS_PORT"), "50001"));
                COMBO_TAG_READ_TYPE.getSelectionModel().clearAndSelect(Misc.getParamAsInt(prop.getProperty("TAG_READ_TYPE"), 0));
//                LovUtils.setLov(null, TokenManager.portNodeId, COMBO_AUTO_COMPLETE, LovDao.LovItemType.AUTO_COMPLETE_VEHICLE,
//                		Misc.getParamAsInt(prop.getProperty("AUTO_COMPLETE_VEHICLE")), Misc.getUndefInt());
//            	LovUtils.setLov(null, TokenManager.portNodeId, COMBO_TAG_READ_TYPE, LovDao.LovItemType.DATA_ENTRY_TYPE,
//            			Misc.getParamAsInt(prop.getProperty("DATA_ENTRY_TYPE")), Misc.getUndefInt());
            	
//                LovUtils.setLov(null, TokenManager.portNodeId, COMBO_PROJECT_AREA, LovDao.LovItemType.PROJECT_AREA,
//                		Misc.getParamAsInt(prop.getProperty("PROJECT_AREA")), Misc.getUndefInt());
//                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean save() {
        boolean isTrue = true;
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "IS_BARRIER", CHECKBOX_BARRIER.isSelected() ? "1" : "0");
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "DEBUG", CHECKBOX_DEBUG_MODE.isSelected() ? "1" : "0");
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SERVER_IP", TEXTBOX_SERVER_IP.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SERVER_PORT", TEXTBOX_SERVER_PORT.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SOCKET_PORT", TEXTBOX_SOCKET_PORT.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "PULSE_TIME", TEXTBOX_PULSE_TIME.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "READER_ONE_RELAY_ID", TEXTBOX_RELAY_ONE.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "READER_TWO_RELAY_ID", TEXTBOX_RELAY_TWO.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "LISTEN_ON", TEXTBOX_LISTEN_ON.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "REFRESH_INTERVAL", TEXTBOX_REFRESH_INTERVAL.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "APPLICATION_MODE", TEXTBOX_APPLICATION_MODE.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SYSTEM_ID", TEXTBOX_SYSTEM_ID.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SAME_STATION_TPR_THRESHOLD", TEXTBOX_SAME_STATION_TPR_THRESHOLD.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "PORT_NODE_ID", TEXTBOX_PORT_NODE_ID.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SAP_USERNAME", TEXTBOX_SAP_USERNAME.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SAP_PASSWORD", TEXTBOX_SAP_PASSWORD.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SAP_HTTP_PORT", TEXTBOX_SAP_HTTP_PORT.getText());
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "SAP_HTTPS_PORT", TEXTBOX_SAP_HTTPS_PORT.getText());
        
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "AUTO_COMPLETE_ON_OFF",Integer.toString(LovUtils.getIntValue(COMBO_AUTO_COMPLETE)));
        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "TAG_READ_TYPE",Integer.toString(LovUtils.getIntValue(COMBO_TAG_READ_TYPE)));
//        PropertyManagerNew.setProperty(PropertyManagerNew.PropertyType.Systems, "PROJECT_AREA",Integer.toString(LovUtils.getIntValue(COMBO_PROJECT_AREA)));
        loadSystemConfigurationProperties(null);
        return isTrue;
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
    public void requestFocusNextField() {
        CHECKBOX_BARRIER.requestFocus();
    }

    @Override
    public void setTitle(String title) {
        screenTitle.setText(title);
    }

    private void handleActionControl(String controlId) {
        if (controlId == null) {
            return;
        }

        controlId = controlId.toUpperCase();

        switch (controlId) {
            case "TEXTBOX_SAME_STATION_TPR_THRESHOLD":
                settingControllerParent.CONTROL_SAVE.requestFocus();
            default:
                break;
        }

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

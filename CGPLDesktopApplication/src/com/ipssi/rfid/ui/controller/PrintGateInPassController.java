/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.ipssi.rfid.beans.TprReportData;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.ui.dao.GateInDao;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class PrintGateInPassController   extends Application  implements Initializable,ControllerI {

    @FXML
    public AnchorPane PARENT_NODE;
    @FXML
    private Label TEXTBOX_VEHICLE_NAME;
    @FXML
    private Label TEXTBOX_DL_NUMBER;
    @FXML
    private Label DRIVER_NAME;
    @FXML
    private Label CUSTOMER;
    @FXML
    private Label TEXTBOX_SALES_ORDER;
    @FXML
    private Label TEXTBOX_PO_LINE;
    @FXML
    private Label TEXTBOX_TRANSPORTER;
    @FXML
    private Label TEXTBOX_NOTE;
    @FXML
    private Label LABEL_GATE_PASS;
	private MainController parent = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
	public void initData(String vehicleName,String dlNo , String driverName,String salesOrder,String poLine, String customer,String transporter, String notes) {
		TEXTBOX_VEHICLE_NAME.setText(vehicleName);
		TEXTBOX_DL_NUMBER.setText(dlNo);
		DRIVER_NAME.setText(driverName);
		TEXTBOX_SALES_ORDER.setText(salesOrder);
		TEXTBOX_PO_LINE.setText(poLine);
		CUSTOMER.setText(customer);
		TEXTBOX_TRANSPORTER.setText(transporter);
		TEXTBOX_NOTE.setText(notes);
	}

	@Override
	public void clearInputs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopRfid() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(MainController parent) {
		this.parent =parent;
	}

	@Override
	public void setVehicleName(String vehicleName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearVehicleName() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableController(Button controllerId, boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableManualEntry(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopSyncTprService() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initController(SettingController settingParent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFocusNextField() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		LABEL_GATE_PASS.setText(title);
	}

	@Override
	public void vehicleNameAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dlNoAction() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource(ScreenConstant.ScreenLinks.PRINT_GATE_PASS_WINDOW));
		Scene scene = new Scene(root,  588, 171);
		primaryStage.setScene(scene);
		primaryStage.show();
		GateInDao.pageSetup(root, primaryStage ,parent);
		primaryStage.close();
	}
    
}

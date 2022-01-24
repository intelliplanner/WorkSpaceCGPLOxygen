/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class Test implements Initializable {

    @FXML
    private Pane HEADER1;
    @FXML
    private JFXTextField TEXT_VEHICLE_NAME;
    @FXML
    private JFXTextField TEXT_TRANSPORTER;
    @FXML
    private JFXTextField TEXT_GRADE;
    @FXML
    private JFXTextField TEXT_MINES;
    @FXML
    private JFXTextField TEXT_SUPPLIER;
    @FXML
    private JFXButton CONTROLLER_SAVE;
    @FXML
    private Label JobStatus;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}

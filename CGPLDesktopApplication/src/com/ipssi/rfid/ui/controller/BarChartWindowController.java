/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.ipssi.cgplSap.SapIntegration;
import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class BarChartWindowController implements Initializable, ControllerI {

	@FXML
	DatePicker datePickerStartDate;
	@FXML
	DatePicker datePickerEndDate;
	@FXML
	BarChart barChart;
	
	@FXML
	private JFXButton CONTROL_SAP;
	
	private MainController parent = null;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		String Euro = "Euro";
		String Pound = "British Pound";
		String A_Dollar = "Austrelian Dollar";
		String frenc = "Swis Franc";
		String EuroA = "EuroA";
		String PoundA = "British PoundA";
		String A_DollarA = "Austrelian DollarA";
		String frencA = "Swis FrancA";
		// Configuring category and NumberAxis
		CategoryAxis xaxis = new CategoryAxis();
		NumberAxis yaxis = new NumberAxis(0.1, 2, 0.1);
		xaxis.setLabel("Currency");
		yaxis.setLabel("Dollar price");

		// Configuring BarChart
		// barChart.set
		barChart.setTitle("Dollar Conversion chart");

		// Configuring Series for XY chart
		XYChart.Series<String, Float> series = new XYChart.Series<>();
		series.getData().add(new XYChart.Data(Euro, 0.83));
		series.getData().add(new XYChart.Data(Pound, 0.73));
		series.getData().add(new XYChart.Data(frenc, 1.00));
		series.getData().add(new XYChart.Data(A_Dollar, 1.32));
		series.getData().add(new XYChart.Data(EuroA, 0.67));
		series.getData().add(new XYChart.Data(PoundA, 0.34));
		series.getData().add(new XYChart.Data(frencA, 1.24));
		series.getData().add(new XYChart.Data(A_DollarA, 1.87));
		// Adding series to the barchart
		barChart.getData().add(series);

	}
	
	@FXML
	public void SAP_Control() {
		SapIntegration.test();
	}

	@Override
	public void clearInputs() {
	}

	@Override
	public void stopRfid() {
	}

	@Override
	public boolean save() {
		
		
		
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

	@Override
	public void initController(SettingController parent) {
	}

	@Override
	public void requestFocusNextField() {
		datePickerStartDate.requestFocus();
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.beans.SalesOrderTable;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.CustomerDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.controller.service.ActionControllerI;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author vicky
 */
public class SalesOrderUpdateController implements Initializable, ControllerI, ActionControllerI {

	@FXML
	private JFXComboBox<ComboItem> COMBO_SALES_ORDER;
	@FXML
	private DatePicker DATE_SALES_ORDER_CREATED_DATE;
	@FXML
	private JFXTextField TEXTBOX_CUSTOMER_SAP_CODE;
	@FXML
	private JFXComboBox<ComboItem> COMBO_CUSTOMER_NAME;
	@FXML
	private JFXTextField TEXTBOX_CUSTOMER_ADDRESS;
	@FXML
	private JFXTextField TEXTBOX_SAP_LINE_ITEM;
	@FXML
	private JFXTextField TEXTBOX_SAP_MATERIAL;
	@FXML
	private JFXTextField TEXTBOX_SAP_ORDER_QUANTITY;
	
	@FXML
	private JFXTextField TEXTBOX_SAP_ORDER_REMAINING_QUANTITY;
	
	// @FXML
	// private JFXTextField TEXTBOX_SAP_ORDER_UNIT;
	@FXML
	private Label screenTitle;
	private SalesOrderTable salesOrderObj = null;
	private MainController parent;
	private SettingController settingParent;
	private static final Logger log = Logger.getLogger(SalesOrderUpdateController.class.getName());
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	DateFormat outFormat = new SimpleDateFormat("dd/MM/yyyy");

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// DATE_SALES_ORDER_CREATED_DATE.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		// setDateConverter(DATE_SALES_ORDER_CREATED_DATE);

		LovUtils.initializeComboBox(COMBO_SALES_ORDER, LovDao.LovItemType.SALES_ORDER_ALL, Misc.getUndefInt(),
				Misc.getUndefInt());
		// LovUtils.initializeComboBox(COMBO_SAP_LINE_ITEM,
		// LovDao.LovItemType.PO_LINE_ITEM, Misc.getUndefInt(),
		// Misc.getUndefInt());

		DATE_SALES_ORDER_CREATED_DATE.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		GateInDao.setDateConverter(DATE_SALES_ORDER_CREATED_DATE, dateFormatter);

		COMBO_SALES_ORDER.valueProperty().addListener(new ChangeListener<ComboItem>() {
			@Override
			public void changed(ObservableValue<? extends ComboItem> observable, ComboItem oldValue,
					ComboItem newValue) {
				searchSalesOrder();
			}
		});
		COMBO_CUSTOMER_NAME.valueProperty().addListener(new ChangeListener<ComboItem>() {
			@Override
			public void changed(ObservableValue<? extends ComboItem> observable, ComboItem oldValue,
					ComboItem newValue) {
				setCustomerDetails(LovUtils.getIntValue(COMBO_SALES_ORDER), TokenManager.portNodeId);
			}

		});
		
		TEXTBOX_SAP_ORDER_QUANTITY.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,7}([\\.]\\d{0,3})?")) {
					TEXTBOX_SAP_ORDER_QUANTITY.setText(oldValue);
//					TEXTBOX_SAP_ORDER_REMAINING_QUANTITY.setText(oldValue-);
                }
			}
		});

	}

	private void setCustomerDetails(int intValue, int portNodeId) {
		CustomerDetails customerDetails = GateInDao.getCustomerDetails(LovUtils.getIntValue(COMBO_CUSTOMER_NAME),
				TokenManager.portNodeId);
		if (customerDetails != null) {
			TEXTBOX_CUSTOMER_SAP_CODE.setText(customerDetails.getSapCode());
			TEXTBOX_CUSTOMER_ADDRESS.setText(customerDetails.getAddress());
			
		}
	}

	@FXML
	private void controlSaveClicked(ActionEvent event) {
		if (save()) {
			closeStage(event);
			// tprData.reportViewController.searchAction();
		}

	}

	private void closeStage(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void controlCloseClicked(ActionEvent event) {
		// Button control = ((Button) event.getSource());
		// handleActionControl(control);
		closeStage(event);
	}

	@Override
	public void handleActionControl(String controllId) {
		String controlId = controllId;
		switch (controlId.toUpperCase()) {
		// case "COMBO_CUSTOMER_NAME":
		// Pair<String,String> pairVal =
		// GateInDao.getCustomerDeatils(LovUtils.getIntValue(COMBO_CUSTOMER_NAME));
		// if(pairVal!=null) {
		// TEXTBOX_CUSTOMER_SAP_CODE.setText(pairVal.first);
		// TEXTBOX_CUSTOMER_ADDRESS.setText(pairVal.second);
		// }
		// break;

		case "TEXT_NOTE":
			parent.CONTROL_SAVE.requestFocus();
			break;
		default:
			break;
		}

	}

	@Override
	public void onControlAction(ActionEvent event) {
		// String controllId = parent.getSourceId(event);
		// handleActionControlOnChange(controllId);
	}

	private void handleActionControlOnChange(String controllId) {
		if (controllId == null || controllId == "" || controllId.length() == 0) {
			return;
		}
		switch (controllId.toUpperCase()) {
		// case "COMBO_CUSTOMER_NAME":
		// Pair<String,String> pairVal =
		// GateInDao.getCustomerDeatils(LovUtils.getIntValue(COMBO_CUSTOMER_NAME));
		// if(pairVal!=null) {
		// TEXTBOX_CUSTOMER_SAP_CODE.setText(pairVal.first);
		// TEXTBOX_CUSTOMER_ADDRESS.setText(pairVal.second);
		// }
		// break;

		default:
			break;
		}

	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		// if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
		// String controllId = parent.getSourceId(event);
		// handleActionControl(controllId);
		// }
	}

	private void setDateConverter(DatePicker datePickerInstance) {
		datePickerInstance.setConverter(new javafx.util.StringConverter<LocalDate>() {

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return dateFormatter.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, dateFormatter);
				} else {
					return null;
				}
			}
		});
	}

	@Override
	public void clearInputs() {

	}

	@Override
	public void stopRfid() {

	}

	@Override
	public boolean save() {
		System.out.println("######### Start Sales Order In save() ######### ");

		Connection conn = null;
		boolean destroyIt = false;
		try {
			if (LovUtils.getIntValue(COMBO_SALES_ORDER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select 'SALES_ORDER'.");
				COMBO_SALES_ORDER.requestFocus();
				return false;
			} else if (Utils.isNull(DATE_SALES_ORDER_CREATED_DATE.getValue().toString())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select 'SALES_ORDER_CREATED_DATE'.");
				return false;
			} else if (LovUtils.getIntValue(COMBO_CUSTOMER_NAME) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Customer Name'.");
				COMBO_CUSTOMER_NAME.requestFocus();
				return false;
			} else if (Utils.isNull(TEXTBOX_SAP_MATERIAL.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Enter 'SAP_MATERIAL'.");
				TEXTBOX_SAP_MATERIAL.requestFocus();
				return false;
			} else if (Utils.isNull(TEXTBOX_SAP_ORDER_QUANTITY.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Enter 'SAP_ORDER_QUANTITY'.");
				TEXTBOX_SAP_ORDER_QUANTITY.requestFocus();
				return false;
			} else {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				updateSalesOrder(conn);
				conn.commit();
				clearInputs(true);
			}
		} catch (Exception ex) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", UIConstant.SAVE_FAILER_MESSAGE + ", " + ex.getMessage());
			log.getLogger(SalesOrderUpdateController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
			return false;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				log.getLogger(SalesOrderUpdateController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("######### End Sales Order save() ######### ");

		return true;

	}

	private void updateSalesOrder(Connection conn) throws Exception {
		if (salesOrderObj == null)
			salesOrderObj = new SalesOrderTable();

		salesOrderObj.setCustomer_id(LovUtils.getIntValue(COMBO_CUSTOMER_NAME));
		salesOrderObj.setSap_customer_address(TEXTBOX_CUSTOMER_ADDRESS.getText());
		salesOrderObj.setSap_customer_name(LovUtils.getTextValue(COMBO_CUSTOMER_NAME));
		salesOrderObj.setSap_customer_sap_code(TEXTBOX_CUSTOMER_SAP_CODE.getText());

		salesOrderObj.setSap_material(TEXTBOX_SAP_MATERIAL.getText());
		salesOrderObj.setSap_order_quantity(TEXTBOX_SAP_ORDER_QUANTITY.getText());
		Date salesOrderCreateDate = DATE_SALES_ORDER_CREATED_DATE.getValue().toString() != null
				? new SimpleDateFormat("yyyy-MM-dd").parse(DATE_SALES_ORDER_CREATED_DATE.getValue().toString())
				: null;
		salesOrderObj.setSap_sales_order_creation_date(salesOrderCreateDate);
		salesOrderObj.setSap_order_unit("To");
		salesOrderObj.setUpdated_on(new Date());
		salesOrderObj.setPort_node_id(TokenManager.portNodeId);
		salesOrderObj.setSTATUS(1);
		salesOrderObj.setSap_sale_order_status(1);
		RFIDMasterDao.update(conn, salesOrderObj, false);
	}

	private void searchSalesOrder() {
		salesOrderObj = GateInDao.getSalesOrder(LovUtils.getIntValue(COMBO_SALES_ORDER), TokenManager.portNodeId);
		if (salesOrderObj != null) {
			LovUtils.initializeComboBox(COMBO_CUSTOMER_NAME, LovDao.LovItemType.CUSTOMER,
					salesOrderObj.getCustomer_id(), Misc.getUndefInt());

			TEXTBOX_SAP_LINE_ITEM.setText("" + salesOrderObj.getSap_line_item());
			TEXTBOX_SAP_MATERIAL
					.setText(salesOrderObj.getSap_material() == null ? "" : salesOrderObj.getSap_material());
			TEXTBOX_SAP_ORDER_QUANTITY.setText(salesOrderObj.getSap_order_quantity() == null ? ""
					: salesOrderObj.getSap_order_quantity() + "");
			TEXTBOX_CUSTOMER_SAP_CODE.setText(
					salesOrderObj.getSap_customer_sap_code() == null ? "" : salesOrderObj.getSap_customer_sap_code());
			TEXTBOX_CUSTOMER_ADDRESS.setText(
					salesOrderObj.getSap_customer_address() == null ? "" : salesOrderObj.getSap_customer_address());
			if (salesOrderObj.getSap_sales_order_creation_date() != null) {
				String date = UIConstant.slipFormat.format(salesOrderObj.getSap_sales_order_creation_date().getTime());
				DATE_SALES_ORDER_CREATED_DATE.setValue(LocalDate.parse(date, dateFormatter));
			} else {
				DATE_SALES_ORDER_CREATED_DATE.setValue(null);
			}
			TEXTBOX_SAP_ORDER_REMAINING_QUANTITY.setText("");
			if(salesOrderObj.getSap_order_quantity() != null && salesOrderObj.getSapOrderLapseQuantity() != null) {
				Double s = Double.parseDouble(salesOrderObj.getSap_order_quantity()) - Double.parseDouble(salesOrderObj.getSapOrderLapseQuantity());
						TEXTBOX_SAP_ORDER_REMAINING_QUANTITY.setText("Remaining Quantity: "+s.toString());
			}else if(salesOrderObj.getSap_order_quantity() != null  && salesOrderObj.getSapOrderLapseQuantity() == null ) {
				TEXTBOX_SAP_ORDER_REMAINING_QUANTITY.setText("Remaining Quantity: "+ salesOrderObj.getSap_order_quantity());
			}
		}
	}

	private void clearInputs(boolean clearToken) {
		COMBO_SALES_ORDER.getSelectionModel().clearAndSelect(0);
		DATE_SALES_ORDER_CREATED_DATE.setValue(null);
		TEXTBOX_CUSTOMER_SAP_CODE.setText("");
		COMBO_CUSTOMER_NAME.getSelectionModel().clearAndSelect(0);
		TEXTBOX_CUSTOMER_ADDRESS.setText("");
		TEXTBOX_SAP_LINE_ITEM.setText("");
		;
		TEXTBOX_SAP_MATERIAL.setText("");
		TEXTBOX_SAP_ORDER_QUANTITY.setText("");
		// TEXTBOX_SAP_ORDER_UNIT.setText("");
		salesOrderObj = null;
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
	public void initController(SettingController settingParent) {
		this.settingParent=settingParent;
	}

	@Override
	public void requestFocusNextField() {

	}

	@Override
	public void setTitle(String title) {
		screenTitle.setText(title);
	}

	@Override
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {

	}

	@Override
	public void vehicleAction() {
		// TODO Auto-generated method stub

	}

	public void openDialogWindow() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource(ScreenConstant.ScreenLinks.SALES_ORDER_UPDATE_WINDOW));
		Parent parent;
		try {
			parent = fxmlLoader.load();
			SalesOrderCancellationFormController dialogController = fxmlLoader
					.<SalesOrderCancellationFormController>getController();
			// dialogController.initData(tprData);
			dialogController.setTitle(ScreenConstant.ScreenTitle.SALES_ORDER_UPDATE_WINDOW);
			Scene scene = new Scene(parent, 480, 380);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);
			stage.setTitle(ScreenConstant.ScreenTitle.SALES_ORDER_UPDATE_WINDOW);
			stage.setResizable(false);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(SalesOrderUpdateController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

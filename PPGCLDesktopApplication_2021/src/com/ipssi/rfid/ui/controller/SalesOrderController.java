/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.beans.SalesOrderTable;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.TprReportData;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.constant.ScreenConstant;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.autocompleteText.AutoCompleteTextFieldNew;
import com.ipssi.rfid.ui.controller.service.ActionControllerI;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author vicky
 */
public class SalesOrderController implements Initializable, ControllerI, ActionControllerI {
	@FXML
	private JFXButton BUTTON_UPDATE_SALES_ORDER;
	@FXML
	private JFXTextField TEXTBOX_SALES_ORDER;
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
	// @FXML
	// private JFXTextField TEXTBOX_SAP_ORDER_UNIT;
	@FXML
	private Label screenTitle;
	private SalesOrderTable salesOrderObj = null;
	private MainController parent;
	private static final Logger log = Logger.getLogger(SalesOrderController.class.getName());
//	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Misc.G_DEFAULT_DATE_FORMAT);
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	SettingController settingParent = null;
	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		LovUtils.initializeComboBox(COMBO_CUSTOMER_NAME, LovDao.LovItemType.CUSTOMER, Misc.getUndefInt(),
				Misc.getUndefInt());
//		LovUtils.initializeComboBox(COMBO_SAP_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM, Misc.getUndefInt(),
//				Misc.getUndefInt(),"");
		
		AutoCompleteTextFieldNew obj = new AutoCompleteTextFieldNew(TEXTBOX_SALES_ORDER, LovDao.LovItemType.PO_SALES_ORDER);
		
		DATE_SALES_ORDER_CREATED_DATE.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		GateInDao.setDateConverter(DATE_SALES_ORDER_CREATED_DATE,dateFormatter);

		BUTTON_UPDATE_SALES_ORDER.setOnAction(e -> {
				openDialogWindow(this);
			});
		
		TEXTBOX_SALES_ORDER.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					TEXTBOX_SALES_ORDER.setText(newValue.replaceAll("[^\\d]", ""));
		        }
			}
		});

		TEXTBOX_SAP_LINE_ITEM.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					TEXTBOX_SAP_LINE_ITEM.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		TEXTBOX_SAP_ORDER_QUANTITY.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,7}([\\.]\\d{0,3})?")) {
					TEXTBOX_SAP_ORDER_QUANTITY.setText(oldValue);
                }
			}
		});
		
		COMBO_CUSTOMER_NAME.valueProperty().addListener(new ChangeListener<ComboItem>() {

			@Override
			public void changed(ObservableValue<? extends ComboItem> observable, ComboItem oldValue,
					ComboItem newValue) {
				Pair<String, String> pairVal = GateInDao.getCustomerDeatils(LovUtils.getIntValue(COMBO_CUSTOMER_NAME));
				if (pairVal != null) {
					TEXTBOX_CUSTOMER_SAP_CODE.setText(pairVal.first);
					TEXTBOX_CUSTOMER_ADDRESS.setText(pairVal.second);
				}
			}
		});


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



	@Override
	public void clearInputs() {
		clearInputs(null, false);
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
			if (Utils.isNull(TEXTBOX_SALES_ORDER.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter 'SALES_ORDER'.");
				TEXTBOX_SALES_ORDER.requestFocus();
				return false;
			} else if (Utils.isNull(DATE_SALES_ORDER_CREATED_DATE.getValue().toString())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Search by 'SALES_ORDER_CREATED_DATE'.");
				return false;
			} else if (Utils.isNull(TEXTBOX_CUSTOMER_SAP_CODE.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Search by 'CUSTOMER_SAP_CODE'.");
				TEXTBOX_CUSTOMER_SAP_CODE.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_CUSTOMER_NAME) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Customer'.");
				COMBO_CUSTOMER_NAME.requestFocus();
				return false;
			} else if (Utils.isNull(TEXTBOX_CUSTOMER_ADDRESS.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select by 'CUSTOMER_ADDRESS'.");
				TEXTBOX_CUSTOMER_ADDRESS.requestFocus();
				return false;
			}
			else if (Utils.isNull(TEXTBOX_SAP_LINE_ITEM.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select by 'TEXTBOX_SAP_LINE_ITEM'.");
				TEXTBOX_SAP_LINE_ITEM.requestFocus();
				return false;
			} 
			else if (Utils.isNull(TEXTBOX_SAP_MATERIAL.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select by 'SAP_MATERIAL'.");
				TEXTBOX_SAP_MATERIAL.requestFocus();
				return false;
			} else if (Utils.isNull(TEXTBOX_SAP_ORDER_QUANTITY.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select by 'SAP_ORDER_QUANTITY'.");
				TEXTBOX_SAP_ORDER_QUANTITY.requestFocus();
				return false;
			}
			// else if (Utils.isNull(TEXTBOX_SAP_ORDER_UNIT.getText())) {
			// parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Search by
			// 'SAP_ORDER_UNIT'.");
			// TEXTBOX_SAP_ORDER_UNIT.requestFocus();
			// return false;
			// }
			else {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				updateSalesOrder(conn);
				conn.commit();
				clearInputs(conn, true);
			}
		} catch (Exception ex) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", UIConstant.SAVE_FAILER_MESSAGE + ", " + ex.getMessage());
			log.getLogger(SalesOrderCancellationFormController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
			return false;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				log.getLogger(SalesOrderCancellationFormController.class.getName()).log(Level.SEVERE, null, ex);
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

		salesOrderObj.setSap_line_item(Integer.parseInt(TEXTBOX_SAP_LINE_ITEM.getText()));
		salesOrderObj.setSap_material(TEXTBOX_SAP_MATERIAL.getText());
		salesOrderObj.setSap_order_quantity(TEXTBOX_SAP_ORDER_QUANTITY.getText());
		
		Date salesOrderCreateDate = DATE_SALES_ORDER_CREATED_DATE.getValue().toString() != null
				? new SimpleDateFormat("yyyy-MM-dd").parse(DATE_SALES_ORDER_CREATED_DATE.getValue().toString())
				: null;
		salesOrderObj.setSap_sales_order_creation_date(salesOrderCreateDate);
		salesOrderObj.setSap_order_unit("To");
		
		String salesOrder = TEXTBOX_SALES_ORDER.getText();
		if(salesOrder.contains("@"))
			salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
		
		salesOrderObj.setSap_sales_order(salesOrder);
		salesOrderObj.setUpdated_on(new Date());
		salesOrderObj.setPort_node_id(TokenManager.portNodeId);
		salesOrderObj.setSTATUS(1);
		salesOrderObj.setSap_sale_order_status(1);
		salesOrderObj.setCreatedBy(1);
		salesOrderObj.setUpdatedBy(1);
		salesOrderObj.setCreated_on(new Date());
		RFIDMasterDao.insert(conn, salesOrderObj, false);
	}

	
	private void clearInputs(Connection conn, boolean clearToken) {
		TEXTBOX_SALES_ORDER.setText("");
		DATE_SALES_ORDER_CREATED_DATE.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		TEXTBOX_CUSTOMER_SAP_CODE.setText("");
		COMBO_CUSTOMER_NAME.getSelectionModel().clearAndSelect(0);
		TEXTBOX_CUSTOMER_ADDRESS.setText("");
		TEXTBOX_SAP_LINE_ITEM.setText("");
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
	
	public  void openDialogWindow(SalesOrderController salesOrderController) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.SALES_ORDER_UPDATE_WINDOW));
		Parent currentWindow;
		try {
			currentWindow = fxmlLoader.load();
			SalesOrderUpdateController dialogController = fxmlLoader.<SalesOrderUpdateController>getController();
//		    dialogController.initData(tprData);
			dialogController.init(parent);
		    dialogController.setTitle(ScreenConstant.ScreenTitle.SALES_ORDER_UPDATE_WINDOW);
			Scene scene = new Scene(currentWindow);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);
			stage.setTitle(ScreenConstant.ScreenTitle.SALES_ORDER_UPDATE_WINDOW);
			stage.setResizable(false);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(TprReportData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}

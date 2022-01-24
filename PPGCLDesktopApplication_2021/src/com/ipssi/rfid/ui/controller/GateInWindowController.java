/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.controlsfx.control.textfield.TextFields;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.processor.utils.VehicleWithName;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.RFIDDataProcessor;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.autocompleteText.AutoCompleteTextField;
import com.ipssi.rfid.ui.autocompleteText.AutoCompleteTextFieldNew;
import com.ipssi.rfid.ui.controller.service.ActionControllerI;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.dao.IssueTag;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author Virendra Gupta
 */
public class GateInWindowController implements Initializable, ControllerI, ActionControllerI {
	@FXML
	private AnchorPane GATE_IN_ROOT_ANCHORPANE;
	@FXML
	private HBox GATE_IN_HBOX;

	@FXML
	public JFXTextField TEXT_VEHICLE_NAME;
	@FXML
	private JFXTextField TEXT_NOTE;
	@FXML
	private JFXComboBox<ComboItem> COMBO_TRANSPORTER;
	@FXML
	private JFXComboBox<ComboItem> COMBO_CUSTOMER;
	@FXML
	private JFXComboBox<ComboItem> COMBO_PO_SALES_ORDER;
	@FXML
	private JFXComboBox<ComboItem> COMBO_LINE_ITEM;

	@FXML
	private DatePicker DATE_LR;

	@FXML
	private JFXTextField TEXT_LR_NO;

	private int m_vehicleId = Misc.getUndefInt();
	private TPStep tpStep = null;
	private Date entryTime = null;
	private TPRecord tpRecord = null;
	private boolean isEnterPressed = false;
	private int isNewVehicle = Misc.getUndefInt();
	private MainController parent;
	private Vehicle vehBean = null;
	private int isTagIssue = Misc.getUndefInt();

	private RFIDDataHandler rfidHandler = null;
	StringBuilder blockingMsg = null;
	private TPRBlockManager tprBlockManager = null;

	boolean isLastTprBlocked = false;
	private Token token = null;
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
	DateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final Logger log = Logger.getLogger(GateInWindowController.class.getName());
	ArrayList<String> suggesstionList = null;

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		TEXT_VEHICLE_NAME.requestFocus();
		// parent.labelBlockingReason.setText("");
		DATE_LR.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		GateInDao.setDateConverter(DATE_LR, dateFormatter);
		LovUtils.initializeComboBox(COMBO_CUSTOMER, LovDao.LovItemType.CUSTOMER, Misc.getUndefInt(),
				Misc.getUndefInt());
		LovUtils.initializeComboBox(COMBO_TRANSPORTER, LovDao.LovItemType.TRANSPORTER, Misc.getUndefInt(),
				Misc.getUndefInt());
		// syncTpr();

		// if (TokenManager.IS_AUTO_COMPLETE_ON) {
		// System.out.println("Gate In vehilce Auto Complete On");
		// AutoCompleteTextField autoCompleteTextField = new
		// AutoCompleteTextField(this.parent, TEXT_VEHICLE_NAME,
		// LovDao.LovItemType.VEHICLE);
		// autoCompleteTextField.setAutoCompleteTextBox();
		//
		// } else {
		// System.out.println("Gate In vehilce Auto Complete Off");
		// }

		AutoCompleteTextFieldNew obj = new AutoCompleteTextFieldNew(TEXT_VEHICLE_NAME, LovDao.LovItemType.VEHICLE);

		TEXT_VEHICLE_NAME.setOnKeyPressed(event -> {
			isEnterPressed = false;
			if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
				if (!Utils.isNull(TEXT_VEHICLE_NAME.getText()) && TEXT_VEHICLE_NAME.getText().trim().length() > 4)
					vehicleAction();
			}
			if (event.getCode() == KeyCode.ENTER) {
				COMBO_CUSTOMER.requestFocus();
			}
		});

		TEXT_VEHICLE_NAME.setOnMousePressed(event -> {
			isEnterPressed = false;
			if (!Utils.isNull(TEXT_VEHICLE_NAME.getText()) && TEXT_VEHICLE_NAME.getText().trim().length() > 4) {
				vehicleAction();
			}
		});

		// new in ppgcl
		COMBO_CUSTOMER.valueProperty().addListener(new ChangeListener<ComboItem>() {
			@Override
			public void changed(ObservableValue<? extends ComboItem> observable, ComboItem oldValue,
					ComboItem newValue) {
				LovUtils.initializeComboBox(COMBO_PO_SALES_ORDER, LovDao.LovItemType.PO_SALES_ORDER, Misc.getUndefInt(),
						LovUtils.getIntValue(COMBO_CUSTOMER), null);
				// vehicleAction();
			}
		});

		COMBO_PO_SALES_ORDER.valueProperty().addListener(new ChangeListener<ComboItem>() {
			@Override
			public void changed(ObservableValue<? extends ComboItem> observable, ComboItem oldValue,
					ComboItem newValue) {
				String salesOrder = LovUtils.getTextValue(COMBO_PO_SALES_ORDER);
				if (salesOrder.contains("@"))
					salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
				LovUtils.initializeComboBox(COMBO_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM,
						LovUtils.getIntValue(COMBO_PO_SALES_ORDER), Misc.getUndefInt(), salesOrder);
			}
		});

	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
			String controllId = parent.getSourceId(event);
			handleActionControl(controllId);
		}
	}

	@FXML
	private void onKeyReleased(KeyEvent event) {
		String controllId = parent.getSourceId(event);
		handleActionControl(controllId);
	}

	@Override
	public void handleActionControl(String control) {
		String controlId = control;
		switch (controlId.toUpperCase()) {
		case "TEXT_VEHICLE_NAME":
			// vehicleAction();
			break;
		case "COMBO_CUSTOMER":
			COMBO_PO_SALES_ORDER.requestFocus();
			break;
		case "COMBO_PO_SALES_ORDER":
			COMBO_LINE_ITEM.requestFocus();
			break;
		case "COMBO_LINE_ITEM":
			COMBO_TRANSPORTER.requestFocus();
			break;
		case "COMBO_TRANSPORTER":
			TEXT_LR_NO.requestFocus();
			break;
		case "TEXT_LR_NO":
			DATE_LR.requestFocus();
			break;
		case "DATE_LR":
			TEXT_NOTE.requestFocus();
			break;
		case "TEXT_NOTE":
			parent.CONTROL_SAVE.requestFocus();
			break;
		default:
			break;
		}
	}

	@Override
	public void onControlAction(ActionEvent event) {
		String controllId = parent.getSourceId(event);
		handleActionControlOnChange(controllId);
	}

	private void handleActionControlOnChange(String controllId) {
		if (controllId == null || controllId == "" || controllId.length() == 0) {
			return;
		}
		switch (controllId.toUpperCase()) {
		// case "COMBO_CUSTOMER":
		// LovUtils.initializeComboBox(COMBO_PO_SALES_ORDER,
		// LovDao.LovItemType.PO_SALES_ORDER, Misc.getUndefInt(),
		// LovUtils.getIntValue(COMBO_CUSTOMER), null);
		// break;
		// case "COMBO_PO_SALES_ORDER":
		// LovUtils.initializeComboBox(COMBO_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM,
		// Misc.getUndefInt(),
		// Misc.getUndefInt(), LovUtils.getTextValue(COMBO_PO_SALES_ORDER));
		// break;
		case "CONTROL_PRINT":
			// printScreen(GATE_IN_HBOX);
			// openDialogWindow();
			break;
		default:
			break;
		}
	}

	@Override
	public void clearInputs() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			clearInputs(conn, true);
		} catch (Exception ex) {
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void clearInputs(Connection conn, boolean clearToken) {
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		clearVehicleName();
		// parent.setControllerDisable(false);
		isNewVehicle = Misc.getUndefInt();
		m_vehicleId = Misc.getUndefInt();
		token = null;
		tpStep = null;
		entryTime = null;
		tpRecord = null;
		isEnterPressed = false;
		vehBean = null;
		isTagIssue = Misc.getUndefInt();
		// rfidHandler = null;
		TEXT_NOTE.setText("");
		// COMBO_LINE_ITEM.getSelectionModel().clearAndSelect(-1);
		// COMBO_PO_SALES_ORDER.getSelectionModel().clearAndSelect(-1);
		COMBO_CUSTOMER.getSelectionModel().clearAndSelect(-1);
		COMBO_TRANSPORTER.getSelectionModel().clearAndSelect(0);
		// DATE_LR.setValue(null);
		DATE_LR.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		TEXT_LR_NO.setText("");
		// TEXT_RUNNING_PROCESS.setText("");

		// blockingMsg = null;
		isLastTprBlocked = false;

	}

	@Override
	public void stopRfid() {
		try {
			if (rfidHandler != null) {
				rfidHandler.stop();
			}

		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public boolean save() {
		System.out.println("######### Start Gate In save() ######### ");

		Connection conn = null;
		boolean destroyIt = false;
		try {
			if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if (!isEnterPressed) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Search by 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if (m_vehicleId == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_CUSTOMER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Customer'.");
				COMBO_CUSTOMER.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_PO_SALES_ORDER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Sales Order'.");
				COMBO_PO_SALES_ORDER.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_LINE_ITEM) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Line Item'.");
				COMBO_LINE_ITEM.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_TRANSPORTER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Transporter'.");
				COMBO_TRANSPORTER.requestFocus();
				return false;
			} else if (Utils.isNull(TEXT_LR_NO.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'LR NO'.");
				TEXT_LR_NO.requestFocus();
				return false;
			} else if (DATE_LR.getValue() == null || DATE_LR.getValue() == null) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select LR-Date");
				DATE_LR.requestFocus();
				return false;
			} else if (Utils.isNull(TEXT_NOTE.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Note'.");
				TEXT_NOTE.requestFocus();
				return false;
			} else {

				String[] options = { "NO", "YES" };
				int res = parent.prompt(Alert.AlertType.CONFIRMATION.toString(),
						"Please Confirm Vehicle Number is Correct ?." + this.TEXT_VEHICLE_NAME.getText(), options);
				if (res == 0) {
					clearVehicleName();
					this.TEXT_VEHICLE_NAME.requestFocus();
					return false;
				}

				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				String salesOrder = LovUtils.getTextValue(COMBO_PO_SALES_ORDER);
				if (salesOrder.contains("@"))
					salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
				double qty = GateInDao.getRemainingQuantity(conn, salesOrder, LovUtils.getTextValue(COMBO_LINE_ITEM));
				if (qty < TokenManager.remaining_quatitity_limit) {
					parent.showAlert(Alert.AlertType.ERROR, "Message", "Sales Order Exhausted");
					// COMBO_LINE_ITEM.getSelectionModel().clearAndSelect(0);
					COMBO_LINE_ITEM.requestFocus();
					return false;
				}

				if (m_vehicleId == Misc.getUndefInt()) {
					Pair<Integer, String> vehPair = TPRInformation.getVehicle(conn, null, TEXT_VEHICLE_NAME.getText());
					if (vehPair != null) {
						m_vehicleId = vehPair.first;
					}
				}

				isTagIssue = issueTag(conn, m_vehicleId, CacheTrack.standardizeName(TEXT_VEHICLE_NAME.getText().trim()),
						TokenManager.userId);
				if (isTagIssue == 0) {
					parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Card Issued");
				} else if (isTagIssue == 1) {
					parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Card Not Issued");
				} else if (isTagIssue == 2) {
					parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Reader Not Connected");
				} else if (isTagIssue == 3) {
					parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Multiple Tags On Reader");
				} else if (Misc.isUndef(isTagIssue)) {
					parent.showAlert(Alert.AlertType.ERROR, "Message",
							"This RFID Tag has already issued for other vehicle");
					return false;
				} else {
					parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Tag not read");
				}
				if (isTagIssue == 0) {
					updateTPR(conn, TokenManager.nextWorkStationType);
					InsertTPRStep(conn, false);

					if (isLastTprBlocked) {
						updateBlocking(conn);
					}
					clearInputs(conn, true);
					conn.commit();
				}

			}
		} catch (Exception ex) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", UIConstant.SAVE_FAILER_MESSAGE);
			// ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
				// ex.printStackTrace();
			}
		}
		System.out.println("######### End Gate In save() ######### ");
		return true;

	}

	private void updateBlocking(Connection conn) {
		try {
			TPRecord lstCompltdTpr = TPRInformation.getLastCompletedTPRForView(conn, tpRecord.getVehicleId());
			if (lstCompltdTpr != null) {
				RFIDMasterDao.executeQuery(conn,
						"update tpr_block_status set status=0 where tpr_id = '" + lstCompltdTpr.getTprId() + "'");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, e);
		}

	}

	private void updateTPR(Connection conn, int nextWorkStation) throws Exception {
		updateTPR(conn, nextWorkStation, false);
	}

	private void updateTPR(Connection conn, int nextWorkStation, boolean isDeny) throws Exception {
		java.util.Date curr = new java.util.Date();
		if (!isDeny) {
			tpRecord.setTransporterId(LovUtils.getIntValue(COMBO_TRANSPORTER));
			tpRecord.setTransporterCode(LovUtils.getTextValue(COMBO_TRANSPORTER));
			tpRecord.setConsignee(LovUtils.getIntValue(COMBO_CUSTOMER));
			tpRecord.setConsigneeName(LovUtils.getTextValue(COMBO_CUSTOMER));
			tpRecord.setDoId(LovUtils.getIntValue(COMBO_LINE_ITEM));
			tpRecord.setDoNumber(LovUtils.getTextValue(COMBO_LINE_ITEM));
			tpRecord.setPortNodeId(TokenManager.portNodeId);
			String salesOrder = LovUtils.getTextValue(COMBO_PO_SALES_ORDER);
			if (salesOrder.contains("@"))
				salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
			tpRecord.setProductCode(salesOrder);
			// tpRecord.setWasheryCode(TokenManager.HSN_NO);
			tpRecord.setLrNo(TEXT_LR_NO.getText());
			Date _lrDate = DATE_LR.getValue().toString() != null
					? new SimpleDateFormat("yyyy-MM-dd").parse(DATE_LR.getValue().toString())
					: null;
			tpRecord.setLrDate(_lrDate);
			tpRecord.setConsigneeRefDoc(Misc.getParamAsString(TEXT_NOTE.getText()));
			tpRecord.setPreStepType(TokenManager.currWorkStationType);
			tpRecord.setPrevTpStep(TokenManager.currWorkStationId);
			tpRecord.setPreStepDate(curr);
			tpRecord.setUpdatedBy(TokenManager.userId);
			tpRecord.setUpdatedOn(curr);
			tpRecord.setNextStepType(nextWorkStation);
			tpRecord.setComboEnd(new Date());
			tpRecord.setStatus(1);
			if (TokenManager.closeTPR) {
				tpRecord.setTprStatus(Status.TPR.CLOSE);
			}
			tpRecord.setLatestLoadGateInExit(new Date());
			tpRecord.setLoadGateInName(TokenManager.userName);
		}
		if (tpRecord.getComboStart() == null) {
			tpRecord.setComboStart(entryTime);
		}
		tpRecord.setIsNewVehicle(isNewVehicle);
		tpRecord.setEarliestLoadGateInEntry(entryTime);
		TPRInformation.insertUpdateTpr(conn, tpRecord);

	}

	private int InsertTPRStep(Connection conn, boolean isDeny) throws Exception {
		if (tpStep == null || Misc.isUndef(tpStep.getId())) {
			System.out.println("[Manual Creted TpStep]");
			tpStep = new TPStep();
			tpStep.setEntryTime(entryTime);
			tpStep.setExitTime(new Date());
			tpStep.setTprId(tpRecord.getTprId());
			tpStep.setUpdatedBy(TokenManager.userId);
			tpStep.setVehicleId(tpRecord.getVehicleId());
			tpStep.setWorkStationId(TokenManager.currWorkStationId);
			tpStep.setWorkStationType(TokenManager.currWorkStationType);
			tpStep.setUpdatedOn(new Date());
			tpStep.setHasValidRf(isNewVehicle);
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.insert(conn, tpStep, false);
			// RFIDMasterDao.insert(conn, tpStep, true);
		} else {
			// tpStep.setTprId(tprRecord.getTprId());
			long currTimeServerMillis = System.currentTimeMillis();
			tpStep.setExitTime(new Date(currTimeServerMillis));
			tpStep.setUpdatedOn(new Date(currTimeServerMillis));
			tpStep.setHasValidRf(isNewVehicle);
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.update(conn, tpStep, false);
			// RFIDMasterDao.update(conn, tpStep, true);
		}
		return tpStep.getId();
	}

	private int issueTag(Connection conn, int vehicleId, String vehicleName, int userId) throws SQLException {
		int retval = Misc.getUndefInt();
		boolean _isTagAlreadyIssued = false;
		if (Misc.isUndef(vehicleId)) {
			return retval;
		}
		try {
			IssueTag issueTag = new IssueTag();
			vehBean = GateInDao.selectDataFromVehicle(conn, vehicleId);
			vehBean.setUpdatedBy(userId);
			vehBean.setRfid_issue_date(new Date());
			retval = issueTag.tagIssued(vehBean);
			_isTagAlreadyIssued = isTagAlreadyIssued(conn, vehBean.getEpcId());
			if (retval == 0 && !_isTagAlreadyIssued) {
				RFIDMasterDao.executeQuery(conn,
						"update vehicle set last_epc=rfid_epc where rfid_epc like '" + vehBean.getEpcId() + "'");
				RFIDMasterDao.executeQuery(conn,
						"update vehicle set rfid_epc=null where rfid_epc like '" + vehBean.getEpcId() + "'");
				RFIDMasterDao.update(conn, vehBean, false);
				parent.labelBlockingReason
						.setText("Vehicle No: " + vehBean.getVehicleName() + ", Tag EPC: " + vehBean.getEpcId());
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (_isTagAlreadyIssued)
			retval = Misc.UNDEF_VALUE;
		if (retval == 0 && _isTagAlreadyIssued)
			retval = 1;

		return retval;
	}

	boolean isTagAlreadyIssued(Connection conn, String epcCode) {
		boolean isTrue = false;
		TPRecord tprData = null;
		try {
			Vehicle prevVehicle = GateInDao.selectDataFromVehicle(conn, epcCode);
			if (prevVehicle != null && prevVehicle.getVehicleName() != null) {
				tprData = new TPRecord();
				tprData.setVehicleName(prevVehicle.getVehicleName());
				tprData.setTprStatus(Status.TPR.OPEN);
				tprData.setStatus(Status.ACTIVE);
				ArrayList<Object> list = GateInDao.getTransactionData(conn, tprData);
				if (list != null && list.size() > 0) {
					isTrue = true;
				}
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return isTrue;
	}

	@Override
	public void setVehicleName(String vehicleName) {
		TEXT_VEHICLE_NAME.setText(vehicleName != null ? vehicleName.toUpperCase() : "");
	}

	@Override
	public void clearVehicleName() {
		TEXT_VEHICLE_NAME.setText("");
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
		// TODO Auto-generated method stub
	}

	@Override
	public void vehicleAction() {
		Connection conn = null;
		boolean destroyIt = false;
		Pair<Integer, String> vehPair = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			String vehName = TEXT_VEHICLE_NAME.getText();
			if (Utils.isNull(vehName)) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Enter Vehicle !!!");
				TEXT_VEHICLE_NAME.requestFocus();
				return;
			} else {
				vehName = CacheTrack.standardizeName(vehName);
				vehPair = TPRInformation.getVehicle(conn, null, vehName);
				isEnterPressed = true;
				if (vehPair != null) {
					m_vehicleId = vehPair.first;
					isNewVehicle = Status.VEHICLE.EXISTING_MANUAL;
					setVehicleName(vehName);
				} else {
					String[] options = { "NO", "YES" };
					int res = parent.prompt(Alert.AlertType.CONFIRMATION.toString(),
							"Do you Want to Create New Vehicle?", options);
					if (res == 0) {
						clearVehicleName();
						this.TEXT_VEHICLE_NAME.requestFocus();
						return;
					} else {
						boolean isInsert = GateInDao.InsertNewVehicle(conn, vehName, TokenManager.userId);
						vehPair = TPRInformation.getVehicle(conn, null, vehName);
						m_vehicleId = vehPair != null ? vehPair.first : Misc.getUndefInt();
						isNewVehicle = Status.VEHICLE.NEW_MANUAL;//
						parent.labelBlockingReason.setText("New Vehicle Created");
						setVehicleName(vehName);
					}

				}
			}

			if (vehPair == null) {
				clearInputs();
				return;
			}

			RFIDDataProcessor rfidProcessor = new RFIDDataProcessor(0, TokenManager.currWorkStationType,
					TokenManager.currWorkStationId, parent.userData.getId());
			rfidProcessor.setTagListener(new TAGListener() {

				@Override
				public void showMessage(String message) {
					parent.showAlert(Alert.AlertType.ERROR, "Message", message);
				}

				@Override
				public void setVehicleName(String vehicleName) {
					TEXT_VEHICLE_NAME.setText(vehicleName != null ? vehicleName.toUpperCase() : "");
				}

				@Override
				public int promptMessage(String message, Object[] options) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps,
						TPRBlockManager _tprBlockManager) {
					tpRecord = tpr;
					tpStep = tps;
					tprBlockManager = _tprBlockManager;
					setTPRecord(conn, tpRecord);
				}

				@Override
				public void clearVehicleName() {
					// TODO Auto-generated method stub
					TEXT_VEHICLE_NAME.setText("");
				}

				@Override
				public void clear(boolean clearToken, Connection conn) {
					clearInputs();
				}

				@Override
				public int mergeData(long sessionId, String epc, RFIDHolder rfidHolder) {
					// TODO Auto-generated method stub
					return 0;
				}
			});
			rfidProcessor.getTprecord(vehName, m_vehicleId, false, true);

		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				// ex.printStackTrace();
				Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		tpRecord = tpr;
		tpStep = tps;
		tprBlockManager = _tprBlockManager;
		setTPRecord(conn, tpRecord);
	}

	private void setTPRecord(Connection conn, TPRecord tpRecord) {
		// TODO Auto-generated method stub
		if (tpRecord == null)
			return;

		if (isBlockedVehicle(conn)) {
			String[] options = { "Denied Entry", "Continue Process" };
			int res = parent.prompt(Alert.AlertType.WARNING.toString(),
					"Vehicle blocked due to " + blockingMsg + " in last trip.", options);

			if (res == 0) {
				clearInputs();
				return;
			} else {
				parent.labelBlockingReason.setText("");
				isLastTprBlocked = true;
			}

		}
		entryTime = new Date();
		tpRecord.setEarliestRegIn(entryTime);
		setBlockingStatus();

	}

	private void setBlockingStatus() {
		if (tprBlockManager == null) {
			return;
		}
		try {
			int blockStatus = tprBlockManager.getBlockStatus();
			if (blockStatus == UIConstant.BLOCKED) {
				Platform.runLater(() -> {
					parent.labelBlockingReason.setText("Vehicle Blocked: " + tprBlockManager.getBlockingReason(false));
					parent.setControllerDisable(true);
				});
			} else {
				Platform.runLater(() -> {
					parent.labelBlockingReason.setText(tprBlockManager.getBlockingReason(false));
					parent.setControllerDisable(false);
				});
			}

		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private boolean isBlockedVehicle(Connection conn) {
		boolean isVehicleBlocked = false;
		blockingMsg = null;

		if (tprBlockManager == null)
			return false;
		if (blockingMsg == null)
			blockingMsg = new StringBuilder();

		TPRecord lastCompletedTpr = TPRInformation.getLastCompletedTPRForView(conn, tpRecord.getVehicleId());
		if (lastCompletedTpr == null)
			return false;

		ArrayList<TPRBlockEntry> previousTprBlockEntries = tprBlockManager.getTPRBlockEntryList(conn,
				lastCompletedTpr.getTprId());
		TPRBlockEntry tprBlockEntry = null;
		for (int i = 0, is = previousTprBlockEntries == null ? 0 : previousTprBlockEntries.size(); i < is; i++) {
			tprBlockEntry = previousTprBlockEntries.get(i);
			BlockingInstruction bInstruction = tprBlockManager.getInstructionById(tprBlockEntry.getInstructionId());
			if (bInstruction == null)
				continue;

			if (bInstruction
					.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY) {
				if (blockingMsg.length() > 0)
					blockingMsg.append(",  ");
				blockingMsg.append(com.ipssi.rfid.constant.Type.BlockingInstruction.getBlockingStr(
						com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY));
				isVehicleBlocked = true;
			}

		}

		parent.labelBlockingReason.setText(blockingMsg.toString());
		return isVehicleBlocked;
	}

	// private void setDateConverter(DatePicker datePickerInstance) {
	// datePickerInstance.setConverter(new javafx.util.StringConverter<LocalDate>()
	// {
	//
	// @Override
	// public String toString(LocalDate date) {
	// if (date != null) {
	// return dateFormatter.format(date);
	// } else {
	// return "";
	// }
	// }
	//
	// @Override
	// public LocalDate fromString(String string) {
	// if (string != null && !string.isEmpty()) {
	// return LocalDate.parse(string, dateFormatter);
	// } else {
	// return null;
	// }
	// }
	// });
	// }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.MenuItemInfo;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.ScreenConstant;
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
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.dao.IssueTag;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.ipssi.rfid.ui.syncTprInfo.SyncTprInfo;
import com.ipssi.rfid.ui.syncTprInfo.SyncTprServiceHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class GateInWindowController implements Initializable, ControllerI {

	@FXML
	private HBox GATE_IN_HBOX;
	@FXML
	public JFXTextField TEXT_VEHICLE_NAME;
	@FXML
	private JFXButton ISSUE_TAG;
	@FXML
	private JFXComboBox<?> COMBO_PO_SALES_ORDER;
	@FXML
	private JFXComboBox<?> COMBO_TRANSPORTER;
	@FXML
	private JFXComboBox<?> COMBO_CUSTOMER;
	@FXML
	private JFXComboBox<?> COMBO_LINE_ITEM;

	@FXML
	public JFXTextField TEXT_DRIVER_NAME;
	
	@FXML
	private JFXTextArea TEXT_NOTE;
	
	@FXML
	private JFXTextField TEXT_DL_NUMBER;
	
	@FXML
	private Label TEXT_RUNNING_PROCESS;
	@FXML
	private Label TEXT_COMPLETED_PROCESS;
	@FXML
	private Label TEXT_TOTAL_PROCESS;
	@FXML
	private JFXButton CONTROL_PRINT;

	private int m_vehicleId = Misc.getUndefInt();
	private TPStep tpStep = null;
	private Date entryTime = null;
	private TPRecord tpRecord = null;
	private boolean isEnterPressed = false;
	private int isNewVehicle = Misc.getUndefInt();

	private Vehicle vehBean = null;
	private int isTagIssue = Misc.getUndefInt();
	private MainController parent = null;
	private MenuItemInfo menuItemInfo;
	private RFIDDataHandler rfidHandler = null;
	private SyncTprInfo syncTprInfo = null;
	StringBuilder blockingMsg  = null ;
	private TPRBlockManager tprBlockManager = null;
	private static final Logger log = Logger.getLogger(GateInWindowController.class.getName());
	boolean isLastTprBlocked = false;
	private Token token = null;
//	private int customerId = Misc.getUndefInt();
//	private int lineItem = Misc.getUndefInt();
	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		TEXT_VEHICLE_NAME.requestFocus();
		LovUtils.initializeComboBox(COMBO_CUSTOMER, LovDao.LovItemType.CUSTOMER, Misc.getUndefInt(),
				Misc.getUndefInt());
		LovUtils.initializeComboBox(COMBO_TRANSPORTER, LovDao.LovItemType.TRANSPORTER, Misc.getUndefInt(),
				Misc.getUndefInt());
		syncTpr();
	
		
		if(TokenManager.IS_AUTO_COMPLETE_ON){
			System.out.println("Gate In vehilce Auto Complete On");
			AutoCompleteTextField autoCompleteTextField = new AutoCompleteTextField(this.parent, TEXT_VEHICLE_NAME, LovDao.LovItemType.VEHICLE);
			autoCompleteTextField.setAutoCompleteTextBox();
		}else {
			System.out.println("Gate In vehilce Auto Complete Off");
		}
		
		CONTROL_PRINT.setVisible(TokenManager.gatePassPrinterConnected);
		
		AutoCompleteTextField driverAutoCombo = new AutoCompleteTextField(this.parent, TEXT_DL_NUMBER, LovDao.LovItemType.DL_NUMBER);
		driverAutoCombo.setAutoCompleteTextBox();
	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			String controllId = parent.getSourceId(event);
			handleActionControl(controllId);
		}
		if (event.getCode() == KeyCode.TAB) {
			String controllId = parent.getSourceId(event);
			handleActionControl(controllId);
		}
	}

	@FXML
	private void onControlAction(ActionEvent event) {
		String controllId = MainController.getSourceId(event);
		handleActionControlOnChange(controllId);
	}

	@FXML
	private void onControlDlNoMouseClicked(MouseEvent event) {
		if(Utils.isNull(TEXT_VEHICLE_NAME.getText()))
			return;
		
		handleActionControl("TEXT_VEHICLE_NAME");
	}

	@FXML
	private void onControlDriverNameMouseClicked(MouseEvent event) {
		if(Utils.isNull(TEXT_DL_NUMBER.getText()))
			return;
		
		handleActionControl("TEXT_DL_NUMBER");
	}
	
	private void handleActionControlOnChange(String controllId) {
		if (controllId == null || controllId == "" || controllId.length() == 0) {
			return;
		}
		switch (controllId.toUpperCase()) {

		case "COMBO_CUSTOMER":
			LovUtils.initializeComboBox(COMBO_PO_SALES_ORDER, LovDao.LovItemType.PO_SALES_ORDER,
					 Misc.getUndefInt(),LovUtils.getIntValue(COMBO_CUSTOMER), null);
			break;
		case "COMBO_PO_SALES_ORDER":
			LovUtils.initializeComboBox(COMBO_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM, Misc.getUndefInt(),
					Misc.getUndefInt(), LovUtils.getTextValue(COMBO_PO_SALES_ORDER));
			break;

		case "TEXT_DL_NUMBER":
			dlNoAction();
			TEXT_DRIVER_NAME.requestFocus();
			break;
		case "CONTROL_PRINT":
//			printScreen(GATE_IN_HBOX);
			openDialogWindow();
			break;
		default:
			break;
		}
	}

	private void handleActionControl(String controlId) {
		if (controlId == null || controlId == "" || controlId.length() == 0) {
			return;
		}
		switch (controlId.toUpperCase()) {
		case "TEXT_VEHICLE_NAME":
			vehicleNameAction();
			break;
		case "COMBO_CUSTOMER":
//			LovUtils.initializeComboBox(COMBO_PO_SALES_ORDER, LovDao.LovItemType.PO_SALES_ORDER,
//					 Misc.getUndefInt(),LovUtils.getIntValue(COMBO_CUSTOMER), null);
			COMBO_PO_SALES_ORDER.requestFocus();
			break;
		case "COMBO_PO_SALES_ORDER":
//			LovUtils.initializeComboBox(COMBO_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM, Misc.getUndefInt(),
//					Misc.getUndefInt(), LovUtils.getTextValue(COMBO_PO_SALES_ORDER));
			COMBO_LINE_ITEM.requestFocus();
			break;

		case "COMBO_LINE_ITEM":
			COMBO_TRANSPORTER.requestFocus();
			break;

		case "COMBO_TRANSPORTER":
			TEXT_NOTE.requestFocus();
			break;
		case "TEXT_DRIVER_NAME":
			COMBO_CUSTOMER.requestFocus();
			break;
		case "TEXT_DL_NUMBER":
			dlNoAction();
			TEXT_DRIVER_NAME.requestFocus();
			break;
		case "CONTROL_PRINT":
//			printScreen(GATE_IN_HBOX);
			openDialogWindow();
			break;
		default:
			break;
		}
	}




	@FXML
	private void vehicleNameMouseClick(ActionEvent event) {
		vehicleNameAction();
	}

	@Override
	public void clearInputs() {
		clearAction();
		}
	
	private void clearInputs(Connection conn, boolean clearToken) {
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token  != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		
		clearVehicleName();
//		parent.setControllerDisable(false);
		isNewVehicle = Misc.getUndefInt();
		m_vehicleId = Misc.getUndefInt();
		token = null;
		tpStep = null;
		entryTime = null;
		tpRecord = null;
		isEnterPressed = false;
		vehBean = null;
		isTagIssue = Misc.getUndefInt();
		rfidHandler = null;
		TEXT_NOTE.setText("");
	
		
		
		
		COMBO_LINE_ITEM.getSelectionModel().clearAndSelect(-1);
		COMBO_PO_SALES_ORDER.getSelectionModel().clearAndSelect(-1);
		COMBO_CUSTOMER.getSelectionModel().clearAndSelect(-1);
		COMBO_TRANSPORTER.getSelectionModel().clearAndSelect(0);
		TEXT_DRIVER_NAME.setText("");
		TEXT_DL_NUMBER.setText("");
		TEXT_RUNNING_PROCESS.setText("");
		TEXT_COMPLETED_PROCESS.setText("");
		TEXT_TOTAL_PROCESS.setText("");
		blockingMsg = null;
		isLastTprBlocked = false;
	

	}

	private void clearAction() {
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

	@Override
	public boolean save() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if (!isEnterPressed) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Search by 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			}else if (m_vehicleId == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if(LovUtils.getIntValue(COMBO_CUSTOMER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Customer'.");
				COMBO_CUSTOMER.requestFocus();
				return false;
			} 
			else if(LovUtils.getIntValue(COMBO_PO_SALES_ORDER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Po Sales Order'. ");
				COMBO_PO_SALES_ORDER.requestFocus();
				return false;
			} else if(LovUtils.getIntValue(COMBO_LINE_ITEM) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Line Item'.");
				COMBO_LINE_ITEM.requestFocus();
				return false;
			}else if(LovUtils.getIntValue(COMBO_TRANSPORTER) ==  Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Transporter'.");
				COMBO_TRANSPORTER.requestFocus();
				return false;
			}else if(Utils.isNull(TEXT_DRIVER_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Enter Driver Name.");
				TEXT_DRIVER_NAME.requestFocus();
				return false;
			}	if (Utils.isNull(TEXT_NOTE.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Card Number'.");
				TEXT_NOTE.requestFocus();
				return false;
			} 
			else {
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
					parent.showAlert(Alert.AlertType.ERROR, "Message", "This RFID Tag has already issued for other vehicle");
					return false;
				} else {
					parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Tag not read");
				}

				
				if (isTagIssue == 0) {
					updateTPR(conn, TokenManager.nextWorkStationType);
					InsertTPRStep(conn, false);
					Pair<String, String> pairVal = GateInDao.getDriverDetails(conn, CacheTrack.standardizeName(TEXT_DL_NUMBER.getText()));
					if(pairVal == null) {
						boolean isTrue = GateInDao.insertDriverDetails(conn,TEXT_DRIVER_NAME.getText(), CacheTrack.standardizeName(TEXT_DL_NUMBER.getText()));
					}
					if(isLastTprBlocked) {
						updateBlocking(conn);
					}
					clearInputs(conn,true);
					conn.commit();
					
				}

			}
		} catch (Exception ex) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", UIConstant.SAVE_FAILER_MESSAGE);
//			ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
				//ex.printStackTrace();
			}
		}

		return true;
	}

	private boolean isTagAlreadyIssued(Connection conn, String epcCode) {
		boolean isTrue = false;
		TPRecord tprData = null;
		try {
			Vehicle prevVehicle = GateInDao.selectDataFromVehicle(conn, epcCode);
			if (prevVehicle != null) {
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
//			ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return isTrue;
	}

	private void updateBlocking(Connection conn) {
		try {
			TPRecord lstCompltdTpr = TPRInformation.getLastCompletedTPRForView(conn, tpRecord.getVehicleId());
			if(lstCompltdTpr != null) {
				RFIDMasterDao.executeQuery(conn,"update tpr_block_status set status=0 where tpr_id = '" + lstCompltdTpr.getTprId() + "'");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, e);
		}

	}
	
	@Override
	public void vehicleNameAction() {
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
					int res = parent.prompt(Alert.AlertType.CONFIRMATION.toString(), "Do you Want to Create New Vehicle?",
							options);
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
						COMBO_LINE_ITEM.requestFocus();
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
//			ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
//				ex.printStackTrace();
				Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
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

		TEXT_DL_NUMBER.requestFocus();
	}

	
	private void setBlockingStatus() {
		if (tprBlockManager == null) {
			return;
		}
		try {
			int blockStatus = tprBlockManager.getBlockStatus();
			if (blockStatus == UIConstant.BLOCKED) {
				Platform.runLater(() -> {
					parent.labelBlockingReason.setText("Vehicle Blocked: "+tprBlockManager.getBlockingReason(false));
					parent.setControllerDisable(true);
				});
				// overrides.setText("BLOCKED");
			} else {
				// overrides.setText("NOT_BLOCKED");
				Platform.runLater(() -> {
					parent.labelBlockingReason.setText(tprBlockManager.getBlockingReason(false));
					parent.setControllerDisable(false);
				});
			}

		} catch (Exception ex) {
//			ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	


	private boolean isBlockedVehicle(Connection conn) {
		boolean isVehicleBlocked = false;
		blockingMsg = null;
		
		if(tprBlockManager == null)
			return false;
		if (blockingMsg == null)
			blockingMsg = new StringBuilder();
		
		
		
		TPRecord lastCompletedTpr = TPRInformation.getLastCompletedTPRForView(conn, tpRecord.getVehicleId());
		if(lastCompletedTpr == null)
			return false;
		
		
		ArrayList<TPRBlockEntry> previousTprBlockEntries = tprBlockManager.getTPRBlockEntryList(conn,lastCompletedTpr.getTprId());
		TPRBlockEntry tprBlockEntry = null;
		for (int i = 0, is = previousTprBlockEntries == null ? 0 : previousTprBlockEntries.size(); i < is; i++) {
			tprBlockEntry = previousTprBlockEntries.get(i);
			BlockingInstruction bInstruction = tprBlockManager.getInstructionById(tprBlockEntry.getInstructionId());
			if(bInstruction == null)
				continue;
			
			if (bInstruction.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY) {
				if(blockingMsg.length() > 0)
					blockingMsg.append(",  ");
				blockingMsg.append(com.ipssi.rfid.constant.Type.BlockingInstruction.getBlockingStr(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY));
				isVehicleBlocked = true;
			}
			
			if (bInstruction.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_UNSAFE_DRIVING) {
				if(blockingMsg.length() > 0)
					blockingMsg.append(",  ");
				blockingMsg.append(com.ipssi.rfid.constant.Type.BlockingInstruction.getBlockingStr(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_UNSAFE_DRIVING));
				isVehicleBlocked = true;
			}
			
			if (bInstruction.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DRIVER_BEHAVIOUR) {
				if(blockingMsg.length() > 0)
					blockingMsg.append(",  ");
				blockingMsg.append(com.ipssi.rfid.constant.Type.BlockingInstruction.getBlockingStr(com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DRIVER_BEHAVIOUR));
				isVehicleBlocked = true;
			}
			
		}

		parent.labelBlockingReason.setText(blockingMsg.toString());
		

		return isVehicleBlocked;
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
			tpRecord.setProductCode(LovUtils.getTextValue(COMBO_PO_SALES_ORDER));
			tpRecord.setWasheryCode(TokenManager.HSN_NO);
			tpRecord.setDriverName(TEXT_DRIVER_NAME.getText());
			tpRecord.setDlNo(CacheTrack.standardizeName(TEXT_DL_NUMBER.getText()));
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

	private int issueTag(Connection conn, int vehicleId, String vehicleName, int userId) throws SQLException {
		int retval = Misc.getUndefInt();

		if (Misc.isUndef(vehicleId)) {
			return retval;
		}
		try {
			IssueTag issueTag = new IssueTag();
				vehBean = GateInDao.selectDataFromVehicle(conn, vehicleId);
				vehBean.setUpdatedBy(userId);
				vehBean.setRfid_issue_date(new Date());
				retval = issueTag.tagIssued(vehBean);
				
				if(isTagAlreadyIssued(conn, vehBean.getEpcId())) {
					return Misc.getUndefInt();
				}
				
				if (retval == 0) {
					RFIDMasterDao.executeQuery(conn,
							"update vehicle set last_epc=rfid_epc where rfid_epc like '" + vehBean.getEpcId() + "'");
					RFIDMasterDao.executeQuery(conn,
							"update vehicle set rfid_epc=null where rfid_epc like '" + vehBean.getEpcId() + "'");
					RFIDMasterDao.update(conn, vehBean, false);
				}
			
		} catch (Exception ex) {
//			ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return retval;
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
			RFIDMasterDao.insert(conn, tpStep, true);
		} else {
			// tpStep.setTprId(tprRecord.getTprId());
			long currTimeServerMillis = System.currentTimeMillis();
			tpStep.setExitTime(new Date(currTimeServerMillis));
			tpStep.setUpdatedOn(new Date(currTimeServerMillis));
			tpStep.setHasValidRf(isNewVehicle);
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.update(conn, tpStep, false);
			RFIDMasterDao.update(conn, tpStep, true);
		}
		return tpStep.getId();
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
		
	}

	@Override
	public void stopRfid() {
		try {
			if (rfidHandler != null) {
				rfidHandler.stop();
			}

		} catch (Exception ex) {
//			ex.printStackTrace();
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
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
	public void enableController(Button controller, boolean enable) {
		if (controller == null || controller.isDisable() || !controller.isVisible()) {
			return;
		}
		String controlId = controller.getId().toUpperCase();

		switch (controlId) {
		case "TEXT_VEHICLE_NAME":
			TEXT_VEHICLE_NAME.setEditable(enable);
			break;
		case "CONTROL_SAVE":
			parent.CONTROL_SAVE.setDisable(enable);
			break;
		default:
			break;
		}
	}

	@Override
	public void enableManualEntry(boolean enable) {
		parent.CONTROL_MANUAL.setDisable(enable);
	}

	void syncTpr() {
		if (syncTprInfo == null) {
			syncTprInfo = new SyncTprInfo();
			syncTprInfo.setHandler(new SyncTprServiceHandler() {
				@Override
				public void clear() {
					TEXT_RUNNING_PROCESS.setText("");
					TEXT_COMPLETED_PROCESS.setText("");
					TEXT_TOTAL_PROCESS.setText("");
				}

				@Override
				public void init(int runningProcess, int completedProcess) {

					Platform.runLater(() -> {
						TEXT_RUNNING_PROCESS.setText(Misc.getPrintableInt(runningProcess));
						TEXT_COMPLETED_PROCESS.setText(Misc.getPrintableInt(completedProcess));
						int total = 0;
						if (!Misc.isUndef(runningProcess)) {
							total += runningProcess;
						}
						if (!Misc.isUndef(completedProcess)) {
							total += completedProcess;
						}
						TEXT_TOTAL_PROCESS.setText(Misc.getPrintableInt(total));

					});
				}

			});

			syncTprInfo.start();
		}

	}

	@Override
	public void stopSyncTprService() {
		if (syncTprInfo != null) {
			syncTprInfo.stop();
		}
	}

	@Override
	public void initController(SettingController parent) {
	}

	@Override
	public void requestFocusNextField() {
		TEXT_VEHICLE_NAME.requestFocus();
	}

	@Override
	public void setTitle(String title) {
	}
	

	@Override
	public void dlNoAction() {
		if(Utils.isNull(TEXT_DL_NUMBER.getText()))
			return;
		String driverName = LovDao. getText(TokenManager.portNodeId, LovDao.LovItemType.DRIVER_NAME,TEXT_DL_NUMBER.getText());
		TEXT_DRIVER_NAME.setText(driverName);
//		TEXT_DRIVER_NAME.requestFocus();
	}	
	
	
	
	public  void openDialogWindow() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ScreenConstant.ScreenLinks.PRINT_GATE_PASS_WINDOW));
		Parent parentDialog;
		try {
			if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return ;
			} else if (!isEnterPressed) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Search by 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return ;
			}else if (m_vehicleId == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Vehicle Number'.");
				TEXT_VEHICLE_NAME.requestFocus();
				return ;
			} else if(LovUtils.getIntValue(COMBO_CUSTOMER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Customer'.");
				COMBO_CUSTOMER.requestFocus();
				return ;
			} 
			else if(LovUtils.getIntValue(COMBO_PO_SALES_ORDER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Po Sales Order'. ");
				COMBO_PO_SALES_ORDER.requestFocus();
				return ;
			} else if(LovUtils.getIntValue(COMBO_LINE_ITEM) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Line Item'.");
				COMBO_LINE_ITEM.requestFocus();
				return ;
			}else if(LovUtils.getIntValue(COMBO_TRANSPORTER) ==  Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Transporter'.");
				COMBO_TRANSPORTER.requestFocus();
				return ;
			}else if(Utils.isNull(TEXT_DRIVER_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Enter Driver Name.");
				TEXT_DRIVER_NAME.requestFocus();
				return ;
			}	
			
			parentDialog = fxmlLoader.load();
			PrintGateInPassController dialogController = fxmlLoader.<PrintGateInPassController>getController();
		    dialogController.initData(TEXT_VEHICLE_NAME.getText(), TEXT_DL_NUMBER.getText(), TEXT_DRIVER_NAME.getText(), LovUtils.getTextValue(COMBO_PO_SALES_ORDER), LovUtils.getTextValue(COMBO_LINE_ITEM), LovUtils.getTextValue(COMBO_CUSTOMER), LovUtils.getTextValue(COMBO_TRANSPORTER), TEXT_NOTE.getText());
		    dialogController.setTitle(ScreenConstant.ScreenTitle.GATE_PASS);
			Scene scene = new Scene(parentDialog, 588, 171);
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
			GateInDao.pageSetup(parentDialog, stage ,parent);
			stage.close();
		} catch (IOException ex) {
			Logger.getLogger(GateInWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
	
	}

	
	
}

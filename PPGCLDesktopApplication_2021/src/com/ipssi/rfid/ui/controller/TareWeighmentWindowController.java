/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.awt.CheckboxGroup;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.integration.WeighBridge;
import com.ipssi.rfid.integration.WeighmentReader;
import com.ipssi.rfid.integration.WeighmentReaderHandler;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.RFIDDataProcessor;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.readers.WeighBridgeTcp;
import com.ipssi.rfid.ui.autocompleteText.AutoCompleteTextField;
import com.ipssi.rfid.ui.controller.service.ActionControllerI;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovDao.LovItemType;
//import com.ipssi.rfid.ui.syncTprInfo.SyncTprInfo;
//import com.ipssi.rfid.ui.syncTprInfo.SyncTprServiceHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Virendra Gupta
 */
public class TareWeighmentWindowController implements Initializable, ControllerI, ActionControllerI {

	@FXML
	private AnchorPane GROSS_ROOT_ANCHORPANE;

	@FXML
	private JFXButton SAVE_TARE_WEIGHT;

	@FXML
	private JFXButton CLEAR_SCREEN;

	@FXML
	private JFXButton weighmentReadButton;

	@FXML
	private Label WEIGHMENT_LABEL;

	@FXML
	private Label TEXT_TPR_ID;
	// @FXML
	// private Label TEXT_RUNNING_PROCESS;
	// @FXML
	// private Label TEXT_COMPLETED_PROCESS;
	// @FXML
	// private Label TEXT_TOTAL_PROCESS;
	WeighmentReader weighmentReader = null;
	@FXML
	private JFXCheckBox CHECKBOX_WB1;

	@FXML
	private JFXCheckBox CHECKBOX_WB2;

	@FXML
	private JFXCheckBox CHECKBOX_WB3;

	@FXML
	private JFXCheckBox CHECKBOX_WB4;

	CheckboxGroup checked = new CheckboxGroup();

	@FXML
	private JFXTextField TEXT_VEHICLE_NAME;
	@FXML
	private Label TEXT_PO_SALES_ORDER;
	@FXML
	private Label TEXT_CUSTOMER;
	@FXML
	private Label TEXT_TRANSPORTER;
	@FXML
	private Label TEXT_TARE_WEIGHT;
	@FXML
	private Label TEXT_TARE_TIME;

	@FXML
	private JFXTextArea TEXT_NOTE;

	private boolean isTagRead = false;
	// private boolean isTpRecordValid = false;
	private int readerId = 0;
	private RFIDDataHandler rfidHandler = null;
	private WeighBridge weighBridge = null;
	private Date entryTime = null;
	Token token = null;
	private TPRecord tpRecord = null;
	private TPStep tpStep = null;
	private ArrayList<Pair<Long, Integer>> readings = null;
	private DisconnectionDialog disconnectionDialog = new DisconnectionDialog(
			"Weigh Bridge Disconnected please check connection.....");
	private MainController parent = null;
	private TPRBlockManager tprBlockManager = null;
	// private SyncTprInfo syncTprInfo = null;
	private AutoCompleteTextField autoCompleteTextField = null;
	private int m_vehicleId = Misc.getUndefInt();
	private boolean isEnterPressed = false;
	private int isNewVehicle = Misc.getUndefInt();
	private static final Logger log = Logger.getLogger(TareWeighmentWindowController.class.getName());
	private boolean isWeighmentRead = false;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		WEIGHMENT_LABEL.setText(TokenManager.weight_val);

		if (!TokenManager.forceManual) {
			try {
				start();
			} catch (IOException ex) {
				Logger.getLogger(TareWeighmentWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else if (TokenManager.forceManual && TokenManager.IS_AUTO_COMPLETE_ON) {
			autoCompleteTextField = new AutoCompleteTextField(this.parent, TEXT_VEHICLE_NAME,
					LovDao.LovItemType.VEHICLE);
			autoCompleteTextField.setAutoCompleteTextBox();
		}
	}

	private void start() throws IOException {
		if (rfidHandler == null) {
			rfidHandler = new RFIDDataHandler(1000, readerId, TokenManager.currWorkStationType,
					TokenManager.currWorkStationId, TokenManager.userId);
			rfidHandler.setTagListener(new TAGListener() {

				@Override
				public void manageTag(Connection conn, Token _token, TPRecord tpr, TPStep tps,
						TPRBlockManager _tprBlockManager) {
					try {
						token = _token;
						tprBlockManager = _tprBlockManager;
						setTPRecord(conn, tpr);
						tpStep = tps;
						// Barrier.ChangeSignal();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				@Override
				public void showMessage(String message) {
					Platform.runLater(() -> {
						parent.showAlert(Alert.AlertType.INFORMATION, "Message", message);
					});
				}

				@Override
				public void setVehicleName(String text) {
					TEXT_VEHICLE_NAME.setText(text);
				}

				@Override
				public void clearVehicleName() {
					TEXT_VEHICLE_NAME.setText("NO VEHICLE DETECTED");
				}

				@Override
				public int promptMessage(String message, Object[] options) {
					return ConfirmationDialog.getDialogBox(new javax.swing.JFrame(), true, options, message);
				}

				@Override
				public void clear(boolean clearToken, Connection conn) {
					Platform.runLater(() -> {
						clearInputs(conn, clearToken);
					});
				}

				@Override
				public int mergeData(long sessionId, String epc, RFIDHolder rfidHolder) {
					// TODO Auto-generated method stub
					return 0;
				}

			});
		}
		rfidHandler.start();

	}

	@FXML
	public void onControlAction(ActionEvent event) {
		String controllId = parent.getSourceId(event);
		handleActionControlOnChange(controllId);
	}

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ALT) {
			String controllId = parent.getSourceId(event);
			// _selectedMenuControl = control;
			// Button control = ((Button) event.getSource());
			handleActionControl(controllId);
		}
	}

	@FXML
	private void controlItemClicked(MouseEvent event) {
		// Button control = ((Button) event.getSource());
		String controllId = parent.getSourceId(event);
		handleActionControl(controllId);
	}

	@Override
	public void handleActionControl(String control) {
		// if (control == null || control.isDisable() || !control.isVisible()) {
		// return;
		// }
		String controlId = control; // control.getId().toUpperCase();
		switch (controlId.toUpperCase()) {
		case "TEXT_VEHICLE_NAME":
			vehicleAction();
			break;
		case "CHECKBOX_WB1":
			weighmentReadButton.requestFocus();
			break;
		case "CHECKBOX_WB2":
			weighmentReadButton.requestFocus();
			break;
		case "CHECKBOX_WB3":
			weighmentReadButton.requestFocus();
			break;
		case "CHECKBOX_WB4":
			weighmentReadButton.requestFocus();
			break;
		case "WEIGHMENTREADBUTTON":
			initializeWeighbridgeNew();
		default:
			break;
		}
	}

	private void handleActionControlOnChange(String controllId) {
		if (controllId == null || controllId == "" || controllId.length() == 0) {
			return;
		}
		switch (controllId.toUpperCase()) {
		case "CHECKBOX_WB1":
			if (CHECKBOX_WB1.isSelected()) {
				changeCheckboxState(controllId);
				System.out.println("CHECKBOX_WB1");
			}
			break;
		case "CHECKBOX_WB2":
			if (CHECKBOX_WB2.isSelected()) {
				changeCheckboxState(controllId);
				System.out.println("CHECKBOX_WB2");
			}
			break;
		case "CHECKBOX_WB3":
			if (CHECKBOX_WB3.isSelected()) {
				changeCheckboxState(controllId);
				System.out.println("CHECKBOX_WB3");
			}
			break;
		case "CHECKBOX_WB4":
			if (CHECKBOX_WB4.isSelected()) {
				changeCheckboxState(controllId);
				System.out.println("CHECKBOX_WB4");
			}
			break;

		default:
			break;
		}
	}

	private void initializeWeighbridge() {
		Platform.runLater(() -> {
			if (CHECKBOX_WB1.isSelected()) {
				readWeighment(TokenManager.WEIGHBRIDGE_ONE_HOST, TokenManager.WEIGHBRIDGE_ONE_PORT);
			} else if (CHECKBOX_WB2.isSelected()) {
				readWeighment(TokenManager.WEIGHBRIDGE_TWO_HOST, TokenManager.WEIGHBRIDGE_TWO_PORT);
			} else if (CHECKBOX_WB3.isSelected()) {
				readWeighment(TokenManager.WEIGHBRIDGE_THREE_HOST, TokenManager.WEIGHBRIDGE_THREE_PORT);
			} else if (CHECKBOX_WB4.isSelected()) {
				readWeighment(TokenManager.WEIGHBRIDGE_FOUR_HOST, TokenManager.WEIGHBRIDGE_FOUR_PORT);
			}

		});
	}

	private void readWeighment(String ipAddress, int port) {
		isWeighmentRead = true;
		WeighBridgeTcp weighBridgeTcp = new WeighBridgeTcp(ipAddress, port);
		// WeighBridgeTcp weighBridgeTcp = new WeighBridgeTcp("127.0.0.1",5000);
		boolean isConnected = weighBridgeTcp.getConnection();
		if (isConnected) {
			byte[] _weight = weighBridgeTcp.executeCommand("05");
			String wght = new String(_weight);
			System.out.println("WeightmentTare: " + wght);
			if (wght != null) {
				wght = wght.substring(0, wght.indexOf("kg")).trim();
				WEIGHMENT_LABEL.setText(wght);
				double tare_txt = Double.parseDouble(wght) / 1000;
				TEXT_TARE_WEIGHT.setText(tare_txt + "");
			}
		}
	}

	private void changeCheckboxState(String controllId) {
		switch (controllId.toUpperCase()) {
		case "CHECKBOX_WB1":
			if (CHECKBOX_WB1.isSelected()) {
				CHECKBOX_WB2.setSelected(false);
				CHECKBOX_WB3.setSelected(false);
				CHECKBOX_WB4.setSelected(false);
			}
			break;
		case "CHECKBOX_WB2":
			if (CHECKBOX_WB2.isSelected()) {
				CHECKBOX_WB1.setSelected(false);
				CHECKBOX_WB3.setSelected(false);
				CHECKBOX_WB4.setSelected(false);
			}
			break;
		case "CHECKBOX_WB3":
			if (CHECKBOX_WB3.isSelected()) {
				CHECKBOX_WB1.setSelected(false);
				CHECKBOX_WB2.setSelected(false);
				CHECKBOX_WB4.setSelected(false);
			}
			break;
		case "CHECKBOX_WB4":
			if (CHECKBOX_WB4.isSelected()) {
				CHECKBOX_WB1.setSelected(false);
				CHECKBOX_WB2.setSelected(false);
				CHECKBOX_WB3.setSelected(false);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void clearInputs() {
		clearAction();
	}

	@Override
	public boolean save() {

		System.out.println("######### Start Tare Weighment save() ######### ");

		Connection conn = null;
		boolean destroyIt = false;
		try {

			if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid vehicle name");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if (!CHECKBOX_WB1.isSelected() && !CHECKBOX_WB2.isSelected() && !CHECKBOX_WB3.isSelected()
					&& !CHECKBOX_WB4.isSelected()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please select atleast one weighbridge checkbox.");
				CHECKBOX_WB1.requestFocus();
				return false;
			} else if (!isWeighmentRead) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Read Weighment.");
				weighmentReadButton.requestFocus();
				return false;
			} else {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				double captureWeight = Misc.getParamAsDouble(WEIGHMENT_LABEL.getText());

				if (captureWeight < TokenManager.min_weight || captureWeight > TokenManager.max_weight) {
					parent.showAlert(Alert.AlertType.ERROR, "Message",
							"Captured Weight is not in limits (" + TokenManager.min_weight / 1000 + "-"
									+ TokenManager.max_weight / 1000 + " MT).Please capture properly");
					return false;
				} else {
					captureWeight = captureWeight / 1000;
				}

				TokenManager.currWorkStationId = CHECKBOX_WB1.isSelected() ? 1
						: CHECKBOX_WB2.isSelected() ? 2 : CHECKBOX_WB3.isSelected() ? 3 : 4;
				updateTPR(conn, captureWeight);
				int stepId = InsertTPRStep(conn, false);
				conn.commit();
				parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Data Saved");
				clearInputs(conn, false);
			}
		} catch (Exception ex) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", UIConstant.SAVE_FAILER_MESSAGE);
			ex.printStackTrace();
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		System.out.println("######### End Tare Weighment save() ######### ");
		return true;

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

	private void clearInputs(Connection conn, boolean clearToken) {
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		clearVehicleName();
		stopWeighmentRead();
		tpStep = null;
		entryTime = null;
		tpRecord = null;
		token = null;
		tpStep = null;
		tprBlockManager = null;
		isTagRead = false;
		m_vehicleId = Misc.getUndefInt();
		isEnterPressed = false;
		isNewVehicle = Misc.getUndefInt();
		TEXT_NOTE.setText("");
		// TEXT_VEHICLE_NAME.setText("");
		// WEIGHMENT_LABEL.setText("0");
		WEIGHMENT_LABEL.setText(TokenManager.weight_val);
		// isTpRecordValid = false;
		TEXT_PO_SALES_ORDER.setText("");
		TEXT_CUSTOMER.setText("");
		TEXT_TRANSPORTER.setText("");
		TEXT_TARE_WEIGHT.setText("0");
		TEXT_TARE_TIME.setText("");
		CHECKBOX_WB1.setSelected(false);
		CHECKBOX_WB2.setSelected(false);
		CHECKBOX_WB3.setSelected(false);
		CHECKBOX_WB4.setSelected(false);
		isWeighmentRead = false;
		// TEXT_RUNNING_PROCESS.setText("");
		// TEXT_COMPLETED_PROCESS.setText("");
		// TEXT_TOTAL_PROCESS.setText("");
		TEXT_TPR_ID.setText("");
		// parent.setControllerDisable(false);
	}

	public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
		System.out.println("######### Weigh bridge In setTPRecord  ########");
		try {
			tpRecord = tpr;
			if (tpRecord != null) {
				System.out.println("TPR Record Create");
				if (true) {
					// isTpRecordValid = true;
					isTagRead = token != null ? token.isReadFromTag() : false;
					if (token == null && tpRecord.getEarliestLoadWbOutEntry() != null) {
						System.out.println("Entry Time 1st");
						entryTime = tpRecord.getEarliestLoadWbOutEntry();
					} else if (token != null && tpRecord.getEarliestLoadWbOutEntry() == null) {
						System.out.println("Entry Time 2nd :" + token.getLastSeen());
						if (token.getLastSeen() != Misc.getUndefInt()) {
							entryTime = new Date(token.getLastSeen());
						} else {
							entryTime = new Date();
						}
					} else if (token != null && tpRecord.getEarliestLoadWbOutEntry() != null) {
						System.out.println("Entry Time 3rd :" + token.getLastSeen());
						if (token.getLastSeen() > Utils.getDateTimeLong(tpRecord.getEarliestLoadWbOutEntry())) {
							if (token.getLastSeen() != Misc.getUndefInt()) {
								entryTime = new Date(token.getLastSeen());
							} else {
								entryTime = new Date();
							}
							System.out.println("token " + entryTime);
						} else {
							entryTime = new Date();
						}
					} else {
						entryTime = new Date();
					}
					System.out.println("Entry Time :" + entryTime);

					Platform.runLater(() -> {
						// WEIGHMENT_LABEL.setText(TokenManager.weight_val);
						setVehicleName(tpRecord.getVehicleName());
						TEXT_TPR_ID.setText(Misc.getPrintableInt(tpRecord.getTprId()));
						TEXT_PO_SALES_ORDER.setText(LovDao.getText(TokenManager.portNodeId, LovItemType.PO_SALES_ORDER,
								tpRecord.getDoId(), Misc.getUndefInt()));
						TEXT_CUSTOMER.setText(LovDao.getText(TokenManager.portNodeId, LovItemType.CUSTOMER,
								tpRecord.getConsignee(), Misc.getUndefInt()));
						TEXT_TRANSPORTER.setText(LovDao.getText(TokenManager.portNodeId, LovItemType.TRANSPORTER,
								tpRecord.getTransporterId(), Misc.getUndefInt()));
						TEXT_TARE_TIME.setText(new SimpleDateFormat("HH:mm").format(new Date().getTime()));
						TEXT_NOTE.setText(
								tpRecord.getConsigneeRefDoc() != null ? tpRecord.getConsigneeRefDoc().trim() : "");
					});
				}
				// calculateGrossShort();
				setBlockingStatus();
				requestFocusNextField();
			} else {
				parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Invalid Vehicle Go to Registration");
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
			ex.printStackTrace();
		}
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	private void updateTPR(Connection conn, double captureWeight) throws Exception {
		updateTPR(conn, captureWeight, false);
	}

	private void updateTPR(Connection conn, double captureWeight, boolean isDeny) throws Exception {
		java.util.Date curr = new java.util.Date();
		if (!isDeny) {
			tpRecord.setLoadTare(captureWeight);
			tpRecord.setLoadWbOutName(TokenManager.userName);
			tpRecord.setPrevTpStep(TokenManager.currWorkStationId);
			tpRecord.setPreStepType(TokenManager.currWorkStationType);
			tpRecord.setNextStepType(TokenManager.nextWorkStationType);
			tpRecord.setPreStepDate(curr);
			tpRecord.setUpdatedBy(TokenManager.userId);
			tpRecord.setUpdatedOn(curr);
			tpRecord.setComboEnd(new Date());
			tpRecord.setLatestLoadWbOutExit(new Date());
			tpRecord.setLoadWbOutName(TokenManager.userName);
		}

		if (tpRecord.getComboStart() == null) {
			tpRecord.setComboStart(curr);
		}
		if (TokenManager.closeTPR) {
			tpRecord.setTprStatus(Status.TPR.CLOSE);
		}

		tpRecord.setEarliestLoadWbOutEntry(entryTime);
		tpRecord.setComboEnd(new Date());
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
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.insert(conn, tpStep, false);
			// RFIDMasterDao.insert(conn, tpStep, true);
		} else {
			// tpStep.setTprId(tprRecord.getTprId());
			long currTimeServerMillis = System.currentTimeMillis();
			tpStep.setExitTime(new Date(currTimeServerMillis));
			tpStep.setUpdatedOn(new Date(currTimeServerMillis));
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.update(conn, tpStep, false);
			// RFIDMasterDao.update(conn, tpStep, true);
		}
		return tpStep.getId();
	}

	@Override
	public void stopRfid() {
		try {
			if (rfidHandler != null) {
				rfidHandler.stop();
			}
			if (weighBridge != null) {
				weighBridge.stopWeighBridge();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
	public void enableController(Button controllerId, boolean enable) {
	}

	@Override
	public void enableManualEntry(boolean enable) {
		TEXT_VEHICLE_NAME.setEditable(enable);
		TEXT_VEHICLE_NAME.setFocusTraversable(enable);
	}

	@Override
	public void stopSyncTprService() {
		// if (syncTprInfo != null) {
		// syncTprInfo.stop();
		// }
	}

	@Override
	public void initController(SettingController parent) {
	}

	@Override
	public void requestFocusNextField() {
		// Platform.runLater(() -> {
		// if (TEXT_VEHICLE_NAME.isEditable())
		// TEXT_VEHICLE_NAME.requestFocus();
		// else if (!parent.CONTROL_SAVE.isDisable())
		// parent.CONTROL_SAVE.requestFocus();
		// else
		// parent.CONTROL_CLEAR.requestFocus();
		// });
	}

	@Override
	public void setTitle(String title) {
	}

	// void syncTpr() {
	// if (syncTprInfo == null) {
	// syncTprInfo = new SyncTprInfo();
	// syncTprInfo.setHandler(new SyncTprServiceHandler() {
	// @Override
	// public void clear() {
	// TEXT_RUNNING_PROCESS.setText("");
	// TEXT_COMPLETED_PROCESS.setText("");
	// TEXT_TOTAL_PROCESS.setText("");
	// }
	//
	// @Override
	// public void init(int runningProcess, int completedProcess) {
	//
	// Platform.runLater(() -> {
	// TEXT_RUNNING_PROCESS.setText(Misc.getPrintableInt(runningProcess));
	// TEXT_COMPLETED_PROCESS.setText(Misc.getPrintableInt(completedProcess));
	// int total = 0;
	// if (!Misc.isUndef(runningProcess)) {
	// total += runningProcess;
	// }
	// if (!Misc.isUndef(completedProcess)) {
	// total += completedProcess;
	// }
	// TEXT_TOTAL_PROCESS.setText(Misc.getPrintableInt(total));
	//
	// });
	// }
	//
	// });
	//
	// syncTprInfo.start();
	// }
	//
	// }

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
				CHECKBOX_WB1.requestFocus();
				vehName = CacheTrack.standardizeName(vehName);
				vehPair = TPRInformation.getVehicle(conn, null, vehName);
				isEnterPressed = true;
				if (vehPair != null) {
					m_vehicleId = vehPair.first;
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
					try {
						tpRecord = tpr;
						tpStep = tps;
						tprBlockManager = _tprBlockManager;
						setTPRecord(conn, tpRecord);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
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
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub

	}

	private void initializeWeighbridgeNew() {
		isWeighmentRead = false;
		Platform.runLater(() -> {
			if (CHECKBOX_WB1.isSelected()) {
				readWeighmentData(TokenManager.WEIGHBRIDGE_ONE_HOST, TokenManager.WEIGHBRIDGE_ONE_PORT);
			} else if (CHECKBOX_WB2.isSelected()) {
				readWeighmentData(TokenManager.WEIGHBRIDGE_TWO_HOST, TokenManager.WEIGHBRIDGE_TWO_PORT);
			} else if (CHECKBOX_WB3.isSelected()) {
				readWeighmentData(TokenManager.WEIGHBRIDGE_THREE_HOST, TokenManager.WEIGHBRIDGE_THREE_PORT);
			} else if (CHECKBOX_WB4.isSelected()) {
				readWeighmentData(TokenManager.WEIGHBRIDGE_FOUR_HOST, TokenManager.WEIGHBRIDGE_FOUR_PORT);
			}
		});
	}

	private void readWeighmentData(String host, int port) {
		weighmentReader = new WeighmentReader(port, host, new WeighmentReaderHandler() {
			@Override
			public void initWeight(String wght) {
				Platform.runLater(() -> {
					if (wght != null) {
						String wghtData = wght.substring(0, wght.indexOf("kg")).trim();
						WEIGHMENT_LABEL.setText(wghtData);
						double tare_txt = Double.parseDouble(wghtData) / 1000;
						TEXT_TARE_WEIGHT.setText(tare_txt + "");
						isWeighmentRead = true;
						stopWeighmentRead();
					}else {
						WEIGHMENT_LABEL.setText(TokenManager.weight_val);
						double gross_txt = Double.parseDouble(TokenManager.weight_val) / 1000;
						TEXT_TARE_WEIGHT.setText(gross_txt + "");
						isWeighmentRead = true;
					} 
					
				});
			}

		});
		weighmentReader.start();
	
	}
	private void stopWeighmentRead() {
		if(weighmentReader!=null) {
			weighmentReader.stop();
			weighmentReader=null;
		}
	}
}

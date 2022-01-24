/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.mpl.dhama_gudiya.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.gen.utils.Misc;
import com.ipssi.mpl.dhama_gudiya.services.HttpClientServicesImpl;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.RFIDTagInfo;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.reader_new.RFIDDataHandlerWeb;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.controller.AutoCompleteTextField;
import com.ipssi.rfid.ui.controller.ConfirmationDialog;
import com.ipssi.rfid.ui.controller.ControllerI;
import com.ipssi.rfid.ui.controller.GateOutWindowController;
import com.ipssi.rfid.ui.controller.MainController;
import com.ipssi.rfid.ui.controller.SettingController;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.ipssi.text.validator.TextFieldValidator;
//import com.ipssi.text.validator.TextFieldValidator.ValidationModus;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class MplRfidHandlerWindowController implements Initializable, ControllerI {

	@FXML
	private HBox GATE_IN_HBOX;
	@FXML
	private JFXTextField TEXT_VEHICLE_NAME;
	@FXML
	private JFXComboBox<?> COMBO_MINES;
	@FXML
	private JFXComboBox<?> COMBO_DO_RR;
	@FXML
	private JFXComboBox<?> COMBO_TRANSPORTER;
	@FXML
	private JFXComboBox<?> COMBO_GRADE;
	@FXML
	private JFXTextField TEXT_INVOICE_NO;
	@FXML
	private JFXTextField TEXT_TARE;
	@FXML
	private JFXTextField TEXT_GROSS;
	@FXML
	private JFXTextField TEXT_LR_NO;

	private MainController parent = null;

	private int readerId = 0;
	private RFIDDataHandlerWeb rfidHandler = null;

	boolean isLastTprBlocked = false;
	private Token token = null;

	// private MenuItemInfo menuItemInfo;
	// private Date entryTime = null;
	// private Vehicle vehBean = null;
	// private SyncTprInfo syncTprInfo = null;
	private static final Logger log = Logger.getLogger(MplRfidHandlerWindowController.class.getName());

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
//		TEXT_GROSS.setTextFormatter(new TextFieldValidator(ValidationModus.MAX_INTEGERS, 5).getFormatter());
//		TEXT_TARE.setTextFormatter(new TextFieldValidator(ValidationModus.MAX_INTEGERS, 5).getFormatter());

		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_TRANSPORTER, LovDao.LovItemType.DHAMA_GODIYA_TRANSPORTER,
				Misc.getUndefInt(), Misc.getUndefInt());
		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_GRADE, LovDao.LovItemType.DHAMA_GODIYA_GRADE,
				Misc.getUndefInt(), Misc.getUndefInt());
		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_MINES, LovDao.LovItemType.DHAMA_GODIYA_MINES,
				Misc.getUndefInt(), Misc.getUndefInt());
		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_DO_RR, LovDao.LovItemType.DHAMA_GODIYA_DO_NO,
				Misc.getUndefInt(), Misc.getUndefInt());

		
		rfidReaderStart();

		// } else {
		//
		// }
	}

	private void rfidReaderStart() {
		if (!TokenManager.forceManual) {
			try {
				start();
			} catch (IOException ex) {
				Logger.getLogger(MplRfidHandlerWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else if (TokenManager.forceManual && TokenManager.IS_AUTO_COMPLETE_ON) {
			AutoCompleteTextField autoCompleteTextField = new AutoCompleteTextField(this.parent, TEXT_VEHICLE_NAME,
					LovDao.LovItemType.VEHICLE);
			autoCompleteTextField.setAutoCompleteTextBox();
		}
	}

	private void start() throws IOException {
		if (rfidHandler == null) {
			rfidHandler = new RFIDDataHandlerWeb(1000, readerId, TokenManager.currWorkStationType,
					TokenManager.currWorkStationId, TokenManager.userId);
			rfidHandler.setTagListener(new TAGListener() {
				@Override
				public void manageTag(Connection conn, Token _token, TPRecord tpr, TPStep tps,
						TPRBlockManager _tprBlockManager) {
					try {

						token = _token;
						setTPRecord(conn, _token);
					} catch (Exception ex) {
						Logger.getLogger(MplRfidHandlerWindowController.class.getName()).log(Level.SEVERE, null, ex);
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

	public void setTPRecord(Connection conn, Token tokenData) {
		if (tokenData != null) {
			setVehicleName(tokenData.getVehicleName());
		}
	}

	@FXML
	private void onControlAction(ActionEvent event) {
		String controllId = parent.getSourceId(event);
		handleActionControlOnChange(controllId);
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

	private void handleActionControl(String controlId) {
		if (controlId == null || controlId == "" || controlId.length() == 0) {
			return;
		}
		switch (controlId.toUpperCase()) {
		case "TEXT_VEHICLE_NAME":
			vehicleNameAction();
			break;
		case "COMBO_MINES":
			COMBO_DO_RR.requestFocus();
			break;
		case "COMBO_DO_RR":
			COMBO_GRADE.requestFocus();
			break;

		case "COMBO_GRADE":
			COMBO_TRANSPORTER.requestFocus();
			break;
		case "COMBO_TRANSPORTER":
			TEXT_INVOICE_NO.requestFocus();
			break;
		case "TEXT_INVOICE_NO":
			TEXT_LR_NO.requestFocus();
			break;

		case "TEXT_LR_NO":
			TEXT_GROSS.requestFocus();
			break;
		case "TEXT_GROSS":
			TEXT_TARE.requestFocus();
			break;
		case "TEXT_TARE":
			if (!parent.CONTROL_SAVE.isDisable())
				parent.CONTROL_SAVE.requestFocus();
			else
				parent.CONTROL_CLEAR.requestFocus();
			break;

		default:
			break;
		}
	}

	private void handleActionControlOnChange(String controllId) {
		if (controllId == null || controllId == "" || controllId.length() == 0) {
			return;
		}
		switch (controllId.toUpperCase()) {
		case "TEXT_TARE":
			if (!parent.CONTROL_SAVE.isDisable())
				parent.CONTROL_SAVE.requestFocus();
			else
				parent.CONTROL_CLEAR.requestFocus();
			break;
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

		case "COMBO_MINES":
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
	public void setVehicleName(String vehicleName) {
		TEXT_VEHICLE_NAME.setText(vehicleName != null ? vehicleName.toUpperCase() : "");
	}

	@Override
	public void clearVehicleName() {
		TEXT_VEHICLE_NAME.setText("NO VEHICLE DETECTED");
	}

	@Override
	public void enableManualEntry(boolean enable) {
		// parent.CONTROL_MANUAL.setDisable(enable);
		TEXT_VEHICLE_NAME.setEditable(enable);
		TEXT_VEHICLE_NAME.setFocusTraversable(enable);
		stopRfid();
	}

	@Override
	public void initController(SettingController parent) {
	}

	@Override
	public void requestFocusNextField() {
		Platform.runLater(() -> {
			if (TEXT_VEHICLE_NAME.isEditable())
				TEXT_VEHICLE_NAME.requestFocus();
			else
				COMBO_MINES.requestFocus();
		});
	}

	@Override
	public void setTitle(String title) {

	}

	@Override
	public void dlNoAction() {

	}

	@Override
	public void stopRfid() {
		try {
			if (rfidHandler != null) {
				rfidHandler.stop();
			}

		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(MplRfidHandlerWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
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
	public boolean save() {
		if (token == null) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Connect Reader");
			return false;
		} else if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid vehicle name");
			TEXT_VEHICLE_NAME.requestFocus();
			return false;
		} else if (LovUtils.getIntValue(COMBO_MINES) == Misc.getUndefInt()) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'MINES'.");
			COMBO_MINES.requestFocus();
			return false;
		} else if (LovUtils.getIntValue(COMBO_DO_RR) == Misc.getUndefInt()) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'DO RR'. ");
			COMBO_DO_RR.requestFocus();
			return false;
		} else if (LovUtils.getIntValue(COMBO_GRADE) == Misc.getUndefInt()) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'GRADE'.");
			COMBO_GRADE.requestFocus();
			return false;
		} else if (LovUtils.getIntValue(COMBO_TRANSPORTER) == Misc.getUndefInt()) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Transporter'.");
			COMBO_TRANSPORTER.requestFocus();
			return false;
		}
		// else if (Utils.isNull(TEXT_INVOICE_NO.getText())) {
		// parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter Invoice");
		// TEXT_INVOICE_NO.requestFocus();
		// return false;
		// }
		else if (Utils.isNull(TEXT_LR_NO.getText())) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter LR NO.");
			TEXT_LR_NO.requestFocus();
			return false;
		} else if (Integer.valueOf(TEXT_GROSS.getText()) > TokenManager.min_gross_weight
				|| Integer.valueOf(TEXT_GROSS.getText()) < TokenManager.max_gross_weight) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", "Gross Weight is not in limits");
			TEXT_GROSS.requestFocus();
			return false;
		} else if (Integer.valueOf(TEXT_TARE.getText()) > TokenManager.min_tare_weight
				|| Integer.valueOf(TEXT_TARE.getText()) < TokenManager.max_tare_weight) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", "Tare Weight is not in limits");
			TEXT_TARE.requestFocus();
			return false;
		}

		String data = null;
		String epcId = token.getEpcId();
		;
		String vehicleName = token.getVehicleName();
		int deviceId = 0;
		int userId = parent.userData.getId();
		System.out.println(userId);
		try {
			RFIDHolder dataHolder = new RFIDHolder();
			dataHolder.setEpcId(token.getEpcId());
			dataHolder.setVehicleId(token.getVehicleId());
			dataHolder.setVehicleName(token.getVehicleName());
			dataHolder.setAvgGross(Integer.valueOf(TEXT_GROSS.getText()));
			dataHolder.setAvgTare(Integer.valueOf(TEXT_TARE.getText()));
			dataHolder.setLRID(TEXT_LR_NO.getText());
			dataHolder.setMinesId(LovUtils.getIntValue(COMBO_MINES));
			dataHolder.setDoId(LovUtils.getIntValue(COMBO_DO_RR));
			dataHolder.setTransporterId(LovUtils.getIntValue(COMBO_TRANSPORTER));
			dataHolder.setGrade(LovUtils.getIntValue(COMBO_GRADE));
			dataHolder.setDeviceId(1);
			RFIDTagInfo tagData = dataHolder.createTag(readerId);
			data = Utils.getBinaryStrFromByteArray(tagData.userData);
			boolean isSuccess = HttpClientServicesImpl.sendDataToServer(data, epcId, vehicleName, deviceId, userId);
			if (isSuccess)
				parent.showAlert(Alert.AlertType.CONFIRMATION, "Hello", "Data Saved");

			clearInputs(null, false);
		} catch (IOException ex) {
			Logger.getLogger(MplRfidHandlerWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return true;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}

	@Override
	public void stopSyncTprService() {
		// throw new UnsupportedOperationException("Not supported yet."); //To change
		// body of generated methods, choose Tools | Templates.
	}

	@Override
	public void vehicleNameAction() {
		// throw new UnsupportedOperationException("Not supported yet."); //To change
		// body of generated methods, choose Tools | Templates.
	}

	private void clearAction() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			clearInputs(conn, true);
		} catch (Exception ex) {
			// ex.printStackTrace();
			Logger.getLogger(MplRfidHandlerWindowController.class.getName()).log(Level.SEVERE, null, ex);

		} finally {

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

		token = null;
		clearVehicleName();
		COMBO_MINES.getSelectionModel().clearAndSelect(-1);
		;
		COMBO_DO_RR.getSelectionModel().clearAndSelect(-1);
		;
		COMBO_TRANSPORTER.getSelectionModel().clearAndSelect(-1);
		;
		COMBO_GRADE.getSelectionModel().clearAndSelect(-1);
		;
		TEXT_INVOICE_NO.setText("");
		TEXT_TARE.setText("");
		TEXT_GROSS.setText("");
		TEXT_LR_NO.setText("");// To change body of generated methods, choose Tools | Templates.
	}
}

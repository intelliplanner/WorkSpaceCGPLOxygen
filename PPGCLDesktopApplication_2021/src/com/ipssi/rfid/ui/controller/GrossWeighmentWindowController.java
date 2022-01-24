/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.cgplSap.RecordType;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
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
import com.ipssi.rfid.ui.dao.InvoiceGeneration;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import functions.rfc.sap.document.sap_com.BAPIRET1;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
public class GrossWeighmentWindowController implements Initializable, ControllerI, ActionControllerI {

	@FXML
	private Label TEXT_TPR_ID;
	@FXML
	private AnchorPane GROSS_ROOT_ANCHORPANE;

	@FXML
	private JFXButton weighmentReadButton;

	@FXML
	private JFXCheckBox CHECKBOX_WB1;

	@FXML
	private JFXCheckBox CHECKBOX_WB2;

	@FXML
	private JFXCheckBox CHECKBOX_WB3;

	@FXML
	private JFXCheckBox CHECKBOX_WB4;
	@FXML
	private Label TEXT_TARE_TIME;
	@FXML
	private Label TEXT_TARE_WEIGHT;
	@FXML
	private JFXTextField TEXT_VEHICLE_NAME;

	@FXML
	private JFXTextArea TEXT_NOTE;

	@FXML
	private Label WEIGHMENT_LABEL;
	@FXML
	private Label TEXT_GROSS_WEIGHT;
	@FXML
	private Label TEXT_GROSS_TIME;
	@FXML
	private Label TEXT_NET_WEIGHT;

	// @FXML
	// private Label TEXT_RUNNING_PROCESS;
	// @FXML
	// private Label TEXT_COMPLETED_PROCESS;
	// @FXML
	// private Label TEXT_TOTAL_PROCESS;

	@FXML
	private JFXTextField LABEL_SAP_RESPONSE;

	@FXML
	private JFXComboBox<ComboItem> COMBO_PO_SALES_ORDER;
	@FXML
	private JFXComboBox<ComboItem> COMBO_TRANSPORTER;
	@FXML
	private JFXComboBox<ComboItem> COMBO_CUSTOMER;
	@FXML
	private JFXComboBox<ComboItem> COMBO_LINE_ITEM;

	// @FXML
	// private Label TEXT_PO_SALES_ORDER;
	// @FXML
	// private Label TEXT_CUSTOMER;
	// @FXML
	// private Label TEXT_TRANSPORTER;
	WeighmentReader weighmentReader = null;
	private TPRBlockManager tprBlockManager = null;
	private boolean isTagRead = false;
	private boolean isTpRecordValid = false;
	private int readerId = 0;
	private RFIDDataHandler rfidHandler = null;
	private WeighBridge weighBridge = null;
	private Date entryTime = null;
	Token token = null;
	private TPRecord tpRecord = null;
	private TPStep tpStep = null;
	private ArrayList<Pair<Long, Integer>> readings = null;
	// RecordsetResp recordsetResp = null;
	BAPIRET1 bapiretResp = null;
	private DisconnectionDialog disconnectionDialog = new DisconnectionDialog(
			"Weigh Bridge Disconnected please check connection.....");
	private MainController parent = null;
	private int sapStatus = 0;
	private String sapMessage = null;
	private String sapExInvoice = null;
	private String sapType = "";

	// private SyncTprInfo syncTprInfo = null;
	private static final Logger log = Logger.getLogger(GrossWeighmentWindowController.class.getName());

	private int m_vehicleId = Misc.getUndefInt();
	private boolean isEnterPressed = false;
	private int isNewVehicle = Misc.getUndefInt();

	boolean isWeighmentRead = false;

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
			AutoCompleteTextField autoCompleteTextField = new AutoCompleteTextField(this.parent, TEXT_VEHICLE_NAME,
					LovDao.LovItemType.VEHICLE);
			autoCompleteTextField.setAutoCompleteTextBox();
		}

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
				LovUtils.initializeComboBox(COMBO_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM,
						LovUtils.getIntValue(COMBO_PO_SALES_ORDER), Misc.getUndefInt(), null);
			}
		});

	}

	@FXML
	public void onControlAction(ActionEvent event) {
		String controllId = parent.getSourceId(event);
		handleActionControlOnChange(controllId);
	}

	@FXML
	public void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
			String controllId = parent.getSourceId(event);
			handleActionControl(controllId);
		}
	}

	@FXML
	private void controlItemClicked(MouseEvent event) {
		String controllId = parent.getSourceId(event);
		handleActionControl(controllId);
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
						double gross_txt = Double.parseDouble(wghtData) / 1000;
						WEIGHMENT_LABEL.setText(wghtData);
						TEXT_GROSS_WEIGHT.setText(gross_txt + "");
						calculateNetWeight();
						isWeighmentRead = true;
						stopWeighmentRead();
					} else {
						WEIGHMENT_LABEL.setText(TokenManager.weight_val);
						double gross_txt = Double.parseDouble(TokenManager.weight_val) / 1000;
						TEXT_GROSS_WEIGHT.setText(gross_txt + "");
						calculateNetWeight();
						isWeighmentRead = true;
					}

				});
			}

		});
		weighmentReader.start();

		// if(weighmentReader!=null) {
		// weighmentReader.stop();
		// weighmentReader=null;
		// }
	}

	private void stopWeighmentRead() {
		if (weighmentReader != null) {
			weighmentReader.stop();
			weighmentReader = null;
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
			calculateNetWeight();
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
				double gross_txt = Double.parseDouble(wght) / 1000;
				TEXT_GROSS_WEIGHT.setText(gross_txt + "");

			}
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

	private void changeCheckboxState(int tareWeighmentWbId) {
		CHECKBOX_WB1.setSelected(false);
		CHECKBOX_WB2.setSelected(false);
		CHECKBOX_WB3.setSelected(false);
		CHECKBOX_WB4.setSelected(false);
		if (!Misc.isUndef(tareWeighmentWbId)) {
			if (tareWeighmentWbId == 1)
				CHECKBOX_WB1.setSelected(true);
			else if (tareWeighmentWbId == 2)
				CHECKBOX_WB2.setSelected(true);
			else if (tareWeighmentWbId == 3)
				CHECKBOX_WB3.setSelected(true);
			else if (tareWeighmentWbId == 4)
				CHECKBOX_WB4.setSelected(true);
		}
	}

	/**
	 * Initializes the controller class.
	 */

	@Override
	public boolean save() {

		System.out.println("######### Start GROSS Weighment save() ######### ");

		boolean dataSaved = false;
		Connection conn = null;
		boolean destroyIt = false;
		try {
			if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid vehicle name");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else if (TEXT_NET_WEIGHT.getText() == null || TEXT_NET_WEIGHT.getText().length() == 0
					|| Misc.getParamAsDouble(TEXT_NET_WEIGHT.getText()) <= 0) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Net Weight is always greater than 0");
			} else if (LovUtils.getIntValue(COMBO_CUSTOMER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please Select 'Customer'.");
				COMBO_CUSTOMER.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_PO_SALES_ORDER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Po Sales Order'. ");
				COMBO_PO_SALES_ORDER.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_LINE_ITEM) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Line Item'.");
				COMBO_LINE_ITEM.requestFocus();
				return false;
			} else if (LovUtils.getIntValue(COMBO_TRANSPORTER) == Misc.getUndefInt()) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", " Please select 'Transporter'.");
				COMBO_TRANSPORTER.requestFocus();
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
					tpRecord.setLoadGross(captureWeight);
				}

				double netWtCalc = (captureWeight - tpRecord.getLoadTare());

				String salesOrder = LovUtils.getTextValue(COMBO_PO_SALES_ORDER);
				if (salesOrder.contains("@"))
					salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
				Pair<Boolean, Double> pairVal = GateInDao.isSalesOrderQuantityExist(conn, salesOrder,
						LovUtils.getTextValue(COMBO_LINE_ITEM), netWtCalc);

				if (pairVal != null && !pairVal.first) {
					parent.showAlert(Alert.AlertType.ERROR, "Message", "Sales Order Exhausted");
					return false;
				}

				tpRecord.setTransporterId(LovUtils.getIntValue(COMBO_TRANSPORTER));
				tpRecord.setTransporterCode(LovUtils.getTextValue(COMBO_TRANSPORTER));
				tpRecord.setConsignee(LovUtils.getIntValue(COMBO_CUSTOMER));
				tpRecord.setConsigneeName(LovUtils.getTextValue(COMBO_CUSTOMER));
				tpRecord.setDoId(LovUtils.getIntValue(COMBO_LINE_ITEM));
				tpRecord.setDoNumber(LovUtils.getTextValue(COMBO_LINE_ITEM));
				tpRecord.setProductCode(LovUtils.getTextValue(COMBO_PO_SALES_ORDER));

				bapiretResp = getSapResponse();
				if (bapiretResp != null) {
					sapMessage = bapiretResp.getMESSAGE();
					sapExInvoice = bapiretResp.getMESSAGE_V1();
					sapType = bapiretResp.getTYPE();
					sapStatus = sapType.equalsIgnoreCase("S") ? RecordType.MessageType.SUCCESS
							: RecordType.MessageType.FAILED;
				} else {
					sapStatus = RecordType.MessageType.NO_RESPONSE;
				}

				TokenManager.currWorkStationId = CHECKBOX_WB1.isSelected() ? 1
						: CHECKBOX_WB2.isSelected() ? 2 : CHECKBOX_WB3.isSelected() ? 3 : 4;
				updateTPR(conn, captureWeight);
				int stepId = InsertTPRStep(conn, false, false);

				if (sapType.equalsIgnoreCase("S")) {
					double totalLapseQuatity = pairVal.second + netWtCalc;
					GateInDao.updateCGPLSalesOrder(conn, salesOrder,
							LovUtils.getTextValue(COMBO_LINE_ITEM), totalLapseQuatity);
				}

				conn.commit();
				String msg = "";
				if (sapType.equalsIgnoreCase("S")) {
					msg = "Data Saved \n" + sapMessage + "\nInvoice No: " + sapExInvoice;
					LABEL_SAP_RESPONSE.setText("Last Invoice Number: " + sapExInvoice);
				} else if (sapType.equalsIgnoreCase("E")) {
					msg = "Data Saved \n " + sapMessage;
				} else {
					msg = "Data Saved";
				}

				parent.showAlert(Alert.AlertType.INFORMATION, "Message", msg);
				clearInputs(conn, false);
				dataSaved = true;
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
		System.out.println("#########  End GROSS Weighment save() ######### ");
		return dataSaved;
	}

	private BAPIRET1 getSapResponseTest() {
		InvoiceGeneration invoiceGenerationObj = new InvoiceGeneration();
		BAPIRET1 response = null;
		try {
			System.out.println("########## Start getSapResponse() ########");
			String _salesOrder = LovDao.getText(TokenManager.portNodeId, LovItemType.PO_SALES_ORDER, tpRecord.getDoId(),
					Misc.getUndefInt());
			String _shipTo = LovDao.getText(TokenManager.portNodeId, LovItemType.CUSTOMER, tpRecord.getConsignee(),
					Misc.getUndefInt());
			String _transporter = LovDao.getText(TokenManager.portNodeId, LovItemType.TRANSPORTER,
					tpRecord.getTransporterId(), Misc.getUndefInt());

			double itmNumber = Misc.getParamAsDouble(LovDao.getText(TokenManager.portNodeId, LovItemType.PO_LINE_ITEM,
					tpRecord.getDoId(), Misc.getUndefInt()));
			int IntValue = (int) Math.round(itmNumber);
			String _itmNumber = IntValue + "";
			String _inTime = UIConstant.timeFormatWithSec.format(tpRecord.getComboStart());
			String _outTime = UIConstant.timeFormatWithSec.format(new Date());
			double _grossWt = Misc.getParamAsDouble(WEIGHMENT_LABEL.getText()) / 1000;
			BigDecimal _netWt = (TEXT_NET_WEIGHT.getText() != null && TEXT_NET_WEIGHT.getText().length() > 0)
					? new BigDecimal(TEXT_NET_WEIGHT.getText())
					: new BigDecimal(0);
			_netWt = _netWt.setScale(2, BigDecimal.ROUND_DOWN);
			String lrNo = tpRecord.getLrNo();
			String lrDate = "09-06-2021";// tprReportData.getLrDate() != null ?
											// UIConstant.dateFormat2.format(tprReportData.getLrDate()) : "";
			System.out.println("[Data: SalesOrder:" + _salesOrder + ",NetWt: " + _netWt + ", Transporter: "
					+ _transporter + ", VehicleName: " + tpRecord.getVehicleName() + ", _shipTo:" + _shipTo
					+ ",HSN_NO: " + TokenManager.HSN_NO + ", TareWt: " + tpRecord.getLoadTare() + ",grossWt: "
					+ Misc.getPrintableDouble(_grossWt) + ",_inTime: " + _inTime + ",OutTime: " + _outTime
					+ ", ItmNumber: " + _itmNumber + " ,lrNo: " + lrNo + ",lrDate: " + lrDate + " ]");

			response = invoiceGenerationObj.getSapRespTest(tpRecord.getVehicleName(),
					Integer.toString(tpRecord.getTprId()), _salesOrder, _shipTo, _transporter, _itmNumber, _inTime,
					_outTime, Misc.getPrintableDouble(tpRecord.getLoadTare()),
					Misc.getPrintableDouble(tpRecord.getLoadGross()), _netWt, TokenManager.HSN_NO, lrNo, lrDate);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("########## END getSapResponse() ########");
		return response;
	}

	private BAPIRET1 getSapResponse() {
		InvoiceGeneration invoiceGenerationObj = new InvoiceGeneration();
		BAPIRET1 response = null;
		try {
			System.out.println("########## Start getSapResponse() ########");
			String _salesOrder = LovDao.getText(TokenManager.portNodeId, LovItemType.PO_SALES_ORDER, tpRecord.getDoId(),
					Misc.getUndefInt());
			String _shipTo = LovDao.getText(TokenManager.portNodeId, LovItemType.CUSTOMER, tpRecord.getConsignee(),
					Misc.getUndefInt());
			String _transporter = LovDao.getText(TokenManager.portNodeId, LovItemType.TRANSPORTER,
					tpRecord.getTransporterId(), Misc.getUndefInt());

			double itmNumber = Misc.getParamAsDouble(LovDao.getText(TokenManager.portNodeId, LovItemType.PO_LINE_ITEM,
					tpRecord.getDoId(), Misc.getUndefInt()));
			int IntValue = (int) Math.round(itmNumber);
			String _itmNumber = IntValue + "";
			String _inTime = UIConstant.timeFormatWithSec.format(tpRecord.getComboStart());
			String _outTime = UIConstant.timeFormatWithSec.format(new Date());
			double _grossWt = tpRecord.getLoadGross() / 1000;
			
			BigDecimal _netWt = (TEXT_NET_WEIGHT.getText() != null && TEXT_NET_WEIGHT.getText().length() > 0)
					? new BigDecimal(TEXT_NET_WEIGHT.getText())
					: new BigDecimal(0);
			_netWt = _netWt.setScale(2, BigDecimal.ROUND_DOWN);
			String lrNo = tpRecord.getLrNo();
			String lrDate = tpRecord.getLrDate() != null ? tpRecord.getLrDate().toString() : "";
			System.out.println("[Data: SalesOrder:" + _salesOrder + ",NetWt: " + _netWt + ", Transporter: "
					+ _transporter + ", VehicleName: " + tpRecord.getVehicleName() + ", _shipTo:" + _shipTo
					+ ",HSN_NO: " + TokenManager.HSN_NO + ", TareWt: " + tpRecord.getLoadTare() + ",grossWt: "
					+ Misc.getPrintableDouble(tpRecord.getLoadGross()) + ",_inTime: " + _inTime + ",OutTime: " + _outTime
					+ ", ItmNumber: " + _itmNumber + " ,lrNo: " + lrNo + ",lrDate: " + lrDate + " ]");

			response = invoiceGenerationObj.getSapResp(tpRecord.getVehicleName(), Integer.toString(tpRecord.getTprId()),
					_salesOrder, _shipTo, _transporter, _itmNumber, _inTime, _outTime,
					Misc.getPrintableDouble(tpRecord.getLoadTare()), Misc.getPrintableDouble(tpRecord.getLoadGross()),
					_netWt, TokenManager.HSN_NO, lrNo, lrDate);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("########## END getSapResponse() ########");
		return response;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
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
					return 0;
				}

			});
		}
		rfidHandler.start();
	}

	public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
		System.out.println("######### Weigh bridge In setTPRecord  ########");
		try {
			tpRecord = tpr;
			if (tpRecord != null) {
				System.out.println("TPR Record Create");
				if (true) {
					isTpRecordValid = true;
					isTagRead = token != null ? token.isReadFromTag() : false;
					if (token == null && tpRecord.getEarliestLoadWbInEntry() != null) {
						System.out.println("Entry Time 1st");
						entryTime = tpRecord.getEarliestLoadWbInEntry();
					} else if (token != null && tpRecord.getEarliestLoadWbInEntry() == null) {
						System.out.println("Entry Time 2nd :" + token.getLastSeen());
						if (token.getLastSeen() != Misc.getUndefInt()) {
							entryTime = new Date(token.getLastSeen());
						} else {
							entryTime = new Date();
						}
					} else if (token != null && tpRecord.getEarliestLoadWbInEntry() != null) {
						if (token.getLastSeen() > Utils.getDateTimeLong(tpRecord.getEarliestLoadWbInEntry())) {
							System.out.println("Entry Time 3rd :" + token.getLastSeen());
							if (token.getLastSeen() != Misc.getUndefInt()) {
								entryTime = new Date(token.getLastSeen());
							} else {
								entryTime = new Date();
							}
						} else {
							entryTime = new Date();
						}
					} else {
						entryTime = new Date();
					}
					System.out.println("Entry Time :" + entryTime);

					Platform.runLater(() -> {
						// LABEL_SAP_RESPONSE.setText("");
						setVehicleName(tpRecord.getVehicleName());
						TEXT_TPR_ID.setText(Misc.getPrintableInt(tpRecord.getTprId()));

						LovUtils.initializeComboBox(COMBO_CUSTOMER, LovDao.LovItemType.CUSTOMER,
								tpRecord.getConsignee(), Misc.getUndefInt());
						LovUtils.initializeComboBox(COMBO_TRANSPORTER, LovDao.LovItemType.TRANSPORTER,
								tpRecord.getTransporterId(), Misc.getUndefInt());
						String selectedSalesOrder = tpRecord.getProductCode() + "@" + tpRecord.getDoNumber();
						LovUtils.initializeComboBox(COMBO_PO_SALES_ORDER, LovDao.LovItemType.PO_SALES_ORDER,
								Misc.getUndefInt(), tpRecord.getConsignee(), selectedSalesOrder);

						LovUtils.initializeComboBox(COMBO_LINE_ITEM, LovDao.LovItemType.PO_LINE_ITEM,
								tpRecord.getDoId(), Misc.getUndefInt(), tpRecord.getProductCode());

						TEXT_TARE_WEIGHT.setText(Misc.getPrintableDouble(tpRecord.getLoadTare()));
						TEXT_TARE_TIME.setText(tpRecord.getLatestLoadWbOutExit() != null
								? new SimpleDateFormat("HH:mm").format(tpRecord.getLatestLoadWbOutExit().getTime())
								: "");
						TEXT_GROSS_TIME.setText(new SimpleDateFormat("HH:mm").format(new Date().getTime()));
						TEXT_NOTE.setText(tpRecord.getConsigneeRefDoc());
						TEXT_GROSS_WEIGHT.setText(Misc.getPrintableDouble(
								(Double.valueOf(Misc.getParamAsInt(WEIGHMENT_LABEL.getText())) / 1000)));
						calculateNetWeight();
						setBlockingStatus();
						requestFocusNextField();
					});

					changeCheckboxState(tpRecord.getPrevTpStep());

					// initializeWeighbridge();
				}

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

	private void clearInputs(Connection conn, boolean clearToken) {
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		stopWeighmentRead();
		tpRecord = null;
		entryTime = null;
		token = null;
		tpStep = null;
		tprBlockManager = null;
		isTagRead = false;
		WEIGHMENT_LABEL.setText(TokenManager.weight_val);
		TEXT_VEHICLE_NAME.setText("");
		isTpRecordValid = false;
		m_vehicleId = Misc.getUndefInt();
		isEnterPressed = false;
		isNewVehicle = Misc.getUndefInt();
		sapMessage = null;
		sapExInvoice = null;
		sapType = "";
		sapStatus = 0;
		weighmentReader = null;
		TEXT_TPR_ID.setText("");
		TEXT_NOTE.setText("");
		TEXT_TARE_WEIGHT.setText("");
		TEXT_TARE_TIME.setText("");
		TEXT_GROSS_WEIGHT.setText("0");
		TEXT_GROSS_TIME.setText("");
		TEXT_NET_WEIGHT.setText("");
		bapiretResp = null;
		CHECKBOX_WB1.setSelected(false);
		CHECKBOX_WB2.setSelected(false);
		CHECKBOX_WB3.setSelected(false);
		CHECKBOX_WB4.setSelected(false);
		// TEXT_RUNNING_PROCESS.setText("");
		// TEXT_COMPLETED_PROCESS.setText("");
		// TEXT_TOTAL_PROCESS.setText("");
		COMBO_PO_SALES_ORDER.getSelectionModel().clearAndSelect(0);
		COMBO_LINE_ITEM.getSelectionModel().clearAndSelect(0);
		COMBO_CUSTOMER.getSelectionModel().clearAndSelect(0);
		COMBO_TRANSPORTER.getSelectionModel().clearAndSelect(0);
		isWeighmentRead = false;
		// parent.setControllerDisable(false);
	}

	@Override
	public void clearInputs() {
		clearAction();
	}

	private void updateTPR(Connection conn, double captureWeight) throws Exception {
		updateTPR(conn, captureWeight, false);
	}

	private void updateTPR(Connection conn, double captureWeight, boolean isDeny) throws Exception {
		java.util.Date curr = new java.util.Date();
		if (!isDeny) {
			tpRecord.setLoadGross(captureWeight);
			tpRecord.setPreStepDate(curr);
			tpRecord.setUpdatedBy(TokenManager.userId);
			tpRecord.setUpdatedOn(curr);
			tpRecord.setPreStepType(TokenManager.currWorkStationType);
			tpRecord.setPrevTpStep(TokenManager.currWorkStationId);
			tpRecord.setNextStepType(TokenManager.nextWorkStationType);

			tpRecord.setTransporterId(LovUtils.getIntValue(COMBO_TRANSPORTER));
			tpRecord.setTransporterCode(LovUtils.getTextValue(COMBO_TRANSPORTER));
			tpRecord.setConsignee(LovUtils.getIntValue(COMBO_CUSTOMER));
			tpRecord.setConsigneeName(LovUtils.getTextValue(COMBO_CUSTOMER));
			tpRecord.setDoId(LovUtils.getIntValue(COMBO_LINE_ITEM));
			tpRecord.setDoNumber(LovUtils.getTextValue(COMBO_LINE_ITEM));
			String salesOrder = LovUtils.getTextValue(COMBO_PO_SALES_ORDER);
			if (salesOrder.contains("@"))
				salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
			tpRecord.setProductCode(salesOrder);
			tpRecord.setReportingStatus(sapStatus);
			tpRecord.setMessage(sapMessage);
			tpRecord.setExInvoice(sapExInvoice);

			if (tpRecord.getComboStart() == null) {
				tpRecord.setComboStart(new Date());
			}
			tpRecord.setComboEnd(new Date());

			if (TokenManager.closeTPR) {
				tpRecord.setTprStatus(Status.TPR.CLOSE);
			}

			tpRecord.setLoadWbInName(TokenManager.userName);
			tpRecord.setEarliestLoadWbInEntry(entryTime);
		}

		tpRecord.setLatestLoadWbInExit(new Date());

		TPRInformation.insertUpdateTpr(conn, tpRecord);

	}

	private int InsertTPRStep(Connection conn, boolean isDeny) throws Exception {
		return InsertTPRStep(conn, isDeny, false);
	}

	private int InsertTPRStep(Connection conn, boolean isDeny, boolean repeatProcess) throws Exception {
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
			// tpStep.setTprId(tpRecord.getTprId());
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
		Platform.runLater(() -> {
			if (TEXT_VEHICLE_NAME.isEditable()) {
				TEXT_VEHICLE_NAME.requestFocus();
			} else {
				COMBO_CUSTOMER.requestFocus();
			}

		});
	}

	@Override
	public void setTitle(String title) {

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

	private void calculateNetWeight() {
		Platform.runLater(() -> {
			if (tpRecord != null && tpRecord.getLoadTare() != Misc.getUndefDouble()
					&& !Utils.isNull(WEIGHMENT_LABEL.getText())) {
				double mplGross = Double.valueOf(WEIGHMENT_LABEL.getText());
				if (!Misc.isUndef(mplGross)) {
					double Wb_Net_Wt = (mplGross / 1000) - tpRecord.getLoadTare();
					TEXT_NET_WEIGHT.setText(Misc.getPrintableDouble(Wb_Net_Wt));
				}
			}
		});
	}

	@Override
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub
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
			CHECKBOX_WB2.requestFocus();
			break;
		case "CHECKBOX_WB2":
			CHECKBOX_WB3.requestFocus();
			break;
		case "CHECKBOX_WB3":
			CHECKBOX_WB4.requestFocus();
			break;
		case "CHECKBOX_WB4":
			parent.CONTROL_SAVE.requestFocus();
			break;
		case "WEIGHMENTREADBUTTON":
			initializeWeighbridgeNew();
		default:
			break;
		}
	}
	// public void handleActionControl(Button control) {
	// if (control == null || control.isDisable() || !control.isVisible()) {
	// return;
	// }
	// String controlId = control.getId().toUpperCase();
	// switch (controlId.toUpperCase()) {
	// case "TEXT_VEHICLE_NAME":
	// vehicleAction();
	// break;
	// case "COMBO_CUSTOMER":
	// COMBO_PO_SALES_ORDER.requestFocus();
	// break;
	// case "COMBO_PO_SALES_ORDER":
	// COMBO_LINE_ITEM.requestFocus();
	// break;
	//
	// case "COMBO_LINE_ITEM":
	// COMBO_TRANSPORTER.requestFocus();
	// break;
	// case "COMBO_TRANSPORTER":
	// CHECKBOX_WB1.requestFocus();
	// break;
	// case "CHECKBOX_WB1":
	// CHECKBOX_WB2.requestFocus();
	// break;
	// case "CHECKBOX_WB2":
	// CHECKBOX_WB3.requestFocus();
	// break;
	// case "CHECKBOX_WB3":
	// CHECKBOX_WB4.requestFocus();
	// break;
	// case "CHECKBOX_WB4":
	// weighmentReadButton.requestFocus();
	// break;
	//
	// case "WEIGHMENTREADBUTTON":
	// initializeWeighbridge();
	// break;
	//
	// default:
	// break;
	// }
	// }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.ipssi.beans.ExcelBean;
import com.ipssi.cgplSap.RecordType;
import com.ipssi.cgplSap.RecordType.MessageType;
import com.ipssi.cgplSap.RecordsetResp;
import com.ipssi.cgplSap.SapIntegration;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.TprReportData;
import com.ipssi.rfid.constant.PropertyManagerNew;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.Criteria;
import com.ipssi.rfid.excel.ExcelWriterAutoFlush;
import com.ipssi.rfid.excel.ExcelWriterManualFlush;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.controller.service.ControllerI;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.dao.InvoiceGeneration;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovType;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import functions.rfc.sap.document.sap_com.BAPIRET1;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Virendra Gupta
 */

public class ReportViewController implements Initializable, ControllerI {

	private static final Logger log = Logger.getLogger(ReportViewController.class.getName());
	private static final String logDir = PropertyManagerNew.BASE + "report" + File.separator;
	static {
		try {
			new File(logDir).mkdirs();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	private JFXButton CONTROL_DOWNLOAD;

	@FXML
	private DatePicker START_DATE_PICKER;

	@FXML
	private DatePicker END_DATE_PICKER;

	@FXML
	private TableView TPR_REPORT_TABLE_VIEW;

	@FXML
	private JFXComboBox COMBO_INVOICE_STATUS;

	@FXML
	private JFXComboBox COMBO_START_HOUR;
	@FXML
	private JFXComboBox COMBO_END_HOUR;
	@FXML
	private JFXComboBox COMBO_START_MIN;
	@FXML
	private JFXComboBox COMBO_END_MIN;
	@FXML
	private JFXTextField LABEL_SAP_RESPONSE;
	@FXML
	private TableColumn tprId;
	@FXML
	private TableColumn tprStatus;

	@FXML
	private TableColumn invoiceNo;
	@FXML
	private TableColumn customer;
	@FXML
	private TableColumn lineItem;
	@FXML
	private TableColumn salesOrder;
	@FXML
	private TableColumn transporterName;
	@FXML
	private TableColumn invoiceStatus;

	@FXML
	private TableColumn vehicleName;

	@FXML
	private TableColumn loadGross;
	@FXML
	private TableColumn loadTare;

	@FXML
	private TableColumn NET_WEIGHT;

	@FXML
	private TableColumn actionControl;

	@FXML
	private JFXButton CONTROL_SEARCH;

	@FXML
	private Pane searchBoxIId;

	@FXML
	private JFXTextField TEXT_VEHICLE_NAME;
	// @FXML
	// private JFXTextField TEXT_SALES_ORDER;
	@FXML
	private JFXTextField TEXT_TPR_ID;

	@FXML
	private JFXComboBox COMBO_OPEN_CLOSE;

	// @FXML
	// private JFXComboBox COMBO_PROCESS_STATUS;

	@FXML
	private JFXComboBox COMBO_SALES_ORDER;

	@FXML
	private TableColumn GateInTime;
	@FXML
	private TableColumn TareTime;
	@FXML
	private TableColumn GrossTime;
	@FXML
	private TableColumn GateOutTime;

	public MainController parent = null;

	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd");
	DateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	BAPIRET1 bapiretResp = null;
	int sapStatus = 0;
	String sapMessage = null;
	String sapExInvoice = null;
	String sapType = "";
	Token token = null;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		START_DATE_PICKER.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		END_DATE_PICKER.setValue(GateInDao.NOW_LOCAL_DATE(dateFormatter));
		setDateConverter(START_DATE_PICKER);
		setDateConverter(END_DATE_PICKER);

		LovUtils.initializeComboBox(COMBO_START_HOUR, LovDao.LovItemType.HOUR, 0, Misc.getUndefInt());

		LovUtils.initializeComboBox(COMBO_START_MIN, LovDao.LovItemType.MINUTE, 0, Misc.getUndefInt());
		LovUtils.initializeComboBox(COMBO_END_HOUR, LovDao.LovItemType.HOUR, 23, Misc.getUndefInt());

		LovUtils.initializeComboBox(COMBO_END_MIN, LovDao.LovItemType.MINUTE, 59, Misc.getUndefInt());

		LovUtils.setLov(null, TokenManager.portNodeId, COMBO_OPEN_CLOSE, LovDao.LovItemType.OPEN_CLOSE, -1,
				Misc.getUndefInt());

		LovUtils.initializeComboBox(COMBO_SALES_ORDER, LovDao.LovItemType.SALES_ORDER, Misc.getUndefInt(),
				Misc.getUndefInt());
		LovUtils.initializeComboBox(COMBO_INVOICE_STATUS, LovDao.LovItemType.INVOICE_STATUS, -1, Misc.getUndefInt());

		tprId.setCellValueFactory(new PropertyValueFactory("tprId"));
		tprStatus.setCellValueFactory(new PropertyValueFactory("tprStatus"));
		invoiceStatus.setCellValueFactory(new PropertyValueFactory("invoiceStatus"));
		invoiceNo.setCellValueFactory(new PropertyValueFactory("invoiceNo"));

		customer.setCellValueFactory(new PropertyValueFactory("customer"));
		lineItem.setCellValueFactory(new PropertyValueFactory("lineItem"));
		salesOrder.setCellValueFactory(new PropertyValueFactory("salesOrder"));
		transporterName.setCellValueFactory(new PropertyValueFactory("transporterName"));

		vehicleName.setCellValueFactory(new PropertyValueFactory("vehicleName"));
		loadTare.setCellValueFactory(new PropertyValueFactory("loadTare"));
		loadGross.setCellValueFactory(new PropertyValueFactory("loadGross"));

		NET_WEIGHT.setCellValueFactory(new PropertyValueFactory("netWt"));

		GateInTime.setCellValueFactory(new PropertyValueFactory("GateInTime"));

		TareTime.setCellValueFactory(new PropertyValueFactory("TareTime"));

		GrossTime.setCellValueFactory(new PropertyValueFactory("GrossTime"));

		GateOutTime.setCellValueFactory(new PropertyValueFactory("GateOutTime"));

		actionControl.setCellValueFactory(new PropertyValueFactory("actionButton"));

		TPR_REPORT_TABLE_VIEW.getColumns().setAll(actionControl, tprId, vehicleName, salesOrder, customer, lineItem,
				transporterName, invoiceNo, invoiceStatus, tprStatus, loadTare, loadGross, NET_WEIGHT, GateInTime,
				TareTime, GrossTime, GateOutTime);
		// TPR_REPORT_TABLE_VIEW.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
	}

	private void setDefaultEndDate() {
		END_DATE_PICKER.setConverter(new javafx.util.StringConverter<LocalDate>() {

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

	@FXML
	private void onControlKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			Button control = ((Button) event.getSource());
			handleActionControl(control);
		}
	}

	@FXML
	private void controlItemClicked(MouseEvent event) {
		Button control = ((Button) event.getSource());
		handleActionControl(control);
	}

	private void handleActionControl(Button control) {

		if (control == null || control.isDisable() || !control.isVisible()) {
			return;
		}
		String controlId = control.getId().toUpperCase();
		switch (controlId) {

		case "CONTROL_SEARCH":
			searchAction();
			// openDialogWindow();
			break;
		case "CONTROL_DOWNLOAD":
			downloadAction();
			// openDialogWindow();
			break;
		default:
			break;
		}

	}

	private void downloadAction() {
		Connection conn = null;
		boolean destroyIt = false;
		Criteria cr = null;
		TPRecord tpData = null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (START_DATE_PICKER.getValue() == null || END_DATE_PICKER.getValue() == null) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select Start and End date");
				return;
			} else if (Misc.isUndef(LovUtils.getIntValue(COMBO_START_HOUR))
					|| Misc.isUndef(LovUtils.getIntValue(COMBO_START_MIN))
					|| Misc.isUndef(LovUtils.getIntValue(COMBO_END_HOUR))
					|| Misc.isUndef(LovUtils.getIntValue(COMBO_END_MIN))) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select Start and End Time");
				return;
			} else {

				String startDt = START_DATE_PICKER.getValue().toString() + " " + LovUtils.getTextValue(COMBO_START_HOUR)
						+ ":" + LovUtils.getTextValue(COMBO_START_MIN);
				String endDt = END_DATE_PICKER.getValue().toString() + " " + LovUtils.getTextValue(COMBO_END_HOUR) + ":"
						+ LovUtils.getTextValue(COMBO_END_MIN);
				// Date startDate = UIConstant.displayFormat2.parse(startDt);
				// Date endDate = UIConstant.displayFormat2.parse(endDt);

				cr = new Criteria(TPRecord.class);
				String clause = "combo_start > '" + startDt + "' and combo_end < '" + endDt + "'";
				cr.setWhrClause(clause);
				if (!Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
					tpData.setVehicleName(TEXT_VEHICLE_NAME.getText());
				}
				if (!Misc.isUndef(LovUtils.getIntValue(COMBO_SALES_ORDER))) {
					tpData.setProductCode(LovUtils.getTextValue(COMBO_SALES_ORDER));
				}
				if (LovUtils.getIntValue(COMBO_INVOICE_STATUS) != LovType.ANY) {
					tpData.setReportingStatus(LovUtils.getIntValue(COMBO_INVOICE_STATUS));
				}
				if (!Utils.isNull(TEXT_TPR_ID.getText())) {
					tpData.setTprId(Misc.getParamAsInt(TEXT_TPR_ID.getText()));
				}
				if (LovUtils.getIntValue(COMBO_OPEN_CLOSE) != LovType.ANY) { // tpr_status = openClose
					tpData.setTprStatus(LovUtils.getIntValue(COMBO_OPEN_CLOSE));
				}

				ArrayList<Object> list = GateInDao.getTransactionData(conn, tpData, cr);
				if (list == null)
					return;
				ArrayList<ExcelBean> exlBeanList = getInitializeExcelData(list);

				writeToExcelAutoFlush(exlBeanList);
				// writeToExcelManualFlush(exlBeanList);
				parent.showAlert(Alert.AlertType.CONFIRMATION, "Message", "Report Downloaded ");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeToExcelManualFlush(ArrayList<ExcelBean> exlBeanList) {
		final long manualFlushStartTime = System.currentTimeMillis();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH").format(new Date());
		String fileName = logDir + "CGPL_REPORT_MANUAL_FLUSH_" + timeStamp + ".xlsx";
		// fileName = "C:/workspace/Excel-manual-flush.xlsx";
		// fileName = "C:" + File.separator + "ipssi" + File.separator +"excel"+
		// File.separator + "Excel-manual-flush.xlsx";
		ExcelWriterManualFlush manualFlush = new ExcelWriterManualFlush();
		manualFlush.writeToExcelManualFlush(fileName, exlBeanList);

		final long manualFlushEndTime = System.currentTimeMillis();

		final long manualFlushExeTime = manualFlushEndTime - manualFlushStartTime;

		final long manualFlushHr = TimeUnit.MILLISECONDS.toHours(manualFlushExeTime);
		final long manualFlushMin = TimeUnit.MILLISECONDS.toMinutes(manualFlushExeTime)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(manualFlushExeTime));
		final long manualFlushSec = TimeUnit.MILLISECONDS.toSeconds(manualFlushExeTime)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(manualFlushExeTime));
		final long manualFlushMs = TimeUnit.MILLISECONDS.toMillis(manualFlushExeTime)
				- TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(manualFlushExeTime));

		System.out.println(String.format(
				"Total time taken to execute " + exlBeanList.size()
						+ " records using manual flush: %d Hours %d Minutes %d Seconds %d Milliseconds",
				manualFlushHr, manualFlushMin, manualFlushSec, manualFlushMs));

	}

	private void writeToExcelAutoFlush(ArrayList<ExcelBean> exlBeanList) {
		final long autoFlushStartTime = System.currentTimeMillis();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String fileName = logDir + "CGPL_REPORT_AUTO_FLUSH_" + timeStamp + ".xlsx";
		ExcelWriterAutoFlush autoFlush = new ExcelWriterAutoFlush();
		autoFlush.writeToExcelAutoFlush(fileName, exlBeanList);

		final long autoFlushEndTime = System.currentTimeMillis();

		final long autoFlushExeTime = autoFlushEndTime - autoFlushStartTime;

		final long hr = TimeUnit.MILLISECONDS.toHours(autoFlushExeTime);
		final long min = TimeUnit.MILLISECONDS.toMinutes(autoFlushExeTime)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(autoFlushExeTime));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(autoFlushExeTime)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(autoFlushExeTime));
		final long ms = TimeUnit.MILLISECONDS.toMillis(autoFlushExeTime)
				- TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(autoFlushExeTime));

		System.out.println(String.format(
				"Total time taken to execute " + exlBeanList.size()
						+ " records using auto flush: %d Hours %d Minutes %d Seconds %d Milliseconds",
				hr, min, sec, ms));

	}

	private ArrayList<ExcelBean> getInitializeExcelData(ArrayList<Object> dataList) {
		ExcelBean exl = null;
		ArrayList<ExcelBean> excelList = null;
		for (int i = 0, is = dataList == null ? 0 : dataList.size(); i < is; i++) {
			TPRecord tpr = (TPRecord) dataList.get(i);
			if (excelList == null)
				excelList = new ArrayList<ExcelBean>();
			String netWt = calculateNetWeight(tpr);
			exl = new ExcelBean(Integer.toString(tpr.getTprId()), MessageType.getStr(tpr.getReportingStatus()),
					tpr.getExInvoice(), tpr.getConsigneeName(), tpr.getDoNumber(), tpr.getProductCode(),
					tpr.getTransporterCode(), LovType.TprStatus.getStr(tpr.getTprStatus()), tpr.getVehicleName(),
					Misc.getPrintableDouble(tpr.getLoadTare()), Misc.getPrintableDouble(tpr.getLoadGross()),
					netWt,
					GateInDao.convertDate(tpr.getLatestLoadGateInExit(),outFormat),
					GateInDao.convertDate(tpr.getLatestLoadWbOutExit(),outFormat),
					GateInDao.convertDate(tpr.getLatestLoadWbInExit(),outFormat),
					GateInDao.convertDate(tpr.getLatestLoadGateOutExit(),outFormat)
					);
			excelList.add(exl);
		}

		return excelList;

	}

	public void searchAction() {
		clearInputs(null, false);
		Connection conn = null;
		TPRecord tprD = null;
		Criteria cr = null;
		boolean destroy = false;

		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			tprD = new TPRecord();

			if (START_DATE_PICKER.getValue() == null || END_DATE_PICKER.getValue() == null) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select Start and End date");
				return;
			} else if (Misc.isUndef(LovUtils.getIntValue(COMBO_START_HOUR))
					|| Misc.isUndef(LovUtils.getIntValue(COMBO_START_MIN))
					|| Misc.isUndef(LovUtils.getIntValue(COMBO_END_HOUR))
					|| Misc.isUndef(LovUtils.getIntValue(COMBO_END_MIN))) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select Start and End Time");
				return;
			} else {

				String startDt = START_DATE_PICKER.getValue().toString() + " " + LovUtils.getTextValue(COMBO_START_HOUR)
						+ ":" + LovUtils.getTextValue(COMBO_START_MIN);
				String endDt = END_DATE_PICKER.getValue().toString() + " " + LovUtils.getTextValue(COMBO_END_HOUR) + ":"
						+ LovUtils.getTextValue(COMBO_END_MIN);
				// Date startDate = UIConstant.displayFormat2.parse(startDt);
				// Date endDate = UIConstant.displayFormat2.parse(endDt);

				cr = new Criteria(TPRecord.class);
				String clause = "combo_start > '" + startDt + "' and combo_end < '" + endDt + "'";
				cr.setWhrClause(clause);
				// tpr.setComboStart(startDate);
				// tpr.setComboEnd(endDate);
			}

			if (!Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				tprD.setVehicleName(TEXT_VEHICLE_NAME.getText());
			}
			if (!Misc.isUndef(LovUtils.getIntValue(COMBO_SALES_ORDER))) {
				String salesOrder = LovUtils.getTextValue(COMBO_SALES_ORDER);
				salesOrder = salesOrder.substring(0, salesOrder.lastIndexOf("@"));
				tprD.setProductCode(salesOrder);
			}
			if (LovUtils.getIntValue(COMBO_INVOICE_STATUS) != LovType.ANY) {
				tprD.setReportingStatus(LovUtils.getIntValue(COMBO_INVOICE_STATUS));
			}
			if (!Utils.isNull(TEXT_TPR_ID.getText())) {
				tprD.setTprId(Misc.getParamAsInt(TEXT_TPR_ID.getText()));
			}
			if (LovUtils.getIntValue(COMBO_OPEN_CLOSE) != LovType.ANY) { // tpr_status = openClose
				tprD.setTprStatus(LovUtils.getIntValue(COMBO_OPEN_CLOSE));
			}
			// if (LovUtils.getIntValue(COMBO_PROCESS_STATUS) != LovType.ANY) { // status =
			// process_status
			// tpr.setStatus(LovUtils.getIntValue(COMBO_PROCESS_STATUS));
			// }

			ArrayList<Object> list = GateInDao.getTransactionData(conn, tprD, cr);
			ObservableList data = getInitialTableData(list);
			if (data != null)
				TPR_REPORT_TABLE_VIEW.setItems(data);
		} catch (Exception ex) {
			destroy = true;
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroy);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// parent.showAlert(Alert.AlertType.INFORMATION, "hello", "hello");
	}

	public ObservableList getInitialTableData(ArrayList<Object> dataList) {
		ArrayList<TprReportData> list = null;
		ObservableList datas = null;
		for (int i = 0, is = dataList == null ? 0 : dataList.size(); i < is; i++) {
			TPRecord tpr = (TPRecord) dataList.get(i);
			if (list == null)
				list = new ArrayList<TprReportData>();

			String netWt = calculateNetWeight(tpr);
			GateInDao.convertDate(tpr.getLatestLoadGateInExit(),outFormat); 
					
			list.add(new TprReportData(this, Integer.toString(tpr.getTprId()),
					LovType.TprStatus.getStr(tpr.getTprStatus()), MessageType.getStr(tpr.getReportingStatus()),
					tpr.getExInvoice(), tpr.getConsigneeName(), tpr.getDoNumber(), tpr.getProductCode(),
					tpr.getTransporterCode(), tpr.getStatus(), tpr.getVehicleName(),
					Misc.getPrintableDouble(tpr.getLoadTare()), Misc.getPrintableDouble(tpr.getLoadGross()),
					tpr.getComboStart(), tpr.getComboEnd(), tpr.getReportingStatus(), tpr.getLrNo(), tpr.getLrDate(),
					GateInDao.convertDate(tpr.getLatestLoadGateInExit(),outFormat),
					GateInDao.convertDate(tpr.getLatestLoadWbOutExit(),outFormat),
					GateInDao.convertDate(tpr.getLatestLoadWbInExit(),outFormat),
					GateInDao.convertDate(tpr.getLatestLoadGateOutExit(),outFormat),netWt));// ,
		}
		if (list != null)
			datas = FXCollections.observableList(list);
		return datas;
	}

	private String calculateNetWeight(TPRecord tpr) {
		String netWt = "";
		if (tpr != null && tpr.getLoadTare() != Misc.getUndefDouble() && tpr.getLoadGross() != Misc.getUndefDouble()) {
			double Wb_Net_Wt = tpr.getLoadGross() - tpr.getLoadTare();
			netWt = Misc.getPrintableDouble(Wb_Net_Wt);
		}
		return netWt;
	}



	/**
	 * Initializes the controller class.
	 */

	@Override
	public void clearInputs() {
		clearAction();
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
		token = null;
		bapiretResp = null;
		sapStatus = 0;
		sapMessage = null;
		sapExInvoice = null;
		sapType = "";
		removeTableRows();
	}

	private void removeTableRows() {
		TPR_REPORT_TABLE_VIEW.getItems().clear();
	}

	@Override
	public void stopRfid() {

	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
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
		// screenTitle.setText(title);
	}

	public void createInvoice(TprReportData tprReportData) {
		System.out.println("#######   Start ReportViewController Create Invoice for TPR_ID= " + tprReportData.getTprId()
				+ "  ##########");
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if ((tprReportData.getCustomer()).equalsIgnoreCase("CGPL")) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Invoice not found for Sampling Sales Order");
				return;
			} else {
				String[] options = { "NO", "YES" };
				int res = parent.prompt(Alert.AlertType.CONFIRMATION.toString(),
						"Do you want to create Invoice for Tpr-Id: " + tprReportData.getTprId(), options);
				if (res == 0) {
					return;
				} else {

					double netWt = Misc.getParamAsDouble(tprReportData.getLoadGross())
							- Misc.getParamAsDouble(tprReportData.getLoadTare());
					Pair<Boolean, Double> pairVal = GateInDao.isSalesOrderQuantityExist(conn,
							tprReportData.getSalesOrder(), tprReportData.getLineItem(), netWt);

					if (pairVal != null && !pairVal.first) {
						parent.showAlert(Alert.AlertType.ERROR, "Message", "Sales Order Exhausted");
						return;
					} else {
						bapiretResp = getSapResponse(tprReportData);
						if (bapiretResp != null) {
							sapMessage = bapiretResp.getMESSAGE();
							sapExInvoice = bapiretResp.getMESSAGE_V1();
							sapType = bapiretResp.getTYPE();
							sapStatus = sapType.equalsIgnoreCase("S") ? RecordType.MessageType.SUCCESS
									: RecordType.MessageType.FAILED;
						} else {
							sapStatus = RecordType.MessageType.NO_RESPONSE;
						}

						updateTpr(conn, tprReportData);
						if (sapType.equalsIgnoreCase("S")) {
							double totalLapseQuatity = pairVal.second + netWt;
							GateInDao.updateCGPLSalesOrder(conn, tprReportData.getSalesOrder(),
									tprReportData.getLineItem(), totalLapseQuatity);
						}

						conn.commit();
						String msg = "";
						if (sapType.equalsIgnoreCase("S")) {
							msg = "Invoice Created \n" + sapMessage + "\nInvoice No: " + sapExInvoice;
							LABEL_SAP_RESPONSE.setText("Last Invoice Number: " + sapExInvoice);
						} else if (sapType.equalsIgnoreCase("E")) {
							msg = "Data Saved\n" + sapMessage;
							LABEL_SAP_RESPONSE.setText(msg);
						} else {
							msg = RecordType.MessageType.getStr(sapStatus);
						}

						parent.showAlert(Alert.AlertType.INFORMATION, "Message", msg);
					}
				}
			}
		} catch (Exception ex) {
			destroyIt = true;
			ex.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("#######    End ReportViewController Create Invoice  ##########");
	}

	private BAPIRET1 getSapResponse(TprReportData tprReportData) {
		InvoiceGeneration invoiceGenerationObj = new InvoiceGeneration();
		BAPIRET1 response = null;
		try {
			System.out.println("########## Start getSapResponse() ########");
			String _salesOrder = tprReportData.getSalesOrder();// LovDao.getText(TokenManager.portNodeId,
			String _shipTo = tprReportData.getCustomer();
			String _transporter = tprReportData.getTransporterName(); // LovDao.getText(TokenManager.portNodeId,
			double itmNumber = Misc.getParamAsDouble(tprReportData.getLineItem());// LovDao.getText(TokenManager.portNodeId,
			int IntValue = (int) Math.round(itmNumber);
			String _itmNumber = IntValue + "";
			String _inTime = UIConstant.timeFormatWithSec.format(tprReportData.getComboStart());
			String _outTime = UIConstant.timeFormatWithSec.format(tprReportData.getGetLatestLoadWbInExit());
			double _grossWt = Misc.getParamAsDouble(tprReportData.getLoadGross());
			double netWt = Misc.getParamAsDouble(tprReportData.getLoadGross())
					- Misc.getParamAsDouble(tprReportData.getLoadTare());
			BigDecimal _netWt = (Misc.getPrintableDouble(netWt) != null && Misc.getPrintableDouble(netWt).length() > 0)
					? new BigDecimal(Misc.getPrintableDouble(netWt))
					: new BigDecimal(0);
			_netWt = _netWt.setScale(2, BigDecimal.ROUND_DOWN);
			String lrNo = tprReportData.getLrNo();
			// String lrDate = "09-06-2021";//
			String lrDate = tprReportData.getLrDate() != null ? UIConstant.dateFormat2.format(tprReportData.getLrDate())
					: "";
			System.out.println("[Data: SalesOrder:" + _salesOrder + ",NetWt: " + _netWt + ", Transporter: "
					+ _transporter + ", VehicleName: " + tprReportData.getVehicleName() + ", _shipTo:" + _shipTo
					+ ",HSN_NO: " + TokenManager.HSN_NO + ", TareWt: " + tprReportData.getLoadTare() + ",grossWt: "
					+ Misc.getPrintableDouble(_grossWt) + ",_inTime: " + _inTime + ",OutTime: " + _outTime
					+ ", ItmNumber: " + _itmNumber + " ,lrNo: " + lrNo + ",lrDate: " + lrDate + " ]");

			response = invoiceGenerationObj.getSapResp(tprReportData.getVehicleName(), tprReportData.getTprId(),
					_salesOrder, _shipTo, _transporter, _itmNumber, _inTime, _outTime, tprReportData.getLoadTare(),
					tprReportData.getLoadGross(), _netWt, TokenManager.HSN_NO, lrNo, lrDate);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("########## END getSapResponse() ########");
		return response;
	}

	private boolean updateTpr(Connection conn, TprReportData tprData) throws Exception {
		boolean isStatus = false;
		java.util.Date curr = new java.util.Date();
		TPRecord tpRecord = null;

		if (tpRecord == null)
			tpRecord = new TPRecord();

		tpRecord.setTprId(Misc.getParamAsInt(tprData.getTprId()));

		ArrayList<Object> dataList = GateInDao.getTransactionData(conn, tpRecord);
		for (int i = 0, is = dataList == null ? 0 : dataList.size(); i < is; i++) {
			tpRecord = (TPRecord) dataList.get(i);
			tpRecord.setUpdatedOn(curr);
			tpRecord.setReportingStatus(sapStatus);
			tpRecord.setMessage(sapMessage);
			tpRecord.setExInvoice(sapExInvoice);
			tpRecord.setLoadYardInName(TokenManager.userName);
			TPRInformation.insertUpdateTpr(conn, tpRecord);
			isStatus = true;
		}

		return isStatus;
	}

	@Override
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub

	}

}

package com.ipssi.rfid.ui.controller;

import java.beans.EventHandler;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import com.ipssi.cgplSap.RecordType.MessageType;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TprReportData;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovType;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SalesOrderCancellationFormController implements Initializable,ControllerI {

	TprReportData tprData = null;
//	ReportViewController reportViewController = null;
	@FXML
	private JFXButton CONTROL_SAVE_SALES_ORDER;
	
	@FXML
	private JFXButton CONTROL_CANCEL_SALES_ORDER;
	
	@FXML
	private Label LABEL_TITLE;
	
	@FXML
	private JFXTextField TEXT_TPR_ID;
	@FXML
	private JFXTextField TEXT_VEHICLE_NO;
	@FXML
	private JFXTextField TEXT_GROSS_WT;

	@FXML
	private JFXTextField TEXT_TARE_WT;

	@FXML
	private JFXComboBox COMBO_CANCEL_REASON;

	@FXML
	private JFXTextField TEXT_TRANSPORTER;
	
	@FXML
	private JFXTextArea TEXT_COMMENTS;
	
	@FXML
	private JFXTextField TEXT_REF_ID;
	
	@FXML
	private JFXTextField TEXT_PROCESS_STATUS;
	private TPRecord tpRecord = null;


	private static final Logger log = Logger.getLogger(SalesOrderCancellationFormController.class.getName());
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		LovUtils.initializeComboBox(COMBO_CANCEL_REASON, LovDao.LovItemType.INVOICE_CANCEL,-1,
				Misc.getUndefInt());
	}

//	@FXML
//	private void onControlKeyPress(KeyEvent event) {
//		if (event.getCode() == KeyCode.ENTER) {
//			Button control = ((Button) event.getSource());
//			handleActionControl(control);
//		}
//	}



	@FXML
	private void controlSaveClicked(ActionEvent event) {
		if(saveAction()) {
			closeStage(event);
			tprData.reportViewController.searchAction();	
		}
		
	}

	@FXML
	private void controlCloseClicked(ActionEvent event) {
//		Button control = ((Button) event.getSource());
//		handleActionControl(control);
		closeStage(event);
	}
	
	private boolean saveAction() {
		boolean isSaved = false;
		Connection conn = null;
		boolean destroyIt = false;
		if (Misc.isUndef(LovUtils.getIntValue(COMBO_CANCEL_REASON))) {
			tprData.reportViewController.parent.showAlert(Alert.AlertType.ERROR, "Message", "Please Select Cancel Invoice Reason.");
			COMBO_CANCEL_REASON.requestFocus();
			return false;
		}
		else if (Utils.isNull(TEXT_REF_ID.getText())) {
			tprData.reportViewController.parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter Ref-Id");
			TEXT_REF_ID.requestFocus();
			return false;
		} else if(!GateInDao.checkNumeric(TEXT_REF_ID.getText())){
			tprData.reportViewController.parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter numeric Ref-Id");
			TEXT_REF_ID.requestFocus();
			return false;
		}else {
			try {
				conn = DBConnectionPool.getConnectionFromPoolNonWeb();
				if(tpRecord ==null)
					tpRecord = new TPRecord();
				tpRecord.setTprId(Misc.getParamAsInt(tprData.getTprId()));
				
				isSaved = updateTpr(conn);
				
				
				conn.commit();
				clearInputs();
			}catch (Exception e) {
				e.printStackTrace();
				destroyIt = true;
			} finally {
				try {
					if (conn != null) {
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
					}
				} catch (GenericException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return isSaved;
	}

	
	 
	private boolean updateTpr(Connection conn) throws Exception {
		boolean isStatus = false;
		java.util.Date curr = new java.util.Date();
		ArrayList<TprReportData> list = null;
//		TPRecord tpr = null;
		if (tpRecord != null) {
			ArrayList<Object> dataList = GateInDao.getTransactionData(conn, tpRecord);
			for (int i = 0, is = dataList == null ? 0 : dataList.size(); i < is; i++) {
				tpRecord = (TPRecord) dataList.get(i);
				tpRecord.setReportingStatus(MessageType.CANCEL);
				tpRecord.setStatusReason(Misc.getParamAsString(TEXT_COMMENTS.getText()));
				tpRecord.setCancellationReason(LovUtils.getIntValue(COMBO_CANCEL_REASON));
				tpRecord.setRefTprIdIfCancelled(Misc.getParamAsInt(TEXT_REF_ID.getText()));
				tpRecord.setUpdatedOn(curr);
				tpRecord.setLoadYardOutName(TokenManager.userName);
				TPRInformation.insertUpdateTpr(conn, tpRecord);
				isStatus = true;
			}
			
			
		}
		return isStatus;
	}

	public void initData(TprReportData tprData) {
		this.tprData = tprData;
		if(tprData!=null) {
			TEXT_TPR_ID.setText(tprData.getTprId());
			TEXT_VEHICLE_NO.setText(tprData.getVehicleName());
			
		    TEXT_TARE_WT.setText(tprData.getLoadTare());
		    TEXT_GROSS_WT.setText(tprData.getLoadGross());
			TEXT_PROCESS_STATUS.setText(tprData.getInvoiceStatus());
			TEXT_TRANSPORTER.setText(tprData.getTransporterName()); 
		}
		
	}
	
	private void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

	@Override
	public void clearInputs() {
		tpRecord = null;
	}

	@Override
	public void stopRfid() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(MainController parent) {
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initController(SettingController settingParent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestFocusNextField() {
		
	}

	@Override
	public void setTitle(String title) {
		LABEL_TITLE.setText(title);
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

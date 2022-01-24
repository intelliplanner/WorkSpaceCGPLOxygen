/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.processor.TPRInformation;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.dao.GateInDao;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class TransporterDetailWindowController implements Initializable,ControllerI {

    @FXML
    private JFXTextField TEXT_TRANSPORTER_NAME;
    @FXML
    private JFXTextField TEXT_TRANSPORTER_SAP_CODE;
    @FXML
    private JFXTextField TEXT_TRANSPORTER_STD_NAME;
	private MainController parent=null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void onControlKeyPress(KeyEvent event) {
    	if (event.getCode() == KeyCode.ENTER) {
			String controllId = parent.getSourceId(event);
			handleActionControl(controllId);
		}
    }

	private void handleActionControl(String controllId) {
		if (controllId == null || controllId == "" || controllId.length() == 0) {
			return;
		}
		switch (controllId.toUpperCase()) {
		case "TEXT_TRANSPORTER_NAME":
			TEXT_TRANSPORTER_SAP_CODE.requestFocus();
			break;
		case "TEXT_TRANSPORTER_SAP_CODE":
			TEXT_TRANSPORTER_STD_NAME.requestFocus();
			break;
		case "TEXT_TRANSPORTER_STD_NAME":
			parent.CONTROL_SAVE.requestFocus();
			break;
		default:
			break;
		}
	}

	@Override
	public void clearInputs() {
		// TODO Auto-generated method stub
		TEXT_TRANSPORTER_NAME.setText("");
		TEXT_TRANSPORTER_SAP_CODE.setText("");
		TEXT_TRANSPORTER_STD_NAME.setText("");
	}

	@Override
	public void stopRfid() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (Utils.isNull(TEXT_TRANSPORTER_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid 'Transporter Name'.");
				TEXT_TRANSPORTER_NAME.requestFocus();
				return false;
			} else {
				GateInDao.insertTransporter(conn, TEXT_TRANSPORTER_NAME.getText(), TEXT_TRANSPORTER_STD_NAME.getText(), TEXT_TRANSPORTER_SAP_CODE.getText());
				conn.commit();
				clearInputs();
				parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Detail Saved");
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

		return true;

	}

	@Override
	public void init(MainController parent) {
		this.parent=parent;
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
		TEXT_TRANSPORTER_NAME.requestFocus();
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
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

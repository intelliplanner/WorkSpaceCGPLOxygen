/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.dao.IssueTag;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class ReadTagWindowController implements Initializable, ControllerI  {

    @FXML
    private JFXTextField TEXT_VEHICLE_NAME;
	private MainController parent=null;
	private Vehicle vehicleObj = null;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	
    }    

    @FXML
    private void onControlKeyPress(KeyEvent event) {
    }

	@Override
	public void clearInputs() {
		Platform.runLater(() -> {
			clearAction();
		});
	}
	

	@Override
	public void stopRfid() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
		try {
			start();
		} catch (IOException ex) {
			Logger.getLogger(TareWeighmentWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
	
    public void start() throws IOException {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			IssueTag isIssued = new  IssueTag();
			com.ipssi.gen.utils.Pair<Integer, String> pair = isIssued.getTagEPC();
			if(pair!=null && !Utils.isNull(pair.second)) {
				setVehicleInformation(conn, pair.second);
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
		parent.CONTROL_SAVE.requestFocus();
	}

	@Override
	public void setTitle(String title) {
		
	}

	@Override
	public void vehicleNameAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dlNoAction() {
		// TODO Auto-generated method stub
		
	}
    
	private void setVehicleInformation(Connection conn, String epcCode) {

		ArrayList<Object> list = null;
		try {
			vehicleObj = new Vehicle();
			vehicleObj.setEpcId(epcCode);
			vehicleObj.setStatus(1);
			list = RFIDMasterDao.select(conn, vehicleObj);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					vehicleObj = (Vehicle) list.get(i);
					TEXT_VEHICLE_NAME.setText(vehicleObj.getVehicleName());
				}
			} else {
				parent.showAlert(Alert.AlertType.INFORMATION, "MESSAGE", "Card not issued.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(" ######## End Get Vehicle Extended Detail  ######");

	}
	
	
	private void clearInputs(Connection conn, boolean clearToken) {
		
		vehicleObj = null;
		TEXT_VEHICLE_NAME.setText("");
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

}

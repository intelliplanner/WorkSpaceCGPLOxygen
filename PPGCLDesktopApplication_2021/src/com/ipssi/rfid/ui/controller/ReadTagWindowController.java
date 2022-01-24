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
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRBlockManager;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.RFIDDataHandler;
import com.ipssi.rfid.readers.TAGListener;
import com.ipssi.rfid.ui.controller.service.ControllerI;
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
    @FXML
    private JFXTextField EPC_CODE;
	private MainController parent=null;
	private Vehicle vehicleObj = null;
	private RFIDDataHandler rfidHandler = null;
	public Token token = null;
	private int readerId = 0;
	private static final Logger log = Logger.getLogger(ReadTagWindowController.class.getName());
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
		if (!TokenManager.tagIdentifyManually) {
			try {
				start();
			} catch (IOException ex) {
				Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
    }    

    @FXML
    private void onControlKeyPress(KeyEvent event) {
    }

	@Override
	public void clearInputs() {
//		Platform.runLater(() -> {
			clearAction();
			
//		});
	}
	

	@Override
	public void stopRfid() {
		try {
			if (rfidHandler != null) {
				rfidHandler.stop();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	
	@Override
	public boolean save() {
		Platform.runLater(() ->{
			try {
				startManual();
			} catch (IOException ex) {
				Logger.getLogger(TareWeighmentWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		return false;
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
						setVehicleName(tpr.getVehicleName());
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
					Platform.runLater(() -> {
						TEXT_VEHICLE_NAME.setText(text);
					});
				}

				@Override
				public void clearVehicleName() {
					TEXT_VEHICLE_NAME.setText("");
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
	
    public void startManual() throws IOException {
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
		TEXT_VEHICLE_NAME.setText(vehicleName);
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
	}

	@Override
	public void stopSyncTprService() {
		
	}

	@Override
	public void initController(SettingController settingParent) {
		
	}

	@Override
	public void requestFocusNextField() {
		if(parent.CONTROL_SAVE.isDisable()) {
			parent.CONTROL_CLEAR.requestFocus();
		}else {
			parent.CONTROL_SAVE.requestFocus();
		}
	}

	@Override
	public void setTitle(String title) {
		
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
					EPC_CODE.setText(epcCode);
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
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token  != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		token = null;
		vehicleObj = null;
		clearVehicleName();
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
	public void manageTag(Connection conn, Token token, TPRecord tpr, TPStep tps, TPRBlockManager _tprBlockManager) {
		// TODO Auto-generated method stub
		
	}

}

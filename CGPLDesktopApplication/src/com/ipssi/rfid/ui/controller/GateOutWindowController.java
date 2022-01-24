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
import com.ipssi.rfid.beans.BlockingInstruction;
import com.ipssi.rfid.beans.RFIDHolder;
import com.ipssi.rfid.beans.TPRBlockEntry;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPSQuestionDetail;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Token;
import com.ipssi.rfid.constant.Results;
import com.ipssi.rfid.constant.Status;
import com.ipssi.rfid.constant.Status.TPRQuestion;
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
import com.ipssi.rfid.ui.data.LovDao;
import com.ipssi.rfid.ui.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.syncTprInfo.SyncTprInfo;
import com.ipssi.rfid.ui.syncTprInfo.SyncTprServiceHandler;
import com.ipssi.rfid.ui.data.LovUtils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author IPSSI
 */
public class GateOutWindowController implements Initializable, ControllerI {

	@FXML
	private Label TEXT_TPR_ID;
	@FXML
	private JFXCheckBox CHECKBOX_SAFE_DRIVING;
	@FXML
	private JFXCheckBox CHECKBOX_DAMAGED_PLANT_PROPERTY;
	@FXML
	private JFXCheckBox CHECKBOX_MISBEHAVIOUR;
	@FXML
	private Label TEXT_PO_SALES_ORDER;
	@FXML
	private Label TEXT_CUSTOMER;
	@FXML
	private Label TEXT_TRANSPORTER;
	@FXML
	public JFXTextField TEXT_VEHICLE_NAME;
	@FXML
	private JFXTextArea TEXT_NOTE;
	@FXML
	private Label TEXT_RUNNING_PROCESS;
	@FXML
	private Label TEXT_COMPLETED_PROCESS;
	@FXML
	private Label TEXT_TOTAL_PROCESS;
	
	private boolean isTagRead = false;
	
	private SyncTprInfo syncTprInfo = null;
	
	private int readerId = 0;
	private RFIDDataHandler rfidHandler = null;
	private Date entryTime = null;
	Token token = null;
	private TPRecord tpRecord = null;
	private TPStep tpStep = null;
	private ArrayList<Pair<Long, Integer>> readings = null;
	private DisconnectionDialog disconnectionDialog = new DisconnectionDialog(
			"Weigh Bridge Disconnected please check connection.....");
	private MainController parent = null;
	private TPRBlockManager tprBlockManager = null;

	private int m_vehicleId = Misc.getUndefInt();
	private boolean isEnterPressed = false;
	private int isNewVehicle = Misc.getUndefInt();
	
	private static final Logger log = Logger.getLogger(GateOutWindowController.class.getName());

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
	}

	private void handleActionControl(String controlId) {
		if (controlId == null || controlId == "" || controlId.length() == 0) {
			return;
		}
		switch (controlId.toUpperCase()) {
		case "TEXT_VEHICLE_NAME":
			vehicleNameAction();
			break;
		case "COMBO_PO_SALES_ORDER":
			break;
		case "COMBO_CUSTOMER":
			break;
		case "COMBO_TRANSPORTER":
			CHECKBOX_SAFE_DRIVING.requestFocus();
			break;
		case "CHECKBOX_SAFE_DRIVING":
			CHECKBOX_DAMAGED_PLANT_PROPERTY.requestFocus();
			break;

		case "CHECKBOX_DAMAGED_PLANT_PROPERTY":
			CHECKBOX_MISBEHAVIOUR.requestFocus();
			break;

		case "CHECKBOX_MISBEHAVIOUR":
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
		case "COMBO_PO_SALES_ORDER":
			break;
		default:
			break;
		}
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
		if (!TokenManager.forceManual) {
			try {
				start();
			} catch (IOException ex) {
				Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}else if(TokenManager.forceManual && TokenManager.IS_AUTO_COMPLETE_ON){
			AutoCompleteTextField autoCompleteTextField = new AutoCompleteTextField(this.parent, TEXT_VEHICLE_NAME, LovDao.LovItemType.VEHICLE);
			autoCompleteTextField.setAutoCompleteTextBox();
		}
		syncTpr();
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
					} catch (Exception ex) {
//						ex.printStackTrace();
						Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
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

	public void setTPRecord(Connection conn, TPRecord tpr) throws IOException {
		System.out.println("######### Gate Out setTPRecord  ########");
		try {
			tpRecord = tpr;
			if (tpRecord != null) {
				System.out.println("TPR Record Create");
				if (true) {
					isTagRead = token != null ? token.isReadFromTag() : false;
					if (token == null && tpRecord.getEarliestLoadGateOutEntry() != null) {
						entryTime = tpRecord.getEarliestLoadGateOutEntry();
					} else if (token != null && tpRecord.getEarliestLoadGateOutEntry() == null) {
						if (token.getLastSeen() != Misc.getUndefInt()) {
							entryTime = new Date(token.getLastSeen());
						} else {
							entryTime = new Date();
						}
					} else if (token != null && tpRecord.getEarliestLoadGateOutEntry() != null) {
						if (token.getLastSeen() > Utils.getDateTimeLong(tpRecord.getEarliestLoadGateOutEntry())) {
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
						setVehicleName(tpRecord.getVehicleName());
						TEXT_TPR_ID.setText(Misc.getPrintableInt(tpRecord.getTprId()));
						TEXT_PO_SALES_ORDER.setText(LovDao.getText(463, LovItemType.PO_SALES_ORDER, tpRecord.getDoId(),
								Misc.getUndefInt()));
						TEXT_CUSTOMER.setText(LovDao.getText(463, LovItemType.CUSTOMER, tpRecord.getConsignee(),
								Misc.getUndefInt()));
						TEXT_TRANSPORTER.setText(LovDao.getText(463, LovItemType.TRANSPORTER,
								tpRecord.getTransporterId(), Misc.getUndefInt()));

						TEXT_NOTE.setText(tpRecord.getConsigneeRefDoc());
					});

				}
				setBlockingStatus();
				
				requestFocusNextField();
			} else {
				parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Invalid Vehicle Go to Registration");
				return;
			}
		} catch (Exception ex) {
//			ex.printStackTrace();
			Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


//	private void setBlockingStatus() {
//		if(tprBlockManager == null){
//			return;
//		}
//		try{
//			int blockStatus = tprBlockManager.getBlockStatus();
//			if (blockStatus == UIConstant.BLOCKED) {
//				parent.labelBlockingReason.setText(tprBlockManager.getBlockingReason());
//				parent.setControllerDisable(true);
////				overrides.setText("BLOCKED");
////				saveAndOpen.requestFocusInWindow();
//			}else{
////				overrides.setText("NOT_BLOCKED");
//				parent.labelBlockingReason.setText("");
//				parent.setControllerDisable(false);
//			}
//			
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//	}

	
	@Override
	public void clearInputs() {
//		Platform.runLater(() -> {
			clearAction();
//		});
	}

	@Override
	public boolean save() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if (Utils.isNull(TEXT_VEHICLE_NAME.getText())) {
				parent.showAlert(Alert.AlertType.ERROR, "Message", "Please enter valid vehicle name");
				TEXT_VEHICLE_NAME.requestFocus();
				return false;
			} else {
				updateTPR(conn, TokenManager.nextWorkStationType);
				int stepId = Misc.getUndefInt();
				stepId = InsertTPRStep(conn, false);
				if (!Misc.isUndef(stepId)) {
					InsertTPRQuestionDetails(conn, stepId);
				}
				
				if(tprBlockManager != null){
		    		updateCurrentBlocking(conn);
//		    		tprBlockManager.calculateBlocking(conn);
//		    		setBlockingStatus();
		    		tprBlockManager.setTprBlockStatus(conn, tpRecord.getTprId(),TokenManager.userId);
		    		unregisteredRFIDFromVehicle(conn);
		    	}
				conn.commit();
				parent.showAlert(Alert.AlertType.INFORMATION, "Message", "Data Saved");
				clearInputs(conn, false);
				
			}
		} catch (Exception ex) {
			parent.showAlert(Alert.AlertType.ERROR, "Message", UIConstant.SAVE_FAILER_MESSAGE);
//			ex.printStackTrace();
			Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
//				ex.printStackTrace();
				Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return true;
	}

	private void unregisteredRFIDFromVehicle(Connection conn) {
		
			try {
				RFIDMasterDao.executeQuery(conn,"update vehicle set last_epc=rfid_epc where id = '" + tpRecord.getVehicleId() + "'");
				RFIDMasterDao.executeQuery(conn,"update vehicle set rfid_epc=null where id = '" +  tpRecord.getVehicleId() + "'");
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, e);
			}
			
		
	}

	@Override
	public void init(MainController parent) {
		this.parent = parent;
	}


	private void updateTPR(Connection conn, int nextWorkStation) throws Exception {
		updateTPR(conn, nextWorkStation, false);
	}

	private void updateTPR(Connection conn, int nextWorkStation, boolean isDeny) throws Exception {
		java.util.Date curr = new java.util.Date();
		if (!isDeny) {
			tpRecord.setPreStepType(TokenManager.currWorkStationType);
			tpRecord.setPrevTpStep(TokenManager.currWorkStationId);
			tpRecord.setPreStepDate(curr);
			tpRecord.setUpdatedBy(TokenManager.userId);
			tpRecord.setUpdatedOn(curr);
			tpRecord.setStatus(1);
			// tpRecord.setTransporterId(DropDownValues.getComboSelectedValCOMBO_TRANSPORTER.));
			tpRecord.setNextStepType(nextWorkStation);
			tpRecord.setComboEnd(new Date());
			if (TokenManager.closeTPR) {
				tpRecord.setTprStatus(Status.TPR.CLOSE);
			}
			tpRecord.setLatestLoadGateOutExit(new Date());
			tpRecord.setLoadGateOutName(TokenManager.userName);

		}
		if (tpRecord.getComboStart() == null) {
			tpRecord.setComboStart(curr);
		}
		tpRecord.setEarliestLoadGateOutEntry(entryTime);
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
			RFIDMasterDao.insert(conn, tpStep, true);
		} else {
			long currTimeServerMillis = System.currentTimeMillis();
			tpStep.setExitTime(new Date(currTimeServerMillis));
			tpStep.setUpdatedOn(new Date(currTimeServerMillis));
			tpStep.setMaterialCat(TokenManager.materialCat);
			tpStep.setSaveStatus(isDeny ? TPStep.REQUEST_OVERRIDE : TPStep.SAVE_AND_CONTINUE);
			RFIDMasterDao.update(conn, tpStep, false);
			RFIDMasterDao.update(conn, tpStep, true);
		}
		return tpStep.getId();
	}

	private void clearInputs(Connection conn, boolean clearToken) {
		if (clearToken) {
			TokenManager.clearWorkstation();
		} else {
			if (token != null) {
				TokenManager.returnToken(conn, token);
			}
		}
		
		
		tpStep = null;
		tpRecord = null;
		entryTime = null;
		token = null;
		tprBlockManager=null;
		

		isTagRead = false;
		m_vehicleId = Misc.getUndefInt();
		isEnterPressed = false;
		isNewVehicle = Misc.getUndefInt();
		
		TEXT_TPR_ID.setText("");
		clearVehicleName();
		TEXT_PO_SALES_ORDER.setText("");
		TEXT_CUSTOMER.setText("");
		TEXT_TRANSPORTER.setText("");
		TEXT_NOTE.setText("");
		CHECKBOX_DAMAGED_PLANT_PROPERTY.setSelected(false);
		CHECKBOX_SAFE_DRIVING.setSelected(false);
		CHECKBOX_MISBEHAVIOUR.setSelected(false);
		TEXT_RUNNING_PROCESS.setText("");
		TEXT_COMPLETED_PROCESS.setText("");
		TEXT_TOTAL_PROCESS.setText("");

	}

	private void clearAction() {
		Connection conn = null;
		boolean destroyIt = false;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			clearInputs(conn, true);
		} catch (Exception ex) {
//			ex.printStackTrace();
			Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
//				ex.printStackTrace();
				Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void stopRfid() {
		try {
			if (rfidHandler != null) {
				rfidHandler.stop();
			}

		} catch (Exception ex) {
//			ex.printStackTrace();
			Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
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
		if (syncTprInfo != null) {
			syncTprInfo.stop();
		}
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
				CHECKBOX_SAFE_DRIVING.requestFocus();
			
		});
	}

	@Override
	public void setTitle(String title) {
		 
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
//						ex.printStackTrace();
						Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
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
//			ex.printStackTrace();
			Logger.getLogger(GateOutWindowController.class.getName()).log(Level.SEVERE, null, ex);
			destroyIt = true;
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	
	
	}

	private boolean InsertTPRQuestionDetails(Connection conn, int stepId) throws Exception {
		HashMap<Integer, Integer> quesAnsList = getQuestionIdList(conn);
		boolean isInsert = true;
		for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
			Integer questionId = entry.getKey();
			Integer answerId = entry.getValue();
			GateInDao.updateTPRQuestion(conn, tpRecord.getTprId(), TokenManager.currWorkStationType, questionId,
					answerId, TokenManager.userId);
		}
		return isInsert;
	}

	private HashMap<Integer, Integer> getQuestionIdList(Connection conn) {

		HashMap<Integer, Integer> quesAnsList = new HashMap<Integer, Integer>();

		if (CHECKBOX_SAFE_DRIVING.isSelected())
			quesAnsList.put(Status.TPRQuestion.isUnSafeDriving, UIConstant.YES);
		else
			quesAnsList.put(Status.TPRQuestion.isUnSafeDriving, UIConstant.NO);

		if (CHECKBOX_DAMAGED_PLANT_PROPERTY.isSelected())
			quesAnsList.put(Status.TPRQuestion.isPlantPropertyDamaged, UIConstant.YES);
		else
			quesAnsList.put(Status.TPRQuestion.isPlantPropertyDamaged, UIConstant.NO);

		if (CHECKBOX_MISBEHAVIOUR.isSelected())
			quesAnsList.put(Status.TPRQuestion.isMisBehaviorByDriver, UIConstant.YES);
		else
			quesAnsList.put(Status.TPRQuestion.isMisBehaviorByDriver, UIConstant.NO);

		
		return quesAnsList;

	}

    private void setQuestionsBlocking(int questionId, int answerId){
	    	if(Misc.isUndef(questionId))
	    		return;
	    	if(tprBlockManager != null){
	    		TPSQuestionDetail tpsQuestionBean = new TPSQuestionDetail();
	    		tpsQuestionBean.setQuestionId(questionId);
	    		tpsQuestionBean.setAnswerId(answerId);
	    		tprBlockManager.addQuestions(tpsQuestionBean);
//	    		setBlockingStatus();
	    	}
	    }
	  
	

	    private void updateCurrentBlocking(Connection conn){
	    	if(tprBlockManager == null)
	    		return;
	    	HashMap<Integer, Integer> quesAnsList = getQuestionIdList(conn);
	    	 for (Map.Entry<Integer, Integer> entry : quesAnsList.entrySet()) {
	             Integer questionId = entry.getKey();
	             Integer answerId = entry.getValue();
	             setQuestionsBlocking(questionId,answerId);
	         }
	    }
	    
	 

		private void setBlockingStatus() {
			if(tprBlockManager == null){
				return;
			}
			try{
				int blockStatus = tprBlockManager.getBlockStatus();
				
				if (blockStatus == UIConstant.BLOCKED) {
					Platform.runLater(() -> {
						parent.labelBlockingReason.setText("Vehicle Blocked: "+tprBlockManager.getBlockingReason(false));
						parent.setControllerDisable(true);
					});
					
					
//					overrides.setText("BLOCKED");
//					saveAndOpen.requestFocusInWindow();
				}else{
//					overrides.setText("NOT_BLOCKED");
					Platform.runLater(() -> {
						parent.labelBlockingReason.setText(tprBlockManager.getBlockingReason(false));
						parent.setControllerDisable(false);
					});
				}
				
				checkedBlocking(blockStatus);
			
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		

		private void checkedBlocking(int blockStatus) {
			if(tprBlockManager == null) {
				return;
			}
			ArrayList<TPRBlockEntry> tprBlockEntries = tprBlockManager.getBlockEntries();
			TPRBlockEntry tprBlockEntry = null;
			for (int i = 0, is = tprBlockEntries == null ? 0 : tprBlockEntries.size(); i < is; i++) {
				tprBlockEntry = tprBlockEntries.get(i);
				BlockingInstruction bInstruction = tprBlockManager.getInstructionById(tprBlockEntry.getInstructionId());
				if(bInstruction == null)
					continue;
				
				if (bInstruction.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DAMAGED_PLANT_PROPERTY) {
					CHECKBOX_DAMAGED_PLANT_PROPERTY.setSelected(true);
				}
				if (bInstruction.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_UNSAFE_DRIVING) {
					CHECKBOX_SAFE_DRIVING.setSelected(true);
				}
				if (bInstruction.getType() == com.ipssi.rfid.constant.Type.BlockingInstruction.BLOCK_DUETO_DRIVER_BEHAVIOUR) {
					CHECKBOX_MISBEHAVIOUR.setSelected(true);
				}
				
			}


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
		public void dlNoAction() {
			// TODO Auto-generated method stub
			
		}

}

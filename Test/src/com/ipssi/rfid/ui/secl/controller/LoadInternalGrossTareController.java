package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.TPRTareDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.beans.TPRecord.WeighmentStep;
import com.ipssi.rfid.constant.Status.TPR;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXMaskTextField;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class LoadInternalGrossTareController implements ControllerI,Initializable {
	@FXML
    private JFXTextField textBoxVehicleName;
    @FXML
    private Label textWBReading;
    @FXML
    private Label textChallanNo;
    @FXML
    private Label textTareWt;
    @FXML
    private Label textGrossWt;
    @FXML
    private Label textNetWt;
    @FXML
    private Label textChallanDate;
    @FXML
    private JFXTextField textBoxTransporter;
    @FXML
    private JFXTextField textBoxSource;
    @FXML
    private JFXTextField textBoxDestination;
    @FXML
    private JFXTextField textBoxMaterialGrade;
    @FXML
    private JFXCheckBox checkBoxTare;
    @FXML
    private JFXCheckBox checkBoxGross;
    @FXML
    private Label textNoTareAllowed;
    @FXML
    private Label textLastTareExpired;
    @FXML
    private Label textLastTare;
    @FXML
    private Label textLastTareDate;
    @FXML
    private Label textSourceName;
    @FXML
    private Label textDestinationName;
    @FXML
    private Label textmaterialGradeName;
    @FXML
    private JFXMaskTextField textBoxLRNo;
    @FXML
	private void toggleWeighment(ActionEvent event) {
		handleToggleWeighment(((Node)event.getSource()).getId());
	}
	@Override
	public void handleToggleWeighment(String controlId){
		WeighmentStep weighmentState = controlId.equalsIgnoreCase("checkBoxTare") ? WeighmentStep.tare : WeighmentStep.gross;
		if((weighmentState == WeighmentStep.tare && checkBoxTare.isDisable()) || (weighmentState == WeighmentStep.gross && checkBoxGross.isDisable()) )
			return;
		setWeighmentMode(weighmentState, true);
		setReadings();
	}
	private void setWeighmentMode(WeighmentStep weighmentState, boolean initTPR){
		this.weighmentStep = weighmentState;
		if(weighmentState == WeighmentStep.tare){
			checkBoxGross.setSelected(false);
			checkBoxGross.setDisable(false);
			checkBoxTare.setSelected(true);
			checkBoxTare.setDisable(true);
		}else if(weighmentState == WeighmentStep.gross){
			checkBoxGross.setSelected(true);
			checkBoxGross.setDisable(true);
			checkBoxTare.setSelected(false);
			checkBoxTare.setDisable(false);
		}else{
			checkBoxGross.setSelected(false);
			checkBoxGross.setDisable(true);
			checkBoxTare.setSelected(false);
			checkBoxTare.setDisable(true);
		}
		if(initTPR)
			parent.handleReinitTPR(false);
	}
    
	MainController parent;
	Parent rootView;
	MenuItemInfo menuItemInfo;
	DigitalClockExt challanClock;
	private WeighmentStep weighmentStep = WeighmentStep.gross;
	private boolean changeWeighment = false;
	@Override
	public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
		this.rootView = rootView;
		this.parent = parent;
		this.menuItemInfo = menuItemInfo;
		if(challanClock == null)
			challanClock = new DigitalClockExt(textChallanDate, ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
	}
	public void setReadings(){
		if(weighmentStep != WeighmentStep.noWeight){
			Random r = new Random();
			int value =  r.nextInt(10)*500 + (weighmentStep == WeighmentStep.gross ? 30000 : 11000); 
			setWeighBridgeReading(value+"");
		}
	}
	@Override
	public void clearInputs() {
		changeWeighment = false;
		if(TokenManager.isSimulateWB){
			setReadings();
		}else{
			textWBReading.setText("");
		}
		challanClock.stop();
		tareInfo = null;
//		setWeighmentMode(isGross);
	}
	@Override
	public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {
		if(tpRecord == null)
			return;
		VehicleExtended vehicleExt;
		try {
			changeWeighment = true;
			if(TokenManager.isSimulateWB){
				/*if(weighmentStep == WeighmentStep.gross)
					textGrossWt.setText(textWBReading.getText());
				else if(weighmentStep == WeighmentStep.tare)
					textTareWt.setText(textWBReading.getText());*/
				setReadings();
			}
			SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
			textBoxVehicleName.setText(tpRecord.getVehicleName());
			textChallanNo.setText(tpRecord.getChallanNo());
			textChallanDate.setText(tpRecord.getChallanDate() == null ? "" : sdf.format(tpRecord.getChallanDate()));
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			Pair<String,String> source = !Utils.isNull(tpRecord.getMinesCode()) ?  LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getMinesCode(), LovItemType.MINES) : LovDao.getAutocompletePrintablePair(portNodeId, parent.getMainWindow().getWorkStationDetails().getMinesCode(),LovItemType.MINES);
			String transporter = !Utils.isNull(tpRecord.getTransporterCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER) : vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER);
			Pair<String,String> destination = !Utils.isNull(tpRecord.getDestinationCode()) ?  LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getDestinationCode(), LovItemType.SIDING) : LovDao.getAutocompletePrintablePair(portNodeId, parent.getMainWindow().getWorkStationDetails().getSidingCode(),LovItemType.SIDING);
			Pair<String,String> grade = !Utils.isNull(tpRecord.getGradeCode()) ?  LovDao.getAutocompletePrintablePair(portNodeId, tpRecord.getGradeCode(), LovItemType.MATERIAL_GRADE) : null;
			textBoxSource.setText(source == null ? "" : source.first);
			textSourceName.setText(source == null ? "" : source.second);
			textBoxTransporter.setText(transporter);
			textBoxDestination.setText(destination == null ? "" : destination.first);
			textDestinationName.setText(destination == null ? "" : destination.second);
			textBoxMaterialGrade.setText(grade == null ? "" : grade.first);
			textmaterialGradeName.setText(grade == null ? "" : grade.second);
			if(Utils.isNull(tpRecord.getLrNo())){
				textBoxLRNo.setMask(new SimpleDateFormat("dd").format(new Date())+parent.getMainWindow().getWorkStationDetails().getLrPrefixFirst()+"DDD"+parent.getMainWindow().getWorkStationDetails().getLrPrefixSecond()+"DDD");
			}else{
				textBoxLRNo.setText(tpRecord.getLrNo());
			}
			handleTareWt(conn, tpRecord, vehicle);
			calculateWeights();
			if(tpRecord.getChallanDate() == null)
				challanClock.play();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	TPRTareDetails tareInfo = null;
	private void handleTareWt(Connection conn,TPRecord tpRecord,Vehicle vehicle) throws Exception{
		tareInfo = TPRTareDetails.getTareInfo(conn, tpRecord, vehicle, true, Type.TPRMATERIAL.COAL_INTERNAL);
		if(tareInfo != null){
			SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			textNoTareAllowed.setText(tareInfo.isNoTareAllowed() ? "Yes" : "No");
			textLastTare.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
			textLastTareDate.setText(Misc.isUndef(tareInfo.getLastTareOn()) ? "" : (sdf.format(new Date(tareInfo.getLastTareOn()))));
			textLastTareExpired.setText(Misc.isUndef(tareInfo.getTareExpiareOn()) ? "" : (sdf.format(new Date(tareInfo.getTareExpiareOn()))));
			if(tareInfo.isNoTareAllowed() && (weighmentStep == WeighmentStep.gross)){
				textTareWt.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
			}
		}
	}
	@Override
	public void setVehicleName(String vehicleName) {
		textBoxVehicleName.setText(vehicleName);
	}

	@Override
	public void clearVehicleName() {
		textBoxVehicleName.setText("");
	}

	@Override
	public boolean setTPRAndSaveNonTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep) throws Exception {
		if(tpRecord == null || Misc.isUndef(tpRecord.getVehicleId()))
			return false;
		changeWeighment = false;
		double capturedWt = Misc.getParamAsDouble(textWBReading.getText());
		Vehicle veh = Vehicle.getVehicle(conn, tpRecord.getVehicleId());
		if(weighmentStep == WeighmentStep.tare){
			double avgTare = TPRTareDetails.getAvgTare(conn, true, tpRecord.getVehicleId());//veh.getAvgTare();
			if(Misc.isUndef(avgTare))
				avgTare = veh.getMinTare();
			double change = Misc.isUndef(avgTare) ? Misc.getUndefDouble() : Math.abs(avgTare-capturedWt);
			boolean isHigh = avgTare < capturedWt; 
			if(!Misc.isUndef(change) && !Misc.isUndef(parent.getMainWindow().getWorkStationDetails().getTareChangeThresHold()) && change > parent.getMainWindow().getWorkStationDetails().getTareChangeThresHold()){
				parent.showAlert(Alert.AlertType.WARNING, "System Exception Message", (isHigh ? UIConstant.MORE_TARE_FOUND : UIConstant.LESS_TARE_FOUND)+"\nAcceptable Tare Weight="+avgTare);
			}
			
		}else if(weighmentStep == WeighmentStep.gross){
			double maxGross = veh.getMinGross();
			double change = Misc.isUndef(maxGross) ? Misc.getUndefDouble() : capturedWt - maxGross;
			if(!Misc.isUndef(change) && !Misc.isUndef(parent.getMainWindow().getWorkStationDetails().getGrossChangeThresHold()) && change > parent.getMainWindow().getWorkStationDetails().getGrossChangeThresHold()){
				parent.showAlert(Alert.AlertType.ERROR, "System Exception Message", UIConstant.MORE_GROSS_FOUND+"\nAcceptable Gross Weight="+maxGross);
				changeWeighment = true;
				return false;
			}
		}
		String[] options = {"  Save  ", "  Cancel  "};
		String msg = "Vehicle Name: " + textBoxVehicleName.getText() + "\nTransporter: " + textBoxTransporter.getText()  + "\nCaptured Weight: " + Misc.printDouble(capturedWt,false);
		int responseVehicleDialog = parent.prompt("Data Saving Confirmation", msg, options);
		if (responseVehicleDialog == 1) {
			changeWeighment = true;
			return false;
		}

		//tpRecord.setMaterialCat(menuItemInfo.getMaterialCat());
		tpRecord.setMaterialCat(menuItemInfo.getTprWorkstionConfig(readerId).getMaterialCat());
		tpRecord.setMinesCode(LovDao.getAutoCompleteValue(textBoxSource));
		tpRecord.setRfMinesCode(LovDao.getAutoCompleteValue(textBoxSource));
		tpRecord.setTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setRfTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setDestinationCode(LovDao.getAutoCompleteValue(textBoxDestination));
		tpRecord.setRfDestinationCode(LovDao.getAutoCompleteValue(textBoxDestination));
		tpRecord.setGradeCode(LovDao.getAutoCompleteValue(textBoxMaterialGrade));
		tpRecord.setRfGradeCode(LovDao.getAutoCompleteValue(textBoxMaterialGrade));
		tpRecord.setLrNo(textBoxLRNo.getText());
		if(weighmentStep == WeighmentStep.gross){
			tpRecord.setLoadGross(capturedWt);
			tpRecord.setRfLoadGross(capturedWt);
			if(tareInfo.isNoTareAllowed()){
				tpRecord.setLoadTare(tareInfo.getTare());
				tpRecord.setRfLoadTare(tareInfo.getTare());
				tpRecord.setLatestLoadWbInExit(Misc.isUndef(tareInfo.getLastTareOn()) ? null : new Date(tareInfo.getLastTareOn()));
				Mines destination = Mines.getMines(conn, LovDao.getAutoCompleteValue(textBoxDestination), Misc.getUndefInt());
				if(destination != null && destination.getType() == 6){//stock
					tpRecord.setTprStatus(TPR.CLOSE);
				}
				return true;
			}else{
				parent.showAlert(Alert.AlertType.WARNING, "System Exception Message", UIConstant.NO_TARE_FOUND);
				return false;
			}
			
		}if(weighmentStep == WeighmentStep.tare){
			tpRecord.setLoadTare(capturedWt);
			tpRecord.setRfLoadTare(capturedWt);
	    	RFIDMasterDao.executeQuery(conn, "update vehicle set flyash_tare="+capturedWt+", flyash_tare_time=now() where id="+tpRecord.getVehicleId());
	    	return true;
		}
		return false;
		/*if(tpRecord.getEarliestLoadWbOutEntry() == null)
			tpRecord.setEarliestLoadWbOutEntry(tpStep.getEntryTime());
		tpRecord.setLatestLoadWbOutExit(new Date(System.currentTimeMillis()));*/
	}

	@Override
	public void enableManualEntry(boolean enable) {
		//textBoxVehicleName.setEditable(enable);
		textBoxVehicleName.setDisable(!enable);
	}

	@Override
	public Pair<Boolean, String> requestFocusNextField(NodeExt currentField) {
		boolean next = true;
		String nextFieldId = null;
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			if(currentField.isVehicle()){
				Triple<Boolean, Integer, Integer> vehicleTriple = parent.checkAndGetVehiclId((TextField) currentField.getNode());
				if(vehicleTriple != null && !Misc.isUndef(vehicleTriple.third)){
					next = true;
					parent.handleManualEntry(conn, Misc.getUndefInt(), vehicleTriple.first, vehicleTriple.second, vehicleTriple.third, ((TextField)currentField.getNode()).getText());
				}else{
					next = false;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			destroyIt = true;
		}finally{
			try{
				DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return new Pair<>(next,nextFieldId);
	}

	@Override
	public boolean isPrintable() {
		return true;
	}

	@Override
	public boolean isManualEntry() {
		return true;
	}

	@Override
	public boolean print(TPRecord tpRecord, int workStationTypeId) {
		return false;
	}

	@Override
	public HashMap<Integer, Integer> getBlockingQuestions() {
		return null;
	}

	@Override
	public ArrayList<String> getInstruction() {
		return null;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@Override
	public void setWeighBridgeReading(String reading) {
		// TODO Auto-generated method stub
		int val = Misc.getParamAsInt(reading);
		if (!Misc.isUndef(val)) {
			int currVal = Misc.getParamAsInt(textWBReading.getText());
			if (Misc.isUndef(currVal) || (currVal >= val ? currVal - val : val - currVal) >= 10 || val == 0) {
				textWBReading.setText((double)val/1000.0 + "");
				if(changeWeighment){
					if(weighmentStep == WeighmentStep.gross){
						//textTareWt.setText("");
						textGrossWt.setText((double)val/1000.0 + "");
					}else if(weighmentStep == WeighmentStep.tare){
						//textGrossWt.setText("");
						textTareWt.setText((double)val/1000.0 + "");
					}
					calculateWeights();
				}
				//to do other
			}
		}
	}
	private void calculateWeights(){
		double tare = Misc.getParamAsDouble(textTareWt.getText());
		double gross = Misc.getParamAsDouble(textGrossWt.getText());
		double net = !Misc.isUndef(tare) && !Misc.isUndef(gross) ? (gross-tare) : Misc.getUndefDouble();
		textNetWt.setText(Misc.isUndef(net) ? "" : net+"");
	}
	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hideActionBar() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh) {
		if(parent == null)
			return null ;
		if(weighmentStep == WeighmentStep.noWeight){
			TPRTareDetails tareInfo = null;
			try {
				tareInfo = TPRTareDetails.getTareInfo(conn, null, veh, true, Type.TPRMATERIAL.COAL_INTERNAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setWeighmentMode(tareInfo != null && tareInfo.isNoTareAllowed() ? WeighmentStep.gross : WeighmentStep.tare ,false);
		}
		return new TPRWorkstationConfig(weighmentStep == WeighmentStep.gross ? Type.WorkStationType.SECL_LOAD_INT_WB_GROSS : Type.WorkStationType.SECL_LOAD_INT_WB_TARE, Type.TPRMATERIAL.COAL_INTERNAL);
	}
	@Override
	public void handleAutoComplete(NodeExt nodeExt, Pair<String, String> codeNamePair) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resetWeighmentMode() {
		// TODO Auto-generated method stub
		setWeighmentMode(WeighmentStep.noWeight, false);
	}
}

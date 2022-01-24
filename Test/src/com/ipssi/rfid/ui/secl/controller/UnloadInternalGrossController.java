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
import com.ipssi.rfid.beans.TPRTareDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.secl.data.DateUtils;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tornadofx.control.DateTimePicker;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class UnloadInternalGrossController implements ControllerI,Initializable {
	@FXML
	private JFXTextField textBoxVehicleName;
	@FXML
	private JFXTextField textBoxChallanNo;
	@FXML
	private DateTimePicker datePickerChallanDate;
	@FXML
	private Label textWBReading;
	@FXML
	private Label textGPSVoilationCriticality;
	@FXML
	private Label textListOfViolation;
	@FXML
	private Label textNoTareAllowed;
	@FXML
	private Label textLastTareDate;
	@FXML
	private Label textLastTareExpired;
	@FXML
	private Label textLastTare;
	@FXML
	private Label textGrossShort;
	@FXML
	private Label textNetShort;
	@FXML
	private JFXTextField textBoxLoadGross;
	@FXML
	private JFXTextField textBoxLoadTare;
	@FXML
	private Label textBlockStatus;
	@FXML
	private JFXCheckBox checkBoxPaperMatchYes;
	@FXML
	private JFXCheckBox checkBoxPaperMatchNo;
	@FXML
	private JFXTextField textBoxTransporter;
	@FXML
	private JFXTextField textBoxSource;
	@FXML
	private JFXTextField textBoxMaterialGrade;
	MainController parent;
	Parent rootView;
	MenuItemInfo menuItemInfo;
	@Override
	public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
		this.rootView = rootView;
		this.parent = parent;
		this.menuItemInfo = menuItemInfo;
	}

	@Override
	public void clearInputs() {
		
		
		tareInfo = null;
		if(TokenManager.isSimulateWB){
			Random r = new Random();
			int value =  r.nextInt(10)*500 + 30000; 
			setWeighBridgeReading(value+"");
		}else{
    		textWBReading.setText("");
    	}
		isPaperChallanMatched = false;
		togglePaperChallanMatched();
	}
	private void disableMinesData(){
		textBoxTransporter.setDisable(isPaperChallanMatched);
		textBoxSource.setDisable(isPaperChallanMatched);
		textBoxMaterialGrade.setDisable(isPaperChallanMatched);
		datePickerChallanDate.setDisable(isPaperChallanMatched);
		textBoxChallanNo.setDisable(isPaperChallanMatched);
		textBoxLoadTare.setDisable(isPaperChallanMatched);
		textBoxLoadGross.setDisable(isPaperChallanMatched);
	}
	@Override
	public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {

		if(tpRecord == null)
			return;
		VehicleExtended vehicleExt;
		try {
			vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
			textBoxVehicleName.setText(tpRecord.getVehicleName());
			textBoxChallanNo.setText(tpRecord.getChallanNo());
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
	        String source = !Utils.isNull(tpRecord.getMinesCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getMinesCode(), LovItemType.MINES) : LovDao.getAutocompletePrintable(portNodeId, parent.getMainWindow().getWorkStationDetails().getMinesCode(),LovItemType.MINES);
            String transporter = !Utils.isNull(tpRecord.getTransporterCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER) : vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER);
			String grade = !Utils.isNull(tpRecord.getGradeCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getGradeCode(), LovItemType.MATERIAL_GRADE) : "";
			textBoxSource.setText(source);
			textBoxTransporter.setText(transporter);
			textBoxMaterialGrade.setText(grade);
			textBoxLoadTare.setText(Misc.isUndef(tpRecord.getLoadTare()) ? "" : tpRecord.getLoadTare()+"");
			textBoxLoadGross.setText(Misc.isUndef(tpRecord.getLoadGross()) ? "" : tpRecord.getLoadGross()+"");
			if(tpRecord.getChallanDate() == null){//to do
				datePickerChallanDate.setValue(DateUtils.asLocalDate(new Date()));
			}else{
				datePickerChallanDate.getEditor().setText(new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM).format(tpRecord.getChallanDate()));
			}
			isPaperChallanMatched = !Utils.isNull(tpRecord.getChallanNo());
			togglePaperChallanMatched();
			handleTareWt(conn, tpRecord, vehicle);
			calculateWeights();
			//parent.handleNextFocus(null, 0);
		}catch (Exception ex){
			ex.printStackTrace();
		}

	}
	TPRTareDetails tareInfo = null;
	private void handleTareWt(Connection conn,TPRecord tpRecord,Vehicle vehicle) throws Exception{
		tareInfo = TPRTareDetails.getTareInfo(conn, tpRecord, vehicle, false, Type.TPRMATERIAL.COAL_INTERNAL);
		if(tareInfo != null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
			textNoTareAllowed.setText(tareInfo.isNoTareAllowed() ? "Yes" : "No");
			textLastTare.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
			textLastTareDate.setText(Misc.isUndef(tareInfo.getLastTareOn()) ? "" : (sdf.format(new Date(tareInfo.getLastTareOn()))));
			textLastTareExpired.setText(Misc.isUndef(tareInfo.getTareExpiareOn()) ? "" : (sdf.format(new Date(tareInfo.getTareExpiareOn()))));
		}
	}

	@Override
	public void setVehicleName(String vehicleName) {

	}

	@Override
	public void clearVehicleName() {

	}

	@Override
	public boolean setTPRAndSaveNonTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep) throws Exception {
		if(tpRecord == null || Misc.isUndef(tpRecord.getVehicleId()))
			return false;
		double capturedWt = Misc.getParamAsDouble(textWBReading.getText());
		String[] options = {"  Cancel  ", "  Save  "};
		String msg = "Vehicle Name: " + textBoxVehicleName.getText() + "\nTransporter: " + textBoxTransporter.getText() + "\nCaptured Weight: " + Misc.printDouble(capturedWt,false);
		int responseVehicleDialog = parent.prompt("Data Saving Confirmation", msg, options);
		if (responseVehicleDialog == 0) {
			return false;
		}
		tpRecord.setMaterialCat(menuItemInfo.getTprWorkstionConfig(readerId).getMaterialCat());
		tpRecord.setMinesCode(LovDao.getAutoCompleteValue(textBoxSource));
		tpRecord.setTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setGradeCode(LovDao.getAutoCompleteValue(textBoxMaterialGrade));
		tpRecord.setLoadTare(Misc.getParamAsDouble(textBoxLoadTare.getText()));
		tpRecord.setLoadGross(Misc.getParamAsDouble(textBoxLoadGross.getText()));
		tpRecord.setChallanNo(textBoxChallanNo.getText());
		tpRecord.setChallanDate(DateUtils.asDate(datePickerChallanDate.getValue()));
		tpRecord.setUnloadGross(capturedWt);
		if(tareInfo.isNoTareAllowed()){
			tpRecord.setUnloadTare(tareInfo.getTare());
			tpRecord.setLatestUnloadWbOutExit(Misc.isUndef(tareInfo.getLastTareOn()) ? null : new Date(tareInfo.getLastTareOn()));
		}
		return true;
	}

	@Override
	public void enableManualEntry(boolean enable) {
		textBoxVehicleName.setEditable(enable);
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
				textWBReading.setText((double)val/1000 + "");
				//to do other
				calculateWeights();
			}
		}
	}
	private void calculateWeights(){
		double loadTare = Misc.getParamAsDouble(textBoxLoadTare.getText());
		double loadGross = Misc.getParamAsDouble(textBoxLoadGross.getText());
		double unloadgross = Misc.getParamAsDouble(textWBReading.getText());
		double unloadtare = Misc.getParamAsDouble(textLastTare.getText());
		double grossShort = !Misc.isUndef(unloadgross) && !Misc.isUndef(loadGross) ? (loadGross-unloadgross) : Misc.getUndefDouble();
		double netShort = !Misc.isUndef(grossShort) && !Misc.isUndef(loadTare) && !Misc.isUndef(unloadtare) ? (grossShort+unloadtare-loadTare) : Misc.getUndefDouble();
		textGrossShort.setText(Misc.isUndef(grossShort) ? "" : grossShort+"");
		textNetShort.setText(Misc.isUndef(netShort) ? "" : netShort+"");
	}
	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}
	private boolean isPaperChallanMatched = false;
	public void checkBoxPaperMatch(ActionEvent event) {
		JFXCheckBox source = (JFXCheckBox) event.getSource();
		isPaperChallanMatched =  "checkBoxPaperMatchYes".equalsIgnoreCase(source.getId());
		togglePaperChallanMatched();
	}
	public void togglePaperChallanMatched(){
		if(isPaperChallanMatched){
			checkBoxPaperMatchNo.setSelected(false);
			checkBoxPaperMatchNo.setDisable(false);
			checkBoxPaperMatchYes.setSelected(true);
			checkBoxPaperMatchYes.setDisable(true);
		}else{
			checkBoxPaperMatchNo.setSelected(true);
			checkBoxPaperMatchNo.setDisable(true);
			checkBoxPaperMatchYes.setSelected(false);
			checkBoxPaperMatchYes.setDisable(false);
			
		}
		parent.handleNextFocus(null, 0);
		disableMinesData();
	}
	
	@Override
	public boolean hideActionBar() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TPRWorkstationConfig getWorkstationConfig(Connection conn, int readerId, Vehicle veh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleAutoComplete(NodeExt nodeExt, Pair<String, String> codeNamePair) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetWeighmentMode() {
		// TODO Auto-generated method stub
		
	}

}
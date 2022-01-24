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
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovUtils;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class LoadInternalGrossController implements ControllerI,Initializable {
	@FXML
	private JFXTextField textBoxVehicleName;
	@FXML
	private ComboBox<?> comboBoxTransporter;
	@FXML
	private Label textWBReading;
	@FXML
	private JFXTextField textBoxTransporter;
	@FXML
	private JFXTextField textBoxSource;
	@FXML
	private JFXTextField textBoxDestination;
	@FXML
	private JFXTextField textBoxMaterialGrade;
	@FXML
    private JFXTextField textBoxWashery;
	@FXML
	private Label textChallanNo;
	@FXML
	private Label textNoTareAllowed;
	@FXML
	private Label textLastTareExpired;
	@FXML
	private Label textLastTareDate;
	@FXML
	private Label textLastTare;
	@FXML
	private Label textTareWt;
	@FXML
	private Label textNetWt;
	@FXML
	private Label textChallanDate;

	MainController parent;
	Parent rootView;
	MenuItemInfo menuItemInfo;
	DigitalClockExt challanClock;
	@Override
	public void init(MainController parent, Parent rootView, MenuItemInfo menuItemInfo) {
		this.rootView = rootView;
		this.parent = parent;
		this.menuItemInfo = menuItemInfo;
		if(challanClock == null)
			challanClock = new DigitalClockExt(textChallanDate, ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
	}

	@Override
	public void clearInputs() {
		if(TokenManager.isSimulateWB){
			Random r = new Random();
			int value =  r.nextInt(10)*500 + 30000; 
			setWeighBridgeReading(value+"");
		}else{
    		textWBReading.setText("");
    	}
		challanClock.stop();
		tareInfo = null;
	}
	@Override
	public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {
		if(tpRecord == null)
			return;
		VehicleExtended vehicleExt;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
			textBoxVehicleName.setText(tpRecord.getVehicleName());
			textChallanNo.setText(tpRecord.getChallanNo());
			textChallanDate.setText(tpRecord.getChallanDate() == null ? "" : sdf.format(tpRecord.getChallanDate()));
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			String source = !Utils.isNull(tpRecord.getMinesCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getMinesCode(), LovItemType.MINES) : LovDao.getAutocompletePrintable(portNodeId, parent.getMainWindow().getWorkStationDetails().getMinesCode(),LovItemType.MINES);
            String transporter = !Utils.isNull(tpRecord.getTransporterCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER) : vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER);
            String destination = !Utils.isNull(tpRecord.getDestinationCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getDestinationCode(), LovItemType.SIDING) : LovDao.getAutocompletePrintable(portNodeId, parent.getMainWindow().getWorkStationDetails().getSidingCode(),LovItemType.SIDING);
			String washery = !Utils.isNull(tpRecord.getWasheryCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getWasheryCode(), LovItemType.WASHERY) : "";
			String grade = !Utils.isNull(tpRecord.getGradeCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getGradeCode(), LovItemType.MATERIAL_GRADE) : "";
			textBoxSource.setText(source);
			textBoxTransporter.setText(transporter);
			textBoxDestination.setText(destination);
			textBoxWashery.setText(washery);
			textBoxMaterialGrade.setText(grade);
			/*LovUtils.setLov(conn,portNodeId, comboBoxTransporter, LovDao.LovItemType.TRANSPORTER, transportId);
			LovUtils.setLov(conn,portNodeId, comboBoxSource, LovDao.LovItemType.MINES, sourceId);
			LovUtils.setLov(conn,portNodeId, comboBoxDestination, LovDao.LovItemType.SIDING, destinationId);
			LovUtils.setLov(conn,portNodeId, comboBoxWashery, LovDao.LovItemType.WASHERY, washeryId);
			LovUtils.setLov(conn,portNodeId, comboBoxMaterialGrade, LovDao.LovItemType.MATERIAL_GRADE, gradeId);*/
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
			textTareWt.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
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
		double capturedWt = Misc.getParamAsDouble(textWBReading.getText());
		String[] options = {"  Cancel  ", "  Save  "};
		String msg = "Vehicle Name: " + textBoxVehicleName.getText() + "\nTransporter: " + LovUtils.getTextValue(comboBoxTransporter) + "\nCaptured Weight: " + Misc.printDouble(capturedWt,false);
		int responseVehicleDialog = parent.prompt("Data Saving Confirmation", msg, options);
		if (responseVehicleDialog == 0) {
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
		tpRecord.setWasheryCode(LovDao.getAutoCompleteValue(textBoxWashery));
		tpRecord.setRfWasheryCode(LovDao.getAutoCompleteValue(textBoxWashery));
		tpRecord.setLoadGross(capturedWt);
		tpRecord.setRfLoadGross(capturedWt);
		if(tareInfo.isNoTareAllowed()){
			tpRecord.setLoadTare(tareInfo.getTare());
			tpRecord.setRfLoadTare(tareInfo.getTare());
			tpRecord.setLatestLoadWbInExit(Misc.isUndef(tareInfo.getLastTareOn()) ? null : new Date(tareInfo.getLastTareOn()));
		}
		/*if(tpRecord.getEarliestLoadWbOutEntry() == null)
			tpRecord.setEarliestLoadWbOutEntry(tpStep.getEntryTime());
		tpRecord.setLatestLoadWbOutExit(new Date(System.currentTimeMillis()));*/
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
				textWBReading.setText((double)val/1000.0 + "");
				calculateWeights();
				//to do other
			}
		}
	}
	private void calculateWeights(){
		double tare = Misc.getParamAsDouble(textTareWt.getText());
		double gross = Misc.getParamAsDouble(textWBReading.getText());
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

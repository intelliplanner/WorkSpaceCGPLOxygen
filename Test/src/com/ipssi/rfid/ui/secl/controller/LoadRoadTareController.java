package com.ipssi.rfid.ui.secl.controller;

import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.custom.Printer;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.rfid.ui.secl.data.TPRDataUtils;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class LoadRoadTareController implements ControllerI,Initializable {
	@FXML
    private Label textAllotedQty;

    @FXML
    private Label textBalancedQty;

    @FXML
    private JFXTextField textBoxVehicleName;
    @FXML
    private Label textWBReading;
    @FXML
    private JFXTextField textBoxDONo;
    @FXML
    private Label textChallanNo;
    @FXML
    private Label textCustomerCode;
    @FXML
    private Label textSource;
    @FXML
    private Label textCustomerName;
    @FXML
    private Label textDestinationName;
    @FXML
    private Label textMaterialGrade;
    @FXML
    private Label textDestinationCode;
    @FXML
    private Label textChallanDate;
    @FXML
    private JFXTextField textBoxTransporter;
    @FXML
    private JFXTextField textBoxWashery;
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
        	this.challanClock = new DigitalClockExt(textChallanDate, ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
    }

	@Override
	public void clearInputs() {
    	if(TokenManager.isSimulateWB){
    		Random r = new Random();
    		int value =  r.nextInt(10)*500 + 10000; 
    		setWeighBridgeReading(value+"");
    	}else{
    		textWBReading.setText("");
    	}
    	challanClock.stop();
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
            String transporter = !Utils.isNull(tpRecord.getTransporterCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER) : vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER);
			textBoxTransporter.setText(transporter);
			handleDoInformation(conn, tpRecord != null && !Misc.isUndef(tpRecord.getDoId())? tpRecord.getDoNumber() : vehicle != null ? vehicle.getDoAssigned() : null,Misc.getUndefInt() );
			if(tpRecord.getChallanDate() == null)
				challanClock.play();
        }catch (Exception ex){
            ex.printStackTrace();
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
        String msg = "Vehicle Name: " + textBoxVehicleName.getText() + "\nTransporter: " + textBoxTransporter.getText() + "\nCaptured Weight: " + Misc.printDouble(capturedWt,false);
        int responseVehicleDialog = parent.prompt("Data Saving Confirmation", msg, options);
        if (responseVehicleDialog == 0) {
            return false;
        }
		TPRDataUtils.updateVehicleLoadTare(conn, tpRecord.getVehicleId(), Misc.getParamAsDouble(textWBReading.getText()), System.currentTimeMillis());
		/*DoDetails doDetails = DoDetails.getDODetails(conn, textBoxDONo.getText(), Misc.getUndefInt());
		int materialGrade = doDetails == null ? Misc.getUndefInt() : doDetails.getGrade();
		int mines = doDetails == null ? Misc.getUndefInt() : doDetails.getSourceMines();
		int destination = doDetails == null ? Misc.getUndefInt() : doDetails.getDestination();
		int doId = doDetails.getId();
		
		tpRecord.setDoId(doId);
		tpRecord.setRfDOId(doId);
		tpRecord.setMinesId(mines);
		tpRecord.setRfMinesId(mines);
		tpRecord.setMaterialGradeId(materialGrade);
		tpRecord.setRfGrade(materialGrade);
		tpRecord.setPlantId(destination);
		int transporterId = LovUtils.getIntValue(comboBoxTransporter);
		tpRecord.setTransporterId(transporterId);
		tpRecord.setRfTransporterId(transporterId);
		*/
		tpRecord.setDoNumber(textBoxDONo.getText());
		tpRecord.setMinesCode(LovDao.getAutoCompleteValue(textSource));
		tpRecord.setRfMinesCode(LovDao.getAutoCompleteValue(textSource));
		tpRecord.setGradeCode(LovDao.getAutoCompleteValue(textMaterialGrade));
		tpRecord.setRfGradeCode(LovDao.getAutoCompleteValue(textMaterialGrade));
		tpRecord.setDestinationCode(LovDao.getAutoCompleteValue(textDestinationCode));
		tpRecord.setTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setRfTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setWasheryCode(LovDao.getAutoCompleteValue(textBoxWashery));
		tpRecord.setLoadTare(capturedWt);
		tpRecord.setRfLoadTare(capturedWt);
		return true;
	}

	@Override
	public void enableManualEntry(boolean enable) {
		textBoxVehicleName.setEditable(enable);
	}

	@Override
	public Pair<Boolean, String> requestFocusNextField(NodeExt currentField) {
		boolean destroyIt = false;
        boolean next = true;
        String nextFieldId = null;
        Connection conn = null;
        try{
        	conn = DBConnectionPool.getConnectionFromPoolNonWeb();
        	if(currentField.isVehicle()){
        		Triple<Boolean, Integer, Integer> vehicleTriple = parent.checkAndGetVehiclId((TextField) currentField.getNode());
        		System.out.println("vehicleManual");
        		if(vehicleTriple != null && !Misc.isUndef(vehicleTriple.third)){
        			next = true;
        			
        			parent.handleManualEntry(conn, Misc.getUndefInt(), vehicleTriple.first, vehicleTriple.second, vehicleTriple.third, ((TextField)currentField.getNode()).getText());
        		}else{
        			next = false;
        		}
        	}else if(currentField.isAutoComplete() && currentField.getAutoCompleteSrc() == LovItemType.DO){
        		next = handleDoInformation(conn, ((TextField)currentField.getNode()).getText(),Misc.getUndefInt());
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
	private boolean handleDoInformation(Connection conn,String doNumber,int doId) throws Exception{
		boolean next = false;
		DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, doId);
		if(doDetails != null){
			LatestDOInfo latestDOInfo = DoDetails.getLatestDOInfo(conn,  doNumber, parent.getMainWindow().getWorkStationDetails().getCode());
			Pair<Integer,String> isDoValid = doDetails.isDOValid(conn,parent.getMainWindow().getWorkStationDetails().getCode(),latestDOInfo);
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			if(isDoValid == null || isDoValid.first != UIConstant.YES){
				textBoxDONo.setText("");
				textSource.setText("");
				textBoxWashery.setText("");;
				textBoxDONo.setText("");
				textCustomerName.setText("");
				textCustomerCode.setText("");
				textDestinationName.setText("");
				textDestinationCode.setText("");
				textAllotedQty.setText("");
				textBalancedQty.setText("");
				textMaterialGrade.setText("");
			}else{
				Pair<String, String> customer = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getCustomerCode(), LovItemType.CUSTOMER);
				Pair<String, String> destination = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
				Pair<Boolean, String> grade = LovDao.getSuggestionPairByCode(portNodeId, doDetails.getGradeCode(), LovItemType.MATERIAL_GRADE);
				textSource.setText(LovDao.getAutocompletePrintable(portNodeId, doDetails.getSourceCode(), LovItemType.MINES));
				textBoxDONo.setText(doDetails.getDoNumber());
				textCustomerName.setText(customer.second);
				textCustomerCode.setText(customer.first);
				textDestinationName.setText(destination.second);
				textDestinationCode.setText(destination.first);
				textMaterialGrade.setText(grade.second);
				textBoxWashery.setText(LovDao.getAutocompletePrintable(portNodeId, doDetails.getWasheryCode(), LovItemType.WASHERY));
				//textAllotedQty.setText(doDetails.getDoDate() == null ? "" : sdf.format(doDetails.getDoDate()));
				textAllotedQty.setText(Misc.isUndef(doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())) ? "" : doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())+"");
				double liftedQty = latestDOInfo != null ? latestDOInfo.getLastLiftedQty() : Misc.getUndefDouble();
				double doRemainingQty = Misc.isUndef(doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())) ? Misc.getUndefDouble() : doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode()) - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) + (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
				textBalancedQty.setText(Printer.printDouble(doRemainingQty));
				next = true;
			}
			
		}
		return next;
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
				textWBReading.setText(val + "");
				textWBReading.setText((double)val/1000.0 + "");
				//to do other
			}
		}
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

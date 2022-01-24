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
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.TPRTareDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.beans.TPStep;
import com.ipssi.rfid.beans.Vehicle;
import com.ipssi.rfid.beans.VehicleExtended;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.constant.UIConstant;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.readers.TPRWorkstationConfig;
import com.ipssi.rfid.ui.custom.Printer;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;
import com.ipssi.rfid.ui.secl.data.MenuItemInfo;
import com.ipssi.shift.ShiftBean;
import com.ipssi.shift.ShiftInformation;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 13-Oct-16.
 */
public class LoadRoadGrossController implements ControllerI ,Initializable{
    @FXML
    private JFXTextField textBoxVehicleName;
    @FXML
    private JFXTextField textBoxDONo;
    @FXML
    private Label textWBReading;
    @FXML
    private Label textChallanNo;
    @FXML
    private Label textCustomerCode;
    @FXML
    private Label textWashery;
    @FXML
    private Label textCustomerName;
    @FXML
    private Label textSource;
    @FXML
    private Label textNoTareAllowed;
    @FXML
    private Label textLastTareDate;
    @FXML
    private Label textLastTareExpired;
    @FXML
    private Label textLastTare;
    @FXML
    private Label textTareWt;
    @FXML
    private Label textNetWt;
    @FXML
    private Label textAllotedQty;
    @FXML
    private Label textBalancedQty;
    @FXML
    private Label textDestinationCode;
    @FXML
    private Label textDestination;
    @FXML
    private Label textChallanDate;
    @FXML
    private Label textmaterialGrade;
    @FXML
    private JFXTextField textBoxTransporter;
    @FXML
    private Label textTripsToday;
    @FXML
    private Label textQuota;
    MainController parent;
	Parent rootView;
	MenuItemInfo menuItemInfo;
    private TPRecord tpRecord=null;
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
    		int value =  r.nextInt(10)*500 + 30000; 
    		setWeighBridgeReading(value+"");
    	}else{
    		textWBReading.setText("");
    	}
    	tpRecord = null;
    	tareInfo = null;
    	challanClock.stop();
	}
	
	@Override
	public void populateTPRData(int readerId, Connection conn, TPRecord tpRecord, TPStep tpStep, Vehicle vehicle) {
		if(tpRecord == null)
            return;
        VehicleExtended vehicleExt;
        try {
        	this.tpRecord = tpRecord;
        	SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			vehicleExt = vehicle == null ? null : vehicle.getVehicleExt();
			textBoxVehicleName.setText(tpRecord.getVehicleName());
			textChallanNo.setText(tpRecord.getChallanNo());
			textChallanDate.setText(tpRecord.getChallanDate() == null ? "" : sdf.format(tpRecord.getChallanDate()));
            textTareWt.setText(Misc.isUndef(tpRecord.getLoadTare()) ? "" : tpRecord.getLoadTare()+"");
            int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
            String transporter = !Utils.isNull(tpRecord.getTransporterCode()) ?  LovDao.getAutocompletePrintable(portNodeId, tpRecord.getTransporterCode(), LovItemType.TRANSPORTER) : vehicleExt == null || vehicleExt.getTransporterCode() == null ? "" : LovDao.getAutocompletePrintable(portNodeId, vehicleExt.getTransporterCode(), LovItemType.TRANSPORTER);
			textBoxTransporter.setText(transporter);
            handleDoInformation(conn, tpRecord != null && !Misc.isUndef(tpRecord.getDoId())? tpRecord.getDoNumber() : vehicle != null ? vehicle.getDoAssigned() : null,Misc.getUndefInt() );
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
		tareInfo = TPRTareDetails.getTareInfo(conn, tpRecord, vehicle, true, Type.TPRMATERIAL.COAL_ROAD);
		if(tareInfo != null){
			SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYYYY_HHMM);
			textNoTareAllowed.setText(tareInfo.isNoTareAllowed() ? "Yes" : "No");
			textLastTare.setText(Misc.isUndef(tareInfo.getTare()) ? "" : tareInfo.getTare()+"");
			textLastTareDate.setText(Misc.isUndef(tareInfo.getLastTareOn()) ? "" : (sdf.format(new Date(tareInfo.getLastTareOn()))));
			textLastTareExpired.setText(Misc.isUndef(tareInfo.getTareExpiareOn()) ? "" : (sdf.format(new Date(tareInfo.getTareExpiareOn()))));
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
        DoDetails doDetails = DoDetails.getDODetails(conn, textBoxDONo.getText(), Misc.getUndefInt());
        /*int materialGrade = doDetails == null ? Misc.getUndefInt() : doDetails.getGrade();
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
		tpRecord.setRfTransporterId(transporterId);*/
		double loadGross = capturedWt;
		double loadTare = tpRecord.getLoadTare();
		tpRecord.setDoNumber(textBoxDONo.getText());
		tpRecord.setMinesCode(LovDao.getAutoCompleteValue(textSource));
		tpRecord.setRfMinesCode(LovDao.getAutoCompleteValue(textSource));
		tpRecord.setGradeCode(LovDao.getAutoCompleteValue(textmaterialGrade));
		tpRecord.setRfGradeCode(LovDao.getAutoCompleteValue(textmaterialGrade));
		tpRecord.setDestinationCode(LovDao.getAutoCompleteValue(textDestinationCode));
		tpRecord.setTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setRfTransporterCode(LovDao.getAutoCompleteValue(textBoxTransporter));
		tpRecord.setLoadGross(capturedWt);
        tpRecord.setRfLoadGross(capturedWt);
        if(tareInfo.isNoTareAllowed()){
        	if(tpRecord.getLoadTare() <= 0){
        		loadTare = tareInfo.getTare();
        		tpRecord.setLoadTare(tareInfo.getTare());
        		tpRecord.setRfLoadTare(tareInfo.getTare());
        		tpRecord.setLatestLoadWbInExit(Misc.isUndef(tareInfo.getLastTareOn()) ? null : new Date(tareInfo.getLastTareOn()));
        	}
        	double net = Misc.isUndef(loadGross) || Misc.isUndef(loadTare) ? Misc.getUndefDouble() : loadGross - loadTare;
			if(!Misc.isUndef(net)){
				//int shiftId = ShiftInformation.getFirstShiftId(parent.getMainWindow().getWorkStationDetails().getPortNodeId(), conn, Misc.SCOPE_SHIFT, new Date());
				//ShiftBean shiftBean = ShiftInformation.getShiftById(parent.getMainWindow().getWorkStationDetails().getPortNodeId(), shiftId, conn);
				Date st = new Date();
//				TimePeriodHelper.setBegOfDate(st, Misc.SCOPE_SHIFT, shiftBean);
				TimePeriodHelper.setBegOfDate(st, Misc.SCOPE_DAY);
				String dayStart = Printer.getTimeStr(st);
				RFIDMasterDao.executeQuery(conn, "INSERT INTO current_do_status (do_id,do_number,lifted_qty,last_lifted_on,last_lifted_qty,last_lifted_vehicle_id,last_lifted_tpr_id,trips_count_daily,trips_count) VALUES ("+doDetails.getId()+",'"+doDetails.getDoNumber()+"',lifted_qty+"+net+",now(),"+net+","+tpRecord.getVehicleId()+","+tpRecord.getTprId()+",1,1) ON DUPLICATE KEY UPDATE lifted_qty=lifted_qty+"+net+",last_lifted_on=now(),last_lifted_qty="+net+",last_lifted_vehicle_id="+tpRecord.getVehicleId()+",last_lifted_tpr_id="+tpRecord.getTprId()+", trips_count_daily=(case when (timestampdiff(second, concat(date(last_lifted_on), ' "+dayStart+"'), last_lifted_on) < 0 or timestampdiff(second, DATE_ADD(concat(date(last_lifted_on), ' "+dayStart+"'),INTERVAL 86399 second),now()) > 0) then 1 else  trips_count_daily+1 end), trips_count=trips_count+1");
			}
        	return true;
        }
        return false;
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
        	}else if(currentField.isAutoComplete() && currentField.getAutoCompleteSrc() == LovItemType.DO){
        		//
        		next = handleDoInformation(conn, ((TextField)currentField.getNode()).getText(),Misc.getUndefInt());
        		if(!next)
        			((TextField)currentField.getNode()).setText("");
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
	private boolean handleDoInformation(Connection conn,String doNumber, int doId) throws Exception{
		boolean next = false;
		DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, doId);
		if(doDetails != null){
			LatestDOInfo latestDOInfo = DoDetails.getLatestDOInfo(conn,  doNumber, parent.getMainWindow().getWorkStationDetails().getCode());
			Pair<Integer,String> isDoValid = doDetails.isDOValid(conn,parent.getMainWindow().getWorkStationDetails().getCode(),latestDOInfo);
			int portNodeId = parent.getMainWindow().getWorkStationDetails().getPortNodeId();
			if(isDoValid == null || isDoValid.first != UIConstant.YES){
				textBoxDONo.setText("");
				textSource.setText("");
				textWashery.setText("");
				textBoxDONo.setText("");
				textCustomerName.setText("");
				textCustomerCode.setText("");
				textDestination.setText("");
				textDestinationCode.setText("");
				textAllotedQty.setText("");
				textBalancedQty.setText("");
				textmaterialGrade.setText("");
				textQuota.setText("");
			}else{
				Pair<String, String> customer = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getCustomerCode(), LovItemType.CUSTOMER);
				Pair<String, String> destination = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
				Pair<Boolean, String> grade = LovDao.getSuggestionPairByCode(portNodeId, doDetails.getGradeCode(), LovItemType.MATERIAL_GRADE);
				textSource.setText(LovDao.getAutocompletePrintable(portNodeId, (tpRecord != null && !Utils.isNull(this.tpRecord.getMinesCode())) ? tpRecord.getMinesCode() : doDetails.getSourceCode(), LovItemType.MINES));
				textBoxDONo.setText(doDetails.getDoNumber());
				textCustomerName.setText(customer.second);
				textCustomerCode.setText(customer.first);
				textDestination.setText(destination.second);
				textDestinationCode.setText(destination.first);
				textmaterialGrade.setText(grade.second);
				textWashery.setText(LovDao.getAutocompletePrintable(portNodeId, (tpRecord != null && !Utils.isNull(this.tpRecord.getWasheryCode())) ? tpRecord.getWasheryCode() : doDetails.getWasheryCode(), LovItemType.WASHERY));
				//textAllotedQty.setText(doDetails.getDoDate() == null ? "" : sdf.format(doDetails.getDoDate()));
				textAllotedQty.setText(Misc.isUndef(doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())) ? "" : doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())+"");
				double liftedQty = latestDOInfo != null ? latestDOInfo.getLastLiftedQty() : Misc.getUndefDouble();
				double doRemainingQty = Misc.isUndef(doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode())) ? Misc.getUndefDouble() : doDetails.getQtyAlloc(parent.getMainWindow().getWorkStationDetails().getCode()) - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) + (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
				textBalancedQty.setText(Printer.printDouble(doRemainingQty));
				textQuota.setText(Printer.print(doDetails.getQuota()));
				textTripsToday.setText(Printer.print(latestDOInfo == null ? Misc.getUndefInt() : latestDOInfo.getDailyTripsCount()));
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
				textWBReading.setText((double)val/1000 + "");
				calculateWeights();
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
